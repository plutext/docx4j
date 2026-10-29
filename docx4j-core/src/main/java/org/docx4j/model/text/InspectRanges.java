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
package org.docx4j.model.text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The per-attribute check of CR-066 D6 ({@code value.out_of_range}, CR-066 section 10): an integer attribute whose
 * value is outside the width Word stores it in, with the value Word reads instead. The widths are
 * {@code word-integer-storage.tsv} beside this class, CR-066's sections 3.1 and 3.4 row for row, which the
 * oracle ({@code inspect_oracle.py}) reads as well, so the two readings of the contract share one table. The
 * type-level floor every save and load gets is {@code org.docx4j.wml.LenientInteger}'s; this check knows the
 * element and the attribute, so it is exact where the type is not (an {@code ST_TwipsMeasure} attribute's width
 * is the attribute's) and gives Word's reading in the attribute's own terms (a hanging indent for a first-line
 * one, an atLeast line for an exact one, a clamp, a dropped rule).
 */
final class InspectRanges {

	static final String KIND = "value.out_of_range";
	static final String RESOURCE = "word-integer-storage.tsv";
	private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	private static final BigInteger K16 = BigInteger.valueOf(65536L), K15 = BigInteger.valueOf(32768L),
			K32 = BigInteger.ONE.shiftLeft(32), K31 = BigInteger.ONE.shiftLeft(31);
	/** Word's lexical form (CR-065): an optional sign, digits, an optional fraction; a bare sign or dot reads 0. */
	private static final Pattern LEXICAL = Pattern.compile("\\s*([+-]?)(\\d*)(?:\\.\\d*)?\\s*");

	/** One row of the table. */
	static final class Rule {
		final String element, attribute, contextName, storage, range, evidence;
		/** The context attribute's values that select the row; an empty string for absent; null for any value. */
		final Set<String> contextValues;
		final BigInteger lo, hi;

		Rule(String[] cols) {
			element = cols[0];
			attribute = cols[1];
			String ctx = cols[2];
			if (ctx.isEmpty()) {
				contextName = null;
				contextValues = null;
			} else {
				int eq = ctx.indexOf('=');
				contextName = ctx.substring(0, eq);
				contextValues = new HashSet<String>(Arrays.asList(ctx.substring(eq + 1).split("\\|", -1)));
			}
			storage = cols[3];
			range = cols[4];
			evidence = cols.length > 5 ? cols[5] : "";
			if (range.isEmpty()) {
				lo = hi = null;
			} else {
				int dots = range.indexOf("..");
				lo = new BigInteger(range.substring(0, dots));
				hi = new BigInteger(range.substring(dots + 2));
			}
		}

		boolean matches(Element e) {
			if (contextName == null) return true;
			String v = e.hasAttributeNS(W, contextName) ? e.getAttributeNS(W, contextName) : "";
			return contextValues.contains(v);
		}
	}

	private static final Map<String, List<Rule>> RULES = load();

	private InspectRanges() {}

	private static Map<String, List<Rule>> load() {
		Map<String, List<Rule>> rules = new HashMap<String, List<Rule>>();
		try (InputStream in = InspectRanges.class.getResourceAsStream(RESOURCE)) {
			if (in == null) throw new IllegalStateException(RESOURCE + " is missing from the docx4j-core jar");
			BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			String line;
			boolean header = true;
			while ((line = r.readLine()) != null) {
				if (line.isEmpty() || line.startsWith("#")) continue;
				if (header) { header = false; continue; }
				String[] cols = line.split("\t", -1);
				if (cols.length < 5) throw new IllegalStateException(RESOURCE + ": short row: " + line);
				Rule rule = new Rule(cols);
				List<Rule> list = rules.get(rule.element + "/" + rule.attribute);
				if (list == null) rules.put(rule.element + "/" + rule.attribute, list = new ArrayList<Rule>());
				list.add(rule);
			}
		} catch (java.io.IOException e) {
			throw new IllegalStateException(RESOURCE + " could not be read", e);
		}
		return rules;
	}

	/** The rows, for the tests: {@code element/attribute} to its rules in file order. */
	static Map<String, List<Rule>> rules() {
		return Collections.unmodifiableMap(RULES);
	}

	/**
	 * Every out-of-range attribute under a root, in document order, each as the element and the finding's
	 * summary ({@code attribute=w:ind/@w:left; value=7205001; storage=int16; range=-32768..32767; word_reads=-3959}).
	 */
	static List<Object[]> findings(Element root) {
		List<Object[]> out = new ArrayList<Object[]>();
		if (root != null) walk(root, out);
		return out;
	}

