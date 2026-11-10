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

import static org.junit.Assert.assertTrue;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import org.docx4j.XmlUtils;
import org.docx4j.chart.render.api.Drawing;
import org.docx4j.chart.render.api.ShapeRenderer;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.jaxb.Context;
import org.docx4j.model.color.ThemeColors;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * {@link ShapeRenderer} against what Word 16.0.20326.20232's PDFs of CR-080 phase 0's probes paint
 * (shapes/cr080-word-drawn.jsonl.gz, from CR-080-notes/export_drawn.py): the region each paints, Word's strokes taken
 * with their own width, caps, joins and dashes, and the colours.
 */
public class ShapeRendererTest {

	private static final String NS = " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"";

	private static final List<Map<String, Object>> PROBES = new ArrayList<Map<String, Object>>();
	private static final Map<String, ThemeColors> THEMES = new HashMap<String, ThemeColors>();
	private static BufferedImage photo;

	@BeforeClass
	public static void read() throws Exception {
		try (InputStream in = new GZIPInputStream(ShapeRendererTest.class.getClassLoader().getResourceAsStream("shapes/cr080-word-drawn.jsonl.gz"));
				BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			for (String line; (line = r.readLine()) != null; ) PROBES.add(Json.object(Json.parse(line)));
		}
		for (String t : new String[] { "2023", "2013" }) {
			try (InputStream in = ShapeRendererTest.class.getClassLoader().getResourceAsStream("org/docx4j/openpackaging/parts/WordprocessingML/theme-" + t + ".xml")) {
				THEMES.put(t, ThemeColors.of((org.docx4j.dml.Theme) XmlUtils.unwrap(XmlUtils.unmarshal(in))));
			}
		}
		try (InputStream in = ShapeRendererTest.class.getClassLoader().getResourceAsStream("shapes/photo.png")) {
			photo = javax.imageio.ImageIO.read(in);
		}
	}

	private static ShapeRenderer.Drawn draw(Map<String, Object> p) throws Exception {
		CTWordprocessingShape wsp = (CTWordprocessingShape) XmlUtils.unmarshalString("<wps:wsp" + NS + ">" + p.get("sppr") + p.get("style")
				+ "<wps:bodyPr/></wps:wsp>", Context.jc, CTWordprocessingShape.class);
		String[] size = ((String) p.get("size")).split("x");
		return ShapeRenderer.draw(wsp.getSpPr(), wsp.getStyle(), THEMES.get(p.get("theme")), Math.round(Double.parseDouble(size[0]) * 12700),
				Math.round(Double.parseDouble(size[1]) * 12700), id -> photo);
	}

	// ------------------------------------------------------------------ what is painted

	private static Path2D.Double path(String s, boolean evenOdd) {
		Path2D.Double p = new Path2D.Double(evenOdd ? Path2D.WIND_EVEN_ODD : Path2D.WIND_NON_ZERO);
		String[] t = s.trim().split(" ");
		for (int i = 0; i < t.length; ) {
			switch (t[i++]) {
				case "M": p.moveTo(d(t[i++]), d(t[i++])); break;
				case "L": p.lineTo(d(t[i++]), d(t[i++])); break;
				case "C": p.curveTo(d(t[i++]), d(t[i++]), d(t[i++]), d(t[i++]), d(t[i++]), d(t[i++])); break;
				case "Z": p.closePath(); break;
				case "": break;
				default: throw new IllegalArgumentException(s);
			}
		}
		return p;
	}

	private static double d(String s) {
		return Double.parseDouble(s);
	}

	/** What Word painted: each fill's region, each stroke's outline with its own width, caps, joins and dashes */
	static Area word(Map<String, Object> p) {
		Area out = new Area();
		for (Object o : Json.list(p.get("draws"))) {
			Map<String, Object> d = Json.object(o);
			String k = (String) d.get("k");
			if ("image".equals(k)) {
				// a picture: its box, as its clip isn't read
				List<Object> b = Json.list(d.get("box"));
				out.add(new Area(new Rectangle2D.Double(Json.number(b.get(0)), Json.number(b.get(1)),
						Json.number(b.get(2)) - Json.number(b.get(0)), Json.number(b.get(3)) - Json.number(b.get(1)))));
				continue;
			}
			if (!"F".equals(k) && !"S".equals(k)) continue;
			Path2D.Double path = path((String) d.get("path"), Boolean.TRUE.equals(d.get("evenodd")));
			if ("F".equals(k)) {
				out.add(new Area(path));
			} else {
				List<Object> dash = Json.list(d.get("dash"));
				List<Object> arr = Json.list(dash.get(0));
				float[] a = new float[arr.size()];
				for (int i = 0; i < a.length; i++) a[i] = Json.number(arr.get(i)).floatValue();
				int cap = Json.number(d.get("cap")).intValue(), join = Json.number(d.get("join")).intValue();
				// PDF's caps 0 butt, 1 round, 2 square, and joins 0 miter, 1 round, 2 bevel, are AWT's in that order
				BasicStroke st = a.length == 0 ? new BasicStroke(Json.number(d.get("lw")).floatValue(), cap, join, 10f)
						: new BasicStroke(Json.number(d.get("lw")).floatValue(), cap, join, 10f, a, Json.number(dash.get(1)).floatValue());
				out.add(new Area(st.createStrokedShape(path)));
			}
		}
		return out;
	}

