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
 * Word's emergency break inside a table cell: a word wider than the cell is broken as
 * soon as it exceeds the cell, at whatever character reaches the edge, where in body
 * text ({@link EmergencyBreakTest}) it is left to overflow until it is an inch over.
 *
 * <p>Measured on a corpus document whose table grid Word wrote and kept: a 21.55pt
 * column breaks {@code Categorizador/Período} down 15 lines of one or two characters.
 *
 * <p>This is a workaround for FOP, which offers no break inside a word at all; docx4j's
 * own line manager splits the word into one glyph mapping per character, and no
 * character is added to the text.  It goes when FOP grows an emergency break of its own.
 *
 * <p>Courier 12pt is 7.2pt a character, so a 100pt cell holds 13.
 *
 * @since 17.1.1
 */
public class CellEmergencyBreakTest {

	/** A one-column table, the column 100pt wide, its cell holding {@code text}. */
	private static String cellFo(String text) {
		return cellFo(text, "12pt");
	}

	private static String cellFo(String text, String fontSize) {
		return "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">"
				+ "<fo:layout-master-set><fo:simple-page-master master-name=\"m\" page-width=\"300pt\" page-height=\"400pt\" margin=\"0pt\">"
				+ "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
				+ "<fo:page-sequence master-reference=\"m\"><fo:flow flow-name=\"xsl-region-body\">"
				+ "<fo:table table-layout=\"fixed\" width=\"100pt\"><fo:table-column column-width=\"100pt\"/>"
				+ "<fo:table-body><fo:table-row><fo:table-cell padding=\"0pt\">"
				+ "<fo:block font-family=\"Courier\" font-size=\"" + fontSize + "\" line-height=\"14pt\">" + text + "</fo:block>"
				+ "</fo:table-cell></fo:table-row></fo:table-body></fo:table>"
				+ "</fo:flow></fo:page-sequence></fo:root>";
	}

	/** The same 100pt measure, but body text: a 100pt page. */
	private static String bodyFo(String text) {
		return "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">"
				+ "<fo:layout-master-set><fo:simple-page-master master-name=\"m\" page-width=\"100pt\" page-height=\"400pt\" margin=\"0pt\">"
				+ "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
				+ "<fo:page-sequence master-reference=\"m\"><fo:flow flow-name=\"xsl-region-body\">"
				+ "<fo:block font-family=\"Courier\" font-size=\"12pt\" line-height=\"14pt\">" + text + "</fo:block>"
				+ "</fo:flow></fo:page-sequence></fo:root>";
	}

	/** A 100pt cell whose text is set in a rotated block-container (a w:textDirection
	 *  cell), the container's own measure also 100pt. */
	private static String rotatedCellFo(String text) {
		return "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">"
				+ "<fo:layout-master-set><fo:simple-page-master master-name=\"m\" page-width=\"300pt\" page-height=\"400pt\" margin=\"0pt\">"
				+ "<fo:region-body/></fo:simple-page-master></fo:layout-master-set>"
				+ "<fo:page-sequence master-reference=\"m\"><fo:flow flow-name=\"xsl-region-body\">"
				+ "<fo:table table-layout=\"fixed\" width=\"100pt\"><fo:table-column column-width=\"100pt\"/>"
				+ "<fo:table-body><fo:table-row><fo:table-cell padding=\"0pt\">"
				+ "<fo:block-container reference-orientation=\"90\" inline-progression-dimension=\"100pt\" block-progression-dimension=\"100pt\">"
				+ "<fo:block font-family=\"Courier\" font-size=\"12pt\" line-height=\"14pt\">" + text + "</fo:block>"
				+ "</fo:block-container>"
				+ "</fo:table-cell></fo:table-row></fo:table-body></fo:table>"
				+ "</fo:flow></fo:page-sequence></fo:root>";
	}

