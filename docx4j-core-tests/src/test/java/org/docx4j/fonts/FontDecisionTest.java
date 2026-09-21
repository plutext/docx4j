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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Fonts;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The decision each pass records (CR-017 phase 1): which pass answered, how it got
 * there, what is known of the substitute's width error, whose line box the text takes,
 * and which scripts left the font's own face during the conversion.
 *
 * <p>The mapping itself is CR-016's and unchanged; what is tested here is that every
 * pass says what it did, in the terms the report and a bisect need - batch 42's step (a)
 * spent its first hours reconstructing, per document, which pass had answered.</p>
 */
public class FontDecisionTest {

	private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	private static final String SANS = "Liberation Sans";
	private static final String SERIF = "Liberation Serif";

	@BeforeClass
	public static void fonts() {
		new IdentityPlusMapper(); // discovery
		Assume.assumeTrue("Liberation not on the classpath",
				PhysicalFonts.get(SANS) != null && PhysicalFonts.get(SERIF) != null);
	}

	private static Fonts fontTable(String entries) throws Exception {
		return (Fonts) XmlUtils.unwrap(XmlUtils.unmarshalString(
				"<w:fonts xmlns:w=\"" + W + "\">" + entries + "</w:fonts>"));
	}

	private static Set<String> names(String... n) {
		return new HashSet<String>(Arrays.asList(n));
	}

	// ------------------------------------------------------------------ one per pass

	@Test
	public void theFontItself() throws Exception {
		IdentityPlusMapper m = new IdentityPlusMapper();
		m.populateFontMappings(names(SANS), fontTable(""));
		FontDecision d = m.getDecision(SANS);
		assertNotNull(d);
		assertEquals(FontDecision.Source.INSTALLED, d.getSource());
		assertEquals(PhysicalFonts.get(SANS), d.getPhysicalFont());
		assertNull("an installed font needs no explanation", d.getVia());
		assertNull("and has no width error", d.getWidthError());
	}

	@Test
	public void theDocumentsEmbeddedForm() throws Exception {
		IdentityPlusMapper m = new IdentityPlusMapper();
		m.registerRegularForm("Docx4j Decision Embedded", PhysicalFonts.get(SERIF));
		m.populateFontMappings(names("Docx4j Decision Embedded"), fontTable(""));
		FontDecision d = m.getDecision("Docx4j Decision Embedded");
		assertNotNull(d);
		assertEquals(FontDecision.Source.EMBEDDED, d.getSource());
	}

	/** The mapper's own answer: IdentityPlusMapper's variant of the name. */
	@Test
	public void theMappersOwnAnswer() throws Exception {
		PhysicalFonts.put("Docx4j Decision Variant Regular", PhysicalFonts.get(SANS));
		IdentityPlusMapper m = new IdentityPlusMapper();
		m.populateFontMappings(names("Docx4j Decision Variant"), fontTable(""));
		FontDecision d = m.getDecision("Docx4j Decision Variant");
		assertNotNull(d);
		assertEquals(FontDecision.Source.MAPPER_OWN, d.getSource());
		assertTrue("the via names what it found: " + d.getVia(),
				d.getVia() != null && d.getVia().contains("a variant of the name")
						&& d.getVia().toLowerCase().contains("docx4j decision variant regular"));
	}

	/** A metric clone, from the table: Word's advances, and the row's measurement. */
	@Test
	public void aMetricClone() throws Exception {
		Assume.assumeTrue("Times New Roman is installed here; the identity mapping applies",
				PhysicalFonts.get("Times New Roman") == null);
		IdentityPlusMapper m = new IdentityPlusMapper();
		m.populateFontMappings(names("Times New Roman"), fontTable(""));
		m.addMetricallyCompatibleSubstitutes();
		FontDecision d = m.getDecision("Times New Roman");
		assertNotNull(d);
		assertEquals(FontDecision.Source.METRIC_CLONE, d.getSource());
		if (PhysicalFonts.get("Tinos Regular") != null) {
			// the row's first substitute, the one measured: its error is quoted
			assertNotNull("the row's measurement", d.getWidthError());
			assertTrue(d.getWidthError(), d.getWidthError().contains("0.05%"));
		} else {
			// a box with the Liberation jar only (EC2, 2026-09-21): Liberation Serif is the
			// same metrics but the row carries no measurement for it, so none is quoted
			assertNotNull(PhysicalFonts.get("Liberation Serif"));
			assertEquals("no measurement for the second substitute", null, d.getWidthError());
		}
		assertEquals("its own line box", "documentFont", d.getLineBox());
	}

