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
 * Word starts the body at the top margin, and pushes it down only where the header
 * reaches further - where the header distance (w:pgMar/@w:header) plus the header's
 * own height is more than the top margin.  A document whose header distance is larger
 * than its top margin and whose header is empty must therefore start at the top
 * margin; until 17.0.5 the page master's margin-top was the header distance
 * regardless, so the whole body was pushed down by the difference (measured: 13.9pt
 * on every line of a real document with w:top=426 and w:header=708).
 *
 * @since 17.0.5
 */
public class HeaderDistanceTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** A4 landscape, top margin 426tw = 21.3pt, header distance 708tw = 35.4pt. */
	private static final String SECT_PR =
			"<w:sectPr><w:pgSz w:w=\"16838\" w:h=\"11906\" w:orient=\"landscape\"/>"
			+ "<w:pgMar w:top=\"426\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\""
			+ " w:header=\"708\" w:footer=\"708\"/></w:sectPr>";

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>first line of the body</w:t></w:r></w:p>"
				+ SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	/** The y (in millipoints from the page top) of the first line of the body region. */
	private static int firstBodyLineY(org.w3c.dom.Document areaTree) {
		NodeList regions = areaTree.getElementsByTagName("regionBody");
		assertTrue("no regionBody", regions.getLength() > 0);
		// the region viewport's rect is "x y width height"; the body starts at y
		Element viewport = (Element) regions.item(0).getParentNode();
		String rect = viewport.getAttribute("rect");
		assertNotNull(rect);
		return Integer.parseInt(rect.trim().split("\\s+")[1]);
	}

	private void bodyStartsAtTheTopMargin(int flags) throws Exception {
		org.w3c.dom.Document areaTree = areaTree(pkg(), flags);
		int y = firstBodyLineY(areaTree);
		assertNear("the top margin (426tw), not the header distance (708tw)", 21300, y, 60);
	}

	@Test
	public void visitor() throws Exception {
		bodyStartsAtTheTopMargin(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		bodyStartsAtTheTopMargin(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	private static void assertNear(String message, int expected, int actual, int tolerance) {
		assertTrue(message + ": expected " + expected + " +/- " + tolerance + " but was " + actual,
				Math.abs(expected - actual) <= tolerance);
	}
}
