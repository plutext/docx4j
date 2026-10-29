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
package org.docx4j.model.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.CTSettings;
import org.junit.Test;

/**
 * The {@code =} formula evaluator (CR-067 phase 2) on its own: the rules Word's run measured (section 13.2) and the
 * ones a document's settings change (its decimal symbol and list separator).
 */
public class FormulaTest {

	static FieldContext context() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		FieldContext ctx = FieldContext.of(pkg).language("en-US");
		ctx.bookmarks().put("x", "5");
		ctx.bookmarks().put("words", "abc");
		return ctx;
	}

	static String eval(String instr, FieldContext ctx) throws Exception {
		return FieldEvaluators.evaluate(instr, ctx);
	}

	@Test
	public void operatorsAndTheDefaultFormat() throws Exception {
		FieldContext ctx = context();
		assertEquals("14", eval(" =2+3*4 ", ctx));
		assertEquals("20", eval(" =(2+3)*4 ", ctx));
		assertEquals("2.5", eval(" =10/4 ", ctx));
		assertEquals("0.33", eval(" =1/3 ", ctx));
		assertEquals("0.67", eval(" =2/3 ", ctx));
		assertEquals("1024", eval(" =2^10 ", ctx));
		assertEquals("-3", eval(" =-5+2 ", ctx));
		assertEquals("0.5", eval(" =50% ", ctx));
		assertEquals("-6", eval(" =2*-3 ", ctx));
		assertEquals("6", eval(" = 5 + 1 ", ctx));
		assertEquals("1000000000000", eval(" =1000000*1000000 ", ctx));
		assertEquals("1", eval(" =3>2 ", ctx));
		assertEquals("0", eval(" =3=2 ", ctx));
		assertEquals("8", eval(" =007+1 ", ctx));
		assertEquals("2.5", eval(" =1.5+1 ", ctx));
		assertEquals("the result echoes the thousands separator", "1,001", eval(" =1,000+1 ", ctx));
		assertEquals("0", eval(" =0.001 ", ctx));
		assertEquals("-0.5", eval(" =-1/2 ", ctx));
	}

	@Test
	public void theSwitches() throws Exception {
		FieldContext ctx = context();
		assertEquals("10.00", eval(" =10 \\# \"0.00\" ", ctx));
		assertEquals("from the unrounded value", "0.333", eval(" =1/3 \\# \"0.000\" ", ctx));
		assertEquals("V", eval(" =x \\* Roman ", ctx)); // a general switch after the numeric format
	}

	@Test
	public void theErrorTexts() throws Exception {
		FieldContext ctx = context();
		assertEquals("!Zero Divide", eval(" =1/0 ", ctx));
		assertEquals("!Unexpected End of Formula", eval(" =1+ ", ctx));
		assertEquals("!Unexpected End of Formula", eval(" = ", ctx));
		assertEquals("!Missing Operator", eval(" =1 2 ", ctx));
		assertEquals("!Syntax Error, ,", eval(" =1,5+1 ", ctx));
		assertEquals("!Undefined Bookmark, NOPE", eval(" =nope ", ctx));
		assertEquals("!Syntax Error, 1", eval(" =NOPE(1) ", ctx));
		assertEquals("!Zero Divide", eval(" =MOD(1,0) ", ctx));
	}

	@Test
	public void theFunctions() throws Exception {
		FieldContext ctx = context();
		assertEquals("3", eval(" =ABS(-3) ", ctx));
		assertEquals("3", eval(" =INT(3.7) ", ctx));
		assertEquals("toward zero", "-3", eval(" =INT(-3.7) ", ctx));
		assertEquals("2.35", eval(" =ROUND(2.345,2) ", ctx));
		assertEquals("3", eval(" =ROUND(2.5,0) ", ctx));
		assertEquals("half away from zero", "-3", eval(" =ROUND(-2.5,0) ", ctx));
		assertEquals("1200", eval(" =ROUND(1234.5,-2) ", ctx));
		assertEquals("1", eval(" =MOD(7,3) ", ctx));
		assertEquals("the dividend's sign", "-1", eval(" =MOD(-7,3) ", ctx));
		assertEquals("-1", eval(" =SIGN(-5) ", ctx));
		assertEquals("6", eval(" =SUM(1,2,3) ", ctx));
		assertEquals("2.33", eval(" =AVERAGE(1,2,4) ", ctx));
		assertEquals("2", eval(" =MIN(4,2,8) ", ctx));
		assertEquals("8", eval(" =MAX(4,2,8) ", ctx));
		assertEquals("3", eval(" =COUNT(1,2,3) ", ctx));
		assertEquals("24", eval(" =PRODUCT(2,3,4) ", ctx));
		assertEquals("0", eval(" =AND(1,0) ", ctx));
		assertEquals("1", eval(" =OR(1,0) ", ctx));
		assertEquals("1", eval(" =NOT(0) ", ctx));
		assertEquals("1", eval(" =TRUE ", ctx));
		assertEquals("0", eval(" =FALSE ", ctx));
		assertEquals("1", eval(" =TRUE() ", ctx));
		assertEquals("10", eval(" =IF(3>2,10,20) ", ctx));
		assertEquals("2", eval(" =ROUND(AVERAGE(1,2),0) ", ctx));
		assertEquals("case does not matter", "6", eval(" =sum(1,2,3) ", ctx));
	}

	@Test
	public void bookmarks() throws Exception {
		FieldContext ctx = context();
		assertEquals("10", eval(" =x*2 ", ctx));
		assertEquals("8", eval(" =3 + x ", ctx));
		assertEquals("6", eval(" =SUM(x,1) ", ctx));
		assertEquals("1", eval(" =DEFINED(x) ", ctx));
		assertEquals("0", eval(" =DEFINED(nope) ", ctx));
		assertEquals("a bookmark holding no number reads as 0 (unmeasured)", "1", eval(" =words+1 ", ctx));
	}

	@Test
	public void cellReferencesNeedCells() throws Exception {
		FieldContext ctx = context();
		assertNull("no cells: the cached result is kept", eval(" =SUM(ABOVE) ", ctx));
		assertNull(eval(" =A1+B2 ", ctx));
		assertNull(eval(" =SUM(A1:B2) ", ctx));
		ctx.cells(new FieldContext.Cells() {
			final Double[][] v = { { 1.0, 10.0, null }, { 2.0, 20.0, null }, { 3.0, 30.0, 5.0 } };
			public Double value(int row, int col) { return row < 0 || row >= 3 || col < 0 || col >= 3 ? null : v[row][col]; }
			public int rowCount() { return 3; }
			public int colCount(int row) { return 3; }
			public int row() { return 2; }
			public int col() { return 2; }
		});
		assertEquals("ABOVE: the text and empty cells skipped", "0", eval(" =SUM(ABOVE) ", ctx));
		assertEquals("33", eval(" =SUM(LEFT) ", ctx));
		assertEquals("21", eval(" =A1+B2 ", ctx));
		assertEquals("33", eval(" =SUM(A1:B2) ", ctx));
		assertEquals("3", eval(" =MAX(A1:A3) ", ctx));
		assertEquals("1", eval(" =COUNT(C1:C3) ", ctx));
		assertEquals("20", eval(" =AVERAGE(B1:B3) ", ctx));
	}

	@Test
	public void theDocumentsDecimalSymbolAndListSeparator() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		DocumentSettingsPart dsp = new DocumentSettingsPart();
		CTSettings settings = Context.getWmlObjectFactory().createCTSettings();
		CTSettings.DecimalSymbol decimal = new CTSettings.DecimalSymbol();
		decimal.setVal(",");
		settings.setDecimalSymbol(decimal);
		CTSettings.ListSeparator list = new CTSettings.ListSeparator();
		list.setVal(";");
		settings.setListSeparator(list);
		dsp.setJaxbElement(settings);
		pkg.getMainDocumentPart().addTargetPart(dsp);
		FieldContext ctx = FieldContext.of(pkg).language("de-DE");
		assertEquals(',', ctx.numberSymbols().decimal);
		assertEquals('.', ctx.numberSymbols().grouping);
		assertEquals(';', ctx.numberSymbols().list);
		assertEquals("2,5", eval(" =1,5+1 ", ctx));
		assertEquals("6", eval(" =SUM(1;2;3) ", ctx));
		assertEquals("1.001", eval(" =1.000+1 ", ctx));
		assertEquals("0,33", eval(" =1/3 ", ctx));
		assertTrue(Formula.number("1.000,5", ctx.numberSymbols()) == 1000.5);
		assertNull(Formula.number("1.0.0", ctx.numberSymbols()));
	}

	@Test
	public void numbers() {
		Formula.Symbols s = Formula.Symbols.DEFAULT;
		assertEquals(5.0, Formula.number("5", s), 0);
		assertEquals(5.0, Formula.number(" 5.0 ", s), 0);
		assertEquals(7.0, Formula.number("007", s), 0);
		assertEquals(1000.0, Formula.number("1,000", s), 0);
		assertEquals(-1.5, Formula.number("-1.5", s), 0);
		assertEquals(0.5, Formula.number(".5", s), 0);
		assertNull(Formula.number("1,5", s));
		assertNull(Formula.number("abc", s));
		assertNull(Formula.number("5 apples", s));
		assertNull(Formula.number("", s));
	}
}
