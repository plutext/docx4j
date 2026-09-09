package org.docx4j.fop.wordlayout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
 * A word split across two runs at a break opportunity - {@code foo-} in one
 * {@code fo:inline}, {@code bar} in the next - cannot break at the seam in FOP, whose
 * {@code LineBreakStatus} starts afresh with each text; Word breaks after the hyphen
 * whichever run it is in (word-layout-rules.md §4.3, §10).  The line manager adds the
 * opportunity.  Courier 12pt on a 200pt line: 27 characters.
 */
public class SeamBreakTest {

	private static String fo(String inlines) {
		return "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">"
				+ "<fo:layout-master-set><fo:simple-page-master master-name=\"m\" page-width=\"200pt\" page-height=\"400pt\" margin=\"0pt\">"
				+ "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
				+ "<fo:page-sequence master-reference=\"m\"><fo:flow flow-name=\"xsl-region-body\">"
				+ "<fo:block font-family=\"Courier\" font-size=\"12pt\" line-height=\"14pt\">" + inlines + "</fo:block>"
				+ "</fo:flow></fo:page-sequence></fo:root>";
	}

	private static String runs(String a, String b) {
		return "<fo:inline>" + a + "</fo:inline><fo:inline>" + b + "</fo:inline>";
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

	// "aaaa bbbb cccc dddd eeee-" is 25 characters and fits the 27-character line;
	// "ffff" after it does not.  With no opportunity at the seam "eeee-ffff" is one unit
	// and the first line ends after "dddd".

	@Test
	public void breaksAfterAHyphenEndingARun() throws Exception {
		List<String> got = lines(fo(runs("aaaa bbbb cccc dddd eeee-", "ffff gggg")), true);
		assertEquals(got.toString(), "aaaa bbbb cccc dddd eeee-", got.get(0));
		assertEquals(got.toString(), "ffff gggg", got.get(1));
	}

	@Test
	public void fopItselfCannotBreakAtTheSeam() throws Exception {
		List<String> got = lines(fo(runs("aaaa bbbb cccc dddd eeee-", "ffff gggg")), false);
		assertEquals("aaaa bbbb cccc dddd", got.get(0));
		assertTrue(got.get(1), got.get(1).startsWith("eeee-ffff"));
	}

	@Test
	public void oneRunBreaksThereInFop() throws Exception {
		List<String> got = lines(fo("<fo:inline>aaaa bbbb cccc dddd eeee-ffff gggg</fo:inline>"), false);
		assertEquals("aaaa bbbb cccc dddd eeee-", got.get(0));
	}

	/** UAX #14 LB25: no break between a hyphen and a digit, in one text or across a seam. */
	@Test
	public void noBreakBeforeADigit() throws Exception {
		List<String> got = lines(fo(runs("aaaa bbbb cccc dddd eeee-", "1234 gggg")), true);
		assertEquals("aaaa bbbb cccc dddd", got.get(0));
	}

	/** Word does not break after a solidus (§4.3), so a seam after one is no opportunity. */
	@Test
	public void noBreakAfterASolidusAtTheSeam() throws Exception {
		List<String> got = lines(fo(runs("aaaa bbbb cccc dddd eeee/", "ffff gggg")), true);
		assertEquals("aaaa bbbb cccc dddd", got.get(0));
	}

	/** A seam inside a word with no opportunity at it is still no opportunity. */
	@Test
	public void noBreakInsideAWord() throws Exception {
		List<String> got = lines(fo(runs("aaaa bbbb cccc dddd eeee", "ffff gggg")), true);
		assertEquals("aaaa bbbb cccc dddd", got.get(0));
		assertTrue(got.get(1), got.get(1).startsWith("eeeeffff"));
	}

	/** A seam after any character but a hyphen or dash is left as FOP has it, whatever
	 *  UAX #14 says of the pair: measured, Word keeps {@code $${{...}}} whole.  (FOP's own
	 *  table breaks between the letter and the first {@code $} inside the first run - that
	 *  is not the seam - so what is asserted is that no line ends at the seam.) */
	@Test
	public void onlyAHyphenOrDashMakesASeamAnOpportunity() throws Exception {
		List<String> got = lines(fo(runs("aaaa bbbb cccc dddd eeee$", "$fff gggg")), true);
		for (String l : got) assertFalse(got.toString(), l.endsWith("eeee$"));
		assertTrue(got.toString(), got.get(1).startsWith("$$fff"));
		// an en dash is a dash
		got = lines(fo(runs("aaaa bbbb cccc dddd eeee\u2013", "ffff gggg")), true);
		assertEquals(got.toString(), "aaaa bbbb cccc dddd eeee\u2013", got.get(0));
	}

	/** A space at the seam is FOP's own break, and is left alone. */
	@Test
	public void spaceAtTheSeamIsUnchanged() throws Exception {
		List<String> got = lines(fo(runs("aaaa bbbb cccc dddd eeee- ", "ffff gggg")), true);
		assertEquals("aaaa bbbb cccc dddd eeee-", got.get(0));
		assertEquals("ffff gggg", got.get(1));
	}
}
