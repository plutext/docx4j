/*
 * Copyright 2026, Plutext Pty Ltd.
 *
 * This file is part of docx4j.
 *
 * docx4j is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * Word 2010's picture adjustments ({@code a14:imgProps}: saturation, colour tone, corrections, artistic effects,
 * background removal) are already applied to the picture the blip names; the original is kept beside it as
 * {@code a14:imgLayer} (CR-084 phase 0 findings section 2, D3: Word's PDF draws the stored blip). docx4j draws the blip,
 * with its {@code a:} adjustments (here {@code a:grayscl}, which Word applies over the adjusted picture), and never the
 * layer, in PDF and HTML alike (CR-084 phase 2; the adjustments in HTML since phase 4). Also a VML picture's
 * adjustments in both outputs (phases 3 and 4).
 */
public class PictureImgLayerTest {

	private static final int RED = 0xC00000, BLUE = 0x0000FF;

	private static byte[] png(int rgb) throws Exception {
		BufferedImage im = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < 32; y++) for (int x = 0; x < 32; x++) im.setRGB(x, y, rgb);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(im, "png", out);
		return out.toByteArray();
	}

	/** A picture whose blip is red (the adjusted picture) and whose a14:imgLayer is blue (the original) */
	private static WordprocessingMLPackage pkg(String blipChildren) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		BinaryPartAbstractImage adjusted = BinaryPartAbstractImage.createImagePart(pkg, png(RED));
		BinaryPartAbstractImage original = BinaryPartAbstractImage.createImagePart(pkg, png(BLUE));
		String blip = adjusted.getSourceRelationships().get(0).getId();
		String layer = original.getSourceRelationships().get(0).getId();
		String xml = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
				+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:a14=\"http://schemas.microsoft.com/office/drawing/2010/main\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<w:r><w:drawing><wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\">"
				+ "<wp:extent cx=\"914400\" cy=\"914400\"/><wp:docPr id=\"1\" name=\"Picture 1\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><pic:pic>"
				+ "<pic:nvPicPr><pic:cNvPr id=\"1\" name=\"p\"/><pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill><a:blip r:embed=\"" + blip + "\">" + blipChildren
				+ "<a:extLst><a:ext uri=\"{BEBA8EAE-BF5A-486C-A8C5-ECC9F3942E4B}\"><a14:imgProps><a14:imgLayer r:embed=\"" + layer
				+ "\"><a14:imgEffect><a14:saturation sat=\"0\"/></a14:imgEffect></a14:imgLayer></a14:imgProps></a:ext></a:extLst>"
				+ "</a:blip><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"914400\" cy=\"914400\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic>"
				+ "</wp:inline></w:drawing></w:r></w:p>";
		pkg.getMainDocumentPart().getContent().add((P) XmlUtils.unmarshalString(xml));
		return pkg;
	}

	/** The picture's colour in the PDF route's FO: the image file it names, read */
	private static int foColour(WordprocessingMLPackage pkg, int flags) throws Exception {
		File dir = Files.createTempDirectory("cr084p2").toFile();
		dir.deleteOnExit();
		FOSettings settings = new FOSettings(pkg);
		settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		settings.setImageDirPath(dir.getPath());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(settings, baos, flags);
		Matcher m = Pattern.compile("external-graphic[^>]*?src=\"([^\"]+)\"").matcher(baos.toString("UTF-8"));
		assertTrue("the fo draws the picture", m.find());
		String src = m.group(1).replaceAll("^url\\(['\"]?|['\"]?\\)$", "");
		assertFalse("one picture, not the layer as well", m.find());
		BufferedImage im = ImageIO.read(new File(new URI(src)));
		assertNotNull(im);
		return im.getRGB(16, 16) & 0xFFFFFF;
	}

	/** The picture's colour in HTML output: the data: URI it names, decoded */
	private static int htmlColour(WordprocessingMLPackage pkg) throws Exception {
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		settings.setImageDirPath("");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, baos, Docx4J.FLAG_NONE);
		Matcher m = Pattern.compile("src=\"data:image/[a-z]+;base64,([^\"]+)\"").matcher(baos.toString("UTF-8"));
		assertTrue("the html draws the picture", m.find());
		BufferedImage im = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(m.group(1))));
		assertFalse("one picture, not the layer as well", m.find());
		return im.getRGB(16, 16) & 0xFFFFFF;
	}

	@Test
	public void theBlipIsDrawnNeverTheLayer() throws Exception {
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			assertEquals("PDF route " + flags, RED, foColour(pkg(""), flags));
		}
		assertEquals("HTML", RED, htmlColour(pkg("")));
	}

	/** An a: adjustment beside the a14 extension applies to the adjusted blip (phase 0: Set Transparent Color over a
	 *  Soften), not to the layer */
	@Test
	public void theBlipsOwnAdjustmentsApplyToIt() throws Exception {
		int grey = 0x292929; // Rec. 709 luma of C00000: 0.2126 x 192 = 40.8
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			assertEquals("PDF route " + flags, grey, foColour(pkg("<a:grayscl/>"), flags));
		}
		// and in HTML output, since phase 4
		assertEquals("HTML", grey, htmlColour(pkg("<a:grayscl/>")));
	}

	/** A VML picture's v:imagedata grayscale="t" (phase 3), in PDF and HTML output (phase 4); and an unadjusted
	 *  picture reaches the HTML as it is stored, byte for byte */
	@Test
	public void aVmlPicturesAdjustmentsInBothOutputs() throws Exception {
		int grey = 0x292929;
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			assertEquals("PDF route " + flags, grey, foColour(vml("grayscale=\"t\""), flags));
		}
		assertEquals("HTML", grey, htmlColour(vml("grayscale=\"t\"")));
		assertEquals("HTML unadjusted", RED, htmlColour(vml("")));
		WordprocessingMLPackage plain = pkg("");
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(plain);
		settings.setImageDirPath("");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, baos, Docx4J.FLAG_NONE);
		assertTrue("the stored bytes, unadjusted", baos.toString("UTF-8").contains(Base64.getEncoder().encodeToString(png(RED))));
	}

	/** A VML picture (w:pict, v:shape, v:imagedata) of the red picture, with these v:imagedata attributes */
	private static WordprocessingMLPackage vml(String attributes) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		BinaryPartAbstractImage part = BinaryPartAbstractImage.createImagePart(pkg, png(RED));
		String rid = part.getSourceRelationships().get(0).getId();
		String xml = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
				+ " xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<w:r><w:pict><v:shape id=\"Picture 1\" style=\"width:72pt;height:72pt\" type=\"#_x0000_t75\">"
				+ "<v:imagedata r:id=\"" + rid + "\" o:title=\"\" " + attributes + "/></v:shape></w:pict></w:r></w:p>";
		pkg.getMainDocumentPart().getContent().add((P) XmlUtils.unmarshalString(xml));
		return pkg;
	}
}
