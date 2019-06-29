/*
 *  Copyright 2019, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */

package org.xlsx4j.samples;


import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlsx4j.sml.CTRElt;
import org.xlsx4j.sml.CTRst;
import org.xlsx4j.sml.CTXstringWhitespace;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;
import org.xlsx4j.sml.SheetData;
import org.xlsx4j.sml.Worksheet;


/**
 * For now, all this does is represent inline strings correctly
 * 
 * @author jharrop
 */
public class CellContentRemediator {
	
	private static Logger log = LoggerFactory.getLogger(CellContentExtractor.class);						

	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/input.xlsx";
			
		// Open a document from the file system
		SpreadsheetMLPackage xlsxPkg = SpreadsheetMLPackage.load(new java.io.File(inputfilepath));		
				
		WorkbookPart workbookPart = xlsxPkg.getWorkbookPart();
		WorksheetPart sheet = workbookPart.getWorksheet(0);
		
		// Now fix strings
		remediateStrings(sheet);
		
		xlsxPkg.save((new java.io.File(System.getProperty("user.dir") + "/fixed2.xlsx")));	
	}
	
	
	private static void remediateStrings(WorksheetPart sheet) throws Docx4JException {

		Worksheet ws = sheet.getContents();
		SheetData data = ws.getSheetData();
		
		for (Row r : data.getRow() ) {
			System.out.println("row " + r.getR() );			
			
			for (Cell c : r.getC() ) {
				
				String cellValue = c.getV();
				
				try {
					if (cellValue!=null) {
						float f = Float.parseFloat(cellValue);
					}
				} catch (NumberFormatException nf) {
					// Its not a number; convert to INLINE_STR
					
					CTXstringWhitespace t = new CTXstringWhitespace();
					t.setValue(cellValue);
					CTRElt rElt = new CTRElt();
					rElt.setT(t);
					
					CTRst rst = new CTRst();
					rst.getR().add(rElt);
					
					c.setIs(rst);
					
					c.setT(STCellType.INLINE_STR);
					
					c.setV(null);
					
					System.out.println("fixed " + c.getR());
					
				}
	        }
		}
	}
	
	
	
}
