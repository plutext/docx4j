package org.docx4j.fidelity.compare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.fidelity.extract.PdfLayout;
import org.docx4j.fidelity.extract.PdfLayout.Line;

/**
 * Level-1 comparison: pair up text lines between the reference (Word) and the
 * candidate (docx4j+FOP) by their text, in document order, then report
 * line-break parity, page-break parity and baseline / start-x deltas.
 *
 * Pairing is a longest-common-subsequence over the line texts, so a paragraph
 * broken differently shows up as unmatched lines on both sides rather than as
 * a cascade of wrong pairs.
 */
public final class LayoutComparison {

	public static final class Pair {
		public Line ref, cand;
		public double dy, dx;
		public boolean samePage;
		/** Paired by the windowed pass rather than by the LCS. @see #WINDOW */
		public boolean windowed;
		/** Paired by the merging pass: this line is the other side's two or more, up to
		 *  {@link LayoutComparison#MERGE_MAX}.
		 *  @see LayoutComparison#MERGE */
		public boolean merged;
	}

	public static final class Result {
		public String id;
		public int refPages, candPages;
		public int refLines, candLines;
		public int matched, matchedSamePage;
		/** Of {@link #matched}, how many the windowed pass recovered. */
		public int windowed;
		/** How many pairs the merging pass formed by concatenation.  Counted apart from
		 *  {@link #windowed} because it is the one pass that can hide a defect rather than
		 *  only fail to pair: see {@link LayoutComparison#MERGE}. */
		public int merged;
		public String firstDivergence = "";
		public double medianDy, maxDy, medianDx, maxDx;
		/** Each side's own line pitch. @see LayoutComparison#linePitch */
		public double refPitch, candPitch;
		public final List<Pair> pairs = new ArrayList<>();
		public final List<Line> refOnly = new ArrayList<>();
		public final List<Line> candOnly = new ArrayList<>();
		public List<PixelComparison.PageDiff> pixels = new ArrayList<>();

		public double lineParity() {
			return refLines == 0 ? 1.0 : (double) matched / refLines;
		}

		public double pageParity() {
			return refLines == 0 ? 1.0 : (double) matchedSamePage / refLines;
		}

		public double worstPixelRatio() {
			double w = 0;
			for (PixelComparison.PageDiff p : pixels) w = Math.max(w, p.ratio);
			return w;
		}
	}

	private LayoutComparison() {}

