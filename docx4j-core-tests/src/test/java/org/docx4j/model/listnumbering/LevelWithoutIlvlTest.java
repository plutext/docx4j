/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.model.listnumbering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.PPr;
import org.junit.Test;

/**
 * A {@code w:lvl} without {@code w:ilvl} (required by the schema, but some producer omits
 * it) threw a NullPointerException (issue 593).  Word reads it as level 0 and keeps none of
 * its content, so the paragraphs at level 0 get no number; a level with no definition gets
 * none either; a later definition of a level replaces an earlier one, empty or not.  Measured
 * with probes 593-a to 593-f, Word 365, 2026-10-11: the labels below are Word's, for
 * paragraphs at w:ilvl 0, 1, 2, 1, 0.
 */
public class LevelWithoutIlvlTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** The levels are told apart by their labels: "A1.", "Ba)", "Ci]", and "DI>" for a second level 0 */
	private static String lvl(String ilvl, int format) {
		String[] numFmt = { "decimal", "lowerLetter", "lowerRoman", "upperRoman" };
		String[] lvlText = { "A%1.", "B%2)", "C%3]", "D%1&gt;" };
		return "<w:lvl" + (ilvl == null ? "" : " w:ilvl=\"" + ilvl + "\"") + ">"
				+ "<w:start w:val=\"1\"/><w:numFmt w:val=\"" + numFmt[format] + "\"/>"
				+ "<w:lvlText w:val=\"" + lvlText[format] + "\"/><w:lvlJc w:val=\"left\"/>"
				+ "<w:pPr><w:ind w:left=\"" + (720 * (format % 3 + 1)) + "\" w:hanging=\"360\"/></w:pPr></w:lvl>";
	}

	private static List<String> labels(String... lvls) throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart(new PartName("/word/numbering.xml"));
		ndp.setJaxbElement((Numbering) XmlUtils.unmarshalString(
				"<w:numbering " + W + "><w:abstractNum w:abstractNumId=\"0\">"
				+ "<w:multiLevelType w:val=\"multilevel\"/>" + String.join("", lvls) + "</w:abstractNum>"
				+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num></w:numbering>"));
		pkg.getMainDocumentPart().addTargetPart(ndp);

		String[] labels = new String[5];
		int[] sequence = { 0, 1, 2, 1, 0 };
		for (int i = 0; i < sequence.length; i++) {
			PPr pPr = (PPr) XmlUtils.unmarshalString("<w:pPr " + W + "><w:numPr><w:ilvl w:val=\""
					+ sequence[i] + "\"/><w:numId w:val=\"1\"/></w:numPr></w:pPr>");
			Emulator.ResultTriple t = Emulator.getNumber(pkg, pPr);
			labels[i] = (t == null || t.getNumString() == null) ? "" : t.getNumString();
		}
		return Arrays.asList(labels);
	}

	@Test
	public void noLevelHasIlvl() throws Exception {

		assertEquals(Arrays.asList("", "", "", "", ""),
				labels(lvl(null, 0), lvl(null, 1), lvl(null, 2)));
	}

	@Test
	public void oneLevelLacksIlvl() throws Exception {

		// the second w:lvl, read as level 0, replaces the first
		assertEquals(Arrays.asList("", "", "Ci]", "", ""),
				labels(lvl("0", 0), lvl(null, 1), lvl("2", 2)));
	}

	@Test
	public void laterLevelZeroReplacesTheEmptyOne() throws Exception {

		assertEquals(Arrays.asList("A1.", "", "Ci]", "", "A2."),
				labels(lvl(null, 1), lvl("0", 0), lvl("2", 2)));
	}

	@Test
	public void laterDefinitionOfALevelWins() throws Exception {

		assertEquals(Arrays.asList("DI>", "Ba)", "Ci]", "Bb)", "DII>"),
				labels(lvl("0", 0), lvl("0", 3), lvl("1", 1), lvl("2", 2)));
	}

	@Test
	public void levelsKeyedByIlvlNotPosition() throws Exception {

		List<String> word = Arrays.asList("A1.", "Ba)", "Ci]", "Bb)", "A2.");
		assertEquals(word, labels(lvl("0", 0), lvl("1", 1), lvl("2", 2)));
		assertEquals(word, labels(lvl("2", 2), lvl("1", 1), lvl("0", 0)));
	}

	@Test
	public void documentLeftAsItIs() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart(new PartName("/word/numbering.xml"));
		ndp.setJaxbElement((Numbering) XmlUtils.unmarshalString(
				"<w:numbering " + W + "><w:abstractNum w:abstractNumId=\"0\">" + lvl(null, 0)
				+ "</w:abstractNum><w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num></w:numbering>"));
		pkg.getMainDocumentPart().addTargetPart(ndp);
		ndp.getInstanceListDefinitions();

		assertNull(ndp.getJaxbElement().getAbstractNum().get(0).getLvl().get(0).getIlvl());
	}
}
