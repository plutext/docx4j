package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.UnitsOfMeasurement;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Two tables docx4j scaled into the text column and Word does not (CR-001 batch 43, the
 * E4/E5 class, narrowed to these two conditions by ledger4).
 *
 * <p><b>A floating table.</b> A table with a <code>w:tblpPr</code> is out of the flow, so
 * the text column does not bound it. Measured on a corpus document whose grid is
 * 1862+1755+2830+2570 = 450.85pt on a 439.85pt column: docx4j scaled every column by
 * 0.9756 where Word paints the frame out to x=543.85, 21.85pt past its own right margin.</p>
 *
 * <p><b>A percentage over 100.</b> A <code>w:tblW</code> in pct above 100% is not resolved
 * against the container: the declared grid is laid out at its absolute widths. Measured on a
 * corpus document's header table, <code>w:tblW 5656 pct</code> = 113.12% of a 9026-twip
 * column with a grid of 9161+1303: Word gives its second cell 65.15pt and the 52.2pt string
 * fits, where docx4j scaled by 0.9757, left a 52.0pt measure, and the string wrapped.</p>
 *
 * @since 17.1.1
 */
public class TableOverWideExemptionsTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";

	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	/** A4 portrait, 1 inch margins: a 9026-twip (451.3pt) text column. */
	private static final String SECT =
			"<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\""
			+ " w:header=\"708\" w:footer=\"708\"/></w:sectPr>";

	private static String cell(int w, String text) {
		return "<w:tc><w:tcPr><w:tcW w:w=\"" + w + "\" w:type=\"dxa\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p></w:tc>";
	}

	/** grid 6000+6000 = 12000 twips = 600pt, 1.33 x the text column - past
	 *  GRID_OVERHANG_LIMIT, so an ordinary table of this shape is scaled into the column */
	private static String table(String tblPrExtra) {
		return "<w:tbl><w:tblPr>" + tblPrExtra + "</w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"6000\"/><w:gridCol w:w=\"6000\"/></w:tblGrid>"
				+ "<w:tr>" + cell(6000, "one") + cell(6000, "two") + "</w:tr></w:tbl>";
	}

	private static List<Element> columns(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, Docx4J.FLAG_NONE);
		org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder()
				.parse(new ByteArrayInputStream(baos.toByteArray()));
		List<Element> out = new ArrayList<Element>();
		NodeList nl = doc.getElementsByTagNameNS(FO_NS, "table-column");
		for (int i = 0; i < nl.getLength(); i++) {
			Element c = (Element) nl.item(i);
			if (c.hasAttribute("column-number")) out.add(c);
		}
		return out;
	}

	private static double pt(Element column) {
		String w = column.getAttribute("column-width");
		if (w.endsWith("pt")) return Double.parseDouble(w.substring(0, w.length() - 2));
		if (w.endsWith("mm")) return Double.parseDouble(w.substring(0, w.length() - 2)) * 72d / 25.4d;
		if (w.endsWith("in")) return Double.parseDouble(w.substring(0, w.length() - 2)) * 72d;
		throw new IllegalArgumentException("column-width=" + w);
	}

	/** The control: an ordinary over-wide grid is still scaled into the column. */
	@Test
	public void anOrdinaryOverWideGridIsStillScaled() throws Exception {
		List<Element> cols = columns(table("<w:tblW w:w=\"0\" w:type=\"auto\"/>"));
		assertEquals(2, cols.size());
		double total = pt(cols.get(0)) + pt(cols.get(1));
		assertTrue("an autofit grid 1.33 x the column is scaled into it, got " + total,
				total < 460);
	}

	/** A w:tblpPr table keeps its declared grid, over-wide or not. */
	@Test
	public void aFloatingTableKeepsItsGrid() throws Exception {
		List<Element> cols = columns(table(
				"<w:tblW w:w=\"0\" w:type=\"auto\"/>"
				+ "<w:tblpPr w:leftFromText=\"180\" w:rightFromText=\"180\""
				+ " w:vertAnchor=\"text\" w:horzAnchor=\"margin\" w:tblpY=\"1\"/>"));
		assertEquals(2, cols.size());
		assertEquals("column 1 is its declared 6000 twips", 300.0, pt(cols.get(0)), 0.5);
		assertEquals("column 2 is its declared 6000 twips", 300.0, pt(cols.get(1)), 0.5);
	}

	/**
	 * A percentage over 100 leaves the grid alone; the same table at 50% is still scaled
	 * to the percentage, which is the behaviour the table-grid-pct golden measures.
	 */
	@Test
	public void aPercentageOverOneHundredKeepsTheGrid() throws Exception {
		List<Element> over = columns(table("<w:tblW w:w=\"5656\" w:type=\"pct\"/>"));
		assertEquals(2, over.size());
		assertEquals("over 100%: column 1 is its declared 6000 twips", 300.0, pt(over.get(0)), 0.5);
		assertEquals("over 100%: column 2 is its declared 6000 twips", 300.0, pt(over.get(1)), 0.5);

		List<Element> half = columns(table("<w:tblW w:w=\"2500\" w:type=\"pct\"/>"));
		assertEquals(2, half.size());
		double total = pt(half.get(0)) + pt(half.get(1));
		assertEquals("50% of the 451.3pt column, the grid scaled to it", 225.65, total, 1.0);
	}
}
