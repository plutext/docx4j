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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The {@code =} field: Word's formula language, as Word 365 evaluates it (CR-067 phase 2, measured 2026-10-28,
 * section 13.2). Numbers (the document's decimal symbol and list separator, {@code w:settings}, else {@code .} and
 * {@code ,}; a thousands separator accepted, and echoed in the result), the operators {@code + - * / ^ %} and the
 * comparisons {@code = <> < <= > >=} (1 or 0), parentheses, the functions ABS, AND, AVERAGE, COUNT, DEFINED, FALSE,
 * IF, INT, MAX, MIN, MOD, NOT, OR, PRODUCT, ROUND, SIGN, SUM and TRUE, a bookmark's text as a number, and in a table
 * the cell references {@code A1}, {@code A1:B2}, ABOVE, LEFT, BELOW and RIGHT (the context's {@link FieldContext.Cells};
 * without them a formula naming a cell keeps its cached result). The result in Word's default format (a whole number
 * plain; else two decimals, trailing zeros dropped), or through the field's {@code \#} switch from the unrounded
 * value, then its {@code \*} switch. Word's error texts as measured: {@code !Zero Divide},
 * {@code !Unexpected End of Formula}, {@code !Missing Operator}, {@code !Syntax Error, x},
 * {@code !Undefined Bookmark, NAME}.
 *
 * <p>Measured as Word does it: INT truncates toward zero (INT(-3.7) is -3); ROUND rounds half away from zero;
 * MOD takes the dividend's sign (MOD(-7,3) is -1); COUNT counts the numbers only; a text or empty cell in a range is
 * skipped; {@code =1,000+1} is 1,001 and {@code =1,5+1} a syntax error at the comma (en-AU: the comma is the list
 * separator). Unmeasured (section 9): a non-integer exponent (Word showed 1.4 for {@code =2^0.5}, this gives 1.41),
 * unary minus against {@code ^}, a function given the wrong number of arguments, a bookmark whose text is not a
 * number (read as 0 here), whether an empty cell ends ABOVE or LEFT (skipped here).</p>
 *
 * @since 17.3.5
 */
public final class Formula {

	/** The symbols a formula's numbers use. */
	public static final class Symbols {
		public final char decimal, grouping, list;
		public Symbols(char decimal, char grouping, char list) {
			this.decimal = decimal;
			this.grouping = grouping;
			this.list = list;
		}
		public static final Symbols DEFAULT = new Symbols('.', ',', ',');
	}

	/** An error Word prints as the field's result. */
	static final class FormulaError extends Exception {
		private static final long serialVersionUID = 1L;
		FormulaError(String text) {
			super(text, null, false, false);
		}
	}

	/** A cell reference where the context has no cells: the field keeps its cached result. */
	static final class NoCells extends Exception {
		private static final long serialVersionUID = 1L;
		NoCells() {
			super(null, null, false, false);
		}
	}

	private Formula() {
	}

	/**
	 * The field's result text, or null where it keeps its cached result (a cell reference with no cells in the
	 * context).
	 */
	public static String evaluate(FieldInstruction instruction, FieldContext context) throws FieldFormattingException {
		Symbols symbols = context.numberSymbols();
		String expression = expressionOf(instruction);
		Result r;
		try {
			r = new Parser(expression, symbols, context).parseAll();
		} catch (FormulaError e) {
			return e.getMessage();
		} catch (NoCells e) {
			return null;
		}
		String value;
		String pattern = instruction.getSwitchValue("\\#");
		if (pattern != null && !pattern.isEmpty()) {
			value = FormattingSwitchHelper.formatNumber(instruction, pattern, r.value, context.getLanguage());
		} else {
			value = format(r.value, r.grouping, symbols);
		}
		return FormattingSwitchHelper.applyGeneralSwitch(instruction, value);
	}

	/** The expression: the instruction's text after {@code =}, up to its first switch. */
	static String expressionOf(FieldInstruction instruction) {
		String text = instruction.getParameterText();
		if (text == null) return "";
		boolean quoted = false;
		for (int i = 0; i < text.length() - 1; i++) {
			char c = text.charAt(i);
			if (c == '"') quoted = !quoted;
			else if (c == '\\' && !quoted && "#*@!".indexOf(text.charAt(i + 1)) >= 0) return text.substring(0, i).trim();
		}
		return text.trim();
	}

