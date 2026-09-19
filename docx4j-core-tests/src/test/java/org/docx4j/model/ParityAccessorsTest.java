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
package org.docx4j.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.docx4j.fonts.FontsAnalysis;
import org.docx4j.jaxb.Context;
import org.docx4j.model.listnumbering.Emulator;
import org.docx4j.model.listnumbering.ListLevel;
import org.docx4j.model.listnumbering.NumberingState;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.Style;
import org.docx4j.wml.TblPr;
import org.junit.Test;

/**
 * The four public accessors the parity harnesses (docx4j-core-ts, docx4j-python) asked for
 * on 2026-09-19, so that a port can be held to docx4j's answers without reflection:
 * {@link Emulator#numRefFor}, {@link NumberingState#counters()} with the
 * {@link ListLevel.Counter} getters, {@link PropertyResolver#reachesDefaultTableStyle} with
 * a public {@link PropertyResolver#ancestry}, and {@link FontsAnalysis#NO_OP_VISITOR}.
 *
 * @since 17.1.1
 */
public class ParityAccessorsTest {

	private static final ObjectFactory F = Context.getWmlObjectFactory();

	/** Normal is numbered (numId 1, decimal); "Unlinked" is based on nothing. */
	private static WordprocessingMLPackage packageWithNumberedNormal() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		pkg.getMainDocumentPart().addTargetPart(ndp);
		ndp.unmarshalDefaultNumbering();
		Style normal = pkg.getMainDocumentPart().getStyleDefinitionsPart().getDefaultParagraphStyle();
		if (normal.getPPr() == null) normal.setPPr(F.createPPr());
		normal.getPPr().setNumPr(numPr(1, null));
		Style unlinked = F.createStyle();
		unlinked.setType("paragraph");
		unlinked.setStyleId("Unlinked");
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add(unlinked);
		return pkg;
	}

	private static PPrBase.NumPr numPr(int numId, Integer ilvl) {
		PPrBase.NumPr np = F.createPPrBaseNumPr();
		PPrBase.NumPr.NumId id = F.createPPrBaseNumPrNumId();
		id.setVal(BigInteger.valueOf(numId));
		np.setNumId(id);
		if (ilvl != null) {
			PPrBase.NumPr.Ilvl lvl = F.createPPrBaseNumPrIlvl();
			lvl.setVal(BigInteger.valueOf(ilvl));
			np.setIlvl(lvl);
		}
		return np;
	}

	private static PPr styled(String styleId) {
		PPr pPr = F.createPPr();
		PPrBase.PStyle ps = F.createPPrBasePStyle();
		ps.setVal(styleId);
		pPr.setPStyle(ps);
		return pPr;
	}

	@Test
	public void numRefForReportsADirectReferenceWithoutTakingANumber() throws Exception {
		WordprocessingMLPackage pkg = packageWithNumberedNormal();
		PPr pPr = F.createPPr();
		pPr.setNumPr(numPr(1, 1));
		Emulator.NumRef ref = Emulator.numRefFor(pkg, pPr);
		assertFalse(ref.notNumbered);
		assertTrue("the numId is the paragraph's own", ref.direct);
		assertEquals("1", ref.numId);
		assertEquals("1", ref.ilvl);
		NumberingState state = pkg.getMainDocumentPart().getNumberingDefinitionsPart().getNumberingState();
		assertTrue("resolving takes no number", state.counters().isEmpty());
		assertNotNull("and getNumber then takes one", Emulator.getNumber(pkg, pPr).getNumString());
	}

	@Test
	public void numRefForReportsTheStyleAsTheSourceAndWhyAParagraphIsNotNumbered() throws Exception {
		WordprocessingMLPackage pkg = packageWithNumberedNormal();
		Emulator.NumRef viaNormal = Emulator.numRefFor(pkg, F.createPPr());
		assertFalse(viaNormal.notNumbered);
		assertFalse("the numId comes from the default paragraph style", viaNormal.direct);
		assertEquals("1", viaNormal.numId);
		assertEquals("0", viaNormal.ilvl);

		Emulator.NumRef unlinked = Emulator.numRefFor(pkg, styled("Unlinked"));
		assertTrue(unlinked.notNumbered);
		assertNotNull(unlinked.reason);
		assertTrue(unlinked.reason, unlinked.reason.contains("Unlinked"));

		assertTrue(Emulator.numRefFor(pkg, null).notNumbered);
		WordprocessingMLPackage noNumbering = WordprocessingMLPackage.createPackage();
		Emulator.NumRef none = Emulator.numRefFor(noNumbering, F.createPPr());
		assertTrue(none.notNumbered);
		assertEquals("no numbering part", none.reason);
	}

	@Test
	public void numIdZeroTurnsNumberingOff() throws Exception {
		// ECMA-376 17.9.18: w:numId 0 designates the removal of numbering, never a definition
		WordprocessingMLPackage pkg = packageWithNumberedNormal();
		PPr pPr = F.createPPr();
		pPr.setNumPr(numPr(0, null));
		Emulator.NumRef ref = Emulator.numRefFor(pkg, pPr);
		assertTrue(ref.notNumbered);
		assertTrue(ref.reason, ref.reason.contains("numId 0"));
		assertNull("Normal is numbered, but the paragraph's numId 0 switches it off", Emulator.getNumber(pkg, pPr));
	}

	@Test
	public void danglingNumIdIsNotNumberedAndSaysWhy() throws Exception {
		// a w:numId naming no w:num: Word's re-save leaves one in an untouched mc:Fallback
		// after renumbering (CR-021 §8.5); the accessor's contract is a reason, not an
		// empty result
		WordprocessingMLPackage pkg = packageWithNumberedNormal();
		PPr pPr = F.createPPr();
		pPr.setNumPr(numPr(99, null));
		Emulator.NumRef ref = Emulator.numRefFor(pkg, pPr);
		assertTrue(ref.notNumbered);
		assertTrue(ref.reason, ref.reason.contains("no w:num for numId 99"));
		assertNull(Emulator.getNumber(pkg, pPr));
	}

	@Test
	public void missingLevelIsNotNumberedAndSaysWhy() throws Exception {
		// the w:num exists, its definition has no w:lvl for the paragraph's ilvl
		WordprocessingMLPackage pkg = packageWithNumberedNormal();
		PPr pPr = F.createPPr();
		pPr.setNumPr(numPr(1, 42));
		Emulator.NumRef ref = Emulator.numRefFor(pkg, pPr);
		assertTrue(ref.notNumbered);
		assertTrue(ref.reason, ref.reason.contains("no w:lvl 42 in w:num 1"));
		assertNull(Emulator.getNumber(pkg, pPr));
	}

	@Test
	public void countersAreReadableAndReadOnly() throws Exception {
		WordprocessingMLPackage pkg = packageWithNumberedNormal();
		NumberingState state = new NumberingState();
		PPr pPr = F.createPPr();
		pPr.setNumPr(numPr(1, 0));
		assertTrue(state.counters().isEmpty());
		Emulator.getNumber(pkg, pPr, state);
		Emulator.getNumber(pkg, pPr, state);
		Map<String, ListLevel.Counter> counters = state.counters();
		// the abstract list's counters are created together, one per level; only level 0 was used
		assertFalse(counters.isEmpty());
		ListLevel.Counter used = null;
		for (Map.Entry<String, ListLevel.Counter> e : counters.entrySet()) {
			if (e.getKey().endsWith("/0")) used = e.getValue();
			else assertFalse(e.getKey() + " unused", e.getValue().isEncounteredAlready());
		}
		assertNotNull("the level 0 counter", used);
		assertEquals(BigInteger.valueOf(2), used.getCurrentValue());
		assertTrue(used.isEncounteredAlready());
		assertFalse(used.isResetPending());
		// keyed numId/ilvl; whether the first use of a w:num records an override is the emulator's business
		for (String key : state.startOverridesApplied()) assertTrue(key, key.startsWith("1/"));
		try {
			state.startOverridesApplied().clear();
			fail("the set is read-only");
		} catch (UnsupportedOperationException expected) {
			// the set is a view
		}
		try {
			counters.clear();
			fail("the view is read-only");
		} catch (UnsupportedOperationException expected) {
			// the map is a view
		}
		assertFalse(state.counters().isEmpty());
	}

	@Test
	public void reachesDefaultTableStyleAndAncestryArePublic() throws Exception {
		WordprocessingMLPackage pkg = packageWithNumberedNormal();
		PropertyResolver resolver = pkg.getMainDocumentPart().getPropertyResolver();
		Style tableNormal = pkg.getMainDocumentPart().getStyleDefinitionsPart().getDefaultTableStyle();
		assertNotNull(tableNormal);

		assertTrue("a table naming no style", resolver.reachesDefaultTableStyle(null));

		TblPr onDefault = F.createTblPr();
		org.docx4j.wml.CTTblPrBase.TblStyle ts = F.createCTTblPrBaseTblStyle();
		ts.setVal(tableNormal.getStyleId());
		onDefault.setTblStyle(ts);
		assertTrue("a chain reaching the default table style", resolver.reachesDefaultTableStyle(onDefault));

		Style detached = F.createStyle();
		detached.setType("table");
		detached.setStyleId("Detached");
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add(detached);
		resolver.refresh();
		TblPr onDetached = F.createTblPr();
		org.docx4j.wml.CTTblPrBase.TblStyle ts2 = F.createCTTblPrBaseTblStyle();
		ts2.setVal("Detached");
		onDetached.setTblStyle(ts2);
		assertFalse("a chain based on nothing", resolver.reachesDefaultTableStyle(onDetached));

		List<Style> chain = resolver.ancestry("Detached");
		assertEquals(1, chain.size());
		assertEquals("Detached", chain.get(0).getStyleId());
		assertTrue(resolver.ancestry(null).isEmpty());
	}

	@Test
	public void theNoOpVisitorIsPublic() {
		assertNotNull(FontsAnalysis.NO_OP_VISITOR);
		assertTrue(FontsAnalysis.NO_OP_VISITOR.isReusable());
		assertNull(FontsAnalysis.NO_OP_VISITOR.getResult());
	}
}
