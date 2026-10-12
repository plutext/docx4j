/*
 * $Id$
 * Created on Oct 30, 2012 by beardj 
 */
package org.pptx4j.samples;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.docx4j.model.chart.ChartDataEditor;
import org.docx4j.model.chart.ChartRef;
import org.docx4j.model.chart.ChartSeries;
import org.docx4j.model.chart.Charts;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.DrawingML.Chart;

/**
 * Simple demonstration of editing charts in a PowerPoint deck: a chart's data changed in the
 * workbook embedded with it, the chart's caches (what PowerPoint draws) kept in step.
 *
 * <p>Since 17.3.3 this uses {@link ChartDataEditor} (CR-036), which keeps the cells, the table,
 * the chart's references and its caches consistent as PowerPoint's own edits do, and writes the
 * workbook back when it is closed (or when the deck is saved).  It can also add and remove
 * categories and series, or replace the data with setData.  (The sample first set the caches and
 * the cells by hand.)
 *
 * @author Jeff Beard
 *
 */
public class EditEmbeddedCharts
{

	public static void main(String[] args) throws Exception {
		
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/pptx-chart.pptx";
		
		String outputfilepath = System.getProperty("user.dir") 
				+ "/OUT_EditEmbeddedCharts-" 
				+ System.currentTimeMillis() + ".pptx";
		
		Random rand = new Random();

		PresentationMLPackage ppt = (PresentationMLPackage) OpcPackage
			.load(new File(inputfilepath));

		// The deck's charts, slide by slide
		List<ChartRef> charts = Charts.in(ppt);
		ChartRef chart = charts.get(0);
		System.out.println("Editing " + chart.getChartPart().getPartName() + " on " + chart.getHost().getPartName());

		try (ChartDataEditor editor = ChartDataEditor.of((Chart) chart.getChartPart())) {
			ChartSeries series = editor.getSeries().get(0);
			System.out.println("series " + series.getShownName() + ": " + series.getValues().getText(0) + ", "
					+ series.getValues().getText(1));
			editor.setValue(series, 0, rand.nextInt(99));
			editor.setValue(series, 1, rand.nextInt(99));
			series = editor.getSeries().get(0);
			System.out.println("now: " + series.getValues().getText(0) + ", " + series.getValues().getText(1));
		}

		// Write the new file to disk
		ppt.save(new File(outputfilepath));

		System.out.println("\n\n done .. saved " + outputfilepath);
	}
}
