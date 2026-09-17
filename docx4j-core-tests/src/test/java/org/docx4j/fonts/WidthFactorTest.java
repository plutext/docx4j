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
import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The measured width factors (CR-001 batch 42 item 3): Calibri Light is Calibri's design
 * at 0.987 of Carlito's advances, and {@link RunFontSelector} carries that the way it
 * carries Word's own character scaling, as a letter space over the run's characters.
 *
 * @since 17.1.1
 */
public class WidthFactorTest {

	private static final String SENTENCE =
			"The quick brown fox jumps over the lazy dog while the farmer watches from the gate.";

	private static final double CALIBRI_LIGHT = 0.987;

	@BeforeClass
	public static void discoverFonts() throws Exception {
		WordprocessingMLPackage.createPackage().getFontMapper();
	}

	// ------------------------------------------------------------------ the table

	@Test
	public void theTableIsKeyedOnTheDocumentFont() {
		assertEquals(CALIBRI_LIGHT, WidthFactors.factorFor("Calibri Light", "Carlito Regular"), 0.0);
		assertEquals("case-insensitive, as font names are",
				CALIBRI_LIGHT, WidthFactors.factorFor("calibri light", "carlito regular"), 0.0);
		assertTrue(WidthFactors.hasFactor("Calibri Light"));
	}

	/** The physical name reaches the lookup with whichever of the FO layer's suffixes
	 *  have been stacked on it; the factor belongs to the document font regardless. */
	@Test
	public void theSubstitutesSuffixesAreStripped() {
		assertEquals(CALIBRI_LIGHT,
				WidthFactors.factorFor("Calibri Light", "Carlito Regular+nobold"), 0.0);
		assertEquals(CALIBRI_LIGHT,
				WidthFactors.factorFor("Calibri Light", "Carlito Regular+nobold+noliga"), 0.0);
		assertEquals(CALIBRI_LIGHT,
				WidthFactors.factorFor("Calibri Light", "Carlito Regular+kern"), 0.0);
	}

	/** Any Carlito face: the bold of a no-bold family is the regular file anyway. */
	@Test
	public void anyFaceOfTheMeasuredFamilyCounts() {
		assertEquals(CALIBRI_LIGHT, WidthFactors.factorFor("Calibri Light", "Carlito Italic"), 0.0);
		assertEquals(CALIBRI_LIGHT, WidthFactors.factorFor("Calibri Light", "Carlito Bold"), 0.0);
	}

	/** A box without the crosextra clones puts Calibri Light on a face whose advances are
	 *  not Calibri's at all, and 0.987 of them would mean nothing. */
	@Test
	public void aDifferentSubstituteGetsNoFactor() {
		assertEquals(1, WidthFactors.factorFor("Calibri Light", "Liberation Sans"), 0.0);
		assertEquals(1, WidthFactors.factorFor("Calibri Light", "Arimo Regular"), 0.0);
	}

	@Test
	public void nothingElseHasOne() {
		assertEquals(1, WidthFactors.factorFor("Calibri", "Carlito Regular"), 0.0);
		// Cambria has one since 17.1.1 (see cambriaIsWiderThanCaladea); Caladea is its
		// substitute by design, not by metric
		assertEquals(1, WidthFactors.factorFor("Cambria Math", "Caladea Regular"), 0.0);
		assertEquals(1, WidthFactors.factorFor(null, "Carlito Regular"), 0.0);
		assertEquals(1, WidthFactors.factorFor("Calibri Light", null), 0.0);
		org.junit.Assert.assertFalse(WidthFactors.hasFactor("Calibri"));
	}

	// ------------------------------------------------------- the altName chain (Meiryo)

	private static final double MEIRYO = 1.21;

	/**
	 * Meiryo's Latin is 1.21 of Carlito's, measured on a corpus document Word drew in
	 * Meiryo (its runs name a corporate face whose {@code w:altName} is Meiryo).  Read out
	 * of the two PDFs' own /Widths and weighted by the 3785 characters Word set in that
	 * face, leaving out the 1359 dots of its tab leaders, the ratio is 1.2107; unweighted
	 * over the 60 letters and digits the two fonts share it is 1.2144; and the one line of
	 * the document each side draws whole in one face measures 1.2051.
	 */
	@Test
	public void meiryoIsKeyedLikeAnyOtherDocumentFont() {
		assertEquals(MEIRYO, WidthFactors.factorFor("Meiryo", "Carlito Regular"), 0.0);
		assertTrue(WidthFactors.hasFactor("Meiryo"));
		// Meiryo UI is a narrower family of its own, and the vertical form another name
		assertEquals(1, WidthFactors.factorFor("Meiryo UI", "Carlito Regular"), 0.0);
		assertEquals(1, WidthFactors.factorFor("@Meiryo UI", "Carlito Regular"), 0.0);
	}

