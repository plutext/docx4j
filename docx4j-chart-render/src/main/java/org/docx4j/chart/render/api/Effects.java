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
package org.docx4j.chart.render.api;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.docx4j.dml.CTEffectList;
import org.docx4j.dml.CTGlowEffect;
import org.docx4j.dml.CTInnerShadowEffect;
import org.docx4j.dml.CTOuterShadowEffect;
import org.docx4j.dml.CTReflectionEffect;
import org.docx4j.dml.STRectAlignment;
import org.docx4j.model.color.DrawingMLColors;
import org.docx4j.model.color.ThemeColors;

/**
 * A drawing's DrawingML effects ({@code a:effectLst}) as Word draws them in a PDF (CR-081): pictures with alpha, under
 * the drawing (its reflection, outer shadow and glow, in that order), over it (its inner shadow), or in its place (the
 * drawing with its soft edge, the inner shadow in it). Every effect follows the drawing's silhouette, its alpha, so a
 * transparent picture's effects follow its strokes.
 *
 * <p>The rules are Word 16.0.20326's, measured in CR-081 phase 0 (CR-081-notes/phase0-findings.md §2) and checked in
 * phase 1 against Word's own effect pictures (EffectsTest):</p>
 * <ul>
 * <li>The effects are a chain: the soft edge (the inner shadow in it) or the inner shadow on the drawing; the glow round
 * the drawing's own silhouette, not its soft edge; the outer shadow cast by the drawing with its glow; the reflection
 * of all of them, about the drawing's box (fxr70).</li>
 * <li>Every blur is a Gaussian: sigma blurRad / 3 for the outer and inner shadow and the reflection, rad / 6 for the
 * glow, rad / 3 for the soft edge.</li>
 * <li>An outer shadow is the silhouette moved dist along dir (clockwise from +x), scaled and skewed about its algn point
 * of the box, in its colour; a glow the silhouette grown by {@link #GLOW_SPREAD} of its radius; an inner shadow the
 * outside of the silhouette moved dist against dir, blurred, inside it (dir 0 shades the right edge); a soft edge the
 * silhouette shrunk by {@link #SOFT_EDGE_INSET} of its radius, blurred, as the drawing's alpha; a reflection the whole
 * drawing flipped, scaled and skewed about its algn point and moved as the shadow, faded linearly from stA at stPos to
 * endA at endPos along fadeDir across what it reflects from the mirror line (half its distance back), holding endA
 * past endPos, then blurred. Scale and skew are one matrix, [sx, tan kx; tan ky, sy].</li>
 * <li>An effect picture is 200 dpi, made coarser by a whole factor k = ceil(blur in pixels / 16), the glow's blur being
 * rad / 2; an inner shadow and a soft edge are always 200 dpi.</li>
 * <li>A picture's line (CR-081 phase 2), wholly outside it, is painted over the drawing and its inner shadow: it glows,
 * casts the shadow and is reflected with the drawing, and a shadow's or reflection's algn point is on the box with the
 * line (fs10-08); the inner shadow is the drawing's alone (fs10-07), at each pixel's centre.</li>
 * </ul>
 *
 * <p>A picture is capped (D10): at most {@link #MAX_SIDE} pixels a side and {@link #MAX_PIXELS} in all, made coarser
 * down to {@link #MIN_PIXELS_PER_POINT}; past that the effect isn't drawn and {@link #OVER_LIMIT} is reported. Charts
 * keep their own shadow and glow ({@link Drawing.Shadow}), measured against Office's charts (CR-081 D2).</p>
 *
 * @since 17.3.7
 */
public final class Effects {

	/** Where a picture goes: under the drawing, over it, or in its place */
	public enum Layer {
		UNDER, OVER, REPLACE
	}

	/** Paints the drawing: its box is (0, 0, w, h) in points, y down */
	public interface Painter {
		void paint(Graphics2D g);
	}

	/** An effect picture: non-premultiplied ARGB, placed in the drawing's frame, points from its box's top left */
	public static final class Picture {
		private final BufferedImage image;
		private final Rectangle2D.Double place;
		private final Layer layer;
		private final String effect;

		Picture(BufferedImage image, Rectangle2D.Double place, Layer layer, String effect) {
			this.image = image;
			this.place = place;
			this.layer = layer;
			this.effect = effect;
		}

		public BufferedImage getImage() {
			return image;
		}

		/** Where it goes, points in the drawing's frame */
		public Rectangle2D.Double getPlace() {
			return place;
		}

		public Layer getLayer() {
			return layer;
		}

		/** The effect's element name: outerShdw, glow, reflection, innerShdw, softEdge */
		public String getEffect() {
			return effect;
		}

		/** Its pixels per point */
		public double getPixelsPerPoint() {
			return image.getWidth() / place.width;
		}

		/** It, painted where it goes */
		public Painter painter() {
			return g -> g.drawImage(image, new AffineTransform(place.width / image.getWidth(), 0, 0, place.height / image.getHeight(), place.x,
					place.y), null);
		}
	}

	/** The pictures, in drawing order (under first), and the gaps (stable codes, CR-081 D9) */
	public static final class Result {
		private final List<Picture> pictures;
		private final List<String> problems;

		Result(List<Picture> pictures, List<String> problems) {
			this.pictures = Collections.unmodifiableList(pictures);
			this.problems = Collections.unmodifiableList(problems);
		}

		public List<Picture> getPictures() {
			return pictures;
		}

		public List<String> getProblems() {
			return problems;
		}

