/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.chart.render.api;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * How each kind of warp sets the lines of a WordArt (CR-083 phase 2), as Word does (phase 0, findings sections 3 to 5):
 * <ul>
 * <li><b>Two paths</b> (33 presets): the ink of the whole block of lines mapped between them ({@link TextWarp}).</li>
 * <li><b>More than two</b>: a band for each line. An even number of paths is that many halves of bands, a pair each
 * ({@code textDeflateInflate}: two; {@code textButtonPour}, {@code textDeflateInflateDeflate}: three), a band's lines
 * mapped into it as two paths map the block, across the whole block's ink and down a range as tall as the tallest
 * band's, centred on their own; {@code textButton}'s three paths are three single paths, a line following each. Line i takes band i; with more lines than bands the first band takes the extra ones
 * ({@code textDeflateInflate} with three lines: the first two in its top band), and with fewer the last bands stay
 * empty, as Word's PDFs show.</li>
 * <li><b>One path</b> (Follow Path: {@code textArchUp}, {@code textArchDown}, {@code textCircle}): each glyph whole,
 * turned to the path, its baseline on the path drawn in toward the text rectangle's centre, the text scaled
 * ({@link #followPath}).</li>
 * </ul>
 *
 * @since 17.3.7
 */
final class WordArtWarps {

	private WordArtWarps() {}

	/**
	 * The glyphs' outlines warped in place (the map's values replaced); for Follow Path, each glyph's scale and the
	 * place and turn its text is drawn at, for its invisible text: {scale, x, y, angle in radians}. Null where no glyph
	 * is placed so.
	 */
	static Map<WordArtRenderer.Glyph, double[]> warp(WordArtRenderer.Layout layout, String name, List<TextWarp.Measured> paths,
			Rectangle2D.Double rect, Map<WordArtRenderer.Glyph, Shape> drawn, List<String> problems) {
		int n = paths.size();
		List<WordArtRenderer.Line> lines = layout.lines;
		if (n == 2) {
			envelope(lines, paths.get(0), paths.get(1), drawn);
			return null;
		}
		Map<WordArtRenderer.Glyph, double[]> placed = new HashMap<WordArtRenderer.Glyph, double[]>();
		if (n == 1) {
			followPath(lines, paths.get(0), rect, drawn, placed, "textCircle".equals(name));
			return placed;
		}
		// more than two: bands, or (textButton) a path for each line
		boolean single = n % 2 == 1;
		int slots = single ? n : n / 2;
		List<List<WordArtRenderer.Line>> byBand = bands(lines, slots);
		Rectangle2D block = ink(lines, drawn);
		double tallest = 0;
		for (List<WordArtRenderer.Line> mine : byBand) {
			Rectangle2D r = ink(mine, drawn);
			if (r != null) tallest = Math.max(tallest, r.getHeight());
		}
		for (int b = 0; b < slots; b++) {
			List<WordArtRenderer.Line> mine = byBand.get(b);
			if (mine.isEmpty()) continue;
			if (single) followPath(mine, paths.get(b), rect, drawn, placed, false);
			else envelope(mine, paths.get(2 * b), paths.get(2 * b + 1), drawn, block, tallest);
		}
		// lines with no band (none drawn): Word draws them nowhere
		return placed;
	}

	/** The lines that have glyphs, given out to the bands: line i to band i, the extra ones to the first */
	static List<List<WordArtRenderer.Line>> bands(List<WordArtRenderer.Line> lines, int bands) {
		List<WordArtRenderer.Line> real = new ArrayList<WordArtRenderer.Line>();
		for (WordArtRenderer.Line l : lines) if (hasInk(l)) real.add(l);
		List<List<WordArtRenderer.Line>> out = new ArrayList<List<WordArtRenderer.Line>>();
		for (int b = 0; b < bands; b++) out.add(new ArrayList<WordArtRenderer.Line>());
		int extra = Math.max(0, real.size() - bands);
		for (int i = 0; i < real.size(); i++) {
			int b = i <= extra ? 0 : i - extra;
			out.get(b).add(real.get(i));
		}
		return out;
	}

	private static boolean hasInk(WordArtRenderer.Line l) {
		for (WordArtRenderer.Glyph g : l.glyphs) if (!g.space && !g.lineBreak && g.outline != null) return true;
		return false;
	}

	/** The lines' ink mapped between the top and the bottom path (section 20.1.9.19) */
	private static void envelope(List<WordArtRenderer.Line> lines, TextWarp.Measured top, TextWarp.Measured bottom,
			Map<WordArtRenderer.Glyph, Shape> drawn) {
		envelope(lines, top, bottom, drawn, null, 0);
	}

	/** The lines' ink, unwarped */
	private static Rectangle2D ink(List<WordArtRenderer.Line> lines, Map<WordArtRenderer.Glyph, Shape> drawn) {
		Rectangle2D ink = null;
		for (WordArtRenderer.Line l : lines) {
			for (WordArtRenderer.Glyph g : l.glyphs) {
				Shape s = drawn.get(g);
				if (s != null) ink = WordArtRenderer.union(ink, tight(s));
			}
		}
		return ink;
	}

	/**
	 * As above; for a band of a multi-path warp (block given), as Word maps it (phase 1-2 findings section 3): the
	 * horizontal fraction across the whole block's ink, and the vertical down a range as tall as the tallest band's
	 * ink, centred on this band's (a 24 pt line in a band beside a 48 pt one is half its height, as in Word's PDF)
	 */
	private static void envelope(List<WordArtRenderer.Line> lines, TextWarp.Measured top, TextWarp.Measured bottom,
			Map<WordArtRenderer.Glyph, Shape> drawn, Rectangle2D block, double tallest) {
		Rectangle2D ink = ink(lines, drawn);
		if (ink == null || ink.getWidth() <= 0 || ink.getHeight() <= 0) return;
		if (block != null) {
			// a band's lines across the block's ink, and down a range as tall as the tallest band's, centred on their own
			double h = Math.max(ink.getHeight(), tallest);
			ink = new Rectangle2D.Double(block.getX(), ink.getCenterY() - h / 2, block.getWidth(), h);
		}
		TextWarp.Mapping m = TextWarp.envelope(ink, top, bottom);
		for (WordArtRenderer.Line l : lines) {
			for (WordArtRenderer.Glyph g : l.glyphs) {
				Shape s = drawn.get(g);
				if (s != null) drawn.put(g, TextWarp.warp(s, m));
			}
		}
	}

	/**
	 * Follow Path, as fitted to phase 0's probes (findings section 4; CR-083 phase 2's record): the text laid out at its
	 * full size along the path, which runs {@link #PATH_ABOVE_BASELINE} of the line's size above the baseline (the
	 * glyphs' tops being on the path's left, the outside of an arch), each glyph whole, its middle where its advances,
	 * measured along the curve {@link #ADVANCE_ABOVE_BASELINE} above the baseline, put it, turned to the baseline there;
	 * a later line further in by the line heights before it; the lines centred on the path's middle by arc length (or
	 * set by their alignment); and then all of it, glyphs and places, scaled about the text rectangle's centre by
	 * {@link #scale}. Word's glyphs and their invisible text are drawn so: within 1 to 2.5 pt of Word's on phase 0's
	 * probes, where a baseline on the path alone was 7 to 9 pt off.
	 */
	private static void followPath(List<WordArtRenderer.Line> lines, TextWarp.Measured path, Rectangle2D.Double rect,
			Map<WordArtRenderer.Glyph, Shape> drawn, Map<WordArtRenderer.Glyph, double[]> placed, boolean circle) {
		List<WordArtRenderer.Line> real = new ArrayList<WordArtRenderer.Line>();
		double widest = 0;
		for (WordArtRenderer.Line l : lines) {
			if (!hasInk(l)) continue;
			real.add(l);
			double lead = 0;
			for (WordArtRenderer.Glyph g : l.glyphs) {
				if (!g.space && !g.lineBreak) {
					lead = g.x;
					break;
				}
			}
			widest = Math.max(widest, l.width() - lead);
		}
		if (real.isEmpty() || path.length <= 0 || widest <= 0) return;
		double length = circle ? path.length / 2 : path.length;
		double[] sk = scales(length, rect.height, widest, real.size());
		double s = sk[0], k = sk[1];
		double cx = rect.getCenterX(), cy = rect.getCenterY();
		// the path drawn in toward the centre by the one-line scale, the glyphs scaled by the text's
		AffineTransform in = new AffineTransform();
		in.translate(cx, cy);
		in.scale(k, k);
		in.translate(-cx, -cy);
		TextWarp.Measured scaled = new TextWarp.Measured(in.createTransformedShape(path.shape()));
		// a later line further in by LINE_STEP of its scaled size, the first further out by OUT_PER_LINE of that a line
		double first = 0;
		for (WordArtRenderer.Glyph g : real.get(0).glyphs) if (g.font != null) first = Math.max(first, g.font.getSize2D());
		double down = -OUT_PER_LINE * (real.size() - 1) * LINE_STEP * first * s;
		for (WordArtRenderer.Line l : real) {
			double size = 0;
			for (WordArtRenderer.Glyph g : l.glyphs) if (g.font != null) size = Math.max(size, g.font.getSize2D());
			TextWarp.Measured base = scaled.offset(-(PATH_ABOVE_BASELINE * size * s + down));
			TextWarp.Measured along = base.offset(ADVANCE_ABOVE_BASELINE * size * s);
			// the line from its first glyph to its last: Word sets Follow Path text without its leading and trailing spaces
			double lead = 0;
			for (WordArtRenderer.Glyph g : l.glyphs) {
				if (!g.space && !g.lineBreak) {
					lead = g.x;
					break;
				}
			}
			double w = (l.width() - lead) * s;
			String align = l.para == null ? "left" : l.para.getAlign();
			double start = "center".equals(align) || "both".equals(align) || "distribute".equals(align) ? (along.length - w) / 2
					: "right".equals(align) ? along.length - w : 0;
			for (WordArtRenderer.Glyph g : l.glyphs) {
				if (g.lineBreak) continue;
				double u = (start + (g.x - lead + g.advance / 2) * s) / along.length;
				double[] p = base.at(u);
				double a = base.angle(u);
				AffineTransform t = new AffineTransform();
				t.translate(p[0], p[1]);
				t.rotate(a);
				t.scale(s, s);
				t.translate(-g.advance / 2, 0);
				if (g.outline != null && !g.space) drawn.put(g, t.createTransformedShape(g.outline));
				double[] o = new double[2];
				t.transform(new double[] { 0, 0 }, 0, o, 0, 1);
				placed.put(g, new double[] { s, o[0], o[1], a });
			}
			down += LINE_STEP * size * s;
		}
	}

	/** Follow Path's lines are this much of their scaled size apart, across the path (fitted: two and three lines, 0.665) */
	static final double LINE_STEP = 0.665;

	/** With more lines, the first is further out by this much of a line's step for each further line (fitted: 0.36) */
	static final double OUT_PER_LINE = 0.36;

	/** How far above the baseline the path runs, a fraction of the line's size (fitted: 0.15) */
	static final double PATH_ABOVE_BASELINE = 0.15;

	/** How far above the baseline the glyphs' advances are measured along, a fraction of the line's size (fitted: 0.2) */
	static final double ADVANCE_ABOVE_BASELINE = 0.2;

	/**
	 * The text's scale on its path and how far the path is drawn in toward the text rectangle's centre, {s, k}, fitted to
	 * phase 0 (findings section 4) and Jason's own WordArt document, by the path's length over the text's width q
	 * (textCircle: half its length) and the rectangle's height over the text's width:
	 * <ul>
	 * <li>k: 0.727 to q = 1.234, then up through 0.745 at 1.65 and 0.8506 at 2.57 ({@link #pathScale}); and in a box
	 * flatter than its text (Word's own form, its height over the text's width h below 0.343) 0.727 + 0.577 (0.343 - h)
	 * where that is more (a long line: h 0.116, Word's 0.858; "WordArt", h 0.343, 0.727);</li>
	 * <li>s: k, but no more than 0.709 q, or that flat term (an arch adjusted to a shorter arc: k 0.73, s 0.394, as Word);
	 * each further line 0.823 of it.</li>
	 * </ul>
	 * The flat term rests on two of Word's lines, and the text's height counts in Word too ("wave", no ascender, 0.793;
	 * "Warpy", descenders, 0.695; the rule 0.80 and 0.77): a round of probes of text lengths and heights would settle both.
	 */
	static double[] scales(double length, double height, double width, int lines) {
		double q = length / width, h = height / width;
		double flat = h < 0.343 ? 0.727 + 0.577 * (0.343 - h) : 0;
		double k = Math.min(1, Math.max(pathScale(q), flat));
		double s = Math.min(k, Math.max(0.709 * q, flat)) * Math.pow(0.823, lines - 1);
		return new double[] { Math.max(0.05, Math.min(1, s)), k };
	}

	/** The text's scale alone (see {@link #scales}) */
	static double scale(double length, double height, double width, int lines) {
		return scales(length, height, width, lines)[0];
	}

	/**
	 * How far the path is drawn in toward the text rectangle's centre, by the path's length over the text's width q:
	 * the text's own scale but for the cap a short arc puts on that (an adjusted arch: the path in by 0.73, the glyphs
	 * by 0.394)
	 */
	static double pathScale(double q) {
		double s;
		if (q <= 1.234) s = 0.727;
		else if (q <= 1.65) s = 0.727 + (q - 1.234) * (0.745 - 0.727) / (1.65 - 1.234);
		else s = 0.745 + (q - 1.65) * (0.8506 - 0.745) / (2.57 - 1.65);
		return Math.min(1, s);
	}

	/** The tight bounds of a shape's outline (Java 11's Path2D bounds hold the control points) */
	static Rectangle2D tight(Shape s) {
		double x0 = Double.POSITIVE_INFINITY, y0 = Double.POSITIVE_INFINITY, x1 = Double.NEGATIVE_INFINITY, y1 = Double.NEGATIVE_INFINITY;
		double[] c = new double[6];
		for (PathIterator it = new FlatteningPathIterator(s.getPathIterator(null), TextWarp.FLATNESS, 16); !it.isDone(); it.next()) {
			int t = it.currentSegment(c);
			if (t == PathIterator.SEG_MOVETO || t == PathIterator.SEG_LINETO) {
				x0 = Math.min(x0, c[0]);
				y0 = Math.min(y0, c[1]);
				x1 = Math.max(x1, c[0]);
				y1 = Math.max(y1, c[1]);
			}
		}
		return x0 > x1 ? null : new Rectangle2D.Double(x0, y0, x1 - x0, y1 - y0);
	}
}
