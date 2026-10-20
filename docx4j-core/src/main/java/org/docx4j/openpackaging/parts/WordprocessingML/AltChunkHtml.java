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

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An HTML or XHTML {@code altChunk} as Word 365 opens it (CR-053 D1, D2). Word refuses a chunk
 * that begins with an XML declaration, whatever its content type, and repairs the document; a
 * declaration after a byte order mark or after white space is refused too. Word decodes a chunk by
 * its BOM, then a {@code meta} charset, then as UTF-8 where the bytes are valid UTF-8, else as
 * windows-1252 (CR-053 section 10.6). So the declaration goes, with the white space around it,
 * and a BOM keeps the encoding the declaration stated:
 * <ul>
 * <li>a chunk that begins with a BOM keeps it, and its bytes; UTF-16 is cut in UTF-16;</li>
 * <li>a UTF-16 chunk without a BOM gains one;</li>
 * <li>a chunk in UTF-8, declared or by the XML default, gains a UTF-8 BOM where its bytes are
 *     valid UTF-8;</li>
 * <li>a chunk declared in another encoding is transcoded to UTF-8 and gains a UTF-8 BOM, and a
 *     {@code meta} charset (or {@code http-equiv} content type) naming another encoding is
 *     rewritten to name UTF-8. ISO-8859-1 and US-ASCII are read as windows-1252, as an HTML
 *     reader reads them.</li>
 * </ul>
 * A chunk without a declaration is left as it is. Where the declared encoding is one Java does not
 * know, or the bytes are not in it, only the declaration is cut.
 * @since 17.3.4
 */
public final class AltChunkHtml {

	private static final Logger log = LoggerFactory.getLogger(AltChunkHtml.class);

	private static final byte[] UTF8_BOM = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

	private static final Pattern ENCODING = Pattern.compile("\\bencoding\\s*=\\s*([\"'])([^\"']*)\\1");

	private static final Pattern META = Pattern.compile("<meta\\b[^>]*>", Pattern.CASE_INSENSITIVE);

	private static final Pattern ATTRIBUTE = Pattern.compile(
			"([^\\s\"'>/=]+)(?:\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s\"'>]+)))?");

	private static final Pattern CHARSET_PARAMETER = Pattern.compile("charset\\s*=\\s*[\"']?([^\\s;\"']+)",
			Pattern.CASE_INSENSITIVE);

	/** The labels WHATWG's Encoding standard reads as windows-1252. */
	private static final Set<String> WINDOWS_1252 = new HashSet<String>(Arrays.asList("ansi_x3.4-1968", "ascii",
			"cp1252", "cp819", "csisolatin1", "ibm819", "iso-8859-1", "iso-ir-100", "iso8859-1", "iso88591",
			"iso_8859-1", "iso_8859-1:1987", "l1", "latin1", "us-ascii", "windows-1252", "x-cp1252"));

	private AltChunkHtml() {}

	/** Whether a chunk of this type is written without its XML declaration: {@code Html} and {@code Xhtml} (D1 (a)). */
	public static boolean appliesTo(AltChunkType type) {
		return type == AltChunkType.Html || type == AltChunkType.Xhtml;
	}

