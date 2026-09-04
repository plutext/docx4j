package org.docx4j.model.table;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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
}
