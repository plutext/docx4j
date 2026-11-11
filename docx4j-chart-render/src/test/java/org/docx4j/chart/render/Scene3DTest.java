/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  docx4j is licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *
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

import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.docx4j.XmlUtils;
import org.docx4j.chart.render.api.Effects;
import org.docx4j.chart.render.api.Scene3D;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.jaxb.Context;
import org.docx4j.model.color.ThemeColors;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A 3-D drawing's silhouette against Word's (CR-081 phase 5a): every fixed probe of the rounds cr081-phase5a-r1 and
 * -r2 (cr081-word-3d.jsonl.gz, from CR-081-notes/export_3d.py), its shape drawn by {@link Scene3D} and its silhouette
 * read as Word's is: the convex hull of the picture's 0.5 alpha crossings along every row and column, points from the
 * frame's centre. Each hull's vertices are within the tolerance of the other's boundary.
 */
public class Scene3DTest {

	private static final String NS = " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\"";
	/**
	 * Phase 5a's gate: a pixel at 200 dpi (0.36 pt, CR-081 section 7), plus Word's own placement. Word puts each picture
	 * on a 1/300 inch grid without resampling it (every picture's box is a whole number of 0.24 pt: fxa0's 215.52 and
	 * 360.48 are 898 and 1502 of them), so its silhouette can be 0.24 pt off its geometry (the flat obliques, fxc1-01).
	 */
	private static final double TOLERANCE = 0.36 + 0.24;

	private static final List<Map<String, Object>> PROBES = new ArrayList<Map<String, Object>>();
	private static ThemeColors theme;

	@BeforeClass
	public static void read() throws Exception {
		ClassLoader cl = Scene3DTest.class.getClassLoader();
		try (InputStream in = new GZIPInputStream(cl.getResourceAsStream("effects/cr081-word-3d.jsonl.gz"));
				BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			for (String line; (line = r.readLine()) != null; ) PROBES.add(Json.object(Json.parse(line)));
		}
		try (InputStream in = cl.getResourceAsStream("org/docx4j/openpackaging/parts/WordprocessingML/theme-2023.xml")) {
			theme = ThemeColors.of((org.docx4j.dml.Theme) XmlUtils.unwrap(XmlUtils.unmarshal(in)));
		}
	}

	private static CTShapeProperties spPr(String inner) throws Exception {
		CTWordprocessingShape wsp = (CTWordprocessingShape) XmlUtils.unmarshalString("<wps:wsp" + NS + "><wps:spPr>" + inner
				+ "</wps:spPr><wps:bodyPr/></wps:wsp>", Context.jc, CTWordprocessingShape.class);
		return wsp.getSpPr();
	}

	/** Not drawn as Word draws them yet: the contour (phase 5a findings section 3) */
	private static boolean expected(String note) {
		return !note.startsWith("contour");
	}

	@Test
	public void theSilhouettesAreWords() throws Exception {
		int n = 0, failed = 0, pixel = 0;
		double worst = 0;
		String worstProbe = null;
		StringBuilder report = new StringBuilder();
		for (Map<String, Object> p : PROBES) {
			String note = (String) p.get("note");
			if (!expected(note)) continue;
			double w = Json.number(p.get("w")), h = Json.number(p.get("h"));
			CTShapeProperties sp = spPr("<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>" + p.get("xml"));
			Effects.Painter face = g -> {
				g.setColor(new Color(0x44, 0x72, 0xC4));
				g.fill(new Rectangle2D.Double(0, 0, w, h));
			};
			Scene3D.Picture pic = Scene3D.draw(face, new Rectangle2D.Double(0, 0, w, h), w, h, sp.getScene3D(), sp.getSp3D(), theme,
					Effects.BASE_PIXELS_PER_POINT, new ArrayList<String>());
			assertTrue((String) p.get("probe"), pic != null);
			List<double[]> mine = hull(crossings(pic, w / 2, h / 2));
			List<double[]> words = new ArrayList<double[]>();
			for (Object v : Json.list(Json.object(p.get("word")).get("hull"))) {
				List<Object> xy = Json.list(v);
				words.add(new double[] { Json.number(xy.get(0)), Json.number(xy.get(1)) });
			}
			double d = Math.max(farthest(words, mine), farthest(mine, words));
			n++;
			if (d <= 0.36) pixel++;
			if (d > worst) {
				worst = d;
				worstProbe = p.get("probe") + " " + note;
			}
			if (d > TOLERANCE) {
				failed++;
				boolean mineOut = farthest(mine, words) >= farthest(words, mine);
				double[] at = farthestPoint(mineOut ? mine : words, mineOut ? words : mine);
				report.append(String.format(Locale.ROOT, "%n  %s %s: %.2f pt, %s's vertex at (%.2f, %.2f)", p.get("probe"), note, d,
						mineOut ? "docx4j" : "Word", at[0], at[1]));
			}
		}
		System.out.println(String.format(Locale.ROOT, "Scene3DTest: %d probes, %d within a pixel (0.36 pt), %d past %.2f pt, worst %.2f pt (%s)%s",
				n, pixel, failed, TOLERANCE, worst, worstProbe, report));
		assertTrue(failed + " of " + n + " probes past " + TOLERANCE + " pt:" + report, failed == 0);
	}

