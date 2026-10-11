/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Body;
import org.docx4j.wml.Style;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A hyperlinked TOC's entries are drawn without the Hyperlink character style in FO, as Word
 * draws them; a hyperlink elsewhere, and a TOC without {@code \h}, keep it (TocHyperlinkStyle;
 * the same document as core-tests' TocHyperlinkStyleTest).
 */
public class TocHyperlinkStyleFoTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static String link(String text) {
		return "<w:hyperlink w:anchor=\"_Toc1\"><w:r><w:rPr><w:rStyle w:val=\"Hipervnculo\"/></w:rPr>"
				+ "<w:t>" + text + "</w:t></w:r></w:hyperlink>";
	}

	private static WordprocessingMLPackage doc() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getContents().getStyle().add((Style) XmlUtils.unmarshalString(
				"<w:style " + W + " w:type=\"character\" w:styleId=\"Hipervnculo\"><w:name w:val=\"Hyperlink\"/>"
				+ "<w:rPr><w:color w:val=\"467886\" w:themeColor=\"hyperlink\"/><w:u w:val=\"single\"/></w:rPr></w:style>"));
		Body body = (Body) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:body " + W + ">"
				+ "<w:p><w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>"
				+ "<w:r><w:instrText xml:space=\"preserve\"> TOC \\o \"1-3\" \\h \\z \\u </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>" + link("TOCENTRYONE") + "</w:p>"
				+ "<w:p>" + link("TOCENTRYTWO") + "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r></w:p>"
				+ "<w:p><w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>"
				+ "<w:r><w:instrText xml:space=\"preserve\"> TOC \\o \"1-3\" </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>"
				+ "<w:r><w:rPr><w:rStyle w:val=\"Hipervnculo\"/></w:rPr><w:t>PLAINTOCENTRY</w:t></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r></w:p>"
				+ "<w:p>" + link("BODYLINK") + "</w:p>"
				+ "</w:body>"));
		pkg.getMainDocumentPart().getJaxbElement().setBody(body);
		return pkg;
	}

	private static Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** The value of an attribute on the innermost element enclosing a text which has it */
	private static String attribute(Document fo, String text, String name) {
		Element e = enclosing(fo.getDocumentElement(), text);
		assertNotNull(text, e);
		for (; e != null; e = e.getParentNode() instanceof Element ? (Element) e.getParentNode() : null) {
			if (e.hasAttribute(name)) return e.getAttribute(name).toUpperCase();
		}
		return null;
	}

	private static Element enclosing(Element e, String text) {
		for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() == Node.TEXT_NODE && n.getNodeValue().trim().equals(text)) return e;
			if (n instanceof Element) {
				Element found = enclosing((Element) n, text);
				if (found != null) return found;
			}
		}
		return null;
	}

	@Test
	public void tocEntriesAreDrawnPlain() throws Exception {
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			String pathway = flags == Docx4J.FLAG_NONE ? "visitor: " : "XSLT: ";
			Document fo = fo(doc(), flags);
			for (String entry : new String[] { "TOCENTRYONE", "TOCENTRYTWO" }) {
				assertNull(pathway + entry, attribute(fo, entry, "text-decoration"));
				assertNull(pathway + entry, attribute(fo, entry, "color"));
			}
			for (String styled : new String[] { "PLAINTOCENTRY", "BODYLINK" }) {
				assertEquals(pathway + styled, "UNDERLINE", attribute(fo, styled, "text-decoration"));
				assertEquals(pathway + styled, "#467886", attribute(fo, styled, "color")); // the 2023 theme's hyperlink
			}
		}
	}
}
