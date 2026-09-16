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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.docx4j.wml.PPrBase;
import org.docx4j.wml.STLineSpacingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The vertical metrics Word uses for line height, read straight from a font's
 * hhea and OS/2 tables.
 *
 * Word's "single" line spacing is not 1.2 x font size (XSL-FO's "normal") and
 * not the ascender/descender FOP picks for its own baseline placement; it is
 * GDI's TEXTMETRIC height plus external leading:
 * <pre>
 *   tmHeight          = usWinAscent + usWinDescent
 *   tmExternalLeading = max(0, (hhea.ascender - hhea.descender + hhea.lineGap) - tmHeight)
 *   single            = (tmHeight + tmExternalLeading) / unitsPerEm * size
 * </pre>
 * (Microsoft OpenType spec, "baseline-to-baseline distances"; LibreOffice does
 * the same, tdf#55469.)  Verified against Word 365 output for Liberation Serif
 * (13.80pt at 12pt), Carlito (13.44 at 11), Liberation Sans (11.52 at 10) and
 * DejaVu Sans (11.64 at 10).
 *
 * The other spacing rules then follow: "auto" multiplies single by line/240;
 * "exact" is line/20 pt regardless of font; "atLeast" is the larger of single
 * and line/20 pt.  {@link #lineHeightPt(PhysicalFont, double, PPrBase.Spacing)}
 * applies them.
 *
 * Metrics are parsed once per font file and cached.  If a font cannot be read
 * (Type 1, a broken file), the fallback factor is 1.2, i.e. FOP's "normal".
 *
 * @since 17.0.5
 */
public final class WordLineMetrics {

	private static final Logger log = LoggerFactory.getLogger(WordLineMetrics.class);

	/** Fallback when the font file cannot be read: XSL-FO's "normal". */
	public static final double FALLBACK_FACTOR = 1.2;

	/** Per-font metrics, as fractions of the font size. */
		public static final class Metrics {
		/** usWinAscent / unitsPerEm */
		public final double winAscent;
		/** usWinDescent / unitsPerEm */
		public final double winDescent;
		/** tmExternalLeading / unitsPerEm */
		public final double externalLeading;
		/** The ascender FOP uses for its content rectangle (its OpenFont.determineAscDesc choice), as a fraction. */
		public final double fopAscent;
		/** FOP's descender, as a positive fraction. */
		public final double fopDescent;
		/** true when the font could not be read and these are guesses. */
		public final boolean fallback;
		/** true when the font's OS/2 code-page bits call it East Asian: its line is
		 *  {@link #EAST_ASIAN_FACTOR} x the usWin box and takes no external leading.
		 *  @since 17.1.1 */
		public final boolean eastAsian;

		Metrics(double winAscent, double winDescent, double externalLeading, double fopAscent, double fopDescent, boolean fallback) {
			this(winAscent, winDescent, externalLeading, fopAscent, fopDescent, fallback, false);
		}

		/** @since 17.1.1 */
		Metrics(double winAscent, double winDescent, double externalLeading, double fopAscent,
				double fopDescent, boolean fallback, boolean eastAsian) {
			this.winAscent = winAscent;
			this.winDescent = winDescent;
			this.externalLeading = externalLeading;
			this.fopAscent = fopAscent;
			this.fopDescent = fopDescent;
			this.fallback = fallback;
			this.eastAsian = eastAsian;
		}

		/** Word's single line height as a multiple of the font size. */
		public double lineHeightFactor() {
			if (eastAsian) return EAST_ASIAN_FACTOR * (winAscent + winDescent);
			return winAscent + winDescent + externalLeading;
		}

		@Override
		public String toString() {
			return String.format("winAscent=%.4f winDescent=%.4f externalLeading=%.4f%s factor=%.4f",
					winAscent, winDescent, externalLeading, eastAsian ? " eastAsian" : "",
					lineHeightFactor());
		}
	}

	/**
	 * What Word multiplies an East Asian font's usWin box by for a single line: <b>1.3</b>,
	 * and it takes no external leading at all.
	 *
	 * <p>Measured on the {@code fonts-cjk-linebox} golden (CR-001 batch 43), five faces at
	 * 10pt with {@code w:spacing w:line="240" w:lineRule="auto"}, against the usWin box the
	 * table holds for each:</p>
	 *
	 * <pre>
	 *   face                 usWin box   x 1.3    Word paints
	 *   MS Gothic              1.0000    1.3000      13.0
	 *   SimSun                 1.0000    1.3000      13.0   (its own ext 0.1406 is not applied)
	 *   Yu Gothic              1.2871    1.6732      16.8
	 *   Malgun Gothic          1.3301    1.7291      17.3
	 *   Microsoft JhengHei     1.3301    1.7291      17.3
	 *   Calibri (the control)  1.2207    -           12.2   (no factor: not East Asian)
	 * </pre>
	 *
	 * <p>The hhea-plus-gap reading does not fit (Yu Gothic's is 1.602 against the 1.68
	 * measured), and {@code w:line="276"} multiplies this by 1.15 on top of it, as for any
	 * other font (Yu Gothic 19.2). Two of the eight faces the probe asks for were absent on
	 * the reference machine, and Word drew the substitute's box rather than the named
	 * font's - so the factor belongs to the face actually <b>drawn</b>, not to the name the
	 * document asks for.</p>
	 *
	 * <p>The extra 0.3 falls <b>below</b> the text: the baseline stays at usWinAscent, which
	 * is what {@link #wordBaselinePt} does for any font under "auto" spacing. Word's
	 * baseline within the taller line was not measured, and this is the choice that moves
	 * nothing that was already right.</p>
	 *
	 * @since 17.1.1
	 */
	public static final double EAST_ASIAN_FACTOR = 1.3;

		private static final Metrics FALLBACK = new Metrics(FALLBACK_FACTOR * 0.8, FALLBACK_FACTOR * 0.2, 0, FALLBACK_FACTOR * 0.8, FALLBACK_FACTOR * 0.2, true);

	private static final Map<String, Metrics> CACHE = new ConcurrentHashMap<>();

	private WordLineMetrics() {}

	/**
	 * Word's vertical metrics of the <i>document</i> font, when it is one of the
	 * Microsoft fonts in word-line-metrics.properties, combined with FOP's
	 * placement of the physical font that renders it; otherwise the physical
	 * font's own.  A metric-compatible substitute matches in advance widths, not
	 * always vertically (Caladea 1.300 for Cambria's 1.172, DejaVu Serif 1.164
	 * for Symbol's 1.225), and Word sizes the line from the document font.
	 * Measured (CR-001 §6.10).  Never null.
	 *
	 * @since 17.0.5
	 */
	public static Metrics get(String documentFont, PhysicalFont pf) {
		Metrics physical = get(pf);
		int[] t = documentFont == null ? null : lookup(documentFont);
		if (t == null) return physical;
		double upem = t[0];
		int winA = t[1], winD = t[2], hheaA = t[3], hheaD = t[4], gap = t[5];
		// GDI: tmExternalLeading = max(0, hhea ascender - hhea descender + lineGap - (usWinAscent + usWinDescent))
		int ext = Math.max(0, (hheaA - hheaD + gap) - (winA + winD));
		// the table's optional seventh field: the OS/2 code-page bits call this family
		// East Asian, so its line is EAST_ASIAN_FACTOR x the usWin box and takes no
		// external leading.  @since 17.1.1
		boolean ea = t.length > 6 && t[6] != 0;
		return new Metrics(winA / upem, winD / upem, ext / upem, physical.fopAscent, physical.fopDescent,
				false, ea);
	}

	/** Whether the table knows this document font - itself, or through a built-in alias
	 *  (Helvetica, and the PostScript names): the line box is the table's.  A document's
	 *  own alias is resolved before the name gets here; see {@link Mapper#lineMetricsFamily}. */
	public static boolean hasTableEntry(String documentFont) {
		return documentFont != null && lookup(documentFont) != null;
	}

	/**
	 * Whether this document font is a family the table lists (itself or a built-in alias):
	 * {@link Mapper#isKnownFamily} asks this.  It is the same question as
	 * {@link #hasTableEntry} now that a document's own aliases are per conversion
	 * (17.1.1); while they were a static map it was not, and asking hasTableEntry here
	 * made the family a document's altName had aliased to Calibri a "known" family for
	 * every later document in the JVM, which then skipped the Word-default pass and left
	 * the font unmapped (found by the CR-016 phase 4 gate: the same document mapped Vrinda
	 * to Carlito in a fresh JVM and to nothing after another document).  Kept as its own
	 * method because the two mean different things to a reader.
	 *
	 * @since 17.1.1
	 */
	/**
	 * Whether the table flags this family East Asian, so that its line is
	 * {@link #EAST_ASIAN_FACTOR} x its usWin box. False for a family the table does not
	 * know, which is the answer a caller wants: the question is only ever asked to decide
	 * whether to take <em>this</em> family's line box.
	 *
	 * @since 17.1.1
	 */
	public static boolean isEastAsianFamily(String documentFont) {
		int[] t = documentFont == null ? null : lookup(documentFont);
		return t != null && t.length > 6 && t[6] != 0;
	}

	public static boolean isTableFamily(String documentFont) {
		if (documentFont == null) return false;
		String key = documentFont.trim().toLowerCase(java.util.Locale.ROOT);
		if (TABLE.get().containsKey(key)) return true;
		String alias = ALIASES.get(key);
		return alias != null && TABLE.get().containsKey(alias);
	}

	private static int[] lookup(String documentFont) {
		String key = documentFont.trim().toLowerCase(java.util.Locale.ROOT);
		int[] t = TABLE.get().get(key);
		if (t != null) return t;
		String alias = ALIASES.get(key);
		return alias == null ? null : TABLE.get().get(alias);
	}

	/**
	 * Whether this family has line metrics of its own here - the table's, or a built-in
	 * alias to a family in the table.  A font which has them takes them, so
	 * {@link Mapper#registerLineMetricsAlias} declines to alias it.
	 *
	 * @since 17.1.1
	 */
	static boolean hasOwnLineMetrics(String documentFont) {
		if (documentFont == null) return false;
		String key = documentFont.trim().toLowerCase(java.util.Locale.ROOT);
		return TABLE.get().containsKey(key) || ALIASES.containsKey(key);
	}

	/**
	 * @deprecated since 17.1.1 this does nothing: a document's own alias is the
	 *             conversion's, not the JVM's - call
	 *             {@link Mapper#registerLineMetricsAlias} on the package's mapper.
	 *             The static map it wrote to was never cleared, so one document's
	 *             alternate font answered for every later document in the same process.
	 */
	@Deprecated
	public static void registerAlias(String documentFont, String substituteFont) {
		if (WARNED_REGISTER_ALIAS.compareAndSet(false, true)) {
			log.warn("WordLineMetrics.registerAlias does nothing since 17.1.1 (it was a JVM-wide map);"
					+ " call Mapper.registerLineMetricsAlias on the package's font mapper instead");
		}
	}

	private static final java.util.concurrent.atomic.AtomicBoolean WARNED_REGISTER_ALIAS =
			new java.util.concurrent.atomic.AtomicBoolean();

	/*
	 * An alias a *document* declares for itself, in w:altName (ECMA-376 17.8.3.1) or
	 * through Word's answer for a font it cannot find, used to be registered above, in a
	 * static map (registerAlias / DOCUMENT_ALIASES, 17.1.0).  It is per conversion since
	 * 17.1.1 - Mapper.registerLineMetricsAlias and Mapper.lineMetricsFamily.  Nothing
	 * downstream needs it: RunFontSelector resolves the alias while it still has the
	 * package, and writes the *resolved* family into its docx4j-font hint, so the FO
	 * exporter, WordLayoutFixups and the FOP fork's line layout manager - none of which
	 * can reach a Mapper - all name a family this table answers for directly.
	 */

	/**
	 * Document fonts Windows itself substitutes, whose line metrics Word therefore takes
	 * from the font it substitutes rather than from any table of its own.
	 *
	 * <p>Without this the line box came from the <em>physical substitute's</em> OS/2 win
	 * metrics, which are not the metrics of the font it stands in for: Arimo's
	 * usWinAscent/Descent are 2136/797 over 2048 units, a factor of 1.432, where Arial's
	 * are 1854/434 with a 67-unit external leading, 1.150 - the number every Arial
	 * document already gets, and the number Word uses for Helvetica.  Measured on a
	 * 9pt single-spaced Helvetica document: Word's line pitch is 10.34pt (1.149 em) and
	 * docx4j's was 12.89pt, +24.6% on every line, five Word pages against our six.</p>
	 *
	 * @since 17.1.0
	 */
	private static final Map<String, String> ALIASES;
	static {
		Map<String, String> m = new java.util.HashMap<>();
		m.put("helvetica", "arial");
		m.put("helvetica neue", "arial");
		m.put("helveticaneue", "arial");
		/* The PostScript and legacy names of the same three families, which documents
		 * carry wherever a PDF or a PostScript printer driver has been round the text:
		 * Word treats them as the family and gives them its line box.  They were reaching
		 * that answer only by accident until 17.1.1 - through the per-JVM alias map, i.e.
		 * only when some other document in the same process happened to have registered
		 * them - and two corpus documents at 1.0000 line parity and page-exact measured
		 * Helv on Arial's line box that way.  With the alias map now scoped to the
		 * conversion (Mapper.registerLineMetricsAlias) they have to be here to keep it.
		 * @since 17.1.1 */
		m.put("helv", "arial");
		m.put("arialmt", "arial");
		m.put("timesnewromanpsmt", "times new roman");
		ALIASES = java.util.Collections.unmodifiableMap(m);
	}

	private static final java.util.function.Supplier<Map<String, int[]>> TABLE = new java.util.function.Supplier<Map<String, int[]>>() {
		private volatile Map<String, int[]> table;
		public Map<String, int[]> get() {
			if (table == null) {
				Map<String, int[]> m = new java.util.HashMap<>();
				try (InputStream is = WordLineMetrics.class.getResourceAsStream("word-line-metrics.properties")) {
					if (is != null) {
						java.util.Properties props = new java.util.Properties();
						props.load(is);
						for (String name : props.stringPropertyNames()) {
							String[] v = props.getProperty(name).split(";");
							if (v.length < 6) continue;
							// six fields, plus the optional seventh: the East Asian flag
							// (etc/GenWordLineMetricsEastAsian).  @since 17.1.1
							int[] t = new int[Math.min(v.length, 7)];
							for (int i = 0; i < t.length; i++) t[i] = Integer.parseInt(v[i].trim());
							m.put(name.trim().toLowerCase(java.util.Locale.ROOT), t);
						}
					}
				} catch (Exception e) {
					log.warn("word-line-metrics.properties: " + e.getMessage());
				}
				table = m;
			}
			return table;
		}
	};

	/** Metrics for this physical font, or the fallback if its file cannot be read. Never null. */
	public static Metrics get(PhysicalFont pf) {
		if (pf == null || pf.getEmbeddedURI() == null) return FALLBACK;
		URI uri = pf.getEmbeddedURI();
		return CACHE.computeIfAbsent(uri.toString(), k -> {
			try (InputStream is = uri.toURL().openStream()) {
				Metrics m = read(is);
				if (log.isDebugEnabled()) log.debug(pf.getName() + ": " + m);
				return m;
			} catch (Exception e) {
				log.warn("Can't read vertical metrics of " + pf.getName() + " (" + uri + "): " + e.getMessage()
						+ "; using factor " + FALLBACK_FACTOR);
				return FALLBACK;
			}
		});
	}

	/**
	 * Word's layout grid: 1/600 inch, 0.12pt.
	 *
	 * <p>Word lays a page out in 600 dpi device units - its A4 page is 595.32 x 841.92pt,
	 * which is 4961 x 7016 of them - and a line height is a whole number of them.
	 * Measured on the CR-001 line-auto and line-exact-atleast goldens, where every line
	 * pitch Word paints is a multiple of 0.12pt:
	 *
	 * <pre>
	 *   font / size          exact single    Word     units
	 *   Liberation Serif 12    13.7988      13.80      115
	 *   Carlito 11             13.4277      13.44      112
	 *   Liberation Sans 10     11.4990      11.52       96
	 *   DejaVu Sans 10         11.6406      11.64       97
	 * </pre>
	 *
	 * and the multiple is taken from the rounded single and rounded again:
	 * Liberation Serif 12pt at {@code w:line="276"} is 115 units x 276/240 = 132.25,
	 * i.e. 132 units = 15.84pt, which is what Word paints (three consecutive pitches of
	 * 15.840 in the golden), where the unrounded 13.7988 x 1.15 = 15.869 is 0.03pt
	 * short every line.  Over the 20 paragraphs of the line-auto golden the residual is
	 * within +/-0.06pt of a line, which is what a page break turns on.  {@code atLeast}
	 * is rounded too: 20pt is 166.67 units, and Word's pitch for
	 * {@code w:line="400" w:lineRule="atLeast"} is 20.04, not 20.00.
	 *
	 * @since 17.1.0
	 */
	public static final double GRID_PT = 72.0 / 600.0;

	/**
	 * A length snapped to Word's 600 dpi layout grid ({@link #GRID_PT}).
	 *
	 * <p>Half a unit goes to the even one, which is what Word's own halves do: Liberation
	 * Serif 12pt at {@code w:line="360"} is 115 x 1.5 = 172.5 units and Word paints
	 * 20.64 (172) on two of the golden's three pitches, not 20.76 (173).
	 *
	 * @since 17.1.0
	 */
	public static double onGrid(double pt) {
		return deviceGrid() ? fromUnits(gridUnits(pt)) : pt;
	}

	/**
	 * {@code docx4j.fonts.wordLineMetrics.deviceGrid}: whether the single line height is
	 * rounded to Word's 600 dpi layout grid.
	 *
	 * <p><b>Off by default, although it is what Word does</b> - which is worth stating
	 * plainly.  The grid is confirmed on all four fonts of the {@code line-auto} golden
	 * (see {@link #GRID_PT}), and the arithmetic without it is 0.001 to 0.021pt short of
	 * every one of them.  But 0.02pt a line is smaller than what the rest of the layout
	 * still gets wrong, and moving it changes which line falls at a page bottom: measured
	 * over the 156 documents of the hardest corpus, rounding cost 124 matched lines
	 * (54491 -&gt; 54367 of 71610), took the median document from 0.9065 to 0.9035, and
	 * lost 0.146 on one seven-page document whose page breaks it flipped.  It is left
	 * here, switchable, for a round which can measure it against a corpus whose page
	 * breaks are not already decided by other errors.
	 *
	 * @since 17.1.0
	 */
	public static final String DEVICE_GRID = "docx4j.fonts.wordLineMetrics.deviceGrid";

	private static boolean deviceGrid() {
		String v = System.getProperty(DEVICE_GRID);
		if (v == null) return org.docx4j.Docx4jProperties.getProperty(DEVICE_GRID, false);
		return Boolean.parseBoolean(v.trim());
	}

	/** A length in whole grid units, which is where the arithmetic is done: 13.80pt
	 *  is 115 of them, and 115 x 276/240 is 132.25, not 15.869pt. */
	private static double gridUnits(double pt) {
		return Math.rint(pt * 600.0 / 72.0);
	}

	private static double fromUnits(double units) {
		return units * 72.0 / 600.0;
	}

	/**
	 * Word's single line height in points for text in this font at this size, on the
	 * 600 dpi grid ({@link #GRID_PT}).  Everything the {@code w:spacing} rules do is
	 * done to this value, not to the raw factor.
	 *
	 * @since 17.1.0
	 */
	public static double singleLineHeightPt(String documentFont, PhysicalFont pf, double sizePt) {
		return fromUnits(singleUnits(documentFont, pf, sizePt));
	}

	private static double singleUnits(String documentFont, PhysicalFont pf, double sizePt) {
		double exact = get(documentFont, pf).lineHeightFactor() * sizePt;
		return deviceGrid() ? gridUnits(exact) : exact * 600.0 / 72.0;
	}

	/**
	 * Word's line height in points for text in this font at this size, under the
	 * paragraph's w:spacing (line/lineRule); "single" when spacing is null or has
	 * no w:line.
	 */
	public static double lineHeightPt(PhysicalFont pf, double sizePt, PPrBase.Spacing spacing) {
		return lineHeightPt(null, pf, sizePt, spacing);
	}

	/** As {@link #lineHeightPt(PhysicalFont, double, PPrBase.Spacing)}, sized from the document font when the table knows it. @since 17.0.5 */
	public static double lineHeightPt(String documentFont, PhysicalFont pf, double sizePt, PPrBase.Spacing spacing) {
		double singleU = singleUnits(documentFont, pf, sizePt);
		if (spacing == null || spacing.getLine() == null) return fromUnits(singleU);
		double line = spacing.getLine().doubleValue();
		STLineSpacingRule rule = spacing.getLineRule() == null ? STLineSpacingRule.AUTO : spacing.getLineRule();
		switch (rule) {
		case EXACT:
			return line / 20.0;
		case AT_LEAST:
			return Math.max(fromUnits(singleU), line / 20.0);
		case AUTO:
		default:
			return fromUnits(singleU) * line / 240.0;
		}
	}

		/**
	 * Where Word puts the baseline, measured from the top of the line, for text
	 * in this font/size under this w:spacing.  Measured from Word 365 output
	 * (CR-001, line-auto and line-exact-atleast probes): for "auto" spacing the
	 * baseline sits at usWinAscent + external leading and any extra leading goes
	 * below the text; for "atLeast" the extra goes above; for "exact" the line is
	 * scaled proportionally.
	 */
	public static double wordBaselinePt(PhysicalFont pf, double sizePt, PPrBase.Spacing spacing) {
		return wordBaselinePt(null, pf, sizePt, spacing);
	}

	/** @since 17.0.5 */
	public static double wordBaselinePt(String documentFont, PhysicalFont pf, double sizePt, PPrBase.Spacing spacing) {
		Metrics m = get(documentFont, pf);
		double single = singleLineHeightPt(documentFont, pf, sizePt);
		double natural = (m.winAscent + m.externalLeading) * sizePt;
		if (spacing == null || spacing.getLine() == null) return natural;
		double line = onGrid(spacing.getLine().doubleValue() / 20.0);
		STLineSpacingRule rule = spacing.getLineRule() == null ? STLineSpacingRule.AUTO : spacing.getLineRule();
		switch (rule) {
		case EXACT:
			// measured 0.80 of the line for Liberation Serif at 9/12/24pt exact (CR-001 §6.9)
			return line * m.winAscent / (m.winAscent + m.winDescent);
		case AT_LEAST:
			return natural + Math.max(0, line - single);
		case AUTO:
		default:
			return natural;
		}
	}

	/**
	 * Where FOP puts the baseline, measured from the top of a line whose block
	 * has this font/size and this line-height: half the leading above FOP's
	 * content rectangle (its ascender + descender), then the ascender.  If the
	 * line-height is smaller than the content rectangle, the content rectangle
	 * wins (FOP does not shrink lines below it).
	 */
	public static double fopBaselinePt(PhysicalFont pf, double sizePt, double lineHeightPt) {
		Metrics m = get(pf);
		double content = (m.fopAscent + m.fopDescent) * sizePt;
		double lh = Math.max(lineHeightPt, content);
		return (lh - content) / 2 + m.fopAscent * sizePt;
	}

		/**
	 * The baseline-shift (positive = up) that would move FOP's text to where Word's
	 * sits within a line of the given height; 0 when the font could not be read.
	 * Not applied by the FO exporter: emitting it as baseline-shift on the span
	 * makes FOP enlarge the line box rather than move the glyphs (measured, CR-001).
	 * Kept for a layout-manager level implementation.
	 */
	public static double baselineShiftPt(PhysicalFont pf, double sizePt, PPrBase.Spacing spacing, double lineHeightPt) {
		if (get(pf).fallback) return 0;
		return fopBaselinePt(pf, sizePt, lineHeightPt) - wordBaselinePt(pf, sizePt, spacing);
	}

	/** {@link #lineHeightPt} formatted as an FO/CSS length, e.g. "13.8pt".  @since 17.0.5 */
	public static String lineHeightPtString(String documentFont, PhysicalFont pf, double sizePt, PPrBase.Spacing spacing) {
		return format(lineHeightPt(documentFont, pf, sizePt, spacing));
	}

	public static String lineHeightPtString(PhysicalFont pf, double sizePt, PPrBase.Spacing spacing) {
		return format(lineHeightPt(pf, sizePt, spacing));
	}

	public static String format(double pt) {
		// 3 decimals is well below anything FOP or Word can position at (both work in millipoints)
		String s = String.format(java.util.Locale.ROOT, "%.3f", pt);
		if (s.indexOf('.') >= 0) {
			s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
		}
		return s + "pt";
	}

	// ------------------------------------------------------------------ parsing

	/** Parse the hhea and OS/2 tables of a TrueType/OpenType font (first font of a collection). */
	static Metrics read(InputStream in) throws IOException {
		byte[] data = in.readAllBytes();
		int offset = 0;
		if (data.length >= 12 && tag(data, 0).equals("ttcf")) {
			offset = u32(data, 12); // first font's offset table
		}
		int numTables = u16(data, offset + 4);
		int upem = 0;
				int hheaAsc = 0, hheaDesc = 0, hheaGap = 0;
		int winAsc = -1, winDesc = -1;
		boolean eastAsian = false;
		int typoAsc = 0, typoDesc = 0;
		for (int i = 0; i < numTables; i++) {
			int rec = offset + 12 + 16 * i;
			String t = tag(data, rec);
			int off = u32(data, rec + 8);
			int len = u32(data, rec + 12);
			if (off + len > data.length) continue;
			if (t.equals("head")) {
				upem = u16(data, off + 18);
			} else if (t.equals("hhea")) {
				hheaAsc = s16(data, off + 4);
				hheaDesc = s16(data, off + 6);
				hheaGap = s16(data, off + 8);
						} else if (t.equals("OS/2") && len >= 78) {
				typoAsc = s16(data, off + 68);
				typoDesc = s16(data, off + 70);
				winAsc = u16(data, off + 74);
				winDesc = u16(data, off + 76);
				// ulCodePageRange1 (OS/2 version 1 and later, so at least 82 bytes) bits
				// 17-21: JIS/Japan 932, Chinese Simplified 936, Korean Wansung 949,
				// Chinese Traditional 950, Korean Johab 1361.  A font they flag takes
				// EAST_ASIAN_FACTOR x its usWin box for a line.  @since 17.1.1
				if (len >= 82) {
					long cp1 = u32(data, off + 78) & 0xffffffffL;
					eastAsian = (cp1 & (0x1fL << 17)) != 0;
				}
			}
		}
		if (upem <= 0) throw new IOException("no head table / unitsPerEm");
		if (winAsc < 0) {
			// no OS/2 table (rare, old Apple fonts): GDI would synthesise from hhea
			winAsc = hheaAsc;
			winDesc = -hheaDesc;
		}
				int tmHeight = winAsc + winDesc;
		int ext = Math.max(0, (hheaAsc - hheaDesc + hheaGap) - tmHeight);
		// FOP's OpenFont.determineAscDesc: OS/2 typo metrics if they fit the em box, else hhea, else whichever exists
		int fopAsc, fopDesc;
		if (typoAsc > 0 && (typoAsc - typoDesc) <= upem) {
			fopAsc = typoAsc; fopDesc = typoDesc;
		} else if (hheaAsc > 0 && (hheaAsc - hheaDesc) <= upem) {
			fopAsc = hheaAsc; fopDesc = hheaDesc;
		} else if (typoAsc > 0) {
			fopAsc = typoAsc; fopDesc = typoDesc;
		} else {
			fopAsc = hheaAsc; fopDesc = hheaDesc;
		}
		return new Metrics((double) winAsc / upem, (double) winDesc / upem, (double) ext / upem,
				(double) fopAsc / upem, (double) -fopDesc / upem, false, eastAsian);
	}

	private static String tag(byte[] d, int p) {
		return new String(d, p, 4, java.nio.charset.StandardCharsets.ISO_8859_1);
	}

	private static int u16(byte[] d, int p) {
		return ((d[p] & 0xff) << 8) | (d[p + 1] & 0xff);
	}

	private static int s16(byte[] d, int p) {
		return (short) u16(d, p);
	}

	private static int u32(byte[] d, int p) {
		return ((d[p] & 0xff) << 24) | ((d[p + 1] & 0xff) << 16) | ((d[p + 2] & 0xff) << 8) | (d[p + 3] & 0xff);
	}

	/** For tests and diagnostics: parse a font from a stream without a PhysicalFont. */
	public static Metrics readMetrics(InputStream in) throws IOException {
		return read(new DataInputStream(in));
	}
}
