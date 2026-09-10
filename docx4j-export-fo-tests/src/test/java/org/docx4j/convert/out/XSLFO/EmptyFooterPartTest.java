package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertTrue;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Document;
import org.docx4j.wml.Ftr;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * An empty footer <em>part</em> is not the same as no footer at all: Word still stops
 * the body above {@code w:pgMar/@w:footer}.
 *
 * <p>{@link FooterDistanceTest} covers the other side - with no {@code footerReference}
 * the footer distance reserves nothing - and the rule that a part of at most one
 * paragraph painting nothing "reserves nothing" was over-fired onto this case.  Measured
 * against Word 365 on an A4 document whose three footer parts are each a single empty
 * {@code w:p}, with {@code w:pgMar w:bottom="274"} (13.7pt) and {@code w:footer="720"}
 * (36pt): Word's body ends at y=792.5 - the footer distance plus the empty footer's own
 * 13.43pt line - and puts the next block on the following page, where the bottom margin
 * alone (a body bottom of 828.25) kept it.  The clamp the "reserves nothing" rule really
 * needed was against an <em>absurd</em> {@code w:footer}: the document it was measured on
 * states {@code w:footer="5811"} (290.55pt, a third of the page) and Word ignores it
 * entirely.  15 documents of three corpora have an empty footer part.</p>
 *
 * @since 17.1.0
 */
public class EmptyFooterPartTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String flagName(int flag) {
		return flag == Docx4J.FLAG_EXPORT_PREFER_XSL ? "XSL" : "visitor";
	}

	/** A4 portrait, bottom margin 13.7pt, and the given footer distance in twips. */
	private static WordprocessingMLPackage pkg(int footerTwips) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		FooterPart footer = new FooterPart(new PartName("/word/footer1.xml"));
		footer.setJaxbElement((Ftr)XmlUtils.unmarshalString(
				"<w:ftr " + W + "><w:p/></w:ftr>", org.docx4j.jaxb.Context.jc, Ftr.class));
		Relationship rel = pkg.getMainDocumentPart().addTargetPart(footer);
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<w:body><w:p><w:r><w:t>first line of the body</w:t></w:r></w:p>"
				+ "<w:sectPr>"
				+ "<w:footerReference w:type=\"default\" r:id=\"" + rel.getId() + "\"/>"
				+ "<w:pgSz w:w=\"11907\" w:h=\"16839\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"274\" w:left=\"1440\""
				+ " w:header=\"720\" w:footer=\"" + footerTwips + "\"/>"
				+ "</w:sectPr></w:body></w:document>"));
		return pkg;
	}

	/** The bottom edge (y + height, in millipoints) of the body region's viewport. */
	private static int bodyBottom(org.w3c.dom.Document areaTree) {
		NodeList regions = areaTree.getElementsByTagName("regionBody");
		assertTrue("no regionBody", regions.getLength() > 0);
		Element viewport = (Element) regions.item(0).getParentNode();
		String[] rect = viewport.getAttribute("rect").trim().split("\\s+");
		return Integer.parseInt(rect[1]) + Integer.parseInt(rect[3]);
	}

	/** w:footer=720 (36pt) on an 841.95pt page: the body stops at the distance plus the
	 *  empty footer's own line, which is what Word was measured doing (792.5 there, with
	 *  a 13.43pt Footer-style line; here the footer paragraph is Normal, whose Calibri
	 *  11pt line at w:line="276" is 15.44pt, so 790.5).  Until 17.1.1 only the distance
	 *  was reserved (805.95), and a landscape corpus document whose eight trailing empty
	 *  paragraphs Word puts on a second page kept them on its first.  @since 17.1.1 */
	@Test
	public void theBodyStopsAtTheFooterDistancePlusItsLine() throws Exception {
		for (int flag : FLAGS) {
			int bottom = bodyBottom(areaTree(pkg(720), flag));
			assertTrue(flagName(flag) + ": the body ends at " + bottom / 1000.0
					+ "pt, not at 790.5 (36pt footer distance + the empty footer's 15.44pt line)",
					Math.abs(bottom - 790510) < 1500);
		}
	}

	/** w:footer=5811 (290.55pt, a third of the page) is absurd and Word ignores it: the
	 *  body runs to the 13.7pt bottom margin. */
	@Test
	public void anAbsurdFooterDistanceIsIgnored() throws Exception {
		for (int flag : FLAGS) {
			int bottom = bodyBottom(areaTree(pkg(5811), flag));
			assertTrue(flagName(flag) + ": the body ends at " + bottom / 1000.0
					+ "pt, not near the page's own 828.25pt",
					Math.abs(bottom - 828250) < 2000);
		}
	}
}
