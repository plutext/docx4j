/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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

package org.docx4j.openpackaging.parts.WordprocessingML;


import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AlternativeFormatInputPart extends BinaryPart {
	
	protected static Logger log = LoggerFactory.getLogger(AlternativeFormatInputPart.class);
	
	
	public AlternativeFormatInputPart(PartName partName) throws InvalidFormatException {
		super(partName);		

		String extension = partName.getExtension().toLowerCase();
		if (extension.equals("htm")) {
			setAltChunkType(AltChunkType.Html);
		} else {
			for(AltChunkType type: AltChunkType.values()) {
				if (extension.equals(type.getExtension())) {
					setAltChunkType(type);
				} 
			}
		}
		
		if (altChunkType==null) {
			// a loaded part's content type, set next, may type it (CR-053 D6)
			log.debug("No AltChunkType for the extension " + extension);
		}
		
		init();
	}
	
	public AlternativeFormatInputPart(AltChunkType type) throws InvalidFormatException {
		super(new PartName(generatePartName(type)));
		
		setAltChunkType(type);
		
		init();
	}
	
	private static String generatePartName(AltChunkType type) {
		return "/chunk." + type.getExtension();
	}
	
	private void init() {
		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.AF);
	}
	
	AltChunkType altChunkType;
	
	public void setAltChunkType(AltChunkType altChunkType) {
		
		this.altChunkType = altChunkType;

		// ContentType will vary - see spec 11.3.1 
		this.setContentType(new ContentType(altChunkType.getContentType()));
	}
	public AltChunkType getAltChunkType() {
		return altChunkType;
	}

	/**
	 * Sets the content type, and the chunk's type to the one it declares, where docx4j knows one
	 * (CR-053 D6): so a loaded chunk is typed by the content type the package declares, the part
	 * name's extension the fallback, and a copy whose content type is set agrees with it.
	 */
	@Override
	public void setContentType(ContentType contentType) {
		super.setContentType(contentType);
		AltChunkType declared = contentType == null ? null : AltChunkType.forContentType(contentType.toString());
		if (declared != null) altChunkType = declared;
	}

	public void registerInContentTypeManager() {
		ContentTypeManager ctm = this.getPackage().getContentTypeManager();
		if (altChunkType != null) {
			ctm.addDefaultContentType(altChunkType.getExtension(), altChunkType.getContentType());
		}
	}
}
