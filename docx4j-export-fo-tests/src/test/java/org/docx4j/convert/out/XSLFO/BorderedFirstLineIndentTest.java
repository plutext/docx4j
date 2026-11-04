/*
 * Copyright 2026, Plutext Pty Ltd.
 *
 * This file is part of docx4j.
 *
 * docx4j is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Two bordered paragraphs that differ only in a first-line indent are one box (probe
 * pbdr-group, Word's golden), so one borders container, built from the first paragraph's
 * properties.  Each keeps its own first line, in either order.
 *
 * @since 17.3.6
 */
public class BorderedFirstLineIndentTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final String BOX =
			"<w:pBdr><w:top w:val=\"single\" w:sz=\"4\" w:space=\"1\" w:color=\"auto\"/>"
			+ "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"1\" w:color=\"auto\"/></w:pBdr>";

	private static final String INDENT = "<w:ind w:firstLine=\"360\"/>";

	/** The text-indent in force on the block holding this text: its own, or the nearest ancestor's. */
	private static String textIndent(org.w3c.dom.Document doc, String text) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "block");
		for (int i = nl.getLength() - 1; i >= 0; i--) {
			Element el = (Element) nl.item(i);
			if (!el.getTextContent().trim().equals(text)) continue;
			for (org.w3c.dom.Node n = el; n instanceof Element; n = n.getParentNode()) {
				if (((Element) n).hasAttribute("text-indent")) return ((Element) n).getAttribute("text-indent");
			}
			return "0pt";
		}
		throw new AssertionError("no block holds " + text);
	}

	private static org.w3c.dom.Document fo(String firstPPr, String secondPPr, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr>" + firstPPr + "</w:pPr><w:r><w:t>first</w:t></w:r></w:p>"
				+ "<w:p><w:pPr>" + secondPPr + "</w:pPr><w:r><w:t>second</w:t></w:r></w:p>"
				+ "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private void check(int flags) throws Exception {
		org.w3c.dom.Document second = fo(BOX, BOX + INDENT, flags);
		assertEquals("0pt", textIndent(second, "first"));
		assertEquals("18pt", textIndent(second, "second"));

		org.w3c.dom.Document first = fo(BOX + INDENT, BOX, flags);
		assertEquals("18pt", textIndent(first, "first"));
		assertEquals("the first paragraph's indent is not the second's", "0pt", textIndent(first, "second"));
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
