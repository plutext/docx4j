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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * FieldInstruction, the one parser of a field instruction (CR-067 D2): the name, the parameters as
 * Word tokenises them, the argument and the switches; and that it reads every instruction exactly as
 * the FldSimpleModel it replaces did.
 */
public class FieldInstructionTest {

	/** The shapes Word writes, including ones it splits across runs and the fixture corpus holds. */
	static final List<String> SHAPES = Arrays.asList(
			" DATE \\@ \"d MMMM yyyy\" ",
			"DATE",
			" TIME \\@ \"HH:mm\" \\* MERGEFORMAT ",
			" MERGEFIELD \"First Name\" \\* Upper ",
			" MERGEFIELD name \\* MERGEFORMAT ",
			"MERGEFIELD  Nachname ",
			" DOCPROPERTY \"My Prop\" \\* CharFormat ",
			"DOCPROPERTY client",
			" DOCVARIABLE v1 ",
			"=SUM(ABOVE) \\# \"#,##0.00\"",
			"= 1 + 2",
			" PAGEREF _Toc123456 \\h ",
			" REF bookmark1 \\h \\* MERGEFORMAT ",
			" SEQ Figure \\* ARABIC ",
			" SEQ Table \\* ARABIC \\r 3 ",
			" SEQ Figure \\c ",
			" HYPERLINK \"http://example.com/x\" \\l \"anchor\" \\o \"a tip\" ",
			" HYPERLINK \\l \"_Toc1\" ",
			" PAGE \\* MERGEFORMAT ",
			" NUMPAGES ",
			" TOC \\o \"1-3\" \\h \\z \\u ",
			" STYLEREF 1 \\s ",
			" AUTHOR \"Jo Bloggs\" ",
			" TITLE ",
			" QUOTE \"hello world\" ",
			" IF { MERGEFIELD x } = \"1\" \"yes\" \"no\" ",
			" FORMTEXT ",
			" INCLUDEPICTURE \"C:\\\\pic.png\" \\d ",
			"",
			"   ",
			"\\* MERGEFORMAT");

	@Test
	public void nameArgumentAndSwitches() {
		FieldInstruction i = FieldInstruction.parse(" DATE \\@ \"d MMMM yyyy\" ");
		assertEquals("DATE", i.getName());
		assertEquals("\\@ \"d MMMM yyyy\"", i.getParameterText());
		assertEquals(Arrays.asList("\\@", "\"d MMMM yyyy\""), i.getParameters());
		assertNull("a switch is not the argument", i.getArgument());
		assertTrue(i.hasSwitch("\\@"));
		assertEquals("d MMMM yyyy", i.getSwitchValue("\\@"));
		assertNull(i.getSwitchValue("\\#"));

		i = FieldInstruction.parse(" MERGEFIELD \"First Name\" \\* Upper ");
		assertEquals("MERGEFIELD", i.getName());
		assertEquals("the quotes come off the argument", "First Name", i.getArgument());
		assertEquals("\"First Name\"", i.getParameters().get(0));
		assertEquals("Upper", i.getSwitchValue("\\*"));

		i = FieldInstruction.parse("mergefield name \\* MERGEFORMAT");
		assertEquals("the name is upper-cased", "MERGEFIELD", i.getName());
		assertTrue(i.hasSwitch("\\*"));
		assertEquals("MERGEFORMAT is not a format: the switch is present with no value", "", i.getSwitchValue("\\*"));
		assertEquals(Arrays.asList("MERGEFORMAT"), i.getSwitchValues("\\*"));

		i = FieldInstruction.parse(" SEQ Table \\* ARABIC \\r 3 ");
		assertEquals("Table", i.getArgument());
		assertEquals("ARABIC", i.getSwitchValue("\\*"));
		assertEquals("3", i.getSwitchValue("\\r"));
		assertEquals(1, i.indexOfSwitch("\\*", 0));
		assertEquals(-1, i.indexOfSwitch("\\*", 2));
		assertEquals("ARABIC", i.valueAt(2));
		assertNull("a switch is never a value", i.valueAt(3));
		assertNull(i.valueAt(99));

		i = FieldInstruction.parse(" PAGEREF _Toc123456 \\h ");
		assertEquals("_Toc123456", i.getArgument());
		assertTrue(i.hasSwitch("\\h"));
		assertEquals("a switch with no value", "", i.getSwitchValue("\\h"));
		assertFalse(i.hasSwitch("\\H"));
	}

