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
 * There are 2 implementations:
 * 
 * - IndentityPlusMapper, which is best
 *   where most of the fonts used in the 
 *   document are physically present
 *   on the system
 *   
 * - BestMatchingMapper, useful on
 *   Linux and OSX systems on which
 *   Microsoft fonts have not been 
 *   installed.
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
	
	@Deprecated // in order to avoid case sensitivity
	public Map<String, PhysicalFont> getFontMappings() {
		return fontMappings;
	}	
	
	/**
	 * Get a PhysicalFont from FontMappings, 
	 * by case-insensitive name.  (Although Word always
	 * uses Title Case for font names, it is actually
	 * case insensitive; the spec is silent on this.)  
	 * 
	 * @param key
	 * @return
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
	 * @param key
	 * @param pf
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
	 * <p>What is still unmapped is then the business of the shared passes
	 * {@link WordprocessingMLPackage#setFontMapper} runs after this: the metric-compatible
	 * table, the document's own {@code w:altName}, a face of the same class, and Word's own
	 * default for a font it cannot find.</p>
	 *
	 * @param documentFontNames - the fonts used in the document
	 * @param wmlFonts - the content model for the fonts part
	 * @throws Exception
	 */
	public void populateFontMappings(Set<String> documentFontNames,
			org.docx4j.wml.Fonts wmlFonts ) throws Exception {

		Map<String, org.docx4j.wml.Fonts.Font> table = fontTable(wmlFonts);
		for (String documentFontName : documentFontNames) {
			if (documentFontName==null || documentFontName.trim().length()==0) continue;
			if (get(documentFontName)!=null) {
				log.debug(documentFontName + " already mapped");
				continue;
			}
			PhysicalFont pf = installedOrEmbedded(documentFontName);
			if (pf==null) {
				pf = resolveDocumentFont(documentFontName, table.get(documentFontName.trim().toLowerCase()));
			}
			if (pf==null) {
				log.warn("- - No physical font for: " + documentFontName + " so ensure it is mapped. ");
			} else {
				put(documentFontName, pf);
				if (log.isDebugEnabled()) log.debug(documentFontName + " -> " + pf.getName());
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
	public static String getSubstituteFontXsltExtension(Mapper s, String documentStyleId, String bolditalic, boolean fontFamilyStack) {
		
		return s.getSubstituteFontXsltExtension(documentStyleId, bolditalic, fontFamilyStack);
	}
	
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
		
//		log.info(documentStyleId + " -> " + physicalFont.getName() );
//		
//		if (fontFamilyStack) {
//			
//			// TODO - if this is an HTML document intended
//			// for viewing in a web browser, we need to add a 
//			// font-family cascade (since the true type font
//			// specified for PDF purposes won't necessarily be
//			// present on web browser's system).
//			
//			// The easiest way to do it might be to just
//			// see whether the substitute font is serif or
//			// not, and add cascade entries accordingly.
//			
//			// If we matched it via FontSubstitutions.xml,
//			// maybe that file contains an HTML match as well?
//			
//			// Either way, this stuff should be worked out in
//			// populateFontMappings, and added to the 
//			// FontMapping objects.
//			
//			return physicalFont.getName();
//		} else {
//			return physicalFont.getName();
//		}
		
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
		// (we could do this the other way around, or make it configurable)
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
     * Auto-add mappings for Calibri, Cambria etc where possible and useful
     * @since 11.5.9
     */
    public void addMetricallyCompatibleSubstitutes() {
		
		// Croscore or Liberation.  NB Times New Roman is a serif and Arial a sans:
		// the two second substitutes were the wrong way round until 17.0.5, so on a
		// box with Liberation but not Croscore each became the other's class.
    	addMetricallyCompatibleSubstitute("Times New Roman", "Tinos Regular", "Liberation Serif");
    	addMetricallyCompatibleSubstitute("Arial", "Arimo Regular", "Liberation Sans");
    	addMetricallyCompatibleSubstitute("Courier New", "Cousine Regular", "Liberation Mono");

		// Crosextra
    	// second choice where the crosextra clones are absent (a box with only the
    	// Liberation jar, e.g. a build server): a font of the same class, so the
    	// text is at least a sans / a serif; line heights still come from the
    	// document font's own metrics (WordLineMetrics).  @since 17.0.5
    	addMetricallyCompatibleSubstitute("Calibri", "Carlito Regular", "Liberation Sans");
    	addMetricallyCompatibleSubstitute("Cambria", "Caladea Regular", "Liberation Serif");
    	addMetricallyCompatibleSubstitute("Calibri Light", "Carlito Regular", "Liberation Sans");

    	// URW base 35 (ghostscript-fonts, on most Linux boxes).  Century Gothic was
    	// drawn to ITC Avant Garde Gothic's widths, and URW Gothic is the Avant Garde
    	// clone: measured against the Century Gothic Word embedded in a real document,
    	// URW Gothic Book matches it to the unit over 6743 characters (0.00%), and
    	// URW Gothic Demi likewise matches Century Gothic Bold.  Without this the
    	// class-based fallback reached a Helvetica clone, 3.1% wider, which is enough
    	// to break a full line differently.  @since 17.0.5
    	addMetricallyCompatibleSubstitute("Century Gothic", "URW Gothic", "Liberation Sans");

    	// Fonts with no metric-compatible clone, but where a stand-in of the right
    	// class is much closer than the document's default font, which is what
    	// RunFontSelector falls back to (a sans in Tinos, or Georgian in Carlito,
    	// was the first divergence in a fifth of a real-document sample; CR-001).
    	// The widths are not Word's, so lines still break differently.  @since 17.0.5
    	// Tw Cen MT (Twentieth Century) is a geometric sans; without an entry it fell
    	// through to the serif default and its labels came out 3-4% narrow (measured
    	// against Word's own PDF of a corpus document, every y and x within 0.3pt and
    	// the text 1.03-1.04x ours).  Arimo is 1.0605x Tinos, so the residual is 2%.
    	// @since 17.1.0
    	for (String sans : new String[] { "Tahoma", "Trebuchet MS", "Segoe UI",
    			"Gadugi", "Helvetica", "Helvetica Neue", "Tw Cen MT" }) {
    		addMetricallyCompatibleSubstitute(sans, "Arimo Regular", "Liberation Sans");
    	}

    	// Arial Black is far heavier and wider than Arial: measured against Word's own
    	// PDF of a corpus document, its centred title is 281.2pt against our Arimo's
    	// 247.9 on the same centre - 1.134x.  Noto Sans Black measures 1.1122x Arimo
    	// over a mixed Latin sample, so the residual is 2% instead of 13.4%.  Arimo
    	// remains the last resort.  @since 17.1.0
    	addFirstAvailableSubstitute("Arial Black", "Noto Sans Black", "Noto Sans Display Black",
    			"Arimo Regular", "Liberation Sans");

    	// Verdana and Comic Sans MS are much wider than Arial, so an Arial clone
    	// re-breaks every line of a document set in them.  Measured against Word's own
    	// PDFs of real documents, on lines whose text matches exactly: Word's Verdana
    	// lines are 1.141x our Arimo ones, and DejaVu Sans is 1.14x Arimo over a mixed
    	// Latin sample; Word's Comic Sans lines are 1.153x our Carlito ones (Comic Sans
    	// reached Carlito through the class-based fallback), and Noto Sans is 1.15x
    	// Carlito.  Tahoma is left where it is: on an all-Tahoma document the median
    	// ratio to our Arimo output is 1.006.  @since 17.1.0
    	addFirstAvailableSubstitute("Verdana", "DejaVu Sans", "Arimo Regular", "Liberation Sans");
    	addFirstAvailableSubstitute("Comic Sans MS", "Noto Sans Regular", "DejaVu Sans",
    			"Arimo Regular", "Liberation Sans");

    	// Segoe UI Light has no metric clone, but Arimo is the wrong shape for it:
    	// measured against the Segoe UI Light Word embeds, Arimo's advances are
    	// systematically 11.8% wider, so every line breaks early.  Source Sans has
    	// no systematic bias at all (+0.4% mean signed, 9.4% mean absolute), which is
    	// what line breaking cares about.  Arimo remains the last resort.
    	// @since 17.0.5
    	addFirstAvailableSubstitute("Segoe UI Light",
    			"Source Sans 3", "Source Sans Pro", "Arimo Regular", "Liberation Sans");
    	for (String serif : new String[] { "Garamond", "Bookman Old Style" }) {
    		addMetricallyCompatibleSubstitute(serif, "Tinos Regular", "Liberation Serif");
    	}


    	/* Held back: the Nokia Pure family, the only document font name of the three
    	 * corpora's 449 documents which reaches FOP unresolved.  Its w:altName is Meiryo -
    	 * itself absent, so the alt-name pass cannot resolve it - and its name matches none
    	 * of FontFallback's class keywords, so it gets no class default either; because the
    	 * document's own theme names Nokia Pure Text Light, even the last-resort document
    	 * default is circular.  FOP renders an unknown family in its base-14 Times, so the
    	 * whole of that document (a 26pt title, 85 table headings, 5253 TOC leader dots)
    	 * comes out in a non-embedded serif where the face is a humanist sans
    	 * (w:family="swiss", panose serif-style 11).
    	 *
    	 * addFirstAvailableSubstitute("Nokia Pure Text", "Source Sans 3", "Source Sans Pro",
    	 * "Arimo Regular", "Liberation Sans") - Segoe UI Light's substitute, for the same
    	 * reasons - was measured: it draws that document in the right class and moves its
    	 * page count towards Word's (63 of Word's 87 to 65), and its body lines are closer
    	 * (Word's "Acceptance Test Manual" is 266.9pt, base-14 Times 274.4, Source Sans
    	 * 269.9), but its headings are further out (115.2 against 120.6 and 104.6) and it
    	 * cost that document 0.045 of line parity, which is the whole of the batch's fall on
    	 * that corpus.  Without a measurement of Nokia Pure's own advances there is nothing
    	 * to choose the substitute by, so it waits for one.  @since 17.1.0 */


    	// The Palatino family, and Georgia, are wider than Times, so a Times clone
    	// re-breaks their lines.  P052 is URW's Palladio, the Palatino clone, and is in
    	// the URW base 35 (ghostscript-fonts).  Measured against Word's own PDFs:
    	// Word's Book Antiqua lines are 1.087-1.114x our Tinos ones and its Georgia
    	// lines 1.076-1.112x, where P052 is 1.09x Tinos over a mixed Latin sample.
    	// @since 17.1.0
    	for (String palatino : new String[] { "Georgia", "Book Antiqua", "Palatino Linotype" }) {
    		addFirstAvailableSubstitute(palatino, "P052", "Tinos Regular", "Liberation Serif");
    	}
    	// Liberation Sans Narrow is metric-compatible with Arial Narrow, but neither the
    	// Liberation nor the Croscore jar carries it, and it is no longer in the
    	// Liberation package.  Nimbus Sans Narrow (URW's Helvetica Narrow, in
    	// ghostscript-fonts) is the same 82% condensation and matches Arial Narrow's
    	// advances to within one unit per 1000 over letters, digits and punctuation
    	// (0.02% mean, bold likewise; the control pair Century Gothic / URW Gothic
    	// measures 0.29% by the same method).  Where neither is installed, Arial Narrow
    	// is still deliberately left unmapped: measured over the real-document corpus,
    	// DejaVu Sans Condensed (the nearest condensed face on a typical Linux box) is
    	// further from Arial Narrow than the document default is, and substituting it
    	// cost line parity on three documents.
    	addFirstAvailableSubstitute("Arial Narrow", "Liberation Sans Narrow", "Nimbus Sans Narrow");

    	// Monospace fonts with no metric-compatible clone: a monospace stand-in keeps
    	// code aligned, where the default (proportional) fallback would not.  Widths
    	// differ (Consolas advances 0.55em, Cousine and Liberation Mono 0.6em); line
    	// heights come from the document font's own metrics (WordLineMetrics).
    	// @since 17.0.5
    	addMetricallyCompatibleSubstitute("Consolas", "Cousine Regular", "Liberation Mono");
    	addMetricallyCompatibleSubstitute("Lucida Console", "Cousine Regular", "Liberation Mono");
    	
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
    		while (alt!=null && seen.add(alt.trim().toLowerCase())) {
    			pf = PhysicalFonts.get(alt);
    			if (pf==null) pf = PhysicalFonts.get(alt + " Regular");
    			if (pf==null) pf = get(alt);
    			if (pf!=null) { resolvedAlt = alt; break; }
    			alt = altNames.get(alt.trim().toLowerCase());
    		}
    		if (pf==null) continue;

    		if (log.isDebugEnabled()) {
    			log.debug("Mapping " + documentFontName + " to " + pf.getName() + " (w:altName " + resolvedAlt + ")");
    		}
    		put(documentFontName, pf);
    		WordLineMetrics.registerAlias(documentFontName, resolvedAlt);
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
     * {@link BestMatchingMapper} already consults; this makes them available to
     * {@link IdentityPlusMapper}, which is the default mapper, without changing what
     * BestMatchingMapper does.</p>
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
    			if (log.isDebugEnabled()) {
    				log.debug("Mapping " + documentFontName + " to " + pf.getName() + " (same class)");
    			}
    			put(documentFontName, pf);
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
    		if (log.isDebugEnabled()) {
    			log.debug("Mapping " + documentFontName + " to " + pf.getName() + " (Word's default for an unknown font: " + wordFont + ")");
    		}
    		put(documentFontName, pf);
    		WordLineMetrics.registerAlias(documentFontName, wordFont);
    	}
    }

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
    	if (WordLineMetrics.hasTableEntry(name)) return true;
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
    		PhysicalFont pf = get(documentFontName);
    		if (pf==null || pf.isNoBoldFace()) continue;
    		if (PhysicalFonts.getBoldForm(pf)==null && boldForms.get(documentFontName)==null) continue; // nothing to withhold
    		PhysicalFont alias = pf.noBoldFaceAlias();
    		if (log.isDebugEnabled()) {
    			log.debug(documentFontName + " has no bold face: " + alias.getName() + " (bold synthesised at the regular advances)");
    		}
    		fontMappings.put(documentFontName.toLowerCase(), alias);
    	}
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
     * @param proprietaryFont
     * @param openSubstitute
     * @param openSubstitute2
     * @since 11.5.9
     */
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

    protected void addMetricallyCompatibleSubstitute(String proprietaryFont, String openSubstitute, String openSubstitute2) {
    	
    	if (isEmbedded(proprietaryFont)) {
    		/* The document embeds this font, so populateFontMappings will have mapped it
    		 * to that.  Don't replace it with a substitute: the embedded font is what the
    		 * author intended, and it is the only thing which is certain to be available.
    		 * NB this runs after populateFontMappings; see
    		 * WordprocessingMLPackage.setFontMapper.
    		 * @since 17.0.3 */
    		if (log.isDebugEnabled()) {
    			log.debug("Not substituting for " + proprietaryFont + "; the document embeds it");
    		}
    		return;
    	}

    	if (PhysicalFonts.get(proprietaryFont)==null) {
    		if (PhysicalFonts.get(openSubstitute)!=null) {
	    		if (log.isDebugEnabled()) {
	    			log.debug("Mapping " + proprietaryFont + " to " + openSubstitute);
	    		}
	    		put(proprietaryFont, PhysicalFonts.get(openSubstitute));
    		} else if (openSubstitute2 !=null && PhysicalFonts.get(openSubstitute2)!=null) {
	    		if (log.isDebugEnabled()) {
	    			log.debug("Mapping " + proprietaryFont + " to " + openSubstitute2);
	    		}
	    		put(proprietaryFont, PhysicalFonts.get(openSubstitute2));
    		} 
    	}
    	
    }
	
}
