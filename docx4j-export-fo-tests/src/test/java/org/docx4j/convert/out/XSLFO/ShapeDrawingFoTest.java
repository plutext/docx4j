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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A DrawingML shape drawn in the FO, by the visitor and the XSLT alike (CR-080 phase 2): its drawing, SVG, the first
 * child of its container, under its text; the text in the geometry's text rectangle, where its bodyPr anchors it; no
 * text in a line; an anchored shape with no text drawn out of the flow, and an inline one not yet (phase 4).
 */
public class ShapeDrawingFoTest extends AbstractXSLFOTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\"";
	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";
	private static final String SVG_NS = "http://www.w3.org/2000/svg";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	/** A shape 180 by 90pt, anchored 72pt in and 36pt down, with this spPr content, text box and bodyPr */
	private static String anchored(String sppr, String txbx, String bodyPr) {
		return "<w:p><w:r><w:drawing><wp:anchor distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\" simplePos=\"0\" relativeHeight=\"1\""
				+ " behindDoc=\"0\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\"><wp:simplePos x=\"0\" y=\"0\"/>"
				+ "<wp:positionH relativeFrom=\"column\"><wp:posOffset>914400</wp:posOffset></wp:positionH>"
				+ "<wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>457200</wp:posOffset></wp:positionV>"
				+ "<wp:extent cx=\"2286000\" cy=\"1143000\"/><wp:wrapNone/><wp:docPr id=\"1\" name=\"Shape 1\"/>"
				+ graphic(sppr, txbx, bodyPr) + "</wp:anchor></w:drawing></w:r></w:p>";
	}

	private static String inline(String sppr) {
		return "<w:p><w:r><w:drawing><wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\"><wp:extent cx=\"2286000\" cy=\"1143000\"/>"
				+ "<wp:docPr id=\"2\" name=\"Shape 2\"/>" + graphic(sppr, null, "<wps:bodyPr/>") + "</wp:inline></w:drawing></w:r></w:p>";
	}

	private static String inlineText(String sppr, String txbx) {
		return "<w:p><w:r><w:t xml:space=\"preserve\">before </w:t></w:r><w:r><w:drawing><wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\">"
				+ "<wp:extent cx=\"2286000\" cy=\"1143000\"/><wp:docPr id=\"3\" name=\"Text Box 3\"/>" + graphic(sppr, txbx, "<wps:bodyPr/>")
				+ "</wp:inline></w:drawing></w:r></w:p>";
	}

	private static String graphic(String sppr, String txbx, String bodyPr) {
		return "<a:graphic><a:graphicData uri=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\"><wps:wsp>"
				+ "<wps:cNvSpPr/><wps:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"2286000\" cy=\"1143000\"/></a:xfrm>" + sppr + "</wps:spPr>"
				+ (txbx == null ? "" : "<wps:txbx><w:txbxContent><w:p><w:r><w:t>" + txbx + "</w:t></w:r></w:p></w:txbxContent></wps:txbx>")
				+ bodyPr + "</wps:wsp></a:graphicData></a:graphic>";
	}

	private static final String RED_RECT = "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom><a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill>"
			+ "<a:ln w=\"12700\"><a:solidFill><a:srgbClr val=\"000000\"/></a:solidFill></a:ln>";

	private static org.w3c.dom.Document fo(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString("<w:document " + NS + "><w:body>" + body
				+ "<w:p><w:r><w:t>body text</w:t></w:r></w:p>" + SECT_PR + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private static NodeList svgs(org.w3c.dom.Document doc) {
		return doc.getElementsByTagNameNS(SVG_NS, "svg");
	}

	/** The block-container holding the text, the one whose first child holds the drawing */
	private static Element textContainer(org.w3c.dom.Document doc, String text) {
		NodeList nl = doc.getElementsByTagNameNS(FO_NS, "block-container");
		Element found = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			Element first = firstElement(el);
			if (first != null && "absolute".equals(first.getAttribute("absolute-position"))
					&& first.getElementsByTagNameNS(SVG_NS, "svg").getLength() > 0 && (text == null || el.getTextContent().contains(text))) found = el;
		}
		return found;
	}

	private static Element firstElement(Element el) {
		for (org.w3c.dom.Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) if (n instanceof Element) return (Element) n;
		return null;
	}

	private static final int[] BOTH = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	@Test
	public void aShapeWithNoTextIsDrawn() throws Exception {
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = fo(anchored(RED_RECT, null, "<wps:bodyPr/>"), flags);
			assertEquals("flags " + flags, 1, svgs(doc).getLength());
			String svg = XmlUtils.w3CDomNodeToString(svgs(doc).item(0)).toLowerCase(java.util.Locale.ROOT);
			// Batik names a colour where SVG has a name for it
			assertTrue(svg, svg.contains("fill=\"red\"") || svg.contains("rgb(255,0,0)") || svg.contains("#ff0000"));
			Element container = textContainer(doc, null);
			assertNotNull("flags " + flags, container);
			// out of the flow, as a wrapNone shape is: positioned, not reserving space
			assertTrue("flags " + flags, container.getAttribute("absolute-position").length() > 0
					|| ((Element) container.getParentNode()).getAttribute("absolute-position").length() > 0);
		}
	}

	@Test
	public void theDrawingIsUnderTheTextInTheTextRectangle() throws Exception {
		String ellipse = "<a:prstGeom prst=\"ellipse\"><a:avLst/></a:prstGeom><a:solidFill><a:srgbClr val=\"DEEAF6\"/></a:solidFill><a:ln><a:noFill/></a:ln>";
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = fo(anchored(ellipse, "IN THE ELLIPSE", "<wps:bodyPr lIns=\"0\" tIns=\"0\" rIns=\"0\" bIns=\"0\" anchor=\"b\"/>"), flags);
			Element container = textContainer(doc, "IN THE ELLIPSE");
			assertNotNull("flags " + flags, container);
			// the text rectangle of an ellipse 180 by 90pt: 90 - 90 cos 45 in from the left, 45 - 45 sin 45 from the bottom
			double in = 90 - 90 * Math.cos(Math.PI / 4);
			assertEquals("flags " + flags, in, pt(container.getAttribute("start-indent")), 0.01);
			assertEquals("flags " + flags, in, pt(container.getAttribute("padding-right")), 0.01);
			assertEquals("flags " + flags, 45 - 45 * Math.sin(Math.PI / 4), pt(container.getAttribute("padding-bottom")), 0.01);
			assertEquals("flags " + flags, "after", container.getAttribute("display-align"));
			// the drawing at the box's top left: its container's top is above the content rectangle by the padding
			Element drawing = firstElement(container);
			assertEquals("flags " + flags, -pt(container.getAttribute("padding-top")), pt(drawing.getAttribute("top")), 0.001);
			assertEquals("flags " + flags, 0, pt(drawing.getAttribute("left")), 0.001);
			assertEquals("flags " + flags, 180, pt(drawing.getAttribute("width")), 0.001);
		}
	}

	/** A shape with no text that wraps takes no space yet: it is drawn out of the flow (phase 4 wraps it) */
	@Test
	public void aShapeWithNoTextTakesNoSpaceYet() throws Exception {
		for (int flags : BOTH) {
			// 400pt wide: a narrower box (under 60% of the column) WordLayoutFixups takes out of the flow anyway
			org.w3c.dom.Document doc = fo(anchored(RED_RECT, null, "<wps:bodyPr/>").replace("<wp:wrapNone/>", "<wp:wrapSquare wrapText=\"bothSides\"/>")
					.replace("cx=\"2286000\"", "cx=\"5080000\""), flags);
			assertEquals("flags " + flags, 1, svgs(doc).getLength());
			NodeList floats = doc.getElementsByTagNameNS(FO_NS, "float");
			for (int i = 0; i < floats.getLength(); i++) {
				assertEquals("flags " + flags + ": floated", 0, ((Element) floats.item(i)).getElementsByTagNameNS(SVG_NS, "svg").getLength());
			}
			// out of the flow: in a zero-height wrapper, where a wrapped box's reserves its height
			boolean zero = false;
			for (org.w3c.dom.Node n = svgs(doc).item(0); n != null; n = n.getParentNode()) {
				if (n instanceof Element && "block-container".equals(n.getLocalName()) && "0pt".equals(((Element) n).getAttribute("height"))) zero = true;
			}
			assertTrue("flags " + flags + ": not in a zero-height wrapper", zero);
		}
	}

	@Test
	public void aLineShowsNoText() throws Exception {
		String line = "<a:prstGeom prst=\"line\"><a:avLst/></a:prstGeom><a:ln w=\"25400\"><a:solidFill><a:srgbClr val=\"000000\"/></a:solidFill></a:ln>";
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = fo(anchored(line, "LINE TEXT", "<wps:bodyPr/>"), flags);
			assertFalse("flags " + flags, doc.getDocumentElement().getTextContent().contains("LINE TEXT"));
			assertEquals("flags " + flags, 1, svgs(doc).getLength());
		}
	}

	@Test
	public void anInlineShapeWithNoTextIsNotDrawnYet() throws Exception {
		for (int flags : BOTH) {
			assertEquals("flags " + flags, 0, svgs(fo(inline(RED_RECT), flags)).getLength());
		}
	}

	/** An inline shape with text is on its line in an fo:inline-container of its extent, its foot on the baseline,
	 *  holding the box, drawn, with its text: in the run's fo:inline FOP painted the box's block-container not at
	 *  all (probe mc-textbox-branches-bound-sdt, its text lost once the wps Choice was drawn).  @since 17.3.7 */
	@Test
	public void anInlineShapeWithTextIsOnItsLine() throws Exception {
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = fo(inlineText(RED_RECT, "INLINE TEXT"), flags);
			NodeList holders = doc.getElementsByTagNameNS(FO_NS, "inline-container");
			assertEquals("flags " + flags, 1, holders.getLength());
			Element holder = (Element) holders.item(0);
			assertEquals("flags " + flags, "180pt", holder.getAttribute("width"));
			assertEquals("flags " + flags, "90pt", holder.getAttribute("height"));
			assertEquals("flags " + flags, "after-edge", holder.getAttribute("alignment-adjust"));
			org.w3c.dom.Node para = holder.getParentNode();
			while (para != null && !"block".equals(para.getLocalName())) para = para.getParentNode();
			assertTrue("flags " + flags + ": in the paragraph's line", para != null && para.getTextContent().startsWith("before"));
			Element box = firstElement(holder);
			assertEquals("flags " + flags, "block-container", box.getLocalName());
			assertEquals("flags " + flags, "", box.getAttribute("absolute-position"));
			Element container = textContainer(doc, "INLINE TEXT");
			assertNotNull("flags " + flags + ": drawn, with its text", container);
			assertSame("flags " + flags, box, container.getParentNode());
			assertEquals("flags " + flags, "absolute", container.getAttribute("absolute-position"));
			assertEquals("flags " + flags, container.getAttribute("padding-top"), container.getAttribute("top"));
		}
	}

	@Test
	public void aShapeThatPaintsNothingWritesNothing() throws Exception {
		String nothing = "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom><a:noFill/><a:ln><a:noFill/></a:ln>";
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = fo(anchored(nothing, null, "<wps:bodyPr/>"), flags);
			assertEquals("flags " + flags, 0, svgs(doc).getLength());
			assertNull("flags " + flags, textContainer(doc, null));
			// no container either: the FO is the same as with no shape there at all
			assertEquals("flags " + flags, fo("", flags).getElementsByTagNameNS(FO_NS, "block-container").getLength(),
					doc.getElementsByTagNameNS(FO_NS, "block-container").getLength());
		}
	}

	private static double pt(String v) {
		return v.isEmpty() ? 0 : Double.parseDouble(v.replace("pt", ""));
	}
}
