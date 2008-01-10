/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.openpackaging.parts.WordprocessingML;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

import org.docx4j.openpackaging.parts.JaxbXmlPart;


public final class StyleDefinitionsPart extends JaxbXmlPart {
	
	private static Logger log = Logger.getLogger(StyleDefinitionsPart.class);		
	

	public StyleDefinitionsPart(PartName partName) throws InvalidFormatException {
		super(partName);
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.WORDPROCESSINGML_STYLES));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.STYLES);

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
