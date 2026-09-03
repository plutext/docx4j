package org.docx4j.fidelity.compare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	}

	public static final class Result {
		public String id;
		public int refPages, candPages;
		public int refLines, candLines;
		public int matched, matchedSamePage;
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
		List<Double> dys = new ArrayList<>(), dxs = new ArrayList<>();
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
					dys.add(p.dy);
					dxs.add(p.dx);
				} else if (r.firstDivergence.isEmpty()) {
					r.firstDivergence = String.format("page break: ref p%d / cand p%d: \"%s\"",
							p.ref.page + 1, p.cand.page + 1, abbreviate(p.ref.text));
				}
				i++;
				j++;
			} else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
				if (r.firstDivergence.isEmpty()) {
					r.firstDivergence = String.format("line break: ref p%d line has no match: \"%s\"",
							a.get(i).page + 1, abbreviate(a.get(i).text));
				}
				r.refOnly.add(a.get(i));
				i++;
			} else {
				if (r.firstDivergence.isEmpty()) {
					r.firstDivergence = String.format("line break: cand p%d line has no match: \"%s\"",
							b.get(j).page + 1, abbreviate(b.get(j).text));
				}
				r.candOnly.add(b.get(j));
				j++;
			}
		}
		while (i < n) r.refOnly.add(a.get(i++));
		while (j < m) r.candOnly.add(b.get(j++));
		if (r.firstDivergence.isEmpty() && r.refPages != r.candPages) {
			r.firstDivergence = "page count differs";
		}

		r.medianDy = median(dys);
		r.maxDy = maxAbs(dys);
		r.medianDx = median(dxs);
		r.maxDx = maxAbs(dxs);
		return r;
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