		/** Nothing is drawn */
		public boolean isEmpty() {
			return pictures.isEmpty();
		}
	}

	/** Word's effect resolution: 200 dpi */
	public static final double BASE_PIXELS_PER_POINT = 200.0 / 72;
	/** The blur Word keeps within, in pixels, making a picture coarser by a whole factor past it */
	public static final double BLUR_PIXELS = 16;
	/** The glow's silhouette grown by this share of its radius before its blur (its 50% contour: 0.486 to 0.493 rad) */
	public static final double GLOW_SPREAD = 0.485;
	/** The soft edge's silhouette shrunk by this share of its radius before its blur (its 50% contour: 0.84 to 0.94) */
	public static final double SOFT_EDGE_INSET = 0.88;
	/** D10's cap */
	public static final int MAX_SIDE = 4096;
	public static final long MAX_PIXELS = 16_000_000L;
	public static final double MIN_PIXELS_PER_POINT = 0.5;
	/** D9's code for an effect past the cap */
	public static final String OVER_LIMIT = "render.effect.over_limit";

	private Effects() {}

	/**
	 * The effects of a drawing w by h points.
	 *
	 * @param maxPixelsPerPoint an upper bound on a picture's resolution (docx4j.convert.out.effects.pixelsPerPoint, D3);
	 *        0 or less for none
	 */
	public static Result draw(CTEffectList list, ThemeColors theme, double w, double h, Painter painter, double maxPixelsPerPoint) {
		return draw(list, theme, w, h, painter, null, null, maxPixelsPerPoint);
	}

	/**
	 * The effects of a drawing w by h points with a line round it, painted over the drawing and its inner shadow: a
	 * picture's line, wholly outside it (CR-081 phase 2). The line is part of what glows, casts the shadow and is
	 * reflected (fxr71-11, -12; 16_en-ZA_num_tbl_10976); the inner shadow is the drawing's alone, under the line
	 * (fs10-07: Word's is the picture's size). Where there is a soft edge, the line is in the picture that takes the
	 * drawing's place (not measured).
	 *
	 * @param line paints the line, or null for none
	 * @param lineBounds what the line paints, in the drawing's frame; null for none
	 * @since 17.3.7 (CR-081 phase 2)
	 */
	public static Result draw(CTEffectList list, ThemeColors theme, double w, double h, Painter painter, Painter line, Rectangle2D lineBounds,
			double maxPixelsPerPoint) {
		return draw(list, theme, w, h, painter, line, lineBounds, null, null, maxPixelsPerPoint);
	}

	/**
	 * As {@link #draw(CTEffectList, ThemeColors, double, double, Painter, Painter, Rectangle2D, double)}, with the
	 * drawing's preset shadow (phase 4), which its reflection reflects (fxp4-01: Word's reflection is the preset's
	 * offset taller); the caller draws the preset itself, after the reflection and before the outer shadow and glow
	 * (fxp4-01 to -03).
	 *
	 * @param preset paints the preset shadow, or null for none
	 * @param presetBounds what it paints, in the drawing's frame
	 * @since 17.3.7 (CR-081 phase 4)
	 */
	public static Result draw(CTEffectList list, ThemeColors theme, double w, double h, Painter painter, Painter line, Rectangle2D lineBounds,
			Painter preset, Rectangle2D presetBounds, double maxPixelsPerPoint) {
		List<Picture> out = new ArrayList<Picture>();
		List<String> problems = new ArrayList<String>();
		if (list == null || painter == null || !(w > 0) || !(h > 0)) return new Result(out, problems);
		Rectangle2D.Double box = new Rectangle2D.Double(0, 0, w, h);
		if (line == null) lineBounds = null;
		Rectangle2D drawn = lineBounds == null ? box : box.createUnion(lineBounds);
		double cap = maxPixelsPerPoint > 0 ? maxPixelsPerPoint : Double.MAX_VALUE;
		// Word's chain (fxr70): the soft edge (its inner shadow in it) or the inner shadow on the drawing; the glow round the
		// drawing's own silhouette (fxr70-03: round the soft edge it is 5.7 of 255 out, round the drawing 0.5); the outer
		// shadow cast by the drawing and its glow; the reflection of all of them, about the drawing's box
		Picture soft = null, inner = null, glow = null, shadow = null, reflection = null;
		if (list.getSoftEdge() != null && list.getSoftEdge().getRad() > 0) {
			soft = add(problems, softEdge(list.getSoftEdge().getRad() / 12700.0, list.getInnerShdw(), theme, box, drawn, painter, line, cap));
		} else if (list.getInnerShdw() != null) {
			inner = add(problems, inner(list.getInnerShdw(), theme, box, painter, cap));
		}
		// the drawing with its line; where there is a soft edge, its picture holds them both
		Painter drawing = soft != null ? soft.painter() : painter;
		Painter lineOver = soft != null ? null : line;
		Painter own = stack(painter, line);
		Rectangle2D bounds = drawn;
		if (list.getGlow() != null) glow = add(problems, glow(list.getGlow(), theme, bounds, own, cap));
		if (glow != null) bounds = bounds.createUnion(glow.place);
		// a shadow's and a reflection's algn point is on the drawing's box with its line (fs10-08: Thick Matte's sy 90%
		// about the bottom of its 35pt line, 3pt lower than about the picture's)
		if (list.getOuterShdw() != null) {
			shadow = add(problems, outer(list.getOuterShdw(), theme, drawn, bounds, stack(painter(glow), drawing, lineOver), cap));
		}
		if (shadow != null) bounds = bounds.createUnion(shadow.place);
		if (preset != null && presetBounds != null) bounds = bounds.createUnion(presetBounds);
		if (list.getReflection() != null) {
			reflection = add(problems, reflection(list.getReflection(), drawn, bounds,
					stack(preset, painter(shadow), painter(glow), drawing, painter(inner), lineOver), cap));
		}
		// drawn in Word's order: under the drawing its reflection, outer shadow and glow; then in its place or over it
		for (Picture p : new Picture[] { reflection, shadow, glow, soft, inner }) if (p != null) out.add(p);
		return new Result(out, problems);
	}

