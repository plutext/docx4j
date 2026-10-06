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
package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A line set in bold only is as tall as a regular line of the same font: Word sizes it by the
 * family's metrics.  docx4j scaled FOP's content height for the run, which comes from the face's
 * typo metrics, and those differ by face (Liberation Serif Bold's sTypoAscender 1387, the
 * Regular's 1420), so a bold-only Times New Roman line rendered in Liberation Serif was 13.55pt
 * where Word's is 13.80 (line-box-bold-run probe, CR-001 batch 53; corpus documents 4025,
 * 13321).  Where the machine's substitute has no such difference the two agree anyway.
 *
 * @since 17.3.1
 */
public class BoldLineHeightTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final String TEXT = "The quick brown fox jumps over the lazy dog while the farmer watches from the gate."
			+ " Pagination is the process of dividing a document into discrete pages, either electronic or printed.";

	private static String para(boolean bold) {
		return "<w:p><w:pPr><w:spacing w:before=\"0\" w:after=\"240\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr>"
				+ "<w:r><w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>" + (bold ? "<w:b/>" : "")
				+ "<w:sz w:val=\"24\"/></w:rPr><w:t>" + (bold ? "BOLD " : "REGULAR ") + TEXT + "</w:t></w:r></w:p>";
	}

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + para(false) + para(true)
				+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/>"
				+ "</w:sectPr></w:body></w:document>"));
		return pkg;
	}

	/** The bpd of each lineArea in the body, in document order. */
	private static List<Integer> lineHeights(org.w3c.dom.Document areaTree) {
		List<Integer> out = new ArrayList<Integer>();
		NodeList lines = areaTree.getElementsByTagName("lineArea");
		for (int i = 0; i < lines.getLength(); i++) {
			Element line = (Element) lines.item(i);
			String bpd = line.getAttribute("bpd");
			if (bpd.length() > 0) out.add(Integer.valueOf(bpd));
		}
		return out;
	}

	private void check(int flags) throws Exception {
		List<Integer> heights = lineHeights(areaTree(pkg(), flags));
		assertTrue("two paragraphs of two lines or more: " + heights, heights.size() >= 4);
		int regular = heights.get(0);
		for (int h : heights) {
			assertEquals("every line, bold or regular, as tall as the first: " + heights, regular, h, 20);
		}
	}

	@Test
	public void visitorPathway() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xsltPathway() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