	/** A measured stand-in, where no clone exists: the error is named. */
	@Test
	public void aMeasuredStandIn() throws Exception {
		Assume.assumeTrue(PhysicalFonts.get("Trebuchet MS") == null);
		Assume.assumeTrue("neither measured face for Trebuchet is installed",
				PhysicalFonts.get("Droid Sans") != null || PhysicalFonts.get("Arimo Regular") != null);
		IdentityPlusMapper m = new IdentityPlusMapper();
		m.populateFontMappings(names("Trebuchet MS"), fontTable(""));
		m.addMetricallyCompatibleSubstitutes();
		FontDecision d = m.getDecision("Trebuchet MS");
		assertNotNull(d);
		assertEquals(FontDecision.Source.MEASURED_STAND_IN, d.getSource());
		assertTrue("the measurement, in the table's words: " + d.getWidthError(),
				d.getWidthError() != null && d.getWidthError().contains("body"));
	}

	/** The document's own w:altName, hop by hop. */
	@Test
	public void theAltNameChain() throws Exception {
		IdentityPlusMapper m = new IdentityPlusMapper();
		Fonts table = fontTable(
				"<w:font w:name=\"Docx4j Decision Alt A\"><w:altName w:val=\"Docx4j Decision Alt B\"/></w:font>"
				+ "<w:font w:name=\"Docx4j Decision Alt B\"><w:altName w:val=\"" + SERIF + "\"/></w:font>");
		Set<String> in = names("Docx4j Decision Alt A");
		m.populateFontMappings(in, table);
		m.addAltNameSubstitutes(in, table);
		FontDecision d = m.getDecision("Docx4j Decision Alt A");
		assertNotNull(d);
		assertEquals(FontDecision.Source.ALT_NAME, d.getSource());
		assertEquals("w:altName Docx4j Decision Alt B -> " + SERIF, d.getVia());
		// Word uses the alternate, so the line box is the alternate's
		assertEquals("alias:" + SERIF, d.getLineBox());
	}

	/** A face of the same class: the class is named, and the error is unknown. */
	@Test
	public void aFaceOfTheSameClass() throws Exception {
		String font = "Docx4j Decision Grotesk"; // "grotesk" is a sans to FontFallback
		Assume.assumeTrue(FontFallback.selectByClass(font) != null);
		IdentityPlusMapper m = new IdentityPlusMapper();
		m.populateFontMappings(names(font), fontTable(""));
		m.addClassBasedSubstitutes(names(font));
		FontDecision d = m.getDecision(font);
		assertNotNull(d);
		assertEquals(FontDecision.Source.CLASS, d.getSource());
		assertTrue("the class it stood in for: " + d.getVia(), d.getVia().contains("SANS"));
		assertEquals(Mapper.UNKNOWN_ERROR, d.getWidthError());
		assertEquals("nothing knows its line box", "substitute", d.getLineBox());
	}

