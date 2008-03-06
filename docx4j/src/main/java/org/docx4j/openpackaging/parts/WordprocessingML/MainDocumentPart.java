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



import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import org.docx4j.dml.BaseStyles;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Body;

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


/**
 * @author jharrop
 *
 */
public class MainDocumentPart extends DocumentPart  {
	
	private static Logger log = Logger.getLogger(MainDocumentPart.class);
		
	
	public MainDocumentPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}
	public MainDocumentPart() throws InvalidFormatException {
		super(new PartName("/word/document.xml"));
		init();
	}
		
	public void init() {
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

    
    /**
     * Traverse the document, looking for fonts which have been applied, either
     * directly, or via a style. 
     * 
     * @return
     */
    public Map fontsInUse() {
    	
    // Setup 
    	
    	Map fontsDiscovered = new java.util.HashMap();
    	
    	// Keep track of styles we encounter, so we can
    	// inspect these for fonts
    	Map stylesInUse = new java.util.HashMap();

		org.docx4j.wml.Styles styles = (org.docx4j.wml.Styles)this.getStyleDefinitionsPart().getJaxbElement();
		
		// It is convenient to have a HashMap of styles
		Map stylesDefined = new java.util.HashMap();
	     for (Iterator iter = styles.getStyle().iterator(); iter.hasNext();) {
	            org.docx4j.wml.Styles.Style s = (org.docx4j.wml.Styles.Style)iter.next();
	            stylesDefined.put(s.getStyleId(), s);
	     }
    // We need to know what fonts and styles are used in the document
    	
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)this.getJaxbElement();
		Body body =  wmlDocumentEl.getBody();

		List <Object> bodyChildren = body.getBlockLevelElements();
		
		traverseMainDocumentRecursive(bodyChildren, fontsDiscovered, stylesInUse); 

	// Add default font
		String defaultFont = getDefaultFont(); 
		log.debug("fontsDiscovered.put:" + defaultFont);
		fontsDiscovered.put( defaultFont, defaultFont  );
		
	// Add fonts used in the styles we discovered
		Iterator it = stylesInUse.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        String styleName = (String)pairs.getKey();
	        log.debug("Inspecting style: " + styleName );
            org.docx4j.wml.Styles.Style existingStyle = (org.docx4j.wml.Styles.Style)stylesDefined.get(styleName);
            if (existingStyle!=null) {
            	String fontName = getFontnameFromStyle(stylesDefined, existingStyle); 
            	log.debug(styleName + " uses font " + fontName);
            	fontsDiscovered.put(fontName, fontName);
            } else {
            	log.error("Couldn't find used style " + styleName + "in styles part!");
            }
	    }
		    	
		return fontsDiscovered;
    }
    
	private org.docx4j.dml.BaseStyles.FontScheme fontScheme = null;
    private void setFontScheme() {
    	
    	if (fontScheme==null) {  // ie we haven't done this already
    		if (this.getThemePart() != null) {
				org.docx4j.dml.Theme theme = 
					(org.docx4j.dml.Theme) this.getThemePart().getJaxbElement();
				if (theme.getThemeElements() != null
						&& theme.getThemeElements().getFontScheme() != null) {
					fontScheme = theme.getThemeElements().getFontScheme();
				}
			}
    	}
    	
    }
    
	/**
	 * Returns default document font, by attempting to look at styles/docDefaults/rPrDefault/rPr/rFonts.
	 * 
	 * @return default document font. 
	 */
	public String getDefaultFont() {
		
		// First look at the defaults
		// 3 look at styles/rPrDefault 
		// 3.1 if there is an rFonts element, do what it says (it may refer you to the theme part, 
		//     in which case if there is no theme part, default to "internally stored settings"
		//	   (there is no normal.dot; see http://support.microsoft.com/kb/924460/en-us ) 
		//	   in this case Calibri and Cambria)
		// 3.2 if there is no rFonts element, default to Times New Roman.
		org.docx4j.wml.Styles styles = (org.docx4j.wml.Styles)this.getStyleDefinitionsPart().getJaxbElement();
		org.docx4j.wml.Styles.DocDefaults docDefaults = styles.getDocDefaults(); 		
		
		if (docDefaults!=null) {			
			org.docx4j.wml.Styles.DocDefaults.RPrDefault rPrDefault = docDefaults.getRPrDefault(); 		
			if (rPrDefault!=null) {
				org.docx4j.wml.RPr rPr = rPrDefault.getRPr();
				if ( rPr!=null ) {
					org.docx4j.wml.RPr.RFonts rFonts = rPr.getRFonts();
					if (rFonts!=null) {
						
						// Usual case
						if (rFonts.getAsciiTheme()!=null ) {
							// for example minorHAnsi, which I think translates to minorFont/latin 
							if (rFonts.getAsciiTheme().equals(org.docx4j.wml.ThemeFontEnumeration.MINOR_H_ANSI)) {
								if (this.getThemePart()!=null) {
									setFontScheme();
									if (fontScheme.getMinorFont()!=null
											&& fontScheme.getMinorFont().getLatin()!=null) {
																				
										org.docx4j.dml.TextFont textFont = fontScheme.getMinorFont().getLatin();
										log.info("minorFont/latin font is " + textFont.getTypeface() );
										return textFont.getTypeface(); 
									} else {
										// No minorFont/latin in theme part - default to Calibri
										log.info("No minorFont/latin in theme part - default to Calibri");								
										return "Calibri"; 
									}
								} else {
									// No theme part - default to Calibri
									log.info("No theme part - default to Calibri");
									return "Calibri"; 
								}
							} else {
								// TODO
								log.error("Don't know how to handle: "
										+ rFonts.getAsciiTheme());
								return null;
							}
						} else if (rFonts.getAscii()!=null ) {
							log.info("rPrDefault/rFonts referenced " + rFonts.getAscii());								
							return rFonts.getAscii(); 							
						} else {
							// TODO
							log.error("Neither ascii or asciTheme.  What to do? ");
							return null;
						}						
					} else {
						log.info("No styles/docDefaults/rPrDefault/rPr/rFonts - default to Times New Roman");
						// Yes, Times New Roman is still buried in Word 2007
						return "Times New Roman"; 						
					}
				} else {
					log.info("No styles/docDefaults/rPrDefault/rPr - default to Times New Roman");
					// Yes, Times New Roman is still buried in Word 2007
					return "Times New Roman"; 
					
				}
			} else {
				log.info("No styles/docDefaults/rPrDefault/rPr - default to Times New Roman");
				// Yes, Times New Roman is still buried in Word 2007
				return "Times New Roman"; 				
			}
		} else {
			log.info("No styles/docDefaults - default to Times New Roman");
			// Yes, Times New Roman is still buried in Word 2007
			return "Times New Roman"; 							
		}
	}


    /**
     * Determine the font used in this style, using the inheritance rules.
     * 
     * @return the font name, or null if there is no rFonts element in any style
     * in the style inheritance hierarchy (ie
     * this method does not look up styles/docDefaults/rPrDefault/rPr/rFonts
     * or from there, the theme part - see getDefaultFont to do that).
     * 
     * @see getDefaultFont
     */	
    public String getFontnameFromStyle(org.docx4j.wml.Styles.Style style) {

		org.docx4j.wml.Styles styles = (org.docx4j.wml.Styles)this.getStyleDefinitionsPart().getJaxbElement();

		// It is convenient to have a HashMap of styles
		Map stylesDefined = new java.util.HashMap();
	     for (Iterator iter = styles.getStyle().iterator(); iter.hasNext();) {
	            org.docx4j.wml.Styles.Style s = (org.docx4j.wml.Styles.Style)iter.next();
	            stylesDefined.put(s.getStyleId(), s);
	     }
	     
	    return getFontnameFromStyle(stylesDefined, style);
    }
    /**
     * 
     * @return the font name, or null if there is no rFonts element in any style
     * in the style inheritance hierarchy (ie
     * this method does not look up styles/docDefaults/rPrDefault/rPr/rFonts
     * or from there, the theme part).
     */
    private String getFontnameFromStyle(Map stylesDefined, org.docx4j.wml.Styles.Style style) {
    	
		/*
		a paragraph style does not inherit anything from its linked character style.

		A linked character style seems to be just a Word 2007 user interface
		hint.  ie if you select some characters in a paragraph and select to
		apply "Heading 1", what you are actually doing is applying "Heading 1
		char".  This is determined by looking at the definition of the
		"Heading 1" style to see what its linked style is.
		
		(Interestingly, in Word 2007, if you right click to modify something 
		 which is Heading 1 char, it modifies both the Heading 1 style and the
		 Heading 1 char style!.  Haven't looked to see what happens to Heading 1 char
		 style if you right click to modify a Heading 1 par.)

		 The algorithm Word 2007 seems to use is:
		    look at the specified style:
		        1 does it have its own rPr which contains rFonts?
		        2 if not, what does this styles basedOn style say? (Ignore
		any linked char style)
				3 look at styles/rPrDefault 
				3.1 if there is an rFonts element, do what it says (it may refer you to the theme part, 
				    in which case if there is no theme part, default to "internally stored settings"
					(there is no normal.dot; see http://support.microsoft.com/kb/924460/en-us ) 
					in this case Calibri and Cambria)
				3.2 if there is no rFonts element, default to Times New Roman.
		
		
		For efficiency reasons, we don't do 3 in this method.
		 */
    	
    	setFontScheme();

        // 1 does it have its own rPr which contains rFonts?
    	org.docx4j.wml.RPr rPr = style.getRPr();
    	if (rPr!=null && rPr.getRFonts()!=null) {
    		if (rPr.getRFonts().getAscii()!=null) {
        		return rPr.getRFonts().getAscii();
    		} else if (rPr.getRFonts().getAsciiTheme()!=null 
    					&& this.getThemePart() != null) {
    			log.debug("Encountered rFonts/AsciiTheme: " + rPr.getRFonts().getAsciiTheme() );
    			
				org.docx4j.dml.Theme theme = (org.docx4j.dml.Theme)this.getThemePart().getJaxbElement();
				if (rPr.getRFonts().getAsciiTheme().equals(org.docx4j.wml.ThemeFontEnumeration.MINOR_H_ANSI)) {
					if (fontScheme != null && fontScheme.getMinorFont().getLatin() != null) {
						fontScheme = theme.getThemeElements().getFontScheme();
						org.docx4j.dml.TextFont textFont = fontScheme.getMinorFont().getLatin();
						log.info("minorFont/latin font is " + textFont.getTypeface());
						return (textFont.getTypeface());
					} else {
						// No minorFont/latin in theme part - default to Calibri
						log.info("No minorFont/latin in theme part - default to Calibri");
						return ("Calibri");
					}
				} else if (rPr.getRFonts().getAsciiTheme().equals(org.docx4j.wml.ThemeFontEnumeration.MAJOR_H_ANSI)) {
					if (fontScheme != null && fontScheme.getMajorFont().getLatin() != null) {
						fontScheme = theme.getThemeElements().getFontScheme();
						org.docx4j.dml.TextFont textFont = fontScheme.getMajorFont().getLatin();
						log.info("majorFont/latin font is " + textFont.getTypeface());
						return (textFont.getTypeface());
					} else {
						// No minorFont/latin in theme part - default to Cambria
						log.info("No majorFont/latin in theme part - default to Cambria");
						return ("Cambria");
					}
				} else {
					log.error("Don't know how to handle: "
							+ rPr.getRFonts().getAsciiTheme());
				}
    		}
    	}
        		
        // 2 if not, what does this styles basedOn style say? (recursive)
    	
    	if (style.getBasedOn()!=null && style.getBasedOn().getVal()!=null) {
        	String basedOnStyleName = style.getBasedOn().getVal();    		
    		log.debug("recursing into basedOn:" + basedOnStyleName);
            org.docx4j.wml.Styles.Style candidateStyle = (org.docx4j.wml.Styles.Style)stylesDefined.get(basedOnStyleName);
            if (candidateStyle != null && candidateStyle.getStyleId().equals(basedOnStyleName)) {
            	return getFontnameFromStyle(stylesDefined, candidateStyle);
            }
    	     // If we get here the style is missing!
     		log.error("couldn't find basedOn:" + basedOnStyleName);    	     
    	     return null;
    	} else {
    		log.debug("No basedOn set for: " + style.getStyleId() );
    		return null;
    	}
    	
    }
    
	void traverseMainDocumentRecursive(List <Object> children, Map fontsDiscovered, Map stylesInUse){
		
		for (Object o : children ) {
						

			if (o instanceof org.docx4j.wml.R) {

				org.docx4j.wml.R run = (org.docx4j.wml.R) o;
				if (run.getRPr() != null) {
					inspectRPr(run.getRPr(), fontsDiscovered, stylesInUse);
				}

				// don't need to traverse run.getRunContent()

			} else if ( ((JAXBElement)o).getDeclaredType().getName().equals("org.docx4j.wml.P") ) {
				org.docx4j.wml.P p = (org.docx4j.wml.P)((JAXBElement)o).getValue();
				
				if (p.getPPr()!=null && p.getPPr().getPStyle()!=null ) {
					// Note this paragraph style
		    		System.out.println("put style " + p.getPPr().getPStyle().getVal() );					
					stylesInUse.put(p.getPPr().getPStyle().getVal(), p.getPPr().getPStyle().getVal());
				}

				if (p.getPPr()!=null && p.getPPr().getRPr() !=null ) {
					// Inspect RPr
					inspectRPr(p.getPPr().getRPr(), fontsDiscovered, stylesInUse);
				}
								
				traverseMainDocumentRecursive(p.getParagraphContent(), fontsDiscovered, stylesInUse);
								
			}   /* else if ( o instanceof javax.xml.bind.JAXBElement) {
				System.out.println("      " +  ((JAXBElement)o).getName() );
				System.out.println("      " +  ((JAXBElement)o).getDeclaredType().getName());
				
				// TODO - unmarshall directly to Text.
				if ( ((JAXBElement)o).getDeclaredType().getName().equals("org.docx4j.wml.Text") ) {
					org.docx4j.wml.Text t = (org.docx4j.wml.Text)((JAXBElement)o).getValue();
					System.out.println("      " +  t.getValue() );					
				}
				
			} else if ( o instanceof org.apache.xerces.dom.ElementNSImpl) {
				System.out.println("      " +  ((org.apache.xerces.dom.ElementNSImpl)o).getNodeName() );					
			else {
				System.out.println( o.getClass().getName() );
				System.out.println( ((JAXBElement)o).getName() );
				System.out.println( ((JAXBElement)o).getDeclaredType().getName() + "\n\n");
			} */
		}
	}
	
    private void inspectRPr(org.docx4j.wml.RPr rPr, Map fontsDiscovered, Map stylesInUse) {
    	if (rPr.getRFonts()!=null) {
    		// 	Note the font - just Ascii for now
    		System.out.println("put font " + rPr.getRFonts().getAscii());
    		fontsDiscovered.put(rPr.getRFonts().getAscii(), rPr.getRFonts().getAscii());
    	}
    	if (rPr.getRStyle()!=null) {
    		// 	Note this run style
    		System.out.println("put style " + rPr.getRStyle().getVal() );
    		stylesInUse.put(rPr.getRStyle().getVal(), rPr.getRStyle().getVal());
    	}
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

	
