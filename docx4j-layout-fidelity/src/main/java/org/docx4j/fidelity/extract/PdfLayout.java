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

		public String key() {
			return text;
		}

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
