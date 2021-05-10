package org.docx4j.model;


import java.math.BigInteger;
import java.util.HashMap;
import java.util.Stack;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.DocDefaults;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.NumPr.NumId;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.TblPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class works out the actual set of properties (paragraph or run)
 * which apply, following the order specified in ECMA-376.
 * 
 * From ECMA-376 > Part3 > 2 Introduction to WordprocessingML > 2.8 Styles > 2.8.10 Style Application 
 * at http://www.documentinteropinitiative.org/implnotes/ecma-376/P3-2.8.10.aspx
 * 
 * (See also Part 4, 2.7.2 which is the normative bit...)

	With the various flavors of styles available, multiple style types can be 
	applied to the same content within a file, which means that properties must 
	be applied in a specific deterministic order. As with inheritance, the 
	resulting formatting properties set by one type can be unchanged, removed, 
	or altered by following types.

	The following table illustrates the order of application of these defaults, 
	and which properties are impacted by each:

	This process can be described as follows: 
	
	First, the document defaults are applied to all runs and paragraphs in 
	the document. 
	
	Next, the table style properties are applied to each table in the document, 
	following the conditional formatting inclusions and exclusions specified 
	per table. 
	
	Next, numbered item and paragraph properties are applied to each paragraph 
	formatted with a numbering style. 
	
	Next, paragraph and run properties are 
	applied to each paragraph as defined by the paragraph style. 
	
	Next, run properties are applied to each run with a specific character style 
	applied. 
	
	Finally, we apply direct formatting (paragraph or run properties not from 
	styles).
	
	-----------
	
	Things which are unclear:
	
	 - the role of w:link on a paragraph style (eg Heading1 links to "Heading1char"),
	   experimentation in Word 2007 suggests the w:link is not used at all
	   
	 - indeed, "Heading1char" is not used at all?
	 
	-----------

	 docx4all does not use this; its org.docx4all.swing.text.StyleSheet
	 uses MutableAttributeSet's resolve function to climb the style hierarchy.
	   
	 This is most relevant to XSLFO, which unlike CSS, doesn't have a concept of 
	 style. HTML NG2 uses CSS inheritance, and so doesn't need it.

 * @author jharrop
 *
 */
public class PropertyResolver {
	
	private static Logger log = LoggerFactory.getLogger(PropertyResolver.class);
	
//	private DocDefaults docDefaults;	
	private PPr documentDefaultPPr;
	private RPr documentDefaultRPr;
	
	public RPr getDocumentDefaultRPr() {
		return documentDefaultRPr;
	}
	public PPr getDocumentDefaultPPr() {
		return documentDefaultPPr;
	}

	private StyleDefinitionsPart styleDefinitionsPart;
	
	private WordprocessingMLPackage wordMLPackage;
	
	/**
	 * All styles in the Style Definitions Part.
	 */
	private org.docx4j.wml.Styles styles;

	/**
	 * Map of all styles in the Style Definitions Part.
	 * Note, you need to manually keep this up to date
	 */
	private java.util.Map<String, org.docx4j.wml.Style>  liveStyles = null;
	
	
	private ThemePart themePart;
	private NumberingDefinitionsPart numberingDefinitionsPart;


	private java.util.Map<String, PPr>  resolvedStylePPrComponent = new HashMap<String, PPr>();

	/**
	 * This map also contains the rPr component of a pPr
	 */
	private java.util.Map<String, RPr>  resolvedStyleRPrComponent = new HashMap<String, RPr>();
	
	public PropertyResolver(WordprocessingMLPackage wordMLPackage) throws Docx4JException {
		
		this.wordMLPackage = wordMLPackage;
		
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		styleDefinitionsPart = mdp.getStyleDefinitionsPart(true);
		themePart = mdp.getThemePart();
		numberingDefinitionsPart = mdp.getNumberingDefinitionsPart();
		if (wordMLPackage.getMainDocumentPart().getDocumentSettingsPart()!=null
				&& wordMLPackage.getMainDocumentPart().getDocumentSettingsPart().getContents()!=null) {
			themeFontLang = wordMLPackage.getMainDocumentPart().getDocumentSettingsPart().getContents().getThemeFontLang();
		}
		init();		
	}
	
	CTLanguage themeFontLang = null;
	
//	public PropertyResolver(StyleDefinitionsPart styleDefinitionsPart,
//							ThemePart themePart,
//							NumberingDefinitionsPart numberingDefinitionsPart) throws Docx4JException {
//		
//		this.styleDefinitionsPart= styleDefinitionsPart;
//		this.themePart = themePart;
//		this.numberingDefinitionsPart = numberingDefinitionsPart;
//		init();
//	}
	
	String defaultParagraphStyleId;  // "Normal" in English, but ...
	String defaultCharacterStyleId;
	
	private void init() throws Docx4JException {

//		styleDefinitionsPart.createVirtualStylesForDocDefaults();
		
		try {
			defaultParagraphStyleId = this.styleDefinitionsPart.getDefaultParagraphStyle().getStyleId();
		} catch (NullPointerException npe) {
			log.warn("No default paragraph style!!");
		}
		try {
			defaultCharacterStyleId = this.styleDefinitionsPart.getDefaultCharacterStyle().getStyleId();
		} catch (NullPointerException npe) {
			log.warn("No default character style!!");
		}

		// Initialise styles
		styles = (org.docx4j.wml.Styles)styleDefinitionsPart.getJaxbElement();	
		initialiseLiveStyles();		
		
//		Style docDefaults = styleDefinitionsPart.getStyleById("DocDefaults");
		DocDefaults docDefaults = styleDefinitionsPart.getJaxbElement().getDocDefaults();
        if(log.isDebugEnabled()) {
            log.debug(XmlUtils.marshaltoString(docDefaults, true, true));
        }

		documentDefaultPPr = new PPr();
		documentDefaultRPr = new RPr();        	

		if (docDefaults!=null
				&& docDefaults.getPPrDefault()!=null
				&& docDefaults.getPPrDefault().getPPr()!=null) {
			
			documentDefaultPPr = docDefaults.getPPrDefault().getPPr();
        }
		
		if (docDefaults!=null
				&& docDefaults.getRPrDefault()!=null
				&& docDefaults.getRPrDefault().getRPr()!=null) {
			
			documentDefaultRPr = docDefaults.getRPrDefault().getRPr();
        }
		
		if (documentDefaultRPr.getSz()==null) {
			// Make Word default explicit
			HpsMeasure sz20 = new HpsMeasure(); 
			sz20.setVal(BigInteger.valueOf(20));
			documentDefaultRPr.setSz(sz20);
		}

		addNormalToResolvedStylePPrComponent();
		addDefaultParagraphFontToResolvedStyleRPrComponent();
	}


