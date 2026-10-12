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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An A1 reference to a rectangle of cells on one sheet: {@code Sheet1!$A$2:$A$5}, or one cell
 * ({@code Sheet1!$B$1}); and the unions a chart's reference can be
 * ({@code (Sheet1!$A$2:$A$3,Sheet1!$A$5)}).  Immutable; its first cell is the top left.
 *
 * @since 17.3.3 (CR-035)
 */
public final class AreaRef {

	private final CellRef first, last;

	/** The rectangle between two cells of one sheet, whichever corners they are */
	public AreaRef(CellRef a, CellRef b) {
		String sheet = a.getSheet() != null ? a.getSheet() : b.getSheet();
		this.first = new CellRef(sheet, Math.min(a.getColumn(), b.getColumn()), Math.min(a.getRow(), b.getRow()),
				a.isAbsoluteColumn(), a.isAbsoluteRow());
		this.last = new CellRef(sheet, Math.max(a.getColumn(), b.getColumn()), Math.max(a.getRow(), b.getRow()),
				b.isAbsoluteColumn(), b.isAbsoluteRow());
	}

	/** {@code Sheet1!$A$2:$A$5}, {@code 'My Sheet'!A1:D5} or one cell; IllegalArgumentException for anything else */
	public static AreaRef parse(String ref) {
		String s = ref.trim(), sheet = null;
		int bang = s.lastIndexOf('!');
		if (bang >= 0) {
			sheet = CellRef.unquoteSheet(s.substring(0, bang));
			s = s.substring(bang + 1);
		}
		String[] ends = s.split(":");
		if (ends.length > 2) throw new IllegalArgumentException("not an area reference: " + ref);
		CellRef a = CellRef.parse(ends[0]), b = ends.length == 2 ? CellRef.parse(ends[1]) : a;
		return new AreaRef(withSheet(a, sheet), withSheet(b, sheet));
	}

	private static CellRef withSheet(CellRef c, String sheet) {
		return new CellRef(sheet, c.getColumn(), c.getRow(), c.isAbsoluteColumn(), c.isAbsoluteRow());
	}

	/**
	 * The areas of a reference, a union's one after another: {@code (Sheet1!$A$2:$A$3,Sheet1!$A$5)}
	 * as two; a quoted sheet name may hold a comma
	 */
	public static List<AreaRef> parseUnion(String ref) {
		String r = ref.trim();
		if (r.startsWith("(") && r.endsWith(")")) r = r.substring(1, r.length() - 1);
		List<AreaRef> out = new ArrayList<AreaRef>();
		StringBuilder sb = new StringBuilder();
		boolean quoted = false;
		for (int i = 0; i < r.length(); i++) {
			char ch = r.charAt(i);
			if (ch == '\'') quoted = !quoted;
			if (ch == ',' && !quoted) {
				out.add(parse(sb.toString()));
				sb.setLength(0);
			} else {
				sb.append(ch);
			}
		}
		if (sb.length() > 0) out.add(parse(sb.toString()));
		return Collections.unmodifiableList(out);
	}

	/** Areas as a chart's reference writes them: one as it is, several as a parenthesised union */
	public static String formatUnion(List<AreaRef> areas) {
		if (areas.size() == 1) return areas.get(0).formatAsString();
		StringBuilder sb = new StringBuilder("(");
		for (int i = 0; i < areas.size(); i++) sb.append(i == 0 ? "" : ",").append(areas.get(i).formatAsString());
		return sb.append(')').toString();
	}

	public String getSheet() {
		return first.getSheet();
	}

	/** The top left cell */
	public CellRef getFirst() {
		return first;
	}

	/** The bottom right cell */
	public CellRef getLast() {
		return last;
	}

	public int getColumnCount() {
		return last.getColumn() - first.getColumn() + 1;
	}

	public int getRowCount() {
		return last.getRow() - first.getRow() + 1;
	}

	public boolean isSingleCell() {
		return first.getColumn() == last.getColumn() && first.getRow() == last.getRow();
	}

	public boolean contains(int column, int row) {
		return column >= first.getColumn() && column <= last.getColumn() && row >= first.getRow() && row <= last.getRow();
	}

	/** Its cells row by row, as a chart takes them */
	public List<CellRef> getCells() {
		List<CellRef> cells = new ArrayList<CellRef>();
		for (int r = first.getRow(); r <= last.getRow(); r++) {
			for (int c = first.getColumn(); c <= last.getColumn(); c++) cells.add(new CellRef(getSheet(), c, r));
		}
		return cells;
	}

	/** This area with its corners moved, the $ signs kept */
	public AreaRef resize(int firstColumns, int firstRows, int lastColumns, int lastRows) {
		return new AreaRef(first.offset(firstColumns, firstRows), last.offset(lastColumns, lastRows));
	}

	/** As a formula writes it: Sheet1!$A$2:$A$5, or Sheet1!$B$1 for one cell */
	public String formatAsString() {
		String sheet = getSheet() == null ? "" : CellRef.quoteSheet(getSheet()) + "!";
		return sheet + first.local() + (isSingleCell() ? "" : ":" + last.local());
	}

	/** Without the sheet or $ signs, as a table's {@code ref} writes it: A1:D5 */
	public String formatLocal() {
		return first.getCellName() + (isSingleCell() ? "" : ":" + last.getCellName());
	}

	@Override
	public String toString() {
		return formatAsString();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof AreaRef && first.equals(((AreaRef) o).first) && last.equals(((AreaRef) o).last);
	}

	@Override
	public int hashCode() {
		return 31 * first.hashCode() + last.hashCode();
	}
}
