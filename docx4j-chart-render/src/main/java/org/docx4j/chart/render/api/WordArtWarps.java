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
import java.util.function.Function;

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
 * turned to the path, its baseline just inside the path drawn in toward the text rectangle's centre by the text's ink,
 * the text scaled with it ({@link #followPath}).</li>
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
		return warp(layout, name, paths, rect, drawn, problems, null);
	}

	/**
	 * As above; frame, where given, the rule for what a set of lines maps between two paths, {x0, x1, top, bottom}, in
	 * place of their ink (a VML text path without {@code trim}, {@link #emBox}; with {@code v-same-letter-heights},
	 * {@link #advanceInkBox}: CR-083 phase 4)
	 */
	static Map<WordArtRenderer.Glyph, double[]> warp(WordArtRenderer.Layout layout, String name, List<TextWarp.Measured> paths,
			Rectangle2D.Double rect, Map<WordArtRenderer.Glyph, Shape> drawn, List<String> problems,
			Function<List<WordArtRenderer.Line>, double[]> frame) {
		int n = paths.size();
		List<WordArtRenderer.Line> lines = layout.lines;
		TextWarp.Budget budget = new TextWarp.Budget(TextWarp.maxPoints);
		if ("vml-along".equals(name) || "vml-along-natural".equals(name)) return along(lines, paths, drawn, "vml-along".equals(name));
		if ("vml-bands".equals(name) && n % 2 == 0) {
			vmlBands(lines, paths, drawn, budget, frame);
			return null;
		}
		if (n == 2) {
			envelope(lines, paths.get(0), paths.get(1), drawn, null, 0, budget, frame == null ? null : frame.apply(lines));
			return null;
		}
		Map<WordArtRenderer.Glyph, double[]> placed = new HashMap<WordArtRenderer.Glyph, double[]>();
		if (n == 1) {
			followPath(lines, paths.get(0), rect, drawn, placed, true, "textCircle".equals(name));
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
			if (single) followPath(mine, paths.get(b), rect, drawn, placed, false, false);
			else envelope(mine, paths.get(2 * b), paths.get(2 * b + 1), drawn, block, tallest, budget);
		}
		// lines with no band (none drawn): Word draws them nowhere
		return placed;
	}

	/**
	 * How far above the baseline, in ems, the line a VML Follow Path's glyphs sit on runs where a run gives no em box:
	 * fitted to Word's o:spt 144, 145 and 146 in Aptos (wv40: 0.22 puts their ink within 0.3, 0.2 and 1.7 pt of Word's).
	 */
	static final double VML_ALONG_RISE = 0.22;

	/**
	 * The middle of the line's em box (the box a text path maps without trim: its foot the baseline plus the face's
	 * larger descent), above its baseline, in ems: what Word lays on a Follow Path (Aptos 0.5 - 0.282 = 0.218, the 0.22
	 * wv40 fitted; cr083-phase4's wv44-52 and -53, Calibri and Times New Roman)
	 */
	static double rise(WordArtRenderer.Line l) {
		double size = 0, d = 0;
		for (WordArtRenderer.Glyph g : l.glyphs) {
			if (g.font == null || g.font.getSize2D() < size) continue;
			size = g.font.getSize2D();
			d = g.run.getEmDescent();
		}
		return d > 0 ? 0.5 - d : VML_ALONG_RISE;
	}

	/**
	 * VML's Follow Path (an odd number of text paths: o:spt 144 to 147), as Word draws it (wv40, CR-083 phase 4 findings
	 * section 6, cr083-phase4 section 7): each line along its path (line i on path i, the extra ones on the first), all
	 * lines at one scale, the least that makes a band's longest line its path's length ({@code fitpath}; without it at
	 * their size, unmeasured), each line aligned along its path, a band's lines moved apart along the path's normal by
	 * their places in the block, the middle of their rise points on the path; each glyph's middle at
	 * its advance along the path, the middle of its em box ({@link #rise}) on the path, turned to the
	 * path's direction (so upright to it: outside an arch drawn over the top, inside one drawn under the bottom).
	 *
	 * @return each glyph's scale and the place and turn its text is drawn at: {scale, x, y, angle in radians}
	 */
	static Map<WordArtRenderer.Glyph, double[]> along(List<WordArtRenderer.Line> lines, List<TextWarp.Measured> paths,
			Map<WordArtRenderer.Glyph, Shape> drawn, boolean fit) {
		Map<WordArtRenderer.Glyph, double[]> placed = new HashMap<WordArtRenderer.Glyph, double[]>();
		List<List<WordArtRenderer.Line>> byBand = bands(lines, paths.size());
		// each line's rise point (VML_ALONG_RISE of its size above its baseline), in the layout's y (down)
		Map<WordArtRenderer.Line, Double> rise = new HashMap<WordArtRenderer.Line, Double>();
		for (WordArtRenderer.Line l : lines) {
			double size = 0;
			for (WordArtRenderer.Glyph g : l.glyphs) if (g.font != null) size = Math.max(size, g.font.getSize2D());
			rise.put(l, l.top + l.baseline - rise(l) * size);
		}
		// one scale for every line: the least of each band's path's length over its longest line's advance
		// (cr083-phase4's wv43-29: "One", "Two" and "Three" on three paths at one size, "Three" filling its path); a band's
		// lines placed by its path's length, each aligned on it, and moved apart along its normal by their places in the
		// block, the middle of their rise points on the path (wv41-09: on Arch Up "Two lines" fills the arch inside it,
		// "WordArt" centred outside it)
		double s = 1;
		if (fit) {
			double least = Double.POSITIVE_INFINITY;
			for (int b = 0; b < paths.size(); b++) {
				double widest = 0;
				for (WordArtRenderer.Line l : byBand.get(b)) widest = Math.max(widest, l.width());
				if (widest > 0 && paths.get(b).length > 0) least = Math.min(least, paths.get(b).length / widest);
			}
			if (least < Double.POSITIVE_INFINITY) s = least;
		}
		for (int b = 0; b < paths.size(); b++) {
			List<WordArtRenderer.Line> mine = byBand.get(b);
			if (mine.isEmpty()) continue;
			TextWarp.Measured path = paths.get(b);
			double length = path.length;
			if (!(length > 0)) continue;
			double mid = (rise.get(mine.get(0)) + rise.get(mine.get(mine.size() - 1))) / 2;
			for (WordArtRenderer.Line l : mine) {
				double width = l.width();
				if (!(width > 0)) continue;
				double out = mid - rise.get(l);                  // outward (the glyphs' up), in the layout's units
				double start;
				String align = l.para == null ? "center" : l.para.getAlign();
				if ("left".equals(align)) start = 0;
				else if ("right".equals(align)) start = length - s * width;
				else start = (length - s * width) / 2;
				for (WordArtRenderer.Glyph g : l.glyphs) {
					if (g.lineBreak) continue;
					double u = (start + s * (g.x + g.advance / 2)) / length;
					double ox = g.advance / 2, oy = -rise(l) * g.font.getSize2D() + out;
					if (g.outline != null && !g.space) drawn.put(g, TextWarp.place(g.outline, ox, oy, s, path, u));
					java.awt.geom.Point2D.Double origin = TextWarp.placed(0, 0, ox, oy, s, path, u);
					placed.put(g, new double[] { s, origin.x, origin.y, path.angle(u) });
				}
			}
		}
		return placed;
	}


	/**
	 * VML's bands (an even number of text paths past two: o:spt 151, 166, 167): each band's lines between its pair of
	 * paths, at one scale across, the least of a band's paths' mean length over its lines' width, centred on the band,
	 * each band's lines down its depth (cr083-phase4's wv41-16, Button Pour on two lines: Word's "WordArt" on the arch at
	 * about the straight band's scale, not stretched over the arch; review 2026-11-13). The frame, where given, the rule
	 * for each band's box in place of its ink (its em boxes without trim).
	 */
	private static void vmlBands(List<WordArtRenderer.Line> lines, List<TextWarp.Measured> paths, Map<WordArtRenderer.Glyph, Shape> drawn,
			TextWarp.Budget budget, Function<List<WordArtRenderer.Line>, double[]> frame) {
		int slots = paths.size() / 2;
		List<List<WordArtRenderer.Line>> byBand = bands(lines, slots);
		double s = Double.POSITIVE_INFINITY, tallest = 0;
		Rectangle2D[] inks = new Rectangle2D[slots];
		for (int b = 0; b < slots; b++) {
			List<WordArtRenderer.Line> mine = byBand.get(b);
			if (mine.isEmpty()) continue;
			Rectangle2D ink = ink(mine, drawn);
			if (ink == null) continue;
			if (frame != null) {
				double[] em = frame.apply(mine);
				if (em != null && em.length == 4) ink = new Rectangle2D.Double(em[0], em[2], em[1] - em[0], em[3] - em[2]);
			}
			inks[b] = ink;
			tallest = Math.max(tallest, ink.getHeight());
			double mean = (paths.get(2 * b).length + paths.get(2 * b + 1).length) / 2;
			if (ink.getWidth() > 0) s = Math.min(s, mean / ink.getWidth());
		}
		if (!(s < Double.POSITIVE_INFINITY)) return;
		for (int b = 0; b < slots; b++) {
			Rectangle2D ink = inks[b];
			if (ink == null) continue;
			double mean = (paths.get(2 * b).length + paths.get(2 * b + 1).length) / 2;
			// the band's frame: as wide as its paths' mean length at the one scale, centred on the ink; as deep as the
			// tallest band's ink, centred on its own
			double w = mean / s, hgt = Math.max(ink.getHeight(), tallest);
			Rectangle2D.Double rect = new Rectangle2D.Double(ink.getCenterX() - w / 2, ink.getCenterY() - hgt / 2, w, hgt);
			TextWarp.Mapping m = TextWarp.envelope(rect, paths.get(2 * b), paths.get(2 * b + 1));
			for (WordArtRenderer.Line l : byBand.get(b)) {
				for (WordArtRenderer.Glyph g : l.glyphs) {
					Shape sh = drawn.get(g);
					if (sh != null) drawn.put(g, TextWarp.warp(sh, m, TextWarp.segment(g.font.getSize2D()), budget));
				}
			}
		}
	}

	/**
	 * What a VML text path maps with {@code v-same-letter-heights} (its glyphs one height, {@code WordArtRenderer
	 * .sameHeights}): across, the lines' advance, as {@link #emBox}; down, their ink. Word's "Gyp ax" (cr083-phase4b's
	 * wv49-13) and "Wordart" (cr083-phase4's wv44-26), with trim, start at the pen and end at the last advance, where
	 * without the face's side bearings are cut (wv44-40). Without trim, {@link #emBox} (unmeasured). {x0, x1, top,
	 * bottom}, or null
	 */
	static double[] advanceInkBox(List<WordArtRenderer.Line> lines) {
		double top = Double.POSITIVE_INFINITY, bottom = Double.NEGATIVE_INFINITY, x0 = Double.POSITIVE_INFINITY, x1 = Double.NEGATIVE_INFINITY;
		for (WordArtRenderer.Line l : lines) {
			double base = l.top + l.baseline;
			for (WordArtRenderer.Glyph g : l.glyphs) {
				if (g.space || g.lineBreak || g.outline == null) continue;
				Rectangle2D b = g.outline.getBounds2D();
				if (!(b.getHeight() > 0)) continue;
				top = Math.min(top, base + b.getMinY());
				bottom = Math.max(bottom, base + b.getMaxY());
			}
			if (l.width() > 0) {
				x0 = Math.min(x0, l.x0);
				x1 = Math.max(x1, l.x0 + l.width());
			}
		}
		if (!(top < bottom) || !(x0 < x1)) return null;
		return new double[] { x0, x1, top, bottom };
	}

	/**
	 * What a VML text path maps without {@code trim} (cr083-phase4's wv44, three faces): across, the lines' advance (their
	 * side bearings kept); down, each line's em box, its foot the baseline plus the face's {@code emDescent} (its larger
	 * descent), its top an em above that. {x0, x1, top, bottom}, or null
	 */
	static double[] emBox(List<WordArtRenderer.Line> lines) {
		double top = Double.POSITIVE_INFINITY, bottom = Double.NEGATIVE_INFINITY, x0 = Double.POSITIVE_INFINITY, x1 = Double.NEGATIVE_INFINITY;
		for (WordArtRenderer.Line l : lines) {
			double size = 0, descent = 0;
			for (WordArtRenderer.Glyph g : l.glyphs) {
				if (g.font == null) continue;
				if (g.font.getSize2D() >= size) {
					size = g.font.getSize2D();
					descent = g.run.getEmDescent() > 0 ? g.run.getEmDescent() : 1 - WordArtRenderer.VML_EM_ABOVE;
				}
			}
			if (size <= 0) continue;
			double base = l.top + l.baseline;
			top = Math.min(top, base + descent * size - size);
			bottom = Math.max(bottom, base + descent * size);
			if (l.width() > 0) {
				x0 = Math.min(x0, l.x0);
				x1 = Math.max(x1, l.x0 + l.width());
			}
		}
		if (!(top < bottom) || !(x0 < x1)) return null;
		return new double[] { x0, x1, top, bottom };
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
			Map<WordArtRenderer.Glyph, Shape> drawn, TextWarp.Budget budget) {
		envelope(lines, top, bottom, drawn, null, 0, budget);
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
			Map<WordArtRenderer.Glyph, Shape> drawn, Rectangle2D block, double tallest, TextWarp.Budget budget) {
		envelope(lines, top, bottom, drawn, block, tallest, budget, null);
	}

	private static void envelope(List<WordArtRenderer.Line> lines, TextWarp.Measured top, TextWarp.Measured bottom,
			Map<WordArtRenderer.Glyph, Shape> drawn, Rectangle2D block, double tallest, TextWarp.Budget budget, double[] frame) {
		Rectangle2D ink = ink(lines, drawn);
		if (ink == null || ink.getWidth() <= 0 || ink.getHeight() <= 0) return;
		if (frame != null && frame.length == 4 && frame[1] > frame[0] && frame[3] > frame[2]) {
			ink = new Rectangle2D.Double(frame[0], frame[2], frame[1] - frame[0], frame[3] - frame[2]);
		}
		if (block != null) {
			// a band's lines across the block's ink, and down a range as tall as the tallest band's, centred on their own
			double h = Math.max(ink.getHeight(), tallest);
			ink = new Rectangle2D.Double(block.getX(), ink.getCenterY() - h / 2, block.getWidth(), h);
		}
		TextWarp.Mapping m = TextWarp.envelope(ink, top, bottom);
		for (WordArtRenderer.Line l : lines) {
			for (WordArtRenderer.Glyph g : l.glyphs) {
				Shape s = drawn.get(g);
				// each piece at most a hundredth of the glyph's size long (0.5 pt up to 50 pt), within the box's budget
				if (s != null) drawn.put(g, TextWarp.warp(s, m, TextWarp.segment(g.font.getSize2D()), budget));
			}
		}
	}

	/**
	 * Follow Path, as Word draws it (the Follow Path round, cr083-followpath and cr083-followpath2: 435 probes; phase 1-2
	 * findings section 4):
	 * <ul>
	 * <li>the path is scaled about the text rectangle's centre by k = D / (D + 2T), D the rectangle's larger side and T
	 * the lines' ink heights added up, unscaled ({@link #pathScale}); the glyphs by k too, or less where the text is
	 * longer than the curve it may take: an arch's half of its ellipse, or on an arc adjusted short its sweep on a circle
	 * of the scaled half-width; a circle's whole ellipse (Word's cap on a circle adjusted short is not known: it took
	 * less);</li>
	 * <li>the text follows the whole ellipse the arc is part of, centred on the arc's middle, so that an adjusted arc
	 * places it as the default does ({@code textArchUp} at 180, 210 and 240 degrees alike); left-aligned text starts at
	 * the arc's start, right-aligned ends at its end;</li>
	 * <li>each glyph's middle is at its advance along the path itself (arc length), its baseline
	 * {@link #PATH_ABOVE_BASELINE} of its scaled size inside the path along the normal there, the glyph turned to the
	 * path;</li>
	 * <li>further lines stand away from the innermost line (the last, or for {@code textArchDown} the first: the one on
	 * the side of the arc's centre), their inks touching, moved as a whole along the normal at the text's middle.</li>
	 * </ul>
	 * Word measured some probes' advances as if along a curve about 0.06 em outside the path (their spacing about 2%
	 * closer, and the text a little back along it: 141 of the 320 one-line probes), which no property of the box, text or
	 * preset that the round varied tells apart; this follows the others.
	 *
	 * @param ellipse whether the path is an arc of the ellipse in the text rectangle (the three Follow Path presets), whose
	 *        whole ellipse the text may follow; otherwise (textButton's paths) the path alone
	 * @param circle whether the text may take the whole ellipse (textCircle), not half
	 */
	private static void followPath(List<WordArtRenderer.Line> lines, TextWarp.Measured path, Rectangle2D.Double rect,
			Map<WordArtRenderer.Glyph, Shape> drawn, Map<WordArtRenderer.Glyph, double[]> placed, boolean ellipse, boolean circle) {
		List<WordArtRenderer.Line> real = new ArrayList<WordArtRenderer.Line>();
		List<double[]> inks = new ArrayList<double[]>();
		double widest = 0, inkSum = 0;
		for (WordArtRenderer.Line l : lines) {
			if (!hasInk(l)) continue;
			// the line's ink, top and bottom from its baseline (y down), unscaled
			double top = Double.POSITIVE_INFINITY, bottom = Double.NEGATIVE_INFINITY;
			for (WordArtRenderer.Glyph g : l.glyphs) {
				if (g.space || g.lineBreak || g.outline == null) continue;
				Rectangle2D r = tight(g.outline);
				if (r == null) continue;
				top = Math.min(top, r.getMinY());
				bottom = Math.max(bottom, r.getMaxY());
			}
			if (top > bottom) continue;
			real.add(l);
			inks.add(new double[] { top, bottom });
			inkSum += bottom - top;
			widest = Math.max(widest, l.width() - lead(l));
		}
		if (real.isEmpty() || path.length <= 0 || widest <= 0) return;
		double cx = rect.getCenterX(), cy = rect.getCenterY();
		double k = pathScale(rect.width, rect.height, inkSum);
		TextWarp.Measured curve;
		double arc = k * path.length, room;
		if (ellipse && rect.width > 0 && rect.height > 0) {
			curve = TextWarp.ellipse(cx, cy, k * rect.width / 2, k * rect.height / 2, path);
			if (circle) room = curve.length;
			else {
				// an arch: half its ellipse, or less on an arc adjusted short: its sweep (seen from the centre) on a circle of
				// the scaled half-width (a 60 degree arch: 129 pt where Word took 125.4, and 49.6 where it took 50.4)
				double[] p0 = path.at(0), pm = path.at(0.5);
				double half = Math.abs(Math.IEEEremainder(Math.atan2(pm[1] - cy, pm[0] - cx) - Math.atan2(p0[1] - cy, p0[0] - cx), 2 * Math.PI));
				room = Math.min(curve.length / 2, 2 * half * k * rect.width / 2);
			}
		} else {
			AffineTransform in = new AffineTransform();
			in.translate(cx, cy);
			in.scale(k, k);
			in.translate(-cx, -cy);
			curve = new TextWarp.Measured(in.createTransformedShape(path.shape()));
			arc = room = curve.length;
		}
		double s = Math.min(k, room / widest);
		double length = curve.length, middle = length / 2;
		// the innermost line: the last, unless the arc's centre is on the glyphs' upper side there (textArchDown)
		double am = curve.angle(0.5);
		double[] pm = curve.at(0.5);
		boolean lastInner = (cx - pm[0]) * -Math.sin(am) + (cy - pm[1]) * Math.cos(am) > 0;
		int n = real.size();
		double[] size = new double[n], off = new double[n];
		for (int i = 0; i < n; i++) {
			for (WordArtRenderer.Glyph g : real.get(i).glyphs) if (g.font != null) size[i] = Math.max(size[i], g.font.getSize2D());
		}
		// each line's baseline from the path, toward the glyphs' feet: the innermost's own, the others' inks touching
		if (lastInner) {
			off[n - 1] = PATH_ABOVE_BASELINE * size[n - 1] * s;
			for (int i = n - 2; i >= 0; i--) off[i] = off[i + 1] - (-inks.get(i + 1)[0] + inks.get(i)[1]) * s;
		} else {
			off[0] = PATH_ABOVE_BASELINE * size[0] * s;
			for (int i = 1; i < n; i++) off[i] = off[i - 1] + (inks.get(i - 1)[1] - inks.get(i)[0]) * s;
		}
		// the normal at the text's middle (the widest line's, by the first line's alignment), along which lines stand apart
		String align0 = alignment(real.get(0));
		double tm = "center".equals(align0) ? middle : "right".equals(align0) ? middle + arc / 2 - widest * s / 2
				: middle - arc / 2 + widest * s / 2;
		double an = curve.angle(tm / length);
		double nx = -Math.sin(an), ny = Math.cos(an);
		for (int i = 0; i < n; i++) {
			WordArtRenderer.Line l = real.get(i);
			double lead = lead(l), w = (l.width() - lead) * s;
			String align = alignment(l);
			double start = "center".equals(align) ? middle - w / 2 : "right".equals(align) ? middle + arc / 2 - w : middle - arc / 2;
			double own = PATH_ABOVE_BASELINE * size[i] * s, apart = off[i] - own;
			for (WordArtRenderer.Glyph g : l.glyphs) {
				if (g.lineBreak) continue;
				double u = (start + (g.x - lead + g.advance / 2) * s) / length;
				double[] p = curve.at(u);
				double a = curve.angle(u);
				AffineTransform t = new AffineTransform();
				t.translate(p[0] - Math.sin(a) * own + nx * apart, p[1] + Math.cos(a) * own + ny * apart);
				t.rotate(a);
				t.scale(s, s);
				t.translate(-g.advance / 2, 0);
				if (g.outline != null && !g.space) drawn.put(g, t.createTransformedShape(g.outline));
				double[] o = new double[2];
				t.transform(new double[] { 0, 0 }, 0, o, 0, 1);
				placed.put(g, new double[] { s, o[0], o[1], a });
			}
		}
	}

	/** Where a line's first glyph that isn't a space starts: Word sets Follow Path text without its leading spaces */
	private static double lead(WordArtRenderer.Line l) {
		for (WordArtRenderer.Glyph g : l.glyphs) if (!g.space && !g.lineBreak) return g.x;
		return 0;
	}

	/** A line's alignment along the path: center (justified ones too), right or left */
	private static String alignment(WordArtRenderer.Line l) {
		String a = l.para == null ? "left" : l.para.getAlign();
		return "center".equals(a) || "both".equals(a) || "distribute".equals(a) ? "center" : "right".equals(a) ? "right" : "left";
	}

	/** How far the line's baseline is inside the path, a fraction of its scaled size (fitted on Aptos: 0.14 to 0.15; Calibri 0.158) */
	static final double PATH_ABOVE_BASELINE = 0.145;

	/**
	 * Follow Path's scale, of the path about the text rectangle's centre and of the glyphs (unless the text is too long
	 * for its curve): D / (D + 2T), D the rectangle's larger side and T the lines' ink heights added up, unscaled. Word's
	 * on the Follow Path round to within 0.002 on 410 of 435 probes, and on phase 0's twelve: as if the path, grown by
	 * the text's ink on each side, were scaled to fit the rectangle's larger side.
	 */
	static double pathScale(double width, double height, double ink) {
		double d = Math.max(width, height);
		return d <= 0 ? 1 : d / (d + 2 * Math.max(0, ink));
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
