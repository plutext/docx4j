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

import org.docx4j.jaxb.Context;
import org.docx4j.wml.STPageOrientation;
import org.docx4j.wml.SectPr.PgMar;
import org.docx4j.wml.SectPr.PgSz;

public class PageDimensions {
	
	// Defaults - if values aren't defined in sectPr 
	// TODO - defaults page size and margins in a .properties file	

	public static int DEFAULT_PAGE_WIDTH_TWIPS = 12240;  // Letter; A4 would be 11907  
	public static int DEFAULT_LEFT_MARGIN_TWIPS = 1440;  // 1 inch
	public static int DEFAULT_RIGHT_MARGIN_TWIPS = 1440;
	// TODO - defaults for the other fields
		
	int pageWidth = DEFAULT_PAGE_WIDTH_TWIPS;
	int pageHeight = 15840;  // Letter
	
	int marginTop = 1440;
	int marginBottom = 1440;
	int marginLeft = DEFAULT_LEFT_MARGIN_TWIPS;
	int marginRight = DEFAULT_RIGHT_MARGIN_TWIPS;

	int marginHeader = 708;
	int marginFooter = 708;
	int marginGutter = 0;		
	
//	@Deprecated
//	public void setA4Defaults() {
//		/* Mimic
//			<w:pgSz w:w="12240" w:h="15840"/>^M
//            <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" 
//            		 w:header="708" w:footer="708" w:gutter="0"/>
//		 */
//		pageWidth = DEFAULT_PAGE_WIDTH_TWIPS;
//		pageHeight = 15840;
//		
//		marginTop = 1440;
//		marginBottom = 1440;
//		marginLeft = DEFAULT_LEFT_MARGIN_TWIPS;
//		marginRight = DEFAULT_RIGHT_MARGIN_TWIPS;
//
//		marginHeader = 708;
//		marginFooter = 708;
//		marginGutter = 0;		
//	}
	
	/**
	 * @since 2.7
	 */
	public void setMargins(MarginsWellKnown m ) {
		
//	    NORMAL("normal"),     // <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="708" w:footer="708" w:gutter="0"/>
		if (m.equals(MarginsWellKnown.NORMAL)) {
			marginTop = 1440;
			marginBottom = 1440;
			marginLeft = 1440;
			marginRight = 1440;
			return;			
		}
		
//	    NARROW("narrow"),     // <w:pgMar w:top="720"  w:right="720"  w:bottom="720"  w:left="720" w:header="708" w:footer="708" w:gutter="0"/>
		if (m.equals(MarginsWellKnown.NARROW)) {
			marginTop = 720;
			marginBottom = 720;
			marginLeft = 720;
			marginRight = 720;
			return;			
		}
		
//	    MODERATE("moderate"), // <w:pgMar w:top="1440" w:right="1080" w:bottom="1440" w:left="1080" w:header="708" w:footer="708" w:gutter="0"/>
		if (m.equals(MarginsWellKnown.MODERATE)) {
			marginTop = 1440;
			marginBottom = 1440;
			marginLeft = 1080;
			marginRight = 1080;
			return;			
		}
		
//	    WIDE("wide");         // <w:pgMar w:top="1440" w:right="2880" w:bottom="1440" w:left="2880" w:header="708" w:footer="708" w:gutter="0"/>
		if (m.equals(MarginsWellKnown.WIDE)) {
			marginTop = 1440;
			marginBottom = 1440;
			marginLeft = 2880;
			marginRight = 2880;
			return;			
		}
		
	}
	
	public void setPageSize(PgSz pgSz ) {
		
		if (pgSz!=null) {
			if (pgSz.getW()!=null) {
				pageWidth = pgSz.getW().intValue();
			}
			if (pgSz.getH()!=null) {
				pageHeight = pgSz.getH().intValue();
			}
		}		
	}
	
	/**
	 * @since 2.7
	 */
	public static PgSz createPgSize(PageSizePaper sz, boolean landscape ) {
		
		PgSz pgSz = Context.getWmlObjectFactory().createSectPrPgSz();
		
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
		return pgSz;
	}	
	
	public PgSz createPgSize() {
		
		PgSz pgSz = Context.getWmlObjectFactory().createSectPrPgSz();
		
		pgSz.setW( BigInteger.valueOf(pageWidth) );
		pgSz.setH( BigInteger.valueOf(pageHeight) );
		
		
		return pgSz;		
	}
	
