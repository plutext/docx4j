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


import java.io.IOException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

import org.docx4j.openpackaging.parts.JaxbXmlPart;


public final class FooterPart extends JaxbXmlPart {
	
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
	     * Unmarshal XML data from the specified InputStream and return the 
	     * resulting content tree.  Validation event location information may
	     * be incomplete when using this form of the unmarshal API.
	     *
	     * <p>
	     * Implements <a href="#unmarshalGlobal">Unmarshal Global Root Element</a>.
	     * 
	     * @param is the InputStream to unmarshal XML data from
	     * @return the newly created root object of the java content tree 
	     *
	     * @throws JAXBException 
	     *     If any unexpected errors occur while unmarshalling
	     */
	    public Object unmarshal( java.io.InputStream is ) throws JAXBException {
	    	
			try {
				
//				if (jc==null) {
//					setJAXBContext(Context.jc);				
//				}
			    		    
				Unmarshaller u = jc.createUnmarshaller();
				
				//u.setSchema(org.docx4j.jaxb.WmlSchema.schema);
				u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

				System.out.println("unmarshalling " + this.getClass().getName() + " \n\n" );									
							
				jaxbElement = u.unmarshal( is );
								
				System.out.println("\n\n" + this.getClass().getName() + " unmarshalled \n\n" );									

			} catch (Exception e ) {
				e.printStackTrace();
			}
	    	
			return jaxbElement;
	    	
	    }
	
	
}
