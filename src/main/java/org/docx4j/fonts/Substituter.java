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
package org.docx4j.fonts;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
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
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.docx4j.wml.Fonts;

/**
 * 
 * Generate a map, mapping each Microsoft font name to:
 * 1. a substitute font which can be embedded in a PDF document
 * 2. a font list suitable for an HTML document (which is not then going to be transformed to PDF!)
 * 3. an AWT substitute font for use in Swing (eg in docx4all)
 * 
 * Note that AWT doesn't tell us physical font paths, so we use FOP to get those.
 * But FOP doesn't tell us what the corresponding AWT font is ...
 * 
 * @author jharrop
 *
 */
public class Substituter {
	protected static Logger log = Logger.getLogger(Substituter.class);
	
	/** This map is the one that the others are used
	 *  to produce.
	 */
	private final static Map<String, FontMapping> fontMappings;
	public static Map<String, FontMapping> getFontMappings() {
		return fontMappings;
	}
	
	private final static HashMap<String, MicrosoftFonts.Font> msFontsFilenames;
	private final static Map<String, FontSubstitutions.Replace> replaceMap;
	private final static Map<String, EmbedFontInfo> physicalFontMap;
	private final static Map<String, String> awtFontFamilyNames;

	private final static java.lang.CharSequence target;
    private final static java.lang.CharSequence replacement;
	
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

			// //////////////////////////////////////////////////////////////////////////////////
			// Get candidate substitutions
			// On a non-MS platform, we need these for two things:
			// 1. to embed this font in the PDF output, in place of MS font
			// 2. in docx4all, use in editor
			replaceMap = new HashMap<String, FontSubstitutions.Replace>();
			setupCandidateSubstitutionsMap();

			// Map of all physical fonts (normalised names) to file paths
			physicalFontMap = new HashMap<String, EmbedFontInfo>();
			setupPhysicalFonts();

