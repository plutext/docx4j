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
 * when {@link HtmlText#hasJsoup()} has found it.
 */
final class JsoupHtml {

	private JsoupHtml() {}

	/** HTML tidied by jsoup, as a W3C DOM. */
	static org.w3c.dom.Document parse(String html) {
		org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(html);
		return new org.jsoup.helper.W3CDom().fromJsoup(doc);
	}
}
