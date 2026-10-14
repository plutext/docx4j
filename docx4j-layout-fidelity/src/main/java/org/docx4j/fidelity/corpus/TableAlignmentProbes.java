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
package org.docx4j.fidelity.corpus;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.STLineSpacingRule;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.docx4j.wml.Tbl;

/**
 * A survey of table content alignment and table width, after a report (2026-10-14) that
 * left, right and centre alignment in tables is broken in 17.x's PDF output, and that the
 * reporter's table widths came right once given in percent rather than inches.
 *
 * <p>One document, in compatibility modes 15 and 14 (the table-style rules of CR-030 differ
 * below mode 15, and so does a table's left edge); the mode 14 one without
 * {@code overrideTableStyleFontSizeAndJustification}, as older documents are.  Sections:</p>
 * <ul>
 * <li>A - a cell paragraph's direct {@code w:jc}: left, center, right, none, start, end, both,
 *     distribute;</li>
 * <li>B - its paragraph style's {@code w:jc}, and direct formatting over it;</li>
 * <li>C - the table style's {@code w:pPr/w:jc} (CR-030), against Normal, a paragraph style
 *     stating left (the [MS-DOCX] exception below mode 15), and direct formatting;</li>
 * <li>D - the table style's conditional formats (firstRow, firstCol, band1Horz) with
 *     {@code w:tblLook} on and off;</li>
 * <li>E - a cell's vertical alignment, direct and from the table style;</li>
 * <li>F - table widths: auto, dxa ("inches") and pct, with the grid and the cells agreeing
 *     and disagreeing, autofit and fixed;</li>
 * <li>G - the table's own position: {@code w:tblPr/w:jc} left, center, right, start, end,
 *     {@code w:tblInd}, pct and auto widths, and the table style's {@code w:jc};</li>
 * <li>H - merged cells; I - a nested table; J - right-to-left tables.</li>
 * </ul>
 *
 * <p>Every cell's text begins with its case id, so each line pairs with Word's by text.
 * The grid is the generator's, not Word's: Word recomputes an autofit grid on open (see the
 * README), which is exactly the situation of a document generated with docx4j and converted
 * without Word.</p>
 */
public final class TableAlignmentProbes {

	private static final String NS = " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** A4 with 1in margins. */
	private static final int TEXT_WIDTH = 11906 - 2 * 1440;

	private TableAlignmentProbes() {
	}

	static List<Probe> probes() {
		return Arrays.asList(probe(15), probe(14),
				phase0("compat15"), phase0("compat15-override0"), phase0("compat14"), phase0("compat12"));
	}

	/**
	 * CR-041 phase 0: what the survey left open.  W, the widths (proportions under a
	 * contradicting w:tblW, a grid content forces past w:tblW, the 2% threshold); P, the
	 * position (over-wide and indented right-aligned tables, w:jc end with w:tblInd, nested);
	 * B, w:bidiVisual; J, w:jc values in body paragraphs (start and end in a w:bidi paragraph,
	 * distribute, thaiDistribute, a kashida value); N, banding against first and last row and
	 * column formats; V, conditional w:vAlign.  Four documents: mode 15, mode 15 with
	 * overrideTableStyleFontSizeAndJustification stated 0, mode 14 without it, and mode 12
	 * (no w:compatSetting at all, as Word 2007 writes it).
	 */
	private static Probe phase0(String variant) {
		return new Probe("table-align-p0-" + variant,
				"CR-041 phase 0: table widths (proportions, forced grids, the 2% threshold), right-aligned, "
				+ "indented, nested and bidiVisual tables, w:jc start/end/distribute/thaiDistribute/kashida, "
				+ "banding against first/last row and column formats, conditional w:vAlign; " + variant,
				() -> buildPhase0(variant));
	}

	private static Probe probe(int mode) {
		return new Probe("table-align-survey-compat" + mode,
				"table content alignment and table widths (2026-10-14 report: alignment broken in 17.x, "
				+ "widths right in percent but not in inches): direct, paragraph-style and table-style "
				+ "w:jc in cells, conditional formats, vertical alignment, tblW auto/dxa/pct against the "
				+ "grid and the cells, the table's own w:jc and w:tblInd, merged cells, nesting, bidiVisual; "
				+ "compatibilityMode " + mode
				+ (mode < 15 ? " with overrideTableStyleFontSizeAndJustification absent" : ""), () -> build(mode));
	}

	/** Writes both documents into the directory given, for the share. */
	public static void main(String[] args) throws Exception {
		File dir = new File(args[0]);
		dir.mkdirs();
		for (Probe p : probes()) {
			File out = new File(dir, p.id + ".docx");
			Docx4J.save(p.build(), out);
			System.out.println("wrote " + out);
		}
	}

