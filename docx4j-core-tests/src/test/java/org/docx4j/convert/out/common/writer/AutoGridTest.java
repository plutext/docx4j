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
package org.docx4j.convert.out.common.writer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * {@link AbstractTableWriter#gridFitsContent} and {@link AbstractTableWriter#autoGridWidths}:
 * a table of auto width with all-auto cells keeps a grid that could be Word's autofit of its
 * content (word-layout-rules.md §6.3, "A table of auto width with all-auto cells").
 */
public class AutoGridTest {

	/** A corpus grid Word kept: each column a few points over docx4j's widest content (the
	 *  58-twip offset seen on nine of the corpora's columns) and above its minimum. */
	@Test
	public void wordsGridIsAnAutofit() {
		int[] grid = { 1105, 390, 390, 390, 405 };
		int[] min = { 439, 332, 332, 332, 332 };
		int[] max = { 1002, 332, 332, 332, 332 };
		assertTrue(AbstractTableWriter.gridFitsContent(grid, min, max));
	}

	/** The table-grid-over-measure probe's generated grid: a column narrower than its token. */
	@Test
	public void aColumnNarrowerThanItsMinimumIsAGeneratorsGrid() {
		int[] grid = { 3000, 3000, 3026 };
		int[] min = { 2700, 2700, 3682 };
		assertFalse(AbstractTableWriter.gridFitsContent(grid, min, min));
	}

	/** Two 1500-twip columns holding "one" and "two": far wider than the content. */
	@Test
	public void aColumnFarWiderThanItsContentIsAGeneratorsGrid() {
		int[] grid = { 1500, 1500 };
		int[] content = { 520, 520 };
		assertFalse(AbstractTableWriter.gridFitsContent(grid, content, content));
	}

	/** A grid wider than the table's room is left to the page fit, which lets Word's
	 *  over-wide autofit grid overhang the margin. */
	@Test
	public void anOverWideGridIsLeftToThePageFit() {
		assertNull(AbstractTableWriter.autoGridWidths(new int[] { 6000, 6000 }, new int[] { 5000, 5000 }, 9000));
	}

	/** A column a hair short of docx4j's widest content is given it (the pbdr-space probe's
	 *  57.95pt grid against a 58.05pt line). */
	@Test
	public void aColumnAHairShortIsGivenItsContent() {
		assertArrayEquals(new int[] { 1161, 1161 },
				AbstractTableWriter.autoGridWidths(new int[] { 1159, 1159 }, new int[] { 1161, 1161 }, 9000));
	}

	/** ... but not where that would take the table past its room, */
	@Test
	public void notWhereTheTableWouldOverrun() {
		assertArrayEquals(new int[] { 4500, 4500 },
				AbstractTableWriter.autoGridWidths(new int[] { 4500, 4500 }, new int[] { 4510, 4510 }, 9000));
	}

	/** ... nor where the content is wider by more than a point and half a per cent: Word
	 *  wrapped it. */
	@Test
	public void notWhereWordWrappedTheContent() {
		assertArrayEquals(new int[] { 2000 },
				AbstractTableWriter.autoGridWidths(new int[] { 2000 }, new int[] { 3000 }, 9000));
	}
}
