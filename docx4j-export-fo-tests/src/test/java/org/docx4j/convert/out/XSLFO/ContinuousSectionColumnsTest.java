package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.ConversionSectionWrappers;
import org.docx4j.convert.out.common.wrappers.ConversionSectionWrapperFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.docx4j.wml.SectPr;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A page-sequence built of merged continuous sections carries one set of page margins, and
 * the other parts carry the difference as indents - which puts every line where Word puts
 * it, because a part's measure is the region body less its own indents. The region body
 * still bounds one thing an indent cannot move: the <b>columns</b>.
 *
 * <p>So the masters are built on the multi-column part's margins. Since 17.1.0 that held
 * only where the multi-column part's text column was the <em>wider</em>; a narrower one was
 * just as wrong. Measured (CR-001 batch 43, M53) on a corpus document whose section 1 is one
 * column at 72pt margins and whose continuous section 2 is two columns at 85.05pt: the
 * masters were built on 72pt, so FOP divided a 468pt region into two 216pt columns where
 * Word divides its own 441.9pt region into two of <b>202.95</b>, and the 13.05pt indents
 * then came off each column rather than once off the page - a 189.9pt measure, column 2 at
 * 337.05 against Word's 324.1, and every line of the two-column run re-wrapped.</p>
 *
 * @since 17.1.1
 */
public class ContinuousSectionColumnsTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";

	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	/** US Letter, 612 x 792pt, as the corpus document is */
	private static final String PG_SZ = "<w:pgSz w:w=\"12240\" w:h=\"15840\"/>";

	private static String para(String text) {
		return "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	/**
	 * @param oneColumnMargin twips, section 1's left and right margin (one column)
	 * @param twoColumnMargin twips, the continuous section 2's (two columns)
	 */
	private static org.w3c.dom.Document fo(int oneColumnMargin, int twoColumnMargin, int flags)
			throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ para("Section one, one column.")
				+ "<w:p><w:pPr><w:sectPr><w:type w:val=\"continuous\"/>" + PG_SZ
				+ "<w:pgMar w:top=\"1440\" w:right=\"" + oneColumnMargin + "\" w:bottom=\"1440\""
				+ " w:left=\"" + oneColumnMargin + "\" w:header=\"709\" w:footer=\"709\"/>"
				+ "</w:sectPr></w:pPr></w:p>"
				+ para("Section two, two columns.")
				+ "<w:sectPr><w:type w:val=\"continuous\"/>"
				+ "<w:cols w:num=\"2\" w:space=\"720\"/>" + PG_SZ
				+ "<w:pgMar w:top=\"1440\" w:right=\"" + twoColumnMargin + "\" w:bottom=\"1440\""
				+ " w:left=\"" + twoColumnMargin + "\" w:header=\"709\" w:footer=\"709\"/>"
				+ "</w:sectPr></w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** the margin-left of the first simple-page-master that carries a two-column region */
	private static double twoColumnMasterMarginPt(org.w3c.dom.Document doc) {
		NodeList masters = doc.getElementsByTagNameNS(FO_NS, "simple-page-master");
		for (int i = 0; i < masters.getLength(); i++) {
			Element m = (Element) masters.item(i);
			NodeList regions = m.getElementsByTagNameNS(FO_NS, "region-body");
			for (int j = 0; j < regions.getLength(); j++) {
				if ("2".equals(((Element) regions.item(j)).getAttribute("column-count"))) {
					return pt(m.getAttribute("margin-left"));
				}
			}
		}
		return -1;
	}

	private static double pt(String v) {
		if (v == null || v.length() == 0) return -1;
		if (v.endsWith("pt")) return Double.parseDouble(v.substring(0, v.length() - 2));
		if (v.endsWith("in")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72d;
		if (v.endsWith("mm")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72d / 25.4d;
		throw new IllegalArgumentException(v);
	}

	/**
	 * The corpus shape: the two-column section's margins are the <b>narrower</b> text
	 * column (85.05pt against 72pt), and the masters must still be built on them, so the
	 * columns come out at Word's (612 - 170.1 - 36) / 2 = 202.95pt.
	 */
	private void checkNarrower(int flags) throws Exception {
		double margin = twoColumnMasterMarginPt(fo(1440, 1701, flags));
		assertEquals("the masters take the two-column section's 85.05pt, not section 1's 72",
				85.05, margin, 0.05);
		double columns = (612 - 2 * margin - 36) / 2;
		assertEquals("so each column is Word's 202.95pt", 202.95, columns, 0.1);
	}

	/**
	 * And the case 17.1.0 measured - the two-column section's text column is the wider -
	 * still behaves as it did.
	 */
	private void checkWider(int flags) throws Exception {
		double margin = twoColumnMasterMarginPt(fo(1701, 1440, flags));
		assertEquals("the masters take the two-column section's 72pt", 72.0, margin, 0.05);
	}

	@Test
	public void narrowerTwoColumnSectionVisitorPathway() throws Exception {
		checkNarrower(Docx4J.FLAG_NONE);
	}

	@Test
	public void narrowerTwoColumnSectionXsltPathway() throws Exception {
		checkNarrower(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void widerTwoColumnSectionIsUnchanged() throws Exception {
		checkWider(Docx4J.FLAG_NONE);
		checkWider(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/**
	 * Only the left and right margins come from the two-column part. The top, bottom,
	 * header and footer distances are the <b>first</b> part's, because that is the one
	 * Word starts the page with and a page master has one before-edge: nothing can carry
	 * a vertical difference as an indent the way the horizontal difference is carried.
	 * Until 17.1.1 the whole w:pgMar came from the reference part, which pushed one
	 * corpus document's first page 14.1pt down the page for a horizontal change that
	 * moved no line at all.
	 *
	 * <p>The page dimensions are read from the section wrapper rather than from the FO,
	 * because the vertical ones do not reach the page master as they are: the region
	 * before and after take their extent from a measuring pre-pass at render time
	 * (FOPAreaTreeHelper), and the master's own margin-top is the header distance.</p>
	 */
	@Test
	public void verticalMarginsStayWithTheFirstPart() throws Exception {
		// section 1: one column, 72pt margins, 72pt top, 35.45pt header
		// section 2: two columns, 85.05pt margins, 30pt top, 20pt header
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ para("Section one, one column.")
				+ "<w:p><w:pPr><w:sectPr><w:type w:val=\"continuous\"/>" + PG_SZ
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\""
				+ " w:header=\"709\" w:footer=\"709\"/></w:sectPr></w:pPr></w:p>"
				+ para("Section two, two columns.")
				+ "<w:sectPr><w:type w:val=\"continuous\"/>"
				+ "<w:cols w:num=\"2\" w:space=\"720\"/>" + PG_SZ
				+ "<w:pgMar w:top=\"600\" w:right=\"1701\" w:bottom=\"800\" w:left=\"1701\""
				+ " w:header=\"400\" w:footer=\"400\"/></w:sectPr></w:body></w:document>"));

		ConversionSectionWrappers wrappers =
				ConversionSectionWrapperFactory.process(pkg, false, false);
		assertEquals("the two continuous sections are one page-sequence",
				1, wrappers.getList().size());
		SectPr.PgMar pgMar = wrappers.getList().get(0).getPageDimensions().getPgMar();

		assertEquals("left is the two-column section's", 1701, pgMar.getLeft().intValue());
		assertEquals("right is the two-column section's", 1701, pgMar.getRight().intValue());
		assertEquals("top is the first section's", 1440, pgMar.getTop().intValue());
		assertEquals("bottom is the first section's", 1440, pgMar.getBottom().intValue());
		assertEquals("header distance is the first section's", 709, pgMar.getHeader().intValue());
		assertEquals("footer distance is the first section's", 709, pgMar.getFooter().intValue());
	}

	/** One section with no continuous run at all is untouched: its own margins. */
	@Test
	public void aSingleSectionIsUntouched() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + para("only section")
				+ "<w:sectPr><w:cols w:num=\"2\" w:space=\"720\"/>" + PG_SZ
				+ "<w:pgMar w:top=\"1440\" w:right=\"1701\" w:bottom=\"1440\" w:left=\"1701\""
				+ " w:header=\"709\" w:footer=\"709\"/></w:sectPr></w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, Docx4J.FLAG_NONE);
		org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder()
				.parse(new ByteArrayInputStream(baos.toByteArray()));
		assertTrue("its own 85.05pt margins",
				Math.abs(twoColumnMasterMarginPt(doc) - 85.05) < 0.05);
	}
}
