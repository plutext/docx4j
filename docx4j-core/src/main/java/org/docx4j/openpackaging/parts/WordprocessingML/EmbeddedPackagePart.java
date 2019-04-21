package org.docx4j.openpackaging.parts.WordprocessingML;
// Ideally this part would have been created in the parts 
// package itself, since it is also used when a 
// chart is embedded in a pptx.  ie there is nothing
// about it which is specific to WML.

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.utils.BufferUtil;

public class EmbeddedPackagePart extends BinaryPart { // maybe should extend EmbeddedPackagePart

	private static Logger log = LoggerFactory.getLogger(EmbeddedPackagePart.class);		
	
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
	
	/**
	 * @return
	 * @throws Docx4JException
	 * @since 3.0.0
	 */
	public OpcPackage extractPackage() throws Docx4JException {
		
		return OpcPackage.load(
				BufferUtil.newInputStream(getBuffer()));
		
	}


}
