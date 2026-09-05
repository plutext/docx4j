package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A table nested in a {@code w:tc} (CR-001 &#xa7;6.1, &#xa7;6.7).
 *
 * <p><b>Its grid edge.</b> Below compatibility mode 15 the grid edge of a
 * <em>top-level</em> table sits one left cell margin back from margin + w:tblInd, so the
 * first column's text lands on margin + w:tblInd.  Word does not do that to a nested
 * table: its grid edge is the containing cell's <em>content</em> edge, and its own cell
 * margin is added on top.  Measured on a mode-14 header (page margin 28.35pt, outer
 * w:tblInd 108, cell margin 108), Word's clip for the nested table runs from 33.9 = 28.35
 * + 5.4 and its text lands at 39.1; docx4j drew it at 34.0, one cell margin left, on
 * every cell of every nested table (45 of them in 11 corpus documents).</p>
 *
 * <p><b>And the mandatory paragraph after it.</b> OOXML requires a {@code w:p} after a
 * {@code w:tbl} inside a {@code w:tc}; Word gives that paragraph no line at all.
 * Measured on the same header, whose outer row 1 holds three nested rows at a 10.2pt
 * pitch: Word's next outer row starts 10.8pt later, where docx4j left room for an 11.5pt
 * line as well - which turned Word's two pages into four.</p>
 *
 * @since 17.0.6
 */
public class NestedTableTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	/** A4 portrait, left margin 567tw = 28.35pt. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"567\" w:bottom=\"1440\" w:left=\"567\"/></w:sectPr>";

	private static final String NESTED =
			"<w:tbl><w:tblPr><w:tblW w:w=\"3000\" w:type=\"dxa\"/>"
			+ "<w:tblLayout w:type=\"fixed\"/></w:tblPr>"
			+ "<w:tblGrid><w:gridCol w:w=\"1500\"/><w:gridCol w:w=\"1500\"/></w:tblGrid>"
			+ "<w:tr><w:tc><w:p><w:r><w:t>nested a</w:t></w:r></w:p></w:tc>"
			+ "<w:tc><w:p><w:r><w:t>nested b</w:t></w:r></w:p></w:tc></w:tr></w:tbl>";

	/** the mandatory empty paragraph OOXML requires after a nested table in a cell */
	private static final String MANDATORY_P = "<w:p/>";

	private static String outerTable(String cellContent) {
		return "<w:tbl><w:tblPr><w:tblW w:w=\"9000\" w:type=\"dxa\"/>"
				+ "<w:tblInd w:w=\"108\" w:type=\"dxa\"/><w:tblLayout w:type=\"fixed\"/>"
				+ "<w:tblCellMar><w:left w:w=\"108\" w:type=\"dxa\"/>"
				+ "<w:right w:w=\"108\" w:type=\"dxa\"/></w:tblCellMar></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"4500\"/><w:gridCol w:w=\"4500\"/></w:tblGrid>"
				+ "<w:tr><w:tc>" + cellContent + "</w:tc>"
				+ "<w:tc><w:p><w:r><w:t>outer b</w:t></w:r></w:p></w:tc></w:tr></w:tbl>";
	}

	private static WordprocessingMLPackage pkg(int compatibilityMode, String cellContent) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + outerTable(cellContent)
				+ "<w:p/>" + SECT_PR + "</w:body></w:document>"));
		if (compatibilityMode > 0) setCompatibilityMode(pkg, compatibilityMode);
		return pkg;
	}

	private static void setCompatibilityMode(WordprocessingMLPackage pkg, int mode) throws Exception {
		org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart settings =
				new org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart();
		settings.setJaxbElement((org.docx4j.wml.CTSettings)XmlUtils.unmarshalString(
				"<w:settings " + W + "><w:compat>"
				+ "<w:compatSetting w:name=\"compatibilityMode\""
				+ " w:uri=\"http://schemas.microsoft.com/office/word\" w:val=\"" + mode + "\"/>"
				+ "</w:compat></w:settings>", org.docx4j.jaxb.Context.jc,
				org.docx4j.wml.CTSettings.class));
		pkg.getMainDocumentPart().addTargetPart(settings);
	}

	private static org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private static double lengthPt(String v) {
		if (v.length() == 0) return 0;
		if (v.endsWith("pt")) return Double.parseDouble(v.substring(0, v.length() - 2));
		if (v.endsWith("in")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72;
		if (v.endsWith("mm")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72 / 25.4;
		return Double.parseDouble(v);
	}

	// ---------------------------------------------------------------- the grid edge

	private void theNestedTableKeepsItsCellMargin(int flags) throws Exception {
		NodeList tables = fo(pkg(14, NESTED + MANDATORY_P), flags).getElementsByTagNameNS(FO, "table");
		assertEquals("outer and nested", 2, tables.getLength());
		// the outer table takes the mode-14 shift: 108tw - 108tw = 0
		assertEquals("the top-level table's grid edge", 0,
				lengthPt(((Element) tables.item(0)).getAttribute("start-indent")), 0.05);
		// the nested one does not: the cell's own padding already puts it on the content edge
		assertEquals("a nested table must not take the mode-14 grid edge shift", 0,
				lengthPt(((Element) tables.item(1)).getAttribute("start-indent")), 0.05);
	}

	@Test
	public void theNestedTableKeepsItsCellMarginVisitor() throws Exception {
		theNestedTableKeepsItsCellMargin(Docx4J.FLAG_NONE);
	}

	@Test
	public void theNestedTableKeepsItsCellMarginXslt() throws Exception {
		theNestedTableKeepsItsCellMargin(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	// ---------------------------------------------------------------- the mandatory paragraph

	private void theMandatoryParagraphTakesNoLine(int flags) throws Exception {
		Element cell = firstCell(fo(pkg(14, NESTED + MANDATORY_P), flags));
		assertEquals("the cell holds the nested table and nothing else", 1, childElements(cell));
	}

	@Test
	public void theMandatoryParagraphTakesNoLineVisitor() throws Exception {
		theMandatoryParagraphTakesNoLine(Docx4J.FLAG_NONE);
	}

	@Test
	public void theMandatoryParagraphTakesNoLineXslt() throws Exception {
		theMandatoryParagraphTakesNoLine(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** But only that one: an empty paragraph elsewhere in the cell keeps its line. */
	private void anEmptyParagraphBeforeTheTableKeepsItsLine(int flags) throws Exception {
		Element cell = firstCell(fo(pkg(14, "<w:p/>" + NESTED + MANDATORY_P), flags));
		assertEquals("the leading empty paragraph and the nested table", 2, childElements(cell));
	}

	@Test
	public void anEmptyParagraphBeforeTheTableKeepsItsLineVisitor() throws Exception {
		anEmptyParagraphBeforeTheTableKeepsItsLine(Docx4J.FLAG_NONE);
	}

	@Test
	public void anEmptyParagraphBeforeTheTableKeepsItsLineXslt() throws Exception {
		anEmptyParagraphBeforeTheTableKeepsItsLine(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	private static Element firstCell(org.w3c.dom.Document doc) {
		NodeList cells = doc.getElementsByTagNameNS(FO, "table-cell");
		assertTrue("no table-cell", cells.getLength() > 0);
		return (Element) cells.item(0);
	}

	private static int childElements(Element parent) {
		int n = 0;
		for (Node c = parent.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c instanceof Element) n++;
		}
		return n;
	}
}