	private static WordprocessingMLPackage build(int mode) throws Exception {
		Doc d = Doc.create(mode);
		if (mode < 15) {
			// as a document from Word 2010 or an older template has it: the setting absent, so
			// the [MS-DOCX] 12pt-and-left exception is in play (CR-030 T6)
			org.docx4j.wml.CTCompat compat = d.mdp().getDocumentSettingsPart().getContents().getCompat();
			compat.getCompatSetting().removeIf(cs -> "overrideTableStyleFontSizeAndJustification".equals(cs.getName()));
		}
		d.documentDefaultRun(Doc.SERIF, 24);
		compactDefaults(d);
		addStyles(d);

		caption(d, "A: direct w:jc in each cell; w:tblW 100% (pct 5000), four columns, no table style");
		d.add(tbl(tblW("pct", 5000), grid(2256, 2256, 2256, 2256),
				tr(cell(2256, p(jc("left"), "A1 left")), cell(2256, p(jc("center"), "A1 centre")),
						cell(2256, p(jc("right"), "A1 right")), cell(2256, p("", "A1 none"))),
				tr(cell(2256, p(jc("start"), "A2 start")), cell(2256, p(jc("end"), "A2 end")),
						cell(2256, p(jc("both"), "A2 both: the quick brown fox jumps over the lazy dog again")),
						cell(2256, p(jc("distribute"), "A2 distribute: the quick brown fox jumps over it")))));

		caption(d, "B: paragraph styles CellLeft, CellCentre, CellRight (w:jc), then direct w:jc over them; TableGrid");
		d.add(tbl(style("TableGrid") + tblW("auto", 0), grid(3008, 3008, 3008),
				tr(cell(3008, p(pStyle("CellLeft"), "B1 CellLeft")),
						cell(3008, p(pStyle("CellCentre"), "B1 CellCentre")),
						cell(3008, p(pStyle("CellRight"), "B1 CellRight"))),
				tr(cell(3008, p(pStyle("CellRight") + jc("left"), "B2 CellRight, direct left")),
						cell(3008, p(pStyle("CellLeft") + jc("center"), "B2 CellLeft, direct centre")),
						cell(3008, p(pStyle("CellCentre") + jc("right"), "B2 CellCentre, direct right")))));

		caption(d, "C1: table style AlignCentre (w:pPr/w:jc center), Normal paragraphs");
		d.add(tbl(style("AlignCentre") + tblW("auto", 0), grid(3008, 3008, 3008),
				tr(cell(3008, p("", "C1 Normal a")), cell(3008, p("", "C1 Normal b")),
						cell(3008, p("", "C1 Normal c")))));
		caption(d, "C2: AlignCentre against CellLeft (states left), CellRight, and direct w:jc left");
		d.add(tbl(style("AlignCentre") + tblW("auto", 0), grid(3008, 3008, 3008),
				tr(cell(3008, p(pStyle("CellLeft"), "C2 CellLeft")),
						cell(3008, p(pStyle("CellRight"), "C2 CellRight")),
						cell(3008, p(jc("left"), "C2 direct left")))));
		caption(d, "C3: table style AlignRight (w:jc right) against Normal, CellLeft and CellCentre");
		d.add(tbl(style("AlignRight") + tblW("auto", 0), grid(3008, 3008, 3008),
				tr(cell(3008, p("", "C3 Normal")), cell(3008, p(pStyle("CellLeft"), "C3 CellLeft")),
						cell(3008, p(pStyle("CellCentre"), "C3 CellCentre")))));

		caption(d, "D1: AlignCond - firstRow centre and bold, firstCol right; w:tblLook firstRow and firstColumn on");
		d.add(conditionalTable("D1", "AlignCond", look(true, true, true), 3));
		caption(d, "D2: the same table style, w:tblLook firstRow off, firstColumn on");
		d.add(conditionalTable("D2", "AlignCond", look(false, true, true), 3));
		caption(d, "D3: AlignBand - band1Horz right, row band size 1; w:tblLook firstRow on, row bands on");
		d.add(conditionalTable("D3", "AlignBand", look(true, false, false), 4));

		caption(d, "E1: w:vAlign top, center, bottom, none (with w:jc center); rows at least 0.75in");
		d.add(tbl(tblW("pct", 5000), grid(2256, 2256, 2256, 2256),
				tr(trHeight(1080), cell(2256, vAlign("top"), p("", "E1 top")),
						cell(2256, vAlign("center"), p("", "E1 centre")),
						cell(2256, vAlign("bottom"), p("", "E1 bottom")),
						cell(2256, "", p(jc("center"), "E1 none, jc centre")))));
		caption(d, "E2: table style VCentre (w:tcPr/w:vAlign center, firstRow bottom); last cell direct top");
		d.add(tbl(style("VCentre") + tblW("pct", 5000) + look(true, false, false), grid(3008, 3008, 3008),
				tr(trHeight(1080), cell(3008, p("", "E2 header a")), cell(3008, p("", "E2 header b")),
						cell(3008, vAlign("top"), p("", "E2 header direct top"))),
				tr(trHeight(1080), cell(3008, p("", "E2 body a")), cell(3008, p("", "E2 body b")),
						cell(3008, vAlign("top"), p("", "E2 body direct top")))));

		d.pageBreak();
		widthCase(d, "F1", "w:tblW auto; grid and w:tcW 3 x 2000 dxa",
				tblW("auto", 0), new int[] { 2000, 2000, 2000 }, "dxa", 2000, 2000, 2000);
		widthCase(d, "F2", "w:tblW 6000 dxa; grid and w:tcW 3 x 2000 dxa (all agree)",
				tblW("dxa", 6000), new int[] { 2000, 2000, 2000 }, "dxa", 2000, 2000, 2000);
		widthCase(d, "F3", "w:tblW " + TEXT_WIDTH + " dxa (the text width in twips); grid and w:tcW 3 x 3009 (agree)",
				tblW("dxa", TEXT_WIDTH), new int[] { 3009, 3009, 3009 }, "dxa", 3009, 3009, 3009);
		widthCase(d, "F4", "w:tblW 100% (pct 5000); grid and w:tcW 3 x 3009 (agree)",
				tblW("pct", 5000), new int[] { 3009, 3009, 3009 }, "dxa", 3009, 3009, 3009);
		widthCase(d, "F5", "w:tblW 100% (pct 5000); grid and w:tcW 3 x 2000 (narrower)",
				tblW("pct", 5000), new int[] { 2000, 2000, 2000 }, "dxa", 2000, 2000, 2000);
		widthCase(d, "F6", "w:tblW " + TEXT_WIDTH + " dxa; grid and w:tcW 3 x 2000 (narrower)",
				tblW("dxa", TEXT_WIDTH), new int[] { 2000, 2000, 2000 }, "dxa", 2000, 2000, 2000);
		widthCase(d, "F7", "w:tblW " + TEXT_WIDTH + " dxa; w:tcW 3 x 3009 (agree), grid 3 x 2000 (stale)",
				tblW("dxa", TEXT_WIDTH), new int[] { 2000, 2000, 2000 }, "dxa", 3009, 3009, 3009);
		widthCase(d, "F8", "w:tblW 6000 dxa; w:tcW 3 x 2000 (agree), grid 3 x 3009 (stale, wider)",
				tblW("dxa", 6000), new int[] { 3009, 3009, 3009 }, "dxa", 2000, 2000, 2000);
		widthCase(d, "F9", "w:tblW 50% (pct 2500); w:tcW 3 x 33% (pct 1667); grid 3 x 2000",
				tblW("pct", 2500), new int[] { 2000, 2000, 2000 }, "pct", 1667, 1667, 1667);
		widthCase(d, "F10", "fixed layout; w:tblW 6000 dxa; grid and w:tcW 3 x 2000 (agree)",
				tblW("dxa", 6000) + FIXED, new int[] { 2000, 2000, 2000 }, "dxa", 2000, 2000, 2000);
		widthCase(d, "F11", "fixed layout; w:tblW " + TEXT_WIDTH + " dxa; grid and w:tcW 3 x 2000 (narrower)",
				tblW("dxa", TEXT_WIDTH) + FIXED, new int[] { 2000, 2000, 2000 }, "dxa", 2000, 2000, 2000);
		widthCase(d, "F12", "fixed layout; w:tblW 100% (pct 5000); grid and w:tcW 3 x 2000 (narrower)",
				tblW("pct", 5000) + FIXED, new int[] { 2000, 2000, 2000 }, "dxa", 2000, 2000, 2000);
		widthCase(d, "F13", "w:tblW 11000 dxa (wider than the text); grid and w:tcW 3 x 3667",
				tblW("dxa", 11000), new int[] { 3667, 3667, 3667 }, "dxa", 3667, 3667, 3667);
		widthCase(d, "F14", "w:tblW " + TEXT_WIDTH + " dxa; grid and w:tcW 1000, 3000, 5026 (unequal, agree)",
				tblW("dxa", TEXT_WIDTH), new int[] { 1000, 3000, 5026 }, "dxa", 1000, 3000, 5026);
		widthCase(d, "F15", "w:tblW 100% (pct 5000); w:tcW 20%, 30%, 50% (pct); grid 3 x 3009",
				tblW("pct", 5000), new int[] { 3009, 3009, 3009 }, "pct", 1000, 1500, 2500);
		widthCase(d, "F16", "no w:tblW and no w:tcW at all; grid 3 x 2000",
				"", new int[] { 2000, 2000, 2000 }, null, 0, 0, 0);

		d.pageBreak();
		String narrow = tblW("dxa", 4500);
		positionCase(d, "G1", "no w:jc; w:tblW 4500 dxa", narrow);
		positionCase(d, "G2", "w:jc left", narrow + tblJc("left"));
		positionCase(d, "G3", "w:jc center", narrow + tblJc("center"));
		positionCase(d, "G4", "w:jc right", narrow + tblJc("right"));
		positionCase(d, "G5", "w:jc start", narrow + tblJc("start"));
		positionCase(d, "G6", "w:jc end", narrow + tblJc("end"));
		positionCase(d, "G7", "w:tblInd 1440, no w:jc", narrow + tblInd(1440));
		positionCase(d, "G8", "w:tblInd 1440 and w:jc right", narrow + tblJc("right") + tblInd(1440));
		positionCase(d, "G9", "w:tblInd 1440 and w:jc center", narrow + tblJc("center") + tblInd(1440));
		positionCase(d, "G10", "w:jc center; w:tblW 50% (pct 2500)", tblW("pct", 2500) + tblJc("center"));
		positionCase(d, "G11", "w:jc right; w:tblW 50% (pct 2500)", tblW("pct", 2500) + tblJc("right"));
		positionCase(d, "G12", "w:jc center; w:tblW auto", tblW("auto", 0) + tblJc("center"));
		positionCase(d, "G13", "w:jc right; w:tblW auto", tblW("auto", 0) + tblJc("right"));
		positionCase(d, "G14", "table style TableCentred (w:tblPr/w:jc center)", style("TableCentred") + narrow);
		positionCase(d, "G15", "table style TableRight (w:tblPr/w:jc right)", style("TableRight") + narrow);
		positionCase(d, "G16", "TableCentred, direct w:jc left", style("TableCentred") + narrow + tblJc("left"));

		d.pageBreak();
		caption(d, "H: a w:gridSpan 2 cell centred; a w:vMerge cell right-aligned and vertically centred");
		d.add(tbl(tblW("pct", 5000), grid(3008, 3008, 3008),
				tr(cell(6016, gridSpan(2), p(jc("center"), "H1 span of two, centre")),
						cell(3008, p(jc("right"), "H1 right"))),
				tr(trHeight(720), cell(3008, vMerge(true) + vAlign("center"), p(jc("right"), "H2 merged, right, centre")),
						cell(3008, p("", "H2 b")), cell(3008, p("", "H2 c"))),
				tr(trHeight(720), cell(3008, vMerge(false), p("", "")),
						cell(3008, p("", "H3 b")), cell(3008, p("", "H3 c")))));

		caption(d, "I: a table nested in a cell, w:jc center and 4000 dxa wide, its cells right-aligned");
		String inner = tblXml(tblW("dxa", 4000) + tblJc("center"), grid(2000, 2000),
				tr(cell(2000, p(jc("right"), "I2 inner a")), cell(2000, p(jc("right"), "I2 inner b"))));
		d.add(tbl(tblW("pct", 5000), grid(TEXT_WIDTH),
				tr("<w:tc><w:tcPr>" + tcW("dxa", TEXT_WIDTH) + "</w:tcPr>" + p(jc("center"), "I1 outer, centre")
						+ inner + p("", "I3 outer, after") + "</w:tc>")));

		caption(d, "J1: w:bidiVisual, w:tblW 6000 dxa, no w:jc; cells w:jc left, center, right; row 2 w:bidi paragraphs");
		d.add(tbl(BIDI + tblW("dxa", 6000), grid(2000, 2000, 2000),
				tr(cell(2000, p(jc("left"), "J1 left")), cell(2000, p(jc("center"), "J1 centre")),
						cell(2000, p(jc("right"), "J1 right"))),
				tr(cell(2000, p("<w:bidi/>" + jc("left"), "J2 bidi left")),
						cell(2000, p("<w:bidi/>" + jc("center"), "J2 bidi centre")),
						cell(2000, p("<w:bidi/>" + jc("right"), "J2 bidi right")))));
		caption(d, "J3: w:bidiVisual with w:jc left");
		d.add(tbl(BIDI + tblW("dxa", 6000) + tblJc("left"), grid(2000, 2000, 2000),
				tr(cell(2000, p("", "J3 a")), cell(2000, p("", "J3 b")), cell(2000, p("", "J3 c")))));
		caption(d, "J4: w:bidiVisual with w:jc right");
		d.add(tbl(BIDI + tblW("dxa", 6000) + tblJc("right"), grid(2000, 2000, 2000),
				tr(cell(2000, p("", "J4 a")), cell(2000, p("", "J4 b")), cell(2000, p("", "J4 c")))));
		d.para("after.").before(240).add();
		return d.pkg();
	}

