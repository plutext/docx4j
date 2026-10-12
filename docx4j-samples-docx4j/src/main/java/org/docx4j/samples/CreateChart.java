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
import java.time.LocalDate;
import java.util.Random;

import org.docx4j.model.chart.ChartCreator;
import org.docx4j.model.chart.ChartDataEditor;
import org.docx4j.model.chart.ChartDefinition;
import org.docx4j.model.chart.ChartRef;
import org.docx4j.model.chart.ChartSettings;
import org.docx4j.model.chart.ChartType;
import org.docx4j.model.chart.LegendPosition;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.wml.P;
import org.docx4j.wml.R;

/**
 * Charts made in a document as Word's Insert Chart makes them (CR-036): each chart Word's own of
 * its type, its data in a workbook embedded with it (Word's own, the data in its table), so that
 * Word's Edit Data opens the data.  A chart in a new paragraph, or as a run to place yourself;
 * its data edited afterwards with ChartDataEditor, its settings with ChartSettings.  A seeded
 * Random makes the same file each time (Office's ids are otherwise random).
 */
public class CreateChart {

	public static void main(String[] args) throws Exception {

		String outputfilepath = System.getProperty("user.dir") + "/OUT_CreateChart.docx";

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		wordMLPackage.getMainDocumentPart().addParagraphOfText("Sales by region");
		ChartCreator creator = new ChartCreator(new Random(1));

		// 1. In a new paragraph, Word's size (6 by 3.5 inches)
		ChartRef sales = creator.inNewParagraph(wordMLPackage, ChartDefinition.of(ChartType.COLUMN_CLUSTERED)
				.categories("North", "South", "East", "West")
				.series("2025", 1.5, 3.5, 2.0, 4.0)
				.series("2026", 2.5, 4.5, 3.0, 3.5)
				.title("Sales")
				.legend(LegendPosition.BOTTOM)
				.valueAxis(a -> a.title("Units").numberFormat("0.0")));

		// 2. A run to place: here after some text, 4 by 2.5 inches, dates as the categories
		wordMLPackage.getMainDocumentPart().addParagraphOfText("Visits by day");
		R run = creator.inDocument(wordMLPackage, ChartDefinition.of(ChartType.LINE_MARKERS)
				.dates("d-mmm", LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 6), LocalDate.of(2026, 1, 7))
				.series("Visits", 120, 135, 128)
				.dataLabels(true), 3657600, 2286000);
		P p = new P();
		p.getContent().add(run);
		wordMLPackage.getMainDocumentPart().getContent().add(p);

		// 3. The first chart's data edited, and a setting changed, as for any chart
		try (ChartDataEditor editor = ChartDataEditor.of((Chart) sales.getChartPart())) {
			editor.addCategory("Central", 3.0, 3.25);
		}
		ChartSettings.setTitle((Chart) sales.getChartPart(), "Sales by region");

		wordMLPackage.save(new File(outputfilepath));
		System.out.println("Saved " + outputfilepath);
	}
}
