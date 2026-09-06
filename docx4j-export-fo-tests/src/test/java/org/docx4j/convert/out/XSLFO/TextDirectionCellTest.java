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
 * {@code w:tcPr/w:textDirection}: a table cell whose text Word turns on its side.
 *
 * <p>The {@code fo:block-container} that carries {@code reference-orientation} is a
 * reference area, and a reference area turned 90&#xb0; has to be given both of its
 * dimensions.  Without them FOP gives the rotated area an inline-progression-dimension
 * of 0, so the text is laid out on a line of no measure, takes no width in the cell and
 * is painted past the page edge - measured on a corpus certificate whose stub column is
 * {@code btLr}: Word's first line at {@code y=53.5 x=99.9..494.8}, ours at
 * {@code y=76.1 x=323.5..672.7} on a 595.3pt page.</p>
 *
 * <p>The properties are stated in the container's own (rotated) frame: the
 * block-progression-dimension is the cell's content width, and the
 * inline-progression-dimension is how far down the page the rotated line may run - the
 * row's {@code w:trHeight} where it states one, and otherwise the cell's minimum content
 * width, the height the rotated text can always be wrapped into.</p>
 *
 * <p>Both FO pathways.</p>
 */
public class TextDirectionCellTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	/** A4 portrait, 1in margins. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	/** A two-column fixed table, the first cell rotated; 534 twips = 26.7pt wide. */
	private static String table(String trPr, String textDirection) {
		return "<w:tbl><w:tblPr><w:tblLayout w:type=\"fixed\"/>"
				+ "<w:tblW w:type=\"dxa\" w:w=\"6000\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"534\"/><w:gridCol w:w=\"5466\"/></w:tblGrid>"
				+ "<w:tr>" + (trPr == null ? "" : "<w:trPr>" + trPr + "</w:trPr>")
				+ "<w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"534\"/>"
				+ "<w:textDirection w:val=\"" + textDirection + "\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>Teil I</w:t></w:r></w:p></w:tc>"
				+ "<w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"5466\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>Angaben zur Sendung</w:t></w:r></w:p></w:tc>"
				+ "</w:tr></w:tbl>";
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

	private static Element rotated(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "block-container");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (el.hasAttribute("reference-orientation")) return el;
		}
		return null;
	}

	private static double pt(String length) {
		if (length == null || length.length() == 0) return Double.NaN;
		if (length.endsWith("pt")) return Double.parseDouble(length.substring(0, length.length() - 2));
		if (length.endsWith("in")) return Double.parseDouble(length.substring(0, length.length() - 2)) * 72;
		throw new IllegalArgumentException(length);
	}

	private void checkRotatedContainerIsSized(int flags) throws Exception {
		// w:trHeight 1200 = 60pt: the rotated line may run 60pt down the page
		Element c = rotated(fo(table("<w:trHeight w:val=\"1200\"/>", "btLr"), flags));
		assertTrue("no rotated block-container", c != null);
		assertEquals("btLr turns the text 90 degrees anticlockwise",
				"90", c.getAttribute("reference-orientation"));
		assertEquals("the row's height", 60.0,
				pt(c.getAttribute("inline-progression-dimension")), 0.01);
		// the cell's 26.7pt grid width, less Word's default 108 twip cell margins
		double bpd = pt(c.getAttribute("block-progression-dimension"));
		assertTrue("the cell's content width, not the whole column: " + bpd,
				bpd > 10 && bpd < 26.7);
	}

	private void checkTbRlTurnsTheOtherWay(int flags) throws Exception {
		Element c = rotated(fo(table("<w:trHeight w:val=\"1200\"/>", "tbRl"), flags));
		assertTrue("no rotated block-container", c != null);
		assertEquals("-90", c.getAttribute("reference-orientation"));
		assertEquals(60.0, pt(c.getAttribute("inline-progression-dimension")), 0.01);
	}

	/** No w:trHeight: the row is as tall as the rotated text needs, and the height the
	 *  text can always be wrapped into is the cell's minimum content width. */
	private void checkNoRowHeightUsesTheContentMinimum(int flags) throws Exception {
		Element c = rotated(fo(table(null, "btLr"), flags));
		assertTrue("no rotated block-container", c != null);
		double ipd = pt(c.getAttribute("inline-progression-dimension"));
		// "Teil I" at the default size: its longest word, so a good deal less than the
		// whole string and nothing like the 651pt text height of the page
		assertTrue("the longest word of the cell: " + ipd, ipd > 5 && ipd < 30);
	}

	@Test public void rotatedContainerIsSizedVisitor() throws Exception {
		checkRotatedContainerIsSized(Docx4J.FLAG_NONE);
	}

	@Test public void rotatedContainerIsSizedXslt() throws Exception {
		checkRotatedContainerIsSized(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test public void tbRlTurnsTheOtherWayVisitor() throws Exception {
		checkTbRlTurnsTheOtherWay(Docx4J.FLAG_NONE);
	}

	@Test public void tbRlTurnsTheOtherWayXslt() throws Exception {
		checkTbRlTurnsTheOtherWay(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test public void noRowHeightUsesTheContentMinimumVisitor() throws Exception {
		checkNoRowHeightUsesTheContentMinimum(Docx4J.FLAG_NONE);
	}

	@Test public void noRowHeightUsesTheContentMinimumXslt() throws Exception {
		checkNoRowHeightUsesTheContentMinimum(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