	private static WordprocessingMLPackage buildPhase0(String variant) throws Exception {
		Doc d = Doc.create(variant.startsWith("compat15") ? 15 : variant.equals("compat14") ? 14 : 15);
		org.docx4j.wml.CTCompat compat = d.mdp().getDocumentSettingsPart().getContents().getCompat();
		if (variant.equals("compat12")) {
			compat.getCompatSetting().clear();
		} else if (variant.equals("compat14")) {
			compat.getCompatSetting().removeIf(cs -> "overrideTableStyleFontSizeAndJustification".equals(cs.getName()));
		} else if (variant.equals("compat15-override0")) {
			for (org.docx4j.wml.CTCompatSetting cs : compat.getCompatSetting()) {
				if ("overrideTableStyleFontSizeAndJustification".equals(cs.getName())) cs.setVal("0");
			}
		}
		d.documentDefaultRun(Doc.SERIF, 24);
		compactDefaults(d);
		addStyles(d);
		addPhase0Styles(d);

		// W - widths.  Cells left / centre / right aligned, as in section F.
		widthCase(d, "W1", "w:tblW " + TEXT_WIDTH + " dxa; grid 1000/2000/3000; w:tcW 3000/2000/1000 (both unequal, opposite)",
				tblW("dxa", TEXT_WIDTH), new int[] { 1000, 2000, 3000 }, null, 0, 0, 0, new int[] { 3000, 2000, 1000 });
		widthCase(d, "W2", "w:tblW " + TEXT_WIDTH + " dxa; grid and w:tcW 1000/2000/3000 (agree, short of the table)",
				tblW("dxa", TEXT_WIDTH), new int[] { 1000, 2000, 3000 }, "dxa", 1000, 2000, 3000);
		widthCase(d, "W3", "w:tblW " + TEXT_WIDTH + " dxa; grid 1000/2000/3000; no w:tcW",
				tblW("dxa", TEXT_WIDTH), new int[] { 1000, 2000, 3000 }, null, 0, 0, 0);
		caption(d, "W4: w:tblW 4000 dxa; grid and w:tcW 3 x 2500; each cell holds a word about 2300 twips wide");
		d.add(tbl(tblW("dxa", 4000), grid(2500, 2500, 2500),
				tr(cell(2500, p("", "W4a Antidisestablishment")), cell(2500, p("", "W4b Antidisestablishment")),
						cell(2500, p("", "W4c Antidisestablishment")))));
		widthCase(d, "W5", "w:tblW 4000 dxa; grid 3 x 2500; no w:tcW; short text",
				tblW("dxa", 4000), new int[] { 2500, 2500, 2500 }, null, 0, 0, 0);
		widthCase(d, "W6", "fixed layout; w:tblW " + TEXT_WIDTH + " dxa; grid 1000/2000/3000; w:tcW 3000/2000/1000",
				tblW("dxa", TEXT_WIDTH) + FIXED, new int[] { 1000, 2000, 3000 }, null, 0, 0, 0, new int[] { 3000, 2000, 1000 });
		widthCase(d, "W7", "w:tblW 100% (pct 5000); grid 1000/2000/3000; no w:tcW",
				tblW("pct", 5000), new int[] { 1000, 2000, 3000 }, null, 0, 0, 0);
		caption(d, "W8: w:tblW " + TEXT_WIDTH + " dxa; grid 3 x 2000; w:tcW 2000, auto, 2000 (mixed)");
		d.add(tbl(tblW("dxa", TEXT_WIDTH), grid(2000, 2000, 2000),
				tr(cell("dxa", 2000, "", p(jc("left"), "W8 left")), cell("auto", 0, "", p(jc("center"), "W8 centre")),
						cell("dxa", 2000, "", p(jc("right"), "W8 right")))));
		widthCase(d, "W9", "w:tblW " + TEXT_WIDTH + " dxa; grid 3 x 2000; w:tcW 20%, 30%, 50% (pct)",
				tblW("dxa", TEXT_WIDTH), new int[] { 2000, 2000, 2000 }, null, 0, 0, 0, null, new int[] { 1000, 1500, 2500 });
		widthCase(d, "W10", "w:tblW 100% (pct 5000); grid and w:tcW 3 x 3081 (= 9243, the column plus two cell margins)",
				tblW("pct", 5000), new int[] { 3081, 3081, 3081 }, "dxa", 3081, 3081, 3081);
		widthCase(d, "W11", "w:tblW 9026 dxa; grid and w:tcW 3 x 3067 (= 9201, 1.9% over)",
				tblW("dxa", TEXT_WIDTH), new int[] { 3067, 3067, 3067 }, "dxa", 3067, 3067, 3067);
		widthCase(d, "W12", "w:tblW 9026 dxa; grid and w:tcW 3 x 3100 (= 9300, 3.0% over)",
				tblW("dxa", TEXT_WIDTH), new int[] { 3100, 3100, 3100 }, "dxa", 3100, 3100, 3100);
		widthCase(d, "W13", "w:tblW 9026 dxa; grid and w:tcW 3 x 2950 (= 8850, 1.9% under)",
				tblW("dxa", TEXT_WIDTH), new int[] { 2950, 2950, 2950 }, "dxa", 2950, 2950, 2950);
		widthCase(d, "W14", "w:tblW 9026 dxa; grid and w:tcW 3 x 2920 (= 8760, 2.9% under)",
				tblW("dxa", TEXT_WIDTH), new int[] { 2920, 2920, 2920 }, "dxa", 2920, 2920, 2920);

		d.pageBreak();
		String narrow = tblW("dxa", 4500);
		caption(d, "P1: w:jc right; w:tblW 11000 dxa (wider than the column); grid 3 x 3667");
		d.add(tbl(tblW("dxa", 11000) + tblJc("right"), grid(3667, 3667, 3667),
				tr(cell(3667, p("", "P1 a")), cell(3667, p("", "P1 b")), cell(3667, p("", "P1 c")))));
		positionCase(d, "P2", "w:jc right and w:tblInd -720", narrow + tblJc("right") + tblInd(-720));
		positionCase(d, "P3", "w:jc end and w:tblInd 1440", narrow + tblJc("end") + tblInd(1440));
		positionCase(d, "P4", "w:jc center and w:tblInd -720", narrow + tblJc("center") + tblInd(-720));
		positionCase(d, "P5", "w:jc start and w:tblInd 1440", narrow + tblJc("start") + tblInd(1440));
		caption(d, "P6: a table nested in a cell, w:jc right and 4000 dxa wide");
		d.add(nestedCase("P6", tblJc("right")));
		caption(d, "P7: a table nested in a cell, w:jc center and 4000 dxa wide");
		d.add(nestedCase("P7", tblJc("center")));

		caption(d, "B1: w:bidiVisual, w:tblW 6000 dxa, w:tblInd 1440, no w:jc");
		d.add(bidiCase("B1", tblInd(1440)));
		caption(d, "B2: w:bidiVisual with w:jc center");
		d.add(bidiCase("B2", tblJc("center")));
		caption(d, "B3: w:bidiVisual with w:jc start");
		d.add(bidiCase("B3", tblJc("start")));
		caption(d, "B4: w:bidiVisual with w:jc end");
		d.add(bidiCase("B4", tblJc("end")));
		caption(d, "B5: w:bidiVisual with w:jc right and w:tblInd 1440");
		d.add(bidiCase("B5", tblJc("right") + tblInd(1440)));

		d.pageBreak();
		caption(d, "J1 to J4: body paragraphs with w:bidi and w:jc start, end, left, right");
		d.add(bodyP("<w:bidi/>" + jc("start"), "J1 bidi start"));
		d.add(bodyP("<w:bidi/>" + jc("end"), "J2 bidi end"));
		d.add(bodyP("<w:bidi/>" + jc("left"), "J3 bidi left"));
		d.add(bodyP("<w:bidi/>" + jc("right"), "J4 bidi right"));
		caption(d, "J5 to J7: distribute on one line, on several, and thaiDistribute");
		d.add(bodyP(jc("distribute"), "J5 distribute, one short line"));
		d.add(bodyP(jc("distribute"), "J6 distribute: " + Doc.prose(2)));
		d.add(bodyP(jc("thaiDistribute"), "J7 thaiDistribute, one short line"));
		caption(d, "J8: w:jc mediumKashida in a w:bidi paragraph of Arabic (Arial)");
		d.add(arabicP("J8", "mediumKashida"));
		caption(d, "J9: the same Arabic paragraph with w:jc both (control)");
		d.add(arabicP("J9", "both"));

		d.pageBreak();
		caption(d, "N1: BandHdr - firstRow bold, band1Horz right; firstRow on, row bands on");
		d.add(bandTable("N1", "BandHdr", look6(true, false, false, false, false, true), 4));
		caption(d, "N2: BandEmptyHdr - an empty firstRow w:tblStylePr, band1Horz right; firstRow on, row bands on");
		d.add(bandTable("N2", "BandEmptyHdr", look6(true, false, false, false, false, true), 4));
		caption(d, "N3: AlignBand (no lastRow format), lastRow on, firstRow off, row bands on");
		d.add(bandTable("N3", "AlignBand", look6(false, true, false, false, false, true), 4));
		caption(d, "N4: BandLast - lastRow bold, band1Horz right; lastRow on, row bands on");
		d.add(bandTable("N4", "BandLast", look6(false, true, false, false, false, true), 4));
		caption(d, "N5: BandCol - band1Vert right, column band size 1, no firstCol format; firstColumn on, column bands on");
		d.add(bandTable("N5", "BandCol", look6(false, false, true, false, true, false), 3));
		caption(d, "N6: BandColFirst - firstCol bold, band1Vert right; firstColumn on, column bands on");
		d.add(bandTable("N6", "BandColFirst", look6(false, false, true, false, true, false), 3));

		caption(d, "V1: VCondOnly - firstRow w:vAlign bottom, no whole-table w:vAlign; rows at least 0.75in");
		d.add(vTable("V1", "VCondOnly"));
		caption(d, "V2: VCentre - whole table w:vAlign center, firstRow bottom");
		d.add(vTable("V2", "VCentre"));
		caption(d, "V3: VCondShd - whole table center, firstRow bottom and shaded");
		d.add(vTable("V3", "VCondShd"));
		caption(d, "V4: ShdOnly - firstRow shaded, no w:vAlign anywhere");
		d.add(vTable("V4", "ShdOnly"));
		caption(d, "V5: VTop - whole table w:vAlign top, firstRow bottom");
		d.add(vTable("V5", "VTop"));
		d.para("after.").before(240).add();
		return d.pkg();
	}

