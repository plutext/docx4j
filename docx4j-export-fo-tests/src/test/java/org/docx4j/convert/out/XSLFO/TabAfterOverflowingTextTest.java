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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * Text before a tab which does not itself fit the cell breaks at the last opportunity that
 * did, as Word breaks it (word-layout-rules.md &#xa7;4.4), rather than running past the cell.
 *
 * <p>The tab ends the text and can reach no stop (the default grid's next stop is past the
 * cell).  Until 17.3.1 the line manager gave up the break when what preceded such a tab did
 * not fit, and drew the line past the cell's edge: corpus document 12301,
 * {@code Configuración de Avance del Proyecto<tab>} in a 162.7pt cell, drawn to 7.5pt past it
 * where Word breaks before "Proyecto" (ledger8 item 5).</p>
 *
 * @since 17.3.1
 */
public class TabAfterOverflowingTextTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** a 110pt cell (2200 twips, 99.2pt between the default 5.4pt margins); 11pt Arial:
	 *  "Configuración" is about 68pt, "Configuración Proyecto" about 114 */
	private static byte[] pdf() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:tbl><w:tblPr><w:tblW w:w=\"2200\" w:type=\"dxa\"/><w:tblLayout w:type=\"fixed\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"2200\"/></w:tblGrid>"
				+ "<w:tr><w:tc><w:tcPr><w:tcW w:w=\"2200\" w:type=\"dxa\"/></w:tcPr>"
				+ "<w:p><w:pPr><w:spacing w:after=\"0\"/></w:pPr>"
				+ "<w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\"/><w:sz w:val=\"22\"/></w:rPr>"
				+ "<w:t>Configuración Proyecto</w:t><w:tab/></w:r></w:p>"
				+ "</w:tc></w:tr></w:tbl><w:p/>"
				+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toPDF(pkg, baos);
		return baos.toByteArray();
	}

	private static List<TextPosition> glyphs(byte[] pdf) throws Exception {
		final List<TextPosition> out = new ArrayList<TextPosition>();
		try (PDDocument doc = Loader.loadPDF(pdf)) {
			PDFTextStripper stripper = new PDFTextStripper() {
				@Override
				protected void writeString(String text, List<TextPosition> positions) throws IOException {
					out.addAll(positions);
				}
			};
			stripper.getText(doc);
		}
		return out;
	}

	/** the glyph which begins this word */
	private static TextPosition first(List<TextPosition> glyphs, String word) {
		StringBuilder b = new StringBuilder();
		for (TextPosition tp : glyphs) b.append(tp.getUnicode());
		int i = b.indexOf(word);
		return i < 0 ? null : glyphs.get(i);
	}

	@Test
	public void theWordThatDoesNotFitGoesToTheNextLine() throws Exception {
		List<TextPosition> glyphs = glyphs(pdf());
		TextPosition c = first(glyphs, "Configuración"), p = first(glyphs, "Proyecto");
		assertNotNull("Configuración", c);
		assertNotNull("Proyecto", p);
		assertTrue("\"Proyecto\" is on the line below (y " + c.getYDirAdj() + " / " + p.getYDirAdj() + ")",
				p.getYDirAdj() > c.getYDirAdj() + 5);
		// and nothing is drawn past the cell: 72 + 110pt
		for (TextPosition tp : glyphs) {
			assertTrue("'" + tp.getUnicode() + "' ends at " + (tp.getXDirAdj() + tp.getWidthDirAdj()),
					tp.getXDirAdj() + tp.getWidthDirAdj() <= 182.5);
		}
	}
}
