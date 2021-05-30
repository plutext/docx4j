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



import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.fonts.CJKToEnglish;
import org.docx4j.fonts.RunFontSelector;
import org.docx4j.fonts.RunFontSelector.RunFontActionType;
import org.docx4j.fonts.RunFontSelector.RunFontCharacterVisitor;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.McIgnorableNamespaceDeclarator;
import org.docx4j.model.PropertyResolver;
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
import org.docx4j.wml.Br;
import org.docx4j.wml.CTEndnotes;
import org.docx4j.wml.CTFootnotes;
import org.docx4j.wml.Comments;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.Lvl;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.P;
import org.docx4j.wml.P.Hyperlink;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


/**
 * @author jharrop
 *
 */
public class MainDocumentPart extends DocumentPart<org.docx4j.wml.Document> implements ContentAccessor  {
	
	private static Logger log = LoggerFactory.getLogger(MainDocumentPart.class);
		
	
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
		
		/*
		 * From [MS-OE376]
		 * 
		 * 2.1.8 Part 1 Section 11.3.10, Main Document Part
		 * 
		 * a. The standard states that the content type for the Main Document part is: 
		 * 
		 *        application/vnd.openxmlformats-officedocument.wordprocessingml.main+xml.
		 *        
		 *    Office uses the following content type for the Main Document part: 
		 *    
		 *        application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml.
		 *        
		 * This note applies to the following products: 2007, 2007 SP1, 2007 SP2.
		 */

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.DOCUMENT);
	}	
	
	@Override
    protected void setMceIgnorable(McIgnorableNamespaceDeclarator namespacePrefixMapper) {

		// NB it is up to you to jaxbElement.setIgnorable; see further McIgnorableNamespaceDeclarator
		
//		MainDocumentPartMceIgnorableHelper helper = new MainDocumentPartMceIgnorableHelper();
//		this.jaxbElement.setIgnorable(
//				helper.getMceIgnorable(this.getJaxbElement().getBody()));
		
		namespacePrefixMapper.setMcIgnorable(
				this.getJaxbElement().getIgnorable() );
	}

	@Override
	public String getMceIgnorable() {
    	return this.getJaxbElement().getIgnorable();
    }
	
    
    

    /**
     * Convenience method to getJaxbElement().getBody().getContent()
     * @since 2.7
     */
    public List<Object> getContent() {
    	
    	if (this.getJaxbElement()==null) {    		
    		this.setJaxbElement( Context.getWmlObjectFactory().createDocument() );
    	}
    	if (this.getJaxbElement().getBody()==null) {
    		this.getJaxbElement().setBody(
    				Context.getWmlObjectFactory().createBody() );
    	}
    	
    	return this.getJaxbElement().getContent();
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
		return getStyleTree(false); // preserve existing behaviour
	}
	
	public StyleTree getStyleTree(boolean refresh) {
		// refresh is post 2.7.1
		
		if (refresh || styleTree==null) {
			
			log.debug("Preparing StyleTree");

//		    try {
//				getStyleDefinitionsPart().createVirtualStylesForDocDefaults();
//			} catch (Docx4JException e) {
//				// Shouldn't happen, so catch here
//				log.error(e.getMessage(), e);
//			}
	    	
//			// Get these first, so we can be sure they are defined... 
//			Style defaultParagraphStyle = getStyleDefinitionsPart().getDefaultParagraphStyle();
//			Style defaultCharacterStyle = getStyleDefinitionsPart().getDefaultCharacterStyle();
			
			// Styles defined in StyleDefinitionsPart
			Map<String, Style> allStyles = new HashMap<String, Style>();
			Styles styles = getStyleDefinitionsPart().getJaxbElement();		
			for ( org.docx4j.wml.Style s : styles.getStyle() ) {				
				allStyles.put(s.getStyleId(), s);	
				//log.debug("live style: " + s.getStyleId() );
			}
			styleTree = new StyleTree(getStylesInUse(), allStyles,
					getStyleDefinitionsPart().getJaxbElement().getDocDefaults(), 
					getStyleDefinitionsPart().getDefaultParagraphStyle());
				
		}
		return styleTree;
		
	}
	
		
    
    /**
     * Traverse the document, looking for fonts which have been applied, either
     * directly, or via a style. 
     * 
     * @return
     */
    public Set<String> fontsInUse() {
    	
    	log.debug("fontsInUse..");
    	
    	getPropertyResolver();  // this inits our virtual DocDefaults style
    	
    // Setup 
    	
    	Set<String> fontsDiscovered = new java.util.HashSet<String>();
    	
//    	// Keep track of styles we encounter, so we can
//    	// inspect these for fonts
//    	Set<String> stylesInUse = new java.util.HashSet<String>();
//
//		org.docx4j.wml.Styles styles = null;
//		if (this.getStyleDefinitionsPart()!=null) {
//			styles = (org.docx4j.wml.Styles)this.getStyleDefinitionsPart().getJaxbElement();			
//		}
//		// It is convenient to have a HashMap of styles
//		Map<String, Style> stylesDefined = new java.util.HashMap<String, Style>();
//		if (styles!=null) {
//		     for (Iterator<Style> iter = styles.getStyle().iterator(); iter.hasNext();) {
//		            Style s = iter.next();
//		            stylesDefined.put(s.getStyleId(), s);
//		     }
//		}
//    // We need to know what fonts and styles are used in the document
    	
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)this.getJaxbElement();
		Body body =  wmlDocumentEl.getBody();

		List <Object> bodyChildren = body.getContent();
		
		FontDiscoveryCharacterVisitor visitor = new FontDiscoveryCharacterVisitor(fontsDiscovered);
		RunFontSelector runFontSelector = new RunFontSelector((WordprocessingMLPackage) this.pack, visitor, RunFontActionType.DISCOVERY); 
		
		FontAndStyleFinder finder = new FontAndStyleFinder(runFontSelector, fontsDiscovered, null);
		finder.defaultCharacterStyle = this.getStyleDefinitionsPart().getDefaultCharacterStyle();
		finder.defaultParagraphStyle = this.getStyleDefinitionsPart().getDefaultParagraphStyle();	
		finder.defaultTableStyle = this.getStyleDefinitionsPart().getDefaultTableStyle();
		finder.styleDefinitionsPart = this.getStyleDefinitionsPart();
		
		new TraversalUtil(bodyChildren, finder);
