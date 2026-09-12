package org.docx4j.model;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.openpackaging.exceptions.CyclicStylesException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.DocDefaults;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.PPr;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.Style;
import org.docx4j.wml.TblPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Works out the properties which actually apply to a paragraph, a run, a paragraph mark
 * or a table, following the order ECMA-376 17.7.2 gives and Word applies:
 *
 * <pre>
 *   effectivePPr(direct)       = docDefaults.pPr + chainPPr(styleOf(direct)) + direct
 *   effectiveRPr(direct, pPr)  = docDefaults.rPr + chainRPr(styleOf(pPr)) + chainRPr(direct.rStyle) + direct
 *   paragraphMarkRPr(pPr)      = docDefaults.rPr + chainRPr(styleOf(pPr)) + pPr.rPr
 *   tableStyle(tblPr)          = built-in Normal Table (where its chain reaches the default table style,
 *                                or it names none) + the chain + tblPr
 * </pre>
 *
 * where {@code styleOf(pPr)} is the paragraph's {@code w:pStyle} if it names a style that
 * exists, else the {@code w:default="1"} paragraph style (Word writes no {@code w:pStyle}
 * for it and treats a missing style as it), and {@code chainPPr}/{@code chainRPr} are a
 * style's {@code w:basedOn} chain merged root-first without the document defaults
 * ({@link #getChainPPr(String)}, {@link #getChainRPr(String)}).  The merging itself is
 * {@link org.docx4j.model.styles.StyleUtil#apply}, driven by
 * {@link org.docx4j.model.styles.PropertyCatalogue}.  Numbering level indents are folded
 * in per layer through the numbering part; table styles are not applied to paragraphs
 * here (the resolver is handed a {@code w:pPr} and does not know the table:
 * {@code ParagraphStylesInTableFix} carries them for the exporters).
 *
 * <p><b>Live objects.</b>  What the style overloads and {@link #getEffectivePPr(PPr)}
 * return is cached and shared: clone it before changing it.  The cached objects share no
 * leaf with the styles part, and resolution writes nothing into the part.</p>
 *
 * <p><b>Threads.</b>  One resolver is cached on the MainDocumentPart for the life of the
 * package ({@code MainDocumentPart.getPropertyResolver()}) and may be used from several
 * threads at once; a style <em>added</em> to the styles part afterwards is found, a style
 * <em>modified</em> or <em>removed</em> needs {@link #refresh()}.  Adding to the styles
 * part's list while another thread resolves is the caller's synchronisation.</p>
 *
 * <p>The design and its measurements are in
 * {@code docs/developer/change-requests/CR-015-property-resolution.md}.</p>
 *
 * @author jharrop
 */
public class PropertyResolver {
	
	private static Logger log = LoggerFactory.getLogger(PropertyResolver.class);
	
	private PPr documentDefaultPPr;
	private RPr documentDefaultRPr;
	
	public RPr getDocumentDefaultRPr() {
		return documentDefaultRPr;
	}
	public PPr getDocumentDefaultPPr() {
		return documentDefaultPPr;
	}

	private StyleDefinitionsPart styleDefinitionsPart;
	
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
	
	private NumberingDefinitionsPart numberingDefinitionsPart;

	/** Missing styles are logged once each per resolver (a w:pStyle naming a deleted style
	 *  is common in real documents, and used to log at ERROR per paragraph). */
	private final java.util.Set<String> missingLogged = java.util.concurrent.ConcurrentHashMap.newKeySet();

	private void logMissing(String styleId) {
		if (missingLogged.add(styleId)) {
			log.warn("Style definition not found: " + styleId + " (logged once)");
		}
	}


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
		
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		styleDefinitionsPart = mdp.getStyleDefinitionsPart(true);
		numberingDefinitionsPart = mdp.getNumberingDefinitionsPart();
		init();		
	}
	
	String defaultParagraphStyleId;  // "Normal" in English, but ...

	/** The styleId of the {@code w:default="1"} paragraph style, or null if the
	 *  styles part declares none.  @since 17.1.1 */
	public String getDefaultParagraphStyleId() {
		return defaultParagraphStyleId;
	}
	String defaultCharacterStyleId;
	String defaultTableStyleId;
	
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
		Style defaultTableStyle = this.styleDefinitionsPart.getDefaultTableStyle();
		defaultTableStyleId = defaultTableStyle == null ? null : defaultTableStyle.getStyleId();

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

	/**
	 * The table style which applies, merged root-first down its w:basedOn chain, then the
	 * table's own w:tblPr over it.
	 *
	 * <p>Word's built-in "Normal Table" (w:tblInd 0; cell margins 108 twips left and
	 * right, 0 top and bottom) underlies a table naming no style and a table whose chain
	 * reaches the document's default table style - and it is the built-in that applies,
	 * not the document's definition of that style: measured (CR-015 probe
	 * styles-table-default) with the document's Table Normal stating w:left 300, Word
	 * started the first cell's text 108 twips in for both, and a table style whose chain
	 * does not reach the default style got no cell margin at all.  So the default style's
	 * own layer is skipped in favour of the built-in, and a chain not reaching it starts
	 * from nothing.  Until 17.1.1 a style-less table got an empty w:tblPr and the table
	 * writers put 108 on every table.</p>
	 *
	 * @param tblPr the table's own w:tblPr; may be null
	 * @return a new Style each call, with a non-null w:tblPr
	 */
	public Style getEffectiveTableStyle(TblPr tblPr) throws CyclicStylesException {

		String styleId = (tblPr != null && tblPr.getTblStyle() != null) ? tblPr.getTblStyle().getVal() : null;
		List<Style> chain = styleId == null ? Collections.<Style>emptyList() : ancestry(styleId);
		boolean builtIn = chain.isEmpty() || (defaultTableStyleId != null && containsId(chain, defaultTableStyleId));
		log.debug(styleId == null ? "No table style specified" : "Table style: " + styleId);

		Style result = builtIn ? builtInTableNormal() : emptyTableStyle();
		for (Style layer : chain) {
			if (defaultTableStyleId != null && defaultTableStyleId.equals(layer.getStyleId())) {
				continue; // the built-in stands in for the document's definition of it
			}
			StyleUtil.apply(layer, result);
		}
		if (tblPr != null) {
			result.setTblPr(StyleUtil.apply(tblPr, result.getTblPr()));
		}
		if (result.getTblPr() == null) {
			result.setTblPr(Context.getWmlObjectFactory().createCTTblPrBase());
		}
		return result;
	}

	private static boolean containsId(List<Style> chain, String styleId) {
		for (Style s : chain) if (styleId.equals(s.getStyleId())) return true;
		return false;
	}

	/** Word's built-in Normal Table, as a style: what it applies whatever the document's own definition says. */
	private Style builtInTableNormal() {
		org.docx4j.wml.ObjectFactory f = Context.getWmlObjectFactory();
		Style s = emptyTableStyle();
		s.setStyleId(defaultTableStyleId == null ? "TableNormal" : defaultTableStyleId);
		Style.Name name = f.createStyleName();
		name.setVal("Normal Table");
		s.setName(name);
		CTTblPrBase tblPr = s.getTblPr();
		tblPr.setTblInd(twips(0));
		org.docx4j.wml.CTTblCellMar mar = f.createCTTblCellMar();
		mar.setTop(twips(0));
		mar.setLeft(twips(WORD_DEFAULT_CELL_MARGIN_TWIPS));
		mar.setBottom(twips(0));
		mar.setRight(twips(WORD_DEFAULT_CELL_MARGIN_TWIPS));
		tblPr.setTblCellMar(mar);
		return s;
	}

	/** 108 twips (0.08in): the left and right cell margin of Word's built-in Normal Table. @since 17.1.1 */
	public static final int WORD_DEFAULT_CELL_MARGIN_TWIPS = 108;

	private static org.docx4j.wml.TblWidth twips(int w) {
		org.docx4j.wml.TblWidth width = Context.getWmlObjectFactory().createTblWidth();
		width.setType("dxa");
		width.setW(BigInteger.valueOf(w));
		return width;
	}

	private static Style emptyTableStyle() {
		Style s = Context.getWmlObjectFactory().createStyle();
		s.setType("table");
		s.setTblPr(Context.getWmlObjectFactory().createCTTblPrBase());
		return s;
	}

	/**
	 * The style and the styles it is based on, root first (the base of the chain at index
	 * 0, the style itself last); empty for a null or missing styleId.  A cycle, or a chain
	 * deeper than StyleUtil.isCyclic's limit, ends the walk where it is detected (and
	 * throws if docx4j.openpackaging.exceptions.CyclicStylesException.throw says so).
	 * One walk serves paragraph, run and table resolution (until 17.1.1 each had its own).
	 */
	private List<Style> ancestry(String styleId) throws CyclicStylesException {
		List<Style> leafFirst = new ArrayList<Style>();
		List<String> seen = new ArrayList<String>();
		String id = styleId;
		while (id != null) {
			if (StyleUtil.isCyclic(id, seen, log)) break;
			seen.add(id);
			Style style = getLiveStyle(id);
			if (style == null) {
				// "DocDefaults" is StyleTree's virtual style for the document defaults, which
				// this resolver applies itself; anything else is a reference to nothing
				if (!"DocDefaults".equals(id)) logMissing(id);
				break;
			}
			leafFirst.add(style);
			id = style.getBasedOn() == null ? null : style.getBasedOn().getVal();
		}
		Collections.reverse(leafFirst);
		return leafFirst;
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
				logMissing(runStyleId);
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
			logMissing(styleId);
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
			logMissing(styleId);
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
			logMissing(styleId);
			return defaultParagraphStyleId;
		}
		return styleId;
	}

	/**
	 * A paragraph style's w:basedOn chain merged root-first, WITHOUT the document
	 * defaults: what the style contributes on its own.  An empty pPr for null or a
	 * missing style.  A live, cached object: clone before changing.
	 * @since 17.1.1
	 */
	public PPr getChainPPr(String styleId) throws CyclicStylesException {
		return chainPPr(styleId);
	}

	/**
	 * A style's w:basedOn chain merged root-first, WITHOUT the document defaults: for a
	 * character style, what it contributes over the paragraph's run properties.  An empty
	 * rPr for null or a missing style.  A live, cached object: clone before changing.
	 * @since 17.1.1
	 */
	public RPr getChainRPr(String styleId) throws CyclicStylesException {
		return chainRPr(styleId);
	}

	/** A style's w:basedOn chain merged root-first, without the document defaults; cached.  An empty pPr for null or a missing style. */
	private PPr chainPPr(String styleId) throws CyclicStylesException {
		if (styleId == null) return factory.createPPr();
		PPr chain = chainPPr.get(styleId);
		if (chain != null) return chain;
		chain = factory.createPPr();
		for (Style style : ancestry(styleId)) {
			applyPPr(headingLayer(style), chain);
		}
		chainPPr.put(styleId, chain);
		return chain;
	}

	/** A style's w:basedOn chain merged root-first, without the document defaults; cached.  An empty rPr for null or a missing style. */
	private RPr chainRPr(String styleId) throws CyclicStylesException {
		if (styleId == null) return factory.createRPr();
		RPr chain = chainRPr.get(styleId);
		if (chain != null) return chain;
		chain = factory.createRPr();
		for (Style style : ancestry(styleId)) {
			applyRPr(style.getRPr(), chain);
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
        if(log.isDebugEnabled()) {
            log.debug("result " + XmlUtils.marshaltoString(effectivePPr, true, true));
        }
	}
	
	protected void applyRPr(RPr rPrToApply, RPr effectiveRPr) {
		if (rPrToApply==null) {
			return;
		}
		StyleUtil.apply(rPrToApply, effectiveRPr);
	}	
	
	protected void applyRPr(ParaRPr rPrToApply, RPr effectiveRPr) {
		if (rPrToApply==null) {
			return;
		}
		StyleUtil.apply(rPrToApply, effectiveRPr);
	}	
	
	/**
	 * Whether the run states any formatting of its own: {@link StyleUtil#hasDirectFormatting(RPr)}
	 * over every member of {@link org.docx4j.model.styles.PropertyCatalogue#RUN} but the
	 * style reference.  (Until 17.1.1 this was a hand-kept list of 19 of the 40 members -
	 * "taken directly from RPr, and so is comprehensive", it said - so a run whose only
	 * direct formatting was w:rtl, w:position, w:szCs, w:w, w:kern or w:cs resolved as
	 * having none.)
	 * @deprecated since 17.1.1: {@link StyleUtil#hasDirectFormatting(RPr)}
	 */
	@Deprecated
	public boolean hasDirectRPrFormatting(RPr rPrToApply) {
		return StyleUtil.hasDirectFormatting(rPrToApply);
	}
	
	private boolean hasDirectRPrFormatting(ParaRPr rPrToApply) {
		return StyleUtil.hasDirectFormatting(rPrToApply);
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
	 * A style's own w:pPr as a layer of its chain.  The built-in heading styles have a
	 * fixed outline level, which Word takes from the style's built-in NAME ("heading 1"
	 * ... "heading 9", the same in every locale; the styleId is localised:
	 * "berschrift1", "Titre1"), whatever w:outlineLvl the style declares: where they
	 * differ the layer is a copy with the level from the name, and the styles part is not
	 * touched (until 17.1.1 this keyed on the id prefix "Heading" and wrote the level into
	 * the style).
	 */
	private PPr headingLayer(Style style) {
		PPr layer = style.getPPr();
		int headingLevel = headingLevelByName(style);
		if (headingLevel > 0 && layer != null
				&& layer.getOutlineLvl() != null && layer.getOutlineLvl().getVal() != null
				&& layer.getOutlineLvl().getVal().intValue() != headingLevel - 1) {
			log.debug(style.getStyleId() + " - outline level " + (headingLevel - 1) + " from its name, not the declared " + layer.getOutlineLvl().getVal());
			layer = XmlUtils.deepCopy(layer);
			layer.getOutlineLvl().setVal(BigInteger.valueOf(headingLevel - 1));
		}
		return layer;
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
	
}
