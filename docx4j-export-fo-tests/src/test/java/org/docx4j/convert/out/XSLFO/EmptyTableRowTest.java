package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A w:tr with nothing of its own to write - every cell continues a vertical merge, or
 * the row has no w:tc at all - used to become an empty fo:table-row, which FOP rejects
 * ("fo:table-row" is missing child elements. Required content model: (table-cell+)),
 * aborting the export.  Word draws such a row as part of the merged cell, contributing
 * its height, so the row is dropped, the merge shortened, and the height carried up.
 *
 * @since 17.0.5
 */
public class EmptyTableRowTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	/** two rows, the second entirely a continuation of the first's vertical merge */
	private static final String VMERGED =
			"<w:tbl>"
			+ "<w:tblPr><w:tblW w:w=\"9000\" w:type=\"dxa\"/><w:tblLayout w:type=\"fixed\"/></w:tblPr>"
			+ "<w:tblGrid><w:gridCol w:w=\"4500\"/><w:gridCol w:w=\"4500\"/></w:tblGrid>"
			+ "<w:tr><w:trPr><w:trHeight w:val=\"300\"/></w:trPr>"
			+ "  <w:tc><w:tcPr><w:vMerge w:val=\"restart\"/></w:tcPr><w:p><w:r><w:t>merged left</w:t></w:r></w:p></w:tc>"
			+ "  <w:tc><w:tcPr><w:vMerge w:val=\"restart\"/></w:tcPr><w:p><w:r><w:t>merged right</w:t></w:r></w:p></w:tc>"
			+ "</w:tr>"
			+ "<w:tr><w:trPr><w:trHeight w:val=\"300\"/></w:trPr>"
			+ "  <w:tc><w:tcPr><w:vMerge/></w:tcPr><w:p/></w:tc>"
			+ "  <w:tc><w:tcPr><w:vMerge/></w:tcPr><w:p/></w:tc>"
			+ "</w:tr>"
			+ "</w:tbl>";

	/** a row with no w:tc at all, before a row which has content */
	private static final String NO_CELLS =
			"<w:tbl>"
			+ "<w:tblPr><w:tblW w:w=\"9000\" w:type=\"dxa\"/><w:tblLayout w:type=\"fixed\"/></w:tblPr>"
			+ "<w:tblGrid><w:gridCol w:w=\"4500\"/><w:gridCol w:w=\"4500\"/></w:tblGrid>"
			+ "<w:tr/>"
			+ "<w:tr>"
			+ "  <w:tc><w:p><w:r><w:t>cell A</w:t></w:r></w:p></w:tc>"
			+ "  <w:tc><w:p><w:r><w:t>cell B</w:t></w:r></w:p></w:tc>"
			+ "</w:tr>"
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

	/** FOP must accept it: an empty fo:table-row is a validation error */
	private static void toPDF(String tbl, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg(tbl)); // setOpcPackage, so FOP gets a font configuration
		Docx4J.toFO(foSettings, new ByteArrayOutputStream(), flags);
	}

	private static List<Element> rows(org.w3c.dom.Document doc) {
		List<Element> rows = new ArrayList<Element>();
		NodeList nl = doc.getElementsByTagNameNS(FO, "table-row");
		for (int i = 0; i < nl.getLength(); i++) rows.add((Element)nl.item(i));
		return rows;
	}

	private static int cellCount(Element row) {
		int n = 0;
		NodeList children = row.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element
					&& "table-cell".equals(((Element)children.item(i)).getLocalName())) n++;
		}
		return n;
	}

	private void check(int flags) throws Exception {

		org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(fo(VMERGED, flags));
		List<Element> rows = rows(doc);
		assertEquals("the row which is entirely merged away is dropped", 1, rows.size());
		for (Element row : rows) {
			assertTrue("every fo:table-row needs at least one fo:table-cell", cellCount(row) > 0);
		}
		assertEquals("it keeps the dropped row's height (300 + 300 twips)",
				"30pt", rows.get(0).getAttribute("height"));
		NodeList cells = doc.getElementsByTagNameNS(FO, "table-cell");
		for (int i = 0; i < cells.getLength(); i++) {
			assertEquals("with the row gone, the merge spans one row only",
					"", ((Element)cells.item(i)).getAttribute("number-rows-spanned"));
		}
		toPDF(VMERGED, flags);

		doc = w3cDomDocumentFromByteArray(fo(NO_CELLS, flags));
		rows = rows(doc);
		assertEquals("the cell-less w:tr is dropped", 1, rows.size());
		assertEquals(2, cellCount(rows.get(0)));
		assertEquals("and the row which follows it keeps its own content",
				"cell A", rows.get(0).getElementsByTagNameNS(FO, "table-cell").item(0).getTextContent().trim());
		assertTrue(rows.get(0).getTextContent().contains("cell B"));
		toPDF(NO_CELLS, flags);
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
