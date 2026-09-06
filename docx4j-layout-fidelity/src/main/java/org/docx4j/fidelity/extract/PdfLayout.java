package org.docx4j.fidelity.extract;

import java.util.ArrayList;
import java.util.List;

/**
 * A PDF reduced to what layout comparison needs: per page, the text lines
 * (baseline, start/end x, text, font, size) and the line-art / image boxes.
 * All coordinates are in points with the origin at the top-left of the page.
 */
public final class PdfLayout {

	public static final class Line {
		public int page;          // 0-based
		public double y;          // baseline
		public double x0, x1;
		public double size;       // font size in pt of the first glyph
		public String font;
		public String text;       // whitespace-collapsed

		/**
		 * The text a line is paired on.  Runs of leader characters are collapsed to a
		 * single token, with the whitespace around them, so that a dot leader whose dots
		 * differ by one - which is what a phase difference of less than one dot's advance
		 * comes to - still pairs.  Word writes {@code "1. Scope ...... 5"} where the dots
		 * start on the reference area's grid; docx4j's start on the text, so the two
		 * extract as {@code "duty ...... 5"} and {@code "duty......5"} although the
		 * geometry agrees to 0.2pt.  Both sides go through this, so nothing is hidden
		 * that is not equally hidden on Word's side.
		 *
		 * <p>Measured over the corpora: worth up to +0.085 of line parity on a
		 * table-of-contents-heavy document.  {@code -Dfidelity.leaderNormalise=false}
		 * restores the raw text.</p>
		 */
		public String key() {
			// cached: the LCS asks every reference line for its key against every
			// candidate line, so a 43,000-line document would run the regex 1.8 billion
			// times (measured: the biggest corpus document stopped finishing at all)
			String k = key;
			if (k == null) {
				k = NORMALISE_LEADERS ? LEADER_RUN.matcher(text).replaceAll("\u2026") : text;
				key = k;
			}
			return k;
		}

		private String key;

		/** Three or more leader glyphs (dot, middle dot, underscore), however spaced,
		 *  together with the whitespace on either side of the run. */
		private static final java.util.regex.Pattern LEADER_RUN = java.util.regex.Pattern
				.compile("\\s*[.\u00b7\u2027_]\\s*(?:[.\u00b7\u2027_]\\s*){2,}");

		private static final boolean NORMALISE_LEADERS =
				!"false".equalsIgnoreCase(System.getProperty("fidelity.leaderNormalise", "true"));

		@Override
		public String toString() {
			return String.format("p%d y=%.2f x=%.2f..%.2f %s %.1fpt \"%s\"", page + 1, y, x0, x1, font, size, text);
		}
	}

	public static final class Box {
		public int page;
		public double x, y, w, h;
		public String kind;       // stroke | fill | image

		@Override
		public String toString() {
			return String.format("p%d %s x=%.2f y=%.2f w=%.2f h=%.2f", page + 1, kind, x, y, w, h);
		}
	}

	public final List<Double> pageWidths = new ArrayList<>();
	public final List<Double> pageHeights = new ArrayList<>();
	public final List<Line> lines = new ArrayList<>();
	public final List<Box> boxes = new ArrayList<>();

	public int pageCount() {
		return pageWidths.size();
	}

	public List<Line> linesOnPage(int page) {
		List<Line> out = new ArrayList<>();
		for (Line l : lines) {
			if (l.page == page) out.add(l);
		}
		return out;
	}
}
