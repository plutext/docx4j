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
package org.docx4j.samples;

import java.io.File;
import java.util.Arrays;

import org.docx4j.model.chart.ChartDataEditor;
import org.docx4j.model.chart.ChartRef;
import org.docx4j.model.chart.ChartTable;
import org.docx4j.model.chart.Charts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.DrawingML.Chart;

/**
 * A chart in a Word document filled with new data, as a template's chart is (CR-036): its
 * embedded workbook's cells, its table, the chart's references and its caches (what Word draws
 * until Edit Data is used) all kept in step by {@link ChartDataEditor}, as Word's own edits keep
 * them.  setData replaces the categories, the series and their values at once, adding or removing
 * series and categories as needed; setValue, setCategory, setSeriesName, addCategory, addSeries
 * and their removals change one thing.  The workbook is written back when the editor is closed,
 * and with the document whenever that is saved.
 */
public class EditChartData {

	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/chart.docx";
		String outputfilepath = System.getProperty("user.dir") + "/OUT_EditChartData.docx";

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new File(inputfilepath));

		ChartRef chart = Charts.in(wordMLPackage).get(0);
		System.out.println("Before:\n" + table(chart));

		try (ChartDataEditor editor = ChartDataEditor.of((Chart) chart.getChartPart())) {
			editor.setData(
					Arrays.asList("North", "South", "East"),
					Arrays.asList("Plan", "Actual"),
					new Double[][] { { 1.5, 3.5, 2.0 }, { 2.5, 4.5, null } });   // null: an empty cell
		}
		System.out.println("After:\n" + table(chart));

		wordMLPackage.save(new File(outputfilepath));
		System.out.println("Saved " + outputfilepath);
	}

	/** The chart's data as a table, as docx4j's markdown output describes a chart */
	private static String table(ChartRef chart) throws Exception {
		ChartTable t = ChartTable.of(chart.read());
		StringBuilder sb = new StringBuilder(String.join(" | ", t.getHeader())).append('\n');
		for (java.util.List<String> row : t.getRows()) sb.append(String.join(" | ", row)).append('\n');
		return sb.toString();
	}
}
