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

package org.docx4j.openpackaging.parts.WordprocessingML;

import org.docx4j.Docx4jProperties;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.fonts.fop.fonts.*;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public class ObfuscatedFontPart extends BinaryPart {

	private static Logger log = LoggerFactory.getLogger(ObfuscatedFontPart.class);		
	
	java.io.File f; // the temp embedded font file

    /** font cache file path */
    private static final String TEMPORARY_FONT_DIR = "temporary embedded fonts";
	
    private static File tmpFontDir = null; 
    
    static {
    	
    	String tmpFontDirPath = Docx4jProperties.getProperty("docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart.tmpFontDir");
    	
    	if (tmpFontDirPath==null) {
	        String defaultTmpPath = System.getProperty("java.io.tmpdir");

	        log.warn("Temporary font dir not configured; consider setting property 'docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart.tmpFontDir'");
	        log.info("Using {} as tmpFontDir", defaultTmpPath);

			tmpFontDir = new File(new File(defaultTmpPath), TEMPORARY_FONT_DIR);
			tmpFontDir.mkdir();
    	} else {

            tmpFontDir = new File(tmpFontDirPath);
            if (!tmpFontDir.exists() ) {
            	log.info(tmpFontDirPath + " does not exist. Attempting to create..");
	            tmpFontDir.mkdir();
            } else if (!tmpFontDir.isDirectory()) {
            	log.info(tmpFontDirPath + " exists, but is not a directory!") ;          	
            }
    	
    	}
    }
    
    public static String getTemporaryEmbeddedFontsDir() {
    	
    	if (tmpFontDir==null) {
    		throw new RuntimeException("No dir configured for temp fonts!  Either set system property user.home to a writable dir, or configure docx4j property 'docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart.tmpFontDir'");
    	}
    	try {
			return tmpFontDir.getCanonicalPath();
		} catch (IOException e) {
			log.error(e.getMessage(),e);
			return null;
		}
    }
    
    private static File getUserHome() {
        String s = System.getProperty("user.home");
        if (s != null) {
            File userDir = new File(s);
            if (userDir.exists()) {
                return userDir;
            }
        }
        return null;
    }
    

	public ObfuscatedFontPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();				
	}
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_FONT));

		// Used when this Part is added to a rels
		// TODO
		//setRelationshipType(Namespaces.);
	}
	
	
	/**
	 * deObfuscate this font, and save it using fontName
	 * 
	 * @param fontNameAsInTablePart - the name to save the font as. We
	 * could read the font name from the deObfuscated data,
	 * but FontLoader can't readily load from a byte array. 
	 * @param fontKey
	 */
	public PhysicalFont deObfuscate(String fontNameAsInTablePart, String fontFileName, String fontKey, String filenamePrefix ) {
		
		/*  NB deobfuscation is done multiple times during PDF output.
		 *  
		 *  This could be avoided, if we cloned the fontMapper.
		 *  TODO for 3.3 
		 *  
		 *  // (new Throwable()).printStackTrace(); 
		 */
		
		byte[] fontData = this.getBytes();
		
		log.debug("bytes: " + fontData.length);
		
		log.info("deObfuscating '" + fontFileName + "' with fontkey: " + fontKey);			
		// INPUT: {1DF903E3-2F14-4575-8028-881FEBABF2AB}

		// See http://openiso.org/Ecma/376/Part4/2.8.1
		// for how to do this.
		
		
		// First, strip {,}
		String tmpString = fontKey.substring(1, fontKey.length()-1);
		log.debug(tmpString);
		
		// Now strip -
		String guidString = tmpString.replace(target, replacement);
		log.debug(guidString);

		
		// Make the font key into a byte array
		byte[] guidByteArray = new byte[16];
		for (int i = 0; i < guidByteArray.length; i++) {
			guidByteArray[i] = fromHexString(guidString.substring(i * 2,
					(i * 2) + 2));
		}
		
		// XOR the reverse of the guidByteArray with 
		// the first and second 16 bytes 
		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < 16; i++) {
				fontData[(j * 16) + i] ^= guidByteArray[15 - i];  // Reverse happens here
			}
		}
		
		// Save the result
		f = new File(tmpFontDir, filenamePrefix + "-"+fontFileName +".ttf");
		f.deleteOnExit();
		String path = null; 
		
		java.io.FileOutputStream fos = null; 
		try {
			path = f.getCanonicalPath();
			fos = new java.io.FileOutputStream(f);
			fos.write(fontData);
			log.debug("wrote: " + fontData.length);
			fos.close();
		} catch (IOException e) {
			log.error("Problem with " + path);
			log.error(e.getMessage(), e);
		} 
		
