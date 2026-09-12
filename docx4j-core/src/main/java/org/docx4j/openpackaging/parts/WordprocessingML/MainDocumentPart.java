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

import jakarta.xml.bind.JAXBException;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.fonts.CJKToEnglish;
import org.docx4j.fonts.RunFontSelector;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.McIgnorableNamespaceDeclarator;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.openpackaging.exceptions.CyclicStylesException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.ThemePart;
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
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.RFonts;
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
	
	private volatile PropertyResolver propertyResolver;

	/**
	 * get the PropertyResolver, creating if necessary
	 * @throws Docx4JException 
	 */
	public PropertyResolver getPropertyResolver() throws Docx4JException {
		return getPropertyResolver(true);
	}

	/**
	 * get the PropertyResolver
	 * 
	 * @param create whether to create if null
	 * @return
	 * @throws Docx4JException 
	 * @since 11.5.2
	 */
	public PropertyResolver getPropertyResolver(boolean create) throws Docx4JException {
		
		// create=false is only really necessary where a StackOverflow is a possibility;
		// eg getPropertyResolver() being invoked from PropertyResolver's constructor.

		/* NB once created, this is kept for the life of the package.  Since 17.0.4, a
		 * style added to the styles part later is still found (see
		 * PropertyResolver.getLiveStyle); for a style modified or removed, use
		 * PropertyResolver.refresh(). */
		if (create && propertyResolver == null) {
			synchronized (this) { // two threads exporting at once share one resolver (CR-015 phase 3)
				if (propertyResolver == null) {
					propertyResolver = new PropertyResolver((WordprocessingMLPackage) this.pack);
				}
			}
		}
		return propertyResolver;
	}
	
	private StyleTree styleTree;
	public StyleTree getStyleTree() throws CyclicStylesException {
		return getStyleTree(false); // preserve existing behaviour
	}
	
	public StyleTree getStyleTree(boolean refresh) throws CyclicStylesException {
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
     * The fonts the document uses, by name: what the font mapper is populated with and
     * the FOP configuration is built from.
     *
     * <p>A walk for names, since 17.1.1: the four slots of every {@code w:rFonts} on the
     * runs, the paragraph marks and the styles in use (with what they are based on), in
     * the body, headers, footers, footnotes, endnotes and comments; {@code w:sym} fonts;
     * the numbering levels' fonts; the document defaults; the default font.  Each theme
     * reference is resolved through the theme part for the document's themeFontLang, as
     * the selector resolves it ({@link RunFontSelector#documentFontsOf}); a CJK font name
     * is collected by its English name ({@link CJKToEnglish}).</p>
     *
     * <p>Until 17.1.1 this ran a {@link RunFontSelector} in its DISCOVERY mode over every
     * run, which decided among the run's fonts by glyph checks it could not yet answer
     * (the mapper is populated from this list, so nothing was mapped), took
     * {@code w:ascii} alone from the numbering levels, and cost a selection per run
     * (165 ms on a 311-page document).  The conversion declares the fonts it actually
     * reaches to FOP itself, late (see FopConfigUtil.declareFallbackFonts), so discovery
     * needs no selection; what it needs is every name the document could ask for, so
     * that the mapper can map it.</p>
     *
     * @return the names, never null
     */
    public Set<String> fontsInUse() {
    	
    	log.debug("fontsInUse..");
    	
    	Set<String> fontsDiscovered = new java.util.HashSet<String>();
    	CTLanguage themeFontLang = null;
    	if (getDocumentSettingsPart()!=null) {
    		try {
    			themeFontLang = getDocumentSettingsPart().getContents().getThemeFontLang();
    		} catch (Docx4JException e) {
    			log.warn("Settings part unreadable: " + e.getMessage());
    		}
    	}
    	FontNames names = new FontNames(fontsDiscovered, getThemePart(), themeFontLang);
    	StyleDefinitionsPart sdp = getStyleDefinitionsPart();

    	// the runs, paragraph marks and w:sym of the body and the other story parts, and
    	// the styles they use
		Set<String> stylesInUse = new java.util.HashSet<String>();
		FontAndStyleFinder finder = new FontAndStyleFinder(names, stylesInUse);
		if (sdp!=null) {
			finder.defaultCharacterStyle = sdp.getDefaultCharacterStyle();
			finder.defaultParagraphStyle = sdp.getDefaultParagraphStyle();	
			finder.defaultTableStyle = sdp.getDefaultTableStyle();
			finder.styleDefinitionsPart = sdp;
		}
		walkStories(finder);
		finder.finish();

		RFonts docDefaultsRFonts = null;
		if (sdp!=null && sdp.getJaxbElement()!=null) {
			org.docx4j.wml.Styles styles = sdp.getJaxbElement();
			if (styles.getDocDefaults()!=null && styles.getDocDefaults().getRPrDefault()!=null
					&& styles.getDocDefaults().getRPrDefault().getRPr()!=null) {
				docDefaultsRFonts = styles.getDocDefaults().getRPrDefault().getRPr().getRFonts();
			}
			names.add(docDefaultsRFonts);

			// the styles in use, each with the chain it is based on
			for (String styleId : stylesInUse) {
				Style style = sdp.getStyleById(styleId);
				int guard = 0;
				while (style!=null && guard++ < 64) {
					if (style.getRPr()!=null) names.add(style.getRPr().getRFonts());
					for (org.docx4j.wml.CTTblStylePr tblStylePr : style.getTblStylePr()) {
						if (tblStylePr.getRPr()!=null) names.add(tblStylePr.getRPr().getRFonts());
					}
					style = style.getBasedOn()==null ? null : sdp.getStyleById(style.getBasedOn().getVal());
				}
			}
		}
	    
	    // Fonts can also be used in the numbering part.
	    // For now, treat any font mentioned in that part as in use.
	    // Ideally, we'd only register fonts used in numbering levels
	    // that were actually used in the document
    	if (getNumberingDefinitionsPart()!=null) {
    		Numbering numbering = getNumberingDefinitionsPart().getJaxbElement();
            for (Numbering.AbstractNum abstractNumNode : numbering.getAbstractNum() ) {
            	for (Lvl lvl : abstractNumNode.getLvl() ) {
            		if (lvl.getRPr()!=null) {
            			names.add(lvl.getRPr().getRFonts());
            		}
            	}
            }    		
    	}	

    	fontsDiscovered.add(RunFontSelector.defaultFontOf(docDefaultsRFonts, getThemePart(), themeFontLang));
    	
    	if (log.isDebugEnabled()) {
    		for (String fontName : fontsDiscovered) {
    			log.debug(fontName);
    		}
    	}
		    	
		return fontsDiscovered;
    }

    /** The body, then the headers and footers, the endnotes, footnotes and comments. */
    private void walkStories(FontAndStyleFinder finder) {

		org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)this.getJaxbElement();
		Body body =  wmlDocumentEl.getBody();
		new TraversalUtil(body.getContent(), finder);
		
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
		if (this.getCommentsPart()!=null) {
			log.debug("Looking at comments");			
			Comments comments = this.getCommentsPart().getJaxbElement();
			finder.walkJAXBElements(comments);
		}
    }

    /**
     * Collects the document font names a walk finds: each w:rFonts' four slots with the
     * theme references resolved ({@link RunFontSelector#documentFontsOf}), a CJK name by
     * its English name ({@link CJKToEnglish}, as the discovery visitor did until 17.1.1),
     * blanks skipped.
     */
    private static class FontNames {

    	private final Set<String> names;
    	private final ThemePart themePart;
    	private final CTLanguage themeFontLang;

    	FontNames(Set<String> names, ThemePart themePart, CTLanguage themeFontLang) {
    		this.names = names;
    		this.themePart = themePart;
    		this.themeFontLang = themeFontLang;
    	}

    	void add(RFonts rFonts) {
    		if (rFonts==null) return;
    		for (String name : RunFontSelector.documentFontsOf(rFonts, themePart, themeFontLang)) {
    			add(name);
    		}
    	}

    	void add(String fontName) {
    		if (fontName==null) return;
    		fontName = fontName.trim();
    		if (fontName.length()==0) return;
			String englishFromCJK = CJKToEnglish.toEnglish(fontName);
			// where there is an English name, no point adding the original CJK name
			names.add(englishFromCJK==null ? fontName : englishFromCJK);
    	}
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
		FontAndStyleFinder finder = new FontAndStyleFinder(null, stylesInUse);
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
		
    	FontNames fontsDiscovered; // null: not collecting fonts
    	Set<String> stylesInUse; // by ID; null: not collecting styles
    	
    	FontAndStyleFinder(FontNames fontsDiscovered, 
    			Set<String> stylesInUse) {
    		
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
				if (fontsDiscovered != null && pPr != null && pPr.getRPr() != null) {
					fontsDiscovered.add(pPr.getRPr().getRFonts()); // the paragraph mark's
				}
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
				if (fontsDiscovered != null && rPr != null) {
					fontsDiscovered.add(rPr.getRFonts());
				}
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
						if (fontsDiscovered != null) {
							fontsDiscovered.add(rPr.getRFonts());
						}
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
						
		try {
			if (getPropertyResolver().activateStyle(styleId)) {
				// Style is available 
				org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();			
				org.docx4j.wml.PPr  pPr = factory.createPPr();
				p.setPPr(pPr);
				org.docx4j.wml.PPrBase.PStyle pStyle = factory.createPPrBasePStyle();
				pPr.setPStyle(pStyle);
				pStyle.setVal(styleId);
			}
		} catch (Docx4JException e) {
			log.error(e.getMessage(), e);
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
		
		FontAndStyleFinder finder = new FontAndStyleFinder(null, stylesInUse);
		finder.defaultCharacterStyle = this.getStyleDefinitionsPart().getDefaultCharacterStyle();
		finder.defaultParagraphStyle = this.getStyleDefinitionsPart().getDefaultParagraphStyle();
		finder.styleDefinitionsPart = this.getStyleDefinitionsPart();		
		
		finder.walkJAXBElements(list);
		finder.finish();
		
		for( String styleName : stylesInUse) {
	        log.debug("Inspecting style: " + styleName );
	        
	        try {
				if (getPropertyResolver().activateStyle(styleName)) {
					// Cool
				} else {
					log.info(styleName + " couldn't be activated!");
				}
			} catch (Docx4JException e) {
				log.error(e.getMessage(), e);
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

	
