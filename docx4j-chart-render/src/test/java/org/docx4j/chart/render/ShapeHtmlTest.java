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
package org.docx4j.chart.render;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.model.shapes.ShapeDrawer;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A DrawingML shape, group or canvas in the HTML output, by the visitor and the XSLT alike (CR-080 phase 5): a box of
 * its extent in points holding the SVG this module draws (found by docx4j-core through {@link ShapeDrawer}), and each
 * shape's text over it, in the geometry's text rectangle, inset, anchored; the box whole inside its paragraph.
 */
public class ShapeHtmlTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\""
			+ " xmlns:wpg=\"http://schemas.microsoft.com/office/word/2010/wordprocessingGroup\"";
	private static final String SVG_NS = "http://www.w3.org/2000/svg";
	private static final int[] BOTH = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static final String RED = "<a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill><a:ln w=\"12700\"><a:solidFill><a:srgbClr val=\"000000\"/></a:solidFill></a:ln>";
	private static final String GRADIENT = "<a:gradFill><a:gsLst><a:gs pos=\"0\"><a:srgbClr val=\"FF0000\"/></a:gs><a:gs pos=\"100000\">"
			+ "<a:srgbClr val=\"0000FF\"/></a:gs></a:gsLst><a:lin ang=\"0\" scaled=\"0\"/></a:gradFill>";

	/** A wps:wsp 180 x 90pt of this preset and fill, with a text box where text isn't null, its bodyPr as given */
	private static String wsp(String prst, String fill, String text, String bodyPr) {
		return "<wps:wsp><wps:cNvSpPr/><wps:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"2286000\" cy=\"1143000\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"" + prst + "\"><a:avLst/></a:prstGeom>" + fill + "</wps:spPr>"
				+ (text == null ? "" : "<wps:txbx><w:txbxContent>" + text + "</w:txbxContent></wps:txbx>") + bodyPr + "</wps:wsp>";
	}

	private static String para(String text) {
		return "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static String graphic(String uri, String content) {
		return "<a:graphic><a:graphicData uri=\"" + uri + "\">" + content + "</a:graphicData></a:graphic>";
	}

	private static final String WPS = "http://schemas.microsoft.com/office/word/2010/wordprocessingShape";
	private static final String WPG = "http://schemas.microsoft.com/office/word/2010/wordprocessingGroup";

	private static String inline(int id, String graphic) {
		return "<w:r><w:drawing><wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\"><wp:extent cx=\"2286000\" cy=\"1143000\"/>"
				+ "<wp:docPr id=\"" + id + "\" name=\"Shape " + id + "\"/>" + graphic + "</wp:inline></w:drawing></w:r>";
	}

	private static String anchor(int id, String wrap, String graphic) {
		return "<w:r><w:drawing><wp:anchor distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\" simplePos=\"0\" relativeHeight=\"1\" behindDoc=\"0\""
				+ " locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\"><wp:simplePos x=\"0\" y=\"0\"/>"
				+ "<wp:positionH relativeFrom=\"column\"><wp:posOffset>0</wp:posOffset></wp:positionH>"
				+ "<wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>0</wp:posOffset></wp:positionV>"
				+ "<wp:extent cx=\"2286000\" cy=\"1143000\"/>" + wrap + "<wp:docPr id=\"" + id + "\" name=\"Shape " + id + "\"/>" + graphic
				+ "</wp:anchor></w:drawing></w:r>";
	}

	private static org.w3c.dom.Document html(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) XmlUtils.unmarshalString("<w:document " + NS + "><w:body>" + body
				+ "</w:body></w:document>"));
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, baos, flags);
		// docx4j's parser refuses a DOCTYPE, as it should; the HTML's is dropped to read the rest
		String html = baos.toString("UTF-8").replaceFirst("<!DOCTYPE[^>]*>", "");
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(html.getBytes("UTF-8")));
	}

	private static List<Element> byClass(org.w3c.dom.Document doc, String cls) {
		List<Element> out = new ArrayList<Element>();
		NodeList nl = doc.getElementsByTagName("*");
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			if (cls.equals(e.getAttribute("class"))) out.add(e);
		}
		return out;
	}

	private static Element firstSvg(Element e) {
		NodeList nl = e.getElementsByTagNameNS(SVG_NS, "svg");
		return nl.getLength() == 0 ? null : (Element) nl.item(0);
	}

	private static String style(Element e, String property) {
		for (String d : e.getAttribute("style").split(";")) {
			int c = d.indexOf(':');
			if (c > 0 && d.substring(0, c).trim().equals(property)) return d.substring(c + 1).trim();
		}
		return null;
	}

	private static double pt(String v) {
		return Double.parseDouble(v.replace("pt", ""));
	}

	private static String flagName(int flags) {
		return flags == Docx4J.FLAG_NONE ? "visitor" : "XSLT";
	}

	@Test
	public void theDrawerIsFound() {
		assertTrue(ShapeDrawer.getDrawer() instanceof ShapeSvgDrawer);
	}

	/** A shape with text: its box in its line, its drawing the box, a user unit a point, what it paints past the box
	 *  shown; its text in a container at the box, set in by the ellipse's text rectangle and the insets, centred */
	@Test
	public void aShapeWithTextIsItsDrawingWithItsTextOverIt() throws Exception {
		String body = "<w:p><w:r><w:t xml:space=\"preserve\">before </w:t></w:r>"
				+ anchor(1, "<wp:wrapSquare wrapText=\"bothSides\"/>", graphic(WPS, wsp("ellipse", RED, para("IN THE ELLIPSE"),
						"<wps:bodyPr lIns=\"0\" tIns=\"0\" rIns=\"0\" bIns=\"0\" anchor=\"ctr\"/>")))
				+ "<w:r><w:t xml:space=\"preserve\"> after</w:t></w:r></w:p>";
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = html(body, flags);
			List<Element> boxes = byClass(doc, "docx4j-shape");
			assertEquals(flagName(flags), 1, boxes.size());
			Element box = boxes.get(0);
			assertEquals(flagName(flags), "span", box.getLocalName());
			assertEquals(flagName(flags), "inline-block", style(box, "display"));
			assertEquals(flagName(flags), 180, pt(style(box, "width")), 1e-6);
			assertEquals(flagName(flags), 90, pt(style(box, "height")), 1e-6);
			assertEquals(flagName(flags) + ": in its paragraph", "p", box.getParentNode().getLocalName());
			assertNull(flagName(flags) + ": text is no image", box.getAttribute("role").isEmpty() ? null : box.getAttribute("role"));
			Element svg = firstSvg(box);
			assertNotNull(flagName(flags), svg);
			assertEquals(flagName(flags), "0 0 180 90", svg.getAttribute("viewBox"));
			assertEquals(flagName(flags), "visible", svg.getAttribute("overflow"));
			assertEquals(flagName(flags), "true", svg.getAttribute("aria-hidden"));
			List<Element> texts = byClass(doc, "docx4j-shape-text");
			assertEquals(flagName(flags), 1, texts.size());
			Element t = texts.get(0);
			assertTrue(flagName(flags), t.getTextContent().contains("IN THE ELLIPSE"));
			// once: the text box's content isn't walked again outside the box
			String all = doc.getDocumentElement().getTextContent();
			assertEquals(flagName(flags), all.indexOf("IN THE ELLIPSE"), all.lastIndexOf("IN THE ELLIPSE"));
			// the ellipse's text rectangle: 90 - 90 cos 45 in from the sides, 45 - 45 sin 45 from top and foot, with
			// half the 1pt line
			String[] pad = style(t, "padding").split(" ");
			assertEquals(flagName(flags), 45 - 45 * Math.sin(Math.PI / 4) + 0.5, pt(pad[0]), 0.01);
			assertEquals(flagName(flags), 90 - 90 * Math.cos(Math.PI / 4) + 0.5, pt(pad[1]), 0.01);
			assertEquals(flagName(flags), "center", style(t, "justify-content"));
			// the paragraph as phrasing content, so that an HTML parser keeps the box whole in its paragraph
			assertEquals(flagName(flags), 0, t.getElementsByTagName("p").getLength());
			assertEquals(flagName(flags), 0, box.getElementsByTagName("p").getLength());
		}
	}

	/** A shape with no text is an image: its role, its docPr's name as its label */
	@Test
	public void aShapeWithNoTextIsAnImage() throws Exception {
		String body = "<w:p>" + inline(2, graphic(WPS, wsp("rect", RED, null, "<wps:bodyPr/>"))) + "</w:p>";
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = html(body, flags);
			Element box = byClass(doc, "docx4j-shape").get(0);
			assertEquals(flagName(flags), "img", box.getAttribute("role"));
			assertEquals(flagName(flags), "Shape 2", box.getAttribute("aria-label"));
			assertNotNull(flagName(flags), firstSvg(box));
			assertTrue(flagName(flags), byClass(doc, "docx4j-shape-text").isEmpty());
		}
	}

	/** A shape's shadow (CR-081 phase 3) is a picture in its drawing, under its fill, by both routes */
	@Test
	public void aShapesShadowIsInItsDrawing() throws Exception {
		String shadow = "<a:effectLst><a:outerShdw blurRad=\"50800\" dist=\"38100\" dir=\"2700000\" algn=\"tl\" rotWithShape=\"0\">"
				+ "<a:srgbClr val=\"000000\"><a:alpha val=\"40000\"/></a:srgbClr></a:outerShdw></a:effectLst>";
		String body = "<w:p>" + inline(2, graphic(WPS, wsp("rect", RED + shadow, null, "<wps:bodyPr/>"))) + "</w:p>";
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = html(body, flags);
			Element svg = firstSvg(byClass(doc, "docx4j-shape").get(0));
			NodeList images = svg.getElementsByTagNameNS(SVG_NS, "image");
			assertEquals(flagName(flags), 1, images.getLength());
			String text = XmlUtils.w3CDomNodeToString(svg);
			assertTrue(flagName(flags), text.indexOf("<image") < text.indexOf("<path"));
		}
	}

	/** An anchored shape with no wrap that paints nothing writes nothing; one in its line keeps its space */
	@Test
	public void aShapeThatPaintsNothingWithNoWrapWritesNothing() throws Exception {
		String nothing = wsp("rect", "<a:noFill/><a:ln><a:noFill/></a:ln>", null, "<wps:bodyPr/>");
		for (int flags : BOTH) {
			assertTrue(flagName(flags), byClass(html("<w:p>" + anchor(3, "<wp:wrapNone/>", graphic(WPS, nothing)) + "</w:p>", flags),
					"docx4j-shape").isEmpty());
			org.w3c.dom.Document doc = html("<w:p>" + inline(3, graphic(WPS, nothing)) + "</w:p>", flags);
			Element box = byClass(doc, "docx4j-shape").get(0);
			assertNull(flagName(flags), firstSvg(box));
		}
	}

	/** Two shapes in one paragraph: two boxes, each with its own label, and their drawings' ids each their own */
	@Test
	public void twoShapesAreTwoBoxesTheirIdsTheirOwn() throws Exception {
		String g = graphic(WPS, wsp("rect", GRADIENT, null, "<wps:bodyPr/>"));
		String body = "<w:p>" + inline(4, g) + inline(5, g) + "</w:p>";
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = html(body, flags);
			List<Element> boxes = byClass(doc, "docx4j-shape");
			assertEquals(flagName(flags), 2, boxes.size());
			assertEquals(flagName(flags), "Shape 4", boxes.get(0).getAttribute("aria-label"));
			assertEquals(flagName(flags), "Shape 5", boxes.get(1).getAttribute("aria-label"));
			Set<String> ids = new HashSet<String>();
			int count = 0;
			for (Element box : boxes) {
				NodeList all = box.getElementsByTagName("*");
				for (int i = 0; i < all.getLength(); i++) {
					String id = ((Element) all.item(i)).getAttribute("id");
					if (!id.isEmpty()) {
						ids.add(id);
						count++;
					}
				}
			}
			assertTrue(flagName(flags) + ": the gradient has an id", count > 0);
			assertEquals(flagName(flags) + ": no id twice", count, ids.size());
		}
	}

	/** A group's child's text in a container at the child's box: the child space twice the group's, a child at
	 *  (2in, 1in), 2in x 1in, is at (72, 36)pt, 72 x 36 */
	@Test
	public void aGroupsChildsTextIsAtItsBox() throws Exception {
		String kid = "<wps:wsp><wps:cNvPr id=\"101\" name=\"Child\"/><wps:cNvSpPr/><wps:spPr><a:xfrm><a:off x=\"1828800\" y=\"914400\"/>"
				+ "<a:ext cx=\"1828800\" cy=\"914400\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>" + RED + "</wps:spPr>"
				+ "<wps:txbx><w:txbxContent>" + para("GROUP TEXT") + "</w:txbxContent></wps:txbx>"
				+ "<wps:bodyPr lIns=\"0\" tIns=\"0\" rIns=\"0\" bIns=\"0\" anchor=\"b\"/></wps:wsp>";
		String group = "<wpg:wgp><wpg:cNvGrpSpPr/><wpg:grpSpPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"2286000\" cy=\"1143000\"/>"
				+ "<a:chOff x=\"0\" y=\"0\"/><a:chExt cx=\"4572000\" cy=\"2286000\"/></a:xfrm></wpg:grpSpPr>" + kid + "</wpg:wgp>";
		String body = "<w:p>" + inline(6, graphic(WPG, group)) + "</w:p>";
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = html(body, flags);
			Element box = byClass(doc, "docx4j-shape").get(0);
			assertNotNull(flagName(flags), firstSvg(box));
			Element t = byClass(doc, "docx4j-shape-text").get(0);
			assertTrue(flagName(flags), t.getTextContent().contains("GROUP TEXT"));
			assertEquals(flagName(flags), 72, pt(style(t, "left")), 1e-6);
			assertEquals(flagName(flags), 36, pt(style(t, "top")), 1e-6);
			assertEquals(flagName(flags), 72, pt(style(t, "width")), 1e-6);
			assertEquals(flagName(flags), "flex-end", style(t, "justify-content"));
		}
	}

	/** A shape in a shape's text box: each drawn, each text in its own container, the outer one's text in the outer
	 *  box (the XSLT pairs its boxes with the shapes in order, skipping those inside a text box) */
	@Test
	public void aShapeInsideAShapesTextIsItsOwn() throws Exception {
		String inner = "<w:p>" + inline(8, graphic(WPS, wsp("rect", RED, para("INNER"), "<wps:bodyPr/>"))) + "</w:p>";
		String outer = wsp("rect", RED, para("OUTER") + inner, "<wps:bodyPr/>");
		String body = "<w:p>" + inline(7, graphic(WPS, outer)) + "</w:p>";
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = html(body, flags);
			List<Element> boxes = byClass(doc, "docx4j-shape");
			assertEquals(flagName(flags), 2, boxes.size());
			List<Element> texts = byClass(doc, "docx4j-shape-text");
			assertEquals(flagName(flags), 2, texts.size());
			Element outerText = texts.get(0);
			assertTrue(flagName(flags), outerText.getTextContent().trim().startsWith("OUTER"));
			assertTrue(flagName(flags), isAncestor(boxes.get(0), boxes.get(1)));
			assertEquals(flagName(flags), "INNER", texts.get(1).getTextContent().trim());
		}
	}

	/** A group whose first child's text holds a shape with text, and whose second child has text: the second child's
	 *  text in the second child's container, where pairing the XSLT's boxes by every shape below the group, the nested
	 *  one included, gave it the nested shape's text */
	@Test
	public void aGroupsChildrenKeepTheirTextPastANestedShape() throws Exception {
		String nested = "<w:p>" + inline(11, graphic(WPS, wsp("rect", RED, para("NESTED"), "<wps:bodyPr/>"))) + "</w:p>";
		String first = "<wps:wsp><wps:cNvPr id=\"201\" name=\"First\"/><wps:cNvSpPr/><wps:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/>"
				+ "<a:ext cx=\"2286000\" cy=\"1143000\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>" + RED + "</wps:spPr>"
				+ "<wps:txbx><w:txbxContent>" + para("FIRST CHILD") + nested + "</w:txbxContent></wps:txbx><wps:bodyPr/></wps:wsp>";
		String second = "<wps:wsp><wps:cNvPr id=\"202\" name=\"Second\"/><wps:cNvSpPr/><wps:spPr><a:xfrm><a:off x=\"2286000\" y=\"0\"/>"
				+ "<a:ext cx=\"2286000\" cy=\"1143000\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>" + RED + "</wps:spPr>"
				+ "<wps:txbx><w:txbxContent>" + para("SECOND CHILD") + "</w:txbxContent></wps:txbx><wps:bodyPr/></wps:wsp>";
		String group = "<wpg:wgp><wpg:cNvGrpSpPr/><wpg:grpSpPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"2286000\" cy=\"1143000\"/>"
				+ "<a:chOff x=\"0\" y=\"0\"/><a:chExt cx=\"4572000\" cy=\"1143000\"/></a:xfrm></wpg:grpSpPr>" + first + second + "</wpg:wgp>";
		String body = "<w:p>" + inline(10, graphic(WPG, group)) + "</w:p>";
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = html(body, flags);
			Element box = byClass(doc, "docx4j-shape").get(0);
			List<Element> own = new ArrayList<Element>();
			for (Node n = box.getFirstChild(); n != null; n = n.getNextSibling()) {
				if (n instanceof Element && "docx4j-shape-text".equals(((Element) n).getAttribute("class"))) own.add((Element) n);
			}
			assertEquals(flagName(flags), 2, own.size());
			assertTrue(flagName(flags), own.get(0).getTextContent().contains("FIRST CHILD") && own.get(0).getTextContent().contains("NESTED"));
			assertEquals(flagName(flags), "SECOND CHILD", own.get(1).getTextContent().trim());
		}
	}

	private static boolean isAncestor(Node a, Node b) {
		for (Node n = b.getParentNode(); n != null; n = n.getParentNode()) if (n == a) return true;
		return false;
	}

	/** The box keeps its attributes through the paragraph's span merging, which rebuilds a run's span from its class
	 *  and style alone */
	@Test
	public void theBoxSurvivesTheParagraphsSpans() throws Exception {
		String body = "<w:p><w:r><w:t>text</w:t></w:r>" + inline(9, graphic(WPS, wsp("rect", RED, null, "<wps:bodyPr/>")))
				+ "<w:r><w:t>text</w:t></w:r></w:p>";
		for (int flags : BOTH) {
			Element box = byClass(html(body, flags), "docx4j-shape").get(0);
			assertEquals(flagName(flags), "img", box.getAttribute("role"));
			assertFalse(flagName(flags), box.getAttribute("aria-label").isEmpty());
		}
	}
}
