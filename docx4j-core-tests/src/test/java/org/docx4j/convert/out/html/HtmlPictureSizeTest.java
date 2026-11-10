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
package org.docx4j.convert.out.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.Test;

/**
 * A picture in HTML at its size against its text (CR-080 D14, 17.3.7): its frame's points as CSS pixels, 96 an inch,
 * 4/3 a point, by the visitor and the XSLT alike. Until 17.3.7 the points were written as pixels, three quarters of
 * its size, and a VML picture's inches as pixels.
 */
public class HtmlPictureSizeTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:v=\"urn:schemas-microsoft-com:vml\""
			+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"";
	private static final int[] BOTH = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static byte[] png() throws Exception {
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(80, 60, java.awt.image.BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		javax.imageio.ImageIO.write(img, "png", out);
		return out.toByteArray();
	}

	private static Element img(WordprocessingMLPackage pkg, int flags) throws Exception {
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		settings.setImageDirPath(System.getProperty("java.io.tmpdir") + "/docx4j-html-picture-size");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, baos, flags);
		Element img = Jsoup.parse(baos.toString("UTF-8")).selectFirst("img");
		assertTrue(img != null);
		return img;
	}

	private static String route(int flags) {
		return flags == Docx4J.FLAG_NONE ? "visitor" : "XSLT";
	}

	/** A DrawingML picture 144 x 108pt (2 x 1.5in): 192 x 144 CSS pixels */
	@Test
	public void aDrawingMLPictureIsAtItsSize() throws Exception {
		for (int flags : BOTH) {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, png());
			org.docx4j.dml.wordprocessingDrawing.Inline inline = part.createImageInline("p", "picture", 1, 2, 2880, false); // cx in twips: 144pt
			org.docx4j.wml.R r = new org.docx4j.wml.R();
			org.docx4j.wml.Drawing drawing = new org.docx4j.wml.Drawing();
			drawing.getAnchorOrInline().add(inline);
			r.getContent().add(drawing);
			org.docx4j.wml.P p = new org.docx4j.wml.P();
			p.getContent().add(r);
			pkg.getMainDocumentPart().getContent().add(p);
			Element img = img(pkg, flags);
			assertEquals(route(flags), "192", img.attr("width"));
			assertEquals(route(flags), "144", img.attr("height"));
		}
	}

	/** A VML picture 1 x 0.75in: 96 x 72 CSS pixels, where its inches had been written as pixels */
	@Test
	public void aVmlPictureInInchesIsAtItsSize() throws Exception {
		for (int flags : BOTH) {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, png());
			String relId = part.getSourceRelationships().get(0).getId();
			pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) XmlUtils.unmarshalString("<w:document " + NS + "><w:body><w:p><w:r>"
					+ "<w:pict><v:shape id=\"pic1\" type=\"#_x0000_t75\" style=\"width:1in;height:0.75in\"><v:imagedata r:id=\"" + relId + "\"/>"
					+ "</v:shape></w:pict></w:r></w:p></w:body></w:document>"));
			Element img = img(pkg, flags);
			assertEquals(route(flags), "96", img.attr("width"));
			assertEquals(route(flags), "72", img.attr("height"));
		}
	}
}
