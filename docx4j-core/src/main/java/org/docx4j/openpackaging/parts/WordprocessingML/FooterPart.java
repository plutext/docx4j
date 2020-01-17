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


import java.util.List;

import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.McIgnorableNamespaceDeclarator;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPartAltChunkHost;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Ftr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class FooterPart extends JaxbXmlPartAltChunkHost<Ftr> implements ContentAccessor {
	
	private static Logger log = LoggerFactory.getLogger(FooterPart.class);			
	
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
		
	@Override
    protected void setMceIgnorable(McIgnorableNamespaceDeclarator namespacePrefixMapper) {
		
		// NB if you add ignorable content, it is up to you to jaxbElement.setIgnorable correctly; see further McIgnorableNamespaceDeclarator
		// You don't need to worry about this if you are merely loading an existing part.
		
		namespacePrefixMapper.setMcIgnorable(
				this.getJaxbElement().getIgnorable() );
	}

	@Override
	public String getMceIgnorable() {
    	return this.getJaxbElement().getIgnorable();
    }	
	
}
