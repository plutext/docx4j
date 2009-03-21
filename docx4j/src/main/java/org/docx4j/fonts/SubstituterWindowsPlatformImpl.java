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

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.FontCache;
import org.apache.fop.fonts.FontResolver;
import org.apache.fop.fonts.FontSetup;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.autodetect.FontFileFinder;
import org.apache.fop.fonts.autodetect.FontInfoFinder;
import org.apache.log4j.Logger;
import org.docx4j.fonts.microsoft.MicrosoftFonts;
import org.docx4j.fonts.substitutions.FontSubstitutions;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart;
import org.docx4j.wml.Fonts;

import com.lowagie.text.pdf.BaseFont;

/**
 * 
 * Generate a map, mapping each Microsoft font name to:
 * 1. a substitute font which can be embedded in a PDF document
 * 2. a font list suitable for an HTML document (which is not then going to be transformed to PDF!)
 * 
 * Docx4all makes a corresponding AWT fonts directly from the substitute font.
 * 
 * @author jharrop
 *
 */
public class SubstituterWindowsPlatformImpl extends Substituter {
	
	
	protected static Logger log = Logger.getLogger(SubstituterWindowsPlatformImpl.class);

	public SubstituterWindowsPlatformImpl() {
		super();
		
		//log.debug(System.getProperty("os.name")); // eg Linux
		//log.debug(System.getProperty("os.arch")); // eg i386
		
		if (System.getProperty("os.name").toLowerCase().indexOf("windows")<0) {
			log.error("WARNING! SubstituterWindowsPlatformImpl works best " +
					"on Windows.  To get good results on other platforms, you'll probably  " +
					"need to have installed Windows fonts.");
		}
		
	}
	
	
	
	private final static HashMap<String, MicrosoftFonts.Font> msFontsFilenames;
	private final static HashMap<String, String> filenamesToMsFontNames;
	public final static Map<String, MicrosoftFonts.Font> getMsFontsFilenames() {
		return msFontsFilenames;
	}		
		
    
	
