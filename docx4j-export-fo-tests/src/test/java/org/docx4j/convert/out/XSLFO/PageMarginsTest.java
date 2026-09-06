package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Three things {@code w:sectPr} says about where the text sits which docx4j did not
 * honour until 17.0.6 (CR-001 &#xa7;7).
 *
 * <ul>
 * <li><b>A negative {@code w:pgMar/@w:top}</b> means "the body starts |top| from the page
 * edge whatever the header does": Word lets the header overlap the text rather than
 * pushing it down.  Measured on a document with {@code w:top="-312"} (-15.6pt) and
 * {@code w:header="709"}: Word's body top is 15.55pt, where
 * {@code max(top, header + header height)} put ours at 49.25 - +33.7pt on the table
 * header, on page 2 and on every one of six pictures.  A second document with
 * {@code w:top="-993"} was +97.5pt throughout.</li>
 *
 * <li><b>{@code w:pgMar/@w:gutter}</b> is the binding margin, added to the left margin.
 * {@code PageDimensions.getWritableWidthTwips()} already subtracted it, so our text
 * column was the right width and started in the wrong place: measured on a document with
 * {@code w:left="851" w:gutter="567"}, Word puts every portrait line at x=70.9 where ours
 * was at 42.5, -28.35pt on 222 pages.</li>
 *
 * <li><b>A single narrow {@code w:col}</b>: where {@code w:cols} declares one column
 * narrower than the margin box, Word uses that width for the text.  Measured on a
 * document whose margin box is 451.45pt and whose {@code w:cols} says
 * {@code <w:col w:w="8640"/>} (432pt): Word centres on 288 where ours was 297.7 (+9.7pt
 * on every centred line) and puts the right edge at 504 where ours was 523.45.</li>
 * </ul>
 *
 * @since 17.0.6
 */
public class PageMarginsTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static WordprocessingMLPackage pkg(String sectPrInner) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>first line of the body</w:t></w:r></w:p>"
				+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>" + sectPrInner + "</w:sectPr>"
				+ "</w:body></w:document>"));
		return pkg;
	}

	/** {x, y, width, height} of the body region viewport, in millipoints. */
	private static int[] bodyRect(org.w3c.dom.Document areaTree) {
		NodeList regions = areaTree.getElementsByTagName("regionBody");
		assertTrue("no regionBody", regions.getLength() > 0);
		Element viewport = (Element) regions.item(0).getParentNode();
		String rect = viewport.getAttribute("rect");
		assertNotNull(rect);
		String[] parts = rect.trim().split("\\s+");
		return new int[] { Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
				Integer.parseInt(parts[2]), Integer.parseInt(parts[3]) };
	}

	private static void assertNear(String message, int expected, int actual, int tolerance) {
		assertTrue(message + ": expected " + expected + " +/- " + tolerance + " but was " + actual,
				Math.abs(expected - actual) <= tolerance);
	}

	// ------------------------------------------------------------------ negative w:top

	/** w:top="-312" = -15.6pt, w:header="709" = 35.45pt: the body starts at 15.6pt. */
	private static final String NEGATIVE_TOP =
			"<w:pgMar w:top=\"-312\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\""
			+ " w:header=\"709\" w:footer=\"709\"/>";

	private void negativeTopMargin(int flags) throws Exception {
		int y = bodyRect(areaTree(pkg(NEGATIVE_TOP), flags))[1];
		assertNear("the body should start at |w:top| = 15.6pt, not below the header", 15600, y, 100);
	}

	@Test
	public void negativeTopMarginVisitor() throws Exception {
		negativeTopMargin(Docx4J.FLAG_NONE);
	}

	@Test
	public void negativeTopMarginXslt() throws Exception {
		negativeTopMargin(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	// ---------------------------------------------------------------------- w:gutter

	/** w:left="851" (42.55pt) + w:gutter="567" (28.35pt) = 70.9pt. */
	private static final String GUTTER =
			"<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"851\""
			+ " w:header=\"709\" w:footer=\"709\" w:gutter=\"567\"/>";
	private static final String NO_GUTTER =
			"<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"851\""
			+ " w:header=\"709\" w:footer=\"709\" w:gutter=\"0\"/>";

	private void gutterMovesTheTextRight(int flags) throws Exception {
		int[] with = bodyRect(areaTree(pkg(GUTTER), flags));
		int[] without = bodyRect(areaTree(pkg(NO_GUTTER), flags));

		assertNear("the gutter is added to the left margin", 70900, with[0], 100);
		assertNear("... and the column is that much narrower", without[2] - 28350, with[2], 100);
	}

	@Test
	public void gutterVisitor() throws Exception {
		gutterMovesTheTextRight(Docx4J.FLAG_NONE);
	}

	@Test
	public void gutterXslt() throws Exception {
		gutterMovesTheTextRight(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	// ------------------------------------------------------------- a single narrow w:col

	/** the margin box is 11906 - 2*1440 = 9026tw (451.3pt); the column is 8640tw (432pt) */
	private static final String NARROW_COL =
			"<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\""
			+ " w:header=\"709\" w:footer=\"709\"/>"
			+ "<w:cols w:space=\"720\" w:equalWidth=\"0\"><w:col w:w=\"8640\"/></w:cols>";

	private void singleNarrowColumn(int flags) throws Exception {
		int[] rect = bodyRect(areaTree(pkg(NARROW_COL), flags));
		assertNear("the text starts at the left margin", 72000, rect[0], 100);
		assertNear("the text column is the w:col width, not the margin box", 432000, rect[2], 200);
	}

	@Test
	public void singleNarrowColumnVisitor() throws Exception {
		singleNarrowColumn(Docx4J.FLAG_NONE);
	}

	@Test
	public void singleNarrowColumnXslt() throws Exception {
		singleNarrowColumn(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
