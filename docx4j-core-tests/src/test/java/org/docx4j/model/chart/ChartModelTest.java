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
package org.docx4j.model.chart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.junit.Test;

/**
 * The chart read model (CR-035 phase 2) on the chart fixtures: what each reads as, against what
 * Office drew (Excel's and Word's renderings on the Office share, office_charts), and the JSON
 * goldens, which docx4j's ports compare against.
 *
 * <p>To write the goldens afresh (after a deliberate change to the model or its JSON form), run
 * this test with {@code -Ddocx4j.chartGoldens.write=<the charts/json directory of
 * src/test/resources>} and review the diff.</p>
 */
public class ChartModelTest {

	private static final String[] FIXTURES = { "bar-chart.docx", "bar-chart-style-2.docx", "bar-chart-style-2.xlsx",
			"bar-chart.xlsx", "clustered-column.docx", "clustered-column.xlsx", "line-chart.docx", "line-chart.xlsx",
			"pie-chart.docx", "pie-chart.xlsx", "stacked-column.docx", "stacked-column.xlsx", "waterfall.docx",
			"waterfall.pptx", "waterfall.xlsx", "p2-3d-column.xlsx", "p2-area.xlsx", "p2-bar-of-pie.xlsx",
			"p2-bubble.xlsx", "p2-chartsheet.xlsx", "p2-combo-secondary.xlsx", "p2-date-axis.xlsx", "p2-doughnut.xlsx",
			"p2-filtered.xlsx", "p2-gaps.xlsx", "p2-multilevel.xlsx", "p2-pasted.pptx", "p2-radar.xlsx",
			"p2-scatter.xlsx", "p2-stock.xlsx", "p2-surface.xlsx" };

	/** Each chart part of a fixture, by part name */
	static Map<String, Chart> charts(String fixture) throws Exception {
		OpcPackage pkg;
		try (InputStream is = ChartModelTest.class.getClassLoader().getResourceAsStream("charts/" + fixture)) {
			pkg = OpcPackage.load(is);
		}
		Map<String, Chart> m = new TreeMap<String, Chart>();
		for (Part p : pkg.getParts().getParts().values()) {
			if (p instanceof Chart) m.put(p.getPartName().getName(), (Chart) p);
		}
		return m;
	}

	static ChartModel model(String fixture) throws Exception {
		Map<String, Chart> m = charts(fixture);
		assertEquals(fixture + ": one chart", 1, m.size());
		return ChartModel.read(m.values().iterator().next(), null);
	}

	private static String golden(String fixture, String partName) {
		return fixture + partName.replace('/', '_') + ".json";
	}

	@Test
	public void jsonGoldens() throws Exception {
		String write = System.getProperty("docx4j.chartGoldens.write");
		List<String> missing = new ArrayList<String>();
		int n = 0;
		for (String fixture : FIXTURES) {
			for (Map.Entry<String, Chart> e : charts(fixture).entrySet()) {
				String json = ChartModel.read(e.getValue(), null).toJson();
				String name = golden(fixture, e.getKey());
				if (write != null) {
					Files.write(new File(write, name).toPath(), json.getBytes(StandardCharsets.UTF_8));
					continue;
				}
				try (InputStream is = getClass().getClassLoader().getResourceAsStream("charts/json/" + name)) {
					if (is == null) {
						missing.add(name);
						continue;
					}
					assertEquals(name, new String(is.readAllBytes(), StandardCharsets.UTF_8), json);
				}
				n++;
			}
		}
		assertTrue("no golden for " + missing, missing.isEmpty() || write != null);
		if (write == null) assertEquals("charts read", 29, n);
	}

	/** Titles as Excel and Word drew them (their PDFs and PNGs, office_charts) */
	@Test
	public void titlesAsOfficeShowsThem() throws Exception {
		// several series, a c:title with no text: "Chart Title"
		assertEquals("Chart Title", model("p2-area.xlsx").getTitle().getShownText());
		assertNull(model("p2-area.xlsx").getTitle().getText());
		// one series with a name: its name ([MS-OI29500] 2.1.1561)
		assertEquals("Series 1", model("p2-doughnut.xlsx").getTitle().getShownText());
		assertEquals("Sales", model("p2-multilevel.xlsx").getTitle().getShownText());
		assertEquals("Visits", model("p2-date-axis.xlsx").getTitle().getShownText());
		assertEquals("Sales", model("pie-chart.docx").getTitle().getShownText());
		// no c:title at all: none
		assertNull(model("p2-chartsheet.xlsx").getTitle());
		// axis titles with no text
		ChartModel bar = model("bar-chart.xlsx");
		for (ChartAxis a : bar.getAxes()) assertEquals("Axis Title", a.getTitle().getShownText());
	}

