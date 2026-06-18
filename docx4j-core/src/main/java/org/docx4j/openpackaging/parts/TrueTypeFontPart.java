/*
 *  Copyright 2019, Plutext Pty Ltd.
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

package org.docx4j.openpackaging.parts;

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
import org.docx4j.fonts.fop.fonts.FontSetup;
import org.docx4j.fonts.fop.fonts.FontUris;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An embedded TTF which is not obfuscated.
 * 
 * @author jharrop
 * @since 8.1.1
 */
public class TrueTypeFontPart extends AbstractFontPart {

	private static Logger log = LoggerFactory.getLogger(TrueTypeFontPart.class);		
	

	public TrueTypeFontPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();				
	}
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.TRUETYPE_FONT));

		// Used when this Part is added to a rels
		setRelationshipType(Namespaces.FONT);
	}
	
	private String fontKey;
	
	public String getFontKey() {
		return fontKey;
	}

	/**
	 * extract this font, and save it using fontName
	 * 
	 * @param fontNameAsInTablePart - the name to save the font as. We
	 * could read the font name from the deObfuscated data,
	 * but FontLoader can't readily load from a byte array. 
	 * @param fontKey
	 */
	public PhysicalFont extract(String fontNameAsInTablePart, String fontFileName, String fontKey, Set<File> embeddedFontTempFiles ) {
		
		this.fontKey = fontKey;
		
		byte[] fontData = this.getBytes();
		
		log.debug("bytes: " + fontData.length);
		
		java.io.FileOutputStream fos = null; 
		String path = null; 
		try {
			// Save the result
			setF(createFile(fontFileName));
			embeddedFontTempFiles.add(getF());
			getF().deleteOnExit();

			path = getF().getCanonicalPath();
			fos = new java.io.FileOutputStream(getF());
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
//        FontResolver fontResolver = FontSetup.createMinimalFontResolver();
		URI baseUri=null;
		try {
			baseUri = new URI("/");
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}  
		InternalResourceResolver fontResolver = new InternalResourceResolver(baseUri,
				ResourceResolverFactory.createDefaultResourceResolver()) ;

		if (log.isDebugEnabled()) {
	        CustomFont customFont = null;
	        try {
	        	log.debug("Loading from: " + path);
	        	String subFontName = null; // TODO set this if its a TTC
	        	boolean embedded = true;   
	        	boolean useKerning = true;
	        	boolean useAdvanced = false;
	        	boolean simulateStyle = false;
	        	boolean embedAsType1 = false;
	        	boolean useSVG = false;
	        	
	        	FontUris fontUris = new FontUris(getF().toURI(), null);
	        	
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
//        try {
          

		// Where the important stuff happens 
		List<PhysicalFont> fonts = PhysicalFonts.getPhysicalFont(fontNameAsInTablePart, getF().toURI());
		return (fonts == null || fonts.isEmpty()) ? null : fonts.iterator().next();


			// This needs to be done before populateFontMappings, 
			// otherwise this font will be ignored, and references
			// to it mapped to some substitute font!
			
//		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
		
	}

	/**
	 * Obfuscate this TrueType font, in accordance
	 * with ECMA-376 Part 4, 2.8.1, so it can form the data of an ObfuscatedFontPart.
	 * 
	 * Useful where an existing docx (perhaps created with Google Docs) contains this non-obfuscated part.
	 */
	public byte[] obfuscate() {
		return obfuscate(this.fontKey);		
	}

	/**
	 * Obfuscate this TrueType font using the supplied fontKey, in accordance
	 * with ECMA-376 Part 4, 2.8.1, so it can be converted to an ObfuscatedFontPart.
	 *
	 * @param fontKey the w:fontKey value, for example "{1DF903E3-2F14-4575-8028-881FEBABF2AB}"
	 * @return a new byte array containing the obfuscated font data
	 */
	public byte[] obfuscate(String fontKey) {
		return obfuscate(fontKey, this.getBytes());
	}	
	

	
}
