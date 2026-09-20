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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The numbering tab's dot leader: Word paints the {@code w:leader} of the stop the
 * numbering tab reaches, from the end of the label to that stop.
 *
 * <p>Measured on the {@code tab-leader-in-cell-2} golden, a level of
 * {@code w:ind w:left="2880" w:hanging="2520"} with one {@code w:leader="dot"} left stop
 * at 1440 twips - which the numbering tab uses, because it falls short of {@code w:ind}
 * left. In a table cell Word sets the label {@code 1.} in Calibri 11.04pt from 95.808 to
 * 104.110, then <b>fourteen</b> dots stepping <b>3.120pt</b> from 106.130 to 146.690, and
 * the paragraph's first line of text at 149.830, which is the stop; in the body, the label
 * at 90.048, the same fourteen dots from 99.888 to 140.448 and the text at 144.070. The
 * same golden's control level, which declares no stop at all, gets no dots at either
 * place, and neither side paints one over the 560-twip stop of the first
 * {@code tab-leader-in-cell} probe, whose advance past the label is under 2pt - less than
 * one cell of Word's 1/300 inch grid.
 *
 * <p>The step is the dot's advance rounded to that grid, so it is decided by the face and
 * the size the leader is drawn in - and the {@code tab-leader-sizes} golden says what
 * those are: <b>Arial</b>, at the <b>label's</b> size, whatever either block is set in.
 * On its three numbered paragraphs Word draws the label in Liberation Serif 7.92 and
 * 16.08 and the leader in <b>ArialMT</b> 7.92 and 16.08, stepping 2.16 (21 dots) and 4.56
 * (8 dots), and for an 8pt Arial label under a 16pt Times New Roman paragraph the leader
 * is ArialMT 7.92 again. The same holds on this golden, whose level names neither face nor
 * size: the label is Calibri 11.04pt and the dots are ArialMT 11.04pt stepping 3.120 -
 * Arial's 569/2048 em period at 11.04pt is 3.0672pt, 12.78 cells of the grid, and Word's
 * 13 cells are 3.120pt.
 *
 * <p>Arial is this construct's own. On the {@code tab-leader-kinds} golden, where the
 * leaders belong to <em>paragraph</em> tabs, Word draws every one in the paragraph's own
 * Calibri, so the two are not the same construct.
 *
 * @since 17.2.0 (CR-001 batch 47 item 5b; the leader's font corrected by its own golden in
 *         batch 48 item 1)
 */
