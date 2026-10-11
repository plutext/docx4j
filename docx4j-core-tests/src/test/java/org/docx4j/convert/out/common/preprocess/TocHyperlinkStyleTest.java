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
package org.docx4j.convert.out.common.preprocess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Body;
import org.docx4j.wml.R;
import org.docx4j.wml.Style;
import org.junit.Test;

/**
 * A hyperlinked TOC's entries are drawn without the Hyperlink character style, as Word draws
 * them; a hyperlink elsewhere, and a TOC without {@code \h}, keep it (TocHyperlinkStyle).  The
 * Hyperlink style here has a translated id ({@code Hipervnculo}), as in a Spanish document:
 * it is found by its name.  The FO side is export-fo-tests' TocHyperlinkStyleFoTest.
 */
public class TocHyperlinkStyleTest {

	static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"";

	static String link(String text) {
		return "<w:hyperlink w:anchor=\"_Toc1\"><w:r><w:rPr><w:rStyle w:val=\"Hipervnculo\"/></w:rPr>"
				+ "<w:t>" + text + "</w:t></w:r></w:hyperlink>";
	}

	/** Each entry's text names what it tests */
	static WordprocessingMLPackage doc() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getContents().getStyle().add((Style) XmlUtils.unmarshalString(
				"<w:style " + W + " w:type=\"character\" w:styleId=\"Hipervnculo\"><w:name w:val=\"Hyperlink\"/>"
				+ "<w:rPr><w:color w:val=\"467886\" w:themeColor=\"hyperlink\"/><w:u w:val=\"single\"/></w:rPr></w:style>"));
		Body body = (Body) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:body " + W + ">"
				// a hyperlinked TOC over two paragraphs, its page numbers PAGEREF \h fields
				+ "<w:p><w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>"
				+ "<w:r><w:instrText xml:space=\"preserve\"> TOC \\o \"1-3\" \\h \\z \\u </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>" + link("TOCENTRYONE")
				+ "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText> PAGEREF _Toc1 \\h </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>"
				+ "<w:r><w:rPr><w:rStyle w:val=\"Hipervnculo\"/></w:rPr><w:t>PAGEONE</w:t></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r></w:p>"
				+ "<w:p>" + link("TOCENTRYTWO") + "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r></w:p>"
				// a TOC without \\h
				+ "<w:p><w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>"
				+ "<w:r><w:instrText xml:space=\"preserve\"> TOC \\o \"1-3\" </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>"
				+ "<w:r><w:rPr><w:rStyle w:val=\"Hipervnculo\"/></w:rPr><w:t>PLAINTOCENTRY</w:t></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r></w:p>"
				// a hyperlink in the body
				+ "<w:p>" + link("BODYLINK") + "</w:p>"
				+ "</w:body>"));
		pkg.getMainDocumentPart().getJaxbElement().setBody(body);
		return pkg;
	}

	private static List<R> runs(WordprocessingMLPackage pkg) {
		final List<R> runs = new ArrayList<R>();
		TraversalUtil.visit(pkg.getMainDocumentPart().getContent(), new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof R) runs.add((R) o);
				return null;
			}
		});
		return runs;
	}

	private static String style(List<R> runs, String text) {
		for (R r : runs) {
			if (XmlUtils.marshaltoString(r).contains(">" + text + "<")) {
				return r.getRPr() == null || r.getRPr().getRStyle() == null ? null : r.getRPr().getRStyle().getVal();
			}
		}
		throw new AssertionError(text + " not found");
	}

	@Test
	public void leavesTheStyleOffOnlyAHyperlinkedTocsEntries() throws Exception {
		WordprocessingMLPackage pkg = doc();
		TocHyperlinkStyle.process(pkg);
		List<R> runs = runs(pkg);
		assertNull(style(runs, "TOCENTRYONE"));
		assertNull(style(runs, "PAGEONE"));
		assertNull(style(runs, "TOCENTRYTWO"));
		assertEquals("Hipervnculo", style(runs, "PLAINTOCENTRY"));
		assertEquals("Hipervnculo", style(runs, "BODYLINK"));
	}

	@Test
	public void htmlDrawsTheEntriesPlain() throws Exception {
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			String pathway = flags == Docx4J.FLAG_NONE ? "visitor: " : "XSLT: ";
			WordprocessingMLPackage pkg = doc();
			HTMLSettings settings = Docx4J.createHTMLSettings();
			settings.setOpcPackage(pkg);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			Docx4J.toHTML(settings, os, flags);
			String html = os.toString("UTF-8");
			assertFalse(pathway + "TOCENTRYONE", span(html, "TOCENTRYONE").contains("Hipervnculo"));
			assertFalse(pathway + "TOCENTRYTWO", span(html, "TOCENTRYTWO").contains("Hipervnculo"));
			assertTrue(pathway + "PLAINTOCENTRY", span(html, "PLAINTOCENTRY").contains("Hipervnculo"));
			assertTrue(pathway + "BODYLINK", span(html, "BODYLINK").contains("Hipervnculo"));
			// the conversion worked on a copy
			assertEquals("Hipervnculo", style(runs(pkg), "TOCENTRYONE"));
		}
	}

	/** The start tag of the span holding a run's text */
	private static String span(String html, String text) {
		int at = html.indexOf(">" + text + "<");
		assertTrue(text + " in " + html, at > 0);
		return html.substring(html.lastIndexOf("<span", at), at);
	}
}
