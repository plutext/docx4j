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
import java.util.Stack;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.listnumbering.AbstractListNumberingDefinition;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTEndnotes;
import org.docx4j.wml.CTFootnotes;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.Comments;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.Lvl;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.P;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtContentBlock;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.docx4j.wml.Tag;
import org.docx4j.wml.Tc;
import org.docx4j.wml.PPrBase.PBdr;
import org.w3c.dom.Node;


/**
 * @author jharrop
 *
 */
public class MainDocumentPart extends DocumentPart<org.docx4j.wml.Document> implements ContentAccessor  {
	
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
     * Convenience method to getJaxbElement().getBody().getContent()
     * @since 2.7
     */
    public List<Object> getContent() {
    	return this.getJaxbElement().getContent();
    }	
	
	private Binder<Node> binder;
	
	
	/**
	 * Enables synchronization between XML infoset nodes and JAXB objects 
	 * representing same XML document.
	 * 
	 * An instance of this class maintains the association between XML nodes
	 * of an infoset preserving view and a JAXB representation of an XML document. 
	 * Navigation between the two views is provided by the methods 
	 * getXMLNode(Object) and getJAXBNode(Object) .
	 * 
	 * In theory, modifications can be made to either the infoset preserving view or 
	 * the JAXB representation of the document while the other view remains
	 * unmodified. The binder ought to be able to synchronize the changes made in
	 * the modified view back into the other view using the appropriate
	 * Binder update methods, #updateXML(Object, Object) or #updateJAXB(Object).
	 * 
	 * But JAXB doesn't currently work as advertised .. access to this
	 * object is offered for advanced users on an experimental basis only.
	 */
	public Binder<Node> getBinder() {
		
//		if (binder ==null) {
//			binder = jc.createBinder();			
//		}		
		return binder;
	}
	
	/**
	 * Fetch JAXB Nodes matching an XPath (for example "//w:p").
	 * 
	 * If you have modified your JAXB objects (eg added or changed a 
	 * w:p paragraph), you need to update the association. The problem
	 * is that this can only be done ONCE, owing to a bug in JAXB:
	 * see https://jaxb.dev.java.net/issues/show_bug.cgi?id=459
	 * 
	 * So this is left for you to choose to do via the refreshXmlFirst parameter.   
	 * 
	 * @param xpathExpr
	 * @param refreshXmlFirst
	 * @return
	 * @throws JAXBException
	 */	
	public List<Object> getJAXBNodesViaXPath(String xpathExpr, boolean refreshXmlFirst) 
			throws JAXBException {
		
		return XmlUtils.getJAXBNodesViaXPath(binder, jaxbElement, xpathExpr, refreshXmlFirst);
	}	

	/**
	 * Fetch JAXB Nodes matching an XPath (for example ".//w:p" - note the dot,
	 * which is necessary for this sort of relative path).
	 * 
	 * If you have modified your JAXB objects (eg added or changed a 
	 * w:p paragraph), you need to update the association. The problem
	 * is that this can only be done ONCE, owing to a bug in JAXB:
	 * see https://jaxb.dev.java.net/issues/show_bug.cgi?id=459
	 * 
	 * So this is left for you to choose to do via the refreshXmlFirst parameter.   

	 * @param xpathExpr
	 * @param someJaxbElement
	 * @param refreshXmlFirst
	 * @return
	 * @throws JAXBException
	 */
	public List<Object> getJAXBNodesViaXPath(String xpathExpr, Object someJaxbElement, boolean refreshXmlFirst) 
		throws JAXBException {

		return XmlUtils.getJAXBNodesViaXPath(binder, someJaxbElement, xpathExpr, refreshXmlFirst);
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
			
			log.info("Preparing StyleTree");

			// Styles actually in use in the document
			List<String> stylesInUse = new ArrayList<String>();
			Iterator it = getStylesInUse().entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pairs = (Map.Entry)it.next();
		        String styleId = (String)pairs.getKey();
		        stylesInUse.add(styleId);
				//log.debug("style in use: " + styleId );
		    }
		    		    
		    try {
				getStyleDefinitionsPart().createVirtualStylesForDocDefaults();
			} catch (Docx4JException e) {
				// Shouldn't happen, so catch here
				log.error(e);
			}
	    	
			// Get these first, so we can be sure they are defined... 
			Style defaultParagraphStyle = getStyleDefinitionsPart().getDefaultParagraphStyle();
			Style defaultCharacterStyle = getStyleDefinitionsPart().getDefaultCharacterStyle();
			
