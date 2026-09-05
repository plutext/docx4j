package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
 * A floating table (w:tblPr/w:tblpPr), as Word places it (CR-001, measured against
 * Word 365 goldens):
 *
 * <ul>
 * <li>the grid edge goes at w:tblpX within the frame w:horzAnchor names (the page, or
 *   the text column), or where w:tblpXSpec says; measured on the table-floating probe,
 *   Word's first cell text is at margin + tblpX + one cell margin;</li>
 * <li>a vertical position measured from the page (w:vertAnchor="page") or from the
 *   margin box (w:vertAnchor="margin", or any w:tblpYSpec) takes the table out of the
 *   flow into an absolutely positioned container, which is what a Word cover page or a
 *   letterhead needs;</li>
 * <li>w:tblpY against the default w:vertAnchor="text" is an offset from the paragraph
 *   the table is anchored to, and leaves the table in the flow (XSL-FO cannot wrap
 *   text around it), with only the horizontal position applied.</li>
 * </ul>
 *
 * Both FO pathways (the table FO is built in Java for each).
 */
public class FloatingTablePositionTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	/** A4 portrait with 1in margins: a 9026 twip (451.3pt) text column, 13958 twips
	 *  (697.9pt) of margin box down the page. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static final double COLUMN_PT = (11906 - 2880) / 20.0;
	private static final double TABLE_PT = 4000 / 20.0;

	/** A 4000 twip fixed-layout table carrying the given w:tblpPr. */
	private static String table(String tblpPr) {
		return "<w:tbl><w:tblPr>" + tblpPr + "<w:tblLayout w:type=\"fixed\"/>"
				+ "<w:tblW w:type=\"dxa\" w:w=\"4000\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/></w:tblGrid>"
				+ "<w:tr><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"2000\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>one</w:t></w:r></w:p></w:tc>"
				+ "<w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"2000\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>two</w:t></w:r></w:p></w:tc></w:tr></w:tbl>";
	}

	/** the cover-page shape: the floating table opens the flow */
	private static String body(String tblpPr) {
		return table(tblpPr) + "<w:p><w:r><w:t>after</w:t></w:r></w:p>";
	}

	private static org.w3c.dom.Document fo(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getDocumentSettingsPart().setWordCompatSetting("compatibilityMode", "15");
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	private static Element foTable(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS(FO_NS, "table");
		assertTrue("no fo:table", nl.getLength() > 0);
		return (Element) nl.item(0);
	}

	/** The absolutely positioned container the table was moved into, or null. */
	private static Element positioningContainer(org.w3c.dom.Document doc) {
		Element table = foTable(doc);
		for (Node n = table.getParentNode(); n instanceof Element; n = n.getParentNode()) {
			Element e = (Element) n;
			if (FO_NS.equals(e.getNamespaceURI()) && "block-container".equals(e.getLocalName())
					&& e.hasAttribute("absolute-position")) {
				return e;
			}
		}
		return null;
	}

	private static double pt(String length) {
		if (length == null || length.length() == 0) return Double.NaN;
		if (length.endsWith("pt")) return Double.parseDouble(length.substring(0, length.length() - 2));
		if (length.endsWith("in")) return Double.parseDouble(length.substring(0, length.length() - 2)) * 72;
		throw new IllegalArgumentException(length);
	}

	/** Word's cover-page geometry: anchored to the page, centred on the text column. */
	private void checkPageAnchored(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(body("<w:tblpPr w:vertAnchor=\"page\" w:horzAnchor=\"margin\""
				+ " w:tblpXSpec=\"center\" w:tblpY=\"2880\"/>"), flags);
		Element abs = positioningContainer(doc);
		assertNotNull("the table should be positioned out of the flow", abs);
		assertEquals("fixed", abs.getAttribute("absolute-position"));
		assertEquals(144.0, pt(abs.getAttribute("top")), 0.01);          // tblpY from the page top
		assertEquals(72 + (COLUMN_PT - TABLE_PT) / 2, pt(abs.getAttribute("left")), 0.01);
		assertEquals(TABLE_PT, pt(abs.getAttribute("width")), 0.01);
		// the container carries the position, so the table starts at its edge
		assertEquals(0.0, pt(foTable(doc).getAttribute("start-indent")), 0.01);
	}

	/** The height (millipoints) of the body flow FOP laid out. */
	private static double flowHeight(org.w3c.dom.Document areaTree) {
		NodeList flows = areaTree.getElementsByTagName("flow");
		assertTrue("no flow in the area tree", flows.getLength() > 0);
		return Double.parseDouble(((Element) flows.item(0)).getAttribute("bpd"));
	}

	/** The positioned table takes no space: the flow is as tall as it would be if the
	 *  table were not there at all. */
	private void positionedTableTakesNoSpace(int flags) throws Exception {
		double withTable = flowHeight(areaTree(pkg("<w:tblpPr w:vertAnchor=\"page\" w:horzAnchor=\"margin\""
				+ " w:tblpXSpec=\"center\" w:tblpY=\"2880\"/>"), flags));
		WordprocessingMLPackage bare = WordprocessingMLPackage.createPackage();
		bare.getMainDocumentPart().getDocumentSettingsPart().setWordCompatSetting("compatibilityMode", "15");
		bare.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>after</w:t></w:r></w:p>" + SECT_PR + "</w:body></w:document>"));
		double withoutTable = flowHeight(areaTree(bare, flags));
		assertEquals("the positioned table should take no space in the flow",
				withoutTable, withTable, 10);
	}

	@Test public void takesNoSpaceVisitor() throws Exception {
		positionedTableTakesNoSpace(Docx4J.FLAG_NONE);
	}

	@Test public void takesNoSpaceXslt() throws Exception {
		positionedTableTakesNoSpace(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** w:tblpYSpec: the table's height is not known before layout, so the container is
	 *  the whole box with display-align on it. */
	private void checkBottomOfMarginBox(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(body("<w:tblpPr w:tblpYSpec=\"bottom\"/>"), flags);
		Element abs = positioningContainer(doc);
		assertNotNull("the table should be positioned out of the flow", abs);
		assertEquals(72.0, pt(abs.getAttribute("top")), 0.01);
		assertEquals((16838 - 2880) / 20.0, pt(abs.getAttribute("height")), 0.01);
		assertEquals("after", abs.getAttribute("display-align"));
		assertEquals(72.0, pt(abs.getAttribute("left")), 0.01);
	}

	/** w:vertAnchor="page" with w:tblpYSpec="center": centred on the page, not the box. */
	private void checkCentreOfPage(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(body("<w:tblpPr w:vertAnchor=\"page\" w:horzAnchor=\"page\""
				+ " w:tblpYSpec=\"center\" w:tblpXSpec=\"right\"/>"), flags);
		Element abs = positioningContainer(doc);
		assertNotNull(abs);
		assertEquals(0.0, pt(abs.getAttribute("top")), 0.01);
		assertEquals(16838 / 20.0, pt(abs.getAttribute("height")), 0.01);
		assertEquals("center", abs.getAttribute("display-align"));
		assertEquals(11906 / 20.0 - TABLE_PT, pt(abs.getAttribute("left")), 0.01);
	}

	/** The common case: an offset from the anchoring paragraph.  The table stays in the
	 *  flow; only the horizontal position is applied (measured on the table-floating
	 *  probe: Word's first cell text at 302.7pt = 72 + tblpX 225 + a 5.4pt cell margin). */
	private void checkTextAnchoredStaysInFlow(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(body("<w:tblpPr w:vertAnchor=\"text\" w:horzAnchor=\"margin\""
				+ " w:tblpX=\"4500\" w:tblpY=\"1440\"/>"), flags);
		assertNull("a text-anchored table stays in the flow", positioningContainer(doc));
		assertEquals(225.0, pt(foTable(doc).getAttribute("start-indent")), 0.01);
	}

	/** horzAnchor="page": tblpX is measured from the paper's edge, so the start-indent
	 *  (which is measured from the text margin) is 1in less. */
	private void checkPageHorizontalAnchor(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(body("<w:tblpPr w:vertAnchor=\"text\" w:horzAnchor=\"page\""
				+ " w:tblpX=\"2880\" w:tblpY=\"20\"/>"), flags);
		assertNull(positioningContainer(doc));
		assertEquals(144.0 - 72.0, pt(foTable(doc).getAttribute("start-indent")), 0.01);
	}

	/** A page-anchored table with text before it is mid-flow, where Word wraps the text
	 *  after it around (in practice below) the table: it stays in the flow, since taking
	 *  it out would draw it over that text.  Measured on two corpus documents. */
	private void midFlowTableStaysInFlow(int flags) throws Exception {
		org.w3c.dom.Document doc = fo("<w:p><w:r><w:t>before</w:t></w:r></w:p>"
				+ body("<w:tblpPr w:vertAnchor=\"page\" w:horzAnchor=\"margin\""
						+ " w:tblpXSpec=\"center\" w:tblpY=\"2880\"/>"), flags);
		assertNull("a table with text before it stays in the flow", positioningContainer(doc));
		// the horizontal position is still Word's
		assertEquals((COLUMN_PT - TABLE_PT) / 2, pt(foTable(doc).getAttribute("start-indent")), 0.01);
	}

	@Test public void midFlowVisitor() throws Exception { midFlowTableStaysInFlow(Docx4J.FLAG_NONE); }
	@Test public void midFlowXslt() throws Exception { midFlowTableStaysInFlow(Docx4J.FLAG_EXPORT_PREFER_XSL); }

	/** An empty paragraph before it is still the cover-page shape. */
	private void emptyParagraphBeforeIsStillTheStart(int flags) throws Exception {
		org.w3c.dom.Document doc = fo("<w:p/>"
				+ body("<w:tblpPr w:vertAnchor=\"page\" w:horzAnchor=\"margin\""
						+ " w:tblpXSpec=\"center\" w:tblpY=\"2880\"/>"), flags);
		assertNotNull(positioningContainer(doc));
	}

	@Test public void emptyParagraphBeforeVisitor() throws Exception {
		emptyParagraphBeforeIsStillTheStart(Docx4J.FLAG_NONE);
	}

	@Test public void emptyParagraphBeforeXslt() throws Exception {
		emptyParagraphBeforeIsStillTheStart(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** A table with no w:tblpPr is untouched. */
	private void checkPlainTableUnaffected(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(body(""), flags);
		assertNull(positioningContainer(doc));
		assertEquals(0.0, pt(foTable(doc).getAttribute("start-indent")), 0.01);
	}

	@Test public void pageAnchoredVisitor() throws Exception { checkPageAnchored(Docx4J.FLAG_NONE); }
	@Test public void pageAnchoredXslt() throws Exception { checkPageAnchored(Docx4J.FLAG_EXPORT_PREFER_XSL); }

	@Test public void bottomOfMarginBoxVisitor() throws Exception { checkBottomOfMarginBox(Docx4J.FLAG_NONE); }
	@Test public void bottomOfMarginBoxXslt() throws Exception { checkBottomOfMarginBox(Docx4J.FLAG_EXPORT_PREFER_XSL); }

	@Test public void centreOfPageVisitor() throws Exception { checkCentreOfPage(Docx4J.FLAG_NONE); }
	@Test public void centreOfPageXslt() throws Exception { checkCentreOfPage(Docx4J.FLAG_EXPORT_PREFER_XSL); }

	@Test public void textAnchoredVisitor() throws Exception { checkTextAnchoredStaysInFlow(Docx4J.FLAG_NONE); }
	@Test public void textAnchoredXslt() throws Exception { checkTextAnchoredStaysInFlow(Docx4J.FLAG_EXPORT_PREFER_XSL); }

	@Test public void pageHorizontalAnchorVisitor() throws Exception { checkPageHorizontalAnchor(Docx4J.FLAG_NONE); }
	@Test public void pageHorizontalAnchorXslt() throws Exception { checkPageHorizontalAnchor(Docx4J.FLAG_EXPORT_PREFER_XSL); }

	@Test public void plainTableVisitor() throws Exception { checkPlainTableUnaffected(Docx4J.FLAG_NONE); }
	@Test public void plainTableXslt() throws Exception { checkPlainTableUnaffected(Docx4J.FLAG_EXPORT_PREFER_XSL); }

	private static WordprocessingMLPackage pkg(String tblpPr) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getDocumentSettingsPart().setWordCompatSetting("compatibilityMode", "15");
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body(tblpPr) + SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	/** FOP must lay the positioned table out where the container puts it: the fixed
	 *  viewport is at (197.65pt, 144pt) from the page's top left corner. */
	private void fopPlacesThePositionedTable(int flags) throws Exception {
		org.w3c.dom.Document at = areaTree(pkg("<w:tblpPr w:vertAnchor=\"page\" w:horzAnchor=\"margin\""
				+ " w:tblpXSpec=\"center\" w:tblpY=\"2880\"/>"), flags);
		boolean found = false;
		StringBuilder seen = new StringBuilder();
		NodeList blocks = at.getElementsByTagName("block");
		for (int i = 0; i < blocks.getLength(); i++) {
			Element b = (Element) blocks.item(i);
			if (!"fixed".equals(b.getAttribute("positioning"))) continue;
			seen.append(b.getAttribute("left-position")).append('/')
					.append(b.getAttribute("top-position")).append(' ');
			double left = Double.parseDouble(b.getAttribute("left-position")) / 1000;
			double top = Double.parseDouble(b.getAttribute("top-position")) / 1000;
			if (Math.abs(left - (72 + (COLUMN_PT - TABLE_PT) / 2)) < 0.5 && Math.abs(top - 144) < 0.5) {
				found = true;
			}
		}
		assertTrue("no fixed viewport at the table's position; saw " + seen, found);
		assertTrue("nothing was laid out", lineCount(at) >= 3);
	}

	@Test public void fopPlacesTheTableVisitor() throws Exception {
		fopPlacesThePositionedTable(Docx4J.FLAG_NONE);
	}

	@Test public void fopPlacesTheTableXslt() throws Exception {
		fopPlacesThePositionedTable(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
