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
import org.docx4j.wml.Br;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.STLineSpacingRule;
import org.docx4j.wml.Tbl;

/**
 * The probe corpus. Each probe isolates one layout rule (or a small family of
 * related ones) so that a difference in the report points at a rule, not at a
 * document. Ids are stable: they name the golden PDFs.
 */
public final class Corpus {

	private static final org.docx4j.wml.ObjectFactory F = org.docx4j.jaxb.Context.getWmlObjectFactory();

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

		/*
		 * §3.5's "a cell disagrees, and is not settled": a corpus document whose cells hold
		 * bulleted paragraphs carrying w:before="100" w:beforeAutospacing="1" w:after="100"
		 * w:afterAutospacing="1" has Word event rows 34.8pt apart where ours are 21.5 -
		 * about a page of its five, and it is one page short of Word on b54.  Whether the
		 * discriminator is the list, the explicit w:before/w:after beside the autospacing,
		 * or neither is what the rows here vary one at a time: R1 the corpus shape, R2 the
		 * list item with autospacing alone, R3 a plain paragraph with autospacing and the
		 * explicit values, R4 two list items with autospacing, R5 a plain control.
		 */
		PROBES.add(new Probe("spacing-autospacing-cell-list",
				"HTML auto spacing on a bulleted paragraph inside a table cell, with and without "
				+ "explicit w:before/w:after beside it; a plain paragraph the same; two items; a "
				+ "control", () -> {
			Doc d = Doc.create(15);
			d.para("Rows vary the paragraph inside a fixed 2-column table cell. " + prose(1)).add();
			Doc.Table t = new Doc.Table(4000, 4000).fixedLayout();
			t.build().getContent().clear();
			for (int r = 1; r <= 5; r++) {
				org.docx4j.wml.Tr tr = Doc.F.createTr();
				org.docx4j.wml.Tc left = Doc.F.createTc();
				left.getContent().add(d.para("R" + r).noLabel().build());
				org.docx4j.wml.Tc right = Doc.F.createTc();
				switch (r) {
				case 1:
					right.getContent().add(d.para("list item, auto before+after, explicit 100/100. " + prose(1, 1))
							.noLabel().autospacing(true, true).before(100).after(100).listItem().build());
					break;
				case 2:
					right.getContent().add(d.para("list item, auto before+after only. " + prose(1, 2))
							.noLabel().autospacing(true, true).listItem().build());
					break;
				case 3:
					right.getContent().add(d.para("plain, auto before+after, explicit 100/100. " + prose(1, 3))
							.noLabel().autospacing(true, true).before(100).after(100).build());
					break;
				case 4:
					right.getContent().add(d.para("first of two list items, auto before+after, explicit 100/100. " + prose(1, 4))
							.noLabel().autospacing(true, true).before(100).after(100).listItem().build());
					right.getContent().add(d.para("second list item, the same. " + prose(1, 5))
							.noLabel().autospacing(true, true).before(100).after(100).listItem().build());
					break;
				default:
					right.getContent().add(d.para("plain control, no spacing. " + prose(1, 6)).noLabel().build());
				}
				tr.getContent().add(left);
				tr.getContent().add(right);
				t.build().getContent().add(tr);
			}
			d.add(t.build());
			d.para("after the table. " + prose(1, 7)).add();
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
		 * §4.4 used to say "a line holding a tab is laid out from the left whatever the
		 * paragraph's w:jc".  Settled by this probe's golden: Word sizes the tabs as if
		 * the line began at the left indent, and then aligns the whole line - the tabs'
		 * widths counted in - by the w:jc; a justified line is laid out from the start.
		 * A leading tab, a mid-line tab and a trailing tab under each of centre, right
		 * and justified.
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
		 * document had seemed to contradict that inside a table cell; this probe's
		 * golden says the vertical rule holds there too, and adds one: Word draws the
		 * left and right borders outside the text area, so neither they nor their
		 * w:space narrow the text.  Same paragraph in and out of a cell, at three
		 * w:space values.
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

		// ------------------------------------- b2 batch 3: floating tables, unequal columns

		/*
		 * C2: a floating table (w:tblpPr) whose position Word measures from the page or
		 * from the margin box.  One case per page, each with a paragraph after it, so
		 * the golden shows both where Word puts the table and what it does with the text
		 * that follows: 17.1.0 places such a table absolutely only where it opens the
		 * section, because Word wraps the following text around it and XSL-FO cannot.
		 * The existing table-floating probe covers the common w:vertAnchor="text" case.
		 */
		PROBES.add(new Probe("table-floating-anchor",
				"floating tables anchored to the page and to the margin box: tblpX/tblpY, "
				+ "tblpXSpec centre and right, tblpYSpec top/centre/bottom", () -> {
			Doc d = Doc.create(15);

			// 1. opens the flow, anchored to the page at 72pt / 144pt
			Doc.Table a = new Doc.Table(2600, 2600).fixedLayout()
					.floating("page", "page", 1440, null, 2880, null);
			a.row(SERIF, 24, false, "page anchor", "x 72 y 144").row(SERIF, 24, false, prose(1, 1), "b");
			d.add(a.build());
			d.para("text after a page-anchored table at 72/144. " + prose(2)).after(240).add();
			d.pageBreak();

			// 2. centred on the text column, 156.8pt down the page (a Word cover page)
			Doc.Table b = new Doc.Table(2600, 2600).fixedLayout()
					.floating("page", "margin", null, "center", 3136, null);
			b.row(SERIF, 24, false, "page anchor", "centred on the margin")
					.row(SERIF, 24, false, prose(1, 2), "b");
			d.add(b.build());
			d.para("text after a centred page-anchored table. " + prose(2, 1)).after(240).add();
			d.pageBreak();

			// 3. tblpYSpec: the bottom of the margin box, with no w:vertAnchor at all
			Doc.Table c = new Doc.Table(2600, 2600).fixedLayout()
					.floating(null, null, null, null, null, "bottom");
			c.row(SERIF, 24, false, "no anchor", "tblpYSpec bottom")
					.row(SERIF, 24, false, prose(1, 3), "b");
			d.add(c.build());
			d.para("text after a table at the bottom of the margin box. " + prose(2, 2)).after(240).add();
			d.pageBreak();

			// 4. tblpYSpec centre of the page, tblpXSpec right of the page
			Doc.Table e = new Doc.Table(2600, 2600).fixedLayout()
					.floating("page", "page", null, "right", null, "center");
			e.row(SERIF, 24, false, "page anchor", "right, centred")
					.row(SERIF, 24, false, prose(1, 4), "b");
			d.add(e.build());
			d.para("text after a table centred on the page. " + prose(2, 3)).after(240).add();
			d.pageBreak();

			// 5. the margin box as the frame, with offsets
			Doc.Table f = new Doc.Table(2600, 2600).fixedLayout()
					.floating("margin", "margin", 1000, null, 2000, null);
			f.row(SERIF, 24, false, "margin anchor", "x 50 y 100")
					.row(SERIF, 24, false, prose(1, 5), "b");
			d.add(f.build());
			d.para("text after a margin-anchored table at 50/100. " + prose(2, 4)).after(240).add();
			d.pageBreak();

			// 6. a narrow table with a lot of text after it: does Word flow text beside it?
			d.para("before the narrow table. " + prose(1, 6)).after(240).add();
			Doc.Table g = new Doc.Table(1600, 1600).fixedLayout()
					.floating("page", "margin", null, "right", 4320, null);
			g.row(SERIF, 20, false, "narrow", "table").row(SERIF, 20, false, "beside", "text");
			d.add(g.build());
			for (int i = 0; i < 5; i++) {
				d.para(prose(4, i + 7)).after(160).add();
			}
			return d.pkg();
		}));

		/*
		 * C6: a section whose w:cols declares columns of different widths, and a run of
		 * merged continuous sections whose w:pgMar differ by a lot.  17.1.0 renders an
		 * unequal-column stretch as a one-row table, split at the w:br w:type="column";
		 * this probe has the same stretch with the break and without it, and a following
		 * continuous section whose margins are 100pt wider, so the golden shows both what
		 * Word's column boundaries are and which margins it starts the page with.
		 */
		PROBES.add(new Probe("columns-unequal",
				"unequal w:cols (157 + 24 + 318pt) with and without a column break, "
				+ "between continuous sections whose w:pgMar differ by 100pt", () -> {
			Doc d = Doc.create(15);
			// section 1: one column, 152 / 186pt margins
			d.pageGeometry(11900, 16840, false, 1440, 3720, 1440, 3040);
			d.columns(new int[] { 5140 }, new int[] { 720 });
			d.para("Section 1, one column, wide margins. " + prose(2)).after(240).add();
			d.endSection("continuous");

			// section 2: two unequal columns, 51 / 45pt margins, divided by a column break
			d.pageGeometry(11900, 16840, false, 1440, 900, 1440, 1020);
			d.columns(new int[] { 3140, 6360 }, new int[] { 480, 0 });
			d.para("Column one of two unequal columns. " + prose(2, 1)).after(120).add();
			d.para("The last line of column one.").columnBreak().after(120).add();
			d.para("Column two, which is twice as wide. " + prose(3, 2)).after(120).add();
			d.endSection("continuous");

			// section 3: the same unequal columns with no column break at all
			d.pageGeometry(11900, 16840, false, 1440, 900, 1440, 1020);
			d.columns(new int[] { 3140, 6360 }, new int[] { 480, 0 });
			d.para("No column break here, so Word balances the two unequal columns itself. "
					+ prose(6, 3)).after(120).add();
			d.endSection("continuous");

			// section 4: one column again, back to the wide margins
			d.pageGeometry(11900, 16840, false, 1440, 3720, 1440, 3040);
			d.columns(new int[] { 5140 }, new int[] { 720 });
			d.para("Section 4, one column, wide margins again. " + prose(3, 4)).before(240).add();
			return d.pkg();
		}));

		// --------------------------------- b2 batch 8: over-wide grids, the grid edge
		//                                   below mode 14, and pages with nothing on them

		/*
		 * E4/E5: what Word does with a grid-sized table (w:tblW in dxa and every w:tcW in
		 * dxa) whose w:tblGrid is wider than the text column.  Two corpus measurements
		 * disagree - a 29%-over grid with w:tblInd -1300 is honoured in full and
		 * overhangs both margins, a 1.7%-over grid with w:tblInd 0 is fitted into the
		 * column - so the question is whether the cut is the size of the overhang or the
		 * indent.  One case a page, each with a paragraph after it whose lines show where
		 * the text column is.  The text column here is 9026 twips (A4, 1in margins).
		 */
		PROBES.add(new Probe("table-grid-overwide",
				"grid-sized tables (w:tblW and every w:tcW in dxa) whose w:tblGrid is 1.7%, 10% "
				+ "and 29% wider than the text column, at w:tblInd 0 and w:tblInd -1300, "
				+ "autofit and fixed layout", () -> {
			Doc d = Doc.create(15);
			int[][] grids = { { 2200, 3400, 3580 },    // 9180tw = 1.7% over
			                  { 2400, 3700, 3829 },    // 9929tw = 10% over
			                  { 2800, 4400, 4444 } };  // 11644tw = 29% over
			String[] over = { "1.7%", "10%", "29%" };
			int page = 0;
			for (int g = 0; g < grids.length; g++) {
				for (int variant = 0; variant < 3; variant++) {
					if (page++ > 0) d.pageBreak();
					// variant 0: tblInd 0; 1: tblInd -1300; 2: tblInd 0, fixed layout
					String what = over[g] + " over, w:tblInd " + (variant == 1 ? "-1300" : "0")
							+ (variant == 2 ? ", fixed layout" : "");
					d.para(what + ". " + prose(1, g)).after(240).add();
					Doc.Table t = new Doc.Table(grids[g]).indent(variant == 1 ? -1300 : 0);
					if (variant == 2) t.fixedLayout();
					t.row(SERIF, 20, false, "A " + over[g], "B " + over[g], "C " + over[g]);
					t.row(SERIF, 20, false, prose(1, g + 1), prose(1, g + 2), prose(1, g + 3));
					d.add(t.build());
					d.para("after the table. " + prose(2, g + 4)).before(240).add();
				}
			}
			return d.pkg();
		}));

		/*
		 * The all-auto twin of table-grid-overwide: the same grids, but every w:tcW auto
		 * and a w:tblW in dxa which is the grid's own sum, which since 17.1.1 makes the
		 * grid authoritative (§6.3, "a grid the table's own w:tblW sums to wins").  A
		 * 7 Sep unit test (TablePositionTest.fitToPage) had such a table - 12000 twips
		 * of grid on a 9026-twip column - clamped to the column, and the grid rule now
		 * draws it at 600pt; §6.5's corpus measurement has Word overhanging the margin
		 * for grids 3-19% wide, but with dxa cells.  Variants: tblInd 0, tblInd -1300,
		 * and w:tblW absent (an autofit table, whose grid Word recomputes).
		 */
		PROBES.add(new Probe("table-grid-overwide-auto",
				"the table-grid-overwide grids (1.7%, 10% and 29% wider than the text column) "
				+ "with every w:tcW auto: w:tblW in dxa equal to the grid's sum at w:tblInd 0 "
				+ "and -1300, and w:tblW absent", () -> {
			Doc d = Doc.create(15);
			int[][] grids = { { 2200, 3400, 3580 },    // 9180tw = 1.7% over
			                  { 2400, 3700, 3829 },    // 9929tw = 10% over
			                  { 2800, 4400, 4444 } };  // 11644tw = 29% over
			String[] over = { "1.7%", "10%", "29%" };
			int page = 0;
			for (int g = 0; g < grids.length; g++) {
				int sum = grids[g][0] + grids[g][1] + grids[g][2];
				for (int variant = 0; variant < 3; variant++) {
					if (page++ > 0) d.pageBreak();
					// variant 0: w:tblW = grid sum, tblInd 0; 1: the same at tblInd -1300;
					// 2: no w:tblW (autofit), tblInd 0
					String what = over[g] + " over, every w:tcW auto, "
							+ (variant == 2 ? "w:tblW absent" : "w:tblW " + sum + " dxa")
							+ ", w:tblInd " + (variant == 1 ? "-1300" : "0");
					d.para(what + ". " + prose(1, g)).after(240).add();
					Doc.Table t = new Doc.Table(grids[g]).indent(variant == 1 ? -1300 : 0);
					if (variant == 2) t.autoWidth(); else t.tableWidth(sum, "dxa");
					t.row(SERIF, 20, true, "A " + over[g], "B " + over[g], "C " + over[g]);
					t.row(SERIF, 20, true, prose(1, g + 1), prose(1, g + 2), prose(1, g + 3));
					d.add(t.build());
					d.para("after the table. " + prose(2, g + 4)).before(240).add();
				}
			}
			return d.pkg();
		}));

		/*
		 * §6.1's grid-edge shift is applied in compatibility mode 14 alone since 17.1.0,
		 * on the strength of one corpus document with no compatibilityMode setting at all
		 * (mode 12).  These two probes are the Word measurement for modes 12 and 11, and
		 * they carry the nested case (§6.1's nested rule was measured in mode 14 only).
		 */
		PROBES.add(gridEdgeNestedProbe(12));
		PROBES.add(gridEdgeNestedProbe(11));

		/*
		 * The shapes suspected of costing (or losing) a page where the content of the two
		 * documents otherwise agrees.  Each section is a page or two, and what matters in
		 * the golden is simply how many pages there are and which of them carry text.
		 */
		PROBES.add(new Probe("page-blank",
				"pages with nothing on them: a nextPage section break after a page-filling "
				+ "table, oddPage and evenPage breaks whose next page is already right, a "
				+ "paragraph holding only a page break, and a document ending in a page break", () -> {
			Doc d = Doc.create(15);

			// 1. a table 32 rows of exactly 20pt tall - 664pt of a 698pt body, so the
			//    page is full and nothing more can go on it - then a nextPage break
			d.para("Section 1 opens with a table which fills the page.").after(0).add();
			Doc.Table t = new Doc.Table(4500, 4500);
			for (int i = 0; i < 32; i++) {
				t.rowOf(400, org.docx4j.wml.STHeightRule.EXACT,
						t.cell("row " + (i + 1), SERIF, 24, 1, 4500),
						t.cell("value " + (i + 1), SERIF, 24, 1, 4500));
			}
			d.add(t.build());
			d.endSection("nextPage", 0);

			// 2. w:type oddPage and evenPage sections: does Word add a filler page where the
			//    page the section would open already has the parity asked for, and where it
			//    does not?  (Doc.endSection names the type of the section it opens.)
			d.para("Section 2, one page; the section after it is w:type oddPage.").after(0).add();
			d.endSection("oddPage", 0);
			d.para("Section 3, one page; the section after it is w:type evenPage.").after(0).add();
			d.endSection("evenPage", 0);
			d.para("Section 4, one page; the section after it is w:type evenPage again.").after(0).add();
			d.endSection("evenPage", 0);
			d.para("Section 5, one page; the section after it is a plain one.").after(0).add();
			d.endSection("nextPage", 0);
			// the last section is a plain one: the trailing break must not be confounded
			// with a parity filler
			d.sectPr().setType(null);

			// 3. a paragraph whose only content is a page break, mid-document (control)
			d.para("Section 6. The next paragraph holds nothing but a page break. "
					+ prose(2, 4)).after(240).add();
			d.pageBreak();
			d.para("After the page-break-only paragraph. " + prose(2, 5)).after(240).add();

			// 4. and the document ends with one: does Word add a page for it?
			d.para("The last paragraph with text on it. " + prose(2, 6)).after(240).add();
			d.pageBreak();
			return d.pkg();
		}));

		// --------------------------------- b2 batch 9: right and dot-leader tabs (E25)

		/*
		 * Which stop's leader a tab draws.  docx4j gave the n-th tab the n-th stop's
		 * leader until 17.1.0, which loses the dots of every table of contents entry
		 * whose tab count differs from its stop count; the rule the goldens are to
		 * confirm is that the leader is the one of the stop the tab REACHES.  The stops
		 * are a corpus TOC's (360/540/851 left with no leader, 9990 right with dots) and
		 * the lines differ in how many tabs they have and where they start.
		 */
		PROBES.add(new Probe("tab-leader-resolved",
				"one dot-leader stop (9000 right) behind three plain left stops, on lines with "
				+ "one, two and three tabs and on a line whose first tab already sits past the "
				+ "last left stop", () -> {
			Doc d = Doc.create(15);
			d.para("Table of contents entries; the only stop with a leader is the last.").after(240).add();
			// (a) a leading tab and text: the tab reaches the first left stop
			tocStops(d.para().noLabel()).tab().text("leading tab, then text").after(120).add();
			// (b) text, one tab, text: the tab passes 360/540/851 and reaches the dot stop
			tocStops(d.para().noLabel()).text("Foreword").tab().text("9").after(120).add();
			// (c) three fields as a numbered entry: "1." tab "Scope" tab "9"
			tocStops(d.para().noLabel()).text("1.").tab().text("Scope").tab().text("9").after(120).add();
			// (d) the first tab already sits past the last left stop
			tocStops(d.para().noLabel())
					.text("A first line long enough that its tab starts past the last left stop")
					.tab().text("10").after(120).add();
			// (e) two tabs where only the second reaches the dot stop
			tocStops(d.para().noLabel()).text("2.").tab().text("Terms and definitions").tab().text("11").after(240).add();
			d.para("after. " + prose(1)).add();
			return d.pkg();
		}));

		/*
		 * The trailing-tab class of the same signature: a corpus TOC entry ends
		 * <w:tab/><w:t/><w:tab/>, so the tab that reaches the dot stop is the FIRST of
		 * them and the second runs on to the next default stop, which has no leader.
		 */
		PROBES.add(new Probe("tab-leader-trailing",
				"paragraphs ending in a tab, and in two tabs with nothing after either, against "
				+ "a right dot-leader stop", () -> {
			Doc d = Doc.create(15);
			d.para("Entries whose tabs are trailing.").after(240).add();
			tocStops(d.para().noLabel()).text("Trailing tab, nothing after it").tab().after(120).add();
			tocStops(d.para().noLabel()).text("Two trailing tabs").tab().tab().after(120).add();
			tocStops(d.para().noLabel()).text("Two trailing tabs and a number").tab().tab().text("12").after(120).add();
			tocStops(d.para().noLabel()).tab().tab().after(240).add();
			d.para("after. " + prose(1)).add();
			return d.pkg();
		}));

		/*
		 * A table of contents whose page numbers are PAGEREF fields: FOP measures an
		 * unresolved fo:page-number-citation as the placeholder "MMM", so the number
		 * landed width("MMM") - width(number) short of the stop.  Page numbering starts
		 * at 98 in the second section, so the entries carry numbers of one, two and
		 * three digits.  The last entry points at a bookmark the document does not
		 * contain, which is what a TOC left behind by editing does: Word paints the
		 * cached result.
		 */
		PROBES.add(new Probe("tab-toc-pageref",
				"table of contents entries whose numbers are PAGEREF fields (1, 2 and 3 digits) "
				+ "against a 9020 right stop with dots, and one whose bookmark is gone", () -> {
			Doc d = Doc.create(15);
			d.para("Contents").font(SANS, 28).after(240).add();
			String[] headings = { "Scope", "Normative references", "Terms and definitions",
					"Requirements", "Test methods", "Marking and labelling" };
			for (int i = 0; i < headings.length; i++) {
				tocEntryStops(d.para().noLabel()).text(headings[i]).tab()
						.pageref("_Toc9000" + i, Integer.toString(i + 1)).after(0).add();
			}
			tocEntryStops(d.para().noLabel()).text("An entry whose bookmark has been deleted").tab()
					.pageref("_TocMissing", "42").after(240).add();

			// the headings themselves, one per page; the second section numbers its pages
			// from 98, so three of the entries above resolve to three digits
			for (int i = 0; i < 3; i++) {
				d.para(headings[i]).font(SANS, 26).bookmark("_Toc9000" + i).after(120).add();
				d.para(prose(6, i)).add();
				d.pageBreak();
			}
			d.endSection("nextPage", 0);
			d.pageNumberStart(98);
			for (int i = 3; i < headings.length; i++) {
				d.para(headings[i]).font(SANS, 26).bookmark("_Toc9000" + i).after(120).add();
				d.para(prose(6, i)).add();
				if (i < headings.length - 1) d.pageBreak();
			}
			return d.pkg();
		}));

		/*
		 * A centre or right stop whose text would run past the right indent.  §4.4 says
		 * a stop beyond the indent is still honoured and the line runs into it, which is
		 * measured for a LEFT stop; a corpus footer says Word clamps a centre stop so
		 * that the text ends on the indent instead of overflowing and wrapping.  The
		 * stops are that footer's (4252 clear, 8504 clear, 9355 centre) and the content
		 * is sized either side of the point at which the aligned text fills the width.
		 */
		PROBES.add(new Probe("tab-clamp-right",
				"centre and right stops close to and past the right indent, with text short "
				+ "enough to fit and long enough to overrun", () -> {
			Doc d = Doc.create(15);
			String[] texts = { "SHORT", "A MEDIUM LENGTH LINE OF TEXT",
					"A CONSIDERABLY LONGER LINE OF TEXT WHICH WOULD OVERRUN THE INDENT DE 20" };
			for (String text : texts) {
				d.para().noLabel().font(SANS, 16).jc(JcEnumeration.CENTER)
						.tabStop(4252, org.docx4j.wml.STTabJc.CLEAR)
						.tabStop(8504, org.docx4j.wml.STTabJc.CLEAR)
						.tabStop(9355, org.docx4j.wml.STTabJc.CENTER)
						.text("centre stop 9355: ").tab().text(text).after(120).add();
				d.para().noLabel().font(SANS, 16)
						.tabStop(9355, org.docx4j.wml.STTabJc.RIGHT)
						.text("right stop 9355: ").tab().text(text).after(120).add();
				d.para().noLabel().font(SANS, 16)
						.tabStop(9355, org.docx4j.wml.STTabJc.LEFT)
						.text("left stop 9355: ").tab().text(text).after(240).add();
			}
			d.para("after. " + prose(1)).add();
			return d.pkg();
		}));

		// --------------------------------- b2 batch 13 triage: PROBES ONLY.  Each of
		//                                   these five settles a rule the corpus suggests
		//                                   but no Word golden has yet answered; nothing
		//                                   is coded until the goldens are back.

		/*
		 * G9: §6.1's below-mode-15 grid edge is capped at min(shift, max(0, w:tblInd)), so
		 * the shift is cancelled where the indent is zero, absent or negative.  Two corpus
		 * measurements contradict that cap in opposite directions and 150 documents move
		 * with it, so Word decides:
		 *
		 *   (a) a NEGATIVE w:tblInd.  A mode-12 header table at w:tblInd -736 has Word's
		 *       grid edge at margin + tblInd - one cell margin: the shift is taken in
		 *       full, and the cap - which reads a signed indent and so caps at 0 - cancels
		 *       it (dx +5.7 with the width identical).  Here at -108 (one cell margin, the
		 *       cap's own boundary) and at -360.
		 *   (b) a FIXED-LAYOUT table with NO w:tblInd at all.  Word puts the first cell's
		 *       text on the page margin, i.e. it takes the shift exactly where the cap
		 *       forbids it (+5.5pt on every line of every table).  This contradicts the
		 *       document the cap was derived from, whose table is autofit, so the autofit
		 *       twin is on the page after each fixed one.
		 *
		 * Grid-sized throughout (w:tblW in dxa and every w:tcW in dxa), one case a page,
		 * with Word's default cell margins and with the table's own w:tblCellMar 108.  The
		 * mode-15 twin says whether any of it is mode-dependent at all.
		 */
		PROBES.add(gridEdgeSignedProbe(12));
		PROBES.add(gridEdgeSignedProbe(14));
		PROBES.add(gridEdgeSignedProbe(15));

		/*
		 * G7: where Word emits a page with nothing on it.  §10 long said that no page of
		 * either PDF is empty; one corpus document has six wholly empty pages (no text, no
		 * image, no path) and another a near-empty one, and until those shapes are
		 * measured §3.3's page-break rules rest on one probe.  Reading the two documents
		 * settles what the shapes are, and both are the opposite way round from the
		 * obvious guess:
		 *
		 *   - all six of the first document's empty pages are an empty paragraph carrying
		 *     a w:sectPr with NO w:type (so nextPage) followed by a paragraph whose only
		 *     content is w:br w:type="page".  The section break opens the page and the
		 *     break paragraph is the whole of it.  The same document has three other
		 *     break-only paragraphs which do NOT follow a section break, and none of them
		 *     costs a page - so it is the adjacency that matters, and that is S1 here.
		 *   - the second document's near-empty page is TWO consecutive w:br w:type="page"
		 *     runs at the head of a paragraph, inside a table cell (S2b).
		 *
		 * page-blank already carries the exact-row table then nextPage, the oddPage and
		 * evenPage parities, a page-break-only paragraph mid-document and a document
		 * ending in a page break; these are the other shapes.  Nothing carries a header or
		 * a footer until the last section, so an empty page here is wholly empty.
		 */
		PROBES.add(new Probe("page-empty",
				"the shapes Word gives a page of its own to: a sectPr with no w:type then a "
				+ "break-only paragraph, two page breaks in one paragraph and in a table "
				+ "cell, a page break then an empty paragraph then a nextPage break, a page "
				+ "break then a nextPage break, a page-filling table then w:pageBreakBefore, "
				+ "w:pageBreakBefore opening a nextPage section, and a last section holding "
				+ "only a header, a footer and an empty paragraph", () -> {
			Doc d = Doc.create(15);

			// S1 - the corpus shape, six times over in one document: an empty paragraph
			//      carrying a w:sectPr with no w:type, then a paragraph holding only a
			//      page break
			d.para("S1. An empty paragraph carrying a w:sectPr with no w:type follows, and "
					+ "then a paragraph whose only content is a page break. " + prose(1)).after(120).add();
			d.sectionBreakHere(null, 0);
			d.pageBreak();
			d.para("S1 after. " + prose(1, 1)).after(240).add();

			// S2 - two page breaks in one paragraph, and two break-only paragraphs
			d.para("S2. Two page breaks in one paragraph follow. " + prose(1, 2)).after(120).add();
			d.para().noLabel().pageBreakRun().pageBreakRun().add();
			d.para("S2 after. " + prose(1, 3)).after(240).add();

			d.para("S2a. Two paragraphs each holding one page break follow. "
					+ prose(1, 4)).after(120).add();
			d.pageBreak();
			d.pageBreak();
			d.para("S2a after. " + prose(1, 5)).after(240).add();

			// S2b - the second corpus document's shape: two page breaks at the head of a
			//       paragraph inside a table cell, before the cell's own content
			d.para("S2b. A one-row table whose first cell opens with two page breaks "
					+ "follows. " + prose(1, 6)).after(120).add();
			Doc.Table cellBreaks = new Doc.Table(4500, 4500);
			cellBreaks.rowOf(null, null,
					cellBreaks.cellOf(4500, null, d.para().noLabel()
							.pageBreakRun().pageBreakRun().text("S2b cell content").build()),
					cellBreaks.cellOf(4500, null, Doc.plainParagraph("S2b right cell", SERIF, 24)));
			d.add(cellBreaks.build());
			d.para("S2b after. " + prose(1, 7)).before(240).after(240).add();

			// S3 - a page break, an empty paragraph, then a nextPage section break: the
			//      corpus shape's mirror image
			d.para("S3. A page break, then an empty paragraph, then a nextPage section "
					+ "break. " + prose(1, 1)).after(120).add();
			d.pageBreak();
			d.emptyParagraph();
			d.sectionBreakHere("nextPage", 0);
			d.para("S3 after. " + prose(1, 2)).after(240).add();

			// S4 - a page break immediately followed by a nextPage section break
			d.para("S4. A page break, then a nextPage section break with nothing between "
					+ "them. " + prose(1, 3)).after(120).add();
			d.pageBreak();
			d.sectionBreakHere("nextPage", 0);
			d.para("S4 after. " + prose(1, 4)).after(240).add();

			// S5 - a table of exact rows filling the page, then w:pageBreakBefore
			//      (page-blank has the same table followed by a nextPage break)
			d.para("S5. A table of exact rows fills the rest of this page; the paragraph "
					+ "after it carries w:pageBreakBefore.").after(0).add();
			Doc.Table filler = new Doc.Table(4500, 4500);
			for (int i = 0; i < 31; i++) {
				filler.rowOf(400, org.docx4j.wml.STHeightRule.EXACT,
						filler.cell("S5 row " + (i + 1), SERIF, 24, 1, 4500),
						filler.cell("value " + (i + 1), SERIF, 24, 1, 4500));
			}
			d.add(filler.build());
			d.para("S5 after, on a page of its own by w:pageBreakBefore. " + prose(1, 5))
					.pageBreakBefore().after(240).add();

			// S6 - w:pageBreakBefore on the first paragraph of a nextPage section, which
			//      has opened a page already
			d.para("S6. A nextPage section break follows, and the first paragraph of the "
					+ "section it opens carries w:pageBreakBefore too. " + prose(1, 6)).after(120).add();
			d.sectionBreakHere("nextPage", 0);
			d.para("S6 after. This paragraph carries w:pageBreakBefore. " + prose(1, 7))
					.pageBreakBefore().after(240).add();

			// S7 - the trailing page whose body is empty: a last section holding nothing
			//      but a header, a footer and one empty paragraph (ledger class C, five
			//      documents, where our last page carries the running header alone)
			d.para("S7. The last section follows: it holds only a header, a footer and one "
					+ "empty paragraph, so its page has no body. " + prose(1)).after(120).add();
			d.sectionBreakHere("nextPage", 0);
			d.addHeader(SANS, 20, "S7 running header");
			d.addFooter(SANS, 20, "S7 running footer");
			d.emptyParagraph();
			return d.pkg();
		}));

		/*
		 * The line a page break ends is on the page before the break, and the paragraph
		 * mark sizes it: a 7-page corpus document whose break-only paragraph carries the
		 * Title style's 28pt mark ends its page 4 17.5pt short of the margin, and Word's
		 * page 5 is empty (the 34.18pt line went there, and the break opened page 6).
		 * docx4j had folded the break paragraph into what follows it, losing the line and
		 * the page.  A and B are that shape with an 11pt mark (fits) and a 28pt one (does
		 * not); C and D put an empty section-break paragraph in the same place, with the
		 * same two marks, since §3 records Word giving that paragraph no line at a
		 * continuous break and a 32-page cover page (14pt marks, 1pt from the margin)
		 * suggests it takes one at a nextPage break; E and F are the mid-page controls, a
		 * continuous section-break paragraph and a plain empty paragraph with 28pt marks
		 * between two prose paragraphs, so the line each takes can be read off the gap.
		 */
		PROBES.add(new Probe("page-break-line",
				"a page filled to 25.9pt of its foot by 28 exact 24pt lines, then a break-only "
				+ "paragraph whose mark is 11pt (A) or 28pt (B); the same page then an empty "
				+ "nextPage section-break paragraph with an 11pt (C) or 28pt (D) mark; and "
				+ "mid-page, a continuous section-break paragraph (E) and an empty paragraph "
				+ "(F) each with a 28pt mark", () -> {
			Doc d = Doc.create(15);
			String[] cases = { "A", "B", "C", "D" };
			int[] marks = { 22, 56, 22, 56 };
			for (int c = 0; c < cases.length; c++) {
				// 28 lines of exactly 24pt: 672pt of the A4 body's 697.9pt, 25.9pt left
				for (int i = 0; i < 28; i++) {
					String t = i == 0
							? cases[c] + ". 28 exact 24pt lines, then a " + (marks[c] / 2)
									+ "pt-mark " + (c < 2 ? "page-break" : "nextPage section-break")
									+ " paragraph"
							: cases[c] + " line " + (i + 1) + " of 28";
					d.para(t).noLabel().font(SERIF, 24).line(480, STLineSpacingRule.EXACT).add();
				}
				if (c < 2) {
					P brk = F.createP();
					PPr ppr = F.createPPr();
					PPrBase.Spacing sp = F.createPPrBaseSpacing();
					sp.setBefore(BigInteger.ZERO);
					sp.setAfter(BigInteger.ZERO);
					sp.setLine(BigInteger.valueOf(240));
					sp.setLineRule(STLineSpacingRule.AUTO);
					ppr.setSpacing(sp);
					brk.setPPr(ppr);
					R r = F.createR();
					Br br = F.createBr();
					br.setType(STBrType.PAGE);
					r.getContent().add(br);
					brk.getContent().add(r);
					markSize(brk, marks[c]);
					d.add(brk);
				} else {
					d.sectionBreakHere("nextPage", 0);
					P sect = (P) d.mdp().getContent().get(d.mdp().getContent().size() - 1);
					markSize(sect, marks[c]);
				}
				d.para(cases[c] + " after: the paragraph after the break. " + prose(1, c))
						.noLabel().font(SERIF, 24).after(240).add();
				d.pageBreak();
			}
			d.para("E. A continuous section-break paragraph with a 28pt mark follows this "
					+ "paragraph. " + prose(2, 4)).noLabel().font(SERIF, 24).after(240).add();
			d.sectionBreakHere("continuous", 0);
			markSize((P) d.mdp().getContent().get(d.mdp().getContent().size() - 1), 56);
			d.para("E after. " + prose(2, 5)).noLabel().font(SERIF, 24).after(240).add();
			d.para("F. An empty paragraph with a 28pt mark follows this paragraph. "
					+ prose(2, 6)).noLabel().font(SERIF, 24).after(240).add();
			markSize(d.emptyParagraph(), 56);
			d.para("F after. " + prose(2, 7)).noLabel().font(SERIF, 24).after(240).add();
			return d.pkg();
		}));

		/*
		 * Which pages take which geometry across a continuous section break that changes
		 * it.  A 29-page corpus document (w:top="0" then w:top="1417", both continuous)
		 * has a last page holding nothing but its footer, where docx4j runs the trailing
		 * paragraphs on with the page before; and a 4-page letter whose letterhead
		 * section says w:footer="5811" (290.55pt) and whose second, continuous, section
		 * says 709 ends its pages 1 and 2 at y=531 and 529 - the first section's footer
		 * distance plus its empty footer's line - and page 3 at 762, the second's, though
		 * the second section begins on page 1.  §7 had read that document's page 3 as
		 * Word ignoring an absurd footer distance, which FOPAreaTreeHelper still clamps.
		 * S1 to S5 change one thing at a time across a continuous break: the top margin,
		 * the footer distance with an empty footer part (5811, then 709), and the left
		 * margin; each part is more than a page long, so the page a change takes effect
		 * on can be read from where its lines fall.
		 */
		PROBES.add(new Probe("section-continuous-geometry",
				"five continuous sections each over a page long: S1 default margins; S2 a "
				+ "2in top margin; S3 w:footer=5811 with an empty footer part and w:bottom="
				+ "1418; S4 w:footer=709 with another empty footer part; S5 a 2in left margin",
				() -> {
			Doc d = Doc.create(15);
			for (int i = 0; i < 20; i++) d.para("S1 " + prose(2, i)).noLabel().font(SERIF, 24).after(160).add();
			d.endSection("continuous", 0);
			d.pageGeometry(11906, 16838, false, 2880, 1440, 1440, 1440);
			for (int i = 0; i < 20; i++) d.para("S2 " + prose(2, i + 3)).noLabel().font(SERIF, 24).after(160).add();
			d.endSection("continuous", 0);
			d.pageGeometry(11906, 16838, false, 1440, 1440, 1418, 1440);
			d.headerFooterDistance(708, 5811);
			d.addFooter(org.docx4j.wml.HdrFtrRef.DEFAULT, java.util.Collections.singletonList(F.createP()));
			for (int i = 0; i < 20; i++) d.para("S3 " + prose(2, i + 6)).noLabel().font(SERIF, 24).after(160).add();
			d.endSection("continuous", 0);
			d.headerFooterDistance(708, 709);
			d.addFooter(org.docx4j.wml.HdrFtrRef.DEFAULT, java.util.Collections.singletonList(F.createP()));
			for (int i = 0; i < 20; i++) d.para("S4 " + prose(2, i + 9)).noLabel().font(SERIF, 24).after(160).add();
			d.endSection("continuous", 0);
			d.pageGeometry(11906, 16838, false, 1440, 1440, 1440, 2880);
			for (int i = 0; i < 20; i++) d.para("S5 " + prose(2, i + 12)).noLabel().font(SERIF, 24).after(160).add();
			return d.pkg();
		}));

		/*
		 * b2-batch11 left a residual on two probe lines which is the leader's font size,
		 * and §4.4 says a line holding only a tab takes its height "from the block's
		 * font".  Word appears instead to size both the lone tab's line and its leader
		 * dots by the TAB RUN's own font - and a tab Word writes between two runs carries
		 * no w:rPr at all, so that font is whatever a bare run inherits (Word's own
		 * application default, Aptos 11.04pt, where the document declares nothing).  The
		 * paragraphs' text runs here are 8pt, so a tab sized by the paragraph and a tab
		 * sized by the application default are far apart.
		 *
		 * A, B, C are leader lines whose tab run carries no w:rPr, the paragraph's 8pt,
		 * and 14pt; D to H put a lone tab on a line of its own between two prose
		 * paragraphs, so its height can be read off the gap - G and H vary the paragraph
		 * mark's size against the tab run's, which is the other candidate (§2.5).  I and J
		 * are the inheritance case: a style saying 8pt in a document whose w:docDefaults
		 * say 11pt Carlito, with every run bare.
		 */
		PROBES.add(new Probe("tab-run-font",
				"a tab run with no w:rPr, with the paragraph's 8pt and with 14pt, on leader "
				+ "lines and on lines holding nothing but the tab; the paragraph mark's size "
				+ "against the tab run's; and a bare tab where w:docDefaults say 11pt and the "
				+ "style 8pt", () -> {
			Doc d = Doc.create(15);
			// the document defaults are 11pt Carlito and the style says 8pt: a bare run
			// takes one of them, and which one is I and J below
			d.documentDefaultRun(CARLITO, 22);
			d.addParagraphStyle("Small8", null, null, Doc.font(SERIF, 16));

			d.para("Tab runs at three sizes against a 9000 right stop with dots; every text "
					+ "run of A, B and C is 8pt. " + prose(1)).font(SERIF, 20).after(240).add();
			leaderStop(d.para().noLabel().font(SERIF, 16))
					.text("A. the tab run has no w:rPr").tab().text("9").after(120).add();
			leaderStop(d.para().noLabel().font(SERIF, 16))
					.text("B. the tab run carries 8pt").tab(SERIF, 16).text("9").after(120).add();
			leaderStop(d.para().noLabel().font(SERIF, 16))
					.text("C. the tab run carries 14pt").tab(SERIF, 28).text("9").after(240).add();

			d.para("D. A line holding nothing but a tab whose run has no w:rPr follows.")
					.font(SERIF, 20).after(0).add();
			leaderStop(d.para().noLabel().font(SERIF, 16)).tab().after(0).add();
			d.para("D after.").font(SERIF, 20).after(240).add();

			d.para("E. A line holding nothing but a tab whose run carries 8pt follows.")
					.font(SERIF, 20).after(0).add();
			leaderStop(d.para().noLabel().font(SERIF, 16)).tab(SERIF, 16).after(0).add();
			d.para("E after.").font(SERIF, 20).after(240).add();

			d.para("F. A line holding nothing but a tab whose run carries 14pt follows.")
					.font(SERIF, 20).after(0).add();
			leaderStop(d.para().noLabel().font(SERIF, 16)).tab(SERIF, 28).after(0).add();
			d.para("F after.").font(SERIF, 20).after(240).add();

			d.para("G. A lone tab run with no w:rPr, whose paragraph mark is 14pt, follows.")
					.font(SERIF, 20).after(0).add();
			leaderStop(d.para().noLabel().font(SERIF, 16)).markSize(28).tab().after(0).add();
			d.para("G after.").font(SERIF, 20).after(240).add();

			d.para("H. A lone 14pt tab run whose paragraph mark is 8pt follows.")
					.font(SERIF, 20).after(0).add();
			leaderStop(d.para().noLabel().font(SERIF, 16)).markSize(16).tab(SERIF, 28).after(0).add();
			d.para("H after.").font(SERIF, 20).after(240).add();

			d.para("I and J are styled Small8, which says 8pt, in a document whose "
					+ "w:docDefaults say 11pt Carlito; none of their runs carries a w:rPr.")
					.font(SERIF, 20).after(120).add();
			leaderStop(d.para().noLabel().style("Small8"))
					.bareText("I. bare runs and a bare tab").tab().bareText("9").after(120).add();
			d.para("J. A line holding nothing but a bare tab, styled Small8, follows.")
					.font(SERIF, 20).after(0).add();
			leaderStop(d.para().noLabel().style("Small8")).tab().after(0).add();
			d.para("J after.").font(SERIF, 20).after(240).add();

			d.para("after. " + prose(1, 1)).font(SERIF, 20).add();
			return d.pkg();
		}));

		/*
		 * b2-batch12's G2 fix (a picture-only paragraph's line box is max(existing,
		 * picture), the baseline at its foot and no descent) cost 10.3pt on a vertically
		 * centred header cell holding a small picture: the cell, and with it the header's
		 * measured extent and every page's body top, grew.  What Word does with such a
		 * cell has never been measured, so this is that measurement, with the same cell in
		 * the body as the control and the two line-spacing cases the rule cancels or keeps
		 * (a multiple, w:spacing line=276 lineRule=auto, and an exact line shorter than
		 * the picture).  The picture inside an italic run is the shape which produces an
		 * fo:inline, and so the line box the rule has to reckon with.
		 */
		PROBES.add(new Probe("picture-header-cell",
				"a 24pt inline picture in a table cell with w:vAlign center, in a header and "
				+ "in the body, alone in the cell and with text under it, against the same "
				+ "table with no w:vAlign; and picture-only paragraphs at line=276 auto, at "
				+ "an exact 12pt line, and inside an italic run", () -> {
			Doc d = Doc.create(15);

			d.para("Section 1. Its header holds a two-row table whose left cell is "
					+ "w:vAlign center and holds a 24pt inline picture; the table below is "
					+ "the same one in the body. " + prose(2)).after(240).add();
			d.add(pictureCellTable(d, "body1", "center"));
			d.para("after the body twin of the header's table. " + prose(2, 1)).before(240).after(240).add();
			d.addHeaderContent(org.docx4j.wml.HdrFtrRef.DEFAULT, java.util.Arrays.asList(
					(Object) Doc.plainParagraph("H1: the cells below are w:vAlign center", SANS, 16),
					pictureCellTable(d, "hdr1", "center"),
					Doc.plainParagraph("", SANS, 16)));
			d.sectionBreakHere("nextPage", 0);

			d.para("Section 2. The same header and the same body table, with no w:vAlign "
					+ "on any cell. " + prose(2, 2)).after(240).add();
			d.add(pictureCellTable(d, "body2", null));
			d.para("after the body twin. " + prose(2, 3)).before(240).after(240).add();
			d.addHeaderContent(org.docx4j.wml.HdrFtrRef.DEFAULT, java.util.Arrays.asList(
					(Object) Doc.plainParagraph("H2: the cells below carry no w:vAlign", SANS, 16),
					pictureCellTable(d, "hdr2", null),
					Doc.plainParagraph("", SANS, 16)));

			// the line-spacing cases the picture-only line box has to reckon with
			d.para("K. A picture-only paragraph at w:spacing line=276 lineRule=auto "
					+ "follows.").after(0).add();
			d.para().noLabel().line(276, STLineSpacingRule.AUTO).run(d.inlineImage(200, 80, 1200))
					.after(0).add();
			d.para("K after.").after(240).add();

			d.para("L. A picture-only paragraph at an exact 12pt line follows; the picture "
					+ "is 24pt tall.").after(0).add();
			d.para().noLabel().line(240, STLineSpacingRule.EXACT).run(d.inlineImage(200, 80, 1200))
					.after(0).add();
			d.para("L after.").after(240).add();

			d.para("M. A picture-only paragraph whose picture sits inside an italic run, at "
					+ "line=276 lineRule=auto, follows.").after(0).add();
			d.para().noLabel().line(276, STLineSpacingRule.AUTO)
					.run(italic(d.inlineImage(200, 80, 1200))).after(0).add();
			d.para("M after. " + prose(2, 4)).after(240).add();
			return d.pkg();
		}));

		/*
		 * G17: a corpus cover's background picture is page-anchored at wp:positionH
		 * posOffset -5353685 (-421.55pt) and positionV -1244600 (-98.0pt) with a
		 * 656.3 x 797.0pt extent, and Word draws it at (-34.8, 0.0) - clamped to the page -
		 * where we draw it at the raw offsets, which puts the inline logo below it 161.8pt
		 * above our body top and costs the document ten of Word's forty-five pages.  Where
		 * Word starts clamping, and whether the clamp depends on the picture's size, has
		 * never been measured.  One case a page: a small negative offset, a larger one, the
		 * corpus's own, and the corpus's own offsets under a small picture.
		 */
		PROBES.add(new Probe("picture-anchor-negative",
				"page-anchored pictures at negative wp:positionH/V offsets - -9pt, -50pt, "
				+ "the corpus cover's -421.55/-98.0pt, and a small picture at the same - one "
				+ "per page, with body text to measure the clamp against", () -> {
			Doc d = Doc.create(15);
			long[][] offsets = {
					{ -114300L, -114300L },     // -9pt and -9pt: just off the page
					{ -635000L, -635000L },     // -50pt and -50pt
					{ -5353685L, -1244600L } }; // -421.55pt and -98.0pt: the corpus cover's
			String[] what = { "-9pt / -9pt", "-50pt / -50pt", "-421.55pt / -98.0pt" };
			for (int i = 0; i < offsets.length; i++) {
				if (i > 0) d.pageBreak();
				d.para("Page " + (i + 1) + ": a page-anchored 656.3 x 797.0pt picture "
						+ "behind the text at wp:positionH/V " + what[i] + " from the page's "
						+ "top left corner. " + prose(2, i)).after(240).add();
				d.para().noLabel()
						.run(d.pageAnchoredImage(240, 291, 8335010L, 10121900L, offsets[i][0], offsets[i][1]))
						.after(240).add();
				d.para("Body text on the same page, so the clamp can be read off these "
						+ "lines. " + prose(3, i + 1)).add();
			}
			d.pageBreak();
			d.para("Page 4: a 200 x 150pt page-anchored picture at the same -421.55pt / "
					+ "-98.0pt, which is more than its own width off the page. "
					+ prose(2, 3)).after(240).add();
			d.para().noLabel()
					.run(d.pageAnchoredImage(200, 150, 2540000L, 1905000L, -5353685L, -1244600L))
					.after(240).add();
			d.para("Body text on the same page. " + prose(3, 4)).add();
			return d.pkg();
		}));

		// --------------------------------- b2 batch 23 triage: PROBES ONLY.  Each of
		//                                   these five settles a rule the corpora suggest
		//                                   but no Word golden has yet answered; nothing
		//                                   is coded until the goldens are back.

		/*
		 * H12: does Word charge a cell's border against the text measure, and when?  The
		 * question has two contradictory answers on the record.
		 *
		 *   - §6.3's content-sized cell rule gives the border allowance back for a
		 *     content-sized column only, and WordLayoutFixups.cellLineWidth's javadoc
		 *     records the OPPOSITE measurement for a grid-sized one, from the table-fixed
		 *     and table-cellspacing goldens: a 150pt column with 5.4pt margins broke a
		 *     139.2pt line, which fits in 150 - 10.8 but not in that less the 0.5pt
		 *     border.  Those probes' lines had NO slack at all, so they say only that
		 *     something was charged - not half a border against a whole one.
		 *   - H12 says the opposite for grid-sized tables and now has seven corpus
		 *     measurements: a corpus document (our 122.27 against a 122.65pt string Word
		 *     keeps on one line in 122.5), a corpus document (72 - 10.83 - 0.5 =
		 *     60.67 against Word's 61.17 and a 61.3pt string), a corpus document (13
		 *     pages became 14), and four more in ledger4, of which a corpus document
		 *     is a direct measurement of the offset rather than an inference: our text
		 *     starts at x=38.7 where Word's starts at 37.9, the 0.8pt being the cell
		 *     margin plus half the left border.
		 *
		 * So the golden has to separate four hypotheses, and the corpus cannot.  A cell
		 * has two borders in the inline direction, and what FOP charges is a share of
		 * EACH of them - half of a collapsed one, all of a separate one - so its charge is
		 * one whole border width for a collapsed cell and two for a separate one.
		 *
		 * The trick which makes that readable is that all three rows of a table hold the
		 * SAME line - so one column serves them all - and differ only in the cell's own
		 * w:tcMar on the END side.  The column is the line's measured advance rounded up
		 * to a whole twip, plus two twips of slack, plus the two 108-twip margins, so with
		 * nothing charged the line fits with a tenth of a point to spare.  Row 2 gives its
		 * right margin back half a border width, row 3 a whole one; the left margin never
		 * moves, so all three lines start at the same x and only the measure changes.  The
		 * line ends in a word which has nowhere to go but a second line, so each row is
		 * one line or two, and the rung the wrapping stops at is the answer:
		 *
		 *      charged against the measure                        R1  R2  R3
		 *      nothing (H12, and §6.3 for a content-sized column)  1   1   1
		 *      half a border width                                 2   1   1
		 *      one border width (FOP's collapsed cell)             2   2   1
		 *      two border widths (FOP's separate cell)             2   2   2
		 *
		 * Half a border is 5, 15 and 30 twips for the three widths, against two twips of
		 * slack; only the 0.5pt table's row 2 is decided by as little as 0.15pt, so the
		 * 1.5 and 3pt tables are the ones to read where the two disagree.  T0 is the
		 * borderless control, which must read 1/1/1 whatever the answer.
		 *
		 * 17.1.0 renders 1/1/1 for T0, 2/1/1 for T1 (the 0.5pt tight rung), 2/2/1 for T2
		 * and T3, and 2/2/2 for the three w:tblCellSpacing tables - FOP's charge exactly.
		 *
		 * Row 1 of every table also answers the second half of H12 on its own, without any
		 * line counting: a corpus document's measurement is where the text STARTS, so
		 * the golden's first glyph x against the table's left border says whether Word put
		 * it on the grid edge plus the cell margin, or on that plus half the border (§6.2).
		 *
		 * The grid-sized tables are w:tblLayout fixed with w:tcW in dxa, so the column is
		 * the grid and nothing refits (whether Word refits a declared width at all is
		 * table-grid-pct's question).  The three content-autofit tables (w:tblW auto,
		 * w:tcW auto) should hold their line whatever the answer, since the content is
		 * what sized the column, so they are read the other way: the right border's x says
		 * whether Word's content-preferred width is the line's advance plus the two
		 * margins, or that plus the border.  17.1.0 wraps them all the same: cellLineWidth
		 * does give the allowance back (padding-right 4.91pt against 5.4), but our own
		 * autofit pass sizes the column 0.4pt narrower than the line's measured advance,
		 * which is a second thing for the golden to settle.
		 *
		 * One table a page, and the line is measured from the installed Liberation Serif,
		 * so the column is exact rather than estimated.
		 */
		PROBES.add(new Probe("table-cell-measure",
				"one line whose advance exactly fills the column less the cell margins, in "
				+ "cells whose own w:tcMar gives back nothing, half a border and a whole "
				+ "border on the end side: grid-sized (w:tblLayout fixed, w:tcW dxa) with "
				+ "collapsed and with separate (w:tblCellSpacing 72) borders of 0.5, 1.5 "
				+ "and 3pt, a borderless control, and content-autofit twins whose width is "
				+ "read off the border positions", () -> {
			Doc d = Doc.create(15);
			// the line every cell holds; the "TnRm " tag is the same width in every cell
			// (Liberation Serif's digits are tabular), so one column serves all three rows
			final String sentence = "The quick brown fox jumps over the lazy dog while the farmer watches.";
			final int[] eighths = { 4, 12, 24 };          // 0.5, 1.5 and 3pt borders
			int table = 0;

			d.para("Every table below holds the same line, whose advance in 12pt Liberation "
					+ "Serif was measured from the font itself. The column is that advance "
					+ "rounded up to a whole twip, plus two twips, plus the two 108-twip "
					+ "cell margins, so the line fits with a tenth of a point to spare when "
					+ "nothing else is charged against the measure. Row 2 of each table "
					+ "gives its right cell margin back half the border width, row 3 the "
					+ "whole border width; the left margin never moves, so all three lines "
					+ "start at the same place. A row which wraps has had that much charged "
					+ "against it. " + prose(1)).after(240).add();

			// T0: the borderless control - 1/1/1 under every hypothesis
			d.para("Table T" + table + ": w:tblBorders none, so nothing can be charged. All "
					+ "three rows must hold their line on one line. "
					+ prose(1, 1)).after(240).add();
			d.add(measureTable("T" + table, sentence, 0, 0, false, true));
			d.para("after T" + table + ". " + prose(1, 2)).before(240).add();
			table++;

			// T1..T3: grid-sized, collapsed borders
			for (int i = 0; i < eighths.length; i++) {
				d.pageBreak();
				d.para("Table T" + table + ": grid-sized (w:tblLayout fixed, w:tcW in dxa), "
						+ "collapsed borders of " + (eighths[i] / 8.0) + "pt. Row 1 wraps if "
						+ "anything at all is charged against the measure, row 2 if more "
						+ "than half a border width is, row 3 if more than a whole one is. "
						+ prose(1, i)).after(240).add();
				d.add(measureTable("T" + table, sentence, eighths[i], 0, false, false));
				d.para("after T" + table + ". " + prose(1, i + 1)).before(240).add();
				table++;
			}

			// T4..T6: grid-sized, separate borders (w:tblCellSpacing), where FOP charges
			// the whole border rather than half of it
			for (int i = 0; i < eighths.length; i++) {
				d.pageBreak();
				d.para("Table T" + table + ": the same, with w:tblCellSpacing 72 - Word's "
						+ "separate-borders model - and borders of " + (eighths[i] / 8.0)
						+ "pt. " + prose(1, i + 2)).after(240).add();
				d.add(measureTable("T" + table, sentence, eighths[i], 72, false, false));
				d.para("after T" + table + ". " + prose(1, i + 3)).before(240).add();
				table++;
			}

			// T7..T9: content-autofit twins, read off the border positions
			for (int i = 0; i < eighths.length; i++) {
				d.pageBreak();
				d.para("Table T" + table + ": content-autofit (w:tblW auto, every w:tcW "
						+ "auto), borders of " + (eighths[i] / 8.0) + "pt. An autofit column "
						+ "is sized to the content which set it, so Word should hold this "
						+ "line whole however much it charges; what the golden gives is the "
						+ "right border's position, and so whether Word's content-preferred "
						+ "width is the line's advance plus the two margins, or that plus "
						+ "the border. "
						+ prose(1, i + 4)).after(240).add();
				d.add(measureTable("T" + table, sentence, eighths[i], 0, true, false));
				d.para("after T" + table + ". " + prose(1, i + 5)).before(240).add();
				table++;
			}
			return d.pkg();
		}));

		/*
		 * J27 against E37, which the ledger calls exact opposites, so neither may be
		 * implemented before Word answers both.
		 *
		 *   - J27 (a corpus document, a corpus document): Word paints NO label
		 *     for a w:pStyle-linked level at ilvl 0 and still uses the level's indent for
		 *     the text.  Verified glyph by glyph: Word's heading is "Entity ed EOS
		 *     Solutions" at x=85.0 with no number, and 85.0 - 56.7 = 28.35pt = the level's
		 *     w:ind w:left 567, while the paragraph's style says w:ind w:left 0
		 *     w:firstLine 0, which we honoured, painting a "1" at 56.7.  Its ilvl 1
		 *     matches us exactly, so whatever it is is specific to the linked ilvl 0.
		 *     9838 has two w:num sharing one abstractNum, which the ledger names as a
		 *     likely trigger.
		 *   - E37 (a corpus document): the paragraph style's own w:ind - which arrives
		 *     through w:basedOn, on the same style that holds the w:numPr - beats the
		 *     level's, and Word does paint the label.  Word's bullet is at 46.8 with its
		 *     continuation at 58.1 (the style's 227tw hanging indent); we render 64.8 /
		 *     82.8 from the level's 720/360.
		 *
		 * Three numbering definitions, each with its own recognisable level-0 indent so
		 * the golden says which one Word used:
		 *
		 *   numId 20 - level 0 carries w:pStyle "NumLinked" and w:ind left 567 hanging 567
		 *   numId 21 - no w:pStyle link on any level; w:ind left 720 hanging 360
		 *   numId 22 - level 0 carries w:pStyle "NumUnused", a style which exists but
		 *              which no paragraph uses; w:ind left 1080 hanging 540
		 *   numId 23 - a second w:num over abstractNum 20, so that abstract definition is
		 *              shared by two instances (9838's shape)
		 *
		 * Each case is one paragraph long enough to wrap, so the golden shows both where
		 * the label sits and where the second line starts, and each is introduced by a
		 * prose paragraph naming it.
		 */
		PROBES.add(new Probe("numbering-label-ilvl0",
				"a numbering definition linked to a paragraph style against direct w:numPr: "
				+ "with and without the paragraph's own w:ind, with and without w:ilvl, a "
				+ "style whose w:ind and w:numPr arrive through w:basedOn, a style stating "
				+ "w:ind left 0 firstLine 0, a level whose w:pStyle names a style the "
				+ "paragraph does not use, two w:num sharing one abstractNum, and w:numId 0 "
				+ "on a numbered style", () -> {
			Doc d = Doc.create(15);
			d.numberingXml(
					"<w:abstractNum w:abstractNumId=\"20\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ Doc.decimalLevel(0, "NumLinked", 567, 567)
					+ Doc.decimalLevel(1, null, 1134, 567)
					+ "</w:abstractNum>"
					+ "<w:abstractNum w:abstractNumId=\"21\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ Doc.decimalLevel(0, null, 720, 360)
					+ Doc.decimalLevel(1, null, 1440, 360)
					+ "</w:abstractNum>"
					+ "<w:abstractNum w:abstractNumId=\"22\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ Doc.decimalLevel(0, "NumUnused", 1080, 540)
					+ Doc.decimalLevel(1, null, 2160, 540)
					+ "</w:abstractNum>"
					+ "<w:num w:numId=\"20\"><w:abstractNumId w:val=\"20\"/></w:num>"
					+ "<w:num w:numId=\"21\"><w:abstractNumId w:val=\"21\"/></w:num>"
					+ "<w:num w:numId=\"22\"><w:abstractNumId w:val=\"22\"/></w:num>"
					+ "<w:num w:numId=\"23\"><w:abstractNumId w:val=\"20\"/></w:num>");

			// the numbered style: w:numPr naming numId 20 and NO w:ilvl, which is the
			// shape Word writes for a style linked to a list
			d.addParagraphStyle("NumLinked", "Normal", ppr -> ppr.setNumPr(numPr(20)));
			// J27's shape: the same, and the style states w:ind left 0 firstLine 0, which
			// is what Word ignored in favour of the level's 567
			d.addParagraphStyle("NumLinkedInd0", "Normal", ppr -> {
				ppr.setNumPr(numPr(20));
				PPrBase.Ind ind = Doc.F.createPPrBaseInd();
				ind.setLeft(BigInteger.ZERO);
				ind.setFirstLine(BigInteger.ZERO);
				ppr.setInd(ind);
			});
			// E37's shape: a base style holding BOTH the w:numPr and a w:ind of 227/227,
			// and the style the paragraph actually uses derived from it
			d.addParagraphStyle("NumBase", "Normal", ppr -> {
				ppr.setNumPr(numPr(21));
				PPrBase.Ind ind = Doc.F.createPPrBaseInd();
				ind.setLeft(BigInteger.valueOf(227));
				ind.setHanging(BigInteger.valueOf(227));
				ppr.setInd(ind);
			});
			d.addParagraphStyle("NumDerived", "NumBase", ppr -> { });
			// a plain style for the direct-numPr cases, and the style numId 22's level 0
			// links to but nothing uses
			d.addParagraphStyle("NumPlain", "Normal", ppr -> { });
			d.addParagraphStyle("NumUnused", "Normal", ppr -> { });

			String tail = " This paragraph is long enough to wrap, so the golden shows where "
					+ "its second line starts as well as where its first one does. " + prose(1);
			int n = 0;

			String[][] cases = {
				{ "w:pStyle NumLinked, whose w:numPr names numId 20 with no w:ilvl; the "
					+ "paragraph carries no w:numPr and no w:ind of its own. The level's "
					+ "indent is 567 left, 567 hanging" },
				{ "the same style, and a direct w:numPr naming numId 20 with w:ilvl 0" },
				{ "the same style, and a direct w:numPr naming numId 20 with NO w:ilvl "
					+ "at all" },
				{ "the same style, and the paragraph's own w:ind left 1440 hanging 360, "
					+ "against the level's 567/567" },
				{ "the same style, a direct w:numPr with w:ilvl 0, and the paragraph's "
					+ "own w:ind left 1440 hanging 360" },
				{ "w:pStyle NumLinkedInd0: the same numbered style, but the STYLE states "
					+ "w:ind left 0 firstLine 0. This is J27's shape, where Word ignored "
					+ "the style's indent, used the level's 567, and painted no label" },
				{ "w:pStyle NumDerived, based on NumBase, which holds both the w:numPr "
					+ "(numId 21, whose level 0 is not style-linked) and w:ind left 227 "
					+ "hanging 227. This is E37's shape, where Word used the style's "
					+ "indent rather than the level's 720/360" },
				{ "an unnumbered style and a direct w:numPr naming numId 21, whose levels "
					+ "carry no w:pStyle link at all, with w:ilvl 0" },
				{ "the same, with the paragraph's own w:ind left 1440 hanging 360" },
				{ "an unnumbered style and a direct w:numPr naming numId 22, whose level 0 "
					+ "carries a w:pStyle link to NumUnused - a style which exists but "
					+ "which this paragraph does not use" },
				{ "w:pStyle NumLinked and a direct w:numPr naming numId 23, a second w:num "
					+ "over the same abstractNum 20 the style's own numId 20 names, so one "
					+ "abstract definition is shared by two instances" },
				{ "w:pStyle NumLinked with a direct w:numPr naming w:numId 0 and no "
					+ "w:ilvl, which is how Word switches a numbered style's numbering off" },
				{ "the same, with w:ilvl 0 written alongside the w:numId 0" },
				{ "w:pStyle NumLinked, no direct w:numPr, and the paragraph's own w:ind "
					+ "left 0 hanging 0 - an indent which overrides the level's without "
					+ "moving anything" },
			};
			for (String[] c : cases) {
				n++;
				d.para("N" + n + ": " + c[0] + ". " + prose(1, n))
						.before(n == 1 ? 0 : 240).after(120).add();
				Doc.Para p = d.para("N" + n + "." + tail).inheritSpacing();
				switch (n) {
					case 1: p.style("NumLinked"); break;
					case 2: p.style("NumLinked").numPr(20, 0); break;
					case 3: p.style("NumLinked").numPr(20, null); break;
					case 4: p.style("NumLinked").indent(1440, 0, 360); break;
					case 5: p.style("NumLinked").numPr(20, 0).indent(1440, 0, 360); break;
					case 6: p.style("NumLinkedInd0"); break;
					case 7: p.style("NumDerived"); break;
					case 8: p.style("NumPlain").numPr(21, 0); break;
					case 9: p.style("NumPlain").numPr(21, 0).indent(1440, 0, 360); break;
					case 10: p.style("NumPlain").numPr(22, 0); break;
					case 11: p.style("NumLinked").numPr(23, 0); break;
					case 12: p.style("NumLinked").numPr(0, null); break;
					case 13: p.style("NumLinked").numPr(0, 0); break;
					default: p.style("NumLinked").indent(0, 0, 0); break;
				}
				p.add();
			}
			d.para("after. " + prose(1, 3)).before(240).add();
			return d.pkg();
		}));

		/*
		 * CR-014's probe set (docs/developer/change-requests/CR-014-list-numbering-model.md,
		 * "Are the comments accurate?").  Each probe prints labels only - one short
		 * paragraph per item - so the golden's text layer is the answer.  numId 1 is
		 * left alone (Doc.listItem's default); these use 10 and up.
		 */
		PROBES.add(new Probe("numbering-shared-abstract",
				"two w:num over one w:abstractNum with no overrides, three items each, "
				+ "interleaved A A B B A B: one sequence 1-6 in document order if the counter "
				+ "belongs to the abstract list, 1 2 1 2 3 3 if to the w:num", () -> {
			Doc d = Doc.create(15);
			d.numberingXml(abstractDecimal(10) + num(10, 10) + num(11, 10));
			d.para("w:num 10 and w:num 11 both name abstractNum 10; the items below are "
					+ "in the order A A B B A B.").after(120).add();
			int[] order = { 10, 10, 11, 11, 10, 11 };
			for (int numId : order) {
				d.para("item of list " + (numId == 10 ? "A (w:num 10)" : "B (w:num 11)")).numPr(numId, 0).add();
			}
			return d.pkg();
		}));

		PROBES.add(new Probe("numbering-numstylelink-separate",
				"a numbering style ProbeList (w:num 20 over abstractNum 20, whose w:styleLink "
				+ "names it) and abstractNum 21 carrying w:numStyleLink to it, used through w:num "
				+ "21: three items of 20, three of 21, three of 20 again - Y 1 2 3, X 1 2 3, Y 4 5 6 "
				+ "if the numStyleLink list counts separately, 1-9 if it is the same list", () -> {
			Doc d = Doc.create(15);
			d.addNumberingStyle("ProbeList", 20);
			d.numberingXml(
					"<w:abstractNum w:abstractNumId=\"20\"><w:multiLevelType w:val=\"multilevel\"/>"
					+ "<w:styleLink w:val=\"ProbeList\"/>"
					+ Doc.decimalLevel(0, null, 720, 360) + Doc.decimalLevel(1, null, 1440, 360)
					+ "</w:abstractNum>"
					+ "<w:abstractNum w:abstractNumId=\"21\"><w:multiLevelType w:val=\"multilevel\"/>"
					+ "<w:numStyleLink w:val=\"ProbeList\"/>"
					+ "</w:abstractNum>"
					+ num(20, 20) + num(21, 21));
			d.para("abstractNum 20 is the numbering style's own list (w:styleLink ProbeList, "
					+ "w:num 20); abstractNum 21 has only a w:numStyleLink to that style "
					+ "(w:num 21).").after(120).add();
			for (int k = 1; k <= 3; k++) d.para("item, w:num 20 (Y)").numPr(20, 0).add();
			for (int k = 1; k <= 3; k++) d.para("item, w:num 21 (X, via numStyleLink)").numPr(21, 0).add();
			for (int k = 1; k <= 3; k++) d.para("item, w:num 20 again (Y)").numPr(20, 0).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("numbering-override-rpr",
				"label run properties from a w:lvlOverride: abstract level 0 with no w:rPr and "
				+ "w:num 30's override level carrying w:b + w:sz 36 (bold 18pt label, 12pt text?); "
				+ "then abstract level 0 with w:rPr w:i and the same override (bold italic = merged, "
				+ "bold only = the override replaces the abstract's rPr)", () -> {
			Doc d = Doc.create(15);
			String overrideLvl = "<w:lvlOverride w:ilvl=\"0\"><w:lvl w:ilvl=\"0\">"
					+ "<w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%1.\"/>"
					+ "<w:lvlJc w:val=\"left\"/><w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr>"
					+ "<w:rPr><w:b/><w:sz w:val=\"36\"/></w:rPr>"
					+ "</w:lvl></w:lvlOverride>";
			d.numberingXml(
					abstractDecimal(30)
					+ "<w:abstractNum w:abstractNumId=\"31\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>"
					+ "<w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>"
					+ "<w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr><w:rPr><w:i/></w:rPr></w:lvl>"
					+ "</w:abstractNum>"
					+ "<w:num w:numId=\"30\"><w:abstractNumId w:val=\"30\"/>" + overrideLvl + "</w:num>"
					+ "<w:num w:numId=\"31\"><w:abstractNumId w:val=\"31\"/>" + overrideLvl + "</w:num>");
			d.para("w:num 30: abstract level 0 has no w:rPr; the override level says w:b and "
					+ "w:sz 36. The text runs are regular 12pt.").after(120).add();
			for (int k = 1; k <= 3; k++) d.para("regular text, w:num 30").numPr(30, 0).add();
			d.para("w:num 31: abstract level 0 says w:i; the override level says w:b and "
					+ "w:sz 36.").before(240).after(120).add();
			for (int k = 1; k <= 3; k++) d.para("regular text, w:num 31").numPr(31, 0).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("numbering-level-pstyle-in-ppr",
				"a level whose w:pPr carries a w:pStyle (LevelStyle) but which has no w:lvl/w:pStyle: "
				+ "used by direct w:numPr from a paragraph of OtherStyle (is the label painted, and "
				+ "indented by the level?), then by a LevelStyle paragraph with no w:numPr (does the "
				+ "w:pPr/w:pStyle link the style to the list?); a plain level as the control", () -> {
			Doc d = Doc.create(15);
			d.addParagraphStyle("LevelStyle", "Normal", ppr -> { });
			d.addParagraphStyle("OtherStyle", "Normal", ppr -> { });
			d.numberingXml(
					"<w:abstractNum w:abstractNumId=\"40\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>"
					+ "<w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>"
					+ "<w:pPr><w:pStyle w:val=\"LevelStyle\"/><w:ind w:left=\"1080\" w:hanging=\"540\"/></w:pPr></w:lvl>"
					+ "</w:abstractNum>"
					+ abstractDecimal(41)
					+ num(40, 40) + num(41, 41));
			d.para("abstractNum 40's level 0 has w:pPr/w:pStyle LevelStyle and w:ind left 1080 "
					+ "hanging 540, and no w:lvl/w:pStyle; abstractNum 41 is plain (720/360).")
					.style("OtherStyle").after(120).add();
			for (int k = 1; k <= 3; k++) d.para("OtherStyle, direct w:numPr 40").style("OtherStyle").numPr(40, 0).add();
			d.para("LevelStyle paragraphs with no w:numPr:").style("OtherStyle").before(240).after(120).add();
			for (int k = 1; k <= 3; k++) d.para("LevelStyle, no w:numPr").style("LevelStyle").add();
			d.para("Control: OtherStyle with direct w:numPr 41.").style("OtherStyle").before(240).after(120).add();
			for (int k = 1; k <= 3; k++) d.para("OtherStyle, direct w:numPr 41").style("OtherStyle").numPr(41, 0).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("numbering-level-vs-style-indent",
				"a style-linked level stating only w:hanging 360 with the style stating only w:left "
				+ "1440 (IndStyleA), then the reverse (IndStyleB): the label and text x positions say "
				+ "whether the level's w:ind and the style's combine attribute by attribute or one "
				+ "replaces the other as a block; the second item of each wraps", () -> {
			Doc d = Doc.create(15);
			d.addParagraphStyle("IndStyleA", "Normal", ppr -> {
				ppr.setNumPr(numPr(50));
				PPrBase.Ind ind = Doc.F.createPPrBaseInd();
				ind.setLeft(BigInteger.valueOf(1440));
				ppr.setInd(ind);
			});
			d.addParagraphStyle("IndStyleB", "Normal", ppr -> {
				ppr.setNumPr(numPr(51));
				PPrBase.Ind ind = Doc.F.createPPrBaseInd();
				ind.setHanging(BigInteger.valueOf(360));
				ppr.setInd(ind);
			});
			d.addParagraphStyle("IndPlain", "Normal", ppr -> { });
			d.numberingXml(
					"<w:abstractNum w:abstractNumId=\"50\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>"
					+ "<w:pStyle w:val=\"IndStyleA\"/><w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>"
					+ "<w:pPr><w:ind w:hanging=\"360\"/></w:pPr></w:lvl>"
					+ "</w:abstractNum>"
					+ "<w:abstractNum w:abstractNumId=\"51\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>"
					+ "<w:pStyle w:val=\"IndStyleB\"/><w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>"
					+ "<w:pPr><w:ind w:left=\"1440\"/></w:pPr></w:lvl>"
					+ "</w:abstractNum>"
					+ num(50, 50) + num(51, 51));
			String wrap = "This item is long enough to wrap onto a second line, so the golden "
					+ "shows the continuation indent as well as the label and first-line positions.";
			d.para("IndStyleA: the level says w:hanging 360 only; the style says w:left 1440 only.")
					.style("IndPlain").after(120).add();
			d.para("IndStyleA item").style("IndStyleA").add();
			d.para(wrap).style("IndStyleA").add();
			d.para("IndStyleA item").style("IndStyleA").add();
			d.para("IndStyleB: the level says w:left 1440 only; the style says w:hanging 360 only.")
					.style("IndPlain").before(240).after(120).add();
			d.para("IndStyleB item").style("IndStyleB").add();
			d.para(wrap).style("IndStyleB").add();
			d.para("IndStyleB item").style("IndStyleB").add();
			return d.pkg();
		}));

		PROBES.add(new Probe("numbering-default-style-numbered",
				"the w:default=1 paragraph style carries w:numPr (w:num 60): three paragraphs "
				+ "with no w:pStyle at all - are they numbered? - and a control paragraph of a "
				+ "style based on nothing", () -> {
			Doc d = Doc.create(15);
			d.numberingXml(abstractDecimal(60) + num(60, 60));
			org.docx4j.wml.Style normal = d.mdp().getStyleDefinitionsPart().getDefaultParagraphStyle();
			if (normal.getPPr() == null) normal.setPPr(Doc.F.createPPr());
			normal.getPPr().setNumPr(numPr(60));
			d.addParagraphStyle("Unlinked", null, ppr -> { });
			d.para("The default paragraph style (" + normal.getStyleId() + ") carries w:numPr 60. "
					+ "The next three paragraphs have no w:pStyle; this one and the last use "
					+ "Unlinked, a style based on nothing.").style("Unlinked").after(120).add();
			for (int k = 1; k <= 3; k++) d.para("no w:pStyle").add();
			d.para("Unlinked control").style("Unlinked").before(240).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("numbering-stories",
				"one w:num (70) numbering three items in each story: body, default header, "
				+ "default footer, two footnotes, an endnote, a comment, a text box, then three "
				+ "more body items - which stories restart at 1, and does the body continue "
				+ "past the notes? (the comment's labels are visible only in Word, not the PDF)", () -> {
			Doc d = Doc.create(15);
			d.numberingXml(abstractDecimal(70) + num(70, 70));
			d.para("Every numbered paragraph in this document uses w:num 70.").after(120).add();
			for (int k = 1; k <= 3; k++) d.para("body item").numPr(70, 0).add();
			d.para("Two footnotes")
					.run(d.footnoteRef(items(d, "footnote one item")))
					.run(Doc.run(" and", SERIF, 24, null))
					.run(d.footnoteRef(items(d, "footnote two item")))
					.before(240).add();
			d.para("An endnote").run(d.endnoteRef(items(d, "endnote item"))).add();
			d.para("A comment").run(d.commentRef(items(d, "comment item"))).add();
			d.para("A text box: ").run(d.textBox(5000, 1800, items(d, "text box item"))).add();
			for (int k = 1; k <= 3; k++) d.para("body item after the notes").numPr(70, 0).add();
			d.addHeader(org.docx4j.wml.HdrFtrRef.DEFAULT, items(d, "header item"));
			d.addFooter(org.docx4j.wml.HdrFtrRef.DEFAULT, items(d, "footer item"));
			d.finishFootnotes();
			d.finishEndnotes();
			d.finishComments();
			return d.pkg();
		}));

		PROBES.add(new Probe("numbering-lvlrestart",
				"w:lvlRestart on level 2 of a three-level list: w:val 0 in list A (never restarts) "
				+ "and w:val 1 in list B; each list walks the levels 0 1 2 2 1 2 0 2, labels "
				+ "%1. / %1.%2. / %1.%2.%3.", () -> {
			Doc d = Doc.create(15);
			d.numberingXml(restartAbstract(80, 0) + restartAbstract(81, 1) + num(80, 80) + num(81, 81));
			int[] walk = { 0, 1, 2, 2, 1, 2, 0, 2 };
			d.para("List A (w:num 80): level 2 carries w:lvlRestart w:val 0.").after(120).add();
			for (int ilvl : walk) d.para("list A, level " + ilvl).numPr(80, ilvl).add();
			d.para("List B (w:num 81): level 2 carries w:lvlRestart w:val 1.").before(240).after(120).add();
			for (int ilvl : walk) d.para("list B, level " + ilvl).numPr(81, ilvl).add();
			return d.pkg();
		}));

		/*
		 * E4/E5's remaining clause: what a w:tblW of type pct means when the w:tblGrid
		 * disagrees with it.  §6.5's exemption for a table stating a width of its own is
		 * unconditional, and a corpus document confirms it for pct - w:tblW 5000
		 * pct with a 492.7pt grid on a 481.9pt column, and Word honours the grid, every
		 * column edge a uniform 1.026x ours, while we clamp.  What no golden says is what
		 * the pct itself then means: whether the grid is scaled to it at all, what happens
		 * when the pct is over 100, whether the pct is taken of the text column or of the
		 * column less a w:tblInd, and whether w:tblLayout fixed changes any of it.
		 *
		 * The last two tables are J28's half: a CONTENT-autofit table (w:tblW auto) whose
		 * grid is 1.47% wider than the text column - the ledger measured Word refitting
		 * such a grid by x0.9855, i.e. exactly down to the column, and §6.5's cut is at
		 * 1.25, so we leave it alone.  P10 has auto cells and P11 the grid-sized twin,
		 * which says whether the refit depends on the cells declaring widths.
		 *
		 * The text column is 9026 twips (A4 less two 1-inch margins). Each cell holds
		 * prose, so the width Word gave the column can be read off where the lines wrap as
		 * well as off the borders. One table a page.
		 */
		/*
		 * L1: which of the two does Word lay a pct table out on when they disagree - the
		 * w:tblGrid, or the cells' own w:tcW in pct?  The dxa answer is settled and is the
		 * grid (a stale row 1 loses to it, measured on a landscape report), and the shipped
		 * gate reads it that way for every unit.  Two corpus measurements say the pct
		 * answer is the opposite: one table's w:tblW is 5000 pct with a grid which, scaled
		 * to the 523.3pt the pct asks for, gives column 8 = 80.7pt, while the widest row's
		 * w:tcW of 1296/5000 gives 135.6pt - and Word measures about 133.4 (gridline
		 * 426.5 to 559.9).  A second has every cell at exactly the grid's proportions and
		 * so cannot tell them apart.  This probe puts the two readings 187pt apart.
		 */
		PROBES.add(new Probe("table-cell-pct",
				"a w:tblW 5000 pct table whose cells declare w:tcW in pct which disagree "
				+ "with the w:tblGrid: the grid and the cells as the layout are 187pt "
				+ "apart on P1, agree on P2, and P3 asks what Word does when the cells' "
				+ "percentages sum to less than 5000; P4 is the corpus shape, a grid which "
				+ "sums to the table width against cells which do not share its "
				+ "proportions, and P5 asks whether dxa cells behave as pct ones do", () -> {
			Doc d = Doc.create(15);
			d.para("The text column of this page is 9026 twips, so a w:tblW of 5000 pct is "
					+ "9026 twips = 451.3pt wide. Each table below states its column "
					+ "proportions twice - once in the w:tblGrid and once in the cells' "
					+ "w:tcW - and the two disagree except on P2. Read column 1's right "
					+ "gridline to see which Word used. " + prose(1)).after(240).add();

			// gridA, gridB, pctA, pctB (pct = fiftieths of a per cent of the table width),
			// or dxaA/dxaB where pct is 0
			int[][] cases = {
				{ 6000, 3026, 1250, 3750 },  // grid 66.5/33.5 against cells 25/75: 187pt apart
				{ 2256, 6770, 1250, 3750 },  // control: the two agree, column 1 = 112.8pt
				{ 4513, 4513, 1000, 2000 },  // the cells sum to 60 per cent: is the rest used?
				{ 4513, 4513, 1296, 3704 },  // the corpus shape: grid 50/50, cells 25.92/74.08
			};
			for (int i = 0; i < cases.length; i++) {
				int[] c = cases[i];
				if (i > 0) d.pageBreak();
				double gridPc = 100.0 * c[0] / (c[0] + c[1]);
				d.para("Table P" + (i + 1) + ": w:tblGrid " + c[0] + "+" + c[1]
						+ " (column 1 is " + Math.round(gridPc * 10) / 10.0 + " per cent = "
						+ Math.round(451.3 * gridPc) / 100.0 + "pt of the table), cells "
						+ "w:tcW " + c[2] + " and " + c[3] + " pct (column 1 is "
						+ (c[2] / 50.0) + " per cent = " + Math.round(451.3 * c[2] / 50.0) / 100.0
						+ "pt). " + prose(1, i)).after(240).add();
				Doc.Table t = new Doc.Table(c[0], c[1]);
				t.tableWidth(5000, "pct");
				t.rowPct(SERIF, 24, new int[] { c[2], c[3] },
						"P" + (i + 1) + " left. " + prose(1, i + 1),
						"P" + (i + 1) + " right. " + prose(1, i + 2));
				d.add(t.build());
				d.para("after P" + (i + 1) + ". " + prose(1, i + 3)).before(240).add();
			}

			// P5: the same disagreement stated in dxa rather than pct
			d.pageBreak();
			d.para("Table P5: w:tblGrid 6000+3026 against cells w:tcW 2256 and 6770 dxa - "
					+ "the same disagreement as P1, stated in dxa. If Word answers P1 with "
					+ "the cells and P5 with the grid, the unit is what decides it. "
					+ prose(1, 5)).after(240).add();
			Doc.Table t5 = new Doc.Table(6000, 3026);
			t5.tableWidth(5000, "pct");
			t5.rowDxa(SERIF, 24, new int[] { 2256, 6770 },
					"P5 left. " + prose(1, 6), "P5 right. " + prose(1, 7));
			d.add(t5.build());
			d.para("after P5. " + prose(1, 8)).before(240).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("table-grid-pct",
				"w:tblW of type pct at 100, 120 and 80 per cent against a w:tblGrid which "
				+ "does not sum to it (2.2 per cent over, 33 per cent over, 33 per cent "
				+ "under), autofit and w:tblLayout fixed, with and without w:tblInd; and a "
				+ "content-autofit grid 1.47 per cent wider than the text column, with auto "
				+ "and with dxa cells", () -> {
			Doc d = Doc.create(15);
			d.para("The text column of this page is 9026 twips. Each table below declares a "
					+ "w:tblW in pct (fiftieths of a per cent) and a w:tblGrid which may or "
					+ "may not sum to the width that pct asks for; the prose in the cells "
					+ "shows where the columns actually ended up. " + prose(1)).after(240).add();

			// pct, gridA, gridB, fixed?, tblInd (-1 = none)
			int[][] cases = {
				{ 5000, 4513, 4513, 0, -1 },   // the grid sums to the pct width exactly
				{ 5000, 4614, 4614, 0, -1 },   // 2.2% over, 2422's shape
				{ 5000, 6000, 6000, 0, -1 },   // 33% over
				{ 5000, 3000, 3000, 0, -1 },   // 33% under
				{ 6000, 4513, 4513, 0, -1 },   // 120%: wider than the text column
				{ 4000, 4513, 4513, 0, -1 },   // 80%
				{ 5000, 4614, 4614, 1, -1 },   // 2.2% over, fixed layout
				{ 5000, 3000, 3000, 1, -1 },   // 33% under, fixed layout
				{ 5000, 4513, 4513, 0, 720 },  // is the pct of the column, or of the rest?
				{ 5000, 4513, 4513, 1, 720 },
			};
			for (int i = 0; i < cases.length; i++) {
				int[] c = cases[i];
				if (i > 0) d.pageBreak();
				d.para("Table P" + (i + 1) + ": w:tblW " + c[0] + " pct (" + (c[0] / 50.0)
						+ " per cent of 9026 = " + Math.round(9026.0 * c[0] / 5000.0)
						+ " twips), w:tblGrid " + c[1] + "+" + c[2] + " = " + (c[1] + c[2])
						+ ", " + (c[3] == 1 ? "w:tblLayout fixed" : "autofit")
						+ (c[4] < 0 ? ", no w:tblInd" : ", w:tblInd " + c[4]) + ". "
						+ prose(1, i)).after(240).add();
				Doc.Table t = new Doc.Table(c[1], c[2]);
				t.tableWidth(c[0], "pct");
				if (c[3] == 1) t.fixedLayout();
				if (c[4] >= 0) t.indent(c[4]);
				t.row(SERIF, 24, true,
						"P" + (i + 1) + " left. " + prose(1, i + 1),
						"P" + (i + 1) + " right. " + prose(1, i + 2));
				d.add(t.build());
				d.para("after P" + (i + 1) + ". " + prose(1, i + 3)).before(240).add();
			}

			// P11, P12: J28 - a content-autofit grid 1.47% wider than the text column
			for (int auto = 1; auto >= 0; auto--) {
				int p = auto == 1 ? 11 : 12;
				d.pageBreak();
				d.para("Table P" + p + ": w:tblW auto with a w:tblGrid of 4580+4579 = 9159, "
						+ "which is 1.47 per cent wider than the 9026-twip text column, and "
						+ "every w:tcW " + (auto == 1 ? "auto" : "in dxa")
						+ ". The ledger measured Word refitting such a grid by 0.9855, "
						+ "which is exactly down to the column. " + prose(1, auto)).after(240).add();
				Doc.Table t = new Doc.Table(4580, 4579).autoWidth();
				t.row(SERIF, 24, auto == 1,
						"P" + p + " left. " + prose(1, auto + 1),
						"P" + p + " right. " + prose(1, auto + 2));
				d.add(t.build());
				d.para("after P" + p + ". " + prose(1, auto + 3)).before(240).add();
			}
			return d.pkg();
		}));

		/*
		 * The open question af866704e left, which §3.3's s33cell states as the narrower
		 * reading rather than as a measurement: a single w:br w:type="page" at the head of
		 * a cell's first paragraph opens the table on a new page (measured on a corpus
		 * document, twice), but page-empty's two consecutive breaks in the same position
		 * were ignored.  H10 says the opposite of the first half outright - "Word never
		 * paginates on a hard break inside a table cell" - on a corpus document, whose
		 * page 1 carries both the introducing paragraph and the table, and
		 * a corpus document; two corpus-3 documents point the other way.  17 documents
		 * of the three corpora hold the shape.
		 *
		 * So the position and the count are varied one at a time, each case a table of its
		 * own on a page of its own with prose before it.  Where Word honours a break the
		 * case's table lands one page later than ours does, so the page counts themselves
		 * are part of the measurement.
		 */
		PROBES.add(new Probe("page-break-in-cell",
				"a page break at the head of a cell's first paragraph, two of them, one in a "
				+ "later paragraph of the cell, one in a cell which is not the first, one in "
				+ "the second row, and w:pageBreakBefore on the cell's first paragraph", () -> {
			Doc d = Doc.create(15);
			d.para("Each case below is a two-column table on a page of its own, introduced "
					+ "by a paragraph like this one. Where Word gives the break inside the "
					+ "cell effect, the table opens on the page after its introduction; "
					+ "where Word ignores it, the two share a page. " + prose(1)).after(240).add();

			// B1: one page break at the head of the first cell's first paragraph
			d.para("B1: a single w:br w:type=page at the head of the first paragraph of the "
					+ "first cell. " + prose(1, 1)).after(240).add();
			Doc.Table b1 = new Doc.Table(4500, 4500);
			b1.rowOf(null, null,
					b1.cellOf(4500, null, d.para().noLabel().pageBreakRun().text("B1 cell one").build()),
					b1.cellOf(4500, null, Doc.plainParagraph("B1 cell two", SERIF, 24)));
			b1.rowOf(null, null,
					b1.cellOf(4500, null, Doc.plainParagraph("B1 row two, cell one", SERIF, 24)),
					b1.cellOf(4500, null, Doc.plainParagraph("B1 row two, cell two", SERIF, 24)));
			d.add(b1.build());
			d.para("after B1. " + prose(1, 2)).before(240).add();

			// B2: two consecutive page breaks in the same position (page-empty's S2b)
			d.pageBreak();
			d.para("B2: two consecutive w:br w:type=page at the head of the first paragraph "
					+ "of the first cell. " + prose(1, 3)).after(240).add();
			Doc.Table b2 = new Doc.Table(4500, 4500);
			b2.rowOf(null, null,
					b2.cellOf(4500, null, d.para().noLabel().pageBreakRun().pageBreakRun()
							.text("B2 cell one").build()),
					b2.cellOf(4500, null, Doc.plainParagraph("B2 cell two", SERIF, 24)));
			b2.rowOf(null, null,
					b2.cellOf(4500, null, Doc.plainParagraph("B2 row two, cell one", SERIF, 24)),
					b2.cellOf(4500, null, Doc.plainParagraph("B2 row two, cell two", SERIF, 24)));
			d.add(b2.build());
			d.para("after B2. " + prose(1, 4)).before(240).add();

			// B3: one break in the cell's SECOND paragraph
			d.pageBreak();
			d.para("B3: one w:br w:type=page at the head of the SECOND paragraph of the "
					+ "first cell. " + prose(1, 5)).after(240).add();
			Doc.Table b3 = new Doc.Table(4500, 4500);
			b3.rowOf(null, null,
					b3.cellOf(4500, null,
							Doc.plainParagraph("B3 cell one, first paragraph", SERIF, 24),
							d.para().noLabel().pageBreakRun()
									.text("B3 cell one, second paragraph").build()),
					b3.cellOf(4500, null, Doc.plainParagraph("B3 cell two", SERIF, 24)));
			d.add(b3.build());
			d.para("after B3. " + prose(1, 6)).before(240).add();

			// B4: one break at the head of the SECOND cell of the first row
			d.pageBreak();
			d.para("B4: one w:br w:type=page at the head of the first paragraph of the "
					+ "SECOND cell of the first row. " + prose(1, 7)).after(240).add();
			Doc.Table b4 = new Doc.Table(4500, 4500);
			b4.rowOf(null, null,
					b4.cellOf(4500, null, Doc.plainParagraph("B4 cell one", SERIF, 24)),
					b4.cellOf(4500, null, d.para().noLabel().pageBreakRun().text("B4 cell two").build()));
			d.add(b4.build());
			d.para("after B4. " + prose(1)).before(240).add();

			// B5: one break at the head of the first cell of the SECOND row
			d.pageBreak();
			d.para("B5: one w:br w:type=page at the head of the first paragraph of the "
					+ "first cell of the SECOND row, so the table has already begun. "
					+ prose(1, 1)).after(240).add();
			Doc.Table b5 = new Doc.Table(4500, 4500);
			b5.rowOf(null, null,
					b5.cellOf(4500, null, Doc.plainParagraph("B5 row one, cell one", SERIF, 24)),
					b5.cellOf(4500, null, Doc.plainParagraph("B5 row one, cell two", SERIF, 24)));
			b5.rowOf(null, null,
					b5.cellOf(4500, null, d.para().noLabel().pageBreakRun()
							.text("B5 row two, cell one").build()),
					b5.cellOf(4500, null, Doc.plainParagraph("B5 row two, cell two", SERIF, 24)));
			d.add(b5.build());
			d.para("after B5. " + prose(1, 2)).before(240).add();

			// B6: w:pageBreakBefore on the first cell's first paragraph
			d.pageBreak();
			d.para("B6: w:pageBreakBefore on the first paragraph of the first cell, rather "
					+ "than a break run. " + prose(1, 3)).after(240).add();
			Doc.Table b6 = new Doc.Table(4500, 4500);
			b6.rowOf(null, null,
					b6.cellOf(4500, null, d.para().noLabel().pageBreakBefore()
							.text("B6 cell one").build()),
					b6.cellOf(4500, null, Doc.plainParagraph("B6 cell two", SERIF, 24)));
			b6.rowOf(null, null,
					b6.cellOf(4500, null, Doc.plainParagraph("B6 row two, cell one", SERIF, 24)),
					b6.cellOf(4500, null, Doc.plainParagraph("B6 row two, cell two", SERIF, 24)));
			d.add(b6.build());
			d.para("after B6. " + prose(1, 4)).before(240).add();
			return d.pkg();
		}));

		/*
		 * Word's row-level keep.  Word has no keep property for a table row; the
		 * paragraphs' w:keepNext does the work, and applied to every paragraph of a row it
		 * keeps the row with the next row (Microsoft's guidance: within a table, keep with
		 * next works only when applied to the whole row), while on the last row it keeps
		 * the table with the paragraph after it.  Measured on a 311-page report whose
		 * 1,183 tables carry it on every paragraph (word-layout-rules.md §3), which says
		 * nothing about a row only SOME of whose paragraphs keep, or which paragraph of
		 * the row Word consults.  CUT 2026-09-10: R1 kept, R2 KEPT, R3 split, R4 split,
		 * R5 and R6 whole - the FIRST paragraph of the FIRST cell decides, alone.
		 * Each case is a three-row table of exact 30pt rows on a
		 * page of its own, after a filler table of exact 20pt rows sized so that one row
		 * of the case's table fits at the foot of the page and two do not: where Word
		 * keeps the row, the whole table opens the next page; where it does not, the
		 * first row stays.  R4 and R5 leave room for the whole table but not for the
		 * paragraph after it.
		 */
		PROBES.add(new Probe("table-row-keepnext",
				"w:keepNext on every paragraph of a row, on the first cell's only, on the last "
				+ "cell's only, on the second paragraph of a cell only; on the last row, with a "
				+ "paragraph after the table; and on a table nested in a one-row table", () -> {
			Doc d = Doc.create(15);
			String[] ids = {"R1", "R2", "R3", "R4", "R5", "R6"};
			// one line each (the generator prefixes a label): the filler below is sized
			// against a one-line introduction; a second line eats the room one row needs
			String[] what = {
				"R1: keepNext on every paragraph of rows one and two.",
				"R2: keepNext on the first cell's paragraph only, rows one and two.",
				"R3: keepNext on the last cell's paragraph only, rows one and two.",
				"R4: cell one has two paragraphs, only the second keeps; cell two keeps.",
				"R5: keepNext on every paragraph of every row; a paragraph follows.",
				"R6: R5's table nested in a one-row table, then a keepNext paragraph."
			};
			// A4, 1in margins: 698pt of body.  The intro paragraph is one line of 12pt
			// serif with 240 twips after (about 26pt); 32 filler rows of 20pt leave about
			// 32pt: one 30pt row fits, two do not.  R5/R6: 29 rows leave about 92pt: three
			// rows (90pt) fit, a line after them does not.  (Rendered through docx4j: R1
			// whole on the next page, R2 and R3 split after row one, R5 and R6 whole.)
			int[] fillerRows = {32, 32, 32, 32, 29, 29};
			for (int c = 0; c < ids.length; c++) {
				if (c > 0) d.pageBreak();
				d.para(what[c]).after(240).add();
				Doc.Table filler = new Doc.Table(4500, 4500);
				for (int i = 0; i < fillerRows[c]; i++) {
					filler.rowOf(400, org.docx4j.wml.STHeightRule.EXACT,
							filler.cell(ids[c] + " filler " + (i + 1), SERIF, 24, 1, 4500),
							filler.cell("value " + (i + 1), SERIF, 24, 1, 4500));
				}
				d.add(filler.build());
				Doc.Table t = new Doc.Table(4500, 4500);
				for (int r = 1; r <= 3; r++) {
					boolean keepRow = r < 3 || c >= 4; // R5/R6: the last row keeps too
					boolean first, last;
					switch (c) {
						case 1: first = keepRow; last = false; break;   // R2
						case 2: first = false; last = keepRow; break;   // R3
						default: first = keepRow; last = keepRow;       // R1, R4, R5, R6
					}
					org.docx4j.wml.P[] firstCell;
					if (c == 3) {
						firstCell = new org.docx4j.wml.P[] {
							d.para().noLabel().text(ids[c] + " row " + r + " first paragraph").build(),
							(first ? d.para().noLabel().keepNext() : d.para().noLabel())
									.text(ids[c] + " row " + r + " second paragraph").build() };
					} else {
						firstCell = new org.docx4j.wml.P[] {
							(first ? d.para().noLabel().keepNext() : d.para().noLabel())
									.text(ids[c] + " row " + r + " cell one").build() };
					}
					t.rowOf(600, org.docx4j.wml.STHeightRule.EXACT,
							t.cellOf(4500, null, firstCell),
							t.cellOf(4500, null, (last ? d.para().noLabel().keepNext() : d.para().noLabel())
									.text(ids[c] + " row " + r + " cell two").build()));
				}
				if (c == 5) {
					Doc.Table outer = new Doc.Table(9000);
					org.docx4j.wml.Tc tc = outer.cellOf(9000, null, d.para().noLabel().keepNext().build());
					tc.getContent().add(0, t.build());
					outer.rowOf(null, null, tc);
					d.add(outer.build());
				} else {
					d.add(t.build());
				}
				d.para(ids[c] + " after the table. " + prose(1, c + 1)).after(0).add();
			}
			return d.pkg();
		}));

		/*
		 * Two seams of the 311-page report the rules doc (§3) cannot yet explain, each
		 * measured there on more than a hundred instances of one structure and nowhere
		 * else, so each needs a control before it can be a rule.
		 *
		 * 1. An EMPTY paragraph (Normal, 10pt after) between a table and a heading with
		 *    10pt before: Word's gap is the row, 11.3, 10 AND 10 - the two spaces add -
		 *    where "larger of" (§3) predicts one 10.  A paragraph of two w:br before the
		 *    same heading shows larger-of.  Cases: text paragraph (after 200) then before
		 *    200; empty paragraph then before 200; a two-w:br paragraph then before 200;
		 *    and the report's own shape, table, empty paragraph, heading.
		 * 2. A trailing tab in a table cell: "Avances y Demora del Proyecto<tab>" in a
		 *    3458-twip column is two lines in Word (the second holds only the tab; §4.4
		 *    says a tab reaching nothing takes no line, measured on a header) and one in
		 *    docx4j; "Configuración de Avance del Proyecto<tab>" wraps its last word.
		 *    Three column widths bracket the text's width, with and without the tab.
		 *    (First cut at 2200-3000 twips wrapped the text at every width; re-cut at
		 *    3200-4000.  First cut of the spacing probe with 200 after against 200 before
		 *    showed larger-of after a text, an empty and a br-only paragraph, but could
		 *    not separate the models for the table cases; re-cut with 400 after.)
		 *    CUT 2 (2026-09-10): spacing - larger-of in every case, 20pt (an empty
		 *    paragraph, and one after a table, keep their space-after; nothing adds).
		 *    Tab - at 3200 and 3600 twips the rows with the tab are two lines, without it
		 *    one; at 4000 (the stop fits) one line: a trailing tab past the cell takes a
		 *    line.  Rules doc §3 (open) and §4.4.
		 */
		PROBES.add(new Probe("spacing-empty-before",
				"20pt space-after of an empty paragraph, of a text paragraph and of a w:br-only "
				+ "paragraph against the 10pt space-before of the paragraph after it, and a "
				+ "table followed by an empty paragraph and a spaced paragraph", () -> {
			Doc d = Doc.create(15);
			d.para("S1: a text paragraph with 400 twips after, then a paragraph with 200 before. "
					+ prose(1)).after(400).add();
			d.para("S1 spaced paragraph. " + prose(1, 1)).before(200).after(400).add();
			d.para("S2: an EMPTY paragraph with 400 after follows this one, then a paragraph "
					+ "with 200 before.").after(400).add();
			d.para().noLabel().after(400).add();
			d.para("S2 spaced paragraph. " + prose(1, 2)).before(200).after(400).add();
			d.para("S3: a paragraph of two w:br and nothing else follows, then a paragraph "
					+ "with 200 before.").after(400).add();
			d.para().noLabel().softReturn().softReturn().after(400).add();
			d.para("S3 spaced paragraph. " + prose(1, 3)).before(200).after(400).add();
			d.para("S4: a one-row table, an empty paragraph with 400 after, then a paragraph "
					+ "with 200 before.").after(400).add();
			Doc.Table t = new Doc.Table(4500, 4500);
			t.rowOf(null, null,
					t.cellOf(4500, null, Doc.plainParagraph("S4 cell one", SERIF, 24)),
					t.cellOf(4500, null, Doc.plainParagraph("S4 cell two", SERIF, 24)));
			d.add(t.build());
			d.para().noLabel().after(400).add();
			d.para("S4 spaced paragraph. " + prose(1, 4)).before(200).after(400).add();
			d.para("S5: control, the table then the spaced paragraph directly.").after(400).add();
			Doc.Table t2 = new Doc.Table(4500, 4500);
			t2.rowOf(null, null,
					t2.cellOf(4500, null, Doc.plainParagraph("S5 cell one", SERIF, 24)),
					t2.cellOf(4500, null, Doc.plainParagraph("S5 cell two", SERIF, 24)));
			d.add(t2.build());
			d.para("S5 spaced paragraph. " + prose(1, 5)).before(200).after(400).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("tab-trailing-cell",
				"a cell whose paragraph ends in a tab, at three column widths bracketing the "
				+ "text's width, and the same text without the tab", () -> {
			Doc d = Doc.create(15);
			d.para("Each table below has one column of the stated width and a second of the "
					+ "rest. Rows one to three end their text in a tab; rows four to six are "
					+ "the same text without it. Where Word gives the trailing tab a line, the "
					+ "row is two lines tall.").after(240).add();
			int[] widths = {3200, 3600, 4000}; // the 12pt text is about 154pt: wraps, fits with no room for a tab, fits with a stop
			for (int w : widths) {
				d.para("T" + w + ": first column " + w + " twips.").after(120).add();
				Doc.Table t = new Doc.Table(w, 9000 - w);
				for (int r = 0; r < 2; r++) {
					boolean tab = r == 0;
					Doc.Para p1 = d.para().noLabel().text("Avance esperado del proyecto");
					if (tab) p1 = p1.tab();
					Doc.Para p2 = d.para().noLabel().text("Demora proyectada del control");
					if (tab) p2 = p2.tab();
					t.rowOf(null, null, t.cellOf(w, null, p1.build()),
							t.cellOf(9000 - w, null, Doc.plainParagraph(tab ? "with tab" : "no tab", SERIF, 24)));
					t.rowOf(null, null, t.cellOf(w, null, p2.build()),
							t.cellOf(9000 - w, null, Doc.plainParagraph(tab ? "with tab" : "no tab", SERIF, 24)));
				}
				d.add(t.build());
				d.para("after T" + w + ".").before(120).after(240).add();
			}
			return d.pkg();
		}));

		/*
		 * The paragraph after a nested table.  A cell whose last content is a table must
		 * end with a paragraph; docx4j drops it (WordLayoutFixups.dropParagraphAfterNestedTable,
		 * measured on a corpus document where Word gives it no line).  The 311-page report
		 * (rules doc §3, open) says otherwise for its shape: outer one-row table, nested
		 * table whose every paragraph keeps with next, an EMPTY trailing paragraph with
		 * w:keepNext, then an empty body paragraph, then a numbered keep-with-next heading
		 * with 10pt before - Word's gap from the last row's text to the heading's is 53.8pt,
		 * which reads as the row (21.2), TWO bare 11.15pt lines and the heading's 10, where
		 * docx4j draws one line and one 10.  The spacing-empty-before probe rules the
		 * spacing model out (larger-of everywhere), so the question is which paragraphs get
		 * a line and which get their space-after.  Spacing values are distinct - the
		 * trailing paragraph 20pt after, the body's empty paragraph 30pt after, the spaced
		 * paragraph 10pt before - so every combination decodes from the gap.
		 */
		PROBES.add(new Probe("table-nested-trailing",
				"a one-row table holding a nested table and its trailing paragraph (empty, "
				+ "empty with w:keepNext, with text), with and without an empty body paragraph "
				+ "after the outer table, before a paragraph with space-before; and a plain "
				+ "table control", () -> {
			Doc d = Doc.create(15);
			d.para("Each case: a one-row table (no cell margins) holding a two-row table, "
					+ "then what the case says, then a paragraph with 200 twips before. "
					+ "Trailing paragraphs have 400 after; an empty body paragraph 600 after.")
					.after(240).add();
			String[] what = {
				"N1: trailing paragraph empty, no keepNext; spaced paragraph directly after the outer table.",
				"N2: trailing paragraph empty WITH keepNext; spaced paragraph directly after.",
				"N3: trailing paragraph empty, no keepNext; then an EMPTY body paragraph, then the spaced paragraph.",
				"N4: the report's shape: nested rows and trailing paragraph all keepNext; empty body paragraph; spaced keepNext paragraph.",
				"N5: trailing paragraph holds text; spaced paragraph directly after.",
				"N6: control: a plain two-row table (no nesting), empty body paragraph, spaced paragraph."
			};
			for (int c = 0; c < what.length; c++) {
				d.para(what[c]).after(240).add();
				boolean kn = c == 1 || c == 3;
				Doc.Table t = new Doc.Table(4000, 4000);
				for (int r = 1; r <= 2; r++) {
					t.rowOf(null, null,
							t.cellOf(4000, null, (c == 3 ? d.para().noLabel().keepNext() : d.para().noLabel())
									.text("N" + (c + 1) + " row " + r + " cell one").build()),
							t.cellOf(4000, null, (c == 3 ? d.para().noLabel().keepNext() : d.para().noLabel())
									.text("N" + (c + 1) + " row " + r + " cell two").build()));
				}
				if (c == 5) {
					d.add(t.build());
				} else {
					Doc.Table outer = new Doc.Table(9000);
					outer.cellMargins(0, 0);
					Doc.Para trailing = d.para().noLabel().after(400);
					if (kn) trailing = trailing.keepNext();
					if (c == 4) trailing = trailing.text("N5 trailing text");
					org.docx4j.wml.Tc tc = outer.cellOf(9000, null, trailing.build());
					tc.getContent().add(0, t.build());
					outer.rowOf(null, null, tc);
					d.add(outer.build());
				}
				if (c == 2 || c == 3 || c == 5) d.para().noLabel().after(600).add();
				Doc.Para spaced = d.para("N" + (c + 1) + " spaced paragraph. " + prose(1, c + 1)).before(200).after(240);
				if (c == 3) spaced = spaced.keepNext();
				spaced.add();
			}
			return d.pkg();
		}));

		/*
		 * The 311-page report's heading seam, one property at a time.  table-nested-trailing
		 * settled that the paragraph after the nested table gets no line and the empty body
		 * paragraph keeps its line and its 10pt after; spacing-empty-before settled that
		 * space-after and space-before combine by larger-of after an empty paragraph.  The
		 * report's gap from the empty paragraph to its numbered, keep-with-next Heading 3
		 * (200 before from the style, line 271 auto) is nonetheless the empty paragraph's
		 * after AND the heading's before, 20 not 10.  Each case below is a one-row table,
		 * an empty paragraph with 200 after, then the target paragraph with 200 before; the
		 * cases add the heading's properties one at a time.  Gap over the empty line: 10 is
		 * larger-of, 20 is additive.  CUT (2026-09-10): 9.4-9.9 in every case (11.6 for
		 * E6/E7, whose style line spacing makes the empty line taller) - larger-of
		 * throughout; the report's paragraph turned out to be a single w:br, two lines
		 * in Word, which docx4j had drawn as one (rules doc §3, §4.3).
		 */
		PROBES.add(new Probe("spacing-empty-heading",
				"an empty paragraph with 200 after before a paragraph with 200 before which is, "
				+ "in turn, plain; numbered; keep-with-next; spaced by its style; a Heading 3 "
				+ "clone (numbered, keep-with-next, style spacing, line 271); and that clone "
				+ "after an empty paragraph whose own spacing comes from a style", () -> {
			Doc d = Doc.create(15);
			d.addParagraphStyle("SpacedHead", "Normal", ppr -> {
				org.docx4j.wml.PPrBase.Spacing sp = new org.docx4j.wml.PPrBase.Spacing();
				sp.setBefore(java.math.BigInteger.valueOf(200)); sp.setAfter(java.math.BigInteger.ZERO);
				sp.setLine(java.math.BigInteger.valueOf(271)); sp.setLineRule(org.docx4j.wml.STLineSpacingRule.AUTO);
				ppr.setSpacing(sp);
			});
			d.addParagraphStyle("Head3Clone", "Normal", ppr -> {
				org.docx4j.wml.PPrBase.Spacing sp = new org.docx4j.wml.PPrBase.Spacing();
				sp.setBefore(java.math.BigInteger.valueOf(200)); sp.setAfter(java.math.BigInteger.ZERO);
				sp.setLine(java.math.BigInteger.valueOf(271)); sp.setLineRule(org.docx4j.wml.STLineSpacingRule.AUTO);
				ppr.setSpacing(sp);
				ppr.setKeepNext(new org.docx4j.wml.BooleanDefaultTrue());
				org.docx4j.wml.PPrBase.NumPr numPr = new org.docx4j.wml.PPrBase.NumPr();
				org.docx4j.wml.PPrBase.NumPr.Ilvl ilvl = new org.docx4j.wml.PPrBase.NumPr.Ilvl();
				ilvl.setVal(java.math.BigInteger.ZERO); numPr.setIlvl(ilvl);
				org.docx4j.wml.PPrBase.NumPr.NumId numId = new org.docx4j.wml.PPrBase.NumPr.NumId();
				numId.setVal(java.math.BigInteger.ONE); numPr.setNumId(numId);
				ppr.setNumPr(numPr);
			});
			d.addParagraphStyle("EmptyAfter", "Normal", ppr -> {
				org.docx4j.wml.PPrBase.Spacing sp = new org.docx4j.wml.PPrBase.Spacing();
				sp.setAfter(java.math.BigInteger.valueOf(200)); sp.setLine(java.math.BigInteger.valueOf(276));
				sp.setLineRule(org.docx4j.wml.STLineSpacingRule.AUTO);
				ppr.setSpacing(sp);
			});
			d.para("Each case: a one-row table, an empty paragraph with 200 twips after, then "
					+ "the target paragraph with 200 before. The gap over the empty line is 10pt "
					+ "where the spaces combine by larger-of and 20pt where they add.").after(240).add();
			String[] what = {
				"E1: the target is a plain paragraph, 200 before direct.",
				"E2: the target is numbered (w:numPr), 200 before direct.",
				"E3: the target has w:keepNext, 200 before direct.",
				"E4: the target's 200 before and line 271 come from its style.",
				"E5: the target is a Heading 3 clone: style spacing, w:keepNext, numbered.",
				"E6: E5 after an empty paragraph whose 200 after comes from ITS style.",
				"E7: E6 with w:ind left and right 29 on the empty paragraph."
			};
			for (int c = 0; c < what.length; c++) {
				if (c == 5) d.pageBreak(); // E6 and E7 on a page of their own: no seam may straddle a page
				d.para(what[c]).after(240).add();
				Doc.Table t = new Doc.Table(4000, 4000);
				t.rowOf(null, null,
						t.cellOf(4000, null, Doc.plainParagraph("E" + (c + 1) + " cell one", SERIF, 24)),
						t.cellOf(4000, null, Doc.plainParagraph("E" + (c + 1) + " cell two", SERIF, 24)));
				d.add(t.build());
				Doc.Para empty = d.para().noLabel();
				if (c >= 5) empty = empty.style("EmptyAfter").inheritSpacing(); else empty = empty.after(200);
				if (c == 6) empty = empty.indent(29, 0, 0);
				empty.add();
				Doc.Para target = d.para("E" + (c + 1) + " target paragraph. " + prose(1, c + 1)).after(240);
				switch (c) {
					case 0: target = target.before(200); break;
					case 1: target = target.before(200).listItem(); break;
					case 2: target = target.before(200).keepNext(); break;
					case 3: target = target.style("SpacedHead").inheritSpacing(); break;
					default: target = target.style("Head3Clone").inheritSpacing().noIndent();
				}
				target.add();
			}
			return d.pkg();
		}));

		/*
		 * w:hideMark: "ignore the end-of-cell mark when sizing the row" (ECMA-376
		 * 17.4.23).  107 corpus documents carry it on 13,977 cells, 1,792 of them empty,
		 * and docx4j ignores it, so an empty row is a full line tall.  Measured on a
		 * corpus letter template (Times New Roman 12pt marks, tblCellMar 15 all round,
		 * tblCellSpacing 15): three consecutive empty hideMark rows are 25.9pt in Word,
		 * 8.6 each, against our 41.4.  What that 8.6 is made of - margins, spacing, a
		 * minimum, or a fraction of the mark's line - is what this probe separates.
		 * CUT (2026-09-10): M1 three empty hideMark rows 8.4pt together (2.8 each), M4 with
		 * 6pt marks identical, M2 with cell spacing 5pt each, M3/M5 controls a full line
		 * each, M6 text cells unchanged: the mark takes no line at all.  Rules doc §6.
		 */
		PROBES.add(new Probe("table-hidemark",
				"empty rows whose cells carry w:hideMark, with and without cell spacing, at 12pt "
				+ "and 6pt marks, beside the same rows without the flag; and a hideMark cell "
				+ "holding text", () -> {
			Doc d = Doc.create(15);
			d.para("Each table below: a text row, three empty rows, a text row. Where the cells "
					+ "carry w:hideMark Word ignores the cell mark's height when sizing the row.")
					.after(240).add();
			String[][] cases = {
				{"M1", "hideMark, no cell spacing, 12pt marks", "1", "0", "24"},
				{"M2", "hideMark, cell spacing 15, 12pt marks", "1", "15", "24"},
				{"M3", "no hideMark, no cell spacing, 12pt marks (control)", "0", "0", "24"},
				{"M4", "hideMark, no cell spacing, 6pt marks", "1", "0", "12"},
				{"M5", "no hideMark, cell spacing 15, 12pt marks (control)", "0", "15", "24"}
			};
			for (String[] c : cases) {
				boolean hide = c[2].equals("1"); int spacing = Integer.parseInt(c[3]); int sz = Integer.parseInt(c[4]);
				d.para(c[0] + ": " + c[1] + ".").before(240).after(120).add();
				Doc.Table t = new Doc.Table(4500, 4500);
				if (spacing > 0) t.cellSpacing(spacing);
				t.cellMargins(15, 15);
				for (int r = 0; r < 5; r++) {
					boolean text = r == 0 || r == 4;
					org.docx4j.wml.Tc[] tcs = new org.docx4j.wml.Tc[2];
					for (int k = 0; k < 2; k++) {
						Doc.Para p = d.para().noLabel().after(0).noLine().markSize(sz);
						if (text) p = p.text(c[0] + (r == 0 ? " top row" : " bottom row") + " cell " + (k + 1));
						tcs[k] = t.cellOf(4500, null, p.build());
						if (hide) tcs[k].getTcPr().setHideMark(new org.docx4j.wml.BooleanDefaultTrue());
					}
					t.rowOf(null, null, tcs);
				}
				d.add(t.build());
			}
			d.para("M6: a hideMark cell holding text beside an empty hideMark cell, no spacing, "
					+ "12pt marks.").before(240).after(120).add();
			Doc.Table t6 = new Doc.Table(4500, 4500);
			t6.cellMargins(15, 15);
			for (int r = 0; r < 3; r++) {
				org.docx4j.wml.Tc a = t6.cellOf(4500, null, d.para().noLabel().after(0).noLine().markSize(24).text("M6 row " + (r + 1) + " text").build());
				org.docx4j.wml.Tc b = t6.cellOf(4500, null, d.para().noLabel().after(0).noLine().markSize(24).build());
				a.getTcPr().setHideMark(new org.docx4j.wml.BooleanDefaultTrue());
				b.getTcPr().setHideMark(new org.docx4j.wml.BooleanDefaultTrue());
				t6.rowOf(null, null, a, b);
			}
			d.add(t6.build());
			d.para("after the tables. " + prose(1)).before(240).add();
			return d.pkg();
		}));

		/*
		 * J14: what a section's w:vAlign counts as the block it aligns.  §7's s75 makes
		 * w:vAlign a display-align on fo:region-body, and a corpus document's
		 * centred title section is then a uniform +5.9pt low over every line, with an
		 * identical block height (316.6 against 316.9): its last block is
		 * space-after="10pt" and FOP counts that 10pt inside the aligned content where
		 * Word appears not to.  Whether Word counts it has never been measured.
		 *
		 * Each alignment gets three sections: one whose last block is a paragraph with
		 * 24pt space-after, a control whose last paragraph has none - so the 24pt can be
		 * read as a difference rather than against an absolute - and one whose last block
		 * is a table.  The first two carry the w:sectPr on the spaced paragraph itself,
		 * which is where Word puts it and which keeps that paragraph the section's last
		 * block; a table cannot carry a w:sectPr, so the table sections end in the empty
		 * paragraph Word requires after a table, which has no spacing of its own.
		 *
		 * Every section is short, so the alignment has somewhere to move the content to.
		 */
		PROBES.add(new Probe("section-valign-bottom",
				"w:vAlign bottom, center, both and top on short sections whose last "
				+ "paragraph has 24pt space-after, on controls whose last paragraph has "
				+ "none, and on sections whose last block is a table; plus a bottom-aligned "
				+ "section whose first paragraph has 24pt space-before", () -> {
			Doc d = Doc.create(15);
			String[] aligns = { "bottom", "center", "both", "top" };
			int s = 0;
			for (String a : aligns) {
				// (i) the last block is a paragraph with 24pt space-after, and it carries
				//     the w:sectPr itself
				s++;
				d.verticalAlignment(a);
				d.para("V" + s + ": this section is w:vAlign " + a + " and its last block is "
						+ "a paragraph whose w:spacing w:after is 24pt. "
						+ prose(2, s)).after(240).add();
				P last = d.para("V" + s + " last paragraph, w:after 24pt. " + prose(1, s))
						.after(480).add();
				d.endSectionOn(last, "nextPage");

				// (ii) the control: the same section with no space-after at all
				s++;
				d.verticalAlignment(a);
				d.para("V" + s + ": the control for V" + (s - 1) + " - w:vAlign " + a
						+ ", and its last paragraph has no space-after at all. "
						+ prose(2, s)).after(240).add();
				P lastCtl = d.para("V" + s + " last paragraph, w:after 0. " + prose(1, s))
						.after(0).add();
				d.endSectionOn(lastCtl, "nextPage");

				// (iii) the last block is a table, followed by the empty paragraph Word
				//       requires after one; that paragraph carries the w:sectPr
				s++;
				d.verticalAlignment(a);
				d.para("V" + s + ": w:vAlign " + a + " again, and this section's last block "
						+ "is a table rather than a paragraph. The empty paragraph Word "
						+ "requires after a table follows it, and carries the w:sectPr. "
						+ prose(2, s)).after(240).add();
				Doc.Table t = new Doc.Table(4500, 4500);
				t.row(SERIF, 24, false, "V" + s + " row one, left", "V" + s + " row one, right");
				t.row(SERIF, 24, false, "V" + s + " row two, left", "V" + s + " row two, right");
				d.add(t.build());
				d.endSection("nextPage", 0);
			}
			// (iv) the other end of the same question: does a bottom-aligned section count
			//      its FIRST block's space-before?
			s++;
			d.verticalAlignment("bottom");
			d.para("V" + s + ": w:vAlign bottom, and this section's FIRST paragraph carries "
					+ "24pt space-before rather than its last carrying space-after. "
					+ prose(2, s)).before(480).after(240).add();
			P lastBefore = d.para("V" + s + " last paragraph, no spacing. " + prose(1, s))
					.after(0).add();
			d.endSectionOn(lastBefore, "nextPage");

			// the document's last section, where the w:vAlign is on the body sectPr rather
			// than on a break paragraph
			s++;
			d.verticalAlignment("bottom");
			d.para("V" + s + ": the document's last section, w:vAlign bottom, so the "
					+ "w:vAlign is on the body sectPr. " + prose(2, s)).after(240).add();
			d.para("V" + s + " last paragraph, w:after 24pt. " + prose(1, s)).after(480).add();
			return d.pkg();
		}));

		/* How much of a collapsed top border Word reserves above the first row's
		 * content.  A collapsed border is centred on the boundary it draws, so FOP
		 * charges half of it to each of the two cells it separates - and at the top of a
		 * table the outer half falls outside the table, leaving ~0 reserved.  Two
		 * independent measurements of real documents disagree on the amount (one a flat
		 * 2 x half the width, one 1.3 x it), which is why nothing is shipped for it: page
		 * 1's first four baselines here settle it.
		 *
		 * A: one 0.5pt border, the table the very first block of the body - the plain
		 *    case, and the one FOP under-reserves most visibly.
		 * B: the same table after a paragraph - is the top of the body special at all?
		 * C: two tables stacked with nothing between them, so their bottom and top
		 *    borders coincide; and the second one's row-1 baseline says whether Word
		 *    reserves one border there or two.
		 * D: an outer 0.5pt-bordered table whose first cell opens with a nested table
		 *    carrying its own 0.5pt borders - two coincident borders in the other
		 *    arrangement.
		 * E: the same as A at 1pt (sz 8), so the reserve can be read as a proportion of
		 *    the border width rather than as a constant. */
		PROBES.add(new Probe("table-first-row-border",
				"a collapsed 0.5pt table border at the very top of the body, the same "
				+ "after a paragraph, two tables stacked, a nested table's border on the "
				+ "outer one's, and a 1pt border", () -> {
			Doc d = Doc.create(15);

			// A: the table is the first block of the body
			Doc.Table a = new Doc.Table(4000, 4000).fixedLayout().borders(4);
			a.row(SERIF, 24, false, "A1 top of body sz4", "A1 right");
			a.row(SERIF, 24, false, "A2 second row", "A2 right");
			d.add(a.build());
			d.para("A after the table. " + prose(1)).before(240).after(240).add();

			// B: a paragraph first, then the same table
			d.pageBreak();
			d.para("B before the table, one line. " + prose(1, 1)).after(240).add();
			Doc.Table b = new Doc.Table(4000, 4000).fixedLayout().borders(4);
			b.row(SERIF, 24, false, "B1 after a paragraph", "B1 right");
			b.row(SERIF, 24, false, "B2 second row", "B2 right");
			d.add(b.build());
			d.para("B after the table. " + prose(1, 2)).before(240).add();

			// C: two tables stacked, their borders coincident
			d.pageBreak();
			Doc.Table c1 = new Doc.Table(4000, 4000).fixedLayout().borders(4);
			c1.row(SERIF, 24, false, "C1 upper table", "C1 right");
			c1.row(SERIF, 24, false, "C2 upper second", "C2 right");
			d.add(c1.build());
			Doc.Table c2 = new Doc.Table(4000, 4000).fixedLayout().borders(4);
			c2.row(SERIF, 24, false, "C3 lower table", "C3 right");
			c2.row(SERIF, 24, false, "C4 lower second", "C4 right");
			d.add(c2.build());
			d.para("C after the tables. " + prose(1, 3)).before(240).add();

			// D: a nested table's top border on the outer table's own
			d.pageBreak();
			Doc.Table inner = new Doc.Table(1800, 1800).fixedLayout().borders(4);
			inner.row(SERIF, 24, false, "D1 nested", "D1 right");
			inner.row(SERIF, 24, false, "D2 nested", "D2 right");
			Doc.Table outer = new Doc.Table(4000, 4000).fixedLayout().borders(4);
			outer.rowOf(null, null,
					outer.cellWith(inner.build(), "D3 after the nested table", SERIF, 24),
					outer.cell("D3 outer right", SERIF, 24, 1, 4000));
			outer.row(SERIF, 24, false, "D4 outer second row", "D4 right");
			d.add(outer.build());
			d.para("D after the table. " + prose(1, 4)).before(240).add();

			// E: the same as A, at twice the border width
			d.pageBreak();
			Doc.Table e = new Doc.Table(4000, 4000).fixedLayout().borders(8);
			e.row(SERIF, 24, false, "E1 sz8 border", "E1 right");
			e.row(SERIF, 24, false, "E2 second row", "E2 right");
			d.add(e.build());
			d.para("E after the table. " + prose(1, 5)).before(240).add();

			return d.pkg();
		}));
	}


	/**
	 * §6.1's grid edge where the cap min(shift, max(0, w:tblInd)) and the corpus
	 * disagree: a grid-sized table at a negative w:tblInd, and a fixed-layout table with
	 * no w:tblInd at all beside its autofit twin, each with Word's default cell margins
	 * and with the table's own w:tblCellMar 108, one case a page.
	 */
	private static Probe gridEdgeSignedProbe(int compatMode) {
		return new Probe("table-grid-edge-signed-compat" + compatMode,
				"grid-sized tables at w:tblInd -108 and -360, and a table with no w:tblInd "
				+ "fixed and autofit, with default and with 108-twip cell margins, "
				+ "compatibilityMode " + compatMode, () -> {
			Doc d = Doc.create(compatMode);
			int page = 0;
			// (a) a negative w:tblInd, which the signed cap cancels the shift for
			for (int ind : new int[] { -108, -360 }) {
				for (int mar = 0; mar < 2; mar++) {
					if (page++ > 0) d.pageBreak();
					d.para("w:tblInd " + ind + ", "
							+ (mar == 0 ? "Word's default cell margins" : "w:tblCellMar 108")
							+ ". The lines of this paragraph start on the text margin. "
							+ prose(2, page)).after(240).add();
					Doc.Table t = new Doc.Table(4000, 4000).indent(ind);
					if (mar == 1) t.cellMargins(108, 0);
					t.row(SERIF, 24, false, "IND" + ind + (mar == 1 ? " margin 108" : ""), "right");
					d.add(t.build());
					d.para("after the table. " + prose(1, page)).before(240).add();
				}
			}
			// (b) no w:tblInd at all, fixed layout and autofit
			for (int fixed = 1; fixed >= 0; fixed--) {
				for (int mar = 0; mar < 2; mar++) {
					d.pageBreak();
					d.para("no w:tblInd at all, " + (fixed == 1 ? "w:tblLayout fixed" : "autofit")
							+ ", " + (mar == 0 ? "Word's default cell margins" : "w:tblCellMar 108")
							+ ". " + prose(2, page)).after(240).add();
					Doc.Table t = new Doc.Table(4000, 4000);
					if (fixed == 1) t.fixedLayout();
					if (mar == 1) t.cellMargins(108, 0);
					t.row(SERIF, 24, false, (fixed == 1 ? "FIXED" : "AUTOFIT")
							+ (mar == 1 ? " margin 108" : ""), "right");
					d.add(t.build());
					d.para("after the table. " + prose(1, page)).before(240).add();
					page++;
				}
			}
			return d.pkg();
		});
	}

	/** One right stop at the right margin with a dot leader, which is what makes a lone
	 *  tab visible: the dots it draws are as tall as the tab's own font. */
	private static Doc.Para leaderStop(Doc.Para p) {
		return p.tabStop(9000, org.docx4j.wml.STTabJc.RIGHT, org.docx4j.wml.STTabTlc.DOT);
	}

	/**
	 * A two-row table whose left cell holds a 24pt inline picture: in row A the picture
	 * cell is the taller of the two, in row B the text cell is, and the picture cell also
	 * holds a line of text.  {@code vAlign} is written on every cell ("center"), or on
	 * none when null.
	 */
	private static Tbl pictureCellTable(Doc d, String tag, String vAlign) throws Exception {
		Doc.Table t = new Doc.Table(3000, 6000);
		P picOnly = Doc.plainParagraph("", SERIF, 20);
		picOnly.getContent().clear();
		picOnly.getContent().add(d.inlineImage(200, 80, 1200));
		t.rowOf(null, null,
				t.cellOf(3000, vAlign, picOnly),
				t.cellOf(6000, vAlign,
						Doc.plainParagraph(tag + " A: one line beside a 24pt picture", SERIF, 20)));
		P picAndText = Doc.plainParagraph("", SERIF, 20);
		picAndText.getContent().clear();
		picAndText.getContent().add(d.inlineImage(200, 80, 1200));
		t.rowOf(null, null,
				t.cellOf(3000, vAlign, picAndText,
						Doc.plainParagraph(tag + " B: text under the picture", SERIF, 20)),
				t.cellOf(6000, vAlign,
						Doc.plainParagraph(tag + " B line 1", SERIF, 20),
						Doc.plainParagraph(tag + " B line 2", SERIF, 20),
						Doc.plainParagraph(tag + " B line 3", SERIF, 20),
						Doc.plainParagraph(tag + " B line 4", SERIF, 20)));
		return t.build();
	}

	/** The run in italics, which is what makes the FO exporter wrap the picture in an
	 *  fo:inline and so give its block a line box of its own. */
	private static org.docx4j.wml.R italic(org.docx4j.wml.R r) {
		org.docx4j.wml.RPr rpr = Doc.F.createRPr();
		rpr.setI(new org.docx4j.wml.BooleanDefaultTrue());
		r.setRPr(rpr);
		return r;
	}
	/**
	 * §6.1's grid edge below mode 14: w:tblInd 108 with Word's default cell margins and
	 * with the table's own, at the top level and nested in a cell.  Since 17.1.0 the
	 * shift is applied in mode 14 alone, which no Word golden has confirmed for the older
	 * modes (§6.1); this is that measurement.
	 */
	private static Probe gridEdgeNestedProbe(int compatMode) {
		return new Probe("table-grid-edge-compat" + compatMode,
				"w:tblInd 108 with default and with 108-twip cell margins, top-level and "
				+ "nested in a cell, compatibilityMode " + compatMode, () -> {
			Doc d = Doc.create(compatMode);

			d.para("top level, default cell margins. " + prose(1)).after(240).add();
			d.add(new Doc.Table(4000, 4000).indent(108)
					.row(SERIF, 24, true, "default margins", "right").build());

			d.para("top level, w:tblCellMar 108. " + prose(1, 1)).before(240).after(240).add();
			d.add(new Doc.Table(4000, 4000).indent(108).cellMargins(108, 0)
					.row(SERIF, 24, true, "cellMar 108", "right").build());

			d.para("nested, both levels default margins. " + prose(1, 2)).before(240).after(240).add();
			Doc.Table outer = new Doc.Table(4000, 4000).indent(108);
			Tbl inner = new Doc.Table(1800, 1800)
					.row(SERIF, 24, true, "nested A", "nested B").build();
			outer.rowOf(null, null, outer.cellWith(inner, "", SERIF, 24),
					outer.cell("outer right", SERIF, 24, 1, null));
			d.add(outer.build());

			d.para("nested, w:tblCellMar 108 on both. " + prose(1, 3)).before(240).after(240).add();
			Doc.Table outer2 = new Doc.Table(4000, 4000).indent(108).cellMargins(108, 0);
			Tbl inner2 = new Doc.Table(1800, 1800).cellMargins(108, 0)
					.row(SERIF, 24, true, "nested C", "nested D").build();
			outer2.rowOf(null, null, outer2.cellWith(inner2, "", SERIF, 24),
					outer2.cell("outer right", SERIF, 24, 1, null));
			d.add(outer2.build());

			d.para("after. " + prose(1, 4)).before(240).add();
			return d.pkg();
		});
	}

	/** A corpus table of contents' stops: three plain left stops and a right stop with a
	 *  dot leader at the right margin.  The corpus TOC's own right stop is 9990 twips;
	 *  this page's content is 9026 wide, so the stop is 9000 - a stop past the right
	 *  indent is the tab-clamp-right probe's question, not this one's. */
	private static Doc.Para tocStops(Doc.Para p) {
		return p.tabStop(360, org.docx4j.wml.STTabJc.LEFT)
				.tabStop(540, org.docx4j.wml.STTabJc.LEFT)
				.tabStop(851, org.docx4j.wml.STTabJc.LEFT)
				.tabStop(9000, org.docx4j.wml.STTabJc.RIGHT, org.docx4j.wml.STTabTlc.DOT);
	}

	/** Another corpus table of contents' stops: one left stop and a right dot stop at the
	 *  right margin (the corpus's own are 1320 and 9350, on a Letter-width page whose
	 *  content edge is 540pt). */
	private static Doc.Para tocEntryStops(Doc.Para p) {
		return p.tabStop(1320, org.docx4j.wml.STTabJc.LEFT)
				.tabStop(9020, org.docx4j.wml.STTabJc.RIGHT, org.docx4j.wml.STTabTlc.DOT);
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

	/** A w:numPr naming {@code numId} and no w:ilvl at all - the shape Word writes on a
	 *  paragraph style linked to a list. */
	/** A three-level decimal w:abstractNum, Word's usual indents, no rPr and no style links. */
	private static String abstractDecimal(int abstractNumId) {
		return "<w:abstractNum w:abstractNumId=\"" + abstractNumId + "\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
				+ Doc.decimalLevel(0, null, 720, 360)
				+ Doc.decimalLevel(1, null, 1440, 360)
				+ Doc.decimalLevel(2, null, 2160, 360)
				+ "</w:abstractNum>";
	}

	/** A w:num with no overrides. */
	private static String num(int numId, int abstractNumId) {
		return "<w:num w:numId=\"" + numId + "\"><w:abstractNumId w:val=\"" + abstractNumId + "\"/></w:num>";
	}

	/** A three-level list whose labels show every counter (%1. / %1.%2. / %1.%2.%3.) and
	 *  whose level 2 carries {@code w:lvlRestart} with the given value. */
	private static String restartAbstract(int abstractNumId, int lvlRestart) {
		StringBuilder sb = new StringBuilder("<w:abstractNum w:abstractNumId=\"" + abstractNumId
				+ "\"><w:multiLevelType w:val=\"multilevel\"/>");
		String[] text = { "%1.", "%1.%2.", "%1.%2.%3." };
		for (int ilvl = 0; ilvl < 3; ilvl++) {
			sb.append("<w:lvl w:ilvl=\"" + ilvl + "\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>");
			if (ilvl == 2) sb.append("<w:lvlRestart w:val=\"" + lvlRestart + "\"/>");
			sb.append("<w:lvlText w:val=\"" + text[ilvl] + "\"/><w:lvlJc w:val=\"left\"/>"
					+ "<w:pPr><w:ind w:left=\"" + (720 + 720 * ilvl) + "\" w:hanging=\"720\"/></w:pPr></w:lvl>");
		}
		return sb.append("</w:abstractNum>").toString();
	}

	/** Three one-line numbered paragraphs (w:num 70, level 0) for the stories probe. */
	private static List<P> items(Doc d, String text) throws Exception {
		List<P> list = new ArrayList<>();
		for (int k = 1; k <= 3; k++) list.add(d.para(text).numPr(70, 0).build());
		return list;
	}

	private static PPrBase.NumPr numPr(int numId) {
		PPrBase.NumPr np = Doc.F.createPPrBaseNumPr();
		PPrBase.NumPr.NumId id = Doc.F.createPPrBaseNumPrNumId();
		id.setVal(BigInteger.valueOf(numId));
		np.setNumId(id);
		return np;
	}

	/**
	 * One table of {@code table-cell-measure}.  Three rows hold the same measured line in
	 * a column exactly wide enough for it (its advance rounded up to a whole twip, plus
	 * two twips of slack, plus the two 108-twip cell margins) and differ only in how much
	 * of the cell's own <b>end</b> margin they give back: nothing, half the border, the
	 * whole border.  The start margin never moves, so all three lines begin at the same x
	 * and only the measure changes, and the row which wraps says what Word charged.
	 *
	 * <p>A content-autofit table cannot wrap, so it gets one row and is read off its
	 * border positions instead.
	 */
	private static Tbl measureTable(String tag, String sentence, int eighthsOfPoint,
			int cellSpacingTwips, boolean autofit, boolean noBorders) {
		int borderTwips = eighthsOfPoint * 5 / 2;   // eighths of a point -> twips
		int advance = Doc.advanceTwipsCeil(tag + "R1 " + sentence, SERIF, 24);
		int col = advance + 2 + 216;
		Doc.Table t = new Doc.Table(col);
		if (autofit) {
			t.autoWidth();
		} else {
			t.fixedLayout();
		}
		t.cellMargins(108, 0);
		if (noBorders) {
			t.noBorders();
		} else {
			t.borders(eighthsOfPoint);
		}
		if (cellSpacingTwips > 0) t.cellSpacing(cellSpacingTwips);
		int rows = autofit ? 1 : 3;
		for (int r = 1; r <= rows; r++) {
			int give = r == 1 ? 0 : (r == 2 ? borderTwips / 2 : borderTwips);
			org.docx4j.wml.Tc tc = t.cellOf(autofit ? null : col, null,
					Doc.plainParagraph(tag + "R" + r + " " + sentence, SERIF, 24));
			Doc.Table.tcMargins(tc, 108, 108 - give);
			t.rowOf(null, null, tc);
		}
		return t.build();
	}

	// ------------------------------------------------- w:compat, one family per flag group
	//
	// Every w:compat flag is document-level, so a probe pair is two otherwise identical
	// documents: the plain one states nothing (and so takes its compatibility mode's
	// default) and the "-set" twin states the flag(s) explicitly.  What the goldens have
	// to show is whether Word's own layout differs between the two.

	/** Justified prose whose lines end in a soft return (w:br with no type). */
	private static WordprocessingMLPackage shiftReturn(int mode, Boolean flag) throws Exception {
		Doc d = Doc.create(mode);
		if (flag != null) d.compat("doNotExpandShiftReturn", flag.booleanValue());
		d.para("P01 doNotExpandShiftReturn = " + (flag == null ? "absent" : flag)
				+ ", compatibilityMode " + mode).add();
		// two full lines then a soft return, three times: what is measured is the right
		// edge of each line that ends at the break
		for (int i = 0; i < 3; i++) {
			d.para().jc(JcEnumeration.BOTH)
					.text("P0" + (i + 2) + " " + longProse(3, i))
					.softReturn()
					.text(longProse(3, i + 4))
					.softReturn()
					.text(prose(1, i + 8))
					.add();
		}
		// a control: the same prose with no soft return in it at all
		d.para("P05 control " + longProse(6, 2)).jc(JcEnumeration.BOTH).add();
		// and one whose break falls where the line is nearly full anyway
		d.para().jc(JcEnumeration.BOTH).text("P06 " + prose(1)).softReturn()
				.text(longProse(4, 1)).add();
		return d.pkg();
	}

	/** A numbered paragraph with a hanging indent and a later tab stop. */
	private static WordprocessingMLPackage numberingTab(int mode, Boolean flags) throws Exception {
		Doc d = Doc.create(mode);
		if (flags != null) {
			d.compat("doNotUseIndentAsNumberingTabStop", flags.booleanValue());
			d.compat("noTabHangInd", flags.booleanValue());
		}
		d.para("P01 doNotUseIndentAsNumberingTabStop / noTabHangInd = "
				+ (flags == null ? "absent" : flags) + ", compatibilityMode " + mode).add();
		d.numberingXml(
				"<w:abstractNum w:abstractNumId=\"70\">"
				+ Doc.decimalLevel(0, null, 720, 360)
				+ "</w:abstractNum>"
				+ "<w:num w:numId=\"70\"><w:abstractNumId w:val=\"70\"/></w:num>");
		for (int i = 0; i < 3; i++) {
			d.para().numPr(70, 0)
					.tabStop(2880, org.docx4j.wml.STTabJc.LEFT)
					.text("P0" + (i + 2) + " label text")
					.tab()
					.text("after the tab " + prose(1, i))
					.add();
		}
		// a hanging indent with no numbering at all: noTabHangInd's own case
		for (int i = 0; i < 2; i++) {
			d.para().indent(1440, 0, 720)
					.tabStop(2880, org.docx4j.wml.STTabJc.LEFT)
					.text("P0" + (i + 5) + " hanging")
					.tab()
					.text("after the tab " + prose(1, i + 3))
					.add();
		}
		return d.pkg();
	}

	/** One over-wide autofit table, one grid-versus-w:tcW table, and one styled table. */
	private static WordprocessingMLPackage compatTables(int mode, Boolean flags) throws Exception {
		Doc d = Doc.create(mode);
		if (flags != null) {
			d.compat("growAutofit", flags.booleanValue());
			d.compat("doNotAutofitConstrainedTables", flags.booleanValue());
			d.compat("layoutRawTableWidth", flags.booleanValue());
			d.compat("useWord2002TableStyleRules", flags.booleanValue());
		}
		d.para("P01 growAutofit / doNotAutofitConstrainedTables / layoutRawTableWidth / "
				+ "useWord2002TableStyleRules = " + (flags == null ? "absent" : flags)
				+ ", compatibilityMode " + mode).add();
		// T1: an autofit table whose own grid is far wider than the text column
		Doc.Table wide = new Doc.Table(4600, 4600, 4600).autoWidth().borders(4);
		wide.row(SERIF, 20, false, "T1C1 " + prose(1), "T1C2 " + prose(1, 1), "T1C3 " + prose(1, 2));
		wide.row(SERIF, 20, false, "T1C1 " + prose(1, 3), "T1C2 " + prose(1, 4), "T1C3 " + prose(1, 5));
		d.add(wide.build());
		d.para("P02 between the tables").add();
		// T2: a grid the row's w:tcW disagree with
		Doc.Table grid = new Doc.Table(3000, 3000, 3000).tableWidth(9000, "dxa").borders(4);
		grid.rowOf(null, null,
				grid.cell("T2C1 " + prose(1), SERIF, 20, 1, 1200),
				grid.cell("T2C2 " + prose(1, 1), SERIF, 20, 1, 1200),
				grid.cell("T2C3 " + prose(1, 2), SERIF, 20, 1, 1200));
		grid.rowOf(null, null,
				grid.cell("T2C1 " + prose(1, 3), SERIF, 20, 1, 3000),
				grid.cell("T2C2 " + prose(1, 4), SERIF, 20, 1, 3000),
				grid.cell("T2C3 " + prose(1, 5), SERIF, 20, 1, 3000));
		d.add(grid.build());
		return d.pkg();
	}

	/** Page breaks, a tab on a right-aligned line, and contextual paragraphs in cells. */
	private static WordprocessingMLPackage compatBreaks(int mode, Boolean flags) throws Exception {
		Doc d = Doc.create(mode);
		if (flags != null) {
			d.compat("splitPgBreakAndParaMark", flags.booleanValue());
			d.compat("suppressSpBfAfterPgBrk", flags.booleanValue());
			d.compat("forgetLastTabAlignment", flags.booleanValue());
			d.compat("allowSpaceOfSameStyleInTable", flags.booleanValue());
		}
		d.para("P01 splitPgBreakAndParaMark / suppressSpBfAfterPgBrk / forgetLastTabAlignment / "
				+ "allowSpaceOfSameStyleInTable = " + (flags == null ? "absent" : flags)
				+ ", compatibilityMode " + mode).add();
		// splitPgBreakAndParaMark: content before the break in the same paragraph
		d.para().text("P02 before the break " + prose(2)).pageBreakRun()
				.text("P02 after the break " + prose(2, 2)).add();
		// suppressSpBfAfterPgBrk: a break-only paragraph then 24pt of space-before
		d.para().pageBreakRun().add();
		d.para("P03 24pt of space-before at the top of the page " + prose(1, 4)).before(480).add();
		// forgetLastTabAlignment: a right-aligned and a centred line ending in a tab
		d.para().jc(JcEnumeration.RIGHT).text("P04 right ").tab().add();
		d.para().jc(JcEnumeration.CENTER).text("P05 centre ").tab().add();
		d.para().jc(JcEnumeration.RIGHT).tabStop(6000, org.docx4j.wml.STTabJc.LEFT)
				.text("P06 right, custom stop ").tab().text("tail").add();
		// allowSpaceOfSameStyleInTable: one contextual paragraph per cell, against
		// space-after that the cell edge would otherwise cancel
		Doc.Table t = new Doc.Table(2400, 2400).borders(4);
		for (int r = 0; r < 3; r++) {
			t.rowOf(null, null,
					t.cellOf(Doc.plainParagraph("T1R" + (r + 1) + "C1", SERIF, 20)),
					t.cellOf(Doc.plainParagraph("T1R" + (r + 1) + "C2", SERIF, 20)));
		}
		org.docx4j.wml.Tbl tbl = t.build();
		contextualCells(tbl);
		d.add(tbl);
		return d.pkg();
	}

	/** Gives every paragraph of every cell w:contextualSpacing and 10pt of space-after. */
	private static void contextualCells(org.docx4j.wml.Tbl tbl) {
		for (Object ro : tbl.getContent()) {
			org.docx4j.wml.Tr tr = (org.docx4j.wml.Tr) org.docx4j.XmlUtils.unwrap(ro);
			for (Object co : tr.getContent()) {
				Object u = org.docx4j.XmlUtils.unwrap(co);
				if (!(u instanceof org.docx4j.wml.Tc)) continue;
				for (Object po : ((org.docx4j.wml.Tc) u).getContent()) {
					Object pu = org.docx4j.XmlUtils.unwrap(po);
					if (!(pu instanceof org.docx4j.wml.P)) continue;
					org.docx4j.wml.P p = (org.docx4j.wml.P) pu;
					if (p.getPPr() == null) p.setPPr(Doc.F.createPPr());
					p.getPPr().setContextualSpacing(new org.docx4j.wml.BooleanDefaultTrue());
					PPrBase.Spacing sp = Doc.F.createPPrBaseSpacing();
					sp.setAfter(BigInteger.valueOf(200));
					p.getPPr().setSpacing(sp);
				}
			}
		}
	}

	static {
		PROBES.add(new Probe("compat-shift-return",
				"justified paragraphs ending in a soft return, mode 15, w:doNotExpandShiftReturn absent",
				() -> shiftReturn(15, null)));
		PROBES.add(new Probe("compat-shift-return-set",
				"the same, with w:doNotExpandShiftReturn stated",
				() -> shiftReturn(15, Boolean.TRUE)));

		PROBES.add(new Probe("compat-numbering-tab",
				"a numbered hanging indent and a later tab stop, mode 15, the two flags absent",
				() -> numberingTab(15, null)));
		PROBES.add(new Probe("compat-numbering-tab-set",
				"the same, with w:doNotUseIndentAsNumberingTabStop and w:noTabHangInd stated",
				() -> numberingTab(15, Boolean.TRUE)));
		PROBES.add(new Probe("compat-numbering-tab-compat12",
				"the same, mode 12, the two flags absent",
				() -> numberingTab(12, null)));

		PROBES.add(new Probe("compat-tables",
				"an over-wide autofit grid and a grid the w:tcW disagree with, mode 15, the table flags absent",
				() -> compatTables(15, null)));
		PROBES.add(new Probe("compat-tables-set",
				"the same, with w:growAutofit, w:doNotAutofitConstrainedTables, w:layoutRawTableWidth and w:useWord2002TableStyleRules stated",
				() -> compatTables(15, Boolean.TRUE)));

		PROBES.add(new Probe("compat-breaks",
				"a split page break, space-before after a break, tabs on aligned lines and contextual cells, mode 15, the flags absent",
				() -> compatBreaks(15, null)));
		PROBES.add(new Probe("compat-breaks-set",
				"the same, with w:splitPgBreakAndParaMark, w:suppressSpBfAfterPgBrk, w:forgetLastTabAlignment and w:allowSpaceOfSameStyleInTable stated",
				() -> compatBreaks(15, Boolean.TRUE)));
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

	/** Gives the paragraph mark (w:pPr/w:rPr) the given size in half-points; an empty
	 *  paragraph's line is sized by it.  @since 17.1.1 */
	private static P markSize(P p, int halfPts) {
		if (p.getPPr() == null) p.setPPr(F.createPPr());
		org.docx4j.wml.ParaRPr rpr = p.getPPr().getRPr() == null ? F.createParaRPr() : p.getPPr().getRPr();
		org.docx4j.wml.HpsMeasure sz = F.createHpsMeasure();
		sz.setVal(BigInteger.valueOf(halfPts));
		rpr.setSz(sz);
		rpr.setSzCs(sz);
		p.getPPr().setRPr(rpr);
		return p;
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
