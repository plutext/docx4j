package org.docx4j.fop.wordlayout;

import static org.junit.Assert.assertEquals;

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

/**
 * List items under the Word rules: the last line's leading is discardable at
 * the foot of the page (WordListItemLayoutManager), and a label's ascent joins
 * the item's first line without the auto multiple (WordLineLayoutManager).
 *
 * Page body 235pt; each item is one Courier 12pt line with a 14pt box on an
 * 11pt baseline and line-height 24pt, so 10pt of leading below.
 */
public class WordListItemTest {

	private static final String NS = "xmlns:docx4j=\"" + WordLayoutElementMapping.URI + "\"";
	private static final String WORD = "docx4j:line-box=\"14pt\" docx4j:baseline=\"11pt\" docx4j:line-rule=\"auto\"";

	private static String fo(int items, String labelAttrs, String bodyAttrs) {
		StringBuilder sb = new StringBuilder("<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" " + NS + ">"
				+ "<fo:layout-master-set><fo:simple-page-master master-name=\"m\" page-width=\"200pt\" page-height=\"235pt\" margin=\"0pt\">"
				+ "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
				+ "<fo:page-sequence master-reference=\"m\"><fo:flow flow-name=\"xsl-region-body\">"
				+ "<fo:list-block provisional-distance-between-starts=\"20pt\" font-family=\"Courier\" font-size=\"12pt\" line-height=\"24pt\">");
		for (int i = 0; i < items; i++) {
			sb.append("<fo:list-item><fo:list-item-label end-indent=\"label-end()\"><fo:block " + labelAttrs + ">-</fo:block></fo:list-item-label>"
					+ "<fo:list-item-body start-indent=\"body-start()\"><fo:block " + bodyAttrs + ">item " + i + "</fo:block></fo:list-item-body></fo:list-item>");
		}
		return sb.append("</fo:list-block></fo:flow></fo:page-sequence></fo:root>").toString();
	}

	private static int pages(String fo, boolean word) throws Exception {
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
		Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
		return doc.getElementsByTagName("pageViewport").getLength();
	}

	@Test
	public void lastItemsLeadingIsDroppedAtThePageFoot() throws Exception {
		// ten 24pt items (240) need two pages in FOP; ten 14pt boxes with nine 10pt leadings (230) fit one
		assertEquals(2, pages(fo(10, "", ""), false));
		assertEquals(1, pages(fo(10, WORD, WORD), true));
		// and the leading between items is kept: an eleventh item (254) needs a second page
		assertEquals(2, pages(fo(11, WORD, WORD), true));
	}

	@Test
	public void labelAscentRaisesTheFirstLineUnmultiplied() throws Exception {
		// a label whose ascent is 13pt adds 2pt to each (single-line) item: 16pt boxes;
		// nine items = 9 x 16 + 8 x 10 = 224 fit, ten = 250 do not
		String label = WORD.replace("line-box=\"14pt\" docx4j:baseline=\"11pt\"", "line-box=\"16pt\" docx4j:baseline=\"13pt\"");
		String body = WORD + " docx4j:label-ascent=\"13pt\"";
		assertEquals(1, pages(fo(9, label, body), true));
		assertEquals(2, pages(fo(10, label, body), true));
	}
}