	private static Tbl nestedCase(String id, String innerTblPr) throws Exception {
		String inner = tblXml(tblW("dxa", 4000) + innerTblPr, grid(2000, 2000),
				tr(cell(2000, p("", id + " inner a")), cell(2000, p("", id + " inner b"))));
		return tbl(tblW("pct", 5000), grid(TEXT_WIDTH),
				tr("<w:tc><w:tcPr>" + tcW("dxa", TEXT_WIDTH) + "</w:tcPr>" + p("", id + " outer")
						+ inner + p("", id + " outer, after") + "</w:tc>"));
	}

	private static Tbl bidiCase(String id, String extra) throws Exception {
		return tbl(BIDI + tblW("dxa", 6000) + extra, grid(2000, 2000, 2000),
				tr(cell(2000, p("", id + " a")), cell(2000, p("", id + " b")), cell(2000, p("", id + " c"))));
	}

	private static Tbl bandTable(String id, String styleId, String look, int rows) throws Exception {
		String[] row = new String[rows];
		for (int r = 0; r < rows; r++) {
			row[r] = tr(cell(3008, p("", id + " r" + (r + 1) + "c1")), cell(3008, p("", id + " r" + (r + 1) + "c2")),
					cell(3008, p("", id + " r" + (r + 1) + "c3")));
		}
		return tbl(style(styleId) + tblW("auto", 0) + look, grid(3008, 3008, 3008), row);
	}

