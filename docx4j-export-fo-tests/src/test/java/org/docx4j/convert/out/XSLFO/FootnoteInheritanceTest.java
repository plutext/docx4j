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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.wml.CTFootnotes;
import org.docx4j.wml.Document;
import org.docx4j.wml.Styles;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * A footnote's formatting is its own: Word lays a note out the same whatever the paragraph
 * it is referenced from looks like (CR-030 D10, measured with probe tables-footnote-in-cell,
 * T8: all three notes upright and regular, from a bold italic cell, an italic cell and a plain
 * paragraph).  docx4j writes the note's body where it is referenced, inside the referencing
 * paragraph's fo:block and run's fo:inline, and until 17.3.1 XSL-FO inheritance carried into
 * the note whatever those stated and the note's own blocks did not: here bold, italic, red,
 * underlined.  Checked on what FOP lays out (the area tree), on both pathways.
 */
public class FootnoteInheritanceTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final String STYLES = "<w:styles " + W + ">"
			+ "<w:docDefaults><w:rPrDefault><w:rPr><w:rFonts w:ascii=\"Liberation Serif\" w:hAnsi=\"Liberation Serif\"/>"
			+ "<w:sz w:val=\"24\"/></w:rPr></w:rPrDefault><w:pPrDefault/></w:docDefaults>"
			+ "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/></w:style>"
			+ "<w:style w:type=\"character\" w:default=\"1\" w:styleId=\"DefaultParagraphFont\"><w:name w:val=\"Default Paragraph Font\"/></w:style>"
			// the referencing paragraph: bold, italic, red, underlined
			+ "<w:style w:type=\"paragraph\" w:styleId=\"Loud\"><w:name w:val=\"Loud\"/><w:basedOn w:val=\"Normal\"/>"
			+ "<w:rPr><w:b/><w:i/><w:color w:val=\"FF0000\"/><w:u w:val=\"single\"/></w:rPr></w:style>"
			+ "<w:style w:type=\"paragraph\" w:styleId=\"FootnoteText\"><w:name w:val=\"footnote text\"/><w:basedOn w:val=\"Normal\"/>"
			+ "<w:rPr><w:sz w:val=\"20\"/></w:rPr></w:style>"
			+ "<w:style w:type=\"character\" w:styleId=\"FootnoteReference\"><w:name w:val=\"footnote reference\"/>"
			+ "<w:rPr><w:vertAlign w:val=\"superscript\"/></w:rPr></w:style>"
			+ "</w:styles>";

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getStyleDefinitionsPart().setJaxbElement((Styles) XmlUtils.unmarshalString(STYLES));
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr><w:pStyle w:val=\"Loud\"/></w:pPr>"
				+ "<w:r><w:t xml:space=\"preserve\">loudly referencing </w:t></w:r>"
				+ "<w:r><w:rPr><w:rStyle w:val=\"FootnoteReference\"/></w:rPr><w:footnoteReference w:id=\"1\"/></w:r></w:p>"
				+ "</w:body></w:document>"));
		FootnotesPart fp = new FootnotesPart();
		pkg.getMainDocumentPart().addTargetPart(fp);
		fp.setJaxbElement((CTFootnotes) XmlUtils.unwrap(XmlUtils.unmarshalString("<w:footnotes " + W + ">"
				+ "<w:footnote w:type=\"separator\" w:id=\"-1\"><w:p><w:r><w:separator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:type=\"continuationSeparator\" w:id=\"0\"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:footnote>"
				+ "<w:footnote w:id=\"1\"><w:p><w:pPr><w:pStyle w:val=\"FootnoteText\"/></w:pPr>"
				+ "<w:r><w:rPr><w:rStyle w:val=\"FootnoteReference\"/></w:rPr><w:footnoteRef/></w:r>"
				+ "<w:r><w:t xml:space=\"preserve\"> quietly noted</w:t></w:r></w:p></w:footnote></w:footnotes>")));
		return pkg;
	}

	/** The area tree's text area holding the word. */
	private static Element textHolding(org.w3c.dom.Document areaTree, String word) {
		NodeList words = areaTree.getElementsByTagName("word");
		for (int i = 0; i < words.getLength(); i++) {
			if (word.equals(words.item(i).getTextContent().trim())) {
				return (Element) words.item(i).getParentNode();
			}
		}
		return null;
	}

	private static String traits(Element e) {
		StringBuilder sb = new StringBuilder();
		NamedNodeMap a = e.getAttributes();
		for (int i = 0; i < a.getLength(); i++) sb.append(' ').append(a.item(i));
		return sb.toString();
	}

	@Test
	public void aNoteIsNotFormattedLikeTheParagraphItIsReferencedFrom() throws Exception {
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			String pathway = flags == Docx4J.FLAG_NONE ? "visitor" : "XSL";
			org.w3c.dom.Document areaTree = areaTree(pkg(), flags);

			// the referencing paragraph is all of those things
			Element loud = textHolding(areaTree, "loudly");
			assertNotNull(pathway + ": no 'loudly'", loud);
			assertEquals(pathway + traits(loud), "700", loud.getAttribute("font-weight"));
			assertEquals(pathway + traits(loud), "italic", loud.getAttribute("font-style"));
			assertTrue(pathway + traits(loud), loud.getAttribute("color").toLowerCase().contains("ff0000"));
			assertEquals(pathway + traits(loud), "true", loud.getAttribute("underline-score"));

			// and the note none of them
			Element quiet = textHolding(areaTree, "quietly");
			assertNotNull(pathway + ": no 'quietly'", quiet);
			assertEquals(pathway + traits(quiet), "400", quiet.getAttribute("font-weight"));
			assertEquals(pathway + traits(quiet), "normal", quiet.getAttribute("font-style"));
			assertFalse(pathway + traits(quiet), quiet.getAttribute("color").toLowerCase().contains("ff0000"));
			assertFalse(pathway + traits(quiet), "true".equals(quiet.getAttribute("underline-score")));
		}
	}
}
