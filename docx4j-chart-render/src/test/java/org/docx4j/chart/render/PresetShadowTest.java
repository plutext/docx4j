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

import java.awt.geom.Rectangle2D;
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
import org.docx4j.chart.render.api.Drawing;
import org.docx4j.chart.render.api.ShapeRenderer;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.jaxb.Context;
import org.docx4j.model.color.ThemeColors;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The preset shadows ({@code a:prstShdw}, CR-081 phase 4) against Word's: each of the 20 presets over a 144 by 108pt
 * picture (fxr60) and shape (fxr61), dist 6pt at 45 degrees, black, as Word 16.0.20326's PDFs draw them
 * (CR-081-notes/export_effects.py --files PRESETS): unblurred vector fills under the drawing, each fill's box within
 * 0.02pt of Word's path's and its grey Word's (0 for the shadow, 0.4 for shdw13's, 17's and 18's second fill).
 */
public class PresetShadowTest {

	private static final String NS = " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\"";

	private static final List<Map<String, Object>> PROBES = new ArrayList<Map<String, Object>>();
	private static ThemeColors theme;

	@BeforeClass
	public static void read() throws Exception {
		ClassLoader cl = PresetShadowTest.class.getClassLoader();
		try (InputStream in = new GZIPInputStream(cl.getResourceAsStream("effects/cr081-word-presets.jsonl.gz"));
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

	/** The fills a probe draws for its preset shadow: a picture's frame's, or a shape's drawing's, in order */
	static List<Drawing.Area> fills(Map<String, Object> p) throws Exception {
		String rect = "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>";
		List<Drawing.Area> out = new ArrayList<Drawing.Area>();
		if (((String) p.get("object")).startsWith("shape")) {
			CTShapeProperties sp = spPr("<a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"1828800\" cy=\"1371600\"/></a:xfrm>" + rect
					+ "<a:solidFill><a:srgbClr val=\"4472C4\"/></a:solidFill>" + p.get("xml"));
			for (Object o : ShapeRenderer.draw(sp, null, theme, 1828800, 1371600, null).getDrawing().getItems()) {
				if (o instanceof Drawing.Area && "shape.shadow".equals(((Drawing.Area) o).getRole())) out.add((Drawing.Area) o);
			}
		} else {
			ShapeRenderer.PictureFrame f = ShapeRenderer.pictureFrame(spPr(rect + p.get("xml")), theme, 1828800, 1371600);
			if (f.getShadow() != null) for (Object o : f.getShadow().getItems()) out.add((Drawing.Area) o);
		}
		return out;
	}

	@Test
	public void thePresetShadowsAreWords() throws Exception {
		int compared = 0;
		for (Map<String, Object> p : PROBES) {
			String what = p.get("probe") + " " + p.get("note");
			List<Map<String, Object>> word = new ArrayList<Map<String, Object>>();
			for (Object o : Json.list(p.get("effects"))) {
				Map<String, Object> e = Json.object(o);
				if ("under".equals(e.get("role")) && "fill".equals(e.get("kind"))) word.add(e);
			}
			List<Drawing.Area> mine = fills(p);
			assertEquals(what, word.size(), mine.size());
			for (int i = 0; i < word.size(); i++) {
				Map<String, Object> e = word.get(i);
				Rectangle2D b = mine.get(i).getShape().getBounds2D();
				String at = String.format(Locale.ROOT, "%s fill %d: docx4j %.2f %.2f %.2f %.2f, Word %s %s %s %s", what, i, b.getX(), b.getY(), b.getWidth(),
						b.getHeight(), e.get("x"), e.get("y"), e.get("w"), e.get("h"));
				assertEquals(at, Json.number(e.get("x")), b.getX(), 0.02);
				assertEquals(at, Json.number(e.get("y")), b.getY(), 0.02);
				assertEquals(at, Json.number(e.get("w")), b.getWidth(), 0.02);
				assertEquals(at, Json.number(e.get("h")), b.getHeight(), 0.02);
				double grey = Double.parseDouble(((String) e.get("colour")).trim());
				java.awt.Color c = mine.get(i).getFill();
				assertEquals(at, grey, c.getRed() / 255.0, 0.01);
				assertTrue(at, c.getRed() == c.getGreen() && c.getGreen() == c.getBlue() && c.getAlpha() == 255);
				compared++;
			}
		}
		assertEquals("fills compared", 46, compared);
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
