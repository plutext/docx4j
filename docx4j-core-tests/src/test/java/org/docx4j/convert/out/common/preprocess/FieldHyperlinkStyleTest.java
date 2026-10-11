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
 * The Hyperlink character style left off where Word leaves it off (FieldHyperlinkStyle): checked
 * case by case against Word's PDF of the probe in {@code hyperlink-style/}, and on a document
 * whose Hyperlink style has a translated id ({@code Hipervnculo}), as in a Spanish document: it
 * is found by its name.  The FO side is export-fo-tests' FieldHyperlinkStyleFoTest.
 */
public class FieldHyperlinkStyleTest {

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
		FieldHyperlinkStyle.process(pkg);
		List<R> runs = runs(pkg);
		assertNull(style(runs, "TOCENTRYONE"));
		assertNull(style(runs, "PAGEONE"));
		assertNull(style(runs, "TOCENTRYTWO"));
		assertEquals("Hipervnculo", style(runs, "PLAINTOCENTRY"));
		assertEquals("Hipervnculo", style(runs, "BODYLINK"));
	}

	/**
	 * Every labelled run of the probe that has a character style keeps it exactly where Word drew
	 * it.  Each style the probe uses is 14pt over 11pt body text, so Word's size says whether it
	 * drew the style (word-readings.tsv).
	 */
	@Test
	public void matchesWordOnTheProbe() throws Exception {
		java.util.Map<String, Boolean> drawn = new java.util.LinkedHashMap<String, Boolean>();
		try (java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(
				getClass().getClassLoader().getResourceAsStream("hyperlink-style/word-readings.tsv"),
				java.nio.charset.StandardCharsets.UTF_8))) {
			in.readLine();
			for (String line; (line = in.readLine()) != null;) {
				String[] f = line.split("\t");
				drawn.put(f[0], Double.parseDouble(f[7]) > 13);
			}
		}
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(
				getClass().getClassLoader().getResourceAsStream("hyperlink-style/hl-fields.docx"));
		List<R> runs = runs(pkg);
		java.util.Map<String, R> styledRuns = new java.util.LinkedHashMap<String, R>();
		for (String label : drawn.keySet()) {
			R run = labelled(runs, label);
			if (run.getRPr() != null && run.getRPr().getRStyle() != null) styledRuns.put(label, run);
		}
		FieldHyperlinkStyle.process(pkg);
		int checked = 0;
		for (java.util.Map.Entry<String, R> e : styledRuns.entrySet()) {
			boolean kept = e.getValue().getRPr().getRStyle() != null;
			assertEquals(e.getKey() + ": Word drew the style", drawn.get(e.getKey()), kept);
			checked++;
		}
		assertEquals("cases with a character style", 19, checked);
	}

	private static R labelled(List<R> runs, String label) {
		for (R r : runs) {
			for (Object o : r.getContent()) {
				Object u = XmlUtils.unwrap(o);
				if (u instanceof org.docx4j.wml.Text && ((org.docx4j.wml.Text) u).getValue().startsWith(label)
						&& (((org.docx4j.wml.Text) u).getValue().length() == label.length()
								|| !Character.isLetterOrDigit(((org.docx4j.wml.Text) u).getValue().charAt(label.length())))) {
					return r;
				}
			}
		}
		throw new AssertionError(label + " not found");
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
