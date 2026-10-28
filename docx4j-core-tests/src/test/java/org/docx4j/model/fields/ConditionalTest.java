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

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/** IF, COMPARE and SET through the evaluators (CR-067 phase 2), the rules Word's run measured (section 13.2). */
public class ConditionalTest {

	static FieldContext context() throws Exception {
		FieldContext ctx = FieldContext.of(WordprocessingMLPackage.createPackage()).language("en-US");
		ctx.bookmarks().put("x", "5");
		ctx.bookmarks().put("greeting", "Hello World");
		return ctx;
	}

	static String eval(String instr, FieldContext ctx) throws Exception {
		return FieldEvaluators.evaluate(instr, ctx);
	}

	@Test
	public void ifForms() throws Exception {
		FieldContext ctx = context();
		assertEquals("yes", eval(" IF 5 > 3 \"yes\" \"no\" ", ctx));
		assertEquals("yes", eval(" IF 5 > 3 yes no ", ctx));
		assertEquals("", eval(" IF 5 = 4 yes ", ctx));
		assertEquals("multi word true", eval(" IF 5 = 5 \"multi word true\" \"multi word false\" ", ctx));
		assertEquals("yes", eval(" IF 5 <> 4 yes no ", ctx));
		assertEquals("yes", eval(" IF 5 >= 5 yes no ", ctx));
		assertEquals("no", eval(" IF 5 <= 4 yes no ", ctx));
		assertEquals(Conditional.UNKNOWN_OP, eval(" IF 5=5 yes no ", ctx));
		assertEquals(Conditional.UNKNOWN_OP, eval(" IF 5 ! 5 yes no ", ctx));
		assertEquals("yes", eval(" IF 5 > 3 yes no extra ", ctx));
		assertEquals("a", eval(" IF 5 = 5 a b c ", ctx));
		assertEquals("yes", eval(" IF \"\" = \"\" yes no ", ctx));
		assertEquals("yes", eval(" IF \"a b\" = \"a b\" yes no ", ctx));
		assertEquals("no", eval(" IF 5 = \"\" yes no ", ctx));
		assertEquals("the general switch applies", "YES", eval(" IF 5 > 3 \"yes\" \"no\" \\* Upper ", ctx));
		assertEquals("a switch is not the false text", "", eval(" IF 5 = 4 yes \\* Upper ", ctx));
		assertNull("too few operands: the cached result is kept", eval(" IF 5 ", ctx));
	}

	@Test
	public void numbersAgainstStrings() throws Exception {
		FieldContext ctx = context();
		assertEquals("yes", eval(" IF 10 > 9 yes no ", ctx));
		assertEquals("quoted numbers are strings", "no", eval(" IF \"10\" > \"9\" yes no ", ctx));
		assertEquals("yes", eval(" IF abc < abd yes no ", ctx));
		assertEquals("no", eval(" IF 5 > \"abc\" yes no ", ctx));
		assertEquals("yes", eval(" IF 5.0 = 5 yes no ", ctx));
		assertEquals("yes", eval(" IF -1 < 0 yes no ", ctx));
		assertEquals("yes", eval(" IF 1,000 > 999 yes no ", ctx));
		assertEquals("yes", eval(" IF 007 = 7 yes no ", ctx));
	}

	@Test
	public void caseAndWildcards() throws Exception {
		FieldContext ctx = context();
		assertEquals("case-sensitive", "no", eval(" IF \"abc\" = \"ABC\" yes no ", ctx));
		assertEquals("yes", eval(" IF \"abc\" = \"a*\" yes no ", ctx));
		assertEquals("yes", eval(" IF \"abc\" = \"a?c\" yes no ", ctx));
		assertEquals("no", eval(" IF \"abc\" <> \"a*\" yes no ", ctx));
		assertEquals("no wildcard for <", "no", eval(" IF \"abc\" < \"a*\" yes no ", ctx));
		assertEquals("none on the left", "no", eval(" IF \"a?c\" = \"abc\" yes no ", ctx));
		assertEquals("yes", eval(" IF \"Hello World\" = \"Hello*\" yes no ", ctx));
	}

	@Test
	public void bareBookmarkNames() throws Exception {
		FieldContext ctx = context();
		assertEquals("a bare name that is a bookmark is its text", "1", eval(" COMPARE x = 5 ", ctx));
		assertEquals("yes", eval(" IF x > 4 yes no ", ctx));
		assertEquals("quoted, it is the name", "0", eval(" COMPARE \"x\" = 5 ", ctx));
		assertEquals("1", eval(" COMPARE greeting = \"Hello*\" ", ctx));
	}

	@Test
	public void compare() throws Exception {
		FieldContext ctx = context();
		assertEquals("1", eval(" COMPARE 5 > 3 ", ctx));
		assertEquals("1", eval(" COMPARE 10 > 9 ", ctx));
		assertEquals("0", eval(" COMPARE \"10\" > \"9\" ", ctx));
		assertEquals("1", eval(" COMPARE abc < abd ", ctx));
		assertEquals("0", eval(" COMPARE \"abc\" = \"ABC\" ", ctx));
		assertEquals("1", eval(" COMPARE \"abc\" = \"a*\" ", ctx));
		assertEquals("0", eval(" COMPARE \"a?c\" = \"abc\" ", ctx));
		assertEquals("0", eval(" COMPARE 5 > \"abc\" ", ctx));
		assertEquals("1", eval(" COMPARE 5 = 5.0 ", ctx));
		assertEquals("1", eval(" COMPARE 1,000 = 1000 ", ctx));
		assertEquals("1", eval(" COMPARE \"\" = \"\" ", ctx));
		assertEquals("0", eval(" COMPARE 5 <= 4 ", ctx));
		assertEquals(Conditional.UNKNOWN_OP, eval(" COMPARE 5 ! 4 ", ctx));
		assertNull(eval(" COMPARE 5 ", ctx));
	}

	@Test
	public void setDefinesABookmarkTheOthersRead() throws Exception {
		FieldContext ctx = context();
		assertEquals("42", eval(" SET total 42 ", ctx));
		assertEquals("42", ctx.bookmarks().get("total"));
		assertEquals("42", eval(" REF total ", ctx));
		assertEquals("43", eval(" =total+1 ", ctx));
		assertEquals("yes", eval(" IF total > 40 yes no ", ctx));
		assertEquals("1", eval(" =DEFINED(total) ", ctx));
		assertEquals("Hello World", eval(" SET greeting2 \"Hello World\" ", ctx));
		assertEquals("Hello World", ctx.bookmarks().get("greeting2"));
		assertEquals("", eval(" SET empty ", ctx));
		assertNull(eval(" SET ", ctx));
	}

	@Test
	public void aNestedResultAsAnOperand() {
		assertEquals("5", FieldContext.asOperand("5"));
		assertEquals("\"Hello World\"", FieldContext.asOperand("Hello World"));
		assertEquals("\"\"", FieldContext.asOperand(""));
		assertEquals("\"\"", FieldContext.asOperand(null));
		assertEquals("\"say \\\"hi\\\"\"", FieldContext.asOperand("say \"hi\""));
	}
}