	private static Picture add(List<String> problems, Object p) {
		if (p instanceof String && !problems.contains(p)) problems.add((String) p);
		return p instanceof Picture ? (Picture) p : null;
	}

	/** The picture painted where it goes; null for none */
	private static Painter painter(Picture p) {
		return p == null ? null : p.painter();
	}

	/** The painters, in order, those given */
	private static Painter stack(Painter... painters) {
		return g -> {
			for (Painter p : painters) if (p != null) p.paint(g);
		};
	}

	// ------------------------------------------------------------------ the effects

	/** Cast by what the painter paints, within bounds; placed about the drawing's box */
	private static Object outer(CTOuterShadowEffect s, ThemeColors theme, Rectangle2D box, Rectangle2D bounds, Painter painter, double cap) {
		Color c = colour(s.getSrgbClr(), s.getSchemeClr(), s.getPrstClr(), s.getSysClr(), s.getHslClr(), s.getScrgbClr(), theme);
		if (c == null) return null;
		double blur = s.getBlurRad() / 12700.0;
		AffineTransform t = placed(box, align(s.getAlgn()), s.getDist() / 12700.0, s.getDir() / 60000.0, s.getSx() / 100000.0,
				s.getSy() / 100000.0, s.getKx() / 60000.0, s.getKy() / 60000.0);
		if (!(Math.abs(t.getDeterminant()) > 1e-12)) return null; // scaled to nothing
		double sigma = blur / 3;
		Rectangle2D area = grow(t.createTransformedShape(bounds).getBounds2D(), blur);
		double ppt = resolution(area, k(blur), cap);
		if (ppt <= 0) return OVER_LIMIT;
		Grid g = Grid.of(area, ppt);
		float[] a = g.alpha(painter, t);
		a = blur(a, g.w, g.h, sigma * ppt);
		return g.picture(a, c, Layer.UNDER, "outerShdw");
	}

	/** Round what the painter paints, within bounds */
	private static Object glow(CTGlowEffect s, ThemeColors theme, Rectangle2D bounds, Painter painter, double cap) {
		Color c = colour(s.getSrgbClr(), s.getSchemeClr(), s.getPrstClr(), s.getSysClr(), s.getHslClr(), s.getScrgbClr(), theme);
		double rad = s.getRad() / 12700.0;
		if (c == null || !(rad > 0)) return null;
		Rectangle2D area = grow(bounds, rad);
		double ppt = resolution(area, k(rad / 2), cap);
		if (ppt <= 0) return OVER_LIMIT;
		Grid g = Grid.of(area, ppt);
		float[] a = g.alpha(painter, null);
		a = dilate(a, g.w, g.h, GLOW_SPREAD * rad * ppt);
		a = blur(a, g.w, g.h, rad / 6 * ppt);
		return g.picture(a, c, Layer.UNDER, "glow");
	}

	private static Object inner(CTInnerShadowEffect s, ThemeColors theme, Rectangle2D.Double box, Painter painter, double cap) {
		Color c = colour(s.getSrgbClr(), s.getSchemeClr(), s.getPrstClr(), s.getSysClr(), s.getHslClr(), s.getScrgbClr(), theme);
		if (c == null) return null;
		double ppt = resolution(box, 1, cap);
		if (ppt <= 0) return OVER_LIMIT;
		Grid g = Grid.of(box, ppt);
		float[] a = innerAlpha(s, g, painter, ppt);
		return g.picture(a, c, Layer.OVER, "innerShdw");
	}

	/** An inner shadow's alpha in the drawing's grid: the outside moved dist against dir, blurred, inside the silhouette */
	private static float[] innerAlpha(CTInnerShadowEffect s, Grid g, Painter painter, double ppt) {
		double blur = s.getBlurRad() / 12700.0, dist = s.getDist() / 12700.0, dir = Math.toRadians(s.getDir() / 60000.0);
		if ((dist + blur) * ppt > 4.0 * Math.max(g.w, g.h)) {
			// moved or blurred far past the drawing, the outside covers all of it (and a grid that wide is not made)
			return g.alpha(painter, null);
		}
		int pad = (int) Math.ceil((dist + blur) * ppt) + 2;
		Grid ext = g.grown(pad);
		float[] inside = ext.alpha(painter, null);
		float[] out = new float[inside.length];
		for (int i = 0; i < out.length; i++) out[i] = 1 - inside[i];
		out = blur(out, ext.w, ext.h, blur / 3 * ppt);
		double dx = dist * Math.cos(dir) * ppt, dy = dist * Math.sin(dir) * ppt;
		float[] a = new float[g.w * g.h];
		for (int y = 0; y < g.h; y++) {
			for (int x = 0; x < g.w; x++) {
				float in = inside[(y + pad) * ext.w + x + pad];
				if (in <= 0) continue;
				// at the pixel's centre, moved (with no dist, Word's corner is 1 - Phi(0.5 px / sigma)^2: fs10-07, fx20-06)
				a[y * g.w + x] = in * sample(out, ext.w, ext.h, x + pad + 0.5 + dx, y + pad + 0.5 + dy, 1f);
			}
		}
		return a;
	}