//		log.info("Done!");
		
		// Save to "Temporary Font Files" directory.
		// TODO 1 write to a subdir controlled by FontTablePart
		// TODO 2 add a method there which can be called to delete the dir when the docx is closed/unloaded
        FontResolver fontResolver = FontSetup.createMinimalFontResolver();

		if (log.isDebugEnabled()) {
	        CustomFont customFont = null;
	        try {
	        	log.debug("Loading from: " + path);
	        	String subFontName = null; // TODO set this if its a TTC
	        	boolean embedded = true;   
	        	boolean useKerning = true;
	            customFont = FontLoader.loadFont("file:" + path, 
	            		subFontName, embedded, EncodingMode.AUTO, useKerning, fontResolver);
	        } catch (Exception e) {
				e.printStackTrace();
	        }
	        if (customFont!=null) {
	        	log.info("Successfully reloaded " + customFont.getFontName());
	        	if (customFont.isEmbeddable()) {
	        		log.debug("confirmed embeddable");
	        	} else {
	        		// Sanity check
	        		log.error("this embedded font claims it is not embeddable!");        		
	        	}
	        }
		}

		// Get this font as a PhysicalFont object; do NOT add it to physical fonts (since those are available to all documents)  
        try {
					List<PhysicalFont> fonts = PhysicalFonts.getPhysicalFont(fontNameAsInTablePart, new java.net.URL("file:" + path));
					return (fonts == null || fonts.isEmpty()) ? null : fonts.iterator().next();


			// This needs to be done before populateFontMappings, 
			// otherwise this font will be ignored, and references
			// to it mapped to some substitute font!
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	
	public static byte fromHexString( String hexStr ){
    	byte firstNibble  = Byte.parseByte(hexStr.substring(0,1),16); 
    	byte secondNibble = Byte.parseByte(hexStr.substring(1,2),16);
    	int finalByte = (secondNibble) | (firstNibble << 4 ); 
    	return (byte) finalByte;
	}
	
	
	static java.lang.CharSequence target = (new String("-")).subSequence(0, 1);
    static java.lang.CharSequence replacement = (new String("")).subSequence(0, 0);
	
    protected static void deleteEmbeddedFontTempFiles(String filenamePrefix) {
    	
    	// this isn't really necessary given finalize(), but this gets rid of them a bit sooner than GC may happen
    	// (when it is invoked first; sometimes it isn't - note this is a static method)
    	
    	for(File f: tmpFontDir.listFiles() ) {
    		
    	    if(f.getName().startsWith(filenamePrefix)) {
    	        f.delete();
    	    }
    	}
    	if (log.isWarnEnabled()) {
    		int count = tmpFontDir.listFiles().length;
    		if (count>0) {
	    		try {
					log.warn(count + " files remain in " + tmpFontDir.getCanonicalPath());
				} catch (IOException e) {
				}
	    	}
    	}
    }
	
	@Override
	protected void finalize() throws Throwable {
		
        try {
        	if (f!=null) {
	    		log.debug("Deleting  " + f.getName());
				f.delete();
        	}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			super.finalize();
		}
		
	}
	
}
