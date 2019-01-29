package org.xlsx4j.samples;

import javax.xml.bind.JAXBException;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.sml.CTSheetFormatPr;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.SheetData;

public class RowHeight {

	/**
	 * @param args
	 * @throws JAXBException 
	 * @throws Docx4JException 
	 */
	public static void main(String[] args) throws JAXBException, Docx4JException {
		// TODO Auto-generated method stub
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.createPackage();
	      
		WorksheetPart sheet = pkg.createWorksheetPart(new PartName("/sheet1.xml"), "Sheet1", 1);
		      
		CTSheetFormatPr format = Context.getsmlObjectFactory().createCTSheetFormatPr();
		format.setDefaultRowHeight(5);
		format.setCustomHeight(Boolean.TRUE);
		sheet.getJaxbElement().setSheetFormatPr(format);
		      
		SheetData sheetData = sheet.getJaxbElement().getSheetData();
		            
		Row row = Context.getsmlObjectFactory().createRow();
		      
		row.setHt(66.0);
		row.setCustomHeight(Boolean.TRUE);
		row.setR(1L);
		      
		Cell cell1 = Context.getsmlObjectFactory().createCell();
		cell1.setV("1234");
		row.getC().add(cell1);
		      
		Cell cell2 = Context.getsmlObjectFactory().createCell();
		cell2.setV("56");
		row.getC().add(cell2);
		sheetData.getRow().add(row);
		            
		SaveToZipFile saver = new SaveToZipFile(pkg);
		saver.save( System.getProperty("user.dir") + "/sheet10.xlsx");
	}

}
