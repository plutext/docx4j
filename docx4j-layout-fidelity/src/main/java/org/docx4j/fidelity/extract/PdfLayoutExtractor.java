package org.docx4j.fidelity.extract;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;

/** Builds a {@link PdfLayout} from a PDF with PDFBox: text lines via PDFTextStripper, boxes via a graphics stream engine. */
public final class PdfLayoutExtractor {

	private PdfLayoutExtractor() {}

	/**
	 * How wide a gap between two glyphs, as a fraction of the font size, is read as a
	 * word space where the PDF has no space glyph.
	 *
	 * <p>0.15 em split words: Word's PDF of one corpus document has a heading PDFBox
	 * reads as "Inte ntion of the 5 week Program", where {@code mutool draw -F stext}
	 * reads {@code Intention} with the glyphs running 12.75..54.91 contiguously.  The
	 * gap is PDFBox's own arithmetic - the glyph advance it charges against the position
	 * the PDF sets - and a space is never that narrow: an ordinary space is 0.25 to 0.28
	 * em in the fonts these documents use, and a justified line compresses one to about
	 * 0.19 em at worst (&#xa7;4.2).  0.25 em is below that and above the widest
	 * intra-word gap measured.  {@code -Dfidelity.wordGapEm=} overrides it.
	 */
	private static final float WORD_GAP_EM =
			Float.parseFloat(System.getProperty("fidelity.wordGapEm", "0.25"));

	/**
	 * How wide a gap has to be, in points, before it can split a baseline into two
	 * lines at all.
	 *
	 * <p>The gap rules below are relative (0.7 em, three median word gaps), so a short
	 * tab gap splits a line whenever it is written as a real gap and does not when it is
	 * written as space glyphs - and Word and docx4j do not agree on which.  A numbered
	 * or bulleted paragraph is the common shape: Word's PDF writes the tab between the
	 * label and the text as space glyphs at the label's own size, so PDFBox reads
	 * {@code "1. Introduction"} as one line, while ours writes no glyph in the gap and
	 * the same paragraph is read as {@code "1."} and {@code "Introduction"}.  Neither
	 * reading is wrong, but they must be the same on both sides or the LCS loses the
	 * pair, and it loses two lines each time it does.  Below this width a gap therefore
	 * never splits, on either side; the vertical-rule test (a real cell boundary) still
	 * does, at any width.
	 *
	 * <p>20pt covers the 0.25in family of hanging indents whole and leaves the 0.5in
	 * family - wide enough that both sides read it the same way - splitting as before.
	 * <b>It is nearly inert</b>, and deliberately so: measured on eight label-heavy
	 * corpus documents it merges four reference lines and wins one extra match out of
	 * 8086, because the 0.7 em and three-median tests below already refuse most short
	 * gaps.  It is kept because the asymmetry it closes is systematic rather than large,
	 * and it can only ever merge - never split - and does so on both sides alike.  See
	 * the README.  {@code -Dfidelity.minSplitPt=} overrides it; 0 disables it.
	 */
	private static final float MIN_SPLIT_PT =
			Float.parseFloat(System.getProperty("fidelity.minSplitPt", "20"));

