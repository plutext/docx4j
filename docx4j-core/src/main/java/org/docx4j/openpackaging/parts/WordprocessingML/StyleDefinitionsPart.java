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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.docx4j.XmlUtils;
import org.docx4j.com.google.common.collect.BiMap;
import org.docx4j.com.google.common.collect.HashBiMap;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.McIgnorableNamespaceDeclarator;
import org.docx4j.model.styles.BrokenStyleRemediator;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPartXPathAware;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.DocDefaults;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.Spacing;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Style.BasedOn;
import org.docx4j.wml.Styles;
import org.docx4j.wml.Styles.LatentStyles.LsdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class StyleDefinitionsPart extends JaxbXmlPartXPathAware<Styles> {
	
	private static Logger log = LoggerFactory.getLogger(StyleDefinitionsPart.class);		
	
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
	
	// A variety of pre-defined styles, available for use in a StyleDefinitionsPart.
	private static java.util.Map<String, org.docx4j.wml.Style>  knownStyles = null;
	
	// private PropertyResolver propertyResolver;
	
	@Override
	public void setJaxbElement(Styles jaxbElement) {
		super.setJaxbElement(jaxbElement);
		// Null out cached values which now point to unrelated objects
	    defaultCharacterStyle = null;
	    defaultParagraphStyle = null;
	    defaultTableStyle = null;
	    css=null;
	}
	
    
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
				is = ResourceUtils.getResourceViaProperty("docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart.DefaultStyles",
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
			is = ResourceUtils.getResourceViaProperty("docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart.KnownStyles",
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

	/**
	 * Returns a map of styles defined in docx4j's KnownStyles.xml
	 * (not styles defined in your pkg)
	 * @return
	 */
	public static java.util.Map<String, org.docx4j.wml.Style> getKnownStyles() {
		if (knownStyles==null) {
			initKnownStyles();
		}
		return knownStyles;
	}
    
	
    
    /**
     * @param id
     * @return
     * @since 3.0.0
     */
    public Style getStyleById(String id) {
    	
		return getStyleById( this.getJaxbElement().getStyle(), id ); 				

    }

    /**
     * @param id
     * @return
     * @since 3.0.1
     */
    private static Style getStyleById(List<Style> styles, String id) {
    	
		for ( org.docx4j.wml.Style s : styles ) {	
			
			if (s.getStyleId()==null) {
				BrokenStyleRemediator.remediate(s);
			}
			
			if( s.getStyleId()!=null
					&& s.getStyleId().equals(id) ) {
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
    	// TODO 2019 rethink this: MergeDocx might specifically remove on purpose?
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
    /**
     * if this returns null; invoke createVirtualStylesForDocDefaults() @since 3.2.0 then try again
     * 
     * @return
     */
    public Style getDefaultParagraphStyle() {
    	
    	if (defaultParagraphStyle==null) {
    		defaultParagraphStyle = getDefaultStyle("paragraph");
    	}
    	// OpenOffice conversion to docx
    	// doesn't set default, so use name
    	// (alternatively, could use id=style0)
    	if (defaultParagraphStyle==null) {
    		for ( org.docx4j.wml.Style s : this.getJaxbElement().getStyle() ) {
				if (s.getType() == null) {
					log.warn("style type missing");
				} else if (s.getName() == null) {
					log.warn("style name missing");
				} else if (s.getType().equals("paragraph")
						&& s.getName().getVal().equals("Default") ) {
    				log.debug("Style with name " + s.getName().getVal() + ", id '" + s.getStyleId() + "' is default " + s.getType() + " style");
    				defaultParagraphStyle=s;
    				break;
    			}
    		}    		
    	}
    	// try using id=style0
    	if (defaultParagraphStyle==null) {
    		for ( org.docx4j.wml.Style s : this.getJaxbElement().getStyle() ) {
    			
    			
				if (s.getType() == null) {
					log.warn("style type missing");
				} else if (s.getStyleId() == null) {
					log.warn("style id missing");
				} else if( s.getType().equals("paragraph")
    					&& s.getStyleId().equals("style0") ) {
    				log.debug("Style with name " + s.getName().getVal() + ", id '" + s.getStyleId() + "' is default " + s.getType() + " style");
    				defaultParagraphStyle=s;
    				break;
    			}
    		}    		
    	}
    	if (defaultParagraphStyle==null) {
    		log.warn("No default paragraph style defined");
    	}
    
    	
		return defaultParagraphStyle;
    }
    
    private Style defaultTableStyle;
    /**
     * @since 3.0
     */
    public Style getDefaultTableStyle() {
    	
    	if (defaultTableStyle==null) {
    		defaultTableStyle = getDefaultStyle("table");
    	}
		return defaultTableStyle;
    }
    
    private Style getDefaultStyle(String type) {
    	
		for ( org.docx4j.wml.Style s : this.getJaxbElement().getStyle() ) {	
			if( s.isDefault() && s.getType().equals(type)) {
				log.debug("Style with name " + s.getName().getVal() + ", id '" + s.getStyleId() + "' is default " + s.getType() + " style");
				return s;
			}
		}
		return null;
    }
    
    
	final static String wNamespaceDec = " xmlns:w=\"" + Namespaces.NS_WORD12 + "\""; 

	public final static String rPrDefaultsString = "<w:rPr" + wNamespaceDec + ">"
		// Word 2007 still uses Times New Roman if there is no theme part, and we'd like to replicate that 
        // + "<w:rFonts w:asciiTheme=\"minorHAnsi\" w:eastAsiaTheme=\"minorHAnsi\" w:hAnsiTheme=\"minorHAnsi\" w:cstheme=\"minorBidi\" />"
        + "<w:sz w:val=\"20\" />"  // was 11 prior to 3.01, but Word default in absence of this setting is 10.
        + "<w:szCs w:val=\"20\" />"
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
	

	/**
	 * It is convenient to have a CSS representation of styles,
	 * and this part is a natural place to store it.
	 * 
	 * HtmlCssHelper contains a method createCssForStyles which
	 * relies on StyleTree to generate CSS.  
	 */
	private String css;
	public String getCss() {
		return css;
	}
	public void setCss(String css) {
		this.css = css;
	}
	
	/**
	 * For a run/character style return its linked paragraph style (if any),
	 * or vice versa.
	 * 
	 * @param rStyleVal
	 * @return
	 */
	public Style getLinkedStyle(String rStyleVal) {
		
		Style rStyle = getStyleById(rStyleVal);
		if (rStyle==null) {
			log.info("Couldn't find rStyle " + rStyleVal);
			return null;
		} else {
			// We have a run level style.  Is there a linked pStyle?
			Style.Link linkedStyle = rStyle.getLink();
			if (linkedStyle==null) {
				log.info("No linked style for rStyle " + rStyleVal);							
				return null;
			} else {
				String pStyleVal = linkedStyle.getVal();
				log.debug(rStyleVal + " is linked to style " + pStyleVal );
				Style pStyle = getStyleById(pStyleVal);
				
				if (pStyle==null) {
					log.info("Couldn't find linked pStyle " + pStyleVal 
							+ " for rStyle " + rStyleVal);		
				}
				return pStyle;
			}
		}
	}

	
	/**
	 * Restrict allowed formatting to specified styles.  You don't want to use this directly; 
	 * use the method with the same name in WordprocessingMLPackage
	 * 
	 * Formatting or styles that aren't allowed are not removed.
	 * 
	 * @param allowedStyleNames
	 * @since 3.3.0
	 */
	public void protectRestrictFormatting(List<String> allowedStyleNames, boolean removedNotAllowedFormatting,
			Set<String> stylesInUse) throws Docx4JException {

		// Check preconditions
		if (this.getJaxbElement()==null) 
			throw new Docx4JException("StyleDefinitionsPart null content");
		
		if (this.getJaxbElement().getLatentStyles()==null) 
			throw new Docx4JException("StyleDefinitionsPart content missing latentStyles");
		
		// Set  <w:latentStyles w:defLockedState="1" >
		this.getJaxbElement().getLatentStyles().setDefLockedState(Boolean.TRUE);
		
		
		//We need a map 
		Map<String, LsdException> lsdExceptions = new HashMap<String, LsdException>(); 
		for (LsdException lsdException : this.getJaxbElement().getLatentStyles().getLsdException()) {
			
			lsdExceptions.put(lsdException.getName(), lsdException);
			
			lsdException.setLocked(true); // lock each, until proven otherwise
		}
		
		// Normal is always allowed
		getDefaultParagraphStyle();
		String defaultParagraphStyleName = "Normal";
		if (defaultParagraphStyle!=null) {
			defaultParagraphStyleName=defaultParagraphStyle.getName().getVal();
		}
		LsdException defaultLsd = lsdExceptions.get(defaultParagraphStyleName);
		if (defaultLsd==null) {
			log.warn("No lsdException for " + defaultParagraphStyleName);
		} else {
			defaultLsd.setLocked(false);
		}
		
		// OK, now the others
		for (String styleName : allowedStyleNames) {

			LsdException lsdEx = lsdExceptions.get(styleName);
			if (lsdEx==null) {
				log.debug("No lsdException for " + styleName);
			} else {
				lsdEx.setLocked(false);
			}			
		}
		
		/*
		 * In Word 2010, if you say you just want h2 and h3, that's what
		 * you'll see in the Word UI.
		 * 
		 * But if h1 and Title are in use, the styles part will contain:
		 * 
			    <w:lsdException w:name="heading 1" w:locked="0" w:semiHidden="0" w:uiPriority="9" w:unhideWhenUsed="0" w:qFormat="1"/>
			    <w:lsdException w:name="heading 2" w:locked="0" w:uiPriority="9" w:qFormat="1"/>
			    <w:lsdException w:name="heading 3" w:locked="0" w:uiPriority="9" w:qFormat="1"/>
			    :
			    <w:lsdException w:name="Title" w:locked="0" w:semiHidden="0" w:uiPriority="10" w:unhideWhenUsed="0" w:qFormat="1"/>
			    
		 * ie h1 and Title also have w:locked="0"!  But w:semiHidden="0" and w:uiPriority="10"  
		 * 
		 * That's if you say remove yes.  If you say remove no, 
		 * 
			    <w:lsdException w:name="heading 1" w:locked="0" w:semiHidden="0" w:uiPriority="9" w:unhideWhenUsed="0" w:qFormat="1"/>
			    <w:lsdException w:name="Title" w:locked="0" w:semiHidden="0" w:uiPriority="10" w:unhideWhenUsed="0" w:qFormat="1"/>	
    
    	 * What's the algorithm for setting w:uiPriority?
    	 * 
    	 * In either case, the style is kept, but set to locked.
    	 * 
    	 * So in either case, do the same thing here. Where we differ is whether we remove the style from the content or not.
    	 */ 		
		for (String styleName : stylesInUse) {

			LsdException lsdEx = lsdExceptions.get(styleName);
			if (lsdEx==null) {
				log.debug("No lsdException for " + styleName);
			} else {
				lsdEx.setLocked(false);
				lsdEx.setSemiHidden(false);
			}			
		}
		
		// Remove style definitions which aren't in the list;
		// restricting formatting doesn't work if any styles are listed!
		allowedStyleNames.add(defaultParagraphStyleName);
		List<Style> deletions = new ArrayList<Style>(); 
		for (Style s : this.getJaxbElement().getStyle()) {
			
			if (!allowedStyleNames.contains(s.getName().getVal())) {

				if (stylesInUse.contains(s.getName().getVal())) {
					
					s.setLocked(new BooleanDefaultTrue());
					
				} else {				
					deletions.add(s);
				}
			}
		}
		this.getJaxbElement().getStyle().removeAll(deletions);
		
	}
	
	private BiMap<String,String> styleIdToName; // = HashBiMap.create();
	private BiMap<String,String> styleNameToId;
	
	/**
	 * Given the ID of a style known in this part, return the corresponding style name.
	 * 
	 * @param id
	 * @return
	 * @since 3.3.0
	 */
	public String getNameForStyleID(String id) {
		
		if (styleIdToName==null) {
			refreshNameIdBiMaps();
		}
		
		return styleIdToName.get(id);
	}
	
	/**
	 * Given the name of a style known in this part, return the corresponding style ID.
	 * 
	 * @param name
	 * @return
	 * @since 3.3.0
	 */
	public String getIDForStyleName(String name) {

		if (styleIdToName==null) {
			refreshNameIdBiMaps();
		}
		
		return styleNameToId.get(name);
	}

	/**
	 * Refresh the style name - ID bimaps, based on styles currently defined in this part.
	 * @since 3.3.0
	 */
	public void refreshNameIdBiMaps() {
		
		styleIdToName= HashBiMap.create();
		for (Style s : this.getJaxbElement().getStyle()) {
			if (s.getName()==null
					|| s.getName().getVal()==null) {
				log.info("style has no name!");
			} else if (s.getStyleId()==null
					|| s.getStyleId().trim().length()==0) {
				log.info("style has no id!");				
			} else {
				styleIdToName.put(s.getStyleId(), s.getName().getVal());
			}
		}
		styleNameToId = styleIdToName.inverse();
	}
	
    
//	public static void main(String[] args) throws Exception {
//		
//		StyleDefinitionsPart sdp = WordprocessingMLPackage.createPackage().getMainDocumentPart().getStyleDefinitionsPart();
//		sdp.refreshNameIdBiMaps();
//		for (String s : sdp.styleNameToId.keySet()) {
//			System.out.println(s + " to " + sdp.styleNameToId.get(s));
//		}
//		for (String s : sdp.styleIdToName.keySet()) {
//			System.out.println(s + " to " + sdp.styleIdToName.get(s));
//		}    
//	}    
}
