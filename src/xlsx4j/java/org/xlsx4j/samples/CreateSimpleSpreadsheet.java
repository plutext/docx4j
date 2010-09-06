package org.xlsx4j.samples;

import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;

public class CreateSimpleSpreadsheet {
	
	public static void main(String[] args) throws Exception {
	
		String outputfilepath = System.getProperty("user.dir") + "/sample-docs/xlsx/test-out.xlsx";
		
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.createPackage();
		
		pkg.createWorksheetPart(new PartName("/xl/worksheets/sheet1.xml"), "Sheet1", 1);
		
		SaveToZipFile saver = new SaveToZipFile(pkg);
		saver.save(outputfilepath);
				
		System.out.println("\n\n done .. " + outputfilepath);	
	}
	
}
