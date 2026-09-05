package org.docx4j.fop.wordlayout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
 * Word's automatic hyphenation, through FOP itself, on a monospace base-14 font
 * so that every width is a character count: Courier advances 0.6em per glyph, so
 * at 12pt on a 200pt line a line holds 27 characters, a hyphen included.
 *
 * <p>The rules measured against Word and implemented in
 * {@link WordLineLayoutManager}: when the next whole word does not fit, Word
 * hyphenates it only where the gap left at the end of the line is <b>greater
 * than</b> the hyphenation zone (w:hyphenationZone, {@code
 * docx4j:hyphenation-zone} here), taking the last hyphenation point that fits;
 * w:consecutiveHyphenLimit caps how many lines in a row may end in a hyphen; and
 * w:doNotHyphenateCaps leaves a word written in capitals whole.</p>
 *
 * <p>Hyphenation needs patterns, which FOP does not ship: net.sf.offo:fop-hyph
 * is a <b>test-scope</b> dependency of this module alone. It is not under the
 * Apache licence, so it is not a dependency of anything docx4j publishes; an
 * application that wants hyphenated output puts it (or its own patterns) on its
 * own classpath.</p>
 */
public class HyphenationTest {

	/** "the international" is 17 characters = 122.4pt, so the gap at the first line's
	 *  last whole word is 77.6pt = 1552 twips: the zone either side of that decides
	 *  whether the word after it is hyphenated. */
	private static final String TEXT =
			"the international responsibilities of the administration were "
			+ "extraordinarily complicated documentation and representation";

	private static String fo(String text, String zone, String limit, String caps,
			boolean hyphenate) {
		StringBuilder root = new StringBuilder("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\"");
		root.append(" xmlns:docx4j=\"").append(WordLayoutElementMapping.URI).append('"');
		if (zone != null) root.append(" docx4j:hyphenation-zone=\"").append(zone).append('"');
		if (limit != null) root.append(" docx4j:hyphen-limit=\"").append(limit).append('"');
		if (caps != null) root.append(" docx4j:hyphenate-caps=\"").append(caps).append('"');
		root.append('>');
		return root
				+ "<fo:layout-master-set><fo:simple-page-master master-name=\"m\" page-width=\"200pt\" page-height=\"600pt\" margin=\"0pt\">"
				+ "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
				+ "<fo:page-sequence master-reference=\"m\"><fo:flow flow-name=\"xsl-region-body\">"
				+ "<fo:block font-family=\"Courier\" font-size=\"12pt\" line-height=\"14pt\""
				+ " language=\"en\" country=\"US\""
				+ (hyphenate ? " hyphenate=\"true\"" : "")
				+ ">" + text + "</fo:block>"
				+ "</fo:flow></fo:page-sequence></fo:root>";
	}

	/** Lines as FOP's area tree has them, words joined by single spaces. */
	private static List<String> lines(String fo, boolean wordLayout) throws Exception {
		FopFactoryBuilder b = new FopFactoryBuilder(new File(".").toURI());
		if (wordLayout) b.setLayoutManagerMakerOverride(new WordLayoutManagerMaker());
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
		List<String> lines = new ArrayList<>();
		NodeList las = doc.getElementsByTagName("lineArea");
		for (int i = 0; i < las.getLength(); i++) {
			StringBuilder sb = new StringBuilder();
			collectWords((Element) las.item(i), sb);
			lines.add(sb.toString().trim().replaceAll(" +", " "));
		}
		return lines;
	}

