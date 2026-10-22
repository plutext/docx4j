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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.common.writer.LinkTargets;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Ignore;
import org.junit.Test;

/**
 * CR-056 phase 0: what the HTML output must not write (measured 2026-10-23 on 17.3.4: it writes all of
 * it), on both pathways. The link rows are POI's defect (apache/poi#1455); the style rows are docx4j's own.
 * Red until phases 1 (links) and 2 (style IDs); the benign rows pass today and must keep passing.
 * Phase 1 (2026-10-23) turned the link rows green and added the property's test.
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

	/** Every href attribute's value (the serializer's quoting is double quotes). */
	static List<String> hrefs(String html) {
		List<String> ret = new ArrayList<String>();
		Matcher m = Pattern.compile("href=\"([^\"]*)\"").matcher(html);
		while (m.find()) ret.add(m.group(1).replace("&amp;", "&"));
		return ret;
	}

	static void noScriptSchemeLinks(String html) {
		for (String href : hrefs(html)) {
			String scheme = LinkTargets.scheme(href);
			assertFalse("a script-scheme href: " + href + "\n" + html,
					"javascript".equals(scheme) || "vbscript".equals(scheme) || "data".equals(scheme));
		}
		assertFalse("the sneaky javascript: link has a href\n" + html, hrefs(html).toString().contains("H3_SNEAKY"));
		assertFalse("the custom-scheme link has a href by default\n" + html, hrefs(html).contains(HostileDocx.CUSTOM_LINK));
		// the link text stays, as text, and the hostile targets only there
		for (String kept : new String[] { "text for javascript:alert('H1_LINK')", "H3_SNEAKY", "field link" }) assertTrue(kept + " kept\n" + html, html.contains(kept));
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

	@Test public void visitorPathwayWritesNoScriptSchemeLink() throws Exception { noScriptSchemeLinks(html(false)); }
	@Test public void xsltPathwayWritesNoScriptSchemeLink() throws Exception { noScriptSchemeLinks(html(true)); }

	/** The property adds a scheme to the default set (D3), on both pathways. */
	@Test public void propertyAddsAScheme() throws Exception {
		String before = Docx4jProperties.getProperty(LinkTargets.LINK_SCHEMES_PROPERTY);
		Docx4jProperties.setProperty(LinkTargets.LINK_SCHEMES_PROPERTY, "ms-word, OneNote:");
		try {
			for (boolean xslt : new boolean[] { false, true }) {
				String html = html(xslt);
				assertTrue("the custom-scheme link kept\n" + html, hrefs(html).contains(HostileDocx.CUSTOM_LINK));
				noScriptSchemeLinks(html.replace("href=\"" + HostileDocx.CUSTOM_LINK + "\"", "")); // the script schemes still refused
			}
		} finally {
			if (before == null) Docx4jProperties.getProperties().remove(LinkTargets.LINK_SCHEMES_PROPERTY);
			else Docx4jProperties.setProperty(LinkTargets.LINK_SCHEMES_PROPERTY, before);
		}
	}
	@Test public void visitorPathwayKeepsBenignLinks() throws Exception { benignLinksKept(html(false)); }
	@Test public void xsltPathwayKeepsBenignLinks() throws Exception { benignLinksKept(html(true)); }
	@Ignore("CR-056 phase 0: red until phase 2 (CSS identifier escaping)")
	@Test public void visitorPathwayEscapesStyleIds() throws Exception { noRawStyleIds(html(false)); }
	@Ignore("CR-056 phase 0: red until phase 2 (CSS identifier escaping)")
	@Test public void xsltPathwayEscapesStyleIds() throws Exception { noRawStyleIds(html(true)); }
}
