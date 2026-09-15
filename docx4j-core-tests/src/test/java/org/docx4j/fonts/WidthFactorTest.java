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
		assertEquals(1, WidthFactors.factorFor("Cambria", "Caladea Regular"), 0.0);
		assertEquals(1, WidthFactors.factorFor(null, "Carlito Regular"), 0.0);
		assertEquals(1, WidthFactors.factorFor("Calibri Light", null), 0.0);
		org.junit.Assert.assertFalse(WidthFactors.hasFactor("Calibri"));
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