	/**
	 * Whether the gap which decides a line split is measured from the last glyph that
	 * put ink on the page, rather than from the last glyph of any kind.
	 *
	 * <p>The two renders disagree about how a tab is written, not about where the text
	 * goes: Word's PDF writes the tab between a list label and its text as a space
	 * glyph, so the gap after it is measured from the space's right edge, where ours
	 * writes no glyph at all and the gap is measured from the label's.  The same tab
	 * therefore splits one side's line and not the other's, and the LCS loses both.
	 * Measuring ink to ink is symmetric, and is applied to the median word gap as well
	 * as to the split test.
	 *
	 * <p><b>Measured, and rejected as a default.</b>  It is symmetric, but it resolves
	 * the disagreement in the splitting direction rather than the merging one: Word's
	 * side gains the splits ours already had.  On the twelve documents three triage
	 * ledgers named for this asymmetry it is a clear win - lines matched 91.6% to 93.2%,
	 * mean line parity 0.9147 to 0.9235, four documents reaching 1.0 - but those are the
	 * documents the hypothesis was written from.  Over a whole corpus of 191 it costs:
	 * reference lines 40339 to 41840 and matched 35053 to 36251, so the 1501 lines it
	 * newly separates match at only 80% against the corpus's 86.9%, and mean line parity
	 * falls 0.8816 to 0.8711 with 34 documents down against 22 up.  The extra splits are
	 * real cell boundaries and the exposure is honest, but it moves the yardstick without
	 * improving the layout, and it breaks comparability with every earlier scoreboard.
	 * Raising {@link #MIN_SPLIT_PT} - the same disagreement resolved the other way, by
	 * merging - was measured over an unbiased quarter of the same corpus and is inert
	 * (mean 0.8967 at 20pt, 0.8973 at 32pt, 0.8950 at 72pt), and so is switching the
	 * column-gutter rule off (0.8967 against 0.8966).
	 *
	 * <p>{@code -Dfidelity.inkGap=true} turns it on; the default is the per-glyph
	 * measure.
	 */
	private static final boolean INK_GAP =
			Boolean.parseBoolean(System.getProperty("fidelity.inkGap", "false"));

	/**
	 * How wide a band of the page has to be free of ink, and how many lines have to lie
	 * on each side of it, before it is read as a <b>column gutter</b> - a boundary the
	 * text on every line is divided at, whatever that line's own word gaps look like.
	 *
	 * <p>The per-line rules below cannot see a two-column page.  A gap splits a baseline
	 * only where it is more than three times that line's median word gap, and a
	 * <em>justified</em> line's word gaps are stretched - on the two-column page which
	 * motivated this, to 4.7pt against the 2.5pt of the same font's natural space - so
	 * the 51pt gutter between the columns is under three of them and the two columns are
	 * read as one line.  Word's PDF of that page has the two columns' baselines a
	 * fraction of a point apart, so its own extraction separates them, and every line of
	 * the page then failed to match ours.
	 *
	 * <p>The page's geometry says where the columns are without reference to any one
	 * line: a run of x where no glyph on the page puts ink, wide enough and with enough
	 * lines either side of it, is a gutter.  It is measured from the same glyph boxes on
	 * both sides, so Word's PDF and ours find the same bands, and it can only ever split
	 * a line further - never merge two.
	 *
	 * <p>10pt is above the widest inter-word gap seen on a justified line (5.2pt) and
	 * below the narrowest real gutter in the corpora (a 12pt {@code w:cols} space); 8
	 * lines each side keeps a two-line letterhead or a single right-tabbed heading from
	 * declaring one.  {@code -Dfidelity.columnGutterPt=} and
	 * {@code -Dfidelity.columnGutterLines=} override them; 0 disables the rule.
	 *
	 * @since 17.1.0
	 */
	private static final float COLUMN_GUTTER_PT =
			Float.parseFloat(System.getProperty("fidelity.columnGutterPt", "10"));

	/** @see #COLUMN_GUTTER_PT */
	private static final int COLUMN_GUTTER_LINES =
			Integer.parseInt(System.getProperty("fidelity.columnGutterLines", "8"));

	/**
	 * How much of the page's ink each side of a gutter has to span before the two are
	 * read as columns, as a fraction of the whole.
	 *
	 * <p>Without it the rule fires on every hanging indent: the band between a bullet
	 * and the text beside it is 13pt wide and repeats on every line of the list, so it
	 * passes both tests above - and splitting there costs, because Word's PDF writes the
	 * label separator as space glyphs and reads the two as one line.  Two text columns
	 * span about 0.47 of the page's ink each; a bullet column spans 0.01 and a label
	 * column 0.15.  Measured over the three corpora, 0.3 leaves the label columns
	 * unsplit and still finds the two-column pages.  {@code -Dfidelity.columnSideFraction=}
	 * overrides it.
	 *
	 * @see #COLUMN_GUTTER_PT
	 */
	private static final float COLUMN_SIDE_FRACTION =
			Float.parseFloat(System.getProperty("fidelity.columnSideFraction", "0.3"));

