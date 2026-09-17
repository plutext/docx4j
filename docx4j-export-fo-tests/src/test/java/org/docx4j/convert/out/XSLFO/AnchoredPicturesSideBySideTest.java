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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Two {@code wrapTopAndBottom} anchored pictures in one paragraph sit <b>side by side</b>,
 * as Word draws them, and reserve the band once - whatever their horizontal offsets.
 *
 * <p>{@code wrapTopAndBottom} says that <em>text</em> does not flow beside the object, so
 * each such picture reserves a band of the flow.  It does not say that another
 * <em>picture</em> cannot sit there.  The four cases below are the four of the
 * {@code anchor-side-by-side} probe, read off Word's own PDF: 200 x 100pt pictures at
 * {@code positionV} 10pt and 12pt in a 451.3pt column.</p>
 *
 * <ul>
 * <li>at {@code posOffset} 0 and 240pt Word draws them at x=72.00 and x=312.00, tops
 *     149.20 and 151.20, and reserves 112pt - max(offset + height) - for the pair;
 * <li>at 0 and 150pt, <b>overlapping by 50pt</b>, Word bands them just the same: x=72.00
 *     and x=222.00, tops 314.59 and 316.59, one 112pt band, the two overlapping on the
 *     page with the later picture painted on top;
 * <li>at 0 and 200pt, touching exactly, likewise;
 * <li>anchored in <b>two</b> paragraphs, Word does not band them: 110pt and 112pt, with
 *     the paragraph between them in between, though their x values are disjoint.
 * </ul>
 *
 * <p>The rule is not that the two share a {@code positionV}: every pair's two tops are
 * 2.00pt apart in Word's PDF, the offsets the docx asks for, so each keeps its own.  What
 * the band does is stop one reserving space against the other.</p>
 *
 * @since 17.1.1
 */
public class AnchoredPicturesSideBySideTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	/** A4 portrait, 1in margins: a 451.3pt text column, which two 200pt pictures fit in. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static final long EMU_PT = 12700L;

	private static byte[] png() throws Exception {
		java.awt.image.BufferedImage img =
				new java.awt.image.BufferedImage(80, 60, java.awt.image.BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics g = img.getGraphics();
		g.setColor(java.awt.Color.GRAY);
		g.fillRect(0, 0, 80, 60);
		g.dispose();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		javax.imageio.ImageIO.write(img, "png", out);
		return out.toByteArray();
	}

	/** An anchored, top-and-bottom-wrapped picture at the given offsets from the column's
	 *  left and the paragraph's top, in points. */
	private static String anchored(String relId, double wPt, double hPt, double xPt, double yPt, int id) {
		return "<w:r><w:drawing xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<wp:anchor distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\" simplePos=\"0\""
				+ " relativeHeight=\"251658240\" behindDoc=\"0\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\">"
				+ "<wp:simplePos x=\"0\" y=\"0\"/>"
				+ "<wp:positionH relativeFrom=\"column\"><wp:posOffset>"
				+ Math.round(xPt * EMU_PT) + "</wp:posOffset></wp:positionH>"
				+ "<wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>"
				+ Math.round(yPt * EMU_PT) + "</wp:posOffset></wp:positionV>"
				+ "<wp:extent cx=\"" + Math.round(wPt * EMU_PT) + "\" cy=\"" + Math.round(hPt * EMU_PT) + "\"/>"
				+ "<wp:wrapTopAndBottom/>"
				+ "<wp:docPr id=\"" + id + "\" name=\"anchor" + id + "\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
				+ "<pic:pic><pic:nvPicPr><pic:cNvPr id=\"" + (100 + id) + "\" name=\"a" + id + "\"/>"
				+ "<pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill><a:blip r:embed=\"" + relId + "\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"" + Math.round(wPt * EMU_PT)
				+ "\" cy=\"" + Math.round(hPt * EMU_PT) + "\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>"
				+ "</a:graphicData></a:graphic></wp:anchor></w:drawing></w:r>";
	}

	/** One paragraph holding the given anchored runs, then a paragraph of text. */
	private org.w3c.dom.Document fo(int flags, String runs) throws Exception {
		return foBody(flags, "<w:p>" + runs
				+ "<w:r><w:t>text of the anchor paragraph</w:t></w:r></w:p>");
	}

	/** The given body paragraphs, then a paragraph of text and the section properties. */
	private org.w3c.dom.Document foBody(int flags, String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage img =
				org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage
					.createImagePart(pkg, png());
		String relId = img.getSourceRelationship().getId();

		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body.replace("@REL@", relId)
				+ "<w:p><w:r><w:t>the paragraph after</w:t></w:r></w:p>"
				+ SECT_PR + "</w:body></w:document>"));

		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	private static Element graphic(org.w3c.dom.Document doc, String contentWidth) {
		NodeList list = doc.getElementsByTagNameNS(FO, "external-graphic");
		for (int i = 0; i < list.getLength(); i++) {
			Element g = (Element) list.item(i);
			if (contentWidth.equals(g.getAttribute("content-width"))) return g;
		}
		return null;
	}

	private static Element ancestor(Element el, String localName, String attribute) {
		for (Node n = el.getParentNode(); n instanceof Element; n = n.getParentNode()) {
			Element e = (Element) n;
			if (localName.equals(e.getLocalName()) && e.hasAttribute(attribute)) return e;
		}
		return null;
	}

	/** The first container which reserves height in the flow, or null. */
	private static Element reserving(org.w3c.dom.Document doc) {
		return reservingHeights(doc).isEmpty() ? null : reservingElements(doc).get(0);
	}

	/** The containers which reserve height in the flow (a band is one of them), in
	 *  document order. */
	private static List<Element> reservingElements(org.w3c.dom.Document doc) {
		List<Element> found = new ArrayList<Element>();
		NodeList list = doc.getElementsByTagNameNS(FO, "block-container");
		for (int i = 0; i < list.getLength(); i++) {
			Element c = (Element) list.item(i);
			if (c.hasAttribute("absolute-position")) continue;
			String h = c.getAttribute("height");
			if (h.length() > 0 && !"0pt".equals(h)) found.add(c);
		}
		return found;
	}

	/** Their heights, in document order. */
	private static List<String> reservingHeights(org.w3c.dom.Document doc) {
		List<String> heights = new ArrayList<String>();
		for (Element c : reservingElements(doc)) heights.add(c.getAttribute("height"));
		return heights;
	}

	private static int reservingContainers(org.w3c.dom.Document doc) {
		return reservingElements(doc).size();
	}

	/** One named attribute of each absolutely positioned container, in document order -
	 *  which is the order the members of a band are painted in. */
	private static List<String> positioned(org.w3c.dom.Document doc, String attribute) {
		List<String> values = new ArrayList<String>();
		NodeList list = doc.getElementsByTagNameNS(FO, "block-container");
		for (int i = 0; i < list.getLength(); i++) {
			Element c = (Element) list.item(i);
			if (c.hasAttribute("absolute-position")) values.add(c.getAttribute(attribute));
		}
		return values;
	}

	/**
	 * One band holding both pictures where the docx puts them, 112pt tall - the greatest
	 * of its members' offset plus height - and not two bands of 110 and 112 one under the
	 * other.  The members come out in document order, which is the order Word paints them
	 * in, so where they overlap the later anchor is on top.
	 *
	 * @param secondX the second picture's {@code posOffset} from the column, in points
	 */
	private void bandedPair(int flags, int secondX) throws Exception {
		org.w3c.dom.Document doc = fo(flags,
				anchored("@REL@", 200, 100, 0, 10, 1) + anchored("@REL@", 200, 100, secondX, 12, 2));

		assertEquals("one band, not one container each", 1, reservingContainers(doc));
		assertNotNull("the pictures are in the FO", graphic(doc, "200pt"));

		List<String> lefts = positioned(doc, "left");
		List<String> tops = positioned(doc, "top");
		assertEquals("both pictures are placed within the band", 2, lefts.size());
		assertEquals("the first at the docx's own x", "0pt", lefts.get(0));
		assertEquals("the second at its own x, not below the first", secondX + "pt", lefts.get(1));
		assertEquals("each keeps its own vertical offset", "10pt", tops.get(0));
		assertEquals("the rule is not that they share a positionV", "12pt", tops.get(1));

		Element band = reserving(doc);
		assertNotNull("the band reserves the flow's height once", band);
		assertEquals("the greatest extent of its members, 12 + 100", "112pt", band.getAttribute("height"));
	}

	/** (a) Two 200pt pictures at x=0 and x=240 in a 451.3pt column: room for both, and
	 *  Word bands them - x=72.00 and x=312.00, tops 149.20 and 151.20. */
	private void sideBySide(int flags) throws Exception {
		bandedPair(flags, 240);
	}

	/** (b) The same pair at x=0 and x=150, <b>overlapping by 50pt</b>: Word bands them
	 *  just the same - x=72.00 and x=222.00, tops 314.59 and 316.59, one 112pt band, the
	 *  two overlapping on the page with the later picture painted on top.  Until the
	 *  golden was read, the overlap stacked them: 222pt reserved for Word's 112, and the
	 *  pair in reverse order. */
	private void overlapping(int flags) throws Exception {
		bandedPair(flags, 150);
	}

	/** (c) And at x=0 and x=200, touching exactly at the boundary, which is banded as the
	 *  disjoint pair is. */
	private void touching(int flags) throws Exception {
		bandedPair(flags, 200);
	}

	/**
	 * (d) The disjoint pair of (a) split over two paragraphs with a paragraph of text
	 * between them: Word does <b>not</b> band these.  Its first picture takes a 110pt
	 * band, the text follows, and the second picture then takes its own 112pt band from
	 * the second anchor paragraph's own top.  A band never crosses a paragraph.
	 */
	private void twoParagraphs(int flags) throws Exception {
		org.w3c.dom.Document doc = foBody(flags,
				"<w:p>" + anchored("@REL@", 200, 100, 0, 10, 1)
				+ "<w:r><w:t>the first anchor paragraph</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:t>one paragraph of text, between the two anchored ones</w:t></w:r></w:p>"
				+ "<w:p>" + anchored("@REL@", 200, 100, 240, 12, 2)
				+ "<w:r><w:t>the second anchor paragraph</w:t></w:r></w:p>");

		assertEquals("a band each: a band never crosses a paragraph", 2, reservingContainers(doc));
		assertEquals("neither is positioned within a band", 0, positioned(doc, "left").size());

		List<String> heights = reservingHeights(doc);
		assertEquals("the first reserves its own offset plus its height", "110pt", heights.get(0));
		assertEquals("the second its own", "112pt", heights.get(1));
	}

	/** One picture alone is written exactly as it was before this rule. */
	private void alone(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(flags, anchored("@REL@", 200, 100, 30, 10, 1));

		assertEquals(1, reservingContainers(doc));
		Element g = graphic(doc, "200pt");
		assertNotNull(g);
		assertNull("not positioned", ancestor(g, "block-container", "absolute-position"));
		Element reserved = ancestor(g, "block-container", "height");
		assertNotNull(reserved);
		assertEquals("its offset plus its height", "110pt", reserved.getAttribute("height"));
	}

	@Test
	public void sideBySideVisitor() throws Exception {
		sideBySide(Docx4J.FLAG_NONE);
	}

	@Test
	public void sideBySideXslt() throws Exception {
		sideBySide(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void overlappingVisitor() throws Exception {
		overlapping(Docx4J.FLAG_NONE);
	}

	@Test
	public void overlappingXslt() throws Exception {
		overlapping(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void touchingVisitor() throws Exception {
		touching(Docx4J.FLAG_NONE);
	}

	@Test
	public void touchingXslt() throws Exception {
		touching(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void twoParagraphsVisitor() throws Exception {
		twoParagraphs(Docx4J.FLAG_NONE);
	}

	@Test
	public void twoParagraphsXslt() throws Exception {
		twoParagraphs(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void aloneVisitor() throws Exception {
		alone(Docx4J.FLAG_NONE);
	}

	@Test
	public void aloneXslt() throws Exception {
		alone(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
