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
package org.docx4j.model.datastorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.McBlockLevel;
import org.docx4j.jaxb.McProbeDocx;
import org.docx4j.model.datastorage.RemovalHandler.Quantifier;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * OpenDoPE's two stylesheets that keep cells in the shape Word asks for ({@code docs/developer/word-table-rules.md}):
 * a cell's last block, as Word reads it through block-level controls, is a paragraph (measured 2026-10-22,
 * probes 03 to 05 and 10 to 12). Until 17.3.4 both looked for a paragraph anywhere in the cell, so a cell
 * ending in a control holding a table passed, and the integrity step padded every control in a cell that had
 * no paragraph inside it.
 */
public class WordTableRulesOpenDoPETest {

	private static final String P = "<w:p><w:r><w:t>p</w:t></w:r></w:p>";
	private static final String TBL_PROPS = "<w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/></w:tblPr><w:tblGrid><w:gridCol w:w=\"1000\"/></w:tblGrid>";
	private static final String NESTED = "<w:tbl>" + TBL_PROPS + "<w:tr><w:tc>" + P + "</w:tc></w:tr></w:tbl>";

	private static String sdt(String tag, String content) {
		return "<w:sdt><w:sdtPr><w:tag w:val=\"" + tag + "\"/><w:id w:val=\"1\"/></w:sdtPr><w:sdtContent>" + content + "</w:sdtContent></w:sdt>";
	}

	private static String table(String cellContent) {
		return "<w:tbl>" + TBL_PROPS + "<w:tr><w:tc><w:tcPr><w:tcW w:w=\"1000\" w:type=\"dxa\"/></w:tcPr>" + cellContent + "</w:tc></w:tr></w:tbl>";
	}

	private static int countOf(String s, String token) {
		int n = 0;
		for (int i = s.indexOf(token); i >= 0; i = s.indexOf(token, i + 1)) n++;
		return n;
	}

	private static String integrity(String body) throws Exception {
		WordprocessingMLPackage pkg = McProbeDocx.body(body).load(McBlockLevel.PRESERVE);
		new OpenDoPEIntegrity().process(pkg);
		return XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
	}

	/** Probe 05's shape: a cell ending in a control holding a table gets one empty paragraph, after the control (Word's repair). */
	@Test
	public void integrityEndsACellWithAParagraphAfterAControlHoldingATable() throws Exception {
		String xml = integrity(table(NESTED + sdt("x", NESTED)) + P);
		assertTrue(xml, xml.contains("</w:sdt><w:p/></w:tc>"));
		assertEquals("one paragraph appended, none inside the control", 1, countOf(xml, "<w:p/>"));
	}

	/** Probe 03's shape: a cell ending in a control holding a paragraph is left alone. */
	@Test
	public void integrityLeavesACellEndingInAWrappedParagraph() throws Exception {
		String body = table(NESTED + sdt("x", P)) + P;
		String xml = integrity(body);
		assertEquals(xml, 0, countOf(xml, "<w:p/>"));
	}

	/** A control holding only a table in the middle of a cell, a paragraph after it: nothing to pad (the old rule padded the control). */
	@Test
	public void integrityNoLongerPadsAControlHoldingATableWhenTheCellEndsWithAParagraph() throws Exception {
		String xml = integrity(table(sdt("x", NESTED) + P) + P);
		assertEquals(xml, 0, countOf(xml, "<w:p/>"));
	}

	/** An empty control in a cell is still populated. */
	@Test
	public void integrityPopulatesAnEmptyControl() throws Exception {
		String xml = integrity(table(P + "<w:sdt><w:sdtPr><w:tag w:val=\"x\"/><w:id w:val=\"1\"/></w:sdtPr><w:sdtContent/></w:sdt>") + P);
		assertTrue(xml, xml.contains("<w:sdtContent><w:p/></w:sdtContent>"));
	}

	/** A row-level control around a cell: the cell inside gets its paragraph; the control is kept as it is. */
	@Test
	public void integrityEndsACellInsideARowControl() throws Exception {
		String body = "<w:tbl>" + TBL_PROPS + "<w:tr>" + sdt("x", "<w:tc>" + NESTED + "</w:tc>") + "</w:tr></w:tbl>" + P;
		String xml = integrity(body);
		assertTrue(xml, xml.contains("</w:tbl><w:p/></w:tc></w:sdtContent></w:sdt>"));
		assertEquals(1, countOf(xml, "<w:p/>"));
	}

	/** The removal stylesheet, unwrapping a control that held the cell's last paragraph beside a table: the table is now last, so a paragraph is appended. */
	@Test
	public void removalEndsACellWhoseLastParagraphItRemovedWithATheControl() throws Exception {
		WordprocessingMLPackage pkg = McProbeDocx.body(table(NESTED + sdt("od:condition=c1", NESTED)) + P).load(McBlockLevel.PRESERVE);
		new RemovalHandler().removeSDTs(pkg, Quantifier.ALL);
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		assertTrue(xml, !xml.contains("<w:sdt>"));
		assertTrue(xml, xml.contains("</w:tbl><w:p/></w:tc>"));
		assertEquals(1, countOf(xml, "<w:p/>"));
	}

	/** The removal stylesheet leaves a cell that ends with a paragraph alone. */
	@Test
	public void removalLeavesACellEndingWithAParagraph() throws Exception {
		WordprocessingMLPackage pkg = McProbeDocx.body(table(NESTED + sdt("od:condition=c1", P)) + P).load(McBlockLevel.PRESERVE);
		new RemovalHandler().removeSDTs(pkg, Quantifier.ALL);
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		assertEquals(xml, 0, countOf(xml, "<w:p/>"));
	}
}