	/** What docx4j paints */
	static Area ours(ShapeRenderer.Drawn drawn) {
		Area out = new Area();
		for (Object o : drawn.getDrawing().getItems()) {
			if (o instanceof Drawing.Area) {
				Drawing.Area a = (Drawing.Area) o;
				if (a.getFill() != null || a.getGradient() != null || a.getPattern() != null || a.getPaint() != null) out.add(new Area(a.getShape()));
				if (a.getLine() != null && a.getLineWidth() > 0) {
					java.awt.Stroke st = a.getStroke() != null ? a.getStroke() : new BasicStroke((float) a.getLineWidth());
					out.add(new Area(st.createStrokedShape(a.getShape())));
				}
			} else if (o instanceof Drawing.Picture) {
				// its box, as Word's is compared by its box
				out.add(new Area(((Drawing.Picture) o).getInto()));
			}
		}
		return out;
	}

	/** The colours painted, hex: Word's from its fills and strokes, docx4j's from its areas (pattern fills and pictures aside) */
	static TreeSet<String> wordColours(Map<String, Object> p) {
		TreeSet<String> out = new TreeSet<String>();
		for (Object o : Json.list(p.get("draws"))) {
			Map<String, Object> d = Json.object(o);
			if (Boolean.TRUE.equals(d.get("pattern"))) continue;
			String c = "F".equals(d.get("k")) ? (String) d.get("fill") : "S".equals(d.get("k")) ? (String) d.get("stroke") : null;
			if (c != null && c.length() == 6) out.add(c);
		}
		return out;
	}

	static TreeSet<String> ourColours(ShapeRenderer.Drawn drawn) {
		TreeSet<String> out = new TreeSet<String>();
		for (Object o : drawn.getDrawing().getItems()) {
			if (!(o instanceof Drawing.Area)) continue;
			Drawing.Area a = (Drawing.Area) o;
			if (a.getFill() != null && a.getGradient() == null && a.getPattern() == null && a.getPaint() == null) out.add(hex(a.getFill()));
			if (a.getLine() != null && a.getLineGradient() == null && a.getLinePattern() == null) out.add(hex(a.getLine()));
		}
		return out;
	}

	private static String hex(java.awt.Color c) {
		return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
	}

	// ------------------------------------------------------------------ how far apart

	/** Where {@link #apart} found the region thickest, for a message */
	static String where = "";

