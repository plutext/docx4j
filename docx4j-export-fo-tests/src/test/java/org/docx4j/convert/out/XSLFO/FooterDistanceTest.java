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
 * The mirror of {@link HeaderDistanceTest}: Word ends the body at the bottom margin and
 * pulls it up only where the footer reaches further - where the footer distance
 * ({@code w:pgMar/@w:footer}) plus the footer's own height is more than
 * {@code w:pgMar/@w:bottom}.  Where there is no footer part at all there is nothing to
 * reserve, and the footer distance alone must not shorten the body.
 *
 * <p>Measured on a real document whose 44 {@code w:sectPr} all say {@code w:bottom="0"}
 * with {@code w:footer="720"} and which has no {@code footerReference}: Word's body runs
 * to the foot of the A4 page (841.9pt) and puts each section's last line at y=827.3-828.2,
 * while docx4j reserved the 36pt footer distance and ended the body at 805.9 - so 21 of
 * the 22 sections spilled their last line onto a page of its own, and Word's 24 pages
 * came out as 44.</p>
 *
 * @since 17.0.6
 */
public class FooterDistanceTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** A4 portrait, bottom margin 0, footer distance 720tw = 36pt, and no footer part. */
	private static final String SECT_PR =
			"<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"0\" w:left=\"1440\""
			+ " w:header=\"720\" w:footer=\"720\"/></w:sectPr>";

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>first line of the body</w:t></w:r></w:p>"
				+ SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	/** The bottom edge (y + height, in millipoints) of the body region's viewport. */
	private static int bodyBottom(org.w3c.dom.Document areaTree) {
		NodeList regions = areaTree.getElementsByTagName("regionBody");
		assertTrue("no regionBody", regions.getLength() > 0);
		Element viewport = (Element) regions.item(0).getParentNode();
		String rect = viewport.getAttribute("rect");
		assertNotNull(rect);
		String[] parts = rect.trim().split("\\s+");
		return Integer.parseInt(parts[1]) + Integer.parseInt(parts[3]);
	}

	private void theBodyRunsToTheFootOfThePage(int flags) throws Exception {
		int bottom = bodyBottom(areaTree(pkg(), flags));
		// A4 is 16838tw = 841.9pt; w:bottom is 0, and there is no footer to reserve for
		assertTrue("the body ends at " + bottom / 1000.0 + "pt, not the page's own 841.9pt:"
				+ " w:footer was reserved although the document has no footer part",
				Math.abs(bottom - 841900) < 1500);
	}

	@Test
	public void visitor() throws Exception {
		theBodyRunsToTheFootOfThePage(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		theBodyRunsToTheFootOfThePage(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
