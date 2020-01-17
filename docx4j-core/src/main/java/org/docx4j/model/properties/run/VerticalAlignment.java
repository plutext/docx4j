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
package org.docx4j.model.properties.run;

import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.CTVerticalAlignRun;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STVerticalAlignRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

/**
 * Support for subscript and superscript.
 * 
 * @author dev
 *
 */
public class VerticalAlignment extends AbstractRunProperty {
	
	protected static Logger log = LoggerFactory.getLogger(VerticalAlignment.class);		

	public final static String CSS_NAME = "vertical-align"; 
	public final static String FO_NAME  = "vertical-align"; 

	/**
	 * @since 2.7.2
	 */	
	public String getCssName() {
		return CSS_NAME;
	}

	public VerticalAlignment(CTVerticalAlignRun val) {
		this.setObject(val);
	}
	
	public VerticalAlignment(CSSValue value) {
		
		CTVerticalAlignRun vAlign = Context.getWmlObjectFactory().createCTVerticalAlignRun();
		
		if (value.getCssText().toLowerCase().equals("top")) {			
			vAlign.setVal(STVerticalAlignRun.SUPERSCRIPT);			
			this.setObject( vAlign );
		} else if (value.getCssText().toLowerCase().equals("super")) {			
			vAlign.setVal(STVerticalAlignRun.SUPERSCRIPT);			
			this.setObject( vAlign );
		} else if (value.getCssText().toLowerCase().equals("bottom")
		        || value.getCssText().toLowerCase().equals("sub")) {
		    vAlign.setVal(STVerticalAlignRun.SUBSCRIPT);			
		    this.setObject( vAlign );
		} else {
			log.warn("What to do with value: " + value.getCssText());
		}
	}

	@Override
	public String getCssProperty() {
		
		STVerticalAlignRun va = ((CTVerticalAlignRun)this.getObject()).getVal();
		
		if ( STVerticalAlignRun.SUBSCRIPT.equals(va) ) {
			return composeCss(CSS_NAME, "sub") + composeCss("font-size", "smaller");
			
			// Another approach is position:relative, and top: or bottom:,
			// but it seems better, all other things being equal, to use
			// properties with equivalent names
			
		} else if ( STVerticalAlignRun.SUPERSCRIPT.equals(va) ) {
			return composeCss(CSS_NAME, "super") + composeCss("font-size", "smaller");
		} else {
			// STVerticalAlignRun.BASELINE
			return null;
		}
	}


	@Override
	public void setXslFO(Element foElement) {
		
		STVerticalAlignRun va = ((CTVerticalAlignRun)this.getObject()).getVal();
		
		if ( STVerticalAlignRun.SUBSCRIPT.equals(va) ) {
			foElement.setAttribute(FO_NAME, "sub" );			
			// Some suggest @baseline-shift="sub" as an alternative			
		} else if ( STVerticalAlignRun.SUPERSCRIPT.equals(va) ) {
			foElement.setAttribute(FO_NAME, "super" );
		} else {
			// STVerticalAlignRun.BASELINE
		}
	}

	@Override
	public void set(RPr rPr) {
		rPr.setVertAlign( (CTVerticalAlignRun)this.getObject() );
	}

    @Override
    public void set(CTTextCharacterProperties rPr) {
        // TODO
    }
	
}
