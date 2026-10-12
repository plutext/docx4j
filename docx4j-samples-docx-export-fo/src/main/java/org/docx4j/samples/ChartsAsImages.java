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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Random;

import org.docx4j.Docx4J;
import org.docx4j.model.chart.ChartCreator;
import org.docx4j.model.chart.ChartDefinition;
import org.docx4j.model.chart.ChartImages;
import org.docx4j.model.chart.ChartRef;
import org.docx4j.model.chart.ChartRenderCheck;
import org.docx4j.model.chart.ChartType;
import org.docx4j.model.chart.Charts;
import org.docx4j.model.chart.LegendPosition;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Each chart in a docx, pptx or xlsx as SVG and PNG, drawn as Office draws it (CR-037): give the
 * file as the argument, or the sample makes a document with two charts (one drawn, one with data
 * labels, which this version's renderer doesn't draw yet: it says why).  A docx's PDF has its
 * charts drawn too, since docx4j-export-fo brings the renderer, docx4j-chart-render.
 */
public class ChartsAsImages {

	public static void main(String[] args) throws Exception {

		String dir = System.getProperty("user.dir");
		OpcPackage pkg;
		if (args.length > 0) {
			pkg = OpcPackage.load(new File(args[0]));
		} else {
			WordprocessingMLPackage doc = WordprocessingMLPackage.createPackage();
			doc.getMainDocumentPart().addParagraphOfText("Sales by region");
			ChartCreator creator = new ChartCreator(new Random(1));
			creator.inNewParagraph(doc, ChartDefinition.of(ChartType.COLUMN_CLUSTERED)
					.categories("North", "South", "East", "West")
					.series("2025", 1.5, 3.5, 2.0, 4.0)
					.series("2026", 2.5, 4.5, 3.0, 3.5)
					.title("Sales")
					.legend(LegendPosition.BOTTOM));
			creator.inNewParagraph(doc, ChartDefinition.of(ChartType.PIE)
					.categories("North", "South", "East", "West")
					.series("2026", 2.5, 4.5, 3.0, 3.5)
					.dataLabels(true));
			pkg = doc;
		}

		int n = 0;
		for (ChartRef chart : Charts.in(pkg)) {
			n++;
			ChartRenderCheck check = ChartImages.check(chart);
			System.out.println(chart.getName() + ": " + check);
			if (!check.isDrawn()) continue;
			try (OutputStream out = new FileOutputStream(new File(dir, "OUT_ChartsAsImages-" + n + ".svg"))) {
				ChartImages.writeSvg(chart, out);
			}
			try (OutputStream out = new FileOutputStream(new File(dir, "OUT_ChartsAsImages-" + n + ".png"))) {
				ChartImages.writePng(chart, 192, out);
			}
			System.out.println("  saved OUT_ChartsAsImages-" + n + ".svg and .png, "
					+ ChartImages.getSize(chart).getWidth() + " x " + ChartImages.getSize(chart).getHeight() + "pt");
		}

		if (pkg instanceof WordprocessingMLPackage) {
			File pdf = new File(dir, "OUT_ChartsAsImages.pdf");
			try (OutputStream out = new FileOutputStream(pdf)) {
				Docx4J.toPDF((WordprocessingMLPackage) pkg, out);
			}
			System.out.println("Saved " + pdf);
		}
	}
}
