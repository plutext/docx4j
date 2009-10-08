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

public class UnitsOfMeasurement {
	

	// Defaults - if values aren't defined in sectPr 
	public static int DEFAULT_PAGE_WIDTH_TWIPS = 12240;  // Letter; A4 would be 11907  
	public static int DEFAULT_LEFT_MARGIN_TWIPS = 1440;  // 1 inch
	public static int DEFAULT_RIGHT_MARGIN_TWIPS = 1440;

	
	public static long twipToEMU(double twips) {		
		return Math.round(635 * twips);				
	}	

	public static int inchToTwip(float inch  ) {
		// 720 twip = 1 inch;
		return Math.round(inch*720);		
	}
	
	public static float twipToInch(int twip) {		
		return twip/720;		
	}
	
	public static int mmToTwip(float mm  ) {		
		float inch = mm*0.0394f;
		return inchToTwip(inch);
	}
	
}
