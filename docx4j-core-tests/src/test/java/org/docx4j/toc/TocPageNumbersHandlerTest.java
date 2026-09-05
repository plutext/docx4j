package org.docx4j.toc;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;

/**
 * FOP's area tree names pages by a viewport key ("P95") that is only
 * sequential while no page is laid out twice; with the Word layout managers
 * (17.0.5) pages restart and the keys jump, so the key must be resolved to the
 * viewport's page number rather than read as one.
 */
public class TocPageNumbersHandlerTest {

	private static Map<String, Integer> parse(String areaTree) throws Exception {
		TocPageNumbersHandler h = new TocPageNumbersHandler();
		SAXParserFactory.newInstance().newSAXParser().parse(
				new ByteArrayInputStream(areaTree.getBytes(StandardCharsets.UTF_8)), h);
		return h.getPageNumbers();
	}

	@Test
	public void viewportKeyIsResolvedToThePageNumber() throws Exception {
		// the links are the TOC's own entries, on page 1, before the viewports they point to
		String at = "<areaTree><pageSequence>"
				+ "<pageViewport key=\"P1\" nr=\"1\" formatted-nr=\"1\">"
				+ "<inlineparent internal-link=\"(P4,_Toc1)\"/><inlineparent internal-link=\"(P95,_Toc2)\"/></pageViewport>"
				+ "<pageViewport key=\"P4\" nr=\"4\" formatted-nr=\"4\"/>"
				+ "<pageViewport key=\"P95\" nr=\"5\" formatted-nr=\"5\"/>"
				+ "</pageSequence></areaTree>";
		Map<String, Integer> pages = parse(at);
		assertEquals(Integer.valueOf(4), pages.get("_Toc1"));
		assertEquals("key P95 is page 5, not page 95", Integer.valueOf(5), pages.get("_Toc2"));
	}

	@Test
	public void formattedNumberWinsWhenNumeric_plainNumberOtherwise() throws Exception {
		// a section restarting its numbering: the TOC shows the formatted number, as Word does
		String at = "<areaTree><pageSequence>"
				+ "<pageViewport key=\"P1\" nr=\"1\" formatted-nr=\"i\">"
				+ "<inlineparent internal-link=\"(P3,_Toc1)\"/><inlineparent internal-link=\"(P7,_Toc2)\"/></pageViewport>"
				+ "<pageViewport key=\"P3\" nr=\"3\" formatted-nr=\"iii\"/>"
				+ "<pageViewport key=\"P7\" nr=\"7\" formatted-nr=\"1\"/>"
				+ "</pageSequence></areaTree>";
		Map<String, Integer> pages = parse(at);
		assertEquals("roman: the plain number", Integer.valueOf(3), pages.get("_Toc1"));
		assertEquals("restarted numbering: the formatted number", Integer.valueOf(1), pages.get("_Toc2"));
	}

	@Test
	public void withoutViewportsTheKeyIsStillReadAsBefore() throws Exception {
		Map<String, Integer> pages = parse("<areaTree><inlineparent internal-link=\"(P12,_Toc9)\"/></areaTree>");
		assertEquals(Integer.valueOf(12), pages.get("_Toc9"));
	}
}