//		finder.finish();
		
		fontsDiscovered.add(
				runFontSelector.getDefaultFont() );
		
		// fonts in headers, footers?
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
		
		// Add fonts used in the styles we discovered
		// .. 2013 03 10: no longer necessary
	    
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
            				fontsDiscovered.add(fontName);	
            				log.debug("Registered " + fontName + " for abstract list " + abstractNumNode.getAbstractNumId() + " lvl " + lvl.getIlvl() );
            			}
            		}
            	}
            }    		
    	}	
    	
    	if (log.isDebugEnabled()) {
    		for (String fontName : fontsDiscovered) {
    			log.debug(fontName);
    		}
    	}
		    	
		return fontsDiscovered;
    }
    
	private class FontDiscoveryCharacterVisitor implements RunFontCharacterVisitor {
		
		FontDiscoveryCharacterVisitor(Set<String> fontsDiscovered) {
			this.fontsDiscovered = fontsDiscovered;
		}
			
    	private Set<String> fontsDiscovered; // same set 

    	// look here
		public void fontAction(String fontname) {
			
			if (fontname==null) {
				log.warn("Got null", new Throwable());
				return;
			}
			
			String englishFromCJK = CJKToEnglish.toEnglish( fontname);
			if (englishFromCJK==null) {
				fontsDiscovered.add(fontname); 
			} else {
				fontsDiscovered.add(englishFromCJK);
				// No point adding the original CJK name
			}
			
		}

		
		private boolean spanReusable = true;
		public boolean isReusable() {
			return spanReusable;
		}
		public void setMustCreateNewFlag(boolean val) {
			spanReusable = !val;
		}

		public void setDocument(Document document) {}
		public void addCharacterToCurrent(char c) {}
		public void addCodePointToCurrent(int cp) {}
		public void finishPrevious() {}
		public void createNew() {}
		public void setRunFontSelector(RunFontSelector runFontSelector) {}
		public Object getResult() {return null;}
		@Override
		public void setFallbackFont(String fontname) {}
				
	}
    

	/**
	 * Traverse the document, and return a map of all styles which are used
	 * directly in the document.  (IE this does not include styles on which 
	 * others are just BasedOn).
	 * @return
	 */
	public Set<String> getStylesInUse(){

		
		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)this.getJaxbElement();
		Body body =  wmlDocumentEl.getBody();

		List <Object> bodyChildren = body.getContent();
		
		Set<String> stylesInUse = new HashSet<String>();
		FontAndStyleFinder finder = new FontAndStyleFinder(null, null, stylesInUse);
		finder.defaultCharacterStyle = this.getStyleDefinitionsPart().getDefaultCharacterStyle();
		finder.defaultParagraphStyle = this.getStyleDefinitionsPart().getDefaultParagraphStyle();
		finder.defaultTableStyle = this.getStyleDefinitionsPart().getDefaultTableStyle();
		
		finder.styleDefinitionsPart = this.getStyleDefinitionsPart();
		
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

		finder.finish();
		
		return stylesInUse;
	}
    
	
    /**
     * Traverse looking for fonts and/or styles
     *
     */
    private static class FontAndStyleFinder extends CallbackImpl {
		
    	Set<String> fontsDiscovered;
    	Set<String> stylesInUse; // by ID
    	
    	RunFontSelector runFontSelector;
    	
    	FontAndStyleFinder(RunFontSelector runFontSelector, 
    			Set<String> fontsDiscovered, 
    			Set<String> stylesInUse) {
    		
    		this.runFontSelector = runFontSelector;
    		this.fontsDiscovered = fontsDiscovered;
    		this.stylesInUse = stylesInUse;
    	}
    	
    	StyleDefinitionsPart styleDefinitionsPart;
    	
        Style defaultParagraphStyle;
        Style defaultCharacterStyle;
        Style defaultTableStyle;
    	
    	private boolean defaultParagraphStyleUsed = false;
    	private boolean defaultCharacterStyleUsed = false;
    	private boolean defaultTableStyleUsed = false;
    	
    	private PPr pPr; 
    	private RPr rPr;

    	
    	public void finish() {
    		if (stylesInUse != null) {
    			if ((defaultParagraphStyleUsed) && (defaultParagraphStyle != null)) {
					stylesInUse.add(defaultParagraphStyle.getStyleId() );
					if (defaultParagraphStyle.getRPr()!=null
							&& defaultParagraphStyle.getRPr().getRStyle()!=null ) {
						
						stylesInUse.add(defaultParagraphStyle.getRPr().getRStyle().getVal() );
					}
    			}
    			if ((defaultCharacterStyleUsed) && (defaultCharacterStyle != null)) {
    				stylesInUse.add(defaultCharacterStyle.getStyleId());
    			}
    			if ((defaultTableStyleUsed) && (defaultTableStyle != null)) {
    				stylesInUse.add(defaultTableStyle.getStyleId());
    			}
    			
    		}
    	}

		@Override
		public List<Object> apply(Object o) {
			
			if (o instanceof org.docx4j.wml.P) {
				pPr = ((P)o).getPPr();
				if (stylesInUse != null) { //do the styles
					boolean customPStyle = false;
					if (pPr != null) {
						if (pPr.getPStyle() != null) {
							// Note this paragraph style
							// log.debug("put style " + pPr.getPStyle().getVal());
							customPStyle = true;
							stylesInUse.add(pPr.getPStyle().getVal());
						}
						if ((pPr.getRPr() != null) && (pPr.getRPr().getRStyle() != null)) {
			        		// 	Note this run style
			        		//log.debug("put style " + pPr.getRPr().getRStyle().getVal() );
							stylesInUse.add(pPr.getRPr().getRStyle().getVal());
						}
					}
					defaultParagraphStyleUsed = defaultParagraphStyleUsed || (!customPStyle);
				}
		
			} else if ( o instanceof org.docx4j.wml.R) {
				rPr = ((R)o).getRPr();
				if (stylesInUse != null) {
					if (rPr != null) {
						if (rPr.getRStyle() == null) {
							defaultCharacterStyleUsed = true;
						}
						else {
							stylesInUse.add(rPr.getRStyle().getVal());
						}
					}
				}
				
			} else if ( o instanceof org.docx4j.wml.Text) {
								
				if (runFontSelector != null) {
					// discover the fonts which apply to this text
					log.debug(((Text)o).getValue());
					runFontSelector.fontSelector(pPr, rPr, ((Text)o) );
				}
				
			} else if (o instanceof org.docx4j.wml.R.Sym ) { 
				
				if ( fontsDiscovered !=null ) {
					org.docx4j.wml.R.Sym sym = (org.docx4j.wml.R.Sym)o;
					fontsDiscovered.add(sym.getFont());
				}
				
			} else if (o instanceof org.docx4j.wml.Tbl ) {
				
				// The table could have a table style;
				// Tables created in Word 2007 default to table style "TableGrid",
				// which is based on "TableNormal".
				org.docx4j.wml.Tbl tbl = (org.docx4j.wml.Tbl)o;
				
				if (stylesInUse != null) { //do the styles
					boolean customTblStyle = false;
					if (tbl.getTblPr() != null) {
						if (tbl.getTblPr().getTblStyle() != null) {
							// Note this tbl style
							// log.debug("put style " + tbl.getTblPr().getTblStyle().getVal());
							customTblStyle = true;
							stylesInUse.add(tbl.getTblPr().getTblStyle().getVal());
						}
					}
					defaultTableStyleUsed = defaultTableStyleUsed || (!customTblStyle);
				}				
				
				// There is no such thing as a tr or a tc style,
				// so we don't need to look for them,
				// but since a tc can contain w:p or nested table,
				// we still need to recurse
				
			} else if (o instanceof SdtElement ) {
				
				SdtPr sdtPr = ((SdtElement)o).getSdtPr();
				if (sdtPr!=null) {
					Object o2 = sdtPr.getByClass(RPr.class);
					if (o2!=null) {
						RPr rPr = (RPr)o2;
						RStyle rStyle = rPr.getRStyle();
						if (rStyle!=null) {
							// Add it
							if (stylesInUse !=null) { 
								stylesInUse.add(rStyle.getVal());
								// and linked p style (if any), useful for OpenDoPE XHTML
								Style pStyle = styleDefinitionsPart.getLinkedStyle(rStyle.getVal());
								if (pStyle!=null) {
									stylesInUse.add(pStyle.getStyleId());
								}
							}
						}
					}
				}
				
				
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
						
		if (getPropertyResolver().activateStyle(styleId)) {
			// Style is available 
			org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();			
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
		
		org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
		org.docx4j.wml.P  para = factory.createP();

		if (simpleText!=null) {
			
			String[] splits = simpleText.replaceAll("\r", "").split("\n"); 
			boolean afterNewline = false;
			org.docx4j.wml.R  run = factory.createR();
			for (String s : splits ) {

				if (afterNewline) {
					 Br br = factory.createBr();
					 run.getContent().add(br);
				}
				
				org.docx4j.wml.Text  t = factory.createText();
				t.setValue(s);
				
				if (s.startsWith(" ")) {
					t.setSpace("preserve");
				} else if (s.endsWith(" ")) {
					t.setSpace("preserve");
				}
				
				run.getContent().add(t); // ContentAccessor					
				
				afterNewline = true;
			}
			
			para.getContent().add(run); // ContentAccessor
		}
		
		return para;
	}
	
	
	/**
	 * Add the object o to the document.
	 * The same as getContent().add, except that   
	 * this will ensure any style used
     * is activated.
     * 
	 * @param o
	 */
	public void addObject(Object o) {
		
		this.getContent().add( o );
		
		// If this object contains paragraphs, make sure any style used
		// is activated
    	Set<String> stylesInUse = new java.util.HashSet<String>();
//    	Set<String> fontsDiscovered = new java.util.HashSet<String>(); 
		List list = new java.util.ArrayList<Object>();
		list.add(o);
		
//		FontDiscoveryCharacterVisitor visitor = new FontDiscoveryCharacterVisitor(fontsDiscovered);
//		RunFontSelector runFontSelector = new RunFontSelector((WordprocessingMLPackage) this.pack, visitor, OutputType.NA); 

        if (styleDefinitionsPart==null) {        	
        	log.info("Style definitions part was null!");
        	return;
        }
		
		FontAndStyleFinder finder = new FontAndStyleFinder(null, null, stylesInUse);
		finder.defaultCharacterStyle = this.getStyleDefinitionsPart().getDefaultCharacterStyle();
		finder.defaultParagraphStyle = this.getStyleDefinitionsPart().getDefaultParagraphStyle();
		finder.styleDefinitionsPart = this.getStyleDefinitionsPart();		
		
		finder.walkJAXBElements(list);
		finder.finish();
		
		for( String styleName : stylesInUse) {
	        log.debug("Inspecting style: " + styleName );
	        
	        if (getPropertyResolver().activateStyle(styleName)) {
	        	// Cool
	        } else {
	        	log.info(styleName + " couldn't be activated!");
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
		
		org.docx4j.wml.P  para = (org.docx4j.wml.P)org.docx4j.XmlUtils.unmarshalString(pXml);
		this.getContent().add( para );
		return para;
	}
	
	/**
	 * Create a Hyperlink object, which is suitable for adding to a w:p
	 * @param bookmarkName
	 * @param linkText
	 * @return
	 */
	public static Hyperlink hyperlinkToBookmark(String bookmarkName, String linkText) {
		
		try {

			
			String hpl = "<w:hyperlink w:anchor=\"" + bookmarkName + "\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" " +
            "w:history=\"1\" >" +
            "<w:r>" +
            "<w:rPr>" +
            "<w:rStyle w:val=\"Hyperlink\" />" +  // TODO: enable this style in the document!
            "</w:rPr>" +
            "<w:t>" + linkText + "</w:t>" +
            "</w:r>" +
            "</w:hyperlink>";

			return (Hyperlink)XmlUtils.unmarshalString(hpl);
			
		} catch (Exception e) {
			// Shouldn't happen
			e.printStackTrace();
			return null;
		}
				
	}

	/**
	 * Attach a template to this document.
	 * This is just an easy way to access the same method in DocumentSettingsPart 
	 * 
	 * @param templatePath
	 * @since 6.1.0
	 */
	public void attachTemplate(String templatePath) {
		
		DocumentSettingsPart dsp = null;
		try {
			dsp = this.getDocumentSettingsPart(true);
		} catch (InvalidFormatException e) {
			// shouldn't happen
			log.error(e.getMessage(), e);
		}
		dsp.attachTemplate(templatePath);
	}
	
	
	GlossaryDocumentPart glossaryDocumentPart;
	/**
	 * @since 3.0.0
	 */
	public GlossaryDocumentPart getGlossaryDocumentPart() {
		return glossaryDocumentPart;
	}

	KeyMapCustomizationsPart keyMapCustomizationsPart;
	/**
	 * @since 8.2.8
	 */	
	public KeyMapCustomizationsPart getKeyMapCustomizationsPart() {
		return keyMapCustomizationsPart;
	}
	
	@Override
	public boolean setPartShortcut(Part part, String relationshipType) {

		if (relationshipType.equals(Namespaces.GLOSSARY_DOCUMENT)) {
			glossaryDocumentPart = (GlossaryDocumentPart)part;
			return true;			
		} else if (relationshipType.equals(Namespaces.KEYMAP)) {
			keyMapCustomizationsPart = (KeyMapCustomizationsPart)part;
			return true;			
		} else {
			return super.setPartShortcut(part, relationshipType);
		}
	}
	
}

	
