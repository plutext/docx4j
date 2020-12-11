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
import org.docx4j.wml.CTEndnotes;


public final class EndnotesPart extends JaxbXmlPartXPathAware<CTEndnotes> {
	
	/* Unfortunately, this class can't easily implement
	 * ContentAccessor, because to do that,
	 * both CTFootnotes and CTEndnotes would need 
	 * to return List<Object> (if the list is to be
	 * live not a snapshot).
	 * 
	 * But in that case, you get 
	 *  java.lang.ClassCastException: ElementNSImpl cannot be cast to org.docx4j.wml.CTFtnEdn
	 * because JAXB marshalls to a DOM node.
	 * And we can't put @XmlRootElement on CTFtnEdn, since it could be either Ftn or Edn.
	 * 
	 * See further http://stackoverflow.com/questions/5122296/jaxb-not-unmarshalling-xml-any-element-to-jaxbelement
	 * 
	 * If we really need to implement ContentAccessor here, 
	 * the first thing to do would be to change wml.xsd, so we generate
	 * distinct classes CTFtn and CTEdn.  But that would be backwards incompatible,
	 * unless we also retained CTFtnEdn in some fashion?
	 */
	
		
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

//    /**
//     * Convenience method to getJaxbElement().getEndnote()
//     * @since 2.8.1
//     */
//    public List<Object> getContent() {
//    	
//    	if (this.getJaxbElement()==null) {    		
//    		this.setJaxbElement( Context.getWmlObjectFactory().createCTEndnotes() );
//    	}
//    	
//    	return this.getJaxbElement().getEndnote();
//    }	
	
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
