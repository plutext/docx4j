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

import java.io.File;

import org.docx4j.XmlUtils;
import org.docx4j.model.text.TextBlock;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.junit.Test;

/**
 * A numbering level belongs to one paragraph style: the one its {@code w:lvl/w:pStyle}
 * names, else the first paragraph style in styles.xml whose own {@code w:numPr} names it.
 * A paragraph whose numbering comes from any other style gets no label and is not counted
 * ({@link Emulator#levelStyleId}).
 *
 * <p>The three documents are the layout-fidelity probes {@code numbering-linked-level-other-style},
 * {@code numbering-unlinked-level-claim} and {@code numbering-shared-abstract-styles} as Word
 * converted them (goldens 2026-10-18, Word
 * 365, in docx4j-layout-fidelity's goldens/word); the expected labels are read off Word's
 * PDFs, one per body paragraph, "-" for none.  Every label names its list, level and counter
 * (A0-3 is list A, level 0, the third).</p>
 */
public class LevelOwnerTest {

	private static final String DIR = "src/test/java/org/docx4j/model/listnumbering/";

	/**
	 * List A's level 0 names LinkA, level 1 nothing, levels 2 and 3 LinkL2 and LinkL3; list C
	 * names nothing.  Unlabelled: another style on level 0 with no w:ilvl (P02, before LinkA's
	 * first use, and P04), with w:ilvl 0 (P06), through a second w:num on the same list (P12);
	 * LinkL2 and LinkL3, which levels 2 and 3 name but whose w:numPr reads level 0 (P14, P16);
	 * SecondC, which list C's first style in styles.xml, FirstC, has taken (P23).  Labelled:
	 * a style at the free level 1 (P08), one based on LinkA (P10), direct w:numPr (P18-P20).
	 */
	private static final String LINKED =
			"- - A0-1 - A0-2 - A0-3 A1-1 A0-4 A0-5 A0-6 - A0-7 - A0-8 - A0-9 A0-10 A0-11 A0-12 A0-13 C0-1 - C0-2 A0-14";

	/**
	 * No level names a style.  Two styles carry each list: the first in styles.xml numbers,
	 * whatever the names and whichever the document uses first (D: DZebra, E: EAlpha, F:
	 * FZebra), even a style no paragraph uses (G: GAlphaUnused, so GZebra's paragraphs are
	 * unlabelled); a style based on the owner is numbered (H); list J's level 1 goes to
	 * OtherIlvl1, first in styles.xml, and OtherIlvl1B, used first, is unlabelled.
	 */
	private static final String UNLINKED =
			"- - D0-1 - D0-2 - E0-1 - E0-2 F0-1 - F0-2 - - - H0-1 H0-2 H0-3 H0-4 I0-1 - I0-2 J0-1 - J1-1 - J1-2";

	/**
	 * The owner is a level of the w:abstractNum, not of a w:num: two styles each using its own
	 * w:num over one abstractNum, at a level naming no style (list K), and the same where the
	 * second w:num overrides the level with a w:lvl of its own (list L, whose override labels
	 * would read M0-n): the second style's paragraphs are unlabelled in both.
	 */
	private static final String SHARED = "- K0-1 - K0-2 - L0-1 - L0-2 -";

	private static WordprocessingMLPackage load(String name) throws Exception {
		return WordprocessingMLPackage.load(new File(DIR + name + ".docx"));
	}

	/** The Emulator's label for each body paragraph, as the FO and HTML exporters number them. */
	private static String emulator(WordprocessingMLPackage pkg) {
		StringBuilder sb = new StringBuilder();
		NumberingState state = new NumberingStates().forPart(pkg.getMainDocumentPart());
		for (Object o : pkg.getMainDocumentPart().getContent()) {
			if (!(XmlUtils.unwrap(o) instanceof P)) continue;
			Emulator.NumberingResult t = Emulator.getNumber(pkg, ((P) XmlUtils.unwrap(o)).getPPr(), state);
			if (sb.length() > 0) sb.append(' ');
			sb.append(t == null || t.getNumString() == null ? "-" : t.getNumString());
		}
		return sb.toString();
	}

