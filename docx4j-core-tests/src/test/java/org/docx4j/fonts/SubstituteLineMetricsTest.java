package org.docx4j.fonts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The line box of a document font docx4j has no metrics of its own for (CR-001
 * &#xa7;2.7).
 *
 * <p>Word takes usWinAscent, usWinDescent and lineGap from the font the document names,
 * installed or not; docx4j ships those for 512 Microsoft families.  For a font in
 * neither - Helvetica, which Windows itself substitutes with Arial - the line box came
 * from the <em>physical substitute's</em> OS/2 win metrics, and those are not the metrics
 * of the font it stands in for: Arimo's usWinAscent/Descent are 2136/797 over 2048 units,
 * a factor of 1.432, where Arial's are 1854/434 with a 67-unit external leading, 1.150.
 * Measured on a 9pt single-spaced Helvetica document: Word's line pitch is 10.34pt and
 * docx4j's was 12.89pt, +24.6% on every line, five Word pages against our six.</p>
 *
 * @since 17.0.6
 */
public class SubstituteLineMetricsTest {

	@BeforeClass
	public static void discoverFonts() throws Exception {
		WordprocessingMLPackage.createPackage().getFontMapper();
	}

	@Test
	public void helveticaTakesArialsLineMetrics() throws Exception {

		Assume.assumeTrue("no metrics for Arial", WordLineMetrics.hasTableEntry("Arial"));
		assertTrue("Helvetica must resolve to a table entry", WordLineMetrics.hasTableEntry("Helvetica"));
		assertTrue(WordLineMetrics.hasTableEntry("Helvetica Neue"));

		WordLineMetrics.Metrics arial = WordLineMetrics.get("Arial", null);
		WordLineMetrics.Metrics helvetica = WordLineMetrics.get("Helvetica", null);
		assertEquals(arial.lineHeightFactor(), helvetica.lineHeightFactor(), 0.0001);
	}

	/** Word's single-spacing factor for Arial is 1.150 (2355/2048), which is what a
	 *  Helvetica document must get - not Arimo's own 1.432. */
	@Test
	public void theFactorIsWordsAndNotTheSubstitutesOwn() throws Exception {

		Assume.assumeTrue("no metrics for Arial", WordLineMetrics.hasTableEntry("Arial"));
		assertEquals(1.1499, WordLineMetrics.get("Helvetica", null).lineHeightFactor(), 0.002);

		PhysicalFont arimo = PhysicalFonts.get("Arimo Regular");
		if (arimo == null) arimo = PhysicalFonts.get("Arimo");
		Assume.assumeNotNull(arimo);
		assertTrue("Arimo's own win metrics are much taller; that is the bug this is about",
				WordLineMetrics.get(arimo).lineHeightFactor() > 1.35);
		assertEquals("the document font decides, not the substitute", 1.1499,
				WordLineMetrics.get("Helvetica", arimo).lineHeightFactor(), 0.002);
	}

	/** A font in neither the table nor the alias list still falls back to the physical
	 *  font, which is all there is to go on. */
	@Test
	public void anUnknownFontStillUsesThePhysicalFont() throws Exception {
		PhysicalFont arimo = PhysicalFonts.get("Arimo Regular");
		if (arimo == null) arimo = PhysicalFonts.get("Arimo");
		Assume.assumeNotNull(arimo);
		assertEquals(WordLineMetrics.get(arimo).lineHeightFactor(),
				WordLineMetrics.get("No Such Font At All", arimo).lineHeightFactor(), 0.0001);
	}
}
