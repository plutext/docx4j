/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
 
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.xlsx4j.org.apache.poi.ss.util;


/**
 * For working with the internals of IEEE 754-2008 'binary64' (double precision) floating point numbers
 *
 * @author Josh Micich
 */
final class IEEEDouble {
	private static final long EXPONENT_MASK = 0x7FF0000000000000L;
	private static final int  EXPONENT_SHIFT = 52;
	public static final long FRAC_MASK = 0x000FFFFFFFFFFFFFL;
	public static final int  EXPONENT_BIAS  = 1023;
	public static final long FRAC_ASSUMED_HIGH_BIT = ( 1L<<EXPONENT_SHIFT );
	/**
	 * The value the exponent field gets for all <i>NaN</i> and <i>Infinity</i> values
	 */
	public static final int BIASED_EXPONENT_SPECIAL_VALUE = 0x07FF;

	/**
	 * @param rawBits the 64 bit binary representation of the double value
	 * @return the top 12 bits (sign and biased exponent value)
	 */
	public static int getBiasedExponent(long rawBits) {
		return (int) ((rawBits & EXPONENT_MASK) >> EXPONENT_SHIFT);
	}
}
