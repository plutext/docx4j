package org.docx4j.openpackaging.parts.SpreadsheetML;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.xlsx4j.sml.CTStylesheet;

public class Styles  extends JaxbSmlPart<CTStylesheet> {
	
	public Styles(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public Styles() throws InvalidFormatException {
		super(new PartName("/xl/styles.xml"));
		init();
	}
	
	public void init() {
				
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.SPREADSHEETML_STYLES));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.SPREADSHEETML_STYLES);
		
	}

}