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
package org.docx4j.convert.out.html;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.Style;
import org.junit.Test;

/**
 * A toggle property set by both a run's character style and its paragraph style is off (ECMA-376-1 17.7.3), as
 * Word, PropertyResolver and the PDF output have it; the HTML output writes it off on the run, since each style's
 * class says on and a browser would show it on (measured in headless Chrome, 2026-10-24: a bold character style in
 * a bold paragraph style rendered bold until 17.3.5). Both HTML writers, XSL and NonXSL, for bold, italic, caps,
 * small caps and strike; a run bold by one style only, or by its own w:b, stays bold.
 */
public class HtmlToggleOffTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String[][] TOGGLES = {
		{ "b", "font-weight: normal" }, { "i", "font-style: normal" }, { "caps", "text-transform: none" },
		{ "smallCaps", "font-variant: normal" }, { "strike", "text-decoration-line: none" } };

	private static WordprocessingMLPackage document() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		StyleDefinitionsPart sdp = pkg.getMainDocumentPart().getStyleDefinitionsPart();
		String r = "<w:r><w:rPr>%s</w:rPr><w:t xml:space=\"preserve\">%s</w:t></w:r>";
		for (String[] t : TOGGLES) {
			String x = t[0];
			sdp.getJaxbElement().getStyle().add((Style) XmlUtils.unmarshalString("<w:style " + W + " w:type=\"character\" w:customStyle=\"1\" w:styleId=\"Char_" + x + "\"><w:name w:val=\"Char " + x + "\"/><w:rPr><w:" + x + "/></w:rPr></w:style>"));
			sdp.getJaxbElement().getStyle().add((Style) XmlUtils.unmarshalString("<w:style " + W + " w:type=\"paragraph\" w:customStyle=\"1\" w:styleId=\"Para_" + x + "\"><w:name w:val=\"Para " + x + "\"/><w:basedOn w:val=\"Normal\"/><w:rPr><w:" + x + "/></w:rPr></w:style>"));
			pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString("<w:p " + W + "><w:pPr><w:pStyle w:val=\"Para_" + x + "\"/></w:pPr>"
					+ String.format(r, "<w:rStyle w:val=\"Char_" + x + "\"/>", "T_" + x + "_BOTH") + String.format(r, "<w:" + x + "/>", "T_" + x + "_DIRECT") + "</w:p>"));
			pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString("<w:p " + W + ">"
					+ String.format(r, "<w:rStyle w:val=\"Char_" + x + "\"/>", "T_" + x + "_CHAR") + "</w:p>"));
		}
		return pkg;
	}

	private static String html(int flag) throws Exception {
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(document());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, out, flag);
		return out.toString("UTF-8");
	}

	/** The style attribute of the span holding the token. */
	private static String spanStyle(String html, String token) {
		Matcher m = Pattern.compile("<span[^>]*?style=\"([^\"]*)\"[^>]*>" + token + "<").matcher(html);
		assertTrue(token + " in " + html, m.find());
		return m.group(1);
	}

	private static void togglesWrittenOff(String writer, String html) {
		for (String[] t : TOGGLES) {
			String both = spanStyle(html, "T_" + t[0] + "_BOTH");
			assertTrue(writer + " " + t[0] + ", both styles: " + both, both.contains(t[1]));
			String one = spanStyle(html, "T_" + t[0] + "_CHAR");
			assertFalse(writer + " " + t[0] + ", the character style alone: " + one, one.contains(t[1]));
			String direct = spanStyle(html, "T_" + t[0] + "_DIRECT");
			assertFalse(writer + " " + t[0] + ", direct formatting in the styled paragraph: " + direct, direct.contains(t[1]));
		}
	}

	@Test
	public void theXslWriterWritesAToggledPropertyOff() throws Exception {
		togglesWrittenOff("XSL", html(Docx4J.FLAG_EXPORT_PREFER_XSL));
	}

	@Test
	public void theNonXslWriterWritesAToggledPropertyOff() throws Exception {
		togglesWrittenOff("NonXSL", html(Docx4J.FLAG_EXPORT_PREFER_NONXSL));
	}
}
