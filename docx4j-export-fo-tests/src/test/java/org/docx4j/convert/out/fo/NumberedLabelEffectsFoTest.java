/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 */
package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.model.shapes.TextEffects;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.Document;
import org.docx4j.wml.Numbering;
import org.junit.After;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A numbered paragraph's label painted with its paragraph mark's Word 2010 text effects (CR-083 phase 5, step 2;
 * phase 5 findings section 6.2, wb82-30 and -31): in both FO routes the number goes in an {@code fo:inline} carrying
 * the effect's id, the mark's room above rides on the label's ascent and none below, and without the capability, or
 * without an effect, the FO is what it was. The capability is the renderer fork's, absent on this classpath, so it is
 * switched through {@code TextEffectsFo.enabledForTest}; the painting is chart-render's TextEffectBodyTest.
 */
public class NumberedLabelEffectsFoTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
			+ "xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final String DOCX4J = "http://docx4j.org/fop/word-layout";

	private static final String NUMBERING = "<w:numbering " + W + ">"
			+ "<w:abstractNum w:abstractNumId=\"0\"><w:multiLevelType w:val=\"singleLevel\"/>"
			+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>"
			+ "<w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>"
			+ "<w:pPr><w:ind w:left=\"720\" w:hanging=\"720\"/></w:pPr></w:lvl>"
			+ "</w:abstractNum>"
			+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num></w:numbering>";

	/** wb82-30's: a glow of 8 pt, ED7D31 at 40% */
	private static final String GLOW8 = "<w14:glow w14:rad=\"101600\"><w14:srgbClr w14:val=\"ED7D31\">"
			+ "<w14:alpha w14:val=\"60000\"/></w14:srgbClr></w14:glow>";

	/** wb82-31's: a gradient red to blue at 0 degrees */
	private static final String GRADIENT = "<w14:textFill><w14:gradFill><w14:gsLst>"
			+ "<w14:gs w14:pos=\"0\"><w14:srgbClr w14:val=\"FF0000\"/></w14:gs>"
			+ "<w14:gs w14:pos=\"100000\"><w14:srgbClr w14:val=\"0000FF\"/></w14:gs>"
			+ "</w14:gsLst><w14:lin w14:ang=\"0\" w14:scaled=\"0\"/></w14:gradFill></w14:textFill>";

	private static final String RUN_PR = "<w:rFonts w:ascii=\"Calibri\" w:hAnsi=\"Calibri\"/><w:sz w:val=\"48\"/>";

	@After
	public void reset() {
		TextEffectsFo.enabledForTest = null;
	}

	/** A numbered paragraph at 24 pt whose mark has {@code markFx}, its text plain; {@code jc} the alignment */
	private static String fo(String markFx, String jc, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		ndp.setJaxbElement((Numbering) XmlUtils.unmarshalString(NUMBERING));
		pkg.getMainDocumentPart().addTargetPart(ndp);
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr>"
				+ "<w:jc w:val=\"" + jc + "\"/><w:rPr>" + RUN_PR + markFx + "</w:rPr></w:pPr>"
				+ "<w:r><w:rPr>" + RUN_PR + "</w:rPr><w:t>The numbered paragraph.</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:rPr>" + RUN_PR + "</w:rPr><w:t>A plain paragraph after it.</w:t></w:r></w:p>"
				+ "<w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return baos.toString("UTF-8");
	}

	private static org.w3c.dom.Document dom(String fo) throws Exception {
		return XmlUtils.getNewDocumentBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader(fo)));
	}

	private static Element only(org.w3c.dom.Document doc, String localName) {
		NodeList list = doc.getElementsByTagNameNS(FO, localName);
		assertEquals(localName, 1, list.getLength());
		return (Element) list.item(0);
	}

	private static Element labelBlock(org.w3c.dom.Document doc) {
		Element label = only(doc, "list-item-label");
		Node block = label.getFirstChild();
		assertTrue("the label's block", block instanceof Element && "block".equals(block.getLocalName()));
		return (Element) block;
	}

	private static Element bodyLineBlock(org.w3c.dom.Document doc) {
		NodeList blocks = only(doc, "list-item-body").getElementsByTagNameNS(FO, "block");
		for (int i = 0; i < blocks.getLength(); i++) {
			Element b = (Element) blocks.item(i);
			if (b.hasAttributeNS(DOCX4J, "label-ascent")) return b;
		}
		return null;
	}

	private static double pt(String v) {
		return Double.parseDouble(v.replace("pt", ""));
	}

	private static final int[] ROUTES = { Docx4J.FLAG_EXPORT_PREFER_XSL, Docx4J.FLAG_EXPORT_PREFER_NONXSL };

	/**
	 * With the capability, the number sits in an fo:inline carrying the mark's effect id (the renderer reads an id
	 * from inlines only), in both routes, with no room on it; the mark's glow raises the label's ascent, and so the
	 * item's line, by phase 3's rule at 24 pt (wb82-30: 2.88 on Word's grid against 2.78), and nothing below
	 */
	@Test
	public void theNumberIsPaintedWithTheMarksEffects() throws Exception {
		WordLayoutOn.assume();
		for (int route : ROUTES) {
			TextEffectsFo.enabledForTest = Boolean.FALSE;
			org.w3c.dom.Document plain = dom(fo(GLOW8, "left", route));
			Element plainBody = bodyLineBlock(plain);
			assertNotNull("the plain label's ascent (the label's face has Word's metrics)", plainBody);
			double plainAscent = pt(plainBody.getAttributeNS(DOCX4J, "label-ascent"));
			double plainBaseline = pt(labelBlock(plain).getAttributeNS(DOCX4J, "baseline"));

			TextEffectsFo.enabledForTest = Boolean.TRUE;
			String fo = fo(GLOW8, "left", route);
			org.w3c.dom.Document doc = dom(fo);
			Element label = labelBlock(doc);
			assertEquals("route " + route + ": one child of the label's block", 1, label.getChildNodes().getLength());
			Node inline = label.getFirstChild();
			assertTrue("route " + route + ": the number in an fo:inline\n" + fo,
					inline instanceof Element && "inline".equals(inline.getLocalName()));
			assertEquals("1.", inline.getTextContent());
			assertEquals("fx1", ((Element) inline).getAttributeNS(DOCX4J, "text-effect"));
			assertFalse("no room on the inline: the renderer would set the label's own line off the text's",
					((Element) inline).hasAttributeNS(DOCX4J, "effect-room"));
			assertFalse("the hint consumed", fo.contains(TextEffectsFo.HINT_LABEL_ROOM));
			assertFalse("no room as a run's anywhere", fo.contains("effect-room"));

			double room = TextEffects.GLOW_ROOM * TextEffects.scale(24) * 8;
			Element body = bodyLineBlock(doc);
			assertNotNull(body);
			assertEquals("route " + route + ": the label's ascent raised by the glow's room above",
					plainAscent + room, pt(body.getAttributeNS(DOCX4J, "label-ascent")), 0.006);
			assertEquals("and the label's own baseline, which the item's first line takes (the larger)",
					Math.max(plainBaseline, plainAscent + room), pt(label.getAttributeNS(DOCX4J, "baseline")), 0.006);
		}
	}

	/** The mark's gradient marks the number (the painter fills its glyphs) and takes no room */
	@Test
	public void aGradientMarksTheNumberWithoutRoom() throws Exception {
		WordLayoutOn.assume();
		TextEffectsFo.enabledForTest = Boolean.FALSE;
		String plainAscent = bodyLineBlock(dom(fo(GRADIENT, "left", Docx4J.FLAG_EXPORT_PREFER_XSL)))
				.getAttributeNS(DOCX4J, "label-ascent");
		TextEffectsFo.enabledForTest = Boolean.TRUE;
		org.w3c.dom.Document doc = dom(fo(GRADIENT, "left", Docx4J.FLAG_EXPORT_PREFER_XSL));
		Element inline = (Element) labelBlock(doc).getFirstChild();
		assertEquals("fx1", inline.getAttributeNS(DOCX4J, "text-effect"));
		assertEquals(plainAscent, bodyLineBlock(doc).getAttributeNS(DOCX4J, "label-ascent"));
	}

	/** A centred number is drawn on the paragraph's line, an fo:inline already: its id, and no room (unmeasured) */
	@Test
	public void aCentredNumberIsMarkedOnItsLine() throws Exception {
		WordLayoutOn.assume();
		for (int route : ROUTES) {
			TextEffectsFo.enabledForTest = Boolean.TRUE;
			String fo = fo(GLOW8, "center", route);
			org.w3c.dom.Document doc = dom(fo);
			assertEquals("no list block for a centred number", 0, doc.getElementsByTagNameNS(FO, "list-block").getLength());
			NodeList inlines = doc.getElementsByTagNameNS(FO, "inline");
			Element label = null;
			for (int i = 0; i < inlines.getLength(); i++) {
				if ("1.".equals(inlines.item(i).getTextContent())) label = (Element) inlines.item(i);
			}
			assertNotNull("route " + route + ": the label inline\n" + fo, label);
			assertEquals("fx1", label.getAttributeNS(DOCX4J, "text-effect"));
			assertFalse(fo.contains("effect-room"));
		}
	}

	/** Without the capability the FO is byte for byte what the renderer docx4j is built against gets */
	@Test
	public void nothingChangesWithoutTheCapability() throws Exception {
		for (int route : ROUTES) {
			TextEffectsFo.enabledForTest = null;
			String asBuilt = fo(GLOW8, "left", route);
			TextEffectsFo.enabledForTest = Boolean.FALSE;
			String off = fo(GLOW8, "left", route);
			assertEquals("route " + route, asBuilt, off);
			org.w3c.dom.Document doc = dom(off);
			Element label = labelBlock(doc);
			assertEquals("the number is the label block's own text", 1, label.getChildNodes().getLength());
			assertTrue(label.getFirstChild() instanceof org.w3c.dom.Text);
			assertEquals("1.", label.getTextContent());
			assertFalse(off.contains("text-effect"));
			assertFalse(off.contains(TextEffectsFo.HINT_LABEL_ROOM));
		}
	}

	/** With the capability but no effect on the mark, the same: nothing is marked and no room is given */
	@Test
	public void nothingChangesWithoutAnEffect() throws Exception {
		for (int route : ROUTES) {
			for (String jc : new String[] { "left", "center" }) {
				TextEffectsFo.enabledForTest = Boolean.FALSE;
				String off = fo("", jc, route);
				TextEffectsFo.enabledForTest = Boolean.TRUE;
				String on = fo("", jc, route);
				assertEquals("route " + route + ", " + jc, off, on);
				assertFalse(on.contains("text-effect"));
			}
		}
	}

	/** The piece: the hint on a label's block adds its above to the label's ascent, and only its above */
	@Test
	public void theLabelRoomRidesOnTheLabelsAscent() throws Exception {
		WordLayoutOn.assume();
		String item = "<fo:root xmlns:fo=\"" + FO + "\"><fo:layout-master-set><fo:simple-page-master master-name=\"p\">"
				+ "<fo:region-body/></fo:simple-page-master></fo:layout-master-set><fo:page-sequence master-reference=\"p\">"
				+ "<fo:flow flow-name=\"xsl-region-body\"><fo:list-block><fo:list-item>"
				+ "<fo:list-item-label font-size=\"11pt\"><fo:block font-family=\"Liberation Serif\" docx4j-font=\"Times New Roman\""
				+ " line-height=\"12.65pt\"%s><fo:inline docx4j-text-effect=\"fx1\">1.</fo:inline></fo:block></fo:list-item-label>"
				+ "<fo:list-item-body><fo:block docx4j-linebox=\"12.65pt\" docx4j-baseline=\"9.76pt\" docx4j-linerule=\"auto\""
				+ " font-family=\"Liberation Serif\" font-size=\"11pt\" line-height=\"12.65pt\">text</fo:block>"
				+ "</fo:list-item-body></fo:list-item></fo:list-block></fo:flow></fo:page-sequence></fo:root>";
		String plain = WordLayoutFixups.apply(String.format(Locale.ROOT, item, ""), 15);
		String roomed = WordLayoutFixups.apply(String.format(Locale.ROOT, item,
				" " + TextEffectsFo.HINT_LABEL_ROOM + "=\"2.780 2.780\""), 15);
		assertFalse(roomed, roomed.contains(TextEffectsFo.HINT_LABEL_ROOM));
		assertTrue(roomed, roomed.contains("docx4j:text-effect=\"fx1\""));
		Element plainBody = bodyLineBlock(dom(plain)), roomedBody = bodyLineBlock(dom(roomed));
		assertNotNull(plainBody);
		assertNotNull(roomedBody);
		double ascent = pt(plainBody.getAttributeNS(DOCX4J, "label-ascent"));
		assertEquals(ascent + 2.78, pt(roomedBody.getAttributeNS(DOCX4J, "label-ascent")), 0.006);
		Element label = labelBlock(dom(roomed));
		assertEquals("the label's line: its ascent with the room over the text's descent",
				ascent + 2.78 + (12.65 - 9.76), pt(label.getAttributeNS(DOCX4J, "line-box")), 0.011);
		assertFalse("none below", roomed.contains("effect-room"));
	}
}
