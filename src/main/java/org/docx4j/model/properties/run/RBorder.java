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

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

public class RBorder extends AbstractRunProperty {
	
	protected static Logger log = LoggerFactory.getLogger(RBorder.class);		
	
	public static final String CSS_NAME_STYLE = "border-style"; 
	public static final String CSS_NAME_WIDTH = "border-width"; 
	public static final String CSS_NAME_COLOR = "border-color";
	
	/**
	 * @since 2.7.2
	 */	
	public String getCssName() {
		return "border"; // NB 
	}
	
	// FO names are the same as the CSS ones.
	//public final static String FO_NAME  = "TODO"; 
	
	
	public RBorder(CTBorder val) {
		this.setObject(val);
	}
	
	public RBorder(CSSValue value) {	
		
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
				val = "none";
			} else if (stBorder==STBorder.SINGLE) {
				// can use == or .equals
				val = "solid";				
			} else if (stBorder == STBorder.DOUBLE
					|| stBorder == STBorder.DOTTED
					|| stBorder == STBorder.DASHED
					|| stBorder == STBorder.OUTSET
					|| stBorder == STBorder.INSET) {
				val = stBorder.value();				
			} else {
				// fallback
				log.warn("Falling back to solid");
				val = "solid";
			}
			val = composeCss(CSS_NAME_STYLE, val);
		} 

		String sz = "";
		if (border.getSz()!=null) {
			float mm = eighthsToMM(border.getSz().intValue()); // eights of a point
			if (mm<0.262) {
				// At 96dpi, that's 1 pixel. Anything less WebKit won't display
				sz = "1px";
			} else {
				sz = UnitsOfMeasurement.format2DP.format(mm) + "mm" ;
			}
			sz = composeCss(CSS_NAME_WIDTH, sz);
		} 
		
		String color = "";
		// IE8 needs color to be specified.  Other browsers don't care.
		if (border.getColor()!=null) {
			if (border.getColor().equals("auto")) {
				color = "#000000";
			} else {
				color = "#" + border.getColor();
			}
			color = composeCss(CSS_NAME_COLOR, color);
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
	String val = null;
		
		CTBorder border = (CTBorder)getObject();
		
		if (border==null) return;
		
		if (border.getVal()!=null) {
			STBorder stBorder = border.getVal();
			
			log.debug("border: " + stBorder);
			
			if (stBorder.equals(STBorder.NIL)
					|| stBorder == STBorder.NONE) {
				val = "none";
			} else if (stBorder==STBorder.SINGLE) {
				// can use == or .equals
				val = "solid";				
			} else if (stBorder == STBorder.DOUBLE
					|| stBorder == STBorder.DOTTED
					|| stBorder == STBorder.DASHED
					|| stBorder == STBorder.OUTSET
					|| stBorder == STBorder.INSET) {
				val = stBorder.value();				
			} else {
				// fallback
				log.warn("Falling back to solid");
				val = "solid";
			}
			foElement.setAttribute(CSS_NAME_STYLE, val);
		} 

		if (border.getSz()!=null) {
			 // eights of a point
			float mm = eighthsToMM(border.getSz().intValue()); // eights of a point
			val = UnitsOfMeasurement.format2DP.format(mm) + "mm";
			foElement.setAttribute(CSS_NAME_WIDTH, val);
		} 

		if (border.getColor()!=null) {
			if (border.getColor().equals("auto")) {
				val = "#000000";
			} else {
				val = "#" + border.getColor();
			}
			foElement.setAttribute(CSS_NAME_COLOR, val);
		}
		
	}

	@Override
	public void set(RPr rPr) {
		rPr.setBdr((CTBorder)getObject());
	}

    @Override
    public void set(CTTextCharacterProperties rPr) {
        //TODO
    }
	
}
