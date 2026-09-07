package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
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
import org.w3c.dom.NodeList;

/**
 * A {@code PAGE} field whose {@code w:fldChar begin}, {@code w:instrText} and
 * {@code w:fldChar separate} share <b>one run</b>, which is how Word commonly writes a
 * page number in a footer - and, wrapped in the "Page Numbers (Bottom of Page)" building
 * block, inside a {@code w:sdt} as well.
 *
 * <p>{@code FieldsCombiner} looked at one {@code w:fldChar} per run, so it saw the BEGIN
 * and never the SEPARATE: the field was never combined into a {@code w:fldSimple}, the
 * runs were emitted as the field's <em>cached result</em>, and the FO held no
 * {@code fo:page-number} at all.  Measured: a 76-page document printed
 * "P&aacute;gina 73 de 76" on every page where Word prints 2 ... 76, and a 23-page one
 * printed "1" on all 23 - one wrong line on every page of the document.  Twelve documents
 * of one corpus and 52 across three.</p>
 *
 * @since 17.1.0
 */
public class PageFieldInOneRunTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final ObjectFactory factory = Context.getWmlObjectFactory();

	/** begin + instrText + separate in a single run, then the cached result, then end */
	private static String pageFieldInOneRun() {
		return "<w:r><w:fldChar w:fldCharType=\"begin\"/>"
				+ "<w:instrText xml:space=\"preserve\"> PAGE   \\* MERGEFORMAT </w:instrText>"
				+ "<w:fldChar w:fldCharType=\"separate\"/></w:r>"
				+ "<w:r><w:t>1</w:t></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>";
	}

	private static WordprocessingMLPackage pkg(boolean inSdt) throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>page one</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:br w:type=\"page\"/></w:r><w:r><w:t>page two</w:t></w:r></w:p>"
				+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\""
				+ " w:header=\"709\" w:footer=\"709\"/></w:sectPr>"
				+ "</w:body></w:document>"));

		String p = "<w:p><w:r><w:t xml:space=\"preserve\">Page </w:t></w:r>" + pageFieldInOneRun() + "</w:p>";
		String inner = inSdt
				? "<w:sdt><w:sdtPr><w:id w:val=\"1\"/><w:docPartObj>"
					+ "<w:docPartGallery w:val=\"Page Numbers (Bottom of Page)\"/><w:docPartUnique/>"
					+ "</w:docPartObj></w:sdtPr><w:sdtContent>" + p + "</w:sdtContent></w:sdt>"
				: p;

		FooterPart fp = new FooterPart(new PartName("/word/footer1.xml"));
		fp.setPackage(pkg);
		fp.setJaxbElement((Ftr) XmlUtils.unmarshalString(
				"<w:ftr " + W + ">" + inner + "</w:ftr>", Context.jc, Ftr.class));
		Relationship fr = pkg.getMainDocumentPart().addTargetPart(fp);

		SectPr sectPr = pkg.getMainDocumentPart().getJaxbElement().getBody().getSectPr();
		FooterReference footerReference = factory.createFooterReference();
		footerReference.setId(fr.getId());
		footerReference.setType(HdrFtrRef.DEFAULT);
		sectPr.getEGHdrFtrReferences().add(footerReference);

		return pkg;
	}

	private static org.w3c.dom.Document fo(boolean inSdt, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg(inSdt));
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private void thePageNumberIsLive(boolean inSdt, int flags) throws Exception {

		org.w3c.dom.Document doc = fo(inSdt, flags);
		NodeList pageNumbers = doc.getElementsByTagNameNS(FO, "page-number");
		assertTrue("the footer's PAGE field did not become an fo:page-number"
				+ (inSdt ? " (inside a w:sdt)" : ""), pageNumbers.getLength() > 0);

		// ... and the cached "1" is not painted alongside it
		assertEquals("the cached field result was painted as well as the live number",
				-1, doc.getDocumentElement().getTextContent().indexOf("Page 1"));
	}

	@Test
	public void plainFooterVisitor() throws Exception {
		thePageNumberIsLive(false, Docx4J.FLAG_NONE);
	}

	@Test
	public void plainFooterXslt() throws Exception {
		thePageNumberIsLive(false, Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void inContentControlVisitor() throws Exception {
		thePageNumberIsLive(true, Docx4J.FLAG_NONE);
	}

	@Test
	public void inContentControlXslt() throws Exception {
		thePageNumberIsLive(true, Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** And page 2's footer really says 2. */
	@Test
	public void theSecondPageSaysTwo() throws Exception {

		org.w3c.dom.Document areaTree = areaTree(pkg(false), Docx4J.FLAG_NONE);
		NodeList viewports = areaTree.getElementsByTagName("pageViewport");
		assertEquals("two pages", 2, viewports.getLength());
		String secondPage = viewports.item(1).getTextContent();
		assertTrue("page 2's footer should say 'Page 2', not the cached 'Page 1': " + secondPage,
				secondPage.contains("2"));
	}
}
