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
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
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

	/**
	 * Phase 5b-1's gate: a probe's mean colour difference from Word's, over the points where both pictures are opaque
	 * (CR-081 section 6: about 6 / 255, set at the first fit). Word's model is fitted, not documented.
	 */
	private static final double SHADING_TOLERANCE = 6;
	/**
	 * Phase 5b-2's, set at its first fit: its turned views of the strong highlights (metal, powder) and the translucent
	 * materials miss most where a bevel meets a side, a crease docx4j's geometry puts a fraction of a pixel from Word's
	 * (16 to 18 / 255 on about a sixth of the points, the model's error there as large); 250 of its 257 probes are within
	 * 6, all within 8.5 (phase5b2-findings.md section 8)
	 */
	private static final double SHADING_TOLERANCE_B2 = 9;

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
		return true;
	}

	/**
	 * A 3-D drawing's shading against Word's (CR-081 phase 5b-1): every probe of the rounds cr081-phase5b-r1 and -r2
	 * (cr081-word-3d-light.jsonl.gz, from CR-081-notes/export_3d_light.py): the picture styles' four light rigs under their
	 * three materials on a deep circle bevel, a rectangle's and an ellipse's; turned rigs, another direction, turned
	 * cameras, an extrusion, a blue fill, the photo; and phase 5b-2's round cr081-phase5b2-r1: the other 23 rigs on an
	 * extruded ellipse from five views, the other materials but legacyWireframe from four, the other presets' shading,
	 * the six other directions and rigs rotated by lat and lon. At every point of a 1.5 pt grid where both pictures are
	 * opaque, round it, the colours' mean difference is within {@link #SHADING_TOLERANCE} (5b-2's within
	 * {@link #SHADING_TOLERANCE_B2}); for a translucent material, where both are not quite clear, the pictures over a white
	 * page.
	 */
	@Test
	public void theShadingIsWords() throws Exception {
		List<Map<String, Object>> probes = new ArrayList<Map<String, Object>>();
		ClassLoader cl = Scene3DTest.class.getClassLoader();
		try (InputStream in = new GZIPInputStream(cl.getResourceAsStream("effects/cr081-word-3d-light.jsonl.gz"));
				BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			for (String line; (line = r.readLine()) != null; ) probes.add(Json.object(Json.parse(line)));
		}
		BufferedImage photo = photo();
		int failed = 0;
		double worst = 0, sum = 0;
		StringBuilder report = new StringBuilder();
		for (Map<String, Object> p : probes) {
			double w = Json.number(p.get("w")), h = Json.number(p.get("h"));
			String geom = (String) p.get("geom");
			boolean shape = Boolean.TRUE.equals(p.get("shape"));
			CTShapeProperties sp = spPr("<a:prstGeom prst=\"" + geom + "\"><a:avLst/></a:prstGeom>" + p.get("xml"));
			Shape outline = "ellipse".equals(geom) ? new Ellipse2D.Double(0, 0, w, h) : new Rectangle2D.Double(0, 0, w, h);
			Color fill = new Color(Integer.parseInt((String) p.get("fill"), 16));
			Effects.Painter face = g -> {
				if (shape) {
					g.setColor(fill);
					g.fill(outline);
				} else {
					g.drawImage(photo, new AffineTransform(w / photo.getWidth(), 0, 0, h / photo.getHeight(), 0, 0), null);
				}
			};
			List<String> problems = new ArrayList<String>();
			Scene3D.Picture pic = Scene3D.draw(face, outline, w, h, sp.getScene3D(), sp.getSp3D(), theme, Effects.BASE_PIXELS_PER_POINT, problems);
			assertTrue((String) p.get("probe"), pic != null);
			assertTrue(p.get("probe") + " " + problems, !problems.contains(Scene3D.SHADING));
			Map<String, Object> word = Json.object(p.get("word"));
			double x0 = Json.number(word.get("x0")), y0 = Json.number(word.get("y0")), step = Json.number(word.get("step"));
			int cols = (int) Math.round(Json.number(word.get("cols"))), rows = (int) Math.round(Json.number(word.get("rows")));
			String rgb = (String) word.get("rgb");
			boolean overWhite = Boolean.TRUE.equals(word.get("overWhite"));
			double err = 0;
			int n = 0, words = 0;
			for (int j = 0; j < rows; j++) {
				for (int i = 0; i < cols; i++) {
					String cell = rgb.substring((j * cols + i) * 6, (j * cols + i) * 6 + 6);
					if (cell.charAt(0) == '.') continue;
					words++;
					int mine = overWhite ? overWhite(pic, x0 + i * step, y0 + j * step) : opaque(pic, x0 + i * step, y0 + j * step);
					if (mine == 0) continue;
					int c = Integer.parseInt(cell, 16);
					err += (Math.abs(((c >> 16) & 0xff) - ((mine >> 16) & 0xff)) + Math.abs(((c >> 8) & 0xff) - ((mine >> 8) & 0xff))
							+ Math.abs((c & 0xff) - (mine & 0xff))) / 3.0;
					n++;
				}
			}
			double mean = n > 0 ? err / n : 255;
			sum += mean;
			worst = Math.max(worst, mean);
			double tolerance = ((String) p.get("probe")).startsWith("fxh") ? SHADING_TOLERANCE_B2 : SHADING_TOLERANCE;
			boolean bad = mean > tolerance || n < 0.9 * words;
			if (bad) failed++;
			report.append(String.format(Locale.ROOT, "%n  %s %-60s %5.2f / 255 over %d of Word's %d points%s", p.get("probe"), p.get("note"), mean, n, words,
					bad ? "  PAST" : ""));
		}
		System.out.println(String.format(Locale.ROOT, "Scene3DTest shading: %d probes, mean %.2f / 255, worst %.2f, %d past %.1f (5b-2's %.1f)%s",
				probes.size(), sum / probes.size(), worst, failed, SHADING_TOLERANCE, SHADING_TOLERANCE_B2, report));
		assertTrue(failed + " of " + probes.size() + " probes past their tolerance:" + report, failed == 0);
	}

	/** docx4j's colour at a point of the frame, where it and its neighbours are opaque; else 0 */
	private static int opaque(Scene3D.Picture pic, double x, double y) {
		BufferedImage img = pic.getImage();
		Rectangle2D.Double place = pic.getPlace();
		int ix = (int) Math.floor((x - place.x) / place.width * img.getWidth()), iy = (int) Math.floor((y - place.y) / place.height * img.getHeight());
		if (ix < 1 || iy < 1 || ix + 1 >= img.getWidth() || iy + 1 >= img.getHeight()) return 0;
		for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) if ((img.getRGB(ix + dx, iy + dy) >>> 24) < 250) return 0;
		return img.getRGB(ix, iy) | 0xff000000;
	}

	/**
	 * docx4j's picture over a white page at a point of the frame, where it and its neighbours aren't quite clear (a
	 * translucent material's, phase 5b-2); else 0
	 */
	private static int overWhite(Scene3D.Picture pic, double x, double y) {
		BufferedImage img = pic.getImage();
		Rectangle2D.Double place = pic.getPlace();
		int ix = (int) Math.floor((x - place.x) / place.width * img.getWidth()), iy = (int) Math.floor((y - place.y) / place.height * img.getHeight());
		if (ix < 1 || iy < 1 || ix + 1 >= img.getWidth() || iy + 1 >= img.getHeight()) return 0;
		for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) if ((img.getRGB(ix + dx, iy + dy) >>> 24) < 5) return 0;
		int c = img.getRGB(ix, iy);
		double a = (c >>> 24) / 255.0;
		int out = 0xff000000;
		for (int sh = 16; sh >= 0; sh -= 8) out |= (int) Math.round(a * ((c >> sh) & 0xff) + (1 - a) * 255) << sh;
		return out;
	}

	/** The rounds' synthetic photo (CR-081-notes/build_phase0.py's photo()): a gradient, a green hill, a red disc top left */
	static BufferedImage photo() {
		BufferedImage img = new BufferedImage(480, 360, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < 360; y++) for (int x = 0; x < 480; x++) img.setRGB(x, y, new Color(40 + 180 * x / 480, 90 + 100 * y / 360, 200 - 120 * x / 480).getRGB());
		Graphics2D g = img.createGraphics();
		g.setColor(new Color(30, 100, 50));
		g.fillPolygon(new int[] { 0, 140, 300, 480, 480, 0 }, new int[] { 300, 230, 280, 210, 360, 360 }, 6);
		g.setColor(new Color(210, 30, 30));
		g.fillOval(30, 30, 81, 81);
		g.dispose();
		return img;
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

	/**
	 * A 3-D shape's drawing (CR-081 phase 5a) is one picture in place of its fill and line, its shadow cast by that
	 * picture; a flat shape's isn't. Its shading is Word's for a fitted rig and material (threePt, warmMatte: phase 5b-1),
	 * and reported for one not yet fitted (phase 5b-2)
	 */
	@Test
	public void a3dShapeIsOnePicture() throws Exception {
		String fill = "<a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"1828800\" cy=\"1371600\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/>"
				+ "</a:prstGeom><a:solidFill><a:srgbClr val=\"4472C4\"/></a:solidFill>";
		String shadow = "<a:effectLst><a:outerShdw blurRad=\"50800\" dist=\"38100\" dir=\"2700000\" algn=\"tl\" rotWithShape=\"0\">"
				+ "<a:prstClr val=\"black\"><a:alpha val=\"40000\"/></a:prstClr></a:outerShdw></a:effectLst>";
		String scene = "<a:scene3d><a:camera prst=\"isometricTopUp\"/><a:lightRig rig=\"threePt\" dir=\"t\"/></a:scene3d>"
				+ "<a:sp3d extrusionH=\"457200\"/>";
		org.docx4j.chart.render.api.ShapeRenderer.Drawn three = org.docx4j.chart.render.api.ShapeRenderer.draw(spPr(fill + shadow + scene), null,
				theme, 1828800, 1371600, null);
		List<Object> items = three.getDrawing().getItems();
		assertTrue(String.valueOf(items), items.size() == 1);    // it, its shadow in it (Word's one picture)
		assertTrue(items.get(0) instanceof org.docx4j.chart.render.api.Drawing.Picture);
		org.docx4j.chart.render.api.Drawing.Picture p = (org.docx4j.chart.render.api.Drawing.Picture) items.get(0);
		assertTrue("wider than its frame: " + p.getInto(), p.getInto().getWidth() > 150);
		// the shadow, in the drawing's plane and turned with it, makes the picture reach further than the drawing alone
		org.docx4j.chart.render.api.ShapeRenderer.Drawn alone = org.docx4j.chart.render.api.ShapeRenderer.draw(spPr(fill + scene), null, theme,
				1828800, 1371600, null);
		java.awt.geom.Rectangle2D a = ((org.docx4j.chart.render.api.Drawing.Picture) alone.getDrawing().getItems().get(0)).getInto();
		assertTrue(p.getInto() + " over " + a, p.getInto().getMaxY() > a.getMaxY() + 1 && p.getInto().getMaxX() > a.getMaxX() + 1);
		assertTrue(three.getProblems().toString(), !three.getProblems().contains(Scene3D.SHADING));
		// every rig and every material but legacyWireframe is Word's (phase 5b-2)
		assertTrue(!problems(fill + scene.replace("threePt", "morning"), false, false).contains(Scene3D.SHADING));
		assertTrue(!problems(fill + scene.replace("<a:sp3d ", "<a:sp3d prstMaterial=\"metal\" "), false, false).contains(Scene3D.SHADING));
		assertTrue(problems(fill + scene.replace("<a:sp3d ", "<a:sp3d prstMaterial=\"legacyWireframe\" "), false, false).contains(Scene3D.SHADING));
		// a rig turned by lat or lon is Word's too (phase 5b-2)
		assertTrue(!problems(fill + scene.replace("dir=\"t\"/>", "dir=\"t\"><a:rot lat=\"1200000\" lon=\"0\" rev=\"0\"/></a:lightRig>"), false, false)
				.contains(Scene3D.SHADING));
		assertTrue(three.getProblems().toString(), !three.getProblems().contains(org.docx4j.chart.render.api.ShapeRenderer.FLAT_3D));
		org.docx4j.chart.render.api.ShapeRenderer.Drawn flat = org.docx4j.chart.render.api.ShapeRenderer.draw(spPr(fill), null, theme, 1828800,
				1371600, null);
		assertTrue(flat.getDrawing().getItems().get(0) instanceof org.docx4j.chart.render.api.Drawing.Area);
	}

	/**
	 * A 3-D drawing's effects are the flat drawing's, in its plane, turned with it (CR-081 phase 5a findings section 5):
	 * every shape of round 3's fxe2 that Word draws as one 3-D picture (cr081-word-3d-effects.jsonl.gz, from
	 * export_3d_effects.py), drawn by {@link org.docx4j.chart.render.api.ShapeRenderer} and compared with Word's
	 * picture on a common grid: the drawing (alpha over 0.75) and the effect (a shadow's alpha about one half; a glow's
	 * or a reflection's over 0.05, off the drawing), by their overlap. Where Word's effect is hidden behind the drawing
	 * (a shadow scaled within the frame; one behind an extrusion), docx4j's is too.
	 */
	@Test
	public void theEffectsAreInTheDrawingsPlane() throws Exception {
		ClassLoader cl = Scene3DTest.class.getClassLoader();
		List<Map<String, Object>> probes = new ArrayList<Map<String, Object>>();
		try (InputStream in = new GZIPInputStream(cl.getResourceAsStream("effects/cr081-word-3d-effects.jsonl.gz"));
				BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			for (String line; (line = r.readLine()) != null; ) probes.add(Json.object(Json.parse(line)));
		}
		assertTrue(probes.size() >= 10);
		StringBuilder report = new StringBuilder();
		boolean ok = true;
		for (Map<String, Object> p : probes) {
			String note = (String) p.get("note");
			CTShapeProperties sp = spPr("<a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"1828800\" cy=\"1371600\"/></a:xfrm><a:prstGeom prst=\"rect\">"
					+ "<a:avLst/></a:prstGeom><a:solidFill><a:srgbClr val=\"FFE699\"/></a:solidFill>" + p.get("xml"));
			org.docx4j.chart.render.api.ShapeRenderer.Drawn d = org.docx4j.chart.render.api.ShapeRenderer.draw(sp, null, theme, 1828800, 1371600, null);
			org.docx4j.chart.render.api.Drawing.Picture mine = null;
			for (Object o : d.getDrawing().getItems()) if (o instanceof org.docx4j.chart.render.api.Drawing.Picture) mine = (org.docx4j.chart.render.api.Drawing.Picture) o;
			assertTrue(note, mine != null && d.getDrawing().getItems().size() == 1);
			Map<String, Object> word = Json.object(p.get("word"));
			BufferedImage wa = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(java.util.Base64.getDecoder().decode((String) word.get("alpha"))));
			List<Object> pl = Json.list(word.get("place"));
			Rectangle2D.Double wp = new Rectangle2D.Double(Json.number(pl.get(0)), Json.number(pl.get(1)), Json.number(pl.get(2)), Json.number(pl.get(3)));
			Rectangle2D grid = wp.createUnion(mine.getInto());
			boolean wide = note.contains("glow") || note.contains("reflection");
			int[] drawing = new int[4], effect = new int[4];        // both, either, Word's, docx4j's
			// a third of a pixel apart: at a pixel's pitch, a sliver a pixel wide turns on the grid's phase (0.99 or 0.76)
			for (double y = grid.getMinY(); y < grid.getMaxY(); y += 0.12) {
				for (double x = grid.getMinX(); x < grid.getMaxX(); x += 0.12) {
					double m = alpha(mine.getImage(), mine.getInto(), x, y, true), w = alpha(wa, wp, x, y, false);
					count(drawing, m > 0.75, w > 0.75);
					boolean me = wide ? m > 0.05 && m <= 0.75 : m > 0.3 && m < 0.7, we = wide ? w > 0.05 && w <= 0.75 : w > 0.3 && w < 0.7;
					count(effect, me, we);
				}
			}
			double dIou = drawing[0] / (double) Math.max(1, drawing[1]), eIou = effect[0] / (double) Math.max(1, effect[1]);
			boolean hidden = effect[2] < 18000;                    // Word's effect behind the drawing: a few edge pixels
			double need = note.contains("glow") ? 0.78 : 0.88;
			boolean pass = dIou >= 0.97 && (hidden ? effect[1] < 36000 : eIou >= need);
			ok &= pass;
			report.append(String.format(Locale.ROOT, "%n  %s %s: drawing %.3f, effect %.3f (Word %d samples, docx4j %d)%s%s", p.get("probe"), note, dIou, eIou,
					effect[2], effect[3],
					hidden ? " (hidden, " + effect[1] + " samples either)" : "", pass ? "" : "  FAILS"));
		}
		System.out.println("Scene3DTest effects:" + report);
		assertTrue(report.toString(), ok);
	}

	/**
	 * A 3-D shape's text (CR-081 phase 5a findings section 5): Word turns it into the 3-D picture under any camera but
	 * orthographicFront unless its bodyPr has a:flatTx; docx4j's is the output's, flat, so that is reported
	 */
	@Test
	public void turnedTextIsReported() throws Exception {
		String fill = "<a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"1828800\" cy=\"1371600\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/>"
				+ "</a:prstGeom><a:solidFill><a:srgbClr val=\"FFE699\"/></a:solidFill>";
		String iso = "<a:scene3d><a:camera prst=\"isometricTopUp\"/><a:lightRig rig=\"threePt\" dir=\"t\"/></a:scene3d><a:sp3d/>";
		String front = "<a:scene3d><a:camera prst=\"orthographicFront\"/><a:lightRig rig=\"threePt\" dir=\"t\"/></a:scene3d>"
				+ "<a:sp3d><a:bevelT w=\"76200\" h=\"76200\"/></a:sp3d>";
		String turned = "<a:scene3d><a:camera prst=\"orthographicFront\"><a:rot lat=\"0\" lon=\"2700000\" rev=\"0\"/></a:camera>"
				+ "<a:lightRig rig=\"threePt\" dir=\"t\"/></a:scene3d><a:sp3d/>";
		assertTrue(problems(fill + iso, true, false).contains(Scene3D.TEXT_3D));
		assertTrue(problems(fill + turned, true, false).contains(Scene3D.TEXT_3D));
		assertTrue(!problems(fill + iso, true, true).contains(Scene3D.TEXT_3D));     // a:flatTx
		assertTrue(!problems(fill + front, true, false).contains(Scene3D.TEXT_3D));  // a front camera
		assertTrue(!problems(fill + iso, false, false).contains(Scene3D.TEXT_3D));   // no text
	}

	private static List<String> problems(String sp, boolean text, boolean flat) throws Exception {
		return org.docx4j.chart.render.api.ShapeRenderer.draw(spPr(sp), null, theme, 1828800, 1371600, null, text, flat).getProblems();
	}

	private static void count(int[] c, boolean a, boolean b) {
		if (a && b) c[0]++;
		if (a || b) c[1]++;
		if (b) c[2]++;
		if (a) c[3]++;
	}

	/** A picture's alpha at a point of the frame, 0 outside it (argb: docx4j's; else a greyscale alpha, Word's) */
	private static double alpha(BufferedImage img, Rectangle2D place, double x, double y, boolean argb) {
		int ix = (int) Math.floor((x - place.getX()) / place.getWidth() * img.getWidth());
		int iy = (int) Math.floor((y - place.getY()) / place.getHeight() * img.getHeight());
		if (ix < 0 || iy < 0 || ix >= img.getWidth() || iy >= img.getHeight()) return 0;
		return argb ? (img.getRGB(ix, iy) >>> 24) / 255.0 : (img.getRaster().getSample(ix, iy, 0) & 0xff) / 255.0;
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
