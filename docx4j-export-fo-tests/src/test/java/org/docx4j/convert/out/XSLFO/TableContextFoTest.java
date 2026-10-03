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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.CTFootnotes;
import org.docx4j.wml.Document;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.Styles;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Table styles on the FO visitor pathway since CR-030 phase 3: no preprocess and no synthetic
 * styles, each paragraph in a table resolved in its cell context as the visitor walks the
 * table.  The XSLT pathway still resolves through the synthetic styles, so the two pathways'
 * answers are compared: the same weight and slant for every cell's text.  A story reached from
 * a cell - a text box anchored in it, a footnote referenced from it - is resolved with no table
 * context (CR-030 T3, T8).
 */
public class TableContextFoTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:v=\"urn:schemas-microsoft-com:vml\""
			+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	/** The whole table italic, its first row bold; a list style whose level names it back. */
	private static final String STYLES = "<w:styles " + W + ">"
			+ "<w:docDefaults><w:rPrDefault><w:rPr><w:rFonts w:ascii=\"Liberation Serif\" w:hAnsi=\"Liberation Serif\"/>"
			+ "<w:sz w:val=\"24\"/></w:rPr></w:rPrDefault>"
			+ "<w:pPrDefault><w:pPr><w:spacing w:after=\"200\"/></w:pPr></w:pPrDefault></w:docDefaults>"
			+ "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/></w:style>"
			+ "<w:style w:type=\"paragraph\" w:styleId=\"ListNumber\"><w:name w:val=\"List Number\"/><w:basedOn w:val=\"Normal\"/>"
			+ "<w:pPr><w:numPr><w:numId w:val=\"1\"/></w:numPr></w:pPr></w:style>"
			+ "<w:style w:type=\"paragraph\" w:styleId=\"FootnoteText\"><w:name w:val=\"footnote text\"/><w:basedOn w:val=\"Normal\"/>"
			+ "<w:rPr><w:sz w:val=\"20\"/></w:rPr></w:style>"
			+ "<w:style w:type=\"character\" w:styleId=\"FootnoteReference\"><w:name w:val=\"footnote reference\"/>"
			+ "<w:rPr><w:vertAlign w:val=\"superscript\"/></w:rPr></w:style>"
			+ "<w:style w:type=\"table\" w:default=\"1\" w:styleId=\"TableNormal\"><w:name w:val=\"Normal Table\"/></w:style>"
			+ "<w:style w:type=\"table\" w:styleId=\"Probe\"><w:name w:val=\"Probe\"/>"
			+ "<w:pPr><w:spacing w:after=\"0\"/></w:pPr><w:rPr><w:i/></w:rPr>"
			+ "<w:tblStylePr w:type=\"firstRow\"><w:rPr><w:b/></w:rPr></w:tblStylePr></w:style>"
			+ "</w:styles>";

	private static final String NUMBERING = "<w:numbering " + W + ">"
			+ "<w:abstractNum w:abstractNumId=\"0\"><w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>"
			+ "<w:pStyle w:val=\"ListNumber\"/><w:lvlText w:val=\"%1.\"/></w:lvl></w:abstractNum>"
			+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num></w:numbering>";

	private static final String LOOK = "<w:tblLook w:val=\"0620\" w:firstRow=\"1\" w:lastRow=\"0\" w:firstColumn=\"0\""
			+ " w:lastColumn=\"0\" w:noHBand=\"1\" w:noVBand=\"1\"/>";

	private static String p(String pStyle, String text) {
		return "<w:p>" + (pStyle == null ? "" : "<w:pPr><w:pStyle w:val=\"" + pStyle + "\"/></w:pPr>")
				+ "<w:r><w:t xml:space=\"preserve\">" + text + "</w:t></w:r></w:p>";
	}

	/** A shaded paragraph: the Containerization preprocess puts it in a container. */
	private static final String SHADED = "<w:p><w:pPr><w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"E5B8B7\"/></w:pPr>"
			+ "<w:r><w:t xml:space=\"preserve\">TEXT</w:t></w:r></w:p>";

	private static final String BODY = "<w:tbl><w:tblPr><w:tblStyle w:val=\"Probe\"/>" + LOOK + "</w:tblPr>"
			+ "<w:tblGrid><w:gridCol w:w=\"8000\"/></w:tblGrid>"
			+ "<w:tr><w:tc><w:tcPr><w:tcW w:w=\"8000\" w:type=\"dxa\"/></w:tcPr>"
			+ p(null, "header cell")
			+ p("ListNumber", "header item")
			+ "<w:p><w:r><w:t xml:space=\"preserve\">anchor </w:t></w:r><w:r><w:pict><v:shape style=\"width:200pt;height:40pt\">"
			+ "<v:textbox><w:txbxContent>" + p(null, "inside the box") + "</w:txbxContent></v:textbox></v:shape></w:pict></w:r></w:p>"
			+ "<w:p><w:r><w:t xml:space=\"preserve\">referencing </w:t></w:r><w:r><w:rPr><w:rStyle w:val=\"FootnoteReference\"/></w:rPr>"
			+ "<w:footnoteReference w:id=\"1\"/></w:r></w:p>"
			+ "</w:tc></w:tr>"
			+ "<w:tr><w:tc><w:tcPr><w:tcW w:w=\"8000\" w:type=\"dxa\"/></w:tcPr>" + p(null, "body cell") + p("ListNumber", "body item")
			+ SHADED.replace("TEXT", "shaded one") + SHADED.replace("TEXT", "shaded two") + "</w:tc></w:tr>"
			+ "</w:tbl>" + p(null, "after the table");

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setContents((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + BODY + "</w:body></w:document>"));
		pkg.getMainDocumentPart().getStyleDefinitionsPart().setContents((Styles) XmlUtils.unmarshalString(STYLES));
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		pkg.getMainDocumentPart().addTargetPart(ndp);
		ndp.setJaxbElement((Numbering) XmlUtils.unmarshalString(NUMBERING));
		FootnotesPart fp = new FootnotesPart();
		pkg.getMainDocumentPart().addTargetPart(fp);
		fp.setJaxbElement((CTFootnotes) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:footnotes " + W + ">"
				+ "<w:footnote w:type=\"separator\" w:id=\"-1\"><w:p><w:r><w:separator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:type=\"continuationSeparator\" w:id=\"0\"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:id=\"1\"><w:p><w:pPr><w:pStyle w:val=\"FootnoteText\"/></w:pPr>"
				+ "<w:r><w:rPr><w:rStyle w:val=\"FootnoteReference\"/></w:rPr><w:footnoteRef/></w:r>"
				+ "<w:r><w:t xml:space=\"preserve\"> the note</w:t></w:r></w:p></w:footnote></w:footnotes>")));
		pkg.getMainDocumentPart().getPropertyResolver().refresh();
		return pkg;
	}

	private static org.w3c.dom.Document fo(int flags) throws Exception {
		FOSettings settings = Docx4J.createFOSettings();
		settings.setOpcPackage(pkg());
		settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(settings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** The innermost FO element whose text starts with the given text. */
	private static Element holding(org.w3c.dom.Document doc, String text) {
		Element found = null;
		NodeList all = doc.getElementsByTagNameNS(FO, "*");
		for (int i = 0; i < all.getLength(); i++) {
			Element el = (Element) all.item(i);
			if (el.getTextContent().trim().startsWith(text)) found = el; // document order: innermost last
		}
		assertNotNull("no FO holding " + text, found);
		return found;
	}

	/** The attribute's value on the element or its nearest ancestor which states it, stopping
	 *  at the given element name (exclusive) where one is given; "" for none. */
	private static String inherited(Element el, String attribute, String stopAt) {
		for (Node n = el; n instanceof Element; n = n.getParentNode()) {
			Element e = (Element) n;
			if (stopAt != null && stopAt.equals(e.getLocalName())) return "";
			if (e.hasAttribute(attribute)) return e.getAttribute(attribute);
		}
		return "";
	}

	private static boolean bold(String weight) {
		return "bold".equals(weight) || "700".equals(weight);
	}

	private static void tableFormatting(org.w3c.dom.Document fo, String pathway) {
		Element header = holding(fo, "header cell");
		assertTrue(pathway + ": header row bold", bold(inherited(header, "font-weight", null)));
		assertEquals(pathway + ": header row italic", "italic", inherited(header, "font-style", null));
		Element body = holding(fo, "body cell");
		assertTrue(pathway + ": body row not bold", !bold(inherited(body, "font-weight", null)));
		assertEquals(pathway + ": body row italic", "italic", inherited(body, "font-style", null));
		Element after = holding(fo, "after the table");
		assertTrue(pathway + ": after the table, plain", !"italic".equals(inherited(after, "font-style", null)));
	}

	@Test
	public void theCellsFormattingOnBothPathways() throws Exception {
		tableFormatting(fo(Docx4J.FLAG_NONE), "visitor");
		tableFormatting(fo(Docx4J.FLAG_EXPORT_PREFER_XSL), "xslt");
	}

	/** T3: the text box is a story of its own (its block-container stops the inheritance). */
	@Test
	public void aTextBoxInACellGetsNoneOfIt() throws Exception {
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			Element box = holding(fo(flags), "inside the box");
			assertTrue("flags " + flags + ": " + inherited(box, "font-weight", "block-container"),
					!bold(inherited(box, "font-weight", "block-container")));
			assertTrue(!"italic".equals(inherited(box, "font-style", "block-container")));
		}
	}

	/** T8: a footnote referenced from a header cell is resolved with no table context: nothing
	 *  in the note's own blocks states the cell's bold or italic.  (What XSL-FO inheritance
	 *  brings in from the referencing paragraph is CR-030 D10, fixed in 17.3.1 and tested by
	 *  FootnoteInheritanceTest.) */
	@Test
	public void aFootnoteFromACellIsResolvedWithoutIt() throws Exception {
		Element note = holding(fo(Docx4J.FLAG_NONE), "the note");
		assertTrue(inherited(note, "font-weight", "footnote-body"), !bold(inherited(note, "font-weight", "footnote-body")));
		assertTrue(inherited(note, "font-style", "footnote-body"), !"italic".equals(inherited(note, "font-style", "footnote-body")));
	}

	/**
	 * A shading container's block takes its first paragraph's w:pPr: in a cell it is resolved in
	 * that paragraph's context, so the table style's w:after 0 is the container's, not the
	 * document defaults' 200 (found by the phase 3 corpus gate: a 30pt drift and a page).
	 */
	@Test
	public void aShadingContainerInACellTakesTheTableStyle() throws Exception {
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			Element el = holding(fo(flags), "shaded one");
			Element outermost = null;
			for (Node n = el; n instanceof Element; n = n.getParentNode()) {
				Element e = (Element) n;
				if ("block".equals(e.getLocalName()) && e.hasAttribute("background-color")) outermost = e;
			}
			assertNotNull("flags " + flags + ": no shaded block", outermost);
			String after = outermost.getAttribute("space-after");
			assertTrue("flags " + flags + ": space-after " + after,
					after.length() == 0 || after.startsWith("0"));
		}
	}

	/** D1 on the visitor pathway too: a style-numbered paragraph in a cell has its label. */
	@Test
	public void numberedParagraphsInCellsAreNumbered() throws Exception {
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			org.w3c.dom.Document fo = fo(flags);
			String text = fo.getDocumentElement().getTextContent().replaceAll("\\s+", " ");
			assertTrue("flags " + flags + ": " + text, text.contains("1.") && text.contains("2."));
			assertTrue("flags " + flags + ": " + text, text.indexOf("1.") < text.indexOf("header item")
					&& text.indexOf("2.") < text.indexOf("body item"));
		}
	}
}