	/**
	 * A chunk's bytes without a leading XML declaration, the encoding it named kept as the class
	 * comment says; the same array where there is none.
	 */
	public static byte[] withoutXmlDeclaration(byte[] bytes) {
		if (bytes == null) return null;
		Charset unit; // the code unit before the declaration ends; null: one byte, ASCII as ASCII
		int bom;
		if (startsWith(bytes, 0xEF, 0xBB, 0xBF)) {
			unit = null;
			bom = 3;
		} else if (startsWith(bytes, 0xFF, 0xFE)) {
			unit = UTF_16LE;
			bom = 2;
		} else if (startsWith(bytes, 0xFE, 0xFF)) {
			unit = UTF_16BE;
			bom = 2;
		} else if (startsWith(bytes, '<', 0, '?', 0)) {
			unit = UTF_16LE;
			bom = 0;
		} else if (startsWith(bytes, 0, '<', 0, '?')) {
			unit = UTF_16BE;
			bom = 0;
		} else {
			unit = null;
			bom = 0;
		}
		int width = unit == null ? 1 : 2;
		int start = skipSpace(bytes, bom, unit, width);
		int end = declarationEnd(bytes, start, unit, width);
		if (end < 0) return bytes;
		String declaration = new String(bytes, start, end - start, unit == null ? ISO_8859_1 : unit);
		byte[] rest = Arrays.copyOfRange(bytes, skipSpace(bytes, end, unit, width), bytes.length);
		if (unit == UTF_16LE) return concat(new byte[] { (byte) 0xFF, (byte) 0xFE }, rest);
		if (unit == UTF_16BE) return concat(new byte[] { (byte) 0xFE, (byte) 0xFF }, rest);
		// a UTF-8 BOM decides the encoding, for Word (enc-7) as for an XML reader
		if (bom == 3) return concat(UTF8_BOM, rest);
		Matcher named = ENCODING.matcher(declaration);
		String name = named.find() ? named.group(2).trim() : null;
		Charset declared = name == null ? UTF_8 : charset(name);
		if (declared == null) {
			log.warn("An HTML altChunk's XML declaration names " + name
					+ ", which isn't an encoding these bytes can be in; the declaration is cut, the bytes left as they are");
			return rest;
		}
		if (declared.equals(UTF_8)) {
			if (decode(rest, UTF_8) != null) return concat(UTF8_BOM, rest);
			log.warn("An HTML altChunk declared UTF-8 isn't; the declaration is cut, the bytes left as they are");
			return rest;
		}
		String text = decode(rest, declared);
		if (text == null) {
			log.warn("An HTML altChunk declared " + name + " isn't; the declaration is cut, the bytes left as they are");
			return rest;
		}
		return concat(UTF8_BOM, metaUtf8(text).getBytes(UTF_8));
	}

	/**
	 * Markup docx4j encodes itself, such as the OpenDoPE XHTML binding's bound value, as Word opens
	 * it: without a leading XML declaration and the white space around it, a {@code meta} charset
	 * naming another encoding rewritten to name UTF-8, in UTF-8 with a BOM.
	 */
	public static byte[] utf8WithoutXmlDeclaration(String markup) {
		String s = markup.startsWith("\uFEFF") ? markup.substring(1) : markup;
		int start = 0;
		while (start < s.length() && space(s.charAt(start))) start++;
		if (s.startsWith("<?xml", start) && start + 5 < s.length()
				&& (space(s.charAt(start + 5)) || s.charAt(start + 5) == '?')) {
			int close = s.indexOf("?>", start + 5);
			if (close >= 0) {
				int rest = close + 2;
				while (rest < s.length() && space(s.charAt(rest))) rest++;
				s = s.substring(rest);
			}
		}
		return concat(UTF8_BOM, metaUtf8(s).getBytes(UTF_8));
	}

	/** The markup with each {@code meta} charset, and {@code http-equiv} content type's charset, naming UTF-8. */
	static String metaUtf8(String text) {
		Matcher meta = META.matcher(text);
		StringBuilder out = null;
		int copied = 0;
		while (meta.find()) {
			String tag = meta.group();
			String fixed = metaTagUtf8(tag);
			if (fixed.equals(tag)) continue;
			if (out == null) out = new StringBuilder(text.length());
			out.append(text, copied, meta.start()).append(fixed);
			copied = meta.end();
		}
		return out == null ? text : out.append(text, copied, text.length()).toString();
	}

	private static String metaTagUtf8(String tag) {
		Matcher attribute = ATTRIBUTE.matcher(tag);
		attribute.region("<meta".length(), tag.length() - 1);
		int charsetStart = -1, charsetEnd = -1, contentStart = -1, contentEnd = -1;
		boolean contentType = false;
		while (attribute.find()) {
			int value = attribute.group(2) != null ? 2 : attribute.group(3) != null ? 3 : attribute.group(4) != null ? 4 : 0;
			if (value == 0) continue;
			String name = attribute.group(1).toLowerCase(Locale.ROOT);
			if (name.equals("charset")) {
				charsetStart = attribute.start(value);
				charsetEnd = attribute.end(value);
			} else if (name.equals("http-equiv")) {
				contentType = attribute.group(value).trim().equalsIgnoreCase("content-type");
			} else if (name.equals("content")) {
				contentStart = attribute.start(value);
				contentEnd = attribute.end(value);
			}
		}
		if (charsetStart >= 0) {
			return utf8(tag.substring(charsetStart, charsetEnd).trim()) ? tag
					: tag.substring(0, charsetStart) + "UTF-8" + tag.substring(charsetEnd);
		}
		if (contentType && contentStart >= 0) {
			Matcher charset = CHARSET_PARAMETER.matcher(tag);
			charset.region(contentStart, contentEnd);
			if (charset.find() && !utf8(charset.group(1))) {
				return tag.substring(0, charset.start(1)) + "UTF-8" + tag.substring(charset.end(1));
			}
		}
		return tag;
	}

