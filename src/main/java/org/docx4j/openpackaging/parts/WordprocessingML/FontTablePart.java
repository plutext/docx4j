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
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.FontRel;
import org.docx4j.wml.Fonts;


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
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
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
				is = org.docx4j.utils.ResourceUtils.getResource("org/docx4j/openpackaging/parts/WordprocessingML/fontTable.xml");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    		
    	
    	return unmarshal( is );    	
    }
    
    public void processEmbeddings() {
    	
    	Fonts fonts = (org.docx4j.wml.Fonts)this.getJaxbElement();
		for (Fonts.Font font : fonts.getFont() ) {
			String fontName =  font.getName();
    	
			FontRel embedRegular = font.getEmbedRegular();
			FontRel embedBold = font.getEmbedBold();
			FontRel embedBoldItalic = font.getEmbedBoldItalic();
			FontRel embedItalic = font.getEmbedItalic();
			
			getObfuscatedFontFromRelationship(fontName, embedRegular);
			getObfuscatedFontFromRelationship(fontName, embedBold);
			getObfuscatedFontFromRelationship(fontName, embedBoldItalic);
			getObfuscatedFontFromRelationship(fontName, embedItalic);
    	
		}
    }
    
    private void getObfuscatedFontFromRelationship(String fontName, FontRel fontRel) {
    
    	if (fontRel == null) {
    		//log.debug("fontRel not found for '" + fontName + "'");
    		return;
    	}
    	
    	String id = fontRel.getId();    	
    	String fontKey = fontRel.getFontKey();
    	    	 
    	ObfuscatedFontPart obfuscatedFont = (ObfuscatedFontPart)this.getRelationshipsPart().getPart(id);
    	if (obfuscatedFont != null) {
    		obfuscatedFont.deObfuscate(fontName, fontKey);
    	} else {
    		log.error("Couldn't find ObfuscatedFontPart with id: " + id);
    	}
    }

	public static void main(String[] args) throws Exception {
		String filepath = System.getProperty("user.dir") + "/sample-docs/FontEmbedded.docx";		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(filepath));
		
		wordMLPackage.getMainDocumentPart().getFontTablePart().processEmbeddings();
	}
    
    
}
