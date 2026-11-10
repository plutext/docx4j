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
import static org.junit.Assert.assertTrue;

import java.awt.geom.Area;
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
import org.docx4j.chart.render.api.ShapeRenderer;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingCanvas.CTWordprocessingCanvas;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup.CTWordprocessingGroup;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.jaxb.Context;
import org.docx4j.model.color.ThemeColors;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * CR-080 phase 3: groups and canvases, and single shapes turned and flipped, drawn where Word draws them. The oracle is
 * phase 0's round 2 (fp90 to fp92), Word 16.0.20326.20232's PDFs read by CR-080-notes/read_shapes.py and written by
 * export_groups.py, each probe's whole graphic beside Word's draws; measured as {@link ShapeRendererTest} measures.
 */
public class GroupRendererTest {

	private static final String NS = " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\""
			+ " xmlns:wpg=\"http://schemas.microsoft.com/office/word/2010/wordprocessingGroup\""
			+ " xmlns:wpc=\"http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
			+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
			+ " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final List<Map<String, Object>> PROBES = new ArrayList<Map<String, Object>>();
	private static final Map<String, ThemeColors> THEMES = new HashMap<String, ThemeColors>();
	private static BufferedImage photo;

	@BeforeClass
	public static void read() throws Exception {
		try (InputStream in = new GZIPInputStream(GroupRendererTest.class.getClassLoader().getResourceAsStream("shapes/cr080-word-groups.jsonl.gz"));
				BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			for (String line; (line = r.readLine()) != null; ) PROBES.add(Json.object(Json.parse(line)));
		}
		for (String t : new String[] { "2023", "2013" }) {
			try (InputStream in = GroupRendererTest.class.getClassLoader().getResourceAsStream("org/docx4j/openpackaging/parts/WordprocessingML/theme-" + t + ".xml")) {
				THEMES.put(t, ThemeColors.of((org.docx4j.dml.Theme) XmlUtils.unwrap(XmlUtils.unmarshal(in))));
			}
		}
		try (InputStream in = GroupRendererTest.class.getClassLoader().getResourceAsStream("shapes/photo.png")) {
			photo = javax.imageio.ImageIO.read(in);
		}
	}

	static ShapeRenderer.Drawn draw(Map<String, Object> p) throws Exception {
		String g = (String) p.get("graphic");
		String xml = g.replaceFirst("^<(\\w+:\\w+)", "<$1" + NS);
		String[] size = ((String) p.get("size")).split("x");
		long cx = Math.round(Double.parseDouble(size[0]) * 12700), cy = Math.round(Double.parseDouble(size[1]) * 12700);
		ThemeColors theme = THEMES.get(p.get("theme"));
		if (g.startsWith("<wpg:wgp")) {
			return ShapeRenderer.drawGroup((CTWordprocessingGroup) XmlUtils.unmarshalString(xml, Context.jc, CTWordprocessingGroup.class), theme, cx, cy,
					id -> photo);
		}
		if (g.startsWith("<wpc:wpc")) {
			return ShapeRenderer.drawCanvas((CTWordprocessingCanvas) XmlUtils.unmarshalString(xml, Context.jc, CTWordprocessingCanvas.class), theme, cx,
					cy, id -> photo);
		}
		CTWordprocessingShape wsp = (CTWordprocessingShape) XmlUtils.unmarshalString(xml, Context.jc, CTWordprocessingShape.class);
		return ShapeRenderer.draw(wsp.getSpPr(), wsp.getStyle(), theme, cx, cy, id -> photo);
	}

	/**
	 * Word's draws but its text's: in a probe with a text box, Word's draws from the empty picture it writes for a
	 * turned, flipped or grouped text box on are its text (phase 0, findings §1), turned text as glyph outlines, which
	 * docx4j keeps as text (D3)
	 */
	static Map<String, Object> withoutText(Map<String, Object> p) {
		if (!((String) p.get("graphic")).contains("<wps:txbx>")) return p;
		List<Object> kept = new ArrayList<Object>();
		for (Object o : Json.list(p.get("draws"))) {
			if ("image".equals(Json.object(o).get("k"))) break;
			kept.add(o);
		}
		Map<String, Object> q = new HashMap<String, Object>(p);
		q.put("draws", kept);
		return q;
	}

	/** Every probe paints within this of where Word paints, in points */
	private static final double TOLERANCE = 0.1;

	@Test
	public void everyProbePaintsWhereWordDoes() throws Exception {
		List<String> off = new ArrayList<String>();
		double worst = 0;
		String farthest = "";
		for (Map<String, Object> p : PROBES) {
			ShapeRenderer.Drawn drawn = draw(p);
			Map<String, Object> word = withoutText(p);
			ShapeRendererTest.where = "";
			double dist = ShapeRendererTest.apart(ShapeRendererTest.word(word), ShapeRendererTest.ours(drawn));
			TreeSet<String> wc = ShapeRendererTest.wordColours(word), oc = ShapeRendererTest.ourColours(drawn);
			if (dist > worst) {
				worst = dist;
				farthest = p.get("probe") + " " + ShapeRendererTest.where;
			}
			if (Boolean.getBoolean("cr080.report")) System.out.println(String.format("   %s %.3f %s", p.get("probe"), dist, ShapeRendererTest.where));
			if (dist > TOLERANCE || !wc.equals(oc)) {
				off.add(String.format("%s %.3f %s%s (%s)", p.get("probe"), dist, ShapeRendererTest.where,
						wc.equals(oc) ? "" : " colours " + wc + ", ours " + oc, p.get("note")));
			}
		}
		assertTrue(off.size() + " of " + PROBES.size() + " probes off Word: " + off, off.isEmpty());
		System.out.println(PROBES.size() + " probes, the farthest " + String.format("%.3f", worst) + " pt from Word's, " + farthest);
	}

