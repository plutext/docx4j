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
		boolean superscript = STVerticalAlignRun.SUPERSCRIPT.equals(va);
		if (!superscript && !STVerticalAlignRun.SUBSCRIPT.equals(va)) {
			return; // STVerticalAlignRun.BASELINE
		}

		// Word draws a superscript or subscript at 65% of the run's size, raised by
		// about a third of that size (superscript) or lowered by about a sixth
		// (subscript); measured against Word 365 output (CR-001 Phase 4).  The run's
		// font-size is already on the element (FontSize precedes this property in
		// PropertyFactory), so both can be given in points; a line's height is not
		// affected, as in Word, because the FO root disregards baseline shifts
		// (WordLayoutFixups).
		double sizePt = sizePt(foElement.getAttribute("font-size"));
		if (sizePt > 0) {
			foElement.setAttribute("font-size", format(sizePt * SIZE_FACTOR) + "pt");
			foElement.setAttribute("baseline-shift",
					format(sizePt * (superscript ? SUPERSCRIPT_RAISE : -SUBSCRIPT_DROP)) + "pt");
		} else {
			foElement.setAttribute("font-size", Math.round(SIZE_FACTOR * 100) + "%");
			foElement.setAttribute("baseline-shift", superscript ? "super" : "sub");
		}
	}

	/** Size of a superscript or subscript relative to the run's size. @since 17.0.5 */
	public static final double SIZE_FACTOR = 0.65;
	/** Raise of a superscript relative to the run's (unreduced) size. @since 17.0.5 */
	public static final double SUPERSCRIPT_RAISE = 0.36;
	/** Drop of a subscript relative to the run's (unreduced) size. @since 17.0.5 */
	public static final double SUBSCRIPT_DROP = 0.16;

	private static double sizePt(String fontSize) {
		if (fontSize == null || !fontSize.endsWith("pt")) return 0;
		try {
			return Double.parseDouble(fontSize.substring(0, fontSize.length() - 2).trim());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private static String format(double pt) {
		String s = String.format(java.util.Locale.ROOT, "%.2f", pt);
		if (s.endsWith("0")) s = s.substring(0, s.length() - 1);
		if (s.endsWith("0")) s = s.substring(0, s.length() - 1);
		if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
		return s;
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
