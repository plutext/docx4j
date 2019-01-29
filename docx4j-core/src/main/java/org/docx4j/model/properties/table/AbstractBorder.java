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
package org.docx4j.model.properties.table;

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.TcPrInner;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

public abstract class AbstractBorder extends AbstractTableProperty {
	
	public String CSS_NAME_BASE;  
	public String CSS_NAME__STYLE; 
	public String CSS_NAME__WIDTH; 
	public String CSS_NAME__COLOR;
	
	// FO names are the same as the CSS ones.
	//public final static String FO_NAME  = "TODO"; 
	
	
	public AbstractBorder(CTBorder val, String css_name) {
		init(css_name);
		this.setObject(val);
	}
	
	private void init(String css_name) {
		CSS_NAME_BASE = css_name;
		CSS_NAME__STYLE = CSS_NAME_BASE + "-style";
		CSS_NAME__WIDTH = CSS_NAME_BASE + "-width";
		CSS_NAME__COLOR = CSS_NAME_BASE + "-color";
	}
	
	public AbstractBorder(CSSValue value, String css_name) {	
		init(css_name);
		
		// TODO
//		if (value.getCssText().toLowerCase().equals("bold")) {
//			this.setObject( Context.getWmlObjectFactory().createBooleanDefaultTrue()  );
//		} else {
//			BooleanDefaultTrue bdt = Context.getWmlObjectFactory().createBooleanDefaultTrue();
//			bdt.setVal(Boolean.FALSE);
//		}
	}

	@Override
	public String getCssProperty() {
		
		// <w:left w:val="single" w:sz="4" 
		//         w:space="0" w:color="000000" w:themeColor="text1"/>
		CTBorder border = (CTBorder)getObject();
		
		if (border==null) return "";
		
		String val = "";
		if (border.getVal()!=null) {
			STBorder stBorder = border.getVal();
			
			log.debug("border: " + stBorder);
			
			if (stBorder.equals(STBorder.NIL)
					|| stBorder == STBorder.NONE) {
				val = composeCss(CSS_NAME__STYLE, "none");
			} else if (stBorder==STBorder.SINGLE) {
				// can use == or .equals
				val = composeCss(CSS_NAME__STYLE, "solid");				
			} else if (stBorder == STBorder.DOUBLE
					|| stBorder == STBorder.DOTTED
					|| stBorder == STBorder.DASHED
					|| stBorder == STBorder.OUTSET
					|| stBorder == STBorder.INSET) {
				val = composeCss(CSS_NAME__STYLE, stBorder.value() );				
			} else {
				// fallback
				log.warn("Falling back to solid");
				val = composeCss(CSS_NAME__STYLE, "solid");
			}
		} 

		String sz = "";
		if (border.getSz()!=null) {
			float mm = eighthsToMM(border.getSz().intValue()); // eights of a point
			if (mm<0.262) {
				// At 96dpi, that's 1 pixel. Anything less WebKit won't display
				sz = composeCss(CSS_NAME__WIDTH, "1px" );
			} else {
				sz = composeCss(CSS_NAME__WIDTH,
						UnitsOfMeasurement.format2DP.format(mm) + "mm" );
			}
		} 
		
		String color = "";
		// IE8 needs color to be specified.  Other browsers don't care.
		if (border.getColor()!=null) {
			if (border.getColor().equals("auto")) {
				color = composeCss(CSS_NAME__COLOR, "#000000"  );
			} else {
				color = composeCss(CSS_NAME__COLOR, "#" + border.getColor() );
			}
		}
		
		return val + sz + color; 
	}

	public float eighthsToMM(int eighths ) {		
		// 72 points per inch
		float inches = eighths/(8*72.00f);
		return inches/0.0394f;
	}
		

	@Override
	public void setXslFO(Element foElement) {
		
		CTBorder border = (CTBorder)getObject();
		
		if (border==null) return;
		
		if (border.getVal()!=null) {
			STBorder stBorder = border.getVal();
			
			log.debug("border: " + stBorder);
			
			if (stBorder.equals(STBorder.NIL)
					|| stBorder == STBorder.NONE) {
				foElement.setAttribute(CSS_NAME__STYLE, "none");
			} else if (stBorder==STBorder.SINGLE) {
				// can use == or .equals
				foElement.setAttribute(CSS_NAME__STYLE, "solid");				
			} else if (stBorder == STBorder.DOUBLE
					|| stBorder == STBorder.DOTTED
					|| stBorder == STBorder.DASHED
					|| stBorder == STBorder.OUTSET
					|| stBorder == STBorder.INSET) {
				foElement.setAttribute(CSS_NAME__STYLE, stBorder.value() );				
			} else {
				// fallback
				log.warn("Falling back to solid");
				foElement.setAttribute(CSS_NAME__STYLE, "solid");
			}
		} 

		if (border.getSz()!=null) {
			 // eights of a point
			float pt = border.getSz().intValue() / 8f; // eights of a point
			foElement.setAttribute(CSS_NAME__WIDTH, 
					UnitsOfMeasurement.format2DP.format(pt) + "pt" );
		} 

		if (border.getColor()!=null) {
			if (border.getColor().equals("auto")) {
				foElement.setAttribute(CSS_NAME__COLOR, "#000000"  );
			} else {
				foElement.setAttribute(CSS_NAME__COLOR, "#" + border.getColor() );
			}
		}
		
	}

	public abstract void set(TcPrInner tcPr);
	
}
