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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.geom.AffineTransform;
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
import java.util.zip.GZIPInputStream;

import org.docx4j.XmlUtils;
import org.docx4j.chart.render.api.Drawing;
import org.docx4j.chart.render.api.Effects;
import org.docx4j.chart.render.api.ShapeRenderer;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.jaxb.Context;
import org.docx4j.model.color.ThemeColors;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A picture's frame against Word's (CR-081 phase 2): its outline (a:prstGeom), which clips the picture; its line, wholly
 * outside it; the fill behind it; and its effects, which follow the outline and the line. The oracle is Word
 * 16.0.20326's PDFs of phase 0's frame probes (fxr71) and of the flat picture styles over the photo, the photo as a
 * JPEG and both line arts (fs10, fs11, fs20, fs21; the 3-D styles are phase 5's), read by
 * CR-081-notes/export_effects.py: each effect picture as {@link EffectsTest} compares it, and each vector draw's box (a
 * line's path, a compound line's rings, the fill behind).
 */
public class PictureFramesTest {

	private static final String NS = " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\"";
	private static final double W = 144, H = 108;

	private static final List<Map<String, Object>> PROBES = new ArrayList<Map<String, Object>>();
	private static final Map<String, BufferedImage> PICTURES = new HashMap<String, BufferedImage>();
	private static ThemeColors theme;

	@BeforeClass
	public static void read() throws Exception {
		ClassLoader cl = PictureFramesTest.class.getClassLoader();
		try (InputStream in = new GZIPInputStream(cl.getResourceAsStream("effects/cr081-word-frames.jsonl.gz"));
				BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			for (String line; (line = r.readLine()) != null; ) PROBES.add(Json.object(Json.parse(line)));
		}
		// the photo as a JPEG (fs11) is the photo, re-encoded: its effects are the PNG's to Word's pixel
		for (String[] p : new String[][] { { "photo.png", "shapes/photo.png" }, { "photo.jpeg", "shapes/photo.png" }, { "lineart.png", "effects/lineart.png" },
				{ "lineart-alpha.png", "effects/lineart-alpha.png" } }) {
			try (InputStream in = cl.getResourceAsStream(p[1])) {
				PICTURES.put(p[0], javax.imageio.ImageIO.read(in));
			}
		}
		try (InputStream in = cl.getResourceAsStream("org/docx4j/openpackaging/parts/WordprocessingML/theme-2023.xml")) {
			theme = ThemeColors.of((org.docx4j.dml.Theme) XmlUtils.unwrap(XmlUtils.unmarshal(in)));
		}
	}

	/** The probe's pic:spPr: a style's as Word wrote it; a frame probe's geometry (its object's second word) and its XML */
	static CTShapeProperties spPr(Map<String, Object> p) throws Exception {
		String xml = (String) p.get("xml");
		String[] object = ((String) p.get("object")).split(" ");
		if (!xml.startsWith("<a:prstGeom")) {
			xml = "<a:prstGeom prst=\"" + (object.length > 1 ? object[1] : "rect") + "\"><a:avLst/></a:prstGeom>" + xml;
		}
		CTWordprocessingShape wsp = (CTWordprocessingShape) XmlUtils.unmarshalString("<wps:wsp" + NS + "><wps:spPr>" + xml
				+ "</wps:spPr><wps:bodyPr/></wps:wsp>", Context.jc, CTWordprocessingShape.class);
		return wsp.getSpPr();
	}

	static final class Drawn {
		ShapeRenderer.PictureFrame frame;
		Effects.Result effects;
	}

	/** The probe drawn as ShapeSvgDrawer draws a picture: its frame, then its effects from the picture in it */
	static Drawn draw(Map<String, Object> p) throws Exception {
		CTShapeProperties sp = spPr(p);
		BufferedImage img = PICTURES.get(((String) p.get("object")).split(" ")[0]);
		Drawn d = new Drawn();
		d.frame = ShapeRenderer.pictureFrame(sp, theme, Math.round(W * 12700), Math.round(H * 12700));
		AffineTransform at = ShapeSvgDrawer.frame(img.getWidth(), img.getHeight(), W, H, null);
		Rectangle2D.Double box = new Rectangle2D.Double(0, 0, W, H);
		Effects.Painter line = d.frame.getLine() == null ? null : g -> d.frame.getLine().paint(g);
		d.effects = sp.getEffectLst() == null ? null
				: Effects.draw(sp.getEffectLst(), theme, W, H, g -> ShapeSvgDrawer.paintPicture(g, d.frame, img, at, box), line, d.frame.getLineBounds(), 0);
		return d;
	}

