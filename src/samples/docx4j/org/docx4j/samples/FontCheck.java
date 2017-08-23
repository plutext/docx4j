/*
 *  Copyright 2007-2017, Plutext Pty Ltd.
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


import org.docx4j.fonts.GlyphCheck;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;


/**
 * Simple way to check whether a font exists, and if it does, 
 * whether it contains a particular glyph.
 * 
 * @author jharrop
 *
 */
public class FontCheck {
	
	public static void main(String[] args) throws Exception {


		PhysicalFonts.discoverPhysicalFonts();
		
		PhysicalFont physicalFont = PhysicalFonts.get("Times New Roman");
		
		if (physicalFont==null) {
			System.out.println("missing TNR!");
		} else {
			System.out.println(
					GlyphCheck.hasChar(physicalFont, 'ě'));
		}
		
	}
		

}
