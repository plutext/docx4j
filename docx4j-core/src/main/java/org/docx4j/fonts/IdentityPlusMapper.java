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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.Docx4jProperties;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;

/**
 *
 * Maps font names used in the document to 
 * corresponding fonts physically available
 * on the system.
 * 
 * This mapper automatically maps
 * document fonts for which the exact
 * font is physically available.  Think
 * of this as an identity mapping.  For 
 * this reason, it will work best on 
 * Windows, or a system on which 
 * Microsoft fonts have been installed.
 * 
 * You can manually add your own
 * additional mappings if you wish. 
 * 
 * @author jharrop
 *
 */
public class IdentityPlusMapper extends Mapper {
	
	
	protected static Logger log = LoggerFactory.getLogger(IdentityPlusMapper.class);

	public IdentityPlusMapper() {
		super();
		
		//log.debug(System.getProperty("os.name")); // eg Linux
		//log.debug(System.getProperty("os.arch")); // eg i386
		
		if (System.getProperty("os.name").toLowerCase().indexOf("windows")<0) {
			log.warn("WARNING! IdentityPlusMapper works best " +
					"on Windows.  To get good results on other platforms, you may  " +
					"need to have installed Windows fonts.");
			// but since 11.5.9, see addMetricallyCompatibleSubstitutes()
		}
		
	}
	
	static {
		
		try {
			
			// @since 11.5.8 symbol fonts from docx4j-export-fo-fonts-symbol jar
			int count = PhysicalFonts.discoverJarFonts("fonts-symbol");
			log.info("Found " + count + " docx4j symbol fonts.");
			
			if (Docx4jProperties.getProperty("docx4j.fonts.discoverJarFonts.enabled", true)) {
				PhysicalFonts.discoverJarFonts();
			}

			if (Docx4jProperties.getProperty("docx4j.fonts.discoverPhysicalFonts.enabled", true)) {
				PhysicalFonts.discoverPhysicalFonts();
			}
			
			PhysicalFonts.fontCache.save();
			
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}

	
	/**
	 * A variant of the name: "Noto Sans Symbols" is registered as "Noto Sans Symbols
	 * Regular"; a family with no plain face is asked for in the order regular, bold,
	 * italic, bold italic (until 17.1.1 italic came before bold, so "Franklin Gothic Demi"
	 * with its regular absent was set upright in Franklin Gothic Demi Italic; Brush
	 * Script MT, Lucida Calligraphy, Vivaldi and Magneto, which have only the one face,
	 * are found either way).  Everything else - the metric clones, w:altName, a face of
	 * the same class, Word's default - is the shared passes' (see Mapper).
	 *
	 * @since 17.1.1 as this method; the same lookups were populateFontMappings' before
	 */
	@Override
	protected PhysicalFont resolveDocumentFont(String documentFontName, org.docx4j.wml.Fonts.Font fontTableEntry) {
		for (String variant : new String[] { " regular", " bold", " italic", " bold italic" }) {
			PhysicalFont pf = PhysicalFonts.get(documentFontName + variant);
			if (pf!=null) {
				log.debug(documentFontName + " .. mismatch mapped to " + documentFontName + variant);
				return pf;
			}
		}
		return null;
	}

	public static void main(String[] args) throws Exception {

		//String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/Word2007-fonts.docx";
//		String inputfilepath = "C:\\Documents and Settings\\Jason Harrop\\workspace\\docx4j-2009\\sample-docs\\Word2007-fonts.docx";
		//String inputfilepath = "/home/jharrop/workspace200711/docx4j-001/sample-docs/fonts-modesOfApplication.docx";
		//String inputfilepath = "/home/jharrop/workspace200711/docx4all/sample-docs/TargetFeatureSet.docx"; //docx4all-fonts.docx";
		String inputfilepath = "C:\\Documents and Settings\\Jason Harrop\\My Documents\\Downloads\\AUMS-easy.docx";
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
				
		FontTablePart fontTablePart= wordMLPackage.getMainDocumentPart().getFontTablePart();		
		org.docx4j.wml.Fonts fonts = (org.docx4j.wml.Fonts)fontTablePart.getJaxbElement();		
	
		IdentityPlusMapper s = new IdentityPlusMapper();
				
		///////////////
		// Go through the FontsTable, and see what we have filenames for.
//		for (Fonts.Font font : fontList ) {
//			String fontName =  font.getName();
//			MicrosoftFonts.Font msFontInfo = (MicrosoftFonts.Font)msFontsFilenames.get(fontName);
//			if (msFontInfo!=null) {
//				System.out.println( fontName + " at " + msFontInfo.getFilename() );				
//			} else {
//				System.out.println( "? " + fontName );								
//			}
//		}
		
		//panoseDebugReportOnMicrosoftFonts( fonts );
		
		s.populateFontMappings(wordMLPackage.getMainDocumentPart().fontsInUse(), fonts );
	}
	
//	private static void panoseDebugReportOnPhysicalFonts( Map<String, PhysicalFont>physicalFontMap ) {
//		Iterator fontIterator = physicalFontMap.entrySet().iterator();
//	    while (fontIterator.hasNext()) {
//	        Map.Entry pairs = (Map.Entry)fontIterator.next();
//	        
//	        if(pairs.getKey()==null) {
//	        	log.info("Skipped null key");
//	        	if (pairs.getValue()!=null) {
//	        		log.error(((PhysicalFont)pairs.getValue()).getEmbeddedFile());
//	        	}
//	        	
//	        	if (fontIterator.hasNext() ) {
//	        		pairs = (Map.Entry)fontIterator.next();
//	        	} else {
//	        		return;
//	        	}
//	        }
//	        
//	        String fontName = (String)pairs.getKey();
//
//			PhysicalFont pf = (PhysicalFont)pairs.getValue();
//			
//			org.apache.fop.fonts.Panose fopPanose = pf.getPanose();
//			
//				if (fopPanose == null ) {
//					System.out.println(fontName + " .. lacks Panose!");					
//				} else if (fopPanose!=null ) {
//					System.out.println(fontName + " .. " + fopPanose);
//				}
////				        long pd = fopPanose.difference(nfontInfo.getPanose().getPanoseArray());
////						System.out.println(".. panose distance: " + pd);					
//	    }
//	}

//	private static void panoseDebugReportOnMicrosoftFonts(org.docx4j.wml.Fonts wmlFonts ) {
//				
//		List<Fonts.Font> fontList = wmlFonts.getFont();
//		for (Fonts.Font font : fontList ) {
//			
//			org.docx4j.wml.FontPanose wmlFontPanoseForDocumentFont = 
//				wmlFontPanoseForDocumentFont = font.getPanose1();
//			
//			org.apache.fop.fonts.Panose documentFontPanose = null;
//			if (wmlFontPanoseForDocumentFont!=null && wmlFontPanoseForDocumentFont.getVal()!=null ) {
//				try {
//					documentFontPanose = org.apache.fop.fonts.Panose.makeInstance(wmlFontPanoseForDocumentFont.getVal() );
//					
//					System.out.println( font.getName() + documentFontPanose);
//					
//				} catch (IllegalArgumentException e) {					
//					log.error(e.getMessage());
//					// For example:
//					// Illegal Panose Array: Invalid value 10 > 8 in position 5 of [ 4 2 7 5 4 10 2 6 7 2 ]
//				}
//				//log.debug(".. " + fopPanose.toString() );					
//				
//			} else {
//				log.debug(".. no panose info!!!");															
//			}
//			
//	    }
//	}
	
	
}
