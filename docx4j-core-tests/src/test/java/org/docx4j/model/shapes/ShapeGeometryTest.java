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
package org.docx4j.model.shapes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Shape;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.docx4j.XmlUtils;
import org.docx4j.dml.CTCustomGeometry2D;
import org.docx4j.dml.CTPresetGeometry2D;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.STPathFillMode;
import org.docx4j.dml.STShapeType;
import org.docx4j.jaxb.Context;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * {@link ShapeGeometry} against Word (CR-080 phase 1): each probe of phase 0 evaluated at its size and compared with the
 * paths Word 16.0.20326.20232's PDF drew for it (shapes/cr080-word.tsv.gz, from CR-080-notes/export_test_paths.py), and
 * the limits of D10 and the codes of D9 on hostile and broken geometry.
 */
public class ShapeGeometryTest {

	private static final String A = "http://schemas.openxmlformats.org/drawingml/2006/main";
	private static final long EMU = 12700;

	/** Every path within this of Word's, in points (CR-080 phase 1's gate). */
	private static final double TOLERANCE = 0.1;

	/**
	 * Word lays a shape's text out on a grid of 1/300 inch, so where its glyph sits shows the text rectangle to within two
	 * steps of it (phase 1's finding), and the glyph boxes are read to 0.01 pt, as is rect's, the control.
	 */
	private static final double TEXT_TOLERANCE = 2 * 72.0 / 300 + 0.035;

	/**
	 * Word's departures from ECMA-376's definitions, one probe each: a wedge callout whose tip is inside the shape (adj1
	 * and adj2 at half their defaults) is drawn with the tip on the edge, no tail, where the definitions draw a notch in
	 * to it. docx4j keeps the definitions; CR-080 section 6 has it as unmeasured beyond these.
	 */
	private static final java.util.Set<String> WORD_DEPARTS = new java.util.HashSet<String>(
			java.util.Arrays.asList("fp12-121", "fp12-122", "fp12-123"));

	private static final class Probe {
		String id, xml, glyph;
		double w, h;
		final List<Path2D.Double> fills = new ArrayList<Path2D.Double>(), strokes = new ArrayList<Path2D.Double>();
	}

	private static final Map<String, Probe> PROBES = new LinkedHashMap<String, Probe>();

	@BeforeClass
	public static void read() throws Exception {
		try (InputStream in = new GZIPInputStream(ShapeGeometryTest.class.getClassLoader().getResourceAsStream("shapes/cr080-word.tsv.gz"));
				BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			for (String line; (line = r.readLine()) != null; ) {
				if (line.startsWith("#") || line.startsWith("probe\t")) continue;
				String[] f = line.split("\t", -1);
				Probe p = new Probe();
				p.id = f[0];
				String[] size = f[1].split("x");
				p.w = Double.parseDouble(size[0]);
				p.h = Double.parseDouble(size[1]);
				p.xml = f[2];
				p.glyph = f[3];
				if (!f[4].isEmpty()) {
					for (String draw : f[4].split(";")) {
						(draw.startsWith("F") ? p.fills : p.strokes).add(path(draw.substring(2)));
					}
				}
				PROBES.put(p.id, p);
			}
		}
		assertEquals(1032, PROBES.size());
	}

	private static Path2D.Double path(String s) {
		Path2D.Double p = new Path2D.Double();
		String[] t = s.trim().split(" ");
		for (int i = 0; i < t.length; ) {
			switch (t[i++]) {
				case "M": p.moveTo(d(t[i++]), d(t[i++])); break;
				case "L": p.lineTo(d(t[i++]), d(t[i++])); break;
				case "C": p.curveTo(d(t[i++]), d(t[i++]), d(t[i++]), d(t[i++]), d(t[i++]), d(t[i++])); break;
				case "Z": p.closePath(); break;
				default: throw new IllegalArgumentException(s);
			}
		}
		return p;
	}

	private static double d(String s) {
		return Double.parseDouble(s);
	}

	private static CTShapeProperties spPr(String xml) throws Exception {
		int end = xml.lastIndexOf('>');
		return (CTShapeProperties) XmlUtils.unmarshalString("<a:spPr xmlns:a=\"" + A + "\">" + xml.substring(0, end + 1) + "</a:spPr>",
				Context.jc, CTShapeProperties.class);
	}

	private static ShapeOutlines outlines(Probe p) throws Exception {
		return ShapeGeometry.outlines(spPr(p.xml), Math.round(p.w * EMU), Math.round(p.h * EMU));
	}

