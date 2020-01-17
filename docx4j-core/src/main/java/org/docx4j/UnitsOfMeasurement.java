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
	public static String twipToBest(int leftL ) {
		
		float inch4f = 80*twipToInch(leftL);
		float inch4fabit = inch4f + 0.49f;
		int inch4 = Math.round(inch4f);
		int inch4next = Math.round( inch4fabit);
		float inches = twipToInch(leftL);
		if (inch4==inch4next) {
			log.debug(leftL + " twips -> " + inches + "inches");
			// inches work 			
			return format2DP.format(inches) + "in";
		} else {
			float mm = inches/0.0394f;
			log.debug(leftL + " twips -> " + mm + "mm ("+ format2DP.format(inches) + "inches)");
			return Math.round(mm) + "mm";
		} 							
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
	
}
