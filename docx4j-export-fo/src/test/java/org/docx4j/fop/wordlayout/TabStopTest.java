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
 * Tabs in the middle of a line, through FOP itself, on Courier so every position
 * can be computed: 12pt Courier is 7.2pt per glyph.  A tab is an fo:leader of no
 * length carrying docx4j:tab, and the block carries the paragraph's stops
 * (docx4j:tabs, docx4j:tab-default, docx4j:tab-ind), as
 * XsltFOFunctions.tabToFO / applyTabStopHints emit them.
 */
public class TabStopTest {

	private static final double CHAR = 7.2;    // Courier 12pt advance
	private static final String NS = WordLayoutElementMapping.URI;

	private static final String TAB = "<fo:leader docx4j:tab=\"1\" leader-length=\"0pt\" leader-pattern=\"space\"/>";

	/** @param tabs docx4j:tabs, "pos:align:leader;..." in twips; "" for none
	 *  @param ind  docx4j:tab-ind, "left:firstLine:separator" in twips */
	private static String fo(String tabs, String ind, String blockAttrs, String content) {
		return "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" xmlns:docx4j=\"" + NS + "\">"
				+ "<fo:layout-master-set><fo:simple-page-master master-name=\"m\" page-width=\"400pt\" page-height=\"400pt\" margin=\"0pt\">"
				+ "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
				+ "<fo:page-sequence master-reference=\"m\"><fo:flow flow-name=\"xsl-region-body\">"
				+ "<fo:block font-family=\"Courier\" font-size=\"12pt\" line-height=\"14pt\""
				+ " docx4j:tabs=\"" + tabs + "\" docx4j:tab-default=\"720\" docx4j:tab-ind=\"" + ind + "\""
				+ (blockAttrs == null ? "" : " " + blockAttrs) + ">" + content + "</fo:block>"
				+ "</fo:flow></fo:page-sequence></fo:root>";
	}

	/** The x at which each word of each line starts, in points from the region edge. */
	private static List<Double> wordStarts(String fo) throws Exception {
		FopFactoryBuilder b = new FopFactoryBuilder(new File(".").toURI());
		b.setLayoutManagerMakerOverride(new WordLayoutManagerMaker());
		FopFactory factory = b.build();
		FOUserAgent ua = factory.newFOUserAgent();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Fop fop = factory.newFop(MimeConstants.MIME_FOP_AREA_TREE, ua, out);
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.transform(new StreamSource(new ByteArrayInputStream(fo.getBytes("UTF-8"))), new SAXResult(fop.getDefaultHandler()));
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
		List<Double> starts = new ArrayList<>();
		NodeList las = doc.getElementsByTagName("lineArea");
		for (int i = 0; i < las.getLength(); i++) {
			Element line = (Element) las.item(i);
			double[] x = { ipd(line, "start-indent") };
			walk(line, x, starts);
		}
		return starts;
	}