	/**
	 * How alike the two sides of a gutter have to be, as the narrower's span over the
	 * wider's, before they are read as columns rather than as an indent.
	 *
	 * <p>What is left over after the fraction above is the shape where a page of
	 * full-width prose has a narrow left-hand column of labels beside part of it: the
	 * label side spans 0.45 to 0.51 of the text side, and whether the band is clear of
	 * ink on any given page is then decided by one long line, so Word's PDF and ours
	 * find it on different pages and the split is one-sided - which loses lines rather
	 * than winning them (two corpus documents, -0.084 and -0.051, where every document
	 * whose columns are alike gained).  Word's own columns are equal unless
	 * {@code w:cols/@w:equalWidth="0"}, and the measured pairs are 0.76 to 0.99.
	 *
	 * <p>The cost is that a genuinely unequal two-column section - &#xa7;7's 157/318pt
	 * certificate is 0.49 - is left to the per-line rules.  {@code -Dfidelity.columnBalance=}
	 * overrides it.
	 *
	 * @see #COLUMN_GUTTER_PT
	 */
	private static final float COLUMN_BALANCE =
			Float.parseFloat(System.getProperty("fidelity.columnBalance", "0.6"));

	public static PdfLayout extract(File pdf) throws IOException {
		try (PDDocument doc = Loader.loadPDF(pdf)) {
			PdfLayout out = new PdfLayout();
			for (PDPage page : doc.getPages()) {
				out.pageWidths.add((double) page.getMediaBox().getWidth());
				out.pageHeights.add((double) page.getMediaBox().getHeight());
			}
			int i = 0;
			for (PDPage page : doc.getPages()) {
				new BoxCollector(page, i, out).run();
				i++;
			}
			// boxes first: the text collector uses vertical rules (table borders) as split points
			TextCollector tc = new TextCollector(out);
			tc.setSortByPosition(true);
			tc.getText(doc);
			out.lines.sort((a, b) -> a.page != b.page ? Integer.compare(a.page, b.page)
					: (Math.abs(a.y - b.y) > 0.01 ? Double.compare(a.y, b.y) : Double.compare(a.x0, b.x0)));
			orderByRow(out);
			return out;
		}
	}

	/**
	 * How far apart, in points, two lines' baselines may be and still be read as one
	 * row - which is ordered left to right, not by baseline.
	 *
	 * <p>The comparison is an LCS over the line list in order, so a pair of lines the
	 * two PDFs put in the opposite order is a pair the LCS cannot match, and it loses
	 * both.  A label cell beside the text it labels is where that happens: a
	 * {@code w:vAlign} label and the first line of the body column beside it sit within
	 * a line of each other, and which of the two has the smaller baseline is decided by
	 * a fraction of a point of vertical alignment - Word puts one corpus document's
	 * "Assessment of student" 0.7pt <em>above</em> the "Diagnostic assessment: KWL
	 * chart" it labels and we put it 7.0pt <em>below</em>, so the strict baseline order
	 * disagrees although both renders paint the same row.  Ordering a row left to right
	 * makes the two agree whatever the vertical alignment does, and it is the order the
	 * documents are read in.
	 *
	 * <p>Measured (b2-batch21) on eight label-heavy corpus documents, 8086 reference
	 * lines: lines matched 6458 at 0 (the strict baseline order), 6725 at 2pt,
	 * <b>6730 at 3pt</b>, 6722 at 4pt, 6686 at 5pt, 6738 at 8pt (but median parity
	 * 0.8620 against 3pt's 0.8697) and 6526 at 12pt, where a row starts swallowing the
	 * next line of a column.  3pt is the peak; a column's line pitch is at least 9pt in
	 * the densest of those tables.  {@code -Dfidelity.rowTolerancePt=} overrides it; 0
	 * restores the strict baseline order.
	 *
	 * @since 17.1.0
	 */
	private static final double ROW_TOLERANCE_PT =
			Double.parseDouble(System.getProperty("fidelity.rowTolerancePt", "3"));

