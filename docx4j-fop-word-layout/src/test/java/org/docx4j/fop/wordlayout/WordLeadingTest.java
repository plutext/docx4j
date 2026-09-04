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
import org.w3c.dom.NodeList;

/**
 * Word's leading rule (CR-001 §6.6 item 14) through FOP: with docx4j:line-box /
 * docx4j:baseline on the block, each line is its text box with the extra
 * leading as glue below it, dropped at the bottom of a page.  Courier 12pt on
 * a 200pt line holds 27 characters, so line counts are exact.
 */
public class WordLeadingTest {

	private static final String NS = "xmlns:docx4j=\"" + WordLayoutElementMapping.URI + "\"";

	/** Ten lines of 27 characters each (26 letters + a space between words). */
	private static String tenLines() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			if (i > 0) sb.append(' ');
			sb.append("abcdefghijklm nopqrstuvwxyz"); // 27 chars: fills a 200pt Courier 12pt line
		}
		return sb.toString();
	}

	/** page body 235pt: ten 24pt lines (240) do not fit; ten 14pt boxes with nine 10pt leadings (230) do */
	private static String fo(String attrs, String... blocks) {
		StringBuilder sb = new StringBuilder("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" " + NS + ">"
				+ "<fo:layout-master-set><fo:simple-page-master master-name=\"m\" page-width=\"200pt\" page-height=\"235pt\" margin=\"0pt\">"
				+ "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
				+ "<fo:page-sequence master-reference=\"m\"><fo:flow flow-name=\"xsl-region-body\">");
		for (String b : blocks) {
			sb.append("<fo:block font-family=\"Courier\" font-size=\"12pt\" line-height=\"24pt\" widows=\"1\" orphans=\"1\" " + attrs + ">" + b + "</fo:block>");
		}
		return sb.append("</fo:flow></fo:page-sequence></fo:root>").toString();
	}

	private static Document areaTree(String fo, boolean word) throws Exception {
		FopFactoryBuilder b = new FopFactoryBuilder(new File(".").toURI());
		if (word) b.setLayoutManagerMakerOverride(new WordLayoutManagerMaker());
		FopFactory factory = b.build();
		FOUserAgent ua = factory.newFOUserAgent();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Fop fop = factory.newFop(MimeConstants.MIME_FOP_AREA_TREE, ua, out);
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.transform(new StreamSource(new ByteArrayInputStream(fo.getBytes("UTF-8"))), new SAXResult(fop.getDefaultHandler()));
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
	}

	/** line areas per page */
	private static List<Integer> linesPerPage(Document doc) {
		List<Integer> out = new ArrayList<>();
		NodeList pages = doc.getElementsByTagName("pageViewport");
		for (int i = 0; i < pages.getLength(); i++) {
			out.add(((Element) pages.item(i)).getElementsByTagName("lineArea").getLength());
		}
		return out;
	}

	private static final String WORD = "docx4j:line-box=\"14pt\" docx4j:baseline=\"11pt\" docx4j:line-rule=\"auto\"";

	@Test
	public void attributesAreAcceptedAndLinesAreTheTextBox() throws Exception {
		Document doc = areaTree(fo(WORD, tenLines()), true);
		Element line = (Element) doc.getElementsByTagName("lineArea").item(0);
		assertEquals("line area is Word's text box", "14000", line.getAttribute("bpd"));
	}

	@Test
	public void lastLeadingOfAPageIsDropped() throws Exception {
		// FOP's rule: 24pt lines, nine fit on 235pt
		assertEquals(9, (int) linesPerPage(areaTree(fo("", tenLines() + " " + tenLines()), false)).get(0));
		// Word's rule: the tenth line's text box fits, its leading hangs below the margin
		assertEquals(10, (int) linesPerPage(areaTree(fo(WORD, tenLines() + " " + tenLines()), true)).get(0));
	}

	@Test
	public void lastLeadingOfAParagraphIsDroppedAtAPageBreak() throws Exception {
		// a ten-line paragraph followed by another: the break after it must not count
		// the last line's leading (the flow manager moves the glue behind the break)
		List<Integer> pages = linesPerPage(areaTree(fo(WORD, tenLines(), tenLines()), true));
		assertEquals(10, (int) pages.get(0));
		assertTrue(pages.size() >= 2);
	}

	@Test
	public void leadingBetweenLinesIsKept() throws Exception {
		// on a tall enough page the lines are still 24pt apart: 10 lines need 230pt, so
		// with a 235pt page all ten are on page one and the eleventh is not
		List<Integer> pages = linesPerPage(areaTree(fo(WORD, tenLines() + " extra"), true));
		assertEquals(10, (int) pages.get(0));
		assertEquals(1, (int) pages.get(1));
	}

	@Test
	public void flowManagerMovesTrailingGlueBehindTheBreak() {
		List<org.apache.fop.layoutmgr.ListElement> list = new ArrayList<>();
		org.apache.fop.layoutmgr.KnuthBox box = new org.apache.fop.layoutmgr.KnuthBox(14000, null, false);
		LeadingGlue glue = new LeadingGlue(10000);
		org.apache.fop.layoutmgr.KnuthPenalty inf = new org.apache.fop.layoutmgr.KnuthPenalty(0, org.apache.fop.layoutmgr.KnuthElement.INFINITE, false, null, true);
		org.apache.fop.layoutmgr.KnuthPenalty brk = new org.apache.fop.layoutmgr.KnuthPenalty(0, 0, false, null, false);
		org.apache.fop.layoutmgr.KnuthBox next = new org.apache.fop.layoutmgr.KnuthBox(14000, null, false);
		list.add(box); list.add(inf); list.add(glue); list.add(brk); list.add(next);
		WordFlowLayoutManager.moveLeadingBehindBreaks(list);
		assertEquals(brk, list.get(2));
		assertEquals(glue, list.get(3));
		assertEquals(next, list.get(4));
	}
}