	/**
	 * Phase 3's gate on the PDF: docx4j's PDFs of round 2's probe files, read as Word's are (CR-080-notes/read_shapes.py
	 * --out, then export_groups.py --json --out), against Word's, by the same measure; and the first glyph of each
	 * probe's text where Word's is level, within 0.5pt at both corners. Run where -Dcr080.groupPdfDraws names that
	 * file; skipped otherwise, as the PDFs come from docx4j-export-fo.
	 */
	@Test
	public void docx4jsPdfsPaintWhereWordsDo() throws Exception {
		String file = System.getProperty("cr080.groupPdfDraws");
		org.junit.Assume.assumeTrue("no -Dcr080.groupPdfDraws", file != null);
		Map<String, Map<String, Object>> ours = new HashMap<String, Map<String, Object>>();
		try (InputStream in = new GZIPInputStream(new java.io.FileInputStream(file));
				BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			for (String line; (line = r.readLine()) != null; ) {
				Map<String, Object> p = Json.object(Json.parse(line));
				ours.put((String) p.get("probe"), p);
			}
		}
		List<String> off = new ArrayList<String>();
		double worst = 0, worstGlyph = 0;
		int n = 0, glyphs = 0;
		for (Map<String, Object> p : PROBES) {
			Map<String, Object> o = ours.get(p.get("probe"));
			if (o == null) continue;
			n++;
			Map<String, Object> word = withoutText(p), docx4j = withoutText(o);
			ShapeRendererTest.where = "";
			double dist = ShapeRendererTest.apart(ShapeRendererTest.word(word), ShapeRendererTest.word(docx4j));
			TreeSet<String> wc = ShapeRendererTest.wordColours(word), oc = ShapeRendererTest.wordColours(docx4j);
			worst = Math.max(worst, dist);
			if (dist > TOLERANCE || !wc.equals(oc)) {
				off.add(String.format("%s %.3f %s%s (%s)", p.get("probe"), dist, ShapeRendererTest.where,
						wc.equals(oc) ? "" : " colours " + wc + ", docx4j's " + oc, p.get("note")));
			}
			if (p.get("glyph") != null) {
				glyphs++;
				if (o.get("glyph") == null) {
					off.add(p.get("probe") + " no glyph in docx4j's (" + p.get("note") + ")");
					continue;
				}
				List<Object> a = Json.list(p.get("glyph")), b = Json.list(o.get("glyph"));
				double g = 0;
				for (int i = 0; i < 4; i++) g = Math.max(g, Math.abs(Json.number(a.get(i)) - Json.number(b.get(i))));
				worstGlyph = Math.max(worstGlyph, g);
				if (g > 0.5) off.add(String.format("%s glyph %.2f off: Word's %s, docx4j's %s (%s)", p.get("probe"), g, a, b, p.get("note")));
			}
		}
		for (String l : off) System.out.println("   " + l);
		System.out.println(n + " probes in docx4j's PDFs, " + off.size() + " off Word's; the farthest " + String.format("%.3f", worst)
				+ " pt, glyphs " + glyphs + " within " + String.format("%.2f", worstGlyph) + " pt");
		assertTrue(off.size() + " of " + n + " probes in docx4j's PDFs off Word's", off.isEmpty());
	}

	/** A group's and a canvas's shapes and pictures are listed where they are drawn, in document order, text boxes among them */
	@Test
	public void theChildrenAreListedWhereTheyAreDrawn() throws Exception {
		Map<String, Map<String, Object>> byId = new HashMap<String, Map<String, Object>>();
		for (Map<String, Object> p : PROBES) byId.put((String) p.get("probe"), p);
		// a child space twice the group's: the children at half size
		List<ShapeRenderer.Placed> half = draw(byId.get("fp91-002")).getChildren();
		assertEquals(2, half.size());
		assertEquals(36, half.get(0).getBox().getWidth(), 1e-9);
		assertEquals(54, half.get(0).getBox().getHeight(), 1e-9);
		assertEquals(36, half.get(1).getBox().getX(), 1e-9);
		// three deep, the middle group half the width: the triangle's box 72 x 108, its turns 20 - 15 under the flip
		List<ShapeRenderer.Placed> nested = draw(byId.get("fp91-012")).getChildren();
		assertEquals(1, nested.size());
		assertEquals(72, nested.get(0).getBox().getWidth(), 1e-9);
		assertEquals(108, nested.get(0).getBox().getHeight(), 1e-9);
		assertEquals(5, nested.get(0).getRotation(), 1e-9);
		assertTrue(nested.get(0).isFlipH());
		// a canvas's shape with text, in the canvas's coordinates
		List<ShapeRenderer.Placed> canvas = draw(byId.get("fp92-008")).getChildren();
		assertEquals(1, canvas.size());
		assertTrue(canvas.get(0).getChild() instanceof CTWordprocessingShape);
		assertEquals(18, canvas.get(0).getBox().getX(), 1e-9);
		assertEquals(13.5, canvas.get(0).getBox().getY(), 1e-9);
		// a single shape has none
		assertTrue(draw(byId.get("fp90-001")).getChildren().isEmpty());
	}

	/** Word's draws, for a probe's regions; also the area Word paints, to see a test isn't empty */
	static Area wordArea(Map<String, Object> p) {
		return ShapeRendererTest.word(withoutText(p));
	}
}
