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
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * <code>w:tblBorders</code>' <code>top</code>, <code>bottom</code>, <code>left</code> and
 * <code>right</code> describe the table's <b>outer</b> edge; <code>insideH</code> and
 * <code>insideV</code> are the rules <b>between</b> cells (ECMA-376 17.4.39).  docx4j
 * applied the outer definition to every cell whenever an inside one existed, which is
 * right only where the two agree.
 *
 * <p>Measured (CR-001 &#xa7;6.5) on a table whose outer borders are <code>nil</code> and
 * whose <code>insideV</code> is <code>single sz="18" color="FFFFFF"</code>: Word draws a
 * white 2.25pt rule between the columns (<code>fill_path x=186.1..188.2</code>) and every
 * one of our cells came out <code>border-*-style="none"</code>.</p>
 *
 * @since 17.1.0
 */
public class TableBordersInsideOutsideTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";

	private static final String TBL_BORDERS =
			"<w:tblBorders>"
			+ "<w:top w:val=\"nil\"/><w:bottom w:val=\"nil\"/>"
			+ "<w:left w:val=\"nil\"/><w:right w:val=\"nil\"/>"
			+ "<w:insideH w:val=\"single\" w:sz=\"18\" w:space=\"0\" w:color=\"FF0000\"/>"
			+ "<w:insideV w:val=\"single\" w:sz=\"18\" w:space=\"0\" w:color=\"FF0000\"/>"
			+ "</w:tblBorders>";

	private static String cell(String text) {
		return "<w:tc><w:tcPr><w:tcW w:w=\"2000\" w:type=\"dxa\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p></w:tc>";
	}

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body><w:tbl>"
				+ "<w:tblPr><w:tblW w:w=\"4000\" w:type=\"dxa\"/>" + TBL_BORDERS + "</w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/></w:tblGrid>"
				+ "<w:tr>" + cell("a") + cell("b") + "</w:tr>"
				+ "<w:tr>" + cell("c") + cell("d") + "</w:tr>"
				+ "</w:tbl><w:p/></w:body></w:document>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg());
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	/** the four cells, in document order */
	private static List<Element> cells(org.w3c.dom.Document doc) {
		List<Element> found = new ArrayList<Element>();
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "table-cell");
		for (int i = 0; i < nl.getLength(); i++) found.add((Element) nl.item(i));
		return found;
	}

	private static String style(Element cell, String side) {
		return cell.getAttribute("border-" + side + "-style");
	}

	private void check(int flags) throws Exception {
		List<Element> cells = cells(fo(flags));
		assertEquals("2x2 table", 4, cells.size());

		Element topLeft = cells.get(0), topRight = cells.get(1);
		Element bottomLeft = cells.get(2), bottomRight = cells.get(3);

		// the table's own edges are nil
		assertOuter("top of row 1", topLeft, "top");
		assertOuter("top of row 1", topRight, "top");
		assertOuter("bottom of row 2", bottomLeft, "bottom");
		assertOuter("left of column 1", topLeft, "left");
		assertOuter("right of column 2", topRight, "right");

		// and the rules between the cells are drawn
		assertInside("the insideV rule between the columns", topLeft, "right");
		assertInside("the insideV rule between the columns", topRight, "left");
		assertInside("the insideH rule between the rows", topLeft, "bottom");
		assertInside("the insideH rule between the rows", bottomRight, "top");
	}

	private static void assertOuter(String message, Element cell, String side) {
		String s = style(cell, side);
		assertTrue(message + ": expected no border, got " + s,
				s.length() == 0 || "none".equals(s) || "hidden".equals(s));
	}

	private static void assertInside(String message, Element cell, String side) {
		assertEquals(message, "solid", style(cell, side));
		assertEquals(message, "#FF0000", cell.getAttribute("border-" + side + "-color").toUpperCase());
	}

	@Test
	public void visitorPathway() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xsltPathway() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
