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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An HTML {@code altChunk}'s text (WD REQ-513): tidied by jsoup where it is on the class path (an
 * optional dependency of docx4j-core, CR-046 D5) and read as blocks, paragraphs and tables; else its
 * text with the tags stripped, a block element's end a line's.
 */
final class HtmlText {

	private static final Logger log = LoggerFactory.getLogger(HtmlText.class);

	private HtmlText() {}

	private static final Set<String> BLOCKS = new HashSet<String>(Arrays.asList("p", "div", "h1", "h2", "h3", "h4", "h5",
			"h6", "li", "dt", "dd", "pre", "blockquote", "address", "figcaption", "caption", "section", "article", "header",
			"footer", "main", "nav", "aside", "center", "ul", "ol", "dl", "hr", "form", "fieldset", "legend", "body", "html"));

	private static final Set<String> SKIPPED = new HashSet<String>(Arrays.asList("head", "script", "style", "template",
			"noscript", "title", "meta", "link", "object", "embed", "iframe", "svg", "math"));

	private static Boolean jsoup;

	/** Whether jsoup is on the class path. */
	static boolean hasJsoup() {
		if (jsoup == null) {
			try {
				Class.forName("org.jsoup.Jsoup", false, HtmlText.class.getClassLoader());
				jsoup = Boolean.TRUE;
			} catch (ClassNotFoundException | LinkageError e) {
				jsoup = Boolean.FALSE;
			}
		}
		return jsoup;
	}

	/** An HTML document's blocks, under {@code prefix}. */
	static List<TextBlock> blocks(String html, TextBlock.Story story, String prefix) {
		if (hasJsoup()) {
			try {
				org.w3c.dom.Document dom = JsoupHtml.parse(html);
				return blocks(dom, story, prefix);
			} catch (RuntimeException | LinkageError e) {
				log.warn("jsoup couldn't read an HTML chunk (" + e.getMessage() + "); its tags are stripped");
			}
		}
		return stripped(html, story, prefix);
	}

	/** An (X)HTML DOM's blocks: the body's, else the document's. */
	static List<TextBlock> blocks(org.w3c.dom.Document dom, TextBlock.Story story, String prefix) {
		Reader r = new Reader(story, prefix);
		Element root = dom.getDocumentElement();
		Element body = root == null ? null : child(root, "body");
		if (root != null) r.children(body != null ? body : root, r.out);
		r.flush(r.out);
		return r.out;
	}