	private void addNormalToResolvedStylePPrComponent() {
		
		Stack<PPr> pPrStack = new Stack<PPr>();
//		String styleId = "Normal";
		String styleId = defaultParagraphStyleId;
		
		fillPPrStack(styleId, pPrStack);
		pPrStack.push(documentDefaultPPr);
			
		PPr effectivePPr = factory.createPPr();			
		// Now, apply the properties starting at the top of the stack
		while (!pPrStack.empty() ) {
			PPr pPr = pPrStack.pop();
			applyPPr(pPr, effectivePPr);
		}
		resolvedStylePPrComponent.put(styleId, effectivePPr);		
	}

	private void addDefaultParagraphFontToResolvedStyleRPrComponent() {
	Stack<RPr> rPrStack = new Stack<RPr>();

		fillRPrStack(defaultParagraphStyleId, rPrStack);
			// Since default font size might be in there.
		
		fillRPrStack(defaultCharacterStyleId, rPrStack);
		rPrStack.push(documentDefaultRPr);
			
		RPr effectiveRPr = factory.createRPr();
		
		// Now, apply the properties starting at the top of the stack
		while (!rPrStack.empty() ) {
			RPr rPr = rPrStack.pop();
			applyRPr(rPr, effectiveRPr);
		}
		resolvedStyleRPrComponent.put(defaultCharacterStyleId, effectiveRPr);		
	}
	
	public Style getEffectiveTableStyle(TblPr tblPr) {
		// OK to pass this a null tblPr.
		
		Stack<Style> tableStyleStack = new Stack<Style>();
		
		if (tblPr !=null && tblPr.getTblStyle()!=null) {
			String styleId = tblPr.getTblStyle().getVal();
			log.debug("Table style: " + styleId);
			fillTableStyleStack(styleId, tableStyleStack);
		} else {
			log.debug("No table style specified");
		}
		
		Style result;
		if (tableStyleStack.size()>0 ) {
			result = XmlUtils.deepCopy(tableStyleStack.pop());
		} else {
			result = Context.getWmlObjectFactory().createStyle();
			CTTblPrBase emptyPr = Context.getWmlObjectFactory().createCTTblPrBase();
			result.setTblPr(emptyPr);
			if (tblPr==null) {
				// Return empty style object
				log.info("Generated empty tblPr" );
				return result;
			}			
		}
		while (!tableStyleStack.empty() ) {
			StyleUtil.apply(tableStyleStack.pop(), result);
		}
		
		// Finally apply the tblPr we were passed
		result.setTblPr(StyleUtil.apply(tblPr, result.getTblPr()));
		
		// Sanity check
		if (result.getTblPr()==null) {
			log.error("Null tblPr. FIXME" );
		}
		
		return result;
	}
	
	/**
	 * Ascend the style hierarchy, capturing the table styles
	 *  
	 * @param stylename
	 * @param effectivePPr
	 */
	private void fillTableStyleStack(String styleId, Stack<Style> tableStyleStack) {
		// get the style
		Style style = liveStyles.get(styleId);
		
		// add it to the stack
		if (style==null) {
			// No such style!
			// For now, just log it..
			log.error("Style definition not found: " + styleId);
			return;
		}
		
		tableStyleStack.push(style);
		log.debug("Added " + styleId + " to table style stack");
		
		// if it is based on, recurse
    	if (style.getBasedOn()==null) {
			log.debug("Style " + styleId + " is a root style.");
    	} else if (style.getBasedOn().getVal()!=null) {
        	String basedOnStyleName = style.getBasedOn().getVal();           	
        	fillTableStyleStack( basedOnStyleName, tableStyleStack);
    	} else {
    		log.debug("No basedOn set for: " + style.getStyleId() );
    	}
		
	}
	
	
	/**
	 * Follow the resolution rules to return the
	 * paragraph properties which actually apply,
	 * given this pPr element (on a w:p).
	 * 
	 * Note 1:  the properties are not the definition
	 * of any style name returned.
	 * Note 2:  run properties are not resolved 
	 * or returned by this method.
	 * 
	 * What is returned is a live object.  If you
	 * want to change it, you should clone it first!
	 *  
	 * @param expressPPr
	 * @return
	 */
	public PPr getEffectivePPr(PPr expressPPr) {
		
		// Used by docx4j-export-fo project.
		
		PPr effectivePPr = null;
		//	First, the document defaults are applied
		
			// Done elsewhere
			// PPr effectivePPr = (PPr)XmlUtils.deepCopy(documentDefaultPPr);
		
		//	Next, the table style properties are applied to each table in the document, 
		//	following the conditional formatting inclusions and exclusions specified 
		//	per table. 
		
			// TODO - if the paragraph is in a table?
				
		//	Next, numbered item and paragraph properties are applied to each paragraph 
		//	formatted with a *numbering *style**.
		
			// TODO - who uses numbering styles (as opposed to numbering
			// via a paragraph style or direct formatting)?
		
		//  Next, paragraph and run properties are 
		//	applied to each paragraph as defined by the paragraph style.
		PPr resolvedPPr = null;
		String styleId;
		if (expressPPr == null || expressPPr.getPStyle() == null ) {
//			styleId = "Normal";
			styleId = defaultParagraphStyleId;
			
		} else {
			styleId = expressPPr.getPStyle().getVal();
			if (styleId==null) {
                if(log.isWarnEnabled()) {
                    log.warn("Missing style id: " + XmlUtils.marshaltoString(expressPPr));
                }
				if (log.isDebugEnabled()) {
					Throwable t = new Throwable();
					log.debug("Null styleId produced by code path", t);
				} else {
					log.warn("Enable debug level logging to see code path");					
				}
				styleId = defaultParagraphStyleId;
			}
		}
		resolvedPPr = getEffectivePPr(styleId);
		
				
		//	Next, run properties are applied to each run with a specific character style 
		//	applied. 
		
			// Not for pPr
				
		//	Finally, we apply direct formatting (paragraph or run properties not from 
		//	styles).		
		if (hasDirectPPrFormatting(expressPPr) ) {
			if (resolvedPPr==null) {
				log.warn("resolvedPPr was null. Look into this?");
				effectivePPr = Context.getWmlObjectFactory().createPPr();
			} else {
				effectivePPr = (PPr)XmlUtils.deepCopy(resolvedPPr);
			}
			applyPPr(expressPPr, effectivePPr);
			return effectivePPr;
		} else {
			return resolvedPPr;
		}
		
	}

