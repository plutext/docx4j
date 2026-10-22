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
package org.docx4j.convert.out.html;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** CR-056 phase 2: CSS.escape semantics for style IDs, and the stylesheet guard. */
public class CssIdentifiersTest {

	@Test
	public void cssEscapeSemantics() {
		// the CSSOM examples
		assertEquals("Heading1", CssIdentifiers.escape("Heading1"));
		assertEquals("\\31 Heading", CssIdentifiers.escape("1Heading"));
		assertEquals("-\\31 x", CssIdentifiers.escape("-1x"));
		assertEquals("\\-", CssIdentifiers.escape("-"));
		assertEquals("--a", CssIdentifiers.escape("--a"));
		assertEquals("Body\\ Text\\ 2", CssIdentifiers.escape("Body Text 2"));
		assertEquals("a\\.b", CssIdentifiers.escape("a.b"));
		assertEquals("\\1 x", CssIdentifiers.escape("\u0001x"));
		assertEquals("\uFFFDx", CssIdentifiers.escape("\u0000x"));
		assertEquals("caf\u00e9_\u4e2d", CssIdentifiers.escape("caf\u00e9_\u4e2d"));
		assertEquals(null, CssIdentifiers.escape(null));
	}

	@Test
	public void lessThanIsAHexEscape() {
		assertEquals("x\\3c \\/style\\>\\3c script\\>", CssIdentifiers.escape("x</style><script>"));
		String escaped = CssIdentifiers.escape("x</style><script>");
		assertEquals("the guard finds nothing to do in an escaped identifier", escaped, CssIdentifiers.guard(escaped));
	}

	@Test
	public void guard() {
		assertEquals(".a {font-family: 'Arial\\3c /style>'}", CssIdentifiers.guard(".a {font-family: 'Arial</style>'}"));
		assertEquals("no change", CssIdentifiers.guard("no change"));
		assertEquals(null, CssIdentifiers.guard(null));
	}
}
