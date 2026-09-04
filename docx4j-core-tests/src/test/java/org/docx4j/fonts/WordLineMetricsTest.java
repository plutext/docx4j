package org.docx4j.fonts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.InputStream;
import java.math.BigInteger;

import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.STLineSpacingRule;
import org.junit.Test;

/**
 * Word's line height from font metrics; expected values were measured from
 * Word 365 output (CR-001 harness, line-auto and line-exact-atleast probes).
 */
public class WordLineMetricsTest {

	private static WordLineMetrics.Metrics liberationSerif() throws Exception {
		InputStream is = WordLineMetricsTest.class.getResourceAsStream("/fonts/LiberationSerif-Regular.ttf");
		assertNotNull("LiberationSerif-Regular.ttf on the test classpath (docx4j-export-fo-fonts-liberation)", is);
		try {
			return WordLineMetrics.readMetrics(is);
		} finally {
			is.close();
		}
	}

	@Test
	public void liberationSerifMetrics() throws Exception {
		WordLineMetrics.Metrics m = liberationSerif();
		// usWinAscent 1825, usWinDescent 443, hhea lineGap 87, upem 2048
		assertEquals(1825.0 / 2048, m.winAscent, 1e-9);
		assertEquals(443.0 / 2048, m.winDescent, 1e-9);
		assertEquals(87.0 / 2048, m.externalLeading, 1e-9);
		// Word: 13.80pt at 12pt
		assertEquals(13.80, m.lineHeightFactor() * 12, 0.01);
	}

	@Test
	public void spacingRules() throws Exception {
		double single = liberationSerif().lineHeightFactor() * 12;
		ObjectFactory f = new ObjectFactory();
		PPrBase.Spacing sp = f.createPPrBaseSpacing();

		sp.setLine(BigInteger.valueOf(360)); sp.setLineRule(STLineSpacingRule.AUTO);
		assertEquals(20.70, apply(single, sp), 0.01);          // Word measured 20.64 (device rounding)

		sp.setLine(BigInteger.valueOf(180)); sp.setLineRule(STLineSpacingRule.EXACT);
		assertEquals(9.0, apply(single, sp), 1e-9);

		sp.setLine(BigInteger.valueOf(120)); sp.setLineRule(STLineSpacingRule.AT_LEAST);
		assertEquals(single, apply(single, sp), 1e-9);         // natural wins

		sp.setLine(BigInteger.valueOf(400)); sp.setLineRule(STLineSpacingRule.AT_LEAST);
		assertEquals(20.0, apply(single, sp), 1e-9);

		sp.setLine(null); sp.setLineRule(null);
		assertEquals(single, apply(single, sp), 1e-9);
	}

	/** Same arithmetic as WordLineMetrics.lineHeightPt, but from a known single value. */
	private static double apply(double single, PPrBase.Spacing sp) {
		if (sp.getLine() == null) return single;
		double line = sp.getLine().doubleValue();
		switch (sp.getLineRule()) {
		case EXACT: return line / 20;
		case AT_LEAST: return Math.max(single, line / 20);
		default: return single * line / 240;
		}
	}

	@Test
	public void formatting() {
		assertEquals("13.8pt", WordLineMetrics.format(13.8));
		assertEquals("13.801pt", WordLineMetrics.format(13.8006));
		assertEquals("12pt", WordLineMetrics.format(12.0));
	}

	/** word-line-metrics.properties: Word's vertical metrics of Microsoft fonts, by
	 *  document font name, for when a substitute renders them (CR-001 §6.10). */
	@Test
	public void documentFontTable() {
		assertTrue(WordLineMetrics.hasTableEntry("Cambria"));
		assertTrue("case-insensitive", WordLineMetrics.hasTableEntry("calibri"));
		assertFalse(WordLineMetrics.hasTableEntry("No Such Font"));

		// cambria=2048;1946;455;1946;-455;0 -> (1946+455)/2048, no external leading
		WordLineMetrics.Metrics cambria = WordLineMetrics.get("Cambria", null);
		assertFalse(cambria.fallback);
		assertEquals(1946 / 2048.0, cambria.winAscent, 1e-9);
		assertEquals(455 / 2048.0, cambria.winDescent, 1e-9);
		assertEquals(0, cambria.externalLeading, 1e-9);
		assertEquals(1.17236, cambria.lineHeightFactor(), 1e-4);

		// symbol=2048;2059;450;2059;-450;0
		assertEquals(1.22510, WordLineMetrics.get("Symbol", null).lineHeightFactor(), 1e-4);
		// consolas=2048;1884;514;1521;-527;350: hhea sums to the win height, so no leading
		assertEquals(1.17090, WordLineMetrics.get("Consolas", null).lineHeightFactor(), 1e-4);
		// times new roman=2048;1825;443;1825;-443;87: the 87 of lineGap is external leading
		WordLineMetrics.Metrics tnr = WordLineMetrics.get("Times New Roman", null);
		assertEquals(87 / 2048.0, tnr.externalLeading, 1e-9);

		// unknown document font, no physical font: the fallback
		assertTrue(WordLineMetrics.get("No Such Font", null).fallback);
		assertTrue(WordLineMetrics.get(null, null).fallback);

		// an 18pt Cambria heading at single spacing: 21.10pt (Caladea's own 1.300 would give 23.4)
		assertEquals(2401 / 2048.0 * 18, WordLineMetrics.lineHeightPt("Cambria", null, 18, null), 1e-9);
		assertEquals("21.103pt", WordLineMetrics.lineHeightPtString("Cambria", null, 18, null));
	}
}
