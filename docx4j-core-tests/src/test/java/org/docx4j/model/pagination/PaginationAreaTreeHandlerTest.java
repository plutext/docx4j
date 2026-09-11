package org.docx4j.model.pagination;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * The area tree reader of CR-012, over hand-written trees in the shape FOP 2.11's
 * XMLRenderer writes: pages, keyed blocks, lines of words and spaces, and the regions
 * and blocks that are not body text.
 */
public class PaginationAreaTreeHandlerTest {

	private static PaginationMap parse(String areaTree) throws Exception {
		PaginationAreaTreeHandler h = new PaginationAreaTreeHandler();
		SAXParserFactory.newInstance().newSAXParser().parse(
				new ByteArrayInputStream(areaTree.getBytes(StandardCharsets.UTF_8)), h);
		return h.getMap();
	}

	private static String page(int nr, String formatted, String body) {
		return "<pageViewport key=\"P" + nr + "\" nr=\"" + nr + "\" formatted-nr=\"" + formatted + "\"><page>"
				+ "<regionViewport><regionBefore><block><lineArea><text><word>Header</word></text></lineArea></block></regionBefore></regionViewport>"
				+ "<regionViewport><regionBody><mainReference><span><flow>" + body + "</flow></span></mainReference></regionBody></regionViewport>"
				+ "<regionViewport><regionAfter><block prod-id=\"p-A\"><lineArea><text><word>Footer</word></text></lineArea></block></regionAfter></regionViewport>"
				+ "</page></pageViewport>";
	}

	private static String block(String prodId, String... words) {
		StringBuilder sb = new StringBuilder("<block" + (prodId == null ? "" : " prod-id=\"" + prodId + "\"") + "><lineArea><text>");
		for (int i = 0; i < words.length; i++) {
			if (i > 0) sb.append("<space> </space>");
			sb.append("<word>").append(words[i]).append("</word>");
		}
		return sb.append("</text></lineArea></block>").toString();
	}

	@Test
	public void eachParagraphHasItsStartPage() throws Exception {
		String at = "<areaTree><pageSequence>"
				+ page(1, "1", block("p-A", "Alpha") + block("p-B", "Beta"))
				+ page(2, "2", block("p-C", "Gamma"))
				+ "</pageSequence></areaTree>";
		PaginationMap map = parse(at);
		assertEquals(2, map.getPageCount());
		assertEquals(Integer.valueOf(1), map.getPageIndex("A"));
		assertEquals(Integer.valueOf(1), map.getPageIndex("B"));
		assertEquals(Integer.valueOf(2), map.getPageIndex("C"));
		assertEquals(Integer.valueOf(2), map.getLastPageIndex("C"));
		assertEquals(0, map.getBreaks("A").length);
		assertNull("not in the tree", map.getPageIndex("Z"));
	}

	@Test
	public void aParagraphOnTwoPagesBreaksAfterTheCharactersOfItsFirstPart() throws Exception {
		// "Long text" = 9 characters on page 1 (the space element's content counts), then it continues
		String at = "<areaTree><pageSequence>"
				+ page(1, "1", block("p-A", "Long", "text"))
				+ page(2, "2", block("p-A", "continues") + block("p-B", "Next"))
				+ "</pageSequence></areaTree>";
		PaginationMap map = parse(at);
		assertEquals(Integer.valueOf(1), map.getPageIndex("A"));
		assertEquals(Integer.valueOf(2), map.getLastPageIndex("A"));
		assertArrayEquals(new int[] { 9 }, map.getBreaks("A"));
		assertEquals("[A]", map.getKeysWithBreaks().toString());
		assertEquals(Integer.valueOf(2), map.getPageIndex("B"));
	}

