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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.docx4j.Docx4jProperties;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.fonts.fop.apps.io.InternalResourceResolver;
import org.docx4j.fonts.fop.apps.io.ResourceResolverFactory;
import org.docx4j.fonts.fop.fonts.CustomFont;
import org.docx4j.fonts.fop.fonts.EmbeddingMode;
import org.docx4j.fonts.fop.fonts.EncodingMode;
import org.docx4j.fonts.fop.fonts.FontLoader;
//import org.docx4j.fonts.fop.fonts.FontResolver;
import org.docx4j.fonts.fop.fonts.FontSetup;
import org.docx4j.fonts.fop.fonts.FontUris;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.AbstractFontPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObfuscatedFontPart extends AbstractFontPart {

	private static Logger log = LoggerFactory.getLogger(ObfuscatedFontPart.class);		
	
	public ObfuscatedFontPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();				
	}
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_FONT));

		// Used when this Part is added to a rels
		setRelationshipType(Namespaces.FONT);
	}
	
	@Deprecated
	public PhysicalFont deObfuscate(String fontNameAsInTablePart, String fontFileName, String fontKey, Set<File> embeddedFontTempFiles ) {
		
		return extract( fontNameAsInTablePart,  fontFileName,  fontKey, embeddedFontTempFiles );
	}
	
	/**
	 * deObfuscate this font, and save it using fontName
	 * 
	 * @param fontNameAsInTablePart - the name to save the font as. We
	 * could read the font name from the deObfuscated data,
	 * but FontLoader can't readily load from a byte array. 
	 * @param fontKey
	 */
	public PhysicalFont extract(String fontNameAsInTablePart, String fontFileName, String fontKey, Set<File> embeddedFontTempFiles ) {
		
		byte[] fontData = this.getBytes();
		
		log.debug("bytes: " + fontData.length);
		
		if (fontData.length==0) {
			log.error(this.getF() + " is empty");
			return null;
		}
		
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
		java.io.FileOutputStream fos = null; 
		try {
			setF(createFile(fontFileName));
			embeddedFontTempFiles.add(getF());			
			getF().deleteOnExit();

			fos = new java.io.FileOutputStream(getF());
			fos.write(fontData);
			log.debug("wrote: " + fontData.length);
			fos.close();
		} catch (IOException e) {
			log.error("Problem with " + this.getF());
			log.error(e.getMessage(), e);
		} 
		
//        FontResolver fontResolver = FontSetup.createMinimalFontResolver();
		URI baseUri=null;
		try {
			baseUri = new URI("/");
		} catch (URISyntaxException e1) {
			log.error(e1.getMessage(), e1);
		}  
		InternalResourceResolver fontResolver = new InternalResourceResolver(baseUri,
				ResourceResolverFactory.createDefaultResourceResolver()) ;

		if (log.isDebugEnabled()) {
	        CustomFont customFont = null;
	        try {
				log.debug("[DEBUG] Loading from: " + this.getF());
	        	String subFontName = null; // TODO set this if its a TTC
	        	boolean embedded = true;   
	        	boolean useKerning = true;
	        	boolean useAdvanced = false;
	        	boolean simulateStyle = false;
	        	boolean embedAsType1 = false;
	        	boolean useSVG = false;
	        	
	        	FontUris fontUris = new FontUris(this.getF().toURI(), null);
	        	
	            customFont = FontLoader.loadFont(fontUris, 
	            		subFontName, embedded, EmbeddingMode.AUTO, EncodingMode.AUTO, 
	            		useKerning, useAdvanced, fontResolver,
	            		simulateStyle, embedAsType1, useSVG);
	            
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
		List<PhysicalFont> fonts = PhysicalFonts.getPhysicalFont(fontNameAsInTablePart, 
				this.getF().toURI() );
		return (fonts == null || fonts.isEmpty()) ? null : fonts.iterator().next();

		// This needs to be done before populateFontMappings, 
		// otherwise this font will be ignored, and references
		// to it mapped to some substitute font!
		
	}
	
	static java.lang.CharSequence target = (new String("-")).subSequence(0, 1);
    static java.lang.CharSequence replacement = (new String("")).subSequence(0, 0);
			
}
