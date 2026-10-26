/*
 *  Copyright 2026, Plutext Pty Ltd.
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
package org.docx4j.wml;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An integer attribute of WordprocessingML read as Word 365 reads it (CR-065 D2 to D4, D6; measured in its
 * section 3, four rounds): the lexical form is an optional sign, digits, optionally a dot and digits, either digit
 * group allowed to be empty but not both, or the empty string; no surrounding whitespace. A fraction is dropped
 * (truncation toward zero: {@code 720.5} is 720, {@code -121.5} is -121, {@code 141.99999999999994} is 141,
 * {@code .5} is 0, {@code 240.} is 240); the empty string is 0. Any other form (whitespace, a comma, hex, an exponent,
 * letters) makes Word refuse the whole document; docx4j drops that attribute instead ({@link #parse} returns null)
 * and logs at WARN (D3). A sign or a dot with no digit at all ({@code -}, {@code .}, {@code +}, {@code +.}) is 0, as
 * Word reads it (measured 2026-10-26, CR-066 round 6 group E). Unmeasured and treated as a refused form: digits
 * other than ASCII {@code 0}-{@code 9}.
 *
 * <p>Bound by {@code xsd/bindings.xjb} as the parse and print methods of the integer simple types of
 * {@code wml.xsd} ({@code ST_DecimalNumber}, {@code ST_TwipsMeasure}, {@code ST_SignedTwipsMeasure},
 * {@code ST_SignedHpsMeasure}, {@code ST_EighthPointMeasure}, {@code ST_PointMeasure}, {@code ST_TextScale},
 * {@code ST_PixelsMeasure}, {@code ST_UnsignedDecimalNumber}; {@code ST_TextScale} through {@link #parseInteger}) and of
 * {@code CT_HpsMeasure/@w:val}, so the first
 * unmarshal of a part succeeds for every integer attribute and the retry through the preprocessor stylesheet is
 * never needed for a decimal. The Java type stays {@link BigInteger}. Before this, JAXB's own parse threw a raw
 * {@link NumberFormatException} out of the unmarshaller for any decimal, and a part whose attribute was not on the
 * stylesheet's rounding list could not be read at all.</p>
 *
 * <p>{@link #print} writes the value as held, as before; it is also the hook where an out-of-range value will be
 * reported at marshal (CR-066).</p>
 *
 * <p>Reporting (D6): a part's unmarshal brackets its read with {@link #beginReport()} and {@link #endReport()},
 * and logs one INFO line per part with the counts and the first few values; outside such a scope (an unmarshal
 * of a string, say) each lenient read is logged at INFO on its own. A dropped value is logged at WARN as it is met,
 * in either case, since it is the one reading that loses information.</p>
 *
 * @since 17.3.5
 */
public final class LenientInteger {

	private static final Logger log = LoggerFactory.getLogger(LenientInteger.class);

	private LenientInteger() {}

	/**
	 * Word's reading of an integer attribute's text (the XJC {@code parseMethod}): the value, or null for a form
	 * Word refuses (the attribute is dropped; logged at WARN), or null for null.
	 */
	public static BigInteger parse(String v) {
		if (v == null) return null;
		int len = v.length();
		// the fast path: Word's integer form, which is also BigInteger's
		int i = 0;
		if (len > 0 && (v.charAt(0) == '-' || v.charAt(0) == '+')) i = 1;
		int digits = 0;
		for (; i < len; i++) {
			char c = v.charAt(i);
			if (c < '0' || c > '9') break;
			digits++;
		}
		if (i == len && digits > 0) {
			return new BigInteger(v);
		}
		return parseSlowly(v);
	}

