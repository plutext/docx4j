package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A column Word's autofit pass sized holds its widest cell content on one line,
 * because that content is what set the width.
 *
 * <p>Measured on the {@code table-autofit-wrap} probe: Word's three columns are the
 * widest content plus the cell margins (127.4 = 116.1 + 10.8), and each of them is
 * drawn whole, the widest bleeding 0.4pt of its right cell margin.  FOP takes the
 * cell's borders off the content width as well - half of each collapsed border - so
 * exactly those lines were re-broken: all three columns of that probe wrapped, and in
 * the {@code pbdr-space} probe a cell holding 47.1pt of text in a 47.1pt column came
 * out on two lines.  The border allowance is given back as a smaller end padding,
 * which leaves the text's start (the grid edge plus half the border plus the left cell
 * margin) where it was.</p>
 *
 * <p>Only for a table docx4j sized from its content: where the w:tblGrid decides the
 * width, Word charges the border too (measured on {@code table-fixed} and
 * {@code table-cellspacing}).</p>
 *
 * <p>Both FO pathways.</p>
 *
 * @since 17.0.6
 */
public class CellLineWidthTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final String FONT =
			"<w:rPr><w:rFonts w:ascii=\"Liberation Serif\" w:hAnsi=\"Liberation Serif\"/></w:rPr>";

	/** A4 portrait with 1in margins. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String flagName(int flags) {
		return flags == Docx4J.FLAG_NONE ? "visitor" : "xslt";
	}

	/** A single-row, two-column table with 0.5pt borders all round; the first cell holds
	 *  the longest content, so autofit sizes its column to exactly that. */
	private static WordprocessingMLPackage pkg(boolean fixedLayout) throws Exception {
		String tblPr = "<w:tblPr>"
				+ (fixedLayout ? "<w:tblLayout w:type=\"fixed\"/>" : "")
				+ "<w:tblBorders>"
				+ "<w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>"
				+ "<w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>"
				+ "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>"
				+ "<w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>"
				+ "<w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>"
				+ "<w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>"
				+ "</w:tblBorders></w:tblPr>";
		String cell1 = cell("Reading comprehension");
		String cell2 = cell("Reflection");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:tbl>" + tblPr
				+ "<w:tblGrid><w:gridCol w:w=\"4513\"/><w:gridCol w:w=\"4513\"/></w:tblGrid>"
				+ "<w:tr>" + cell1 + cell2 + "</w:tr></w:tbl>"
				+ "<w:p/>" + SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	private static String cell(String text) {
		return "<w:tc><w:tcPr><w:tcW w:w=\"0\" w:type=\"auto\"/></w:tcPr>"
				+ "<w:p><w:r>" + FONT + "<w:t>" + text + "</w:t></w:r></w:p></w:tc>";
	}

	private org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		org.docx4j.convert.out.FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(org.docx4j.convert.out.FOSettings.INTERNAL_FO_MIME);
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	private static Element firstCell(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "table-cell");
		assertTrue("no cells", nl.getLength() > 0);
		return (Element) nl.item(0);
	}

	/** An FO length in points. */
	private static double pt(String v) {
		v = v.trim();
		if (v.endsWith("pt")) return Double.parseDouble(v.substring(0, v.length() - 2));
		if (v.endsWith("mm")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72 / 25.4;
		if (v.endsWith("in")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72;
		throw new IllegalArgumentException(v);
	}

	@Test
	public void theBorderAllowanceComesOffTheEndPaddingOfAContentSizedCell() throws Exception {
		for (int flags : FLAGS) {
			org.w3c.dom.Document doc = fo(pkg(false), flags);
			Element cell = firstCell(doc);
			double start = pt(cell.getAttribute("padding-left"));
			double end = pt(cell.getAttribute("padding-right"));
			// Word's default cell margin, 108 twips
			assertEquals(flagName(flags) + ": the start padding places the text and must not move",
					5.4, start, 0.05);
			// half of each 0.5pt collapsed border is charged to this cell
			// (the FO carries two decimal places)
			assertEquals(flagName(flags) + ": the end padding gives the borders back",
					start - 0.5, end, 0.01);
			assertEquals(flagName(flags) + ": the hint must not reach FOP", "",
					((Element) doc.getElementsByTagNameNS(FO, "table").item(0))
							.getAttribute("docx4j-content-sized"));
		}
	}

	@Test
	public void aGridSizedTableKeepsItsPadding() throws Exception {
		for (int flags : FLAGS) {
			Element cell = firstCell(fo(pkg(true), flags));
			assertEquals(flagName(flags) + ": w:tblLayout=fixed is the grid's layout, not autofit",
					pt(cell.getAttribute("padding-left")), pt(cell.getAttribute("padding-right")), 0.001);
		}
	}

	/**
	 * And it lays out: the content that sized the column stays on one line, where the
	 * borders used to re-break it.
	 */
	@Test
	public void theContentThatSizedTheColumnStaysOnOneLine() throws Exception {
		org.w3c.dom.Document areaTree = areaTree(pkg(false), Docx4J.FLAG_NONE);
		NodeList lines = areaTree.getElementsByTagName("lineArea");
		int holding = 0;
		for (int i = 0; i < lines.getLength(); i++) {
			String text = words((Element) lines.item(i));
			if (text.contains("Reading")) {
				holding++;
				assertEquals("the cell's content was re-broken", "Reading comprehension", text);
			}
		}
		assertEquals(1, holding);
	}

	/** the words of a line, joined by single spaces */
	private static String words(Element line) {
		StringBuilder sb = new StringBuilder();
		collect(line, sb);
		return sb.toString().trim().replaceAll(" +", " ");
	}

	private static void collect(Element el, StringBuilder sb) {
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (!(children.item(i) instanceof Element)) continue;
			Element c = (Element) children.item(i);
			if ("word".equals(c.getLocalName())) sb.append(c.getTextContent());
			else if ("space".equals(c.getLocalName())) sb.append(' ');
			else collect(c, sb);
		}
	}
}
