/*
 * Copyright 2026, Plutext Pty Ltd.
 *
 * This file is part of docx4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fonts.RunFontSelector;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.After;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Word's ligature setting written as {@code fox:gsub-features} (fork CR-001 §8), where
 * the renderer has the hook: a "-liga" on every span of a run asking for no standard
 * ligatures, on the font's own declaration; the +noliga twin is never taken (since
 * 17.3.0, so that the PDF's fonts are subset).  The property
 * docx4j.convert.out.fo.gsubFeatures=true writes it whatever the renderer, which is how
 * these run on Apache FOP too.
 *
 * @since 17.3.0
 */
public class GsubFeaturesTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String W14 = "xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	@After
	public void restore() {
		Docx4jProperties.setProperty("docx4j.convert.out.fo.gsubFeatures", "");
		Docx4jProperties.setProperty("docx4j.convert.out.fo.ligatures", "false");
		Docx4jProperties.setProperty("docx4j.convert.out.fo.noligaTwin", "false");
	}

	@Test
	public void theTwinCanBeAskedBackWithTheHookOn() throws Exception {
		Docx4jProperties.setProperty("docx4j.convert.out.fo.gsubFeatures", "true");
		Docx4jProperties.setProperty("docx4j.convert.out.fo.noligaTwin", "true");
		org.w3c.dom.Document doc = fo(run("", "plain office") + run("<w:kern w:val=\"2\"/><w:sz w:val=\"24\"/>", "kerned office"));
		Element plain = span(doc, "plain office");
		// the 17.2.x declaration where it works, the delta where it never reached
		if (twinned(plain)) assertNull(delta(plain)); else assertEquals("-liga", delta(plain));
		Element kerned = span(doc, "kerned office");
		assertTrue(!twinned(kerned));
		assertEquals("-liga", delta(kerned));
	}

	private static String run(String rPr, String text) {
		return "<w:r><w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\" w:cs=\"Times New Roman\"/>"
				+ rPr + "</w:rPr><w:t>" + text + "</w:t></w:r>";
	}

	private static String lig(String val) {
		return "<w14:ligatures w14:val=\"" + val + "\"/>";
	}

	private static org.w3c.dom.Document fo(String runs) throws Exception {
		return fo(runs, Docx4J.FLAG_NONE);
	}

	private static WordprocessingMLPackage pkg(String runs) throws Exception {
		String xml = "<w:document " + W + " " + W14 + "><w:body><w:p>" + runs + "</w:p>"
				+ "<w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"720\" w:footer=\"720\" w:gutter=\"0\"/>"
				+ "</w:sectPr></w:body></w:document>";
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(xml));
		return pkg;
	}

	private static org.w3c.dom.Document fo(String runs, int flags) throws Exception {
		FOSettings settings = new FOSettings(pkg(runs));
		settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(settings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/**
	 * The XSLT pathway copies each run's fo:inline in from a DOM fragment, and Xalan drops a
	 * namespaced attribute's declaration on the way: until 17.3.1 docx2fo.xslt did not declare
	 * fox on fo:root, so this FO did not parse ("The prefix "fox" for attribute
	 * "fox:gsub-features" ... is not bound").
	 * @since 17.3.1
	 */
	@Test
	public void theXsltPathwayDeclaresTheNamespace() throws Exception {
		Docx4jProperties.setProperty("docx4j.convert.out.fo.gsubFeatures", "true");
		Element el = span(fo(run("", "plain office"), Docx4J.FLAG_EXPORT_PREFER_XSL), "plain office");
		assertEquals("-liga", delta(el));
	}

	/**
	 * And so PDF output through the XSLT pathway failed outright in 17.3.0 on the docx4j FO
	 * renderer: the FO which did not parse skipped WordLayoutFixups, and FOP was handed its
	 * hint attributes ("Invalid property encountered on "fo:block": docx4j-baseline").  With
	 * the renderer's own default for the hook.
	 * @since 17.3.1
	 */
	@Test
	public void theXsltPathwayRendersAPdf() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(new FOSettings(pkg(run("", "plain office"))), baos, Docx4J.FLAG_EXPORT_PREFER_XSL);
		assertTrue(new String(baos.toByteArray(), 0, 5, "US-ASCII").startsWith("%PDF"));
	}

	/** the innermost fo:inline with a font-family whose text contains the marker */
	private static Element span(org.w3c.dom.Document doc, String marker) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "inline");
		Element found = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (el.getAttribute("font-family").length() > 0 && el.getTextContent().contains(marker)) found = el;
		}
		assertNotNull("no span holding " + marker, found);
		return found;
	}

	/** the delta on the span or an ancestor (the property is inherited), or null */
	private static String delta(Element el) {
		for (org.w3c.dom.Node n = el; n instanceof Element; n = n.getParentNode()) {
			Element e = (Element) n;
			if (e.hasAttributeNS(RunFontSelector.FOX_NS, RunFontSelector.GSUB_FEATURES)) {
				return e.getAttributeNS(RunFontSelector.FOX_NS, RunFontSelector.GSUB_FEATURES);
			}
		}
		return null;
	}

	private static boolean twinned(Element el) {
		return el.getAttribute("font-family").endsWith(RunFontSelector.NOLIGA_SUFFIX);
	}

	@Test
	public void theDeltaIsThe16Values() {
		assertEquals("-liga", RunFontSelector.ligatureDelta(null));
		for (org.docx4j.w14.STLigatures v : org.docx4j.w14.STLigatures.values()) {
			boolean standard = v.value().startsWith("standard") || v == org.docx4j.w14.STLigatures.ALL;
			assertEquals(v.value(), standard ? null : "-liga", RunFontSelector.ligatureDelta(v));
		}
	}

	@Test
	public void theTwinIsNeverTakenWithTheHook() throws Exception {
		Docx4jProperties.setProperty("docx4j.convert.out.fo.gsubFeatures", "true");
		Element el = span(fo(run("", "plain office")), "plain office");
		// a Latin run in a TrueType font with no ligatures asked, the twin's own case
		// until 17.3.0: the delta on the font's own declaration, which FOP subsets
		assertTrue(el.getAttribute("font-family"), !twinned(el));
		assertEquals("-liga", delta(el));
	}

	@Test
	public void theTwinWithoutTheHook() throws Exception {
		Docx4jProperties.setProperty("docx4j.convert.out.fo.gsubFeatures", "false");
		Element el = span(fo(run("", "plain office")), "plain office");
		// Apache FOP's path: the twin where the font is TrueType-flavoured, no delta
		assertNull(delta(el));
		assertTrue(el.getAttribute("font-family"), twinned(el)
				|| !org.docx4j.fonts.fop.util.FopConfigUtil.isTrueTypeFlavoured(
						String.valueOf(org.docx4j.fonts.PhysicalFonts.get(el.getAttribute("font-family")) == null ? null
								: org.docx4j.fonts.PhysicalFonts.get(el.getAttribute("font-family")).getEmbeddedURI())));
	}

	@Test
	public void aKernedRunGetsTheDelta() throws Exception {
		Docx4jProperties.setProperty("docx4j.convert.out.fo.gsubFeatures", "true");
		Element el = span(fo(run("<w:kern w:val=\"2\"/><w:sz w:val=\"24\"/>", "kerned office")), "kerned office");
		assertTrue(!twinned(el));
		assertEquals("-liga", delta(el));
	}

	@Test
	public void ligaturesWithoutTheStandardOnesGetTheDelta() throws Exception {
		Docx4jProperties.setProperty("docx4j.convert.out.fo.gsubFeatures", "true");
		org.w3c.dom.Document doc = fo(run(lig("contextual"), "contextual office")
				+ run(lig("discretional"), "discretional office")
				+ run(lig("standard"), "standard office")
				+ run(lig("standardContextual"), "standardContextual office")
				+ run(lig("all"), "all office"));
		assertEquals("-liga", delta(span(doc, "contextual office")));
		assertEquals("-liga", delta(span(doc, "discretional office")));
		assertNull(delta(span(doc, "standard office")));
		assertNull(delta(span(doc, "standardContextual office")));
		assertNull(delta(span(doc, "all office")));
	}

	@Test
	public void cyrillicGetsTheDeltaArabicWaits() throws Exception {
		Docx4jProperties.setProperty("docx4j.convert.out.fo.gsubFeatures", "true");
		org.w3c.dom.Document doc = fo(run("", "офис")
				+ "<w:r><w:rPr><w:rFonts w:cs=\"Times New Roman\"/><w:rtl/></w:rPr><w:t>الله</w:t></w:r>");
		assertEquals("-liga", delta(span(doc, "офис")));
		assertNull(delta(span(doc, "الله")));
	}

	@Test
	public void offWithoutTheHookOrWhenLigaturesAreGlobal() throws Exception {
		Docx4jProperties.setProperty("docx4j.convert.out.fo.gsubFeatures", "false");
		assertNull(delta(span(fo(run("<w:kern w:val=\"2\"/><w:sz w:val=\"24\"/>", "kerned office")), "kerned office")));

		Docx4jProperties.setProperty("docx4j.convert.out.fo.gsubFeatures", "true");
		Docx4jProperties.setProperty("docx4j.convert.out.fo.ligatures", "true");
		Element el = span(fo(run("<w:kern w:val=\"2\"/><w:sz w:val=\"24\"/>", "kerned office")), "kerned office");
		assertNull(delta(el));
		assertTrue(!twinned(el));
	}
}
