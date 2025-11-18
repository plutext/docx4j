/*
 *  Copyright 2025, Plutext Pty Ltd.
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

package org.docx4j.samples;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.docx4j.fonts.BestMatchingMapper;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;


/**
 * List the fonts used in this document,
 * and how they are handled by font mapper.
 * 
 * This can help you to discover the precise
 * name of a font.
 * 
 * @author jharrop
 *
 */
public class FontsUsed {
	
	public static void main(String[] args) throws Exception {

	
		// Docx file using fonts of interest
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/sample-docx.docx";
//		String inputfilepath = System.getProperty("user.dir") + "/Liberation Sans.docx";

		boolean listDiscoveredFonts = false;
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

		// Specify your font mapper
		// Note, you can turn on DEBUG level logging for org.docx4j.fonts.PhysicalFonts
		// to see the fonts discovered and their names
		Mapper fontMapper = new IdentityPlusMapper();  // Only for Windows, unless you have Microsoft's fonts installed
//		Mapper fontMapper = new BestMatchingMapper();  // Good for Linux (and OSX?)
		
		wordMLPackage.setFontMapper(fontMapper);
		
		// Example overrides
//		fontMapper.put("Cambria", PhysicalFonts.get("Liberation Serif"));
//		fontMapper.put("Calibri", PhysicalFonts.get("Carlito Regular"));		
		
		Set<String> fonts = documentPart.fontsInUse();
		
		System.out.println("Fonts used: ");
		for (String fontName : fonts ) {
			if (PhysicalFonts.get(fontName)!=null) {
				System.out.println(fontName + " - present ");
			} else {
				
				PhysicalFont targetFont = fontMapper.get(fontName);
				if (targetFont==null) {
					System.out.println(fontName + " <- map this ");									
				} else {
					if (PhysicalFonts.get(targetFont.getName())==null) {
						System.out.println(fontName + " <- map to " + targetFont.getName() + " but that font is not installed!");															
					} else {
						System.out.println(fontName + " mapped to " + targetFont.getName() );																					
					}
					
				}
			}
				
		}
		
		if (listDiscoveredFonts) {
			System.out.println("PhysicalFonts discovered: ");
			Set<String> keySet = PhysicalFonts.getPhysicalFonts().keySet();
			List<String> sortedKeys = new ArrayList<>(keySet);
			Collections.sort(sortedKeys);
			for(String key : sortedKeys ) {
				if (key.endsWith("bold") || key.endsWith("italic")) {
					// skip
				} else {
					System.out.println("\t"+key);
				}
			}
			
		}
				
	}
		

}
