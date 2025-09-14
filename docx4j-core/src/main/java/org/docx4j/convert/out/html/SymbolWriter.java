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
 *  @author Jason Harrop
 *  
*/
public class SymbolWriter extends AbstractSymbolWriter {
	
	public SymbolWriter() {
		super();
	}

	private final static Logger log = LoggerFactory.getLogger(BrWriter.class);

	private final static int UNICODE_PRIV_USE_START = 0xF000;
	private final static int UNICODE_PRIV_USE_END = 0xFFFF;
	
	private final static boolean USE_UNICODE_SYMBOL_REPLACEMENTS = true; //TODO: make this configurable
	
	@Override
	public Node toNode(AbstractWmlConversionContext context, Object unmarshalledNode, 
			Node modelContent, TransformState state, Document doc)
			throws TransformerException {
	R.Sym modelData = (R.Sym)unmarshalledNode;
	String value =  modelData.getChar(); 

	byte[] valBytes = hexStringToByteArray(value);
	assert(valBytes.length <= 2); //this is a short according to the ECMA spec
	
	String fontName = modelData.getFont();
	
	String valStr;
	boolean haveUnicodeReplacement = false;
	
	// Pre-process according to ECMA-376 2.3.3.29
	// If bytes are between 0xF000 and 0xFFFF, subtract 0xF000	
	if (valBytes.length==2 && UNICODE_PRIV_USE_START <= short2Int(valBytes)
			&& UNICODE_PRIV_USE_END >= short2Int(valBytes) ) {
		
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
				valStr = SymbolMapper.getUnicodeReplacementChar(fontName, (short)short2Int(valBytes));
				
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
		int codePoint = short2Int(valBytes);
		valStr = Character.toString( codePoint );
	}
	
    Text theChar = doc.createTextNode( valStr );
    
	DocumentFragment docfrag = doc.createDocumentFragment();
		
	if (haveUnicodeReplacement) {
		
		Element span = doc.createElement("span");
	    docfrag.appendChild(span);
		
	    //TODO: add a font-family style with a font that will likely cover the unicode symbols
	    //span.setAttribute("style", "font-family: '" + ??? + "'" );
	    span.appendChild( theChar );		
		
	} else {
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
	
	protected static int short2Int(byte[] val) {
		
		assert(val.length<=2);
		
		if (val.length==1) {
			return (val[0] & 0xFF);
		} else {
			return (((val[0] & 0xFF) << 8) | ((val[1] & 0xFF) << 0));
		}
	}
	
	protected byte[] hexStringToByteArray(String s) {
		// From http://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
  
}
