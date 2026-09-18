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
package org.docx4j.fop.wordlayout;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A table-of-contents entry is broken against the page number it will have, not against
 * FOP's {@code "MMM"} placeholder.
 *
 * <p>The shape is a corpus document's {@code toc 2} entry: the section number, a tab to
 * the hanging indent, the title (which ends in a long token), then a right tab stop with
 * a dot leader at the right margin and the page number - an
 * {@code fo:page-number-citation} pointing <em>forward</em>, so FOP cannot resolve it
 * while the line is being broken and measures it as three capital Ms instead.  Times
 * 12pt: {@code "MMM"} is 32.0pt, {@code "000"} 18.0pt.
 *
 * <p>So the title's token has to be shorter to fit the first line under FOP's placeholder
 * than under a page number's own width, which is what
 * {@link WordPageNumberCitationLayoutManager} corrects.  Measured on this shape, the
 * placeholder costs the token three characters and the digits give two of them back (the
 * third is the 6pt by which {@code "000"} is wider than a two-digit number); in the
 * corpus document it was one line and, from there on, every page one out.
 *
 * @since 17.1.1
 */
public class PageNumberCitationWidthTest {

	private static final String DOCX4J_NS = "http://docx4j.org/fop/word-layout";

	/** The entry: Times 12pt on an A4 page with the corpus document's own margins, so the
	 *  region is 481.85pt wide, and the first line starts at the margin (start-indent
	 *  31.2pt with the hanging indent taken back off it). */
	private static String fo(int tokenChars, String number) {
		StringBuilder token = new StringBuilder();
		for (int i = 0; i < tokenChars; i++) token.append('x');
		return "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" xmlns:docx4j=\"" + DOCX4J_NS + "\">"
				+ "<fo:layout-master-set><fo:simple-page-master master-name=\"m\" page-width=\"595.25pt\""
				+ " page-height=\"841.85pt\" margin-left=\"70.9pt\" margin-right=\"42.5pt\""
				+ " margin-top=\"56.7pt\" margin-bottom=\"56.7pt\">"
				+ "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
				+ "<fo:page-sequence master-reference=\"m\"><fo:flow flow-name=\"xsl-region-body\">"
				+ "<fo:block font-family=\"Times\" font-size=\"12pt\" line-height=\"14pt\""
				+ " start-indent=\"31.2pt\" text-indent=\"-31.2pt\" text-align-last=\"justify\""
				+ " docx4j:tab-default=\"709\" docx4j:tab-ind=\"624:-624:.\" docx4j:tabs=\"9627:right:dot\">"
				+ "<fo:inline>4.33</fo:inline>"
				+ "<fo:inline><fo:leader docx4j:tab=\"1\" leader-length=\"0pt\" leader-pattern=\"dots\""
				+ " leader-alignment=\"reference-area\"/></fo:inline>"
				+ "<fo:inline>Constante " + token + "</fo:inline>"
				+ "<fo:inline><fo:leader docx4j:toc-leader=\"dot\" leader-length.minimum=\"12pt\""
				+ " leader-length.optimum=\"40pt\" leader-length.maximum=\"100%\" leader-pattern=\"dots\"/>"
				+ number + "</fo:inline>"
				+ "</fo:block>"
				+ "<fo:block break-before=\"page\" id=\"tgt\">the page the entry cites</fo:block>"
				+ "</fo:flow></fo:page-sequence></fo:root>";
	}

	/** The entry with its page number as an unresolved forward citation, as a table of
	 *  contents has it. */
	private static String withCitation(int tokenChars) {
		return fo(tokenChars, "<fo:page-number-citation ref-id=\"tgt\"/>");
	}

	/** The same entry with the page number set as text: what the line would be broken
	 *  against if FOP knew the number, and what Word breaks it against. */
	private static String withTheNumberItself(int tokenChars) {
		return fo(tokenChars, "23");
	}

	/** How many lines the entry takes (the page the citation points at is a block of its
	 *  own on page 2, and is not counted). */
	private static int entryLines(String fo) throws Exception {
		List<String> lines = lines(fo);
		return lines.size() - 1;
	}

	private static List<String> lines(String fo) throws Exception {
		FopFactoryBuilder b = new FopFactoryBuilder(new File(".").toURI());
		b.setLayoutManagerMakerOverride(new WordLayoutManagerMaker());
		FopFactory factory = b.build();
		FOUserAgent ua = factory.newFOUserAgent();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Fop fop = factory.newFop(MimeConstants.MIME_FOP_AREA_TREE, ua, out);
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.transform(new StreamSource(new ByteArrayInputStream(fo.getBytes("UTF-8"))),
				new SAXResult(fop.getDefaultHandler()));
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
		List<String> lines = new ArrayList<String>();
		NodeList las = doc.getElementsByTagName("lineArea");
		for (int i = 0; i < las.getLength(); i++) {
			StringBuilder sb = new StringBuilder();
			collectWords((Element) las.item(i), sb);
			lines.add(sb.toString().trim().replaceAll(" +", " "));
		}
		return lines;
	}

	private static void collectWords(Element el, StringBuilder sb) {
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (!(n instanceof Element)) continue;
			Element c = (Element) n;
			if ("word".equals(c.getLocalName())) sb.append(c.getTextContent());
			else if ("space".equals(c.getLocalName())) sb.append(' ');
			else collectWords(c, sb);
		}
	}

	/** The token which only just fits the entry's first line when the page number is
	 *  measured as a page number: 56 characters.  Measured, the entry takes one line at
	 *  55 and 56 and two at 57 with the digits placeholder, one line up to 57 and two at
	 *  58 with the number itself set as text, and two lines from 55 on with FOP's
	 *  {@code "MMM"} - so the placeholder is worth two of the three characters
	 *  {@code "MMM"} was taking, and the last one is the 6pt by which {@code "000"} is
	 *  wider than {@code "23"}. */
	private static final int FITS = 56;

	/** A token no page number can make fit: two lines however the number is measured. */
	private static final int TOO_LONG = 58;

	/**
	 * The gate: the entry whose token fits the line keeps one line, where FOP's "MMM"
	 * placeholder took two.
	 */
	@Test
	public void anEntryIsBrokenAgainstThePageNumberNotAgainstMMM() throws Exception {
		assertEquals("one line", 1, entryLines(withCitation(FITS)));
	}

	/** And the number itself agrees: the same token, one line. */
	@Test
	public void theNumberItselfBreaksItTheSameWay() throws Exception {
		assertEquals(1, entryLines(withTheNumberItself(FITS)));
	}

	/** FOP's own placeholder, which the property restores, takes two lines for it. */
	@Test
	public void fopsPlaceholderCostsTheTokenItsLine() throws Exception {
		System.setProperty(WordLayoutCustomizer.PAGE_NUMBER_PLACEHOLDER, "MMM");
		try {
			assertEquals("two lines", 2, entryLines(withCitation(FITS)));
		} finally {
			System.clearProperty(WordLayoutCustomizer.PAGE_NUMBER_PLACEHOLDER);
		}
	}

	/** A token one character longer does not fit either way: the placeholder decides
	 *  where the line breaks, it does not stop the line breaking. */
	@Test
	public void aTokenTooLongForTheLineStillMovesDown() throws Exception {
		assertEquals(2, entryLines(withCitation(TOO_LONG)));
		assertEquals(2, entryLines(withTheNumberItself(TOO_LONG)));
	}
}
