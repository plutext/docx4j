package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertTrue;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.FooterReference;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.SectPr;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A header's or footer's height, as Word measures it, includes the space-after of its
 * last paragraph.  XSL-FO drops space at the end of a reference area, so the area-tree
 * pre-pass which measures the two regions ({@code FOPAreaTreeHelper}) measured one
 * space-after short, and &#xa7;7's {@code max(top margin, header distance + header
 * height)} then started the body that much too high and ended it that much too low.
 *
 * <p>Measured on a document with {@code w:pgMar w:top="1440" w:header="709"} whose header
 * and footer each hold two paragraphs 10pt apart: our {@code region-before} extent was
 * 58.867pt and {@code region-after} 32.362pt, each exactly the blocks' line boxes plus
 * the <em>middle</em> 10pt.  Word's first body line is at y=113.6 where ours was 102.3,
 * and Word's footer line at 722.6 where ours was 731.9 - 20.6pt more body on every one of
 * 311 pages.</p>
 *
 * @since 17.1.0
 */
public class HeaderFooterExtentSpacingTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";
	private static final ObjectFactory factory = Context.getWmlObjectFactory();

	/** The header distance (35.45pt) is smaller than the top margin (72pt), so the body
	 *  only moves at all once the header is tall enough - which is what we are measuring. */
	private static final String PG_MAR =
			"<w:pgMar w:top=\"720\" w:right=\"1440\" w:bottom=\"720\" w:left=\"1440\""
			+ " w:header=\"709\" w:footer=\"709\"/>";

	private static String para(int afterTwips) {
		return "<w:p><w:pPr><w:spacing w:before=\"0\" w:after=\"" + afterTwips + "\"/></w:pPr>"
				+ "<w:r><w:t>running text</w:t></w:r></w:p>";
	}

	private static WordprocessingMLPackage pkg(int lastParaAfterTwips) throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((org.docx4j.wml.Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>first line of the body</w:t></w:r></w:p>"
				+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>" + PG_MAR + "</w:sectPr>"
				+ "</w:body></w:document>"));

		// two paragraphs each: the first's space-after is inside the region already, the
		// second's is the one FO drops at the end of it
		String inner = para(200) + para(lastParaAfterTwips);

		HeaderPart hp = new HeaderPart(new PartName("/word/header1.xml"));
		hp.setPackage(pkg);
		hp.setJaxbElement((Hdr) XmlUtils.unmarshalString(
				"<w:hdr " + W + ">" + inner + "</w:hdr>", Context.jc, Hdr.class));
		Relationship hr = pkg.getMainDocumentPart().addTargetPart(hp);

		FooterPart fp = new FooterPart(new PartName("/word/footer1.xml"));
		fp.setPackage(pkg);
		fp.setJaxbElement((Ftr) XmlUtils.unmarshalString(
				"<w:ftr " + W + ">" + inner + "</w:ftr>", Context.jc, Ftr.class));
		Relationship fr = pkg.getMainDocumentPart().addTargetPart(fp);

		SectPr sectPr = pkg.getMainDocumentPart().getJaxbElement().getBody().getSectPr();
		HeaderReference headerReference = factory.createHeaderReference();
		headerReference.setId(hr.getId());
		headerReference.setType(HdrFtrRef.DEFAULT);
		sectPr.getEGHdrFtrReferences().add(headerReference);
		FooterReference footerReference = factory.createFooterReference();
		footerReference.setId(fr.getId());
		footerReference.setType(HdrFtrRef.DEFAULT);
		sectPr.getEGHdrFtrReferences().add(footerReference);

		return pkg;
	}

	/** the millipoint extent of the named region's viewport */
	private static int extent(org.w3c.dom.Document areaTree, String regionName) {
		NodeList regions = areaTree.getElementsByTagName(regionName);
		assertTrue("no " + regionName, regions.getLength() > 0);
		Element viewport = (Element) regions.item(0).getParentNode();
		return Integer.parseInt(viewport.getAttribute("rect").trim().split("\\s+")[3]);
	}

	private void lastSpaceAfterCounts(int flags) throws Exception {

		org.w3c.dom.Document without = areaTree(pkg(0), flags);
		org.w3c.dom.Document with = areaTree(pkg(200), flags); // 200tw = 10pt

		int headerGrowth = extent(with, "regionBefore") - extent(without, "regionBefore");
		int footerGrowth = extent(with, "regionAfter") - extent(without, "regionAfter");

		assertNear("the header's last paragraph's 10pt space-after is part of its height",
				10000, headerGrowth, 300);
		assertNear("... and so is the footer's", 10000, footerGrowth, 300);
	}

	@Test
	public void visitor() throws Exception {
		lastSpaceAfterCounts(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		lastSpaceAfterCounts(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	private static void assertNear(String message, int expected, int actual, int tolerance) {
		assertTrue(message + ": expected " + expected + " +/- " + tolerance + " but was " + actual,
				Math.abs(expected - actual) <= tolerance);
	}
}
