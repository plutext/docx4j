/*
 *  Copyright 2010, Plutext Pty Ltd.
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

package org.docx4j.openpackaging.packages;


import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.io3.stores.ZipPartStore;
import org.docx4j.openpackaging.parts.DocPropsCorePart;
import org.docx4j.openpackaging.parts.DocPropsCustomPart;
import org.docx4j.openpackaging.parts.DocPropsExtendedPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.sml.Sheet;
import org.xlsx4j.sml.Sheets;
import org.xlsx4j.sml.Worksheet;



/**
 * @author jharrop
 *
 */
public class SpreadsheetMLPackage extends OpcPackage {
	
	protected static Logger log = LoggerFactory.getLogger(SpreadsheetMLPackage.class);
		
	
	/**
	 * Constructor.  Also creates a new content type manager
	 * 
	 */	
	public SpreadsheetMLPackage() {
		super();
		setContentType(new ContentType(ContentTypes.PRESENTATIONML_MAIN)); 		
	}
	/**
	 * Constructor.
	 *  
	 * @param contentTypeManager
	 *            The content type manager to use 
	 */
	public SpreadsheetMLPackage(ContentTypeManager contentTypeManager) {
		super(contentTypeManager);
		setContentType(new ContentType(ContentTypes.PRESENTATIONML_MAIN));
	}
	
	// Workbook part
	WorkbookPart wb;
	public WorkbookPart getWorkbookPart() {
		return wb;
	}
	
	
	/**
	 * Convenience method to create a SpreadsheetMLPackage
	 * from an existing File (.xlsx or .xml Flat OPC).
     *
	 * @param xlsxFile
	 *            The xlsx file 
	 */	
	public static SpreadsheetMLPackage load(java.io.File xlsxFile) throws Docx4JException {
		
		return (SpreadsheetMLPackage)OpcPackage.load(xlsxFile);
	}
	
	public boolean setPartShortcut(Part part, String relationshipType) {
		if (relationshipType.equals(Namespaces.PROPERTIES_CORE)) {
			docPropsCorePart = (DocPropsCorePart)part;
			log.info("Set shortcut for docPropsCorePart");
			return true;			
		} else if (relationshipType.equals(Namespaces.PROPERTIES_EXTENDED)) {
			docPropsExtendedPart = (DocPropsExtendedPart)part;
			log.info("Set shortcut for docPropsExtendedPart");
			return true;			
		} else if (relationshipType.equals(Namespaces.PROPERTIES_CUSTOM)) {
			docPropsCustomPart = (DocPropsCustomPart)part;
			log.info("Set shortcut for docPropsCustomPart");
			return true;			
		} else if (relationshipType.equals(Namespaces.SPREADSHEETML_WORKBOOK)) {
			wb = (WorkbookPart)part;
			log.info("Set shortcut for WorkbookPart");
			return true;			
		} else {	
			return false;
		}
	}
	

	
	/**
	 * Create an empty presentation.
	 * 
	 * @return
	 * @throws InvalidFormatException
	 */
	public static SpreadsheetMLPackage createPackage() throws InvalidFormatException {
		
		
		// Create a package
		SpreadsheetMLPackage xlsPack = new SpreadsheetMLPackage();

		try {
			
			xlsPack.wb = new WorkbookPart();
			xlsPack.wb.setJaxbElement(
					Context.getsmlObjectFactory().createWorkbook()
			);
			xlsPack.addTargetPart(xlsPack.wb);	
			
			xlsPack.wb.getJaxbElement().setSheets(
					Context.getsmlObjectFactory().createSheets()					
			);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidFormatException("Couldn't create package", e);
		}
		
		xlsPack.setPartStore(new ZipPartStore());

		// Return the new package
		return xlsPack;
		
	}
	
	/**
	 * Create a worksheet and add it to the package
	 * 
	 * @param wb
	 * @param partName
	 * @param sheetName
	 * @param sheetId
	 * @return
	 * @throws InvalidFormatException
	 * @throws JAXBException
	 */
	public WorksheetPart createWorksheetPart(PartName partName,
			String sheetName, long sheetId) 
		throws InvalidFormatException, JAXBException {
		
		WorksheetPart worksheetPart = new WorksheetPart(partName);
		
		Relationship r = wb.addTargetPart(worksheetPart);
		
		Sheets sheets = wb.getJaxbElement().getSheets();
		
		Sheet s = Context.getsmlObjectFactory().createSheet();
		s.setName(sheetName);
		s.setId(r.getId());
		s.setSheetId(sheetId);
		
		sheets.getSheet().add(s);
		
		// minimal content for the part
		Worksheet ws = Context.getsmlObjectFactory().createWorksheet();
		worksheetPart.setJaxbElement(ws);
		ws.setSheetData(
				Context.getsmlObjectFactory().createSheetData()
				);
		
		return worksheetPart;
	}
	
	
	
	public static void main(String[] args) throws Exception {
		
		String outputfilepath = System.getProperty("user.dir") + "/sample-docs/xlsx/test-out.xlsx";
		
		SpreadsheetMLPackage pkg = createPackage();
		
		pkg.createWorksheetPart(new PartName("/xl/worksheets/sheet1.xml"), "Sheet1", 1);
		
		SaveToZipFile saver = new SaveToZipFile(pkg);
		saver.save(outputfilepath);
				
		System.out.println("\n\n done .. " + outputfilepath);
		
	}	
}
