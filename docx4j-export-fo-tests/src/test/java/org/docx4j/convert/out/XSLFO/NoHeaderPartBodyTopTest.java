package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Document;
import org.docx4j.wml.FooterReference;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.SectPr;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Where <code>w:titlePg</code> (or <code>w:evenAndOddHeaders</code>) asks for a header the
 * document has no part for, docx4j invents an empty one so the page master has a region to
 * hang the static content on.  Its single empty paragraph measured a line box, and that
 * line box was reserved above the body - which Word does not do for a header that is not
 * in the document at all.
 *
 * <p>Measured (CR-001 &#xa7;7) on a document with no header part and no
 * <code>w:headerReference</code>, <code>w:pgMar w:top="432"</code> (21.6pt) and
 * <code>w:header="706"</code> (35.3pt): Word's body top is <code>w:top</code> = 21.6 where
 * ours was 35.3 + 13.799 = 49.1, and every line and the logo came out +26.5 to +27.5pt
 * low.</p>
 *
 * @since 17.1.0
 */
public class NoHeaderPartBodyTopTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";
	private static final ObjectFactory factory = Context.getWmlObjectFactory();

	/** w:header (35.3pt) is bigger than w:pgMar w:top (21.6pt), which is where it showed */
	private static final String PG_MAR =
			"<w:pgMar w:top=\"432\" w:right=\"1440\" w:bottom=\"432\" w:left=\"1440\""
			+ " w:header=\"706\" w:footer=\"706\"/>";

	/** w:titlePg with only a first-page *footer*: the first-page header is docx4j's dummy */
	private static WordprocessingMLPackage pkg() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>first line of the body</w:t></w:r></w:p>"
				+ "<w:sectPr><w:titlePg/><w:pgSz w:w=\"11906\" w:h=\"16838\"/>" + PG_MAR + "</w:sectPr>"
				+ "</w:body></w:document>"));

		FooterPart fp = new FooterPart(new PartName("/word/footer1.xml"));
		fp.setPackage(pkg);
		fp.setJaxbElement((Ftr) XmlUtils.unmarshalString(
				"<w:ftr " + W + "><w:p><w:r><w:t>footer</w:t></w:r></w:p></w:ftr>", Context.jc, Ftr.class));
		Relationship fr = pkg.getMainDocumentPart().addTargetPart(fp);

		SectPr sectPr = pkg.getMainDocumentPart().getJaxbElement().getBody().getSectPr();
		FooterReference footerReference = factory.createFooterReference();
		footerReference.setId(fr.getId());
		footerReference.setType(HdrFtrRef.FIRST);
		sectPr.getEGHdrFtrReferences().add(footerReference);

		return pkg;
	}

	/** the y offset of the first lineArea of the body, in millipoints */
	private static int firstBodyLineY(org.w3c.dom.Document areaTree) {
		NodeList regions = areaTree.getElementsByTagName("regionBody");
		assertTrue("no regionBody", regions.getLength() > 0);
		Element viewport = (Element) regions.item(0).getParentNode();
		String[] rect = viewport.getAttribute("rect").trim().split("\\s+");
		return Integer.parseInt(rect[1]);
	}

	private void check(int flags) throws Exception {
		org.w3c.dom.Document areaTree = areaTree(pkg(), flags);
		assertEquals("Word starts the body at w:pgMar w:top where there is no header part",
				21600, firstBodyLineY(areaTree));
	}

	@Test
	public void visitorPathway() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xsltPathway() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
