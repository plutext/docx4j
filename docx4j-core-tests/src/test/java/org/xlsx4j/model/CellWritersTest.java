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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.junit.Test;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;

/** The cell writers chart editing needs (CR-035 phase 4, D11) */
public class CellWritersTest {

	@Test
	public void references() {
		CellRef c = CellRef.parse("'My, Sheet'!$B$2");
		assertEquals("My, Sheet", c.getSheet());
		assertEquals(2, c.getColumn());
		assertEquals("'My, Sheet'!$B$2", c.formatAsString());
		assertEquals("Sheet1!C3", CellRef.parse("Sheet1!C3").formatAsString());
		assertEquals("'It''s'!$A$1", new CellRef("It's", 1, 1).formatAsString());
		// a name that reads as a cell, or has a space, is quoted
		assertEquals("'A1'!$A$1", new CellRef("A1", 1, 1).formatAsString());
		assertEquals("'Q1 2026'!$A$1", new CellRef("Q1 2026", 1, 1).formatAsString());
		assertEquals("AA", CellRef.columnName(27));
		assertEquals(703, CellRef.columnNumber("AAA"));

		AreaRef a = AreaRef.parse("Sheet1!$A$2:$A$5");
		assertEquals(4, a.getRowCount());
		assertEquals("Sheet1!$A$2:$A$6", a.resize(0, 0, 0, 1).formatAsString());
		assertEquals("A2:A5", a.formatLocal());
		assertEquals("Sheet1!$B$1", AreaRef.parse("Sheet1!$B$1").formatAsString());
		List<AreaRef> u = AreaRef.parseUnion("(Sheet1!$A$2:$A$3,'My, Sheet'!$A$5)");
		assertEquals(2, u.size());
		assertEquals("My, Sheet", u.get(1).getSheet());
		assertEquals("(Sheet1!$A$2:$A$3,'My, Sheet'!$A$5)", AreaRef.formatUnion(u));
	}

	@Test
	public void numbers() {
		assertEquals("4", SheetCells.numberText(4));
		assertEquals("2.2", SheetCells.numberText(2.2));
		assertEquals("0.0001", SheetCells.numberText(1e-4));
		assertEquals("1E-7", SheetCells.numberText(1e-7));
		assertEquals("1.5E15", SheetCells.numberText(1.5e15));
		assertEquals("-46100", SheetCells.numberText(-46100));
	}

	private static SheetCells sheet() throws Exception {
		SpreadsheetMLPackage pkg = SpreadsheetMLPackage.createPackage();
		pkg.createWorksheetPart(new PartName("/xl/worksheets/sheet1.xml"), "Sheet1", 1);
		return SheetCells.of(pkg, "Sheet1");
	}

	/** Cells and rows made in order, strings shared (the part made), a cleared cell's element gone */
	@Test
	public void writing() throws Exception {
		SheetCells sc = sheet();
		assertNull(sc.getWorkbook().getWorkbookPart().getSharedStrings());
		sc.setNumber(3, 2, 7.5);
		sc.setString(1, 2, "North");
		sc.setString(2, 5, "North");
		sc.setError(2, 2, "#N/A");
		List<Row> rows = sc.getPart().getContents().getSheetData().getRow();
		assertEquals(2, rows.size());
		assertEquals(Long.valueOf(2), rows.get(0).getR());
		List<String> names = Arrays.asList(rows.get(0).getC().get(0).getR(), rows.get(0).getC().get(1).getR(),
				rows.get(0).getC().get(2).getR());
		assertEquals(Arrays.asList("A2", "B2", "C2"), names);
		assertEquals(1, sc.getWorkbook().getWorkbookPart().getSharedStrings().getContents().getSi().size());
		assertEquals("North", sc.getText(sc.getCell(2, 5)));
		Cell err = sc.getCell(2, 2);
		assertEquals(STCellType.E, err.getT());
		sc.tidy();
		assertEquals("A2:C5", sc.getPart().getContents().getDimension().getRef());
		assertEquals(Long.valueOf(2), sc.getWorkbook().getWorkbookPart().getSharedStrings().getContents().getCount());
		sc.clear(2, 5);
		assertNull(sc.getCell(2, 5));
		assertEquals(1, rows.size());
		// a styled cell keeps its element and style
		sc.getCell(3, 2).setS(1L);
		sc.clear(3, 2);
		assertEquals(1L, sc.getCell(3, 2).getS());
		assertNull(sc.getCell(3, 2).getV());
	}

	/** A block's row and column deleted, the cells after them moved, the cells outside it left */
	@Test
	public void deletingInABlock() throws Exception {
		SheetCells sc = sheet();
		for (int r = 1; r <= 4; r++) for (int c = 1; c <= 4; c++) sc.setNumber(c, r, 10 * r + c);
		AreaRef block = AreaRef.parse("Sheet1!A1:C4");
		sc.deleteRow(new AreaRef(new CellRef("Sheet1", 1, 2), block.getLast()), 2);
		assertEquals("31", sc.getText(sc.getCell(1, 2)));
		assertNull(sc.getCell(1, 4));
		assertEquals("24", sc.getText(sc.getCell(4, 2)));
		sc.deleteColumn(new AreaRef(new CellRef("Sheet1", 2, 1), new CellRef("Sheet1", 3, 3)), 2);
		assertEquals("13", sc.getText(sc.getCell(2, 1)));
		assertNull(sc.getCell(3, 1));
		assertEquals("14", sc.getText(sc.getCell(4, 1)));
		assertTrue(sc.getText(sc.getCell(1, 1)).equals("11"));
	}
}
