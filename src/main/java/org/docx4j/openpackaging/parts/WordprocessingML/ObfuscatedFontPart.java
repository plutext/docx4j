/*
 *  Copyright 2008, Plutext Pty Ltd.
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

package org.docx4j.openpackaging.parts.WordprocessingML;

import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.FontLoader;
import org.apache.fop.fonts.FontResolver;
import org.apache.fop.fonts.FontSetup;
import org.apache.fop.fonts.autodetect.FontFileFinder;
import org.apache.fop.fonts.autodetect.FontInfoFinder;
import org.apache.log4j.Logger;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;

public class ObfuscatedFontPart extends BinaryPart {

	private static Logger log = Logger.getLogger(ObfuscatedFontPart.class);		
	
    /** docx4j's user directory name */
    private static final String DOCX4J_USER_DIR = ".docx4all"; 
    	// docx4all already creates a dir; no point creating a second 

    /** font cache file path */
    private static final String TEMPORARY_FONT_DIR = "temporary embedded fonts";
	
    private static File tmpFontDir = null; 
    
    static {
        File userHome = getUserHome();
        if (userHome != null) {
            File docx4jUserDir = new File(userHome, DOCX4J_USER_DIR);
            docx4jUserDir.mkdir();
            tmpFontDir = new File(docx4jUserDir, TEMPORARY_FONT_DIR);
            tmpFontDir.mkdir();
        }    	
    }
    
    public static String getTemporaryEmbeddedFontsDir() {
    	try {
			return tmpFontDir.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
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
	

	static final int BUFF_SIZE = 100000;
	static final byte[] buffer = new byte[BUFF_SIZE];

	public byte[] createFontDataByteArray() throws IOException{
	   InputStream in = getBinaryData();
	   ByteArrayOutputStream out = null;  
	   try {
		   out = new ByteArrayOutputStream();
	      while (true) {
	         synchronized (buffer) {
	            int amountRead = in.read(buffer);
	            if (amountRead == -1) {
	               break;
	            }
	            out.write(buffer, 0, amountRead); 
	         }
	      } 
	      return out.toByteArray();
	   } finally {
	      if (in != null) {
	         in.close();
	      }
	      if (out != null) {
	         out.close();
	      }
	   }
	}	
	
	/**
	 * deObfuscate this font, and save it using fontName
	 * 
	 * @param fontName - the name to save the font as. We
	 * could read the font name from the deObfuscated data,
	 * but FontLoader can't readily load from a byte array. 
	 * @param fontKey
	 */
	public void deObfuscate(String fontName, String fontKey ) {
		
		// Convert the input stream into a byte array
		byte[] fontData = null;
		try {
			fontData = createFontDataByteArray();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		log.debug("bytes: " + fontData.length);
		
		log.info("deObfuscating with fontkey: " + fontKey);			
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
		java.io.File f = new File(tmpFontDir, fontName +".ttf"); 
		String path = null; 
		
		java.io.FileOutputStream fos = null; 
		try {
			path = f.getCanonicalPath();
			fos = new java.io.FileOutputStream(f);
			fos.write(fontData);
			log.debug("wrote: " + fontData.length);
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		log.info("Done!");
		
		// Save to "Temporary Font Files" directory.
        FontResolver fontResolver = FontSetup.createMinimalFontResolver();

		if (log.isDebugEnabled()) {
	        CustomFont customFont = null;
	        try {
	        	log.debug("Loading from: " + path);
	            customFont = FontLoader.loadFont("file:" + path, fontResolver);
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
		
		// Add this font to our known physical fonts  
        try {
			FontInfoFinder finder = new FontInfoFinder();
			org.docx4j.fonts.Substituter.setupPhysicalFont(fontResolver, new java.net.URL("file:" + path), finder);
			
			// This needs to be done before populateFontMappings, 
			// otherwise this font will be ignored, and references
			// to it mapped to some substitute font!
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static byte fromHexString( String hexStr ){
    	byte firstNibble  = Byte.parseByte(hexStr.substring(0,1),16); 
    	byte secondNibble = Byte.parseByte(hexStr.substring(1,2),16);
    	int finalByte = (secondNibble) | (firstNibble << 4 ); 
    	return (byte) finalByte;
	}
	
	
	static java.lang.CharSequence target = (new String("-")).subSequence(0, 1);
    static java.lang.CharSequence replacement = (new String("")).subSequence(0, 0);
	
	
	
}
