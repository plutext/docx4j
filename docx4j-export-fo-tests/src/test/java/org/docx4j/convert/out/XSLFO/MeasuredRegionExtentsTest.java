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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.wml.Document;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.junit.After;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Where the FO renderer measures header and footer extents itself (fork CR-018,
 * {@code fox:extent="measured"}), docx4j asks it to for each region which reserves space and
 * skips the extent pre-pass (CR-031 phase 5).  The page master's margin is then the header
 * (footer) distance and the body's what the top (bottom) margin adds to it, so that the
 * renderer's rule - the body's margin the larger of its stated margin and the region's height -
 * is Word's: the body starts at max(top margin, header distance + header height).  Property
 * {@code docx4j.convert.out.fo.measuredRegionExtents} ({@code true} asks whatever the renderer,
 * as here; {@code false} never; by default where the renderer has
 * {@code measured-region-extents}).
 *
 * @since 17.3.1
 */
public class MeasuredRegionExtentsTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String R = "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final String FOX = "http://xmlgraphics.apache.org/fop/extensions";
	private static final String PROPERTY = "docx4j.convert.out.fo.measuredRegionExtents";

	private static final String HEADING1 = "<w:p><w:pPr><w:pStyle w:val=\"Heading1\"/></w:pPr><w:r><w:t>Scope</w:t></w:r></w:p>";

	@After
	public void resetProperty() {
		Docx4jProperties.getProperties().remove(PROPERTY);
	}

	private static String header(WordprocessingMLPackage pkg, String paragraphs) throws Exception {
		HeaderPart hp = new HeaderPart(new PartName("/word/header1.xml"));
		hp.setPackage(pkg);
		hp.setJaxbElement((Hdr) XmlUtils.unmarshalString("<w:hdr " + W + ">" + paragraphs + "</w:hdr>",
				org.docx4j.jaxb.Context.jc, Hdr.class));
		return "<w:headerReference w:type=\"default\" r:id=\"" + pkg.getMainDocumentPart().addTargetPart(hp).getId() + "\"/>";
	}

	private static String footer(WordprocessingMLPackage pkg, String paragraphs) throws Exception {
		FooterPart fp = new FooterPart(new PartName("/word/footer1.xml"));
		fp.setPackage(pkg);
		fp.setJaxbElement((Ftr) XmlUtils.unmarshalString("<w:ftr " + W + ">" + paragraphs + "</w:ftr>",
				org.docx4j.jaxb.Context.jc, Ftr.class));
		return "<w:footerReference w:type=\"default\" r:id=\"" + pkg.getMainDocumentPart().addTargetPart(fp).getId() + "\"/>";
	}

	/** One section, A4, top and bottom margins as given, 36pt header and footer distances. */
	private static WordprocessingMLPackage pkg(String headerParagraphs, String footerParagraphs, int top, int bottom)
			throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String refs = (headerParagraphs == null ? "" : header(pkg, headerParagraphs))
				+ (footerParagraphs == null ? "" : footer(pkg, footerParagraphs));
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + " " + R + "><w:body>"
				+ HEADING1 + "<w:p><w:r><w:t>body</w:t></w:r></w:p>"
				+ "<w:sectPr>" + refs + "<w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"" + top + "\" w:right=\"1440\" w:bottom=\"" + bottom + "\" w:left=\"1440\""
				+ " w:header=\"720\" w:footer=\"720\" w:gutter=\"0\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		return pkg;
	}

	private static String para(String text) {
		return "<w:p><w:r><w:t xml:space=\"preserve\">" + text + "</w:t></w:r></w:p>";
	}

	private static String styleRef(String before) {
		return "<w:p>" + (before == null ? "" : "<w:r><w:t xml:space=\"preserve\">" + before + "</w:t></w:r>")
				+ "<w:fldSimple w:instr=\" STYLEREF &quot;Heading 1&quot; \"><w:r><w:t>Introduction</w:t></w:r></w:fldSimple></w:p>";
	}

	private org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	private static Element master(org.w3c.dom.Document doc, String masterName) {
		NodeList nl = doc.getElementsByTagNameNS(FO, "simple-page-master");
		for (int i = 0; i < nl.getLength(); i++) {
			if (masterName.equals(((Element) nl.item(i)).getAttribute("master-name"))) return (Element) nl.item(i);
		}
		return null;
	}

	private static Element child(Element parent, String localName) {
		NodeList nl = parent.getElementsByTagNameNS(FO, localName);
		return nl.getLength() == 0 ? null : (Element) nl.item(0);
	}

	private static boolean measured(Element region) {
		return region != null && "measured".equals(region.getAttributeNS(FOX, "extent"));
	}

	private static int countMeasured(org.w3c.dom.Document doc) {
		int n = 0;
		for (String side : new String[] { "region-before", "region-after" }) {
			NodeList nl = doc.getElementsByTagNameNS(FO, side);
			for (int i = 0; i < nl.getLength(); i++) {
				if (measured((Element) nl.item(i))) n++;
			}
		}
		return n;
	}

	private static float pt(String length) {
		if (length == null || length.isEmpty()) return 0f;
		if (length.endsWith("pt")) return Float.parseFloat(length.substring(0, length.length() - 2));
		if (length.endsWith("in")) return 72f * Float.parseFloat(length.substring(0, length.length() - 2));
		if (length.endsWith("mm")) return 72f / 25.4f * Float.parseFloat(length.substring(0, length.length() - 2));
		throw new IllegalArgumentException(length);
	}

	private void checkHeaderAndFooter(int flags) throws Exception {
		Docx4jProperties.setProperty(PROPERTY, "true");
		org.w3c.dom.Document doc = fo(pkg(para("Running head"), para("Footer"), 1440, 1440), flags);
		Element spm = master(doc, "s1-default");
		assertNotNull("the section's master", spm);
		Element body = child(spm, "region-body");
		assertTrue("the header asked to be measured", measured(child(spm, "region-before")));
		assertTrue("the footer asked to be measured", measured(child(spm, "region-after")));
		assertEquals("the page master's top at the header distance", 36f, pt(spm.getAttribute("margin-top")), 0.01f);
		assertEquals("the body's stated top at the top margin", 72f,
				pt(spm.getAttribute("margin-top")) + pt(body.getAttribute("margin-top")), 0.01f);
		assertEquals("the page master's bottom at the footer distance", 36f, pt(spm.getAttribute("margin-bottom")), 0.01f);
		assertEquals("the body's stated bottom at the bottom margin", 72f,
				pt(spm.getAttribute("margin-bottom")) + pt(body.getAttribute("margin-bottom")), 0.01f);
	}

	@Test
	public void headerAndFooterAreMeasuredByTheRenderer() throws Exception {
		checkHeaderAndFooter(Docx4J.FLAG_NONE);
		checkHeaderAndFooter(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** Off, the pre-pass sizes the regions as before and nothing is asked. */
	@Test
	public void offNothingIsAsked() throws Exception {
		Docx4jProperties.setProperty(PROPERTY, "false");
		org.w3c.dom.Document doc = fo(pkg(para("Running head"), para("Footer"), 1440, 1440), Docx4J.FLAG_NONE);
		assertEquals(0, countMeasured(doc));
	}

	/** An empty header part reserves nothing (HeaderFooterPolicy.reservesNothing), so it is not
	 *  asked and the body starts at the top margin; an empty footer part is asked, its empty line
	 *  being what Word reserves above the footer distance. */
	@Test
	public void anEmptyHeaderIsNotAskedAnEmptyFooterIs() throws Exception {
		Docx4jProperties.setProperty(PROPERTY, "true");
		org.w3c.dom.Document doc = fo(pkg("<w:p/>", "<w:p/>", 1440, 1440), Docx4J.FLAG_NONE);
		Element spm = master(doc, "s1-default");
		Element body = child(spm, "region-body");
		assertTrue("an empty header is not measured", !measured(child(spm, "region-before")));
		assertEquals("the body at the top margin", 72f,
				pt(spm.getAttribute("margin-top")) + pt(body.getAttribute("margin-top")), 0.01f);
		assertTrue("an empty footer part is measured", measured(child(spm, "region-after")));
	}

	/** A header painting over a negative top margin needs its extent from the pre-pass, which
	 *  then sizes every region: nothing is asked. */
	@Test
	public void aNegativeTopMarginUnderAHeaderKeepsThePrePass() throws Exception {
		Docx4jProperties.setProperty(PROPERTY, "true");
		org.w3c.dom.Document doc = fo(pkg(para("Running head"), para("Footer"), -312, 1440), Docx4J.FLAG_NONE);
		assertEquals(0, countMeasured(doc));
	}

	/** The mirrored even-page master has the same static content, so it is asked too. */
	@Test
	public void theMirroredMasterIsAskedToo() throws Exception {
		Docx4jProperties.setProperty(PROPERTY, "true");
		WordprocessingMLPackage pkg = pkg(para("Running head"), para("Footer"), 1440, 1440);
		pkg.getMainDocumentPart().getDocumentSettingsPart().getJaxbElement()
				.setMirrorMargins(new org.docx4j.wml.BooleanDefaultTrue());
		org.w3c.dom.Document doc = fo(pkg, Docx4J.FLAG_NONE);
		Element mirror = master(doc, "s1-default-mirrored");
		assertNotNull("a mirrored master", mirror);
		assertTrue(measured(child(mirror, "region-before")));
		assertTrue(measured(child(mirror, "region-after")));
	}

	/** A header whose one paragraph holds this paragraph content. */
	private static WordprocessingMLPackage headerPkg(String paragraphContent) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		HeaderPart hp = new HeaderPart(new PartName("/word/header1.xml"));
		hp.setPackage(pkg);
		String hdrRel = pkg.getMainDocumentPart().addTargetPart(hp).getId();
		org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage img =
				org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.createImagePart(pkg, hp, png());
		hp.setJaxbElement((Hdr) XmlUtils.unmarshalString("<w:hdr " + W + "><w:p>"
				+ paragraphContent.replace("RELID", img.getSourceRelationship().getId()) + "</w:p></w:hdr>",
				org.docx4j.jaxb.Context.jc, Hdr.class));
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + " " + R + "><w:body>" + para("body")
				+ "<w:sectPr><w:headerReference w:type=\"default\" r:id=\"" + hdrRel + "\"/>"
				+ "<w:pgSz w:w=\"11906\" w:h=\"16838\"/><w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\""
				+ " w:left=\"1440\" w:header=\"720\" w:footer=\"720\" w:gutter=\"0\"/></w:sectPr></w:body></w:document>"));
		return pkg;
	}

	/** The block-containers of the header's static content. */
	private static List<Element> headerContainers(org.w3c.dom.Document doc) {
		List<Element> out = new ArrayList<Element>();
		NodeList scs = doc.getElementsByTagNameNS(FO, "static-content");
		for (int i = 0; i < scs.getLength(); i++) {
			Element sc = (Element) scs.item(i);
			if (!sc.getAttribute("flow-name").startsWith("xsl-region-before")) continue;
			NodeList bcs = sc.getElementsByTagNameNS(FO, "block-container");
			for (int k = 0; k < bcs.getLength(); k++) out.add((Element) bcs.item(k));
		}
		return out;
	}

	/** A header paragraph whose whole content is a floating picture takes no space for it, as
	 *  Word sizes the region and places the text after it (corpus document 17: a 54pt
	 *  wrapTopAndBottom logo, Word's header text beside it and its body clear of the text alone).
	 *  It is drawn as a no-wrap picture is, absolutely positioned in a zero-height container, so a
	 *  renderer measuring the real header measures what the pre-pass measures, and the region is
	 *  asked to be measured (12502's first-page header, a full-page picture, measured 857pt while
	 *  the picture reserved its height). */
	private void checkAnchorOnly(int flags) throws Exception {
		Docx4jProperties.setProperty(PROPERTY, "true");
		org.w3c.dom.Document doc = fo(headerPkg(anchoredPicture("RELID", "<wp:wrapTopAndBottom/>")), flags);
		List<Element> containers = headerContainers(doc);
		assertEquals("a zero-height container and the positioned one in it", 2, containers.size());
		assertEquals("0pt", containers.get(0).getAttribute("height"));
		assertEquals("absolute", containers.get(1).getAttribute("absolute-position"));
		assertTrue("the header asked to be measured", measured(child(master(doc, "s1-default"), "region-before")));
	}

	@Test
	public void anAnchorOnlyHeaderPictureTakesNoSpace() throws Exception {
		checkAnchorOnly(Docx4J.FLAG_NONE);
		checkAnchorOnly(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** Beside text in its paragraph the picture keeps its height: the pre-pass measures it there
	 *  too, and Word reserves for it. */
	@Test
	public void aHeaderPictureBesideTextKeepsItsHeight() throws Exception {
		Docx4jProperties.setProperty(PROPERTY, "true");
		org.w3c.dom.Document doc = fo(headerPkg(anchoredPicture("RELID", "<wp:wrapTopAndBottom/>")
				+ "<w:r><w:t>Running head</w:t></w:r>"), Docx4J.FLAG_NONE);
		List<Element> containers = headerContainers(doc);
		assertEquals("one container, reserving the picture's height", 1, containers.size());
		assertTrue(pt(containers.get(0).getAttribute("height")) > 70f);
	}

	private static byte[] png() throws Exception {
		java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(80, 60, java.awt.image.BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		javax.imageio.ImageIO.write(image, "png", out);
		return out.toByteArray();
	}

	private static String anchoredPicture(String relId, String wrap) {
		return "<w:r><w:drawing xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
				+ "<wp:anchor distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\" simplePos=\"0\" relativeHeight=\"251658240\""
				+ " behindDoc=\"0\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\"><wp:simplePos x=\"0\" y=\"0\"/>"
				+ "<wp:positionH relativeFrom=\"column\"><wp:posOffset>0</wp:posOffset></wp:positionH>"
				+ "<wp:positionV relativeFrom=\"paragraph\"><wp:posOffset>0</wp:posOffset></wp:positionV>"
				+ "<wp:extent cx=\"1270000\" cy=\"952500\"/>" + wrap + "<wp:docPr id=\"1\" name=\"logo\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
				+ "<pic:pic><pic:nvPicPr><pic:cNvPr id=\"101\" name=\"logo\"/><pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill><a:blip " + R + " r:embed=\"" + relId + "\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"1270000\" cy=\"952500\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>"
				+ "</a:graphicData></a:graphic></wp:anchor></w:drawing></w:r>";
	}

	/** A region not asked to be measured keeps a placeholder extent of half the page; a footer's
	 *  content goes to its foot, where the pre-pass's exact extent put it, not half way up the
	 *  page (an empty paragraph paints nothing, but its preserved space is still text).  Here
	 *  the first-page footer w:titlePg asks for, which docx4j invents. */
	@Test
	public void anUnaskedFooterSitsAtItsFoot() throws Exception {
		Docx4jProperties.setProperty(PROPERTY, "true");
		WordprocessingMLPackage pkg = pkg(para("Running head"), para("Footer"), 1440, 1440);
		org.docx4j.wml.SectPr sectPr = pkg.getMainDocumentPart().getJaxbElement().getBody().getSectPr();
		sectPr.setTitlePg(new org.docx4j.wml.BooleanDefaultTrue());
		Element first = master(fo(pkg, Docx4J.FLAG_NONE), "s1-firstpage");
		assertNotNull("the first-page master", first);
		Element after = child(first, "region-after");
		assertTrue("the invented footer is not measured", !measured(after));
		assertEquals("after", after.getAttribute("display-align"));
	}

	private static NodeList leaders(org.w3c.dom.Document doc) {
		Element staticContent = null;
		NodeList nl = doc.getElementsByTagNameNS(FO, "static-content");
		for (int i = 0; i < nl.getLength(); i++) {
			Element sc = (Element) nl.item(i);
			if (sc.getElementsByTagNameNS(FO, "retrieve-marker").getLength() > 0) staticContent = sc;
		}
		assertNotNull("the header holding the STYLEREF", staticContent);
		return staticContent.getElementsByTagNameNS(FO, "leader");
	}

	/** A header line holding only a STYLEREF measures with marker retrieval off, so it gets a
	 *  zero-length leader as a strut; a line with text of its own does not need one. */
	private void checkStrut(int flags) throws Exception {
		Docx4jProperties.setProperty(PROPERTY, "true");
		NodeList struts = leaders(fo(pkg(styleRef(null), null, 1440, 1440), flags));
		assertEquals("a strut beside the retrieve-marker", 1, struts.getLength());
		Element strut = (Element) struts.item(0);
		assertEquals("0pt", strut.getAttribute("leader-length"));
		assertEquals("retrieve-marker", ((Element) strut.getPreviousSibling()).getLocalName());

		assertEquals("text of its own gives the line", 0,
				leaders(fo(pkg(styleRef("Chapter: "), null, 1440, 1440), flags)).getLength());

		Docx4jProperties.setProperty(PROPERTY, "false");
		assertEquals("no strut where the pre-pass measures", 0,
				leaders(fo(pkg(styleRef(null), null, 1440, 1440), flags)).getLength());
	}

	@Test
	public void aStyleRefOnlyLineGetsAStrut() throws Exception {
		checkStrut(Docx4J.FLAG_NONE);
		checkStrut(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
