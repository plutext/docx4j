/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.fidelity.corpus;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilderFactory;

import org.docx4j.model.color.ResolvedColor;
import org.docx4j.model.color.ThemeColors;
import org.docx4j.model.color.WordColors;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * CR-034 phase 1's corpus check: every WordprocessingML theme colour attribute in a set of docx,
 * resolved by docx4j ({@link ThemeColors#of(WordprocessingMLPackage)}, {@link WordColors}) and
 * compared with the value Word wrote beside it.  Two counts: the computation alone (the theme
 * colour, tinted or shaded, against Word's value), and what {@link WordColors#resolve} returns
 * (Word's value where it is a current cache, the computation where it is stale).  Counts only;
 * the corpora are not redistributable.
 *
 * <p>usage: {@code java -cp ... org.docx4j.fidelity.corpus.ThemeColourCheck DOCX...}</p>
 *
 * @since 17.3.3 (CR-034 phase 1)
 */
public final class ThemeColourCheck {

	private static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	private static final Pattern PARTS = Pattern.compile(
			"word/(document\\d*|styles|numbering|header\\d*|footer\\d*|footnotes|endnotes|comments)\\.xml");

	public static void main(String[] args) throws Exception {
		Map<String, Integer> n = new TreeMap<>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		for (String path : args) {
			WordprocessingMLPackage pkg;
			try {
				pkg = WordprocessingMLPackage.load(new File(path));
			} catch (Exception e) {
				n.merge("documents docx4j could not load", 1, Integer::sum);
				continue;
			}
			n.merge("documents", 1, Integer::sum);
			boolean themePart = pkg.getMainDocumentPart().getThemePart() != null;
			ThemeColors theme = ThemeColors.of(pkg);
			try (ZipFile z = new ZipFile(path)) {
				for (ZipEntry e : java.util.Collections.list(z.entries())) {
					if (!PARTS.matcher(e.getName()).matches()) continue;
					NodeList all = dbf.newDocumentBuilder().parse(z.getInputStream(e)).getElementsByTagNameNS("*", "*");
					for (int i = 0; i < all.getLength(); i++) {
						Element el = (Element) all.item(i);
						check(el, "themeColor", "themeTint", "themeShade", "color".equals(el.getLocalName()) ? "val" : "color",
								theme, themePart, n);
						check(el, "themeFill", "themeFillTint", "themeFillShade", "fill", theme, themePart, n);
					}
				}
			}
		}
		for (Map.Entry<String, Integer> e : n.entrySet()) {
			System.out.println(String.format("%-62s %d", e.getKey(), e.getValue()));
		}
	}

	private static String attr(Element e, String name) {
		String v = e.getAttributeNS(W, name);
		return v.isEmpty() ? null : v;
	}

	private static void check(Element el, String tc, String tt, String ts, String val, ThemeColors theme, boolean themePart,
			Map<String, Integer> n) {
		String themeColor = attr(el, tc);
		if (themeColor == null || "none".equals(themeColor)) return;
		String tint = attr(el, tt), shade = attr(el, ts);
		String op = tint != null ? "tint" : shade != null ? "shade" : "plain";
		ResolvedColor written = ResolvedColor.ofHex(attr(el, val));
		if (written == null) {
			n.merge("attributes with no hex value beside them", 1, Integer::sum);
			return;
		}
		if (!themePart) n.merge("attributes in documents with no theme part (default theme)", 1, Integer::sum);
		ResolvedColor computed = WordColors.resolve(null, themeColor, tint, shade, theme);
		ResolvedColor resolved = WordColors.resolve(attr(el, val), themeColor, tint, shade, theme);
		String scope = themePart ? "" : " [no theme part]";
		if (computed == null) {
			n.merge("unresolvable theme colour" + scope, 1, Integer::sum);
		} else if (computed.equals(written)) {
			n.merge("computed " + op + ": exact" + scope, 1, Integer::sum);
		} else if (computed.within(written, 1)) {
			n.merge("computed " + op + ": one unit" + scope, 1, Integer::sum);
		} else {
			n.merge("computed " + op + ": differs from the written value" + scope, 1, Integer::sum);
		}
		n.merge("resolved = written value" + (resolved != null && resolved.equals(written) ? "" : " NOT") + scope, 1,
				Integer::sum);
	}
}
