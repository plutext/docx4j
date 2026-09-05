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
 * hyphenates it, taking the last hyphenation point that fits;
 * w:consecutiveHyphenLimit caps how many lines in a row may end in a hyphen; and
 * w:doNotHyphenateCaps leaves a word written in capitals whole.</p>
 *
 * <p>w:hyphenationZone - the gap Word is said to tolerate at a line end before it
 * hyphenates - is <b>not</b> applied: measured against Word 365's own PDFs for the
 * same prose at zones of 18pt and 36pt, Word broke every line identically and
 * hyphenated gaps of 16.71pt to 34.09pt inside the 36pt zone. The 17.0.5 behaviour
 * is kept behind {@link WordLayoutCustomizer#ENFORCE_HYPHENATION_ZONE}.</p>
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
		return fo(text, zone, limit, caps, hyphenate, 200, null);
	}

	private static String fo(String text, String zone, String limit, String caps,
			boolean hyphenate, int pageWidthPt, String textAlign) {
		StringBuilder root = new StringBuilder("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\"");
		root.append(" xmlns:docx4j=\"").append(WordLayoutElementMapping.URI).append('"');
		if (zone != null) root.append(" docx4j:hyphenation-zone=\"").append(zone).append('"');
		if (limit != null) root.append(" docx4j:hyphen-limit=\"").append(limit).append('"');
		if (caps != null) root.append(" docx4j:hyphenate-caps=\"").append(caps).append('"');
		root.append('>');
		return root
				+ "<fo:layout-master-set><fo:simple-page-master master-name=\"m\" page-width=\""
				+ pageWidthPt + "pt\" page-height=\"600pt\" margin=\"0pt\">"
				+ "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
				+ "<fo:page-sequence master-reference=\"m\"><fo:flow flow-name=\"xsl-region-body\">"
				+ "<fo:block font-family=\"Courier\" font-size=\"12pt\" line-height=\"14pt\""
				+ " language=\"en\" country=\"US\""
				+ (textAlign == null ? "" : " text-align=\"" + textAlign + "\"")
				+ (hyphenate ? " hyphenate=\"true\"" : "")
				+ ">" + text + "</fo:block>"
				+ "</fo:flow></fo:page-sequence></fo:root>";
	}

	/** The properties are globals; another test in this JVM may have left one set. */
	@org.junit.Before
	@org.junit.After
	public void clearProperties() {
		System.clearProperty(WordLayoutCustomizer.ENFORCE_HYPHENATION_ZONE);
		System.clearProperty(WordLayoutCustomizer.MAX_HYPHEN_SPACE_SHRINK);
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
	 * A word that does not fit is hyphenated at the last point that does
	 * ("responsi-", filling the 27-character line), whatever the zone says.
	 */
	@Test
	public void aWordThatDoesNotFitIsHyphenatedAtTheLastPointThatDoes() throws Exception {
		List<String> got = lines(fo(TEXT, "1551", null, null, true));
		assertEquals("the international responsi-", got.get(0));
		assertEquals("bilities of the administra-", got.get(1));
	}

	/**
	 * The word after "the international" leaves a 77.6pt gap (1552 twips), so a zone
	 * of 1552 or more would - if the zone were applied - leave the ragged edge and
	 * break before the word.  Measured against Word 365, it is not: the same prose
	 * breaks identically at zones of 18pt and 36pt, and Word hyphenated gaps well
	 * inside its zone.
	 */
	@Test
	public void theZoneDoesNotStopHyphenation() throws Exception {
		assertEquals(2, hyphenated(lines(fo(TEXT, "1552", null, null, true))));
		assertEquals(2, hyphenated(lines(fo(TEXT, "4000", null, null, true))));
		// the same lines a zone of 0 gives: the zone changes nothing at all
		assertEquals(lines(fo(TEXT, "0", null, null, true)),
				lines(fo(TEXT, "4000", null, null, true)));
	}

	/**
	 * An application that wants the 17.0.5 behaviour back sets
	 * docx4j.convert.out.fo.wordLayout.hyphenationZone: 77.6pt is then not greater
	 * than a 77.6pt zone, so the line is left ragged.
	 */
	@Test
	public void theZoneIsEnforcedWhenTheApplicationAsksForIt() throws Exception {
		System.setProperty(WordLayoutCustomizer.ENFORCE_HYPHENATION_ZONE, "true");
		assertEquals(0, hyphenated(lines(fo(TEXT, "1552", null, null, true))));
		assertEquals(0, hyphenated(lines(fo(TEXT, "4000", null, null, true))));
		// and a zone below the gap still hyphenates
		assertEquals("the international responsi-", lines(fo(TEXT, "1551", null, null, true)).get(0));
	}

	/** Word's default zone, 0.25 inch, and no zone attribute at all: the same lines. */
	@Test
	public void theDefaultZoneHyphenates() throws Exception {
		assertEquals(2, hyphenated(lines(fo(TEXT, "360", null, null, true))));
		// no zone attribute at all: the line manager's own default is Word's 360 twips
		assertEquals(2, hyphenated(lines(fo(TEXT, null, null, null, true))));
	}

	/**
	 * FOP's pattern lookup folds case with the pattern file's class table, which is
	 * lossy for a word in capitals: measured with fop-hyph's en patterns,
	 * APPROPRIATIONS came back AP-PRO-PRIATIONS where Word breaks it APPROPRI-ATIONS,
	 * and DEPARTMENTS gained a spurious DEPARTMEN-TS.  The word is lowercased for the
	 * lookup, so capitals break where the same word in lower case breaks - which, in
	 * this monospace font, means the same lines.
	 */
	@Test
	public void aWordInCapitalsBreaksWhereItsLowerCaseFormDoes() throws Exception {
		List<String> mixed = lines(fo(TEXT, "0", null, null, true));
		List<String> caps = lines(fo(TEXT.toUpperCase(), "0", null, null, true));
		assertTrue("the text must hyphenate at all for this to mean anything",
				hyphenated(mixed) > 0);
		List<String> expected = new ArrayList<>();
		for (String l : mixed) expected.add(l.toUpperCase());
		assertEquals(expected, caps);
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

	/** A justified 400pt line - 55 Courier characters - whose thirteen spaces make
	 *  compression worth a lot: 13 x 7.2pt = 93.6pt of space, so a fragment two
	 *  characters (14.4pt) too long costs 15.4% of them, between the 10% Word will pay
	 *  for a longer hyphenation fragment and the 24% it will pay for a whole word. */
	private static final String JUSTIFIED =
			"aaa aa aa aa aa aa aa aa aa aa aa aa aa internationalisation "
			+ "of the responsibilities of the administration was complicated";

	/**
	 * Word compresses a justified line's spaces by up to 20.5% to pull a whole word
	 * on, but pays far less to take a longer piece of a word it is hyphenating anyway:
	 * measured against the hyphenation probes' goldens it accepted fragments costing
	 * 1.2% and 6.0% of the line's spaces and rejected 13.5%, 14.5%, 22.0% and 25.6%.
	 * The default limit is 10%, against maxSpaceShrink's 24%.
	 */
	@Test
	public void aLongerHyphenationFragmentIsNotBoughtWithSpaceCompression() throws Exception {
		List<String> tight = lines(fo(JUSTIFIED, "0", null, null, true, 400, "justify"));
		System.setProperty(WordLayoutCustomizer.MAX_HYPHEN_SPACE_SHRINK, "0.24");
		List<String> loose = lines(fo(JUSTIFIED, "0", null, null, true, 400, "justify"));
		assertTrue("the first line must be hyphenated for this to mean anything",
				tight.get(0).endsWith("-") && loose.get(0).endsWith("-"));
		assertTrue("with the spaces free to compress as far as a whole word may,"
				+ " a longer fragment is taken: " + tight.get(0) + " / " + loose.get(0),
				loose.get(0).length() > tight.get(0).length());
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
