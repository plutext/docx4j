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

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.writer.AbstractSymbolWriter;
import org.docx4j.fonts.PhysicalFont;
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
 *  @author Jason Harrop
 *  
*/
public class SymbolWriter extends AbstractSymbolWriter {
	
	public SymbolWriter() {
		super();
	}

	private final static Logger log = LoggerFactory.getLogger(BrWriter.class);


	@Override
	public Node toNode(AbstractWmlConversionContext context, Object unmarshalledNode, 
			Node modelContent, TransformState state, Document doc)
			throws TransformerException {
	R.Sym modelData = (R.Sym)unmarshalledNode;
	String value =  modelData.getChar(); 

	// Pre-process according to ECMA-376 2.3.3.29
	if (value.startsWith("F0")
			|| value.startsWith("f0") ) {
		value = value.substring(2);
	}
    
    Text theChar = doc.createTextNode( new String(hexStringToByteArray(value) ) );
    
	DocumentFragment docfrag = doc.createDocumentFragment();
	
	String fontName = modelData.getFont();
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
    
    return docfrag;
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
