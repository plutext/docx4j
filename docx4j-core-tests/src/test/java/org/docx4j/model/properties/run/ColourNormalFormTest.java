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
package org.docx4j.model.properties.run;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CTRPrChange;
import org.docx4j.wml.Color;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.Test;

/**
 * CR-061: a docx4j save writes every colour in the schema's form, holding Word's reading (CR-060 sections 12 and 13):
 * an accepted value as upper-case RRGGBB, auto as auto; a rejected w:color dropped (val="auto" beside a theme colour),
 * a rejected w:u/@w:color dropped, a rejected fill or border colour as auto; a revision's former run properties too,
 * with CR-060's drop of an invalid highlight, vertAlign or em. The object in memory keeps what it read. That the save
 * keeps Word's reading is RunPropertiesWord365Test.roundTripKeepsWordsReading's.
 */
public class ColourNormalFormTest {

	private static final String[] COLOUR_PROBES = { "colour-case", "colour-hex", "colour-names-css",
		"colour-names-highlight", "colour-table-grey", "colour-table-other", "props-enum-other", "props-enum-missing",
		"props-u-valid", "colour-styles", "colour-elsewhere", "colour-auto-styles", "colour-elsewhere-styles" };

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/**
	 * A part of the package as docx4j saves it, every XML part read first, as a caller who edited the file would have:
	 * a part docx4j never unmarshals is copied as it came.
	 */
	static String saved(WordprocessingMLPackage pkg, String part) throws Exception {
		for (org.docx4j.openpackaging.parts.Part p : pkg.getParts().getParts().values()) {
			if (p instanceof org.docx4j.openpackaging.parts.JaxbXmlPart) ((org.docx4j.openpackaging.parts.JaxbXmlPart<?>) p).getJaxbElement();
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pkg.save(out);
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))) {
			ZipEntry e;
			while ((e = zis.getNextEntry()) != null) {
				if (e.getName().equals(part)) return new String(zis.readAllBytes(), "UTF-8");
			}
		}
		throw new AssertionError("no " + part);
	}

	/** The colour values in a part's XML that aren't in the schema's form, and any w:color without a w:val */
	static List<String> outOfForm(String xml) {
		List<String> bad = new ArrayList<>();
		Pattern[] attributes = {
			Pattern.compile("<w:color\\b[^>]*?\\bw:val=\"([^\"]*)\""),
			Pattern.compile("<w:u\\b[^>]*?\\bw:color=\"([^\"]*)\""),
			Pattern.compile("<w:shd\\b[^>]*?\\bw:fill=\"([^\"]*)\""),
			Pattern.compile("<w:shd\\b[^>]*?\\bw:color=\"([^\"]*)\""),
			Pattern.compile("<w:(?:top|left|bottom|right|start|end|between|bar|bdr|insideH|insideV|tl2br|tr2bl)\\b[^>]*?\\bw:color=\"([^\"]*)\""),
			Pattern.compile("<w:background\\b[^>]*?\\bw:color=\"([^\"]*)\"") };
		for (Pattern p : attributes) {
			Matcher m = p.matcher(xml);
			while (m.find()) {
				if (!m.group(1).matches("auto|[0-9A-F]{6}")) bad.add(m.group());
			}
		}
		Matcher bare = Pattern.compile("<w:color(?:\\s+(?!w:val=)[\\w:]+=\"[^\"]*\")*\\s*/>").matcher(xml);
		while (bare.find()) bad.add(bare.group() + " (no w:val)");
		return bad;
	}

	/** A marker's run in a part's XML */
	static String run(String xml, String marker) {
		int i = xml.indexOf(">" + marker + "<");
		assertTrue(marker, i > 0);
		int s = Math.max(xml.lastIndexOf("<w:r>", i), xml.lastIndexOf("<w:r ", i));
		return xml.substring(s, xml.indexOf("</w:r>", i));
	}

	@Test
	public void everySavedColourIsInTheSchemasForm() throws Exception {
		List<String> bad = new ArrayList<>();
		for (String f : COLOUR_PROBES) {
			WordprocessingMLPackage pkg = RunPropertiesWord365Test.load(f + ".docx");
			for (String part : new String[] { "word/document.xml", "word/styles.xml" }) {
				String xml;
				try {
					xml = saved(pkg, part);
				} catch (AssertionError noStyles) {
					continue;
				}
				for (String b : outOfForm(xml)) bad.add(f + " " + part + ": " + b);
			}
		}
		assertTrue(bad.size() + " colours out of the schema's form:\n" + String.join("\n", bad), bad.isEmpty());
	}

	@Test
	public void eachRuleAsWordSavesIt() throws Exception {
		String caseXml = saved(RunPropertiesWord365Test.load("colour-case.docx"), "word/document.xml");
		assertTrue(run(caseXml, "K4_Red"), run(caseXml, "K4_Red").contains("w:val=\"FF0000\""));
		assertTrue(run(caseXml, "K1_auto"), run(caseXml, "K1_auto").contains("w:val=\"auto\""));
		String hex = saved(RunPropertiesWord365Test.load("colour-hex.docx"), "word/document.xml");
		assertTrue(run(hex, "X1_ff0000"), run(hex, "X1_ff0000").contains("w:val=\"FF0000\""));
		assertTrue(run(hex, "X10_FF0000"), run(hex, "X10_FF0000").contains("w:val=\"FF0000\""));
		assertFalse(run(hex, "X3_F00"), run(hex, "X3_F00").contains("<w:color"));          // rejected: absent
		String styles = saved(RunPropertiesWord365Test.load("colour-styles.docx"), "word/document.xml");
		assertFalse(run(styles, "R2_F00"), run(styles, "R2_F00").contains("<w:color"));     // the style's shows
		assertTrue(run(styles, "R9_AUTO"), run(styles, "R9_AUTO").contains("w:val=\"auto\""));
		String elsewhere = saved(RunPropertiesWord365Test.load("colour-elsewhere.docx"), "word/document.xml");
		assertTrue(run(elsewhere, "U1_UNDERLINE_RED"), run(elsewhere, "U1_UNDERLINE_RED").contains("w:color=\"FF0000\""));
		assertTrue(run(elsewhere, "U3_UNDERLINE_F00"), run(elsewhere, "U3_UNDERLINE_F00").contains("<w:u w:val=\"single\"/>"));
		assertTrue(run(elsewhere, "S2_SHADING_AQUA"), run(elsewhere, "S2_SHADING_AQUA").contains("w:fill=\"00FFFF\""));
		assertTrue(run(elsewhere, "S3_SHADING_F00"), run(elsewhere, "S3_SHADING_F00").contains("w:fill=\"auto\""));
		assertTrue(run(elsewhere, "B1_BORDER_RED"), run(elsewhere, "B1_BORDER_RED").contains("w:color=\"FF0000\""));
		assertTrue(run(elsewhere, "B2_BORDER_F00"), run(elsewhere, "B2_BORDER_F00").contains("w:color=\"auto\""));
	}

	private static WordprocessingMLPackage synthetic() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String rev = " w:author=\"A\" w:date=\"2026-10-24T00:00:00Z\"";
		String[] ps = {
			"<w:p " + W + "><w:r><w:rPr><w:color w:val=\"bogus\" w:themeColor=\"accent1\"/></w:rPr><w:t>T1_THEME</w:t></w:r></w:p>",
			"<w:p " + W + "><w:r><w:rPr><w:b/><w:rPrChange w:id=\"1\"" + rev + "><w:rPr><w:color w:val=\"F00\"/>"
				+ "<w:highlight w:val=\"YELLOW\"/><w:vertAlign/><w:em w:val=\"DOT\"/><w:u w:val=\"single\" w:color=\"red\"/>"
				+ "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"bogus\"/></w:rPr></w:rPrChange></w:rPr><w:t>T2_REVISION</w:t></w:r></w:p>",
			"<w:p " + W + "><w:r><w:rPr><w:i/><w:rPrChange w:id=\"2\"" + rev + "><w:rPr><w:color w:val=\"red\"/>"
				+ "<w:highlight w:val=\"green\"/></w:rPr></w:rPrChange></w:rPr><w:t>T3_REVISION_NAME</w:t></w:r></w:p>",
			"<w:p " + W + "><w:pPr><w:rPr><w:b/><w:rPrChange w:id=\"3\"" + rev + "><w:rPr><w:color w:val=\"F00\"/>"
				+ "<w:highlight w:val=\"bogus\"/><w:color w:val=\"navy\"/></w:rPr></w:rPrChange></w:rPr></w:pPr>"
				+ "<w:r><w:t>T4_MARK_REVISION</w:t></w:r></w:p>" };
		for (String p : ps) pkg.getMainDocumentPart().getContent().add(XmlUtils.unmarshalString(p));
		return pkg;
	}

	/** Beside a theme colour, a rejected w:val is written "auto" (it is required; the theme colour is drawn) */
	@Test
	public void aRejectedValueBesideAThemeColourIsAuto() throws Exception {
		String xml = saved(synthetic(), "word/document.xml");
		assertTrue(run(xml, "T1_THEME"), run(xml, "T1_THEME").matches("(?s).*<w:color (?=[^>]*w:val=\"auto\")(?=[^>]*w:themeColor=\"accent1\")[^>]*/>.*"));
	}

	/** A revision's former run properties, which Reject All restores, are written as live ones are */
	@Test
	public void aRevisionsFormerPropertiesToo() throws Exception {
		String xml = saved(synthetic(), "word/document.xml");
		String t2 = run(xml, "T2_REVISION");
		assertFalse(t2, t2.contains("<w:color"));
		assertFalse(t2, t2.contains("<w:highlight"));
		assertFalse(t2, t2.contains("<w:vertAlign"));
		assertFalse(t2, t2.contains("<w:em"));
		assertTrue(t2, t2.contains("w:color=\"FF0000\""));
		assertTrue(t2, t2.contains("w:fill=\"auto\""));
		String t3 = run(xml, "T3_REVISION_NAME");
		assertTrue(t3, t3.contains("<w:color w:val=\"FF0000\"/>"));
		assertTrue(t3, t3.contains("<w:highlight w:val=\"green\"/>"));
		int p4 = xml.indexOf("T4_MARK_REVISION");
		String mark = xml.substring(xml.lastIndexOf("<w:p>", p4), p4);
		assertFalse(mark, mark.contains("w:val=\"F00\""));
		assertFalse(mark, mark.contains("<w:highlight"));
		assertTrue(mark, mark.contains("<w:color w:val=\"000080\"/>"));
	}

	/** Only what is written is normalised: the object in memory keeps the value it read (D6) */
	@Test
	public void theObjectInMemoryIsUnchanged() throws Exception {
		WordprocessingMLPackage pkg = synthetic();
		saved(pkg, "word/document.xml");
		P p3 = (P) XmlUtils.unwrap(pkg.getMainDocumentPart().getContent().get(2));
		R r = (R) XmlUtils.unwrap(p3.getContent().get(0));
		CTRPrChange.RPr former = r.getRPr().getRPrChange().getRPr();
		boolean red = false;
		for (Object o : former.getEGRPrBase()) {
			if (XmlUtils.unwrap(o) instanceof Color && "red".equals(((Color) XmlUtils.unwrap(o)).getVal())) red = true;
		}
		assertTrue("the former w:color still reads \"red\" in memory", red);
		P p2 = (P) XmlUtils.unwrap(pkg.getMainDocumentPart().getContent().get(1));
		R r2 = (R) XmlUtils.unwrap(p2.getContent().get(0));
		assertEquals("the dropped elements are still in memory", 6, r2.getRPr().getRPrChange().getRPr().getEGRPrBase().size());
	}
}