	/** Word's own answer for a font it cannot find, and the line box that comes with it. */
	@Test
	public void wordsDefaultForAnUnknownFamily() throws Exception {
		String font = "Docx4j Decision Unknown";
		IdentityPlusMapper m = new IdentityPlusMapper();
		Fonts table = fontTable("<w:font w:name=\"" + font + "\"><w:family w:val=\"swiss\"/></w:font>");
		Set<String> in = names(font);
		m.populateFontMappings(in, table);
		m.addMetricallyCompatibleSubstitutes();
		m.addAltNameSubstitutes(in, table);
		m.addClassBasedSubstitutes(in);
		m.addWordDefaultSubstitutes(in, table);
		FontDecision d = m.getDecision(font);
		assertNotNull(d);
		assertEquals(FontDecision.Source.WORD_DEFAULT, d.getSource());
		assertTrue("the family Word substitutes: " + d.getVia(), d.getVia().contains("Calibri"));
		assertEquals("Word draws it in Calibri, so the line box is Calibri's",
				"wordDefault:Calibri", d.getLineBox());
	}

	/** Nothing mapped it: the decision says so, rather than the font being absent from
	 *  the record altogether (which is how it looked before 17.2.0). */
	@Test
	public void unmapped() throws Exception {
		String font = "Docx4j Decision Nonesuch";
		IdentityPlusMapper m = new IdentityPlusMapper();
		m.populateFontMappings(names(font), fontTable(""));
		FontDecision d = m.getDecision(font);
		assertNotNull(d);
		assertEquals(FontDecision.Source.UNMAPPED, d.getSource());
		assertNull(d.getPhysicalFont());
		assertNull(d.getBoldFace());
	}

	/** The no-bold-face pass is not a source of its own: it re-maps to an alias, and the
	 *  decision reports a synthetic bold, which is what Word does for such a family. */
	@Test
	public void aFamilyWithNoBoldFaceOfItsOwn() throws Exception {
		Assume.assumeTrue(PhysicalFonts.get("Calibri Light") == null);
		IdentityPlusMapper m = new IdentityPlusMapper();
		Set<String> in = names("Calibri Light");
		m.populateFontMappings(in, fontTable(""));
		m.addMetricallyCompatibleSubstitutes();
		FontDecision before = m.getDecision("Calibri Light");
		Assume.assumeTrue("nothing to withhold on this machine", before.getPhysicalFont() != null
				&& PhysicalFonts.getBoldForm(before.getPhysicalFont()) != null);
		m.addNoBoldFaceAliases(in);
		FontDecision d = m.getDecision("Calibri Light");
		assertEquals("the pass before it still owns the decision", before.getSource(), d.getSource());
		assertEquals(Mapper.SYNTHETIC, d.getBoldFace());
	}

	/** The measured width factor rides on the decision. */
	@Test
	public void theWidthFactorIsOnTheDecision() throws Exception {
		Assume.assumeTrue(PhysicalFonts.get("Calibri Light") == null);
		Assume.assumeTrue(PhysicalFonts.get("Carlito Regular") != null);
		IdentityPlusMapper m = new IdentityPlusMapper();
		m.populateFontMappings(names("Calibri Light"), fontTable(""));
		m.addMetricallyCompatibleSubstitutes();
		FontDecision d = m.getDecision("Calibri Light");
		assertEquals(0.987, d.getWidthFactor(), 0.0001);
		assertTrue("the factor is in the error too: " + d.getWidthError(),
				d.getWidthError().contains("0.987"));
	}

	// ------------------------------------------------------- the selector's own entries

	/** A script the mapped face cannot draw goes to another face during the conversion,
	 *  and the decision records where it went - keyed as the selector's own cache is.
	 *  Georgian, because the Liberation faces the test maps to have none of it (Greek and
	 *  Cyrillic they do have, so the coverage pass never runs on those). */
	@Test
	public void aScriptThatLeftTheFontsFace() throws Exception {
		String font = "Docx4j Decision Georgian";
		WordprocessingMLPackage pkg = FontsTestSupport.packageWith(
				FontsTestSupport.styles("<w:rFonts w:ascii=\"" + font + "\" w:hAnsi=\"" + font + "\"/>", null),
				null, null,
				FontsTestSupport.p(null, "აბგ"),
				font, SERIF);
		Mapper m = pkg.getFontMapper();
		FontsTestSupport.families(pkg, 0);
		FontDecision d = m.getDecision(font);
		assertNotNull("the selector records against the document font", d);
		FontDecision.ScriptChoice georgian = null;
		for (FontDecision.ScriptChoice sc : d.getPerScript()) {
			if ("GEORGIAN".equals(sc.getScript())) georgian = sc;
		}
		assertNotNull("no GEORGIAN entry in " + d, georgian);
		// the face where the machine has one, and null - recorded, not silent - where not
		assertEquals(FontFallback.selectCovering(font, new int[] { 0x10d0, 0x10d1, 0x10d2 }) == null,
				georgian.getFace() == null);
	}

