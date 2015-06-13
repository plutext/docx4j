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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.docx4j.Docx4jProperties;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.fonts.fop.fonts.FontTriplet;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.utils.ResourceUtils;

/**
 * The sole role of this class is to create an avalon configuration
 * (as a String)
 * which can be used to configure FOP 1.1 and earlier,
 * or FOP post 1.1
 * 
 * @author jharrop
 *
 */
public class FopConfigUtil {
	
	protected static Logger log = LoggerFactory.getLogger(FopConfigUtil.class);
	
	private static final String substitutions;
	
	static {
		
		String substitutionsPath = Docx4jProperties.getProperty("docx4j.fonts.fop.util.FopConfigUtil.substitutions");
		String substitutionsTmp;
		if (substitutionsPath==null) {
			substitutionsTmp="";
		} else {
			try {
				substitutionsTmp=IOUtils.toString(ResourceUtils.getResource(substitutionsPath));
			} catch (IOException e) {
				log.error("Problems with class path resource " + substitutionsPath);
				log.error(e.getMessage(), e);
				substitutionsTmp="";
			}
		}
		substitutions = substitutionsTmp;
	}

	public static String createDefaultConfiguration(Mapper fontMapper, Set<String> fontsInUse) throws Docx4JException {
//  public static Configuration createDefaultConfiguration(Mapper fontMapper, Map<String, String> fontsInUse) throws Docx4JException {
		
		// This method now returns a String, since that works best
		// with FOP going forward.  See comments in FopFactoryUtil. 
		
		StringBuilder buffer = new StringBuilder(10240);

		buffer.append("<fop version=\"1.0\"><strict-configuration>true</strict-configuration>");
		if (substitutions.length()>0) {
			buffer.append("<fonts>");
			buffer.append(substitutions);
			buffer.append("</fonts>");			
		}
		buffer.append("<renderers><renderer mime=\"application/pdf\">");
		buffer.append("<fonts>");
		declareFonts(fontMapper, fontsInUse, buffer);
		buffer.append("</fonts></renderer></renderers></fop>");
		
		if (log.isDebugEnabled()) {
			log.debug("\nUsing fop config:\n " + buffer.toString() + "\n");
		}

		// See FOP's PrintRendererConfigurator
		// String myConfig = "<fop
		// version=\"1.0\"><strict-configuration>true</strict-configuration>"
		// +
		// "<renderers><renderer mime=\"application/pdf\">" +
		// "<fonts><directory
		// recursive=\"true\">C:\\WINDOWS\\Fonts</directory>" +
		// "<auto-detect/>" +
		// "</fonts></renderer></renderers></fop>";

		return buffer.toString();
	}

	/**
	 * Create a FOP font configuration for each font used in the
	 * document.
	 * 
	 * @return
	 */
	protected static void declareFonts(Mapper fontMapper, Set<String> fontsInUse, StringBuilder result) {

		
		for (String fontName : fontsInUse) {		    
		    
		    PhysicalFont pf = fontMapper.get(fontName);
		    String subFontAtt = "";
		    
		    if (pf==null) {
		    	log.warn("Document font " + fontName + " is not mapped to a physical font!");
		    	// We may still have eg Cambria-bold embedded
		    } else {
		    
			    if (pf.getEmbedFontInfo().getSubFontName()!=null)
			    	subFontAtt= " sub-font=\"" + pf.getEmbedFontInfo().getSubFontName() + "\"";
			    
			    result.append("<font embed-url=\"" +pf.getEmbeddedFile() + "\""+ subFontAtt +">" );
			    	// now add the first font triplet
				    FontTriplet fontTriplet = (FontTriplet)pf.getEmbedFontInfo().getFontTriplets().get(0);
				    addFontTriplet(result, fontTriplet);
			    result.append("</font>" );
		    }
		    
		    // bold, italic etc
		    PhysicalFont pfVariation = fontMapper.getBoldForm(fontName, pf);
		    if (pfVariation==null) {
		    	log.debug(fontName + " no bold form");
		    } else {
			    result.append("<font embed-url=\"" +pfVariation.getEmbeddedFile() + "\""+ subFontAtt +">" );
		    	addFontTriplet(result, pf.getName(), "normal", "bold");
			    result.append("</font>" );
		    }
		    pfVariation = fontMapper.getBoldItalicForm(fontName, pf);
		    if (pfVariation==null) {
		    	log.debug(fontName + " no bold italic form");
		    } else {
			    result.append("<font embed-url=\"" +pfVariation.getEmbeddedFile() + "\""+ subFontAtt +">" );
		    	addFontTriplet(result, pf.getName(), "italic", "bold");
			    result.append("</font>" );
		    }
		    pfVariation = fontMapper.getItalicForm(fontName, pf);
		    if (pfVariation==null) {
		    	log.debug(fontName + " no italic form");
		    } else {
			    result.append("<font embed-url=\"" +pfVariation.getEmbeddedFile() + "\""+ subFontAtt +">" );
		    	addFontTriplet(result, pf.getName(), "italic", "normal");
			    result.append("</font>" );
		    }
			    
		}
	}
		
	protected static void addFontTriplet(StringBuilder result, FontTriplet fontTriplet) {
		addFontTriplet(result, fontTriplet.getName(), 
							   fontTriplet.getStyle(), 
							   weightToCSS2FontWeight(fontTriplet.getWeight()));
	}
	
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