	private static BigInteger parseSlowly(String v) {
		if (v.isEmpty()) {
			report(Kind.EMPTY, v, BigInteger.ZERO);
			return BigInteger.ZERO;
		}
		// [+-]?\d*(\.\d*)? with at least one digit, else refused
		int len = v.length(), i = 0;
		boolean negative = false;
		if (v.charAt(0) == '-' || v.charAt(0) == '+') { negative = v.charAt(0) == '-'; i = 1; }
		int intStart = i;
		while (i < len && v.charAt(i) >= '0' && v.charAt(i) <= '9') i++;
		int intEnd = i;
		int fractionDigits = 0;
		if (i < len && v.charAt(i) == '.') {
			i++;
			while (i < len && v.charAt(i) >= '0' && v.charAt(i) <= '9') { i++; fractionDigits++; }
		}
		if (i != len) {
			report(Kind.DROPPED, v, null);
			return null;
		}
		// a sign or a dot with no digit at all ("-", ".", "+", "+.") is 0: measured 2026-10-26 (CR-066 round 6, group E)
		BigInteger n = intEnd == intStart ? BigInteger.ZERO : new BigInteger(v.substring(intStart, intEnd));
		if (negative) n = n.negate();
		report(Kind.TRUNCATED, v, n);
		return n;
	}

	/** The written form (the XJC {@code printMethod}): the value as held; null for null. */
	public static String print(BigInteger v) {
		return v == null ? null : v.toString();
	}

	/**
	 * {@link #parse}, for the one type XJC binds to {@link Integer} rather than {@link BigInteger}
	 * ({@code ST_TextScale}, bounded 0..600 in the schema; the Java type is kept). A value beyond an int is
	 * dropped as a refused form would be, with the same WARN.
	 */
	public static Integer parseInteger(String v) {
		BigInteger n = parse(v);
		if (n == null) return null;
		if (n.bitLength() > 31) {
			log.warn("integer attribute value \"{}\" is beyond the int the schema binds it to; the attribute is dropped", v);
			return null;
		}
		return n.intValue();
	}

	/** The written form of an {@link Integer}-typed attribute: the value as held; null for null. */
	public static String printInteger(Integer v) {
		return v == null ? null : v.toString();
	}

	// ---- reporting (D6)

	private enum Kind { TRUNCATED, EMPTY, DROPPED }

	/** What one scope (one part's unmarshal) read leniently. */
	public static final class Report {
		private int truncated, empty, dropped;
		private final List<String> samples = new ArrayList<>(4);
		int depth = 1;

		public int getTruncated() { return truncated; }
		public int getEmpty() { return empty; }
		public int getDropped() { return dropped; }
		public boolean isEmpty() { return truncated == 0 && empty == 0 && dropped == 0; }

		/** One line for a log: the counts and up to four of the values, text and reading. */
		public String summary() {
			StringBuilder sb = new StringBuilder();
			if (truncated > 0) sb.append(truncated).append(" decimal integer attribute value").append(truncated == 1 ? "" : "s").append(" truncated as Word reads them");
			if (empty > 0) sb.append(sb.length() > 0 ? "; " : "").append(empty).append(" empty read as 0");
			if (dropped > 0) sb.append(sb.length() > 0 ? "; " : "").append(dropped).append(" of a form Word refuses dropped");
			if (!samples.isEmpty()) sb.append(" (e.g. ").append(String.join(", ", samples)).append(")");
			return sb.toString();
		}
	}

	private static final ThreadLocal<Report> REPORT = new ThreadLocal<>();

	/** Opens a reporting scope on this thread (re-entrant: an inner begin and end are counted, not reported). */
	public static void beginReport() {
		Report r = REPORT.get();
		if (r == null) REPORT.set(new Report()); else r.depth++;
	}

	/**
	 * Closes the scope opened by {@link #beginReport()}: the report, for the caller to log with its own context
	 * (the part name), when the outermost scope closes; null for an inner scope or when none is open.
	 */
	public static Report endReport() {
		Report r = REPORT.get();
		if (r == null) return null;
		if (--r.depth > 0) return null;
		REPORT.remove();
		return r;
	}

	private static void report(Kind kind, String text, BigInteger value) {
		String sample = "\"" + text + "\"" + (value == null ? " dropped" : " -> " + value);
		if (kind == Kind.DROPPED) {
			log.warn("integer attribute value \"{}\" is not a form Word reads ([+-]?digits[.digits], or empty); the attribute is dropped (CR-065 D3)", text);
		}
		Report r = REPORT.get();
		if (r == null) {
			if (kind != Kind.DROPPED) log.info("integer attribute value {} (Word's reading; CR-065)", sample);
			return;
		}
		switch (kind) {
			case TRUNCATED: r.truncated++; break;
			case EMPTY: r.empty++; break;
			default: r.dropped++;
		}
		if (r.samples.size() < 4) r.samples.add(sample);
	}
}
