package org.docx4j.model.listnumbering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.math.BigInteger;

import org.docx4j.wml.Lvl;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.PPrBase.NumPr;
import org.junit.Test;

public class NumberingLevelNullSafetyTest {

	@Test
	public void directListLevelConstructionDefaultsMissingIdToZero() {
		Lvl level = new Lvl();

		ListLevel listLevel = new ListLevel(level);

		assertEquals(BigInteger.ZERO, level.getIlvl());
		assertEquals("0", listLevel.getID());
	}

	@Test
	public void missingAbstractLevelIdUsesFirstAvailableLevel() {
		Numbering.AbstractNum abstractNum = new Numbering.AbstractNum();
		abstractNum.setAbstractNumId(BigInteger.ZERO);

		Lvl missingLevelId = new Lvl();
		Lvl levelZero = new Lvl();
		levelZero.setIlvl(BigInteger.ZERO);
		abstractNum.getLvl().add(missingLevelId);
		abstractNum.getLvl().add(levelZero);

		AbstractListNumberingDefinition definition = new AbstractListNumberingDefinition(abstractNum);

		assertEquals(BigInteger.ONE, missingLevelId.getIlvl());
		assertSame(missingLevelId, definition.getListLevels().get("1").getJaxbAbstractLvl());
		assertSame(levelZero, definition.getListLevels().get("0").getJaxbAbstractLvl());
	}

	@Test
	public void linkedStyleLevelDoesNotOverwriteExistingLevel() {
		Numbering.AbstractNum abstractNum = new Numbering.AbstractNum();
		abstractNum.setAbstractNumId(BigInteger.ZERO);
		Numbering.AbstractNum.NumStyleLink numStyleLink = new Numbering.AbstractNum.NumStyleLink();
		numStyleLink.setVal("LinkedListStyle");
		abstractNum.setNumStyleLink(numStyleLink);

		Lvl levelZero = new Lvl();
		levelZero.setIlvl(BigInteger.ZERO);
		abstractNum.getLvl().add(levelZero);

		AbstractListNumberingDefinition definition = new AbstractListNumberingDefinition(abstractNum);
		Numbering.AbstractNum linkedAbstractNum = new Numbering.AbstractNum();
		linkedAbstractNum.setAbstractNumId(BigInteger.ONE);
		Lvl linkedLevelWithoutId = new Lvl();
		linkedAbstractNum.getLvl().add(linkedLevelWithoutId);

		definition.updateDefinitionFromLinkedStyle(linkedAbstractNum);

		assertEquals(BigInteger.ONE, linkedLevelWithoutId.getIlvl());
		assertSame(levelZero, definition.getListLevels().get("0").getJaxbAbstractLvl());
		assertSame(linkedLevelWithoutId, definition.getListLevels().get("1").getJaxbAbstractLvl());
	}

	@Test
	public void missingParagraphLevelDefaultsToZero() {
		NumPr numPr = new NumPr();

		assertNull(Emulator.getExplicitIlvl(numPr));
		assertEquals(BigInteger.ZERO, Emulator.getIlvlOrDefault(numPr));
	}

	@Test
	public void paragraphLevelWithoutValueDefaultsToZero() {
		NumPr numPr = new NumPr();
		numPr.setIlvl(new NumPr.Ilvl());

		assertNull(Emulator.getExplicitIlvl(numPr));
		assertEquals(BigInteger.ZERO, Emulator.getIlvlOrDefault(numPr));
	}
}