	/**
	 * Follow the resolution rules to return the
	 * paragraph properties which actually apply,
	 * given this paragraph style
	 * 
	 * Note 1:  the properties are not the definition
	 * of any style name returned.
	 * Note 2:  run properties are not resolved 
	 * or returned by this method.
	 * 
	 * What is returned is a live object.  If you
	 * want to change it, you should clone it first!
	 *  
	 * @param expressPPr
	 * @return
	 */
	public PPr getEffectivePPr(String styleId) {
		
		PPr resolvedPPr = resolvedStylePPrComponent.get(styleId);
		
		if (resolvedPPr!=null) {
			return resolvedPPr;
		}
		
		// Hmm, have to do the work
		Style s = liveStyles.get(styleId);
		
		if (s==null) {
			log.error("Couldn't find style: " + styleId);
			return null;
		}
		
//		PPr expressPPr = s.getPPr();
//		if (expressPPr==null) {
//			// A paragraph style may have no w:pPr component
//			log.debug("style: " + styleId + " has no PPr");
//			String normalId = this.styleDefinitionsPart.getDefaultParagraphStyle().getStyleId();			
//			resolvedPPr = resolvedStylePPrComponent.get(normalId);
//			return resolvedPPr;
//		}
		
		//  Next, paragraph and run properties are 
		//	applied to each paragraph as defined by the paragraph style.
		Stack<PPr> pPrStack = new Stack<PPr>();
		// Haven't done this one yet				
		fillPPrStack(styleId, pPrStack);
		// Finally, on top
		pPrStack.push(documentDefaultPPr);
						
		resolvedPPr = factory.createPPr();			
		// Now, apply the properties starting at the top of the stack
		while (!pPrStack.empty() ) {
			PPr pPr = pPrStack.pop();
			applyPPr(pPr, resolvedPPr);
		}
		resolvedStylePPrComponent.put(styleId, resolvedPPr);
		return resolvedPPr;
	}
	
	/**
	 * @param expressRPr
	 * @param pPr - 
	 * @return
	 */
	public RPr getEffectiveRPr(RPr expressRPr, PPr pPr) {
		
		// NB Currently used in PDF viaXSLFO only
		
		if (pPr==null) {
			log.debug("pPr was null");
		} else {
			// At the pPr level, what rPr do we have?
			// .. ascend the paragraph style tree
			if (pPr.getPStyle()==null) {
				if (log.isDebugEnabled()) {
					log.debug("No pstyle:");
					log.debug(XmlUtils.marshaltoString(pPr, true, true));
				}
			} else {
				log.debug("pstyle:" + pPr.getPStyle().getVal());
				RPr pPrLevelRunStyle = getEffectiveRPr(pPr.getPStyle().getVal());
				// .. and apply those
				
				log.debug("Resulting pPrLevelRunStyle: " + XmlUtils.marshaltoString(pPrLevelRunStyle));
				
				return getEffectiveRPrUsingPStyleRPr(expressRPr, pPrLevelRunStyle);
			}
			// Check Paragraph rPr (our special hack of using ParaRPr to format a fo:block)
			// 2013 10 02: doubts whether this is right?
			if ((expressRPr == null) && (pPr.getRPr() != null) && (hasDirectRPrFormatting(pPr.getRPr())) ) {
				log.debug("No express rPr, using p level");
				return getEffectiveRPrUsingPStyleRPr(expressRPr, 
						StyleUtil.apply(pPr.getRPr(), Context.getWmlObjectFactory().createRPr()));
			} 
			log.debug("pPr not null, but falling through");
			
		}

		return getEffectiveRPrUsingPStyleRPr( expressRPr, null);
		
	}

