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
package org.docx4j.convert.out.common.writer;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.docx4j.Docx4jProperties;

/**
 * Which link targets the outputs (HTML, XSL-FO/PDF, Markdown, and docx4j-tika's XHTML) will write
 * as a link (CR-056). A document's {@code w:hyperlink} relationship target, HYPERLINK field
 * target or picture click target is written as the {@code href} as it is, so a crafted
 * {@code javascript:alert(1)} target runs script in the page that serves the HTML (CWE-79, the
 * defect apache/poi#1455 fixed in POI's {@code WordToHtmlConverter}). The rule is an allowlist of
 * schemes, as POI chose: a target with no scheme (a relative path, a fragment, a UNC path), a
 * Windows drive path ({@code C:\...}), and {@code http}, {@code https}, {@code ftp}, {@code mailto},
 * {@code file}, {@code news} and {@code tel}, plus whatever the property
 * {@value #LINK_SCHEMES_PROPERTY} adds (a comma-separated list, for a deployment whose documents
 * carry {@code onenote:}, {@code ms-word:} or {@code notes:} links). A refused target gives the
 * link's text with no link.
 *
 * <p>The scheme is read as a browser reads one (WHATWG URL parsing): leading control characters
 * and spaces are ignored and tabs and line breaks are dropped anywhere, so {@code  \u0001java\tscri\npt:}
 * is {@code javascript:}; the comparison is case-insensitive. Anything up to the first {@code ':'}
 * (where that comes before any {@code '/'}, {@code '?'} or {@code '#'}) is the scheme, so a
 * target whose scheme a browser would reject as malformed is refused too, rather than second-guessed.
 *
 * @since 17.3.5
 */
public final class LinkTargets {

	/** A comma-separated list of schemes added to the default set; {@code docx4j.convert.out.linkSchemes}. */
	public static final String LINK_SCHEMES_PROPERTY = "docx4j.convert.out.linkSchemes";

	private static final Set<String> DEFAULT_SCHEMES = Collections.unmodifiableSet(new LinkedHashSet<String>(
			Arrays.asList("http", "https", "ftp", "mailto", "file", "news", "tel")));

	private LinkTargets() {}

	/** The default schemes (POI's set), unmodifiable. */
	public static Set<String> defaultSchemes() {
		return DEFAULT_SCHEMES;
	}

	/** The schemes a link may use: the defaults plus the property's, lower-cased; read each call. */
	public static Set<String> schemes() {
		String extra = Docx4jProperties.getProperty(LINK_SCHEMES_PROPERTY);
		if (extra == null || extra.trim().isEmpty()) return DEFAULT_SCHEMES;
		Set<String> set = new LinkedHashSet<String>(DEFAULT_SCHEMES);
		for (String s : extra.split(",")) {
			s = s.trim().toLowerCase(Locale.ROOT);
			if (s.endsWith(":")) s = s.substring(0, s.length() - 1);
			if (!s.isEmpty()) set.add(s);
		}
		return Collections.unmodifiableSet(set);
	}

	/**
	 * Whether an output may write this target as a link: it has no scheme, is a drive path, or its
	 * scheme (read as a browser reads it) is in {@link #schemes()}. A null or empty target is safe
	 * (there is nothing to write).
	 */
	public static boolean isSafe(String target) {
		String scheme = scheme(target);
		if (scheme == null) return true;
		if (scheme.length() == 1 && Character.isLetter(scheme.charAt(0))) {
			// a Windows drive path, C:\ or C:/
			String rest = normalise(target);
			char next = rest.length() > 2 ? rest.charAt(2) : 0;
			return next == '\\' || next == '/';
		}
		return schemes().contains(scheme);
	}

	/**
	 * The target's scheme, read as a browser reads it, lower-cased; null where it has none (a
	 * relative path, a fragment, a UNC path, or nothing at all).
	 */
	public static String scheme(String target) {
		if (target == null) return null;
		String s = normalise(target);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == ':') return i == 0 ? null : s.substring(0, i).toLowerCase(Locale.ROOT);
			if (c == '/' || c == '?' || c == '#') return null;
		}
		return null;
	}

	/** Leading controls and spaces removed; tabs and line breaks removed anywhere. */
	static String normalise(String target) {
		StringBuilder sb = new StringBuilder(target.length());
		for (int i = 0; i < target.length(); i++) {
			char c = target.charAt(i);
			if (c == '\t' || c == '\n' || c == '\r') continue;
			if (c <= ' ' && sb.length() == 0) continue;
			sb.append(c);
		}
		return sb.toString();
	}

	/** A target for a log line: at most 80 characters, control characters shown as spaces. */
	public static String excerpt(String target) {
		if (target == null) return "null";
		String s = target.length() > 80 ? target.substring(0, 80) + "..." : target;
		StringBuilder sb = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			sb.append(c < ' ' ? ' ' : c);
		}
		return sb.toString();
	}

	/** The message the outputs log, once per document, when they refuse a target. */
	public static String refusedMessage(String target) {
		return "Link target refused (scheme not in " + LINK_SCHEMES_PROPERTY + "): the text is written without a link: "
				+ excerpt(target);
	}
}
