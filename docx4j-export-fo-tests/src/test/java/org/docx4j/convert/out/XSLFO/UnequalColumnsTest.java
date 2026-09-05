package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A section whose w:cols declares columns of different widths (w:equalWidth="0" with
 * w:col children), as Word lays it out (CR-001, measured against a Word golden):
 *
 * <ul>
 * <li>XSL-FO's region-body columns are all the same width, so such a stretch is rendered
 *   as a one-row fo:table whose cells are the columns, split where the document says
 *   (w:br w:type="column"); measured, a certificate whose columns are 157 and 318pt with
 *   a 24pt gap had Word's second column starting at x=232.2 where equal columns put ours
 *   at 312.5;</li>
 * <li>without a column break the content has to flow from one column into the next, which
 *   a table cannot do, so the section is left as equal columns;</li>
 * <li>columns within 5% of each other are Word's own rounding of equal columns and are
 *   left to the region body too.</li>
 * </ul>
 *
 * Both FO pathways.
 */
public class UnequalColumnsTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	/** section 1: one column, 152 / 186pt margins (a 257pt text column) */
	private static final String SECT_1 = "<w:p><w:pPr><w:sectPr>"
			+ "<w:pgSz w:w=\"11900\" w:h=\"16840\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"3720\" w:bottom=\"1440\" w:left=\"3040\"/>"
			+ "<w:cols w:space=\"720\" w:equalWidth=\"0\"><w:col w:w=\"5140\"/></w:cols>"
			+ "</w:sectPr></w:pPr><w:r><w:t>Certificate of Completion</w:t></w:r></w:p>";

	/** section 2, continuous: two unequal columns, 51 / 45pt margins (a 499pt text column) */
	private static String sect2(String cols) {
		return "<w:sectPr><w:type w:val=\"continuous\"/><w:pgSz w:w=\"11900\" w:h=\"16840\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"900\" w:bottom=\"1440\" w:left=\"1020\"/>"
				+ cols + "</w:sectPr>";
	}

	private static final String UNEQUAL = "<w:cols w:num=\"2\" w:space=\"480\" w:equalWidth=\"0\">"
			+ "<w:col w:w=\"3140\" w:space=\"480\"/><w:col w:w=\"6360\"/></w:cols>";
	private static final String NEARLY_EQUAL = "<w:cols w:num=\"2\" w:space=\"480\" w:equalWidth=\"0\">"
			+ "<w:col w:w=\"4716\" w:space=\"480\"/><w:col w:w=\"4715\"/></w:cols>";

	private static final String WITH_BREAK =
			"<w:p><w:r><w:t>This certificate is presented to</w:t></w:r></w:p>"
			+ "<w:p><w:r><w:t>for successfully completing</w:t></w:r><w:r><w:br w:type=\"column\"/></w:r></w:p>"
			+ "<w:p><w:r><w:t>Series 1 Module 5 assessment</w:t></w:r></w:p>";
	private static final String NO_BREAK =
			"<w:p><w:r><w:t>This certificate is presented to</w:t></w:r></w:p>"
			+ "<w:p><w:r><w:t>for successfully completing</w:t></w:r></w:p>";

	private static org.w3c.dom.Document fo(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getDocumentSettingsPart().setWordCompatSetting("compatibilityMode", "15");
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	private static double pt(String length) {
		if (length == null || length.length() == 0) return Double.NaN;
		if (length.endsWith("pt")) return Double.parseDouble(length.substring(0, length.length() - 2));
		if (length.endsWith("in")) return Double.parseDouble(length.substring(0, length.length() - 2)) * 72;
		if (length.endsWith("mm")) return Double.parseDouble(length.substring(0, length.length() - 2)) * 72 / 25.4;
		throw new IllegalArgumentException(length);
	}

	private static int columnCount(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS(FO_NS, "region-body");
		assertTrue("no region-body", nl.getLength() > 0);
		String count = ((Element) nl.item(0)).getAttribute("column-count");
		return count.length() == 0 ? 1 : Integer.parseInt(count);
	}

	private void unequalColumnsBecomeATable(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(SECT_1 + WITH_BREAK + sect2(UNEQUAL), flags);

		// the page-sequence is single-column: the columns are the table's
		assertEquals(1, columnCount(doc));

		NodeList tables = doc.getElementsByTagNameNS(FO_NS, "table");
		assertEquals("one table, the columns", 1, tables.getLength());
		Element table = (Element) tables.item(0);
		assertEquals(499.0, pt(table.getAttribute("width")), 0.01);

		NodeList cols = doc.getElementsByTagNameNS(FO_NS, "table-column");
		assertEquals("two columns and the gap between them", 3, cols.getLength());
		assertEquals(157.0, pt(((Element) cols.item(0)).getAttribute("column-width")), 0.01);
		assertEquals(24.0, pt(((Element) cols.item(1)).getAttribute("column-width")), 0.01);
		assertEquals(318.0, pt(((Element) cols.item(2)).getAttribute("column-width")), 0.01);

		// the content is split at the column break, which itself takes no line
		NodeList cells = doc.getElementsByTagNameNS(FO_NS, "table-cell");
		assertEquals(3, cells.getLength());
		assertTrue("column 1: " + cells.item(0).getTextContent(),
				cells.item(0).getTextContent().contains("presented to")
				&& cells.item(0).getTextContent().contains("successfully"));
		assertTrue("column 2: " + cells.item(2).getTextContent(),
				cells.item(2).getTextContent().contains("Module 5"));
		assertTrue("the column break should not take a line",
				!cells.item(0).getTextContent().contains("\n"));

		// and the table has none of a table's own geometry
		NodeList cellEls = doc.getElementsByTagNameNS(FO_NS, "table-cell");
		for (int i = 0; i < cellEls.getLength(); i++) {
			assertEquals("no cell margins", 0.0,
					pt(((Element) cellEls.item(i)).getAttribute("padding-left")), 0.01);
		}
	}

	@Test public void unequalVisitor() throws Exception {
		unequalColumnsBecomeATable(Docx4J.FLAG_NONE);
	}

	@Test public void unequalXslt() throws Exception {
		unequalColumnsBecomeATable(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** No column break: Word balances the columns itself, so the content is divided by
	 *  estimated line count - measured on the columns-unequal probe, whose sixteen lines
	 *  Word divides eight and eight, breaking inside the paragraph. */
	private void withoutAColumnBreakTheColumnsAreBalanced(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(SECT_1 + NO_BREAK + sect2(UNEQUAL), flags);
		assertEquals("the columns are the table's", 1, columnCount(doc));
		NodeList cols = doc.getElementsByTagNameNS(FO_NS, "table-column");
		assertEquals(3, cols.getLength());
		assertEquals(157.0, pt(((Element) cols.item(0)).getAttribute("column-width")), 0.01);
		assertEquals(318.0, pt(((Element) cols.item(2)).getAttribute("column-width")), 0.01);
		NodeList cells = doc.getElementsByTagNameNS(FO_NS, "table-cell");
		assertEquals(3, cells.getLength());
		assertTrue("column 1 holds the start: " + cells.item(0).getTextContent(),
				cells.item(0).getTextContent().contains("certificate"));
		assertTrue("column 2 holds the rest: " + cells.item(2).getTextContent(),
				cells.item(2).getTextContent().contains("completing"));
	}

	@Test public void noBreakVisitor() throws Exception {
		withoutAColumnBreakTheColumnsAreBalanced(Docx4J.FLAG_NONE);
	}

	@Test public void noBreakXslt() throws Exception {
		withoutAColumnBreakTheColumnsAreBalanced(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** Word divides the paragraph the balance point falls in.  Here one long paragraph is
	 *  all there is, so the division is inside it. */
	private void balancingDividesTheParagraph(int flags) throws Exception {
		StringBuilder prose = new StringBuilder("<w:p><w:r><w:t>");
		for (int i = 0; i < 40; i++) prose.append("word").append(i).append(' ');
		prose.append("</w:t></w:r></w:p>");
		org.w3c.dom.Document doc = fo(SECT_1 + prose + sect2(UNEQUAL), flags);
		NodeList cells = doc.getElementsByTagNameNS(FO_NS, "table-cell");
		assertEquals(3, cells.getLength());
		String one = cells.item(0).getTextContent(), two = cells.item(2).getTextContent();
		assertTrue("column 1 opens the paragraph: " + one, one.contains("word0"));
		assertTrue("column 2 ends it: " + two, two.contains("word39"));
		assertTrue("neither column holds it all", !one.contains("word39") && !two.contains("word0"));
	}

	@Test public void balanceDividesVisitor() throws Exception {
		balancingDividesTheParagraph(Docx4J.FLAG_NONE);
	}

	@Test public void balanceDividesXslt() throws Exception {
		balancingDividesTheParagraph(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** More content than fits the page: a one-row table cannot break into columns across
	 *  a page, so the section is left to the region body, whose balancing can. */
	private void tooMuchToFitIsLeftAlone(int flags) throws Exception {
		StringBuilder prose = new StringBuilder("<w:p><w:r><w:t>");
		for (int i = 0; i < 4000; i++) prose.append("word").append(i).append(' ');
		prose.append("</w:t></w:r></w:p>");
		org.w3c.dom.Document doc = fo(SECT_1 + prose + sect2(UNEQUAL), flags);
		assertEquals(2, columnCount(doc));
		assertEquals(0, doc.getElementsByTagNameNS(FO_NS, "table").getLength());
	}

	@Test public void tooMuchVisitor() throws Exception {
		tooMuchToFitIsLeftAlone(Docx4J.FLAG_NONE);
	}

	/** The half of a divided paragraph which opens the next column takes a line of its
	 *  own even when the break ends the paragraph and nothing is left but its mark, and
	 *  the paragraph's space-after goes with it.  Measured on the columns-unequal probe:
	 *  Word starts column 2 at y=183.4, 19.4pt below column 1's 164.0 - one 13.8pt line
	 *  plus the paragraph's 6pt space-after - and the next section 13.9pt below column
	 *  one's last line, ie with no space-after there at all. */
	private void aBreakEndingAParagraphLeavesItsMark(int flags) throws Exception {
		String endBreak = "<w:p><w:pPr><w:spacing w:after=\"120\"/></w:pPr>"
				+ "<w:r><w:t>ends column one</w:t><w:br w:type=\"column\"/></w:r></w:p>"
				+ "<w:p><w:r><w:t>opens column two</w:t></w:r></w:p>";
		org.w3c.dom.Document doc = fo(SECT_1 + endBreak + sect2(UNEQUAL), flags);
		NodeList cells = doc.getElementsByTagNameNS(FO_NS, "table-cell");
		assertEquals(3, cells.getLength());

		Element one = (Element) cells.item(0), two = (Element) cells.item(2);
		NodeList blocksOne = one.getElementsByTagNameNS(FO_NS, "block");
		assertEquals("no space-after on the half that does not end the paragraph", 0.0,
				pt(((Element) blocksOne.item(0)).getAttribute("space-after")), 0.01);

		NodeList blocksTwo = two.getElementsByTagNameNS(FO_NS, "block");
		assertTrue("column 2 opens with the paragraph's mark: " + two.getTextContent(),
				blocksTwo.getLength() > 1);
		Element mark = (Element) blocksTwo.item(0);
		assertEquals("which carries the space-after", 6.0,
				pt(mark.getAttribute("space-after")), 0.01);
		assertTrue("and nothing else: " + mark.getTextContent(),
				mark.getTextContent().trim().length() == 0);
		assertTrue("the text follows it", two.getTextContent().contains("opens column two"));
	}

	@Test public void breakEndingAParagraphVisitor() throws Exception {
		aBreakEndingAParagraphLeavesItsMark(Docx4J.FLAG_NONE);
	}

	@Test public void breakEndingAParagraphXslt() throws Exception {
		aBreakEndingAParagraphLeavesItsMark(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** Word divides the paragraph the break is in: what precedes it ends the column,
	 *  what follows it opens the next (measured on a letterhead whose address block is
	 *  one paragraph with the break in the middle of it). */
	private void aMidParagraphBreakDividesTheParagraph(int flags) throws Exception {
		String midBreak = "<w:p><w:r><w:t>ends column one</w:t>"
				+ "<w:br w:type=\"column\"/><w:t>opens column two</w:t></w:r></w:p>";
		org.w3c.dom.Document doc = fo(SECT_1 + midBreak + sect2(UNEQUAL), flags);
		NodeList cells = doc.getElementsByTagNameNS(FO_NS, "table-cell");
		assertEquals(3, cells.getLength());
		assertTrue("column 1: " + cells.item(0).getTextContent(),
				cells.item(0).getTextContent().contains("ends column one"));
		assertTrue("column 1 should not hold column two's text: " + cells.item(0).getTextContent(),
				!cells.item(0).getTextContent().contains("opens column two"));
		assertTrue("column 2: " + cells.item(2).getTextContent(),
				cells.item(2).getTextContent().contains("opens column two"));
	}

	@Test public void midParagraphBreakVisitor() throws Exception {
		aMidParagraphBreakDividesTheParagraph(Docx4J.FLAG_NONE);
	}

	@Test public void midParagraphBreakXslt() throws Exception {
		aMidParagraphBreakDividesTheParagraph(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** Columns Word rounded to 4716 / 4715 are equal columns. */
	private void nearlyEqualColumnsAreLeftAlone(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(SECT_1 + WITH_BREAK + sect2(NEARLY_EQUAL), flags);
		assertEquals(2, columnCount(doc));
		assertEquals(0, doc.getElementsByTagNameNS(FO_NS, "table").getLength());
	}

	@Test public void nearlyEqualVisitor() throws Exception {
		nearlyEqualColumnsAreLeftAlone(Docx4J.FLAG_NONE);
	}

	@Test public void nearlyEqualXslt() throws Exception {
		nearlyEqualColumnsAreLeftAlone(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** FOP puts the two columns' first lines on the same baseline, at Word's x: the
	 *  golden has column 1 at 51.1 and column 2 at 232.2. */
	@Test
	public void fopPutsTheColumnsSideBySide() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getDocumentSettingsPart().setWordCompatSetting("compatibilityMode", "15");
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + SECT_1 + WITH_BREAK + sect2(UNEQUAL)
				+ "</w:body></w:document>"));
		org.w3c.dom.Document at = areaTree(pkg, Docx4J.FLAG_NONE);
		// the region body starts at the page-sequence's own 152pt margin; the cells are
		// offset from it, so column 1 lands at 152-101 = 51 and column 2 at 152+80 = 232
		NodeList blocks = at.getElementsByTagName("block");
		java.util.List<Double> cellOffsets = new java.util.ArrayList<Double>();
		for (int i = 0; i < blocks.getLength(); i++) {
			Element b = (Element) blocks.item(i);
			String offset = b.getAttribute("left-offset");
			if (offset.length() > 0 && "absolute".equals(b.getAttribute("positioning"))) {
				cellOffsets.add(Double.parseDouble(offset) / 1000);
			}
		}
		assertEquals("three cells: column 1, the gap, column 2; saw " + cellOffsets,
				3, cellOffsets.size());
		assertEquals("column 1 at x=51 (Word 51.1)", -101.0, cellOffsets.get(0), 0.5);
		assertEquals("column 2 at x=232 (Word 232.2)", 80.0, cellOffsets.get(2), 0.5);
		assertTrue("nothing was laid out", lineCount(at) >= 3);
	}
}
