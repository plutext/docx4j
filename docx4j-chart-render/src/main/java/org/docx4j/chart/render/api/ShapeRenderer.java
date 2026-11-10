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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.docx4j.dml.CTBlipFillProperties;
import org.docx4j.dml.CTGradientFillProperties;
import org.docx4j.dml.CTLineEndProperties;
import org.docx4j.dml.CTLineProperties;
import org.docx4j.dml.CTPatternFillProperties;
import org.docx4j.dml.CTRelativeRect;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTShapeStyle;
import org.docx4j.dml.CTSolidColorFillProperties;
import org.docx4j.dml.CTTransform2D;
import org.docx4j.dml.STPathFillMode;
import org.docx4j.model.color.DrawingMLColors;
import org.docx4j.model.color.ResolvedColor;
import org.docx4j.model.color.StyleFill;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.shapes.ShapeGeometry;
import org.docx4j.model.shapes.ShapeOutline;
import org.docx4j.model.shapes.ShapeOutlines;
import org.w3c.dom.Document;

/**
 * A DrawingML shape ({@code wps:wsp}'s {@code wps:spPr} and {@code wps:style}) drawn as Word draws it in a PDF
 * (CR-080 phase 2): its geometry ({@link ShapeGeometry}), its fill and its line, the shape's own where it states them
 * and its style's references through the theme otherwise.
 * <ul>
 * <li>Fills: solid, with alpha; gradients; the preset patterns; pictures, stretched or tiled. A path's fill mode
 * lightens or darkens the fill as Word does: {@code lighten} 40% toward white, {@code lightenLess} 20%,
 * {@code darken} to 60%, {@code darkenLess} to 80.7%, measured on 4472C4.</li>
 * <li>Lines: Word's width (0 is a 0.14pt hairline), dashes in multiples of the width (with a round cap each dash a
 * width shorter and each gap a width longer), caps (flat where none is stated), joins (round where none is stated;
 * {@code a:miter}'s limit isn't used: Word's is 10), compound lines and inside lines as filled bands, and arrowheads,
 * their sizes 2, 3 and 5 times the larger of the width and 2pt.</li>
 * <li>The shape's own turn and flips ({@code a:xfrm}), about its box's centre.</li>
 * </ul>
 * Effects are CR-081's. A geometry that can't be drawn is its box, with the code its {@link ShapeOutlines} gives.
 *
 * @since 17.3.7
 */
public final class ShapeRenderer {

	/** A line of width 0, as Word draws it, in points */
	public static final double HAIRLINE = 0.14;

	/** Gives the picture a picture fill names by its relationship, or null where there is none */
	public interface Pictures {
		BufferedImage picture(String relationshipId);
	}

	/** A shape drawn: its drawing, the box it paints (from the shape's top left, in points), its geometry. */
	public static final class Drawn {
		private final Drawing drawing;
		private final Rectangle2D.Double bounds;
		private final ShapeOutlines outlines;
		private final List<String> problems;

		Drawn(Drawing drawing, Rectangle2D.Double bounds, ShapeOutlines outlines, List<String> problems) {
			this.drawing = drawing;
			this.bounds = bounds;
			this.outlines = outlines;
			this.problems = Collections.unmodifiableList(problems);
		}

		/** What is painted, in the shape's frame: points from its box's top left, y down */
		public Drawing getDrawing() {
			return drawing;
		}

		/** All it paints, its line and arrowheads included, in the shape's frame; it can reach past the box */
		public Rectangle2D.Double getBounds() {
			return bounds;
		}

		/** The geometry, its text rectangle included */
		public ShapeOutlines getOutlines() {
			return outlines;
		}

		/** The gaps (stable codes, CR-080 D9): the geometry's, and a picture fill whose picture can't be had */
		public List<String> getProblems() {
			return problems;
		}

		/** Nothing is painted */
		public boolean isEmpty() {
			return drawing.getItems().isEmpty();
		}

		/** As SVG, its canvas {@link #getBounds()}, so that (0, 0) is the box's top left */
		public Document toSvg() {
			return SvgWriter.toSvg(drawing, bounds);
		}
	}

	/** A picture fill whose picture couldn't be had: drawn as no fill */
	public static final String PICTURE_MISSING = "render.shape.picture_missing";

	private ShapeRenderer() {}

