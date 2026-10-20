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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The payloads the inspector decodes itself, never converting them (CR-050 section 2.1's third pass):
 * an HTML or XHTML chunk's markup scanned for scripts, event handlers and external references
 * (jsoup's parser where it is on the class path, else a tag scanner); an MHT chunk's HTML parts; and a
 * zip payload's entries decompressed once, counted against the budgets, before docx4j loads it.
 */
final class InspectPayloads {

	private InspectPayloads() {}

	/** What a chunk's markup holds. */
	static final class Scan {
		boolean script;
		final List<String> refs = new ArrayList<String>();
	}

	/** Whether jsoup is on the class path. */
	static boolean jsoup() {
		return JsoupHtml.present();
	}

	/** A chunk's markup: a {@code <script>} or an {@code on*} handler; each external {@code src}, {@code href} or {@code background}. */
	static Scan html(String html) {
		if (jsoup()) {
			try {
				return JsoupScan.scan(html);
			} catch (RuntimeException | LinkageError e) {
				// jsoup present but unusable: the scanner
			}
		}
		return scanTags(html);
	}

	/** A reference a viewer would fetch: not empty, a fragment, or a {@code data:}, {@code cid:}, {@code mailto:} or {@code javascript:} URI. */
	static boolean external(String value) {
		String v = value == null ? "" : value.trim();
		if (v.isEmpty() || v.startsWith("#")) return false;
		String lower = v.toLowerCase(Locale.ROOT);
		return !(lower.startsWith("data:") || lower.startsWith("cid:") || lower.startsWith("mailto:") || lower.startsWith("javascript:"));
	}

	private static void attribute(Scan s, String name, String value) {
		String n = name.toLowerCase(Locale.ROOT);
		if (n.length() > 2 && n.startsWith("on")) s.script = true;
		else if (n.equals("src") || n.equals("href") || n.equals("background")) {
			if (javascript(value)) s.script = true; // a handler, as a script is
			else if (external(value)) s.refs.add(value.trim());
		}
	}

	/** A {@code javascript:} URI, as a browser reads one: tabs, line feeds and leading controls and spaces ignored. */
	static boolean javascript(String value) {
		if (value == null) return false;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < value.length() && sb.length() < 11; i++) {
			char c = value.charAt(i);
			if (c == '\t' || c == '\n' || c == '\r' || c <= ' ' && sb.length() == 0) continue;
			sb.append(c);
		}
		return sb.toString().toLowerCase(Locale.ROOT).equals("javascript:");
	}

	/** An HTML or XHTML chunk's characters, as the text model reads them ({@link HtmlChunks#decode}). */
	static String decode(byte[] data) {
		return HtmlChunks.decode(data);
	}

	/** jsoup's reading, kept apart so that the class loads only where jsoup is present. */
	private static final class JsoupScan {
		static Scan scan(String html) {
			Scan s = new Scan();
			org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(html);
			for (org.jsoup.nodes.Element e : doc.getAllElements()) {
				if (e.normalName().equals("script")) s.script = true;
				for (org.jsoup.nodes.Attribute a : e.attributes()) attribute(s, a.getKey(), a.getValue());
			}
			return s;
		}
	}

	/** A tag scanner: start tags and their attributes (quoted either way or bare); comments and script bodies skipped. */
	static Scan scanTags(String html) {
		Scan s = new Scan();
		int n = html.length(), i = 0;
		while ((i = html.indexOf('<', i)) >= 0) {
			if (html.startsWith("<!--", i)) {
				int end = html.indexOf("-->", i + 4);
				i = end < 0 ? n : end + 3;
				continue;
			}
			int j = i + 1;
			if (j >= n || !Character.isLetter(html.charAt(j))) {
				i = j;
				continue;
			}
			while (j < n && (Character.isLetterOrDigit(html.charAt(j)) || html.charAt(j) == ':' || html.charAt(j) == '-')) j++;
			String tag = html.substring(i + 1, j).toLowerCase(Locale.ROOT);
			// the attributes, to the tag's end
			while (j < n && html.charAt(j) != '>') {
				char c = html.charAt(j);
				if (Character.isWhitespace(c) || c == '/') { j++; continue; }
				int a = j;
				while (j < n && !Character.isWhitespace(html.charAt(j)) && "/>=".indexOf(html.charAt(j)) < 0) j++;
				String name = html.substring(a, j);
				while (j < n && Character.isWhitespace(html.charAt(j))) j++;
				String value = "";
				if (j < n && html.charAt(j) == '=') {
					j++;
					while (j < n && Character.isWhitespace(html.charAt(j))) j++;
					if (j < n && (html.charAt(j) == '"' || html.charAt(j) == '\'')) {
						char q = html.charAt(j);
						int end = html.indexOf(q, j + 1);
						value = html.substring(j + 1, end < 0 ? n : end);
						j = end < 0 ? n : end + 1;
					} else {
						int v = j;
						while (j < n && !Character.isWhitespace(html.charAt(j)) && html.charAt(j) != '>') j++;
						value = html.substring(v, j);
					}
				}
				if (!name.isEmpty()) attribute(s, name, value);
			}
			i = j;
			if (tag.equals("script")) {
				s.script = true;
				int end = indexOfIgnoreCase(html, "</script", i);
				i = end < 0 ? n : end;
			}
		}
		return s;
	}

	private static int indexOfIgnoreCase(String s, String what, int from) {
		for (int i = s.indexOf('<', Math.max(from, 0)); i >= 0; i = s.indexOf('<', i + 1)) {
			if (s.regionMatches(true, i, what, 0, what.length())) return i;
		}
		return -1;
	}

	// --- MHT ----------------------------------------------------------------------------------------

	/** The HTML and XHTML parts of a MIME message (an MHT chunk), decoded, at any depth of multipart nesting. */
	static List<String> mhtHtml(byte[] data) {
		List<String> out = new ArrayList<String>();
		mime(data, out, 0);
		return out;
	}

	private static final Pattern PARAM = Pattern.compile(";\\s*([A-Za-z0-9_-]+)\\s*=\\s*(\"([^\"]*)\"|[^;\\s]+)");

	private static void mime(byte[] data, List<String> out, int depth) {
		if (depth > 8) return;
		int split = headerEnd(data);
		Map<String, String> headers = headers(new String(data, 0, split < 0 ? data.length : split, StandardCharsets.ISO_8859_1));
		byte[] body = split < 0 ? new byte[0] : java.util.Arrays.copyOfRange(data, bodyStart(data, split), data.length);
		String ctype = headers.getOrDefault("content-type", "text/plain");
		String type = ctype.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
		Map<String, String> params = new LinkedHashMap<String, String>();
		Matcher m = PARAM.matcher(ctype);
		while (m.find()) params.put(m.group(1).toLowerCase(Locale.ROOT), m.group(3) != null ? m.group(3) : m.group(2));
		if (type.startsWith("multipart/") && params.containsKey("boundary")) {
			for (byte[] part : parts(body, params.get("boundary"))) mime(part, out, depth + 1);
			return;
		}
		if (!type.equals("text/html") && !type.equals("application/xhtml+xml")) return;
		String encoding = headers.getOrDefault("content-transfer-encoding", "").trim().toLowerCase(Locale.ROOT);
		byte[] decoded = body;
		if (encoding.equals("base64")) {
			try {
				decoded = Base64.getMimeDecoder().decode(body);
			} catch (IllegalArgumentException e) {
				decoded = new byte[0];
			}
		} else if (encoding.equals("quoted-printable")) {
			decoded = quotedPrintable(body);
		}
		// an unknown charset: as the part's own bytes say
		Charset cs = HtmlChunks.charset(params.get("charset"));
		out.add(cs != null ? new String(decoded, cs) : decode(decoded));
	}

	/** Where a header block ends: the index of its blank line's first line break, or -1. */
	private static int headerEnd(byte[] d) {
		for (int i = 0; i + 1 < d.length; i++) {
			if (d[i] == '\n' && d[i + 1] == '\n') return i;
			if (d[i] == '\n' && d[i + 1] == '\r' && i + 2 < d.length && d[i + 2] == '\n') return i;
		}
		return -1;
	}

	private static int bodyStart(byte[] d, int end) {
		int i = end + 1;
		if (i < d.length && d[i] == '\r') i++;
		if (i < d.length && d[i] == '\n') i++;
		return i;
	}

	/** Header names lower-cased, folded lines unfolded. */
	private static Map<String, String> headers(String block) {
		Map<String, String> out = new LinkedHashMap<String, String>();
		String last = null;
		for (String line : block.split("\r?\n")) {
			if (!line.isEmpty() && (line.charAt(0) == ' ' || line.charAt(0) == '\t') && last != null) {
				out.put(last, out.get(last) + " " + line.trim());
				continue;
			}
			int colon = line.indexOf(':');
			if (colon <= 0) continue;
			last = line.substring(0, colon).trim().toLowerCase(Locale.ROOT);
			out.putIfAbsent(last, line.substring(colon + 1).trim());
		}
		return out;
	}

	/** A multipart body's parts, between its {@code --boundary} lines (the preamble and epilogue left out). */
	private static List<byte[]> parts(byte[] body, String boundary) {
		List<byte[]> out = new ArrayList<byte[]>();
		String delimiter = "--" + boundary;
		StringBuilder part = null;
		for (String line : new String(body, StandardCharsets.ISO_8859_1).split("\n", -1)) {
			String bare = line.endsWith("\r") ? line.substring(0, line.length() - 1) : line;
			if (bare.equals(delimiter) || bare.equals(delimiter + "--")) {
				if (part != null) {
					// the line break before a delimiter belongs to it
					int len = part.length();
					if (len > 0 && part.charAt(len - 1) == '\n') part.setLength(--len);
					if (len > 0 && part.charAt(len - 1) == '\r') part.setLength(--len);
					out.add(part.toString().getBytes(StandardCharsets.ISO_8859_1));
				}
				if (bare.equals(delimiter + "--")) return out;
				part = new StringBuilder();
				continue;
			}
			if (part != null) part.append(line).append('\n');
		}
		return out;
	}

	private static byte[] quotedPrintable(byte[] b) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int i = 0; i < b.length; i++) {
			if (b[i] == '=') {
				if (i + 1 < b.length && (b[i + 1] == '\n' || b[i + 1] == '\r')) {
					i++;
					if (b[i] == '\r' && i + 1 < b.length && b[i + 1] == '\n') i++;
					continue;
				}
				if (i + 2 < b.length) {
					int h = Character.digit(b[i + 1], 16), l = Character.digit(b[i + 2], 16);
					if (h >= 0 && l >= 0) {
						out.write(h * 16 + l);
						i += 2;
						continue;
					}
				}
			}
			out.write(b[i]);
		}
		return out.toByteArray();
	}

	// --- a zip payload -------------------------------------------------------------------------------

	/** The entries a zip payload may hold at most. */
	private static final int MAX_ENTRIES = 100000;

	/**
	 * Every entry of a zip payload decompressed once and counted, so that a payload over its budget is
	 * stopped as it is decoded, before docx4j loads it.
	 */
	static void zip(byte[] data, InspectRun.Payload payload) throws IOException, InspectRun.PayloadLimit {
		byte[] buffer = new byte[8192];
		int entries = 0;
		try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(data))) {
			ZipEntry e;
			while ((e = zin.getNextEntry()) != null) {
				if (++entries > MAX_ENTRIES) throw new IOException("more than " + MAX_ENTRIES + " entries");
				int n;
				while ((n = zin.read(buffer)) > 0) payload.add(n);
			}
		}
		if (entries == 0) throw new IOException("no zip entries");
	}
}
