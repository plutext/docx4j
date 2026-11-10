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
 * text in a line. A shape, group or canvas with no text to show is a picture of its extent, placed and wrapped as a
 * picture is, anchored or inline (phase 4).
 */
public class ShapeDrawingFoTest extends AbstractXSLFOTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\""
			+ " xmlns:wpg=\"http://schemas.microsoft.com/office/word/2010/wordprocessingGroup\""
			+ " xmlns:wpc=\"http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas\"";
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

	/** A group or canvas (the graphicData's child, uri its namespace) anchored as {@link #anchored} anchors a shape, 180 x 90pt */
	private static String anchoredGroup(String uri, String graphic) {
		return "<w:p><w:r><w:drawing><wp:anchor distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\" simplePos=\"0\" relativeHeight=\"1\""
				+ " behindDoc=\"0\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\"><wp:simplePos x=\"0\" y=\"0\"/>"
				+ "<wp:positionH relativeFrom=\"column\"><wp:posOffset>914400</wp:posOffset></wp:positionH>"
				+ "<wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>457200</wp:posOffset></wp:positionV>"
				+ "<wp:extent cx=\"2286000\" cy=\"1143000\"/><wp:wrapNone/><wp:docPr id=\"4\" name=\"Group 4\"/>"
				+ "<a:graphic><a:graphicData uri=\"" + uri + "\">" + graphic + "</a:graphicData></a:graphic></wp:anchor></w:drawing></w:r></w:p>";
	}

	private static final String WPG = "http://schemas.microsoft.com/office/word/2010/wordprocessingGroup";
	private static final String WPC = "http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas";

	/** A group of the anchor's 180 x 90pt, its child space twice that: its children at half their size */
	private static String group(String kids) {
		return "<wpg:wgp><wpg:cNvGrpSpPr/><wpg:grpSpPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"2286000\" cy=\"1143000\"/>"
				+ "<a:chOff x=\"0\" y=\"0\"/><a:chExt cx=\"4572000\" cy=\"2286000\"/></a:xfrm></wpg:grpSpPr>" + kids + "</wpg:wgp>";
	}

	/** A child shape at (x, y), w x h, in EMU, filled; with a text box where text isn't null */
	private static String child(long x, long y, long w, long h, String prst, String fill, String text) {
		return "<wps:wsp><wps:cNvPr id=\"" + (x + y) % 9973 + "\" name=\"Child\"/><wps:cNvSpPr/><wps:spPr><a:xfrm><a:off x=\"" + x + "\" y=\"" + y
				+ "\"/><a:ext cx=\"" + w + "\" cy=\"" + h + "\"/></a:xfrm><a:prstGeom prst=\"" + prst + "\"><a:avLst/></a:prstGeom><a:solidFill><a:srgbClr val=\""
				+ fill + "\"/></a:solidFill></wps:spPr>"
				+ (text == null ? "" : "<wps:txbx><w:txbxContent><w:p><w:r><w:t>" + text + "</w:t></w:r></w:p></w:txbxContent></wps:txbx>")
				+ "<wps:bodyPr lIns=\"0\" tIns=\"0\" rIns=\"0\" bIns=\"0\"/></wps:wsp>";
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

	/** A shape with no text is a picture of its extent: an fo:instream-foreign-object of 180 x 90pt holding its
	 *  drawing, the SVG's viewBox the box with its overflow visible, so that its line, centred on the box's edge, is
	 *  painted whole; with no wrap it is out of the flow where its anchor puts it, as a picture is (phase 4).  */
	@Test
	public void aShapeWithNoTextIsAPictureOfItsExtent() throws Exception {
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = fo(anchored(RED_RECT, null, "<wps:bodyPr/>"), flags);
			assertEquals("flags " + flags, 1, svgs(doc).getLength());
			Element svg = (Element) svgs(doc).item(0);
			String text = XmlUtils.w3CDomNodeToString(svg).toLowerCase(java.util.Locale.ROOT);
			// Batik names a colour where SVG has a name for it
			assertTrue(text, text.contains("fill=\"red\"") || text.contains("rgb(255,0,0)") || text.contains("#ff0000"));
			assertPicture("flags " + flags, svg, 180, 90);
			// out of the flow, as a wrapNone picture is: positioned in a zero-height wrapper at the paragraph's top
			Element abs = (Element) svg.getParentNode().getParentNode().getParentNode();
			assertEquals("flags " + flags, "block-container", abs.getLocalName());
			assertEquals("flags " + flags, "absolute", abs.getAttribute("absolute-position"));
			assertEquals("flags " + flags, 72, pt(abs.getAttribute("left")), 1e-6);
			assertEquals("flags " + flags, 36, pt(abs.getAttribute("top")), 1e-6);
			assertEquals("flags " + flags, "0pt", ((Element) abs.getParentNode()).getAttribute("height"));
			assertNoHints("flags " + flags, doc);
		}
	}

	/** The svg is a picture's of w x h pt: in an fo:instream-foreign-object of that extent, its viewBox the box, a user
	 *  unit a point, what it paints past the box shown */
	private static Element assertPicture(String message, Element svg, double w, double h) {
		Element ifo = (Element) svg.getParentNode();
		assertEquals(message, "instream-foreign-object", ifo.getLocalName());
		assertEquals(message, w, pt(ifo.getAttribute("content-width")), 1e-6);
		assertEquals(message, h, pt(ifo.getAttribute("content-height")), 1e-6);
		String[] box = svg.getAttribute("viewBox").split(" ");
		assertEquals(message, 0, Double.parseDouble(box[0]), 0);
		assertEquals(message, 0, Double.parseDouble(box[1]), 0);
		assertEquals(message, w, Double.parseDouble(box[2]), 1e-6);
		assertEquals(message, h, Double.parseDouble(box[3]), 1e-6);
		assertEquals(message, w, pt(svg.getAttribute("width")), 1e-6);
		assertEquals(message, h, pt(svg.getAttribute("height")), 1e-6);
		assertEquals(message, "visible", svg.getAttribute("overflow"));
		return ifo;
	}

	/** WordLayoutFixups has read and removed every anchor hint */
	private static void assertNoHints(String message, org.w3c.dom.Document doc) {
		String all = XmlUtils.w3CDomNodeToString(doc.getDocumentElement());
		assertFalse(message, all.contains("docx4j-anchor"));
	}

	/** The nearest ancestor of this element named so, or null */
	private static Element ancestor(Element el, String localName) {
		for (org.w3c.dom.Node n = el.getParentNode(); n != null; n = n.getParentNode()) {
			if (n instanceof Element && localName.equals(n.getLocalName())) return (Element) n;
		}
		return null;
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

	/** A shape with no text wrapped square is floated as a picture is, the text beside it: at the column's left,
	 *  padded to its offset (72pt) and its distR (9pt) with its effect extent's r (1pt), its 36pt offset below the
	 *  paragraph's top as the float's;
	 *  until phase 4 it was drawn out of the flow, the text running under it.  The shape is 300pt wide, as a box under
	 *  60% of the column would be taken out of the flow in a cell anyway, under 90% of the 451.3pt column, where the
	 *  text goes below it, and nearer the left than the right.  */
	@Test
	public void aShapeWithNoTextWrappedSquareIsFloatedAsAPictureIs() throws Exception {
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = fo(anchored(RED_RECT, null, "<wps:bodyPr/>").replace("<wp:wrapNone/>", "<wp:wrapSquare wrapText=\"bothSides\"/>")
					.replace("cx=\"2286000\"", "cx=\"3810000\"").replace("distR=\"0\"", "distR=\"114300\"")
					.replace("<wp:wrapSquare", "<wp:effectExtent l=\"0\" t=\"0\" r=\"12700\" b=\"0\"/><wp:wrapSquare"), flags);
			assertEquals("flags " + flags, 1, svgs(doc).getLength());
			Element svg = (Element) svgs(doc).item(0);
			assertPicture("flags " + flags, svg, 300, 90);
			Element f = ancestor(svg, "float");
			assertNotNull("flags " + flags + ": floated", f);
			assertEquals("flags " + flags, "left", f.getAttribute("float"));
			Element holder = (Element) svg.getParentNode().getParentNode();
			assertEquals("flags " + flags, 72, pt(holder.getAttribute("padding-left")), 1e-6);
			// distR and the effect extent's r, as Word wraps it (17.3.7)
			assertEquals("flags " + flags, 10, pt(holder.getAttribute("padding-right")), 1e-6);
			assertNoHints("flags " + flags, doc);
		}
	}

	/** A shape with no text wrapped top and bottom reserves its band as a picture does: drawn at its offset, 36pt
	 *  down, out of the flow, and the band at the paragraph's top as tall as its offset and height, 36 + 90pt, less the
	 *  lines that fit above it (Word keeps those above; docx4j sets them below, the rest where Word's are: 17.3.7) */
	@Test
	public void aShapeWithNoTextWrappedTopAndBottomReservesItsBand() throws Exception {
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = fo(anchored(RED_RECT, null, "<wps:bodyPr/>").replace("<wp:wrapNone/>", "<wp:wrapTopAndBottom/>"), flags);
			Element svg = (Element) svgs(doc).item(0);
			assertPicture("flags " + flags, svg, 180, 90);
			Element abs = (Element) svg.getParentNode().getParentNode().getParentNode();
			assertEquals("flags " + flags, "absolute", abs.getAttribute("absolute-position"));
			assertEquals("flags " + flags, 36, pt(abs.getAttribute("top")), 1e-6);
			assertEquals("flags " + flags, 72, pt(abs.getAttribute("left")), 1e-6);
			Element para = (Element) abs.getParentNode().getParentNode();
			double pitch = pt(para.getAttribute("line-height"));
			assertTrue("flags " + flags + ": a pitch", pitch > 0 && pitch <= 36);
			Element band = null;
			for (org.w3c.dom.Node n = para.getFirstChild(); n != null; n = n.getNextSibling()) {
				if (n instanceof Element && "block-container".equals(n.getLocalName()) && !"0pt".equals(((Element) n).getAttribute("height"))) band = (Element) n;
			}
			assertNotNull("flags " + flags + ": the band", band);
			assertEquals("flags " + flags, 126 - Math.floor(36 / pitch) * pitch, pt(band.getAttribute("height")), 0.01);
			assertNoHints("flags " + flags, doc);
		}
	}

	/** An alignment places the box with its effect extent (ECMA-376 20.4.2.6; the probes: aligned right with r = 1pt,
	 *  the right edge 1pt in from the column's): a right float padded 1pt from the column's edge.  @since 17.3.7 */
	@Test
	public void anAlignmentPlacesTheBoxWithItsEffectExtent() throws Exception {
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = fo(anchored(RED_RECT, null, "<wps:bodyPr/>").replace("<wp:wrapNone/>", "<wp:wrapSquare wrapText=\"bothSides\"/>")
					.replace("<wp:posOffset>914400</wp:posOffset></wp:positionH>", "<wp:align>right</wp:align></wp:positionH>")
					.replace("<wp:wrapSquare", "<wp:effectExtent l=\"0\" t=\"0\" r=\"12700\" b=\"12700\"/><wp:wrapSquare"), flags);
			Element svg = (Element) svgs(doc).item(0);
			Element f = ancestor(svg, "float");
			assertNotNull("flags " + flags, f);
			assertEquals("flags " + flags, "right", f.getAttribute("float"));
			Element holder = (Element) svg.getParentNode().getParentNode();
			assertEquals("flags " + flags, 1, pt(holder.getAttribute("padding-right")), 0.01);
			// centred, the box with its effect extent (180 + 2pt) is centred in the 451.3pt column: 134.65pt in
			doc = fo(anchored(RED_RECT, null, "<wps:bodyPr/>").replace("<wp:wrapNone/>", "<wp:wrapSquare wrapText=\"bothSides\"/>")
					.replace("<wp:posOffset>914400</wp:posOffset></wp:positionH>", "<wp:align>center</wp:align></wp:positionH>")
					.replace("<wp:wrapSquare", "<wp:effectExtent l=\"0\" t=\"0\" r=\"25400\" b=\"0\"/><wp:wrapSquare"), flags);
			svg = (Element) svgs(doc).item(0);
			holder = (Element) svg.getParentNode().getParentNode();
			assertEquals("flags " + flags, "left", ((Element) holder.getParentNode()).getAttribute("float"));
			assertEquals("flags " + flags, 134.65, pt(holder.getAttribute("padding-left")), 0.01);
		}
	}

	/** Tight and through wrap a shape as a picture's do: as square, its box (phase 4) */
	@Test
	public void aShapeWithNoTextWrappedTightIsFloated() throws Exception {
		String tight = "<wp:wrapTight wrapText=\"bothSides\"><wp:wrapPolygon edited=\"0\"><wp:start x=\"0\" y=\"0\"/><wp:lineTo x=\"0\" y=\"21600\"/>"
				+ "<wp:lineTo x=\"21600\" y=\"21600\"/><wp:lineTo x=\"21600\" y=\"0\"/><wp:lineTo x=\"0\" y=\"0\"/></wp:wrapPolygon></wp:wrapTight>";
		for (String wrap : new String[] { tight, tight.replace("wrapTight", "wrapThrough") }) {
			for (int flags : BOTH) {
				org.w3c.dom.Document doc = fo(anchored(RED_RECT, null, "<wps:bodyPr/>").replace("<wp:wrapNone/>",
						"<wp:effectExtent l=\"0\" t=\"0\" r=\"12700\" b=\"12700\"/>" + wrap).replace("distR=\"0\"", "distR=\"114300\""), flags);
				Element svg = (Element) svgs(doc).item(0);
				assertPicture("flags " + flags, svg, 180, 90);
				assertNotNull("flags " + flags + " " + wrap, ancestor(svg, "float"));
				// Word wraps the polygon, without the effect extent (the probes: 225.22, not 226.18; 17.3.7)
				assertEquals("flags " + flags, 9, pt(((Element) svg.getParentNode().getParentNode()).getAttribute("padding-right")), 1e-6);
			}
		}
	}

	/** Behind the text a shape wrapped square is floated as one in front is, as Word wraps it (phase 4's wrap probes,
	 *  fw10-13 and fw10-01); with no wrap, behind the text, it takes no space */
	@Test
	public void aShapeWithNoTextBehindTheTextIsWrappedAsInFront() throws Exception {
		for (int flags : BOTH) {
			String behind = anchored(RED_RECT, null, "<wps:bodyPr/>").replace("behindDoc=\"0\"", "behindDoc=\"1\"");
			org.w3c.dom.Document doc = fo(behind.replace("<wp:wrapNone/>", "<wp:wrapSquare wrapText=\"bothSides\"/>"), flags);
			Element svg = (Element) svgs(doc).item(0);
			assertPicture("flags " + flags, svg, 180, 90);
			assertNotNull("flags " + flags, ancestor(svg, "float"));
			doc = fo(behind, flags);
			svg = (Element) svgs(doc).item(0);
			assertNull("flags " + flags, ancestor(svg, "float"));
			Element abs = (Element) svg.getParentNode().getParentNode().getParentNode();
			assertEquals("flags " + flags, "absolute", abs.getAttribute("absolute-position"));
			assertEquals("flags " + flags, "0pt", ((Element) abs.getParentNode()).getAttribute("height"));
		}
	}

	/** A shape that paints nothing but wraps keeps its space, the drawing empty, as a chart's box does (phase 4) */
	@Test
	public void aShapeThatPaintsNothingButWrapsKeepsItsSpace() throws Exception {
		String nothing = "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom><a:noFill/><a:ln><a:noFill/></a:ln>";
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = fo(anchored(nothing, null, "<wps:bodyPr/>").replace("<wp:wrapNone/>", "<wp:wrapSquare wrapText=\"bothSides\"/>"), flags);
			assertEquals("flags " + flags, 1, svgs(doc).getLength());
			Element svg = (Element) svgs(doc).item(0);
			assertPicture("flags " + flags, svg, 180, 90);
			assertNull("flags " + flags + ": nothing drawn", firstElement(svg));
			assertNotNull("flags " + flags, ancestor(svg, "float"));
		}
	}

	/** A line has no height: it is a picture of the least extent, 0.01pt, whose drawing, its 2pt line, is painted past
	 *  it (phase 4); a viewport of no height paints nothing */
	@Test
	public void aLineIsAPictureOfTheLeastExtent() throws Exception {
		String line = "<a:prstGeom prst=\"line\"><a:avLst/></a:prstGeom><a:ln w=\"25400\"><a:solidFill><a:srgbClr val=\"000000\"/></a:solidFill></a:ln>";
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = fo(anchored(line, null, "<wps:bodyPr/>").replace("cy=\"1143000\"", "cy=\"0\""), flags);
			assertEquals("flags " + flags, 1, svgs(doc).getLength());
			Element svg = (Element) svgs(doc).item(0);
			assertPicture("flags " + flags, svg, 180, 0.01);
			assertNotNull("flags " + flags + ": drawn", firstElement(svg));
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

	/** An inline shape with no text is an inline picture of its extent, in its line, its effect extent round it as
	 *  padding, as an inline picture's (phase 4; until then it wasn't drawn) */
	@Test
	public void anInlineShapeWithNoTextIsAnInlinePicture() throws Exception {
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = fo(inline(RED_RECT).replace("<wp:docPr", "<wp:effectExtent l=\"6350\" t=\"6350\" r=\"6350\" b=\"12700\"/><wp:docPr"), flags);
			assertEquals("flags " + flags, 1, svgs(doc).getLength());
			Element svg = (Element) svgs(doc).item(0);
			Element ifo = assertPicture("flags " + flags, svg, 180, 90);
			assertEquals("flags " + flags, 0.5, pt(ifo.getAttribute("padding-left")), 1e-6);
			assertEquals("flags " + flags, 0.5, pt(ifo.getAttribute("padding-top")), 1e-6);
			assertEquals("flags " + flags, 0.5, pt(ifo.getAttribute("padding-right")), 1e-6);
			assertEquals("flags " + flags, 1, pt(ifo.getAttribute("padding-bottom")), 1e-6);
			assertNull("flags " + flags, ancestor(svg, "block-container"));
			assertNull("flags " + flags, ancestor(svg, "inline-container"));
			assertNotNull("flags " + flags + ": in the paragraph", ancestor(svg, "block"));
		}
	}

	/** An inline group with no text is an inline picture too (phase 4) */
	@Test
	public void anInlineGroupWithNoTextIsAnInlinePicture() throws Exception {
		String para = anchoredGroup(WPG, group(child(0, 0, 1828800, 914400, "ellipse", "FF0000", null)));
		String drawing = para.substring(para.indexOf("<w:drawing>"), para.indexOf("</w:drawing>") + "</w:drawing>".length());
		String graphic = drawing.substring(drawing.indexOf("<a:graphic>"), drawing.indexOf("</wp:anchor>"));
		String body = "<w:p><w:r><w:drawing><wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\"><wp:extent cx=\"2286000\" cy=\"1143000\"/>"
				+ "<wp:docPr id=\"5\" name=\"Group 5\"/>" + graphic + "</wp:inline></w:drawing></w:r></w:p>";
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = fo(body, flags);
			assertEquals("flags " + flags, 1, svgs(doc).getLength());
			Element svg = (Element) svgs(doc).item(0);
			assertPicture("flags " + flags, svg, 180, 90);
			assertNull("flags " + flags, ancestor(svg, "block-container"));
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

	/** A group is drawn as one, its child's text in a container at the child's box: the child space twice the group's,
	 *  a child at (2in, 1in), 2in x 1in, is at (72, 36)pt, 72 x 36 (CR-080 phase 3).  @since 17.3.7 */
	@Test
	public void aGroupIsDrawnWithItsShapesText() throws Exception {
		String kids = child(0, 0, 1828800, 914400, "rtTriangle", "70AD47", null) + child(1828800, 914400, 1828800, 914400, "rect", "DEEAF6", "GROUP TEXT");
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = fo(anchoredGroup(WPG, group(kids)), flags);
			assertEquals("flags " + flags + ": one drawing", 1, svgs(doc).getLength());
			Element container = textContainer(doc, "GROUP TEXT");
			assertNotNull("flags " + flags + ": the drawing first in the group's container, the text in it", container);
			Element text = null;
			NodeList nl = container.getElementsByTagNameNS(FO_NS, "block-container");
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				if (el.getTextContent().contains("GROUP TEXT") && "absolute".equals(el.getAttribute("absolute-position"))) text = el;
			}
			assertNotNull("flags " + flags + ": the child's text container", text);
			assertEquals("flags " + flags, 72, pt(text.getAttribute("left")), 1e-6);
			assertEquals("flags " + flags, 36, pt(text.getAttribute("top")) - pt(text.getAttribute("padding-top")), 1e-6);
			assertEquals("flags " + flags + ": 72pt wide", 72, pt(text.getAttribute("width")) + pt(text.getAttribute("padding-right")), 1e-6);
		}
	}

	/** A group with text in mc:AlternateContent, its wpg Choice beside a VML Fallback as Word writes one, is drawn from
	 *  its Choice by default (CR-080 D13), where the Fallback painted nothing and lost the text.  @since 17.3.7 */
	@Test
	public void aGroupsChoiceIsDrawnByDefault() throws Exception {
		String kids = child(1828800, 914400, 1828800, 914400, "rect", "DEEAF6", "CHOICE TEXT");
		String para = anchoredGroup(WPG, group(kids));
		String drawing = para.substring(para.indexOf("<w:drawing>"), para.indexOf("</w:drawing>") + "</w:drawing>".length());
		String mc = "<mc:AlternateContent xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\"><mc:Choice Requires=\"wpg\">"
				+ drawing + "</mc:Choice><mc:Fallback><w:pict><v:group xmlns:v=\"urn:schemas-microsoft-com:vml\" style=\"width:180pt;height:90pt\""
				+ " coordsize=\"21600,21600\"><v:rect style=\"position:absolute;left:0;top:0;width:21600;height:21600\"><v:textbox><w:txbxContent>"
				+ "<w:p><w:r><w:t>FALLBACK TEXT</w:t></w:r></w:p></w:txbxContent></v:textbox></v:rect></v:group></w:pict></mc:Fallback></mc:AlternateContent>";
		String body = para.replace(drawing, mc);
		assertEquals("the default", "wps wpg wpc", org.docx4j.jaxb.McSelection.PREFER_CHOICE_DEFAULT);
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = fo(body, flags);
			String all = doc.getDocumentElement().getTextContent();
			assertTrue("flags " + flags, all.contains("CHOICE TEXT"));
			assertFalse("flags " + flags, all.contains("FALLBACK TEXT"));
			assertEquals("flags " + flags, 1, svgs(doc).getLength());
		}
	}

	/** A canvas draws its background, and a connector in a group shows no text, as Word shows none in one.  @since 17.3.7 */
	@Test
	public void aCanvasIsDrawnAndAConnectorShowsNoText() throws Exception {
		String canvas = "<wpc:wpc><wpc:bg><a:solidFill><a:srgbClr val=\"00FF00\"/></a:solidFill></wpc:bg>"
				+ child(457200, 228600, 914400, 457200, "rect", "0000FF", "CANVAS TEXT")
				+ child(0, 0, 914400, 457200, "straightConnector1", "000000", "HIDDEN") + "</wpc:wpc>";
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = fo(anchoredGroup(WPC, canvas), flags);
			assertEquals("flags " + flags, 1, svgs(doc).getLength());
			String svg = XmlUtils.w3CDomNodeToString(svgs(doc).item(0)).toLowerCase(java.util.Locale.ROOT);
			assertTrue(svg, svg.contains("lime") || svg.contains("rgb(0,255,0)") || svg.contains("#00ff00"));
			String all = doc.getDocumentElement().getTextContent();
			assertTrue("flags " + flags, all.contains("CANVAS TEXT"));
			assertFalse("flags " + flags + ": a connector's text", all.contains("HIDDEN"));
		}
	}

	/** A group with no text anchored and wrapped square is a picture floated as a shape with none is (phase 4):
	 *  360pt wide, under 90% of the column.  @since 17.3.7 */
	@Test
	public void aGroupWithNoTextWrappedSquareIsFloated() throws Exception {
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = fo(anchoredGroup(WPG, group(child(0, 0, 1828800, 914400, "ellipse", "FF0000", null)))
					.replace("<wp:wrapNone/>", "<wp:wrapSquare wrapText=\"bothSides\"/>").replace("cx=\"2286000\"", "cx=\"4572000\""), flags);
			assertEquals("flags " + flags, 1, svgs(doc).getLength());
			Element svg = (Element) svgs(doc).item(0);
			assertPicture("flags " + flags, svg, 360, 90);
			assertNotNull("flags " + flags, ancestor(svg, "float"));
			assertNull("flags " + flags + ": no text container", textContainer(doc, null));
			assertNoHints("flags " + flags, doc);
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