	/** The text model's list label for each block, which it has exactly where it has a TextNumbering. */
	private static String textModel(WordprocessingMLPackage pkg) {
		StringBuilder sb = new StringBuilder();
		for (TextBlock b : Texts.of(pkg, new TextOptions().listLabels(true)).blocks()) {
			if (b.getKind() != TextBlock.Kind.PARAGRAPH) continue;
			String label = b.getListLabel() == null ? "-" : b.getListLabel().trim();
			assertEquals("a TextNumbering where there is a label, none where there is not: " + b.getAddress(),
					label.equals("-"), b.getNumbering() == null);
			if (sb.length() > 0) sb.append(' ');
			sb.append(label);
		}
		return sb.toString();
	}

	@Test
	public void aLinkedLevelNumbersOnlyItsOwnStyle() throws Exception {
		assertEquals(LINKED, emulator(load("numbering-linked-level-other-style")));
	}

	@Test
	public void anUnlinkedLevelBelongsToTheFirstStyleInStylesXml() throws Exception {
		assertEquals(UNLINKED, emulator(load("numbering-unlinked-level-claim")));
	}

	@Test
	public void theOwnerIsALevelOfTheAbstractNumNotOfAWNum() throws Exception {
		assertEquals(SHARED, emulator(load("numbering-shared-abstract-styles")));
	}

	@Test
	public void theTextModelNumbersAsWordDoes() throws Exception {
		assertEquals(LINKED, textModel(load("numbering-linked-level-other-style")));
		assertEquals(UNLINKED, textModel(load("numbering-unlinked-level-claim")));
		assertEquals(SHARED, textModel(load("numbering-shared-abstract-styles")));
	}

	/** A TextNumbering's value is the count its label shows (the 3 of A0-3), and its list the abstractNum. */
	@Test
	public void theTextModelsCountIsTheLabels() throws Exception {
		for (String name : new String[] { "numbering-linked-level-other-style", "numbering-unlinked-level-claim",
				"numbering-shared-abstract-styles" }) {
			WordprocessingMLPackage pkg = load(name);
			for (TextBlock b : Texts.of(pkg, new TextOptions().listLabels(true)).blocks()) {
				if (b.getNumbering() == null) continue;
				String label = b.getListLabel().trim();
				assertEquals(name + " " + label, Integer.valueOf(label.substring(label.indexOf('-') + 1)), b.getNumbering().getValue());
				assertEquals(pkg.getMainDocumentPart().getNumberingDefinitionsPart().getInstanceListDefinitions()
						.get(b.getNumbering().getNumId()).getAbstractListDefinition().getID(), b.getNumbering().getAbstractNumId());
			}
		}
	}

	@Test
	public void theOwners() throws Exception {
		WordprocessingMLPackage pkg = load("numbering-unlinked-level-claim");
		org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart ndp
				= pkg.getMainDocumentPart().getNumberingDefinitionsPart();
		String[][] owners = { { "110", "0", "DZebra" }, { "111", "0", "EAlpha" }, { "112", "0", "FZebra" },
				{ "113", "0", "GAlphaUnused" }, { "114", "0", "HBase" }, { "115", "0", "IOwner" },
				{ "116", "0", "JLinked" }, { "116", "1", "OtherIlvl1" } };
		for (String[] o : owners) {
			assertEquals("w:num " + o[0] + " level " + o[1], o[2], Emulator.levelStyleId(ndp, o[0], o[1]));
		}
		assertEquals("a level no style carries belongs to none", null, Emulator.levelStyleId(ndp, "116", "2"));
	}

	/**
	 * A paragraph of a style that lost the level keeps the level's indent: Word's re-save writes
	 * w:ind left 720 hanging 720 into IOther, and its PDF wraps P21's second line at 36pt.
	 */
	@Test
	public void theStyleThatLostTheLevelKeepsItsIndent() throws Exception {
		WordprocessingMLPackage pkg = load("numbering-unlinked-level-claim");
		PPr pPr = (PPr) XmlUtils.unmarshalString(
				"<w:pPr xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ "<w:pStyle w:val=\"IOther\"/></w:pPr>");
		PPrBase.Ind ind = pkg.getMainDocumentPart().getPropertyResolver().getEffectivePPr(pPr).getInd();
		assertNotNull(ind);
		assertEquals(720, ind.getLeft().intValue());
		assertEquals(720, ind.getHanging().intValue());
	}
}
