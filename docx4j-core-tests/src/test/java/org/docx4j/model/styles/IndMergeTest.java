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
package org.docx4j.model.styles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigInteger;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.PPrBase.Ind;
import org.junit.Test;

/**
 * How Word merges two <code>w:ind</code>: attribute by attribute, except that
 * <code>w:firstLine</code> and <code>w:hanging</code> are two spellings of one property
 * (Word's Paragraph dialog offers "Special: (none) / First line / Hanging"), so whichever
 * of them the higher-priority <code>w:ind</code> states replaces both - and a
 * <code>w:ind</code> which states neither leaves the inherited one alone (CR-001 §2.8).
 *
 * @since 17.0.6
 */
public class IndMergeTest {

	private static Ind ind(Integer left, Integer firstLine, Integer hanging) {
		Ind ind = Context.getWmlObjectFactory().createPPrBaseInd();
		if (left != null) ind.setLeft(BigInteger.valueOf(left));
		if (firstLine != null) ind.setFirstLine(BigInteger.valueOf(firstLine));
		if (hanging != null) ind.setHanging(BigInteger.valueOf(hanging));
		return ind;
	}

	private static Integer val(BigInteger b) {
		return b == null ? null : b.intValue();
	}

	/** A w:ind stating only w:left must not wipe an inherited hanging indent. */
	@Test
	public void leftAloneKeepsTheInheritedHangingIndent() {
		Ind merged = StyleUtil.apply(ind(720, null, null), ind(346, null, 198));
		assertEquals(Integer.valueOf(720), val(merged.getLeft()));
		assertEquals(Integer.valueOf(198), val(merged.getHanging()));
		assertNull(merged.getFirstLine());
	}

	/** Nor an inherited first-line indent. */
	@Test
	public void leftAloneKeepsTheInheritedFirstLineIndent() {
		Ind merged = StyleUtil.apply(ind(720, null, null), ind(346, 198, null));
		assertEquals(Integer.valueOf(720), val(merged.getLeft()));
		assertEquals(Integer.valueOf(198), val(merged.getFirstLine()));
		assertNull(merged.getHanging());
	}

	/** A stated w:firstLine replaces the pair, so an inherited hanging goes. */
	@Test
	public void firstLineReplacesAnInheritedHangingIndent() {
		Ind merged = StyleUtil.apply(ind(0, 709, null), ind(283, null, 283));
		assertEquals(Integer.valueOf(0), val(merged.getLeft()));
		assertEquals(Integer.valueOf(709), val(merged.getFirstLine()));
		assertNull(merged.getHanging());
	}

	/** And the other way about. */
	@Test
	public void hangingReplacesAnInheritedFirstLineIndent() {
		Ind merged = StyleUtil.apply(ind(null, null, 100), ind(283, 200, null));
		assertEquals(Integer.valueOf(283), val(merged.getLeft()));
		assertEquals(Integer.valueOf(100), val(merged.getHanging()));
		assertNull(merged.getFirstLine());
	}

	/** Nothing stated at all leaves the destination as it was. */
	@Test
	public void anEmptyIndChangesNothing() {
		Ind destination = ind(346, null, 198);
		Ind merged = StyleUtil.apply(Context.getWmlObjectFactory().createPPrBaseInd(), destination);
		assertEquals(Integer.valueOf(346), val(merged.getLeft()));
		assertEquals(Integer.valueOf(198), val(merged.getHanging()));
	}

}
