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
 * says so ("Phase n changes this"), with what the result becomes.
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

	private static void readable() {
		assumeFalse("docx4j-ImportXHTML is on the class path", AltChunks.importerPresent());
	}

	// --- reading ----------------------------------------------------------------------------------

	/**
	 * Section 1, case 1: an HTML chunk's display:none text comes out as ordinary text, and
	 * {@code hidden: true} flags none of it; the inspector reports the chunk and nothing hidden in it.
	 * Phase 2 changes this: without the importer the chunk is not expanded
	 * ({@code altchunk.not_expanded}, D7); with it, the four hidden texts are hidden text, out by
	 * default. Phase 3: with the importer, a {@code text.hidden} finding for each of the three
	 * outermost elements and {@code parsed: html}; without it, the chunk is skipped as
	 * {@code not_decoded}.
	 */
	@Test
	public void htmlChunkDisplayNoneIsOrdinaryText() throws Exception {
		readable();
		WordprocessingMLPackage pkg = chunk(AltChunkType.Html, "<html><head>" + HIDING);
		List<String> expected = Arrays.asList("VISIBLE_A", "INLINE_NONE", "CLASS_NONE", "VISIBLE_SPAN_NONEB");
		TextResult result = Texts.of(pkg, new TextOptions());
		assertEquals(expected, blocks(result));
		assertEquals(new ArrayList<String>(), warnings(result));
		assertEquals(expected, blocks(Texts.of(pkg, new TextOptions().hidden(true))));
		Inspection inspection = Inspect.of(pkg);
		assertEquals(Arrays.asList("altchunk"), kinds(inspection));
		assertTrue(inspection.skipped().toString(), inspection.skipped().isEmpty());
	}

	/**
	 * Section 1, case 2: without the importer an XHTML chunk is read by jsoup, as case 1 is.
	 * Phase 2 changes this: not expanded without the importer (D7). The inspector also reports the
	 * chunk's part as {@code part.unknown}: the WD's known families (REQ-902) have no
	 * {@code application/xhtml+xml}, which the portfolio is asked to add (CR-053 phase 0).
	 */
	@Test
	public void xhtmlChunkWithoutTheImporterIsReadAsHtml() throws Exception {
		readable();
		WordprocessingMLPackage pkg = chunk(AltChunkType.Xhtml, XHTML + "<head>" + HIDING);
		List<String> expected = Arrays.asList("VISIBLE_A", "INLINE_NONE", "CLASS_NONE", "VISIBLE_SPAN_NONEB");
		assertEquals(expected, blocks(Texts.of(pkg, new TextOptions())));
		assertEquals(expected, blocks(Texts.of(pkg, new TextOptions().hidden(true))));
		assertEquals(Arrays.asList("part.unknown", "altchunk"), kinds(Inspect.of(pkg)));
	}

	/**
	 * Section 1, case 5 without the importer: an XHTML chunk that is not well-formed (an unclosed
	 * {@code p}, a stray {@code </div>}) is repaired by jsoup as Word repairs it (round 4's
	 * {@code xhtml-14}), its display:none text ordinary text. Phase 2 changes this: not expanded
	 * without the importer (D7); with it, read as HTML, NONE hidden.
	 */
	@Test
	public void xhtmlChunkNotWellFormedIsRepairedAsHtml() throws Exception {
		readable();
		WordprocessingMLPackage pkg = chunk(AltChunkType.Xhtml, XHTML + "<head><style>.h{display:none}</style></head>"
				+ "<body><p>BEFORE</p><p class='h'>NONE<p>UNCLOSED<p>AFTER_UNCLOSED</div><p>AFTER_STRAY</p></body></html>");
		assertEquals(Arrays.asList("BEFORE", "NONE", "UNCLOSED", "AFTER_UNCLOSED", "AFTER_STRAY"),
				blocks(Texts.of(pkg, new TextOptions())));
	}

	/**
	 * An MHT chunk without the importer is not expanded, with the warning; that stays (D7). The
	 * inspector scans its first HTML part today. Phase 3 changes the inspector's: without the
	 * importer, the chunk is skipped as {@code not_decoded} (its scripts and references still found).
	 */
	@Test
	public void mhtChunkWithoutTheImporterIsNotExpanded() throws Exception {
		readable();
		WordprocessingMLPackage pkg = chunk(AltChunkType.Mht, "MIME-Version: 1.0\r\n"
				+ "Content-Type: multipart/related; boundary=\"B\"\r\n\r\n"
				+ "--B\r\nContent-Type: text/html; charset=\"utf-8\"\r\n\r\n"
				+ "<html><body><p>MHT_TEXT</p><p style='display:none'>MHT_NONE</p></body></html>\r\n--B--\r\n");
		TextResult result = Texts.of(pkg, new TextOptions());
		assertEquals(new ArrayList<String>(), blocks(result));
		assertEquals(Arrays.asList("altchunk.not_expanded message/rfc822"), warnings(result));
		Inspection inspection = Inspect.of(pkg);
		assertEquals(Arrays.asList("altchunk"), kinds(inspection));
		assertTrue(inspection.skipped().toString(), inspection.skipped().isEmpty());
	}

	/**
	 * What a browser hides and Word shows (section 2): jsoup's reading drops {@code template} and
	 * {@code noscript} content, and keeps the {@code hidden} attribute's and a closed
	 * {@code details}' text. Phase 2 changes this: the importer, whose default stylesheet hides only
	 * {@code head}, {@code script} and a {@code select}'s options, shows all four, as Word does.
	 */
	@Test
	public void htmlChunkDropsTemplateAndNoscript() throws Exception {
		readable();
		WordprocessingMLPackage pkg = chunk(AltChunkType.Html, "<html><body><p>FIRST</p>"
				+ "<template><p>TEMPLATE_TEXT</p></template><noscript><p>NOSCRIPT_TEXT</p></noscript>"
				+ "<p hidden='hidden'>HIDDEN_ATTRIBUTE</p><details><summary>SUMMARY</summary><p>DETAILS_CLOSED</p></details>"
				+ "<p>LAST</p></body></html>");
		assertEquals(Arrays.asList("FIRST", "HIDDEN_ATTRIBUTE", "SUMMARY", "DETAILS_CLOSED", "LAST"),
				blocks(Texts.of(pkg, new TextOptions())));
	}

	/**
	 * Section 1a: jsoup's reading of a row span marks the first cell and gives the next row no
	 * continuation cell, so that row is a column short. Phase 2 changes this: {@code HtmlText}
	 * goes (D7); the importer gives the continuation cell.
	 */
	@Test
	public void htmlChunkRowSpanHasNoContinuationCell() throws Exception {
		readable();
		WordprocessingMLPackage pkg = chunk(AltChunkType.Html, "<html><body><table>"
				+ "<tr><td rowspan='2'>SPAN_A</td><td>CELL_B</td></tr><tr><td>CELL_C</td></tr></table></body></html>");
		List<TextBlock> blocks = Texts.of(pkg, new TextOptions()).blocks();
		assertEquals(1, blocks.size());
		List<TextRow> rows = blocks.get(0).getRows();
		assertEquals(2, rows.size());
		assertEquals(2, rows.get(0).getCells().size());
		assertTrue(rows.get(0).getCells().get(0).isMergeRestart());
		assertEquals(1, rows.get(1).getCells().size());
		assertEquals("CELL_C", rows.get(1).getCells().get(0).getBlocks().get(0).getText());
	}

	/**
	 * Section 1: the text model decodes a chunk with no BOM that is not UTF-8 as windows-1252, and
	 * the inspector by the {@code meta} charset (the WD's Chunks rule), so the two read one byte as
	 * different characters: here 0xD0, which ISO-8859-5 makes U+0430. Phase 2 changes this: one
	 * decoder, the inspector's, so the text model reads U+0430 too.
	 */
	@Test
	public void extractionAndInspectionDecodeAChunkDifferently() throws Exception {
		readable();
		byte[] bytes = ("<html><head><meta charset='ISO-8859-5'></head><body><p>CYRILLIC_Ð</p>"
				+ "<p><img src='http://cr053.invalid/Ð.png' alt=''></p></body></html>").getBytes(StandardCharsets.ISO_8859_1);
		WordprocessingMLPackage pkg = chunk(AltChunkType.Html, bytes);
		assertEquals(Arrays.asList("CYRILLIC_Ð"), blocks(Texts.of(pkg, new TextOptions())));
		String refs = null;
		for (InspectFinding f : Inspect.of(pkg).findings()) {
			if (f.getKind().equals(InspectFinding.ALTCHUNK_EXTERNAL_REF)) refs = f.getSummary();
		}
		assertEquals("http://cr053.invalid/а.png", refs);
	}

	// --- writing ----------------------------------------------------------------------------------

	/**
	 * Section 1, case 6: each {@code addAltChunk} path writes an HTML or XHTML chunk byte for byte,
	 * its XML declaration included, which Word refuses (section 2). Phase 1 changes this for
	 * {@code Html} and {@code Xhtml}: the declaration is stripped (D1, D2). A Flat OPC chunk keeps
	 * its declaration, which Word accepts: that stays.
	 */
	@Test
	public void addAltChunkWritesTheDeclaration() throws Exception {
		byte[] html = (DECLARATION + "<html><body><p>DECLARED_é</p></body></html>").getBytes(StandardCharsets.UTF_8);
		byte[] xhtml = (DECLARATION + XHTML + "<body><p>DECLARED_é</p></body></html>").getBytes(StandardCharsets.UTF_8);
		byte[] flatOpc = (DECLARATION + "<pkg:package xmlns:pkg='http://schemas.microsoft.com/office/2006/xmlPackage'/>")
				.getBytes(StandardCharsets.UTF_8);
		for (AltChunkType type : new AltChunkType[] { AltChunkType.Html, AltChunkType.Xhtml, AltChunkType.Xml }) {
			byte[] bytes = type == AltChunkType.Html ? html : type == AltChunkType.Xhtml ? xhtml : flatOpc;
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
				assertArrayEquals(type + ", path " + path, bytes, part.getBytes());
			}
		}
	}

	/**
	 * Section 1: the OpenDoPE XHTML binding, without the importer, writes the bound value as a
	 * {@code text/html} chunk, its declaration included, in the platform's charset
	 * ({@code BindingTraverserXSLT}'s {@code r.getBytes()}). Phase 1 changes this: the declaration
	 * is stripped and the chunk is UTF-8.
	 */
	@Test
	public void boundXhtmlValueKeepsItsDeclaration() throws Exception {
		readable();
		String value = DECLARATION + "<html><body><p>BOUND_é</p></body></html>";
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
		assertArrayEquals(value.getBytes(Charset.defaultCharset()), chunk);
	}

	/**
	 * Section 1: a loaded chunk's {@code AltChunkType} comes from its part name's extension, so a
	 * {@code .htm} part declared {@code application/xhtml+xml} is {@code Html}, and a part whose
	 * extension docx4j does not know has no type, which {@code convertAltChunks} then leaves as it
	 * is, though the text model, which decides by the content type, reads it. Phase 1 changes this
	 * (D6): the type from the declared content type, the extension the fallback; the
	 * {@code text/plain} chunk is then converted.
	 */
	@Test
	public void loadedChunkTypeFollowsTheExtension() throws Exception {
		String af = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/aFChunk";
		WordprocessingMLPackage pkg = MiniDocx.body("<w:altChunk r:id='rIdA'/><w:altChunk r:id='rIdB'/>")
				.part("chunkA.htm", "application/xhtml+xml", af, "rIdA", XHTML + "<body><p>CHUNK_A</p></body></html>")
				.part("chunkB.dat", "text/plain", af, "rIdB", "CHUNK_B".getBytes(StandardCharsets.UTF_8))
				.load();
		RelationshipsPart rels = pkg.getMainDocumentPart().getRelationshipsPart();
		AlternativeFormatInputPart a = (AlternativeFormatInputPart) rels.getPart("rIdA");
		AlternativeFormatInputPart b = (AlternativeFormatInputPart) rels.getPart("rIdB");
		assertEquals(AltChunkType.Html, a.getAltChunkType());
		assertNull(b.getAltChunkType());
		if (!AltChunks.importerPresent()) {
			assertEquals(Arrays.asList("CHUNK_A", "CHUNK_B"), blocks(Texts.of(pkg, new TextOptions())));
		}

		WordprocessingMLPackage plain = MiniDocx.body("<w:altChunk r:id='rIdB'/>")
				.part("chunkB.dat", "text/plain", af, "rIdB", "CHUNK_B".getBytes(StandardCharsets.UTF_8))
				.load();
		plain.getMainDocumentPart().convertAltChunks();
		String xml = XmlUtils.marshaltoString(plain.getMainDocumentPart().getJaxbElement());
		assertTrue(xml, xml.contains("altChunk"));
		assertFalse(xml, xml.contains("CHUNK_B"));
	}
}
