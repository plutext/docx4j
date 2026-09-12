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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigInteger;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CTTblCellMar;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.Style;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.junit.Before;
import org.junit.Test;

/**
 * Which table style underlies a table, per the CR-015 probe styles-table-default (Word,
 * with the document's Table Normal stating a 300-twip left margin): a table naming no
 * style and one whose chain reaches the default table style get Word's BUILT-IN Normal
 * Table (108 left and right), not the document's definition; a style whose chain does not
 * reach it gets no cell margin at all.
 *
 * @since 17.1.1
 */
public class PropertyResolverTableStyleTest {

	private static final org.docx4j.wml.ObjectFactory F = Context.getWmlObjectFactory();

	private WordprocessingMLPackage pkg;
	private Style tableNormal;

	@Before
	public void setUp() throws Exception {
		pkg = WordprocessingMLPackage.createPackage();
		tableNormal = pkg.getMainDocumentPart().getStyleDefinitionsPart().getDefaultTableStyle();
		assertNotNull("the template's default table style", tableNormal);
		// the document says 300; Word applies its built-in 108 regardless (probe (a), (c))
		tableNormal.getTblPr().getTblCellMar().getLeft().setW(BigInteger.valueOf(300));

		tableStyle("Custom", null, false);                           // no w:basedOn (probe (b))
		tableStyle("Grid2", tableNormal.getStyleId(), true);          // based on Table Normal (probe (c))
		tableStyle("Grid3", "Grid2", false);                          // reaches it through Grid2
	}

	@Test
	public void aTableNamingNoStyleGetsTheBuiltInNormalTable() throws Exception {
		Style effective = new PropertyResolver(pkg).getEffectiveTableStyle(F.createTblPr());
		assertEquals(108, left(effective));
		assertEquals(108, right(effective));
		assertEquals(0, effective.getTblPr().getTblInd().getW().intValue());
		assertEquals("null tblPr too", 108, left(new PropertyResolver(pkg).getEffectiveTableStyle(null)));
	}

	@Test
	public void aStyleBasedOnTheDefaultGetsTheBuiltInNotTheDocumentsDefinition() throws Exception {
		Style effective = new PropertyResolver(pkg).getEffectiveTableStyle(tblPr("Grid2"));
		assertEquals("108, not the document's 300 (golden (c))", 108, left(effective));
		assertNotNull("the style's own borders are carried", effective.getTblPr().getTblBorders());
		assertEquals(108, left(new PropertyResolver(pkg).getEffectiveTableStyle(tblPr("Grid3"))));
	}

	@Test
	public void aStyleWhoseChainDoesNotReachTheDefaultHasNoCellMargin() throws Exception {
		Style effective = new PropertyResolver(pkg).getEffectiveTableStyle(tblPr("Custom"));
		assertNotNull(effective.getTblPr());
		assertNull("no margin at all (golden (b): text at the border)", effective.getTblPr().getTblCellMar());
	}

	@Test
	public void aMissingTableStyleIsTheBuiltIn() throws Exception {
		assertEquals(108, left(new PropertyResolver(pkg).getEffectiveTableStyle(tblPr("Nope"))));
	}

	@Test
	public void theTablesOwnPropertiesWinAndTheDocumentIsNotTouched() throws Exception {
		TblPr own = tblPr("Grid2");
		CTTblCellMar mar = F.createCTTblCellMar();
		mar.setLeft(twips(50));
		own.setTblCellMar(mar);
		Style effective = new PropertyResolver(pkg).getEffectiveTableStyle(own);
		assertEquals(50, left(effective));
		assertEquals("the right margin still the built-in's", 108, right(effective));
		assertEquals("the document's Table Normal untouched", 300, tableNormal.getTblPr().getTblCellMar().getLeft().getW().intValue());
	}

	// ---------------------------------------------------------------- helpers

	private static int left(Style s) {
		return s.getTblPr().getTblCellMar().getLeft().getW().intValue();
	}

	private static int right(Style s) {
		return s.getTblPr().getTblCellMar().getRight().getW().intValue();
	}

	private static TblWidth twips(int w) {
		TblWidth t = F.createTblWidth();
		t.setType("dxa");
		t.setW(BigInteger.valueOf(w));
		return t;
	}

	private static TblPr tblPr(String styleId) {
		TblPr t = F.createTblPr();
		CTTblPrBase.TblStyle ts = F.createCTTblPrBaseTblStyle();
		ts.setVal(styleId);
		t.setTblStyle(ts);
		return t;
	}

	private void tableStyle(String styleId, String basedOn, boolean borders) {
		Style s = F.createStyle();
		s.setType("table");
		s.setStyleId(styleId);
		Style.Name n = F.createStyleName();
		n.setVal(styleId);
		s.setName(n);
		if (basedOn != null) {
			Style.BasedOn b = F.createStyleBasedOn();
			b.setVal(basedOn);
			s.setBasedOn(b);
		}
		CTTblPrBase tblPr = F.createCTTblPrBase();
		tblPr.setTblInd(twips(0));
		if (borders) {
			TblBorders tb = F.createTblBorders();
			org.docx4j.wml.CTBorder top = F.createCTBorder();
			top.setVal(STBorder.SINGLE);
			tb.setTop(top);
			tblPr.setTblBorders(tb);
		}
		s.setTblPr(tblPr);
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add(s);
	}
}
