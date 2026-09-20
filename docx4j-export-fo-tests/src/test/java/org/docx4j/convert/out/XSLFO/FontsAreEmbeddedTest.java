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
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * Every font the PDF names must be embedded in it.
 *
 * <p>A font FOP declares but does not embed is a shipped defect: the PDF fails PDF/A, and
 * a reader or printer without that font substitutes another, so the document does not look
 * as it was written. It happened silently - the run was drawn, in whatever the reader had -
 * whenever FOP's subsetter could not read a font, which for the fonts docx4j ships is any
 * run containing a non-breaking space: their last glyph is U+00A0, it is empty, and the
 * glyf table ends the file, so FOP read two bytes past the end and gave up on the font
 * (see {@code org.docx4j.fop.fonts.FontPaddingResourceResolver}).</p>
 *
 * <p>The document below is a non-breaking space in each of the five faces that were
 * affected: Calibri and Calibri italic (Carlito Regular, Carlito Italic), and Arial bold,
 * italic and bold italic (Arimo Bold, Italic, BoldItalic). Each run asks for kerning, which
 * is what sends it to FOP as a CID font - the font docx4j declares for a run that kerns -
 * and so through the subsetter.</p>
 *
 * @since 17.2.0
 */
public class FontsAreEmbeddedTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** a non-breaking space between two letters, as a numeric character reference */
	private static final String NBSP_TEXT = "a&#160;b";

	private static String para(String font, String styleTags) {
		return "<w:p><w:r><w:rPr>"
				+ "<w:rFonts w:ascii=\"" + font + "\" w:hAnsi=\"" + font + "\"/>"
				+ styleTags
				+ "<w:kern w:val=\"16\"/>"
				+ "</w:rPr><w:t>" + NBSP_TEXT + "</w:t></w:r></w:p>";
	}

	private static byte[] pdf() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ para("Calibri", "")
				+ para("Calibri", "<w:i/>")
				+ para("Arial", "<w:b/>")
				+ para("Arial", "<w:i/>")
				+ para("Arial", "<w:b/><w:i/>")
				+ "</w:body></w:document>"));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toPDF(pkg, baos);
		return baos.toByteArray();
	}

	@Test
	public void everyFontInThePdfIsEmbedded() throws Exception {

		List<String> notEmbedded = new ArrayList<String>();
		TreeSet<String> all = new TreeSet<String>();
		TreeSet<String> cid = new TreeSet<String>();

		try (PDDocument doc = Loader.loadPDF(pdf())) {
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

					// a Type0 font carries the descriptor on its descendant CIDFont
					COSBase descendants = font.getDictionaryObject(COSName.DESCENDANT_FONTS);
					if (descendants instanceof COSArray && ((COSArray) descendants).size() > 0) {
						cid.add(baseFont);
						COSBase d = ((COSArray) descendants).getObject(0);
						if (d instanceof COSDictionary) {
							font = (COSDictionary) d;
						}
					}
					COSBase descriptor = font.getDictionaryObject(COSName.FONT_DESC);
					if (!(descriptor instanceof COSDictionary)) {
						continue; // one of the base 14, which needs no descriptor
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
		assertTrue("nothing went to FOP as a CID font, so nothing was subset; fonts were " + all,
				!cid.isEmpty());
		assertTrue("the PDF names fonts it does not embed: " + notEmbedded + " (of " + all + ")",
				notEmbedded.isEmpty());
	}
}