	/** The farthest a point of either drawing is from the other's, in points: fills as regions' edges, strokes as lines. */
	private static double distance(List<? extends Shape> word, List<? extends Shape> ours, boolean fill) {
		List<double[]> a = segments(word, fill), b = segments(ours, fill);
		if (a.isEmpty() && b.isEmpty()) return 0;
		if (a.isEmpty() || b.isEmpty()) return Double.POSITIVE_INFINITY;
		return Math.max(directed(a, b), directed(b, a));
	}

	private static List<double[]> segments(List<? extends Shape> shapes, boolean closeEach) {
		List<double[]> segs = new ArrayList<double[]>();
		double[] c = new double[6];
		for (Shape s : shapes) {
			double sx = 0, sy = 0, px = 0, py = 0;
			boolean open = false;
			for (PathIterator it = new FlatteningPathIterator(s.getPathIterator(null), 0.002, 16); !it.isDone(); it.next()) {
				switch (it.currentSegment(c)) {
					case PathIterator.SEG_MOVETO:
						if (open && closeEach) segs.add(new double[] { px, py, sx, sy });
						sx = px = c[0];
						sy = py = c[1];
						segs.add(new double[] { px, py, px, py });
						open = true;
						break;
					case PathIterator.SEG_LINETO:
						segs.add(new double[] { px, py, c[0], c[1] });
						px = c[0];
						py = c[1];
						break;
					case PathIterator.SEG_CLOSE:
						segs.add(new double[] { px, py, sx, sy });
						px = sx;
						py = sy;
						open = false;
						break;
					default:
						break;
				}
			}
			if (open && closeEach) segs.add(new double[] { px, py, sx, sy });
		}
		return segs;
	}

	private static List<Path2D.Double> withArea(List<Path2D.Double> paths) {
		List<Path2D.Double> kept = new ArrayList<Path2D.Double>();
		for (Path2D.Double p : paths) if (area(p) > 1e-6) kept.add(p);
		return kept;
	}

	/** The sum of the subpaths' areas, each taken as closed, in square points. */
	private static double area(Shape s) {
		double total = 0, sub = 0, sx = 0, sy = 0, px = 0, py = 0;
		double[] c = new double[6];
		for (PathIterator it = new FlatteningPathIterator(s.getPathIterator(null), 0.002, 16); !it.isDone(); it.next()) {
			int t = it.currentSegment(c);
			if (t == PathIterator.SEG_MOVETO) {
				total += Math.abs(sub + (px * sy - sx * py)) / 2;
				sub = 0;
				sx = px = c[0];
				sy = py = c[1];
			} else if (t == PathIterator.SEG_LINETO) {
				sub += px * c[1] - c[0] * py;
				px = c[0];
				py = c[1];
			}
		}
		return total + Math.abs(sub + (px * sy - sx * py)) / 2;
	}

	/** The farthest a point on {@code from} is from {@code to}, sampled every 0.2 pt. */
	private static double directed(List<double[]> from, List<double[]> to) {
		Grid near = new Grid(to);
		double worst = 0;
		for (double[] s : from) {
			double len = Math.hypot(s[2] - s[0], s[3] - s[1]);
			int n = Math.max(1, (int) Math.ceil(len / 0.2));
			for (int i = 0; i <= n; i++) {
				double x = s[0] + (s[2] - s[0]) * i / n, y = s[1] + (s[3] - s[1]) * i / n;
				worst = Math.max(worst, near.distance(x, y, to));
			}
		}
		return worst;
	}

	/**
	 * Segments in cells of {@link #CELL} pt, each cut into pieces no longer than a cell. Any segment within a cell of a
	 * point has a piece in the 3 by 3 cells around it, so a distance up to a cell is exact from those; past that, every
	 * segment is measured.
	 */
	private static final class Grid {
		static final double CELL = 0.5;
		final Map<Long, List<double[]>> cells = new java.util.HashMap<Long, List<double[]>>();

