/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.xlsx4j.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An A1 reference to one cell, with its sheet where it names one: {@code Sheet1!$B$2},
 * {@code 'My Sheet'!B2}, {@code B2}.  Columns and rows count from 1.  Immutable.
 *
 * @since 17.3.3 (CR-035)
 */
public final class CellRef {

	private static final Pattern A1 = Pattern.compile("(\\$?)([A-Za-z]{1,3})(\\$?)([0-9]{1,7})");

	private final String sheet;
	private final int column, row;
	private final boolean absoluteColumn, absoluteRow;

	public CellRef(String sheet, int column, int row, boolean absoluteColumn, boolean absoluteRow) {
		if (column < 1 || row < 1) throw new IllegalArgumentException("column " + column + ", row " + row);
		this.sheet = sheet;
		this.column = column;
		this.row = row;
		this.absoluteColumn = absoluteColumn;
		this.absoluteRow = absoluteRow;
	}

	/** An absolute reference ($B$2), as a chart writes them */
	public CellRef(String sheet, int column, int row) {
		this(sheet, column, row, true, true);
	}

	/** {@code Sheet1!$B$2}, {@code 'My Sheet'!B2} or {@code B2}; IllegalArgumentException for anything else */
	public static CellRef parse(String ref) {
		String s = ref.trim(), sheet = null;
		int bang = s.lastIndexOf('!');
		if (bang >= 0) {
			sheet = unquoteSheet(s.substring(0, bang));
			s = s.substring(bang + 1);
		}
		Matcher m = A1.matcher(s);
		if (!m.matches()) throw new IllegalArgumentException("not an A1 cell reference: " + ref);
		return new CellRef(sheet, columnNumber(m.group(2)), Integer.parseInt(m.group(4)), !m.group(1).isEmpty(),
				!m.group(3).isEmpty());
	}

	/** The sheet's name, unquoted; null where the reference names none */
	public String getSheet() {
		return sheet;
	}

	public int getColumn() {
		return column;
	}

	public int getRow() {
		return row;
	}

	public boolean isAbsoluteColumn() {
		return absoluteColumn;
	}

	public boolean isAbsoluteRow() {
		return absoluteRow;
	}

	/** This reference moved by the given columns and rows, its sheet and $ kept */
	public CellRef offset(int columns, int rows) {
		return new CellRef(sheet, column + columns, row + rows, absoluteColumn, absoluteRow);
	}

	/** The cell's name as a cell's {@code r} attribute writes it: B2 */
	public String getCellName() {
		return columnName(column) + row;
	}

	/** The reference without its sheet, with its $ signs: $B$2 */
	String local() {
		return (absoluteColumn ? "$" : "") + columnName(column) + (absoluteRow ? "$" : "") + row;
	}

	/** As a formula writes it: Sheet1!$B$2, 'My Sheet'!$B$2, $B$2 */
	public String formatAsString() {
		return (sheet == null ? "" : quoteSheet(sheet) + "!") + local();
	}

	@Override
	public String toString() {
		return formatAsString();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof CellRef)) return false;
		CellRef c = (CellRef) o;
		return column == c.column && row == c.row && (sheet == null ? c.sheet == null : sheet.equals(c.sheet));
	}

	@Override
	public int hashCode() {
		return 31 * (31 * column + row) + (sheet == null ? 0 : sheet.hashCode());
	}

	// ------------------------------------------------------------------ names

	/** A 1-based column number's letters: 1 is A, 27 is AA */
	public static String columnName(int column) {
		StringBuilder sb = new StringBuilder();
		for (int c = column; c > 0; c = (c - 1) / 26) sb.insert(0, (char) ('A' + (c - 1) % 26));
		return sb.toString();
	}

	/** Column letters' 1-based number: A is 1, AA is 27 */
	public static int columnNumber(String letters) {
		int c = 0;
		for (int i = 0; i < letters.length(); i++) c = c * 26 + (Character.toUpperCase(letters.charAt(i)) - 'A' + 1);
		return c;
	}

	/** {column, row} of a cell name (B2); null for anything else */
	public static int[] cellName(String name) {
		if (name == null) return null;
		Matcher m = A1.matcher(name.trim());
		if (!m.matches()) return null;
		return new int[] { columnNumber(m.group(2)), Integer.parseInt(m.group(4)) };
	}

	/** A sheet's name as a reference writes it: quoted where it has to be, a quote doubled */
	public static String quoteSheet(String name) {
		if (name.matches("[A-Za-z_\\u0080-\\uFFFF][A-Za-z0-9_.\\u0080-\\uFFFF]*")
				&& !name.matches("(?i)[A-Z]{1,3}[0-9]+|R[0-9]*C[0-9]*|R|C|TRUE|FALSE")) {
			return name;
		}
		return "'" + name.replace("'", "''") + "'";
	}

	/** 'My Sheet' as My Sheet, 'It''s' as It's; anything else as it is */
	public static String unquoteSheet(String s) {
		if (s.length() >= 2 && s.startsWith("'") && s.endsWith("'")) return s.substring(1, s.length() - 1).replace("''", "'");
		return s;
	}
}
