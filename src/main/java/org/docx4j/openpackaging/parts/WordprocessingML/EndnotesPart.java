/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.openpackaging.parts.WordprocessingML;


import java.util.List;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPartXPathAware;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.CTEndnotes;
import org.docx4j.wml.ContentAccessor;


public final class EndnotesPart extends JaxbXmlPartXPathAware<CTEndnotes>  implements ContentAccessor {
	
	public EndnotesPart(PartName partName) throws InvalidFormatException {
		super(partName);
	}

	public EndnotesPart() throws InvalidFormatException {
		super(new PartName("/word/endnotes.xml"));
		init();		
	}
	
	public void init() {		
			
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.WORDPROCESSINGML_ENDNOTES));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.ENDNOTES);
		
	}

    /**
     * Convenience method to getJaxbElement().getEndnote()
     * @since 2.8.1
     */
    public List<Object> getContent() {
    	
    	if (this.getJaxbElement()==null) {    		
    		this.setJaxbElement( Context.getWmlObjectFactory().createCTEndnotes() );
    	}
    	
    	return this.getJaxbElement().getEndnote();
    }	
	

}
