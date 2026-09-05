package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * Word gives no line to the paragraph mark which carries a section break, where that
 * paragraph is empty: the mark is all it is.  docx4j put an empty fo:block at the end
 * of the section's flow instead, which cost a line height at every mid-page break and,
 * where the block did not fit on the section's last page, made FOP start a page for it
 * - a page carrying only the running header.  Measured over a real-document corpus,
 * four documents gained one to three such pages each, and one gained twenty lines.
 *
 * <p>A paragraph which has content of its own is still rendered, and so is one which
 * is all the section has (a flow with no block is invalid FO).</p>
 *
 * @since 17.0.5
 */
public class SectionBreakParagraphMarkTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	/** A4 portrait, 2cm margins. */
	private static final String PG =
			"<w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1134\" w:right=\"1134\" w:bottom=\"1134\" w:left=\"1134\"/>";

	private static WordprocessingMLPackage pkg(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** the fo:blocks of the n-th flow which are neither containers nor list machinery */
	private static int blocksInFlow(org.w3c.dom.Document doc, int flow) {
		org.w3c.dom.NodeList flows = doc.getElementsByTagNameNS(FO, "flow");
		assertTrue("flow " + flow + " of " + flows.getLength(), flows.getLength() > flow);
		org.w3c.dom.Element f = (org.w3c.dom.Element) flows.item(flow);
		return f.getElementsByTagNameNS(FO, "block").getLength();
	}

	private static final String EMPTY_BREAK =
			"<w:p><w:pPr><w:sectPr>" + PG + "</w:sectPr></w:pPr></w:p>";

	private static String p(String text) {
		return "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	// ----------------------------------------------------------- an empty mark

	private void emptyMarkProducesNoBlock(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(pkg(
				p("first section, one paragraph") + EMPTY_BREAK
				+ p("second section, one paragraph")
				+ "<w:sectPr>" + PG + "</w:sectPr>"), flags);
		assertEquals(2, doc.getElementsByTagNameNS(FO, "page-sequence").getLength());
		assertEquals("the section's one paragraph, and no empty block after it",
				1, blocksInFlow(doc, 0));
		assertEquals(1, blocksInFlow(doc, 1));
	}

	@Test
	public void visitor() throws Exception {
		emptyMarkProducesNoBlock(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		emptyMarkProducesNoBlock(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	// ------------------------------------------------------- a mark with content

	private void aMarkWithContentIsKept(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(pkg(
				p("first section, one paragraph")
				+ "<w:p><w:pPr><w:sectPr>" + PG + "</w:sectPr></w:pPr>"
				+ "<w:r><w:t>and this one ends it</w:t></w:r></w:p>"
				+ p("second section")
				+ "<w:sectPr>" + PG + "</w:sectPr>"), flags);
		assertEquals(2, blocksInFlow(doc, 0));
	}

	@Test
	public void nonEmptyMarkKeptVisitor() throws Exception {
		aMarkWithContentIsKept(Docx4J.FLAG_NONE);
	}

	@Test
	public void nonEmptyMarkKeptXslt() throws Exception {
		aMarkWithContentIsKept(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	// ------------------------------------------------------------- and it lays out

	/**
	 * The whole point: a section whose content exactly fills its page does not spill
	 * onto a second one.  Twenty-five lines of 11pt text fit the A4 body here with
	 * room to spare, and the section break adds no line of its own.
	 */
	@Test
	public void noExtraLine() throws Exception {
		StringBuilder body = new StringBuilder();
		for (int i = 0; i < 25; i++) body.append(p("line " + i));
		body.append(EMPTY_BREAK).append(p("second section"));
		body.append("<w:sectPr>").append(PG).append("</w:sectPr>");

		assertEquals(26, lineCount(areaTree(pkg(body.toString()), Docx4J.FLAG_NONE)));
	}

	/** A section which is nothing but the break paragraph still renders something. */
	@Test
	public void aSectionWithNothingElseStillRenders() throws Exception {
		org.w3c.dom.Document doc = fo(pkg(
				EMPTY_BREAK + p("second section") + "<w:sectPr>" + PG + "</w:sectPr>"), Docx4J.FLAG_NONE);
		assertEquals(2, doc.getElementsByTagNameNS(FO, "page-sequence").getLength());
		assertEquals(1, blocksInFlow(doc, 0));
	}
}
