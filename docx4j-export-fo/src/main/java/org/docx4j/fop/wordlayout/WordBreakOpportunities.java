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

import java.lang.reflect.Field;

import org.apache.fop.fonts.GlyphMapping;
import org.apache.fop.text.linebreak.LineBreakStatus;
import org.apache.fop.text.linebreak.LineBreakUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @since 17.2.0
 */
public final class WordBreakOpportunities {

	private WordBreakOpportunities() {}

	/** Whether {@link #applyWordPairTable} has run. */
	private static boolean pairTableApplied;

	private static final Logger log = LoggerFactory.getLogger(WordBreakOpportunities.class);

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
		if (after == '\\' && isLetter(before)) return true;
		return noBreakBeforePerCent(before, after);
	}

	/**
	 * Word does not break between a letter and a <b>per-cent sign</b>, where FOP does.
	 *
	 * <p>Same root as the reverse solidus above: FOP's pair table predates Unicode 8.0's
	 * LB24, which added {@code (AL|HL) x (PR|PO)}, so it holds {@code AL x PO} as a
	 * <em>direct</em> break and sets {@code VAT|%}. Word keeps the sign with the word.
	 * {@code NU x PO} is already indirect in the table, so {@code 100%} never broke.</p>
	 *
	 * <p>Measured on corpus document {@code 5253} on a 100pt Courier measure, which is
	 * one of the two break defects that took its page from Word's 14 to 15.</p>
	 *
	 * <p>The rule is the per-cent sign's and not the whole of class PO, as the reverse
	 * solidus rule is the backslash's and not the whole of PR: what a currency sign, a
	 * degree sign or a prime does after a letter is unmeasured, and the one PR case that
	 * <em>is</em> measured goes the other way (Word breaks between a letter and a dollar
	 * sign).</p>
	 *
	 * @since 17.2.0 (CR-001 batch 48 item 3)
	 */
	public static boolean noBreakBeforePerCent(char before, int after) {
		return after == '%' && isLetter(before);
	}

	/**
	 * Word breaks <b>after</b> a hyphen followed by a digit, where UAX #14 does not.
	 *
	 * <p>Rule LB25 keeps a hyphen with the number after it - FOP's pair table holds
	 * {@code HY x NU} as an <em>indirect</em> break, which without an intervening space
	 * is no break at all - so {@code 1997-05-12}, {@code 2013-2014}, {@code ISO-8601}
	 * and {@code T-1000} are unbreakable tokens, where the same hyphen before a letter
	 * ({@code HY x AL}, a direct break) breaks: {@code x-|y}. Word breaks after the
	 * hyphen in both.</p>
	 *
	 * <p>Measured on corpus document {@code 5253} on a 100pt Courier measure, the other
	 * half of the page it lost against Word's 14.</p>
	 *
	 * <p>It is the hyphen's rule, not every dash's, and the class decides rather than a
	 * list of characters: measured on FOP's own table, U+002D HYPHEN-MINUS is the only
	 * HY, while U+2010 HYPHEN, U+2012 FIGURE DASH and U+2013 EN DASH are all BA - which
	 * is a direct break before a digit already - and U+2011 NON-BREAKING HYPHEN is GL,
	 * which must not break at all. So only the hyphen-minus is touched.</p>
	 *
	 * @since 17.2.0 (CR-001 batch 48 item 3)
	 */
	public static boolean breakBetween(char before, int after) {
		if (after < 0 || after > Character.MAX_VALUE) return false;
		return LineBreakUtils.getLineBreakProperty(before) == LineBreakUtils.LINE_BREAK_PROPERTY_HY
				&& LineBreakUtils.getLineBreakProperty((char) after)
						== LineBreakUtils.LINE_BREAK_PROPERTY_NU;
	}

	/**
	 * Whether a line may break between the last character of one text and the first of
	 * the next - the seam of two runs - as it may between the same two characters inside
	 * one text.  Each {@code FOText} gets its own {@link LineBreakStatus}, and UAX #14
	 * decides a break before a character from the pair it makes with the one before it,
	 * so the opportunity after a hyphen which ends one {@code fo:inline} is not seen
	 * when the next begins; what FOP puts at such a seam instead is a <em>flagged</em>
	 * penalty after a {@code -} or {@code /}, which the breaking algorithm ignores with
	 * hyphenation off and would take after a solidus with it on.  Word breaks after the
	 * hyphen whichever run it is in, and never after the solidus.  The same pair table,
	 * with Word's adjustments, gives the answer for the seam
	 * ({@code WordLineLayoutManager.seamBreaks}) - but only for a run ending in a
	 * <b>hyphen or dash</b>, which is the population that was measured (107 lines Word
	 * breaks at such a seam over three corpora).  Reading every pair the table allows
	 * was measured too and costs two documents: Word keeps {@code 4፡30} - an Ethiopic
	 * word space, class BA, between digits - and {@code $${{...}}} whole where the pair
	 * table breaks after the {@code ፡} and between the two {@code $} (0.4627 -> 0.3881
	 * and 0.9570 -> 0.9355 of Word's lines), so a seam at any other character is left
	 * as FOP has it.  A space on either side is FOP's own business and is not a seam.
	 *
	 * @since 17.2.0
	 */
	public static boolean breakAtSeam(char before, char after) {
		if (!isHyphenOrDash(before) || GlyphMapping.isSpace(after)) return false;
		return breakBefore(new String(new char[] { before, after }))[1];
	}

	/** A hyphen-minus, hyphen, figure dash, en dash, em dash or horizontal bar. */
	private static boolean isHyphenOrDash(char c) {
		return c == '-' || c == '\u2010' || c == '\u2012' || c == '\u2013' || c == '\u2014' || c == '\u2015';
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
	 * Teach FOP's own pair table that a hyphen before a digit breaks, so that its text
	 * managers split the box there.
	 *
	 * <p>{@link #breakBetween} states the rule, and that is enough for anything which
	 * <em>measures</em> text ({@link #breakBefore}, and through it the autofit sizer and
	 * {@link #breakAtSeam}). It is not enough for the line itself: FOP decides where a
	 * word may break while it builds the Knuth elements, in
	 * {@code TextLayoutManager.getNextKnuthElements}, and a break it does not see is a
	 * box it does not split - there is no penalty for the line manager to relax
	 * afterwards, as there is for the seam and the solidus-led word. The one lever that
	 * does not mean reimplementing that method is the table it reads:
	 * {@code LineBreakUtils.PAIR_TABLE[HY][NU]} goes from {@code INDIRECT_BREAK} - a
	 * break only across a space, so none inside {@code 1997-05} - to
	 * {@code DIRECT_BREAK}, which is what the table already holds for {@code HY x AL}.
	 *
	 * <p><b>Its reach.</b> The table is static and package-private in FOP, so this is a
	 * reflective write and it is process-wide: every FOP layout in the JVM sees it, not
	 * only docx4j's. That is why it is done from the Word layout path alone and only
	 * when {@link WordLayoutCustomizer#breakOpportunities()} is on, and why it is worth
	 * saying out loud - a host doing its own FOP work beside docx4j would inherit it.
	 * The narrower alternative, splitting the box in the line manager afterwards, means
	 * building a {@code GlyphMapping} and re-indexing every {@code LeafPosition} of the
	 * text manager, which is a great deal more to go wrong in the hottest path of the
	 * exporter.
	 *
	 * <p>Idempotent, and a no-op where FOP's table has changed shape (a value other
	 * than the {@code INDIRECT_BREAK} measured here is left alone and logged).
	 *
	 * @since 17.2.0 (CR-001 batch 48 item 3)
	 */
	public static synchronized void applyWordPairTable() {
		if (pairTableApplied || !WordLayoutCustomizer.breakOpportunities()) return;
		pairTableApplied = true;
		int hyProp = LineBreakUtils.LINE_BREAK_PROPERTY_HY;
		int nuProp = LineBreakUtils.LINE_BREAK_PROPERTY_NU;
		/* The docx4j FO renderer (hook pair-table) has a public override,
		 * LineBreakUtils.setLineBreakPairProperty(before, after, value); Apache FOP has only the
		 * private static table.  CR-020 phase 1. */
		java.lang.invoke.MethodHandle override = FopHooks.method(FopHooks.PAIR_TABLE, LineBreakUtils.class,
				"setLineBreakPairProperty", int.class, int.class, byte.class);
		if (override != null) {
			byte was = LineBreakUtils.getLineBreakPairProperty(hyProp, nuProp);
			if (was != LineBreakUtils.INDIRECT_BREAK) {
				log.info("FOP's line-break pair table holds HY x NU as " + was
						+ ", not the indirect break this was measured against; left alone");
				return;
			}
			FopHooks.call(override, hyProp, nuProp, LineBreakUtils.DIRECT_BREAK);
			return;
		}
		try {
			Field field = LineBreakUtils.class.getDeclaredField("PAIR_TABLE");
			field.setAccessible(true);
			byte[][] table = (byte[][]) field.get(null);
			int hy = LineBreakUtils.LINE_BREAK_PROPERTY_HY - 1;
			int nu = LineBreakUtils.LINE_BREAK_PROPERTY_NU - 1;
			if (hy < 0 || nu < 0 || hy >= table.length || nu >= table[hy].length) return;
			byte was = table[hy][nu];
			if (was != LineBreakUtils.INDIRECT_BREAK) {
				log.info("FOP's line-break pair table holds HY x NU as " + was
						+ ", not the indirect break this was measured against; left alone");
				return;
			}
			table[hy][nu] = LineBreakUtils.DIRECT_BREAK;
		} catch (ReflectiveOperationException | RuntimeException e) {
			log.warn("could not apply Word's HY x NU break to FOP's pair table: " + e.getMessage());
		}
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
				} else if (breakBetween(prev, c)) {
					brk = true;
				}
			}
			out[i] = brk;
		}
		return out;
	}
}