	/** Lines as FOP's area tree has them, words joined by single spaces. */
	private static List<String> lines(String fo) throws Exception {
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

	/** 20 characters, 144pt: 44pt wider than the cell, well inside the inch body text
	 *  tolerates. */
	private static final String WIDE = "Categorizador_Period";

	/** 13 characters, 93.6pt: fits the cell. */
	private static final String FITS = "Categorizador";

	@Test
	public void aWordThatFitsItsCellIsUntouched() throws Exception {
		List<String> got = lines(cellFo(FITS));
		assertEquals(1, got.size());
		assertEquals(FITS, got.get(0));
	}

	/** Word's rule in a cell: the word is broken where the cell's edge falls, no hyphen. */
	@Test
	public void aWordWiderThanItsCellIsBrokenAtTheEdge() throws Exception {
		List<String> got = lines(cellFo(WIDE));
		assertEquals("two lines", 2, got.size());
		assertEquals(WIDE.substring(0, 13), got.get(0));
		assertEquals(WIDE.substring(13), got.get(1));
	}

	/** Word gives the word a line of its own first, in a cell as in body text. */
	@Test
	public void theWordFirstMovesToALineOfItsOwn() throws Exception {
		List<String> got = lines(cellFo("abc " + WIDE));
		assertEquals("abc", got.get(0));
		assertEquals(WIDE.substring(0, 13), got.get(1));
		assertEquals(WIDE.substring(13), got.get(2));
	}

	/** The same word on the same measure in body text keeps the inch: 44pt over is left
	 *  to overflow, as in 17.1.0. */
	@Test
	public void bodyTextKeepsTheGeneralTolerance() throws Exception {
		List<String> got = lines(bodyFo(WIDE));
		assertEquals(1, got.size());
		assertEquals(WIDE, got.get(0));
	}

	/** A rotated cell's measure is a row height docx4j only bounds, so it keeps the inch. */
	@Test
	public void aRotatedCellKeepsTheGeneralTolerance() throws Exception {
		List<String> got = lines(rotatedCellFo(WIDE));
		assertEquals(1, got.size());
		assertEquals(WIDE, got.get(0));
	}

	/** The tolerance is a twip, not zero: the column sizer and FOP measure the same word
	 *  a hair apart, and an overrun below what Word can lay out is not one Word breaks.
	 *  14 Courier characters at 11.9pt are 99.96pt (fits) and at 11.92pt 100.13pt, 0.13pt
	 *  over the 100pt cell (broken). */
	@Test
	public void anOverrunBelowATwipIsNotABreak() throws Exception {
		String word = "Categorizador_"; // 14 characters
		List<String> under = lines(cellFo(word, "11.9pt"));
		assertEquals(1, under.size());
		assertEquals(word, under.get(0));
		List<String> over = lines(cellFo(word, "11.92pt"));
		assertEquals(2, over.size());
		assertEquals(word.substring(0, 13), over.get(0));
	}

	/** The cell tolerance raised to the inch: 17.1.0's behaviour, the word overflows. */
	@Test
	public void theCellToleranceCanBeRaisedToTheInch() throws Exception {
		System.setProperty(WordLayoutCustomizer.CELL_EMERGENCY_BREAK_TOLERANCE, "72");
		try {
			List<String> got = lines(cellFo(WIDE));
			assertEquals(1, got.size());
			assertEquals(WIDE, got.get(0));
		} finally {
			System.clearProperty(WordLayoutCustomizer.CELL_EMERGENCY_BREAK_TOLERANCE);
		}
	}

	/** ... and the emergency break as a whole turned off turns this off too. */
	@Test
	public void theRuleCanBeTurnedOff() throws Exception {
		System.setProperty(WordLayoutCustomizer.EMERGENCY_BREAK, "false");
		try {
			List<String> got = lines(cellFo(WIDE));
			assertEquals(1, got.size());
			assertEquals(WIDE, got.get(0));
		} finally {
			System.clearProperty(WordLayoutCustomizer.EMERGENCY_BREAK);
		}
	}
}
