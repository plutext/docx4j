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
			log.warn("WARNING! SubstituterWindowsPlatformImpl works best " +
					"on Windows.  To get good results on other platforms, you'll probably  " +
					"need to have installed Windows fonts.");
		}
		
	}
	
	static {
		
		try {
			
			PhysicalFonts.discoverPhysicalFonts();
			
		} catch (Exception exc) {
			throw new RuntimeException(exc);
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
	public void populateFontMappings(Set<String> documentFontNames, org.docx4j.wml.Fonts wmlFonts ) throws Exception {
		
		// documentFontNames comes from MDP's fontsInUse()
		// which contains getPropertyResolver().getFontnameFromStyle
				
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
		

		for( String documentFontname : documentFontNames) {
	        log.debug("Document font: " + documentFontname);
	        PhysicalFont mappedTo = PhysicalFonts.get(documentFontname);
	        if ( mappedTo!=null ) {
	        	
	        	// An identity mapping; that is all
	        	// this class knows how to do!
        		put(documentFontname,         				 
        				mappedTo );	
        			log.debug(".. mapped to " + mappedTo.getName() );
	        } else if (regularForms.get(documentFontname)!=null) {
        		put(documentFontname,         				 
        				regularForms.get(documentFontname) );	
        			log.debug(".. mapped to embedded regular form " );
	        } else if (boldForms.get(documentFontname)!=null) {
        		put(documentFontname,         				 
        				boldForms.get(documentFontname) );	
        			log.debug(".. mapped to embedded bold form " );
	        } else if (italicForms.get(documentFontname)!=null) {
        		put(documentFontname,         				 
        				italicForms.get(documentFontname) );	
        			log.debug(".. mapped to embedded italic form " );
	        } else if (boldItalicForms.get(documentFontname)!=null) {
        		put(documentFontname,         				 
        				boldItalicForms.get(documentFontname) );	
        			log.debug(".. mapped to embedded bold italic form " );
	        	
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
