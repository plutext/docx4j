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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.Style;
import org.junit.Test;

/**
 * A numbering level which names a paragraph style of its own ({@code w:pStyle} inside
 * {@code w:lvl}, ECMA-376 17.9.24) numbers only that style: where the numbering reaches
 * a paragraph through a <em>different</em> style's {@code w:numPr}, Word paints no label
 * and does not count the paragraph (CR-001 &#xa7;2.8).
 *
 * <p>Measured on the {@code numbering-label-ilvl0} probe, whose level 0 of numId 20 is
 * linked to the style "NumLinked": a paragraph using a second style which carries the
 * same {@code w:numPr} gets no number at all from Word, and the next paragraph of the
 * list is numbered 6 where docx4j had counted it and reached 7.  Direct formatting is
 * never suppressed: a paragraph whose own {@code w:numPr} names a numbering whose level
 * is linked to a style it does not use is numbered.</p>
 *
 * <p>Also here: {@code w:numId 0} takes the paragraph out of the list, so the level's
 * {@code w:ind} goes with the label.  That rule existed but never fired, because
 * {@code StyleUtil.apply(NumPr, NumPr)} writes into the destination object and the
 * inherited {@code w:numPr} it was reading had already become the {@code w:numId 0}.</p>
 *
 * @since 17.0.6
 */
public class StyleLinkedLevelTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** level 0 linked to pStyle "NumLinked", indent 567/567; level 1 unlinked. */
	private static final String NUMBERING =
			"<w:numbering " + W + ">"
			+ "<w:abstractNum w:abstractNumId=\"20\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
			+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>"
			+ "<w:pStyle w:val=\"NumLinked\"/><w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>"
			+ "<w:pPr><w:ind w:left=\"567\" w:hanging=\"567\"/></w:pPr></w:lvl>"
			+ "</w:abstractNum>"
			+ "<w:abstractNum w:abstractNumId=\"21\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
			+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>"
			+ "<w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>"
			+ "<w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr></w:lvl>"
			+ "</w:abstractNum>"
			+ "<w:num w:numId=\"20\"><w:abstractNumId w:val=\"20\"/></w:num>"
			+ "<w:num w:numId=\"21\"><w:abstractNumId w:val=\"21\"/></w:num>"
			+ "</w:numbering>";

	/** a paragraph style holding a w:numPr for numId 20, plus an optional own w:ind */
	private static String style(String id, String ind) {
		return "<w:style w:type=\"paragraph\" w:styleId=\"" + id + "\" " + W + ">"
				+ "<w:name w:val=\"" + id + "\"/><w:basedOn w:val=\"Normal\"/>"
				+ "<w:pPr><w:numPr><w:numId w:val=\"20\"/></w:numPr>"
				+ (ind == null ? "" : ind) + "</w:pPr></w:style>";
	}

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage p = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart(new PartName("/word/numbering.xml"));
		ndp.setJaxbElement((Numbering) XmlUtils.unmarshalString(NUMBERING));
		p.getMainDocumentPart().addTargetPart(ndp);
		p.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add(
				(Style) XmlUtils.unmarshalString(style("NumLinked", null)));
		p.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add(
				(Style) XmlUtils.unmarshalString(
						style("NumLinkedInd0", "<w:ind w:left=\"0\" w:firstLine=\"0\"/>")));
		p.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add(
				(Style) XmlUtils.unmarshalString(
						"<w:style w:type=\"paragraph\" w:styleId=\"Plain\" " + W + ">"
						+ "<w:name w:val=\"Plain\"/><w:basedOn w:val=\"Normal\"/></w:style>"));
		// discard any resolver built before the parts were added
		p.getMainDocumentPart().getPropertyResolver(true);
		return p;
	}

	private static String number(WordprocessingMLPackage p, String styleId, boolean direct) {
		Emulator.ResultTriple t = Emulator.getNumber(p, styleId, "20", null, direct);
		return t == null ? null : t.getNumString();
	}

	@Test
	public void theLinkedStyleIsNumbered() throws Exception {
		WordprocessingMLPackage p = pkg();
		assertEquals("1.", number(p, "NumLinked", false));
		assertEquals("2.", number(p, "NumLinked", false));
	}

	@Test
	public void anotherStyleCarryingTheSameNumPrIsNotNumberedOrCounted() throws Exception {
		WordprocessingMLPackage p = pkg();
		assertEquals("1.", number(p, "NumLinked", false));
		assertNull("the level belongs to NumLinked", number(p, "NumLinkedInd0", false));
		assertEquals("and the paragraph it skipped was not counted",
				"2.", number(p, "NumLinked", false));
	}

	@Test
	public void directFormattingIsAlwaysNumbered() throws Exception {
		WordprocessingMLPackage p = pkg();
		assertEquals("a paragraph's own w:numPr applies whatever the level names",
				"1.", number(p, "Plain", true));
	}

	@Test
	public void anUnlinkedLevelIsNumberedThroughAnyStyle() throws Exception {
		WordprocessingMLPackage p = pkg();
		Emulator.ResultTriple t = Emulator.getNumber(p, "Plain", "21", null, false);
		assertNotNull("numId 21's level names no style", t);
		assertEquals("1.", t.getNumString());
	}

	/** {@code w:numId 0} drops the level's w:ind along with its label. */
	@Test
	public void numberingOffDropsTheLevelIndent() throws Exception {
		WordprocessingMLPackage p = pkg();
		PPr direct = (PPr) XmlUtils.unmarshalString(
				"<w:pPr " + W + "><w:pStyle w:val=\"NumLinked\"/>"
				+ "<w:numPr><w:numId w:val=\"0\"/></w:numPr></w:pPr>");
		PPr effective = p.getMainDocumentPart().getPropertyResolver().getEffectivePPr(direct);
		PPrBase.Ind ind = effective == null ? null : effective.getInd();
		if (ind != null) {
			assertNull("the level's 567 twips must go with its label", ind.getLeft());
			assertNull(ind.getHanging());
		}
		// the control: without the w:numId 0 the level's indent stands
		PPr numbered = (PPr) XmlUtils.unmarshalString(
				"<w:pPr " + W + "><w:pStyle w:val=\"NumLinked\"/></w:pPr>");
		PPrBase.Ind still = p.getMainDocumentPart().getPropertyResolver()
				.getEffectivePPr(numbered).getInd();
		assertNotNull("the level's indent applies to a numbered paragraph", still);
		assertEquals(567, still.getLeft().intValue());
	}
}
