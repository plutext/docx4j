/*
   Copyright 2026, Plutext Pty Ltd.

   This file is part of docx4j.

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
package org.docx4j.convert.out.html;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.P;
import org.docx4j.wml.Style;
import org.junit.Test;

/**
 * A numbered paragraph's label in HTML is written by docx4j, not drawn by the browser
 * (CR-003, the list-marker defect of 2026-09-18): a span at the head of the paragraph
 * carrying the level's text, number format and run formatting, with the paragraph's own
 * hanging indent kept, and the list around it structural only.  Both pathways.
 */
public class HtmlListLabelTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static WordprocessingMLPackage pkgWithNumbering(String lvl7, String lvl8) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		pkg.getMainDocumentPart().addTargetPart(ndp);
		ndp.setJaxbElement((Numbering) XmlUtils.unmarshalString(
				"<w:numbering " + W + ">"
				+ "<w:abstractNum w:abstractNumId=\"7\"><w:multiLevelType w:val=\"singleLevel\"/>" + lvl7 + "</w:abstractNum>"
				+ "<w:abstractNum w:abstractNumId=\"8\"><w:multiLevelType w:val=\"singleLevel\"/>" + lvl8 + "</w:abstractNum>"
				+ "<w:num w:numId=\"7\"><w:abstractNumId w:val=\"7\"/></w:num>"
				+ "<w:num w:numId=\"8\"><w:abstractNumId w:val=\"8\"/></w:num>"
				+ "</w:numbering>", Context.jc, Numbering.class));
		return pkg;
	}

	private static void addStyle(WordprocessingMLPackage pkg, String styleXml) throws Exception {
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add(
				(Style) XmlUtils.unmarshalString(styleXml, Context.jc, Style.class));
	}

	private static void addP(WordprocessingMLPackage pkg, String pXml) throws Exception {
		pkg.getMainDocumentPart().getContent().add((P) XmlUtils.unmarshalString(pXml, Context.jc, P.class));
	}

	private static String html(WordprocessingMLPackage pkg, int flag) throws Exception {
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, os, flag);
		return os.toString("UTF-8");
	}

	private static final int[] FLAGS = { Docx4J.FLAG_EXPORT_PREFER_XSL, Docx4J.FLAG_EXPORT_PREFER_NONXSL };

	/** A paragraph numbered by its STYLE gets a label (it got nothing), the label is the
	 *  level's own bullet, the item keeps the style's hanging indent and asks for no
	 *  browser marker, and the list around it is structural. */
	@Test
	public void styleNumberedBulletIsLabelledAndUnmarked() throws Exception {
		for (int flag : FLAGS) {
			WordprocessingMLPackage pkg = pkgWithNumbering(
					"<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"bullet\"/><w:lvlText w:val=\"-\"/>"
					+ "<w:lvlJc w:val=\"left\"/><w:pPr><w:ind w:left=\"567\" w:hanging=\"283\"/></w:pPr>"
					+ "<w:rPr><w:color w:val=\"1B365D\"/></w:rPr></w:lvl>",
					"<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/></w:lvl>");
			addStyle(pkg, "<w:style " + W + " w:type=\"paragraph\" w:styleId=\"ListBullet\"><w:name w:val=\"List Bullet\"/>"
					+ "<w:pPr><w:numPr><w:numId w:val=\"7\"/></w:numPr><w:ind w:left=\"567\" w:hanging=\"283\"/></w:pPr></w:style>");
			addP(pkg, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"ListBullet\"/></w:pPr><w:r><w:t>first item</w:t></w:r></w:p>");
			addP(pkg, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"ListBullet\"/></w:pPr><w:r><w:t>second item</w:t></w:r></w:p>");
			String html = html(pkg, flag);
			String impl = (flag == Docx4J.FLAG_EXPORT_PREFER_XSL ? "xslt: " : "visitor: ");
			assertTrue(impl + "no label on the style-numbered item: " + html, Pattern.compile(
					"<li[^>]*><span class=\"ListLabel\" style=\"[^\"]*min-width: 14\\.1[0-9]*pt;[^\"]*color: #1B365D;[^\"]*\">-</span>(<span[^>]*>)*first item")
					.matcher(html).find());
			// text-indent inherits into the inline-block: without a reset the paragraph's
			// negative indent is applied a second time inside the label (layout-only, so
			// asserted on the emitted style)
			assertTrue(impl + "the label must reset text-indent", Pattern.compile(
					"<span class=\"ListLabel\" style=\"display: inline-block;text-indent: 0;").matcher(html).find());
			assertTrue(impl + "the item still asks for the browser's marker", html.contains("list-style: none;"));
			assertFalse(impl + "display: list-item survives", html.contains("display: list-item"));
			assertTrue(impl + "the style's hanging indent must survive in the class rule",
					Pattern.compile("\\.ListBullet \\{[^}]*margin-left: 28\\.3[0-9]*pt;[^}]*text-indent: -14\\.1[0-9]*pt;").matcher(html).find());
			assertTrue(impl + "no structural ul around the items: " + html, Pattern.compile(
					"<ul style=\"list-style: none; margin: 0; padding-left: 0;\">\\s*<li").matcher(html).find());
		}
	}

	/** A label level - numFmt none, lvlText NOTE - and an upperLetter level with text
	 *  ("Appendix %1") reach the output as written; an ordered level counts. */
	@Test
	public void labelTextAndNumberFormatReachTheOutput() throws Exception {
		for (int flag : FLAGS) {
			WordprocessingMLPackage pkg = pkgWithNumbering(
					"<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"none\"/><w:suff w:val=\"tab\"/><w:lvlText w:val=\"NOTE\"/>"
					+ "<w:lvlJc w:val=\"left\"/><w:pPr><w:ind w:left=\"1134\" w:hanging=\"1134\"/></w:pPr></w:lvl>",
					"<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"upperLetter\"/><w:suff w:val=\"space\"/><w:lvlText w:val=\"Appendix %1\"/>"
					+ "<w:lvlJc w:val=\"left\"/><w:pPr><w:ind w:left=\"0\" w:hanging=\"0\"/></w:pPr></w:lvl>");
			addP(pkg, "<w:p " + W + "><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"7\"/></w:numPr></w:pPr><w:r><w:t>a note</w:t></w:r></w:p>");
			addP(pkg, "<w:p " + W + "><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"8\"/></w:numPr></w:pPr><w:r><w:t>Tag Parameter Reference</w:t></w:r></w:p>");
			addP(pkg, "<w:p " + W + "><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"8\"/></w:numPr></w:pPr><w:r><w:t>Implementation Notes</w:t></w:r></w:p>");
			String html = html(pkg, flag);
			String impl = (flag == Docx4J.FLAG_EXPORT_PREFER_XSL ? "xslt: " : "visitor: ");
			assertTrue(impl + "NOTE label lost: " + html, Pattern.compile(
					"<span class=\"ListLabel\"[^>]*>NOTE</span>(<span[^>]*>)*a note").matcher(html).find());
			assertTrue(impl + "Appendix A lost", Pattern.compile(
					"<span class=\"ListLabel\"[^>]*>Appendix A </span>(<span[^>]*>)*Tag Parameter").matcher(html).find());
			assertTrue(impl + "Appendix B lost", Pattern.compile(
					"<span class=\"ListLabel\"[^>]*>Appendix B </span>(<span[^>]*>)*Implementation").matcher(html).find());
		}
	}

	/** A paragraph numbered directly keeps its number, once: the number is no longer a
	 *  bare text node ahead of the content, and the counter is not advanced twice. */
	@Test
	public void directlyNumberedParagraphCountsOnce() throws Exception {
		for (int flag : FLAGS) {
			WordprocessingMLPackage pkg = pkgWithNumbering(
					"<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>"
					+ "<w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr></w:lvl>",
					"<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"bullet\"/><w:lvlText w:val=\"o\"/><w:lvlJc w:val=\"left\"/></w:lvl>");
			for (String t : new String[] { "one", "two", "three" }) {
				addP(pkg, "<w:p " + W + "><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"7\"/></w:numPr></w:pPr><w:r><w:t>" + t + "</w:t></w:r></w:p>");
			}
			String html = html(pkg, flag);
			String impl = (flag == Docx4J.FLAG_EXPORT_PREFER_XSL ? "xslt: " : "visitor: ");
			assertTrue(impl + "3. lost: " + html, Pattern.compile("<span class=\"ListLabel\"[^>]*>3\\.</span>(<span[^>]*>)*three").matcher(html).find());
			assertFalse(impl + "counter advanced twice", html.contains(">5.</span>") || html.contains(">6.</span>"));
			assertFalse(impl + "a bare number text node survives", Pattern.compile(">1\\. <span").matcher(html).find());
		}
	}
}
