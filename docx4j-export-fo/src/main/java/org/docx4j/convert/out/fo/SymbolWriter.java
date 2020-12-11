/*
 *  Copyright 2009-2010, Plutext Pty Ltd.
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

package org.docx4j.convert.out.fo;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.writer.AbstractSymbolWriter;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.fop.fonts.Typeface;
import org.docx4j.wml.R;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/*
 * Convert the character reference to a string, 
 * since XSLT doesn't like us putting &#x and @w:char and ';' together
 * 
 *  @author Jason Harrop, alberto
 *  
*/
public class SymbolWriter extends AbstractSymbolWriter {
	private final static Logger log = LoggerFactory.getLogger(SymbolWriter.class);
	
	public SymbolWriter() {
		super();
	}

	/* 
		Some TTF Symbol Fonts (probably all :-D ) have their glyphs in the 
		private area. org.apache.fop.fonts.truetype.TTFFile contains a 
		workaround where it maps those glyphs on the lower ascii:
		
		if (encodingID == 0 && j >= 0xF020 && j <= 0xF0FF) {
		//Experimental: Mapping 0xF020-0xF0FF to 0x0020-0x00FF
		//Tested with Wingdings and Symbol TTF fonts which map their
		//glyphs in the region 0xF020-0xF0FF.
		int mapped = j - 0xF000;
		
		}
		
		The problem is that this workaround is only used if
		
		if (cmapRangeOffsets[i] != 0 && j != 65535) {
		
		but not if the rangeOffsets are zero. Several fonts (Wingdings2 v1.55, 
		Wingdings3 v1.55, etc) do have a rangeOffset of zero and the 
		workaround isn't applied (in the example its Wingdings3).
				
		So here we check if there is a glyph in the lower range, and if there 
		isn't it will output a glyph in the private area. 
		
		What I don't like about this solution, is that I haven't found a way to 
		output an explicit character reference in DOM (the ampersand
		will get expanded) and if I output the character itself (like it is now), 
		it works but the fo-file doesn't look nice.
		
		We have a patch for org.apache.fop.fonts.truetype.TTFFile 
		which could be applied instead, but first, see
		https://issues.apache.org/bugzilla/show_bug.cgi?id=50492 
		
		*/
	

	@Override
	public Node toNode(AbstractWmlConversionContext context, Object unmarshalledNode, 
			Node modelContent, TransformState state, Document doc)
			throws TransformerException {
		R.Sym modelData = (R.Sym)unmarshalledNode;
		String fontName = modelData.getFont();
		String textValue =  modelData.getChar();
		PhysicalFont pf = context.getWmlPackage().getFontMapper().get(fontName);
		char chValue = '\0';
		Typeface typeface = null;
	  
	  	if (pf != null) {
	  		typeface = pf.getTypeface();
	  		
	  	  	if (typeface != null) {
		  	  	if (textValue.length() > 1) {
		  	  		try {
		  	  			chValue = (char)Integer.parseInt(textValue, 16);
		  	  		}
		  	  		catch (NumberFormatException nfe) {
		  	  			chValue = '\0';
		  	  		}
		  	  	}
		  	  	else {
		  	  		chValue = textValue.charAt(0);
		  	  	}
		  	  	
		  	  	if (chValue != '\0') {
		  	  		if (chValue > 0xf000) { //let's check first the character in the lower ascii (Pre-process according to ECMA-376 2.3.3.29)
		  	  			chValue -= 0xf000;
		  	  		}
		  	  		if (typeface.mapChar(chValue) == 0) {
		  	  			chValue += 0xf000;
		  	  			if (typeface.mapChar(chValue) == 0) {
		  	  				chValue = '\0';
		  	  			}
		  	  		}
		  	  		if (chValue != '\0') {//character was found
		  	  			textValue = Character.toString(chValue);
		  	  		}
		  	  	}
	  	  	}
	  	}
	    
	    Text theChar = doc.createTextNode(textValue);
		DocumentFragment docfrag = doc.createDocumentFragment();
	
		if (pf==null) {
			log.warn("No physical font present for:" + fontName);		
		    docfrag.appendChild( theChar );
			
		} else {
			
		    Element foInline = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:inline");
		    docfrag.appendChild(foInline);
			
		    foInline.setAttribute("font-family", pf.getName() );
		    foInline.appendChild(theChar);
		}
	    
	    return docfrag;
	}
}
