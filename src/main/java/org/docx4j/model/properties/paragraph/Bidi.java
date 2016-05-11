/*
 *  Copyright 2014, Plutext Pty Ltd.
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
package org.docx4j.model.properties.paragraph;

import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.PPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

public class Bidi extends AbstractParagraphProperty {
	
	/* By experiment, I determined that it is:
	 * 
            <w:pPr>
              <w:bidi/>
            </w:pPr>
            
	 * which causes Word to right align arabic, not:
	 * 
              <w:rPr>
                <w:rtl/>
              </w:rPr>	
              
	 * which I guess makes sense (pPr affecting alignment).
	 * 
	 * See src\test\resources\multilingual\arabic\arabic_in_tc.docx
	 * 
	 * w:rtl swaps Word order; see TextDirection.
	 *                
	 */
	
	protected static Logger log = LoggerFactory.getLogger(Bidi.class);		
		
	
	
	public Bidi(BooleanDefaultTrue val) {
		this.setObject(val);
	}

	
	@Override
	public String getCssName() {
		return null;
	}
	
	private Bidi(CSSValue value) {	
		
		// Not implemented
	}
	


	@Override
	public String getCssProperty() {
		return null;
	}
	

	@Override
	public void setXslFO(Element foElement) {

		BooleanDefaultTrue bdt = (BooleanDefaultTrue)this.getObject();
		if (bdt.isVal()) {
			/* 
			 * There is an impedence mismatch between docx format, and XSL FO,
			 * since 
			 * 
			 * Per XSL-FO 1.1:

					"Property writing-mode applies to: fo:page-sequence, fo:simple-page-master, 
					fo:region-body, fo:region-before, fo:region-after, fo:region-start, fo:region-end, 
					fo:block-container, fo:inline-container, and fo:table."
					http://www.w3.org/TR/2006/REC-xsl11-20061205/#prapply
				
			   Per Glenn Adams (FOP user mailing list): none of these is fo:block. 
			   You need to specify it on fo:table.
			   
			   But it doesn't work on a nested table.
			   
			   So all we need to do is:				
			 */
			foElement.setAttribute(Justification.FO_NAME,  "right"); 
			// unless jc is set to align right; this interaction handled in XsltFOFunctions.createFoAttributes
			// and equivalent method in FOExporterVisitorGenerator
			
		} else {
			foElement.setAttribute(Justification.FO_NAME,  "left"); // eg English
		}
		

	}
	

	
	@Override
	public void set(PPr pPr) {
		pPr.setBidi( (BooleanDefaultTrue)this.getObject() );
	}


	
}
