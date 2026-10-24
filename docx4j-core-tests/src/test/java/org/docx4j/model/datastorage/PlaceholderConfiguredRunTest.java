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
package org.docx4j.model.datastorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.customXmlProperties.DatastoreItem;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.GlossaryDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CTDocPart;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;

/**
 * CR-063 D4: a configured placeholder run ({@code docx4j.model.datastorage.placeholder}) is still
 * honoured for a text control naming no entry, written into the glossary as an entry of its own, as
 * Word makes one for placeholder text of its own (check 65): named with 32 upper-case hex digits, with
 * its own GUID and the default entry's properties, its body one paragraph holding the run. One entry
 * per distinct run per document, reused across controls and across binding runs.
 *
 * <p>Its own class: the run is read once per JVM, so the property must be set before any binding, and
 * surefire forks one JVM per test class.</p>
 */
public class PlaceholderConfiguredRunTest {

	private static final String IMPL_PROPERTY = "docx4j.model.datastorage.BindingHandler.Implementation";
	private static final String ITEM_ID = "{A1B2C3D4-0000-4000-8000-000000000064}";
	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String CONFIGURED_TEXT = "Please enter a value.";

	@BeforeClass
	public static void configure() {
		Docx4jProperties.setProperty("docx4j.model.datastorage.placeholder", "OpenDoPE/placeholder-configured-test.xml");
	}

	private static String runControl(String alias, String xpath) {
		return "<w:p><w:sdt><w:sdtPr><w:alias w:val=\"" + alias + "\"/><w:tag w:val=\"" + alias + "\"/><w:id w:val=\"" + alias.hashCode() + "\"/>"
				+ "<w:dataBinding w:xpath=\"" + xpath + "\" w:storeItemID=\"" + ITEM_ID + "\"/><w:text/></w:sdtPr>"
				+ "<w:sdtContent><w:r><w:t>STALE</w:t></w:r></w:sdtContent></w:sdt></w:p>";
	}

	private static WordprocessingMLPackage template() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart main = pkg.getMainDocumentPart();
		main.setJaxbElement((org.docx4j.wml.Document) XmlUtils.unmarshalString("<w:document " + W + "><w:body>"
				+ runControl("One", "/root/empty") + runControl("Two", "/root/empty") + "</w:body></w:document>"));
		CustomXmlDataStoragePart cx = new CustomXmlDataStoragePart();
		CustomXmlDataStorage data = new CustomXmlDataStorageImpl();
		data.setDocument(XmlUtils.getNewDocumentBuilder().parse(new InputSource(new StringReader("<root><empty></empty></root>"))));
		cx.setData(data);
		main.addTargetPart(cx);
		CustomXmlDataStoragePropertiesPart props = new CustomXmlDataStoragePropertiesPart();
		DatastoreItem dsi = new org.docx4j.customXmlProperties.ObjectFactory().createDatastoreItem();
		dsi.setItemID(ITEM_ID);
		props.setJaxbElement(dsi);
		cx.addTargetPart(props);
		return reload(pkg);
	}

	private static WordprocessingMLPackage reload(WordprocessingMLPackage pkg) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		pkg.save(bos);
		return WordprocessingMLPackage.load(new ByteArrayInputStream(bos.toByteArray()));
	}

	private static WordprocessingMLPackage bind(WordprocessingMLPackage pkg, String implementation) throws Exception {
		Docx4jProperties.setProperty(IMPL_PROPERTY, implementation);
		try {
			Docx4J.bind(pkg, (org.w3c.dom.Document) null, Docx4J.FLAG_BIND_BIND_XML);
			return pkg;
		} finally {
			Docx4jProperties.setProperty(IMPL_PROPERTY, "BindingTraverserXSLT");
		}
	}

	private static String control(String xml, String alias) {
		Matcher m = Pattern.compile("<w:sdt>(?:(?!<w:sdt>).)*?<w:alias w:val=\"" + alias + "\"/>.*?</w:sdt>", Pattern.DOTALL).matcher(xml);
		assertTrue("control " + alias + " in " + xml, m.find());
		return m.group();
	}

	private static String reference(String control) {
		Matcher m = Pattern.compile("<w:placeholder><w:docPart w:val=\"([^\"]*)\"/></w:placeholder>").matcher(control);
		assertTrue("a reference in " + control, m.find());
		return m.group(1);
	}

	private static void configuredRunGetsAnEntryOfItsOwn(String implementation) throws Exception {
		WordprocessingMLPackage pkg = bind(template(), implementation);
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		String one = control(xml, "One"), two = control(xml, "Two");
		String entry = reference(one);
		assertTrue(implementation + ": 32 upper-case hex digits: " + entry, entry.matches("[0-9A-F]{32}"));
		assertEquals(implementation + ": one entry for both controls", entry, reference(two));
		assertTrue(implementation + ": " + one, one.contains("<w:t>" + CONFIGURED_TEXT + "</w:t>") && one.contains("<w:showingPlcHdr/>"));

		GlossaryDocumentPart glossary = pkg.getMainDocumentPart().getGlossaryDocumentPart();
		List<CTDocPart> parts = glossary.getContents().getDocParts().getDocPart();
		assertEquals(implementation + ": one entry, not Word's default", 1, parts.size());
		assertEquals(implementation, entry, PlaceholderEntries.nameOf(parts.get(0)));
		String glossaryXml = XmlUtils.marshaltoString(glossary.getJaxbElement(), true, false);
		for (String expected : new String[] { "<w:name w:val=\"General\"/>", "<w:gallery w:val=\"placeholder\"/>",
				"<w:type w:val=\"bbPlcHdr\"/>", "<w:behavior w:val=\"content\"/>", "<w:guid w:val=\"{",
				"<w:docPartBody><w:p><w:r><w:rPr><w:rStyle w:val=\"PlaceholderText\"/></w:rPr><w:t>" + CONFIGURED_TEXT + "</w:t></w:r></w:p></w:docPartBody>" }) {
			assertTrue(implementation + ": " + expected + " in " + glossaryXml, glossaryXml.contains(expected));
		}
		assertTrue(implementation + ": the glossary's styles part", glossary.getStyleDefinitionsPart() != null);

		// bound again: the entry is reused
		WordprocessingMLPackage again = bind(reload(pkg), implementation);
		assertEquals(implementation + ": bound twice", 1,
				again.getMainDocumentPart().getGlossaryDocumentPart().getContents().getDocParts().getDocPart().size());
		assertEquals(implementation, entry, reference(control(
				XmlUtils.marshaltoString(again.getMainDocumentPart().getJaxbElement(), true, false), "One")));
	}

	@Test
	public void configuredRunGetsAnEntryOfItsOwnXSLT() throws Exception {
		configuredRunGetsAnEntryOfItsOwn("BindingTraverserXSLT");
	}

	@Test
	public void configuredRunGetsAnEntryOfItsOwnNonXSLT() throws Exception {
		configuredRunGetsAnEntryOfItsOwn("BindingTraverserNonXSLT");
	}

	@Test
	public void configuredRunGetsAnEntryOfItsOwnStAX() throws Exception {
		configuredRunGetsAnEntryOfItsOwn("BindingTraverserStAX");
	}
}
