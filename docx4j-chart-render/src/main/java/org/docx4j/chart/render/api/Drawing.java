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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A chart laid out: what is painted, in painting order, in points from the chart's top left,
 * and the boxes Office's object model reports for the same chart (CR-037 §11: the plot area,
 * the title, the legend and its entries, each point, a pie's slices), so that a layout can be
 * compared with Excel's without painting it.
 *
 * <p>A renderer builds one ({@link #add}), and paints it ({@link #paint}) or writes it as SVG
 * ({@link SvgWriter}).  Sizes are points, from the chart's top left.</p>
 *
 * @since 17.3.3 (CR-039: from docx4j-chart-render's internals, CR-037)
 */
public final class Drawing {

	/**
	 * A DrawingML gradient ({@code a:gradFill}), made a paint for each shape's box: linear at an
	 * angle (clockwise from the x axis, scaled to the box where it says), or along a path from its
	 * focus ({@code a:fillToRect}) out to the farthest corner, its first stop at the focus
	 */
	public static final class Gradient {
		private final float[] stops;
		private final Color[] colours;
		private final boolean linear, scaled;
		private final double angle;
		/** a path's focus: its insets from the box's left, top, right and bottom, shares of the box */
		private final double[] focus;

		/**
		 * @param stops strictly increasing, 0 to 1
		 * @param angle a linear gradient's, degrees clockwise from the x axis
		 * @param focus a path's (null for a linear gradient)
		 */
		public Gradient(float[] stops, Color[] colours, boolean linear, double angle, boolean scaled, double[] focus) {
			this.stops = stops;
			this.colours = colours;
			this.linear = linear;
			this.angle = angle;
			this.scaled = scaled;
			this.focus = focus;
		}

		/** The stops' places, 0 to 1 */
		public float[] getStops() {
			return stops;
		}

		/** The stops' colours */
		public Color[] getColours() {
			return colours;
		}

		/** Linear at {@link #getAngle()}, else along a path from {@link #getFocus()} */
		public boolean isLinear() {
			return linear;
		}

		/** A linear gradient's angle scaled to the box ({@code a:lin/@scaled}) */
		public boolean isScaled() {
			return scaled;
		}

		/** A linear gradient's angle, degrees clockwise from the x axis */
		public double getAngle() {
			return angle;
		}

		/** A path's focus: its insets from the box's left, top, right and bottom, shares of the box; null where linear */
		public double[] getFocus() {
			return focus;
		}

		/** The gradient as a paint for a shape's box */
		public java.awt.Paint paint(Rectangle2D b) {
			if (colours.length == 1) return colours[0];
			double w = Math.max(b.getWidth(), 1e-6), h = Math.max(b.getHeight(), 1e-6);
			if (linear) {
				double t = Math.toRadians(angle), cos = Math.cos(t), sin = Math.sin(t);
				if (scaled) {
					// in the unit square, then scaled to the box (the angle is the box's diagonal's at 45)
					double len = Math.abs(cos) + Math.abs(sin);
					java.awt.geom.Point2D p0 = new java.awt.geom.Point2D.Double(0.5 - cos * len / 2, 0.5 - sin * len / 2);
					java.awt.geom.Point2D p1 = new java.awt.geom.Point2D.Double(0.5 + cos * len / 2, 0.5 + sin * len / 2);
					AffineTransform at = new AffineTransform(w, 0, 0, h, b.getX(), b.getY());
					return new java.awt.LinearGradientPaint(p0, p1, stops, colours, java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE,
							java.awt.MultipleGradientPaint.ColorSpaceType.SRGB, at);
				}
				double len = Math.abs(w * cos) + Math.abs(h * sin), cx = b.getCenterX(), cy = b.getCenterY();
				return new java.awt.LinearGradientPaint(new java.awt.geom.Point2D.Double(cx - cos * len / 2, cy - sin * len / 2),
						new java.awt.geom.Point2D.Double(cx + cos * len / 2, cy + sin * len / 2), stops, colours);
			}
			double fx = b.getX() + w * (focus[0] + (1 - focus[0] - focus[2]) / 2), fy = b.getY() + h * (focus[1] + (1 - focus[1] - focus[3]) / 2);
			double r = 0;
			for (double[] c : new double[][] { { b.getMinX(), b.getMinY() }, { b.getMaxX(), b.getMinY() }, { b.getMinX(), b.getMaxY() },
					{ b.getMaxX(), b.getMaxY() } }) {
				r = Math.max(r, Math.hypot(c[0] - fx, c[1] - fy));
			}
			return new java.awt.RadialGradientPaint(new java.awt.geom.Point2D.Double(fx, fy), (float) Math.max(r, 1e-3), stops, colours);
		}
	}

	/**
	 * A DrawingML preset pattern ({@code a:pattFill}): an 8 by 8 tile of the foreground over the
	 * background, a pixel 0.75pt (96 dpi), as Office draws it (Word's PDF: tiled pictures, r15-patterns)
	 */
	public static final class Pattern {
		private final boolean[] on;
		private final Color fg, bg;

		/** @param on the tile's 64 pixels, row by row, true where the foreground is */
		public Pattern(boolean[] on, Color fg, Color bg) {
			this.on = on;
			this.fg = fg;
			this.bg = bg;
		}

		/** The tile's 64 pixels, row by row: true where the foreground is */
		public boolean[] getTile() {
			return on;
		}

		/** The foreground */
		public Color getForeground() {
			return fg;
		}

		/** The background, or null for none */
		public Color getBackground() {
			return bg;
		}

		/** The pattern as a paint */
		public java.awt.Paint paint() {
			java.awt.image.BufferedImage tile = new java.awt.image.BufferedImage(8, 8, java.awt.image.BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < 8; y++) {
				for (int x = 0; x < 8; x++) {
					Color c = on[y * 8 + x] ? fg : bg;
					if (c != null) tile.setRGB(x, y, c.getRGB());
				}
			}
			return new java.awt.TexturePaint(tile, new Rectangle2D.Double(0, 0, 6, 6));
		}

		/** The preset's tile: the common presets by their rows, columns and diagonals; pctN by an ordered dither; others half on */
		public static boolean[] preset(String prst) {
			boolean[] on = new boolean[64];
			int[] bayer = { 0, 32, 8, 40, 2, 34, 10, 42, 48, 16, 56, 24, 50, 18, 58, 26, 12, 44, 4, 36, 14, 46, 6, 38, 60, 28, 52, 20, 62, 30, 54, 22, 3,
					35, 11, 43, 1, 33, 9, 41, 51, 19, 59, 27, 49, 17, 57, 25, 15, 47, 7, 39, 13, 45, 5, 37, 63, 31, 55, 23, 61, 29, 53, 21 };
			for (int y = 0; y < 8; y++) {
				for (int x = 0; x < 8; x++) {
					int d = (x - y + 8) % 8, u = (x + y) % 8;
					boolean v;
					switch (prst) {
					case "horz": v = y == 0; break;
					case "vert": v = x == 0; break;
					case "ltHorz": v = y % 4 == 0; break;
					case "ltVert": v = x % 4 == 0; break;
					case "dkHorz": v = y % 4 < 2; break;
					case "dkVert": v = x % 4 < 2; break;
					case "narHorz": v = y % 2 == 0; break;
					case "narVert": v = x % 2 == 0; break;
					case "dashHorz": v = y == 0 && x < 4 || y == 4 && x >= 4; break;
					case "dashVert": v = x == 0 && y < 4 || x == 4 && y >= 4; break;
					case "cross": case "lgGrid": v = x == 0 || y == 0; break;
					case "smGrid": v = x % 4 == 0 || y % 4 == 0; break;
					case "dotGrid": v = (x == 0 && y % 2 == 0) || (y == 0 && x % 2 == 0); break;
					case "dnDiag": v = d == 0; break;
					case "upDiag": v = u == 7; break;
					case "ltDnDiag": v = d % 4 == 0; break;
					case "ltUpDiag": v = u % 4 == 3; break;
					case "dkDnDiag": v = d % 4 < 2; break;
					case "dkUpDiag": v = u % 4 >= 2; break;
					case "wdDnDiag": v = d < 3; break;
					case "wdUpDiag": v = u >= 5; break;
					case "dashDnDiag": v = d % 4 == 0 && y % 4 < 2; break;
					case "dashUpDiag": v = u % 4 == 3 && y % 4 < 2; break;
					case "diagCross": v = d == 0 || u == 7; break;
					case "smCheck": v = (x / 2 + y / 2) % 2 == 0; break;
					case "lgCheck": v = (x / 4 + y / 4) % 2 == 0; break;
					default:
						int pct = prst.startsWith("pct") ? Integer.parseInt(prst.substring(3)) : 50;
						v = bayer[y * 8 + x] < Math.round(pct * 64 / 100.0);
					}
					on[y * 8 + x] = v;
				}
			}
			return on;
		}
	}

	/**
	 * A shape's outer shadow or glow ({@code a:outerShdw}, {@code a:glow}), drawn as Office draws
	 * them in a PDF, a picture under the shape (CR-037 finding 6): the shape, moved by the shadow's
	 * distance along its direction, scaled and skewed about its alignment point, or (a glow) grown
	 * by its radius, filled in the colour and blurred
	 */
	public static final class Shadow {
		Color colour;
		/** the blur's radius, the distance and direction (degrees clockwise from the x axis), in points */
		double blur, dist, dir;
		double sx = 1, sy = 1, kx, ky;
		/** the alignment point's horizontal and vertical place in the shape's box, 0 to 1 */
		double ax = 0.5, ay = 0.5;
		/** a glow: its radius, in points; 0 for a shadow */
		double glow;

		/**
		 * A glow's spread beyond what is drawn, and its blur's sigma, shares of its radius (r10-column-style-213
		 * against Excel's picture: 0.15% of pixels off, mean 0.6 of 255; one style)
		 */
		public static final double GLOW_SPREAD = 0.5, GLOW_BLUR = 0.25;

		private Shadow() {
		}

		/**
		 * An outer shadow ({@code a:outerShdw}), in points and degrees
		 *
		 * @param dir its direction, degrees clockwise from the x axis
		 * @param sx sy kx ky its scale and skew (degrees)
		 * @param ax ay its alignment point's place in the shape's box, 0 to 1
		 */
		public static Shadow outer(Color colour, double blur, double dist, double dir, double sx, double sy, double kx, double ky, double ax,
				double ay) {
			Shadow s = new Shadow();
			s.colour = colour;
			s.blur = blur;
			s.dist = dist;
			s.dir = dir;
			s.sx = sx;
			s.sy = sy;
			s.kx = kx;
			s.ky = ky;
			s.ax = ax;
			s.ay = ay;
			return s;
		}

		/** A glow ({@code a:glow}) of a radius in points */
		public static Shadow glow(Color colour, double radius) {
			Shadow s = new Shadow();
			s.colour = colour;
			s.glow = radius;
			return s;
		}

		public Color getColour() {
			return colour;
		}

		/** The blur's radius, in points */
		public double getBlur() {
			return blur;
		}

		/** The distance, in points */
		public double getDistance() {
			return dist;
		}

		/** The direction, degrees clockwise from the x axis */
		public double getDirection() {
			return dir;
		}

		/** A glow's radius, in points; 0 for a shadow */
		public double getGlow() {
			return glow;
		}

		/** the shadow's (or glow's) shape: a glow around what is drawn, the outline alone where the shape isn't filled */
		public Shape shape(Shape s, boolean filled, double lineWidth) {
			if (glow > 0) {
				java.awt.geom.Area a = new java.awt.geom.Area(new BasicStroke((float) (lineWidth + 2 * GLOW_SPREAD * glow), BasicStroke.CAP_ROUND,
						BasicStroke.JOIN_ROUND).createStrokedShape(s));
				if (filled) a.add(new java.awt.geom.Area(s));
				return a;
			}
			Rectangle2D b = s.getBounds2D();
			double px = b.getX() + ax * b.getWidth(), py = b.getY() + ay * b.getHeight();
			AffineTransform t = new AffineTransform();
			t.translate(dist * Math.cos(Math.toRadians(dir)), dist * Math.sin(Math.toRadians(dir)));
			t.translate(px, py);
			t.shear(Math.tan(Math.toRadians(kx)), Math.tan(Math.toRadians(ky)));
			t.scale(sx, sy);
			t.translate(-px, -py);
			return t.createTransformedShape(s);
		}

		/** Drawn under the shape: a picture at the drawing's own scale (at least 2 pixels a point) */
		public void paint(Graphics2D g, Shape s, boolean filled, double lineWidth) {
			Shape ss = shape(s, filled, lineWidth);
			double sigma = Math.max(0.01, glow > 0 ? GLOW_BLUR * glow : blur / 2);
			double scale = Math.max(2, Math.abs(g.getTransform().getScaleX()));
			Rectangle2D b = ss.getBounds2D();
			double pad = 3 * sigma + 1;
			Rectangle2D area = new Rectangle2D.Double(b.getX() - pad, b.getY() - pad, b.getWidth() + 2 * pad, b.getHeight() + 2 * pad);
			int w = (int) Math.ceil(area.getWidth() * scale), h = (int) Math.ceil(area.getHeight() * scale);
			if (w <= 0 || h <= 0 || (long) w * h > 16_000_000L) return;
			java.awt.image.BufferedImage im = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
			Graphics2D ig = im.createGraphics();
			ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			ig.scale(scale, scale);
			ig.translate(-area.getX(), -area.getY());
			ig.setColor(Color.BLACK);
			ig.fill(ss);
			ig.dispose();
			// the alpha blurred (Gaussian, separable), then the colour at its alpha
			int[] px = im.getRGB(0, 0, w, h, null, 0, w);
			float[] a = new float[px.length];
			for (int i = 0; i < px.length; i++) a[i] = (px[i] >>> 24) / 255f;
			a = blur(a, w, h, sigma * scale);
			int rgb = colour.getRGB() & 0xFFFFFF;
			double alpha = colour.getAlpha() / 255.0;
			for (int i = 0; i < px.length; i++) px[i] = ((int) Math.round(Math.min(1, a[i]) * alpha * 255) << 24) | rgb;
			im.setRGB(0, 0, w, h, px, 0, w);
			g.drawImage(im, new AffineTransform(1 / scale, 0, 0, 1 / scale, area.getX(), area.getY()), null);
		}

		private static float[] blur(float[] a, int w, int h, double sigma) {
			int r = (int) Math.ceil(3 * sigma);
			if (r < 1) return a;
			float[] k = new float[2 * r + 1];
			float sum = 0;
			for (int i = -r; i <= r; i++) sum += k[i + r] = (float) Math.exp(-i * i / (2 * sigma * sigma));
			for (int i = 0; i < k.length; i++) k[i] /= sum;
			float[] t = new float[a.length], o = new float[a.length];
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					float v = 0;
					for (int i = -r; i <= r; i++) {
						int xx = x + i;
						if (xx >= 0 && xx < w) v += k[i + r] * a[y * w + xx];
					}
					t[y * w + x] = v;
				}
			}
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					float v = 0;
					for (int i = -r; i <= r; i++) {
						int yy = y + i;
						if (yy >= 0 && yy < h) v += k[i + r] * t[yy * w + x];
					}
					o[y * w + x] = v;
				}
			}
			return o;
		}
	}

	/** A filled or stroked shape: a bar, a gridline, the chart area, a legend key */
	public static final class Area {
		private final Shape shape;
		private final Color fill;
		private final Color line;
		private final double lineWidth;
		private final String role;
		/** what it is drawn within (a bar, the plot's inside: Office clips there), or null */
		private final Shape clip;
		/** its line's ends and joins rounded (a line series'), else butt and mitred */
		private final boolean round;
		/** its fill a gradient ({@link #fill} its first colour), or null */
		private Gradient gradient;
		/** its line a gradient ({@link #line} its first colour), or null */
		private Gradient lineGradient;
		/** its fill a pattern ({@link #fill} its foreground), or null */
		private Pattern pattern;
		/** its shadows and glow, drawn under it */
		private List<Shadow> shadows;
		/** its line's dashes, in points, or null for a solid line */
		private float[] dash;

		public Area(Shape shape, Color fill, Color line, double lineWidth, String role) {
			this(shape, fill, line, lineWidth, role, null, false);
		}

		public Area(Shape shape, Color fill, Color line, double lineWidth, String role, Shape clip) {
			this(shape, fill, line, lineWidth, role, clip, false);
		}

		public Area(Shape shape, Color fill, Color line, double lineWidth, String role, Shape clip, boolean round) {
			this.shape = shape;
			this.fill = fill;
			this.line = line;
			this.lineWidth = lineWidth;
			this.role = role;
			this.clip = clip;
			this.round = round;
		}

		public Shape getShape() {
			return shape;
		}

		/** Its solid fill (a gradient's or pattern's first colour), or null */
		public Color getFill() {
			return fill;
		}

		/** Its line's colour, or null for none */
		public Color getLine() {
			return line;
		}

		/** Its line's width, in points */
		public double getLineWidth() {
			return lineWidth;
		}

		/** What it is, as Office's object model names it: plotArea, point.S.P, legend.key.N, ... */
		public String getRole() {
			return role;
		}

		/** What it is drawn within, or null */
		public Shape getClip() {
			return clip;
		}

		/** Its line's ends and joins rounded (a line series'), else butt and mitred */
		public boolean isRound() {
			return round;
		}

		public Gradient getGradient() {
			return gradient;
		}

		public void setGradient(Gradient gradient) {
			this.gradient = gradient;
		}

		public Gradient getLineGradient() {
			return lineGradient;
		}

		public void setLineGradient(Gradient lineGradient) {
			this.lineGradient = lineGradient;
		}

		public Pattern getPattern() {
			return pattern;
		}

		public void setPattern(Pattern pattern) {
			this.pattern = pattern;
		}

		/** Its shadows and glow, drawn under it, or null */
		public List<Shadow> getShadows() {
			return shadows;
		}

		public void setShadows(List<Shadow> shadows) {
			this.shadows = shadows;
		}

		/** Its line's dashes, in points, or null for a solid line */
		public float[] getDash() {
			return dash;
		}

		public void setDash(float[] dash) {
			this.dash = dash;
		}
	}

	/** A line of text, from its baseline's start, turned by angle degrees (clockwise) about it */
	public static final class Text {
		private final String text;
		/** the face the chart names (Calibri), where {@link #font} is the file it is drawn in (Carlito) */
		private final String face;
		private final Font font;
		private final Color colour;
		private final double x;
		private final double baseline;
		private final double width;
		private final double angle;
		private final String role;
		/** the space after each character (DrawingML's spc), in points */
		private final double spacing;

		public Text(String text, String face, Font font, Color colour, double x, double baseline, double width, double angle, String role) {
			this(text, face, font, colour, x, baseline, width, angle, role, 0);
		}

		public Text(String text, String face, Font font, Color colour, double x, double baseline, double width, double angle, String role, double spacing) {
			this.spacing = spacing;
			this.text = text;
			this.face = face;
			this.font = font;
			this.colour = colour;
			this.x = x;
			this.baseline = baseline;
			this.width = width;
			this.angle = angle;
			this.role = role;
		}

		/** The text, as shown (in capitals where its style says) */
		public String getText() {
			return text;
		}

		/** The face the chart names (Calibri), where {@link #getFont()} is the file it is drawn in (Carlito) */
		public String getFace() {
			return face;
		}

		public Font getFont() {
			return font;
		}

		public Color getColour() {
			return colour;
		}

		/** Its baseline's start */
		public double getX() {
			return x;
		}

		public double getBaseline() {
			return baseline;
		}

		/** Its width, to its last glyph */
		public double getWidth() {
			return width;
		}

		/** Turned by this many degrees, clockwise, about its baseline's start */
		public double getAngle() {
			return angle;
		}

		/** What it is, as Office's object model names it: title, legend.text.N, ... */
		public String getRole() {
			return role;
		}

		/** The space after each character (DrawingML's spc), in points */
		public double getSpacing() {
			return spacing;
		}
	}

	private final double width;
	private final double height;
	private final List<Object> items = new ArrayList<>();

	/** The boxes Office reports, by name: title, legend, legend.entry.N, legend.key.N, plotArea, inside, point.S.P */
	private final Map<String, Rectangle2D> boxes = new LinkedHashMap<>();

	/**
	 * A pie's or doughnut's slices, by point (point.S.P), at the places Office reports
	 * ({@code PieSliceLocation}, 1 to 9: the end's outer point, the arc's middle, the start's outer
	 * point, the start edge's middle, the centre, the end edge's middle, then a doughnut's inner
	 * start, inner arc's middle and inner end); null where a pie has none
	 */
	private final Map<String, double[][]> slices = new LinkedHashMap<>();

	/** The value axis as laid out: min, max, major unit */
	private double[] valueAxis;

	/** A scatter chart's x axis as laid out: min, max, major unit; a date axis's first and last dates and major unit */
	private double[] xAxis;

	/** A drawing of width by height points, empty */
	public Drawing(double width, double height) {
		this.width = width;
		this.height = height;
	}

	/** Adds a shape, painted after those before it */
	public void add(Area a) {
		items.add(a);
	}

	/** Adds a text, painted after what is before it */
	public void add(Text t) {
		items.add(t);
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	/** What is painted, {@link Area}s and {@link Text}s in painting order (the list itself) */
	public List<Object> getItems() {
		return items;
	}

	/** The boxes Office reports, by name (the map itself): title, legend, legend.entry.N, legend.key.N, plotArea, inside, point.S.P */
	public Map<String, Rectangle2D> getBoxes() {
		return boxes;
	}

	/** A pie's or doughnut's slices, by point, at Office's nine places (the map itself; see the field's note) */
	public Map<String, double[][]> getSlices() {
		return slices;
	}

	/** The value axis as laid out: min, max, major unit; null where there is none */
	public double[] getValueAxis() {
		return valueAxis;
	}

	public void setValueAxis(double[] valueAxis) {
		this.valueAxis = valueAxis;
	}

	/** A scatter chart's x axis as laid out, a date axis's first and last dates and major unit; null where there is none */
	public double[] getXAxis() {
		return xAxis;
	}

	public void setXAxis(double[] xAxis) {
		this.xAxis = xAxis;
	}

	/** A line across or down (a gridline, an axis) */
	private static boolean level(Shape s) {
		if (!(s instanceof java.awt.geom.Line2D)) return false;
		java.awt.geom.Line2D l = (java.awt.geom.Line2D) s;
		return l.getX1() == l.getX2() || l.getY1() == l.getY2();
	}

	/** Paints the items in order */
	public void paint(Graphics2D g) {
		paint(g, false);
	}

	/**
	 * Paints the items in order; for a picture (raster), with a rectangle's edges on whole pixels,
	 * as Excel's pictures have them (a bar's edge is its colour or the background, not a blend)
	 */
	public void paint(Graphics2D g, boolean raster) {
		for (Object o : items) {
			if (o instanceof Area) {
				Area a = (Area) o;
				Shape saved = g.getClip();
				Object aa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
				boolean crisp = raster && (a.shape instanceof Rectangle2D || level(a.shape));
				if (crisp) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				if (a.clip != null) g.clip(a.clip);
				if (a.shadows != null) {
					for (Shadow sh : a.shadows) sh.paint(g, a.shape, a.fill != null || a.gradient != null || a.pattern != null, a.line == null ? 0 : a.lineWidth);
				}
				if (a.gradient != null) {
					g.setPaint(a.gradient.paint(a.shape.getBounds2D()));
					g.fill(a.shape);
				} else if (a.pattern != null) {
					g.setPaint(a.pattern.paint());
					g.fill(a.shape);
				} else if (a.fill != null) {
					g.setColor(a.fill);
					g.fill(a.shape);
				}
				if (a.line != null && a.lineWidth > 0) {
					if (a.lineGradient != null) {
						java.awt.Stroke st = new BasicStroke((float) a.lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
						g.setPaint(a.lineGradient.paint(st.createStrokedShape(a.shape).getBounds2D()));
					} else {
						g.setColor(a.line);
					}
					g.setStroke(a.dash != null
							? new BasicStroke((float) a.lineWidth, a.round ? BasicStroke.CAP_ROUND : BasicStroke.CAP_BUTT,
									a.round ? BasicStroke.JOIN_ROUND : BasicStroke.JOIN_MITER, 10f, a.dash, 0f)
							: a.round ? new BasicStroke((float) a.lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
									: new BasicStroke((float) a.lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
					g.draw(a.shape);
				}
				if (a.clip != null) g.setClip(saved);
				if (crisp) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
			} else {
				Text t = (Text) o;
				g.setColor(t.colour);
				g.setFont(t.font);
				if (t.angle == 0) {
					drawString(g, t, raster);
				} else {
					AffineTransform saved = g.getTransform();
					g.rotate(Math.toRadians(t.angle), t.x, t.baseline);
					drawString(g, t, raster);
					g.setTransform(saved);
				}
			}
		}
	}

	/**
	 * A text drawn: kerned, for anything but a picture, as runs split at each kerned pair, each
	 * from where the kerned layout puts its first glyph.  An SVG's text carries no kerning, so
	 * FOP and a browser draw a {@code <text>} unkerned, a kerned title 1.1pt wider than Office's
	 * ("Y-Values" at 14pt, CR-037 phase 3); as runs, each is drawn at its own advances (the
	 * mapped font's, the same file) from its kerned place.  A picture is drawn by AWT, kerned.
	 */
	private static void drawString(Graphics2D g, Text t, boolean raster) {
		float x = (float) t.x, y = (float) t.baseline;
		if (t.spacing != 0 && t.text.length() > 1) {
			// letter-spaced: each character from its kerned place and the spacing before it
			char[] cs = t.text.toCharArray();
			java.awt.font.GlyphVector gv = t.font.layoutGlyphVector(g.getFontRenderContext(), cs, 0, cs.length, java.awt.Font.LAYOUT_LEFT_TO_RIGHT);
			if (gv.getNumGlyphs() == cs.length) {
				for (int i = 0; i < cs.length; i++) {
					if (cs[i] == ' ') continue;
					g.drawString(String.valueOf(cs[i]), x + (float) (gv.getGlyphPosition(i).getX() + i * t.spacing), y);
				}
				return;
			}
		}
		if (raster || !java.awt.font.TextAttribute.KERNING_ON.equals(t.font.getAttributes().get(java.awt.font.TextAttribute.KERNING))
				|| t.text.length() < 2) {
			g.drawString(t.text, x, y);
			return;
		}
		char[] cs = t.text.toCharArray();
		java.awt.font.GlyphVector gv = t.font.layoutGlyphVector(g.getFontRenderContext(), cs, 0, cs.length,
				java.awt.Font.LAYOUT_LEFT_TO_RIGHT);
		if (gv.getNumGlyphs() != cs.length) {
			g.drawString(t.text, x, y);
			return;
		}
		int start = 0;
		for (int i = 1; i <= cs.length; i++) {
			boolean kerned = i < cs.length && Math.abs(gv.getGlyphPosition(i).getX()
					- gv.getGlyphPosition(i - 1).getX() - gv.getGlyphMetrics(i - 1).getAdvanceX()) > 1e-4;
			if (i == cs.length || kerned) {
				g.drawString(new String(cs, start, i - start), x + (float) gv.getGlyphPosition(start).getX(), y);
				start = i;
			}
		}
	}
}
