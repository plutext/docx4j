/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.samples;


import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.FontCache;
import org.apache.fop.fonts.FontResolver;
import org.apache.fop.fonts.FontSetup;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.autodetect.FontFileFinder;
import org.apache.fop.fonts.autodetect.FontInfoFinder;
import org.docx4j.fonts.microsoft.MicrosoftFonts;
import org.docx4j.fonts.substitutions.FontSubstitutions;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.docx4j.wml.Body;
import org.docx4j.wml.Fonts;


public class FontExplorer {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String inputfilepath = "/home/jharrop/workspace200711/docx4j-001/sample-docs/Word2007-fonts.docx";
		//String inputfilepath = "C:\\Users\\jharrop\\workspace\\docx4j\\sample-docs\\Word2007-fonts.docx";
		//String inputfilepath = "/home/jharrop/workspace200711/docx4j-001/sample-docs/fonts-modesOfApplication.docx";
		//String inputfilepath = "/home/jharrop/workspace200711/docx4all/sample-docs/TargetFeatureSet.docx"; //docx4all-fonts.docx";
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		FontTablePart fontTablePart= wordMLPackage.getMainDocumentPart().getFontTablePart();
		
		
		
		// Display its contents 
		System.out.println( "\n\n OUTPUT " );
		System.out.println( "====== \n\n " );	
		
		org.docx4j.wml.Fonts fonts = (org.docx4j.wml.Fonts)fontTablePart.getJaxbElement();
		
		List<Fonts.Font> fontList = fonts.getFont();
		
		//Map m = wordMLPackage.getMainDocumentPart().fontsInUse();
				
//		walkFontsTable(fontList);
		
		////////////////////////////////////////////////////////////////////////////////////
		// Get Microsoft fonts
		// We need these:
		// 1. On Microsoft platform, to embed in PDF output
		// 2. docx4all - all platforms - to populate font dropdown list	
		
		JAXBContext msFontsContext = JAXBContext.newInstance("org.docx4j.fonts.microsoft");		
		Unmarshaller u = msFontsContext.createUnmarshaller();		
		u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

		System.out.println("unmarshalling fonts.microsoft \n\n" );									
		// Get the xml file
		java.io.InputStream is = null;
		// Works in Eclipse - note absence of leading '/'
		is = org.docx4j.utils.ResourceUtils.getResource("org/docx4j/fonts/microsoft/MicrosoftFonts.xml");
					
		org.docx4j.fonts.microsoft.MicrosoftFonts msFonts = (org.docx4j.fonts.microsoft.MicrosoftFonts)u.unmarshal( is );
		
		List<MicrosoftFonts.Font> msFontsList = msFonts.getFont();
		Map msFontsMap = createMicrosoftFontsMap(msFontsList);
		
		///////////////
		// Go through the FontsTable, and see what we have filenames for.
//		for (Fonts.Font font : fontList ) {
//			String fontName =  font.getName();
//			MicrosoftFonts.Font msFontInfo = (MicrosoftFonts.Font)msFontsMap.get(fontName);
//			if (msFontInfo!=null) {
//				System.out.println( fontName + " at " + msFontInfo.getFilename() );				
//			} else {
//				System.out.println( "? " + fontName );								
//			}
//		}

		////////////////////////////////////////////////////////////////////////////////////
		// Get candidate substitutions 
		// On a non-MS platform, we need these for two things:
		// 1.  to embed this font in the PDF output, in place of MS font
		// 2.  in docx4all, use in editor
		
		JAXBContext substitutionsContext = JAXBContext.newInstance("org.docx4j.fonts.substitutions");		
		Unmarshaller u2 = substitutionsContext.createUnmarshaller();		
		u2.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

		System.out.println("unmarshalling fonts.substitutions \n\n" );									
		// Get the xml file
		java.io.InputStream is2 = null;
		// Works in Eclipse - note absence of leading '/'
		is2 = org.docx4j.utils.ResourceUtils.getResource("org/docx4j/fonts/substitutions/FontSubstitutions.xml");
					
		org.docx4j.fonts.substitutions.FontSubstitutions fs = (org.docx4j.fonts.substitutions.FontSubstitutions)u2.unmarshal( is2 );
		
		List<FontSubstitutions.Replace> replaceList = fs.getReplace();
		Map replaceMap = createFontSubstitutionsMap(replaceList);
		

		// Try to use AWT to get the filename of the actual substitutions.  Can it be made to work?
		// filenameViaAWT(fontList, replaceMap);
		
		// Use FOP
		Map physicalFontMap = new HashMap();
        FontResolver fontResolver = FontSetup.createMinimalFontResolver();
        FontCache fontCache = new FontCache();
        
