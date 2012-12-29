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

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.DocDefaults;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.docx4j.wml.Style.BasedOn;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;


public final class StyleDefinitionsPart extends JaxbXmlPart<Styles> {
	
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
	
	
    
//	@Override
//    public Styles unmarshal(org.w3c.dom.Element el) throws JAXBException {
//    	
//    	// Note: This is used when we read in a pkg:package 
//
//		try {
//
//			Unmarshaller u = jc.createUnmarshaller();
//						
//			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
//
//			jaxbElement = (Styles) u.unmarshal( el );
//			
//			return jaxbElement;
//			
//		} catch (JAXBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		}
//	}

    
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
				is = org.docx4j.utils.ResourceUtils.getResource(
						"org/docx4j/openpackaging/parts/WordprocessingML/styles.xml");
				
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
			is = org.docx4j.utils.ResourceUtils.getResource(
					"org/docx4j/openpackaging/parts/WordprocessingML/KnownStyles.xml");						
			
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
    
	/*
	 * Manufacture styles from the following, so they can be used as the 
	 * roots of our style trees.
	 * 
	 * 	<w:docDefaults>
			<w:rPrDefault>
				<w:rPr>
					<w:rFonts w:asciiTheme="minorHAnsi" w:eastAsiaTheme="minorHAnsi" w:hAnsiTheme="minorHAnsi" w:cstheme="minorBidi" />
					<w:sz w:val="22" />
					<w:szCs w:val="22" />
					<w:lang w:val="en-US" w:eastAsia="en-US" w:bidi="ar-SA" />
				</w:rPr>
			</w:rPrDefault>
			<w:pPrDefault>
				<w:pPr>
					<w:spacing w:after="200" w:line="276" w:lineRule="auto" />
				</w:pPr>
			</w:pPrDefault>
		</w:docDefaults>

	 */
    protected void createVirtualStylesForDocDefaults() throws Docx4JException {
    	
    	Style pDefault = Context.getWmlObjectFactory().createStyle();
    	
    	String ROOT_NAME = "DocDefaults";
    	
    	pDefault.setStyleId(ROOT_NAME);
    	pDefault.setType("paragraph");
    			
		// Initialise docDefaults		
		DocDefaults docDefaults = this.getJaxbElement().getDocDefaults(); 		
		
		if (docDefaults == null) {
			// The only way this can happen is if the
			// styles definition part is missing the docDefaults element
			// (these are present in docs created from Word, and
			// in our default styles, so maybe the user created it using
			// some 3rd party program?)
			try {

				docDefaults = (DocDefaults) XmlUtils
						.unmarshalString(docDefaultsString);
			} catch (JAXBException e) {
				throw new Docx4JException("Problem unmarshalling "
						+ docDefaultsString, e);
			}
		}

		// Setup documentDefaultPPr
		PPr documentDefaultPPr;
		if (docDefaults.getPPrDefault() == null) {
			try {
				documentDefaultPPr = (PPr) XmlUtils
						.unmarshalString(pPrDefaultsString);
			} catch (JAXBException e) {
				throw new Docx4JException("Problem unmarshalling "
						+ pPrDefaultsString, e);
			}

		} else {
			documentDefaultPPr = docDefaults.getPPrDefault().getPPr();
		}

		// Setup documentDefaultRPr
		RPr documentDefaultRPr;
		if (docDefaults.getRPrDefault() == null) {
			try {
				documentDefaultRPr = (RPr) XmlUtils
						.unmarshalString(rPrDefaultsString);
			} catch (JAXBException e) {
				throw new Docx4JException("Problem unmarshalling "
						+ rPrDefaultsString, e);
			}
		} else {
			documentDefaultRPr = docDefaults.getRPrDefault().getRPr();
		}
    	
		pDefault.setPPr(documentDefaultPPr);
		pDefault.setRPr(documentDefaultRPr);
		
		// Now point Normal at this
		Style normal = getDefaultParagraphStyle();
		if (normal==null) {
			log.warn("No default paragraph style!!");
			normal = Context.getWmlObjectFactory().createStyle();
			normal.setType("paragraph");
			normal.setStyleId("Normal");
			
			org.docx4j.wml.Style.Name n = Context.getWmlObjectFactory().createStyleName();
			n.setVal("Normal");
			normal.setName(n);
			this.getJaxbElement().getStyle().add(normal);			
		}
		
		BasedOn based = Context.getWmlObjectFactory().createStyleBasedOn();
		based.setVal(ROOT_NAME);		
		normal.setBasedOn(based);
		
		// Finally, add it to styles
		this.getJaxbElement().getStyle().add(pDefault);
		log.debug("Added virtual style, id '" + pDefault.getStyleId() + "', name '"+ pDefault.getName() + "'");
		
		
    	
    }
    
