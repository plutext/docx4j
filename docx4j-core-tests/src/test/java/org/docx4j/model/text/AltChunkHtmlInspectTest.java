/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.model.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.Docx4jProperties;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * CR-053 phase 3: what {@code display: none} hides in an HTML, XHTML or MHT {@code altChunk}, as the
 * inspector reports it (WD REQ-902): through the import its extraction runs (REQ-901, WD 0.26), REQ-907's
 * limits checked first, one {@code text.hidden} finding for each outermost hidden element with text, at
 * its path in the tree jsoup built and at its block's address under {@code hidden: true}. Without an
 * HTML reader the chunk is skipped as {@code not_decoded}. The paths are tested with jsoup alone, as the
 * build runs, and so is the skip; the rest where docx4j-ImportXHTML is on the class path (a hand run's,
 * CR-053 section 12.3), skipped otherwise.
 */
public class AltChunkHtmlInspectTest {

	private static WordprocessingMLPackage chunk(AltChunkType type, String markup) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().addAltChunk(type, markup.getBytes(StandardCharsets.UTF_8));
		return pkg;
	}

	private static WordprocessingMLPackage html(String body) throws Exception {
		return chunk(AltChunkType.Html, "<html><head></head><body>" + body + "</body></html>");
	}

	private static void reader() {
		assumeTrue("docx4j-ImportXHTML and jsoup are on the class path", HtmlChunkImport.readerPresent());
	}

	private static List<InspectFinding> of(Inspection i, String kind) {
		List<InspectFinding> out = new ArrayList<InspectFinding>();
		for (InspectFinding f : i.findings()) if (f.getKind().equals(kind)) out.add(f);
		return out;
	}

	/** Each {@code text.hidden} finding: its path and excerpt. */
	private static List<String> hidden(Inspection i) {
		List<String> out = new ArrayList<String>();
		for (InspectFinding f : of(i, InspectFinding.TEXT_HIDDEN)) out.add(f.getLocation().getPath() + " " + f.getExcerpt());
		return out;
	}

	private static String parsed(Inspection i) {
		return of(i, InspectFinding.ALTCHUNK).get(0).getParsed();
	}

	/** Each block's address to its text, cells' blocks included. */
	private static Map<String, String> addresses(List<TextBlock> blocks, Map<String, String> out) {
		for (TextBlock b : blocks) {
			StringBuilder sb = new StringBuilder();
			for (TextRun r : b.getRuns()) if (r.getText() != null) sb.append(r.getText());
			out.put(b.getAddress(), sb.toString());
			for (TextRow row : b.getRows()) for (TextCell cell : row.getCells()) addresses(cell.getBlocks(), out);
		}
		return out;
	}

	// --- with jsoup alone ----------------------------------------------------------------------------

	/**
	 * A path counts the tree jsoup built, its repairs included, and is recorded before the rewrite
	 * moves anything: an element the rewrite unwraps ({@code article}), and a body {@code style} it moves
	 * to the head, keep the paths they had; jsoup's {@code tbody} and the paragraph an unclosed {@code p}
	 * closes count.
	 */
	@Test
	public void pathsCountTheParsedTreeBeforeTheRewrite() throws Exception {
		assumeTrue("jsoup is on the class path", JsoupHtml.present());
		String markup = "<html><body><style>.x{color:red}</style><article><p>IN_ARTICLE</p></article>"
				+ "<p>UNCLOSED<p>AFTER_UNCLOSED</p><table><tr><td>A</td><td>CELL_B</td></tr></table></body></html>";
		Map<Element, String> paths = new IdentityHashMap<Element, String>();
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		Document dom = HtmlChunkImport.parsed(markup.getBytes(StandardCharsets.UTF_8), false, "a test", paths);
		assertTrue(HtmlChunkImport.rewritten(dom, pkg, "a test"));
		Map<String, String> byText = new LinkedHashMap<String, String>();
		NodeList all = dom.getElementsByTagName("*");
		for (int n = 0; n < all.getLength(); n++) {
			Element e = (Element) all.item(n);
			String own = e.getFirstChild() == null ? "" : e.getFirstChild().getNodeValue();
			if (own != null && own.matches("[A-Z_]+")) byText.put(own, paths.get(e));
			if (HtmlChunks.local(e).equals("style")) byText.put("style", paths.get(e));
		}
		assertEquals("/html[1]/body[1]/article[1]/p[1]", byText.get("IN_ARTICLE"));
		assertEquals("/html[1]/body[1]/p[1]", byText.get("UNCLOSED"));
		assertEquals("/html[1]/body[1]/p[2]", byText.get("AFTER_UNCLOSED"));
		assertEquals("/html[1]/body[1]/table[1]/tbody[1]/tr[1]/td[2]", byText.get("CELL_B"));
		assertEquals("moved to the head, its path the body's", "/html[1]/body[1]/style[1]", byText.get("style"));
		assertEquals("the article is unwrapped", 0, dom.getElementsByTagName("article").getLength());
	}

	/**
	 * Without an HTML reader (the build's: jsoup without the importer), an HTML, XHTML or MHT chunk is
	 * skipped as {@code not_decoded}, with no {@code parsed}; its scripts and references are found all
	 * the same. A plain-text chunk has no {@code parsed} either way.
	 */
	@Test
	public void withoutAReaderTheChunkIsNotDecoded() throws Exception {
		assumeFalse("an HTML reader is on the class path", HtmlChunkImport.readerPresent());
		WordprocessingMLPackage pkg = html("<p style='display:none'>HIDDEN</p><script>go()</script><img src='http://cr053.invalid/i.png'>");
		Inspection i = Inspect.of(pkg);
		assertEquals("[not_decoded@|/chunk.html|]", i.skipped().toString());
		assertEquals(Inspection.Status.PARTIAL, i.status());
		assertNull(parsed(i));
		assertTrue(of(i, InspectFinding.TEXT_HIDDEN).isEmpty());
		assertEquals(1, of(i, InspectFinding.ALTCHUNK_SCRIPT).size());
		assertEquals("http://cr053.invalid/i.png", of(i, InspectFinding.ALTCHUNK_EXTERNAL_REF).get(0).getSummary());
		WordprocessingMLPackage txt = chunk(AltChunkType.TextPlain, "PLAIN");
		Inspection t = Inspect.of(txt);
		assertNull(parsed(t));
		assertTrue(t.skipped().toString(), t.skipped().isEmpty());
	}

	// --- with an HTML reader -------------------------------------------------------------------------

	/**
	 * One finding for the outermost element (REQ-902): a {@code display: none} inside another adds none,
	 * and the outer one's excerpt holds both texts, a block a line feed apart. An element inside a hidden
	 * one whose parent shows its content again ({@code display: block}, Word's rule) is its own finding.
	 */
	@Test
	public void oneFindingForTheOutermostElement() throws Exception {
		reader();
		Inspection i = Inspect.of(html("<p>VISIBLE</p><div style='display:none'><p>OUTER_A</p><p style='display:none'>INNER_B</p></div>"
				+ "<div style='display:none'><p style='display:block'>SHOWN_<span style='display:none'>INNER_SPAN</span></p></div>"));
		assertEquals(Arrays.asList("/html[1]/body[1]/div[1] OUTER_A\nINNER_B", "/html[1]/body[1]/div[2]/p[1]/span[1] INNER_SPAN"), hidden(i));
		assertEquals(InspectFinding.PARSED_HTML, parsed(i));
		assertEquals(Inspection.Status.COMPLETE, i.status());
	}

	/**
	 * CR-056 phase 3: a chunk's script-scheme link. The import keeps it as written (docx4j-ImportXHTML's
	 * link check is turned off for the extraction, as Word keeps such a target), so the text model's run
	 * carries it for the outputs to refuse; the inspector's chunk scan reports it as
	 * {@code altchunk.script} whether or not a reader is present.
	 */
	@Test
	public void aScriptSchemeLinkIsKeptForTheOutputsToRefuse() throws Exception {
		reader();
		WordprocessingMLPackage pkg = html("<p><a href='javascript:alert(1)'>CLICK</a> and <a href='https://example.org/'>WEB</a></p>");
		Inspection i = Inspect.of(pkg);
		List<String> kinds = new ArrayList<String>();
		for (InspectFinding f : i.findings()) kinds.add(f.getKind() + " " + f.getSubject() + " | " + f.getExcerpt());
		System.out.println("CR056P3 findings: " + kinds);
		List<String> links = new ArrayList<String>();
		for (TextBlock b : Texts.of(pkg, new TextOptions()).blocks()) for (TextRun r : b.getRuns()) if (r.getLink() != null) links.add(r.getText() + " -> " + r.getLink());
		System.out.println("CR056P3 links: " + links);
		assertEquals(1, of(i, InspectFinding.ALTCHUNK_SCRIPT).size());
		assertTrue(links.toString(), links.contains("CLICK -> javascript:alert(1)"));
		assertTrue(links.toString(), links.contains("WEB -> https://example.org/"));
	}

	/** An element with no text gives no finding: empty, white space alone, a picture alone. */
	@Test
	public void anElementWithNoTextGivesNone() throws Exception {
		reader();
		Inspection i = Inspect.of(html("<p>VISIBLE</p><p style='display:none'></p><div style='display:none'>  \n </div>"
				+ "<p><span style='display:none'><img src='data:image/gif;base64,R0lGODlhAQABAAAAACw=' alt=''></span>AFTER</p>"
				+ "<p style='display:none'>ONLY_THIS</p>"));
		assertEquals(Arrays.asList("/html[1]/body[1]/p[4] ONLY_THIS"), hidden(i));
	}

	/**
	 * The location under each parse: an HTML chunk's repaired tree (an unclosed {@code p}, jsoup's
	 * {@code tbody}), a well-formed XHTML chunk's HTML tree (WD 0.26, D9 (iii)), an MHT chunk's first HTML
	 * part; {@code parsed} is {@code html} for all three.
	 */
	@Test
	public void theLocationIsThePathInTheParsedTree() throws Exception {
		reader();
		Inspection h = Inspect.of(html("<p>UNCLOSED<p style='display:none'>AFTER_UNCLOSED</p><table><tr><td>A</td>"
				+ "<td style='display:none'>CELL_B</td></tr></table>"));
		assertEquals(Arrays.asList("/html[1]/body[1]/p[2] AFTER_UNCLOSED", "/html[1]/body[1]/table[1]/tbody[1]/tr[1]/td[2] CELL_B"), hidden(h));
		assertEquals(InspectFinding.PARSED_HTML, parsed(h));
		Inspection x = Inspect.of(chunk(AltChunkType.Xhtml, "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
				+ "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html xmlns='http://www.w3.org/1999/xhtml'><head><title>T</title>"
				+ "</head><body><div><p>X_VISIBLE</p><p style='display:none'>X_HIDDEN</p></div></body></html>"));
		assertEquals(Arrays.asList("/html[1]/body[1]/div[1]/p[2] X_HIDDEN"), hidden(x));
		assertEquals(InspectFinding.PARSED_HTML, parsed(x));
		Inspection m = Inspect.of(chunk(AltChunkType.Mht, "MIME-Version: 1.0\r\nContent-Type: multipart/related; boundary=\"B\"\r\n\r\n"
				+ "--B\r\nContent-Type: text/html; charset=\"utf-8\"\r\n\r\n"
				+ "<html><body><p>M_VISIBLE</p><p style='display:none'>M_FIRST_PART</p></body></html>\r\n"
				+ "--B\r\nContent-Type: text/html; charset=\"utf-8\"\r\nContent-Location: second.htm\r\n\r\n"
				+ "<html><body><p style='display:none'>M_SECOND_PART</p></body></html>\r\n--B--\r\n"));
		assertEquals("the first HTML part alone", Arrays.asList("/html[1]/body[1]/p[2] M_FIRST_PART"), hidden(m));
		assertEquals(InspectFinding.PARSED_HTML, parsed(m));
	}

	/**
	 * Each finding's address is the block that holds its text in the extraction under
	 * {@code hidden: true}, block for block: a hidden paragraph's own, a hidden span's paragraph's, a
	 * hidden cell's paragraph's, a hidden list item's.
	 */
	@Test
	public void theAddressIsTheExtractionsBlock() throws Exception {
		reader();
		WordprocessingMLPackage pkg = html("<p>FIRST</p><p style='display:none'>P_HIDDEN</p><p>SPAN_<span style='display:none'>S_HIDDEN</span>_AFTER</p>"
				+ "<table><tr><td>C1</td><td style='display:none'>TD_HIDDEN</td></tr></table>"
				+ "<ol><li>ONE</li><li style='display:none'>LI_HIDDEN</li><li>THREE</li></ol><p>LAST</p>");
		Inspection i = Inspect.of(pkg);
		Map<String, String> blocks = addresses(Texts.of(pkg, new TextOptions().hidden(true)).blocks(), new LinkedHashMap<String, String>());
		List<InspectFinding> hidden = of(i, InspectFinding.TEXT_HIDDEN);
		assertEquals(4, hidden.size());
		for (InspectFinding f : hidden) {
			String text = blocks.get(f.getAddress());
			assertTrue(f + " at " + f.getAddress() + ": " + blocks, text != null && text.contains(f.getExcerpt()));
			assertEquals("options: hidden: true; html reader: present", f.getReveal().toString());
			assertEquals(Visible.NO, f.getVisible());
		}
		assertTrue(blocks.get(hidden.get(1).getAddress()).equals("SPAN_S_HIDDEN_AFTER"));
	}

	/**
	 * The counts (REQ-903, REQ-905): what a chunk hides is omitted under the defaults and unseen under
	 * {@code hidden: true}, with an HTML reader; without one, the output does not hold it.
	 */
	@Test
	public void theCountsFollowMembership() throws Exception {
		reader();
		Inspection i = Inspect.of(html("<p>VISIBLE</p><p style='display:none'>HIDDEN</p>"));
		EnumSet<Inspection.Capability> reader = EnumSet.of(Inspection.Capability.HTML_READER);
		EnumSet<Inspection.Capability> none = EnumSet.noneOf(Inspection.Capability.class);
		Inspection.Counts plain = i.counts(new TextOptions(), Inspection.Format.PLAIN, reader);
		Inspection.Counts hidden = i.counts(new TextOptions().hidden(true), Inspection.Format.PLAIN, reader);
		Inspection.Counts noReader = i.counts(new TextOptions().hidden(true), Inspection.Format.PLAIN, none);
		assertEquals(1, plain.getOmitted() - hidden.getOmitted());
		assertEquals(1, hidden.getUnseen() - plain.getUnseen());
		assertEquals(plain.getOmitted(), noReader.getOmitted());
		assertEquals(plain.getUnseen(), noReader.getUnseen());
	}

	/**
	 * REQ-907's limits are checked before the import: a chunk over {@link HtmlChunkImport#MAX_ELEMENTS}
	 * (D11), counted in the tree jsoup built and the rewrite left, is skipped as {@code limit.elements},
	 * parsed but not imported; a chunk whose import is abandoned (the time limit, D10) is
	 * {@code unreadable}. The payload's own limit is {@code InspectTest}'s.
	 */
	@Test
	public void aChunkOverALimitIsSkippedBeforeTheImport() throws Exception {
		reader();
		StringBuilder many = new StringBuilder("<p style='display:none'>HIDDEN</p>");
		for (int n = 0; n < 30; n++) many.append("<p>P</p>");
		String was = Docx4jProperties.getProperty(HtmlChunkImport.MAX_ELEMENTS);
		Docx4jProperties.setProperty(HtmlChunkImport.MAX_ELEMENTS, "20");
		try {
			Inspection i = Inspect.of(html(many.toString()));
			assertEquals("[limit.elements@|/chunk.html|]", i.skipped().toString());
			assertEquals(InspectFinding.PARSED_HTML, parsed(i));
			assertTrue(hidden(i).isEmpty());
		} finally {
			Docx4jProperties.setProperty(HtmlChunkImport.MAX_ELEMENTS, was == null ? "50000" : was);
		}
		StringBuilder slow = new StringBuilder("<p style='display:none'>HIDDEN</p>");
		for (int n = 0; n < 20000; n++) slow.append("<p>P</p>");
		was = Docx4jProperties.getProperty(HtmlChunkImport.TIMEOUT_MILLIS);
		Docx4jProperties.setProperty(HtmlChunkImport.TIMEOUT_MILLIS, "50");
		try {
			Inspection i = Inspect.of(html(slow.toString()));
			assertEquals("[unreadable@|/chunk.html|]", i.skipped().toString());
			assertTrue(hidden(i).isEmpty());
		} finally {
			Docx4jProperties.setProperty(HtmlChunkImport.TIMEOUT_MILLIS, was == null ? "0" : was);
		}
	}
}
