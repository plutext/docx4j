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
package org.docx4j.model.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.wml.Tc;
import org.junit.Test;

/**
 * Word's cell rule as measured on Word 365, 2026-10-22 ({@code docs/developer/word-table-rules.md},
 * rule 2, probes 01 to 12): the last block as Word reads it, through block-level controls and the
 * read branch of an {@code mc:AlternateContent}, must be a paragraph.
 */
public class TableRulesTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\""
			+ " xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\""
			+ " xmlns:w99x=\"urn:docx4j:probe:unknown\"";

	private static final String P = "<w:p><w:r><w:t>p</w:t></w:r></w:p>";
	private static final String TBL = "<w:tbl><w:tblPr/><w:tblGrid><w:gridCol w:w=\"1000\"/></w:tblGrid><w:tr><w:tc>" + P + "</w:tc></w:tr></w:tbl>";

	private static String sdt(String content) {
		return "<w:sdt><w:sdtPr><w:id w:val=\"1\"/></w:sdtPr><w:sdtContent>" + content + "</w:sdtContent></w:sdt>";
	}

	private static String mc(String requires, String choice, String fallback) {
		return "<mc:AlternateContent><mc:Choice Requires=\"" + requires + "\">" + choice + "</mc:Choice><mc:Fallback>" + fallback + "</mc:Fallback></mc:AlternateContent>";
	}

	private static Tc tc(String content) throws Exception {
		return (Tc) XmlUtils.unmarshalString("<w:tc " + NS + "><w:tcPr><w:tcW w:w=\"1000\" w:type=\"dxa\"/></w:tcPr>" + content + "</w:tc>");
	}

	private static void opens(String content) throws Exception {
		assertTrue(content, TableRules.endsWithParagraph(tc(content)));
	}

	private static void unreadable(String content) throws Exception {
		assertFalse(content, TableRules.endsWithParagraph(tc(content)));
	}

	@Test
	public void theProbesWordOpens() throws Exception {
		opens(TBL + P); // 01
		opens(TBL + sdt(P)); // 03
		opens(TBL + sdt(sdt(P))); // 04
		opens(TBL + mc("w14", P, P)); // 06
		opens(TBL + mc("w99x", P, P)); // 07, the Fallback read
		opens(TBL + mc("w14", P, TBL)); // 08, the unread Fallback not checked
		opens(sdt(P)); // 10
		opens(mc("w14", P, P)); // 11
	}

	@Test
	public void theProbesWordRepairs() throws Exception {
		unreadable(TBL); // 02
		unreadable(TBL + sdt(TBL)); // 05
		unreadable(TBL + mc("w14", TBL, P)); // 09, the read Choice ends in a table
		unreadable(sdt(TBL)); // 12
		unreadable(""); // no block at all (rule 1)
	}

	/** Range markup after the last paragraph is not a block, and does not count. */
	@Test
	public void rangeMarkupAfterTheParagraphDoesNotCount() throws Exception {
		opens(P + "<w:bookmarkEnd w:id=\"0\"/>");
		unreadable(TBL + "<w:bookmarkEnd w:id=\"0\"/>");
	}

	/** The repair is Word's: one empty paragraph appended to the cell, after the wrapper, and none when the rule holds. */
	@Test
	public void endWithParagraphAppendsOnceAfterTheWrapper() throws Exception {
		Tc cell = tc(TBL + sdt(TBL));
		assertTrue(TableRules.endWithParagraph(cell));
		String xml = XmlUtils.marshaltoString(cell, true, false);
		assertTrue(xml, xml.endsWith("</w:sdt><w:p/></w:tc>"));
		assertFalse("already ends with a paragraph", TableRules.endWithParagraph(cell));
		assertEquals(3, cell.getContent().size());

		Tc wrapped = tc(TBL + sdt(P));
		assertFalse(TableRules.endWithParagraph(wrapped));
		assertEquals(2, wrapped.getContent().size());
	}
}