        FontFileFinder fontFileFinder = new FontFileFinder();
        List fontFileList = fontFileFinder.find();
		
        for (Iterator iter = fontFileList.iterator(); iter.hasNext();) {
            URL fontUrl = (URL)iter.next();
            // parse font to ascertain font info
            FontInfoFinder finder = new FontInfoFinder();
            EmbedFontInfo fontInfo = finder.find(fontUrl, fontResolver, fontCache);
            if (fontInfo != null) {
            
            	for (Iterator iterIn = fontInfo.getFontTriplets().iterator() ; iterIn.hasNext();) {
            		FontTriplet triplet = (FontTriplet)iterIn.next(); 
            		
            		// TODO - make value an EmbedFontInfo object;
            		// Add Panose to EmbedFontInfo .. FontInfoFinder will get this from CustomFont; TTFFontLoader has to set it.
            		
                	physicalFontMap.put(normalise(triplet.getName()), fontInfo );
//                	physicalFontMap.put(normalise(triplet.getName()), fontInfo.getEmbedFile() );
                	
                	// Uncomment this to see ...
            		//System.out.println("Added " + triplet.getName() + " -> " + fontInfo.getEmbedFile());
            		
            		// Which have PANOSE info?
					if (fontInfo.getPanose()!=null) {
						System.out.println(fontInfo.getPanose().toString() + " -> " + triplet.getName() + " -> " + fontInfo.getEmbedFile() );
					} else {
						System.out.println("? ? ? ? ? "  + " -> " + triplet.getName() + " -> " + fontInfo.getEmbedFile() );								
					}
            		
            	}
            	
            }
        }
        
