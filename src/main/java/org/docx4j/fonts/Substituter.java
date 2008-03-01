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
	
	java.util.Map MicrosoftFontsInDocument;

	public Substituter(Map microsoftFontsInDocument) {
		super();
		MicrosoftFontsInDocument = microsoftFontsInDocument;
	}

	public Substituter() {
		super();
	}

	
	public Map fontMappings;
	
	public static void main(String[] args) throws Exception {

		//String inputfilepath = "/home/jharrop/workspace200711/docx4j-001/sample-docs/Word2007-fonts.docx";
		//String inputfilepath = "C:\\Users\\jharrop\\workspace\\docx4j\\sample-docs\\Word2007-fonts.docx";
		String inputfilepath = "/home/jharrop/workspace200711/docx4j-001/sample-docs/fonts-modesOfApplication.docx";
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		
		// For testing, use all the fonts defined in the fonts part 
		FontTablePart fontTablePart= wordMLPackage.getMainDocumentPart().getFontTablePart();		
		org.docx4j.wml.Fonts fonts = (org.docx4j.wml.Fonts)fontTablePart.getJaxbElement();		
		List<Fonts.Font> fontList = fonts.getFont();
	
		Substituter s = new Substituter();
		
		// Standard setup
		s.setup();
		
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
		
		
	}
	
	// For Xalan
	public static String getPdfSubstituteFont(Substituter s, String documentStyleId) {
		
		return s.getPdfSubstituteFont(documentStyleId);
	}
	
	public String getPdfSubstituteFont(String documentStyleId) {
		
		if (documentStyleId==null) {
			log.error("passed null documentStyleId");
			return "nullInputToExtension";
		}
		
		FontMapping fontMapping = (FontMapping)fontMappings.get(normalise(documentStyleId));

		if (fontMapping==null) {
			log.error("no mapping for:" + normalise(documentStyleId));
			return "noMappingFor" + normalise(documentStyleId);
		}
		
		log.error(documentStyleId + " -> " + fontMapping.getPdfSubstituteFont());
		
		return fontMapping.getPdfSubstituteFont();
		
	}
	
	public void populateFontMappings(Map documentFontNames) throws Exception {
		
		setup();
		
		log.info("\n\n Populating font mappings.");
		
		fontMappings = new HashMap();
		
		// Go through the font names, and determine which ones we can render!
		
		Iterator it = documentFontNames.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        
	        if(pairs.getKey()==null) {
	        	log.info("Skipped null key");
	        	pairs = (Map.Entry)it.next();
	        }
	        
	        String fontName = (String)pairs.getKey();

			log.info("\n Populating for font: " + fontName);
	        
	        String normalisedFontName = normalise(fontName);
	        
			boolean foundAwtMapping = false;
			boolean foundPdfMapping = false;
			
			FontMapping fm = new FontMapping();
			// This will be the key
			fm.setMicrosoftFontName(normalisedFontName);
			
			// Windows - Is the actual font available?
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
	        
	        
			// First, is the actual font available?
	        if (physicalFontMap.get(normalise(fontName)) != null) {
				System.out.println(fontName + " --> NATIVE");
	        	// if so ..
	        	foundPdfMapping = true;
	        	fm.setPdfSubstituteFont(normalise(fontName));
	        	fm.setPdfEmbeddedFile( (String)physicalFontMap.get(normalise(fontName)));
			} 
	        
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
							String physicalFontFile = (String) physicalFontMap
									.get(tokens[x]);
							log.debug("PDF: " + fontName + " --> "
									+ physicalFontFile);
							foundPdfMapping = true;
				        	fm.setPdfSubstituteFont(tokens[x]);
				        	fm.setPdfEmbeddedFile( physicalFontFile);
							break;
						} else {
							// System.out.println("no match on token " + x + ":"
							// + tokens[x]);
						}	
					}
				}

				// AWT
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
			}
			
			fontMappings.put(normalisedFontName, fm);
			log.info("Entry added for: " +  normalisedFontName);
		}
		
		
	}
	
	private void setup() throws Exception {
		
		// Microsoft Fonts
		// 1. On Microsoft platform, to embed in PDF output
		// 2. docx4all - all platforms - to populate font dropdown list	
		setupMicrosoftFontFilenames();
		
		////////////////////////////////////////////////////////////////////////////////////
		// Get candidate substitutions 
		// On a non-MS platform, we need these for two things:
		// 1.  to embed this font in the PDF output, in place of MS font
		// 2.  in docx4all, use in editor
		setupCandidateSubstitutionsMap();
		
		// Map of all physical fonts (normalised names) to file paths 
		setupPhysicalFonts();	
		
		////////////////////////////////////////////////////////////////////////////////////
		// What fonts are available to AWT
		setupAwtFontFamilyNames();
			
		
	}
	
	
	HashMap msFontsFilenames = new HashMap(); 
	private void setupMicrosoftFontFilenames() throws Exception {
		
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
		
		msFontsFilenames = new HashMap(); 
		
		for (MicrosoftFonts.Font font : msFontsList ) {
			msFontsFilenames.put(font.getName(), font);
				//System.out.println( "put " + font.getName() );
		}
		
	}
	
	Map replaceMap = new HashMap();
	private void setupCandidateSubstitutionsMap() throws Exception {
		
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
	private Map physicalFontMap = new HashMap();
	private void setupPhysicalFonts() throws Exception {
		
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
                	physicalFontMap.put(normalise(triplet.getName()), fontInfo.getEmbedFile() );
                	
                	// Uncomment this to see ...
            		// System.out.println("Added " + triplet.getName() + " -> " + fontInfo.getEmbedFile());
            	}
            	
            }
        }
        
        // http://www.brawer.ch/software/fonts/doc/gnu/java/awt/font/opentype/NameDecoder.html 
        //    can be used to interrogate the TTF file. Also http://www.brawer.ch/software/fonts/
	}
	
	private Map awtFontFamilyNames = new HashMap();
	private void setupAwtFontFamilyNames() {
		
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
	
	
	java.lang.CharSequence target = (new String(" ")).subSequence(0, 1);
    java.lang.CharSequence replacement = (new String("")).subSequence(0, 0);
	
	// Remove spaces and make lower case        
	String normalise(String realName) {		
        return realName.replace(target, replacement).toLowerCase();
	}
	
		
	public class FontMapping {

		String microsoftFontName;
		
		String awtSubstituteFont;
		
		String pdfSubstituteFont;
		
		String pdfEmbeddedFile;

		public String getMicrosoftFontName() {
			return microsoftFontName;
		}

		public void setMicrosoftFontName(String microsoftFontName) {
			this.microsoftFontName = microsoftFontName;
		}

		public String getAwtSubstituteFont() {
			return awtSubstituteFont;
		}

		public void setAwtSubstituteFont(String awtSubstituteFont) {
			this.awtSubstituteFont = awtSubstituteFont;
		}

		public String getPdfSubstituteFont() {
			log.debug("Returning " + pdfSubstituteFont);
			return pdfSubstituteFont;
		}

		public void setPdfSubstituteFont(String pdfSubstituteFont) {
			this.pdfSubstituteFont = pdfSubstituteFont;
		}

		public String getPdfEmbeddedFile() {
			return pdfEmbeddedFile;
		}

		public void setPdfEmbeddedFile(String pdfEmbeddedFile) {
			this.pdfEmbeddedFile = pdfEmbeddedFile;
		}
	}
}
