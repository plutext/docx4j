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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Document;
import org.docx4j.wml.FooterReference;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.SectPr;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The running header and footer of a page-sequence built of merged continuous sections.
 *
 * <p>The masters are built on whichever part the <b>columns</b> need
 * ({@link ContinuousSectionColumnsTest}), and every body part carries its own margin
 * difference on its blocks - but the header and footer are regions of the page master, and
 * no block's indent reaches them. Word draws each page's header and footer at the margins
 * of the section which owns that page; a page-sequence has one set of static content, so it
 * takes the <b>first</b> part's margins, which is the section Word starts the page with and
 * the one that owns most of the pages.
 *
 * <p>Measured on a corpus document of five continuous sections whose right margins are 476,
 * 386 and 1134 twips (CR-001 batch 44): Word's page number ends at x=571.93 on the five
 * pages the first section owns, 576.49 on the two the 386-twip two-column section owns and
 * 539.05 on the last. The masters are built on the two-column part, so ours ended at 576.00
 * on every page; with the header and footer taking the first part's margins it is 571.50,
 * and the document's five first-section pages are Word's.
 *
 * @since 17.2.0
 */
public class ContinuousSectionHeaderFooterMarginTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"" + Namespaces.NS_WORD12 + "\"";

	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	/** US Letter, 612 x 792pt */
	private static final String PG_SZ = "<w:pgSz w:w=\"12240\" w:h=\"15840\"/>";

	/** Section 1: one column, 72pt margins. Section 2: two columns, 85.05pt - the
	 *  narrower text column, so the masters take it (batch 43's M53). */
	private static final int FIRST_MARGIN_TWIPS = 1440;
	private static final int TWO_COLUMN_MARGIN_TWIPS = 1701;

	private static WordprocessingMLPackage pkg() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t>Section one, one column.</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:sectPr><w:type w:val=\"continuous\"/>" + PG_SZ
				+ "<w:pgMar w:top=\"1440\" w:right=\"" + FIRST_MARGIN_TWIPS + "\" w:bottom=\"1440\""
				+ " w:left=\"" + FIRST_MARGIN_TWIPS + "\" w:header=\"709\" w:footer=\"709\"/>"
				+ "</w:sectPr></w:pPr></w:p>"
				+ "<w:p><w:r><w:t>Section two, two columns.</w:t></w:r></w:p>"
				+ "<w:sectPr><w:type w:val=\"continuous\"/>"
				+ "<w:cols w:num=\"2\" w:space=\"720\"/>" + PG_SZ
				+ "<w:pgMar w:top=\"1440\" w:right=\"" + TWO_COLUMN_MARGIN_TWIPS + "\" w:bottom=\"1440\""
				+ " w:left=\"" + TWO_COLUMN_MARGIN_TWIPS + "\" w:header=\"709\" w:footer=\"709\"/>"
				+ "</w:sectPr></w:body></w:document>"));

		// a right-aligned page number, as a running footer carries, on both parts
		FooterPart fp = new FooterPart(new PartName("/word/footer1.xml"));
		fp.setPackage(pkg);
		fp.setJaxbElement((Ftr) XmlUtils.unmarshalString(
				"<w:ftr " + W + "><w:p><w:pPr><w:jc w:val=\"right\"/></w:pPr>"
				+ "<w:r><w:t>1/1</w:t></w:r></w:p></w:ftr>", Context.jc, Ftr.class));
		Relationship rel = pkg.getMainDocumentPart().addTargetPart(fp);
		for (SectPr sectPr : sectPrs(pkg)) {
			FooterReference ref = Context.getWmlObjectFactory().createFooterReference();
			ref.setId(rel.getId());
			ref.setType(HdrFtrRef.DEFAULT);
			sectPr.getEGHdrFtrReferences().add(ref);
		}
		return pkg;
	}

	/** Every w:sectPr of the document, in order. */
	private static java.util.List<SectPr> sectPrs(WordprocessingMLPackage pkg) {
		java.util.List<SectPr> out = new java.util.ArrayList<SectPr>();
		for (Object o : pkg.getMainDocumentPart().getContent()) {
			o = XmlUtils.unwrap(o);
			if (o instanceof org.docx4j.wml.P && ((org.docx4j.wml.P) o).getPPr() != null
					&& ((org.docx4j.wml.P) o).getPPr().getSectPr() != null) {
				out.add(((org.docx4j.wml.P) o).getPPr().getSectPr());
			}
		}
		out.add(pkg.getMainDocumentPart().getJaxbElement().getBody().getSectPr());
		return out;
	}

	private static org.w3c.dom.Document fo(int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg());
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	private static double pt(String v) {
		if (v == null || v.length() == 0) return 0;
		if (v.endsWith("pt")) return Double.parseDouble(v.substring(0, v.length() - 2));
		if (v.endsWith("in")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72d;
		if (v.endsWith("mm")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72d / 25.4d;
		throw new IllegalArgumentException(v);
	}

	/** The end-indent of the first block of the region-after's static content. */
	private static double footerEndIndentPt(org.w3c.dom.Document doc) {
		NodeList contents = doc.getElementsByTagNameNS(FO_NS, "static-content");
		for (int i = 0; i < contents.getLength(); i++) {
			Element sc = (Element) contents.item(i);
			if (!sc.getAttribute("flow-name").startsWith("xsl-region-after")) continue;
			NodeList blocks = sc.getChildNodes();
			for (int j = 0; j < blocks.getLength(); j++) {
				if (blocks.item(j) instanceof Element) {
					return pt(((Element) blocks.item(j)).getAttribute("end-indent"));
				}
			}
		}
		return Double.NaN;
	}

	/**
	 * The footer takes the first part's right margin: the masters carry the two-column
	 * part's 85.05pt, so the footer's own block is indented by 72 - 85.05 = -13.05pt,
	 * which puts its right edge back at the 72pt margin Word draws it at.
	 */
	private void checkFooterIndent(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(flags);
		assertEquals("the masters are the two-column part's", 85.05,
				pt(((Element) doc.getElementsByTagNameNS(FO_NS, "simple-page-master").item(0))
						.getAttribute("margin-right")), 0.05);
		assertEquals("so the footer is indented back to the first part's 72pt margin",
				-13.05, footerEndIndentPt(doc), 0.05);
		// and the indent is not left on the region, where FOP ignores it
		NodeList regions = doc.getElementsByTagNameNS(FO_NS, "region-after");
		assertTrue("no region-after", regions.getLength() > 0);
		for (int i = 0; i < regions.getLength(); i++) {
			assertEquals("the indent is moved off the region", "",
					((Element) regions.item(i)).getAttribute("end-indent"));
		}
	}

	@Test
	public void footerTakesTheFirstPartsMarginVisitorPathway() throws Exception {
		checkFooterIndent(Docx4J.FLAG_NONE);
	}

	@Test
	public void footerTakesTheFirstPartsMarginXsltPathway() throws Exception {
		checkFooterIndent(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/**
	 * And it is the same indent the first part's own blocks carry - which is the whole of
	 * the rule: the header and footer go where that part's body goes, because Word draws
	 * them at the margins of the section that owns the page.
	 */
	private void checkFooterMatchesTheFirstPart(int flags) throws Exception {
		org.w3c.dom.Document doc = fo(flags);
		assertEquals("the footer takes the indent the first part's own content carries",
				firstFlowBlockEndIndentPt(doc), footerEndIndentPt(doc), 0.01);
	}

	/** The end-indent of the first part's content: the flow's first block, or the first
	 *  block inside it where the part is wrapped to span the columns. */
	private static double firstFlowBlockEndIndentPt(org.w3c.dom.Document doc) {
		NodeList flows = doc.getElementsByTagNameNS(FO_NS, "flow");
		assertTrue("no flow", flows.getLength() > 0);
		for (org.w3c.dom.Node n = flows.item(0).getFirstChild(); n != null; n = n.getNextSibling()) {
			if (!(n instanceof Element)) continue;
			Element block = (Element) n;
			if (!block.getAttribute("end-indent").isEmpty()) return pt(block.getAttribute("end-indent"));
			for (org.w3c.dom.Node c = block.getFirstChild(); c != null; c = c.getNextSibling()) {
				if (c instanceof Element && !((Element) c).getAttribute("end-indent").isEmpty()) {
					return pt(((Element) c).getAttribute("end-indent"));
				}
			}
		}
		return Double.NaN;
	}

	@Test
	public void footerMatchesTheFirstPartVisitorPathway() throws Exception {
		checkFooterMatchesTheFirstPart(Docx4J.FLAG_NONE);
	}

	@Test
	public void footerMatchesTheFirstPartXsltPathway() throws Exception {
		checkFooterMatchesTheFirstPart(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
