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
				rendererFonts.getFont().add(entry);
				if (!kerning()) {
					rendererFonts.getFont().add(kernedTwin(entry));
				}
			}			
		}
		return rendererFonts;
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
		for (org.docx4j.convert.out.fopconf.Fonts.Font.FontTriplet t : font.getFontTriplet()) {
			twin.getFontTriplet().add(createFontTriplet(t.getName() + RunFontSelector.KERNED_SUFFIX, t.getStyle(), t.getWeight()));
		}
		return twin;
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
    		
    	} else {
    		// If we don't have to simulate-style, fall back to the old way of doing things
    		rendererFont.setEmbedUrl(pf.getEmbeddedURI().toString());
    
	    	// now add the first font triplet
		    FontTriplet fontTriplet = (FontTriplet)pf.getEmbedFontInfo().getFontTriplets().get(0);
    		rendererFont.getFontTriplet().add(
    				createFontTriplet(fontTriplet.getName(), fontTriplet.getStyle(), 
    						weightToCSS2FontWeight(fontTriplet.getWeight())));
		    			    
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

		if (fopConfig==null || fontMapper==null || fontMapper.getLastResortFallbacks().isEmpty()) return;

		Renderer renderer = (fopConfig.getRenderers()==null) ? null
				: get(fopConfig.getRenderers(), "application/pdf");
		if (renderer==null) return;
		if (renderer.getFonts()==null) {
			renderer.setFonts(factory.createFonts());
		}

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
			Font existing = find(renderer.getFonts().getFont(), entry, false);
			if (existing==null) {
				renderer.getFonts().getFont().add(entry);
				if (!kerning()) {
					renderer.getFonts().getFont().add(kernedTwin(entry));
				}
				continue;
			}
			mergeTriplets(existing, entry);
			if (!kerning()) {
				Font twin = find(renderer.getFonts().getFont(), entry, true);
				if (twin==null) {
					renderer.getFonts().getFont().add(kernedTwin(entry));
				} else {
					mergeTriplets(twin, kernedTwin(entry));
				}
			}
		}
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
