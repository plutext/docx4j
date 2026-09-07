package org.docx4j.fop.wordlayout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.ListElement;
import org.junit.Test;

/**
 * A {@code keep-with-next} chain taller than a page.  FOP writes each keep as a penalty
 * of {@link KnuthElement#INFINITE}, and the breaking algorithm has no rule for a run of
 * them longer than a page: it puts the lot on one page and lets it overrun.  Measured on
 * a corpus document with 88 {@code keep-with-next="always"}, one page held 1037 lines and
 * ran to y=7693.5 on a 792pt page.
 *
 * <p>Word applies the keep locally and drops it where the blocks do not fit together, so
 * {@link WordFlowLayoutManager#boundKeepChains} reduces such a chain's keeps to a large
 * but finite penalty - the breaker may then break inside the chain, but only where it
 * must.  A chain which fits on a page is left exactly as it was.</p>
 *
 * @since 17.1.0
 */
public class KeepChainBoundTest {

	private static final int PAGE = 700000;		// millipoints, about a Letter text area

	/** n blocks of the given height, each kept with the next. */
	private static List<ListElement> keptChain(int n, int blockHeight) {
		List<ListElement> els = new ArrayList<ListElement>();
		for (int i = 0; i < n; i++) {
			if (i > 0) {
				els.add(new KnuthPenalty(0, KnuthElement.INFINITE, false, null, false));
			}
			els.add(new KnuthBox(blockHeight, null, false));
		}
		return els;
	}

	private static int infinites(List<ListElement> els) {
		int n = 0;
		for (ListElement e : els) {
			if (e instanceof KnuthPenalty
					&& ((KnuthPenalty) e).getPenalty() == KnuthElement.INFINITE) {
				n++;
			}
		}
		return n;
	}

	/** Six blocks of 100pt keep together on a 700pt page: nothing is touched. */
	@Test
	public void aKeepChainWhichFitsIsUntouched() {
		List<ListElement> els = keptChain(6, 100000);
		WordFlowLayoutManager.boundKeepChains(els, PAGE);
		assertEquals("no keep should have been weakened", 5, infinites(els));
	}

	/** Forty blocks of 100pt cannot, by any margin: the chain becomes breakable.  The
	 *  height summed at flow level over-estimates what the page must hold, so the
	 *  default tolerance is three times the page. */
	@Test
	public void aKeepChainTallerThanThePageIsBounded() {
		List<ListElement> els = keptChain(40, 100000);
		WordFlowLayoutManager.boundKeepChains(els, PAGE);
		assertTrue("the breaker still has no break in the chain", infinites(els) < 39);
		boolean weakened = false;
		for (ListElement e : els) {
			if (e instanceof KnuthPenalty) {
				int p = ((KnuthPenalty) e).getPenalty();
				if (p < KnuthElement.INFINITE) {
					weakened = true;
					assertTrue("the keep must stay expensive, so the breaker prefers to honour it",
							p >= 500);
				}
			}
		}
		assertTrue("some keep must have been weakened", weakened);
	}

	/** A break the breaker may already take resets the accumulation. */
	@Test
	public void aBreakableGapEndsTheChain() {
		List<ListElement> els = new ArrayList<ListElement>();
		els.addAll(keptChain(6, 100000));
		els.add(new KnuthPenalty(0, 0, false, null, false));	// a legal break
		els.addAll(keptChain(6, 100000));
		WordFlowLayoutManager.boundKeepChains(els, PAGE);
		assertEquals("neither half exceeds the page on its own", 10, infinites(els));
	}

	/** A chain only somewhat over the page is one Word fits, and the tolerance keeps
	 *  it: measured, a chain 12% over cost a 71-page document a 72nd page. */
	@Test
	public void aChainOnlySomewhatOverThePageIsKept() {
		List<ListElement> els = keptChain(9, 100000);	// 900pt against a 700pt page
		WordFlowLayoutManager.boundKeepChains(els, PAGE);
		assertEquals("under three times the page, the keeps stand", 8, infinites(els));
	}

	/** With no page dimension to compare against, nothing is decided. */
	@Test
	public void noPageHeightLeavesTheListAlone() {
		List<ListElement> els = keptChain(40, 100000);
		WordFlowLayoutManager.boundKeepChains(els, 0);
		assertEquals(39, infinites(els));
	}
}
