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
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.docx4j.wml.Numbering;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A left paragraph border against a hanging indent: Word stands the bar at the
 * paragraph's leftmost text edge less the border's {@code w:space}, and with a hanging
 * indent that edge is the first line's, not {@code w:ind left}.
 *
 * <p>The FO box model measures border and padding from the block's content rectangle,
 * whose start edge is the start-indent, so the bar was drawn at the start-indent less
 * the space - in the middle of the first line, which the negative text-indent runs back
 * over.  Measured on a plain FOP render of both shapes (start-indent 56.7pt, text-indent
 * -56.7pt, 8pt space, 2.25pt border): the lines open at 70.900 and 127.600 and the bar
 * is stroked down x=117.350; with the padding carrying the hanging indent as well the
 * lines do not move and the bar is stroked down x=60.650, the leftmost text edge less
 * the space.  The correction is therefore the padding alone
 * ({@code XsltFOFunctions.borderAgainstHangingIndent}).</p>
 *
 * @since 17.1.1 (CR-001 batch 48 item 9)
 */
public class BorderAgainstHangingIndentTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";

	private static final String BORDER =
			"<w:pBdr><w:left w:val=\"single\" w:sz=\"18\" w:space=\"8\" w:color=\"000000\"/></w:pBdr>";

	/** numId 1 -> a level indenting 1134 with a 1134tw hanging indent */
	private static final String NUMBERING =
			"<w:numbering " + W + ">"
			+ "<w:abstractNum w:abstractNumId=\"0\"><w:lvl w:ilvl=\"0\">"
			+ "<w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%1.\"/>"
			+ "<w:lvlJc w:val=\"left\"/>"
			+ "<w:pPr><w:ind w:left=\"1134\" w:hanging=\"1134\"/></w:pPr>"
			+ "</w:lvl></w:abstractNum>"
			+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num>"
			+ "</w:numbering>";

	private static org.w3c.dom.Document fo(String pPr, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart(new PartName("/word/numbering.xml"));
		ndp.setJaxbElement((Numbering) XmlUtils.unmarshalString(NUMBERING));
		pkg.getMainDocumentPart().addTargetPart(ndp);
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr>" + pPr + "</w:pPr>"
				+ "<w:r><w:t>the paragraph under test</w:t></w:r></w:p>"
				+ "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** The block which carries the left border. */
	private static Element bordered(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "block");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (el.getAttribute("border-left-style").length() > 0
					&& !"none".equals(el.getAttribute("border-left-style"))) return el;
		}
		return null;
	}

	private void check(int flags) throws Exception {

		Element hanging = bordered(fo(BORDER + "<w:ind w:left=\"1134\" w:hanging=\"1134\"/>", flags));
		assertNotNull("the bordered paragraph", hanging);
		assertEquals("the content edge is still w:ind left", "56.7pt", hanging.getAttribute("start-indent"));
		assertEquals("and the first line still runs back over it", "-56.7pt",
				hanging.getAttribute("text-indent"));
		assertEquals("the space carries the hanging indent, so the bar stands left of both lines",
				"64.7pt", hanging.getAttribute("padding-left"));

		Element firstLine = bordered(fo(BORDER + "<w:ind w:left=\"1134\" w:firstLine=\"567\"/>", flags));
		assertNotNull("the firstLine control", firstLine);
		assertEquals("a forward first line leaves w:ind left as the leftmost edge, so the space stands",
				"8pt", firstLine.getAttribute("padding-left"));

		Element noBorder = bordered(fo("<w:ind w:left=\"1134\" w:hanging=\"1134\"/>", flags));
		assertEquals("nothing to stand anywhere without a left border", null, noBorder);

		Element numbered = bordered(fo(BORDER
				+ "<w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr>", flags));
		assertNotNull("the numbered bordered paragraph", numbered);
		assertEquals("the label column, which is where a numbered paragraph's leftmost ink is",
				"64.7pt", numbered.getAttribute("padding-left"));

		Element noSpace = bordered(fo(
				"<w:pBdr><w:left w:val=\"single\" w:sz=\"18\" w:color=\"000000\"/></w:pBdr>"
				+ "<w:ind w:left=\"1134\" w:hanging=\"1134\"/>", flags));
		assertNotNull("a border with no w:space", noSpace);
		assertEquals("no space is a zero gap, and the shift is the hanging indent alone",
				"56.7pt", noSpace.getAttribute("padding-left"));

		/* Word's own clamp, measured on three corpus documents: a hanging indent wider
		 * than w:ind left would hang the first line to the left of the text area, and
		 * Word hangs nothing there - so the bar stays against the area's edge. */
		Element hangingPastTheEdge = bordered(fo(BORDER + "<w:ind w:hanging=\"1134\"/>", flags));
		assertNotNull("a hanging indent with no w:ind left to hang inside", hangingPastTheEdge);
		assertEquals("the bar stands no further left than the area's own edge",
				"8pt", hangingPastTheEdge.getAttribute("padding-left"));

		Element hangingWiderThanLeft = bordered(fo(BORDER
				+ "<w:ind w:left=\"567\" w:hanging=\"1134\"/>", flags));
		assertEquals("and a partial hang shifts only as far as the edge",
				"36.35pt", hangingWiderThanLeft.getAttribute("padding-left"));
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
