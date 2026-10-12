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
package org.pptx4j.samples;

import java.io.File;
import java.util.Random;

import org.docx4j.model.chart.ChartCreator;
import org.docx4j.model.chart.ChartDataEditor;
import org.docx4j.model.chart.ChartDefinition;
import org.docx4j.model.chart.ChartRef;
import org.docx4j.model.chart.ChartSettings;
import org.docx4j.model.chart.ChartType;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.pptx4j.pml.CTGraphicalObjectFrame;

/**
 * Charts made on a slide as PowerPoint's Insert Chart makes them (CR-036): each chart
 * PowerPoint's own of its type, its data in a workbook embedded with it, so that PowerPoint's
 * Edit Data opens the data.  Its data edited afterwards with ChartDataEditor, its settings with
 * ChartSettings.  A seeded Random makes the same file each time.
 */
public class CreateChart {

	public static void main(String[] args) throws Exception {

		String outputfilepath = System.getProperty("user.dir") + "/OUT_CreateChart.pptx";

		PresentationMLPackage presentationMLPackage = PresentationMLPackage.createPackage();
		MainPresentationPart pp = presentationMLPackage.getMainPresentationPart();
		SlideLayoutPart layout = (SlideLayoutPart) presentationMLPackage.getParts().getParts()
				.get(new PartName("/ppt/slideLayouts/slideLayout1.xml"));
		SlidePart slide = PresentationMLPackage.createSlidePart(pp, layout, new PartName("/ppt/slides/slide1.xml"));
		ChartCreator creator = new ChartCreator(new Random(1));

		// A chart of PowerPoint's size (6 by 3.5 inches), an inch from the slide's top left corner
		CTGraphicalObjectFrame frame = creator.inSlide(slide, ChartDefinition.of(ChartType.PIE)
				.categories("North", "South", "East", "West")
				.series("Sales", 8.2, 3.2, 1.4, 1.2)
				.title("Sales by region")
				.dataLabels(true), 914400, 914400, ChartCreator.DEFAULT_WIDTH, ChartCreator.DEFAULT_HEIGHT);

		// Its data edited, a setting changed, as for any chart
		Chart chart = (Chart) ChartRef.of(slide, frame).getChartPart();
		try (ChartDataEditor editor = ChartDataEditor.of(chart)) {
			editor.addCategory("Central", 2.0);
		}
		ChartSettings.setDataLabels(chart, false);

		presentationMLPackage.save(new File(outputfilepath));
		System.out.println("Saved " + outputfilepath);
	}
}