	/** Order each row (a run of lines on one page whose baselines lie within
	 *  {@link #ROW_TOLERANCE_PT} of the row's first) left to right. */
	private static void orderByRow(PdfLayout out) {
		if (ROW_TOLERANCE_PT <= 0) return;
		for (int i = 0; i < out.lines.size(); ) {
			int j = i + 1;
			double y0 = out.lines.get(i).y;
			int page = out.lines.get(i).page;
			while (j < out.lines.size() && out.lines.get(j).page == page
					&& out.lines.get(j).y - y0 <= ROW_TOLERANCE_PT) j++;
			if (j - i > 1) {
				// stable: lines at the same x keep their baseline order
				out.lines.subList(i, j).sort((a, b) -> Double.compare(a.x0, b.x0));
			}
			i = j;
		}
	}

	/**
	 * Collects every glyph position per page, then forms lines itself: glyphs are
	 * clustered by baseline, and a cluster is split into separate lines where the
	 * horizontal gap exceeds 0.7 x the font size (table cells, tab stops, columns).
	 * PDFTextStripper's own line grouping merges table cells that share a baseline.
	 */
	private static final float[] NO_GUTTERS = new float[0];

	private static final class TextCollector extends PDFTextStripper {
		private final PdfLayout out;
		private final List<TextPosition> pagePositions = new ArrayList<>();

		TextCollector(PdfLayout out) {
			this.out = out;
		}

		@Override
		protected void writeString(String s, List<TextPosition> positions) {
			pagePositions.addAll(positions);
		}

		@Override
		protected void endPage(PDPage page) throws IOException {
			formLines(getCurrentPageNo() - 1);
			pagePositions.clear();
			super.endPage(page);
		}

		/**
		 * The floor on how far apart, in points, two glyphs' baselines may be and still
		 * be read as one line.  The tolerance is the larger of this and 0.3 em, and the
		 * comparison is against the baseline of the glyph which opened the cluster.
		 *
		 * <p>Below the floor the grouping is unstable where a row's cells are aligned a
		 * fraction of a point apart: on one corpus document Word's own PDF spreads five
		 * table headings over 2.16pt and is read as one line, where our render spreads
		 * the same five over 1.95pt - the <em>smaller</em> spread - and is read as
		 * several, so the LCS loses every one of them.
		 *
		 * <p><b>Measured, and the floor is not the cause.</b>  The tolerance is the
		 * larger of this and 0.3 em, and 0.3 em already exceeds it for any body text, so
		 * the floor never binds: 1, 1.5 and 2pt score identically, and 2.5pt and above
		 * are worse (mean line parity 0.9147 at 1-2pt, 0.9127 at 2.5pt, 0.9140 at 4pt).
		 * That document's inflation is horizontal, not vertical - it is the tab-gap
		 * asymmetry of {@link #INK_GAP}, which takes it from 278 reference lines against
		 * 362 of ours to 362 against 362.  Kept as a knob so the measurement can be
		 * repeated.  {@code -Dfidelity.clusterTolerancePt=} overrides it.
		 */
		private static final float CLUSTER_TOLERANCE_PT =
				Float.parseFloat(System.getProperty("fidelity.clusterTolerancePt", "1"));

