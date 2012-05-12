package org.docx4j.openpackaging.parts.WordprocessingML;
// Ideally this part would have been created in the parts 
// package itself, since it is also used when a 
// chart is embedded in a pptx.  ie there is nothing
// about it which is specific to WML.

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

public class EmbeddedPackagePart extends BinaryPart { // maybe should extend EmbeddedPackagePart

	private static Logger log = Logger.getLogger(EmbeddedPackagePart.class);		
	
	public EmbeddedPackagePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();				
	}

	
	public EmbeddedPackagePart() throws InvalidFormatException {
		super( new PartName("/word/embeddings/foo") ); // eg Microsoft_Office_Powerpoint_Presentation.pptx
		init();				
	}
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
//		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
//				org.docx4j.openpackaging.contenttype.ContentTypes.PRESENTATION));

		// Used when this Part is added to a rels
		setRelationshipType(Namespaces.EMBEDDED_PKG);
		
		
	}


}
