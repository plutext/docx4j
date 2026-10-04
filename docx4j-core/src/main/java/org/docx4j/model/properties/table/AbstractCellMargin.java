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
package org.docx4j.model.properties.table;

import java.math.BigInteger;

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.TcPrInner;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

/*
 *  @author Alberto Zerolo
 *  @since 3.0.0
 */
public abstract class AbstractCellMargin extends AbstractTableProperty {
	
	protected static final String CSS_NAME_PREFIX = "padding-";  
	protected static final String FO_NAME_PREFIX = CSS_NAME_PREFIX;  

	protected String cssAttributeName = null; 
	protected String foAttributeName = null; 

	int MAGIC_NUMBER = 240;
	
	protected AbstractCellMargin(TblWidth val, String suffix) {
		init(suffix);
		this.setObject(val);
	}
	
	public AbstractCellMargin(CSSValue value, String suffix) {	
		CSSPrimitiveValue cssPrimitiveValue = (CSSPrimitiveValue)value;
		TblWidth tblWidth = Context.getWmlObjectFactory().createTblWidth();
		short ignored = 1;
		float fVal = cssPrimitiveValue.getFloatValue(ignored); // unit type ignored in cssparser

		int twip;
		
		short type = cssPrimitiveValue.getPrimitiveType();
		if (CSSPrimitiveValue.CSS_IN == type) {
			twip = UnitsOfMeasurement.inchToTwip(fVal);
		} else if (CSSPrimitiveValue.CSS_MM == type) {
			twip = UnitsOfMeasurement.mmToTwip(fVal);		
		} else if (CSSPrimitiveValue.CSS_PERCENTAGE == type) {
			twip = twipFromPercentage(fVal);		
		} else {
			log.error("No support for unit " + type);
			twip = 0;
		}
		init(suffix);
		tblWidth.setW(BigInteger.valueOf(twip));
		tblWidth.setType(TblWidth.TYPE_DXA);
		this.setObject(tblWidth);
	}
	
	protected int twipFromPercentage(float fVal) {		
		return Math.round(MAGIC_NUMBER*fVal/100);		
	}
	
	protected void init(String suffix) {
		cssAttributeName = CSS_NAME_PREFIX + suffix;
		foAttributeName = FO_NAME_PREFIX + suffix;
	}

	@Override
	public String getCssProperty() {
	int twips = getTwips();
	String ret = null;
		if (twips > -1) {
			ret = composeCss(cssAttributeName, formatTwips(twips));
		}
		return ret;
	}

	@Override
	public String getCssName() {
		return cssAttributeName;
	}

	@Override
	public void setXslFO(Element foElement) {
	int twips = getTwips();
		if (twips > -1) {
			foElement.setAttribute(foAttributeName, formatFoTwips(twips));
		}
	}

	protected int getTwips() {
	TblWidth width = (TblWidth)getObject();
	int ret = -1;
		if ((width != null) && (width.getW() != null)) {
			ret = width.getW().intValue();
		}
		return ret;
	}

	protected String formatTwips(int twips) {
		return UnitsOfMeasurement.format2DP.format(UnitsOfMeasurement.twipToMm(twips)) + "mm";
	}

	/**
	 * The margin as the FO exporter writes it: top and bottom in points on Word's
	 * 1/600-inch grid, left and right in millimetres to two decimals as before (and as
	 * the CSS still is).
	 *
	 * <p>Down the row Word lays a cell margin out as a whole number of 1/600 inch (0.12pt):
	 * 28 twips are 12 units, 1.44pt.  Measured on corpus document 11657 (2393 rows of one
	 * Calibri 11 line, 28-twip top and bottom margins, a 0.5pt border), Word's row is 140
	 * units, 16.80pt (13.44 + 2 x 1.44 + 0.48), where ours was 16.69 (0.49mm = 1.389pt),
	 * and by each page's foot we had fitted a row more than Word.  On the grid the document
	 * is at Word's 222 pages (+179 lines; 7147 +5; no probe moved).</p>
	 *
	 * <p>Across the row the grid is not Word's.  The default 108-twip margin is 5.414pt in
	 * millimetres and 5.40 on the grid, and that 0.014pt a side gained 11398 65 lines and
	 * 8814 11 but cost 6380 140 lines and a page, 9888 15, and the table-cell-pct probe its
	 * exact match.  Exact points, all four sides, were worse still (-74 lines over the
	 * corpora).  @since 17.3.1</p>
	 */
	protected String formatFoTwips(int twips) {
		if ("padding-top".equals(foAttributeName) || "padding-bottom".equals(foAttributeName)) {
			long units = Math.round(twips * 5 / 12.0);   // 1/600 inch = 2.4 twips
			return java.math.BigDecimal.valueOf(units * 12, 2).stripTrailingZeros().toPlainString() + "pt";
		}
		return formatTwips(twips);
	}

	protected void ensureMargin(TcPrInner tcPr) {
		if (tcPr.getTcMar() == null) {
			tcPr.setTcMar(Context.getWmlObjectFactory().createTcMar());
		}
	}

	public void ensureMargin(TblPr tblPr) {
		if (tblPr.getTblCellMar() == null) {
			tblPr.setTblCellMar(Context.getWmlObjectFactory().createCTTblCellMar());
		}
	}
	
	public abstract void set(TcPrInner tcPr);
	
}