	/**
	 * The lookup follows the altName chain: a document font Word could not find, whose
	 * {@code w:altName} says which font Word used instead, takes that font's factor.  The
	 * chain is the one the line box already follows, so a row does not have to be written
	 * again for every corporate face which names the same alternate.
	 */
	@Test
	public void theFactorFollowsTheAltNameChain() throws Exception {
		Mapper m = new IdentityPlusMapper();
		assertEquals("no row of its own, and no alias yet",
				1, m.widthFactorFor("Docx4j Probe Corporate", "Carlito Regular"), 0.0);

		m.registerLineMetricsAlias("Docx4j Probe Corporate", "Meiryo");
		assertEquals("the altName's factor, through the chain",
				MEIRYO, m.widthFactorFor("Docx4j Probe Corporate", "Carlito Regular"), 0.0);
		assertEquals("and with the FO layer's suffixes on the physical name",
				MEIRYO, m.widthFactorFor("Docx4j Probe Corporate", "Carlito Regular+noliga"), 0.0);
	}

	/**
	 * It must not fire where the machine has the font Word drew: then the run is set in
	 * that font, and it is not the wrong width - it is the right one.  The substitute test
	 * in {@link WidthFactors#factorFor} is what does this.
	 */
	@Test
	public void noFactorWhereTheAliasedFontIsInstalled() throws Exception {
		assertEquals("Meiryo itself", 1, WidthFactors.factorFor("Meiryo", "Meiryo"), 0.0);
		assertEquals("or any face of it", 1, WidthFactors.factorFor("Meiryo", "Meiryo Bold"), 0.0);
		Mapper m = new IdentityPlusMapper();
		m.registerLineMetricsAlias("Docx4j Probe Corporate", "Meiryo");
		assertEquals(1, m.widthFactorFor("Docx4j Probe Corporate", "Meiryo"), 0.0);
		// nor where the substitute is some other face this machine happened to choose
		assertEquals(1, m.widthFactorFor("Docx4j Probe Corporate", "Liberation Sans"), 0.0);
	}

	/** A document font with a row of its own is not sent down the chain. */
	@Test
	public void aRowOfItsOwnWins() throws Exception {
		Mapper m = new IdentityPlusMapper();
		m.registerLineMetricsAlias("Calibri Light", "Meiryo");
		assertEquals(CALIBRI_LIGHT, m.widthFactorFor("Calibri Light", "Carlito Regular"), 0.0);
	}

	// ------------------------------------------------------------- Cambria -> Caladea

	private static final double CAMBRIA = 1.048;

	/**
	 * Caladea is Cambria's substitute by design, not by metric: it carries Cambria's own
	 * advance for n, l, x, X and i and is eight to eleven per cent narrower on the rest of
	 * the lower case (e 441 against 488, o 480 against 531, s 392 against 430), and where
	 * Cambria's digits are tabular - 554 every one - Caladea's are proportional, its 1
	 * measuring 362.  Over English letter frequencies the pair measures 1.0494, and over
	 * the 14792 Cambria glyphs of one corpus document 1.0497.
	 */
	@Test
	public void cambriaIsWiderThanCaladea() {
		assertEquals(CAMBRIA, WidthFactors.factorFor("Cambria", "Caladea Regular"), 0.0);
		assertEquals(CAMBRIA, WidthFactors.factorFor("Cambria", "Caladea Bold"), 0.0);
		assertEquals(CAMBRIA, WidthFactors.factorFor("cambria", "caladea regular+noliga"), 0.0);
		assertTrue(WidthFactors.hasFactor("Cambria"));
	}

	/** Not where the machine has Cambria itself, and not for another family of the name. */
	@Test
	public void noCambriaFactorWhereCambriaIsDrawn() {
		assertEquals(1, WidthFactors.factorFor("Cambria", "Cambria"), 0.0);
		assertEquals(1, WidthFactors.factorFor("Cambria", "Liberation Serif"), 0.0);
		assertEquals("Cambria Math is a family of its own",
				1, WidthFactors.factorFor("Cambria Math", "Caladea Regular"), 0.0);
	}

	// ------------------------------------------------------------------- per face

