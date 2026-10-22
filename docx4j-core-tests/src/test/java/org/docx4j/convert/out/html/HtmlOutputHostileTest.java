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
import org.junit.Test;

/**
 * CR-056 phase 0: what the HTML output must not write (measured 2026-10-23 on 17.3.4: it writes all of
 * it), on both pathways. The link rows are POI's defect (apache/poi#1455); the style rows are docx4j's own.
 * Red until phases 1 (links) and 2 (style IDs); the benign rows pass today and must keep passing.
 * Phase 1 (2026-10-23) turned the link rows green and added the property's test; phase 2 the style rows,
 * with the escaped selectors and the untouched user CSS.
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
		// docx4j's own toggleDiv script is the one script element; none from the document
		assertEquals("script elements\n" + html, 1, HostileDocx.count(html, "<script"));
		assertFalse("the document's script\n" + html, html.contains("<script>"));
		// the class attributes are escaped by the serializer already; the stylesheet must not say otherwise
		assertTrue("the hostile paragraph's class attribute\n" + html, html.contains("class=\"x&lt;/style&gt;"));
		// the selectors, CSS.escape'd: they match the class attributes' decoded tokens
		assertTrue("the hostile paragraph style's selector\n" + html, html.contains(".x\\3c \\/style\\>\\3c script\\>alert\\(\\'H6_STYLEID\\'\\)\\3c \\/script\\> {"));
		assertTrue("the hostile table style's selector\n" + html, html.contains("table.t\\3c \\/style\\>"));
		assertTrue("the digit-leading style's selector\n" + html, html.contains(".\\31 Heading {"));
		assertTrue("the spaced style's selector\n" + html, html.contains(".Body\\ Text\\ 2 {"));
		assertTrue("the digit-leading paragraph's class attribute\n" + html, html.contains("class=\"1Heading "));
		// nothing in the stylesheet can end it
		int style = html.indexOf("<style"), end = html.indexOf("</style>");
		assertTrue(html, style >= 0 && end > style);
		String css = html.substring(html.indexOf('>', style) + 1, end);
		if (css.startsWith("<!--")) css = css.substring(4); // the default handler's comment wrapper
		assertFalse("a < in the stylesheet\n" + html, css.contains("<"));
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
	@Test public void visitorPathwayEscapesStyleIds() throws Exception { noRawStyleIds(html(false)); }
	@Test public void xsltPathwayEscapesStyleIds() throws Exception { noRawStyleIds(html(true)); }

	/** A caller's own CSS is appended after docx4j's and left alone (D5). */
	@Test public void userCssUntouched() throws Exception {
		WordprocessingMLPackage pkg = HostileDocx.build();
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		settings.setImageDirPath(Files.createTempDirectory("cr056").toString()); settings.setImageTargetUri("img");
		settings.setUserCSS("p > span { color: red }");
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, os, Docx4J.FLAG_EXPORT_PREFER_NONXSL);
		String html = os.toString(StandardCharsets.UTF_8);
		assertTrue(html, html.contains("p > span { color: red }"));
		noRawStyleIds(html.replace("p > span { color: red }", ""));
	}
}
