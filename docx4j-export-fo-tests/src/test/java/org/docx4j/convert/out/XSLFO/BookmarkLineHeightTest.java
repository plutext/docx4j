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
 * A bookmark around a heading's text does not make its line taller.  The bookmark's empty
 * fo:inline names no document font, and its height was split in the substitute's own ascent
 * share where the run beside it, naming its document font, took Word's: Nimbus Sans Narrow's
 * 0.7917 against Arial Narrow's 0.8166, so the line took one's ascent and the other's descent,
 * 0.51pt over Word's box at 18pt (corpus document 9623, its headings and running head).  An
 * element in the block's own font naming no document font now takes the block's split.  Where
 * the machine's substitute for Arial Narrow has Arial Narrow's split the two agree anyway.
 *
 * @since 17.3.1
 */
public class BookmarkLineHeightTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final String PPR = "<w:pPr><w:spacing w:before=\"0\" w:after=\"240\" w:line=\"240\" w:lineRule=\"auto\"/>"
			+ "<w:rPr><w:rFonts w:ascii=\"Arial Narrow\" w:hAnsi=\"Arial Narrow\"/><w:b/><w:sz w:val=\"36\"/></w:rPr></w:pPr>";

	private static final String RUN = "<w:r><w:rPr><w:rFonts w:ascii=\"Arial Narrow\" w:hAnsi=\"Arial Narrow\"/><w:b/>"
			+ "<w:sz w:val=\"36\"/></w:rPr><w:t>Introduction</w:t></w:r>";

	private static WordprocessingMLPackage pkg() throws Exception {
		String plain = "<w:p>" + PPR + RUN + "</w:p>";
		String bookmarked = "<w:p>" + PPR + "<w:bookmarkStart w:id=\"1\" w:name=\"_Toc1\"/>"
				+ "<w:bookmarkStart w:id=\"2\" w:name=\"_Toc2\"/>" + RUN
				+ "<w:bookmarkEnd w:id=\"1\"/><w:bookmarkEnd w:id=\"2\"/></w:p>";
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + plain + bookmarked
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
			String bpd = ((Element) lines.item(i)).getAttribute("bpd");
			if (bpd.length() > 0) out.add(Integer.valueOf(bpd));
		}
		return out;
	}

	private void check(int flags) throws Exception {
		List<Integer> heights = lineHeights(areaTree(pkg(), flags));
		assertTrue("a line each: " + heights, heights.size() >= 2);
		assertEquals("the bookmarked heading's line as tall as the plain one's: " + heights,
				heights.get(0), heights.get(1), 20);
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
