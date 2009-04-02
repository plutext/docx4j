package org.docx4j.model;


import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.Color;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tabs;
import org.docx4j.wml.U;
import org.docx4j.wml.PPrBase.Ind;
import org.docx4j.wml.PPrBase.NumPr;
import org.docx4j.wml.PPrBase.OutlineLvl;
import org.docx4j.wml.PPrBase.PBdr;
import org.docx4j.wml.PPrBase.Spacing;
import org.docx4j.wml.PPrBase.TextAlignment;
import org.docx4j.wml.Styles.DocDefaults;

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

 * @author jharrop
 *
 */
public class PropertyResolver {
	
	private static Logger log = Logger.getLogger(PropertyResolver.class);
	
	private DocDefaults docDefaults;	
	private PPr documentDefaultPPr;
	private RPr documentDefaultRPr;
	
	private StyleDefinitionsPart styleDefinitionsPart;
	private org.docx4j.wml.Styles styles;
	private ThemePart themePart;
	private NumberingDefinitionsPart numberingDefinitionsPart;

	// Note, you need to manually keep this up to date
	private java.util.Map<String, org.docx4j.wml.Style>  liveStyles = null;

	private java.util.Map<String, PPr>  resolvedStylePPrComponent = new HashMap<String, PPr>();
//	public java.util.Map<String, PPr> getResolvedStylePPrComponent() {
//		return resolvedStylePPrComponent;
//	}

	private java.util.Map<String, RPr>  resolvedStyleRPrComponent = new HashMap<String, RPr>();
//	public java.util.Map<String, RPr> getResolvedStyleRPrComponent() {
//		return resolvedStyleRPrComponent;
//	}
	
	public PropertyResolver(WordprocessingMLPackage wordMLPackage) throws Docx4JException {
		
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		styleDefinitionsPart = mdp.getStyleDefinitionsPart();
		themePart = mdp.getThemePart();
		numberingDefinitionsPart = mdp.getNumberingDefinitionsPart();
		init();
	}
	
	public PropertyResolver(StyleDefinitionsPart styleDefinitionsPart,
							ThemePart themePart,
							NumberingDefinitionsPart numberingDefinitionsPart) throws Docx4JException {
		
		this.styleDefinitionsPart= styleDefinitionsPart;
		this.themePart = themePart;
		this.numberingDefinitionsPart = numberingDefinitionsPart;
		init();
	}
	
	private void init() throws Docx4JException {

		// Make sure we have a styles definitions part
		try {
			if (styleDefinitionsPart==null) {
				styleDefinitionsPart = new StyleDefinitionsPart();
				styleDefinitionsPart.unmarshalDefaultStyles();
				
				// For this case, should we provide a way
				// to add this new part to the package?
				
			}
		} catch (Exception e) {
			throw new Docx4JException("Couldn't create default StyleDefinitionsPart", e);
		}

		// Initialise styles
		styles = (org.docx4j.wml.Styles)styleDefinitionsPart.getJaxbElement();	
		initialiseLiveStyles();		
		
		// Initialise docDefaults		
		docDefaults = styles.getDocDefaults(); 		
		
		if (docDefaults==null) {
			// The only way this can happen is if the 
			// styles definition part is missing the docDefaults element
			// (these are present in docs created from Word, and
			//  in our default styles, so maybe the user created it using 
			//  some 3rd party program?)
			docDefaults = (DocDefaults)XmlUtils.unmarshalString(docDefaultsString);
		} 
		
		// Setup documentDefaultPPr
		if (docDefaults.getPPrDefault()==null) {
			documentDefaultPPr = (PPr)XmlUtils.unmarshalString(pPrDefaultsString);
		} else {
			documentDefaultPPr = docDefaults.getPPrDefault().getPPr();
		}

		// Setup documentDefaultRPr
		if (docDefaults.getRPrDefault()==null) {
			documentDefaultRPr = (RPr)XmlUtils.unmarshalString(rPrDefaultsString);
		} else {
			documentDefaultRPr = docDefaults.getRPrDefault().getRPr();
		}
		
		addNormalToResolvedStylePPrComponent();
		addDefaultParagraphFontToResolvedStyleRPrComponent();
	}