	/** The drawing with its soft edge, in its place: its inner shadow in it, and its line over that, within drawn */
	private static Object softEdge(double rad, CTInnerShadowEffect innerShadow, ThemeColors theme, Rectangle2D.Double box, Rectangle2D drawn,
			Painter painter, Painter line, double cap) {
		double ppt = resolution(drawn, 1, cap);
		if (ppt <= 0) return OVER_LIMIT;
		Grid g = Grid.of(drawn, ppt);
		BufferedImage im = g.colour(painter, null);
		int[] px = im.getRGB(0, 0, g.w, g.h, null, 0, g.w);
		if (innerShadow != null) {
			Color c = colour(innerShadow.getSrgbClr(), innerShadow.getSchemeClr(), innerShadow.getPrstClr(), innerShadow.getSysClr(),
					innerShadow.getHslClr(), innerShadow.getScrgbClr(), theme);
			if (c != null) over(px, innerAlpha(innerShadow, g, painter, ppt), c);
		}
		if (line != null) {
			im.setRGB(0, 0, g.w, g.h, px, 0, g.w);
			g.paintOver(im, line);
			px = im.getRGB(0, 0, g.w, g.h, null, 0, g.w);
		}
		float[] mask = new float[px.length];
		if (SOFT_EDGE_INSET * rad * ppt < Math.max(g.w, g.h)) {
			for (int i = 0; i < px.length; i++) mask[i] = (px[i] >>> 24) / 255f;
			mask = erode(mask, g.w, g.h, SOFT_EDGE_INSET * rad * ppt);
		} // else eroded away: nothing is left (fx40-07)
		mask = blur(mask, g.w, g.h, rad / 3 * ppt);
		for (int i = 0; i < px.length; i++) {
			int alpha = (int) Math.round((px[i] >>> 24) * Math.min(1, mask[i]));
			px[i] = (alpha << 24) | (px[i] & 0xFFFFFF);
		}
		im.setRGB(0, 0, g.w, g.h, px, 0, g.w);
		return new Picture(im, g.place(), Layer.REPLACE, "softEdge");
	}

	/** What the painter paints, within bounds, reflected about the drawing's box */
	private static Object reflection(CTReflectionEffect s, Rectangle2D box, Rectangle2D bounds, Painter painter, double cap) {
		double blur = s.getBlurRad() / 12700.0;
		double[] al = align(s.getAlgn());
		double dist = s.getDist() / 12700.0, dir = s.getDir() / 60000.0, sx = s.getSx() / 100000.0, sy = s.getSy() / 100000.0,
				kx = s.getKx() / 60000.0, ky = s.getKy() / 60000.0;
		AffineTransform t = placed(box, al, dist, dir, sx, sy, kx, ky);
		if (!(Math.abs(t.getDeterminant()) > 1e-12)) return null; // scaled to nothing
		Rectangle2D area = grow(t.createTransformedShape(bounds).getBounds2D(), blur);
		double ppt = resolution(area, k(blur), cap);
		if (ppt <= 0) return OVER_LIMIT;
		Grid g = Grid.of(area, ppt);
		BufferedImage im = g.colour(painter, t);
		int[] px = im.getRGB(0, 0, g.w, g.h, null, 0, g.w);
		// the fade, along fadeDir across what is reflected (the drawing, with its glow and shadow: fxr70-01 and 05) from the
		// mirror line: it as if moved half the distance, joined to it (fxr50-12 to 14: below a 108pt picture alone Word's
		// fade starts, and spans, 108 + dist / 2)
		double fd = Math.toRadians(s.getFadeDir() / 60000.0), vx = Math.cos(fd), vy = Math.sin(fd);
		double lo = Double.MAX_VALUE, hi = -Double.MAX_VALUE;
		List<Point2D> fadeBox = corners(bounds, t);
		fadeBox.addAll(corners(bounds, placed(box, al, dist / 2, dir, sx, sy, kx, ky)));
		for (Point2D p : fadeBox) {
			double d = p.getX() * vx + p.getY() * vy;
			lo = Math.min(lo, d);
			hi = Math.max(hi, d);
		}
		double stA = s.getStA() / 100000.0, stPos = s.getStPos() / 100000.0, endA = s.getEndA() / 100000.0, endPos = s.getEndPos() / 100000.0;
		float[] r = new float[px.length], gr = new float[px.length], b = new float[px.length], a = new float[px.length];
		for (int y = 0; y < g.h; y++) {
			for (int x = 0; x < g.w; x++) {
				int i = y * g.w + x;
				double qx = g.x0 + (x + 0.5) / ppt, qy = g.y0 + (y + 0.5) / ppt;
				double f = hi > lo ? ((qx * vx + qy * vy) - lo) / (hi - lo) : 0;
				float v = (px[i] >>> 24) / 255f * (float) fade(f, stA, stPos, endA, endPos);
				// premultiplied, for the blur
				a[i] = v;
				r[i] = v * ((px[i] >> 16) & 0xFF);
				gr[i] = v * ((px[i] >> 8) & 0xFF);
				b[i] = v * (px[i] & 0xFF);
			}
		}
		double sigma = blur / 3 * ppt;
		a = blur(a, g.w, g.h, sigma);
		r = blur(r, g.w, g.h, sigma);
		gr = blur(gr, g.w, g.h, sigma);
		b = blur(b, g.w, g.h, sigma);
		for (int i = 0; i < px.length; i++) {
			int ai = Math.round(Math.min(1, a[i]) * 255);
			if (ai == 0) {
				px[i] = 0;
				continue;
			}
			px[i] = (ai << 24) | (clamp(r[i] / a[i]) << 16) | (clamp(gr[i] / a[i]) << 8) | clamp(b[i] / a[i]);
		}
		im.setRGB(0, 0, g.w, g.h, px, 0, g.w);
		return new Picture(im, g.place(), Layer.UNDER, "reflection");
	}

