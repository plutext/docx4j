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

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.Style;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An HTML, XHTML or MHT {@code altChunk}'s markup made ready for docx4j-ImportXHTML, as Word reads it
 * (CR-053, phase 0's measurements in its sections 10.6 and 10.7): decoded in Word's order, and its tree,
 * once jsoup has parsed it, rewritten so that the importer's CSS engine applies what Word applies, and
 * made safe to lay out (section 4b). Nothing here names jsoup or the importer.
 */
final class HtmlChunks {

	private HtmlChunks() {}

	// --- decoding (the WD's Chunks rule, with Word's last step) ----------------------------------------

	private static final Pattern DECLARED = Pattern.compile(
			"<\\?xml[^>]*?\\sencoding\\s*=\\s*[\"']([A-Za-z0-9._:-]+)|<meta\\b[^>]*?charset\\s*=\\s*[\"']?([A-Za-z0-9._:-]+)",
			Pattern.CASE_INSENSITIVE);

	/** The labels WHATWG's Encoding standard reads as windows-1252, as an HTML reader does. */
	private static final Set<String> WINDOWS_1252 = new HashSet<String>(Arrays.asList("ansi_x3.4-1968", "ascii",
			"cp1252", "cp819", "csisolatin1", "ibm819", "iso-8859-1", "iso-ir-100", "iso8859-1", "iso88591",
			"iso_8859-1", "iso_8859-1:1987", "l1", "latin1", "us-ascii", "windows-1252", "x-cp1252"));

	/**
	 * A chunk's markup in its encoding: by its byte order mark (UTF-8, UTF-16LE or UTF-16BE); else as
	 * UTF-16 where its first two bytes are {@code <} so encoded; else in the encoding an XML
	 * declaration's {@code encoding} or a {@code meta} element's {@code charset} (or {@code http-equiv}
	 * content type's) names within its first 1,024 bytes, a UTF-16 or UTF-32 or unknown name ignored;
	 * else as UTF-8 where the bytes are valid UTF-8, else as windows-1252, as Word reads it (CR-053
	 * section 10.6). Bytes the named encoding cannot decode are read as U+FFFD.
	 */
	static String decode(byte[] data) {
		int n = data.length;
		if (n >= 3 && (data[0] & 0xff) == 0xEF && (data[1] & 0xff) == 0xBB && (data[2] & 0xff) == 0xBF) return new String(data, 3, n - 3, StandardCharsets.UTF_8);
		if (n >= 2 && (data[0] & 0xff) == 0xFF && (data[1] & 0xff) == 0xFE) return new String(data, 2, n - 2, StandardCharsets.UTF_16LE);
		if (n >= 2 && (data[0] & 0xff) == 0xFE && (data[1] & 0xff) == 0xFF) return new String(data, 2, n - 2, StandardCharsets.UTF_16BE);
		if (n >= 2 && data[0] == '<' && data[1] == 0) return new String(data, StandardCharsets.UTF_16LE);
		if (n >= 2 && data[0] == 0 && data[1] == '<') return new String(data, StandardCharsets.UTF_16BE);
		Matcher m = DECLARED.matcher(new String(data, 0, Math.min(n, 1024), StandardCharsets.ISO_8859_1));
		if (m.find()) {
			Charset cs = charset(m.group(1) != null ? m.group(1) : m.group(2));
			if (cs != null) return new String(data, cs);
		}
		try {
			return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(data)).toString();
		} catch (CharacterCodingException e) {
			return new String(data, Charset.forName("windows-1252"));
		}
	}

	/**
	 * The encoding a label names, as an HTML reader takes it (the windows-1252 labels, ISO-8859-1 and
	 * US-ASCII among them, as windows-1252); null for a name Java does not know, or a UTF-16 or UTF-32
	 * one, which bytes read as ASCII cannot be in.
	 */
	static Charset charset(String label) {
		if (label == null) return null;
		String l = label.trim().toLowerCase(Locale.ROOT);
		if (WINDOWS_1252.contains(l)) return Charset.forName("windows-1252");
		try {
			Charset cs = Charset.forName(l);
			return cs.name().startsWith("UTF-16") || cs.name().startsWith("UTF-32") ? null : cs;
		} catch (RuntimeException e) {
			return null;
		}
	}

	// --- the rewrite (D4, section 10.7) ----------------------------------------------------------------

	static final String XHTML_NS = "http://www.w3.org/1999/xhtml";

	/**
	 * HTML 4.01's elements: those Word knows. Another element is transparent in Word (CR-053
	 * section 10.6, {@code css-4}, {@code css-6}): its content is kept in its place, and no rule, no
	 * {@code style} attribute and no {@code id} of its own counts.
	 */
	static final Set<String> HTML4 = new HashSet<String>(Arrays.asList("a", "abbr", "acronym", "address", "applet",
			"area", "b", "base", "basefont", "bdo", "big", "blockquote", "body", "br", "button", "caption", "center",
			"cite", "code", "col", "colgroup", "dd", "del", "dfn", "dir", "div", "dl", "dt", "em", "fieldset", "font",
			"form", "frame", "frameset", "h1", "h2", "h3", "h4", "h5", "h6", "head", "hr", "html", "i", "iframe", "img",
			"input", "ins", "isindex", "kbd", "label", "legend", "li", "link", "map", "menu", "meta", "noframes",
			"noscript", "object", "ol", "optgroup", "option", "p", "param", "pre", "q", "s", "samp", "script", "select",
			"small", "span", "strike", "strong", "style", "sub", "sup", "table", "tbody", "td", "textarea", "tfoot", "th",
			"thead", "title", "tr", "tt", "u", "ul", "var"));

	/**
	 * The Word style an element's type rule goes through (CR-053 section 10.6, {@code css-3},
	 * {@code host-1}): measured for {@code p}, {@code h6} and {@code address}; the rest is Word's
	 * set of HTML styles, by inference.
	 */
	static final Map<String, String> WORD_STYLE = new HashMap<String, String>();
	static {
		WORD_STYLE.put("p", "normal (web)");
		for (int i = 1; i <= 6; i++) WORD_STYLE.put("h" + i, "heading " + i);
		WORD_STYLE.put("address", "html address");
		WORD_STYLE.put("pre", "html preformatted");
		WORD_STYLE.put("code", "html code");
		WORD_STYLE.put("kbd", "html keyboard");
		WORD_STYLE.put("samp", "html sample");
		WORD_STYLE.put("var", "html variable");
		WORD_STYLE.put("cite", "html cite");
		WORD_STYLE.put("dfn", "html definition");
		WORD_STYLE.put("acronym", "html acronym");
		WORD_STYLE.put("tt", "html typewriter");
	}

	/** The names of the styles a package's styles part defines, in lower case: those a chunk's rule cannot change. */
	static Set<String> hostStyles(OpcPackage pkg) {
		Set<String> out = new HashSet<String>();
		if (!(pkg instanceof WordprocessingMLPackage) || ((WordprocessingMLPackage) pkg).getMainDocumentPart() == null) return out;
		StyleDefinitionsPart sdp = ((WordprocessingMLPackage) pkg).getMainDocumentPart().getStyleDefinitionsPart(false);
		if (sdp == null || sdp.getJaxbElement() == null) return out;
		for (Style s : sdp.getJaxbElement().getStyle()) {
			if (s.getName() != null && s.getName().getVal() != null) out.add(s.getName().getVal().toLowerCase(Locale.ROOT));
		}
		return out;
	}

	/**
	 * The parsed chunk rewritten as Word reads it, in place, nodes moved rather than copied (so a path
	 * recorded before the rewrite still names its element):
	 * <ul>
	 * <li>a {@code style} element in the body moves to the head, where the importer reads it (Word
	 *     applies it; {@code css-1});</li>
	 * <li>each {@code style} element keeps only the rules Word applies ({@link #css});</li>
	 * <li>an element outside HTML 4.01 is unwrapped, its content kept in its place;</li>
	 * <li>and, for the layout's sake (section 4b), {@code colspan} and {@code rowspan} are clamped as
	 *     WHATWG clamps them, and each element in the body is given {@code float: none !important}.</li>
	 * </ul>
	 */
	static void rewrite(Document dom, Set<String> hostStyles) {
		Element html = dom.getDocumentElement();
		if (html == null) return;
		Element head = child(html, "head");
		Element body = child(html, "body");
		if (head == null) {
			head = dom.createElementNS(XHTML_NS, "head");
			html.insertBefore(head, html.getFirstChild());
		}
		if (body != null) {
			for (Element style : elements(body, "style")) head.appendChild(style);
		}
		for (Element style : elements(head, "style")) {
			String css = style.getTextContent();
			String kept = css(css, hostStyles);
			if (!kept.equals(css)) style.setTextContent(kept);
		}
		for (Element e : elements(html, null)) {
			if (!HTML4.contains(local(e)) || !isHtml(e)) unwrap(e);
		}
		if (body == null) return;
		for (Element e : elements(body, null)) {
			clamp(e, "colspan", 1, 1000);
			clamp(e, "rowspan", 0, 65534);
			String style = e.getAttribute("style");
			e.setAttribute("style", style.isEmpty() ? "float:none !important" : style + ";float:none !important");
		}
	}

	/** The number of elements in the tree. */
	static int count(Document dom) {
		return dom.getElementsByTagName("*").getLength();
	}

	private static boolean isHtml(Element e) {
		String ns = e.getNamespaceURI();
		return ns == null || ns.equals(XHTML_NS);
	}

	/** The element replaced by its content, which moves into its place. */
	private static void unwrap(Element e) {
		Node parent = e.getParentNode();
		if (parent == null) return;
		while (e.getFirstChild() != null) parent.insertBefore(e.getFirstChild(), e);
		parent.removeChild(e);
	}

	/**
	 * A span attribute as WHATWG's rules for parsing non-negative integers read it, clamped; removed
	 * where it is not a number, as a browser then takes the default.
	 */
	private static void clamp(Element e, String name, int min, int max) {
		if (!e.hasAttribute(name)) return;
		String v = e.getAttribute(name).trim();
		int i = 0;
		while (i < v.length() && Character.isDigit(v.charAt(i))) i++;
		if (i == 0) {
			e.removeAttribute(name);
			return;
		}
		long n = i > 10 ? Long.MAX_VALUE : Long.parseLong(v.substring(0, i));
		e.setAttribute(name, Long.toString(Math.max(min, Math.min(max, n))));
	}

	/** An element's descendants in document order (a snapshot), those named so, or all where name is null. */
	private static List<Element> elements(Element root, String name) {
		List<Element> out = new ArrayList<Element>();
		NodeList all = root.getElementsByTagName("*");
		for (int i = 0; i < all.getLength(); i++) {
			Element e = (Element) all.item(i);
			if (name == null || name.equals(local(e))) out.add(e);
		}
		return out;
	}

	private static Element child(Element e, String name) {
		for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element && name.equals(local((Element) n))) return (Element) n;
		}
		return null;
	}

	static String local(Element e) {
		String n = e.getLocalName() != null ? e.getLocalName() : e.getNodeName();
		int colon = n.indexOf(':');
		return (colon >= 0 ? n.substring(colon + 1) : n).toLowerCase(Locale.ROOT);
	}

	// --- the CSS Word applies ----------------------------------------------------------------------------

	/**
	 * A {@code style} element's CSS with only the rules Word applies (CR-053 section 10.6):
	 * <ul>
	 * <li>a rule inside {@code @media} (screen or print), or another conditional group rule, goes;
	 *     so does {@code @import}, which nothing fetches;</li>
	 * <li>a selector goes where it has a child or sibling combinator, an attribute selector, a pseudo-class
	 *     or pseudo-element, {@code *}, or two classes on one element: Word applies type, class, id and
	 *     descendant selectors only. A rule keeps the selectors in its list that are left;</li>
	 * <li>a type selector alone, or a class selector alone, goes where the host's styles part already
	 *     defines the Word style the rule would go through ({@link #WORD_STYLE}, or the class's name):
	 *     Word keeps the host's definition ({@code host-1}, {@code host-2}).</li>
	 * </ul>
	 * Comments go; other at-rules are kept as they are.
	 */
	static String css(String css, Set<String> hostStyles) {
		StringBuilder out = new StringBuilder();
		String s = withoutComments(css);
		int i = 0, n = s.length();
		while (i < n) {
			char c = s.charAt(i);
			if (Character.isWhitespace(c)) {
				i++;
				continue;
			}
			if (c == '@') {
				int nameEnd = i + 1;
				while (nameEnd < n && (Character.isLetterOrDigit(s.charAt(nameEnd)) || s.charAt(nameEnd) == '-')) nameEnd++;
				String name = s.substring(i + 1, nameEnd).toLowerCase(Locale.ROOT);
				int end = atRuleEnd(s, nameEnd);
				boolean drop = name.equals("media") || name.equals("supports") || name.equals("document")
						|| name.equals("layer") || name.equals("container") || name.equals("import");
				if (!drop) out.append(s, i, end).append('\n');
				i = end;
				continue;
			}
			int open = indexOutsideStrings(s, '{', i);
			if (open < 0) break; // a selector with no block: nothing
			int close = blockEnd(s, open);
			String prelude = s.substring(i, open);
			List<String> kept = new ArrayList<String>();
			for (String selector : splitSelectors(prelude)) {
				if (wordSelector(selector.trim(), hostStyles)) kept.add(selector.trim());
			}
			if (!kept.isEmpty()) out.append(String.join(", ", kept)).append(' ').append(s, open, close).append('\n');
			i = close;
		}
		return out.toString();
	}

	private static final Pattern COMPOUND = Pattern.compile("([A-Za-z][A-Za-z0-9]*)?((?:[#.][-_A-Za-z0-9\\u0080-\\uFFFF]+)*)");

	/** Whether Word applies a rule through this selector. */
	static boolean wordSelector(String selector, Set<String> hostStyles) {
		if (selector.isEmpty() || selector.indexOf('>') >= 0 || selector.indexOf('+') >= 0 || selector.indexOf('~') >= 0) return false;
		String[] compounds = selector.split("\\s+");
		for (String compound : compounds) {
			Matcher m = COMPOUND.matcher(compound);
			if (!m.matches() || compound.isEmpty()) return false;
			String rest = m.group(2);
			int classes = 0, ids = 0;
			for (int k = 0; k < rest.length(); k++) {
				if (rest.charAt(k) == '.') classes++;
				if (rest.charAt(k) == '#') ids++;
			}
			if (classes > 1 || ids > 1) return false;
		}
		if (compounds.length == 1 && hostStyles != null && !hostStyles.isEmpty()) {
			String only = compounds[0];
			if (only.startsWith(".") && only.indexOf('.', 1) < 0 && only.indexOf('#') < 0) {
				if (hostStyles.contains(only.substring(1).toLowerCase(Locale.ROOT))) return false;
			} else if (only.indexOf('.') < 0 && only.indexOf('#') < 0) {
				String style = WORD_STYLE.get(only.toLowerCase(Locale.ROOT));
				if (style != null && hostStyles.contains(style)) return false;
			}
		}
		return true;
	}

	private static String withoutComments(String s) {
		StringBuilder out = new StringBuilder(s.length());
		int i = 0, n = s.length();
		char quote = 0;
		while (i < n) {
			char c = s.charAt(i);
			if (quote != 0) {
				out.append(c);
				if (c == '\\' && i + 1 < n) out.append(s.charAt(++i));
				else if (c == quote) quote = 0;
				i++;
			} else if (c == '"' || c == '\'') {
				quote = c;
				out.append(c);
				i++;
			} else if (c == '/' && i + 1 < n && s.charAt(i + 1) == '*') {
				int end = s.indexOf("*/", i + 2);
				i = end < 0 ? n : end + 2;
				out.append(' ');
			} else {
				out.append(c);
				i++;
			}
		}
		return out.toString();
	}

	/** Where an at-rule that starts its prelude at i ends: after its {@code ;}, or its block's {@code }}. */
	private static int atRuleEnd(String s, int i) {
		int n = s.length();
		char quote = 0;
		for (; i < n; i++) {
			char c = s.charAt(i);
			if (quote != 0) {
				if (c == '\\') i++;
				else if (c == quote) quote = 0;
			} else if (c == '"' || c == '\'') {
				quote = c;
			} else if (c == ';') {
				return i + 1;
			} else if (c == '{') {
				return blockEnd(s, i);
			}
		}
		return n;
	}

	/** Where the block opened at open ends: after its matching {@code }}, or at the end. */
	private static int blockEnd(String s, int open) {
		int depth = 0, n = s.length();
		char quote = 0;
		for (int i = open; i < n; i++) {
			char c = s.charAt(i);
			if (quote != 0) {
				if (c == '\\') i++;
				else if (c == quote) quote = 0;
			} else if (c == '"' || c == '\'') {
				quote = c;
			} else if (c == '{') {
				depth++;
			} else if (c == '}' && --depth == 0) {
				return i + 1;
			}
		}
		return n;
	}

	private static int indexOutsideStrings(String s, char target, int from) {
		char quote = 0;
		for (int i = from; i < s.length(); i++) {
			char c = s.charAt(i);
			if (quote != 0) {
				if (c == '\\') i++;
				else if (c == quote) quote = 0;
			} else if (c == '"' || c == '\'') {
				quote = c;
			} else if (c == target) {
				return i;
			}
		}
		return -1;
	}

	/** A selector list's selectors, split at the commas outside brackets, parentheses and strings. */
	private static List<String> splitSelectors(String prelude) {
		List<String> out = new ArrayList<String>();
		int depth = 0, start = 0;
		char quote = 0;
		for (int i = 0; i < prelude.length(); i++) {
			char c = prelude.charAt(i);
			if (quote != 0) {
				if (c == '\\') i++;
				else if (c == quote) quote = 0;
			} else if (c == '"' || c == '\'') {
				quote = c;
			} else if (c == '(' || c == '[') {
				depth++;
			} else if (c == ')' || c == ']') {
				depth--;
			} else if (c == ',' && depth == 0) {
				out.add(prelude.substring(start, i));
				start = i + 1;
			}
		}
		out.add(prelude.substring(start));
		return out;
	}
}
