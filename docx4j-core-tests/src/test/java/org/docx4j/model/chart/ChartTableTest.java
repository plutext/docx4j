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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * A chart described as a table (CR-035 phase 3): the probes' data exactly as the probes were
 * made (excel-chart-probes.ps1, Word's default chart data), formatted as Excel formats it; and
 * the HTML table the property adds.
 */
public class ChartTableTest {

	private static ChartTable table(String fixture) throws Exception {
		ChartRef r = ChartsTest.refs(fixture).get(0);
		return r.isChartEx() ? ChartTable.of(r.readChartEx()) : ChartTable.of(r.read());
	}

	private static void row(ChartTable t, int i, String... cells) {
		assertEquals(Arrays.asList(cells), t.getRows().get(i));
	}

	@Test
	public void categoriesDownSeriesAcross() throws Exception {
		ChartTable t = table("clustered-column.xlsx");
		assertEquals("Chart Title", t.getTitle());
		assertEquals(Arrays.asList("", "Series 1", "Series 2", "Series 3"), t.getHeader());
		row(t, 0, "Category 1", "4.3", "2.4", "2");
		row(t, 1, "Category 2", "2.5", "4.4", "2");   // 4.4000000000000004 in the cache
		assertEquals(4, t.getRows().size());
	}

	@Test
	public void gapsAndErrors() throws Exception {
		ChartTable t = table("p2-gaps.xlsx");
		row(t, 1, "Category 2", "", "4.4", "2");
		row(t, 2, "Category 3", "3.5", "#N/A", "3");
	}

	@Test
	public void datesFormattedByTheirFormatCodes() throws Exception {
		row(table("p2-date-axis.xlsx"), 0, "1-Jan", "120");
		ChartTable stock = table("p2-stock.xlsx");
		assertEquals(Arrays.asList("", "High", "Low", "Close"), stock.getHeader());
		row(stock, 0, "2026-01-01", "55", "11", "25");
	}

	@Test
	public void multiLevelCategoriesAColumnEach() throws Exception {
		ChartTable t = table("p2-multilevel.xlsx");
		assertEquals(Arrays.asList("", "", "Sales"), t.getHeader());
		row(t, 0, "North", "Q1", "4.3");
		row(t, 1, "", "Q2", "2.5");
		row(t, 2, "South", "Q1", "3.5");
	}

	@Test
	public void scatterSharesItsX() throws Exception {
		ChartTable t = table("p2-scatter.xlsx");
		assertEquals(Arrays.asList("", "Y"), t.getHeader());
		row(t, 0, "0.7", "2.7");
		assertEquals(5, t.getRows().size());
	}

	/** What Office shows: a filtered series and category left out; a pie's first series */
	@Test
	public void onlyWhatOfficeShows() throws Exception {
		ChartTable f = table("p2-filtered.xlsx");
		assertEquals(Arrays.asList("", "Series 1", "Series 3"), f.getHeader());
		assertEquals(3, f.getRows().size());
		row(f, 2, "Category 4", "4.5", "5");
		ChartTable pie = table("pie-chart.xlsx");
		assertEquals(2, pie.getHeader().size());
	}

	@Test
	public void combinationChartAllItsSeries() throws Exception {
		ChartTable t = table("p2-combo-secondary.xlsx");
		assertEquals(Arrays.asList("", "Series 1", "Series 2", "Share"), t.getHeader());
		row(t, 3, "Category 4", "4.5", "2.8", "0.62");
	}

	@Test
	public void chartex() throws Exception {
		ChartTable t = table("waterfall.docx");
		assertEquals("Chart Title", t.getTitle());
		assertEquals(Arrays.asList("", "Series1"), t.getHeader());
		row(t, 3, "Category 4", "-40");
		assertEquals(8, t.getRows().size());
	}

	/** D9: the box only by default; with the property, the data as a table after it */
	@Test
	public void htmlDataTableOnTheProperty() throws Exception {
		String was = Docx4jProperties.getProperty(ChartBox.HTML_DATA_TABLE, "false");
		try {
			for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
				Docx4jProperties.setProperty(ChartBox.HTML_DATA_TABLE, "false");
				assertTrue(!html("pie-chart.docx", flags).contains("docx4j-chart-data"));
				Docx4jProperties.setProperty(ChartBox.HTML_DATA_TABLE, "true");
				String html = html("pie-chart.docx", flags);
				int t = html.indexOf("<table class=\"docx4j-chart-data\"");
				assertTrue(flags + ": " + html, t > 0);
				String table = html.substring(t, html.indexOf("</table>", t));
				assertTrue(table, table.contains("<caption>Sales</caption>"));
				assertTrue(table, table.contains("<td>1st Qtr</td><td>8.2</td>"));
			}
		} finally {
			Docx4jProperties.setProperty(ChartBox.HTML_DATA_TABLE, was);
		}
	}

	private static String html(String fixture, int flags) throws Exception {
		WordprocessingMLPackage pkg;
		try (InputStream is = ChartTableTest.class.getClassLoader().getResourceAsStream("charts/" + fixture)) {
			pkg = WordprocessingMLPackage.load(is);
		}
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, os, flags);
		return os.toString("UTF-8");
	}
}