	// ------------------------------------------------------------------ preset shadows (CR-081 phase 4)

	/** A vector fill of a preset shadow: its shape, in the drawing's frame, and its colour */
	public static final class Fill {
		private final java.awt.Shape shape;
		private final Color colour;

		Fill(java.awt.Shape shape, Color colour) {
			this.shape = shape;
			this.colour = colour;
		}

		public java.awt.Shape getShape() {
			return shape;
		}

		public Color getColour() {
			return colour;
		}
	}

	/** shdw3's far edge, at half the box's height, leans tan 60 degrees of that half: 93.53pt over a 108pt box */
	private static final double PRESET_SHEAR = Math.tan(Math.toRadians(60)) / 2;

	/**
	 * A preset shadow ({@code a:prstShdw}) as Word draws it (CR-081 phase 4, D6): unblurred vector fills of the
	 * drawing's outline, under it, moved {@code dist} along {@code dir} (clockwise from +x), each preset's own
	 * transform about a point of the box first. Measured on Word's PDFs of every preset over a 144 by 108pt picture and
	 * shape, dist 6pt at 45 degrees (fxr60, fxr61):
	 * <ul>
	 * <li>shdw1, 2, 5, 6, 14: the outline moved;</li>
	 * <li>shdw3, 11 and 4, 12: half its height up from its foot, leaning left and right (its top edge tan 60 degrees of
	 * that half across); shdw7, 15 and 8, 16 the same mirrored down from its foot;</li>
	 * <li>shdw9: 75% about its top left; shdw10: 125% about its bottom right;</li>
	 * <li>shdw19: half its height up from its foot; shdw20: mirrored down from its foot;</li>
	 * <li>shdw13: a second fill twice as far; shdw17, 18: a second fill as far the other way; the second under the
	 * first, each channel of its colour plus 0.4.</li>
	 * </ul>
	 * Word's presets named for another direction (shdw1 is "top left") are drawn along dir as well. The round after
	 * phase 4 (fxp1 to fxp5) confirmed the move at other dists and dirs, the transforms on other sizes (the lean and the
	 * scales follow the box), and the outline followed: an ellipse frame's, a star's, an unfilled shape's line (a ring),
	 * the box with a picture's line, each transformed with its stroke.
	 *
	 * @param outline the drawing's outline, in its frame
	 * @param box the box the presets' points are on: the drawing's, with its line where it has one
	 * @return the fills in drawing order; none where the shadow has no colour
	 */
	public static List<Fill> presetShadow(org.docx4j.dml.CTPresetShadowEffect s, ThemeColors theme, java.awt.Shape outline, Rectangle2D box) {
		List<Fill> out = new ArrayList<Fill>();
		Color[] colours = presetColours(s, theme);
		if (colours == null || outline == null || box == null) return out;
		for (Object[] l : presetLayers(s, box)) {
			out.add(new Fill(((AffineTransform) l[0]).createTransformedShape(outline), colours[(Integer) l[1]]));
		}
		return out;
	}

	/**
	 * A preset shadow as pictures, from what the painter paints (its alpha), as Word draws one for a transparent picture
	 * (fxp3-02, -07: the shadow of a line art's strokes, not of its box): each fill of {@link #presetShadow} as a
	 * picture at 200 dpi, unblurred, under the drawing.
	 *
	 * @param bounds what the painter paints, in the drawing's frame
	 * @since 17.3.7 (CR-081 phase 4)
	 */
	public static List<Picture> presetShadowPictures(org.docx4j.dml.CTPresetShadowEffect s, ThemeColors theme, Painter painter, Rectangle2D box,
			Rectangle2D bounds, double maxPixelsPerPoint) {
		List<Picture> out = new ArrayList<Picture>();
		Color[] colours = presetColours(s, theme);
		if (colours == null || painter == null || box == null || bounds == null) return out;
		double cap = maxPixelsPerPoint > 0 ? maxPixelsPerPoint : Double.MAX_VALUE;
		for (Object[] l : presetLayers(s, box)) {
			AffineTransform t = (AffineTransform) l[0];
			Rectangle2D area = t.createTransformedShape(bounds).getBounds2D();
			double ppt = resolution(area, 1, cap);
			if (ppt <= 0) continue;
			Grid g = Grid.of(area, ppt);
			out.add(g.picture(g.alpha(painter, t), colours[(Integer) l[1]], Layer.UNDER, "prstShdw"));
		}
		return out;
	}

	/**
	 * The shadow's colour and its second fill's (shdw13, 17, 18): each channel plus 0.4, to white at most (fxp2: black
	 * 0.4 grey, red FF6666, accent1 156082 7BC6E8, blue 6464FF), the alpha the colour's; null where it has none
	 */
	private static Color[] presetColours(org.docx4j.dml.CTPresetShadowEffect s, ThemeColors theme) {
		if (s == null || s.getPrst() == null) return null;
		Color c = colour(s.getSrgbClr(), s.getSchemeClr(), s.getPrstClr(), s.getSysClr(), s.getHslClr(), s.getScrgbClr(), theme);
		if (c == null) return null;
		Color second = new Color(Math.min(255, c.getRed() + 102), Math.min(255, c.getGreen() + 102), Math.min(255, c.getBlue() + 102), c.getAlpha());
		return new Color[] { c, second };
	}