	/** Word's default format: a whole number plain; else to two decimals, trailing zeros dropped. */
	static String format(double value, boolean grouping, Symbols symbols) {
		BigDecimal bd = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
		if (bd.scale() < 0) bd = bd.setScale(0);
		if (bd.signum() == 0) bd = BigDecimal.ZERO;
		String plain = bd.toPlainString();
		String sign = plain.startsWith("-") ? "-" : "";
		if (!sign.isEmpty()) plain = plain.substring(1);
		int dot = plain.indexOf('.');
		String integer = dot < 0 ? plain : plain.substring(0, dot);
		String fraction = dot < 0 ? "" : plain.substring(dot + 1);
		StringBuilder sb = new StringBuilder(sign);
		if (grouping) {
			int first = integer.length() % 3;
			for (int i = 0; i < integer.length(); i++) {
				if (i > 0 && (i - first) % 3 == 0) sb.append(symbols.grouping);
				sb.append(integer.charAt(i));
			}
		} else {
			sb.append(integer);
		}
		if (!fraction.isEmpty()) sb.append(symbols.decimal).append(fraction);
		return sb.toString();
	}

	/**
	 * The number a text holds ({@code 5}, {@code -1.5}, {@code 007}, {@code 1,000} with the grouping separator in
	 * groups of three), or null where it is not one. Leading and trailing spaces ignored.
	 */
	public static Double number(String text, Symbols symbols) {
		if (text == null) return null;
		String t = text.trim();
		if (t.isEmpty()) return null;
		int i = 0;
		boolean negative = false;
		if (t.charAt(0) == '-' || t.charAt(0) == '+') {
			negative = t.charAt(0) == '-';
			i = 1;
		}
		int[] end = new int[1];
		boolean[] grouped = new boolean[1];
		Double v = scanNumber(t, i, symbols, end, grouped);
		if (v == null || end[0] != t.length()) return null;
		return negative ? -v : v;
	}

	/**
	 * Scans a number at {@code from}: digits with optional groups of three after the grouping separator, an optional
	 * fraction after the decimal symbol. {@code end[0]} receives the index after it; {@code grouped[0]} whether a
	 * grouping separator was used. Null where no digit starts there.
	 */
	static Double scanNumber(String s, int from, Symbols symbols, int[] end, boolean[] grouped) {
		int i = from, n = s.length();
		StringBuilder digits = new StringBuilder();
		boolean any = false;
		if (i < n && Character.isDigit(s.charAt(i))) {
			while (i < n && Character.isDigit(s.charAt(i))) digits.append(s.charAt(i++));
			any = true;
			// groups of exactly three digits after the grouping separator
			while (i + 3 < n && s.charAt(i) == symbols.grouping
					&& Character.isDigit(s.charAt(i + 1)) && Character.isDigit(s.charAt(i + 2))
					&& Character.isDigit(s.charAt(i + 3)) && (i + 4 >= n || !Character.isDigit(s.charAt(i + 4)))) {
				digits.append(s, i + 1, i + 4);
				i += 4;
				grouped[0] = true;
			}
		}
		if (i < n && s.charAt(i) == symbols.decimal && i + 1 < n && Character.isDigit(s.charAt(i + 1))) {
			digits.append('.');
			i++;
			while (i < n && Character.isDigit(s.charAt(i))) digits.append(s.charAt(i++));
			any = true;
		}
		if (!any) return null;
		end[0] = i;
		return Double.parseDouble(digits.toString());
	}

	static final class Result {
		final double value;
		final boolean grouping;
		Result(double value, boolean grouping) {
			this.value = value;
			this.grouping = grouping;
		}
	}

	/** A recursive-descent parser and evaluator in one pass. */
	static final class Parser {
		private final String s;
		private final Symbols symbols;
		private final FieldContext context;
		private int pos;
		private boolean grouping;

		Parser(String expression, Symbols symbols, FieldContext context) {
			this.s = expression;
			this.symbols = symbols;
			this.context = context;
		}

