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

package org.docx4j;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jharrop, azerolo
 *
 */
public class UnitsOfMeasurement {
	
	private final static Logger log = LoggerFactory.getLogger(UnitsOfMeasurement.class);
	
	public final static DecimalFormat format2DP;
	public final static int DPI;
	static {
		format2DP =  new DecimalFormat("##.##", 
							new DecimalFormatSymbols(Locale.ENGLISH)); 
		
		
		DPI = Integer.parseInt(Docx4jProperties.getProperty("docx4j.DPI", "96"));
		
	}
	
	public static long twipToEMU(double twips) {		
		return Math.round(635 * twips);				
	}	

	public static int inchToTwip(float inch  ) {
		// 1440 twip = 1 inch;
		return Math.round(inch*1440);		
	}

	public static float twipToInch(int twip) {
		return twip/1440.00f;		
	}

	/**
	 * @since 3.0.0
	 */
	public static float twipToMm(int twip) {
		return twip / 56.6928f;		
	}
		
	public static int mmToTwip(float mm  ) {		
		float inch = mm*0.0394f;
		return inchToTwip(inch);
	}

	/**
	 * @since 3.0.0
	 */
	public static float twipToPoint(int twip) {
		return twip / 20f;		
	}
		
	/**
	 * @since 3.0.0
	 */
	public static int pointToTwip(float point  ) {		
		return Math.round(20 * point);
	}

	/**
	 * @since 3.0.0
	 */
	public static int pxToTwip(float px) {
		
		float inch = px/DPI;
		return inchToTwip(inch);
		
	}

	/**
	 * Convert an inches value to twips and return as a double.
	 */
	public static double inchToTwipDouble(double inch) {
		// 1440 twip = 1 inch;
		return inch * 1440;
	}

	/**
	 * Convert a pixel value to twips and return as a double. This may be useful
	 * where the returned value will then be converted to another unit, and we
	 * don't want to lose precision due to rounding.
	 */
	public static double pxToTwipDouble(double px) {
		double inch = px / DPI;
		return inchToTwipDouble(inch);
	}

	/**
	 * 1440 twip = 1 inch;Try to guess whether inches or mm looks nicer
	 * @param left
	 * @return
	 */
	/**
	 * A twips value as a CSS/XSL-FO length, losslessly: whole inches as "Nin",
	 * otherwise points ("12pt", "35.4pt"; a twip is 0.05pt, so two decimals are exact).
	 *
	 * Before 17.0.5 this rounded to whole millimetres whenever the value was not a
	 * multiple of 1/80 inch, which lost up to 0.5mm (1.4pt) per value: a 708-twip
	 * header distance became "12mm" (12.49), 12pt paragraph spacing became "4mm"
	 * (11.34pt).  Measured against Word (CR-001 harness), those losses were a
	 * visible part of the page layout error.
	 */
	public static String twipToBest(int leftL ) {
		if (leftL % 1440 == 0) {
			return (leftL / 1440) + "in";
		}
		if (leftL % 20 == 0) {
			return (leftL / 20) + "pt";
		}
		String s = String.format(java.util.Locale.ROOT, "%.2f", leftL / 20f);
		s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
		return s + "pt";
	}
	
	public static String rgbTripleToHex(float red, float green, float blue) {
		return getHex(red) + getHex(green) + getHex(blue);		
	}
	
	private static String getHex(float f) {
		
		int i = Math.round(f);
		String hexValue = Integer.toHexString(i);
		
		if (i < 16) {
			// Pad so we have 2 digits
			return "0" + hexValue;
		}
		return hexValue;
	}

	/**
	 * @since 3.0.0
	 */
	public static String toHexColor(int color) {
		String	ret = Integer.toHexString(color).toUpperCase();
		return (ret.length() < 6 ?
				"000000".substring(0, 6 - ret.length()) + ret :
				ret);
	}
	
	/**
	 * @since 3.0.0
	 */
	public static int combineColors(int fgColor, int bgColor, int pctFg) {
	int resColor = 0;
		if (pctFg < 1) {
			resColor = bgColor;
		}
		else if (pctFg == 100) {
			resColor = fgColor;
		}
		else {
			int pctBg = 100 - pctFg;
			resColor = 
					//Red
					(((((((fgColor >> 16) & 0xff) * pctFg) +
					    (((bgColor >> 16) & 0xff) * pctBg))) / 100) << 16) |
					//Green
					(((((((fgColor >> 8) & 0xff) * pctFg) +
					    (((bgColor >> 8) & 0xff) * pctBg))) / 100) << 8) |
					//Blue
					(((((fgColor & 0xff) * pctFg) +
					    ((bgColor & 0xff) * pctBg))) / 100);
		}
		
		return resColor;
	}
	
	private String calcHexColor(int value) {
	String	ret = Integer.toHexString(value).toUpperCase();
		return (ret.length() < 6 ?
				"000000".substring(0, 6 - ret.length()) + ret :
				ret);
	}
	
//	public static void main(String[] args) throws Exception {
//		System.out.println(format2DP.format(twipToInch(2235)));
//		System.out.println(twipToBest(2235) );
//	}
	
	/** Word's 1/300 inch grid, in points: 0.24pt a cell, the unit its layout works in. */
	private static final double BORDER_GRID_PT = 72.0 / 300.0;

	/**
	 * The width Word inks a {@code w:sz} border: the eighths of a point it names, laid on
	 * Word's 1/300-inch grid and <b>truncated</b> to whole cells.
	 *
	 * <p>Measured on four Word goldens, five distinct widths, every one the floor:</p>
	 *
	 * <pre>
	 *   w:sz   named      cells    Word inks    golden
	 *      4   0.50pt      2.083     0.480pt    table-cell-measure, table-first-row-border
	 *      8   1.00pt      4.167     0.960pt    page-top-space-before, table-first-row-border
	 *     12   1.50pt      6.250     1.440pt    table-cell-measure
	 *     18   2.25pt      9.375     2.160pt    border-hanging
	 *     24   3.00pt     12.500     2.880pt    table-cell-measure
	 * </pre>
	 *
	 * <p>{@code w:sz} 24 is the one that settles the rounding: 3.00pt is <b>exactly</b> 12.5
	 * cells and Word inks 12, not 13, so it truncates rather than rounding half up.  (Half to
	 * even would agree on all five; truncation is the simpler statement of the same readings.)</p>
	 *
	 * <p>Used by the XSL FO paths only.  The paragraph border wrote millimetres to two
	 * decimals until 17.1.1 - {@code w:sz} 18 came out as 0.79mm = 2.2394pt against Word's
	 * 2.160 - and the table border wrote the named points, 0.50 where Word inks 0.480; both
	 * were part of the residue measured on every bar of the {@code border-hanging} golden
	 * (CR-001 batch 48 item 9 §2, batch 49 item 5).  The CSS paths still write millimetres:
	 * a browser has no 1/300-inch grid to honour.</p>
	 *
	 * @param eighths the {@code w:sz}, in eighths of a point
	 * @since 17.1.1 (CR-001 batch 49 item 5)
	 */
	public static double eighthsToGridPt(int eighths) {
		if (eighths <= 0) return 0;
		return Math.floor((eighths / 8.0) / BORDER_GRID_PT) * BORDER_GRID_PT;
	}

}