    /**
     * @param id
     * @return
     * @since 3.0.0
     */
    public Style getStyleById(String id) {
    	
		for ( org.docx4j.wml.Style s : this.getJaxbElement().getStyle() ) {				
			if( s.getStyleId().equals(id) ) {
				return s;
			}
		}
    	return null;
    }

    private Style defaultCharacterStyle;
    public Style getDefaultCharacterStyle() {
    	
    	if (defaultCharacterStyle==null) {
    		defaultCharacterStyle = getDefaultStyle("character");
    	}
    	// OpenOffice conversion to docx
    	// doesn't necessarily contain a default character style
    	// so manufacture one
    	if (defaultCharacterStyle==null) {
    		try {
				defaultCharacterStyle = (Style)XmlUtils.unmarshalString(DEFAULT_CHARACTER_STYLE_DEFAULT);
				this.getJaxbElement().getStyle().add(defaultCharacterStyle);
			} catch (JAXBException e) {
				e.printStackTrace();
			}
    	}
		return defaultCharacterStyle;
    }
    
    private final static String DEFAULT_CHARACTER_STYLE_DEFAULT = "<w:style w:type=\"character\" w:default=\"1\" w:styleId=\"DefaultParagraphFont\" " + Namespaces.W_NAMESPACE_DECLARATION + "><w:name w:val=\"Default Paragraph Font\" /></w:style>";
    
    
    private Style defaultParagraphStyle;
    public Style getDefaultParagraphStyle() {
    	
    	if (defaultParagraphStyle==null) {
    		defaultParagraphStyle = getDefaultStyle("paragraph");
    	}
    	// OpenOffice conversion to docx
    	// doesn't set default, so use name
    	// (alternatively, could use id=style0)
    	if (defaultParagraphStyle==null) {
    		for ( org.docx4j.wml.Style s : this.getJaxbElement().getStyle() ) {				
    			if( s.getType().equals("paragraph")
    					&& s.getName().getVal().equals("Default") ) {
    				log.info("Style with name " + s.getName().getVal() + ", id '" + s.getStyleId() + "' is default " + s.getType() + " style");
    				defaultParagraphStyle=s;
    				break;
    			}
    		}    		
    	}
    	// try using id=style0
    	if (defaultParagraphStyle==null) {
    		for ( org.docx4j.wml.Style s : this.getJaxbElement().getStyle() ) {				
    			if( s.getType().equals("paragraph")
    					&& s.getStyleId().equals("style0") ) {
    				log.info("Style with name " + s.getName().getVal() + ", id '" + s.getStyleId() + "' is default " + s.getType() + " style");
    				defaultParagraphStyle=s;
    				break;
    			}
    		}    		
    	}
    	
		return defaultParagraphStyle;
    }
    private Style getDefaultStyle(String type) {
    	
		for ( org.docx4j.wml.Style s : this.getJaxbElement().getStyle() ) {				
			if( s.isDefault() && s.getType().equals(type)) {
				log.info("Style with name " + s.getName().getVal() + ", id '" + s.getStyleId() + "' is default " + s.getType() + " style");
				return s;
			}
		}
		return null;
    }
    
    
	final static String wNamespaceDec = " xmlns:w=\"" + Namespaces.NS_WORD12 + "\""; 

	public final static String rPrDefaultsString = "<w:rPr" + wNamespaceDec + ">"
		// Word 2007 still uses Times New Roman if there is no theme part, and we'd like to replicate that 
        // + "<w:rFonts w:asciiTheme=\"minorHAnsi\" w:eastAsiaTheme=\"minorHAnsi\" w:hAnsiTheme=\"minorHAnsi\" w:cstheme=\"minorBidi\" />"
        + "<w:sz w:val=\"22\" />"
        + "<w:szCs w:val=\"22\" />"
        + "<w:lang w:val=\"en-US\" w:eastAsia=\"en-US\" w:bidi=\"ar-SA\" />"
      + "</w:rPr>";
	public final static String pPrDefaultsString = "<w:pPr" + wNamespaceDec + ">"
	        + "<w:spacing w:after=\"200\" w:line=\"276\" w:lineRule=\"auto\" />"
	      + "</w:pPr>";
	public final static String docDefaultsString = "<w:docDefaults" + wNamespaceDec + ">"
	    + "<w:rPrDefault>"
	    + 	rPrDefaultsString
	    + "</w:rPrDefault>"
	    + "<w:pPrDefault>"
	    + 	pPrDefaultsString
	    + "</w:pPrDefault>"
	  + "</w:docDefaults>";
	
    
    
//	public static void main(String[] args) throws Exception {
//		
//		StyleDefinitionsPart sdp = new StyleDefinitionsPart ();		
//		sdp.initKnownStyles();
//    
//	}    
}
