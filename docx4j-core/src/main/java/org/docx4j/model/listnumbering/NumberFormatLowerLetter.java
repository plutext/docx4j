package org.docx4j.model.listnumbering;

/**
 * {@code w:numFmt lowerLetter}: a, b, ... z, then aa, bb, cc (ECMA-376 17.18.59:
 * the letter repeats, it is not a base-26 numeral - before 17.1.1 this class
 * gave "ab" for 28 where Word gives "bb").
 */
public class NumberFormatLowerLetter extends NumberFormatAlphabet
{
	public NumberFormatLowerLetter() {
		super("abcdefghijklmnopqrstuvwxyz");
	}
}
