package org.docx4j.model;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.openpackaging.exceptions.CyclicStylesException;
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
import org.docx4j.wml.RStyle;
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
	 * Map of all styles in the Style Definitions Part,
	 * keyed by styleId.
	 * Access it via getLiveStyle, which falls back to rescanning the styles part,
	 * so a style added to the part after this map was built is still found.
	 */
	private final java.util.Map<String, org.docx4j.wml.Style>  liveStyles = new java.util.concurrent.ConcurrentHashMap<String, org.docx4j.wml.Style>();

	/** How many styles the part held when liveStyles was last scanned; a miss rescans only when that has changed. */
	private volatile int scannedStyleCount = -1;
	
	
	private ThemePart themePart;
	private NumberingDefinitionsPart numberingDefinitionsPart;


	/**
	 * A style's w:basedOn chain merged root-first, WITHOUT the document defaults,
	 * keyed by styleId (CR-015 phase 2).  These are the units every effective value is
	 * composed from, so nothing cached depends on what a caller asked for first.
	 */
	private final java.util.Map<String, PPr> chainPPr = new java.util.concurrent.ConcurrentHashMap<String, PPr>();
	private final java.util.Map<String, RPr> chainRPr = new java.util.concurrent.ConcurrentHashMap<String, RPr>();

	/** Document defaults with the chain applied over them, keyed by styleId: what
	 *  getEffectivePPr(String) and getEffectiveRPr(String) return. */
	private final java.util.Map<String, PPr> effectivePPrByStyle = new java.util.concurrent.ConcurrentHashMap<String, PPr>();
	private final java.util.Map<String, RPr> effectiveRPrByStyle = new java.util.concurrent.ConcurrentHashMap<String, RPr>();

	/** The cache key for "no style" (a styles part with no default paragraph style). */
	private static final String NO_STYLE = "";

	private static String key(String styleId) {
		return styleId == null ? NO_STYLE : styleId;
	}

	/*
	 * Thread safety (CR-015 phase 3): one PropertyResolver is shared by everything that
	 * touches the package for its lifetime.  Resolution reads the styles part and writes
	 * nothing into it (the document defaults are a private copy; the heading outline level
	 * and an inherited w:numId are computed in the resolved objects, not written into the
	 * styles), so two threads computing the same entry do duplicate work and store equal
	 * values; the caches are ConcurrentHashMaps, and refresh() clears them.
	 */

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

	/** The styleId of the {@code w:default="1"} paragraph style, or null if the
	 *  styles part declares none.  @since 17.1.1 */
	public String getDefaultParagraphStyleId() {
		return defaultParagraphStyleId;
	}
	String defaultCharacterStyleId;
	
	private void init() throws Docx4JException {

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
		
		DocDefaults docDefaults = styleDefinitionsPart.getJaxbElement().getDocDefaults();
        if(log.isDebugEnabled()) {
            log.debug(XmlUtils.marshaltoString(docDefaults, true, true));
        }

		// private copies: the resolver's defaults are its own, so nothing below writes into
		// the styles part (until 17.1.1 the w:sz 20 default went into the part, and a docx
		// saved after an export carried it)
		documentDefaultPPr = new PPr();
		documentDefaultRPr = new RPr();        	

		if (docDefaults!=null
				&& docDefaults.getPPrDefault()!=null
				&& docDefaults.getPPrDefault().getPPr()!=null) {
			
			documentDefaultPPr = XmlUtils.deepCopy(docDefaults.getPPrDefault().getPPr());
        }
		
		if (docDefaults!=null
				&& docDefaults.getRPrDefault()!=null
				&& docDefaults.getRPrDefault().getRPr()!=null) {
			
			documentDefaultRPr = XmlUtils.deepCopy(docDefaults.getRPrDefault().getRPr());
        }
		
		if (documentDefaultRPr.getSz()==null) {
			// Make Word's default explicit: 10pt where nothing states a size (measured, CR-015
			// probe styles-no-size-anywhere: identical to an explicit 10pt run)
			HpsMeasure sz20 = new HpsMeasure(); 
			sz20.setVal(BigInteger.valueOf(20));
			documentDefaultRPr.setSz(sz20);
		}
	}

	/** The default paragraph style's effective pPr (document defaults included), as
	 *  getEffectivePPr(String) gives it. */
	public PPr getResolvedDefaultParagraphStyle() {
		try {
			return getEffectivePPr(defaultParagraphStyleId);
		} catch (CyclicStylesException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public Style getEffectiveTableStyle(TblPr tblPr) throws CyclicStylesException {
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

	private void fillTableStyleStack(String styleId, Stack<Style> tableStyleStack) throws CyclicStylesException {
		// wrapper that starts a fresh seen set for cycle detection
		List<String> seen = new ArrayList<String>();
		fillTableStyleStackInternal(styleId, tableStyleStack, seen);
	}
		
	/**
	 * Ascend the style hierarchy, capturing the table styles
	 *  
	 * @param stylename
	 * @param effectivePPr
	 * @throws CyclicStylesException 
	 */
	private void fillTableStyleStackInternal(String styleId, Stack<Style> tableStyleStack, List<String> seen) throws CyclicStylesException {
		
		if (StyleUtil.isCyclic(styleId, seen, log)) {
			// TODO, jump up to add default table style (if not part of the cycle) and DocumentDefaults
			return;
		}
		seen.add(styleId);

		// get the style
		Style style = getLiveStyle(styleId);

		// add it to the stack
		if (style==null) {
			// No such style!
			// For now, just log it..
			log.error("Style definition not found: " + styleId);
			seen.remove(styleId);
			return;
		}
		
		
		tableStyleStack.push(style);
		log.debug("Added " + styleId + " to table style stack");
		
		// if it is based on, recurse
    	if (style.getBasedOn()==null) {
			log.debug("Style " + styleId + " is a root style.");
    	} else if (style.getBasedOn().getVal()!=null) {
        	String basedOnStyleName = style.getBasedOn().getVal();           	
        	fillTableStyleStackInternal( basedOnStyleName, tableStyleStack, seen);
    	} else {
    		log.debug("No basedOn set for: " + style.getStyleId() );
    	}
		
		// remove from seen so that other branches may traverse
		seen.remove(styleId);    	
	}
	
	
	/*
	 * The resolution order (ECMA-376 17.7.2), as composed here since CR-015 phase 2:
	 *
	 *   effectivePPr(direct)       = docDefaults.pPr + chainPPr(styleOf(direct)) + direct
	 *   effectiveRPr(direct, pPr)  = docDefaults.rPr + chainRPr(styleOf(pPr)) + chainRPr(direct.rStyle) + direct
	 *   paragraphMarkRPr(pPr)      = docDefaults.rPr + chainRPr(styleOf(pPr)) + pPr.rPr
	 *
	 * where styleOf(pPr) is the paragraph's w:pStyle if it names a style that exists, else
	 * the w:default="1" paragraph style (Word writes no w:pStyle for it, and treats a
	 * missing style as it; measured, probe styles-default-pstyle), and chainPPr/chainRPr
	 * are a style's w:basedOn chain merged root-first without the document defaults,
	 * cached per styleId.  Table styles are not applied here: the resolver is handed a
	 * w:pPr and does not know the table (ParagraphStylesInTableFix carries them for the
	 * exporters; see CR-015 "Layering").
	 */

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
	 * @throws CyclicStylesException 
	 */
	public PPr getEffectivePPr(PPr expressPPr) throws CyclicStylesException {
		
		PPr resolvedPPr = getEffectivePPr(paragraphStyleOf(expressPPr));

		//	Finally, we apply direct formatting (paragraph properties not from styles)
		if (hasDirectPPrFormatting(expressPPr) ) {
			PPr effectivePPr = (PPr)XmlUtils.deepCopy(resolvedPPr);
			applyPPr(expressPPr, effectivePPr);
			return effectivePPr;
		} else {
			return resolvedPPr;
		}
	}

	/**
	 * Follow the resolution rules to return the
	 * paragraph properties which actually apply,
	 * given this paragraph style (document defaults included).
	 * 
	 * A styleId naming no style resolves as the default paragraph style does
	 * (since 17.1.1; used to return null).
	 * 
	 * What is returned is a live object.  If you
	 * want to change it, you should clone it first!
	 *  
	 * @param styleId
	 * @return
	 * @throws CyclicStylesException 
	 */
	public PPr getEffectivePPr(String styleId) throws CyclicStylesException {
		
		String existing = existingParagraphStyle(styleId);
		PPr resolved = effectivePPrByStyle.get(key(existing));
		if (resolved!=null) {
			return resolved;
		}
		resolved = (PPr)XmlUtils.deepCopy(documentDefaultPPr);
		applyPPr(chainPPr(existing), resolved);
		effectivePPrByStyle.put(key(existing), resolved);
		return resolved;
	}

	/**
	 * The run properties which apply to a run, given its own rPr and the pPr of the
	 * paragraph it is in: document defaults, the paragraph style's run properties, the
	 * run's character style, then its direct formatting.  The paragraph mark's rPr
	 * (pPr/rPr) is never applied to a run; for the mark itself see
	 * {@link #getEffectiveParagraphMarkRPr(PPr)}.  (Until 17.1.1 a run with no rPr in a
	 * paragraph naming no style got the mark's formatting, and a paragraph naming no
	 * style - which is how Word writes the default style - got no paragraph-style run
	 * properties at all.)
	 * 
	 * @param expressRPr the run's own w:rPr, or null
	 * @param pPr the paragraph's own w:pPr, or null
	 * @return a new object each call
	 * @throws CyclicStylesException 
	 */
	public RPr getEffectiveRPr(RPr expressRPr, PPr pPr) throws CyclicStylesException {

		RPr effectiveRPr = (RPr)XmlUtils.deepCopy(documentDefaultRPr);
		applyRPr(chainRPr(paragraphStyleOf(pPr)), effectiveRPr);
		applyCharacterStyleAndDirect(expressRPr, effectiveRPr);
		return effectiveRPr;
	}

	/**
	 * The run properties of the paragraph mark: document defaults, the paragraph style's
	 * run properties, then the pPr's own rPr.  What sizes an empty paragraph, and what a
	 * list label starts from.
	 * 
	 * @since 17.1.1
	 */
	public RPr getEffectiveParagraphMarkRPr(PPr pPr) throws CyclicStylesException {

		RPr effectiveRPr = (RPr)XmlUtils.deepCopy(documentDefaultRPr);
		applyRPr(chainRPr(paragraphStyleOf(pPr)), effectiveRPr);
		if (pPr!=null && pPr.getRPr()!=null) {
			applyRPr(pPr.getRPr(), effectiveRPr);
		}
		return effectiveRPr;
	}

	/**
	 * Return effective rPr, as follows: Starting with the rPr from the pStyle, 
	 * apply character style (if any) specified on the run,
	 * then any other direct (ad-hoc) run formatting.
	 * 
	 * @param expressRPr
	 * @param rPrFromPStyle should be rPr from the paragraph style (as opposed to rPr in the direct pPr, which is only relevant to the paragraph mark)
	 * @return
	 * @throws CyclicStylesException 
	 * @deprecated since 17.1.1: {@link #getEffectiveRPr(RPr, PPr)} composes the paragraph style itself
	 */
	@Deprecated
	public RPr getEffectiveRPrUsingPStyleRPr(RPr expressRPr, RPr rPrFromPStyle) throws CyclicStylesException {
		
		RPr effectiveRPr = (RPr)XmlUtils.deepCopy(documentDefaultRPr);
		if (rPrFromPStyle!=null) {
			applyRPr(rPrFromPStyle, effectiveRPr);
		}		
		applyCharacterStyleAndDirect(expressRPr, effectiveRPr);
		return effectiveRPr;
	}

	/** The character style's chain (the style it names, if it exists), then the direct formatting. */
	private void applyCharacterStyleAndDirect(RPr expressRPr, RPr effectiveRPr) throws CyclicStylesException {

		if (expressRPr != null && expressRPr.getRStyle() != null && expressRPr.getRStyle().getVal() != null) {
			String runStyleId = expressRPr.getRStyle().getVal();
			if (getLiveStyle(runStyleId) == null) {
				log.error("Couldn't find style: " + runStyleId);
			} else {
				applyRPr(chainRPr(runStyleId), effectiveRPr);
			}
		}
		if (hasDirectRPrFormatting(expressRPr) ) {			
			applyRPr(expressRPr, effectiveRPr);
		} 
	}

	/**
	 * The run properties a style resolves to on its own: document defaults, then its
	 * w:basedOn chain.  For a paragraph style, the run properties of its paragraphs
	 * (the fo:block's); for a character style, what it contributes with nothing under it.
	 * 
	 * What is returned is a live object.  If you want to change it, clone it first.
	 * 
	 * @param styleId
	 * @return
	 * @throws CyclicStylesException 
	 */
	public RPr getEffectiveRPr(String styleId) throws CyclicStylesException {

		RPr resolved = effectiveRPrByStyle.get(key(styleId));
		if (resolved!=null) {
			return resolved;
		}
		Style s = getLiveStyle(styleId);
		if (s==null) {
			log.error("Couldn't find style: " + styleId);
			log.debug("Couldn't find style: " + styleId, new Throwable());
			return null;
		}
		resolved = (RPr)XmlUtils.deepCopy(documentDefaultRPr);
		applyRPr(chainRPr(styleId), resolved);
		effectiveRPrByStyle.put(key(styleId), resolved);
		return resolved;
	}
	
	/**
	 * The run properties which apply to a run outside any paragraph: document
	 * defaults, the default paragraph style's run properties, the character style the
	 * rPr names, then its direct formatting.  A new object each call.
	 * 
	 * @param directRPr
	 * @return
	 * @throws CyclicStylesException 
	 * @since 11.5.12
	 */
	public RPr getEffectiveRPr(RPr directRPr) throws CyclicStylesException {
		
		return getEffectiveRPr(directRPr, (PPr)null);
	}
	
	/**
	 * apply the rPr in the stack of styles, optionally including documentDefaultRPr
	 * (its rFonts, sz/szCs and lang).  Computed on each call.
	 * 
	 * @param styleId
	 * @return
	 * @throws CyclicStylesException 
	 * @since 8.2.4
	 * @deprecated since 17.1.1: the flags existed to keep the document defaults from
	 * overriding the paragraph style's when a character style was applied over it, which
	 * {@link #getEffectiveRPr(RPr, PPr)} now composes correctly; and the result used to be
	 * cached under the styleId alone, so it depended on which caller came first.
	 */
	@Deprecated
	public RPr getEffectiveRPr(String styleId, 
			boolean applyDocDefaultsRFonts, boolean applyDocDefaultsSz,
			boolean applyDocDefaultsLang) throws CyclicStylesException {

		Style s = getLiveStyle(styleId);
		if (s==null) {
			log.error("Couldn't find style: " + styleId);
			log.debug("Couldn't find style: " + styleId, new Throwable());
			return null;
		}
		RPr resolvedRPr = factory.createRPr();
		if (applyDocDefaultsRFonts) {
			resolvedRPr.setRFonts(this.documentDefaultRPr.getRFonts());
		}
		if (applyDocDefaultsSz) {
			resolvedRPr.setSz(this.documentDefaultRPr.getSz());
			resolvedRPr.setSzCs(this.documentDefaultRPr.getSzCs());
		}
		if (applyDocDefaultsLang) {
			resolvedRPr.setLang(this.documentDefaultRPr.getLang());
		}
		applyRPr(chainRPr(styleId), resolvedRPr);
		return resolvedRPr;
	}

	// ---------------------------------------------------------------- the chains

	/** The paragraph's style: its w:pStyle where that names a style that exists, else the default paragraph style. */
	private String paragraphStyleOf(PPr pPr) {
		if (pPr == null || pPr.getPStyle() == null) {
			return defaultParagraphStyleId;
		}
		String styleId = pPr.getPStyle().getVal();
		if (styleId == null) {
			if (log.isWarnEnabled()) {
				log.warn("Missing style id: " + XmlUtils.marshaltoString(pPr));
			}
			return defaultParagraphStyleId;
		}
		return existingParagraphStyle(styleId);
	}

	/** styleId if it names a style that exists, else the default paragraph style's id (logged). */
	private String existingParagraphStyle(String styleId) {
		if (styleId == null) return defaultParagraphStyleId;
		if (getLiveStyle(styleId) == null) {
			log.error("Couldn't find style: " + styleId + "; using the default paragraph style");
			return defaultParagraphStyleId;
		}
		return styleId;
	}

	/** A style's w:basedOn chain merged root-first, without the document defaults; cached.  An empty pPr for null or a missing style. */
	private PPr chainPPr(String styleId) throws CyclicStylesException {
		if (styleId == null) return factory.createPPr();
		PPr chain = chainPPr.get(styleId);
		if (chain != null) return chain;
		Stack<PPr> pPrStack = new Stack<PPr>();
		fillPPrStack(styleId, pPrStack);
		chain = factory.createPPr();
		while (!pPrStack.empty() ) {
			applyPPr(pPrStack.pop(), chain);
		}
		chainPPr.put(styleId, chain);
		return chain;
	}

	/** A style's w:basedOn chain merged root-first, without the document defaults; cached.  An empty rPr for null or a missing style. */
	private RPr chainRPr(String styleId) throws CyclicStylesException {
		if (styleId == null) return factory.createRPr();
		RPr chain = chainRPr.get(styleId);
		if (chain != null) return chain;
		Stack<RPr> rPrStack = new Stack<RPr>();
		fillRPrStack(styleId, rPrStack);
		chain = factory.createRPr();
		while (!rPrStack.empty() ) {
			applyRPr(rPrStack.pop(), chain);
		}
		chainRPr.put(styleId, chain);
		return chain;
	}
	
	org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
	
	/**
	 * Whether the paragraph states any formatting of its own: {@link StyleUtil#hasDirectFormatting(PPrBase)}
	 * over every member of {@link org.docx4j.model.styles.PropertyCatalogue#PARAGRAPH}.
	 * Any rPr is intentionally ignored, since pPr/rPr is not applicable to anything
	 * except the paragraph mark.  (Until 17.1.1 this was a hand-kept list of 17 of the
	 * 34 members, to which each fidelity batch added one more: w:framePr, w:contextualSpacing,
	 * w:suppressAutoHyphens; a paragraph whose only direct formatting was w:mirrorIndents
	 * or w:textDirection resolved as having none.)
	 */
	private boolean hasDirectPPrFormatting(PPr pPrToApply) {
		return StyleUtil.hasDirectFormatting(pPrToApply);
	}
	
	

	protected void applyPPr(PPr pPrToApply, PPr effectivePPr) {
        if(log.isDebugEnabled()) {
            log.debug("apply " + XmlUtils.marshaltoString(pPrToApply, true, true)
                    + "\n\r to " + XmlUtils.marshaltoString(effectivePPr, true, true));
        }
		
		StyleUtil.apply(pPrToApply, effectivePPr, this.numberingDefinitionsPart);
		
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
	
	/**
	 * Whether the run states any formatting of its own: {@link StyleUtil#hasDirectFormatting(RPr)}
	 * over every member of {@link org.docx4j.model.styles.PropertyCatalogue#RUN} but the
	 * style reference.  (Until 17.1.1 this was a hand-kept list of 19 of the 40 members -
	 * "taken directly from RPr, and so is comprehensive", it said - so a run whose only
	 * direct formatting was w:rtl, w:position, w:szCs, w:w, w:kern or w:cs resolved as
	 * having none.)
	 */
	public boolean hasDirectRPrFormatting(RPr rPrToApply) {
		return StyleUtil.hasDirectFormatting(rPrToApply);
	}
	
	private boolean hasDirectRPrFormatting(ParaRPr rPrToApply) {
		return StyleUtil.hasDirectFormatting(rPrToApply);
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

    private static final java.util.regex.Pattern HEADING_NAME = java.util.regex.Pattern.compile("heading ([1-9])", java.util.regex.Pattern.CASE_INSENSITIVE);

    /**
     * The outline level (1-9) of a built-in heading style, from its w:name ("heading 1"
     * ... "heading 9"), or -1.  Word identifies built-in styles by name, so this holds in
     * every locale; {@link #getLvlFromHeadingStyle(String)} keys on the English styleId.
     * @since 17.1.1
     */
    public static int headingLevelByName(Style style) {
    	if (style == null || style.getName() == null || style.getName().getVal() == null) return -1;
    	java.util.regex.Matcher m = HEADING_NAME.matcher(style.getName().getVal().trim());
    	return m.matches() ? Integer.parseInt(m.group(1)) : -1;
    }
	
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
	 * @throws CyclicStylesException 
	 */
	private void fillPPrStack(String styleId, Stack<PPr> pPrStack) throws CyclicStylesException {
		// wrapper that starts a fresh seen set for cycle detection
		List<String> seen = new ArrayList<String>();
		fillPPrStackInternal(styleId, pPrStack, seen);
	}

	private void fillPPrStackInternal(String styleId, Stack<PPr> pPrStack, List<String> seen) throws CyclicStylesException {
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

		// Detect cycles
		if (StyleUtil.isCyclic(styleId, seen, log)) {
			// TODO, jump up to add default pStyle (if not part of the cycle) and DocumentDefaults
			return;
		}
		seen.add(styleId);

		// get the style
		Style style = getLiveStyle(styleId);

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
			seen.remove(styleId);
			return;
		}

		/* The built-in heading styles have a fixed outline level, which Word takes from the
		 * style's built-in NAME ("heading 1" ... "heading 9", the same in every locale; the
		 * styleId is localised: "berschrift1", "Titre1"), whatever w:outlineLvl the style
		 * declares.  Until 17.1.1 this keyed on the id prefix "Heading" and wrote the level
		 * into the style; it is now computed by name, on a copy, so the styles part is not
		 * touched (CR-015 phase 3). */
		PPr layer = style.getPPr();
		int headingLevel = headingLevelByName(style);
		if (headingLevel > 0 && layer != null
				&& layer.getOutlineLvl() != null && layer.getOutlineLvl().getVal() != null
				&& layer.getOutlineLvl().getVal().intValue() != headingLevel - 1) {
			log.debug(styleId + " - outline level " + (headingLevel - 1) + " from its name, not the declared " + layer.getOutlineLvl().getVal());
			layer = XmlUtils.deepCopy(layer);
			layer.getOutlineLvl().setVal(BigInteger.valueOf(headingLevel - 1));
		}
		pPrStack.push(layer);
		log.debug("Added " + styleId + " to pPr stack");

		// (A style whose w:numPr names no w:numId inherits it from the style it is based
		// on: since 17.1.1 StyleUtil.apply(NumPr) merges the two elements separately, so
		// the inherited id reaches the effective pPr without being written into the style,
		// which is what happened here until then.)

		// if it is based on, recurse
		if (style.getBasedOn()==null) {
			log.debug("Style " + styleId + " is a root style.");
		} else if (style.getBasedOn().getVal()!=null) {
			String basedOnStyleName = style.getBasedOn().getVal();
			log.debug("Style " + styleId + " is based on " + basedOnStyleName);
        	fillPPrStackInternal(basedOnStyleName, pPrStack, seen);
		} else {
			log.debug("No basedOn set for: " + style.getStyleId() );
		}

		// remove from seen so that other branches may traverse
		seen.remove(styleId);
	}
	

	private void fillRPrStack(String styleId, Stack<RPr> rPrStack) throws CyclicStylesException {
		// wrapper that starts a fresh seen set for cycle detection
		List<String> seen = new ArrayList<String>();
		fillRPrStackInternal(styleId, rPrStack, seen);
	}
	
	/**
	 * Ascend the style hierarchy, capturing the rPr bit
	 *  
	 * @param stylename
	 * @param effectivePPr
	 * @throws CyclicStylesException 
	 */
	private void fillRPrStackInternal(String styleId, Stack<RPr> rPrStack, List<String> seen) throws CyclicStylesException {
		
		if (StyleUtil.isCyclic(styleId, seen, log)) {
			// TODO, jump up to add default rStyle (if not part of the cycle) and DocumentDefaults			
			return;
		}
		seen.add(styleId);

		// get the style
		Style style = getLiveStyle(styleId);

		// add it to the stack
		if (style==null) {
			// No such style!
			// For now, just log it..
			log.error("Style definition not found: " + styleId);
			seen.remove(styleId);
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
        	fillRPrStackInternal( basedOnStyleName, rPrStack, seen);
    	} else {
    		log.debug("No basedOn set for: " + style.getStyleId() );
    	}
		
		// remove from seen so that other branches may traverse
		seen.remove(styleId);    	
	}
	
	
    private void initialiseLiveStyles() {

    	log.debug("initialiseLiveStyles()");
		liveStyles.clear();
		scanStyles();
    }

    /** (Re)read the styles part into liveStyles.  A style with no id is skipped (it can be
     *  neither referenced nor keyed). */
    private void scanStyles() {
		java.util.List<org.docx4j.wml.Style> list = styles.getStyle();
		for ( org.docx4j.wml.Style s : list ) {
			if (s.getStyleId() == null) continue;
			if (liveStyles.put(s.getStyleId(), s) == null) {
				log.debug("live style: " + s.getStyleId() );
			}
		}
		scannedStyleCount = list.size();

    }

    /**
     * Get the style with this styleId, looking first in the liveStyles map, and where it
     * is missing there, rescanning the styles part.  The map is built when this
     * PropertyResolver is constructed, and the PropertyResolver is then cached on the
     * MainDocumentPart for the life of the package (see
     * MainDocumentPart.getPropertyResolver), so without the rescan a style added to the
     * styles part afterwards would be invisible to style resolution.  The rescan is on
     * the miss path only, and only when the styles list's size has changed since it was
     * last scanned (since 17.1.1; a style which is genuinely absent used to cost one pass
     * over the styles list per lookup).
     *
     * Note that this handles styles *added* since construction, not styles modified or
     * removed: resolved properties are cached (the chain and effective maps), so a
     * change to an existing style's definition is not picked up.  For that, see
     * {@link #refresh()}.
     *
     * @since 17.0.4
     */
    private Style getLiveStyle(String styleId) {

    	if (styleId==null) return null;

    	Style result = liveStyles.get(styleId);
    	if (result==null && styles.getStyle().size() != scannedStyleCount) {
    		// styles is the styles part's own JAXB element (not a copy),
    		// so this picks up styles added since the map was built
    		scanStyles();
    		result = liveStyles.get(styleId);
    	}
    	return result;
    }

    /**
     * Discard cached state and re-read the styles part.  A style merely <em>added</em>
     * to the styles part since this PropertyResolver was constructed is picked up
     * automatically, but resolved style properties are cached, so if you have
     * <em>modified</em> or <em>removed</em> a style (or replaced the styles part's
     * JAXB element), call this to make the change visible.
     *
     * @since 17.0.4
     */
    public void refresh() throws Docx4JException {

    	chainPPr.clear();
    	chainRPr.clear();
    	effectivePPrByStyle.clear();
    	effectiveRPrByStyle.clear();
    	init();
    }

	
    /**
     * Activate a style contained in docx4j's KnownStyles.xml, making it available for use in the document.  
     * This is a recursive process, since if the style is based on another style, that other style must also be activated.
     * @param styleId
     * @return
     */
    public boolean activateStyle( String styleId  ) {

    	if (getLiveStyle(styleId)!=null) {
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

    	Style existing = getLiveStyle(s.getStyleId());
    	if (existing!=null) {
    		// Its already live

    		if (!replace) {
    			return false;
    		}

    		// Remove existing entry
			styles.getStyle().remove(existing);
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
    		result1 = activateStyle( basedOn );  // we don't check for cycles here, since we assume your KnownStyles.xml is ok.
    		
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

    	return getLiveStyle(styleId);
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
