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
			log.error("WARNING! SubstituterWindowsPlatformImpl will only work " +
					"properly on Windows! You'll get strange results with font " +
					"handling. Use some other Substituter implementation!");
		}
		
	}
	
	protected static FontCache fontCache;
	
	
	private final static HashMap<String, MicrosoftFonts.Font> msFontsFilenames;
	private final static HashMap<String, String> filenamesToMsFontNames;
	public final static Map<String, MicrosoftFonts.Font> getMsFontsFilenames() {
		return msFontsFilenames;
	}		
		
	/** These are the physical fonts on the system which we have discovered. */ 
	private final static Map<String, PhysicalFont> physicalFontMap;
	private final static Map<String, PhysicalFontFamily> physicalFontFamiliesMap;
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
			filenamesToMsFontNames = new HashMap<String, String>();
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
			// Quite a few fonts exist that we can't seem to get
			// EmbedFontInfo for. To be investigated.
			log.warn("Aborting: " + fontUrl.toString() );
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
		        	log.warn(fontInfo.getEmbedFile() + " is not embeddable; ignoring this font.");
				 
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
		        log.debug("Processing physical font: " + lower);
		        		        
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
		    		log.debug("Added " + pf.getName() + " -> " + pf.getEmbeddedFile());                	
		        	
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
		
		// We need the documentFontNames normalised
//		Map<String, String> documentFontNamesKeyedByNormalised = new HashMap<String, String>();
//		Iterator documentFontIterator = documentFontNames.entrySet().iterator();
//	    while (documentFontIterator.hasNext()) {
//	        Map.Entry pairs = (Map.Entry)documentFontIterator.next();
//	        
//	        if(pairs.getKey()==null) {
//	        	log.info("Skipped null key");
//	        	pairs = (Map.Entry)documentFontIterator.next();
//	        }
//	        
//	        String documentFontName = (String)pairs.getKey();
//
//			log.debug("Added normalised: " + documentFontName);
//	        
//	        String normalisedFontName = normalise(documentFontName);
//	        
//	        documentFontNamesKeyedByNormalised.put(normalisedFontName, documentFontName);
//	        	// each name, keyed by its normalised value
//	    }

		// Iterate through the physical fonts, since their key is their 
		// postscript name (non-normalised).  This way, we can do a single
		// pass.
		Iterator physicalFontMapIterator = physicalFontMap.entrySet().iterator();
	    while (physicalFontMapIterator.hasNext()) {
	        Map.Entry pairs = (Map.Entry)physicalFontMapIterator.next();
	        
	        if(pairs.getKey()==null) {
	        	log.info("Skipped null key");
	        	pairs = (Map.Entry)physicalFontMapIterator.next();
	        }
		
	        String physicalFontName = (String)pairs.getKey();
			//log.debug("\n\n" + physicalFontName);	        
	        //String normalisedFontName = normalise(physicalFontName);
			String fontPath = ((PhysicalFont)pairs.getValue()).getEmbeddedFile();
			String physicalFilename = fontPath.substring( fontPath.lastIndexOf("/") +1).toLowerCase();
			
			String msFontName = filenamesToMsFontNames.get(physicalFilename);
			
			if (msFontName!=null) {
			
//				String msFontName = font.getName(); 
				
				String baseform = msFontName;
				if (msFontName.indexOf(SEPARATOR)>0) {
					baseform = msFontName.substring(0, msFontName.indexOf(SEPARATOR));
				}
				
		        // Add it to the mapping if it is present in the document
		        if ( documentFontNames.get(baseform)!=null ) {
		        	
					// for now, if the baseform is used, 
		        	// we say bold, italic, and bolditalic are as well
		        	
		        	if (pairs.getValue()==null) {
		        		log.debug("Handle that");
		        	} else {
		        		fontMappings.put(normalise(msFontName), 
			        			new FontMapping(msFontName, (PhysicalFont)pairs.getValue() ) );
		        		log.info("Added mapping for: " + normalise(msFontName));		        		
		        	}
		        	
		        } else {
		        	log.debug("Ignoring physical font " + msFontName
		        			+ ((PhysicalFont)pairs.getValue()).getEmbeddedFile() );
		        	
		        }
			} else {
				
				log.info("Unknown font: " + physicalFontName + "(" + physicalFilename);
				
			}
				
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
	
	
	public static void main(String[] args) throws Exception {

		//String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/Word2007-fonts.docx";
		String inputfilepath = "C:\\Documents and Settings\\Jason Harrop\\workspace\\docx4j-2009\\sample-docs\\Word2007-fonts.docx";
		//String inputfilepath = "/home/jharrop/workspace200711/docx4j-001/sample-docs/fonts-modesOfApplication.docx";
		//String inputfilepath = "/home/jharrop/workspace200711/docx4all/sample-docs/TargetFeatureSet.docx"; //docx4all-fonts.docx";
		
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
