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
public class Substituter {
	
	/*
	 * TODO
	 *   
	 * - Exclude non latin fonts from Panose match eg Segoe UI matching file:/usr/share/fonts/truetype/ttf-tamil-fonts/TAMu_Kalyani.ttf
	 * 
	 * - Look at Unsupport CMap format: 6 in Sun's PDF stuff. 
	 */
	
	
	
	protected static Logger log = Logger.getLogger(Substituter.class);

	public Substituter() {
		super();
	}
	
	protected static FontCache fontCache;
	
	/** This map is the one that the others are used
	 *  to produce. */
	private final static Map<String, FontMapping> fontMappings;
	public Map<String, FontMapping> getFontMappings() {
		return fontMappings;
	}	
	
	private final static HashMap<String, MicrosoftFonts.Font> msFontsFilenames;
	public final static Map<String, MicrosoftFonts.Font> getMsFontsFilenames() {
		return msFontsFilenames;
	}		
	
	/** The substitutions listed in FontSubstitutions.xml
	 * Will be used only if there is no panose match.  */
	private final static Map<String, FontSubstitutions.Replace> explicitSubstitutionsMap;
	
	/** These are the physical fonts on the system which we have discovered. */ 
	private final static Map<String, PhysicalFont> physicalFontMap;
	private final static Map<String, PhysicalFontFamily> physicalFontFamiliesMap;
	int lastSeenNumberOfPhysicalFonts = 0;
	
	private final static java.lang.CharSequence target;
    private final static java.lang.CharSequence replacement;
    
    public final static String BOLD   = "Bold";
    public final static String ITALIC = "Italic";
    public final static String BOLD_ITALIC = "BoldItalic";
    
    /** Max difference for it to be considered an acceptable match.
     *  Note that this value will depend on the weights in the
     *  difference function.
     */ 
    public static final int MATCH_THRESHOLD = 30;
    
    
	
	static {
		fontMappings = Collections.synchronizedMap(new HashMap<String, FontMapping>());
		target = (new String(" ")).subSequence(0, 1);
		replacement = (new String("")).subSequence(0, 0);
		
		try {
			// Microsoft Fonts
			// 1. On Microsoft platform, to embed in PDF output
			// 2. docx4all - all platforms - to populate font dropdown list
			msFontsFilenames = new HashMap<String, MicrosoftFonts.Font>();
			setupMicrosoftFontFilenames();

	        fontCache = FontCache.load();
	        if (fontCache == null) {
	            fontCache = new FontCache();
	        }			
			
			// Map of all physical fonts (normalised names) to file paths
	        // We'll use panose first to see which of these is the best
	        // substitute, then failing that, the explicit substitutions
			physicalFontMap = new HashMap<String, PhysicalFont>();
			physicalFontFamiliesMap = new HashMap<String, PhysicalFontFamily>();
			setupPhysicalFonts();

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
			msFontsFilenames.put( normalise(font.getName()), font); // 20080318 - normalised
			//log.debug( "put msFontsFilenames: " + normalise(font.getName()) );
		}
		
	}
	
 
	/**
	 * Add all physical fonts (normalised names) to a map, 
	 * value being EmbedFontInfo objects.
	 */ 
	private final static void setupPhysicalFonts() throws Exception {
		
		// Currently we use FOP - inspired by org.apache.fop.render.PrintRendererConfigurator
		// TODO 20080320 - iText also has a font discoverer.  Given that
		// we're using iText for PDF output, we should use iText's
		// font discovery provided it works
		// (ie discovers all fonts, and can be used to create AWT fonts),
		// we should use that instead of FOP (merging the Panose stuff into
		// it).
		
		
        FontResolver fontResolver = FontSetup.createMinimalFontResolver();        
        FontFileFinder fontFileFinder = new FontFileFinder();
        
        // Automagically finds a list of font files on local system
        // based on os.name
        List fontFileList = fontFileFinder.find();                
        for (Iterator iter = fontFileList.iterator(); iter.hasNext();) {
        	
        	URL fontUrl = getURL(iter.next());
            
            // parse font to ascertain font info
            FontInfoFinder finder = new FontInfoFinder();
            setupPhysicalFont(fontResolver, fontUrl, finder);
        }

        // Add fonts from our Temporary Embedded Fonts dir
        fontFileList = fontFileFinder.find( ObfuscatedFontPart.getTemporaryEmbeddedFontsDir() );
        for (Iterator iter = fontFileList.iterator(); iter.hasNext();) {
            URL fontUrl = getURL(iter.next());
            // parse font to ascertain font info
            FontInfoFinder finder = new FontInfoFinder();
            setupPhysicalFont(fontResolver, fontUrl, finder);
        }
        
        fontCache.save();
        // TODO - reenable the cache
        
        panoseDebugReportOnPhysicalFonts(physicalFontMap);
	}
	
