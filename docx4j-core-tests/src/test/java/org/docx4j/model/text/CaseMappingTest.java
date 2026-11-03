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
package org.docx4j.model.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.Document;
import org.docx4j.wml.Style;
import org.junit.Test;

/**
 * WD REQ-607 (CR-069 phase 1): {@code w:caps} written in capitals by the run's language, per code point, as Word's text
 * save and PDF write them (the {@code caps-lang} probes, {@code text-extraction/dpp/caps}, 31 October 2026);
 * {@code w:smallCaps} as stored unless {@code small_caps: upper}; the block model keeping the stored characters and
 * carrying the flags, {@code w:vertAlign} among them (REQ-710).
 */
public class CaseMappingTest {

	static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";

	/** Word's measurements: the run's language, one code point to one. */
	@Test
	public void wordsCapitals() {
		assertEquals("İSTANBUL FİLM", CaseMapping.upper("istanbul film", "tr-TR")); // caps-lang7c, 6a
		assertEquals("ISTANBUL FILM", CaseMapping.upper("istanbul film", "en-US")); // caps-lang6a
		assertEquals("ISTANBUL FILM", CaseMapping.upper("istanbul film", null)); // docx4j's default, en-US
		assertEquals("STRAßE", CaseMapping.upper("straße", "de-DE")); // caps-lang6b, 7b: no SS, no U+1E9E
		assertEquals("CAFÉ", CaseMapping.upper("café", "en-US")); // caps-lang6b
		assertEquals("ﬁNE CAFÉ Ǆ", CaseMapping.upper("ﬁne café ǆ", "en-US")); // caps-lang7d
	}

	/** The dotted capital I is Turkish's and Azeri's, by the primary subtag in any case; every other language maps i to I. */
	@Test
	public void theDottedIRule() {
		for (String lang : new String[] { "tr", "TR-tr", "tr_TR", "az", "az-Latn-AZ" })
			assertEquals(lang, "İ", CaseMapping.upper("i", lang));
		for (String lang : new String[] { "en-US", "de-DE", "lt-LT", "", "trk", "x-tr" })
			assertEquals(lang, "I", CaseMapping.upper("i", lang));
		// dotless i is lowercase-only: I in every language
		assertEquals("I", CaseMapping.upper("ı", "tr-TR"));
		assertEquals("I", CaseMapping.upper("ı", "en-US"));
	}

	/**
	 * Unicode's simple mapping, never the special casing of {@link String#toUpperCase(Locale)}: the differences pinned,
	 * since the ports' own upper (Python's {@code str.upper()}, JavaScript's {@code toUpperCase()}) is the special one.
	 */
	@Test
	public void notStringToUpperCase() {
		assertEquals("STRASSE", "straße".toUpperCase(Locale.ROOT));
		assertEquals("FINE", "ﬁne".toUpperCase(Locale.ROOT));
		assertEquals("ﬁNE", CaseMapping.upper("ﬁne", "en-US"));
		// U+1FB3: simple uppercase U+1FBC, special casing two letters; "keep a letter whose upper expands" is not the rule
		assertEquals("ΑΙ", "ᾳ".toUpperCase(Locale.ROOT));
		assertEquals("ᾼ", CaseMapping.upper("ᾳ", "el-GR"));
		// the titlecase digraph to its uppercase
		assertEquals("Ǆ", CaseMapping.upper("ǅ", "en-US"));
		// a supplementary letter (Deseret)
		assertEquals(new String(Character.toChars(0x10400)), CaseMapping.upper(new String(Character.toChars(0x10428)), "en-US"));
	}

	/** The mapping keeps the length in code points and in UTF-16 units, for every code point, so offsets hold (D8). */
	@Test
	public void lengthIsKept() {
		StringBuilder all = new StringBuilder();
		for (int cp = 0; cp <= Character.MAX_CODE_POINT; cp++) {
			if (cp >= Character.MIN_SURROGATE && cp <= Character.MAX_SURROGATE) continue;
			all.appendCodePoint(cp);
		}
		String text = all.toString();
		for (String lang : new String[] { "en-US", "tr-TR" }) {
			String upper = CaseMapping.upper(text, lang);
			assertEquals(text.length(), upper.length());
			assertEquals(text.codePointCount(0, text.length()), upper.codePointCount(0, upper.length()));
		}
		assertEquals("", CaseMapping.upper("", "tr-TR"));
		assertNull(CaseMapping.upper(null, "tr-TR"));
	}

