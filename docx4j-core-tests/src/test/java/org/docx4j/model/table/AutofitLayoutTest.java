package org.docx4j.model.table;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AutofitLayoutTest {

	@Test
	public void contentFitsTakesMaxima() {
		int[] w = AutofitLayout.distribute(new int[] { 100, 200 }, new int[] { 300, 400 }, null, 1000);
		assertArrayEquals(new int[] { 300, 400 }, w);
	}

	@Test
	public void minimaDoNotFitTakesMinima() {
		int[] w = AutofitLayout.distribute(new int[] { 600, 600 }, new int[] { 900, 900 }, null, 1000);
		assertArrayEquals(new int[] { 600, 600 }, w);
	}

	@Test
	public void slackSharedByFlexibility() {
		// the Word-measured shape: a short word, a short phrase, a paragraph; 9026 twips available
		int[] min = { 694, 1036, 1520 };
		int[] max = { 694, 2320, 38000 };
		int[] w = AutofitLayout.distribute(min, max, null, 9026);
		assertEquals(9026, w[0] + w[1] + w[2]);
		assertEquals(694, w[0]);            // no flexibility
		assertEquals(1036 + (9026 - 3250) * 1284 / 37764, w[1], 1);
	}

	@Test
	public void preferredColumnIsFixed() {
		int[] w = AutofitLayout.distribute(new int[] { 100, 100, 100 }, new int[] { 1000, 1000, 1000 },
				new int[] { 500, -1, -1 }, 2000);
		assertEquals(500, w[0]);
		assertEquals(2000, w[0] + w[1] + w[2]);
		assertEquals(w[1], w[2]);
	}

	// ---- preferences as maxima: a grid Word did not compute (word-layout-rules.md §6.3) ----

	/** The corpus certificate Word's re-saved grid was measured on: every cell states a
	 *  width, 10450 twips of them on a 9020-twip column; Word wrote 1778 / 905 / 1027 / 1998 /
	 *  1540 / 776 / 986 (9010).  The last column is widened past its 948 to what M/TONS needs. */
	@Test
	public void preferredAsMaximumReproducesWordsGrid() {
		int[] min = { 877, 320, 766, 1363, 443, 320, 987 };
		int[] pref = { 2116, 1125, 1126, 2236, 1951, 948, 948 };
		int[] word = { 1778, 905, 1027, 1998, 1540, 776, 986 };
		int[] w = AutofitLayout.distributePreferredAsMaximum(min, pref, 9010);
		int sum = 0;
		for (int i = 0; i < w.length; i++) {
			assertEquals("column " + i, word[i], w[i], 1);
			sum += w[i];
		}
		assertEquals(9010, sum);
	}

	@Test
	public void preferredAsMaximumTakesThePreferencesWhereTheyFit() {
		int[] w = AutofitLayout.distributePreferredAsMaximum(new int[] { 100, 200, 300 },
				new int[] { 1000, 2000, 3000 }, 6500);
		assertArrayEquals(new int[] { 1000, 2000, 3000 }, w);
	}

	@Test
	public void preferredAsMaximumWidensAColumnToItsContent() {
		// the middle column's content needs 800: its preference of 500 is not enough, and
		// the excess comes out of the others' slack
		int[] w = AutofitLayout.distributePreferredAsMaximum(new int[] { 100, 800, 100 },
				new int[] { 1000, 500, 1000 }, 2000);
		assertEquals(800, w[1]);
		assertEquals(2000, w[0] + w[1] + w[2]);
		assertEquals(w[0], w[2]);
	}

	@Test
	public void preferredAsMaximumFallsBackToTheMinima() {
		int[] w = AutofitLayout.distributePreferredAsMaximum(new int[] { 600, 600 },
				new int[] { 1000, 1000 }, 1000);
		assertArrayEquals(new int[] { 600, 600 }, w);
		// and treats a column with no preference as fixed at its minimum
		w = AutofitLayout.distributePreferredAsMaximum(new int[] { 100, 100 }, new int[] { 500, -1 }, 400);
		assertArrayEquals(new int[] { 300, 100 }, w);
	}

	// ---- squeeze: how a shortfall is shared (word-layout-rules.md §6.5) ----

	/** The 36-column corpus table Word's kept grid was measured on: 2672 / 664 x 34 / 881
	 *  twips of minima, 216 of cell margins each, 9350 to share.  Word: 431 / 255,254 / 271. */
	@Test
	public void squeezeKeepsMarginsAndSharesByText() {
		int n = 36;
		int[] min = new int[n], floor = new int[n];
		java.util.Arrays.fill(min, 664);
		java.util.Arrays.fill(floor, 216);
		min[0] = 2672;
		min[35] = 881;
		int[] w = AutofitLayout.squeeze(min, floor, 9350);
		int sum = 0;
		for (int v : w) sum += v;
		assertEquals(9350, sum);
		assertEquals(431, w[0], 5);       // Word 431; scaling in proportion gives 987
		assertEquals(271, w[35], 3);      // Word 271
		for (int i = 1; i < 35; i++) assertEquals(254, w[i], 1);   // Word 255 and 254
	}

	/** A picture beside a text column: the picture is incompressible.  Word keeps its
	 *  403 twips of a 1198 table and breaks the text into the 795 left. */
	@Test
	public void squeezeKeepsAPictureWhole() {
		int[] w = AutofitLayout.squeeze(new int[] { 1462, 405 }, new int[] { 30, 405 }, 1198);
		assertEquals(1198, w[0] + w[1]);
		assertEquals(405, w[1]);
		assertEquals(793, w[0]);
	}

	@Test
	public void squeezeLeavesFittingWidthsAlone() {
		assertArrayEquals(new int[] { 300, 400 },
				AutofitLayout.squeeze(new int[] { 300, 400 }, new int[] { 216, 216 }, 700));
		assertArrayEquals(new int[] { 300, 400 },
				AutofitLayout.squeeze(new int[] { 300, 400 }, new int[] { 216, 216 }, 1000));
	}

	/** Where not even the margins fit, or nothing stands above them, every column is
	 *  scaled in proportion - the rule the squeeze replaces. */
	@Test
	public void squeezeFallsBackToProportion() {
		assertArrayEquals(new int[] { 100, 300 },
				AutofitLayout.squeeze(new int[] { 500, 1500 }, new int[] { 216, 216 }, 400));
		assertArrayEquals(new int[] { 250, 250 },
				AutofitLayout.squeeze(new int[] { 300, 300 }, new int[] { 300, 300 }, 500));
		assertArrayEquals(new int[] { 100, 300 },
				AutofitLayout.squeeze(new int[] { 500, 1500 }, null, 400));
	}

	@Test
	public void squeezeSumsExactlyAndKeepsEveryColumnPositive() {
		int[] w = AutofitLayout.squeeze(new int[] { 217, 218, 219, 5000 }, new int[] { 216, 216, 216, 216 }, 1000);
		assertEquals(1000, w[0] + w[1] + w[2] + w[3]);
		for (int v : w) assertTrue(v >= 216);
		assertTrue(w[3] > w[2]);
	}
}