		private void formLines(int pageIndex) {
			List<TextPosition> ps = new ArrayList<>(pagePositions);
			ps.sort((a, b) -> Math.abs(a.getYDirAdj() - b.getYDirAdj()) > 0.01f
					? Float.compare(a.getYDirAdj(), b.getYDirAdj()) : Float.compare(a.getXDirAdj(), b.getXDirAdj()));
			List<List<TextPosition>> clusters = new ArrayList<>();
			List<TextPosition> cluster = new ArrayList<>();
			float clusterY = 0;
			for (TextPosition tp : ps) {
				float tol = Math.max(CLUSTER_TOLERANCE_PT, 0.3f * tp.getFontSizeInPt());
				if (!cluster.isEmpty() && Math.abs(tp.getYDirAdj() - clusterY) > tol) {
					clusters.add(cluster);
					cluster = new ArrayList<>();
				}
				if (cluster.isEmpty()) clusterY = tp.getYDirAdj();
				cluster.add(tp);
			}
			if (!cluster.isEmpty()) clusters.add(cluster);
			// the page's own column geometry, which no single line can show
			float[] gutters = gutters(clusters);
			for (List<TextPosition> c : clusters) emit(pageIndex, c, gutters);
		}

		/**
		 * The column gutters of this page: an even-length array of x pairs, each the
		 * low and high edge of a band no glyph on the page puts ink in.
		 *
		 * @see #COLUMN_GUTTER_PT
		 */
		private static float[] gutters(List<List<TextPosition>> clusters) {
			if (COLUMN_GUTTER_PT <= 0 || clusters.size() < 2 * COLUMN_GUTTER_LINES) return NO_GUTTERS;
			List<float[]> ink = new ArrayList<>();
			for (List<TextPosition> c : clusters) {
				for (TextPosition tp : c) {
					if (isBlank(tp)) continue; // a space does not fill a gutter
					ink.add(new float[] { tp.getXDirAdj(), tp.getXDirAdj() + tp.getWidthDirAdj() });
				}
			}
			if (ink.isEmpty()) return NO_GUTTERS;
			ink.sort((a, b) -> Float.compare(a[0], b[0]));
			// the empty bands between the runs of ink, in order
			List<float[]> bands = new ArrayList<>();
			float hi = ink.get(0)[1];
			for (float[] iv : ink) {
				if (iv[0] - hi >= COLUMN_GUTTER_PT) bands.add(new float[] { hi, iv[0] });
				hi = Math.max(hi, iv[1]);
			}
			if (bands.isEmpty()) return NO_GUTTERS;
			float inkFrom = ink.get(0)[0], inkTo = hi;
			float minSide = COLUMN_SIDE_FRACTION * (inkTo - inkFrom);
			// and only those with enough lines on both sides, each side spanning enough
			// of the page's ink to be a column: the band between a bullet and its text
			// repeats on every line of a list but has 5pt of ink to its left, and one
			// heading tabbed to the right of a page of body text is not a boundary either
			List<Float> keep = new ArrayList<>();
			for (float[] band : bands) {
				int left = 0, right = 0;
				float lFrom = Float.MAX_VALUE, lTo = -Float.MAX_VALUE;
				float rFrom = Float.MAX_VALUE, rTo = -Float.MAX_VALUE;
				for (List<TextPosition> c : clusters) {
					boolean l = false, r = false;
					for (TextPosition tp : c) {
						if (isBlank(tp)) continue;
						float x0 = tp.getXDirAdj(), x1 = x0 + tp.getWidthDirAdj();
						if (x1 <= band[0] + 0.01f) {
							l = true;
							lFrom = Math.min(lFrom, x0);
							lTo = Math.max(lTo, x1);
						} else if (x0 >= band[1] - 0.01f) {
							r = true;
							rFrom = Math.min(rFrom, x0);
							rTo = Math.max(rTo, x1);
						}
					}
					if (l) left++;
					if (r) right++;
				}
				float lSpan = lTo - lFrom, rSpan = rTo - rFrom;
				boolean alike = Math.min(lSpan, rSpan) >= COLUMN_BALANCE * Math.max(lSpan, rSpan);
				if (left >= COLUMN_GUTTER_LINES && right >= COLUMN_GUTTER_LINES
						&& lSpan >= minSide && rSpan >= minSide && alike) {
					keep.add(band[0]);
					keep.add(band[1]);
				}
			}
			if (keep.isEmpty()) return NO_GUTTERS;
			float[] out = new float[keep.size()];
			for (int i = 0; i < out.length; i++) out[i] = keep.get(i);
			return out;
		}

