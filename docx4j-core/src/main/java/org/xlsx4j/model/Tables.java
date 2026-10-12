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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.TablePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.relationships.Relationship;
import org.xlsx4j.sml.CTAutoFilter;
import org.xlsx4j.sml.CTTable;
import org.xlsx4j.sml.CTTableColumn;
import org.xlsx4j.sml.CTTableColumns;
import org.xlsx4j.sml.CTTablePart;
import org.xlsx4j.sml.CTTableParts;
import org.xlsx4j.sml.CTTableStyleInfo;
import org.xlsx4j.sml.Worksheet;

/**
 * A worksheet's tables, grown and shrunk as Excel grows and shrinks them (CR-035 phase 4,
 * measured on Excel's own edits): the table's {@code ref} and its {@code autoFilter}'s; a column
 * added at the right with an {@code id} one above the largest, a fresh {@code xr3:uid} and its
 * header cell's text as its {@code name}; a column removed with its entry; every column's name
 * following its header cell, as Excel's does.  A table with a totals row, or without a header
 * row, isn't resized: those growths are unmeasured, so {@link #canResize} says no.
 *
 * @since 17.3.3 (CR-035)
 */
public final class Tables {

	private Tables() {
	}

	/** The sheet's tables */
	public static List<TablePart> of(WorksheetPart sheet) {
		List<TablePart> out = new ArrayList<TablePart>();
		if (sheet.getRelationshipsPart() == null) return out;
		for (Relationship r : sheet.getRelationshipsPart().getRelationships().getRelationship()) {
			Part p = sheet.getRelationshipsPart().getPart(r);
			if (p instanceof TablePart) out.add((TablePart) p);
		}
		return out;
	}

	/** The table of the sheet whose range holds the whole area; null for none */
	public static TablePart containing(WorksheetPart sheet, AreaRef area) throws Docx4JException {
		for (TablePart t : of(sheet)) {
			AreaRef ref = getRef(t);
			if (ref.contains(area.getFirst().getColumn(), area.getFirst().getRow())
					&& ref.contains(area.getLast().getColumn(), area.getLast().getRow())) {
				return t;
			}
		}
		return null;
	}

	public static AreaRef getRef(TablePart table) throws Docx4JException {
		return AreaRef.parse(table.getContents().getRef());
	}

	/**
	 * A table made over the area, as Excel's <i>Format as Table</i> makes one (CR-036 phase 2): its
	 * first row the header row, each column named from its header cell as {@link #nameColumns}
	 * names them; its {@code id} one above the workbook's tables', named Table and the lowest
	 * number no table of the workbook has, its part {@code /xl/tables/tableN.xml} after the
	 * package's; Excel's default style (TableStyleMedium2, row stripes); its {@code xr:uid}s from
	 * {@code ids}.  Refused over a table it would overlap.
	 */
	public static TablePart create(SheetCells cells, AreaRef ref, Random ids) throws Docx4JException {
		WorksheetPart sheet = cells.getPart();
		long id = 0;
		Set<String> names = new HashSet<String>();
		for (WorksheetPart w : SheetCells.worksheets(cells.getWorkbook())) {
			for (TablePart t : of(w)) {
				CTTable ct = t.getContents();
				id = Math.max(id, ct.getId());
				if (ct.getName() != null) names.add(ct.getName().toLowerCase(Locale.ROOT));
				if (ct.getDisplayName() != null) names.add(ct.getDisplayName().toLowerCase(Locale.ROOT));
				if (w == sheet && overlap(getRef(t), ref)) {
					throw new IllegalStateException("a table can't overlap another (" + ct.getName() + ", " + ct.getRef() + ")");
				}
			}
		}
		id++;
		int n = 1;
		while (names.contains(("Table" + n).toLowerCase(Locale.ROOT))) n++;
		String name = "Table" + n;
		CTTable t = new CTTable();
		t.setId(id);
		t.setUid(guid(ids));
		t.setName(name);
		t.setDisplayName(name);
		t.setRef(ref.formatLocal());
		t.setTotalsRowShown(false);
		t.setIgnorable("xr xr3");
		CTAutoFilter af = new CTAutoFilter();
		af.setRef(ref.formatLocal());
		af.setUid(t.getUid());
		af.setParent(t);
		t.setAutoFilter(af);
		CTTableColumns cols = new CTTableColumns();
		cols.setParent(t);
		for (int i = 0; i < ref.getColumnCount(); i++) {
			CTTableColumn c = new CTTableColumn();
			c.setId(i + 1);
			c.setUid(guid(ids));
			c.setName("");
			c.setParent(cols);
			cols.getTableColumn().add(c);
		}
		cols.setCount((long) ref.getColumnCount());
		t.setTableColumns(cols);
		CTTableStyleInfo style = new CTTableStyleInfo();
		style.setName("TableStyleMedium2");
		style.setShowFirstColumn(false);
		style.setShowLastColumn(false);
		style.setShowRowStripes(true);
		style.setShowColumnStripes(false);
		style.setParent(t);
		t.setTableStyleInfo(style);

		TablePart part;
		try {
			int i = 1;
			while (cells.getWorkbook().getParts().get(new PartName("/xl/tables/table" + i + ".xml")) != null) i++;
			part = new TablePart(new PartName("/xl/tables/table" + i + ".xml"));
		} catch (InvalidFormatException e) {
			throw new Docx4JException(e.getMessage(), e);
		}
		part.setJaxbElement(t);
		Relationship r = sheet.addTargetPart(part);
		Worksheet ws = sheet.getContents();
		CTTableParts tps = ws.getTableParts();
		if (tps == null) {
			tps = new CTTableParts();
			tps.setParent(ws);
			ws.setTableParts(tps);
		}
		CTTablePart tp = new CTTablePart();
		tp.setId(r.getId());
		tp.setParent(tps);
		tps.getTablePart().add(tp);
		tps.setCount((long) tps.getTablePart().size());
		nameColumns(part, cells);
		return part;
	}

