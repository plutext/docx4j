package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * <code>w:tblPrEx</code> states table properties which apply to its own <code>w:tr</code>
 * instead of the table's (ECMA-376 17.4.61), and the merge is <b>per child</b>: an
 * exception naming only <code>w:bottom</code> leaves the table's top, left and right in
 * force for that row.  docx4j dropped <code>w:tblPrEx</code> on the floor -
 * <code>AbstractTableWriter.createCellProperties(List, CTTblPrEx)</code> was an empty
 * method - so every row took the table's own margins and borders.
 *
 * <p>Measured (CR-001 batch 43, M4) on a corpus document whose tables declare
 * <code>w:tblCellMar</code> top=28 left=0 bottom=113 right=0 and whose 1839 data rows each
 * carry <code>w:tblPrEx/w:tblCellMar/w:bottom w:w="28"</code>: Word's row pitch is 4.25pt
 * (113 - 28 twips) shorter than ours on every one of them.  That the difference is the
 * bottom margin alone, and not 5.65pt, is what says the merge is per child rather than a
 * wholesale replacement.</p>
 *
 * @since 17.1.1
 */
public class TablePrExRowTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";

	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	/** the table's own cell margins: the shape the corpus document declares */
	private static final String TBL_CELL_MAR =
			"<w:tblCellMar>"
			+ "<w:top w:w=\"28\" w:type=\"dxa\"/><w:left w:w=\"0\" w:type=\"dxa\"/>"
			+ "<w:bottom w:w=\"113\" w:type=\"dxa\"/><w:right w:w=\"0\" w:type=\"dxa\"/>"
			+ "</w:tblCellMar>";

	/** the row's exception: the bottom margin only */
	private static final String PR_EX_CELL_MAR =
			"<w:tblPrEx><w:tblCellMar><w:bottom w:w=\"28\" w:type=\"dxa\"/></w:tblCellMar></w:tblPrEx>";

	private static final String TBL_BORDERS =
			"<w:tblBorders>"
			+ "<w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"0000FF\"/>"
			+ "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"0000FF\"/>"
			+ "<w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"0000FF\"/>"
			+ "<w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"0000FF\"/>"
			+ "<w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"0000FF\"/>"
			+ "<w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"0000FF\"/>"
			+ "</w:tblBorders>";

	/** the row's exception: the left edge and the rule between the columns only */
	private static final String PR_EX_BORDERS =
			"<w:tblPrEx><w:tblBorders>"
			+ "<w:left w:val=\"single\" w:sz=\"18\" w:space=\"0\" w:color=\"FF0000\"/>"
			+ "<w:insideV w:val=\"single\" w:sz=\"18\" w:space=\"0\" w:color=\"FF0000\"/>"
			+ "</w:tblBorders></w:tblPrEx>";

	private static String cell(String text) {
		return "<w:tc><w:tcPr><w:tcW w:w=\"2000\" w:type=\"dxa\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p></w:tc>";
	}

	private static String row(String prEx, String a, String b) {
		return "<w:tr>" + prEx + cell(a) + cell(b) + "</w:tr>";
	}

	private static WordprocessingMLPackage pkg(String tblPr, String... rows) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("<w:document ").append(W).append("><w:body><w:tbl>")
				.append("<w:tblPr><w:tblW w:w=\"4000\" w:type=\"dxa\"/>").append(tblPr).append("</w:tblPr>")
				.append("<w:tblGrid><w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/></w:tblGrid>");
		for (String r : rows) sb.append(r);
		sb.append("</w:tbl><w:p/></w:body></w:document>");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(sb.toString()));
		return pkg;
	}

	private static List<Element> cells(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder()
				.parse(new ByteArrayInputStream(baos.toByteArray()));
		List<Element> found = new ArrayList<Element>();
		NodeList nl = doc.getElementsByTagNameNS(FO_NS, "table-cell");
		for (int i = 0; i < nl.getLength(); i++) found.add((Element) nl.item(i));
		return found;
	}

	// ------------------------------------------------------------ cell margins

	private void checkCellMargins(int flags) throws Exception {
		List<Element> cells = cells(pkg(TBL_CELL_MAR,
				row("", "a", "b"),
				row(PR_EX_CELL_MAR, "c", "d")), flags);
		assertEquals("2x2 table", 4, cells.size());

		// row 1 takes the table's own margins: 113tw = 5.65pt, 28tw = 1.4pt
		assertPoints("row 1 bottom is the table's 113tw", 5.65, cells.get(0), "padding-bottom");
		assertPoints("row 1 cell 2 agrees", 5.65, cells.get(1), "padding-bottom");
		assertPoints("row 1 top is the table's 28tw", 1.4, cells.get(0), "padding-top");

		// row 2's exception replaces the bottom margin, and only the bottom margin
		assertPoints("row 2 bottom is the exception's 28tw", 1.4, cells.get(2), "padding-bottom");
		assertPoints("row 2 cell 2 agrees", 1.4, cells.get(3), "padding-bottom");
		assertPoints("row 2 top is still the table's 28tw", 1.4, cells.get(2), "padding-top");
	}

	/** The attribute in points, whatever unit the writer chose to express it in. */
	private static void assertPoints(String message, double expectedPt, Element cell, String attribute) {
		String v = cell.getAttribute(attribute);
		double pt;
		if (v.endsWith("pt")) {
			pt = Double.parseDouble(v.substring(0, v.length() - 2));
		} else if (v.endsWith("mm")) {
			pt = Double.parseDouble(v.substring(0, v.length() - 2)) * 72d / 25.4d;
		} else if (v.endsWith("in")) {
			pt = Double.parseDouble(v.substring(0, v.length() - 2)) * 72d;
		} else {
			throw new IllegalArgumentException(message + ": unexpected " + attribute + "=" + v);
		}
		// the writer rounds to two decimals in whatever unit it picks
		assertEquals(message + " (" + attribute + "=" + v + ")", expectedPt, pt, 0.03);
	}

	@Test
	public void cellMarginsVisitorPathway() throws Exception {
		checkCellMargins(Docx4J.FLAG_NONE);
	}

	@Test
	public void cellMarginsXsltPathway() throws Exception {
		checkCellMargins(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	// ---------------------------------------------------------------- borders

	private void checkBorders(int flags) throws Exception {
		List<Element> cells = cells(pkg(TBL_BORDERS,
				row("", "a", "b"),
				row(PR_EX_BORDERS, "c", "d"),
				row("", "e", "f")), flags);
		assertEquals("3x2 table", 6, cells.size());

		Element r1c1 = cells.get(0);
		Element r2c1 = cells.get(2), r2c2 = cells.get(3);
		Element r3c1 = cells.get(4);

		// rows with no exception keep the table's blue borders
		assertBorder("row 1 left", r1c1, "left", "#0000FF");
		assertBorder("row 1 right (insideV)", r1c1, "right", "#0000FF");
		assertBorder("row 3 left", r3c1, "left", "#0000FF");

		// the exception's two sides stand in for the table's, for this row only
		assertBorder("row 2 left, from w:tblPrEx", r2c1, "left", "#FF0000");
		assertBorder("row 2 insideV, from w:tblPrEx", r2c1, "right", "#FF0000");
		assertBorder("row 2 insideV, from w:tblPrEx", r2c2, "left", "#FF0000");

		// and the sides it does not state are still the table's
		assertBorder("row 2 top (insideH), not stated in the exception", r2c1, "top", "#0000FF");
		assertBorder("row 2 bottom (insideH), not stated in the exception", r2c1, "bottom", "#0000FF");
		assertBorder("row 2 right edge, not stated in the exception", r2c2, "right", "#0000FF");
	}

	private static void assertBorder(String message, Element cell, String side, String colour) {
		assertEquals(message, "solid", cell.getAttribute("border-" + side + "-style"));
		assertEquals(message, colour, cell.getAttribute("border-" + side + "-color").toUpperCase());
	}

	@Test
	public void bordersVisitorPathway() throws Exception {
		checkBorders(Docx4J.FLAG_NONE);
	}

	@Test
	public void bordersXsltPathway() throws Exception {
		checkBorders(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
