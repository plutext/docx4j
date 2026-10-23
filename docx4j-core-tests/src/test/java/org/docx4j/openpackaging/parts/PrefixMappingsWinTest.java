/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.openpackaging.parts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.NamespacePrefixMappings;
import org.docx4j.model.datastorage.CustomXmlDataStorage;
import org.docx4j.model.datastorage.CustomXmlDataStorageImpl;
import org.junit.Test;
import org.xml.sax.InputSource;

/**
 * CR-054: a binding's own {@code w:prefixMappings} win over docx4j's built-in prefix table when its XPath
 * is evaluated against a custom XML part. Measured 2026-10-22 on 17.3.4: {@code /p:root[1]/p:dd[1]} with
 * {@code xmlns:p='urn:probe:bind'} returned "" (the table maps {@code p} to PresentationML and was
 * consulted first); with {@code ns0} it returned the value. The context is now built per evaluation:
 * the binding's mappings, then the table, then the shared registered map.
 */
public class PrefixMappingsWinTest {

	private static final String PROBE = "urn:probe:bind";

	private static CustomXmlDataStoragePart part(String xml) throws Exception {
		CustomXmlDataStorage storage = new CustomXmlDataStorageImpl();
		storage.setDocument(XmlUtils.getNewDocumentBuilder().parse(new InputSource(new StringReader(xml))));
		CustomXmlDataStoragePart part = new CustomXmlDataStoragePart();
		part.setData(storage);
		return part;
	}

	private static CustomXmlDataStoragePart probe(String value) throws Exception {
		return part("<root xmlns='" + PROBE + "'><dd>" + value + "</dd></root>");
	}

	@Test
	public void aPrefixInTheTableMeansWhatTheBindingDeclares() throws Exception {
		CustomXmlDataStoragePart part = probe("v2");
		String mappings = "xmlns:p='" + PROBE + "' "; // as Word writes it, trailing space included
		assertEquals("v2", part.xpathGetString("/p:root[1]/p:dd[1]", mappings));
		assertEquals("v2", part.cachedXPathGetString("/p:root[1]/p:dd[1]", mappings));
		assertEquals(1, part.xpathGetNodes("/p:root[1]/p:dd[1]", mappings).size());
		assertTrue(part.xpathGetAsBoolean("/p:root[1]/p:dd[1] = 'v2'", mappings));
	}

	@Test
	public void evenWordsOwnPrefix() throws Exception {
		assertEquals("v2", probe("v2").xpathGetString("/w:root[1]/w:dd[1]", "xmlns:w='" + PROBE + "'"));
	}

	@Test
	public void doubleQuotesAndNoSpaceBetweenMappings() throws Exception {
		CustomXmlDataStoragePart part = part("<root xmlns='" + PROBE + "'><dd xmlns:q='urn:q'><q:x>deep</q:x></dd></root>");
		assertEquals("deep", part.xpathGetString("/p:root/p:dd/q:x", "xmlns:p=\"" + PROBE + "\"xmlns:q='urn:q'"));
	}

	/** Two parts, one JVM, {@code p} meaning a different namespace in each: each resolves its own. */
	@Test
	public void twoPartsEachResolveTheirOwnMappings() throws Exception {
		CustomXmlDataStoragePart one = part("<root xmlns='urn:one'><v>ONE</v></root>");
		CustomXmlDataStoragePart two = part("<root xmlns='urn:two'><v>TWO</v></root>");
		assertEquals("ONE", one.xpathGetString("/p:root/p:v", "xmlns:p='urn:one'"));
		assertEquals("TWO", two.xpathGetString("/p:root/p:v", "xmlns:p='urn:two'"));
		assertEquals("ONE", one.cachedXPathGetString("/p:root/p:v", "xmlns:p='urn:one'"));
		assertEquals("TWO", two.cachedXPathGetString("/p:root/p:v", "xmlns:p='urn:two'"));
		assertEquals("the other part's mapping does not leak", "", one.xpathGetString("/p:root/p:v", "xmlns:p='urn:two'"));
	}

	/** The table stays behind the mappings: an undeclared prefix docx4j knows still resolves. */
	@Test
	public void anUndeclaredTablePrefixStillResolves() throws Exception {
		CustomXmlDataStoragePart part = part("<root xmlns='http://schemas.openxmlformats.org/wordprocessingml/2006/main'><v>W</v></root>");
		assertEquals("W", part.xpathGetString("/w:root/w:v", null));
		assertEquals("W", part.xpathGetString("/w:root/w:v", "xmlns:p='" + PROBE + "'"));
	}

	/** The shared map {@code registerPrefixMappings} fills is still consulted, last. */
	@Test
	@SuppressWarnings("deprecation")
	public void theRegisteredMapIsStillConsultedLast() throws Exception {
		NamespacePrefixMappings.registerPrefixMappings("xmlns:zz9='urn:registered'");
		CustomXmlDataStoragePart part = part("<root xmlns='urn:registered'><v>R</v></root>");
		assertEquals("R", part.xpathGetString("/zz9:root/zz9:v", null));
		assertEquals("a declared mapping beats the registered one", "",
				part.xpathGetString("/zz9:root/zz9:v", "xmlns:zz9='urn:other'"));
	}

	@Test
	public void theParserReadsWhatTheSchemaAllows() {
		Map<String, String> m = NamespacePrefixMappings.parse("xmlns:ns0='urn:a' xmlns:b=\"urn:b\"xmlns:c = 'urn:c'\txmlns:d='urn:d'");
		assertEquals("urn:a", m.get("ns0"));
		assertEquals("urn:b", m.get("b"));
		assertEquals("urn:c", m.get("c"));
		assertEquals("urn:d", m.get("d"));
		assertEquals(4, m.size());
		assertTrue(NamespacePrefixMappings.parse(null).isEmpty());
		assertTrue(NamespacePrefixMappings.parse("  ").isEmpty());
		NamespacePrefixMappings context = NamespacePrefixMappings.of("xmlns:xs='http://www.w3.org/2001/XMLSchema'");
		assertEquals("a declared prefix is answered for its URI too", "xs", context.getPrefix("http://www.w3.org/2001/XMLSchema"));
	}
}
