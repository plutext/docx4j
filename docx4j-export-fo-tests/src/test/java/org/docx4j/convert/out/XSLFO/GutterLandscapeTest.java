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
 * {@code w:pgMar/@w:gutter} is the binding margin: Word adds it to the left margin of a
 * <b>portrait</b> section and takes it off the text column, and does neither in a
 * <b>landscape</b> one.
 *
 * <p>Measured on the one corpus document which has both orientations and a gutter
 * ({@code w:gutter="567"} on each of its two {@code w:sectPr}).  Portrait
 * ({@code w:left="851"}): Word puts every line at x=70.9 = (851 + 567)/20.  Landscape
 * ({@code w:left="680"}): Word puts the running head at 49.7 and the table grid edge at
 * 28.55 = 34.0 less one 5.4pt cell margin, i.e. on {@code w:left} itself.  Adding the
 * gutter there put all 222 landscape pages 28.35pt right of Word's - our text ran
 * 62.4..843.2 against Word's 34.1..804.2, past the 841.9pt page edge - and narrowed the
 * writable width, so the document's 100%-wide tables came out 737pt against Word's
 * 765.35pt (their full {@code w:tblGrid}).  Nor is it moved to the top: Word's table on
 * that page begins at y=52.85, above the 70.9pt a top gutter would give.
 *
 * @since 17.0.6
 */
public class GutterLandscapeTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	private static String body(String pgSz, String pgMar) {
		return "<w:p><w:r><w:t>text</w:t></w:r></w:p>"
				+ "<w:sectPr>" + pgSz + pgMar + "</w:sectPr>";
	}

	private static org.w3c.dom.Document fo(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	private static double pt(String length) {
		if (length == null || length.length() == 0) return Double.NaN;
		if (length.endsWith("pt")) return Double.parseDouble(length.substring(0, length.length() - 2));
		if (length.endsWith("in")) return Double.parseDouble(length.substring(0, length.length() - 2)) * 72;
		if (length.endsWith("mm")) return Double.parseDouble(length.substring(0, length.length() - 2)) * 72 / 25.4;
		throw new IllegalArgumentException(length);
	}

	private static double marginLeftPt(org.w3c.dom.Document doc) {
		NodeList masters = doc.getElementsByTagNameNS(FO_NS, "simple-page-master");
		assertTrue("no simple-page-master", masters.getLength() > 0);
		return pt(((Element) masters.item(0)).getAttribute("margin-left"));
	}

	private void checkPortraitTakesTheGutter(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(body("<w:pgSz w:w=\"11906\" w:h=\"16838\"/>",
				"<w:pgMar w:top=\"1985\" w:right=\"851\" w:bottom=\"1701\" w:left=\"851\" w:gutter=\"567\"/>"), flags);
		assertEquals("851 + 567 twips", 70.9, marginLeftPt(doc), 0.05);
	}

	private void checkLandscapeTakesNoGutter(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(body(
				"<w:pgSz w:w=\"16838\" w:h=\"11906\" w:orient=\"landscape\"/>",
				"<w:pgMar w:top=\"851\" w:right=\"851\" w:bottom=\"851\" w:left=\"680\" w:gutter=\"567\"/>"), flags);
		assertEquals("w:left alone", 34.0, marginLeftPt(doc), 0.05);
	}

	/** And the writable width keeps the gutter too, so a 100%-wide table is the full
	 *  margin box: 16838 - 680 - 851 = 15307 twips = 765.35pt. */
	private void checkLandscapeWritableWidth(int flags) throws Exception {
		String table = "<w:tbl><w:tblPr><w:tblW w:type=\"pct\" w:w=\"5000\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"15307\"/></w:tblGrid>"
				+ "<w:tr><w:tc><w:tcPr><w:tcW w:type=\"pct\" w:w=\"5000\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>wide</w:t></w:r></w:p></w:tc></w:tr></w:tbl>";
		org.w3c.dom.Document doc = fo(table + "<w:sectPr>"
				+ "<w:pgSz w:w=\"16838\" w:h=\"11906\" w:orient=\"landscape\"/>"
				+ "<w:pgMar w:top=\"851\" w:right=\"851\" w:bottom=\"851\" w:left=\"680\" w:gutter=\"567\"/>"
				+ "</w:sectPr>", flags);
		NodeList tables = doc.getElementsByTagNameNS(FO_NS, "table");
		assertTrue("no fo:table", tables.getLength() > 0);
		assertEquals(765.35, pt(((Element) tables.item(0)).getAttribute("width")), 0.05);
	}

	@Test
	public void portraitTakesTheGutterVisitor() throws Exception {
		checkPortraitTakesTheGutter(Docx4J.FLAG_NONE);
	}

	@Test
	public void portraitTakesTheGutterXslt() throws Exception {
		checkPortraitTakesTheGutter(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void landscapeTakesNoGutterVisitor() throws Exception {
		checkLandscapeTakesNoGutter(Docx4J.FLAG_NONE);
	}

	@Test
	public void landscapeTakesNoGutterXslt() throws Exception {
		checkLandscapeTakesNoGutter(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void landscapeWritableWidthVisitor() throws Exception {
		checkLandscapeWritableWidth(Docx4J.FLAG_NONE);
	}

	@Test
	public void landscapeWritableWidthXslt() throws Exception {
		checkLandscapeWritableWidth(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