		/** Whether the gap from {@code from} to {@code to} contains a column gutter. */
		private static boolean crossesGutter(float[] gutters, float from, float to) {
			for (int i = 0; i < gutters.length; i += 2) {
				if (from <= gutters[i] + 0.01f && to >= gutters[i + 1] - 0.01f) return true;
			}
			return false;
		}

		/**
		 * Split a baseline cluster into lines (a) at a gap of at least {@link #MIN_SPLIT_PT}
		 * which is also wider than 0.7 em and more than three times the cluster's median
		 * word gap (tab stops, borderless cells), (b) at any gap crossed by a vertical
		 * rule (table borders), or (c) at any gap containing one of the page's column
		 * gutters. Justified text has uniformly wide word gaps, so (a) keeps such lines
		 * together - which is what (c) is for.
		 */
		private void emit(int pageIndex, List<TextPosition> cluster, float[] gutters) {
			cluster.sort((a, b) -> Float.compare(a.getXDirAdj(), b.getXDirAdj()));
			List<Float> gaps = new ArrayList<>();
			TextPosition gapFrom = null;
			for (TextPosition tp : cluster) {
				if (INK_GAP && isBlank(tp)) continue;
				if (gapFrom != null) {
					float g = tp.getXDirAdj() - (gapFrom.getXDirAdj() + gapFrom.getWidthDirAdj());
					if (g > WORD_GAP_EM * gapFrom.getFontSizeInPt()) gaps.add(g);
				}
				gapFrom = tp;
			}
			Collections.sort(gaps);
			float medianWordGap = gaps.isEmpty() ? 0f : gaps.get(gaps.size() / 2);
			List<TextPosition> run = new ArrayList<>();
			TextPosition prev = null;
			for (TextPosition tp : cluster) {
				/* A glyph which puts no ink on the page neither opens a line nor closes
				 * one: Word writes the tab between a list label and its text as a space
				 * glyph, so the gap on its side of the comparison is measured from the
				 * space's right edge and ours from the label's, and the same tab splits
				 * one render's line and not the other's.  Measuring ink to ink puts both
				 * sides on the same footing.  @see #INK_GAP */
				if (INK_GAP && isBlank(tp)) {
					run.add(tp);
					continue;
				}
				if (prev != null) {
					float from = prev.getXDirAdj() + prev.getWidthDirAdj();
					float gap = tp.getXDirAdj() - from;
					float em = Math.max(prev.getFontSizeInPt(), 1f);
					boolean wide = gap >= MIN_SPLIT_PT && gap > 0.7f * em && gap > 3f * medianWordGap;
					if (wide || crossesGutter(gutters, from, tp.getXDirAdj())
							|| (gap > 0 && verticalRuleBetween(pageIndex, from, tp.getXDirAdj(), tp.getYDirAdj(), em))) {
						addLine(pageIndex, run);
						run = new ArrayList<>();
					}
				}
				run.add(tp);
				prev = tp;
			}
			addLine(pageIndex, run);
		}

