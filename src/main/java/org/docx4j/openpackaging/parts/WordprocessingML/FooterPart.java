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

import org.apache.log4j.Logger;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPartXPathAware;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Ftr;


public final class FooterPart extends JaxbXmlPartXPathAware<Ftr>  implements ContentAccessor {
	
	private static Logger log = Logger.getLogger(FooterPart.class);			
	
	public FooterPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
		
	}

	public FooterPart() throws InvalidFormatException {
		super(new PartName("/word/footer.xml"));  // Not very useful, since normally there is more than one footer part
		init();		
	}		
		
		public void init() {
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.WORDPROCESSINGML_FOOTER));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.FOOTER);
	}
	
    /**
     * Convenience method to getJaxbElement().getBody().getContent()
     * @since 2.7
     */
    public List<Object> getContent() {
    	
    	if (this.getJaxbElement()==null) {    		
    		this.setJaxbElement( Context.getWmlObjectFactory().createFtr() );
    	}
    	
    	return this.getJaxbElement().getContent();
    }	
		
	
}