	/**
	 * A family's weights are not one another's width, so the factor is asked for the face
	 * the <b>run</b> is set in.  Measured as residuals on renders already carrying the
	 * regular row: Cambria's bold wants 0.989 of it and its bold italic 0.974, so 1.036
	 * and 1.021 (CR-001 batch 46 item 1).
	 */
	@Test
	public void cambriaBoldIsNotCambriaRegular() {
		assertEquals(CAMBRIA, WidthFactors.factorFor("Cambria", "Caladea Regular", false, false), 0.0);
		assertEquals(1.036, WidthFactors.factorFor("Cambria", "Caladea Regular", true, false), 0.0001);
		assertEquals(1.021, WidthFactors.factorFor("Cambria", "Caladea Regular", true, true), 0.0001);
		assertEquals("no italic row measured, so the regular's",
				CAMBRIA, WidthFactors.factorFor("Cambria", "Caladea Regular", false, true), 0.0);
		assertEquals("the two-argument call is the regular face",
				CAMBRIA, WidthFactors.factorFor("Cambria", "Caladea Regular"), 0.0);
	}

	/**
	 * One document font, two substitutes at once: Caladea has no Greek at all, so a
	 * Cambria document's Greek is drawn in P052 and the two want corrections in opposite
	 * directions - the Latin 4.8 per cent wider, the Greek 1.5 per cent narrower.
	 */
	@Test
	public void cambriaGreekTakesItsOwnSubstitutesRow() {
		assertEquals(0.985, WidthFactors.factorFor("Cambria", "P052 Roman", false, false), 0.0001);
		assertEquals(1.058, WidthFactors.factorFor("Cambria", "P052 Bold", true, false), 0.0001);
		assertEquals("the suffixes the FO layer stacks on are stripped first",
				0.985, WidthFactors.factorFor("Cambria", "P052 Roman+noliga", false, false), 0.0001);
		assertEquals("a bold italic falls back to the bold row where none is measured",
				1.058, WidthFactors.factorFor("Cambria", "P052 Bold", true, true), 0.0001);
	}

	/**
	 * Where only the bold is out, only the bold gets a row: Word's Tahoma Bold is 6 per
	 * cent wider than the Arimo Bold which stands in for it (tahomabd.ttf against
	 * Arimo-Bold 1.0598 over English letter frequencies, and 1.0693 over the 573 paired
	 * bold lines of one corpus document), where the regular is within 0.6 per cent of
	 * Arimo's and is left alone.
	 */
	@Test
	public void onlyTahomasBoldHasARow() {
		assertEquals(1.06, WidthFactors.factorFor("Tahoma", "Arimo Bold", true, false), 0.0001);
		assertEquals("the regular is inside what this table does not correct",
				1, WidthFactors.factorFor("Tahoma", "Arimo Regular", false, false), 0.0);
		assertEquals("and so is an italic, which takes the regular's absent row",
				1, WidthFactors.factorFor("Tahoma", "Arimo Italic", false, true), 0.0);
		assertEquals("a bold italic takes the bold row",
				1.06, WidthFactors.factorFor("Tahoma", "Arimo Bold Italic", true, true), 0.0001);
		assertEquals("not where the machine has Tahoma itself",
				1, WidthFactors.factorFor("Tahoma", "Tahoma", true, false), 0.0);
		assertTrue(WidthFactors.hasFactor("Tahoma"));
	}

	// ------------------------------------------------------------------ the selector

	/** The letter space the factor alone asks for, at 11pt over this sentence. */
	private static double expectedSpacing(double scale) {
		PhysicalFont carlito = PhysicalFonts.get("Carlito Regular");
		double width = TextMeasurer.widthPt(SENTENCE, carlito, 11);
		return width * (scale - 1) / SENTENCE.codePointCount(0, SENTENCE.length());
	}

	private static WordprocessingMLPackage calibriLightPackage(String extraRPr) throws Exception {
		Assume.assumeNotNull(PhysicalFonts.get("Carlito Regular"));
		return FontsTestSupport.packageWith(
				FontsTestSupport.styles("<w:rFonts w:ascii=\"Calibri Light\" w:hAnsi=\"Calibri Light\"/>", ""),
				null, null,
				FontsTestSupport.p("<w:sz w:val=\"22\"/>" + extraRPr, SENTENCE),
				"Calibri Light", "Carlito Regular");
	}