			// //////////////////////////////////////////////////////////////////////////////////
			// What fonts are available to AWT
			awtFontFamilyNames = new HashMap<String, String>();
			setupAwtFontFamilyNames();
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}
	
	private final static void setupMicrosoftFontFilenames() throws Exception {
		
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
		
		for (MicrosoftFonts.Font font : msFontsList ) {
			System.out.println( "put font=" + font);
			System.out.println( "put font.getName()=" + font.getName() );
			msFontsFilenames.put(font.getName(), font);
				//System.out.println( "put " + font.getName() );
		}
		
	}
	
	private final static void setupCandidateSubstitutionsMap() throws Exception {
		
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

		for (FontSubstitutions.Replace replacement : replaceList ) {
			replaceMap.put(replacement.getName(), replacement);
		}
				
	}

	// Map of all physical fonts (normalised names) to file paths 
	private final static void setupPhysicalFonts() throws Exception {
		// Use FOP
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
            		log.debug("Added " + triplet.getName() + " -> " + fontInfo.getEmbedFile());
                	physicalFontMap.put(normalise(triplet.getName()), fontInfo );
                	
                	// Uncomment this to see ...
            		// System.out.println("Added " + triplet.getName() + " -> " + fontInfo.getEmbedFile());
            	}
            	
            }
        }
        
        // http://www.brawer.ch/software/fonts/doc/gnu/java/awt/font/opentype/NameDecoder.html 
        //    can be used to interrogate the TTF file. Also http://www.brawer.ch/software/fonts/
	}
	
	private final static void setupAwtFontFamilyNames() {
		
		////////////////////////////////////////////////////////////////////////////////////
		// What fonts are available to AWT
		
		java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
		//System.out.println(ge.getClass().getName());
		// sun.java2d.SunGraphicsEnvironment
		// on Ubuntu Gnome, sun.awt.X11GraphicsEnvironment, which extends SunGraphicsEnvironment 
		// But X11GraphicsEnvironment source code is not available.
		//((sun.awt.X11GraphicsEnvironment)ge).loadFontFiles();
		
		//java.awt.Font[] geFonts = ge.getAllFonts();
		String[] geFonts = ge.getAvailableFontFamilyNames();
		for (int i=0; i<geFonts.length; i++) {
			//System.out.println( geFonts[i] );
			awtFontFamilyNames.put(normalise(geFonts[i]), geFonts[i]);
	    }
	}
	
	
	// Remove spaces and make lower case        
	public final static String normalise(String realName) {		
        return realName.replace(target, replacement).toLowerCase();
	}
	
	public final static Map<String, MicrosoftFonts.Font> getMsFontsFilenames() {
		return msFontsFilenames;
	}
		
	public Substituter() {
		super();
	}

	public static void main(String[] args) throws Exception {

		String inputfilepath = "/home/jharrop/workspace200711/docx4j-001/sample-docs/Word2007-fonts.docx";
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
		
		s.populateFontMappings(wordMLPackage.getMainDocumentPart().fontsInUse(), fonts );
	}
	
	// For Xalan
	public static String getSubstituteFontXsltExtension(Substituter s, String documentStyleId, boolean fontFamilyStack) {
		
		return s.getSubstituteFontXsltExtension(documentStyleId, fontFamilyStack);
	}
	
	public String getSubstituteFontXsltExtension(String documentStyleId, boolean fontFamilyStack) {
				
		if (documentStyleId==null) {
			log.error("passed null documentStyleId");
			return "nullInputToExtension";
		}
		
		FontMapping fontMapping = (FontMapping)fontMappings.get(normalise(documentStyleId));

		if (fontMapping==null) {
			log.error("no mapping for:" + normalise(documentStyleId));
			return "noMappingFor" + normalise(documentStyleId);
		}
		
		log.error(documentStyleId + " -> " + fontMapping.getTripletName());
		
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
			
			return fontMapping.getTripletName();
		} else {
			return fontMapping.getTripletName();
		}
		
	}
	
	public void populateFontMappings(Map documentFontNames, org.docx4j.wml.Fonts fonts ) throws Exception {
		
		//setup();
		
		/* org.docx4j.wml.Fonts fonts is obtained as follows:
		 * 
		 *     FontTablePart fontTablePart= wordMLPackage.getMainDocumentPart().getFontTablePart();
		 *     org.docx4j.wml.Fonts fonts = (org.docx4j.wml.Fonts)fontTablePart.getJaxbElement();
		 *     
		 * We need to make a map out of it.    
		 */ 
		List<Fonts.Font> fontList = fonts.getFont();
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
	        
	        String fontName = (String)pairs.getKey();

			log.info("\n\n" + fontName);
	        
	        String normalisedFontName = normalise(fontName);
	        if (fontMappings.get(normalisedFontName) != null) {
	        	continue;
	        }
	        
			boolean foundAwtMapping = false;
			boolean foundPdfMapping = false;
			
			FontMapping fm = new FontMapping();
			// This will be the key
			
//			fm.setMicrosoftFontName(normalisedFontName);
			
			// Panose setup
			org.docx4j.wml.FontPanose panose = null;
			Fonts.Font font = fontsInFontTable.get(normalisedFontName);
			if (font==null) {
				log.error("Font " + normalisedFontName + "not found in font table!");
				
				// TODO - should keep a reasonably complete font table we can use
				// to get Panose values if the one in the document is missing or incomplete
			} else {
				panose = font.getPanose1();
			}
			org.apache.fop.fonts.Panose fopPanose = null;
			if (panose!=null && panose.getVal()!=null ) {
				fopPanose = new org.apache.fop.fonts.Panose(panose.getVal() );
				System.out.println(".. " + fopPanose.toString() );					
				
			} else {
				System.out.println(".. no panose info!!!");															
			}
			
			
			// First, is the actual font available?
			// 1A Windows - Is the actual font available?
	        if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS")>-1) {
	        	// Windows
	        	
	        	// TODO
	        	// Filename f = new java.util.
	        	
	        		// Does it exist?
	        	
	        	// if so ..
//	        	foundPdfMapping = true;
//	        	fm.setPdfSubstituteFont();
//	        	fm.setPdfEmbeddedFile()
	        }
	        
	        // 1B
	        if (physicalFontMap.get(normalise(fontName)) != null) {
				EmbedFontInfo nfontInfo = ((EmbedFontInfo)physicalFontMap.get(normalise(fontName)));

		        if (!nfontInfo.isEmbeddable() ) {
		        	log.info(fontName + " is not embeddable; skipping.");
		        } else {
				
					System.out.println(fontName + " --> NATIVE");
		        	// if so ..
		        	foundPdfMapping = true;
		        	fm.setEmbeddedFile( ((EmbedFontInfo)physicalFontMap.get(normalise(fontName))).getEmbedFile());
		        	
					// sanity check using Panose (since 
					// a font could conceivably have the same name
					// but quite different content)				
					if (nfontInfo.getPanose() == null ) {
						System.out.println(".. and lacking Panose!");					
					} else if (fopPanose!=null ) {
					        long pd = fopPanose.difference(nfontInfo.getPanose().getPanoseArray());
							System.out.println(".. panose distance: " + pd);					
					}
		        	
					// We're done with this font.
		        	fm.setTripletName( ((EmbedFontInfo)physicalFontMap.get(normalise(fontName))).getFontTriplets(), normalise(fontName) );				
					fontMappings.put(normalisedFontName, fm);
					
					log.info("Entry added for: " +  normalisedFontName);
					continue;
		        }
			} 
	        
			// Second, what about a panose match?
	        // TODO - only do this for latin fonts!
			String physicalFontKey = null;
			String panoseKey = null;
			if (fopPanose!=null ) {
				
				// NB org.apache.fop.fonts.Panose only exists in our patched FOP
				
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
			        Map.Entry mapPairs = (Map.Entry)it.next();
			        			        
			        physicalFontKey = (String)mapPairs.getKey();
			        EmbedFontInfo fontInfo = (EmbedFontInfo)mapPairs.getValue();
			        
			        if (!fontInfo.isEmbeddable() ) {			        	
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
			        	log.info(physicalFontKey + " is not embeddable; skipping.");
			        	continue;
			        } 
			        
			        if (fontInfo.getPanose() == null ) {			        	
			        	//log.info(physicalFontKey + " has no Panose data; skipping.");
			        	continue;
			        }
			        
			        long panoseMatchValue = fopPanose.difference(fontInfo.getPanose().getPanoseArray());
			        
			        if (bestPanoseMatchValue==-1 || panoseMatchValue < bestPanoseMatchValue ) {
			        	
			        	bestPanoseMatchValue = panoseMatchValue;
			        	matchingPanoseString = fontInfo.getPanose().toString();
			        	panoseKey = physicalFontKey;
			        	
			        	//System.out.println("Candidate " + panoseMatchValue + "  (" + panoseKey + ") " + matchingPanoseString);
			        	
			        	if (bestPanoseMatchValue==0) {
			        		
			        		// Can't do any better than this!
			        		continue; // this is just the inner while
			        	}
			        	
			        	
			        } else {
			        	//System.out.println("not small " + panoseMatchValue + "  " + fontInfo.getPanose().toString() );
			        	
			        }
			    }

				if (panoseKey!=null && bestPanoseMatchValue < org.apache.fop.fonts.Panose.MATCH_THRESHOLD) {
					log.info("MATCHED " + panoseKey + " --> " + matchingPanoseString + " distance " + bestPanoseMatchValue);					
					log.debug(fontName + " --> " + ((EmbedFontInfo)physicalFontMap.get(panoseKey)).getEmbedFile() );
					
		        	fm.setEmbeddedFile( ((EmbedFontInfo)physicalFontMap.get(panoseKey)).getEmbedFile());					
					
		        	fm.setTripletName( ((EmbedFontInfo)physicalFontMap.get(panoseKey)).getFontTriplets(), panoseKey );		        	
		        	
					// Out of interest, is this match in font substitutions table?
					FontSubstitutions.Replace rtmp = (FontSubstitutions.Replace) replaceMap.get(normalise(fontName));
					if (rtmp!=null && rtmp.getSubstFonts()!=null) {
						if (rtmp.getSubstFonts().contains(panoseKey) ) {
							System.out.println("(consistent with explicit substitutes)");
						} else {
							System.out.println("(lucky, since this is missing from explicit substitutes)");							
						}
						
					}
					
					// TODO - add corresponding AWT font
					
					fontMappings.put(normalisedFontName, fm);
					log.info("Entry added for: " +  normalisedFontName);
					continue; // we're done
				} 
				
				
			} else {
				System.out.println(" --> null Panose");				
			}
	        
			// Finally, try explicit font substitutions
			// - most likely to be useful for a font that doesn't have panose entries
			log.debug("So try explicit font substitutions table");					        
			FontSubstitutions.Replace replacement = (FontSubstitutions.Replace) replaceMap
					.get(normalise(fontName));
			if (replacement != null) {
				// System.out.println( "\n" + fontName + " found." );
				// String subsFonts = replacement.getSubstFonts();

				// Is there anything in subsFonts we can use?
				String[] tokens = replacement.getSubstFonts().split(";");
				
				// PDF
				if (!foundPdfMapping) {
					for (int x = 0; x < tokens.length; x++) {
						// System.out.println(tokens[x]);
						if (physicalFontMap.get(tokens[x]) != null) {
							
							EmbedFontInfo embedFontInfo = (EmbedFontInfo)physicalFontMap.get(tokens[x]);

					        if (!embedFontInfo.isEmbeddable() ) {			        	
					        	log.info(tokens[x] + " is not embeddable; skipping.");
					        } else {
								String physicalFontFile = embedFontInfo.getEmbedFile();
								log.debug("PDF: " + fontName + " --> "
										+ physicalFontFile);
								foundPdfMapping = true;
					        	fm.setTripletName( embedFontInfo.getFontTriplets(), tokens[x] );				
					        	fm.setEmbeddedFile( physicalFontFile);
								
								// Out of interest, does this have a Panose value?
								// And what is the distance?
								if (embedFontInfo.getPanose() == null ) {
									System.out.println(".. as expected, lacking Panose");					
								} else if (fopPanose!=null  ) {
								        long pd = fopPanose.difference(embedFontInfo.getPanose().getPanoseArray());
								        
								        if (pd >= org.apache.fop.fonts.Panose.MATCH_THRESHOLD) {						        
								        	log.debug(".. with a panose distance exceeding threshold: " + pd);
								        } else {
								        	// Sanity check
								        	log.error(".. with a low panose distance (! How did we get here?) : " + pd);						        	
								        }									
								}						
												        	
								break;
					        }
						} else {
							// System.out.println("no match on token " + x + ":"
							// + tokens[x]);
						}	
					}
				}

				// AWT				
				// TODO - replace this.  See http://www.krugle.org/examples/p-xGdIjpq67jXKmBJt/FreeStandingAndSystemFonts.txt				
				for (int x = 0; x < tokens.length; x++) {
					// System.out.println(tokens[x]);
					if (awtFontFamilyNames.get(tokens[x]) != null) {
						log.debug("AWT: " + tokens[x] );
						fm.setAwtSubstituteFont(tokens[x]);
						foundAwtMapping = true;
						break;
					} else {
						//log.debug("AWT: !" + fontName );
					}
				}
				
				
				if (!foundAwtMapping) {
					log.debug("AWT: !" + fontName  + " -->  Couldn't find any of "
							+ replacement.getSubstFonts());
				}
				if (!foundPdfMapping) {
					log.debug("PDF: !" + fontName  + " -->  Couldn't find any of "
							+ replacement.getSubstFonts());
				}

			} else {
				System.out.println("Nothing in FontSubstitutions.xml for: "
						+ fontName);
				
				// TODO - add default fallback values
				
			}
			
			fontMappings.put(normalisedFontName, fm);
			log.info("Entry added for: " +  normalisedFontName);
		}
		
		
	}
	
	public class FontMapping {

//		String microsoftFontName;
		
		// Get rid of this?  The AWT font is created from the 
		// same TTF as we use for PDF.
		// See See http://www.krugle.org/examples/p-xGdIjpq67jXKmBJt/FreeStandingAndSystemFonts.txt
		String awtSubstituteFont;
				
		String embeddedFile;
		
		/** The actual name of the font, used for embedding. */
		String tripletName;

		public String getTripletName() {
			return tripletName;
		}

		public void setTripletName(String tripletName) {
			this.tripletName = tripletName;
		}

		public String setTripletName(List fontTriplets, String normalisedFontName) {
			            
        	for (Iterator iterIn = fontTriplets.iterator() ; iterIn.hasNext();) {
        		FontTriplet triplet = (FontTriplet)iterIn.next();
        		
        		if (normalise(triplet.getName()).equals(normalisedFontName) ) {
        			this.tripletName = triplet.getName();
            		log.debug("Real name for " + normalisedFontName + " --> " + triplet.getName() );
        			return triplet.getName();
        		}
        	}
    		log.error("Couldn't get Real name for " + normalisedFontName );
    		return null;        	
        }
		
		
//		public String getMicrosoftFontName() {
//			return microsoftFontName;
//		}
//
//		public void setMicrosoftFontName(String microsoftFontName) {
//			this.microsoftFontName = microsoftFontName;
//		}

		public String getAwtSubstituteFont() {
			return awtSubstituteFont;
		}

		public void setAwtSubstituteFont(String awtSubstituteFont) {
			this.awtSubstituteFont = awtSubstituteFont;
		}


		public String getEmbeddedFile() {
			return embeddedFile;
		}

		public void setEmbeddedFile(String embeddedFile) {
			this.embeddedFile = embeddedFile;
		}
	}

}