	private static List<String> lines(String fo) throws Exception {
		return lines(fo, true);
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

	/** How many lines end in a hyphen, i.e. were hyphenated. */
	private static int hyphenated(List<String> lines) {
		int n = 0;
		for (String l : lines) {
			if (l.endsWith("-")) n++;
		}
		return n;
	}

	/** Without patterns on the classpath every assertion here would pass vacuously. */
	@Test
	public void patternsAreOnTheTestClasspath() {
		assertTrue("net.sf.offo:fop-hyph (test scope) must be on the test classpath",
				HyphenationTest.class.getResource("/hyph/en.hyp") != null);
	}

	/**
	 * Without hyphenate="true" the greedy breaker breaks whole words only, whatever
	 * the document's hyphenation settings say.
	 */
	@Test
	public void aBlockThatDoesNotAskForHyphenationIsNotHyphenated() throws Exception {
		List<String> got = lines(fo(TEXT, "0", null, null, false));
		assertEquals(0, hyphenated(got));
		assertEquals(Arrays.asList(
				"the international",
				"responsibilities of the",
				"administration were",
				"extraordinarily complicated",
				"documentation and",
				"representation"), got);
	}

	/**
	 * The word after "the international" leaves a 77.6pt gap (1552 twips). A zone
	 * below that is exceeded, so Word hyphenates, taking the last hyphenation point
	 * that fits ("responsi-", filling the 27-character line).
	 */
	@Test
	public void aGapLargerThanTheZoneIsHyphenated() throws Exception {
		List<String> got = lines(fo(TEXT, "1551", null, null, true));
		assertEquals("the international responsi-", got.get(0));
		assertEquals("bilities of the administra-", got.get(1));
	}

	/**
	 * The same paragraph with a zone of exactly the gap: 77.6pt is not greater than
	 * 77.6pt, so Word leaves the ragged edge and breaks before the word.
	 */
	@Test
	public void aGapNoLargerThanTheZoneIsNotHyphenated() throws Exception {
		assertEquals(0, hyphenated(lines(fo(TEXT, "1552", null, null, true))));
		assertEquals(0, hyphenated(lines(fo(TEXT, "4000", null, null, true))));
	}

	/** Word's default zone, 0.25 inch, is exceeded by most ragged line ends. */
	@Test
	public void theDefaultZoneHyphenates() throws Exception {
		assertEquals(2, hyphenated(lines(fo(TEXT, "360", null, null, true))));
		// no zone attribute at all: the line manager's own default is Word's 360 twips
		assertEquals(2, hyphenated(lines(fo(TEXT, null, null, null, true))));
	}

	/**
	 * w:consecutiveHyphenLimit: with a limit of 1, the second of two lines that
	 * would both end in a hyphen breaks at a whole word instead. 0 (and no
	 * attribute) is no limit.
	 */
	@Test
	public void theConsecutiveHyphenLimitIsEnforced() throws Exception {
		List<String> unlimited = lines(fo(TEXT, "0", null, null, true));
		assertEquals("the international responsi-", unlimited.get(0));
		assertEquals("bilities of the administra-", unlimited.get(1));

		List<String> limit1 = lines(fo(TEXT, "0", "1", null, true));
		assertEquals("the international responsi-", limit1.get(0));
		assertEquals("a hyphen is refused on the line after a hyphenated one",
				"bilities of the", limit1.get(1));
		assertEquals("administration were extra-", limit1.get(2));

		// two in a row is what this paragraph does, so a limit of 2 changes nothing
		assertEquals(unlimited, lines(fo(TEXT, "0", "2", null, true)));
	}

	/** w:doNotHyphenateCaps: a word written in capitals is left whole. */
	@Test
	public void allCapitalWordsAreSkippedWhenTheDocumentSaysSo() throws Exception {
		String caps = TEXT.toUpperCase();
		assertTrue("the same words in capitals hyphenate by default",
				hyphenated(lines(fo(caps, "0", null, null, true))) > 0);
		assertEquals("docx4j:hyphenate-caps=false leaves them whole",
				0, hyphenated(lines(fo(caps, "0", null, "false", true))));
		// and only capitals are skipped: mixed-case text still hyphenates
		assertTrue(hyphenated(lines(fo(TEXT, "0", null, "false", true))) > 0);
	}

	/**
	 * With the Word layout managers off (docx4j.convert.out.fo.wordLayout=false)
	 * FOP's own total-fit breaker hyphenates as it always has, and knows nothing of
	 * the zone.
	 */
	@Test
	public void plainFopHyphenationIsUnchanged() throws Exception {
		assertTrue(hyphenated(lines(fo(TEXT, null, null, null, true), false)) > 0);
		assertEquals(0, hyphenated(lines(fo(TEXT, null, null, null, false), false)));
	}
}
