/*
 *  Copyright 2009-2025, Plutext Pty Ltd.
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

import java.util.concurrent.ExecutionException;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.writer.AbstractSymbolWriter;
import org.docx4j.convert.out.common.writer.SymbolMapper;
import org.docx4j.convert.out.common.writer.SymbolUtils;
import org.docx4j.fonts.GlyphCheck;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
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
 *  @author Jason Harrop, alberto, tom07091
 *  
*/
public class SymbolWriter extends AbstractSymbolWriter {
	private final static Logger log = LoggerFactory.getLogger(SymbolWriter.class);
	
	private final static boolean USE_UNICODE_SYMBOL_REPLACEMENTS = true; 
	
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

		boolean haveUnicodeReplacement = false;
		
		// TODO: if Symbol, Wingdings, Webdings is actually present, use it?
		// If there is a PhysicalFont, and it is the identity mapping, 
		// ie Symbol, Wingdings, Webdings is actually present,
		// should we try to use it?
		// Maybe this should be fallback if Noto Sans Symbols 2 (Linux) or Segoe UI Symbol (Windows)
		// is not present?
				
		// TODO: are there other symbol fonts?  what to do?
		
		PhysicalFont pf;
		PhysicalFont pf2;
		if (fontName.equals("Symbol")) {
			pf =PhysicalFonts.getSymbolFont();
			pf2 = null;
		} else {
			pf = PhysicalFonts.getWDingsFont();
			pf2 = PhysicalFonts.getWDingsFont2();
		}
		
		if (pf!=null) {
			
			byte[] valBytes = SymbolUtils.hexStringToByteArray(textValue);
			assert(valBytes.length <= 2); //this is a short according to the ECMA spec
						
			// Pre-process according to ECMA-376 2.3.3.29
			// If bytes are between 0xF000 and 0xFFFF, subtract 0xF000	
			if (valBytes.length==2 && SymbolUtils.UNICODE_PRIV_USE_START <= SymbolUtils.short2Int(valBytes)
					&& SymbolUtils.UNICODE_PRIV_USE_END >= SymbolUtils.short2Int(valBytes) ) {
				
				valBytes[0] = (byte)(valBytes[0] - 0xF0);
				int nonZeroIdx = -1; 
				for (int i=0; i<valBytes.length; i++) {
					if (valBytes[i]!=0) {
						nonZeroIdx = i;
						break;
					}
				}
				
				int codePoint = SymbolUtils.short2Int(valBytes);
				
				if (nonZeroIdx!=-1) {
						
					if (USE_UNICODE_SYMBOL_REPLACEMENTS) {
							//check if we have a suitable unicode replacement character for the symbol
						textValue = SymbolMapper.getUnicodeReplacementChar(fontName, (short)codePoint);
						
						if (textValue!=null) {
							haveUnicodeReplacement = true;
							
							try {
								if (GlyphCheck.hasCodepoint(pf, textValue.codePointAt(0))) {
									// good, it is there
								} else if (pf2!=null && GlyphCheck.hasCodepoint(pf2, textValue.codePointAt(0))) {									
									pf =pf2; // use pf2
									log.debug("For " + fontName + " mapped to " + textValue.codePointAt(0) + ", using 2nd substitute font " + pf2.getName());
								} else {
									log.warn("Missing symbol " + fontName + " " + codePoint + " in substitute font " + pf.getName());
								}
							} catch (ExecutionException e) {}

						}
					}
					if (!haveUnicodeReplacement) {
						//valStr = new String(valBytes, nonZeroIdx, (valBytes.length-nonZeroIdx), StandardCharsets.ISO_8859_1); //TODO: check if this charset is correct
						textValue = SymbolUtils.MISSING_SYMBOL;
						if (log.isDebugEnabled()) {
							if (fontName.equals("Symbol")
									&& codePoint >=127 && codePoint <=160) {
								// Symbol font doesn't contain these code points,
								// so it shouldn't be being used in a real Word document 
								log.debug("Symbol does not contain " + codePoint + "; why is this in the docx?");
							} else {
								log.debug("Missing symbol " + fontName + " " + codePoint);
							}
						}
					}
				} else {
					textValue = ""; //valBytes only contains null characters
				}
				
			} else {
				// Doesn't happen
				int codePoint = SymbolUtils.short2Int(valBytes);
				textValue = Character.toString( codePoint );
			}
			
		} else {
				
			char chValue = '\0';
			Typeface typeface = null;
			pf = context.getWmlPackage().getFontMapper().get(fontName);
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
	  	}
	    
	    Text theChar = doc.createTextNode(textValue);
		DocumentFragment docfrag = doc.createDocumentFragment();
			
		if (haveUnicodeReplacement) {
						
		    Element foInline = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:inline");
		    docfrag.appendChild(foInline);
			
		    foInline.setAttribute("font-family", pf.getName() );
		    foInline.appendChild(theChar);
			
		} else {
	
			if (pf==null) {
				log.warn("No physical font present for:" + fontName);		
			    docfrag.appendChild( theChar );
				
			} else {
				
			    Element foInline = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:inline");
			    docfrag.appendChild(foInline);
				
			    foInline.setAttribute("font-family", pf.getName() );
			    foInline.appendChild(theChar);
			}
		}		
	    
	    return docfrag;
	}
}
