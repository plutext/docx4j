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
import org.docx4j.jaxb.Context;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TcPrInner;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

public abstract class AbstractBorder extends AbstractTableProperty {
	
	public String CSS_NAME_BASE;  
	public String CSS_NAME__STYLE; 
	public String CSS_NAME__WIDTH; 
	
	public final static String FO_NAME  = "TODO"; 
	
	
	public AbstractBorder(CTBorder val, String css_name) {
		init(css_name);
		this.setObject(val);
	}
	
	private void init(String css_name) {
		CSS_NAME_BASE = css_name;
		CSS_NAME__STYLE = CSS_NAME_BASE + "-style";
		CSS_NAME__WIDTH = CSS_NAME_BASE + "-width";
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
		
		String val = "";
		if (border.getVal()!=null) {
			STBorder stBorder = border.getVal();
			
			System.out.println(stBorder);
			
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
			 // eights of a point
			sz = composeCss(CSS_NAME__WIDTH, 
					UnitsOfMeasurement.eighthsToMM(
							border.getSz().intValue() ) );
		} 
		
		return val + sz; 
	}


	@Override
	public void setXslFO(Element foElement) {
		
		CTBorder border = (CTBorder)getObject();
		
		if (border.getVal()!=null) {
			STBorder stBorder = border.getVal();
			
			System.out.println(stBorder);
			
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
			foElement.setAttribute(CSS_NAME__WIDTH, 
					UnitsOfMeasurement.eighthsToMM(
							border.getSz().intValue() ) );
		} 

		
	}

	public abstract void set(TcPrInner tcPr);
	
}
