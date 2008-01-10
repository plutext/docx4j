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



import org.apache.log4j.Logger;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBElement; 

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class MainDocumentPart extends DocumentPart  {
	
	private static Logger log = Logger.getLogger(MainDocumentPart.class);
		
	
	public MainDocumentPart(PartName partName) throws InvalidFormatException {
		super(partName);
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.WORDPROCESSINGML_DOCUMENT));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.DOCUMENT);
		
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
			
			JAXBElement<?> root = (JAXBElement<?>)u.unmarshal( is );
			
			jaxbElement = (org.docx4j.wml.Document)root.getValue();
			
			System.out.println("\n\n" + this.getClass().getName() + " unmarshalled \n\n" );									

		} catch (Exception e ) {
			e.printStackTrace();
		}
    	
		return jaxbElement;
    	
    }



	private void debugPrint( Document coreDoc) {
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
		    XMLWriter writer = new XMLWriter( System.out, format );
		    writer.write( coreDoc );
		} catch (Exception e ) {
			e.printStackTrace();
		}	    
	}
			
	
}

	