	/**
	 * How far apart two painted regions are: the thickness of the region where they disagree (one paints, the other
	 * doesn't), in points, from a distance transform of that region drawn at 40 pixels a point. A region's outline can
	 * hold edges of no width (where a stroke's inner join folds), which a distance between outlines would count; they
	 * enclose nothing here.
	 */
	static double apart(Area a, Area b) {
		Area x = new Area(a);
		x.exclusiveOr(b);
		if (x.isEmpty()) return 0;
		Rectangle2D r = x.getBounds2D();
		double k = 40;
		int w = (int) Math.ceil(r.getWidth() * k) + 4, h = (int) Math.ceil(r.getHeight() * k) + 4;
		if ((long) w * h > 12_000_000L) {
			k = Math.sqrt(12_000_000.0 / (r.getWidth() * r.getHeight()));
			w = (int) Math.ceil(r.getWidth() * k) + 4;
			h = (int) Math.ceil(r.getHeight() * k) + 4;
		}
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
		java.awt.Graphics2D g = img.createGraphics();
		g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);
		g.translate(2, 2);
		g.scale(k, k);
		g.translate(-r.getX(), -r.getY());
		g.setColor(java.awt.Color.WHITE);
		g.fill(x);
		g.dispose();
		// chamfer distance (3-4) from the region's outside, the largest the region's half-thickness
		int[] d = new int[w * h];
		byte[] px = ((java.awt.image.DataBufferByte) img.getRaster().getDataBuffer()).getData();
		int inf = Integer.MAX_VALUE / 4;
		for (int i = 0; i < d.length; i++) d[i] = px[i] != 0 ? inf : 0;
		for (int y = 1; y < h; y++)
			for (int xx = 1; xx < w - 1; xx++) {
				int i = y * w + xx;
				if (d[i] == 0) continue;
				d[i] = Math.min(d[i], Math.min(Math.min(d[i - 1] + 3, d[i - w] + 3), Math.min(d[i - w - 1] + 4, d[i - w + 1] + 4)));
			}
		int best = 0;
		for (int y = h - 2; y >= 0; y--)
			for (int xx = w - 2; xx >= 1; xx--) {
				int i = y * w + xx;
				if (d[i] == 0) continue;
				d[i] = Math.min(d[i], Math.min(Math.min(d[i + 1] + 3, d[i + w] + 3), Math.min(d[i + w + 1] + 4, d[i + w - 1] + 4)));
				if (d[i] > best) {
					best = d[i];
					where = String.format("at %.2f %.2f", (xx - 2) / k + r.getX(), (y - 2) / k + r.getY());
				}
			}
		// half-thickness in pixels is best / 3; the region's thickness, in points
		return 2 * (best / 3.0) / k;
	}

	// ------------------------------------------------------------------ the report

	static double area(Shape s) {
		double total = 0, sub = 0, sx = 0, sy = 0, px = 0, py = 0;
		double[] c = new double[6];
		for (PathIterator it = new FlatteningPathIterator(s.getPathIterator(null), 0.005, 16); !it.isDone(); it.next()) {
			int t = it.currentSegment(c);
			if (t == PathIterator.SEG_MOVETO) { total += sub + (px * sy - sx * py); sub = 0; sx = px = c[0]; sy = py = c[1]; }
			else if (t == PathIterator.SEG_LINETO) { sub += px * c[1] - c[0] * py; px = c[0]; py = c[1]; }
		}
		return Math.abs(total + sub + (px * sy - sx * py)) / 2;
	}

	/** Every probe paints within this of where Word paints, in points */
	private static final double TOLERANCE = 0.1;

	/** The probes that don't, each with its bound and why */
	private static final Map<String, Object[]> KNOWN = new HashMap<String, Object[]>();
	static {
		KNOWN.put("fp30-061", new Object[] { 0.35, "an inside line (algn in): Word's sits 0.24pt further out, its 1/300-inch grid" });
		KNOWN.put("fp31-069", new Object[] { 0.15, "a triangle head on an arc: its base meets the curve a little differently" });
	}

	@Test
	public void everyProbePaintsWhereWordDoes() throws Exception {
		List<String> off = new ArrayList<String>();
		double worst = 0;
		for (Map<String, Object> p : PROBES) {
			String id = (String) p.get("probe");
			ShapeRenderer.Drawn drawn = draw(p);
			where = "";
			double dist = apart(word(p), ours(drawn));
			TreeSet<String> wc = wordColours(p), oc = ourColours(drawn);
			Object[] known = KNOWN.get(id);
			double bound = known == null ? TOLERANCE : (Double) known[0];
			if (known == null) worst = Math.max(worst, dist);
			if (dist > bound || !wc.equals(oc)) {
				off.add(String.format("%s %.3f %s%s (%s)", id, dist, where, wc.equals(oc) ? "" : " colours " + wc + ", ours " + oc, p.get("note")));
			}
		}
		assertTrue(off.size() + " of " + PROBES.size() + " probes off Word: " + off, off.isEmpty());
		System.out.println(PROBES.size() + " probes, the farthest " + String.format("%.3f", worst) + " pt from Word's, but for " + KNOWN.keySet());
	}

	/**
	 * CR-080 phase 2's gate on the PDF: docx4j's PDFs of phase 0's probe files, read as Word's are
	 * (CR-080-notes/read_shapes.py --out, then export_drawn.py --json --out), against Word's, by the same measure. Run
	 * where -Dcr080.pdfDraws names that file; skipped otherwise, as the PDFs come from docx4j-export-fo.
	 */
	@Test
	public void docx4jsPdfsPaintWhereWordsDo() throws Exception {
		String file = System.getProperty("cr080.pdfDraws");
		org.junit.Assume.assumeTrue("no -Dcr080.pdfDraws", file != null);
		Map<String, Map<String, Object>> ours = new HashMap<String, Map<String, Object>>();
		try (InputStream in = new GZIPInputStream(new java.io.FileInputStream(file));
				BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			for (String line; (line = r.readLine()) != null; ) {
				Map<String, Object> p = Json.object(Json.parse(line));
				ours.put((String) p.get("probe"), p);
			}
		}
		List<String> off = new ArrayList<String>();
		double worst = 0;
		int n = 0;
		for (Map<String, Object> p : PROBES) {
			String id = (String) p.get("probe");
			Map<String, Object> o = ours.get(id);
			if (o == null) continue;
			n++;
			where = "";
			double dist = apart(word(p), word(o));
			TreeSet<String> wc = wordColours(p), oc = wordColours(o);
			Object[] known = KNOWN.get(id);
			double bound = known == null ? TOLERANCE : (Double) known[0];
			if (known == null) worst = Math.max(worst, dist);
			if (dist > bound || !wc.equals(oc)) {
				off.add(String.format("%s %.3f %s%s (%s)", id, dist, where, wc.equals(oc) ? "" : " colours " + wc + ", docx4j's " + oc, p.get("note")));
			}
		}
		for (String l : off) System.out.println("   " + l);
		System.out.println(n + " probes in docx4j's PDFs, " + off.size() + " off Word's; the farthest " + String.format("%.3f", worst) + " pt, but for "
				+ KNOWN.keySet());
		assertTrue(off.size() + " of " + n + " probes in docx4j's PDFs off Word's", off.isEmpty());
	}
}
