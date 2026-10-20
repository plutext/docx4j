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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.wml.Style;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * CR-053 phase 2: an HTML, XHTML or MHT {@code altChunk} read as Word reads it. The decoder, the
 * rewrite that makes the importer's CSS engine apply what Word applies, and the guards are tested
 * with jsoup alone, as the build runs. The tests that read a chunk through docx4j-ImportXHTML
 * ({@link HtmlChunkImport#readerPresent()}) run where it is on the class path, a hand run's (the
 * CR's section 11 says how), and are skipped otherwise. Their expectations are Word 365's, measured
 * in phase 0 (CR-053 section 10.6).
 */
public class AltChunkHtmlReadingTest {

	private static final String UTF8_TEXT = "café “quoted” Ωμέγα";

	private static void reader() {
		assumeTrue("an HTML reader: jsoup and docx4j-ImportXHTML", HtmlChunkImport.readerPresent());
	}

	private static WordprocessingMLPackage chunk(AltChunkType type, String markup) throws Exception {
		return chunk(type, markup.getBytes(StandardCharsets.UTF_8));
	}

	private static WordprocessingMLPackage chunk(AltChunkType type, byte[] bytes) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().addAltChunk(type, bytes);
		return pkg;
	}

	/** Each block's text, with "{H}" before a hidden block and around a hidden run's text; empty blocks left out. */
	private static List<String> blocks(TextResult result) {
		List<String> out = new ArrayList<String>();
		for (TextBlock block : result.blocks()) {
			StringBuilder sb = new StringBuilder(block.isHidden() ? "{H}" : "");
			if (block.getKind() == TextBlock.Kind.TABLE) sb.append(block.getText().replace('\n', '/').replace('\t', '|'));
			for (TextRun run : block.getRuns()) {
				if (run.getText() == null) continue;
				sb.append(run.isHidden() ? "{H}" + run.getText() + "{H}" : run.getText());
			}
			if (sb.length() > 0) out.add(sb.toString());
		}
		return out;
	}

	private static List<String> shown(WordprocessingMLPackage pkg) {
		return blocks(Texts.of(pkg, new TextOptions()));
	}

	private static List<String> all(WordprocessingMLPackage pkg) {
		return blocks(Texts.of(pkg, new TextOptions().hidden(true)));
	}

	// --- decoding ----------------------------------------------------------------------------------------

	/**
	 * The shared decoder (the WD's Chunks rule, 0.27): BOM, then UTF-16 sniffed, then a {@code meta}'s
	 * charset (or {@code http-equiv}'s), then UTF-8 where valid, else windows-1252; ISO-8859-1 and
	 * US-ASCII as windows-1252 (WHATWG's labels). An XML declaration names nothing.
	 */
	@Test
	public void decoderFollowsWordsOrder() {
		Charset cp1252 = Charset.forName("windows-1252");
		String body = "<p>" + UTF8_TEXT + "</p>";
		byte[] bom = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
		assertEquals(body, HtmlChunks.decode(concat(bom, body.getBytes(StandardCharsets.UTF_8))));
		assertEquals(body, HtmlChunks.decode(concat(new byte[] { (byte) 0xFF, (byte) 0xFE }, body.getBytes(StandardCharsets.UTF_16LE))));
		assertEquals(body, HtmlChunks.decode(concat(new byte[] { (byte) 0xFE, (byte) 0xFF }, body.getBytes(StandardCharsets.UTF_16BE))));
		assertEquals(body, HtmlChunks.decode(body.getBytes(StandardCharsets.UTF_16LE)));
		assertEquals(body, HtmlChunks.decode(body.getBytes(StandardCharsets.UTF_8)));
		// not UTF-8, nothing declared: windows-1252 (enc-3)
		String latin = "<p>café “quoted” €</p>";
		assertEquals(latin, HtmlChunks.decode(latin.getBytes(cp1252)));
		// a meta, either form, over the bytes (enc-4, enc-5, enc-6)
		String meta = "<meta charset='windows-1252'>" + latin;
		assertEquals(meta, HtmlChunks.decode(meta.getBytes(cp1252)));
		String equiv = "<meta http-equiv='Content-Type' content='text/html; charset=windows-1252'>" + latin;
		assertEquals(equiv, HtmlChunks.decode(equiv.getBytes(cp1252)));
		String says1252 = "<meta charset=\"windows-1252\"><p>café</p>";
		assertEquals("<meta charset=\"windows-1252\"><p>cafÃ©</p>", HtmlChunks.decode(says1252.getBytes(StandardCharsets.UTF_8)));
		// a BOM over a meta (enc-7)
		assertEquals(says1252, HtmlChunks.decode(concat(bom, says1252.getBytes(StandardCharsets.UTF_8))));
		// ISO-8859-1 read as windows-1252, its curly quotes kept
		String iso = "<meta charset='ISO-8859-1'>" + latin;
		assertEquals(iso, HtmlChunks.decode(iso.getBytes(cp1252)));
		// an XML declaration names nothing (Word refuses a chunk that has one): the bytes decide
		String declared = "<?xml version='1.0' encoding='iso-8859-1'?>" + latin;
		assertEquals(declared, HtmlChunks.decode(declared.getBytes(cp1252)));
		String declaredUtf8 = "<?xml version='1.0' encoding='windows-1252'?><p>café</p>";
		assertEquals(declaredUtf8, HtmlChunks.decode(declaredUtf8.getBytes(StandardCharsets.UTF_8)));
		// a UTF-16 or unknown name ignored
		String wrong = "<meta charset='UTF-16'>" + body;
		assertEquals(wrong, HtmlChunks.decode(wrong.getBytes(StandardCharsets.UTF_8)));
		String unknown = "<meta charset='x-no-such'>" + body;
		assertEquals(unknown, HtmlChunks.decode(unknown.getBytes(StandardCharsets.UTF_8)));
		// the inspector reads the same
		assertEquals(HtmlChunks.decode(iso.getBytes(cp1252)), InspectPayloads.decode(iso.getBytes(cp1252)));
	}

	private static byte[] concat(byte[] a, byte[] b) {
		byte[] out = Arrays.copyOf(a, a.length + b.length);
		System.arraycopy(b, 0, out, a.length, b.length);
		return out;
	}

	// --- the CSS Word applies (D4) --------------------------------------------------------------------

	/** The selectors Word applies: type, class, id and descendant (CR-053 section 10.6, css-1, css-3). */
	@Test
	public void cssKeepsTheRulesWordApplies() {
		Set<String> none = Collections.emptySet();
		String css = "/* a comment */ p { display: none } .h{display:none} #id { color: red }\n"
				+ "div.box p, div.kid > p { display: none }\n"
				+ "p[data-h], .a.b, p:first-child, a::before, * { display: none }\n"
				+ "@media screen { .ms { display: none } } @media print { .mp { display: none } }\n"
				+ "@import url(x.css); @font-face { font-family: F }\n"
				+ "td.x  { content: \"a { b } , c\" }";
		assertEquals("p { display: none }\n.h {display:none}\n#id { color: red }\ndiv.box p { display: none }\n"
				+ "@font-face { font-family: F }\ntd.x { content: \"a { b } , c\" }\n", HtmlChunks.css(css, none));
		assertTrue(HtmlChunks.wordSelector("p.os", none));
		assertTrue(HtmlChunks.wordSelector("div#x.y span", none));
		assertFalse(HtmlChunks.wordSelector("p + p", none));
		assertFalse(HtmlChunks.wordSelector("p ~ p", none));
		assertFalse(HtmlChunks.wordSelector("p#a#b", none));
	}

	/**
	 * A type or single-class rule goes where the host defines the Word style it would go through:
	 * the host's definition wins in Word (section 10.6, host-1, host-2).
	 */
	@Test
	public void cssDropsARuleTheHostsStyleOverrides() {
		Set<String> host = new HashSet<String>(Arrays.asList("normal (web)", "heading 6", "hid"));
		assertEquals("h5 {display:none}\n.other {display:none}\np.hid {display:none}\ndiv p {display:none}\n",
				HtmlChunks.css("p {display:none} h6 {display:none} h5 {display:none} .hid {display:none} .HID {display:none}"
						+ " .other {display:none} p.hid {display:none} div p {display:none}", host));
	}

	// --- the rewrite --------------------------------------------------------------------------------

	/**
	 * The tree rewritten in place, its nodes moved and never copied: a body {@code style} into the
	 * head; an element Word does not know unwrapped, its attributes gone and its content kept; spans
	 * clamped; floats defused.
	 */
	@Test
	public void rewriteMovesNodesAsWordReadsThem() {
		Document dom = JsoupHtml.parse("﻿<html><head><style>.a{display:none}</style></head><body>"
				+ "<p>BEFORE</p><style>.b{display:none}</style>"
				+ "<article class='a' style='display:none' id='z'><p>INSIDE</p></article>"
				+ "<zz>INLINE</zz><svg><text>SVG_TEXT</text></svg>"
				+ "<table><tr><td colspan='99999' rowspan='9999999999'>X</td><td colspan='junk' rowspan='0'>Y</td></tr></table>"
				+ "</body></html>");
		Element body = (Element) dom.getElementsByTagName("body").item(0);
		Element bodyStyle = (Element) body.getElementsByTagName("style").item(0);
		Element inside = (Element) dom.getElementsByTagName("article").item(0).getFirstChild();
		HtmlChunks.rewrite(dom, Collections.<String>emptySet());
		Element head = (Element) dom.getElementsByTagName("head").item(0);
		assertSame(head, bodyStyle.getParentNode());
		assertEquals(2, head.getElementsByTagName("style").getLength());
		assertEquals(0, dom.getElementsByTagName("article").getLength());
		assertEquals(0, dom.getElementsByTagName("zz").getLength());
		assertEquals(0, dom.getElementsByTagName("svg").getLength());
		assertSame(body, inside.getParentNode());
		assertEquals("BEFOREINSIDEINLINESVG_TEXTXY", body.getTextContent());
		Element x = (Element) dom.getElementsByTagName("td").item(0);
		Element y = (Element) dom.getElementsByTagName("td").item(1);
		assertEquals("1000", x.getAttribute("colspan"));
		assertEquals("65534", x.getAttribute("rowspan"));
		assertFalse(y.hasAttribute("colspan"));
		assertEquals("0", y.getAttribute("rowspan"));
		assertEquals("float:none !important", inside.getAttribute("style"));
		assertFalse(((Element) head.getElementsByTagName("style").item(0)).hasAttribute("style"));
	}

	// --- the guards (section 4b, D10, D11) ----------------------------------------------------------------

	/** A chunk whose tree, as parsed, has more elements than the limit is not read (D11, WD 0.27): those the rewrite unwraps count. */
	@Test
	public void overTheElementLimitIsNotRead() throws Exception {
		StringBuilder html = new StringBuilder("<html><body>");
		for (int i = 0; i < 30; i++) html.append("<p>P").append(i).append("</p>");
		WordprocessingMLPackage pkg = chunk(AltChunkType.Html, html.append("</body></html>").toString());
		AlternativeFormatInputPart part = (AlternativeFormatInputPart) pkg.getMainDocumentPart().getRelationshipsPart()
				.getRelationships().getRelationship().stream()
				.map(r -> pkg.getMainDocumentPart().getRelationshipsPart().getPart(r))
				.filter(p -> p instanceof AlternativeFormatInputPart).findFirst().get();
		assertNotNull(HtmlChunkImport.tree(part, "test"));
		String was = Docx4jProperties.getProperty(HtmlChunkImport.MAX_ELEMENTS);
		Docx4jProperties.setProperty(HtmlChunkImport.MAX_ELEMENTS, "20");
		try {
			assertNull(HtmlChunkImport.tree(part, "test"));
			TextResult result = Texts.of(pkg, new TextOptions());
			assertTrue(result.blocks().isEmpty());
			assertEquals("altchunk.not_expanded", result.warnings().get(0).getKind());
			// counted as parsed (WD 0.27's REQ-513): 14 unknown elements count, though the rewrite unwraps them
			StringBuilder unknown = new StringBuilder("<html><body>");
			for (int i = 0; i < 14; i++) unknown.append("<zz>");
			for (int i = 0; i < 5; i++) unknown.append("<p>Q").append(i).append("</p>");
			WordprocessingMLPackage wrapped = chunk(AltChunkType.Html, unknown.append("</body></html>").toString());
			AlternativeFormatInputPart wrappedPart = (AlternativeFormatInputPart) wrapped.getMainDocumentPart().getRelationshipsPart()
					.getRelationships().getRelationship().stream()
					.map(r -> wrapped.getMainDocumentPart().getRelationshipsPart().getPart(r))
					.filter(p -> p instanceof AlternativeFormatInputPart).findFirst().get();
			assertNull("22 elements as parsed, 8 once unwrapped", HtmlChunkImport.tree(wrappedPart, "test"));
		} finally {
			Docx4jProperties.setProperty(HtmlChunkImport.MAX_ELEMENTS, was == null ? "50000" : was);
		}
	}

	/** Anything the import throws is caught, an Error included (a layout engine's stack overflow); the chunk is not read. */
	@Test
	public void anErrorFromTheImportIsCaught() {
		assertNull(HtmlChunkImport.guarded("test", () -> {
			throw new StackOverflowError("deep");
		}));
		assertNull(HtmlChunkImport.guarded("test", () -> {
			throw new IllegalStateException("broken");
		}));
		assertEquals(Arrays.asList("ok"), HtmlChunkImport.guarded("test", () -> Arrays.<Object>asList("ok")));
	}

	/** With the opt-in timeout, an import that takes longer is abandoned (D10 (b)). */
	@Test
	public void aSlowImportIsAbandonedUnderTheTimeout() {
		String was = Docx4jProperties.getProperty(HtmlChunkImport.TIMEOUT_MILLIS);
		Docx4jProperties.setProperty(HtmlChunkImport.TIMEOUT_MILLIS, "50");
		try {
			long start = System.nanoTime();
			assertNull(HtmlChunkImport.guarded("test", () -> {
				Thread.sleep(5000);
				return Arrays.<Object>asList("late");
			}));
			assertTrue((System.nanoTime() - start) / 1000000 < 4000);
			assertEquals(Arrays.asList("ok"), HtmlChunkImport.guarded("test", () -> Arrays.<Object>asList("ok")));
			assertNull(HtmlChunkImport.guarded("test", () -> {
				throw new StackOverflowError("deep, on the import's thread");
			}));
		} finally {
			Docx4jProperties.setProperty(HtmlChunkImport.TIMEOUT_MILLIS, was == null ? "0" : was);
		}
	}

	/**
	 * {@code convertAltChunks} leaves a chunk it can't convert and goes on to the others: it stopped
	 * at the first XHTML or MHT chunk when the importer was missing (CR-053 section 10.2).
	 */
	@Test
	public void convertAltChunksGoesOnPastAChunkItCannotConvert() throws Exception {
		assumeTrue(!HtmlChunkImport.importerPresent());
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().addAltChunk(AltChunkType.TextPlain, "PLAIN_CHUNK".getBytes(StandardCharsets.UTF_8));
		pkg.getMainDocumentPart().addAltChunk(AltChunkType.Xhtml, "<html><body><p>X</p></body></html>".getBytes(StandardCharsets.UTF_8));
		pkg.getMainDocumentPart().convertAltChunks();
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement());
		assertTrue(xml, xml.contains("PLAIN_CHUNK"));
		assertEquals(xml, 1, xml.split("altChunk", -1).length - 1);
	}

	// --- through the importer: Word's results (section 10.6) ----------------------------------------------

	/** css-1: which display:none rules Word applies, and how the overrides go. */
	@Test
	public void wordsCascade() throws Exception {
		reader();
		WordprocessingMLPackage pkg = chunk(AltChunkType.Html, "<html><head><style>"
				+ "article{display:none} #hid{display:none} div.box p{display:none} div.kid > p{display:none} p[data-h]{display:none}"
				+ " .a.b{display:none} div.pc p:first-child{display:none} @media screen{.ms{display:none}} @media print{.mp{display:none}}"
				+ " .ol{display:none} .ol{display:block} p.os{display:block} .os{display:none} .imp{display:none !important} .ib{display:none}"
				+ "</style></head><body>"
				+ "<article><p>ART_ELEMENT_SHOWS</p></article><p id='hid'>ID_HIDDEN</p><div class='box'><p>DESC_HIDDEN</p></div>"
				+ "<div class='kid'><p>CHILD_SHOWS</p></div><p data-h='1'>ATTR_SHOWS</p><p class='a b'>TWO_CLASSES_SHOWS</p>"
				+ "<div class='pc'><p>FIRST_CHILD_SHOWS</p><p>SECOND</p></div><p class='ms'>MEDIA_SCREEN_SHOWS</p><p class='mp'>MEDIA_PRINT_SHOWS</p>"
				+ "<p class='ol'>LATER_SHOWS</p><p class='os'>SPECIFIC_SHOWS</p><p class='imp' style='display:block'>IMPORTANT_HIDDEN</p>"
				+ "<p class='ib' style='display:block'>INLINE_SHOWS</p><style>.bs{display:none}</style><p class='bs'>BODY_STYLE_HIDDEN</p>"
				+ "<zz style='display:none'>ZZ_SHOWS</zz></body></html>");
		assertEquals(Arrays.asList("ART_ELEMENT_SHOWS", "CHILD_SHOWS", "ATTR_SHOWS", "TWO_CLASSES_SHOWS", "FIRST_CHILD_SHOWS", "SECOND",
				"MEDIA_SCREEN_SHOWS", "MEDIA_PRINT_SHOWS", "LATER_SHOWS", "SPECIFIC_SHOWS", "INLINE_SHOWS", "ZZ_SHOWS"), shown(pkg));
		assertEquals(Arrays.asList("ART_ELEMENT_SHOWS", "{H}{H}ID_HIDDEN{H}", "{H}{H}DESC_HIDDEN{H}", "CHILD_SHOWS", "ATTR_SHOWS",
				"TWO_CLASSES_SHOWS", "FIRST_CHILD_SHOWS", "SECOND", "MEDIA_SCREEN_SHOWS", "MEDIA_PRINT_SHOWS", "LATER_SHOWS",
				"SPECIFIC_SHOWS", "{H}{H}IMPORTANT_HIDDEN{H}", "INLINE_SHOWS", "{H}{H}BODY_STYLE_HIDDEN{H}", "ZZ_SHOWS"), all(pkg));
	}

	/**
	 * css-2 and css-5: a hidden row and cell keep their places, a hidden list item stays, and an
	 * explicit display on a descendant shows its content (D5).
	 */
	@Test
	public void wordsStructure() throws Exception {
		reader();
		WordprocessingMLPackage pkg = chunk(AltChunkType.Html, "<html><head><style>.n{display:none}</style></head><body><table>"
				+ "<tr><td>R1A</td><td>R1B</td></tr><tr class='n'><td>ROW_HIDDEN_A</td><td>ROW_HIDDEN_B</td></tr>"
				+ "<tr><td class='n'>CELL_HIDDEN</td><td>R3B</td></tr></table>"
				+ "<ol><li>ONE</li><li class='n'>TWO_HIDDEN</li><li>THREE</li></ol>"
				+ "<div class='n'><p>PLAIN_HIDDEN</p><p style='display:block'>BLOCK_SHOWS</p>"
				+ "<span style='display:inline'>INLINE_SHOWS</span><span style='display:inherit'>INHERIT_HIDDEN</span></div></body></html>");
		List<String> shown = shown(pkg);
		assertEquals(Arrays.asList("R1A|R1B/|/|R3B", "ONE", "THREE", "BLOCK_SHOWS"), shown.subList(0, 4));
		assertTrue(shown.toString(), shown.toString().contains("INLINE_SHOWS"));
		assertFalse(shown.toString(), shown.toString().contains("INHERIT_HIDDEN"));
		List<String> all = all(pkg);
		assertEquals("R1A|R1B/ROW_HIDDEN_A|ROW_HIDDEN_B/CELL_HIDDEN|R3B", all.get(0));
		assertTrue(all.toString(), all.containsAll(Arrays.asList("ONE", "{H}{H}TWO_HIDDEN{H}", "THREE", "{H}{H}PLAIN_HIDDEN{H}", "BLOCK_SHOWS")));
		assertTrue(all.toString(), all.toString().contains("INHERIT_HIDDEN{H}"));
	}

	/** host-1, host-2: where the host defines the style a type or class rule goes through, the rule's display:none is lost. */
	@Test
	public void theHostsStylesWin() throws Exception {
		reader();
		WordprocessingMLPackage pkg = chunk(AltChunkType.Html, "<html><head><style>.hid{display:none} .other{display:none}</style>"
				+ "</head><body><p class='hid'>HOST_STYLE_SHOWS</p><p class='other'>OTHER_HIDDEN</p></body></html>");
		Style hid = new Style();
		hid.setStyleId("Hid");
		hid.setType("paragraph");
		Style.Name name = new Style.Name();
		name.setVal("hid");
		hid.setName(name);
		pkg.getMainDocumentPart().getStyleDefinitionsPart(true).getJaxbElement().getStyle().add(hid);
		assertEquals(Arrays.asList("HOST_STYLE_SHOWS"), shown(pkg));
		assertEquals(Arrays.asList("HOST_STYLE_SHOWS", "{H}{H}OTHER_HIDDEN{H}"), all(pkg));
	}

	/** A chunk's encoding by its meta, or by its bytes where a declaration names one, read as the inspector reads it. */
	@Test
	public void encodingByMetaOrDeclaration() throws Exception {
		reader();
		Charset cp1252 = Charset.forName("windows-1252");
		assertEquals(Arrays.asList("café “quoted”"), shown(chunk(AltChunkType.Html,
				"<html><head><meta http-equiv='Content-Type' content='text/html; charset=ISO-8859-1'></head><body><p>café “quoted”</p></body></html>"
						.getBytes(cp1252))));
		// a loaded chunk keeps its declaration (D1), which names nothing (WD 0.27): the bytes, not UTF-8, are windows-1252
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		AlternativeFormatInputPart part = new AlternativeFormatInputPart(AltChunkType.Xhtml);
		part.setBinaryData(("<?xml version='1.0' encoding='windows-1252'?><html xmlns='http://www.w3.org/1999/xhtml'><body><p>café</p></body></html>")
				.getBytes(cp1252));
		org.docx4j.relationships.Relationship rel = pkg.getMainDocumentPart().addTargetPart(part);
		org.docx4j.wml.CTAltChunk ac = new org.docx4j.wml.CTAltChunk();
		ac.setId(rel.getId());
		pkg.getMainDocumentPart().getContent().add(ac);
		assertEquals(Arrays.asList("café"), shown(pkg));
	}

	/** An MHT chunk whose HTML is not well-formed: read as HTML, which the importer's XML parser could not. */
	@Test
	public void mhtNotWellFormed() throws Exception {
		reader();
		WordprocessingMLPackage pkg = chunk(AltChunkType.Mht, "MIME-Version: 1.0\r\nContent-Type: multipart/related; boundary=\"B\"\r\n\r\n"
				+ "--B\r\nContent-Type: text/html; charset=\"windows-1252\"\r\nContent-Transfer-Encoding: quoted-printable\r\n\r\n"
				+ "<html><body><p>MHT_<b>BOLD<i>BOTH</b></i> <p class=3Dx>MHT_UNCLOSED caf=E9<br><p style=3D'display:none'>MHT_NONE</body></html>\r\n--B--\r\n");
		// the br is a line break at the paragraph's end
		assertEquals(Arrays.asList("MHT_BOLDBOTH ", "MHT_UNCLOSED caf\u00e9\n"), shown(pkg));
		assertTrue(all(pkg).toString(), all(pkg).contains("{H}{H}MHT_NONE{H}"));
	}

	/** D8: {@code convertAltChunks} converts an HTML chunk, what display:none hides kept as hidden text. */
	@Test
	public void convertAltChunksKeepsHiddenText() throws Exception {
		reader();
		WordprocessingMLPackage pkg = chunk(AltChunkType.Html, "<html><head><style>.h{display:none}</style></head><body>"
				+ "<p>CONVERTED_VISIBLE</p><p class='h'>CONVERTED_HIDDEN</p></body></html>");
		pkg.getMainDocumentPart().addAltChunk(AltChunkType.TextPlain, "PLAIN_AFTER".getBytes(StandardCharsets.UTF_8));
		pkg.getMainDocumentPart().convertAltChunks();
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement());
		assertFalse(xml, xml.contains("altChunk"));
		assertTrue(xml, xml.contains("CONVERTED_VISIBLE") && xml.contains("CONVERTED_HIDDEN") && xml.contains("PLAIN_AFTER"));
		assertTrue(xml, xml.contains("vanish"));
		assertEquals(Arrays.asList("CONVERTED_VISIBLE", "PLAIN_AFTER"), shown(pkg));
		assertEquals(Arrays.asList("CONVERTED_VISIBLE", "{H}{H}CONVERTED_HIDDEN{H}", "PLAIN_AFTER"), all(pkg));
	}

	/**
	 * Section 1a's chunk through the new path: list levels, formats, {@code start}, {@code type} and
	 * {@code value} (docx4j-ImportXHTML 17.3.4), inline formatting, the caption as its own paragraph
	 * before an ordinary table, a row span's continuation cell. The labels stay right after an
	 * {@code li value}, whose {@code w:num} the importer adds after docx4j has built the numbering maps.
	 */
	@Test
	public void section1aStructure() throws Exception {
		reader();
		WordprocessingMLPackage pkg = chunk(AltChunkType.Html, "<html><body><h2>HEADING_TWO</h2>"
				+ "<ol><li>ONE<ol type='a'><li>NESTED_A</li><li>NESTED_B</li></ol></li><li value='7'>SEVEN</li><li>EIGHT</li></ol>"
				+ "<ol start='4' style='list-style-type: lower-roman'><li>ROMAN_FOUR</li><li>ROMAN_FIVE</li></ol>"
				+ "<ul><li>BULLET<ul><li>BULLET_NESTED</li></ul></li></ul>"
				+ "<p><b>BOLD</b> <em>EMPH</em> <a href='http://example.invalid/'>LINK</a> <code>CODE</code></p>"
				+ "<blockquote>QUOTED</blockquote><hr/>"
				+ "<table><caption>CAPTION_TEXT</caption><thead><tr><th>H1</th><th>H2</th></tr></thead>"
				+ "<tr><td rowspan='2'>SPAN</td><td>B1</td></tr><tr><td>B2</td></tr><tr><td colspan='2'>WIDE</td></tr></table>"
				+ "</body></html>");
		List<String> labelled = new ArrayList<String>();
		TextBlock table = null;
		for (TextBlock b : Texts.of(pkg, new TextOptions().listLabels(true)).blocks()) {
			if (b.getKind() == TextBlock.Kind.TABLE) table = b;
			else if (!b.getText().isEmpty()) labelled.add((b.getListLabel() == null ? "" : b.getListLabel().trim() + " ") + b.getText());
		}
		assertEquals(Arrays.asList("HEADING_TWO", "1. ONE", "a. NESTED_A", "b. NESTED_B", "7. SEVEN", "8. EIGHT", "iv. ROMAN_FOUR",
				"v. ROMAN_FIVE"), labelled.subList(0, 8));
		assertTrue(labelled.get(8), labelled.get(8).endsWith(" BULLET") && labelled.get(9).endsWith(" BULLET_NESTED"));
		assertEquals(Arrays.asList("BOLD EMPH LINK CODE", "QUOTED", "CAPTION_TEXT"), labelled.subList(10, 13));
		assertNotNull(table);
		assertEquals("H1\tH2\nSPAN\tB1\n\tB2\nWIDE", table.getText());
		assertEquals(2, table.getRows().get(2).getCells().size());
	}
}
