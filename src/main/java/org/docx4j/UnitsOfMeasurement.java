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

import org.apache.log4j.Logger;

public class UnitsOfMeasurement {
	private final static Logger log = Logger.getLogger(UnitsOfMeasurement.class);
	
	public final static DecimalFormat format2DP;
	static {
		format2DP =  new DecimalFormat("##.##", 
							new DecimalFormatSymbols(Locale.ENGLISH)); 
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
		
	public static int mmToTwip(float mm  ) {		
		float inch = mm*0.0394f;
		return inchToTwip(inch);
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
		
		if (i<=16) {
			// Pad so we have 2 digits
			return "0" + Integer.toHexString( i );
		} else {
			return Integer.toHexString( i );
		}
	}
	
//	public static void main(String[] args) throws Exception {
//		System.out.println(format2DP.format(twipToInch(2235)));
//		System.out.println(twipToBest(2235) );
//	}
	
}
