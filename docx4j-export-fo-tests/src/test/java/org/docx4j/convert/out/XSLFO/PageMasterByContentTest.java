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

	/**
	 * Corpus document 5507's shape: a first section with an empty footer part and
	 * {@code w:footer="5811"} (290.55pt), then a continuous one at 709 (35.45pt).  Word
	 * honours the 290.55pt on the first section's pages (its pages 1 and 2 end at y=531) and
	 * the 35.45pt on the second's (page 3 at 762.5).  With part masters each section's
	 * master has its own; without them the run's one master keeps the quarter-page clamp,
	 * the lesser error, since honouring 290.55pt there would end every page of the run high.
	 */
	private WordprocessingMLPackage letter() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		org.docx4j.openpackaging.parts.WordprocessingML.FooterPart footer =
				new org.docx4j.openpackaging.parts.WordprocessingML.FooterPart(
						new org.docx4j.openpackaging.parts.PartName("/word/footer1.xml"));
		footer.setJaxbElement((org.docx4j.wml.Ftr) XmlUtils.unmarshalString(
				"<w:ftr " + W + "><w:p/></w:ftr>", org.docx4j.jaxb.Context.jc, org.docx4j.wml.Ftr.class));
		String f = pkg.getMainDocumentPart().addTargetPart(footer).getId();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + " " + R + "><w:body>"
				+ "<w:p><w:pPr><w:sectPr><w:footerReference w:type=\"default\" r:id=\"" + f + "\"/>"
				+ A4 + pgMar(964, 1418, 709, 5811) + "</w:sectPr></w:pPr><w:r><w:t>letterhead</w:t></w:r></w:p>"
				+ para("the letter")
				+ "<w:sectPr><w:type w:val=\"continuous\"/>" + A4 + pgMar(964, 1417, 709, 709) + "</w:sectPr>"
				+ "</w:body></w:document>"));
		return pkg;
	}

	/** The distance from the page's bottom edge to the body's, in points. */
	private static float bodyFoot(org.w3c.dom.Document doc, String master) {
		Element spm = named(doc, "simple-page-master", master);
		Element body = (Element) spm.getElementsByTagNameNS(FO, "region-body").item(0);
		return pt(spm.getAttribute("margin-bottom")) + pt(body.getAttribute("margin-bottom"));
	}

	private void checkLetter(int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(letter());
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);

		Docx4jProperties.setProperty(PROPERTY, "false");
		ByteArrayOutputStream off = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, off, flags);
		org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(off.toByteArray());
		assertEquals("one master for the run: the clamp, the body to the 70.9pt bottom margin", 70.9f,
				bodyFoot(doc, "s1-default"), 0.1f);

		Docx4jProperties.setProperty(PROPERTY, "true");
		foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(letter());
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream on = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, on, flags);
		doc = w3cDomDocumentFromByteArray(on.toByteArray());
		assertTrue("the first section's pages honour its 290.55pt footer distance, and the empty footer's line: "
				+ bodyFoot(doc, "s1-default"), bodyFoot(doc, "s1-default") > 300f);
		// 35.45pt + the empty footer's line and space-after is 60.89, inside the 70.85pt
		// bottom margin, which therefore ends the body (Word's page 3 ends at 762.5)
		assertEquals("the second section's pages: its own bottom margin", 70.85f,
				bodyFoot(doc, "s1-p2-default"), 0.1f);
	}

	@Test
	public void aLargeFooterDistanceOnlyWhereThePagesFollowTheirSection() throws Exception {
		checkLetter(Docx4J.FLAG_NONE);
		checkLetter(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/**
	 * Each part's own headers and footers on the pages it owns (CR-031 phase 3; D3: a page takes
	 * the header and footer text of the section owning its first line).  The three sections'
	 * margins are the same, so only their headers tell them apart: S1 a header; S2 its own, and
	 * w:titlePg with a first-page header; S3 none of its own, so Word's inheritance gives it S2's
	 * default header but not S2's first page (w:titlePg is the section's own).
	 */
	private WordprocessingMLPackage ownHeaders() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String h1 = header(pkg, "headS1"), h2 = header(pkg, "headS2"), f2 = header(pkg, "firstS2");
		String mar = A4 + pgMar(1440, 1440, 720, 720);
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + " " + R + "><w:body>"
				+ "<w:p><w:pPr><w:sectPr><w:headerReference w:type=\"default\" r:id=\"" + h1 + "\"/>" + mar
				+ "</w:sectPr></w:pPr><w:r><w:t>S1 text</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:sectPr><w:headerReference w:type=\"default\" r:id=\"" + h2 + "\"/>"
				+ "<w:headerReference w:type=\"first\" r:id=\"" + f2 + "\"/><w:type w:val=\"continuous\"/>" + mar
				+ "<w:titlePg/></w:sectPr></w:pPr><w:r><w:t>S2 text</w:t></w:r></w:p>"
				+ para("S3 text")
				+ "<w:sectPr><w:type w:val=\"continuous\"/>" + mar + "</w:sectPr>"
				+ "</w:body></w:document>"));
		return pkg;
	}

	/** The text of the page-sequence's fo:static-content of this flow name, or null. */
	private static String staticContent(org.w3c.dom.Document doc, String flowName) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "static-content");
		for (int i = 0; i < nl.getLength(); i++) {
			Element sc = (Element) nl.item(i);
			if (flowName.equals(sc.getAttribute("flow-name"))) return sc.getTextContent().trim();
		}
		return null;
	}

	private static String regionBefore(org.w3c.dom.Document doc, String master) {
		Element spm = named(doc, "simple-page-master", master);
		return spm == null ? null
				: ((Element) spm.getElementsByTagNameNS(FO, "region-before").item(0)).getAttribute("region-name");
	}

	private void checkOwnHeaders(int flags) throws Exception {
		Docx4jProperties.setProperty(PROPERTY, "true");
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(ownHeaders());
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		org.w3c.dom.Document doc = w3cDomDocumentFromByteArray(baos.toByteArray());

		// S2: its own header, and its first-page header on a page its first line opens
		assertEquals("xsl-region-before-default-p2", regionBefore(doc, "s1-p2-default"));
		assertEquals("xsl-region-before-firstpage-p2", regionBefore(doc, "s1-p2-firstpage"));
		boolean first = false;
		NodeList refs = named(doc, "page-sequence-master", "s1-p2").getElementsByTagNameNS(FO, "conditional-page-master-reference");
		for (int i = 0; i < refs.getLength(); i++) {
			Element ref = (Element) refs.item(i);
			if ("first".equals(ref.getAttribute("page-position"))) {
				first = true;
				assertEquals("s1-p2-firstpage", ref.getAttribute("master-reference"));
			}
		}
		assertTrue("S2's first-page master, for a page its first line opens", first);
		assertEquals("headS2", staticContent(doc, "xsl-region-before-default-p2"));
		assertEquals("firstS2", staticContent(doc, "xsl-region-before-firstpage-p2"));

		// S3: S2's default header by inheritance, no first page of its own
		assertEquals("xsl-region-before-default-p3", regionBefore(doc, "s1-p3-default"));
		assertTrue("S3 has no first-page master", named(doc, "simple-page-master", "s1-p3-firstpage") == null);
		assertEquals("headS2", staticContent(doc, "xsl-region-before-default-p3"));

		// the page-sequence's own, as before
		assertEquals("headS1", staticContent(doc, "xsl-region-before-default"));

		List<Element> marks = marked(doc);
		assertEquals(2, marks.size());
		assertEquals("s1-p2", marks.get(0).getAttributeNS(FOX, "page-sequence-master-reference"));
		assertEquals("S2 text", marks.get(0).getTextContent().trim());
		assertEquals("s1-p3", marks.get(1).getAttributeNS(FOX, "page-sequence-master-reference"));
		assertEquals("S3 text", marks.get(1).getTextContent().trim());
	}

	@Test
	public void eachPartsOwnHeaders() throws Exception {
		checkOwnHeaders(Docx4J.FLAG_NONE);
		checkOwnHeaders(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/**
	 * A merged part restarting its page numbers (CR-031 phase 3, fork CR-017.2): S2 restarts at 1
	 * and S3 at 4, neither changing margins or headers.  Each still gets a marker - naming the
	 * masters in force, s1-p1 - which carries fox:page-number-restart, and
	 * fox:page-number-restart-parity="keep" where the document has odd and even headers (Word
	 * keeps a folio's parity the page's there: probes P6, P7).  The page-sequence's own
	 * initial-page-number is then its first part's restart, of which S1 has none; without the
	 * renderer's restart it is, as before, the first restart any part declares.
	 */
	private WordprocessingMLPackage restarts(boolean evenOdd) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String h1 = header(pkg, "headS1");
		String mar = A4 + pgMar(1440, 1440, 720, 720);
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + " " + R + "><w:body>"
				+ "<w:p><w:pPr><w:sectPr><w:headerReference w:type=\"default\" r:id=\"" + h1 + "\"/>" + mar
				+ "</w:sectPr></w:pPr><w:r><w:t>S1 text</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:sectPr><w:type w:val=\"continuous\"/>" + mar + "<w:pgNumType w:start=\"1\"/>"
				+ "</w:sectPr></w:pPr><w:r><w:t>S2 text</w:t></w:r></w:p>"
				+ para("S3 text")
				+ "<w:sectPr><w:type w:val=\"continuous\"/>" + mar + "<w:pgNumType w:start=\"4\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		if (evenOdd) {
			pkg.getMainDocumentPart().getDocumentSettingsPart(true).getJaxbElement()
					.setEvenAndOddHeaders(new org.docx4j.wml.BooleanDefaultTrue());
		}
		return pkg;
	}

	private org.w3c.dom.Document restartsFo(boolean evenOdd, String property, int flags) throws Exception {
		Docx4jProperties.setProperty(PROPERTY, property);
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(restarts(evenOdd));
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	private void checkRestarts(int flags) throws Exception {
		for (boolean evenOdd : new boolean[] { false, true }) {
			org.w3c.dom.Document doc = restartsFo(evenOdd, "true", flags);
			Element seq = (Element) doc.getElementsByTagNameNS(FO, "page-sequence").item(0);
			assertEquals("S1 does not restart, so the page-sequence does not", "", seq.getAttribute("initial-page-number"));
			List<Element> marks = marked(doc);
			assertEquals(2, marks.size());
			String[][] expected = { { "S2 text", "1" }, { "S3 text", "4" } };
			for (int i = 0; i < 2; i++) {
				assertEquals(expected[i][0], marks.get(i).getTextContent().trim());
				assertEquals("the masters in force", "s1-p1", marks.get(i).getAttributeNS(FOX, "page-sequence-master-reference"));
				assertEquals(expected[i][1], marks.get(i).getAttributeNS(FOX, "page-number-restart"));
				assertEquals(evenOdd ? "keep" : "", marks.get(i).getAttributeNS(FOX, "page-number-restart-parity"));
			}
			assertFalse(anyStamp(doc));
			for (Element m : marks) assertFalse(m.hasAttribute("docx4j-restart"));
		}
		org.w3c.dom.Document off = restartsFo(false, "false", flags);
		assertEquals("without the renderer's restart: the first restart any part declares, as before", "1",
				((Element) off.getElementsByTagNameNS(FO, "page-sequence").item(0)).getAttribute("initial-page-number"));
		assertEquals(0, marked(off).size());
	}

	@Test
	public void aRestartingPartCarriesItsRestart() throws Exception {
		checkRestarts(Docx4J.FLAG_NONE);
		checkRestarts(Docx4J.FLAG_EXPORT_PREFER_XSL);
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