	/** The picture's 0.5 alpha crossings, each row's and column's first and last, points from the frame's centre */
	static List<double[]> crossings(Scene3D.Picture pic, double cx, double cy) {
		BufferedImage img = pic.getImage();
		int w = img.getWidth(), h = img.getHeight();
		double[][] a = new double[h][w];
		for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) a[y][x] = (img.getRGB(x, y) >>> 24) / 255.0;
		Rectangle2D.Double place = pic.getPlace();
		double sx = place.width / w, sy = place.height / h;
		List<double[]> out = new ArrayList<double[]>();
		for (int y = 0; y < h; y++) {
			int first = -1, last = -1;
			for (int x = 0; x < w; x++) if (a[y][x] >= 0.5) { if (first < 0) first = x; last = x; }
			if (first < 0) continue;
			out.add(new double[] { place.x + edge(a[y][first], first > 0 ? a[y][first - 1] : 0, first, -1) * sx - cx, place.y + (y + 0.5) * sy - cy });
			out.add(new double[] { place.x + edge(a[y][last], last + 1 < w ? a[y][last + 1] : 0, last, 1) * sx - cx, place.y + (y + 0.5) * sy - cy });
		}
		for (int x = 0; x < w; x++) {
			int first = -1, last = -1;
			for (int y = 0; y < h; y++) if (a[y][x] >= 0.5) { if (first < 0) first = y; last = y; }
			if (first < 0) continue;
			out.add(new double[] { place.x + (x + 0.5) * sx - cx, place.y + edge(a[first][x], first > 0 ? a[first - 1][x] : 0, first, -1) * sy - cy });
			out.add(new double[] { place.x + (x + 0.5) * sx - cx, place.y + edge(a[last][x], last + 1 < h ? a[last + 1][x] : 0, last, 1) * sy - cy });
		}
		return out;
	}

	/** Where 0.5 falls between a pixel at or over it and its neighbour under it, in pixels from the image's edge */
	private static double edge(double in, double out, int i, int step) {
		double t = in != out ? (0.5 - in) / (out - in) : 0.5;
		return i + 0.5 + step * t;
	}

	static List<double[]> hull(List<double[]> pts) {
		List<double[]> p = new ArrayList<double[]>(pts);
		p.sort((a, b) -> a[0] != b[0] ? Double.compare(a[0], b[0]) : Double.compare(a[1], b[1]));
		List<double[]> lo = new ArrayList<double[]>(), up = new ArrayList<double[]>();
		for (double[] q : p) {
			while (lo.size() >= 2 && cross(lo.get(lo.size() - 2), lo.get(lo.size() - 1), q) <= 0) lo.remove(lo.size() - 1);
			lo.add(q);
		}
		for (int i = p.size() - 1; i >= 0; i--) {
			double[] q = p.get(i);
			while (up.size() >= 2 && cross(up.get(up.size() - 2), up.get(up.size() - 1), q) <= 0) up.remove(up.size() - 1);
			up.add(q);
		}
		lo.remove(lo.size() - 1);
		up.remove(up.size() - 1);
		lo.addAll(up);
		return lo;
	}

	private static double cross(double[] o, double[] a, double[] b) {
		return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0]);
	}

	/** The vertex of a farthest from b's boundary */
	static double[] farthestPoint(List<double[]> a, List<double[]> b) {
		double worst = -1;
		double[] at = null;
		for (double[] q : a) {
			double d = farthest(java.util.Collections.singletonList(q), b);
			if (d > worst) {
				worst = d;
				at = q;
			}
		}
		return at;
	}

	/** The greatest distance of a's vertices from b's boundary */
	static double farthest(List<double[]> a, List<double[]> b) {
		double worst = 0;
		for (double[] q : a) {
			double best = Double.MAX_VALUE;
			for (int i = 0; i < b.size(); i++) {
				double[] s = b.get(i), e = b.get((i + 1) % b.size());
				double ex = e[0] - s[0], ey = e[1] - s[1], l2 = ex * ex + ey * ey;
				double t = l2 == 0 ? 0 : Math.max(0, Math.min(1, ((q[0] - s[0]) * ex + (q[1] - s[1]) * ey) / l2));
				best = Math.min(best, Math.hypot(q[0] - s[0] - t * ex, q[1] - s[1] - t * ey));
			}
			worst = Math.max(worst, best);
		}
		return worst;
	}
}
