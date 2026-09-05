package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A hard page break which follows content in its own paragraph (CR-001 &#xa7;3).
 *
 * <p>Word breaks the page <em>at</em> the break: what precedes it stays on the page it is
 * on, and what follows opens the next one.  docx4j moved the break to the front of the
 * paragraph (setting w:pageBreakBefore and dropping the w:br), which took the text before
 * the break to the next page with the rest of the paragraph.  Measured on a real
 * document whose cover picture and page break share a paragraph: Word puts the 270x225pt
 * picture on page 1 (mutool {@code transform="270 0 0 225 162.65 347.59"}), docx4j put it
 * on page 2 at y=80.58, and every line of page 2 then sat 268.4pt low - 54 Word pages
 * came out as 44.  21 of 99 documents of a corpus of long real documents hold such a
 * paragraph.</p>
 *
 * <p>The two halves are one paragraph, so nothing is doubled between them: the first
 * keeps the space-before and loses the space-after, the second the other way about, and
 * the second takes neither the numbering label nor the first-line indent.</p>
 *
 * @since 17.0.6
 */
public class PageBreakInParagraphTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	/** before the break | the break | after it, all in one w:p. */
	private static final String BREAK_AFTER_CONTENT =
			"<w:p><w:pPr><w:spacing w:before=\"240\" w:after=\"240\"/></w:pPr>"
			+ "<w:r><w:t>Before the break</w:t></w:r>"
			+ "<w:r><w:br w:type=\"page\"/><w:t>After the break</w:t></w:r></w:p>";

	/** the break is the paragraph's first content: docx4j's old behaviour is right. */
	private static final String BREAK_FIRST =
			"<w:p><w:r><w:br w:type=\"page\"/><w:t>After the break</w:t></w:r></w:p>";

	private static WordprocessingMLPackage pkg(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
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

	/** The fo:blocks of the body flow which carry text, in document order. */
	private static List<Element> textBlocks(org.w3c.dom.Document doc) {
		List<Element> result = new ArrayList<Element>();
		NodeList blocks = doc.getElementsByTagNameNS(FO_NS, "block");
		for (int i = 0; i < blocks.getLength(); i++) {
			Element b = (Element) blocks.item(i);
			String text = b.getTextContent();
			if (text != null && text.contains("the break")) result.add(b);
		}
		return result;
	}

	private void theBreakSplitsTheParagraph(int flags) throws Exception {
		List<Element> blocks = textBlocks(fo(pkg(BREAK_AFTER_CONTENT), flags));
		assertEquals("the paragraph must become two blocks", 2, blocks.size());

		Element first = blocks.get(0), second = blocks.get(1);
		assertTrue(first.getTextContent().contains("Before the break"));
		assertFalse("the text before the break must stay on the page it is on",
				"page".equals(first.getAttribute("break-before")));
		assertTrue(second.getTextContent().contains("After the break"));
		assertEquals("the text after the break opens the next page",
				"page", second.getAttribute("break-before"));
	}

	@Test
	public void theBreakSplitsTheParagraphVisitor() throws Exception {
		theBreakSplitsTheParagraph(Docx4J.FLAG_NONE);
	}

	@Test
	public void theBreakSplitsTheParagraphXslt() throws Exception {
		theBreakSplitsTheParagraph(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** One paragraph, so one space-before at its start and one space-after at its end. */
	private void spacingIsNotDoubled(int flags) throws Exception {
		List<Element> blocks = textBlocks(fo(pkg(BREAK_AFTER_CONTENT), flags));
		assertEquals(2, blocks.size());
		assertEquals("the half which ends the paragraph carries no space-after in the middle",
				0.0, points(blocks.get(0).getAttribute("space-after")), 0.001);
		assertEquals("nor the second half a space-before",
				0.0, points(blocks.get(1).getAttribute("space-before")), 0.001);
	}

	@Test
	public void spacingIsNotDoubledVisitor() throws Exception {
		spacingIsNotDoubled(Docx4J.FLAG_NONE);
	}

	@Test
	public void spacingIsNotDoubledXslt() throws Exception {
		spacingIsNotDoubled(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** "0in", "0pt" and "0" are all no space. */
	private static double points(String length) {
		if (length==null || length.length()==0) return 0;
		String v = length.trim();
		if (v.endsWith("pt")) return Double.parseDouble(v.substring(0, v.length()-2));
		if (v.endsWith("in")) return Double.parseDouble(v.substring(0, v.length()-2)) * 72;
		if (v.endsWith("mm")) return Double.parseDouble(v.substring(0, v.length()-2)) * 72 / 25.4;
		if (v.endsWith("cm")) return Double.parseDouble(v.substring(0, v.length()-2)) * 72 / 2.54;
		return Double.parseDouble(v);
	}

	/** A break which opens its paragraph is still the paragraph's own break-before. */
	private void aLeadingBreakIsUnchanged(int flags) throws Exception {
		List<Element> blocks = textBlocks(fo(pkg("<w:p><w:r><w:t>page one</w:t></w:r></w:p>" + BREAK_FIRST), flags));
		assertEquals("nothing to split", 1, blocks.size());
		assertEquals("page", blocks.get(0).getAttribute("break-before"));
	}

	@Test
	public void aLeadingBreakIsUnchangedVisitor() throws Exception {
		aLeadingBreakIsUnchanged(Docx4J.FLAG_NONE);
	}

	@Test
	public void aLeadingBreakIsUnchangedXslt() throws Exception {
		aLeadingBreakIsUnchanged(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** What the corpus document does: a picture, then the break, in one paragraph.  The
	 *  picture must stay on the page it is on. */
	@Test
	public void thePageAPictureIsOnDoesNotChange() throws Exception {
		String p = "<w:p><w:r><w:t>anchor</w:t></w:r>"
				+ "<w:r><w:t>picture stand-in</w:t><w:br w:type=\"page\"/></w:r></w:p>";
		org.w3c.dom.Document doc = fo(pkg(p), Docx4J.FLAG_NONE);
		NodeList blocks = doc.getElementsByTagNameNS(FO_NS, "block");
		for (int i = 0; i < blocks.getLength(); i++) {
			Element b = (Element) blocks.item(i);
			if (!b.getTextContent().contains("picture stand-in")) continue;
			assertFalse("the break belongs after this content, not before it",
					"page".equals(b.getAttribute("break-before")));
		}
	}
}
