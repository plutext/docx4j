/*
 *  Copyright 2013-2016, Plutext Pty Ltd.
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
package org.docx4j.toc;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TocStyles {
	
	private static Logger log = LoggerFactory.getLogger(TocStyles.class);		
	
    protected static final String TOC_STYLE_MASK = "toc %s";
    

	private static Map<String, Style> defaultToCStyles = new HashMap<String, Style>(); 
	
	static {
		// configure backup definitions of toc 1-9, 
		// in case these aren't present the docx styles.xml
		
		try {
			InputStream is = getResourceViaProperty("docx4j.toc.TocStyles.xml",
					"org/docx4j/toc/TocStyles.xml");
			Styles defaultStyles = (Styles)XmlUtils.unmarshal(is);
			for (Style s : defaultStyles.getStyle()) {
				defaultToCStyles.put(s.getName().getVal(), s);
			}
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
	}
	
	public static TocStyles getTocStyles(MainDocumentPart documentPart) throws TocException {
//      Styles styles = null;
      if (documentPart.getStyleDefinitionsPart()==null
      		|| documentPart.getStyleDefinitionsPart().getJaxbElement()==null) {
      	throw new TocException("No StyleDefinitions present in package");
      }
      return new TocStyles(documentPart.getStyleDefinitionsPart());
		
	}
	
	
	// needed until 3.2.0, since that's when this was introduced into docx4j.
    private static java.io.InputStream getResourceViaProperty(String propName, String defaultPath) throws java.io.IOException
    {
    	String resourcePath= Docx4jProperties.getProperty(propName, defaultPath);
    	log.debug(propName + " resolved to " + resourcePath);
    	InputStream resourceIS = null;
    	try {
    		resourceIS = ResourceUtils.getResource(resourcePath);
    	} catch (IOException ioe) {
    		log.warn(resourcePath + ": " + ioe.getMessage());
    	}
    	if (resourceIS==null) {
    		log.warn("Property " + propName + " resolved to missing resource " + resourcePath + "; using " +  defaultPath);
    		return ResourceUtils.getResource(defaultPath);
    	} else {
    		return resourceIS;
    	}
    }
	
	/**
	 * style name (eg toc 1) to style ID (eg Sumrio1) 
	 */
	private Map<String, String> styleNameToID = new HashMap<String, String>(); 
	
	
	/**
	 * this docx's styles part styles, in case we need to 
	 * add ToC style definitions to it.
	 */
	private Styles stylesPartStyles;
		
	protected TocStyles(StyleDefinitionsPart sdp) {
		
		this.stylesPartStyles = sdp.getJaxbElement();
		
		/*  A style looks like:
		 * 
		 *   <w:style w:type="paragraph" w:styleId="Sumrio1">
    	 *		<w:name w:val="toc 1"/>
    	 *
		 *  In other words, irrespective of the language, the name
		 *  remains in English.
		 */
		
		String defaultPStyleName = null;
		if (sdp.getDefaultParagraphStyle()!=null) {
			defaultPStyleName=sdp.getDefaultParagraphStyle().getName().getVal();
			defaultPStyleId = sdp.getDefaultParagraphStyle().getStyleId();
		}
		
		for (Style s : stylesPartStyles.getStyle()) {
			
			if (s.getName().getVal().startsWith("toc")
					|| s.getName().getVal().equals(defaultPStyleName)
					|| s.getName().getVal().equals(TOC_HEADING)
					|| s.getName().getVal().equals(HEADING_1) // in order to create a TOC Heading, if necessary
					) {
				
				styleNameToID.put(s.getName().getVal(), s.getStyleId());
				
			}
		}
		
		// Proactively add toc1-9
		for (int i=1; i<=9; i++) {
			getStyleIdForName("toc " + i);
		}
		
		// Proactively add hyperlink
		getStyleIdForName("Hyperlink");		
				
	}
	
	public final static String TOC_HEADING = "TOC Heading";
	public final static String HEADING_1 = "heading 1";
	
	private String defaultPStyleId=null;

	/**
	 * Get the style ID corresponding to this name.  If it isn't
	 * in the docx styles.xml part, use our default definition to add it.
	 * 
	 * @param name
	 * @param documentStyles
	 * @return
	 */
	protected String getStyleIdForName(String name) {
		
		String id = styleNameToID.get(name);
		
		log.debug(name + " --> " + id);
		
		if (id!=null) {  // style is defined in this docx
			return id;
		}
		
		log.info(name + " style not defined in styles part; falling back to configured default");
		Style s = defaultToCStyles.get(name);
		if (s==null) {
			log.warn("No default style defined with name " + name);
			return null;
		} else {
			// add a clone
			Style clone = XmlUtils.deepCopy(s);
			stylesPartStyles.getStyle().add(clone);
			
			// and now we know for next time
			styleNameToID.put(name, clone.getStyleId());
			log.info("Added a styles definition for " + name);
			
			// Remember, generally speaking, style name is standardised and English,
			// but style id is localised, and basedOn is an IDREF.
			
			// Our default TOC styles assume a style with ID "Normal" is present.
			// There are 2 cases to consider:
			// Case 1: user is using default TOC styles, with a non-English docx (so their document default style is something else)
			// Case 2: user has substituted their own TOC styles (ie with localised style id).
			// We don't worry about case 2 here.  User is responsible for ensuring the TOC styles are consistent with their document
			// (ie are basedOn a style id present in their styles part)
			
			// For case 1
			if (clone.getBasedOn()!=null
					&& clone.getBasedOn().getVal().equals("Normal") /* the style id used in basedOn in our default styles */ 
					&& defaultPStyleId!=null
					&& !defaultPStyleId.equals("Normal")) {
				// add it
//					stylesPartStyles.getStyle().add(
//							(Style)XmlUtils.unmarshalString(normalXml) );
					
				clone.getBasedOn().setVal(defaultPStyleId);
			}
			
			
			
			return styleNameToID.get(name);
		}
	}
	
	// TODO FIXME Don't define these defaults if the docx already contains defaults!
	
//	private final static String normalXml = "<w:style w:default=\"1\" w:styleId=\"Normal\" w:type=\"paragraph\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
//            + "<w:name w:val=\"Normal\"/>"
//            + "<w:qFormat/>"
//        +"</w:style>";
	
//	private final static String defaultParagraphFontXml = "<w:style w:default=\"1\" w:styleId=\"DefaultParagraphFont\" w:type=\"character\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
//            + "<w:name w:val=\"Default Paragraph Font\"/>"
//        +"</w:style>";
	
}
