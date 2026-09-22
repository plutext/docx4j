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
package org.docx4j.anon;

import java.util.regex.Pattern;

/**
 * Scrubs a spreadsheet formula (a cell's {@code f}, a defined name, a chart's
 * {@code c:f}, a conditional format's or validation's formula, an
 * {@code xm:f}) so that it still computes over the anonymised workbook:
 * <ul>
 * <li>string literals ({@code "..."}) are scrambled;</li>
 * <li>numeric constants have their digits randomised ({@link ScrambleText#number}),
 * unless numbers are kept; a row range ({@code 1:1}) is a reference, not a
 * constant, and stays;</li>
 * <li>sheet names ({@code 'My Sheet'!A1}, {@code Sales!A1}) become the
 * workbook's {@code Sheet<n>} ({@link Names#sheet}); a sheet of an external
 * workbook ({@code '[1]Sheet'!A1}) is scrambled consistently with the external
 * link part;</li>
 * <li>defined names become {@code n_<hash>} ({@link Names#definedName});</li>
 * <li>structured references ({@code Sales[Amount]}, {@code Sales[[#This Row],[Amount]]})
 * keep their keywords and have the table and column names scrambled
 * consistently with the table part and the header cells;</li>
 * <li>cell and range references, function names, operators, the {@code _xlfn.}
 * prefixes, {@code TRUE}/{@code FALSE} and error literals stay.</li>
 * </ul>
 *
 * @since 17.2.1
 */
public class SmlFormulas {

	private final ScrambleText scrambler;
	private final Names names;

	/** A1-style reference: an optional column (with optional $) and row; also column-only and row-only when next to a colon */
	private static final Pattern CELL_REF = Pattern.compile("^\\$?[A-Za-z]{1,3}\\$?[0-9]+$");
	private static final Pattern COLUMN_REF = Pattern.compile("^\\$?[A-Za-z]{1,3}$");
	private static final Pattern R1C1 = Pattern.compile("^([Rr](\\[-?[0-9]+\\]|[0-9]+)?[Cc](\\[-?[0-9]+\\]|[0-9]+)?|[Rr]|[Cc])$");

	public SmlFormulas(ScrambleText scrambler, Names names) {
		this.scrambler = scrambler;
		this.names = names;
	}

	public String scrub(String formula) {
		if (formula == null || formula.isEmpty()) return formula;
		StringBuilder out = new StringBuilder();
		int i = 0, n = formula.length();
		while (i < n) {
			char c = formula.charAt(i);

			if (c == '"') {
				// a string literal, "" escaping a quote
				int j = i + 1;
				StringBuilder lit = new StringBuilder();
				while (j < n) {
					if (formula.charAt(j) == '"') {
						if (j + 1 < n && formula.charAt(j + 1) == '"') {
							lit.append('"');
							j += 2;
							continue;
						}
						break;
					}
					lit.append(formula.charAt(j));
					j++;
				}
				out.append('"').append(scrambler.scramble(lit.toString()).replace("\"", "\"\"")).append('"');
				i = Math.min(j + 1, n);

			} else if (c == '\'') {
				// a quoted sheet name (or '[n]Sheet' of an external book), '' escaping a quote
				int j = i + 1;
				StringBuilder name = new StringBuilder();
				while (j < n) {
					if (formula.charAt(j) == '\'') {
						if (j + 1 < n && formula.charAt(j + 1) == '\'') {
							name.append('\'');
							j += 2;
							continue;
						}
						break;
					}
					name.append(formula.charAt(j));
					j++;
				}
				out.append('\'').append(sheetName(name.toString()).replace("'", "''")).append('\'');
				i = Math.min(j + 1, n);

			} else if (c == '[') {
				// a structured reference's brackets, or an external book number [1] before a sheet
				int j = matchingBracket(formula, i);
				String inner = formula.substring(i + 1, j);
				if (inner.matches("-?[0-9]+")) { // [1] before an external sheet, [-1] of R1C1
					out.append('[').append(inner).append(']');
				} else {
					out.append('[').append(structured(inner)).append(']');
				}
				i = j + 1;

			} else if (c == '#') {
				// an error literal: #REF!, #N/A, #DIV/0!, #NAME?, #GETTING_DATA
				int j = i + 1;
				while (j < n && (Character.isLetterOrDigit(formula.charAt(j)) || "/_!?".indexOf(formula.charAt(j)) >= 0)) j++;
				out.append(formula, i, j);
				i = j;

			} else if (Character.isDigit(c) || (c == '.' && i + 1 < n && Character.isDigit(formula.charAt(i + 1)))) {
				int j = i;
				while (j < n && (Character.isDigit(formula.charAt(j)) || formula.charAt(j) == '.')) j++;
				if (j < n && (formula.charAt(j) == 'E' || formula.charAt(j) == 'e')) {
					int k = j + 1;
					if (k < n && (formula.charAt(k) == '+' || formula.charAt(k) == '-')) k++;
					if (k < n && Character.isDigit(formula.charAt(k))) {
						while (k < n && Character.isDigit(formula.charAt(k))) k++;
						j = k;
					}
				}
				String number = formula.substring(i, j);
				boolean rowRange = (i > 0 && formula.charAt(i - 1) == ':') || (j < n && formula.charAt(j) == ':');
				out.append(rowRange ? number : scrambler.number(number));
				i = j;

			} else if (Character.isLetter(c) || c == '_' || c == '$' || c == '\\') {
				// an identifier: function, cell reference, sheet name, table name, defined name
				int j = i;
				while (j < n && (Character.isLetterOrDigit(formula.charAt(j)) || "_.$\\".indexOf(formula.charAt(j)) >= 0)) j++;
				String ident = formula.substring(i, j);
				int k = j;
				while (k < n && formula.charAt(k) == ' ') k++;
				char next = k < n ? formula.charAt(k) : 0;
				out.append(identifier(ident, next, i > 0 ? formula.charAt(i - 1) : 0));
				i = j;

			} else {
				out.append(c);
				i++;
			}
		}
		return out.toString();
	}

