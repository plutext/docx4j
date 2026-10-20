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

/**
 * The one class of the text model that names jsoup (an optional dependency, CR-046 D5): loaded only
 * when {@link #present()} has found it.
 */
final class JsoupHtml {

	private JsoupHtml() {}

	/** The nesting jsoup builds before it makes deeper elements siblings (CR-053 section 10.7; its own cap is 512). */
	static final int MAX_DEPTH = 256;

	private static Boolean present;

	/** Whether jsoup is on the class path. */
	static boolean present() {
		if (present == null) {
			try {
				Class.forName("org.jsoup.Jsoup", false, JsoupHtml.class.getClassLoader());
				present = Boolean.TRUE;
			} catch (ClassNotFoundException | LinkageError e) {
				present = Boolean.FALSE;
			}
		}
		return present;
	}

	/**
	 * HTML parsed by jsoup, [HTML]'s algorithm with its repairs, as a W3C DOM whose HTML elements carry
	 * the XHTML namespace; nested no deeper than {@link #MAX_DEPTH} where jsoup can be told (1.22.1 on).
	 * A leading U+FEFF is not text: jsoup would keep it, and close the head before its {@code style}s.
	 */
	static org.w3c.dom.Document parse(String html) {
		if (html.startsWith("﻿")) html = html.substring(1);
		org.jsoup.parser.Parser parser = org.jsoup.parser.Parser.htmlParser();
		try {
			parser.setMaxDepth(MAX_DEPTH);
		} catch (NoSuchMethodError e) {
			// an earlier jsoup: its own cap
		}
		org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(html, "", parser);
		return new org.jsoup.helper.W3CDom().fromJsoup(doc);
	}
}
