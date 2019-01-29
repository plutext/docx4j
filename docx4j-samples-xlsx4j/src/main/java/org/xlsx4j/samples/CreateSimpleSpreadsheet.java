package org.xlsx4j.samples;

import java.io.File;

import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.sml.CTRst;
import org.xlsx4j.sml.CTXstringWhitespace;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;
import org.xlsx4j.sml.SheetData;

public class CreateSimpleSpreadsheet {
	
	public static void main(String[] args) throws Exception {
	
		String outputfilepath = System.getProperty("user.dir") + "/OUT_CreateSimpleSpreadsheet.xlsx";
		
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.createPackage();
		
		WorksheetPart sheet = pkg.createWorksheetPart(new PartName("/xl/worksheets/sheet1.xml"), "Sheet1", 1);
		
		addContent(sheet);
				
		pkg.save(new File(outputfilepath));
						
		System.out.println("\n\n done .. " + outputfilepath);	
	}
	
	private static void addContent(WorksheetPart sheet) {
		
		// Minimal content already present
		SheetData sheetData = sheet.getJaxbElement().getSheetData();
				
		// Now add
		Row row = Context.getsmlObjectFactory().createRow();
		//row.setR((long) 1);  // optional
		
		Cell cell = Context.getsmlObjectFactory().createCell();
		cell.setV("1234");
		cell.setR("A1");  // Apple Numbers needs this, or cell content won't show 
		row.getC().add(cell);
		
		Cell cell2 = createCell("hello world!");
		row.getC().add(cell2);
		cell2.setR("B1"); // be careful the numeral matches the row correctly, or Excel will complain
		
		sheetData.getRow().add(row);
	}
	
	/**
		// Note: if you are trying to add characters, not a number,
		// the easiest approach is to use inline strings (as opposed to the shared string table).
		// See http://openxmldeveloper.org/blog/b/openxmldeveloper/archive/2011/11/22/screen-cast-write-simpler-spreadsheetml-when-generating-spreadsheets.aspx
		// and http://www.docx4java.org/forums/xlsx-java-f15/cells-with-character-values-t874.html
	 */
	private static Cell createCell(String content) {

		Cell cell = Context.getsmlObjectFactory().createCell();
		
		CTXstringWhitespace ctx = Context.getsmlObjectFactory().createCTXstringWhitespace();
		ctx.setValue(content);
		
		CTRst ctrst = new CTRst();
		ctrst.setT(ctx);

		cell.setT(STCellType.INLINE_STR);
		cell.setIs(ctrst); // add ctrst as inline string
		
		return cell;
		
	}
	
}
