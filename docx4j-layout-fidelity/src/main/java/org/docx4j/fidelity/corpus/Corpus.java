package org.docx4j.fidelity.corpus;

import static org.docx4j.fidelity.corpus.Doc.CARLITO;
import static org.docx4j.fidelity.corpus.Doc.DEJAVU;
import static org.docx4j.fidelity.corpus.Doc.SANS;
import static org.docx4j.fidelity.corpus.Doc.SERIF;
import static org.docx4j.fidelity.corpus.Doc.longProse;
import static org.docx4j.fidelity.corpus.Doc.prose;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.P;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.STLineSpacingRule;

/**
 * The probe corpus. Each probe isolates one layout rule (or a small family of
 * related ones) so that a difference in the report points at a rule, not at a
 * document. Ids are stable: they name the golden PDFs.
 */
public final class Corpus {

	private static final List<Probe> PROBES = new ArrayList<>();

	static {
		// ---------------------------------------------------------- spacing
		PROBES.add(new Probe("spacing-adjacent",
				"space-after of one paragraph meeting space-before of the next: 0/0, 12/0, 0/12, 12/12, 6/12, 12/6 pt", () -> {
			Doc d = Doc.create(15);
			int[][] combos = { {0, 0}, {240, 0}, {0, 240}, {240, 240}, {120, 240}, {240, 120} };
			for (int[] c : combos) {
				d.para("after=" + c[0] / 20 + "pt. " + prose(2)).after(c[0]).add();
				d.para("before=" + c[1] / 20 + "pt. " + prose(2, 3)).before(c[1]).add();
				d.para("plain. " + prose(1, 5)).add();
			}
			return d.pkg();
		}));

		PROBES.add(new Probe("spacing-contextual",
				"contextualSpacing between paragraphs of the same style, then a style change", () -> {
			Doc d = Doc.create(15);
			d.addParagraphStyle("ProbeBody", "Normal", ppr -> {
				PPrBase.Spacing sp = Doc.F.createPPrBaseSpacing();
				sp.setAfter(BigInteger.valueOf(240));
				sp.setBefore(BigInteger.valueOf(240));
				ppr.setSpacing(sp);
			});
			d.addParagraphStyle("ProbeOther", "Normal", ppr -> {
				PPrBase.Spacing sp = Doc.F.createPPrBaseSpacing();
				sp.setAfter(BigInteger.valueOf(240));
				ppr.setSpacing(sp);
			});
			// no direct spacing on these paragraphs: the style's 12pt before/after must apply
			for (int i = 0; i < 4; i++) {
				d.para("same style, contextual. " + prose(1, i)).style("ProbeBody").inheritSpacing().contextual().add();
			}
			d.para("other style. " + prose(1, 4)).style("ProbeOther").inheritSpacing().add();
			for (int i = 0; i < 3; i++) {
				d.para("same style, contextual again. " + prose(1, i + 5)).style("ProbeBody").inheritSpacing().contextual().add();
			}
			d.para("same style, NOT contextual. " + prose(1, 2)).style("ProbeBody").inheritSpacing().add();
			d.para("same style, NOT contextual. " + prose(1, 3)).style("ProbeBody").inheritSpacing().add();
			return d.pkg();
		}));

		PROBES.add(new Probe("spacing-autospacing",
				"beforeAutospacing/afterAutospacing (HTML auto spacing) in consecutive paragraphs", () -> {
			Doc d = Doc.create(15);
			d.para("plain. " + prose(1)).add();
			d.para("auto before+after. " + prose(1, 1)).autospacing(true, true).add();
			d.para("auto before+after. " + prose(1, 2)).autospacing(true, true).add();
			d.para("auto after only. " + prose(1, 3)).autospacing(false, true).add();
			d.para("auto before only. " + prose(1, 4)).autospacing(true, false).add();
			d.para("plain. " + prose(1, 5)).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("spacing-page-top",
				"space-before at top of page: hard page break, pageBreakBefore, section break, natural flow", () -> {
			Doc d = Doc.create(15);
			d.para("first paragraph of document, before=36pt. " + prose(2)).before(720).add();
			d.pageBreak();
			d.para("after hard page break, before=36pt. " + prose(2, 1)).before(720).add();
			d.para("pageBreakBefore, before=36pt. " + prose(2, 2)).before(720).pageBreakBefore().add();
			d.endSection("nextPage");
			d.para("first in new section, before=36pt. " + prose(2, 3)).before(720).add();
			// natural flow: many paragraphs with before=24pt so several land at a page top
			for (int i = 0; i < 40; i++) {
				d.para("natural flow, before=24pt. " + prose(1, i)).before(480).add();
			}
			return d.pkg();
		}));

				PROBES.add(new Probe("spacing-section-start",
				"space-before of the first paragraph after a next-page section break, with the section-break paragraph's space-after 0 / 10 / 20pt, and before smaller than that after", () -> {
			Doc d = Doc.create(15);
			d.para("section 1. " + prose(2)).add();
			d.endSection("nextPage", 0);
			d.para("after break para after=0, before=36pt. " + prose(2, 1)).before(720).add();
			d.endSection("nextPage", 200);
			d.para("after break para after=10pt, before=36pt. " + prose(2, 2)).before(720).add();
			d.endSection("nextPage", 400);
			d.para("after break para after=20pt, before=36pt. " + prose(2, 3)).before(720).add();
			d.endSection("nextPage", 400);
			d.para("after break para after=20pt, before=6pt. " + prose(2, 4)).before(120).add();
			d.endSection("nextPage", 0);
			d.para("after break para after=0, before=0. " + prose(2, 5)).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("spacing-autospacing-context",
				"HTML auto spacing between list items and inside a table cell", () -> {
			Doc d = Doc.create(15);
			d.para("plain. " + prose(1)).add();
			d.para("list item, auto before+after. " + prose(1, 1)).autospacing(true, true).listItem().add();
			d.para("list item, auto before+after. " + prose(1, 2)).autospacing(true, true).listItem().add();
			d.para("list item, auto before+after. " + prose(1, 3)).autospacing(true, true).listItem().add();
			d.para("plain after list. " + prose(1, 4)).add();
			Doc.Table t = new Doc.Table(4000, 4000).fixedLayout();
			org.docx4j.wml.Tr tr = Doc.F.createTr();
			for (int i = 0; i < 2; i++) {
				org.docx4j.wml.Tc tc = Doc.F.createTc();
				tc.getContent().add(d.para("cell auto before+after " + prose(1, i)).noLabel().autospacing(true, true).build());
				tc.getContent().add(d.para("second cell para, auto " + prose(1, i + 2)).noLabel().autospacing(true, true).build());
				tr.getContent().add(tc);
			}
			t.build().getContent().add(tr);
			d.add(t.build());
			d.para("plain after table. " + prose(1, 5)).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("spacing-in-table",
				"paragraph space-before/after inside table cells (Word applies them at cell top and, since 2013, bottom)", () -> {
			Doc d = Doc.create(15);
			d.para("before table. " + prose(1)).add();
			Doc.Table t = new Doc.Table(4000, 4000).fixedLayout();
			t.row(SERIF, 24, false, "no spacing", "no spacing");
			d.add(t.build());
			d.para("between tables. " + prose(1, 1)).after(240).add();
			Doc.Table t2 = new Doc.Table(4000, 4000).fixedLayout();
			t2.build().getContent().clear();
			// cells whose single paragraph has before=12pt after=12pt
			org.docx4j.wml.Tr tr = Doc.F.createTr();
			for (int i = 0; i < 2; i++) {
				org.docx4j.wml.Tc tc = Doc.F.createTc();
				P p = d.para("cell before=after=12pt " + prose(1, i)).noLabel().before(240).after(240).build();
				tc.getContent().add(p);
				tr.getContent().add(tc);
			}
			t2.build().getContent().add(tr);
			d.add(t2.build());
			d.para("after table. " + prose(1, 2)).before(240).add();
			return d.pkg();
		}));

		// ---------------------------------------------------------- line height
		PROBES.add(new Probe("line-auto",
				"auto line spacing 1.0 / 1.08 / 1.15 / 1.5 / 2.0 in four fonts", () -> {
			Doc d = Doc.create(15);
			String[] fonts = { SERIF, CARLITO, SANS, DEJAVU };
			int[] sizes = { 24, 22, 20, 20 };
			int[] lines = { 240, 259, 276, 360, 480 };
			for (int f = 0; f < fonts.length; f++) {
				for (int l : lines) {
					d.para(fonts[f] + " " + sizes[f] / 2 + "pt line=" + l + " auto. " + prose(3, l % 8))
							.font(fonts[f], sizes[f]).line(l, STLineSpacingRule.AUTO).add();
				}
			}
			return d.pkg();
		}));

		PROBES.add(new Probe("line-exact-atleast",
				"exact and atLeast line spacing, including exact smaller than the font (clipping)", () -> {
			Doc d = Doc.create(15);
			d.para("exact 12pt with 12pt font. " + prose(3)).line(240, STLineSpacingRule.EXACT).add();
			d.para("exact 9pt with 12pt font (clips). " + prose(3, 1)).line(180, STLineSpacingRule.EXACT).add();
			d.para("exact 24pt with 12pt font. " + prose(3, 2)).line(480, STLineSpacingRule.EXACT).add();
			d.para("atLeast 6pt with 12pt font (natural wins). " + prose(3, 3)).line(120, STLineSpacingRule.AT_LEAST).add();
			d.para("atLeast 20pt with 12pt font. " + prose(3, 4)).line(400, STLineSpacingRule.AT_LEAST).add();
			d.para("no w:line at all (inherits). " + prose(3, 5)).noLine().add();
			return d.pkg();
		}));

		PROBES.add(new Probe("line-mixed",
				"mixed run sizes in a line, superscript, and a large paragraph mark", () -> {
			Doc d = Doc.create(15);
			d.para("single-size control. " + prose(2)).add();
			d.para().text("small 10pt run, ").run("then a 24pt run, ", SERIF, 48, null)
					.run("then 10pt again. " + prose(2, 1), SERIF, 20, null).font(SERIF, 20).add();
			d.para().text("with superscript").run("2", SERIF, 24, Doc::superscript)
					.run(" continuing. " + prose(2, 2), SERIF, 24, null).add();
			d.para("paragraph mark is 36pt, runs are 12pt. " + prose(2, 3)).markSize(72).add();
			d.para("bold run inside. ").run("bold text " + prose(1, 4), SERIF, 24, Doc::bold).add();
			d.para("control again. " + prose(2, 5)).add();
			return d.pkg();
		}));

		// ---------------------------------------------------------- line breaking
		PROBES.add(new Probe("break-justified",
				"justified prose in three fonts (line-break parity)", () -> {
			Doc d = Doc.create(15);
			String[] fonts = { SERIF, CARLITO, SANS };
			int[] sizes = { 24, 22, 20 };
			for (int f = 0; f < fonts.length; f++) {
				for (int i = 0; i < 4; i++) {
					d.para(prose(6, i * 2)).font(fonts[f], sizes[f]).jc(JcEnumeration.BOTH).after(120).add();
				}
			}
			return d.pkg();
		}));

		PROBES.add(new Probe("break-ragged",
				"left-aligned prose in three fonts, with indents (line-break parity)", () -> {
			Doc d = Doc.create(15);
			String[] fonts = { SERIF, CARLITO, SANS };
			int[] sizes = { 24, 22, 20 };
			for (int f = 0; f < fonts.length; f++) {
				d.para(prose(6, 1)).font(fonts[f], sizes[f]).after(120).add();
				d.para(prose(6, 3)).font(fonts[f], sizes[f]).after(120).indent(720, 720, 0).add();
				d.para(prose(6, 5)).font(fonts[f], sizes[f]).after(120).indent(720, 0, 360).add();
			}
			return d.pkg();
		}));

		// ---------------------------------------------------------- hyphenation
		PROBES.add(new Probe("hyphenation",
				"automatic hyphenation, English, w:hyphenationZone 360 and no consecutive limit:"
				+ " justified and ragged prose of long words, narrow measures, a suppressAutoHyphens"
				+ " paragraph and a paragraph of capitals", () -> {
			Doc d = Doc.create(15);
			d.documentLanguage("en-US");
			d.hyphenation(true, 360, 0, false);
			// justified: Word hyphenates where the gap left by the next word exceeds the zone
			for (int i = 0; i < 5; i++) {
				d.para(longProse(2, i * 2)).lang("en-US").jc(JcEnumeration.BOTH).after(120).add();
			}
			// ragged right: the same rule, with no space compression to confuse it
			for (int i = 0; i < 5; i++) {
				d.para(longProse(2, i * 2 + 1)).lang("en-US").after(120).add();
			}
			// narrow measures, where a long word leaves a big gap on nearly every line
			for (int i = 0; i < 3; i++) {
				d.para(longProse(2, i)).lang("en-US").indent(2160, 0, 0).after(120).add();
			}
			d.para(longProse(2, 4)).lang("en-US").jc(JcEnumeration.BOTH).indent(2880, 0, 0).after(120).add();
			// small type, so a hyphenation point falls inside the zone more often
			d.para(longProse(3, 2)).lang("en-US").font(SERIF, 16).jc(JcEnumeration.BOTH).after(120).add();
			d.para(longProse(3, 5)).lang("en-US").font(SERIF, 16).after(120).add();
			// this paragraph alone is exempt
			d.para("suppressAutoHyphens. " + longProse(2, 3)).lang("en-US")
					.suppressAutoHyphens().jc(JcEnumeration.BOTH).after(120).add();
			// capitals: hyphenated here (the control for the doNotHyphenateCaps probe)
			d.para(longProse(2, 6).toUpperCase()).lang("en-US").jc(JcEnumeration.BOTH).after(120).add();
			d.para(longProse(2, 7)).lang("en-US").jc(JcEnumeration.BOTH).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("hyphenation-zone",
				"automatic hyphenation with w:hyphenationZone 720, w:consecutiveHyphenLimit 2 and"
				+ " w:doNotHyphenateCaps: the same prose as the hyphenation probe", () -> {
			Doc d = Doc.create(15);
			d.documentLanguage("en-US");
			d.hyphenation(true, 720, 2, true);
			for (int i = 0; i < 5; i++) {
				d.para(longProse(2, i * 2)).lang("en-US").jc(JcEnumeration.BOTH).after(120).add();
			}
			for (int i = 0; i < 5; i++) {
				d.para(longProse(2, i * 2 + 1)).lang("en-US").after(120).add();
			}
			for (int i = 0; i < 3; i++) {
				d.para(longProse(2, i)).lang("en-US").indent(2160, 0, 0).after(120).add();
			}
			d.para(longProse(2, 4)).lang("en-US").jc(JcEnumeration.BOTH).indent(2880, 0, 0).after(120).add();
			d.para(longProse(3, 2)).lang("en-US").font(SERIF, 16).jc(JcEnumeration.BOTH).after(120).add();
			d.para(longProse(3, 5)).lang("en-US").font(SERIF, 16).after(120).add();
			// all capitals: w:doNotHyphenateCaps leaves these words whole
			d.para(longProse(2, 6).toUpperCase()).lang("en-US").jc(JcEnumeration.BOTH).after(120).add();
			d.para(longProse(2, 7)).lang("en-US").jc(JcEnumeration.BOTH).add();
			return d.pkg();
		}));

		// ---------------------------------------------------------- tables
		PROBES.add(new Probe("table-fixed",
				"fixed-layout tables: default borders, indent, thick borders, cell margins", () -> {
			Doc d = Doc.create(15);
			d.para("before. " + prose(1)).after(240).add();
			d.add(new Doc.Table(2000, 3000, 4000).fixedLayout()
					.row(SERIF, 24, false, "a", "bb", "ccc")
					.row(SERIF, 24, false, prose(1), prose(1, 1), prose(1, 2)).build());
			d.para("indent 720. " + prose(1, 3)).before(240).after(240).add();
			d.add(new Doc.Table(3000, 3000).fixedLayout().indent(720)
					.row(SERIF, 24, false, "indented", "table").build());
			d.para("thick borders 3pt. " + prose(1, 4)).before(240).after(240).add();
			d.add(new Doc.Table(3000, 3000).fixedLayout().borders(24)
					.row(SERIF, 24, false, "thick", "borders")
					.row(SERIF, 24, false, prose(1, 5), "x").build());
			d.para("cell margins 144/72. " + prose(1, 6)).before(240).after(240).add();
			d.add(new Doc.Table(3000, 3000).fixedLayout().cellMargins(144, 72)
					.row(SERIF, 24, false, "margins", "here")
					.row(SERIF, 24, false, prose(1, 7), "y").build());
			d.para("after. " + prose(1, 7)).before(240).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("table-autofit",
				"autofit tables: auto widths with short and long content, then a mix of dxa and auto cells", () -> {
			Doc d = Doc.create(15);
			d.para("before. " + prose(1)).after(240).add();
			d.add(new Doc.Table(3000, 3000, 3000).autoWidth()
					.row(SERIF, 24, true, "short", "medium length cell", prose(2))
					.row(SERIF, 24, true, "a", "b", "c").build());
			d.para("mixed. " + prose(1, 1)).before(240).after(240).add();
			d.add(new Doc.Table(1500, 6000).autoWidth()
					.row(SERIF, 24, true, "fixed 1500?", prose(3))
					.row(SERIF, 24, false, "dxa", "dxa " + prose(1)).build());
			d.para("after. " + prose(1, 2)).before(240).add();
			return d.pkg();
		}));

				PROBES.add(new Probe("table-width",
				"autofit with w:tblW 50% (pct), 5000 twips (dxa) and auto: same auto-width cells in each", () -> {
			Doc d = Doc.create(15);
			String[][] rows = { { "short", "medium length cell", prose(2) }, { "a", "b", "c" } };
			for (String[] spec : new String[][] { { "2500", "pct" }, { "5000", "dxa" }, { "0", "auto" } }) {
				d.para("tblW " + spec[0] + " " + spec[1] + ". " + prose(1)).after(240).add();
				Doc.Table t = new Doc.Table(3000, 3000, 3000).tableWidth(Integer.parseInt(spec[0]), spec[1]);
				for (String[] r : rows) t.row(SERIF, 24, true, r);
				d.add(t.build());
			}
			d.para("after. " + prose(1, 1)).before(240).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("table-span",
				"cells spanning columns (gridSpan) under autofit and under fixed layout", () -> {
			Doc d = Doc.create(15);
			d.para("autofit with spans. " + prose(1)).after(240).add();
			Doc.Table t = new Doc.Table(3000, 3000, 3000).autoWidth();
			t.rowOf(null, null, t.cell("spans two columns: " + prose(1, 2), SERIF, 24, 2, null), t.cell("third", SERIF, 24, 1, null));
			t.rowOf(null, null, t.cell("one", SERIF, 24, 1, null), t.cell("two " + prose(1, 3), SERIF, 24, 1, null), t.cell("three", SERIF, 24, 1, null));
			t.rowOf(null, null, t.cell("first", SERIF, 24, 1, null), t.cell("spans two: " + prose(1, 4), SERIF, 24, 2, null));
			d.add(t.build());
			d.para("fixed with spans. " + prose(1, 1)).before(240).after(240).add();
			Doc.Table f = new Doc.Table(2000, 3000, 4000).fixedLayout();
			f.rowOf(null, null, f.cell("spans two " + prose(1, 5), SERIF, 24, 2, 5000), f.cell("third " + prose(1, 6), SERIF, 24, 1, 4000));
			f.rowOf(null, null, f.cell("one", SERIF, 24, 1, 2000), f.cell("two", SERIF, 24, 1, 3000), f.cell("three", SERIF, 24, 1, 4000));
			d.add(f.build());
			d.para("after. " + prose(1, 7)).before(240).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("table-nested",
				"a fixed 2-column table nested in the second cell of an autofit table", () -> {
			Doc d = Doc.create(15);
			d.para("before. " + prose(1)).after(240).add();
			Doc.Table inner = new Doc.Table(1500, 1500).fixedLayout();
			inner.row(SERIF, 20, false, "in a", "in b").row(SERIF, 20, false, prose(1, 1), "x");
			Doc.Table outer = new Doc.Table(3000, 3000, 3000).autoWidth();
			outer.rowOf(null, null, outer.cell("outer left " + prose(1, 2), SERIF, 24, 1, null),
					outer.cellWith(inner.build(), "after nested", SERIF, 24),
					outer.cell("outer right", SERIF, 24, 1, null));
			d.add(outer.build());
			d.para("after. " + prose(1, 3)).before(240).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("table-floating",
				"a floating table (tblpPr, right of the margin, 1in down) with body text flowing beside it", () -> {
			Doc d = Doc.create(15);
			d.para("before. " + prose(1)).after(240).add();
			Doc.Table t = new Doc.Table(2000, 2000).fixedLayout().floating(4500, 1440);
			t.row(SERIF, 24, false, "float a", "float b").row(SERIF, 24, false, prose(1, 1), "y");
			d.add(t.build());
			for (int i = 0; i < 6; i++) {
				d.para(prose(4, i + 2)).after(160).add();
			}
			return d.pkg();
		}));

		PROBES.add(new Probe("table-cellspacing",
				"tblCellSpacing 72 twips (separate borders) and default, same content", () -> {
			Doc d = Doc.create(15);
			d.para("cell spacing 72 twips. " + prose(1)).after(240).add();
			d.add(new Doc.Table(3000, 3000).fixedLayout().cellSpacing(72)
					.row(SERIF, 24, false, "spaced", "borders")
					.row(SERIF, 24, false, prose(1, 1), "z").build());
			d.para("no cell spacing. " + prose(1, 2)).before(240).after(240).add();
			d.add(new Doc.Table(3000, 3000).fixedLayout()
					.row(SERIF, 24, false, "collapsed", "borders")
					.row(SERIF, 24, false, prose(1, 1), "z").build());
			d.para("after. " + prose(1, 3)).before(240).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("table-rowheight",
				"trHeight atLeast 600 (bigger than one line), atLeast 100 (smaller), exact 600, exact 200 (smaller than the text)", () -> {
			Doc d = Doc.create(15);
			d.para("before. " + prose(1)).after(240).add();
			Doc.Table t = new Doc.Table(4000, 4000).fixedLayout();
			t.rowOf(600, org.docx4j.wml.STHeightRule.AT_LEAST, t.cell("atLeast 600", SERIF, 24, 1, 4000), t.cell("one line", SERIF, 24, 1, 4000));
			t.rowOf(100, org.docx4j.wml.STHeightRule.AT_LEAST, t.cell("atLeast 100", SERIF, 24, 1, 4000), t.cell(prose(1, 1), SERIF, 24, 1, 4000));
			t.rowOf(600, org.docx4j.wml.STHeightRule.EXACT, t.cell("exact 600", SERIF, 24, 1, 4000), t.cell("one line", SERIF, 24, 1, 4000));
			t.rowOf(200, org.docx4j.wml.STHeightRule.EXACT, t.cell("exact 200 clips", SERIF, 24, 1, 4000), t.cell(prose(1, 2), SERIF, 24, 1, 4000));
			t.rowOf(null, null, t.cell("no height", SERIF, 24, 1, 4000), t.cell("one line", SERIF, 24, 1, 4000));
			d.add(t.build());
			d.para("after. " + prose(1, 3)).before(240).add();
			return d.pkg();
		}));

		// ---------------------------------------------------------- page furniture
		PROBES.add(new Probe("page-header-footer",
				"one-line header/footer in section 1; four-line header in section 2; body pushed down", () -> {
			Doc d = Doc.create(15);
			d.addHeader(SANS, 20, "Section one header");
			d.addFooter(SANS, 20, "Section one footer");
			for (int i = 0; i < 12; i++) {
				d.para(prose(4, i)).after(160).add();
			}
			d.endSection("nextPage");
			d.addHeader(SANS, 20, "Section two header line 1", "line 2", "line 3", "line 4 (body must start below this)");
			d.addFooter(SANS, 20, "Section two footer line 1", "line 2");
			for (int i = 0; i < 12; i++) {
				d.para(prose(4, i + 3)).after(160).add();
			}
			return d.pkg();
		}));

				PROBES.add(new Probe("widow-orphan",
				"three-line paragraphs straddling page bottoms with widowControl on (section 1) and off (section 2)", () -> {
			Doc d = Doc.create(15);
			for (int i = 0; i < 34; i++) {
				d.para(prose(3, i)).add();
			}
			d.endSection("nextPage", 0);
			for (int i = 0; i < 34; i++) {
				d.para(prose(3, i + 1)).widowControl(false).add();
			}
			return d.pkg();
		}));

		PROBES.add(new Probe("page-first-even-odd",
				"different first-page header, even/odd headers and footers, over five pages", () -> {
			Doc d = Doc.create(15);
			d.addHeader(org.docx4j.wml.HdrFtrRef.FIRST, SANS, 20, "FIRST PAGE HEADER");
			d.addHeader(org.docx4j.wml.HdrFtrRef.EVEN, SANS, 20, "even page header");
			d.addHeader(org.docx4j.wml.HdrFtrRef.DEFAULT, SANS, 20, "odd page header");
			d.addFooter(org.docx4j.wml.HdrFtrRef.DEFAULT, SANS, 20, "odd footer");
			d.addFooter(org.docx4j.wml.HdrFtrRef.EVEN, SANS, 20, "even footer");
			for (int i = 0; i < 60; i++) {
				d.para(prose(3, i)).after(160).add();
			}
			return d.pkg();
		}));

		PROBES.add(new Probe("page-tall-header",
				"a 12-line header whose height exceeds the top margin (body must move down), and a 6-line footer", () -> {
			Doc d = Doc.create(15);
			String[] h = new String[12];
			for (int i = 0; i < 12; i++) h[i] = "tall header line " + (i + 1);
			d.addHeader(SANS, 20, h);
			d.addFooter(SANS, 20, "footer 1", "footer 2", "footer 3", "footer 4", "footer 5", "footer 6");
			for (int i = 0; i < 30; i++) {
				d.para(prose(3, i)).after(160).add();
			}
			return d.pkg();
		}));

		PROBES.add(new Probe("page-header-footnotes",
				"four-line header and two-line footer at 0.25in/0.75in distances, with footnotes in the flow (one near the page bottom)", () -> {
			Doc d = Doc.create(15);
			d.headerFooterDistance(360, 1080);
			d.addHeader(SANS, 20, "Header line 1", "Header line 2", "Header line 3", "Header line 4 (body starts below this)");
			d.addFooter(SANS, 20, "Footer line 1", "Footer line 2 (body ends above this)");
			d.para("first paragraph").run(d.footnoteRef("A short footnote.", SERIF, 20)).text(" " + prose(2)).after(160).add();
			d.para(prose(2, 1)).run(d.footnoteRef("A long footnote: " + prose(4, 2), SERIF, 20)).text(" " + prose(1, 3)).after(160).add();
			for (int i = 0; i < 24; i++) {
				Doc.Para p = d.para(prose(3, i + 2)).after(160);
				if (i % 5 == 4) p.run(d.footnoteRef("Footnote in the flow, paragraph " + i + ". " + prose(1, i), SERIF, 20));
				p.add();
			}
			d.finishFootnotes();
			return d.pkg();
		}));
		PROBES.add(new Probe("page-first-even-odd-heights",
				"first-page header of three lines, even header of one, odd header of five with a picture; one-line odd and three-line even footers; six pages", () -> {
			Doc d = Doc.create(15);
			d.addHeader(org.docx4j.wml.HdrFtrRef.FIRST, SANS, 20, "FIRST PAGE HEADER line 1", "first line 2", "first line 3");
			d.addHeader(org.docx4j.wml.HdrFtrRef.EVEN, SANS, 20, "even page header");
			java.util.List<org.docx4j.wml.P> odd = new java.util.ArrayList<>();
			odd.add(Doc.plainParagraph("odd page header line 1", SANS, 20));
			odd.add(Doc.plainParagraph("odd line 2", SANS, 20));
			odd.add(d.pictureParagraph(240, 60, 2160));
			odd.add(Doc.plainParagraph("odd line 4", SANS, 20));
			odd.add(Doc.plainParagraph("odd line 5 (body starts below this)", SANS, 20));
			d.addHeader(org.docx4j.wml.HdrFtrRef.DEFAULT, odd);
			d.addFooter(org.docx4j.wml.HdrFtrRef.DEFAULT, SANS, 20, "odd footer");
			d.addFooter(org.docx4j.wml.HdrFtrRef.EVEN, SANS, 20, "even footer line 1", "even footer line 2", "even footer line 3");
			for (int i = 0; i < 72; i++) {
				d.para(prose(3, i)).after(160).add();
			}
			return d.pkg();
		}));
		PROBES.add(new Probe("footnote-space-after",
				"paragraphs with 24pt space after ending against a footnote area (section 1) and against a plain page bottom (section 2, control): is the space after kept above the footnotes?", () -> {
			Doc d = Doc.create(15);
			for (int i = 0; i < 26; i++) {
				Doc.Para p = d.para(prose(2, i)).after(480);
				if (i % 4 == 0) p.run(d.footnoteRef("Note for paragraph " + i + ". " + prose(1, i), SERIF, 20));
				p.add();
			}
			d.endSection("nextPage", 0);
			for (int i = 0; i < 26; i++) {
				d.para(prose(2, i + 1)).after(480).add();
			}
			d.finishFootnotes();
			return d.pkg();
		}));
		PROBES.add(new Probe("page-landscape-margins",
				"section 1 A4 portrait 1in margins; section 2 A4 landscape with 0.5in/1.5in/0.75in/2in margins; section 3 Letter portrait 0.75in", () -> {
			Doc d = Doc.create(15);
			for (int i = 0; i < 6; i++) d.para(prose(3, i)).after(160).add();
			d.endSection("nextPage", 0);
			d.pageGeometry(16839, 11907, true, 720, 2160, 1080, 2880);
			for (int i = 0; i < 8; i++) d.para(prose(3, i + 2)).after(160).add();
			d.endSection("nextPage", 0);
			d.pageGeometry(12240, 15840, false, 1080, 1080, 1080, 1080);
			for (int i = 0; i < 8; i++) d.para(prose(3, i + 4)).after(160).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("footnotes",
				"short and long footnotes, several on one page, one near the page bottom", () -> {
			Doc d = Doc.create(15);
			d.para("first paragraph").run(d.footnoteRef("A short footnote.", SERIF, 20)).text(" " + prose(2)).after(160).add();
			d.para(prose(2, 1)).run(d.footnoteRef("A long footnote: " + prose(4, 2), SERIF, 20)).text(" " + prose(1, 3)).after(160).add();
			for (int i = 0; i < 20; i++) {
				Doc.Para p = d.para(prose(3, i + 2)).after(160);
				if (i % 6 == 5) p.run(d.footnoteRef("Footnote in the flow, paragraph " + i + ". " + prose(1, i), SERIF, 20));
				p.add();
			}
			d.finishFootnotes();
			return d.pkg();
		}));

		PROBES.add(new Probe("image-anchored",
				"anchored pictures: square wrap at the right margin, top-and-bottom wrap, behind text, and a square wrap at a left offset", () -> {
			Doc d = Doc.create(15);
			d.para("square wrap, right of margin. " + prose(1)).after(160).run(d.anchoredImage(200, 150, 1440000L, 1080000L, "square", "right", 0, 0)).add();
			for (int i = 0; i < 4; i++) d.para(prose(3, i + 1)).after(160).add();
			d.para("top and bottom wrap, centred. " + prose(1, 2)).after(160).run(d.anchoredImage(300, 100, 2160000L, 720000L, "topAndBottom", "center", 0, 0)).add();
			for (int i = 0; i < 3; i++) d.para(prose(3, i + 3)).after(160).add();
			d.para("behind text at a left offset. " + prose(1, 4)).after(160).run(d.anchoredImage(200, 100, 1440000L, 720000L, "none", null, 914400L, 0)).add();
			for (int i = 0; i < 3; i++) d.para(prose(3, i + 5)).after(160).add();
			d.para("square wrap at a left offset of 1in, 0.5in below the paragraph. " + prose(1, 6)).after(160).run(d.anchoredImage(150, 150, 1080000L, 1080000L, "square", null, 914400L, 457200L)).add();
			for (int i = 0; i < 4; i++) d.para(prose(3, i + 6)).after(160).add();
			return d.pkg();
		}));

		// ---------------------------------------------------------- images
		PROBES.add(new Probe("kern-title",
				"kerning: w:kern at, above and below the run size, via the Title style, and none (control)", () -> {
			Doc d = Doc.create(15);
			String pairs = "AVAILABLE TAX WAYS To Yo Vo Wa Te Ty. Typography AWAY from the Valley; PAY VAT AT LA. "
					+ "Two Yachts, Toy Yaks, Wavy Tyres. ";
			d.para("no kern, 12pt (control). " + pairs + prose(2)).after(160).add();
			d.para().run("kern 28 at 28pt (kerns). " + pairs, SERIF, 56, Doc.kern(28)).after(160).add();
			d.para().run("kern 28 at 12pt (below threshold, no kern). " + pairs + prose(2, 1), SERIF, 24, Doc.kern(28)).after(160).add();
			d.para().run("kern 24 at 12pt (at threshold, kerns). " + pairs + prose(2, 2), SERIF, 24, Doc.kern(24)).after(160).add();
			d.para().run("kern 28 at 14pt (at threshold, kerns). " + pairs + prose(2, 3), SERIF, 28, Doc.kern(28)).after(160).add();
			d.para().run("kern 2 at 12pt, prose (kerns). " + prose(4, 4), SERIF, 24, Doc.kern(2)).after(160).add();
			d.para("Title style (kern 28 from the style). " + pairs).style("Title").add();
			d.para("no kern again (control). " + pairs + prose(2, 5)).after(160).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("spacing-char",
				"character spacing (w:spacing): expanded 0.25, 1 and 3pt, condensed 0.5pt, and a control; does the expansion apply to spaces?", () -> {
			Doc d = Doc.create(15);
			String pairs = "AVAILABLE TAX WAYS To Yo Vo Wa Te Ty. Two Yachts, Toy Yaks, Wavy Tyres. ";
			d.para("control, no spacing. " + pairs + prose(2)).after(160).add();
			d.para().run("expanded 0.25pt (w:spacing 5). " + pairs + prose(2, 1), SERIF, 24, Doc.charSpacing(5)).after(160).add();
			d.para().run("expanded 1pt (w:spacing 20). " + pairs + prose(2, 2), SERIF, 24, Doc.charSpacing(20)).after(160).add();
			d.para().run("expanded 3pt (w:spacing 60). " + pairs + prose(2, 3), SERIF, 24, Doc.charSpacing(60)).after(160).add();
			d.para().run("condensed 0.5pt (w:spacing -10). " + pairs + prose(2, 4), SERIF, 24, Doc.charSpacing(-10)).after(160).add();
			d.para("control again. " + pairs + prose(2, 5)).after(160).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("image-inline",
				"inline images: small in a line of text, and a wide one on its own", () -> {
			Doc d = Doc.create(15);
			d.para("before. " + prose(1)).after(240).add();
			P p = d.para("text then image ").build();
			p.getContent().add(d.inlineImage(200, 80, 2160L)); // 1.5in wide (twips)
			p.getContent().add(Doc.run(" then text. " + prose(1, 1), SERIF, 24, null));
			d.add(p);
			d.para("wide image next. " + prose(1, 2)).before(240).after(240).add();
			P wide = d.para().noLabel().build();
			wide.getContent().add(d.inlineImage(600, 200, 8640L)); // 6in wide (twips)
			d.add(wide);
			d.para("after. " + prose(1, 3)).before(240).add();
			return d.pkg();
		}));

		// ---------------------------------------------------- table indent (N7 probe)
		PROBES.add(tableIndentProbe(15));
		PROBES.add(tableIndentProbe(14));

		PROBES.add(new Probe("ptab-right",
				"a right w:ptab in the header and in the body, and a paragraph with a right indent", () -> {
			Doc d = Doc.create(15);

			List<P> header = new ArrayList<>();
			P h = Doc.plainParagraph("left text", SANS, 20);
			h.getContent().add(rightPtab());
			h.getContent().add(Doc.run("right text", SANS, 20, null));
			header.add(h);
			d.addHeader(org.docx4j.wml.HdrFtrRef.DEFAULT, header);

			d.para("before the ptab paragraph. " + prose(1)).after(240).add();

			P body = d.para("body left").noLabel().build();
			body.getContent().add(rightPtab());
			body.getContent().add(Doc.run("body right", SERIF, 24, null));
			d.add(body);

			d.para("right indent 1440. " + prose(2, 1)).before(240).after(240).indent(0, 0, 0).add();
			P indented = d.para("this paragraph has w:ind right=1440. " + prose(2, 2)).noLabel().build();
			PPrBase.Ind ind = Doc.F.createPPrBaseInd();
			ind.setRight(BigInteger.valueOf(1440));
			indented.getPPr().setInd(ind);
			d.add(indented);

			d.para("after. " + prose(1, 3)).before(240).add();
			return d.pkg();
		}));

		// ------------------------------------- rules 17.0.5 added, to be re-measured

		/*
		 * §4.4 says "a line holding a tab is laid out from the left whatever the
		 * paragraph's w:jc".  A real document contradicts that for a *trailing* tab:
		 * Word drew a centred line ending in a tab at x=208.9, docx4j at 56.7, flush
		 * left.  This probe separates a leading tab, a mid-line tab and a trailing tab
		 * under each of centre, right and justified.
		 */
		PROBES.add(new Probe("tab-jc",
				"centre/right/justified paragraphs whose tab is leading, mid-line or trailing", () -> {
			Doc d = Doc.create(15);
			JcEnumeration[] alignments = { JcEnumeration.CENTER, JcEnumeration.RIGHT, JcEnumeration.BOTH };
			for (JcEnumeration jc : alignments) {
				String name = jc.value();
				d.para("no tab, " + name).jc(jc).after(120).add();
				d.para().noLabel().jc(jc).text("trailing tab, " + name).tab().after(120).add();
				d.para().noLabel().jc(jc).tab().text("leading tab, " + name).after(120).add();
				d.para().noLabel().jc(jc).text("mid ").tab().text("tab, " + name).after(120).add();
				d.para().noLabel().jc(jc).text("trailing tab with a stop, " + name)
						.tabStop(6000, org.docx4j.wml.STTabJc.LEFT).tab().after(240).add();
			}
			d.para("after. " + prose(1)).before(240).add();
			return d.pkg();
		}));

		/*
		 * §3 says a border's w:space is the padding on that side (measured on Word's
		 * Title style: 4pt space, 1pt border, the next paragraph 5pt lower).  A real
		 * document contradicts that inside a table cell: for a 0.5pt border with
		 * w:space="1" Word's row pitch was exactly the bare line box (9.1pt for an 8pt
		 * Arial line), where docx4j's was 12.2pt.  Same paragraph in and out of a cell,
		 * at three w:space values.
		 */
		PROBES.add(new Probe("pbdr-space",
				"a 0.5pt paragraph border with w:space 0, 1 and 4pt, inside a table cell and outside", () -> {
			Doc d = Doc.create(15);
			for (int space : new int[] { 0, 1, 4 }) {
				d.para("border space " + space + "pt, in the flow").font(SANS, 16).borders(4, space).add();
				d.para("next paragraph, space " + space).font(SANS, 16).after(240).add();

				d.para("the same in a cell, space " + space).font(SANS, 16).after(120).add();
				Doc.Table t = new Doc.Table(4000, 4000).autoWidth();
				t.rowOf(null, null,
						t.cellOf(d.para().noLabel().font(SANS, 16).text("bordered " + space).borders(4, space).build(),
								 d.para().noLabel().font(SANS, 16).text("second line " + space).build()),
						t.cellOf(d.para().noLabel().font(SANS, 16).text("plain " + space).build(),
								 d.para().noLabel().font(SANS, 16).text("second line " + space).build()));
				d.add(t.build());
				d.para("after the table, space " + space).font(SANS, 16).before(240).after(240).add();
			}
			return d.pkg();
		}));

		/*
		 * §6.1's compat-14 table grid edge disagreed with two real mode-14 documents,
		 * whose first cell text sits at margin + w:tblInd + one left cell margin - the
		 * mode-15 geometry.  The table-indent-compat14/15 probes cover w:tblInd with
		 * Word's default cell margins; this pair adds an explicit w:tblCellMar (Word's
		 * own 108 twips, and a different 72) and a fixed layout, which are the other
		 * things those documents have.
		 */
		PROBES.add(gridEdgeProbe(14));
		PROBES.add(gridEdgeProbe(15));

		/*
		 * C7: our autofit column widths differ from Word's by enough to change where a
		 * cell wraps.  A 2-column autofit table whose cells hold long words gives the
		 * classic algorithm little slack to share, so a small difference in the minima
		 * or the maxima shows up as a different break.
		 */
		PROBES.add(new Probe("table-autofit-wrap",
				"2-column autofit tables of long words: where the column boundary falls decides the wrap", () -> {
			Doc d = Doc.create(15);
			d.para("before. " + prose(1)).after(240).add();

			d.para("long words both sides").before(240).after(120).add();
			d.add(new Doc.Table(4513, 4513).autoWidth()
					.row(SERIF, 24, true,
						"Teaching and learning sequence for the fortnight",
						"Differentiation and assessment opportunities considered")
					.row(SERIF, 24, true,
						"Comprehension strategies demonstrated",
						"Reflection")
					.build());

			d.para("one long word, one short").before(240).after(120).add();
			d.add(new Doc.Table(4513, 4513).autoWidth()
					.row(SERIF, 24, true,
						"Responsibilities",
						"The organisation's internationalisation programme covers " + prose(1))
					.row(SERIF, 24, true, "Owner", "Name")
					.build());

			d.para("three columns, mixed word lengths").before(240).after(120).add();
			d.add(new Doc.Table(3008, 3008, 3009).autoWidth()
					.row(SERIF, 24, true, "Monday", "Wednesday", "Friday")
					.row(SERIF, 24, true,
						"Reading comprehension",
						"Mathematics investigation",
						"Physical education and sport")
					.build());

			d.para("after. " + prose(1, 1)).before(240).add();
			return d.pkg();
		}));
	}

	/**
	 * Where Word puts an autofit table's grid edge when the table declares its own
	 * cell margins, under one compatibility mode.  §6.1's rule (below mode 15 the
	 * first column's text lands on the margin + w:tblInd; from mode 15 the grid edge
	 * does) was measured with Word's default margins only.
	 */
	private static Probe gridEdgeProbe(int compatMode) {
		return new Probe("table-grid-edge-compat" + compatMode,
				"w:tblInd 108 with default, 108 and 72 twip cell margins, autofit and fixed,"
				+ " compatibilityMode " + compatMode, () -> {
			Doc d = Doc.create(compatMode);
			d.para("default cell margins. " + prose(1)).after(240).add();
			d.add(new Doc.Table(4000, 4000).indent(108)
					.row(SERIF, 24, true, "default margins", "right").build());

			d.para("w:tblCellMar 108. " + prose(1, 1)).before(240).after(240).add();
			d.add(new Doc.Table(4000, 4000).indent(108).cellMargins(108, 0)
					.row(SERIF, 24, true, "cellMar 108", "right").build());

			d.para("w:tblCellMar 72. " + prose(1, 2)).before(240).after(240).add();
			d.add(new Doc.Table(4000, 4000).indent(108).cellMargins(72, 0)
					.row(SERIF, 24, true, "cellMar 72", "right").build());

			d.para("fixed layout, w:tblCellMar 108. " + prose(1, 3)).before(240).after(240).add();
			d.add(new Doc.Table(4000, 4000).fixedLayout().indent(108).cellMargins(108, 0)
					.row(SERIF, 24, false, "fixed cellMar 108", "right").build());

			d.para("after. " + prose(1, 4)).before(240).add();
			return d.pkg();
		});
	}

	/** A run holding a right w:ptab relative to the margin. */
	private static org.docx4j.wml.R rightPtab() {
		org.docx4j.wml.R r = Doc.F.createR();
		org.docx4j.wml.R.Ptab ptab = Doc.F.createRPtab();
		ptab.setAlignment(org.docx4j.wml.STPTabAlignment.RIGHT);
		ptab.setRelativeTo(org.docx4j.wml.STPTabRelativeTo.MARGIN);
		ptab.setLeader(org.docx4j.wml.STPTabLeader.NONE);
		r.getContent().add(ptab);
		return r;
	}

	/**
	 * Where Word puts the grid edge of an autofit table: with no w:tblInd at all,
	 * with w:tblInd 0, and with w:tblInd 108, under one compatibility mode.  Word's
	 * default cell margins apply (no w:tblCellMar), so the difference between the
	 * cases is the whole question.
	 */
	private static Probe tableIndentProbe(int compatMode) {
		return new Probe("table-indent-compat" + compatMode,
				"autofit 2-column tables with no w:tblInd, w:tblInd 0 and w:tblInd 108,"
				+ " default cell margins, compatibilityMode " + compatMode, () -> {
			Doc d = Doc.create(compatMode);
			d.para("no w:tblInd. " + prose(1)).after(240).add();
			d.add(new Doc.Table(4000, 4000)
					.row(SERIF, 24, true, "no tblInd left", "no tblInd right").build());
			d.para("w:tblInd 0. " + prose(1, 1)).before(240).after(240).add();
			d.add(new Doc.Table(4000, 4000).indent(0)
					.row(SERIF, 24, true, "tblInd 0 left", "tblInd 0 right").build());
			d.para("w:tblInd 108. " + prose(1, 2)).before(240).after(240).add();
			d.add(new Doc.Table(4000, 4000).indent(108)
					.row(SERIF, 24, true, "tblInd 108 left", "tblInd 108 right").build());
			d.para("after. " + prose(1, 3)).before(240).add();
			return d.pkg();
		});
	}

	public static List<Probe> all() {
		return Collections.unmodifiableList(PROBES);
	}

	public static Probe byId(String id) {
		for (Probe p : PROBES) {
			if (p.id.equals(id)) return p;
		}
		return null;
	}

	/** Writes every probe as {@code <id>.docx} into dir, plus corpus.txt and corpus-manifest.properties. */
	public static void generate(File dir) throws Exception {
		dir.mkdirs();
		try (PrintWriter idx = new PrintWriter(new FileWriter(new File(dir, "corpus.txt")))) {
			for (Probe p : PROBES) {
				WordprocessingMLPackage pkg = p.build();
				File out = new File(dir, p.id + ".docx");
				Docx4J.save(pkg, out);
				idx.println(p.id + "\t" + p.description);
				System.out.println("wrote " + out);
			}
		}
		try (PrintWriter m = new PrintWriter(new FileWriter(new File(dir, "corpus-manifest.properties")))) {
			m.println("generated=" + ZonedDateTime.now());
			m.println("docx4j.version=" + Docx4J.class.getPackage().getImplementationVersion());
			m.println("probes=" + PROBES.size());
			m.println("fonts=" + SERIF + ", " + SANS + ", " + CARLITO + ", " + DEJAVU);
		}
	}
}
