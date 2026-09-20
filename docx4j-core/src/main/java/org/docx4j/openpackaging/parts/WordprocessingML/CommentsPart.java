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

import org.docx4j.jaxb.McIgnorableNamespaceDeclarator;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.JaxbXmlPartXPathAware;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Comments;



public final class CommentsPart extends JaxbXmlPartXPathAware<Comments> {
	
	public CommentsPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public CommentsPart() throws InvalidFormatException {
		super(new PartName("/word/comments.xml"));
		init();
	}
	
	public void init() {		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.WORDPROCESSINGML_COMMENTS));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.COMMENTS);
		
	}

	/**
	 * Hand this part's mc:Ignorable value to the prefix mapper, so that the
	 * namespaces it names (w14 over w14:paraId, for example) are declared on
	 * this part's root element.  Word requires that; see further
	 * {@link org.docx4j.jaxb.McIgnorableNamespaceDeclarator}.
	 *
	 * NB it is up to you to jaxbElement.setIgnorable.
	 *
	 * @since 17.2.0
	 */
	@Override
    protected void setMceIgnorable(McIgnorableNamespaceDeclarator namespacePrefixMapper) {

		namespacePrefixMapper.setMcIgnorable(
				this.getJaxbElement().getIgnorable() );
	}

	/**
	 * @since 17.2.0
	 */
	@Override
	public String getMceIgnorable() {
    	return this.getJaxbElement().getIgnorable();
    }

}
