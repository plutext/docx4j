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
import static org.junit.Assert.assertNotNull;
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
import org.docx4j.convert.out.FOSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.wml.CTEndnotes;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The "Endnotes" heading docx4j writes above a section's endnotes is set in the
 * document's default font (CR-001, non-embedded fonts).
 *
 * <p>Word writes no such heading, so it is docx4j's own text, and it carried
 * {@code font-weight} and {@code font-size} and no {@code font-family} at all - and
 * neither does {@code fo:root}. FOP drew it in the initial value of the property, one of
 * its base-14 fonts, which it does not embed: the PDF then named a font it did not carry,
 * failing PDF/A and leaving a reader without Helvetica to substitute, for the sake of one
 * word. Measured over a 449-document corpus, 13 documents were in that state, and in 12
 * of them this heading was the only thing wrong.</p>
 *
 * @since 17.2.0
 */
public class EndnotesHeadingFontTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>Body text.</w:t></w:r>"
				+ "<w:r><w:endnoteReference w:id=\"2\"/></w:r></w:p>"
				+ "</w:body></w:document>"));

		EndnotesPart ep = new EndnotesPart();
		ep.setJaxbElement((CTEndnotes) XmlUtils.unmarshalString(
				"<w:endnotes " + W + ">"
				+ "<w:endnote w:type=\"separator\" w:id=\"0\"><w:p><w:r><w:separator/></w:r></w:p></w:endnote>"
				+ "<w:endnote w:type=\"continuationSeparator\" w:id=\"1\">"
				+   "<w:p><w:r><w:continuationSeparator/></w:r></w:p></w:endnote>"
				+ "<w:endnote w:id=\"2\"><w:p>"
				+   "<w:r><w:endnoteRef/></w:r>"
				+   "<w:r><w:t xml:space=\"preserve\"> The endnote.</w:t></w:r>"
				+ "</w:p></w:endnote></w:endnotes>", Context.jc, CTEndnotes.class));
		pkg.getMainDocumentPart().addTargetPart(ep);
		return pkg;
	}

	private org.w3c.dom.Document fo(int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg());
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	/** the fo:block whose text is the heading docx4j generates */
	private static Element heading(org.w3c.dom.Document doc) {
		NodeList blocks = doc.getElementsByTagNameNS(FO, "block");
		for (int i = 0; i < blocks.getLength(); i++) {
			Element b = (Element) blocks.item(i);
			if ("Endnotes".equals(b.getTextContent())) {
				return b;
			}
		}
		return null;
	}

	private void check(int flags) throws Exception {
		Element heading = heading(fo(flags));
		assertNotNull("no Endnotes heading in the FO", heading);
		String family = heading.getAttribute("font-family");
		assertFalse("the Endnotes heading has no font-family, so FOP draws it in a base-14"
				+ " font it does not embed: " + XmlUtils.w3CDomNodeToString(heading),
				family == null || family.length() == 0);
		// it is still the heading it was: bold, 14pt, its space above
		assertTrue("font-weight lost", "bold".equals(heading.getAttribute("font-weight")));
		assertTrue("font-size lost", "14pt".equals(heading.getAttribute("font-size")));
		assertTrue("space-before lost", "44pt".equals(heading.getAttribute("space-before")));
	}

	@Test
	public void theEndnotesHeadingHasAFontVisitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void theEndnotesHeadingHasAFontXslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** And so the PDF embeds every font it names. */
	@Test
	public void everyFontInThePdfIsEmbedded() throws Exception {
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
						// no descriptor at all is one of the base 14: FOP fell back
						notEmbedded.add(baseFont);
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
	}
}
