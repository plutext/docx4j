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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.Docx4jProperties;
import org.docx4j.model.chart.ChartFonts;
import org.docx4j.model.chart.ChartModel;
import org.docx4j.model.chart.ChartRef;
import org.docx4j.model.chart.Charts;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.junit.Test;

/**
 * CR-037's gate (§5, D4): each render probe the renderer draws, laid out and drawn against
 * Office's (the oracle in charts/render/, README there): Excel's layout, Word's PDF (Excel's own
 * charts, which have no docx: the layout alone); R1's pptx against PowerPoint's layout and PDF;
 * Word's and PowerPoint's own charts (office/) against their PDFs.  Prints the
 * scoreboard; every probe drawn must be within tolerance in every element, but those
 * {@link #OPEN}, each with what is not yet Office's.
 */
public class ChartRenderGateTest {

	static final String DIR = "charts/render/";

	/** The probes drawn but not yet within tolerance, and why (CR-037 §11); R1's pptx as name.pptx */
	static final Map<String, String> OPEN = new LinkedHashMap<>();
	static {
		for (String p : new String[] { "r11-default-h72", "r11-eight-h72" }) {
			OPEN.put(p, "a plot 1pt high: the inside is Office's (nothing), but Word's PDF has each column, nothing high, as a 0.24pt sliver"
					+ " at the inside's top, and its gridline; ours draws none");
		}
		OPEN.put("r2-max-set", "a maximum set, the labels hanging from a zero line near the bottom: the inside 0.55pt shorter");
		for (String p : new String[] { "r1-scatter", "r1-scatter.pptx", "r14-scatter-lines", "r14-scatter-smooth", "r14-sweep-scatter" }) {
			OPEN.put(p, "the title \"Y-Values\": Excel kerns its \"Va\" but not its \"Y-\" in Calibri (in Aptos Narrow, p1-scatter, both),"
					+ " though Calibri's own tables have the pair as Carlito's do: the box 0.4pt narrower than Office's");
		}
		for (String p : new String[] { "r14-markers-symbols", "r14-markers-sizes" }) {
			OPEN.put(p, "Word's PDF puts markers on its 0.24pt grid, flooring a circle's or square's box and rounding a diamond's: up to"
					+ " 0.48pt from the marker at its size on its point (PowerPoint's PDF has them at their size, P4)");
		}
	}

	static {
		// Office shows number format 14 (m/d/yyyy) in the machine's short date: the VM's is d/mm/yyyy
		Docx4jProperties.setProperty("docx4j.chart.render.shortDate", "d/mm/yyyy");
	}

	static String text(String path) throws IOException {
		try (InputStream is = ChartRenderGateTest.class.getClassLoader().getResourceAsStream(DIR + path)) {
			if (is == null) return null;
			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	static ChartRef chart(String probe) throws Exception {
		return chart(probe, ".xlsx");
	}

	static ChartRef chart(String probe, String ext) throws Exception {
		try (InputStream is = ChartRenderGateTest.class.getClassLoader().getResourceAsStream(DIR + "probes/" + probe + ext)) {
			return Charts.in(OpcPackage.load(is)).get(0);
		}
	}

	/** Each drawn probe's result, against Excel's layout and Word's PDF */
	static List<RenderGate.Result> run() throws Exception {
		ClassicChartRenderer renderer = new ClassicChartRenderer();
		List<RenderGate.Result> results = new ArrayList<>();
		String[] rows = text("probes.tsv").split("\r?\n");
		for (int i = 1; i < rows.length; i++) {
			String[] r = rows[i].split("\t", -1);
			if (!r[6].isEmpty()) continue; // R10's styles are phase 4's
			ChartRef ref = chart(r[0]);
			ChartModel m = ref.read();
			if (!renderer.check(m, ref.getHostTheme()).isDrawn()) continue;
			double w = Double.parseDouble(r[2]), h = Double.parseDouble(r[3]);
			Drawing d = renderer.layout(m, ref.getHostTheme(), ChartFonts.of(ref.getHost()), w, h);
			String dump = text("excel-layout/" + r[0] + ".json"), trace = text("word-pdf/" + r[0] + ".json");
			// Excel's own charts (R14's copies of CR-036's) have no docx: their layout alone
			boolean xlsxOnly = "xlsx".equals(r[4]);
			if (dump == null || (trace == null && !xlsxOnly)) continue;
			results.add(RenderGate.compare(r[0], d, Json.object(Json.parse(dump)), trace == null ? null : Json.object(Json.parse(trace))));
			// R1's pptx: PowerPoint's chart (its template's: no title, larger text), its layout and PDF
			String pptDump = text("powerpoint-layout/" + r[0] + ".json"), pptTrace = text("powerpoint-pdf/" + r[0] + ".json");
			if (r[4].contains("pptx") && pptDump != null && pptTrace != null) {
				ChartRef p = chart(r[0], ".pptx");
				ChartModel pm = p.read();
				if (!renderer.check(pm, p.getHostTheme()).isDrawn()) continue;
				Drawing pd = renderer.layout(pm, p.getHostTheme(), ChartFonts.of(p.getHost()), w, h);
				results.add(RenderGate.compare(r[0] + ".pptx", pd, Json.object(Json.parse(pptDump)), Json.object(Json.parse(pptTrace))));
			}
		}
		// Office's own charts (CR-036's P3, Word's Insert Chart, and P4, PowerPoint's): the drawing
		// against their PDFs (they have no layout dump)
		String[] office = text("office/office.tsv").split("\r?\n");
		for (int i = 1; i < office.length; i++) {
			String[] r = office[i].split("\t", -1);
			ChartRef ref;
			try (InputStream is = ChartRenderGateTest.class.getClassLoader().getResourceAsStream(DIR + "office/" + r[1])) {
				ref = Charts.in(OpcPackage.load(is)).get(0);
			}
			ChartModel m = ref.read();
			if (!renderer.check(m, ref.getHostTheme()).isDrawn()) continue;
			Drawing d = renderer.layout(m, ref.getHostTheme(), ChartFonts.of(ref.getHost()), Double.parseDouble(r[2]), Double.parseDouble(r[3]));
			String trace = text("office/" + (r[1].endsWith(".pptx") ? "powerpoint-pdf/" : "word-pdf/") + r[0] + ".json");
			results.add(RenderGate.drawing(r[0], d, Json.object(Json.parse(trace))));
		}
		return results;
	}

	@Test
	public void gate() throws Exception {
		List<RenderGate.Result> results = run();
		int checked = 0, within = 0, passing = 0;
		List<String> failed = new ArrayList<>();
		for (RenderGate.Result r : results) {
			System.out.println(r);
			checked += r.checked;
			within += r.within;
			if (r.passes()) passing++;
			if (!OPEN.containsKey(r.probe) && !r.passes()) failed.add(r.toString());
			if (OPEN.containsKey(r.probe) && r.passes()) failed.add(r.probe + " passes now: take it off OPEN");
		}
		System.out.println(String.format("CR-037 gate: %d probes drawn, %d within tolerance in every element; elements %d/%d (%.1f%%)",
				results.size(), passing, within, checked, checked == 0 ? 0 : 100.0 * within / checked));
		assertTrue(String.join("\n", failed), failed.isEmpty());
	}

	public static void main(String[] args) throws Exception {
		new ChartRenderGateTest().gate();
	}
}
