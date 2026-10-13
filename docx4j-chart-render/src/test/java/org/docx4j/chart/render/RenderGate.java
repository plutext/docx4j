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
package org.docx4j.chart.render;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.docx4j.chart.render.api.Drawing;

/**
 * docx4j's layout and drawing of a chart against Office's (CR-037 §5), with D4's tolerances:
 * Excel's layout dump (each box Office reports, the value axis) and chart_trace.py's reading of
 * Word's PDF (each text by its string: centre, size, colour, face; each shape by its colour and
 * box).  A result counts the elements compared and those within tolerance, and names the rest.
 */
final class RenderGate {

	/** D4: a shape's box (each edge), in points */
	static final double GEOMETRY = 0.25;
	/** D4: a text's centre, in points */
	static final double TEXT_PLACE = 0.5;
	/** D4: a text's size against Word's PDF (Word draws on a 0.24pt grid, give or take 0.02: 9 is 9.14) */
	static final double TEXT_SIZE = 0.15;
	/** Office reports elements from 4pt inside the chart's box (finding 1) */
	static final double ORIGIN = 4;

	static final class Result {
		final String probe;
		int checked;
		int within;
		final List<String> misses = new ArrayList<>();
		/** the farthest any measured element is from Office's, in points */
		double worst;

		Result(String probe) {
			this.probe = probe;
		}

		void check(boolean ok, String what) {
			checked++;
			if (ok) within++;
			else misses.add(what);
		}

		/** an element measured: how far it is from Office's */
		void check(boolean ok, String what, double off) {
			check(ok, what);
			worst = Math.max(worst, off);
		}

		boolean passes() {
			return checked > 0 && within == checked;
		}

		@Override
		public String toString() {
			return String.format("%-28s %3d/%3d%s", probe, within, checked, misses.isEmpty() ? "" : "  " + String.join("; ", misses));
		}
	}

	/** @param trace Word's PDF read, or null for a chart that has none (Excel's own) */
	static Result compare(String probe, Drawing d, Map<String, Object> dump, Map<String, Object> trace) {
		Result r = new Result(probe);
		layout(r, d, dump);
		if (trace != null) {
			texts(r, d, trace);
			shapes(r, d, trace);
		}
		return r;
	}

	/** The drawing alone against Word's or PowerPoint's PDF, for a chart Excel hasn't laid out (CR-036's P3, P4) */
	static Result drawing(String probe, Drawing d, Map<String, Object> trace) {
		Result r = new Result(probe);
		texts(r, d, trace);
		shapes(r, d, trace);
		return r;
	}

	// ------------------------------------------------------------------ against Excel's layout

	private static Rectangle2D box(Object o, double origin) {
		if (o == null) return null;
		Map<String, Object> m = Json.object(o);
		Double l = Json.number(m.get("left")), t = Json.number(m.get("top")), w = Json.number(m.get("width")),
				h = Json.number(m.get("height"));
		if (l == null || t == null || w == null || h == null) return null;
		return new Rectangle2D.Double(l + origin, t + origin, w, h);
	}

	private static double edge(Rectangle2D a, Rectangle2D b) {
		return Math.max(Math.max(Math.abs(a.getMinX() - b.getMinX()), Math.abs(a.getMinY() - b.getMinY())),
				Math.max(Math.abs(a.getMaxX() - b.getMaxX()), Math.abs(a.getMaxY() - b.getMaxY())));
	}

	private static void same(Result r, String what, Rectangle2D office, Rectangle2D ours) {
		if (office == null && ours == null) return;
		if (office == null || ours == null) {
			r.check(false, what + (office == null ? " drawn, Office has none" : " missing"));
			return;
		}
		double e = edge(office, ours);
		r.check(e <= GEOMETRY, String.format("%s off by %.2f (Office %s, ours %s)", what, e, str(office), str(ours)), e);
	}

