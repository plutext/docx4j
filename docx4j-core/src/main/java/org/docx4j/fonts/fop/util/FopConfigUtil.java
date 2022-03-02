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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.fopconf.Fop;
import org.docx4j.convert.out.fopconf.Fop.Fonts;
import org.docx4j.convert.out.fopconf.Fop.Renderers;
import org.docx4j.convert.out.fopconf.Fop.Renderers.Renderer;
import org.docx4j.convert.out.fopconf.Substitutions;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
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
				System.out.println(o.getClass().getName());
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
		
		fopConfig.setStrictConfiguration(true);

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
		renderers.setRenderer(renderer);
		renderer.setMime("application/pdf");

		renderer.setFonts(declareRendererFonts(fontMapper, fontsInUse));
		
		return fopConfig;
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
		
		boolean haveSomeMappedPhysicalFonts = false;
		
		if (Docx4jProperties.getProperty("docx4j.fonts.fop.util.FopConfigUtil.simulate-style", false)) {
			
			for (String fontName : fontsInUse) {		    
			    
			    PhysicalFont pf = fontMapper.get(fontName);
			    
			    if (pf==null) {
			    	log.warn("Document font " + fontName + " is not mapped to a physical font!");
			    	// We may still have eg Cambria-bold embedded, but ignore this for now
			    } else {
			    	haveSomeMappedPhysicalFonts = true;
			    	
			    	org.docx4j.convert.out.fopconf.Fonts.Font rendererFont = factory.createFontsFont();
			    	rendererFont.setSimulateStyle(true);
			    	rendererFonts.getFont().add(rendererFont);
			    	
				    if (pf.getEmbedFontInfo().getSubFontName()!=null) {
				    	rendererFont.setSubFont( pf.getEmbedFontInfo().getSubFontName() );
				    }
				    	
			    	if (fontMapper.getBoldForm(fontName, pf)==null
			    			|| fontMapper.getItalicForm(fontName, pf)==null) {
			    		
			    		rendererFont.setSimulateStyle(true);
			    		rendererFont.setEmbedUrl(pf.getEmbeddedURI().toString());
			    		
			    		rendererFont.getFontTriplet().add(createFontTriplet(pf.getName(), "normal", "normal"));
			    		rendererFont.getFontTriplet().add(createFontTriplet(pf.getName(), "italic", "normal"));
			    		rendererFont.getFontTriplet().add(createFontTriplet(pf.getName(), "normal", "bold"));
			    		rendererFont.getFontTriplet().add(createFontTriplet(pf.getName(), "italic", "bold"));
			    		
			    	} else {
			    		// If we don't have to simulate-style, fall back to the old way of doing things
			    		rendererFont.setEmbedUrl(pf.getEmbeddedURI().toString());
			    
				    	// now add the first font triplet
					    FontTriplet fontTriplet = (FontTriplet)pf.getEmbedFontInfo().getFontTriplets().get(0);
			    		rendererFont.getFontTriplet().add(
			    				createFontTriplet(fontTriplet.getName(), fontTriplet.getStyle(), 
			    						weightToCSS2FontWeight(fontTriplet.getWeight())));
					    			    
					    addVariations(fontMapper, rendererFonts, fontName, pf, rendererFont.getSubFont());
			    	}
			    }
			}
			
			
		} else {

		
			for (String fontName : fontsInUse) {		    
			    
			    PhysicalFont pf = fontMapper.get(fontName);
			    
			    if (pf==null) {
			    	log.warn("Document font " + fontName + " is not mapped to a physical font!");
			    	// We may still have eg Cambria-bold embedded
			    } else {

			    	haveSomeMappedPhysicalFonts = true;

			    	org.docx4j.convert.out.fopconf.Fonts.Font rendererFont = factory.createFontsFont();
			    	rendererFont.setSimulateStyle(false);
			    	rendererFonts.getFont().add(rendererFont);
			    	
				    if (pf.getEmbedFontInfo().getSubFontName()!=null) {
				    	rendererFont.setSubFont( pf.getEmbedFontInfo().getSubFontName() );
				    }
		    		rendererFont.setEmbedUrl(pf.getEmbeddedURI().toString());
				    

		    		// now add the first font triplet
				    FontTriplet fontTriplet = (FontTriplet)pf.getEmbedFontInfo().getFontTriplets().get(0);
		    		rendererFont.getFontTriplet().add(
		    				createFontTriplet(fontTriplet.getName(), fontTriplet.getStyle(), 
		    						weightToCSS2FontWeight(fontTriplet.getWeight())));

				    addVariations(fontMapper, rendererFonts, fontName, pf, 
				    		pf.getEmbedFontInfo().getSubFontName());
			    }
			}
		}
		if (!haveSomeMappedPhysicalFonts) log.warn("No fonts configured!");
		return rendererFonts;
	}
	
	private static org.docx4j.convert.out.fopconf.Fonts.Font.FontTriplet createFontTriplet(String name, String style, String weight) {

		org.docx4j.convert.out.fopconf.Fonts.Font.FontTriplet triplet = factory.createFontsFontFontTriplet();
		triplet.setName(name);
		triplet.setStyle(style);
		triplet.setWeight(weight);
		return triplet;
	}

	private static void addVariations(Mapper fontMapper, org.docx4j.convert.out.fopconf.Fonts rendererFonts, 
			String fontName, PhysicalFont pf,
			String subFontAtt) {
		
		
		// bold, italic etc
		PhysicalFont pfVariation = fontMapper.getBoldForm(fontName, pf);
		if (pfVariation==null) {
			log.debug(fontName + " no bold form");
		} else {
	    	rendererFonts.getFont().add(createVariant(pf, pfVariation, subFontAtt, "normal", "bold"));
		}
		pfVariation = fontMapper.getBoldItalicForm(fontName, pf);
		if (pfVariation==null) {
			log.debug(fontName + " no bold italic form");
		} else {
	    	rendererFonts.getFont().add(createVariant(pf, pfVariation, subFontAtt, "italic", "bold"));
		}
		pfVariation = fontMapper.getItalicForm(fontName, pf);
		if (pfVariation==null) {
			log.debug(fontName + " no italic form");
		} else {
	    	rendererFonts.getFont().add(createVariant(pf, pfVariation, subFontAtt, "italic", "normal"));
		}
	}
		
	private static org.docx4j.convert.out.fopconf.Fonts.Font createVariant(PhysicalFont pf, PhysicalFont pfVariation ,
			String subFontAtt, String style, String weight) {

		org.docx4j.convert.out.fopconf.Fonts.Font rendererFont = factory.createFontsFont();
    	rendererFont.setSimulateStyle(
    			Docx4jProperties.getProperty("docx4j.fonts.fop.util.FopConfigUtil.simulate-style", false));
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
	
}
