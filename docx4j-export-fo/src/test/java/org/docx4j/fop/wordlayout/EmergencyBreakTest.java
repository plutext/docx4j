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
 * Word's emergency break: a word too long for a line of its own is broken inside it, at
 * the last character that fits, with no hyphen.
 *
 * <p>UAX #14, which FOP follows, offers no break inside such a word, so FOP paints the
 * whole of it however narrow the measure - which is why 1959 lines are painted outside
 * their page in 73 of the corpora's documents, against Word's 207 in 20.
 *
 * <p>Measured on a corpus golden (an insurance template of long placeholder tokens in a
 * 279pt cell): Word gives such a word a line of its own - the line before
 * {@code tartóval):ELSEENDIFIF_csak_az_uzembentartoval_THEN(megegyezik} ends 85pt short
 * of the measure rather than taking the word's head - and then breaks it wherever the
 * measure falls, mid-token: {@code ...THEN(m} then {@code egegyezik...}.
 *
 * <p>Courier 12pt on a 200pt line is 27 characters (7.2pt each).
 *
 * @since 17.1.0
 */
public class EmergencyBreakTest {

	private static String fo(String text) {
		return "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">"
				+ "<fo:layout-master-set><fo:simple-page-master master-name=\"m\" page-width=\"200pt\" page-height=\"400pt\" margin=\"0pt\">"
				+ "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
				+ "<fo:page-sequence master-reference=\"m\"><fo:flow flow-name=\"xsl-region-body\">"
				+ "<fo:block font-family=\"Courier\" font-size=\"12pt\" line-height=\"14pt\">" + text + "</fo:block>"
				+ "</fo:flow></fo:page-sequence></fo:root>";
	}

	private static List<String> lines(String fo, boolean wordLayout) throws Exception {
		FopFactoryBuilder b = new FopFactoryBuilder(new File(".").toURI());
		if (wordLayout) b.setLayoutManagerMakerOverride(new WordLayoutManagerMaker());
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

	/** 54 characters, twice the 27 a line holds, and no break opportunity in it. */
	private static final String TOKEN = "KONS_ADATOK_SZERZODO_ADATAI_TERM_SZEMELY_LAKCIM_VAROS,";

	@Test
	public void anOverlongWordIsBrokenAtTheMeasure() throws Exception {
		List<String> got = lines(fo(TOKEN), true);
		assertEquals("two lines", 2, got.size());
		assertEquals(27, got.get(0).length());
		assertEquals(TOKEN, got.get(0) + got.get(1));
		// no hyphen is drawn: Word breaks such a word, it does not hyphenate it
		assertEquals(TOKEN.substring(0, 27), got.get(0));
	}

	/** Word gives the word a line of its own first: the line before it is left short
	 *  rather than filled with the word's head. */
	@Test
	public void theWordFirstMovesToALineOfItsOwn() throws Exception {
		List<String> got = lines(fo("abc " + TOKEN), true);
		assertEquals("abc", got.get(0));
		assertEquals(TOKEN.substring(0, 27), got.get(1));
		assertEquals(TOKEN.substring(27), got.get(2));
	}

	/** A word which fits on a line of its own is never broken inside: it moves to the
	 *  next line whole, as it always did. */
	@Test
	public void aWordThatFitsOnALineIsNotBroken() throws Exception {
		// 20 characters: too long for what is left of line 1, but not for a line
		String word = "abcdefghijklmnopqrst";
		List<String> got = lines(fo("0123456789 " + word), true);
		assertEquals(2, got.size());
		assertEquals("0123456789", got.get(0));
		assertEquals(word, got.get(1));
	}

	/** FOP itself paints the whole word, outside the page. */
	@Test
	public void fopItselfDoesNotBreakIt() throws Exception {
		List<String> got = lines(fo(TOKEN), false);
		assertEquals(1, got.size());
		assertEquals(TOKEN, got.get(0));
	}

	/** ... and so do we with the rule turned off. */
	@Test
	public void theRuleCanBeTurnedOff() throws Exception {
		System.setProperty(WordLayoutCustomizer.EMERGENCY_BREAK, "false");
		try {
			List<String> got = lines(fo(TOKEN), true);
			assertEquals(1, got.size());
			assertEquals(TOKEN, got.get(0));
		} finally {
			System.clearProperty(WordLayoutCustomizer.EMERGENCY_BREAK);
		}
	}

	/** A word three lines long wraps three times. */
	@Test
	public void aVeryLongWordWrapsRepeatedly() throws Exception {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 70; i++) sb.append('_');
		List<String> got = lines(fo(sb.toString()), true);
		assertTrue("three lines, not " + got.size(), got.size() == 3);
		StringBuilder joined = new StringBuilder();
		for (String l : got) joined.append(l);
		assertEquals(sb.toString(), joined.toString());
	}
}