		Grid(List<double[]> segs) {
			for (double[] t : segs) {
				int pieces = Math.max(1, (int) Math.ceil(Math.hypot(t[2] - t[0], t[3] - t[1]) / CELL));
				for (int i = 0; i < pieces; i++) {
					double x0 = t[0] + (t[2] - t[0]) * i / pieces, y0 = t[1] + (t[3] - t[1]) * i / pieces;
					double x1 = t[0] + (t[2] - t[0]) * (i + 1) / pieces, y1 = t[1] + (t[3] - t[1]) * (i + 1) / pieces;
					double[] piece = { x0, y0, x1, y1 };
					for (int cx = cell(Math.min(x0, x1)); cx <= cell(Math.max(x0, x1)); cx++) {
						for (int cy = cell(Math.min(y0, y1)); cy <= cell(Math.max(y0, y1)); cy++) {
							cells.computeIfAbsent(key(cx, cy), k -> new ArrayList<double[]>()).add(piece);
						}
					}
				}
			}
		}

		static int cell(double v) {
			return (int) Math.floor(v / CELL);
		}

		static long key(int cx, int cy) {
			return ((long) cx << 32) ^ (cy & 0xffffffffL);
		}

		double distance(double x, double y, List<double[]> all) {
			double best = Double.POSITIVE_INFINITY;
			int cx = cell(x), cy = cell(y);
			for (int i = cx - 1; i <= cx + 1; i++) {
				for (int j = cy - 1; j <= cy + 1; j++) {
					List<double[]> in = cells.get(key(i, j));
					if (in != null) for (double[] t : in) best = Math.min(best, toSegment(x, y, t));
				}
			}
			if (best <= CELL) return best;
			for (double[] t : all) best = Math.min(best, toSegment(x, y, t));
			return best;
		}
	}

	private static double toSegment(double x, double y, double[] t) {
		double dx = t[2] - t[0], dy = t[3] - t[1], l2 = dx * dx + dy * dy;
		double u = l2 == 0 ? 0 : Math.max(0, Math.min(1, ((x - t[0]) * dx + (y - t[1]) * dy) / l2));
		return Math.hypot(x - (t[0] + u * dx), y - (t[1] + u * dy));
	}

	/** Each probe of these files, drawn by docx4j as Word drew it: the fills, and the strokes, within the tolerance. */
	private static void drawsAsWord(String... files) throws Exception {
		List<String> off = new ArrayList<String>();
		int n = 0;
		double worst = 0;
		for (Probe p : PROBES.values()) {
			boolean mine = false;
			for (String f : files) mine |= p.id.startsWith(f + "-");
			if (!mine) continue;
			n++;
			ShapeOutlines o = outlines(p);
			assertNull(p.id + ": " + o.getDetail(), o.getProblem());
			// the geometry says which paths are filled; the shape's properties whether there is a fill at all
			boolean filled = p.xml.replaceAll("<a:ln[ >].*", "").matches(".*<a:(solid|grad|patt|blip)Fill.*");
			List<Path2D.Double> fills = new ArrayList<Path2D.Double>(), strokes = new ArrayList<Path2D.Double>();
			for (ShapeOutline s : o.getOutlines()) {
				if (filled && s.getFill() != STPathFillMode.NONE) fills.add(s.getPath());
				if (s.isStroked()) strokes.add(s.getPath());
			}
			// a fill with no area paints nothing, whether or not Word's PDF has one (line has none, diagStripe at 100000 one)
			double df = distance(withArea(p.fills), withArea(fills), true), ds = distance(p.strokes, strokes, false);
			boolean departs = WORD_DEPARTS.contains(p.id);
			if (!departs) worst = Math.max(worst, Math.max(df, ds));
			if ((df > TOLERANCE || ds > TOLERANCE) != departs) off.add(String.format("%s%s fill %.3f stroke %.3f", p.id, departs ? " (a known departure)" : "", df, ds));
		}
		assertTrue("no probes", n > 0);
		assertTrue(off.size() + " of " + n + " off Word by more than " + TOLERANCE + " pt, or a known departure no longer: " + off, off.isEmpty());
		System.out.println(String.join(", ", files) + ": " + n + " probes, the farthest " + String.format("%.3f", worst) + " pt from Word's");
	}

	@Test
	public void everyPresetAtItsDefaultsDrawsAsWord() throws Exception {
		drawsAsWord("fp10", "fp11"); // 100 by 100 and 144 by 72 pt
	}

	@Test
	public void everyAdjustedPresetDrawsAsWord() throws Exception {
		drawsAsWord("fp12", "fp13"); // each adjustment at half its default, and at double
	}

	@Test
	public void customGeometryDrawsAsWord() throws Exception {
		drawsAsWord("fp20", "fp21"); // each formula, path command and path attribute; the built-in guides; sqrt of a negative
	}

