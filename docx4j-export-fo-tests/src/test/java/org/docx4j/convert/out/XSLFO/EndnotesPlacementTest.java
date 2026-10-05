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

import java.io.ByteArrayOutputStream;

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
 * Word prints endnotes only where there are some, and by default once, at the end of the
 * document ({@code w:endnotePr/w:pos} docEnd); {@code sectEnd} puts them at each section's end.
 * docx4j wrote its "Endnotes" heading wherever an endnotes part existed, at the end of every
 * section: measured on the four corpora, 10 documents printed a heading Word does not (their
 * parts hold Word's separators and a continuation notice and nothing else), one of them at the
 * end of each of its two sections, and 8695, with two endnotes and six sections, printed it
 * twice (XsltFOFunctions.endnotesHere).
 *
 * @since 17.3.1
 */
public class EndnotesPlacementTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static final String SEPARATORS =
			"<w:endnote w:type=\"separator\" w:id=\"-1\"><w:p><w:r><w:separator/></w:r></w:p></w:endnote>"
			+ "<w:endnote w:type=\"continuationSeparator\" w:id=\"0\"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:endnote>"
			+ "<w:endnote w:type=\"continuationNotice\" w:id=\"1\"><w:p/></w:endnote>";

	/** Two sections (nextPage); with a real endnote, referenced from the first. */
	private static WordprocessingMLPackage pkg(boolean realNote, boolean sectEnd) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>Section one.</w:t></w:r>"
				+ (realNote ? "<w:r><w:endnoteReference w:id=\"2\"/></w:r>" : "") + "</w:p>"
				+ "<w:p><w:pPr><w:sectPr/></w:pPr></w:p>"
				+ "<w:p><w:r><w:t>Section two.</w:t></w:r></w:p>"
				+ "<w:sectPr/></w:body></w:document>"));
		EndnotesPart ep = new EndnotesPart();
		ep.setJaxbElement((CTEndnotes) XmlUtils.unmarshalString(
				"<w:endnotes " + W + ">" + SEPARATORS
				+ (realNote ? "<w:endnote w:id=\"2\"><w:p><w:r><w:endnoteRef/></w:r>"
						+ "<w:r><w:t xml:space=\"preserve\"> The endnote.</w:t></w:r></w:p></w:endnote>" : "")
				+ "</w:endnotes>", Context.jc, CTEndnotes.class));
		pkg.getMainDocumentPart().addTargetPart(ep);
		if (sectEnd) {
			org.docx4j.wml.CTEdnDocProps pr = Context.getWmlObjectFactory().createCTEdnDocProps();
			org.docx4j.wml.CTEdnPos pos = Context.getWmlObjectFactory().createCTEdnPos();
			pos.setVal(org.docx4j.wml.STEdnPos.SECT_END);
			pr.setPos(pos);
			pkg.getMainDocumentPart().getDocumentSettingsPart(true).getJaxbElement().setEndnotePr(pr);
		}
		return pkg;
	}

	/** How many "Endnotes" headings each page-sequence carries. */
	private int[] headings(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		NodeList seqs = w3cDomDocumentFromByteArray(baos.toByteArray()).getElementsByTagNameNS(FO, "page-sequence");
		int[] out = new int[seqs.getLength()];
		for (int i = 0; i < out.length; i++) {
			NodeList blocks = ((Element) seqs.item(i)).getElementsByTagNameNS(FO, "block");
			for (int j = 0; j < blocks.getLength(); j++) {
				if ("Endnotes".equals(blocks.item(j).getTextContent())) out[i]++;
			}
		}
		return out;
	}

	@Test
	public void separatorsAloneAreNoEndnotes() throws Exception {
		for (int flags : FLAGS) {
			int[] h = headings(pkg(false, false), flags);
			assertEquals(2, h.length);
			assertEquals("no heading in section one", 0, h[0]);
			assertEquals("none in section two", 0, h[1]);
		}
	}

	@Test
	public void endnotesOnceAtTheEndOfTheDocument() throws Exception {
		for (int flags : FLAGS) {
			int[] h = headings(pkg(true, false), flags);
			assertEquals("not at the end of section one", 0, h[0]);
			assertEquals("at the end of the document", 1, h[1]);
		}
	}

	@Test
	public void sectEndPutsThemAtEachSectionsEnd() throws Exception {
		for (int flags : FLAGS) {
			int[] h = headings(pkg(true, true), flags);
			assertEquals(1, h[0]);
			assertEquals(1, h[1]);
		}
	}
}
