package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
 * Where a table's cells declare widths of their own, Word lays it out on its
 * {@code w:tblGrid} - {@code w:tcW} is a <em>preferred</em> width, and row 1's need
 * neither describe every column nor agree with the grid.
 *
 * <p>docx4j's content-based autofit pass gave a column whose cells state a
 * {@code w:tcW} exactly that width, so a table whose row 1 was stale was laid out on
 * row 1 instead.  Measured against Word on a landscape report whose table is
 * {@code w:tblW 14580 dxa} with a grid summing to the same 729pt while row 1's
 * {@code w:tcW} sum to 404.4pt: Word's cell clips run 43.9..81.9 | 82.6..128.2 |
 * 128.9..182.0 | 182.4..235.5 | 236.2..327.7 | 328.4..772.3 = 728.4pt, the grid, where
 * docx4j wrote {@code width="404.4pt"} and wrapped every cell (Word's 249 lines came out
 * as 320).  45 documents of the three corpora have a row 1 whose {@code w:tcW} sum
 * differs from the grid by more than a tenth.
 *
 * <p>A wholly auto-width table is untouched, so Word's content-based autofit and the
 * widening to {@code w:tblW} still apply to it (TableWidthTest).</p>
 *
 * Both FO pathways.
 *
 * @since 17.0.6
 */
public class TableGridAuthoritativeTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** A3 landscape with 1in margins: a 15398 twip (769.9pt) text column, wide enough for
	 *  the 14580 twip grid, so no over-wide clamp is involved. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"16838\" w:h=\"11906\" w:orient=\"landscape\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"720\" w:bottom=\"1440\" w:left=\"720\"/></w:sectPr>";

	private static String cell(String width, String text) {
		return "<w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"" + width + "\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p></w:tc>";
	}

	/** The corpus shape: a 6-column table, w:tblW 14580 dxa, a w:tblGrid summing to
	 *  14580, and a row 1 whose w:tcW sum to 8088. */
	private static String staleRowOne() {
		return "<w:tbl><w:tblPr><w:tblW w:type=\"dxa\" w:w=\"14580\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"773\"/><w:gridCol w:w=\"928\"/><w:gridCol w:w=\"1072\"/>"
				+ "<w:gridCol w:w=\"1072\"/><w:gridCol w:w=\"1845\"/><w:gridCol w:w=\"8890\"/></w:tblGrid>"
				+ "<w:tr>" + cell("600", "a") + cell("720", "b") + cell("720", "c")
				+ cell("720", "d") + cell("600", "e") + cell("4728", "11:30 PM") + "</w:tr>"
				+ "<w:tr>" + cell("600", "a") + cell("720", "b") + cell("720", "c")
				+ cell("720", "d") + cell("600", "e")
				+ "<w:tc><w:p><w:r><w:t>an auto cell</w:t></w:r></w:p></w:tc>" + "</w:tr>"
				+ "</w:tbl>";
	}

	private static org.w3c.dom.Document fo(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	private static Element foTable(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "table");
		assertNotNull(nl);
		assertTrue("no fo:table", nl.getLength() > 0);
		return (Element) nl.item(0);
	}

	private static double pt(String length) {
		if (length == null || length.length() == 0) return Double.NaN;
		if (length.endsWith("pt")) return Double.parseDouble(length.substring(0, length.length() - 2));
		if (length.endsWith("in")) return Double.parseDouble(length.substring(0, length.length() - 2)) * 72;
		if (length.endsWith("mm")) return Double.parseDouble(length.substring(0, length.length() - 2)) * 72 / 25.4;
		throw new IllegalArgumentException(length);
	}

	private static double[] columnsPt(org.w3c.dom.Document doc) {
		NodeList cols = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "table-column");
		double[] out = new double[cols.getLength()];
		for (int i = 0; i < out.length; i++) {
			out[i] = pt(((Element) cols.item(i)).getAttribute("column-width"));
		}
		return out;
	}

	private void checkGridWinsOverStaleRowOne(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(staleRowOne(), flags);
		assertEquals("the grid's 729pt, not row 1's 404.4", 729.0, pt(foTable(doc).getAttribute("width")), 0.05);
		double[] cols = columnsPt(doc);
		assertEquals(6, cols.length);
		assertEquals(38.65, cols[0], 0.05);
		assertEquals(46.4, cols[1], 0.05);
		assertEquals(444.5, cols[5], 0.05);
	}

	/** A table whose cells are all auto-width still gets Word's content-based autofit,
	 *  widened to w:tblW in the columns' content proportions. */
	private void checkAutoWidthTableStillAutofits(int flags) throws Exception {
		String auto = "<w:tbl><w:tblPr><w:tblW w:type=\"dxa\" w:w=\"8000\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"4000\"/><w:gridCol w:w=\"4000\"/></w:tblGrid>"
				+ "<w:tr><w:tc><w:p><w:r><w:t>i</w:t></w:r></w:p></w:tc>"
				+ "<w:tc><w:p><w:r><w:t>wide content in the second column</w:t></w:r></w:p></w:tc></w:tr></w:tbl>";
		double[] cols = columnsPt(fo(auto, flags));
		assertEquals(400.0, cols[0] + cols[1], 0.05);
		assertTrue("content proportions, not the grid's halves: " + cols[0] + " / " + cols[1],
				cols[1] > cols[0] * 2);
	}

	@Test
	public void gridWinsOverStaleRowOneVisitor() throws Exception {
		checkGridWinsOverStaleRowOne(Docx4J.FLAG_NONE);
	}

	@Test
	public void gridWinsOverStaleRowOneXslt() throws Exception {
		checkGridWinsOverStaleRowOne(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void autoWidthTableStillAutofitsVisitor() throws Exception {
		checkAutoWidthTableStillAutofits(Docx4J.FLAG_NONE);
	}

	@Test
	public void autoWidthTableStillAutofitsXslt() throws Exception {
		checkAutoWidthTableStillAutofits(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