	/**
	 * The text rectangle, against the 8 pt "X" Word put at its top left (fp70 to fp73) and its bottom right (fp74 to fp77)
	 * with insets of 0. The glyph's offset from the rectangle is read from rect's, whose rectangle is its box.
	 */
	@Test
	public void textRectangleIsWords() throws Exception {
		double[] tl = glyph("fp73-002"), br = glyph("fp77-002"); // rect
		double offL = tl[0], offT = tl[1], offR = br[2] - 144, offB = br[3] - 108;
		List<String> off = new ArrayList<String>();
		int n = 0;
		double worst = 0;
		for (Probe p : PROBES.values()) {
			if (!p.id.startsWith("fp7") || p.glyph.isEmpty() || !p.xml.contains("prstGeom")) continue;
			boolean topLeft = p.id.compareTo("fp74") < 0;
			ShapeOutlines o = outlines(p);
			assertNull(p.id, o.getProblem());
			Rectangle2D r = o.getTextRectangle();
			double[] g = glyph(p.id);
			double a, b;
			if (topLeft) {
				a = (g[0] - offL) - r.getMinX();
				b = (g[1] - offT) - r.getMinY();
			} else {
				a = (g[2] - offR) - r.getMaxX();
				b = (g[3] - offB) - r.getMaxY();
			}
			n++;
			worst = Math.max(worst, Math.max(Math.abs(a), Math.abs(b)));
			int at = p.xml.indexOf("prst=") + 6;
			if (Math.abs(a) > TEXT_TOLERANCE || Math.abs(b) > TEXT_TOLERANCE) {
				off.add(String.format("%s %s %s %.3f %.3f", p.id, p.xml.substring(at, p.xml.indexOf('"', at)), topLeft ? "l t" : "r b", a, b));
			}
		}
		assertEquals(346, n); // 173 presets with text, at each corner: Word draws none in line, the connectors and three charts
		assertTrue(off.size() + " of " + n + " text rectangles off Word's: " + off, off.isEmpty());
		System.out.println(n + " text rectangles, the farthest " + String.format("%.3f", worst) + " pt from Word's glyph");
	}

	private static double[] glyph(String id) {
		String[] t = PROBES.get(id).glyph.split(" ");
		return new double[] { d(t[0]), d(t[1]), d(t[2]), d(t[3]) };
	}

	/** Every preset docx4j's definitions have evaluates, at a square and a wide size, with no problem. */
	@Test
	public void everyPresetIsDefined() {
		for (STShapeType t : STShapeType.values()) {
			CTPresetGeometry2D g = new CTPresetGeometry2D();
			g.setPrst(t);
			for (long[] size : new long[][] { { 1270000, 1270000 }, { 1828800, 914400 }, { 0, 914400 } }) {
				ShapeOutlines o = ShapeGeometry.outlines(g, size[0], size[1]);
				assertNull(t.value() + ": " + o.getDetail(), o.getProblem());
			}
		}
	}

	/** docx4j's leftArrow named an undefined dy in y1, where ECMA-376 has dy1 (CR-080-notes/licence-notes.md section 3). */
	@Test
	public void leftArrowReadsDy1() {
		CTPresetGeometry2D g = new CTPresetGeometry2D();
		g.setPrst(STShapeType.LEFT_ARROW);
		ShapeOutlines o = ShapeGeometry.outlines(g, 1270000, 1270000);
		assertNull(o.getDetail(), o.getProblem());
		// at the defaults the shaft is half the height, centred: y1 = vc - dy1 = 25, y2 = 75, from the head to r
		List<String> vertices = new ArrayList<String>();
		double[] c = new double[6];
		for (PathIterator it = o.getOutlines().get(0).getPath().getPathIterator(null); !it.isDone(); it.next()) {
			if (it.currentSegment(c) != PathIterator.SEG_CLOSE) vertices.add(String.format("%.2f %.2f", c[0], c[1]));
		}
		assertTrue(vertices.toString(), vertices.contains("100.00 25.00"));
		assertTrue(vertices.toString(), vertices.contains("100.00 75.00"));
	}

	// D9 and D10: hostile and broken geometry, each drawn as the box with its code

	private static ShapeOutlines custom(String inner, long cx, long cy) throws Exception {
		CTCustomGeometry2D g = (CTCustomGeometry2D) XmlUtils.unmarshalString(
				"<a:custGeom xmlns:a=\"" + A + "\">" + inner + "</a:custGeom>", Context.jc, CTCustomGeometry2D.class);
		return ShapeGeometry.outlines(g, cx, cy);
	}

