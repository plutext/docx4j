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


import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.docx4j.listnumbering.Emulator;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.wml.Numbering;



public final class NumberingDefinitionsPart extends JaxbXmlPart {
	
	public NumberingDefinitionsPart(PartName partName) throws InvalidFormatException {
		super(partName);
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.WORDPROCESSINGML_NUMBERING));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.NUMBERING);
	}
	
	private Emulator em;
//	public void setEmulator(Emulator em) {
//		this.em = em;
//	}
	public Emulator getEmulator() {
		
    	if (em == null ) {    		
    		em = new Emulator( (Numbering)jaxbElement );    		
    	}
		
		return em;
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
			
//			javax.xml.validation.SchemaFactory sf = 
//				javax.xml.validation.SchemaFactory.newInstance(
//				      javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
//
//			javax.xml.validation.Schema schema = sf.newSchema(new java.io.File("/home/jharrop/workspace200711/docx4j-001/src/main/resources/wml-local-subset.xsd"));			

			//u.setSchema(org.docx4j.jaxb.WmlSchema.schema);
			
			//u.setValidating( false );
			
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
						
			jaxbElement = u.unmarshal( is );
			
			System.out.println("\n\n" + this.getClass().getName() + " unmarshalled \n\n" );									

		} catch (Exception e ) {
			e.printStackTrace();
		}
    	
		return jaxbElement;
    	
    }



	
	
}