	/** The flags and the language as the extractor sets them on Word's probe (dpp-forks-word365.docx). */
	@Test
	public void theBlockModelKeepsTheStoredTextAndCarriesTheFlags() throws Exception {
		TextResult r = Texts.of(load("text-extraction/dpp/dpp-forks-word365.docx"), new TextOptions());
		TextRun jackson = run(r, "Jackson"), roberts = run(r, "Roberts");
		assertTrue(jackson.isSmallCaps());
		assertFalse(jackson.isCaps());
		assertEquals("en-US", jackson.getLanguage()); // no w:lang: the document defaults'
		assertTrue(roberts.isCaps());
		assertFalse(roberts.isSmallCaps());
		TextRun plain = run(r, ", J., dissenting; ");
		assertFalse(plain.isCaps() || plain.isSmallCaps());
		assertNull(plain.getLanguage());
		assertEquals(TextRun.VerticalAlignment.BASELINE, plain.getVerticalAlignment());
		List<TextRun> raised = new ArrayList<TextRun>();
		for (TextBlock b : r.blocks()) for (TextRun t : b.getRuns()) if (t.getVerticalAlignment() != TextRun.VerticalAlignment.BASELINE) raised.add(t);
		assertEquals("[2 SUBSCRIPT, 2 SUPERSCRIPT, st SUPERSCRIPT, 3 SUPERSCRIPT]", aligned(raised));
		// the writers' capitals, not the block model's
		String text = PlainText.of(r);
		assertTrue(text, text.contains("C05_ Jackson, J., dissenting; ROBERTS, C.J."));
		assertTrue(PlainText.of(Texts.of(load("text-extraction/dpp/dpp-forks-word365.docx"), new TextOptions().smallCaps(TextOptions.SmallCaps.UPPER)))
				.contains("C05_ JACKSON, J., dissenting; ROBERTS, C.J."));
	}

	/** The run's own w:lang, else the document defaults': caps-lang6a and 6b. */
	@Test
	public void theRunsLanguage() throws Exception {
		TextResult a = Texts.of(load("text-extraction/dpp/caps/caps-lang6a.docx"), new TextOptions());
		List<String> langs = new ArrayList<String>();
		for (TextBlock b : a.blocks()) for (TextRun t : b.getRuns()) if (t.getLanguage() != null) langs.add(t.getLanguage() + (t.isCaps() ? " caps" : " smallCaps"));
		assertEquals("[tr-TR caps, en-US caps, tr-TR smallCaps]", langs.toString());
		String text = PlainText.of(a);
		assertTrue(text, text.contains("L02_ tr-TR caps: İSTANBUL FİLM (dotted"));
		assertTrue(text, text.contains("L03_ en-US caps: ISTANBUL FILM\n"));
		assertTrue(text, text.contains("L04_ tr-TR smallCaps: istanbul film\n"));
		TextResult b = Texts.of(load("text-extraction/dpp/caps/caps-lang6b.docx"), new TextOptions());
		assertTrue(PlainText.of(b).contains("L07_ no w:lang, caps: ISTANBUL FILM\n"));
	}

