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

package org.docx4j.model.structure;

import java.math.BigInteger;

import org.docx4j.Docx4jProperties;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.CTColumns;
import org.docx4j.wml.STPageOrientation;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.SectPr.PgMar;
import org.docx4j.wml.SectPr.PgSz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps PgSz (Page size) and PgMar (margin settings).
 * - gives you an easy way to set these;
 * - performs a few useful calculations
 * 
 * Used in SectionWrapper, to store the dimensions
 * of a page for a section; can also be used to
 * store a set of page dimensions which aren't
 * associated with any section. 
 * 
 * @author jharrop
 *
 */
public class PageDimensions {

	protected static Logger log = LoggerFactory.getLogger(PageDimensions.class);


	public PageDimensions() {

		pgSz = Context.getWmlObjectFactory().createSectPrPgSz();
		setPgSize();

		pgMar = Context.getWmlObjectFactory().createSectPrPgMar();
		setMargins();
	}

	/**
	 * @since 2.7
	 */
	public PageDimensions(PgSz pgSz, PgMar pgMar) {
		init(pgSz, pgMar, null);
	}

	/**
	 * @since 2.7
	 */
	public PageDimensions(SectPr sectPr) {
		if (sectPr==null) {
			init(null, null, null);
		} else {
			init(sectPr.getPgSz(), sectPr.getPgMar(), sectPr.getCols());
		}
	}

	private void init(PgSz pgSz, PgMar pgMar, CTColumns cols) {

		if (pgSz == null) {
			log.info("No pgSz in this section; defaulting.");
			this.pgSz = Context.getWmlObjectFactory().createSectPrPgSz();
			setPgSize();
		} else {
			this.pgSz = pgSz;
		}

		if (pgMar ==null) {
			log.info("No pgMar in this section; defaulting.");
			this.pgMar = Context.getWmlObjectFactory().createSectPrPgMar();
			setMargins();
		} else {
			this.pgMar = pgMar;
		}

		if (cols == null) {
			log.info("No cols in this section; defaulting.");
			this.cols = Context.getWmlObjectFactory().createCTColumns();
		} else {
			this.cols = cols;
		}

	}

	private CTColumns cols;

	private PgSz pgSz;
	/**
	 * @since 2.7
	 */
	public PgSz getPgSz() {
		return pgSz;
	}
	/**
	 * @since 2.7
	 */
	public void setPgSz(PgSz pgSz) {
		this.pgSz = pgSz;
	}
	@Deprecated
	public PgSz createPgSize() {
		return pgSz;
	}

	private PgMar pgMar;
	/**
	 * @since 2.7
	 */
	public PgMar getPgMar() {
		return pgMar;
	}
	/**
	 * @since 2.7
	 */
	public void setPgMar(PgMar pgMar) {
		this.pgMar = pgMar;
	}
	@Deprecated
	public PgMar createPgMar() {
		return pgMar;
	}

	/**
	 * set Margins from docx4j.properties
	 *
	 * @since 2.8
	 */
	public void setMargins() {

		String margin= Docx4jProperties.getProperties().getProperty("docx4j.PageMargins", "NORMAL");

		setMargins(MarginsWellKnown.valueOf(margin));

//		if (margin.equals("normal")) {
//			setMargins(MarginsWellKnown.NORMAL);
//		} else if (margin.equals("narrow")) {
//			setMargins(MarginsWellKnown.NARROW);
//		} else if (margin.equals("moderate")) {
//			setMargins(MarginsWellKnown.MODERATE);
//		} else if (margin.equals("wide")) {
//			setMargins(MarginsWellKnown.WIDE);
//		} else {
//			log.warn("Unknown margin setting " + margin + "in docx4j.properties");
//			setMargins(MarginsWellKnown.NORMAL);			
//		}
	}

