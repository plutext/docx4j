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

package org.apache.poi.ss.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Represents a transformation of a 64 bit IEEE double quantity having a decimal exponent and a
 * fixed point (15 decimal digit) significand.  Some quirks of Excel's calculation behaviour are
 * simpler to reproduce with numeric quantities in this format.  This class is currently used to
 * help:
 * <ol>
 * <li>Comparison operations</li>
 * <li>Conversions to text</li>
 * </ol>
 *
 * <p/>
 * This class does not handle negative numbers or zero.
 * <p/>
 * The value of a {@link NormalisedDecimal} is given by<br/>
 * <tt> significand &times; 10<sup>decimalExponent</sup></tt>
 * <br/>
 * where:<br/>
 *
 * <tt>significand</tt> = wholePart + fractionalPart / 2<sup>24</sup><br/>
 *
 * @author Josh Micich
 */
final class NormalisedDecimal {
	/**
	 * Number of powers of ten contained in the significand
	 */
	private static final int EXPONENT_OFFSET = 14;

	private static final BigDecimal BD_2_POW_24 = new BigDecimal(BigInteger.ONE.shiftLeft(24));

	/**
	 * log<sub>10</sub>(2)&times;2<sup>20</sup>
	 */
	private static final int LOG_BASE_10_OF_2_TIMES_2_POW_20 = 315653; // 315652.8287

	/**
	 * 2<sup>19</sup>
	 */
	private static final int C_2_POW_19 = 1 << 19;


	/**
	 * the value of {@link #_fractionalPart} that represents 0.5
	 */
	private static final int FRAC_HALF = 0x800000;

	/**
	 * 10<sup>15</sup>
	 */
	private static final long MAX_REP_WHOLE_PART = 0x38D7EA4C68000L;



	public static NormalisedDecimal create(BigInteger frac, int binaryExponent) {
		// estimate pow2&pow10 first, perform optional mulShift, then normalize
		int pow10;
		if (binaryExponent > 49 || binaryExponent < 46) {

			// working with ints (left shifted 20) instead of doubles
			// x = 14.5 - binaryExponent * log10(2);
			int x = (29 << 19) - binaryExponent * LOG_BASE_10_OF_2_TIMES_2_POW_20;
			x += C_2_POW_19; // round
			pow10 = -(x >> 20);
		} else {
			pow10 = 0;
		}
		MutableFPNumber cc = new MutableFPNumber(frac, binaryExponent);
		if (pow10 != 0) {
			cc.multiplyByPowerOfTen(-pow10);
		}

		switch (cc.get64BitNormalisedExponent()) {
			case 46:
				if (cc.isAboveMinRep()) {
					break;
				}
			case 44:
			case 45:
				cc.multiplyByPowerOfTen(1);
				pow10--;
				break;
			case 47:
			case 48:
				break;
			case 49:
				if (cc.isBelowMaxRep()) {
					break;
				}
			case 50:
				cc.multiplyByPowerOfTen(-1);
				pow10++;
				break;

			default:
				throw new IllegalStateException("Bad binary exp " + cc.get64BitNormalisedExponent() + ".");
		}
		cc.normalise64bit();

		return cc.createNormalisedDecimal(pow10);
	}

	/**
	 * Rounds at the digit with value 10<sup>decimalExponent</sup>
	 */
	public NormalisedDecimal roundUnits() {
		long wholePart = _wholePart;
		if (_fractionalPart >= FRAC_HALF) {
			wholePart++;
		}

		int de = _relativeDecimalExponent;

		if (wholePart < MAX_REP_WHOLE_PART) {
			return new NormalisedDecimal(wholePart, 0, de);
		}
		return new NormalisedDecimal(wholePart/10, 0, de+1);
	}

