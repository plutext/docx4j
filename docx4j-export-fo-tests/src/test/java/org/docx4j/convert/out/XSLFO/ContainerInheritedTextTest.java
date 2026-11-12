/*
 * Copyright 2026, Plutext Pty Ltd.
 *
 * This file is part of docx4j.
 *
 * docx4j is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.docx4j.wml.Document;
import org.docx4j.wml.PPrBase.PBdr;
import org.docx4j.wml.Styles;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A borders or shading container is built from its first paragraph's properties, and holds the other paragraphs of
 * its group. The paragraphs after the first take none of its text's properties: a bold, coloured, centred heading at
 * the top of a box doesn't make the box's other paragraphs bold, coloured or centred. And borders that draw nothing
 * make no container (an ONLYOFFICE document's empty w:pBdr in every style made one of each run of paragraphs, under
 * which a contract's clauses came out in a Heading's bold dark blue). @since 17.3.7
 */
public class ContainerInheritedTextTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";
	private static final String BOX = "<w:pBdr><w:top w:val=\"single\" w:sz=\"4\" w:space=\"1\" w:color=\"auto\"/>"
			+ "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"1\" w:color=\"auto\"/></w:pBdr>";
	/** A paragraph style of a heading's text: bold, dark blue, 12pt, centred */
	private static final String STYLE = "<w:style " + W + " w:type=\"paragraph\" w:styleId=\"BoxHeading\">"
			+ "<w:name w:val=\"Box Heading\"/><w:pPr><w:jc w:val=\"center\"/></w:pPr>"
			+ "<w:rPr><w:b/><w:i/><w:color w:val=\"1F3864\"/><w:sz w:val=\"24\"/></w:rPr></w:style>";

	private static WordprocessingMLPackage pkg(String headingPPr, String plainPPr) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Styles styles = pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement();
		styles.getStyle().add((org.docx4j.wml.Style) XmlUtils.unmarshalString(STYLE));
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr><w:pStyle w:val=\"BoxHeading\"/>" + headingPPr + "</w:pPr><w:r><w:t>The heading</w:t></w:r></w:p>"
				+ "<w:p><w:pPr>" + plainPPr + "</w:pPr><w:r><w:t>Plain text after it</w:t></w:r></w:p>"
				+ SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** The innermost block holding the text */
	private static Element blockOf(org.w3c.dom.Document doc, String text) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "block");
		Element found = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Element b = (Element) nl.item(i);
			if (b.getTextContent().contains(text)) found = b; // document order: the last is the innermost
		}
		return found;
	}

	/** The value the block takes for an inherited property: its own, else its nearest ancestor's; "" for none */
	private static String inherited(Element block, String property) {
		for (Node n = block; n instanceof Element; n = n.getParentNode()) {
			Element e = (Element) n;
			if (e.getLocalName().equals("flow")) return "";
			if (e.hasAttribute(property)) return e.getAttribute(property);
		}
		return "";
	}

	/** The block of a paragraph inside a container (at least two block ancestors below the flow) */
	private static int depth(Element block) {
		int d = 0;
		for (Node n = block.getParentNode(); n instanceof Element && !((Element) n).getLocalName().equals("flow"); n = n.getParentNode()) d++;
		return d;
	}

	private static void plainTakesNothingFromTheHeading(String headingPPr, String plainPPr, int flags) throws Exception {
		org.w3c.dom.Document doc = fo(pkg(headingPPr, plainPPr), flags);
		Element heading = blockOf(doc, "The heading"), plain = blockOf(doc, "Plain text after it");
		assertNotNull(heading);
		assertNotNull(plain);
		// the heading keeps its own
		assertEquals("bold", inherited(heading, "font-weight"));
		assertEquals("#1F3864", inherited(heading, "color").toUpperCase());
		assertEquals("center", inherited(heading, "text-align"));
		// the plain paragraph, in the same container, takes none of them
		String where = " (flags " + flags + ", depth " + depth(plain) + ")";
		assertEquals("font-weight" + where, "", inherited(plain, "font-weight"));
		assertEquals("font-style" + where, "", inherited(plain, "font-style"));
		assertEquals("color" + where, "", inherited(plain, "color"));
		assertEquals("text-align" + where, "", inherited(plain, "text-align"));
	}

	@Test
	public void aBorderedHeadingPassesNothingOn() throws Exception {
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			org.w3c.dom.Document doc = fo(pkg(BOX, BOX), flags);
			// the two paragraphs share a box
			assertEquals("one container" + flags, blockOf(doc, "The heading").getParentNode(),
					blockOf(doc, "Plain text after it").getParentNode());
			plainTakesNothingFromTheHeading(BOX, BOX, flags);
		}
	}

	@Test
	public void aShadedHeadingPassesNothingOn() throws Exception {
		String shd = "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"D9D9D9\"/>";
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			plainTakesNothingFromTheHeading(shd, shd, flags);
		}
	}

	/** ONLYOFFICE's empty w:pBdr in the document defaults (and every style) and on each paragraph: no box, no container */
	@Test
	public void emptyBordersMakeNoContainer() throws Exception {
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			WordprocessingMLPackage pkg = pkg("<w:pBdr/>", "<w:pBdr/>");
			Styles styles = pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement();
			styles.getDocDefaults().getPPrDefault().getPPr().setPBdr(new PBdr());
			org.w3c.dom.Document doc = fo(pkg, flags);
			Element plain = blockOf(doc, "Plain text after it");
			assertEquals("the plain paragraph's block is the flow's" + flags, "flow", ((Element) plain.getParentNode()).getLocalName());
			plainTakesNothingFromTheHeading("<w:pBdr/>", "<w:pBdr/>", flags);
		}
	}
}
