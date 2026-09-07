package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
 * {@code w:tblPr/w:jc="center"} centres a table in the text column whether the table is
 * wider than the column or narrower.  TableWriter.applyStartIndent only centred a table
 * wider than the column, so a narrower one fell through to {@code start-indent} 0 and
 * sat at the left margin: measured on a corpus document whose centred one-column table
 * carries the label "1", Word draws it at x=174.3 and ours was at 75.7 - 99pt out.
 *
 * @since 17.1.0
 */
public class CentredTableIndentTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String flagName(int flag) {
		return flag == Docx4J.FLAG_EXPORT_PREFER_XSL ? "XSL" : "visitor";
	}

	/** A 5690-twip table on the A4 text column of 9638 twips (margins 1134 each side). */
	private static String table(String jc) {
		return "<w:tbl><w:tblPr><w:tblW w:w=\"5690\" w:type=\"dxa\"/>" + jc
				+ "<w:tblLayout w:type=\"fixed\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"1089\"/><w:gridCol w:w=\"4601\"/></w:tblGrid>"
				+ "<w:tr>"
				+ "<w:tc><w:tcPr><w:tcW w:w=\"1089\" w:type=\"dxa\"/></w:tcPr><w:p><w:r><w:t>1</w:t></w:r></w:p></w:tc>"
				+ "<w:tc><w:tcPr><w:tcW w:w=\"4601\" w:type=\"dxa\"/></w:tcPr><w:p><w:r><w:t>x</w:t></w:r></w:p></w:tc>"
				+ "</w:tr></w:tbl>";
	}

	private static final String SECT = "<w:p><w:pPr><w:sectPr>"
			+ "<w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1418\" w:right=\"1134\" w:bottom=\"1418\" w:left=\"1134\""
			+ " w:header=\"709\" w:footer=\"709\" w:gutter=\"0\"/>"
			+ "</w:sectPr></w:pPr></w:p>";

	private static Element firstTable(String jc, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + table(jc) + SECT + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder()
				.parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
		NodeList nl = doc.getElementsByTagNameNS(FO, "table");
		return nl.getLength() == 0 ? null : (Element) nl.item(0);
	}

	/** (9638 - 5690) / 2 = 1974 twips = 98.7pt. */
	@Test
	public void aNarrowCentredTableIsCentred() throws Exception {
		for (int flag : FLAGS) {
			Element table = firstTable("<w:jc w:val=\"center\"/>", flag);
			assertNotNull(flagName(flag) + ": no fo:table", table);
			assertEquals(flagName(flag), "98.7pt", table.getAttribute("start-indent"));
		}
	}

	/** Without w:jc the table starts at the margin (less the mode-below-15 grid shift). */
	@Test
	public void anUncentredTableIsNotMoved() throws Exception {
		for (int flag : FLAGS) {
			Element table = firstTable("", flag);
			assertNotNull(flagName(flag) + ": no fo:table", table);
			assertEquals(flagName(flag), "-5.4pt", table.getAttribute("start-indent"));
		}
	}
}
