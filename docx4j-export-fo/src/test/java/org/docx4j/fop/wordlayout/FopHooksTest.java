/*
 * Copyright 2026, Plutext Pty Ltd.
 *
 * This file is part of docx4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.docx4j.fop.wordlayout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.apache.fop.area.inline.FilledArea;
import org.apache.fop.area.inline.Space;
import org.apache.fop.layoutmgr.LeafPosition;
import org.apache.fop.layoutmgr.inline.LineLayoutManager.LineBreakPosition;
import org.apache.fop.text.linebreak.LineBreakUtils;
import org.docx4j.convert.out.fo.FopCapabilities;
import org.junit.Test;

/**
 * The hook path and the reflective path give the same answers (CR-020 phase 1): on
 * Apache FOP every handle is null and the fields are read; on the docx4j FO renderer
 * (-Pfo-renderer-fork) the fork's public accessors are used and no field is touched.
 */
public class FopHooksTest {

	private static boolean fork() {
		return FopCapabilities.get().isDocx4jRenderer();
	}

	@Test
	public void handlesFollowTheRenderer() {
		assertEquals(fork(), FopHooks.INLINE_ACCESS);
		assertEquals(fork(), FopHooks.LEADER_PLACEMENT);
		assertEquals(fork(), FopHooks.PAIR_TABLE);
		java.lang.invoke.MethodHandle h = FopHooks.method(FopHooks.INLINE_ACCESS, LeafPosition.class, "setLeafPos", int.class);
		if (fork()) assertNotNull(h); else assertNull(h);
		// a hook that is off never resolves, even for a method Apache FOP has
		assertNull(FopHooks.method(false, LeafPosition.class, "getLeafPos"));
	}

	@Test
	public void lineBreakPositionRoundTrip() {
		LineBreakPosition p = LBP.create(null, 1, 2, 3, 4, 5, 6, 0.5, 0.25, 7, 8, 9, 10, 11, 12, 13);
		assertEquals(1, LBP.parIndex(p));
		assertEquals(2, LBP.startIndex(p));
		assertEquals(3, p.getLeafPos());
		assertEquals(4, LBP.availableShrink(p));
		assertEquals(5, LBP.availableStretch(p));
		assertEquals(6, LBP.difference(p));
		assertEquals(0.5, LBP.ipdAdjust(p), 0);
		assertEquals(0.25, LBP.dAdjust(p), 0);
		assertEquals(7, LBP.startIndent(p));
		assertEquals(8, LBP.endIndent(p));
		assertEquals(9, LBP.lineHeight(p));
		assertEquals(10, LBP.lineWidth(p));
		assertEquals(11, LBP.spaceBefore(p));
		assertEquals(12, LBP.spaceAfter(p));
		assertEquals(13, LBP.baseline(p));
	}

	@Test
	public void leafPosSetter() {
		LeafPosition p = new LeafPosition(null, 3);
		LBP.setLeafPos(p, 7);
		assertEquals(7, p.getLeafPos());
	}

	@Test
	public void filledAreaUnitIsTheUnexpandedList() {
		FilledArea run = new FilledArea();
		Space unit = new Space();
		unit.setIPD(100);
		run.addChildArea(unit);
		run.setUnitWidth(100);
		run.setIPD(550);
		assertEquals(5, run.getChildAreas().size());
		assertSame(unit, LBP.filledUnit(run).get(0));
		assertEquals(1, LBP.filledUnit(run).size());
	}

	@Test
	public void pairTableIsWordsAfterApply() {
		WordBreakOpportunities.applyWordPairTable();
		if (WordLayoutCustomizer.breakOpportunities()) {
			assertEquals(LineBreakUtils.DIRECT_BREAK, LineBreakUtils.getLineBreakPairProperty(
					LineBreakUtils.LINE_BREAK_PROPERTY_HY, LineBreakUtils.LINE_BREAK_PROPERTY_NU));
		}
	}
}
