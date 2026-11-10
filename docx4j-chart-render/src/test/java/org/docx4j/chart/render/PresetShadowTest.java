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
package org.docx4j.chart.render;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Shape;
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
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.docx4j.XmlUtils;
import org.docx4j.chart.render.api.Drawing;
import org.docx4j.chart.render.api.ShapeRenderer;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.jaxb.Context;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.shapes.ShapeDrawer;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The preset shadows ({@code a:prstShdw}, CR-081 phase 4) against Word's, as Word 16.0.20326's PDFs draw them
 * (CR-081-notes/export_effects.py --files PRESETS): each of the 20 presets on a 144 by 108pt picture (fxr60) and shape
 * (fxr61), dist 6pt at 45 degrees, black; and the round after phase 4 (cr081-phase4-r1): other dists and dirs (fxp1),
 * colours (fxp2), outlines (fxp3: an ellipse frame, a transparent picture, a star and an ellipse shape, an unfilled
 * shape's line), sizes (fxp5). The combinations (fxp4) are their own tests.
 * <ul>
 * <li>Word's vector fills: each fill's box within 0.05pt of Word's, both taken over the path's points (a curve's
 * control points too, as the PDF reading takes them), its colour Word's.</li>
 * <li>Where the colour has alpha, Word draws each fill as a picture of it (fxp2-02, -05, -07); docx4j's vector fill has
 * Word's place, to its picture's pixel of padding, and its colour and alpha.</li>
 * <li>A transparent picture's (fxp3-02, -07) is a picture of its strokes in Word and docx4j alike, compared as
 * {@link EffectsTest} compares a picture.</li>
 * </ul>
 */
public class PresetShadowTest {

	private static final String NS = " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\"";
	private static final Pattern SIZE = Pattern.compile("(\\d+) by (\\d+) pt");

	private static final List<Map<String, Object>> PROBES = new ArrayList<Map<String, Object>>();
	private static final Map<String, BufferedImage> PICTURES = new HashMap<String, BufferedImage>();
	private static ThemeColors theme;

	@BeforeClass
	public static void read() throws Exception {
		ClassLoader cl = PresetShadowTest.class.getClassLoader();
		try (InputStream in = new GZIPInputStream(cl.getResourceAsStream("effects/cr081-word-presets.jsonl.gz"));
				BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			for (String line; (line = r.readLine()) != null; ) PROBES.add(Json.object(Json.parse(line)));
		}
		for (String[] p : new String[][] { { "photo.png", "shapes/photo.png" }, { "lineart-alpha.png", "effects/lineart-alpha.png" } }) {
			try (InputStream in = cl.getResourceAsStream(p[1])) {
				PICTURES.put(p[0], javax.imageio.ImageIO.read(in));
			}
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

	/** The probe's size in points (its note names one other than 144 by 108) */
	static double[] size(Map<String, Object> p) {
		Matcher m = SIZE.matcher((String) p.get("note"));
		return m.find() ? new double[] { Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)) } : new double[] { 144, 108 };
	}

	/** What docx4j draws for a probe's preset shadow: its vector fills (shapes), or its pictures (a transparent picture's) */
	static final class Drawn {
		List<Drawing.Area> fills = new ArrayList<Drawing.Area>();
		List<ShapeDrawer.EffectPicture> pictures = new ArrayList<ShapeDrawer.EffectPicture>();
	}

	static Drawn draw(Map<String, Object> p) throws Exception {
		String[] object = ((String) p.get("object")).split(" ");
		String xml = (String) p.get("xml");
		double[] wh = size(p);
		long cx = Math.round(wh[0] * 12700), cy = Math.round(wh[1] * 12700);
		Drawn d = new Drawn();
		if (object[0].equals("shape")) {
			String geom = object.length > 1 ? object[1] : "rect";
			String fill = ((String) p.get("note")).contains("no fill") ? "<a:noFill/>" : "<a:solidFill><a:srgbClr val=\"4472C4\"/></a:solidFill>";
			// the probe's line, before its effects, in the schema's order
			CTShapeProperties sp = spPr("<a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"" + cx + "\" cy=\"" + cy + "\"/></a:xfrm><a:prstGeom prst=\""
					+ geom + "\"><a:avLst/></a:prstGeom>" + fill + xml);
			for (Object o : ShapeRenderer.draw(sp, null, theme, cx, cy, null).getDrawing().getItems()) {
				if (o instanceof Drawing.Area && "shape.shadow".equals(((Drawing.Area) o).getRole())) d.fills.add((Drawing.Area) o);
			}
			return d;
		}
		String geom = object.length > 1 ? object[1] : "rect";
		CTShapeProperties sp = spPr("<a:prstGeom prst=\"" + geom + "\"><a:avLst/></a:prstGeom>" + xml);
		BufferedImage img = PICTURES.get(object[0]);
		ShapeRenderer.PictureFrame f = ShapeRenderer.pictureFrame(sp, theme, cx, cy);
		if (ShapeSvgDrawer.transparent(img)) {
			for (ShapeDrawer.EffectPicture e : new ShapeSvgDrawer().drawPicture(sp, theme, wh[0], wh[1], img, null, 0, new ArrayList<String>()).getEffects()) {
				if ("prstShdw".equals(e.getEffect())) d.pictures.add(e);
			}
		} else if (f.getShadow() != null) {
			for (Object o : f.getShadow().getItems()) d.fills.add((Drawing.Area) o);
		}
		return d;
	}

	/** A shape's box over its path's points, a curve's control points included, as the PDF reading measures Word's */
	static Rectangle2D pointsBox(Shape s) {
		double x0 = Double.MAX_VALUE, y0 = Double.MAX_VALUE, x1 = -Double.MAX_VALUE, y1 = -Double.MAX_VALUE;
		double[] c = new double[6];
		for (PathIterator it = s.getPathIterator(null); !it.isDone(); it.next()) {
			int n = it.currentSegment(c);
			int pts = n == PathIterator.SEG_CUBICTO ? 3 : n == PathIterator.SEG_QUADTO ? 2 : n == PathIterator.SEG_CLOSE ? 0 : 1;
			for (int k = 0; k < pts; k++) {
				x0 = Math.min(x0, c[2 * k]);
				y0 = Math.min(y0, c[2 * k + 1]);
				x1 = Math.max(x1, c[2 * k]);
				y1 = Math.max(y1, c[2 * k + 1]);
			}
		}
		return new Rectangle2D.Double(x0, y0, x1 - x0, y1 - y0);
	}

	private static String at(String what, Rectangle2D b, Map<String, Object> e) {
		return String.format(Locale.ROOT, "%s: docx4j %.2f %.2f %.2f %.2f, Word %s %s %s %s", what, b.getX(), b.getY(), b.getWidth(), b.getHeight(),
				e.get("x"), e.get("y"), e.get("w"), e.get("h"));
	}

	/** Word's colour of a fill: a grey ("0.400") or an RGB triple ("1.000 0.400 0.400"), 0 to 1 */
	static double[] wordColour(String s) {
		String[] v = s.trim().split("\\s+");
		double[] c = new double[3];
		for (int i = 0; i < 3; i++) c[i] = Double.parseDouble(v[v.length == 1 ? 0 : i]);
		return c;
	}

	@Test
	public void thePresetShadowsAreWords() throws Exception {
		int vectors = 0, alpha = 0, pictures = 0;
		for (Map<String, Object> p : PROBES) {
			String probe = (String) p.get("probe");
			if (probe.startsWith("fxp4")) continue;
			String what = probe + " " + p.get("note");
			List<Map<String, Object>> word = new ArrayList<Map<String, Object>>();
			for (Object o : Json.list(p.get("effects"))) {
				Map<String, Object> e = Json.object(o);
				if ("under".equals(e.get("role"))) word.add(e);
			}
			Drawn d = draw(p);
			if (!d.pictures.isEmpty()) {
				// a transparent picture's: pictures of its strokes
				assertEquals(what, word.size(), d.pictures.size());
				for (int i = 0; i < word.size(); i++) {
					ShapeDrawer.EffectPicture pic = d.pictures.get(i);
					EffectsTest.Compared c = EffectsTest.compare(probe, "prstShdw", pic.getImage(), pic.getPlace(), word.get(i));
					assertTrue(c.toString(), Math.abs(c.dx) <= 1 && Math.abs(c.dy) <= 1 && Math.abs(c.dw) <= 1 && Math.abs(c.dh) <= 1);
					assertEquals(c.toString(), 1, c.pptRatio, 0.01);
					assertTrue(c.toString(), c.mean <= 3.5);
					assertEquals(c.toString(), c.wordAmax, c.amax, 0.025);
					pictures++;
				}
				continue;
			}
			assertEquals(what, word.size(), d.fills.size());
			for (int i = 0; i < word.size(); i++) {
				Map<String, Object> e = word.get(i);
				Drawing.Area a = d.fills.get(i);
				Rectangle2D b = pointsBox(a.getShape());
				Color c = a.getFill();
				if ("fill".equals(e.get("kind"))) {
					double tol = 0.05;
					assertEquals(at(what, b, e), Json.number(e.get("x")), b.getX(), tol);
					assertEquals(at(what, b, e), Json.number(e.get("y")), b.getY(), tol);
					assertEquals(at(what, b, e), Json.number(e.get("w")), b.getWidth(), tol);
					assertEquals(at(what, b, e), Json.number(e.get("h")), b.getHeight(), tol);
					double[] wc = wordColour((String) e.get("colour"));
					assertEquals(at(what, b, e), wc[0], c.getRed() / 255.0, 0.006);
					assertEquals(at(what, b, e), wc[1], c.getGreen() / 255.0, 0.006);
					assertEquals(at(what, b, e), wc[2], c.getBlue() / 255.0, 0.006);
					assertEquals(at(what, b, e), 255, c.getAlpha());
					vectors++;
				} else {
					// Word's picture of a fill with alpha: its place is the fill's, padded by a pixel or so
					double tol = 0.45;
					assertEquals(at(what, b, e), Json.number(e.get("x")), b.getX(), tol);
					assertEquals(at(what, b, e), Json.number(e.get("y")), b.getY(), tol);
					assertEquals(at(what, b, e), Json.number(e.get("w")), b.getWidth(), tol);
					assertEquals(at(what, b, e), Json.number(e.get("h")), b.getHeight(), tol);
					String hex = (String) e.get("colour");
					assertEquals(at(what, b, e), Integer.parseInt(hex.substring(0, 2), 16), c.getRed(), 2);
					assertEquals(at(what, b, e), Integer.parseInt(hex.substring(2, 4), 16), c.getGreen(), 2);
					assertEquals(at(what, b, e), Integer.parseInt(hex.substring(4, 6), 16), c.getBlue(), 2);
					assertEquals(at(what, b, e), Json.number(e.get("amax")), c.getAlpha() / 255.0, 0.01);
					alpha++;
				}
			}
		}
		assertEquals("vector fills compared", 92, vectors);
		assertEquals("fills with alpha compared", 5, alpha);
		assertEquals("pictures compared", 2, pictures);
	}

	private static Map<String, Object> probe(String id) {
		for (Map<String, Object> p : PROBES) if (id.equals(p.get("probe"))) return p;
		throw new IllegalArgumentException(id);
	}

	/**
	 * With other effects (fxp4): the preset shadow is drawn after the reflection and before the outer shadow and glow
	 * (fxp4-01 to -03), and the reflection reflects it (fxp4-01: Word's reflection 114.24pt tall, 109.74 without)
	 */
	@Test
	public void aPresetShadowIsAfterTheReflectionAndInIt() throws Exception {
		for (String id : new String[] { "fxp4-01", "fxp4-02", "fxp4-03" }) {
			Map<String, Object> p = probe(id);
			String xml = "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>" + p.get("xml");
			ShapeDrawer.PictureDrawing d = new ShapeSvgDrawer().drawPicture(spPr(xml), theme, 144, 108, PICTURES.get("photo.png"), null, 0,
					new ArrayList<String>());
			assertTrue(id, d.getShadow() != null);
			for (ShapeDrawer.EffectPicture e : d.getEffects()) {
				if (!"reflection".equals(e.getEffect())) continue;
				// Word's: y 102.69, h 114.24; reflected about the picture's foot, the preset 4.24pt lower
				assertEquals(id, 114.24, e.getPlace().getHeight(), 1.0);
			}
		}
		// in a shape's drawing: the reflection, the preset's fill, the outer shadow, then the shape
		CTShapeProperties sp = spPr("<a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"1828800\" cy=\"1371600\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/>"
				+ "</a:prstGeom><a:solidFill><a:srgbClr val=\"4472C4\"/></a:solidFill><a:effectLst><a:outerShdw blurRad=\"50800\" dist=\"38100\" "
				+ "dir=\"2700000\" algn=\"tl\" rotWithShape=\"0\"><a:srgbClr val=\"000000\"><a:alpha val=\"40000\"/></a:srgbClr></a:outerShdw>"
				+ "<a:prstShdw prst=\"shdw1\" dist=\"76200\" dir=\"2700000\"><a:prstClr val=\"black\"/></a:prstShdw><a:reflection blurRad=\"6350\" "
				+ "stA=\"52000\" endA=\"300\" endPos=\"35000\" dir=\"5400000\" sy=\"-100000\" algn=\"bl\" rotWithShape=\"0\"/></a:effectLst>");
		List<Object> items = ShapeRenderer.draw(sp, null, theme, 1828800, 1371600, null).getDrawing().getItems();
		assertTrue(items.get(0) instanceof Drawing.Picture);
		assertEquals("shape.shadow", ((Drawing.Area) items.get(1)).getRole());
		assertTrue(items.get(2) instanceof Drawing.Picture);
		assertEquals("shape.fill", ((Drawing.Area) items.get(3)).getRole());
	}

	/** A preset shadow alone makes a picture's frame more than its bitmap; none, none */
	@Test
	public void aPresetShadowIsAFramesShadow() throws Exception {
		String rect = "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>";
		ShapeRenderer.PictureFrame f = ShapeRenderer.pictureFrame(spPr(rect + "<a:effectLst><a:prstShdw prst=\"shdw6\" dist=\"38100\" dir=\"5400000\">"
				+ "<a:srgbClr val=\"FF0000\"><a:alpha val=\"50000\"/></a:srgbClr></a:prstShdw></a:effectLst>"), theme, 1828800, 1371600);
		assertTrue(!f.isPlain());
		Drawing.Area a = (Drawing.Area) f.getShadow().getItems().get(0);
		assertEquals(3, a.getShape().getBounds2D().getY(), 1e-6);
		assertEquals(255, a.getFill().getRed());
		assertEquals(128, a.getFill().getAlpha(), 1);
		assertNull(ShapeRenderer.pictureFrame(spPr(rect), theme, 1828800, 1371600).getShadow());
	}
}