	/**
	 * The case is the text's: a paragraph style's caps applies (unlike its bold, which is no emphasis); a run under
	 * caps and small caps is a caps run; the runs merge only where their case and language agree.
	 */
	@Test
	public void theCaseIsTheTextsWhateverTheParagraphStyle() throws Exception {
		WordprocessingMLPackage pkg = document("<w:p><w:pPr><w:pStyle w:val=\"Shout\"/></w:pPr><w:r><w:t>loud</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:rPr><w:caps/><w:smallCaps/></w:rPr><w:t>both</w:t></w:r>"
				+ "<w:r><w:rPr><w:caps/><w:lang w:val=\"en-US\"/></w:rPr><w:t xml:space=\"preserve\"> i</w:t></w:r>"
				+ "<w:r><w:rPr><w:caps/><w:lang w:val=\"tr-TR\"/></w:rPr><w:t xml:space=\"preserve\"> i</w:t></w:r>"
				+ "<w:r><w:rPr><w:caps w:val=\"0\"/></w:rPr><w:t xml:space=\"preserve\"> off</w:t></w:r></w:p>");
		Style shout = (Style) XmlUtils.unmarshalString("<w:style xmlns:w=\"" + W + "\" w:type=\"paragraph\" w:styleId=\"Shout\">"
				+ "<w:name w:val=\"Shout\"/><w:rPr><w:b/><w:caps/></w:rPr></w:style>");
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add(shout);
		pkg.getMainDocumentPart().getPropertyResolver().refresh();
		TextResult r = Texts.of(pkg, new TextOptions());
		TextRun loud = r.blocks().get(0).getRuns().get(0);
		assertTrue(loud.isCaps());
		assertFalse(loud.isBold()); // the style's bold is the heading's, not emphasis
		List<TextRun> second = r.blocks().get(1).getRuns();
		// "both" takes the document defaults' en-US, so it merges with the en-US caps run, not the tr-TR one
		assertEquals("[both i, ' i' tr-TR, ' off']", runs(second));
		assertTrue(second.get(0).isCaps());
		assertFalse(second.get(0).isSmallCaps());
		assertFalse(second.get(2).isCaps());
		assertEquals("LOUD\nBOTH I İ off\n", PlainText.of(r));
	}

	/**
	 * Where neither the run, its styles nor the document defaults name a language: the default_language option,
	 * en-US by default. w:themeFontLang is not a link (WD 0.80): Word cases such a run by its editing language
	 * whether the settings name tr-TR there or not (the screen-caps probe, 4 November 2026).
	 */
	@Test
	public void theLanguageOfLastResort() throws Exception {
		String body = "<w:p><w:r><w:rPr><w:caps/></w:rPr><w:t>istanbul</w:t></w:r></w:p>";
		WordprocessingMLPackage pkg = document(body);
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		org.docx4j.wml.DocDefaults dd = mdp.getStyleDefinitionsPart().getJaxbElement().getDocDefaults();
		if (dd != null && dd.getRPrDefault() != null && dd.getRPrDefault().getRPr() != null) dd.getRPrDefault().getRPr().setLang(null);
		mdp.getPropertyResolver().refresh();
		DocumentSettingsPart settings = mdp.getDocumentSettingsPart();
		if (settings == null) {
			settings = new DocumentSettingsPart();
			settings.setJaxbElement(new CTSettings());
			mdp.addTargetPart(settings);
		}
		settings.getJaxbElement().setThemeFontLang(null);
		assertEquals("ISTANBUL\n", PlainText.of(Texts.of(pkg, new TextOptions())));
		assertEquals(CaseMapping.DEFAULT_LANGUAGE, Texts.of(pkg, new TextOptions()).blocks().get(0).getRuns().get(0).getLanguage());
		CTLanguage tr = new CTLanguage();
		tr.setVal("tr-TR");
		settings.getJaxbElement().setThemeFontLang(tr);
		assertEquals("ISTANBUL\n", PlainText.of(Texts.of(pkg, new TextOptions())));
		// default_language stands in for the editing language: a Turkish installation dots the i
		TextResult turkish = Texts.of(pkg, new TextOptions().defaultLanguage("tr-TR"));
		assertEquals("İSTANBUL\n", PlainText.of(turkish));
		assertEquals("tr-TR", turkish.blocks().get(0).getRuns().get(0).getLanguage());
		assertEquals("İSTANBUL\n", PlainText.of(Texts.of(pkg,
				TextOptions.of(java.util.Collections.singletonMap("default_language", "tr-TR")))));
	}