	/** Excel's chart filter: a series and a category filtered out (the p2-filtered probe) */
	@Test
	public void filteredSeriesAndCategories() throws Exception {
		ChartGroup g = model("p2-filtered.xlsx").getGroups().get(0);
		assertEquals(3, g.getSeries().size());
		assertEquals(2, g.getShownSeries().size());
		ChartSeries hidden = g.getSeries().get(1);
		assertTrue(hidden.isHidden());
		assertEquals(1, hidden.getIdx());
		assertEquals("Series 2", hidden.getShownName());
		assertEquals("Sheet1!$C$1", hidden.getName().getReference()); // c15:formulaRef
		ChartData values = g.getSeries().get(0).getValues();
		assertEquals("(Sheet1!$B$2:$B$3,Sheet1!$B$5)", values.getReference());
		assertEquals("Sheet1!$B$2:$B$5", values.getFullReference());
		assertEquals(Long.valueOf(3), values.getCount());
		assertEquals("Category 4", g.getSeries().get(0).getCategories().getText(2));
	}

	/** A blank cell is a gap (no point at its index); #N/A is kept as Office wrote it */
	@Test
	public void gaps() throws Exception {
		ChartGroup g = model("p2-gaps.xlsx").getGroups().get(0);
		ChartData s1 = g.getSeries().get(0).getValues();
		assertEquals(Long.valueOf(4), s1.getCount());
		assertNull(s1.getText(1));
		assertEquals(4.3, s1.getNumber(0), 0);
		ChartData s2 = g.getSeries().get(1).getValues();
		assertEquals("#N/A", s2.getText(2));
		assertNull(s2.getNumber(2));
		assertEquals("gap", g.getShownDispBlanksAs());
	}

	@Test
	public void multiLevelCategories() throws Exception {
		ChartData cats = model("p2-multilevel.xlsx").getGroups().get(0).getSeries().get(0).getCategories();
		assertEquals(ChartData.Kind.MULTI_LEVEL_REFERENCE, cats.getKind());
		assertEquals(2, cats.getLevels().size());
		assertEquals("Q2", cats.getLevels().get(0).get(1));   // the innermost level first
		assertEquals("South", cats.getLevels().get(1).get(2));
		assertNull(cats.getLevels().get(1).get(1));
	}

	/** Two groups; the line on a secondary value axis, at the right */
	@Test
	public void combinationWithSecondaryAxis() throws Exception {
		ChartModel m = model("p2-combo-secondary.xlsx");
		assertEquals(2, m.getGroups().size());
		ChartGroup line = m.getGroups().get(1);
		assertEquals(ChartGroup.Kind.LINE, line.getKind());
		assertEquals("Share", line.getSeries().get(0).getShownName());
		ChartAxis secondaryValue = null;
		for (Long id : line.getAxisIds()) {
			ChartAxis a = m.getAxis(id);
			assertNotNull(a);
			if (a.getKind() == ChartAxis.Kind.VALUE) secondaryValue = a;
			else assertTrue("its category axis is deleted", a.isDeleted());
		}
		assertEquals("r", secondaryValue.getPosition());
	}

	/** Colours: Excel's in the workbook's 2023 theme; Word's in the document's (charts plan, finding 7) */
	@Test
	public void coloursInTheHostsTheme() throws Exception {
		ChartSeries xlsx = model("clustered-column.xlsx").getGroups().get(0).getSeries().get(0);
		assertEquals(ChartFill.Kind.SOLID, xlsx.getFill().getKind());
		assertEquals("156082", xlsx.getFill().getColor().toHex());
		ChartSeries docx = model("clustered-column.docx").getGroups().get(0).getSeries().get(0);
		assertEquals("4472C4", docx.getFill().getColor().toHex());
	}

	/** varyColors: honoured with one series (the doughnut probe), ignored with several */
	@Test
	public void varyColors() throws Exception {
		assertTrue(model("p2-doughnut.xlsx").getGroups().get(0).variesColors());
		ChartGroup pie = model("pie-chart.xlsx").getGroups().get(0);
		assertTrue(pie.variesColors());
		assertFalse(model("p2-area.xlsx").getGroups().get(0).variesColors());
	}

	/** The style: c14:style, and c:style its value less 100 */
	@Test
	public void style() throws Exception {
		ChartModel m = model("bar-chart.xlsx");
		assertEquals(Integer.valueOf(102), m.getC14Style());
		assertEquals(Integer.valueOf(2), m.getStyle());
	}

	/** Data labels: the series' over the group's; bar-chart.xlsx's 0.0 format */
	@Test
	public void dataLabels() throws Exception {
		ChartSeries s = model("bar-chart.xlsx").getGroups().get(0).getSeries().get(0);
		assertEquals(Boolean.TRUE, s.getDataLabels().getShowValue());
		assertEquals("#,##0.0", s.getDataLabels().getNumberFormat());
		assertEquals("outEnd", s.getDataLabels().getShownPosition());
	}
}
