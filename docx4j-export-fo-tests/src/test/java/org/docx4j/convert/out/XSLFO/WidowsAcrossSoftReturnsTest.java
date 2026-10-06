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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Word's widow control counts a paragraph's lines across its soft returns.  A paragraph of
 * a line of text and a trailing {@code w:br} is two lines to Word, which widow control will
 * not divide: at the foot of a page it goes to the next page whole.  FOP counted each run
 * of lines between the nested blocks docx4j writes for a {@code w:br} on its own, and set
 * the text on one page and the empty line on the next (corpus document 2065, whose article
 * entries end in a {@code w:br}).
 *
 * <p>The paragraph is moved down the page a line at a time, so that whatever the machine's
 * fonts, some position puts its first line last on a page.</p>
 *
 * @since 17.3.1
 */
public class WidowsAcrossSoftReturnsTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final String PROPERTY = "docx4j.convert.out.fo.wordLayout.widowsAcrossSoftReturns";

	private static final String PPR = "<w:pPr><w:spacing w:before=\"0\" w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr>";

	private static WordprocessingMLPackage pkg(int fillers) throws Exception {
		StringBuilder body = new StringBuilder();
		for (int i = 0; i < fillers; i++) {
			body.append("<w:p>").append(PPR).append("<w:r><w:t>Filler line ").append(i).append("</w:t></w:r></w:p>");
		}
		body.append("<w:p>").append(PPR).append("<w:r><w:t>Entry text</w:t></w:r><w:r><w:br/></w:r></w:p>");
		body.append("<w:p>").append(PPR).append("<w:r><w:t>After the entry</w:t></w:r></w:p>");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body
				+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"4000\"/>"
				+ "<w:pgMar w:top=\"720\" w:right=\"1440\" w:bottom=\"720\" w:left=\"1440\" w:header=\"0\" w:footer=\"0\"/>"
				+ "</w:sectPr></w:body></w:document>"));
		return pkg;
	}

	/** Each lineArea's page (1-based) and text, in document order. */
	private static List<String[]> lines(org.w3c.dom.Document areaTree) {
		List<String[]> out = new ArrayList<String[]>();
		NodeList pages = areaTree.getElementsByTagName("pageViewport");
		for (int p = 0; p < pages.getLength(); p++) {
			NodeList las = ((Element) pages.item(p)).getElementsByTagName("lineArea");
			for (int i = 0; i < las.getLength(); i++) {
				StringBuilder sb = new StringBuilder();
				words(las.item(i), sb);
				out.add(new String[] { String.valueOf(p + 1), sb.toString().trim() });
			}
		}
		return out;
	}

	private static void words(Node n, StringBuilder sb) {
		for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c instanceof Element && "word".equals(c.getNodeName())) sb.append(c.getTextContent()).append(' ');
			else words(c, sb);
		}
	}

	/** Whether the entry's text line and its last line - the empty one after the break,
	 *  the line before the next paragraph's - are on different pages.  (The break's own
	 *  block brings empty line areas of its own between them.) */
	private boolean divided(int fillers, int flags) throws Exception {
		List<String[]> lines = lines(areaTree(pkg(fillers), flags));
		String entryPage = null;
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i)[1].startsWith("Entry text")) entryPage = lines.get(i)[0];
			if (lines.get(i)[1].startsWith("After the entry") && entryPage != null) {
				return !entryPage.equals(lines.get(i - 1)[0]);
			}
		}
		throw new AssertionError("no entry line in " + fillers);
	}

	private void check(int flags) throws Exception {
		String was = org.docx4j.Docx4jProperties.getProperty(PROPERTY);
		try {
			boolean dividedWithout = false;
			org.docx4j.Docx4jProperties.setProperty(PROPERTY, "false");
			for (int n = 0; n < 12 && !dividedWithout; n++) dividedWithout = divided(n, flags);
			assertTrue("counting each part alone, some position divides the paragraph", dividedWithout);

			org.docx4j.Docx4jProperties.setProperty(PROPERTY, "true");
			for (int n = 0; n < 12; n++) {
				assertTrue("the entry and its empty line are divided with " + n + " lines above it", !divided(n, flags));
			}
		} finally {
			if (was == null) org.docx4j.Docx4jProperties.getProperties().remove(PROPERTY);
			else org.docx4j.Docx4jProperties.setProperty(PROPERTY, was);
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
