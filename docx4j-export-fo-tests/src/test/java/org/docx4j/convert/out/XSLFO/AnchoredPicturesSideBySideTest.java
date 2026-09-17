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
 * Two {@code wrapTopAndBottom} anchored pictures which do not overlap horizontally sit
 * <b>side by side</b>, as Word draws them, and reserve the band once.
 *
 * <p>{@code wrapTopAndBottom} says that <em>text</em> does not flow beside the object, so
 * each such picture reserves a band of the flow.  It does not say that another
 * <em>picture</em> cannot sit there.  Measured on a corpus document with seven of them,
 * each about 225pt wide in a 470pt column, from Word's own PDF: its page 3 carries four,
 * two at x=73.85 and x=308.25 on one baseline and two at x=78.75 and x=317.20 on another,
 * and its page 4 carries three.  Ours put each below the last - the second of a pair
 * landed 210.75pt lower - and the document ran to six pages against Word's four.</p>
 *
 * <p>The rule is not that the two share a {@code positionV}: two of that document's pairs
 * sit 0.25pt and 2.00pt apart in Word's own PDF, so each keeps its own offset. What the
 * band does is stop one reserving space against the other.</p>
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

	private org.w3c.dom.Document fo(int flags, String runs) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage img =
				org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage
					.createImagePart(pkg, png());
		String relId = img.getSourceRelationship().getId();

		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p>" + runs.replace("@REL@", relId)
				+ "<w:r><w:t>text of the anchor paragraph</w:t></w:r></w:p>"
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
		NodeList list = doc.getElementsByTagNameNS(FO, "block-container");
		for (int i = 0; i < list.getLength(); i++) {
			Element c = (Element) list.item(i);
			if (c.hasAttribute("absolute-position")) continue;
			String h = c.getAttribute("height");
			if (h.length() > 0 && !"0pt".equals(h)) return c;
		}
		return null;
	}

	/** The containers which reserve height in the flow (a band is one of them). */
	private static int reservingContainers(org.w3c.dom.Document doc) {
		int n = 0;
		NodeList list = doc.getElementsByTagNameNS(FO, "block-container");
		for (int i = 0; i < list.getLength(); i++) {
			Element c = (Element) list.item(i);
			if (c.hasAttribute("absolute-position")) continue;
			String h = c.getAttribute("height");
			if (h.length() > 0 && !"0pt".equals(h)) n++;
		}
		return n;
	}

	/**
	 * Two 200pt pictures at x=0 and x=240 in a 451.3pt column, 10pt and 12pt below the
	 * paragraph's top: one band 112pt tall holding both where the docx puts them, not two
	 * bands of 110 and 112 one under the other.
	 */
	private void sideBySide(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(flags,
				anchored("@REL@", 200, 100, 0, 10, 1) + anchored("@REL@", 200, 100, 240, 12, 2));

		assertEquals("one band, not one container each", 1, reservingContainers(doc));

		Element left = graphic(doc, "200pt");
		assertNotNull("the pictures are in the FO", left);

		NodeList placed = doc.getElementsByTagNameNS(FO, "block-container");
		int positioned = 0;
		String[] lefts = new String[2], tops = new String[2];
		for (int i = 0; i < placed.getLength(); i++) {
			Element c = (Element) placed.item(i);
			if (!c.hasAttribute("absolute-position")) continue;
			if (positioned < 2) {
				lefts[positioned] = c.getAttribute("left");
				tops[positioned] = c.getAttribute("top");
			}
			positioned++;
		}
		assertEquals("both pictures are placed within the band", 2, positioned);
		assertEquals("the first at the docx's own x", "0pt", lefts[0]);
		assertEquals("the second at its own x, not below the first", "240pt", lefts[1]);
		assertEquals("each keeps its own vertical offset", "10pt", tops[0]);
		assertEquals("the rule is not that they share a positionV", "12pt", tops[1]);

		Element band = reserving(doc);
		assertNotNull("the band reserves the flow's height once", band);
		assertEquals("the greatest extent of its members, 12 + 100", "112pt", band.getAttribute("height"));
	}

	/**
	 * And two which <b>do</b> overlap horizontally are stacked as before: Word puts the
	 * second below the first there, and so must we.
	 */
	private void overlapping(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(flags,
				anchored("@REL@", 200, 100, 0, 10, 1) + anchored("@REL@", 200, 100, 150, 12, 2));

		assertEquals("a container each, as before", 2, reservingContainers(doc));
		Element g = graphic(doc, "200pt");
		assertNotNull(g);
		assertNull("neither is positioned", ancestor(g, "block-container", "absolute-position"));
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
	public void aloneVisitor() throws Exception {
		alone(Docx4J.FLAG_NONE);
	}

	@Test
	public void aloneXslt() throws Exception {
		alone(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