	/** Word's draws of a kind in a layer: images (less a transparent picture's own redraw, where there is no soft edge), or vectors */
	static List<Map<String, Object>> word(Map<String, Object> p, String role, boolean images) {
		List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
		boolean soft = ((String) p.get("xml")).contains("softEdge");
		for (Object o : Json.list(p.get("effects"))) {
			Map<String, Object> e = Json.object(o);
			if (!role.equals(e.get("role"))) continue;
			if (images != "image".equals(e.get("kind"))) continue;
			if (role.equals("replace") && !soft) continue;
			out.add(e);
		}
		return out;
	}

	/** Reflected Rounded Rectangle (fs*-05): Word's reflection picture is cut past the fade */
	static final java.util.Set<String> FADE_CUT = java.util.Collections.singleton("-05");

	/** The box a line or fill paints in the drawing, as Word's PDF gives a path's: a stroke's path, a fill's shape */
	static Rectangle2D box(Drawing d) {
		Rectangle2D r = null;
		for (Object o : d.getItems()) {
			if (!(o instanceof Drawing.Area)) continue;
			Rectangle2D b = ((Drawing.Area) o).getShape().getBounds2D();
			r = r == null ? b : r.createUnion(b);
		}
		return r;
	}

	@Test
	public void theFramesAreWords() throws Exception {
		List<EffectsTest.Compared> all = new ArrayList<EffectsTest.Compared>();
		int vectors = 0;
		for (Map<String, Object> p : PROBES) {
			String probe = (String) p.get("probe"), what = probe + " " + p.get("note");
			Drawn d = draw(p);
			assertTrue(what + ": " + d.frame.getProblems(), d.frame.getProblems().isEmpty());
			// the effect pictures, layer by layer
			for (Effects.Layer layer : Effects.Layer.values()) {
				List<Map<String, Object>> word = word(p, layer.name().toLowerCase(Locale.ROOT), true);
				List<Effects.Picture> mine = new ArrayList<Effects.Picture>();
				if (d.effects != null) for (Effects.Picture pic : d.effects.getPictures()) if (pic.getLayer() == layer) mine.add(pic);
				assertEquals(what + " " + layer, word.size(), mine.size());
				for (int i = 0; i < word.size(); i++) all.add(EffectsTest.compare(probe, mine.get(i), word.get(i)));
			}
			// the vectors: the fill behind (under), the line (over: a stroke's path, a compound line's rings)
			List<Map<String, Object>> behind = word(p, "under", false), line = word(p, "over", false);
			assertEquals(what + " fill behind", behind.size(), d.frame.getBehind() == null ? 0 : 1);
			assertEquals(what + " line", line.size(), d.frame.getLine() == null ? 0 : 1);
			for (Object[] v : new Object[][] { { behind, d.frame.getBehind() }, { line, d.frame.getLine() } }) {
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> w = (List<Map<String, Object>>) v[0];
				if (w.isEmpty()) continue;
				Map<String, Object> e = w.get(0);
				Rectangle2D b = box((Drawing) v[1]);
				String at = String.format(Locale.ROOT, "%s %s: docx4j %.2f %.2f %.2f %.2f, Word %s %s %s %s", what, e.get("kind"), b.getX(), b.getY(),
						b.getWidth(), b.getHeight(), e.get("x"), e.get("y"), e.get("w"), e.get("h"));
				// fs*-14's path starts 0.18pt above its top: a point of Word's own (its first moveto)
				double tol = probe.endsWith("-14") ? 0.2 : 0.02;
				assertEquals(at, Json.number(e.get("x")), b.getX(), 0.02);
				assertEquals(at, Json.number(e.get("y")), b.getY(), tol);
				assertEquals(at, Json.number(e.get("w")), b.getWidth(), 0.02);
				assertEquals(at, Json.number(e.get("h")), b.getHeight(), tol);
				vectors++;
			}
		}
		if (Boolean.getBoolean("effects.report")) for (EffectsTest.Compared c : all) System.out.println(c);
		assertEquals("vector draws compared", 34, vectors);
		assertEquals("effect pictures compared", 56, all.size());
		double sum = 0;
		for (EffectsTest.Compared c : all) {
			String what = c.toString();
			assertTrue(what, Math.abs(c.dx) <= 1 && Math.abs(c.dw) <= 2.2);
			// Word's picture of Reflected Rounded Rectangle (endA left out) ends 16 pixels past the fade's end, where
			// fxr50-08's (endA="0" written) doesn't: the rows between are transparent, so docx4j's is the reflection's whole
			assertTrue(what, Math.abs(c.dy) <= 1 && (Math.abs(c.dh) <= 2.2 || FADE_CUT.contains(c.probe.substring(c.probe.indexOf('-')))));
			if (c.pptRatio == 0) continue;
			assertEquals(what, 1, c.pptRatio, 0.01);
			assertEquals(what, c.wordAmax, c.amax, 0.025);
			// a colour premultiplied in 8 bits at a low alpha is a few levels: Word's is read back only where its alpha is
			// a quarter or more (fs21-04's shadow of a line art's strokes peaks at 0.149: 2A2A2A for 333333)
			if (c.colour != null && c.wordColour != null && c.wordAmax >= 0.25) {
				for (int i = 0; i < 6; i += 2) {
					assertEquals(what, Integer.parseInt(c.wordColour.substring(i, i + 2), 16), Integer.parseInt(c.colour.substring(i, i + 2), 16), 6);
				}
			}
			assertTrue(what, c.mean <= 3.5);
			sum += c.mean;
		}
		assertTrue("mean alpha error " + sum / all.size(), sum / all.size() <= 1.0);
	}

