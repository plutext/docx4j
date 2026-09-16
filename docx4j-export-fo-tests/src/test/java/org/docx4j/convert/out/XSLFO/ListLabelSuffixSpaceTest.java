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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.fop.wordlayout.WordLayoutCustomizer;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.Document;
import org.docx4j.wml.Numbering;
import org.junit.After;
import org.junit.Test;

/**
 * The {@code w:suff} separator between a list item's number and its text is a character
 * in the PDF's text layer, as it is in Word's.
 *
 * <p>Word paints a numbered paragraph as number, separator, text on one line, and writes
 * the separator - a tab, or a space - as one space glyph whose quad is its advance:
 * measured on one corpus report's first heading, Word's label runs 127.49..134.45 and a
 * space quad 134.45..138.53 precedes the text at 138.53.  docx4j lays the number out as an
 * {@code fo:list-item-label} beside an {@code fo:list-item-body}, so the separator was
 * geometry and no character at all, and the label fused with the word after it: the
 * {@code styles-numpr-ilvl-only} golden probe extracted as
 * {@code 1.1.(b) L, direct w:numPr of w:ilvl 1 only} where Word's golden reads
 * {@code 1.1. (b) L, ...} (50% to 100% line parity), and 847 lines over the three
 * real-document corpora differ from Word's by that one space (CR-001 batch 45).</p>
 *
 * <p>The space is added to the label's line area at layout time with the gap's own width,
 * so it is the text layer alone that changes.  The test renders the same document with
 * {@link WordLayoutCustomizer#LABEL_SUFFIX_SPACE} off and on and asserts both halves:
 * the text gains the space, and <b>no glyph moves</b>.</p>
 *
 * @since 17.1.1
 */
public class ListLabelSuffixSpaceTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	/** numId 1: "%1." with a tab separator; numId 2: the same with w:suff "nothing". */
	private static String numbering(String suffix) {
		return "<w:numbering " + W + ">"
				+ "<w:abstractNum w:abstractNumId=\"0\"><w:lvl w:ilvl=\"0\">"
				+ "<w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>"
				+ (suffix == null ? "" : "<w:suff w:val=\"" + suffix + "\"/>")
				+ "<w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>"
				+ "<w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr></w:lvl></w:abstractNum>"
				+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num>"
				+ "</w:numbering>";
	}

	private static final String TEXT = "the body of the item";

	@After
	public void restore() {
		Docx4jProperties.getProperties().remove(WordLayoutCustomizer.LABEL_SUFFIX_SPACE);
	}

	private static byte[] pdf(String suffix) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		ndp.setJaxbElement((Numbering) XmlUtils.unmarshalString(numbering(suffix)));
		pkg.getMainDocumentPart().addTargetPart(ndp);
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr></w:pPr>"
				+ "<w:r><w:t>" + TEXT + "</w:t></w:r></w:p>"
				+ SECT_PR + "</w:body></w:document>"));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toPDF(pkg, baos);
		return baos.toByteArray();
	}

	/** every glyph of the PDF, in the order the text layer reports it */
	private static List<TextPosition> glyphs(byte[] pdf) throws Exception {
		final List<TextPosition> out = new ArrayList<TextPosition>();
		try (PDDocument doc = Loader.loadPDF(pdf)) {
			PDFTextStripper stripper = new PDFTextStripper() {
				@Override
				protected void writeString(String text, List<TextPosition> positions) throws IOException {
					out.addAll(positions);
				}
			};
			stripper.setSortByPosition(true);
			stripper.getText(doc);
		}
		return out;
	}

	private static String text(List<TextPosition> glyphs) {
		StringBuilder b = new StringBuilder();
		for (TextPosition tp : glyphs) b.append(tp.getUnicode());
		return b.toString();
	}

	/** the glyphs which put ink on the page, which are the ones that may not move */
	private static List<TextPosition> ink(List<TextPosition> glyphs) {
		List<TextPosition> out = new ArrayList<TextPosition>();
		for (TextPosition tp : glyphs) {
			if (tp.getUnicode() != null && tp.getUnicode().trim().length() > 0) out.add(tp);
		}
		return out;
	}

	@Test
	public void tabSeparatorIsASpaceInTheTextLayer() throws Exception {

		Docx4jProperties.setProperty(WordLayoutCustomizer.LABEL_SUFFIX_SPACE, false);
		List<TextPosition> without = glyphs(pdf("tab"));

		Docx4jProperties.setProperty(WordLayoutCustomizer.LABEL_SUFFIX_SPACE, true);
		List<TextPosition> with = glyphs(pdf("tab"));

		assertEquals("the label and its text, with no separator", "1." + TEXT, text(without));
		assertEquals("the separator Word writes", "1. " + TEXT, text(with));

		// and nothing moved: same glyphs, same x, same y
		List<TextPosition> a = ink(without), b = ink(with);
		assertEquals("the same ink glyphs", a.size(), b.size());
		assertTrue("there are glyphs to compare", a.size() > 0);
		for (int i = 0; i < a.size(); i++) {
			assertEquals("glyph " + i, a.get(i).getUnicode(), b.get(i).getUnicode());
			assertEquals("glyph " + i + " (" + a.get(i).getUnicode() + ") x",
					a.get(i).getXDirAdj(), b.get(i).getXDirAdj(), 0.001);
			assertEquals("glyph " + i + " (" + a.get(i).getUnicode() + ") y",
					a.get(i).getYDirAdj(), b.get(i).getYDirAdj(), 0.001);
		}

		// the space stands in the gap the separator made, and ends where the text begins
		TextPosition space = null, first = null;
		for (int i = 0; i < with.size(); i++) {
			if (" ".equals(with.get(i).getUnicode())) {
				space = with.get(i);
				first = with.get(i + 1);
				break;
			}
		}
		assertTrue("a space was written", space != null && first != null);
		assertTrue("the space begins where the number ends",
				space.getXDirAdj() <= first.getXDirAdj());
	}

	/** {@code w:suff w:val="nothing"}: Word writes no character, and neither do we. */
	@Test
	public void nothingSeparatorWritesNoSpace() throws Exception {
		Docx4jProperties.setProperty(WordLayoutCustomizer.LABEL_SUFFIX_SPACE, true);
		assertEquals("1." + TEXT, text(glyphs(pdf("nothing"))));
	}
}
