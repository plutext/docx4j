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

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.docx4j.chart.render.api.Drawing;
import org.docx4j.model.chart.ChartFonts;
import org.docx4j.model.chart.ChartModel;
import org.docx4j.model.chart.ChartRef;
import org.junit.Test;

/**
 * CR-037 phase 2's sweeps (R14): Excel resized each swept probe step by step and read its axes
 * and the plot's inside at each size (excel-layout/name.sweep.json); laid out at each size, the
 * renderer's axes must be Excel's and its inside within D4's geometry tolerance.
 */
public class ChartSweepTest {

	/** The sizes not yet Excel's (probe at size), and why */
	static final Map<String, String> OPEN = new java.util.LinkedHashMap<>();
	static {
		for (String p : new String[] { "r14-sweep-bar-text-12", "r14-sweep-bar-x100-text-12" }) {
			for (String size : new String[] { "120", "123" }) {
				OPEN.put(p + " at " + size, "the narrowest, the legend in two rows: Office's inside 0.55 and 0.93pt lower at the top; not yet explained");
			}
		}
	}

	/** Each sweep's rows that disagree, as text */
	static List<String> run(boolean verbose) throws Exception {
		ClassicChartRenderer renderer = new ClassicChartRenderer();
		List<String> bad = new ArrayList<>();
		String[] rows = ChartRenderGateTest.text("probes.tsv").split("\r?\n");
		for (int i = 1; i < rows.length; i++) {
			String[] r = rows[i].split("\t", -1);
			if (r.length < 11 || r[10].isEmpty()) continue;
			ChartRef ref = ChartRenderGateTest.chart(r[0]);
			ChartModel m = ref.read();
			boolean width = r[10].startsWith("width");
			double w = Double.parseDouble(r[2]), h = Double.parseDouble(r[3]);
			Map<String, Object> sweep = Json.object(Json.parse(ChartRenderGateTest.text("excel-layout/" + r[0] + ".sweep.json")));
			int n = 0, ok = 0;
			for (Object o : Json.list(sweep.get("rows"))) {
				Map<String, Object> row = Json.object(o);
				double size = Json.number(row.get("size"));
				Drawing d = renderer.layout(m, ref.getHostTheme(), ChartFonts.of(ref.getHost()), width ? size : w, width ? h : size);
				List<String> why = new ArrayList<>();
				Rectangle2D in = d.getBoxes().get("inside");
				double[] office = { Json.number(row.get("insideLeft")) + RenderGate.ORIGIN, Json.number(row.get("insideTop")) + RenderGate.ORIGIN,
						Json.number(row.get("insideWidth")), Json.number(row.get("insideHeight")) };
				if (in == null) {
					why.add("no inside");
				} else {
					double e = Math.max(Math.max(Math.abs(in.getX() - office[0]), Math.abs(in.getY() - office[1])),
							Math.max(Math.abs(in.getWidth() - office[2]), Math.abs(in.getHeight() - office[3])));
					if (e > RenderGate.GEOMETRY) {
						why.add(String.format("inside off by %.2f (Office %.2f,%.2f %.2fx%.2f, ours %.2f,%.2f %.2fx%.2f)", e, office[0], office[1],
								office[2], office[3], in.getX(), in.getY(), in.getWidth(), in.getHeight()));
					}
				}
				for (Object ao : Json.list(row.get("axes"))) {
					Map<String, Object> a = Json.object(ao);
					if (a.get("min") == null) continue;
					boolean x = Json.number(a.get("type")) == 1;
					double[] ours = x ? d.getXAxis() : d.getValueAxis();
					double[] theirs = { Json.number(a.get("min")), Json.number(a.get("max")), Json.number(a.get("unit")) };
					boolean same = ours != null;
					for (int k = 0; same && k < 3; k++) same = Math.abs(theirs[k] - ours[k]) <= 1e-9 * Math.max(1, Math.abs(theirs[k]));
					if (!same) {
						why.add((x ? "x" : "value") + " axis: Excel " + theirs[0] + " to " + theirs[1] + " by " + theirs[2] + ", ours "
								+ (ours == null ? "none" : ours[0] + " to " + ours[1] + " by " + ours[2]));
					}
				}
				n++;
				String at = r[0] + " at " + (int) size;
				if (why.isEmpty()) {
					ok++;
					if (OPEN.containsKey(at)) bad.add(at + " is Excel's now: take it off OPEN");
				} else if (!OPEN.containsKey(at)) {
					bad.add(at + ": " + String.join("; ", why));
				}
			}
			if (verbose) System.out.println(String.format("%-28s %3d/%3d sizes", r[0], ok, n));
		}
		return bad;
	}

	@Test
	public void everySizeIsExcels() throws Exception {
		List<String> bad = run(true);
		assertTrue(bad.size() + " sizes disagree:\n" + String.join("\n", bad), bad.isEmpty());
	}

	public static void main(String[] args) throws Exception {
		List<String> bad = run(true);
		for (String b : bad) System.out.println(b);
	}
}
