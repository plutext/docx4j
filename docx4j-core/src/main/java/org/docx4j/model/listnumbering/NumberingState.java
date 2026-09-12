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
package org.docx4j.model.listnumbering;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The counters of one story of one traversal: what number each list level is up
 * to, and which {@code w:num} start overrides have been applied.  Definitions
 * ({@link ListNumberingDefinition}, {@link ListLevel}) hold none of this since
 * 17.1.1; a traversal owns its states (see {@link NumberingStates}) and passes
 * the right one to {@link Emulator#getNumber(org.docx4j.openpackaging.packages.WordprocessingMLPackage, org.docx4j.wml.PPr, NumberingState)},
 * so two exports of one package at once do not interleave, a footer's list
 * starts at its own start whenever the footer is converted, and
 * {@link Emulator#peek} can ask what a paragraph's number would be without
 * taking it.
 *
 * <p>Counters are keyed by the <em>referencing</em> {@code w:abstractNum} and
 * the level, which is the sharing rule Word applies: every {@code w:num} over
 * one abstract definition continues one sequence (CR-014 probe P1), and a
 * {@code w:numStyleLink} abstract definition is a list of its own (P2).  A
 * {@code w:num} with a {@code w:startOverride} for a level resets that shared
 * counter the first time the {@code w:num} is met at that level, in this story.
 *
 * <p>Not thread-safe: one state belongs to one traversal.  The state
 * {@link org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart#getNumberingState()}
 * holds is what the no-state overloads use, as they always have.
 *
 * @since 17.1.1 (CR-014 phase 4)
 */
public final class NumberingState {

	private final Map<String, ListLevel.Counter> counters = new HashMap<String, ListLevel.Counter>();
	private final Set<String> startOverridesApplied = new HashSet<String>();

	/** The counter of a level of an abstract list, created at {@code initial}
	 *  (the level's start value less one) on first use. */
	ListLevel.Counter counter(String abstractNumId, String ilvl, BigInteger initial) {
		String key = abstractNumId + '/' + ilvl;
		ListLevel.Counter c = counters.get(key);
		if (c == null) {
			c = new ListLevel.Counter();
			c.setCurrentValue(initial == null ? BigInteger.ZERO : initial);
			counters.put(key, c);
		}
		return c;
	}

	boolean startOverrideApplied(String numId, String ilvl) {
		return startOverridesApplied.contains(numId + '/' + ilvl);
	}

	void markStartOverrideApplied(String numId, String ilvl) {
		startOverridesApplied.add(numId + '/' + ilvl);
	}

	/** Every list starts again, as at the head of a story. */
	public void reset() {
		counters.clear();
		startOverridesApplied.clear();
	}

	/** Nothing has been numbered in this state yet. */
	public boolean isEmpty() {
		return counters.isEmpty() && startOverridesApplied.isEmpty();
	}

	/** An independent copy: what {@link Emulator#peek} numbers against. */
	public NumberingState copy() {
		NumberingState c = new NumberingState();
		for (Map.Entry<String, ListLevel.Counter> e : counters.entrySet()) {
			c.counters.put(e.getKey(), e.getValue().copy());
		}
		c.startOverridesApplied.addAll(startOverridesApplied);
		return c;
	}

	@Override
	public String toString() {
		return "NumberingState" + counters;
	}
}
