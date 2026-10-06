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

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Document;
import org.docx4j.wml.Hdr;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * An empty header <em>part</em> is not the same as no header at all: Word reserves the
 * header distance and the empty paragraph's line, as it does for an empty footer part
 * ({@link EmptyFooterPartTest}).  With no part ({@link HeaderDistanceTest},
 * {@link NoHeaderPartBodyTopTest}) it reserves nothing.
 *
 * <p>Measured against Word 365 on the header-empty-paragraph-compat probes (CR-001 batch
 * 53), in compatibility modes 12, 14 and 15 alike: {@code w:pgMar w:top="900"} (45pt) and
 * {@code w:header="720"} (36pt), {@code header1.xml} a single empty {@code w:p} of 12pt
 * Liberation Serif at {@code w:line="276"}, with no spacing before or after.  Word's body top
 * is 36 + 15.87 = 51.87; docx4j, which until 17.3.1 took such a part to reserve nothing,
 * started the body at the 45pt top margin.  65 corpus documents have such a part.</p>
 *
 * @since 17.3.1
 */
public class EmptyHeaderPartTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String flagName(int flag) {
		return flag == Docx4J.FLAG_EXPORT_PREFER_XSL ? "XSL" : "visitor";
	}

	/** A4 portrait, the given top margin, a 36pt header distance, and a header part holding
	 *  one empty paragraph with no spacing before or after. */
	private static WordprocessingMLPackage pkg(int topTwips) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		HeaderPart header = new HeaderPart(new PartName("/word/header1.xml"));
		header.setJaxbElement((Hdr)XmlUtils.unmarshalString(
				"<w:hdr " + W + "><w:p><w:pPr><w:spacing w:before=\"0\" w:after=\"0\"/></w:pPr></w:p></w:hdr>",
				org.docx4j.jaxb.Context.jc, Hdr.class));
		Relationship rel = pkg.getMainDocumentPart().addTargetPart(header);
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<w:body><w:p><w:r><w:t>first line of the body</w:t></w:r></w:p>"
				+ "<w:sectPr>"
				+ "<w:headerReference w:type=\"default\" r:id=\"" + rel.getId() + "\"/>"
				+ "<w:pgSz w:w=\"11907\" w:h=\"16839\"/>"
				+ "<w:pgMar w:top=\"" + topTwips + "\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\""
				+ " w:header=\"720\" w:footer=\"720\"/>"
				+ "</w:sectPr></w:body></w:document>"));
		return pkg;
	}

	/** The top edge (y, in millipoints) of the body region's viewport. */
	private static int bodyTop(org.w3c.dom.Document areaTree) {
		NodeList regions = areaTree.getElementsByTagName("regionBody");
		assertTrue("no regionBody", regions.getLength() > 0);
		Element viewport = (Element) regions.item(0).getParentNode();
		return Integer.parseInt(viewport.getAttribute("rect").trim().split("\\s+")[1]);
	}

	/** w:top=900 (45pt) under w:header=720 (36pt): the body starts below the distance plus
	 *  the empty paragraph's line - here Normal's Calibri 11pt at w:line="276", 15.44pt - so
	 *  at 51.44, not at the top margin. */
	@Test
	public void theBodyStartsBelowTheHeaderDistancePlusItsLine() throws Exception {
		for (int flag : FLAGS) {
			int top = bodyTop(areaTree(pkg(900), flag));
			assertTrue(flagName(flag) + ": the body starts at " + top / 1000.0
					+ "pt, not at 51.44 (36pt header distance + the empty header's 15.44pt line)",
					Math.abs(top - 51440) < 1500);
		}
	}

	/** Where the top margin reaches further than the header, the body starts there. */
	@Test
	public void aLargerTopMarginStillWins() throws Exception {
		for (int flag : FLAGS) {
			int top = bodyTop(areaTree(pkg(1440), flag));
			assertTrue(flagName(flag) + ": the body starts at " + top / 1000.0 + "pt, not at the 72pt top margin",
					Math.abs(top - 72000) < 100);
		}
	}
}
