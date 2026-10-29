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
 * {@code wml.xsd}, one pair per type: {@link #parse} and {@link #print} for {@code ST_DecimalNumber},
 * {@code ST_UnsignedDecimalNumber} and {@code ST_PixelsMeasure}; {@link #parseTwips} for {@code ST_TwipsMeasure};
 * {@link #parseSignedTwips} for {@code ST_SignedTwipsMeasure}; {@link #parseSignedHps} for
 * {@code ST_SignedHpsMeasure}; {@link #parseHps} for {@code CT_HpsMeasure/@w:val} ({@code w:sz}, {@code w:szCs},
 * {@code w:kern}); {@link #parseEighthPoint} and {@link #parsePoint} for a border's {@code w:sz} and {@code w:space};
 * {@link #parseTextScale} for {@code ST_TextScale} (an {@link Integer}); and their print methods. So the first
 * unmarshal of a part succeeds for every integer attribute and the retry through the preprocessor stylesheet is
 * never needed for a decimal. The Java type stays {@link BigInteger}. Before this, JAXB's own parse threw a raw
 * {@link NumberFormatException} out of the unmarshaller for any decimal, and a part whose attribute was not on the
 * stylesheet's rounding list could not be read at all.</p>
 *
 * <p>Word's range (CR-066 D1 to D4, D7; measured in its section 3, rounds 5 and 6): Word stores these attributes in
 * 16 bits or fewer and reads a value outside that width modulo the width, so {@code w:ind/@w:left="7205001"} is
 * -3959 twips in Word. The width is the attribute's, not the type's, so each type's methods report what is certain
 * for every attribute of the type: {@code ST_SignedTwipsMeasure} and {@code ST_SignedHpsMeasure} are signed 16-bit
 * (-32768..32767); {@code CT_HpsMeasure/@w:val} and {@code ST_TextScale} unsigned 16-bit (0..65535); a border's
 * {@code w:sz} 8 bits (0..255) and its {@code w:space} 5 bits (0..31), each reported with the value Word reads;
 * {@code ST_TwipsMeasure} mixes signed and unsigned 16-bit attributes and a few other shapes, so a value outside
 * -32768..65535 is reported as one Word changes, without saying how; {@code ST_DecimalNumber} is not reported (its
 * attributes differ: {@code w:tblW/@w:w} is 16-bit, {@code w:pgNumType/@w:start} 32-bit, ids and counts unmeasured).
 * The report is a WARN, at unmarshal and at marshal alike; the value is held as written and written as held, never
 * wrapped, clamped or refused (D2, D3): the caller's number is the one to fix, and docx4j's own helpers are guarded
 * where they compute one ({@code TblFactory}, the CSS converters).</p>
 *
 * <p>Reporting (CR-065 D6, CR-066 D8): a part's unmarshal and its marshal bracket the work with
 * {@link #beginReport()} and {@link #endReport()}, and log one line per part with the counts and the first few
 * values (INFO for the lenient reads, WARN when a value is outside Word's range); outside such a scope (an
 * unmarshal of a string, say) each lenient read is logged at INFO on its own and each out-of-range value at WARN.
 * A dropped value is logged at WARN as it is met, in either case, since it is the one reading that loses
 * information.</p>
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

	// ---- the per-type methods (CR-066 D1, D7): parse and print, plus the range report for that type

	/** The storage width Word gives every attribute of a type (CR-066 section 3.1 and 3.4). */
	enum Range {
		/** -32768..32767, read modulo 2^16 as signed: ST_SignedTwipsMeasure, ST_SignedHpsMeasure */
		INT16("int16", "-32768..32767", 16, true),
		/** 0..65535, read modulo 2^16: CT_HpsMeasure/@w:val (w:sz, w:szCs, w:kern), ST_TextScale */
		UINT16("uint16", "0..65535", 16, false),
		/** 0..255, read modulo 2^8: a border's w:sz (ST_EighthPointMeasure) */
		UINT8("8 bits", "0..255", 8, false),
		/** 0..31, read modulo 2^5: a border's w:space (ST_PointMeasure) */
		UINT5("5 bits", "0..31", 5, false),
		/**
		 * -32768..65535, the range outside which every measured ST_TwipsMeasure attribute is changed; how depends
		 * on the attribute (w:before and the unsigned margins are unsigned 16-bit, a frame's or w:tblpPr's
		 * distances signed, w:firstLine and w:hanging one signed field, w:trHeight's sign its rule), so no "reads
		 * it as" is given
		 */
		TWIPS("int16 or uint16 by attribute (ST_TwipsMeasure)", "-32768..65535", 16, false);

		/** The storage, in the ports' shared vocabulary: int16, uint16, 8 bits, 5 bits (docx4j-python CR-020 section 4.7) */
		final String storage;
		/** The faithful min..max */
		final String range;
		final int bits;
		final boolean signed;
		final BigInteger modulus;

		Range(String storage, String range, int bits, boolean signed) {
			this.storage = storage; this.range = range; this.bits = bits; this.signed = signed;
			this.modulus = BigInteger.ONE.shiftLeft(bits);
		}

		boolean holds(BigInteger v) {
			if (this == TWIPS) return v.signum() < 0 ? v.bitLength() <= 15 : v.bitLength() <= 16;
			if (signed) return v.bitLength() < bits;
			return v.signum() >= 0 && v.bitLength() <= bits;
		}

		/** The value Word reads for one outside the range: modulo the width, as signed or unsigned; null for TWIPS. */
		BigInteger wordsReading(BigInteger v) {
			if (this == TWIPS) return null;
			BigInteger r = v.mod(modulus);
			if (signed && r.bitLength() >= bits) r = r.subtract(modulus);
			return r;
		}
	}

	private static BigInteger checked(BigInteger v, Range range) {
		if (v != null && !range.holds(v)) reportOutOfRange(v, range);
		return v;
	}

	/** {@code ST_SignedTwipsMeasure} (w:ind left and right, w:spacing line, a tab's pos, frame and floating-table positions, cols space...): signed 16-bit. */
	public static BigInteger parseSignedTwips(String v) { return checked(parse(v), Range.INT16); }
	/** The written form of an {@code ST_SignedTwipsMeasure}: as held, an out-of-range value reported. */
	public static String printSignedTwips(BigInteger v) { return print(checked(v, Range.INT16)); }

	/** {@code ST_SignedHpsMeasure} (w:position): signed 16-bit. */
	public static BigInteger parseSignedHps(String v) { return checked(parse(v), Range.INT16); }
	/** The written form of an {@code ST_SignedHpsMeasure}: as held, an out-of-range value reported. */
	public static String printSignedHps(BigInteger v) { return print(checked(v, Range.INT16)); }

	/** {@code CT_HpsMeasure/@w:val} (w:sz, w:szCs, w:kern; half-points): unsigned 16-bit. */
	public static BigInteger parseHps(String v) { return checked(parse(v), Range.UINT16); }
	/** The written form of a {@code CT_HpsMeasure/@w:val}: as held, an out-of-range value reported. */
	public static String printHps(BigInteger v) { return print(checked(v, Range.UINT16)); }

	/** {@code ST_EighthPointMeasure} (a border's w:sz): 8 bits. */
	public static BigInteger parseEighthPoint(String v) { return checked(parse(v), Range.UINT8); }
	/** The written form of an {@code ST_EighthPointMeasure}: as held, an out-of-range value reported. */
	public static String printEighthPoint(BigInteger v) { return print(checked(v, Range.UINT8)); }

	/** {@code ST_PointMeasure} (a border's w:space): 5 bits. */
	public static BigInteger parsePoint(String v) { return checked(parse(v), Range.UINT5); }
	/** The written form of an {@code ST_PointMeasure}: as held, an out-of-range value reported. */
	public static String printPoint(BigInteger v) { return print(checked(v, Range.UINT5)); }

	/**
	 * {@code ST_TwipsMeasure} (w:spacing before and after, the margins, w:ind firstLine and hanging, w:trHeight,
	 * frame and floating-table distances, w:defaultTabStop...): 16-bit, signed or unsigned by attribute, so only a
	 * value outside -32768..65535 is reported, as one Word changes.
	 */
	public static BigInteger parseTwips(String v) { return checked(parse(v), Range.TWIPS); }
	/** The written form of an {@code ST_TwipsMeasure}: as held, a value outside -32768..65535 reported. */
	public static String printTwips(BigInteger v) { return print(checked(v, Range.TWIPS)); }

	/** {@code ST_TextScale} (w:w; the schema's 0..600 is the UI's, Word keeps 601): unsigned 16-bit, an {@link Integer}. */
	public static Integer parseTextScale(String v) {
		Integer n = parseInteger(v);
		if (n != null) checked(BigInteger.valueOf(n), Range.UINT16);
		return n;
	}
	/** The written form of an {@code ST_TextScale}: as held, an out-of-range value reported. */
	public static String printTextScale(Integer v) {
		if (v != null) checked(BigInteger.valueOf(v), Range.UINT16);
		return printInteger(v);
	}

	// ---- reporting (D6)

	private enum Kind { TRUNCATED, EMPTY, DROPPED }

	/** What one scope (one part's unmarshal or marshal) read leniently (CR-065) or found outside Word's range (CR-066). */
	public static final class Report {
		private int truncated, empty, dropped, outOfRange;
		private final List<String> samples = new ArrayList<>(4);
		private final List<String> rangeSamples = new ArrayList<>(4);
		int depth = 1;

		public int getTruncated() { return truncated; }
		public int getEmpty() { return empty; }
		public int getDropped() { return dropped; }
		/** Values outside Word's range for their type (CR-066): held as written, reported at WARN. @since 17.3.5 */
		public int getOutOfRange() { return outOfRange; }
		public boolean isEmpty() { return truncated == 0 && empty == 0 && dropped == 0 && outOfRange == 0; }

		/** One line for a log: the counts and up to four of the values, text and reading. */
		public String summary() {
			StringBuilder sb = new StringBuilder();
			if (truncated > 0) sb.append(truncated).append(" decimal integer attribute value").append(truncated == 1 ? "" : "s").append(" truncated as Word reads them");
			if (empty > 0) sb.append(sb.length() > 0 ? "; " : "").append(empty).append(" empty read as 0");
			if (dropped > 0) sb.append(sb.length() > 0 ? "; " : "").append(dropped).append(" of a form Word refuses dropped");
			if (!samples.isEmpty()) sb.append(" (e.g. ").append(String.join(", ", samples)).append(")");
			if (outOfRange > 0) {
				sb.append(sb.length() > 0 ? "; " : "").append(CODE).append(": ").append(outOfRange).append(" integer attribute value").append(outOfRange == 1 ? "" : "s")
					.append(" outside Word's 16-bit range, held as written (CR-066; e.g. ").append(String.join(", ", rangeSamples)).append(")");
			}
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

	/**
	 * The code the ports share for an integer attribute Word would misread (docx4j-python CR-020 section 4.7 and
	 * CR-022, docx4j-core-ts CR-017): a search for it finds every port's report. The fields the type-level report
	 * can give are the value, Word's reading (none for ST_TwipsMeasure), the storage and the range; the part is on
	 * the part's own line; the element and attribute are the whole-tree check's (CR-066 D6).
	 *
	 * @since 17.3.5
	 */
	public static final String CODE = "value.out_of_range";

	private static void reportOutOfRange(BigInteger v, Range range) {
		BigInteger reading = range.wordsReading(v);
		String sample = v + " (" + range.storage + " " + range.range
				+ (reading == null ? "; Word changes it, how depends on the attribute)" : "; Word reads " + reading + ")");
		Report r = REPORT.get();
		if (r == null) {
			log.warn("{}: integer attribute value {} is outside Word's range; held as written (CR-066)", CODE, sample);
			return;
		}
		r.outOfRange++;
		if (r.rangeSamples.size() < 4) r.rangeSamples.add(sample);
	}
}