	private static Tbl vTable(String id, String styleId) throws Exception {
		return tbl(style(styleId) + tblW("pct", 5000) + look(true, false, true), grid(3008, 3008, 3008),
				tr(trHeight(1080), cell(3008, p("", id + " header a")), cell(3008, p("", id + " header b")),
						cell(3008, vAlign("top"), p("", id + " header direct top"))),
				tr(trHeight(1080), cell(3008, p("", id + " body a")), cell(3008, p("", id + " body b")),
						cell(3008, vAlign("top"), p("", id + " body direct top"))));
	}

	/** A body paragraph (no label: its text starts with its case id). */
	private static org.docx4j.wml.P bodyP(String pPr, String text) throws Exception {
		return (org.docx4j.wml.P) XmlUtils.unmarshalString(p(pPr, text).replaceFirst("<w:p>", "<w:p" + NS + ">"));
	}

	private static org.docx4j.wml.P arabicP(String id, String jcVal) throws Exception {
		String arabic = "\u0647\u0630\u0627 \u0646\u0635 \u0639\u0631\u0628\u064a \u0644\u0644\u0627\u062e\u062a\u0628\u0627\u0631 ";
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < 9; i++) text.append(arabic);
		String rPr = "<w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:rtl/><w:sz w:val=\"24\"/><w:szCs w:val=\"24\"/></w:rPr>";
		String xml = "<w:p" + NS + "><w:pPr><w:bidi/>" + jc(jcVal) + "</w:pPr>"
				+ "<w:r><w:t xml:space=\"preserve\">" + id + " </w:t></w:r>"
				+ "<w:r>" + rPr + "<w:t xml:space=\"preserve\">" + text.toString().trim() + "</w:t></w:r></w:p>";
		return (org.docx4j.wml.P) XmlUtils.unmarshalString(xml);
	}

	private static void addPhase0Styles(Doc d) throws Exception {
		String band1Horz = "<w:tblStylePr w:type=\"band1Horz\"><w:pPr>" + jc("right") + "</w:pPr></w:tblStylePr>";
		String band1Vert = "<w:tblStylePr w:type=\"band1Vert\"><w:pPr>" + jc("right") + "</w:pPr></w:tblStylePr>";
		String rowBand = "<w:tblPr><w:tblStyleRowBandSize w:val=\"1\"/></w:tblPr>";
		String colBand = "<w:tblPr><w:tblStyleColBandSize w:val=\"1\"/></w:tblPr>";
		String bold = "<w:rPr><w:b/></w:rPr>";
		String xml = "<w:styles" + NS + ">"
				+ tableStyle("BandHdr", rowBand + "<w:tblStylePr w:type=\"firstRow\">" + bold + "</w:tblStylePr>" + band1Horz)
				+ tableStyle("BandEmptyHdr", rowBand + "<w:tblStylePr w:type=\"firstRow\"/>" + band1Horz)
				+ tableStyle("BandLast", rowBand + "<w:tblStylePr w:type=\"lastRow\">" + bold + "</w:tblStylePr>" + band1Horz)
				+ tableStyle("BandCol", colBand + band1Vert)
				+ tableStyle("BandColFirst", colBand + "<w:tblStylePr w:type=\"firstCol\">" + bold + "</w:tblStylePr>" + band1Vert)
				+ tableStyle("VCondOnly", "<w:tblStylePr w:type=\"firstRow\"><w:tcPr><w:vAlign w:val=\"bottom\"/></w:tcPr></w:tblStylePr>")
				+ tableStyle("VCondShd", "<w:tcPr><w:vAlign w:val=\"center\"/></w:tcPr>"
						+ "<w:tblStylePr w:type=\"firstRow\"><w:tcPr><w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"D9D9D9\"/>"
						+ "<w:vAlign w:val=\"bottom\"/></w:tcPr></w:tblStylePr>")
				+ tableStyle("ShdOnly", "<w:tblStylePr w:type=\"firstRow\"><w:tcPr><w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"D9D9D9\"/></w:tcPr></w:tblStylePr>")
				+ tableStyle("VTop", "<w:tcPr><w:vAlign w:val=\"top\"/></w:tcPr>"
						+ "<w:tblStylePr w:type=\"firstRow\"><w:tcPr><w:vAlign w:val=\"bottom\"/></w:tcPr></w:tblStylePr>")
				+ "</w:styles>";
		Styles mine = (Styles) XmlUtils.unmarshalString(xml);
		d.mdp().getStyleDefinitionsPart().getJaxbElement().getStyle().addAll(mine.getStyle());
	}

	// ------------------------------------------------------------------ the cases

	/** A labelled caption paragraph before a case. */
	private static void caption(Doc d, String text) {
		d.para(text).before(240).after(60).add();
	}

	/** As {@link #widthCase(Doc, String, String, String, int[], String, int, int, int)}, the cells'
	 *  w:tcW given as dxa widths, or (where those are null) as pct widths. */
	private static void widthCase(Doc d, String id, String what, String tblPr, int[] grid, String unused,
			int u1, int u2, int u3, int[] dxa) throws Exception {
		widthCase(d, id, what, tblPr, grid, unused, u1, u2, u3, dxa, null);
	}

	private static void widthCase(Doc d, String id, String what, String tblPr, int[] grid, String unused,
			int u1, int u2, int u3, int[] dxa, int[] pct) throws Exception {
		if (dxa == null && pct == null) {
			widthCase(d, id, what, tblPr, grid, null, 0, 0, 0);
			return;
		}
		String type = dxa != null ? "dxa" : "pct";
		int[] w = dxa != null ? dxa : pct;
		caption(d, id + ": " + what);
		d.add(tbl(tblPr, grid(grid),
				tr(cell(type, w[0], "", p(jc("left"), id + " left")),
						cell(type, w[1], "", p(jc("center"), id + " centre")),
						cell(type, w[2], "", p(jc("right"), id + " right")))));
	}

	/** Three columns whose cells are left, centre and right aligned: what width does the table take? */
	private static void widthCase(Doc d, String id, String what, String tblPr, int[] grid, String tcWType,
			int w1, int w2, int w3) throws Exception {
		caption(d, id + ": " + what);
		d.add(tbl(tblPr, grid(grid),
				tr(cell(tcWType, w1, "", p(jc("left"), id + " left")),
						cell(tcWType, w2, "", p(jc("center"), id + " centre")),
						cell(tcWType, w3, "", p(jc("right"), id + " right")))));
	}

	/** A narrow table of three 1500-twip columns, its cells left-aligned: where does the table sit? */
	private static void positionCase(Doc d, String id, String what, String tblPr) throws Exception {
		caption(d, id + ": " + what);
		d.add(tbl(tblPr, grid(1500, 1500, 1500),
				tr(cell(1500, p("", id + " a")), cell(1500, p("", id + " b")), cell(1500, p("", id + " c")))));
	}

	/** A table of the given style and look, three columns, the given number of rows. */
	private static Tbl conditionalTable(String id, String styleId, String look, int rows) throws Exception {
		String[] row = new String[rows];
		for (int r = 0; r < rows; r++) {
			row[r] = tr(cell(3008, p("", id + " r" + (r + 1) + "c1")), cell(3008, p("", id + " r" + (r + 1) + "c2")),
					cell(3008, p("", id + " r" + (r + 1) + "c3")));
		}
		return tbl(style(styleId) + tblW("auto", 0) + look, grid(3008, 3008, 3008), row);
	}

	// ------------------------------------------------------------------ styles

	private static void compactDefaults(Doc d) {
		Styles styles = d.mdp().getStyleDefinitionsPart().getJaxbElement();
		PPrBase.Spacing sp = Doc.F.createPPrBaseSpacing();
		sp.setBefore(BigInteger.ZERO);
		sp.setAfter(BigInteger.ZERO);
		sp.setLine(BigInteger.valueOf(240));
		sp.setLineRule(STLineSpacingRule.AUTO);
		styles.getDocDefaults().getPPrDefault().getPPr().setSpacing(sp);
	}

	private static void addStyles(Doc d) throws Exception {
		String xml = "<w:styles" + NS + ">"
				+ paragraphStyle("CellLeft", "left")
				+ paragraphStyle("CellCentre", "center")
				+ paragraphStyle("CellRight", "right")
				+ tableStyle("AlignCentre", "<w:pPr>" + jc("center") + "</w:pPr>")
				+ tableStyle("AlignRight", "<w:pPr>" + jc("right") + "</w:pPr>")
				+ tableStyle("AlignCond",
						"<w:tblStylePr w:type=\"firstRow\"><w:pPr>" + jc("center") + "</w:pPr><w:rPr><w:b/></w:rPr></w:tblStylePr>"
						+ "<w:tblStylePr w:type=\"firstCol\"><w:pPr>" + jc("right") + "</w:pPr></w:tblStylePr>")
				+ tableStyle("AlignBand", "<w:tblPr><w:tblStyleRowBandSize w:val=\"1\"/></w:tblPr>"
						+ "<w:tblStylePr w:type=\"band1Horz\"><w:pPr>" + jc("right") + "</w:pPr></w:tblStylePr>")
				+ tableStyle("VCentre", "<w:tcPr><w:vAlign w:val=\"center\"/></w:tcPr>"
						+ "<w:tblStylePr w:type=\"firstRow\"><w:tcPr><w:vAlign w:val=\"bottom\"/></w:tcPr></w:tblStylePr>")
				+ tableStyle("TableCentred", "<w:tblPr>" + tblJc("center") + "</w:tblPr>")
				+ tableStyle("TableRight", "<w:tblPr>" + tblJc("right") + "</w:tblPr>")
				+ "</w:styles>";
		Styles mine = (Styles) XmlUtils.unmarshalString(xml);
		List<Style> styles = d.mdp().getStyleDefinitionsPart().getJaxbElement().getStyle();
		styles.addAll(mine.getStyle());
	}

	private static String paragraphStyle(String id, String jc) {
		return "<w:style w:type=\"paragraph\" w:customStyle=\"1\" w:styleId=\"" + id + "\"><w:name w:val=\"" + id
				+ "\"/><w:basedOn w:val=\"Normal\"/><w:qFormat/><w:pPr>" + jc(jc) + "</w:pPr></w:style>";
	}

	/** A table style based on TableNormal (so its cell margins are Word's 108); borders are on each table. */
	private static String tableStyle(String id, String body) {
		return "<w:style w:type=\"table\" w:customStyle=\"1\" w:styleId=\"" + id + "\"><w:name w:val=\"" + id
				+ "\"/><w:basedOn w:val=\"TableNormal\"/>" + body + "</w:style>";
	}

	// ------------------------------------------------------------------ markup

	private static final String FIXED = "<w:tblLayout w:type=\"fixed\"/>";
	private static final String BIDI = "<w:bidiVisual/>";

	private static String jc(String val) {
		return "<w:jc w:val=\"" + val + "\"/>";
	}

	/** The table's own w:jc (same element as a paragraph's; kept apart for reading). */
	private static String tblJc(String val) {
		return jc(val);
	}

	private static String pStyle(String id) {
		return "<w:pStyle w:val=\"" + id + "\"/>";
	}

	private static String style(String id) {
		return "<w:tblStyle w:val=\"" + id + "\"/>";
	}

	private static String tblW(String type, int w) {
		return "<w:tblW w:w=\"" + w + "\" w:type=\"" + type + "\"/>";
	}

	private static String tcW(String type, int w) {
		return "<w:tcW w:w=\"" + w + "\" w:type=\"" + type + "\"/>";
	}

	private static String tblInd(int twips) {
		return "<w:tblInd w:w=\"" + twips + "\" w:type=\"dxa\"/>";
	}

	private static String vAlign(String val) {
		return "<w:vAlign w:val=\"" + val + "\"/>";
	}

	private static String gridSpan(int n) {
		return "<w:gridSpan w:val=\"" + n + "\"/>";
	}

	private static String vMerge(boolean restart) {
		return restart ? "<w:vMerge w:val=\"restart\"/>" : "<w:vMerge/>";
	}

	private static String trHeight(int twips) {
		return "<w:trPr><w:trHeight w:val=\"" + twips + "\" w:hRule=\"atLeast\"/></w:trPr>";
	}

	private static String look(boolean firstRow, boolean firstColumn, boolean noHBand) {
		return "<w:tblLook w:firstRow=\"" + (firstRow ? 1 : 0) + "\" w:lastRow=\"0\" w:firstColumn=\""
				+ (firstColumn ? 1 : 0) + "\" w:lastColumn=\"0\" w:noHBand=\"" + (noHBand ? 1 : 0)
				+ "\" w:noVBand=\"1\"/>";
	}

	private static String look6(boolean firstRow, boolean lastRow, boolean firstColumn, boolean lastColumn,
			boolean noHBand, boolean noVBand) {
		return "<w:tblLook w:firstRow=\"" + (firstRow ? 1 : 0) + "\" w:lastRow=\"" + (lastRow ? 1 : 0)
				+ "\" w:firstColumn=\"" + (firstColumn ? 1 : 0) + "\" w:lastColumn=\"" + (lastColumn ? 1 : 0)
				+ "\" w:noHBand=\"" + (noHBand ? 1 : 0) + "\" w:noVBand=\"" + (noVBand ? 1 : 0) + "\"/>";
	}

	private static int[] grid(int... widths) {
		return widths;
	}

	/** A paragraph of one w:rPr-less run (the document defaults' Liberation Serif 12pt). */
	private static String p(String pPr, String text) {
		return "<w:p>" + (pPr.isEmpty() ? "" : "<w:pPr>" + pPr + "</w:pPr>")
				+ (text.isEmpty() ? "" : "<w:r><w:t xml:space=\"preserve\">" + text + "</w:t></w:r>") + "</w:p>";
	}

	private static String cell(int dxa, String paragraph) {
		return cell("dxa", dxa, "", paragraph);
	}

	private static String cell(int dxa, String tcPr, String paragraph) {
		return cell("dxa", dxa, tcPr, paragraph);
	}

	/** A cell; no w:tcW where the type is null. */
	private static String cell(String tcWType, int w, String tcPr, String paragraph) {
		String props = (tcWType == null ? "" : tcW(tcWType, w)) + tcPr;
		return "<w:tc>" + (props.isEmpty() ? "" : "<w:tcPr>" + props + "</w:tcPr>") + paragraph + "</w:tc>";
	}

	/** A row; a first argument starting {@code <w:trPr>} is the row's properties. */
	private static String tr(String... cells) {
		return "<w:tr>" + String.join("", cells) + "</w:tr>";
	}

	private static Tbl tbl(String tblPr, int[] grid, String... rows) throws Exception {
		return (Tbl) XmlUtils.unwrap(XmlUtils.unmarshalString(tblXml(tblPr, grid, rows)));
	}

	/** A table with single borders all round and inside, whatever its style. */
	private static String tblXml(String tblPr, int[] grid, String... rows) {
		StringBuilder x = new StringBuilder("<w:tbl" + NS + "><w:tblPr>").append(tblPr).append("<w:tblBorders>");
		for (String side : new String[] { "top", "left", "bottom", "right", "insideH", "insideV" }) {
			x.append("<w:").append(side).append(" w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>");
		}
		x.append("</w:tblBorders></w:tblPr><w:tblGrid>");
		for (int w : grid) x.append("<w:gridCol w:w=\"").append(w).append("\"/>");
		x.append("</w:tblGrid>");
		for (String r : rows) x.append(r);
		x.append("</w:tbl>");
		return x.toString();
	}
}