public class NumberingTabLeaderTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";
	private static final String DOCX4J_FO = "http://docx4j.org/fop/word-layout";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	/**
	 * A decimal level over {@code w:ind w:left="2880" w:hanging="2520"} - so the label
	 * runs from 360 twips and the paragraph's indent is at 2880 - with one left stop at
	 * {@code stop} twips carrying {@code leader} (and none at all where stop is 0).
	 */
	private static String level(int abstractId, int stop, String leader) {
		return level(abstractId, stop, leader, null);
	}

	/** {@code lvlRPr} is the level's own {@code w:rPr}, which is what the label is set in. */
	private static String level(int abstractId, int stop, String leader, String lvlRPr) {
		return "<w:abstractNum w:abstractNumId=\"" + abstractId + "\">"
				+ "<w:multiLevelType w:val=\"hybridMultilevel\"/>"
				+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>"
				+ "<w:suff w:val=\"tab\"/><w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>"
				+ "<w:pPr>"
				+ (stop <= 0 ? "" : "<w:tabs><w:tab w:val=\"left\" w:pos=\"" + stop + "\""
						+ (leader == null ? "" : " w:leader=\"" + leader + "\"") + "/></w:tabs>")
				+ "<w:ind w:left=\"2880\" w:hanging=\"2520\"/>"
				+ "</w:pPr>"
				+ (lvlRPr == null ? "" : lvlRPr)
				+ "</w:lvl></w:abstractNum>"
				+ "<w:num w:numId=\"" + abstractId + "\"><w:abstractNumId w:val=\"" + abstractId
				+ "\"/></w:num>";
	}

	/** 1: the golden's level. 2: the same stop with no leader. 3: no stop at all.
	 *  4: a stop 20 twips past the label, whose advance is under one grid cell.
	 *  5: the golden's level with an 8pt label under the 12pt paragraph, which is what
	 *  separates "the label's size" from "the paragraph's" (batch 48 item 1). */
	private static final String NUMBERING = "<w:numbering " + W + ">"
			+ level(1, 1440, "dot")
			+ level(2, 1440, null)
			+ level(3, 0, null)
			+ level(4, 560, "dot")
			+ level(5, 1440, "dot", "<w:rPr><w:sz w:val=\"16\"/><w:szCs w:val=\"16\"/></w:rPr>")
			+ level(6, 1440, "hyphen")
			+ level(7, 1440, "underscore")
			+ level(8, 1440, "heavy")
			+ level(9, 1440, "middleDot")
			+ "</w:numbering>";

	private static String item(String numId) {
		return "<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/>"
				+ "<w:numId w:val=\"" + numId + "\"/></w:numPr>"
				+ "<w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
				+ "<w:sz w:val=\"24\"/></w:rPr></w:pPr>"
				+ "<w:r><w:rPr><w:rFonts w:ascii=\"Times New Roman\" w:hAnsi=\"Times New Roman\"/>"
				+ "<w:sz w:val=\"24\"/></w:rPr><w:t>the item's text</w:t></w:r></w:p>";
	}

	/** The package the last {@link #fo} call converted, so a test can ask its mapper what
	 *  this environment draws Arial in. */
	private static WordprocessingMLPackage lastPackage;

	private static org.w3c.dom.Document fo(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		lastPackage = pkg;
		org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart ndp
				= new org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart();
		ndp.setJaxbElement((org.docx4j.wml.Numbering) XmlUtils.unmarshalString(NUMBERING));
		pkg.getMainDocumentPart().addTargetPart(ndp);
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
	}

	/** The n-th list item's label block. */
	private static Element label(org.w3c.dom.Document doc, int n) {
		Element holder = (Element) doc.getElementsByTagNameNS(FO, "list-item-label").item(n);
		assertNotNull("no list item " + n, holder);
		return (Element) holder.getElementsByTagNameNS(FO, "block").item(0);
	}

	/** The n-th list item's body block. */
	private static Element body(org.w3c.dom.Document doc, int n) {
		Element holder = (Element) doc.getElementsByTagNameNS(FO, "list-item-body").item(n);
		return (Element) holder.getElementsByTagNameNS(FO, "block").item(0);
	}

	/** The leader inside the n-th item's label, or null. */
	private static Element leader(org.w3c.dom.Document doc, int n) {
		NodeList nl = label(doc, n).getElementsByTagNameNS(FO, "leader");
		return nl.getLength() == 0 ? null : (Element) nl.item(0);
	}

	private void check(int flags) throws Exception {

		org.w3c.dom.Document doc = fo(item("1") + item("2") + item("3") + item("4") + item("5")
				+ item("6") + item("7") + item("8") + item("9"), flags);

		// (a) the golden's level: a dot leader from the end of the label to the stop
		Element leader = leader(doc, 0);
		assertNotNull("no leader on the numbering tab's stop", leader);
		/* Every kind is a run of its own character, so the leader carries the character and
		 * FOP repeats it (batch 48 item 2) - not the dot pattern, and not a rule. */
		assertEquals("use-content", leader.getAttribute("leader-pattern"));
		assertEquals(".", leader.getTextContent());
		assertEquals("dot", leader.getAttributeNS(DOCX4J_FO, "toc-leader"));
		assertEquals("reference-area", leader.getAttribute("leader-alignment"));
		// the stop at 1440tw less the end of the label, which is measured: the level's
		// number position (360tw) plus the width of "1." in the label's own face
		double advance = pt(leader.getAttribute("leader-length"));
		assertEquals("the leader runs from the label's end to the 72pt stop: 1440tw less the"
				+ " level's 360tw number position and the measured width of \"1.\"",
				45.0, advance, 1.5);

		/* In Arial at the LABEL's size, which is what decides the step (batch 48 item 1).
		 * This level names neither face nor size, so the label takes the paragraph mark's
		 * own 12pt - the same as the paragraph here; level 5 below is the case that
		 * separates the two. */
		assertEquals("the leader takes the label's size", pt(sizeOf(label(doc, 0))),
				pt(leader.getAttribute("font-size")), 0.01);
		org.docx4j.fonts.PhysicalFont arial = lastPackage.getFontMapper().get("Arial");
		assertNotNull("no physical font for Arial in this environment", arial);
		assertEquals("the leader is drawn in Arial, whatever the label's face",
				arial.getName(), leader.getAttribute("font-family"));
		assertNotEquals("Arial is not the label's own face here",
				label(doc, 0).getAttribute("font-family"), leader.getAttribute("font-family"));

		// and the first line still starts at the stop (item 3): w:ind left less the stop
		assertEquals("-1in", body(doc, 0).getAttribute("text-indent"));

		// (b) the same stop with no w:leader paints nothing
		assertNull("a stop with no w:leader painted a leader", leader(doc, 1));
		assertEquals("-1in", body(doc, 1).getAttribute("text-indent"));

		// (c) a level with no stop at all: no leader, and no first-line move either
		assertNull("a level with no stop painted a leader", leader(doc, 2));
		assertEquals("", body(doc, 2).getAttribute("text-indent"));

		// (d) a stop 20 twips past the label: Word paints no dot over an advance under one
		// cell of its grid, and neither do we - the fo:leader is written, and it is
		// narrower than its own repeating unit, so it draws nothing
		Element narrow = leader(doc, 3);
		assertNotNull(narrow);
		assertEquals("the advance is the stop less the measured label", 1.0,
				pt(narrow.getAttribute("leader-length")), 1.5);

		/* (e) an 8pt label under the 12pt paragraph: the leader takes the LABEL's 8pt.
		 * Word's tab-leader-sizes golden is exactly this shape - an 8pt label under a 16pt
		 * paragraph gives ArialMT 7.92 stepping 2.16, and a 16pt label under an 8pt
		 * paragraph ArialMT 16.08 stepping 4.56, the inverse of what batch 47 item 5(b)
		 * wrote. */
		Element eight = leader(doc, 4);
		assertNotNull("no leader on the 8pt level's stop", eight);
		assertEquals("the label's own size", 8.0, pt(sizeOf(label(doc, 4))), 0.01);
		/* The label's 8pt on Word's 1/300 inch grid, which is the size Word draws at and
		 * what its step is computed from: 8 / 0.24 = 33.33 cells, so 33 = 7.92pt.  The
		 * first item's 12pt is 50 cells exactly and does not move. */
		assertEquals("the leader takes the label's 8pt on the grid, not the paragraph's 12pt",
				7.92, pt(eight.getAttribute("font-size")), 0.01);
		assertEquals("the paragraph's own size, for contrast",
				12.0, pt(sizeOf(body(doc, 4))), 0.01);
		assertEquals("and still Arial", arial.getName(), eight.getAttribute("font-family"));

		/* (f) every leader kind is a run of its own character, on the numbering-leader-kinds
		 * golden's reading: Word draws hyphen as a hyphen stepping 3.60, underscore AND
		 * heavy as an underscore stepping 6.24 - the two runs are identical, glyph for
		 * glyph and position for position - and middleDot as U+00B7 stepping 3.60, all in
		 * ArialMT at the label's size.  docx4j asked FOP for a rule for the first three,
		 * which paints a line and nothing in the text layer, and for a full stop for
		 * middleDot. */
		String[] expected = { "-", "_", "_", String.valueOf((char) 0x00B7) };
		String[] kindName = { "hyphen", "underscore", "heavy", "middleDot" };
		for (int k = 0; k < expected.length; k++) {
			Element kind = leader(doc, 5 + k);
			assertNotNull("no leader for w:leader=" + kindName[k], kind);
			assertEquals(kindName[k] + " is not a character run",
					"use-content", kind.getAttribute("leader-pattern"));
			assertEquals(kindName[k] + " repeats the wrong character",
					expected[k], kind.getTextContent());
			assertEquals(kindName[k] + " is not in Arial",
					arial.getName(), kind.getAttribute("font-family"));
			assertEquals(kindName[k] + " does not carry the kind",
					kindName[k], kind.getAttributeNS(DOCX4J_FO, "toc-leader"));
		}
	}

	/** The font-size in force on {@code el}: its own, or the nearest ancestor's. */
	private static String sizeOf(Element el) {
		for (org.w3c.dom.Node n = el; n instanceof Element; n = n.getParentNode()) {
			String size = ((Element) n).getAttribute("font-size");
			if (size != null && size.length() > 0) return size;
		}
		return "";
	}

	/** points from an FO length, which twipToBest writes as pt, in or mm */
	private static double pt(String length) {
		assertNotNull(length);
		String v = length.trim();
		if (v.endsWith("pt")) return Double.parseDouble(v.substring(0, v.length() - 2));
		if (v.endsWith("in")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72;
		if (v.endsWith("mm")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72 / 25.4;
		return Double.parseDouble(v);
	}

	@Test
	public void visitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
