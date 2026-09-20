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
package org.docx4j.fonts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Fonts;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The report (CR-017 phases 2 and 3): what the document uses each font for, how close
 * what docx4j draws it with is, what the fontTable says about the machine it was saved
 * on, and what to do about it.
 *
 * <p>The documents here are the shapes of the CR-016 probes - docx4j-layout-fidelity
 * generates those (<code>fonts-unresolvable</code>, <code>fonts-light-bold</code>,
 * <code>fonts-symbol-and-emoji</code>, <code>fonts-theme-lang</code>) and this module
 * cannot depend on the harness, so each case is built here with the probe's own
 * fontTable entries and text.  The two worked <code>authorHad</code> cases are corpus
 * documents, cited by id in the comments and never copied into the repository.</p>
 */
public class FontsAnalysisTest {

	private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	private static final String SANS = "Liberation Sans";
	private static final String SERIF = "Liberation Serif";
	private static final String SENTENCE = "The quick brown fox jumps over the lazy dog";

	@BeforeClass
	public static void fonts() {
		new IdentityPlusMapper();
		Assume.assumeTrue("Liberation not on the classpath",
				PhysicalFonts.get(SANS) != null && PhysicalFonts.get(SERIF) != null);
	}

	/** A package whose document defaults name a font, with these body paragraphs and
	 *  this font table; the mapper populated by the usual passes. */
	private static WordprocessingMLPackage packageWith(String defaultFont, String bodyXml,
			String fontTableEntries) throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getStyleDefinitionsPart().setJaxbElement(
				(org.docx4j.wml.Styles) XmlUtils.unwrap(XmlUtils.unmarshalString(
						"<w:styles xmlns:w=\"" + W + "\"><w:docDefaults><w:rPrDefault><w:rPr>"
						+ "<w:rFonts w:ascii=\"" + defaultFont + "\" w:hAnsi=\"" + defaultFont + "\"/>"
						+ "</w:rPr></w:rPrDefault><w:pPrDefault/></w:docDefaults>"
						+ "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\">"
						+ "<w:name w:val=\"Normal\"/></w:style>"
						+ "<w:style w:type=\"character\" w:default=\"1\" w:styleId=\"DefaultParagraphFont\">"
						+ "<w:name w:val=\"Default Paragraph Font\"/></w:style></w:styles>")));
		if (fontTableEntries != null) {
			org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart ftp
					= pkg.getMainDocumentPart().getFontTablePart();
			if (ftp == null) {
				ftp = new org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart();
				pkg.getMainDocumentPart().addTargetPart(ftp);
			}
			ftp.setJaxbElement((Fonts) XmlUtils.unwrap(XmlUtils.unmarshalString(
					"<w:fonts xmlns:w=\"" + W + "\">" + fontTableEntries + "</w:fonts>")));
		}
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) XmlUtils.unwrap(
				XmlUtils.unmarshalString("<w:document xmlns:w=\"" + W + "\"><w:body>"
						+ bodyXml + "</w:body></w:document>")));
		pkg.setFontMapper(new IdentityPlusMapper());
		return pkg;
	}

	private static String p(String rPrXml, String text) {
		return "<w:p><w:r>" + (rPrXml == null ? "" : "<w:rPr>" + rPrXml + "</w:rPr>")
				+ "<w:t xml:space=\"preserve\">" + text + "</w:t></w:r></w:p>";
	}

	private static FontReport.Entry entryFor(FontReport report, String documentFont) {
		for (FontReport.Entry e : report.getEntries()) {
			if (e.getDocumentFont().equalsIgnoreCase(documentFont)) return e;
		}
		return null;
	}

	// ------------------------------------------------------------------ the use walk

	/** Characters and runs per font, script and face - the walk asks the selector per
	 *  character, so a run set in one font whose text is Latin and Greek counts once in
	 *  each script. */
	@Test
	public void theUseWalkCountsCharactersScriptsAndFaces() throws Exception {

		WordprocessingMLPackage pkg = packageWith(SERIF,
				p(null, "abc")
				+ p("<w:b/>", "de")
				+ p(null, "αβ"),   // Greek
				null);
		FontUsage usage = FontsAnalysis.usage(pkg);

		assertEquals(7, usage.getCharacters());
		assertEquals(3, usage.getRuns());
		FontUsage.Use use = usage.get(SERIF);
		assertNotNull("the document default carries every run", use);
		assertEquals(7, use.getCharacters());
		assertEquals(3, use.getRuns());
		assertEquals("abc and de", 5, use.getByScript().get("LATIN")[0]);
		assertTrue(use.getScripts().contains("GREEK"));
		assertEquals(2, use.getByScript().get("GREEK")[0]);
		assertTrue(use.getFaces().contains(FontUsage.Face.REGULAR));
		assertTrue(use.getFaces().contains(FontUsage.Face.BOLD));
		assertEquals(2, use.getByFace().get(FontUsage.Face.BOLD)[0]);
		// a code point of each script, so a report can ask the face whether it has it
		assertNotNull(use.sampleCodePoint("GREEK"));
		assertEquals(0x03b1, use.sampleCodePoint("GREEK").intValue());
	}

	/** The headers, footers, notes and comments are walked, as fontsInUse walks them. */
	@Test
	public void theUseWalkCoversTheOtherStories() throws Exception {

		WordprocessingMLPackage pkg = packageWith(SERIF, p(null, "body"), null);
		org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart header
				= new org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart();
		header.setJaxbElement((org.docx4j.wml.Hdr) XmlUtils.unwrap(XmlUtils.unmarshalString(
				"<w:hdr xmlns:w=\"" + W + "\">" + p(null, "header!") + "</w:hdr>")));
		pkg.getMainDocumentPart().addTargetPart(header);

		FontUsage usage = FontsAnalysis.usage(pkg);
		assertEquals("body + header!", 4 + 7, usage.getCharacters());
	}

	/** A w:sym is one character of the font it names (probe fonts-symbol-and-emoji (b)). */
	@Test
	public void theUseWalkCountsSymbolCharacters() throws Exception {

		WordprocessingMLPackage pkg = packageWith(SERIF,
				"<w:p><w:r><w:sym w:font=\"Wingdings\" w:char=\"F0FC\"/></w:r></w:p>", null);
		FontUsage usage = FontsAnalysis.usage(pkg);
		FontUsage.Use use = usage.get("Wingdings");
		assertNotNull(use);
		assertEquals(1, use.getCharacters());
		assertTrue(use.getScripts().contains(FontFallback.SYMBOL_GROUP));
	}

	// ------------------------------------------------------------------ the grades

	/** The document's own font, installed: nothing to report and nothing to do. */
	@Test
	public void anInstalledFontIsExact() throws Exception {
		WordprocessingMLPackage pkg = packageWith(SANS, p(null, SENTENCE), null);
		FontReport.Entry entry = entryFor(FontsAnalysis.analyse(pkg), SANS);
		assertNotNull(entry);
		assertEquals(FontReport.Grade.EXACT, entry.getGrade());
		assertEquals("", entry.getAction());
	}

	/** A metric clone is exact too - Word's advances (probe fonts-light-bold's Calibri
	 *  line, where Word itself draws Carlito). */
	@Test
	public void aMetricCloneIsExact() throws Exception {
		Assume.assumeTrue(PhysicalFonts.get("Calibri") == null);
		Assume.assumeTrue(PhysicalFonts.get("Carlito Regular") != null);
		WordprocessingMLPackage pkg = packageWith("Calibri", p(null, SENTENCE), null);
		FontReport.Entry entry = entryFor(FontsAnalysis.analyse(pkg), "Calibri");
		assertNotNull(entry);
		assertEquals(FontReport.Grade.EXACT, entry.getGrade());
		assertEquals(FontDecision.Source.METRIC_CLONE, entry.getDecision().getSource());
	}

	/** A measured stand-in is near, and the report names the measurement. */
	@Test
	public void aMeasuredStandInIsNear() throws Exception {
		Assume.assumeTrue(PhysicalFonts.get("Trebuchet MS") == null);
		Assume.assumeTrue(PhysicalFonts.get("Droid Sans") != null || PhysicalFonts.get("Arimo Regular") != null);
		WordprocessingMLPackage pkg = packageWith("Trebuchet MS", p(null, SENTENCE), null);
		FontReport.Entry entry = entryFor(FontsAnalysis.analyse(pkg), "Trebuchet MS");
		assertNotNull(entry);
		assertEquals(FontReport.Grade.NEAR, entry.getGrade());
		assertTrue(entry.getAction(), entry.getAction().startsWith("install Trebuchet MS"));
		assertTrue(entry.getAction(), entry.getAction().contains("closest measured"));
		// not "no open clone exists": what is true is that docx4j's table has no metric
		// clone of it (Selawik is an open replacement for Segoe UI and still a stand-in,
		// having no italic face).  The action carries the measurement itself, since it is
		// a field of its own in the JSON.  @since 17.2.0
		assertTrue(entry.getAction(), entry.getAction().contains("docx4j knows no metric clone of it"));
		assertFalse(entry.getAction(), entry.getAction().contains("no open clone of it exists"));
		String error = entry.getDecision().getWidthError();
		if (error != null && !Mapper.UNKNOWN_ERROR.equals(error)) {
			assertTrue(entry.getAction(), entry.getAction().contains(error));
		}
	}

	/** A clone which cannot draw a script the document sets in that font is only near,
	 *  and the note says where the script goes - Cambria's Greek, which Caladea has none
	 *  of (CR-016; the measured answer is P052). */
	@Test
	public void aCloneWithAScriptGoingElsewhereIsNear() throws Exception {
		Assume.assumeTrue(PhysicalFonts.get("Cambria") == null);
		Assume.assumeTrue(PhysicalFonts.get("Caladea Regular") != null);
		WordprocessingMLPackage pkg = packageWith("Cambria",
				p(null, SENTENCE) + p(null, "Ανοικτό"), null);
		FontReport.Entry entry = entryFor(FontsAnalysis.analyse(pkg), "Cambria");
		assertNotNull(entry);
		assertEquals(FontReport.Grade.NEAR, entry.getGrade());
		boolean greek = false;
		for (String note : entry.getNotes()) {
			if (note.startsWith("GREEK")) greek = true;
		}
		assertTrue("no note about the Greek: " + entry.getNotes(), greek);
	}

	/** An unknown family gets Word's own default, which is a class answer: the widths
	 *  are not the document font's (probe fonts-unresolvable (b): a swiss family). */
	@Test
	public void wordsDefaultForAnUnknownFamilyIsClass() throws Exception {
		String font = "Docx4j Report Unknown";
		WordprocessingMLPackage pkg = packageWith(font, p(null, SENTENCE),
				"<w:font w:name=\"" + font + "\"><w:panose1 w:val=\"020B0604020202020204\"/>"
				+ "<w:family w:val=\"swiss\"/><w:sig w:usb0=\"A00002EF\" w:csb0=\"0000019F\"/></w:font>");
		FontReport.Entry entry = entryFor(FontsAnalysis.analyse(pkg), font);
		assertNotNull(entry);
		assertEquals(FontReport.Grade.CLASS, entry.getGrade());
		assertEquals(FontDecision.Source.WORD_DEFAULT, entry.getDecision().getSource());
		assertEquals("Word draws such a font in Calibri, so the line box is Calibri's",
				"wordDefault:Calibri", entry.getDecision().getLineBox());
	}

	/** Nothing mapped it: FOP's base-14 fallback, and the report says so (probe
	 *  fonts-unresolvable's held-back case: a name no table knows, left unmapped). */
	@Test
	public void anUnmappableFontIsNone() throws Exception {
		String font = "Docx4j Report Narrow"; // condensed: deliberately left unmapped
		WordprocessingMLPackage pkg = packageWith(font, p(null, SENTENCE), null);
		FontReport.Entry entry = entryFor(FontsAnalysis.analyse(pkg), font);
		assertNotNull(entry);
		if (entry.getDecision().getPhysicalFont() != null) return; // a machine with a condensed twin
		assertEquals(FontReport.Grade.NONE, entry.getGrade());
		assertTrue(entry.getAction(), entry.getAction().contains("Mapper.put"));
	}

	// ------------------------------------------------------------------ authorHad

	/** The fontTable evidence, each answer with the entry that gives it.  The worked
	 *  cases are corpus documents 9919 (whose "Nokia Pure Text" entry carries w:panose1
	 *  and w:sig, and whose "Wingdings-Regular" entry is w:notTrueType with an all-zero
	 *  panose) and 278, the EnBW document of the 17.2.0 CHANGELOG (w:panose1 and w:sig). */
	@Test
	public void theFontTableSaysWhatTheAuthorsMachineHad() throws Exception {

		String panoseAndSig = "<w:font w:name=\"Docx4j Had\"><w:panose1 w:val=\"020B0604020202020204\"/>"
				+ "<w:charset w:val=\"00\"/><w:family w:val=\"swiss\"/>"
				+ "<w:sig w:usb0=\"A00002EF\" w:csb0=\"0000019F\"/></w:font>";
		String nameOnly = "<w:font w:name=\"Docx4j Lacked\"><w:charset w:val=\"00\"/></w:font>";
		String notTrueType = "<w:font w:name=\"Docx4j NotTrueType\">"
				+ "<w:panose1 w:val=\"00000000000000000000\"/><w:notTrueType/></w:font>";

		WordprocessingMLPackage pkg = packageWith(SERIF,
				p("<w:rFonts w:ascii=\"Docx4j Had\" w:hAnsi=\"Docx4j Had\"/>", "a")
				+ p("<w:rFonts w:ascii=\"Docx4j Lacked\" w:hAnsi=\"Docx4j Lacked\"/>", "b")
				+ p("<w:rFonts w:ascii=\"Docx4j NotTrueType\" w:hAnsi=\"Docx4j NotTrueType\"/>", "c")
				+ p("<w:rFonts w:ascii=\"Docx4j NoEntry\" w:hAnsi=\"Docx4j NoEntry\"/>", "d"),
				panoseAndSig + nameOnly + notTrueType);

		FontReport report = FontsAnalysis.analyse(pkg);
		assertEquals(FontReport.AuthorHad.LIKELY, entryFor(report, "Docx4j Had").getAuthorHad());
		assertTrue(entryFor(report, "Docx4j Had").getAuthorHadEvidence().contains("w:panose1 and w:sig"));
		assertEquals(FontReport.AuthorHad.UNLIKELY, entryFor(report, "Docx4j Lacked").getAuthorHad());
		assertEquals(FontReport.AuthorHad.UNLIKELY, entryFor(report, "Docx4j NotTrueType").getAuthorHad());
		assertTrue(entryFor(report, "Docx4j NotTrueType").getAuthorHadEvidence().contains("w:notTrueType"));
		assertEquals(FontReport.AuthorHad.UNKNOWN, entryFor(report, "Docx4j NoEntry").getAuthorHad());
		assertTrue(entryFor(report, "Docx4j NoEntry").getAuthorHadEvidence().contains("no w:font entry"));
	}

	/** UNLIKELY turns the action round: Word itself could not find the font, so docx4j
	 *  drawing what Word drew is the faithful answer and installing it would not be. */
	@Test
	public void unlikelyTurnsTheActionRound() throws Exception {
		String font = "Docx4j Report Lacked";
		WordprocessingMLPackage pkg = packageWith(font, p(null, SENTENCE),
				"<w:font w:name=\"" + font + "\"><w:charset w:val=\"00\"/>"
				+ "<w:family w:val=\"swiss\"/></w:font>");
		FontReport.Entry entry = entryFor(FontsAnalysis.analyse(pkg), font);
		assertNotNull(entry);
		assertEquals(FontReport.AuthorHad.UNLIKELY, entry.getAuthorHad());
		assertEquals(FontDecision.Source.WORD_DEFAULT, entry.getDecision().getSource());
		assertTrue(entry.getAction(), entry.getAction().startsWith("nothing to do"));
		assertTrue(entry.getAction(), entry.getAction().contains("away from the page its author saw"));
	}

	/** The document embeds it: the strongest answer, and no action at all. */
	@Test
	public void anEmbeddedFontIsTheAuthorsOwn() throws Exception {
		WordprocessingMLPackage pkg = packageWith("Docx4j Embedded", p(null, SENTENCE), null);
		// as FontTablePart.processEmbeddings does, before the passes run
		Mapper mapper = new IdentityPlusMapper();
		mapper.registerRegularForm("Docx4j Embedded", PhysicalFonts.get(SERIF));
		pkg.setFontMapper(mapper);
		FontReport.Entry entry = entryFor(FontsAnalysis.analyse(pkg), "Docx4j Embedded");
		assertNotNull(entry);
		assertEquals(FontReport.AuthorHad.EMBEDDED, entry.getAuthorHad());
		assertEquals(FontDecision.Source.EMBEDDED, entry.getDecision().getSource());
		assertEquals(FontReport.Grade.EXACT, entry.getGrade());
	}

	// ------------------------------------------------------------------ the renderings

	/** The report is built from the use walk, not from the mapper's whole map: the metric
	 *  table maps its 26 fonts whether the document names them or not. */
	@Test
	public void theReportIsAboutTheFontsTheDocumentUses() throws Exception {
		WordprocessingMLPackage pkg = packageWith(SANS, p(null, SENTENCE), null);
		FontReport report = FontsAnalysis.analyse(pkg);
		assertTrue("a report about every font in the table: " + report.getEntries().size(),
				report.getEntries().size() < 10);
		assertNotNull(entryFor(report, SANS));
		assertNull("Verdana is in the substitution table but not in this document",
				entryFor(report, "Verdana"));
	}

	@Test
	public void theTextAndJsonRenderings() throws Exception {
		WordprocessingMLPackage pkg = packageWith(SANS, p(null, SENTENCE), null);
		FontReport report = FontsAnalysis.analyse(pkg);

		String text = report.toText();
		assertTrue(text, text.contains("Fonts in"));
		assertTrue(text, text.contains(SANS));
		assertTrue(text, text.contains("the machine that saved it"));

		String json = report.toJson();
		assertTrue(json, json.startsWith("{"));
		assertTrue(json, json.contains("\"worstGrade\""));
		assertTrue(json, json.contains("\"documentFont\": \"" + SANS + "\""));
		assertTrue(json, json.contains("\"authorHad\""));
		// the strings are escaped, so a font name with a quote cannot break it
		assertFalse(json, json.contains("\"action\": " + SANS));
		assertEquals("balanced braces", count(json, '{'), count(json, '}'));
		assertEquals("balanced brackets", count(json, '['), count(json, ']'));
	}

	private static int count(String s, char c) {
		int n = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == c) n++;
		}
		return n;
	}

	/** The summary the conversion will log: one line per font, with the grade in it. */
	@Test
	public void theSummaryIsOneLinePerFont() throws Exception {
		WordprocessingMLPackage pkg = packageWith(SANS, p(null, SENTENCE), null);
		FontReport report = FontsAnalysis.analyse(pkg);
		assertEquals(report.getEntries().size(), report.getSummaryLines().size());
		for (String line : report.getSummaryLines()) {
			assertFalse("a summary line must be one line: " + line, line.contains("\n"));
			assertTrue(line, line.contains(":"));
		}
	}

	// ------------------------------------------------------------------ the environment

	/** A jars-only deployment is a different answer, and the report says which jar to
	 *  add (issue #695: the jars are the whole font supply in a container). */
	@Test
	public void theJarsOnlyEnvironment() throws Exception {
		FontEnvironment jars = FontEnvironment.jarsOnly();
		Assume.assumeTrue("no font jar on this classpath", jars.size() > 0);
		assertTrue(jars.getName().contains("jar"));
		/* Every face it names came from a jar; a face the jar supplies which this machine
		 * also has installed under that name is the machine's entry in PhysicalFonts, so
		 * it is not in this view - see the method's javadoc. */
		for (String name : jars.names()) {
			assertTrue(name, PhysicalFonts.get(name) != null);
		}

		WordprocessingMLPackage pkg = packageWith("Verdana", p(null, SENTENCE), null);
		FontReport report = FontsAnalysis.analyse(pkg, jars);
		assertEquals("the docx4j font jars", report.getEnvironment());
		FontReport.Entry entry = entryFor(report, "Verdana");
		assertNotNull(entry);
		if (jars.has(entry.getDecision().getPhysicalFont().getName())) return; // the jar has it
		boolean saysSo = false;
		for (String note : entry.getNotes()) {
			if (note.contains("does not have")) saysSo = true;
		}
		assertTrue("no note about the environment: " + entry.getNotes(), saysSo);
	}

	/** The machine's environment is a live view, not a snapshot: discovery happens in the
	 *  Mapper's static initialiser, which may not have run when it is asked for. */
	@Test
	public void theMachineEnvironmentIsLive() {
		assertTrue(FontEnvironment.machine().has(SANS));
		assertEquals(PhysicalFonts.getPhysicalFonts().size(), FontEnvironment.machine().size());
	}
}