	static {
		
		try {
			// Microsoft Fonts
			// 1. On Microsoft platform, to embed in PDF output
			// 2. docx4all - all platforms - to populate font dropdown list
			msFontsFilenames = new HashMap<String, MicrosoftFonts.Font>();
			filenamesToMsFontNames = new HashMap<String, String>();
			setupMicrosoftFontFilenames();
			
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}
	
	/**
	 * Get Microsoft fonts
	 * We need these:
	 * 1. On Microsoft platform, to embed in PDF output
	 * 2. docx4all - all platforms - to populate font dropdown list */	
	private final static void setupMicrosoftFontFilenames() throws Exception {
				
		JAXBContext msFontsContext = JAXBContext.newInstance("org.docx4j.fonts.microsoft");		
		Unmarshaller u = msFontsContext.createUnmarshaller();		
		u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

		log.info("unmarshalling fonts.microsoft \n\n" );									
		// Get the xml file
		java.io.InputStream is = null;
		// Works in Eclipse - note absence of leading '/'
		is = org.docx4j.utils.ResourceUtils.getResource("org/docx4j/fonts/microsoft/MicrosoftFonts.xml");
					
		org.docx4j.fonts.microsoft.MicrosoftFonts msFonts = (org.docx4j.fonts.microsoft.MicrosoftFonts)u.unmarshal( is );
		
		List<MicrosoftFonts.Font> msFontsList = msFonts.getFont();
		
		for (MicrosoftFonts.Font font : msFontsList ) {			
			msFontsFilenames.put( (font.getName()), font); // 20080318 - normalised
			//log.debug( "put msFontsFilenames: " + normalise(font.getName()) );
			
			filenamesToMsFontNames.put( font.getFilename().toLowerCase() , font.getName());
//			log.debug( "put msFontsFilenames: " + font.getName() );
			
			if (font.getBold()!=null) {
				filenamesToMsFontNames.put( font.getBold().getFilename().toLowerCase(), font.getName()+SEPARATOR+Substituter.BOLD);
//				log.debug( "put bold: " +  font.getName()+SEPARATOR+Substituter.BOLD );				
			}
			if (font.getItalic()!=null) {
				filenamesToMsFontNames.put( font.getItalic().getFilename().toLowerCase(), font.getName()+SEPARATOR+Substituter.ITALIC);
//				log.debug( "put italic: " + font.getName()+SEPARATOR+Substituter.ITALIC );				
			}
			if (font.getBolditalic() !=null) {
				filenamesToMsFontNames.put( font.getBolditalic().getFilename().toLowerCase(), font.getName()+SEPARATOR+Substituter.BOLD_ITALIC);
//				log.debug( "put bold italic: " + font.getName()+SEPARATOR+Substituter.BOLD_ITALIC );				
			}
			
		}
		
	}
	
 
	
	
	
	/**
	 * Populate the fontMappings object. We make an entry for each
	 * of the documentFontNames.
	 * 
	 * @param documentFontNames - the fonts used in the document
	 * @param wmlFonts - the content model for the fonts part
	 * @throws Exception
	 */
	public void populateFontMappings(Map documentFontNames, org.docx4j.wml.Fonts wmlFonts ) throws Exception {
				
		/* org.docx4j.wml.Fonts fonts is obtained as follows:
		 * 
		 *     FontTablePart fontTablePart= wordMLPackage.getMainDocumentPart().getFontTablePart();
		 *     org.docx4j.wml.Fonts fonts = (org.docx4j.wml.Fonts)fontTablePart.getJaxbElement();
		 *     
		 * If the document doesn't have a font table, 
		 *     
		 *		org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart fontTable 
		 *			= new org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart();
		 *		fontTable.unmarshalDefaultFonts();
		 */ 
		
		//  We need to make a map out of it.
//		List<Fonts.Font> fontList = wmlFonts.getFont();
//		Map<String, Fonts.Font> fontsInFontTable = new HashMap<String, Fonts.Font>();
//		for (Fonts.Font font : fontList ) {
//			fontsInFontTable.put( normalise(font.getName()), font );
//		}
		

		Iterator documentFontMapIterator = documentFontNames.entrySet().iterator();
	    while (documentFontMapIterator.hasNext()) {
	        Map.Entry pairs = (Map.Entry)documentFontMapIterator.next();
	        
	        if(pairs.getKey()==null) {
	        	log.info("Skipped null key");
	        	pairs = (Map.Entry)documentFontMapIterator.next();
	        }
	        
	        String documentFontname = (String)pairs.getKey();
	        log.debug("Document font: " + documentFontname);
	        
	        if ( PhysicalFonts.getPhysicalFonts().get(documentFontname)!=null ) {
	        	
	        	// An identity mapping; that is all
	        	// this class knows how to do!
        		fontMappings.put(documentFontname, 
        				new FontMapping(documentFontname, 
        						PhysicalFonts.getPhysicalFonts().get(documentFontname) ) );	        		        	
	        } else {
	        	
	        	log.warn("- - No physical font for: " + documentFontname);
	        }
	    }	        	
		
		
//		// Iterate through the physical fonts, since their key is their 
//		// postscript name (non-normalised).  This way, we can do a single
//		// pass.
//		Iterator physicalFontMapIterator = physicalFontMap.entrySet().iterator();
//	    while (physicalFontMapIterator.hasNext()) {
//	        Map.Entry pairs = (Map.Entry)physicalFontMapIterator.next();
//	        
//	        if(pairs.getKey()==null) {
//	        	log.info("Skipped null key");
//	        	pairs = (Map.Entry)physicalFontMapIterator.next();
//	        }
//		
//	        String physicalFontName = (String)pairs.getKey();
//			//log.debug("\n\n" + physicalFontName);	        
//	        //String normalisedFontName = normalise(physicalFontName);
//			String fontPath = ((PhysicalFont)pairs.getValue()).getEmbeddedFile();
//			String physicalFilename = fontPath.substring( fontPath.lastIndexOf("/") +1).toLowerCase();
//			
//			String msFontName = filenamesToMsFontNames.get(physicalFilename);
//			
//			if (msFontName!=null) {
//			
////				String msFontName = font.getName(); 
//				
//				String baseform = msFontName;
//				if (msFontName.indexOf(SEPARATOR)>0) {
//					baseform = msFontName.substring(0, msFontName.indexOf(SEPARATOR));
//				}
//				
//		        // Add it to the mapping if it is present in the document
//		        if ( documentFontNames.get(baseform)!=null ) {
//		        	
//					// for now, if the baseform is used, 
//		        	// we say bold, italic, and bolditalic are as well
//		        	
//		        	if (pairs.getValue()==null) {
//		        		log.debug("Handle that");
//		        	} else {
//		        		fontMappings.put((msFontName), 
//			        			new FontMapping(msFontName, (PhysicalFont)pairs.getValue() ) );
//		        		log.info("Added mapping for: " + (msFontName));		        		
//		        	}
//		        	
//		        } else {
//		        	log.debug("Ignoring physical font " + msFontName
//		        			+ ((PhysicalFont)pairs.getValue()).getEmbeddedFile() );
//		        	
//		        }
//			} else {
//				
//				log.info("Unknown font: " + physicalFontName + "(" + physicalFilename);
//				
//			}
//				
//	    }
	        
	}


//	public static class PhysicalFontFamily {
//
//		String familyName; // For example: Times New Roman
//		public String getFamilyName() {
//			return familyName;
//		}
//
//		PhysicalFontFamily(String familyName) {
//			this.familyName = familyName;
//		}
//
//		// We want this, so that when were are searching panose space
//		// for bold, bolditalic, italic, we can restrict the search
//		// to this list
//		Map<String, PhysicalFont> physicalFonts = new HashMap<String, PhysicalFont> ();
//		void addFont(PhysicalFont physicalFont){
//			physicalFonts.put(physicalFont.getName(), physicalFont);
//		}
//		
//		Map<String, PhysicalFont> getPhysicalFonts() {
//			return physicalFonts;
//		}
//		
//	}
	
	
	public static void main(String[] args) throws Exception {

		//String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/Word2007-fonts.docx";
//		String inputfilepath = "C:\\Documents and Settings\\Jason Harrop\\workspace\\docx4j-2009\\sample-docs\\Word2007-fonts.docx";
		//String inputfilepath = "/home/jharrop/workspace200711/docx4j-001/sample-docs/fonts-modesOfApplication.docx";
		//String inputfilepath = "/home/jharrop/workspace200711/docx4all/sample-docs/TargetFeatureSet.docx"; //docx4all-fonts.docx";
		String inputfilepath = "C:\\Documents and Settings\\Jason Harrop\\My Documents\\Downloads\\AUMS-easy.docx";
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
				
		FontTablePart fontTablePart= wordMLPackage.getMainDocumentPart().getFontTablePart();		
		org.docx4j.wml.Fonts fonts = (org.docx4j.wml.Fonts)fontTablePart.getJaxbElement();		
	
		SubstituterWindowsPlatformImpl s = new SubstituterWindowsPlatformImpl();
				
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