	private static Element child(Element e, String name) {
		for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element && name.equals(local((Element) n))) return (Element) n;
		}
		return null;
	}

	private static String local(Element e) {
		String n = e.getLocalName() != null ? e.getLocalName() : e.getNodeName();
		int colon = n.indexOf(':');
		return (colon >= 0 ? n.substring(colon + 1) : n).toLowerCase(Locale.ROOT);
	}

	/** The walk: inline text gathered into the paragraph being made, a block element's edges ending it. */
	private static final class Reader {
		final TextBlock.Story story;
		final String prefix;
		final List<TextBlock> out = new ArrayList<TextBlock>();
		StringBuilder line = new StringBuilder();
		boolean pre;

		Reader(TextBlock.Story story, String prefix) {
			this.story = story;
			this.prefix = prefix;
		}

		void children(Node parent, List<TextBlock> sink) {
			for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) node(n, sink);
		}

		void node(Node n, List<TextBlock> sink) {
			if (n.getNodeType() == Node.TEXT_NODE || n.getNodeType() == Node.CDATA_SECTION_NODE) {
				text(n.getNodeValue());
				return;
			}
			if (!(n instanceof Element)) return;
			Element e = (Element) n;
			String name = local(e);
			if (SKIPPED.contains(name)) return;
			if ("br".equals(name)) {
				line.append('\n');
				return;
			}
			if ("table".equals(name)) {
				flush(sink);
				table(e, sink);
				return;
			}
			if (BLOCKS.contains(name)) {
				flush(sink);
				boolean wasPre = pre;
				if ("pre".equals(name)) pre = true;
				children(e, sink);
				pre = wasPre;
				flush(sink);
				return;
			}
			children(e, sink);
		}

		void text(String s) {
			if (s == null || s.isEmpty()) return;
			if (pre) {
				line.append(s);
				return;
			}
			// HTML's own whitespace rule: a run of spaces, tabs and line ends is one space
			String collapsed = s.replaceAll("[ \\t\\r\\n\\f]+", " ");
			if (collapsed.startsWith(" ") && (line.length() == 0 || line.charAt(line.length() - 1) == ' '
					|| line.charAt(line.length() - 1) == '\n')) collapsed = collapsed.substring(1);
			line.append(collapsed);
		}

		void flush(List<TextBlock> sink) {
			String s = line.toString();
			line = new StringBuilder();
			if (!pre) s = s.replaceAll(" +$", "").replaceAll(" *\n *", "\n");
			if (s.isEmpty()) return;
			sink.add(Embedded.paragraph(story, prefix + "/" + count(sink), s));
		}

		private int count(List<TextBlock> sink) {
			return sink.size();
		}

		void table(Element tableElement, List<TextBlock> sink) {
			TextBlock table = new TextBlock(TextBlock.Kind.TABLE, story, prefix + "/" + sink.size());
			List<Element> rows = new ArrayList<Element>();
			rows(tableElement, rows);
			for (int r = 0; r < rows.size(); r++) {
				TextRow row = new TextRow(table.address + "/" + r);
				int c = 0;
				for (Node n = rows.get(r).getFirstChild(); n != null; n = n.getNextSibling()) {
					if (!(n instanceof Element)) continue;
					String name = local((Element) n);
					if (!"td".equals(name) && !"th".equals(name)) continue;
					Element td = (Element) n;
					TextCell cell = new TextCell(row.address + "/" + c++);
					cell.gridSpan = Math.max(1, integer(td.getAttribute("colspan")));
					cell.mergeRestart = integer(td.getAttribute("rowspan")) > 1;
					Reader inner = new Reader(story, cell.address);
					inner.children(td, inner.out);
					inner.flush(inner.out);
					if (inner.out.isEmpty()) inner.out.add(Embedded.paragraph(story, cell.address + "/0", ""));
					cell.blocks.addAll(inner.out);
					row.cells.add(cell);
				}
				if ("thead".equals(local((Element) rows.get(r).getParentNode()))) row.header = true;
				table.rows.add(row);
			}
			if (!table.rows.isEmpty()) sink.add(table);
		}

		private static void rows(Element e, List<Element> rows) {
			for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
				if (!(n instanceof Element)) continue;
				String name = local((Element) n);
				if ("tr".equals(name)) rows.add((Element) n);
				else if ("thead".equals(name) || "tbody".equals(name) || "tfoot".equals(name)) rows((Element) n, rows);
			}
		}

		private static int integer(String s) {
			try {
				return s == null || s.isEmpty() ? 1 : Integer.parseInt(s.trim());
			} catch (NumberFormatException e) {
				return 1;
			}
		}
	}

	// --- without an HTML parser ------------------------------------------------------------------------

	private static final Pattern DROPPED = Pattern.compile("(?is)<!--.*?-->|<(head|script|style|template|noscript|title)\\b.*?</\\1\\s*>");
	private static final Pattern BREAK = Pattern.compile("(?i)<br\\b[^>]*>");
	private static final Pattern BLOCK_EDGE = Pattern.compile("(?i)</?(p|div|h[1-6]|li|dt|dd|pre|blockquote|tr|table|ul|ol|dl|section|article|header|footer|address|hr)\\b[^>]*>");
	private static final Pattern CELL_EDGE = Pattern.compile("(?i)</t[dh]\\s*>");
	private static final Pattern TAG = Pattern.compile("(?s)<[^>]*>");
	private static final Pattern ENTITY = Pattern.compile("&(#[0-9]+|#[xX][0-9a-fA-F]+|amp|lt|gt|quot|apos|nbsp);");

	/** The text with its tags stripped: a line for each block element, a table's cells a tab apart. */
	static List<TextBlock> stripped(String html, TextBlock.Story story, String prefix) {
		String s = DROPPED.matcher(html).replaceAll("");
		s = BREAK.matcher(s).replaceAll("\u0000");
		s = CELL_EDGE.matcher(s).replaceAll("\t");
		s = BLOCK_EDGE.matcher(s).replaceAll("\n");
		s = TAG.matcher(s).replaceAll("");
		s = s.replaceAll("[ \\t\\r\\f]*\\n[ \\t\\r\\f]*", "\n").replaceAll("[ \\r\\f]+", " ");
		List<TextBlock> out = new ArrayList<TextBlock>();
		for (String line : s.split("\n")) {
			String text = entities(line.replace('\u0000', '\n')).trim();
			if (!text.isEmpty()) out.add(Embedded.paragraph(story, prefix + "/" + out.size(), text));
		}
		return out;
	}

	private static String entities(String s) {
		Matcher m = ENTITY.matcher(s);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String e = m.group(1);
			String r;
			switch (e) {
			case "amp": r = "&"; break;
			case "lt": r = "<"; break;
			case "gt": r = ">"; break;
			case "quot": r = "\""; break;
			case "apos": r = "'"; break;
			case "nbsp": r = " "; break;
			default:
				try {
					int cp = e.startsWith("#x") || e.startsWith("#X") ? Integer.parseInt(e.substring(2), 16) : Integer.parseInt(e.substring(1));
					r = Character.isValidCodePoint(cp) ? new String(Character.toChars(cp)) : m.group();
				} catch (NumberFormatException ex) {
					r = m.group();
				}
			}
			m.appendReplacement(sb, Matcher.quoteReplacement(r));
		}
		m.appendTail(sb);
		return sb.toString();
	}
}