		/** A thin vertical stroke/fill box lying horizontally inside [x0,x1] and vertically spanning the baseline.
		 *
		 * <p>A rule the reader cannot see is not a rule.  Word paints its cell borders as
		 * filled rectangles, so they arrive here with a real width; FOP paints them as
		 * <em>strokes</em>, whose bounding box is zero-wide for a vertical line, so the
		 * width alone cannot tell a genuine border from a border that paints nothing.
		 * What tells them apart is the colour: a document collapsing its cell boundaries
		 * had FOP stroke 52 of them per page in {@code 6pt white} on white paper - Word's
		 * PDF of the same page draws nothing there at all - and every such boundary split
		 * an extracted table row into one line per cell, doubling that document's
		 * candidate line count against an unchanged golden.  Its visible neighbours, and
		 * the 364 grey 1pt strokes of another document's table, are unaffected. */
		private boolean verticalRuleBetween(int pageIndex, float x0, float x1, float baseline, float em) {
			for (PdfLayout.Box b : out.boxes) {
				if (b.page != pageIndex || b.w > 3 || b.h < 0.5 * em || b.invisible) continue;
				double cx = b.x + b.w / 2;
				if (cx < x0 || cx > x1) continue;
				if (b.y <= baseline && b.y + b.h >= baseline - 0.7 * em) return true;
			}
			return false;
		}

		private void addLine(int pageIndex, List<TextPosition> run) {
			if (run.isEmpty()) return;
			StringBuilder text = new StringBuilder();
			List<Double> ys = new ArrayList<>();
			/* x0/x1 span the ink, not the whitespace glyphs around it.  Word writes a
			 * tab as a space glyph at the position the tab started from, and ends a
			 * justified line with the space that carries the paragraph mark's size, so
			 * counting those made a line look up to 3pt wider at each end than the text
			 * on it - and made a correct indent look like a 60 twip error. */
			double x0 = Double.MAX_VALUE, x1 = -Double.MAX_VALUE;
			TextPosition prev = null;
			TextPosition firstInk = null;
			for (TextPosition tp : run) {
				if (prev != null) {
					float gap = tp.getXDirAdj() - (prev.getXDirAdj() + prev.getWidthDirAdj());
					if (gap > WORD_GAP_EM * prev.getFontSizeInPt() && text.length() > 0 && text.charAt(text.length() - 1) != ' ') {
						text.append(' ');
					}
				}
				text.append(tp.getUnicode());
				ys.add((double) tp.getYDirAdj());
				if (!isBlank(tp)) {
					if (firstInk == null) firstInk = tp;
					x0 = Math.min(x0, tp.getXDirAdj());
					x1 = Math.max(x1, tp.getXDirAdj() + tp.getWidthDirAdj());
				}
				prev = tp;
			}
			/* A line which puts no ink on the page is not a line.  String.trim() only
			 * strips characters <= U+0020, so a run of U+2002 EN SPACE or NBSP survived
			 * it and read out as a line with x0 = Double.MAX_VALUE: 54 such phantom
			 * lines in 4 documents of one corpus, 44 of them 24% of a single document's
			 * extracted lines, where Word's PDF of the same document has none.  isBlank()
			 * already knows every whitespace glyph, so firstInk == null is the test. */
			if (firstInk == null) return;
			String t = text.toString().trim().replaceAll("\\s+", " ");
			if (t.isEmpty()) return;
			Collections.sort(ys);
			PdfLayout.Line l = new PdfLayout.Line();
			l.page = pageIndex;
			l.y = ys.get(ys.size() / 2);
			l.x0 = x0;
			l.x1 = x1;
			TextPosition first = firstInk;
			l.size = first.getFontSizeInPt();
			l.font = first.getFont() == null ? "" : String.valueOf(first.getFont().getName());
			l.text = t;
			out.lines.add(l);
		}

		/** Whether this glyph puts no ink on the page (a space, or a tab written as one). */
		private static boolean isBlank(TextPosition tp) {
			String u = tp.getUnicode();
			if (u == null || u.isEmpty()) return true;
			for (int i = 0; i < u.length(); i++) {
				char c = u.charAt(i);
				if (!Character.isWhitespace(c) && c != '\u00a0' && c != '\u200b') return false;
			}
			return true;
		}
	}

