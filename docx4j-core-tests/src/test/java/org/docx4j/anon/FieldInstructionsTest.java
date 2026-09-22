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
package org.docx4j.anon;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * The instruction scrub, with a scrambler which upper-cases letters to X so the
 * result is readable: the keyword and switches stay, pictures stay, bookmark
 * arguments map, everything else loses its letters.
 */
public class FieldInstructionsTest {

	private final FieldInstructions fi = new FieldInstructions(
			s -> s.replaceAll("\\p{L}", "X"),
			name -> "bm_" + name.length(),
			name -> name.equals("Heading 1"));

	@Test
	public void mergefieldKeepsKeywordAndFormatSwitch() {
		assertEquals(" MERGEFIELD  XXXXXXXXXXXXX \\* MERGEFORMAT ",
				fi.scrub(" MERGEFIELD  Kundenstrasse \\* MERGEFORMAT "));
		assertEquals(" MERGEFIELD Name \\* Upper \\b \"Dear \" ".replace("Name", "XXXX").replace("Dear", "XXXX"),
				fi.scrub(" MERGEFIELD Name \\* Upper \\b \"Dear \" "));
	}

	@Test
	public void hyperlinkLosesUrlAndTipKeepsSwitches() {
		assertEquals(" HYPERLINK \"XXXXX://XXXXXX.XXXX.XXXXXXX/XXXXXX\" \\o \"XXXX XXX\" ",
				fi.scrub(" HYPERLINK \"https://portal.acme.example/secret\" \\o \"Acme tip\" "));
		// an internal link: the \l argument is a bookmark, mapped
		assertEquals(" HYPERLINK \\l \"bm_12\" ", fi.scrub(" HYPERLINK \\l \"SecretClause\" "));
	}

	@Test
	public void referencesMapTheBookmark() {
		assertEquals(" REF bm_12 \\h ", fi.scrub(" REF SecretClause \\h "));
		assertEquals(" PAGEREF bm_7 \\h \\* MERGEFORMAT ", fi.scrub(" PAGEREF _Toc123 \\h \\* MERGEFORMAT "));
		assertEquals(" NOTEREF bm_3 \\f ", fi.scrub(" NOTEREF abc \\f "));
		assertEquals(" SEQ bm_6 \\* ARABIC ", fi.scrub(" SEQ Figure \\* ARABIC "));
		assertEquals(" ASK bm_4 \"XXXX XXXX?\" ", fi.scrub(" ASK Name \"Your name?\" "));
		assertEquals(" TOC \\o \"1-3\" \\h \\z \\u \\b bm_5 ", fi.scrub(" TOC \\o \"1-3\" \\h \\z \\u \\b Range "));
	}

	@Test
	public void picturesAreKept() {
		assertEquals(" DATE \\@ \"dddd, d MMMM yyyy\" ", fi.scrub(" DATE \\@ \"dddd, d MMMM yyyy\" "));
		assertEquals(" = SUM(ABOVE) \\# \"#,##0.00\" ", fi.scrub(" = SUM(ABOVE) \\# \"#,##0.00\" "));
	}

	@Test
	public void formulaKeepsFunctionsMapsNothingElse() {
		// a plain identifier in a formula is a bookmark
		assertEquals(" = bm_5 * 2 ", fi.scrub(" = Price * 2 "));
		assertEquals(" = SUM(LEFT) ", fi.scrub(" = SUM(LEFT) "));
		assertEquals(" = B2*3 + bm_3 ", fi.scrub(" = B2*3 + Tax "));
	}

	@Test
	public void styleRefKeepsBuiltInNames() {
		assertEquals(" STYLEREF \"Heading 1\" \\* MERGEFORMAT ", fi.scrub(" STYLEREF \"Heading 1\" \\* MERGEFORMAT "));
		assertEquals(" STYLEREF \"XXXX XXXXX\" ", fi.scrub(" STYLEREF \"Acme Title\" "));
	}

	@Test
	public void pathsKeepTheirShape() {
		assertEquals(" INCLUDEPICTURE \"X:\\\\XXXXX\\\\XXXX\\\\XXXX-XXXX.XXX\" \\* MERGEFORMAT \\d ",
				fi.scrub(" INCLUDEPICTURE \"C:\\\\Users\\\\jane\\\\acme-logo.png\" \\* MERGEFORMAT \\d "));
		assertEquals(" INCLUDETEXT \"X:\\\\X\\\\XXXXX.XXXX\" XXX ", fi.scrub(" INCLUDETEXT \"C:\\\\x\\\\notes.docx\" abc "));
	}

	@Test
	public void formFieldsAndPageAreUntouched() {
		assertEquals(" FORMTEXT ", fi.scrub(" FORMTEXT "));
		assertEquals(" FORMCHECKBOX ", fi.scrub(" FORMCHECKBOX "));
		assertEquals(" PAGE ", fi.scrub(" PAGE "));
		assertEquals("PAGE  \\* MERGEFORMAT", fi.scrub("PAGE  \\* MERGEFORMAT"));
	}

	@Test
	public void docPropertyAndDocVariableLoseTheirNames() {
		assertEquals(" DOCPROPERTY XXXXXXX \\* MERGEFORMAT ", fi.scrub(" DOCPROPERTY Company \\* MERGEFORMAT "));
		assertEquals(" DOCVARIABLE XXXXXXXXXXXXX ", fi.scrub(" DOCVARIABLE AcmeSecretVar "));
	}

	@Test
	public void keyword() {
		assertEquals("MERGEFIELD", FieldInstructions.keyword(" MERGEFIELD  Kundenstrasse "));
		assertEquals("=", FieldInstructions.keyword(" = SUM(ABOVE) "));
		assertEquals(null, FieldInstructions.keyword("   "));
	}

}
