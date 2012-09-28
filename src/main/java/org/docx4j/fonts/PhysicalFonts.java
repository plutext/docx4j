package org.docx4j.fonts;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.docx4j.fonts.fop.fonts.EmbedFontInfo;
import org.docx4j.fonts.fop.fonts.FontCache;
import org.docx4j.fonts.fop.fonts.FontResolver;
import org.docx4j.fonts.fop.fonts.FontSetup;
import org.docx4j.fonts.fop.fonts.FontTriplet;
import org.docx4j.fonts.fop.fonts.autodetect.FontFileFinder;
import org.docx4j.fonts.fop.fonts.autodetect.FontInfoFinder;
import org.docx4j.fonts.microsoft.MicrosoftFonts;
import org.docx4j.fonts.microsoft.MicrosoftFontsRegistry;
import org.docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart;

//import com.lowagie.text.pdf.BaseFont;

/**
 * The fonts which are physically installed on the system.
 * 
 * They can be discovered automatically, or you can
 * just add specific fonts.
 * 
 * @author dev
 *
 */
public class PhysicalFonts {

	protected static Logger log = Logger.getLogger(PhysicalFonts.class);
	
	protected static FontCache fontCache;

	
	/** These are the physical fonts on the system which we have discovered. */ 
	private final static Map<String, PhysicalFont> physicalFontMap;
	public static Map<String, PhysicalFont> getPhysicalFonts() {
		return physicalFontMap;
	}


	private final static Map<String, PhysicalFont> physicalFontMapByFilenameLowercase;
	
	
	//	private final static Map<String, PhysicalFontFamily> physicalFontFamiliesMap;
//	int lastSeenNumberOfPhysicalFonts = 0;
//	
//    
//    /** Max difference for it to be considered an acceptable match.
//     *  Note that this value will depend on the weights in the
//     *  difference function.
//     */ 
//    public static final int MATCH_THRESHOLD = 30;

    private static FontResolver fontResolver;        
	
    // parse font to ascertain font info
    private static FontInfoFinder fontInfoFinder; 

