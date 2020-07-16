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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.fonts.microsoft.MicrosoftFonts;
import org.docx4j.fonts.substitutions.FontSubstitutions;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.docx4j.wml.Fonts;

/**
 * 
 * This mapper uses Panose to guess the physical font
 * which is a closest fit for the font used in the
 * document.  
 * 
 * It is most likely to be suitable on Linux or OSX
 * systems which don't have Microsoft's fonts installed.
 * 
 * @author jharrop
 *
 */
public class BestMatchingMapper extends Mapper {
	
	/*
	 * TODO
	 *   
	 * - Exclude non latin fonts from Panose match eg Segoe UI matching file:/usr/share/fonts/truetype/ttf-tamil-fonts/TAMu_Kalyani.ttf
	 * 
	 * - Look at Unsupport CMap format: 6 in Sun's PDF stuff. 
	 */
	
	
	
	protected static Logger log = LoggerFactory.getLogger(BestMatchingMapper.class);

	public BestMatchingMapper() {
		super();
	}
	
	
	private final static HashMap<String, MicrosoftFonts.Font> msFontsFilenames;
	public final static Map<String, MicrosoftFonts.Font> getMsFontsFilenames() {
		return msFontsFilenames;
	}		
	
	/** The substitutions listed in FontSubstitutions.xml
	 * Will be used only if there is no panose match.  */
	private final static Map<String, FontSubstitutions.Replace> explicitSubstitutionsMap;

    /** Physical fonts remapped using the short key convention in FontSubstitutions.xml;
     * For purpose, see comments below. */
    private final static Map<String, PhysicalFont> physicalFontsByKey;


	int lastSeenNumberOfPhysicalFonts = 0;

    
    /** Max difference for it to be considered an acceptable match.
     *  Note that this value will depend on the weights in the
     *  difference function.
     */ 
    public static final int MATCH_THRESHOLD = 30;
    
    
	
	static {
		
		try {
			
			// Microsoft Fonts
			// 1. On Microsoft platform, to embed in PDF output
			// 2. docx4all - all platforms - to populate font dropdown list
			msFontsFilenames = new HashMap<String, MicrosoftFonts.Font>();
			setupMicrosoftFontFilenames();

			PhysicalFonts.discoverPhysicalFonts();

            physicalFontsByKey = new HashMap<String, PhysicalFont>();
            generateKeysForPhysicalFonts();

			// //////////////////////////////////////////////////////////////////////////////////
			// Get candidate substitutions
			// On a non-MS platform, we need these for two things:
			// 1. to embed this font in the PDF output, in place of MS font
			// 2. in docx4all, use in editor
			// but it will only be used if there is no panose match
			explicitSubstitutionsMap = new HashMap<String, FontSubstitutions.Replace>();
			setupExplicitSubstitutionsMap();
			
			
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

		java.lang.ClassLoader classLoader = BestMatchingMapper.class.getClassLoader();				
		JAXBContext msFontsContext = JAXBContext.newInstance("org.docx4j.fonts.microsoft", classLoader);
		
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
		}
		
	}
	
    private static void generateKeysForPhysicalFonts() {
        for (Map.Entry<String, PhysicalFont> entry : PhysicalFonts.getPhysicalFonts().entrySet()) {
            physicalFontsByKey.put(generateFontKey(entry.getKey()), entry.getValue());
        }
    }

    private static String generateFontKey(String fontName) {
        return StringUtils.replaceChars(fontName.toLowerCase(), "- ", "");
    }

    private static PhysicalFont getPhysicalFontByKey(String key) {
        return physicalFontsByKey.get(key);
    }

