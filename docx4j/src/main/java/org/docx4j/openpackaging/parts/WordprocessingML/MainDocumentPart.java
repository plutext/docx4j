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



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Body;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;


/**
 * @author jharrop
 *
 */
public class MainDocumentPart extends DocumentPart<org.docx4j.wml.Document>  {
	
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
	
    private PropertyResolver propertyResolver;
	public PropertyResolver getPropertyResolver() {
		if (propertyResolver==null) {
			try {
				propertyResolver = new PropertyResolver( (WordprocessingMLPackage)this.pack);
			} catch (Docx4JException e) {
				e.printStackTrace();
			}			
		}
		return propertyResolver;
	}
	
	private StyleTree styleTree;
	public StyleTree getStyleTree() {
		
		if (styleTree==null) {

			List<String> stylesInUse = new ArrayList<String>();
			Iterator it = getStylesInUse().entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pairs = (Map.Entry)it.next();
		        String styleId = (String)pairs.getKey();
		        stylesInUse.add(styleId);
		    }
		    
//	    	if (!stylesInUse.contains("Normal") ) {
//	    		stylesInUse.add("Normal");
//	    	}
//	    	if (!stylesInUse.contains("DefaultParagraphFont") ) {
//	    		stylesInUse.add("DefaultParagraphFont");
//	    	}	
		    
		    try {
				getStyleDefinitionsPart().createVirtualStylesForDocDefaults();
			} catch (Docx4JException e) {
				// Shouldn't happen, so catch here
				log.error(e);
			}
	    	
			Map<String, Style> allStyles = new HashMap<String, Style>();
			Styles styles = getStyleDefinitionsPart().getJaxbElement();		
			for ( org.docx4j.wml.Style s : styles.getStyle() ) {				
				allStyles.put(s.getStyleId(), s);	
				log.debug("live style: " + s.getStyleId() );
			}
			styleTree = new StyleTree(stylesInUse, allStyles);
				
		}
		return styleTree;
		
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
	@Override
    public org.docx4j.wml.Document unmarshal( java.io.InputStream is ) throws JAXBException {
    	
		try {
		    		    
			Unmarshaller u = jc.createUnmarshaller();

			//u.setSchema(org.docx4j.jaxb.WmlSchema.schema);			
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
			
//			JAXBElement<?> root = (JAXBElement<?>)u.unmarshal( is );			
//			jaxbElement = (org.docx4j.wml.Document)root.getValue();
			
			jaxbElement =  (org.docx4j.wml.Document) u.unmarshal( is );
			return jaxbElement;
			
			//System.out.println("\n\n" + this.getClass().getName() + " unmarshalled \n\n" );									

		} catch (Exception e ) {
			e.printStackTrace();
			return null;
		}
    	
    	
    }

    
    /**
     * Traverse the document, looking for fonts which have been applied, either
     * directly, or via a style. 
     * 
     * @return
     */
    public Map fontsInUse() {
    	
    // Setup 
    	
    	Map<String, String> fontsDiscovered = new java.util.HashMap<String, String>();
    	
    	// Keep track of styles we encounter, so we can
    	// inspect these for fonts
    	Map<String, String> stylesInUse = new java.util.HashMap<String, String>();

		org.docx4j.wml.Styles styles = null;
		if (this.getStyleDefinitionsPart()!=null) {
			styles = (org.docx4j.wml.Styles)this.getStyleDefinitionsPart().getJaxbElement();			
		}
		// It is convenient to have a HashMap of styles
		Map<String, Style> stylesDefined = new java.util.HashMap<String, Style>();
		if (styles!=null) {
		     for (Iterator<Style> iter = styles.getStyle().iterator(); iter.hasNext();) {
		            Style s = iter.next();
		            stylesDefined.put(s.getStyleId(), s);
		     }
		}
    // We need to know what fonts and styles are used in the document
    	
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)this.getJaxbElement();
		Body body =  wmlDocumentEl.getBody();

		List <Object> bodyChildren = body.getEGBlockLevelElts();
		
