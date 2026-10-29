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
package org.docx4j.model.fields;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * IF and COMPARE: {@code expr op expr}, as Word 365 reads them (CR-067 phase 2, measured 2026-10-28, section 13.2).
 * An operand in quotes is a string, a number in it included ({@code "10" > "9"} is false). A bare operand that is a
 * number (the formula's number forms: {@code 5.0}, {@code 007}, {@code 1,000}, {@code -1}) is a number; a bare operand
 * naming a bookmark is the bookmark's text (then a number if it is one); any other bare operand is itself as a
 * string. Two numbers compare as numbers; anything else as strings, case-sensitive, with {@code *} and {@code ?} in
 * the right operand for {@code =} and {@code <>} only. The operators are {@code = <> < <= > >=}, spaces around them
 * ({@code 5=5} is one token): anything else gives Word's {@code Error! Unknown op code for conditional.}. IF's result
 * is its true or false text (bare or quoted; a missing false text is empty); COMPARE's is 1 or 0. The field's
 * {@code \*} switch applies to the result afterwards.
 *
 * @since 17.3.5
 */
final class Conditional {

	static final String UNKNOWN_OP = "Error! Unknown op code for conditional.";

	private Conditional() {
	}

	/** COMPARE: 1 or 0, the error text, or null where the field has too few operands (the cached result is kept). */
	static String compare(FieldInstruction instruction, FieldContext context) {
		Boolean r = test(instruction, context);
		if (r == null) return instruction.getParameters().size() >= 3 ? UNKNOWN_OP : null;
		return r ? "1" : "0";
	}

	/** IF: the true or false text, the error text, or null where the field has too few operands. */
	static String ifField(FieldInstruction instruction, FieldContext context) {
		List<String> p = instruction.getParameters();
		Boolean r = test(instruction, context);
		if (r == null) return p.size() >= 3 ? UNKNOWN_OP : null;
		int index = r ? 3 : 4;
		if (index >= p.size() || p.get(index).startsWith("\\")) return "";
		return FieldInstruction.unquote(p.get(index));
	}

	/** The comparison, or null where there is no operator to apply (too few operands, or not an operator). */
	static Boolean test(FieldInstruction instruction, FieldContext context) {
		List<String> p = instruction.getParameters();
		if (p.size() < 3) return null;
		String op = p.get(1);
		if (!isOperator(op)) return null;
		Formula.Symbols symbols = context.numberSymbols();
		Object left = operand(p.get(0), context, symbols);
		Object right = operand(p.get(2), context, symbols);
		if (left instanceof Double && right instanceof Double) {
			double a = (Double) left, b = (Double) right;
			switch (op) {
			case "=": return a == b;
			case "<>": return a != b;
			case "<": return a < b;
			case "<=": return a <= b;
			case ">": return a > b;
			default: return a >= b;
			}
		}
		String a = left.toString(), b = right.toString();
		switch (op) {
		case "=": return matches(a, b);
		case "<>": return !matches(a, b);
		case "<": return a.compareTo(b) < 0;
		case "<=": return a.compareTo(b) <= 0;
		case ">": return a.compareTo(b) > 0;
		default: return a.compareTo(b) >= 0;
		}
	}

	static boolean isOperator(String op) {
		return "=".equals(op) || "<>".equals(op) || "<".equals(op) || "<=".equals(op) || ">".equals(op) || ">=".equals(op);
	}

	/** A Double, or a String. */
	static Object operand(String token, FieldContext context, Formula.Symbols symbols) {
		if (token.startsWith("\"")) return FieldInstruction.unquote(token);
		Double n = Formula.number(token, symbols);
		if (n != null) return n;
		String bookmark = context.bookmarks().get(token.toLowerCase(Locale.ROOT));
		if (bookmark != null) {
			Double v = Formula.number(bookmark, symbols);
			return v != null ? v : bookmark;
		}
		return token;
	}

	/** The right operand's {@code *} (any characters) and {@code ?} (one character); the rest literal, case-sensitive. */
	static boolean matches(String value, String pattern) {
		if (pattern.indexOf('*') < 0 && pattern.indexOf('?') < 0) return value.equals(pattern);
		StringBuilder re = new StringBuilder();
		for (char c : pattern.toCharArray()) {
			if (c == '*') re.append(".*");
			else if (c == '?') re.append('.');
			else re.append(Pattern.quote(String.valueOf(c)));
		}
		return Pattern.compile(re.toString(), Pattern.DOTALL).matcher(value).matches();
	}
}
