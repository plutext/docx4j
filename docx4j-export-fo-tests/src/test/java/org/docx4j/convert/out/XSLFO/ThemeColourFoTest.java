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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.docx4j.wml.Style;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * WordprocessingML theme colours in the FO output (CR-034 phase 2): Word draws the theme
 * reference, and the value beside it is a cache which can be stale (CR-034 §11, P1).  The
 * document is a new one, in the Office 2023 theme; its stale values are Office 2007's
 * {@code accent1}, 4F81BD, which should appear nowhere in the FO.  The same document is
 * core-tests' ThemeColourOutputTest, for PropertyResolver and HTML.
 */
public class ThemeColourFoTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final int[] PATHWAYS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static WordprocessingMLPackage doc() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		List<Style> styles = mdp.getStyleDefinitionsPart().getContents().getStyle();
		styles.add(style("StaleColour", "<w:rPr><w:color w:val=\"4F81BD\" w:themeColor=\"accent1\"/></w:rPr>"));
		styles.add(style("ThemeOnly", "<w:rPr><w:color w:themeColor=\"accent2\"/></w:rPr>"));
		styles.add(style("CurrentCache", "<w:rPr><w:color w:val=\"156082\" w:themeColor=\"accent1\"/></w:rPr>"));
		List<Object> body = mdp.getContent();
		body.add(p("<w:pPr><w:pStyle w:val=\"StaleColour\"/></w:pPr><w:r><w:t>STALE</w:t></w:r>"));
		body.add(p("<w:pPr><w:pStyle w:val=\"StaleColour\"/></w:pPr>"
				+ "<w:r><w:rPr><w:color w:val=\"FF0000\"/></w:rPr><w:t>RED</w:t></w:r>"));
		body.add(p("<w:pPr><w:pStyle w:val=\"ThemeOnly\"/></w:pPr><w:r><w:t>THEMEONLY</w:t></w:r>"));
		body.add(p("<w:pPr><w:pStyle w:val=\"CurrentCache\"/></w:pPr><w:r><w:t>CURRENT</w:t></w:r>"));
		body.add(p("<w:r><w:rPr><w:color w:val=\"auto\" w:themeColor=\"accent5\"/></w:rPr><w:t>AUTO</w:t></w:r>"));
		body.add(p("<w:pPr><w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"auto\" w:themeFill=\"accent2\"/></w:pPr>"
				+ "<w:r><w:t>SHADEDAUTO</w:t></w:r>"));
		body.add(p("<w:r><w:t>UNBORDERED</w:t></w:r>"));
		body.add(p("<w:pPr><w:pBdr><w:top w:val=\"single\" w:sz=\"8\" w:space=\"1\" w:color=\"4F81BD\" w:themeColor=\"accent6\"/></w:pBdr></w:pPr>"
				+ "<w:r><w:t>BORDERED</w:t></w:r>"));
		body.add(XmlUtils.unmarshalString("<w:tbl " + W + "><w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"4000\"/></w:tblGrid><w:tr><w:tc><w:tcPr><w:tcW w:w=\"4000\" w:type=\"dxa\"/>"
				+ "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"4F81BD\" w:themeFill=\"accent4\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>CELL</w:t></w:r></w:p></w:tc></w:tr></w:tbl>"));
		body.add(p("<w:r><w:t>END</w:t></w:r>"));
		return pkg;
	}

	private static Style style(String id, String pr) throws Exception {
		return (Style) XmlUtils.unmarshalString("<w:style " + W + " w:type=\"paragraph\" w:styleId=\"" + id + "\">"
				+ "<w:name w:val=\"" + id + "\"/><w:basedOn w:val=\"Normal\"/>" + pr + "</w:style>");
	}

	private static P p(String content) throws Exception {
		return (P) XmlUtils.unmarshalString("<w:p " + W + ">" + content + "</w:p>");
	}

	private static String foString(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return baos.toString("UTF-8");
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
	public void foDrawsTheThemeColours() throws Exception {
		for (int flags : PATHWAYS) {
			String pathway = flags == Docx4J.FLAG_NONE ? "visitor: " : "XSLT: ";
			String s = foString(doc(), flags);
			assertFalse(pathway + "a stale value written", s.toUpperCase().contains("4F81BD"));
			Document fo = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(s.getBytes("UTF-8")));
			assertEquals(pathway + "STALE", "#156082", attribute(fo, "STALE", "color"));
			assertEquals(pathway + "RED", "#FF0000", attribute(fo, "RED", "color"));
			assertEquals(pathway + "THEMEONLY", "#E97132", attribute(fo, "THEMEONLY", "color"));
			assertEquals(pathway + "CURRENT", "#156082", attribute(fo, "CURRENT", "color"));
			assertEquals(pathway + "AUTO", "#A02B93", attribute(fo, "AUTO", "color"));
			assertEquals(pathway + "SHADEDAUTO", "#E97132", attribute(fo, "SHADEDAUTO", "background-color"));
			assertEquals(pathway + "BORDERED", "#4EA72E", attribute(fo, "BORDERED", "border-top-color"));
			assertEquals(pathway + "CELL", "#0F9ED5", attribute(fo, "CELL", "background-color"));
		}
	}
}