		traverseMainDocumentRecursive(bodyChildren, fontsDiscovered, stylesInUse); 

	// Add default font
		//String defaultFont = PropertyResolver.getDefaultFont(this.getStyleDefinitionsPart(), this.getThemePart());
//		String defaultFont = getPropertyResolver().getDefaultFont();
//		log.debug("fontsDiscovered.put:" + defaultFont);
//		fontsDiscovered.put( defaultFont, defaultFont  );
		fontsDiscovered.put( ((WordprocessingMLPackage)pack).getDefaultFont(), ((WordprocessingMLPackage)pack).getDefaultFont() );
		
	// Add fonts used in the styles we discovered
		Iterator it = stylesInUse.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        String styleName = (String)pairs.getKey();
	        log.debug("Inspecting style: " + styleName );
            org.docx4j.wml.Style existingStyle = (org.docx4j.wml.Style)stylesDefined.get(styleName);
            if (existingStyle!=null) {
            	String fontName = getPropertyResolver().getFontnameFromStyle(stylesDefined, this.getThemePart(), existingStyle); 
            	log.debug(styleName + " uses font " + fontName);
            	fontsDiscovered.put(fontName, fontName);
            } else {
            	log.error("Couldn't find used style " + styleName + "in styles part!");
            }
	    }
		    	
		return fontsDiscovered;
    }
    

	/**
	 * Traverse the document, and return a map of all styles which are used
	 * directly in the document.  (IE this does not include styles on which 
	 * others are just BasedOn).
	 * @return
	 */
	public Map<String, String> getStylesInUse(){

		Map<String, String> stylesInUse = new HashMap<String, String>();
		
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)this.getJaxbElement();
		Body body =  wmlDocumentEl.getBody();

		List <Object> bodyChildren = body.getEGBlockLevelElts();
		
		traverseMainDocumentRecursive(bodyChildren, null, stylesInUse);
		
		return stylesInUse;
	}
    
	private void traverseMainDocumentRecursive(List <Object> children, Map fontsDiscovered, Map<String, String> stylesInUse){
		
		for (Object o : children ) {
						
			//log.debug("object: " + o.getClass().getName() );
			
			if (o instanceof org.docx4j.wml.P) {
				
				org.docx4j.wml.P p = (org.docx4j.wml.P) o;
		
				if (stylesInUse !=null && p.getPPr() != null && p.getPPr().getPStyle() != null) {
					// Note this paragraph style
					log.debug("put style "
							+ p.getPPr().getPStyle().getVal());
					stylesInUse.put(p.getPPr().getPStyle().getVal(), p
							.getPPr().getPStyle().getVal());
				}
		
				if (p.getPPr() != null && p.getPPr().getRPr() != null) {
					// Inspect RPr
					inspectRPr(p.getPPr().getRPr(), fontsDiscovered,
							stylesInUse);
				}
		
				traverseMainDocumentRecursive(p.getParagraphContent(),
						fontsDiscovered, stylesInUse);
		
			} else if (o instanceof org.docx4j.wml.SdtBlock) {

				org.docx4j.wml.SdtBlock sdt = (org.docx4j.wml.SdtBlock) o;
				
				// Don't bother looking in SdtPr
				
				traverseMainDocumentRecursive(sdt.getSdtContent().getEGContentBlockContent(),
						fontsDiscovered, stylesInUse);
				
//			} else if (o instanceof org.docx4j.wml.SdtContentBlock) {
//
//				org.docx4j.wml.SdtBlock sdt = (org.docx4j.wml.SdtBlock) o;
//				
//				// Don't bother looking in SdtPr
//				
//				traverseMainDocumentRecursive(sdt.getSdtContent().getEGContentBlockContent(),
//						fontsDiscovered, stylesInUse);
				
			} else if (o instanceof org.docx4j.wml.R) {

				org.docx4j.wml.R run = (org.docx4j.wml.R) o;
				if (run.getRPr() != null) {
					inspectRPr(run.getRPr(), fontsDiscovered, stylesInUse);
				}

				// need to traverse run.getRunContent() for w:sym only
				traverseMainDocumentRecursive(run.getRunContent(),
						fontsDiscovered, stylesInUse);
				
			} else if (o instanceof org.w3c.dom.Node) {
				
				// If Xerces is on the path, this will be a org.apache.xerces.dom.NodeImpl;
				// otherwise, it will be com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
				
				// Ignore these, eg w:bookmarkStart
				
				log.debug("not traversing into unhandled Node: " + ((org.w3c.dom.Node)o).getNodeName() );

			} else if ( o  instanceof org.docx4j.wml.Tbl) {
				// A table can be either this or a JAXBElement 
				// An existing table we have unmarshalled will be 
				// a JAXBElement; one we have just created
				// via object factory will be a naked
				// org.docx4j.wml.Tbl
				inspectTable( (org.docx4j.wml.Tbl)o, fontsDiscovered, stylesInUse );
				
			} else if ( o instanceof javax.xml.bind.JAXBElement) {

				if ( ((JAXBElement)o).getDeclaredType().getName().equals("org.docx4j.wml.Text") ) {
					
					// Ignore
					
				} else if ( ((JAXBElement)o).getDeclaredType().getName().equals("org.docx4j.wml.Tbl") ) {
					
					org.docx4j.wml.Tbl tbl = (org.docx4j.wml.Tbl)((JAXBElement)o).getValue();						
					inspectTable(tbl, fontsDiscovered, stylesInUse );
					
				} else if ( ((JAXBElement)o).getDeclaredType().getName().equals("org.docx4j.wml.P$Hyperlink") ) {
						
					org.docx4j.wml.P.Hyperlink hyperlink = (org.docx4j.wml.P.Hyperlink)((JAXBElement)o).getValue();							
					traverseMainDocumentRecursive(hyperlink.getParagraphContent(),
							fontsDiscovered, stylesInUse);
				} else if ( ((JAXBElement)o).getDeclaredType().getName().equals("org.docx4j.wml.R$Sym") ) {

					org.docx4j.wml.R.Sym sym = (org.docx4j.wml.R.Sym)((JAXBElement)o).getValue(); 
					fontsDiscovered.put(sym.getFont(), sym.getFont());

				} else if ( log.isDebugEnabled() ){
					log.debug( XmlUtils.JAXBElementDebug((JAXBElement)o) );
				}
					
					
					
//				if (((JAXBElement) o).getDeclaredType().getName().equals(
//						"org.docx4j.wml.P")) {
//					org.docx4j.wml.P p = (org.docx4j.wml.P) ((JAXBElement) o)
//							.getValue();
//
//					if (p.getPPr() != null && p.getPPr().getPStyle() != null) {
//						// Note this paragraph style
//						log.debug("put style "
//								+ p.getPPr().getPStyle().getVal());
//						stylesInUse.put(p.getPPr().getPStyle().getVal(), p
//								.getPPr().getPStyle().getVal());
//					}
//
//					if (p.getPPr() != null && p.getPPr().getRPr() != null) {
//						// Inspect RPr
//						inspectRPr(p.getPPr().getRPr(), fontsDiscovered,
//								stylesInUse);
//					}
//
//					traverseMainDocumentRecursive(p.getParagraphContent(),
//							fontsDiscovered, stylesInUse);
//
//				}
				
			} else {
				log.error( "UNEXPECTED: " + o.getClass().getName() );
			} 
		}
	}
	
	private void inspectTable( org.docx4j.wml.Tbl tbl, Map fontsDiscovered, Map stylesInUse) {
		
		// The table could have a table style;
		// Tables created in Word 2007 default to table style "TableGrid",
		// which is based on "TableNormal".
		if (tbl.getTblPr()!=null 
				&& tbl.getTblPr().getTblStyle()!=null) {
			log.debug("Adding table style: " + tbl.getTblPr().getTblStyle().getVal() );
			stylesInUse.put(tbl.getTblPr().getTblStyle().getVal(),
							tbl.getTblPr().getTblStyle().getVal() );
		}
		// There is no such thing as a tr or a tc style,
		// so we don't need to look for them,
		// but since a tc can contain w:p or nested table,
		// we still need to recurse

		// We already looked for a w:tblStyle;
		 // here, we are looking for styles in the tc.
		 for (Object o : tbl.getEGContentRowContent() ) {
			 
			 if (o instanceof org.docx4j.wml.Tr) {				 
				 log.debug( "\n in w:tr .. ");
				 org.docx4j.wml.Tr tr = (org.docx4j.wml.Tr)o;				 
				 for (Object o2 : tr.getEGContentCellContent() ) {					 
						if ( o2 instanceof javax.xml.bind.JAXBElement) {
							// Usual content for w:tr is w:tc
							if ( ((JAXBElement)o2).getDeclaredType().getName().equals("org.docx4j.wml.Tc") ) {
								log.debug( "\n  in w:tc .. ");
								org.docx4j.wml.Tc tc = (org.docx4j.wml.Tc)((JAXBElement)o2).getValue();
								
								// Look at the paragraphs in the tc
								traverseMainDocumentRecursive( tc.getEGBlockLevelElts(), 
										fontsDiscovered, stylesInUse);
								
							} else {
								// Could be custom markup (custom XML or sdt)
								log.warn("TODO - not w:tc - handle:  " + XmlUtils.JAXBElementDebug((JAXBElement)o2) );
							}
						} else if (o2 instanceof org.docx4j.wml.Tc) {
							// Again, it could be this or wrapped in a JAXBElement!
							traverseMainDocumentRecursive( ((org.docx4j.wml.Tc)o2).getEGBlockLevelElts(), 
									fontsDiscovered, stylesInUse);							
							
						} else {
							 // Could be custom markup (custom XML or sdt)
							 log.warn("TODO - not w:tc - handle:  " + o2.getClass().getName() );
						}					 
				 }
			 } else {
				 // What?
				 log.warn("TODO - handle:  " + o.getClass().getName() );
			 }
			 
		 }
		 
		 
		
	}
	
	
    private void inspectRPr(Object rPrObj, Map fontsDiscovered, Map stylesInUse) {
    	
    	if ( rPrObj instanceof org.docx4j.wml.RPr) {

    		org.docx4j.wml.RPr rPr =  (org.docx4j.wml.RPr)rPrObj;
    		
        	if (fontsDiscovered!=null && rPr.getRFonts()!=null) {
        		// 	Note the font - just Ascii for now
        		//log.debug("put font " + rPr.getRFonts().getAscii());
        		fontsDiscovered.put(rPr.getRFonts().getAscii(), rPr.getRFonts().getAscii());
        	}
        	if (stylesInUse !=null && rPr.getRStyle()!=null) {
        		// 	Note this run style
        		//log.debug("put style " + rPr.getRStyle().getVal() );
        		stylesInUse.put(rPr.getRStyle().getVal(), rPr.getRStyle().getVal());
        	}
    		
    		
    	} else if ( rPrObj instanceof org.docx4j.wml.ParaRPr) {

    		org.docx4j.wml.ParaRPr rPr =  (org.docx4j.wml.ParaRPr)rPrObj;
    		
        	if (fontsDiscovered!=null && rPr.getRFonts()!=null) {
        		// 	Note the font - just Ascii for now
        		//log.debug("put font " + rPr.getRFonts().getAscii());
        		fontsDiscovered.put(rPr.getRFonts().getAscii(), rPr.getRFonts().getAscii());
        	}
        	if (stylesInUse !=null && rPr.getRStyle()!=null) {
        		// 	Note this run style
        		//log.debug("put style " + rPr.getRStyle().getVal() );
        		stylesInUse.put(rPr.getRStyle().getVal(), rPr.getRStyle().getVal());
        	}
    		
    		
    	} else {
    		
    		log.error("Expected some kind of rPr, not " + rPrObj.getClass().getName() );    		
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

	/**
	 * Create a paragraph containing the string simpleText, styled 
	 * using the specified style
	 * (up to user to ensure it is a paragraph style)
	 * and add it to the document.
	 * 
	 * @param styleId
	 * @param text
	 * @return
	 */
	public org.docx4j.wml.P addStyledParagraphOfText(String styleId, String text) {
		
		org.docx4j.wml.P p = createStyledParagraphOfText(styleId, text);
		addObject(p);
		
		return p;

	}

	/**
	 * Create a paragraph containing the string simpleText, styled 
	 * using the specified style (up to user to ensure it is a paragraph style)
	 * without adding it to the document.
	 * 
	 * @param styleId
	 * @param text
	 * @return
	 */
	public org.docx4j.wml.P createStyledParagraphOfText(String styleId, String text) {
		
		org.docx4j.wml.P p = createParagraphOfText(text);
						
		StyleDefinitionsPart styleDefinitionsPart 
			= this.getStyleDefinitionsPart();

		if (getPropertyResolver().activateStyle(styleId)) {
			// Style is available 
			org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();			
			org.docx4j.wml.PPr  pPr = factory.createPPr();
			p.setPPr(pPr);
			org.docx4j.wml.PPrBase.PStyle pStyle = factory.createPPrBasePStyle();
			pPr.setPStyle(pStyle);
			pStyle.setVal(styleId);
		} 		
		
		return p;

	}
	
	
	/**
	 * Create a paragraph containing the string simpleText,
	 * and add it to the document.  If passed null, the result
	 * is an empty P.
	 * 
	 * @param simpleText
	 * @return
	 */
	public org.docx4j.wml.P addParagraphOfText(String simpleText) {
		
		org.docx4j.wml.P  para = createParagraphOfText(simpleText);
		addObject(para);
		
		return para;
		
	}

	/**
	 * Create a paragraph containing the string simpleText,
	 * without adding it to the document.  If passed null, the result
	 * is an empty P.
	 * 
	 * @param simpleText
	 * @return
	 */
	public org.docx4j.wml.P createParagraphOfText(String simpleText) {
		
		// Create content

		org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
		org.docx4j.wml.P  para = factory.createP();

		if (simpleText!=null) {
			org.docx4j.wml.Text  t = factory.createText();
			t.setValue(simpleText);
	
			org.docx4j.wml.R  run = factory.createR();
			run.getRunContent().add(t);		
			
			para.getParagraphContent().add(run);
		}
		
		return para;
		
		
	}
	
	
	/**
	 * Add the object o to the document.  
	 * 
	 * @param o
	 */
	public void addObject(Object o) {
		
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)this.getJaxbElement();
		Body body =  wmlDocumentEl.getBody();
		body.getEGBlockLevelElts().add(o);
		
		// If this object contains paragraphs, make sure any style used
		// is activated
    	Map stylesInUse = new java.util.HashMap();
		Map fontsDiscovered = new java.util.HashMap(); // method requires this
		List list = new java.util.ArrayList<Object>();
		list.add(o);
		traverseMainDocumentRecursive( list, fontsDiscovered, stylesInUse);
		Iterator it = stylesInUse.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        String styleName = (String)pairs.getKey();
	        log.debug("Inspecting style: " + styleName );
	        
	        if (styleDefinitionsPart==null) {
	        	
	        	log.warn("Style definitions part was null!");
	        	
	        } else if (getPropertyResolver().activateStyle(styleName)) {
	        
	        	// Cool
	        	
	        } else {
	        	
	        	log.warn(styleName + " couldn't be activated!");
	        }
	        
	    }
		
		
		
		
	}
	
	/**
	 * Create a paragraph from the xml string <w:p>...</w:p> ,
	 * and add it to the document.  You'll need to ensure the 
	 * string contains namespace declarations (including for w:)
	 * 
	 * @param simpleText
	 * @return
	 */
	public org.docx4j.wml.P addParagraph(String pXml) throws JAXBException {
		
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)this.getJaxbElement();
		Body body =  wmlDocumentEl.getBody();
		org.docx4j.wml.P  para = (org.docx4j.wml.P)org.docx4j.XmlUtils.unmarshalString(pXml); 
		body.getEGBlockLevelElts().add( para );
		return para;
	}
}

	