	private String identifier(String ident, char next, char previous) {
		if (next == '(') return ident; // a function
		if (R1C1.matcher(ident).matches()) return ident; // R1C1: R[-1]C, RC[1], R2C3 (Excel reserves R and C, so never a name)
		if (next == '!') return previous == ']' ? scrambler.consistent(ident) : sheetName(ident); // [1]Sheet1! is an external book's sheet
		if (next == '[') return scrambler.consistentIdentifier(ident); // a table's display name, as the table part maps it
		if (CELL_REF.matcher(ident).matches()) return ident;
		if (COLUMN_REF.matcher(ident).matches() && (next == ':' || previous == ':')) return ident;
		String upper = ident.toUpperCase(java.util.Locale.ROOT);
		if (upper.equals("TRUE") || upper.equals("FALSE")) return ident;
		if (ident.startsWith("_xlfn.") || ident.startsWith("_xlws.") || ident.startsWith("_xlpm.")) return ident;
		return Names.definedName(ident);
	}

	/** a sheet name, with an optional [n] external-book prefix */
	private String sheetName(String name) {
		String prefix = "";
		String rest = name;
		if (name.startsWith("[")) {
			int close = name.indexOf(']');
			if (close > 0) {
				prefix = name.substring(0, close + 1);
				rest = name.substring(close + 1);
			}
		}
		if (prefix.isEmpty() && names.isSheet(rest)) return names.sheet(rest);
		if (prefix.isEmpty()) return scrambler.consistent(rest); // a sheet the workbook does not list
		// an external workbook's sheet: the same mapping as its external link part's sheetNames
		return prefix + scrambler.consistent(rest);
	}

	/** the inside of a structured reference's brackets: keywords kept, column names scrambled consistently */
	private String structured(String inner) {
		String trimmed = inner.trim();
		if (trimmed.isEmpty()) return inner;
		if (trimmed.startsWith("#")) return inner; // [#Headers], [#This Row], [#All], [#Data], [#Totals]
		if (trimmed.startsWith("@")) return "@" + structured(trimmed.substring(1));
		if (trimmed.startsWith("[")) {
			// [[#This Row],[Amount]] or [[Col A]:[Col B]]
			StringBuilder sb = new StringBuilder();
			int i = 0;
			while (i < trimmed.length()) {
				char c = trimmed.charAt(i);
				if (c == '[') {
					int j = matchingBracket(trimmed, i);
					sb.append('[').append(structured(trimmed.substring(i + 1, j))).append(']');
					i = j + 1;
				} else {
					sb.append(c);
					i++;
				}
			}
			return sb.toString();
		}
		return scrambler.consistent(inner);
	}

	private static int matchingBracket(String s, int open) {
		int depth = 0;
		for (int i = open; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\'' && i + 1 < s.length()) {
				i++; // an escaped special character inside a structured reference
				continue;
			}
			if (c == '[') depth++;
			if (c == ']') {
				depth--;
				if (depth == 0) return i;
			}
		}
		return s.length() - 1;
	}

}
