/*
 *  Copyright 2009, Plutext Pty Ltd.
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
package org.docx4j.convert.out.html;

import java.nio.charset.StandardCharsets;

import javax.xml.transform.TransformerException;

import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.writer.AbstractSymbolWriter;
import org.docx4j.convert.out.common.writer.SymbolMapper;
import org.docx4j.convert.out.common.writer.SymbolUtils;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.wml.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/*
 * Convert the character reference to a string, 
 * since XSLT doesn't like us putting &#x and @w:char and ';' together
 * 
 * Note: this class handles R.Sym, but usually Word 
 * does not necessarily use that element for a symbol:
 * it may do so if you use Insert > Symbol, but not
 * if you add it some other way.  The other case is
 * handled in RunFontSelector.
 * 
 *  @author Jason Harrop
 *  
*/
public class SymbolWriter extends AbstractSymbolWriter {
	
	public SymbolWriter() {
		super();
	}

	private final static Logger log = LoggerFactory.getLogger(SymbolWriter.class);
	
	private final static boolean USE_UNICODE_SYMBOL_REPLACEMENTS = true; //TODO: make this configurable
	
	@Override
	public Node toNode(AbstractWmlConversionContext context, Object unmarshalledNode, 
			Node modelContent, TransformState state, Document doc)
			throws TransformerException {
				
		R.Sym modelData = (R.Sym)unmarshalledNode;
		String value =  modelData.getChar(); 
	
		byte[] valBytes = SymbolUtils.hexStringToByteArray(value);
		assert(valBytes.length <= 2); //this is a short according to the ECMA spec
		
		String fontName = modelData.getFont();
		
		String valStr;
		boolean haveUnicodeReplacement = false;
		
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
			if (nonZeroIdx!=-1) {
					
				if (USE_UNICODE_SYMBOL_REPLACEMENTS) {
						//check if we have a suitable unicode replacement character for the symbol
					valStr = SymbolMapper.getUnicodeReplacementChar(fontName, (short)SymbolUtils.short2Int(valBytes));
					
					if (valStr!=null) {
						haveUnicodeReplacement = true;
					}
				}
				if (!haveUnicodeReplacement) {
					valStr = new String(valBytes, nonZeroIdx, (valBytes.length-nonZeroIdx), StandardCharsets.ISO_8859_1); //TODO: check if this charset is correct
				}
			} else {
				valStr = ""; //valBytes only contains null characters
			}
			
		} else {
			int codePoint = SymbolUtils.short2Int(valBytes);
			valStr = Character.toString( codePoint );
		}
		
	    Text theChar = doc.createTextNode( valStr );
	    
		DocumentFragment docfrag = doc.createDocumentFragment();
			
		if (haveUnicodeReplacement) {
			
			Element span = doc.createElement("span");
		    docfrag.appendChild(span);
			
		    span.setAttribute("style", "font-family: " + SymbolUtils.HTML_FONT_FAMILY );
		    span.appendChild( theChar );		
			
		} else {
			
			if (log.isDebugEnabled()) {
				log.debug("No Unicode replacement for ? in font " + fontName);
			}
			
			PhysicalFont pf = context.getWmlPackage().getFontMapper().get(fontName);
	
			if (pf==null) {
				log.warn("No physical font present for:" + fontName);		
			    docfrag.appendChild( theChar );
				
			} else {
				
			    Element span = doc.createElement("span");
			    docfrag.appendChild(span);
				
			    span.setAttribute("style", "font-family: '" + pf.getName() + "'" );
			    span.appendChild( theChar );
			}
		}
	
	    
	    return docfrag;
	}
	  
}
