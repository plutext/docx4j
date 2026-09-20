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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * The text inside a metafile is drawn in a font the PDF embeds
 * (CR-001, non-embedded fonts; CR-011's pathway).
 *
 * <p>A metafile's text is replayed onto Batik's {@code SVGGraphics2D}, and what Batik
 * writes as the SVG's {@code font-family} is the AWT font's <em>family</em>.  A metafile
 * names its fonts by GDI face name - "Calibri" here - which is a document font name, so
 * AWT does not know it and the family degraded to {@code Dialog}.  FOP has never been told
 * about a font called Dialog, so it reported "Font Dialog,normal,400 not found.
 * Substituting with any" and drew the picture's text in one of its base-14 fonts: named in
 * the PDF, and not embedded in it.  18 of a 449-document corpus were in that state.</p>
 *
 * <p>Both pathways, since the two serialize the SVG differently.</p>
 *
 * @since 17.2.0
 */
public class MetafileFontEmbeddedTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** an EMF whose text records name Calibri */
	private static final String METAFILE = "pptx.emf";

	private static WordprocessingMLPackage pkg() throws Exception {
		byte[] emf;
		try (InputStream is = MetafileFontEmbeddedTest.class
				.getResourceAsStream("/metafiles/" + METAFILE)) {
			emf = org.apache.commons.io.IOUtils.toByteArray(is);
		}
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String relId = BinaryPartAbstractImage.createImagePart(pkg, emf)
				.getSourceRelationship().getId();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r>"
				+ "<w:drawing xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\">"
				+ "<wp:inline><wp:extent cx=\"3600000\" cy=\"2700000\"/>"
				+ "<wp:docPr id=\"1\" name=\"picture\"/>"
				+ "<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
				+ "<a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
				+ "<pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
				+ "<pic:nvPicPr><pic:cNvPr id=\"1\" name=\"picture\"/><pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill><a:blip xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
				+ " r:embed=\"" + relId + "\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"3600000\" cy=\"2700000\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr>"
				+ "</pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing>"
				+ "</w:r></w:p>"
				+ "<w:p><w:r><w:t>text below the picture</w:t></w:r></w:p>"
				+ "</w:body></w:document>"));
		return pkg;
	}

	/** the SVG the FO carries names the face the mapper chose for the metafile's font */
	private void checkFo(int flags) throws Exception {
		WordprocessingMLPackage pkg = pkg();
		org.docx4j.fonts.PhysicalFont substitute = pkg.getFontMapper().get("Calibri");
		org.junit.Assume.assumeTrue("no substitute for Calibri on this machine",
				substitute != null && substitute.getFamilyName() != null);

		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		String fo = new String(baos.toByteArray(), "UTF-8");

		assertTrue("no SVG in the FO, so the metafile was not drawn as vectors",
				fo.indexOf("instream-foreign-object") > 0);
		/* Batik writes the AWT font's family.  ('Dialog' is still on the <svg> root - it
		 * is SVGGraphics2D's initial graphics state, which Batik always emits, whether or
		 * not anything is drawn in it; FopConfigUtil declares it for that reason.) */
		assertTrue("the SVG does not name " + substitute.getFamilyName()
				+ ", so the metafile's face did not go through the font mapper",
				fo.indexOf("'" + substitute.getFamilyName() + "'") > 0);
	}

	@Test
	public void theSvgNamesAResolvableFamilyVisitor() throws Exception {
		checkFo(Docx4J.FLAG_NONE);
	}

	@Test
	public void theSvgNamesAResolvableFamilyXslt() throws Exception {
		checkFo(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void everyFontInThePdfIsEmbeddedAndNoneIsBase14() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toPDF(pkg(), baos);

		List<String> notEmbedded = new ArrayList<String>();
		TreeSet<String> all = new TreeSet<String>();

		try (PDDocument doc = Loader.loadPDF(baos.toByteArray())) {
			for (PDPage page : doc.getPages()) {
				PDResources resources = page.getResources();
				if (resources == null) {
					continue;
				}
				COSBase fonts = resources.getCOSObject().getDictionaryObject(COSName.FONT);
				if (!(fonts instanceof COSDictionary)) {
					continue;
				}
				for (COSName name : ((COSDictionary) fonts).keySet()) {
					COSBase base = ((COSDictionary) fonts).getDictionaryObject(name);
					if (!(base instanceof COSDictionary)) {
						continue;
					}
					COSDictionary font = (COSDictionary) base;
					String baseFont = font.getNameAsString(COSName.BASE_FONT);
					all.add(baseFont);
					COSBase descendants = font.getDictionaryObject(COSName.DESCENDANT_FONTS);
					if (descendants instanceof COSArray && ((COSArray) descendants).size() > 0) {
						COSBase d = ((COSArray) descendants).getObject(0);
						if (d instanceof COSDictionary) {
							font = (COSDictionary) d;
						}
					}
					COSBase descriptor = font.getDictionaryObject(COSName.FONT_DESC);
					if (!(descriptor instanceof COSDictionary)) {
						notEmbedded.add(baseFont); // no descriptor at all is one of the base 14
						continue;
					}
					COSDictionary fd = (COSDictionary) descriptor;
					if (fd.getDictionaryObject(COSName.FONT_FILE) == null
							&& fd.getDictionaryObject(COSName.FONT_FILE2) == null
							&& fd.getDictionaryObject(COSName.FONT_FILE3) == null) {
						notEmbedded.add(baseFont);
					}
				}
			}
		}

		assertFalse("no fonts in the PDF", all.isEmpty());
		assertTrue("the PDF names fonts it does not embed: " + notEmbedded + " (of " + all + ")",
				notEmbedded.isEmpty());
		for (String baseFont : all) {
			String name = baseFont == null ? "" : baseFont;
			int plus = name.indexOf('+');
			if (plus >= 0) {
				name = name.substring(plus + 1);
			}
			assertFalse("a base-14 font in the PDF: " + baseFont + " (of " + all + ")",
					name.startsWith("Times-") || name.startsWith("Helvetica")
					|| name.startsWith("Courier") || "Symbol".equals(name)
					|| "ZapfDingbats".equals(name));
		}
	}
}
