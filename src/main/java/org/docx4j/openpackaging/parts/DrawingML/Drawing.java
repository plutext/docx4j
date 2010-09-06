package org.docx4j.openpackaging.parts.DrawingML;

import javax.xml.bind.JAXBElement;

import org.docx4j.dml.CTTableStyleList;
import org.docx4j.dml.spreadsheetdrawing.CTDrawing;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.sml.CTStylesheet;

public class Drawing extends JaxbDmlPart<JAXBElement<CTDrawing>> {
	// This part uses a JAXB content model from dml,
	// so we need to use that context.
	// Hence this class doesn't extent JaxbPmlPart.
	
	public Drawing(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public Drawing() throws InvalidFormatException {
		super(new PartName("/xl/drawings/drawing1.xml"));
		init();
	}
	
	public void init() {
				
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.DRAWINGML_DRAWING));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.SPREADSHEETML_DRAWING);
		
	}

}