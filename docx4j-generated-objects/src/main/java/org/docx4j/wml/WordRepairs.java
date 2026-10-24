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
package org.docx4j.wml;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Run properties Word repairs away: a {@code w:highlight}, {@code w:vertAlign} or {@code w:em} whose {@code w:val} is
 * missing or outside its enumeration makes Word 365 offer to repair the file, and the repair drops the element
 * (measured, CR-060 section 1, rule 2). docx4j reads such an element as absent, and leaves it out when it writes the
 * run properties (CR-060 D3), so a docx4j save opens without the repair prompt; the object in memory keeps it.
 *
 * <p>The adapters are on those fields of {@link RPr} and {@link ParaRPr} ({@code ModifyGeneratedSources}); reading is
 * unchanged. A revision's former run properties ({@code w:rPrChange/w:rPr}), a list, are filtered the same way as they
 * are marshalled, since 17.3.5 (CR-061: {@link NormaliseOnSave#filterOnSave}). Part of what a save normalises, so
 * {@code docx4j.save.normalise=false} or a package's {@code setNormaliseOnSave(false)} turns it off.</p>
 *
 * @since 17.3.5
 */
public final class WordRepairs {

	private WordRepairs() {
	}

	private static final Set<String> HIGHLIGHT_COLORS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			"black", "blue", "cyan", "green", "magenta", "red", "yellow", "white", "darkBlue", "darkCyan", "darkGreen",
			"darkMagenta", "darkRed", "darkYellow", "darkGray", "lightGray", "none")));

	/** Whether a {@code w:highlight} value is one of ST_HighlightColor's, compared case-exactly, as Word does. */
	public static boolean isHighlightColor(String val) {
		return val != null && HIGHLIGHT_COLORS.contains(val);
	}

	/** A {@code w:highlight} whose value is missing or not an ST_HighlightColor is not written. */
	public static class HighlightOnSave extends NormaliseOnSave.Switched<Highlight, Highlight> {
		public HighlightOnSave() {
		}

		public HighlightOnSave(boolean on) {
			super(on);
		}

		@Override
		protected HighlightOnSave fixed(boolean on) {
			return new HighlightOnSave(on);
		}

		@Override
		public Highlight unmarshal(Highlight v) {
			return v;
		}

		@Override
		public Highlight marshal(Highlight v) {
			return isOn() && (v != null && !isHighlightColor(v.getVal())) ? null : v;
		}
	}

	/** A {@code w:vertAlign} whose value is missing (or was not an ST_VerticalAlignRun, so read as null) is not written. */
	public static class VertAlignOnSave extends NormaliseOnSave.Switched<CTVerticalAlignRun, CTVerticalAlignRun> {
		public VertAlignOnSave() {
		}

		public VertAlignOnSave(boolean on) {
			super(on);
		}

		@Override
		protected VertAlignOnSave fixed(boolean on) {
			return new VertAlignOnSave(on);
		}

		@Override
		public CTVerticalAlignRun unmarshal(CTVerticalAlignRun v) {
			return v;
		}

		@Override
		public CTVerticalAlignRun marshal(CTVerticalAlignRun v) {
			return isOn() && (v != null && v.getVal() == null) ? null : v;
		}
	}

	/** A {@code w:em} whose value is missing (or was not an ST_Em, so read as null) is not written. */
	public static class EmOnSave extends NormaliseOnSave.Switched<CTEm, CTEm> {
		public EmOnSave() {
		}

		public EmOnSave(boolean on) {
			super(on);
		}

		@Override
		protected EmOnSave fixed(boolean on) {
			return new EmOnSave(on);
		}

		@Override
		public CTEm unmarshal(CTEm v) {
			return v;
		}

		@Override
		public CTEm marshal(CTEm v) {
			return isOn() && (v != null && v.getVal() == null) ? null : v;
		}
	}
}