	/**
	 * The decimal exponent increased by one less than the digit count of {@link #_wholePart}
	 */
	private final int _relativeDecimalExponent;
	/**
	 * The whole part of the significand (typically 15 digits).
	 *
	 * 47-50 bits long (MSB may be anywhere from bit 46 to 49)
	 * LSB is units bit.
	 */
	private final long _wholePart;
	/**
	 * The fractional part of the significand.
	 * 24 bits (only top 14-17 bits significant): a value between 0x000000 and 0xFFFF80
	 */
	private final int _fractionalPart;


	NormalisedDecimal(long wholePart, int fracPart, int decimalExponent) {
		_wholePart = wholePart;
		_fractionalPart = fracPart;
		_relativeDecimalExponent = decimalExponent;
	}


	/**
	 * Convert to an equivalent {@link ExpandedDouble} representation (binary frac and exponent).
	 * The resulting transformed object is easily converted to a 64 bit IEEE double:
	 * <ul>
	 * <li>bits 2-53 of the {@link #getSignificand()} become the 52 bit 'fraction'.</li>
	 * <li>{@link #getBinaryExponent()} is biased by 1023 to give the 'exponent'.</li>
	 * </ul>
	 * The sign bit must be obtained from somewhere else.
	 * @return a new {@link NormalisedDecimal} normalised to base 2 representation.
	 */
	public ExpandedDouble normaliseBaseTwo() {
		MutableFPNumber cc = new MutableFPNumber(composeFrac(), 39);
		cc.multiplyByPowerOfTen(_relativeDecimalExponent);
		cc.normalise64bit();
		return cc.createExpandedDouble();
	}

	/**
	 * @return the significand as a fixed point number (with 24 fraction bits and 47-50 whole bits)
	 */
	BigInteger composeFrac() {
		long wp = _wholePart;
		int fp = _fractionalPart;
		return new BigInteger(new byte[] {
				(byte) (wp >> 56), // N.B. assuming sign bit is zero
				(byte) (wp >> 48),
				(byte) (wp >> 40),
				(byte) (wp >> 32),
				(byte) (wp >> 24),
				(byte) (wp >> 16),
				(byte) (wp >>  8),
				(byte) (wp >>  0),
				(byte) (fp >> 16),
				(byte) (fp >> 8),
				(byte) (fp >> 0),
		});
	}

	public String getSignificantDecimalDigits() {
		return Long.toString(_wholePart);
	}
	/**
	 * Rounds the first whole digit position (considers only units digit, not frational part).
	 * Caller should check total digit count of result to see whether the rounding operation caused
	 * a carry out of the most significant digit
	 */
	public String getSignificantDecimalDigitsLastDigitRounded() {
		long wp = _wholePart + 5; // rounds last digit
		StringBuilder sb = new StringBuilder(24);
		sb.append(wp);
		sb.setCharAt(sb.length()-1, '0');
		return sb.toString();
	}

	/**
	 * @return the number of powers of 10 which have been extracted from the significand and binary exponent.
	 */
	public int getDecimalExponent() {
		return _relativeDecimalExponent+EXPONENT_OFFSET;
	}

	/**
	 * assumes both this and other are normalised
	 */
	public int compareNormalised(NormalisedDecimal other) {
		int cmp = _relativeDecimalExponent - other._relativeDecimalExponent;
		if (cmp != 0) {
			return cmp;
		}
		if (_wholePart > other._wholePart) {
			return 1;
		}
		if (_wholePart < other._wholePart) {
			return -1;
		}
		return _fractionalPart - other._fractionalPart;
	}
	public BigDecimal getFractionalPart() {
		return new BigDecimal(_fractionalPart).divide(BD_2_POW_24);
	}

	private String getFractionalDigits() {
		if (_fractionalPart == 0) {
			return "0";
		}
		return getFractionalPart().toString().substring(2);
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName());
		sb.append(" [");
		String ws = String.valueOf(_wholePart);
		sb.append(ws.charAt(0));
		sb.append('.');
		sb.append(ws.substring(1));
		sb.append(' ');
		sb.append(getFractionalDigits());
		sb.append("E");
		sb.append(getDecimalExponent());
		sb.append("]");
		return sb.toString();
	}
}