		Result parseAll() throws FormulaError, NoCells {
			skipSpaces();
			if (pos >= s.length()) throw new FormulaError("!Unexpected End of Formula");
			double v = comparison();
			skipSpaces();
			if (pos < s.length()) {
				if (startsOperand()) throw new FormulaError("!Missing Operator");
				throw new FormulaError("!Syntax Error, " + token());
			}
			return new Result(v, grouping);
		}

		private void skipSpaces() {
			while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
		}

		private boolean startsOperand() {
			if (pos >= s.length()) return false;
			char c = s.charAt(pos);
			return Character.isDigit(c) || Character.isLetter(c) || c == '_' || c == '(' || c == symbols.decimal;
		}

		/** The token at pos, for an error text. */
		private String token() {
			int i = pos;
			char c = s.charAt(i);
			if (Character.isLetterOrDigit(c) || c == '_') {
				while (i < s.length() && (Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i) == '_')) i++;
			} else {
				i++;
			}
			return s.substring(pos, i);
		}

		private boolean at(String op) {
			return s.startsWith(op, pos);
		}

		private double comparison() throws FormulaError, NoCells {
			double left = additive();
			while (true) {
				skipSpaces();
				String op = null;
				for (String candidate : new String[] { "<>", "<=", ">=", "=", "<", ">" }) {
					if (at(candidate)) {
						op = candidate;
						break;
					}
				}
				if (op == null) return left;
				pos += op.length();
				double right = additive();
				boolean r;
				switch (op) {
				case "=": r = left == right; break;
				case "<>": r = left != right; break;
				case "<": r = left < right; break;
				case "<=": r = left <= right; break;
				case ">": r = left > right; break;
				default: r = left >= right; break;
				}
				left = r ? 1 : 0;
			}
		}

		private double additive() throws FormulaError, NoCells {
			double left = multiplicative();
			while (true) {
				skipSpaces();
				if (at("+")) {
					pos++;
					left += multiplicative();
				} else if (at("-")) {
					pos++;
					left -= multiplicative();
				} else {
					return left;
				}
			}
		}

		private double multiplicative() throws FormulaError, NoCells {
			double left = power();
			while (true) {
				skipSpaces();
				if (at("*")) {
					pos++;
					left *= power();
				} else if (at("/")) {
					pos++;
					double right = power();
					if (right == 0) throw new FormulaError("!Zero Divide");
					left /= right;
				} else {
					return left;
				}
			}
		}

		private double power() throws FormulaError, NoCells {
			double left = unary();
			while (true) {
				skipSpaces();
				if (at("^")) {
					pos++;
					left = Math.pow(left, unary());
				} else {
					return left;
				}
			}
		}

		private double unary() throws FormulaError, NoCells {
			skipSpaces();
			if (at("-")) {
				pos++;
				return -unary();
			}
			if (at("+")) {
				pos++;
				return unary();
			}
			return postfix();
		}

		private double postfix() throws FormulaError, NoCells {
			double v = primary();
			while (true) {
				skipSpaces();
				if (at("%")) {
					pos++;
					v /= 100;
				} else {
					return v;
				}
			}
		}

		private double primary() throws FormulaError, NoCells {
			skipSpaces();
			if (pos >= s.length()) throw new FormulaError("!Unexpected End of Formula");
			char c = s.charAt(pos);
			if (c == '(') {
				pos++;
				double v = comparison();
				skipSpaces();
				expect(')');
				return v;
			}
			if (Character.isDigit(c) || c == symbols.decimal) {
				int[] end = new int[1];
				boolean[] grouped = new boolean[1];
				Double v = scanNumber(s, pos, symbols, end, grouped);
				if (v == null) throw new FormulaError("!Syntax Error, " + token());
				pos = end[0];
				if (grouped[0]) grouping = true;
				return v;
			}
			if (Character.isLetter(c) || c == '_') {
				String name = token();
				pos += name.length();
				skipSpaces();
				if (pos < s.length() && s.charAt(pos) == '(') {
					pos++;
					return function(name);
				}
				if ("TRUE".equalsIgnoreCase(name)) return 1; // =TRUE and =FALSE need no parentheses (measured)
				if ("FALSE".equalsIgnoreCase(name)) return 0;
				return reference(name);
			}
			throw new FormulaError("!Syntax Error, " + token());
		}

