package org.docx4j.openpackaging.parts.SpreadsheetML;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

public class PrinterSettings extends BinaryPart {

	public PrinterSettings(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public PrinterSettings() throws InvalidFormatException {
		super(new PartName("/xl/printerSettings/printerSettings1.bin"));
		init();
	}
	
	public void init() {	
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.SPREADSHEETML_PRINTER_SETTINGS));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.SPREADSHEETML_PRINTER_SETTINGS);
		
	}
	
}