    static {
		
		try {

	        fontCache = FontCache.load();
	        if (fontCache == null) {
	            fontCache = new FontCache();
	        }			
			
			physicalFontMap = new HashMap<String, PhysicalFont>();
			physicalFontMapByFilenameLowercase 
							= new HashMap<String, PhysicalFont>();
			
//			physicalFontFamiliesMap = new HashMap<String, PhysicalFontFamily>();
			
			fontResolver = FontSetup.createMinimalFontResolver();
			
            // parse font to ascertain font info
			fontInfoFinder = new FontInfoFinder();			
			
			// setupPhysicalFonts();
			
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}

    
    private static String regex;
	public static String getRegex() {
		return regex;
	}
	
	/**
	 *  Set a regex to limit to the common fonts in order to lower memory use.
	 *  eg on Mac regex=".*(Courier New|Arial|Times New Roman|Comic Sans|Georgia|Impact|Lucida Console|Lucida Sans Unicode|Palatino Linotype|Tahoma|Trebuchet|Verdana|Symbol|Webdings|Wingdings|MS Sans Serif|MS Serif).*";
	 *  on Windows:  regex=".*(calibri|cour|arial|times|comic|georgia|impact|LSANS|pala|tahoma|trebuc|verdana|symbol|webdings|wingding).*";
	 *  
	 *  If you want to use this, set it before instantiating a Mapper. 
	 *  
	 *  @since 2.8.1
	 */
	public static void setRegex(String regex) {
		PhysicalFonts.regex = regex;
	}
    
	
	/**
	 * Autodetect fonts available on the system.
	 * 
	 */ 
	public final static void discoverPhysicalFonts() throws Exception {
		
		// Currently we use FOP - inspired by org.apache.fop.render.PrintRendererConfigurator
		// iText also has a font discoverer (which we could use
		// instead, but don't).  
		// It remains to be seen which of XSL FO or iText
		// PDF generation becomes the most popular.
		// (If it is iText, then there _may_ be something
		//  to be said for using their font discovery instead)
		
		
        FontFileFinder fontFileFinder = new FontFileFinder();
        
        // Automagically finds a list of font files on local system
        // based on os.name
        List fontFileList = fontFileFinder.find();      
        
        
        if (regex==null) {
            for (Iterator iter = fontFileList.iterator(); iter.hasNext();) {
            	
            	URL fontUrl = getURL(iter.next());
                
                // parse font to ascertain font info
            	addPhysicalFont( fontUrl);
            }
        } else {
        	Pattern pattern = Pattern.compile(regex);
            for (Iterator iter = fontFileList.iterator(); iter.hasNext();) {
            	
            	URL fontUrl = getURL(iter.next());
                
            	
                // parse font to ascertain font info
            	if (pattern.matcher(fontUrl.toString()).matches()){
            		addPhysicalFont( fontUrl);        		
            	} else {
                	log.debug("Ignoring " + fontUrl.toString() );

            	}
            }
        }
        

        // Add fonts from our Temporary Embedded Fonts dir
        fontFileList = fontFileFinder.find( ObfuscatedFontPart.getTemporaryEmbeddedFontsDir() );
        for (Iterator iter = fontFileList.iterator(); iter.hasNext();) {
            URL fontUrl = getURL(iter.next());
            addPhysicalFont( fontUrl);
        }
        
        fontCache.save();
        
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

	private static boolean loggedWarningAlready = false;
	
	/**
	 * Add a physical font's EmbedFontInfo object.
	 *
	 * @param fontUrl eg new java.net.URL("file:" + path)
	 */
	public static void addPhysicalFont(URL fontUrl) {
		addPhysicalFont(null, fontUrl);
	}

	/**
	 * Add a physical font's EmbedFontInfo object.
	 * 
	 * @param fontUrl eg new java.net.URL("file:" + path)
	 */
	public static void addPhysicalFont(String nameAsInFontTablePart, URL fontUrl) {

		
		//List<EmbedFontInfo> embedFontInfoList = fontInfoFinder.find(fontUrl, fontResolver, fontCache);		
		EmbedFontInfo[] embedFontInfoList = fontInfoFinder.find(fontUrl, fontResolver, fontCache);
		/* FOP r644208 (Bugzilla #44737) 3/04/08 made this an array,
		// so if you are using non-patched FOP, it needs to be at least this revision
		// (but doesn't seem to be in FOP 0.95 binary?!) */ 
		
		if (embedFontInfoList==null) {
			// Quite a few fonts exist that we can't seem to get
			// EmbedFontInfo for. To be investigated.
			log.warn("Aborting: " + fontUrl.toString() +  " (can't get EmbedFontInfo[] .. try deleting fop-fonts.cache?)");
			return;
		}
		
		StringBuffer debug = new StringBuffer();
		
		for ( EmbedFontInfo fontInfo : embedFontInfoList ) {
			
			/* EmbedFontInfo has:
			 * - subFontName (if the underlying CustomFont is a TTC)
			 * - PostScriptName = CustomFont.getFontName()
			 * - FontTriplets named:
			 * 		- CustomFont.getFullName() with quotes stripped
			 * 		- CustomFont.getFontName() with whitespace stripped
			 * 		- each family name        (with quotes stripped)
			 * 
			 * By creating one PhysicalFont object 
			 * per triplet, each referring to the same
			 * EmbedFontInfo, we increase the chances 
			 * of a match
			 * 
				ComicSansMS
				.. triplet Comic Sans MS (priority + 0
				.. triplet ComicSansMS (priority + 0
				
				ComicSansMS-Bold
				.. triplet Comic Sans MS Bold (priority + 0
				.. triplet ComicSansMS-Bold (priority + 0
				.. triplet Comic Sans MS (priority + 5
			 * 
			 * but the second triplet is what FOP creates where its
			 * getPostScriptName() 
			 * does FontUtil.stripWhiteSpace(getFullName());.
			 * 
			 * and the third is just the family name.
			 * 
			 * So we only get the first.
			 * 
			 */
			
			
			if (fontInfo == null) {
//				return;
				continue;
			}
			
			debug.append("------- \n");
			
			 try {
				debug.append(fontInfo.getPostScriptName() + "\n" );
				if (!fontInfo.isEmbeddable() ) {			        	
//	        	log.info(tokens[x] + " is not embeddable; skipping.");
					 
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
					 
					 //return;
				    continue;
				 }
			} catch (Exception e1) {
				// NB isEmbeddable() only exists in our patched FOP
				if (!loggedWarningAlready) {
					log.warn("Not using patched FOP; isEmbeddable() method missing.");
					loggedWarningAlready = true;
				}				
			}
				
			PhysicalFont pf; 
			
//			for (Iterator iterIn = fontInfo.getFontTriplets().iterator() ; iterIn.hasNext();) {
//				FontTriplet triplet = (FontTriplet)iterIn.next();
			
				FontTriplet triplet = (FontTriplet)fontInfo.getFontTriplets().get(0); 
				// There is one triplet for each of the font family names
				// this font has, and we create a PhysicalFont object 
				// for each of them.  For our purposes though, each of
				// these physical font objects contains the same info
		    	
		        String lower = fontInfo.getEmbedFile().toLowerCase();
		        log.debug("Processing physical font: " + lower);
				debug.append(".. triplet " + triplet.getName() 
						+ " (priority " + triplet.getPriority() +"\n" );
		        		        
		        pf = null;
		        // xhtmlrenderer's org.xhtmlrenderer.pdf.ITextFontResolver.addFont
		        // can handle
		        // .otf, .ttf, .ttc, .pfb
		        if (lower.endsWith(".otf") || lower.endsWith(".ttf") || lower.endsWith(".ttc") ) {
		        	pf = new PhysicalFont(triplet.getName(), fontInfo, fontResolver);
		        } else if (lower.endsWith(".pfb") ) {
		        	// See whether we have everything org.xhtmlrenderer.pdf.ITextFontResolver.addFont
		        	// will need - for a .pfb file, it needs a corresponding .afm or .pfm
					String afm = FontUtils.pathFromURL(lower);
					afm = afm.substring(0, afm.length()-4 ) + ".afm";  // drop the 'file:'
					log.debug("Looking for: " + afm);					
					File f = new File(afm);
			        if (f.exists()) {
			        	
			        	log.debug(".. found");

// Uncomment if you want to use the iText stuff in docx4j-extras			        	
//			        	// We're only interested if this font supports UTF-8 encoding
//			        	// since otherwise iText can't use it (at least on a
//			        	// UTF8 encoded XHTML document) 
//			        	try {
//			    		    BaseFont bf = BaseFont.createFont(afm,
//			    		    		BaseFont.IDENTITY_H, 
//									BaseFont.NOT_EMBEDDED);
//						} catch (java.io.UnsupportedEncodingException uee) {
//							log.error(afm + " does not support UTF encoding, so ignoring");
//							continue;
//						} catch (Exception e) {
//							log.error(e);
//							continue;
//						}
			        	pf = new PhysicalFont(triplet.getName(),fontInfo, fontResolver);
			        	
			        	
			        } else {
			        	// Should we be doing afm first, or pfm?
						String pfm = FontUtils.pathFromURL(lower);
						pfm = pfm.substring(0, pfm.length()-4 ) + ".pfm";  // drop the 'file:'
						log.debug("Looking for: " + pfm);
						f = new File(pfm);
				        if (f.exists()) {				
				        	log.debug(".. found");

				        	// Uncomment if you want to use the iText stuff in docx4j-extras			        					        	
//				        	// We're only interested if this font supports UTF-8 encoding
//				        	try {
//				    		    BaseFont bf = BaseFont.createFont(pfm,
//				    		    		BaseFont.IDENTITY_H, 
//										BaseFont.NOT_EMBEDDED);
//							} catch (java.io.UnsupportedEncodingException uee) {
//								log.error(pfm + " does not support UTF encoding, so ignoring");
//								continue;
//							} catch (Exception e) {
//								log.error(e);
//								continue;
//							}
				        	pf = new PhysicalFont(triplet.getName(), fontInfo, fontResolver);
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

				if (nameAsInFontTablePart != null && !physicalFontMap.containsKey(nameAsInFontTablePart)) {
					physicalFontMap.put(nameAsInFontTablePart, pf);
					log.debug("Added " + nameAsInFontTablePart + " -> " + pf.getEmbeddedFile());
				}
		    		
		    		// We also need to add it to map by filename
		    		String filename = pf.getEmbeddedFile();
		    		filename = filename.substring( filename.lastIndexOf("/")+1).toLowerCase();
		    		physicalFontMapByFilenameLowercase.put(filename, pf);
		    		log.debug("added to filename map: " + filename);
		        	
//		        	String familyName = triplet.getName();
//		        	pf.setFamilyName(familyName);
//		        	
//		        	PhysicalFontFamily pff;
//		        	if (physicalFontFamiliesMap.get(familyName)==null) {
//		        		pff = new PhysicalFontFamily(familyName);
//		        		physicalFontFamiliesMap.put(familyName, pff);
//		        	} else {
//		        		pff = physicalFontFamiliesMap.get(familyName);
//		        	}
//		        	pff.addFont(pf);
		        	
		        }
			}            	
		
		log.debug(debug.toString() );
	}
	
	public static PhysicalFont getBoldForm( PhysicalFont pf) {
		
		// look up the font in MicrosoftFontsRegistry
		MicrosoftFonts.Font msFont = MicrosoftFontsRegistry.getMsFonts().get(pf.getName() );
		
		if (msFont==null) {
			log.warn("No entry in MicrosoftFontsRegistry for: " + pf.getName());
			return null;
		}
		
		if (msFont.getBold()==null) {
			log.debug("No bold form for: " + pf.getName());
			return null;
		} else {
			
			// We have to go via the file name, grrr..
			// since MicrosoftFonts.xml doesn't give the associate font name
			String filename = msFont.getBold().getFilename().toLowerCase();
			log.debug("Fetching: " + filename);
			return physicalFontMapByFilenameLowercase.get(filename);
		}		
	}
	
	public static PhysicalFont getBoldItalicForm( PhysicalFont pf) {
		
		// look up the font in MicrosoftFontsRegistry
		MicrosoftFonts.Font msFont = MicrosoftFontsRegistry.getMsFonts().get(pf.getName() );
		
		if (msFont==null) {
			log.warn("No entry in MicrosoftFontsRegistry for: " + pf.getName());
			return null;
		}
		
		if (msFont.getBolditalic()==null) {
			log.debug("No Bolditalic form for: " + pf.getName());
			return null;
		} else {
			
			// We have to go via the file name, grrr..
			// since MicrosoftFonts.xml doesn't give the associate font name
			String filename = msFont.getBolditalic().getFilename().toLowerCase();
			log.debug("Fetching: " + filename);
			return physicalFontMapByFilenameLowercase.get(filename);
		}		
	}
	
	public static PhysicalFont getItalicForm( PhysicalFont pf) {
		
		// look up the font in MicrosoftFontsRegistry
		MicrosoftFonts.Font msFont = MicrosoftFontsRegistry.getMsFonts().get(pf.getName() );
		
		if (msFont==null) {
			log.debug("No entry in MicrosoftFontsRegistry for: " + pf.getName());
			return null;
		}
		
		if (msFont.getItalic()==null) {
			log.info("No italic form for: " + pf.getName());
			return null;
		} else {
			
			// We have to go via the file name, grrr..
			// since MicrosoftFonts.xml doesn't give the associate font name
			String filename = msFont.getItalic().getFilename().toLowerCase();
			log.debug("Fetching: " + filename);
			return physicalFontMapByFilenameLowercase.get(filename);
		}
		
	}

	public static void main(String[] args) throws Exception {

		discoverPhysicalFonts();
		System.out.println("That should have listed your physical fonts (provided you have logging enabled).");
	}
	

}