	/** A rectangle with no line, no fill and no effects is plain: drawn as its bitmap alone */
	@Test
	public void aBareRectangleIsPlain() throws Exception {
		CTWordprocessingShape wsp = (CTWordprocessingShape) XmlUtils.unmarshalString("<wps:wsp" + NS
				+ "><wps:spPr><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom><a:ln><a:noFill/></a:ln></wps:spPr><wps:bodyPr/></wps:wsp>", Context.jc,
				CTWordprocessingShape.class);
		ShapeRenderer.PictureFrame f = ShapeRenderer.pictureFrame(wsp.getSpPr(), theme, 1828800, 1371600);
		assertTrue(f.isPlain());
		assertNull(f.getLine());
		assertNull(f.getBehind());
		assertTrue(ShapeRenderer.pictureFrame(null, theme, 1828800, 1371600).isPlain());
	}

	/** An ellipse's outline clips; a dashed line is stroked on the outline moved outward, dashed */
	@Test
	public void anEllipsesLineIsOutsideIt() throws Exception {
		CTWordprocessingShape wsp = (CTWordprocessingShape) XmlUtils.unmarshalString("<wps:wsp" + NS
				+ "><wps:spPr><a:prstGeom prst=\"ellipse\"><a:avLst/></a:prstGeom><a:ln w=\"76200\"><a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill>"
				+ "<a:prstDash val=\"dash\"/></a:ln></wps:spPr><wps:bodyPr/></wps:wsp>", Context.jc, CTWordprocessingShape.class);
		ShapeRenderer.PictureFrame f = ShapeRenderer.pictureFrame(wsp.getSpPr(), theme, 1828800, 1371600);
		assertTrue(!f.isBox());
		assertNotNull(f.getLine());
		Drawing.Area a = (Drawing.Area) f.getLine().getItems().get(0);
		Rectangle2D b = a.getShape().getBounds2D();
		assertEquals(-3, b.getX(), 0.01);
		assertEquals(-3, b.getY(), 0.01);
		assertEquals(150, b.getWidth(), 0.01);
		assertEquals(114, b.getHeight(), 0.01);
		assertTrue(((java.awt.BasicStroke) a.getStroke()).getDashArray() != null);
		assertEquals(-6, f.getLineBounds().getX(), 0.05);
	}
}