		/////////////////
		// Go through FontsTable, and see which ones we can render!
		for (Fonts.Font font : fontList ) {
			String fontName =  font.getName();
			
			System.out.println("\n\n" + fontName);
			org.docx4j.wml.FontPanose panose = font.getPanose1();
			org.apache.fop.fonts.Panose fopPanose = null;
			if (panose!=null && panose.getVal()!=null ) {
				fopPanose = new org.apache.fop.fonts.Panose(panose.getVal() );
				System.out.println(".. " + fopPanose.toString() );					
				
			} else {
				System.out.println(".. no panose info!!!");															
			}
			
			// First, is the actual font available?
			if (physicalFontMap.get(normalise(fontName)) != null) {
				System.out.println(fontName + " --> NATIVE");
				
				// sanity check using Panose (since 
				// a font could conceivably have the same name
				// but quite different content)				
				EmbedFontInfo nfontInfo = ((EmbedFontInfo)physicalFontMap.get(normalise(fontName)));
				if (nfontInfo.getPanose() == null ) {
					System.out.println(".. and lacking Panose!");					
				} else if (fopPanose!=null ) {
				        long pd = fopPanose.difference(nfontInfo.getPanose().getPanoseArray());
						System.out.println(".. panose distance: " + pd);					
				}
				
				continue;
			} 


			// Second, what about a panose match?
			String panoseKey = null;
			if (fopPanose!=null ) {
				
				if (!org.apache.fop.fonts.Panose.validPanose(panose.getVal())) {
					System.out.println("INVALID !");					
				}
								
				System.out.println(" --> " + fopPanose);				
				
				// Logic to search panose space for closest matching physical 
				// font file
				Iterator it = physicalFontMap.entrySet().iterator();
				long bestPanoseMatchValue = -1;		
				String matchingPanoseString = null;
			    while (it.hasNext()) {
			        Map.Entry pairs = (Map.Entry)it.next();
			        			        
			        EmbedFontInfo fontInfo = (EmbedFontInfo)pairs.getValue();
			        
			        if (fontInfo.getPanose() == null ) {			        	
			        	continue;
			        }
			        
			        long panoseMatchValue = fopPanose.difference(fontInfo.getPanose().getPanoseArray());
			        
			        if (bestPanoseMatchValue==-1 || panoseMatchValue < bestPanoseMatchValue ) {
			        	
			        	bestPanoseMatchValue = panoseMatchValue;
			        	matchingPanoseString = fontInfo.getPanose().toString();
			        	panoseKey = (String)pairs.getKey();
			        	
			        	//System.out.println("Candidate " + panoseMatchValue + "  (" + panoseKey + ") " + matchingPanoseString);
			        	
			        	if (bestPanoseMatchValue==0) {
			        		
			        		// Can't do any better than this!
			        		continue;
			        	}
			        	
			        	
			        } else {
			        	//System.out.println("not small " + panoseMatchValue + "  " + fontInfo.getPanose().toString() );
			        	
			        }
			    }

				if (panoseKey!=null && bestPanoseMatchValue < org.apache.fop.fonts.Panose.MATCH_THRESHOLD) {
					System.out.println("MATCHED " + panoseKey);									
					System.out.println(" --> " + matchingPanoseString + " distance " + bestPanoseMatchValue);
					
					System.out.println(fontName + " --> " + ((EmbedFontInfo)physicalFontMap.get(panoseKey)).getEmbedFile() );
					
					// Out of interest, is this match in font substitutions table?
					FontSubstitutions.Replace rtmp = (FontSubstitutions.Replace) replaceMap.get(normalise(fontName));
					if (rtmp!=null && rtmp.getSubstFonts()!=null) {
						if (rtmp.getSubstFonts().contains(panoseKey) ) {
							System.out.println("(consistent with explicit substitutes)");
						} else {
							System.out.println("(lucky, since this is missing from explicit substitutes)");							
						}
						
					}
					
					
					continue; // we're done
				} 
				
				
			} else {
				System.out.println(" --> null Panose");				
			}
			

			// Finally, try explicit font substitutions
			// - most likely to be useful for a font that doesn't have panose entries
			System.out.println("So try explicit font substitutions table");				
			FontSubstitutions.Replace replacement = (FontSubstitutions.Replace) replaceMap
					.get(normalise(fontName));
			if (replacement != null) {
				// System.out.println( "\n" + fontName + " found." );
				// String subsFonts = replacement.getSubstFonts();

				// Is there anything in subsFonts we can use?
				String[] tokens = replacement.getSubstFonts().split(";");
				boolean found = false;
				for (int x = 0; x < tokens.length; x++) {
					// System.out.println(tokens[x]);
					if (physicalFontMap.get(tokens[x]) != null) {
						
						EmbedFontInfo embedFontInfo = (EmbedFontInfo)physicalFontMap.get(tokens[x]);
						
						
						String physicalFontFile = embedFontInfo.getEmbedFile();
						
						System.out.println(fontName + " --> "
								+ physicalFontFile);
						
						found = true;
						
						// Out of interest, does this have a Panose value?
						// And what is the distance?
						if (embedFontInfo.getPanose() == null ) {
							System.out.println(".. as expected, lacking Panose");					
						} else if (fopPanose!=null  ) {
						        long pd = fopPanose.difference(embedFontInfo.getPanose().getPanoseArray());
						        
						        if (pd >= org.apache.fop.fonts.Panose.MATCH_THRESHOLD) {						        
						        	System.out.println(".. with a panose distance exceeding threshold: " + pd);
						        } else {
						        	// Sanity check
						        	System.out.println(".. with a low panose distance (! How did we get here?) : " + pd);						        	
						        }
								
						}						
						
						break;
					} else {
						// System.out.println("no match on token " + x + ":"
						// + tokens[x]);
					}

				}

				if (!found) {
					System.out.println("!  " + fontName
							+ " -->  Couldn't find any of "
							+ replacement.getSubstFonts());
				}

			} else {
				System.out.println("Nothing in FontSubstitutions.xml for: "
						+ fontName);
				System.out.println("Add the following ..");					
				System.out.println("<replace name=\"" + normalise(fontName) + "\">"
										+ "<SubstFonts>" + normalise(fontName) + "</SubstFonts>"
										//+ "<SubstFontsPS></SubstFontsPS>"
										+ "<SubstFontsHTML></SubstFontsHTML>"
										+ "<FontWeight>Normal</FontWeight>"
										+ "<FontWidth>Normal</FontWidth>"
										+ "<FontType></FontType>"
										+ "</replace>");
			}
		}
        
