package org.xlsx4j.samples;

import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.SheetData;
import org.xlsx4j.sml.Worksheet;

public class CreateSimpleSpreadsheet {
	
	public static void main(String[] args) throws Exception {
	
		String outputfilepath = System.getProperty("user.dir") + "/sample-docs/xlsx/test-out.xlsx";
		
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.createPackage();
		
		WorksheetPart sheet = pkg.createWorksheetPart(new PartName("/xl/worksheets/sheet1.xml"), "Sheet1", 1);
		
		addContent(sheet);
		
		SaveToZipFile saver = new SaveToZipFile(pkg);
		saver.save(outputfilepath);
				
		System.out.println("\n\n done .. " + outputfilepath);	
	}
	
	private static void addContent(WorksheetPart sheet) {
		
		// Minimal content already present
		SheetData sheetData = sheet.getJaxbElement().getSheetData();
				
		// Now add
		Row row = Context.getsmlObjectFactory().createRow();
		Cell cell = Context.getsmlObjectFactory().createCell();
		cell.setV("1234");
		
		row.getC().add(cell);
		sheetData.getRow().add(row);
	}
	
}