	/** Each fill of a preset, in drawing order: its transform in the drawing's frame, and 1 for the second fill's colour */
	private static List<Object[]> presetLayers(org.docx4j.dml.CTPresetShadowEffect s, Rectangle2D box) {
		List<Object[]> out = new ArrayList<Object[]>();
		int n;
		try {
			n = Integer.parseInt(s.getPrst().value().substring(4));
		} catch (RuntimeException e) {
			return out;
		}
		double sx = 1, sy = 1, shear = 0, ax = 0, ay = 0;
		switch (n) {
			case 3: case 11: sy = 0.5; shear = PRESET_SHEAR; ax = 0.5; ay = 1; break;
			case 4: case 12: sy = 0.5; shear = -PRESET_SHEAR; ax = 0.5; ay = 1; break;
			case 7: case 15: sy = -0.5; shear = PRESET_SHEAR; ax = 0.5; ay = 1; break;
			case 8: case 16: sy = -0.5; shear = -PRESET_SHEAR; ax = 0.5; ay = 1; break;
			case 9: sx = sy = 0.75; break;
			case 10: sx = sy = 1.25; ax = 1; ay = 1; break;
			case 19: sy = 0.5; ax = 0.5; ay = 1; break;
			case 20: sy = -1; ax = 0.5; ay = 1; break;
			default: break;
		}
		double px = box.getX() + ax * box.getWidth(), py = box.getY() + ay * box.getHeight();
		double dist = s.getDist() / 12700.0, dir = Math.toRadians(s.getDir() / 60000.0);
		double dx = dist * Math.cos(dir), dy = dist * Math.sin(dir);
		double[][] layers = n == 13 ? new double[][] { { 2, 1 }, { 1, 0 } } : n == 17 || n == 18 ? new double[][] { { -1, 1 }, { 1, 0 } }
				: new double[][] { { 1, 0 } };
		for (double[] l : layers) {
			AffineTransform t = AffineTransform.getTranslateInstance(l[0] * dx, l[0] * dy);
			t.translate(px, py);
			t.concatenate(new AffineTransform(sx, 0, shear, sy, 0, 0));
			t.translate(-px, -py);
			out.add(new Object[] { t, (int) l[1] });
		}
		return out;
	}

	/** The fade's alpha at f, 0 to 1 across the reflection along fadeDir */
	static double fade(double f, double stA, double stPos, double endA, double endPos) {
		if (stPos > endPos) return f <= stPos ? stA : 0;
		if (f <= stPos) return stA;
		if (f >= endPos) return endA;
		return stA + (endA - stA) * (f - stPos) / (endPos - stPos);
	}

	// ------------------------------------------------------------------ geometry

	/** The algn point's place in the box, 0 to 1 each way (b by default): tl, t, tr, l, ctr, r, bl, b, br */
	static double[] align(STRectAlignment a) {
		String s = a == null ? "b" : a.value();
		if (s.equals("ctr")) return new double[] { 0.5, 0.5 };
		return new double[] { s.endsWith("l") ? 0 : s.endsWith("r") ? 1 : 0.5, s.startsWith("t") ? 0 : s.startsWith("b") ? 1 : 0.5 };
	}

	/**
	 * Scaled and skewed about the algn point of the box by the one matrix [sx, tan kx; tan ky, sy], then moved dist along
	 * dir (degrees clockwise from +x). Neither product of a scale and a shear fits Word: a perspective shadow (sy 23%,
	 * kx 20°) leans by tan 20° of the picture's whole height, not of its scaled height (fxr12-21 to 25), which a scale
	 * after the skew would give; a reflection (sy -100%) skewed by kx leans its far end left (fxr50-28) and by ky leans
	 * its right side down (fxr50-29), where a scale after the skew turns the one and a scale before it the other. Phase
	 * 1's probes have sx 100% wherever there is a skew, so sx tan kx is not told from tan kx; the picture styles'
	 * Perspective Shadow (sx 97%, kx 15°) can tell them (phase 2).
	 */
	static AffineTransform placed(Rectangle2D box, double[] al, double dist, double dir, double sx, double sy, double kx, double ky) {
		double px = box.getX() + al[0] * box.getWidth(), py = box.getY() + al[1] * box.getHeight();
		AffineTransform t = new AffineTransform();
		t.translate(dist * Math.cos(Math.toRadians(dir)), dist * Math.sin(Math.toRadians(dir)));
		t.translate(px, py);
		t.concatenate(new AffineTransform(sx, Math.tan(Math.toRadians(ky)), Math.tan(Math.toRadians(kx)), sy, 0, 0));
		t.translate(-px, -py);
		return t;
	}

	private static List<Point2D> corners(Rectangle2D b, AffineTransform t) {
		List<Point2D> out = new ArrayList<Point2D>();
		for (double[] c : new double[][] { { b.getMinX(), b.getMinY() }, { b.getMaxX(), b.getMinY() }, { b.getMinX(), b.getMaxY() },
				{ b.getMaxX(), b.getMaxY() } }) {
			out.add(t.transform(new Point2D.Double(c[0], c[1]), null));
		}
		return out;
	}

