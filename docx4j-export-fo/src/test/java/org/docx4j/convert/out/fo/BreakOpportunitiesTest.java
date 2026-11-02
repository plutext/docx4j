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
package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** The column sizer's break opportunities (CR-071 D6): the renderer's Word rules where it carries them, else UAX #14. */
public class BreakOpportunitiesTest {

	@Test
	public void uax14AsFopAppliesIt() {
		boolean[] b = BreakOpportunities.uax14("one two");
		assertFalse("never before the first character (LB2)", b[0]);
		assertFalse(b[1]);
		assertFalse("not before the space", b[3]);
		assertTrue("after the space", b[4]);
	}

	/** A solidus-led word after a space: Word breaks before it, UAX #14 (LB13, x SY) does not. */
	@Test
	public void theRenderersOwnWhereItCarriesWordLayout() throws Exception {
		String text = "one /two and 10% of well-known";
		int solidus = text.indexOf('/');
		boolean[] got = BreakOpportunities.breakBefore(text);
		assertFalse(BreakOpportunities.uax14(text)[solidus]);
		if (FopCapabilities.has(FopCapabilities.Capability.WORD_LAYOUT)) {
			Class<?> fork = Class.forName(BreakOpportunities.FORK_CLASS);
			assertArrayEquals((boolean[]) fork.getMethod("breakBefore", CharSequence.class).invoke(null, text), got);
			assertTrue("Word breaks before a solidus-led word after a space", got[solidus]);
		} else {
			assertArrayEquals(BreakOpportunities.uax14(text), got);
		}
	}
}
