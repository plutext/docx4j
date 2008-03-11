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

package org.docx4j.openpackaging.parts.WordprocessingML;

//import java.io.IOException;

//import javax.xml.bind.JAXBElement;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
//import org.docx4j.jaxb.Context;

import org.docx4j.wml.Fonts;
import org.docx4j.wml.FontRel;
import org.docx4j.openpackaging.parts.WordprocessingML.ObfuscatedFontPart;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.LoadFromZipFile;
//import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

import org.docx4j.openpackaging.parts.JaxbXmlPart;


public final class FontTablePart extends JaxbXmlPart {
	
	private static Logger log = Logger.getLogger(FontTablePart.class);		
	
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
     * Unmarshal XML data from the specified InputStream and return the 
     * resulting content tree.  Validation event location information may
     * be incomplete when using this form of the unmarshal API.
     *
     * <p>
     * Implements <a href="#unmarshalGlobal">Unmarshal Global Root Element</a>.
     * 
     * @param is the InputStream to unmarshal XML data from
     * @return the newly created root object of the java content tree 
     *
     * @throws JAXBException 
     *     If any unexpected errors occur while unmarshalling
     */
    public Object unmarshal( java.io.InputStream is ) throws JAXBException {
    	
		try {
			
//			if (jc==null) {
//				setJAXBContext(Context.jc);				
//			}
		    		    
			Unmarshaller u = jc.createUnmarshaller();
			
			//u.setSchema(org.docx4j.jaxb.WmlSchema.schema);
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

			System.out.println("unmarshalling " + this.getClass().getName() + " \n\n" );									
						
			jaxbElement = u.unmarshal( is );
			
			
			System.out.println("\n\n" + this.getClass().getName() + " unmarshalled \n\n" );									

		} catch (Exception e ) {
			e.printStackTrace();
		}
    	
		return jaxbElement;
    	
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
			
			getObfuscatedFontFromRelationship(embedRegular);
    	
		}
    }
    
    private void getObfuscatedFontFromRelationship(FontRel fontRel) {
    
    	if (fontRel == null) {
    		log.info("fontRel not found.");
    		return;
    	}
    	
    	String id = fontRel.getId();    	
    	String fontKey = fontRel.getFontKey();
    	    	 
    	ObfuscatedFontPart obfuscatedFont = (ObfuscatedFontPart)this.getRelationshipsPart().getPart(id);
    	if (obfuscatedFont != null) {
    		obfuscatedFont.deObfuscate(fontKey);
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
