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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.model.chart.ChartColorStyles;
import org.docx4j.model.chart.ChartFill;
import org.docx4j.model.chart.ChartGroup;
import org.docx4j.model.chart.ChartModel;
import org.docx4j.model.chart.ChartRef;
import org.docx4j.model.chart.ChartSeries;
import org.docx4j.model.chart.Charts;
import org.docx4j.model.color.DrawingMLColors;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.junit.Test;

/**
 * CR-037 phase 0's oracle (charts/render/, README there): every probe has its files, and the read
 * model (CR-035) agrees with what Office reported of each chart: the title Office shows (its
 * text, or none), whether there is a legend, how many series, and the first series' colour as
 * CR-034 resolves it in the probe's theme (a pie's first point's), or where it has none of its own
 * the colour Office shows for it (ChartColorStyles, R14).  The geometry is phase 1's.
 */
public class ChartRenderFixturesTest {

	private static final String DIR = "charts/render/";

	private static String text(String path) throws IOException {
		try (InputStream is = ChartRenderFixturesTest.class.getClassLoader().getResourceAsStream(DIR + path)) {
			assertNotNull("no " + DIR + path, is);
			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static ChartRef ref(String path) throws Exception {
		try (InputStream is = ChartRenderFixturesTest.class.getClassLoader().getResourceAsStream(DIR + path)) {
			assertNotNull("no " + DIR + path, is);
			List<ChartRef> refs = Charts.in(OpcPackage.load(is));
			assertEquals(path + " charts", 1, refs.size());
			return refs.get(0);
		}
	}

	/** A dump's top-level value (one a line, as chart_score.py fixtures writes them), or a field of it */
	private static String field(String json, String key, String sub) {
		for (String line : json.split("\n")) {
			if (!line.startsWith(" \"" + key + "\":")) continue;
			if (sub == null) return line.substring(line.indexOf(':') + 1).replaceAll(",$", "").trim();
			Matcher m = Pattern.compile("\"" + sub + "\":\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(line);
			return m.find() ? m.group(1) : null;
		}
		return null;
	}

	private static final Pattern FIRST_POINT_FILL = Pattern.compile("\"points\":\\[\\{[^}]*?\"fill\":\"([0-9A-F]{6})\"");

	/** What disagrees between the model and Office's dump of the chart */
	private static List<String> compare(ChartRef ref, String json) throws Exception {
		ChartModel m = ref.read();
		List<String> bad = new ArrayList<>();
		String title = "true".equals(field(json, "hasTitle", null)) ? field(json, "title", "text") : null;
		// (Excel's paragraphs end in a carriage return, the model's in a line feed: r15-title-paragraphs)
		if (title != null) title = title.replace("\\r", "\n").replace("\r", "\n");
		String shown = m.getTitle() == null ? null : m.getTitle().getShownText();
		if (!Objects.equals(title, shown)) bad.add("title: Office " + title + ", the model " + shown);
		boolean legend = "true".equals(field(json, "hasLegend", null));
		if (legend != (m.getLegend() != null)) bad.add("legend: Office " + legend + ", the model " + (m.getLegend() != null));
		String series = field(json, "series", null);
		int n = 0;
		// (a series filtered out isn't in Excel's collection: c35-filtered)
		for (ChartGroup g : m.getGroups()) for (ChartSeries s : g.getSeries()) if (!s.isHidden()) n++;
		int office = series == null ? 0 : series.split("\"index\":", -1).length - 1;
		if (n != office) bad.add("series: Office " + office + ", the model " + n);
		Matcher f = FIRST_POINT_FILL.matcher(series == null ? "" : series);
		// Office reports a line's point as black: it has no fill of its own
		if (f.find() && !"000000".equals(f.group(1))) {
			ChartGroup g = m.getGroups().get(0);
			ChartSeries s = g.getSeries().get(0);
			// the series' own fill, else (a pie's) its first point's
			ChartFill fill = s.getFill();
			boolean byPoint = false;
			if ((fill == null || fill.getKind() == ChartFill.Kind.AUTOMATIC || fill.getKind() == ChartFill.Kind.NONE)
					&& s.getPointFills().get(0) != null) {
				fill = s.getPointFills().get(0);
				byPoint = true;
			}
			// a gradient's or pattern's (Office reports one colour, the model none: the renderer draws them)
			if (fill == null || (fill.getKind() != ChartFill.Kind.GRADIENT && fill.getKind() != ChartFill.Kind.PATTERN)) {
				String hex = fill == null || fill.getColor() == null ? null : fill.getColor().toHex();
				// none of its own: the colour Office shows for its index (R14's automatic colours)
				if (hex == null && (fill == null || fill.getKind() == ChartFill.Kind.AUTOMATIC)) {
					boolean points = byPoint || g.getKind() == ChartGroup.Kind.PIE || g.getKind() == ChartGroup.Kind.DOUGHNUT || g.variesColors();
					int count = 0;
					for (ChartGroup each : m.getGroups()) count += each.getSeries().size();
					Object c = points ? ChartColorStyles.automatic(0, s.getValues() == null ? 0 : s.getValues().size())
							: ChartColorStyles.automatic(s.getIdx(), count);
					hex = DrawingMLColors.resolve(c, ref.getHostTheme()).toHex();
				}
				if (!f.group(1).equals(hex)) bad.add("fill: Office " + f.group(1) + ", the model " + hex);
			}
		}
		return bad;
	}

	@Test
	public void everyProbeAgrees() throws Exception {
		String[] rows = text("probes.tsv").split("\r?\n");
		assertEquals("name\tset\twidth\theight\thosts\ttheme\tstyle\tdocx_at\tpptx_at\tnote\tsweep", rows[0]);
		List<String> failures = new ArrayList<>();
		int pptx = 0;
		for (int i = 1; i < rows.length; i++) {
			String[] r = rows[i].split("\t", -1);
			String name = r[0];
			// the drawing's oracle is there (Excel's own charts, R14's copies, are xlsx only)
			if (r[4].contains("docx")) assertTrue(name, text("word-pdf/" + name + ".json").contains("\"texts\""));
			// a swept probe's sizes
			if (!r[10].isEmpty()) assertTrue(name, text("excel-layout/" + name + ".sweep.json").contains("\"inside"));
			// an R10 probe as Excel styled it, which its dump is of
			String xlsx = r[6].isEmpty() ? name + ".xlsx" : name + "-styled.xlsx";
			for (String b : compare(ref("probes/" + xlsx), text("excel-layout/" + name + ".json"))) failures.add(name + " " + b);
			if (r[4].contains("pptx")) {
				pptx++;
				assertTrue(name, text("powerpoint-pdf/" + name + ".json").contains("\"texts\""));
				for (String b : compare(ref("probes/" + name + ".pptx"), text("powerpoint-layout/" + name + ".json"))) {
					failures.add(name + ".pptx " + b);
				}
			}
		}
		assertEquals(543, rows.length - 1);
		assertEquals(12, pptx);
		assertTrue(String.join("\n", failures), failures.isEmpty());
	}
}