	private static ShapeOutlines custom(String inner) throws Exception {
		return custom(inner, 914400, 914400);
	}

	private static final String TRIANGLE = "<a:moveTo><a:pt x=\"l\" y=\"b\"/></a:moveTo><a:lnTo><a:pt x=\"hc\" y=\"t\"/></a:lnTo>"
			+ "<a:lnTo><a:pt x=\"r\" y=\"b\"/></a:lnTo><a:close/>";

	private static void isBox(ShapeOutlines o, String problem) {
		assertEquals(o.getDetail(), problem, o.getProblem());
		assertNotNull(o.getDetail());
		assertEquals(1, o.getOutlines().size());
		Rectangle2D b = o.getOutlines().get(0).getPath().getBounds2D();
		assertEquals(new Rectangle2D.Double(0, 0, 72, 72), b);
		assertEquals(new Rectangle2D.Double(0, 0, 72, 72), o.getTextRectangle());
	}

	private static String repeat(String s, int n) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < n; i++) b.append(s);
		return b.toString();
	}

	@Test
	public void withinTheLimitsDraws() throws Exception {
		ShapeOutlines o = custom("<a:pathLst><a:path>" + TRIANGLE + "</a:path></a:pathLst>");
		assertNull(o.getDetail(), o.getProblem());
		assertEquals(new Rectangle2D.Double(0, 0, 72, 72), o.getOutlines().get(0).getPath().getBounds2D());
	}

	@Test
	public void tooManyGuides() throws Exception {
		StringBuilder gd = new StringBuilder();
		for (int i = 0; i <= GeometryReader.MAX_GUIDES; i++) gd.append("<a:gd name=\"g" + i + "\" fmla=\"val " + i + "\"/>");
		isBox(custom("<a:gdLst>" + gd + "</a:gdLst><a:pathLst><a:path>" + TRIANGLE + "</a:path></a:pathLst>"), ShapeOutlines.OVER_LIMIT);
	}

	@Test
	public void tooManyAdjustmentsOnAPreset() throws Exception {
		StringBuilder gd = new StringBuilder();
		for (int i = 0; i <= GeometryReader.MAX_GUIDES; i++) gd.append("<a:gd name=\"adj" + i + "\" fmla=\"val " + i + "\"/>");
		CTPresetGeometry2D g = (CTPresetGeometry2D) XmlUtils.unmarshalString("<a:prstGeom xmlns:a=\"" + A + "\" prst=\"roundRect\"><a:avLst>"
				+ gd + "</a:avLst></a:prstGeom>", Context.jc, CTPresetGeometry2D.class);
		isBox(ShapeGeometry.outlines(g, 914400, 914400), ShapeOutlines.OVER_LIMIT);
	}

	@Test
	public void tooManyPaths() throws Exception {
		isBox(custom("<a:pathLst>" + repeat("<a:path>" + TRIANGLE + "</a:path>", GeometryReader.MAX_PATHS + 1) + "</a:pathLst>"),
				ShapeOutlines.OVER_LIMIT);
	}

	@Test
	public void tooManyCommands() throws Exception {
		isBox(custom("<a:pathLst><a:path><a:moveTo><a:pt x=\"l\" y=\"t\"/></a:moveTo>"
				+ repeat("<a:lnTo><a:pt x=\"r\" y=\"b\"/></a:lnTo>", GeometryReader.MAX_COMMANDS) + "</a:path></a:pathLst>"), ShapeOutlines.OVER_LIMIT);
	}

	@Test
	public void tooMuchText() throws Exception {
		String name = repeat("g", GeometryReader.MAX_TEXT);
		isBox(custom("<a:gdLst><a:gd name=\"" + name + "\" fmla=\"val 1\"/></a:gdLst><a:pathLst><a:path>" + TRIANGLE + "</a:path></a:pathLst>"),
				ShapeOutlines.OVER_LIMIT);
	}

	@Test
	public void anArcPastSixteenTurns() throws Exception {
		String arc = "<a:pathLst><a:path fill=\"none\"><a:moveTo><a:pt x=\"r\" y=\"vc\"/></a:moveTo>"
				+ "<a:arcTo wR=\"wd2\" hR=\"hd2\" stAng=\"0\" swAng=\"%s\"/></a:path></a:pathLst>";
		assertNull(custom(String.format(arc, "345600000")).getProblem()); // 16 turns
		isBox(custom(String.format(arc, "345600001")), ShapeOutlines.OVER_LIMIT);
		isBox(custom("<a:gdLst><a:gd name=\"s\" fmla=\"*/ 345600000 2 1\"/></a:gdLst>" + String.format(arc, "s")), ShapeOutlines.OVER_LIMIT);
	}

	@Test
	public void aCoordinatePastTwoToTheThirtyOne() throws Exception {
		String at = "<a:pathLst><a:path><a:moveTo><a:pt x=\"l\" y=\"t\"/></a:moveTo><a:lnTo><a:pt x=\"%s\" y=\"b\"/></a:lnTo><a:close/></a:path></a:pathLst>";
		assertNull(custom(String.format(at, "2147483647")).getProblem());
		isBox(custom(String.format(at, "2147483648")), ShapeOutlines.OVER_LIMIT);
		isBox(custom("<a:gdLst><a:gd name=\"g\" fmla=\"val Infinity\"/></a:gdLst>" + String.format(at, "g")), ShapeOutlines.OVER_LIMIT);
		isBox(custom("<a:gdLst><a:gd name=\"g\" fmla=\"val NaN\"/></a:gdLst>" + String.format(at, "g")), ShapeOutlines.OVER_LIMIT);
		// a path in its own coordinates, scaled to the shape past the limit
		isBox(custom("<a:pathLst><a:path w=\"1\" h=\"1\"><a:moveTo><a:pt x=\"0\" y=\"0\"/></a:moveTo><a:lnTo><a:pt x=\"10000\" y=\"1\"/></a:lnTo>"
				+ "</a:path></a:pathLst>"), ShapeOutlines.OVER_LIMIT);
	}

	@Test
	public void aTextRectanglePastTheLimit() throws Exception {
		isBox(custom("<a:rect l=\"l\" t=\"t\" r=\"1e12\" b=\"b\"/><a:pathLst><a:path>" + TRIANGLE + "</a:path></a:pathLst>"), ShapeOutlines.OVER_LIMIT);
	}

	@Test
	public void aConnectionSitePastTheLimit() throws Exception {
		isBox(custom("<a:cxnLst><a:cxn ang=\"0\"><a:pos x=\"1e12\" y=\"t\"/></a:cxn></a:cxnLst><a:pathLst><a:path>" + TRIANGLE + "</a:path></a:pathLst>"),
				ShapeOutlines.OVER_LIMIT);
	}

	/** The most work the limits allow: every guide evaluated again for each of 256 paths in its own coordinates. */
	@Test(timeout = 20000)
	public void theWorstWithinTheLimitsIsBounded() throws Exception {
		StringBuilder gd = new StringBuilder("<a:gd name=\"g0\" fmla=\"val 1\"/>");
		for (int i = 1; i < GeometryReader.MAX_GUIDES; i++) gd.append("<a:gd name=\"g" + i + "\" fmla=\"+- g" + (i - 1) + " w 0\"/>");
		StringBuilder paths = new StringBuilder();
		int per = GeometryReader.MAX_COMMANDS / GeometryReader.MAX_PATHS;
		for (int i = 0; i < GeometryReader.MAX_PATHS; i++) {
			paths.append("<a:path w=\"" + (1000 + i) + "\" h=\"1000\"><a:moveTo><a:pt x=\"0\" y=\"0\"/></a:moveTo>");
			for (int j = 1; j < per; j++) paths.append("<a:arcTo wR=\"100\" hR=\"100\" stAng=\"0\" swAng=\"345600000\"/>");
			paths.append("</a:path>");
		}
		ShapeOutlines o = custom("<a:gdLst>" + gd + "</a:gdLst><a:pathLst>" + paths + "</a:pathLst>");
		assertNull(o.getDetail(), o.getProblem());
		assertEquals(GeometryReader.MAX_PATHS, o.getOutlines().size());
	}

	@Test
	public void anUndefinedGuide() throws Exception {
		isBox(custom("<a:pathLst><a:path><a:moveTo><a:pt x=\"nowhere\" y=\"t\"/></a:moveTo><a:lnTo><a:pt x=\"r\" y=\"b\"/></a:lnTo></a:path></a:pathLst>"),
				ShapeOutlines.BAD_GEOMETRY);
		isBox(custom("<a:gdLst><a:gd name=\"g\" fmla=\"*/ w nowhere 2\"/></a:gdLst><a:pathLst><a:path>" + TRIANGLE + "</a:path></a:pathLst>"),
				ShapeOutlines.BAD_GEOMETRY);
	}

	@Test
	public void anUnknownFormula() throws Exception {
		isBox(custom("<a:gdLst><a:gd name=\"g\" fmla=\"pow w 2\"/></a:gdLst><a:pathLst><a:path>" + TRIANGLE + "</a:path></a:pathLst>"),
				ShapeOutlines.BAD_GEOMETRY);
	}

	@Test
	public void aCommandWithNoCurrentPoint() throws Exception {
		isBox(custom("<a:pathLst><a:path><a:lnTo><a:pt x=\"r\" y=\"b\"/></a:lnTo></a:path></a:pathLst>"), ShapeOutlines.BAD_GEOMETRY);
		// Apache POI 2acb5cf66a: an arc with no current point
		isBox(custom("<a:pathLst><a:path><a:arcTo wR=\"wd2\" hR=\"hd2\" stAng=\"0\" swAng=\"5400000\"/></a:path></a:pathLst>"),
				ShapeOutlines.BAD_GEOMETRY);
	}

	@Test
	public void aBezierWithTheWrongPoints() throws Exception {
		isBox(custom("<a:pathLst><a:path><a:moveTo><a:pt x=\"l\" y=\"t\"/></a:moveTo><a:cubicBezTo><a:pt x=\"r\" y=\"t\"/><a:pt x=\"r\" y=\"b\"/>"
				+ "</a:cubicBezTo></a:path></a:pathLst>"), ShapeOutlines.BAD_GEOMETRY);
		isBox(custom("<a:pathLst><a:path><a:moveTo><a:pt x=\"l\" y=\"t\"/></a:moveTo><a:quadBezTo><a:pt x=\"r\" y=\"t\"/></a:quadBezTo>"
				+ "</a:path></a:pathLst>"), ShapeOutlines.BAD_GEOMETRY);
	}

	@Test
	public void aBezierWithTooManyPoints() throws Exception {
		isBox(custom("<a:pathLst><a:path><a:moveTo><a:pt x=\"l\" y=\"t\"/></a:moveTo><a:cubicBezTo><a:pt x=\"r\" y=\"t\"/><a:pt x=\"r\" y=\"b\"/>"
				+ "<a:pt x=\"l\" y=\"b\"/><a:pt x=\"hc\" y=\"vc\"/></a:cubicBezTo></a:path></a:pathLst>"), ShapeOutlines.BAD_GEOMETRY);
		isBox(custom("<a:pathLst><a:path><a:moveTo><a:pt x=\"l\" y=\"t\"/></a:moveTo><a:quadBezTo><a:pt x=\"r\" y=\"t\"/><a:pt x=\"r\" y=\"b\"/>"
				+ "<a:pt x=\"l\" y=\"b\"/></a:quadBezTo></a:path></a:pathLst>"), ShapeOutlines.BAD_GEOMETRY);
	}

	/** An arc on a flat ellipse runs along it, as Word draws donut's and the braces' at their limits (fp13-039, fp13-056). */
	@Test
	public void anArcWithNoWidthRunsAlongIt() throws Exception {
		// from the top (visual 270 degrees), half a turn: down the line to the bottom
		ShapeOutlines o = custom("<a:pathLst><a:path fill=\"none\"><a:moveTo><a:pt x=\"hc\" y=\"t\"/></a:moveTo>"
				+ "<a:arcTo wR=\"0\" hR=\"hd2\" stAng=\"16200000\" swAng=\"10800000\"/></a:path></a:pathLst>", 1828800, 914400);
		assertNull(o.getDetail(), o.getProblem());
		assertEquals(new Rectangle2D.Double(72, 0, 0, 72), o.getOutlines().get(0).getPath().getBounds2D());
		// from the left (180), a quarter: along the line to the centre
		o = custom("<a:pathLst><a:path fill=\"none\"><a:moveTo><a:pt x=\"l\" y=\"vc\"/></a:moveTo>"
				+ "<a:arcTo wR=\"wd2\" hR=\"0\" stAng=\"10800000\" swAng=\"5400000\"/></a:path></a:pathLst>", 1828800, 914400);
		assertNull(o.getDetail(), o.getProblem());
		Rectangle2D b = o.getOutlines().get(0).getPath().getBounds2D();
		assertEquals(0, b.getMinX(), 1e-9);
		assertEquals(72, b.getMaxX(), 1e-9);
		assertEquals(36, b.getMinY(), 1e-9);
		assertEquals(36, b.getMaxY(), 1e-9);
		// no radius at all: the point stays
		o = custom("<a:pathLst><a:path fill=\"none\"><a:moveTo><a:pt x=\"hc\" y=\"vc\"/></a:moveTo>"
				+ "<a:arcTo wR=\"0\" hR=\"0\" stAng=\"0\" swAng=\"5400000\"/><a:lnTo><a:pt x=\"r\" y=\"vc\"/></a:lnTo></a:path></a:pathLst>", 1828800, 914400);
		assertNull(o.getDetail(), o.getProblem());
		assertEquals(new Rectangle2D.Double(72, 36, 72, 0), o.getOutlines().get(0).getPath().getBounds2D());
	}

	/** fp20-034: a band each of norm, lighten, lightenLess, darken, darkenLess and none, in that order. */
	@Test
	public void eachPathKeepsItsFillMode() throws Exception {
		ShapeOutlines o = outlines(PROBES.get("fp20-034"));
		List<STPathFillMode> modes = new ArrayList<STPathFillMode>();
		for (ShapeOutline s : o.getOutlines()) modes.add(s.getFill());
		assertEquals(java.util.Arrays.asList(STPathFillMode.NORM, STPathFillMode.LIGHTEN, STPathFillMode.LIGHTEN_LESS, STPathFillMode.DARKEN,
				STPathFillMode.DARKEN_LESS, STPathFillMode.NONE), modes);
	}

	/**
	 * A path with its own w and h: its guides read that w and h, as Apache POI has it, so hc is the path's centre. Word's
	 * reading of a guide there isn't measured (fp20-030 has numbers only); this holds the reading docx4j takes.
	 */
	@Test
	public void aPathsOwnSpaceIsItsGuides() throws Exception {
		ShapeOutlines o = custom("<a:pathLst><a:path w=\"100\" h=\"100\"><a:moveTo><a:pt x=\"hc\" y=\"t\"/></a:moveTo>"
				+ "<a:lnTo><a:pt x=\"r\" y=\"b\"/></a:lnTo><a:lnTo><a:pt x=\"l\" y=\"b\"/></a:lnTo><a:close/></a:path></a:pathLst>", 1828800, 914400);
		assertNull(o.getDetail(), o.getProblem());
		assertEquals(new Rectangle2D.Double(0, 0, 144, 72), o.getOutlines().get(0).getPath().getBounds2D());
		double[] c = new double[6];
		o.getOutlines().get(0).getPath().getPathIterator(null).currentSegment(c);
		assertEquals(72, c[0], 1e-9);
	}

	@Test
	public void aBadAdjustmentOnAPreset() throws Exception {
		CTPresetGeometry2D g = (CTPresetGeometry2D) XmlUtils.unmarshalString("<a:prstGeom xmlns:a=\"" + A + "\" prst=\"roundRect\"><a:avLst>"
				+ "<a:gd name=\"adj\" fmla=\"val adj\"/></a:avLst></a:prstGeom>", Context.jc, CTPresetGeometry2D.class);
		isBox(ShapeGeometry.outlines(g, 914400, 914400), ShapeOutlines.BAD_GEOMETRY);
	}

	@Test
	public void aPresetWithNoName() {
		isBox(ShapeGeometry.outlines(new CTPresetGeometry2D(), 914400, 914400), ShapeOutlines.UNKNOWN_PRESET);
	}

	@Test
	public void noGeometryHasNoPaths() {
		ShapeOutlines o = ShapeGeometry.outlines(new CTShapeProperties(), 914400, 457200);
		assertNull(o.getProblem());
		assertTrue(o.getOutlines().isEmpty());
		assertEquals(new Rectangle2D.Double(0, 0, 72, 36), o.getTextRectangle());
	}

	@Test
	public void connectionSitesAndTheirAngles() {
		CTPresetGeometry2D g = new CTPresetGeometry2D();
		g.setPrst(STShapeType.RECT);
		ShapeOutlines o = ShapeGeometry.outlines(g, 1828800, 914400);
		// rect: top centre at 270 degrees, then left middle, bottom centre, right middle
		assertEquals(4, o.getConnectionSites().size());
		assertEquals(72, o.getConnectionSites().get(0).getPosition().getX(), 1e-9);
		assertEquals(0, o.getConnectionSites().get(0).getPosition().getY(), 1e-9);
		assertEquals(270, o.getConnectionSites().get(0).getAngle(), 1e-9);
	}
}