	/**
	 * @since 2.7
	 */
	public void setMargins(MarginsWellKnown m ) {

//	    NORMAL("normal"),     // <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="708" w:footer="708" w:gutter="0"/>
		if (m.equals(MarginsWellKnown.NORMAL)) {
			pgMar.setTop( BigInteger.valueOf(1440));
			pgMar.setBottom( BigInteger.valueOf(1440));
			pgMar.setLeft( BigInteger.valueOf(1440));
			pgMar.setRight( BigInteger.valueOf(1440));
			return;
		}

//	    NARROW("narrow"),     // <w:pgMar w:top="720"  w:right="720"  w:bottom="720"  w:left="720" w:header="708" w:footer="708" w:gutter="0"/>
		if (m.equals(MarginsWellKnown.NARROW)) {
			pgMar.setTop( BigInteger.valueOf(720));
			pgMar.setBottom( BigInteger.valueOf(720));
			pgMar.setLeft( BigInteger.valueOf(720));
			pgMar.setRight( BigInteger.valueOf(720));
			return;
		}

//	    MODERATE("moderate"), // <w:pgMar w:top="1440" w:right="1080" w:bottom="1440" w:left="1080" w:header="708" w:footer="708" w:gutter="0"/>
		if (m.equals(MarginsWellKnown.MODERATE)) {
			pgMar.setTop( BigInteger.valueOf(1440));
			pgMar.setBottom( BigInteger.valueOf(1440));
			pgMar.setLeft( BigInteger.valueOf(1080));
			pgMar.setRight( BigInteger.valueOf(1080));
			return;
		}

//	    WIDE("wide");         // <w:pgMar w:top="1440" w:right="2880" w:bottom="1440" w:left="2880" w:header="708" w:footer="708" w:gutter="0"/>
		if (m.equals(MarginsWellKnown.WIDE)) {
			pgMar.setTop( BigInteger.valueOf(1440));
			pgMar.setBottom( BigInteger.valueOf(1440));
			pgMar.setLeft( BigInteger.valueOf(2880));
			pgMar.setRight( BigInteger.valueOf(2880));
			return;
		}

	}

	/**
	 * set page size/orientation from docx4j.properties
	 * @since 2.8
	 */
	public void setPgSize() {

		String papersize= Docx4jProperties.getProperties().getProperty("docx4j.PageSize", "A4");
		log.debug("Using paper size: " + papersize);

		String landscapeString = Docx4jProperties.getProperties().getProperty("docx4j.PageOrientationLandscape", "false");
		boolean landscape= Boolean.parseBoolean(landscapeString);
		log.debug("Landscape orientation: " + landscape);

		setPgSize(PageSizePaper.valueOf(papersize), landscape);
	}
	/**
	 * @since 2.7
	 */
	public void setPgSize(PageSizePaper sz, boolean landscape ) {

		if (sz.equals(PageSizePaper.LETTER)) {
			pgSz.setCode(BigInteger.valueOf(1));
			if (landscape) {
				pgSz.setOrient(STPageOrientation.LANDSCAPE);
				pgSz.setW(BigInteger.valueOf(15840));
				pgSz.setH(BigInteger.valueOf(12240));
			} else {
				pgSz.setW(BigInteger.valueOf(12240));
				pgSz.setH(BigInteger.valueOf(15840));

			}
		}

		else if (sz.equals(PageSizePaper.LEGAL)) {
			pgSz.setCode(BigInteger.valueOf(5));
			if (landscape) {
				pgSz.setOrient(STPageOrientation.LANDSCAPE);
				pgSz.setW(BigInteger.valueOf(20160));
				pgSz.setH(BigInteger.valueOf(12240));
			} else {
				pgSz.setW(BigInteger.valueOf(12240));
				pgSz.setH(BigInteger.valueOf(20160));

			}
		}

		else if (sz.equals(PageSizePaper.A3)) {
			pgSz.setCode(BigInteger.valueOf(8));
			if (landscape) {
				pgSz.setOrient(STPageOrientation.LANDSCAPE);
				pgSz.setW(BigInteger.valueOf(23814));
				pgSz.setH(BigInteger.valueOf(16839));
			} else {
				pgSz.setW(BigInteger.valueOf(16839));
				pgSz.setH(BigInteger.valueOf(23814));

			}
		}

		else if (sz.equals(PageSizePaper.A4)) {
			pgSz.setCode(BigInteger.valueOf(9));
			if (landscape) {
				pgSz.setOrient(STPageOrientation.LANDSCAPE);
				pgSz.setW(BigInteger.valueOf(16839));
				pgSz.setH(BigInteger.valueOf(11907));
			} else {
				pgSz.setW(BigInteger.valueOf(11907));
				pgSz.setH(BigInteger.valueOf(16839));

			}
		}

		else if (sz.equals(PageSizePaper.A5)) {
			pgSz.setCode(BigInteger.valueOf(11));
			if (landscape) {
				pgSz.setOrient(STPageOrientation.LANDSCAPE);
				pgSz.setW(BigInteger.valueOf(11907));
				pgSz.setH(BigInteger.valueOf(8391));
			} else {
				pgSz.setW(BigInteger.valueOf(8391));
				pgSz.setH(BigInteger.valueOf(11907));

			}
		}

		else if (sz.equals(PageSizePaper.B4JIS)) {
			pgSz.setCode(BigInteger.valueOf(12));
			if (landscape) {
				pgSz.setOrient(STPageOrientation.LANDSCAPE);
				pgSz.setW(BigInteger.valueOf(20639));
				pgSz.setH(BigInteger.valueOf(14572));
			} else {
				pgSz.setW(BigInteger.valueOf(14572));
				pgSz.setH(BigInteger.valueOf(20639));

			}
		}
	}


