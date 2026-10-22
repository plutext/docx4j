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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.docx4j.Docx4jProperties;
import org.junit.Test;

/** CR-056 phase 1: the link target allowlist, read as a browser reads a scheme. */
public class LinkTargetsTest {

	@Test
	public void safeTargets() {
		for (String t : new String[] { null, "", "http://example.org/a", "HTTPS://example.org/b", "mailto:a@example.org",
				"ftp://example.org/c", "file:///C:/docs/d.docx", "tel:+61000000", "news:comp.lang.java", "sub/dir/page.html",
				"#bookmark1", "C:\\docs\\e.docx", "c:/docs/e.docx", "\\\\server\\share\\f.docx", "page.html?x=javascript:1",
				"page.html#javascript:1", "/root/path:with:colons", "  http://example.org/leading-spaces" }) {
			assertTrue(String.valueOf(t), LinkTargets.isSafe(t));
		}
	}

	@Test
	public void refusedTargets() {
		for (String t : new String[] { "javascript:alert(1)", "JavaScript:alert(1)", " \u0001java\tscri\npt:alert(2)",
				"\r\njavascript:alert(3)", "vbscript:MsgBox(1)", "data:text/html,<script>alert(1)</script>", "onenote:///x",
				"ms-word:ofe|u|https://example.org/x.docx", "C:", "java script:alert(1)", "about:blank", "blob:x" }) {
			assertFalse(t, LinkTargets.isSafe(t));
		}
	}

	@Test
	public void schemeAsABrowserReadsIt() {
		assertEquals("javascript", LinkTargets.scheme(" \u0001java\tscri\npt:alert(2)"));
		assertEquals("http", LinkTargets.scheme("HTTP://x"));
		assertEquals(null, LinkTargets.scheme("sub/dir:x"));
		assertEquals(null, LinkTargets.scheme("#frag:x"));
		assertEquals(null, LinkTargets.scheme(":x"));
		assertEquals("c", LinkTargets.scheme("C:\\x"));
	}

	@Test
	public void propertyAddsSchemes() {
		String before = Docx4jProperties.getProperty(LinkTargets.LINK_SCHEMES_PROPERTY);
		Docx4jProperties.setProperty(LinkTargets.LINK_SCHEMES_PROPERTY, " OneNote:, ms-word ,,notes");
		try {
			assertTrue(LinkTargets.schemes().containsAll(LinkTargets.defaultSchemes()));
			assertTrue(LinkTargets.schemes().contains("onenote"));
			assertTrue(LinkTargets.isSafe("onenote:///x"));
			assertTrue(LinkTargets.isSafe("ms-word:ofe|u|https://example.org/x.docx"));
			assertTrue(LinkTargets.isSafe("Notes://server/db.nsf"));
			assertFalse("a default is never removed", LinkTargets.isSafe("javascript:alert(1)"));
		} finally {
			if (before == null) Docx4jProperties.getProperties().remove(LinkTargets.LINK_SCHEMES_PROPERTY);
			else Docx4jProperties.setProperty(LinkTargets.LINK_SCHEMES_PROPERTY, before);
		}
		assertFalse("restored", LinkTargets.isSafe("onenote:///x"));
	}

	@Test
	public void excerptForTheLog() {
		assertEquals("java scri pt:alert(1)", LinkTargets.excerpt("java\tscri\npt:alert(1)"));
		assertEquals(83, LinkTargets.excerpt(new String(new char[200]).replace('\0', 'x')).length());
	}
}
