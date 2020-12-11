package org.docx4j.openpackaging.parts.SpreadsheetML;

import javax.xml.bind.JAXBElement;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.xlsx4j.sml.CTExternalLink;

public class ExternalLinkPart  extends JaxbSmlPart<JAXBElement<CTExternalLink>> {
	
	
	public ExternalLinkPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public ExternalLinkPart() throws InvalidFormatException {
		super(new PartName("/xl/externalLinks/externalLink1.xml"));
		init();
	}
	
	public void init() {	
				
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.SPREADSHEETML_EXTERNAL_LINK));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.SPREADSHEETML_EXTERNAL_LINK);
		
	}
	

}