	@Test
	public void aFormulaIsNamedByItsEqualsSign() {
		FieldInstruction i = FieldInstruction.parse("=SUM(ABOVE) \\# \"#,##0.00\"");
		assertEquals("=", i.getName());
		assertEquals("SUM(ABOVE) \\# \"#,##0.00\"", i.getParameterText());
		assertEquals("SUM(ABOVE)", i.getArgument());
		assertEquals("#,##0.00", i.getSwitchValue("\\#"));
		assertEquals("=", FieldInstruction.parse("= 1 + 2").getName());
	}

	@Test
	public void quotedParametersKeepTheirQuotesAndTheirSpaces() {
		FieldInstruction i = FieldInstruction.parse(" HYPERLINK \"http://example.com/x\" \\l \"anchor\" \\o \"a tip\" ");
		assertEquals(Arrays.asList("\"http://example.com/x\"", "\\l", "\"anchor\"", "\\o", "\"a tip\""), i.getParameters());
		assertEquals("http://example.com/x", i.getArgument());
		assertEquals("anchor", i.getSwitchValue("\\l"));
		assertEquals("a tip", i.getSwitchValue("\\o"));
		assertEquals(Arrays.asList("\"a b\"", "c"), FieldInstruction.splitParameters("\"a b\"  c"));
		assertEquals("a b", FieldInstruction.unquote("\"a b\""));
		assertEquals("\"", FieldInstruction.unquote("\""));
		assertNull(FieldInstruction.unquote(null));
	}

	@Test
	public void emptyAndBlankInstructions() {
		for (String s : new String[] { null, "", "   " }) {
			FieldInstruction i = FieldInstruction.parse(s);
			assertNull(i.getName());
			assertNull(i.getParameterText());
			assertTrue(i.getParameters().isEmpty());
			assertNull(i.getArgument());
			assertFalse(i.hasSwitch("\\*"));
		}
		FieldInstruction i = FieldInstruction.parse("\\* MERGEFORMAT");
		assertEquals("the first token is the name, whatever it is", "\\*", i.getName());
	}

	/** A deprecated FldSimpleModel reads every shape exactly as FieldInstruction does: it delegates. */
	@Test
	@SuppressWarnings("deprecation")
	public void readsAsFldSimpleModelRead() throws Exception {
		for (String s : SHAPES) {
			FieldInstruction i = FieldInstruction.parse(s);
			FldSimpleModel m = new FldSimpleModel();
			m.build(s);
			assertEquals(s, m.getFldName(), i.getName());
			assertEquals(s, m.getFldParameterString(), i.getParameterText());
			assertEquals(s, m.getFldParameters(), i.getParameters());
			assertEquals(s, FormattingSwitchHelper.findFirstSwitchValue("\\*", m.getFldParameters(), true), i.getSwitchValue("\\*"));
			assertEquals(s, FormattingSwitchHelper.findFirstSwitchValue("\\@", m.getFldParameters(), true), i.getSwitchValue("\\@"));
			assertEquals(s, FormattingSwitchHelper.hasSwitch("\\h", m.getFldParameters()), i.hasSwitch("\\h"));
			// getFldSimpleName reads a formula's name as "=" plus what follows it to the first space; the model
			// (and FieldInstruction) as "=": the one difference between the two old parsers, left as it is
			if (!s.trim().startsWith("=")) assertEquals(s, FormattingSwitchHelper.getFldSimpleName(s), i.getName());
			assertSame(s, m.toInstruction().getName(), i.getName());
		}
	}

	private static void assertSame(String message, String a, String b) {
		assertEquals(message, a, b);
	}

	@Test
	public void theParametersAreUnmodifiable() {
		FieldInstruction i = FieldInstruction.parse(" SEQ Figure ");
		try {
			i.getParameters().add("x");
			org.junit.Assert.fail();
		} catch (UnsupportedOperationException expected) {
		}
	}
}
