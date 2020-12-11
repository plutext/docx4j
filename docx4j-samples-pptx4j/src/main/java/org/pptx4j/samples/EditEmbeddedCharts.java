/*
 * $Id$
 * Created on Oct 30, 2012 by beardj 
 */
package org.pptx4j.samples;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.docx4j.dml.chart.CTBarChart;
import org.docx4j.dml.chart.CTBarSer;
import org.docx4j.dml.chart.CTNumVal;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EmbeddedPackagePart;
import org.docx4j.utils.BufferUtil;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;

/**
 * Simple demonstration of editing charts in a PowerPoint deck
 *
 * @author Jeff Beard
 *
 */
public class EditEmbeddedCharts
{

	/**
	 * Main method
	 *
	 * @param args
	 * @throws Docx4JException 
	 */
	public static void main(String[] args) throws Docx4JException {
		
		// Input file
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/pptx-chart.pptx";
		
		// The names of the parts which will be edited
		// Alter these to match what is in your input pptx
		// .. the chart
		String chartPartName = "/ppt/charts/chart1.xml";
		// .. the xlsx
		String xlsPartName = "/ppt/embeddings/Microsoft_Excel_Sheet1.xlsx";
		
		// Output file
		String outputfilepath = System.getProperty("user.dir") 
				+ "/OUT_EditEmbeddedCharts-" 
				+ System.currentTimeMillis() + ".pptx";
		
		// Values to change
		Random rand = new Random();

		String firstValue  = String.valueOf(rand.nextInt(99));
		String secondValue = String.valueOf(rand.nextInt(99));
		
		// Open the PPT template file
		PresentationMLPackage ppt = (PresentationMLPackage) OpcPackage
			.load(new java.io.File(inputfilepath));

		/*
		 * Get the Chart object and update the values. Afterwards, we'll update 
		 * the associated spreadsheet so that the data is synchronized.
		 */
		Chart chart = (Chart) ppt.getParts().get(new PartName(chartPartName));
		
		List<Object> objects = chart.getJaxbElement().getChart().getPlotArea()
				.getAreaChartOrArea3DChartOrLineChart();
		
		for (Object object : objects) {
			
			if (object instanceof CTBarChart) {

				List<CTBarSer> ctBarSers = ((CTBarChart) object).getSer();
				
				for (CTBarSer ctBarSer : ctBarSers)
				{
					List<CTNumVal> ctNumVals = ctBarSer.getVal().getNumRef().getNumCache().getPt();
					for (CTNumVal ctNumVal : ctNumVals)
					{
						System.out.println("ctNumVal Val BEFORE: " + ctNumVal.getV());
						if (ctNumVal.getIdx() == 0) {
							ctNumVal.setV(firstValue);
						}
						else if (ctNumVal.getIdx() == 1) {
							ctNumVal.setV(secondValue);	
						}
						System.out.println("ctNumVal Val AFTER: " + ctNumVal.getV());
					}
				}
			}
		}
				
		/*
		 * Get the spreadsheet and find the cell values that need to be updated
		 */
		
		EmbeddedPackagePart epp  = (EmbeddedPackagePart) ppt
			.getParts().get(new PartName(xlsPartName));
		
		if (epp==null) {
			throw new Docx4JException("Could find EmbeddedPackagePart: " + xlsPartName);
		}
		
		InputStream is = BufferUtil.newInputStream(epp.getBuffer());
		
		SpreadsheetMLPackage spreadSheet = (SpreadsheetMLPackage) SpreadsheetMLPackage.load(is);

		Map<PartName,Part> partsMap = spreadSheet.getParts().getParts();		 
		Iterator<Entry<PartName, Part>> it = partsMap.entrySet().iterator();

		while(it.hasNext()) {
			Map.Entry<PartName, Part> pairs = it.next();
			
			if (partsMap.get(pairs.getKey()) instanceof WorksheetPart) {
				
				WorksheetPart wsp = (WorksheetPart) partsMap.get(pairs.getKey()) ;
				
				List<Row> rows = wsp.getJaxbElement().getSheetData().getRow();

				for (Row row : rows) {
					List<Cell> cells = row.getC();
					for (Cell cell : cells)
					{
						if (cell.getR().equals("B2") && cell.getV() != null) {
							System.out.println("B2 CELL VAL: " + cell.getV());
							// change the B2 cell value
							cell.setT(STCellType.STR);
							cell.setV(firstValue);
						}
						else if (cell.getR().equals("B3") && cell.getV() != null) {
							System.out.println("B3 CELL VAL: " + cell.getV());
							// Change the B3 cell value
							cell.setT(STCellType.STR);
							cell.setV(secondValue);
						}
					}					
				}
			}
		}

		/*
		 * Convert the Spreadsheet to a binary format, set it on the 
		 * EmbeddedPackagePart, add it back onto the deck and save to a file.
		 *  
		 */		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		SaveToZipFile saver = new SaveToZipFile(spreadSheet);

		saver.save(baos);
		epp.setBinaryData(baos.toByteArray());

		// Write the new file to disk
		ppt.save(new java.io.File(outputfilepath));

		System.out.println("\n\n done .. saved " + outputfilepath);
	}
}