	/**
	 * Return effective rPr, as follows: Starting with the rPr from the pStyle, 
	 * apply character style (if any) specified on the run,
	 * then any other direct (ad-hoc) run formatting.
	 * 
	 * @param expressRPr
	 * @param rPrFromPStyle should be rPr from the paragraph style (as opposed to rPr in the direct pPr, which is only relevant to the paragraph mark)
	 * @return
	 */
	public RPr getEffectiveRPrUsingPStyleRPr(RPr expressRPr, RPr rPrFromPStyle) {
		
		// Note, RPr pPrLevelRunStyle should be rPr from the paragraph style
		// (as opposed to rPr in the direct pPr, which is only
		//  relevant to the paragraph mark)
		
		log.debug("in getEffectiveRPrUsingPStyle");
//		Throwable t = new Throwable();
//		t.printStackTrace();
		
		
//		Idea is that you pass pPr if you are using this for XSL FO,
//		since we need to take account of rPr in paragraph styles
//		(but not the rPr in a pPr direct formatting, since
//       that only applies to the paragraph mark). 
//		 * For HTML/CSS, this would be null (since the pPr level rPr 
//		 * is made into a separate style applied via a second value in
//		 * the class attribute).  But, in the CSS case, this
		// function is not used - since the rPr is made into a style as well.
		

		//	First, the document defaults are applied		
		RPr effectiveRPr = null;
		if (documentDefaultRPr==null) {
			log.warn("documentDefaultRPr null");
			effectiveRPr = Context.getWmlObjectFactory().createRPr();
		} else {		
			effectiveRPr = (RPr)XmlUtils.deepCopy(documentDefaultRPr);
		}
//			
//			// Apply DefaultParagraphFont.  We only do it explicitly
//			// here as per conditions, because if there is a run style,
//			// walking the hierarchy will include this if it is needed
//			if (expressRPr == null || expressRPr.getRStyle() == null ) {
//				applyRPr(resolvedStyleRPrComponent.get(defaultCharacterStyleId), effectiveRPr);								
//			}
		
		if (rPrFromPStyle!=null) {
			effectiveRPr=StyleUtil.apply(rPrFromPStyle, effectiveRPr);
		}		
		
		
//    	System.out.println("start\n" + XmlUtils.marshaltoString(effectiveRPr, true, true));
		
		
		//	Next, the table style properties are applied to each table in the document, 
		//	following the conditional formatting inclusions and exclusions specified 
		//	per table. 
		
			// TODO - if the paragraph is in a table?
				
		//	Next, numbered item and paragraph properties are applied to each paragraph 
		//	formatted with a *numbering *style**.
		
//			 TODO - who uses numbering styles (as opposed to numbering
			// via a paragraph style or direct formatting)?
		
		//  Next, paragraph and run properties are 
		//	applied to each paragraph as defined by the paragraph style
		// (this includes run properties defined in a paragraph style,
		//  but not run properties directly included in a pPr in the
		//  document (those only apply to a paragraph mark).
//		applyRPr(pPrLevelRunStyle, effectiveRPr);
					
		//	Next, run properties are applied to each run with a specific character style 
		//	applied. 		
		RPr resolvedRPr = null;
		String runStyleId;
		if (expressRPr != null && expressRPr.getRStyle() != null ) {
			runStyleId = expressRPr.getRStyle().getVal();
			
			// we want to ignore doc default contrib, if rFonts or sz is set in the pStyle
			boolean rFontsMissing = effectiveRPr.getRFonts()==null;
			boolean szMissing = effectiveRPr.getSz()==null;
			resolvedRPr = getEffectiveRPr(runStyleId, rFontsMissing, szMissing); 
			StyleUtil.apply(resolvedRPr, effectiveRPr);
//	    	System.out.println("resolved=\n" + XmlUtils.marshaltoString(resolvedRPr, true, true));
			
		}
//    	System.out.println("effective is now\n" + XmlUtils.marshaltoString(effectiveRPr, true, true));
				
		//	Finally, we apply direct formatting (run properties not from 
		//	styles).		
		if (hasDirectRPrFormatting(expressRPr) ) {			
			//effectiveRPr = (RPr)XmlUtils.deepCopy(effectiveRPr);			
//	    	System.out.println("applying express\n" + XmlUtils.marshaltoString(expressRPr, true, true));
			StyleUtil.apply(expressRPr, effectiveRPr);
		} 
		return effectiveRPr;
		
	}


	/**
	 * apply the rPr in the stack of styles, including documentDefaultRPr
	 * 
	 * @param styleId
	 * @return
	 */
	public RPr getEffectiveRPr(String styleId) {

		return getEffectiveRPr(styleId, true, true); 
	}
	/**
	 * apply the rPr in the stack of styles, optionally including documentDefaultRPr
	 * 
	 * @param styleId
	 * @return
	 * @since 8.2.4
	 */
	public RPr getEffectiveRPr(String styleId, 
			boolean applyDocDefaultsRFonts, boolean applyDocDefaultsSz) {
		// styleId passed in could be a run style
		// or a *paragraph* style
		
		RPr resolvedRPr = resolvedStyleRPrComponent.get(styleId);
		
		if (resolvedRPr!=null) {
			return resolvedRPr;
		}
		
		// Hmm, have to do the work
		Style s = liveStyles.get(styleId);
		
		if (s==null) {			
			log.error("Couldn't find style: " + styleId);
			log.debug("Couldn't find style: " + styleId, new Throwable());
			return null;
		}

		// Comment out - this style might not have rPr,
		// but an ancestor might!
		
//		RPr expressRPr = s.getRPr();
//		if (expressRPr==null) {
//			log.error("style: " + runStyleId + " has no RPr");
//			resolvedRPr = resolvedStyleRPrComponent.get(defaultCharacterStyleId);
//			return resolvedRPr;
//		}

		
		//	Next, run properties are applied to each run with a specific character style applied. 		
		Stack<RPr> rPrStack = new Stack<RPr>();
		fillRPrStack(styleId, rPrStack);
		
		// Finally, on top
		// The intent is to be able to ignore doc default contrib, 
		// where rFonts or sz is set in the pStyle
		RPr defaultRPr = new RPr();
		if (applyDocDefaultsRFonts) {
			defaultRPr.setRFonts(this.documentDefaultRPr.getRFonts());
		}
		if (applyDocDefaultsSz) {
			defaultRPr.setSz(this.documentDefaultRPr.getSz());
			defaultRPr.setSzCs(this.documentDefaultRPr.getSzCs());
		}
		rPrStack.push(defaultRPr); 
						
		resolvedRPr = factory.createRPr();			
		// Now, apply the properties starting at the top of the stack
		while (!rPrStack.empty() ) {
			RPr rPr = rPrStack.pop();
            if(log.isDebugEnabled()) {
                log.debug("applying " + XmlUtils.marshaltoString(rPr));
            }
			applyRPr(rPr, resolvedRPr);
		}
		resolvedStyleRPrComponent.put(styleId, resolvedRPr);
		return resolvedRPr;
	}
	
	org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
	
	private BooleanDefaultTrue newBooleanDefaultTrue(boolean val) {
		
		BooleanDefaultTrue newBooleanDefaultTrue = factory.createBooleanDefaultTrue();
		newBooleanDefaultTrue.setVal(Boolean.valueOf(val));
		return newBooleanDefaultTrue;
	}
	
