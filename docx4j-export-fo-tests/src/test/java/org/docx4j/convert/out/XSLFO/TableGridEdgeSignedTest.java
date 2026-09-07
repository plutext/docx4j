package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Below compatibility mode 15 the table's grid edge sits one left cell margin back from
 * the text margin plus {@code w:tblInd}, <b>whatever the sign of the indent and whether
 * or not the table has one</b> (&#xa7;6.1).
 *
 * <p>Measured on the {@code table-grid-edge-signed-compat12} golden, a 72pt text margin
 * and Word's default 108-twip cell margins: Word's first cell text is at 66.5pt for
 * {@code w:tblInd -108}, 54.0pt for {@code -360} and 72.0pt for no {@code w:tblInd} at
 * all - margin + tblInd exactly - so the grid edge is margin + tblInd - 5.4pt.  17.1.0
 * briefly capped the shift at {@code max(0, w:tblInd)}, which put those at 72.3 / 59.7 /
 * 77.7.</p>
 *
 * @since 17.1.0
 */
public class TableGridEdgeSignedTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	/** A one-cell table at the given w:tblInd (null for none), grid-sized. */
	private static WordprocessingMLPackage pkg(Integer tblIndTwips, int compatibilityMode) throws Exception {

		String tblInd = tblIndTwips == null ? ""
				: "<w:tblInd w:w=\"" + tblIndTwips + "\" w:type=\"dxa\"/>";

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		DocumentSettingsPart settings = pkg.getMainDocumentPart().getDocumentSettingsPart(true);
		settings.getJaxbElement().setCompat((org.docx4j.wml.CTCompat) XmlUtils.unmarshalString(
				"<w:compat " + W + "><w:compatSetting w:name=\"compatibilityMode\""
				+ " w:uri=\"http://schemas.microsoft.com/office/word\""
				+ " w:val=\"" + compatibilityMode + "\"/></w:compat>",
				org.docx4j.jaxb.Context.jc, org.docx4j.wml.CTCompat.class));

		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:tbl><w:tblPr><w:tblW w:w=\"4000\" w:type=\"dxa\"/>" + tblInd
				+ "<w:tblLayout w:type=\"fixed\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"4000\"/></w:tblGrid>"
				+ "<w:tr><w:tc><w:tcPr><w:tcW w:w=\"4000\" w:type=\"dxa\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>cell</w:t></w:r></w:p></w:tc></w:tr></w:tbl>"
				+ "<w:p><w:r><w:t>after</w:t></w:r></w:p>"
				+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/>"
				+ "</w:sectPr></w:body></w:document>"));
		return pkg;
	}

	private static double startIndentPt(WordprocessingMLPackage pkg, int flags) throws Exception {

		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		org.w3c.dom.Document fo = XmlUtils.getNewDocumentBuilder()
				.parse(new ByteArrayInputStream(baos.toByteArray()));

		NodeList tables = fo.getElementsByTagNameNS(FO, "table");
		assertNotNull("no fo:table", tables.item(0));
		String v = ((Element) tables.item(0)).getAttribute("start-indent");
		if (v.endsWith("pt")) return Double.parseDouble(v.substring(0, v.length() - 2));
		if (v.endsWith("in")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72;
		if (v.endsWith("mm")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72 / 25.4;
		return Double.parseDouble(v);
	}

	/** The grid edge is tblInd - one cell margin, for every sign of tblInd. */
	private void gridEdge(int flags) throws Exception {
		assertEquals("no w:tblInd", -5.4, startIndentPt(pkg(null, 12), flags), 0.05);
		assertEquals("w:tblInd 0", -5.4, startIndentPt(pkg(Integer.valueOf(0), 12), flags), 0.05);
		assertEquals("w:tblInd -108", -10.8, startIndentPt(pkg(Integer.valueOf(-108), 12), flags), 0.05);
		assertEquals("w:tblInd -360", -23.4, startIndentPt(pkg(Integer.valueOf(-360), 12), flags), 0.05);
		assertEquals("w:tblInd 108", 0.0, startIndentPt(pkg(Integer.valueOf(108), 12), flags), 0.05);
	}

	@Test
	public void compat12Visitor() throws Exception {
		gridEdge(Docx4J.FLAG_NONE);
	}

	@Test
	public void compat12Xslt() throws Exception {
		gridEdge(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** Mode 15 takes no shift at all: the grid edge is the indent itself. */
	@Test
	public void compat15TakesNoShift() throws Exception {
		assertEquals(0.0, startIndentPt(pkg(null, 15), Docx4J.FLAG_NONE), 0.05);
		assertEquals(-5.4, startIndentPt(pkg(Integer.valueOf(-108), 15), Docx4J.FLAG_NONE), 0.05);
		assertEquals(0.0, startIndentPt(pkg(null, 15), Docx4J.FLAG_EXPORT_PREFER_XSL), 0.05);
	}
}
