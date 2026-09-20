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

import java.util.Locale;

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
 * @since 17.2.0
 */
public final class WidthFactors {

	private WidthFactors() {}

	/* The factors themselves are font-substitutes.xml's since 17.2.0
	 * (FontSubstitutionTable, CR-017 phase 0), so that a report can cite them and the
	 * ports can read them.  The one row there is
	 *
	 *   <widthFactor font="Calibri Light" substituteFamily="Carlito" factor="0.987"/>
	 *
	 * and the measurement which chose it stays here:
	 *
	 * Calibri Light, against Carlito.
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
	 * Calibri's at all, and gets no factor - hence the substitute test in
	 * {@link #factorFor}.
	 *
	 *
	 * The per-face rows (CR-001 batch 46 item 1).
	 *
	 * A family's weights are not one another's width, and until this batch the
	 * table could not say so: one row per document font, and the FO naming the
	 * regular family for both weights (the bold coming from an ancestor's
	 * font-weight), so a bold run in a factored face could not be corrected at
	 * all.  Three documents had shown it - a Greek document two pages long after
	 * the Cambria row, a Tahoma-heavy document whose bold is 6 per cent out where
	 * its regular is within 0.6, and a Cambria document the single row
	 * over-corrected.
	 *
	 * Measured by batch 42's method, on the lines the two PDFs pair on identical
	 * text, unjustified, grouped by the face each side drew and by the size Word
	 * set, taking the median of Word's ink width over ours.  A ratio above 1 means
	 * our side is narrow and the factor raises it.  n is lines.
	 *
	 *   Cambria -> Caladea, as residuals on renders which already carry the 1.048
	 *   regular row, so the row below is 1.048 x the residual:
	 *
	 *     document 6693   regular      1.0011 (n=60, 9pt), 1.0012 (n=46, 11pt)
	 *                     bold         0.9895 (n=12), 1.0113 (n=23), 0.9912 (n=3)
	 *                     bold italic  0.9743 (n=10, 11pt)
	 *     document 3003   regular      1.0004 (n=12, 12pt), 1.0330 (n=7, 11pt)
	 *                     bold         0.9893 (n=10), 0.9896 (n=6), 0.9849 (n=3)
	 *
	 *   so the regular row is confirmed where it was cut and the bold is about
	 *   0.989 of it: 1.048 x 0.989 = 1.036.  Independently, batch 45 measured that
	 *   document 7399's Cambria bold wants 1.0363 where the single row gave it
	 *   1.048, and it was the document that row cost lines.  The bold italic is
	 *   1.048 x 0.9743 = 1.021, on one document's ten lines.
	 *
	 *   Cambria -> P052 (the Greek script substitute; no factor before this batch):
	 *
	 *     document 8371   regular      0.9868 (n=59, 12pt), 0.9826 (n=6), 0.9708 (n=6)
	 *                     bold         1.0585 (n=6), 1.0574 (n=6), 1.0514 (n=11)
	 *
	 *   Those lines are part Latin and part Greek - Caladea has no Greek at all, so
	 *   the coverage pass cuts every Greek paragraph in two and the extractor names
	 *   a line for its first glyph - which biases each bucket towards 1.  Two
	 *   earlier readings which are not so biased agree on the direction and read
	 *   further out: font to font, Word/P052 0.9744 and Word/P052-Bold 1.0656
	 *   (batch 42 item 2); on that document's wholly Greek lines, 0.9925 and 0.9813
	 *   regular, 1.0569 and 1.0613 bold (batch 45 item 8b).  0.985 and 1.058 are
	 *   the centre of the three.
	 *
	 *   Tahoma -> Arimo (no factor before this batch):
	 *
	 *     document 6380   regular      0.9693 (n=506, 8pt), 0.9815 (n=72, 10pt)
	 *                     bold         1.0693 (n=573, 8pt), 1.0662 (n=6, 18pt),
	 *                                  1.0220 (n=19, 11pt)
	 *     document 14776  regular      0.9820 (n=59, 10pt)
	 *                     bold         1.0239 (n=11, 10pt)
	 *
	 *   and font to font over English letter frequencies, tahoma.ttf against
	 *   Arimo-Regular 0.9941 and tahomabd.ttf against Arimo-Bold 1.0598 (batch 45
	 *   item 8).  Only the bold gets a row: the regular's 0.6 per cent font to font
	 *   is inside what this table does not try to correct, and the documents' 2 to 3
	 *   per cent is their own letter mix rather than the face.  1.06 is the centre
	 *   of the bold readings, which the largest sample (573 lines) puts at 1.0693. */

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
	 * @since 17.2.0
	 */
	public static double factorFor(String documentFontName, String physicalFontName) {
		return factorFor(documentFontName, physicalFontName, false, false);
	}

	/**
	 * The measured factor for this document font, the physical font docx4j mapped it to
	 * <b>and the face the run is set in</b>, or 1 where there is none.
	 *
	 * <p>A family's weights are not one another's width, and one document font can be
	 * drawn in two substitutes at once, so the answer depends on both.  Word's Tahoma
	 * Bold is 6 per cent wider than the Arimo Bold which stands in for it, where its
	 * regular is within 0.6 per cent of Arimo's - a single row for the family would
	 * correct one face by breaking the other.  And Cambria's Latin goes to Caladea while
	 * its Greek goes to P052, which want corrections in opposite directions.</p>
	 *
	 * <p>The rows for the document font are filtered to those whose substitute family the
	 * physical font really is, and the closest face wins: the run's own weight and style,
	 * else its weight alone, else the regular row.  So a family which has only a bold row
	 * measured leaves its regular uncorrected, and a bold italic takes the bold row until
	 * someone measures the bold italic.</p>
	 *
	 * @param bold   whether the run's effective properties make it bold
	 * @param italic whether they make it italic
	 * @since 17.2.0
	 */
	public static double factorFor(String documentFontName, String physicalFontName,
			boolean bold, boolean italic) {

		if (documentFontName == null || physicalFontName == null) return 1;
		java.util.List<FontSubstitutionTable.WidthFactor> rows =
				FontSubstitutionTable.widthFactors().get(key(documentFontName));
		if (rows == null || rows.isEmpty()) return 1;
		String physical = key(PhysicalFonts.stripSuffixes(physicalFontName));
		FontSubstitutionTable.WidthFactor best = null;
		int bestRank = -1;
		for (FontSubstitutionTable.WidthFactor f : rows) {
			if (!physical.startsWith(key(f.getSubstituteFamily()))) continue;
			int rank = rank(f, bold, italic);
			if (rank > bestRank) {
				best = f;
				bestRank = rank;
			}
		}
		return best == null ? 1 : best.getFactor();
	}

	/** How well a row's face fits the run's: the run's own face 2, its weight alone 1,
	 *  the regular row 0, anything else not at all. */
	private static int rank(FontSubstitutionTable.WidthFactor f, boolean bold, boolean italic) {
		if (f.isBold() == bold && f.isItalic() == italic) return 2;
		if (f.isBold() == bold && !f.isItalic()) return 1;
		if (!f.isBold() && !f.isItalic()) return 0;
		return -1;
	}

	/** Whether this document font has an entry at all, whatever it is mapped to and
	 *  whatever face the run is in.
	 *  @since 17.2.0 */
	public static boolean hasFactor(String documentFontName) {
		return documentFontName != null
				&& FontSubstitutionTable.widthFactors().containsKey(key(documentFontName));
	}

	private static String key(String name) {
		return name.trim().toLowerCase(Locale.ENGLISH);
	}
}