	private static URL getURL(Object o) throws Exception {
		
    	if (o instanceof java.io.File) {
    		// Running in Tomcat
    		java.io.File f = (java.io.File)o;
    		return f.toURL();
    	} else if (o instanceof java.net.URL) {
    		return (URL)o;
    	} else {
    		throw new Exception("Unexpected object:" + o.getClass().getName() );
    	}        	
	}

	/**
	 * Add a physical font's EmbedFontInfo object.
	 * 
	 * @param fontResolver
	 * @param fontUrl
	 * @param fontInfoFinder
	 */
	public static void setupPhysicalFont(FontResolver fontResolver,
			URL fontUrl, FontInfoFinder fontInfoFinder) {
		
		//List<EmbedFontInfo> embedFontInfoList = fontInfoFinder.find(fontUrl, fontResolver, fontCache);		
		EmbedFontInfo[] embedFontInfoList = fontInfoFinder.find(fontUrl, fontResolver, fontCache);
		
		if (embedFontInfoList==null) {
			return;
		}
		
		for ( EmbedFontInfo fontInfo : embedFontInfoList ) {
			
			
			if (fontInfo == null) {
				return;
			}
			 if (!fontInfo.isEmbeddable() ) {			        	
	//	        	log.info(tokens[x] + " is not embeddable; skipping.");
				 
					// NB isEmbeddable() only exists in our patched FOP
			        
					/*
					 * No point looking at this font, since if we tried to use it,
					 * later, we'd get:
					 *  
					 * com.lowagie.text.DocumentException: file:/usr/share/fonts/truetype/ttf-tamil-fonts/lohit_ta.ttf cannot be embedded due to licensing restrictions.
						at com.lowagie.text.pdf.TrueTypeFont.<init>(TrueTypeFont.java:364)
						at com.lowagie.text.pdf.TrueTypeFont.<init>(TrueTypeFont.java:335)
						at com.lowagie.text.pdf.BaseFont.createFont(BaseFont.java:399)
						at com.lowagie.text.pdf.BaseFont.createFont(BaseFont.java:345)
						at org.xhtmlrenderer.pdf.ITextFontResolver.addFont(ITextFontResolver.java:164)
						
						will be thrown if os_2.fsType == 2
						
					 */
	//	        	log.info(physicalFontKey + " is not embeddable; skipping.");
				 
				 return;
			 }
				
			PhysicalFont pf; 
			
			for (Iterator iterIn = fontInfo.getFontTriplets().iterator() ; iterIn.hasNext();) {
				FontTriplet triplet = (FontTriplet)iterIn.next(); 
				// There is one triplet for each of the font family names
				// this font has, and we create a PhysicalFont object 
				// for each of them.  For our purposes though, each of
				// these physical font objects contains the same info
		    	
		        String lower = fontInfo.getEmbedFile().toLowerCase();
		        		        
		        pf = null;
		        // xhtmlrenderer's org.xhtmlrenderer.pdf.ITextFontResolver.addFont
		        // can handle
		        // .otf, .ttf, .ttc, .pfb
		        if (lower.endsWith(".otf") || lower.endsWith(".ttf") || lower.endsWith(".ttc") ) {
		        	pf = new PhysicalFont(fontInfo);
		        } else if (lower.endsWith(".pfb") ) {
		        	// See whether we have everything org.xhtmlrenderer.pdf.ITextFontResolver.addFont
		        	// will need - for a .pfb file, it needs a corresponding .afm or .pfm
					String afm = FontUtils.pathFromURL(lower);
					afm = afm.substring(0, afm.length()-4 ) + ".afm";  // drop the 'file:'
					//log.debug("Looking for: " + afm);					
					File f = new File(afm);
			        if (f.exists()) {				
			        	pf = new PhysicalFont(fontInfo);
			        } else {
			        	// Should we be doing afm first, or pfm?
						String pfm = FontUtils.pathFromURL(lower);
						pfm = pfm.substring(0, pfm.length()-4 ) + ".pfm";  // drop the 'file:'
						//log.debug("Looking for: " + pfm);
						f = new File(pfm);
				        if (f.exists()) {				
				        	pf = new PhysicalFont(fontInfo);
				        } else {
				    		log.warn("Skipping " + triplet.getName() + "; couldn't find .afm or .pfm for : " + fontInfo.getEmbedFile());                	                    					        	
				        }
			        }
		        } else {                    	
		    		log.warn("Skipping " + triplet.getName() + "; unsupported type: " + fontInfo.getEmbedFile());                	                    	
		        }
		    	
		        
		        if (pf!=null) {
		        	
		        	// Add it to the map
		        	physicalFontMap.put(pf.getName(), pf);
		    		//log.debug("Added " + pf.getName() + " -> " + pf.getEmbeddedFile());                	
		        	
		        	// Handle the font family bit - this is critical, since
		        	// it is what iText uses
		        	String familyName = triplet.getName();
		        	pf.setFamilyName(familyName);
		        	
		        	PhysicalFontFamily pff;
		        	if (physicalFontFamiliesMap.get(familyName)==null) {
		        		pff = new PhysicalFontFamily(familyName);
		        		physicalFontFamiliesMap.put(familyName, pff);
		        	} else {
		        		pff = physicalFontFamiliesMap.get(familyName);
		        	}
		        	pff.addFont(pf);
		        	
		        }
			}            	
		}
	}
	
	
	/**
	 * Get candidate substitutions 
	 * On a non-MS platform, we need these for two things:
	 * 1.  to embed this font in the PDF output, in place of MS font
	 * 2.  in docx4all, use in editor 
	 * but it will only be used if there is no panose match */	
	private final static void setupExplicitSubstitutionsMap() throws Exception {
				
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

		for (FontSubstitutions.Replace replacement : replaceList ) {
			explicitSubstitutionsMap.put(replacement.getName(), replacement);
		}
				
	}
	
	
	// Remove spaces and make lower case        
	public final static String normalise(String realName) {		
        return realName.replace(target, replacement).toLowerCase();
	}
	
	
	// For Xalan
	public static String getSubstituteFontXsltExtension(Substituter s, String documentStyleId, String bolditalic, boolean fontFamilyStack) {
		
		return s.getSubstituteFontXsltExtension(documentStyleId, bolditalic, fontFamilyStack);
	}
	
