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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.docx4j.wml.Numbering;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * The tab which follows a numbering label stops at the first tab stop past the label and
 * before {@code w:ind} left - of the <b>paragraph's</b> stops as well as the level's.
 *
 * <p>Measured on a corpus CV whose bulleted paragraphs carry
 * {@code w:ind w:left="993" w:hanging="567"} and their own
 * {@code w:tab w:val="left" w:pos="709"} over a level whose only stop is
 * {@code w:tab w:val="num" w:pos="786"}: Word sets the text of those lines at x=72.74,
 * which is the paragraph's 709 stop from a 37.28pt text origin, where docx4j set it at
 * 76.50 - the level's 786 - reading the level's stops alone.  The paragraph's two other
 * stops there, {@code w:val="num"} at -996 and at 1548, need no rule of their own: one is
 * behind the label and the other past {@code w:ind} left, and both filters already
 * existed.</p>
 *
 * @since 17.2.0 (CR-001 batch 48 item 6)
 */
public class NumberingTabParagraphStopTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	/** A bullet level at w:ind left 993 hanging 567 whose only stop is a num stop at 786. */
	private static final String NUMBERING = "<w:numbering " + W + ">"
			+ "<w:abstractNum w:abstractNumId=\"1\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
			+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"bullet\"/>"
			+ "<w:suff w:val=\"tab\"/><w:lvlText w:val=\"-\"/><w:lvlJc w:val=\"left\"/>"
			+ "<w:pPr><w:tabs><w:tab w:val=\"num\" w:pos=\"786\"/></w:tabs>"
			+ "<w:ind w:left=\"993\" w:hanging=\"567\"/></w:pPr>"
			+ "</w:lvl></w:abstractNum>"
			+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"1\"/></w:num>"
			+ "</w:numbering>";

	private static String item(String tabs) {
		return "<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr>"
				+ (tabs == null ? "" : "<w:tabs>" + tabs + "</w:tabs>")
				+ "<w:ind w:left=\"993\" w:hanging=\"567\"/>"
				+ "</w:pPr><w:r><w:t>the item's text</w:t></w:r></w:p>";
	}

	private static org.w3c.dom.Document fo(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		ndp.setJaxbElement((Numbering) XmlUtils.unmarshalString(NUMBERING));
		pkg.getMainDocumentPart().addTargetPart(ndp);
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** The n-th list item's body block, whose text-indent pulls the first line to the stop. */
	private static Element body(org.w3c.dom.Document doc, int n) {
		Element holder = (Element) doc.getElementsByTagNameNS(FO, "list-item-body").item(n);
		assertNotNull("no list item " + n, holder);
		return (Element) holder.getElementsByTagNameNS(FO, "block").item(0);
	}

	private void check(int flags) throws Exception {

		/* 0: the level's stop alone (786) - 993 - 786 = 207 twips = 10.35pt back from the
		 *    paragraph's indent.
		 * 1: the paragraph's own 709 stop comes first - 993 - 709 = 284 twips = 14.2pt.
		 * 2: the paragraph's stops which cannot be reached: -996 is behind the label and
		 *    1548 is past w:ind left, so the level's 786 stands.
		 * 3: a paragraph stop past the level's - the level's 786 is still the first. */
		org.w3c.dom.Document doc = fo(
				item(null)
				+ item("<w:tab w:val=\"num\" w:pos=\"-996\"/><w:tab w:val=\"left\" w:pos=\"709\"/>"
						+ "<w:tab w:val=\"num\" w:pos=\"1548\"/>")
				+ item("<w:tab w:val=\"num\" w:pos=\"-996\"/><w:tab w:val=\"num\" w:pos=\"1548\"/>")
				+ item("<w:tab w:val=\"left\" w:pos=\"900\"/>"), flags);

		assertEquals("the level's own stop, as before", "-10.35pt",
				body(doc, 0).getAttribute("text-indent"));
		assertEquals("the paragraph's 709 stop comes before the level's 786", "-14.2pt",
				body(doc, 1).getAttribute("text-indent"));
		assertEquals("a stop behind the label and one past w:ind left are both dropped",
				"-10.35pt", body(doc, 2).getAttribute("text-indent"));
		assertEquals("and where the paragraph's stop is later, the level's still wins",
				"-10.35pt", body(doc, 3).getAttribute("text-indent"));
	}

	@Test
	public void visitorPathway() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xsltPathway() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
