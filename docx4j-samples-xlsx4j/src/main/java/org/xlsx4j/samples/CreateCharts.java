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
package org.xlsx4j.samples;

import java.io.File;
import java.time.LocalDate;

import org.docx4j.model.chart.ChartCreator;
import org.docx4j.model.chart.ChartDefinition;
import org.docx4j.model.chart.ChartRef;
import org.docx4j.model.chart.ChartSettings;
import org.docx4j.model.chart.ChartType;
import org.docx4j.model.chart.LegendPosition;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.xlsx4j.model.AreaRef;
import org.xlsx4j.model.CellRef;
import org.xlsx4j.model.SheetCells;

/**
 * Charts made in a workbook as Excel's Insert Chart makes them (CR-036): from data, which
 * ChartCreator writes into the sheet as a table, and over cells already in the sheet, read as
 * Excel reads a selection.  Each chart is Excel's own chart of its type, its series, points,
 * colours and axes made for the data as Excel makes them; its title, legend, axis titles, number
 * format and data labels are set by ChartSettings, here or on any chart later.  A seeded Random
 * makes the same file each time (Office's ids are otherwise random).
 */
public class CreateCharts {

	public static void main(String[] args) throws Exception {

		String outputfilepath = System.getProperty("user.dir") + "/OUT_CreateCharts.xlsx";

		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.createPackage();
		WorksheetPart sheet = pkg.createWorksheetPart(new PartName("/xl/worksheets/sheet1.xml"), "Sheet1", 1);
		ChartCreator creator = new ChartCreator(new java.util.Random(1));

		// 1. From data: written at A1 as a table, the chart placed over F1:M15
		ChartDefinition sales = ChartDefinition.of(ChartType.COLUMN_CLUSTERED)
				.categories("North", "South", "East", "West")
				.series("2025", 1.5, 3.5, 2.0, 4.0)
				.series("2026", 2.5, 4.5, 3.0, 3.5)
				.title("Sales")
				.legend(LegendPosition.BOTTOM)
				.valueAxis(a -> a.title("Units").numberFormat("0.0"));
		creator.inSheet(sheet, sales, new CellRef(null, 1, 1), AreaRef.parse("F1:M15"));

		// 2. Dates as categories: a date axis, as Excel's over dates
		ChartDefinition daily = ChartDefinition.of(ChartType.LINE_MARKERS)
				.dates("d-mmm", LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 6), LocalDate.of(2026, 1, 7))
				.series("Visits", 120, 135, 128)
				.dataLabels(true);
		creator.inSheet(sheet, daily, new CellRef(null, 1, 18), AreaRef.parse("F18:M32"));

		// 3. Over cells already there (no table made): a blank top-left cell, names across, regions down
		SheetCells cells = new SheetCells(pkg, sheet, "Sheet1");
		String[] regions = { "North", "South", "East" };
		cells.setString(2, 35, "Plan");
		cells.setString(3, 35, "Actual");
		for (int i = 0; i < regions.length; i++) {
			cells.setString(1, 36 + i, regions[i]);
			cells.setNumber(2, 36 + i, 10 + i);
			cells.setNumber(3, 36 + i, 12 + i);
		}
		cells.tidy();
		ChartRef pie = creator.fromCells(sheet, AreaRef.parse("Sheet1!A35:B38"), ChartType.PIE, AreaRef.parse("F35:M49"));

		// 4. Settings on an existing chart
		ChartSettings.setTitle((Chart) pie.getChartPart(), "Plan by region");
		ChartSettings.setDataLabels((Chart) pie.getChartPart(), true);

		pkg.save(new File(outputfilepath));
		System.out.println("Saved " + outputfilepath);
	}
}
