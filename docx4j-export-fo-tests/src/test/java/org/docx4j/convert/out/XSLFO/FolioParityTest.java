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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A restarted folio takes its oddPage or evenPage section's parity, and costs a blank page only
 * with different odd and even headers (word-layout-rules.md &#xa7;7, 17.3.1): the
 * page-number-restart-parity probes.  Where the numbering continues, an oddPage section's
 * blank page is as before.
 */
public class FolioParityTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";
	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String sectPr(String type, Integer start) {
		return "<w:sectPr>" + (type == null ? "" : "<w:type w:val=\"" + type + "\"/>")
				+ "<w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"708\" w:footer=\"708\"/>"
				+ (start == null ? "" : "<w:pgNumType w:start=\"" + start + "\"/>") + "</w:sectPr>";
	}

	private static List<Element> sequences(Integer start, boolean evenOdd, int flags) throws Exception {
		return sequences("oddPage", start, evenOdd, flags);
	}

	/** S1, then a section of this type restarting at {@code start} (or continuing, where null) */
	private static List<Element> sequences(String type, Integer start, boolean evenOdd, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr>" + sectPr(null, null) + "</w:pPr><w:r><w:t>S1</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:t>S2</w:t></w:r></w:p>"
				+ sectPr(type, start) + "</w:body></w:document>"));
		if (evenOdd) {
			pkg.getMainDocumentPart().getDocumentSettingsPart().getJaxbElement()
					.setEvenAndOddHeaders(new org.docx4j.wml.BooleanDefaultTrue());
		}
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
		NodeList nl = doc.getElementsByTagNameNS(FO_NS, "page-sequence");
		List<Element> out = new ArrayList<Element>();
		for (int i = 0; i < nl.getLength(); i++) out.add((Element) nl.item(i));
		assertEquals("two sections, two page sequences", 2, out.size());
		return out;
	}

	@Test
	public void anEvenRestartOnAnOddPageSectionTakesTheNextOdd() throws Exception {
		for (int flags : FLAGS) {
			List<Element> seq = sequences(4, false, flags);
			assertEquals("4 prints 5", "5", seq.get(1).getAttribute("initial-page-number"));
			assertEquals("no blank page without evenAndOddHeaders", "no-force", seq.get(0).getAttribute("force-page-count"));
		}
	}

	@Test
	public void withEvenAndOddHeadersARestartKeepsTheParityAlternating() throws Exception {
		for (int flags : FLAGS) {
			List<Element> seq = sequences(4, true, flags);
			assertEquals("4 prints 5", "5", seq.get(1).getAttribute("initial-page-number"));
			assertEquals("a blank page where the parity would repeat", "auto", seq.get(0).getAttribute("force-page-count"));
		}
	}

	/** For a nextPage restart, on by default since 17.3.1 (docx4j.convert.out.fo.wordLayout.restartParityBlankPage,
	 *  CR-031 phase 1), and off where the property says so. */
	@Test
	public void aNextPageRestartTakesItsBlankPageUnlessThePropertySaysNot() throws Exception {
		try {
			for (int flags : FLAGS) {
				assertEquals("a blank page where the parity would repeat", "auto",
						sequences("nextPage", 7, true, flags).get(0).getAttribute("force-page-count"));
			}
			org.docx4j.Docx4jProperties.setProperty("docx4j.convert.out.fo.wordLayout.restartParityBlankPage", false);
			for (int flags : FLAGS) {
				assertEquals("off", "no-force",
						sequences("nextPage", 7, true, flags).get(0).getAttribute("force-page-count"));
			}
		} finally {
			org.docx4j.Docx4jProperties.getProperties().remove("docx4j.convert.out.fo.wordLayout.restartParityBlankPage");
		}
	}

	@Test
	public void continuedNumberingKeepsItsBlankPage() throws Exception {
		for (int flags : FLAGS) {
			List<Element> seq = sequences(null, false, flags);
			assertEquals("", seq.get(1).getAttribute("initial-page-number"));
			assertEquals("end-on-even", seq.get(0).getAttribute("force-page-count"));
		}
	}
}
