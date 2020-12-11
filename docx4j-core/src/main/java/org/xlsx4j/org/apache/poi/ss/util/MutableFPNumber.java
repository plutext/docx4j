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

final class MutableFPNumber {


	// TODO - what about values between (10<sup>14</sup>-0.5) and (10<sup>14</sup>-0.05) ?
	/**
	 * The minimum value in 'Base-10 normalised form'.<br/>
	 * When {@link #_binaryExponent} == 46 this is the the minimum {@link #_frac} value
	 *  (10<sup>14</sup>-0.05) * 2^17
	 *  <br/>
	 *  Values between (10<sup>14</sup>-0.05) and 10<sup>14</sup> will be represented as '1'
	 *  followed by 14 zeros.
	 *  Values less than (10<sup>14</sup>-0.05) will get shifted by one more power of 10
	 *
	 *  This frac value rounds to '1' followed by fourteen zeros with an incremented decimal exponent
	 */
	private static final BigInteger BI_MIN_BASE = new BigInteger("0B5E620F47FFFE666", 16);
	/**
	 * For 'Base-10 normalised form'<br/>
	 * The maximum {@link #_frac} value when {@link #_binaryExponent} == 49
	 * (10^15-0.5) * 2^14
	 */
	private static final BigInteger BI_MAX_BASE = new BigInteger("0E35FA9319FFFE000", 16);

	/**
	 * Width of a long
	 */
	private static final int C_64 = 64;

	/**
	 * Minimum precision after discarding whole 32-bit words from the significand
	 */
	private static final int MIN_PRECISION = 72;
	private BigInteger _significand;
	private int _binaryExponent;
	public MutableFPNumber(BigInteger frac, int binaryExponent) {
		_significand = frac;
		_binaryExponent = binaryExponent;
	}


	public MutableFPNumber copy() {
		return new MutableFPNumber(_significand, _binaryExponent);
	}
	public void normalise64bit() {
		int oldBitLen = _significand.bitLength();
		int sc = oldBitLen - C_64;
		if (sc == 0) {
			return;
		}
		if (sc < 0) {
			throw new IllegalStateException("Not enough precision");
		}
		_binaryExponent += sc;
		if (sc > 32) {
			int highShift = (sc-1) & 0xFFFFE0;
			_significand = _significand.shiftRight(highShift);
			sc -= highShift;
			oldBitLen -= highShift;
		}
		if (sc < 1) {
			throw new IllegalStateException();
		}
		_significand = Rounder.round(_significand, sc);
		if (_significand.bitLength() > oldBitLen) {
			sc++;
			_binaryExponent++;
		}
		_significand = _significand.shiftRight(sc);
	}
	public int get64BitNormalisedExponent() {
		return _binaryExponent + _significand.bitLength() - C_64;

	}

	public boolean isBelowMaxRep() {
		int sc = _significand.bitLength() - C_64;
		return _significand.compareTo(BI_MAX_BASE.shiftLeft(sc)) < 0;
	}
	public boolean isAboveMinRep() {
		int sc = _significand.bitLength() - C_64;
		return _significand.compareTo(BI_MIN_BASE.shiftLeft(sc)) > 0;
	}
	public NormalisedDecimal createNormalisedDecimal(int pow10) {
		// missingUnderBits is (0..3)
		int missingUnderBits = _binaryExponent-39;
		int fracPart = (_significand.intValue() << missingUnderBits) & 0xFFFF80;
		long wholePart = _significand.shiftRight(C_64-_binaryExponent-1).longValue();
		return new NormalisedDecimal(wholePart, fracPart, pow10);
	}
	public void multiplyByPowerOfTen(int pow10) {
		TenPower tp = TenPower.getInstance(Math.abs(pow10));
		if (pow10 < 0) {
			mulShift(tp._divisor, tp._divisorShift);
		} else {
			mulShift(tp._multiplicand, tp._multiplierShift);
		}
	}
	private void mulShift(BigInteger multiplicand, int multiplierShift) {
		_significand = _significand.multiply(multiplicand);
		_binaryExponent += multiplierShift;
		// check for too much precision
		int sc = (_significand.bitLength() - MIN_PRECISION) & 0xFFFFFFE0;
		// mask makes multiples of 32 which optimises BigInteger.shiftRight
		if (sc > 0) {
			// no need to round because we have at least 8 bits of extra precision
			_significand = _significand.shiftRight(sc);
			_binaryExponent += sc;
		}
	}

	private static final class Rounder {
		private static final BigInteger[] HALF_BITS;

		static {
			BigInteger[] bis = new BigInteger[33];
			long acc=1;
			for (int i = 1; i < bis.length; i++) {
				bis[i] = BigInteger.valueOf(acc);
				acc <<=1;
			}
			HALF_BITS = bis;
		}
		/**
		 * @param nBits number of bits to shift right
		 */
		public static BigInteger round(BigInteger bi, int nBits) {
			if (nBits < 1) {
				return bi;
			}
			return bi.add(HALF_BITS[nBits]);
		}
	}

	/**
	 * Holds values for quick multiplication and division by 10
	 */
	private static final class TenPower {
		private static final BigInteger FIVE = new BigInteger("5");
		private static final TenPower[] _cache = new TenPower[350];

		public final BigInteger _multiplicand;
		public final BigInteger _divisor;
		public final int _divisorShift;
		public final int _multiplierShift;

		private TenPower(int index) {
			BigInteger fivePowIndex = FIVE.pow(index);

			int bitsDueToFiveFactors = fivePowIndex.bitLength();
			int px = 80 + bitsDueToFiveFactors;
			BigInteger fx = BigInteger.ONE.shiftLeft(px).divide(fivePowIndex);
			int adj = fx.bitLength() - 80;
			_divisor = fx.shiftRight(adj);
			bitsDueToFiveFactors -= adj;

			_divisorShift = -(bitsDueToFiveFactors+index+80);
			int sc = fivePowIndex.bitLength() - 68;
			if (sc > 0) {
				_multiplierShift = index + sc;
				_multiplicand = fivePowIndex.shiftRight(sc);
			} else {
				_multiplierShift = index;
				_multiplicand = fivePowIndex;
			}
		}

		static TenPower getInstance(int index) {
			TenPower result = _cache[index];
			if (result == null) {
				result = new TenPower(index);
				_cache[index] = result;
			}
			return result;
		}
	}

	public ExpandedDouble createExpandedDouble() {
		return new ExpandedDouble(_significand, _binaryExponent);
	}
}
