package org.docx4j.model.properties.table.tr;

import java.math.BigInteger;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.wml.CTHeight;
import org.docx4j.wml.STHeightRule;
import org.docx4j.wml.TrPr;
import org.w3c.dom.Element;

public class TrHeight extends AbstractTrProperty {
	
	public final static String CSS_NAME = "height";
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
	
	@Override
	public void set(TrPr trPr) {
	JAXBElement<CTHeight> jbHeight = null;
	CTHeight ctHeight = (CTHeight)getObject();
		if ((ctHeight != null) && (ctHeight.getVal() != null)) {
			jbHeight = new JAXBElement<CTHeight>(TRHEIGHT_NAME, CTHeight.class, (CTHeight)getObject());
			trPr.getCnfStyleOrDivIdOrGridBefore().add(jbHeight);
		}
	}

	@Override
	public String getCssProperty() {
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