	private static void walk(Element el, double[] x, List<Double> starts) {
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (!(n instanceof Element)) continue;
			Element c = (Element) n;
			String name = c.getLocalName();
			if ("text".equals(name)) {
				starts.add(round(x[0]));
				x[0] += ipd(c, "ipd");
			} else if ("space".equals(name) || "leader".equals(name)) {
				x[0] += ipd(c, "ipd");
			} else {
				walk(c, x, starts);
			}
		}
	}

	private static double ipd(Element el, String attr) {
		String v = el.getAttribute(attr);
		if (v.length() == 0) return 0;
		return Double.parseDouble(v) / 1000.0;
	}

	private static double round(double v) {
		return Math.round(v * 100) / 100.0;
	}

	private static List<Double> at(double... xs) {
		List<Double> out = new ArrayList<>();
		for (double x : xs) out.add(round(x));
		return out;
	}

	@Test
	public void defaultStopsAreEvery36pt() throws Exception {
		// "abc" is 21.6pt, so the tab runs to the second default stop, 36pt
		assertEquals(at(0, 36), wordStarts(fo("", "0:0:.", null, "abc" + TAB + "x")));
		// a longer word passes 36pt: the next stop is 72pt.  A stop exactly at x is not
		// the "next" one, so 10 characters (72pt) would reach 108pt.
		assertEquals(at(0, 72), wordStarts(fo("", "0:0:.", null, "abcdefghi" + TAB + "x")));
		assertEquals(at(0, 108), wordStarts(fo("", "0:0:.", null, "abcdefghij" + TAB + "x")));
	}

	@Test
	public void twoTabsInARowGoToConsecutiveStops() throws Exception {
		assertEquals(at(0, 72), wordStarts(fo("", "0:0:.", null, "abc" + TAB + TAB + "x")));
	}

	@Test
	public void aCustomStopClearsTheDefaultStopsBeforeIt() throws Exception {
		// one left stop at 2000 twips = 100pt: the first tab reaches it although the
		// default grid would have offered 36pt, and the grid resumes beyond it
		assertEquals(at(0, 100, 108),
				wordStarts(fo("2000:left:none", "0:0:.", null, "abc" + TAB + "x" + TAB + "y")));
	}

	@Test
	public void aRightStopPutsTheEndOfTheFollowingTextOnIt() throws Exception {
		// stop at 4000 twips = 200pt, "wxyz" is 28.8pt wide -> it starts at 171.2pt
		assertEquals(at(0, 200 - 4 * CHAR),
				wordStarts(fo("4000:right:none", "0:0:.", null, "abc" + TAB + "wxyz")));
	}

	@Test
	public void aCentreStopPutsTheMiddleOfTheFollowingTextOnIt() throws Exception {
		assertEquals(at(0, 200 - 2 * CHAR),
				wordStarts(fo("4000:center:none", "0:0:.", null, "abc" + TAB + "wxyz")));
	}

	@Test
	public void aDecimalStopAlignsTheDecimalSeparator() throws Exception {
		// "12.34": the separator is the third character, so the text starts 2 chars left
		assertEquals(at(0, 200 - 2 * CHAR),
				wordStarts(fo("4000:decimal:none", "0:0:.", null, "abc" + TAB + "12.34")));
		// no separator: Word right-aligns it
		assertEquals(at(0, 200 - 5 * CHAR),
				wordStarts(fo("4000:decimal:none", "0:0:.", null, "abc" + TAB + "12345")));
	}

	@Test
	public void aRightStopTheTextCannotReachCollapsesTheTab() throws Exception {
		// the stop is at 36pt but "wxyz" ends at 21.6 + 28.8 = 50.4: Word cannot move
		// backwards, so the tab adds nothing
		assertEquals(at(0, 3 * CHAR),
				wordStarts(fo("720:right:none", "0:0:.", null, "abc" + TAB + "wxyz")));
	}

	@Test
	public void aStopPastTheRightIndentIsStillHonoured() throws Exception {
		// the only stop is at 9000 twips = 450pt, past the 400pt right edge: Word puts the
		// text on it anyway and the line runs into the indent rather than wrapping
		// (measured on a w:ind right=360 footer whose right stop is the full text width)
		assertEquals(at(0, 450),
				wordStarts(fo("9000:left:none", "0:0:.", null, "abc" + TAB + "x")));
	}

	@Test
	public void aHangingIndentMakesAStopAtTheLeftIndent() throws Exception {
		// left 1440 twips (72pt), hanging 720 (36pt): the first line starts 36pt in and
		// a tab there reaches the left indent, 72pt.  The area tree gives x within the
		// block, whose own start-indent is the 72pt left indent, so those are -36 and 0.
		assertEquals(at(-36, 0),
				wordStarts(fo("", "1440:-720:.", "start-indent=\"72pt\" text-indent=\"-36pt\"",
						"ab" + TAB + "x")));
	}
}