	/** default_language by name, its default, its place in WD section 10's order, the preset leaving it, a bad tag refused. */
	@Test
	public void theDefaultLanguageOption() {
		assertEquals(CaseMapping.DEFAULT_LANGUAGE, new TextOptions().getDefaultLanguage());
		assertEquals("en-US", new TextOptions().toMap().get("default_language"));
		assertEquals(TextOptions.NAMES.indexOf("small_caps") + 1, TextOptions.NAMES.indexOf("default_language"));
		assertEquals("en-US", new TextOptions().profile(TextOptions.Profile.WORD_TEXT).getDefaultLanguage());
		assertEquals("az-Latn-AZ", new TextOptions().defaultLanguage("az-Latn-AZ").profile(TextOptions.Profile.WORD_TEXT).copy().getDefaultLanguage());
		for (String bad : new String[] { "not a tag", "" }) {
			try {
				new TextOptions().defaultLanguage(bad);
				throw new AssertionError("accepted '" + bad + "'");
			} catch (IllegalArgumentException expected) {
				assertTrue(expected.getMessage(), expected.getMessage().startsWith("default_language: "));
			}
		}
	}

	/** A note reference is a reference, not a superscript (REQ-710), whatever its style raises; the probe's note case. */
	@Test
	public void aNoteReferenceIsNotASuperscript() throws Exception {
		TextResult r = Texts.of(load("text-extraction/phase0/extraction-probe.docx"), new TextOptions());
		int references = 0;
		for (TextBlock b : r.blocks()) for (TextRun t : b.getRuns()) {
			if (t.getKind() != TextRun.Kind.NOTE_REFERENCE) continue;
			references++;
			assertEquals(TextRun.VerticalAlignment.BASELINE, t.getVerticalAlignment());
		}
		assertTrue(references > 0);
	}

	/** small_caps by name, its default and the word-text preset's value. */
	@Test
	public void theSmallCapsOption() {
		assertEquals(TextOptions.SmallCaps.ASIS, new TextOptions().getSmallCaps());
		assertEquals("asis", new TextOptions().toMap().get("small_caps"));
		assertEquals(TextOptions.SmallCaps.UPPER, TextOptions.of(java.util.Collections.singletonMap("small_caps", "upper")).getSmallCaps());
		assertEquals(TextOptions.SmallCaps.UPPER, new TextOptions().profile(TextOptions.Profile.WORD_TEXT).getSmallCaps());
		assertEquals(TextOptions.SmallCaps.ASIS, new TextOptions().smallCaps(TextOptions.SmallCaps.ASIS).profile(TextOptions.Profile.WORD_TEXT).getSmallCaps());
		assertEquals(TextOptions.SmallCaps.UPPER, new TextOptions().smallCaps(TextOptions.SmallCaps.UPPER).copy().getSmallCaps());
		assertNotEquals(TextOptions.NAMES.indexOf("small_caps"), -1);
		assertEquals(TextOptions.NAMES.indexOf("symbols") + 1, TextOptions.NAMES.indexOf("small_caps")); // WD section 10's order
	}

	private static WordprocessingMLPackage load(String resource) throws Exception {
		try (InputStream is = ResourceUtils.getResource(resource)) {
			return WordprocessingMLPackage.load(is);
		}
	}

	private static WordprocessingMLPackage document(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unwrap(
				XmlUtils.unmarshalString("<w:document xmlns:w=\"" + W + "\"><w:body>" + body + "</w:body></w:document>")));
		return pkg;
	}

	private static TextRun run(TextResult r, String text) {
		for (TextBlock b : r.blocks()) for (TextRun t : b.getRuns()) if (t.getText().equals(text)) return t;
		throw new AssertionError("No run '" + text + "'");
	}

	private static String aligned(List<TextRun> runs) {
		List<String> out = new ArrayList<String>();
		for (TextRun t : runs) out.add(t.getText() + " " + t.getVerticalAlignment());
		return out.toString();
	}

	private static String runs(List<TextRun> runs) {
		List<String> out = new ArrayList<String>();
		for (TextRun t : runs) out.add(t.getText().startsWith(" ") ? "'" + t.getText() + "'" + (t.getLanguage() == null ? "" : " " + t.getLanguage()) : t.getText());
		return out.toString();
	}
}
