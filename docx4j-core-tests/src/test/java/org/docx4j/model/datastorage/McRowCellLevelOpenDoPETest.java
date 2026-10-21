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

import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.McBlockLevel;
import org.docx4j.jaxb.McProbeDocx;
import org.docx4j.mce.AlternateContent;
import org.docx4j.model.datastorage.RemovalHandler.Quantifier;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CTSdtCell;
import org.docx4j.wml.P;
import org.docx4j.wml.Tc;
import org.junit.Test;

/**
 * CR-051 phase 2, OpenDoPE's two readers of rows and cells by type: the row removal of
 * {@code RemovalHandler.xslt}, which deleted a row whose cells all stood in a cell-level
 * {@code mc:AlternateContent} kept whole, and {@code OpenDoPEHandler}'s emptying of a cell
 * control, which lifted the first cell it found out of such an element.
 */
public class McRowCellLevelOpenDoPETest {

	private static String p(String text) {
		return "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static String tc(String text) {
		return "<w:tc><w:tcPr><w:tcW w:w=\"1000\" w:type=\"dxa\"/></w:tcPr>" + p(text) + "</w:tc>";
	}

	/** A cell-level element: a cell in each branch. */
	private static String ac(String tag) {
		return "<mc:AlternateContent><mc:Choice Requires=\"w14\">" + tc(tag + "_W14") + "</mc:Choice>"
				+ "<mc:Fallback>" + tc(tag + "_FB") + "</mc:Fallback></mc:AlternateContent>";
	}

	private static final String TBL_PROPS = "<w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/></w:tblPr><w:tblGrid><w:gridCol w:w=\"1000\"/></w:tblGrid>";

	private static int countOf(String s, String token) {
		int n = 0;
		for (int i = s.indexOf(token); i >= 0; i = s.indexOf(token, i + 1)) n++;
		return n;
	}

	/** A row whose only cell stands in a kept cell-level element survives the removal, the element with it; the control in the other row is removed. */
	@Test
	public void removalHandlerKeepsARowWhoseCellsAreInAKeptElement() throws Exception {
		String body = "<w:tbl>" + TBL_PROPS
				+ "<w:tr>" + ac("ONLY") + "</w:tr>"
				+ "<w:tr><w:sdt><w:sdtPr><w:tag w:val=\"od:condition=c1\"/></w:sdtPr><w:sdtContent>" + tc("inControl") + "</w:sdtContent></w:sdt></w:tr>"
				+ "</w:tbl>" + p("after");
		WordprocessingMLPackage pkg = McProbeDocx.body(body).load(McBlockLevel.PRESERVE);
		new RemovalHandler().removeSDTs(pkg, Quantifier.ALL);
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false);
		assertEquals("both rows kept: " + xml, 2, countOf(xml, "<w:tr>"));
		assertTrue(xml, xml.contains("<mc:AlternateContent") && xml.contains("ONLY_W14") && xml.contains("ONLY_FB"));
		assertTrue("the control removed, its cell kept: " + xml, !xml.contains("<w:sdt>") && xml.contains("inControl"));
	}

	/** A cell control whose content is a kept cell-level element: emptied, each branch keeps its own cell, with its tcPr, and the element stays. */
	@Test
	public void emptyingACellControlKeepsEachBranchsCell() throws Exception {
		String sdt = "<w:sdt " + McProbeDocx.NS + "><w:sdtPr><w:tag w:val=\"od:repeat=r1\"/></w:sdtPr><w:sdtContent>" + ac("CELL") + "</w:sdtContent></w:sdt>";
		CTSdtCell control = (CTSdtCell) XmlUtils.unmarshalString(sdt, Context.jc, CTSdtCell.class);
		OpenDoPEHandler.emptyKeepingTableShape(control);
		List<Object> content = control.getSdtContent().getContent();
		assertEquals(1, content.size());
		AlternateContent ac = (AlternateContent) XmlUtils.unwrap(content.get(0));
		for (Object branch : new Object[] { ac.getChoice().get(0), ac.getFallback() }) {
			List<Object> branchContent = org.docx4j.jaxb.McSplice.contentOf(branch);
			assertEquals(1, branchContent.size());
			Tc tc = (Tc) XmlUtils.unwrap(branchContent.get(0));
			assertTrue("tcPr kept", tc.getTcPr() != null && tc.getTcPr().getTcW() != null);
			assertEquals("one empty paragraph", 1, tc.getContent().size());
			assertTrue(XmlUtils.unwrap(tc.getContent().get(0)) instanceof P);
			assertTrue(((P) XmlUtils.unwrap(tc.getContent().get(0))).getContent().isEmpty());
		}
		String xml = XmlUtils.marshaltoString(control, true, false);
		assertTrue(xml, !xml.contains("CELL_W14") && !xml.contains("CELL_FB"));
	}

	/** Without an element, a cell control is emptied to its cell as before. */
	@Test
	public void emptyingACellControlWithoutAnElementIsUnchanged() throws Exception {
		String sdt = "<w:sdt " + McProbeDocx.NS + "><w:sdtPr><w:tag w:val=\"od:repeat=r1\"/></w:sdtPr><w:sdtContent>" + tc("CELL") + "</w:sdtContent></w:sdt>";
		CTSdtCell control = (CTSdtCell) XmlUtils.unmarshalString(sdt, Context.jc, CTSdtCell.class);
		OpenDoPEHandler.emptyKeepingTableShape(control);
		List<Object> content = control.getSdtContent().getContent();
		assertEquals(1, content.size());
		Tc tc = (Tc) XmlUtils.unwrap(content.get(0));
		assertTrue(tc.getTcPr() != null);
		assertEquals(1, tc.getContent().size());
		assertTrue(((P) XmlUtils.unwrap(tc.getContent().get(0))).getContent().isEmpty());
	}
}
