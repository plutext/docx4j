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
	 * Populate the fontMappings object. We make an entry for each
	 * of the documentFontNames.
	 * 
	 * @param documentFontNames - the fonts used in the document
	 * @param wmlFonts - the content model for the fonts part
	 * @throws Exception
	 */
	public abstract void populateFontMappings(Set<String> documentFontNames, 
			org.docx4j.wml.Fonts wmlFonts ) throws Exception;
	
	
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
    	for (String sans : new String[] { "Tahoma", "Verdana", "Trebuchet MS", "Segoe UI",
    			"Arial Black", "Gadugi", "Helvetica", "Helvetica Neue" }) {
    		addMetricallyCompatibleSubstitute(sans, "Arimo Regular", "Liberation Sans");
    	}

    	// Segoe UI Light has no metric clone, but Arimo is the wrong shape for it:
    	// measured against the Segoe UI Light Word embeds, Arimo's advances are
    	// systematically 11.8% wider, so every line breaks early.  Source Sans has
    	// no systematic bias at all (+0.4% mean signed, 9.4% mean absolute), which is
    	// what line breaking cares about.  Arimo remains the last resort.
    	// @since 17.0.5
    	addFirstAvailableSubstitute("Segoe UI Light",
    			"Source Sans 3", "Source Sans Pro", "Arimo Regular", "Liberation Sans");
    	for (String serif : new String[] { "Georgia", "Garamond", "Book Antiqua", "Palatino Linotype",
    			"Bookman Old Style" }) {
    		addMetricallyCompatibleSubstitute(serif, "Tinos Regular", "Liberation Serif");
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
    
    /** Whether {@link #addClassBasedSubstitutes} applies to this mapper.  False for
     *  {@link BestMatchingMapper}, which reaches its own conclusions from panose and
     *  from FontSubstitutions.xml, and whose behaviour is unchanged.
     *  @since 17.0.5 */
    public boolean wantsClassBasedSubstitutes() {
    	return true;
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
