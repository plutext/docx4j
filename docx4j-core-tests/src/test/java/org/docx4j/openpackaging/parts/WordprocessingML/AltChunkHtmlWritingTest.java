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
package org.docx4j.openpackaging.parts.WordprocessingML;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.docx4j.Docx4J;
import org.docx4j.model.text.PlainText;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * CR-053 phase 1: an HTML or XHTML chunk added by any of {@code addAltChunk}'s four paths is
 * written as Word opens it (D1, D2): no XML declaration, even after a byte order mark or white
 * space (Word refuses both, section 10.6's {@code enc-8} and {@code enc-9}), and the encoding the
 * declaration named kept by a BOM. Other types, a caller's own part, and a loaded part are left as
 * they are.
 */
public class AltChunkHtmlWritingTest {

	private static final String BODY = "<html><head><title>T</title></head><body><p>café “quoted”</p></body></html>";

	private static final byte[] UTF8_BOM = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

	private static byte[] bytes(String s, Charset cs) {
		return s.getBytes(cs);
	}

	private static byte[] concat(byte[]... parts) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (byte[] p : parts) out.write(p, 0, p.length);
		return out.toByteArray();
	}

	private static byte[] utf8(String s) {
		return bytes(s, StandardCharsets.UTF_8);
	}

	/** The chunk each path writes, which must be the same for all four. */
	private static byte[] written(AltChunkType type, byte[] bytes) throws Exception {
		byte[] first = null;
		for (int path = 0; path < 4; path++) {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			MainDocumentPart main = pkg.getMainDocumentPart();
			AlternativeFormatInputPart part;
			switch (path) {
			case 0: part = main.addAltChunk(type, bytes, 0); break;
			case 1: part = main.addAltChunk(type, new ByteArrayInputStream(bytes), 0); break;
			case 2: part = main.addAltChunk(type, bytes, main, 0); break;
			default: part = main.addAltChunk(type, new ByteArrayInputStream(bytes), main, 0); break;
			}
			if (first == null) first = part.getBytes();
			else assertArrayEquals(type + ", path " + path, first, part.getBytes());
		}
		return first;
	}

	private static void assertWritten(String message, byte[] expected, byte[] given) throws Exception {
		assertArrayEquals(message + " (Html)", expected, written(AltChunkType.Html, given));
		assertArrayEquals(message + " (Xhtml)", expected, written(AltChunkType.Xhtml, given));
	}

	@Test
	public void utf8DeclarationIsCutAndABomAdded() throws Exception {
		assertWritten("declared", concat(UTF8_BOM, utf8(BODY)), utf8("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + BODY));
		assertWritten("by default", concat(UTF8_BOM, utf8(BODY)), utf8("<?xml version='1.0' standalone='yes'?>" + BODY));
		assertWritten("lower case, no space", concat(UTF8_BOM, utf8(BODY)), utf8("<?xml version='1.0' encoding='utf-8'?>" + BODY));
	}

	/** Word refuses a declaration after a BOM (enc-8) or after white space (enc-9). */
	@Test
	public void declarationAfterABomOrWhiteSpaceIsCut() throws Exception {
		String declaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		assertWritten("after a BOM", concat(UTF8_BOM, utf8(BODY)), concat(UTF8_BOM, utf8(declaration + "\r\n" + BODY)));
		assertWritten("after white space", concat(UTF8_BOM, utf8(BODY)), utf8(" \r\n\t" + declaration + "\n\n" + BODY));
		// a BOM decides the encoding, whatever the declaration says
		assertWritten("a BOM and another encoding declared", concat(UTF8_BOM, utf8(BODY)),
				concat(UTF8_BOM, utf8("<?xml version='1.0' encoding='ISO-8859-1'?>" + BODY)));
	}

	/** UTF-16 is cut in UTF-16 and keeps its BOM (Word reads UTF-16LE with a BOM, enc-10), or gains one. */
	@Test
	public void utf16IsCutInUtf16() throws Exception {
		String declared = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>\r\n" + BODY;
		byte[] le = { (byte) 0xFF, (byte) 0xFE };
		byte[] be = { (byte) 0xFE, (byte) 0xFF };
		assertWritten("LE, BOM", concat(le, bytes(BODY, StandardCharsets.UTF_16LE)), concat(le, bytes(declared, StandardCharsets.UTF_16LE)));
		assertWritten("BE, BOM", concat(be, bytes(BODY, StandardCharsets.UTF_16BE)), concat(be, bytes(declared, StandardCharsets.UTF_16BE)));
		assertWritten("LE, no BOM", concat(le, bytes(BODY, StandardCharsets.UTF_16LE)), bytes(declared, StandardCharsets.UTF_16LE));
		assertWritten("BE, no BOM", concat(be, bytes(BODY, StandardCharsets.UTF_16BE)), bytes(declared, StandardCharsets.UTF_16BE));
	}

	/**
	 * D2 (c): a chunk declared in another encoding is transcoded to UTF-8, gains a UTF-8 BOM (which
	 * Word lets outrank a {@code meta}, enc-7), and each {@code meta} naming another encoding is
	 * rewritten. ISO-8859-1 is read as windows-1252, so its curly quotes survive.
	 */
	@Test
	public void otherEncodingIsTranscodedAndItsMetaRewritten() throws Exception {
		String head = "<html><head><meta charset=\"ISO-8859-1\"/>"
				+ "<META HTTP-EQUIV='Content-Type' CONTENT='text/html; charset=iso-8859-1'>"
				+ "<meta http-equiv=content-type content=\"text/html;charset=latin1\">"
				+ "<meta charset=windows-1252 />"
				+ "<meta name=\"description\" content=\"charset=ISO-8859-1\">"
				+ "<meta charset=\"utf-8\"><title>T</title></head>";
		String body = "<body><p>café “quoted”</p></body></html>";
		String rewritten = "<html><head><meta charset=\"UTF-8\"/>"
				+ "<META HTTP-EQUIV='Content-Type' CONTENT='text/html; charset=UTF-8'>"
				+ "<meta http-equiv=content-type content=\"text/html;charset=UTF-8\">"
				+ "<meta charset=UTF-8 />"
				+ "<meta name=\"description\" content=\"charset=ISO-8859-1\">"
				+ "<meta charset=\"utf-8\"><title>T</title></head>";
		Charset cp1252 = Charset.forName("windows-1252");
		assertWritten("ISO-8859-1", concat(UTF8_BOM, utf8(rewritten + body)),
				bytes("<?xml version='1.0' encoding='ISO-8859-1'?>" + head + body, cp1252));

		String japanese = "<html><head><meta charset='Shift_JIS'></head><body><p>日本語</p></body></html>";
		assertWritten("Shift_JIS", concat(UTF8_BOM, utf8(japanese.replace("Shift_JIS", "UTF-8"))),
				bytes("<?xml version=\"1.0\" encoding=\"Shift_JIS\"?>" + japanese, Charset.forName("Shift_JIS")));
	}

	/** Where docx4j can't tell the encoding, only the declaration is cut. */
	@Test
	public void unknownOrWrongEncodingCutsTheDeclarationOnly() throws Exception {
		byte[] cp1252 = bytes(BODY, Charset.forName("windows-1252"));
		assertWritten("declared UTF-8, isn't", cp1252, concat(utf8("<?xml version='1.0' encoding='UTF-8'?>"), cp1252));
		assertWritten("unknown encoding", cp1252, concat(utf8("<?xml version='1.0' encoding='x-no-such-thing'?>"), cp1252));
		assertWritten("UTF-16 declared in single bytes", utf8(BODY), utf8("<?xml version='1.0' encoding='UTF-16'?>" + BODY));
	}

	/** A chunk without a declaration is written as it is given, a BOM or white space included. */
	@Test
	public void noDeclarationIsWrittenAsGiven() throws Exception {
		byte[][] given = {
				utf8(BODY),
				concat(UTF8_BOM, utf8(BODY)),
				utf8("\n  " + BODY),
				bytes(BODY, Charset.forName("windows-1252")),
				utf8("<?xml-stylesheet href='a.css'?>" + BODY),
				utf8("<!DOCTYPE html>" + BODY),
				new byte[0] };
		for (byte[] g : given) assertWritten(new String(g, StandardCharsets.UTF_8), g, g);
	}

	/** D1 (a): Flat OPC keeps its declaration (Word needs it); MHT and plain text are as given. */
	@Test
	public void otherTypesAreWrittenAsGiven() throws Exception {
		byte[] declared = utf8("<?xml version='1.0' encoding='UTF-8'?>" + BODY);
		for (AltChunkType type : new AltChunkType[] { AltChunkType.Xml, AltChunkType.Mht, AltChunkType.TextPlain, AltChunkType.Rtf }) {
			assertArrayEquals(type.toString(), declared, written(type, declared));
		}
	}

	/** D1 (a): bytes a caller puts into a part themselves are theirs. */
	@Test
	public void aCallersOwnPartIsWrittenAsGiven() throws Exception {
		byte[] declared = utf8("<?xml version='1.0' encoding='UTF-8'?>" + BODY);
		AlternativeFormatInputPart part = new AlternativeFormatInputPart(AltChunkType.Html);
		part.setBinaryData(declared);
		assertArrayEquals(declared, part.getBytes());
	}

	/** The chunk survives a save and a load as written, with its type, and the text model reads it right. */
	@Test
	public void writtenChunkRoundTrips() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		AlternativeFormatInputPart part = pkg.getMainDocumentPart().addAltChunk(AltChunkType.Html,
				bytes("<?xml version='1.0' encoding='ISO-8859-1'?>" + BODY, Charset.forName("windows-1252")));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Docx4J.save(pkg, out);
		WordprocessingMLPackage loaded = WordprocessingMLPackage.load(new ByteArrayInputStream(out.toByteArray()));
		AlternativeFormatInputPart again = (AlternativeFormatInputPart) loaded.getParts().get(part.getPartName());
		assertArrayEquals(concat(UTF8_BOM, utf8(BODY)), again.getBytes());
		assertEquals(AltChunkType.Html, again.getAltChunkType());
		String text = PlainText.of(Texts.of(loaded));
		assertTrue(text, text.contains("café “quoted”"));
	}

	/** The binding's markup: a BOM character, white space and the declaration cut, metas UTF-8, UTF-8 with a BOM. */
	@Test
	public void markupDocx4jEncodes() {
		String meta = "<html><head><meta charset='windows-1252'></head><body><p>café</p></body></html>";
		byte[] expected = concat(UTF8_BOM, utf8(meta.replace("windows-1252", "UTF-8")));
		assertArrayEquals(expected, AltChunkHtml.utf8WithoutXmlDeclaration("<?xml version='1.0' encoding='ISO-8859-1'?>\n" + meta));
		assertArrayEquals(expected, AltChunkHtml.utf8WithoutXmlDeclaration("﻿ \n<?xml version='1.0'?>" + meta));
		assertArrayEquals(expected, AltChunkHtml.utf8WithoutXmlDeclaration(meta));
		String stylesheet = "<?xml-stylesheet href='a.css'?><p>x</p>";
		assertArrayEquals(concat(UTF8_BOM, utf8(stylesheet)), AltChunkHtml.utf8WithoutXmlDeclaration(stylesheet));
	}

	@Test
	public void appliesToHtmlAndXhtmlOnly() {
		for (AltChunkType type : AltChunkType.values()) {
			assertEquals(type.toString(), type == AltChunkType.Html || type == AltChunkType.Xhtml, AltChunkHtml.appliesTo(type));
		}
		assertEquals(Arrays.asList(AltChunkType.Html, AltChunkType.Xhtml, AltChunkType.Mht, AltChunkType.Rtf, AltChunkType.Rtf, null),
				Arrays.asList(AltChunkType.forContentType("text/html"), AltChunkType.forContentType(" Application/XHTML+XML ; charset=utf-8"),
						AltChunkType.forContentType("message/rfc822"), AltChunkType.forContentType("text/rtf"),
						AltChunkType.forContentType("application/rtf"), AltChunkType.forContentType("application/octet-stream")));
	}
}