	private static void walk(Element e, List<Object[]> out) {
		if (W.equals(e.getNamespaceURI())) check(e, out);
		for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() == Node.ELEMENT_NODE) walk((Element) n, out);
		}
	}

	private static void check(Element e, List<Object[]> out) {
		String local = e.getLocalName();
		org.w3c.dom.NamedNodeMap atts = e.getAttributes();
		for (int i = 0; i < atts.getLength(); i++) {
			Node a = atts.item(i);
			if (!W.equals(a.getNamespaceURI())) continue;
			String summary = summary(local, a.getLocalName(), e, a.getNodeValue());
			if (summary != null) out.add(new Object[] { e, summary });
		}
	}

	/** The summary for an attribute's value on its element, or null where it is within Word's range or not checked. */
	static String summary(String element, String attribute, Element e, String text) {
		List<Rule> rules = RULES.get(element + "/" + attribute);
		if (rules == null) return null;
		Rule rule = null;
		for (Rule r : rules) if (r.matches(e)) { rule = r; break; }
		if (rule == null || rule.storage.equals("ignored")) return null;
		BigInteger v = parse(text);
		if (v == null || (v.compareTo(rule.lo) >= 0 && v.compareTo(rule.hi) <= 0)) return null;
		StringBuilder sb = new StringBuilder("attribute=w:").append(element).append("/@w:").append(attribute);
		// the key's deciding sibling (type, lineRule, hRule), named by any of its rows, where the element has it
		for (Rule r : rules) {
			if (r.contextName != null) {
				if (e.hasAttributeNS(W, r.contextName)) sb.append("; ").append(r.contextName).append('=').append(e.getAttributeNS(W, r.contextName));
				break;
			}
		}
		sb.append("; value=").append(text.trim()).append("; storage=").append(storage(rule.storage))
				.append("; range=").append(rule.range).append("; word_reads=").append(reading(rule, v));
		return sb.toString();
	}

	/** Word's lexical reading (CR-065): null for a form Word refuses (the attribute is dropped). */
	static BigInteger parse(String text) {
		if (text == null) return null;
		Matcher m = LEXICAL.matcher(text);
		if (!m.matches()) return null;
		String digits = m.group(2);
		BigInteger v = digits.isEmpty() ? BigInteger.ZERO : new BigInteger(digits);
		return m.group(1).equals("-") ? v.negate() : v;
	}

	/** The storage in the ports' shared vocabulary. */
	static String storage(String s) {
		switch (s) {
		case "int16": case "int16-clamp": case "column-w": case "column-space":
		case "pair-firstLine": case "pair-hanging":
		case "rule-negated": case "rule-positive": case "rule-negated-drop": case "rule-positive-drop":
			return "int16";
		case "uint16": case "uint16-clamp": case "uint16-min": return "uint16";
		case "int32": return "int32";
		case "uint8": return "8 bits";
		case "uint5": return "5 bits";
		case "bits15-flag": return "15 bits";
		case "twips-unmeasured": return "16 bits";
		default: return s;
		}
	}

	private static BigInteger wrap16(BigInteger v) {
		BigInteger m = v.mod(K16);
		return m.compareTo(K15) >= 0 ? m.subtract(K16) : m;
	}

	private static BigInteger wrap32(BigInteger v) {
		BigInteger m = v.mod(K32);
		return m.compareTo(K31) >= 0 ? m.subtract(K32) : m;
	}

	private static String rule(BigInteger stored, boolean dropAtZero) {
		int sign = stored.signum();
		if (sign < 0) return "exact " + stored.negate();
		if (sign > 0) return "atLeast " + stored;
		return dropAtZero ? "dropped" : "atLeast 0";
	}

	/** What Word reads for a value outside the row's range, in the attribute's own terms. */
	static String reading(Rule rule, BigInteger v) {
		switch (rule.storage) {
		case "int16": return wrap16(v).toString();
		case "uint16": return v.mod(K16).toString();
		case "int32": return wrap32(v).toString();
		case "uint8": return v.mod(BigInteger.valueOf(256)).toString();
		case "uint5": return v.mod(BigInteger.valueOf(32)).toString();
		case "pair-firstLine": {
			BigInteger s = wrap16(v);
			return s.signum() >= 0 ? "firstLine " + s : "hanging " + s.negate();
		}
		case "pair-hanging": {
			BigInteger s = wrap16(v.negate());
			return s.signum() >= 0 ? "firstLine " + s : "hanging " + s.negate();
		}
		case "rule-negated": return rule(wrap16(v.negate()), false);
		case "rule-positive": return rule(wrap16(v), false);
		case "rule-negated-drop": return rule(wrap16(v.negate()), true);
		case "rule-positive-drop": return rule(wrap16(v), true);
		case "bits15-flag": {
			BigInteger u = v.mod(K16);
			boolean flag = u.compareTo(K15) >= 0;
			return "h=" + (flag ? u.subtract(K15) : u) + (flag ? " hRule dropped" : "");
		}
		case "int16-clamp": return wrap16(v).max(rule.lo).min(rule.hi).toString();
		case "uint16-clamp": return v.mod(K16).max(rule.lo).min(rule.hi).toString();
		case "uint16-min": return v.mod(K16).max(rule.lo).toString();
		case "column-w": {
			BigInteger s = wrap16(v);
			return s.signum() < 0 ? "computed" : s.toString();
		}
		case "column-space": {
			BigInteger s = wrap16(v);
			return s.signum() < 0 ? "-1" : s.toString();
		}
		case "twips-unmeasured": return "changed";
		default: throw new IllegalStateException(RESOURCE + ": unknown storage " + rule.storage);
		}
	}
}
