package org.docx4j.convert.out.common.writer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * {@link AbstractTableWriter#gridContradictsPreferences}: when a {@code w:tblGrid} cannot be
 * the layout Word made of the cells' {@code w:tcW}, and the table is laid out on the
 * preferences instead (word-layout-rules.md §6.3).
 */
public class StaleGridTest {

	/** The corpus certificate: columns 2 and 6 of the grid are 66% and 87% wider than the
	 *  cells' widths, whose content needs far less. */
	@Test
	public void certificateGridIsNoLayoutOfItsPreferences() {
		int[] grid = { 1603, 1863, 721, 1702, 423, 1773, 925 };
		int[] pref = { 2116, 1125, 1126, 2236, 1951, 948, 948 };
		int[] min = { 877, 320, 766, 1363, 443, 320, 987 };
		assertTrue(AbstractTableWriter.gridContradictsPreferences(grid, pref, min, 9020));
	}

	/** A grid a column of which Word widened past its preference because the content needed
	 *  it (a kept corpus grid: 714 for a 421 preference holding a 714 token) is a layout. */
	@Test
	public void contentThatNeedsTheExcessIsNotAContradiction() {
		int[] grid = { 714, 10619 };
		int[] pref = { 421, 10907 };
		int[] min = { 714, 1259 };
		assertFalse(AbstractTableWriter.gridContradictsPreferences(grid, pref, min, 11338));
	}

	/** A grid within the excess a preference tolerates (an 11% wider column, kept by Word). */
	@Test
	public void smallExcessIsTolerated() {
		int[] grid = { 1642, 3611, 2200, 2698 };
		int[] pref = { 1476, 3655, 2510, 2510 };
		int[] min = { 1417, 218, 683, 2473 };
		assertFalse(AbstractTableWriter.gridContradictsPreferences(grid, pref, min, 10161));
	}

	/** A grid narrower than the preferences is a layout Word can have made (a squeeze). */
	@Test
	public void narrowerThanThePreferencesIsNotAContradiction() {
		int[] grid = { 4177, 5620 };
		int[] pref = { 9638, 7370 };
		int[] min = { 1119, 2375 };
		assertFalse(AbstractTableWriter.gridContradictsPreferences(grid, pref, min, 9797));
	}

	/** An over-wide grid is the page fit's business, whatever its shape. */
	@Test
	public void overWideGridIsLeftToThePageFit() {
		int[] grid = { 3000, 6000 };
		int[] pref = { 4000, 1000 };
		int[] min = { 500, 500 };
		assertFalse(AbstractTableWriter.gridContradictsPreferences(grid, pref, min, 8000));
		assertTrue(AbstractTableWriter.gridContradictsPreferences(grid, pref, min, 9000));
	}

	@Test
	public void columnsWithoutAPreferenceAreIgnored() {
		int[] grid = { 3000, 6000 };
		int[] pref = { -1, -1 };
		int[] min = { 500, 500 };
		assertFalse(AbstractTableWriter.gridContradictsPreferences(grid, pref, min, 9000));
		assertFalse(AbstractTableWriter.gridContradictsPreferences(null, pref, min, 9000));
	}
}