	/** Every fo:inline the selector produced for paragraph 0's first run. */
	private static java.util.List<Element> spans(WordprocessingMLPackage pkg) throws Exception {
		RunFontSelector rfs = FontsTestSupport.xslFoSelector(pkg);
		P p = (P) pkg.getMainDocumentPart().getContent().get(0);
		R r = (R) p.getContent().get(0);
		Text t = (Text) XmlUtils.unwrap(r.getContent().get(0));
		Object o = rfs.fontSelector(p.getPPr(), r.getRPr(), t);
		java.util.List<Element> out = new java.util.ArrayList<Element>();
		if (o instanceof DocumentFragment) {
			for (Node n = ((DocumentFragment) o).getFirstChild(); n != null; n = n.getNextSibling()) {
				if (n instanceof Element) out.add((Element) n);
			}
		}
		return out;
	}

	private static double letterSpacingPt(Element el) {
		String v = el.getAttribute("letter-spacing");
		assertTrue("no letter-spacing on " + XmlUtils.w3CDomNodeToString(el), v.endsWith("pt"));
		return Double.parseDouble(v.substring(0, v.length() - 2));
	}

	@Test
	public void calibriLightCarriesTheFactorAsALetterSpace() throws Exception {

		java.util.List<Element> spans = spans(calibriLightPackage(""));
		assertEquals(1, spans.size());
		Element span = spans.get(0);

		assertEquals(expectedSpacing(CALIBRI_LIGHT), letterSpacingPt(span), 0.001);
		assertTrue("it is negative: Calibri Light is narrower than Carlito",
				letterSpacingPt(span) < 0);
		assertEquals("the mark must not reach the FO", "",
				span.getAttribute("docx4j-width-factor"));
	}

	/** Word's own character scaling is the author's instruction and the factor is
	 *  docx4j's correction for the face it had to use, so the two multiply. */
	@Test
	public void theFactorAndCharacterScalingAreMultiplied() throws Exception {

		java.util.List<Element> spans = spans(calibriLightPackage("<w:w w:val=\"110\"/>"));
		assertEquals(1, spans.size());

		assertEquals(expectedSpacing(1.10 * CALIBRI_LIGHT), letterSpacingPt(spans.get(0)), 0.001);
		assertTrue("1.10 x 0.987 is over 1, so this one widens",
				letterSpacingPt(spans.get(0)) > 0);
	}

	/** The run's own w:spacing is on the ancestor inline the exporter writes, and
	 *  letter-spacing is inherited, so the span is marked for
	 *  WordLayoutFixups.combineLetterSpacing to add the two. */
	@Test
	public void theSpanIsMarkedSoTheRunsOwnSpacingIsAddedToIt() throws Exception {

		java.util.List<Element> spans = spans(calibriLightPackage(""));
		assertEquals("1", spans.get(0).getAttribute(RunFontSelector.HINT_SCALED_LETTER_SPACING));
	}

	@Test
	public void aRunOnADifferentSubstituteGetsNoLetterSpace() throws Exception {

		WordprocessingMLPackage pkg = FontsTestSupport.packageWith(
				FontsTestSupport.styles("<w:rFonts w:ascii=\"Calibri Light\" w:hAnsi=\"Calibri Light\"/>", ""),
				null, null,
				FontsTestSupport.p("<w:sz w:val=\"22\"/>", SENTENCE),
				"Calibri Light", FontsTestSupport.SANS);

		for (Element span : spans(pkg)) {
			assertEquals("no factor was measured against this face", "",
					span.getAttribute("letter-spacing"));
			assertEquals("", span.getAttribute("docx4j-width-factor"));
		}
	}

	@Test
	public void anotherFontIsUntouched() throws Exception {

		WordprocessingMLPackage pkg = FontsTestSupport.packageWith(
				FontsTestSupport.styles("<w:rFonts w:ascii=\"Calibri\" w:hAnsi=\"Calibri\"/>", ""),
				null, null,
				FontsTestSupport.p("<w:sz w:val=\"22\"/>", SENTENCE),
				"Calibri", "Carlito Regular");
		Assume.assumeNotNull(PhysicalFonts.get("Carlito Regular"));

		for (Element span : spans(pkg)) {
			assertEquals("Calibri's clone needs no correction", "",
					span.getAttribute("letter-spacing"));
		}
	}

	/** The line box follows the document font, not the substitute and not the factor. */
	@Test
	public void theLineBoxIsStillTheDocumentFonts() throws Exception {

		Element span = spans(calibriLightPackage("")).get(0);
		assertEquals(WordLineMetrics.lineHeightPtString("Calibri Light",
						PhysicalFonts.get("Carlito Regular"), 11, null),
				span.getAttribute("line-height"));
	}
}