	private boolean hasDirectPPrFormatting(PPr pPrToApply) {
		
		// NB, any rPr is intentionally ignored,
		// since pPr/rPr is not applicable to anything
		// except the paragraph mark
		
		if (pPrToApply==null) {
			return false;
		}
		
		// Here is where we do the real work.  
		// There are a lot of paragraph properties
		// The below list is taken directly from PPrBase.
		
		//PPrBase.PStyle pStyle;
		
			// Ignore

		if (pPrToApply.getBidi()!=null) {
			return true;		
		}
		
		
		//BooleanDefaultTrue keepNext;
		if (pPrToApply.getKeepNext()!=null) {
			return true;		
		}
		
	
		//BooleanDefaultTrue keepLines;
		if (pPrToApply.getKeepLines()!=null) {
			return true;		
		}
	
		//BooleanDefaultTrue pageBreakBefore;
		if (pPrToApply.getPageBreakBefore()!=null) {
			return true;		
		}
	
		//CTFramePr framePr;
		//BooleanDefaultTrue widowControl;
		if (pPrToApply.getWidowControl()!=null) {
			return true;		
		}
	
		//PPrBase.NumPr numPr;
		//NumPr numPr;
		if (pPrToApply.getNumPr()!=null) {
			return true;		
		}
	
		//BooleanDefaultTrue suppressLineNumbers;
		if (pPrToApply.getSuppressLineNumbers()!=null) {
			return true;		
		}
		
		// PBdr pBdr;
		if (pPrToApply.getPBdr()!=null) {
			return true;		
		}
	
		//CTShd shd;
		if (pPrToApply.getShd()!=null) {
			return true;		
		}
			
		//Tabs tabs;
		if (pPrToApply.getTabs()!=null) {
			return true;		
		}
	
		//BooleanDefaultTrue suppressAutoHyphens;
		//BooleanDefaultTrue kinsoku;
		//BooleanDefaultTrue wordWrap;
		//BooleanDefaultTrue overflowPunct;
		//BooleanDefaultTrue topLinePunct;
		//BooleanDefaultTrue autoSpaceDE;
		//BooleanDefaultTrue autoSpaceDN;
		//BooleanDefaultTrue bidi;
		//BooleanDefaultTrue adjustRightInd;
		//BooleanDefaultTrue snapToGrid;
		//PPrBase.Spacing spacing;
		if (pPrToApply.getSpacing()!=null) {
			return true;		
		}
	
		//PPrBase.Ind ind;
		if (pPrToApply.getInd()!=null) {
			return true;		
		}
	
		//BooleanDefaultTrue contextualSpacing;
		//BooleanDefaultTrue mirrorIndents;
		//BooleanDefaultTrue suppressOverlap;
		//Jc jc;
		if (pPrToApply.getJc()!=null) {
			return true;		
		}
	
		//TextDirection textDirection;
		//PPrBase.TextAlignment textAlignment;
		if (pPrToApply.getTextAlignment()!=null ) {
			return true;		
		}
	
		//CTTextboxTightWrap textboxTightWrap;
		//PPrBase.OutlineLvl outlineLvl;
		if (pPrToApply.getOutlineLvl()!=null ) {
			return true;		
		}
		//PPrBase.DivId divId;
		//CTCnf cnfStyle;
		return false;
		
	}
	
	
	protected void applyPPr(PPr pPrToApply, PPr effectivePPr) {
        if(log.isDebugEnabled()) {
            log.debug("apply " + XmlUtils.marshaltoString(pPrToApply, true, true)
                    + "\n\r to " + XmlUtils.marshaltoString(effectivePPr, true, true));
        }
		
		StyleUtil.apply(pPrToApply, effectivePPr);
		
//		if (pPrToApply==null) {
//			return;
//		}
//		
//    	List<Property> properties = PropertyFactory.createProperties(wordMLPackage, pPrToApply); 
//    	for( Property p :  properties ) {
//			if (p!=null) {
////				log.debug("applying pPr " + p.getClass().getName() );
//				((AbstractParagraphProperty)p).set(effectivePPr);  // NB, this new method does not copy. TODO?
//			}
//    	}

        if(log.isDebugEnabled()) {
            log.debug("result " + XmlUtils.marshaltoString(effectivePPr, true, true));
        }
    	
	}
	
	public boolean hasDirectRPrFormatting(RPr rPrToApply) {
		
		if (rPrToApply==null) {
			return false;
		}
		
		// Here is where we do the real work.  
		// There are a lot of run properties
		// The below list is taken directly from RPr, and so
		// is comprehensive.
		
		//RStyle rStyle;
		//RFonts rFonts;
		if (rPrToApply.getRFonts()!=null ) {
			return true;
		}

		//BooleanDefaultTrue b;
		if (rPrToApply.getB()!=null) {
			return true;			
		}

		//BooleanDefaultTrue bCs;
		//BooleanDefaultTrue i;
		if (rPrToApply.getI()!=null) {
			return true;			
		}

		//BooleanDefaultTrue iCs;
		//BooleanDefaultTrue caps;
		if (rPrToApply.getCaps()!=null) {
			return true;			
		}

		//BooleanDefaultTrue smallCaps;
		if (rPrToApply.getSmallCaps()!=null) {
			return true;			
		}

		//BooleanDefaultTrue strike;
		if (rPrToApply.getStrike()!=null) {
			return true;			
		}
		//BooleanDefaultTrue dstrike;
		//BooleanDefaultTrue outline;
		//BooleanDefaultTrue shadow;
		//BooleanDefaultTrue emboss;
		//BooleanDefaultTrue imprint;
		//BooleanDefaultTrue noProof;
		//BooleanDefaultTrue snapToGrid;
		//BooleanDefaultTrue vanish;
		//BooleanDefaultTrue webHidden;
		//Color color;
		if (rPrToApply.getColor()!=null ) {
			return true;			
		}

		//CTSignedTwipsMeasure spacing;
		//CTTextScale w;
		//HpsMeasure kern;
		//CTSignedHpsMeasure position;
		//HpsMeasure sz;
		if (rPrToApply.getSz()!=null ) {
			return true;			
		}

		//HpsMeasure szCs;
		//Highlight highlight;
		if (rPrToApply.getHighlight()!=null ) {
			return true;			
		}
		//U u;
		if (rPrToApply.getU()!=null ) {
			return true;			
		}

		//CTTextEffect effect;
		//CTBorder bdr;
		if (rPrToApply.getBdr()!=null ) {
			return true;			
		}
		//CTShd shd;
		if (rPrToApply.getShd()!=null ) {
			return true;			
		}
		//CTFitText fitText;
		//CTVerticalAlignRun vertAlign;
		if (rPrToApply.getVertAlign()!=null ) {
			return true;			
		}
		//BooleanDefaultTrue rtl;
		//BooleanDefaultTrue cs;
		//CTEm em;
		//CTLanguage lang;
		//CTEastAsianLayout eastAsianLayout;
		//BooleanDefaultTrue specVanish;
		//BooleanDefaultTrue oMath;
		//CTRPrChange rPrChange;
		
		// If we got here...
		return false;
	}
	