	private static Rectangle2D grow(Rectangle2D r, double d) {
		return new Rectangle2D.Double(r.getX() - d, r.getY() - d, r.getWidth() + 2 * d, r.getHeight() + 2 * d);
	}

	/** Word's coarsening: a whole factor k for a blur of this many points */
	static int k(double blurPoints) {
		return Math.max(1, (int) Math.ceil(blurPoints * BASE_PIXELS_PER_POINT / BLUR_PIXELS - 1e-9));
	}

	/**
	 * The resolution for an area at k, within the property's bound and D10's cap; 0 where the cap would make it coarser
	 * than {@link #MIN_PIXELS_PER_POINT} (Word's own coarsening of a wide blur goes below it, and is kept)
	 */
	static double resolution(Rectangle2D area, int k, double cap) {
		double wanted = Math.min(BASE_PIXELS_PER_POINT / k, cap);
		double ppt = wanted;
		double w = area.getWidth(), h = area.getHeight();
		if (w > 0) ppt = Math.min(ppt, MAX_SIDE / w);
		if (h > 0) ppt = Math.min(ppt, MAX_SIDE / h);
		if (w > 0 && h > 0) ppt = Math.min(ppt, Math.sqrt(MAX_PIXELS / (w * h)));
		if (!(ppt > 0) || Double.isInfinite(ppt)) return 0;
		return ppt < wanted && ppt < MIN_PIXELS_PER_POINT ? 0 : ppt;
	}

	private static Color colour(Object srgb, Object scheme, Object prst, Object sys, Object hsl, Object scrgb, ThemeColors theme) {
		Object c = scheme != null ? scheme : srgb != null ? srgb : prst != null ? prst : sys != null ? sys : hsl != null ? hsl : scrgb;
		if (c == null) return null;
		return Fills.awt(DrawingMLColors.resolve(c, theme));
	}

	// ------------------------------------------------------------------ rasters

	/** An area of the drawing's frame at a resolution, its pixels whole */
	static final class Grid {
		final double x0, y0, ppt;
		final int w, h;

		private Grid(double x0, double y0, int w, int h, double ppt) {
			this.x0 = x0;
			this.y0 = y0;
			this.w = w;
			this.h = h;
			this.ppt = ppt;
		}

		static Grid of(Rectangle2D area, double ppt) {
			return new Grid(area.getX(), area.getY(), Math.max(1, (int) Math.ceil(area.getWidth() * ppt)),
					Math.max(1, (int) Math.ceil(area.getHeight() * ppt)), ppt);
		}

		/** This grid with n pixels more on each side */
		Grid grown(int n) {
			return new Grid(x0 - n / ppt, y0 - n / ppt, w + 2 * n, h + 2 * n, ppt);
		}

		Rectangle2D.Double place() {
			return new Rectangle2D.Double(x0, y0, w / ppt, h / ppt);
		}

		/** The drawing painted here (moved by t, where given), in its colours */
		BufferedImage colour(Painter painter, AffineTransform t) {
			BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = im.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.scale(ppt, ppt);
			g.translate(-x0, -y0);
			if (t != null) g.transform(t);
			painter.paint(g);
			g.dispose();
			return im;
		}

		/** The painter's paint over what the picture (of this grid) holds */
		void paintOver(BufferedImage im, Painter painter) {
			Graphics2D g = im.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.scale(ppt, ppt);
			g.translate(-x0, -y0);
			painter.paint(g);
			g.dispose();
		}

		/** The drawing's silhouette here: its alpha, 0 to 1 */
		float[] alpha(Painter painter, AffineTransform t) {
			BufferedImage im = colour(painter, t);
			int[] px = im.getRGB(0, 0, w, h, null, 0, w);
			float[] a = new float[px.length];
			for (int i = 0; i < px.length; i++) a[i] = (px[i] >>> 24) / 255f;
			return a;
		}

		/** A picture of this alpha in the colour, its alpha the colour's at most */
		Picture picture(float[] a, Color c, Layer layer, String effect) {
			BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			int rgb = c.getRGB() & 0xFFFFFF;
			double peak = c.getAlpha() / 255.0;
			int[] px = new int[w * h];
			for (int i = 0; i < px.length; i++) px[i] = ((int) Math.round(Math.max(0, Math.min(1, a[i])) * peak * 255) << 24) | rgb;
			im.setRGB(0, 0, w, h, px, 0, w);
			return new Picture(im, place(), layer, effect);
		}
	}

