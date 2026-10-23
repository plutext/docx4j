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
package org.docx4j.model.fields.merge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Body;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.After;
import org.junit.Test;

/**
 * Issues 236 and 256: a MERGEFIELD merged with an empty or blank value, in REMOVED mode, takes its
 * paragraph with it once nothing is left in it; in a table cell that must not leave the cell without a
 * paragraph as its last block, which Word reports as a corrupt document (the cell rule docx4j measured
 * for CR-050). A cell's sole paragraph was kept already; one after a nested table, or between bookmark
 * markers, was not, until 17.3.5.
 */
public class MailMergerCellRuleTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FIELD_P = "<w:p><w:fldSimple w:instr=\" MERGEFIELD blankField \\* MERGEFORMAT \"><w:r><w:t>«blankField»</w:t></w:r></w:fldSimple></w:p>";

	@After
	public void restore() {
		MailMerger.setMERGEFIELDInOutput(MailMerger.OutputField.DEFAULT);
	}

	/** The cell after merging {@code value} into a cell holding {@code cellContent}. */
	private static Tc merged(String cellContent, String value) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Body body = (Body) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:body " + W + "><w:tbl><w:tblGrid><w:gridCol w:w=\"3000\"/></w:tblGrid>"
				+ "<w:tr><w:tc><w:tcPr><w:tcW w:w=\"3000\" w:type=\"dxa\"/></w:tcPr>" + cellContent + "</w:tc></w:tr></w:tbl><w:p/></w:body>"));
		pkg.getMainDocumentPart().getJaxbElement().setBody(body);
		Map<DataFieldName, String> data = new HashMap<DataFieldName, String>();
		data.put(new DataFieldName("blankField"), value);
		MailMerger.setMERGEFIELDInOutput(MailMerger.OutputField.REMOVED);
		MailMerger.performMerge(pkg, data, true);
		Tbl tbl = (Tbl) XmlUtils.unwrap(pkg.getMainDocumentPart().getContent().get(0));
		Tr tr = (Tr) XmlUtils.unwrap(tbl.getContent().get(0));
		return (Tc) XmlUtils.unwrap(tr.getContent().get(0));
	}

	/** Word's rule: the cell's last block, range markers passed over, is a paragraph. */
	private static void endsInAParagraph(String what, Tc tc) {
		List<Object> content = tc.getContent();
		Object last = null;
		for (int i = content.size() - 1; i >= 0 && last == null; i--) {
			Object v = XmlUtils.unwrap(content.get(i));
			if (v instanceof P || v instanceof Tbl || v instanceof org.docx4j.wml.SdtBlock) last = v;
		}
		assertTrue(what + ": " + XmlUtils.marshaltoString(tc), last instanceof P);
	}

	@Test
	public void aCellsSoleParagraphStays() throws Exception {
		for (String value : new String[] { "", "   " }) {
			Tc tc = merged(FIELD_P, value);
			endsInAParagraph("sole paragraph, '" + value + "'", tc);
			assertEquals(1, tc.getContent().size());
		}
	}

	@Test
	public void theParagraphAfterANestedTableStays() throws Exception {
		Tc tc = merged("<w:tbl><w:tblGrid><w:gridCol w:w=\"1000\"/></w:tblGrid><w:tr><w:tc><w:p><w:r><w:t>inner</w:t></w:r></w:p></w:tc></w:tr></w:tbl>"
				+ FIELD_P, "");
		endsInAParagraph("after a nested table", tc);
		assertEquals(2, tc.getContent().size());
	}

	@Test
	public void theParagraphBetweenBookmarkMarkersStays() throws Exception {
		Tc tc = merged("<w:bookmarkStart w:id=\"5\" w:name=\"b\"/>" + FIELD_P + "<w:bookmarkEnd w:id=\"5\"/>", "");
		endsInAParagraph("between bookmark markers", tc);
	}

	/** A cell whose other paragraph remains loses the emptied one, as before. */
	@Test
	public void anEmptiedParagraphGoesWhereAnotherRemains() throws Exception {
		Tc tc = merged("<w:p><w:r><w:t>kept</w:t></w:r></w:p>" + FIELD_P, "");
		assertEquals(1, tc.getContent().size());
		assertEquals("kept", TextUtils.getText((P) XmlUtils.unwrap(tc.getContent().get(0))));
	}
}
