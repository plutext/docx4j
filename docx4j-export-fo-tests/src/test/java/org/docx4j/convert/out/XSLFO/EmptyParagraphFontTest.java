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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
 * An empty paragraph is measured in the font its paragraph mark names, not in FOP's
 * default (CR-001 &#xa7;2.5).
 *
 * <p>The block's content is a single space standing for the paragraph mark, and the font
 * for it was asked for with that space as the sample.  {@code RunFontSelector} adds a
 * space to whatever span it is building without ever firing a font action - a space
 * belongs to the run it falls in - so for text that is <em>only</em> a space there is no
 * span and no font, and the block went out with no {@code font-family} at all.  FOP's
 * initial value for the property is {@code sans-serif}, which its base-14 collection
 * answers with <b>Helvetica</b>: that measured the line and was written into the PDF.
 *
 * <p>Measured on a corpus document whose first paragraph is empty: its line box is
 * <b>10.2pt short</b> of Word's and every line of page 1 carried the -10.2 (Word's
 * baselines 143.1 / 340.7 / 472.9 against 132.9 / 330.5 / 462.9).  <b>137 of the three
 * corpora's 449 renders referenced base-14 Helvetica</b>, 121 of them drawing nothing but
 * blanks in it, and every one of those 137 has such a block; 465 of 465 of them also
 * carried no {@code docx4j:line-box}, since the line-box pass has no font to take
 * metrics from either.
 *
 * @since 17.1.0
 */
public class EmptyParagraphFontTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final String DOCX4J = "http://docx4j.org/fop/word-layout";

	/** an empty paragraph in Arial 20pt, then one with content in the same font */
	private static final String BODY =
			"<w:document " + W + "><w:body>"
			+ "<w:p><w:pPr><w:rPr>"
			+ "<w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\"/><w:sz w:val=\"40\"/>"
			+ "</w:rPr></w:pPr></w:p>"
			+ "<w:p><w:r><w:rPr>"
			+ "<w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\"/><w:sz w:val=\"40\"/>"
			+ "</w:rPr><w:t>text</w:t></w:r></w:p>"
			+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/>"
			+ "</w:sectPr></w:body></w:document>";

	private org.w3c.dom.Document fo(int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(BODY));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	/** the first fo:block in the flow whose text is only whitespace */
	private static Element emptyBlock(org.w3c.dom.Document doc) {
		NodeList blocks = doc.getElementsByTagNameNS(FO, "block");
		for (int i = 0; i < blocks.getLength(); i++) {
			Element b = (Element) blocks.item(i);
			if (b.getElementsByTagNameNS(FO, "block").getLength() > 0) continue;
			String t = b.getTextContent();
			if (t != null && t.length() > 0 && t.trim().isEmpty()) return b;
		}
		return null;
	}

	private void check(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(flags);
		Element empty = emptyBlock(doc);
		org.junit.Assert.assertNotNull("no empty paragraph block", empty);

		String family = empty.getAttribute("font-family");
		assertFalse("an empty paragraph with no font-family is measured in FOP's"
				+ " base-14 Helvetica: " + XmlUtils.w3CDomNodeToString(empty),
				family == null || family.length() == 0);

		// and the line-box pass, which needs a font to read metrics from, ran
		assertTrue("no docx4j:line-box on the empty paragraph: " + XmlUtils.w3CDomNodeToString(empty),
				empty.getAttributeNS(DOCX4J, "line-box").length() > 0);

		// the font is the paragraph mark's own, which is the one the next paragraph's
		// text is set in
		NodeList inlines = doc.getElementsByTagNameNS(FO, "inline");
		String textFamily = null;
		for (int i = 0; i < inlines.getLength(); i++) {
			Element in = (Element) inlines.item(i);
			if (!"text".equals(in.getTextContent())) continue;
			textFamily = in.getAttribute("font-family");
		}
		org.junit.Assert.assertNotNull("test setup: no run to compare against", textFamily);
		assertEquals("the empty paragraph is set in the paragraph mark's own font",
				textFamily, family);
	}

	@Test
	public void anEmptyParagraphKeepsItsFontVisitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void anEmptyParagraphKeepsItsFontXslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
