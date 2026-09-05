package org.docx4j.convert.out.common.writer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Wingdings 0xD8, the black rightwards arrowhead Word uses for a bullet, was
 * mapped to U+2B9A - the "equilateral" arrowhead Unicode 7.0 added, which
 * alanwood's table (the source of these maps) gives.  Word itself maps it to the
 * Dingbats character U+27A2, as its own PDF output shows, and that is what most
 * fonts carry (CR-001, two real documents).  Its up, down and left counterparts
 * have no Dingbats equivalent, so they keep the 2B98..2B9B codes.
 */
public class SymbolMapperWingdingsTest {

	/** the way RunFontSelector reaches the map for a w:sym-less Wingdings run: the
	 *  private use code point F0D8 less F000 */
	private static String wingdings(int privateUseCodePoint) {
		return SymbolMapper.getUnicodeReplacementChar("Wingdings", (short)(privateUseCodePoint - 0xF000));
	}

	@Test
	public void rightwardsArrowheadIsTheDingbatsOne() {
		assertEquals("\u27A2", wingdings(0xF0D8));
	}

	@Test
	public void itsCounterpartsAreUnchanged() {
		assertEquals("\u2B98", wingdings(0xF0D7)); // leftwards
		assertEquals("\u2B99", wingdings(0xF0D9)); // upwards
		assertEquals("\u2B9B", wingdings(0xF0DA)); // downwards
		assertEquals("\u2B88", wingdings(0xF0DB)); // leftwards black circled white arrow
	}
}
