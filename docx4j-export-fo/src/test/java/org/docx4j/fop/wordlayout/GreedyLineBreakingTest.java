package org.docx4j.fop.wordlayout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
 * The greedy breaker through FOP itself, on a monospace base-14 font so every
 * expected break can be computed by counting characters: Courier is 0.6em per
 * glyph, so at 12pt on a 200pt line a line holds 27 characters.
 */
public class GreedyLineBreakingTest {

	private static final double CHAR = 7.2; // Courier 12pt advance
	private static final double LINE = 200;

	private static String fo(String text, String align) {
		return fo(text, align, null);
	}

	/** @param spaceShrink docx4j:space-shrink on fo:root, as docx4j writes it for a
	 *  document whose compatibility mode is below 15; null to leave it out. */
	private static String fo(String text, String align, String spaceShrink) {
		return "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\""
				+ (spaceShrink==null ? "" : " xmlns:docx4j=\"" + WordLayoutElementMapping.URI
						+ "\" docx4j:space-shrink=\"" + spaceShrink + "\"") + ">"
				+ "<fo:layout-master-set><fo:simple-page-master master-name=\"m\" page-width=\"200pt\" page-height=\"400pt\" margin=\"0pt\">"
				+ "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
				+ "<fo:page-sequence master-reference=\"m\"><fo:flow flow-name=\"xsl-region-body\">"
				+ "<fo:block font-family=\"Courier\" font-size=\"12pt\" line-height=\"14pt\" text-align=\"" + align + "\">" + text + "</fo:block>"
				+ "</fo:flow></fo:page-sequence></fo:root>";
	}

	/** Lines as FOP's area tree has them, words joined by single spaces. */
	private static List<String> lines(String fo, boolean greedy) throws Exception {
		FopFactoryBuilder b = new FopFactoryBuilder(new File(".").toURI());
		if (greedy) b.setLayoutManagerMakerOverride(new WordLayoutManagerMaker());
		FopFactory factory = b.build();
		FOUserAgent ua = factory.newFOUserAgent();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Fop fop = factory.newFop(MimeConstants.MIME_FOP_AREA_TREE, ua, out);
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.transform(new StreamSource(new ByteArrayInputStream(fo.getBytes("UTF-8"))), new SAXResult(fop.getDefaultHandler()));
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
		List<String> lines = new ArrayList<>();
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

	/** Greedy expectation for a monospace font: a line holds at most 27 characters. */
	private static List<String> greedyExpectation(String text) {
		List<String> out = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		for (String w : text.split(" ")) {
			String candidate = line.length() == 0 ? w : line + " " + w;
			if (candidate.length() * CHAR <= LINE) {
				line = new StringBuilder(candidate);
			} else {
				out.add(line.toString());
				line = new StringBuilder(w);
			}
		}
		out.add(line.toString());
		return out;
	}

	private static String words(int... lengths) {
		StringBuilder sb = new StringBuilder();
		char c = 'a';
		for (int len : lengths) {
			if (sb.length() > 0) sb.append(' ');
			for (int i = 0; i < len; i++) sb.append(c);
			c = (char) (c == 'z' ? 'a' : c + 1);
		}
		return sb.toString();
	}

	@Test
	public void raggedTextBreaksFirstFit() throws Exception {
		String text = words(4, 7, 3, 9, 2, 6, 5, 8, 4, 3, 7, 2, 9, 5, 11, 2, 4, 6, 3, 8, 2, 5, 7, 4, 9, 3, 6, 2, 8, 5);
		List<String> got = lines(fo(text, "start"), true);
		assertEquals(greedyExpectation(text), got);
	}

	@Test
	public void justifiedTextPullsAWordInByShrinkingSpacesWithinTheLimit() throws Exception {
		// 24 characters + space + 3-character word = 28 characters = 201.6pt: 1.6pt over,
		// four spaces of 7.2pt can give 6.9pt at the default 24% limit -> the word fits
		String fits = words(5, 5, 5, 6) + " abc " + words(9, 9, 9);
		List<String> got = lines(fo(fits, "justify"), true);
		assertEquals("word pulled in by compressing the spaces", 28, got.get(0).length());

		// 24 characters + space + 7-character word = 32 characters = 230.4pt: 30.4pt over,
		// far beyond what four spaces can give -> the word goes to the next line
		String noFit = words(5, 5, 5, 6) + " abcdefg " + words(9, 9, 9);
		got = lines(fo(noFit, "justify"), true);
		assertEquals(24, got.get(0).length());
	}

	/**
	 * Word only compresses the spaces of a justified line from its 2013 layout engine
	 * (w:compatSetting compatibilityMode 15); measured over 190 Word goldens, no line
	 * of a mode 11, 12 or 14 document has spaces below their natural width.  docx4j
	 * writes docx4j:space-shrink="0" on fo:root for such a document, and the word that
	 * only fitted by compression then goes to the next line, as Word puts it.
	 */
	@Test
	public void aLegacyCompatDocumentDoesNotCompressSpacesToPullAWordIn() throws Exception {
		String fits = words(5, 5, 5, 6) + " abc " + words(9, 9, 9);
		assertEquals("without the attribute, the word is pulled in",
				28, lines(fo(fits, "justify"), true).get(0).length());
		assertEquals("space-shrink=0: the word goes to the next line",
				24, lines(fo(fits, "justify", "0"), true).get(0).length());
	}

	@Test
	public void defaultFopStillWorksWithoutTheOverride() throws Exception {
		String text = words(4, 7, 3, 9, 2, 6, 5, 8, 4, 3, 7, 2, 9, 5);
		List<String> got = lines(fo(text, "start"), false);
		assertTrue(got.size() >= 2);
	}
}
