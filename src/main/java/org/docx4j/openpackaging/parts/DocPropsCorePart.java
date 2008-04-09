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
package org.docx4j.openpackaging.parts;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.dom4j.Document;


public class DocPropsCorePart extends JaxbXmlPart {
	
	/*
	 * <?xml version="1.0" encoding="UTF-8" standalone="yes"?> 
	 * <cp:coreProperties 
		 * xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" 
		 * xmlns:dc="http://purl.org/dc/elements/1.1/" 
		 * xmlns:dcterms="http://purl.org/dc/terms/" 
		 * xmlns:dcmitype="http://purl.org/dc/dcmitype/" 
		 * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
		 * <dc:creator>Jason Harrop</dc:creator>
		 * <cp:lastModifiedBy>Jason Harrop</cp:lastModifiedBy>
		 * <cp:revision>2</cp:revision>
		 * <dcterms:created xsi:type="dcterms:W3CDTF">2007-08-12T00:34:00Z</dcterms:created>
		 * <dcterms:modified xsi:type="dcterms:W3CDTF">2007-08-12T00:34:00Z</dcterms:modified>
	 * </cp:coreProperties>
	 */
	
	
	private static Logger log = Logger.getLogger(DocPropsCorePart.class);
	
	 /** 
	 * @throws InvalidFormatException
	 */
	public DocPropsCorePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public DocPropsCorePart() throws InvalidFormatException {
		super(new PartName("/docProps/core.xml"));
		init();
	}
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.PACKAGE_COREPROPERTIES));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.PROPERTIES_CORE);
		
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
			
//			if (jc==null) {
//				setJAXBContext(Context.jc);				
//			}
		    		    
			setJAXBContext(org.docx4j.jaxb.Context.jcDocPropsCore);
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

	
