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
package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An anchored picture in a table cell whose position is relative to the page or the
 * margin: Word measures it from the cell - {@code relativeFrom="page"} from the cell's
 * left edge and top, {@code "margin"} from its text (inside the left cell margin) and
 * top - and in mode 15 whatever {@code layoutInCell} says.  Measured on Word's PDFs of
 * corpus documents (WordXmlPictureE20.stampAnchorHints): a margin-relative picture lands
 * at the cell's text edge + 30.3 and its top + 6.5, a page-relative header logo at the
 * cell's edge + 6.8 and its top + 3.1, and a layoutInCell="0" picture in a mode-15
 * document at its cell's edge - 421.55.
 *
 * <p>m1.docx's letterhead logo ({@code margin} 139.25pt, {@code page} 0.75pt, in the
 * second cell of a two-cell table) was drawn at the top of the page across the first
 * cell; Word draws it in the second.  Both pathways.</p>
 *
 * @since 17.3.3
 */
public class AnchorPositionedFromCellTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	/** Letter, 0.25in side margins: a 576pt text column. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/>"
			+ "<w:pgMar w:top=\"144\" w:right=\"360\" w:bottom=\"576\" w:left=\"360\"/></w:sectPr>";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static byte[] png() throws Exception {
		java.awt.image.BufferedImage img =
				new java.awt.image.BufferedImage(80, 20, java.awt.image.BufferedImage.TYPE_INT_RGB);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		javax.imageio.ImageIO.write(img, "png", out);
		return out.toByteArray();
	}

	/** A 139.65 x 36pt picture (m1's logo) anchored with the given position elements. */
	private static String anchored(String relId, String layoutInCell, String wrap, String positionH, String positionV) {
		return anchored(relId, layoutInCell, wrap, positionH, positionV, 0);
	}

	/** As above, with a distB (EMU). */
	private static String anchored(String relId, String layoutInCell, String wrap, String positionH, String positionV,
			long distB) {
		return "<w:r><w:drawing xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
				+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
				+ " xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\""
				+ " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
				+ "<wp:anchor distT=\"0\" distB=\"" + distB + "\" distL=\"114300\" distR=\"114300\" simplePos=\"0\""
				+ " relativeHeight=\"251683845\" behindDoc=\"1\" locked=\"0\" layoutInCell=\"" + layoutInCell
				+ "\" allowOverlap=\"1\"><wp:simplePos x=\"0\" y=\"0\"/>" + positionH + positionV
				+ "<wp:extent cx=\"1773555\" cy=\"457200\"/><wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/>"
				+ wrap + "<wp:docPr id=\"1\" name=\"Picture 1\"/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">"
				+ "<pic:pic><pic:nvPicPr><pic:cNvPr id=\"2\" name=\"Picture 1\"/><pic:cNvPicPr/></pic:nvPicPr>"
				+ "<pic:blipFill><a:blip r:embed=\"" + relId + "\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>"
				+ "<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"1773555\" cy=\"457200\"/></a:xfrm>"
				+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>"
				+ "</a:graphicData></a:graphic></wp:anchor></w:drawing></w:r>";
	}

	private static String pos(String axis, String relativeFrom, String value) {
		String inner = value.startsWith("@") ? "<wp:align>" + value.substring(1) + "</wp:align>"
				: "<wp:posOffset>" + value + "</wp:posOffset>";
		return "<wp:position" + axis + " relativeFrom=\"" + relativeFrom + "\">" + inner + "</wp:position" + axis + ">";
	}

	/** m1's letterhead: two 5760-twip cells (default 108-twip cell margins), the picture in the second. */
	private org.w3c.dom.Document fo(int flags, int mode, String layoutInCell, String wrap,
			String positionH, String positionV) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String relId = org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage
				.createImagePart(pkg, png()).getSourceRelationship().getId();
		return fo(pkg, flags, mode, anchored(relId, layoutInCell, wrap, positionH, positionV));
	}

	private org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags, int mode, String run) throws Exception {
		return fo(pkg, flags, mode, run, "");
	}

	/** As above, the row with the given w:trPr ("" for none). */
	private org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags, int mode, String run, String trPr)
			throws Exception {
		pkg.getMainDocumentPart().getDocumentSettingsPart().setCompatibilityMode(mode);
		String cellPr = "<w:tcPr><w:tcW w:w=\"5760\" w:type=\"dxa\"/></w:tcPr>";
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:tbl><w:tblPr><w:tblW w:w=\"11520\" w:type=\"dxa\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"5760\"/><w:gridCol w:w=\"5760\"/></w:tblGrid><w:tr>" + trPr
				+ "<w:tc>" + cellPr + "<w:p><w:r><w:t>Sample Processing Center</w:t></w:r></w:p></w:tc>"
				+ "<w:tc>" + cellPr + "<w:p><w:pPr><w:jc w:val=\"right\"/></w:pPr>"
				+ run + "</w:p></w:tc>"
				+ "</w:tr></w:tbl><w:p><w:r><w:t>after the table</w:t></w:r></w:p>"
				+ SECT_PR + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	/** The positioned container holding the picture. */
	private static Element positioned(org.w3c.dom.Document doc, String pathway) {
		NodeList list = doc.getElementsByTagNameNS(FO, "external-graphic");
		assertEquals(pathway + "one picture", 1, list.getLength());
		for (Node n = list.item(0).getParentNode(); n instanceof Element; n = n.getParentNode()) {
			Element e = (Element) n;
			if ("block-container".equals(e.getLocalName()) && e.hasAttribute("absolute-position")) return e;
		}
		return null;
	}

	private static double pt(String v) {
		assertTrue("no value", v != null && v.length() > 0);
		return Double.parseDouble(v.substring(0, v.length() - 2));
	}

	private static String pathway(int flags) {
		return flags == Docx4J.FLAG_NONE ? "visitor: " : "XSLT: ";
	}

	private static final String SQUARE = "<wp:wrapSquare wrapText=\"bothSides\"/>";
	private static final String NONE = "<wp:wrapNone/>";

	/** The zero-height wrapper sits at the head of the second cell, which is what the
	 *  offsets are measured from. */
	private static void atTheCellsTop(Element abs, String pathway) {
		Node wrapper = abs.getParentNode();
		Node cell = wrapper.getParentNode();
		assertEquals(pathway + "the wrapper is in the cell itself", "table-cell", cell.getLocalName());
		Node first = cell.getFirstChild();
		while (first != null && !(first instanceof Element)) first = first.getNextSibling();
		assertTrue(pathway + "at its head", first == wrapper);
		Node row = cell.getParentNode();
		Node firstCell = row.getFirstChild();
		while (firstCell != null && !(firstCell instanceof Element)) firstCell = firstCell.getNextSibling();
		assertTrue(pathway + "the second cell, not the first", firstCell != cell);
	}

	/** m1: margin 139.25pt, page 0.75pt - from the cell's text and top, not the page's. */
	@Test
	public void marginAndPageFromTheCell() throws Exception {
		for (int flags : FLAGS) {
			Element abs = positioned(fo(flags, 15, "1", SQUARE, pos("H", "margin", "1768475"),
					pos("V", "page", "9525")), pathway(flags));
			assertNotNull(pathway(flags) + "positioned", abs);
			assertEquals(pathway(flags), "absolute", abs.getAttribute("absolute-position"));
			assertEquals(pathway(flags) + "left", 139.25, pt(abs.getAttribute("left")), 0.01);
			assertEquals(pathway(flags) + "top", 0.75, pt(abs.getAttribute("top")), 0.01);
			atTheCellsTop(abs, pathway(flags));
		}
	}

	/** page horizontally is the cell's edge: 6.8pt less the 5.4pt cell margin. */
	@Test
	public void pageIsTheCellsEdge() throws Exception {
		for (int flags : FLAGS) {
			Element abs = positioned(fo(flags, 15, "1", NONE, pos("H", "page", "86360"),
					pos("V", "page", "39370")), pathway(flags));
			assertNotNull(pathway(flags) + "positioned", abs);
			assertEquals(pathway(flags), "absolute", abs.getAttribute("absolute-position"));
			assertEquals(pathway(flags) + "left", 1.4, pt(abs.getAttribute("left")), 0.02); // the cell margin is written as 1.91mm
			assertEquals(pathway(flags) + "top", 3.1, pt(abs.getAttribute("top")), 0.01);
			atTheCellsTop(abs, pathway(flags));
		}
	}

	/** An alignment is within the cell's text: right-aligned, its right edge on the text's. */
	@Test
	public void alignedWithinTheCell() throws Exception {
		for (int flags : FLAGS) {
			Element abs = positioned(fo(flags, 15, "1", NONE, pos("H", "margin", "@right"),
					pos("V", "margin", "0")), pathway(flags));
			assertNotNull(pathway(flags) + "positioned", abs);
			assertEquals(pathway(flags) + "left = 288 - 10.8 - 139.65", 137.55, pt(abs.getAttribute("left")), 0.05);
			assertEquals(pathway(flags) + "top", 0, pt(abs.getAttribute("top")), 0.01);
		}
	}

	/** Mode 15: in the cell though layoutInCell is 0, as Word 2013 has it. */
	@Test
	public void mode15IgnoresLayoutInCell() throws Exception {
		for (int flags : FLAGS) {
			Element abs = positioned(fo(flags, 15, "0", NONE, pos("H", "page", "86360"),
					pos("V", "page", "39370")), pathway(flags));
			assertNotNull(pathway(flags) + "positioned", abs);
			assertEquals(pathway(flags), "absolute", abs.getAttribute("absolute-position"));
			atTheCellsTop(abs, pathway(flags));
		}
	}

	/** Below mode 15 layoutInCell="0" keeps the page's reading (not measured otherwise). */
	@Test
	public void belowMode15LayoutInCell0IsThePage() throws Exception {
		for (int flags : FLAGS) {
			Element abs = positioned(fo(flags, 14, "0", NONE, pos("H", "page", "86360"),
					pos("V", "page", "39370")), pathway(flags));
			assertNotNull(pathway(flags) + "positioned", abs);
			assertEquals(pathway(flags), "fixed", abs.getAttribute("absolute-position"));
			assertEquals(pathway(flags) + "left", 6.8, pt(abs.getAttribute("left")), 0.01);
			assertEquals(pathway(flags) + "top", 3.1, pt(abs.getAttribute("top")), 0.01);
		}
	}

	/** Below mode 15 layoutInCell="1" is the cell, as in mode 15. */
	@Test
	public void belowMode15LayoutInCell1IsTheCell() throws Exception {
		for (int flags : FLAGS) {
			Element abs = positioned(fo(flags, 14, "1", NONE, pos("H", "page", "86360"),
					pos("V", "page", "39370")), pathway(flags));
			assertNotNull(pathway(flags) + "positioned", abs);
			assertEquals(pathway(flags), "absolute", abs.getAttribute("absolute-position"));
			assertEquals(pathway(flags) + "left", 1.4, pt(abs.getAttribute("left")), 0.02); // the cell margin is written as 1.91mm
		}
	}

	/** A paragraph-relative offset is still the paragraph's; a page-relative horizontal one
	 *  beside it is the cell's edge. */
	@Test
	public void paragraphVerticalKeepsTheParagraph() throws Exception {
		for (int flags : FLAGS) {
			Element abs = positioned(fo(flags, 15, "1", NONE, pos("H", "page", "86360"),
					pos("V", "paragraph", "127000")), pathway(flags));
			assertNotNull(pathway(flags) + "positioned", abs);
			assertEquals(pathway(flags), "absolute", abs.getAttribute("absolute-position"));
			assertEquals(pathway(flags) + "left", 1.4, pt(abs.getAttribute("left")), 0.02); // the cell margin is written as 1.91mm
			assertEquals(pathway(flags) + "top", 10, pt(abs.getAttribute("top")), 0.01);
			Node holder = abs.getParentNode().getParentNode();
			assertEquals(pathway(flags) + "at the paragraph's head", "block", holder.getLocalName());
		}
	}

	/** The row a positioned picture's cell is in. */
	private static Element rowOf(Element abs) {
		for (Node n = abs.getParentNode(); n instanceof Element; n = n.getParentNode()) {
			if ("table-row".equals(n.getLocalName())) return (Element) n;
		}
		return null;
	}

	private org.w3c.dom.Document fo(int flags, int mode, String layoutInCell, String wrap, String positionH,
			String positionV, long distB, String trPr) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		String relId = org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage
				.createImagePart(pkg, png()).getSourceRelationship().getId();
		return fo(pkg, flags, mode, anchored(relId, layoutInCell, wrap, positionH, positionV, distB), trPr);
	}

	/** m1's page 2: Word grows the one-line row to the foot of the logo its cell wraps the
	 *  text round (behindDoc="1" beside wrapSquare), 0.04 + 36pt below the cell's top, where
	 *  docx4j's row ended at its line and the page's body 22pt high (probe
	 *  anchor-in-cell-row-height cases 1 and 2). */
	@Test
	public void theRowGrowsToThePicturesFoot() throws Exception {
		for (int flags : FLAGS) {
			Element abs = positioned(fo(flags, 15, "1", SQUARE, pos("H", "margin", "1768475"),
					pos("V", "page", "545"), 0, ""), pathway(flags));
			assertNotNull(pathway(flags) + "positioned", abs);
			assertEquals(pathway(flags) + "the row's height", 36.04, pt(rowOf(abs).getAttribute("height")), 0.01);
		}
	}

	/** From the paragraph: its offset below the cell's first paragraph, then the picture. */
	@Test
	public void aParagraphRelativePictureGrowsItsRow() throws Exception {
		for (int flags : FLAGS) {
			Element abs = positioned(fo(flags, 15, "1", SQUARE, pos("H", "margin", "0"),
					pos("V", "paragraph", "127000"), 0, ""), pathway(flags));
			assertNotNull(pathway(flags) + "positioned", abs);
			assertEquals(pathway(flags) + "the row's height", 46, pt(rowOf(abs).getAttribute("height")), 0.01);
		}
	}

	/** distB below the picture in mode 15 (probe case 10: 10pt more), not in mode 14. */
	@Test
	public void distBCountsInMode15Only() throws Exception {
		for (int flags : FLAGS) {
			Element abs = positioned(fo(flags, 15, "1", SQUARE, pos("H", "margin", "0"),
					pos("V", "page", "545"), 127000, ""), pathway(flags));
			assertEquals(pathway(flags) + "mode 15", 46.04, pt(rowOf(abs).getAttribute("height")), 0.01);
			abs = positioned(fo(flags, 14, "1", SQUARE, pos("H", "margin", "0"),
					pos("V", "page", "545"), 127000, ""), pathway(flags));
			assertEquals(pathway(flags) + "mode 14", 36.04, pt(rowOf(abs).getAttribute("height")), 0.01);
		}
	}

	/** In front of the text or behind it with no wrap, the row keeps to its text (cases 4, 5). */
	@Test
	public void noWrapLeavesTheRow() throws Exception {
		for (int flags : FLAGS) {
			Element abs = positioned(fo(flags, 15, "1", NONE, pos("H", "margin", "0"),
					pos("V", "page", "545"), 0, ""), pathway(flags));
			assertEquals(pathway(flags) + "no height", "", rowOf(abs).getAttribute("height"));
		}
	}

	/** An exact row keeps its height (case 13: Word clips the picture, the row 20pt). */
	@Test
	public void anExactRowKeepsItsHeight() throws Exception {
		for (int flags : FLAGS) {
			Element abs = positioned(fo(flags, 15, "1", SQUARE, pos("H", "margin", "0"), pos("V", "page", "545"), 0,
					"<w:trPr><w:trHeight w:val=\"400\" w:hRule=\"exact\"/></w:trPr>"), pathway(flags));
			assertTrue(pathway(flags) + "at most its 20pt", pt(rowOf(abs).getAttribute("height")) <= 20.001);
		}
	}

	/** A VML text box (13743's, as docx4j draws a DrawingML shape's mc:Fallback). */
	private static String vmlTextBox(String allowincell) {
		return "<w:r><w:pict xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">"
				+ "<v:shape id=\"TextBox 6\" type=\"#_x0000_t202\" style=\"position:absolute;margin-left:54.15pt;"
				+ "margin-top:25.8pt;width:100pt;height:50pt;z-index:1;mso-position-horizontal:absolute;"
				+ "mso-position-horizontal-relative:text;mso-position-vertical:absolute;"
				+ "mso-position-vertical-relative:page\" o:allowincell=\"" + allowincell + "\" stroked=\"f\">"
				+ "<v:textbox inset=\"14.4pt,14.4pt,14.4pt,14.4pt\"><w:txbxContent><w:p><w:r><w:t>boxed</w:t></w:r></w:p>"
				+ "</w:txbxContent></v:textbox></v:shape></w:pict></w:r>";
	}

	/** The positioned container holding the text box's text. */
	private static Element positionedBox(org.w3c.dom.Document doc) {
		NodeList list = doc.getElementsByTagNameNS(FO, "block-container");
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			if (e.hasAttribute("absolute-position") && e.getTextContent().contains("boxed")) return e;
		}
		return null;
	}

	/** A VML text box takes the same rule: in mode 15 its cell's, though o:allowincell is "f". */
	@Test
	public void aVmlTextBoxInMode15IsTheCells() throws Exception {
		for (int flags : FLAGS) {
			Element box = positionedBox(fo(WordprocessingMLPackage.createPackage(), flags, 15, vmlTextBox("f")));
			assertNotNull(pathway(flags) + "positioned", box);
			assertEquals(pathway(flags), "absolute", box.getAttribute("absolute-position"));
			assertEquals(pathway(flags) + "left", 54.15, pt(box.getAttribute("left")), 0.01);
			assertEquals(pathway(flags) + "top (with the 14.4pt inset)", 40.2, pt(box.getAttribute("top")), 0.01);
			assertEquals(pathway(flags), "table-cell", box.getParentNode().getParentNode().getLocalName());
		}
	}

	/** Below mode 15 o:allowincell="f" keeps the page's reading. */
	@Test
	public void aVmlTextBoxBelowMode15NotAllowedInCellIsThePage() throws Exception {
		for (int flags : FLAGS) {
			Element box = positionedBox(fo(WordprocessingMLPackage.createPackage(), flags, 14, vmlTextBox("f")));
			assertNotNull(pathway(flags) + "positioned", box);
			assertEquals(pathway(flags), "fixed", box.getAttribute("absolute-position"));
		}
	}
}
