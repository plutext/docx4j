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
package org.docx4j.model.chart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.junit.Test;

/**
 * A chart's data from its cells beside its caches (CR-035 §4.2, D7): none of the fixtures Office
 * saved has a cache that disagrees with its cells, the docx's embedded workbook included; a cell
 * changed afterwards is reported.
 */
public class ChartCellsTest {

	private static Chart chart(String fixture) throws Exception {
		return ChartModelTest.charts(fixture).values().iterator().next();
	}

	@Test
	public void officesCachesAgreeWithTheirCells() throws Exception {
		for (String f : new String[] { "bar-chart.xlsx", "clustered-column.docx", "stacked-column.docx",
				"stacked-column.xlsx", "pie-chart.docx", "p2-gaps.xlsx", "p2-filtered.xlsx", "p2-scatter.xlsx",
				"p2-bubble.xlsx", "p2-combo-secondary.xlsx" }) {
			Chart c = chart(f);
			ChartCells cells = ChartCells.of(c);
			assertNotNull(f, cells);
			assertEquals(f + ": " + cells.differences(ChartModel.read(c, null)), 0,
					cells.differences(ChartModel.read(c, null)).size());
		}
	}

	@Test
	public void references() throws Exception {
		ChartCells cells = ChartCells.of(chart("p2-filtered.xlsx"));
		Map<Integer, String> union = cells.cells("(Sheet1!$A$2:$A$3,Sheet1!$A$5)");
		assertEquals(3, union.size());
		assertEquals("Category 4", union.get(2));
		assertEquals("Category 3", cells.cells("Sheet1!$A$2:$A$5").get(2));
		assertEquals(Arrays.asList("Sheet1!$A$2:$A$3", "'My, Sheet'!$A$5"), ChartCells.areas("(Sheet1!$A$2:$A$3,'My, Sheet'!$A$5)"));
		assertEquals("It's", ChartCells.unquote("'It''s'"));
		assertEquals("AA", ChartCells.column(27));
		// the gaps probe: an empty cell is a gap, an error its code
		ChartCells gaps = ChartCells.of(chart("p2-gaps.xlsx"));
		assertNull(gaps.cells("Sheet1!$B$2:$B$5").get(1));
		assertEquals("#N/A", gaps.cells("Sheet1!$C$2:$C$5").get(2));
	}

	@Test
	public void aCellChangedAfterwardsIsReported() throws Exception {
		Chart c = chart("bar-chart.xlsx");
		ChartCells cells = ChartCells.of(c);
		WorksheetPart sheet = (WorksheetPart) cells.getWorkbook().getWorkbookPart().getRelationshipsPart()
				.getPart(cells.getWorkbook().getWorkbookPart().getContents().getSheets().getSheet().get(0).getId());
		for (Row r : sheet.getContents().getSheetData().getRow()) {
			for (Cell cell : r.getC()) if ("B3".equals(cell.getR())) cell.setV("7.5");
		}
		ChartCells fresh = ChartCells.of(cells.getWorkbook());
		List<ChartCells.Difference> d = fresh.differences(ChartModel.read(c, null));
		assertEquals(d.toString(), 1, d.size());
		assertEquals("values", d.get(0).getData());
		assertEquals(1, d.get(0).getIdx());
		assertEquals("2.5", d.get(0).getCache());
		assertEquals("7.5", d.get(0).getCell());
		assertTrue(d.get(0).getSeries().getIdx() == 0);
	}
}