		private void expect(char c) throws FormulaError {
			skipSpaces();
			if (pos >= s.length()) throw new FormulaError("!Unexpected End of Formula");
			if (s.charAt(pos) != c) throw new FormulaError("!Syntax Error, " + token());
			pos++;
		}

		/** A bare name: a cell reference in a table, else a bookmark's text as a number. */
		private double reference(String name) throws FormulaError, NoCells {
			int[] cell = cellOf(name);
			if (cell != null) {
				FieldContext.Cells cells = context.getCells();
				if (cells == null) throw new NoCells();
				Double v = cells.value(cell[0], cell[1]);
				return v == null ? 0 : v;
			}
			String text = context.bookmarks().get(name.toLowerCase(Locale.ROOT));
			if (text == null) throw new FormulaError("!Undefined Bookmark, " + name.toUpperCase(Locale.ROOT));
			Double v = number(text, symbols);
			return v == null ? 0 : v;
		}

		/** {@code A1} as {row, col} (from 0), or null where the name is not a cell reference. */
		static int[] cellOf(String name) {
			int i = 0;
			while (i < name.length() && Character.isLetter(name.charAt(i))) i++;
			if (i == 0 || i > 2 || i == name.length()) return null;
			for (int j = i; j < name.length(); j++) if (!Character.isDigit(name.charAt(j))) return null;
			int col = 0;
			for (int j = 0; j < i; j++) col = col * 26 + (Character.toUpperCase(name.charAt(j)) - 'A' + 1);
			int row = Integer.parseInt(name.substring(i));
			if (row < 1) return null;
			return new int[] { row - 1, col - 1 };
		}

		/** The function's arguments, each a number or (for a range or direction) several; then its value. */
		private double function(String name) throws FormulaError, NoCells {
			String upper = name.toUpperCase(Locale.ROOT);
			List<Double> args = new ArrayList<Double>();
			List<String> names = new ArrayList<String>();
			skipSpaces();
			if (!"DEFINED".equals(upper) && !FUNCTIONS.contains(upper)) {
				throw new FormulaError("!Syntax Error, " + (pos < s.length() && s.charAt(pos) != ')' ? token() : ")"));
			}
			if (pos < s.length() && s.charAt(pos) == ')') {
				pos++;
			} else {
				while (true) {
					skipSpaces();
					if ("DEFINED".equals(upper)) {
						names.add(token());
						pos += names.get(names.size() - 1).length();
					} else if (!rangeArgument(args)) {
						args.add(comparison());
					}
					skipSpaces();
					if (pos >= s.length()) throw new FormulaError("!Unexpected End of Formula");
					char c = s.charAt(pos);
					if (c == symbols.list) {
						pos++;
						continue;
					}
					expect(')');
					break;
				}
			}
			return apply(upper, args, names);
		}

		/** A direction (ABOVE, LEFT, BELOW, RIGHT) or a range {@code A1:B2}: the numbers of its cells, text and empty cells skipped. */
		private boolean rangeArgument(List<Double> args) throws FormulaError, NoCells {
			int start = pos;
			if (pos >= s.length() || !(Character.isLetter(s.charAt(pos)))) return false;
			String name = token();
			String upper = name.toUpperCase(Locale.ROOT);
			int[] from = cellOf(name);
			boolean direction = DIRECTIONS.contains(upper);
			int after = pos + name.length();
			int[] to = null;
			if (from != null) {
				int j = after;
				while (j < s.length() && Character.isWhitespace(s.charAt(j))) j++;
				if (j < s.length() && s.charAt(j) == ':') {
					j++;
					while (j < s.length() && Character.isWhitespace(s.charAt(j))) j++;
					int k = j;
					while (k < s.length() && (Character.isLetterOrDigit(s.charAt(k)))) k++;
					to = cellOf(s.substring(j, k));
					if (to == null) throw new FormulaError("!Syntax Error, " + s.substring(j, Math.max(j + 1, k)));
					after = k;
				}
			}
			if (!direction && to == null) {
				pos = start;
				return false;
			}
			FieldContext.Cells cells = context.getCells();
			if (cells == null) throw new NoCells();
			pos = after;
			if (direction) {
				int row = cells.row(), col = cells.col();
				switch (upper) {
				case "ABOVE": for (int r = row - 1; r >= 0; r--) add(args, cells.value(r, col)); break;
				case "BELOW": for (int r = row + 1; r < cells.rowCount(); r++) add(args, cells.value(r, col)); break;
				case "LEFT": for (int c = col - 1; c >= 0; c--) add(args, cells.value(row, c)); break;
				default: for (int c = col + 1; c < cells.colCount(row); c++) add(args, cells.value(row, c)); break;
				}
			} else {
				for (int r = Math.min(from[0], to[0]); r <= Math.max(from[0], to[0]); r++) {
					for (int c = Math.min(from[1], to[1]); c <= Math.max(from[1], to[1]); c++) add(args, cells.value(r, c));
				}
			}
			return true;
		}

