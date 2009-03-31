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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

import org.docx4j.openpackaging.parts.JaxbXmlPart;


public final class StyleDefinitionsPart extends JaxbXmlPart {
	
	private static Logger log = Logger.getLogger(StyleDefinitionsPart.class);		
	

	public StyleDefinitionsPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public StyleDefinitionsPart() throws InvalidFormatException {
		super(new PartName("/word/styles.xml"));
		init();
	}
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.WORDPROCESSINGML_STYLES));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.STYLES);
	}
	
	// A variety of pre-defined styles, available for use in a StyleDefinitionsPart.
	private static java.util.Map<String, org.docx4j.wml.Style>  knownStyles = null;
	
	// private PropertyResolver propertyResolver;
	
	
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
    
    public Object unmarshal(org.w3c.dom.Element el) throws JAXBException {
    	
    	// Note: This is used when we read in a pkg:package 

		try {

			Unmarshaller u = jc.createUnmarshaller();
						
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

			jaxbElement = u.unmarshal( el );
			
			return jaxbElement;
			
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

    
    /**
     * Unmarshal a default set of styles, useful when creating this
     * part from scratch. 
     *
     * @return the newly created root object of the java content tree 
     *
     * @throws JAXBException 
     *     If any unexpected errors occur while unmarshalling
     */
    public Object unmarshalDefaultStyles() throws JAXBException {
    	
    	
//    	Throwable t = new Throwable();
//    	t.printStackTrace();
    	  
    		java.io.InputStream is = null;
			try {
				//Works in Eclipse if the resource is in source/main/java
				//is = getResource("styles.xml");
				
				// Works in Eclipse - not absence of leading '/'
				is = org.docx4j.utils.ResourceUtils.getResource("org/docx4j/openpackaging/parts/WordprocessingML/styles.xml");
				
					// styles.xml defines a small subset of common styles
					// (it is a much smaller set of styles than KnownStyles.xml)
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    		
    	
    	return unmarshal( is );    // side-effect is to set jaxbElement 	
    }
    
    private static void initKnownStyles() {

//    	Throwable t = new Throwable();
//    	t.printStackTrace();
    	
		java.io.InputStream is = null;
		try {
			is = org.docx4j.utils.ResourceUtils.getResource("org/docx4j/openpackaging/parts/WordprocessingML/KnownStyles.xml");						
			
			JAXBContext jc = Context.jc;
			Unmarshaller u = jc.createUnmarshaller();			
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

			org.docx4j.wml.Styles styles = (org.docx4j.wml.Styles)u.unmarshal( is );			
			
			knownStyles = new java.util.HashMap<String, org.docx4j.wml.Style>();
			
			for ( org.docx4j.wml.Style s : styles.getStyle() ) {				
				knownStyles.put(s.getStyleId(), s);				
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    		
    	
		
    }

	public static java.util.Map<String, org.docx4j.wml.Style> getKnownStyles() {
		if (knownStyles==null) {
			initKnownStyles();
		}
		return knownStyles;
	}
    
    
    
//	public static void main(String[] args) throws Exception {
//		
//		StyleDefinitionsPart sdp = new StyleDefinitionsPart ();		
//		sdp.initKnownStyles();
//    
//	}    
}
