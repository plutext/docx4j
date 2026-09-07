package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A section whose {@code w:cols} declares a single {@code w:col} uses that column's
 * width for its text, whether it is narrower <em>or wider</em> than the margin box.
 *
 * <p>The narrow half landed in 17.1.0 and took a corpus document from 0.557 to 0.951.
 * Where the declared column is <em>wider</em>, Word lets the section overhang the right
 * margin: measured on a 595.35pt page with 72pt margins (a 451.35pt margin box) whose
 * {@code <w:cols w:equalWidth="0"><w:col w:w="9560"/>} declares 478pt, Word centres a
 * heading on x=311.15 where docx4j centred it on 297.5, and ends a right-tabbed line at
 * 535.4 against our 508.0 - a flat -27.4pt, which is 550 - 522.6 - so Word's page 2 was
 * absorbed into our page 1.  14 documents of three corpora declare a single
 * {@code w:col} more than 1% from their margin box.</p>
 *
 * @since 17.1.0
 */
public class SingleColumnWidthTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String flagName(int flag) {
		return flag == Docx4J.FLAG_EXPORT_PREFER_XSL ? "XSL" : "visitor";
	}

	/** A4 portrait, 72pt margins: a 451.35pt margin box (9027 twips). */
	private static WordprocessingMLPackage pkg(String cols) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>one line of body text</w:t></w:r></w:p>"
				+ "<w:sectPr><w:pgSz w:w=\"11907\" w:h=\"16839\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\""
				+ " w:header=\"720\" w:footer=\"720\"/>"
				+ cols
				+ "</w:sectPr></w:body></w:document>"));
		return pkg;
	}

	/** The body region's width in millipoints. */
	private static int bodyWidth(org.w3c.dom.Document areaTree) {
		NodeList regions = areaTree.getElementsByTagName("regionBody");
		assertTrue("no regionBody", regions.getLength() > 0);
		Element viewport = (Element) regions.item(0).getParentNode();
		String[] rect = viewport.getAttribute("rect").trim().split("\\s+");
		return Integer.parseInt(rect[2]);
	}

	@Test
	public void aWiderSingleColumnWidensTheText() throws Exception {
		for (int flag : FLAGS) {
			// 9560 twips = 478pt, 26.65pt wider than the 451.35pt margin box
			int wide = bodyWidth(areaTree(
					pkg("<w:cols w:space=\"720\" w:equalWidth=\"0\"><w:col w:w=\"9560\"/></w:cols>"), flag));
			assertTrue(flagName(flag) + ": the text column is " + wide / 1000.0
					+ "pt, not the declared 478pt", Math.abs(wide - 478000) < 1500);
		}
	}

	@Test
	public void aNarrowerSingleColumnNarrowsTheText() throws Exception {
		for (int flag : FLAGS) {
			// 8640 twips = 432pt
			int narrow = bodyWidth(areaTree(
					pkg("<w:cols w:space=\"720\" w:equalWidth=\"0\"><w:col w:w=\"8640\"/></w:cols>"), flag));
			assertTrue(flagName(flag) + ": the text column is " + narrow / 1000.0
					+ "pt, not the declared 432pt", Math.abs(narrow - 432000) < 1500);
		}
	}

	/** Word's own rounding of a single column of the full width is common: under 1% of
	 *  the margin box the declared width is ignored. */
	@Test
	public void aRoundedFullWidthColumnChangesNothing() throws Exception {
		for (int flag : FLAGS) {
			int plain = bodyWidth(areaTree(pkg(""), flag));
			int rounded = bodyWidth(areaTree(
					pkg("<w:cols w:space=\"720\"><w:col w:w=\"9020\"/></w:cols>"), flag));
			assertEquals(flagName(flag) + ": a 7-twip rounding changed the text column",
					plain, rounded);
		}
	}
}