		private static void add(List<Double> args, Double v) {
			if (v != null) args.add(v);
		}

		private double apply(String f, List<Double> a, List<String> names) throws FormulaError {
			switch (f) {
			case "ABS": return Math.abs(one(a));
			case "SIGN": return Math.signum(one(a));
			case "INT": return one(a) < 0 ? Math.ceil(one(a)) : Math.floor(one(a)); // toward zero, as measured
			case "NOT": return one(a) == 0 ? 1 : 0;
			case "TRUE": return 1;
			case "FALSE": return 0;
			case "DEFINED": {
				if (names.size() != 1) throw new FormulaError("!Syntax Error, )");
				String name = names.get(0);
				return context.bookmarks().containsKey(name.toLowerCase(Locale.ROOT)) ? 1 : 0;
			}
			case "AND": { if (a.isEmpty()) throw new FormulaError("!Syntax Error, )"); for (double v : a) if (v == 0) return 0; return 1; }
			case "OR": { if (a.isEmpty()) throw new FormulaError("!Syntax Error, )"); for (double v : a) if (v != 0) return 1; return 0; }
			case "SUM": { double t = 0; for (double v : a) t += v; return t; }
			case "PRODUCT": { double t = 1; for (double v : a) t *= v; return t; }
			case "COUNT": return a.size();
			case "AVERAGE": { if (a.isEmpty()) throw new FormulaError("!Zero Divide"); double t = 0; for (double v : a) t += v; return t / a.size(); }
			case "MAX": { if (a.isEmpty()) throw new FormulaError("!Syntax Error, )"); double m = a.get(0); for (double v : a) m = Math.max(m, v); return m; }
			case "MIN": { if (a.isEmpty()) throw new FormulaError("!Syntax Error, )"); double m = a.get(0); for (double v : a) m = Math.min(m, v); return m; }
			case "MOD": {
				if (a.size() != 2) throw new FormulaError("!Syntax Error, )");
				if (a.get(1) == 0) throw new FormulaError("!Zero Divide");
				return a.get(0) % a.get(1); // the dividend's sign, as measured
			}
			case "ROUND": {
				if (a.size() != 2) throw new FormulaError("!Syntax Error, )");
				int digits = (int) Math.round(a.get(1));
				return BigDecimal.valueOf(a.get(0)).setScale(digits, RoundingMode.HALF_UP).doubleValue(); // half away from zero, as measured
			}
			case "IF": {
				if (a.size() != 3) throw new FormulaError("!Syntax Error, )");
				return a.get(0) != 0 ? a.get(1) : a.get(2);
			}
			default: throw new FormulaError("!Syntax Error, " + f);
			}
		}

		private static double one(List<Double> a) throws FormulaError {
			if (a.size() != 1) throw new FormulaError("!Syntax Error, )");
			return a.get(0);
		}
	}

	static final java.util.Set<String> FUNCTIONS = new java.util.HashSet<String>(java.util.Arrays.asList(
			"ABS", "AND", "AVERAGE", "COUNT", "DEFINED", "FALSE", "IF", "INT", "MAX", "MIN", "MOD", "NOT", "OR",
			"PRODUCT", "ROUND", "SIGN", "SUM", "TRUE"));
	static final java.util.Set<String> DIRECTIONS = new java.util.HashSet<String>(java.util.Arrays.asList(
			"ABOVE", "BELOW", "LEFT", "RIGHT"));
}
