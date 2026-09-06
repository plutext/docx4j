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
 * A justified line holding a tab: Word justifies the text after the last tab.
 *
 * <p>17.0.6 first made every line holding a tab start-aligned when the paragraph is
 * {@code w:jc="both"}, on the {@code tab-jc} probe, whose tab is the last thing on the
 * line - there the tab absorbs the slack and Word does lay the line out from the start.
 * Where text <em>follows</em> the tab, Word justifies it to the right indent as it would
 * on any other line: measured against Word 365 on a numbered clause reading
 * {@code 1.1.&lt;tab&gt;Nastoyashchiy dogovor ...} in a justified paragraph, the words
 * and the tab position are docx4j's exactly (both start the text at the same x) and Word
 * stretches the spaces to x1=560.1 where the start-aligned line stopped at 530.8 -
 * 29.3pt of stretch lost, on every such line of the document.  784 blocks in 83
 * documents of three corpora carry both a tab and {@code w:jc="both"}.</p>
 *
 * <p>The stretch is FOP's, applied to every stretchable area of the line, so it is kept
 * only where nothing stretchable sits <em>before</em> the last tab - which is the
 * ordinary shape, a label or a number and then the tab.  Otherwise the tab's settled
 * width would no longer put the text on its stop.</p>
 *
 * @since 17.0.6
 */
public class JustifiedTabLineTest {

	private static final String NS = WordLayoutElementMapping.URI;

	private static final String TAB = "<fo:leader docx4j:tab=\"1\" leader-length=\"0pt\" leader-pattern=\"space\"/>";

	/** 12pt Courier is 7.2pt per glyph; the region is 400pt wide. */
	private static String fo(String textAlign, String content) {
		return "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" xmlns:docx4j=\"" + NS + "\">"
				+ "<fo:layout-master-set><fo:simple-page-master master-name=\"m\" page-width=\"400pt\""
				+ " page-height=\"400pt\" margin=\"0pt\">"
				+ "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
				+ "<fo:page-sequence master-reference=\"m\"><fo:flow flow-name=\"xsl-region-body\">"
				+ "<fo:block font-family=\"Courier\" font-size=\"12pt\" line-height=\"14pt\""
				+ " text-align=\"" + textAlign + "\""
				+ " docx4j:tabs=\"\" docx4j:tab-default=\"720\" docx4j:tab-ind=\"0:0:.\">"
				+ content + "</fo:block>"
				+ "</fo:flow></fo:page-sequence></fo:root>";
	}

	private static Document area(String fo) throws Exception {
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
		return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
	}

	/** Where each line's last glyph ends, in points from the region edge. */
	private static List<Double> lineEnds(String fo) throws Exception {
		List<Double> ends = new ArrayList<Double>();
		NodeList las = area(fo).getElementsByTagName("lineArea");
		for (int i = 0; i < las.getLength(); i++) {
			Element line = (Element) las.item(i);
			double[] x = { ipd(line, "start-indent") };
			double[] end = { x[0] };
			walk(line, x, end);
			ends.add(Math.round(end[0] * 10) / 10.0);
		}
		return ends;
	}

	private static void walk(Element el, double[] x, double[] end) {
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (!(n instanceof Element)) continue;
			Element c = (Element) n;
			String name = c.getLocalName();
			if ("text".equals(name)) {
				x[0] += ipd(c, "ipd");
				end[0] = x[0];
			} else if ("space".equals(name) || "leader".equals(name)) {
				x[0] += ipd(c, "ipd");
			} else {
				walk(c, x, end);
			}
		}
	}

	private static double ipd(Element el, String name) {
		String v = el.getAttribute(name);
		return v == null || v.length() == 0 ? 0 : Integer.parseInt(v) / 1000.0;
	}

	/** "1.1." then a tab then enough words to fill more than one line. */
	private static final String CLAUSE =
			"1.1." + TAB + "aaaa bbbb cccc dddd eeee ffff gggg hhhh iiii jjjj kkkk llll mmmm";

	@Test
	public void aJustifiedLineWithATabIsStretched() throws Exception {
		List<Double> ends = lineEnds(fo("justify", CLAUSE));
		assertTrue("expected the clause to wrap: " + ends, ends.size() > 1);
		assertEquals("the first line of a justified paragraph holding a tab was not stretched"
				+ " to the measure: " + ends, 400.0, ends.get(0).doubleValue(), 0.5);
	}

	/** The last line of a justified paragraph is not stretched, as ever. */
	@Test
	public void theLastLineIsNotStretched() throws Exception {
		List<Double> ends = lineEnds(fo("justify", CLAUSE));
		assertTrue("the last line was stretched to the measure: " + ends,
				ends.get(ends.size() - 1).doubleValue() < 399.0);
	}

	/** A tab with nothing after it absorbs the slack: Word lays such a line out from the
	 *  start, which is what the tab-jc probe measured. */
	@Test
	public void aTrailingTabAbsorbsTheSlack() throws Exception {
		// one short line whose only tab is at its end
		List<Double> ends = lineEnds(fo("justify", "aaaa bbbb" + TAB));
		assertEquals("expected one line: " + ends, 1, ends.size());
		assertTrue("a line whose tab is its last content was stretched to the measure: " + ends,
				ends.get(0).doubleValue() < 399.0);
	}

	/** A start-aligned line holding a tab is unchanged. */
	@Test
	public void aStartAlignedLineIsNotStretched() throws Exception {
		List<Double> ends = lineEnds(fo("start", CLAUSE));
		assertTrue("a start-aligned line was stretched: " + ends,
				ends.get(0).doubleValue() < 399.0);
	}
}
