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
package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.fo.FopCapabilities;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.After;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A merged run of continuous sections whose vertical margins differ gets page masters per part,
 * chosen by the content a page starts with (CR-031 phase 2): Word gives a page the top and bottom
 * margins and the header and footer distances of the section owning its first line (CR-031 D1,
 * measured on the continuous-margins-vertical probes), which one page master cannot.  docx4j
 * writes the parts' masters and the docx4j FO renderer's {@code fox:page-sequence-master-reference}
 * (fork CR-017) where that renderer has it; property
 * {@code docx4j.convert.out.fo.wordLayout.pageMasterByContent} ({@code true} writes them whatever
 * the renderer, {@code false} never).
 *
 * <p>The document is the probes' shape: S1 72/72pt margins and 36/36pt header and footer
 * distances, S2 continuous at 144/36 and 18/18, S3 continuous back at 72/72 and 36/36.  The
 * page-sequence's own masters are S1's; S2's pages need masters of their own ({@code s1-p2}),
 * and S3's pages S1's margins again, without S1's first-page master ({@code s1-p1}).</p>
 *
 * @since 17.3.1
 */
public class PageMasterByContentTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String R = "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final String FOX = "http://xmlgraphics.apache.org/fop/extensions";
	private static final String PROPERTY = "docx4j.convert.out.fo.wordLayout.pageMasterByContent";

	private static final String A4 = "<w:pgSz w:w=\"11906\" w:h=\"16838\"/>";

	@After
	public void resetProperty() {
		Docx4jProperties.getProperties().remove(PROPERTY);
	}

	private static String pgMar(int top, int bottom, int header, int footer) {
		return "<w:pgMar w:top=\"" + top + "\" w:right=\"1440\" w:bottom=\"" + bottom + "\" w:left=\"1440\""
				+ " w:header=\"" + header + "\" w:footer=\"" + footer + "\" w:gutter=\"0\"/>";
	}

	private static String para(String text) {
		return "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	/** A header part holding one paragraph of this text; returns its relationship id. */
	private static String header(WordprocessingMLPackage pkg, String name) throws Exception {
		org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart hp =
				new org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart(
						new org.docx4j.openpackaging.parts.PartName("/word/" + name + ".xml"));
		hp.setPackage(pkg);
		hp.setJaxbElement((org.docx4j.wml.Hdr) XmlUtils.unmarshalString(
				"<w:hdr " + W + "><w:p><w:r><w:t>" + name + "</w:t></w:r></w:p></w:hdr>",
				org.docx4j.jaxb.Context.jc, org.docx4j.wml.Hdr.class));
		return pkg.getMainDocumentPart().addTargetPart(hp).getId();
	}

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String h = header(pkg, "runningHead");
		String ref = "<w:headerReference w:type=\"default\" r:id=\"" + h + "\"/>";
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + " " + R + "><w:body>"
				+ para("S1 first")
				+ "<w:p><w:pPr><w:sectPr>" + ref + A4 + pgMar(1440, 1440, 720, 720) + "</w:sectPr></w:pPr>"
				+ "<w:r><w:t>S1 last</w:t></w:r></w:p>"
				+ para("S2 first")
				+ "<w:p><w:pPr><w:sectPr><w:type w:val=\"continuous\"/>" + A4 + pgMar(2880, 720, 360, 360)
				+ "</w:sectPr></w:pPr><w:r><w:t>S2 last</w:t></w:r></w:p>"
				+ para("S3 first")
				+ para("S3 last")
				+ "<w:sectPr><w:type w:val=\"continuous\"/>" + A4 + pgMar(1440, 1440, 720, 720) + "</w:sectPr>"
				+ "</w:body></w:document>"));
		return pkg;
	}

	private org.w3c.dom.Document fo(int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg());
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	private static Element named(org.w3c.dom.Document doc, String localName, String masterName) {
		NodeList nl = doc.getElementsByTagNameNS(FO, localName);
		for (int i = 0; i < nl.getLength(); i++) {
			if (masterName.equals(((Element) nl.item(i)).getAttribute("master-name"))) return (Element) nl.item(i);
		}
		return null;
	}

	/** The elements carrying the renderer's attribute, in document order. */
	private static List<Element> marked(org.w3c.dom.Document doc) {
		List<Element> out = new ArrayList<Element>();
		NodeList nl = doc.getElementsByTagName("*");
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			if (el.hasAttributeNS(FOX, "page-sequence-master-reference")) out.add(el);
		}
		return out;
	}

	private static boolean anyStamp(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagName("*");
		for (int i = 0; i < nl.getLength(); i++) {
			if (((Element) nl.item(i)).hasAttribute("docx4j-psm")) return true;
		}
		return false;
	}

	/** A length in points; an unstated one (a region-body with no footer below it) is 0. */
	private static float pt(String length) {
		return length.isEmpty() ? 0f : Float.parseFloat(length.replace("pt", ""));
	}

	private void checkOn(int flags) throws Exception {
		Docx4jProperties.setProperty(PROPERTY, "true");
		org.w3c.dom.Document doc = fo(flags);
		assertEquals("the three continuous sections are one page-sequence", 1,
				doc.getElementsByTagNameNS(FO, "page-sequence").getLength());

		// S2's masters, and S1's margins again for S3, neither with a first-page alternative
		for (String psm : new String[] { "s1-p2", "s1-p1" }) {
			Element master = named(doc, "page-sequence-master", psm);
			assertTrue(psm + " is written", master != null);
			NodeList refs = master.getElementsByTagNameNS(FO, "conditional-page-master-reference");
			for (int i = 0; i < refs.getLength(); i++) {
				assertFalse(psm + " has no first-page master", "first".equals(((Element) refs.item(i)).getAttribute("page-position")));
			}
		}

		// S2's page master: its own 18pt header distance and 144pt top margin, 36pt bottom
		Element s2 = named(doc, "simple-page-master", "s1-p2-default");
		assertTrue("S2's page master is written", s2 != null);
		Element body = (Element) s2.getElementsByTagNameNS(FO, "region-body").item(0);
		assertEquals("the header at S2's header distance", 18f, pt(s2.getAttribute("margin-top")), 0.01f);
		assertEquals("the body at S2's top margin", 144f,
				pt(s2.getAttribute("margin-top")) + pt(body.getAttribute("margin-top")), 0.01f);
		assertEquals("the body ends at S2's bottom margin", 36f,
				pt(s2.getAttribute("margin-bottom")) + pt(body.getAttribute("margin-bottom")), 0.01f);
		Element s3 = named(doc, "simple-page-master", "s1-p1-default");
		Element body3 = (Element) s3.getElementsByTagNameNS(FO, "region-body").item(0);
		assertEquals("S3's pages take S1's 72pt top margin", 72f,
				pt(s3.getAttribute("margin-top")) + pt(body3.getAttribute("margin-top")), 0.01f);
		assertEquals("the parts show the page-sequence's own header",
				"xsl-region-before-default",
				((Element) s2.getElementsByTagNameNS(FO, "region-before").item(0)).getAttribute("region-name"));

		// the renderer's attribute on the first block of S2 and of S3, and nowhere else
		List<Element> marks = marked(doc);
		assertEquals("two part boundaries", 2, marks.size());
		assertEquals("s1-p2", marks.get(0).getAttributeNS(FOX, "page-sequence-master-reference"));
		assertEquals("S2 first", marks.get(0).getTextContent().trim());
		assertEquals("s1-p1", marks.get(1).getAttributeNS(FOX, "page-sequence-master-reference"));
		assertEquals("S3 first", marks.get(1).getTextContent().trim());
		assertFalse("the private stamps are gone", anyStamp(doc));
	}

	private void checkOff(int flags) throws Exception {
		Docx4jProperties.setProperty(PROPERTY, "false");
		org.w3c.dom.Document doc = fo(flags);
		assertTrue("no part masters", named(doc, "page-sequence-master", "s1-p2") == null);
		assertEquals("no attribute", 0, marked(doc).size());
		assertFalse("no stamps", anyStamp(doc));
	}

	/** By default it follows the renderer's capability (fork CR-017). */
	private void checkDefault(int flags) throws Exception {
		boolean hook = FopCapabilities.has(FopCapabilities.Capability.PAGE_MASTER_BY_CONTENT);
		org.w3c.dom.Document doc = fo(flags);
		assertEquals("part masters where the renderer chooses masters by content", hook,
				named(doc, "page-sequence-master", "s1-p2") != null);
		assertEquals(hook ? 2 : 0, marked(doc).size());
		assertFalse(anyStamp(doc));
	}

	@Test
	public void onVisitor() throws Exception {
		checkOn(Docx4J.FLAG_NONE);
	}

	@Test
	public void onXslt() throws Exception {
		checkOn(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void offVisitor() throws Exception {
		checkOff(Docx4J.FLAG_NONE);
	}

	@Test
	public void offXslt() throws Exception {
		checkOff(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void byDefaultTheRenderersCapability() throws Exception {
		checkDefault(Docx4J.FLAG_NONE);
		checkDefault(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
