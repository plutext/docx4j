package org.docx4j.fop.wordlayout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

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
import org.w3c.dom.NodeList;

/**
 * Word gives a space the same character spacing as any other character - one
 * {@code w:spacing w:val} after it - so a space in a letter-spaced run advances the
 * font's space plus val/20 pt.  FOP's {@code SpaceVal.makeWordSpacing} adds the letter
 * space to the word space <em>twice</em> (its own TODO says so), so every such space was
 * measured one letter space too wide and lines broke early.
 *
 * <p>Measured against Word 365 (CR-001 &#xa7;4.6): Times New Roman 11pt, a run of one space
 * carrying {@code w:spacing w:val="19"} (0.95pt).  The natural space is 2.75pt; Word's
 * advance is 3.87pt and docx4j's was 4.65 = 2.75 + 2 x 0.95.</p>
 */
public class SpaceLetterSpacingWidthTest {

	private static final int SPACES = 4;

	/** One line: five words separated by four spaces, the spaces in their own inline. */
	private static String fo(String spaceLetterSpacing) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < SPACES + 1; i++) {
			if (i > 0) {
				b.append("<fo:inline letter-spacing=\"").append(spaceLetterSpacing).append("\"> </fo:inline>");
			}
			b.append("<fo:inline>abcde</fo:inline>");
		}
		return "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">"
				+ "<fo:layout-master-set><fo:simple-page-master master-name=\"m\" page-width=\"595pt\" page-height=\"842pt\" margin=\"20pt\">"
				+ "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
				+ "<fo:page-sequence master-reference=\"m\"><fo:flow flow-name=\"xsl-region-body\">"
				+ "<fo:block font-family=\"Courier\" font-size=\"12pt\" line-height=\"14pt\""
				+ " white-space-collapse=\"false\">" + b + "</fo:block>"
				+ "</fo:flow></fo:page-sequence></fo:root>";
	}

	/** the total width of the line's text areas, in millipoints */
	private static int textIpd(String fo, boolean wordLayout) throws Exception {
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
		NodeList las = doc.getElementsByTagName("lineArea");
		assertEquals("one line expected", 1, las.getLength());
		NodeList texts = doc.getElementsByTagName("text");
		int total = 0;
		for (int i = 0; i < texts.getLength(); i++) {
			String ipd = ((Element) texts.item(i)).getAttribute("ipd");
			if (ipd.length() > 0) total += Integer.parseInt(ipd.trim());
		}
		return total;
	}

	@Test
	public void aSpaceGetsOneLetterSpace() throws Exception {
		int plain = textIpd(fo("0pt"), true);
		int spaced = textIpd(fo("1pt"), true);
		assertEquals("one 1pt letter space per space, not two",
				SPACES * 1000, spaced - plain);
	}

	@Test
	public void fopItselfCountsItTwice() throws Exception {
		// documents the FOP 2.11 behaviour the fix exists for: when this fails, FOP has
		// fixed SpaceVal.makeWordSpacing and fixSpaceLetterSpaces can go
		int plain = textIpd(fo("0pt"), false);
		int spaced = textIpd(fo("1pt"), false);
		assertTrue("FOP measured " + (spaced - plain) + " for " + SPACES + " spaces",
				spaced - plain >= SPACES * 2000);
	}
}
