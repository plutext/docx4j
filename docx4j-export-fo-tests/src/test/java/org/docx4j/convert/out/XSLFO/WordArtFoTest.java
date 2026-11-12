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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * CR-083 phases 1 and 2: a text box drawn as WordArt in the FO and the PDF, by both routes: its box the one Word draws,
 * its drawing in it and no text container, its text in the drawing, invisible where Word draws the glyphs as outlines
 * (PDF text rendering mode 3: found by a search, not seen), and its alternative text for a tagged PDF.
 */
public class WordArtFoTest extends AbstractXSLFOTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\""
			+ " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\"";
	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";
	private static final String SVG_NS = "http://www.w3.org/2000/svg";
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";
	private static final int[] BOTH = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String bodyPr(String warp, boolean wordForm) {
		return "<wps:bodyPr wrap=\"" + (wordForm ? "none" : "square") + "\" lIns=\"91440\" tIns=\"45720\" rIns=\"91440\" bIns=\"45720\" anchor=\"t\">"
				+ (warp == null ? "" : "<a:prstTxWarp prst=\"" + warp + "\"><a:avLst/></a:prstTxWarp>") + (wordForm ? "<a:spAutoFit/>" : "<a:noAutofit/>")
				+ "</wps:bodyPr>";
	}

	/** A text box stored 288 x 144pt, anchored 72pt in and 36pt down, holding one 36pt paragraph */
	private static String anchored(String text, String bodyPr) {
		return "<w:p><w:r><w:drawing><wp:anchor distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\" simplePos=\"0\" relativeHeight=\"1\""
				+ " behindDoc=\"0\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\"><wp:simplePos x=\"0\" y=\"0\"/>"
				+ "<wp:positionH relativeFrom=\"column\"><wp:posOffset>914400</wp:posOffset></wp:positionH>"
				+ "<wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>457200</wp:posOffset></wp:positionV>"
				+ "<wp:extent cx=\"3657600\" cy=\"1828800\"/><wp:wrapNone/><wp:docPr id=\"1\" name=\"Text Box 1\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\"><wps:wsp>"
				+ "<wps:cNvSpPr txBox=\"1\"/><wps:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"3657600\" cy=\"1828800\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom><a:noFill/><a:ln><a:noFill/></a:ln></wps:spPr>"
				+ "<wps:txbx><w:txbxContent><w:p><w:pPr><w:spacing w:before=\"0\" w:after=\"0\"/><w:jc w:val=\"center\"/></w:pPr>"
				+ "<w:r><w:rPr><w:sz w:val=\"72\"/></w:rPr><w:t>" + text + "</w:t></w:r></w:p></w:txbxContent></wps:txbx>" + bodyPr
				+ "</wps:wsp></a:graphicData></a:graphic></wp:anchor></w:drawing></w:r></w:p>";
	}

	private static WordprocessingMLPackage pkg(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString("<w:document " + NS + "><w:body>" + body
				+ "<w:p><w:r><w:t>body text</w:t></w:r></w:p>" + SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(String body, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg(body));
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private static byte[] pdf(String body, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg(body));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return baos.toByteArray();
	}

	private static double pt(String v) {
		return Double.parseDouble(v.replace("pt", ""));
	}

	/** The innermost block-container whose first child is the absolutely placed drawing (a wrapNone box's wrapper is one too) */
	private static Element box(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS(FO_NS, "block-container");
		Element found = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			for (org.w3c.dom.Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
				if (!(n instanceof Element)) continue;
				Element first = (Element) n;
				if ("absolute".equals(first.getAttribute("absolute-position")) && first.getElementsByTagNameNS(SVG_NS, "svg").getLength() > 0) found = el;
				break;
			}
		}
		return found;
	}

	/** Word's own WordArt: its box its text's (wrap none, spAutoFit), not the 288 x 144pt stored; no text container */
	@Test
	public void wordArtIsItsDrawingInTheBoxWordDraws() throws Exception {
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = fo(anchored("WordArt", bodyPr("textArchUp", true)), flags);
			Element box = box(doc);
			assertNotNull("flags " + flags, box);
			double w = pt(box.getAttribute("width")), h = pt(box.getAttribute("height"));
			assertTrue("flags " + flags + ": " + w, w > 100 && w < 200);
			// one line of 36pt (at this package's line spacing) with the insets, not the 144pt stored
			assertTrue("flags " + flags + ": " + h, h > 40 && h < 70);
			// the text is in the drawing alone: the container holds the drawing and an empty block
			Element svg = (Element) box.getElementsByTagNameNS(SVG_NS, "svg").item(0);
			StringBuilder inSvg = new StringBuilder();
			NodeList texts = svg.getElementsByTagNameNS(SVG_NS, "text");
			for (int i = 0; i < texts.getLength(); i++) {
				Element t = (Element) texts.item(i);
				assertEquals("flags " + flags + ": warped, so invisible", "none", t.getAttribute("fill"));
				inSvg.append(t.getTextContent());
			}
			assertEquals("flags " + flags, "WordArt", inSvg.toString());
			String outside = box.getTextContent().replace(svg.getTextContent(), "");
			assertTrue("flags " + flags + ": " + outside, outside.trim().isEmpty());
			// its alternative text, for a tagged PDF
			Element ifo = (Element) svg.getParentNode();
			assertEquals("flags " + flags, "WordArt", ifo.getAttributeNS("http://xmlgraphics.apache.org/fop/extensions", "alt-text"));
			assertTrue("flags " + flags + ": the mark is removed", !box.hasAttribute("docx4j-wordart"));
		}
	}

	/** An ordinary text box is as it was: its text in its container, no WordArt drawing */
	@Test
	public void anOrdinaryTextBoxIsText() throws Exception {
		for (int flags : BOTH) {
			org.w3c.dom.Document doc = fo(anchored("Plain words", bodyPr(null, false)), flags);
			assertEquals("flags " + flags, 0, doc.getElementsByTagNameNS(SVG_NS, "text").getLength());
			assertTrue("flags " + flags, doc.getDocumentElement().getTextContent().contains("Plain words"));
		}
	}

	/** In the PDF: the warped glyphs as paths, the text invisible (mode 3) and found by a text extraction */
	@Test
	public void inThePdfItsTextIsInvisibleAndFound() throws Exception {
		for (int flags : BOTH) {
			byte[] bytes = pdf(anchored("WordArt", bodyPr("textInflate", false)), flags);
			try (PDDocument d = Loader.loadPDF(bytes)) {
				String text = new PDFTextStripper().getText(d);
				assertTrue("flags " + flags + ": " + text, text.contains("WordArt"));
				assertTrue("flags " + flags + ": text rendering mode 3", usesMode3(d.getPage(0)));
			}
		}
	}

	/** Whether a page, or a form it draws, sets text rendering mode 3 */
	private static boolean usesMode3(PDPage page) throws Exception {
		if (mode3(new PDFStreamParser(page).parse())) return true;
		if (page.getResources() != null) {
			for (org.apache.pdfbox.cos.COSName n : page.getResources().getXObjectNames()) {
				PDXObject x = page.getResources().getXObject(n);
				if (x instanceof PDFormXObject && mode3(new PDFStreamParser((PDFormXObject) x).parse())) return true;
			}
		}
		return false;
	}

	private static boolean mode3(java.util.List<Object> tokens) {
		for (int i = 1; i < tokens.size(); i++) {
			Object t = tokens.get(i);
			if (t instanceof Operator && "Tr".equals(((Operator) t).getName())) {
				Object v = tokens.get(i - 1);
				if (v instanceof org.apache.pdfbox.cos.COSNumber && ((org.apache.pdfbox.cos.COSNumber) v).intValue() == 3) return true;
			}
		}
		return false;
	}
}
