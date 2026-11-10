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
package org.docx4j.model.shapes;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * An SVG's ids made its own in an HTML document that holds many: Batik numbers a drawing's gradients, patterns and
 * clip paths from 1, so two drawings in one page would share them.  Moved here from ChartBox (17.3.3) for shapes too
 * (CR-080 phase 5).
 *
 * @since 17.3.7
 */
public final class SvgIds {

	private SvgIds() {}

	private static final Pattern URL = Pattern.compile("url\\(#([^)]*)\\)");

	/** Each id in the SVG prefixed, and each reference to one ({@code url(#id)}, {@code #id}) */
	public static void prefix(Element svg, String prefix) {
		Set<String> ids = new HashSet<String>();
		collect(svg, ids);
		if (!ids.isEmpty()) rename(svg, ids, prefix);
	}

	private static void collect(Element e, Set<String> ids) {
		if (e.hasAttribute("id")) ids.add(e.getAttribute("id"));
		for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element) collect((Element) n, ids);
		}
	}

	private static void rename(Element e, Set<String> ids, String prefix) {
		NamedNodeMap attrs = e.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			Attr a = (Attr) attrs.item(i);
			String v = a.getValue();
			if ("id".equals(a.getName())) {
				a.setValue(prefix + v);
			} else if (v.startsWith("#") && ids.contains(v.substring(1))) {
				a.setValue("#" + prefix + v.substring(1));
			} else if (v.contains("url(#")) {
				StringBuilder b = new StringBuilder();
				Matcher m = URL.matcher(v);
				while (m.find()) {
					m.appendReplacement(b, Matcher.quoteReplacement(ids.contains(m.group(1)) ? "url(#" + prefix + m.group(1) + ")" : m.group()));
				}
				m.appendTail(b);
				a.setValue(b.toString());
			}
		}
		for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element) rename((Element) n, ids, prefix);
		}
	}
}
