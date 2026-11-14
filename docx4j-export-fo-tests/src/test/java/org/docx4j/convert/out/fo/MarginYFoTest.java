/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 */
package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.After;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A shape positioned relative to the margin carries {@code docx4j:margin-y} and {@code docx4j:margin-box} for the
 * renderer fork (CR-083 phase 4b; fop/CR-045): which edge of the margin box its written top was aligned to, and that box,
 * the stated margins, which the renderer shifts the top by as far as that edge of the body as laid out is from the box's,
 * as Word measures it (cr083-phase4b-margin, CR-083-notes/phase4-findings.md
 * section 11). The top itself is the stated margins' as before; without the capability nothing is written. The
 * capability is the fork's, absent on this classpath, so it is switched through {@code WordLayoutFixups.marginYForTest}.
 */
public class MarginYFoTest {

	private static final String NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
			+ "xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" "
			+ "xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" "
			+ "xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" "
			+ "xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final String DOCX4J = "http://docx4j.org/fop/word-layout";
	private static final int[] ROUTES = { Docx4J.FLAG_EXPORT_PREFER_XSL, Docx4J.FLAG_EXPORT_PREFER_NONXSL };

	@After
	public void reset() {
		WordLayoutFixups.marginYForTest = null;
	}

	/** A VML text box 200 by 50 pt, centred across the margin, positioned down it by {@code v} ("center", "top",
	 *  "bottom", or null for margin-top 100pt) relative to {@code rel} */
	private static String vml(String v, String rel) {
		return "<w:r><w:pict><v:shape id=\"b1\" style=\"position:absolute;margin-left:0;margin-top:" + (v == null ? "100pt" : "0")
				+ ";width:200pt;height:50pt;z-index:1;mso-position-horizontal:center;mso-position-horizontal-relative:margin;"
				+ (v == null ? "" : "mso-position-vertical:" + v + ";") + "mso-position-vertical-relative:" + rel + "\">"
				+ "<v:textbox><w:txbxContent><w:p><w:r><w:t>Box</w:t></w:r></w:p></w:txbxContent></v:textbox></v:shape>"
				+ "</w:pict></w:r>";
	}

	/** A DrawingML rectangle 300 by 80 pt behind the text, centred across the margin, aligned {@code v} down it (null:
	 *  posOffset 100pt) relative to {@code rel} */
	private static String dml(String v, String rel) {
		String pos = v == null ? "<wp:posOffset>1270000</wp:posOffset>" : "<wp:align>" + v + "</wp:align>";
		return "<w:r><w:drawing><wp:anchor distT=\"0\" distB=\"0\" distL=\"114300\" distR=\"114300\" simplePos=\"0\" "
				+ "relativeHeight=\"251659264\" behindDoc=\"1\" locked=\"0\" layoutInCell=\"1\" allowOverlap=\"1\">"
				+ "<wp:simplePos x=\"0\" y=\"0\"/><wp:positionH relativeFrom=\"margin\"><wp:align>center</wp:align></wp:positionH>"
				+ "<wp:positionV relativeFrom=\"" + rel + "\">" + pos + "</wp:positionV><wp:extent cx=\"3810000\" cy=\"1016000\"/>"
				+ "<wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/><wp:wrapNone/><wp:docPr id=\"1\" name=\"Rectangle 1\"/>"
				+ "<wp:cNvGraphicFramePr/><a:graphic><a:graphicData uri=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\">"
				+ "<wps:wsp><wps:cNvSpPr/><wps:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"3810000\" cy=\"1016000\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom><a:solidFill><a:srgbClr val=\"BDD7EE\"/></a:solidFill>"
				+ "<a:ln><a:noFill/></a:ln></wps:spPr><wps:bodyPr/></wps:wsp></a:graphicData></a:graphic></wp:anchor></w:drawing></w:r>";
	}

	/** The FO of a Letter page with 72 pt margins whose one paragraph holds {@code run} */
	private static String fo(String run, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + NS + "><w:body><w:p>" + run + "<w:r><w:t>Text.</w:t></w:r></w:p>"
				+ "<w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"708\" w:footer=\"708\"/>"
				+ "</w:sectPr></w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return baos.toString("UTF-8");
	}

	/** The page-positioned container: the one block-container with absolute-position fixed */
	private static Element fixed(String fo) throws Exception {
		org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader(fo)));
		NodeList list = doc.getElementsByTagNameNS(FO, "block-container");
		Element found = null;
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			if ("fixed".equals(e.getAttribute("absolute-position"))) {
				assertEquals("one fixed container\n" + fo, null, found);
				found = e;
			}
		}
		assertNotNull("a fixed container\n" + fo, found);
		return found;
	}

	private static void check(String run, String expected) throws Exception {
		for (int route : ROUTES) {
			WordLayoutFixups.marginYForTest = Boolean.TRUE;
			String with = fo(run, route);
			Element box = fixed(with);
			assertEquals("route " + route + "\n" + with, expected, box.getAttributeNS(DOCX4J, "margin-y"));
			// the box the top was aligned in: the stated margins, 72 pt top and bottom on a Letter page
			String marginBox = box.getAttributeNS(DOCX4J, "margin-box");
			if (expected.length() == 0) {
				assertEquals("route " + route, "", marginBox);
			} else {
				String[] tb = marginBox.trim().split("\\s+");
				assertEquals("route " + route + ": " + marginBox, 2, tb.length);
				assertEquals(72, Double.parseDouble(tb[0]), 0.005);
				assertEquals(792 - 72, Double.parseDouble(tb[1]), 0.005);
			}
			assertFalse(with, with.contains(WordLayoutFixups.HINT_ANCHOR_MARGIN_Y));
			WordLayoutFixups.marginYForTest = Boolean.FALSE;
			String without = fo(run, route);
			Element plain = fixed(without);
			assertEquals("route " + route + ": none without the capability", "", plain.getAttributeNS(DOCX4J, "margin-y"));
			assertEquals("route " + route + ": none without the capability", "", plain.getAttributeNS(DOCX4J, "margin-box"));
			assertFalse(without, without.contains(WordLayoutFixups.HINT_ANCHOR_MARGIN_Y));
			// the written top is the stated margins' either way
			assertEquals("route " + route, plain.getAttribute("top"), box.getAttribute("top"));
		}
	}

	/** VML: centred, at the top, at the bottom and an offset relative to the margin (wv51-01, -03, -05, -07) */
	@Test
	public void vmlRelativeToTheMargin() throws Exception {
		WordLayoutOn.assume();
		check(vml("center", "margin"), "center");
		check(vml("top", "margin"), "top");
		check(vml("bottom", "margin"), "bottom");
		check(vml(null, "margin"), "top");
	}

	/** DrawingML: the same (wv51-16, -18, -19, -20) */
	@Test
	public void drawingMLRelativeToTheMargin() throws Exception {
		WordLayoutOn.assume();
		check(dml("center", "margin"), "center");
		check(dml("top", "margin"), "top");
		check(dml("bottom", "margin"), "bottom");
		check(dml(null, "margin"), "top");
	}

	/** Relative to the page Word doesn't move the shape (wv51-09, -22): no hint */
	@Test
	public void relativeToThePageIsLeft() throws Exception {
		WordLayoutOn.assume();
		check(vml("center", "page"), "");
		check(dml("center", "page"), "");
	}
}
