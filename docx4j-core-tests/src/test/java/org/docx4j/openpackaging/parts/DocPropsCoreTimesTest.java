/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.openpackaging.parts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Properties;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.docProps.core.CoreProperties;
import org.docx4j.docProps.core.dc.elements.SimpleLiteral;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * docx4j.dcterms.write (CR-036): false, the default, leaves dcterms:created and dcterms:modified as
 * they are; true sets dcterms:modified to the current time on every save, and dcterms:created too
 * on a new package that has none, as Office writes them (a dcterms:W3CDTF literal, UTC, to the
 * second).
 */
public class DocPropsCoreTimesTest {

	private Object before;

	@Before
	public void keep() {
		before = Docx4jProperties.getProperties().get("docx4j.dcterms.write");
	}

	@After
	public void restore() {
		Properties p = Docx4jProperties.getProperties();
		if (before == null) p.remove("docx4j.dcterms.write");
		else p.put("docx4j.dcterms.write", before);
	}

	@Test
	public void leftAsTheyAreByDefault() throws Exception {
		Docx4jProperties.getProperties().remove("docx4j.dcterms.write");
		CoreProperties cp = core(saved(WordprocessingMLPackage.createPackage()));
		assertNull(cp.getCreated());
		assertNull(cp.getModified());
	}

	@Test
	public void theCurrentTimeWhereAsked() throws Exception {
		Docx4jProperties.getProperties().setProperty("docx4j.dcterms.write", "true");
		long from = Instant.now().getEpochSecond();
		WordprocessingMLPackage once = saved(WordprocessingMLPackage.createPackage());
		CoreProperties cp = core(once);
		assertNotNull(cp.getCreated());
		long created = seconds(cp.getCreated()), modified = seconds(cp.getModified());
		assertTrue(created >= from && created <= Instant.now().getEpochSecond());
		assertEquals(created, modified);
		// as Office writes it
		String xml = XmlUtils.marshaltoString(cp, true, false, org.docx4j.jaxb.Context.jcDocPropsCore);
		assertTrue(xml, xml.matches("(?s).*<dcterms:created xsi:type=\"dcterms:W3CDTF\"[^>]*>\\d{4}-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\dZ</dcterms:created>.*"));

		// a loaded package: created kept, modified the time of this save
		cp.setCreated(literal("2001-02-03T04:05:06Z"));
		cp.setModified(literal("2001-02-03T04:05:06Z"));
		CoreProperties again = core(saved(once));
		assertEquals("2001-02-03T04:05:06Z", again.getCreated().getContent().get(0));
		assertTrue(seconds(again.getModified()) >= from);
	}

	private static SimpleLiteral literal(String s) {
		SimpleLiteral l = new org.docx4j.docProps.core.dc.terms.W3CDTF();
		l.getContent().add(s);
		return l;
	}

	private static long seconds(SimpleLiteral l) {
		return Instant.parse(l.getContent().get(0)).getEpochSecond();
	}

	private static CoreProperties core(WordprocessingMLPackage pkg) throws Exception {
		return pkg.getDocPropsCorePart().getContents();
	}

	private static WordprocessingMLPackage saved(WordprocessingMLPackage pkg) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		pkg.save(os);
		return WordprocessingMLPackage.load(new ByteArrayInputStream(os.toByteArray()));
	}
}
