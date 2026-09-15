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
package org.docx4j.fonts;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps font names used in the document to 
 * fonts physically available
 * on the system.
 * 
 * So, a mapper per document.
 * If fonts are added to the document
 * (ie fonts introduced into use)
 * then the mapper should be updated
 * to include a mapping for the
 * new font.
 * 
 * There are 2 implementations, which since 17.1.1 share one order of precedence (see
 * {@link #populateFontMappings} and the passes WordprocessingMLPackage.setFontMapper
 * runs after it) and differ only in {@link #resolveDocumentFont} and
 * {@link #addMapperSubstitutes}:
 *
 * - IdentityPlusMapper, the default: the font of that name, else a variant of the
 *   name ("Calibri Light Regular"), else the shared passes
 *
 * - BestMatchingMapper, which adds a panose match and FontSubstitutions.xml for
 *   whatever the shared passes leave unmapped
 *
 * Measured on three real-document corpora in six font environments (CR-016 phase 0c,
 * and again after phase 3): IdentityPlusMapper is ahead of BestMatchingMapper in every
 * one of them, so it is the one to use; BestMatchingMapper is kept for compatibility,
 * and with the shared passes it is now within 0.001 to 0.005 of line parity, where it
 * had been 0.010 to 0.021 behind.
 *
 * Whichever one you use, you can
 * add/remove mappings programmatically
 * to customise to your needs.
 *
 * @author jharrop
 *
 */
public abstract class Mapper {
	
	
	protected static Logger log = LoggerFactory.getLogger(Mapper.class);

	// For embedded fonts, which we can't store in our system-wide PhysicalFonts,
	// but we can and do put them in the local field fontMappings
	protected ConcurrentHashMap<String, PhysicalFont> regularForms = new ConcurrentHashMap<String, PhysicalFont>();
	protected ConcurrentHashMap<String, PhysicalFont> boldForms = new ConcurrentHashMap<String, PhysicalFont>();
	protected ConcurrentHashMap<String, PhysicalFont> italicForms = new ConcurrentHashMap<String, PhysicalFont>();
	protected ConcurrentHashMap<String, PhysicalFont> boldItalicForms = new ConcurrentHashMap<String, PhysicalFont>();

	public Mapper() {
		super();
	}
	
	/* not static since 11.5.14, so embedded fonts can be put in here.
	 * The alternative would be to have a separate map for embedded fonts,
	 * which get consulted if it wasn't found in fontMappings. 
	 */
	protected final ConcurrentHashMap<String, PhysicalFont> fontMappings = new ConcurrentHashMap<String, PhysicalFont>();
	
	/** @deprecated the live map, keyed by the lower-cased name; use {@link #get} and
	 *  {@link #put}, which are case-insensitive, and {@link #physicalFontNamed} for a
	 *  lookup by the physical name. */
	@Deprecated
	public Map<String, PhysicalFont> getFontMappings() {
		return fontMappings;
	}	
	
	/**
	 * Get a PhysicalFont from FontMappings, 
	 * by case-insensitive name.  (Although Word always
	 * uses Title Case for font names, it is actually
	 * case insensitive; the spec is silent on this.)
	 *
	 * @param key the document font's name, or null for a slot nothing names
	 * @return the physical font it is mapped to, or null
	 */
	public PhysicalFont get(String key) {
		if (key==null) return null; // a slot nothing names (17.1.1; was a NullPointerException)
		return fontMappings.get(key.toLowerCase());
	}
	/**
	 * Put a PhysicalFont into FontMappings, 
	 * by case-insensitive name.  (Although Word always
	 * uses Title Case for font names, it is actually
	 * case insensitive; the spec is silent on this.)
	 *
	 * @param key the document font's name
	 * @param pf the physical font it is to be rendered in
	 */
	public void put(String key, PhysicalFont pf) {
		
		PhysicalFont priorPf = fontMappings.get(key.toLowerCase());
		if (priorPf != null) {
			if (priorPf == pf) {
				// No change, nothing to do.
				return;
			}
			if (log.isWarnEnabled()) {
				log.warn("Overwriting existing fontMapping: " + key.toLowerCase() + " at " + priorPf.embeddedURI + " with " + pf.getEmbeddedURI());
			}
		}		
		fontMappings.put(key.toLowerCase(), pf);
	}
	public int size() {
		return fontMappings.size();
	}

	/**
	 * The PhysicalFont behind a <em>physical</em> font name - a font-family the FO layer
	 * wrote, with or without the twin suffixes ({@link PhysicalFonts#stripSuffixes}).
	 *
	 * <p>The Mapper is keyed by the document's names, and {@link PhysicalFonts} by the
	 * installed fonts' names; a font embedded in the document is in neither key set (it is
	 * deliberately not added to PhysicalFonts, which every document shares; see
	 * ObfuscatedFontPart.extract), so a post-process holding only the name it wrote must
	 * look through this document's mappings - what the runs mapped to, the embedded faces,
	 * the last-resort fallbacks - before the installed fonts.  Until 17.1.1 the line-height,
	 * space-kerning, character-scaling and ligature passes looked the name up in
	 * PhysicalFonts alone, and so never saw an embedded font (CR-016 gap 7).</p>
	 *
	 * @return the font, or null where nothing this document knows or the machine has
	 *         carries that name
	 * @since 17.1.1
	 */
	public PhysicalFont physicalFontNamed(String physicalFontName) {
		if (physicalFontName==null) return null;
		String name = PhysicalFonts.stripSuffixes(physicalFontName);
		PhysicalFont pf = named(fontMappings, name);
		if (pf==null) pf = named(regularForms, name);
		if (pf==null) pf = named(boldForms, name);
		if (pf==null) pf = named(italicForms, name);
		if (pf==null) pf = named(boldItalicForms, name);
		if (pf==null) pf = named(lastResortFallbacks, name);
		return pf!=null ? pf : PhysicalFonts.get(name);
	}

	private static PhysicalFont named(Map<String, PhysicalFont> fonts, String name) {
		for (PhysicalFont pf : fonts.values()) {
			if (pf!=null && name.equalsIgnoreCase(pf.getName())) return pf;
		}
		return null;
	}
	
	// ---- the decisions: what each pass did, and why.  @since 17.1.1 (CR-017 phase 1)

	/** Keyed by the lower-cased document font, as {@link #fontMappings} is. */
	private final ConcurrentHashMap<String, FontDecision> decisions = new ConcurrentHashMap<String, FontDecision>();

	/**
	 * Record what a pass decided for this document font, replacing any earlier decision
	 * (a later pass may re-map the font) but keeping the per-script choices the selector
	 * has recorded against it.
	 *
	 * @param documentFont the name as the document has it
	 * @param source which pass answered
	 * @param via how it got there - the {@code w:altName} chain, the class, Word's
	 *        default family - or null where the source says it all
	 * @param widthError what is known of the substitute's width error, or null
	 * @since 17.1.1
	 */
	protected FontDecision decide(String documentFont, FontDecision.Source source, String via, String widthError) {

		if (documentFont==null) return null;
		String key = documentFont.trim().toLowerCase(java.util.Locale.ROOT);
		FontDecision decision = new FontDecision(documentFont.trim(), source, via, widthError);
		decision.copyScriptChoicesFrom(decisions.get(key));
		decisions.put(key, decision);
		return decision;
	}

	/**
	 * Record that a script this document font is used for went to another face during the
	 * conversion - {@link RunFontSelector}'s coverage pass, and the per-script substitutes
	 * of {@code font-substitutes.xml}.  Keyed on (document font, coverage group), as the
	 * selector's own cache is.
	 *
	 * @param face the face the script was drawn in, or null where nothing installed
	 *        covers it
	 * @since 17.1.1
	 */
	public void recordScriptChoice(String documentFont, String coverageGroup, PhysicalFont face) {

		if (documentFont==null || documentFont.trim().length()==0 || coverageGroup==null) return;
		String key = documentFont.trim().toLowerCase(java.util.Locale.ROOT);
		FontDecision decision = decisions.get(key);
		if (decision==null) {
			// a font no pass met (the selector reaches fonts a run names directly)
			decision = decide(documentFont, get(documentFont)==null
					? FontDecision.Source.UNMAPPED : FontDecision.Source.INSTALLED, null, null);
		}
		String faceName = face==null ? null : face.getName();
		FontSubstitutionTable.Substitute measured = faceName==null ? null
				: FontSubstitutionTable.scriptSubstitute(documentFont, coverageGroup, faceName);
		decision.addScriptChoice(coverageGroup, faceName, measured==null ? null : measured.getError());
	}

	/**
	 * Record that {@link RunFontSelector} drew a symbol font's characters in this face -
	 * the one {@link PhysicalFonts#getWDingsFont} or {@link PhysicalFonts#getSymbolFont}
	 * picks for the glyphs, whatever this mapper made of the name, since a {@code w:sym}
	 * character is a code point in that face and not in the font the run asks for.
	 *
	 * @since 17.1.1
	 */
	public void recordSymbolFace(String documentFont, PhysicalFont face) {

		if (documentFont==null || documentFont.trim().length()==0 || face==null) return;
		String key = documentFont.trim().toLowerCase(java.util.Locale.ROOT);
		FontDecision existing = decisions.get(key);
		if (existing!=null && existing.getSource()==FontDecision.Source.SYMBOL
				&& face.equals(existing.getFixedPhysical())) {
			return; // said once, not once per run
		}
		FontDecision decision = decide(documentFont, FontDecision.Source.SYMBOL,
				"a symbol-font run, drawn in the face which has the glyphs", null);
		if (decision!=null) decision.setFixedPhysical(face);
	}

	/**
	 * What was decided for each of this document's fonts, and why, ordered by the
	 * document font's name so that two runs of the same document give the same list.
	 *
	 * <p>The physical font, its faces and the line box are read off this mapper now, not
	 * when the pass recorded its decision, so they are what the conversion will use.</p>
	 *
	 * @since 17.1.1
	 */
	public java.util.List<FontDecision> getDecisions() {

		java.util.List<FontDecision> all = new java.util.ArrayList<FontDecision>(decisions.values());
		for (FontDecision decision : all) complete(decision);
		java.util.Collections.sort(all, new java.util.Comparator<FontDecision>() {
			public int compare(FontDecision a, FontDecision b) {
				return String.CASE_INSENSITIVE_ORDER.compare(a.getDocumentFont(), b.getDocumentFont());
			}
		});
		return java.util.Collections.unmodifiableList(all);
	}

	/** What was decided for this document font, recording one where no pass met it (a
	 *  font the selector reached directly): {@link FontsAnalysis} asks this, so that
	 *  every font a report is about has a decision.  @since 17.1.1 */
	FontDecision decisionFor(String documentFont) {
		FontDecision decision = getDecision(documentFont);
		if (decision==null) {
			decide(documentFont, get(documentFont)==null
					? FontDecision.Source.UNMAPPED : FontDecision.Source.INSTALLED, null, null);
			decision = getDecision(documentFont);
		}
		return decision;
	}

	/** What was decided for this document font, or null where no pass met it.
	 *  @since 17.1.1 */
	public FontDecision getDecision(String documentFont) {
		if (documentFont==null) return null;
		FontDecision decision = decisions.get(documentFont.trim().toLowerCase(java.util.Locale.ROOT));
		if (decision!=null) complete(decision);
		return decision;
	}

	/** The volatile half of a decision: the mapping as it now stands. */
	private void complete(FontDecision decision) {

		String documentFont = decision.getDocumentFont();
		PhysicalFont pf = decision.getFixedPhysical()!=null ? decision.getFixedPhysical() : get(documentFont);
		String bold = null, italic = null, boldItalic = null;
		double factor = 1;
		if (pf!=null) {
			bold = pf.isNoBoldFace() ? SYNTHETIC : faceName(getBoldForm(documentFont, pf));
			italic = faceName(getItalicForm(documentFont, pf));
			boldItalic = faceName(getBoldItalicForm(documentFont, pf));
			factor = WidthFactors.factorFor(documentFont, pf.getName());
		}
		decision.complete(pf, bold, italic, boldItalic, lineBox(documentFont), factor);
	}

	/** FOP synthesises the face from the regular one, which is what Word does for a
	 *  family that has none of its own. */
	public static final String SYNTHETIC = "synthetic";

	private static String faceName(PhysicalFont pf) {
		return pf==null ? SYNTHETIC : pf.getName();
	}

	/** Whose metrics the line box takes; see {@link FontDecision#getLineBox}. */
	private String lineBox(String documentFont) {
		String key = documentFont.trim().toLowerCase(java.util.Locale.ROOT);
		String family = lineMetricsFamily(documentFont);
		if (family!=null && !family.equalsIgnoreCase(documentFont)) {
			return (wordDefaulted.contains(key) ? "wordDefault:" : "alias:") + family;
		}
		return WordLineMetrics.hasTableEntry(documentFont) ? "documentFont" : "substitute";
	}

	public final static String FONT_FALLBACK = "Times New Roman";
	
	/**
	 * Populate the fontMappings object: an entry for each of the documentFontNames.
	 *
	 * <p>One order of precedence for every mapper (CR-016 phase 3; until 17.1.1 the two
	 * mappers disagreed, one preferring an installed font to the document's embedded one
	 * and the other the reverse):</p>
	 * <ol>
	 * <li>the font itself where the machine has it, by its name;</li>
	 * <li>the document's embedded form of it (regular, bold, italic, bold-italic, in that
	 *     order);</li>
	 * <li>the mapper's own answer, {@link #resolveDocumentFont} - a variant of the name
	 *     for {@link IdentityPlusMapper}, a panose match or a FontSubstitutions.xml entry
	 *     for {@link BestMatchingMapper}.</li>
	 * </ol>
	 * <p>What is still unmapped is then the business of the passes
	 * {@link WordprocessingMLPackage#setFontMapper} runs after this, in this order:
	 * {@link #addMetricallyCompatibleSubstitutes()}, {@link #addAltNameSubstitutes} (the
	 * document's own {@code w:altName}), {@link #addClassBasedSubstitutes} (a face of the
	 * same class), {@link #addMapperSubstitutes} (the mapper's own guesses),
	 * {@link #addWordDefaultSubstitutes} (what Word itself shows for a font it cannot find)
	 * and last {@link #addNoBoldFaceAliases}, which re-maps a family which has no bold face
	 * of its own.</p>
	 *
	 * @param documentFontNames - the fonts used in the document
	 * @param wmlFonts - the content model for the fonts part
	 * @throws Exception where the mapper cannot be populated
	 */
	public void populateFontMappings(Set<String> documentFontNames,
			org.docx4j.wml.Fonts wmlFonts ) throws Exception {

		Map<String, org.docx4j.wml.Fonts.Font> table = fontTable(wmlFonts);
		for (String documentFontName : documentFontNames) {
			if (documentFontName==null || documentFontName.trim().length()==0) continue;
			if (get(documentFontName)!=null) continue; // already mapped; its decision stands
			PhysicalFont pf = installedOrEmbedded(documentFontName);
			FontDecision.Source source = pf==null ? null
					: (PhysicalFonts.get(documentFontName)!=null ? FontDecision.Source.INSTALLED
							: FontDecision.Source.EMBEDDED);
			if (pf==null) {
				pf = resolveDocumentFont(documentFontName, table.get(documentFontName.trim().toLowerCase()));
				if (pf!=null) source = FontDecision.Source.MAPPER_OWN;
			}
			if (pf==null) {
				log.warn("- - No physical font for: " + documentFontName + " so ensure it is mapped. ");
				/* Recorded, not merely logged: a later pass will overwrite this decision
				 * where it maps the font, and what is left says which fonts reached FOP
				 * with nothing (CR-017 phase 1). */
				decide(documentFontName, FontDecision.Source.UNMAPPED, null, null);
			} else {
				put(documentFontName, pf);
				decide(documentFontName, source,
						source==FontDecision.Source.MAPPER_OWN
								? (resolvedVia!=null ? resolvedVia : "the mapper's own answer: " + pf.getName())
								: null,
						null);
			}
		}
	}

	/** The installed font of this name, else the document's embedded form of it. */
	protected PhysicalFont installedOrEmbedded(String documentFontName) {
		PhysicalFont pf = PhysicalFonts.get(documentFontName);
		if (pf!=null) return pf;
		pf = regularForms.get(documentFontName);
		if (pf==null) pf = boldForms.get(documentFontName);
		if (pf==null) pf = italicForms.get(documentFontName);
		if (pf==null) pf = boldItalicForms.get(documentFontName);
		return pf;
	}

	/**
	 * The mapper's own answer for a document font the machine has neither installed
	 * under that name nor embedded; null where it has none.
	 *
	 * @param documentFontName the name as the document writes it
	 * @param fontTableEntry the document's w:font entry for it, or null
	 * @since 17.1.1
	 */
	protected PhysicalFont resolveDocumentFont(String documentFontName, org.docx4j.wml.Fonts.Font fontTableEntry) {
		return null;
	}

	/** How the mapper's own answer was reached - a variant of the name, a panose match,
	 *  a FontSubstitutions.xml entry - for the decision the caller records; a subclass
	 *  sets it as it answers, and it is read straight afterwards.  @since 17.1.1 */
	protected String resolvedVia;

	/**
	 * The mapper's own substitutes for whatever is still unmapped after the shared passes
	 * that follow measured tables (the metric clones, w:altName, a face of the same class)
	 * and before Word's default: {@link BestMatchingMapper}'s panose match and
	 * FontSubstitutions.xml.  A guess from panose is worth less than a measured clone,
	 * so it comes after them (until 17.1.1 it came first, and a legacy Indic face with
	 * Arial's panose took Myriad and CorpoS away from Arimo and Carlito).  Nothing here
	 * by default.
	 *
	 * @since 17.1.1
	 */
	public void addMapperSubstitutes(Set<String> documentFontNames, org.docx4j.wml.Fonts wmlFonts) {
	}

	/** The font table keyed by lower-cased name. */
	static Map<String, org.docx4j.wml.Fonts.Font> fontTable(org.docx4j.wml.Fonts wmlFonts) {
		Map<String, org.docx4j.wml.Fonts.Font> table = new java.util.HashMap<String, org.docx4j.wml.Fonts.Font>();
		if (wmlFonts==null || wmlFonts.getFont()==null) return table;
		for (org.docx4j.wml.Fonts.Font f : wmlFonts.getFont()) {
			if (f!=null && f.getName()!=null) table.put(f.getName().trim().toLowerCase(), f);
		}
		return table;
	}
	
	
	// For Xalan
	/** @deprecated since 17.1.1: no pathway calls it; the HTML font-family is
	 *  RunFontSelector's (the document font, the physical family, the generic class). */
	@Deprecated
	public static String getSubstituteFontXsltExtension(Mapper s, String documentStyleId, String bolditalic, boolean fontFamilyStack) {
		
		return s.getSubstituteFontXsltExtension(documentStyleId, bolditalic, fontFamilyStack);
	}
	
	/** @deprecated since 17.1.1: see the static form. */
	@Deprecated
	public String getSubstituteFontXsltExtension(String documentStyleId, 
			String bolditalic, boolean fontFamilyStack) {
		
		log.debug("Trying to insert HTML font-family value for " + documentStyleId);
				
		if (documentStyleId==null) {
			log.error("passed null documentStyleId");
			return "nullInputToExtension";
		}

		
		
		PhysicalFont physicalFont = get((documentStyleId));
		if (physicalFont==null) {

			log.error("No mapping for: " + documentStyleId);
			return Mapper.FONT_FALLBACK;
		} else {

			// iTextFontResolver wants a font family name
			// Until such time as we get this from FOP,
			// use the following heuristic..
			
			String fontFamily = physicalFont.getName();
			
			if (fontFamily.startsWith("Britannic")) { // special case
				return fontFamily;
			}
			if (fontFamily.endsWith(" Demibold" ) ) {
				fontFamily = fontFamily.substring(0, fontFamily.length() - 9);
			}
			if (fontFamily.endsWith(" Oblique" ) ) {
				fontFamily = fontFamily.substring(0, fontFamily.length() - 8);
			}
			if (fontFamily.endsWith(" Italic" ) ) {
				fontFamily = fontFamily.substring(0, fontFamily.length() - 7);
			}
			if (fontFamily.endsWith(" Bold" ) ) {
				fontFamily = fontFamily.substring(0, fontFamily.length() - 5);
			}
			// NB, in that order, it handles " Bold Italic" and "Bold Oblique" as well.
			log.debug("Mapping " + documentStyleId + " to " + physicalFont.getName());
			
			/* On my Windows box, the following are passed
			 * to ITextFontResolver, but still not found in its
			 * _fontFamilies map:
			 * 
			 *      DejaVu Sans ExtraLight
			 *      Lucida Sans Demibold
			 *      Lucida Sans Regular
			 *      Lucida Bright Demibold
			 *      Lucida Sans Demibold Roman
			 *      Lucida Fax Regular
			 *      Lucida Fax Demibold
			 */
			
			return fontFamily;

		}

		/*
		 * We want to return eg "Times New Roman" 
		 * or "Arial Unicode MS" here, ie _with spaces_, since that is 
		 * what xhtmlrender's org.xhtmlrenderer.pdf.ITextFontResolver sets up.
		 * 
		 * 
		 */
		
	}

	/** Does the document embed this font (in any of its forms)?
	 *
	 * @since 17.0.3
	 */
	public boolean isEmbedded(String fontNameAsInFontTablePart) {
		return regularForms.get(fontNameAsInFontTablePart)!=null
				|| boldForms.get(fontNameAsInFontTablePart)!=null
				|| italicForms.get(fontNameAsInFontTablePart)!=null
				|| boldItalicForms.get(fontNameAsInFontTablePart)!=null;
	}

	public void registerRegularForm(String fontNameAsInFontTablePart, PhysicalFont pfRegular) {
		if (pfRegular == null) {
			regularForms.remove(fontNameAsInFontTablePart);
		} else {
			regularForms.put(fontNameAsInFontTablePart, pfRegular);
		}
	}
	
	public void registerBoldForm(String fontNameAsInFontTablePart, PhysicalFont pfBold) {
		if (pfBold == null) {
			boldForms.remove(fontNameAsInFontTablePart);
		} else {
			boldForms.put(fontNameAsInFontTablePart, pfBold);
		}
	}

	public void registerItalicForm(String fontNameAsInFontTablePart, PhysicalFont pfItalic) {
		if (pfItalic == null) {
			italicForms.remove(fontNameAsInFontTablePart);
		} else {
			italicForms.put(fontNameAsInFontTablePart, pfItalic);
		}
	}

	public void registerBoldItalicForm(String fontNameAsInFontTablePart, PhysicalFont pfBoldItalic) {
		if (pfBoldItalic == null) {
			boldItalicForms.remove(fontNameAsInFontTablePart);
		} else {
			boldItalicForms.put(fontNameAsInFontTablePart, pfBoldItalic);
		}
	}

	// The following methods are used in FopConfigUtil
	
	public PhysicalFont getRegularForm(String fontNameAsInFontTablePart) {
		final PhysicalFont pfRegular = PhysicalFonts.get(fontNameAsInFontTablePart);
		return (pfRegular != null) ? pfRegular : regularForms.get(fontNameAsInFontTablePart);
	}
	
	public PhysicalFont getBoldForm(String fontNameAsInFontTablePart, PhysicalFont pf) {
		if (pf==null) return boldForms.get(fontNameAsInFontTablePart); // for where eg Cambria-bold was embedded, but Cambria is not present
		final PhysicalFont pfBold = PhysicalFonts.getBoldForm(pf); // prefer the physical font if present on the system (this potentially helps if we need a glyph which is not embedded)
		return (pfBold != null) ? pfBold : boldForms.get(fontNameAsInFontTablePart); // otherwise, look for embedded
		// The installed font wins over the embedded one, for all four faces and in both
		// mappers: CR-016 Decisions 2, which settled the question this comment used to ask.
	}
	
	public PhysicalFont getItalicForm(String fontNameAsInFontTablePart, PhysicalFont pf) {
		if (pf==null) return italicForms.get(fontNameAsInFontTablePart);
		final PhysicalFont pfItalic = PhysicalFonts.getItalicForm(pf);
		return (pfItalic != null) ? pfItalic : italicForms.get(fontNameAsInFontTablePart);
	}

	public PhysicalFont getBoldItalicForm(String fontNameAsInFontTablePart, PhysicalFont pf) {
		if (pf==null) return boldItalicForms.get(fontNameAsInFontTablePart);
		final PhysicalFont pfBoldItalic = PhysicalFonts.getBoldItalicForm(pf);
		return (pfBoldItalic != null) ? pfBoldItalic : boldItalicForms.get(fontNameAsInFontTablePart);
	}
	
    /**
     * Auto-add mappings for Calibri, Cambria etc where possible and useful: for each
     * document font the table knows, the first of its open substitutes this machine has.
     *
     * <p>The table itself is {@code font-substitutes.xml} since 17.1.1
     * ({@link FontSubstitutionTable}, CR-017 phase 0), so that a report - and a port -
     * can read the same rows this pass takes; it carries the same document fonts, in the
     * same order, with the same substitutes.  <b>The measurements which chose each
     * substitute stay here</b>, below, in the table's order: they are the record of why a
     * row is what it is, and the table's {@code error} attributes quote them.</p>
     *
     * @since 11.5.9
     */
    public void addMetricallyCompatibleSubstitutes() {

    	for (FontSubstitutionTable.Row row : FontSubstitutionTable.substitutes()) {
    		PhysicalFont before = get(row.getDocumentFont());
    		addFirstAvailableSubstitute(row.getDocumentFont(), row.substituteNames());
    		PhysicalFont after = get(row.getDocumentFont());
    		if (after!=null && after!=before) {
    			// which candidate it took: the first the machine has, as the pass chooses
    			FontSubstitutionTable.Substitute taken = null;
    			for (String candidate : row.substituteNames()) {
    				if (PhysicalFonts.get(candidate)!=null) { taken = row.substituteNamed(candidate); break; }
    			}
    			decide(row.getDocumentFont(), sourceOf(taken), null,
    					taken==null ? null : (taken.getError()!=null ? taken.getError()
    							: ("class".equals(taken.getQuality()) ? UNKNOWN_ERROR : null)));
    		}
    	}

    	/* ---------------------------------------------------------------------------
    	 * The measurements behind the rows, in the table's order.
    	 * ---------------------------------------------------------------------------
    	 *
    	 * Times New Roman, Arial, Courier New - Croscore or Liberation.  NB Times New
    	 * Roman is a serif and Arial a sans: the two second substitutes were the wrong way
    	 * round until 17.0.5, so on a box with Liberation but not Croscore each became the
    	 * other's class.
    	 *
    	 * Calibri, Cambria, Calibri Light - Crosextra.  Second choice where the crosextra
    	 * clones are absent (a box with only the Liberation jar, e.g. a build server): a
    	 * font of the same class, so the text is at least a sans / a serif; line heights
    	 * still come from the document font's own metrics (WordLineMetrics).  @since 17.0.5
    	 *
    	 * Century Gothic - URW base 35 (ghostscript-fonts, on most Linux boxes).  Century
    	 * Gothic was drawn to ITC Avant Garde Gothic's widths, and URW Gothic is the Avant
    	 * Garde clone: measured against the Century Gothic Word embedded in a real
    	 * document, URW Gothic Book matches it to the unit over 6743 characters (0.00%),
    	 * and URW Gothic Demi likewise matches Century Gothic Bold.  Without this the
    	 * class-based fallback reached a Helvetica clone, 3.1% wider, which is enough to
    	 * break a full line differently.  @since 17.0.5
    	 *
    	 * Tahoma, Segoe UI, Gadugi, Helvetica, Helvetica Neue, Tw Cen MT - fonts with no
    	 * metric-compatible clone, but where a stand-in of the right class is much closer
    	 * than the document's default font, which is what RunFontSelector falls back to (a
    	 * sans in Tinos, or Georgian in Carlito, was the first divergence in a fifth of a
    	 * real-document sample; CR-001).  The widths are not Word's, so lines still break
    	 * differently.  @since 17.0.5
    	 * Tw Cen MT (Twentieth Century) is a geometric sans; without an entry it fell
    	 * through to the serif default and its labels came out 3-4% narrow (measured
    	 * against Word's own PDF of a corpus document, every y and x within 0.3pt and the
    	 * text 1.03-1.04x ours).  Arimo is 1.0605x Tinos, so the residual is 2%.
    	 * @since 17.1.0
    	 *
    	 * Trebuchet MS is not an Arial shape and an Arial clone is wrong for it in two
    	 * directions at once: its lower case is wider than Arial's and its capitals are
    	 * much narrower.  Measured against Word's own PDFs of three corpus documents, on
    	 * unjustified lines whose text matches exactly (Word's pen advance over the
    	 * candidate's advance for the same string at the same size), Word / Arimo is
    	 * 1.0273 over the mixed-case body but 0.9061 over the bold capitals of the
    	 * headings, so no single width factor can repair it - scaling Arimo to fit the
    	 * body makes the headings worse.  Droid Sans is the closest installed face in
    	 * every weight: 1.0096 body, 0.9449 bold capitals, 1.0246 italic, against Arimo's
    	 * 1.0273 / 0.9061 / 1.0504; on the two other Trebuchet documents its body ratio
    	 * is 1.0060 and 0.9889 where Arimo is 1.0212 and 0.9918, and on their all-capitals
    	 * lines 0.9703 / 1.0272 where Arimo is 0.8942 / 0.8822.  Noto Sans was measured
    	 * and rejected: 0.9678 body, 0.9133 bold capitals, 1.0382 italic - further from
    	 * Trebuchet than Arimo is on two of the three documents.  Droid Sans ships no
    	 * italic face, so FOP obliques the regular, whose advances are the ones measured
    	 * above; the line box stays Trebuchet's own (WordLineMetrics has it).  Arimo
    	 * remains the last resort, which is what a machine without Droid Sans keeps.
    	 * @since 17.1.1
    	 *
    	 * Arial Black is far heavier and wider than Arial: measured against Word's own
    	 * PDF of a corpus document, its centred title is 281.2pt against our Arimo's
    	 * 247.9 on the same centre - 1.134x.  Noto Sans Black measures 1.1122x Arimo
    	 * over a mixed Latin sample, so the residual is 2% instead of 13.4%.  Arimo
    	 * remains the last resort.  @since 17.1.0
    	 *
    	 * Verdana and Comic Sans MS are much wider than Arial, so an Arial clone
    	 * re-breaks every line of a document set in them.  Measured against Word's own
    	 * PDFs of real documents, on lines whose text matches exactly: Word's Verdana
    	 * lines are 1.141x our Arimo ones, and DejaVu Sans is 1.14x Arimo over a mixed
    	 * Latin sample; Word's Comic Sans lines are 1.153x our Carlito ones (Comic Sans
    	 * reached Carlito through the class-based fallback), and Noto Sans is 1.15x
    	 * Carlito.  Tahoma is left where it is: on an all-Tahoma document the median
    	 * ratio to our Arimo output is 1.006.  @since 17.1.0
    	 *
    	 * Segoe UI Light has no metric clone, but Arimo is the wrong shape for it:
    	 * measured against the Segoe UI Light Word embeds, Arimo's advances are
    	 * systematically 11.8% wider, so every line breaks early.  Source Sans has
    	 * no systematic bias at all (+0.4% mean signed, 9.4% mean absolute), which is
    	 * what line breaking cares about.  Arimo remains the last resort.  @since 17.0.5
    	 *
    	 * Held back: the Nokia Pure family, the only document font name of the three
    	 * corpora's 449 documents which reaches FOP unresolved.  Its w:altName is Meiryo -
    	 * itself absent, so the alt-name pass cannot resolve it - and its name matches none
    	 * of FontFallback's class keywords, so it gets no class default either; because the
    	 * document's own theme names Nokia Pure Text Light, even the last-resort document
    	 * default is circular.  FOP renders an unknown family in its base-14 Times, so the
    	 * whole of that document (a 26pt title, 85 table headings, 5253 TOC leader dots)
    	 * comes out in a non-embedded serif where the face is a humanist sans
    	 * (w:family="swiss", panose serif-style 11).
    	 *
    	 * A row "Nokia Pure Text" -> Source Sans 3, Source Sans Pro, Arimo Regular,
    	 * Liberation Sans - Segoe UI Light's substitutes, for the same reasons - was
    	 * measured: it draws that document in the right class and moves its page count
    	 * towards Word's (63 of Word's 87 to 65), and its body lines are closer (Word's
    	 * "Acceptance Test Manual" is 266.9pt, base-14 Times 274.4, Source Sans 269.9),
    	 * but its headings are further out (115.2 against 120.6 and 104.6) and it cost
    	 * that document 0.045 of line parity, which is the whole of the batch's fall on
    	 * that corpus.  Without a measurement of Nokia Pure's own advances there is
    	 * nothing to choose the substitute by, so it waits for one.  @since 17.1.0
    	 *
    	 * Georgia, Book Antiqua, Palatino Linotype - the Palatino family, and Georgia, are
    	 * wider than Times, so a Times clone re-breaks their lines.  P052 is URW's
    	 * Palladio, the Palatino clone, and is in the URW base 35 (ghostscript-fonts).
    	 * Measured against Word's own PDFs: Word's Book Antiqua lines are 1.087-1.114x our
    	 * Tinos ones and its Georgia lines 1.076-1.112x, where P052 is 1.09x Tinos over a
    	 * mixed Latin sample.  @since 17.1.0
    	 *
    	 * Arial Narrow - Liberation Sans Narrow is metric-compatible with it, but neither
    	 * the Liberation nor the Croscore jar carries it, and it is no longer in the
    	 * Liberation package.  Nimbus Sans Narrow (URW's Helvetica Narrow, in
    	 * ghostscript-fonts) is the same 82% condensation and matches Arial Narrow's
    	 * advances to within one unit per 1000 over letters, digits and punctuation
    	 * (0.02% mean, bold likewise; the control pair Century Gothic / URW Gothic
    	 * measures 0.29% by the same method).  Where neither is installed, Arial Narrow
    	 * is still deliberately left unmapped: measured over the real-document corpus,
    	 * DejaVu Sans Condensed (the nearest condensed face on a typical Linux box) is
    	 * further from Arial Narrow than the document default is, and substituting it
    	 * cost line parity on three documents.
    	 *
    	 * Consolas, Lucida Console - monospace fonts with no metric-compatible clone: a
    	 * monospace stand-in keeps code aligned, where the default (proportional) fallback
    	 * would not.  Widths differ (Consolas advances 0.55em, Cousine and Liberation Mono
    	 * 0.6em); line heights come from the document font's own metrics
    	 * (WordLineMetrics).  @since 17.0.5
    	 */
    }
    
    /** What the report says where nothing was measured: a face chosen on class alone is
     *  chosen because nothing closer exists, and no number describes how far off it is.
     *  @since 17.1.1 */
    public static final String UNKNOWN_ERROR = "unknown";

    /** The source a table row's quality word says.  @since 17.1.1 */
    private static FontDecision.Source sourceOf(FontSubstitutionTable.Substitute substitute) {
    	if (substitute==null) return FontDecision.Source.CLASS;
    	if ("metric".equals(substitute.getQuality())) return FontDecision.Source.METRIC_CLONE;
    	if ("measured".equals(substitute.getQuality())) return FontDecision.Source.MEASURED_STAND_IN;
    	return FontDecision.Source.CLASS;
    }

    /** Whether {@link #addClassBasedSubstitutes} applies to this mapper.  True for both
     *  since 17.1.1 (CR-016 phase 3): BestMatchingMapper's own step runs first, and what
     *  it leaves unmapped goes through the same passes as IdentityPlusMapper's.
     *  @since 17.0.5 */
    public boolean wantsClassBasedSubstitutes() {
    	return true;
    }

    /**
     * {@code w:altName} in {@code word/fontTable.xml} (ECMA-376 17.8.3.1): "the name of
     * an alternate font which shall be used if the font specified is not available".
     * That is the document author's own answer to a missing font, and Word takes it -
     * measured on a document whose Normal style is {@code HelveticaNeue LT 55 Roman}
     * with {@code &lt;w:altName w:val="Times New Roman"/&gt;}, where Word's PDF embeds
     * TimesNewRomanPSMT while docx4j reached the theme's minorHAnsi (Arial) 2194 times,
     * making our lines 1.072 x Word's over 140 matched lines and re-breaking them.
     *
     * <p>Consulted after {@link #addMetricallyCompatibleSubstitutes()} - a metric clone
     * of the document's own font is a better answer than any alias - and before
     * {@link #addClassBasedSubstitutes(Set)}, which is only a guess from the name.  A
     * font the machine has, or the document embeds, keeps itself.</p>
     *
     * <p>The alternate name is resolved the way any document font is: the physical font
     * of that name if the machine has it, else whatever this mapper has already mapped
     * it to (its metric clone).  Where it resolves, the alternate is also registered
     * with {@link WordLineMetrics} as this font's alias, since Word takes the line
     * metrics of the font it actually uses (&#xa7;2.7).</p>
     *
     * @param documentFontNames the fonts the document uses; null for every font in the table
     * @param wmlFonts the font table part's content
     * @since 17.1.0
     */
    public void addAltNameSubstitutes(Set<String> documentFontNames, org.docx4j.wml.Fonts wmlFonts) {

    	if (wmlFonts==null || wmlFonts.getFont()==null) return;

    	// the table is keyed on the name as the document writes it; match case-insensitively
    	Map<String, String> altNames = new java.util.HashMap<String, String>();
    	for (org.docx4j.wml.Fonts.Font font : wmlFonts.getFont()) {
    		if (font==null || font.getName()==null || font.getAltName()==null) continue;
    		String alt = font.getAltName().getVal();
    		if (alt==null || alt.trim().length()==0) continue;
    		altNames.put(font.getName().trim().toLowerCase(), alt.trim());
    	}
    	if (altNames.isEmpty()) return;

    	java.util.Collection<String> names = documentFontNames!=null ? documentFontNames : altNames.keySet();
    	for (String documentFontName : names) {

    		if (documentFontName==null || documentFontName.trim().length()==0) continue;
    		if (get(documentFontName)!=null) continue;             // already mapped
    		if (isEmbedded(documentFontName)) continue;
    		if (PhysicalFonts.get(documentFontName)!=null) continue; // installed; identity

    		/* Follow the chain: the alternate may itself be absent and name an alternate
    		 * (CR-016 probe fonts-unresolvable (e), (f)); a few hops, never a cycle. */
    		String alt = altNames.get(documentFontName.trim().toLowerCase());
    		java.util.Set<String> seen = new java.util.HashSet<String>();
    		seen.add(documentFontName.trim().toLowerCase());
    		PhysicalFont pf = null;
    		String resolvedAlt = null;
    		StringBuilder chain = new StringBuilder("w:altName "); // hop by hop, for the decision
    		while (alt!=null && seen.add(alt.trim().toLowerCase())) {
    			if (chain.length()>10) chain.append(" -> ");
    			chain.append(alt);
    			pf = PhysicalFonts.get(alt);
    			if (pf==null) pf = PhysicalFonts.get(alt + " Regular");
    			if (pf==null) pf = get(alt);
    			if (pf!=null) { resolvedAlt = alt; break; }
    			alt = altNames.get(alt.trim().toLowerCase());
    		}
    		if (pf==null) continue;

    		put(documentFontName, pf);
    		registerLineMetricsAlias(documentFontName, resolvedAlt);
    		decide(documentFontName, FontDecision.Source.ALT_NAME, chain.toString(), null);
    	}
    }

    /**
     * Map whatever is still unmapped after {@link #addMetricallyCompatibleSubstitutes()}
     * to a font of the same class.
     *
     * <p>Without this, an unmapped font falls back to whatever the document's *default*
     * font maps to, which is a Times clone standing in for a sans as often as not
     * (CR-001 cause C3).  Doing it here, rather than only in RunFontSelector, means the
     * chosen font is declared to FOP with the rest.  Glyph coverage is a separate
     * matter, settled per script segment during the conversion; see
     * {@link FontFallback#selectCovering}.</p>
     *
     * <p>The classes and the candidate lists come from FontSubstitutions.xml, which
     * {@link BestMatchingMapper} already consults.  Both mappers take this pass since
     * 17.1.1 (CR-016 phase 3; it was IdentityPlusMapper's alone when it was added in
     * 17.0.5), BestMatchingMapper's own panose step having moved behind it - see
     * {@link #wantsClassBasedSubstitutes}.</p>
     *
     * <p>Deliberately conservative: a condensed face (Arial Narrow) is left unmapped,
     * since measured over a real-document corpus the ordinary condensed faces a Linux
     * box has are further from its widths than the document default is.</p>
     *
     * @param documentFontNames the fonts the document uses
     * @since 17.0.5
     */
    public void addClassBasedSubstitutes(Set<String> documentFontNames) {

    	if (documentFontNames==null) return;
    	for (String documentFontName : documentFontNames) {

    		if (documentFontName==null || documentFontName.trim().length()==0) continue;
    		if (get(documentFontName)!=null) continue; // already mapped
    		if (isEmbedded(documentFontName)) continue;
    		if (PhysicalFonts.get(documentFontName)!=null) continue; // installed; identity

    		PhysicalFont pf = FontFallback.selectByClass(documentFontName);
    		if (pf!=null) {
    			put(documentFontName, pf);
    			decide(documentFontName, FontDecision.Source.CLASS,
    					"a face of the same class: " + FontFallback.substitutionClass(documentFontName),
    					UNKNOWN_ERROR);
    		}
    	}
    }

    /**
     * Word's own answer for a font it cannot find, for whatever is still unmapped after
     * every other pass: measured (CR-016 probe fonts-unresolvable, Word 365), a face
     * whose fontTable entry says {@code w:family="roman"} is drawn in <b>Cambria</b>, one
     * saying {@code swiss} in <b>Calibri</b>, one with an entry that names no family in
     * Calibri, one with no fontTable entry at all in Cambria; an exact panose does not
     * change that (Arial's gave Calibri), and the document's default font is never used.
     * A {@code w:altName} that resolves has already won ({@link #addAltNameSubstitutes});
     * one that does not is looked through for its family.
     *
     * <p>Applied only to a family none of docx4j's tables know ({@link #isKnownFamily}):
     * a real Microsoft or common font the machine merely lacks is closer to Word's output
     * in a face of its own class (the passes before this one), since Word on the author's
     * machine had the font; a name no table knows is one Word itself substituted.  The
     * face is whatever this mapper maps Cambria or Calibri to (their metric clones where
     * installed), and {@link WordLineMetrics} is told the alias, so the line box is
     * Cambria's or Calibri's, as Word's is.</p>
     *
     * @since 17.1.1
     */
    public void addWordDefaultSubstitutes(Set<String> documentFontNames, org.docx4j.wml.Fonts wmlFonts) {

    	if (documentFontNames==null) return;
    	Map<String, org.docx4j.wml.Fonts.Font> table = fontTable(wmlFonts);
    	for (String documentFontName : documentFontNames) {

    		if (documentFontName==null || documentFontName.trim().length()==0) continue;
    		if (get(documentFontName)!=null) continue;
    		if (isEmbedded(documentFontName)) continue;
    		if (PhysicalFonts.get(documentFontName)!=null) continue;
    		if (isKnownFamily(documentFontName)) continue;

    		String wordFont = wordDefaultFor(documentFontName, table);
    		PhysicalFont pf = get(wordFont);
    		if (pf==null) pf = PhysicalFonts.get(wordFont);
    		if (pf==null) pf = FontFallback.selectByClass(wordFont);
    		if (pf==null) continue;
    		put(documentFontName, pf);
    		wordDefaulted.add(documentFontName.trim().toLowerCase());
    		registerLineMetricsAlias(documentFontName, wordFont);
    		decide(documentFontName, FontDecision.Source.WORD_DEFAULT,
    				"Word's own default for a font it cannot find: " + wordFont, UNKNOWN_ERROR);
    	}
    }

    /** The document fonts {@link #addWordDefaultSubstitutes} mapped, lower-cased: Word
     *  substitutes such a font whole, bold face included, so {@link #addNoBoldFaceAliases}
     *  leaves them alone.  @since 17.1.1 */
    private final java.util.Set<String> wordDefaulted = ConcurrentHashMap.newKeySet();

    /** The font Word draws an unknown font in, by its fontTable entry (see
     *  {@link #addWordDefaultSubstitutes}). */
    static String wordDefaultFor(String documentFontName, Map<String, org.docx4j.wml.Fonts.Font> table) {
    	org.docx4j.wml.Fonts.Font entry = table.get(documentFontName.trim().toLowerCase());
    	if (entry==null) return "Cambria";
    	// the family: the entry's own, else the first along its altName chain
    	java.util.Set<String> seen = new java.util.HashSet<String>();
    	org.docx4j.wml.Fonts.Font e = entry;
    	while (e!=null && e.getName()!=null && seen.add(e.getName().trim().toLowerCase())) {
    		if (e.getFamily()!=null && e.getFamily().getVal()!=null) {
    			String family = e.getFamily().getVal().trim().toLowerCase();
    			if (family.equals("roman")) return "Cambria";
    			if (family.equals("modern")) return "Courier New";
    			return "Calibri"; // swiss, script, decorative, auto
    		}
    		e = (e.getAltName()==null || e.getAltName().getVal()==null) ? null
    				: table.get(e.getAltName().getVal().trim().toLowerCase());
    	}
    	return "Calibri";
    }

    /**
     * Whether docx4j's tables know this family: MicrosoftFonts.xml, word-line-metrics
     * (512 Microsoft and Office cloud families), FontSubstitutions.xml, or the class
     * heuristic on its name.  A known family the machine lacks is substituted for its
     * widths; an unknown one is what Word could not find either.
     *
     * @since 17.1.1
     */
    public static boolean isKnownFamily(String documentFontName) {
    	if (documentFontName==null) return false;
    	String name = documentFontName.trim();
    	if (org.docx4j.fonts.microsoft.MicrosoftFontsRegistry.getMsFonts().containsKey(name)) return true;
    	// the table's own families, not an alias another document registered in this JVM
    	if (WordLineMetrics.isTableFamily(name)) return true;
    	if (FontFallback.classOf(name)!=FontFallback.FontClass.UNKNOWN) return true;
    	return false;
    }

    /**
     * Whether this document font has a bold face of its own: the MicrosoftFonts.xml
     * entry where there is one (Franklin Gothic Book has none), else a family whose name
     * ends in a weight word (Calibri Light, Segoe UI Semibold, Arial Black) is a single
     * weight in Windows' font model and has none; any other family is taken to have one.
     *
     * @since 17.1.1
     */
    public static boolean hasBoldFace(String documentFontName) {
    	if (documentFontName==null) return true;
    	String name = documentFontName.trim();
    	org.docx4j.fonts.microsoft.MicrosoftFonts.Font ms = org.docx4j.fonts.microsoft.MicrosoftFontsRegistry.getMsFonts().get(name);
    	if (ms!=null) return ms.getBold()!=null;
    	String last = name.toLowerCase();
    	int sp = last.lastIndexOf(' ');
    	if (sp>0) last = last.substring(sp+1);
    	for (String weight : new String[] { "light", "semilight", "semibold", "demibold", "medium", "black", "thin", "extralight", "ultralight", "heavy" }) {
    		if (last.equals(weight)) return false;
    	}
    	return true;
    }

    /**
     * For each document font which has no bold face of its own but whose mapped physical
     * font has a real bold sibling, an alias of that font reporting none
     * ({@link PhysicalFont#noBoldFaceAlias}), so that FOP synthesises the bold at the
     * regular advances as Word does (probe fonts-light-bold: Calibri Light's w:b is the
     * Calibri Light font object, +0.3% wide, where Carlito Bold is +2.7%).  Last of the
     * passes: it re-maps.
     *
     * @since 17.1.1
     */
    public void addNoBoldFaceAliases(Set<String> documentFontNames) {

    	if (documentFontNames==null) return;
    	for (String documentFontName : documentFontNames) {
    		if (documentFontName==null || documentFontName.trim().length()==0) continue;
    		if (hasBoldFace(documentFontName)) continue;
    		if (isEmbedded(documentFontName)) continue; // the embedded forms say what the document has
    		/* A font Word itself could not find is substituted whole - Calibri's real bold
    		 * for its w:b - however its name reads: "EnBW DIN Pro Light" is Calibri Bold in
    		 * Word, not a synthesised Calibri.  Measured (CR-016 phase 4 gate): with the
    		 * alias that document scored 0.8441, without it 0.8783.  The alias is for a font
    		 * Word has (or a clone of one) which has no bold face of its own. */
    		if (wordDefaulted.contains(documentFontName.trim().toLowerCase())) continue;
    		PhysicalFont pf = get(documentFontName);
    		if (pf==null || pf.isNoBoldFace()) continue;
    		if (PhysicalFonts.getBoldForm(pf)==null && boldForms.get(documentFontName)==null) continue; // nothing to withhold
    		PhysicalFont alias = pf.noBoldFaceAlias();
    		fontMappings.put(documentFontName.toLowerCase(), alias);
    		/* Not a source of its own: the pass re-maps the font to an alias of what an
    		 * earlier pass chose, and the decision says so by reporting a synthetic bold
    		 * face (FontDecision.getBoldFace).  @since 17.1.1 */
    	}
    }

    /**
     * The families whose Word line metrics this document's fonts take, keyed on the
     * lower-cased document font name: a font docx4j resolved through its {@code w:altName}
     * ({@link #addAltNameSubstitutes}) or through Word's answer for a font it cannot find
     * ({@link #addWordDefaultSubstitutes}) is a font <em>Word itself</em> is not using, so
     * its vertical metrics are the other family's (&#xa7;2.7).
     *
     * <p><b>Per conversion, and deliberately so.</b>  This lived in {@code WordLineMetrics}
     * as a static map until 17.1.1, where it was JVM-wide and never cleared, so one
     * document's {@code w:altName} answered for every later document in the same process
     * which named the same font: measured over the three real-document corpora, <b>93 of
     * 449 documents register at least one</b> (137 registrations, 116 distinct names), and
     * four documents were reading an alias another document had left behind.  A server
     * converting document after document is exactly the case that breaks, and the effect
     * is order-dependent, which makes it hard to see.  A {@code Mapper} is created per
     * package ({@code WordprocessingMLPackage.setFontMapper}), and both passes that
     * register are this class's, so the map belongs here.</p>
     *
     * @since 17.1.1
     */
    private final Map<String, String> lineMetricsAliases = new ConcurrentHashMap<String, String>();

    /**
     * Record that this document font takes its Word line metrics from another family.
     * Ignored where the document font has metrics of its own - then they are the ones
     * Word uses.
     *
     * @since 17.1.1
     */
    public void registerLineMetricsAlias(String documentFont, String family) {
    	if (documentFont==null || family==null) return;
    	String key = documentFont.trim().toLowerCase(java.util.Locale.ROOT);
    	String value = family.trim();
    	if (key.length()==0 || value.length()==0 || key.equals(value.toLowerCase(java.util.Locale.ROOT))) return;
    	if (WordLineMetrics.hasOwnLineMetrics(documentFont)) return; // it has its own
    	lineMetricsAliases.put(key, value);
    }

    /**
     * The family whose Word line metrics this document font takes: the alias where one was
     * registered, else the name itself.  Never null for a non-null argument, so the caller
     * can hand the answer straight to {@link WordLineMetrics}.
     *
     * @since 17.1.1
     */
    public String lineMetricsFamily(String documentFont) {
    	if (documentFont==null) return null;
    	String alias = lineMetricsAliases.get(documentFont.trim().toLowerCase(java.util.Locale.ROOT));
    	return alias==null ? documentFont : alias;
    }

    /** Physical fonts RunFontSelector chose as a last resort during conversion, which the
     *  FOP configuration therefore has to be told about late; see FopConfigUtil.
     *  @since 17.0.5 */
    private final Map<String, PhysicalFont> lastResortFallbacks = new ConcurrentHashMap<String, PhysicalFont>();

    /** @since 17.0.5 */
    public void registerLastResortFallback(PhysicalFont pf) {
    	if (pf!=null && pf.getName()!=null) lastResortFallbacks.putIfAbsent(pf.getName(), pf);
    }

    /** @since 17.0.5 */
    public Map<String, PhysicalFont> getLastResortFallbacks() {
    	return lastResortFallbacks;
    }

    /**
     * As {@link #addMetricallyCompatibleSubstitute(String, String, String)}, but
     * choosing the first of any number of candidates which is installed, best first.
     *
     * @since 17.0.5
     */
    protected void addFirstAvailableSubstitute(String proprietaryFont, String... openSubstitutes) {
    	for (String candidate : openSubstitutes) {
    		if (candidate!=null && PhysicalFonts.get(candidate)!=null) {
    			addMetricallyCompatibleSubstitute(proprietaryFont, candidate, null);
    			return;
    		}
    	}
    }

    /**
     * Map this document font to the first of the two open substitutes the machine has,
     * unless the machine has the font itself or the document embeds it.
     *
     * @param proprietaryFont the document font (Calibri, Times New Roman ...)
     * @param openSubstitute the metric-compatible clone to prefer (Carlito, Tinos ...)
     * @param openSubstitute2 a second choice, or null
     * @since 11.5.9
     */
    protected void addMetricallyCompatibleSubstitute(String proprietaryFont, String openSubstitute, String openSubstitute2) {
    	
    	if (isEmbedded(proprietaryFont)) {
    		/* The document embeds this font, so populateFontMappings will have mapped it
    		 * to that.  Don't replace it with a substitute: the embedded font is what the
    		 * author intended, and it is the only thing which is certain to be available.
    		 * NB this runs after populateFontMappings; see
    		 * WordprocessingMLPackage.setFontMapper.  The decision populateFontMappings
    		 * recorded (EMBEDDED) says so; nothing is logged here.
    		 * @since 17.0.3 */
    		return;
    	}

    	if (PhysicalFonts.get(proprietaryFont)==null) {
    		// what was mapped, and why, is the caller's decision to record; see
    		// addMetricallyCompatibleSubstitutes
    		if (PhysicalFonts.get(openSubstitute)!=null) {
	    		put(proprietaryFont, PhysicalFonts.get(openSubstitute));
    		} else if (openSubstitute2 !=null && PhysicalFonts.get(openSubstitute2)!=null) {
	    		put(proprietaryFont, PhysicalFonts.get(openSubstitute2));
    		}
    	}
    	
    }
	
}