	public void addNormalToResolvedStylePPrComponent() {
		
		Stack<PPr> pPrStack = new Stack<PPr>();
		String styleId = "Normal";

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

	public void addDefaultParagraphFontToResolvedStyleRPrComponent() {
		
		Stack<RPr> rPrStack = new Stack<RPr>();
		String styleId = "DefaultParagraphFont";

		fillRPrStack(styleId, rPrStack);
		rPrStack.push(documentDefaultRPr);
			
		RPr effectiveRPr = factory.createRPr();			
		// Now, apply the properties starting at the top of the stack
		while (!rPrStack.empty() ) {
			RPr rPr = rPrStack.pop();
			applyRPr(rPr, effectiveRPr);
		}
		resolvedStyleRPrComponent.put(styleId, effectiveRPr);		
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
			styleId = "Normal";
		} else {
			styleId = expressPPr.getPStyle().getVal();
		}
		resolvedPPr = getEffectivePPr(styleId);
		
				
		//	Next, run properties are applied to each run with a specific character style 
		//	applied. 
		
			// Not for pPr
				
		//	Finally, we apply direct formatting (paragraph or run properties not from 
		//	styles).		
		if (hasDirectPPrFormatting(expressPPr) ) {			
			effectivePPr = (PPr)XmlUtils.deepCopy(resolvedPPr);			
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
		
		PPr expressPPr = s.getPPr();
		if (expressPPr==null) {
			log.error("style: " + styleId + " has no PPr");
			resolvedPPr = resolvedStylePPrComponent.get("Normal");
			return resolvedPPr;
		}
		
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
	
	public RPr getEffectiveRPr(RPr expressRPr) {
		
		RPr effectiveRPr = null;
		//	First, the document defaults are applied
		
			// Done elsewhere
			// RPr effectiveRPr = (RPr)XmlUtils.deepCopy(documentDefaultRPr);
		
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
		
			// Not for pPr
				
		//	Next, run properties are applied to each run with a specific character style 
		//	applied. 		
		RPr resolvedRPr = null;
		String styleId;
		if (expressRPr == null || expressRPr.getRStyle() == null ) {
			styleId = "DefaultParagraphFont";
		} else {
			styleId = expressRPr.getRStyle().getVal();
		}
		resolvedRPr = getEffectiveRPr(styleId);
		
		//	Finally, we apply direct formatting (run properties not from 
		//	styles).		
		if (hasDirectRPrFormatting(expressRPr) ) {			
			effectiveRPr = (RPr)XmlUtils.deepCopy(resolvedRPr);			
			applyRPr(expressRPr, effectiveRPr);
			return effectiveRPr;
		} else {
			return resolvedRPr;
		}
		
	}
	
	public RPr getEffectiveRPr(String styleId) {
		
		RPr resolvedRPr = resolvedStyleRPrComponent.get(styleId);
		
		if (resolvedRPr!=null) {
			return resolvedRPr;
		}
		
		// Hmm, have to do the work
		Style s = liveStyles.get(styleId);
		
		if (s==null) {
			log.error("Couldn't find style: " + styleId);
			return null;
		}
		
		RPr expressRPr = s.getRPr();
		if (expressRPr==null) {
			log.error("style: " + styleId + " has no RPr");
			resolvedRPr = resolvedStyleRPrComponent.get("DefaultParagraphFont");
			return resolvedRPr;
		}

		
		//	Next, run properties are applied to each run with a specific character style 
		//	applied. 		
		Stack<RPr> rPrStack = new Stack<RPr>();
		// Haven't done this one yet				
		fillRPrStack(styleId, rPrStack);
		// Finally, on top
		rPrStack.push(documentDefaultRPr);
						
		resolvedRPr = factory.createRPr();			
		// Now, apply the properties starting at the top of the stack
		while (!rPrStack.empty() ) {
			RPr rPr = rPrStack.pop();
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
		
		if (pPrToApply==null) {
			return false;
		}
		
		// Here is where we do the real work.  
		// There are a lot of paragraph properties
		// The below list is taken directly from PPrBase.
		
		//PPrBase.PStyle pStyle;
		
			// Ignore
		
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
	
	
	private void applyPPr(PPr pPrToApply, PPr effectivePPr) {
		
		if (pPrToApply==null) {
			return;
		}
		
		// Here is where we do the real work.  
		// There are a lot of paragraph properties
		// The below list is taken directly from PPrBase.
		
		//PPrBase.PStyle pStyle;
		
			// Ignore
		
		//BooleanDefaultTrue keepNext;
		if (pPrToApply.getKeepNext()!=null) {
			effectivePPr.setKeepNext(
					newBooleanDefaultTrue(
							pPrToApply.getKeepNext().isVal()));			
		}
		
	
		//BooleanDefaultTrue keepLines;
		if (pPrToApply.getKeepLines()!=null) {
			effectivePPr.setKeepLines(
					newBooleanDefaultTrue(
							pPrToApply.getKeepLines().isVal()));			
		}
	
		//BooleanDefaultTrue pageBreakBefore;
		if (pPrToApply.getPageBreakBefore()!=null) {
			effectivePPr.setPageBreakBefore(
					newBooleanDefaultTrue(
							pPrToApply.getPageBreakBefore().isVal()));			
		}
	
		//CTFramePr framePr;
		//BooleanDefaultTrue widowControl;
		if (pPrToApply.getWidowControl()!=null) {
			effectivePPr.setWidowControl(
					newBooleanDefaultTrue(
							pPrToApply.getWidowControl().isVal()));			
		}
	
		//NumPr numPr;
		if (pPrToApply.getNumPr()!=null) {
			NumPr numPrClone = (NumPr)XmlUtils.deepCopy(pPrToApply.getNumPr());
			effectivePPr.setNumPr(numPrClone);			
		}
	
		//BooleanDefaultTrue suppressLineNumbers;
		if (pPrToApply.getSuppressLineNumbers()!=null) {
			effectivePPr.setSuppressLineNumbers(
					newBooleanDefaultTrue(
							pPrToApply.getSuppressLineNumbers().isVal()));			
		}
		
		// PBdr pBdr;
		if (pPrToApply.getPBdr()!=null) {
			PBdr pBdrClone = (PBdr)XmlUtils.deepCopy(pPrToApply.getPBdr());
			effectivePPr.setPBdr(pBdrClone);						
		}
	
		//CTShd shd;
		if (pPrToApply.getShd()!=null) {
			CTShd shdClone = (CTShd)XmlUtils.deepCopy(pPrToApply.getShd());
			effectivePPr.setShd(shdClone);
		}
			
		//Tabs tabs;
		if (pPrToApply.getTabs()!=null) {
			Tabs tabs = (Tabs)XmlUtils.deepCopy(pPrToApply.getTabs());
			effectivePPr.setTabs(tabs);
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
			Spacing spacing = (Spacing)XmlUtils.deepCopy(pPrToApply.getSpacing());
			effectivePPr.setSpacing(spacing);
		}
	
		//PPrBase.Ind ind;
		if (pPrToApply.getInd()!=null) {
			Ind ind = (Ind)XmlUtils.deepCopy(pPrToApply.getInd());
			effectivePPr.setInd(ind);
		}
	
		//BooleanDefaultTrue contextualSpacing;
		//BooleanDefaultTrue mirrorIndents;
		//BooleanDefaultTrue suppressOverlap;
		//Jc jc;
		if (pPrToApply.getJc()!=null) {
			Jc jc = (Jc)XmlUtils.deepCopy(pPrToApply.getJc() );
			effectivePPr.setJc(jc);
		}
	
		//TextDirection textDirection;
		//PPrBase.TextAlignment textAlignment;
		if (pPrToApply.getTextAlignment()!=null ) {
			TextAlignment ta = (TextAlignment)XmlUtils.deepCopy(pPrToApply.getTextAlignment() );
			effectivePPr.setTextAlignment(ta);
		}
	
		//CTTextboxTightWrap textboxTightWrap;
		//PPrBase.OutlineLvl outlineLvl;
		if (pPrToApply.getOutlineLvl()!=null ) {
			OutlineLvl ol = (OutlineLvl)XmlUtils.deepCopy(pPrToApply.getOutlineLvl() );
			effectivePPr.setOutlineLvl(ol);
		}
	
		//PPrBase.DivId divId;
		//CTCnf cnfStyle;
		
	}
	
	private boolean hasDirectRPrFormatting(RPr rPrToApply) {
		
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
	
	private void applyRPr(RPr rPrToApply, RPr effectiveRPr) {
		
		if (rPrToApply==null) {
			return;
		}
		
		// Here is where we do the real work.  
		// There are a lot of run properties
		// The below list is taken directly from RPr, and so
		// is comprehensive.
		
		//RStyle rStyle;
		//RFonts rFonts;
		if (rPrToApply.getRFonts()!=null ) {
			RFonts rf = (RFonts)XmlUtils.deepCopy( rPrToApply.getRFonts() );
			effectiveRPr.setRFonts(rf);
		}

		//BooleanDefaultTrue b;
		if (rPrToApply.getB()!=null) {
			effectiveRPr.setB(
					newBooleanDefaultTrue(
							rPrToApply.getB().isVal()));			
		}

		//BooleanDefaultTrue bCs;
		//BooleanDefaultTrue i;
		if (rPrToApply.getI()!=null) {
			effectiveRPr.setI(
					newBooleanDefaultTrue(
							rPrToApply.getI().isVal()));			
		}

		//BooleanDefaultTrue iCs;
		//BooleanDefaultTrue caps;
		if (rPrToApply.getCaps()!=null) {
			effectiveRPr.setCaps(
					newBooleanDefaultTrue(
							rPrToApply.getCaps().isVal()));			
		}

		//BooleanDefaultTrue smallCaps;
		if (rPrToApply.getSmallCaps()!=null) {
			effectiveRPr.setSmallCaps(
					newBooleanDefaultTrue(
							rPrToApply.getSmallCaps().isVal()));			
		}

		//BooleanDefaultTrue strike;
		if (rPrToApply.getStrike()!=null) {
			effectiveRPr.setStrike(
					newBooleanDefaultTrue(
							rPrToApply.getStrike().isVal()));			
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
			Color c = (Color)XmlUtils.deepCopy( rPrToApply.getColor() );
			effectiveRPr.setColor(c);
		}

		//CTSignedTwipsMeasure spacing;
		//CTTextScale w;
		//HpsMeasure kern;
		//CTSignedHpsMeasure position;
		//HpsMeasure sz;
		if (rPrToApply.getSz()!=null ) {
			// We don't have @XmlRootElement on HpsMeasure, so can't deepCopy
			HpsMeasure sz = factory.createHpsMeasure(); 
			long szLong = rPrToApply.getSz().getVal().longValue();
			sz.setVal( BigInteger.valueOf(szLong)  );
			effectiveRPr.setSz(sz);
		}

		//HpsMeasure szCs;
		//Highlight highlight;
		//U u;
		if (rPrToApply.getU()!=null ) {
			U rf = (U)XmlUtils.deepCopy( rPrToApply.getU() );
			effectiveRPr.setU(rf);
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
		
	}	
	
	/**
	 * Ascend the style hierarchy, capturing the pPr bit
	 *  
	 * @param stylename
	 * @param effectivePPr
	 */
	private void fillPPrStack(String styleId, Stack<PPr> pPrStack) {
		
		// get the style
		Style style = liveStyles.get(styleId);
		
		// add it to the stack
		if (style==null) {
			// No such style!
			// For now, just log it..
			log.error("Style definition not found: " + styleId);
			return;
		}
		pPrStack.push(style.getPPr());
		log.debug("Added " + styleId + " to pPr stack");
		
		// if it is based on, recurse
    	if (style.getBasedOn()==null) {
			log.error("Style " + styleId + " is a root style.");
    	} else if (style.getBasedOn().getVal()!=null) {
        	String basedOnStyleName = style.getBasedOn().getVal();           	
        	fillPPrStack( basedOnStyleName, pPrStack);
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
		log.debug("Added " + styleId + " to pPr stack");
		
		// if it is based on, recurse
    	if (style.getBasedOn()==null) {
			log.error("Style " + styleId + " is a root style.");
    	} else if (style.getBasedOn().getVal()!=null) {
        	String basedOnStyleName = style.getBasedOn().getVal();           	
        	fillRPrStack( basedOnStyleName, rPrStack);
    	} else {
    		log.debug("No basedOn set for: " + style.getStyleId() );
    	}
		
	}
	
	
    private void initialiseLiveStyles() {
    	
		liveStyles = new java.util.HashMap<String, org.docx4j.wml.Style>();
		
		for ( org.docx4j.wml.Style s : styles.getStyle() ) {				
			liveStyles.put(s.getStyleId(), s);	
			log.debug("live style: " + s.getStyleId() );
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
		
		org.docx4j.wml.RFonts rFonts = documentDefaultRPr.getRFonts();
		if (rFonts==null) {
			log.info("No styles/docDefaults/rPrDefault/rPr/rFonts - default to Times New Roman");
			// Yes, Times New Roman is still buried in Word 2007
			return "Times New Roman"; 						
		} else {						
			// Usual case
			if (rFonts.getAsciiTheme()!=null ) {
				// for example minorHAnsi, which I think translates to minorFont/latin 
				if (rFonts.getAsciiTheme().equals(org.docx4j.wml.STTheme.MINOR_H_ANSI)) {
					if (themePart!=null) {
						org.docx4j.dml.BaseStyles.FontScheme fontScheme = themePart.getFontScheme();
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
		} 
	}

	final static String rPrDefaultsString = "<w:rPr>"
		// Word 2007 still uses Times New Roman if there is no theme part, and we'd like to replicate that 
        // + "<w:rFonts w:asciiTheme=\"minorHAnsi\" w:eastAsiaTheme=\"minorHAnsi\" w:hAnsiTheme=\"minorHAnsi\" w:cstheme=\"minorBidi\" />"
        + "<w:sz w:val=\"22\" />"
        + "<w:szCs w:val=\"22\" />"
        + "<w:lang w:val=\"en-US\" w:eastAsia=\"en-US\" w:bidi=\"ar-SA\" />"
      + "</w:rPr>";
	final static String pPrDefaultsString = "<w:pPr>"
	        + "<w:ind w:left=\"86\" w:right=\"86\" />"
	      + "</w:pPr>";
	final static String docDefaultsString = "<w:docDefaults>"
	    + "<w:rPrDefault>"
	    + 	rPrDefaultsString
	    + "</w:rPrDefault>"
	    + "<w:pPrDefault>"
	    + 	pPrDefaultsString
	    + "</w:pPrDefault>"
	  + "</w:docDefaults>";
	
    public boolean activateStyle( String styleId  ) {
    	
    	if (liveStyles.get(styleId)!=null) {
    		// Its already live - nothing to do
    		return true;    		
    	}
    	
    	java.util.Map<String, org.docx4j.wml.Style> knownStyles 
    		= styleDefinitionsPart.getKnownStyles();
    	
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
    		
    	} else if ( s.getStyleId().equals("Normal")
    			|| s.getStyleId().equals("DefaultParagraphFont") )
    	{
    		// stop condition
    		result1 = true;
    	} else {
    		
    		log.error("Expected " + s.getStyleId() + " to have <w:basedOn ??");
    		// Not properly activated
    		result1 = false;
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
	
// TODO - what follows is old code.  It is however necessary to be able to
// get all fonts.  
// 1.  can the above code be modified to just get a single property (ie fonts)?
// 2.  we only really want to climb the hierarchy once per style, so 
//     introduce an effectivePr map?
    
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
    public String getFontnameFromStyle(org.docx4j.wml.Style style) {
    	
    	return getFontnameFromStyle(styleDefinitionsPart, themePart, style); 
    	
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
    public static String getFontnameFromStyle(StyleDefinitionsPart styleDefinitionsPart, ThemePart themePart,  org.docx4j.wml.Style style) {

		org.docx4j.wml.Styles styles = (org.docx4j.wml.Styles)styleDefinitionsPart.getJaxbElement();
//		org.docx4j.wml.Styles styles = (org.docx4j.wml.Styles)this.getStyleDefinitionsPart().getJaxbElement();

		// It is convenient to have a HashMap of styles
		Map stylesDefined = new java.util.HashMap();
	     for (Iterator iter = styles.getStyle().iterator(); iter.hasNext();) {
	            org.docx4j.wml.Style s = (org.docx4j.wml.Style)iter.next();
	            stylesDefined.put(s.getStyleId(), s);
	     }
	     
	    return getFontnameFromStyle(stylesDefined, themePart, style);
    }
    /**
     * 
     * @return the font name, or null if there is no rFonts element in any style
     * in the style inheritance hierarchy (ie
     * this method does not look up styles/docDefaults/rPrDefault/rPr/rFonts
     * or from there, the theme part).
     */
    public static String getFontnameFromStyle(Map stylesDefined, ThemePart themePart, org.docx4j.wml.Style style) {
    	
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
    	

        // 1 does it have its own rPr which contains rFonts?
    	org.docx4j.wml.RPr rPr = style.getRPr();
    	if (rPr!=null && rPr.getRFonts()!=null) {
    		if (rPr.getRFonts().getAscii()!=null) {
        		return rPr.getRFonts().getAscii();
    		} else if (rPr.getRFonts().getAsciiTheme()!=null 
    					&& themePart != null) {
    			log.debug("Encountered rFonts/AsciiTheme: " + rPr.getRFonts().getAsciiTheme() );
    			
				org.docx4j.dml.Theme theme = (org.docx4j.dml.Theme)themePart.getJaxbElement();
				org.docx4j.dml.BaseStyles.FontScheme fontScheme = themePart.getFontScheme();
				if (rPr.getRFonts().getAsciiTheme().equals(org.docx4j.wml.STTheme.MINOR_H_ANSI)) {
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
				} else if (rPr.getRFonts().getAsciiTheme().equals(org.docx4j.wml.STTheme.MAJOR_H_ANSI)) {
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
    		//log.debug("recursing into basedOn:" + basedOnStyleName);
            org.docx4j.wml.Style candidateStyle = (org.docx4j.wml.Style)stylesDefined.get(basedOnStyleName);
            if (candidateStyle != null && candidateStyle.getStyleId().equals(basedOnStyleName)) {
            	return getFontnameFromStyle(stylesDefined, themePart, candidateStyle);
            }
    	     // If we get here the style is missing!
     		log.error("couldn't find basedOn:" + basedOnStyleName);    	     
    	     return null;
    	} else {
    		//log.debug("No basedOn set for: " + style.getStyleId() );
    		return null;
    	}
    	
    }

	
}
