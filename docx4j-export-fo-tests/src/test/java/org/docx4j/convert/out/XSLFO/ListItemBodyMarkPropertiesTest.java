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

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The paragraph mark's run properties belong to the <b>mark</b> and to the <b>number</b>,
 * not to the paragraph's text: only the size reaches the {@code fo:list-item-body}.
 *
 * <p>The body is formatted at all because FOP aligns a list item's label and its body on
 * their baselines only where the font size is set at the same level on both. Everything
 * else the mark carried rode along with it, and the body is the wrapper the paragraph's
 * text sits in - so the text inherited it.</p>
 *
 * <p>Measured on a corpus document whose numbered paragraphs carry {@code <w:b/>} on the
 * paragraph mark and nothing bold anywhere else (not the document defaults, not
 * {@code Normal}, not {@code ListParagraph}, not the runs, and the numbering level's own
 * {@code w:rPr} says {@code <w:b w:val="0"/>}): <b>62 of its 175</b> bodies came out
 * {@code font-weight="bold"}, where Word draws that text in {@code ArialMT}. Line parity
 * 0.7677 -&gt; 0.92 with this (CR-001 batch 46 item 4).</p>
 *
 * @since 17.2.0
 */
public class ListItemBodyMarkPropertiesTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	/** One decimal level, with {@code levelRPr} as its own {@code w:rPr}. */
	private static String numbering(String levelRPr) {
		return NUMBERING.replace("@LVLRPR@", levelRPr);
	}

	/** The corpus document's level: its own w:rPr turns bold off, so the number is not
	 *  bold either however bold the paragraph mark is. */
	private static final String LEVEL_NOT_BOLD =
			"<w:rPr><w:rFonts w:hint=\"default\"/><w:b w:val=\"0\"/></w:rPr>";

	/** A level which says nothing about the weight: the number then takes the mark's. */
	private static final String LEVEL_SILENT = "<w:rPr><w:rFonts w:hint=\"default\"/></w:rPr>";

	private static final String NUMBERING = "<w:numbering " + W + ">"
			+ "<w:abstractNum w:abstractNumId=\"0\"><w:multiLevelType w:val=\"multilevel\"/>"
			+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>"
			+ "<w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>"
			+ "<w:pPr><w:ind w:left=\"360\" w:hanging=\"360\"/></w:pPr>"
			+ "@LVLRPR@</w:lvl>"
			+ "</w:abstractNum>"
			+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num></w:numbering>";

	/**
	 * A numbered paragraph whose <b>paragraph mark</b> is bold and 28 half-points, and
	 * whose runs say nothing about either.
	 */
	private org.w3c.dom.Document fo(int flags, String levelRPr) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart ndp
				= new org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart();
		ndp.setJaxbElement((org.docx4j.wml.Numbering) XmlUtils.unmarshalString(numbering(levelRPr)));
		pkg.getMainDocumentPart().addTargetPart(ndp);

		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr>"
				+ "<w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr>"
				+ "<w:rPr><w:b/><w:i/><w:caps/><w:sz w:val=\"28\"/></w:rPr>"   // the MARK's
				+ "</w:pPr>"
				+ "<w:r><w:t>the text of the item</w:t></w:r></w:p>"
				+ SECT_PR + "</w:body></w:document>"));

		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	private static Element only(org.w3c.dom.Document doc, String localName) {
		NodeList list = doc.getElementsByTagNameNS(FO, localName);
		return list.getLength() > 0 ? (Element) list.item(0) : null;
	}

	/** The corpus document's shape: a bold paragraph mark and a level which says the
	 *  number is not bold.  Word draws neither the number nor the text bold. */
	private void markDoesNotReachTheText(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(flags, LEVEL_NOT_BOLD);

		Element body = only(doc, "list-item-body");
		assertNotNull("the list item's body", body);
		assertEquals("the size reaches the body: FOP aligns label and body on it",
				"14.0pt", body.getAttribute("font-size"));
		assertEquals("the mark's bold does not: it is the mark's, not the text's",
				"", body.getAttribute("font-weight"));
		assertEquals("nor its italic", "", body.getAttribute("font-style"));
		assertEquals("nor any other of the mark's properties",
				"", body.getAttribute("text-transform"));

		Element label = only(doc, "list-item-label");
		assertNotNull("the list item's label", label);
		assertEquals("and the level's own w:rPr says the number is not bold either",
				"normal", label.getAttribute("font-weight"));
		assertEquals("the size is the mark's on both", "14.0pt", label.getAttribute("font-size"));
	}

	/** Where the level says nothing about the weight, the <b>number</b> takes the mark's
	 *  bold - the label is where the mark's properties belong - and the text still does
	 *  not. */
	private void markStillReachesTheNumber(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(flags, LEVEL_SILENT);

		Element label = only(doc, "list-item-label");
		assertNotNull("the list item's label", label);
		assertEquals("the number takes the paragraph mark's weight",
				"bold", label.getAttribute("font-weight"));

		Element body = only(doc, "list-item-body");
		assertNotNull("the list item's body", body);
		assertEquals("the paragraph's text does not", "", body.getAttribute("font-weight"));
		assertEquals("14.0pt", body.getAttribute("font-size"));
	}

	@Test
	public void markDoesNotReachTheTextVisitor() throws Exception {
		markDoesNotReachTheText(Docx4J.FLAG_NONE);
	}

	@Test
	public void markDoesNotReachTheTextXslt() throws Exception {
		markDoesNotReachTheText(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void markStillReachesTheNumberVisitor() throws Exception {
		markStillReachesTheNumber(Docx4J.FLAG_NONE);
	}

	@Test
	public void markStillReachesTheNumberXslt() throws Exception {
		markStillReachesTheNumber(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
