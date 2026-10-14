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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Where a right-aligned or nested table sits, as Word puts it (CR-041 phase 1, measured on
 * the probes table-align-survey-compat15/-compat14 and table-align-p0-*):
 *
 * <ul>
 * <li>w:jc right or end puts the table's end on the text column's end, w:tblInd ignored
 *   (a 4500-twip table's first cell text at 303.7pt on a 72..523.3 column), and below mode
 *   15 one right cell margin past it (309.2); start is left;</li>
 * <li>a right-aligned table wider than the column overhangs the left margin;</li>
 * <li>a centred or right-aligned table nested in a cell is aligned within that cell's
 *   content (Word's inner grid edge at 197.65pt for a 4000-twip table centred in a
 *   full-width cell), not on the page's column;</li>
 * <li>a w:bidiVisual table is left as it was (CR-041 phase 3).</li>
 * </ul>
 *
 * Both FO pathways.
 */
public class TableAlignmentTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** A4 portrait with 1in margins: a 9026 twip (451.3pt) text column. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static final int COLUMN_TWIPS = 11906 - 2880;

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String cell(int width, String text) {
		return "<w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\"" + width + "\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p></w:tc>";
	}

	/** Three fixed columns of a third of the width each. */
	private static String table(String tblPrExtra, int width) {
		int col = width / 3;
		return "<w:tbl><w:tblPr><w:tblW w:w=\"" + width + "\" w:type=\"dxa\"/>" + tblPrExtra
				+ "<w:tblLayout w:type=\"fixed\"/></w:tblPr><w:tblGrid>"
				+ "<w:gridCol w:w=\"" + col + "\"/><w:gridCol w:w=\"" + col + "\"/><w:gridCol w:w=\"" + col + "\"/>"
				+ "</w:tblGrid><w:tr>" + cell(col, "a") + cell(col, "b") + cell(col, "c") + "</w:tr></w:tbl>";
	}

	/** A full-width one-cell table holding a paragraph, the given table, and a paragraph. */
	private static String nested(String inner) {
		return "<w:tbl><w:tblPr><w:tblW w:w=\"5000\" w:type=\"pct\"/></w:tblPr><w:tblGrid>"
				+ "<w:gridCol w:w=\"" + COLUMN_TWIPS + "\"/></w:tblGrid><w:tr><w:tc><w:tcPr><w:tcW w:type=\"dxa\" w:w=\""
				+ COLUMN_TWIPS + "\"/></w:tcPr><w:p><w:r><w:t>outer</w:t></w:r></w:p>" + inner
				+ "<w:p><w:r><w:t>after</w:t></w:r></w:p></w:tc></w:tr></w:tbl>";
	}

	private static org.w3c.dom.Document fo(String body, int flags, int compatMode, String tableStyleXml) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getDocumentSettingsPart()
				.setWordCompatSetting("compatibilityMode", Integer.toString(compatMode));
		if (tableStyleXml != null) {
			Styles s = (Styles) XmlUtils.unmarshalString("<w:styles " + W + ">" + tableStyleXml + "</w:styles>");
			for (Style style : s.getStyle()) {
				pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add(style);
			}
		}
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	/** The n-th fo:table (0 the first, 1 a table nested in it). */
	private static Element foTable(org.w3c.dom.Document doc, int n) {
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "table");
		assertTrue("fo:table " + n, nl.getLength() > n);
		return (Element) nl.item(n);
	}

	/** a length like "-5.4pt", "0in" or "451.3pt", in points */
	private static double pt(String length) {
		if (length == null || length.length() == 0) return 0;
		if (length.endsWith("pt")) return Double.parseDouble(length.substring(0, length.length() - 2));
		if (length.endsWith("in")) return Double.parseDouble(length.substring(0, length.length() - 2)) * 72;
		throw new IllegalArgumentException(length);
	}

	private static double startIndentPt(String body, int flags, int mode) throws Exception {
		return startIndentPt(body, flags, mode, null, 0);
	}

	private static double startIndentPt(String body, int flags, int mode, String style, int n) throws Exception {
		Element tbl = foTable(fo(body, flags, mode, style), n);
		assertTrue("no docx4j hint left on the fo:table", !tbl.hasAttribute("docx4j-table-align")
				&& !tbl.hasAttribute("docx4j-table-width"));
		return pt(tbl.getAttribute("start-indent"));
	}

	private static String pathway(int flags) {
		return flags == Docx4J.FLAG_NONE ? "visitor: " : "XSLT: ";
	}

	@Test
	public void rightAlignedEndsOnTheColumnsEnd() throws Exception {
		for (int flags : FLAGS) {
			assertEquals(pathway(flags) + "right, mode 15", (COLUMN_TWIPS - 4500) / 20.0,
					startIndentPt(table("<w:jc w:val=\"right\"/>", 4500), flags, 15), 0.01);
			assertEquals(pathway(flags) + "end, w:tblInd 1440 ignored", (COLUMN_TWIPS - 4500) / 20.0,
					startIndentPt(table("<w:jc w:val=\"end\"/><w:tblInd w:w=\"1440\" w:type=\"dxa\"/>", 4500), flags, 15), 0.01);
			assertEquals(pathway(flags) + "right, w:tblInd -720 ignored", (COLUMN_TWIPS - 4500) / 20.0,
					startIndentPt(table("<w:jc w:val=\"right\"/><w:tblInd w:w=\"-720\" w:type=\"dxa\"/>", 4500), flags, 15), 0.01);
		}
	}

	@Test
	public void rightAlignedBelowMode15OverhangsByTheCellMargin() throws Exception {
		for (int flags : FLAGS) {
			// Word's text 309.2 in mode 14 against 303.7 in mode 15: one 108-twip cell margin
			assertEquals(pathway(flags), (COLUMN_TWIPS - 4500 + 108) / 20.0,
					startIndentPt(table("<w:jc w:val=\"right\"/>", 4500), flags, 14), 0.01);
		}
	}

	@Test
	public void overWideRightAlignedOverhangsTheLeftMargin() throws Exception {
		for (int flags : FLAGS) {
			assertEquals(pathway(flags), (COLUMN_TWIPS - 11001) / 20.0,
					startIndentPt(table("<w:jc w:val=\"right\"/>", 11001), flags, 15), 0.01);
		}
	}

	@Test
	public void startIsLeftAndKeepsItsIndent() throws Exception {
		for (int flags : FLAGS) {
			assertEquals(pathway(flags), 72.0,
					startIndentPt(table("<w:jc w:val=\"start\"/><w:tblInd w:w=\"1440\" w:type=\"dxa\"/>", 4500), flags, 15), 0.01);
		}
	}

	@Test
	public void rightFromTheTableStyle() throws Exception {
		String style = "<w:style w:type=\"table\" w:styleId=\"TableRight\"><w:name w:val=\"TableRight\"/>"
				+ "<w:basedOn w:val=\"TableNormal\"/><w:tblPr><w:jc w:val=\"right\"/></w:tblPr></w:style>";
		for (int flags : FLAGS) {
			assertEquals(pathway(flags), (COLUMN_TWIPS - 4500) / 20.0,
					startIndentPt(table("<w:tblStyle w:val=\"TableRight\"/>", 4500), flags, 15, style, 0), 0.01);
		}
	}

	@Test
	public void bidiVisualLeftForPhase3() throws Exception {
		for (int flags : FLAGS) {
			// Word puts this one at the LEFT margin (right = end in a right-to-left table);
			// CR-041 phase 3 places w:bidiVisual tables, and phase 1 must not move them
			assertEquals(pathway(flags), 0.0,
					startIndentPt(table("<w:bidiVisual/><w:jc w:val=\"right\"/>", 4500), flags, 15), 0.01);
		}
	}

	@Test
	public void nestedTableAlignedInItsCell() throws Exception {
		// the cell's content: the 451.3pt column less its two 5.4pt margins
		double content = COLUMN_TWIPS / 20.0 - 10.8;
		for (int flags : FLAGS) {
			assertEquals(pathway(flags) + "centred", (content - 200) / 2,
					startIndentPt(nested(table("<w:jc w:val=\"center\"/>", 4000)), flags, 15, null, 1), 0.05);
			assertEquals(pathway(flags) + "right", content - 200,
					startIndentPt(nested(table("<w:jc w:val=\"right\"/>", 4000)), flags, 15, null, 1), 0.05);
		}
	}
}
