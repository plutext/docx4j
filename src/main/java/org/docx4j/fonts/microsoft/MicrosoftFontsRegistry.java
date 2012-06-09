/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.fonts.microsoft;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFonts;

public class MicrosoftFontsRegistry {

	protected static Logger log = Logger.getLogger(MicrosoftFontsRegistry.class);

	private static HashMap<String, MicrosoftFonts.Font> msFontsByName;
	public final static Map<String, MicrosoftFonts.Font> getMsFonts() {
		return msFontsByName;
	}		

//	private static HashMap<String, String> filenamesToMsFontNames;

	static {
		
		try {
			
			setupMicrosoftFontsRegistry();
			
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}
	
	
	/**
	 * Get Microsoft fonts
	 * docx4all - all platforms - to populate font dropdown list */	
	private final static void setupMicrosoftFontsRegistry() throws Exception {

		
		msFontsByName = new HashMap<String, MicrosoftFonts.Font>();
//		filenamesToMsFontNames = new HashMap<String, String>();

		JAXBContext msFontsContext = JAXBContext.newInstance("org.docx4j.fonts.microsoft");		
		Unmarshaller u = msFontsContext.createUnmarshaller();		
		u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

		log.info("unmarshalling fonts.microsoft" );									
		// Get the xml file
		java.io.InputStream is = null;
		// Works in Eclipse - note absence of leading '/'
		is = org.docx4j.utils.ResourceUtils.getResource("org/docx4j/fonts/microsoft/MicrosoftFonts.xml");
					
		org.docx4j.fonts.microsoft.MicrosoftFonts msFonts = (org.docx4j.fonts.microsoft.MicrosoftFonts)u.unmarshal( is );
		
		List<MicrosoftFonts.Font> msFontsList = msFonts.getFont();
		
		for (MicrosoftFonts.Font font : msFontsList ) {			
			msFontsByName.put( (font.getName()), font); // 20080318 - normalised
			//log.debug( "put msFontsFilenames: " + normalise(font.getName()) );
			
//			filenamesToMsFontNames.put( font.getFilename().toLowerCase() , font.getName());
////			log.debug( "put msFontsFilenames: " + font.getName() );
//			
//			if (font.getBold()!=null) {
//				filenamesToMsFontNames.put( font.getBold().getFilename().toLowerCase(), font.getName()+SEPARATOR+Mapper.BOLD);
////				log.debug( "put bold: " +  font.getName()+SEPARATOR+Substituter.BOLD );				
//			}
//			if (font.getItalic()!=null) {
//				filenamesToMsFontNames.put( font.getItalic().getFilename().toLowerCase(), font.getName()+SEPARATOR+Mapper.ITALIC);
////				log.debug( "put italic: " + font.getName()+SEPARATOR+Substituter.ITALIC );				
//			}
//			if (font.getBolditalic() !=null) {
//				filenamesToMsFontNames.put( font.getBolditalic().getFilename().toLowerCase(), font.getName()+SEPARATOR+Mapper.BOLD_ITALIC);
////				log.debug( "put bold italic: " + font.getName()+SEPARATOR+Substituter.BOLD_ITALIC );				
//			}
			
		}
		
	}
	
	
	
	
	
}





