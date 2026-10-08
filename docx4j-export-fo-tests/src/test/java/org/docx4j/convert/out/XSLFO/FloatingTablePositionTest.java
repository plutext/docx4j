package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
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

	/** The fo:float the table was moved into, or null. */
	private static Element floatContainer(org.w3c.dom.Document doc) {
		Element table = foTable(doc);
		for (Node n = table.getParentNode(); n instanceof Element; n = n.getParentNode()) {
			Element e = (Element) n;
			if (FO_NS.equals(e.getNamespaceURI()) && "float".equals(e.getLocalName())) return e;
		}
		return null;
	}

	/** The widths of the columns of the one-row table which holds a floated table: the
	 *  gap to the text, the table, and the gap to the edge. */
	private static double[] floatColumns(org.w3c.dom.Document doc) {
		Element fl = floatContainer(doc);
		assertNotNull("the table should have been floated", fl);
		Element outer = null; // the float's own one-row table, not the document's
		for (Node n = fl.getFirstChild(); n != null && outer == null; n = n.getNextSibling()) {
			if (!(n instanceof Element)) continue;
			for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
				if (c instanceof Element && FO_NS.equals(c.getNamespaceURI())
						&& "table".equals(c.getLocalName())) {
					outer = (Element) c;
					break;
				}
			}
		}
		assertNotNull("the float should hold a one-row table", outer);
		java.util.List<Double> widths = new java.util.ArrayList<Double>();
		for (Node n = outer.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element && FO_NS.equals(n.getNamespaceURI())
					&& "table-column".equals(n.getLocalName())) {
				widths.add(pt(((Element) n).getAttribute("column-width")));
			}
		}
		double[] result = new double[widths.size()];
		for (int i = 0; i < result.length; i++) result[i] = widths.get(i);
		return result;
	}

	/** The common case (69 of the 74 w:tblpPr of the corpora): an offset from the
	 *  paragraph the table is anchored to, with the text flowing beside it.  Measured on
	 *  the table-floating probe: Word's first cell text is at 302.7 = 72 + tblpX 225 +
	 *  a 5.4pt cell margin, its top edge 72pt (tblpY) below the top of the paragraph the
	 *  w:tbl precedes, and that paragraph's next lines stop one w:leftFromText short of
	 *  it.  The float goes in the anchor paragraph, since FOP drops a float with no line
	 *  to anchor to. */
	private void checkTextAnchoredFloats(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(body("<w:tblpPr w:vertAnchor=\"text\" w:horzAnchor=\"margin\""
				+ " w:leftFromText=\"180\" w:rightFromText=\"180\""
				+ " w:tblpX=\"4500\" w:tblpY=\"1440\"/>"), flags);
		Element fl = floatContainer(doc);
		assertNotNull("a text-anchored table floats", fl);
		assertEquals("right", fl.getAttribute("float"));
		assertNull("and is not positioned as well", positioningContainer(doc));

		// the gap to the text, the table, and what is left to the column's edge
		double[] columns = floatColumns(doc);
		assertEquals(3, columns.length);
		assertEquals(9.0, columns[0], 0.01);
		assertEquals(TABLE_PT, columns[1], 0.01);
		assertEquals(COLUMN_PT - 225 - TABLE_PT, columns[2], 0.01);

		// tblpY is the distance from the anchor paragraph's top to the table's top edge: the
		// float's own offset on a renderer with float-offset (fork CR-023, 2.11-docx4j.6),
		// else padding on the block inside the float
		NodeList blocks = ((Element) fl).getElementsByTagNameNS(FO_NS, "block");
		if (floatOffset()) {
			assertEquals(72.0, pt(fl.getAttributeNS(FOX_NS, "float-offset")), 0.01);
			assertEquals("", ((Element) fl.getFirstChild()).getAttribute("padding-top"));
		} else {
			assertEquals(72.0, pt(((Element) fl.getFirstChild()).getAttribute("padding-top")), 0.01);
		}
		assertTrue("the float holds the table", blocks.getLength() > 0);

		// the float carries the position, so the table starts at its cell's edge
		assertEquals(0.0, pt(foTable(doc).getAttribute("start-indent")), 0.01);
		// and it is anchored in the paragraph it precedes
		Element para = (Element) fl.getParentNode();
		assertTrue("the float belongs to the following paragraph: " + para.getTextContent(),
				para.getTextContent().contains("after"));
	}

	/** horzAnchor="page": tblpX is measured from the paper's edge, so the table sits 1in
	 *  less than that into the text column. */
	private void checkPageHorizontalAnchor(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(body("<w:tblpPr w:vertAnchor=\"text\" w:horzAnchor=\"page\""
				+ " w:tblpX=\"2880\" w:tblpY=\"20\"/>"), flags);
		assertNull(positioningContainer(doc));
		Element fl = floatContainer(doc);
		assertNotNull(fl);
		assertEquals("left", fl.getAttribute("float"));
		double[] columns = floatColumns(doc);
		assertEquals(144.0 - 72.0, columns[0], 0.01); // tblpX from the page, less the margin
		assertEquals(TABLE_PT, columns[1], 0.01);
	}

	/** A table with no room for text beside it is left in the flow, where the text
	 *  follows it - which is where Word's wrapping puts it too. */
	private void aWideTableStaysInFlow(int flags) throws Exception {
		String wide = "<w:tbl><w:tblPr>"
				+ "<w:tblpPr w:vertAnchor=\"text\" w:horzAnchor=\"margin\" w:tblpX=\"100\"/>"
				+ "<w:tblLayout w:type=\"fixed\"/><w:tblW w:type=\"dxa\" w:w=\"8000\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"8000\"/></w:tblGrid>"
				+ "<w:tr><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"8000\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>one</w:t></w:r></w:p></w:tc></w:tr></w:tbl>"
				+ "<w:p><w:r><w:t>after</w:t></w:r></w:p>";
		org.w3c.dom.Document doc = fo(wide, flags);
		// 9026 - 8000 - 180 - 180 twips of room is less than the 2in a float needs on either
		// renderer (Word puts words in a sliver that narrow, probe -offset-sides case 3, but
		// FOP overflows what does not fit, gate b182)
		assertNull("nothing fits beside it", floatContainer(doc));
		assertEquals(5.0, pt(foTable(doc).getAttribute("start-indent")), 0.01);
		// a 5500 twip table leaves 3166 twips (2.2in): it floats on a renderer with sound
		// float edges, and stays in the flow on Apache FOP (over 60%)
		doc = fo(wide.replace("8000", "5500"), flags);
		if (sideFloatEdges()) assertNotNull("2in of room: floats", floatContainer(doc));
		else assertNull("over 60% on Apache FOP", floatContainer(doc));
		// a table as wide as the column stays in the flow on both renderers, where Word's
		// layout is the in-flow one (probe table-floating-wide-empties case 1)
		doc = fo(wide.replace("8000", "9026").replace("w:tblpX=\"100\"", "w:tblpX=\"0\""), flags);
		assertNull("no room beside a full-width table", floatContainer(doc));
	}

	private static boolean sideFloatEdges() {
		return org.docx4j.convert.out.fo.FopCapabilities.has(
				org.docx4j.convert.out.fo.FopCapabilities.Capability.SIDE_FLOAT_EDGES);
	}

	private static boolean floatOffset() {
		return org.docx4j.convert.out.fo.FopCapabilities.has(
				org.docx4j.convert.out.fo.FopCapabilities.Capability.FLOAT_OFFSET);
	}

	private static final String FOX_NS = "http://xmlgraphics.apache.org/fop/extensions";

	private static final String DOCX4J_NS = "http://docx4j.org/fop/word-layout";

	/** A length in pt or in (docx4j writes "0in" for a zero space), else 0. */
	private static double len(String v) {
		if (v == null || v.isEmpty()) return 0;
		if (v.endsWith("in")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72;
		if (v.endsWith("pt")) return Double.parseDouble(v.substring(0, v.length() - 2));
		return 0;
	}

	private static String fullWidthTable(String tblpPr, String cellText) {
		return "<w:tbl><w:tblPr>" + tblpPr
				+ "<w:tblLayout w:type=\"fixed\"/><w:tblW w:type=\"dxa\" w:w=\"9026\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"9026\"/></w:tblGrid>"
				+ "<w:tr><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"9026\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>" + cellText + "</w:t></w:r></w:p></w:tc></w:tr></w:tbl>";
	}

	/** The element siblings before the fo:table in its flow, in order. */
	private static java.util.List<Element> siblingsBefore(Element table) {
		java.util.List<Element> out = new java.util.ArrayList<Element>();
		for (Node n = table.getPreviousSibling(); n != null; n = n.getPreviousSibling()) {
			if (n instanceof Element) out.add(0, (Element) n);
		}
		return out;
	}

	/** A full-width text-anchored table keeps its w:tblpY in the flow, as Word lays it out:
	 *  the empty paragraphs after it that fit in the gap go above it, the rest of the gap is
	 *  a spacer before it (probe table-floating-wide-empties case 3: tblpY 36pt, eight empties,
	 *  Word lays two above and six below; CR-032 phase 1). */
	private void inFlowOffsetMovesEmptiesAbove(int flags) throws Exception {
		org.w3c.dom.Document doc = fo("<w:p><w:r><w:t>before</w:t></w:r></w:p>"
				+ fullWidthTable("<w:tblpPr w:vertAnchor=\"text\" w:horzAnchor=\"margin\" w:tblpX=\"0\" w:tblpY=\"720\"/>", "wide")
				+ "<w:p/><w:p/><w:p/><w:p><w:r><w:t>after</w:t></w:r></w:p>", flags);
		Element table = foTable(doc);
		assertNull(floatContainer(doc));
		java.util.List<Element> before = siblingsBefore(table);
		// "before", the empties that fit in 36pt, the spacer
		Element spacer = before.get(before.size() - 1);
		assertEquals("block-container", spacer.getLocalName());
		double moved = 0;
		int empties = 0;
		for (int i = 1; i < before.size() - 1; i++) {
			Element empty = before.get(i);
			assertEquals("block", empty.getLocalName());
			assertEquals("an empty paragraph", "", empty.getTextContent().trim());
			moved += pt(empty.getAttribute("line-height")) + len(empty.getAttribute("space-before"))
					+ len(empty.getAttribute("space-after"));
			empties++;
		}
		assertTrue("at least one empty paragraph fits in 36pt", empties >= 1);
		assertTrue("the next would not have fitted: " + moved, moved <= 36 && moved + moved / empties > 36);
		assertEquals(36.0 - moved, pt(spacer.getAttribute("height")), 0.05);
		// the empties not moved are still after the table, then "after"
		Element next = (Element) table.getNextSibling();
		assertEquals("", next.getTextContent().trim());
	}

	@Test public void inFlowOffsetVisitor() throws Exception { inFlowOffsetMovesEmptiesAbove(Docx4J.FLAG_NONE); }
	@Test public void inFlowOffsetXslt() throws Exception { inFlowOffsetMovesEmptiesAbove(Docx4J.FLAG_EXPORT_PREFER_XSL); }

	/** A text anchor: Word lays the lines that fit in the gap above the table; docx4j keeps
	 *  only the part of the gap those lines would not fill, so the following text lands
	 *  where Word's does (probe -wide-anchor-text case 2: tblpY 30pt, two 13.8pt lines above
	 *  in Word, the next paragraph within 1.1pt). */
	private void textAnchorKeepsTheGapsRemainder(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(fullWidthTable("<w:tblpPr w:vertAnchor=\"text\" w:horzAnchor=\"margin\" w:tblpX=\"0\" w:tblpY=\"600\"/>", "wide")
				+ "<w:p><w:r><w:t>anchor paragraph with text</w:t></w:r></w:p>", flags);
		Element table = foTable(doc);
		Element anchor = (Element) table.getNextSibling();
		double pitch = pt(anchor.getAttribute("line-height"));
		assertTrue(pitch > 10);
		Element spacer = (Element) table.getPreviousSibling();
		assertEquals("block-container", spacer.getLocalName());
		assertEquals(30.0 - Math.floor(30.0 / pitch) * pitch, pt(spacer.getAttribute("height")), 0.05);
	}

	@Test public void textAnchorOffsetVisitor() throws Exception { textAnchorKeepsTheGapsRemainder(Docx4J.FLAG_NONE); }
	@Test public void textAnchorOffsetXslt() throws Exception { textAnchorKeepsTheGapsRemainder(Docx4J.FLAG_EXPORT_PREFER_XSL); }

	/** Two floating tables on one anchor paragraph go into it side by side (Word: probe
	 *  table-floating-pair case 1, the text between them); the first used to be left in the
	 *  flow because its next sibling was the second table, not the paragraph. */
	private void aPairSharesTheAnchor(int flags) throws Exception {
		String narrow = "<w:tbl><w:tblPr>%s<w:tblLayout w:type=\"fixed\"/><w:tblW w:type=\"dxa\" w:w=\"2000\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"2000\"/></w:tblGrid><w:tr><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"2000\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>%s</w:t></w:r></w:p></w:tc></w:tr></w:tbl>";
		org.w3c.dom.Document doc = fo(
				String.format(narrow, "<w:tblpPr w:vertAnchor=\"text\" w:horzAnchor=\"margin\" w:tblpX=\"0\" w:tblpY=\"0\"/>", "left")
				+ String.format(narrow, "<w:tblpPr w:vertAnchor=\"text\" w:horzAnchor=\"margin\" w:tblpXSpec=\"right\" w:tblpY=\"0\"/>", "right")
				+ "<w:p><w:r><w:t>anchor of both</w:t></w:r></w:p>", flags);
		NodeList floats = doc.getElementsByTagNameNS(FO_NS, "float");
		assertEquals(2, floats.getLength());
		assertSame("both in the one paragraph", floats.item(0).getParentNode(), floats.item(1).getParentNode());
		assertEquals("left", ((Element) floats.item(0)).getAttribute("float"));
		assertEquals("right", ((Element) floats.item(1)).getAttribute("float"));
	}

	@Test public void pairVisitor() throws Exception { aPairSharesTheAnchor(Docx4J.FLAG_NONE); }
	@Test public void pairXslt() throws Exception { aPairSharesTheAnchor(Docx4J.FLAG_EXPORT_PREFER_XSL); }

	/** No w:vertAnchor is the margin box (probe table-floating-anchor case 5: Word at
	 *  y=173 = 72 + 100), not the text - taken where the positioned container reserves its
	 *  band, the lower half of the page; in the upper half the old text-anchored treatment
	 *  stands (3387 and 7490 lost a page and 26 lines to an unreserved container, gate b182). */
	private void noVertAnchorIsTheMargin(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(body("<w:tblpPr w:horzAnchor=\"margin\" w:tblpX=\"1000\" w:tblpY=\"8000\"/>"), flags);
		Element abs = positioningContainer(doc);
		assertNotNull("anchored to the margin box", abs);
		assertEquals(472.0, pt(abs.getAttribute("top")), 0.01);
		doc = fo(body("<w:tblpPr w:horzAnchor=\"margin\" w:tblpX=\"1000\" w:tblpY=\"2000\"/>"), flags);
		assertNull("upper half: not positioned", positioningContainer(doc));
	}

	@Test public void noVertAnchorVisitor() throws Exception { noVertAnchorIsTheMargin(Docx4J.FLAG_NONE); }
	@Test public void noVertAnchorXslt() throws Exception { noVertAnchorIsTheMargin(Docx4J.FLAG_EXPORT_PREFER_XSL); }

	@Test public void wideTableVisitor() throws Exception { aWideTableStaysInFlow(Docx4J.FLAG_NONE); }
	@Test public void wideTableXslt() throws Exception {
		aWideTableStaysInFlow(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** FOP drops a float from a multi-column region silently, so such a table is left in
	 *  the flow. */
	private void aTableInColumnsStaysInFlow(int flags) throws Exception {
		String twoColumns = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/>"
				+ "<w:cols w:num=\"2\" w:space=\"720\"/></w:sectPr>";
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getDocumentSettingsPart().setWordCompatSetting("compatibilityMode", "15");
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ body("<w:tblpPr w:vertAnchor=\"text\" w:horzAnchor=\"margin\" w:tblpX=\"1000\"/>")
				+ twoColumns + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder()
				.parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
		assertNull("no float in a multi-column region", floatContainer(doc));
	}

	@Test public void tableInColumnsVisitor() throws Exception {
		aTableInColumnsStaysInFlow(Docx4J.FLAG_NONE);
	}

	@Test public void tableInColumnsXslt() throws Exception {
		aTableInColumnsStaysInFlow(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** Apache FOP throws when a float shares a flow with a block inside an inline - the shape
	 *  a line break inside a run takes - and renders nothing at all when the float holding a
	 *  table is moved to flow level to avoid that (both measured).  So on Apache FOP such a
	 *  table is left in the flow rather than lost.  The docx4j renderer from 2.11-docx4j.5
	 *  (capability side-float-edges) carries the fix of that NPE (fop/CR-011), and Word floats
	 *  the table as any other (probe table-floating-br-anchor), so there it is floated
	 *  (CR-032 phase 1). */
	private void aTableBeforeALineBreak(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(body("<w:tblpPr w:vertAnchor=\"text\" w:horzAnchor=\"margin\""
						+ " w:tblpX=\"4500\"/>")
				+ "<w:p><w:r><w:t>a</w:t><w:br/><w:t>b</w:t></w:r></w:p>", flags);
		if (sideFloatEdges()) {
			assertNotNull("floated on a renderer whose float survives the line break", floatContainer(doc));
		} else {
			assertNull("the float would be lost", floatContainer(doc));
			assertEquals(225.0, pt(foTable(doc).getAttribute("start-indent")), 0.01);
		}
		// with an offset of more than a line and a half it stays in the flow, the offset kept
		// as a spacer, until the fork's float-offset puts the band at the offset (4083: the
		// lines above the table were cut into a column beside it); with float-offset
		// (2.11-docx4j.6) it floats, the offset on the float
		doc = fo(body("<w:tblpPr w:vertAnchor=\"text\" w:horzAnchor=\"margin\" w:tblpX=\"4500\" w:tblpY=\"1000\"/>")
				+ "<w:p><w:r><w:t>a</w:t><w:br/><w:t>b</w:t></w:r></w:p>", flags);
		if (sideFloatEdges() && floatOffset()) {
			Element fl = floatContainer(doc);
			assertNotNull("a large offset: floated at it", fl);
			assertEquals(50.0, pt(fl.getAttributeNS(FOX_NS, "float-offset")), 0.01);
		} else {
			assertNull("a large offset: in the flow", floatContainer(doc));
			Element spacer = (Element) foTable(doc).getPreviousSibling();
			assertEquals("block-container", spacer.getLocalName());
			assertTrue("the offset's remainder as a spacer", pt(spacer.getAttribute("height")) > 0);
		}
	}

	@Test public void tableBeforeALineBreakVisitor() throws Exception {
		aTableBeforeALineBreak(Docx4J.FLAG_NONE);
	}

	@Test public void tableBeforeALineBreakXslt() throws Exception {
		aTableBeforeALineBreak(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** A page-anchored table which opens a page - after a hard page break - is positioned
	 *  there, as one which opens the section is: Word puts the following text at the top
	 *  of the page and the table at its anchor (measured, table-floating-anchor probe:
	 *  the paragraph at y=83.1, the table at 168.3). */
	private void aTableAfterAPageBreakIsPositioned(int flags) throws Exception {
		org.w3c.dom.Document doc = fo("<w:p><w:r><w:t>page one</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>"
				+ body("<w:tblpPr w:vertAnchor=\"page\" w:horzAnchor=\"margin\""
						+ " w:tblpXSpec=\"center\" w:tblpY=\"2880\"/>"), flags);
		Element abs = positioningContainer(doc);
		assertNotNull("a table which opens a page is positioned", abs);
		assertEquals(144.0, pt(abs.getAttribute("top")), 0.01);
	}

	@Test public void afterAPageBreakVisitor() throws Exception {
		aTableAfterAPageBreakIsPositioned(Docx4J.FLAG_NONE);
	}

	@Test public void afterAPageBreakXslt() throws Exception {
		aTableAfterAPageBreakIsPositioned(Docx4J.FLAG_EXPORT_PREFER_XSL);
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

	/**
	 * A page-anchored table too wide for anything to fit beside it, with content before
	 * it: Word draws it at its anchor and puts what follows below it, so it is positioned
	 * <em>and</em> its band is reserved in the flow by an invisible copy left where it
	 * was (&#xa7;9.5's w:wrap trick, &#xa7;6.8).  Before 17.1.0 it was left in the flow
	 * entirely, because the flow closing over it drew the two on top of each other.  The
	 * copy is in an fo:block with visibility="hidden", since FOP honours the property on a
	 * block only: marked on the table itself, the copy was drawn too (corpus document 5075,
	 * its table twice; Enterprise CR-001 &#xa7;6.6 item 43).  @since 17.3.1 for the block
	 */
	private void aWideMidFlowTableIsPositionedAndReserved(int flags) throws Exception {
		String wide = "<w:tbl><w:tblPr>"
				+ "<w:tblpPr w:vertAnchor=\"page\" w:horzAnchor=\"margin\" w:tblpX=\"0\" w:tblpY=\"12000\"/>"
				+ "<w:tblLayout w:type=\"fixed\"/><w:tblW w:type=\"dxa\" w:w=\"8800\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"8800\"/></w:tblGrid>"
				+ "<w:tr><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"8800\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>one</w:t></w:r></w:p></w:tc></w:tr></w:tbl>";
		org.w3c.dom.Document doc = fo("<w:p><w:r><w:t>before</w:t></w:r></w:p>"
				+ wide + "<w:p><w:r><w:t>after</w:t></w:r></w:p>", flags);
		NodeList tables = doc.getElementsByTagNameNS(FO_NS, "table");
		assertEquals("the table and its invisible band", 2, tables.getLength());
		int hidden = 0, positioned = 0;
		for (int i = 0; i < tables.getLength(); i++) {
			Element t = (Element) tables.item(i);
			assertEquals("FOP ignores visibility on a table", "", t.getAttribute("visibility"));
			Node parent = t.getParentNode();
			if (parent instanceof Element && "block".equals(parent.getLocalName())
					&& "hidden".equals(((Element) parent).getAttribute("visibility"))) hidden++;
			for (Node n = t.getParentNode(); n instanceof Element; n = n.getParentNode()) {
				Element e = (Element) n;
				if (FO_NS.equals(e.getNamespaceURI()) && "block-container".equals(e.getLocalName())
						&& e.hasAttribute("absolute-position")) {
					positioned++;
					assertEquals("tblpY from the page top", 600.0, pt(e.getAttribute("top")), 0.01);
					break;
				}
			}
		}
		assertEquals("one copy in a hidden block reserves the band", 1, hidden);
		assertEquals("one copy is positioned at the anchor", 1, positioned);
	}

	@Test public void wideMidFlowVisitor() throws Exception {
		aWideMidFlowTableIsPositionedAndReserved(Docx4J.FLAG_NONE);
	}

	@Test public void wideMidFlowXslt() throws Exception {
		aWideMidFlowTableIsPositionedAndReserved(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** docx4j.convert.out.fo.tables.reserveBand=false leaves it in the flow, as b2-batch18
	 *  did. */
	@Test public void reserveBandCanBeTurnedOff() throws Exception {
		org.docx4j.Docx4jProperties.setProperty("docx4j.convert.out.fo.tables.reserveBand", false);
		try {
			org.w3c.dom.Document doc = fo("<w:p><w:r><w:t>before</w:t></w:r></w:p>"
					+ "<w:tbl><w:tblPr><w:tblpPr w:vertAnchor=\"page\" w:horzAnchor=\"margin\""
					+ " w:tblpX=\"0\" w:tblpY=\"12000\"/><w:tblLayout w:type=\"fixed\"/>"
					+ "<w:tblW w:type=\"dxa\" w:w=\"8800\"/></w:tblPr>"
					+ "<w:tblGrid><w:gridCol w:w=\"8800\"/></w:tblGrid>"
					+ "<w:tr><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"8800\"/></w:tcPr>"
					+ "<w:p><w:r><w:t>one</w:t></w:r></w:p></w:tc></w:tr></w:tbl>"
					+ "<w:p><w:r><w:t>after</w:t></w:r></w:p>", Docx4J.FLAG_NONE);
			assertEquals("one table, in the flow", 1,
					doc.getElementsByTagNameNS(FO_NS, "table").getLength());
			assertNull(positioningContainer(doc));
		} finally {
			org.docx4j.Docx4jProperties.setProperty("docx4j.convert.out.fo.tables.reserveBand", true);
		}
	}

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

	@Test public void textAnchoredVisitor() throws Exception { checkTextAnchoredFloats(Docx4J.FLAG_NONE); }
	@Test public void textAnchoredXslt() throws Exception { checkTextAnchoredFloats(Docx4J.FLAG_EXPORT_PREFER_XSL); }

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

	/** FOP must flow the text beside the floated table: the lines next to it are shorter
	 *  than the column by the table and its w:leftFromText (measured on the table-floating
	 *  probe, where Word's lines beside the table stop at 285.8 in a column which runs to
	 *  523.3, and the table's own text is at 302.7). */
	private void fopFlowsTextBesideTheFloat(int flags) throws Exception {
		String prose = "Pagination is the process of dividing a document into discrete pages,"
				+ " either electronic pages or printed pages. Ut enim ad minim veniam, quis"
				+ " nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo.";
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getDocumentSettingsPart().setWordCompatSetting("compatibilityMode", "15");
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ table("<w:tblpPr w:vertAnchor=\"text\" w:horzAnchor=\"margin\""
						+ " w:leftFromText=\"180\" w:tblpX=\"4500\"/>")
				+ "<w:p><w:r><w:t>" + prose + "</w:t></w:r></w:p>"
				+ SECT_PR + "</w:body></w:document>"));
		org.w3c.dom.Document at = areaTree(pkg, flags);

		double narrowest = Double.MAX_VALUE;
		int proseLines = 0;
		NodeList lines = at.getElementsByTagName("lineArea");
		for (int i = 0; i < lines.getLength(); i++) {
			Element line = (Element) lines.item(i);
			if (line.getTextContent().trim().length() < 20) continue; // not the table's
			proseLines++;
			narrowest = Math.min(narrowest, Double.parseDouble(line.getAttribute("ipd")) / 1000);
		}
		assertTrue("no line was laid out", proseLines > 0);
		// the column is 451.3 wide; the float takes 9 (leftFromText) + 200 (the table) +
		// 26.3 (what is left of the column beyond it), leaving 216 for the lines beside it
		assertEquals("the lines beside the table should stop short of it",
				216.0, narrowest, 1.0);
	}

	@Test public void fopFlowsTextBesideVisitor() throws Exception {
		fopFlowsTextBesideTheFloat(Docx4J.FLAG_NONE);
	}

	@Test public void fopFlowsTextBesideXslt() throws Exception {
		fopFlowsTextBesideTheFloat(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