	@Test
	public void aContinuationIdIsTheSameParagraph() throws Exception {
		// the exporter suffixes the id of a paragraph it writes twice (split at a page break inside it)
		String at = "<areaTree><pageSequence>"
				+ page(1, "1", block("p-A", "Before"))
				+ page(2, "2", block("p-A~1", "After"))
				+ page(3, "3", block("p-A~2", "Again"))
				+ "</pageSequence></areaTree>";
		PaginationMap map = parse(at);
		assertEquals(Integer.valueOf(1), map.getPageIndex("A"));
		assertEquals(Integer.valueOf(3), map.getLastPageIndex("A"));
		assertArrayEquals(new int[] { 6, 11 }, map.getBreaks("A"));
		assertEquals("p-A~1", PaginationAreaTreeHandler.foId("A", 1));
		assertEquals("p-A", PaginationAreaTreeHandler.foId("A", 0));
		assertEquals("A", PaginationAreaTreeHandler.keyOf("p-A~12"));
		assertEquals("a~b", PaginationAreaTreeHandler.keyOf("p-a~b"));
	}

	@Test
	public void theSamePageAgainIsNotABreak() throws Exception {
		// a paragraph split across the two columns of one page
		String at = "<areaTree><pageSequence>"
				+ page(1, "1", block("p-A", "left") + "</flow><flow>" + block("p-A", "right"))
				+ "</pageSequence></areaTree>";
		PaginationMap map = parse(at);
		assertEquals(0, map.getBreaks("A").length);
		assertEquals(Integer.valueOf(1), map.getLastPageIndex("A"));
	}

	@Test
	public void headersFootersFootnotesAndFloatsAreNotBodyText_positionedBlocksAre() throws Exception {
		// the footer block above carries p-A on every page; a footnote holds p-F, a before-float
		// p-G; the positioned block is how the export wraps a table (a text box's paragraphs
		// get no id from the exporter in the first place)
		String at = "<areaTree><pageSequence>"
				+ page(1, "1", block("p-A", "Body")
						+ "<footnote>" + block("p-F", "Note") + "</footnote>"
						+ "<block><block positioning=\"absolute\">" + block("p-T", "Table", "cell") + "</block></block>"
						+ "<beforeFloat>" + block("p-G", "Float") + "</beforeFloat>")
				+ page(2, "2", block("p-B", "More"))
				+ "</pageSequence></areaTree>";
		PaginationMap map = parse(at);
		assertEquals("the footer's p-A on page 2 is not a continuation", 0, map.getBreaks("A").length);
		assertEquals(Integer.valueOf(1), map.getLastPageIndex("A"));
		assertFalse(map.contains("F"));
		assertFalse(map.contains("G"));
		assertEquals(Integer.valueOf(1), map.getPageIndex("T"));
		assertTrue(map.contains("B"));
	}

	@Test
	public void nestedBlocksCountTowardsTheInnermostKeyedParagraph() throws Exception {
		// a table's cell paragraphs are blocks inside the table's blocks (no prod-id of their own)
		String at = "<areaTree><pageSequence>"
				+ page(1, "1", "<block><block><block>" + block("p-C1", "cell", "one") + "</block></block>"
						+ "<block><block>" + block("p-C2", "cell", "two") + "</block></block></block>")
				+ page(2, "2", "<block><block><block>" + block("p-C2", "more") + "</block></block></block>")
				+ "</pageSequence></areaTree>";
		PaginationMap map = parse(at);
		assertEquals(Integer.valueOf(1), map.getPageIndex("C1"));
		assertArrayEquals(new int[] { 8 }, map.getBreaks("C2"));
	}

	@Test
	public void formattedNumberIsThePage_indexIsThePosition() throws Exception {
		String at = "<areaTree><pageSequence>"
				+ page(1, "i", block("p-A", "Front"))
				+ page(2, "ii", block("p-B", "Matter"))
				+ "</pageSequence><pageSequence>"
				+ page(3, "1", block("p-C", "Chapter"))
				+ "</pageSequence></areaTree>";
		PaginationMap map = parse(at);
		assertEquals("roman: the plain number", Integer.valueOf(2), map.getPage("B"));
		assertEquals("restarted numbering: the formatted number", Integer.valueOf(1), map.getPage("C"));
		assertEquals(Integer.valueOf(3), map.getPageIndex("C"));
		assertEquals(3, map.getPageCount());
	}

	@Test
	public void aViewportWithoutANumberIsNotTheExpectedFormat() throws Exception {
		try {
			parse("<areaTree><pageSequence><pageViewport key=\"P1\"><page/></pageViewport></pageSequence></areaTree>");
			fail("expected a SAXException");
		} catch (SAXException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("nr"));
		}
	}
}
