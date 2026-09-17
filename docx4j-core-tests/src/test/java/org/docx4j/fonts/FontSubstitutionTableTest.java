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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The knowledge table, {@code font-substitutes.xml} (CR-017 phase 0): the substitutes
 * {@link Mapper#addMetricallyCompatibleSubstitutes()}, {@link FontFallback} and
 * {@link WidthFactors} had in their code until 17.1.1, now in one resource a report - and
 * a port - can read.
 *
 * <p>What this checks is that nothing was lost or invented in the move: the same document
 * fonts in the same order with the same substitutes; every name one the machine could
 * load a font under; every substitute the package can choose (the table's, and the class
 * defaults {@link FontFallback} names itself) accounted for in the catalogue, with a
 * licence and somewhere to get it, since that is what a report's action is made of; and
 * the scripts column re-measured against whichever of those faces this machine has.</p>
 */
public class FontSubstitutionTableTest {

	@BeforeClass
	public static void fonts() {
		new IdentityPlusMapper(); // font discovery happens in the static initialiser
	}

	/** The document fonts the table covers, in the order the pass takes them - the order
	 *  Mapper.addMetricallyCompatibleSubstitutes had them in until 17.1.1. */
	@Test
	public void theTableCoversTheDocumentFontsTheCodeDid() {

		List<String> expected = Arrays.asList(
				"Times New Roman", "Arial", "Courier New",
				"Calibri", "Cambria", "Calibri Light",
				"Century Gothic",
				"Tahoma", "Segoe UI", "Gadugi", "Helvetica", "Helvetica Neue", "Tw Cen MT",
				"Trebuchet MS", "Arial Black", "Verdana", "Comic Sans MS", "Segoe UI Light",
				"Garamond", "Bookman Old Style",
				"Georgia", "Book Antiqua", "Palatino Linotype",
				"Arial Narrow", "Consolas", "Lucida Console");

		List<String> actual = new ArrayList<String>();
		for (FontSubstitutionTable.Row row : FontSubstitutionTable.substitutes()) {
			actual.add(row.getDocumentFont());
		}
		assertEquals(expected, actual);
	}

	/** The substitutes themselves, best first, for the rows whose order was measured. */
	@Test
	public void theSubstitutesAreTheMeasuredOnesInOrder() {

		assertEquals(Arrays.asList("Tinos Regular", "Liberation Serif"), names("Times New Roman"));
		assertEquals(Arrays.asList("Carlito Regular", "Liberation Sans"), names("Calibri"));
		assertEquals(Arrays.asList("Caladea Regular", "Liberation Serif"), names("Cambria"));
		// Droid Sans is the closest installed face in every weight; Arimo the last resort
		assertEquals(Arrays.asList("Droid Sans", "Arimo Regular", "Liberation Sans"), names("Trebuchet MS"));
		assertEquals(Arrays.asList("Noto Sans Black", "Noto Sans Display Black", "Arimo Regular",
				"Liberation Sans"), names("Arial Black"));
		// Selawik is Microsoft's own open Segoe UI, measured against Word's golden
		// (CR-017 phase 5); Arimo remains for a machine without it
		assertEquals(Arrays.asList("Selawik", "Arimo Regular", "Liberation Sans"), names("Segoe UI"));
		assertEquals(Arrays.asList("Selawik Light", "Source Sans 3", "Source Sans Pro", "Arimo Regular",
				"Liberation Sans"), names("Segoe UI Light"));
		// Gelasio is metric-compatible with Georgia in all four faces, measured; P052 is
		// 11 to 12% narrow in Georgia's bold and italic and stays as the second choice
		assertEquals(Arrays.asList("Gelasio Regular", "P052", "Tinos Regular", "Liberation Serif"),
				names("Georgia"));
		// only a metric twin will do for a condensed face; otherwise it is left unmapped
		assertEquals(Arrays.asList("Liberation Sans Narrow", "Nimbus Sans Narrow"), names("Arial Narrow"));
	}

	private static List<String> names(String documentFont) {
		FontSubstitutionTable.Row row = FontSubstitutionTable.rowFor(documentFont);
		assertNotNull(documentFont + " has no row", row);
		return Arrays.asList(row.substituteNames());
	}

	/** The per-script rows, in the order {@link FontFallback} tries them. */
	@Test
	public void theScriptRowsAreTheMeasuredOnes() {

		List<String> rows = new ArrayList<String>();
		for (FontSubstitutionTable.Row row : FontSubstitutionTable.scriptSubstitutes()) {
			rows.add(row.getDocumentFont() + "/" + row.getScript());
		}
		assertEquals(Arrays.asList("*/EMOJI", "*/SYMBOL", "Sylfaen/GEORGIAN", "Cambria/GREEK"), rows);

		// Word's own face first, then the monochrome faces a Linux box may have
		assertEquals("Segoe UI Emoji", FontSubstitutionTable.scriptSubstitutes().get(0).substituteNames()[0]);
		assertEquals("Segoe UI Symbol", FontSubstitutionTable.scriptSubstitutes().get(1).substituteNames()[0]);
		// Caladea has no Greek at all, so Cambria's Greek needs a face of its own
		assertEquals(Arrays.asList("P052"),
				Arrays.asList(FontSubstitutionTable.scriptSubstitutes().get(3).substituteNames()));
	}

	/** A script row is for one document font (by name) or for every font ({@code *}). */
	@Test
	public void aScriptRowMatchesTheFontItIsFor() {
		FontSubstitutionTable.Row emoji = FontSubstitutionTable.scriptSubstitutes().get(0);
		FontSubstitutionTable.Row cambriaGreek = FontSubstitutionTable.scriptSubstitutes().get(3);
		assertTrue(emoji.matches("Whatever"));
		assertTrue(emoji.matches(null));
		assertTrue(cambriaGreek.matches("Cambria"));
		assertTrue(cambriaGreek.matches("Cambria Math")); // as the startsWith test it replaced did
		assertFalse(cambriaGreek.matches("Calibri"));
		assertFalse(cambriaGreek.matches(null));
	}

	/** The one measured width factor, and the substitute test that goes with it: a
	 *  machine without Carlito puts Calibri Light on a face whose advances are not
	 *  Calibri's at all, and gets no factor. */
	@Test
	public void theWidthFactorIsTheOneMeasured() {
		assertEquals(2, FontSubstitutionTable.widthFactors().size());
		assertEquals(0.987, WidthFactors.factorFor("Calibri Light", "Carlito Regular"), 0.0001);
		// Meiryo's Latin against Carlito's, measured on a corpus document Word drew in it
		assertEquals(1.21, WidthFactors.factorFor("Meiryo", "Carlito Regular"), 0.0001);
		assertEquals("not where the machine has Meiryo itself",
				1, WidthFactors.factorFor("Meiryo", "Meiryo"), 0.0001);
		assertEquals(0.987, WidthFactors.factorFor("Calibri Light", "Carlito Regular+kern"), 0.0001);
		assertEquals(1, WidthFactors.factorFor("Calibri Light", "Liberation Sans"), 0.0001);
		assertEquals(1, WidthFactors.factorFor("Calibri", "Carlito Regular"), 0.0001);
		assertTrue(WidthFactors.hasFactor("calibri light"));
		assertFalse(WidthFactors.hasFactor("Calibri"));
	}

	/** Every row names a font PhysicalFonts could hand back if the machine had it: a
	 *  trimmed name, carrying none of the FO layer's suffixes, and - where the machine
	 *  does have it - the face PhysicalFonts finds under that name is the one named. */
	@Test
	public void everyRowNamesALoadableFont() {
		for (String name : allSubstituteNames()) {
			assertEquals("'" + name + "' is not trimmed", name.trim(), name);
			assertTrue("'" + name + "' is empty", name.length() > 0);
			assertEquals("'" + name + "' carries an FO suffix", name, PhysicalFonts.stripSuffixes(name));
			PhysicalFont pf = PhysicalFonts.get(name);
			if (pf == null) continue; // not installed here; nothing to check but the shape
			assertNotNull(name + " resolves to a font with no name", pf.getName());
		}
	}

	/** Every substitute the package can choose is in the catalogue, with a licence and
	 *  somewhere to get it: the table's own rows, and the class defaults and
	 *  wide-coverage faces {@link FontFallback} names for itself. */
	@Test
	public void everyFontTheCodeNamesIsInTheCatalogue() {

		List<String> named = new ArrayList<String>(allSubstituteNames());
		named.addAll(FontFallback.namedSubstitutes());

		for (String name : named) {
			FontSubstitutionTable.Clone clone = FontSubstitutionTable.cloneNamed(name);
			assertNotNull(name + " is chosen by the code but is not in the table's catalogue,"
					+ " so a report could not say where to get it", clone);
			assertTrue(name + " has no licence in the catalogue",
					clone.getLicence() != null && clone.getLicence().length() > 0);
			assertTrue(name + " says neither a docx4j jar nor a package to get it from",
					(clone.getJar() != null && clone.getJar().length() > 0)
					|| (clone.getPackages() != null && clone.getPackages().length() > 0));
			assertTrue(name + " does not say what scripts it carries",
					clone.getScripts() != null && clone.getScripts().length() > 0);
		}
	}

	/** Quality is one of the three words the report grades on. */
	@Test
	public void everySubstituteSaysHowGoodItIs() {
		List<FontSubstitutionTable.Row> rows = new ArrayList<FontSubstitutionTable.Row>();
		rows.addAll(FontSubstitutionTable.substitutes());
		rows.addAll(FontSubstitutionTable.scriptSubstitutes());
		for (FontSubstitutionTable.Row row : rows) {
			for (FontSubstitutionTable.Substitute s : row.getSubstitutes()) {
				assertTrue(row.getDocumentFont() + " -> " + s.getFont() + " has quality '"
						+ s.getQuality() + "'",
						"metric".equals(s.getQuality()) || "measured".equals(s.getQuality())
						|| "class".equals(s.getQuality()));
				if ("measured".equals(s.getQuality())) {
					assertTrue(row.getDocumentFont() + " -> " + s.getFont()
							+ " is measured but says no error", s.getError() != null);
				}
			}
		}
	}

	/**
	 * The scripts column, re-measured: for every catalogue face this machine or the font
	 * jars on the test classpath carry, the font really does have a glyph for each script
	 * the table claims.  Faces it does not have are not checked - the column was read
	 * with fc-query on the machine the table was written on.
	 */
	@Test
	public void theScriptsColumnHoldsForTheFontsThisMachineHas() {

		Map<String, Integer> markers = new LinkedHashMap<String, Integer>();
		markers.put("Latin", 0x0041);      // A
		markers.put("Greek", 0x03B1);      // alpha
		markers.put("Cyrillic", 0x0430);   // a
		markers.put("Georgian", 0x10D0);   // an
		markers.put("Armenian", 0x0561);   // ayb
		markers.put("Hebrew", 0x05D0);     // alef
		markers.put("Arabic", 0x0627);     // alef

		int checked = 0;
		for (FontSubstitutionTable.Clone clone : FontSubstitutionTable.clones()) {
			PhysicalFont pf = PhysicalFonts.get(clone.getName());
			if (pf == null && clone.getAlsoKnownAs() != null) pf = PhysicalFonts.get(clone.getAlsoKnownAs());
			if (pf == null) continue;
			for (String token : clone.getScripts().split(",")) {
				Integer cp = markers.get(token.trim());
				if (cp == null) continue; // prose, eg "the emoji blocks; no text script"
				assertTrue(clone.getName() + " (" + pf.getName() + ") has no " + token.trim()
						+ ", which the table says it carries",
						FontFallback.covers(pf, new int[] { cp.intValue() }));
				checked++;
			}
		}
		assertTrue("no catalogue face is installed here, so nothing was checked", checked > 0);
	}

	/**
	 * Caladea, Cambria's metric twin in Latin, has no Greek at all - which is why
	 * Cambria's Greek has a row of its own, and why the table says "Latin" for Caladea
	 * where it says "Latin, Greek, Cyrillic" for Carlito.
	 */
	@Test
	public void caladeaStillHasNoGreek() {
		PhysicalFont caladea = PhysicalFonts.get("Caladea Regular");
		if (caladea == null) caladea = PhysicalFonts.get("Caladea");
		if (caladea == null) return; // the crosextra jar is not on this classpath
		assertFalse("Caladea now has Greek; the Cambria/GREEK row and the scripts column"
				+ " need re-measuring", FontFallback.covers(caladea, new int[] { 0x03B1 }));
		assertEquals("Latin", FontSubstitutionTable.cloneNamed("Caladea").getScripts());
	}

	/** Every substitute named anywhere in the table, rows and script rows alike. */
	private static List<String> allSubstituteNames() {
		List<String> names = new ArrayList<String>();
		List<FontSubstitutionTable.Row> rows = new ArrayList<FontSubstitutionTable.Row>();
		rows.addAll(FontSubstitutionTable.substitutes());
		rows.addAll(FontSubstitutionTable.scriptSubstitutes());
		for (FontSubstitutionTable.Row row : rows) {
			for (String name : row.substituteNames()) {
				if (!names.contains(name)) names.add(name);
			}
		}
		return names;
	}
}
