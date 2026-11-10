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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

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
 * A picture's effects in the PDF output (CR-081 phase 1), by the visitor and the XSLT alike: the picture and its effects
 * one SVG in an fo:instream-foreign-object of the picture's frame, the effect pictures round the picture's own bytes;
 * an inline picture's effects cut at its effect extent, an anchored one's not; a JPEG still passed through to the PDF.
 * Its frame (phase 2): the outline clips it, the line is outside it, a fill behind shows through a transparent one.
 */
public class PictureEffectsFoTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
			+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"";
	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";
	private static final String SVG_NS = "http://www.w3.org/2000/svg";
	private static final String XLINK_NS = "http://www.w3.org/1999/xlink";
	private static final int[] BOTH = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"708\" w:footer=\"708\" w:gutter=\"0\"/></w:sectPr>";

	/** A shadow 4.5pt down and right (blur 4pt), black at 40% */
	static final String SHADOW = "<a:effectLst><a:outerShdw blurRad=\"50800\" dist=\"63500\" dir=\"2700000\" algn=\"tl\" rotWithShape=\"0\">"
			+ "<a:srgbClr val=\"000000\"><a:alpha val=\"40000\"/></a:srgbClr></a:outerShdw></a:effectLst>";
	/** A shadow 24pt straight down, unblurred */
	static final String SHADOW_DOWN = "<a:effectLst><a:outerShdw dist=\"304800\" dir=\"5400000\" algn=\"tl\" rotWithShape=\"0\">"
			+ "<a:srgbClr val=\"000000\"/></a:outerShdw></a:effectLst>";
	static final String SOFT_EDGE = "<a:effectLst><a:softEdge rad=\"63500\"/></a:effectLst>";

	/** An oval frame (CR-081 phase 2) */
	static final String OVAL = "<a:prstGeom prst=\"ellipse\"><a:avLst/></a:prstGeom>";
	/** A 6pt black line round a rectangle */
	static final String LINE = "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom><a:ln w=\"76200\"><a:solidFill><a:srgbClr val=\"000000\"/></a:solidFill>"
			+ "<a:miter lim=\"800000\"/></a:ln>";
	/** A grey fill behind a rectangle */
	static final String FILL = "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom><a:solidFill><a:srgbClr val=\"808080\"/></a:solidFill>";

	/** A picture with transparent corners */
	static byte[] transparentPicture() throws Exception {
		BufferedImage img = new BufferedImage(160, 120, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setColor(new Color(40, 120, 200));
		g.fillOval(0, 0, 160, 120);
		g.dispose();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(img, "png", out);
		return out.toByteArray();
	}

	static byte[] picture(String format) throws Exception {
		BufferedImage img = new BufferedImage(160, 120, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setColor(new Color(40, 120, 200));
		g.fillRect(0, 0, 160, 120);
		g.setColor(Color.RED);
		g.fillOval(10, 10, 30, 30);
		g.dispose();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(img, format, out);
		return out.toByteArray();
	}

	/** A 144 x 108pt picture with these effects, inline (effect extent "l t r b" in EMU, or null) or anchored square */
	static String drawing(String relId, String effects, String effectExtent, boolean anchored, String srcRect) {
		String ee = effectExtent == null ? "" : "<wp:effectExtent l=\"" + effectExtent.split(" ")[0] + "\" t=\"" + effectExtent.split(" ")[1]
				+ "\" r=\"" + effectExtent.split(" ")[2] + "\" b=\"" + effectExtent.split(" ")[3] + "\"/>";
		String graphic = "<wp:docPr id=\"1\" name=\"Picture 1\" descr=\"a photo\"/><a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
				+ "<pic:pic><pic:nvPicPr><pic:cNvPr id=\"1\" name=\"p\"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed=\"" + relId + "\"/>"
				+ (srcRect == null ? "" : srcRect) + "<a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/>"
				+ "<a:ext cx=\"1828800\" cy=\"1371600\"/></a:xfrm>"
				+ (effects != null && effects.startsWith("<a:prstGeom") ? effects : "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>" + (effects == null ? "" : effects))
				+ "</pic:spPr></pic:pic></a:graphicData></a:graphic>";
		if (anchored) {
			return "<w:r><w:drawing><wp:anchor distT=\"0\" distB=\"0\" distL=\"114300\" distR=\"114300\" simplePos=\"0\" relativeHeight=\"1\" behindDoc=\"0\""
					+ " locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\"><wp:simplePos x=\"0\" y=\"0\"/><wp:positionH relativeFrom=\"column\"><wp:posOffset>0</wp:posOffset>"
					+ "</wp:positionH><wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>0</wp:posOffset></wp:positionV><wp:extent cx=\"1828800\" cy=\"1371600\"/>"
					+ ee + "<wp:wrapSquare wrapText=\"bothSides\"/>" + graphic + "</wp:anchor></w:drawing></w:r>";
		}
		return "<w:r><w:drawing><wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\"><wp:extent cx=\"1828800\" cy=\"1371600\"/>" + ee + graphic
				+ "</wp:inline></w:drawing></w:r>";
	}

	static WordprocessingMLPackage pkg(byte[] picture, String effects, String effectExtent, boolean anchored, String srcRect) throws Exception {
		return pkg(picture, effects, effectExtent, anchored, srcRect, anchored);
	}

	/** @param words words after the picture in its paragraph; without, the picture is all its paragraph shows */
	static WordprocessingMLPackage pkg(byte[] picture, String effects, String effectExtent, boolean anchored, String srcRect, boolean words)
			throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, picture);
		String relId = part.getSourceRelationships().get(0).getId();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString("<w:document " + NS + "><w:body><w:p>"
				+ drawing(relId, effects, effectExtent, anchored, srcRect) + (words ? "<w:r><w:t>words beside the picture</w:t></w:r>" : "")
				+ "<w:r><w:t xml:space=\"preserve\"> </w:t></w:r></w:p><w:p><w:r><w:t>body text</w:t></w:r></w:p>" + SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	static org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private static String route(int flags) {
		return flags == Docx4J.FLAG_NONE ? "visitor" : "XSLT";
	}

	/** The SVG's image elements, in drawing order, as {href, x, y, width, height} */
	static List<String[]> images(Element svg) {
		List<String[]> out = new ArrayList<String[]>();
		NodeList n = svg.getElementsByTagNameNS(SVG_NS, "image");
		for (int i = 0; i < n.getLength(); i++) {
			Element e = (Element) n.item(i);
			out.add(new String[] { e.getAttributeNS(XLINK_NS, "href"), e.getAttribute("x"), e.getAttribute("y"), e.getAttribute("width"), e.getAttribute("height") });
		}
		return out;
	}

	static Element onlySvg(org.w3c.dom.Document fo, String route) {
		NodeList svgs = fo.getElementsByTagNameNS(SVG_NS, "svg");
		List<Element> outer = new ArrayList<Element>();
		for (int i = 0; i < svgs.getLength(); i++) {
			Element s = (Element) svgs.item(i);
			if (s.getParentNode() instanceof Element && "instream-foreign-object".equals(s.getParentNode().getLocalName())) outer.add(s);
		}
		assertEquals(route, 1, outer.size());
		return outer.get(0);
	}

	/** An inline picture with a shadow: one SVG of its frame, the shadow under the picture's own bytes, its effect
	 *  extent as padding, as an external-graphic's */
	@Test
	public void aPictureWithEffectsIsOneSvg() throws Exception {
		for (int flags : BOTH) {
			org.w3c.dom.Document fo = fo(pkg(picture("png"), SHADOW, "0 0 76200 76200", false, null), flags);
			assertEquals(route(flags), 0, fo.getElementsByTagNameNS(FO_NS, "external-graphic").getLength());
			Element svg = onlySvg(fo, route(flags));
			Element ifo = (Element) svg.getParentNode();
			assertEquals(route(flags), "144pt", ifo.getAttribute("content-width"));
			assertEquals(route(flags), "108pt", ifo.getAttribute("content-height"));
			assertEquals(route(flags), "6pt", ifo.getAttribute("padding-right"));
			assertEquals(route(flags), "6pt", ifo.getAttribute("padding-bottom"));
			assertEquals(route(flags), "0 0 144 108", svg.getAttribute("viewBox"));
			assertEquals(route(flags), "visible", svg.getAttribute("overflow"));
			List<String[]> images = images(svg);
			assertEquals(route(flags), 2, images.size());
			assertTrue(route(flags) + ": the shadow, a picture with alpha", images.get(0)[0].startsWith("data:image/png;base64,"));
			// its centre 4.5pt along 45 degrees from the picture's: 3.18pt right and down
			double cx = Double.parseDouble(images.get(0)[1]) + Double.parseDouble(images.get(0)[3]) / 2;
			double cy = Double.parseDouble(images.get(0)[2]) + Double.parseDouble(images.get(0)[4]) / 2;
			assertEquals(route(flags) + ": the shadow's centre", 72 + 3.18, cx, 0.5);
			assertEquals(route(flags) + ": the shadow's centre", 54 + 3.18, cy, 0.5);
			assertTrue(route(flags) + ": the picture, its own bytes", images.get(1)[0].startsWith("data:image/png;base64,"));
			assertEquals(route(flags), "144", images.get(1)[3]);
		}
	}

	/** Word cuts an inline picture's effects at its paragraph; where the picture is all its paragraph shows, its line:
	 *  a shadow 24pt down, effect extent 0, ends at the frame's foot (a blank run doesn't count) */
	@Test
	public void anInlinePicturesEffectsAreCutAtItsEffectExtent() throws Exception {
		for (int flags : BOTH) {
			for (String ee : new String[] { null, "0 0 0 152400" }) {
				Element svg = onlySvg(fo(pkg(picture("png"), SHADOW_DOWN, ee, false, null), flags), route(flags));
				String[] shadow = images(svg).get(0);
				double foot = Double.parseDouble(shadow[2]) + Double.parseDouble(shadow[4]);
				double cut = ee == null ? 108 : 120;
				assertEquals(route(flags) + " " + ee, cut, foot, 0.5);
			}
		}
	}

	/** A picture with words in its paragraph: Word's cut is the paragraph's, past its line (fx91), so docx4j doesn't
	 *  cut; the shadow reaches 24pt below it */
	@Test
	public void aPictureSharingItsParagraphIsNotCut() throws Exception {
		for (int flags : BOTH) {
			Element svg = onlySvg(fo(pkg(picture("png"), SHADOW_DOWN, null, false, null, true), flags), route(flags));
			String[] shadow = images(svg).get(0);
			assertEquals(route(flags), 132, Double.parseDouble(shadow[2]) + Double.parseDouble(shadow[4]), 0.5);
		}
	}

	/** An anchored picture's effects aren't cut: the shadow reaches 24pt below it */
	@Test
	public void anAnchoredPicturesEffectsAreNotCut() throws Exception {
		for (int flags : BOTH) {
			Element svg = onlySvg(fo(pkg(picture("png"), SHADOW_DOWN, null, true, null), flags), route(flags));
			String[] shadow = images(svg).get(0);
			assertEquals(route(flags), 132, Double.parseDouble(shadow[2]) + Double.parseDouble(shadow[4]), 0.5);
		}
	}

	/** A soft edge replaces the picture: one picture, the picture's own bytes not drawn */
	@Test
	public void aSoftEdgeReplacesThePicture() throws Exception {
		for (int flags : BOTH) {
			Element svg = onlySvg(fo(pkg(picture("jpg"), SOFT_EDGE, null, false, null), flags), route(flags));
			List<String[]> images = images(svg);
			assertEquals(route(flags), 1, images.size());
			assertTrue(route(flags), images.get(0)[0].startsWith("data:image/png;base64,"));
		}
	}

	/** A crop: the picture as the image handler cut it (the middle half of the bitmap), filling the frame */
	@Test
	public void aCropIsTheHandlersCut() throws Exception {
		for (int flags : BOTH) {
			Element svg = onlySvg(fo(pkg(picture("png"), SHADOW, null, false, "<a:srcRect l=\"25000\" r=\"25000\"/>"), flags), route(flags));
			String[] pic = images(svg).get(1);
			BufferedImage shown = ImageIO.read(new ByteArrayInputStream(java.util.Base64.getDecoder().decode(pic[0].substring(pic[0].indexOf(',') + 1))));
			assertEquals(route(flags), 80, shown.getWidth());
			assertEquals(route(flags), "144", pic[3]);
			assertEquals(route(flags), 0, svg.getElementsByTagNameNS(SVG_NS, "svg").getLength());
		}
	}

	/** No effects: an external-graphic, as before */
	@Test
	public void aPictureWithNoEffectsIsAnExternalGraphic() throws Exception {
		for (int flags : BOTH) {
			org.w3c.dom.Document fo = fo(pkg(picture("png"), null, null, false, null), flags);
			assertEquals(route(flags), 1, fo.getElementsByTagNameNS(FO_NS, "external-graphic").getLength());
			assertEquals(route(flags), 0, fo.getElementsByTagNameNS(SVG_NS, "svg").getLength());
		}
	}

	/** The svg elements directly in an SVG, as the drawer's fill behind and line are nested */
	static List<Element> nested(Element svg) {
		List<Element> out = new ArrayList<Element>();
		for (org.w3c.dom.Node n = svg.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element && "svg".equals(n.getLocalName())) out.add((Element) n);
		}
		return out;
	}

	/** An oval frame clips the picture (CR-081 phase 2): one SVG, the picture in a group clipped to the ellipse's path */
	@Test
	public void anOvalFrameClipsThePicture() throws Exception {
		for (int flags : BOTH) {
			org.w3c.dom.Document fo = fo(pkg(picture("png"), OVAL, null, false, null), flags);
			assertEquals(route(flags), 0, fo.getElementsByTagNameNS(FO_NS, "external-graphic").getLength());
			Element svg = onlySvg(fo, route(flags));
			NodeList clips = svg.getElementsByTagNameNS(SVG_NS, "clipPath");
			assertEquals(route(flags), 1, clips.getLength());
			String id = ((Element) clips.item(0)).getAttribute("id");
			Element path = (Element) ((Element) clips.item(0)).getElementsByTagNameNS(SVG_NS, "path").item(0);
			assertTrue(route(flags) + ": " + path.getAttribute("d"), path.getAttribute("d").matches("M[^C]*C.*Z"));
			Element image = (Element) svg.getElementsByTagNameNS(SVG_NS, "image").item(0);
			assertEquals(route(flags), "url(#" + id + ")", ((Element) image.getParentNode()).getAttribute("clip-path"));
			assertTrue(route(flags), nested(svg).isEmpty());
		}
	}

	/** A line is drawn wholly outside the picture, over it: a 6pt line's SVG from 6pt outside the frame, its stroke
	 *  on the frame grown by 3pt */
	@Test
	public void aLineIsOutsideThePicture() throws Exception {
		for (int flags : BOTH) {
			Element svg = onlySvg(fo(pkg(picture("png"), LINE, null, false, null), flags), route(flags));
			List<Element> nested = nested(svg);
			assertEquals(route(flags), 1, nested.size());
			Element line = nested.get(0);
			assertEquals(route(flags), "-6", line.getAttribute("x"));
			assertEquals(route(flags), "-6", line.getAttribute("y"));
			assertEquals(route(flags), "156", line.getAttribute("width"));
			assertEquals(route(flags), "120", line.getAttribute("height"));
			assertEquals(route(flags), "visible", line.getAttribute("overflow"));
			// after the picture
			assertTrue(route(flags), svg.getLastChild() == line);
			assertEquals(route(flags), 1, images(svg).size());
		}
	}

	/** A fill behind shows only through a transparent picture: behind an opaque one the picture is an external-graphic;
	 *  behind a transparent one, it is drawn under the picture */
	@Test
	public void aFillBehindIsDrawnUnderATransparentPicture() throws Exception {
		for (int flags : BOTH) {
			org.w3c.dom.Document fo = fo(pkg(picture("png"), FILL, null, false, null), flags);
			assertEquals(route(flags), 1, fo.getElementsByTagNameNS(FO_NS, "external-graphic").getLength());
			Element svg = onlySvg(fo(pkg(transparentPicture(), FILL, null, false, null), flags), route(flags));
			List<Element> nested = nested(svg);
			assertEquals(route(flags), 1, nested.size());
			// before the picture
			assertTrue(route(flags), svg.getFirstChild() == nested.get(0));
			assertEquals(route(flags), "0 0 144 108", nested.get(0).getAttribute("viewBox"));
		}
	}

	/** A frame, a line and a shadow together: the PDF is made, and the JPEG still passes through */
	@Test
	public void aFramedJpegPassesThroughToThePdf() throws Exception {
		WordprocessingMLPackage pkg = pkg(picture("jpg"), OVAL.replace("ellipse", "roundRect") + "<a:ln w=\"88900\"><a:solidFill><a:srgbClr val=\"FFFFFF\"/>"
				+ "</a:solidFill></a:ln>" + SHADOW, "0 0 76200 76200", false, null);
		Element svg = onlySvg(fo(pkg, Docx4J.FLAG_NONE), "visitor");
		assertEquals(2, images(svg).size());
		assertEquals(1, nested(svg).size());
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		ByteArrayOutputStream pdf = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, pdf, Docx4J.FLAG_NONE);
		String s = new String(pdf.toByteArray(), StandardCharsets.ISO_8859_1);
		assertTrue("the JPEG passed through", s.contains("/DCTDecode"));
		assertTrue("the shadow, a picture with a soft mask", s.contains("/SMask"));
	}

	/** The picture's own bytes in the SVG keep FOP's JPEG passthrough: the PDF holds the JPEG (DCTDecode) */
	@Test
	public void aJpegPassesThroughToThePdf() throws Exception {
		WordprocessingMLPackage pkg = pkg(picture("jpg"), SHADOW, "0 0 76200 76200", false, null);
		Element svg = onlySvg(fo(pkg, Docx4J.FLAG_NONE), "visitor");
		assertTrue(images(svg).get(1)[0].startsWith("data:image/jpeg;base64,"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		ByteArrayOutputStream pdf = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, pdf, Docx4J.FLAG_NONE);
		String s = new String(pdf.toByteArray(), StandardCharsets.ISO_8859_1);
		assertTrue("the JPEG passed through", s.contains("/DCTDecode"));
		assertTrue("the shadow, a picture with a soft mask", s.contains("/SMask"));
	}
}
