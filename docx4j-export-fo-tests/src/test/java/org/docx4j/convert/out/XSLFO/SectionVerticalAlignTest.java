package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
 * w:sectPr/w:vAlign - Word's Page Setup "Vertical alignment" - was ignored, so a title
 * page whose section asks for "center" was laid out at the top of the page.  Measured
 * against Word's own PDF of a 179 page specification whose title section carries
 * &lt;w:vAlign w:val="center"/&gt;: every line of page 1 was 112.5pt above Word's (Word
 * put its first line at y=275.9, docx4j at 163.4).
 *
 * <p>XSL 1.1's display-align on fo:region-body is the equivalent, and it costs nothing
 * on a full page, so it applies to the whole section as Word applies it.</p>
 *
 * @since 17.0.6
 */
public class SectionVerticalAlignTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static String body(String vAlign) {
		return body(vAlign, 0);
	}

	private static String body(String vAlign, int spaceAfter) {
		return "<w:document " + W + "><w:body>"
				+ "<w:p>"
				+ (spaceAfter > 0 ? "<w:pPr><w:spacing w:after=\"" + spaceAfter + "\"/></w:pPr>" : "")
				+ "<w:r><w:t>Title</w:t></w:r></w:p>"
				+ "<w:sectPr>"
				+ (vAlign == null ? "" : "<w:vAlign w:val=\"" + vAlign + "\"/>")
				+ "<w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/>"
				+ "</w:sectPr></w:body></w:document>";
	}

	private org.w3c.dom.Document fo(String vAlign, int flags) throws Exception {
		return fo(vAlign, 0, flags);
	}

	private org.w3c.dom.Document fo(String vAlign, int spaceAfter, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(body(vAlign, spaceAfter)));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	/** the display-align of every fo:region-body, joined */
	private String displayAlign(org.w3c.dom.Document doc) {
		NodeList bodies = doc.getElementsByTagNameNS(FO, "region-body");
		assertTrue("no region-body", bodies.getLength() > 0);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bodies.getLength(); i++) {
			sb.append(((Element)bodies.item(i)).getAttribute("display-align")).append('|');
		}
		return sb.toString();
	}

	private void check(int flags) throws Exception {
		assertEquals("|", displayAlign(fo(null, flags)));
		assertEquals("center|", displayAlign(fo("center", flags)));
		assertEquals("after|", displayAlign(fo("bottom", flags)));
		/* "both" is vertical justification: Word holds the first block at the top of
		 * the text area and the last at its bottom, sharing the slack between them.
		 * XSL-FO has no property for that, so the top - which is the half of it FO can
		 * express - is what the region keeps.  Measured on section-valign-bottom, whose
		 * "both" sections Word opens at y=83.1 like an unaligned one, where
		 * display-align="center" put every line up to 300pt out. */
		assertEquals("no FO equivalent for vertical justification; Word's top is kept",
				"|", displayAlign(fo("both", flags)));
		assertEquals("top is the default", "|", displayAlign(fo("top", flags)));
	}

	/** the space-after.conditionality of the flow's last fo:block */
	private String lastBlockConditionality(org.w3c.dom.Document doc) {
		NodeList blocks = doc.getElementsByTagNameNS(FO, "block");
		assertTrue("no blocks", blocks.getLength() > 0);
		return ((Element) blocks.item(blocks.getLength() - 1)).getAttribute("space-after.conditionality");
	}

	/**
	 * A vertically aligned section counts its last paragraph's space-after as part of
	 * the block it aligns, so a bottom-aligned section's last line sits that much above
	 * the bottom margin.  FO's space-after.conditionality defaults to discard at the end
	 * of a reference area, so FOP dropped it.
	 *
	 * <p>Measured on the {@code section-valign-bottom} probe, which pairs a last
	 * paragraph carrying 24pt of space-after with a control carrying none: Word's
	 * bottom-aligned pages close at y=743.7 and 767.5 - 23.8pt apart - where docx4j put
	 * both at 767.4; its centre-aligned pair is 11.8pt apart, half of the same 24pt.</p>
	 */
	private void checkSpaceAfterRetained(int flags) throws Exception {
		assertEquals("bottom-aligned: the last space-after is part of the aligned block",
				"retain", lastBlockConditionality(fo("bottom", 480, flags)));
		assertEquals("centre-aligned too",
				"retain", lastBlockConditionality(fo("center", 480, flags)));
		assertEquals("an unaligned section keeps FO's default",
				"", lastBlockConditionality(fo(null, 480, flags)));
		assertEquals("and so does a top-aligned one",
				"", lastBlockConditionality(fo("top", 480, flags)));
	}

	@Test
	public void spaceAfterIsRetainedInAnAlignedFlowVisitor() throws Exception {
		checkSpaceAfterRetained(Docx4J.FLAG_NONE);
	}

	@Test
	public void spaceAfterIsRetainedInAnAlignedFlowXslt() throws Exception {
		checkSpaceAfterRetained(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	private static final String TABLE =
			"<w:tbl><w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/></w:tblPr>"
			+ "<w:tblGrid><w:gridCol w:w=\"4675\"/></w:tblGrid>"
			+ "<w:tr><w:tc><w:tcPr><w:tcW w:w=\"4675\" w:type=\"dxa\"/></w:tcPr>"
			+ "<w:p><w:r><w:t>cell</w:t></w:r></w:p></w:tc></w:tr></w:tbl>";

	/** a table, then the empty paragraph carrying the section break */
	private org.w3c.dom.Document foTableThenBreak(String vAlign, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>Title</w:t></w:r></w:p>"
				+ TABLE
				+ "<w:p><w:pPr><w:sectPr>"
				+ (vAlign == null ? "" : "<w:vAlign w:val=\"" + vAlign + "\"/>")
				+ "<w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/>"
				+ "</w:sectPr></w:pPr></w:p>"
				+ "<w:p><w:r><w:t>next section</w:t></w:r></w:p>"
				+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	/** the local name of the last element child of the first fo:flow */
	private String lastInFirstFlow(org.w3c.dom.Document doc) {
		Element flow = (Element) doc.getElementsByTagNameNS(FO, "flow").item(0);
		String name = "";
		NodeList children = flow.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i) instanceof Element) name = children.item(i).getLocalName();
		}
		return name;
	}

	/**
	 * Where a vertically aligned section's content ends with a table, the empty
	 * paragraph a table must be followed by is part of what Word aligns, even though
	 * all it carries is the section break: measured on {@code section-valign-bottom},
	 * whose three table sections Word closes 15.7pt higher than docx4j did, and 7.9pt -
	 * half of it - for the centred one.  Everywhere else that paragraph is given no
	 * line at all, and adding one only pushes the flow's last line off the page.
	 */
	private void checkParagraphAfterTable(int flags) throws Exception {
		assertEquals("bottom-aligned: the paragraph after the table is aligned with it",
				"block", lastInFirstFlow(foTableThenBreak("bottom", flags)));
		assertEquals("centre-aligned too",
				"block", lastInFirstFlow(foTableThenBreak("center", flags)));
		assertEquals("an unaligned section renders nothing for it",
				"table", lastInFirstFlow(foTableThenBreak(null, flags)));
		assertEquals("nor does a top-aligned one",
				"table", lastInFirstFlow(foTableThenBreak("top", flags)));
	}

	@Test
	public void paragraphAfterTableIsAlignedVisitor() throws Exception {
		checkParagraphAfterTable(Docx4J.FLAG_NONE);
	}

	@Test
	public void paragraphAfterTableIsAlignedXslt() throws Exception {
		checkParagraphAfterTable(Docx4J.FLAG_EXPORT_PREFER_XSL);
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
