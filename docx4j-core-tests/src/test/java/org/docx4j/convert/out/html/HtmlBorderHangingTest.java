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
import org.docx4j.wml.P;
import org.docx4j.wml.Style;
import org.junit.Test;

/**
 * A left paragraph border against a hanging indent (CR-003, 2026-09-18): Word stands the
 * bar left of the first line, min(left, left - hanging) less w:space, and every line
 * clears it.  In CSS the hang moves from the margin into the padding, in the class rule
 * and in an inline style alike.
 */
public class HtmlBorderHangingTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** left 1134 hanging 1134 (56.7pt), a single 18-eighths border 8pt off the text */
	private static final String REQ_PPR = "<w:pPr><w:pBdr><w:left w:val=\"single\" w:sz=\"18\" w:space=\"8\" w:color=\"2B6CB0\"/></w:pBdr>"
			+ "<w:ind w:left=\"1134\" w:hanging=\"1134\"/></w:pPr>";

	private static String html(WordprocessingMLPackage pkg, int flag) throws Exception {
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, os, flag);
		return os.toString("UTF-8");
	}

	private static final int[] FLAGS = { Docx4J.FLAG_EXPORT_PREFER_XSL, Docx4J.FLAG_EXPORT_PREFER_NONXSL };

	private static void addStyle(WordprocessingMLPackage pkg, String styleXml) throws Exception {
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add(
				(Style) XmlUtils.unmarshalString(styleXml, Context.jc, Style.class));
	}

	private static void addP(WordprocessingMLPackage pkg, String pXml) throws Exception {
		pkg.getMainDocumentPart().getContent().add((P) XmlUtils.unmarshalString(pXml, Context.jc, P.class));
	}

	/** The style's class rule: margin-left 0 (left - hanging), padding-left 64.7pt (space +
	 *  hanging), text-indent -56.7pt; the border itself as before. */
	@Test
	public void styleRuleMovesTheHangIntoThePadding() throws Exception {
		for (int flag : FLAGS) {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			addStyle(pkg, "<w:style " + W + " w:type=\"paragraph\" w:styleId=\"Requirement\"><w:name w:val=\"Requirement\"/>" + REQ_PPR + "</w:style>");
			addP(pkg, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Requirement\"/></w:pPr><w:r><w:t>REQ-001 A processor MUST.</w:t></w:r></w:p>");
			String html = html(pkg, flag);
			String impl = (flag == Docx4J.FLAG_EXPORT_PREFER_XSL ? "xslt: " : "visitor: ");
			String rule = rule(html, "Requirement");
			assertTrue(impl + "margin-left must be left - hanging: " + rule, rule.contains("margin-left: 0"));
			assertTrue(impl + "text-indent must stay -hanging: " + rule, Pattern.compile("text-indent: -56\\.7[0-9]*pt").matcher(rule).find());
			assertTrue(impl + "padding-left must be space + hanging, after the border's own: " + rule,
					Pattern.compile("padding-left: 8pt;.*padding-left: 64\\.7[0-9]*pt").matcher(rule).find());
			assertTrue(impl + "the border itself is kept: " + rule, rule.contains("border-left-style: solid"));
		}
	}

	/** The same indent given directly on a paragraph under a bordered style: the inline
	 *  style is shifted too (an unshifted inline margin-left would undo the class rule's). */
	@Test
	public void inlineIndentUnderABorderedStyleIsShiftedToo() throws Exception {
		for (int flag : FLAGS) {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			addStyle(pkg, "<w:style " + W + " w:type=\"paragraph\" w:styleId=\"Requirement\"><w:name w:val=\"Requirement\"/>"
					+ "<w:pPr><w:pBdr><w:left w:val=\"single\" w:sz=\"18\" w:space=\"8\" w:color=\"2B6CB0\"/></w:pBdr></w:pPr></w:style>");
			addP(pkg, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Requirement\"/><w:ind w:left=\"1134\" w:hanging=\"567\"/></w:pPr><w:r><w:t>direct indent</w:t></w:r></w:p>");
			String html = html(pkg, flag);
			String impl = (flag == Docx4J.FLAG_EXPORT_PREFER_XSL ? "xslt: " : "visitor: ");
			assertTrue(impl + "inline style not shifted: " + html, Pattern.compile(
					"<p class=\"Requirement[^\"]*\" style=\"[^\"]*margin-left: 28\\.3[0-9]*pt;[^\"]*text-indent: -28\\.3[0-9]*pt;[^\"]*padding-left: 36\\.3[0-9]*pt;")
					.matcher(html).find());
		}
	}

	/** Without a left border, or with a positive firstLine, nothing changes. */
	@Test
	public void noBorderOrFirstLineIsUnchanged() throws Exception {
		for (int flag : FLAGS) {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			addStyle(pkg, "<w:style " + W + " w:type=\"paragraph\" w:styleId=\"Hang\"><w:name w:val=\"Hang\"/>"
					+ "<w:pPr><w:ind w:left=\"1134\" w:hanging=\"1134\"/></w:pPr></w:style>");
			addStyle(pkg, "<w:style " + W + " w:type=\"paragraph\" w:styleId=\"First\"><w:name w:val=\"First\"/>"
					+ "<w:pPr><w:pBdr><w:left w:val=\"single\" w:sz=\"6\" w:space=\"4\"/></w:pBdr><w:ind w:left=\"720\" w:firstLine=\"360\"/></w:pPr></w:style>");
			addP(pkg, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"Hang\"/></w:pPr><w:r><w:t>hang only</w:t></w:r></w:p>");
			addP(pkg, "<w:p " + W + "><w:pPr><w:pStyle w:val=\"First\"/></w:pPr><w:r><w:t>first line</w:t></w:r></w:p>");
			String html = html(pkg, flag);
			String impl = (flag == Docx4J.FLAG_EXPORT_PREFER_XSL ? "xslt: " : "visitor: ");
			String hang = rule(html, "Hang");
			assertTrue(impl + "a hang with no border keeps its margin: " + hang, Pattern.compile("margin-left: 56\\.7[0-9]*pt").matcher(hang).find());
			assertFalse(impl + "a hang with no border gains padding: " + hang, hang.contains("padding-left"));
			String first = rule(html, "First");
			assertTrue(impl + "a firstLine keeps its margin: " + first, Pattern.compile("margin-left: 36pt").matcher(first).find());
			assertTrue(impl + "a firstLine keeps the border's own padding: " + first, first.contains("padding-left: 4pt;") && !first.contains("padding-left: 22"));
		}
	}

	private static String rule(String html, String styleId) {
		int i = html.indexOf("." + styleId + " {");
		assertTrue("no class rule for " + styleId, i >= 0);
		return html.substring(i, html.indexOf('}', i));
	}
}