	/** The colour at alpha a, painted over each pixel */
	private static void over(int[] px, float[] a, Color c) {
		double peak = c.getAlpha() / 255.0;
		int cr = c.getRed(), cg = c.getGreen(), cb = c.getBlue();
		for (int i = 0; i < px.length; i++) {
			double s = Math.max(0, Math.min(1, a[i])) * peak;
			if (s <= 0) continue;
			double da = (px[i] >>> 24) / 255.0;
			double oa = s + da * (1 - s);
			if (oa <= 0) continue;
			int r = (int) Math.round((cr * s + ((px[i] >> 16) & 0xFF) * da * (1 - s)) / oa);
			int g = (int) Math.round((cg * s + ((px[i] >> 8) & 0xFF) * da * (1 - s)) / oa);
			int b = (int) Math.round((cb * s + (px[i] & 0xFF) * da * (1 - s)) / oa);
			// within the drawing: its alpha kept, as the inner shadow is clipped to it
			int alpha = (px[i] >>> 24);
			px[i] = (alpha << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
		}
	}

	private static int clamp(double v) {
		return (int) Math.max(0, Math.min(255, Math.round(v)));
	}

	/** Bilinear, outside the grid the value given */
	private static float sample(float[] a, int w, int h, double x, double y, float outside) {
		double fx = x - 0.5, fy = y - 0.5;
		int x0 = (int) Math.floor(fx), y0 = (int) Math.floor(fy);
		double tx = fx - x0, ty = fy - y0;
		return (float) ((1 - ty) * ((1 - tx) * at(a, w, h, x0, y0, outside) + tx * at(a, w, h, x0 + 1, y0, outside))
				+ ty * ((1 - tx) * at(a, w, h, x0, y0 + 1, outside) + tx * at(a, w, h, x0 + 1, y0 + 1, outside)));
	}

	private static float at(float[] a, int w, int h, int x, int y, float outside) {
		return x < 0 || y < 0 || x >= w || y >= h ? outside : a[y * w + x];
	}

	/**
	 * A separable Gaussian, sigma in pixels; outside the grid is 0. A kernel wider than the grid is cut to it, weighed
	 * as the whole Gaussian is (no tap past the grid's size can fall in it)
	 */
	static float[] blur(float[] a, int w, int h, double sigma) {
		if (!(sigma > 0)) return a;
		double reach = Math.ceil(3 * sigma);
		if (reach < 1) return a;
		int r = (int) Math.min(reach, Math.max(w, h));
		float[] k = new float[2 * r + 1];
		double sum = 0;
		for (int i = -r; i <= r; i++) sum += k[i + r] = (float) Math.exp(-(double) i * i / (2 * sigma * sigma));
		if (r < reach) sum = sigma * Math.sqrt(2 * Math.PI);
		for (int i = 0; i < k.length; i++) k[i] /= sum;
		float[] t = new float[a.length], o = new float[a.length];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				float v = 0;
				for (int i = Math.max(-r, -x); i <= Math.min(r, w - 1 - x); i++) v += k[i + r] * a[y * w + x + i];
				t[y * w + x] = v;
			}
		}
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				float v = 0;
				for (int i = Math.max(-r, -y); i <= Math.min(r, h - 1 - y); i++) v += k[i + r] * t[(y + i) * w + x];
				o[y * w + x] = v;
			}
		}
		return o;
	}

	/** The silhouette (alpha at least a half) grown by d pixels, its edge antialiased */
	static float[] dilate(float[] a, int w, int h, double d) {
		boolean[] in = new boolean[a.length];
		for (int i = 0; i < a.length; i++) in[i] = a[i] >= 0.5f;
		float[] dist = distance(in, w, h);
		float[] o = new float[a.length];
		for (int i = 0; i < a.length; i++) o[i] = in[i] ? 1 : (float) Math.max(0, Math.min(1, d - dist[i] + 1));
		return o;
	}

	/** The silhouette (alpha at least a half) shrunk by d pixels, its edge antialiased */
	static float[] erode(float[] a, int w, int h, double d) {
		boolean[] out = new boolean[a.length];
		for (int i = 0; i < a.length; i++) out[i] = a[i] < 0.5f;
		// outside the grid is outside: a frame of outside pixels round it
		int fw = w + 2, fh = h + 2;
		boolean[] framed = new boolean[fw * fh];
		for (int y = 0; y < fh; y++) for (int x = 0; x < fw; x++) framed[y * fw + x] = x == 0 || y == 0 || x == fw - 1 || y == fh - 1 || out[(y - 1) * w + x - 1];
		float[] dist = distance(framed, fw, fh);
		float[] o = new float[a.length];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int i = y * w + x;
				o[i] = out[i] ? 0 : (float) Math.max(0, Math.min(1, dist[(y + 1) * fw + x + 1] - d));
			}
		}
		return o;
	}

	/** The Euclidean distance from each pixel to the nearest of the set (Felzenszwalb and Huttenlocher's transform) */
	static float[] distance(boolean[] set, int w, int h) {
		final double inf = 1e20;
		double[] f = new double[Math.max(w, h)], d = new double[Math.max(w, h)];
		double[] g = new double[w * h];
		for (int i = 0; i < g.length; i++) g[i] = set[i] ? 0 : inf;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) f[y] = g[y * w + x];
			dt(f, h, d);
			for (int y = 0; y < h; y++) g[y * w + x] = d[y];
		}
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) f[x] = g[y * w + x];
			dt(f, w, d);
			for (int x = 0; x < w; x++) g[y * w + x] = d[x];
		}
		float[] out = new float[g.length];
		for (int i = 0; i < g.length; i++) out[i] = (float) Math.sqrt(g[i]);
		return out;
	}

	private static void dt(double[] f, int n, double[] d) {
		int[] v = new int[n];
		double[] z = new double[n + 1];
		int k = 0;
		v[0] = 0;
		z[0] = -Double.MAX_VALUE;
		z[1] = Double.MAX_VALUE;
		for (int q = 1; q < n; q++) {
			double s = ((f[q] + (double) q * q) - (f[v[k]] + (double) v[k] * v[k])) / (2.0 * q - 2.0 * v[k]);
			while (s <= z[k]) {
				k--;
				s = ((f[q] + (double) q * q) - (f[v[k]] + (double) v[k] * v[k])) / (2.0 * q - 2.0 * v[k]);
			}
			k++;
			v[k] = q;
			z[k] = s;
			z[k + 1] = Double.MAX_VALUE;
		}
		k = 0;
		for (int q = 0; q < n; q++) {
			while (z[k + 1] < q) k++;
			d[q] = (double) (q - v[k]) * (q - v[k]) + f[v[k]];
		}
	}
}
