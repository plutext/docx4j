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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.P;
import org.docx4j.wml.Style;
import org.junit.Test;

/**
 * A raised or lowered run is a {@code <sup>} or {@code <sub>} in {@code Docx4J.toHTML} (CR-069 phase 3, D4), as the
 * text model's flag and Markdown's tags have it (WD REQ-710): its {@code w:vertAlign}, its own else its styles', with
 * its class and CSS kept, so a browser draws it as it drew the span; a note's reference or mark stays a span. Both HTML
 * writers, XSL and NonXSL, the same elements.
 */
public class HtmlVerticalAlignTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String SUP = "<w:vertAlign w:val=\"superscript\"/>";
	private static final String SUB = "<w:vertAlign w:val=\"subscript\"/>";

	private static String r(String rPr, String text) {
		return "<w:r>" + (rPr.isEmpty() ? "" : "<w:rPr>" + rPr + "</w:rPr>") + "<w:t xml:space=\"preserve\">" + text + "</w:t></w:r>";
	}

	private static WordprocessingMLPackage document() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		StyleDefinitionsPart sdp = pkg.getMainDocumentPart().getStyleDefinitionsPart();
		sdp.getJaxbElement().getStyle().add((Style) XmlUtils.unmarshalString("<w:style " + W + " w:type=\"character\" w:customStyle=\"1\" w:styleId=\"Raised\"><w:name w:val=\"Raised\"/><w:rPr>" + SUP + "</w:rPr></w:style>"));
		sdp.getJaxbElement().getStyle().add((Style) XmlUtils.unmarshalString("<w:style " + W + " w:type=\"character\" w:styleId=\"NoteMark\"><w:name w:val=\"Note Mark\"/><w:rPr>" + SUP + "</w:rPr></w:style>"));
		add(pkg, r("", "H") + r(SUB, "2") + r("", "O and x") + r(SUP, "2") + r("", " T_DIRECT"));
		add(pkg, r("", "Revenue 48,210") + r("<w:rStyle w:val=\"Raised\"/>", "3") + r("", " T_STYLE"));
		add(pkg, r("", "x") + r("<w:rStyle w:val=\"Raised\"/><w:vertAlign w:val=\"baseline\"/>", "T_BASELINE"));
		add(pkg, r(SUP, "a") + r(SUP, "b") + r("", " T_FIRST"));
		add(pkg, r("", "Body T_NOTE") + "<w:r><w:rPr><w:rStyle w:val=\"NoteMark\"/>" + SUP + "</w:rPr><w:footnoteReference w:id=\"1\"/></w:r>");
		FootnotesPart footnotes = new FootnotesPart();
		pkg.getMainDocumentPart().addTargetPart(footnotes);
		footnotes.setJaxbElement((org.docx4j.wml.CTFootnotes) XmlUtils.unmarshalString("<w:footnotes " + W + ">"
				+ "<w:footnote w:type=\"separator\" w:id=\"-1\"><w:p><w:r><w:separator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:type=\"continuationSeparator\" w:id=\"0\"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:id=\"1\"><w:p><w:r><w:rPr><w:rStyle w:val=\"NoteMark\"/></w:rPr><w:footnoteRef/></w:r>" + r("", " T_NOTE_TEXT") + "</w:p></w:footnote>"
				+ "</w:footnotes>", Context.jc, org.docx4j.wml.CTFootnotes.class));
		return pkg;
	}

	private static void add(WordprocessingMLPackage pkg, String content) throws Exception {
		pkg.getMainDocumentPart().getContent().add((P) XmlUtils.unmarshalString("<w:p " + W + ">" + content + "</w:p>", Context.jc, P.class));
	}

	private static String html(int flag) throws Exception {
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(document());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, out, flag);
		return out.toString("UTF-8");
	}

	/** Each sup and sub element, its attributes and its text. */
	private static List<String> elements(String html) {
		List<String> found = new ArrayList<String>();
		Matcher m = Pattern.compile("<(su[pb])( [^>]*)?>(.*?)</\\1>").matcher(html);
		while (m.find()) found.add(m.group());
		return found;
	}

	@Test
	public void raisedAndLoweredRunsAreElements() throws Exception {
		for (int flag : new int[] { Docx4J.FLAG_EXPORT_PREFER_XSL, Docx4J.FLAG_EXPORT_PREFER_NONXSL }) {
			String html = html(flag);
			// the run's own w:vertAlign: its CSS kept on the element
			assertTrue(html, Pattern.compile("H</span><sub [^>]*style=\"vertical-align: sub;font-size: smaller;[^\"]*\">2</sub>").matcher(html).find());
			assertTrue(html, Pattern.compile("x</span><sup [^>]*style=\"vertical-align: super;font-size: smaller;[^\"]*\">2</sup>").matcher(html).find());
			// a character style's: its class, which carries the CSS
			assertTrue(html, Pattern.compile("48,210</span><sup class=\"[^\"]*Raised[^\"]*\"[^>]*>3</sup>").matcher(html).find());
			// the run's own baseline over its style's superscript
			assertTrue(html, Pattern.compile("<span [^>]*>T_BASELINE</span>").matcher(html).find());
			// two raised runs alike are one element; a paragraph may start with one
			assertTrue(html, Pattern.compile("<p [^>]*><sup [^>]*>ab</sup>").matcher(html).find());
		}
	}

	/** A note's reference and its mark are references (REQ-710): raised by their style and their own w:vertAlign, spans. */
	@Test
	public void aNoteReferenceStaysASpan() throws Exception {
		for (int flag : new int[] { Docx4J.FLAG_EXPORT_PREFER_XSL, Docx4J.FLAG_EXPORT_PREFER_NONXSL }) {
			String html = html(flag);
			assertTrue(html, html.contains("href=\"#fn1\""));
			assertTrue(html, html.contains("href=\"#fs1\""));
			for (String e : elements(html)) assertFalse(e, e.contains("href"));
			assertEquals(html, 4, elements(html).size());
		}
	}

	@Test
	public void bothWritersWriteTheSameElements() throws Exception {
		assertEquals(elements(html(Docx4J.FLAG_EXPORT_PREFER_XSL)), elements(html(Docx4J.FLAG_EXPORT_PREFER_NONXSL)));
	}
}