	private boolean hasDirectRPrFormatting(ParaRPr rPrToApply) {
		
		if (rPrToApply==null) {
			return false;
		}
		
		// Here is where we do the real work.  
		// There are a lot of run properties
		// The below list is taken directly from RPr, and so
		// is comprehensive.
		
		//RStyle rStyle;
		//RFonts rFonts;
		if (rPrToApply.getRFonts()!=null ) {
			return true;
		}

		//BooleanDefaultTrue b;
		if (rPrToApply.getB()!=null) {
			return true;			
		}

		//BooleanDefaultTrue bCs;
		//BooleanDefaultTrue i;
		if (rPrToApply.getI()!=null) {
			return true;			
		}

		//BooleanDefaultTrue iCs;
		//BooleanDefaultTrue caps;
		if (rPrToApply.getCaps()!=null) {
			return true;			
		}

		//BooleanDefaultTrue smallCaps;
		if (rPrToApply.getSmallCaps()!=null) {
			return true;			
		}

		//BooleanDefaultTrue strike;
		if (rPrToApply.getStrike()!=null) {
			return true;			
		}
		//BooleanDefaultTrue dstrike;
		//BooleanDefaultTrue outline;
		//BooleanDefaultTrue shadow;
		//BooleanDefaultTrue emboss;
		//BooleanDefaultTrue imprint;
		//BooleanDefaultTrue noProof;
		//BooleanDefaultTrue snapToGrid;
		//BooleanDefaultTrue vanish;
		//BooleanDefaultTrue webHidden;
		//Color color;
		if (rPrToApply.getColor()!=null ) {
			return true;			
		}

		//CTSignedTwipsMeasure spacing;
		//CTTextScale w;
		//HpsMeasure kern;
		//CTSignedHpsMeasure position;
		//HpsMeasure sz;
		if (rPrToApply.getSz()!=null ) {
			return true;			
		}

		//HpsMeasure szCs;
		//Highlight highlight;
		//U u;
		if (rPrToApply.getU()!=null ) {
			return true;			
		}

		//CTTextEffect effect;
		//CTBorder bdr;
		//CTShd shd;
		//CTFitText fitText;
		//CTVerticalAlignRun vertAlign;
		//BooleanDefaultTrue rtl;
		//BooleanDefaultTrue cs;
		//CTEm em;
		//CTLanguage lang;
		//CTEastAsianLayout eastAsianLayout;
		//BooleanDefaultTrue specVanish;
		//BooleanDefaultTrue oMath;
		//CTRPrChange rPrChange;
		
		// If we got here...
		return false;
	}
	
	protected void applyRPr(RPr rPrToApply, RPr effectiveRPr) {
		
		if (rPrToApply==null) {
			return;
		}
		
		StyleUtil.apply(rPrToApply, effectiveRPr);
		
//    	List<Property> properties = PropertyFactory.createProperties(null, rPrToApply); // wmlPackage null
//    	
//    	for( Property p :  properties ) {
//			if (p!=null) {    		
//				((AbstractRunProperty)p).set(effectiveRPr);  // NB, this new method does not copy. TODO?
//			}
//    	}
		
	}	
	
	protected void applyRPr(ParaRPr rPrToApply, RPr effectiveRPr) {
		
		if (rPrToApply==null) {
			return;
		}

		StyleUtil.apply(rPrToApply, effectiveRPr);
		
//    	List<Property> properties = PropertyFactory.createProperties(null, rPrToApply); // wmlPackage null
//    	
//    	for( Property p :  properties ) {
//			if (p!=null) {    		
//				((AbstractRunProperty)p).set(effectiveRPr);  // NB, this new method does not copy. TODO?
//			}
//    	}
	}	
	
    private static final String HEADING_STYLE = "Heading";
	
    /*
     * @since 3.0.2
     */
    public int getLvlFromHeadingStyle(String style){
    	// Note that this is done using the style ID, not its OutlineLevel,
    	// since Word does it purely on the name of the style!
        int level = -1;
        try{
            level = Integer.parseInt(style.substring(HEADING_STYLE.length(), style.length()).trim());
        } catch (NumberFormatException ex){
            //log.debug(style + " - what level is this? ");
        }

        return level;
    }	
	
