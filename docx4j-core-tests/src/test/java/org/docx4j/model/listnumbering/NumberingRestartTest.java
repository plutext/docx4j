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

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.junit.Test;

/**
 * {@code w:lvlRestart} (ECMA-376 17.9.11) and what a reset level shows, replaying
 * the fidelity probe {@code numbering-lvlrestart} against Word's labels (CR-014
 * probe P8, golden 2026-09-12): three-level lists walked 0 1 2 2 1 2 0 2, level 2
 * carrying {@code w:lvlRestart w:val="0"} (never restarts), {@code w:val="1"}
 * (restarts after level 1 only, which is ilvl 0), and nothing (any shallower
 * level restarts it).  The last label of each list also says what a level that
 * was reset but not yet used shows in a deeper label: its start value (2.1.x),
 * where docx4j printed 2.0.x before 17.1.1.
 */
public class NumberingRestartTest {

	private static final int[] WALK = { 0, 1, 2, 2, 1, 2, 0, 2 };

	private static String abstractNum(int id, Integer lvlRestart) {
		StringBuilder sb = new StringBuilder("<w:abstractNum w:abstractNumId=\"" + id
				+ "\"><w:multiLevelType w:val=\"multilevel\"/>");
		String[] text = { "%1.", "%1.%2.", "%1.%2.%3." };
		for (int ilvl = 0; ilvl < 3; ilvl++) {
			sb.append("<w:lvl w:ilvl=\"" + ilvl + "\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>");
			if (ilvl == 2 && lvlRestart != null) sb.append("<w:lvlRestart w:val=\"" + lvlRestart + "\"/>");
			sb.append("<w:lvlText w:val=\"" + text[ilvl] + "\"/><w:lvlJc w:val=\"left\"/>"
					+ "<w:pPr><w:ind w:left=\"" + (720 + 720 * ilvl) + "\" w:hanging=\"720\"/></w:pPr></w:lvl>");
		}
		return sb.append("</w:abstractNum>").toString();
	}

	private static WordprocessingMLPackage packageWith(String numberingBody) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String xml = "<w:numbering xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ numberingBody + "</w:numbering>";
		Object o = XmlUtils.unmarshalString(xml, Context.jc, org.docx4j.wml.Numbering.class);
		if (o instanceof jakarta.xml.bind.JAXBElement) o = ((jakarta.xml.bind.JAXBElement<?>) o).getValue();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		ndp.setJaxbElement((org.docx4j.wml.Numbering) o);
		pkg.getMainDocumentPart().addTargetPart(ndp);
		return pkg;
	}

	private static String walk(WordprocessingMLPackage pkg, String numId) {
		StringBuilder sb = new StringBuilder();
		for (int ilvl : WALK) {
			if (sb.length() > 0) sb.append(' ');
			sb.append(Emulator.getNumber(pkg, null, numId, Integer.toString(ilvl)).getNumString());
		}
		return sb.toString();
	}

	@Test
	public void lvlRestartAsWordLabelsIt() throws Exception {
		WordprocessingMLPackage pkg = packageWith(
				abstractNum(80, 0) + abstractNum(81, 1) + abstractNum(82, null)
				+ "<w:num w:numId=\"80\"><w:abstractNumId w:val=\"80\"/></w:num>"
				+ "<w:num w:numId=\"81\"><w:abstractNumId w:val=\"81\"/></w:num>"
				+ "<w:num w:numId=\"82\"><w:abstractNumId w:val=\"82\"/></w:num>");

		// list A, w:val 0: level 2 never restarts (Word golden, verbatim)
		assertEquals("1. 1.1. 1.1.1. 1.1.2. 1.2. 1.2.3. 2. 2.1.4.", walk(pkg, "80"));
		// list B, w:val 1: only a level-0 item restarts level 2 (Word golden, verbatim)
		assertEquals("1. 1.1. 1.1.1. 1.1.2. 1.2. 1.2.3. 2. 2.1.1.", walk(pkg, "81"));
		// no w:lvlRestart: any shallower level restarts it
		assertEquals("1. 1.1. 1.1.1. 1.1.2. 1.2. 1.2.1. 2. 2.1.1.", walk(pkg, "82"));
	}

	@Test
	public void restartsAfter() {
		assertEquals(null, new NumberingRestartTest().restartOf(null).getLvlRestart());
		// no lvlRestart: every shallower level restarts a level-2 counter
		org.junit.Assert.assertTrue(restartOf(null).restartsAfter(0));
		org.junit.Assert.assertTrue(restartOf(null).restartsAfter(1));
		// 0: none does
		org.junit.Assert.assertFalse(restartOf(0).restartsAfter(0));
		org.junit.Assert.assertFalse(restartOf(0).restartsAfter(1));
		// 1: ilvl 0 only
		org.junit.Assert.assertTrue(restartOf(1).restartsAfter(0));
		org.junit.Assert.assertFalse(restartOf(1).restartsAfter(1));
		// 2: ilvl 0 and 1 (the same as none, for a level-2 counter)
		org.junit.Assert.assertTrue(restartOf(2).restartsAfter(1));
	}

	/** A level-2 ListLevel with the given w:lvlRestart. */
	private ListLevel restartOf(Integer lvlRestart) {
		org.docx4j.wml.ObjectFactory f = Context.getWmlObjectFactory();
		org.docx4j.wml.Lvl lvl = f.createLvl();
		lvl.setIlvl(java.math.BigInteger.valueOf(2));
		if (lvlRestart != null) {
			org.docx4j.wml.Lvl.LvlRestart r = f.createLvlLvlRestart();
			r.setVal(java.math.BigInteger.valueOf(lvlRestart));
			lvl.setLvlRestart(r);
		}
		return new ListLevel(lvl);
	}
}
