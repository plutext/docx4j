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
	}

	public static final class Result {
		public String id;
		public int refPages, candPages;
		public int refLines, candLines;
		public int matched, matchedSamePage;
		/** Of {@link #matched}, how many the windowed pass recovered. */
		public int windowed;
		public String firstDivergence = "";
		public double medianDy, maxDy, medianDx, maxDx;
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
		return r;
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