	/**
	 * Ascend (recursively) the style hierarchy, capturing the pPr bit.
	 * 
	 * Doesn't use StyleTree.
	 *  
	 * @param stylename
	 * @param effectivePPr
	 */
	private void fillPPrStack(String styleId, Stack<PPr> pPrStack) {
		// The return value is the style on which styleId is based.
		
		// It is purely for the purposes of ascertainNumId (? no, its used by getEffectivePPr(styleId))
		
		if (styleId==null) {
			if (log.isDebugEnabled()) {
				Throwable t = new Throwable();
				log.debug("Null styleId produced by code path", t);
			} else {
				log.warn("Null styleId; Enable debug level logging to see code path");					
			}
			return;
		}
		
		// get the style
		Style style = liveStyles.get(styleId);
		
		// add it to the stack
		if (style==null) {
			// No such style!
			// For now, just log it..
			if (styleId!=null
					&& styleId.equals("DocDefaults")) {
				
				// Don't worry about this.
				// SDP.createVirtualStylesForDocDefaults()
				// creates a DocDefaults style, and makes Normal based on it
				// (and so if a different approach to handling
				//  DocDefaults ... we really should do it one
				//  way consistently).
				// The problem here is, that is typically done
				// after the PropertyResolver is created,
				// so as far as this PropertyResolver is 
				// concerned, the style doesn't exist.
				// And we don't really want to always
				// do createVirtualStylesForDocDefaults() before
				// or during init of PropertyResolver, since that
				// mean any docx saved would contain those
				// virtual styles.
				// Anyway, we don't need to worry about it
				// here, because the doc defaults are still handled...
				
			} else {
				log.error("Style definition not found: " + styleId);
			}
			return;
		}
		
		// For heading styles, check the outline level is as expected
		if (styleId.startsWith(HEADING_STYLE)) {
			
			int level = getLvlFromHeadingStyle(styleId);
			if (level>0
					&& style.getPPr()!=null
					&& style.getPPr().getOutlineLvl()!=null
					&& style.getPPr().getOutlineLvl().getVal()!=null
					&& style.getPPr().getOutlineLvl().getVal().intValue()!=(level-1)) {
				
				// must use the outline level appropriate to this heading!
				// No need to clone, since Microsoft Word automatically overwrites like this!
				log.info(styleId + " - reset actual outline level with " + (level-1));
				style.getPPr().getOutlineLvl().setVal(BigInteger.valueOf(level-1));
			} 
			
			pPrStack.push(style.getPPr());				
		} else {
			pPrStack.push(style.getPPr());
		}
		
		log.debug("Added " + styleId + " to pPr stack");
		
		// Some styles contain numPr, without specifying
		// their numId!  In this case you have to get it
		// from the numPr in their basedOn style.
		// To save numbering emulator from having to do
		// that work, we make the numId explicit here.
		boolean ascertainNumId = false;
		if (style.getPPr()!=null 
				&& style.getPPr().getNumPr()!=null
				&& style.getPPr().getNumPr().getNumId()==null) {

			ascertainNumId = true;			
			log.debug(styleId +" ascertainNumId: " + ascertainNumId);
		} else {
			log.debug(styleId +" ascertainNumId: " + ascertainNumId);			
		}
		
		// if it is based on, recurse
    	if (style.getBasedOn()==null) {
			log.debug("Style " + styleId + " is a root style.");
    	} else if (style.getBasedOn().getVal()!=null) {
        	String basedOnStyleName = style.getBasedOn().getVal();           	
			log.debug("Style " + styleId + " is based on " + basedOnStyleName);
        	fillPPrStack( basedOnStyleName, pPrStack);
        	Style basedOnStyle = liveStyles.get(basedOnStyleName);
        	if (ascertainNumId && basedOnStyle!=null) {
        		// This works via recursion        		
        		//log.debug( XmlUtils.marshaltoString(basedOnStyle, true, true));
        		if (basedOnStyle.getPPr()!=null 
        				&& basedOnStyle.getPPr().getNumPr()!=null
        				&& basedOnStyle.getPPr().getNumPr().getNumId()!=null) {
        			NumId numId = basedOnStyle.getPPr().getNumPr().getNumId();
        			// Attach it at this level - for this to work,
        			// you can't have a style in the basedOn hierarchy
        			// which doesn't have a numPr element, because
        			// in that case there is nowhere to hang the style
        			style.getPPr().getNumPr().setNumId(numId);
        			log.info("Injected numId " + numId);
        		}
        	}
    	} else {
    		log.debug("No basedOn set for: " + style.getStyleId() );
    	}
		
	}

	/**
	 * Ascend the style hierarchy, capturing the rPr bit
	 *  
	 * @param stylename
	 * @param effectivePPr
	 */
	private void fillRPrStack(String styleId, Stack<RPr> rPrStack) {
		
		// get the style
		Style style = liveStyles.get(styleId);
		
		// add it to the stack
		if (style==null) {
			// No such style!
			// For now, just log it..
			log.error("Style definition not found: " + styleId);
			return;
		}
		rPrStack.push(style.getRPr());
		log.debug("Added " + styleId + " to rPr stack");
		
		// if it is based on, recurse
    	if (style.getBasedOn()==null) {
			log.debug("Style " + styleId + " is a root style.");
    	} else if (style.getBasedOn().getVal()!=null) {
        	String basedOnStyleName = style.getBasedOn().getVal();
//        	if (styleId.equals(basedOnStyleName)) {
//    		log.error(XmlUtils.marshaltoString(style));
//    		throw new RuntimeException(styleId + " is basedOn itself!");
//    	} 
        	fillRPrStack( basedOnStyleName, rPrStack);
    	} else {
    		log.debug("No basedOn set for: " + style.getStyleId() );
    	}
		
	}
	
	
    private void initialiseLiveStyles() {
    	
    	log.debug("initialiseLiveStyles()");
		liveStyles = new java.util.HashMap<String, org.docx4j.wml.Style>();
		
		for ( org.docx4j.wml.Style s : styles.getStyle() ) {				
			liveStyles.put(s.getStyleId(), s);	
			log.debug("live style: " + s.getStyleId() );
		}
    	
    }

	
    public boolean activateStyle( String styleId  ) {
    	
    	if (liveStyles.get(styleId)!=null) {
    		// Its already live - nothing to do
    		return true;    		
    	}
    	// Assumption here is that it doesn't exist in your styles part, so..
    	java.util.Map<String, org.docx4j.wml.Style> knownStyles 
    		= StyleDefinitionsPart.getKnownStyles(); // NB KnownStyles.xml, not those in docx!
    	
    	org.docx4j.wml.Style s = knownStyles.get(styleId);
    	
    	if (s==null) {
    		log.error("Unknown style: " + styleId);
    		return false;
    	}
    	    	
    	return activateStyle(s, false); 
    		// false -> don't replace an existing live style with a template
    	
    }
    
    public boolean activateStyle(org.docx4j.wml.Style s) {

    	return activateStyle(s, true);
    	
    }