	/** A symbol font is drawn in whatever face has the glyphs, whatever the mapper made
	 *  of the name; the decision says so rather than naming the mapper's text font. */
	@Test
	public void aSymbolFont() throws Exception {
		Assume.assumeTrue("no symbol face on this machine", PhysicalFonts.getWDingsFont() != null);
		WordprocessingMLPackage pkg = FontsTestSupport.packageWith(
				FontsTestSupport.styles("<w:rFonts w:ascii=\"" + SANS + "\" w:hAnsi=\"" + SANS + "\"/>", null),
				null, null,
				FontsTestSupport.p("<w:rFonts w:ascii=\"Wingdings\" w:hAnsi=\"Wingdings\"/>", "ü"),
				SANS, SANS, "Wingdings", SERIF);
		FontsTestSupport.families(pkg, 0);
		FontDecision d = pkg.getFontMapper().getDecision("Wingdings");
		assertNotNull(d);
		assertEquals(FontDecision.Source.SYMBOL, d.getSource());
		assertEquals(PhysicalFonts.getWDingsFont(), d.getPhysicalFont());
	}

	// ------------------------------------------------------------------ the whole set

	/** Every mapping the passes make has a decision; one without is a hole in the record
	 *  (CR-017's first risk), and the report would have nothing to say about that font. */
	@Test
	public void everyMappingHasADecision() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) XmlUtils.unwrap(
				XmlUtils.unmarshalString("<w:document xmlns:w=\"" + W + "\"><w:body>"
						+ "<w:p><w:r><w:rPr><w:rFonts w:ascii=\"Trebuchet MS\" w:hAnsi=\"Trebuchet MS\"/></w:rPr><w:t>a</w:t></w:r></w:p>"
						+ "<w:p><w:r><w:rPr><w:rFonts w:ascii=\"Docx4j Decision Wholeset\" w:hAnsi=\"Docx4j Decision Wholeset\"/></w:rPr><w:t>b</w:t></w:r></w:p>"
						+ "<w:p><w:r><w:sym w:font=\"Wingdings\" w:char=\"F0FC\"/></w:r></w:p>"
						+ "</w:body></w:document>")));
		Mapper m = new IdentityPlusMapper();
		pkg.setFontMapper(m); // the whole pass pipeline

		assertFalse("no fonts were mapped", m.getFontMappings().isEmpty());
		for (String key : m.getFontMappings().keySet()) {
			assertNotNull("mapped with no decision recorded: " + key, m.getDecision(key));
		}

		List<FontDecision> decisions = m.getDecisions();
		assertTrue(decisions.size() >= m.getFontMappings().size());
		// a stable order, whatever order fontsInUse hands the names over in
		for (int i = 1; i < decisions.size(); i++) {
			assertTrue("out of order: " + decisions.get(i - 1).getDocumentFont() + " then "
					+ decisions.get(i).getDocumentFont(),
					String.CASE_INSENSITIVE_ORDER.compare(decisions.get(i - 1).getDocumentFont(),
							decisions.get(i).getDocumentFont()) <= 0);
		}
		// and every decision names the font it is about, in one line
		for (FontDecision d : decisions) {
			assertTrue(d.toString(), d.toString().startsWith(d.getDocumentFont() + ": "));
		}
	}
}
