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

		Result(String probe) {
			this.probe = probe;
		}

		void check(boolean ok, String what) {
			checked++;
			if (ok) within++;
			else misses.add(what);
		}

		boolean passes() {
			return checked > 0 && within == checked;
		}

		@Override
		public String toString() {
			return String.format("%-28s %3d/%3d%s", probe, within, checked, misses.isEmpty() ? "" : "  " + String.join("; ", misses));
		}
	}

	static Result compare(String probe, Drawing d, Map<String, Object> dump, Map<String, Object> trace) {
		Result r = new Result(probe);
		layout(r, d, dump);
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
		r.check(e <= GEOMETRY, String.format("%s off by %.2f (Office %s, ours %s)", what, e, str(office), str(ours)));
	}

	private static String str(Rectangle2D b) {
		return String.format("%.2f,%.2f %.2fx%.2f", b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
	}

	private static void layout(Result r, Drawing d, Map<String, Object> dump) {
		if (Boolean.TRUE.equals(dump.get("hasTitle"))) same(r, "title", box(dump.get("title"), ORIGIN), d.boxes.get("title"));
		else same(r, "title", null, d.boxes.get("title"));
		if (Boolean.TRUE.equals(dump.get("hasLegend"))) {
			Map<String, Object> lg = Json.object(dump.get("legend"));
			same(r, "legend", box(lg, ORIGIN), d.boxes.get("legend"));
			List<Object> entries = Json.list(lg.get("entries"));
			for (int i = 0; i < entries.size(); i++) {
				Map<String, Object> e = Json.object(entries.get(i));
				same(r, "legend entry " + i, box(e, ORIGIN), d.boxes.get("legend.entry." + i));
				same(r, "legend key " + i, box(e.get("key"), ORIGIN), d.boxes.get("legend.key." + i));
			}
		}
		Map<String, Object> pa = Json.object(dump.get("plotArea"));
		same(r, "plot area", box(pa, ORIGIN), d.boxes.get("plotArea"));
		Double il = Json.number(pa.get("insideLeft")), it = Json.number(pa.get("insideTop")), iw = Json.number(pa.get("insideWidth")),
				ih = Json.number(pa.get("insideHeight"));
		if (il != null) same(r, "inside", new Rectangle2D.Double(il + ORIGIN, it + ORIGIN, iw, ih), d.boxes.get("inside"));
		// points: from the inner frame in Excel, from the box in Word and PowerPoint
		double pointOrigin = "excel".equals(dump.get("app")) ? ORIGIN : 0;
		double cw = Json.number(Json.list(dump.get("size")).get(0)), ch = Json.number(Json.list(dump.get("size")).get(1));
		List<Object> series = Json.list(dump.get("series"));
		for (int s = 0; s < series.size(); s++) {
			List<Object> points = Json.list(Json.object(series.get(s)).get("points"));
			for (int p = 0; p < points.size(); p++) {
				Rectangle2D b = box(points.get(p), pointOrigin);
				// a blank point's box is far off the chart (no geometry)
				if (b == null || b.getMinX() < -1 || b.getMinY() < -1 || b.getMaxX() > cw + 1 || b.getMaxY() > ch + 1) continue;
				same(r, "point " + s + "." + p, b, d.boxes.get("point." + s + "." + p));
			}
		}
		for (Object o : Json.list(dump.get("axes"))) {
			Map<String, Object> a = Json.object(o);
			if (a.get("categoryType") != null || a.get("minimumScale") == null || a.get("tickLabels") == null) continue;
			double[] office = { Json.number(a.get("minimumScale")), Json.number(a.get("maximumScale")), Json.number(a.get("majorUnit")) };
			boolean ok = d.valueAxis != null;
			for (int i = 0; ok && i < 3; i++) ok = Math.abs(office[i] - d.valueAxis[i]) <= 1e-9 * Math.max(1, Math.abs(office[i]));
			r.check(ok, "value axis: Office " + office[0] + " to " + office[1] + " by " + office[2] + ", ours "
					+ (d.valueAxis == null ? "none" : d.valueAxis[0] + " to " + d.valueAxis[1] + " by " + d.valueAxis[2]));
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
		double a = Math.toRadians(t.angle);
		double mx = t.x + Math.cos(a) * t.width / 2, my = t.baseline + Math.sin(a) * t.width / 2;
		double s = t.font.getSize2D();
		return new double[] { mx + Math.sin(a) * s / 3, my - Math.cos(a) * s / 3 };
	}

	private static void texts(Result r, Drawing d, Map<String, Object> trace) {
		List<Drawing.Text> ours = new ArrayList<>();
		for (Object o : d.items) if (o instanceof Drawing.Text) ours.add((Drawing.Text) o);
		boolean[] used = new boolean[ours.size()];
		for (Object o : Json.list(trace.get("texts"))) {
			Map<String, Object> t = Json.object(o);
			String text = norm((String) t.get("text"));
			double cx = Json.number(t.get("cx")), cy = Json.number(t.get("cy"));
			int best = -1;
			double bestD = Double.MAX_VALUE;
			for (int i = 0; i < ours.size(); i++) {
				if (used[i] || !norm(ours.get(i).text).equals(text)) continue;
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
			r.check(bestD <= TEXT_PLACE, String.format("text \"%s\" %.2f from Office's", text, bestD));
			double size = Json.number(t.get("size"));
			r.check(Math.abs(our.font.getSize2D() - size) <= TEXT_SIZE,
					String.format("text \"%s\" size %.2f, Office %.2f", text, our.font.getSize2D(), size));
			String colour = (String) t.get("colour");
			r.check(hex(our.colour).equals(colour), "text \"" + text + "\" colour " + hex(our.colour) + ", Office " + colour);
			String font = (String) t.get("font");
			String family = font.replaceAll("-(Bold|Italic|BoldItalic)$", "").replaceAll(",.*$", "");
			r.check(family.replace(" ", "").equalsIgnoreCase(our.face.replace(" ", "")),
					"text \"" + text + "\" face " + our.face + ", Office " + font);
		}
		for (int i = 0; i < ours.size(); i++) {
			if (!used[i]) r.check(false, "text \"" + ours.get(i).text + "\" drawn, Office has none");
		}
	}

	private static void shapes(Result r, Drawing d, Map<String, Object> trace) {
		// ours: each area's box, filled or stroked
		List<Drawing.Area> areas = new ArrayList<>();
		for (Object o : d.items) if (o instanceof Drawing.Area) areas.add((Drawing.Area) o);
		boolean[] used = new boolean[areas.size()];
		for (Object o : Json.list(trace.get("shapes"))) {
			Map<String, Object> s = Json.object(o);
			if (s.get("alpha") != null && Json.number(s.get("alpha")) == 0) continue;
			boolean fill = "fill".equals(s.get("kind"));
			String colour = ((String) s.get("colour")).replaceAll("/.*$", "");
			for (Object p : Json.list(s.get("parts"))) {
				List<Object> b = Json.list(Json.object(p).get("box"));
				Rectangle2D office = new Rectangle2D.Double(Json.number(b.get(0)), Json.number(b.get(1)),
						Json.number(b.get(2)) - Json.number(b.get(0)), Json.number(b.get(3)) - Json.number(b.get(1)));
				int best = -1;
				double bestD = Double.MAX_VALUE;
				for (int i = 0; i < areas.size(); i++) {
					Drawing.Area a = areas.get(i);
					Color c = fill ? a.fill : a.line;
					if (used[i] || c == null || !hex(c).equals(colour)) continue;
					double e = edge(office, a.shape.getBounds2D());
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
				used[best] = true;
				r.check(bestD <= GEOMETRY, String.format("%s off by %.2f", what, bestD));
			}
		}
		for (int i = 0; i < areas.size(); i++) {
			if (!used[i]) r.check(false, areas.get(i).role + " drawn, Office has none");
		}
	}
}
