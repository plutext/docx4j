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

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Measured width factors: what a substitute's advances have to be multiplied by for a
 * run to measure what the document font measures.
 *
 * <p>Most substitutions need none.  A metric clone is by definition already right, and a
 * stand-in chosen on class alone ({@link FontFallback}) is chosen because nothing closer
 * exists, so a factor there would be false precision - its letterforms are not the
 * document font's and no single number makes them fit.  This table is for the third
 * case, where the substitute has the document font's <em>design</em> and the wrong
 * width: a family which is another weight of a family docx4j has a clone of.  Word draws
 * Calibri Light with Calibri's skeleton and its own, narrower advances; Carlito, the
 * Calibri clone, ships only the regular weight, so every Calibri Light line comes out
 * uniformly wide and re-breaks.</p>
 *
 * <p>The factor is applied by {@link RunFontSelector} exactly as Word's own character
 * scaling (<code>w:w</code>) is - as a letter space spread over the run's characters -
 * and the two are multiplied where a run has both.  One approximation is worth knowing:
 * Word's difference is per glyph, where this is spread evenly, so the run's word spaces
 * shrink with its letters.  What it reproduces exactly is the whole run's advance, which
 * is what decides where a line breaks.</p>
 *
 * <p>Only the XSL FO pathway applies it; HTML names the document font itself and leaves
 * the width to the reader's machine.</p>
 *
 * @since 17.1.1
 */
public final class WidthFactors {

	private WidthFactors() {}

	/** A measured factor, and the substitute family it was measured against. */
	private static final class Factor {
		private final String substitute;
		private final double factor;
		Factor(String substitute, double factor) {
			this.substitute = substitute;
			this.factor = factor;
		}
	}

	private static final Map<String, Factor> FACTORS;
	static {
		Map<String, Factor> m = new HashMap<String, Factor>();

		/* Calibri Light, against Carlito.
		 *
		 * Probe fonts-light-bold (CR-016 phase 0b) sets the same 83-character sentence
		 * in Calibri Light and in Calibri in one Word document.  Word's pen advances -
		 * the distance from the first glyph origin to the last, which carries no side
		 * bearing - are 374.797pt and 379.500pt, so Calibri Light is 0.98761 of Calibri.
		 * The same golden also has Word drawing a run in Carlito itself, and that run's
		 * pen advance agrees with TextMeasurer's Carlito to 0.04 per cent, which fixes
		 * the second half: Carlito is Calibri's metric twin to 0.07 per cent.  So
		 * Calibri Light is 374.797 / 379.775 = 0.9869 of Carlito, and the factor is
		 * that, rounded.
		 *
		 * Corroborated within a corpus document, which is the form that carries no
		 * assumption about Word's PDF at all: in 13743 the ratio of Word's Calibri Light
		 * lines to Word's own Calibri lines, each against Carlito at the same size, is
		 * 0.9838 / 0.9955 = 0.9883, i.e. the same 1.2 per cent, on 92 lines.  Measured
		 * over three documents (13743, 1654, 2065; 80 unjustified exact-match lines
		 * each) the mean absolute error per line is 1.62 / 1.51 / 1.40 per cent without
		 * the factor.
		 *
		 * Read the size off the font, not off the PDF: mutool reports the text-matrix
		 * size, which Word writes on a 1/300 inch grid (10pt as 10.08, 11 as 11.04, 9 as
		 * 9.12) and compensates for elsewhere, so measuring a candidate at the reported
		 * size makes it up to 1 per cent too wide and the ratio correspondingly low.
		 * The first pass at this factor read 0.985 that way.
		 *
		 * No installed face does this job without a factor: every genuinely Light design
		 * measured is 10 to 20 per cent away (Source Sans Pro 0.963, Noto Sans Light
		 * 0.884, DejaVu Sans ExtraLight 0.794), because Calibri Light is not a
		 * light-weight design in that sense - it is Calibri, a little narrower.
		 * (CR-001 batch 42 item 3.)
		 *
		 * The bold is a separate rule and not this one: a family with no bold face of
		 * its own is emboldened at its own advances, which is
		 * Mapper.addNoBoldFaceAliases (probe P7).  A machine without the crosextra
		 * clones puts Calibri Light on Liberation Sans, whose advances are not
		 * Calibri's at all, and gets no factor - hence the substitute test below. */
		m.put(key("Calibri Light"), new Factor("carlito", 0.987));

		FACTORS = Collections.unmodifiableMap(m);
	}

	/**
	 * The measured factor for this document font given the physical font docx4j mapped
	 * it to, or 1 where there is none.
	 *
	 * <p>The table is keyed on the <em>document</em> font: that is the name the
	 * measurement belongs to, and the physical name carries whichever of the FO layer's
	 * suffixes have been stacked on it (<code>+kern</code>, <code>+noliga</code>,
	 * <code>+nobold</code>), so it is only used - stripped - to check that the
	 * substitute really is the family the factor was measured against.</p>
	 *
	 * @param documentFontName the font the docx asks for
	 * @param physicalFontName the name of the PhysicalFont it resolved to, with or
	 *        without those suffixes
	 * @since 17.1.1
	 */
	public static double factorFor(String documentFontName, String physicalFontName) {

		if (documentFontName == null || physicalFontName == null) return 1;
		Factor f = FACTORS.get(key(documentFontName));
		if (f == null) return 1;
		String physical = key(PhysicalFonts.stripSuffixes(physicalFontName));
		return physical.startsWith(f.substitute) ? f.factor : 1;
	}

	/** Whether this document font has an entry at all, whatever it is mapped to.
	 *  @since 17.1.1 */
	public static boolean hasFactor(String documentFontName) {
		return documentFontName != null && FACTORS.containsKey(key(documentFontName));
	}

	private static String key(String name) {
		return name.trim().toLowerCase(Locale.ENGLISH);
	}
}
