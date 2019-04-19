/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
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

import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.writer.AbstractBrWriter;
import org.docx4j.wml.Br;
import org.docx4j.wml.STBrType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class BrWriter extends AbstractBrWriter {
	private static String XSL_FO = "http://www.w3.org/1999/XSL/Format";

	public BrWriter() {
		super();
	}
	
	@Override
	public Node toNode(AbstractWmlConversionContext context, Object unmarshalledNode, 
			Node modelContent, TransformState state, Document doc)
			throws TransformerException {
		
		Br modelData = (Br)unmarshalledNode;
		Element ret;
		
		if (modelData.getType()!=null 
				&& modelData.getType().equals(STBrType.PAGE)) {
		
			ret = doc.createElementNS(XSL_FO, "block");
			ret.setAttribute("break-before", "page");
		
		} else {
			
			/* w:br is converted to fo:block with @line-height="0pt", to get rid of
			 * unwanted vertical whitespace.  If effect, it results in a carriage return.
			 * 
			 * However, some users use contiguous w:br to create vertical spacing
			 * which works in Word.  But FOP only does a single carriage return
			 * for contiguous fo:block/@line-height="0pt"
			 * 
			 * So we need to work around this.  Possible approaches:
			 * 
			 * 1. pre-process to convert the w:br to a w:p.  This is difficult,
			 *    for example, numbering.
			 * 
			 * 2. set TransformState, so the attribute is not set for the 2nd w:br
			 * 
			 * 3. set some custom attribute on the br
			 * 
			 * 4. post-process the XSL FO, so in contiguous fo:block/@line-height="0pt"
			 *    the attributes on 2nd and subsequent are changed.
			 *    
			 *    eg in org/docx4j/convert/out/fo/renderers/AbstractFORenderer_POSTPROCESSING.xslt
			 *     
			 * It turns out the easiest approach is to catch the special case in the XSLT
			 * 
			 */
		
			ret = doc.createElementNS(XSL_FO, "block");  // yuck .. block/inline/block
			// see http://stackoverflow.com/a/3664468/1031689 answer
			// at http://stackoverflow.com/questions/3661483/inserting-a-line-break-in-a-pdf-generated-from-xsl-fo-using-xslvalue-of
			ret.setAttribute("linefeed-treatment", "preserve");
			ret.setAttribute("white-space-treatment", "preserve");
			//ret.setAttribute("space-after", "0in"); // doesn't help
			ret.setAttribute("line-height", "0pt"); // suits FOP 1.1
			ret.setTextContent("\n");
		}
		return ret;
	}

}