	/**
	 * How many pages either side of where the matched lines say a reference page
	 * landed the <b>windowed pass</b> will look for a line the LCS could not pair;
	 * 0 turns the pass off and leaves the plain LCS.
	 *
	 * <p>The LCS is the longest common subsequence, so it already pairs as many
	 * lines as any <em>monotone</em> pairing can.  What it cannot do is pair two
	 * lines the two renders put in the opposite order, and one pair out of order
	 * costs two lines however identical they are.  A document whose cells repeat
	 * short strings - a test procedure with hundreds of {@code "Function"},
	 * {@code "Operator action"}, {@code "Expected result"} rows - is where that
	 * bites: once the two sides differ by a page the subsequence can bind only one
	 * occurrence of each repeated string on the pages either side of the drift and
	 * drops the rest, although every one of them is painted where Word paints it.
	 * Measured on {@code 12_ru-RU_fields1_num_tbl_13383}: 177 of its 232 unmatched
	 * reference lines have an unmatched candidate line with the same text at the
	 * same x.
	 *
	 * <p>So after the LCS, every reference line it left unmatched is offered the
	 * unmatched candidate lines with the same text, and takes the nearest one that is
	 * <b>in the very place it should be</b>: on the page the matched lines around it
	 * say this reference page landed on (plus {@link #WINDOW_PAGES} pages of slack,
	 * none by default), starting within {@link #WINDOW_X_PT} of the same x, and on a
	 * baseline within {@link #WINDOW_Y_PT}.  The page map is built from the LCS's own
	 * pairs - the median candidate page of the lines matched on each reference page,
	 * carried across pages that have none - so the window follows the document's real
	 * pagination rather than assuming the two agree.
	 *
	 * <p>This is the one rule in the harness that pairs out of document order, and
	 * the tolerances are what keep it honest: at 2pt of x and 3pt of baseline the
	 * pair is the same line in the same place on the same page, differing only in
	 * which of the two the reading order reaches first - the very disagreement the
	 * extractor's own {@code ROW_TOLERANCE_PT} row ordering settles where it can see
	 * it.  A line we put on another page, at another indent or another height, or did
	 * not paint at all, is outside the window and stays unmatched.  <b>Nothing that is
	 * laid out differently is rescued.</b>
	 *
	 * <p>Measured over the three corpora on the b61-labelascent renders (a rescore, so
	 * the only thing that changed is the pairing).  The looser windows were measured
	 * and rejected: a page of slack lets a <em>running page number</em> pair with the
	 * neighbouring page's, which is exactly the defect a document whose numbering is
	 * one out should be scored for (12_en-US_fields1_num_tbl_4899: 121 lines rescued
	 * at one page of slack against 16 at none, 80 of them its page numbers), and no
	 * baseline bound lets a line pair with a copy of itself 600pt up the same page
	 * (12_ru-RU_fields1_num_tbl_13383, 0.9462 against 0.9352).  3pt buys all but a
	 * handful of what no bound does and buys nothing that moved.
	 *
	 * <p>{@code -Dfidelity.window=false} restores the pure LCS;
	 * {@code -Dfidelity.windowPages=}, {@code -Dfidelity.windowXPt=} and
	 * {@code -Dfidelity.windowYPt=} override the tolerances.
	 *
	 * @since 17.1.1
	 */
	private static final boolean WINDOW =
			!"false".equalsIgnoreCase(System.getProperty("fidelity.window", "true"));

	/** Extra pages of slack around the expected page. @see #WINDOW */
	private static final int WINDOW_PAGES =
			Integer.getInteger("fidelity.windowPages", 0);

	/** How far apart two windowed lines' start x may be, in points. @see #WINDOW */
	private static final double WINDOW_X_PT =
			Double.parseDouble(System.getProperty("fidelity.windowXPt", "2"));

	/** How far apart two windowed lines' baselines may be, in points; 0 or less does
	 *  not constrain them. @see #WINDOW */
	private static final double WINDOW_Y_PT =
			Double.parseDouble(System.getProperty("fidelity.windowYPt", "3"));

	public static Result compare(String id, PdfLayout ref, PdfLayout cand) {
		Result r = new Result();
		r.id = id;
		r.refPages = ref.pageCount();
		r.candPages = cand.pageCount();
		r.refLines = ref.lines.size();
		r.candLines = cand.lines.size();

		List<Line> a = ref.lines;
		List<Line> b = cand.lines;
		int n = a.size(), m = b.size();
		int[][] lcs = new int[n + 1][m + 1];
		for (int i = n - 1; i >= 0; i--) {
			for (int j = m - 1; j >= 0; j--) {
				lcs[i][j] = a.get(i).key().equals(b.get(j).key()) ? lcs[i + 1][j + 1] + 1
						: Math.max(lcs[i + 1][j], lcs[i][j + 1]);
			}
		}
		int i = 0, j = 0;
		/* Divergences are recorded in walk order and read afterwards: the windowed
		 * pass may yet pair the line this one names. */
		List<Object[]> divergences = new ArrayList<>();
		while (i < n && j < m) {
			if (a.get(i).key().equals(b.get(j).key())) {
				Pair p = new Pair();
				p.ref = a.get(i);
				p.cand = b.get(j);
				p.samePage = p.ref.page == p.cand.page;
				p.dy = p.cand.y - p.ref.y;
				p.dx = p.cand.x0 - p.ref.x0;
				r.pairs.add(p);
				r.matched++;
				if (p.samePage) {
					r.matchedSamePage++;
				} else {
					divergences.add(new Object[] { null, String.format(
							"page break: ref p%d / cand p%d: \"%s\"",
							p.ref.page + 1, p.cand.page + 1, abbreviate(p.ref.text)) });
				}
				i++;
				j++;
			} else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
				Line l = a.get(i);
				divergences.add(new Object[] { l, String.format(
						"line break: ref p%d line has no match: \"%s\"",
						l.page + 1, abbreviate(l.text)) });
				r.refOnly.add(l);
				i++;
			} else {
				Line l = b.get(j);
				divergences.add(new Object[] { l, String.format(
						"line break: cand p%d line has no match: \"%s\"",
						l.page + 1, abbreviate(l.text)) });
				r.candOnly.add(l);
				j++;
			}
		}
		while (i < n) r.refOnly.add(a.get(i++));
		while (j < m) r.candOnly.add(b.get(j++));

