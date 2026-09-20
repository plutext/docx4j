package org.docx4j.fop.wordlayout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Where the autofit sizer's units end: the break opportunities the line manager will
 * take, from FOP's own UAX #14 table plus Word's solidus and backslash rules.
 *
 * @since 17.2.0
 */
public class WordBreakOpportunitiesTest {

	/** The runs between break opportunities, spaces and all. */
	static List<String> units(String text) {
		boolean[] brk = WordBreakOpportunities.breakBefore(text);
		List<String> out = new ArrayList<>();
		int start = 0;
		for (int i = 1; i < text.length(); i++) {
			if (brk[i]) {
				out.add(text.substring(start, i));
				start = i;
			}
		}
		if (text.length() > 0) out.add(text.substring(start));
		return out;
	}

	/** A URL breaks after its {@code ?} and after <b>every</b> hyphen - Word breaks after
	 *  one that digits follow too, where UAX #14 LB25 ({@code HY x NU}) does not
	 *  (CR-001 batch 48 item 3) - and nowhere else: not after a solidus (Word's rule) and
	 *  not at a dot. */
	@Test
	public void aUrlBreaksAfterItsQueryMarkAndHyphens() {
		assertEquals(
				Arrays.asList("http://www.example.com/content/getDocument.aspx?", "key=AB-",
						"12-", "CD-", "34-", "EF-", "56#&doc=1"),
				units("http://www.example.com/content/getDocument.aspx?key=AB-12-CD-34-EF-56#&doc=1"));
	}

	/**
	 * Word breaks after a hyphen followed by a digit, where UAX #14 rule LB25 keeps the
	 * two together: FOP's pair table holds {@code HY x NU} as an indirect break, which
	 * inside a word is no break at all, and {@code 1997-05-12} was one unbreakable token.
	 * Measured on corpus document 5253 (CR-001 batch 48 item 3).
	 */
	@Test
	public void aHyphenBeforeDigitsBreaks() {
		assertEquals(Arrays.asList("1997-", "05-", "12"), units("1997-05-12"));
		assertEquals(Arrays.asList("ISO-", "8601"), units("ISO-8601"));
		assertEquals(Arrays.asList("2013-", "2014"), units("2013-2014"));
		assertTrue(WordBreakOpportunities.breakBetween('-', '5'));
		assertFalse("a hyphen before a letter already broke",
				WordBreakOpportunities.breakBetween('-', 'y'));
		/* In FOP's table only U+002D is class HY: U+2010 HYPHEN and U+2012 FIGURE DASH and
		 * U+2013 EN DASH are all BA, which already breaks after itself before a digit, and
		 * U+2011 NON-BREAKING HYPHEN is GL, which must not break. */
		assertFalse("U+2010 is class BA in FOP's table, and BA x NU already breaks",
				WordBreakOpportunities.breakBetween('‐', '5'));
		assertFalse("an en dash is class BA too",
				WordBreakOpportunities.breakBetween('–', '5'));
		assertFalse("a non-breaking hyphen is class GL and must not break",
				WordBreakOpportunities.breakBetween('‑', '5'));
	}

	/** And the same opportunity at the seam of two runs, which is decided by the same
	 *  pair: {@code 1997-} in one {@code fo:inline} and {@code 05} in the next. */
	@Test
	public void theSeamAfterAHyphenBreaksBeforeDigitsToo() {
		assertTrue(WordBreakOpportunities.breakAtSeam('-', '4'));
		assertTrue(WordBreakOpportunities.breakAtSeam('-', 'a'));
		assertFalse("a space on either side is FOP's own business",
				WordBreakOpportunities.breakAtSeam('-', ' '));
	}

	/**
	 * Word does not break between a letter and a per-cent sign, where FOP does: its pair
	 * table predates Unicode 8.0's LB24 ({@code (AL|HL) x (PR|PO)}) and holds
	 * {@code AL x PO} as a direct break, so {@code VAT%} was set as {@code VAT} / {@code %}.
	 * {@code NU x PO} was already indirect, so {@code 100%} never broke.
	 * Measured on corpus document 5253 (CR-001 batch 48 item 3).
	 */
	@Test
	public void aPerCentSignStaysWithTheWordBeforeIt() {
		assertEquals(Arrays.asList("VAT% ", "of ", "turnover"), units("VAT% of turnover"));
		assertEquals("the digit control, which never broke",
				Arrays.asList("100% ", "of ", "turnover"), units("100% of turnover"));
		assertTrue(WordBreakOpportunities.noBreakBeforePerCent('T', '%'));
		assertTrue(WordBreakOpportunities.noBreakBetween('T', '%'));
		assertFalse("a space before it is a break of FOP's own",
				WordBreakOpportunities.noBreakBeforePerCent(' ', '%'));
		assertFalse("and the rule is the per-cent sign's, not the whole of class PO",
				WordBreakOpportunities.noBreakBeforePerCent('T', '°'));
	}

	/** The two rules the batch-45 and batch-47 measurements settled are unmoved: a
	 *  backslash keeps its word whole and a dollar sign does not. */
	@Test
	public void theBackslashAndDollarRulesAreUnchanged() {
		assertEquals(Arrays.asList("Quejas\\Clientes\\Minoristas"), units("Quejas\\Clientes\\Minoristas"));
		assertEquals(Arrays.asList("$table.temps_ligne", "$"), units("$table.temps_ligne$"));
	}

