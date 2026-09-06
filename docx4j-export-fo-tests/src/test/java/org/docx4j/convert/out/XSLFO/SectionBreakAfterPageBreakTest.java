package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A page break at the very end of a section, which is what a document whose sections
 * each end with one looks like: the paragraph holding the break, and the section-break
 * paragraph after it, are the last things in the section.
 *
 * <p>Word does not give that break a page of its own - the section which follows starts
 * one anyway.  Measured on the {@code tab-toc-pageref} probe, whose third section ends
 * with a page-break paragraph and then its section-break paragraph: Word renders 6 pages
 * and opens the next section at the top of page 4, where docx4j had a page 4 holding
 * nothing but the empty block the break had been moved onto (7 pages).  At the end of the
 * <em>document</em> the page is Word's own, which the {@code page-blank} probe measures
 * (it ends in a page break and Word gives it a ninth page), so the break stays there.
 *
 * @since 17.0.6
 */
public class SectionBreakAfterPageBreakTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static String sectPr(String type) {
		return "<w:sectPr>" + (type == null ? "" : "<w:type w:val=\"" + type + "\"/>")
				+ "<w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";
	}

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>section one</w:t></w:r></w:p>"
				// the paragraph whose only content is a page break, then the section break
				+ "<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>"
				+ "<w:p><w:pPr>" + sectPr("nextPage") + "</w:pPr></w:p>"
				+ "<w:p><w:r><w:t>section two</w:t></w:r></w:p>"
				// and the document ends with one, which Word does give a page
				+ "<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>"
				+ sectPr(null)
				+ "</w:body></w:document>"));
		return pkg;
	}

	private void check(int flags) throws Exception {

		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg());
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);

		org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(baos.toByteArray());
		NodeList sequences = doc.getElementsByTagNameNS(FO, "page-sequence");
		assertEquals(2, sequences.getLength());

		assertEquals("the break ending the first section costs no page: the next section starts one",
				0, pageBreaks((Element) sequences.item(0)));
		assertEquals("the break ending the document keeps its page",
				1, pageBreaks((Element) sequences.item(1)));
	}

	/** How many blocks of this page-sequence ask for a page of their own. */
	private static int pageBreaks(Element sequence) {
		int n = 0;
		NodeList blocks = sequence.getElementsByTagNameNS(FO, "block");
		for (int i = 0; i < blocks.getLength(); i++) {
			if ("page".equals(((Element) blocks.item(i)).getAttribute("break-before"))) n++;
		}
		return n;
	}

	@Test
	public void visitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
