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
package org.docx4j.convert.out.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Body;
import org.junit.Test;

/**
 * HTML output: a paragraph's own top and bottom borders are drawn (they were lost, left to a
 * border container which by default drew nothing), and adjacent paragraphs with the same
 * borders are one box, as Word draws them: the first keeps the top border, the last the
 * bottom.  And a simple field whose run has a property with no CSS name (w:lang) is drawn,
 * and leaves nothing behind for the next element styled (17.3.3).
 */
public class HtmlParagraphBordersTest {

	static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	static final String BOX = "<w:pBdr>"
			+ "<w:top w:val=\"single\" w:sz=\"4\" w:space=\"1\" w:color=\"0000FF\"/>"
			+ "<w:left w:val=\"single\" w:sz=\"4\" w:space=\"4\" w:color=\"0000FF\"/>"
			+ "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"1\" w:color=\"0000FF\"/>"
			+ "<w:right w:val=\"single\" w:sz=\"4\" w:space=\"4\" w:color=\"0000FF\"/></w:pBdr>";

	private static WordprocessingMLPackage doc(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getJaxbElement().setBody(
				(Body) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:body " + W + ">" + body + "</w:body>")));
		return pkg;
	}

	private static String p(String pPr, String text) {
		return "<w:p><w:pPr>" + pPr + "</w:pPr><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	static String html(WordprocessingMLPackage pkg, int flags) throws Exception {
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, os, flags);
		return os.toString("UTF-8");
	}

	/** The start tag of the block element (p or li) holding a text */
	private static String block(String html, String text) {
		int at = html.indexOf(">" + text + "<");
		assertTrue(text, at > 0);
		return html.substring(Math.max(html.lastIndexOf("<p ", at), html.lastIndexOf("<li ", at)), at);
	}

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String pathway(int flags) {
		return flags == Docx4J.FLAG_NONE ? "visitor: " : "XSLT: ";
	}

	@Test
	public void bordersAreDrawnAndAGroupIsOneBox() throws Exception {
		String body = p("", "BEFORE")
				+ p("<w:pBdr><w:top w:val=\"single\" w:sz=\"8\" w:space=\"1\" w:color=\"FF0000\"/></w:pBdr>", "SINGLE")
				+ p("", "BETWEEN")
				+ p(BOX, "BOXONE") + p(BOX, "BOXTWO") + p(BOX, "BOXTHREE")
				+ p("", "AFTER")
				+ p("<w:pBdr><w:top w:val=\"single\" w:sz=\"8\" w:space=\"1\" w:color=\"00FF00\"/></w:pBdr>"
						+ "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"FFFF00\"/>", "SHADEDBORDERED")
				+ p("", "END");
		for (int flags : FLAGS) {
			String html = html(doc(body), flags);
			String impl = pathway(flags);

			String single = block(html, "SINGLE");
			assertTrue(impl + single, single.contains("border-top-style: solid;"));
			assertTrue(impl + single, single.contains("border-top-color: #FF0000;"));

			String one = block(html, "BOXONE"), two = block(html, "BOXTWO"), three = block(html, "BOXTHREE");
			assertTrue(impl + one, one.contains("border-top-style: solid;"));
			assertFalse(impl + one, one.contains("border-bottom-"));
			assertFalse(impl + two, two.contains("border-top-") || two.contains("border-bottom-"));
			assertFalse(impl + three, three.contains("border-top-"));
			assertTrue(impl + three, three.contains("border-bottom-style: solid;"));
			for (String b : new String[] { one, two, three }) {
				assertTrue(impl + b, b.contains("border-left-style: solid;") && b.contains("border-right-style: solid;"));
			}

			// the shaded paragraph's own top border wins over the 1px fill-coloured border
			String shaded = block(html, "SHADEDBORDERED");
			assertTrue(impl + shaded, shaded.indexOf("border-top-color: #00FF00;") > shaded.indexOf("border-color: "));

			assertFalse(impl + "BEFORE", block(html, "BEFORE").contains("border-"));
		}
	}

	@Test
	public void aFieldWithALanguageIsDrawnAndLeavesNothingBehind() throws Exception {
		String body = "<w:p><w:r><w:t xml:space=\"preserve\">PAGE </w:t></w:r>"
				+ "<w:fldSimple w:instr=\" PAGE \"><w:r><w:rPr><w:b/><w:lang w:val=\"en-AU\"/></w:rPr>"
				+ "<w:t>7</w:t></w:r></w:fldSimple></w:p>"
				+ "<w:tbl><w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/></w:tblPr><w:tblGrid><w:gridCol w:w=\"4000\"/></w:tblGrid>"
				+ "<w:tr><w:tc><w:tcPr><w:tcW w:w=\"4000\" w:type=\"dxa\"/></w:tcPr><w:p><w:r><w:t>CELL</w:t></w:r></w:p></w:tc></w:tr></w:tbl>"
				+ p("", "END");
		for (int flags : FLAGS) {
			String html = html(doc(body), flags);
			String impl = pathway(flags);
			int field = html.indexOf("PAGE ");
			assertTrue(impl + "the field's result", field > 0 && html.indexOf("font-weight: bold;", field) > 0);
			int table = html.indexOf("<table");
			assertTrue(impl, table > 0);
			String tableTag = html.substring(table, html.indexOf('>', table));
			assertFalse(impl + tableTag, tableTag.contains("font-weight"));
			assertFalse(impl + "a null declaration", html.contains("null;") || html.contains(";null"));
		}
	}

	@Test
	public void withoutSides() {
		String s = "border-top-style: solid;border-top-width: 1px;padding-top: 1pt;"
				+ "border-left-style: solid;border-bottom-color: #000000;padding-bottom: 1pt;margin-left: 2pt;";
		assertEquals("border-left-style: solid;border-bottom-color: #000000;padding-bottom: 1pt;margin-left: 2pt;",
				TagSingleBox.withoutSides(s, true, false));
		assertEquals("border-top-style: solid;border-top-width: 1px;padding-top: 1pt;border-left-style: solid;margin-left: 2pt;",
				TagSingleBox.withoutSides(s, false, true));
	}
}
