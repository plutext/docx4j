package org.docx4j.openpackaging.parts.SpreadsheetML;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.xlsx4j.sml.CTCalcChain;

public class CalcChain  extends JaxbSmlPart<CTCalcChain> {
	
	public CalcChain(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public CalcChain() throws InvalidFormatException {
		super(new PartName("/xl/calcChain.xml"));
		init();
	}
	
	public void init() {	
				
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.SPREADSHEETML_CALC_CHAIN));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.SPREADSHEETML_CALC_CHAIN);
		
	}

}