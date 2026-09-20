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
import org.junit.Assume;
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

	/**
	 * Word's single line height is a whole number of 1/600 inch units - it lays a page out
	 * at 600 dpi - and 17.1.0 rounds to that grid.  What w:spacing then makes of it is
	 * <b>not</b> rounded again: Word's own pitch alternates between the two units either
	 * side of the exact value (Liberation Serif 12pt at w:line="276" is 132.25 units, and
	 * the line-auto golden's four pitches are 15.96 / 15.84 / 15.87 / 15.84), so it is the
	 * accumulated position Word rounds, not the pitch.  Values below are from the
	 * line-auto and line-exact-atleast goldens.
	 */
	@Test
	public void wordDeviceGrid() throws Exception {
		// off by default (it moved page breaks on the corpora); see WordLineMetrics.DEVICE_GRID
		System.setProperty(WordLineMetrics.DEVICE_GRID, "true");
		try {
		assertEquals(0.12, WordLineMetrics.GRID_PT, 1e-12);
		// every single-spacing pitch Word paints is a whole number of grid units
		for (double pt : new double[] { 13.80, 13.44, 11.52, 11.64 }) {
			assertEquals(pt, WordLineMetrics.onGrid(pt), 1e-9);
		}
		// half a unit goes to the even one: 172.5 -> 172 (20.64), which is Word's
		assertEquals(20.64, WordLineMetrics.onGrid(20.70), 1e-9);

		ObjectFactory f = new ObjectFactory();
		PPrBase.Spacing sp = f.createPPrBaseSpacing();

		// single spacing, exact -> Word (grid units).  Word paints all four exactly.
		// Liberation Serif 12: 13.7988 -> 13.80 (115); Carlito 11: 13.4277 -> 13.44 (112);
		// Liberation Sans 10: 11.4990 -> 11.52 (96); DejaVu Sans 10: 11.6406 -> 11.64 (97)
		assertEquals(13.80, lineHeight("Liberation Serif", 12, null), 1e-9);
		assertEquals(13.44, lineHeight("Carlito", 11, null), 1e-9);
		assertEquals(11.52, lineHeight("Liberation Sans", 10, null), 1e-9);
		assertEquals(11.64, lineHeight("DejaVu Sans", 10, null), 1e-9);
		assertEquals(13.80, lineHeight("Liberation Serif", 12, auto(sp, 240)), 1e-9);

		// the multiple is taken from the rounded single and left there.  Word's mean pitch
		// over the golden's paragraphs is in brackets
		assertEquals(14.8925, lineHeight("Liberation Serif", 12, auto(sp, 259)), 1e-9);   // 14.880
		assertEquals(15.87, lineHeight("Liberation Serif", 12, auto(sp, 276)), 1e-9);     // 15.8775
		assertEquals(20.70, lineHeight("Liberation Serif", 12, auto(sp, 360)), 1e-9);     // 20.680
		assertEquals(27.60, lineHeight("Liberation Serif", 12, auto(sp, 480)), 1e-9);     // 27.607
		assertEquals(15.456, lineHeight("Carlito", 11, auto(sp, 276)), 1e-9);             // 15.450
		assertEquals(26.88, lineHeight("Carlito", 11, auto(sp, 480)), 1e-9);              // 26.880
		assertEquals(13.248, lineHeight("Liberation Sans", 10, auto(sp, 276)), 1e-9);     // 13.208
		assertEquals(13.386, lineHeight("DejaVu Sans", 10, auto(sp, 276)), 1e-9);         // 13.392

		// exact and atLeast are the w:line value itself, ungridded
		assertEquals(9.00, lineHeight("Liberation Serif", 12, rule(sp, 180, STLineSpacingRule.EXACT)), 1e-9);
		assertEquals(24.00, lineHeight("Liberation Serif", 12, rule(sp, 480, STLineSpacingRule.EXACT)), 1e-9);
		assertEquals(13.80, lineHeight("Liberation Serif", 12, rule(sp, 120, STLineSpacingRule.AT_LEAST)), 1e-9);
		assertEquals(20.00, lineHeight("Liberation Serif", 12, rule(sp, 400, STLineSpacingRule.AT_LEAST)), 1e-9);
		} finally {
			System.clearProperty(WordLineMetrics.DEVICE_GRID);
		}
	}

	/** The shipped default is the 17.0.5 arithmetic, ungridded. */
	@Test
	public void theGridIsOffByDefault() throws Exception {
		assertEquals(13.4277, lineHeight("Carlito", 11, null), 1e-4);
		assertEquals(13.4277, WordLineMetrics.onGrid(13.4277), 1e-9);
		assertEquals(13.7988, lineHeight("Liberation Serif", 12, null), 1e-4);
	}

	private static double lineHeight(String documentFont, double sizePt, PPrBase.Spacing sp) {
		return WordLineMetrics.lineHeightPt(documentFont, null, sizePt, sp);
	}

	private static PPrBase.Spacing auto(PPrBase.Spacing sp, int line) {
		return rule(sp, line, STLineSpacingRule.AUTO);
	}

	private static PPrBase.Spacing rule(PPrBase.Spacing sp, int line, STLineSpacingRule r) {
		sp.setLine(BigInteger.valueOf(line));
		sp.setLineRule(r);
		return sp;
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

	/**
	 * An East Asian font's single line is 1.3 x its usWin box, and takes no external
	 * leading at all.
	 *
	 * <p>Measured on the fonts-cjk-linebox golden (CR-001 batch 43): the same Latin
	 * sentence at 10pt with w:spacing w:line="240" w:lineRule="auto", one paragraph per
	 * face, and Word's baseline pitch read off the PDF. The table's seventh field carries
	 * the flag, put there from OS/2 ulCodePageRange1 bits 17-21 by
	 * etc/GenWordLineMetricsEastAsian.</p>
	 */
	@Test
	public void eastAsianLineBox() {
		// ms gothic=256;220;36;220;-36;0;1 -> usWin box exactly 1.0; Word paints 13.0 at 10pt
		WordLineMetrics.Metrics msGothic = WordLineMetrics.get("MS Gothic", null);
		assertTrue("flagged East Asian", msGothic.eastAsian);
		assertEquals(1.0, msGothic.winAscent + msGothic.winDescent, 1e-9);
		assertEquals(1.3000, msGothic.lineHeightFactor(), 1e-4);
		assertEquals(13.00, msGothic.lineHeightFactor() * 10, 0.05);

		// simsun=256;220;36;220;-36;36;1 -> its own external leading (36/256 = 0.1406) is
		// NOT applied: Word paints 13.0, the same as MS Gothic
		WordLineMetrics.Metrics simSun = WordLineMetrics.get("SimSun", null);
		assertEquals(36 / 256.0, simSun.externalLeading, 1e-9);
		assertEquals(13.00, simSun.lineHeightFactor() * 10, 0.05);

		// yu gothic=2048;2017;619;1802;-455;1024;1 -> usWin 1.2871, x 1.3 = 1.6732;
		// Word paints 16.8.  The hhea+gap reading (1802+455+1024)/2048 = 1.6021 does not fit
		WordLineMetrics.Metrics yuGothic = WordLineMetrics.get("Yu Gothic", null);
		assertEquals(1.2871, yuGothic.winAscent + yuGothic.winDescent, 1e-4);
		assertEquals(1.6732, yuGothic.lineHeightFactor(), 1e-4);
		assertEquals(16.8, yuGothic.lineHeightFactor() * 10, 0.13);

		// malgun gothic and microsoft jhenghei both 2048;...;495 -> 1.3301, x 1.3 = 1.7291;
		// Word paints 17.3 for each
		for (String face : new String[] { "Malgun Gothic", "Microsoft JhengHei" }) {
			WordLineMetrics.Metrics m = WordLineMetrics.get(face, null);
			assertTrue(face + " flagged East Asian", m.eastAsian);
			assertEquals(face, 1.3301, m.winAscent + m.winDescent, 1e-4);
			assertEquals(face, 17.3, m.lineHeightFactor() * 10, 0.05);
		}

		// the control: Calibri is not East Asian and takes no factor - Word paints 12.2
		WordLineMetrics.Metrics calibri = WordLineMetrics.get("Calibri", null);
		assertFalse("Calibri is not East Asian", calibri.eastAsian);
		assertEquals(12.2, calibri.lineHeightFactor() * 10, 0.05);

		// and w:line="276" multiplies the East Asian line like any other: Yu Gothic 19.2
		PPrBase.Spacing auto276 = new ObjectFactory().createPPrBaseSpacing();
		auto276.setLine(BigInteger.valueOf(276));
		auto276.setLineRule(STLineSpacingRule.AUTO);
		assertEquals(19.2, WordLineMetrics.lineHeightPt("Yu Gothic", null, 10, auto276), 0.15);
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
		assertEquals(2401 / 2048.0 * 18, WordLineMetrics.get("Cambria", null).lineHeightFactor() * 18, 1e-9);
		assertEquals(2401 / 2048.0 * 18, WordLineMetrics.lineHeightPt("Cambria", null, 18, null), 1e-9);
		assertEquals("21.103pt", WordLineMetrics.lineHeightPtString("Cambria", null, 18, null));
	}

	/**
	 * The built-in aliases: families Word draws in another family's metrics.  Helvetica
	 * and Helvetica Neue have been here since 17.1.0; Helv, ArialMT and TimesNewRomanPSMT
	 * are the PostScript and legacy names of the same families, which documents pick up
	 * wherever a PDF or a PostScript driver has been round the text.  They were answering
	 * only by accident until 17.2.0 - through the per-JVM alias map, i.e. only when some
	 * other document in the same process had registered them - and two corpus documents at
	 * 1.0000 line parity and page-exact were measuring Helv on Arial's line box that way.
	 *
	 * @since 17.2.0
	 */
	@Test
	public void builtInAliases() {
		double arial = WordLineMetrics.get("Arial", null).lineHeightFactor();
		double times = WordLineMetrics.get("Times New Roman", null).lineHeightFactor();

		for (String name : new String[] { "Helvetica", "Helvetica Neue", "HelveticaNeue",
				"Helv", "ArialMT", "arialmt", "HELV" }) {
			assertTrue(name + " should answer", WordLineMetrics.hasTableEntry(name));
			assertEquals(name + " takes Arial's line box", arial,
					WordLineMetrics.get(name, null).lineHeightFactor(), 1e-9);
		}
		for (String name : new String[] { "TimesNewRomanPSMT", "timesnewromanpsmt" }) {
			assertTrue(name + " should answer", WordLineMetrics.hasTableEntry(name));
			assertEquals(name + " takes Times New Roman's line box", times,
					WordLineMetrics.get(name, null).lineHeightFactor(), 1e-9);
		}
		// and nothing else joined them
		assertFalse(WordLineMetrics.hasTableEntry("Arial-BoldMT"));
		assertFalse(WordLineMetrics.hasTableEntry("Myriad Pro"));
	}

	/**
	 * A font whose OS/2 fsSelection sets USE_TYPO_METRICS is laid out by Word on its typo
	 * box, not its usWin box.  Aptos, measured from Word's own PDF: 13.45pt at 11pt, its
	 * typo box (1923 + 577 = 2500 of 2048); the usWin box (2068 + 563) would give 14.13.
	 * Calibri does not set the flag and keeps the usWin rule.
	 */
	@Test
	public void aUseTypoMetricsFamilyTakesItsTypoBox() {
		Assume.assumeTrue("no metrics for Aptos", WordLineMetrics.hasTableEntry("Aptos"));
		WordLineMetrics.Metrics aptos = WordLineMetrics.get("Aptos", null);
		assertTrue("the table's fields 8-10 flag the family", aptos.typoMetrics);
		assertEquals(13.43, aptos.lineHeightFactor() * 11, 0.01);       // Word 13.45
		assertEquals(1923.0 / 2048, aptos.winAscent, 1e-9);
		assertEquals(577.0 / 2048, aptos.winDescent, 1e-9);
		assertEquals(0.0, aptos.externalLeading, 1e-9);
		assertEquals("Aptos Display is a face of the same family", 13.43,
				WordLineMetrics.get("Aptos Display", null).lineHeightFactor() * 11, 0.01);
		// Georgia Pro: typo 1549 + 444 = 1993 against usWin 1878 + 449 = 2327, 1.8pt a line
		if (WordLineMetrics.hasTableEntry("Georgia Pro")) {
			assertEquals(10.70, WordLineMetrics.get("Georgia Pro", null).lineHeightFactor() * 11, 0.01);
		}
		WordLineMetrics.Metrics calibri = WordLineMetrics.get("Calibri", null);
		assertFalse(calibri.typoMetrics);
		assertEquals(13.43, calibri.lineHeightFactor() * 11, 0.01);     // usWin 1950 + 550
	}

	/** The same rule read off a font file: the flag at OS/2 offset 62, bit 7. */
	@Test
	public void theReaderHonoursUseTypoMetrics() throws Exception {
		// unitsPerEm 2048; hhea 1923/-577/0; OS/2 v4 typo 1923/-577/0, usWin 2068/563
		WordLineMetrics.Metrics flagged = WordLineMetrics.readMetrics(
				new java.io.ByteArrayInputStream(sfnt(2048, 1923, -577, 0, 1923, -577, 0, 2068, 563, true)));
		assertTrue(flagged.typoMetrics);
		assertEquals(2500.0 / 2048, flagged.lineHeightFactor(), 1e-9);
		WordLineMetrics.Metrics plain = WordLineMetrics.readMetrics(
				new java.io.ByteArrayInputStream(sfnt(2048, 1923, -577, 0, 1923, -577, 0, 2068, 563, false)));
		assertFalse(plain.typoMetrics);
		assertEquals("usWin box, no external leading (hhea is smaller)", 2631.0 / 2048,
				plain.lineHeightFactor(), 1e-9);
		// the bit is honoured in an OS/2 table of version 3 too: DokChampa's shape, which
		// Word steps at 14.79pt at 11pt (its typo box 2754), not 21.31 (its usWin box 3967)
		WordLineMetrics.Metrics v3 = WordLineMetrics.readMetrics(
				new java.io.ByteArrayInputStream(sfnt(3, 2048, 2850, -1117, 0, 1999, -555, 200, 2850, 1117, true)));
		assertTrue(v3.typoMetrics);
		assertEquals(2754.0 / 2048, v3.lineHeightFactor(), 1e-9);
		// a typo line gap counts, as hhea's does for a usWin font
		WordLineMetrics.Metrics gap = WordLineMetrics.readMetrics(
				new java.io.ByteArrayInputStream(sfnt(2048, 1520, -532, 410, 1520, -532, 410, 1802, 539, true)));
		assertEquals("Bierstadt's shape", 2462.0 / 2048, gap.lineHeightFactor(), 1e-9);
	}

	/** A minimal sfnt: head, hhea and OS/2 (version 4) tables, nothing else. */
	private static byte[] sfnt(int upem, int hheaAsc, int hheaDesc, int hheaGap, int typoAsc, int typoDesc,
			int typoGap, int winAsc, int winDesc, boolean useTypoMetrics) {
		return sfnt(4, upem, hheaAsc, hheaDesc, hheaGap, typoAsc, typoDesc, typoGap, winAsc, winDesc, useTypoMetrics);
	}

	private static byte[] sfnt(int os2Version, int upem, int hheaAsc, int hheaDesc, int hheaGap, int typoAsc,
			int typoDesc, int typoGap, int winAsc, int winDesc, boolean useTypoMetrics) {
		java.nio.ByteBuffer head = java.nio.ByteBuffer.allocate(54);
		head.putShort(18, (short) upem);
		java.nio.ByteBuffer hhea = java.nio.ByteBuffer.allocate(36);
		hhea.putShort(4, (short) hheaAsc); hhea.putShort(6, (short) hheaDesc); hhea.putShort(8, (short) hheaGap);
		java.nio.ByteBuffer os2 = java.nio.ByteBuffer.allocate(96);
		os2.putShort(0, (short) os2Version);
		os2.putShort(62, (short) (useTypoMetrics ? 0x80 : 0));
		os2.putShort(68, (short) typoAsc); os2.putShort(70, (short) typoDesc); os2.putShort(72, (short) typoGap);
		os2.putShort(74, (short) winAsc); os2.putShort(76, (short) winDesc);
		byte[][] tables = { head.array(), hhea.array(), os2.array() };
		String[] tags = { "head", "hhea", "OS/2" };
		int dir = 12 + 16 * tables.length;
		int total = dir; for (byte[] t : tables) total += t.length;
		java.nio.ByteBuffer out = java.nio.ByteBuffer.allocate(total);
		out.putInt(0x00010000); out.putShort((short) tables.length); out.putShort((short) 0); out.putShort((short) 0); out.putShort((short) 0);
		int off = dir;
		for (int i = 0; i < tables.length; i++) {
			out.put(tags[i].getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
			out.putInt(0); out.putInt(off); out.putInt(tables[i].length);
			off += tables[i].length;
		}
		for (byte[] t : tables) out.put(t);
		return out.array();
	}
}
