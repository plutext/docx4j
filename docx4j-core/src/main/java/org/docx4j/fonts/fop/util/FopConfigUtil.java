/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
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

package org.docx4j.fonts.fop.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.fopconf.Fonts.Font;
import org.docx4j.convert.out.fopconf.Fop;
import org.docx4j.convert.out.fopconf.Fop.Fonts;
import org.docx4j.convert.out.fopconf.Fop.Renderers;
import org.docx4j.convert.out.fopconf.Fop.Renderers.Renderer;
import org.docx4j.convert.out.fopconf.Substitutions;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.RunFontSelector;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.fonts.fop.fonts.FontTriplet;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.utils.ResourceUtils;

/**
 * The sole role of this class is to create a configuration
 * which can be used to configure FOP.
 * 
 * As of 8.3.3, we have a JAXB representation of the FOP XML config. 
 * 
 * @author jharrop
 *
 */
public class FopConfigUtil {
	
	protected static Logger log = LoggerFactory.getLogger(FopConfigUtil.class);
	
	private static Substitutions substitutions=null;
	
	private static org.docx4j.convert.out.fopconf.ObjectFactory factory = null;
	
	static {
		
		factory = new org.docx4j.convert.out.fopconf.ObjectFactory(); 
		
		// See https://github.com/plutext/docx4j/issues/424 for motivations,
		// and also https://github.com/plutext/docx4j/blob/master/docx4j-samples-resources/src/main/resources/fop-substitutions.xml
		
		String substitutionsPath = Docx4jProperties.getProperty("docx4j.fonts.fop.util.FopConfigUtil.substitutions");
		if (substitutionsPath!=null) {
		
			java.io.InputStream is = null;
			try {
				is = ResourceUtils.getResource(substitutionsPath);
	
				Unmarshaller u = Context.getFopConfigContext().createUnmarshaller();
				Object o = u.unmarshal(is);
//				log.debug(o.getClass().getName());
				substitutions = (Substitutions)o;
			} catch (IOException e) {
				log.error("Problems with class path resource " + substitutionsPath);
				log.error(e.getMessage(), e);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	public static Fop createConfigurationObject(Mapper fontMapper, Set<String> fontsInUse) throws Docx4JException {
		
		log.debug("Config object");

		Fop fopConfig = factory.createFop();
		fopConfig.setVersion("1.0");
		
		fopConfig.setStrictConfiguration(false);
		fopConfig.setUseCache(false); // no need for cache, since we declare fonts (and have our own FontCache in PhysicalFonts)

		if (substitutions!=null) {
			Fonts fonts = factory.createFopFonts();
			fopConfig.setFonts(fonts);
			fonts.setSubstitutions(substitutions);
		} else {
			log.debug("No font substitutions provided at " 
					+ Docx4jProperties.getProperty("docx4j.fonts.fop.util.FopConfigUtil.substitutions"));
		}
		Renderers renderers = factory.createFopRenderers();
		fopConfig.setRenderers(renderers);
		Renderer renderer = factory.createFopRenderersRenderer();
		renderers.getRenderer().add(renderer);
		renderer.setMime("application/pdf");

		renderer.setFonts(declareRendererFonts(fontMapper, fontsInUse));
		
		return fopConfig;
	}
		
    public static Renderer get(Renderers renderers, String mime) {
    	
    	for( Renderer r : renderers.getRenderer()) {
    		
    		if (r.getMime().equals(mime)) {
    			return r;
    		}
    	}
    	return null;
    }

	/**
	 * Create a FOP font configuration for each font used in the
	 * document.
	 * 
	 * @return
	 */
	protected static org.docx4j.convert.out.fopconf.Fonts declareRendererFonts(Mapper fontMapper, Set<String> fontsInUse) {

		org.docx4j.convert.out.fopconf.Fonts rendererFonts = factory.createFonts();

		if (fontsInUse.size()==0) {
			log.error("No fonts detected in document!");
			return rendererFonts;
		}
		
		/* Two document fonts commonly map to the same file - Times New Roman and
		 * Tinos to Tinos-Regular.ttf, several unmapped fonts to one substitute -
		 * and each needs its own font-triplet on the one <font embed-url=..>.
		 * Until 17.0.5 the entries were kept in a map keyed by @embed-url, so the
		 * second font silently replaced the first (its triplets are added after the
		 * put) and FOP then reported "Font X not found. Substituting with any".
		 * They are collected in order now and merged by mergeByEmbedUrl.
		 */

		List<org.docx4j.convert.out.fopconf.Fonts.Font> fontEntries = new ArrayList<org.docx4j.convert.out.fopconf.Fonts.Font>(); 
		
		if (Docx4jProperties.getProperty("docx4j.fonts.fop.util.FopConfigUtil.simulate-style", true)) {
		// <font simulate-style="true"	
			for (String fontName : fontsInUse) {		    
			    
				PhysicalFont pf;
				PhysicalFont pf2 = null;
				if (fontName.equals("Webdings")
						|| fontName.equals("Wingdings")
						|| fontName.equals("Wingdings 2")
						|| fontName.equals("Wingdings 3")
						) {
					pf = PhysicalFonts.getWDingsFont();
					pf2 = PhysicalFonts.getWDingsFont2();
				} else if (fontName.equals("Symbol")) {
					pf = PhysicalFonts.getSymbolFont();
				} else {
					// Usual case
					pf = fontMapper.get(fontName);
				}

				
			    if (pf==null) {
			    	log.warn("Document font " + fontName + " is not mapped to a physical font!");
			    	// We may still have eg Cambria-bold embedded, but ignore this for now
			    } else {
			    	
			    	createFontEntrySimulateStyles( fontMapper,  fontEntries, pf.getName(), pf); // using pf.getName() ensures we use the symbol font substitute name
			    	if (pf2!=null) {
			    		createFontEntrySimulateStyles( fontMapper,  fontEntries, pf2.getName(), pf2);			    		
			    	}
			    }
			}
			
			
		} else {

		// <font simulate-style="false"
			for (String fontName : fontsInUse) {		    
			    
				PhysicalFont pf;
				PhysicalFont pf2 = null;
				if (fontName.equals("Webdings")
						|| fontName.equals("Wingdings")
						|| fontName.equals("Wingdings 2")
						|| fontName.equals("Wingdings 3")
						) {
					pf = PhysicalFonts.getWDingsFont();
					pf2 = PhysicalFonts.getWDingsFont2();
				} else if (fontName.equals("Symbol")) {
					pf = PhysicalFonts.getSymbolFont();
				} else {				
					pf = fontMapper.get(fontName);
				}
			    
			    if (pf==null) {
			    	log.warn("Document font " + fontName + " is not mapped to a physical font!");
			    	// We may still have eg Cambria-bold embedded
			    } else {

			    	createFontEntry( fontMapper,  fontEntries, pf.getName(), pf);
			    	if (pf2!=null) {
				    	createFontEntry( fontMapper,  fontEntries, pf2.getName(), pf2);			    		
			    	}
			    }
			}
		}
		if (fontEntries.isEmpty()) {
			log.warn("No fonts configured!");
		} else {
			for (Font entry : mergeByEmbedUrl(fontEntries) ) {
				if (mustNotUseOpenTypeLayout(entry)) {
					entry.setAdvanced(Boolean.FALSE);
				}
				rendererFonts.getFont().add(entry);
				if (!kerning()) {
					rendererFonts.getFont().add(kernedTwin(entry));
				}
				Font noLiga = noLigaTwin(entry);
				if (noLiga!=null) {
					rendererFonts.getFont().add(noLiga);
				}
			}
		}
		return rendererFonts;
	}

	/**
	 * Leave FOP to apply the OpenType layout features to a CJK font, as it did before
	 * 17.1.1 - which costs the text layer the characters below, so this is off by
	 * default.
	 *
	 * @since 17.1.1
	 */
	private static boolean cjkAdvancedFeatures() {
		return Docx4jProperties.getProperty("docx4j.convert.out.fo.cjkAdvancedFeatures", false);
	}

	/**
	 * Whether this font must be declared with FOP's OpenType layout turned off, so that
	 * the PDF's text layer says what the document says.
	 *
	 * <p>FOP runs a substituted run through its layout tables as <em>characters</em>: it
	 * maps the characters to glyphs, substitutes, and maps the glyphs back to characters
	 * ({@code MultiByteFont.performSubstitution}, whose {@code mapGlyphsToChars} takes
	 * each glyph's character from the first cmap segment which covers it -
	 * {@code findCharacterFromGlyphIndex}, "if more than one correspondence exists, then
	 * the first one is returned").  A CJK font maps a Kangxi radical and the ideograph it
	 * is the radical of to <b>one glyph</b> - in Source Han Sans CN, U+2F63 and U+751F are
	 * both glyph 18742 - and the radical is the lower code point, so the round trip
	 * replaces the ideograph with the radical.  The right glyph is still drawn; what
	 * changes is the character, and it is the character which reaches the PDF's ToUnicode
	 * map, so the text cannot be extracted, searched or read out.  Measured on a real
	 * document: an ideograph which shares its glyph with a radical came out of the PDF as
	 * that radical, on 110 lines of two documents of one corpus.</p>
	 *
	 * <p>Nothing in a CJK font's layout tables applies to horizontal text of the font's
	 * own region (vert and vrt2 are for vertical writing, and locl selects the region the
	 * face is already for), so turning them off changes no glyph: measured on the five
	 * characters above through FOP itself, the glyph indices, their positions and their
	 * advances are identical with the features on and off, and only the ToUnicode differs.
	 * This is the same trade {@link #noLigaTwin} makes for a Latin font, and the reason is
	 * the same one; it is expressed with FOP's per-font "advanced" attribute rather than
	 * with encoding-mode="single-byte" because a CFF-flavoured or CID font cannot be
	 * declared single-byte (see that method).</p>
	 *
	 * <p>Drop this when a FOP which keeps the original characters ships (Enterprise CR-001
	 * section 6.6).  {@code docx4j.convert.out.fo.cjkAdvancedFeatures=true} turns it off.</p>
	 *
	 * @since 17.1.1
	 */
	private static boolean mustNotUseOpenTypeLayout(Font entry) {
		if (cjkAdvancedFeatures()) return false;
		PhysicalFont pf = byEmbedUrl(entry.getEmbedUrl());
		if (pf==null) return false;
		try {
			return org.docx4j.fonts.GlyphCheck.reverseLookupTakesACjkRadical(pf);
		} catch (Exception e) {
			log.debug("couldn't read the cmap of " + entry.getEmbedUrl() + ": " + e.getMessage());
			return false;
		}
	}

	/** The physical font declared at this embed-url, or null (an embedded font, which is
	 *  not in PhysicalFonts).  The bold and italic forms are declared from their own files
	 *  and are not in the map under a name of their own, so they are asked for by form.
	 *  @since 17.1.1 */
	private static PhysicalFont byEmbedUrl(String embedUrl) {
		if (embedUrl==null) return null;
		for (PhysicalFont pf : PhysicalFonts.getPhysicalFonts().values()) {
			if (isAt(pf, embedUrl)) return pf;
			PhysicalFont form = PhysicalFonts.getBoldForm(pf);
			if (isAt(form, embedUrl)) return form;
			form = PhysicalFonts.getItalicForm(pf);
			if (isAt(form, embedUrl)) return form;
			form = PhysicalFonts.getBoldItalicForm(pf);
			if (isAt(form, embedUrl)) return form;
		}
		return null;
	}

	private static boolean isAt(PhysicalFont pf, String embedUrl) {
		return pf!=null && pf.getEmbeddedURI()!=null
				&& embedUrl.equals(pf.getEmbeddedURI().toString());
	}

	/**
	 * Word does not apply kerning unless a run asks for it (w:kern), whereas
	 * FOP kerns every font that has a kern table; the difference moves line
	 * breaks (a kerned line can be a fraction of a point shorter).  So kerning
	 * is off unless docx4j.convert.out.fo.kerning=true.
	 *
	 * @since 17.0.5
	 */
	private static boolean kerning() {
		return Docx4jProperties.getProperty("docx4j.convert.out.fo.kerning", false);
	}

	/**
	 * The same font again with FOP kerning on, under each triplet name plus
	 * {@link RunFontSelector#KERNED_SUFFIX}: Word kerns a run only when its
	 * w:kern threshold is at or below its size, and FOP kerns per font, so
	 * RunFontSelector sends kerned runs to this twin.  FOP embeds a declared
	 * font only when it is used, so documents without kerned runs pay nothing.
	 *
	 * @since 17.0.5
	 */
	private static org.docx4j.convert.out.fopconf.Fonts.Font kernedTwin(org.docx4j.convert.out.fopconf.Fonts.Font font) {
		org.docx4j.convert.out.fopconf.Fonts.Font twin = factory.createFontsFont();
		twin.setEmbedUrl(font.getEmbedUrl());
		twin.setSubFont(font.getSubFont());
		twin.setSimulateStyle(font.isSimulateStyle());
		twin.setKerning(true);
		twin.setAdvanced(font.isAdvanced());
		for (org.docx4j.convert.out.fopconf.Fonts.Font.FontTriplet t : font.getFontTriplet()) {
			twin.getFontTriplet().add(createFontTriplet(t.getName() + RunFontSelector.KERNED_SUFFIX, t.getStyle(), t.getWeight()));
		}
		return twin;
	}

	/**
	 * The same font again with no OpenType features at all, under each triplet name
	 * plus {@link RunFontSelector#NOLIGA_SUFFIX}.
	 *
	 * <p>FOP applies the GSUB features ccmp, liga and locl to every font that has
	 * them, unconditionally (DefaultScriptProcessor.GSUB_FEATURES); Word applies no
	 * standard ligature unless the run asks for one (w14:ligatures).  The difference
	 * is not only width: a ligature glyph has no cmap entry of its own, so FOP mints
	 * a private-use code point for it and the PDF's ToUnicode maps the ligature to
	 * U+E000 - the text is painted, but cannot be extracted, searched or read by a
	 * screen reader.  Measured over a real-document corpus, a third of the lines of
	 * a French document came out with "ti" as U+E000.</p>
	 *
	 * <p>FOP has no per-run or per-feature switch (the renderer's complex-scripts
	 * option disables Arabic and Indic shaping too, and the per-font "advanced"
	 * attribute is dropped on the PDF path).  What does work per declaration is
	 * encoding-mode="single-byte", which loads the font as a simple TrueType font -
	 * one that implements neither Substitutable nor Positionable, so no GSUB and no
	 * GPOS.  RunFontSelector sends runs of simple-script text that ask for neither
	 * ligatures nor kerning to this twin.</p>
	 *
	 * <p>Only TrueType-flavoured files get a twin: FOP forces a single-byte font to
	 * FontType.TRUETYPE, which would misdescribe a CFF/OpenType font in the PDF.
	 * Measured (CR-001, 17.1.0): declaring an <code>.otf</code> substitute this way does
	 * suppress the ligatures and does extract, but FOP writes the font as
	 * <code>/Subtype /TrueType</code> with the <code>OTTO</code> file in a
	 * <code>/FontFile2</code> stream - invalid PDF, which only lenient readers draw.  So
	 * a CFF-flavoured substitute (URW's Nimbus Sans Narrow for Arial Narrow, Source Sans
	 * 3 for Segoe UI Light) still gets FOP's ligatures; the fix belongs upstream.</p>
	 *
	 * @return null where the font must not be declared this way
	 * @since 17.0.5
	 */
	private static org.docx4j.convert.out.fopconf.Fonts.Font noLigaTwin(org.docx4j.convert.out.fopconf.Fonts.Font font) {
		if (!isTrueTypeFlavoured(font.getEmbedUrl())) return null;
		org.docx4j.convert.out.fopconf.Fonts.Font twin = factory.createFontsFont();
		twin.setEmbedUrl(font.getEmbedUrl());
		twin.setSubFont(font.getSubFont());
		twin.setSimulateStyle(font.isSimulateStyle());
		twin.setKerning(false);
		twin.setEncodingMode("single-byte");
		for (org.docx4j.convert.out.fopconf.Fonts.Font.FontTriplet t : font.getFontTriplet()) {
			twin.getFontTriplet().add(createFontTriplet(t.getName() + RunFontSelector.NOLIGA_SUFFIX, t.getStyle(), t.getWeight()));
		}
		return twin;
	}

	/** whether the font file holds glyf outlines (.ttf/.ttc), as opposed to CFF ones. */
	public static boolean isTrueTypeFlavoured(String embedUrl) {
		if (embedUrl==null) return false;
		String u = embedUrl.toLowerCase();
		int q = u.indexOf('?');
		if (q>0) u = u.substring(0, q);
		return u.endsWith(".ttf") || u.endsWith(".ttc");
	}

	private static void createFontEntrySimulateStyles(Mapper fontMapper, List<org.docx4j.convert.out.fopconf.Fonts.Font> fontEntries,
			String fontName, PhysicalFont pf) {
		
    	org.docx4j.convert.out.fopconf.Fonts.Font rendererFont = factory.createFontsFont();
		fontEntries.add(rendererFont);
    	
    	rendererFont.setSimulateStyle(false);
    	rendererFont.setKerning(kerning());
    	
	    if (pf.getEmbedFontInfo().getSubFontName()!=null) {
	    	rendererFont.setSubFont( pf.getEmbedFontInfo().getSubFontName() );
	    }
	    	
    	if (fontMapper.getBoldForm(fontName, pf)==null
    			|| fontMapper.getItalicForm(fontName, pf)==null) {
    		
    		rendererFont.setSimulateStyle(true);
    		rendererFont.setEmbedUrl(pf.getEmbeddedURI().toString());
    		
    		rendererFont.getFontTriplet().add(createFontTriplet(fontName, "normal", "normal"));

    		// Italics
			PhysicalFont pfVariation = fontMapper.getItalicForm(fontName, pf);
    		if (pfVariation==null) {
    			rendererFont.getFontTriplet().add(createFontTriplet(fontName, "italic", "normal"));
    			if (log.isDebugEnabled()) {
    				log.debug(fontName + " - no italic form");
    			}
    		} else {    			
    			org.docx4j.convert.out.fopconf.Fonts.Font variant = createVariant(pf, pfVariation, "italic", "italic", "normal");    			
        		fontEntries.add(variant);    			
    			if (log.isDebugEnabled()) {
    				log.debug(fontName + " - added italic form");
    			}
    		}
    		
    		
    		// Bold
			pfVariation = fontMapper.getBoldForm(fontName, pf);
    		if (pfVariation==null) {
    			rendererFont.getFontTriplet().add(createFontTriplet(fontName, "normal", "bold"));
    			if (log.isDebugEnabled()) {
    				log.debug(fontName + " - no bold form");
    			}
    		} else {    			
    			org.docx4j.convert.out.fopconf.Fonts.Font variant = createVariant(pf, pfVariation, "bold", "normal", "bold");
        		fontEntries.add(variant);    			
    			if (log.isDebugEnabled()) {
    				log.debug(fontName + " - added bold form");
    			}
    		}
    		
    		
    		rendererFont.getFontTriplet().add(createFontTriplet(pf.getName(), "italic", "bold"));
    		addFamilyTriplet(rendererFont, pf, "normal", "normal");

    	} else {
    		// If we don't have to simulate-style, fall back to the old way of doing things
    		rendererFont.setEmbedUrl(pf.getEmbeddedURI().toString());
    
	    	// now add the first font triplet
		    FontTriplet fontTriplet = (FontTriplet)pf.getEmbedFontInfo().getFontTriplets().get(0);
    		rendererFont.getFontTriplet().add(
    				createFontTriplet(fontTriplet.getName(), fontTriplet.getStyle(), 
    						weightToCSS2FontWeight(fontTriplet.getWeight())));
    		addFamilyTriplet(rendererFont, pf, fontTriplet.getStyle(),
    				weightToCSS2FontWeight(fontTriplet.getWeight()));

		    addVariations(fontMapper, fontEntries, fontName, pf, rendererFont.getSubFont());
    	}
		
	}	
	
	private static void createFontEntry(Mapper fontMapper, List<org.docx4j.convert.out.fopconf.Fonts.Font> fontEntries, 
			String fontName, PhysicalFont pf) {

    	org.docx4j.convert.out.fopconf.Fonts.Font rendererFont = factory.createFontsFont();
		fontEntries.add(rendererFont);    	
    	
    	rendererFont.setSimulateStyle(false);
    	rendererFont.setKerning(kerning());
    	
	    if (pf.getEmbedFontInfo().getSubFontName()!=null) {
	    	rendererFont.setSubFont( pf.getEmbedFontInfo().getSubFontName() );
	    }
		rendererFont.setEmbedUrl(pf.getEmbeddedURI().toString());
	    

		// now add the first font triplet
	    FontTriplet fontTriplet = (FontTriplet)pf.getEmbedFontInfo().getFontTriplets().get(0);
		rendererFont.getFontTriplet().add(
				createFontTriplet(fontTriplet.getName(), fontTriplet.getStyle(), 
						weightToCSS2FontWeight(fontTriplet.getWeight())));
		addFamilyTriplet(rendererFont, pf, fontTriplet.getStyle(),
				weightToCSS2FontWeight(fontTriplet.getWeight()));

	    addVariations(fontMapper, fontEntries, fontName, pf, 
	    		pf.getEmbedFontInfo().getSubFontName());
		
	}
	
	private static org.docx4j.convert.out.fopconf.Fonts.Font.FontTriplet createFontTriplet(String name, String style, String weight) {

		org.docx4j.convert.out.fopconf.Fonts.Font.FontTriplet triplet = factory.createFontsFontFontTriplet();
		triplet.setName(name);
		triplet.setStyle(style);
		triplet.setWeight(weight);
		return triplet;
	}

	/**
	 * Also declare this face under the font's <b>family</b> name - "Carlito" beside
	 * "Carlito Regular" - where the two differ.
	 *
	 * <p>RunFontSelector writes the face name, so the document's own text needs nothing
	 * more; but a picture does. A metafile's text is drawn through AWT and written out by
	 * Batik as SVG, and what Batik puts in {@code font-family} is the AWT font's family.
	 * FOP resolves the SVG's families against this same configuration, so without the
	 * family name it reported "Font Carlito,normal,400 not found. Substituting with
	 * any" - one of its base-14 fonts, which the PDF names and does not embed
	 * (CR-001, non-embedded fonts; CR-011's pathway).</p>
	 *
	 * @since 17.1.1
	 */
	private static void addFamilyTriplet(org.docx4j.convert.out.fopconf.Fonts.Font entry,
			PhysicalFont pf, String style, String weight) {
		if (pf==null || entry==null) return;
		String family = pf.getFamilyName();
		if (family==null || family.length()==0) return;
		for (org.docx4j.convert.out.fopconf.Fonts.Font.FontTriplet t : entry.getFontTriplet()) {
			if (family.equals(t.getName()) && eq(style, t.getStyle()) && eq(weight, t.getWeight())) {
				return;
			}
		}
		entry.getFontTriplet().add(createFontTriplet(family, style, weight));
	}

	private static void addVariations(Mapper fontMapper, List<org.docx4j.convert.out.fopconf.Fonts.Font> fontEntries, 
			String fontName, PhysicalFont pf,
			String subFontAtt) {
				
		// bold, italic etc
		PhysicalFont pfVariation = fontMapper.getBoldForm(fontName, pf);
		if (pfVariation==null) {
			log.debug(fontName + " no bold form");
		} else {
			org.docx4j.convert.out.fopconf.Fonts.Font variant = createVariant(pf, pfVariation, subFontAtt, "normal", "bold");
    		fontEntries.add(variant);
		}
		pfVariation = fontMapper.getBoldItalicForm(fontName, pf);
		if (pfVariation==null) {
			log.debug(fontName + " no bold italic form");
		} else {
			org.docx4j.convert.out.fopconf.Fonts.Font variant = createVariant(pf, pfVariation, subFontAtt, "italic", "bold");
    		fontEntries.add(variant);
		}
		pfVariation = fontMapper.getItalicForm(fontName, pf);
		if (pfVariation==null) {
			log.debug(fontName + " no italic form");
		} else {
			org.docx4j.convert.out.fopconf.Fonts.Font variant = createVariant(pf, pfVariation, subFontAtt, "italic", "normal");
    		fontEntries.add(variant);
		}
	}
		
	private static org.docx4j.convert.out.fopconf.Fonts.Font createVariant(PhysicalFont pf, PhysicalFont pfVariation ,
			String subFontAtt, String style, String weight) {

		org.docx4j.convert.out.fopconf.Fonts.Font rendererFont = factory.createFontsFont();
    	rendererFont.setSimulateStyle(false);
    	rendererFont.setKerning(kerning());
    	// name?
    	rendererFont.setEmbedUrl(pfVariation.getEmbeddedURI().toString());
    	rendererFont.setSubFont(subFontAtt);
		rendererFont.getFontTriplet().add(createFontTriplet(pf.getName(), style, weight));
		addFamilyTriplet(rendererFont, pfVariation, style, weight);

		return rendererFont;
	}
	
//	protected static void addFontTriplet(StringBuilder result, FontTriplet fontTriplet) {
//		addFontTriplet(result, fontTriplet.getName(), 
//							   fontTriplet.getStyle(), 
//							   weightToCSS2FontWeight(fontTriplet.getWeight()));
//	}
	
	protected static void addFontTriplet(StringBuilder result, String familyName, String style, String weight) {
	    result.append("<font-triplet name=\""); 
	    result.append(familyName);
	    result.append('"');
	    result.append(" style=\"");
	    result.append(style);
	    result.append('"');
	    result.append(" weight=\"");
	    result.append(weight); 
	    result.append("\"/>");
	}
	
	protected static String weightToCSS2FontWeight(int i) {
		return (i >= 700 ? "bold" : "normal");
	}

	/**
	 * The face FOP will draw a run of this family in, given the configuration
	 * {@link #declareRendererFonts} builds from the same mapper - so that a width docx4j
	 * measures before FOP sees the document (the table autofit pass) is the width FOP
	 * lays out.
	 *
	 * <p>RunFontSelector writes the regular face's name as {@code font-family} for all
	 * four faces of a family, and {@code font-weight} / {@code font-style} say which one;
	 * this reproduces what the declaration above and FOP's own lookup
	 * ({@code FontInfo.fuzzyFontLookup}) make of that:</p>
	 * <ul>
	 * <li><b>bold</b>, or <b>italic</b>: that face where the mapper has one, else the
	 * regular file - FOP re-strokes or skews the regular glyphs when
	 * {@code simulate-style} is on, and matches the nearest declared weight when it is
	 * off, and either way the advances are the regular face's;</li>
	 * <li><b>bold italic</b>: the bold-italic face.  Failing that, with
	 * {@code simulate-style} on (the default) the family's variants are only declared at
	 * all when it has <i>both</i> a bold and an italic face, in which case FOP adjusts the
	 * weight and lands on the italic; otherwise the bold-italic triplet is the regular
	 * file, synthesised.  With it off, FOP's lookup tries the italic at weight 400 before
	 * the bold at style normal, before the regular.</li>
	 * </ul>
	 *
	 * <p>Never null when {@code regular} is not - a family without the face is drawn in
	 * the regular file, and that is what is returned.</p>
	 *
	 * @since 17.1.1
	 */
	public static PhysicalFont renderedFace(Mapper fontMapper, PhysicalFont regular, boolean bold, boolean italic) {

		if (regular==null || (!bold && !italic)) return regular;

		String name = regular.getName();
		PhysicalFont pfBold = fontMapper==null ? PhysicalFonts.getBoldForm(regular)
				: fontMapper.getBoldForm(name, regular);
		PhysicalFont pfItalic = fontMapper==null ? PhysicalFonts.getItalicForm(regular)
				: fontMapper.getItalicForm(name, regular);

		if (bold && !italic) return pfBold!=null ? pfBold : regular;
		if (italic && !bold) return pfItalic!=null ? pfItalic : regular;

		// bold italic
		boolean simulate = Docx4jProperties.getProperty("docx4j.fonts.fop.util.FopConfigUtil.simulate-style", true);
		if (simulate && (pfBold==null || pfItalic==null)) {
			return regular; // createFontEntrySimulateStyles declares (name, italic, bold) on the regular file
		}
		PhysicalFont pfBoldItalic = fontMapper==null ? PhysicalFonts.getBoldItalicForm(regular)
				: fontMapper.getBoldItalicForm(name, regular);
		if (pfBoldItalic!=null) return pfBoldItalic;
		if (pfItalic!=null) return pfItalic;
		if (pfBold!=null) return pfBold;
		return regular;
	}

	/**
	 * Declare the fonts RunFontSelector chose as a last resort while generating the FO.
	 *
	 * <p>The configuration is built from the fonts the document names, before the FO
	 * exists; a font picked during the conversion because the mapped one had no glyphs
	 * for the run (a Noto face for the document's Georgian, say) is therefore not in it,
	 * and FOP would silently render the run in its default font instead.  FOSettings
	 * calls this when the renderer asks for the configuration, which is after the FO has
	 * been generated.  Idempotent: a font already declared is left alone.</p>
	 *
	 * @since 17.0.5
	 */
	public static void declareFallbackFonts(Fop fopConfig, Mapper fontMapper) {

		if (fopConfig==null || fontMapper==null) return;

		Renderer renderer = (fopConfig.getRenderers()==null) ? null
				: get(fopConfig.getRenderers(), "application/pdf");
		if (renderer==null) return;
		if (renderer.getFonts()==null) {
			renderer.setFonts(factory.createFonts());
		}

		// the names a metafile's SVG can carry for a face nothing resolved
		declareSvgLogicalFamilies(renderer, fontMapper);

		if (fontMapper.getLastResortFallbacks().isEmpty()) return;

		List<Font> fontEntries = new ArrayList<Font>();
		for (PhysicalFont pf : fontMapper.getLastResortFallbacks().values()) {
			if (pf.getEmbeddedURI()==null) continue;
			if (Docx4jProperties.getProperty("docx4j.fonts.fop.util.FopConfigUtil.simulate-style", true)) {
				createFontEntrySimulateStyles(fontMapper, fontEntries, pf.getName(), pf);
			} else {
				createFontEntry(fontMapper, fontEntries, pf.getName(), pf);
			}
		}
		/* A fallback is commonly the same file as a font the document already
		 * names (Times New Roman and the Tinos Regular fallback are one file),
		 * and until 17.0.5 it was then dropped, leaving RunFontSelector's
		 * font-family undeclared: FOP reported "Font Tinos Regular,normal,400
		 * not found. Substituting with any" and used a default font.  Its
		 * triplets go onto the existing declaration instead. */
		for (Font entry : mergeByEmbedUrl(fontEntries)) {
			// the substitutes the coverage pass reaches for are declared here, and the
			// CJK ones are exactly the fonts whose layout tables cost the text layer
			if (mustNotUseOpenTypeLayout(entry)) {
				entry.setAdvanced(Boolean.FALSE);
			}
			Font existing = find(renderer.getFonts().getFont(), entry, false);
			if (existing==null) {
				renderer.getFonts().getFont().add(entry);
				if (!kerning()) {
					renderer.getFonts().getFont().add(kernedTwin(entry));
				}
			} else {
				mergeTriplets(existing, entry);
				if (Boolean.FALSE.equals(entry.isAdvanced())) {
					existing.setAdvanced(Boolean.FALSE);   // one file, one answer
				}
				if (!kerning()) {
					Font twin = find(renderer.getFonts().getFont(), entry, true);
					if (twin==null) {
						renderer.getFonts().getFont().add(kernedTwin(entry));
					} else {
						mergeTriplets(twin, kernedTwin(entry));
					}
				}
			}
			Font noLiga = noLigaTwin(entry);
			if (noLiga!=null) {
				Font existingNoLiga = find(renderer.getFonts().getFont(), noLiga, false);
				if (existingNoLiga==null) {
					renderer.getFonts().getFont().add(noLiga);
				} else {
					mergeTriplets(existingNoLiga, noLiga);
				}
			}
		}
	}

	/**
	 * The names a metafile's SVG can still carry when nothing resolved its face, mapped
	 * to the document font whose substitute should draw them.
	 *
	 * <p>Batik writes the AWT font's family into the SVG.  Where the metafile names a
	 * face neither the mapper nor AWT knows - a GDI stock name like "System", or a font
	 * this machine simply has not got - AWT's family is {@code Dialog}, and where POI's
	 * own guard replaces that it is {@code SansSerif}; a face whose AWT <i>name</i> is
	 * "Symbol" comes out as {@code 'WingDings'}, which is Batik's own logical mapping.
	 * None of those is a font FOP has been told about, so it drew them in one of its
	 * base-14 fonts - which the PDF names and does not embed.</p>
	 *
	 * @since 17.1.1
	 */
	private static final String[][] SVG_LOGICAL_FAMILIES = {
		{ "Dialog", "Arial" },
		{ "SansSerif", "Arial" },
		{ "Serif", "Times New Roman" },
		{ "Monospaced", "Courier New" },
	};

	/**
	 * Declare those names, so that whatever a metafile's SVG asks for is a font the PDF
	 * embeds.  Belt and braces: the pathway resolves the face through the mapper first
	 * (Docx4jDrawFontManager), and this is for what it could not.
	 *
	 * @since 17.1.1
	 */
	private static void declareSvgLogicalFamilies(Renderer renderer, Mapper fontMapper) {

		for (String[] pair : SVG_LOGICAL_FAMILIES) {
			declareAlias(renderer, fontMapper, pair[0], fontMapper.get(pair[1]));
		}
		// Batik's logical mapping for an AWT font named "Symbol"
		declareAlias(renderer, fontMapper, "WingDings", PhysicalFonts.getWDingsFont());
	}

	/** {@code family} as a name for this face and the mapper's bold/italic forms of it. */
	private static void declareAlias(Renderer renderer, Mapper fontMapper, String family, PhysicalFont pf) {

		if (pf==null || pf.getEmbeddedURI()==null || family==null || family.length()==0) return;

		List<Font> entries = new ArrayList<Font>();
		aliasEntry(entries, pf, family, "normal", "normal");
		PhysicalFont bold = fontMapper.getBoldForm(pf.getName(), pf);
		aliasEntry(entries, bold!=null ? bold : pf, family, "normal", "bold");
		PhysicalFont italic = fontMapper.getItalicForm(pf.getName(), pf);
		aliasEntry(entries, italic!=null ? italic : pf, family, "italic", "normal");
		PhysicalFont boldItalic = fontMapper.getBoldItalicForm(pf.getName(), pf);
		aliasEntry(entries, boldItalic!=null ? boldItalic : (italic!=null ? italic : pf),
				family, "italic", "bold");

		for (Font entry : mergeByEmbedUrl(entries)) {
			Font existing = find(renderer.getFonts().getFont(), entry, null);
			if (existing==null) {
				renderer.getFonts().getFont().add(entry);
			} else {
				mergeTriplets(existing, entry);
			}
		}
	}

	private static void aliasEntry(List<Font> entries, PhysicalFont pf, String family,
			String style, String weight) {
		if (pf==null || pf.getEmbeddedURI()==null) return;
		Font entry = factory.createFontsFont();
		entry.setSimulateStyle(false);
		entry.setKerning(kerning());
		if (pf.getEmbedFontInfo()!=null && pf.getEmbedFontInfo().getSubFontName()!=null) {
			entry.setSubFont(pf.getEmbedFontInfo().getSubFontName());
		}
		entry.setEmbedUrl(pf.getEmbeddedURI().toString());
		entry.getFontTriplet().add(createFontTriplet(family, style, weight));
		entries.add(entry);
	}

	/**
	 * One {@code <font>} per (embed-url, sub-font, simulate-style) declaration, carrying
	 * every triplet declared for it: FOP looks a font up by triplet, so two document
	 * fonts sharing a file need both names on the one declaration.
	 *
	 * @since 17.0.5
	 */
	private static List<Font> mergeByEmbedUrl(List<Font> entries) {
		List<Font> merged = new ArrayList<Font>();
		for (Font f : entries) {
			Font existing = find(merged, f, null);
			if (existing==null) {
				merged.add(f);
			} else {
				mergeTriplets(existing, f);
			}
		}
		return merged;
	}

	/** The entry of the same file, sub-font and simulate-style as {@code like};
	 *  {@code kerned} null matches either, true or false the entry's kerning. */
	private static Font find(List<Font> entries, Font like, Boolean kerned) {
		for (Font f : entries) {
			if (!eq(f.getEmbedUrl(), like.getEmbedUrl())) continue;
			if (!eq(f.getSubFont(), like.getSubFont())) continue;
			if (f.isSimulateStyle()!=like.isSimulateStyle()) continue;
			if (!eq(f.getEncodingMode(), like.getEncodingMode())) continue; // the +noliga twin is its own declaration
			if (kerned!=null && kerned.booleanValue()!=Boolean.TRUE.equals(f.isKerning())) continue;
			return f;
		}
		return null;
	}

	private static boolean eq(String a, String b) {
		return a==null ? b==null : a.equals(b);
	}

	/** Add the triplets {@code into} does not have yet. */
	private static void mergeTriplets(Font into, Font from) {
		for (org.docx4j.convert.out.fopconf.Fonts.Font.FontTriplet t : from.getFontTriplet()) {
			boolean present = false;
			for (org.docx4j.convert.out.fopconf.Fonts.Font.FontTriplet e : into.getFontTriplet()) {
				if (eq(e.getName(), t.getName()) && eq(e.getStyle(), t.getStyle())
						&& eq(e.getWeight(), t.getWeight())) {
					present = true;
					break;
				}
			}
			if (!present) into.getFontTriplet().add(t);
		}
	}

}
