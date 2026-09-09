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
package org.docx4j.fop.wordlayout;

import org.apache.fop.fonts.GlyphMapping;
import org.apache.fop.text.linebreak.LineBreakStatus;
import org.apache.fop.text.linebreak.LineBreakUtils;

/**
 * Where a line may be broken inside a run of text, as {@link WordLineLayoutManager}
 * will break it.
 *
 * <p>The opportunities are FOP's own: {@link LineBreakStatus}, its table-driven
 * UAX #14 implementation, is what {@code TextLayoutManager.getNextKnuthElements}
 * consults character by character, and it is consulted here the same way.  On top
 * of that come the adjustments the line manager makes for Word (word-layout-rules.md
 * &#xa7;4.3), stated once here so that the line manager and anything measuring text
 * ahead of it cannot drift apart:</p>
 * <ul>
 * <li>{@link #noBreakAfter}: Word does not break after a solidus, where UAX #14 lets
 *     FOP break;</li>
 * <li>{@link #noBreakBetween}: nor between a letter and a reverse solidus, where
 *     FOP's pair table does.  Both of these are applied by
 *     {@code WordLineLayoutManager.suppressWordBreaks};</li>
 * <li>{@link #startsSolidusLedWord}: Word breaks before a solidus-led word that
 *     follows a space, where UAX #14's LB13 forbids a break before class SY
 *     ({@code WordLineLayoutManager.solidusLeadingBreaks}).</li>
 * </ul>
 *
 * <p>The autofit column sizer ({@code org.docx4j.convert.out.fo.TableWriter}) takes a
 * cell's minimum width to be its widest run between these opportunities, so a URL
 * is measured to its {@code ?} and its hyphens, where it will be broken, rather than
 * whole.  Two things the line manager can also do are deliberately <em>not</em>
 * opportunities here.  Hyphenation points: FOP inserts them into a word only where
 * the block asks to be hyphenated, and whether Word lets hyphenation narrow a column
 * is unmeasured (the reference machine hyphenates none of the corpus documents that
 * ask for it), so the minimum stays the whole word - a column is then never narrower
 * than the text needs with hyphenation off, which is how the harness scores.  And the
 * emergency break (&#xa7;4.3, &#xa7;6.11), which splits a word wider than its measure
 * at whatever character reaches the edge: it is a consequence of the width, not an
 * input to it - fed back into the sizer it would make every minimum one character,
 * which is not what Word does when it sizes a column.</p>
 *
 * @since 17.1.1
 */
public final class WordBreakOpportunities {

	private WordBreakOpportunities() {}

	/**
	 * Word does not break a line after a solidus: a URL, or a pair of words joined by
	 * a slash, goes whole to the next line (word-layout-rules.md &#xa7;4.3).
	 */
	public static boolean noBreakAfter(char c) {
		return c == '/';
	}

	/**
	 * Whether a break UAX #14 (as FOP applies it) allows between these two adjacent
	 * characters is one Word does not take: after a solidus ({@link #noBreakAfter}),
	 * or between a letter and a reverse solidus.  FOP's pair table is generated from
	 * a Unicode version before 8.0, whose LB24 added {@code (AL|HL) x (PR|PO)}, so it
	 * breaks {@code Quejas|\Clientes}; Word sets the token whole (measured on a
	 * 311-page corpus document whose category column holds
	 * {@code Quejas\Clientes\Minoristas}: Word's line holds it whole, three times over,
	 * where ours broke it before each backslash).  The rule is the backslash's, not the
	 * whole class's: Word <em>does</em> break between a letter and a dollar sign -
	 * measured on two corpus templates of {@code $table.temps_ligne$} tokens, whose
	 * trailing {@code $} Word puts on a line of its own - so a currency sign, a plus
	 * and the rest of PR and PO are left as UAX #14 has them.  A letter here is class
	 * AL after LB1's resolution, as {@link LineBreakStatus} resolves it.
	 *
	 * @param after the following character, or -1 where it is not known (only the
	 *        solidus rule then applies)
	 */
	public static boolean noBreakBetween(char before, int after) {
		if (noBreakAfter(before)) return true;
		return after == '\\' && isLetter(before);
	}

	/** Class AL, including what LB1 resolves to it (AI, SG, XX, an unassigned code point,
	 *  and SA where it is not a combining mark). */
	private static boolean isLetter(char c) {
		byte cls = LineBreakUtils.getLineBreakProperty(c);
		switch (cls) {
			case LineBreakUtils.LINE_BREAK_PROPERTY_AL:
			case LineBreakUtils.LINE_BREAK_PROPERTY_AI:
			case LineBreakUtils.LINE_BREAK_PROPERTY_SG:
			case LineBreakUtils.LINE_BREAK_PROPERTY_XX:
			case 0:
				return true;
			case LineBreakUtils.LINE_BREAK_PROPERTY_SA:
				int type = Character.getType(c);
				return type != Character.COMBINING_SPACING_MARK && type != Character.NON_SPACING_MARK;
			default:
				return false;
		}
	}

	/**
	 * Whether the text at {@code i} opens a solidus-led word: one or more solidi
	 * followed by a letter or digit ({@code /Registration}, {@code //server}).  Word
	 * breaks before such a word where it follows a space; UAX #14 does not (LB13).
	 * {@code / } on its own, or a solidus that ends the text, is not one.
	 */
	public static boolean startsSolidusLedWord(CharSequence text, int i) {
		int n = text.length();
		if (i < 0 || i >= n || text.charAt(i) != '/') return false;
		int j = i + 1;
		while (j < n && text.charAt(j) == '/') j++;
		return j < n && Character.isLetterOrDigit(text.charAt(j));
	}

	/**
	 * For each character of {@code text}, whether a line may be broken immediately
	 * before it, as the line manager will decide: UAX #14 as FOP applies it (a
	 * mandatory break counts), less the breaks of {@link #noBreakBetween}, plus the
	 * break before a solidus-led word after a space.  Index 0 is never a break (LB2).
	 *
	 * <p>The text is one text node's worth, as FOP sees it: a new
	 * {@link LineBreakStatus} for each, which is why a word running across two
	 * {@code fo:inline}s cannot break at the seam - FOP's text managers do not either,
	 * and a trailing space is the one thing that breaks there.</p>
	 */
	public static boolean[] breakBefore(CharSequence text) {
		int n = text.length();
		boolean[] out = new boolean[n];
		LineBreakStatus status = new LineBreakStatus();
		for (int i = 0; i < n; i++) {
			char c = text.charAt(i);
			boolean brk;
			switch (status.nextChar(c)) {
				case LineBreakStatus.DIRECT_BREAK:
				case LineBreakStatus.INDIRECT_BREAK:
				case LineBreakStatus.COMBINING_INDIRECT_BREAK:
				case LineBreakStatus.EXPLICIT_BREAK:
					brk = true;
					break;
				default:
					brk = false;
			}
			if (i > 0) {
				char prev = text.charAt(i - 1);
				if (brk) {
					if (noBreakBetween(prev, c)) brk = false;
				} else if (GlyphMapping.isSpace(prev) && startsSolidusLedWord(text, i)) {
					brk = true;
				}
			}
			out[i] = brk;
		}
		return out;
	}
}
