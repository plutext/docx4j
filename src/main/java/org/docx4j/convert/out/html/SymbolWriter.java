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
 
package org.docx4j.convert.out.html;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.ModelConverter;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.model.Model;
import org.docx4j.model.SymbolModel;
import org.docx4j.model.TransformState;
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
public class SymbolWriter extends ModelConverter {
	
  private final static Logger log = Logger.getLogger(SymbolWriter.class);
  

  public Node toNode(Model symbolModel, TransformState state) throws TransformerException {
    SymbolModel sm = (SymbolModel)symbolModel;
    String value =  sm.getSym().getChar(); 

	// Pre-process according to ECMA-376 2.3.3.29
	if (value.startsWith("F0")
			|| value.startsWith("f0") ) {
		value = value.substring(2);
	}
    
    org.w3c.dom.Document doc = XmlUtils.neww3cDomDocument(); 
    
    Text theChar = doc.createTextNode( new String(hexStringToByteArray(value) ) );
    
	DocumentFragment docfrag = doc.createDocumentFragment();
	
	String fontName = sm.getSym().getFont();
	PhysicalFont pf = wordMLPackage.getFontMapper().getFontMappings().get(fontName);

	if (pf==null) {
		log.warn("No physical font present for:" + fontName);		
	    docfrag.appendChild( theChar );
		
	} else {
		
	    Element span = doc.createElement("span");
	    docfrag.appendChild(span);
		
	    span.setAttribute("style", "font-family:" + pf.getName() );
	    span.appendChild( theChar );
	}
    
    return docfrag;
  }
  
	public static byte[] hexStringToByteArray(String s) {
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
