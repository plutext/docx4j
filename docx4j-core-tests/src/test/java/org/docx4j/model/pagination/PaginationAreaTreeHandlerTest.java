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

	private static String anchored(String anchor, String... words) {
		StringBuilder sb = new StringBuilder("<inlineparent prod-id=\"" + anchor + "\"><inlineparent><text>");
		for (int i = 0; i < words.length; i++) {
			if (i > 0) sb.append("<space> </space>");
			sb.append("<word>").append(words[i]).append("</word>");
		}
		return sb.append("</text></inlineparent></inlineparent>").toString();
	}

	private static String blockOf(String prodId, String inner) {
		return "<block prod-id=\"" + prodId + "\"><lineArea>" + inner + "</lineArea></block>";
	}

	@Test
	public void aRunAnchorPlacesTheBoundaryInsideTheRun() throws Exception {
		// the run r-A-0 spans the boundary: 9 characters of it were on page 1, on one
		// line, at whose end FOP dropped the space: "Long text continues" breaks at 10
		String at = "<areaTree><pageSequence>"
				+ page(1, "1", blockOf("p-A", anchored("r-A-0", "Long", "text")))
				+ page(2, "2", blockOf("p-A", anchored("r-A-0", "continues") + anchored("r-A-20", "more")))
				+ "</pageSequence></areaTree>";
		PaginationMap map = parse(at);
		assertArrayEquals(new int[] { 10 }, map.getBreaks("A"));
		assertEquals(0, map.raw("A").get(0).part);
		assertFalse("placed by the anchor, not the fallback count", map.raw("A").get(0).absolute);
		assertEquals(0, map.raw("A").get(0).hyphenEnds);
		assertEquals("the new page's line, as far as it goes", "continuesmore", map.raw("A").get(0).lineStart.toString());
	}

	@Test
	public void everyLineTheRunWasOnDroppedACharacter() throws Exception {
		// three lines on page 1 (the anchor appears once per line): 3 dropped spaces
		String at = "<areaTree><pageSequence>"
				+ page(1, "1", "<block prod-id=\"p-A\">"
						+ "<lineArea>" + anchored("r-A-0", "one") + "</lineArea>"
						+ "<lineArea>" + anchored("r-A-0", "two") + "</lineArea>"
						+ "<lineArea>" + anchored("r-A-0", "three") + "</lineArea></block>")
				+ page(2, "2", blockOf("p-A", anchored("r-A-0", "four")))
				+ "</pageSequence></areaTree>";
		PaginationMap map = parse(at);
		assertArrayEquals("3 + 3 + 5 characters and 3 line ends", new int[] { 14 }, map.getBreaks("A"));
	}

	@Test
	public void aLaterRunAtTheSameOffsetCarriesACounter() throws Exception {
		// a field character's run stands for no text: the result run after it shares its
		// offset, and its id is suffixed to stay unique
		String at = "<areaTree><pageSequence>"
				+ page(1, "1", blockOf("p-A", anchored("r-A-0", "text")))
				+ page(2, "2", blockOf("p-A", anchored("r-A-5.2", "result")))
				+ "</pageSequence></areaTree>";
		PaginationMap map = parse(at);
		assertArrayEquals(new int[] { 5 }, map.getBreaks("A"));
	}

	@Test
	public void aTabIsAnEmptySpaceArea_oneCharacter() throws Exception {
		String at = "<areaTree><pageSequence>"
				+ page(1, "1", blockOf("p-A", "<inlineparent prod-id=\"r-A-0\"><inlineparent><text><word>ab</word></text></inlineparent>"
						+ "<space/><inlineparent><text><word>cd</word></text></inlineparent></inlineparent>"))
				+ page(2, "2", blockOf("p-A", anchored("r-A-0", "ef")))
				+ "</pageSequence></areaTree>";
		PaginationMap map = parse(at);
		assertArrayEquals("2 + 1 (tab) + 2, and the line end", new int[] { 6 }, map.getBreaks("A"));
	}

	@Test
	public void aNewRunOnTheNewPageIsTheBoundary() throws Exception {
		String at = "<areaTree><pageSequence>"
				+ page(1, "1", blockOf("p-A", anchored("r-A-0", "Long", "text")))
				+ page(2, "2", blockOf("p-A", anchored("r-A-14", "next", "run")))
				+ "</pageSequence></areaTree>";
		PaginationMap map = parse(at);
		assertArrayEquals(new int[] { 14 }, map.getBreaks("A"));
	}

	@Test
	public void aHyphenFopAddedIsReported() throws Exception {
		String at = "<areaTree><pageSequence>"
				+ page(1, "1", blockOf("p-A", anchored("r-A-0", "incompre-")))
				+ page(2, "2", blockOf("p-A", anchored("r-A-0", "hensible")))
				+ "</pageSequence></areaTree>";
		PaginationMap map = parse(at);
		assertArrayEquals("as counted: the hyphen, and no dropped space at that line end", new int[] { 9 }, map.getBreaks("A"));
		assertEquals("for Paginate to settle against the text", 1, map.raw("A").get(0).hyphenEnds);
		assertEquals("hensible", map.raw("A").get(0).lineStart.toString());
	}

	@Test
	public void aContinuationPartIsRelativeToThatPart() throws Exception {
		// the preprocessing split the paragraph at a page break inside it: the second
		// part's runs count from 0 again
		String at = "<areaTree><pageSequence>"
				+ page(1, "1", blockOf("p-A", anchored("r-A-0", "before")))
				+ page(2, "2", blockOf("p-A~1", anchored("r-A~1-0", "after")))
				+ "</pageSequence></areaTree>";
		PaginationMap map = parse(at);
		assertEquals(1, map.raw("A").size());
		assertEquals(1, map.raw("A").get(0).part);
		assertEquals(0, map.raw("A").get(0).offset);
		assertFalse(map.raw("A").get(0).absolute);
	}

	@Test
	public void withoutAnAnchorTheCharacterCountStands() throws Exception {
		String at = "<areaTree><pageSequence>"
				+ page(1, "1", blockOf("p-A", anchored("r-A-0", "Long", "text")))
				+ page(2, "2", block("p-A", "continues"))
				+ "</pageSequence></areaTree>";
		PaginationMap map = parse(at);
		assertArrayEquals(new int[] { 9 }, map.getBreaks("A"));
		assertTrue(map.raw("A").get(0).absolute);
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