		//wordMLPackage.getMainDocumentPart().fontsInUse();    
        
	}

		
	/**
	 * Try to use AWT to get the filename of the actual substitutions.  Can it be made to work? 
	 * 
	 * @param fontList
	 * @param replaceMap
	 */
	private static void filenameViaAWT(List<Fonts.Font> fontList, Map replaceMap) {
		
		////////////////////////////////////////////////////////////////////////////////////
		// What fonts exist on this system?
		// - this determines the actual substitution
		
		java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
		//System.out.println(ge.getClass().getName());
		// sun.java2d.SunGraphicsEnvironment
		// on Ubuntu Gnome, sun.awt.X11GraphicsEnvironment, which extends SunGraphicsEnvironment 
		// But X11GraphicsEnvironment source code is not available.
		//((sun.awt.X11GraphicsEnvironment)ge).loadFontFiles();
		
		//java.awt.Font[] geFonts = ge.getAllFonts();
		String[] geFonts = ge.getAvailableFontFamilyNames();
		Map geFontFamilyNameMap = new HashMap();
		for (int i=0; i<geFonts.length; i++) {
			//System.out.println( geFonts[i] );
			geFontFamilyNameMap.put(normalise(geFonts[i]), geFonts[i]);
	    }
		
		
		 
//		Map fms = sun.font.FontManager.getCreatedFontFamilyNames();
//		for (java.util.Iterator it = fms.keySet().iterator(); it.hasNext();) {
//		    Object o = it.next();
//		    System.out.println( (String)o );
//		}

		System.out.println( sun.font.FontManager.getFontPath(false) );
		// returns /var/lib/defoma/fontconfig.d/N:/var/lib/defoma/fontconfig.d/T
		// :/usr/share/fonts/truetype/ttf-arabeyes:/usr/share/fonts/truetype/ttf-devanagari-fonts
		// :/usr/share/fonts/truetype/kochi:/usr/share/fonts/truetype/ttf-dejavu
		// :/var/lib/defoma/fontconfig.d/M:/usr/share/X11/fonts/Type1
		// :/var/lib/defoma/fontconfig.d/U:/var/lib/defoma/fontconfig.d/D
		// :/var/lib/defoma/fontconfig.d/B:/usr/share/fonts/truetype/freefont
		// :/usr/share/fonts/truetype/ttf-bitstream-vera etc etc etc

		
//		// Doesn't work either
//		String[] geFonts2 = sun.font.FontManager.getFontNamesFromPlatform();
//		// populateFontFileNameMap is implemented only on Windows!!!
//		for (int i=0; i<geFonts2.length; i++) {
//			System.out.println( geFonts2[i] );
//			System.out.println( sun.font.FontManager.getFileNameForFontName(geFonts2[i]) );
//	    }
		
				
		/////////////////
		// Go through FontsTable, and see which ones we can render!
		for (Fonts.Font font : fontList ) {
			String fontName =  font.getName();
			FontSubstitutions.Replace replacement= (FontSubstitutions.Replace)replaceMap.get(normalise(fontName));
			if (replacement!=null) {
				//System.out.println( "\n" + fontName + " found." );	
//				String subsFonts = replacement.getSubstFonts();
				
				// Is there anything in subsFonts we can use?
				String[] tokens = replacement.getSubstFonts().split(";");
			     for (int x=0; x<tokens.length; x++) {
			         //System.out.println(tokens[x]);
			    	 if(geFontFamilyNameMap.get(tokens[x])!=null) {
			    		 String logicalAwtFontName = (String)geFontFamilyNameMap.get(tokens[x]);
			    		 System.out.println(fontName + " --> "  +  logicalAwtFontName);
			    		 
			    		 // But how to find the corresponding physical path?
			    		 // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4752644
			    		 
			    		 //System.out.println(sun.font.FontManager.getFileNameForFontName(logicalAwtFontName) );
			    		 //System.out.println(((sun.awt.X11GraphicsEnvironment)ge).getFileNameFromPlatformName(logicalAwtFontName));
			    		 break;
			    	 } else {
			    		 //System.out.println("no match on token " + x + ":" + tokens[x]);
			    	 }

			     }
				
			} else {
				//System.out.println( "? " + fontName );								
			}
		}
		
		// http://www.java2s.com/Open-Source/Java-Document/JDK-Modules-sun/font/sun/font/FontManager.java.htm		
		// http://www.java2s.com/Open-Source/Java-Document/JDK-Modules-sun/java2d/sun/java2d/SunGraphicsEnvironment.java.htm		
		// http://www.java2s.com/Open-Source/Java-Document/JDK-Modules-sun/awt/sun/awt/FontConfiguration.java.htm
	
		
	}

	static java.lang.CharSequence target = (new String(" ")).subSequence(0, 1);
    static java.lang.CharSequence replacement = (new String("")).subSequence(0, 0);
	
	
	static String normalise(String realName) {
		
		// Remove spaces and make lower case        
        return realName.replace(target, replacement).toLowerCase();
        
		
	}

	static Map createMicrosoftFontsMap(List<MicrosoftFonts.Font> fontList){
		
		HashMap msFontsMap = new HashMap(); 
		
		for (MicrosoftFonts.Font font : fontList ) {
				msFontsMap.put(font.getName(), font);
				//System.out.println( "put " + font.getName() );
		}
		return msFontsMap;
	}

	static Map createFontSubstitutionsMap(List<FontSubstitutions.Replace> replaceList){
		
		HashMap replaceMap = new HashMap(); 
		
		for (FontSubstitutions.Replace replacement : replaceList ) {
			replaceMap.put(replacement.getName(), replacement);
		}
		return replaceMap;
	}
	
}
