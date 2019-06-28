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

//import java.io.IOException;

//import javax.xml.bind.JAXBElement;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.TrueTypeFontPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.wml.FontRel;
import org.docx4j.wml.Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class FontTablePart extends JaxbXmlPart<Fonts> {
	
	private static Logger log = LoggerFactory.getLogger(FontTablePart.class);		
	
	public FontTablePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();		
	}

	public FontTablePart() throws InvalidFormatException {
		super(new PartName("/word/fontTable.xml"));
		init();		
	}
	
	public void init() {
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new org.docx4j.openpackaging.contenttype.ContentType(
				org.docx4j.openpackaging.contenttype.ContentTypes.WORDPROCESSINGML_FONTTABLE));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.FONT_TABLE);
	}
    
    /**
     * Unmarshal a default font table, useful when creating this
     * part from scratch. 
     *
     * @return the newly created root object of the java content tree 
     *
     * @throws JAXBException 
     *     If any unexpected errors occur while unmarshalling
     */
    public Object unmarshalDefaultFonts() throws JAXBException {
    	  
    		java.io.InputStream is = null;
			try {
				// Works in Eclipse - not absence of leading '/'
				is = ResourceUtils.getResourceViaProperty(
						"docx4j.openpackaging.parts.WordprocessingML.FontTablePart.DefaultFonts",
						"org/docx4j/openpackaging/parts/WordprocessingML/fontTable.xml");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    		
    	
    	return unmarshal( is );    	
    }

	public void processEmbeddings() {
		processEmbeddings(null);
	}
		
	private String filenamePrefix = null;

    public void processEmbeddings(Mapper fontMapper) {
    	
    	Fonts fonts = (org.docx4j.wml.Fonts)this.getJaxbElement();
    	
    	if (fonts==null) {
    		log.warn("No content in font table part");
    		return;
    	} else {
			filenamePrefix = "" + System.currentTimeMillis(); 
			log.info("Writing temp embedded fonts " + filenamePrefix);
    	}
    	
		for (Fonts.Font font : fonts.getFont() ) {
			String fontName =  font.getName();
    	
			FontRel embedRegular = font.getEmbedRegular();
			FontRel embedBold = font.getEmbedBold();
			FontRel embedBoldItalic = font.getEmbedBoldItalic();
			FontRel embedItalic = font.getEmbedItalic();

			PhysicalFont pfRegular = getFontFromRelationship(fontName, fontName, embedRegular);
			PhysicalFont pfBold = getFontFromRelationship(fontName, fontName + "-bold", embedBold);
			PhysicalFont pfItalic = getFontFromRelationship(fontName, fontName + "-italic", embedItalic);
			PhysicalFont pfBoldItalic = getFontFromRelationship(fontName, fontName + "-bold-italic", embedBoldItalic);
			if (fontMapper != null) { // && pfRegular != null) {
				fontMapper.registerRegularForm(fontName, pfRegular);
				fontMapper.registerBoldForm(fontName, pfBold);
				fontMapper.registerItalicForm(fontName, pfItalic);
				fontMapper.registerBoldItalicForm(fontName, pfBoldItalic);
			}

		}
    }
    
    private PhysicalFont getFontFromRelationship(String fontNameAsInFontTablePart, String fontFileName, FontRel fontRel) {
    
    	if (fontRel == null) {
    		//log.debug("fontRel not found for '" + fontName + "'");
    		return null;
    	}
    	
    	String id = fontRel.getId();    	
    	String fontKey = fontRel.getFontKey();
    	    	 
    	Part p = this.getRelationshipsPart().getPart(id);
    	
    	if (p instanceof ObfuscatedFontPart) {
    	
	    	ObfuscatedFontPart obfuscatedFont = (ObfuscatedFontPart)this.getRelationshipsPart().getPart(id);
	    	if (obfuscatedFont != null) {
	    		return obfuscatedFont.extract(fontNameAsInFontTablePart, fontFileName, fontKey, filenamePrefix);
	    	} else {
	    		log.error("Couldn't find ObfuscatedFontPart with id: " + id);
	    	}
	    	
    	} else {
	    	TrueTypeFontPart truetypeFont = (TrueTypeFontPart)this.getRelationshipsPart().getPart(id);
	    	if (truetypeFont != null) {
	    		return truetypeFont.extract(fontNameAsInFontTablePart, fontFileName, fontKey, filenamePrefix);
	    	} else {
	    		log.error("Couldn't find TrueTypeFontPart with id: " + id);
	    	}
    	}
		return null;
    }
    
    
    /**
     *  Temporary embedded fonts should be deleted on exit, but for a long running app
     *  that may not be adequate, in which case you'll want to invoke this method
     *  when you have finished with a WordML pkg.  You can get this part from your MainDocumentPart,
     *  using getFontTablePart()
     */
    public void deleteEmbeddedFontTempFiles() {
    	if (filenamePrefix!=null) {
    		log.debug("Deleting temp embedded fonts " + filenamePrefix);
    		ObfuscatedFontPart.deleteEmbeddedFontTempFiles(filenamePrefix);
    	}
    }

	public static void main(String[] args) throws Exception {
		String filepath = System.getProperty("user.dir") + "/sample-docs/word/FontEmbedded.docx";		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(filepath));
		
		wordMLPackage.getMainDocumentPart().getFontTablePart().processEmbeddings();
	}
    
    
}