	private static boolean utf8(String label) {
		String l = label.toLowerCase(Locale.ROOT);
		return l.equals("utf-8") || l.equals("utf8") || l.equals("unicode-1-1-utf-8");
	}

	/** The encoding a declaration names, where Java has it and it writes ASCII as ASCII; else null. */
	private static Charset charset(String name) {
		if (WINDOWS_1252.contains(name.toLowerCase(Locale.ROOT))) return Charset.forName("windows-1252");
		Charset charset;
		try {
			charset = Charset.forName(name);
		} catch (IllegalArgumentException e) {
			return null;
		}
		if (!charset.canEncode()) return null;
		return Arrays.equals("<?xml".getBytes(charset), "<?xml".getBytes(US_ASCII)) ? charset : null;
	}

	/** The bytes as text in this encoding; null where they aren't in it (windows-1252's five unused bytes aside). */
	private static String decode(byte[] bytes, Charset charset) {
		CodingErrorAction onError = charset.name().equals("windows-1252") ? CodingErrorAction.REPLACE : CodingErrorAction.REPORT;
		try {
			return charset.newDecoder().onMalformedInput(onError).onUnmappableCharacter(onError)
					.decode(ByteBuffer.wrap(bytes)).toString();
		} catch (CharacterCodingException e) {
			return null;
		}
	}

	/** Where the declaration that begins at i ends, after its {@code ?>}; -1 where none begins there. */
	private static int declarationEnd(byte[] b, int i, Charset unit, int width) {
		String target = "<?xml";
		for (int k = 0; k < target.length(); k++) {
			if (codeUnit(b, i + k * width, unit) != target.charAt(k)) return -1;
		}
		int next = codeUnit(b, i + target.length() * width, unit);
		// <?xml-stylesheet ...?> is a processing instruction, not a declaration
		if (!(next >= 0 && space((char) next) || next == '?')) return -1;
		for (int j = i + target.length() * width; codeUnit(b, j + width, unit) >= 0; j += width) {
			if (codeUnit(b, j, unit) == '?' && codeUnit(b, j + width, unit) == '>') return j + 2 * width;
		}
		return -1;
	}

	private static int skipSpace(byte[] b, int i, Charset unit, int width) {
		while (true) {
			int c = codeUnit(b, i, unit);
			if (c < 0 || !space((char) c)) return i;
			i += width;
		}
	}

	/** The code unit at byte offset i; -1 past the end. */
	private static int codeUnit(byte[] b, int i, Charset unit) {
		if (unit == UTF_16LE) return i + 1 < b.length ? (b[i] & 0xff) | (b[i + 1] & 0xff) << 8 : -1;
		if (unit == UTF_16BE) return i + 1 < b.length ? (b[i] & 0xff) << 8 | (b[i + 1] & 0xff) : -1;
		return i < b.length ? b[i] & 0xff : -1;
	}

	/** XML's white space. */
	private static boolean space(char c) {
		return c == ' ' || c == '\t' || c == '\r' || c == '\n';
	}

	private static boolean startsWith(byte[] b, int... prefix) {
		if (b.length < prefix.length) return false;
		for (int i = 0; i < prefix.length; i++) {
			if ((b[i] & 0xff) != prefix[i]) return false;
		}
		return true;
	}

	private static byte[] concat(byte[] a, byte[] b) {
		byte[] out = Arrays.copyOf(a, a.length + b.length);
		System.arraycopy(b, 0, out, a.length, b.length);
		return out;
	}
}
