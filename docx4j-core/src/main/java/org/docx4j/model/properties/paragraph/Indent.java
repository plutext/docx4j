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
package org.docx4j.model.properties.paragraph;

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.Ind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

import java.math.BigInteger;

public class Indent extends AbstractParagraphProperty {
	
	protected static Logger log = LoggerFactory.getLogger(Indent.class);		
		
	public final static String CSS_NAME = "margin-left";  // Use 'margin-left' instead of 'left' for CSS.
	// 'Left' pushes the box to the right, which results can result in a horizontal scroll bar in the web browser.
	/**
	 * @since 2.7.2
	 */	
	public String getCssName() {
		return CSS_NAME;
	}
	
	public final static String FO_NAME  = "start-indent"; 
	public final static String FO_NAME_TEXT_INDENT  = "text-indent";
	
	
	public Indent(Ind val) {
		this.setObject(val);
	}

	/**
	 * Compose an effective indent value.
	 * 
	 * @param pPrDirectIndent
	 * @param numberingIndent
	 */
	public Indent(Ind pPrDirectIndent, Ind numberingIndent) {
		
		Ind val=Context.getWmlObjectFactory().createPPrBaseInd();
		if (pPrDirectIndent!=null) {
			StyleUtil.apply(pPrDirectIndent, val);
		}
		if (numberingIndent!=null) {
			// Use anything not specifically set already
			
			if (val.getHanging()==null 
					&& val.getFirstLine()==null // since these are mutually exclusive
					&& numberingIndent.getHanging()!=null) {
				val.setHanging(numberingIndent.getHanging());
			}
			if (val.getFirstLine()==null 
					&& numberingIndent.getFirstLine()!=null) {
				val.setFirstLine(numberingIndent.getFirstLine());
			}
			
			if (val.getLeft()==null 
					&& numberingIndent.getLeft()!=null) {
				val.setLeft(numberingIndent.getLeft());
			}
		}
		
		this.setObject(val);
		
	}
	
	public Indent(CSSValue value) {	
		
		debug(CSS_NAME, value);
		
		Ind ind = Context.getWmlObjectFactory().createPPrBaseInd();
		
		CSSPrimitiveValue cssPrimitiveValue = (CSSPrimitiveValue)value;	
		
		int twip = getTwip(cssPrimitiveValue);
		ind.setLeft(BigInteger.valueOf(twip) );		
		this.setObject(ind);
	}
	
	public static int getTwip(CSSPrimitiveValue cssPrimitiveValue) {

		short ignored = 1;
		float fVal = cssPrimitiveValue.getFloatValue(ignored); // unit type ignored in cssparser
		log.debug("margin-left: " + fVal);
		if (fVal==0f) {
			return 0;
		}
		
		int twip;
		
		short type = cssPrimitiveValue.getPrimitiveType();		
		
		if (CSSPrimitiveValue.CSS_PX == type) {
			// Unit 5
			twip = UnitsOfMeasurement.pxToTwip(fVal);
		} else if (CSSPrimitiveValue.CSS_CM == type) { 
			// Unit 6
			twip = UnitsOfMeasurement.mmToTwip(fVal*10);		
		} else if (CSSPrimitiveValue.CSS_MM == type) {
			// Unit 7
			twip = UnitsOfMeasurement.mmToTwip(fVal);		
		} else if (CSSPrimitiveValue.CSS_IN == type) {
			// Unit 8
			twip = UnitsOfMeasurement.inchToTwip(fVal);
		} else if (CSSPrimitiveValue.CSS_PT == type) {
			// Unit 9
			twip = UnitsOfMeasurement.pointToTwip(fVal);
		} else if (CSSPrimitiveValue.CSS_EMS == type) {
			// TODO: Don't hardcode 1em == 16px, but make it depend on the paragraph font.
			twip = UnitsOfMeasurement.pxToTwip(16.0f * fVal);
		} else if (CSSPrimitiveValue.CSS_EXS == type) {
			// TODO: Don't hardcode 1ex == 8px, but make it depend on the paragraph font.
			twip = UnitsOfMeasurement.pxToTwip(8.0f * fVal);
		} else if (CSSPrimitiveValue.CSS_NUMBER == type) {
			log.error("Indent: No support for unspecified unit: CSS_NUMBER "); 
			// http://stackoverflow.com/questions/11479985/what-is-the-default-unit-for-margin-left
			/*
			 * In quirks mode (without a doctype), most browsers will try to correct the code by 
			 * using the unit px. In standards compliance mode (with a proper doctype), most browsers 
			 * will ignore the style.			
			 **/
			twip = 0; // TODO: should throw UnsupportedUnitException?
		} else {
			log.error("Indent: No support for unit " + type);
			twip = 0;
		}
		
		return twip;
	}

