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
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Two shapes at a section boundary, from &#xa7;3.3 of
 * <code>docx4j-export-fo/docs/word-layout-rules.md</code>, both of which
 * <code>WordLayoutFixups.mergePageBreakParagraphs</code> could not reach.
 *
 * <p><b>A page break at the end of a section costs no page.</b> The rule was there, but it
 * asked for the trailing block's <i>immediate</i> parent to be the <code>fo:flow</code>. A
 * multi-column section wraps its trailing material in a <code>span="all"</code> block, so
 * the break survived and made a page Word does not: measured on a corpus document whose
 * three-column section ends that way, where our page 2 was a second landscape page
 * carrying nothing but its running head.</p>
 *
 * <p><b>A typeless <code>w:sectPr</code> followed by a break-only paragraph is Word's own
 * empty page.</b> The rule for that was there too, but where the <code>w:br</code> run
 * carries <code>w:rPr</code> the break leaves an empty <code>fo:inline</code> behind when
 * it moves to the block, and the collection step - which asked for a block with no element
 * child at all - refused it. Measured on a corpus document with five of them, where Word
 * has six blank pages and docx4j emitted one.</p>
 *
 * @since 17.1.1
 */
public class SectionEndPageBreakTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";

	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	private static final String PG =
			"<w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\""
			+ " w:header=\"708\" w:footer=\"708\"/>";

	private static String para(String text) {
		return "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	private static WordprocessingMLPackage pkg(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		return pkg;
	}

	private static org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder()
				.parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private static List<Element> flows(org.w3c.dom.Document doc) {
		List<Element> out = new ArrayList<Element>();
		NodeList nl = doc.getElementsByTagNameNS(FO_NS, "flow");
		for (int i = 0; i < nl.getLength(); i++) out.add((Element) nl.item(i));
		return out;
	}

	private static Element firstChildElement(Element el) {
		for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element) return (Element) n;
		}
		return null;
	}

	/*
	 * The first shape has no unit test, and this is why.  The span="all" wrapper it is
	 * about exists only where a continuous section of one column count is merged into a
	 * page-sequence of another, and FOP's BalancingColumnBreakingAlgorithm throws an
	 * AssertionError laying such a page-sequence out - measured on this shape at 12 and at
	 * 60 paragraphs, and with docx4j.convert.out.fo.wordLayoutFixups=false and with
	 * docx4j.convert.out.fo.wordLayout=false, so it is FOP's own and not this rule's.
	 * Assertions are on in this module and off in production, which is why the corpus
	 * renders it without complaint.  That half is measured on the corpus instead: the
	 * document's FO ended
	 *   <block span="all">...<fo:block break-before="page"> </fo:block></block></flow>
	 * and its page 2 was a second landscape page carrying nothing but its running head.
	 */

	// ------------------------------------------------- Word's own empty page

	/**
	 * A section break followed by a paragraph holding nothing but a page break whose run
	 * carries <code>w:rPr</code>: Word puts the mark on the page the section break opens
	 * and the break makes another, so that page is empty.  The break must move off the
	 * flow's first block - which is the empty page - onto the block after it.
	 */
	private void checkEmptyPage(int flags) throws Exception {
		WordprocessingMLPackage pkg = pkg(
				para("Section one.")
				+ "<w:p><w:pPr><w:sectPr>" + PG + "</w:sectPr></w:pPr></w:p>"
				// the w:lastRenderedPageBreak is what leaves an empty fo:inline behind
				// when the break moves to the block - the shape the corpus carries, and
				// the one the collection step refused
				+ "<w:p><w:r><w:rPr><w:sz w:val=\"17\"/><w:szCs w:val=\"17\"/></w:rPr>"
				+ "<w:lastRenderedPageBreak/><w:br w:type=\"page\"/></w:r></w:p>"
				+ para("Section two, after Word's empty page.")
				+ "<w:sectPr>" + PG + "</w:sectPr>");
		List<Element> flows = flows(fo(pkg, flags));
		assertEquals("two sections, two flows", 2, flows.size());

		Element first = firstChildElement(flows.get(1));
		assertTrue("the second flow opens with a block", first != null);
		// that block is the empty page itself: it paints nothing.  (Its own break-before
		// is left where it is - at the start of a flow XSL-FO ignores it.)
		assertEquals("the flow's first block paints nothing", "",
				first.getTextContent().trim());

		Element second = null;
		for (Node n = first.getNextSibling(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element) { second = (Element) n; break; }
		}
		assertTrue("a second block follows it", second != null);
		assertEquals("the break moves onto it, which is what makes the page before it empty",
				"page", second.getAttribute("break-before"));
	}

	@Test
	public void emptyPageVisitorPathway() throws Exception {
		checkEmptyPage(Docx4J.FLAG_NONE);
	}

	@Test
	public void emptyPageXsltPathway() throws Exception {
		checkEmptyPage(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
