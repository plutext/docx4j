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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import org.docx4j.chart.render.api.ShapeRenderer;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.shapes.ShapeText;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A shape's effects against Word's (CR-081 phase 3): each probe shape of phase 0's shape files (fxr80: the effects on a
 * rect, an ellipse, a star, an arrow, an unfilled shape, a shape with text; fxr81 to fxr83: a:effectRef under the 2013,
 * 2023 and a custom theme), read from the probe documents themselves (their themes resolve the references), drawn by
 * {@link ShapeRenderer#draw}, its effect pictures against Word 16.0.20326's from the same files' PDFs
 * (CR-081-notes/export_effects.py --files SHAPES), compared as {@link EffectsTest} compares a picture's.
 *
 * <p>Word's text casts the shape's effects too (fxr80-10, -11: a shadow picture of its own under the fill, and the text
 * drawn as a picture); docx4j's text is the output's, so those pictures are left out, and the gap is reported where it
 * shows ({@link ShapeRenderer#TEXT_EFFECTS}).</p>
 */
public class ShapeEffectsTest {

	private static final String[] FILES = { "fxr80-shapes", "fxr81-effectref-2013", "fxr82-effectref-2023", "fxr83-effectref-custom" };

	private static final List<Map<String, Object>> PROBES = new ArrayList<Map<String, Object>>();
	private static final Map<String, List<Inline>> SHAPES = new HashMap<String, List<Inline>>();
	private static final Map<String, ThemeColors> THEMES = new HashMap<String, ThemeColors>();

	@BeforeClass
	public static void read() throws Exception {
		ClassLoader cl = ShapeEffectsTest.class.getClassLoader();
		try (InputStream in = new GZIPInputStream(cl.getResourceAsStream("effects/cr081-word-shapes.jsonl.gz"));
				BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			for (String line; (line = r.readLine()) != null; ) PROBES.add(Json.object(Json.parse(line)));
		}
		for (String f : FILES) {
			try (InputStream in = cl.getResourceAsStream("effects/cr081-" + f + ".docx")) {
				WordprocessingMLPackage pkg = WordprocessingMLPackage.load(in);
				List<Inline> inlines = new ArrayList<Inline>();
				collect(pkg.getMainDocumentPart().getContent(), inlines);
				SHAPES.put(f, inlines);
				THEMES.put(f, ThemeColors.of(pkg));
			}
		}
	}

	/** The inline drawings, in document order: a probe a page, a drawing a probe */
	private static void collect(List<Object> content, List<Inline> out) {
		for (Object o : content) {
			Object v = XmlUtils.unwrap(o);
			if (v instanceof org.docx4j.wml.Drawing) {
				for (Object a : ((org.docx4j.wml.Drawing) v).getAnchorOrInline()) if (a instanceof Inline) out.add((Inline) a);
			} else if (v instanceof org.docx4j.mce.AlternateContent) {
				// the probes' wps drawings are in mc:Choice Requires="wps", as Word writes them
				for (org.docx4j.mce.AlternateContent.Choice c : ((org.docx4j.mce.AlternateContent) v).getChoice()) collect(c.getAny(), out);
			} else if (v instanceof ContentAccessor) {
				collect(((ContentAccessor) v).getContent(), out);
			}
		}
	}

	private static CTWordprocessingShape wsp(Inline inline) {
		for (Object o : inline.getGraphic().getGraphicData().getAny()) {
			Object v = XmlUtils.unwrap(o);
			if (v instanceof CTWordprocessingShape) return (CTWordprocessingShape) v;
		}
		return null;
	}

	/** The probe's shape drawn as the PDF output draws it */
	static ShapeRenderer.Drawn draw(Map<String, Object> p) {
		String file = (String) p.get("file");
		Inline inline = SHAPES.get(file).get(Json.number(p.get("page")).intValue() - 1);
		CTWordprocessingShape wsp = wsp(inline);
		return ShapeRenderer.draw(wsp.getSpPr(), wsp.getStyle(), THEMES.get(file), inline.getExtent().getCx(), inline.getExtent().getCy(), null,
				ShapeText.hasText(wsp));
	}

	/** The effect pictures in a drawing by where they are: before the shape's first area under it, after one over it; alone, in its place */
	static Map<String, List<Drawing.Picture>> layers(Drawing d) {
		Map<String, List<Drawing.Picture>> out = new HashMap<String, List<Drawing.Picture>>();
		for (String l : new String[] { "under", "over", "replace" }) out.put(l, new ArrayList<Drawing.Picture>());
		boolean areas = false;
		for (Object o : d.getItems()) if (o instanceof Drawing.Area) areas = true;
		boolean seen = false;
		for (Object o : d.getItems()) {
			if (o instanceof Drawing.Area) seen = true;
			if (o instanceof Drawing.Picture) out.get(!areas ? "replace" : seen ? "over" : "under").add((Drawing.Picture) o);
		}
		return out;
	}

	/** Word's effect pictures in a layer, less its text's (a probe with text: the shape's own shadow is the first) */
	static List<Map<String, Object>> word(Map<String, Object> p, String role) {
		List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
		boolean text = ((String) p.get("note")).contains("text");
		for (Object o : Json.list(p.get("effects"))) {
			Map<String, Object> e = Json.object(o);
			if (!role.equals(e.get("role")) || !"image".equals(e.get("kind"))) continue;
			if (text && (!role.equals("under") || !out.isEmpty())) continue;
			out.add(e);
		}
		return out;
	}

	@Test
	public void theShapesEffectsAreWords() throws Exception {
		List<EffectsTest.Compared> all = new ArrayList<EffectsTest.Compared>();
		for (Map<String, Object> p : PROBES) {
			String probe = (String) p.get("probe"), what = probe + " " + p.get("note");
			ShapeRenderer.Drawn d = draw(p);
			if (p.get("dw") == null) {
				// fxr80-10: an unfilled shape with no line, whose text alone casts Word's shadow: nothing drawn, the gap reported
				assertTrue(what, layers(d.getDrawing()).get("under").isEmpty() && layers(d.getDrawing()).get("replace").isEmpty());
				assertTrue(what + ": " + d.getProblems(), d.getProblems().contains(ShapeRenderer.TEXT_EFFECTS));
				continue;
			}
			// a filled shape's text casts its shadow under the fill, which hides it: not reported
			assertFalse(what + ": " + d.getProblems(), d.getProblems().contains(ShapeRenderer.TEXT_EFFECTS));
			Map<String, List<Drawing.Picture>> mine = layers(d.getDrawing());
			for (String role : new String[] { "under", "replace", "over" }) {
				List<Map<String, Object>> w = word(p, role);
				assertEquals(what + " " + role, w.size(), mine.get(role).size());
				for (int i = 0; i < w.size(); i++) {
					Drawing.Picture pic = mine.get(role).get(i);
					all.add(EffectsTest.compare(probe, role, pic.getImage(), pic.getInto(), w.get(i)));
				}
			}
		}
		if (Boolean.getBoolean("effects.report")) for (EffectsTest.Compared c : all) System.out.println(c);
		assertEquals("effect pictures compared", 14, all.size());
		double sum = 0;
		for (EffectsTest.Compared c : all) {
			String what = c.toString();
			assertTrue(what, Math.abs(c.dx) <= 1 && Math.abs(c.dw) <= 2.2);
			assertTrue(what, Math.abs(c.dy) <= 1 && Math.abs(c.dh) <= 2.2);
			assertEquals(what, 1, c.pptRatio, 0.01);
			assertEquals(what, c.wordAmax, c.amax, 0.025);
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

	/** A shape's effects take its drawing's place in order: under it, the fill, the inner shadow, the line */
	@Test
	public void anInnerShadowIsOverTheFillAndUnderTheLine() throws Exception {
		Map<String, Object> p = null;
		for (Map<String, Object> q : PROBES) if ("fxr80-06".equals(q.get("probe"))) p = q;
		List<Object> items = draw(p).getDrawing().getItems();
		assertTrue(items.get(0) instanceof Drawing.Area);
		assertEquals("shape.fill", ((Drawing.Area) items.get(0)).getRole());
		assertTrue(items.get(1) instanceof Drawing.Picture);
		String report = String.format(Locale.ROOT, "%d items", items.size());
		for (int i = 2; i < items.size(); i++) assertEquals(report, "shape.line", ((Drawing.Area) items.get(i)).getRole());
	}
}