	/**
	 * The shape drawn at its size.
	 *
	 * @param spPr the shape's properties
	 * @param style the shape's style, or null
	 * @param theme its colours and format scheme, the document's
	 * @param cx its width in EMU
	 * @param cy its height in EMU
	 * @param pictures gives a picture fill's picture, or null where there are none
	 */
	public static Drawn draw(CTShapeProperties spPr, CTShapeStyle style, ThemeColors theme, long cx, long cy, Pictures pictures) {
		ShapeOutlines outlines = ShapeGeometry.outlines(spPr, cx, cy);
		List<String> problems = new ArrayList<String>();
		if (outlines.getProblem() != null) problems.add(outlines.getProblem());
		double w = cx / 12700.0, h = cy / 12700.0;
		Drawing drawing = new Drawing(w, h);
		AffineTransform turn = turn(spPr == null ? null : spPr.getXfrm(), w, h);

		Fill fill = fill(spPr, style, theme);
		String preset = spPr != null && spPr.getPrstGeom() != null && spPr.getPrstGeom().getPrst() != null ? spPr.getPrstGeom().getPrst().value() : null;
		Line line = line(spPr, style, theme, preset);
		Rectangle2D bounds = null;

		// the fills, then the lines, path by path, as Word paints them
		for (ShapeOutline o : outlines.getOutlines()) {
			if (fill == null || o.getFill() == STPathFillMode.NONE) continue;
			// Word fills by the even-odd rule (its PDF's f*), and a path of no area (line) not at all
			Path2D.Double evenOdd = new Path2D.Double(Path2D.WIND_EVEN_ODD);
			evenOdd.append(o.getPath(), false);
			Shape s = turn.createTransformedShape(evenOdd);
			if (new Area(s).isEmpty()) continue;
			Rectangle2D painted = fill.paint(drawing, s, o.getFill(), turn.createTransformedShape(new Rectangle2D.Double(0, 0, w, h)), pictures,
					problems);
			bounds = union(bounds, painted);
		}
		if (line != null) {
			List<ShapeOutline> stroked = new ArrayList<ShapeOutline>();
			for (ShapeOutline o : outlines.getOutlines()) if (o.isStroked()) stroked.add(o);
			for (int i = 0; i < stroked.size(); i++) {
				Path2D.Double p = stroked.get(i).getPath();
				bounds = union(bounds, line.paint(drawing, p, turn, i == 0, i == stroked.size() - 1));
			}
		}
		return new Drawn(drawing, bounds == null ? new Rectangle2D.Double(0, 0, w, h) : rect(bounds), outlines, problems);
	}