	private static boolean overlap(AreaRef a, AreaRef b) {
		return a.getFirst().getColumn() <= b.getLast().getColumn() && b.getFirst().getColumn() <= a.getLast().getColumn()
				&& a.getFirst().getRow() <= b.getLast().getRow() && b.getFirst().getRow() <= a.getLast().getRow();
	}

	/** A GUID as Office writes one, braced, upper case, from the source given */
	static String guid(Random ids) {
		return "{" + new UUID(ids.nextLong(), ids.nextLong()).toString().toUpperCase(Locale.ROOT) + "}";
	}

	/** Whether Excel's resizing of this table is known: a header row, no totals row */
	public static boolean canResize(TablePart table) throws Docx4JException {
		CTTable t = table.getContents();
		return t.getHeaderRowCount() == 1 && t.getTotalsRowCount() == 0;
	}

	/**
	 * The table's range set: its {@code ref} and its {@code autoFilter}'s; columns added or removed
	 * at the right to match; every column named from its header cell
	 */
	public static void setRef(TablePart table, AreaRef ref, SheetCells cells) throws Docx4JException {
		CTTable t = table.getContents();
		t.setRef(ref.formatLocal());
		if (t.getAutoFilter() != null) t.getAutoFilter().setRef(ref.formatLocal());
		CTTableColumns cols = t.getTableColumns();
		List<CTTableColumn> list = cols.getTableColumn();
		while (list.size() > ref.getColumnCount()) list.remove(list.size() - 1);
		while (list.size() < ref.getColumnCount()) {
			long id = 0;
			for (CTTableColumn c : list) id = Math.max(id, c.getId());
			CTTableColumn c = new CTTableColumn();
			c.setId(id + 1);
			c.setUid("{" + UUID.randomUUID().toString().toUpperCase(Locale.ROOT) + "}");
			c.setName("");
			c.setParent(cols);
			list.add(c);
		}
		cols.setCount((long) list.size());
		nameColumns(table, cells);
	}

	/** The column at that position (from 0) removed, the range one column narrower */
	public static void removeColumn(TablePart table, int position, SheetCells cells) throws Docx4JException {
		CTTable t = table.getContents();
		AreaRef ref = getRef(table).resize(0, 0, -1, 0);
		t.getTableColumns().getTableColumn().remove(position);
		setRef(table, ref, cells);
	}

	/**
	 * Every column named from its header cell, as Excel names them: the cell's text, else
	 * Column and its position; a name another column has (Excel compares them ignoring case)
	 * made unique with a number, the header cell given the same name
	 */
	public static void nameColumns(TablePart table, SheetCells cells) throws Docx4JException {
		CTTable t = table.getContents();
		AreaRef ref = getRef(table);
		if (t.getHeaderRowCount() == 0) return;
		int row = ref.getFirst().getRow();
		Set<String> taken = new HashSet<String>();
		List<CTTableColumn> list = t.getTableColumns().getTableColumn();
		for (int i = 0; i < list.size(); i++) {
			int column = ref.getFirst().getColumn() + i;
			String text = cells.getText(cells.getCell(column, row));
			if (text != null && SheetCells.isNumber(cells.getCell(column, row))) {
				text = SheetCells.numberText(Double.parseDouble(text));
			}
			String name = text == null || text.isEmpty() ? "Column" + (i + 1) : text;
			if (taken.contains(name.toLowerCase(Locale.ROOT))) {
				int n = 2;
				while (taken.contains((name + n).toLowerCase(Locale.ROOT))) n++;
				name = name + n;
			}
			if (text == null || !name.equals(text)) cells.setString(column, row, name);
			taken.add(name.toLowerCase(Locale.ROOT));
			list.get(i).setName(name);
		}
	}
}