		merge(r, a, b);
		window(r);

		java.util.Set<Line> unmatched = Collections.newSetFromMap(new java.util.IdentityHashMap<>());
		unmatched.addAll(r.refOnly);
		unmatched.addAll(r.candOnly);
		for (Object[] d : divergences) {
			if (d[0] == null || unmatched.contains(d[0])) {
				r.firstDivergence = (String) d[1];
				break;
			}
		}
		if (r.firstDivergence.isEmpty() && r.refPages != r.candPages) {
			r.firstDivergence = "page count differs";
		}

		List<Double> dys = new ArrayList<>(), dxs = new ArrayList<>();
		for (Pair p : r.pairs) {
			if (p.samePage) {
				dys.add(p.dy);
				dxs.add(p.dx);
			}
		}
		r.medianDy = median(dys);
		r.maxDy = maxAbs(dys);
		r.medianDx = median(dxs);
		r.maxDx = maxAbs(dxs);
		r.refPitch = linePitch(ref);
		r.candPitch = linePitch(cand);
		return r;
	}

	/**
	 * One extraction's <b>line pitch</b>: the median baseline gap between consecutive
	 * extracted lines on one page, counting only pairs that are stacked one above the
	 * other - a pair that sits <b>side by side</b>, the later beginning at or after the
	 * earlier ends, has no line pitch between it at all.
	 *
	 * <p>Without that exclusion the number is not a line pitch on any document with a
	 * table in it, and the triage ledgers have twice read a defect out of it that was not
	 * there.  The cells of one row are consecutive extracted lines whose baselines are a
	 * fraction of a point apart (Word usually writes them at exactly the same baseline),
	 * so on a table-heavy document they are the <em>majority</em> of the consecutive
	 * pairs and they drag the median to nothing.  Measured on the b70-batch45 renders,
	 * ours against Word's:
	 *
	 * <table><caption>median baseline gap, Word / ours / the ratio</caption>
	 * <tr><th>document</th><th>every consecutive pair</th><th>stacked pairs only</th></tr>
	 * <tr><td>{@code 15_en-AU_sdt_num_tbl_11398}</td><td>0.000 / 0.277 - Word's own
	 *     median is <b>zero</b></td><td>13.700 / 13.799 = 1.007</td></tr>
	 * <tr><td>{@code 15_en-AU_sdt_num_tbl_11783}</td><td>7.710 / 3.943 = 0.511</td>
	 *     <td>12.960 / 12.649 = 0.976</td></tr>
	 * <tr><td>{@code 14_en-AU_tbl_174}</td><td>6.240 / 4.427 = 0.709</td>
	 *     <td>9.600 / 9.174 = 0.956</td></tr>
	 * <tr><td>{@code 14_fr-FR_num_tbl_7235}</td><td>4.560 / 4.349 = 0.954</td>
	 *     <td>10.320 / 10.349 = 1.003</td></tr>
	 * <tr><td>{@code 16_en-AU_num_tbl_13118}</td><td>1.920 / 1.835 = 0.956</td>
	 *     <td>9.150 / 9.199 = 1.005</td></tr>
	 * <tr><td>{@code 15_en-US_sdt_num_tbl_13743}</td><td>11.040 / 10.986 = 0.995</td>
	 *     <td>12.000 / 11.736 = 0.978</td></tr>
	 * <tr><td>{@code 16_hu-HU_sdt_tbl_2065}</td><td>15.600 / 15.442 = 0.990</td>
	 *     <td>15.600 / 15.442 = 0.990 - a document with no such rows does not move</td></tr>
	 * </table>
	 *
	 * <p>Dropping only the pairs under a point - the correction the ledgers have been
	 * making by hand - is <b>not enough</b>: it leaves 7235 at 8.400 against 10.349, a
	 * 23% error read as a line-box defect, because that document's row cells sit more
	 * than a point apart on Word's side as well.  The test is the geometry, not a
	 * threshold, and it needs no number.
	 *
	 * <p>It is applied to both extractions alike, and it is a reported statistic only: no
	 * pair is made or lost by it.
	 *
	 * @since 17.1.1
	 */
	public static double linePitch(PdfLayout l) {
		List<Double> gaps = new ArrayList<>();
		for (int i = 1; i < l.lines.size(); i++) {
			Line a = l.lines.get(i - 1), b = l.lines.get(i);
			if (a.page != b.page) continue;
			if (b.x0 >= a.x1 - X_EPSILON_PT) continue;    // side by side: not a line pitch
			gaps.add(Math.abs(b.y - a.y));
		}
		return median(gaps);
	}

	/** How much two x may differ and still count as the same, in points. */
	private static final double X_EPSILON_PT = 0.01;

	/**
	 * Whether a line one render's extractor read as one is paired with the several the
	 * other's read in the same place (CR-001 batch 44 step 1, option (b); on by default
	 * since that batch, and the javadoc's "EXPERIMENTAL, off by default" was left behind
	 * by it).
	 *
	 * <p>The two extractors read the same ink; what they can disagree about is where
	 * one line ends and the next begins, and the disagreement is not always resolvable
	 * by a rule applied to both sides - a list label painted on its own baseline, a
	 * fraction of a line above the text it labels, is one line on the side that put it
	 * on the text's baseline and two on the side that did not, whatever threshold
	 * either side is read with.  This pass pairs the one with the concatenation of the
	 * others when they are consecutive, unmatched, on the page the surrounding matches
	 * say this one landed on, start at the same x ({@link #MERGE_X_PT}), number no more
	 * than {@link #MERGE_MAX}, and lie within {@link #MERGE_Y_EM} - or
	 * {@link #MERGE_Y_ROW_EM} where they are a row - of one another vertically.  Both
	 * bounds are under the 1.15 em a single-spaced line pitch is, so two lines of one
	 * paragraph can never be merged into Word's one.  It runs in both directions.
	 *
	 * <p>{@code -Dfidelity.merge=true} turns it on; {@code -Dfidelity.mergeMax=},
	 * {@code -Dfidelity.mergeYEm=}, {@code -Dfidelity.mergeYRowEm=},
	 * {@code -Dfidelity.mergeYPt=} and {@code -Dfidelity.mergeXPt=} override the
	 * bounds (CR-001 batch 46 item 6 settled each of the four by measurement).
	 */
	private static final boolean MERGE =
			!"false".equalsIgnoreCase(System.getProperty("fidelity.merge", "true"));

	/**
	 * How many lines of one side may be paired with one of the other.
	 *
	 * <p>Three when the pass shipped (CR-001 batch 44), which is a bullet and its text,
	 * or a label, its text and a continuation - but not a table row, which has as many
	 * pieces as the table has columns.  Measured on {@code 14_en-AU_tbl_174}, whose
	 * page-1 row Word reads as one line at x 86.42..681.13 and our extractor reads as
	 * <b>five</b> at x 86.25 / 223.90 / 358.20 / 506.45 / 642.05: at three the row
	 * cannot pair at all.  Rows longer than five exist too, and <b>six</b> is the widest
	 * on the board: a rating scale beside its question on
	 * {@code 16_fr-CA_sdt_num_tbl_3640}, six pieces; and a six-cell table heading on one
	 * baseline at 12pt on {@code 12_en-US_sdt_fields1_num_tbl_4957}.
	 *
	 * <p>So six, and the number is a measurement rather than a margin.  Measured at 5, 6,
	 * 8, 12 and 20 over the three corpora, against {@code b70-batch45}'s own renders:
	 * five is worth +55 matched lines, <b>six +60</b>, eight +62, twelve exactly what
	 * eight is, twenty +64.  Everything six buys over five is a row.  Nothing above six
	 * is: the seven, eight, fifteen and twenty-piece runs are all one document's
	 * letter-spaced diagram, {@code 15_es-AR_sdt_num_tbl_12301} painting single digits on
	 * one baseline (4643 of its lines are three characters or fewer), which is a grouping
	 * disagreement of its own - triage class P1, 1657 lines - and not a row.  It gains
	 * two such runs at eight and four at twenty; raise this number if that class is what
	 * is wanted, but it is not what this pass is for.
	 *
	 * <p>No document loses a pair at any of the five settings, and no line or page count
	 * moves - the pass renders nothing.
	 *
	 * @see #MERGE
	 */
	private static final int MERGE_MAX = Integer.getInteger("fidelity.mergeMax", 6);

	/** How far apart the merged pieces' baselines may lie, in ems, where the pieces
	 *  are stacked - each overlapping the one before it horizontally, as a label above
	 *  its text does. @see #MERGE_Y_ROW_EM @see #MERGE */
	private static final double MERGE_Y_EM =
			Double.parseDouble(System.getProperty("fidelity.mergeYEm", "0.5"));

	/**
	 * How far apart the merged pieces' baselines may lie, in ems, where each piece
	 * begins at or after the end of the one before it - a <b>row</b>, the cells of which
	 * sit side by side and need share no baseline at all.
	 *
	 * <p>Half an em (the stacked bound) is too tight for a row: measured over the three
	 * corpora, 22 rows which one extractor reads as one line and the other as two lie
	 * <b>0.52 to 0.97 em</b> apart - a two-column contract on {@code 14_en-US_tbl_394}
	 * at 0.53 em, a table on {@code 15_de-DE_sdt_79} at 0.54 to 0.66, a letterhead cell
	 * on {@code 14_en-US_tbl_2564} and {@code 14_en-US_tbl_10224} at 0.97 - and none of
	 * them pairs at 0.5.  At one em all 22 pair, worth +34 matched lines over the three
	 * corpora and costing none.
	 *
	 * <p>The bound is separate from {@link #MERGE_Y_EM} rather than simply raised,
	 * because what keeps the pass honest is different in the two shapes.  Two lines of
	 * one paragraph are <em>stacked</em>: they start at the same x and overlap, so no
	 * row ever looks like one, and the row bound cannot collapse a paragraph however
	 * wide it is.  Stacked pieces keep the tighter bound, and the measurement says what
	 * that buys: at one em the stacked bound would also have merged a check-box glyph on
	 * {@code 14_en-GB_num_tbl_4083} which we paint <b>9.86pt (0.99 em) above</b> the
	 * label Word paints it beside - one pair gained by hiding a displacement of very
	 * nearly a whole line, which is the one thing this pass must not do.
	 *
	 * @see #MERGE
	 */
	private static final double MERGE_Y_ROW_EM =
			Double.parseDouble(System.getProperty("fidelity.mergeYRowEm", "1.0"));

	/** The floor on {@link #MERGE_Y_EM} and {@link #MERGE_Y_ROW_EM}, in points.
	 *  @see #MERGE */
	private static final double MERGE_Y_PT =
			Double.parseDouble(System.getProperty("fidelity.mergeYPt", "3"));

	/**
	 * How far apart the run's first piece and the single line may start, in points;
	 * 0 or less does not constrain them.
	 *
	 * <p>Two points, which is {@link #WINDOW_X_PT}'s value and was {@link #WINDOW_X_PT}
	 * itself until this constant was split out: the merge pass had been borrowing the
	 * windowed pass's bound, so {@code -Dfidelity.windowXPt=} silently moved it too.
	 *
	 * <p>Simply widening it buys nothing: over the six documents whose rows the merge
	 * pass is at its limit on (174, 13118, 11398, 7046, 5123, 11783) six points and
	 * twelve points are each worth <b>not one</b> extra pair.  Simply dropping it
	 * <b>loses</b> a pair on 174 - with no bound at all a run elsewhere on the page can
	 * score better, the score being dominated by the baseline distance, and take pieces
	 * the right run needed (962 matched with the bound, 961 without; 975 against 974 at
	 * {@code mergeMax=5}).
	 *
	 * <p>Dropping it is worth a great deal more than the piece count is - over the three
	 * corpora, +78 matched lines on 27 documents - and what it reaches is measurably the
	 * same line: on {@code 15_it-IT_num_tbl_11741}, 23 runs of a bold label and its text
	 * <b>on one baseline</b> (spread 0.00pt) which our extractor splits and Word's does
	 * not; on {@code 14_en-GB_num_tbl_4083}, a date box Word paints in three pieces on
	 * one baseline.  It is not taken, and the reason is not the arithmetic: a run whose
	 * first piece starts elsewhere is a run <b>at another indent</b>, and this bound is
	 * where the merge pass says so - {@code LayoutComparisonMergeTest}'s
	 * {@code piecesAtAnotherIndentDoNotMerge} is that statement.  Relaxing it is a
	 * change to what the metric counts as the same line, not a widening of this pass,
	 * and belongs to whoever decides that.
	 *
	 * @see #MERGE
	 */
	private static final double MERGE_X_PT =
			Double.parseDouble(System.getProperty("fidelity.mergeXPt", "2"));

	/** Prints every pair the merging pass makes, for reading one document. @see #MERGE */
	private static final boolean MERGE_DUMP =
			Boolean.parseBoolean(System.getProperty("fidelity.mergeDump", "false"));

	/**
	 * The merging pass: pairs an unmatched line of one side with the consecutive
	 * unmatched lines of the other whose text concatenates to its own.
	 *
	 * @see #MERGE
	 */
	private static void merge(Result r, List<Line> a, List<Line> b) {
		if (!MERGE || MERGE_MAX < 2) return;
		int[] expected = pageMap(r);
		// ref line <- several candidate lines
		mergeOneWay(r, b, r.candOnly, r.refOnly, expected, false);
		// candidate line <- several reference lines
		mergeOneWay(r, a, r.refOnly, r.candOnly, expected, true);
	}

	/**
	 * Pairs each line of {@code singles} with a run of consecutive lines of
	 * {@code pieces}, which are drawn from the full list {@code all}.
	 *
	 * @param refIsPieces whether it is the reference side that is being merged, in
	 *                    which case the run's length is how many reference lines the
	 *                    pair matches
	 */
	private static void mergeOneWay(Result r, List<Line> all, List<Line> pieces, List<Line> singles,
			int[] expected, boolean refIsPieces) {
		if (pieces.isEmpty() || singles.isEmpty()) return;
		java.util.Set<Line> free = Collections.newSetFromMap(new java.util.IdentityHashMap<>());
		free.addAll(pieces);
		// every run of 2..MERGE_MAX consecutive unmatched lines, by the text it makes
		Map<String, List<int[]>> runs = new HashMap<>();
		for (int i = 0; i < all.size(); i++) {
			Line first = all.get(i);
			if (!free.contains(first)) continue;
			StringBuilder spaced = new StringBuilder(Line.mergePiece(first));
			StringBuilder tight = new StringBuilder(Line.mergePiece(first));
			// a run is a row while every piece so far begins at or after the end of the
			// one before it; one piece that overlaps its predecessor makes it stacked,
			// and stays stacked, for the rest of the run
			boolean row = true;
			double prevX1 = first.x1;
			for (int k = 2; k <= MERGE_MAX && i + k - 1 < all.size(); k++) {
				Line next = all.get(i + k - 1);
				if (!free.contains(next) || next.page != first.page) break;
				row &= next.x0 >= prevX1;
				prevX1 = next.x1;
				double em = row ? Math.max(MERGE_Y_EM, MERGE_Y_ROW_EM) : MERGE_Y_EM;
				double tol = Math.max(MERGE_Y_PT, em * Math.max(next.size, first.size));
				if (Math.abs(next.y - first.y) > tol) break;
				spaced.append(' ').append(Line.mergePiece(next));
				tight.append(Line.mergePiece(next));
				// the whole-line normalisations run on the joined run, not on each piece:
				// the bullet rule reads the head of a line (Line.mergePiece, Line.joinedKey)
				String spacedKey = Line.joinedKey(spaced.toString());
				String tightKey = Line.joinedKey(tight.toString());
				runs.computeIfAbsent(spacedKey, s -> new ArrayList<>()).add(new int[] { i, k });
				if (!tightKey.equals(spacedKey)) {
					runs.computeIfAbsent(tightKey, s -> new ArrayList<>()).add(new int[] { i, k });
				}
			}
		}
		if (runs.isEmpty()) return;
		java.util.Set<Line> taken = Collections.newSetFromMap(new java.util.IdentityHashMap<>());
		List<Line> kept = new ArrayList<>();
		for (Line l : singles) {
			List<int[]> cs = runs.get(l.key());
			int[] best = null;
			double bestScore = 0;
			if (cs != null) {
				for (int[] c : cs) {
					Line first = all.get(c[0]);
					int want = refIsPieces ? l.page : (l.page < expected.length ? expected[l.page] : l.page);
					int dp = refIsPieces ? Math.abs(expectedOf(expected, first.page) - l.page)
							: Math.abs(first.page - want);
					if (dp > WINDOW_PAGES) continue;
					double dx = Math.abs(first.x0 - l.x0);
					if (MERGE_X_PT > 0 && dx > MERGE_X_PT) continue;
					boolean clash = false;
					for (int q = 0; q < c[1]; q++) {
						if (taken.contains(all.get(c[0] + q))) clash = true;
					}
					if (clash) continue;
					double score = dp * 1e6 + Math.abs(first.y - l.y) * 1e3 + dx;
					if (best == null || score < bestScore) {
						best = c;
						bestScore = score;
					}
				}
			}
			if (best == null) {
				kept.add(l);
				continue;
			}
			Line first = all.get(best[0]);
			for (int q = 0; q < best[1]; q++) taken.add(all.get(best[0] + q));
			if (MERGE_DUMP) {
				System.out.println("  merge " + (refIsPieces ? "cand<-ref " : "ref<-cand ") + best[1]
						+ ": \"" + abbreviate(l.text) + "\"");
				for (int q = 0; q < best[1]; q++) System.out.println("      " + all.get(best[0] + q));
			}
			Pair p = new Pair();
			p.ref = refIsPieces ? first : l;
			p.cand = refIsPieces ? l : first;
			p.merged = true;
			p.samePage = p.ref.page == p.cand.page;
			p.dy = p.cand.y - p.ref.y;
			p.dx = p.cand.x0 - p.ref.x0;
			r.pairs.add(p);
			// lineParity counts reference lines, so a reference line split in two is
			// worth the two it was split into
			int worth = refIsPieces ? best[1] : 1;
			r.matched += worth;
			r.merged++;
			if (p.samePage) r.matchedSamePage += worth;
		}
		singles.clear();
		singles.addAll(kept);
		pieces.removeIf(taken::contains);
	}

	/** Which candidate page a reference page landed on; the identity past the map. */
	private static int expectedOf(int[] expected, int refPage) {
		return refPage >= 0 && refPage < expected.length ? expected[refPage] : refPage;
	}

	/**
	 * The windowed pass: pairs a reference line the LCS left unmatched with an
	 * unmatched candidate line of the same text, where the candidate is on the page
	 * the surrounding matches say it should be on and starts at the same x.
	 *
	 * @see #WINDOW
	 */
	private static void window(Result r) {
		if (!WINDOW || r.refOnly.isEmpty() || r.candOnly.isEmpty()) return;
		int[] expected = pageMap(r);
		Map<String, List<Line>> pool = new HashMap<>();
		for (Line l : r.candOnly) pool.computeIfAbsent(l.key(), k -> new ArrayList<>()).add(l);
		List<Line> refKept = new ArrayList<>();
		java.util.Set<Line> taken = Collections.newSetFromMap(new java.util.IdentityHashMap<>());
		for (Line l : r.refOnly) {
			List<Line> candidates = pool.get(l.key());
			Line best = null;
			double bestScore = 0;
			if (candidates != null) {
				int want = l.page < expected.length ? expected[l.page] : l.page;
				for (Line c : candidates) {
					if (taken.contains(c)) continue;
					int dp = Math.abs(c.page - want);
					if (dp > WINDOW_PAGES) continue;
					double dx = Math.abs(c.x0 - l.x0);
					if (dx > WINDOW_X_PT) continue;
					double dy = Math.abs(c.y - l.y);
					if (WINDOW_Y_PT > 0 && dy > WINDOW_Y_PT) continue;
					// the nearest candidate wins: a string a page repeats (a table
					// heading, a running footer) must pair with the copy in its own row
					double score = dp * 1e6 + dy * 1e3 + dx;
					if (best == null || score < bestScore) {
						best = c;
						bestScore = score;
					}
				}
			}
			if (best == null) {
				refKept.add(l);
				continue;
			}
			taken.add(best);
			Pair p = new Pair();
			p.ref = l;
			p.cand = best;
			p.windowed = true;
			p.samePage = l.page == best.page;
			p.dy = best.y - l.y;
			p.dx = best.x0 - l.x0;
			r.pairs.add(p);
			r.matched++;
			r.windowed++;
			if (p.samePage) r.matchedSamePage++;
		}
		r.refOnly.clear();
		r.refOnly.addAll(refKept);
		r.candOnly.removeIf(taken::contains);
	}

	/**
	 * Which candidate page each reference page landed on, from the LCS's own pairs:
	 * the median candidate page of the lines matched on that reference page, carried
	 * forward across reference pages that matched nothing.
	 */
	private static int[] pageMap(Result r) {
		int pages = Math.max(r.refPages, 1);
		List<List<Integer>> seen = new ArrayList<>();
		for (int p = 0; p < pages; p++) seen.add(new ArrayList<>());
		for (Pair p : r.pairs) {
			if (p.ref.page >= 0 && p.ref.page < pages) seen.get(p.ref.page).add(p.cand.page);
		}
		int[] out = new int[pages];
		int carry = 0;
		for (int p = 0; p < pages; p++) {
			List<Integer> v = seen.get(p);
			if (v.isEmpty()) {
				// no evidence on this page: the last page's offset, applied here
				out[p] = p + carry;
			} else {
				Collections.sort(v);
				out[p] = v.get(v.size() / 2);
				carry = out[p] - p;
			}
		}
		// pages before the first evidence take the first offset that was found
		for (int p = 0; p < pages; p++) {
			if (!seen.get(p).isEmpty()) {
				int off = out[p] - p;
				for (int q = 0; q < p; q++) out[q] = q + off;
				break;
			}
		}
		return out;
	}

	private static String abbreviate(String s) {
		return s.length() > 60 ? s.substring(0, 57) + "..." : s;
	}

	static double median(List<Double> v) {
		if (v.isEmpty()) return 0;
		List<Double> s = new ArrayList<>(v);
		Collections.sort(s);
		return s.get(s.size() / 2);
	}

	static double maxAbs(List<Double> v) {
		double m = 0;
		for (double d : v) m = Math.max(m, Math.abs(d));
		return m;
	}
}
