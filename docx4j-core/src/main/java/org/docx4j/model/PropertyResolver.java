package org.docx4j.model;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.model.table.CellContext;
import org.docx4j.model.table.TableContext;
import org.docx4j.openpackaging.exceptions.CyclicStylesException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.CTTblStylePr;
import org.docx4j.wml.DocDefaults;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
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
 *   tableStyle(tblPr)          = built-in Normal Table (where its chain reaches a style named "Normal Table",
 *                                or there is none) + the chain above that + tblPr
 *
 *   in a table cell, given its CellContext (since 17.3.1):
 *   effectivePPr(direct, ctx)  = docDefaults.pPr + table(ctx).pPr + chainPPr(styleOf(direct)) [+ jc exception] + direct
 *   effectiveRPr(direct, pPr, ctx)
 *                              = docDefaults.rPr + level(table(ctx).rPr, chainRPr(styleOf(pPr))) [+ size exception]
 *                                + chainRPr(direct.rStyle) + direct
 *   table(ctx)                 = the table style's own pPr/rPr, then its conditional formats which
 *                                apply, in ECMA-376-1 17.7.6 order
 * </pre>
 *
 * where {@code styleOf(pPr)} is the paragraph's {@code w:pStyle} if it names a style that
 * exists, else the {@code w:default="1"} paragraph style (Word writes no {@code w:pStyle}
 * for it and treats a missing style as it), and {@code chainPPr}/{@code chainRPr} are a
 * style's {@code w:basedOn} chain merged root-first without the document defaults
 * ({@link #getChainPPr(String)}, {@link #getChainRPr(String)}).  The merging itself is
 * {@link org.docx4j.model.styles.StyleUtil#apply}, driven by
 * {@link org.docx4j.model.styles.PropertyCatalogue}.  Numbering level indents are folded
 * in per layer through the numbering part.  A table style reaches a paragraph where the
 * caller hands the resolver the paragraph's {@link CellContext} (since 17.3.1; from a
 * {@link org.docx4j.model.table.TableContextTracker} during a walk, or
 * {@link #cellContextOf(P)}): the overloads which take none know nothing of tables.  For
 * the outputs which name styles, {@code ParagraphStylesInTableFix} gives each table
 * paragraph a synthetic style named and built here ({@link #styleIdFor},
 * {@link #syntheticStyle}, {@link #sourceStyleOf}; CR-030).
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
		
		this.wordMLPackage = wordMLPackage;
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		styleDefinitionsPart = mdp.getStyleDefinitionsPart(true);
		numberingDefinitionsPart = mdp.getNumberingDefinitionsPart();
		init();		
	}
	
	String defaultParagraphStyleId;  // "Normal" in English, but ...

	/** The styleId of the {@code w:default="1"} paragraph style, or null if the
	 *  styles part declares none.  @since 17.2.0 */
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

		// [MS-DOCX] overrideTableStyleFontSizeAndJustification: see tableStyleSizeJcException
		CompatibilityOptions compat = CompatibilityOptions.of(wordMLPackage);
		tableStyleSizeJcException = compat.mode() < 15
				&& !compat.setting("overrideTableStyleFontSizeAndJustification", false);

		// Initialise styles
		styles = (org.docx4j.wml.Styles)styleDefinitionsPart.getJaxbElement();	
		initialiseLiveStyles();		
		
		DocDefaults docDefaults = styleDefinitionsPart.getJaxbElement().getDocDefaults();
        if(log.isDebugEnabled()) {
            log.debug(XmlUtils.marshaltoString(docDefaults, true, true));
        }

		// private copies: the resolver's defaults are its own, so nothing below writes into
		// the styles part (until 17.2.0 the w:sz 20 default went into the part, and a docx
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
	 * <p>Word goes by the style's <em>name</em> (measured, CR-030 probes T1, T2 and T7).  A
	 * style named "Normal Table" is Word's built-in Normal Table (w:tblInd 0; cell margins 108
	 * twips left and right, 0 top and bottom), whatever its own definition says and whether
	 * or not it is the document's default: the built-in stands in for it, and the walk up
	 * the chain ends there.  Every other style applies as written, with nothing beneath it:
	 * a chain which reaches no style so named starts from nothing, so a table style with no
	 * w:basedOn has no cell margin at all (CR-015 probe styles-table-default), and the
	 * document's default table style, where it is named otherwise, applies as written to a
	 * table naming no style (T7: its margins of 300, or none).  The built-in also stands in
	 * for a style which is missing, and where the document has no default table style.
	 * (Until 17.3.1 the built-in was decided by the default table style's <em>id</em>; until
	 * 17.2.0 a style-less table got an empty w:tblPr and the table writers put 108 on every
	 * table.)</p>
	 *
	 * <p>The chain is {@link #getTableStyleChain(String)}'s, copied: its cache is shared.</p>
	 *
	 * @param tblPr the table's own w:tblPr; may be null
	 * @return a new Style each call, with a non-null w:tblPr
	 */
	public Style getEffectiveTableStyle(TblPr tblPr) throws CyclicStylesException {

		String styleId = getTableStyleIdOf(tblPr);
		log.debug(styleId == null ? "No table style specified" : "Table style: " + styleId);

		Style result = builtInTableNormalUnderlies(styleId) ? builtInTableNormal() : emptyTableStyle();
		Style chain = getTableStyleChain(styleId);
		if (chain != NO_TABLE_STYLE) {
			StyleUtil.apply((Style)XmlUtils.deepCopy(chain), result);
		}
		if (tblPr != null) {
			result.setTblPr(StyleUtil.apply(tblPr, result.getTblPr()));
		}
		if (result.getTblPr() == null) {
			result.setTblPr(Context.getWmlObjectFactory().createCTTblPrBase());
		}
		return result;
	}

	/**
	 * Whether Word's built-in Normal Table underlies a table - the flag
	 * {@link #getEffectiveTableStyle(TblPr)} decides by: the table's style chain (its own
	 * w:tblStyle, else the default table style) reaches a style <em>named</em> "Normal
	 * Table", or the style is missing, or there is none.  Exposed for the parity harnesses,
	 * which otherwise infer it from the cell margins.  Since 17.3.1 decided by name (CR-030
	 * T2, T7); the method keeps its name from when it was decided by the default style's id.
	 * @param tblPr the table's own w:tblPr; may be null
	 * @since 17.2.0
	 */
	public boolean reachesDefaultTableStyle(TblPr tblPr) throws CyclicStylesException {
		return builtInTableNormalUnderlies(getTableStyleIdOf(tblPr));
	}

	private boolean builtInTableNormalUnderlies(String styleId) throws CyclicStylesException {
		if (styleId == null) return true;
		List<Style> chain = ancestry(styleId);
		if (chain.isEmpty()) return true;
		for (Style s : chain) {
			if (isNamedNormalTable(s)) return true;
		}
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

	/** 108 twips (0.08in): the left and right cell margin of Word's built-in Normal Table. @since 17.2.0 */
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
	 * One walk serves paragraph, run and table resolution (until 17.2.0 each had its own).
	 * Public since 17.2.0 for the parity harnesses; the styles are the live ones, read them.
	 */
	public List<Style> ancestry(String styleId) throws CyclicStylesException {
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
	 * (since 17.2.0; used to return null).
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
	 * {@link #getEffectiveParagraphMarkRPr(PPr)}.  (Until 17.2.0 a run with no rPr in a
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
	 * @since 17.2.0
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
	 * @deprecated since 17.2.0: {@link #getEffectiveRPr(RPr, PPr)} composes the paragraph style itself
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

	/**
	 * The character style's chain (the style it names, if it exists), then the direct
	 * formatting.
	 *
	 * <p>The character style is a <b>level</b> of the style hierarchy over the paragraph's,
	 * so its toggle properties are XORed with what is beneath rather than overriding it
	 * (ECMA-376-1 &#xa7;17.7.3, quoted in {@link StyleUtil#applyStyleLevel}).  The direct
	 * formatting after it is not a level: an explicit value there is used as it stands.</p>
	 */
	private void applyCharacterStyleAndDirect(RPr expressRPr, RPr effectiveRPr) throws CyclicStylesException {

		if (expressRPr != null && expressRPr.getRStyle() != null && expressRPr.getRStyle().getVal() != null) {
			String runStyleId = expressRPr.getRStyle().getVal();
			if (getLiveStyle(runStyleId) == null) {
				logMissing(runStyleId);
			} else {
				StyleUtil.applyStyleLevel(chainRPr(runStyleId), effectiveRPr, documentDefaultRPr);
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
	 * @deprecated since 17.2.0: the flags existed to keep the document defaults from
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

	// ---------------------------------------------------------------- the table context (CR-030)

	/** The package, for its settings (the [MS-DOCX] exception below).  @since 17.3.1 */
	private final WordprocessingMLPackage wordMLPackage;

	/**
	 * Whether [MS-DOCX]'s {@code overrideTableStyleFontSizeAndJustification} exception applies
	 * to this document: a default paragraph style's 12pt does not override the table style's
	 * size, nor its left justification the table style's, for paragraphs in tables.  Measured
	 * with Word 365 (CR-030 probes T5 and T6): it applies below compatibility mode 15 (a
	 * document stating no compatibilityMode is mode 12) where the setting is not on, and never
	 * in mode 15, where Word ignores a stated 0 and re-saves it as 1.  Read once per
	 * {@link #refresh()}.  @since 17.3.1
	 */
	private volatile boolean tableStyleSizeJcException;

	/** Each table style's w:basedOn chain merged, by id; styles only, so refresh() clears it. */
	private final java.util.Map<String, Style> tableStyleChains = new java.util.concurrent.ConcurrentHashMap<String, Style>();

	/** A paragraph style composed over a table level, document defaults included; by
	 *  (paragraph style, table level).  What getEffectivePPr/getEffectiveRPr with a context
	 *  start from, as getEffectivePPr(String) is what they start from without one. */
	private final java.util.Map<Composition, PPr> composedPPr = new java.util.concurrent.ConcurrentHashMap<Composition, PPr>();
	private final java.util.Map<Composition, RPr> composedRPr = new java.util.concurrent.ConcurrentHashMap<Composition, RPr>();

	/** The cache key of a composition: a structured tuple, never a joined string (CR-030 D8). */
	private static final class Composition {
		final String styleId;
		final CellContext.Key table;
		Composition(String styleId, CellContext.Key table) {
			this.styleId = styleId;
			this.table = table;
		}
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Composition)) return false;
			Composition c = (Composition) o;
			return (styleId == null ? c.styleId == null : styleId.equals(c.styleId)) && table.equals(c.table);
		}
		@Override
		public int hashCode() {
			return 31 * (styleId == null ? 0 : styleId.hashCode()) + table.hashCode();
		}
	}

	/** An empty table style: what a table with no style to apply contributes. */
	private static final Style NO_TABLE_STYLE = Context.getWmlObjectFactory().createStyle();

	/**
	 * Whether [MS-DOCX]'s overrideTableStyleFontSizeAndJustification exception applies to this
	 * document (below compatibility mode 15, the setting not on; measured, CR-030 T5 and T6).
	 * @since 17.3.1
	 */
	public boolean appliesTableStyleSizeJcException() {
		return tableStyleSizeJcException;
	}

	// ------------------------------------------------ synthetic style names (CR-030 phase 2)

	/**
	 * What a synthetic paragraph style stands for: a paragraph style composed over a table
	 * level.  Kept across {@link #refresh()}: the ids are written into the document.
	 */
	private static final class Synthetic {
		final String sourceStyleId;
		final CellContext context;
		Synthetic(String sourceStyleId, CellContext context) {
			this.sourceStyleId = sourceStyleId;
			this.context = context;
		}
	}

	private final java.util.Map<String, Synthetic> synthetics = new java.util.concurrent.ConcurrentHashMap<String, Synthetic>();
	private final java.util.Map<Composition, String> syntheticIds = new java.util.concurrent.ConcurrentHashMap<Composition, String>();

	/**
	 * The id of the synthetic paragraph style which stands for a paragraph style in a table
	 * cell's context: named as ParagraphStylesInTableFix has named them since 17.2.0
	 * ({@code <paragraph style>-<table style>[-<conditions>]-BR}), and distinct where that
	 * name is taken by another (paragraph style, table level), or by a style of the
	 * document: style ids may contain the hyphens the name is joined with, so a name alone
	 * is ambiguous (CR-030 D8).  The same pair always gets the same id.
	 *
	 * <p>For the outputs which name styles (HTML's CSS classes; the FO XSLT pathway, which
	 * resolves a paragraph by its w:pStyle alone).  {@link #syntheticStyle(String)} gives the
	 * style itself and {@link #sourceStyleOf(String)} the paragraph style it stands for.</p>
	 *
	 * @param paragraphStyleId the paragraph's w:pStyle (or null for none); a missing style is
	 *        the default paragraph style, as everywhere in resolution, and a synthetic id
	 *        stands for its source
	 * @param cellContext the paragraph's context; it must name a table style
	 * @since 17.3.1
	 */
	public synchronized String styleIdFor(String paragraphStyleId, CellContext cellContext) {
		String source = existingParagraphStyle(sourceStyleOf(paragraphStyleId));
		Composition key = new Composition(source, cellContext.getKey());
		String id = syntheticIds.get(key);
		if (id != null) return id;

		String tableStyle = cellContext.getTableStyleId();
		String conditions = org.docx4j.model.table.TableStyleConditions.key(cellContext.getKey().getConditions());
		String name = source + "-" + tableStyle + (conditions.length() > 0 ? "-" + conditions : "");
		if (!(tableStyle != null && tableStyle.endsWith("-BR") && conditions.length() == 0)) {
			name = name + "-BR";
		}
		id = name;
		for (int n = 2; synthetics.containsKey(id) || getLiveStyle(id) != null; n++) {
			id = name + "-" + n;
		}
		synthetics.put(id, new Synthetic(source, cellContext));
		syntheticIds.put(key, id);
		return id;
	}

	/**
	 * The synthetic paragraph style an id from {@link #styleIdFor} names: a paragraph style
	 * with no w:basedOn whose w:pPr and w:rPr are what the resolver composes for its
	 * (paragraph style, table level), document defaults included, so that resolving it the
	 * plain way gives the in-context answer (CR-030 decision 7).  A new object each call; null
	 * for an id which is not one.
	 * @since 17.3.1
	 */
	public Style syntheticStyle(String id) throws CyclicStylesException {
		Synthetic synthetic = synthetics.get(id);
		if (synthetic == null) return null;
		Style style = Context.getWmlObjectFactory().createStyle();
		style.setType("paragraph");
		style.setStyleId(id);
		Style.Name name = Context.getWmlObjectFactory().createStyleName();
		name.setVal(id);
		style.setName(name);
		if (synthetic.context.formatsText()) {
			style.setPPr((PPr)XmlUtils.deepCopy(composedPPr(synthetic.sourceStyleId, synthetic.context)));
			style.setRPr((RPr)XmlUtils.deepCopy(composedRPr(synthetic.sourceStyleId, synthetic.context)));
		} else {
			style.setPPr((PPr)XmlUtils.deepCopy(getEffectivePPr(synthetic.sourceStyleId)));
			RPr rPr = synthetic.sourceStyleId == null ? null : getEffectiveRPr(synthetic.sourceStyleId);
			style.setRPr(rPr == null ? (RPr)XmlUtils.deepCopy(documentDefaultRPr) : (RPr)XmlUtils.deepCopy(rPr));
		}
		return style;
	}

	/**
	 * The paragraph style a synthetic style id from {@link #styleIdFor} stands for; any
	 * other id unchanged.  For code which reads a paragraph's w:pStyle and needs the
	 * document's own style: numbering (a level linked to a style), STYLEREF, contextual
	 * spacing (CR-030 D1, D7).
	 * @since 17.3.1
	 */
	public String sourceStyleOf(String styleId) {
		if (styleId == null) return null;
		Synthetic synthetic = synthetics.get(styleId);
		return synthetic == null ? styleId : synthetic.sourceStyleId;
	}

	/**
	 * The id of the table style a table resolves to: its own {@code w:tblStyle}, else the
	 * document's {@code w:default} table style, whatever its name.  (Whether that style
	 * contributes anything is {@link #getTableStyleChain(String)}'s question.)
	 * @param tblPr the table's own w:tblPr; may be null
	 * @return the id, or null where the table names none and the document has no default
	 * @since 17.3.1
	 */
	public String getTableStyleIdOf(TblPr tblPr) {
		if (tblPr != null && tblPr.getTblStyle() != null) {
			return tblPr.getTblStyle().getVal();
		}
		return defaultTableStyleId;
	}

	/**
	 * A table style's {@code w:basedOn} chain merged root-first, conditional formats merged
	 * per condition ({@link StyleUtil#apply(Style, Style)}), as it gives the paragraphs of its
	 * tables their text formatting.  Word goes by the style's <em>name</em>: a style named
	 * "Normal Table" is Word's built-in, which gives text nothing whatever its own definition
	 * says, so the walk up the chain ends below it; every other style applies as written, the
	 * default table style included (measured, CR-030 probes T1, T2 and T7).
	 *
	 * <p>Cached per id, and shared: read it, do not change it.  An empty style for null, for a
	 * missing style, and for a chain which is "Normal Table" all the way down.</p>
	 *
	 * @throws RuntimeException wrapping a CyclicStylesException, where
	 *         docx4j.openpackaging.exceptions.CyclicStylesException.throw asks for one
	 * @since 17.3.1
	 */
	public Style getTableStyleChain(String styleId) {
		if (styleId == null) return NO_TABLE_STYLE;
		Style cached = tableStyleChains.get(styleId);
		if (cached != null) return cached;
		List<Style> chain;
		try {
			chain = ancestry(styleId);
		} catch (CyclicStylesException e) {
			throw new RuntimeException(e);
		}
		int start = 0;
		for (int i = chain.size() - 1; i >= 0; i--) {
			if (isNamedNormalTable(chain.get(i))) {
				start = i + 1;
				break;
			}
		}
		Style merged = null;
		for (int i = start; i < chain.size(); i++) {
			merged = StyleUtil.apply(chain.get(i), merged);
		}
		if (merged == null) merged = NO_TABLE_STYLE;
		tableStyleChains.put(styleId, merged);
		return merged;
	}

	private static boolean isNamedNormalTable(Style s) {
		// Google Docs (Nov 2014) writes table styles without a w:name
		return s.getName() != null && "Normal Table".equals(s.getName().getVal());
	}

	/**
	 * The context of a table, for the paragraphs in it: build one per table and hold it while
	 * walking the table (the resolver keeps none, since it reads the table's content).
	 * @since 17.3.1
	 */
	public TableContext tableContext(Tbl tbl) {
		return new TableContext(tbl, this);
	}

	/**
	 * The table context of one paragraph, found through its parent pointers: the nearest
	 * enclosing cell, its row and its table, through whatever lies between ({@code w:sdt},
	 * {@code w:customXml}, {@code w:smartTag}).  Null where the paragraph is in no table, where
	 * a story begins before a cell is reached (a text box, a footnote, endnote or comment, a
	 * header, a footer, the body), or where the pointers are not there: content created with
	 * the ObjectFactory and added with {@code getContent().add()} has none, while unmarshalled,
	 * cloned and deep-copied content has them.
	 *
	 * <p>Each call builds a {@link TableContext}, a walk of the table's rows.  That is right
	 * for "what formatting does this paragraph have"; code resolving every paragraph of a
	 * document walks it with a {@link org.docx4j.model.table.TableContextTracker} instead, since
	 * calling this for each paragraph of a large table is quadratic.</p>
	 * @since 17.3.1
	 */
	public CellContext cellContextOf(P p) {
		Tc tc = null;
		Tr tr = null;
		for (Object o = parentOf(p); o != null; o = parentOf(o)) {
			if (o instanceof org.docx4j.wml.CTTxbxContent || o instanceof org.docx4j.wml.CTFtnEdn
					|| o instanceof org.docx4j.wml.Comments.Comment || o instanceof org.docx4j.wml.Hdr
					|| o instanceof org.docx4j.wml.Ftr || o instanceof org.docx4j.wml.Body
					|| o instanceof org.docx4j.wml.Document) {
				return null;
			}
			if (o instanceof Tc) {
				if (tc == null) tc = (Tc) o;
			} else if (o instanceof Tr) {
				if (tc != null && tr == null) tr = (Tr) o;
			} else if (o instanceof Tbl) {
				return tr == null ? null : tableContext((Tbl) o).forParagraph(tr, tc, p.getPPr());
			}
		}
		return null;
	}

	private static Object parentOf(Object o) {
		return (o instanceof org.jvnet.jaxb.lang.Child) ? ((org.jvnet.jaxb.lang.Child) o).getParent() : null;
	}

	/**
	 * The paragraph properties which apply to a paragraph in a table cell: as
	 * {@link #getEffectivePPr(PPr)}, with the table style's contribution (its own w:pPr, then
	 * the conditional formats the paragraph is under) between the document defaults and the
	 * paragraph's style, and [MS-DOCX]'s justification exception where it applies.
	 *
	 * @param cellContext where the paragraph sits; null for a paragraph in no table, which
	 *        resolves exactly as {@link #getEffectivePPr(PPr)}
	 * @return a live object where the paragraph has no direct formatting; clone it before changing it
	 * @since 17.3.1
	 */
	public PPr getEffectivePPr(PPr expressPPr, CellContext cellContext) throws CyclicStylesException {
		if (cellContext == null || !cellContext.formatsText()) {
			return getEffectivePPr(expressPPr);
		}
		PPr composed = composedPPr(sourceParagraphStyleOf(expressPPr), cellContext);
		if (hasDirectPPrFormatting(expressPPr)) {
			PPr effectivePPr = (PPr)XmlUtils.deepCopy(composed);
			applyPPr(expressPPr, effectivePPr);
			return effectivePPr;
		}
		return composed;
	}

	/**
	 * The run properties which apply to a run in a table cell: as
	 * {@link #getEffectiveRPr(RPr, PPr)}, with the table style's run properties (its own,
	 * then its conditional formats') as a level of the style hierarchy beneath the paragraph
	 * style's (the toggle properties combine across the two, ECMA-376-1 &#xa7;17.7.3), and
	 * [MS-DOCX]'s size exception where it applies.
	 *
	 * @param cellContext where the paragraph sits; null resolves exactly as
	 *        {@link #getEffectiveRPr(RPr, PPr)}
	 * @return a new object each call
	 * @since 17.3.1
	 */
	public RPr getEffectiveRPr(RPr expressRPr, PPr pPr, CellContext cellContext) throws CyclicStylesException {
		if (cellContext == null || !cellContext.formatsText()) {
			return getEffectiveRPr(expressRPr, pPr);
		}
		RPr effectiveRPr = (RPr)XmlUtils.deepCopy(composedRPr(sourceParagraphStyleOf(pPr), cellContext));
		applyCharacterStyleAndDirect(expressRPr, effectiveRPr);
		return effectiveRPr;
	}

	/**
	 * The run properties of the mark of a paragraph in a table cell: as
	 * {@link #getEffectiveParagraphMarkRPr(PPr)}, with the table level beneath the paragraph
	 * style's.
	 * @since 17.3.1
	 */
	public RPr getEffectiveParagraphMarkRPr(PPr pPr, CellContext cellContext) throws CyclicStylesException {
		if (cellContext == null || !cellContext.formatsText()) {
			return getEffectiveParagraphMarkRPr(pPr);
		}
		RPr effectiveRPr = (RPr)XmlUtils.deepCopy(composedRPr(sourceParagraphStyleOf(pPr), cellContext));
		if (pPr!=null && pPr.getRPr()!=null) {
			applyRPr(pPr.getRPr(), effectiveRPr);
		}
		return effectiveRPr;
	}

	/** The paragraph's style for composing over a table level: where it names a synthetic
	 *  style (the preprocess has run), the style that stands for, so the table level is not
	 *  applied twice.  @since 17.3.1 */
	private String sourceParagraphStyleOf(PPr pPr) {
		return existingParagraphStyle(sourceStyleOf(paragraphStyleOf(pPr)));
	}

	/** The table level's w:pPr: the style's own, then its conditional formats'; null if none. */
	private static PPr tableLevelPPr(CellContext ctx) {
		PPr out = ctx.getTableStyle().getPPr() == null ? null : StyleUtil.apply(ctx.getTableStyle().getPPr(), (PPr)null);
		for (CTTblStylePr pr : ctx.getTextConditions()) {
			out = StyleUtil.apply(pr.getPPr(), out);
		}
		return out;
	}

	/** The table level's w:rPr: the style's own, then its conditional formats'; null if none. */
	private static RPr tableLevelRPr(CellContext ctx) {
		RPr out = ctx.getTableStyle().getRPr() == null ? null : StyleUtil.apply(ctx.getTableStyle().getRPr(), (RPr)null);
		for (CTTblStylePr pr : ctx.getTextConditions()) {
			out = StyleUtil.apply(pr.getRPr(), out);
		}
		return out;
	}

	/*
	 * The composition is ParagraphStylesInTableFix's (17.2.0) moved, not reinvented: document
	 * defaults, the table level over them, the paragraph style's chain over that (for the run
	 * properties a level of its own, the toggles combined), then the [MS-DOCX] exception.  The
	 * preprocess built it into a synthetic paragraph style per (paragraph style, table style,
	 * conditions); this is the same thing, cached per (paragraph style, table level).
	 */
	private PPr composedPPr(String styleId, CellContext ctx) throws CyclicStylesException {
		Composition key = new Composition(styleId, ctx.getKey());
		PPr composed = composedPPr.get(key);
		if (composed != null) return composed;

		PPr tableLevel = tableLevelPPr(ctx);
		composed = (PPr)XmlUtils.deepCopy(documentDefaultPPr);
		if (tableLevel != null) {
			StyleUtil.apply(tableLevel, composed);
		}
		applyPPr(chainPPr(styleId), composed);
		if (tableStyleSizeJcException && tableLevel != null && tableLevel.getJc() != null
				&& paragraphStyleGivesWay(styleId, false)) {
			composed.setJc(XmlUtils.deepCopy(tableLevel.getJc()));
		}
		composedPPr.put(key, composed);
		return composed;
	}

	private RPr composedRPr(String styleId, CellContext ctx) throws CyclicStylesException {
		Composition key = new Composition(styleId, ctx.getKey());
		RPr composed = composedRPr.get(key);
		if (composed != null) return composed;

		RPr tableOnly = tableLevelRPr(ctx);
		RPr tableLevel = (RPr)XmlUtils.deepCopy(documentDefaultRPr);
		if (tableOnly != null) {
			StyleUtil.apply(tableOnly, tableLevel);
		}
		RPr paragraphLevel = chainRPr(styleId);
		composed = (RPr)XmlUtils.deepCopy(tableLevel);
		applyRPr(paragraphLevel, composed);
		// the table and the paragraph style are two levels of the hierarchy: the twelve
		// toggles combine across them rather than the paragraph's overriding (17.7.3)
		StyleUtil.applyToggles(paragraphLevel, tableLevel, documentDefaultRPr, composed);
		if (tableStyleSizeJcException && tableOnly != null && tableOnly.getSz() != null
				&& paragraphStyleGivesWay(styleId, true)) {
			composed.setSz(XmlUtils.deepCopy(tableOnly.getSz()));
		}
		composedRPr.put(key, composed);
		return composed;
	}

	/**
	 * [MS-DOCX]'s exception, for a paragraph in a table whose style states a size (or a
	 * justification): the paragraph's style gives way to it if the style is the default
	 * paragraph style, or states no size (justification) of its own, and resolves to 12pt
	 * (left).  A style of the paragraph's own which states one keeps it.  As
	 * ParagraphStylesInTableFix applied it (measured there with Word 2010; CR-030 T6 with Word
	 * 365 in modes 12 and 14).
	 */
	private boolean paragraphStyleGivesWay(String styleId, boolean size) throws CyclicStylesException {
		boolean isDefault = styleId == null || styleId.equals(defaultParagraphStyleId);
		Style express = getLiveStyle(styleId);
		if (!isDefault && express != null) {
			if (size && express.getRPr() != null && express.getRPr().getSz() != null) return false;
			if (!size && express.getPPr() != null && express.getPPr().getJc() != null) return false;
		}
		if (size) {
			RPr effective = getEffectiveRPr(styleId);
			return effective != null && effective.getSz() != null && effective.getSz().getVal() != null
					&& effective.getSz().getVal().intValue() == 24;
		} else {
			PPr effective = getEffectivePPr(styleId);
			return effective != null && effective.getJc() != null
					&& effective.getJc().getVal() == JcEnumeration.LEFT;
		}
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
	 * @since 17.2.0
	 */
	public PPr getChainPPr(String styleId) throws CyclicStylesException {
		return chainPPr(styleId);
	}

	/**
	 * A style's w:basedOn chain merged root-first, WITHOUT the document defaults: for a
	 * character style, what it contributes over the paragraph's run properties.  An empty
	 * rPr for null or a missing style.  A live, cached object: clone before changing.
	 * @since 17.2.0
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
	 * Whether the paragraph states any formatting of its own: {@link StyleUtil#hasDirectFormatting(org.docx4j.wml.PPrBase)}
	 * over every member of {@link org.docx4j.model.styles.PropertyCatalogue#PARAGRAPH}.
	 * Any rPr is intentionally ignored, since pPr/rPr is not applicable to anything
	 * except the paragraph mark.  (Until 17.2.0 this was a hand-kept list of 17 of the
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
	 * style reference.  (Until 17.2.0 this was a hand-kept list of 19 of the 40 members -
	 * "taken directly from RPr, and so is comprehensive", it said - so a run whose only
	 * direct formatting was w:rtl, w:position, w:szCs, w:w, w:kern or w:cs resolved as
	 * having none.)
	 * @deprecated since 17.2.0: {@link StyleUtil#hasDirectFormatting(RPr)}
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
     * @since 17.2.0
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
	 * touched (until 17.2.0 this keyed on the id prefix "Heading" and wrote the level into
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
     * last scanned (since 17.2.0; a style which is genuinely absent used to cost one pass
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
    	tableStyleChains.clear();
    	composedPPr.clear();
    	composedRPr.clear();
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
