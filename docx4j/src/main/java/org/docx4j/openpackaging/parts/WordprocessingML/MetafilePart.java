package org.docx4j.openpackaging.parts.WordprocessingML;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;

public abstract class MetafilePart extends BinaryPart {
	
	public MetafilePart(PartName partName) throws InvalidFormatException {
		super(partName);
		
		// Can't setContentType or setRelationshipType, since 
		// these will differ depending on the nature of the data.
		// Common binary parts should extend this class to 
		// provide that information.
		
	}
	

}
