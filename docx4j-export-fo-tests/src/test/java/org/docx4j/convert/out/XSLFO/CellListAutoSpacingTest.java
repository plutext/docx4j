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
 * HTML auto spacing on a list item inside a table cell: Word keeps the 14pt after the
 * item at the foot of the cell, and between two items, where a plain paragraph's auto
 * spacing is dropped at the cell's edges (&#xa7;3.5).
 *
 * <p>Measured on the {@code spacing-autospacing-cell-list} probe: the row after a cell
 * ending in an auto-spaced list item begins 14.6-14.8pt below the item's last line, two
 * such items sit 14.3pt apart, and the row after a plain auto-spaced paragraph follows
 * at 1.2pt.  A corpus document of about a hundred such rows was 13.3pt a row short of
 * Word and a page short of its five.</p>
 *
 * @since 17.1.1
 */
public class CellListAutoSpacingTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final String AUTO = "<w:spacing w:before=\"100\" w:beforeAutospacing=\"1\" w:after=\"100\" w:afterAutospacing=\"1\"/>";
	private static final String NUM = "<w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr>";

	private static String cell(String... paragraphs) {
		StringBuilder sb = new StringBuilder("<w:tc><w:tcPr><w:tcW w:w=\"4000\" w:type=\"dxa\"/></w:tcPr>");
		for (String p : paragraphs) sb.append(p);
		return sb.append("</w:tc>").toString();
	}

	private static String listItem(String text) {
		return "<w:p><w:pPr>" + NUM + AUTO + "</w:pPr><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static String plain(String text) {
		return "<w:p><w:pPr>" + AUTO + "</w:pPr><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private org.w3c.dom.Document fo(String cellContent, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:tbl><w:tblPr><w:tblW w:w=\"8000\" w:type=\"dxa\"/><w:tblLayout w:type=\"fixed\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"4000\"/><w:gridCol w:w=\"4000\"/></w:tblGrid>"
				+ "<w:tr>" + cell("<w:p><w:r><w:t>label</w:t></w:r></w:p>") + cellContent + "</w:tr></w:tbl>"
				+ "<w:p><w:r><w:t>after</w:t></w:r></w:p>"
				+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart ndp =
				new org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart();
		ndp.unmarshalDefaultNumbering();
		pkg.getMainDocumentPart().addTargetPart(ndp);
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	/** The fo:list-block elements inside the second cell, in order. */
	private static java.util.List<Element> listBlocks(org.w3c.dom.Document doc) {
		NodeList cells = doc.getElementsByTagNameNS(FO, "table-cell");
		Element second = (Element) cells.item(1);
		NodeList lbs = second.getElementsByTagNameNS(FO, "list-block");
		java.util.List<Element> out = new java.util.ArrayList<Element>();
		for (int i = 0; i < lbs.getLength(); i++) out.add((Element) lbs.item(i));
		return out;
	}

	private static double pt(String v) {
		return v == null || v.length() == 0 ? 0 : Double.parseDouble(v.replaceAll("[a-z]+$", ""));
	}

	/** One auto-spaced list item: its 14pt after is kept at the foot of the cell. */
	@Test
	public void aListItemKeepsItsAutoSpacingAfterAtTheCellFoot() throws Exception {
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			Element lb = listBlocks(fo(cell(listItem("item")), flags)).get(0);
			assertEquals("space-after of the item", 14.0, pt(lb.getAttribute("space-after")), 0.01);
			assertEquals("kept at the end of the cell", "retain", lb.getAttribute("space-after.conditionality"));
		}
	}

	/** Two auto-spaced list items: the 14pt between them stands (dropped only outside a cell). */
	@Test
	public void twoListItemsKeepTheirAutoSpacingBetween() throws Exception {
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			java.util.List<Element> lbs = listBlocks(fo(cell(listItem("one"), listItem("two")), flags));
			assertEquals(2, lbs.size());
			assertTrue("something between the items",
					pt(lbs.get(0).getAttribute("space-after")) >= 14.0 || pt(lbs.get(1).getAttribute("space-before")) >= 14.0);
		}
	}

	/** A plain auto-spaced paragraph: nothing at the cell's foot, as before. */
	@Test
	public void aPlainParagraphDropsItsAutoSpacingAtTheCellFoot() throws Exception {
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			org.w3c.dom.Document doc = fo(cell(plain("plain")), flags);
			Element second = (Element) doc.getElementsByTagNameNS(FO, "table-cell").item(1);
			Element block = (Element) second.getElementsByTagNameNS(FO, "block").item(0);
			assertEquals("space-after of the plain paragraph", 0.0, pt(block.getAttribute("space-after")), 0.01);
		}
	}
}
