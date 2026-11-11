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
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A picture's effects in the HTML output (CR-081 phase 1), by the visitor and the XSLT alike: an inline svg in the img's
 * place, the picture's frame as CSS pixels (4/3 a point), labelled as the img is, its overflow visible, the effect
 * pictures round the picture, an inline picture's effect extent round it as margin. With no effects, the img.
 */
public class PictureEffectsHtmlTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
			+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"";
	private static final String SVG_NS = "http://www.w3.org/2000/svg";
	private static final String XLINK_NS = "http://www.w3.org/1999/xlink";
	private static final int[] BOTH = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };
	private static final String SHADOW = "<a:effectLst><a:outerShdw blurRad=\"50800\" dist=\"63500\" dir=\"2700000\" algn=\"tl\" rotWithShape=\"0\">"
			+ "<a:srgbClr val=\"000000\"><a:alpha val=\"40000\"/></a:srgbClr></a:outerShdw></a:effectLst>";

	private static byte[] picture() throws Exception {
		BufferedImage img = new BufferedImage(160, 120, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setColor(new Color(40, 120, 200));
		g.fillRect(0, 0, 160, 120);
		g.dispose();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(img, "png", out);
		return out.toByteArray();
	}

	private static org.w3c.dom.Document html(String effects, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, picture());
		String relId = part.getSourceRelationships().get(0).getId();
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) XmlUtils.unmarshalString("<w:document " + NS + "><w:body><w:p><w:r><w:drawing>"
				+ "<wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\"><wp:extent cx=\"1828800\" cy=\"1371600\"/>"
				+ "<wp:effectExtent l=\"0\" t=\"0\" r=\"76200\" b=\"76200\"/><wp:docPr id=\"1\" name=\"Picture 1\" descr=\"a photo\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><pic:pic><pic:nvPicPr><pic:cNvPr id=\"1\" name=\"p\"/>"
				+ "<pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed=\"" + relId + "\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"1828800\" cy=\"1371600\"/></a:xfrm>"
				+ (effects != null && effects.startsWith("<a:prstGeom") ? effects : "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>" + (effects == null ? "" : effects))
				+ "</pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r>"
				+ "<w:r><w:t>after</w:t></w:r></w:p></w:body></w:document>"));
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, baos, flags);
		String html = baos.toString("UTF-8").replaceFirst("<!DOCTYPE[^>]*>", "");
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(html.getBytes("UTF-8")));
	}

	private static String route(int flags) {
		return flags == Docx4J.FLAG_NONE ? "visitor" : "XSLT";
	}

	@Test
	public void aPictureWithEffectsIsAnInlineSvg() throws Exception {
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = html(SHADOW, flags);
			assertEquals(route(flags), 0, doc.getElementsByTagName("img").getLength());
			NodeList svgs = doc.getElementsByTagNameNS(SVG_NS, "svg");
			assertEquals(route(flags), 1, svgs.getLength());
			Element svg = (Element) svgs.item(0);
			assertEquals(route(flags), "192", svg.getAttribute("width"));
			assertEquals(route(flags), "144", svg.getAttribute("height"));
			assertEquals(route(flags), "0 0 144 108", svg.getAttribute("viewBox"));
			assertEquals(route(flags), "img", svg.getAttribute("role"));
			assertEquals(route(flags), "a photo", svg.getAttribute("aria-label"));
			String style = svg.getAttribute("style");
			assertTrue(route(flags) + ": " + style, style.contains("overflow:visible") && style.contains("margin:0px 8px 8px 0px"));
			NodeList images = svg.getElementsByTagNameNS(SVG_NS, "image");
			assertEquals(route(flags), 2, images.getLength());
			assertTrue(route(flags), ((Element) images.item(0)).getAttributeNS(XLINK_NS, "href").startsWith("data:image/png;base64,"));
			assertTrue(route(flags) + ": the paragraph goes on", doc.getDocumentElement().getTextContent().contains("after"));
		}
	}

	/** A 3-D picture (CR-081 phase 5a): one picture with alpha in its place, reaching past the frame (isometricTopUp) */
	@Test
	public void a3dPictureIsOnePictureInItsPlace() throws Exception {
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = html("<a:scene3d><a:camera prst=\"isometricTopUp\"/><a:lightRig rig=\"threePt\" dir=\"t\"/></a:scene3d><a:sp3d/>", flags);
			assertEquals(route(flags), 0, doc.getElementsByTagName("img").getLength());
			NodeList svgs = doc.getElementsByTagNameNS(SVG_NS, "svg");
			assertEquals(route(flags), 1, svgs.getLength());
			NodeList images = ((Element) svgs.item(0)).getElementsByTagNameNS(SVG_NS, "image");
			assertEquals(route(flags), 1, images.getLength());
			Element image = (Element) images.item(0);
			assertTrue(route(flags), image.getAttributeNS(XLINK_NS, "href").startsWith("data:image/png;base64,"));
			assertTrue(route(flags) + ": x " + image.getAttribute("x"), Double.parseDouble(image.getAttribute("x")) < 0);
		}
	}

	/**
	 * An oval frame with a line (CR-081 phase 2): the picture clipped to the ellipse, the line an svg in the picture's,
	 * after it; every id the picture's own (its wp:docPr's), none twice
	 */
	@Test
	public void aFramedPictureIsAnInlineSvg() throws Exception {
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = html("<a:prstGeom prst=\"ellipse\"><a:avLst/></a:prstGeom><a:ln w=\"76200\"><a:solidFill><a:srgbClr val=\"C00000\"/>"
					+ "</a:solidFill></a:ln>" + SHADOW, flags);
			assertEquals(route(flags), 0, doc.getElementsByTagName("img").getLength());
			Element svg = (Element) doc.getElementsByTagNameNS(SVG_NS, "svg").item(0);
			assertEquals(route(flags), "0 0 144 108", svg.getAttribute("viewBox"));
			Element clip = (Element) svg.getElementsByTagNameNS(SVG_NS, "clipPath").item(0);
			assertEquals(route(flags), "docx4j-picture-1-outline", clip.getAttribute("id"));
			Element last = (Element) svg.getLastChild();
			assertEquals(route(flags), "svg", last.getLocalName());
			assertEquals(route(flags), "-6", last.getAttribute("x"));
			java.util.Set<String> ids = new java.util.HashSet<String>();
			NodeList all = svg.getElementsByTagName("*");
			for (int i = 0; i < all.getLength(); i++) {
				String id = ((Element) all.item(i)).getAttribute("id");
				if (id.isEmpty()) continue;
				assertTrue(route(flags) + ": " + id, id.startsWith("docx4j-picture-1-"));
				assertTrue(route(flags) + ": " + id + " twice", ids.add(id));
			}
		}
	}

	@Test
	public void aPictureWithNoEffectsIsAnImg() throws Exception {
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = html(null, flags);
			assertEquals(route(flags), 1, doc.getElementsByTagName("img").getLength());
			assertEquals(route(flags), 0, doc.getElementsByTagNameNS(SVG_NS, "svg").getLength());
		}
	}
}
