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
 * A {@code w:type="continuous"} section break which changes the page size or the
 * orientation starts a page in Word, and the two parts cannot share a page master
 * (CR-001 &#xa7;7).
 *
 * <p>A run of continuous sections is merged into one {@code fo:page-sequence}, since
 * XSL-FO fixes the column count on the page master; that merge took the <em>last</em>
 * part's page size for the whole run.  Measured on a 22-page corpus document whose first
 * four {@code w:sectPr} are continuous and whose first three declare
 * {@code <w:pgSz w:w="23814" w:h="16840" w:orient="landscape"/>}: Word's page 1 is A3
 * landscape, 1190.6 x 841.9pt, with its content at x=76.6..396.3, where docx4j produced
 * 841.7 x 595.5 (A4 landscape, the last section's) and ran the content to x=881.9 - 40pt
 * past its own page edge, one column overprinting another.  With the break ending the
 * section instead, that document's page 1 is 1190.7 x 842.0 and its line parity went
 * 0.6494 to 0.6928.</p>
 *
 * <p>Sections whose page size agrees are still merged, which is what keeps a run of
 * continuous column changes on one page.</p>
 *
 * @since 17.1.0
 */
public class ContinuousSectionPageSizeTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	/** A3 landscape */
	private static final String A3L = "<w:pgSz w:w=\"23814\" w:h=\"16840\" w:orient=\"landscape\"/>";
	/** A4 landscape */
	private static final String A4L = "<w:pgSz w:w=\"16834\" w:h=\"11909\" w:orient=\"landscape\"/>";
	/** A4 portrait, the same measurements the other way up */
	private static final String A4P = "<w:pgSz w:w=\"11909\" w:h=\"16834\"/>";

	private static final String MAR =
			"<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/>";

	/**
	 * Two sections: the first continuous with page size {@code first}, the body's with
	 * page size {@code second}.
	 */
	private static String body(String first, String second) {
		return "<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>one</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:sectPr>"
				+ first + MAR
				+ "</w:sectPr></w:pPr></w:p>"
				+ "<w:p><w:r><w:t>two</w:t></w:r></w:p>"
				+ "<w:sectPr><w:type w:val=\"continuous\"/>"
				+ second + MAR
				+ "</w:sectPr></w:body></w:document>";
	}

	private org.w3c.dom.Document fo(String first, String second, int flags) throws Exception {
		return foOf(body(first, second), null, flags);
	}

	/** Two sections of the same page size: the first continuous with margins
	 *  {@code marFirst}, the body's with margins {@code marSecond}.  @since 17.1.1 */
	private static String bodyMar(String marFirst, String marSecond) {
		return "<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>one</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:sectPr>" + A4P + marFirst + "</w:sectPr></w:pPr></w:p>"
				+ "<w:p><w:r><w:t>two</w:t></w:r></w:p>"
				+ "<w:sectPr><w:type w:val=\"continuous\"/>" + A4P + marSecond
				+ "</w:sectPr></w:body></w:document>";
	}

	private org.w3c.dom.Document foOf(String document, String compatibilityMode, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(document));
		if (compatibilityMode != null) {
			pkg.getMainDocumentPart().getDocumentSettingsPart(true)
					.setWordCompatSetting("compatibilityMode", compatibilityMode);
		}
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	/** the page-width of each simple-page-master, in document order */
	private static List<String> pageWidths(org.w3c.dom.Document doc) {
		NodeList masters = doc.getElementsByTagNameNS(FO, "simple-page-master");
		List<String> out = new ArrayList<String>();
		for (int i = 0; i < masters.getLength(); i++) {
			String w = ((Element)masters.item(i)).getAttribute("page-width");
			if (!out.contains(w)) out.add(w);
		}
		return out;
	}

	private static int pageSequences(org.w3c.dom.Document doc) {
		return doc.getElementsByTagNameNS(FO, "page-sequence").getLength();
	}

	private void checkSplit(String first, String second, int flags) throws Exception {
		org.w3c.dom.Document doc = fo(first, second, flags);
		assertEquals("a continuous break changing the page size ends the section",
				2, pageSequences(doc));
		List<String> widths = pageWidths(doc);
		assertEquals("each part keeps its own page size: " + widths, 2, widths.size());
	}

	private void check(int flags) throws Exception {
		// a page size change
		checkSplit(A3L, A4L, flags);
		// an orientation change alone, the same measurements the other way up
		checkSplit(A4P, A4L, flags);

		// the control: sections whose page size agrees are still merged onto one
		// page-sequence, which is what keeps a continuous column change on one page
		org.w3c.dom.Document same = fo(A4L, A4L, flags);
		assertEquals("an unchanged page size still merges", 1, pageSequences(same));
		assertTrue("one page size", pageWidths(same).size() == 1);
	}

	@Test
	public void aContinuousPageSizeChangeSplitsTheSequenceVisitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void aContinuousPageSizeChangeSplitsTheSequenceXslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** 2in top margin on the second section, against 1in on the first. */
	private static final String MAR_TOP2 =
			"<w:pgMar w:top=\"2880\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"708\" w:footer=\"708\"/>";
	private static final String MAR_1 =
			"<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"708\" w:footer=\"708\"/>";
	/** the footer distance alone changes */
	private static final String MAR_FOOTER =
			"<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"708\" w:footer=\"5811\"/>";
	/** the left margin alone changes */
	private static final String MAR_LEFT2 =
			"<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"2880\" w:header=\"708\" w:footer=\"708\"/>";

	/**
	 * A continuous break which changes only the margins or the footer distance is still
	 * merged, in every mode: the probe which seemed to show Word starting a page for it
	 * changed the page size by a twip as well, and four mode-15 corpus documents whose
	 * continuous sections vary their vertical margins on one page went to many when it
	 * was tried.  @since 17.1.1
	 */
	@Test
	public void marginOrFooterDistanceChangeStillMerges() throws Exception {
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			for (String mode : new String[] { "12", "15" }) {
				assertEquals("top margin change, mode " + mode, 1, pageSequences(foOf(bodyMar(MAR_1, MAR_TOP2), mode, flags)));
				assertEquals("footer distance change, mode " + mode, 1, pageSequences(foOf(bodyMar(MAR_1, MAR_FOOTER), mode, flags)));
				assertEquals("left margin change, mode " + mode, 1, pageSequences(foOf(bodyMar(MAR_1, MAR_LEFT2), mode, flags)));
			}
		}
	}
}