	private static String str(Rectangle2D b) {
		return String.format("%.2f,%.2f %.2fx%.2f", b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
	}

	private static void layout(Result r, Drawing d, Map<String, Object> dump) {
		if (Boolean.TRUE.equals(dump.get("hasTitle"))) same(r, "title", box(dump.get("title"), ORIGIN), d.getBoxes().get("title"));
		else same(r, "title", null, d.getBoxes().get("title"));
		if (Boolean.TRUE.equals(dump.get("hasLegend"))) {
			Map<String, Object> lg = Json.object(dump.get("legend"));
			same(r, "legend", box(lg, ORIGIN), d.getBoxes().get("legend"));
			List<Object> entries = Json.list(lg.get("entries"));
			for (int i = 0; i < entries.size(); i++) {
				Map<String, Object> e = Json.object(entries.get(i));
				same(r, "legend entry " + i, box(e, ORIGIN), d.getBoxes().get("legend.entry." + i));
				same(r, "legend key " + i, box(e.get("key"), ORIGIN), d.getBoxes().get("legend.key." + i));
			}
		}
		Map<String, Object> pa = Json.object(dump.get("plotArea"));
		same(r, "plot area", box(pa, ORIGIN), d.getBoxes().get("plotArea"));
		Double il = Json.number(pa.get("insideLeft")), it = Json.number(pa.get("insideTop")), iw = Json.number(pa.get("insideWidth")),
				ih = Json.number(pa.get("insideHeight"));
		if (il != null) same(r, "inside", new Rectangle2D.Double(il + ORIGIN, it + ORIGIN, iw, ih), d.getBoxes().get("inside"));
		// points: from the inner frame in Excel, from the box in Word and PowerPoint
		double pointOrigin = "excel".equals(dump.get("app")) ? ORIGIN : 0;
		double cw = Json.number(Json.list(dump.get("size")).get(0)), ch = Json.number(Json.list(dump.get("size")).get(1));
		List<Object> series = Json.list(dump.get("series"));
		for (int s = 0; s < series.size(); s++) {
			// a pie shows its first series alone, though Excel reports the others' slices as if it drew
			// them (p6-pie-two-by-two's picture)
			Double type = Json.number(Json.object(series.get(s)).get("chartType"));
			if (s > 0 && type != null && (type == 5 || type == 69)) continue;
			List<Object> points = Json.list(Json.object(series.get(s)).get("points"));
			for (int p = 0; p < points.size(); p++) {
				Rectangle2D b = box(points.get(p), pointOrigin);
				// a blank point's box is far off the chart (no geometry)
				if (b == null || b.getMinX() < -1 || b.getMinY() < -1 || b.getMaxX() > cw + 1 || b.getMaxY() > ch + 1) continue;
				same(r, "point " + s + "." + p, b, d.getBoxes().get("point." + s + "." + p));
				// its data label's box, an element (from the inner frame in every application)
				Object dl = Json.object(points.get(p)).get("dataLabel");
				if (dl != null || d.getBoxes().containsKey("label." + s + "." + p)) {
					same(r, "label " + s + "." + p, box(dl, ORIGIN), d.getBoxes().get("label." + s + "." + p));
				}
				// a pie's slice, by the places Office reports (PieSliceLocation 1 to 9, finding 7)
				Object at = Json.object(points.get(p)).get("slice");
				double[][] ours = d.getSlices().get("point." + s + "." + p);
				if (at == null) continue;
				Map<String, Object> office = Json.object(at);
				for (int k = 1; k <= 9; k++) {
					List<Object> xy = Json.list(office.get(String.valueOf(k)));
					if (xy == null || xy.size() < 2 || xy.get(0) == null) continue;
					double ox = Json.number(xy.get(0)) + pointOrigin, oy = Json.number(xy.get(1)) + pointOrigin;
					if (ours == null || ours[k] == null) {
						r.check(false, "slice " + s + "." + p + " place " + k + " missing");
						continue;
					}
					double e = Math.max(Math.abs(ox - ours[k][0]), Math.abs(oy - ours[k][1]));
					r.check(e <= GEOMETRY, String.format("slice %d.%d place %d off by %.2f (Office %.2f,%.2f, ours %.2f,%.2f)", s, p, k, e, ox, oy,
							ours[k][0], ours[k][1]), e);
				}
			}
		}
		for (Object o : Json.list(dump.get("axes"))) {
			Map<String, Object> a = Json.object(o);
			// its title's box (an element)
			if (Boolean.TRUE.equals(a.get("hasTitle")) || d.getBoxes().containsKey("axisTitle." + Json.number(a.get("type")).intValue())) {
				int type = Json.number(a.get("type")).intValue();
				same(r, "axis title " + type, Boolean.TRUE.equals(a.get("hasTitle")) ? box(a.get("title"), ORIGIN) : null, d.getBoxes().get("axisTitle." + type));
			}
			if (a.get("categoryType") != null || a.get("minimumScale") == null || a.get("tickLabels") == null) continue;
			double[] office = { Json.number(a.get("minimumScale")), Json.number(a.get("maximumScale")), Json.number(a.get("majorUnit")) };
			// a scatter chart's x axis is Office's "category" axis (type 1), a value axis
			boolean x = Json.number(a.get("type")) == 1;
			double[] ours = x ? d.getXAxis() : d.getValueAxis();
			boolean ok = ours != null;
			for (int i = 0; ok && i < 3; i++) ok = Math.abs(office[i] - ours[i]) <= 1e-9 * Math.max(1, Math.abs(office[i]));
			r.check(ok, (x ? "x" : "value") + " axis: Office " + office[0] + " to " + office[1] + " by " + office[2] + ", ours "
					+ (ours == null ? "none" : ours[0] + " to " + ours[1] + " by " + ours[2]));
		}
	}

	// ------------------------------------------------------------------ against Word's PDF

	private static String norm(String s) {
		return s == null ? "" : s.replaceAll("\\s+", " ").trim();
	}

	private static String hex(Color c) {
		return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
	}

	/** Our text's centre as chart_trace.py has Office's: the baseline's middle, raised a third of the size */
	private static double[] centre(Drawing.Text t) {
		double a = Math.toRadians(t.getAngle());
		double mx = t.getX() + Math.cos(a) * t.getWidth() / 2, my = t.getBaseline() + Math.sin(a) * t.getWidth() / 2;
		double s = t.getFont().getSize2D();
		return new double[] { mx + Math.sin(a) * s / 3, my - Math.cos(a) * s / 3 };
	}

	private static void texts(Result r, Drawing d, Map<String, Object> trace) {
		List<Drawing.Text> ours = new ArrayList<>();
		for (Object o : d.getItems()) if (o instanceof Drawing.Text) ours.add((Drawing.Text) o);
		boolean[] used = new boolean[ours.size()];
		for (Object o : Json.list(trace.get("texts"))) {
			Map<String, Object> t = Json.object(o);
			String text = norm((String) t.get("text"));
			double cx = Json.number(t.get("cx")), cy = Json.number(t.get("cy"));
			int best = -1;
			double bestD = Double.MAX_VALUE;
			for (int i = 0; i < ours.size(); i++) {
				if (used[i] || !norm(ours.get(i).getText()).equals(text)) continue;
				double[] c = centre(ours.get(i));
				double dd = Math.hypot(c[0] - cx, c[1] - cy);
				if (dd < bestD) {
					bestD = dd;
					best = i;
				}
			}
			if (best < 0) {
				r.check(false, "text \"" + text + "\" not drawn");
				continue;
			}
			used[best] = true;
			Drawing.Text our = ours.get(best);
			r.check(bestD <= TEXT_PLACE, String.format("text \"%s\" %.2f from Office's", text, bestD), bestD);
			double size = Json.number(t.get("size"));
			r.check(Math.abs(our.getFont().getSize2D() - size) <= TEXT_SIZE,
					String.format("text \"%s\" size %.2f, Office %.2f", text, our.getFont().getSize2D(), size));
			String colour = (String) t.get("colour");
			r.check(hex(our.getColour()).equals(colour), "text \"" + text + "\" colour " + hex(our.getColour()) + ", Office " + colour);
			String font = (String) t.get("font");
			String family = font.replaceAll("-(Bold|Italic|BoldItalic)$", "").replaceAll(",.*$", "");
			r.check(family.replace(" ", "").equalsIgnoreCase(our.getFace().replace(" ", "")),
					"text \"" + text + "\" face " + our.getFace() + ", Office " + font);
		}
		// Word leaves category labels turned 45 degrees out of its PDF (CR-037 §11, finding 10):
		// ours turned so are Excel's picture's to check, not Word's PDF's
		boolean word45 = false;
		for (Object o : Json.list(trace.get("texts"))) {
			if (Math.abs(Json.number(Json.object(o).get("angle")) + 45) < 1) word45 = true;
		}
		for (int i = 0; i < ours.size(); i++) {
			if (used[i] || (!word45 && Math.abs(ours.get(i).getAngle() + 45) < 1)) continue;
			r.check(false, "text \"" + ours.get(i).getText() + "\" drawn, Office has none");
		}
	}

	private static void shapes(Result r, Drawing d, Map<String, Object> trace) {
		// ours: each area's box, filled or stroked
		List<Drawing.Area> areas = new ArrayList<>();
		for (Object o : d.getItems()) if (o instanceof Drawing.Area) areas.add((Drawing.Area) o);
		// a shape's fill and its line are separate paths in Word's PDF (a marker, a slice)
		boolean[] usedFill = new boolean[areas.size()], usedLine = new boolean[areas.size()];
		// Word draws category labels turned 45 degrees as glyph outlines, not text (finding 10):
		// those below the plot, in the labels' colour, are Excel's picture's to check
		String outlines = null;
		for (Object o : d.getItems()) {
			if (o instanceof Drawing.Text && Math.abs(((Drawing.Text) o).getAngle() + 45) < 1) outlines = hex(((Drawing.Text) o).getColour());
		}
		Rectangle2D inside = d.getBoxes().get("inside");
		for (Object o : Json.list(trace.get("shapes"))) {
			Map<String, Object> s = Json.object(o);
			if (s.get("alpha") != null && Json.number(s.get("alpha")) == 0) continue;
			boolean fill = "fill".equals(s.get("kind"));
			String colour = ((String) s.get("colour")).replaceAll("/.*$", "");
			for (Object p : Json.list(s.get("parts"))) {
				List<Object> b = Json.list(Json.object(p).get("box"));
				Rectangle2D office = new Rectangle2D.Double(Json.number(b.get(0)), Json.number(b.get(1)),
						Json.number(b.get(2)) - Json.number(b.get(0)), Json.number(b.get(3)) - Json.number(b.get(1)));
				if (fill && colour.equals(outlines) && inside != null && office.getMinY() >= inside.getMaxY() - 1) continue;
				// what reaches well past the chart is the page's or slide's (PowerPoint's white slide), not the chart's
				if (office.getMinX() < -2 || office.getMinY() < -2 || office.getMaxX() > d.getWidth() + 2 || office.getMaxY() > d.getHeight() + 2) continue;
				int best = -1;
				double bestD = Double.MAX_VALUE;
				for (int i = 0; i < areas.size(); i++) {
					Drawing.Area a = areas.get(i);
					Color c = fill ? (a.getGradient() != null ? null : a.getFill()) : (a.getLineGradient() != null ? null : a.getLine());
					if ((fill ? usedFill[i] : usedLine[i]) || c == null || !hex(c).equals(colour)) continue;
					double e = edge(office, bounds(a.getShape()));
					if (e < bestD) {
						bestD = e;
						best = i;
					}
				}
				String what = (fill ? "fill " : "line ") + colour + " at " + str(office);
				if (best < 0 || bestD > 5) {
					r.check(false, what + " not drawn");
					continue;
				}
				if (fill) usedFill[best] = true;
				else usedLine[best] = true;
				r.check(bestD <= GEOMETRY, String.format("%s off by %.2f", what, bestD), bestD);
			}
		}
		// a gradient is a shading in Word's PDF (CR-037 finding 6), in the shape's clip: ours by its box
		List<Object> images = trace.get("images") == null ? new ArrayList<>() : Json.list(trace.get("images"));
		List<Rectangle2D> pictures = new ArrayList<>();
		for (Object o : images) {
			Map<String, Object> im = Json.object(o);
			if (!"fill_shade".equals(im.get("kind"))) {
				pictures.add(rect(Json.list(im.get("box"))));
				continue;
			}
			for (Object p : Json.list(im.get("parts"))) {
				Rectangle2D office = rect(Json.list(Json.object(p).get("box")));
				if (office.getMinX() < -2 || office.getMinY() < -2 || office.getMaxX() > d.getWidth() + 2 || office.getMaxY() > d.getHeight() + 2) continue;
				int best = -1;
				double bestD = Double.MAX_VALUE;
				for (int i = 0; i < areas.size(); i++) {
					Drawing.Area a = areas.get(i);
					// a gradient fill, or a gradient line (its stroke's outline)
					if (a.getGradient() != null ? usedFill[i] : a.getLineGradient() == null || usedLine[i]) continue;
					Rectangle2D ab = a.getGradient() != null ? bounds(a.getShape())
							: new java.awt.BasicStroke((float) a.getLineWidth(), java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER).createStrokedShape(a.getShape()).getBounds2D();
					double e = edge(office, ab);
					if (e < bestD) {
						bestD = e;
						best = i;
					}
				}
				String what = "shading at " + str(office);
				if (best < 0 || bestD > 5) {
					r.check(false, what + " not drawn");
					continue;
				}
				if (areas.get(best).getGradient() != null) usedFill[best] = true;
				else usedLine[best] = true;
				r.check(bestD <= GEOMETRY, String.format("%s off by %.2f", what, bestD), bestD);
			}
		}
		for (int i = 0; i < areas.size(); i++) {
			Drawing.Area a = areas.get(i);
			// what Word makes a picture of (a shadow, a glow, a bevel: finding 6; a pattern's tiles) is the picture's to check (D4)
			boolean pictured = a.getPattern() != null && !pictures.isEmpty();
			Rectangle2D b = bounds(a.getShape());
			for (Rectangle2D p : pictures) pictured |= p.getMinX() - 3 <= b.getMinX() && p.getMinY() - 3 <= b.getMinY() && b.getMaxX() <= p.getMaxX() + 3
					&& b.getMaxY() <= p.getMaxY() + 3 && p.getWidth() < 0.9 * d.getWidth();
			if (a.getFill() != null && !usedFill[i] && !pictured) {
				r.check(false, a.getRole() + " drawn (" + (a.getGradient() != null ? "gradient" : "fill " + hex(a.getFill())) + "), Office has none");
			}
			if (a.getLine() != null && a.getLineWidth() > 0 && !usedLine[i] && !pictured) r.check(false, a.getRole() + " drawn (line " + hex(a.getLine()) + "), Office has none");
		}
	}

	private static Rectangle2D rect(List<Object> b) {
		return new Rectangle2D.Double(Json.number(b.get(0)), Json.number(b.get(1)), Json.number(b.get(2)) - Json.number(b.get(0)),
				Json.number(b.get(3)) - Json.number(b.get(1)));
	}

	/** A shape's box, its curves flattened (as chart_trace.py samples Office's), not their control points' */
	static Rectangle2D bounds(java.awt.Shape shape) {
		Rectangle2D b = null;
		double[] c = new double[6];
		for (java.awt.geom.PathIterator it = shape.getPathIterator(null, 0.01); !it.isDone(); it.next()) {
			int type = it.currentSegment(c);
			if (type == java.awt.geom.PathIterator.SEG_CLOSE) continue;
			if (b == null) b = new Rectangle2D.Double(c[0], c[1], 0, 0);
			else b.add(c[0], c[1]);
		}
		return b == null ? shape.getBounds2D() : b;
	}
}
