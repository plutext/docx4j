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
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * The geometry of a text warp ({@code a:prstTxWarp}, CR-083 phase 2), written from ISO/IEC 29500-1 section 20.1.9.19
 * as phase 0 found Word reads it (findings section 3, 157 of 198 two-path probes within 0.5 pt of Word's outline):
 * <ol>
 * <li>the rectangle mapped is the ink of the whole block of lines: the glyphs' outlines, no side bearings, spaces at
 * the ends left out;</li>
 * <li>a point's horizontal fraction of it is measured along the top path and along the bottom path, each by its own
 * arc length;</li>
 * <li>the two points are joined by a straight line, and the point's vertical fraction is measured down it;</li>
 * <li>the outline is warped densely, as a curve: each of its segments flattened and cut short before its points are
 * mapped, not its control points alone.</li>
 * </ol>
 * The paths are the preset's evaluated on the text rectangle (the box less its insets), not the box
 * ({@link org.docx4j.model.shapes.ShapeGeometry#textWarp}).
 *
 * @since 17.3.7
 */
final class TextWarp {

	/** How finely a glyph's curves are flattened before they are mapped, in points */
	static final double FLATNESS = 0.01;

	/** The longest piece of an outline mapped as one straight line, in points, up to a glyph of 50 pt ({@link #segment}) */
	static final double MAX_SEGMENT = 0.5;

	/**
	 * The points a WordArt's warped outlines may have, past which it is drawn unwarped (CR-083 phases 1 and 2 review:
	 * 3,200 glyphs warped at 0.5 pt made an SVG of 12.8 MB): about 17 MB of path data
	 */
	static final int MAX_POINTS = 1_000_000;

	/** The limit in force: {@link #MAX_POINTS}, lowered by a test */
	static volatile int maxPoints = MAX_POINTS;

	/**
	 * The longest piece mapped as one straight line for a glyph of this size: {@link #MAX_SEGMENT}, or a hundredth of
	 * the size past 50 pt, so that a glyph's points don't grow with its size (a 400 pt glyph: 4 pt pieces)
	 */
	static double segment(double size) {
		return Math.max(MAX_SEGMENT, size / 100);
	}

	/** The points a WordArt's warp may still write */
	static final class Budget {
		int left;

		Budget(int points) {
			this.left = points;
		}
	}

	/** Past the {@link Budget}: the WordArt is drawn unwarped */
	static final class OverLimit extends RuntimeException {
		private static final long serialVersionUID = 1L;

		OverLimit() {
			super(null, null, false, false);
		}
	}

	private TextWarp() {}

	/** A path flattened, and measured by its arc length */
	static final class Measured {
		final double[] x, y, s;
		final double length;

		Measured(Shape path) {
			List<double[]> pts = new ArrayList<double[]>();
			double[] c = new double[6];
			for (PathIterator it = new FlatteningPathIterator(path.getPathIterator(null), FLATNESS, 16); !it.isDone(); it.next()) {
				int t = it.currentSegment(c);
				if (t == PathIterator.SEG_MOVETO || t == PathIterator.SEG_LINETO) {
					if (!pts.isEmpty()) {
						double[] last = pts.get(pts.size() - 1);
						if (Math.abs(last[0] - c[0]) < 1e-9 && Math.abs(last[1] - c[1]) < 1e-9) continue;
					}
					pts.add(new double[] { c[0], c[1] });
				}
			}
			if (pts.isEmpty()) pts.add(new double[] { 0, 0 });
			int n = pts.size();
			x = new double[n];
			y = new double[n];
			s = new double[n];
			double d = 0;
			for (int i = 0; i < n; i++) {
				x[i] = pts.get(i)[0];
				y[i] = pts.get(i)[1];
				if (i > 0) d += Math.hypot(x[i] - x[i - 1], y[i] - y[i - 1]);
				s[i] = d;
			}
			length = d;
			if (d > 0) for (int i = 0; i < n; i++) s[i] /= d;
		}

		private Measured(double[] x, double[] y) {
			int n = x.length;
			this.x = x;
			this.y = y;
			this.s = new double[n];
			double d = 0;
			for (int i = 0; i < n; i++) {
				if (i > 0) d += Math.hypot(x[i] - x[i - 1], y[i] - y[i - 1]);
				s[i] = d;
			}
			length = d;
			if (d > 0) for (int i = 0; i < n; i++) s[i] /= d;
		}

		/**
		 * The path moved by d to its left (y down: up, for a path drawn left to right), each point along the mean of
		 * its segments' normals, less the points where it doubles back
		 */
		Measured offset(double d) {
			int n = x.length;
			double[] ox = new double[n], oy = new double[n];
			for (int i = 0; i < n; i++) {
				double tx = 0, ty = 0;
				if (i > 0) {
					double l = Math.hypot(x[i] - x[i - 1], y[i] - y[i - 1]);
					if (l > 0) {
						tx += (x[i] - x[i - 1]) / l;
						ty += (y[i] - y[i - 1]) / l;
					}
				}
				if (i < n - 1) {
					double l = Math.hypot(x[i + 1] - x[i], y[i + 1] - y[i]);
					if (l > 0) {
						tx += (x[i + 1] - x[i]) / l;
						ty += (y[i + 1] - y[i]) / l;
					}
				}
				double l = Math.hypot(tx, ty);
				if (l > 0) {
					tx /= l;
					ty /= l;
				}
				ox[i] = x[i] + ty * d;
				oy[i] = y[i] - tx * d;
			}
			// where the offset passes the path's radius of curvature (a flat ellipse's ends) it doubles back: those points
			// go, so that the curve stays one way along the path
			double[] kx = new double[n], ky = new double[n];
			int m = 0;
			for (int i = 0; i < n; i++) {
				if (m > 0 && i > 0) {
					double px = x[i] - x[i - 1], py = y[i] - y[i - 1];
					double qx = ox[i] - kx[m - 1], qy = oy[i] - ky[m - 1];
					if (px * qx + py * qy <= 0) continue;
				}
				kx[m] = ox[i];
				ky[m] = oy[i];
				m++;
			}
			if (m < 2) return new Measured(ox, oy);
			return new Measured(java.util.Arrays.copyOf(kx, m), java.util.Arrays.copyOf(ky, m));
		}

		/** The flattened path as a shape again */
		Shape shape() {
			Path2D.Double p = new Path2D.Double();
			for (int i = 0; i < x.length; i++) {
				if (i == 0) p.moveTo(x[i], y[i]);
				else p.lineTo(x[i], y[i]);
			}
			return p;
		}

		/** The point at a fraction u of the length, held to the ends outside 0 to 1 */
		double[] at(double u) {
			int n = s.length;
			if (n == 1 || u <= 0) return new double[] { x[0], y[0] };
			if (u >= 1) return new double[] { x[n - 1], y[n - 1] };
			int i = index(u);
			double f = (u - s[i]) / Math.max(1e-12, s[i + 1] - s[i]);
			return new double[] { x[i] + f * (x[i + 1] - x[i]), y[i] + f * (y[i + 1] - y[i]) };
		}

		/** The direction at a fraction u of the length, in radians (y down, so clockwise) */
		double angle(double u) {
			int n = s.length;
			if (n == 1) return 0;
			int i = u <= 0 ? 0 : u >= 1 ? n - 2 : index(u);
			return Math.atan2(y[i + 1] - y[i], x[i + 1] - x[i]);
		}

		private int index(double u) {
			int lo = 0, hi = s.length - 1;
			while (hi - lo > 1) {
				int mid = (lo + hi) >>> 1;
				if (s[mid] <= u) lo = mid;
				else hi = mid;
			}
			return lo;
		}
	}

	/** Maps a point (x, y) */
	interface Mapping {
		void map(double x, double y, double[] out);
	}

	/**
	 * The whole ellipse an arc is part of (Follow Path: phase 1-2 findings section 4), centre (cx, cy) and semi-axes a
	 * and b, starting opposite the arc's middle and running the arc's way, so that the arc's middle is halfway along it.
	 * The three Follow Path presets' arcs are symmetric about an axis of their ellipse, so their middle is snapped to
	 * the nearest axis.
	 */
	static Measured ellipse(double cx, double cy, double a, double b, Measured arc) {
		double[] mid = arc.at(0.5);
		double tm = Math.atan2((mid[1] - cy) / b, (mid[0] - cx) / a);
		tm = Math.round(tm / (Math.PI / 2)) * (Math.PI / 2);
		double[] e1 = arc.at(Math.min(1, 0.5 + 1e-3)), e0 = arc.at(Math.max(0, 0.5 - 1e-3));
		boolean clockwise = (e1[0] - e0[0]) * -a * Math.sin(tm) + (e1[1] - e0[1]) * b * Math.cos(tm) >= 0;
		int n = (int) Math.max(256, Math.ceil(2 * Math.PI * Math.max(a, b) / 0.25));
		double[] x = new double[n + 1], y = new double[n + 1];
		for (int i = 0; i <= n; i++) {
			double t = tm + Math.PI + (clockwise ? 1 : -1) * 2 * Math.PI * i / n;
			x[i] = cx + a * Math.cos(t);
			y[i] = cy + b * Math.sin(t);
		}
		return new Measured(x, y);
	}

	/** The two-path mapping of section 20.1.9.19: from the rectangle to between the top and the bottom path */
	static Mapping envelope(final Rectangle2D rect, final Measured top, final Measured bottom) {
		final double x0 = rect.getX(), y0 = rect.getY(), w = Math.max(1e-9, rect.getWidth()), h = Math.max(1e-9, rect.getHeight());
		return (x, y, out) -> {
			double u = (x - x0) / w, v = (y - y0) / h;
			double[] t = top.at(u), b = bottom.at(u);
			out[0] = t[0] + v * (b[0] - t[0]);
			out[1] = t[1] + v * (b[1] - t[1]);
		};
	}

	/**
	 * A shape warped: its outline flattened, each straight piece cut at most {@link #MAX_SEGMENT} long, and each point
	 * mapped; the subpaths kept, closed as they were. Its winding rule is kept.
	 */
	static Path2D.Double warp(Shape shape, Mapping m) {
		return warp(shape, m, MAX_SEGMENT, null);
	}

	/**
	 * As {@link #warp(Shape, Mapping)}, each piece cut at most segment long, its points taken from the budget (null:
	 * none)
	 *
	 * @throws OverLimit where the budget runs out
	 */
	static Path2D.Double warp(Shape shape, Mapping m, double segment, Budget budget) {
		Path2D.Double out = new Path2D.Double(shape instanceof Path2D ? ((Path2D) shape).getWindingRule() : Path2D.WIND_NON_ZERO);
		double[] c = new double[6], p = new double[2];
		double cx = 0, cy = 0, sx = 0, sy = 0;
		for (PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), FLATNESS, 16); !it.isDone(); it.next()) {
			switch (it.currentSegment(c)) {
				case PathIterator.SEG_MOVETO:
					if (budget != null && --budget.left < 0) throw new OverLimit();
					m.map(c[0], c[1], p);
					out.moveTo(p[0], p[1]);
					cx = sx = c[0];
					cy = sy = c[1];
					break;
				case PathIterator.SEG_LINETO:
					line(out, m, cx, cy, c[0], c[1], p, segment, budget);
					cx = c[0];
					cy = c[1];
					break;
				case PathIterator.SEG_CLOSE:
					line(out, m, cx, cy, sx, sy, p, segment, budget);
					out.closePath();
					cx = sx;
					cy = sy;
					break;
				default:
					break;
			}
		}
		return out;
	}

	private static void line(Path2D.Double out, Mapping m, double x0, double y0, double x1, double y1, double[] p, double segment,
			Budget budget) {
		double len = Math.hypot(x1 - x0, y1 - y0);
		double cut = Math.ceil(len / segment);
		if (!(cut < Integer.MAX_VALUE)) throw new OverLimit();
		int n = Math.max(1, (int) cut);
		if (budget != null && (budget.left -= n) < 0) throw new OverLimit();
		for (int i = 1; i <= n; i++) {
			double f = (double) i / n;
			m.map(x0 + f * (x1 - x0), y0 + f * (y1 - y0), p);
			out.lineTo(p[0], p[1]);
		}
	}

	/**
	 * A glyph placed on a path (Follow Path): its outline, in its line's frame, moved so that the point (ox, oy) of it
	 * is at the path's point a fraction u along it, scaled, and turned to the path's direction there.
	 */
	static Shape place(Shape glyph, double ox, double oy, double scale, Measured path, double u) {
		double[] at = path.at(u);
		AffineTransform t = new AffineTransform();
		t.translate(at[0], at[1]);
		t.rotate(path.angle(u));
		t.scale(scale, scale);
		t.translate(-ox, -oy);
		return t.createTransformedShape(glyph);
	}

	/** The mapping that point takes (Follow Path), for an invisible text's place: the glyph's origin */
	static Point2D.Double placed(double gx, double gy, double ox, double oy, double scale, Measured path, double u) {
		double[] at = path.at(u);
		AffineTransform t = new AffineTransform();
		t.translate(at[0], at[1]);
		t.rotate(path.angle(u));
		t.scale(scale, scale);
		t.translate(-ox, -oy);
		Point2D.Double out = new Point2D.Double();
		t.transform(new Point2D.Double(gx, gy), out);
		return out;
	}
}
