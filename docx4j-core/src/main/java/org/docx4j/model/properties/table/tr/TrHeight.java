package org.docx4j.model.properties.table.tr;

import java.math.BigInteger;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTHeight;
import org.docx4j.wml.STHeightRule;
import org.docx4j.wml.TrPr;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class TrHeight extends AbstractTrProperty {
	
	public final static String CSS_NAME = "height"; // NB, not strictly what we want; since in HTML/CSS, you set it on td, not tr!
	public final static String FO_NAME  = "height"; 
	protected static final BigInteger DEFAULT_TWIPS_HEIGHT = BigInteger.valueOf(UnitsOfMeasurement.mmToTwip(5f));
	protected static final QName TRHEIGHT_NAME = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "trHeight");
	
	public TrHeight() {
		CTHeight defHeight = new CTHeight();
		defHeight.setVal(DEFAULT_TWIPS_HEIGHT);
		defHeight.setHRule(STHeightRule.AT_LEAST);
		setObject(defHeight);
	}

	public TrHeight(CTHeight val) {
		setObject(val);
	}
	
	/* Won't ever get a tr height property from CSS!
	 * 
	public TrHeight(CSSValue value) {	
		
		debug(CSS_NAME, value);
		
		CSSPrimitiveValue cssPrimitiveValue = (CSSPrimitiveValue)value;	
		short ignored = 1;
		float fVal = cssPrimitiveValue.getFloatValue(ignored); // unit type ignored in cssparser
		if (fVal==0f) {
			this.setObject(BigInteger.ZERO);
			return;
		}

		int twip;
		
		short type = cssPrimitiveValue.getPrimitiveType();
		if (CSSPrimitiveValue.CSS_IN == type) {
			twip = UnitsOfMeasurement.inchToTwip(fVal);
		} else if (CSSPrimitiveValue.CSS_MM == type) {
			twip = UnitsOfMeasurement.mmToTwip(fVal);	
		} else if (CSSPrimitiveValue.CSS_PT == type) {
			twip = UnitsOfMeasurement.pointToTwip(fVal);	
		} else if (CSSPrimitiveValue.CSS_EMS == type) {
			log.warn("No support for unit: CSS_EMS; instead of em, please use an absolute unit. ");
			// calculated based on the font size
			twip = 0;
		} else if (CSSPrimitiveValue.CSS_PX == type) {
			twip = UnitsOfMeasurement.pxToTwip(fVal);
		} else if (CSSPrimitiveValue.CSS_NUMBER == type) {
			log.error("No support for unit: CSS_NUMBER ");
			twip = 0;			
		} else {
			log.error("No support for unit " + type);
			twip = 0;
		}
		
		CTHeight defHeight = new CTHeight();
		defHeight.setVal(BigInteger.valueOf(twip));
		defHeight.setHRule(STHeightRule.AT_LEAST);
	}
	*/
	
	
	@Override
	public void set(TrPr trPr) {
		
		JAXBElement<CTHeight> jbHeight = null;
		CTHeight ctHeight = (CTHeight)getObject();
		if ((ctHeight != null) && (ctHeight.getVal() != null)) {
			jbHeight = new JAXBElement<CTHeight>(TRHEIGHT_NAME, CTHeight.class, (CTHeight)getObject());
			
			JAXBElement<?> existing = XmlUtils.getListItemByQName(trPr.getCnfStyleOrDivIdOrGridBefore(), jbHeight.getName() );
			if (existing==null) {
				
				// just add it
				trPr.getCnfStyleOrDivIdOrGridBefore().add(jbHeight);
				
			} else {
				
				// replace it
				trPr.getCnfStyleOrDivIdOrGridBefore().remove(existing);
				trPr.getCnfStyleOrDivIdOrGridBefore().add(jbHeight);
			}
			
		}
	}

	@Override
	public String getCssProperty() { // Won't work?  See above?
		
		String ret = null;
		CTHeight ctHeight = (CTHeight)getObject();
		if ((ctHeight != null) && (ctHeight.getVal() != null)) {
			ret = composeCss(getCssName(), UnitsOfMeasurement.twipToBest(ctHeight.getVal().intValue()));
		}
		return ret;
	}

	@Override
	public String getCssName() {
		return CSS_NAME;
	}

	@Override
	public void setXslFO(Element foElement) {
		
		CTHeight ctHeight = (CTHeight)getObject();
		if ((ctHeight != null) && (ctHeight.getVal() != null)) {
			foElement.setAttribute(FO_NAME, 
					UnitsOfMeasurement.twipToBest(ctHeight.getVal().intValue()));
		}
	}

}
