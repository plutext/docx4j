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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.Style;
import org.junit.Test;

/**
 * The default paragraph style carries a w:numPr (CR-014 probe P6: Word numbers
 * paragraphs naming no style 1 2 3 through it).  Before 17.1.1
 * Emulator.getNumber(pkg, pPr) assumed the default style unnumbered.
 */
public class DefaultStyleNumberedTest {

	private static final ObjectFactory F = Context.getWmlObjectFactory();

	private static WordprocessingMLPackage packageWithNumberedNormal() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		pkg.getMainDocumentPart().addTargetPart(ndp);
		ndp.unmarshalDefaultNumbering(); // numId 1: decimal
		Style normal = pkg.getMainDocumentPart().getStyleDefinitionsPart().getDefaultParagraphStyle();
		if (normal.getPPr() == null) normal.setPPr(F.createPPr());
		normal.getPPr().setNumPr(numPr(1));
		// a style based on nothing, so it inherits no numbering
		Style unlinked = F.createStyle();
		unlinked.setType("paragraph");
		unlinked.setStyleId("Unlinked");
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add(unlinked);
		return pkg;
	}

	private static PPrBase.NumPr numPr(int numId) {
		PPrBase.NumPr np = F.createPPrBaseNumPr();
		PPrBase.NumPr.NumId id = F.createPPrBaseNumPrNumId();
		id.setVal(BigInteger.valueOf(numId));
		np.setNumId(id);
		return np;
	}

	@Test
	public void paragraphsNamingNoStyleAreNumbered() throws Exception {
		WordprocessingMLPackage pkg = packageWithNumberedNormal();
		PPr noStyle = F.createPPr();
		assertEquals("1.", Emulator.getNumber(pkg, noStyle).getNumString());
		assertEquals("2.", Emulator.getNumber(pkg, noStyle).getNumString());
		assertEquals("3.", Emulator.getNumber(pkg, noStyle).getNumString());
	}

	@Test
	public void aStyleBasedOnNothingIsNot() throws Exception {
		WordprocessingMLPackage pkg = packageWithNumberedNormal();
		PPr pPr = F.createPPr();
		PPrBase.PStyle ps = F.createPPrBasePStyle();
		ps.setVal("Unlinked");
		pPr.setPStyle(ps);
		assertNull(Emulator.getNumber(pkg, pPr));
		assertNull(Emulator.getInd(pkg, "Unlinked", null, null));
	}

	@Test
	public void resolveSaysWhere() throws Exception {
		WordprocessingMLPackage pkg = packageWithNumberedNormal();
		NumberingDefinitionsPart ndp = pkg.getMainDocumentPart().getNumberingDefinitionsPart();
		Emulator.NumRef ref = Emulator.resolve(ndp, pkg.getMainDocumentPart().getPropertyResolver(),
				null, null, null, false);
		assertFalse(ref.notNumbered);
		assertEquals("1", ref.numId);
		assertEquals("0", ref.ilvl);
		assertFalse(ref.direct);

		ref = Emulator.resolve(ndp, pkg.getMainDocumentPart().getPropertyResolver(), "Unlinked", null, null, false);
		assertTrue(ref.notNumbered);

		ref = Emulator.resolve(ndp, pkg.getMainDocumentPart().getPropertyResolver(), "Unlinked", "1", "2", true);
		assertFalse(ref.notNumbered);
		assertTrue(ref.direct);
		assertEquals("2", ref.ilvl);
	}
}
