/*
 * Copyright 2026, Plutext Pty Ltd.
 *
 * This file is part of docx4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A block takes the font and size of the run holding most of its text
 * (XsltFOFunctions.applyBlockLineHeight); where two runs hold the same number of
 * characters, the first in document order wins, not the one whose family name hashes
 * first.  Found 2026-09-30 when the +noliga twin went (CR-020): a paragraph of one
 * 11pt space followed by one 14pt space took the 11pt line with the twin and the 14pt
 * line without it, and Word takes the 11pt one (real corpus 3115, EMPLOYMENT HISTORY).
 *
 * @since 17.3.0
 */
public class DominantRunTieTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static String run(String font, int halfPoints, String text) {
		return "<w:r><w:rPr><w:rFonts w:ascii=\"" + font + "\" w:hAnsi=\"" + font + "\"/><w:sz w:val=\"" + halfPoints
				+ "\"/></w:rPr><w:t xml:space=\"preserve\">" + text + "</w:t></w:r>";
	}

	private static Element block(String runs, String marker) throws Exception {
		String xml = "<w:document " + W + "><w:body><w:p>" + runs + "</w:p>"
				+ "<w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"720\" w:footer=\"720\" w:gutter=\"0\"/>"
				+ "</w:sectPr></w:body></w:document>";
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(xml));
		FOSettings settings = new FOSettings(pkg);
		settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(settings, baos, Docx4J.FLAG_NONE);
		org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
		NodeList nl = doc.getElementsByTagNameNS(FO, "block");
		for (int i = 0; i < nl.getLength(); i++) {
			Element b = (Element) nl.item(i);
			String t = b.getTextContent();
			if (marker != null ? t.contains(marker) : (t.length() > 0 && t.trim().length() == 0)) return b;
		}
		StringBuilder sb = new StringBuilder("no block holding " + marker + " among: ");
		for (int i = 0; i < nl.getLength(); i++) sb.append('[').append(nl.item(i).getTextContent()).append("] ");
		assertNotNull(sb.toString(), null);
		return null;
	}

	@Test
	public void theFirstOfTwoEqualRunsSizesTheBlock() throws Exception {
		assertEquals("11.0pt", block(run("Arial", 22, " ") + run("Cambria", 28, " "), null).getAttribute("font-size"));
		assertEquals("14.0pt", block(run("Cambria", 28, " ") + run("Arial", 22, " "), null).getAttribute("font-size"));
	}

	@Test
	public void moreTextStillWins() throws Exception {
		assertEquals("14.0pt", block(run("Arial", 22, " ") + run("Cambria", 28, "two"), "two").getAttribute("font-size"));
	}
}
