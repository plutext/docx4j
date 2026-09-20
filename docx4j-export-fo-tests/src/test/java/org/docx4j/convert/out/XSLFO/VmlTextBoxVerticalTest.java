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
import static org.junit.Assert.assertNull;

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
 * A VML text box of {@code layout-flow:vertical}: Word turns its text on its side and
 * lays it along the box's <b>height</b>, as it turns a table cell's text for
 * {@code w:textDirection}.  {@code mso-layout-flow-alt:bottom-to-top} turns it the other
 * way (Word's {@code btLr} against its {@code tbRl}).
 *
 * <p>The property is on the {@code v:textbox}, not on the shape - measured on four corpus
 * documents, whose shape style carries only the position and the size.  Before 17.2.0 it
 * was read nowhere at all and the box was laid out unrotated: one of those documents has
 * a 16.5pt-wide legend beside a table whose 17-character label was set one character to a
 * line where Word lays it along the 93.75pt height in one.</p>
 *
 * @since 17.2.0 (CR-001 batch 48 item 4)
 */
public class VmlTextBoxVerticalTest extends AbstractXSLFOTest {

	private static final String NS = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\""
			+ " xmlns:v=\"urn:schemas-microsoft-com:vml\""
			+ " xmlns:o=\"urn:schemas-microsoft-com:office:office\"";

	/** A page-anchored 16.5 x 93.75pt VML box, with whatever style on its v:textbox. */
	private static org.w3c.dom.Document fo(String textboxStyle, int flags) throws Exception {
		String style = textboxStyle==null ? "" : " style=\"" + textboxStyle + "\"";
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + NS + "><w:body><w:p><w:r><w:pict>"
				+ "<v:rect id=\"R1\" o:spid=\"_x0000_s1026\" style=\"position:absolute;"
				+ "margin-left:0;margin-top:0;width:16.5pt;height:93.75pt;z-index:1;"
				+ "mso-position-vertical-relative:text\" stroked=\"f\">"
				+ "<v:textbox inset=\"0,0,0,0\"" + style + ">"
				+ "<w:txbxContent><w:p><w:r><w:t>Vertical legend</w:t></w:r></w:p></w:txbxContent>"
				+ "</v:textbox></v:rect></w:pict></w:r></w:p>"
				+ "<w:p><w:r><w:t>a tail paragraph</w:t></w:r></w:p>"
				+ "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** The container the box's text is in: the innermost one holding the text box's
	 *  content, the outer one being the zero-height wrapper it is positioned in. */
	private static Element box(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format",
				"block-container");
		Element found = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (el.getTextContent().contains("Vertical legend")
					&& !el.getTextContent().contains("a tail paragraph")) found = el;
		}
		return found;
	}

	private void check(int flags) throws Exception {

		Element down = box(fo("layout-flow:vertical", flags));
		assertNotNull("the vertical box", down);
		assertEquals("Word's tbRl: the text runs down the box", "-90",
				down.getAttribute("reference-orientation"));
		assertEquals("the line runs along the box's height, which is the measure", "93.75pt",
				down.getAttribute("inline-progression-dimension"));
		assertEquals("and the box's width is the other dimension", "16.5pt",
				down.getAttribute("block-progression-dimension"));
		assertEquals("the unrotated pair goes, being the wrong way round", "",
				down.getAttribute("width"));
		assertEquals("", down.getAttribute("height"));

		Element up = box(fo("layout-flow:vertical;mso-layout-flow-alt:bottom-to-top", flags));
		assertNotNull("the bottom-to-top box", up);
		assertEquals("Word's btLr: the text runs up the box", "90",
				up.getAttribute("reference-orientation"));
		assertEquals("93.75pt", up.getAttribute("inline-progression-dimension"));

		Element control = box(fo(null, flags));
		assertNotNull("the control, no layout-flow at all", control);
		assertEquals("no rotation", "", control.getAttribute("reference-orientation"));
		assertEquals("and the box keeps its own width and height", "16.5pt",
				control.getAttribute("width"));
		assertEquals("93.75pt", control.getAttribute("height"));

		Element horizontal = box(fo("mso-fit-shape-to-text:t", flags));
		assertNotNull("a textbox style which says nothing about the flow", horizontal);
		assertEquals("", horizontal.getAttribute("reference-orientation"));
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
