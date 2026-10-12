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

import org.docx4j.model.chart.ChartFonts;
import org.docx4j.model.chart.ChartModel;
import org.docx4j.model.chart.ChartRef;
import org.docx4j.model.chart.Charts;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.junit.Test;

/**
 * CR-037's gate (§5, D4): each render probe the renderer draws, laid out and drawn against
 * Office's (the oracle in charts/render/, README there): Excel's layout, Word's PDF.  Prints the
 * scoreboard; every probe drawn must be within tolerance in every element, but those
 * {@link #OPEN}, each with what is not yet Office's.
 */
public class ChartRenderGateTest {

	static final String DIR = "charts/render/";

	/** The probes drawn but not yet within tolerance, and why (CR-037 §11) */
	static final Map<String, String> OPEN = new LinkedHashMap<>();
	static {
		for (String p : new String[] { "r3-default-180x108", "r3-eight-180x108", "r3-eight-288x168", "r11-default-h72",
				"r11-default-h90", "r11-eight-h72", "r11-eight-h90", "r11-text14-h108" }) {
			OPEN.put(p, "a small plot: Office caps the category labels' band, overlapping, cutting short or turning them (R12)");
		}
		OPEN.put("r2-max-set", "a maximum set, the labels hanging from a zero line near the bottom: the inside 0.55pt shorter");
	}

	static String text(String path) throws IOException {
		try (InputStream is = ChartRenderGateTest.class.getClassLoader().getResourceAsStream(DIR + path)) {
			if (is == null) return null;
			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	static ChartRef chart(String probe) throws Exception {
		try (InputStream is = ChartRenderGateTest.class.getClassLoader().getResourceAsStream(DIR + "probes/" + probe + ".xlsx")) {
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
			if (dump == null || trace == null) continue;
			results.add(RenderGate.compare(r[0], d, Json.object(Json.parse(dump)), Json.object(Json.parse(trace))));
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
