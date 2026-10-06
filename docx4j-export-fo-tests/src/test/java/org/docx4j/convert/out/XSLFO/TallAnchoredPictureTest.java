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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * An anchored picture taller than the page's body (corpus document 4957: a 787.2pt wrapSquare
 * class diagram on a 697.5pt body).  Word draws it overflowing the margins and puts what follows
 * on the next page; FOP cannot place a block-container taller than the body, and painted the
 * picture on two pages.  So it reserves nothing: it is positioned at its offset in a zero-height
 * container, as a picture that does not wrap is, and overflows the margins as Word draws it.
 *
 * @since 17.3.1
 */
public class TallAnchoredPictureTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	/** A4, 1 inch margins, no header or footer: a 697.9pt body. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"708\" w:footer=\"708\" w:gutter=\"0\"/></w:sectPr>";
	private static final double BODY_PT = 16838 / 20.0 - 2 * 72;

	private static byte[] png() throws Exception {
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(60, 100, java.awt.image.BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		javax.imageio.ImageIO.write(img, "png", out);
		return out.toByteArray();
	}

	private static String anchoredPicture(String relId, long cyEmu) {
		return "<w:r><w:drawing xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<wp:anchor distT=\"0\" distB=\"0\" distL=\"114300\" distR=\"114300\" simplePos=\"0\""
				+ " relativeHeight=\"251658240\" behindDoc=\"0\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\">"
				+ "<wp:simplePos x=\"0\" y=\"0\"/>"
				+ "<wp:positionH relativeFrom=\"margin\"><wp:posOffset>0</wp:posOffset></wp:positionH>"
				+ "<wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>0</wp:posOffset></wp:positionV>"
				+ "<wp:extent cx=\"5924550\" cy=\"" + cyEmu + "\"/><wp:wrapTopAndBottom/>"
				+ "<wp:docPr id=\"1\" name=\"diagram\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
				+ "<pic:pic><pic:nvPicPr><pic:cNvPr id=\"101\" name=\"diagram\"/><pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill><a:blip r:embed=\"" + relId + "\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"5924550\" cy=\"" + cyEmu + "\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>"
				+ "</a:graphicData></a:graphic></wp:anchor></w:drawing></w:r>";
	}

	/** The block-container reserving the picture's height in the flow. */
	private Element reservation(long cyEmu, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		BinaryPartAbstractImage img = BinaryPartAbstractImage.createImagePart(pkg, png());
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:p><w:r><w:t>before</w:t></w:r></w:p>"
				+ "<w:p>" + anchoredPicture(img.getSourceRelationship().getId(), cyEmu) + "</w:p>"
				+ "<w:p><w:r><w:t>after</w:t></w:r></w:p>" + SECT_PR + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(baos.toByteArray());
		NodeList graphics = doc.getElementsByTagNameNS(FO, "external-graphic");
		assertEquals(1, graphics.getLength());
		for (org.w3c.dom.Node n = graphics.item(0).getParentNode(); n instanceof Element; n = n.getParentNode()) {
			if ("block-container".equals(n.getLocalName())) return (Element) n;
		}
		throw new AssertionError("the picture is not in a block-container");
	}

	private static double pt(String length) {
		return Double.parseDouble(length.replace("pt", ""));
	}

	private void checkTall(int flags) throws Exception {
		Element bc = reservation(9997440L, flags);   // 787.2pt, more than the body's 697.9
		assertEquals("positioned", "absolute", bc.getAttribute("absolute-position"));
		Element wrapper = (Element) bc.getParentNode();
		assertEquals("in a container reserving nothing", "0pt", wrapper.getAttribute("height"));
		assertTrue(BODY_PT < 787.2);
	}

	@Test
	public void aPictureTallerThanTheBodyReservesNothing() throws Exception {
		checkTall(Docx4J.FLAG_NONE);
		checkTall(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** A picture that fits reserves its own height, as before. */
	@Test
	public void aPictureThatFitsReservesItsHeight() throws Exception {
		Element bc = reservation(3810000L, Docx4J.FLAG_NONE);   // 300pt
		assertEquals(300.0, pt(bc.getAttribute("height")), 0.01);
	}
}
