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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.model.datastorage.BindingHandler;
import org.docx4j.model.datastorage.OpenDoPEHandler;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * CR-053 phase 0: what docx4j does today with an HTML, XHTML or MHT {@code altChunk}, written and
 * read, as the CR's section 1 measured it. The build runs with jsoup and without
 * docx4j-ImportXHTML, so the cases that need the importer (section 1's 3 to 5, section 1a's
 * importer column) are scratch-driver evidence recorded in the CR, and the tests that read a chunk
 * are skipped where the importer is on the class path. A test whose result a later phase changes
 * says so ("Phase n changes this"), with what the result becomes. Phases 1 and 2 changed the
 * writing and reading tests, which now say what phase 0 found. A reading test asserts both of
 * phase 2's results: with an HTML reader (jsoup and docx4j-ImportXHTML, a hand run's class path)
 * and without one (the build's).
 */
public class AltChunkHtmlTest {

	/** Section 1's chunk body: two visible paragraphs, and display:none inline, by a class and on a span. */
	private static final String HIDING = "<style>.h{display:none}</style></head><body><p>VISIBLE_A</p>"
			+ "<p style='display:none'>INLINE_NONE</p><div class='h'><p>CLASS_NONE</p></div>"
			+ "<p>VISIBLE_<span style='display:none'>SPAN_NONE</span>B</p></body></html>";

	private static final String XHTML = "<html xmlns='http://www.w3.org/1999/xhtml'>";

	private static final String DECLARATION = "<?xml version='1.0' encoding='UTF-8'?>";

	private static WordprocessingMLPackage chunk(AltChunkType type, byte[] bytes) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().addAltChunk(type, bytes);
		return pkg;
	}

	private static WordprocessingMLPackage chunk(AltChunkType type, String markup) throws Exception {
		return chunk(type, markup.getBytes(StandardCharsets.UTF_8));
	}

	/** Each block's text, with "{H}" before a hidden block and around a hidden run's text. */
	private static List<String> blocks(TextResult result) {
		List<String> out = new ArrayList<String>();
		for (TextBlock block : result.blocks()) {
			StringBuilder sb = new StringBuilder(block.isHidden() ? "{H}" : "");
			if (block.getKind() == TextBlock.Kind.TABLE) sb.append(block.getText());
			for (TextRun run : block.getRuns()) {
				if (run.getText() == null) continue;
				sb.append(run.isHidden() ? "{H}" + run.getText() + "{H}" : run.getText());
			}
			out.add(sb.toString());
		}
		return out;
	}

	private static List<String> kinds(Inspection inspection) {
		List<String> out = new ArrayList<String>();
		for (InspectFinding f : inspection.findings()) out.add(f.getKind());
		return out;
	}

	private static List<String> warnings(TextResult result) {
		List<String> out = new ArrayList<String>();
		for (TextWarning w : result.warnings()) out.add(w.getKind() + " " + w.getContentType());
		return out;
	}

	/** Where docx4j-ImportXHTML is on the class path, the binding imports XHTML rather than writing a chunk. */
	private static void noImporter() {
		assumeFalse("docx4j-ImportXHTML is on the class path", AltChunks.importerPresent());
	}

	/** An HTML reader present: jsoup and docx4j-ImportXHTML (the build has jsoup only). */
	private static boolean reader() {
		return HtmlChunkImport.readerPresent();
	}

	/** Without an HTML reader, an HTML, XHTML or MHT chunk is not expanded, with the warning (D7). */
	private static void notExpanded(TextResult result, String contentType) {
		assertEquals(new ArrayList<String>(), blocks(result));
		assertEquals(Arrays.asList("altchunk.not_expanded " + contentType), warnings(result));
	}

	private static List<String> nonEmpty(List<String> blocks) {
		List<String> out = new ArrayList<String>();
		for (String b : blocks) if (!b.isEmpty()) out.add(b);
		return out;
	}

	// --- reading ----------------------------------------------------------------------------------

	/**
	 * Section 1, case 1, as phase 2 leaves it: with an HTML reader, the four display:none texts are
	 * hidden text, out by default and flagged hidden under {@code hidden: true} (D5); without one,
	 * the chunk is not expanded (D7). Phase 0 found them ordinary text. The inspector is phase 3's:
	 * it reports the chunk and nothing hidden in it, and with the reader it is to report a
	 * {@code text.hidden} for each of the three outermost elements and {@code parsed: html}; without
	 * it, to skip the chunk as {@code not_decoded}.
	 */
	@Test
	public void htmlChunkDisplayNoneIsHiddenText() throws Exception {
		WordprocessingMLPackage pkg = chunk(AltChunkType.Html, "<html><head>" + HIDING);
		TextResult result = Texts.of(pkg, new TextOptions());
		if (reader()) {
			assertEquals(Arrays.asList("VISIBLE_A", "VISIBLE_B"), blocks(result));
			assertEquals(new ArrayList<String>(), warnings(result));
			assertEquals(Arrays.asList("VISIBLE_A", "{H}{H}INLINE_NONE{H}", "{H}{H}CLASS_NONE{H}", "VISIBLE_{H}SPAN_NONE{H}B"),
					blocks(Texts.of(pkg, new TextOptions().hidden(true))));
		} else {
			notExpanded(result, "text/html");
		}
		Inspection inspection = Inspect.of(pkg);
		assertEquals(Arrays.asList("altchunk"), kinds(inspection));
		assertTrue(inspection.skipped().toString(), inspection.skipped().isEmpty());
	}

	/**
	 * Section 1, cases 2 and 3, as phase 2 leaves them: an XHTML chunk is read as an HTML one is, by
	 * jsoup and the importer, as Word reads it (D9 (iii)); without an HTML reader it is not expanded.
	 * Phase 0 found it read by jsoup alone without the importer, and with the importer its hidden text
	 * lost. The inspector reports the chunk's part as {@code part.unknown}: the WD's known families
	 * (REQ-902) have no {@code application/xhtml+xml} yet (WD 0.26).
	 */
	@Test
	public void xhtmlChunkIsReadAsHtml() throws Exception {
		WordprocessingMLPackage pkg = chunk(AltChunkType.Xhtml, XHTML + "<head>" + HIDING);
		TextResult result = Texts.of(pkg, new TextOptions());
		if (reader()) {
			assertEquals(Arrays.asList("VISIBLE_A", "VISIBLE_B"), blocks(result));
			assertEquals(Arrays.asList("VISIBLE_A", "{H}{H}INLINE_NONE{H}", "{H}{H}CLASS_NONE{H}", "VISIBLE_{H}SPAN_NONE{H}B"),
					blocks(Texts.of(pkg, new TextOptions().hidden(true))));
		} else {
			notExpanded(result, "application/xhtml+xml");
		}
		assertEquals(Arrays.asList("part.unknown", "altchunk"), kinds(Inspect.of(pkg)));
	}

	/**
	 * Section 1, case 5, as phase 2 leaves it: an XHTML chunk that is not well-formed (an unclosed
	 * {@code p}, a stray {@code </div>}) is repaired by jsoup as Word repairs it (round 4's
	 * {@code xhtml-14}), NONE hidden; without an HTML reader, not expanded. Phase 0 found the importer
	 * failing on it, and jsoup's reading with NONE ordinary text.
	 */
	@Test
	public void xhtmlChunkNotWellFormedIsRepairedAsHtml() throws Exception {
		WordprocessingMLPackage pkg = chunk(AltChunkType.Xhtml, XHTML + "<head><style>.h{display:none}</style></head>"
				+ "<body><p>BEFORE</p><p class='h'>NONE<p>UNCLOSED<p>AFTER_UNCLOSED</div><p>AFTER_STRAY</p></body></html>");
		TextResult result = Texts.of(pkg, new TextOptions());
		if (reader()) {
			assertEquals(Arrays.asList("BEFORE", "UNCLOSED", "AFTER_UNCLOSED", "AFTER_STRAY"), blocks(result));
			assertEquals(Arrays.asList("BEFORE", "{H}{H}NONE{H}", "UNCLOSED", "AFTER_UNCLOSED", "AFTER_STRAY"),
					blocks(Texts.of(pkg, new TextOptions().hidden(true))));
		} else {
			notExpanded(result, "application/xhtml+xml");
		}
	}

	/**
	 * An MHT chunk: its first HTML part, read as an HTML chunk is, its display:none text hidden; without
	 * an HTML reader, not expanded, with the warning, as before. The inspector scans its first HTML part
	 * today. Phase 3 changes the inspector's: without the reader, the chunk is skipped as
	 * {@code not_decoded} (its scripts and references still found).
	 */
	@Test
	public void mhtChunkIsReadAsItsHtml() throws Exception {
		WordprocessingMLPackage pkg = chunk(AltChunkType.Mht, "MIME-Version: 1.0\r\n"
				+ "Content-Type: multipart/related; boundary=\"B\"\r\n\r\n"
				+ "--B\r\nContent-Type: text/html; charset=\"utf-8\"\r\n\r\n"
				+ "<html><body><p>MHT_TEXT</p><p style='display:none'>MHT_NONE</p></body></html>\r\n--B--\r\n");
		TextResult result = Texts.of(pkg, new TextOptions());
		if (reader()) {
			assertEquals(Arrays.asList("MHT_TEXT"), blocks(result));
			assertEquals(Arrays.asList("MHT_TEXT", "{H}{H}MHT_NONE{H}"), blocks(Texts.of(pkg, new TextOptions().hidden(true))));
		} else {
			notExpanded(result, "message/rfc822");
		}
		Inspection inspection = Inspect.of(pkg);
		assertEquals(Arrays.asList("altchunk"), kinds(inspection));
		assertTrue(inspection.skipped().toString(), inspection.skipped().isEmpty());
	}

	/**
	 * What a browser hides and Word shows (section 2), as phase 2 leaves it: the importer, whose
	 * default stylesheet hides only {@code head}, {@code script} and a {@code select}'s options, shows
	 * {@code template}, {@code noscript}, the {@code hidden} attribute's and a closed {@code details}'
	 * text, as Word does. Phase 0 found {@code template} and {@code noscript} dropped by jsoup's reading.
	 */
	@Test
	public void htmlChunkShowsTemplateAndNoscript() throws Exception {
		WordprocessingMLPackage pkg = chunk(AltChunkType.Html, "<html><body><p>FIRST</p>"
				+ "<template><p>TEMPLATE_TEXT</p></template><noscript><p>NOSCRIPT_TEXT</p></noscript>"
				+ "<p hidden='hidden'>HIDDEN_ATTRIBUTE</p><details><summary>SUMMARY</summary><p>DETAILS_CLOSED</p></details>"
				+ "<p>LAST</p></body></html>");
		TextResult result = Texts.of(pkg, new TextOptions());
		if (reader()) {
			assertEquals(Arrays.asList("FIRST", "TEMPLATE_TEXT", "NOSCRIPT_TEXT", "HIDDEN_ATTRIBUTE", "SUMMARY", "DETAILS_CLOSED", "LAST"),
					nonEmpty(blocks(result)));
		} else {
			notExpanded(result, "text/html");
		}
	}

	/**
	 * Section 1a, as phase 2 leaves it: the importer gives a row span's continuation cell, so the
	 * second row has its two cells. Phase 0 found jsoup's reading a cell short.
	 */
	@Test
	public void htmlChunkRowSpanHasItsContinuationCell() throws Exception {
		WordprocessingMLPackage pkg = chunk(AltChunkType.Html, "<html><body><table>"
				+ "<tr><td rowspan='2'>SPAN_A</td><td>CELL_B</td></tr><tr><td>CELL_C</td></tr></table></body></html>");
		TextResult result = Texts.of(pkg, new TextOptions());
		if (!reader()) {
			notExpanded(result, "text/html");
			return;
		}
		List<TextBlock> blocks = result.blocks();
		assertEquals(1, blocks.size());
		List<TextRow> rows = blocks.get(0).getRows();
		assertEquals(2, rows.size());
		assertEquals(2, rows.get(0).getCells().size());
		assertTrue(rows.get(0).getCells().get(0).isMergeRestart());
		assertEquals(2, rows.get(1).getCells().size());
		assertEquals("CELL_C", rows.get(1).getCells().get(1).getBlocks().get(0).getText());
	}

	/**
	 * Section 1, as phase 2 leaves it: one decoder, the inspector's (the WD's Chunks rule, with Word's
	 * last step), so the text model and the inspector read 0xD0 under {@code meta charset='ISO-8859-5'}
	 * alike, as U+0430. Phase 0 found the text model reading it as windows-1252's U+00D0.
	 */
	@Test
	public void extractionAndInspectionDecodeAChunkAlike() throws Exception {
		byte[] bytes = ("<html><head><meta charset='ISO-8859-5'></head><body><p>CYRILLIC_Ð</p>"
				+ "<p><img src='http://cr053.invalid/Ð.png' alt=''></p></body></html>").getBytes(StandardCharsets.ISO_8859_1);
		assertEquals(InspectPayloads.decode(bytes), HtmlChunks.decode(bytes));
		assertTrue(HtmlChunks.decode(bytes).contains("CYRILLIC_а"));
		WordprocessingMLPackage pkg = chunk(AltChunkType.Html, bytes);
		TextResult result = Texts.of(pkg, new TextOptions());
		if (reader()) {
			assertEquals(Arrays.asList("CYRILLIC_а"), nonEmpty(blocks(result)));
		} else {
			notExpanded(result, "text/html");
		}
		String refs = null;
		for (InspectFinding f : Inspect.of(pkg).findings()) {
			if (f.getKind().equals(InspectFinding.ALTCHUNK_EXTERNAL_REF)) refs = f.getSummary();
		}
		assertEquals("http://cr053.invalid/а.png", refs);
	}

	// --- writing ----------------------------------------------------------------------------------

	private static final byte[] UTF8_BOM = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

	private static byte[] bom(String markup) {
		byte[] utf8 = markup.getBytes(StandardCharsets.UTF_8);
		byte[] out = Arrays.copyOf(UTF8_BOM, UTF8_BOM.length + utf8.length);
		System.arraycopy(utf8, 0, out, UTF8_BOM.length, utf8.length);
		return out;
	}

	/**
	 * Section 1, case 6, as phase 1 leaves it (D1, D2): each {@code addAltChunk} path writes an HTML
	 * or XHTML chunk without its XML declaration, which Word refuses (section 2), and with a UTF-8
	 * BOM for the UTF-8 the declaration named. A Flat OPC chunk keeps its declaration, which Word
	 * accepts. Phase 0 found all three written byte for byte. {@code AltChunkHtmlWritingTest} has
	 * the other encodings.
	 */
	@Test
	public void addAltChunkStripsTheDeclaration() throws Exception {
		String htmlBody = "<html><body><p>DECLARED_é</p></body></html>";
		String xhtmlBody = XHTML + "<body><p>DECLARED_é</p></body></html>";
		byte[] html = (DECLARATION + htmlBody).getBytes(StandardCharsets.UTF_8);
		byte[] xhtml = (DECLARATION + xhtmlBody).getBytes(StandardCharsets.UTF_8);
		byte[] flatOpc = (DECLARATION + "<pkg:package xmlns:pkg='http://schemas.microsoft.com/office/2006/xmlPackage'/>")
				.getBytes(StandardCharsets.UTF_8);
		for (AltChunkType type : new AltChunkType[] { AltChunkType.Html, AltChunkType.Xhtml, AltChunkType.Xml }) {
			byte[] bytes = type == AltChunkType.Html ? html : type == AltChunkType.Xhtml ? xhtml : flatOpc;
			byte[] expected = type == AltChunkType.Html ? bom(htmlBody) : type == AltChunkType.Xhtml ? bom(xhtmlBody) : flatOpc;
			for (int path = 0; path < 4; path++) {
				WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
				MainDocumentPart main = pkg.getMainDocumentPart();
				AlternativeFormatInputPart part;
				switch (path) {
				case 0: part = main.addAltChunk(type, bytes); break;
				case 1: part = main.addAltChunk(type, new ByteArrayInputStream(bytes)); break;
				case 2: part = main.addAltChunk(type, bytes, main); break;
				default: part = main.addAltChunk(type, new ByteArrayInputStream(bytes), main); break;
				}
				assertArrayEquals(type + ", path " + path, expected, part.getBytes());
			}
		}
	}

	/**
	 * Section 1, as phase 1 leaves it: the OpenDoPE XHTML binding, without the importer, writes the
	 * bound value as a {@code text/html} chunk in UTF-8 with a BOM, without its declaration. Phase 0
	 * found the declaration kept and the value in the platform's charset
	 * ({@code BindingTraverserXSLT}'s {@code r.getBytes()}).
	 */
	@Test
	public void boundXhtmlValueIsUtf8WithoutItsDeclaration() throws Exception {
		noImporter();
		String body = "<html><head><meta charset='windows-1252'/></head><body><p>BOUND_é</p></body></html>";
		String value = DECLARATION + body;
		String impl = "docx4j.model.datastorage.BindingHandler.Implementation";
		Docx4jProperties.setProperty(impl, "BindingTraverserXSLT");
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new File(System.getProperty("user.dir")
				+ "/src/test/resources/OpenDoPE/text-bind-parity.docx"));
		CustomXmlDataStoragePart data = (CustomXmlDataStoragePart) pkg.getCustomXmlDataStorageParts()
				.get("{6A47349F-7005-4A97-AE9A-450217272349}".toLowerCase());
		if (data == null) data = (CustomXmlDataStoragePart) pkg.getCustomXmlDataStorageParts()
				.get("{6A47349F-7005-4A97-AE9A-450217272349}");
		assertNotNull(pkg.getCustomXmlDataStorageParts().keySet().toString(), data);
		Element xhtmlVal = (Element) data.getData().getDocument().getElementsByTagName("xhtmlVal").item(0);
		xhtmlVal.setTextContent(value);
		OpenDoPEHandler odh = new OpenDoPEHandler(pkg);
		pkg = odh.preprocess();
		BindingHandler bh = new BindingHandler(pkg);
		bh.setStartingIdForNewBookmarks(odh.getNextBookmarkId());
		bh.applyBindings(pkg.getMainDocumentPart());
		byte[] chunk = null;
		RelationshipsPart rels = pkg.getMainDocumentPart().getRelationshipsPart();
		for (Relationship rel : rels.getRelationships().getRelationship()) {
			Part part = rels.getPart(rel);
			if (part instanceof AlternativeFormatInputPart
					&& ((AlternativeFormatInputPart) part).getAltChunkType() == AltChunkType.Html) {
				chunk = ((AlternativeFormatInputPart) part).getBytes();
			}
		}
		assertNotNull("no HTML chunk", chunk);
		// the value's own meta charset is rewritten, since the bytes are UTF-8
		assertArrayEquals(new String(chunk, StandardCharsets.UTF_8), bom(body.replace("windows-1252", "UTF-8")), chunk);
	}

	/**
	 * Section 1, as phase 1 leaves it (D6): a loaded chunk's {@code AltChunkType} comes from its
	 * declared content type, the part name's extension the fallback, and the declared content type
	 * is kept. So a {@code .htm} part declared {@code application/xhtml+xml} is {@code Xhtml}, and
	 * the {@code text/plain} chunk with an extension docx4j does not know is {@code TextPlain}, which
	 * {@code convertAltChunks} now converts. Phase 0 found the first {@code Html} and the second
	 * without a type, left as it was.
	 */
	@Test
	public void loadedChunkTypeFollowsTheContentType() throws Exception {
		String af = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/aFChunk";
		WordprocessingMLPackage pkg = MiniDocx.body("<w:altChunk r:id='rIdA'/><w:altChunk r:id='rIdB'/>")
				.part("chunkA.htm", "application/xhtml+xml", af, "rIdA", XHTML + "<body><p>CHUNK_A</p></body></html>")
				.part("chunkB.dat", "text/plain", af, "rIdB", "CHUNK_B".getBytes(StandardCharsets.UTF_8))
				.part("chunkC.bin", "application/rtf", af, "rIdC", "{\\rtf1 CHUNK_C}".getBytes(StandardCharsets.UTF_8))
				.part("chunkD.html", "application/octet-stream", af, "rIdD", "<p>CHUNK_D</p>")
				.part("chunkE.dat", "application/octet-stream", af, "rIdE", "CHUNK_E".getBytes(StandardCharsets.UTF_8))
				.part("chunkF.txt", "Text/HTML; charset=utf-8", af, "rIdF", "<p>CHUNK_F</p>")
				.load();
		RelationshipsPart rels = pkg.getMainDocumentPart().getRelationshipsPart();
		AlternativeFormatInputPart a = (AlternativeFormatInputPart) rels.getPart("rIdA");
		AlternativeFormatInputPart b = (AlternativeFormatInputPart) rels.getPart("rIdB");
		assertEquals(AltChunkType.Xhtml, a.getAltChunkType());
		assertEquals(AltChunkType.TextPlain, b.getAltChunkType());
		assertEquals(AltChunkType.Rtf, ((AlternativeFormatInputPart) rels.getPart("rIdC")).getAltChunkType());
		// the extension, where docx4j knows no type for the content type
		assertEquals(AltChunkType.Html, ((AlternativeFormatInputPart) rels.getPart("rIdD")).getAltChunkType());
		assertNull(((AlternativeFormatInputPart) rels.getPart("rIdE")).getAltChunkType());
		assertEquals(AltChunkType.Html, ((AlternativeFormatInputPart) rels.getPart("rIdF")).getAltChunkType());
		// the declared content type is kept, not replaced by the type's own
		assertEquals("application/rtf", rels.getPart("rIdC").getContentType());
		assertEquals("application/octet-stream", rels.getPart("rIdD").getContentType());
		if (reader()) {
			assertEquals(Arrays.asList("CHUNK_A", "CHUNK_B"), blocks(Texts.of(pkg, new TextOptions())));
		} else {
			// without an HTML reader, the XHTML chunk is not expanded (D7)
			assertEquals(Arrays.asList("CHUNK_B"), blocks(Texts.of(pkg, new TextOptions())));
		}

		WordprocessingMLPackage plain = MiniDocx.body("<w:altChunk r:id='rIdB'/>")
				.part("chunkB.dat", "text/plain", af, "rIdB", "CHUNK_B".getBytes(StandardCharsets.UTF_8))
				.load();
		plain.getMainDocumentPart().convertAltChunks();
		String xml = XmlUtils.marshaltoString(plain.getMainDocumentPart().getJaxbElement());
		assertFalse(xml, xml.contains("altChunk"));
		assertTrue(xml, xml.contains("CHUNK_B"));
	}
}
