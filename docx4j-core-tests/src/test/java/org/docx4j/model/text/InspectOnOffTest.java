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
package org.docx4j.model.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.docx4j.XmlUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * The inspector reads the XML itself, not through JAXB; its on/off reading is Word's (CR-060 section 1, rule 1):
 * off for exactly false, off or 0, case-sensitive and untrimmed.
 */
public class InspectOnOffTest {

	private static Element b(String attributes) throws Exception {
		String xml = "<w:b xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" " + attributes + "/>";
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes("UTF-8"))).getDocumentElement();
	}

	@Ignore("CR-060 phase 1")
	@Test
	public void onOffAsWordReadsIt() throws Exception {
		assertNull(InspectXml.on(null));
		assertEquals(Boolean.TRUE, InspectXml.on(b("")));
		String[] off = { "false", "off", "0" };
		for (String v : off) assertEquals(v, Boolean.FALSE, InspectXml.on(b("w:val=\"" + v + "\"")));
		String[] on = { "true", "on", "1", "", " ", "FALSE", "Off", "OFF", " false", "false ", "00", "no" };
		for (String v : on) assertEquals("\"" + v + "\"", Boolean.TRUE, InspectXml.on(b("w:val=\"" + v + "\"")));
	}
}
