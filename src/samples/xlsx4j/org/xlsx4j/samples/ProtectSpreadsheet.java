package org.xlsx4j.samples;

import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.ProtectWorkbook;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.org.apache.poi.poifs.crypt.HashAlgorithm;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.sml.CTRst;
import org.xlsx4j.sml.CTSheetProtection;
import org.xlsx4j.sml.CTXstringWhitespace;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;
import org.xlsx4j.sml.SheetData;

public class ProtectSpreadsheet {
	
	public static void main(String[] args) throws Exception {
	
		String outputfilepath = System.getProperty("user.dir") + "/OUT_ProtectBook-512-B.xlsx";
		
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.createPackage();
		
		WorksheetPart sheet = pkg.createWorksheetPart(new PartName("/xl/worksheets/sheet1.xml"), "Sheet1", 1);
		
		addContent(sheet);
		
		ProtectWorkbook protection = pkg.getProtectionSettings();
//		CTSheetProtection sheetProtection = protection.getSheetProtection(sheet);		
//		protection.setSheetProtectionPassword(sheetProtection, "hello");
//		//protection.setSheetProtection(sheetProtection, "hello", HashAlgorithm.sha512);
//		System.out.println(protection.validateSheetProtectionPassword(sheetProtection, "hello"));
		
		protection.setWorkbookProtection("sothere", true, true, false);
		
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
		
		
		row.getC().add(createCell("hello world!"));
		
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