	/**
	 * Get candidate substitutions 
	 * On a non-MS platform, we need these for two things:
	 * 1.  to embed this font in the PDF output, in place of MS font
	 * 2.  in docx4all, use in editor 
	 * but it will only be used if there is no panose match.
	 * 
	 * Issues with  FontSubstitutions.xml, as noted and addressed by Jeromy Evans
	 * http://www.docx4java.org/forums/docx-java-f6/bestmatchingmapper-bugs-handling-explicit-substitutions-t940.html
	 *  
	 * (1) FontSubstutitions.xml uses the lowercase whitespace and punctuation removed name of the font. 
	 *     If the document contains "Times New Roman" it is not matched to the equivalent replace element for "timesnewroman". 
	 *     Similarly "Arial" is not matched to "arial".
	 * (2) When matched, the method searching PhysicalFonts for the substitution font also uses the short key, 
	 *     not the proper name used by PhysicalFonts. For example, if matching "arial" to a substitute it tries 
	 *     to find "freesans" in PhysicalFont's map instead of "Free Sans".
	 * (3) On the system tested, the SubsFonts value is inclusive of the leading whitespace (eg in the line above, 
	 *     the first token is "\n\t\tarial' instead of "arial" (seems odd that whitespace is included after unmarshalling). 
	 *     This means the first substitution always fails to match a font. As, by convention, the first token is usually the 
	 *     name of the font, this effectively means on systems where msttcorefonts are installed, the BestMatchingMapper fails 
	 *     to match the exact font. ie. it can't match "arial" to "arial" because the substitution is named "\n\t\tarial".
	 * 
	 *  */	
	private final static void setupExplicitSubstitutionsMap() throws Exception {
				
		java.lang.ClassLoader classLoader = BestMatchingMapper.class.getClassLoader();						
		JAXBContext substitutionsContext = JAXBContext.newInstance("org.docx4j.fonts.substitutions", classLoader);
		
		Unmarshaller u2 = substitutionsContext.createUnmarshaller();		
		u2.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

		log.info("unmarshalling fonts.substitutions" );									
		// Get the xml file
		java.io.InputStream is2 = null;
		// Works in Eclipse - note absence of leading '/'
		is2 = org.docx4j.utils.ResourceUtils.getResource("org/docx4j/fonts/substitutions/FontSubstitutions.xml");
					
		org.docx4j.fonts.substitutions.FontSubstitutions fs = (org.docx4j.fonts.substitutions.FontSubstitutions)u2.unmarshal( is2 );
		
		List<FontSubstitutions.Replace> replaceList = fs.getReplace();

		for (FontSubstitutions.Replace replacement : replaceList ) {
			explicitSubstitutionsMap.put(replacement.getName(), replacement);
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
		List<Fonts.Font> fontList = wmlFonts.getFont();
		Map<String, Fonts.Font> fontsInFontTable = new HashMap<String, Fonts.Font>();
		for (Fonts.Font font : fontList ) {
			fontsInFontTable.put( (font.getName()), font );
		}
			
		log.info("\n\n Populating font mappings.");
		
		// Go through the font names, and determine which ones we can render!		
		for (String documentFontName : documentFontNames) {
			
	    	PhysicalFont fontMatched = null;

			log.debug("\n\n" + documentFontName);
	        	        
	        // Since docx4all invokes this method when opening
	        // each new document, the mapping may have been done
	        // last time.  We don't need to do it again
	        if (get(documentFontName) != null ) {
	        	log.info(documentFontName + " already mapped.");
        		if ( lastSeenNumberOfPhysicalFonts == 
        				PhysicalFonts.getPhysicalFonts().size() ) {
        			// TODO - set this up properly!
    	        	log.info(".. and no need to check again.");
    	        	continue;
    	        	
    	        	// Assume bold, italic etc already mapped
    	        	
        		} else {
    	        	log.info(".. but checking again, since physical fonts have changed.");
        		}
	        }
	        
	        // Embedded fonts - bypass panose for these
	        if (regularForms.get(documentFontName)!=null) {
        		put(documentFontName,         				 
        				regularForms.get(documentFontName) );	
    			log.debug(".. mapped to embedded regular form " );
    			continue;
	        } else if (boldForms.get(documentFontName)!=null) {
        		put(documentFontName,         				 
        				boldForms.get(documentFontName) );	
    			log.debug(".. mapped to embedded bold form " );
    			continue;
	        } else if (italicForms.get(documentFontName)!=null) {
        		put(documentFontName,         				 
        				italicForms.get(documentFontName) );	
    			log.debug(".. mapped to embedded italic form " );
    			continue;
	        } else if (boldItalicForms.get(documentFontName)!=null) {
        		put(documentFontName,         				 
        				boldItalicForms.get(documentFontName) );	
    			log.debug(".. mapped to embedded bold italic form " );
    			continue;
	        }	        
	
//	        boolean normalFormFound = false;
	        			
			// Panose setup
			org.docx4j.wml.FontPanose wmlFontPanoseForDocumentFont = null;
			Fonts.Font font = fontsInFontTable.get(documentFontName);
			if (font==null) {
				log.error("Font " + documentFontName + "not found in font table!");
			} else {
				wmlFontPanoseForDocumentFont = font.getPanose1();
			}
			org.docx4j.fonts.foray.font.format.Panose documentFontPanose = null;
			if (wmlFontPanoseForDocumentFont!=null && wmlFontPanoseForDocumentFont.getVal()!=null ) {
				try {
					documentFontPanose = org.docx4j.fonts.foray.font.format.Panose.makeInstance(wmlFontPanoseForDocumentFont.getVal() );
				} catch (IllegalArgumentException e) {					
					log.error(e.getMessage());
					// For example:
					// Illegal Panose Array: Invalid value 10 > 8 in position 5 of [ 4 2 7 5 4 10 2 6 7 2 ]
				}
				if (documentFontPanose!=null) {
					log.debug(".. " + documentFontPanose.toString() );
				}
				
			} else {
				log.debug(".. no panose info!!!");															
			}
			
			
			/* What about a panose match?
			 * 
			 * We rely on this almost exclusively at present.  It works very well, with the following exceptions:
			 * 
			 * Garamond-Bold .. [ 2 2 8 4 3 3 7 1 8 3 ]
     				Looking for [ 2 2 8 4 3 3 1 1 8 3 ]
                                              ^----------- stuffs us up
                                              
                                              
			 * 
			 */  
	        // TODO - only do this for latin fonts!
			if (documentFontPanose==null ) {
				log.debug(" --> null Panose");								
			} else {
								
				// Is the Panose value valid?
				if (log.isDebugEnabled() &&  org.docx4j.fonts.foray.font.format.Panose.validPanose(documentFontPanose.getPanoseArray())!=null) {														
					// NB org.apache.fop.fonts.Panose only exists in our patched FOP
					log.debug(documentFontName + " : " + org.docx4j.fonts.foray.font.format.Panose.validPanose(documentFontPanose.getPanoseArray()));					
					//This is the case for 'Impact' which has 
					//Invalid value 9 > 8 in position 5 of 2 11 8 6 3 9 2 5 2 4 
				}
				
				String panoseKey =  findClosestPanoseMatch(documentFontName, documentFontPanose,
							PhysicalFonts.getPhysicalFonts() , MATCH_THRESHOLD);
				
				if ( panoseKey==null) {
					log.debug(documentFontName + " -->  no panose match");					
				} else {
		        	
					fontMatched = PhysicalFonts.getPhysicalFonts().get(panoseKey);
					
					if (fontMatched!=null) {
					
						put(documentFontName, PhysicalFonts.getPhysicalFonts().get(panoseKey));
						log.debug("Mapped " +  documentFontName  + " -->  " + panoseKey 
								+ "( "+ PhysicalFonts.getPhysicalFonts().get(panoseKey).getEmbeddedFile() );
					} else {
						
						log.debug("font with key " + panoseKey + " doesn't exist!");
					}

					// Out of interest, is this match in font substitutions table?
					FontSubstitutions.Replace rtmp 
						= (FontSubstitutions.Replace) explicitSubstitutionsMap.get(documentFontName);
					if (rtmp!=null && rtmp.getSubstFonts()!=null) {
						if (rtmp.getSubstFonts().contains(panoseKey) ) {
							log.debug("(consistent with explicit substitutes)");
						} else {
							log.debug("(lucky, since this is missing from explicit substitutes)");							
						}
						
					}
				} 
					
//				// However we found our match for the normal form of
//				// this document font, we still need to do
//				// bold, italic, and bolditalic?
//
//				MicrosoftFonts.Font msFont = (MicrosoftFonts.Font)msFontsFilenames.get(documentFontName);
//				
//				if (msFont==null) {
//					log.warn("Font not found in MicrosoftFonts.xml");
//					continue; 
//				} 
//				
////				PhysicalFont fmTmp;
////				
//				org.apache.fop.fonts.Panose seekingPanose = null; 
//				if (msFont.getBold()!=null) {
//					log.debug("this font has a bold form");
//					seekingPanose = documentFontPanose.getBold();
//					fmTmp = getAssociatedPhysicalFont(documentFontName, panoseKey, seekingPanose); 					
//					if (fmTmp!=null) {
//						fontMappings.put(documentFontName+BOLD, fmTmp);
//					}
//				} 
//				
//				fmTmp = null;
//				seekingPanose = null; 
//				if (msFont.getItalic()!=null) {
//					log.debug("this font has an italic form");
//					seekingPanose = documentFontPanose.getItalic();
//					fmTmp = getAssociatedPhysicalFont(documentFontName, panoseKey, seekingPanose);
//					if (fmTmp!=null) {
//						fontMappings.put(documentFontName+ITALIC, fmTmp);
//					}						
//				} 
//				
//				fmTmp = null;
//				seekingPanose = null; 
//				if (msFont.getBolditalic()!=null) {
//					log.debug("this font has a bold italic form");												
//					seekingPanose = documentFontPanose.getBold();
//					seekingPanose = seekingPanose.getItalic();
//					fmTmp = getAssociatedPhysicalFont(documentFontName, panoseKey, seekingPanose);
//					if (fmTmp!=null) {
//						fontMappings.put(documentFontName+BOLD_ITALIC, fmTmp);
//					}						
//				}
				
				continue; // we're done with this document font
				
			} 
	        
			// Finally, try explicit font substitutions
			// - most likely to be useful for a font that doesn't have panose entries
//			if ( normalFormFound) {
//				continue;
//			}
			
			// Don't bother trying this for bold, italic if you've already
			// got the normal form
			
			log.debug("So try explicit font substitutions table");					        
			FontSubstitutions.Replace replacement = (FontSubstitutions.Replace) explicitSubstitutionsMap
					.get((generateFontKey(documentFontName)));
			if (replacement != null) {
				// log.debug( "\n" + fontName + " found." );
				// String subsFonts = replacement.getSubstFonts();

				// Is there anything in subsFonts we can use?
				String[] tokens = StringUtils.stripAll(replacement.getSubstFonts().split(";"));
				
	        	boolean foundMapping = false;
				for (int x = 0; x < tokens.length; x++) {
					// log.debug(tokens[x]);
                    fontMatched = getPhysicalFontByKey(tokens[x]);
					if (fontMatched != null) {

						String physicalFontFile = fontMatched.getEmbeddedFile();
						log.debug("PDF: " + documentFontName + " --> "
								+ physicalFontFile);
						foundMapping = true;
						
						// Out of interest, does this have a Panose value?
						// And what is the distance?
						if (fontMatched.getPanose() == null ) {
							log.debug(".. as expected, lacking Panose");					
						} else if (documentFontPanose!=null  ) {
							org.docx4j.fonts.foray.font.format.Panose physicalFontPanose = null;
							try {
								physicalFontPanose = org.docx4j.fonts.foray.font.format.Panose.makeInstance(fontMatched
												.getPanose()
												.getPanoseArray());
							} catch (IllegalArgumentException e) {					
								log.error(e.getMessage());
								// For example:
								// Illegal Panose Array: Invalid value 10 > 8 in position 5 of [ 4 2 7 5 4 10 2 6 7 2 ]
							}
							
							if (physicalFontPanose != null) {
								long pd = documentFontPanose
										.difference(physicalFontPanose,
												null);

								if (pd >= MATCH_THRESHOLD) {
									log
											.debug(".. with a panose distance exceeding threshold: "
													+ pd);
								} else {
									// Sanity check
									log
											.error(".. with a low panose distance (! How did we get here?) : "
													+ pd);
								}
							} 
						} 										        	
						break;
					} else {
						// log.debug("no match on token " + x + ":"
						// + tokens[x]);
					}	
				}
				
				if (!foundMapping) {
					log.debug( documentFontName  + " -->  Couldn't find any of "
							+ replacement.getSubstFonts());
				}

			} else {
				log.debug("Nothing in FontSubstitutions.xml for: "
						+ documentFontName);
				
				// TODO - add default fallback values
				
			}
			
			if (fontMatched!=null) {
				put(documentFontName, fontMatched);
				log.warn("Mapped " +  documentFontName  + " -->  " + fontMatched.getName() 
						+ "( "+ fontMatched.getEmbeddedFile() );
			} else {
				log.debug("Nothing added for: " + documentFontName);
			}
		}
		
	    lastSeenNumberOfPhysicalFonts = PhysicalFonts.getPhysicalFonts().size();
	}

	private final static int MATCH_THRESHOLD_INTRA_FAMILY = 4;

	/**
	 * @param fm
	 * @param soughtPanose
	 */
	private PhysicalFont getAssociatedPhysicalFont(String documentFontName, String orignalKey, org.docx4j.fonts.foray.font.format.Panose soughtPanose) {

		log.debug("Looking for " + soughtPanose);
		
		String resultingPanoseKey;
		
//		// First try panose space restricted to this font family
//		2009 03 22 - we don't have physicalFontFamiliesMap any more		
//		if (orignalKey!=null) {
//			PhysicalFontFamily thisFamily = 
//				physicalFontFamiliesMap.get( PhysicalFonts.getPhysicalFonts().get(orignalKey).getName() );					
//			
//			log.debug("Searching within family:" + thisFamily.getFamilyName() );
//			
//			resultingPanoseKey = findClosestPanoseMatch(documentFontName, soughtPanose, 
//					thisFamily.getPhysicalFonts(), MATCH_THRESHOLD_INTRA_FAMILY);    
//			if ( resultingPanoseKey!=null ) {
//				log.info("--> " + PhysicalFonts.getPhysicalFonts().get(resultingPanoseKey).getEmbeddedFile() );
//	        	fm.setPhysicalFont( PhysicalFonts.getPhysicalFonts().get(resultingPanoseKey) );													
//				return fm;
//			}  else {
//				log.warn("No match in immediate font family");
//			}
//		} else {
//			log.debug("originalKey was null.");
//		}
		
		// Well, that failed, so search the whole space
		
		//fm.setDocumentFont(documentFontName); ???
		resultingPanoseKey = findClosestPanoseMatch(documentFontName, soughtPanose, PhysicalFonts.getPhysicalFonts(),
				MATCH_THRESHOLD); 
		if ( resultingPanoseKey!=null ) {
			log.info("--> " + PhysicalFonts.getPhysicalFonts().get(resultingPanoseKey).getEmbeddedFile() );
        	return PhysicalFonts.getPhysicalFonts().get(resultingPanoseKey);
		}  else {
			log.warn("No match in panose space");
			return null;
		}
	}
	
	/** Logic to search panose space for closest matching physical 
		font file. 
		
		Returns key of matching font in physicalFontMap. */
	private String findClosestPanoseMatch(String documentFontName, org.docx4j.fonts.foray.font.format.Panose documentFontPanose, 
			Map<String, PhysicalFont> physicalFontSpace, int matchThreshold) {
		
		// documentFontName enables us to use a name match to break a tie;
		// otherwise it would not be required
		String keywordToMatch = documentFontName.toLowerCase();		 
		if (documentFontName.indexOf(" ")>-1 ) {
			keywordToMatch = keywordToMatch.substring(0, keywordToMatch.indexOf(" "));
		}
		
		String physicalFontKey = null;
		String panoseKey = null;
		
		Iterator it = physicalFontSpace.entrySet().iterator();
		long bestPanoseMatchValue = -1;		
		String matchingPanoseString = null;
	    while (it.hasNext()) {
	        Map.Entry mapPairs = (Map.Entry)it.next();
	        			        
	        physicalFontKey = (String)mapPairs.getKey();
	        PhysicalFont physicalFont = (PhysicalFont)mapPairs.getValue();
	        	        
	        if (physicalFont.getPanose() == null ) {			        	
	        	//log.info(physicalFontKey + " has no Panose data; skipping.");
	        	continue;
	        }
			org.docx4j.fonts.foray.font.format.Panose physicalFontPanose = null;
	        long panoseMatchValue = MATCH_THRESHOLD + 1; // inititaliase to a non-match
			try {
				physicalFontPanose = org.docx4j.fonts.foray.font.format.Panose.makeInstance(physicalFont.getPanose().getPanoseArray() );
		        panoseMatchValue = documentFontPanose.difference(physicalFontPanose, null);
			} catch (IllegalArgumentException e) {					
				log.error(e.getMessage());
				// For example:
				// Illegal Panose Array: Invalid value 10 > 8 in position 5 of [ 4 2 7 5 4 10 2 6 7 2 ]
			}
			
			// Verdana and Tahoma have the same panose value,
			// and without this code, one may be used for the other
			// TODO - Garamond and Garamond-Italic also have the same
			// panose values, but this code is not smart enough to
			// pick the correct one.  Similar confusion between
			// Cambria and Cambria Math
			boolean trump = false;
			if (panoseMatchValue == bestPanoseMatchValue) {
				//log.debug("tie .. checking " + keywordToMatch  + " against " +  physicalFont.getName().toLowerCase());
				if (physicalFont.getName().toLowerCase().indexOf(keywordToMatch)>-1) {
					trump = true;
					log.debug("trumped previous best (which was " + panoseKey + ")");
				}
			}
			
			if (log.isDebugEnabled() ) {
				if ((panoseMatchValue > bestPanoseMatchValue) 
						&& (physicalFont.getName().toLowerCase().indexOf(keywordToMatch)>0) ) {
					log.debug("Despite name match, " + physicalFont.getName() 
							+ physicalFont.getPanose()
							+ " is too far from " + documentFontPanose
							+ " .. " + panoseMatchValue + " > " + bestPanoseMatchValue);
				}
			}
	        
	        if (trump || bestPanoseMatchValue==-1 || panoseMatchValue < bestPanoseMatchValue ) {
	        	
	        	bestPanoseMatchValue = panoseMatchValue;
	        	matchingPanoseString = physicalFont.getPanose().toString();
	        	panoseKey = physicalFontKey;
	        	
	        	//log.debug("Candidate " + panoseMatchValue + "  (" + panoseKey + ") " + matchingPanoseString);
	        	
	        	// Verdana and Tahoma seem to have the same panose value
	        	// so we can't use this optimisation
	        	//if (bestPanoseMatchValue==0) {
	        	//	// Can't do any better than this!
	        	//	continue; // this is just the inner while
	        	//}
	        } else {
	        	//log.debug("not small " + panoseMatchValue + "  " + fontInfo.getPanose().toString() );	        	
	        }
	    }

		if (panoseKey!=null && bestPanoseMatchValue < matchThreshold) {
			log.debug("MATCHED " + panoseKey + " --> " + matchingPanoseString + " distance " + bestPanoseMatchValue);					
			
			return panoseKey;
		}  else {
			return null;
		}
		
		
		
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

		String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/Word2007-fonts.docx";
		//String inputfilepath = "C:\\Users\\jharrop\\workspace\\docx4j\\sample-docs\\Word2007-fonts.docx";
		//String inputfilepath = "/home/jharrop/workspace200711/docx4j-001/sample-docs/fonts-modesOfApplication.docx";
		//String inputfilepath = "/home/jharrop/workspace200711/docx4all/sample-docs/TargetFeatureSet.docx"; //docx4all-fonts.docx";
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
				
		FontTablePart fontTablePart= wordMLPackage.getMainDocumentPart().getFontTablePart();		
		org.docx4j.wml.Fonts fonts = (org.docx4j.wml.Fonts)fontTablePart.getJaxbElement();		
	
		BestMatchingMapper s = new BestMatchingMapper();
				
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
	
	private static void panoseDebugReportOnPhysicalFonts( Map<String, PhysicalFont>physicalFontMap ) {
		Iterator fontIterator = physicalFontMap.entrySet().iterator();
	    while (fontIterator.hasNext()) {
	        Map.Entry pairs = (Map.Entry)fontIterator.next();
	        
	        if(pairs.getKey()==null) {
	        	log.info("Skipped null key");
	        	if (pairs.getValue()!=null) {
	        		log.error(((PhysicalFont)pairs.getValue()).getEmbeddedFile());
	        	}
	        	
	        	if (fontIterator.hasNext() ) {
	        		pairs = (Map.Entry)fontIterator.next();
	        	} else {
	        		return;
	        	}
	        }
	        
	        String fontName = (String)pairs.getKey();

			PhysicalFont pf = (PhysicalFont)pairs.getValue();
			
			org.docx4j.fonts.foray.font.format.Panose fopPanose = pf.getPanose();
			
				if (fopPanose == null ) {
					log.warn(fontName + " .. lacks Panose!");					
				} else if (fopPanose!=null ) {
					log.debug(fontName + " .. " + fopPanose);
				}
//				        long pd = fopPanose.difference(nfontInfo.getPanose().getPanoseArray());
//						System.out.println(".. panose distance: " + pd);					
	    }
	}

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
	
//	private final static void setupAwtFontFamilyNames() {
//		
//		////////////////////////////////////////////////////////////////////////////////////
//		// What fonts are available to AWT
//		
//		java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
//		//System.out.println(ge.getClass().getName());
//		// sun.java2d.SunGraphicsEnvironment
//		// on Ubuntu Gnome, sun.awt.X11GraphicsEnvironment, which extends SunGraphicsEnvironment 
//		// But X11GraphicsEnvironment source code is not available.
//		//((sun.awt.X11GraphicsEnvironment)ge).loadFontFiles();
//		
//		//java.awt.Font[] geFonts = ge.getAllFonts();
//		//String[] geFonts = ge.getAvailableFontFamilyNames();
//		//for (int i=0; i<geFonts.length; i++) {
//			//System.out.println( geFonts[i] );
//			//awtFontFamilyNames.put(normalise(geFonts[i]), geFonts[i]);
//	   // }
//	}
	
}
