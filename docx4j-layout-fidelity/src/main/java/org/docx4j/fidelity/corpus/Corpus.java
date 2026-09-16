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

	// CR-016 fonts-* constants (declared before the static block that uses them)
	/** The sentence every fonts-* case sets, so widths compare across cases and probes. */
	private static final String FONTS_SENTENCE =
			"The quick brown fox jumps over the lazy dog while the farmer watches from the gate.";
	private static final String ARABIC_SENTENCE = "مرحبا بالعالم، هذا نص تجريبي قصير لقياس الخط.";
	private static final String HEBREW_SENTENCE = "שלום עולם, זהו משפט קצר לבדיקת הגופן.";
	private static final String CYRILLIC_GREEK_SENTENCE = "Привет, мир: это короткое предложение. Γειά σου κόσμε, αβγδ εζηθ.";
	private static final String LATIN1_SENTENCE = "café über façade naïve ñandú Ångström œuvre – ¿qué? ½ × ÷";
	/** Arial's and Times New Roman's PANOSE-1, as Word writes them in fontTable.xml. */
	private static final String PANOSE_ARIAL = "020B0604020202020204";
	private static final String PANOSE_TIMES = "02020603050405020304";
	/** A real sans's w:sig: Liberation Sans's own OS/2 unicode and code-page ranges, read
	 *  from LiberationSans-Regular.ttf in docx4j-export-fo-fonts-liberation (its PANOSE is
	 *  PANOSE_ARIAL above, Liberation Sans being Arial's metric clone).  Word writes these
	 *  off the font file as it saves, which is what CR-017's authorHad reads. */
	private static final String SIG_LIBERATION_SANS =
			"w:usb0=\"E0000AFF\" w:usb1=\"500078FF\" w:usb2=\"00000021\" w:usb3=\"00000000\""
			+ " w:csb0=\"600001BF\" w:csb1=\"DFF70000\"";


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
		 * CR-015's probe set (docs/developer/change-requests/CR-015-property-resolution.md,
		 * "Are the comments accurate?").  Style resolution: unless a case says otherwise the
		 * runs carry no w:rPr of their own and the paragraphs no direct w:spacing, so what a
		 * paragraph inherits is the whole answer; the case letters are in the text.
		 */
		PROBES.add(new Probe("styles-default-pstyle",
				"docDefaults Carlito 11pt; Normal (the w:default=1 paragraph style) w:rPr Liberation "
				+ "Serif 14pt; style H Liberation Sans 20pt; character style S w:b only.  (a) no "
				+ "w:pStyle, run without w:rPr; (b) w:pStyle Normal; (c) no w:pStyle, run with w:b "
				+ "only; (d) w:pStyle names a style that does not exist; (e) H, run w:rStyle S; "
				+ "(f) H, run w:rStyle DefaultParagraphFont; (g) H, run without w:rPr - does Normal's "
				+ "14pt Serif reach (a), (c) and (d), and H's 20pt Sans reach (e) and (f)?", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(CARLITO, 22);
			Doc.font(SERIF, 28).accept(styleRPr(d, "Normal"));
			d.addParagraphStyle("H", "Normal", ppr -> { }, Doc.font(SANS, 40));
			addCharacterStyle(d, "S", Doc::bold);
			stylesPara(d, null, "(a) no w:pStyle, a run with no w:rPr. " + prose(2, 1));
			stylesPara(d, "Normal", "(b) w:pStyle Normal, a run with no w:rPr. " + prose(2, 2));
			d.para().noLabel().inheritSpacing().bareText("(c) no w:pStyle, a run with w:b only: ")
					.run(bareRun("bold " + prose(2, 3), Doc::bold)).add();
			stylesPara(d, "Missing", "(d) w:pStyle Missing, no such style. " + prose(2, 4));
			d.para().noLabel().inheritSpacing().style("H").bareText("(e) H, a run with w:rStyle S: ")
					.run(bareRun("styled " + prose(2, 5), rStyle("S"))).add();
			d.para().noLabel().inheritSpacing().style("H")
					.bareText("(f) H, a run with w:rStyle DefaultParagraphFont: ")
					.run(bareRun("styled " + prose(2, 6), rStyle("DefaultParagraphFont"))).add();
			stylesPara(d, "H", "(g) H, a run with no w:rPr. " + prose(2, 7));
			return d.pkg();
		}));

		PROBES.add(new Probe("styles-linerule",
				"style X states w:spacing w:line 480 w:lineRule exact (24pt lines).  Paragraphs in X: "
				+ "(a) no direct w:spacing; (b) direct w:spacing stating only w:after 0; (c) direct "
				+ "w:spacing stating only w:line 240 - does (b) keep the exact 24pt pitch (the lineRule "
				+ "is inherited) and does (c) go to single auto (a w:line without a w:lineRule means "
				+ "auto)?", () -> {
			Doc d = Doc.create(15);
			d.addParagraphStyle("X", "Normal", ppr -> {
				PPrBase.Spacing sp = Doc.F.createPPrBaseSpacing();
				sp.setLine(BigInteger.valueOf(480));
				sp.setLineRule(STLineSpacingRule.EXACT);
				ppr.setSpacing(sp);
			});
			stylesPara(d, "X", "(a) X, no direct w:spacing. " + prose(4, 1));
			PPrBase.Spacing after0 = Doc.F.createPPrBaseSpacing();
			after0.setAfter(BigInteger.ZERO);
			stylesPara(d, "X", "(b) X, direct w:spacing w:after 0 only. " + prose(4, 2)).getPPr().setSpacing(after0);
			PPrBase.Spacing line240 = Doc.F.createPPrBaseSpacing();
			line240.setLine(BigInteger.valueOf(240));
			stylesPara(d, "X", "(c) X, direct w:spacing w:line 240 only. " + prose(4, 3)).getPPr().setSpacing(line240);
			return d.pkg();
		}));

		PROBES.add(new Probe("styles-numpr-ilvl-only",
				"a two-level list (w:num 90: %1. at 720/360, %1.%2. at 1440/360); style L carries "
				+ "w:numPr numId 90 ilvl 0; L2 is based on L and carries a w:numPr of w:ilvl 1 only.  "
				+ "(a) L; (b) L with a direct w:numPr of w:ilvl 1 only; (c) L with a direct w:numPr of "
				+ "w:numId 90 only; (d) L2; (e) L with a direct w:numPr of both (numId 90, ilvl 1), the "
				+ "control - do (b) and (d) take level 1 (x.y.) and does (c) stay at level 0?", () -> {
			Doc d = Doc.create(15);
			d.numberingXml(twoLevelAbstract(90) + num(90, 90));
			d.addParagraphStyle("L", "Normal", ppr -> ppr.setNumPr(numPrOf(90, 0)));
			d.addParagraphStyle("L2", "L", ppr -> ppr.setNumPr(numPrOf(null, 1)));
			d.para("Style L numbers with w:num 90 at level 0; L2, based on L, states w:ilvl 1 only.")
					.after(120).add();
			stylesPara(d, "L", "(a) L");
			stylesPara(d, "L", "(b) L, direct w:numPr of w:ilvl 1 only").getPPr().setNumPr(numPrOf(null, 1));
			stylesPara(d, "L", "(c) L, direct w:numPr of w:numId 90 only").getPPr().setNumPr(numPrOf(90, null));
			stylesPara(d, "L2", "(d) L2");
			stylesPara(d, "L", "(e) L, direct w:numPr numId 90 ilvl 1 (control)").getPPr().setNumPr(numPrOf(90, 1));
			return d.pkg();
		}));

		PROBES.add(new Probe("styles-table-default",
				"the w:default=1 table style (TableNormal) given w:tblCellMar left 300 (15pt) instead "
				+ "of 108.  Three one-row tables: (a) no w:tblStyle; (b) w:tblStyle Custom, a table style "
				+ "with no w:basedOn and no cell margins; (c) w:tblStyle Grid2, based on TableNormal - "
				+ "where does each first cell's text start: margin + 15pt (Table Normal applies), + 5.4pt "
				+ "(Word's built-in default), or + 0?", () -> {
			Doc d = Doc.create(15);
			org.docx4j.wml.Style tn = d.mdp().getStyleDefinitionsPart().getDefaultTableStyle();
			tn.getTblPr().getTblCellMar().getLeft().setW(BigInteger.valueOf(300));
			addTableStyle(d, "Custom", null);
			addTableStyle(d, "Grid2", tn.getStyleId());
			d.para("(a) no w:tblStyle").after(120).add();
			d.add(new Doc.Table(4000, 4000).row(SERIF, 24, false, "a1 first cell", "a2").build());
			d.para("(b) w:tblStyle Custom, no w:basedOn").before(240).after(120).add();
			Tbl b = new Doc.Table(4000, 4000).row(SERIF, 24, false, "b1 first cell", "b2").build();
			tableStyle(b, "Custom");
			d.add(b);
			d.para("(c) w:tblStyle Grid2, based on " + tn.getStyleId()).before(240).after(120).add();
			Tbl c = new Doc.Table(4000, 4000).row(SERIF, 24, false, "c1 first cell", "c2").build();
			tableStyle(c, "Grid2");
			d.add(c);
			return d.pkg();
		}));

		PROBES.add(new Probe("styles-no-size-anywhere",
				"no w:sz anywhere: docDefaults state w:rFonts Carlito but no w:sz, Normal has no "
				+ "w:rPr.  (a) a paragraph whose run has no w:rPr; then the same text in explicit "
				+ "(b) 10pt, (c) 11pt and (d) 12pt runs - which of them does (a) match (docx4j assumes "
				+ "10pt)?", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(CARLITO, 22);
			org.docx4j.wml.RPr dd = d.mdp().getStyleDefinitionsPart().getJaxbElement()
					.getDocDefaults().getRPrDefault().getRPr();
			dd.setSz(null);
			dd.setSzCs(null);
			stylesPara(d, null, "(a) no size anywhere. " + prose(3, 1));
			d.para().noLabel().inheritSpacing().run("(b) explicit 10pt. " + prose(3, 1), CARLITO, 20, null).add();
			d.para().noLabel().inheritSpacing().run("(c) explicit 11pt. " + prose(3, 1), CARLITO, 22, null).add();
			d.para().noLabel().inheritSpacing().run("(d) explicit 12pt. " + prose(3, 1), CARLITO, 24, null).add();
			return d.pkg();
		}));


		/*
		 * CR-016's probe set (docs/developer/change-requests/CR-016-font-selection-and-mapping.md,
		 * "Are the comments accurate?").  Font selection and mapping: which of a run's four
		 * fonts (w:ascii, w:hAnsi, w:eastAsia, w:cs) formats each character, and what Word
		 * does with a font the machine lacks.  Every case sets the same sentence, so the
		 * golden's text layer (widths, pdffonts) decides; the case letters are in the text
		 * and the runs carry no label.
		 */
		PROBES.add(new Probe("fonts-cs-off",
				"docDefaults Carlito 11pt with w:cs Courier New; character style CsOn stating w:cs.  Runs of one "
				+ "sentence: (a) w:cs (control: the cs font, Courier New); (b) w:cs w:val=0; (c) w:rStyle CsOn "
				+ "with a direct w:cs w:val=0; (d) w:rtl w:val=0; (e) w:rtl on Latin text; (f) w:cs on Arabic "
				+ "text (control) - which of (b)-(e) are set in Carlito, i.e. is a false value off?", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(CARLITO, 22);
			docDefaultsRFonts(d).setCs("Courier New");
			addCharacterStyle(d, "CsOn", csOn());
			fontsPara(d, "(a) w:cs: ", FONTS_SENTENCE, csOn());
			fontsPara(d, "(b) w:cs w:val=0: ", FONTS_SENTENCE, csOff());
			fontsPara(d, "(c) w:rStyle CsOn, direct w:cs w:val=0: ", FONTS_SENTENCE, rStyle("CsOn").andThen(csOff()));
			fontsPara(d, "(d) w:rtl w:val=0: ", FONTS_SENTENCE, rtlOff());
			fontsPara(d, "(e) w:rtl: ", FONTS_SENTENCE, rtlOn());
			fontsPara(d, "(f) w:cs, Arabic: ", ARABIC_SENTENCE, csOn());
			fontsPara(d, "(g) nothing, the control: ", FONTS_SENTENCE, null);
			return d.pkg();
		}));

		PROBES.add(new Probe("fonts-space-cjk",
				"ascii/hAnsi Carlito 12pt, eastAsia MS Gothic.  (a) '日本 語 ' repeated: does the space between "
				+ "two CJK words take the ascii font (Carlito's 0.226em space) or the East Asian one (MS Gothic's "
				+ "half em)?  (b) the same with w:hint eastAsia; (c) digits and an ideographic comma between CJK; "
				+ "(d) a Latin word's trailing space before CJK; (e) Carlito only, the control", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(CARLITO, 24);
			fontsPara(d, "(a) ", "日本 語 ".repeat(8), rFonts(null, null, null, "MS Gothic", null));
			fontsPara(d, "(b) hint eastAsia: ", "日本 語 ".repeat(8), rFonts(null, null, null, "MS Gothic", org.docx4j.wml.STHint.EAST_ASIA));
			fontsPara(d, "(c) ", "日本2013年、語 ".repeat(6), rFonts(null, null, null, "MS Gothic", null));
			fontsPara(d, "(d) ", "abc 日本 def ".repeat(6), rFonts(null, null, null, "MS Gothic", null));
			fontsPara(d, "(e) control: ", "abc def ".repeat(8), null);
			return d.pkg();
		}));

		PROBES.add(new Probe("fonts-hebrew-no-cs",
				"docDefaults w:ascii Liberation Sans, w:hAnsi DejaVu Sans, w:cs Liberation Serif, 12pt - three "
				+ "faces which all have Hebrew.  Hebrew text (a) with no w:cs and no w:rtl; (b) w:rtl; (c) w:cs; "
				+ "Arabic text (d) with neither, (e) w:cs; (f) Latin, the control - which slot draws (a) and (d): "
				+ "the ascii font ([MS-OI29500]'s table), the cs font (as the Indic ranges), or something else?", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(SANS, 24);
			docDefaultsRFonts(d).setHAnsi(DEJAVU);
			docDefaultsRFonts(d).setCs(SERIF);
			fontsPara(d, "(a) Hebrew, nothing: ", HEBREW_SENTENCE, null);
			fontsPara(d, "(b) Hebrew, w:rtl: ", HEBREW_SENTENCE, rtlOn());
			fontsPara(d, "(c) Hebrew, w:cs: ", HEBREW_SENTENCE, csOn());
			fontsPara(d, "(d) Arabic, nothing: ", ARABIC_SENTENCE, null);
			fontsPara(d, "(e) Arabic, w:cs: ", ARABIC_SENTENCE, csOn());
			fontsPara(d, "(f) Latin, the control: ", FONTS_SENTENCE, null);
			return d.pkg();
		}));

		PROBES.add(new Probe("fonts-theme-lang",
				"w:themeFontLang w:val et-EE (Estonian); docDefaults name the theme fonts (minorHAnsi); the theme's "
				+ "minor Latin font is Carlito and it carries Office's script list (Ethi Nyala, Beng Vrinda, ...).  "
				+ "(a) a paragraph of prose; (b) the same with w:lang et-EE on the run - Carlito, or Nyala (docx4j "
				+ "maps 'et' to the Ethiopic script because 'eth' contains it)?", () -> {
			Doc d = Doc.create(15);
			themePart(d, CARLITO, CARLITO);
			themeFontLang(d, "et-EE");
			fontsPara(d, "(a) theme font, no w:lang: ", FONTS_SENTENCE + " " + prose(2, 1), null);
			fontsPara(d, "(b) theme font, w:lang et-EE: ", FONTS_SENTENCE + " " + prose(2, 2),
					rpr -> rpr.setLang(Doc.language("et-EE")));
			return d.pkg();
		}));

		PROBES.add(new Probe("fonts-unresolvable",
				"docDefaults Liberation Serif 11pt.  Runs in made-up families no machine has, differing only in "
				+ "their fontTable entry: (a) no entry; (b) w:family swiss, Arial's w:panose1, w:charset 00; (c) "
				+ "w:family roman, Times New Roman's panose; (d) w:altName Arial; (e) w:altName naming another "
				+ "absent family with no entry; (f) w:altName naming an absent family whose own entry says swiss + "
				+ "Arial's panose; (g) as (b) with Cyrillic and Greek text; (h) Liberation Serif, the control - "
				+ "which face draws each (pdffonts): the document default, the UI font, a panose/family match, the "
				+ "altName, or the altName's class; and does (g) change face per character?", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(SERIF, 22);
			fontTable(d,
					fontEntry("Docx4j Probe B", "swiss", PANOSE_ARIAL, null)
					+ fontEntry("Docx4j Probe C", "roman", PANOSE_TIMES, null)
					+ fontEntry("Docx4j Probe D", null, null, "Arial")
					+ fontEntry("Docx4j Probe E", null, null, "Docx4j Probe X")
					+ fontEntry("Docx4j Probe F", null, null, "Docx4j Probe Y")
					+ fontEntry("Docx4j Probe Y", "swiss", PANOSE_ARIAL, null));
			fontsPara(d, "(a) no fontTable entry: ", FONTS_SENTENCE, allFour("Docx4j Probe A"));
			fontsPara(d, "(b) swiss, Arial panose: ", FONTS_SENTENCE, allFour("Docx4j Probe B"));
			fontsPara(d, "(c) roman, Times panose: ", FONTS_SENTENCE, allFour("Docx4j Probe C"));
			fontsPara(d, "(d) altName Arial: ", FONTS_SENTENCE, allFour("Docx4j Probe D"));
			fontsPara(d, "(e) altName absent, no entry: ", FONTS_SENTENCE, allFour("Docx4j Probe E"));
			fontsPara(d, "(f) altName absent, swiss entry: ", FONTS_SENTENCE, allFour("Docx4j Probe F"));
			fontsPara(d, "(g) as (b), Cyrillic and Greek: ", CYRILLIC_GREEK_SENTENCE, allFour("Docx4j Probe B"));
			fontsPara(d, "(h) Liberation Serif, the control: ", FONTS_SENTENCE, allFour(SERIF));
			return d.pkg();
		}));

		PROBES.add(new Probe("fonts-missing-slots",
				"docDefaults with no w:rFonts at all (11pt), and no theme part.  (a) a run with no w:rPr; (b) a run "
				+ "naming w:asciiTheme/w:hAnsiTheme minorHAnsi AND w:ascii/w:hAnsi Liberation Sans; (c) a run with "
				+ "w:ascii Liberation Sans only, text with é ü ñ (the hAnsi range); (d) a run with w:hAnsi Liberation "
				+ "Sans only, ASCII text; (e) Liberation Sans in all four slots, the control - Word's built-in "
				+ "default font, its theme when the document has none, and its fallback for an empty slot", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(SERIF, 22);
			docDefaultsRPr(d).setRFonts(null);
			fontsPara(d, "(a) no w:rFonts anywhere: ", FONTS_SENTENCE, null);
			fontsPara(d, "(b) theme refs and explicit Liberation Sans, no theme part: ", FONTS_SENTENCE, rpr -> {
				org.docx4j.wml.RFonts rf = Doc.F.createRFonts();
				rf.setAsciiTheme(org.docx4j.wml.STTheme.MINOR_H_ANSI);
				rf.setHAnsiTheme(org.docx4j.wml.STTheme.MINOR_H_ANSI);
				rf.setAscii(SANS);
				rf.setHAnsi(SANS);
				rpr.setRFonts(rf);
			});
			fontsPara(d, "(c) w:ascii only, hAnsi-range text: ", LATIN1_SENTENCE, rFonts(SANS, null, null, null, null));
			fontsPara(d, "(d) w:hAnsi only, ASCII text: ", FONTS_SENTENCE, rFonts(null, SANS, null, null, null));
			fontsPara(d, "(e) Liberation Sans, the control: ", FONTS_SENTENCE, allFour(SANS));
			return d.pkg();
		}));

		PROBES.add(new Probe("fonts-light-bold",
				"the same sentence in (a) Calibri Light 11pt; (b) Calibri 11pt; (c) Calibri Light with w:b; (d) "
				+ "Calibri with w:b - the Light face's advances against the regular's (CR-001 ledger4 M1: expect "
				+ "about 0.975), and which face Word's PDF names for (c), Calibri Light with synthetic bold or "
				+ "Calibri Bold (M2)", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(CARLITO, 22);
			d.para().noLabel().inheritSpacing().bareText("(a) Calibri Light: ").run(FONTS_SENTENCE, "Calibri Light", 22, null).add();
			d.para().noLabel().inheritSpacing().bareText("(b) Calibri: ").run(FONTS_SENTENCE, "Calibri", 22, null).add();
			d.para().noLabel().inheritSpacing().bareText("(c) Calibri Light, w:b: ").run(FONTS_SENTENCE, "Calibri Light", 22, Doc::bold).add();
			d.para().noLabel().inheritSpacing().bareText("(d) Calibri, w:b: ").run(FONTS_SENTENCE, "Calibri", 22, Doc::bold).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("fonts-symbol-and-emoji",
				"docDefaults Carlito 12pt.  (a) w:rFonts 'symbol' (lower case) with 'abgdpw'; (b) 'wingdings' "
				+ "(lower case) with U+F0FC U+F0FE U+F0A7; (c) an emoji in Carlito; (d) an arrow, a shadowed "
				+ "square and a check mark (U+2190-U+2BFF) in Carlito; (e) 'Symbol' and (f) 'Wingdings' in title "
				+ "case, the controls - is the symbol-font name case-insensitive, and which face draws the emoji "
				+ "and the symbols Carlito lacks?", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(CARLITO, 24);
			fontsPara(d, "(a) 'symbol': ", "abgdpw", rFonts("symbol", "symbol", null, null, null));
			fontsPara(d, "(b) 'wingdings': ", "", rFonts("wingdings", "wingdings", null, null, null));
			fontsPara(d, "(c) emoji: ", "a😀b 😀😀 c", null);
			fontsPara(d, "(d) symbols: ", "a→b ❑ c ✔ d", null);
			fontsPara(d, "(e) 'Symbol': ", "abgdpw", rFonts("Symbol", "Symbol", null, null, null));
			fontsPara(d, "(f) 'Wingdings': ", "", rFonts("Wingdings", "Wingdings", null, null, null));
			return d.pkg();
		}));

		/* ---------------------------------------------------------- CR-017 phase 5
		 *
		 * Two candidate clones to measure before either enters font-substitutes.xml, and
		 * the one measurement the authorHad inference still needs.  "Metric-compatible"
		 * on a project page is a claim, not a measurement (P052's own comment records
		 * why), so each face is set beside a face Word itself will draw on the VM -
		 * Carlito - which calibrates the golden's size grid: batch 42's method note
		 * measured Word's Carlito against TextMeasurer's at the nominal size to 0.04%,
		 * and reading the size off the PDF instead makes a candidate up to 1% too wide.
		 */

		PROBES.add(new Probe("fonts-segoe-ui",
				"the same sentence in (a) Segoe UI 11pt; (b) Segoe UI with w:b; (c) Segoe UI with w:i; (d) Segoe "
				+ "UI Light; (e) Carlito, the control Word draws itself (the size calibration) - Word's pen "
				+ "advances for each face, to measure Selawik (Microsoft's own open metric-compatible "
				+ "replacement for Segoe UI, OFL) against them.  docx4j sends Segoe UI to Arimo today and Segoe "
				+ "UI Light to Source Sans (CR-017 gap 7)", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(CARLITO, 22);
			d.para().noLabel().inheritSpacing().bareText("(a) Segoe UI: ").run(FONTS_SENTENCE, "Segoe UI", 22, null).add();
			d.para().noLabel().inheritSpacing().bareText("(b) Segoe UI, w:b: ").run(FONTS_SENTENCE, "Segoe UI", 22, Doc::bold).add();
			d.para().noLabel().inheritSpacing().bareText("(c) Segoe UI, w:i: ").run(FONTS_SENTENCE, "Segoe UI", 22, Doc::italic).add();
			d.para().noLabel().inheritSpacing().bareText("(d) Segoe UI Light: ").run(FONTS_SENTENCE, "Segoe UI Light", 22, null).add();
			d.para().noLabel().inheritSpacing().bareText("(e) Carlito, the control: ").run(FONTS_SENTENCE, CARLITO, 22, null).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("fonts-georgia",
				"the same sentence in (a) Georgia 11pt; (b) Georgia with w:b; (c) Georgia with w:i; (d) Carlito, "
				+ "the control Word draws itself (the size calibration) - Word's pen advances for each face, to "
				+ "measure Gelasio (metric-compatible with Georgia by its own description, OFL) against them.  "
				+ "docx4j sends Georgia to P052 today, measured at 1.09x Tinos against Word's 1.076-1.112x", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(CARLITO, 22);
			d.para().noLabel().inheritSpacing().bareText("(a) Georgia: ").run(FONTS_SENTENCE, "Georgia", 22, null).add();
			d.para().noLabel().inheritSpacing().bareText("(b) Georgia, w:b: ").run(FONTS_SENTENCE, "Georgia", 22, Doc::bold).add();
			d.para().noLabel().inheritSpacing().bareText("(c) Georgia, w:i: ").run(FONTS_SENTENCE, "Georgia", 22, Doc::italic).add();
			d.para().noLabel().inheritSpacing().bareText("(d) Carlito, the control: ").run(FONTS_SENTENCE, CARLITO, 22, null).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("fonts-author-had",
				"docDefaults Liberation Serif 11pt.  The same sentence in two made-up families no machine has, "
				+ "differing only in their fontTable entry: (a) a bare w:font entry, name and nothing else; (b) an "
				+ "entry carrying w:family swiss, w:charset and the w:panose1 and w:sig of a real sans (Liberation "
				+ "Sans's own OS/2 values); (c) Liberation Serif, the control - does Word's substitution at render "
				+ "time differ between the two, i.e. does Word read the entry when it lacks the font?  CR-017's "
				+ "authorHad field infers from exactly that evidence, and turns its action round for a name-only "
				+ "entry", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(SERIF, 22);
			fontTable(d,
					bareFontEntry("Docx4j Probe NameOnly")
					+ signedFontEntry("Docx4j Probe Signed", "swiss", PANOSE_ARIAL, SIG_LIBERATION_SANS));
			fontsPara(d, "(a) name-only entry: ", FONTS_SENTENCE, allFour("Docx4j Probe NameOnly"));
			fontsPara(d, "(b) panose and sig: ", FONTS_SENTENCE, allFour("Docx4j Probe Signed"));
			fontsPara(d, "(c) Liberation Serif, the control: ", FONTS_SENTENCE, allFour(SERIF));
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

	// ---------------------------------------------------------------- CR-015 styles-* helpers

	/** The w:rPr of a style in the styles part, created if the style has none. */
	private static org.docx4j.wml.RPr styleRPr(Doc d, String styleId) {
		org.docx4j.wml.Style s = d.mdp().getStyleDefinitionsPart().getStyleById(styleId);
		if (s.getRPr() == null) s.setRPr(Doc.F.createRPr());
		return s.getRPr();
	}

	/** A character style based on DefaultParagraphFont with the given run properties. */
	private static void addCharacterStyle(Doc d, String styleId, java.util.function.Consumer<org.docx4j.wml.RPr> rPrCustomiser) {
		org.docx4j.wml.Style s = Doc.F.createStyle();
		s.setType("character");
		s.setStyleId(styleId);
		org.docx4j.wml.Style.Name n = Doc.F.createStyleName();
		n.setVal(styleId);
		s.setName(n);
		org.docx4j.wml.Style.BasedOn b = Doc.F.createStyleBasedOn();
		b.setVal("DefaultParagraphFont");
		s.setBasedOn(b);
		org.docx4j.wml.RPr rpr = Doc.F.createRPr();
		rPrCustomiser.accept(rpr);
		s.setRPr(rpr);
		d.mdp().getStyleDefinitionsPart().getJaxbElement().getStyle().add(s);
	}

	/** A table style stating only w:tblInd 0 (so it is not empty), optionally based on another. */
	private static void addTableStyle(Doc d, String styleId, String basedOn) {
		org.docx4j.wml.Style s = Doc.F.createStyle();
		s.setType("table");
		s.setStyleId(styleId);
		org.docx4j.wml.Style.Name n = Doc.F.createStyleName();
		n.setVal(styleId);
		s.setName(n);
		if (basedOn != null) {
			org.docx4j.wml.Style.BasedOn b = Doc.F.createStyleBasedOn();
			b.setVal(basedOn);
			s.setBasedOn(b);
		}
		org.docx4j.wml.CTTblPrBase tblPr = Doc.F.createCTTblPrBase();
		org.docx4j.wml.TblWidth ind = Doc.F.createTblWidth();
		ind.setW(BigInteger.ZERO);
		ind.setType("dxa");
		tblPr.setTblInd(ind);
		s.setTblPr(tblPr);
		d.mdp().getStyleDefinitionsPart().getJaxbElement().getStyle().add(s);
	}

	private static void tableStyle(Tbl tbl, String styleId) {
		org.docx4j.wml.CTTblPrBase.TblStyle ts = Doc.F.createCTTblPrBaseTblStyle();
		ts.setVal(styleId);
		tbl.getTblPr().setTblStyle(ts);
	}

	/** A run carrying exactly the run properties the customiser sets, and no w:rPr at all when it is null. */
	private static R bareRun(String text, java.util.function.Consumer<org.docx4j.wml.RPr> customiser) {
		R r = Doc.F.createR();
		if (customiser != null) {
			org.docx4j.wml.RPr rpr = Doc.F.createRPr();
			customiser.accept(rpr);
			r.setRPr(rpr);
		}
		org.docx4j.wml.Text t = Doc.F.createText();
		t.setValue(text);
		t.setSpace("preserve");
		r.getContent().add(t);
		return r;
	}

	private static java.util.function.Consumer<org.docx4j.wml.RPr> rStyle(String styleId) {
		return rpr -> {
			org.docx4j.wml.RStyle rs = Doc.F.createRStyle();
			rs.setVal(styleId);
			rpr.setRStyle(rs);
		};
	}

	/** One w:rPr-less run, no direct w:spacing, no list label, in the given style (none when null). */
	private static P stylesPara(Doc d, String pStyle, String text) {
		Doc.Para para = d.para().noLabel().inheritSpacing().bareText(text);
		if (pStyle != null) para.style(pStyle);
		return para.add();
	}

	/** A w:numPr stating whichever of w:numId and w:ilvl is not null. */
	private static PPrBase.NumPr numPrOf(Integer numId, Integer ilvl) {
		PPrBase.NumPr np = Doc.F.createPPrBaseNumPr();
		if (ilvl != null) {
			PPrBase.NumPr.Ilvl lvl = Doc.F.createPPrBaseNumPrIlvl();
			lvl.setVal(BigInteger.valueOf(ilvl));
			np.setIlvl(lvl);
		}
		if (numId != null) {
			PPrBase.NumPr.NumId id = Doc.F.createPPrBaseNumPrNumId();
			id.setVal(BigInteger.valueOf(numId));
			np.setNumId(id);
		}
		return np;
	}

	/** A two-level decimal list: %1. at 720/360 and %1.%2. at 1440/360. */
	private static String twoLevelAbstract(int abstractNumId) {
		return "<w:abstractNum w:abstractNumId=\"" + abstractNumId + "\"><w:multiLevelType w:val=\"multilevel\"/>"
				+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%1.\"/>"
				+ "<w:lvlJc w:val=\"left\"/><w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr></w:lvl>"
				+ "<w:lvl w:ilvl=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%1.%2.\"/>"
				+ "<w:lvlJc w:val=\"left\"/><w:pPr><w:ind w:left=\"1440\" w:hanging=\"360\"/></w:pPr></w:lvl>"
				+ "</w:abstractNum>";
	}

	private static PPrBase.NumPr numPr(int numId) {
		PPrBase.NumPr np = Doc.F.createPPrBaseNumPr();
		PPrBase.NumPr.NumId id = Doc.F.createPPrBaseNumPrNumId();
		id.setVal(BigInteger.valueOf(numId));
		np.setNumId(id);
		return np;
	}


	// ---------------------------------------------------------------- CR-016 fonts-* helpers

	private static org.docx4j.wml.RPr docDefaultsRPr(Doc d) {
		return d.mdp().getStyleDefinitionsPart().getJaxbElement().getDocDefaults().getRPrDefault().getRPr();
	}

	private static org.docx4j.wml.RFonts docDefaultsRFonts(Doc d) {
		org.docx4j.wml.RPr rpr = docDefaultsRPr(d);
		if (rpr.getRFonts() == null) rpr.setRFonts(Doc.F.createRFonts());
		return rpr.getRFonts();
	}

	/** One paragraph: a lead-in run with no w:rPr (the document default font), then the text in a run
	 *  carrying exactly what the customiser sets (no w:rPr at all when it is null). */
	private static P fontsPara(Doc d, String lead, String text, java.util.function.Consumer<org.docx4j.wml.RPr> customiser) {
		return d.para().noLabel().inheritSpacing().bareText(lead).run(bareRun(text, customiser)).add();
	}

	/** A w:rFonts stating whichever slots are not null (the rest are inherited), and the hint. */
	private static java.util.function.Consumer<org.docx4j.wml.RPr> rFonts(String ascii, String hAnsi, String cs,
			String eastAsia, org.docx4j.wml.STHint hint) {
		return rpr -> {
			org.docx4j.wml.RFonts rf = Doc.F.createRFonts();
			if (ascii != null) rf.setAscii(ascii);
			if (hAnsi != null) rf.setHAnsi(hAnsi);
			if (cs != null) rf.setCs(cs);
			if (eastAsia != null) rf.setEastAsia(eastAsia);
			if (hint != null) rf.setHint(hint);
			rpr.setRFonts(rf);
		};
	}

	private static java.util.function.Consumer<org.docx4j.wml.RPr> allFour(String font) {
		return rFonts(font, font, font, font, null);
	}

	private static org.docx4j.wml.BooleanDefaultTrue flag(boolean on) {
		org.docx4j.wml.BooleanDefaultTrue b = new org.docx4j.wml.BooleanDefaultTrue();
		if (!on) b.setVal(false);
		return b;
	}

	private static java.util.function.Consumer<org.docx4j.wml.RPr> csOn() { return rpr -> rpr.setCs(flag(true)); }
	private static java.util.function.Consumer<org.docx4j.wml.RPr> csOff() { return rpr -> rpr.setCs(flag(false)); }
	private static java.util.function.Consumer<org.docx4j.wml.RPr> rtlOn() { return rpr -> rpr.setRtl(flag(true)); }
	private static java.util.function.Consumer<org.docx4j.wml.RPr> rtlOff() { return rpr -> rpr.setRtl(flag(false)); }

	/** w:themeFontLang in the settings part. */
	private static void themeFontLang(Doc d, String val) throws Exception {
		org.docx4j.wml.CTLanguage l = Doc.F.createCTLanguage();
		l.setVal(val);
		d.mdp().getDocumentSettingsPart().getContents().setThemeFontLang(l);
	}

	/** A fontTable part holding exactly these w:font entries (the document otherwise has none). */
	private static void fontTable(Doc d, String fontEntries) throws Exception {
		org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart ftp =
				new org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart();
		ftp.setJaxbElement((org.docx4j.wml.Fonts) org.docx4j.XmlUtils.unwrap(org.docx4j.XmlUtils.unmarshalString(
				"<w:fonts xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ fontEntries + "</w:fonts>")));
		d.mdp().addTargetPart(ftp);
	}

	/** One w:font entry, as Word writes them: charset 00, variable pitch, and whichever of family, panose
	 *  and altName are given. */
	private static String fontEntry(String name, String family, String panose, String altName) {
		StringBuilder sb = new StringBuilder("<w:font w:name=\"" + name + "\">");
		if (altName != null) sb.append("<w:altName w:val=\"" + altName + "\"/>");
		if (panose != null) sb.append("<w:panose1 w:val=\"" + panose + "\"/>");
		sb.append("<w:charset w:val=\"00\"/>");
		if (family != null) sb.append("<w:family w:val=\"" + family + "\"/>");
		sb.append("<w:pitch w:val=\"variable\"/></w:font>");
		return sb.toString();
	}

	/** A w:font entry with nothing but the name - what Word writes for a font the saving
	 *  machine did not have (CR-017's authorHad: UNLIKELY). */
	private static String bareFontEntry(String name) {
		return "<w:font w:name=\"" + name + "\"/>";
	}

	/** A w:font entry as Word writes one for a font the saving machine <em>has</em>: the
	 *  family, the charset, and the w:panose1 and w:sig it reads off the font file. */
	private static String signedFontEntry(String name, String family, String panose, String sig) {
		return "<w:font w:name=\"" + name + "\">"
				+ "<w:panose1 w:val=\"" + panose + "\"/>"
				+ "<w:charset w:val=\"00\"/>"
				+ "<w:family w:val=\"" + family + "\"/>"
				+ "<w:pitch w:val=\"variable\"/>"
				+ "<w:sig " + sig + "/></w:font>";
	}

	/** A theme part whose font scheme names these Latin faces and carries Office's own per-script
	 *  list (from a Word 365 resave: what a document's theme part says for Ethi, Beng, ...). */
	private static void themePart(Doc d, String minorLatin, String majorLatin) throws Exception {
		String scripts = "<a:font script=\"Jpan\" typeface=\"游明朝\"/><a:font script=\"Hang\" typeface=\"맑은 고딕\"/>"
				+ "<a:font script=\"Hans\" typeface=\"等线\"/><a:font script=\"Hant\" typeface=\"新細明體\"/>"
				+ "<a:font script=\"Arab\" typeface=\"Arial\"/><a:font script=\"Hebr\" typeface=\"Arial\"/>"
				+ "<a:font script=\"Thai\" typeface=\"Cordia New\"/><a:font script=\"Ethi\" typeface=\"Nyala\"/>"
				+ "<a:font script=\"Beng\" typeface=\"Vrinda\"/><a:font script=\"Gujr\" typeface=\"Shruti\"/>"
				+ "<a:font script=\"Khmr\" typeface=\"DaunPenh\"/><a:font script=\"Knda\" typeface=\"Tunga\"/>"
				+ "<a:font script=\"Guru\" typeface=\"Raavi\"/><a:font script=\"Cans\" typeface=\"Euphemia\"/>"
				+ "<a:font script=\"Cher\" typeface=\"Plantagenet Cherokee\"/><a:font script=\"Yiii\" typeface=\"Microsoft Yi Baiti\"/>"
				+ "<a:font script=\"Tibt\" typeface=\"Microsoft Himalaya\"/><a:font script=\"Thaa\" typeface=\"MV Boli\"/>"
				+ "<a:font script=\"Deva\" typeface=\"Mangal\"/><a:font script=\"Telu\" typeface=\"Gautami\"/>"
				+ "<a:font script=\"Taml\" typeface=\"Latha\"/><a:font script=\"Syrc\" typeface=\"Estrangelo Edessa\"/>"
				+ "<a:font script=\"Orya\" typeface=\"Kalinga\"/><a:font script=\"Mlym\" typeface=\"Kartika\"/>"
				+ "<a:font script=\"Laoo\" typeface=\"DokChampa\"/><a:font script=\"Sinh\" typeface=\"Iskoola Pota\"/>"
				+ "<a:font script=\"Mong\" typeface=\"Mongolian Baiti\"/><a:font script=\"Viet\" typeface=\"Arial\"/>"
				+ "<a:font script=\"Uigh\" typeface=\"Microsoft Uighur\"/><a:font script=\"Geor\" typeface=\"Sylfaen\"/>"
				+ "<a:font script=\"Armn\" typeface=\"Arial\"/>";
		String fill = "<a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill>";
		String ln = "<a:ln w=\"9525\">" + fill + "</a:ln>";
		String xml = "<a:theme xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" name=\"Office\"><a:themeElements>"
				+ "<a:clrScheme name=\"Office\"><a:dk1><a:sysClr val=\"windowText\" lastClr=\"000000\"/></a:dk1>"
				+ "<a:lt1><a:sysClr val=\"window\" lastClr=\"FFFFFF\"/></a:lt1><a:dk2><a:srgbClr val=\"1F497D\"/></a:dk2>"
				+ "<a:lt2><a:srgbClr val=\"EEECE1\"/></a:lt2><a:accent1><a:srgbClr val=\"4F81BD\"/></a:accent1>"
				+ "<a:accent2><a:srgbClr val=\"C0504D\"/></a:accent2><a:accent3><a:srgbClr val=\"9BBB59\"/></a:accent3>"
				+ "<a:accent4><a:srgbClr val=\"8064A2\"/></a:accent4><a:accent5><a:srgbClr val=\"4BACC6\"/></a:accent5>"
				+ "<a:accent6><a:srgbClr val=\"F79646\"/></a:accent6><a:hlink><a:srgbClr val=\"0000FF\"/></a:hlink>"
				+ "<a:folHlink><a:srgbClr val=\"800080\"/></a:folHlink></a:clrScheme>"
				+ "<a:fontScheme name=\"Office\">"
				+ "<a:majorFont><a:latin typeface=\"" + majorLatin + "\"/><a:ea typeface=\"\"/><a:cs typeface=\"\"/>" + scripts + "</a:majorFont>"
				+ "<a:minorFont><a:latin typeface=\"" + minorLatin + "\"/><a:ea typeface=\"\"/><a:cs typeface=\"\"/>" + scripts + "</a:minorFont>"
				+ "</a:fontScheme>"
				+ "<a:fmtScheme name=\"Office\"><a:fillStyleLst>" + fill + fill + fill + "</a:fillStyleLst>"
				+ "<a:lnStyleLst>" + ln + ln + ln + "</a:lnStyleLst>"
				+ "<a:effectStyleLst><a:effectStyle><a:effectLst/></a:effectStyle><a:effectStyle><a:effectLst/></a:effectStyle>"
				+ "<a:effectStyle><a:effectLst/></a:effectStyle></a:effectStyleLst>"
				+ "<a:bgFillStyleLst>" + fill + fill + fill + "</a:bgFillStyleLst></a:fmtScheme>"
				+ "</a:themeElements></a:theme>";
		org.docx4j.openpackaging.parts.ThemePart tp = new org.docx4j.openpackaging.parts.ThemePart();
		tp.setJaxbElement((org.docx4j.dml.Theme) org.docx4j.XmlUtils.unwrap(org.docx4j.XmlUtils.unmarshalString(xml)));
		d.mdp().addTargetPart(tp);
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

	// ------------------------------------------------- CR-001 batch 43 probes

	/**
	 * A right tab stop with a dot leader at the right margin of the A4 body
	 * (595.3 - 144 = 451.3pt = 9026 twips), which is what a table-of-contents entry
	 * carries.
	 */
	private static org.docx4j.wml.Tabs dotLeaderTabs() {
		org.docx4j.wml.Tabs tabs = F.createTabs();
		org.docx4j.wml.CTTabStop stop = F.createCTTabStop();
		stop.setPos(BigInteger.valueOf(9026));
		stop.setVal(org.docx4j.wml.STTabJc.RIGHT);
		stop.setLeader(org.docx4j.wml.STTabTlc.DOT);
		tabs.getTab().add(stop);
		return tabs;
	}

	/** The w:spacing every frame-stacking entry carries: no before/after, w:line="276"
	 *  auto (1.15 lines, which is where our 14.04pt pitch at 10pt Carlito comes from:
	 *  10 x 1.2207 x 1.15). */
	private static PPrBase.Spacing frameEntrySpacing() {
		PPrBase.Spacing sp = F.createPPrBaseSpacing();
		sp.setBefore(BigInteger.ZERO);
		sp.setAfter(BigInteger.ZERO);
		sp.setLine(BigInteger.valueOf(276));
		sp.setLineRule(STLineSpacingRule.AUTO);
		return sp;
	}

	/** One block of the frame-stacking probes: a lead-in paragraph, six dot-leader entries
	 *  in {@code styleId}, and a trailing paragraph.  {@code perParagraph}, where it is
	 *  not null, writes a frame on each entry itself over whatever the style says. */
	private static void frameStackingBlock(Doc d, String tag, String styleId,
			java.util.function.Consumer<Doc.Para> perParagraph) {
		d.para(tag + ". lead-in paragraph, not framed, before the six entries. " + prose(1))
				.font(SERIF, 24).add();
		for (int i = 1; i <= 6; i++) {
			Doc.Para p = d.para().style(styleId).inheritSpacing().font(CARLITO, 20)
					.text(tag + " entry " + i + " of six, and a dot leader follows this text")
					.tab().text(String.valueOf(i + 2));
			if (perParagraph != null) perParagraph.accept(p);
			p.add();
		}
		d.para(tag + ". trailing paragraph, not framed, after the six entries. " + prose(1, 3))
				.font(SERIF, 24).add();
	}

	/** The two entry styles both frame-stacking probes use: {@code ProbeFrameEntry} carries
	 *  the style-level frame, {@code ProbeFlowEntry} is the same style without one. */
	private static Doc frameStackingDoc() throws Exception {
		Doc d = Doc.create(15);
		d.addParagraphStyle("ProbeFrameEntry", "Normal", ppr -> {
			ppr.setSpacing(frameEntrySpacing());
			ppr.setTabs(dotLeaderTabs());
			styleFrame(ppr);
		}, Doc.font(CARLITO, 20));
		d.addParagraphStyle("ProbeFlowEntry", "Normal", ppr -> {
			ppr.setSpacing(frameEntrySpacing());
			ppr.setTabs(dotLeaderTabs());
		}, Doc.font(CARLITO, 20));
		return d;
	}

	// ------------------------------- CR-001 batch 43: the East Asian line box

	/** The Latin sentence every fonts-cjk-linebox case sets, long enough to wrap to about
	 *  five lines at 10pt in the A4 text column, and the same in every case so the pitch
	 *  and the wrap are comparable across the faces. */
	private static final String CJK_LATIN_TEXT = longProse(6, 0);

	/** A short Japanese string, for the case which has to have East Asian text present. */
	private static final String CJK_WORD = "日本語のテキスト";

	/** The eight faces: seven East Asian, then Calibri as the control Word draws itself.
	 *  These are Windows/Office faces, so unlike the rest of this corpus they are not
	 *  installed on the Linux side - our render substitutes, and the question is Word's. */
	private static final String[] CJK_FACES = {
		"Meiryo", "Yu Gothic", "MS Gothic", "MS Mincho", "SimSun",
		"Malgun Gothic", "Microsoft JhengHei", "Calibri"
	};

	/** The two-line Calibri paragraph which separates two cases, so each case's own
	 *  baseline pitch can be read without its neighbours. */
	private static void cjkSeparator(Doc d, int n) {
		d.para("--- separator " + n + ", Calibri 10pt. " + prose(2, n))
				.noLabel().font("Calibri", 20).line(240, STLineSpacingRule.AUTO).add();
	}

	/** The style-level frame of a corpus document's table-of-contents style. */
	private static void styleFrame(PPr ppr) {
		Doc.framePr(ppr, 180, "around", "text", "text", "center", 1);
		Doc.suppressOverlap(ppr);
	}

	/** The frame that document's TOC paragraphs each write over that style: no hSpace,
	 *  wrap auto, anchored to the margin, left, inline vertically, and suppressOverlap
	 *  turned off.  Note there is no w:hAnchor and no w:y. */
	private static void paragraphFrame(Doc.Para p) {
		p.framePr(0, "auto", null, "margin", "left", null, "inline").suppressOverlap(false);
	}

	/** Its vertical half alone: wrap auto, vAnchor margin, yAlign inline. */
	private static void paragraphFrameVerticalOnly(Doc.Para p) {
		p.framePr(null, "auto", null, "margin", null, null, "inline");
	}

	/** A paragraph whose only content is one {@code w:br w:type="page"}, with a paragraph
	 *  mark of the given size in half-points and no space before or after. */
	private static P breakOnlyParagraph(int markHalfPts) {
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
		markSize(brk, markHalfPts);
		return brk;
	}

	/** The paragraph whose space-before is in question: 24pt of {@code w:before} and a
	 *  1pt {@code w:pBdr} with {@code w:space="1"}, the shape three corpus documents
	 *  carry on the heading that opens a page. */
	private static void spaceBeforeHeading(Doc d, String tag, String where) {
		d.para(tag + ". heading, w:before=24pt and a 1pt w:pBdr: " + where)
				.font(SERIF, 24).before(480).borders(8, 1).add();
	}

	/** n paragraphs of one exact 24pt line each, to fill a page to a known remainder. */
	private static void exactLines(Doc d, String tag, int n, String firstText) {
		for (int i = 0; i < n; i++) {
			d.para(i == 0 ? tag + ". " + firstText : tag + " line " + (i + 1) + " of " + n)
					.noLabel().font(SERIF, 24).line(480, STLineSpacingRule.EXACT).add();
		}
	}

	private static WordprocessingMLPackage pageTopSpaceBefore(int mode, Boolean suppressSpBfAfterPgBrk)
			throws Exception {
		Doc d = Doc.create(mode);
		if (suppressSpBfAfterPgBrk != null) {
			d.compat("suppressSpBfAfterPgBrk", suppressSpBfAfterPgBrk.booleanValue());
		}
		// A: the control - the same heading mid-page, where the space-before certainly applies
		d.para("A. the paragraph before the mid-page control heading. " + prose(2)).font(SERIF, 24).add();
		spaceBeforeHeading(d, "A", "mid-page, the control");
		d.para("A after. " + prose(2, 1)).font(SERIF, 24).add();

		// B: a page top reached by an explicit w:br w:type="page"
		d.pageBreak();
		spaceBeforeHeading(d, "B", "first on a page opened by an explicit page break");
		d.para("B after. " + prose(2, 2)).font(SERIF, 24).add();

		// C: a page top reached by flow - 29 exact 24pt lines leave 1.9pt of the
		// A4 body's 697.9, so the heading cannot fit and starts the next page itself
		d.pageBreak();
		exactLines(d, "C", 29, "29 exact 24pt lines fill this page to 1.9pt of its foot");
		spaceBeforeHeading(d, "C", "first on a page reached by flow, no break before it");
		d.para("C after. " + prose(2, 3)).font(SERIF, 24).add();

		// D and E: the break-only paragraph's own line, at a page filled to 25.9pt of
		// its foot, with an 11pt mark (fits in the remainder) and a 28pt one (does not)
		for (int c = 0; c < 2; c++) {
			String tag = c == 0 ? "D" : "E";
			int mark = c == 0 ? 22 : 56;
			d.pageBreak();
			exactLines(d, tag, 28, "28 exact 24pt lines, then a break-only paragraph with a "
					+ (mark / 2) + "pt mark");
			d.add(breakOnlyParagraph(mark));
			d.para(tag + " after: a plain paragraph, no space-before. " + prose(1, c))
					.noLabel().font(SERIF, 24).add();
		}

		// F: both at once - the page filled to 25.9pt, an 11pt-mark break-only
		// paragraph, and then the heading, which is the shape the corpus carries
		d.pageBreak();
		exactLines(d, "F", 28, "28 exact 24pt lines, then an 11pt-mark break-only paragraph "
				+ "and then the heading");
		d.add(breakOnlyParagraph(22));
		spaceBeforeHeading(d, "F", "after a break-only paragraph at a filled page");
		d.para("F after. " + prose(2, 5)).font(SERIF, 24).add();
		return d.pkg();
	}

	// --------------------------- CR-001 batch 44: the justification crossover

	/**
	 * The justification-crossover cases: id, spaces on the first line, the fraction of a
	 * nominal word space aimed at, the fraction the case actually comes to, the first
	 * line's glyph width in points, the first line, and the word after it.
	 *
	 * <p>Every number was computed with {@code org.docx4j.fonts.TextMeasurer} on Carlito
	 * at 11pt, which is the face and the size the reference VM draws itself, so they are
	 * Word's own advances: nominal space 2.4860pt, measure 468.0pt, and
	 * {@code fraction = (468 - g1 - next) / ((spaces + 1) x 2.4860)}.  The words which
	 * are not ordinary English are built letter by letter to a wanted advance, which is
	 * why they are nonsense; no sequence any font ligates (ff, fi, fl) occurs anywhere.
	 * For C60, C120 and N the fourth column is the <em>stretch</em> the line takes when
	 * the next word - 70.048pt, beyond any compression - stays where it is.
	 */
	private static final String[][] JUST_CASES = {
		{ "A5", "12", "0.95", "0.9511", "394.262", "known output engine before margin further width column narrow never printed shorter limem", "limomom" },
		{ "A10", "12", "0.90", "0.9014", "392.865", "known engine output before margin further width narrow shorter never printed column lilewo", "rilibemum" },
		{ "A15", "12", "0.85", "0.8497", "391.523", "engine known before output margin further width narrow shorter never printed space lililililile", "rilimemum" },
		{ "A20", "12", "0.80", "0.8010", "390.104", "engine before known output margin width narrow further never printed shorter space tigomo", "lumumumo" },
		{ "A25", "12", "0.75", "0.7500", "403.766", "margin output further known narrow engine before shorter printed width handed column lilicum", "lililawem" },
		{ "A30", "12", "0.70", "0.7010", "402.347", "margin output further engine known narrow before shorter printed width column spacing womo", "limomom" },
		{ "A35", "12", "0.65", "0.6492", "401.016", "output margin known further engine before narrow shorter width handed printed never gumum", "rilibemum" },
		{ "A40", "12", "0.60", "0.5999", "399.597", "output margin known engine further before narrow shorter width printed column never sumum", "rilimemum" },
		{ "A50", "12", "0.50", "0.5015", "399.784", "output margin known engine further before narrow shorter width column printed never lilitamo", "lumumumo" },
		{ "B5", "5", "0.95", "0.9502", "413.831", "accommodated consideration characteristically typographical internationalization lililililisumom", "lililawem" },
		{ "B10", "5", "0.90", "0.9000", "411.576", "accommodated consideration characteristically typographical internationalization lilililililibomo", "limomom" },
		{ "B15", "5", "0.85", "0.8499", "409.321", "accommodated consideration characteristically typographical responsibility lisumumumumum", "rilibemum" },
		{ "B20", "5", "0.80", "0.8005", "407.044", "accommodated consideration characteristically typographical responsibility wamemumumum", "rilimemum" },
		{ "B25", "5", "0.75", "0.7496", "404.811", "accommodated consideration characteristically typographical responsibility limemomomomo", "lumumumo" },
		{ "B30", "5", "0.70", "0.7002", "417.560", "accommodated characteristically consideration typographical internationalization liwumumumu", "lililawem" },
		{ "B35", "5", "0.65", "0.6493", "415.316", "accommodated characteristically consideration typographical internationalization rililimumumu", "limomom" },
		{ "B40", "5", "0.60", "0.5999", "413.050", "accommodated consideration characteristically typographical internationalization lililililiremum", "rilibemum" },
		{ "B50", "5", "0.50", "0.4996", "411.532", "accommodated consideration characteristically typographical internationalization liwemomom", "rilimemum" },
		{ "C60", "12", "1.60", "1.6012", "420.233", "narrow shorter further printed margin column output handed spacing known amount engine temom", "lililamemumum" },
		{ "C120", "12", "2.20", "2.2008", "402.347", "margin output further engine known narrow before shorter printed width column spacing womo", "lililamemumum" },
		{ "N", "12", "1.05", "1.0499", "436.678", "spacing handed column printed amount shorter narrow decided further adjusted before margin lawomo", "lililamemumum" },
	};

	// ------------------------ CR-001 batch 44 step 4: the list label's baseline (M10)

	/** Enough text that every list-label-baseline item runs to two lines, so the item's
	 *  first-line pitch and its second's can be read apart. */
	private static final String LABEL_ITEM_TEXT = longProse(1, 0);

	/** The Symbol face's bullet, which is where Word draws a w:numFmt="bullet" label from
	 *  unless the level says otherwise: U+F0B7, in the Private Use Area, written as a code
	 *  point because the character itself does not survive a source file. */
	private static final String SYMBOL_BULLET = String.valueOf((char) 0xF0B7);

	/** A bullet in a face both machines have, so its box can be compared with Symbol's. */
	private static final String PLAIN_BULLET = String.valueOf((char) 0x2022);

	/**
	 * The list-label-baseline cases: the w:abstractNum / w:num id, the level's
	 * {@code w:numFmt}, its {@code w:lvlText}, the level's own {@code w:rPr} (empty where
	 * the case is about not having one), the lead-in line, and the case letter.
	 */
	private static final String[][] LABEL_CASES = {
		{ "50", "decimal", "%1.", "",
			"(A) the label in the very font and size of the text: the level carries no w:rPr, "
			+ "so the label is Carlito 11pt like the item", "A" },
		{ "51", "decimal", "%1.",
			"<w:rPr><w:rFonts w:ascii=\"Liberation Serif\" w:hAnsi=\"Liberation Serif\"/></w:rPr>",
			"(B) the level's w:rPr names Liberation Serif, the same size: a different ascent, "
			+ "the same 11pt", "B" },
		{ "52", "decimal", "%1.", "<w:rPr><w:sz w:val=\"32\"/><w:szCs w:val=\"32\"/></w:rPr>",
			"(C) the level's own w:rPr w:sz=\"32\": a 16pt label on 11pt text", "C" },
		{ "53", "decimal", "%1.", "<w:rPr><w:sz w:val=\"16\"/><w:szCs w:val=\"16\"/></w:rPr>",
			"(D) the level's own w:rPr w:sz=\"16\": an 8pt label on 11pt text", "D" },
		{ "54", "bullet", SYMBOL_BULLET,
			"<w:rPr><w:rFonts w:ascii=\"Symbol\" w:hAnsi=\"Symbol\" w:hint=\"default\"/></w:rPr>",
			"(E) a Symbol bullet, the corpus's own shape: w:numFmt=\"bullet\" and the level's "
			+ "w:rFonts naming Symbol", "E" },
		{ "55", "bullet", PLAIN_BULLET,
			"<w:rPr><w:rFonts w:ascii=\"DejaVu Sans\" w:hAnsi=\"DejaVu Sans\"/></w:rPr>",
			"(F) the same bullet in DejaVu Sans, which both machines have, so the label's box "
			+ "can be compared with Symbol's", "F" },
	};

	/**
	 * The justification-crossover-2 cases: the same columns as {@link #JUST_CASES}, at a
	 * 2% grid through the band the first probe left open.
	 *
	 * <p>The first probe put Word's floor between 0.8010 (accepted) and 0.7500 (refused)
	 * and ours in the same band, so the two are not yet separated - and the corpus
	 * document the item came from shows Word accepting a space of 0.749 x nominal by the
	 * same ink-gap reading the probe uses, which is inside the band.  P is 12 spaces and
	 * Q is 5, as before; R is about 20, where the same fraction costs the line 1.6 times
	 * what it costs 12 spaces, so that "per space" can be tested over a 4:1 range rather
	 * than 2.4:1.
	 */
	private static final String[][] JUST_CASES_2 = {
		{ "P78x12", "12", "0.78", "0.7803", "399.784", "output margin known engine further before narrow shorter width printed column never lilitamo", "limomom" },
		{ "P76x12", "12", "0.76", "0.7612", "397.397", "output known margin engine before further narrow width column shorter never spacing ligomo", "rilibemum" },
		{ "P74x12", "12", "0.74", "0.7398", "395.076", "output known engine margin before further width handed narrow never column shorter wemu", "rilimemum" },
		{ "P72x12", "12", "0.72", "0.7214", "392.678", "engine known output before margin further width narrow shorter never printed column rilililili", "lumumumo" },
		{ "Q78x5", "5", "0.78", "0.7806", "416.361", "accommodated characteristically consideration typographical internationalization liliwamomom", "lililawem" },
		{ "Q76x5", "5", "0.76", "0.7607", "413.655", "accommodated consideration characteristically typographical internationalization lilicumomom", "limomom" },
		{ "Q74x5", "5", "0.74", "0.7400", "410.960", "accommodated consideration characteristically typographical internationalization lilililililigumo", "rilibemum" },
		{ "Q72x5", "5", "0.72", "0.7201", "408.243", "accommodated consideration characteristically typographical responsibility litamumumomom", "rilimemum" },
		{ "R78x20", "20", "0.78", "0.7797", "375.287", "text that two may one and with line right see use each run after but the page set must we lewu", "lumumumo" },
		{ "R76x20", "20", "0.76", "0.7586", "388.399", "that may text with two one right and each line after use page run see must but word the given lire", "lililawem" },
		{ "R74x20", "20", "0.74", "0.7401", "386.364", "that may text two with one right and line each use after run page see must but word the set litam", "limomom" },
	};

	/** What follows the next word, so that every case is two lines and the second is the
	 *  paragraph's last - which w:jc="both" does not justify. */
	private static final String JUST_TAIL = "and the rest of this case sits on the second line.";

	static {
		/*
		 * Framed-paragraph stacking.  A corpus document's table-of-contents style carries
		 * w:framePr together with w:suppressOverlap, so every one of its body paragraphs
		 * is a text frame; Word's baseline pitch between consecutive entries is 22.3-22.6pt
		 * at 10pt where ours is 14.04 (= 10pt x Carlito's 1.2207 x the style's w:line="276"
		 * auto, i.e. single spacing at 1.15 lines and no frame at all), and the document
		 * runs to 87 Word pages against our 62.  Whether consecutive paragraphs carrying
		 * the same w:framePr are ONE frame - which is what the exporter's WordLayoutFixups
		 * assumes, per its comment - or a stack of frames each positioned by its own w:y
		 * against the one before, is what decides that pitch, and only Word can say.
		 * A is the frame in the style, B the same entries with no frame (the control, which
		 * must give 14.04), C the same w:framePr and w:suppressOverlap written on each
		 * paragraph instead of on the style.
		 */
		PROBES.add(new Probe("frame-stacking",
				"six consecutive dot-leader entries, 10pt Carlito, w:spacing w:line=\"276\" auto: "
				+ "(A) in a style carrying w:framePr w:hSpace=\"180\" w:wrap=\"around\" "
				+ "w:hAnchor=\"text\" w:vAnchor=\"text\" w:xAlign=\"center\" w:y=\"1\" and "
				+ "w:suppressOverlap; (B) in the same style without the frame, the control; "
				+ "(C) with that w:framePr and w:suppressOverlap on each paragraph rather than "
				+ "on the style - what is Word's baseline pitch between consecutive framed "
				+ "paragraphs, and are six same-frame paragraphs one frame or six?", () -> {
			Doc d = frameStackingDoc();
			frameStackingBlock(d, "A", "ProbeFrameEntry", null);
			d.pageBreak();
			frameStackingBlock(d, "B", "ProbeFlowEntry", null);
			d.pageBreak();
			frameStackingBlock(d, "C", "ProbeFlowEntry", p -> {
				p.framePr(180, "around", "text", "text", "center", 1).suppressOverlap();
			});
			return d.pkg();
		}));

		/*
		 * Where an East Asian font's line box comes from.  frame-stacking and
		 * frame-stacking-2 between them ruled out w:framePr as the cause of a corpus
		 * document's 22.4pt pitch at 10pt: Word's pitch is 14.04 for every frame shape
		 * those two probes ask about, identical to the unframed control, so no form of
		 * w:framePr changes the pitch.  What is left is the font.  That document's entries
		 * are Meiryo at 10pt with w:line="276" auto and Word's pitch is 22.44 = 1.95 em;
		 * its other entries are Yu Gothic at the same spacing and Word's pitch is 19.2 =
		 * 1.67 em.  Meiryo's usWin line box is 1.5000 and Yu Gothic's 1.287, so both
		 * measured pitches are 1.30 x the usWin box on two independent fonts - which
		 * suggests Word gives an East Asian font a line box larger than usWin.  That is a
		 * hypothesis to measure, not a rule, and it wants more than two faces before any
		 * formula is believed.
		 *
		 * (a) and (b) separate the line multiplier from the box: single against the 1.15
		 * the corpus document uses.  (a) states w:line="240" auto explicitly rather than
		 * omitting w:line, because this corpus's docDefaults already carry w:line="276"
		 * auto - an omitted w:line would inherit that and (a) would be (b).  (c) separates
		 * "the face is in the Latin slot" from "East Asian text is present", which is the
		 * other way round Word could be choosing the box.
		 */
		PROBES.add(new Probe("fonts-cjk-linebox",
				"where an East Asian font's line box comes from: for each of Meiryo, Yu Gothic, "
				+ "MS Gothic, MS Mincho, SimSun, Malgun Gothic, Microsoft JhengHei and Calibri "
				+ "(the control), one paragraph of about five wrapped lines of the same Latin "
				+ "sentence with the face in w:rFonts ascii/hAnsi/eastAsia/cs at 10pt, (a) at "
				+ "w:spacing w:line=\"240\" w:lineRule=\"auto\" (single - stated, because this "
				+ "corpus's docDefaults carry 276) and (b) at w:line=\"276\" auto; then, for "
				+ "Meiryo and Yu Gothic only, (c) the same with Calibri in ascii/hAnsi and the "
				+ "face in w:eastAsia alone, with a short Japanese string in the run, so that "
				+ "\"the face is in the Latin slot\" and \"East Asian text is present\" are "
				+ "separated.  A two-line Calibri paragraph between cases.  These are Windows "
				+ "faces: the Linux side substitutes, and the question is Word's pitch", () -> {
			Doc d = Doc.create(15);
			int sep = 0;
			for (String face : CJK_FACES) {
				d.para("(a) " + face + " 10pt, single (w:line=240 auto). " + CJK_LATIN_TEXT)
						.noLabel().font(face, 20).line(240, STLineSpacingRule.AUTO).add();
				cjkSeparator(d, ++sep);
				d.para("(b) " + face + " 10pt, w:line=276 auto. " + CJK_LATIN_TEXT)
						.noLabel().font(face, 20).line(276, STLineSpacingRule.AUTO).add();
				cjkSeparator(d, ++sep);
			}
			for (final String face : new String[] { "Meiryo", "Yu Gothic" }) {
				d.para().noLabel().font("Calibri", 20).line(276, STLineSpacingRule.AUTO)
						.run("(c) Calibri in ascii/hAnsi, " + face + " in w:eastAsia, w:line=276 "
								+ "auto, East Asian text present: " + CJK_WORD + " " + CJK_LATIN_TEXT,
								"Calibri", 20, rpr -> rpr.getRFonts().setEastAsia(face))
						.add();
				cjkSeparator(d, ++sep);
			}
			return d.pkg();
		}));

		/*
		 * frame-stacking answered its own question and refuted the reading behind it:
		 * Word's pitch between six paragraphs sharing the style's w:framePr is 14.04,
		 * line for line identical to the unframed control, so consecutive same-frame
		 * paragraphs are ONE frame drawn in the flow and that style-level frame is not
		 * where the corpus document's 22.4pt pitch comes from.  What its TOC paragraphs
		 * actually carry is a frame of their own OVER the style's - w:framePr w:hSpace="0"
		 * w:wrap="auto" w:vAnchor="margin" w:xAlign="left" w:yAlign="inline" with no
		 * w:hAnchor and no w:y, and w:suppressOverlap w:val="false" - and nothing at all
		 * between consecutive entries.  D is that merge (the paragraph frame over a style
		 * frame), E the paragraph frame alone, F its vertical half alone (wrap auto,
		 * vAnchor margin, yAlign inline), and N repeats the unframed control so the pitch
		 * can be read off this one document.
		 */
		PROBES.add(new Probe("frame-stacking-2",
				"the same six dot-leader entries, 10pt Carlito, w:line=\"276\" auto, with the "
				+ "frame a corpus document's TOC paragraphs write over their style: (D) "
				+ "w:framePr w:hSpace=\"0\" w:wrap=\"auto\" w:vAnchor=\"margin\" w:xAlign=\"left\" "
				+ "w:yAlign=\"inline\" and w:suppressOverlap w:val=\"false\" on each paragraph, "
				+ "in a style which itself carries w:framePr w:hSpace=\"180\" w:wrap=\"around\" "
				+ "w:hAnchor=\"text\" w:vAnchor=\"text\" w:xAlign=\"center\" w:y=\"1\" and "
				+ "w:suppressOverlap - the merge Word computes; (E) the same paragraph frame "
				+ "where the style carries none; (F) only w:framePr w:wrap=\"auto\" "
				+ "w:vAnchor=\"margin\" w:yAlign=\"inline\" per paragraph; (N) no frame "
				+ "anywhere, the control repeated from frame-stacking's B (pitch 14.04)", () -> {
			Doc d = frameStackingDoc();
			frameStackingBlock(d, "D", "ProbeFrameEntry", Corpus::paragraphFrame);
			d.pageBreak();
			frameStackingBlock(d, "E", "ProbeFlowEntry", Corpus::paragraphFrame);
			d.pageBreak();
			frameStackingBlock(d, "F", "ProbeFlowEntry", Corpus::paragraphFrameVerticalOnly);
			d.pageBreak();
			frameStackingBlock(d, "N", "ProbeFlowEntry", null);
			return d.pkg();
		}));

		/*
		 * Space-before at a page top, in both directions, together with the break-only
		 * paragraph's own line.  Three corpus documents in modes 12 and 14, none of which
		 * states w:suppressSpBfAfterPgBrk, show Word SUPPRESSING a paragraph's w:before at
		 * a page top the document reached by an explicit w:br w:type="page"; a fourth, in
		 * mode 15 with no page break anywhere, shows Word APPLYING it at a page top reached
		 * by flow.  We do the opposite in both, and one of the four also shows the
		 * break-only paragraph taking a line Word does not give it - the same paragraph
		 * batch 39's PP_PDF_PAGEBREAK_PARAGRAPH_LINE gives a line to, seen from the other
		 * side, which is why D and E repeat that probe's A/B here: both sides of the one
		 * paragraph have to be read off one document.  A is the mid-page control, B the
		 * break-reached page top, C the flow-reached page top, D and E the break-only
		 * paragraph's line with an 11pt and a 28pt mark, F both rules at once.
		 */
		String pageTopDesc =
				"space-before at a page top and the break-only paragraph's own line: (A) a "
				+ "heading with w:spacing w:before=\"480\" and a 1pt w:pBdr mid-page, the "
				+ "control; (B) the same heading first on a page opened by an explicit "
				+ "w:br w:type=\"page\"; (C) the same heading first on a page reached by flow "
				+ "(29 exact 24pt lines fill the page before it to 1.9pt); (D, E) a page "
				+ "filled to 25.9pt of its foot then a break-only paragraph whose mark is "
				+ "11pt (fits the remainder) or 28pt (does not); (F) both at once - a filled "
				+ "page, an 11pt-mark break-only paragraph, then the heading";
		PROBES.add(new Probe("page-top-space-before",
				pageTopDesc + ", mode 15, w:suppressSpBfAfterPgBrk absent",
				() -> pageTopSpaceBefore(15, null)));
		PROBES.add(new Probe("page-top-space-before-compat14",
				pageTopDesc + ", mode 14, w:suppressSpBfAfterPgBrk absent",
				() -> pageTopSpaceBefore(14, null)));
		PROBES.add(new Probe("page-top-space-before-compat12",
				pageTopDesc + ", mode 12, w:suppressSpBfAfterPgBrk absent",
				() -> pageTopSpaceBefore(12, null)));
		PROBES.add(new Probe("page-top-space-before-set",
				pageTopDesc + ", mode 15, with w:suppressSpBfAfterPgBrk stated - what the flag "
				+ "itself does, which is what a rule gated on it has to be gated against",
				() -> pageTopSpaceBefore(15, Boolean.TRUE)));

		/*
		 * The justification crossover: how far Word will compress a line's word spaces to
		 * bring the next word up, and whether it will refuse a stretch.
		 *
		 * Word compresses where FOP stretches.  The one corpus measurement (document 2331)
		 * is a single point: Word set 15 words with 14 spaces
		 * compressed to 2.29pt = 0.749 x nominal rather than push a 37.5pt word over,
		 * where we set 14 words with 13 spaces at 5.33pt = 1.744 x nominal.  A rule needs
		 * two numbers only Word can give - the smallest fraction of a nominal space Word
		 * will accept, and whether the stretch side has a limit of its own - so every case
		 * here is built to need one stated fraction and nothing else.
		 *
		 * Each case is one w:jc="both" paragraph of two lines, Carlito 11pt on a 468pt
		 * measure (US Letter, 1 inch margins), preceded by an unjustified marker line
		 * naming it.  Carlito is the face the reference VM draws itself - batch 42's
		 * calibration - so the advances below are Word's own.  Nominal space = 2.4860pt;
		 * no sequence any font ligates (ff, fi, fl) appears in any case, and every
		 * paragraph carries w:suppressAutoHyphens.
		 *
		 * A case's first line is S+1 words of glyph width g1, and its next word has an
		 * advance chosen so that pulling that word up needs each of the S+1 spaces at
		 * exactly the stated fraction of nominal: fraction = (468 - g1 - next) / ((S+1) x
		 * 2.4860).  The words that are not in the pool are built letter by letter with
		 * TextMeasurer on Carlito at 11pt to hit that advance, which is why they are
		 * nonsense; the fractions below are what they actually come to, computed the same
		 * way, and every one is within 0.0012 of its target.
		 *
		 * A5..A50 have S = 12 spaces, B5..B50 have S = 5, because the per-space shrink is
		 * what Word is expected to limit: the same fraction costs 12 spaces 4.5 times what
		 * it costs 5.  What a case gives up if Word does NOT pull the word up is the hole
		 * that word leaves, spread over the spaces that remain: 2.15 to 2.61 x nominal in
		 * the A cases and 4.06 to 5.08 x in the B cases, which is inherent - a word which
		 * nearly fits leaves nearly its own width behind - and is why A and B together say
		 * whether Word is weighing the shrink against that alternative or applying a floor
		 * to the shrink alone.  The A series is the discriminating one; if Word compresses
		 * throughout B it has told us it weighs.
		 *
		 * C60 and C120 ask the stretch question: their next word is 70.05pt and cannot be
		 * brought up at any compression (it would need a negative space), so Word must
		 * justify the line as it stands, stretching its 12 spaces to 3.98pt (1.60 x
		 * nominal) and 5.47pt (2.20 x).  If Word ever refuses a stretch - hyphenating, or
		 * compressing to take the word after all - these are where it shows.  N is the
		 * control: the same 70.05pt word, but the line needs only 1.05 x nominal, so no
		 * decision arises.
		 *
		 * What to read off Word's PDF, per case: whether the next word is on line 1, and
		 * the pen advance between the words of line 1 (mutool stext), which is the space
		 * width Word chose.  The smallest fraction among the cases whose word came up is
		 * the crossover; the largest stretch Word sets without balking is the stretch
		 * limit.
		 */
		PROBES.add(new Probe("justification-crossover",
				"how far Word compresses a justified line's word spaces to bring the next "
				+ "word up, and whether it refuses a stretch: 21 two-line w:jc=\"both\" "
				+ "paragraphs, Carlito 11pt on a 468pt measure (US Letter, 1in margins), "
				+ "w:suppressAutoHyphens, nominal space 2.4860pt.  A5..A50 have 12 spaces on "
				+ "the first line and B5..B50 have 5; in each, the next word's advance is "
				+ "built so that pulling it up needs every space at 0.95, 0.90, 0.85, 0.80, "
				+ "0.75, 0.70, 0.65, 0.60 or 0.50 of nominal and nothing else (the fraction "
				+ "Word was measured accepting on a corpus document is 0.749).  C60 and C120 "
				+ "put a 70.05pt word beyond any compression so the line must be justified "
				+ "by stretching its spaces to 1.60 and 2.20 x nominal - does Word ever "
				+ "refuse a stretch?  N is the control, 1.05 x, no decision to make.  Read "
				+ "off each case: did the next word come up, and what space width did Word set",
				() -> {
			Doc d = Doc.create(15);
			// US Letter, 1 inch margins: a measure of exactly 468.0pt
			d.pageGeometry(12240, 15840, false, 1440, 1440, 1440, 1440);
			for (String[] c : JUST_CASES) {
				boolean shrink = c[0].charAt(0) == 'A' || c[0].charAt(0) == 'B';
				d.para("case " + c[0] + ": " + c[1] + " spaces, first line " + c[4] + "pt, "
						+ (shrink ? "bringing \"" + c[6] + "\" up needs each of " + (Integer.parseInt(c[1]) + 1)
								+ " spaces at " + c[3] + " x nominal"
								: "\"" + c[6] + "\" cannot come up; the line stretches its " + c[1]
										+ " spaces to " + c[3] + " x nominal"))
						.noLabel().font(SANS, 16).add();
				d.para(c[5] + " " + c[6] + " " + JUST_TAIL)
						.noLabel().font(CARLITO, 22).jc(JcEnumeration.BOTH)
						.suppressAutoHyphens().add();
			}
			return d.pkg();
		}));

		/*
		 * M10: where a list label's baseline sits against the first line of the item it
		 * labels, and what the label's own font and size do to it.
		 *
		 * Measured over four corpus documents (their goldens and our b67-batch43 renders):
		 * Word puts the label on the item's first-line baseline in 100%, 98%, 95% and 100%
		 * of the labels it paints, and where we do not the offset is the label font's own
		 * ascent showing through - 4.15pt above the text in one document (a bullet
		 * substituted to DejaVu Serif, and also where label and text are the same face and
		 * size), 0.90pt below in another (a 10pt label on 9pt text), 1.01pt below in a
		 * third.  So the rule looks like "one baseline, whatever the label's own font
		 * says", and batch 40's rule (557730871: a taller label grows the line by what its
		 * ascent exceeds the text's, without the auto multiple) is about the line's height,
		 * not about where in it the label sits.
		 *
		 * What the corpus cannot separate is the height half from the baseline half: every
		 * corpus case has a label within a point or two of its text's size, so "the label
		 * sits on the line's baseline" and "the label sits on its own font's baseline, and
		 * the two agree because the fonts are alike" both fit.  These cases pull them
		 * apart, and they also say whether the ITEM'S FIRST LINE moves when the label is
		 * much taller - which is batch 40's rule seen from the other side.
		 *
		 * A is the control: the label in the very font and size of the text, nothing to
		 * choose.  B changes the face and not the size (Liberation Serif's ascent share is
		 * larger than Carlito's, so a label on its own metrics sits higher).  C and D
		 * change the size, up to 16pt and down to 8pt, through the level's own w:rPr w:sz -
		 * the property ledger4 names.  E is the corpus's own shape, a Symbol bullet, which
		 * Word draws from a face whose ascent is nothing like the text's; F is the same
		 * shape in a face both machines have, so the width and the box can be compared.
		 * Every item runs to two lines, and a plain paragraph of the same size sits between
		 * the cases, so the item's first-line pitch, its second line's, and the gap to the
		 * next item can all be read off one page.
		 */
		PROBES.add(new Probe("list-label-baseline",
				"where a list label's baseline sits against the first line of its item, and "
				+ "what the label's own font and size do to it: six w:numPr lists of two "
				+ "two-line items each, text Carlito 11pt throughout, label (A) in the very "
				+ "font and size of the text - no w:rPr on the level; (B) the level's w:rPr "
				+ "naming Liberation Serif at the same size; (C) the level's own w:rPr "
				+ "w:sz=\"32\" (16pt label on 11pt text); (D) w:sz=\"16\" (8pt); (E) a "
				+ "Symbol bullet, w:numFmt=\"bullet\" with the level's w:rFonts naming "
				+ "Symbol; (F) the same bullet in DejaVu Sans, which both machines have.  "
				+ "A plain Carlito 11pt paragraph separates the cases.  Read off each: the "
				+ "label's baseline against the item's first-line baseline, the item's "
				+ "first-line pitch against its second's, and the gap to the next item",
				() -> {
			Doc d = Doc.create(15);
			StringBuilder nums = new StringBuilder();
			for (String[] c : LABEL_CASES) {
				nums.append("<w:abstractNum w:abstractNumId=\"").append(c[0]).append("\">")
						.append("<w:multiLevelType w:val=\"hybridMultilevel\"/>")
						.append("<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/>")
						.append("<w:numFmt w:val=\"").append(c[1]).append("\"/>")
						.append("<w:lvlText w:val=\"").append(c[2]).append("\"/>")
						.append("<w:lvlJc w:val=\"left\"/>")
						.append("<w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr>")
						.append(c[3])
						.append("</w:lvl></w:abstractNum>")
						.append(num(Integer.parseInt(c[0]), Integer.parseInt(c[0])));
			}
			d.numberingXml(nums.toString());
			for (String[] c : LABEL_CASES) {
				d.para(c[4]).noLabel().font(SANS, 16).after(60).add();
				for (int k = 1; k <= 2; k++) {
					d.para("Item " + k + " of case " + c[5] + ". " + LABEL_ITEM_TEXT)
							.noLabel().font(CARLITO, 22).numPr(Integer.parseInt(c[0]), 0).add();
				}
				d.para("A plain Carlito 11pt paragraph, no list. " + prose(1, 4))
						.noLabel().font(CARLITO, 22).before(120).after(120).add();
			}
			return d.pkg();
		}));

		/*
		 * The justification crossover, finer.  justification-crossover put Word's floor
		 * between 0.8010 and 0.7500 of a nominal word space - it brought the next word up
		 * at 0.8010 and left it at 0.7500, for 12 spaces and for 5 alike - and docx4j made
		 * the same choice in all 21 of its cases, so the two floors are known only to lie
		 * in the same 5% band.  Word took a 5.00 x stretch rather than compress to 0.7496
		 * (case B25), so it is a floor and not a judgement weighing the alternative.
		 *
		 * This one is a 2% grid through that band: 0.78, 0.76, 0.74 and 0.72.  P is 12
		 * spaces and Q is 5, as before, and R is about 20 - the same fraction costs a
		 * 20-space line 1.6 times what it costs a 12-space one, so "the floor is per
		 * space" is tested over a 4:1 range of lines rather than 2.4:1.  Everything else
		 * is justification-crossover's: Carlito 11pt on a 468pt measure (US Letter, 1in
		 * margins), w:jc="both", w:suppressAutoHyphens, nominal space 2.4860pt, no
		 * sequence any font ligates, and every fraction computed with TextMeasurer on
		 * Carlito at 11pt and stated in the marker line.
		 */
		PROBES.add(new Probe("justification-crossover-2",
				"the justification crossover at a 2 per cent grid through the band the "
				+ "first probe left open (Word brought the word up at 0.8010 and left it at "
				+ "0.7500): eleven two-line w:jc=\"both\" paragraphs, Carlito 11pt on a "
				+ "468pt measure (US Letter, 1in margins), w:suppressAutoHyphens, nominal "
				+ "space 2.4860pt.  P78x12..P72x12 have 12 spaces on the first line, "
				+ "Q78x5..Q72x5 have 5 and R78x20..R74x20 have about 20; in each, the next "
				+ "word's advance is built so that pulling it up needs every space at 0.78, "
				+ "0.76, 0.74 or 0.72 of nominal and nothing else.  Read off each case: did "
				+ "the next word come up, and what space width did Word set",
				() -> {
			Doc d = Doc.create(15);
			// US Letter, 1 inch margins: a measure of exactly 468.0pt
			d.pageGeometry(12240, 15840, false, 1440, 1440, 1440, 1440);
			for (String[] c : JUST_CASES_2) {
				d.para("case " + c[0] + ": " + c[1] + " spaces, first line " + c[4] + "pt, "
						+ "bringing \"" + c[6] + "\" up needs each of "
						+ (Integer.parseInt(c[1]) + 1) + " spaces at " + c[3] + " x nominal")
						.noLabel().font(SANS, 16).add();
				d.para(c[5] + " " + c[6] + " " + JUST_TAIL)
						.noLabel().font(CARLITO, 22).jc(JcEnumeration.BOTH)
						.suppressAutoHyphens().add();
			}
			return d.pkg();
		}));
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
