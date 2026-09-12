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

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.Docx4jProperties;
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
 * Since 17.1.1 that guess is not its first answer but its last but two: it takes the
 * shared order of precedence in {@link Mapper} (the font itself, the document's
 * embedded form, then the measured passes - the metric clones, w:altName, a face of
 * the same class), and its own panose match and FontSubstitutions.xml are consulted
 * after them, for what they leave unmapped ({@link #addMapperSubstitutes}).
 *
 * It was long described as the one for Linux or OSX systems without Microsoft's
 * fonts.  Measured (CR-016 phase 0c) on three real-document corpora in six font
 * environments - this developer's box, docx4j's font jars alone, eclipse-temurin,
 * and the Ubuntu, Debian and Fedora desktop font sets - it is behind
 * {@link IdentityPlusMapper} in every one of them, including those without any
 * Microsoft font.  Use {@link IdentityPlusMapper}, the default, everywhere (CR-016
 * Decisions 7, 2026-09-13); this class is kept for compatibility.  With the measured
 * passes in front of its panose step it is now within 0.001 to 0.005 of line parity,
 * where it had been 0.010 to 0.021 behind.
 *
 * @author jharrop
 *
 */
public class BestMatchingMapper extends Mapper {
	
	/*
	 * TODO
	 *   
	 * - Look at Unsupport CMap format: 6 in Sun's PDF stuff. 
	 */
	
	
	
	protected static Logger log = LoggerFactory.getLogger(BestMatchingMapper.class);

	public BestMatchingMapper() {
		super();
	}

	
	/** The substitutions listed in FontSubstitutions.xml
	 * Will be used only if there is no panose match.  */
	private final static Map<String, FontSubstitutions.Replace> explicitSubstitutionsMap;

    /** Physical fonts remapped using the short key convention in FontSubstitutions.xml;
     * For purpose, see comments below. */
    private final static Map<String, PhysicalFont> physicalFontsByKey;


    
    /** Max difference for it to be considered an acceptable match.
     *  Note that this value will depend on the weights in the
     *  difference function.
     */ 
    public static final int MATCH_THRESHOLD = 30;
    
    
	
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
	 * A panose match over the installed fonts (the closest within
	 * {@link #MATCH_THRESHOLD}, ties broken on the name, and never a face which cannot
	 * draw Basic Latin - the old TODO: Segoe UI matched a Tamil font), else the first
	 * installed face FontSubstitutions.xml lists for the name - for what is still
	 * unmapped after the installed font, the embedded forms, the metric clones,
	 * w:altName and a face of the same class (the shared order, Mapper), and before
	 * Word's default.  Until 17.1.1 the panose match came first of all, and a legacy
	 * Indic face carrying Arial's panose took Myriad and CorpoS; the Calibri-to-Carlito
	 * workaround that guarded against the same thing for Calibri is the metric table's
	 * job now.
	 *
	 * @since 17.1.1 as this method; the same lookups were populateFontMappings' before
	 */
	@Override
	public void addMapperSubstitutes(Set<String> documentFontNames, org.docx4j.wml.Fonts wmlFonts) {
		if (documentFontNames==null) return;
		Map<String, org.docx4j.wml.Fonts.Font> table = fontTable(wmlFonts);
		for (String documentFontName : documentFontNames) {
			if (documentFontName==null || documentFontName.trim().length()==0) continue;
			if (get(documentFontName)!=null) continue;
			if (isEmbedded(documentFontName)) continue;
			PhysicalFont pf = panoseOrExplicit(documentFontName, table.get(documentFontName.trim().toLowerCase()));
			if (pf!=null) put(documentFontName, pf);
		}
	}

	private PhysicalFont panoseOrExplicit(String documentFontName, org.docx4j.wml.Fonts.Font font) {

		// Panose setup
		org.docx4j.wml.FontPanose wmlFontPanoseForDocumentFont = null;
		if (font==null) {
			log.debug("Font " + documentFontName + " not found in font table");
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
		}

		/* A panose match.  It works very well, with the following exceptions:
		 *
		 * Garamond-Bold .. [ 2 2 8 4 3 3 7 1 8 3 ]
		 *      Looking for [ 2 2 8 4 3 3 1 1 8 3 ]
		 *                               ^----------- stuffs us up
		 */
		if (documentFontPanose!=null) {
			java.util.Map<String, PhysicalFont> space = new HashMap<String, PhysicalFont>(PhysicalFonts.getPhysicalFonts());
			for (int attempt=0; attempt<8 && !space.isEmpty(); attempt++) {
				String panoseKey = findClosestPanoseMatch(documentFontName, documentFontPanose, space, MATCH_THRESHOLD);
				if (panoseKey==null) break;
				PhysicalFont fontMatched = space.get(panoseKey);
				if (fontMatched!=null && drawsBasicLatin(fontMatched)) {
					log.debug("Mapped " +  documentFontName  + " -->  " + panoseKey
							+ " ( " + fontMatched.getEmbeddedURI() + ")");
					return fontMatched;
				}
				// a face for another script with a nearby panose: not for a Latin document font
				space.remove(panoseKey);
			}
			log.debug(documentFontName + " -->  no panose match");
		}

		// Finally, try explicit font substitutions - most likely to be useful for a font
		// that doesn't have panose entries
		FontSubstitutions.Replace replacement = (FontSubstitutions.Replace) explicitSubstitutionsMap
				.get((generateFontKey(documentFontName)));
		if (replacement != null && replacement.getSubstFonts()!=null) {
			String[] tokens = StringUtils.stripAll(replacement.getSubstFonts().split(";"));
			for (int x = 0; x < tokens.length; x++) {
				PhysicalFont fontMatched = getPhysicalFontByKey(tokens[x]);
				if (fontMatched != null) {
					log.debug(documentFontName + " --> " + fontMatched.getEmbeddedURI() + " (FontSubstitutions.xml)");
					return fontMatched;
				}
			}
			log.debug( documentFontName  + " -->  Couldn't find any of " + replacement.getSubstFonts());
		} else {
			log.debug("Nothing in FontSubstitutions.xml for: " + documentFontName);
		}
		return null;
	}

	/** Whether the face has the Latin letters a document font is asked for (a panose
	 *  neighbour may be a Tamil or Hebrew face). */
	private static boolean drawsBasicLatin(PhysicalFont pf) {
		try {
			return GlyphCheck.hasCodepoint(pf, 'a') && GlyphCheck.hasCodepoint(pf, 'A');
		} catch (Exception e) {
			return false;
		}
	}

	private final static int MATCH_THRESHOLD_INTRA_FAMILY = 4;

	/**
	 * The closest match in panose space over all the physical fonts.
	 *
	 * @param documentFontName the name the document uses, for the tie-break on the name
	 * @param orignalKey unused: since 2009-03-22 there is no map of physical font families
	 *                   to restrict the search to
	 * @param soughtPanose the document font's panose value
	 */
	private PhysicalFont getAssociatedPhysicalFont(String documentFontName, String orignalKey, org.docx4j.fonts.foray.font.format.Panose soughtPanose) {

		log.debug("Looking for " + soughtPanose);

		String resultingPanoseKey;

		resultingPanoseKey = findClosestPanoseMatch(documentFontName, soughtPanose, PhysicalFonts.getPhysicalFonts(),
				MATCH_THRESHOLD); 
		if ( resultingPanoseKey!=null ) {
			log.info("--> " + PhysicalFonts.getPhysicalFonts().get(resultingPanoseKey).getEmbeddedURI() );
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
		String documentFontNameLower = documentFontName.toLowerCase();		 
		// the first word only, eg "franklin" of "Franklin Gothic Demi"; used as the
		// weakest form of name affinity.  See nameAffinity.
		String firstWord = documentFontNameLower;
		if (firstWord.indexOf(" ")>-1 ) {
			firstWord = firstWord.substring(0, firstWord.indexOf(" "));
		}
		
		String physicalFontKey = null;
		String panoseKey = null;
		
		Iterator it = physicalFontSpace.entrySet().iterator();
		long bestPanoseMatchValue = -1;
		int bestNameAffinity = -1;		
		String matchingPanoseString = null;
	    while (it.hasNext()) {
	        Map.Entry mapPairs = (Map.Entry)it.next();
	        			        
	        physicalFontKey = (String)mapPairs.getKey();
	        PhysicalFont physicalFont = (PhysicalFont)mapPairs.getValue();
	        	        
	        if (physicalFont.getPanose() == null ) {
	        	continue; // nothing to compare
	        }
			org.docx4j.fonts.foray.font.format.Panose physicalFontPanose = null;
	        long panoseMatchValue = MATCH_THRESHOLD + 1; // initialize to a non-match
			try {
				physicalFontPanose = org.docx4j.fonts.foray.font.format.Panose.makeInstance(physicalFont.getPanose().getPanoseArray() );
		        panoseMatchValue = documentFontPanose.difference(physicalFontPanose, null);
			} catch (IllegalArgumentException e) {					
				log.error(e.getMessage());
				// For example:
				// Illegal Panose Array: Invalid value 10 > 8 in position 5 of [ 4 2 7 5 4 10 2 6 7 2 ]
			}
			
			/* Verdana and Tahoma have the same panose value, and without this code,
			 * one may be used for the other.  Likewise Garamond and Garamond-Italic,
			 * and Cambria and Cambria Math.
			 *
			 * Where the panose values tie, we choose on the name.  Until 17.0.3 that
			 * test was "does the physical font's name contain the first word of the
			 * document font's name", which can't tell apart the members of a family:
			 * for "Franklin Gothic Demi" it is just "franklin", so Book, Heavy and
			 * Medium all matched, and the winner was whichever the map happened to
			 * yield last.  We now prefer the closest name, and failing that choose
			 * deterministically, so the same document always converts the same way.
			 *
			 * @since 17.0.3
			 */
			int affinity = nameAffinity(documentFontNameLower, firstWord, physicalFont.getName());
			boolean trump = false;
			if (panoseMatchValue == bestPanoseMatchValue) {
				if (affinity > bestNameAffinity) {
					trump = true;
				} else if (affinity == bestNameAffinity
						&& panoseKey != null
						&& physicalFontKey.compareTo(panoseKey) < 0) {
					// nothing to choose between them on either panose or name
					trump = true;
				}
				if (trump) {
					log.debug("trumped previous best (which was " + panoseKey + ")");
				}
			}
			
			if (log.isDebugEnabled() ) {
				if ((panoseMatchValue > bestPanoseMatchValue) 
						&& (physicalFont.getName().toLowerCase().indexOf(firstWord)>0) ) {
					log.debug("Despite name match, " + physicalFont.getName() 
							+ physicalFont.getPanose()
							+ " is too far from " + documentFontPanose
							+ " .. " + panoseMatchValue + " > " + bestPanoseMatchValue);
				}
			}
	        
	        if (trump || bestPanoseMatchValue==-1 || panoseMatchValue < bestPanoseMatchValue ) {
	        	
	        	bestPanoseMatchValue = panoseMatchValue;
	        	bestNameAffinity = affinity;
	        	matchingPanoseString = physicalFont.getPanose().toString();
	        	panoseKey = physicalFontKey;

	        	// No short circuit on a distance of 0: Verdana and Tahoma seem to have the
	        	// same panose value, so a perfect match is not necessarily the only one, and
	        	// the name tie-break above has still to be applied to the rest.
	        } else {
	        	// further from the sought panose than the best so far, or an equal distance
	        	// with no better name affinity: keep what we have
	        }
	    }

		if (panoseKey!=null && bestPanoseMatchValue < matchThreshold) {
			log.debug("MATCHED " + panoseKey + " --> " + matchingPanoseString + " distance " + bestPanoseMatchValue);

			return panoseKey;
		}  else {
			return null;
		}



	}

	/**
	 * How closely the name of a physical font resembles the name of the font the
	 * document asks for; used to choose between candidates whose panose values are
	 * equally distant from it.  Higher is better.
	 *
	 * The point of the intermediate values is the members of a font family: for
	 * "Franklin Gothic Demi", "Franklin Gothic Demi Italic" is a nearer thing than
	 * "Franklin Gothic Book", which in turn is nearer than some unrelated font
	 * which happens to share the panose value.
	 *
	 * @param documentFontNameLower the document's font name, lower cased
	 * @param firstWord its first word, eg "franklin"
	 * @param physicalFontName the candidate's name (any case)
	 * @since 17.0.3
	 */
	private static int nameAffinity(String documentFontNameLower, String firstWord, String physicalFontName) {

		if (physicalFontName == null) return 0;
		String candidate = physicalFontName.toLowerCase();

		if (candidate.equals(documentFontNameLower)) return 3;
		// eg "Franklin Gothic Demi Italic" for "Franklin Gothic Demi"
		if (candidate.startsWith(documentFontNameLower)
				|| documentFontNameLower.startsWith(candidate)) return 2;
		// eg "Franklin Gothic Book" for "Franklin Gothic Demi"
		if (candidate.indexOf(firstWord)>-1) return 1;
		return 0;
	}


	public static void main(String[] args) throws Exception {

		String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/Word2007-fonts.docx";

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
				
		FontTablePart fontTablePart= wordMLPackage.getMainDocumentPart().getFontTablePart();		
		org.docx4j.wml.Fonts fonts = (org.docx4j.wml.Fonts)fontTablePart.getJaxbElement();		
	
		BestMatchingMapper s = new BestMatchingMapper();

		s.populateFontMappings(wordMLPackage.getMainDocumentPart().fontsInUse(), fonts );
	}
	
	private static void panoseDebugReportOnPhysicalFonts( Map<String, PhysicalFont>physicalFontMap ) {
		Iterator fontIterator = physicalFontMap.entrySet().iterator();
	    while (fontIterator.hasNext()) {
	        Map.Entry pairs = (Map.Entry)fontIterator.next();
	        
	        if(pairs.getKey()==null) {
	        	log.info("Skipped null key");
	        	if (pairs.getValue()!=null) {
	        		log.error(((PhysicalFont)pairs.getValue()).getEmbeddedURI().toString());
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
	    }
	}

}