	/** Records the bounding box of every stroked/filled path and every image, in top-left page coordinates. */
	private static final class BoxCollector extends PDFGraphicsStreamEngine {
		private final int pageIndex;
		private final PdfLayout out;
		private final double pageHeight;
		private GeneralPath path = new GeneralPath();

		BoxCollector(PDPage page, int pageIndex, PdfLayout out) {
			super(page);
			this.pageIndex = pageIndex;
			this.out = out;
			this.pageHeight = page.getMediaBox().getHeight();
		}

		void run() throws IOException {
			processPage(getPage());
		}

		private void record(String kind, Rectangle2D r) {
			if (r == null || (r.getWidth() == 0 && r.getHeight() == 0)) return;
			PdfLayout.Box b = new PdfLayout.Box();
			b.page = pageIndex;
			b.kind = kind;
			b.x = r.getX();
			b.y = pageHeight - (r.getY() + r.getHeight());
			b.w = r.getWidth();
			b.h = r.getHeight();
			b.invisible = !"image".equals(kind) && isWhite(kind);
			out.boxes.add(b);
		}

		/** True where the paint is white, i.e. the same as the paper. */
		private boolean isWhite(String kind) {
			try {
				PDColor c = "stroke".equals(kind)
						? getGraphicsState().getStrokingColor()
						: getGraphicsState().getNonStrokingColor();
				int rgb = c.toRGB();
				return ((rgb >> 16) & 0xff) >= 250 && ((rgb >> 8) & 0xff) >= 250 && (rgb & 0xff) >= 250;
			} catch (Exception e) {
				return false;   // a colour space we cannot convert is not evidence of invisibility
			}
		}

		@Override
		public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) {
			path.moveTo((float) p0.getX(), (float) p0.getY());
			path.lineTo((float) p1.getX(), (float) p1.getY());
			path.lineTo((float) p2.getX(), (float) p2.getY());
			path.lineTo((float) p3.getX(), (float) p3.getY());
			path.closePath();
		}

		@Override
		public void drawImage(PDImage pdImage) {
			Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
			// unit square mapped through the CTM
			Point2D a = ctm.transformPoint(0, 0);
			Point2D b = ctm.transformPoint(1, 0);
			Point2D c = ctm.transformPoint(1, 1);
			Point2D d = ctm.transformPoint(0, 1);
			double minX = Math.min(Math.min(a.getX(), b.getX()), Math.min(c.getX(), d.getX()));
			double maxX = Math.max(Math.max(a.getX(), b.getX()), Math.max(c.getX(), d.getX()));
			double minY = Math.min(Math.min(a.getY(), b.getY()), Math.min(c.getY(), d.getY()));
			double maxY = Math.max(Math.max(a.getY(), b.getY()), Math.max(c.getY(), d.getY()));
			record("image", new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY));
		}

		@Override
		public void clip(int windingRule) {
			// clipping paths are not layout; drop the pending path
			path = new GeneralPath();
		}

		@Override
		public void moveTo(float x, float y) {
			path.moveTo(x, y);
		}

		@Override
		public void lineTo(float x, float y) {
			path.lineTo(x, y);
		}

		@Override
		public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) {
			path.curveTo(x1, y1, x2, y2, x3, y3);
		}

		@Override
		public Point2D getCurrentPoint() {
			return path.getCurrentPoint() == null ? new Point2D.Float(0, 0) : path.getCurrentPoint();
		}

		@Override
		public void closePath() {
			path.closePath();
		}

		@Override
		public void endPath() {
			path = new GeneralPath();
		}

		@Override
		public void strokePath() {
			record("stroke", path.getBounds2D());
			path = new GeneralPath();
		}

		@Override
		public void fillPath(int windingRule) {
			record("fill", path.getBounds2D());
			path = new GeneralPath();
		}

		@Override
		public void fillAndStrokePath(int windingRule) {
			record("fill", path.getBounds2D());
			path = new GeneralPath();
		}

		@Override
		public void shadingFill(COSName shadingName) {
			// ignore
		}
	}
}
