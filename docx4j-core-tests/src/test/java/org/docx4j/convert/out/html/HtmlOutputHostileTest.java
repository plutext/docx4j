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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Ignore;
import org.junit.Test;

/**
 * CR-056 phase 0: what the HTML output must not write (measured 2026-10-23 on 17.3.4: it writes all of
 * it), on both pathways. The link rows are POI's defect (apache/poi#1455); the style rows are docx4j's own.
 * Red until phases 1 (links) and 2 (style IDs); the benign rows pass today and must keep passing.
 */
public class HtmlOutputHostileTest {

	static String html(boolean xslt) throws Exception {
		WordprocessingMLPackage pkg = HostileDocx.build();
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		String dir = Files.createTempDirectory("cr056").toString();
		settings.setImageDirPath(dir); settings.setImageTargetUri("img");
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, os, xslt ? Docx4J.FLAG_EXPORT_PREFER_XSL : Docx4J.FLAG_EXPORT_PREFER_NONXSL);
		return os.toString(StandardCharsets.UTF_8);
	}

	static void noScriptSchemeLinks(String html) {
		String lower = html.toLowerCase();
		assertFalse("a javascript: href\n" + html, lower.contains("href=\"javascript:") || lower.contains("href='javascript:"));
		assertFalse("the sneaky javascript: href\n" + html, html.contains("H3_SNEAKY") && lower.contains("scri\npt:"));
		assertFalse("a vbscript: href\n" + html, lower.contains("vbscript:"));
		assertFalse("a data: href\n" + html, lower.contains("href=\"data:"));
		// the link text stays, as text
		for (String kept : new String[] { "text for ", "field link" }) assertTrue(kept + " kept\n" + html, html.contains(kept));
	}

	static void benignLinksKept(String html) {
		for (String target : HostileDocx.BENIGN) {
			String attr = target.replace("&", "&amp;");
			assertTrue("benign link lost: " + target + "\n" + html, html.contains("href=\"" + attr + "\""));
		}
		assertTrue("benign field link lost\n" + html, html.contains("href=\"https://example.org/field\""));
	}

	static void noRawStyleIds(String html) {
		assertFalse("a style ID closes the style element\n" + html, html.contains("</style><script>"));
		assertEquals("script elements\n" + html, 0, HostileDocx.count(html, "<script"));
		// the class attributes are escaped by the serializer already; the stylesheet must not say otherwise
		assertTrue("the hostile paragraph's class attribute\n" + html, html.contains("class=\"x&lt;/style&gt;"));
	}

	@Ignore("CR-056 phase 0: red until phase 1 (the link allowlist)")
	@Test public void visitorPathwayWritesNoScriptSchemeLink() throws Exception { noScriptSchemeLinks(html(false)); }
	@Ignore("CR-056 phase 0: red until phase 1 (the link allowlist)")
	@Test public void xsltPathwayWritesNoScriptSchemeLink() throws Exception { noScriptSchemeLinks(html(true)); }
	@Test public void visitorPathwayKeepsBenignLinks() throws Exception { benignLinksKept(html(false)); }
	@Test public void xsltPathwayKeepsBenignLinks() throws Exception { benignLinksKept(html(true)); }
	@Ignore("CR-056 phase 0: red until phase 2 (CSS identifier escaping)")
	@Test public void visitorPathwayEscapesStyleIds() throws Exception { noRawStyleIds(html(false)); }
	@Ignore("CR-056 phase 0: red until phase 2 (CSS identifier escaping)")
	@Test public void xsltPathwayEscapesStyleIds() throws Exception { noRawStyleIds(html(true)); }
}
