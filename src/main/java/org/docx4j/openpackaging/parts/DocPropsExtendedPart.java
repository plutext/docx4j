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

package org.docx4j.openpackaging.parts;


import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.dom4j.Document;




public class DocPropsExtendedPart extends JaxbXmlPart {
	
	/*
	 * <?xml version="1.0" encoding="UTF-8" standalone="yes"?> 
	 * <Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" 
	 * xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
		 * <Template>Normal.dotm</Template>
		 * <TotalTime>0</TotalTime>
		 * <Pages>1</Pages><Words>3</Words><Characters>20</Characters>
		 * <Application>Microsoft Office Word</Application>
		 * <DocSecurity>0</DocSecurity>
		 * <Lines>1</Lines><Paragraphs>1</Paragraphs>
		 * <ScaleCrop>false</ScaleCrop>
		 * <Company>Plutext Pty Ltd</Company>
		 * <LinksUpToDate>false</LinksUpToDate><CharactersWithSpaces>22</CharactersWithSpaces>
		 * <SharedDoc>false</SharedDoc>
		 * <HyperlinksChanged>false</HyperlinksChanged>
		 * <AppVersion>12.0000</AppVersion>
	 * </Properties>
	 */
	
	
	private static Logger log = Logger.getLogger(DocPropsExtendedPart.class);
	
	 /** 
	 * @throws InvalidFormatException
	 */
	public DocPropsExtendedPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public DocPropsExtendedPart() throws InvalidFormatException {
		super(new PartName("/docProps/app.xml"));
		init();
	}
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_EXTENDEDPROPERTIES));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.PROPERTIES_EXTENDED);
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
		    		    
			setJAXBContext(org.docx4j.jaxb.Context.jcDocPropsExtended);
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

	
