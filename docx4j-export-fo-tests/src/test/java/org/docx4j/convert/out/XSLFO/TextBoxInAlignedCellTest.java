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
import static org.junit.Assert.assertNotNull;

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
 * A text box anchored in a cell of {@code w:vAlign bottom} keeps its text at the box's
 * top: {@code display-align} is inherited, and the box took the cell's "after", so its
 * text stood at the box's foot.  Corpus document 4083's check marks in its "you" and
 * "your partner" heads came out up to 3.1pt low, the taller the box the lower.  And the
 * wrapper the box hangs in keeps with its paragraph's first line.
 *
 * @since 17.3.6
 */
public class TextBoxInAlignedCellTest extends AbstractXSLFOTest {

	private static final String NS = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\""
			+ " xmlns:v=\"urn:schemas-microsoft-com:vml\""
			+ " xmlns:o=\"urn:schemas-microsoft-com:office:office\"";

	private static org.w3c.dom.Document fo(int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + NS + "><w:body><w:tbl><w:tblPr><w:tblW w:w=\"5000\" w:type=\"dxa\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"5000\"/></w:tblGrid><w:tr><w:tc><w:tcPr><w:tcW w:w=\"5000\" w:type=\"dxa\"/>"
				+ "<w:vAlign w:val=\"bottom\"/></w:tcPr><w:p><w:r><w:pict>"
				+ "<v:rect id=\"R1\" o:spid=\"_x0000_s1026\" style=\"position:absolute;"
				+ "margin-left:0;margin-top:14pt;width:14pt;height:28pt;z-index:1;"
				+ "mso-position-vertical-relative:text\" stroked=\"f\"><v:textbox>"
				+ "<w:txbxContent><w:p><w:r><w:t>the mark</w:t></w:r></w:p></w:txbxContent>"
				+ "</v:textbox></v:rect></w:pict></w:r><w:r><w:t>the head</w:t></w:r></w:p></w:tc></w:tr></w:tbl>"
				+ "<w:p/></w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private void check(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(flags);
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "block-container");
		Element box = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (el.hasAttribute("absolute-position")) box = el;
		}
		assertNotNull("the positioned box", box);
		assertEquals("the box's text starts at its top, whatever the cell's alignment", "before",
				box.getAttribute("display-align"));
		nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "table-cell");
		assertEquals("the cell keeps its own", "after", ((Element) nl.item(0)).getAttribute("display-align"));
	}

	/** The zero-height wrapper a box hangs in stays with its paragraph's first line (probe
	 *  vml-box-page-top's K9), and with the paragraph after where it has no text, on both
	 *  pathways: left at a page's foot a tall box runs off the page (1616's caption). */
	private void checkKeep(int flags) throws Exception {
		final String BOX = "<w:r><w:pict><v:rect id=\"R1\" o:spid=\"_x0000_s1026\" style=\"position:absolute;"
				+ "margin-left:0;margin-top:14pt;width:14pt;height:28pt;z-index:1;"
				+ "mso-position-vertical-relative:text\" stroked=\"f\"><v:textbox>"
				+ "<w:txbxContent><w:p><w:r><w:t>the mark</w:t></w:r></w:p></w:txbxContent>"
				+ "</v:textbox></v:rect></w:pict></w:r>";
		assertEquals("always", wrapper(body(BOX + "<w:r><w:t>the line</w:t></w:r>"), flags)
				.getAttribute("keep-with-next.within-page"));
		assertEquals("always", wrapper(body(BOX), flags).getAttribute("keep-with-next.within-page"));
	}

	private static String body(String runs) {
		return "<w:document " + NS + "><w:body><w:p>" + runs + "</w:p><w:p/></w:body></w:document>";
	}

	private static Element wrapper(String document, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(document));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		NodeList nl = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()))
				.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "block-container");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (el.hasAttribute("absolute-position")) return (Element) el.getParentNode();
		}
		throw new AssertionError("no positioned box");
	}

	@Test
	public void keepVisitor() throws Exception {
		checkKeep(Docx4J.FLAG_NONE);
	}

	@Test
	public void keepXslt() throws Exception {
		checkKeep(Docx4J.FLAG_EXPORT_PREFER_XSL);
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
