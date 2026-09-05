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
 * Rows wider than the columns written for the table made FOP throw ("The column-number
 * or number of cells in the row overflows the number of fo:table-columns specified for
 * the table"), aborting the export.  Two causes: w:gridAfter/w:gridBefore lost a column
 * from the row it applies to (so the first row, which decided the column count, was
 * narrower than the rest), and a w:tblGrid with fewer w:gridCol than a row has cells -
 * which Word renders by extending the grid.
 *
 * @since 17.0.5
 */
public class TableGridOverflowTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static String cell(String text) {
		return "<w:tc><w:p><w:r><w:t>" + text + "</w:t></w:r></w:p></w:tc>";
	}

	/** the grid declares two columns, but the second row has three cells */
	private static final String SHORT_GRID =
			"<w:tbl>"
			+ "<w:tblPr><w:tblW w:w=\"9000\" w:type=\"dxa\"/><w:tblLayout w:type=\"fixed\"/></w:tblPr>"
			+ "<w:tblGrid><w:gridCol w:w=\"3000\"/><w:gridCol w:w=\"3000\"/></w:tblGrid>"
			+ "<w:tr>" + cell("A") + cell("B") + "</w:tr>"
			+ "<w:tr>" + cell("C") + cell("D") + cell("E") + "</w:tr>"
			+ "</w:tbl>";

	/** the first row ends one column short of the grid, per w:gridAfter */
	private static final String GRID_AFTER =
			"<w:tbl>"
			+ "<w:tblPr><w:tblW w:w=\"9000\" w:type=\"dxa\"/><w:tblLayout w:type=\"fixed\"/></w:tblPr>"
			+ "<w:tblGrid><w:gridCol w:w=\"3000\"/><w:gridCol w:w=\"3000\"/><w:gridCol w:w=\"3000\"/></w:tblGrid>"
			+ "<w:tr><w:trPr><w:gridAfter w:val=\"1\"/></w:trPr>" + cell("A") + cell("B") + "</w:tr>"
			+ "<w:tr>" + cell("C") + cell("D") + cell("E") + "</w:tr>"
			+ "</w:tbl>";

	/** and the same at the start of the row */
	private static final String GRID_BEFORE =
			"<w:tbl>"
			+ "<w:tblPr><w:tblW w:w=\"9000\" w:type=\"dxa\"/><w:tblLayout w:type=\"fixed\"/></w:tblPr>"
			+ "<w:tblGrid><w:gridCol w:w=\"3000\"/><w:gridCol w:w=\"3000\"/><w:gridCol w:w=\"3000\"/></w:tblGrid>"
			+ "<w:tr><w:trPr><w:gridBefore w:val=\"1\"/></w:trPr>" + cell("A") + cell("B") + "</w:tr>"
			+ "<w:tr>" + cell("C") + cell("D") + cell("E") + "</w:tr>"
			+ "</w:tbl>";

	private static WordprocessingMLPackage pkg(String tbl) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + tbl
				+ "<w:p><w:r><w:t>after</w:t></w:r></w:p></w:body></w:document>"));
		return pkg;
	}

	private static byte[] fo(String tbl, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg(tbl));
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return baos.toByteArray();
	}

	/** FOP must accept the result: too few columns is a validation error */
	private static void toPDF(String tbl, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg(tbl)); // setOpcPackage, so FOP gets a font configuration
		Docx4J.toFO(foSettings, new ByteArrayOutputStream(), flags);
	}

	private static int columns(org.w3c.dom.Document doc) {
		return doc.getElementsByTagNameNS(FO, "table-column").getLength();
	}

	/** cells (real and placeholder) in row rowIndex */
	private static int cellsInRow(org.w3c.dom.Document doc, int rowIndex) {
		Element row = (Element)doc.getElementsByTagNameNS(FO, "table-row").item(rowIndex);
		int n = 0;
		NodeList children = row.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element
					&& "table-cell".equals(((Element)children.item(i)).getLocalName())) n++;
		}
		return n;
	}

	private void check(int flags) throws Exception {

		org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(fo(SHORT_GRID, flags));
		assertEquals("the grid follows the widest row", 3, columns(doc));
		Element third = (Element)doc.getElementsByTagNameNS(FO, "table-column").item(2);
		assertEquals("the added column takes what is left of the table's width",
				"150pt", third.getAttribute("column-width"));
		assertTrue(new String(fo(SHORT_GRID, flags), "UTF-8").contains("E"));
		toPDF(SHORT_GRID, flags);

		doc = w3cDomDocumentFromByteArray(fo(GRID_AFTER, flags));
		assertEquals(3, columns(doc));
		assertEquals("w:gridAfter of 1 means one placeholder cell", 3, cellsInRow(doc, 0));
		toPDF(GRID_AFTER, flags);

		doc = w3cDomDocumentFromByteArray(fo(GRID_BEFORE, flags));
		assertEquals(3, columns(doc));
		assertEquals("w:gridBefore of 1 means one placeholder cell", 3, cellsInRow(doc, 0));
		toPDF(GRID_BEFORE, flags);
	}

	@Test
	public void visitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