	private static Rectangle2D.Double rect(Rectangle2D r) {
		return new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	private static Rectangle2D union(Rectangle2D a, Rectangle2D b) {
		if (b == null || b.isEmpty() && b.getWidth() == 0 && b.getHeight() == 0) return a;
		if (a == null) return b.getBounds2D();
		Rectangle2D u = a.getBounds2D();
		Rectangle2D.union(u, b, u);
		return u;
	}

	/** The shape's turn and flips about its box's centre: flipped, then turned */
	static AffineTransform turn(CTTransform2D xfrm, double w, double h) {
		AffineTransform at = new AffineTransform();
		if (xfrm == null || (xfrm.getRot() == 0 && !xfrm.isFlipH() && !xfrm.isFlipV())) return at;
		at.translate(w / 2, h / 2);
		if (xfrm.getRot() != 0) at.rotate(Math.toRadians(xfrm.getRot() / 60000.0));
		at.scale(xfrm.isFlipH() ? -1 : 1, xfrm.isFlipV() ? -1 : 1);
		at.translate(-w / 2, -h / 2);
		return at;
	}

	// ------------------------------------------------------------------ the fill

	/** A shape's fill: its own where it states one, else its style's fillRef, with the theme that resolves it */
	private static final class Fill {
		final Object fill;
		final Fills fills;

		Fill(Object fill, Fills fills) {
			this.fill = fill;
			this.fills = fills;
		}

		/** The area painted; null where nothing is */
		Rectangle2D paint(Drawing d, Shape s, STPathFillMode mode, Shape box, Pictures pictures, List<String> problems) {
			if (fill instanceof CTSolidColorFillProperties) {
				Color c = Fills.awt(DrawingMLColors.resolve(fill, fills.getTheme()));
				if (c == null) return null;
				d.add(new Drawing.Area(s, modify(c, mode), null, 0, "shape.fill"));
			} else if (fill instanceof CTGradientFillProperties) {
				Drawing.Gradient g = fills.gradient((CTGradientFillProperties) fill);
				if (g == null) return null;
				Color[] cs = g.getColours().clone();
				for (int i = 0; i < cs.length; i++) cs[i] = modify(cs[i], mode);
				Drawing.Area a = new Drawing.Area(s, cs[0], null, 0, "shape.fill");
				a.setGradient(new Drawing.Gradient(g.getStops(), cs, g.isLinear(), g.getAngle(), g.isScaled(), g.getFocus()));
				d.add(a);
			} else if (fill instanceof CTPatternFillProperties) {
				CTShapeProperties holder = new CTShapeProperties();
				holder.setPattFill((CTPatternFillProperties) fill);
				Drawing.Pattern p = fills.pattern(holder);
				Drawing.Area a = new Drawing.Area(s, modify(p.getForeground(), mode), null, 0, "shape.fill");
				a.setPattern(new Drawing.Pattern(p.getTile(), modify(p.getForeground(), mode), modify(p.getBackground(), mode)));
				d.add(a);
			} else if (fill instanceof CTBlipFillProperties) {
				if (!picture(d, s, box.getBounds2D(), (CTBlipFillProperties) fill, pictures)) {
					if (!problems.contains(PICTURE_MISSING)) problems.add(PICTURE_MISSING);
					return null;
				}
			} else {
				return null;
			}
			return s.getBounds2D();
		}
	}

	private static Fill fill(CTShapeProperties sp, CTShapeStyle style, ThemeColors theme) {
		if (sp != null) {
			if (sp.getNoFill() != null) return null;
			Object own = sp.getSolidFill() != null ? sp.getSolidFill() : sp.getGradFill() != null ? sp.getGradFill()
					: sp.getPattFill() != null ? sp.getPattFill() : sp.getBlipFill() != null ? sp.getBlipFill() : null;
			if (own != null) return new Fill(own, new Fills(theme));
			// a:grpFill is a group's: phase 3
			if (sp.getGrpFill() != null) return null;
		}
		if (style == null || style.getFillRef() == null) return null;
		StyleFill sf = DrawingMLColors.fillReference(style.getFillRef(), theme);
		if (sf.getFill() == null) return null;
		return new Fill(sf.getFill(), new Fills(theme.withPlaceholder(DrawingMLColors.resolve(style.getFillRef(), theme))));
	}

	/** The fill mode's lightening or darkening, as Word's (CR-080 phase 0, on 4472C4) */
	static Color modify(Color c, STPathFillMode mode) {
		if (c == null || mode == null) return c;
		double f;
		boolean lighten;
		switch (mode) {
			case LIGHTEN: f = 0.4; lighten = true; break;
			case LIGHTEN_LESS: f = 0.2; lighten = true; break;
			case DARKEN: f = 0.6; lighten = false; break;
			case DARKEN_LESS: f = 0.807; lighten = false; break;
			default: return c;
		}
		int[] v = { c.getRed(), c.getGreen(), c.getBlue() };
		for (int i = 0; i < 3; i++) v[i] = (int) Math.round(lighten ? v[i] + f * (255 - v[i]) : v[i] * f);
		return new Color(v[0], v[1], v[2], c.getAlpha());
	}

	/** A picture fill, stretched (into its fillRect, cropped by its srcRect) or tiled; false where there is no picture */
	private static boolean picture(Drawing d, Shape s, Rectangle2D box, CTBlipFillProperties bf, Pictures pictures) {
		String id = bf.getBlip() == null ? null : bf.getBlip().getEmbed();
		BufferedImage img = id == null || pictures == null ? null : pictures.picture(id);
		if (img == null) return false;
		img = crop(img, bf.getSrcRect());
		if (bf.getTile() != null) {
			org.docx4j.dml.CTTileInfoProperties t = bf.getTile();
			// the picture's own size, at 96 dpi where it says nothing, scaled
			double tw = img.getWidth() * 72.0 / 96 * (t.getSx() == null ? 1 : t.getSx() / 100000.0);
			double th = img.getHeight() * 72.0 / 96 * (t.getSy() == null ? 1 : t.getSy() / 100000.0);
			String flip = t.getFlip() == null ? "none" : t.getFlip().value();
			BufferedImage tile = flip(img, flip);
			double cellW = flip.contains("x") ? 2 * tw : tw, cellH = flip.contains("y") ? 2 * th : th;
			String algn = t.getAlgn() == null ? "tl" : t.getAlgn().value();
			double x = box.getX() + (algn.endsWith("r") ? box.getWidth() - tw : algn.endsWith("l") ? 0 : (box.getWidth() - tw) / 2);
			double y = box.getY() + (algn.startsWith("b") ? box.getHeight() - th : algn.startsWith("t") ? 0 : (box.getHeight() - th) / 2);
			x += t.getTx() == null ? 0 : t.getTx() / 12700.0;
			y += t.getTy() == null ? 0 : t.getTy() / 12700.0;
			Drawing.Area a = new Drawing.Area(s, null, null, 0, "shape.fill");
			a.setPaint(new java.awt.TexturePaint(tile, new Rectangle2D.Double(x, y, cellW, cellH)));
			d.add(a);
			return true;
		}
		CTRelativeRect fr = bf.getStretch() == null ? null : bf.getStretch().getFillRect();
		Rectangle2D into = box;
		if (fr != null) {
			double bw = box.getWidth(), bh = box.getHeight();
			into = new Rectangle2D.Double(box.getX() + bw * fr.getL() / 100000.0, box.getY() + bh * fr.getT() / 100000.0,
					bw * (1 - (fr.getL() + fr.getR()) / 100000.0), bh * (1 - (fr.getT() + fr.getB()) / 100000.0));
		}
		d.add(new Drawing.Picture(img, into, s));
		return true;
	}

	private static BufferedImage crop(BufferedImage img, CTRelativeRect src) {
		if (src == null || (src.getL() == 0 && src.getT() == 0 && src.getR() == 0 && src.getB() == 0)) return img;
		int w = img.getWidth(), h = img.getHeight();
		int x0 = clamp((int) Math.round(w * src.getL() / 100000.0), 0, w - 1), y0 = clamp((int) Math.round(h * src.getT() / 100000.0), 0, h - 1);
		int x1 = clamp((int) Math.round(w * (1 - src.getR() / 100000.0)), x0 + 1, w), y1 = clamp((int) Math.round(h * (1 - src.getB() / 100000.0)), y0 + 1, h);
		return img.getSubimage(x0, y0, x1 - x0, y1 - y0);
	}

	private static int clamp(int v, int lo, int hi) {
		return Math.max(lo, Math.min(hi, v));
	}

	/** A tile flipped as {@code a:tile/@flip} has it: the picture beside its mirror image across, down or both */
	private static BufferedImage flip(BufferedImage img, String flip) {
		if ("none".equals(flip)) return img;
		boolean fx = flip.contains("x"), fy = flip.contains("y");
		int w = img.getWidth(), h = img.getHeight();
		BufferedImage out = new BufferedImage(fx ? 2 * w : w, fy ? 2 * h : h, BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g = out.createGraphics();
		g.drawImage(img, 0, 0, null);
		if (fx) g.drawImage(img, 2 * w, 0, -w, h, null);
		if (fy) g.drawImage(img, 0, 2 * h, w, -h, null);
		if (fx && fy) g.drawImage(img, 2 * w, 2 * h, -w, -h, null);
		g.dispose();
		return out;
	}

	// ------------------------------------------------------------------ the line

	/** A shape's line, its own a:ln over its style's lnRef, attribute by attribute (CR-080 phase 0: fp50-012) */
	private static final class Line {
		Object paint; // the line's fill: solid, gradient or pattern
		Fills fills;
		double width;
		String cap = "flat", cmpd = "sng", algn = "ctr", dash;
		int join = BasicStroke.JOIN_ROUND;
		List<org.docx4j.dml.CTDashStop> custDash;
		CTLineEndProperties head, tail;

		/** Paints a stroked path, with its arrowheads where it is the first or the last; the area painted */
		Rectangle2D paint(Drawing d, Path2D.Double path, AffineTransform turn, boolean first, boolean last) {
			Color colour = paint instanceof CTSolidColorFillProperties ? Fills.awt(DrawingMLColors.resolve(paint, fills.getTheme())) : null;
			Drawing.Gradient gradient = paint instanceof CTGradientFillProperties ? fills.gradient((CTGradientFillProperties) paint) : null;
			Drawing.Pattern pattern = null;
			if (paint instanceof CTPatternFillProperties) {
				CTShapeProperties holder = new CTShapeProperties();
				holder.setPattFill((CTPatternFillProperties) paint);
				pattern = fills.pattern(holder);
			}
			if (colour == null && gradient != null) colour = gradient.getColours()[0];
			if (colour == null && pattern != null) colour = pattern.getForeground();
			if (colour == null) return null;

			boolean open = isOpen(path);
			// a rectangle as Word's PDF draws it, an re from its bottom left, so that its dashes start there
			Shape shape = turn.createTransformedShape(asRe(path));
			Rectangle2D painted = null;

			// arrowheads on the open ends of the first and last stroked paths, the line drawn up to them
			List<Shape> heads = new ArrayList<Shape>();
			if (open && (first && head != null || last && tail != null)) {
				List<double[]> pts = flatten(shape);
				if (pts.size() >= 2) {
					double headBack = 0, tailBack = 0;
					if (first && end(head) != null) {
						double[] dir = direction(pts, true, length(head, width));
						Head hd = head(head, width, pts.get(0), dir);
						if (hd != null) {
							heads.add(hd.shape);
							headBack = hd.back;
						}
					}
					if (last && end(tail) != null) {
						double[] dir = direction(pts, false, length(tail, width));
						Head hd = head(tail, width, pts.get(pts.size() - 1), dir);
						if (hd != null) {
							heads.add(hd.shape);
							tailBack = hd.back;
						}
					}
					if (headBack > 0 || tailBack > 0) shape = trim(pts, headBack, tailBack);
				}
			}

			double w = width <= 0 ? HAIRLINE : width;
			boolean compound = !"sng".equals(cmpd), inside = "in".equals(algn) && !open;
			if (compound || inside) {
				// Word draws these as filled bands (phase 0: fp30-056 to -059, -061)
				Area band = band(shape, w, open, compound ? cmpd : "sng", inside);
				Drawing.Area a = new Drawing.Area(band, colour, null, 0, "shape.line");
				if (gradient != null) a.setGradient(gradient);
				else if (pattern != null) a.setPattern(pattern);
				d.add(a);
				painted = band.getBounds2D();
			} else {
				Stroke st = stroke(w);
				Drawing.Area a = new Drawing.Area(shape, null, colour, w, "shape.line");
				a.setStroke(st);
				if (gradient != null) a.setLineGradient(gradient);
				else if (pattern != null) a.setLinePattern(pattern);
				d.add(a);
				painted = st.createStrokedShape(shape).getBounds2D();
			}
			for (Shape hd : heads) {
				Drawing.Area a = new Drawing.Area(hd, colour, null, 0, "shape.arrowhead");
				d.add(a);
				painted = union(painted, hd.getBounds2D());
			}
			return painted;
		}

		Stroke stroke(double w) {
			int c = "rnd".equals(cap) ? BasicStroke.CAP_ROUND : "sq".equals(cap) ? BasicStroke.CAP_SQUARE : BasicStroke.CAP_BUTT;
			float[] dashes = dashes(w);
			// a:miter's lim isn't used: Word's PDF has the default limit, 10 (phase 0: fp30-052, -053)
			return dashes == null ? new BasicStroke((float) w, c, join, 10f) : new BasicStroke((float) w, c, join, 10f, dashes, 0f);
		}

		/** The dashes in points: the preset's or custDash's, in multiples of the width; with a round cap, each dash a width shorter */
		float[] dashes(double w) {
			float[] u = null;
			if (custDash != null && !custDash.isEmpty()) {
				u = new float[custDash.size() * 2];
				for (int i = 0; i < custDash.size(); i++) {
					u[2 * i] = custDash.get(i).getD() / 100000f;
					u[2 * i + 1] = custDash.get(i).getSp() / 100000f;
				}
			} else if (dash != null) {
				CTShapeProperties holder = new CTShapeProperties();
				CTLineProperties ln = new CTLineProperties();
				org.docx4j.dml.CTPresetLineDashProperties pd = new org.docx4j.dml.CTPresetLineDashProperties();
				pd.setVal(org.docx4j.dml.STPresetLineDashVal.fromValue(dash));
				ln.setPrstDash(pd);
				holder.setLn(ln);
				float[] f = Fills.dashes(holder, 1);
				u = f;
			}
			if (u == null) return null;
			float[] out = new float[u.length];
			// a round cap reaches half a width past each end of a dash: Word takes that off the dash (fp30-033 to
			// -043); a square cap's dashes aren't measured, and are left as the preset's
			boolean round = "rnd".equals(cap);
			for (int i = 0; i < u.length; i++) {
				double v = u[i] * w;
				if (round) v += (i % 2 == 0) ? -w : w;
				out[i] = (float) Math.max(i % 2 == 0 ? 0.001 : 0, v);
			}
			return out;
		}
	}

	/**
	 * The presets Word strokes with a mitred join where no join is stated (phase 0: fp10, fp11, 31 of 187); the rest
	 * it rounds. Office's legacy shape types differ so: rightArrow is mitred and leftArrow round.
	 */
	static final java.util.Set<String> MITRED = new java.util.HashSet<String>(java.util.Arrays.asList("bentUpArrow", "chevron", "cube",
			"downArrow", "ellipseRibbon", "ellipseRibbon2", "leftArrowCallout", "leftRightArrowCallout", "leftUpArrow", "mathEqual", "mathMinus",
			"mathMultiply", "mathNotEqual", "mathPlus", "moon", "notchedRightArrow", "plus", "quadArrowCallout", "rightArrow", "star10", "star12",
			"star16", "star24", "star32", "star4", "star5", "star8", "stripedRightArrow", "upArrow", "upDownArrow", "upDownArrowCallout"));

	private static Line line(CTShapeProperties sp, CTShapeStyle style, ThemeColors theme, String preset) {
		CTLineProperties own = sp == null ? null : sp.getLn();
		CTLineProperties base = null;
		ThemeColors baseTheme = theme;
		if (style != null && style.getLnRef() != null) {
			long idx = style.getLnRef().getIdx();
			org.docx4j.dml.CTStyleMatrix fmt = theme == null ? null : theme.getFormatScheme();
			if (idx > 0 && fmt != null && fmt.getLnStyleLst() != null && idx <= fmt.getLnStyleLst().getLn().size()) {
				base = fmt.getLnStyleLst().getLn().get((int) idx - 1);
				baseTheme = theme.withPlaceholder(DrawingMLColors.resolve(style.getLnRef(), theme));
			}
		}
		if (own == null && base == null) return null;
		Line l = new Line();
		if (preset != null && MITRED.contains(preset)) l.join = BasicStroke.JOIN_MITER;
		// the line's fill: the shape's own, else the style's
		Object ownPaint = own == null ? null : own.getNoFill() != null ? own.getNoFill() : own.getSolidFill() != null ? own.getSolidFill()
				: own.getGradFill() != null ? own.getGradFill() : own.getPattFill();
		if (ownPaint instanceof org.docx4j.dml.CTNoFillProperties) return null;
		if (ownPaint != null) {
			l.paint = ownPaint;
			l.fills = new Fills(theme);
		} else if (base != null) {
			Object bp = base.getNoFill() != null ? null : base.getSolidFill() != null ? base.getSolidFill()
					: base.getGradFill() != null ? base.getGradFill() : base.getPattFill();
			if (bp == null) return null;
			l.paint = bp;
			l.fills = new Fills(baseTheme);
		} else {
			return null;
		}
		Integer w = own != null && own.getW() != null ? own.getW() : base != null ? base.getW() : null;
		l.width = w == null ? 0.75 : w / 12700.0;
		CTLineProperties[] order = { base, own };
		for (CTLineProperties p : order) {
			if (p == null) continue;
			if (p.getCap() != null) l.cap = p.getCap().value();
			if (p.getCmpd() != null) l.cmpd = p.getCmpd().value();
			if (p.getAlgn() != null) l.algn = p.getAlgn().value();
			if (p.getPrstDash() != null && p.getPrstDash().getVal() != null) {
				l.dash = p.getPrstDash().getVal().value();
				l.custDash = null;
			}
			if (p.getCustDash() != null) {
				l.custDash = p.getCustDash().getDs();
				l.dash = null;
			}
			if (p.getRound() != null) l.join = BasicStroke.JOIN_ROUND;
			if (p.getBevel() != null) l.join = BasicStroke.JOIN_BEVEL;
			if (p.getMiter() != null) l.join = BasicStroke.JOIN_MITER;
			if (p.getHeadEnd() != null) l.head = p.getHeadEnd();
			if (p.getTailEnd() != null) l.tail = p.getTailEnd();
		}
		return l;
	}

	// ------------------------------------------------------------------ compound and inside lines

	/**
	 * A compound or inside line as the band Word fills, its offsets from the path in widths, outward positive
	 * (phase 0, at 9pt): dbl 1:1:1, thickThin 3:1:1 thick outside, thinThick 1:1:3, tri 1:1:2:1:1, and an inside line
	 * all of its width inside. An open path has no inside: there the bands are as on a closed path's outside, mirrored.
	 */
	static Area band(Shape shape, double w, boolean open, String cmpd, boolean inside) {
		double[][] bands;
		switch (cmpd) {
			case "dbl": bands = new double[][] { { 0.5, 1.0 / 6 }, { -1.0 / 6, -0.5 } }; break;
			case "thickThin": bands = new double[][] { { 0.5, -0.1 }, { -0.3, -0.5 } }; break;
			case "thinThick": bands = new double[][] { { 0.5, 0.3 }, { 0.1, -0.5 } }; break;
			case "tri": bands = new double[][] { { 0.5, 1.0 / 3 }, { 1.0 / 6, -1.0 / 6 }, { -1.0 / 3, -0.5 } }; break;
			default: bands = new double[][] { { 0.5, -0.5 } };
		}
		if (inside) {
			// the whole width inside the path
			for (double[] b : bands) {
				b[0] = b[0] - 0.5;
				b[1] = b[1] - 0.5;
			}
		}
		Area interior = open ? null : new Area(shape);
		Area out = new Area();
		for (double[] b : bands) {
			double outer = b[0] * w, inner = b[1] * w;
			out.add(open ? openBand(shape, Math.max(Math.abs(outer), Math.abs(inner)), Math.min(Math.abs(outer), Math.abs(inner)), outer * inner < 0)
					: closedBand(shape, interior, outer, inner));
		}
		return out;
	}

	/** The points at signed distance between inner and outer (outward positive) from a closed path */
	private static Area closedBand(Shape shape, Area interior, double outer, double inner) {
		Area band = new Area();
		if (outer > 0) {
			Area ring = within(shape, outer);
			if (inner > 0) ring.subtract(within(shape, inner));
			ring.subtract(interior);
			band.add(ring);
		}
		if (inner < 0) {
			Area ring = within(shape, -inner);
			if (outer < 0) ring.subtract(within(shape, -outer));
			ring.intersect(interior);
			band.add(ring);
		}
		return band;
	}

	private static Area openBand(Shape shape, double far, double near, boolean across) {
		Area ring = within(shape, far);
		if (!across && near > 0) ring.subtract(within(shape, near));
		return ring;
	}

	/** The points within d of the path: a round-joined stroke of width 2d, as Word's bands' outer corners are round */
	private static Area within(Shape shape, double d) {
		if (d <= 0) return new Area();
		return new Area(new BasicStroke((float) (2 * d), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND).createStrokedShape(shape));
	}

	// ------------------------------------------------------------------ arrowheads

	private static String end(CTLineEndProperties e) {
		if (e == null || e.getType() == null) return null;
		String t = e.getType().value();
		return "none".equals(t) ? null : t;
	}

	private static final class Head {
		final Shape shape;
		/** how far back from the end the line stops */
		final double back;

		Head(Shape shape, double back) {
			this.shape = shape;
			this.back = back;
		}
	}

	/** The units of an arrowhead's width and length: sm 2, med 3, lg 5 */
	private static double units(Object v) {
		String s = v == null ? "med" : v instanceof org.docx4j.dml.STLineEndWidth ? ((org.docx4j.dml.STLineEndWidth) v).value()
				: ((org.docx4j.dml.STLineEndLength) v).value();
		return "sm".equals(s) ? 2 : "lg".equals(s) ? 5 : 3;
	}

	/**
	 * An arrowhead at the end of a line, pointing along dir (a unit vector toward the end), as Word draws it (phase 0:
	 * fp31): with s the larger of the line's width and 2pt, its length L and width W are 2, 3 or 5 s. A triangle's tip
	 * is the end, the line stopping s/2 inside its base; a stealth's notch is s, 2s or 3s back, where the line stops; a
	 * diamond and an oval are centred on the end, where the line stops; an arrow is an open V of the line's width, its
	 * outer tip the end, its arms round-ended, 2s, 3s or 4.5s back and 1.25s, 1.75s or 2.5s across from its apex.
	 */
	static Head head(CTLineEndProperties e, double lineWidth, double[] end, double[] dir) {
		String type = end(e);
		if (type == null) return null;
		double s = Math.max(lineWidth, 2);
		double lu = units(e.getLen()), L = lu * s, W = units(e.getW()) * s;
		double ex = end[0], ey = end[1], ux = dir[0], uy = dir[1], nx = -uy, ny = ux;
		Path2D.Double p = new Path2D.Double();
		switch (type) {
			case "triangle":
				p.moveTo(ex, ey);
				p.lineTo(ex - L * ux + W / 2 * nx, ey - L * uy + W / 2 * ny);
				p.lineTo(ex - L * ux - W / 2 * nx, ey - L * uy - W / 2 * ny);
				p.closePath();
				return new Head(p, L - s / 2);
			case "stealth": {
				double notch = (lu == 2 ? 1 : lu == 3 ? 2 : 3) * s;
				p.moveTo(ex, ey);
				p.lineTo(ex - L * ux + W / 2 * nx, ey - L * uy + W / 2 * ny);
				p.lineTo(ex - notch * ux, ey - notch * uy);
				p.lineTo(ex - L * ux - W / 2 * nx, ey - L * uy - W / 2 * ny);
				p.closePath();
				return new Head(p, notch);
			}
			case "diamond":
				p.moveTo(ex + L / 2 * ux, ey + L / 2 * uy);
				p.lineTo(ex + W / 2 * nx, ey + W / 2 * ny);
				p.lineTo(ex - L / 2 * ux, ey - L / 2 * uy);
				p.lineTo(ex - W / 2 * nx, ey - W / 2 * ny);
				p.closePath();
				return new Head(p, 0);
			case "oval": {
				Shape o = new Ellipse2D.Double(-L / 2, -W / 2, L, W);
				AffineTransform at = new AffineTransform(ux, uy, nx, ny, ex, ey);
				return new Head(at.createTransformedShape(o), 0);
			}
			case "arrow": {
				/* An open V, its arms the line's width with round ends, its apex mitred, as Word draws it (phase 0: fp31-037
				 * to -045, -058 to -060). From the V's apex, each arm's end is 2s, 3s or 4.5s back and 1.25s, 1.75s or 2.5s
				 * across, for sm, med and lg; the apex is where the arms' outer edges meet at the end; the line stops at
				 * the apex. */
				double lw = Math.max(lineWidth, HAIRLINE), wu = units(e.getW());
				double back = (lu == 2 ? 2 : lu == 3 ? 3 : 4.5) * s, across = (wu == 2 ? 1.25 : wu == 3 ? 1.75 : 2.5) * s;
				double apex = lw / 2 / Math.sin(Math.atan2(across, back));
				double ax = ex - apex * ux, ay = ey - apex * uy;
				Path2D.Double v = new Path2D.Double();
				v.moveTo(ax - back * ux + across * nx, ay - back * uy + across * ny);
				v.lineTo(ax, ay);
				v.lineTo(ax - back * ux - across * nx, ay - back * uy - across * ny);
				Shape sh = new BasicStroke((float) lw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10f).createStrokedShape(v);
				return new Head(sh, apex);
			}
			default:
				return null;
		}
	}

	/** The path flattened to points, for the ends' directions and trimming */
	private static List<double[]> flatten(Shape s) {
		List<double[]> pts = new ArrayList<double[]>();
		double[] c = new double[6];
		for (PathIterator it = new FlatteningPathIterator(s.getPathIterator(null), 0.01, 12); !it.isDone(); it.next()) {
			int t = it.currentSegment(c);
			if (t == PathIterator.SEG_MOVETO && !pts.isEmpty()) break; // the first subpath only
			if (t == PathIterator.SEG_MOVETO || t == PathIterator.SEG_LINETO) {
				double[] last = pts.isEmpty() ? null : pts.get(pts.size() - 1);
				if (last == null || Math.hypot(c[0] - last[0], c[1] - last[1]) > 1e-9) pts.add(new double[] { c[0], c[1] });
			}
		}
		return pts;
	}

	/** An arrowhead's length in points: 2, 3 or 5 times the larger of the line's width and 2pt */
	private static double length(CTLineEndProperties e, double lineWidth) {
		return units(e.getLen()) * Math.max(lineWidth, 2);
	}

	/**
	 * The unit direction toward an end, along the chord from the point the head's length back along the path, so that
	 * on a curve the head's base sits on it (phase 0: fp31-068, -069)
	 */
	private static double[] direction(List<double[]> pts, boolean start, double length) {
		List<double[]> p = new ArrayList<double[]>(pts);
		if (start) Collections.reverse(p);
		double[] end = p.get(p.size() - 1), from = p.get(p.size() - 2);
		double left = length;
		for (int i = p.size() - 1; i > 0; i--) {
			double[] a = p.get(i - 1), b = p.get(i);
			double seg = Math.hypot(b[0] - a[0], b[1] - a[1]);
			if (seg >= left) {
				double t = seg == 0 ? 0 : left / seg;
				from = new double[] { b[0] + (a[0] - b[0]) * t, b[1] + (a[1] - b[1]) * t };
				break;
			}
			left -= seg;
			from = a;
		}
		double dx = end[0] - from[0], dy = end[1] - from[1], len = Math.hypot(dx, dy);
		return len == 0 ? new double[] { 1, 0 } : new double[] { dx / len, dy / len };
	}

	/** The polyline with headBack taken off its start and tailBack off its end */
	private static Shape trim(List<double[]> pts, double headBack, double tailBack) {
		List<double[]> p = new ArrayList<double[]>(pts);
		if (headBack > 0) p = cut(p, headBack);
		if (tailBack > 0) {
			Collections.reverse(p);
			p = cut(p, tailBack);
			Collections.reverse(p);
		}
		Path2D.Double out = new Path2D.Double();
		for (int i = 0; i < p.size(); i++) {
			if (i == 0) out.moveTo(p.get(i)[0], p.get(i)[1]);
			else out.lineTo(p.get(i)[0], p.get(i)[1]);
		}
		return out;
	}

	private static List<double[]> cut(List<double[]> p, double d) {
		double left = d;
		int i = 0;
		while (i < p.size() - 1) {
			double[] a = p.get(i), b = p.get(i + 1);
			double len = Math.hypot(b[0] - a[0], b[1] - a[1]);
			if (len >= left) {
				double t = len == 0 ? 0 : left / len;
				List<double[]> out = new ArrayList<double[]>();
				out.add(new double[] { a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t });
				out.addAll(p.subList(i + 1, p.size()));
				return out;
			}
			left -= len;
			i++;
		}
		// shorter than the cut: a point at its end
		List<double[]> out = new ArrayList<double[]>();
		out.add(p.get(p.size() - 1));
		out.add(p.get(p.size() - 1));
		return out;
	}

	/**
	 * A path whose every subpath is an axis-aligned rectangle (four corners and a close), each from its bottom left,
	 * then right, up and back: the order of the PDF's re, which Word draws a rectangle with. Any other path as it is.
	 */
	static Path2D.Double asRe(Path2D.Double path) {
		List<double[]> pts = new ArrayList<double[]>();
		Path2D.Double out = new Path2D.Double(path.getWindingRule());
		double[] c = new double[6];
		boolean closed = false;
		for (PathIterator it = path.getPathIterator(null); !it.isDone(); it.next()) {
			int t = it.currentSegment(c);
			if (t == PathIterator.SEG_MOVETO) {
				if (!pts.isEmpty() && !rect(pts, closed, out)) return path;
				pts.clear();
				closed = false;
				pts.add(new double[] { c[0], c[1] });
			} else if (t == PathIterator.SEG_LINETO) {
				pts.add(new double[] { c[0], c[1] });
			} else if (t == PathIterator.SEG_CLOSE) {
				closed = true;
			} else {
				return path;
			}
		}
		if (pts.isEmpty() || !rect(pts, closed, out)) return path;
		return out;
	}

	private static boolean rect(List<double[]> pts, boolean closed, Path2D.Double out) {
		List<double[]> p = new ArrayList<double[]>(pts);
		if (p.size() == 5 && p.get(4)[0] == p.get(0)[0] && p.get(4)[1] == p.get(0)[1]) p.remove(4);
		if (!closed || p.size() != 4) return false;
		double l = Double.MAX_VALUE, r = -Double.MAX_VALUE, t = Double.MAX_VALUE, b = -Double.MAX_VALUE;
		for (double[] q : p) {
			l = Math.min(l, q[0]);
			r = Math.max(r, q[0]);
			t = Math.min(t, q[1]);
			b = Math.max(b, q[1]);
		}
		for (int i = 0; i < 4; i++) {
			double[] a = p.get(i), n = p.get((i + 1) % 4);
			if (a[0] != l && a[0] != r || a[1] != t && a[1] != b) return false;
			if (a[0] != n[0] && a[1] != n[1]) return false; // a diagonal
		}
		if (l == r || t == b) return false;
		out.moveTo(l, b);
		out.lineTo(r, b);
		out.lineTo(r, t);
		out.lineTo(l, t);
		out.closePath();
		return true;
	}

	/** Whether the path's first subpath is left open (a line, an arc) */
	private static boolean isOpen(Shape s) {
		double[] c = new double[6];
		boolean started = false;
		for (PathIterator it = s.getPathIterator(null); !it.isDone(); it.next()) {
			int t = it.currentSegment(c);
			if (t == PathIterator.SEG_MOVETO) {
				if (started) return true;
				started = true;
			} else if (t == PathIterator.SEG_CLOSE) {
				return false;
			}
		}
		return true;
	}
}