			// Styles defined in StyleDefinitionsPart
			Map<String, Style> allStyles = new HashMap<String, Style>();
			Styles styles = getStyleDefinitionsPart().getJaxbElement();		
			for ( org.docx4j.wml.Style s : styles.getStyle() ) {				
				allStyles.put(s.getStyleId(), s);	
				//log.debug("live style: " + s.getStyleId() );
			}
			styleTree = new StyleTree(stylesInUse, allStyles, 
					defaultParagraphStyle.getStyleId(),
					defaultCharacterStyle.getStyleId());
				
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
			
			log.info("For MDP, unmarshall via binder");
			// InputStream to Document
			javax.xml.parsers.DocumentBuilderFactory dbf 
				= DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			org.w3c.dom.Document doc = dbf.newDocumentBuilder().parse(is);

			// 
			binder = jc.createBinder();
			
			binder.setEventHandler(
					new org.docx4j.jaxb.JaxbValidationEventHandler());
			
			jaxbElement =  (org.docx4j.wml.Document) binder.unmarshal( doc );
			
			return jaxbElement;
/*		    		    
			Unmarshaller u = jc.createUnmarshaller();

			//u.setSchema(org.docx4j.jaxb.WmlSchema.schema);			
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
			
//			JAXBElement<?> root = (JAXBElement<?>)u.unmarshal( is );			
//			jaxbElement = (org.docx4j.wml.Document)root.getValue();
			
			jaxbElement =  (org.docx4j.wml.Document) u.unmarshal( is );
			return jaxbElement;
			
			//System.out.println("\n\n" + this.getClass().getName() + " unmarshalled \n\n" );									
*/
			
		} catch (Exception e ) {
			e.printStackTrace();
			return null;
		}
    }

    public org.docx4j.wml.Document unmarshal(org.w3c.dom.Element el) throws JAXBException {

		try {

			binder = jc.createBinder();
			binder.setEventHandler(
					new org.docx4j.jaxb.JaxbValidationEventHandler());			
			jaxbElement = (org.docx4j.wml.Document) binder.unmarshal( el );

			return jaxbElement;
			
		} catch (JAXBException e) {
//			e.printStackTrace();
			log.error(e);
			throw e;
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
		
//		traverseMainDocumentRecursive(bodyChildren, fontsDiscovered, stylesInUse); 
		Finder finder = new Finder(fontsDiscovered, stylesInUse);
		new TraversalUtil(bodyChildren, finder);

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
            	if (fontName!=null) {
	            	log.debug(styleName + " uses font " + fontName);
	            	fontsDiscovered.put(fontName, fontName);
            	}
            } else {
            	log.error("Couldn't find used style " + styleName + "in styles part!");
            }
	    }
	    
	    // Fonts can also be used in the numbering part
	    // For now, treat any font mentioned in that part as in use.
	    // Ideally, we'd only register fonts used in numbering levels
	    // that were actually used in the document
    	if (getNumberingDefinitionsPart()!=null) {
    		Numbering numbering = getNumberingDefinitionsPart().getJaxbElement();
            for (Numbering.AbstractNum abstractNumNode : numbering.getAbstractNum() ) {
            	for (Lvl lvl : abstractNumNode.getLvl() ) {
            		if (lvl.getRPr()!=null
            				&& lvl.getRPr().getRFonts()!=null ) {
            			String fontName = lvl.getRPr().getRFonts().getAscii();            			
            			if (fontName!=null) {
            				fontsDiscovered.put(fontName, fontName);	
            				log.debug("Registered " + fontName + " for abstract list " + abstractNumNode.getAbstractNumId() + " lvl " + lvl.getIlvl() );
            			}
            		}
            	}
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

		
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)this.getJaxbElement();
		Body body =  wmlDocumentEl.getBody();

		List <Object> bodyChildren = body.getEGBlockLevelElts();
		
		Map<String, String> stylesInUse = new HashMap<String, String>();
		Finder finder = new Finder(null, stylesInUse);
		new TraversalUtil(bodyChildren, finder);

		// Styles in headers, footers?
		RelationshipsPart rp = this.getRelationshipsPart();
		if (rp!=null) {
			for ( Relationship r : rp.getRelationships().getRelationship() ) {
				Part part = rp.getPart(r);
				if ( part instanceof FooterPart ) {
					
					Ftr ftr = ((FooterPart)part).getJaxbElement();
					finder.walkJAXBElements(ftr);
					
				} else if (part instanceof HeaderPart) {
					
					Hdr hdr = ((HeaderPart)part).getJaxbElement();
					finder.walkJAXBElements(hdr);
				}
			}
		}
		
		// Styles in endnotes, footnotes?
		if (this.getEndNotesPart()!=null) {
			log.debug("Looking at endnotes");
			CTEndnotes endnotes= this.getEndNotesPart().getJaxbElement();
			finder.walkJAXBElements(endnotes);
		}
		if (this.getFootnotesPart()!=null) {
			log.debug("Looking at footnotes");
			CTFootnotes footnotes= this.getFootnotesPart().getJaxbElement();
			finder.walkJAXBElements(footnotes);
		}
		
		// Comments
		if (this.getCommentsPart()!=null) {
			log.debug("Looking at comments");			
			Comments comments = this.getCommentsPart().getJaxbElement();
			finder.walkJAXBElements(comments);
		}
		
		return stylesInUse;
	}
    
	
    /**
     * Traverse looking for fonts and/or styles
     *
     */
    static class Finder extends CallbackImpl {
		
    	Map fontsDiscovered;
    	Map<String, String> stylesInUse;
    	
    	Finder(Map fontsDiscovered, Map<String, String> stylesInUse) {
    		this.fontsDiscovered = fontsDiscovered;
    		this.stylesInUse = stylesInUse;
    	}
    	
    	@Override
		public List<Object> apply(Object o) {
			
			if (o instanceof org.docx4j.wml.P
					&& ((org.docx4j.wml.P)o).getPPr()!=null) {					
				
	    		org.docx4j.wml.PPr pPr =  ((org.docx4j.wml.P)o).getPPr();
				if (stylesInUse !=null && pPr.getPStyle() != null) {
					// Note this paragraph style
					log.debug("put style "
							+ pPr.getPStyle().getVal());
					stylesInUse.put(pPr.getPStyle().getVal(), 
							pPr.getPStyle().getVal());
				}
				
				if (pPr.getRPr()!=null) {
					
		    		org.docx4j.wml.ParaRPr rPr =  pPr.getRPr();
		    		
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
				}
		
			} else if ( o instanceof org.docx4j.wml.R
					&& ((org.docx4j.wml.R)o).getRPr()!=null) {

	    		org.docx4j.wml.RPr rPr =  ((org.docx4j.wml.R)o).getRPr();
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
		    		
			} else if (o instanceof org.docx4j.wml.R.Sym ) { 
				
				if ( fontsDiscovered !=null ) {
					org.docx4j.wml.R.Sym sym = (org.docx4j.wml.R.Sym)o;
					fontsDiscovered.put(sym.getFont(), sym.getFont());
				}
				
			} else if (o instanceof org.docx4j.wml.Tbl ) {
				// The table could have a table style;
				// Tables created in Word 2007 default to table style "TableGrid",
				// which is based on "TableNormal".
				org.docx4j.wml.Tbl tbl = (org.docx4j.wml.Tbl)o;
				if (stylesInUse !=null && tbl.getTblPr()!=null 
						&& tbl.getTblPr().getTblStyle()!=null) {
					log.debug("Adding table style: " + tbl.getTblPr().getTblStyle().getVal() );
					stylesInUse.put(tbl.getTblPr().getTblStyle().getVal(),
									tbl.getTblPr().getTblStyle().getVal() );
				}
				// There is no such thing as a tr or a tc style,
				// so we don't need to look for them,
				// but since a tc can contain w:p or nested table,
				// we still need to recurse
				
			}
			return null;
		}
    	
    	@Override
		public boolean shouldTraverse(Object o) {
    		
    		if (o instanceof org.docx4j.wml.Br
    				|| o instanceof org.docx4j.wml.R.Tab
    				|| o instanceof org.docx4j.wml.R.LastRenderedPageBreak) {
    			return false;
    		}
			return true;
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
		
		Body body =  this.jaxbElement.getBody();
		body.getEGBlockLevelElts().add(o);
		
		// If this object contains paragraphs, make sure any style used
		// is activated
    	Map stylesInUse = new java.util.HashMap();
		Map fontsDiscovered = new java.util.HashMap(); // method requires this
		List list = new java.util.ArrayList<Object>();
		list.add(o);
		
//		traverseMainDocumentRecursive( list, fontsDiscovered, stylesInUse);
		Finder finder = new Finder(fontsDiscovered, stylesInUse);
		finder.walkJAXBElements(list);
		
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

	