	@Test
	public void aHyphenatedCompoundBreaksAfterEachHyphen() {
		assertEquals(Arrays.asList("state-", "of-", "the-", "art"), units("state-of-the-art"));
	}

	/** Ideographs break between any two (UAX #14 class ID), as the line manager breaks them. */
	@Test
	public void ideographsBreakBetweenAnyTwo() {
		assertEquals(Arrays.asList("日", "本", "語", "の", "文", "章"), units("日本語の文章"));
	}

	@Test
	public void aPlainWordIsOneUnit() {
		assertEquals(Arrays.asList("Conclusions"), units("Conclusions"));
	}

	/** Words split at spaces as before; the space stays with the unit it follows, where
	 *  the sizer drops it. */
	@Test
	public void wordsSplitAtSpaces() {
		assertEquals(Arrays.asList("Pas ", "soumis "), units("Pas soumis "));
	}

	/** Word does not break after a solidus (word-layout-rules.md 4.3). */
	@Test
	public void noBreakAfterASolidus() {
		assertEquals(Arrays.asList("OpenOffice/jodconverter"), units("OpenOffice/jodconverter"));
		assertEquals(Arrays.asList("http://schemas.openxmlformats.org/wordprocessingml/2006/main"),
				units("http://schemas.openxmlformats.org/wordprocessingml/2006/main"));
	}

	/** Word breaks before a solidus-led word after a space, where UAX #14 (LB13) does not. */
	@Test
	public void aSolidusLedWordBreaksAfterTheSpaceBeforeIt() {
		assertEquals(Arrays.asList("Roll ", "Number ", "/Registration ", "Number"),
				units("Roll Number /Registration Number"));
		assertEquals(Arrays.asList("see ", "//server/share"), units("see //server/share"));
		assertEquals(Arrays.asList("item ", "/123"), units("item /123"));
	}

	/** A solidus on its own is not a solidus-led word: the space before it stays with
	 *  the word before, as FOP's non-breaking space box does. */
	@Test
	public void aLoneSolidusKeepsTheSpaceBeforeIt() {
		assertEquals(Arrays.asList("a / ", "b"), units("a / b"));
	}

	@Test
	public void solidusLedWordPredicate() {
		assertTrue(WordBreakOpportunities.startsSolidusLedWord("/Registration", 0));
		assertTrue(WordBreakOpportunities.startsSolidusLedWord("//server", 0));
		assertTrue(WordBreakOpportunities.startsSolidusLedWord("/123", 0));
		assertFalse(WordBreakOpportunities.startsSolidusLedWord("/ b", 0));
		assertFalse(WordBreakOpportunities.startsSolidusLedWord("/", 0));
		assertFalse(WordBreakOpportunities.startsSolidusLedWord("a/b", 0));
		assertFalse(WordBreakOpportunities.startsSolidusLedWord("a/b", 5));
		assertTrue(WordBreakOpportunities.noBreakAfter('/'));
		assertFalse(WordBreakOpportunities.noBreakAfter('-'));
	}

	/** Word does not break between a letter and a backslash, where FOP's pair table -
	 *  from before Unicode 8.0's LB24 - does: measured, Word sets a category column's
	 *  {@code Quejas\\Clientes\\Minoristas} whole where ours broke before each backslash.
	 *  It does break before a dollar sign after a letter (a template's trailing {@code $}
	 *  goes to a line of its own in Word's PDF), so the rest of class PR is FOP's. */
	@Test
	public void noBreakBetweenALetterAndABackslash() {
		assertEquals(Arrays.asList("Quejas\\Clientes\\Mayoristas"), units("Quejas\\Clientes\\Mayoristas"));
		assertEquals(Arrays.asList("Cargo\\Program ", "Files\\docx4j"), units("Cargo\\Program Files\\docx4j"));
		assertEquals(Arrays.asList("$table.temps_ligne", "$"), units("$table.temps_ligne$"));
		assertEquals(Arrays.asList("US", "$100"), units("US$100"));
		assertTrue(WordBreakOpportunities.noBreakBetween('s', '\\'));
		assertTrue(WordBreakOpportunities.noBreakBetween('/', 'a'));
		assertFalse(WordBreakOpportunities.noBreakBetween('s', '$'));
		assertFalse(WordBreakOpportunities.noBreakBetween('-', 'a'));
		assertFalse(WordBreakOpportunities.noBreakBetween('s', -1));
	}

	/** A no-break space glues (UAX #14 class GL); a zero-width space is a break (ZW). */
	@Test
	public void noBreakSpaceGluesAndZeroWidthSpaceBreaks() {
		assertEquals(Arrays.asList("a\u00A0b"), units("a\u00A0b"));
		assertEquals(Arrays.asList("ab\u200B", "cd"), units("ab\u200Bcd"));
	}

	@Test
	public void indexZeroIsNeverABreak() {
		boolean[] brk = WordBreakOpportunities.breakBefore(" leading");
		assertFalse(brk[0]);
		assertEquals(0, WordBreakOpportunities.breakBefore("").length);
	}
}
