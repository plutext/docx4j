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
package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Assume;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A run in Wingdings whose characters need both substitute faces (Noto Sans Symbols 2
 * for most of the range, Noto Sans Symbols for the rest; PhysicalFonts.getWDingsFont
 * and getWDingsFont2): one fo:inline per face, every character rendered.  Until 17.1.1
 * the second inline was appended to the selector's scratch Document beside the first,
 * which a Document does not allow (HIERARCHY_REQUEST_ERR), so such a run failed
 * (CR-016 phase 4).
 */
public class SymbolRunSubstituteFacesTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	@Test
	public void aRunNeedingBothSubstituteFaces() throws Exception {
		Assume.assumeTrue("the two Wingdings substitutes are not on the classpath",
				PhysicalFonts.getWDingsFont()!=null && PhysicalFonts.getWDingsFont2()!=null
				&& PhysicalFonts.getWDingsFont()!=PhysicalFonts.getWDingsFont2());
		// Wingdings 'a'..'z' (0x61-0x7A): the comment on getWDingsFont2 has 85-105 in the
		// second face and the rest in the first
		StringBuilder text = new StringBuilder();
		for (char c = 'a'; c <= 'z'; c++) text.append(c);

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p><w:r><w:rPr><w:rFonts w:ascii=\"Wingdings\" w:hAnsi=\"Wingdings\"/></w:rPr>"
				+ "<w:t>" + text + "</w:t></w:r></w:p></w:body></w:document>"));

		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, Docx4J.FLAG_NONE);
		org.w3c.dom.Document fo = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));

		Set<String> families = new LinkedHashSet<String>();
		StringBuilder rendered = new StringBuilder();
		NodeList nl = fo.getElementsByTagNameNS(FO, "inline");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (el.getAttribute("font-family").length()==0 || el.getTextContent().length()==0) continue;
			families.add(el.getAttribute("font-family"));
			rendered.append(el.getTextContent());
		}
		assertEquals("one replacement character per character", text.length(), rendered.codePointCount(0, rendered.length()));
		assertTrue("both substitute faces expected, got " + families, families.size() >= 2);
	}
}