	public int getWritableWidthTwips() {

		int gutter = 0;
		if (pgMar.getGutter()!=null) {
			gutter = pgMar.getGutter().intValue();
		}

		return pgSz.getW().intValue() - (gutter + pgMar.getLeft().intValue() + pgMar.getRight().intValue());
	}

	/**headers/footers into account!
	 * Doesn't take
	 *
	 * @since 3.0.1
	 */
	@Deprecated
	public int getWritableHeightTwips() {
		return pgSz.getH().intValue() - (pgMar.getTop().intValue() + pgMar.getBottom().intValue());
	}
	
	/* From http://msdn.microsoft.com/en-us/library/aa537167(office.11).aspx 
	 * 
	 * In Word, the size of header and footer areas can change dynamically. 
	 * The central region of the page increases or decreases when the contents of a side region changes. 
	 * The XSL-FO format has no means to express dynamically changing header and footer areas. 
	 * In the XSL-FO format, side regions must have fixed dimensions, regardless of their actual contents. 
	 * 
	 * That article goes on to say:
	 * 
	 *    Consequently, you must reserve the space for headers and footers manually by adjusting page margins.
	 *    
	 * (that is, in the Word document).  We don't require that.
	 * 
	 * Instead, this class allows headerExtent and footerExtent to be
	 * set as required (in TWIPS).  In due course (when we have an algorithm for amount of 
	 * space occupied), it may be possible to do this automatically.   
	 * 
	 * ------------------
	 * 
	 * pgMar.header  is the distance from the top edge of the paper to the top edge of the header
	 * 
	 * in XSL FO, extent is  the inline-progression-dimension of the region-start or region-end 
	 * or the block-progression-dimension of the region-before or region-after.
	 */

	/**
	 * Get the distance from the top edge of the paper to the top edge of the header
	 */
	public int getHeaderMargin() {
		if (pgMar.getHeader()==null
//				|| pgMar.getHeader().intValue() ==0 
				) {
			return 720; //default in Word 2010 is 1/2 inch
		} else {
			return pgMar.getHeader().intValue();
		}
	}

	/**
	 * Get the distance from the bottom edge of the paper to the bottom edge of the footer
	 */
	public int getFooterMargin() {
		if (pgMar.getFooter()==null
//				|| pgMar.getFooter().intValue() ==0 
				) {
			return 720; //default in Word 2010 is 1/2 inch
		} else {
			return pgMar.getFooter().intValue();
		}
	}

	public int getColsNum() {
		if (cols.getNum() != null) {
			return cols.getNum().intValue();
		} else {
			return 1;
		}
	}

	public int getColsSpacing() {
		if (cols.getSpace() != null) {
			return cols.getSpace().intValue();
		} else {
			return 720; //default in Word 2010 is 1/2 inch
		}
	}
}