	public String getSubstituteFontXsltExtension(String documentStyleId, 
			String bolditalic, boolean fontFamilyStack) {
				
		if (documentStyleId==null) {
			log.error("passed null documentStyleId");
			return "nullInputToExtension";
		}
		
		// Try with bold italic modifier		
		FontMapping fontMapping = (FontMapping)fontMappings.get(normalise(documentStyleId + bolditalic));

		if (fontMapping==null) {
			log.error("no mapping for:" + normalise(documentStyleId + bolditalic));
			
			// try without  bold italic modifier
			fontMapping = (FontMapping)fontMappings.get(normalise(documentStyleId));
			
			if (fontMapping==null) {
				log.error("still no good:" + normalise(documentStyleId));
				return "noMappingFor" + normalise(documentStyleId);
			}
		} else {
			
			documentStyleId = documentStyleId + bolditalic;
			
		}
		
		log.info(documentStyleId + " -> " + fontMapping.getPhysicalFont().getFamilyName() );
		
		if (fontFamilyStack) {
			
			// TODO - if this is an HTML document intended
			// for viewing in a web browser, we need to add a 
			// font-family cascade (since the true type font
			// specified for PDF purposes won't necessarily be
			// present on web browser's system).
			
			// The easiest way to do it might be to just
			// see whether the substitute font is serif or
			// not, and add cascade entries accordingly.
			
			// If we matched it via FontSubstitutions.xml,
			// maybe that file contains an HTML match as well?
			
			// Either way, this stuff should be worked out in
			// populateFontMappings, and added to the 
			// FontMapping objects.
			
			return fontMapping.getPhysicalFont().getFamilyName();
		} else {
			return fontMapping.getPhysicalFont().getFamilyName();
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
		List<Fonts.Font> fontList = wmlFonts.getFont();
		Map<String, Fonts.Font> fontsInFontTable = new HashMap<String, Fonts.Font>();
		for (Fonts.Font font : fontList ) {
			fontsInFontTable.put( normalise(font.getName()), font );
		}
			
		log.info("\n\n Populating font mappings.");
		
		// Go through the font names, and determine which ones we can render!		
		Iterator documentFontIterator = documentFontNames.entrySet().iterator();
	    while (documentFontIterator.hasNext()) {
	        Map.Entry pairs = (Map.Entry)documentFontIterator.next();
	        
	        if(pairs.getKey()==null) {
	        	log.info("Skipped null key");
	        	pairs = (Map.Entry)documentFontIterator.next();
	        }
	        
	        String documentFontName = (String)pairs.getKey();

			log.debug("\n\n" + documentFontName);
	        
	        String normalisedFontName = normalise(documentFontName);
	        
	        // Since docx4all invokes this method when opening
	        // each new document, the mapping may have been done
	        // last time.  We don't need to do it again, unless
	        // new physical fonts have been added (eg via
	        // an embedding)
	        if (fontMappings.get(normalisedFontName) != null ) {
	        	log.info(normalisedFontName + " already mapped.");
        		if ( lastSeenNumberOfPhysicalFonts == physicalFontMap.size() ) {
    	        	log.info(".. and no need to check again.");
    	        	continue;
        		} else {
    	        	log.info(".. but checking again, since physical fonts have changed.");
        		}
	        }
	
//	        boolean normalFormFound = false;
	        
			FontMapping fm = new FontMapping();
			fm.setDocumentFont(documentFontName);
			
			// Panose setup
			org.docx4j.wml.FontPanose wmlFontPanoseForDocumentFont = null;
			Fonts.Font font = fontsInFontTable.get(normalisedFontName);
			if (font==null) {
				log.error("Font " + normalisedFontName + "not found in font table!");
			} else {
				wmlFontPanoseForDocumentFont = font.getPanose1();
			}
			org.apache.fop.fonts.Panose documentFontPanose = null;
			if (wmlFontPanoseForDocumentFont!=null && wmlFontPanoseForDocumentFont.getVal()!=null ) {
				try {
					documentFontPanose = org.apache.fop.fonts.Panose.makeInstance(wmlFontPanoseForDocumentFont.getVal() );
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
			if (documentFontPanose!=null ) {
								
				// Is the Panose value valid?
				if (log.isDebugEnabled() &&  org.apache.fop.fonts.Panose.validPanose(documentFontPanose.getPanoseArray())!=null) {														
					// NB org.apache.fop.fonts.Panose only exists in our patched FOP
					log.debug(documentFontName + " : " + org.apache.fop.fonts.Panose.validPanose(documentFontPanose.getPanoseArray()));					
					//This is the case for 'Impact' which has 
					//Invalid value 9 > 8 in position 5 of 2 11 8 6 3 9 2 5 2 4 
				}
				
				String panoseKey = null;
//				if ( !normalFormFound) 
					panoseKey = findClosestPanoseMatch(documentFontName, documentFontPanose, 
							physicalFontMap , MATCH_THRESHOLD);
				
				if ( panoseKey!=null) {
					log.info("panose: " + fm.getDocumentFont() + " --> " + physicalFontMap.get(panoseKey).getEmbeddedFile() );
		        	fm.setPhysicalFont( physicalFontMap.get(panoseKey) );										
		        	
					// Out of interest, is this match in font substitutions table?
					FontSubstitutions.Replace rtmp 
						= (FontSubstitutions.Replace) explicitSubstitutionsMap.get(normalise(fm.getDocumentFont()));
					if (rtmp!=null && rtmp.getSubstFonts()!=null) {
						if (rtmp.getSubstFonts().contains(panoseKey) ) {
							log.debug("(consistent with explicit substitutes)");
						} else {
							log.debug("(lucky, since this is missing from explicit substitutes)");							
						}
						
					}
					fontMappings.put(normalise(fm.getDocumentFont()), fm);
					log.debug("Entry added for: " +  normalise(fm.getDocumentFont()) );
				} else {
					log.debug(fm.getDocumentFont() + " -->  no panose match");
				}
					
				// However we found our match for the normal form of
				// this document font, we still need to do
				// bold, italic, and bolditalic?

				MicrosoftFonts.Font msFont = (MicrosoftFonts.Font)msFontsFilenames.get(normalisedFontName);
				
				if (msFont==null) {
					log.warn("Font not found in MicrosoftFonts.xml");
					continue; 
				} 
				
				FontMapping fmTmp = null;
				org.apache.fop.fonts.Panose seekingPanose = null; 
				if (msFont.getBold()!=null) {
					log.debug("this font has a bold form");
					seekingPanose = documentFontPanose.getBold();
					fmTmp = getAssociatedFontMapping(documentFontName, panoseKey, seekingPanose); 					
					if (fmTmp!=null) {
						fontMappings.put(normalise(fm.getDocumentFont()+BOLD), fmTmp);
					}
				} 
				
				fmTmp = null;
				seekingPanose = null; 
				if (msFont.getItalic()!=null) {
					log.debug("this font has an italic form");
					seekingPanose = documentFontPanose.getItalic();
					fmTmp = getAssociatedFontMapping(documentFontName, panoseKey, seekingPanose);
					if (fmTmp!=null) {
						fontMappings.put(normalise(fm.getDocumentFont()+ITALIC), fmTmp);
					}						
				} 
				
				fmTmp = null;
				seekingPanose = null; 
				if (msFont.getBolditalic()!=null) {
					log.debug("this font has a bold italic form");												
					seekingPanose = documentFontPanose.getBold();
					seekingPanose = seekingPanose.getItalic();
					fmTmp = getAssociatedFontMapping(documentFontName, panoseKey, seekingPanose);
					if (fmTmp!=null) {
						fontMappings.put(normalise(fm.getDocumentFont()+BOLD_ITALIC), fmTmp);
					}						
				}
				
				continue; // we're done with this document font
				
			} else {
				log.debug(" --> null Panose");				
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
					.get(normalise(documentFontName));
			if (replacement != null) {
				// log.debug( "\n" + fontName + " found." );
				// String subsFonts = replacement.getSubstFonts();

				// Is there anything in subsFonts we can use?
				String[] tokens = replacement.getSubstFonts().split(";");
				
	        	boolean foundMapping = false;
				for (int x = 0; x < tokens.length; x++) {
					// log.debug(tokens[x]);
					if (physicalFontMap.get(tokens[x]) != null) {
						
						PhysicalFont physicalFont = physicalFontMap.get(tokens[x]);

						String physicalFontFile = physicalFont.getEmbeddedFile();
						log.debug("PDF: " + documentFontName + " --> "
								+ physicalFontFile);
						foundMapping = true;
			        	//fm.setTripletName( embedFontInfo.getFontTriplets(), tokens[x] );
						fm.setPhysicalFont(physicalFont);
						
						// Out of interest, does this have a Panose value?
						// And what is the distance?
						if (physicalFont.getPanose() == null ) {
							log.debug(".. as expected, lacking Panose");					
						} else if (documentFontPanose!=null  ) {
							org.apache.fop.fonts.Panose physicalFontPanose = null;
							try {
								physicalFontPanose = org.apache.fop.fonts.Panose.makeInstance(physicalFont
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
			
			if (fm!=null && fm.getPhysicalFont()!=null) {
				fontMappings.put(normalisedFontName, fm);
				log.info("subtable: " + normalisedFontName + " --> " + fm.getPhysicalFont().getEmbeddedFile() );
			} else {
				log.debug("Nothing added for: " + normalisedFontName);
			}
		}
		
	    lastSeenNumberOfPhysicalFonts = physicalFontMap.size();
	}

	private final static int MATCH_THRESHOLD_INTRA_FAMILY = 4;

	/**
	 * @param fm
	 * @param soughtPanose
	 */
	private FontMapping getAssociatedFontMapping(String documentFontName, String orignalKey, org.apache.fop.fonts.Panose soughtPanose) {

		log.debug("Looking for " + soughtPanose);
		
		FontMapping fm = new FontMapping();
		String resultingPanoseKey;
		
		// First try panose space restricted to this font family
		if (orignalKey!=null) {
			PhysicalFontFamily thisFamily = 
				physicalFontFamiliesMap.get( physicalFontMap.get(orignalKey).getFamilyName() );					
			
			log.debug("Searching within family:" + thisFamily.getFamilyName() );
			
			resultingPanoseKey = findClosestPanoseMatch(documentFontName, soughtPanose, 
					thisFamily.getPhysicalFonts(), MATCH_THRESHOLD_INTRA_FAMILY);    
			if ( resultingPanoseKey!=null ) {
				log.info("--> " + physicalFontMap.get(resultingPanoseKey).getEmbeddedFile() );
	        	fm.setPhysicalFont( physicalFontMap.get(resultingPanoseKey) );													
				return fm;
			}  else {
				log.warn("No match in immediate font family");
			}
		} else {
			log.debug("originalKey was null.");
		}
		
		// Well, that failed, so search the whole space
		
		//fm.setDocumentFont(documentFontName); ???
		resultingPanoseKey = findClosestPanoseMatch(documentFontName, soughtPanose, physicalFontMap,
				MATCH_THRESHOLD); 
		if ( resultingPanoseKey!=null ) {
			log.info("--> " + physicalFontMap.get(resultingPanoseKey).getEmbeddedFile() );
        	fm.setPhysicalFont( physicalFontMap.get(resultingPanoseKey) );													
			return fm;
		}  else {
			log.warn("No match in panose space");
			return null;
		}
	}
	
	/** Logic to search panose space for closest matching physical 
		font file. 
		
		Returns key of matching font in physicalFontMap. */
	private String findClosestPanoseMatch(String documentFontName, org.apache.fop.fonts.Panose documentFontPanose, 
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
			org.apache.fop.fonts.Panose physicalFontPanose = null;
	        long panoseMatchValue = MATCH_THRESHOLD + 1; // inititaliase to a non-match
			try {
				physicalFontPanose = org.apache.fop.fonts.Panose.makeInstance(physicalFont.getPanose().getPanoseArray() );
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
	

	public static class PhysicalFontFamily {

		String familyName; // For example: Times New Roman
		public String getFamilyName() {
			return familyName;
		}

		PhysicalFontFamily(String familyName) {
			this.familyName = familyName;
		}

		// We want this, so that when were are searching panose space
		// for bold, bolditalic, italic, we can restrict the search
		// to this list
		Map<String, PhysicalFont> physicalFonts = new HashMap<String, PhysicalFont> ();
		void addFont(PhysicalFont physicalFont){
			physicalFonts.put(physicalFont.getName(), physicalFont);
		}
		
		Map<String, PhysicalFont> getPhysicalFonts() {
			return physicalFonts;
		}
		
	}
	
	
	public static class PhysicalFont {

		
		PhysicalFont(EmbedFontInfo fontInfo) {
			
			// Sanity check
			if (fontInfo.getPostScriptName()==null) {
				log.error("Not set!");
				//log.error(((org.apache.fop.fonts.FontTriplet)fontInfo.getFontTriplets().get(0)).getName());
			}
			
	    	setName(fontInfo.getPostScriptName());
	    	setEmbeddedFile(fontInfo.getEmbedFile());
	    	setPanose(fontInfo.getPanose());		
		}
		
		// For example, Times New Roman Bold
		// What exactly to call it?
		// Note that we don't use this name anywhere that matters
		// - iText uses the font family name,
		// with magic appendages 'Bold', 'Italic', 'BoldItalic'
		// as necessary
		// Nevertheless, lets refer to it here by its
		// postscript name, since that is pretty well defined
		String name;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		// // For example: Times New Roman
		String familyName;
		public String getFamilyName() {
			return familyName;
		}
		public void setFamilyName(String familyName) {
			this.familyName = familyName;
		}
				
		String embeddedFile;
		public String getEmbeddedFile() {
			return embeddedFile;
		}
		public void setEmbeddedFile(String embeddedFile) {
			this.embeddedFile = embeddedFile;
		}
		
		org.apache.fop.fonts.Panose panose;
		public org.apache.fop.fonts.Panose getPanose() {
			return panose;
		}
		public void setPanose(org.apache.fop.fonts.Panose panose) {
			this.panose = panose;
		}

	}

	
	public class FontMapping {

		FontMapping() {}
		
		FontMapping(String documentFont, PhysicalFont physicalFont) {
			
			this.documentFont = documentFont;
			
			this.physicalFont = physicalFont;
			
		}
		
		// The document font.  This, normalised, is the
		// key under which this font mapping can be found
		String documentFont;
		public String getDocumentFont() {
			return documentFont;
		}
		public void setDocumentFont(String documentFont) {
			this.documentFont = documentFont;
		}

		PhysicalFont physicalFont;
		public PhysicalFont getPhysicalFont() {
			return physicalFont;
		}
		public void setPhysicalFont(PhysicalFont physicalFont) {
			this.physicalFont = physicalFont;
		}

	}

	public static void main(String[] args) throws Exception {

		String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/Word2007-fonts.docx";
		//String inputfilepath = "C:\\Users\\jharrop\\workspace\\docx4j\\sample-docs\\Word2007-fonts.docx";
		//String inputfilepath = "/home/jharrop/workspace200711/docx4j-001/sample-docs/fonts-modesOfApplication.docx";
		//String inputfilepath = "/home/jharrop/workspace200711/docx4all/sample-docs/TargetFeatureSet.docx"; //docx4all-fonts.docx";
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
				
		FontTablePart fontTablePart= wordMLPackage.getMainDocumentPart().getFontTablePart();		
		org.docx4j.wml.Fonts fonts = (org.docx4j.wml.Fonts)fontTablePart.getJaxbElement();		
	
		Substituter s = new Substituter();
				
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
			
			org.apache.fop.fonts.Panose fopPanose = pf.getPanose();
			
				if (fopPanose == null ) {
					System.out.println(fontName + " .. lacks Panose!");					
				} else if (fopPanose!=null ) {
					System.out.println(fontName + " .. " + fopPanose);
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
