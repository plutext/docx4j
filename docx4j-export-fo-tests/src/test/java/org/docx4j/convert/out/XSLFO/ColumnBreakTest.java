package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A {@code w:br w:type="column"} in a section whose columns are equal (CR-001 &#xa7;7.3).
 *
 * <p>Word divides the paragraph the break is in: what precedes it ends the column and what
 * follows it opens the next.  docx4j emitted the break as an ordinary line break, so the
 * column was never taken at all.  The paragraph is now split
 * ({@code ConversionSectionWrapperFactory} / {@code ColumnBreaks}) and the second half's
 * block carries {@code break-before="column"}.</p>
 *
 * <p>The two halves are one paragraph: the space-after goes with the half which ends it
 * and the space-before stays on both, as measured for the unequal-columns pathway, which
 * divides the same documents at the same breaks.</p>
 *
 * @since 17.1.0
 */
public class ColumnBreakTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static String sectPr(String cols) {
		return "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/>"
				+ cols + "</w:sectPr>";
	}

	private static final String TWO_EQUAL_COLUMNS = "<w:cols w:num=\"2\" w:space=\"720\"/>";

	private static WordprocessingMLPackage pkg(String body, String cols) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + sectPr(cols) + "</w:body></w:document>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(String body, String cols, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg(body, cols));
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** The blocks of the flow, in order (the paragraphs; not their nested content). */
	private static List<Element> flowBlocks(org.w3c.dom.Document doc) {
		List<Element> blocks = new ArrayList<Element>();
		NodeList flows = doc.getElementsByTagNameNS(FO, "flow");
		for (int i = 0; i < flows.getLength(); i++) {
			for (Node n = flows.item(i).getFirstChild(); n != null; n = n.getNextSibling()) {
				if (n instanceof Element && "block".equals(((Element)n).getLocalName())) {
					blocks.add((Element)n);
				}
			}
		}
		return blocks;
	}

	/** a length in points, whatever unit the FO states it in ("0in" is a zero too) */
	private static double pt(String v) {
		if (v == null || v.length() == 0) return 0;
		if (v.endsWith("in")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72;
		if (v.endsWith("pt")) return Double.parseDouble(v.substring(0, v.length() - 2));
		return Double.parseDouble(v);
	}

	private static String text(Element el) {
		return el.getTextContent().trim();
	}

	/** "one <break> two": Word ends column 1 with "one" and opens column 2 with "two". */
	private static final String BREAK_INSIDE = "<w:p><w:pPr><w:spacing w:before=\"120\" w:after=\"120\"/></w:pPr>"
			+ "<w:r><w:t>one</w:t></w:r>"
			+ "<w:r><w:br w:type=\"column\"/><w:t>two</w:t></w:r></w:p>";

	private void breakInsideAParagraph(int flags) throws Exception {

		List<Element> blocks = flowBlocks(fo(BREAK_INSIDE, TWO_EQUAL_COLUMNS, flags));
		assertEquals(2, blocks.size());

		assertEquals("one", text(blocks.get(0)));
		assertEquals("two", text(blocks.get(1)));

		// the break is taken, and only by the half which follows it
		assertEquals("auto", blocks.get(0).getAttribute("break-before"));
		assertEquals("column", blocks.get(1).getAttribute("break-before"));

		// one paragraph, one space-after: it goes with the half which ends it, and the
		// space-before stays on both
		assertEquals("6pt", blocks.get(0).getAttribute("space-before"));
		assertEquals(0d, pt(blocks.get(0).getAttribute("space-after")), 0.001);
		assertEquals("6pt", blocks.get(1).getAttribute("space-before"));
		assertEquals("6pt", blocks.get(1).getAttribute("space-after"));
	}

	@Test
	public void visitor() throws Exception {
		breakInsideAParagraph(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		breakInsideAParagraph(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/**
	 * Where the break ends its paragraph, the half which opens the next column is nothing
	 * but the paragraph mark - and takes a line there, with the paragraph's space-after
	 * (measured on {@code columns-unequal}: Word starts column 2 one line plus 6pt below
	 * the end of column 1).
	 */
	@Test
	public void breakAtTheEndOfAParagraph() throws Exception {
		String body = "<w:p><w:pPr><w:spacing w:after=\"120\"/></w:pPr>"
				+ "<w:r><w:t>one</w:t></w:r><w:r><w:br w:type=\"column\"/></w:r></w:p>"
				+ "<w:p><w:r><w:t>next</w:t></w:r></w:p>";
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			List<Element> blocks = flowBlocks(fo(body, TWO_EQUAL_COLUMNS, flags));
			assertEquals(3, blocks.size());
			assertEquals("one", text(blocks.get(0)));
			assertEquals(0d, pt(blocks.get(0).getAttribute("space-after")), 0.001);
			assertEquals("column", blocks.get(1).getAttribute("break-before"));
			assertEquals("6pt", blocks.get(1).getAttribute("space-after"));
			// the mark's own line
			assertTrue(blocks.get(1).hasChildNodes());
		}
	}

	/** A break which already opens its paragraph needs no split, only the break-before. */
	@Test
	public void breakAtTheStartOfAParagraph() throws Exception {
		String body = "<w:p><w:r><w:t>one</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:br w:type=\"column\"/><w:t>two</w:t></w:r></w:p>";
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			List<Element> blocks = flowBlocks(fo(body, TWO_EQUAL_COLUMNS, flags));
			assertEquals(2, blocks.size());
			assertEquals("column", blocks.get(1).getAttribute("break-before"));
			assertEquals("two", text(blocks.get(1)));
		}
	}

	/**
	 * In a single-column section there is no next column to go to - the region body's
	 * columns are all there is - so the break stays the line break docx4j has always made
	 * of it, and the paragraph is not divided.
	 */
	@Test
	public void singleColumnSectionIsUnchanged() throws Exception {
		for (int flags : new int[] { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL }) {
			org.w3c.dom.Document doc = fo(BREAK_INSIDE, "", flags);
			List<Element> blocks = flowBlocks(doc);
			assertEquals(1, blocks.size());
			assertEquals("auto", blocks.get(0).getAttribute("break-before"));
			assertTrue(isAbsent(doc, "//*[@break-before='column']"));
			// and no hint leaks into the FO
			assertTrue(isAbsent(doc, "//*[@docx4j-colbreak]"));
		}
	}

}