    private boolean activateStyle(org.docx4j.wml.Style s, boolean replace) {
    	
    	if (liveStyles.get(s.getStyleId())!=null) {
    		// Its already live
    		
    		if (!replace) {    			
    			return false;
    		}
    		
    		// Remove existing entry
			styles.getStyle().remove( 
					liveStyles.get(s.getStyleId()) );				
    	}
    	
    	// Add it
    	// .. to the JAXB object
    	styles.getStyle().add(s);
    	// .. here
    	liveStyles.put(s.getStyleId(), s);
    	
    	// Now, recursively check that what it is based on is present
    	boolean result1;
    	if (s.getBasedOn()!=null) {
    		String basedOn = s.getBasedOn().getVal();
    		result1 = activateStyle( basedOn );
    		
    	} else if ( s.getStyleId().equals(defaultParagraphStyleId)
    			|| s.getStyleId().equals(defaultCharacterStyleId) )
    	{
    		// stop condition
    		result1 = true;
    	} else {
    		
    		log.debug( s.getStyleId() + "  not w:basedOn anything, but that's ok");
    		result1 = true;
    	}
    	
    	// Also add the linked style, if any
    	// .. Word might expect it to be there
    	boolean result2 = true;
    	if (s.getLink()!=null) {
    		
    		org.docx4j.wml.Style.Link link = s.getLink();
    		result2 = activateStyle(link.getVal());
    		
    	}
    	
    	return (result1 & result2);
    	    	
    }
    
    public org.docx4j.wml.Style getStyle(String styleId) {
    	
    	return liveStyles.get(styleId);
    }
	
//		/*
//		a paragraph style does not inherit anything from its linked character style.
//
//		A linked character style seems to be just a Word 2007 user interface
//		hint.  ie if you select some characters in a paragraph and select to
//		apply "Heading 1", what you are actually doing is applying "Heading 1
//		char".  This is determined by looking at the definition of the
//		"Heading 1" style to see what its linked style is.
//		
//		(Interestingly, in Word 2007, if you right click to modify something 
//		 which is Heading 1 char, it modifies both the Heading 1 style and the
//		 Heading 1 char style!.  Haven't looked to see what happens to Heading 1 char
//		 style if you right click to modify a Heading 1 par.)
//
//		 The algorithm Word 2007 seems to use is:
//		    look at the specified style:
//		        1 does it have its own rPr which contains rFonts?
//		        2 if not, what does this styles basedOn style say? (Ignore
//		any linked char style)
//				3 look at styles/rPrDefault 
//				3.1 if there is an rFonts element, do what it says (it may refer you to the theme part, 
//				    in which case if there is no theme part, default to "internally stored settings"
//					(there is no normal.dot; see http://support.microsoft.com/kb/924460/en-us ) 
//					in this case Calibri and Cambria)
//				3.2 if there is no rFonts element, default to Times New Roman.
//		

    //        // 1 does it have its own rPr which contains rFonts?
//    	org.docx4j.wml.RPr rPr = style.getRPr();
//    	if (rPr!=null && rPr.getRFonts()!=null) {
//    		if (rPr.getRFonts().getAscii()!=null) {
//        		return rPr.getRFonts().getAscii();
//    		} else if (rPr.getRFonts().getAsciiTheme()!=null 
//    					&& themePart != null) {
//    			log.debug("Encountered rFonts/AsciiTheme: " + rPr.getRFonts().getAsciiTheme() );
//    			
//				org.docx4j.dml.Theme theme = (org.docx4j.dml.Theme)themePart.getJaxbElement();
//				org.docx4j.dml.BaseStyles.FontScheme fontScheme = themePart.getFontScheme();
//				if (rPr.getRFonts().getAsciiTheme().equals(org.docx4j.wml.STTheme.MINOR_H_ANSI)) {
//					if (fontScheme != null && fontScheme.getMinorFont().getLatin() != null) {
//						fontScheme = theme.getThemeElements().getFontScheme();
//						org.docx4j.dml.TextFont textFont = fontScheme.getMinorFont().getLatin();
//						log.info("minorFont/latin font is " + textFont.getTypeface());
//						return (textFont.getTypeface());
//					} else {
//						// No minorFont/latin in theme part - default to Calibri
//						log.info("No minorFont/latin in theme part - default to Calibri");
//						return ("Calibri");
//					}
//				} else if (rPr.getRFonts().getAsciiTheme().equals(org.docx4j.wml.STTheme.MAJOR_H_ANSI)) {
//					if (fontScheme != null && fontScheme.getMajorFont().getLatin() != null) {
//						fontScheme = theme.getThemeElements().getFontScheme();
//						org.docx4j.dml.TextFont textFont = fontScheme.getMajorFont().getLatin();
//						log.debug("majorFont/latin font is " + textFont.getTypeface());
//						return (textFont.getTypeface());
//					} else {
//						// No minorFont/latin in theme part - default to Cambria
//						log.info("No majorFont/latin in theme part - default to Cambria");
//						return ("Cambria");
//					}
//				} else {
//					log.error("Don't know how to handle: "
//							+ rPr.getRFonts().getAsciiTheme());
//				}
//    		}
//    	}
//        		
//        // 2 if not, what does this styles basedOn style say? (recursive)
//    	
//    	if (style.getBasedOn()!=null && style.getBasedOn().getVal()!=null) {
//        	String basedOnStyleName = style.getBasedOn().getVal();    		
//    		//log.debug("recursing into basedOn:" + basedOnStyleName);
//            org.docx4j.wml.Style candidateStyle = (org.docx4j.wml.Style)stylesDefined.get(basedOnStyleName);
//            if (candidateStyle != null && candidateStyle.getStyleId().equals(basedOnStyleName)) {
//            	return getFontnameFromStyle(stylesDefined, themePart, candidateStyle);
//            }
//    	     // If we get here the style is missing!
//     		log.error("couldn't find basedOn:" + basedOnStyleName);    	     
//    	     return null;
//    	} else {
//    		//log.debug("No basedOn set for: " + style.getStyleId() );
//    		return null;
//    	}
//    	
//    }
//
	
}