	@Override
	public String getCssProperty() {
		
		// Note regarding numbering case; handling of tab after the number:-
		// We get this right in the PDF case, via setXslFOListBlock below.
		// We don't attempt to get the tab right in the HTML case,
		// since without some research, I don't know what markup would be required.
		
		String prop = "position: relative; ";
		
		BigInteger left = ((Ind)this.getObject()).getLeft();		
		if (left!=null) {
			prop  = prop  + composeCss(CSS_NAME, UnitsOfMeasurement.twipToBest(left.intValue()) );
		} 

		// SPEC: The firstLine and hanging attributes are mutually exclusive, if both are specified, then
		// the firstLine value is ignored.		
		BigInteger firstline = ((Ind)this.getObject()).getFirstLine();		
		BigInteger hanging = ((Ind)this.getObject()).getHanging();		
		if (hanging!=null) {
			prop  = prop  + composeCss("text-indent", "-" + UnitsOfMeasurement.twipToBest(hanging.intValue()) );
		} 
		else if (firstline!=null) {
			prop  = prop  + composeCss("text-indent", UnitsOfMeasurement.twipToBest(firstline.intValue()) );
		} 

		
		if (left==null && firstline == null && hanging==null){
            if(log.isDebugEnabled()) {
                log.debug("What to do with " + XmlUtils.marshaltoString(this.getObject(), true, true));
            }
			prop =  CSS_NULL;
		} 
		return prop;
	}
	

	@Override
	public void setXslFO(Element foElement) {

		// <w:ind w:left="360" w:hanging="360"/>
		
		boolean updated = false;
		BigInteger left = ((Ind)this.getObject()).getLeft();
		if (left !=null) {
			foElement.setAttribute(FO_NAME, UnitsOfMeasurement.twipToBest(left.intValue()) );
			updated = true;
		}
		BigInteger firstLine = ((Ind)this.getObject()).getFirstLine();
		BigInteger hanging = ((Ind)this.getObject()).getHanging();
		// SPEC: The firstLine and hanging attributes are mutually exclusive, if both are specified, then
		// the firstLine value is ignored.
		if (hanging != null) {
			foElement.setAttribute(FO_NAME_TEXT_INDENT, UnitsOfMeasurement.twipToBest(-hanging.intValue()) );
		} else if (firstLine != null) {
			foElement.setAttribute(FO_NAME_TEXT_INDENT, UnitsOfMeasurement.twipToBest(firstLine.intValue()) );
			updated = true;
		}
		
		if (!updated) {
			log.warn("Only left/first-line indentation is handled at present");
		}

	}
	
	public boolean isHanging() {
		
		return ((Ind)this.getObject()).getHanging()!=null;
	}
	
	public int getNumberPosition() {

		boolean updated = false;
		BigInteger left = ((Ind)this.getObject()).getLeft();
		int leftInt = 0;
		if (left!=null) {
			leftInt = left.intValue();
		}
		
		BigInteger firstLine = ((Ind)this.getObject()).getFirstLine();
		BigInteger hanging = ((Ind)this.getObject()).getHanging();
		
		// SPEC: The firstLine and hanging attributes are mutually exclusive, if both are specified, then
		// the firstLine value is ignored.
		if (hanging != null) {
			// <w:ind w:left="360" w:hanging="360"/>
			
			int hangingInt = hanging.intValue();
			
			return ( leftInt-hangingInt) ;

		} else { 
			
			int firstLineInt = 0;
			if (firstLine != null) {
				firstLineInt = firstLine.intValue();
			}
			return (leftInt + firstLineInt );
		} 
	}
	

	public void setXslFOListBlock(Element foElement, int pdbs) {
		// NB the calculations in this method are correct and tested
		// (at least for positive left values).
		// So if there are problems with indentation, they are more 
		// likely to be somewhere else, eg in how the Ind value
		// is being derived.

		// The pdbs value only affects the firstLine case.
		// It is not used in the hanging case.
				
		boolean updated = false;
		BigInteger left = ((Ind)this.getObject()).getLeft();
		int leftInt = 0;
		if (left!=null) {
			leftInt = left.intValue();
		}
		
		BigInteger firstLine = ((Ind)this.getObject()).getFirstLine();
		BigInteger hanging = ((Ind)this.getObject()).getHanging();
		
		// SPEC: The firstLine and hanging attributes are mutually exclusive, if both are specified, then
		// the firstLine value is ignored.
		if (hanging != null) {
			
			int hangingInt = hanging.intValue();
			
			// start = left - hanging
			foElement.setAttribute(FO_NAME, UnitsOfMeasurement.twipToBest( leftInt-hangingInt) );
			
			// pdbs = hanging
			foElement.setAttribute("provisional-distance-between-starts",  UnitsOfMeasurement.twipToBest(hangingInt));

			// text is always 0
			foElement.setAttribute(FO_NAME_TEXT_INDENT, UnitsOfMeasurement.twipToBest(0) );

		} else { 			
			
			int firstLineInt = 0;
			if (firstLine != null) {
				firstLineInt = firstLine.intValue();
			}
			
			foElement.setAttribute("provisional-distance-between-starts",  UnitsOfMeasurement.twipToBest(pdbs));
			System.out.println("Using pdbs " + pdbs + "=" + UnitsOfMeasurement.twipToBest(pdbs));
			
			// start = left - pdbs
			foElement.setAttribute(FO_NAME, UnitsOfMeasurement.twipToBest( leftInt-pdbs) );	
			
			// text 
			foElement.setAttribute(FO_NAME_TEXT_INDENT, UnitsOfMeasurement.twipToBest( firstLineInt + pdbs ) );
						
		} 
		
		updated = true;
			
    
		if (!updated) {
			log.warn("Only left/first-line indentation is handled at present");
		}

	}
	
	@Override
	public void set(PPr pPr) {
		pPr.setInd( (Ind)this.getObject() );
	}
	
}
