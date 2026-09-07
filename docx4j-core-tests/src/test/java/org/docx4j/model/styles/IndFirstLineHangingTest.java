package org.docx4j.model.styles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigInteger;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.PPrBase.Ind;
import org.junit.Test;

/**
 * {@code w:firstLine} and {@code w:hanging} are two spellings of one property - Word's
 * Paragraph dialog offers "Special: (none) / First line / Hanging", not two boxes - and
 * ECMA-376 17.3.1.12 says {@code w:firstLine} "is ignored if the hanging attribute is
 * also specified".  Merged attribute by attribute, an inherited hanging therefore
 * survived a direct firstLine and won.
 *
 * <p>Measured against Word 365: a paragraph whose direct formatting is
 * {@code <w:ind w:left="0" w:firstLine="709"/>} over a style stating
 * {@code <w:ind w:left="283" w:hanging="283"/>} starts its first line at x=120.6
 * (85.05pt margin + 35.45pt) and ours put it at 70.9.</p>
 *
 * @since 17.1.0
 */
public class IndFirstLineHangingTest {

	private static Ind ind(Integer left, Integer firstLine, Integer hanging) {
		Ind ind = Context.getWmlObjectFactory().createPPrBaseInd();
		if (left != null) ind.setLeft(BigInteger.valueOf(left));
		if (firstLine != null) ind.setFirstLine(BigInteger.valueOf(firstLine));
		if (hanging != null) ind.setHanging(BigInteger.valueOf(hanging));
		return ind;
	}

	/** source is the higher-priority w:ind. */
	@Test
	public void aDirectFirstLineClearsAnInheritedHanging() {
		Ind destination = ind(283, null, 283);          // the style
		Ind result = StyleUtil.apply(ind(0, 709, null), destination); // the paragraph
		assertEquals(BigInteger.valueOf(709), result.getFirstLine());
		assertNull("the inherited hanging must go", result.getHanging());
		assertEquals(BigInteger.ZERO, result.getLeft());
	}

	@Test
	public void aDirectHangingClearsAnInheritedFirstLine() {
		Ind destination = ind(0, 709, null);
		Ind result = StyleUtil.apply(ind(283, null, 283), destination);
		assertEquals(BigInteger.valueOf(283), result.getHanging());
		assertNull("the inherited firstLine must go", result.getFirstLine());
	}

	/** A w:ind that states neither leaves whichever was inherited alone. */
	@Test
	public void anIndStatingNeitherLeavesBothAlone() {
		Ind destination = ind(283, null, 283);
		Ind result = StyleUtil.apply(ind(720, null, null), destination);
		assertEquals(BigInteger.valueOf(283), result.getHanging());
		assertEquals(BigInteger.valueOf(720), result.getLeft());
	}

	/** Where the higher-priority w:ind states both, ECMA-376's own rule applies (hanging
	 *  wins at rendering time) and both are carried through unchanged. */
	@Test
	public void bothOnOneIndAreBothCarried() {
		Ind result = StyleUtil.apply(ind(0, 100, 200), ind(283, null, 283));
		assertEquals(BigInteger.valueOf(100), result.getFirstLine());
		assertEquals(BigInteger.valueOf(200), result.getHanging());
	}
}
