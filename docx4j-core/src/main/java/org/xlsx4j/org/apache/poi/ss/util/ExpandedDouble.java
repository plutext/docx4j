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

import java.math.BigInteger;
import static org.xlsx4j.org.apache.poi.ss.util.IEEEDouble.*;

/**
 * Represents a 64 bit IEEE double quantity expressed with both decimal and binary exponents
 * Does not handle negative numbers or zero
 * <p/>
 * The value of a {@link ExpandedDouble} is given by<br/>
 * <tt> a &times; 2<sup>b</sup></tt>
 * <br/>
 * where:<br/>
 *
 * <tt>a</tt> = <i>significand</i><br/>
 * <tt>b</tt> = <i>binaryExponent</i> - bitLength(significand) + 1<br/>
 *
 * @author Josh Micich
 */
final class ExpandedDouble {
	private static final BigInteger BI_FRAC_MASK = BigInteger.valueOf(FRAC_MASK);
	private static final BigInteger BI_IMPLIED_FRAC_MSB = BigInteger.valueOf(FRAC_ASSUMED_HIGH_BIT);

	private static BigInteger getFrac(long rawBits) {
		return BigInteger.valueOf(rawBits).and(BI_FRAC_MASK).or(BI_IMPLIED_FRAC_MSB).shiftLeft(11);
	}


	public static ExpandedDouble fromRawBitsAndExponent(long rawBits, int exp) {
		return new ExpandedDouble(getFrac(rawBits), exp);
	}

	/**
	 * Always 64 bits long (MSB, bit-63 is '1')
	 */
	private final BigInteger _significand;
	private final int _binaryExponent;

	public ExpandedDouble(long rawBits) {
		int biasedExp = (int) (rawBits >> 52);
		if (biasedExp == 0) {
			// sub-normal numbers
			BigInteger frac = BigInteger.valueOf(rawBits).and(BI_FRAC_MASK);
			int expAdj = 64 - frac.bitLength();
			_significand = frac.shiftLeft(expAdj);
			_binaryExponent = (biasedExp & 0x07FF) - 1023 - expAdj;
		} else {
			BigInteger frac = getFrac(rawBits);
			_significand = frac;
			_binaryExponent = (biasedExp & 0x07FF) - 1023;
		}
	}

	ExpandedDouble(BigInteger frac, int binaryExp) {
		if (frac.bitLength() != 64) {
			throw new IllegalArgumentException("bad bit length");
		}
		_significand = frac;
		_binaryExponent = binaryExp;
	}


	/**
	 * Convert to an equivalent {@link NormalisedDecimal} representation having 15 decimal digits of precision in the
	 * non-fractional bits of the significand.
	 */
	public NormalisedDecimal normaliseBaseTen() {
		return NormalisedDecimal.create(_significand, _binaryExponent);
	}

	/**
	 * @return the number of non-fractional bits after the MSB of the significand
	 */
	public int getBinaryExponent() {
		return _binaryExponent;
	}

	public BigInteger getSignificand() {
		return _significand;
	}
}
