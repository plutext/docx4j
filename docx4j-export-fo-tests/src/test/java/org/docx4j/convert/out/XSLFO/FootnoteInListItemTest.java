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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.CTFootnotes;
import org.docx4j.wml.Document;
import org.docx4j.wml.Numbering;
import org.junit.Test;

/**
 * A footnote cited from a list item is in the PDF, whatever the item's line spacing.
 *
 * <p>A list item at more than single spacing ends with leading that Word drops at the foot
 * of a page, and {@code WordListItemLayoutManager} takes it out of the item's last box to
 * leave it discardable.  That box is a {@code KnuthBlockBox}, which carries the footnotes
 * cited on its lines to the page breaker, and the plain {@code KnuthBox} put in its place
 * lost them: on corpus document 2451 (list items at 1.5 lines) the five footnotes cited from
 * list items were missing, bodies and all.  Single-spaced items have no leading to expose,
 * so their footnotes were always there.</p>
 *
 * @since 17.3.1
 */
public class FootnoteInListItemTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final String NUMBERING = "<w:numbering " + W + ">"
			+ "<w:abstractNum w:abstractNumId=\"0\"><w:lvl w:ilvl=\"0\">"
			+ "<w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>"
			+ "<w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>"
			+ "<w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr></w:lvl></w:abstractNum>"
			+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num>"
			+ "</w:numbering>";

	private static String para(boolean listItem, int line, String text, int footnoteId) {
		return "<w:p><w:pPr>"
				+ (listItem ? "<w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr>" : "")
				+ "<w:spacing w:before=\"0\" w:after=\"0\" w:line=\"" + line + "\" w:lineRule=\"auto\"/></w:pPr>"
				+ "<w:r><w:t>" + text + "</w:t></w:r>"
				+ "<w:r><w:rPr><w:vertAlign w:val=\"superscript\"/></w:rPr><w:footnoteReference w:id=\""
				+ footnoteId + "\"/></w:r></w:p>";
	}

	private static String footnote(int id, String text) {
		return "<w:footnote w:id=\"" + id + "\"><w:p>"
				+ "<w:r><w:rPr><w:vertAlign w:val=\"superscript\"/></w:rPr><w:footnoteRef/></w:r>"
				+ "<w:r><w:t xml:space=\"preserve\"> " + text + "</w:t></w:r>"
				+ "</w:p></w:footnote>";
	}

	/** a plain paragraph and two list items, each citing a footnote, at the given line spacing */
	private static String pdfText(int line) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		ndp.setJaxbElement((Numbering) XmlUtils.unmarshalString(NUMBERING));
		pkg.getMainDocumentPart().addTargetPart(ndp);
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ para(false, line, "A plain paragraph", 1)
				+ para(true, line, "A list item", 2)
				+ para(true, line, "A second list item", 3)
				+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		FootnotesPart fp = new FootnotesPart();
		fp.setJaxbElement((CTFootnotes) XmlUtils.unmarshalString(
				"<w:footnotes " + W + ">"
				+ "<w:footnote w:type=\"separator\" w:id=\"-1\"><w:p><w:r><w:separator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:type=\"continuationSeparator\" w:id=\"0\"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:footnote>"
				+ footnote(1, "NOTE ONE")
				+ footnote(2, "NOTE TWO")
				+ footnote(3, "NOTE THREE")
				+ "</w:footnotes>", Context.jc, CTFootnotes.class));
		pkg.getMainDocumentPart().addTargetPart(fp);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toPDF(pkg, baos);
		try (PDDocument doc = Loader.loadPDF(baos.toByteArray())) {
			return new PDFTextStripper().getText(doc);
		}
	}

	private static void assertAllNotes(String text) {
		for (String note : new String[] { "NOTE ONE", "NOTE TWO", "NOTE THREE" }) {
			assertTrue(note + " is in the PDF: " + text, text.contains(note));
		}
	}

	@Test
	public void singleSpacedListItems() throws Exception {
		assertAllNotes(pdfText(240));
	}

	/** 1.5 lines: the items end with leading to expose (2451, 7396) */
	@Test
	public void listItemsWithTrailingLeading() throws Exception {
		assertAllNotes(pdfText(360));
	}
}