	public void setMargins(PgMar pgMar) {
				
		if (pgMar!=null) {
			if (pgMar.getTop()!=null) {
				marginTop = pgMar.getTop().intValue();
			}
		
			if (pgMar.getBottom()!=null) {
				marginBottom = pgMar.getBottom().intValue();
			}
			if (pgMar.getLeft()!=null) {
				marginLeft = pgMar.getLeft().intValue();
			}
			if (pgMar.getRight()!=null) {
				marginRight = pgMar.getRight().intValue();
			}
			
			if (pgMar.getHeader()!=null) {
				marginHeader = pgMar.getHeader().intValue();
			}
			if (pgMar.getFooter()!=null) {
				marginFooter = pgMar.getFooter().intValue();
			}
			if (pgMar.getGutter()!=null) {
				marginGutter = pgMar.getGutter().intValue();
			}
		}		
	}

	public PgMar createPgMar() {
		
		PgMar pgMar = Context.getWmlObjectFactory().createSectPrPgMar();
		
		pgMar.setTop(    BigInteger.valueOf(marginTop) );
		pgMar.setBottom( BigInteger.valueOf(marginBottom) );
		pgMar.setLeft(   BigInteger.valueOf(marginLeft) );
		pgMar.setRight(  BigInteger.valueOf(marginRight) );

		pgMar.setHeader( BigInteger.valueOf(marginHeader) );
		pgMar.setFooter( BigInteger.valueOf(marginFooter) );
		pgMar.setGutter( BigInteger.valueOf(marginGutter) );
		
		return pgMar;		
	}
	
	
	/**
	 * @return the pageWidth
	 */
	public int getPageWidth() {
		return pageWidth;
	}

	/**
	 * @param pageWidth the pageWidth to set
	 */
	public void setPageWidth(int pageWidth) {
		this.pageWidth = pageWidth;
	}
	
	public int getWritableWidthTwips() {
		return pageWidth - (marginLeft + marginRight);		
	}

	/**
	 * @return the pageHeight
	 */
	public int getPageHeight() {
		return pageHeight;
	}

	/**
	 * @param pageHeight the pageHeight to set
	 */
	public void setPageHeight(int pageHeight) {
		this.pageHeight = pageHeight;
	}

	/**
	 * @return the marginTop
	 */
	public int getMarginTop() {
		return marginTop;
	}

	/**
	 * @param marginTop the marginTop to set
	 */
	public void setMarginTop(int marginTop) {
		this.marginTop = marginTop;
	}

	/**
	 * @return the marginBottom
	 */
	public int getMarginBottom() {
		return marginBottom;
	}

	/**
	 * @param marginBottom the marginBottom to set
	 */
	public void setMarginBottom(int marginBottom) {
		this.marginBottom = marginBottom;
	}

	/**
	 * @return the marginLeft
	 */
	public int getMarginLeft() {
		return marginLeft;
	}

	/**
	 * @param marginLeft the marginLeft to set
	 */
	public void setMarginLeft(int marginLeft) {
		this.marginLeft = marginLeft;
	}

	/**
	 * @return the marginRight
	 */
	public int getMarginRight() {
		return marginRight;
	}

	/**
	 * @param marginRight the marginRight to set
	 */
	public void setMarginRight(int marginRight) {
		this.marginRight = marginRight;
	}

	/**
	 * @return the marginHeader
	 */
	public int getMarginHeader() {
		return marginHeader;
	}

	/**
	 * @param marginHeader the marginHeader to set
	 */
	public void setMarginHeader(int marginHeader) {
		this.marginHeader = marginHeader;
	}

	/**
	 * @return the marginFooter
	 */
	public int getMarginFooter() {
		return marginFooter;
	}

	/**
	 * @param marginFooter the marginFooter to set
	 */
	public void setMarginFooter(int marginFooter) {
		this.marginFooter = marginFooter;
	}

	/**
	 * @return the marginGutter
	 */
	public int getMarginGutter() {
		return marginGutter;
	}

	/**
	 * @param marginGutter the marginGutter to set
	 */
	public void setMarginGutter(int marginGutter) {
		this.marginGutter = marginGutter;
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
	 * set as required (in TWIPS).  In due course (when we have an algorirthm for amount of 
	 * space occupied), it may be possible to do this automatically.   
	 * 
	 */
	
	private int headerExtent = 708;
	public int getHeaderExtent() {
		return headerExtent;
	}
	public void setHeaderExtent(int headerExtent) {
		this.headerExtent = headerExtent;
	}

	private int footerExtent = 1440; //708;
	public int getFooterExtent() {
		return footerExtent;
	}
	public void setFooterExtent(int footerExtent) {
		this.footerExtent = footerExtent;
	}
	
	
}
