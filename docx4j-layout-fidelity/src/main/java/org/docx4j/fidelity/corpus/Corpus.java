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
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Br;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.P;
import org.docx4j.wml.Tc;
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
	/** The face 8132 is set in; Tinos is its metric clone on this machine.
	 *  @since 17.2.0 (CR-001 batch 48, the line-box-bold probe) */
	private static final String TIMES_NEW_ROMAN = "Times New Roman";

	/** Arial's and Times New Roman's PANOSE-1, as Word writes them in fontTable.xml. */
	private static final String PANOSE_ARIAL = "020B0604020202020204";
	/** Arial's PANOSE-1 with the weight digit 03 (light) in place of 06.  @since 17.2.0 */
	private static final String PANOSE_ARIAL_LIGHT = "020B0304020202020204";
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

		// ------------------------------------- CR-032 phase 0: floating tables as Word lays them

		/*
		 * CR-032 (docs/developer/change-requests/CR-032-floating-tables-as-word-lays-them.md).
		 * The corpora's floating tables are mostly full-width text-anchored tables with
		 * empty paragraphs after them, which Word lays BEHIND the table; today docx4j leaves
		 * a table wider than 60% of the column in the flow (the empties then stack below it)
		 * and drops its tblpY.  Eight probes, one Word run, one case a page: the band of a
		 * wide table and the empties behind it, the anchor paragraph's own text, the sides of
		 * a centred float, a negative offset, overlapping pairs, a floating table in a cell, a
		 * table taller than its page, and a line break or section break on the anchor.
		 */

		// A full-width (9026 twips = the A4 text width at 72pt margins) text-anchored table,
		// followed by empty paragraphs and then text.  Three cases: a tall table with few
		// empties (the deficit), a short table with many (the surplus), and a 36pt offset.
		PROBES.add(new Probe("table-floating-wide-empties",
				"full-width text-anchored floating tables (tblpY 0, 6pt, 36pt) followed by 3 or 8 empty "
				+ "paragraphs then text, one case a page; mode 15.  Read whether the empties sit in the "
				+ "table's band (the text after resumes at the table's foot + bottomFromText) or below it, "
				+ "and what the surplus empties cost", () -> {
			Doc d = Doc.create(15);
			int[][] cases = { { 6, 0, 3 }, { 2, 120, 8 }, { 6, 720, 8 } };
			for (int c = 0; c < cases.length; c++) {
				int rows = cases[c][0], y = cases[c][1], empties = cases[c][2];
				d.para("before case " + (c + 1) + ": " + rows + " rows, tblpY " + y + ", " + empties
						+ " empty paragraphs behind. " + prose(1, c)).after(160).add();
				Doc.Table t = new Doc.Table(4513, 4513).fixedLayout()
						.floating("text", "margin", 0, null, y, null);
				for (int r = 0; r < rows; r++) {
					t.row(SERIF, 24, false, "wide row " + (r + 1), prose(1, r));
				}
				d.add(t.build());
				for (int i = 0; i < empties; i++) d.emptyParagraph();
				d.para("after the empties. " + prose(3, c + 3)).after(160).add();
				d.para(prose(3, c + 6)).after(160).add();
				if (c < cases.length - 1) d.pageBreak();
			}
			return d.pkg();
		}));

		// The same wide table with a paragraph of text as its anchor: does the anchor
		// paragraph's text go below the table, does the offset count from the paragraph's
		// top (the lines above it full width), and does a 75% table get text beside it.
		PROBES.add(new Probe("table-floating-wide-anchor-text",
				"a full-width text-anchored floating table whose anchor paragraph holds five sentences "
				+ "(tblpY 0 and 30pt), and a 75%-wide one; one case a page; mode 15.  Read where the anchor "
				+ "paragraph's text goes (above the offset, below the table, beside a 75% table)", () -> {
			Doc d = Doc.create(15);
			int[][] cases = { { 4513, 0 }, { 4513, 600 }, { 3385, 0 } };
			for (int c = 0; c < cases.length; c++) {
				int half = cases[c][0], y = cases[c][1];
				d.para("before case " + (c + 1) + ": width " + (2 * half) + ", tblpY " + y + ". " + prose(1, c)).after(160).add();
				Doc.Table t = new Doc.Table(half, half).fixedLayout()
						.floating("text", "margin", 0, null, y, null);
				t.row(SERIF, 24, false, "anchor text", "case " + (c + 1))
						.row(SERIF, 24, false, prose(1, c), "b")
						.row(SERIF, 24, false, "third", prose(1, c + 1));
				d.add(t.build());
				d.para("anchor paragraph. " + prose(5, c + 2)).after(160).add();
				d.para("the paragraph after. " + prose(3, c + 7)).after(160).add();
				if (c < cases.length - 1) d.pageBreak();
			}
			return d.pkg();
		}));

		// A 40% table with an offset of 1in: centred (room both sides), at the left edge, and
		// at tblpX 1000 (unequal room).  Which side(s) Word uses, and the lines above the
		// offset full width.
		PROBES.add(new Probe("table-floating-offset-sides",
				"40%-wide text-anchored floating tables with tblpY 1in: centred on the margin box, at its "
				+ "left edge, at tblpX 1000, and centred again with 24pt of space-before on the anchor paragraph; "
				+ "a seven-sentence anchor paragraph and two more; one case a page; mode 15.  Read which side or "
				+ "sides the text runs down, that the lines above the offset are full width, and whether the "
				+ "offset counts from the paragraph's space-before or from its first line", () -> {
			Doc d = Doc.create(15);
			// case 4 = case 1 with 24pt of space-before on the anchor paragraph: is tblpY
			// measured from the paragraph's top including its space, or from its first line?
			String[] xSpec = { "center", null, null, "center" };
			Integer[] xTw = { null, 0, 1000, null };
			for (int c = 0; c < 4; c++) {
				d.para("before case " + (c + 1) + ". " + prose(1, c)).after(160).add();
				Doc.Table t = new Doc.Table(1805, 1805).fixedLayout()
						.floating("text", "margin", xTw[c], xSpec[c], 1440, null);
				t.row(SERIF, 24, false, "sides", "case " + (c + 1)).row(SERIF, 24, false, prose(1, c), "y");
				d.add(t.build());
				Doc.Para anchor = d.para("anchor paragraph. " + prose(7, c + 1)).after(160);
				if (c == 3) anchor.before(480);
				anchor.add();
				d.para(prose(4, c + 8)).after(160).add();
				d.para(prose(4, c + 12)).after(160).add();
				if (c < 3) d.pageBreak();
			}
			return d.pkg();
		}));

		// A negative offset: mid-page over the paragraph before, and on the first paragraph
		// of a page (into the top margin, or the previous page's space?).
		PROBES.add(new Probe("table-floating-negative",
				"a full-width text-anchored floating table with tblpY -30pt, anchored to a mid-page paragraph "
				+ "and to the first paragraph of page 2; mode 15.  Read where the table sits against the "
				+ "paragraph before it, and what happens at the top of a page", () -> {
			Doc d = Doc.create(15);
			d.para("P1 the paragraph before the table. " + prose(4, 0)).after(160).add();
			Doc.Table t = new Doc.Table(4513, 4513).fixedLayout().floating("text", "margin", 0, null, -600, null);
			t.row(SERIF, 24, false, "negative", "mid-page").row(SERIF, 24, false, prose(1, 1), "b");
			d.add(t.build());
			d.para("anchor paragraph, mid-page. " + prose(4, 2)).after(160).add();
			d.para(prose(3, 6)).after(160).add();
			d.pageBreak();
			Doc.Table u = new Doc.Table(4513, 4513).fixedLayout().floating("text", "margin", 0, null, -600, null);
			u.row(SERIF, 24, false, "negative", "page top").row(SERIF, 24, false, prose(1, 3), "b");
			d.add(u.build());
			d.para("anchor paragraph, first on page 2. " + prose(4, 4)).after(160).add();
			d.para(prose(3, 8)).after(160).add();
			return d.pkg();
		}));

		// Pairs: two 44% tables on one anchor, left and right; two on the same side; and
		// 6705's shape, two full-width tables with three empties between them.
		PROBES.add(new Probe("table-floating-pair",
				"two 44%-wide text-anchored floating tables on one anchor paragraph, left and right, then "
				+ "both at the left; and two full-width ones with three empty paragraphs between (a corpus "
				+ "shape); one case a page; mode 15.  Read whether Word places them side by side, overlaps "
				+ "them, or stacks them", () -> {
			Doc d = Doc.create(15);
			// 1. left and right
			d.para("before case 1. " + prose(1, 0)).after(160).add();
			Doc.Table a = new Doc.Table(1000, 1000).fixedLayout().floating("text", "margin", 0, null, 0, null);
			a.row(SERIF, 24, false, "pair", "left").row(SERIF, 24, false, prose(1, 0), "a");
			d.add(a.build());
			Doc.Table b = new Doc.Table(1000, 1000).fixedLayout().floating("text", "margin", null, "right", 0, null);
			b.row(SERIF, 24, false, "pair", "right").row(SERIF, 24, false, prose(1, 1), "b");
			d.add(b.build());
			d.para("anchor of both. " + prose(6, 2)).after(160).add();
			d.para(prose(3, 8)).after(160).add();
			d.pageBreak();
			// 2. both at the left
			d.para("before case 2. " + prose(1, 1)).after(160).add();
			Doc.Table c = new Doc.Table(1000, 1000).fixedLayout().floating("text", "margin", 0, null, 0, null);
			c.row(SERIF, 24, false, "same side", "first").row(SERIF, 24, false, prose(1, 2), "c");
			d.add(c.build());
			Doc.Table e = new Doc.Table(1000, 1000).fixedLayout().floating("text", "margin", 0, null, 0, null);
			e.row(SERIF, 24, false, "same side", "second").row(SERIF, 24, false, prose(1, 3), "e");
			d.add(e.build());
			d.para("anchor of both. " + prose(6, 3)).after(160).add();
			d.para(prose(3, 9)).after(160).add();
			d.pageBreak();
			// 3. two full-width tables, three empties between
			d.para("before case 3. " + prose(1, 2)).after(160).add();
			Doc.Table f = new Doc.Table(4513, 4513).fixedLayout().floating("text", "margin", 0, null, 0, null);
			f.row(SERIF, 24, false, "wide", "first").row(SERIF, 24, false, prose(1, 4), "f");
			d.add(f.build());
			for (int i = 0; i < 3; i++) d.emptyParagraph();
			Doc.Table g = new Doc.Table(4513, 4513).fixedLayout().floating("text", "margin", 0, null, 0, null);
			g.row(SERIF, 24, false, "wide", "second").row(SERIF, 24, false, prose(1, 5), "g");
			d.add(g.build());
			for (int i = 0; i < 3; i++) d.emptyParagraph();
			d.para("after both. " + prose(4, 4)).after(160).add();
			return d.pkg();
		}));

		// 5936's shape: a one-cell floating table inside a cell, with six paragraphs of
		// text in the cell after it.
		PROBES.add(new Probe("table-floating-in-cell",
				"a one-cell text-anchored floating table (70.5pt wide, tblpX 0) nested in the single cell of "
				+ "a full-width table, followed in the cell by six two-sentence paragraphs; mode 15.  Read "
				+ "whether the cell's text wraps beside the nested table as it does in the body", () -> {
			Doc d = Doc.create(15);
			d.para("before the outer table. " + prose(1, 0)).after(160).add();
			Doc.Table outer = new Doc.Table(9026).fixedLayout();
			Doc.Table inner = new Doc.Table(1410).fixedLayout().floating("text", "margin", 0, null, 0, null);
			inner.row(SERIF, 24, false, "in cell");
			P[] ps = new P[6];
			for (int i = 0; i < 6; i++) ps[i] = d.para(prose(2, i + 1)).after(160).build();
			Tc tc = outer.cellOf(ps);
			tc.getContent().add(0, inner.build());
			outer.rowOf(null, null, tc);
			d.add(outer.build());
			d.para("after the outer table. " + prose(2, 7)).after(160).add();
			return d.pkg();
		}));

		// A text-anchored table taller than the room left on its page (anchored half-way
		// down), then a page-anchored one running past the page foot.
		PROBES.add(new Probe("table-floating-tall",
				"a 40-row full-width text-anchored floating table anchored half-way down page 1, and a "
				+ "40-row page-anchored one at tblpY 360pt; mode 15.  Read whether Word splits the table "
				+ "across pages (and how the text after it flows) or moves it whole", () -> {
			Doc d = Doc.create(15);
			for (int i = 0; i < 5; i++) d.para(prose(5, i)).after(160).add();
			Doc.Table t = new Doc.Table(4513, 4513).fixedLayout().floating("text", "margin", 0, null, 0, null);
			for (int r = 0; r < 40; r++) t.row(SERIF, 24, false, "tall row " + (r + 1), prose(1, r));
			d.add(t.build());
			d.para("anchor paragraph. " + prose(4, 5)).after(160).add();
			d.para(prose(4, 9)).after(160).add();
			d.pageBreak();
			d.para("page-anchored case. " + prose(2, 0)).after(160).add();
			Doc.Table u = new Doc.Table(4513, 4513).fixedLayout().floating("page", "margin", 0, null, 7200, null);
			for (int r = 0; r < 40; r++) u.row(SERIF, 24, false, "page row " + (r + 1), prose(1, r + 3));
			d.add(u.build());
			d.para("text after the page-anchored table. " + prose(4, 6)).after(160).add();
			return d.pkg();
		}));

		// The same tall text-anchored table with w:doNotBreakWrappedTables.
		PROBES.add(new Probe("table-floating-tall-nobreak",
				"the 40-row text-anchored floating table of table-floating-tall, with "
				+ "w:doNotBreakWrappedTables; mode 15.  Read whether the setting keeps the table whole", () -> {
			Doc d = Doc.create(15);
			d.compat("doNotBreakWrappedTables", true);
			for (int i = 0; i < 5; i++) d.para(prose(5, i)).after(160).add();
			Doc.Table t = new Doc.Table(4513, 4513).fixedLayout().floating("text", "margin", 0, null, 0, null);
			for (int r = 0; r < 40; r++) t.row(SERIF, 24, false, "tall row " + (r + 1), prose(1, r));
			d.add(t.build());
			d.para("anchor paragraph. " + prose(4, 5)).after(160).add();
			d.para(prose(4, 9)).after(160).add();
			return d.pkg();
		}));

		// 6293's shape: the anchor paragraph opens with a w:br; and an anchor paragraph
		// carrying the section's sectPr.
		PROBES.add(new Probe("table-floating-br-anchor",
				"a 40%-wide text-anchored floating table whose anchor paragraph opens with a w:br line break, "
				+ "and a full-width one whose anchor paragraph carries the sectPr of a continuous section; "
				+ "one case a page; mode 15.  Read where each table sits and how the text flows", () -> {
			Doc d = Doc.create(15);
			d.para("before case 1. " + prose(1, 0)).after(160).add();
			Doc.Table t = new Doc.Table(1805, 1805).fixedLayout().floating("text", "margin", 0, null, 0, null);
			t.row(SERIF, 24, false, "br anchor", "case 1").row(SERIF, 24, false, prose(1, 0), "a");
			d.add(t.build());
			d.para().noLabel().softReturn().text("anchor paragraph opening with a line break. " + prose(5, 1)).after(160).add();
			d.para(prose(3, 6)).after(160).add();
			d.pageBreak();
			d.para("before case 2. " + prose(1, 1)).after(160).add();
			Doc.Table u = new Doc.Table(4513, 4513).fixedLayout().floating("text", "margin", 0, null, 0, null);
			u.row(SERIF, 24, false, "sectPr anchor", "case 2").row(SERIF, 24, false, prose(1, 2), "b");
			d.add(u.build());
			P last = d.para("anchor paragraph carrying the section break. " + prose(3, 3)).after(160).add();
			d.endSectionOn(last, "continuous");
			d.para("first paragraph of the next section. " + prose(4, 7)).after(160).add();
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
		 * and a w:tblW in dxa which is the grid's own sum, which since 17.2.0 makes the
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
		 * CR-030's probe set (docs/developer/change-requests/CR-030-table-conditions-in-the-resolver.md,
		 * section 5).  What a table style gives the paragraphs of its table: the runs in the
		 * cells carry no w:rPr and the paragraphs no direct w:spacing, so a cell's text shows what
		 * the styles give it and nothing else; the case letters are in the text.  Every table has
		 * direct single borders and w:tblLook 0620 (first row on, everything else off), so the
		 * first row is the only region a condition can name.
		 */
		PROBES.add(new Probe("tables-normal-table-text",
				"the w:default=1 table style, named Normal Table, given w:rPr Liberation Sans 20pt and "
				+ "w:pPr w:spacing w:after 600 (30pt); docDefaults Liberation Serif 12pt.  Three tables, "
				+ "two paragraphs per cell: (a) no w:tblStyle; (b) w:tblStyle Grid2, based on the default "
				+ "table style; (c) w:tblStyle Custom, no w:basedOn - is any cell's text 20pt Sans, and "
				+ "are its paragraphs 30pt apart (does Word apply the document's Normal Table text "
				+ "properties)?", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(SERIF, 24);
			org.docx4j.wml.Style tn = d.mdp().getStyleDefinitionsPart().getDefaultTableStyle();
			styleName(tn, "Normal Table");
			Doc.font(SANS, 40).accept(rPrOf(tn));
			pPrOf(tn).setSpacing(spacingAfter(600));
			addTableStyle(d, "Grid2", tn.getStyleId());
			addTableStyle(d, "Custom", null);
			String[] cases = { "(a) no w:tblStyle", "(b) w:tblStyle Grid2, based on " + tn.getStyleId(),
					"(c) w:tblStyle Custom, no w:basedOn" };
			String[] styles = { null, "Grid2", "Custom" };
			for (int k = 0; k < 3; k++) {
				d.para(cases[k]).before(k == 0 ? 0 : 240).after(120).add();
				Tbl t = tablesProbeTable(styles[k], 1, 4500, 4500);
				String letter = cases[k].substring(0, 3);
				cellAdd(t, 0, 0, tablesPara(d, null, letter + " first paragraph"),
						tablesPara(d, null, letter + " second paragraph"));
				cellAdd(t, 0, 1, tablesPara(d, null, letter + " other cell"));
				d.add(t);
			}
			stylesPara(d, null, "(d) a body paragraph after the tables, the 12pt Serif control");
			return d.pkg();
		}));

		PROBES.add(new Probe("tables-named-normal-table",
				"which table style Word ignores, by name or by being the default: the w:default=1 table "
				+ "style renamed My Default, and a second table style OtherNormal (not the default) named "
				+ "Normal Table; both give w:rPr Liberation Sans 20pt; docDefaults Liberation Serif 12pt.  "
				+ "Tables: (a) w:tblStyle OtherNormal; (b) no w:tblStyle; (c) w:tblStyle Grid2, based on "
				+ "the default - which cells are 20pt Sans?", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(SERIF, 24);
			org.docx4j.wml.Style tn = d.mdp().getStyleDefinitionsPart().getDefaultTableStyle();
			styleName(tn, "My Default");
			Doc.font(SANS, 40).accept(rPrOf(tn));
			addTableStyle(d, "OtherNormal", null);
			org.docx4j.wml.Style other = d.mdp().getStyleDefinitionsPart().getStyleById("OtherNormal");
			styleName(other, "Normal Table");
			Doc.font(SANS, 40).accept(rPrOf(other));
			addTableStyle(d, "Grid2", tn.getStyleId());
			String[] cases = { "(a) w:tblStyle OtherNormal, named Normal Table, not the default",
					"(b) no w:tblStyle: the default table style, named My Default",
					"(c) w:tblStyle Grid2, based on the default table style" };
			String[] styles = { "OtherNormal", null, "Grid2" };
			for (int k = 0; k < 3; k++) {
				d.para(cases[k]).before(k == 0 ? 0 : 240).after(120).add();
				Tbl t = tablesProbeTable(styles[k], 1, 4500, 4500);
				String letter = cases[k].substring(0, 3);
				cellAdd(t, 0, 0, tablesPara(d, null, letter + " cell text"));
				cellAdd(t, 0, 1, tablesPara(d, null, letter + " other cell"));
				d.add(t);
			}
			stylesPara(d, null, "(d) a body paragraph after the tables, the 12pt Serif control");
			return d.pkg();
		}));

		PROBES.add(new Probe("tables-textbox-in-cell",
				"a table style giving the whole table w:sz 32 (16pt) and its firstRow condition w:b; "
				+ "docDefaults Liberation Serif 12pt.  Header row: (a) cell text, (b) a text box anchored "
				+ "in the cell (mc:AlternateContent, wps and VML alike); body row: (c) cell text, (d) a "
				+ "text box; after the table (e) a text box, the control - is the text inside (b) and (d) "
				+ "16pt, bold, or 12pt regular like (e)?", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(SERIF, 24);
			addTableStyle(d, "ProbeBox", null);
			org.docx4j.wml.Style box = d.mdp().getStyleDefinitionsPart().getStyleById("ProbeBox");
			Doc.font(SERIF, 32).accept(rPrOf(box));
			addTableStyleCondition(d, "ProbeBox", org.docx4j.wml.STTblStyleOverrideType.FIRST_ROW, Doc::bold);
			Tbl t = tablesProbeTable("ProbeBox", 2, 9000);
			cellAdd(t, 0, 0, tablesPara(d, null, "(a) header row cell text"),
					boxPara(d, "(b) the box anchored here: ", "(b) text in a box in the header row"));
			cellAdd(t, 1, 0, tablesPara(d, null, "(c) body row cell text"),
					boxPara(d, "(d) the box anchored here: ", "(d) text in a box in a body row"));
			d.add(t);
			d.add(boxPara(d, "(e) the box anchored here, outside the table: ", "(e) text in a box outside any table"));
			return d.pkg();
		}));

		PROBES.add(new Probe("tables-numbered-in-cell",
				"paragraph style ProbeListNumber numbers through its own w:numPr (w:num 95, no w:ilvl), "
				+ "and level 0 of w:num 95 names it back with w:pStyle, as Word's built-in List Number "
				+ "does.  (a), (b) in the body; (c) header row and (d) body row of a table whose style "
				+ "makes the first row bold; (e), (f) in the body; (g) in a table naming no style; (h) in "
				+ "the body - are (c) and (d) numbered, and is (h) 8. (docx4j 17.3.0: (c) and (d) "
				+ "unnumbered, (h) 6.)?", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(SERIF, 24);
			d.numberingXml("<w:abstractNum w:abstractNumId=\"95\"><w:multiLevelType w:val=\"singleLevel\"/>"
					+ "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/>"
					+ "<w:pStyle w:val=\"ProbeListNumber\"/><w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/>"
					+ "<w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr></w:lvl></w:abstractNum>"
					+ num(95, 95));
			d.addParagraphStyle("ProbeListNumber", "Normal", ppr -> ppr.setNumPr(numPrOf(95, null)));
			addTableStyle(d, "ProbeHeader", null);
			addTableStyleCondition(d, "ProbeHeader", org.docx4j.wml.STTblStyleOverrideType.FIRST_ROW, Doc::bold);
			stylesPara(d, "ProbeListNumber", "(a) body item");
			stylesPara(d, "ProbeListNumber", "(b) body item");
			Tbl t = tablesProbeTable("ProbeHeader", 2, 9000);
			cellAdd(t, 0, 0, tablesPara(d, "ProbeListNumber", "(c) item in the header row"));
			cellAdd(t, 1, 0, tablesPara(d, "ProbeListNumber", "(d) item in a body row"));
			d.add(t);
			stylesPara(d, "ProbeListNumber", "(e) body item after the table");
			stylesPara(d, "ProbeListNumber", "(f) body item");
			Tbl plain = tablesProbeTable(null, 1, 9000);
			cellAdd(plain, 0, 0, tablesPara(d, "ProbeListNumber", "(g) item in a table naming no style"));
			d.add(plain);
			stylesPara(d, "ProbeListNumber", "(h) body item, the last");
			return d.pkg();
		}));

		PROBES.add(new Probe("tables-compat-size-jc",
				"[MS-DOCX] overrideTableStyleFontSizeAndJustification OFF; Normal states Liberation Serif "
				+ "12pt and w:jc left.  Table style ProbeCompat: whole table w:sz 18 (9pt) w:jc right; "
				+ "firstRow condition w:sz 28 (14pt) w:jc center.  Header and body row, three columns: "
				+ "(a) Normal; (b) ProbeA, based on ProbeB (w:sz 20, 10pt) based on Normal, stating no "
				+ "size; (c) ProbeC, based on Normal, stating w:sz 32 (16pt) - what size and alignment "
				+ "does each cell's text take (does the 12pt-and-left exception cover the condition, and "
				+ "the inherited (b))?", () -> compatSizeJcProbe(15, "0")));

		PROBES.add(new Probe("tables-compat-size-jc-on",
				"the same as tables-compat-size-jc with overrideTableStyleFontSizeAndJustification ON "
				+ "(ECMA-376's own order): expected (a) 12pt left, (b) 10pt left, (c) 16pt left in both "
				+ "rows", () -> compatSizeJcProbe(15, "1")));

		/*
		 * CR-030's follow-up probes (section 5.2), after Word's goldens for the six above:
		 * T6, whether Word 365 applies the [MS-DOCX] 12pt-and-left exception at all below
		 * mode 15 (and in mode 15 with the setting absent); T7, what lies under a default
		 * table style not named "Normal Table"; T8, whether a table style reaches a footnote
		 * referenced from one of its cells.
		 */
		PROBES.add(new Probe("tables-compat-size-jc-mode12",
				"tables-compat-size-jc as Word 2007 writes it: no compatibilityMode and no "
				+ "overrideTableStyleFontSizeAndJustification (no w:compatSetting at all) - does Word 365 "
				+ "apply the 12pt-and-left exception: (a) 14pt centred in the header and 9pt right in the "
				+ "body, and (b), (c) centred and right; or ECMA-376's order, everything left at 12, 10 and "
				+ "16pt?", () -> compatSizeJcProbe(null, null)));

		PROBES.add(new Probe("tables-compat-size-jc-mode14",
				"tables-compat-size-jc in compatibilityMode 14, the setting absent - the same question "
				+ "as -mode12", () -> compatSizeJcProbe(14, null)));

		PROBES.add(new Probe("tables-compat-size-jc-absent",
				"tables-compat-size-jc in compatibilityMode 15 with the setting absent rather than stated "
				+ "0 (docx4j takes absent as 0) - expected as the stated 0: ECMA-376's order", () -> compatSizeJcProbe(15, null)));

		PROBES.add(new Probe("tables-renamed-default-margins",
				"the w:default=1 table style renamed My Default and given w:tblCellMar left and right 300 "
				+ "(15pt).  Tables: (a) no w:tblStyle; (b) Grid2, based on My Default; (c) Custom, no "
				+ "w:basedOn - where does each first cell's text start: 15pt in (the default applies as "
				+ "written), 5.4pt (Word's built-in Normal Table underneath), or 0?", () -> renamedDefaultProbe(true)));

		PROBES.add(new Probe("tables-renamed-default-nomargins",
				"the same, My Default stating no w:tblCellMar at all - 0 (nothing beneath it) or 5.4pt "
				+ "(the built-in beneath it)?", () -> renamedDefaultProbe(false)));

		PROBES.add(new Probe("tables-footnote-in-cell",
				"a table style giving the whole table w:i and w:sz 32, and its firstRow condition w:b.  "
				+ "Footnotes referenced from (a) a header-row cell, (b) a body-row cell and (c) a body "
				+ "paragraph after the table; each note's run states Liberation Serif 10pt - are the three "
				+ "notes alike, upright and regular, or do (a) and (b) take the italic (and (a) the bold) "
				+ "of the cell they are referenced from?", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(SERIF, 24);
			addTableStyle(d, "ProbeNotes", null);
			org.docx4j.wml.Style ts = d.mdp().getStyleDefinitionsPart().getStyleById("ProbeNotes");
			Doc.font(SERIF, 32).accept(rPrOf(ts));
			rPrOf(ts).setI(Doc.F.createBooleanDefaultTrue());
			addTableStyleCondition(d, "ProbeNotes", org.docx4j.wml.STTblStyleOverrideType.FIRST_ROW, Doc::bold);
			Tbl t = tablesProbeTable("ProbeNotes", 2, 9000);
			cellAdd(t, 0, 0, d.para().noLabel().inheritSpacing().bareText("(a) header-row cell, its note here")
					.run(d.footnoteRef("(a) the note referenced from the header row", SERIF, 20)).build());
			cellAdd(t, 1, 0, d.para().noLabel().inheritSpacing().bareText("(b) body-row cell, its note here")
					.run(d.footnoteRef("(b) the note referenced from a body row", SERIF, 20)).build());
			d.add(t);
			d.para().noLabel().inheritSpacing().bareText("(c) a body paragraph, its note here")
					.run(d.footnoteRef("(c) the note referenced from the body", SERIF, 20)).add();
			d.finishFootnotes();
			return d.pkg();
		}));

		PROBES.add(new Probe("tables-banding-merged-row",
				"a table style whose band1Horz condition gives bold text and grey shading, and whose "
				+ "firstRow gives italic; w:tblLook firstRow and row banding on, no w:cnfStyle anywhere.  "
				+ "(a) Six rows, the third's every cell continuing the second's vertical merge; (b) the "
				+ "control, that row keeping one cell of its own.  In (b) rows 2, 4 and 6 are the first "
				+ "band (bold, shaded).  In (a), are rows 4 and 6 bold and shaded (the merged row counts, as "
				+ "docx4j counts it since 17.3.1), or row 5 (it does not)?  CR-030 phase 4.", () -> {
			/* Word's golden (2026-10-04) bands NO row of either table, not even the control's:
			 * its re-save writes firstRow's w:cnfStyle on row 1 and no band bit anywhere.  The
			 * style states no w:tblStyleRowBandSize (Word's own banded styles always do), so the
			 * merged-row question is asked again by tables-banding-band-size, which also asks
			 * whether that absence is the reason. */
			Doc d = Doc.create(15);
			d.documentDefaultRun(SERIF, 24);
			addBandsProbeStyle(d, "ProbeBands", null);
			d.para("(a) row 3 continues row 2's merge in every cell").after(120).add();
			d.add(bandsProbeTable("a", "ProbeBands", true, null));
			d.para("(b) the control: row 3 keeps a cell of its own").before(240).after(120).add();
			d.add(bandsProbeTable("b", "ProbeBands", false, null));
			return d.pkg();
		}));

		PROBES.add(new Probe("tables-banding-band-size",
				"tables-banding-merged-row's question asked again, since Word banded nothing there: "
				+ "its style states no w:tblStyleRowBandSize.  ProbeBands1 is that style stating 1; "
				+ "ProbeBands states none.  (a) ProbeBands1, row 3 continuing row 2's merge in every "
				+ "cell; (b) ProbeBands1, the control; (c) ProbeBands, the control - banded at all?; (d) "
				+ "ProbeBands with the table's own w:tblStyleRowBandSize 1, the control - is the table's "
				+ "enough?  Banded rows are bold and shaded; the first row italic.  In (b) rows 2, 4 "
				+ "and 6 are the first band; in (a), rows 4 and 6 (the merged row counts) or row 5?", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(SERIF, 24);
			addBandsProbeStyle(d, "ProbeBands1", Integer.valueOf(1));
			addBandsProbeStyle(d, "ProbeBands", null);
			d.para("(a) ProbeBands1 (band size 1): row 3 continues row 2's merge in every cell").after(120).add();
			d.add(bandsProbeTable("a", "ProbeBands1", true, null));
			d.para("(b) ProbeBands1: the control").before(240).after(120).add();
			d.add(bandsProbeTable("b", "ProbeBands1", false, null));
			d.para("(c) ProbeBands (no band size): the control").before(240).after(120).add();
			d.add(bandsProbeTable("c", "ProbeBands", false, null));
			d.para("(d) ProbeBands, the table stating band size 1: the control").before(240).after(120).add();
			d.add(bandsProbeTable("d", "ProbeBands", false, Integer.valueOf(1)));
			return d.pkg();
		}));

		PROBES.add(new Probe("tables-banding-col-band-size",
				"tables-banding-band-size's question for the columns: does a style stating no "
				+ "w:tblStyleColBandSize band its columns?  band1Vert gives bold text and grey shading, "
				+ "firstCol italic; w:tblLook first column and column banding on, nothing else.  Six "
				+ "columns, two rows.  (a) ProbeColBands1, the style stating 1; (b) ProbeColBands, "
				+ "stating none; (c) ProbeColBands with the table's own w:tblStyleColBandSize 1.  In (a) "
				+ "columns 2, 4 and 6 are the first band - and in (b)?  CR-030 D11.", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(SERIF, 24);
			addBandProbeStyle(d, "ProbeColBands1", org.docx4j.wml.STTblStyleOverrideType.FIRST_COL,
					org.docx4j.wml.STTblStyleOverrideType.BAND_1_VERT, null, Integer.valueOf(1));
			addBandProbeStyle(d, "ProbeColBands", org.docx4j.wml.STTblStyleOverrideType.FIRST_COL,
					org.docx4j.wml.STTblStyleOverrideType.BAND_1_VERT, null, null);
			d.para("(a) ProbeColBands1 (column band size 1)").after(120).add();
			d.add(colBandsProbeTable("a", "ProbeColBands1", null));
			d.para("(b) ProbeColBands (no column band size)").before(240).after(120).add();
			d.add(colBandsProbeTable("b", "ProbeColBands", null));
			d.para("(c) ProbeColBands, the table stating column band size 1").before(240).after(120).add();
			d.add(colBandsProbeTable("c", "ProbeColBands", Integer.valueOf(1)));
			return d.pkg();
		}));

		// ------------------------------------------------------------- CR-001 batch 50
		//                                   ledger7's three questions for one Word run:
		//                                   rows split in cells, text box insets, and
		//                                   the justified compression decision

		/*
		 * table-rowsplit-3 (ledger7 section 3 item 1).  On corpus document 11657, at
		 * compatibility mode 11, Word breaks the page inside two- and three-line cell
		 * paragraphs, 1+1 and 1+2, which widow control forbids.  The document sets nothing
		 * to allow it: no w:cantSplit, no w:keepLines, and no w:widowControl anywhere in
		 * the package, which Word reads as on (32 corpus documents write w:val="0" to turn
		 * it off).  probes47's table-rowsplit (mode 15, one three-line paragraph per row)
		 * never divided a row.  So one of these holds:
		 *   - Word does not apply widow control inside a table cell at mode 11;
		 *   - it does not apply it at all below some mode;
		 *   - one of 11657's Word 2003 compatibility flags switches it off.
		 * The mode is a document property, so this is five documents: modes 11, 12, 14
		 * and 15, and mode 11 again with 11657's fifteen w:compat flags.
		 *
		 * Each document has sixteen variants, each on a page of its own (its first filler
		 * paragraph is w:pageBreakBefore).  Every line in them is an exact 14pt line, and
		 * the tables have no borders and no top or bottom cell margin, so a row is exactly
		 * its lines and the page bottom (A4, 1in margins: a 697.9pt body) can be put
		 * where wanted.  Fillers and a spacer line bring the table's top to where the page
		 * bottom falls half way through line 2 of the third three-line row (the "1"
		 * variants: one line of that row fits) or half way through its line 3 (the "2"
		 * variants: two fit).  A row is a tag cell ("C1 r3"), a cell whose paragraph
		 * wraps to two lines, and a cell whose paragraph wraps to three, every break at
		 * least 3pt from the measure either way.
		 *
		 *   A  w:widowControl absent everywhere (11657's shape)
		 *   B  w:widowControl w:val="false" on every cell paragraph
		 *   C  w:widowControl, on, on every cell paragraph
		 *   D  A with w:cantSplit on every row
		 *   E  A with w:keepLines on every cell paragraph
		 *   F  A in 11657's row shape: a repeated header row (w:tblHeader), every cell
		 *      w:vAlign bottom, and the tag cell's text in two one-line paragraphs
		 *   G  no table: a body paragraph of two lines (G1, one fits) or three (G2, one
		 *      fits; G3, two fit), w:widowControl absent
		 *   H  G1 with w:widowControl on
		 *
		 * Read off each golden: which page each line of the straddling row or paragraph
		 * lands on (1+1, 1+2, 2+1, or the whole row moved).
		 */
		PROBES.add(rowSplit3Probe(11, false));
		PROBES.add(rowSplit3Probe(11, true));
		PROBES.add(rowSplit3Probe(12, false));
		PROBES.add(rowSplit3Probe(14, false));
		PROBES.add(rowSplit3Probe(15, false));

		/*
		 * textbox-inset-stroke-list (ledger7 section 3 item 2).  Two readings, from four
		 * corpus documents, both about where a text box's text area starts:
		 *   - Word insets a VML box's text by half its stroke: text origins Word minus
		 *     ours +0.42pt and +0.41 at VML's default 0.75pt stroke (5123, 4994), and
		 *     +1.38 at strokeweight 3pt (7046), whose measure is 3pt narrower in Word;
		 *   - a list paragraph in a box loses the box's inset in our render: the list's
		 *     start-indent replaces the container's 7.2pt where Word adds the two (5123,
		 *     7046, 6614: labels 7.2pt to 8.8pt left of Word's).
		 * Neither is settled for strokes the corpus does not have, for a box whose inset
		 * is 0 or not the default, or for a DrawingML box, whose a:ln may or may not be
		 * charged the same way.
		 *
		 * Twelve boxes, four to a page, every one 240 x 340pt, positioned against the
		 * page (50pt or 305pt from its left edge, 70pt or 450pt from its top), unfilled,
		 * each holding the same six paragraphs in Liberation Serif 12pt:
		 *   1. a one-line heading naming the box;
		 *   2. a justified paragraph (its lines' x0 and x1 give the text area);
		 *   3. a list item at w:ind left 360 hanging 360;
		 *   4. a list item at left 720 hanging 360;
		 *   5. a plain paragraph at w:ind left 720, the control for 4;
		 *   6. a right-aligned one-word line.
		 * The boxes:
		 *   - S0, S1, S2, S3, S6: VML v:shape text boxes (the four corpus documents' own
		 *     shape, type #_x0000_t202), default inset, stroked="f" / no stroke
		 *     attributes (VML's default 0.75pt) / strokeweight .5pt (5123's) / 3pt
		 *     (7046's) / 6pt;
		 *   - I0, I3: inset="0,0,0,0" at the default stroke and at 3pt;
		 *   - IW: inset 14.4pt,7.2pt,14.4pt,7.2pt at 3pt;
		 *   - D0, D1, D3, D6: DrawingML wps text boxes (mc:AlternateContent with a VML
		 *     fallback of the same stroke, as Word writes them), default insets, a:ln
		 *     with a:noFill, and w 9525 (0.75pt), 38100 (3pt) and 76200 (6pt).
		 * Read off the golden, per box: the heading's x0 and baseline against the box's
		 * edge, the justified lines' x1, the list labels' x and their text's x, and the
		 * plain indented paragraph's x.
		 */
		PROBES.add(new Probe("textbox-inset-stroke-list",
				"twelve unfilled 240 x 340pt text boxes, four to a page, each holding a heading, a "
				+ "justified paragraph, list items at w:ind 360/360 and 720/360, a plain paragraph "
				+ "at left 720 and a right-aligned word, Liberation Serif 12pt.  VML v:shape "
				+ "(type 202) boxes stroked=\"f\", with no stroke attributes (0.75pt), and at "
				+ "strokeweight .5, 3 and 6pt; inset 0 at the default stroke and at 3pt; inset "
				+ "14.4/7.2pt at 3pt; DrawingML wps boxes with a:ln noFill, 0.75, 3 and 6pt.  "
				+ "Read where each box's text starts and ends: is half the stroke charged, and "
				+ "does a list's indent add to the box's inset?", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(SERIF, 24);
			d.numberingXml("<w:abstractNum w:abstractNumId=\"30\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ Doc.decimalLevel(0, null, 360, 360) + "</w:abstractNum>"
					+ "<w:abstractNum w:abstractNumId=\"31\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ Doc.decimalLevel(0, null, 720, 360) + "</w:abstractNum>"
					+ num(30, 30) + num(31, 31));
			String[][] boxes = {
				// tag, kind, stroke (VML attributes, or a:ln w in EMU), inset, what
				{ "S0", "vml", "stroked=\"f\"", null, "VML, stroked f" },
				{ "S1", "vml", "", null, "VML, default stroke" },
				{ "S2", "vml", "strokeweight=\".5pt\"", null, "VML, stroke .5pt" },
				{ "S3", "vml", "strokeweight=\"3pt\"", null, "VML, stroke 3pt" },
				{ "S6", "vml", "strokeweight=\"6pt\"", null, "VML, stroke 6pt" },
				{ "I0", "vml", "", "0,0,0,0", "VML, inset 0" },
				{ "I3", "vml", "strokeweight=\"3pt\"", "0,0,0,0", "VML, inset 0, stroke 3pt" },
				{ "IW", "vml", "strokeweight=\"3pt\"", "14.4pt,7.2pt,14.4pt,7.2pt",
						"VML, inset 14.4/7.2, 3pt" },
				{ "D0", "dml", null, null, "DrawingML, no line" },
				{ "D1", "dml", "9525", null, "DrawingML, line 0.75pt" },
				{ "D3", "dml", "38100", null, "DrawingML, line 3pt" },
				{ "D6", "dml", "76200", null, "DrawingML, line 6pt" },
			};
			int[][] at = { { 50, 70 }, { 305, 70 }, { 50, 450 }, { 305, 450 } };
			for (int page = 0; page < 3; page++) {
				StringBuilder runs = new StringBuilder();
				for (int i = 0; i < 4; i++) {
					String[] b = boxes[4 * page + i];
					int n = 4 * page + i + 1;
					String content = insetBoxContent(d, b[0], b[4]);
					runs.append("vml".equals(b[1])
							? "<w:r><w:pict>" + (n == 1 ? Corpus.SHAPETYPE_202 : "")
									+ vmlInsetBox(n, at[i][0], at[i][1], b[2], b[3], content) + "</w:pict></w:r>"
							: wpsInsetBox(n, at[i][0], at[i][1], b[2], content));
				}
				// the anchor paragraph holds the boxes and no text, which would print under the first
				d.add((P) org.docx4j.XmlUtils.unwrap(org.docx4j.XmlUtils.unmarshalString(
						"<w:p " + Corpus.BOX_NAMESPACES + "><w:pPr>" + (page > 0 ? "<w:pageBreakBefore/>" : "")
						+ "</w:pPr>" + runs + "</w:p>")));
			}
			return d.pkg();
		}));

		/*
		 * justified-compression-decision (ledger7 section 3 item 7).  When does Word
		 * compress a justified line's spaces to bring the next word up, rather than
		 * stretch them to leave it down?  Say bringing it up needs every space c short of
		 * the face's own space, and leaving it stretches every space by s (both as
		 * fractions of that space).  Eight measured (c, s) pairs from 1035, 2580 and 6083,
		 * and three from section 4.2:
		 *   - Word compressed at (2.4, 22), (3.1, 18.2), (5.8, 18.8) and (10, 32) per cent;
		 *   - it refused at (20.4, 40.4), (21.3, 35), (23.7, 28.6), (32, 87), (22.5, 38.5),
		 *     (15.1, 16.1) and (13.3, 12.8).
		 * Two rules fit all eleven: c <= s/2, or a flat cap near 12%.  batch 44's
		 * justification-crossover brought a word up at c = 19.9% when its alternative was
		 * stretched over 200%, which a flat 12% cap does not allow.  docx4j's rule (c <=
		 * 24% and s >= 30%) gets seven of the eight wrong, and on 1035's numbered first
		 * lines it compresses 31%, beyond its own cap.
		 *
		 * So this is a grid: c of 3, 6, 9, 12, 15, 18, 21 and 24 per cent against s of 8,
		 * 16, 24, 32, 48 and 80, in three faces at 12pt:
		 *   - Carlito (C), the Calibri clone of 2580;
		 *   - Liberation Sans (S), the Arial clone of 1035;
		 *   - Liberation Serif (T), the Times clone of 6083.
		 * 12pt is 50/300 inch, so Word's 1/300-inch size grid (11pt drawn as 11.04)
		 * changes no advance.
		 *
		 * Each case is a two-line w:jc="both" paragraph on a 468pt measure (US Letter,
		 * 1in margins), w:suppressAutoHyphens, under a small marker line that states it.
		 * Its first line is k + 1 words with k spaces, measured off the installed faces
		 * with Doc.advancePoints; the line's last word and the next word are built letter
		 * by letter, with no f anywhere, so nothing ligates.  c depends only on k, s and
		 * the next word, so k is picked (between 6 and 36, the next word about three
		 * spaces wide) as the one whose buildable next word lands nearest; the first line
		 * is built a point short and the paragraph's w:ind w:right takes up the rest, so
		 * s is exact to a twip.  Built, c is within 0.3% of the grid in 152 of the 159
		 * cases and 0.8% in all; the marker states what was built.
		 *
		 * Two further series:
		 *   - K: (9, 20), (12, 25) and (15, 32) in Carlito at k = 12, 20 and 32, which
		 *     says whether the rule is per space or per line;
		 *   - L: 1035's numbered shape in Liberation Sans (w:ind left 567 hanging 567, a
		 *     label, a tab, the text from 28.35pt), which is where docx4j went past its
		 *     cap.
		 * Read off each case: did the next word come up onto line 1?
		 */
		PROBES.add(new Probe("justified-compression-decision",
				"when Word compresses a justified line's spaces to bring the next word up: "
				+ "two-line w:jc=\"both\" paragraphs on a 468pt measure, each built so that "
				+ "bringing its next word up needs every space compressed by c and leaving it "
				+ "stretches them by s (fractions of the face's own space).  c 3..24% by 3 "
				+ "against s 8, 16, 24, 32, 48 and 80%, in Carlito, Liberation Sans and "
				+ "Liberation Serif 12pt; three (c, s) pairs at k = 12, 20 and 32 spaces; and "
				+ "1035's numbered first line (left 567 hanging 567).  The marker above each "
				+ "case states its c, s, k and the w:ind w:right that sets s.  Read off each: "
				+ "did the next word come up?",
				() -> {
			Doc d = Doc.create(15);
			// US Letter, 1 inch margins: a measure of exactly 468.0pt
			d.pageGeometry(12240, 15840, false, 1440, 1440, 1440, 1440);
			d.documentDefaultRun(SANS, 24);
			d.numberingXml("<w:abstractNum w:abstractNumId=\"40\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ Doc.decimalLevel(0, null, 567, 567) + "</w:abstractNum>" + num(40, 40));
			int[] cs = { 3, 6, 9, 12, 15, 18, 21, 24 };
			int[] ss = { 8, 16, 24, 32, 48, 80 };
			String[][] faces = { { "C", CARLITO }, { "S", SANS }, { "T", SERIF } };
			for (String[] face : faces) {
				for (int c : cs) {
					for (int s : ss) {
						compressionCase(d, face[0], face[1], c, s, null, false);
					}
				}
			}
			int[][] perLine = { { 9, 20 }, { 12, 25 }, { 15, 32 } };
			for (int[] cs2 : perLine) {
				for (int k : new int[] { 12, 20, 32 }) {
					compressionCase(d, "K", CARLITO, cs2[0], cs2[1], k, false);
				}
			}
			int[][] labelled = { { 6, 16 }, { 9, 24 }, { 12, 32 }, { 18, 48 }, { 24, 80 }, { 32, 87 } };
			for (int[] cs2 : labelled) {
				compressionCase(d, "L", SANS, cs2[0], cs2[1], null, true);
			}
			return d.pkg();
		}));

		// ------------------------------------------------------------- CR-001 batch 51
		//                                   keeps in tables and chains, a grid wider than
		//                                   the measure, an orphan in a page-spanning cell

		/*
		 * table-keeps-compat12/14/15 (ledger7 section 3 item 4).  Batch 50 found that below
		 * compatibility mode 15 Word applies neither widow control nor w:keepLines in a table
		 * cell.  The keeps that cost 7396 and 10730 (both mode 14) are w:keepNext in cells, and
		 * table-row-keepnext was cut at mode 15 only.  So this asks the same questions per mode,
		 * one document per mode, a case to a page.
		 *   K1, K2, K5: table-row-keepnext's R1, R2 and R5 - keepNext on every paragraph of
		 *     rows one and two (K1), on the first cell's only (K2), on every paragraph of every
		 *     row with a paragraph after the table (K5) - after a filler table sized so that
		 *     one 30pt row fits at the foot of the page (K5: three rows, but not the paragraph
		 *     after them).
		 *   K7: 7396's P5a.  30 exact 30pt rows, every row w:cantSplit and the first cell's
		 *     paragraph keepNext - a 900pt chain - started about two thirds down a page.
		 *   K8: K7 without w:cantSplit.  K9: K7 without keepNext, the control.
		 * Read off the golden: on which page each row's tag ("K7 r12") lands.
		 */
		PROBES.add(tableKeepsProbe(12));
		PROBES.add(tableKeepsProbe(14));
		PROBES.add(tableKeepsProbe(15));

		/*
		 * keep-chain-overlong-compat14/15 (ledger7 item 4).  On 7396 Word moves a keepNext
		 * chain of about 2.3 pages to a fresh page and then breaks inside it, where FOP breaks
		 * it in place; docx4j bounds a keep chain at three pages (boundKeepChains).  Each case
		 * starts a page with exact 24pt fillers to two thirds down it, then a chain of
		 * three-sentence paragraphs each w:keepNext but the last:
		 *   C05, C15, C25: chains of about half a page, one and a half pages and two and a half;
		 *   C15W: C15 with widow control off (7396's style);
		 *   C15S: C15 with the keepNext on a paragraph style rather than on each paragraph.
		 * Read off: the page each chain's first paragraph lands on, and where Word breaks
		 * inside the chain.
		 */
		PROBES.add(keepChainProbe(14));
		PROBES.add(keepChainProbe(15));

		/*
		 * table-grid-over-measure (ledger7 item 3; 14924 and 13886).  Word kept an autofit
		 * grid wider than the measure, the table overhanging the right margin, where docx4j
		 * clamps it to the measure.  A grid this harness writes is recomputed by Word on open,
		 * so here the width comes from the content: three columns, each holding one
		 * unbreakable token of Liberation Serif digits (6pt each at 12pt), whose widths add up
		 * to 102%, 106% and 110% of the 451.3pt measure (A4, 1in margins, mode 14), so that
		 * Word's own autofit needs that width.  w:tblW auto and every cell auto; a full-width
		 * row of justified prose spans the three columns.  Each table has a twin stating
		 * w:tblW 100% (5000 pct).
		 * Read off: the x of each column's first glyph and the spanning row's x0 and x1.
		 */
		PROBES.add(new Probe("table-grid-over-measure",
				"autofit tables (w:tblW auto, every cell auto) whose content - one unbreakable "
				+ "token of digits per cell - adds up to 102%, 106% and 110% of the 451.3pt "
				+ "measure, each with a full-width row of justified prose spanning its three "
				+ "columns, and a twin of each stating w:tblW 100%; mode 14.  Does Word keep the "
				+ "grid wider than the measure, the table overhanging the margin?", () -> {
			Doc d = Doc.create(14);
			final int advTw = Doc.advanceTwipsCeil("0", SERIF, 24); // 120: 6pt a digit
			final int measureTw = 9026;
			for (int pct : new int[] { 102, 106, 110 }) {
				for (boolean stated : new boolean[] { false, true }) {
					String tag = "G" + pct + (stated ? "s" : "");
					d.para(tag + ": three cells whose tokens add up to " + pct + "% of the measure"
							+ (stated ? ", w:tblW 100%" : ", w:tblW auto")).before(240).after(120).add();
					// token widths 30% / 30% / 40% of pct% of the measure, less each cell's margins
					int total = measureTw * pct / 100;
					int[] share = { total * 30 / 100, total * 30 / 100, total * 40 / 100 };
					String[] tokens = new String[3];
					for (int c = 0; c < 3; c++) {
						int digits = Math.max(2, (share[c] - 216) / advTw);
						StringBuilder sb = new StringBuilder();
						for (int k = 0; k < digits; k++) sb.append((char) ('0' + (k % 10)));
						tokens[c] = sb.toString();
					}
					Doc.Table t = new Doc.Table(3000, 3000, 3026);
					if (stated) t.tableWidth(5000, "pct"); else t.autoWidth();
					t.rowOf(null, null, t.cell(tokens[0], SERIF, 24, 1, null),
							t.cell(tokens[1], SERIF, 24, 1, null), t.cell(tokens[2], SERIF, 24, 1, null));
					P spanning = d.para(tag + " spanning row. " + prose(2, pct)).noLabel()
							.jc(JcEnumeration.BOTH).build();
					org.docx4j.wml.Tc tc = t.cell("", SERIF, 24, 3, null);
					tc.getContent().clear();
					tc.getContent().add(spanning);
					t.rowOf(null, null, tc);
					d.add(t.build());
				}
			}
			d.para("after. " + prose(1)).before(240).add();
			return d.pkg();
		}));

		/*
		 * widow-orphan-cell (ledger7 item 4; 7639, mode 15).  FOP leaves the one-line start of
		 * a paragraph at the foot of a page inside a cell that spans pages, where Word moves
		 * it; at mode 15 a cell keeps widow control (table-rowsplit-3), so this is about a
		 * paragraph among many in one page-spanning cell.  The widow-orphan probe's 34
		 * three-line paragraphs, inside the second cell of a one-row two-column table (1600 /
		 * 7400 twips); section 1 widow control absent, section 2 w:widowControl off.
		 * Read off: the lines of each paragraph on each side of every page boundary.
		 */
		PROBES.add(new Probe("widow-orphan-cell",
				"the widow-orphan probe's three-line paragraphs inside the second cell of a "
				+ "one-row table spanning pages: section 1 widow control absent, section 2 off; "
				+ "mode 15.  Does a paragraph's single line stay at a page foot inside the cell?",
				() -> {
			Doc d = Doc.create(15);
			for (int section = 0; section < 2; section++) {
				boolean off = section == 1;
				Doc.Table t = new Doc.Table(1600, 7400);
				P[] ps = new P[34];
				for (int i = 0; i < ps.length; i++) {
					Doc.Para p = d.para("W" + section + " " + String.format("%02d", i + 1) + ". "
							+ prose(3, i + section)).noLabel();
					if (off) p.widowControl(false);
					ps[i] = p.build();
				}
				t.rowOf(null, null, t.cell("W" + section + " tag", SERIF, 24, 1, 1600), t.cellOf(7400, null, ps));
				d.add(t.build());
				d.para("after the table of section " + (section + 1)).add();
				if (section == 0) d.endSection("nextPage", 0);
			}
			return d.pkg();
		}));

		/*
		 * table-cellspacing-pitch-compat14/15 (batch 51, corpus document 4083).  4083's form
		 * table states w:tblCellSpacing 28 twips on w:tblPr and on every w:trPr; across its
		 * date-of-birth row (one digit per cell, ~20 columns) our cells drift 1.4pt a column
		 * from Word's, and a wrap that follows pushes every row below down 11pt.  Our FO uses
		 * the separate-border model (border-separation = 2 x the spacing, each column the grid
		 * less one spacing; word-layout-rules §6.6), which table-cellspacing (2 columns, 72tw
		 * on w:tblPr only, mode 15) found right.  What differs in 4083: the spacing on the
		 * rows too, mode 14, many columns.  Six fixed 1500-twip columns, each cell a
		 * left-aligned tag and a right-aligned one, so both content edges show:
		 *   P28/P72: w:tblCellSpacing 28 / 72 on w:tblPr only;
		 *   R28/R72: on every w:trPr only;
		 *   B28/B72: on both.
		 * Read off: each cell's left tag x0 and right tag x1, i.e. Word's pitch and widths.
		 */
		PROBES.add(cellSpacingPitchProbe(14));
		PROBES.add(cellSpacingPitchProbe(15));

		/*
		 * vml-box-anchor-space-before (batch 51, M22; 4083, 9775, 7042).  A text-relative VML
		 * box (mso-position-vertical-relative:text) is placed margin-top below its anchor
		 * paragraph.  On 4083 our box text sits ~3pt low against the same row's label where
		 * the anchor paragraph has 3pt of space-before, and level where it has none: Word
		 * seems to measure from the paragraph's top BEFORE its space-before, docx4j after it.
		 * But 9775 (body, 14pt before) matches, and 7042 (cell, 12pt, a tall inline image) is
		 * 3.1pt the other way.  Each case: a paragraph of text with space-before 0, 6 or
		 * 18pt holding a 20pt-tall v:rect text box (inset 0) at margin-top 0, -1.2 or 10pt;
		 * in the body (B), and as the only paragraph of a table cell (C).
		 * Read off: the box text's baseline minus its anchor paragraph's text baseline.
		 */
		PROBES.add(new Probe("vml-box-anchor-space-before",
				"text-relative VML text boxes (mso-position-vertical-relative:text, inset 0) at "
				+ "margin-top 0, -1.2 and 10pt, anchored in paragraphs with space-before 0, 6 and "
				+ "18pt, in the body (B) and as a table cell's only paragraph (C); mode 15.  Read "
				+ "the box text's baseline minus its anchor paragraph's text baseline: is a box "
				+ "measured from the paragraph's top before or after its space-before?", () -> {
			Doc d = Doc.create(15);
			int[] befores = { 0, 120, 360 };
			String[] tops = { "0", "-1.2pt", "10pt" };
			int n = 0;
			for (int b : befores) {
				for (String top : tops) {
					String tag = "B" + (b / 20) + "m" + top.replace("pt", "").replace("-", "n").replace(".", "");
					n++;
					d.para(tag + " filler, an ordinary line before the anchor").after(0).add();
					String style = "position:absolute;margin-left:250pt;margin-top:" + top
							+ ";width:150pt;height:20pt;z-index:" + n
							+ ";mso-position-horizontal-relative:text;mso-position-vertical-relative:text";
					// 36pt after: the box (up to 10pt down, 20pt tall) clears the next case's line
					d.para(tag + " anchor").before(b).after(720).run(d.vmlRect(style, tag + " box text")).add();
				}
			}
			d.para("after the body cases").before(240).add();
			Doc.Table t = new Doc.Table(4500, 4500).fixedLayout();
			for (int b : befores) {
				for (String top : tops) {
					String tag = "C" + (b / 20) + "m" + top.replace("pt", "").replace("-", "n").replace(".", "");
					n++;
					// inside the 225pt first cell, clear of the anchor text and of the next cell
					String style = "position:absolute;margin-left:95pt;margin-top:" + top
							+ ";width:120pt;height:20pt;z-index:" + n
							+ ";mso-position-horizontal-relative:text;mso-position-vertical-relative:text";
					P anchor = d.para(tag + " anchor").noLabel().before(b).after(0)
							.run(d.vmlRect(style, tag + " box text")).build();
					t.rowOf(800, org.docx4j.wml.STHeightRule.AT_LEAST, t.cellOf(4500, null, anchor),
							t.cell(tag + " beside", SERIF, 24, 1, 4500));
				}
			}
			d.add(t.build());
			d.para("after the table").before(240).add();
			return d.pkg();
		}));

		// CR-001 batch 52 (ledger8 §5, ~/fidelity-real3/triage/ledger8.md): probes first, one Word run
		PROBES.add(pagebreakParagraphProbe(12));
		PROBES.add(pagebreakParagraphProbe(14));
		PROBES.add(pagebreakParagraphProbe(15));
		PROBES.add(folioParityProbe(false));
		PROBES.add(folioParityProbe(true));
		PROBES.add(sizeChangeBreakProbe());
		PROBES.add(sdtTagsProbe());
		PROBES.add(pctAutofitGridProbe(12));
		PROBES.add(pctAutofitGridProbe(14));
		PROBES.add(pctAutofitGridProbe(15));
		PROBES.add(cellNeighbourBorderProbe());
		PROBES.add(compressionCapProbe());
		PROBES.add(keepChainRoomProbe(14));
		PROBES.add(keepChainRoomProbe(15));
		PROBES.add(ccmpTextLayerProbe());
		PROBES.add(breakAtSpaceProbe());

		// CR-031 phase 0 (docs/developer/change-requests/CR-031-continuous-sections-paginated-as-word.md §5)
		PROBES.add(continuousMarginsProbe(15));
		PROBES.add(continuousMarginsProbe(14));
		PROBES.add(continuousMarginsAtTopProbe());
		PROBES.add(continuousRestartProbe());
		PROBES.add(continuousTitlePgProbe());
		PROBES.add(sectionBreakParagraphFootProbe());
		PROBES.add(continuousRestartEvenOddProbe(false));   // P6, the CR-031 review's finding 4
		PROBES.add(continuousRestartEvenOddProbe(true));
		PROBES.add(continuousRestartEvenOddProbe("continuous-restart-evenodd-start2", 40, 2, false));   // P7
		PROBES.add(continuousRestartEvenOddProbe("continuous-restart-evenodd-start2-mirror", 40, 2, true));
		PROBES.add(continuousRestartEvenOddProbe("continuous-restart-evenodd-oddstart", 15, 1, false));
		for (char v = 'a'; v <= 'e'; v++) PROBES.add(documentEndBreakProbe(v));   // case F (rules §3.3)
		// CR-001 batch 53 (ledger9 §5): one Word run
		PROBES.add(fontSizeGridProbe("serif", SERIF));
		PROBES.add(fontSizeGridProbe("sans", SANS));
		PROBES.add(fontSizeGridProbe("carlito", CARLITO));
		for (int mode : new int[] { 12, 14, 15 }) PROBES.add(headerEmptyParagraphCompatProbe(mode));
		PROBES.add(tableHeaderOverflowProbe(false));
		PROBES.add(tableHeaderOverflowProbe(true));
		for (char v = 'a'; v <= 'd'; v++) PROBES.add(tableNestedRowsplitProbe(v));
		PROBES.add(ommlInlineLineBoxProbe());
		for (String v : new String[] { "2", "6", "20", "heading", "break" }) PROBES.add(trailingEmptiesOverflowProbe(v));
		for (String v : new String[] { "2", "4", "6", "4-cantsplit", "4-header" }) PROBES.add(keepNextTableFirstRowProbe(v));
		PROBES.add(tableNormalStyleRprProbe(12));
		PROBES.add(tableNormalStyleRprProbe(15));
		PROBES.add(tableStyleSizeTriggerProbe('a'));   // ledger9 §9: what in TableGrid sets 1912's tables at docDefaults
		PROBES.add(tableStyleSizeTriggerProbe('b'));
		PROBES.add(vmlBoxBesideProbe(false));   // ledger9 §9 follow-ups: 10855
		PROBES.add(vmlBoxBesideProbe(true));
		PROBES.add(tableOuterBorderStackProbe());   // 9919
		PROBES.add(tableOuterBorderAtLeastProbe()); // 11657
		PROBES.add(actualTextClustersProbe());   // fop/CR-019: the text a reader should get per cluster
		for (String v : new String[] { "first", "middle", "none" }) PROBES.add(shadedGroupKeepsProbe(v));
		PROBES.add(listLabelLineMultiplierProbe());
		PROBES.add(lineBoxBoldRunProbe());
		for (char v = 'a'; v <= 'c'; v++) PROBES.add(sectionFinalEmptyNextPageProbe(v));
		PROBES.add(breakAtDoubleSpaceProbe());

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

		/* fonts-light-bold-italic (2065; CR-001 §6.6 item 46).  Word draws bold italic in a family with an
		 * italic face but no bold one (Calibri Light) as the italic face with a synthetic bold: stroked at
		 * 1/35 em and spread about 0.4% on 2065's 13pt runs; docx4j draws the regular face, FOP-sheared.
		 * To pin the rule: the same sample in Calibri Light regular, italic, bold and bold italic at 8, 11,
		 * 13, 16, 22 and 36pt; Aptos Light italic and bold italic at 11 and 22pt; Calibri (real bold
		 * faces) the control.  Read per line: the face Word's PDF names, the stroke width, and each
		 * glyph's x - bold italic against italic, and bold against regular, advance by advance.
		 * @since 17.3.1 */
		PROBES.add(new Probe("fonts-light-bold-italic",
				"one sample per line, a label in Carlito then the sample: Calibri Light regular (R), italic (I), bold"
				+ " (B) and bold italic (BI) at 8, 11, 13, 16, 22 and 36pt; Aptos Light I and BI at 11 and 22pt;"
				+ " Calibri I and BI at 11 and 22pt (real faces, the control).  Read the face, the stroke width and"
				+ " each glyph's x: Word's synthetic bold of an italic face, its widening per glyph and per size", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(CARLITO, 18);
			final String sample = "\u00d6ssze\u00e1ll\u00edt\u00e1s Hamburgefons";
			java.util.function.Consumer<org.docx4j.wml.RPr> boldItalic = rpr -> { Doc.bold(rpr); Doc.italic(rpr); };
			for (int pt : new int[] { 8, 11, 13, 16, 22, 36 }) {
				d.para().noLabel().after(0).bareText("R" + pt + " ").run(sample, "Calibri Light", pt * 2, null).add();
				d.para().noLabel().after(0).bareText("I" + pt + " ").run(sample, "Calibri Light", pt * 2, Doc::italic).add();
				d.para().noLabel().after(0).bareText("B" + pt + " ").run(sample, "Calibri Light", pt * 2, Doc::bold).add();
				d.para().noLabel().after(120).bareText("BI" + pt + " ").run(sample, "Calibri Light", pt * 2, boldItalic).add();
			}
			for (String font : new String[] { "Aptos Light", "Calibri" }) {
				String tag = font.startsWith("Aptos") ? "AL" : "C";
				for (int pt : new int[] { 11, 22 }) {
					d.para().noLabel().after(0).bareText(tag + "I" + pt + " ").run(sample, font, pt * 2, Doc::italic).add();
					d.para().noLabel().after(120).bareText(tag + "BI" + pt + " ").run(sample, font, pt * 2, boldItalic).add();
				}
			}
			return d.pkg();
		}));

		/* fonts-aptos-light-italic (register cause aptos-light-italic-synthetic).  Word has no italic face for
		 * Aptos Light: fonts-light-bold-italic showed its italic drawn as Aptos-Light sheared 0.3333, where
		 * docx4j draws the metric clone's real Akasia Light Italic, 0.3% narrower on that sample.  No corpus
		 * document uses Aptos Light, so this is the fix's only measure: the same six-sentence paragraph in
		 * Aptos Light regular, italic and bold italic at 11pt and regular and italic at 16pt, and in Aptos
		 * regular and italic (a real italic face), the control.  If Word shears the upright face, its
		 * italic paragraphs wrap line for line as its regular ones do.  @since 17.3.1 */
		PROBES.add(new Probe("fonts-aptos-light-italic",
				"the same six-sentence paragraph, a label in Carlito then the text: Aptos Light regular (LR),"
				+ " italic (LI) and bold italic (LBI) at 11pt, LR and LI at 16pt; Aptos regular (AR) and italic (AI)"
				+ " at 11pt, the control.  Read each paragraph's line breaks and the face and shear Word's PDF names:"
				+ " a sheared upright italic wraps as the upright text does", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(CARLITO, 22);
			final String text = Doc.prose(6, 0);
			java.util.function.Consumer<org.docx4j.wml.RPr> boldItalic = rpr -> { Doc.bold(rpr); Doc.italic(rpr); };
			Object[][] cases = {
				{ "LR11 ", "Aptos Light", 22, null }, { "LI11 ", "Aptos Light", 22, (java.util.function.Consumer<org.docx4j.wml.RPr>) Doc::italic },
				{ "LBI11 ", "Aptos Light", 22, boldItalic },
				{ "LR16 ", "Aptos Light", 32, null }, { "LI16 ", "Aptos Light", 32, (java.util.function.Consumer<org.docx4j.wml.RPr>) Doc::italic },
				{ "AR11 ", "Aptos", 22, null }, { "AI11 ", "Aptos", 22, (java.util.function.Consumer<org.docx4j.wml.RPr>) Doc::italic } };
			for (Object[] c : cases) {
				@SuppressWarnings("unchecked")
				java.util.function.Consumer<org.docx4j.wml.RPr> styling = (java.util.function.Consumer<org.docx4j.wml.RPr>) c[3];
				d.para().noLabel().after(200).bareText((String) c[0]).run(text, (String) c[1], (Integer) c[2], styling).add();
			}
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

	/** A {@code w:tblStylePr} conditional format on an existing table style: the run
	 *  properties the style applies to the cells of that region only (firstCol, firstRow,
	 *  ...).  This is the level 14924's spurious weight comes from.
	 *  @since 17.2.0 (CR-001 batch 48, the toggle-levels probe) */
	private static void addTableStyleCondition(Doc d, String styleId,
			org.docx4j.wml.STTblStyleOverrideType type,
			java.util.function.Consumer<org.docx4j.wml.RPr> rPrCustomiser) {
		org.docx4j.wml.Style s = d.mdp().getStyleDefinitionsPart().getStyleById(styleId);
		org.docx4j.wml.CTTblStylePr cond = Doc.F.createCTTblStylePr();
		cond.setType(type);
		org.docx4j.wml.RPr rpr = Doc.F.createRPr();
		rPrCustomiser.accept(rpr);
		cond.setRPr(rpr);
		s.getTblStylePr().add(cond);
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


	// ---------------------------------------------------------------- CR-030 tables-* helpers

	/** A table of rows x widths.length empty cells (cellAdd fills them), naming styleId (none
	 *  when null), with direct single borders and w:tblLook 0620: first row on, the last row,
	 *  both columns and both bandings off.  Written as XML so that nothing in it formats text. */
	private static Tbl tablesProbeTable(String styleId, int rows, int... widths) throws Exception {
		StringBuilder x = new StringBuilder("<w:tbl xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:tblPr>");
		if (styleId != null) x.append("<w:tblStyle w:val=\"").append(styleId).append("\"/>");
		x.append("<w:tblW w:w=\"0\" w:type=\"auto\"/><w:tblBorders>");
		for (String side : new String[] { "top", "left", "bottom", "right", "insideH", "insideV" }) {
			x.append("<w:").append(side).append(" w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>");
		}
		x.append("</w:tblBorders><w:tblLook w:val=\"0620\" w:firstRow=\"1\" w:lastRow=\"0\" w:firstColumn=\"0\" "
				+ "w:lastColumn=\"0\" w:noHBand=\"1\" w:noVBand=\"1\"/></w:tblPr><w:tblGrid>");
		for (int w : widths) x.append("<w:gridCol w:w=\"").append(w).append("\"/>");
		x.append("</w:tblGrid>");
		for (int r = 0; r < rows; r++) {
			x.append("<w:tr>");
			for (int w : widths) {
				x.append("<w:tc><w:tcPr><w:tcW w:w=\"").append(w).append("\" w:type=\"dxa\"/></w:tcPr></w:tc>");
			}
			x.append("</w:tr>");
		}
		x.append("</w:tbl>");
		return (Tbl) org.docx4j.XmlUtils.unwrap(org.docx4j.XmlUtils.unmarshalString(x.toString()));
	}

	/** The banding probes' table style: band1Horz bold with grey shading, firstRow italic,
	 *  and w:tblStyleRowBandSize where rowBandSize is given. */
	private static void addBandsProbeStyle(Doc d, String styleId, Integer rowBandSize) {
		addBandProbeStyle(d, styleId, org.docx4j.wml.STTblStyleOverrideType.FIRST_ROW,
				org.docx4j.wml.STTblStyleOverrideType.BAND_1_HORZ, rowBandSize, null);
	}

	/** A banding probe's table style: the band condition bold with grey shading, the first
	 *  condition italic, and the band sizes where given. */
	private static void addBandProbeStyle(Doc d, String styleId, org.docx4j.wml.STTblStyleOverrideType first,
			org.docx4j.wml.STTblStyleOverrideType band, Integer rowBandSize, Integer colBandSize) {
		addTableStyle(d, styleId, null);
		addTableStyleCondition(d, styleId, first, rpr -> rpr.setI(Doc.F.createBooleanDefaultTrue()));
		addTableStyleCondition(d, styleId, band, Doc::bold);
		org.docx4j.wml.Style ts = d.mdp().getStyleDefinitionsPart().getStyleById(styleId);
		for (org.docx4j.wml.CTTblStylePr cond : ts.getTblStylePr()) {
			if (cond.getType() == band) {
				org.docx4j.wml.TcPr tcPr = Doc.F.createTcPr();
				org.docx4j.wml.CTShd shd = Doc.F.createCTShd();
				shd.setVal(org.docx4j.wml.STShd.CLEAR);
				shd.setColor("auto");
				shd.setFill("D9D9D9");
				tcPr.setShd(shd);
				cond.setTcPr(tcPr);
			}
		}
		if (rowBandSize != null) {
			org.docx4j.wml.CTTblPrBase.TblStyleRowBandSize size = Doc.F.createCTTblPrBaseTblStyleRowBandSize();
			size.setVal(BigInteger.valueOf(rowBandSize.intValue()));
			ts.getTblPr().setTblStyleRowBandSize(size);
		}
		if (colBandSize != null) {
			org.docx4j.wml.CTTblPrBase.TblStyleColBandSize size = Doc.F.createCTTblPrBaseTblStyleColBandSize();
			size.setVal(BigInteger.valueOf(colBandSize.intValue()));
			ts.getTblPr().setTblStyleColBandSize(size);
		}
	}

	/** tables-banding-col-band-size's table: two rows of six cells under the style,
	 *  w:tblLook with the first column and column banding on and nothing else, the table's own
	 *  w:tblStyleColBandSize where colBandSize is given.  No w:cnfStyle. */
	private static Tbl colBandsProbeTable(String letter, String styleId, Integer colBandSize) throws Exception {
		StringBuilder x = new StringBuilder("<w:tbl xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:tblPr>"
				+ "<w:tblStyle w:val=\"" + styleId + "\"/>"
				+ (colBandSize == null ? "" : "<w:tblStyleColBandSize w:val=\"" + colBandSize + "\"/>")
				+ "<w:tblW w:w=\"0\" w:type=\"auto\"/><w:tblBorders>");
		for (String side : new String[] { "top", "left", "bottom", "right", "insideH", "insideV" }) {
			x.append("<w:").append(side).append(" w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>");
		}
		x.append("</w:tblBorders><w:tblLook w:val=\"0280\" w:firstRow=\"0\" w:lastRow=\"0\" w:firstColumn=\"1\" "
				+ "w:lastColumn=\"0\" w:noHBand=\"1\" w:noVBand=\"0\"/></w:tblPr><w:tblGrid>");
		for (int c = 1; c <= 6; c++) x.append("<w:gridCol w:w=\"1500\"/>");
		x.append("</w:tblGrid>");
		for (int r = 1; r <= 2; r++) {
			x.append("<w:tr>");
			for (int c = 1; c <= 6; c++) {
				x.append("<w:tc><w:tcPr><w:tcW w:w=\"1500\" w:type=\"dxa\"/></w:tcPr><w:p><w:r><w:t>(")
						.append(letter).append(") c").append(c).append("</w:t></w:r></w:p></w:tc>");
			}
			x.append("</w:tr>");
		}
		x.append("</w:tbl>");
		return (Tbl) org.docx4j.XmlUtils.unwrap(org.docx4j.XmlUtils.unmarshalString(x.toString()));
	}

	/** The banding probes' table: six rows of two cells under the style, w:tblLook with the
	 *  first row and row banding on and nothing else; rows 2 and 3 merged vertically in the
	 *  first column, and in the second too where fullyMerged, which leaves row 3 no cell of
	 *  its own; the table's own w:tblStyleRowBandSize where rowBandSize is given.  No
	 *  w:cnfStyle, so Word places the bands itself. */
	private static Tbl bandsProbeTable(String letter, String styleId, boolean fullyMerged, Integer rowBandSize) throws Exception {
		StringBuilder x = new StringBuilder("<w:tbl xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:tblPr>"
				+ "<w:tblStyle w:val=\"" + styleId + "\"/>"
				+ (rowBandSize == null ? "" : "<w:tblStyleRowBandSize w:val=\"" + rowBandSize + "\"/>")
				+ "<w:tblW w:w=\"0\" w:type=\"auto\"/><w:tblBorders>");
		for (String side : new String[] { "top", "left", "bottom", "right", "insideH", "insideV" }) {
			x.append("<w:").append(side).append(" w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>");
		}
		x.append("</w:tblBorders><w:tblLook w:val=\"0420\" w:firstRow=\"1\" w:lastRow=\"0\" w:firstColumn=\"0\" "
				+ "w:lastColumn=\"0\" w:noHBand=\"0\" w:noVBand=\"1\"/></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"4500\"/><w:gridCol w:w=\"4500\"/></w:tblGrid>");
		for (int r = 1; r <= 6; r++) {
			x.append("<w:tr>");
			for (int c = 1; c <= 2; c++) {
				boolean merged = c == 1 || fullyMerged;
				String vMerge = !merged || (r != 2 && r != 3) ? "" : (r == 2 ? "<w:vMerge w:val=\"restart\"/>" : "<w:vMerge/>");
				String text = (r == 3 && merged) ? "" : "(" + letter + ") row " + r + (r == 2 && merged ? ", merged down" : "");
				x.append("<w:tc><w:tcPr><w:tcW w:w=\"4500\" w:type=\"dxa\"/>").append(vMerge).append("</w:tcPr><w:p>");
				if (text.length() > 0) x.append("<w:r><w:t>").append(text).append("</w:t></w:r>");
				x.append("</w:p></w:tc>");
			}
			x.append("</w:tr>");
		}
		x.append("</w:tbl>");
		return (Tbl) org.docx4j.XmlUtils.unwrap(org.docx4j.XmlUtils.unmarshalString(x.toString()));
	}

	/** Appends the paragraphs to the cell at row r, column c. */
	private static void cellAdd(Tbl tbl, int r, int c, P... ps) {
		org.docx4j.wml.Tr tr = (org.docx4j.wml.Tr) org.docx4j.XmlUtils.unwrap(tbl.getContent().get(r));
		org.docx4j.wml.Tc tc = (org.docx4j.wml.Tc) org.docx4j.XmlUtils.unwrap(tr.getContent().get(c));
		for (P p : ps) tc.getContent().add(p);
	}

	/** stylesPara's paragraph, built but not added to the body. */
	private static P tablesPara(Doc d, String pStyle, String text) {
		Doc.Para para = d.para().noLabel().inheritSpacing().bareText(text);
		if (pStyle != null) para.style(pStyle);
		return para.build();
	}

	/** A paragraph of text with a text box anchored after it, the box holding one plain paragraph. */
	private static P boxPara(Doc d, String text, String boxText) throws Exception {
		List<P> inside = new ArrayList<>();
		inside.add(tablesPara(d, null, boxText));
		String xml = Doc.paragraphsXml(inside);
		return d.para().noLabel().inheritSpacing().bareText(text).run(d.textBox(4500, 900, xml, xml)).build();
	}

	private static org.docx4j.wml.RPr rPrOf(org.docx4j.wml.Style s) {
		if (s.getRPr() == null) s.setRPr(Doc.F.createRPr());
		return s.getRPr();
	}

	private static org.docx4j.wml.PPr pPrOf(org.docx4j.wml.Style s) {
		if (s.getPPr() == null) s.setPPr(Doc.F.createPPr());
		return s.getPPr();
	}

	private static void styleName(org.docx4j.wml.Style s, String name) {
		org.docx4j.wml.Style.Name n = Doc.F.createStyleName();
		n.setVal(name);
		s.setName(n);
	}

	private static PPrBase.Spacing spacingAfter(int twips) {
		PPrBase.Spacing sp = Doc.F.createPPrBaseSpacing();
		sp.setAfter(BigInteger.valueOf(twips));
		return sp;
	}

	private static org.docx4j.wml.Jc jc(org.docx4j.wml.JcEnumeration val) {
		org.docx4j.wml.Jc jc = Doc.F.createJc();
		jc.setVal(val);
		return jc;
	}

	/** tables-renamed-default-margins and -nomargins: the default table style renamed My
	 *  Default, with cell margins of 300 or none, under three tables. */
	private static WordprocessingMLPackage renamedDefaultProbe(boolean margins300) throws Exception {
		Doc d = Doc.create(15);
		d.documentDefaultRun(SERIF, 24);
		org.docx4j.wml.Style tn = d.mdp().getStyleDefinitionsPart().getDefaultTableStyle();
		styleName(tn, "My Default");
		if (margins300) {
			tn.getTblPr().getTblCellMar().getLeft().setW(BigInteger.valueOf(300));
			tn.getTblPr().getTblCellMar().getRight().setW(BigInteger.valueOf(300));
		} else {
			tn.getTblPr().setTblCellMar(null);
		}
		addTableStyle(d, "Grid2", tn.getStyleId());
		addTableStyle(d, "Custom", null);
		String[] cases = { "(a) no w:tblStyle: the default, My Default",
				"(b) w:tblStyle Grid2, based on My Default", "(c) w:tblStyle Custom, no w:basedOn" };
		String[] styles = { null, "Grid2", "Custom" };
		for (int k = 0; k < 3; k++) {
			d.para(cases[k]).before(k == 0 ? 0 : 240).after(120).add();
			Tbl t = tablesProbeTable(styles[k], 1, 4500, 4500);
			String letter = cases[k].substring(0, 3);
			cellAdd(t, 0, 0, tablesPara(d, null, letter + " first cell"));
			cellAdd(t, 0, 1, tablesPara(d, null, letter + " second cell"));
			d.add(t);
		}
		return d.pkg();
	}

	/** tables-compat-size-jc and its twins: the same document but for the compatibility mode
	 *  (null: none at all, as Word 2007 writes it) and the setting (null: absent). */
	private static WordprocessingMLPackage compatSizeJcProbe(Integer mode, String setting) throws Exception {
		Doc d = Doc.create(mode == null ? 12 : mode.intValue());
		d.documentDefaultRun(SERIF, 24);
		/* Word 2007 (mode null) writes no compat setting at all, not even the compatibilityMode
		 * Doc.create(12) states; and where the setting is to be absent, the one Doc.create
		 * keeps for modes 14 and 15 goes.  (Until the harness fix of 2026-10-04 this also had
		 * to take out the second compatibilityMode Doc.create left, so the documents on the
		 * share differ from a regeneration in compat settings the question does not touch:
		 * -mode14 carries Word 2013's two, -absent states its mode last, and
		 * tables-compat-size-jc and -on state mode 15 twice.) */
		org.docx4j.wml.CTCompat compat = d.mdp().getDocumentSettingsPart().getContents().getCompat();
		if (mode == null) {
			if (compat != null) compat.getCompatSetting().clear();
		} else if (setting == null && compat != null) {
			compat.getCompatSetting().removeIf(cs -> "overrideTableStyleFontSizeAndJustification".equals(cs.getName()));
		}
		if (setting != null) {
			d.mdp().getDocumentSettingsPart().setWordCompatSetting(
					"overrideTableStyleFontSizeAndJustification", setting);
		}
		org.docx4j.wml.Style normal = d.mdp().getStyleDefinitionsPart().getStyleById("Normal");
		Doc.font(SERIF, 24).accept(rPrOf(normal));
		pPrOf(normal).setJc(jc(org.docx4j.wml.JcEnumeration.LEFT));
		d.addParagraphStyle("ProbeB", "Normal", ppr -> { }, Doc.font(SERIF, 20));
		d.addParagraphStyle("ProbeA", "ProbeB", ppr -> { });
		d.addParagraphStyle("ProbeC", "Normal", ppr -> { }, Doc.font(SERIF, 32));
		addTableStyle(d, "ProbeCompat", null);
		org.docx4j.wml.Style ts = d.mdp().getStyleDefinitionsPart().getStyleById("ProbeCompat");
		Doc.font(SERIF, 18).accept(rPrOf(ts));
		pPrOf(ts).setJc(jc(org.docx4j.wml.JcEnumeration.RIGHT));
		addTableStyleCondition(d, "ProbeCompat", org.docx4j.wml.STTblStyleOverrideType.FIRST_ROW, Doc.font(SERIF, 28));
		for (org.docx4j.wml.CTTblStylePr cond : ts.getTblStylePr()) {
			if (cond.getType() == org.docx4j.wml.STTblStyleOverrideType.FIRST_ROW) {
				org.docx4j.wml.PPr ppr = Doc.F.createPPr();
				ppr.setJc(jc(org.docx4j.wml.JcEnumeration.CENTER));
				cond.setPPr(ppr);
			}
		}
		String label = setting == null ? "absent" : ("1".equals(setting) ? "ON" : "OFF");
		String modeText = mode == null ? ", no compatibilityMode (Word 2007)"
				: (mode.intValue() != 15 || setting == null ? ", compatibilityMode " + mode : "");
		d.para("overrideTableStyleFontSizeAndJustification " + label + modeText
				+ "; the first row is the header").after(120).add();
		Tbl t = tablesProbeTable("ProbeCompat", 2, 3000, 3000, 3000);
		String[] row = { "header", "body" };
		for (int r = 0; r < 2; r++) {
			cellAdd(t, r, 0, tablesPara(d, null, "(a) " + row[r]));
			cellAdd(t, r, 1, tablesPara(d, "ProbeA", "(b) " + row[r]));
			cellAdd(t, r, 2, tablesPara(d, "ProbeC", "(c) " + row[r]));
		}
		d.add(t);
		stylesPara(d, null, "(d) a body paragraph, the 12pt control");
		return d.pkg();
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

		/*
		 * What each w:leader kind draws, and what a tab leaves in the PDF's text layer
		 * (CR-001 batch 45).  Word's PDF was measured on corpus documents only: a tab is
		 * one space glyph of the tab's advance, a leader run has a space at each end, an
		 * underscore or hyphen leader is a run of that character on the entry's own
		 * baseline, and a dot leader opens with a gap of less than one dot.  The probe
		 * puts every kind (none, dot, middleDot, hyphen, underscore, heavy) on a left
		 * stop mid-line, on a right stop before a number (the contents-entry shape), and
		 * from the margin (a leading tab); then a form's fill-in lines (a trailing tab to
		 * an underscore stop, and two on one line); then a tab of almost no advance,
		 * against a ladder of stops every 20 twips, with and without a leader.  Read
		 * off the golden: the character each kind writes and where its glyphs sit; the
		 * width of the space Word writes for the tab, including one under a quarter em;
		 * and whether a heavy leader is a character or a drawn line.
		 */
		PROBES.add(new Probe("tab-leader-kinds",
				"each w:leader kind on a left stop, a right stop and from the margin; form "
				+ "fill-in lines; and a tab of almost no advance, with and without a leader",
				() -> {
			Doc d = Doc.create(15);
			d.para("Each leader kind on a left stop at 4500, on a right stop at 9000 before a "
					+ "number, and from the margin.").after(240).add();
			org.docx4j.wml.STTabTlc[] kinds = {
					org.docx4j.wml.STTabTlc.NONE, org.docx4j.wml.STTabTlc.DOT,
					org.docx4j.wml.STTabTlc.MIDDLE_DOT, org.docx4j.wml.STTabTlc.HYPHEN,
					org.docx4j.wml.STTabTlc.UNDERSCORE, org.docx4j.wml.STTabTlc.HEAVY };
			for (org.docx4j.wml.STTabTlc kind : kinds) {
				String name = kind.value();
				d.para("left " + name).tabStop(4500, org.docx4j.wml.STTabJc.LEFT, kind)
						.tab().text("after " + name).after(60).add();
				d.para("right " + name).tabStop(9000, org.docx4j.wml.STTabJc.RIGHT, kind)
						.tab().text("12").after(60).add();
				d.para().noLabel().tabStop(4500, org.docx4j.wml.STTabJc.LEFT, kind)
						.tab().text("leading " + name).after(180).add();
			}
			d.para("A form's fill-in lines: a trailing tab to an underscore stop, and two "
					+ "on one line.").before(240).after(120).add();
			d.para("Name:").tabStop(9000, org.docx4j.wml.STTabJc.RIGHT, org.docx4j.wml.STTabTlc.UNDERSCORE)
					.tab().after(60).add();
			d.para("Name:").tabStop(4500, org.docx4j.wml.STTabJc.LEFT, org.docx4j.wml.STTabTlc.UNDERSCORE)
					.tabStop(9000, org.docx4j.wml.STTabJc.RIGHT, org.docx4j.wml.STTabTlc.UNDERSCORE)
					.tab().text("Date:").tab().after(60).add();
			d.para("Signed:").tabStop(9000, org.docx4j.wml.STTabJc.RIGHT, org.docx4j.wml.STTabTlc.DOT)
					.tab().after(180).add();
			d.para("A tab of almost no advance: left stops every 20 twips from 880 to 1240, "
					+ "so the tab reaches the first stop past the text, at most 1pt on, whatever "
					+ "the label's width.").before(240).after(120).add();
			Doc.Para ladder = d.para("tiny");
			for (int pos = 880; pos <= 1240; pos += 20) {
				ladder.tabStop(pos, org.docx4j.wml.STTabJc.LEFT);
			}
			ladder.tab().text("after a tab of almost no advance").after(60).add();
			Doc.Para dotted = d.para("tiny");
			for (int pos = 880; pos <= 1240; pos += 20) {
				dotted.tabStop(pos, org.docx4j.wml.STTabJc.LEFT, org.docx4j.wml.STTabTlc.DOT);
			}
			dotted.tab().text("after a dot leader of almost no advance").after(60).add();
			Doc.Para lined = d.para("tiny");
			for (int pos = 880; pos <= 1240; pos += 20) {
				lined.tabStop(pos, org.docx4j.wml.STTabJc.LEFT, org.docx4j.wml.STTabTlc.UNDERSCORE);
			}
			lined.tab().text("after an underscore leader of almost no advance").after(240).add();
			d.para("after. " + prose(1)).add();
			return d.pkg();
		}));
		/*
		 * P5a (ledger6 section 5 item 8): a table our render puts wholly on one page where
		 * Word breaks it.  Corpus document 2451's page 58 carries 439 lines to y=1750.71 on
		 * a 792pt page where Word uses three pages, and 13743's page 22 carries 246 to
		 * y=1701.5.  Neither docx has w:cantSplit and neither has w:tblpPr: the FO is a
		 * plain fo:table whose table-rows each carry a height= taken from w:trHeight, so
		 * what has to be settled is which of the four shapes below FOP will not break, and
		 * what Word does with each of them.
		 *
		 * One document, four tables in sequence.  Each is preceded by twenty filler
		 * paragraphs of exact 24pt lines - 480pt of the 697.9pt body (A4, 1in margins) - so
		 * every table starts a little over two thirds of the way down its page with about
		 * 218pt left, and a page break before each filler gives each variant a page of its
		 * own to start on:
		 *
		 *   A  30 rows carrying w:trHeight 800 and NO w:hRule, which is 2451's shape: 40pt
		 *      rows, taller than their own two lines of content, so the height is what binds
		 *   B  the same 30 rows with no w:trHeight at all
		 *   C  ten rows, of which row 6 holds sixty one-line paragraphs - one row whose own
		 *      content (828pt) is taller than the whole page body
		 *   D  A again, with w:cantSplit on every row
		 *
		 * Every row's first cell names its variant and row number ("A r07"), so the row a
		 * page break falls on can be read off any extractor, and every other row's second
		 * cell holds a sentence two lines long, so a row Word divides shows one of its
		 * lines on each page.  Read off the golden: which row each page break falls on, and
		 * whether Word ever splits inside a row.
		 */
		PROBES.add(new Probe("table-rowsplit",
				"a 30-row table started two thirds of the way down its page, in four "
				+ "variants: rows carrying w:trHeight 800 with no w:hRule, the same rows "
				+ "with no w:trHeight at all, a ten-row table one of whose rows is taller "
				+ "than the page body, and the first variant with w:cantSplit on every row",
				() -> {
			Doc d = Doc.create(15);
			d.para("Four tables, each started two thirds of the way down a page of its own "
					+ "by twenty filler paragraphs of exact 24pt lines. Every row's first "
					+ "cell names its variant and row number, so the row a page break falls "
					+ "on can be read off the text layer.").after(240).add();
			String[] names = { "A", "B", "C", "D" };
			for (int v = 0; v < names.length; v++) {
				String name = names[v];
				d.pageBreak();
				for (int i = 1; i <= 20; i++) {
					d.para(name + " filler " + String.format("%02d", i) + " of 20, an exact "
							+ "24pt line; twenty of them fill 480pt of the 697.9pt body")
							.line(480, STLineSpacingRule.EXACT).add();
				}
				Doc.Table t = new Doc.Table(1600, 7400).fixedLayout();
				int rows = "C".equals(name) ? 10 : 30;
				Integer height = ("B".equals(name) || "C".equals(name)) ? null : 800;
				for (int r = 1; r <= rows; r++) {
					String tag = name + " r" + String.format("%02d", r);
					if ("C".equals(name) && r == 6) {
						// the one row taller than the page body: sixty one-line paragraphs,
						// 828pt of content against a 697.9pt body
						P[] tall = new P[60];
						for (int k = 0; k < tall.length; k++) {
							tall[k] = Doc.plainParagraph(tag + " tall line "
									+ String.format("%02d", k + 1) + " of 60", SERIF, 24);
						}
						t.rowOf(null, null, t.cell(tag, SERIF, 24, 1, 1600),
								t.cellOf(7400, null, tall));
					} else {
						t.rowOf(height, null, t.cell(tag, SERIF, 24, 1, 1600),
								t.cell(tag + ": this cell holds a sentence which is long "
										+ "enough to take two lines in a column 370 points "
										+ "wide, so a row which Word divides shows one of "
										+ "its lines on each page.", SERIF, 24, 1, 7400));
					}
				}
				Tbl tbl = t.build();
				if ("D".equals(name)) cantSplitEveryRow(tbl);
				d.add(tbl);
				d.para("after table " + name + ". " + prose(1, v)).before(240).add();
			}
			return d.pkg();
		}));

		/*
		 * P5a recut (ledger6 section 5 item 8).  The first probe settled nothing.  Its
		 * variant A declared w:trHeight 800 - 40pt - against 41.76pt of content, so the
		 * height never bound and A was a copy of B; and w:trHeight, w:cantSplit and a row
		 * taller than the whole body all paginated identically on both sides, to 0.73pt
		 * over 548 lines.  What corpus documents 2451 and 13743 do that the first probe
		 * did not test is a declared row height LARGER than the row's own content, a table
		 * whose TOTAL height rather than one row's decides, and a table nested one level
		 * inside another table's cell - a fo:table inside a table-cell being the one
		 * container FOP will not break.
		 *
		 * One document, five variants, each on a page of its own.  Every row carries the
		 * first probe's own content, unchanged so that its height stays the measured
		 * 41.76pt (a tag cell, "A r07", and a sentence three lines long), and declares
		 * w:trHeight 1200 = 60pt, half as tall again as that content, so this time the
		 * height is what binds:
		 *
		 *   A  30 rows, w:trHeight 1200 with NO w:hRule (which is Word's atLeast)
		 *   B  the same 30 rows with w:hRule="atLeast" stated
		 *   C  the same 30 rows with w:hRule="exact"
		 *   D  18 rows - the count the corpus tables carry - started at the PAGE TOP, so
		 *      that the table's total height, 1080pt against a 697.9pt body, decides
		 *   E  30 such rows nested in the single cell of a one-cell outer table, which is
		 *      the shape 2451 and 13743 may have; the outer table declares zero cell
		 *      margins so the nested table's columns are variant A's exactly
		 *
		 * A, B, C and E are each preceded by twenty filler paragraphs of exact 24pt lines
		 * - 480pt of the 697.9pt body - so each starts a little over two thirds of the way
		 * down its page with about 218pt left, exactly where the first probe's tables
		 * started; D follows its page break with nothing at all.  (The page break's own
		 * empty paragraph takes a line at the top of the new page in Word always, and in
		 * our render only where a table rather than a paragraph follows it - our exporter
		 * folds the break onto the next block when it can - which is about 25pt.  It
		 * changes no row here: every variant puts the same row on the same page with the
		 * lead-in and without it, the tightest margin being D's 7pt.)  Read off the
		 * golden: which row each page break falls on, how tall Word makes a row under each
		 * w:hRule, and whether Word divides the nested table at all.
		 */
		PROBES.add(new Probe("table-rowsplit-2",
				"rows declaring w:trHeight 1200 - half as tall again as their own content "
				+ "- with no w:hRule, with w:hRule=\"atLeast\" and with w:hRule=\"exact\", "
				+ "an 18-row table of the same rows started at the page top, and 30 of them "
				+ "nested in the single cell of a one-cell table", () -> {
			Doc d = Doc.create(15);
			d.para("Five tables whose rows all declare w:trHeight 1200 - 60pt - over 41.76pt "
					+ "of content. Every row's first cell names its variant and row number, "
					+ "so the row a page break falls on can be read off the text layer.")
					.after(240).add();
			String[] names = { "A", "B", "C", "D", "E" };
			for (int v = 0; v < names.length; v++) {
				String name = names[v];
				org.docx4j.wml.STHeightRule rule = "B".equals(name) ? org.docx4j.wml.STHeightRule.AT_LEAST
						: "C".equals(name) ? org.docx4j.wml.STHeightRule.EXACT : null;
				int rows = "D".equals(name) ? 18 : 30;
				d.pageBreak();
				// D starts at the page top; the others two thirds of the way down it
				if (!"D".equals(name)) {
					for (int i = 1; i <= 20; i++) {
						d.para(name + " filler " + String.format("%02d", i) + " of 20, an exact "
								+ "24pt line; twenty of them fill 480pt of the 697.9pt body")
								.line(480, STLineSpacingRule.EXACT).add();
					}
				}
				Doc.Table t = new Doc.Table(1600, 7400).fixedLayout();
				for (int r = 1; r <= rows; r++) {
					String tag = name + " r" + String.format("%02d", r);
					t.rowOf(1200, rule, t.cell(tag, SERIF, 24, 1, 1600),
							t.cell(tag + ": this cell holds a sentence which is long "
									+ "enough to take two lines in a column 370 points "
									+ "wide, so a row which Word divides shows one of "
									+ "its lines on each page.", SERIF, 24, 1, 7400));
				}
				if ("E".equals(name)) {
					Doc.Table outer = new Doc.Table(9000).fixedLayout().cellMargins(0, 0);
					outer.rowOf(null, null, outer.cellWith(9000, t.build(),
							"E after the nested table", SERIF, 24));
					d.add(outer.build());
				} else {
					d.add(t.build());
				}
				d.para("after table " + name + ". " + prose(1, v)).before(240).add();
			}
			return d.pkg();
		}));

		/*
		 * P10 (ledger6 section 5 item 9): which of w:tblGrid and the row's own w:tcW Word
		 * lays a table out on.  Corpus 4025's description column is 106.7pt against Word's
		 * 183.7 and starts 40.7pt to the right, and takes 38 lines where Word takes 19;
		 * 2564's letterhead cell is 37.75pt too narrow and 37.75pt too far right; 5528's
		 * left column is 355pt against Word's 255.  gridIsAuthoritative (0a457a177) keys on
		 * exactly this, and on the compatibility mode, so the four configurations are cut
		 * at modes 12, 14 and 15 - and since the mode is a document property, that is three
		 * probe documents (the pattern table-indent-compat14/15 and table-grid-edge-compat*
		 * already use).
		 *
		 * Each document holds four tables, one to a page, over a 9026-twip text column:
		 *
		 *   A  w:tblGrid 2000/3000/4000 and every row-1 w:tcW the same
		 *   B  the same grid, row 1's w:tcW 4000/3000/2000 - the same sum, the opposite
		 *      proportions, so whichever of the two Word uses is unmistakable
		 *   C  the same grid, row 1's first cell w:tcW auto and the other two in dxa
		 *   D  w:tblW auto over a grid of 2800/4400/4444 = 11644 twips, 29% wider than the
		 *      text column, every row-1 w:tcW the grid's own
		 *
		 * Row 2 of every table declares w:tcW auto, so the golden also says whether a later
		 * row follows the grid or follows row 1.  Each cell's text opens with a label
		 * naming the table and the column ("B c2"), so each column's left edge is the x of
		 * a first glyph and its width is the gap to the next one.
		 */
		PROBES.add(tableGridVsTcwProbe(12));
		PROBES.add(tableGridVsTcwProbe(14));
		PROBES.add(tableGridVsTcwProbe(15));

		/*
		 * The overlong unbreakable token, both halves (ledger6 section 5 item 10, ledger
		 * seed 2 and P4).  Word breaks KAP_DUCTLENGTH_FEEDER_KOPA INSIDE the token in a
		 * 205pt cell - "...KAP_DUCTLE" then "NGTH_FEEDER_KOPA" - where we set the whole of
		 * it on one 140.4pt line; and on 11657 Word fits a 481.7pt token on one line where
		 * we break before it.  So the two halves are opposite, and neither may be
		 * implemented until Word has answered both measures.
		 *
		 * The tokens are built out of the font's own advances.  Every digit of Liberation
		 * Serif, and its underscore, has an advance of exactly 6.0pt at 12pt - 120 twips,
		 * measured off the installed face with Doc.advancePoints - and the digits are
		 * tabular, so a token of n digits is exactly 120n twips wide AND its characters can
		 * be counted in the extracted text, which a run of one repeated letter could not.
		 * The tokens are therefore cycles of "0123456789"; they are letters-digits-and-
		 * underscores tokens with no hyphen and no break opportunity in them.
		 *
		 * Two measures, each tested in the body and in a fixed-layout cell:
		 *
		 *   the body   A4 portrait with 1143-twip left and right margins: 9620 twips =
		 *              481.0pt exactly, 11657's measure
		 *   the cell   a one-column table, w:tblLayout fixed, w:tcW 4100 dxa, w:tblCellMar 0
		 *              and w:tblBorders none, so nothing at all is charged against the
		 *              measure and it is exactly 4100 twips = 205.0pt
		 *
		 * and four cases at each, the token paragraph carrying NO label (a label would move
		 * the token and is the one thing being measured here), introduced by a labelled
		 * paragraph of its own:
		 *
		 *   A  floor(measure/120) + 8 characters: 48pt longer than the measure
		 *   B  a measured lead run, then a token 3 characters longer than the space the
		 *      lead leaves on the line but far shorter than the measure
		 *   C  floor(measure/120) characters: 4080 twips in the cell and 9600 in the body,
		 *      20 twips - one point - inside the measure.  The longest token that fits
		 *   D  one character more: 4200 and 9720 twips, 100 twips - five points - over.
		 *      The shortest token that does not fit
		 *
		 * C and D bracket "exactly the measure" from both sides to within one character's
		 * advance, which is as close as a token of one alphabet can come: both measures
		 * happen to sit 20 twips above a multiple of 120, so the bracket is the same 1pt /
		 * 5pt at 205pt as at 481pt.  E is the corpus's own token, KAP_DUCTLENGTH_FEEDER_KOPA
		 * (200.67pt in this face at 12pt), in the same 4100-twip cell but with Word's
		 * default 108-twip cell margins, which leaves a measure of 194.2pt - the
		 * configuration in which Word broke it mid-token.
		 *
		 * Read off the golden: at each measure and in each container, where Word breaks -
		 * inside the token, before it, or nowhere, letting it overflow.
		 */
		PROBES.add(new Probe("break-longword",
				"a token of digits and underscores (120 twips a character in Liberation "
				+ "Serif 12pt) longer than the measure, longer than the space left on the "
				+ "line, one character inside the measure and one character over it, at a "
				+ "481.0pt body measure and in a 205.0pt fixed-layout cell, plus the corpus "
				+ "token KAP_DUCTLENGTH_FEEDER_KOPA in a cell whose measure is 194.2pt", () -> {
			Doc d = Doc.create(15);
			// A4 portrait, 1143-twip left and right margins: a body measure of exactly
			// 9620 twips = 481.0pt
			d.pageGeometry(11906, 16838, false, 1440, 1143, 1440, 1143);
			final int advTw = Doc.advanceTwipsCeil("0", SERIF, 24);      // 120, checked below
			if (advTw != 120) throw new IllegalStateException("Liberation Serif's digit advance "
					+ "is " + advTw + " twips, not the 120 this probe's token lengths are built on");
			final String lead = "B lead text, then the token: ";
			final int leadTw = Doc.advanceTwipsCeil(lead, SERIF, 24);
			final String corpusToken = "KAP_DUCTLENGTH_FEEDER_KOPA";

			d.para("Tokens of digits and underscores, every character of which is exactly "
					+ "120 twips wide in Liberation Serif at 12pt. The body's measure here "
					+ "is 9620 twips = 481.0pt exactly; the cells below are 4100 twips = "
					+ "205.0pt, with no cell margin and no border, so nothing is charged "
					+ "against their measure. Each token paragraph carries no label, "
					+ "because a label would move the token.").after(240).add();

			for (int part = 0; part < 2; part++) {
				final boolean body = part == 0;
				final int measure = body ? 9620 : 4100;
				final String where = body ? "BODY" : "CELL";
				final int fit = measure / advTw;
				if (!body) d.pageBreak();
				d.para(where + ": the measure is " + measure + " twips = " + (measure / 20.0)
						+ "pt, and " + fit + " characters of the token fill "
						+ (fit * advTw / 20.0) + "pt of it.").before(240).after(120).add();

				int[] lengths = { fit + 8, 0, fit, fit + 1 };
				String[] tags = { "A", "B", "C", "D" };
				String[] what = {
					"a token of " + (fit + 8) + " characters, " + ((fit + 8) * advTw / 20.0)
						+ "pt, which is " + (8 * advTw / 20.0) + "pt longer than the measure",
					"", // filled in below, once the lead is measured
					"a token of " + fit + " characters, " + (fit * advTw / 20.0) + "pt, "
						+ ((measure - fit * advTw) / 20.0) + "pt inside the measure: the "
						+ "longest token which fits",
					"a token of " + (fit + 1) + " characters, " + ((fit + 1) * advTw / 20.0)
						+ "pt, " + (((fit + 1) * advTw - measure) / 20.0) + "pt over the "
						+ "measure: the shortest token which does not fit" };
				int lenB = (measure - leadTw) / advTw + 3;
				lengths[1] = lenB;
				what[1] = "a lead run of " + (leadTw / 20.0) + "pt, then a token of " + lenB
						+ " characters, " + (lenB * advTw / 20.0) + "pt, which is longer "
						+ "than the " + ((measure - leadTw) / 20.0) + "pt the lead leaves on "
						+ "the line but shorter than the whole measure";
				for (int c = 0; c < tags.length; c++) {
					String token = digitToken(lengths[c]);
					d.para(where + "-" + tags[c] + ": " + what[c] + ".").after(60).add();
					Doc.Para tp = d.para().noLabel();
					if (c == 1) tp.text(lead);
					tp.text(token).after(180);
					if (body) {
						tp.add();
					} else {
						d.add(tokenCell(measure, true, tp.build()));
					}
				}
				if (!body) {
					d.para("CELL-E: the corpus's own token, " + corpusToken + ", which is "
							+ String.format("%.2f", Doc.advancePoints(corpusToken, SERIF, 24))
							+ "pt in this face, in the same 4100-twip cell but with Word's "
							+ "default 108-twip cell margins, so the measure is 194.2pt and "
							+ "the token does not fit. This is the corpus's own "
							+ "configuration.").after(60).add();
					d.add(tokenCell(measure, false, d.para().noLabel().text(corpusToken).build()));
					d.para("BODY-E: the same token in the body, where it fits with room to "
							+ "spare (the control).").before(180).after(60).add();
					d.para().noLabel().text(corpusToken).after(180).add();
				}
			}
			d.para("after. " + prose(1)).before(240).add();
			return d.pkg();
		}));

		/*
		 * 7235's two blank pages (ledger6 section 5 item 11).  Our pages 3 and 8 carry 0
		 * and 1 lines; the document is a landscape assessment grid whose first section is
		 * portrait and holds a table far wider than the portrait column, and whose second
		 * section is landscape and continuous.  page-blank and page-empty carry every other
		 * shape that has been suspected of costing or losing a page; this is the one they
		 * do not, and it is cut at compatibility mode 14, which is 7235's.
		 *
		 * A continuous break which also changes the page size is the interesting half: Word
		 * cannot honour "continue on this page" and a new page size at once.  Read off the
		 * golden: which pages Word emits, whether any of them is blank, and which page the
		 * landscape section starts on.
		 */
		PROBES.add(new Probe("page-blank-landscape",
				"an A4 portrait section holding a table 14000 twips wide - well over its "
				+ "9026-twip column - followed by a landscape continuous section, at "
				+ "compatibilityMode 14", () -> {
			Doc d = Doc.create(14);
			d.para("S1. This section is A4 portrait and its text column is 9026 twips. The "
					+ "table below is 14000 twips wide, so it overhangs. " + prose(2)).after(240).add();
			Doc.Table t = new Doc.Table(3500, 3500, 3500, 3500).fixedLayout();
			t.row(SERIF, 20, false, "S1 c1 head", "S1 c2 head", "S1 c3 head", "S1 c4 head");
			for (int r = 1; r <= 6; r++) {
				t.row(SERIF, 20, false, "S1 r" + r + " c1", "S1 r" + r + " c2",
						"S1 r" + r + " c3", "S1 r" + r + " c4");
			}
			d.add(t.build());
			d.para("S1 after the wide table. " + prose(2, 1)).before(240).add();
			d.endSection("continuous", 0);
			// the landscape section, opened by a continuous break: A4 landscape, 1in margins
			d.pageGeometry(16838, 11906, true, 1440, 1440, 1440, 1440);
			for (int i = 0; i < 20; i++) {
				d.para("S2 landscape. " + prose(2, i)).after(160).add();
			}
			return d.pkg();
		}));

		/*
		 * The other half of ledger6 section 5 item 11: on 7235 we paint 173 dot-leader runs
		 * to Word's 42, and our hanging indent inside its cells is 55.86 against Word's
		 * 50.18.  The runs are numbering tabs - w:suff is a tab, the level's own w:pPr
		 * carries a tab stop with w:leader="dot" - and they are inside table cells, where
		 * the label sits so close to the stop that there is barely room for a dot.
		 *
		 * Three numbering definitions, each used once inside a cell and once in the body as
		 * its control.  The label "1." is 180 twips wide and level 0's w:ind is left 720
		 * hanging 360, so the label runs from 360 to 540:
		 *
		 *   numId 40  a left stop at 560 with w:leader="dot" - 20 twips, one point, of
		 *             advance past the label, which is less than one dot
		 *   numId 41  the same stop at 1000 - 23pt of advance, enough for a countable run
		 *             of dots
		 *   numId 42  no tab stop in the level at all (the control: the numbering tab then
		 *             runs to the level's own w:ind left)
		 *
		 * Read off the golden: whether Word paints a leader at a numbering tab inside a
		 * cell at all, how many dots it paints at each advance, and where the wrapped line
		 * of each paragraph starts.
		 */
		PROBES.add(new Probe("tab-leader-in-cell",
				"a w:numPr paragraph inside a table cell whose numbering level has a tab "
				+ "suffix and a w:leader=\"dot\" stop 20 twips past the label, the same at "
				+ "a 460-twip advance, and a level with no stop at all, each with the same "
				+ "paragraph in the body as its control", () -> {
			Doc d = Doc.create(15);
			d.numberingXml(
					"<w:abstractNum w:abstractNumId=\"40\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ leaderLevel(0, 560, "dot") + "</w:abstractNum>"
					+ "<w:abstractNum w:abstractNumId=\"41\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ leaderLevel(0, 1000, "dot") + "</w:abstractNum>"
					+ "<w:abstractNum w:abstractNumId=\"42\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ leaderLevel(0, 0, null) + "</w:abstractNum>"
					+ "<w:num w:numId=\"40\"><w:abstractNumId w:val=\"40\"/></w:num>"
					+ "<w:num w:numId=\"41\"><w:abstractNumId w:val=\"41\"/></w:num>"
					+ "<w:num w:numId=\"42\"><w:abstractNumId w:val=\"42\"/></w:num>");
			String[] what = {
				"a dot leader on a stop at 560 twips, 20 twips past the end of the label",
				"a dot leader on a stop at 1000 twips, 460 twips past the end of the label",
				"no tab stop in the level at all" };
			for (int i = 0; i < what.length; i++) {
				int numId = 40 + i;
				d.para("L" + (i + 1) + ": w:numId " + numId + ", " + what[i] + ". The "
						+ "numbered paragraph is first inside a table cell and then in the "
						+ "body.").before(i == 0 ? 0 : 240).after(120).add();
				Doc.Table t = new Doc.Table(4500, 4500);
				t.rowOf(null, null,
						t.cellOf(4500, null, d.para("L" + (i + 1) + " in a cell: this "
								+ "paragraph is long enough to wrap, so the golden shows "
								+ "where its second line starts.").numPr(numId, 0).build()),
						t.cellOf(4500, null, Doc.plainParagraph("L" + (i + 1) + " right cell",
								SERIF, 24)));
				d.add(t.build());
				d.para("L" + (i + 1) + " in the body: this paragraph is long enough to wrap, "
						+ "so the golden shows where its second line starts.")
						.numPr(numId, 0).before(120).after(120).add();
			}
			d.para("after. " + prose(1)).before(240).add();
			return d.pkg();
		}));

		/*
		 * The leader question proper (ledger6 section 5 item 11, second half), which the
		 * first probe could not reach.  Its honoured stop was at 560 twips over a label
		 * ending at 540, so the tab opened 1.78pt where a Liberation Serif period advances
		 * 3.0pt: Word painted no dot, we painted no dot, and nothing was learnt about a
		 * leader of countable length.  What the golden did settle is the stop's own rule -
		 * the numbering tab goes to the first of {the level's explicit stops, w:ind left}
		 * past the end of the label - so a stop which is to be used has to fall SHORT of
		 * w:ind left, and to be readable it has to open several points of advance.
		 *
		 * Two levels, each used once inside a table cell and once in the body as its
		 * control.  Both declare w:ind w:left="2880" w:hanging="2520", so the label "1."
		 * runs from 360 to 540 twips and the paragraph's own indent is at 2880:
		 *
		 *   numId 43  one left stop at 1440 with w:leader="dot" - past the label and 1440
		 *             twips short of w:ind left, so by the rule above it is the stop the
		 *             numbering tab uses, and it opens 900 twips = 45pt of advance, which
		 *             is fifteen periods of the 12pt face
		 *   numId 44  the same w:ind and no w:tabs at all: the numbering tab then runs to
		 *             w:ind left, 2340 twips = 117pt of advance, and no leader is due
		 *
		 * The cell is 7000 twips rather than the first probe's 4500, because a 2880-twip
		 * indent inside a 4500-twip cell would leave a wrapped line 70pt of measure and
		 * the wrap, not the leader, would be what the golden showed.  Read off the golden:
		 * how many dots Word paints over the 45pt advance, where the first glyph after the
		 * tab sits at each level, and whether a wide numbering tab with no stop paints
		 * anything at all.
		 */
		PROBES.add(new Probe("tab-leader-in-cell-2",
				"a w:numPr paragraph whose level declares w:ind left 2880 hanging 2520 and "
				+ "one w:leader=\"dot\" stop at 1440 twips - used, because it falls short "
				+ "of w:ind left, and 45pt wide - and the same level with no stop at all, "
				+ "each once inside a table cell and once in the body", () -> {
			Doc d = Doc.create(15);
			d.numberingXml(
					"<w:abstractNum w:abstractNumId=\"43\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ leaderLevel(0, 1440, "dot", 2880, 2520) + "</w:abstractNum>"
					+ "<w:abstractNum w:abstractNumId=\"44\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ leaderLevel(0, 0, null, 2880, 2520) + "</w:abstractNum>"
					+ "<w:num w:numId=\"43\"><w:abstractNumId w:val=\"43\"/></w:num>"
					+ "<w:num w:numId=\"44\"><w:abstractNumId w:val=\"44\"/></w:num>");
			String[] what = {
				"a dot leader on a stop at 1440 twips, 900 twips - 45 points - past the end "
						+ "of the label and 1440 twips short of w:ind left",
				"no tab stop in the level at all, so the numbering tab runs to w:ind left at "
						+ "2880 twips, 2340 twips of advance past the label" };
			for (int i = 0; i < what.length; i++) {
				int numId = 43 + i;
				d.para("L" + (i + 1) + ": w:numId " + numId + ", w:ind left 2880 hanging "
						+ "2520, " + what[i] + ". The numbered paragraph is first inside a "
						+ "table cell and then in the body.").before(i == 0 ? 0 : 240)
						.after(120).add();
				Doc.Table t = new Doc.Table(7000, 2000);
				t.rowOf(null, null,
						t.cellOf(7000, null, d.para("L" + (i + 1) + " in a cell: this "
								+ "paragraph is long enough to wrap, so the golden shows "
								+ "where its second line starts.").numPr(numId, 0).build()),
						t.cellOf(2000, null, Doc.plainParagraph("L" + (i + 1) + " right cell",
								SERIF, 24)));
				d.add(t.build());
				d.para("L" + (i + 1) + " in the body: this paragraph is long enough to wrap, "
						+ "so the golden shows where its second line starts.")
						.numPr(numId, 0).before(120).after(120).add();
			}
			d.para("after. " + prose(1)).before(240).add();
			return d.pkg();
		}));

		/*
		 * 12363's first page (ledger6 section 5 item 12, first half): our body starts
		 * 25.48pt below Word's on page 1.  Its first-page header holds a picture taller
		 * than the w:header distance, and page-tall-header measures the same question for a
		 * header of twelve text lines only - never for a picture, and never on a w:titlePg
		 * first page, where the header which decides page 1 is not the one which decides
		 * page 2.
		 *
		 * The first-page header here holds a caption line and a picture 288pt tall against
		 * a w:header distance of 708 twips (35.4pt) and a top margin of 1440 (72pt), so the
		 * picture alone is four times the top margin.  The default header is one ordinary
		 * line, and the body runs to three pages, so page 2 is the control.  Read off the
		 * golden: where Word's body starts on page 1 and where on page 2.
		 */
		PROBES.add(new Probe("page-tall-header-image",
				"a w:titlePg first-page header holding a picture 288pt tall - far more than "
				+ "the 35.4pt w:header distance and the 72pt top margin - against an "
				+ "ordinary one-line default header on the pages after it", () -> {
			Doc d = Doc.create(15);
			d.addHeader(org.docx4j.wml.HdrFtrRef.FIRST, java.util.Arrays.asList(
					Doc.plainParagraph("HFIRST: the picture below is 72pt wide and 288pt "
							+ "tall; w:header is 708 twips and the top margin 1440", SANS, 16),
					d.pictureParagraph(100, 400, 1440)));
			d.addHeader(org.docx4j.wml.HdrFtrRef.DEFAULT, SANS, 20,
					"HDEFAULT: one ordinary header line");
			d.addFooter(org.docx4j.wml.HdrFtrRef.DEFAULT, SANS, 20, "footer");
			for (int i = 0; i < 40; i++) {
				d.para(prose(3, i)).after(160).add();
			}
			return d.pkg();
		}));

		/*
		 * 12363's footer (ledger6 section 5 item 12, second half): 133 of that document's
		 * unmatched lines come from a VML box in the footer at
		 * "margin-top:719.35pt; mso-position-vertical-relative:text", which Word puts off
		 * the page and simply does not draw, and which we paint.  The rule to be confirmed
		 * is that what falls wholly off the page is not painted at all.
		 *
		 * The footer holds three of the same shape: one at margin-top 900pt, which is more
		 * than the 842pt height of the whole A4 page and so cannot be on it wherever the
		 * footer's own line sits; one at the corpus's own margin-top 719.35pt, which is
		 * past the page's foot but not past its height, and which is the value that
		 * produced 12363's 133 lines; and one at margin-top -300pt, which is 300pt above
		 * the footer line and so squarely on the page.  The last is the control: it proves
		 * that Word does draw this shape when it is on the page, so that the absence of
		 * the other two means something.  All three are bare w:pict / v:rect - no
		 * mc:AlternateContent, no DrawingML twin - which is 12363's own shape, and each
		 * holds one line of text whose words appear nowhere else in the document.
		 *
		 * The distinction between 900 and 719.35 is not idle: on b70 our own render draws
		 * the -300pt shape and drops the 900pt one (FOP puts it past the page bottom and
		 * paints nothing), so the corpus's value is the one which has to be cut as well if
		 * the probe is to reproduce what 12363 does.  Read off the golden: whether Word's
		 * PDF carries any glyph of either off-page shape.
		 */
		PROBES.add(new Probe("footer-offpage-shape",
				"a footer holding three VML v:rect shapes with text in them, one at "
				+ "margin-top 900pt - more than the whole page's 842pt height - one at the "
				+ "corpus's own margin-top 719.35pt, and one at margin-top -300pt, on the "
				+ "page, as their control", () -> {
			Doc d = Doc.create(15);
			P off = Doc.plainParagraph("", SERIF, 20);
			off.getContent().clear();
			off.getContent().add(d.vmlRect(
					"position:absolute;margin-left:0;margin-top:900pt;width:300pt;height:40pt;"
					+ "z-index:1;mso-position-vertical-relative:text",
					"OFFPAGE shape text at margin-top 900pt"));
			P corpus = Doc.plainParagraph("", SERIF, 20);
			corpus.getContent().clear();
			corpus.getContent().add(d.vmlRect(
					"position:absolute;margin-left:0;margin-top:719.35pt;width:300pt;height:40pt;"
					+ "z-index:2;mso-position-vertical-relative:text",
					"CORPUSPAGE shape text at margin-top 719.35pt"));
			P on = Doc.plainParagraph("", SERIF, 20);
			on.getContent().clear();
			on.getContent().add(d.vmlRect(
					"position:absolute;margin-left:0;margin-top:-300pt;width:300pt;height:40pt;"
					+ "z-index:3;mso-position-vertical-relative:text",
					"ONPAGE shape text at margin-top -300pt"));
			d.addFooter(org.docx4j.wml.HdrFtrRef.DEFAULT, java.util.Arrays.asList(
					Doc.plainParagraph("FOOTER line", SANS, 20), off, corpus, on));
			d.para("The footer of every page holds three VML v:rect shapes, each with a line "
					+ "of text in it: one at margin-top 900pt, which is more than the "
					+ "page's whole 842pt height, one at the corpus's own margin-top "
					+ "719.35pt, and one at margin-top -300pt, which is on the page. "
					+ prose(2)).after(240).add();
			for (int i = 0; i < 30; i++) {
				d.para(prose(3, i)).after(160).add();
			}
			return d.pkg();
		}));
		/*
		 * Two anchored drawings sharing a band (CR-001 batch 46 item 3, section 3.6).  That
		 * batch built the reserving container which lets two wrapTopAndBottom anchors in one
		 * paragraph sit side by side instead of stacking, and read Word's own page for the
		 * pair it was aimed at - but three things it had to guess at are still guesses:
		 * whether Word aligns the two tops when their positionV offsets differ, what it does
		 * when the two boxes overlap in x or touch exactly at the boundary of the
		 * disjointness test, and whether a band can hold anchors from two different
		 * paragraphs.  One document, four cases, each on a 451.3pt column (A4, 1in margins).
		 *
		 *   (a) both fit: 200pt pictures at posOffset 0 and 240pt, positionV 10pt and 12pt -
		 *       deliberately unequal, which is the case the batch found Word does not align
		 *   (b) they overlap by 50pt: 200pt at 0 and 200pt at 150pt
		 *   (c) they touch exactly: 200pt at 0 and 200pt at 200pt
		 *   (d) the pair of (a) split over two paragraphs, one short text paragraph between
		 *
		 * positionH is relativeFrom="column" (not the "margin" the older anchor probes use),
		 * because a posOffset from the column is what the corpus documents carry.  Each
		 * picture is numbered in its own fill, so the two members of a pair are told apart
		 * in the page image as well as by their extents.  Read off the golden: the x and y
		 * of each picture, whether a pair shares a y or keeps its own offsets, how much
		 * vertical space the pair takes from the text after it, and for (d) whether Word
		 * still draws them side by side across the paragraph boundary.
		 */
		PROBES.add(new Probe("anchor-side-by-side",
				"two wp:anchor pictures with wrapTopAndBottom in one paragraph, positionH "
				+ "relativeFrom column and positionV relativeFrom paragraph, on a 451.3pt "
				+ "column: (a) 200pt at posOffset 0 and 200pt at 240pt, positionV 10pt and "
				+ "12pt (both fit, unequal tops); (b) the same at 0 and 150pt (they overlap "
				+ "by 50pt); (c) at 0 and 200pt (they touch exactly); (d) the pair of (a) "
				+ "anchored in two paragraphs with a short text paragraph between them - "
				+ "each picture's x and y, whether a pair shares a y or keeps its own "
				+ "offsets, what the text after it starts at, and whether a band crosses a "
				+ "paragraph boundary", () -> {
			Doc d = Doc.create(15);
			final long PT = 12700;   // EMU per point
			d.para("Four cases. Every picture is 200pt wide and 100pt tall, anchored with "
					+ "wrapTopAndBottom, positionH relativeFrom column and positionV "
					+ "relativeFrom paragraph, in a 451.3pt column. Each carries its own "
					+ "number. Read off each picture's x and y, whether the two members of "
					+ "a pair share a y, and where the text after them starts.")
					.after(240).add();

			d.para().noLabel()
					.run(d.anchoredImage(200, 100, 200 * PT, 100 * PT, "topAndBottom", null,
							0, 10 * PT, "column", 1))
					.run(d.anchoredImage(200, 100, 200 * PT, 100 * PT, "topAndBottom", null,
							240 * PT, 12 * PT, "column", 2))
					.text("(a) both fit: picture 1 at posOffset 0pt and picture 2 at 240pt, "
							+ "positionV 10pt and 12pt.").add();
			d.para("(a) after: the short text paragraph which follows case (a). " + prose(1, 0))
					.after(240).add();

			d.para().noLabel()
					.run(d.anchoredImage(200, 100, 200 * PT, 100 * PT, "topAndBottom", null,
							0, 10 * PT, "column", 3))
					.run(d.anchoredImage(200, 100, 200 * PT, 100 * PT, "topAndBottom", null,
							150 * PT, 12 * PT, "column", 4))
					.text("(b) they overlap by 50pt: picture 3 at posOffset 0pt and picture "
							+ "4 at 150pt, positionV 10pt and 12pt.").add();
			d.para("(b) after: the short text paragraph which follows case (b). " + prose(1, 1))
					.after(240).add();

			d.para().noLabel()
					.run(d.anchoredImage(200, 100, 200 * PT, 100 * PT, "topAndBottom", null,
							0, 10 * PT, "column", 5))
					.run(d.anchoredImage(200, 100, 200 * PT, 100 * PT, "topAndBottom", null,
							200 * PT, 12 * PT, "column", 6))
					.text("(c) they touch exactly: picture 5 at posOffset 0pt and picture 6 "
							+ "at 200pt, positionV 10pt and 12pt.").add();
			d.para("(c) after: the short text paragraph which follows case (c). " + prose(1, 2))
					.after(240).add();

			d.para().noLabel()
					.run(d.anchoredImage(200, 100, 200 * PT, 100 * PT, "topAndBottom", null,
							0, 10 * PT, "column", 7))
					.text("(d) first paragraph: picture 7 at posOffset 0pt, positionV 10pt.").add();
			d.para("(d) between: one short text paragraph, between the two anchored "
					+ "paragraphs. " + prose(1, 3)).add();
			d.para().noLabel()
					.run(d.anchoredImage(200, 100, 200 * PT, 100 * PT, "topAndBottom", null,
							240 * PT, 12 * PT, "column", 8))
					.text("(d) second paragraph: picture 8 at posOffset 240pt, positionV 12pt.").add();
			d.para("(d) after: the short text paragraph which follows case (d). " + prose(1, 4))
					.after(240).add();
			return d.pkg();
		}));

		/*
		 * Toggle properties at more than one level of the style hierarchy (ECMA-376-1
		 * 17.7.3; CR-001 batch 46 item 4, Step B).  StyleUtil.toggle now XORs the twelve,
		 * with two deliberate narrowings of the letter of the spec - an explicit false is
		 * applied rather than XORed, and an upper level which is silent about the property
		 * is no boundary at all, so the document defaults' true is not forced back on.  Both
		 * narrowings were chosen on argument: nothing in 449 corpus documents tells either
		 * of them from the letter, and its Javadoc says a probe settles them.  This is the
		 * probe, and it is two documents because w:docDefaults is a document property.
		 *
		 * toggle-levels, document defaults NOT bold:
		 *   1  a bold paragraph style with a run in a character style <w:b w:val="0"/>
		 *      (narrowing one: expect regular, which the letter would not give)
		 *   2  14924's own shape - a table style whose firstCol condition is w:b, a cell
		 *      with w:cnfStyle firstCol, a run in a character style which is also w:b
		 *      (two levels true: expect regular)
		 *   3  the same cell with a plain run (one level true: expect bold)
		 *   4  a bold paragraph style with a bold character style (two levels true)
		 *   5  the controls: a plain Normal paragraph, and the bold paragraph style alone
		 *
		 * toggle-levels-docdefaults, w:docDefaults rPr w:b:
		 *   6  a paragraph style <w:b w:val="0"/> with a plain run
		 *   7  the same with a run in a character style which is silent on the weight (it
		 *      sets only w:color) - narrowing two: expect the same as 6, where the letter
		 *      would make it bold
		 *   8  a bold paragraph style with a plain run (document defaults true, one style
		 *      level true)
		 *   9  the control: a Normal paragraph
		 *
		 * Every case's line says what it is and is a single run, so a line is wholly one
		 * weight.  Read off the goldens: which lines Word draws in the bold face.
		 */
		PROBES.add(new Probe("toggle-levels",
				"ECMA-376-1 17.7.3 toggle properties over levels of the style hierarchy, "
				+ "document defaults NOT bold: (1) a bold paragraph style with a run in a "
				+ "character style w:b w:val=0; (2) a table style whose firstCol condition "
				+ "is w:b, a cell with w:cnfStyle firstCol, and a run in a character style "
				+ "which is w:b (14924's shape); (3) the same cell with a plain run; (4) a "
				+ "bold paragraph style with a bold character style; (5) a plain Normal "
				+ "paragraph and the bold paragraph style alone, as controls - which lines "
				+ "does Word draw in the bold face", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(SERIF, 24);
			d.addParagraphStyle("ProbeBold", "Normal", ppr -> { }, Doc::bold);
			addCharacterStyle(d, "ProbeUnbold", rpr -> rpr.setB(flag(false)));
			addCharacterStyle(d, "ProbeStrong", Doc::bold);
			addTableStyle(d, "ProbeTableStyle", null);
			addTableStyleCondition(d, "ProbeTableStyle",
					org.docx4j.wml.STTblStyleOverrideType.FIRST_COL, Doc::bold);

			d.para("Document defaults are NOT bold. Every case below is one run, so each "
					+ "line is wholly one weight; the line says which case it is.")
					.after(240).add();

			d.para().noLabel().style("ProbeBold").run(bareRun(
					"case 1: paragraph style ProbeBold is w:b, the run is in character style "
					+ "ProbeUnbold which is w:b w:val=0 - one level true, one explicitly false",
					rStyle("ProbeUnbold"))).add();

			P case2 = d.para().noLabel().run(bareRun(
					"case 2: firstCol table-style condition w:b, run in character style "
					+ "ProbeStrong which is w:b - two levels true", rStyle("ProbeStrong"))).build();
			P case3 = d.para().noLabel().run(bareRun(
					"case 3: the same firstCol cell, a plain run - one level true", null)).build();
			P note2 = d.para().noLabel().run(bareRun(
					"case 2 note: this cell is not firstCol, so no condition applies to it", null)).build();
			P note3 = d.para().noLabel().run(bareRun(
					"case 3 note: this cell is not firstCol either", null)).build();
			Doc.Table t = new Doc.Table(4600, 4400).tblLook(false, false, true, false);
			t.rowOf(null, null,
					Doc.Table.cnfStyle(t.cellOf(4600, null, case2), "001000000000"),
					t.cellOf(4400, null, note2));
			t.rowOf(null, null,
					Doc.Table.cnfStyle(t.cellOf(4600, null, case3), "001000000000"),
					t.cellOf(4400, null, note3));
			Tbl tbl = t.build();
			tableStyle(tbl, "ProbeTableStyle");
			d.add(tbl);

			d.para().noLabel().style("ProbeBold").before(240).run(bareRun(
					"case 4: paragraph style ProbeBold is w:b and the run is in character "
					+ "style ProbeStrong which is w:b - two levels true", rStyle("ProbeStrong"))).add();
			d.para().noLabel().run(bareRun(
					"case 5a: Normal paragraph, plain run, no level states the weight - the "
					+ "regular control", null)).add();
			d.para().noLabel().style("ProbeBold").run(bareRun(
					"case 5b: paragraph style ProbeBold is w:b, plain run - the bold control",
					null)).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("toggle-levels-docdefaults",
				"ECMA-376-1 17.7.3 toggle properties with w:docDefaults rPr w:b: (6) a "
				+ "paragraph style w:b w:val=0 with a plain run; (7) the same with a run in "
				+ "a character style which is silent on the weight (it sets only w:color); "
				+ "(8) a bold paragraph style with a plain run - document defaults true and "
				+ "one style level true; (9) a Normal paragraph, as the control - which "
				+ "lines does Word draw in the bold face", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(SERIF, 24);
			Doc.bold(docDefaultsRPr(d));
			d.addParagraphStyle("ProbeUnbold", "Normal", ppr -> { }, rpr -> rpr.setB(flag(false)));
			d.addParagraphStyle("ProbeBold", "Normal", ppr -> { }, Doc::bold);
			addCharacterStyle(d, "ProbeColour", rpr -> {
				org.docx4j.wml.Color c = Doc.F.createColor();
				c.setVal("C00000");
				rpr.setColor(c);
			});

			d.para().noLabel().run(bareRun(
					"The document defaults are bold. Every case below is one run, so each "
					+ "line is wholly one weight; the line says which case it is.", null)).after(240).add();
			d.para().noLabel().style("ProbeUnbold").run(bareRun(
					"case 6: docDefaults bold, paragraph style ProbeUnbold is w:b w:val=0, "
					+ "plain run", null)).add();
			d.para().noLabel().style("ProbeUnbold").run(bareRun(
					"case 7: docDefaults bold, paragraph style ProbeUnbold is w:b w:val=0, "
					+ "run in character style ProbeColour which sets only w:color",
					rStyle("ProbeColour"))).add();
			d.para().noLabel().style("ProbeBold").run(bareRun(
					"case 8: docDefaults bold, paragraph style ProbeBold is w:b, plain run",
					null)).add();
			d.para().noLabel().run(bareRun(
					"case 9: docDefaults bold, Normal paragraph, plain run - the control",
					null)).add();
			return d.pkg();
		}));

		/*
		 * What the weight on a line does to its line box (CR-001 batch 46 Step A, section
		 * 5.4).  Corpus document 8132 is Times New Roman 9pt (Tinos on this machine), and
		 * its bold lines step 10.32pt baseline to baseline in Word where ours step 10.15pt -
		 * 0.17pt a line, which is enough to move an item from the bottom of one page to the
		 * top of the next five times in one document.  The 10.35pt step of its ordinary
		 * lines is not in dispute.  What is not known is which of the line's parts Word
		 * measures the box from: every run, the bold runs alone, or the paragraph mark, whose
		 * own w:b is what docx4j used to put on the body and no longer does.
		 *
		 * Five paragraphs at Times New Roman 9pt, then the same five at Calibri 11pt, each
		 * at least four lines, consecutive, w:line 240 auto with no space before or after,
		 * so both the step inside a paragraph and the step across its boundary are readable:
		 *
		 *   (a) every run regular, the paragraph mark silent on the weight
		 *   (b) every run bold, the paragraph mark bold
		 *   (c) runs of three words alternating bold and regular, so every line holds both
		 *   (d) every run regular, the paragraph mark <w:b/>        (against (a))
		 *   (e) every run bold, the paragraph mark <w:b w:val="0"/> (against (b))
		 *
		 * Every paragraph mark carries the paragraph's own w:rFonts and w:sz, so the only
		 * thing which varies between (a) and (d), and between (b) and (e), is the mark's
		 * weight.  Read off the golden: the baseline-to-baseline step within each paragraph
		 * and across each paragraph boundary, per case, at both sizes.
		 */
		PROBES.add(new Probe("line-box-bold",
				"the baseline-to-baseline step against the weight on the line, at Times New "
				+ "Roman 9pt and then Calibri 11pt, w:line 240 auto with no spacing: (a) "
				+ "every run regular; (b) every run bold; (c) three-word runs alternating "
				+ "bold and regular within every line; (d) regular runs under a bold "
				+ "paragraph mark; (e) bold runs under a w:b w:val=0 paragraph mark - the "
				+ "mark carries the paragraph's own font and size in every case, so only "
				+ "its weight varies (8132 reads Word 10.32pt for bold 9pt lines, ours "
				+ "10.15pt)", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(TIMES_NEW_ROMAN, 18);
			d.para("Five paragraphs at Times New Roman 9pt, then the same five at Calibri "
					+ "11pt. Each is at least four lines, single spaced, with no space "
					+ "before or after, so the step within a paragraph and the step across "
					+ "its boundary are both readable.").after(240).add();
			lineBoxCases(d, "9pt TNR", TIMES_NEW_ROMAN, 18);
			d.para("The same five cases at Calibri 11pt.").before(240).after(240).add();
			lineBoxCases(d, "11pt Calibri", "Calibri", 22);
			return d.pkg();
		}));

		/*
		 * A family no machine has, with a bold run in it (CR-001 batch 46 Step C, section
		 * 4.4b).  Corpus document 9919's theme names a minor Latin family neither Word's
		 * machine nor ours has; Word substituted Sylfaen and emitted it at FontWeight 400
		 * for the document's 1006 bold runs, with no bold resource of any kind, while the
		 * same PDF carries four other bold resources for families Word did have.  Two
		 * questions were left open there, and neither can be answered from that document:
		 * whether Word drops the weight for EVERY missing family or only on the last-resort
		 * path, and whether how the family is named changes it.
		 *
		 *   (a) "Plutext Probe Serif", named directly in w:rFonts, w:family roman with Times
		 *       New Roman's PANOSE-1 in the fontTable
		 *   (b) "Plutext Probe Sans", the same, w:family swiss with Arial's PANOSE-1
		 *   (c) "Plutext Probe Ghost", named directly, with NO fontTable entry at all
		 *   (d) "Plutext Probe Theme", the theme's minor Latin face, named by runs which
		 *       carry only w:asciiTheme/w:hAnsiTheme minorHAnsi - 9919's own shape.  Its
		 *       fontTable entry is (b)'s, so the only thing separating (b) from (d) is how
		 *       the run names the family
		 *
		 * Each case is a regular line, a bold line and a Calibri control line holding a
		 * regular and a bold run, so a case sits beside a family Word has.  Read off the
		 * golden's font resources: the face Word substitutes for each case, and whether the
		 * bold run gets a bold face or, as on 9919, weight 400.
		 */
		PROBES.add(new Probe("missing-family-weight",
				"two invented families no machine has, each with a regular and a bold run: "
				+ "(a) named directly in w:rFonts, w:family roman with Times New Roman's "
				+ "panose; (b) the same, w:family swiss with Arial's panose; (c) named "
				+ "directly with no w:fontTable entry at all; (d) the theme's minor Latin "
				+ "face, named by w:asciiTheme/w:hAnsiTheme minorHAnsi over (b)'s own "
				+ "fontTable entry (9919's shape); each case beside a Calibri control line "
				+ "of a regular and a bold run - which face does Word substitute, and does "
				+ "the bold run get a bold face or weight 400", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(SERIF, 22);
			fontTable(d,
					fontEntry("Plutext Probe Serif", "roman", PANOSE_TIMES, null)
					+ fontEntry("Plutext Probe Sans", "swiss", PANOSE_ARIAL, null)
					+ fontEntry("Plutext Probe Theme", "swiss", PANOSE_ARIAL, null));
			themePart(d, "Plutext Probe Theme", "Plutext Probe Theme");
			d.para("Four cases. Each is a regular line and a bold line in a family which "
					+ "exists on no machine, then a Calibri control line holding a regular "
					+ "run and a bold run.").after(240).add();
			missingFamilyCase(d, "(a)", "Plutext Probe Serif, w:rFonts, family roman",
					allFour("Plutext Probe Serif"));
			missingFamilyCase(d, "(b)", "Plutext Probe Sans, w:rFonts, family swiss",
					allFour("Plutext Probe Sans"));
			missingFamilyCase(d, "(c)", "Plutext Probe Ghost, w:rFonts, no fontTable entry",
					allFour("Plutext Probe Ghost"));
			missingFamilyCase(d, "(d)", "Plutext Probe Theme, minorHAnsi theme reference",
					rpr -> {
						org.docx4j.wml.RFonts rf = Doc.F.createRFonts();
						rf.setAsciiTheme(org.docx4j.wml.STTheme.MINOR_H_ANSI);
						rf.setHAnsiTheme(org.docx4j.wml.STTheme.MINOR_H_ANSI);
						rpr.setRFonts(rf);
					});
			return d.pkg();
		}));

		/*
		 * The numbering label's own width (CR-001 batch 47 item 5).  Until 17.2.0 docx4j
		 * estimated a label at 90 twips a character - 4.5pt, about a digit of an 11pt face -
		 * and that estimate decides the label column of a list block, the gap an inline label
		 * leaves before the text, and whether the numbering tab's stop falls past the end of
		 * the label.  It is wrong in both directions, so the three cases here are the two
		 * directions and a control:
		 *
		 *   numId 45  lvlText "Appendix %1" with w:suff space and w:ind left 0 hanging 0, in
		 *             a 20pt bold paragraph: the estimate gives ten characters plus a space,
		 *             990 twips, where the label is nearly twice that.  With the estimate the
		 *             label wrapped inside its own column ("Appendix" / "A") and the body was
		 *             set beside it
		 *   numId 46  lvlText "Section %1 of the Annex" at 11pt with w:ind left 720
		 *             hanging 360: the label is far wider than the hanging indent, so what follows it is decided
		 *             by w:suff (a tab here) rather than by the indent
		 *   numId 47  the control: "%1." at 11pt with the same left 720 hanging 360, where
		 *             the label fits the hanging indent comfortably
		 *
		 * Read off the golden: the x of the first glyph of the label and of the text after
		 * it, on each of the three paragraphs, and whether Word ever wraps a label inside its
		 * own column.
		 */
		PROBES.add(new Probe("list-label-width",
				"three numbered paragraphs whose labels are wide, wider than their hanging "
				+ "indent, and narrow: lvlText \"Appendix %1\" with w:suff space and no "
				+ "hanging indent in a 20pt bold paragraph, \"Section %1 of the Annex\" at "
				+ "11pt with w:ind left 720 hanging 360 - far wider than that indent - and "
				+ "\"%1.\" at 11pt as the control - the x of each label and of the text after "
				+ "it", () -> {
			Doc d = Doc.create(15);
			d.numberingXml(
					"<w:abstractNum w:abstractNumId=\"45\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ labelLevel(0, "Appendix %1", "space", 0, 0, 40) + "</w:abstractNum>"
					+ "<w:abstractNum w:abstractNumId=\"46\"><w:multiLevelType w:val=\"multilevel\"/>"
					+ labelLevel(0, "Section %1 of the Annex", "tab", 720, 360, 22) + "</w:abstractNum>"
					+ "<w:abstractNum w:abstractNumId=\"47\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ labelLevel(0, "%1.", "tab", 720, 360, 22) + "</w:abstractNum>"
					+ "<w:num w:numId=\"45\"><w:abstractNumId w:val=\"45\"/></w:num>"
					+ "<w:num w:numId=\"46\"><w:abstractNumId w:val=\"46\"/></w:num>"
					+ "<w:num w:numId=\"47\"><w:abstractNumId w:val=\"47\"/></w:num>");

			d.para("Three numbered paragraphs, each with a label of a different width against "
					+ "its own indent. Read the x of each label and of the first glyph of the "
					+ "text after it.").after(240).add();

			d.para("L1: the label is \"Appendix A\" in 20pt bold with w:suff space and no "
					+ "hanging indent, so what follows the label is one space after it. "
					+ prose(1, 0))
					.font(SERIF, 40).numPr(45, 0).markSize(40)
					.markRPr(pr -> pr.setB(new org.docx4j.wml.BooleanDefaultTrue()))
					.before(120).after(120).add();

			d.para("L2: the label is \"Section 1 of the Annex\" at 11pt, far wider than the "
					+ "360-twip hanging indent, so w:suff decides what follows it. " + prose(1, 1))
					.numPr(46, 0).before(120).after(120).add();

			d.para("L3 control: the label is \"1.\" at 11pt, narrower than the 360-twip "
					+ "hanging indent. " + prose(1, 2))
					.numPr(47, 0).before(120).after(120).add();

			d.para("after. " + prose(1, 3)).before(240).add();
			return d.pkg();
		}));

		/*
		 * 9919's Sylfaen-400 path (CR-001 batch 47 close-out, the probes waiting for a
		 * golden; ledger6 Amendment D).  missing-family-weight settled that Word substitutes
		 * an absent family by its fontTable w:family and KEEPS the weight, via w:rFonts or the
		 * theme alike - so 9919, whose 1006 bold runs Word drew in Sylfaen at weight 400 with
		 * no bold resource at all, is a different path.  Its fontTable entry differs from the
		 * probe's case (d) in two things, and its family name is a third candidate:
		 *
		 *   (a) "Plutext Probe Alt", w:family swiss with Arial's panose, and a w:altName
		 *       naming a SECOND absent family, "Plutext Probe Alt Two"
		 *   (b) "Plutext Probe Thin", w:family swiss with a LIGHT panose - weight digit 03
		 *       (020B0304020202020204) where Arial's is 06
		 *   (c) "Plutext Probe Light", w:family swiss with Arial's panose, the family NAMED as
		 *       a light face
		 *   (d) all three at once, through the theme's minor Latin face as 9919 names it
		 *
		 * Each case is a regular line, a bold line and a Calibri control line.  Read off the
		 * golden's font resources: the face Word substitutes for each case and the weight
		 * its bold run gets - 700 with a bold resource, as missing-family-weight found, or
		 * 400 with none, as 9919's.
		 */
		PROBES.add(new Probe("missing-family-weight-2",
				"four more invented families, each with a regular and a bold run beside a "
				+ "Calibri control: (a) a fontTable w:altName naming a second absent family; "
				+ "(b) a light panose (weight digit 03); (c) a family named \"... Light\"; "
				+ "(d) all three through the theme's minor Latin face, 9919's route - which "
				+ "face does Word substitute, and does the bold run get a bold face or "
				+ "weight 400", () -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun(SERIF, 22);
			fontTable(d,
					fontEntry("Plutext Probe Alt", "swiss", PANOSE_ARIAL, "Plutext Probe Alt Two")
					+ fontEntry("Plutext Probe Thin", "swiss", PANOSE_ARIAL_LIGHT, null)
					+ fontEntry("Plutext Probe Light", "swiss", PANOSE_ARIAL, null)
					+ fontEntry("Plutext Probe Theme Light", "swiss", PANOSE_ARIAL_LIGHT,
							"Plutext Probe Theme Light Two"));
			themePart(d, "Plutext Probe Theme Light", "Plutext Probe Theme Light");
			d.para("Four cases. Each is a regular line and a bold line in a family which "
					+ "exists on no machine, then a Calibri control line holding a regular "
					+ "run and a bold run.").after(240).add();
			missingFamilyCase(d, "(a)", "Plutext Probe Alt, w:altName to a second absent family",
					allFour("Plutext Probe Alt"));
			missingFamilyCase(d, "(b)", "Plutext Probe Thin, light panose 020B03",
					allFour("Plutext Probe Thin"));
			missingFamilyCase(d, "(c)", "Plutext Probe Light, a family named Light",
					allFour("Plutext Probe Light"));
			missingFamilyCase(d, "(d)", "Plutext Probe Theme Light, minorHAnsi theme reference "
					+ "with altName and light panose",
					rpr -> {
						org.docx4j.wml.RFonts rf = Doc.F.createRFonts();
						rf.setAsciiTheme(org.docx4j.wml.STTheme.MINOR_H_ANSI);
						rf.setHAnsiTheme(org.docx4j.wml.STTheme.MINOR_H_ANSI);
						rpr.setRFonts(rf);
					});
			return d.pkg();
		}));

		/*
		 * The USE_TYPO_METRICS rule (docx4j eb00c6be2).  Word lays Aptos out on its typo box
		 * (13.45pt at 11pt, measured), not its usWin box (14.13); Aptos' typo and hhea boxes
		 * are equal, so that measurement cannot say whether Word takes the TYPO box or the
		 * HHEA box for a font which sets the flag.  Bierstadt can: its Regular face sets the
		 * flag with sTypo 1520/-532/410 = 2462 against hhea 1802/-539/0 = 2341 and usWin
		 * 1802 + 539 = 2341, so at 11pt the typo rule gives 13.22pt a line and the hhea or
		 * usWin rule 12.57.  DokChampa sets the bit in an OS/2 table of VERSION 3, where the
		 * flag is not defined (typo 2754, hhea and usWin 3967 - 14.79pt against 21.31): does
		 * Word read the bit regardless of the version?  Aptos and Calibri are the controls.
		 *
		 * Twenty single-spaced lines in each face, w:line 240 auto, no spacing.  Read off
		 * the golden: the baseline step in each block.
		 */
		PROBES.add(new Probe("line-box-typo-metrics",
				"the baseline-to-baseline step of twenty single-spaced 11pt lines in "
				+ "Bierstadt (USE_TYPO_METRICS set; typo box 2462 of 2048 against hhea and "
				+ "usWin 2341: 13.22pt against 12.57), DokChampa (the bit set in an OS/2 "
				+ "version 3 table: typo 2754 against 3967), and the controls Aptos (typo = "
				+ "hhea 2500 against usWin 2631) and Calibri (no flag, 2500 either way)",
				() -> {
			Doc d = Doc.create(15);
			d.documentDefaultRun("Calibri", 22);
			d.para("Four blocks of twenty single-spaced 11pt lines, one face each, with a "
					+ "spaced Calibri heading line before each: Bierstadt, DokChampa, Aptos, "
					+ "Calibri. Read the baseline step within each block.").after(240).add();
			String[][] faces = {
				{ "Bierstadt", "T1 Bierstadt 11pt: USE_TYPO_METRICS set, typo 2462 against hhea and usWin 2341" },
				{ "DokChampa", "T2 DokChampa 11pt: the bit set in an OS/2 version 3 table, typo 2754 against 3967" },
				{ "Aptos", "T3 Aptos 11pt: the flag set, typo and hhea both 2500 against usWin 2631" },
				{ "Calibri", "T4 Calibri 11pt: no flag, 2500 every way" } };
			for (String[] f : faces) {
				d.para(f[1]).before(240).after(120).add();
				for (int i = 0; i < 20; i++) {
					d.para(f[0] + " line " + (i + 1) + ": " + prose(1, i)).font(f[0], 22)
							.markRPr(markFont(f[0], 22, null)).add();
				}
			}
			return d.pkg();
		}));

		/*
		 * The numbering tab's dot leader takes its step from which face and size?  Item 5(b)
		 * gives the leader the PARAGRAPH's face and size, and tab-leader-in-cell-2 cannot
		 * separate that from "Arial at the label's size": Arial's period at the label's
		 * 11.04pt (3.069pt) and Liberation Serif's at the paragraph's 12pt (3.000pt) round
		 * to the same 13 grid cells, and the step is what decides the dot count.  So here the
		 * two sizes are far apart in both directions:
		 *
		 *   numId 48  the level's own w:rPr at 8pt under a 16pt paragraph
		 *   numId 49  the level's own w:rPr at 16pt under an 8pt paragraph
		 *   numId 50  the level's own w:rPr at 8pt, and the level's w:rFonts Arial, under a
		 *             16pt Times New Roman paragraph - the face and the size both differing
		 *
		 * Each once in the body, the stop at 1440 twips under w:ind left 2880 hanging 2520 as
		 * in tab-leader-in-cell-2.  Read off the golden: the dot count and step of each run.
		 */
		PROBES.add(new Probe("tab-leader-sizes",
				"the numbering tab's dot leader where the label's size differs from the "
				+ "paragraph's: the level's w:rPr at 8pt under a 16pt paragraph, at 16pt "
				+ "under an 8pt paragraph, and at 8pt Arial under a 16pt Times New Roman "
				+ "paragraph, each with a w:leader=\"dot\" stop at 1440 twips under w:ind "
				+ "left 2880 hanging 2520 - the dot count and step of each run", () -> {
			Doc d = Doc.create(15);
			d.numberingXml(
					"<w:abstractNum w:abstractNumId=\"48\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ sizedLeaderLevel(0, 1440, "dot", 2880, 2520, 16, null) + "</w:abstractNum>"
					+ "<w:abstractNum w:abstractNumId=\"49\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ sizedLeaderLevel(0, 1440, "dot", 2880, 2520, 32, null) + "</w:abstractNum>"
					+ "<w:abstractNum w:abstractNumId=\"50\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ sizedLeaderLevel(0, 1440, "dot", 2880, 2520, 16, "Arial") + "</w:abstractNum>"
					+ "<w:num w:numId=\"48\"><w:abstractNumId w:val=\"48\"/></w:num>"
					+ "<w:num w:numId=\"49\"><w:abstractNumId w:val=\"49\"/></w:num>"
					+ "<w:num w:numId=\"50\"><w:abstractNumId w:val=\"50\"/></w:num>");
			d.para("Three numbered paragraphs whose label size differs from the paragraph's, "
					+ "each with a dot leader from the label to a stop at 1440 twips. Read the "
					+ "dot count and the step of each run.").after(240).add();
			d.para("S1: label 8pt, paragraph 16pt. " + prose(1, 0)).font(SERIF, 32)
					.numPr(48, 0).markRPr(markFont(SERIF, 32, null)).before(120).after(120).add();
			d.para("S2: label 16pt, paragraph 8pt. " + prose(1, 1)).font(SERIF, 16)
					.numPr(49, 0).markRPr(markFont(SERIF, 16, null)).before(120).after(120).add();
			d.para("S3: label 8pt Arial, paragraph 16pt Times New Roman. " + prose(1, 2))
					.font(TIMES_NEW_ROMAN, 32).numPr(50, 0)
					.markRPr(markFont(TIMES_NEW_ROMAN, 32, null)).before(120).after(120).add();
			d.para("after. " + prose(1, 3)).before(240).add();
			return d.pkg();
		}));

		/*
		 * The numbering tab's leader of a kind other than dot (item 5(b)'s unmeasured
		 * mapping): w:leader hyphen, underscore, heavy and middleDot on the level's own stop,
		 * which docx4j maps to leader-pattern="rule" for the first three as the paragraph-tab
		 * path does, and to dots for middleDot.  A rule leader has no repeating unit, so the
		 * layout manager reports nothing painted and keeps the w:suff space.  Read off the
		 * golden: what Word paints for each kind, from where to where, and whether a space
		 * glyph precedes it.
		 */
		PROBES.add(new Probe("numbering-leader-kinds",
				"the numbering tab's leader of each kind Word has - hyphen, underscore, heavy "
				+ "and middleDot - on a stop at 1440 twips under w:ind left 2880 hanging "
				+ "2520, each once in the body, with the dot kind as the control: what Word "
				+ "paints for each, from where to where", () -> {
			Doc d = Doc.create(15);
			String[] kinds = { "dot", "hyphen", "underscore", "heavy", "middleDot" };
			StringBuilder num = new StringBuilder();
			for (int i = 0; i < kinds.length; i++) {
				/* The level names the label's own face and size, so the label does not lean
				 * on the theme default.  It did, and the two machines answer it differently:
				 * the probe has no theme part, Word 365 gives such a document Aptos and
				 * docx4j's built-in Office theme gives Calibri, so Word drew the label in
				 * Aptos 11.04pt and we in Calibri's clone Carlito at 11.00 - 8.349pt wide
				 * against Word's 9.06.  That is 0.71pt of the leader's advance, and at this
				 * probe's 3.60pt step it decided whether the run held eleven characters or
				 * twelve: 43.199pt of room is 11.9997 steps and 43.22 is 12.006.  Aptos has
				 * no metric clone, so on a machine without it the count could not be Word's
				 * whatever the exporter did - the probe was measuring the theme default and
				 * the font substitution, not the leader.  Liberation Serif is on both
				 * machines.  (CR-001 batch 48 item 7; the golden wants re-cutting) */
				num.append("<w:abstractNum w:abstractNumId=\"" + (51 + i) + "\"><w:multiLevelType w:val=\"hybridMultilevel\"/>"
						+ sizedLeaderLevel(0, 1440, kinds[i], 2880, 2520, 22, "Liberation Serif")
						+ "</w:abstractNum>");
			}
			for (int i = 0; i < kinds.length; i++) {
				num.append("<w:num w:numId=\"" + (51 + i) + "\"><w:abstractNumId w:val=\"" + (51 + i) + "\"/></w:num>");
			}
			d.numberingXml(num.toString());
			d.para("Five numbered paragraphs, one leader kind each on the level's stop at "
					+ "1440 twips: dot as the control, then hyphen, underscore, heavy and "
					+ "middleDot. Read what is painted between the label and the text.")
					.after(240).add();
			for (int i = 0; i < kinds.length; i++) {
				d.para("K" + (i + 1) + " w:leader " + kinds[i] + ": " + prose(1, i))
						.numPr(51 + i, 0).before(120).after(120).add();
			}
			d.para("after. " + prose(1, 5)).before(240).add();
			return d.pkg();
		}));

		/*
		 * CR-001 batch 48 item 3.  The two break opportunities corpus document 5253
		 * measures, each with its control, on a narrow measure so that whether the break
		 * is taken decides the line.  UAX #14 rule LB25 keeps a hyphen with the digits
		 * after it, and Word breaks there; FOP's pair table, which predates Unicode 8.0's
		 * LB24, breaks between a letter and a per-cent sign, and Word does not.  Read off
		 * the golden: for each pair, which of the two lines the token's tail is on.
		 */
		PROBES.add(new Probe("break-opportunities",
				"a hyphen before digits and a per-cent sign after a letter, each against "
				+ "its control - a hyphen before letters, a per-cent sign after digits, an "
				+ "en dash and a solidus - straddling a 120.0pt measure: where Word breaks "
				+ "and where it does not", () -> {
			Doc d = Doc.create(15);
			// A4 portrait with 4753-twip side margins: a body measure of 2400 twips = 120.0pt
			d.pageGeometry(11906, 16838, false, 1440, 4753, 1440, 4753);
			final int measure = 2400;
			final int advTw = Doc.advanceTwipsCeil("0", SERIF, 24);   // 120, as break-longword
			if (advTw != 120) throw new IllegalStateException("Liberation Serif's digit advance "
					+ "is " + advTw + " twips, not the 120 this probe's padding is built on");

			d.para("Each pair below is a padding run of digits - every one exactly 120 twips "
					+ "wide in Liberation Serif at 12pt - then a token which straddles the "
					+ "2400-twip (120.0pt) measure. Read where the token's tail goes: on the "
					+ "first line, or the second.").after(240).add();

			// token, what it tests, and where the break under test is (0-based, before this
			// character); the pad is sized so the break under test sits past the measure
			String[][] cases = {
				{ "1997-05-12", "A", "a hyphen before digits: LB25 keeps them together and "
						+ "Word breaks after the hyphen", "5" },
				{ "alpha-beta", "B", "a hyphen before letters, the control: both break after "
						+ "the hyphen", "6" },
				{ "VAT% paid", "C", "a per-cent sign after a letter: FOP's pair table breaks "
						+ "before the sign and Word does not", "3" },
				{ "100% paid", "D", "a per-cent sign after digits, the control: neither "
						+ "breaks before the sign", "3" },
				{ "alpha–beta", "E", "an en dash, the control: class BA, both break "
						+ "after it", "6" },
				{ "alpha/beta", "F", "a solidus, the control: FOP breaks after it and Word "
						+ "does not (word-layout-rules 4.3)", "6" },
			};
			for (String[] c : cases) {
				String token = c[0];
				String tag = c[1];
				int at = Integer.parseInt(c[3]);
				// the head up to the break under test must fit and the tail must not: pad so
				// that the head ends 60 twips (half a digit) inside the measure
				int headTw = Doc.advanceTwipsCeil(token.substring(0, at), SERIF, 24);
				int spaceTw = Doc.advanceTwipsCeil(" ", SERIF, 24);
				int padChars = Math.max(1, (measure - headTw - spaceTw - 60) / advTw);
				String pad = digitToken(padChars);
				int startTw = padChars * advTw + spaceTw;
				d.para(tag + ": " + c[2] + ". The padding is " + padChars + " characters = "
						+ (padChars * advTw / 20.0) + "pt, so the token opens at "
						+ (startTw / 20.0) + "pt and the character under test would fall at "
						+ ((startTw + headTw) / 20.0) + "pt of the 120.0pt measure.")
						.after(60).add();
				d.para().noLabel().text(pad + " " + token).after(180).add();
			}
			d.para("after.").before(240).add();
			return d.pkg();
		}));

		// ---------------------------------------------------------------- CR-001 batch 48
		//                                   item 9: where a left paragraph border stands
		//                                   when the first line is hung out to its left

		PROBES.add(new Probe("border-hanging",
				"a left paragraph border against a hanging indent, a numbered paragraph "
				+ "and a w:firstLine control: whether Word stands the bar against the "
				+ "leftmost text edge or against w:ind left", () -> {
			Doc d = Doc.create(15);
			d.numberingXml("<w:abstractNum w:abstractNumId=\"70\">"
					+ "<w:multiLevelType w:val=\"hybridMultilevel\"/>"
					+ Doc.decimalLevel(0, null, 1134, 1134)
					+ "</w:abstractNum>"
					+ "<w:num w:numId=\"70\"><w:abstractNumId w:val=\"70\"/></w:num>");

			// the bordered paragraphs are kept apart by an unbordered one, so that the
			// exporter's borders container does not merge them into one bar
			String tail = "This paragraph is long enough to wrap, so the golden shows the "
					+ "continuation lines' left edge as well as the first line's. "
					+ Doc.prose(2);

			d.para("Each bordered paragraph below carries a left w:pBdr of w:sz 18 "
					+ "(2.25pt) and w:space 8, and nothing on the other three sides. Read "
					+ "the bar's x against the paragraph's two text edges: the first "
					+ "line's and the continuation lines'.").after(240).add();

			d.para("A: w:ind left 1134 hanging 1134, so the first line starts at the "
					+ "margin and the lines after it 56.7pt in.").after(60).add();
			d.para(tail).indent(1134, 0, 1134).leftBorder(18, 8).after(180).add();

			d.para("B: the same indent from a numbering level (w:ind left 1134 hanging "
					+ "1134 on the level), so the label stands where A's first line "
					+ "does.").after(60).add();
			d.para(tail).numPr(70, 0).leftBorder(18, 8).after(180).add();

			d.para("C: w:ind left 1134 firstLine 567, the control: the first line is "
					+ "indented FORWARD, so w:ind left is the leftmost text edge.")
					.after(60).add();
			d.para(tail).indent(1134, 567, 0).leftBorder(18, 8).after(180).add();

			d.para("D: w:ind left 1134 and no first-line indent at all, the second "
					+ "control.").after(60).add();
			d.para(tail).indent(1134, 0, 0).leftBorder(18, 8).after(180).add();

			d.para("E: w:ind left 1134 hanging 1134 with w:space 0, so the bar has no "
					+ "gap to stand off by.").after(60).add();
			d.para(tail).indent(1134, 0, 1134).leftBorder(18, 0).after(180).add();

			d.para("after.").before(240).add();
			return d.pkg();
		}));

		// ---------------------------------------------------------------- CR-001 batch 48
		//                                   item 4: a VML text box whose text Word turns
		//                                   on its side, and the table cell that does it

		PROBES.add(new Probe("vml-textbox-vertical",
				"a VML v:rect text box of layout-flow:vertical, one of them with "
				+ "mso-layout-flow-alt:bottom-to-top, against a horizontal box of the same "
				+ "shape and a w:textDirection btLr table cell beside them: which way the "
				+ "text runs, what measure it is laid along, and where the first character "
				+ "stands", () -> {
			Doc d = Doc.create(15);

			// 1139's shape: a narrow tall legend beside a table, 16.5pt wide and 93.75pt
			// high, whose label is 17 characters long
			final String LABEL = "Vertical legend A";
			final String BOX = "position:absolute;margin-left:0;margin-top:%s;width:16.5pt;"
					+ "height:93.75pt;z-index:%d;mso-position-vertical-relative:text%s";

			d.para("Each box below is 16.5pt wide and 93.75pt high, so a horizontal line in "
					+ "it has 16.5pt of measure and a vertical one 93.75pt. Read which way "
					+ "the text runs and how many lines it takes.").after(240).add();

			d.para("A: layout-flow:vertical, which Word reads as its tbRl - the text runs "
					+ "down the box.").after(60).add();
			P a = Doc.plainParagraph("", SERIF, 20);
			a.getContent().clear();
			a.getContent().add(d.vmlRect(String.format(BOX, "0", 1, ""),
					"layout-flow:vertical", LABEL));
			d.add(a);
			d.para("A tail paragraph, so the box has a line to be anchored to.")
					.before(120).after(240).add();

			d.para("B: layout-flow:vertical with mso-layout-flow-alt:bottom-to-top, Word's "
					+ "btLr - the text runs up the box.").after(60).add();
			P b = Doc.plainParagraph("", SERIF, 20);
			b.getContent().clear();
			b.getContent().add(d.vmlRect(String.format(BOX, "0", 2, ""),
					"layout-flow:vertical;mso-layout-flow-alt:bottom-to-top",
					"Vertical legend B"));
			d.add(b);
			d.para("A tail paragraph, so the box has a line to be anchored to.")
					.before(120).after(240).add();

			d.para("C: the control, no layout-flow at all - the same box laid out across "
					+ "its 16.5pt width.").after(60).add();
			P c = Doc.plainParagraph("", SERIF, 20);
			c.getContent().clear();
			c.getContent().add(d.vmlRect(String.format(BOX, "0", 3, ""), "Horizontal C"));
			d.add(c);
			d.para("A tail paragraph, so the box has a line to be anchored to.")
					.before(120).after(240).add();

			d.para("D: the same rotation as a table asks for it, w:textDirection btLr in "
					+ "the first cell of a two-cell row 93.75pt (1875 twips) tall.")
					.after(60).add();
			Doc.Table t = new Doc.Table(330, 5000).fixedLayout().borders(4);
			t.rowOf(1875, org.docx4j.wml.STHeightRule.EXACT,
					Doc.Table.textDirection(t.cell("Vertical legend D", SERIF, 20, 1, 330), "btLr"),
					t.cell("The cell beside it, whose text is horizontal. " + Doc.prose(1),
							SERIF, 20, 1, 5000));
			d.add(t.build());

			d.para("after.").before(240).add();
			return d.pkg();
		}));

		// ---------------------------------------------------------------- CR-001 batch 49
		//                                   item 0b: the theme faces of a package that has
		//                                   no theme part at all.  docx4j's built-in Office
		//                                   theme is the pre-2024 one (Calibri / Cambria);
		//                                   Word 365's is Aptos / Aptos Display.  Which one
		//                                   Word actually draws here is what the golden says.

		PROBES.add(new Probe("theme-fonts-no-theme-part",
				"a package with NO theme part whose runs reference the theme faces - "
				+ "w:asciiTheme minorHAnsi (the body slot), majorHAnsi (the heading slot), "
				+ "and a run with no w:rFonts at all, so what resolves is the document "
				+ "defaults' own minorHAnsi reference - each as a line of digits and a line "
				+ "of lower-case letters at 11pt, against controls in an explicit Calibri "
				+ "and an explicit Cambria: WHICH FACE Word draws each line in, and what the "
				+ "line advances to", () -> {
			// createPackage puts a theme part in since 17.2.0; this probe's whole subject
			// is a package that has none, and its docx must stay the one Word's golden was
			// cut from
			Doc d = Doc.create(15).noThemePart();

			// the same two strings in every case, so the advances compare directly; the
			// paragraph label (P01, P02, ...) keeps each extracted line distinct
			final String DIGITS = "0123456789 0123456789 0123456789";
			final String LOWER = "abcdefghijklmnopqrstuvwxyz abcdefghijklmnopqrstuvwxyz";

			d.para("This package has no theme part. Each pair of lines below is one way of "
					+ "asking for a theme face, at 11pt: read the FACE the glyphs are drawn "
					+ "in and the advance of the whole line, against the two explicit "
					+ "controls at the end.").after(240).add();

			d.para("A: w:rFonts w:asciiTheme=\"minorHAnsi\" w:hAnsiTheme=\"minorHAnsi\", "
					+ "the body slot, with no explicit face.").after(60).add();
			d.para().font(SERIF, 22)
					.run(Doc.themeRun(DIGITS, org.docx4j.wml.STTheme.MINOR_H_ANSI, 22)).add();
			d.para().font(SERIF, 22)
					.run(Doc.themeRun(LOWER, org.docx4j.wml.STTheme.MINOR_H_ANSI, 22))
					.after(180).add();

			d.para("B: w:rFonts w:asciiTheme=\"majorHAnsi\" w:hAnsiTheme=\"majorHAnsi\", "
					+ "the heading slot.").after(60).add();
			d.para().font(SERIF, 22)
					.run(Doc.themeRun(DIGITS, org.docx4j.wml.STTheme.MAJOR_H_ANSI, 22)).add();
			d.para().font(SERIF, 22)
					.run(Doc.themeRun(LOWER, org.docx4j.wml.STTheme.MAJOR_H_ANSI, 22))
					.after(180).add();

			d.para("C: a run with no w:rPr at all, so its face and size come from the "
					+ "document defaults, whose w:rFonts is itself minorHAnsi and whose "
					+ "w:sz is 22.").after(60).add();
			d.para().font(SERIF, 22).bareText(DIGITS).add();
			d.para().font(SERIF, 22).bareText(LOWER).after(180).add();

			d.para("D: the control, an explicit w:ascii=\"Calibri\" at 11pt - what docx4j "
					+ "answers for the minorHAnsi slot of its built-in theme.")
					.after(60).add();
			d.para().font(SERIF, 22).run(DIGITS, "Calibri", 22, null).add();
			d.para().font(SERIF, 22).run(LOWER, "Calibri", 22, null).after(180).add();

			d.para("E: the second control, an explicit w:ascii=\"Cambria\" at 11pt - what "
					+ "docx4j answers for the majorHAnsi slot.").after(60).add();
			d.para().font(SERIF, 22).run(DIGITS, "Cambria", 22, null).add();
			d.para().font(SERIF, 22).run(LOWER, "Cambria", 22, null).after(180).add();

			d.para("after.").before(240).add();
			return d.pkg();
		}));


		// ---------------------------------------------------------------- CR-028 phase 1
		//                                   the Arabic ligature probe (CR-020 §8), generated:
		//                                   its font is EMBEDDED with FontEmbedder, where the
		//                                   hand-made original (make_probe.py, 2026-09-27)
		//                                   wrote an entry Word ignored (no w:sig).  Only
		//                                   registered when the font file is on this machine.

		File notoNaskh = new File(System.getProperty("fidelity.font.notoNaskhArabic",
				"/usr/share/fonts/noto/NotoNaskhArabic-Regular.ttf"));
		if (notoNaskh.isFile()) {
			PROBES.add(new Probe("ligatures-arabic",
					"w14:ligatures on Arabic w:cs runs: vowelled Allah (Noto liga), lillah (Noto dlig), bare "
					+ "Allah and a phrase, under absent/none/standard/contextual/discretional/standardContextual/all, "
					+ "in EMBEDDED Noto Naskh Arabic (FontEmbedder: obfuscated part, w:sig, w:embedTrueTypeFonts), "
					+ "Arial, Traditional Arabic and Arabic Typesetting, with a Calibri Latin control; the Latin "
					+ "rows say whether Word honours the setting at all, the Noto rows whether it applies it to "
					+ "Arabic, and the F0 rows draw in the embedded font only if the entry is one Word acts on", () -> {
				Doc d = Doc.create(15);
				d.documentDefaultRun("Calibri", 22);
				final String NOTO = "Noto Naskh Arabic";
				final String[] FONTS = { NOTO, "Arial", "Traditional Arabic", "Arabic Typesetting" };
				final org.docx4j.w14.STLigatures[] SETTINGS = { null, org.docx4j.w14.STLigatures.NONE,
						org.docx4j.w14.STLigatures.STANDARD, org.docx4j.w14.STLigatures.CONTEXTUAL,
						org.docx4j.w14.STLigatures.DISCRETIONAL, org.docx4j.w14.STLigatures.STANDARD_CONTEXTUAL,
						org.docx4j.w14.STLigatures.ALL };
				final String A = "\u0627\u0644\u0644\u0651\u0670\u0647";   // Allah, vowelled: Noto liga -> U+FDF2
				final String B = "\u0644\u0644\u0647";                       // lillah: Noto dlig
				final String C = "\u0627\u0644\u0644\u0647";                 // Allah, bare
				final String PHRASE = "\u0628\u0633\u0645 \u0627\u0644\u0644\u0647 \u0627\u0644\u0631\u062D\u0645\u0646 \u0627\u0644\u0631\u062D\u064A\u0645";
				final String LATIN = "office fi fl ffi ffl stick";

				org.docx4j.fonts.FontEmbedder.embed(d.pkg(), notoNaskh, NOTO, org.docx4j.fonts.FontEmbedder.Style.REGULAR);
				org.docx4j.wml.Fonts fonts = d.mdp().getFontTablePart().getContents();
				for (String f : new String[] { "Calibri", "Arial", "Traditional Arabic", "Arabic Typesetting" }) {
					org.docx4j.wml.Fonts.Font bare = new org.docx4j.wml.Fonts.Font();
					bare.setName(f);
					fonts.getFont().add(bare);
				}

				java.util.function.Function<org.docx4j.w14.STLigatures, java.util.function.Consumer<org.docx4j.wml.RPr>> lig = setting -> rpr -> {
					if (setting != null) {
						org.docx4j.w14.CTLigatures l = new org.docx4j.w14.CTLigatures();
						l.setVal(setting);
						rpr.setLigatures(l);
					}
				};
				d.para().noLabel().before(120).after(0).run("w14:ligatures Arabic probe. Each row: label \"F<font> S<setting>\", "
						+ "then the run. Texts: A=vowelled Allah (Noto liga), B=lillah (Noto dlig), C=bare Allah, then a phrase.",
						"Calibri", 16, null).add();
				for (int si = 0; si < SETTINGS.length; si++) {
					d.para().noLabel().before(120).after(0).run("L S" + si + " " + (SETTINGS[si] == null ? "absent" : SETTINGS[si].value())
							+ " Calibri", "Calibri", 16, null).add();
					d.para().noLabel().after(0).run(LATIN, "Calibri", 36, lig.apply(SETTINGS[si])).add();
				}
				for (int fi = 0; fi < FONTS.length; fi++) {
					for (int si = 0; si < SETTINGS.length; si++) {
						final org.docx4j.w14.STLigatures setting = SETTINGS[si];
						d.para().noLabel().before(120).after(0).run("F" + fi + " S" + si + " " + FONTS[fi] + " / "
								+ (setting == null ? "absent" : setting.value()), "Calibri", 16, null).add();
						P p = d.para().noLabel().after(0).run(A + " " + B + " " + C + " " + PHRASE, FONTS[fi], 36, rpr -> {
							rpr.setRtl(new BooleanDefaultTrue());
							org.docx4j.wml.HpsMeasure szCs = Doc.F.createHpsMeasure();
							szCs.setVal(BigInteger.valueOf(36));
							rpr.setSzCs(szCs);
							org.docx4j.wml.CTLanguage lang = Doc.F.createCTLanguage();
							lang.setBidi("ar-SA");
							rpr.setLang(lang);
							lig.apply(setting).accept(rpr);
						}).add();
						if (p.getPPr() == null) p.setPPr(Doc.F.createPPr());
						p.getPPr().setBidi(new BooleanDefaultTrue());
					}
				}
				return d.pkg();
			}));
		} else {
			System.err.println("ligatures-arabic not generated: no font at " + notoNaskh
					+ " (-Dfidelity.font.notoNaskhArabic=<path to NotoNaskhArabic-Regular.ttf>)");
		}

		// ---------------------------------------------------------------- CR-021 phase 0
		//                                   mc:AlternateContent: which branch Word draws, and
		//                                   what a list and a bound content control inside a
		//                                   text box do - measured with branches that DIFFER,
		//                                   so the drawn branch is read from the text itself

		PROBES.add(new Probe("mc-textbox-branches-numbered",
				"a text box written as mc:AlternateContent whose two branches DIFFER: the wps "
				+ "Choice holds two items of the body's list A (w:num 80) and one of a list B "
				+ "(w:num 81) seen nowhere else before it; the VML Fallback holds three of A and "
				+ "two of B; three body items of A before the box and three after, then one body "
				+ "item of B - which branch Word draws, what its items are numbered, and whether "
				+ "the body's count after the box is advanced by the box's paragraphs at all, "
				+ "once or twice (TraversalUtil visits both branches; numbering-stories says a "
				+ "text box is its own story)", () -> {
			Doc d = Doc.create(15);
			d.numberingXml(abstractDecimal(80) + num(80, 80) + abstractDecimal(81) + num(81, 81));
			d.para("Every numbered paragraph here is list A (w:num 80) unless it says list B "
					+ "(w:num 81). The text box's two branches differ, so the branch drawn is "
					+ "read from its text, and the body's count after the box from its labels.")
					.after(120).add();
			for (int k = 1; k <= 3; k++) d.para("body item of list A").numPr(80, 0).add();
			List<P> choice = new ArrayList<>();
			for (int k = 1; k <= 2; k++) choice.add(d.para("wps Choice item of list A").numPr(80, 0).build());
			choice.add(d.para("wps Choice item of list B").numPr(81, 0).build());
			List<P> fallback = new ArrayList<>();
			for (int k = 1; k <= 3; k++) fallback.add(d.para("VML Fallback item of list A").numPr(80, 0).build());
			for (int k = 1; k <= 2; k++) fallback.add(d.para("VML Fallback item of list B").numPr(81, 0).build());
			d.para("A text box whose branches differ: ")
					.run(d.textBox(5000, 2400, Doc.paragraphsXml(choice), Doc.paragraphsXml(fallback)))
					.before(120).after(120).add();
			for (int k = 1; k <= 3; k++) d.para("body item of list A after the box").numPr(80, 0).add();
			d.para("body item of list B after the box").numPr(81, 0).add();
			d.para("after.").before(240).add();
			return d.pkg();
		}));

		PROBES.add(new Probe("mc-textbox-branches-bound-sdt",
				"a custom XML part (root/name = FRESH NAME, root/box = FRESH BOX) and three "
				+ "plain-text content controls bound to it whose document text is STALE: one in "
				+ "the body, and one in each branch of a text box written as mc:AlternateContent "
				+ "(the wps Choice says STALE CHOICE, the VML Fallback STALE FALLBACK); then a "
				+ "control box with no binding whose branches say CHOICE TEXT and FALLBACK TEXT - "
				+ "which branch Word draws, whether it refreshes the bound text from the part, "
				+ "and (in Word's re-save) what it writes into the branch it did not draw", () -> {
			Doc d = Doc.create(15);
			String storeItemId = addCustomXml(d,
					"<root><name>FRESH NAME</name><box>FRESH BOX</box></root>");
			d.para("The custom XML part says FRESH; every bound control below says STALE in "
					+ "the document. Read which text Word shows, and in each text box which "
					+ "branch.").after(120).add();
			d.add(withInlineSdt(d.para("Body control, bound to root/name: ").build(),
					boundSdtXml("STALE NAME", "/root[1]/name[1]", storeItemId, 21001)));
			List<P> choice = new ArrayList<>();
			choice.add(withInlineSdt(d.para("wps Choice control, bound to root/box: ").build(),
					boundSdtXml("STALE CHOICE", "/root[1]/box[1]", storeItemId, 21002)));
			List<P> fallback = new ArrayList<>();
			fallback.add(withInlineSdt(d.para("VML Fallback control, bound to root/box: ").build(),
					boundSdtXml("STALE FALLBACK", "/root[1]/box[1]", storeItemId, 21003)));
			d.para("A text box whose branches each hold a bound control: ")
					.run(d.textBox(5000, 1200, Doc.paragraphsXml(choice), Doc.paragraphsXml(fallback)))
					.before(120).after(120).add();
			List<P> choice2 = new ArrayList<>();
			choice2.add(d.para("CHOICE TEXT, the wps branch, no binding").build());
			List<P> fallback2 = new ArrayList<>();
			fallback2.add(d.para("FALLBACK TEXT, the VML branch, no binding").build());
			d.para("The control box, no binding: ")
					.run(d.textBox(5000, 1200, Doc.paragraphsXml(choice2), Doc.paragraphsXml(fallback2)))
					.after(120).add();
			d.para("after.").before(240).add();
			return d.pkg();
		}));

		// ---------------------------------------------------------- astral characters (CR-020 P2-1)
		PROBES.add(new Probe("surrogate-pairs",
				"characters outside the BMP - an emoji (U+1F600) and a CJK Extension B ideograph"
				+ " (U+20000), each a surrogate pair in Java - at the end of a run, at the end of a"
				+ " paragraph, inside a right-to-left run beside Arabic, and where the pair falls on a"
				+ " font change: FOP's word scanner split a pair across word fragments when a bidi"
				+ " level or a per-character font change ended the word, and its surrogate test"
				+ " threw on a high surrogate in the last position (fork P2-1; Metanorma #39)", () -> {
			Doc d = Doc.create(15);
			String emoji = new String(Character.toChars(0x1F600));
			String extB = new String(Character.toChars(0x20000));
			// the pair in the last position of a run and of a paragraph
			d.para("An emoji ends this run " + emoji).after(120).add();
			d.para("An Extension B ideograph ends this paragraph " + extB).after(120).add();
			// a pair on each side of a font change, so per-character font selection ends a word there
			d.para("Font change on the pair: ").run(emoji + " and " + extB, SANS, 24, null)
					.text(" then serif " + emoji + " again").after(120).add();
			// inside a right-to-left run: the bidi level changes at the pair
			d.para("Right to left: ")
					.run("\u0645\u0631\u062D\u0628\u0627 " + emoji + " \u0628\u0643", SANS, 24, r -> {
						r.setRtl(new BooleanDefaultTrue());
					}).text(" and back to left to right " + emoji).after(120).add();
			// a run of pairs, so consecutive words are all astral
			d.para("Only pairs: ").run(emoji + emoji + " " + extB + extB + " " + emoji, SANS, 24, null)
					.after(120).add();
			d.para("after.").add();
			return d.pkg();
		}));

		// ------------------------------------- astral characters in bidi text (fop/CR-008, FOP-2918)
		PROBES.add(new Probe("surrogate-pairs-bidi",
				"characters outside the BMP where FOP resolves bidi levels: a right-to-left letter"
				+ " outside the BMP (Cypriot U+10826, class R) between Latin and inside Hebrew, whose"
				+ " low surrogate's level was never resolved (an InlineRun.split assertion under -ea,"
				+ " a reversed word without); an emoji FOP classes ON (U+1F44D) between two Hebrew"
				+ " words, where the unresolved placeholder cut the run of neutrals; U+263A, a BMP"
				+ " neutral, as the reference order; U+1F600, which FOP's table classes L (Enterprise"
				+ " CR-001 item 35), as the control; and the emoji case again in a right-to-left"
				+ " paragraph (fork fop/CR-008)", () -> {
			Doc d = Doc.create(15);
			String cypriot = new String(Character.toChars(0x10826));
			String thumbs = new String(Character.toChars(0x1F44D));
			String grin = new String(Character.toChars(0x1F600));
			String shalom = "\u05E9\u05DC\u05D5\u05DD", olam = "\u05E2\u05D5\u05DC\u05DD";
			// 1: an R letter outside the BMP, between Latin letters in one run, then inside Hebrew
			d.para("ab" + cypriot + "cd").after(120).add();
			d.para(shalom + " " + cypriot + " " + olam).after(120).add();
			// 2: an ON emoji outside the BMP between two Hebrew words (the placeholder's class)
			d.para(shalom + thumbs + " " + olam).after(120).add();
			// 3: the reference, a BMP neutral in the same place
			d.para(shalom + "\u263A " + olam).after(120).add();
			// 4: the control, an emoji FOP's table reads as L (unchanged by fop/CR-008)
			d.para(shalom + grin + " " + olam).after(120).add();
			// 5: case 2 again, in a right-to-left paragraph
			P rtl = d.para(shalom + thumbs + " " + olam).after(120).add();
			if (rtl.getPPr() == null) rtl.setPPr(Doc.F.createPPr());
			rtl.getPPr().setBidi(new BooleanDefaultTrue());
			d.para("after.").add();
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
	 *  paragraph's line is sized by it.  @since 17.2.0 */
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

	/** {@code w:cantSplit} on every row of a table: a row may not be divided across a page
	 *  boundary.  Written first in each w:trPr, which is where Word writes it.
	 *  @since 17.2.0 (CR-001 batch 47, the table-rowsplit probe) */
	private static Tbl cantSplitEveryRow(Tbl tbl) {
		for (Object o : tbl.getContent()) {
			if (!(o instanceof org.docx4j.wml.Tr)) continue;
			org.docx4j.wml.Tr tr = (org.docx4j.wml.Tr) o;
			if (tr.getTrPr() == null) tr.setTrPr(F.createTrPr());
			tr.getTrPr().getCnfStyleOrDivIdOrGridBefore().add(0,
					F.createCTTrPrBaseCantSplit(new org.docx4j.wml.BooleanDefaultTrue()));
		}
		return tbl;
	}

	/** The four grid-against-w:tcW configurations of ledger6's item 9, under one
	 *  compatibility mode: the mode is a document property, so each mode is its own
	 *  probe document (as table-indent-compat* and table-grid-edge-compat* already are).
	 *  @since 17.2.0 (CR-001 batch 47) */
	private static Probe tableGridVsTcwProbe(int compatMode) {
		return new Probe("table-grid-vs-tcw-compat" + compatMode,
				"a three-column table whose w:tblGrid and row-1 w:tcW agree, disagree "
				+ "(4000/3000/2000 against 2000/3000/4000), have one cell auto and the rest "
				+ "dxa, and a w:tblW auto table over a grid 29 per cent wider than the text "
				+ "column; row 2 of each is auto, compatibilityMode " + compatMode, () -> {
			Doc d = Doc.create(compatMode);
			int[] grid = { 2000, 3000, 4000 };
			int[] disagree = { 4000, 3000, 2000 };
			int[] wide = { 2800, 4400, 4444 };   // 11644 twips = 29% over the 9026 column

			d.para("Four tables, one to a page, at compatibilityMode " + compatMode + ". The "
					+ "text column is 9026 twips. Each cell's text opens with its table's "
					+ "letter and its column's number, so each column's left edge is the x "
					+ "of a first glyph.").after(240).add();

			for (int t = 0; t < 4; t++) {
				String name = String.valueOf((char) ('A' + t));
				d.pageBreak();
				d.para("Table " + name + ": " + gridCaseCaption(t) + ". " + prose(1, t))
						.after(240).add();
				Doc.Table tbl = new Doc.Table(t == 3 ? wide : grid);
				if (t == 3) tbl.autoWidth();
				switch (t) {
					case 0:
						tbl.rowDxa(SERIF, 24, grid, gridCell(name, 1), gridCell(name, 2), gridCell(name, 3));
						break;
					case 1:
						tbl.rowDxa(SERIF, 24, disagree, gridCell(name, 1), gridCell(name, 2), gridCell(name, 3));
						break;
					case 2:
						tbl.rowOf(null, null,
								tbl.cell(gridCell(name, 1), SERIF, 24, 1, null),   // w:tcW auto
								tbl.cell(gridCell(name, 2), SERIF, 24, 1, 3000),
								tbl.cell(gridCell(name, 3), SERIF, 24, 1, 4000));
						break;
					default:
						tbl.rowDxa(SERIF, 24, wide, gridCell(name, 1), gridCell(name, 2), gridCell(name, 3));
						break;
				}
				tbl.row(SERIF, 24, true, name + " r2 c1 auto", name + " r2 c2 auto", name + " r2 c3 auto");
				d.add(tbl.build());
				d.para("after table " + name + ". " + prose(1, t + 1)).before(240).add();
			}
			return d.pkg();
		});
	}

	/** What each table-grid-vs-tcw table declares, for its introducing paragraph. */
	private static String gridCaseCaption(int t) {
		switch (t) {
			case 0: return "w:tblGrid 2000/3000/4000 and every row-1 w:tcW the same";
			case 1: return "w:tblGrid 2000/3000/4000 with row 1's w:tcW 4000/3000/2000 - the "
					+ "same sum, the opposite proportions";
			case 2: return "w:tblGrid 2000/3000/4000 with row 1's first cell w:tcW auto and "
					+ "the other two 3000 and 4000 dxa";
			default: return "w:tblW auto over a grid of 2800/4400/4444 = 11644 twips, 29 per "
					+ "cent wider than the text column, every row-1 w:tcW the grid's own";
		}
	}

	/** A table-grid-vs-tcw cell's text: the label first, so the column's left edge is the
	 *  x of its first glyph, and enough after it to show where the column ends. */
	private static String gridCell(String table, int col) {
		return table + " c" + col + " left edge here; this text wraps.";
	}

	/** A token of {@code n} characters cycling through the ten digits: every digit of
	 *  Liberation Serif is 120 twips wide at 12pt and they are tabular, so the token's
	 *  advance is exactly 120n twips and its characters can still be counted in an
	 *  extracted line.  @since 17.2.0 (CR-001 batch 47, the break-longword probe) */
	private static String digitToken(int n) {
		StringBuilder sb = new StringBuilder(n);
		for (int i = 0; i < n; i++) sb.append((char) ('0' + i % 10));
		return sb.toString();
	}

	/** A one-column table holding one prepared paragraph, w:tblLayout fixed and
	 *  w:tblBorders none: with {@code zeroMargins} its cell's text measure is exactly
	 *  {@code twips}, and without it Word's default 108-twip cell margins are the only
	 *  thing charged against the measure.
	 *  @since 17.2.0 (CR-001 batch 47, the break-longword probe) */
	private static Tbl tokenCell(int twips, boolean zeroMargins, P content) {
		Doc.Table t = new Doc.Table(twips).fixedLayout().noBorders();
		if (zeroMargins) t.cellMargins(0, 0);
		t.rowOf(null, null, t.cellOf(twips, null, content));
		return t.build();
	}

	/** Level 0 of a decimal list whose w:suff is a tab and whose own w:pPr carries one
	 *  left tab stop at {@code stopTwips} with {@code leader} (or no w:tabs at all when
	 *  stopTwips is 0), over Word's own w:ind left 720 hanging 360 - so the label "1."
	 *  runs from 360 to 540 and the stop's advance past it is stopTwips - 540.
	 *  @since 17.2.0 (CR-001 batch 47, the tab-leader-in-cell probe) */
	private static String leaderLevel(int ilvl, int stopTwips, String leader) {
		return leaderLevel(ilvl, stopTwips, leader, 720, 360);
	}

	/** {@link #leaderLevel(int, int, String)} over an explicit w:ind: the label runs from
	 *  {@code leftTwips - hangingTwips} for its own width and the paragraph's indent is at
	 *  {@code leftTwips}, so a stop between the two is the one the numbering tab uses and
	 *  the advance it opens is measurable.  Element order follows CT_Lvl and
	 *  CT_PPrGeneral, which JAXB needs.
	 *  @since 17.2.0 (CR-001 batch 47, the tab-leader-in-cell-2 probe) */
	private static String leaderLevel(int ilvl, int stopTwips, String leader, int leftTwips,
			int hangingTwips) {
		return "<w:lvl w:ilvl=\"" + ilvl + "\">"
				+ "<w:start w:val=\"1\"/>"
				+ "<w:numFmt w:val=\"decimal\"/>"
				+ "<w:suff w:val=\"tab\"/>"
				+ "<w:lvlText w:val=\"%" + (ilvl + 1) + ".\"/>"
				+ "<w:lvlJc w:val=\"left\"/>"
				+ "<w:pPr>"
				+ (stopTwips <= 0 ? "" : "<w:tabs><w:tab w:val=\"left\" w:pos=\"" + stopTwips + "\""
						+ (leader == null ? "" : " w:leader=\"" + leader + "\"") + "/></w:tabs>")
				+ "<w:ind w:left=\"" + leftTwips + "\" w:hanging=\"" + hangingTwips + "\"/>"
				+ "</w:pPr></w:lvl>";
	}

	/** {@link #leaderLevel(int, int, String, int, int)} with the level's own {@code w:rPr}:
	 *  a size in half-points and, where {@code font} is not null, a {@code w:rFonts} - the
	 *  label's own face and size, against the paragraph's.
	 *  @since 17.2.0 (CR-001 batch 47 close-out, the tab-leader-sizes probe) */
	private static String sizedLeaderLevel(int ilvl, int stopTwips, String leader, int leftTwips,
			int hangingTwips, int halfPts, String font) {
		String lvl = leaderLevel(ilvl, stopTwips, leader, leftTwips, hangingTwips);
		String rpr = "<w:rPr>"
				+ (font == null ? "" : "<w:rFonts w:ascii=\"" + font + "\" w:hAnsi=\"" + font + "\" w:cs=\"" + font + "\"/>")
				+ "<w:sz w:val=\"" + halfPts + "\"/><w:szCs w:val=\"" + halfPts + "\"/></w:rPr>";
		return lvl.replace("</w:pPr></w:lvl>", "</w:pPr>" + rpr + "</w:lvl>");
	}


	// ------------------------------------------ CR-001 batch 48 probe helpers

	/** The paragraph mark's own w:rFonts and w:sz, and its weight when {@code bold} is not
	 *  null: a mark which does not carry the paragraph's font and size would size the line
	 *  box from the document defaults, which is not the thing being measured.
	 *  @since 17.2.0 (CR-001 batch 48, the line-box-bold probe) */
	private static java.util.function.Consumer<org.docx4j.wml.ParaRPr> markFont(String font,
			int halfPts, Boolean bold) {
		return rpr -> {
			org.docx4j.wml.RFonts rf = Doc.F.createRFonts();
			rf.setAscii(font);
			rf.setHAnsi(font);
			rf.setCs(font);
			rf.setEastAsia(font);
			rpr.setRFonts(rf);
			org.docx4j.wml.HpsMeasure sz = Doc.F.createHpsMeasure();
			sz.setVal(BigInteger.valueOf(halfPts));
			rpr.setSz(sz);
			rpr.setSzCs(sz);
			if (bold != null) rpr.setB(flag(bold.booleanValue()));
		};
	}

	/** The text split into runs of three words which alternate bold and regular, so that
	 *  every line of the paragraph holds both weights.
	 *  @since 17.2.0 (CR-001 batch 48, the line-box-bold probe) */
	private static Doc.Para alternatingWeights(Doc.Para p, String text, String font, int halfPts) {
		java.util.function.Consumer<org.docx4j.wml.RPr> b = Doc::bold;
		String[] words = text.split(" ");
		StringBuilder chunk = new StringBuilder();
		boolean boldChunk = true;
		for (int i = 0; i < words.length; i++) {
			chunk.append(words[i]).append(' ');
			if ((i + 1) % 3 == 0 || i == words.length - 1) {
				p.run(chunk.toString(), font, halfPts, boldChunk ? b : null);
				chunk.setLength(0);
				boldChunk = !boldChunk;
			}
		}
		return p;
	}

	/** The five line-box cases at one font and size, consecutive and unspaced.
	 *  @since 17.2.0 (CR-001 batch 48, the line-box-bold probe) */
	private static void lineBoxCases(Doc d, String tag, String font, int halfPts) {
		java.util.function.Consumer<org.docx4j.wml.RPr> b = Doc::bold;
		d.para().noLabel().font(font, halfPts).markRPr(markFont(font, halfPts, null))
				.text(tag + " (a) every run regular, mark silent on the weight: " + prose(6, 0)).add();
		d.para().noLabel().font(font, halfPts).markRPr(markFont(font, halfPts, Boolean.TRUE))
				.run(tag + " (b) every run bold, mark bold: " + prose(6, 1), font, halfPts, b).add();
		alternatingWeights(d.para().noLabel().font(font, halfPts)
				.markRPr(markFont(font, halfPts, null))
				.text(tag + " (c) three-word runs alternating bold and regular: "),
				prose(6, 2), font, halfPts).add();
		d.para().noLabel().font(font, halfPts).markRPr(markFont(font, halfPts, Boolean.TRUE))
				.text(tag + " (d) every run regular, mark w:b: " + prose(6, 3)).add();
		d.para().noLabel().font(font, halfPts).markRPr(markFont(font, halfPts, Boolean.FALSE))
				.run(tag + " (e) every run bold, mark w:b w:val=0: " + prose(6, 4), font, halfPts, b).add();
	}

	/** One case of missing-family-weight: a regular line and a bold line in the family the
	 *  customiser names, then a Calibri control line holding a regular and a bold run.
	 *  @since 17.2.0 (CR-001 batch 48) */
	private static void missingFamilyCase(Doc d, String label, String what,
			java.util.function.Consumer<org.docx4j.wml.RPr> fontRef) {
		d.para().noLabel().inheritSpacing()
				.run(bareRun(label + " regular, " + what + ": " + FONTS_SENTENCE, fontRef)).add();
		d.para().noLabel().inheritSpacing()
				.run(bareRun(label + " bold, " + what + ": " + FONTS_SENTENCE,
						fontRef.andThen(Doc::bold))).add();
		d.para().noLabel().inheritSpacing()
				.run(bareRun(label + " control, Calibri regular: " + FONTS_SENTENCE, allFour("Calibri")))
				.run(bareRun(" And " + label + " control, Calibri bold.",
						allFour("Calibri").andThen(Doc::bold))).add();
	}

	/** A numbering level with an explicit {@code w:lvlText}, {@code w:suff} and indent, and a
	 *  size (half-points) on the level's own {@code w:rPr} - the label's own face, which is
	 *  what its width is measured in.  Element order follows CT_Lvl and CT_PPrGeneral.
	 *  @since 17.2.0 (CR-001 batch 47, the list-label-width probe) */
	private static String labelLevel(int ilvl, String lvlText, String suff, int leftTwips,
			int hangingTwips, int halfPts) {
		return "<w:lvl w:ilvl=\"" + ilvl + "\">"
				+ "<w:start w:val=\"1\"/>"
				+ "<w:numFmt w:val=\"decimal\"/>"
				+ "<w:suff w:val=\"" + suff + "\"/>"
				+ "<w:lvlText w:val=\"" + lvlText + "\"/>"
				+ "<w:lvlJc w:val=\"left\"/>"
				+ "<w:pPr><w:ind w:left=\"" + leftTwips + "\""
				+ (hangingTwips > 0 ? " w:hanging=\"" + hangingTwips + "\"" : "") + "/></w:pPr>"
				+ "<w:rPr><w:sz w:val=\"" + halfPts + "\"/>"
				+ (halfPts >= 40 ? "<w:b/>" : "") + "</w:rPr>"
				+ "</w:lvl>";
	}

	// ---------------------------------------------------------------- CR-021 phase 0 helpers

	/** A custom XML data storage part holding this XML, with its itemProps part; returns the
	 *  store item id (fixed, so the probe's docx is reproducible).  @since 17.2.0 (CR-021 phase 0) */
	private static String addCustomXml(Doc d, String xml) throws Exception {
		org.docx4j.openpackaging.parts.CustomXmlDataStoragePart part =
				new org.docx4j.openpackaging.parts.CustomXmlDataStoragePart();
		org.docx4j.model.datastorage.CustomXmlDataStorage data =
				new org.docx4j.model.datastorage.CustomXmlDataStorageImpl();
		data.setDocument(org.docx4j.XmlUtils.getNewDocumentBuilder().parse(
				new org.xml.sax.InputSource(new java.io.StringReader(xml))));
		part.setData(data);
		d.mdp().addTargetPart(part,
				org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour.RENAME_IF_NAME_EXISTS);
		org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart props =
				new org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart();
		org.docx4j.customXmlProperties.DatastoreItem dsi =
				new org.docx4j.customXmlProperties.ObjectFactory().createDatastoreItem();
		String storeItemId = "{7C021000-0000-4000-8000-0000000C0021}";
		dsi.setItemID(storeItemId);
		props.setJaxbElement(dsi);
		part.addTargetPart(props);
		return storeItemId;
	}

	/** An inline plain-text content control (w:sdt with w:text) bound by w:dataBinding to
	 *  xpath in the custom XML part storeItemId, showing text.  @since 17.2.0 (CR-021 phase 0) */
	private static String boundSdtXml(String text, String xpath, String storeItemId, int id) {
		return "<w:sdt><w:sdtPr><w:id w:val=\"" + id + "\"/>"
				+ "<w:dataBinding w:xpath=\"" + xpath + "\" w:storeItemID=\"" + storeItemId + "\"/>"
				+ "<w:text/></w:sdtPr>"
				+ "<w:sdtContent><w:r><w:t xml:space=\"preserve\">" + text + "</w:t></w:r></w:sdtContent></w:sdt>";
	}

	/** The paragraph with this inline w:sdt appended to its content.  @since 17.2.0 (CR-021 phase 0) */
	private static P withInlineSdt(P p, String sdtXml) throws Exception {
		String xml = org.docx4j.XmlUtils.marshaltoString(p, true, false, org.docx4j.jaxb.Context.jc);
		if (!xml.contains("xmlns:w=")) {
			xml = xml.replaceFirst("<w:p(?=[ >])",
					"<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"");
		}
		int at = xml.lastIndexOf("</w:p>");
		xml = xml.substring(0, at) + sdtXml + xml.substring(at);
		Object o = org.docx4j.XmlUtils.unmarshalString(xml, org.docx4j.jaxb.Context.jc, P.class);
		if (o instanceof jakarta.xml.bind.JAXBElement) o = ((jakarta.xml.bind.JAXBElement<?>) o).getValue();
		return (P) o;
	}

	// ---------------------------------------------------------------- CR-001 batch 50 helpers

	/** A4 with 1 inch margins: the body's height in twips (16838 - 2 x 1440).
	 *  @since 17.3.1 (CR-001 batch 50) */
	private static final int A4_BODY_TW = 13958;

	/** Every line of table-rowsplit-3 is exactly this tall: 14pt.  @since 17.3.1 */
	private static final int SPLIT_LINE_TW = 280;

	/** Corpus document 11657's w:compat flags besides its compatibility mode 11 (the Word
	 *  2003 set), by their {@link Doc#compat} names.  @since 17.3.1 (CR-001 batch 50) */
	private static final String[] FLAGS_11657 = { "useNormalStyleForList",
		"doNotUseIndentAsNumberingTabStop", "useAltKinsokuLineBreakRules",
		"allowSpaceOfSameStyleInTable", "doNotSuppressIndentation",
		"doNotAutofitConstrainedTables", "autofitToFirstFixedWidthCell", "underlineTabInNumList",
		"displayHangulFixedWidth", "splitPgBreakAndParaMark", "doNotVertAlignCellWithSp",
		"doNotBreakConstrainedForcedTable", "doNotVertAlignInTxbx", "useAnsiKerningPairs",
		"cachedColBalance" };

	/** One table-rowsplit-3 document, at one compatibility mode (and, for mode 11, with or
	 *  without 11657's w:compat flags).  @since 17.3.1 (CR-001 batch 50) */
	private static Probe rowSplit3Probe(int mode, boolean flags11657) {
		String what = "mode " + mode + (flags11657 ? " with corpus document 11657's fifteen w:compat flags" : "");
		return new Probe("table-rowsplit-3-compat" + mode + (flags11657 ? "-11657" : ""),
				"does Word divide a table row inside a two- or three-line cell paragraph (1+1, 1+2) "
				+ "where widow control forbids it, as it does on 11657 at mode 11?  At " + what
				+ ": rows of a tag cell, a two-line and a three-line cell paragraph, every line an "
				+ "exact 14pt, the page bottom half way through line 2 (one line fits) or line 3 "
				+ "(two fit) of the third row.  A w:widowControl absent, B w:val=\"false\", C on, "
				+ "D A + w:cantSplit, E A + w:keepLines, F A in 11657's row shape (w:tblHeader, "
				+ "w:vAlign bottom, a two-paragraph tag cell); G body paragraphs of two and three "
				+ "lines, widow control absent, H on.  A page each.  Read which page each line "
				+ "of the straddling row or paragraph lands on", () -> {
			Doc d = Doc.create(mode);
			if (flags11657) {
				for (String flag : FLAGS_11657) d.compat(flag, true);
			}
			d.para("table-rowsplit-3 at " + what + ". Sixteen variants follow, a page each; the "
					+ "page bottom falls inside the third three-line row of each table, or inside "
					+ "the body paragraph.").add();
			for (String v : new String[] { "A", "B", "C", "D", "E", "F" }) {
				for (int fit = 1; fit <= 2; fit++) {
					String tag = v + fit;
					// above the straddling row: F's one-line header row, then two rows of three lines
					int above = ("F".equals(v) ? SPLIT_LINE_TW : 0) + 2 * 3 * SPLIT_LINE_TW;
					fillTo(d, tag, A4_BODY_TW - above - (2 * fit + 1) * SPLIT_LINE_TW / 2);
					d.add(splitTable(tag, v));
					d.para(tag + " after the table").noLabel().line(SPLIT_LINE_TW, STLineSpacingRule.EXACT).add();
				}
			}
			// tag, lines, how many of them fit, widow control (null = absent)
			Object[][] body = { { "G1", 2, 1, null }, { "G2", 3, 1, null }, { "G3", 3, 2, null },
					{ "H1", 2, 1, Boolean.TRUE } };
			for (Object[] b : body) {
				String tag = (String) b[0];
				int fit = (Integer) b[2];
				fillTo(d, tag, A4_BODY_TW - (2 * fit + 1) * SPLIT_LINE_TW / 2);
				// A4 with 1in margins: a 451.3pt measure
				Doc.Para p = d.para(linesOf(tag + " body:", (Integer) b[1], fit + 5, SERIF, 24, 451.3))
						.noLabel().line(SPLIT_LINE_TW, STLineSpacingRule.EXACT);
				if (b[3] != null) p.widowControl((Boolean) b[3]);
				p.add();
				d.para(tag + " after the paragraph").noLabel().line(SPLIT_LINE_TW, STLineSpacingRule.EXACT).add();
			}
			return d.pkg();
		});
	}

	/** Exact 14pt filler lines from the top of a new page, then one spacer line of
	 *  whatever is left over (14pt to 28pt), so that what follows starts topTwips below
	 *  the body's top.  @since 17.3.1 (CR-001 batch 50) */
	private static void fillTo(Doc d, String tag, int topTwips) {
		int n = topTwips / SPLIT_LINE_TW - 1;
		int spacer = topTwips - n * SPLIT_LINE_TW;
		for (int i = 0; i <= n; i++) {
			Doc.Para p = i < n
					? d.para(tag + " filler " + (i + 1)).noLabel().line(SPLIT_LINE_TW, STLineSpacingRule.EXACT)
					: d.para(tag + " spacer, one exact line of " + spacer / 20.0 + "pt").noLabel()
							.line(spacer, STLineSpacingRule.EXACT);
			if (i == 0) p.pageBreakBefore();
			p.add();
		}
	}

	/** One of table-rowsplit-3's tables: five rows (after a one-line header row in F) of
	 *  a tag cell, a two-line and a three-line cell paragraph.  @since 17.3.1 */
	private static Tbl splitTable(String tag, String variant) {
		boolean f = "F".equals(variant);
		String vAlign = f ? "bottom" : null;
		Doc.Table t = new Doc.Table(1500, 3700, 3800).fixedLayout().noBorders().cellMargins(108, 0);
		if (f) {
			t.rowOf(null, null, t.cellOf(1500, vAlign, splitCellPara(tag + " head", variant)),
					t.cellOf(3700, vAlign, splitCellPara("two lines", variant)),
					t.cellOf(3800, vAlign, splitCellPara("three lines", variant)));
		}
		for (int r = 1; r <= 5; r++) {
			String rt = tag + " r" + r;
			P[] tagCell = f ? new P[] { splitCellPara(rt, variant), splitCellPara("code " + r, variant) }
					: new P[] { splitCellPara(rt, variant) };
			// cell measures: 3700 and 3800 twips less 2 x 108 of cell margin
			t.rowOf(null, null, t.cellOf(1500, vAlign, tagCell),
					t.cellOf(3700, vAlign, splitCellPara(linesOf(rt + " two:", 2, r, SERIF, 24, 174.2), variant)),
					t.cellOf(3800, vAlign, splitCellPara(linesOf(rt + " three:", 3, r + 3, SERIF, 24, 179.2), variant)));
		}
		Tbl tbl = t.build();
		if ("D".equals(variant)) cantSplitEveryRow(tbl);
		if (f) {
			org.docx4j.wml.Tr head = (org.docx4j.wml.Tr) tbl.getContent().get(0);
			if (head.getTrPr() == null) head.setTrPr(F.createTrPr());
			head.getTrPr().getCnfStyleOrDivIdOrGridBefore().add(
					F.createCTTrPrBaseTblHeader(new BooleanDefaultTrue()));
		}
		return tbl;
	}

	/** A table-rowsplit-3 cell paragraph: Liberation Serif 12pt on exact 14pt lines, with
	 *  what the variant states about widow control and keeping lines.  @since 17.3.1 */
	private static P splitCellPara(String text, String variant) {
		P p = Doc.plainParagraph(text, SERIF, 24);
		PPr ppr = p.getPPr();
		ppr.getSpacing().setLine(BigInteger.valueOf(SPLIT_LINE_TW));
		ppr.getSpacing().setLineRule(STLineSpacingRule.EXACT);
		if ("B".equals(variant)) {
			BooleanDefaultTrue off = new BooleanDefaultTrue();
			off.setVal(Boolean.FALSE);
			ppr.setWidowControl(off);
		} else if ("C".equals(variant)) {
			ppr.setWidowControl(new BooleanDefaultTrue());
		} else if ("E".equals(variant)) {
			ppr.setKeepLines(new BooleanDefaultTrue());
		}
		return p;
	}

	/**
	 * Text which Word's greedy line breaker sets in exactly {@code lines} lines of
	 * {@code measurePt}, in font at halfPts.  Every full line is at least 3pt short of the
	 * measure, and at least 3pt too long once the next word is added; a word that would
	 * bring its line within 3pt of the measure, either way, is passed over.  The last
	 * line is about half full.  The words are {@link Doc#prose}'s from offset on, after
	 * lead.  @since 17.3.1 (CR-001 batch 50)
	 */
	private static String linesOf(String lead, int lines, int offset, String font, int halfPts,
			double measurePt) {
		String[] words = prose(8, offset).split(" ");
		StringBuilder text = new StringBuilder(lead);
		String line = lead;
		int made = 1;
		for (int w = 0; w < 10 * words.length; w++) {
			String word = words[w % words.length];
			String trial = line + " " + word;
			double width = Doc.advancePoints(trial, font, halfPts);
			if (made == lines) {
				if (width > 0.55 * measurePt) return text.toString();
				text.append(' ').append(word);
				line = trial;
			} else if (width <= measurePt - 3) {
				text.append(' ').append(word);
				line = trial;
			} else if (width > measurePt + 3) {
				text.append(' ').append(word);
				line = word;
				made++;
			}
		}
		throw new IllegalStateException("cannot set \"" + lead + "\" in " + lines + " lines");
	}

	/** The VML text-box shape type, which Word writes once before the first box that
	 *  uses it.  @since 17.3.1 (CR-001 batch 50) */
	private static final String SHAPETYPE_202 = "<v:shapetype id=\"_x0000_t202\" coordsize=\"21600,21600\""
			+ " o:spt=\"202\" path=\"m,l,21600r21600,l21600,xe\"><v:stroke joinstyle=\"miter\"/>"
			+ "<v:path gradientshapeok=\"t\" o:connecttype=\"rect\"/></v:shapetype>";

	/** The namespaces a paragraph of textbox-inset-stroke-list's boxes needs.  @since 17.3.1 */
	private static final String BOX_NAMESPACES =
			"xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\""
			+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\""
			+ " xmlns:v=\"urn:schemas-microsoft-com:vml\""
			+ " xmlns:o=\"urn:schemas-microsoft-com:office:office\""
			+ " xmlns:w10=\"urn:schemas-microsoft-com:office:word\"";

	/** textbox-inset-stroke-list's six paragraphs, as the content of a w:txbxContent.
	 *  @since 17.3.1 (CR-001 batch 50) */
	private static String insetBoxContent(Doc d, String tag, String what) throws Exception {
		List<P> ps = new ArrayList<>();
		ps.add(d.para(tag + ": " + what).noLabel().build());
		ps.add(d.para(tag + " justified. " + prose(2, 1)).noLabel().jc(JcEnumeration.BOTH).build());
		ps.add(d.para(tag + " list at w:ind left 360 hanging 360. " + prose(1, 3)).noLabel()
				.numPr(30, 0).build());
		ps.add(d.para(tag + " list at w:ind left 720 hanging 360. " + prose(1, 4)).noLabel()
				.numPr(31, 0).build());
		ps.add(d.para(tag + " plain at w:ind left 720. " + prose(1, 5)).noLabel().indent(720, 0, 0).build());
		ps.add(d.para(tag + "-right").noLabel().jc(JcEnumeration.RIGHT).build());
		return Doc.paragraphsXml(ps);
	}

	/** A 240 x 340pt unfilled VML text box (v:shape of type 202) at (leftPt, topPt) on
	 *  the page, with the given stroke attributes and v:textbox inset (null: none, the
	 *  default 7.2pt,3.6pt,7.2pt,3.6pt).  @since 17.3.1 (CR-001 batch 50) */
	private static String vmlInsetBox(int n, int leftPt, int topPt, String strokeAttrs, String inset,
			String content) {
		return "<v:shape id=\"Text Box " + n + "\" o:spid=\"_x0000_s" + (1025 + n) + "\" type=\"#_x0000_t202\""
				+ " style=\"position:absolute;margin-left:" + leftPt + "pt;margin-top:" + topPt
				+ "pt;width:240pt;height:340pt;z-index:" + n
				+ ";mso-position-horizontal-relative:page;mso-position-vertical-relative:page\""
				+ " filled=\"f\"" + (strokeAttrs.isEmpty() ? "" : " " + strokeAttrs) + ">"
				+ "<v:textbox" + (inset == null ? "" : " inset=\"" + inset + "\"") + ">"
				+ "<w:txbxContent>" + content + "</w:txbxContent></v:textbox></v:shape>";
	}

	/** The same box as DrawingML: an anchored wps text box, page-relative, no wrap, a:ln
	 *  of lineEmu (null: a:noFill), default body insets, in mc:AlternateContent with a VML
	 *  fallback of the same stroke, as Word writes it.  @since 17.3.1 (CR-001 batch 50) */
	private static String wpsInsetBox(int n, int leftPt, int topPt, String lineEmu, String content) {
		long cx = 240 * 12700L, cy = 340 * 12700L;
		String ln = lineEmu == null ? "<a:ln><a:noFill/></a:ln>"
				: "<a:ln w=\"" + lineEmu + "\"><a:solidFill><a:srgbClr val=\"000000\"/></a:solidFill></a:ln>";
		String fallbackStroke = lineEmu == null ? "stroked=\"f\""
				: "strokeweight=\"" + (Integer.parseInt(lineEmu) / 12700.0) + "pt\"";
		return "<w:r><mc:AlternateContent><mc:Choice Requires=\"wps\"><w:drawing>"
				+ "<wp:anchor distT=\"0\" distB=\"0\" distL=\"114300\" distR=\"114300\" simplePos=\"0\""
				+ " relativeHeight=\"" + (251659264 + n) + "\" behindDoc=\"0\" locked=\"0\" layoutInCell=\"1\""
				+ " allowOverlap=\"1\"><wp:simplePos x=\"0\" y=\"0\"/>"
				+ "<wp:positionH relativeFrom=\"page\"><wp:posOffset>" + leftPt * 12700L + "</wp:posOffset></wp:positionH>"
				+ "<wp:positionV relativeFrom=\"page\"><wp:posOffset>" + topPt * 12700L + "</wp:posOffset></wp:positionV>"
				+ "<wp:extent cx=\"" + cx + "\" cy=\"" + cy + "\"/><wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/>"
				+ "<wp:wrapNone/><wp:docPr id=\"" + (200 + n) + "\" name=\"Text Box " + n + "\"/><wp:cNvGraphicFramePr/>"
				+ "<a:graphic><a:graphicData uri=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\">"
				+ "<wps:wsp><wps:cNvSpPr txBox=\"1\"/><wps:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/>"
				+ "<a:ext cx=\"" + cx + "\" cy=\"" + cy + "\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>"
				+ "<a:noFill/>" + ln + "</wps:spPr><wps:txbx><w:txbxContent>" + content + "</w:txbxContent></wps:txbx>"
				+ "<wps:bodyPr rot=\"0\" vert=\"horz\" wrap=\"square\" lIns=\"91440\" tIns=\"45720\" rIns=\"91440\""
				+ " bIns=\"45720\" anchor=\"t\" anchorCtr=\"0\"><a:noAutofit/></wps:bodyPr></wps:wsp>"
				+ "</a:graphicData></a:graphic></wp:anchor></w:drawing></mc:Choice>"
				+ "<mc:Fallback><w:pict>" + vmlInsetBox(n, leftPt, topPt, fallbackStroke, null, content)
				+ "</w:pict></mc:Fallback></mc:AlternateContent></w:r>";
	}

	/** Words with no f in them, so nothing ligates in any of the probe faces: from one
	 *  letter to twenty, for justified-compression-decision's first lines.
	 *  @since 17.3.1 (CR-001 batch 50) */
	private static final String[] PLAIN_WORDS = ("a to in it is on an at by as or we he be do go no so up us my me "
			+ "the and was not but can are has had his her one two way day may say new old man men see own our "
			+ "out all any how now who its yet set put run sat sun ten top use try win yes also back been both "
			+ "came come does done down each even ever give good have here into just keep kind last left like "
			+ "line long look made make many more most much must name near need next only open over part same "
			+ "seem show side some such sure take than that them then they this time turn very want well went "
			+ "were what when will with word work year about above again along among began being below bring "
			+ "built carry cause close could count early earth event every great green group heard house large "
			+ "later learn least light might money never north order other paper place plant point power press "
			+ "quite reach right round seven shall short since small sound south space stand start state still "
			+ "story study table taken their there these thing think those three today total under until upper "
			+ "using value water where which while white whole whose world would write young another because "
			+ "between brought company country current develop example general however include instead machine "
			+ "measure natural nothing number certain central morning outside picture present problem process "
			+ "program provide question several special student support through without accommodated "
			+ "consideration characteristically typographical internationalization responsibility "
			+ "administration organization representative").split(" ");

	/** Advances already measured, by face and size, since a case measures hundreds of
	 *  words.  @since 17.3.1 */
	private static final java.util.Map<String, Double> ADVANCES = new java.util.HashMap<>();

	private static double advance(String text, String font, int halfPts) {
		return ADVANCES.computeIfAbsent(font + "|" + halfPts + "|" + text,
				key -> Doc.advancePoints(text, font, halfPts));
	}

	/** The letters a built word is made of: five wide and five narrow, no f.  @since 17.3.1 */
	private static final String BUILD_WIDE = "moneu";
	private static final String BUILD_NARROW = "iltrs";

	/**
	 * A nonsense word whose advance in font at halfPts comes as near targetPt as letters
	 * can bring it.  The counts (0 to 5) of five wide and five narrow letters are searched
	 * meet-in-the-middle, the fewer letters winning a tie within 0.002pt; the letters are
	 * then dealt round-robin so the word reads as one.  The caller states the width the
	 * word actually has.  @since 17.3.1 (CR-001 batch 50)
	 */
	private static String builtWord(double targetPt, String font, int halfPts) {
		double[] wide = new double[5], narrow = new double[5];
		for (int i = 0; i < 5; i++) {
			wide[i] = advance(BUILD_WIDE.substring(i, i + 1), font, halfPts);
			narrow[i] = advance(BUILD_NARROW.substring(i, i + 1), font, halfPts);
		}
		int combos = 7776; // 6^5
		double[] narrowSum = new double[combos];
		Integer[] order = new Integer[combos];
		for (int code = 0; code < combos; code++) {
			narrowSum[code] = sumOf(code, narrow);
			order[code] = code;
		}
		java.util.Arrays.sort(order, (x, y) -> Double.compare(narrowSum[x], narrowSum[y]));
		double[] sorted = new double[combos];
		for (int i = 0; i < combos; i++) sorted[i] = narrowSum[order[i]];
		int bestWide = 0, bestNarrow = 0, bestLetters = Integer.MAX_VALUE;
		double bestErr = Double.MAX_VALUE;
		for (int wc = 0; wc < combos; wc++) {
			double need = targetPt - sumOf(wc, wide);
			if (need < -0.5) continue;
			int at = java.util.Arrays.binarySearch(sorted, need);
			if (at < 0) at = -at - 1;
			for (int i = Math.max(0, at - 2); i <= Math.min(combos - 1, at + 2); i++) {
				int nc = order[i];
				double err = Math.abs(need - narrowSum[nc]);
				int letters = letterCount(wc) + letterCount(nc);
				if (letters == 0) continue;
				if (err < bestErr - 0.002 || (err < bestErr + 0.002 && letters < bestLetters)) {
					bestErr = Math.min(err, bestErr);
					bestWide = wc;
					bestNarrow = nc;
					bestLetters = letters;
				}
			}
		}
		int[] count = new int[10];
		for (int i = 0, w = bestWide, n = bestNarrow; i < 5; i++, w /= 6, n /= 6) {
			count[i] = w % 6;
			count[5 + i] = n % 6;
		}
		String letters = BUILD_WIDE + BUILD_NARROW;
		StringBuilder word = new StringBuilder();
		for (boolean any = true; any;) {
			any = false;
			for (int i : new int[] { 0, 5, 1, 6, 2, 7, 3, 8, 4, 9 }) { // m i o l n t e r u s
				if (count[i] > 0) {
					word.append(letters.charAt(i));
					count[i]--;
					any = true;
				}
			}
		}
		return word.toString();
	}

	private static double sumOf(int code, double[] widths) {
		double sum = 0;
		for (int i = 0; i < 5; i++, code /= 6) sum += (code % 6) * widths[i];
		return sum;
	}

	private static int letterCount(int code) {
		int n = 0;
		for (int i = 0; i < 5; i++, code /= 6) n += code % 6;
		return n;
	}

	/**
	 * One justified-compression-decision case, added to d: a marker line stating it, and
	 * a two-line w:jc="both" paragraph in font at 12pt whose first line has k spaces and
	 * whose next word needs every one of k + 1 spaces compressed by c per cent of the
	 * face's own space to come up, where leaving it stretches the k spaces by s per cent.
	 *
	 * <p>c depends only on k, s and the next word's advance - c = 1 - (k(1 + s) x space -
	 * next) / ((k + 1) x space) - and Liberation Sans and Serif draw letters on a grid of
	 * 1/18 em, so a built word cannot land anywhere.  k is therefore chosen from a window
	 * round the value that makes the next word about three spaces wide (6 to 36), as the
	 * one whose buildable next word comes nearest; kGiven fixes it.  The first line is
	 * then built about a point short, and the paragraph's w:ind w:right (in twips) takes
	 * up the rest, so that s is what was asked to a twentieth of a point.  labelled puts
	 * the paragraph in list 40 (w:ind left 567 hanging 567), so its first line's measure
	 * is what is left after the tab: 468 - 28.35pt.  @since 17.3.1 (CR-001 batch 50)
	 */
	private static void compressionCase(Doc d, String series, String font, int cPct, int sPct, Integer kGiven,
			boolean labelled) throws Exception {
		compressionCase(d, d::add, series, font, cPct, sPct, kGiven, labelled);
	}

	/** As above, with the marker and the case's paragraph handed to sink (a table cell's content, say)
	 *  rather than added to the body.  @since 17.3.1 (CR-001 batch 52) */
	private static void compressionCase(Doc d, java.util.function.Consumer<P> sink, String series, String font,
			int cPct, int sPct, Integer kGiven, boolean labelled) throws Exception {
		final int halfPts = 24;
		double c = cPct / 100.0, s = sPct / 100.0;
		double measure = labelled ? 468.0 - 567 / 20.0 : 468.0;
		double space = advance(" ", font, halfPts);
		int k0 = Math.max(6, Math.min(36, (int) Math.round((3.5 + 1 - c) / (s + c))));
		int k = -1;
		String next = null;
		double bestErr = Double.MAX_VALUE;
		for (int kk = kGiven != null ? kGiven : Math.max(6, k0 - 4);
				kk <= (kGiven != null ? kGiven : Math.min(36, k0 + 4)); kk++) {
			double target = kk * space * (1 + s) - (kk + 1) * space * (1 - c);
			if (target < 1.6 * space) continue;
			String word = builtWord(target, font, halfPts);
			// in c, plus a little for straying from k0, so a tie goes to the nearer k
			double err = Math.abs(advance(word, font, halfPts) - target) / ((kk + 1) * space)
					+ 0.0001 * Math.abs(kk - k0);
			if (err < bestErr) {
				bestErr = err;
				k = kk;
				next = word;
			}
		}
		if (k < 0) {
			throw new IllegalStateException(series + " c" + cPct + " s" + sPct + ": no k gives a next word");
		}
		// the first line: k + 1 words, the last of them built, about a point short of the
		// glyph width that gives s; a word may repeat, the less used much preferred
		String line = null;
		double glyphs = 0;
		for (double shortBy = 1.0; line == null || measure - glyphs - k * space * (1 + s) < 0; shortBy += 1.0) {
			if (shortBy > 20) {
				throw new IllegalStateException(series + " c" + cPct + " s" + sPct + " k" + k
						+ ": no first line under " + (measure - k * space * (1 + s)) + "pt; last " + glyphs
						+ " \"" + line + "\"");
			}
			double g = measure - k * space * (1 + s) - shortBy;
			java.util.Map<String, Integer> uses = new java.util.HashMap<>();
			StringBuilder sb = new StringBuilder();
			double sum = 0;
			for (int i = 0; i < k; i++) {
				double ideal = (g - sum) / (k + 1 - i);
				String best = null;
				double bestScore = Double.MAX_VALUE;
				for (String w : PLAIN_WORDS) {
					if (w.indexOf('f') >= 0) continue;
					// a repeat costs 5% of the wanted width: variety among long words, while a line
				// of k = 36 still gets the two-letter words it needs
				double score = Math.abs(advance(w, font, halfPts) - ideal) + 0.05 * ideal * uses.getOrDefault(w, 0);
					if (score < bestScore) {
						bestScore = score;
						best = w;
					}
				}
				uses.merge(best, 1, Integer::sum);
				sum += advance(best, font, halfPts);
				sb.append(i == 0 ? "" : " ").append(best);
			}
			sb.append(' ').append(builtWord(g - sum, font, halfPts));
			line = sb.toString();
			glyphs = Doc.advancePoints(line, font, halfPts) - k * space;
		}
		int rightTw = (int) Math.round((measure - glyphs - k * space * (1 + s)) * 20);
		double m = measure - rightTw / 20.0;
		double nextWidth = Doc.advancePoints(next, font, halfPts);
		double sBuilt = (m - glyphs) / (k * space) - 1;
		double cBuilt = 1 - (m - glyphs - nextWidth) / ((k + 1) * space);
		String id = series + "-c" + String.format("%02d", cPct) + "-s" + String.format("%02d", sPct)
				+ (kGiven != null ? "-k" + k : "");
		P marker = d.para(String.format(java.util.Locale.ROOT, "case %s, %s%s: k %d, next \"%s\" %.2fpt, "
				+ "c %.1f%%, s %.1f%%, w:ind right %d", id, font, labelled ? " numbered" : "", k, next,
				nextWidth, 100 * cBuilt, 100 * sBuilt, rightTw))
				.noLabel().font(SANS, 16).before(120).build();
		sink.accept(marker);
		Doc.Para p = d.para(line + " " + next + " " + JUST_TAIL).noLabel().font(font, halfPts)
				.jc(JcEnumeration.BOTH).suppressAutoHyphens();
		if (labelled) p.numPr(40, 0).indent(567, 0, 567);
		P built = p.build();
		if (built.getPPr().getInd() == null) built.getPPr().setInd(F.createPPrBaseInd());
		built.getPPr().getInd().setRight(BigInteger.valueOf(rightTw));
		sink.accept(built);
	}

	// ---------------------------------------------------------------- CR-001 batch 51 helpers

	/** One table-keeps document, at one compatibility mode.  @since 17.3.1 (CR-001 batch 51) */
	private static Probe tableKeepsProbe(int mode) {
		return new Probe("table-keeps-compat" + mode,
				"w:keepNext in table cells at mode " + mode + ": table-row-keepnext's R1 (every "
				+ "paragraph of rows one and two), R2 (the first cell's only) and R5 (every row, a "
				+ "paragraph after) as K1, K2, K5; then 7396's P5a, 30 exact 30pt rows each "
				+ "w:cantSplit with the first cell keepNext, started two thirds down a page (K7), "
				+ "without cantSplit (K8) and without keepNext (K9).  Read which page each row's "
				+ "tag lands on", () -> {
			Doc d = Doc.create(mode);
			String[] ids = { "K1", "K2", "K5" };
			String[] what = { "K1: keepNext on every paragraph of rows one and two.",
					"K2: keepNext on the first cell's paragraph only, rows one and two.",
					"K5: keepNext on every paragraph of every row; a paragraph follows." };
			// as table-row-keepnext: 32 filler rows of 20pt leave room for one 30pt row (K5: 29
			// leave room for the three rows but not the line after them)
			int[] fillerRows = { 32, 32, 29 };
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
					boolean keepRow = r < 3 || c == 2;
					boolean first = keepRow, last = keepRow && c != 1;
					t.rowOf(600, org.docx4j.wml.STHeightRule.EXACT,
							t.cellOf(4500, null, (first ? d.para().noLabel().keepNext() : d.para().noLabel())
									.text(ids[c] + " row " + r + " cell one").build()),
							t.cellOf(4500, null, (last ? d.para().noLabel().keepNext() : d.para().noLabel())
									.text(ids[c] + " row " + r + " cell two").build()));
				}
				d.add(t.build());
				d.para(ids[c] + " after the table. " + prose(1, c + 1)).after(0).add();
			}
			// P5a: 20 exact 24pt fillers (480pt of the 697.9pt body), then 30 rows of 30pt
			for (String v : new String[] { "K7", "K8", "K9" }) {
				d.pageBreak();
				for (int i = 1; i <= 20; i++) {
					d.para(v + " filler " + i).noLabel().line(480, STLineSpacingRule.EXACT).add();
				}
				Doc.Table t = new Doc.Table(4500, 4500);
				for (int r = 1; r <= 30; r++) {
					String tag = v + " r" + String.format("%02d", r);
					Doc.Para first = d.para().noLabel().text(tag);
					if (!"K9".equals(v)) first.keepNext();
					t.rowOf(600, org.docx4j.wml.STHeightRule.EXACT, t.cellOf(4500, null, first.build()),
							t.cellOf(4500, null, d.para().noLabel().text(tag + " second cell").build()));
				}
				Tbl tbl = t.build();
				if (!"K8".equals(v)) cantSplitEveryRow(tbl);
				d.add(tbl);
				d.para(v + " after the table. " + prose(1, 3)).add();
			}
			return d.pkg();
		});
	}

	/** One keep-chain-overlong document, at one compatibility mode.  @since 17.3.1 */
	private static Probe keepChainProbe(int mode) {
		return new Probe("keep-chain-overlong-compat" + mode,
				"chains of w:keepNext paragraphs (three sentences each, the last without "
				+ "keepNext) of about half a page (C05), one and a half (C15) and two and a half "
				+ "(C25), started two thirds down a page; C15 with widow control off (C15W) and "
				+ "with the keepNext on a paragraph style (C15S); mode " + mode + ".  Read where "
				+ "each chain starts and where Word breaks inside it", () -> {
			Doc d = Doc.create(mode);
			d.addParagraphStyle("KeepStyle", null, ppr -> ppr.setKeepNext(new BooleanDefaultTrue()));
			Object[][] cases = { { "C05", 7, false, false }, { "C15", 21, false, false },
					{ "C25", 35, false, false }, { "C15W", 21, true, false }, { "C15S", 21, false, true } };
			for (Object[] c : cases) {
				String tag = (String) c[0];
				int n = (Integer) c[1];
				boolean widowOff = (Boolean) c[2], viaStyle = (Boolean) c[3];
				for (int i = 1; i <= 20; i++) {
					Doc.Para f = d.para(tag + " filler " + i).noLabel().line(480, STLineSpacingRule.EXACT);
					if (i == 1) f.pageBreakBefore();
					f.add();
				}
				for (int i = 1; i <= n; i++) {
					Doc.Para p = d.para(tag + " p" + String.format("%02d", i) + ". " + prose(3, i)).noLabel();
					if (i < n) {
						if (viaStyle) p.style("KeepStyle"); else p.keepNext();
					}
					if (widowOff) p.widowControl(false);
					p.add();
				}
				d.para(tag + " after the chain. " + prose(1, n)).noLabel().add();
			}
			return d.pkg();
		});
	}

	/** One table-cellspacing-pitch document, at one compatibility mode.  @since 17.3.1 */
	private static Probe cellSpacingPitchProbe(int mode) {
		return new Probe("table-cellspacing-pitch-compat" + mode,
				"six fixed 1500-twip columns whose cells hold a left-aligned and a right-aligned "
				+ "tag, with w:tblCellSpacing 28 and 72 twips on w:tblPr only (P28, P72), on every "
				+ "w:trPr only (R28, R72) and on both (B28, B72); mode " + mode + ".  Read each "
				+ "cell's content edges: Word's column pitch and cell width", () -> {
			Doc d = Doc.create(mode);
			for (String where : new String[] { "P", "R", "B" }) {
				for (int sp : new int[] { 28, 72 }) {
					String tag = where + sp;
					d.para(tag + ": w:tblCellSpacing " + sp + " twips "
							+ ("P".equals(where) ? "on w:tblPr" : "R".equals(where) ? "on every w:trPr" : "on both"))
							.before(240).after(120).add();
					Doc.Table t = new Doc.Table(1500, 1500, 1500, 1500, 1500, 1500).fixedLayout();
					if (!"R".equals(where)) t.cellSpacing(sp);
					for (int r = 1; r <= 2; r++) {
						org.docx4j.wml.Tc[] cells = new org.docx4j.wml.Tc[6];
						for (int c = 0; c < 6; c++) {
							String cell = tag + "r" + r + "c" + (c + 1);
							cells[c] = t.cellOf(1500, null,
									d.para(cell + "L").noLabel().build(),
									d.para(cell + "R").noLabel().jc(JcEnumeration.RIGHT).build());
						}
						t.rowOf(null, null, cells);
					}
					Tbl tbl = t.build();
					if (!"P".equals(where)) {
						for (Object o : tbl.getContent()) {
							if (!(o instanceof org.docx4j.wml.Tr)) continue;
							org.docx4j.wml.Tr tr = (org.docx4j.wml.Tr) o;
							if (tr.getTrPr() == null) tr.setTrPr(F.createTrPr());
							org.docx4j.wml.TblWidth w = F.createTblWidth();
							w.setW(BigInteger.valueOf(sp));
							w.setType("dxa");
							tr.getTrPr().getCnfStyleOrDivIdOrGridBefore().add(F.createCTTrPrBaseTblCellSpacing(w));
						}
					}
					d.add(tbl);
				}
			}
			d.para("after. " + prose(1)).before(240).add();
			return d.pkg();
		});
	}

	// ---------------------------------------------------------------- CR-001 batch 52 (ledger8 §5)

	/** A paragraph holding nothing but a page break, with no properties of its own (Normal's).
	 *  @since 17.3.1 (CR-001 batch 52) */
	private static P breakOnlyParagraph() {
		P p = F.createP();
		R r = F.createR();
		Br br = F.createBr();
		br.setType(STBrType.PAGE);
		r.getContent().add(br);
		p.getContent().add(r);
		return p;
	}

	/**
	 * pagebreak-paragraph-compat&lt;mode&gt; (ledger8 item 19).  Where does a break-only paragraph's mark go, and
	 * what becomes of the paragraph after the break?  In mode 15 Word keeps the mark on the page before the
	 * break (13347, 2703, 4372, 1372: 11 of the 50 mode-15 class 2 documents with break-only paragraphs miss
	 * Word's page count, against 7% without); below 15 the mark's line is thought to go to the next page
	 * (ledger7 item 6, docx4j's parked pageBreakParagraphLine), except before a table (rules §3.3).  Each case
	 * opens a page (pageBreakBefore on its first paragraph):
	 * A a paragraph, the break-only paragraph, then a paragraph with 12pt before;
	 * B the same with a one-row table after the break;
	 * C with a 16pt heading carrying pageBreakBefore after it (13347: is there a blank page?);
	 * D the page filled by 29 exact 24pt lines (1.9pt left) before the break;
	 * E 28 exact 24pt lines (25.9pt left: room for the mark's line);
	 * F the break-only paragraph as the document's last.
	 * Read: the page count; the first baseline on each case's second page; any blank page.
	 * @since 17.3.1 (CR-001 batch 52)
	 */
	private static Probe pagebreakParagraphProbe(int mode) {
		return new Probe("pagebreak-paragraph-compat" + mode,
				"break-only paragraphs (w:br type=page alone, Normal's properties) at mode " + mode + ": mid-page "
				+ "followed by a paragraph with 12pt before (A), a table (B) and a pageBreakBefore heading (C); at "
				+ "a page foot with 1.9pt left (D) and 25.9pt left (E); and as the document's last paragraph (F). "
				+ "Read the page count, the first baseline on each case's second page, and any blank page", () -> {
			Doc d = Doc.create(mode);
			String[] cases = { "A", "B", "C", "D", "E" };
			for (int i = 0; i < cases.length; i++) {
				String c = cases[i];
				if ("D".equals(c) || "E".equals(c)) {
					int lines = "D".equals(c) ? 29 : 28;
					for (int k = 1; k <= lines; k++) {
						Doc.Para f = d.para(k == 1 ? c + ". " + lines + " exact 24pt lines, then a break-only paragraph"
								: c + " line " + k + " of " + lines).noLabel().font(SERIF, 24)
								.before(0).after(0).line(480, STLineSpacingRule.EXACT);
						if (k == 1) f.pageBreakBefore();
						f.add();
					}
				} else {
					Doc.Para t = d.para(c + ". A paragraph, then a break-only paragraph, then "
							+ ("A".equals(c) ? "a paragraph with 12pt before" : "B".equals(c) ? "a table"
									: "a heading with pageBreakBefore") + ". " + prose(3, i)).noLabel();
					if (i > 0) t.pageBreakBefore();
					t.add();
				}
				d.add(breakOnlyParagraph());
				if ("B".equals(c)) {
					Doc.Table t = new Doc.Table(4513, 4513);
					t.rowOf(null, null, t.cell("B cell one", SERIF, 24, 1, 4513), t.cell("B cell two", SERIF, 24, 1, 4513));
					d.add(t.build());
					d.para("B after the table. " + prose(1, i)).noLabel().add();
				} else if ("C".equals(c)) {
					Doc.Para h = d.para("C heading with pageBreakBefore").noLabel().font(SERIF, 32);
					h.pageBreakBefore();
					h.add();
					d.para("C after the heading. " + prose(1, i)).noLabel().add();
				} else {
					d.para(c + " after: 12pt before. " + prose(1, i)).noLabel().before(240).add();
				}
			}
			Doc.Para last = d.para("F. The break-only paragraph after this one is the document's last paragraph. "
					+ prose(1, 7)).noLabel();
			last.pageBreakBefore();
			last.add();
			d.add(breakOnlyParagraph());
			return d.pkg();
		});
	}

	/** A footer paragraph: the label, then a PAGE field (cached "1").  @since 17.3.1 (CR-001 batch 52) */
	private static P folioParagraph(String label) {
		P p = Doc.plainParagraph(label + " ", SANS, 18);
		org.docx4j.wml.CTSimpleField f = F.createCTSimpleField();
		f.setInstr(" PAGE ");
		f.getContent().add(Doc.run("1", SANS, 18, null));
		p.getContent().add(F.createPFldSimple(f));
		return p;
	}

	/**
	 * page-number-restart-parity (ledger8 item 21; 9539, 8695).  Does Word insert a blank page so that a
	 * restarted folio does not repeat the parity of the one before it, and how does an oddPage or evenPage
	 * section read a restarted folio?  On 9539 (mode 14, evenAndOddHeaders) Word puts a wholly blank page
	 * before each of two sections restarting at 7 after an odd folio; docx4j none.  On 8695 an oddPage section
	 * restarting at 3 gets no blank page in Word, one in docx4j (end-on-even).  Eight sections of one page:
	 * S1 from 1; then nextPage at 7, nextPage at 7 again, nextPage at 8, oddPage at 3, oddPage at 4, evenPage
	 * at 5, evenPage at 6.  The footer prints the folio; the -eo variant has evenAndOddHeaders, with odd and
	 * even footers.  Read: the page count, each page's footer, and which pages are blank.
	 * @since 17.3.1 (CR-001 batch 52)
	 */
	private static Probe folioParityProbe(boolean evenOdd) {
		return new Probe("page-number-restart-parity" + (evenOdd ? "-eo" : ""),
				"eight one-page sections, mode 14" + (evenOdd ? ", w:evenAndOddHeaders with odd and even footers" : "")
				+ ": S1 from folio 1, then nextPage sections restarting at 7, 7 and 8, oddPage at 3 and 4, evenPage "
				+ "at 5 and 6; the footer prints the folio.  Read the page count, the footers and the blank pages",
				() -> {
			Doc d = Doc.create(14);
			if (evenOdd) {
				d.addFooter(org.docx4j.wml.HdrFtrRef.DEFAULT, java.util.List.of(folioParagraph("odd footer, folio")));
				d.addFooter(org.docx4j.wml.HdrFtrRef.EVEN, java.util.List.of(folioParagraph("even footer, folio")));
			} else {
				d.addFooter(org.docx4j.wml.HdrFtrRef.DEFAULT, java.util.List.of(folioParagraph("footer, folio")));
			}
			String[] types = { null, "nextPage", "nextPage", "nextPage", "oddPage", "oddPage", "evenPage", "evenPage" };
			int[] starts = { 0, 7, 7, 8, 3, 4, 5, 6 };
			for (int i = 0; i < types.length; i++) {
				d.para("S" + (i + 1) + ": " + (types[i] == null ? "the first section, folios from 1"
						: "a " + types[i] + " section, w:pgNumType start " + starts[i]) + ". " + prose(2, i))
						.noLabel().add();
				if (i + 1 < types.length) {
					d.endSection(types[i + 1]);
					d.sectPr().setPgNumType(null);
					d.pageNumberStart(starts[i + 1]);
				}
			}
			return d.pkg();
		});
	}

	/**
	 * section-break-page-size-change (ledger8 item 21; 9539 p2).  A paragraph ending in a page break, an empty
	 * paragraph closing its section, and a continuous section of another page size after it: Word gives the
	 * size change its new page and the break none of its own; docx4j gives both (+1 page on 9539).  C1 that
	 * shape (A4 portrait, then continuous A4 landscape); C2 the control with no size change (continuous, A4
	 * portrait again); C3 a nextPage section of the other size.  Read: the page count and the page each case's
	 * "after" text is on.  @since 17.3.1 (CR-001 batch 52)
	 */
	private static Probe sizeChangeBreakProbe() {
		return new Probe("section-break-page-size-change",
				"a paragraph ending in a page break, then an empty paragraph closing its section, then: a continuous "
				+ "section of another page size (C1), a continuous section of the same size (C2), a nextPage section "
				+ "of another size (C3); mode 14.  Read the page count and where each case's after-text lands", () -> {
			Doc d = Doc.create(14);
			String[] cases = { "C1", "C2", "C3" };
			String[] next = { "continuous", "continuous", "nextPage" };
			boolean[] landscapeAfter = { true, false, true };
			boolean landscape = false;
			for (int i = 0; i < cases.length; i++) {
				P p = d.para(cases[i] + ": this paragraph ends in a page break; its section's last paragraph is empty, "
						+ "and the next section is " + next[i] + (landscapeAfter[i] != landscape ? ", of another page size"
								: ", of the same size") + ". " + prose(1, i)).noLabel().build();
				R r = F.createR();
				Br br = F.createBr();
				br.setType(STBrType.PAGE);
				r.getContent().add(br);
				p.getContent().add(r);
				d.add(p);
				d.endSection(next[i]);
				landscape = landscapeAfter[i];
				if (landscape) d.pageGeometry(16838, 11906, true, 1440, 1440, 1440, 1440);
				else d.pageGeometry(11906, 16838, false, 1440, 1440, 1440, 1440);
				d.para(cases[i] + " after: the first paragraph of the next section. " + prose(1, i + 3)).noLabel().add();
				if (i + 1 < cases.length) {
					d.endSection("nextPage");
					landscape = false;
					d.pageGeometry(11906, 16838, false, 1440, 1440, 1440, 1440);
				}
			}
			return d.pkg();
		});
	}

	/**
	 * sdt-appearance-tags (ledger8 item 2; 1277, 9775).  Word draws a content control whose w15:appearance is
	 * "tags" with its start and end tags in the page (Tahoma 8pt on 1277), and a block control's end tag takes
	 * a line of its own; docx4j draws nothing (1277: 7 pages short).  Block controls of two paragraphs with
	 * appearance tags (T1), boundingBox (T3), hidden (T4) and none (T5); inline controls in mid-line with
	 * tags and an alias (T2), tags and only a w:tag (T6), and boundingBox (T7).  A plain paragraph before and
	 * after each block.  Read: what is drawn for each tag (text, font, size, position) and the lines it takes.
	 * @since 17.3.1 (CR-001 batch 52)
	 */
	private static Probe sdtTagsProbe() {
		return new Probe("sdt-appearance-tags",
				"content controls with w15:appearance tags, boundingBox, hidden and none: block controls of two "
				+ "paragraphs (T1 tags, T3 boundingBox, T4 hidden, T5 none) between plain paragraphs, and inline "
				+ "controls (T2 tags with an alias, T6 tags with only a tag, T7 boundingBox); mode 15.  Read what "
				+ "Word draws for each tag and the lines it takes", () -> {
			Doc d = Doc.create(15);
			String ns = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
					+ "xmlns:w15=\"http://schemas.microsoft.com/office/word/2012/wordml\"";
			StringBuilder body = new StringBuilder();
			String[][] blocks = { { "T1", "tags", "Block control" }, { "T3", "boundingBox", "Bounding box" },
					{ "T4", "hidden", "Hidden control" }, { "T5", null, "No appearance" } };
			int id = 52001, k = 0;
			for (String[] b : blocks) {
				body.append(plainXml(b[0] + " before: a plain paragraph. " + prose(1, k++)));
				body.append("<w:sdt>").append(sdtPrXml(b[2], b[0], id++, b[1])).append("<w:sdtContent>")
						.append(plainXml(b[0] + " first paragraph inside the control. " + prose(1, k++)))
						.append(plainXml(b[0] + " second paragraph inside the control."))
						.append("</w:sdtContent></w:sdt>");
				body.append(plainXml(b[0] + " after: a plain paragraph. " + prose(1, k++)));
			}
			String[][] inlines = { { "T2", "tags", "Inline control" }, { "T6", "tags", null }, { "T7", "boundingBox", "Inline box" } };
			for (String[] b : inlines) {
				body.append("<w:p><w:r><w:t xml:space=\"preserve\">").append(b[0])
						.append(" text before an inline control </w:t></w:r><w:sdt>")
						.append(sdtPrXml(b[2], b[0], id++, b[1]))
						.append("<w:sdtContent><w:r><w:t>inline content</w:t></w:r></w:sdtContent></w:sdt>")
						.append("<w:r><w:t xml:space=\"preserve\"> and the text after it. ").append(prose(1, k++))
						.append("</w:t></w:r></w:p>");
			}
			org.docx4j.wml.Document doc = (org.docx4j.wml.Document) org.docx4j.XmlUtils.unmarshalString(
					"<w:document " + ns + "><w:body>" + body + "</w:body></w:document>");
			d.para("Content controls, w15:appearance. " + prose(1)).noLabel().add();
			d.mdp().getContent().addAll(doc.getBody().getContent());
			return d.pkg();
		});
	}

	private static String plainXml(String text) {
		String escaped = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
		return "<w:p><w:r><w:t xml:space=\"preserve\">" + escaped + "</w:t></w:r></w:p>";
	}

	private static String sdtPrXml(String alias, String tag, int id, String appearance) {
		return "<w:sdtPr>" + (alias == null ? "" : "<w:alias w:val=\"" + alias + "\"/>")
				+ "<w:tag w:val=\"" + tag + "\"/><w:id w:val=\"" + id + "\"/>"
				+ (appearance == null ? "" : "<w15:appearance w15:val=\"" + appearance + "\"/>") + "</w:sdtPr>";
	}

	/**
	 * table-grid-pct-autofit-compat&lt;mode&gt; (ledger8 item 1).  How wide does Word draw an autofit table whose
	 * w:tblW is a percentage?  Below mode 15 the corpora's re-saved grids are the column plus 216tw - both
	 * outer cell margins - in 24 of 28 such tables (545, 3229, slice D), and the column itself in 11 of 12 at
	 * mode 15; docx4j draws the column.  G1 5000 pct, cells auto, Word's default margins; G2 cells 2500 pct
	 * each; G3 tblCellMar left/right 0; G4 200; G5 4000 pct; G6 no borders.  Each cell holds a left-aligned
	 * and a right-aligned tag.  Read: the tags' x (the content edges) and the drawn borders.
	 * @since 17.3.1 (CR-001 batch 52)
	 */
	private static Probe pctAutofitGridProbe(int mode) {
		return new Probe("table-grid-pct-autofit-compat" + mode,
				"autofit tables with w:tblW in pct at mode " + mode + ": 5000 pct with auto cells (G1), 2500 pct "
				+ "cells (G2), tblCellMar left/right 0 (G3) and 200 (G4), 4000 pct (G5), no borders (G6); each cell "
				+ "a left- and a right-aligned tag.  Read the tags' x and the drawn borders", () -> {
			Doc d = Doc.create(mode);
			Object[][] cases = {
				{ "G1", 5000, null, null, false }, { "G2", 5000, 2500, null, false }, { "G3", 5000, null, 0, false },
				{ "G4", 5000, null, 200, false }, { "G5", 4000, null, null, false }, { "G6", 5000, null, null, true } };
			for (Object[] c : cases) {
				String tag = (String) c[0];
				int pct = (Integer) c[1];
				Integer tcPct = (Integer) c[2], mar = (Integer) c[3];
				boolean noBorders = (Boolean) c[4];
				d.para(tag + ": w:tblW " + pct + " pct, autofit, cells " + (tcPct == null ? "auto" : tcPct + " pct")
						+ ", cell margins " + (mar == null ? "Word's default" : mar + "tw") + (noBorders ? ", no borders"
								: ", 0.5pt borders") + ". " + prose(1)).noLabel().before(240).after(120).add();
				Doc.Table t = new Doc.Table(4513, 4513);
				t.tableWidth(pct, "pct");
				if (mar != null) t.cellMargins(mar, 0);
				if (noBorders) t.noBorders();
				org.docx4j.wml.Tc[] cells = new org.docx4j.wml.Tc[2];
				for (int k = 0; k < 2; k++) {
					String id = tag + "c" + (k + 1);
					P right = Doc.plainParagraph(id + "R", SERIF, 24);
					org.docx4j.wml.Jc jc = F.createJc();
					jc.setVal(JcEnumeration.RIGHT);
					right.getPPr().setJc(jc);
					org.docx4j.wml.Tc tc = t.cellOf(Doc.plainParagraph(id + "L", SERIF, 24), right);
					if (tcPct != null) {
						org.docx4j.wml.TblWidth w = F.createTblWidth();
						w.setW(BigInteger.valueOf(tcPct));
						w.setType("pct");
						tc.getTcPr().setTcW(w);
					}
					cells[k] = tc;
				}
				t.rowOf(null, null, cells);
				d.add(t.build());
			}
			d.para("after. " + prose(1)).before(240).add();
			return d.pkg();
		});
	}

	/** A border for a w:tcBorders side: single, eighthsOfPoint wide, or w:val="nil" where 0. */
	private static org.docx4j.wml.CTBorder cellBorder(int eighthsOfPoint) {
		org.docx4j.wml.CTBorder b = F.createCTBorder();
		if (eighthsOfPoint <= 0) {
			b.setVal(org.docx4j.wml.STBorder.NIL);
		} else {
			b.setVal(org.docx4j.wml.STBorder.SINGLE);
			b.setSz(BigInteger.valueOf(eighthsOfPoint));
			b.setSpace(BigInteger.ZERO);
			b.setColor("000000");
		}
		return b;
	}

	/**
	 * table-cell-measure-neighbour (ledger8 §2, item 7; 13743).  What measure does Word give a cell's text when
	 * the border between it and its neighbour is collapsed?  docx4j gives back a cell's own borders; FOP also
	 * charges half of a neighbour's collapsed border, so 13743's cells are 0.48pt narrow (64 lines).  Two fixed
	 * 4513tw columns, Word's default margins: N1 the left cell has a 1pt right border, the right cell a nil left
	 * one; N2 the reverse (the right cell's own 1pt left border); N3 both 1pt; N4 no borders; N5 0.5pt all round
	 * (table borders).  In the right cell, six paragraphs whose first line's natural width is the nominal measure
	 * (225.65 - 10.8 = 214.85pt) plus 0.72, 0.48, 0.24 less and 0.24, 0.48, 0.72 more, the difference set by a
	 * right indent; each marker states it.  Read: which first lines keep their last word.
	 * @since 17.3.1 (CR-001 batch 52)
	 */
	private static Probe cellNeighbourBorderProbe() {
		return new Probe("table-cell-measure-neighbour",
				"two fixed 4513tw columns with collapsed borders: the left cell's 1pt right border against a nil "
				+ "left one (N1), the reverse (N2), both 1pt (N3), none (N4), 0.5pt all round (N5); the right "
				+ "cell's lines are its nominal measure (214.85pt) +/- 0.24, 0.48, 0.72pt wide.  Read which lines "
				+ "keep their last word", () -> {
			Doc d = Doc.create(15);
			final String font = SERIF;
			final int halfPts = 24;
			final double nominal = 4513 / 20.0 - 2 * 5.4;
			double[] deltas = { -0.72, -0.48, -0.24, 0.24, 0.48, 0.72 };
			int[][] borders = { { 8, 0 }, { 0, 8 }, { 8, 8 }, { 0, 0 }, { -1, -1 } }; // left cell's right, right cell's left; -1: table 0.5pt
			String[] tags = { "N1", "N2", "N3", "N4", "N5" };
			String[] words = longProse(6, 3).split(" ");
			for (int t = 0; t < tags.length; t++) {
				d.para(tags[t] + ": " + (borders[t][0] < 0 ? "0.5pt table borders all round"
						: "left cell's right border " + (borders[t][0] / 8.0) + "pt, right cell's left border "
								+ (borders[t][1] / 8.0) + "pt") + ". " + prose(1, t)).noLabel().before(240).after(120).add();
				Doc.Table tb = new Doc.Table(4513, 4513).fixedLayout();
				if (borders[t][0] >= 0) tb.noBorders();
				java.util.List<P> right = new java.util.ArrayList<>();
				int w = t * 7;
				for (double delta : deltas) {
					StringBuilder line = new StringBuilder(tags[t] + (delta > 0 ? "+" : "") + delta);
					while (Doc.advancePoints(line + " " + words[w % words.length], font, halfPts) < nominal - 3.0) {
						line.append(' ').append(words[w++ % words.length]);
					}
					double natural = Doc.advancePoints(line.toString(), font, halfPts);
					int rightTw = (int) Math.round((nominal - (natural - delta)) * 20);
					right.add(Doc.plainParagraph(String.format(java.util.Locale.ROOT, "%s %+.2f: natural %.2f, ind right %d",
							tags[t], delta, natural, rightTw), SANS, 14));
					P p = Doc.plainParagraph(line + " " + words[w % words.length] + " " + words[(w + 1) % words.length]
							+ " " + words[(w + 2) % words.length], font, halfPts);
					w += 3;
					if (p.getPPr().getInd() == null) p.getPPr().setInd(F.createPPrBaseInd());
					p.getPPr().getInd().setRight(BigInteger.valueOf(rightTw));
					right.add(p);
				}
				org.docx4j.wml.Tc a = tb.cellOf(4513, null, Doc.plainParagraph(tags[t] + " left cell", SERIF, 24));
				org.docx4j.wml.Tc b = tb.cellOf(4513, null, right.toArray(new P[0]));
				if (borders[t][0] >= 0) {
					org.docx4j.wml.TcPrInner.TcBorders ab = F.createTcPrInnerTcBorders();
					ab.setRight(cellBorder(borders[t][0]));
					a.getTcPr().setTcBorders(ab);
					org.docx4j.wml.TcPrInner.TcBorders bb = F.createTcPrInnerTcBorders();
					bb.setLeft(cellBorder(borders[t][1]));
					b.getTcPr().setTcBorders(bb);
				}
				tb.rowOf(null, null, a, b);
				d.add(tb.build());
			}
			return d.pkg();
		});
	}

	/**
	 * justified-compression-cap (ledger8 §2, item 7; 11741, 1035, 3959).  Batch 50's rule is that Word
	 * compresses iff c &lt; s/2, capped at 24%.  Word took 24.4-25.0% (11741 p10 needs 24.5%), compressed on
	 * c = s/2 ties, and refused c = 16% against s = 75% in a table cell (3959).  Body cases at c 24, 25, 26%
	 * against s 80% in Liberation Serif and Sans; ties c = s/2 at (10, 20), (12, 24), (15, 30); and in a
	 * one-cell table whose measure is the page's 468pt, c 16 against s 75 and the controls c 10 against s 40
	 * and c 20 against s 80.  Each marker states the built c, s and k.  Read: did the next word come up?
	 * @since 17.3.1 (CR-001 batch 52)
	 */
	private static Probe compressionCapProbe() {
		return new Probe("justified-compression-cap",
				"justified lines built as in justified-compression-decision: c 24, 25, 26% against s 80% in "
				+ "Liberation Serif and Sans, the ties c = s/2 at 10/20, 12/24, 15/30, and in a one-cell table of "
				+ "the page's 468pt c 16/s 75, 10/40 and 20/80.  Read whether each next word came up", () -> {
			Doc d = Doc.create(15);
			d.pageGeometry(12240, 15840, false, 1440, 1440, 1440, 1440);
			d.documentDefaultRun(SANS, 24);
			for (String font : new String[] { SERIF, SANS }) {
				for (int c : new int[] { 24, 25, 26 }) compressionCase(d, "cap", font, c, 80, null, false);
			}
			for (int[] cs : new int[][] { { 10, 20 }, { 12, 24 }, { 15, 30 } }) {
				compressionCase(d, "tie", SERIF, cs[0], cs[1], null, false);
			}
			d.para("In a table cell whose measure is the page's 468pt (no margins, no borders):").noLabel()
					.before(240).add();
			Doc.Table t = new Doc.Table(9360).fixedLayout().noBorders().cellMargins(0, 0);
			java.util.List<P> inCell = new java.util.ArrayList<>();
			for (int[] cs : new int[][] { { 16, 75 }, { 10, 40 }, { 20, 80 } }) {
				compressionCase(d, inCell::add, "cell", SERIF, cs[0], cs[1], null, false);
			}
			t.rowOf(null, null, t.cellOf(9360, null, inCell.toArray(new P[0])));
			d.add(t.build());
			d.para("after. " + prose(1)).before(240).add();
			return d.pkg();
		});
	}

	/**
	 * keep-chain-room-compat&lt;mode&gt; (ledger8 item 9; 2514).  A keepNext chain a little taller than the room left
	 * (but shorter than a page): on 2514 (mode 14, two columns) Word moves the whole 587pt chain to the next
	 * column with 582pt free, because the chain's last paragraph would split 1+1, widow control forbids it, and
	 * keepNext cascades back; docx4j breaks it in place.  keep-chain-overlong's chains, longer than a page,
	 * break inside a paragraph instead.  Exact 24pt lines; each case opens a page with 10 filler lines,
	 * leaving 457.9pt: K1 18 one-line keepNext paragraphs then a two-line paragraph (the break would split it
	 * 1+1); K2 17 then a five-line paragraph (2+3, which widow control allows); K3 19 one-line keepNext
	 * paragraphs then a one-line paragraph (the break falls between two chain paragraphs); K4 K1 in the second
	 * section's two columns.  Read: where each chain starts, and where it breaks.
	 * @since 17.3.1 (CR-001 batch 52)
	 */
	private static Probe keepChainRoomProbe(int mode) {
		return new Probe("keep-chain-room-compat" + mode,
				"keepNext chains of exact 24pt lines a little taller than the 457.9pt left after 10 filler lines, "
				+ "shorter than a page: ending in a two-line paragraph that would split 1+1 (K1), a five-line one "
				+ "(K2), a one-line one with the break between chain paragraphs (K3), and K1 in two columns (K4); "
				+ "mode " + mode + ".  Read where each chain starts and breaks", () -> {
			Doc d = Doc.create(mode);
			Object[][] cases = { { "K1", 18, 2 }, { "K2", 17, 5 }, { "K3", 19, 1 }, { "K4", 18, 2 } };
			for (int i = 0; i < cases.length; i++) {
				String tag = (String) cases[i][0];
				int chain = (Integer) cases[i][1], lastLines = (Integer) cases[i][2];
				if ("K4".equals(tag)) {
					d.endSection("nextPage");
					d.equalColumns(2, 708);
				}
				for (int k = 1; k <= 10; k++) {
					Doc.Para f = d.para(tag + " filler " + k).noLabel().before(0).after(0)
							.line(480, STLineSpacingRule.EXACT);
					if (k == 1 && i > 0 && !"K4".equals(tag)) f.pageBreakBefore();
					f.add();
				}
				for (int k = 1; k <= chain; k++) {
					d.para(tag + " chain " + String.format("%02d", k) + " (keepNext)").noLabel().before(0).after(0)
							.line(480, STLineSpacingRule.EXACT).keepNext().add();
				}
				Doc.Para last = d.para(tag + " last paragraph, " + lastLines + " line" + (lastLines > 1 ? "s" : "")
						+ ", line 1").noLabel().before(0).after(0).line(480, STLineSpacingRule.EXACT);
				for (int l = 2; l <= lastLines; l++) {
					last.softReturn();
					last.text(tag + " last paragraph, line " + l);
				}
				last.add();
				d.para(tag + " after the chain.").noLabel().before(0).after(0).line(480, STLineSpacingRule.EXACT).add();
			}
			return d.pkg();
		});
	}

	/**
	 * ccmp-text-layer (§6.6 item 42; fop/CR-016).  The text Word's PDF and docx4j's give for Cambria Regular
	 * letters that its GSUB ccmp decomposes (accented Latin and Greek), beside their plain forms; Cambria Bold,
	 * which has no such decompositions, as the control.  Read: the extracted text, against the source.
	 * @since 17.3.1 (CR-001 batch 52)
	 */
	private static Probe ccmpTextLayerProbe() {
		return new Probe("ccmp-text-layer",
				"Cambria Regular and Bold lines mixing letters its ccmp decomposes with their plain forms "
				+ "(à a é e ü u ć c ά α ό ο Å A) and two sentences of Greek and of accented French; mode 15.  "
				+ "Read the extracted text against the source", () -> {
			Doc d = Doc.create(15);
			String[] lines = {
				"à a é e ü u ć c ά α ό ο έ ε ή η ί ι ύ υ ώ ω Å A",
				"Η παρούσα μεταπτυχιακή διατριβή υποβλήθηκε, ο κανόνας και η ομάδα, α β γ δ ε ζ η θ ι κ λ μ ν ξ ο π ρ σ τ υ φ χ ψ ω.",
				"Le café et la naïveté, à côté de l'été : une forêt, un château et Ålesund, Øre, Ação." };
			for (boolean bold : new boolean[] { false, true }) {
				for (int i = 0; i < lines.length; i++) {
					d.para().noLabel().run((bold ? "B" : "R") + (i + 1) + " " + lines[i], "Cambria", 24,
							bold ? Doc::bold : null).add();
				}
			}
			return d.pkg();
		});
	}

	/**
	 * break-after-space-uax14 (ledger8 item 15).  Word breaks at a space before ',' '.' '?' and after a closing
	 * quote before '(' or '[' (69 lines over the 80 near misses), where FOP's UAX #14 pair table refuses (LB13:
	 * no break before IS/CL/EX even after spaces).  Each case is a ragged paragraph whose first line's measure,
	 * set by a right indent, holds "... WORD" with 1pt to spare but not "... WORD ," : if Word may break at the
	 * space the line ends with WORD; if not, WORD goes down with its punctuation.  Cases: ' ,' ' .' ' ?' ' :'
	 * ' ;' ' !' ' …', and '” (' and '» ['.  Read: the last word of each first line.
	 * @since 17.3.1 (CR-001 batch 52)
	 */
	private static Probe breakAtSpaceProbe() {
		return new Probe("break-after-space-uax14",
				"ragged paragraphs whose first line holds \"... WORD\" with 1pt to spare but not what follows: "
				+ "a space then , . ? : ; ! or an ellipsis, or a closing quote, a space and ( or [; mode 15.  Read "
				+ "whether Word breaks at the space", () -> {
			Doc d = Doc.create(15);
			final String font = SERIF;
			final int halfPts = 24;
			final double measure = 9026 / 20.0;
			String[][] cases = { { "S1", " ,", "comma" }, { "S2", " .", "period" }, { "S3", " ?", "question" },
					{ "S4", " :", "colon" }, { "S5", " ;", "semicolon" }, { "S6", " !", "exclamation" },
					{ "S7", " …", "ellipsis" }, { "S8", "” (", "quote then paren" },
					{ "S9", "» [", "guillemet then bracket" } };
			String[] words = longProse(8, 5).split(" ");
			int w = 0;
			for (String[] c : cases) {
				StringBuilder line = new StringBuilder(c[0]);
				while (Doc.advancePoints(line + " " + words[w % words.length], font, halfPts) < measure - 40) {
					line.append(' ').append(words[w++ % words.length]);
				}
				line.append(" WORD");
				double holds = Doc.advancePoints(line.toString(), font, halfPts);
				int rightTw = (int) Math.round((measure - holds - 1.0) * 20);
				d.para(String.format(java.util.Locale.ROOT, "%s (%s): the first line holds %.2fpt; ind right %d",
						c[0], c[2], holds, rightTw)).noLabel().font(SANS, 14).before(120).add();
				P p = Doc.plainParagraph(line + c[1] + " tail " + prose(1, w), font, halfPts);
				if (p.getPPr().getInd() == null) p.getPPr().setInd(F.createPPrBaseInd());
				p.getPPr().getInd().setRight(BigInteger.valueOf(rightTw));
				d.add(p);
			}
			return d.pkg();
		});
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

	// ------------------------------------------------------------------ CR-031 phase 0

	/** The current section's vertical w:pgMar, in twips. */
	private static void verticalMargins(Doc d, int top, int bottom, int header, int footer) {
		org.docx4j.wml.SectPr.PgMar m = d.sectPr().getPgMar();
		m.setTop(BigInteger.valueOf(top));
		m.setBottom(BigInteger.valueOf(bottom));
		m.setHeader(BigInteger.valueOf(header));
		m.setFooter(BigInteger.valueOf(footer));
	}

	/** A header and a footer naming the section and printing the folio. */
	private static void sectionHeaderFooter(Doc d, String section) throws Exception {
		d.addHeader(org.docx4j.wml.HdrFtrRef.DEFAULT, java.util.List.of(folioParagraph(section + " header, folio")));
		d.addFooter(org.docx4j.wml.HdrFtrRef.DEFAULT, java.util.List.of(folioParagraph(section + " footer, folio")));
	}

	/** n exact 24pt lines labelled with the section; returns the last paragraph. */
	private static P exactLines(Doc d, String section, int n) {
		P last = null;
		for (int k = 1; k <= n; k++) {
			last = d.para(section + " line " + k + " of " + n).noLabel().font(SERIF, 24).before(0).after(0)
					.line(480, STLineSpacingRule.EXACT).add();
		}
		return last;
	}

	/**
	 * continuous-margins-vertical-compat&lt;mode&gt; (CR-031 D1).  Which top and bottom margins, and which header and
	 * footer distances, does a page take where continuous sections with different vertical margins share a run?
	 * S1: top/bottom 72/72pt, header/footer 36/36, 45 lines (a page and a half); S2 continuous: 144/36, distances
	 * 18/18, 70 lines (about three pages); S3 continuous: back to 72/72 and 36/36, 20 lines.  Exact 24pt lines;
	 * each section's own header and footer name it and print the folio.  The breaks sit on each section's last
	 * line, so no empty break paragraph is in the way.  Read: each page's first and last baselines, and its header
	 * and footer text and y.  @since 17.3.1 (CR-031 phase 0)
	 */
	private static Probe continuousMarginsProbe(int mode) {
		return new Probe("continuous-margins-vertical-compat" + mode,
				"three continuous sections in one run: S1 72/72pt margins and 36/36pt header/footer distances, "
				+ "S2 144/36 and 18/18, S3 72/72 and 36/36; exact 24pt lines; each section's header and footer "
				+ "name it; mode " + mode + ".  Read each page's first and last baselines and its header and footer",
				() -> {
			Doc d = Doc.create(mode);
			verticalMargins(d, 1440, 1440, 720, 720);
			sectionHeaderFooter(d, "S1");
			d.endSectionOn(exactLines(d, "S1", 45), "continuous");
			verticalMargins(d, 2880, 720, 360, 360);
			sectionHeaderFooter(d, "S2");
			d.endSectionOn(exactLines(d, "S2", 70), "continuous");
			verticalMargins(d, 1440, 1440, 720, 720);
			sectionHeaderFooter(d, "S3");
			exactLines(d, "S3", 20);
			return d.pkg();
		});
	}

	/**
	 * continuous-margins-at-top (CR-031 D1).  S1 (72/72pt) fills its page exactly with 29 exact 24pt lines, so the
	 * continuous S2 (144/36pt, distances 18/18) starts at the top of page 2.  Read: page 2's first baseline and
	 * header, and page 3's.  @since 17.3.1 (CR-031 phase 0)
	 */
	private static Probe continuousMarginsAtTopProbe() {
		return new Probe("continuous-margins-at-top",
				"S1 (72/72pt margins) fills page 1 with 29 exact 24pt lines; the continuous S2 (144/36, header and "
				+ "footer 18/18) begins at the top of page 2 and runs 40 lines; mode 15.  Read page 2's and page 3's "
				+ "first baselines and headers", () -> {
			Doc d = Doc.create(15);
			verticalMargins(d, 1440, 1440, 720, 720);
			sectionHeaderFooter(d, "S1");
			d.endSectionOn(exactLines(d, "S1", 29), "continuous");
			verticalMargins(d, 2880, 720, 360, 360);
			sectionHeaderFooter(d, "S2");
			exactLines(d, "S2", 40);
			return d.pkg();
		});
	}

	/**
	 * continuous-restart (CR-031 D3).  Where does a continuous section's w:pgNumType w:start take effect?  S1, 40
	 * exact 24pt lines (to mid page 2); S2 continuous, restarting at 1, 60 lines.  Each footer names its section
	 * and prints the folio.  Read: each page's folio and footer.  @since 17.3.1 (CR-031 phase 0)
	 */
	private static Probe continuousRestartProbe() {
		return new Probe("continuous-restart",
				"S1 runs 40 exact 24pt lines, to mid page 2; S2, continuous, w:pgNumType w:start=\"1\", 60 lines; "
				+ "each footer names its section and prints the folio; mode 15.  Read each page's folio and footer",
				() -> {
			Doc d = Doc.create(15);
			sectionHeaderFooter(d, "S1");
			d.endSectionOn(exactLines(d, "S1", 40), "continuous");
			sectionHeaderFooter(d, "S2");
			d.pageNumberStart(1);
			exactLines(d, "S2", 60);
			return d.pkg();
		});
	}

	/**
	 * continuous-titlepg (CR-031 D3).  S1, 40 exact 24pt lines with a default header; S2 continuous, w:titlePg, a
	 * first-page header and a default one, 70 lines.  Read: which header each page carries.
	 * @since 17.3.1 (CR-031 phase 0)
	 */
	private static Probe continuousTitlePgProbe() {
		return new Probe("continuous-titlepg",
				"S1 runs 40 exact 24pt lines under a default header; S2, continuous, has w:titlePg with a first-page "
				+ "header and a default header, 70 lines; mode 15.  Read which header each page carries", () -> {
			Doc d = Doc.create(15);
			d.addHeader(org.docx4j.wml.HdrFtrRef.DEFAULT, java.util.List.of(folioParagraph("S1 default header, folio")));
			d.endSectionOn(exactLines(d, "S1", 40), "continuous");
			d.addHeader(org.docx4j.wml.HdrFtrRef.FIRST, java.util.List.of(folioParagraph("S2 FIRST-page header, folio")));
			d.addHeader(org.docx4j.wml.HdrFtrRef.DEFAULT, java.util.List.of(folioParagraph("S2 default header, folio")));
			exactLines(d, "S2", 70);
			return d.pkg();
		});
	}

	/**
	 * section-break-paragraph-foot (CR-031 D2; 9539).  Does an empty paragraph carrying a section break, at the foot
	 * of a page with no room for its line, start a page of its own?  Three cases, each a page of 29 exact 24pt
	 * lines (1.95pt left at 72/72pt margins) and then an empty section-break paragraph (no spacing) whose next
	 * section is: A continuous, the same page size; B continuous, A4 landscape (a page size change, which starts a
	 * page: 9539's shape); C nextPage.  Each next section holds one line, "X after".  Headers name the case.  Read:
	 * the page count, and any page holding only a header.  @since 17.3.1 (CR-031 phase 0)
	 */
	private static Probe sectionBreakParagraphFootProbe() {
		return new Probe("section-break-paragraph-foot",
				"pages filled to 1.95pt above the foot, then an empty paragraph carrying a section break whose next "
				+ "section is continuous (A), continuous with a page size change (B) and nextPage (C); mode 15.  "
				+ "Read the page count and any page holding only a header", () -> {
			Doc d = Doc.create(15);
			String[][] cases = { { "A", "continuous" }, { "B", "continuous" }, { "C", "nextPage" } };
			for (int i = 0; i < cases.length; i++) {
				String c = cases[i][0];
				d.addHeader(org.docx4j.wml.HdrFtrRef.DEFAULT, java.util.List.of(folioParagraph("case " + c + " header, folio")));
				exactLines(d, c, 29);
				d.endSection(cases[i][1], 0);
				if ("B".equals(c)) d.pageGeometry(16839, 11907, true, 1440, 1440, 1440, 1440);
				d.addHeader(org.docx4j.wml.HdrFtrRef.DEFAULT, java.util.List.of(folioParagraph("case " + c + " after header, folio")));
				P after = d.para(c + " after the section break").noLabel().font(SERIF, 24).before(0).after(0)
						.line(480, STLineSpacingRule.EXACT).add();
				if (i + 1 < cases.length) {
					d.endSectionOn(after, "nextPage");
					if ("B".equals(c)) {
						d.pageGeometry(11907, 16839, false, 1440, 1440, 1440, 1440);
						d.sectPr().getPgSz().setOrient(null);   // pageGeometry leaves a landscape orientation set
					}
				}
			}
			return d.pkg();
		});
	}

	/**
	 * continuous-restart-evenodd / -mirror (CR-031 D3, probe P6; the review's finding 4).  P3's shape with
	 * w:evenAndOddHeaders: S1 runs 40 exact 24pt lines, to mid page 2; S2, continuous, w:pgNumType w:start="1",
	 * runs 80 lines, to page 5, so the folios Word prints are 1, 2, 2, 3, 4 (P3's rule) and two even folios meet
	 * at pages 2 and 3.  Each section has odd (default) and even headers and footers naming themselves and
	 * printing the folio.  The -mirror twin adds w:mirrorMargins, inside (w:left) 108pt and outside (w:right)
	 * 36pt.  Read: which header and footer, odd or even, each page carries, and on the twin the x of its lines -
	 * whether they follow the printed folio or the physical page.  @since 17.3.1 (CR-031 phase 0)
	 */
	private static Probe continuousRestartEvenOddProbe(boolean mirror) {
		return continuousRestartEvenOddProbe("continuous-restart-evenodd" + (mirror ? "-mirror" : ""), 40, 1, mirror);
	}

	/**
	 * P6's shape with S1's length and S2's restart number as given (CR-031 probe P7, after P6 showed that with
	 * w:evenAndOddHeaders the restart counts from the first page S2 owns - folios 1, 2, 1, 2, 3 - where P3, without
	 * them, counted from the page S2 starts on).  Two readings fit P6: (H1) with odd and even headers the count
	 * starts at the first page the section owns; (H2) Word puts the origin where the folio's parity matches the
	 * physical page's.  {@code -start2}: S2 restarts at 2 - H1 puts folio 2 (even) on physical page 3 (odd), H2
	 * counts from page 2 so page 3 prints 3; under H1 page 3's header says whether odd/even follows the folio or the
	 * page, and the {@code -mirror} twin's x says the same for the margins.  {@code -oddstart}: S1 runs 15 lines,
	 * so S2 (start 1) begins mid-page 1, a physical odd page - H1: page 2, the first S2 owns, prints 1 (odd folio,
	 * even page); H2: counting from page 1, page 2 prints 2.  @since 17.3.1 (CR-031 phase 0)
	 */
	private static Probe continuousRestartEvenOddProbe(String id, int s1Lines, int start, boolean mirror) {
		return new Probe(id,
				"S1 runs " + s1Lines + " exact 24pt lines; S2, continuous, w:pgNumType w:start=\"" + start + "\", 80 lines; "
				+ "w:evenAndOddHeaders, each section's odd and even headers and footers naming themselves and "
				+ "printing the folio" + (mirror ? "; w:mirrorMargins, inside 108pt, outside 36pt" : "")
				+ "; mode 15.  Read each page's folio and which header and footer, odd or even, it carries"
				+ (mirror ? ", and the x of its lines" : ""), () -> {
			Doc d = Doc.create(15);
			if (mirror) {
				d.pkg().getMainDocumentPart().getDocumentSettingsPart().getContents()
						.setMirrorMargins(new org.docx4j.wml.BooleanDefaultTrue());
				org.docx4j.wml.SectPr.PgMar m = d.sectPr().getPgMar();
				m.setLeft(BigInteger.valueOf(2160));   // inside
				m.setRight(BigInteger.valueOf(720));   // outside
			}
			evenOddHeaderFooter(d, "S1");
			d.endSectionOn(exactLines(d, "S1", s1Lines), "continuous");
			evenOddHeaderFooter(d, "S2");
			d.pageNumberStart(start);
			exactLines(d, "S2", 80);
			return d.pkg();
		});
	}

	/** Odd (default) and even headers and footers naming the section and printing the folio. */
	private static void evenOddHeaderFooter(Doc d, String section) throws Exception {
		d.addHeader(org.docx4j.wml.HdrFtrRef.DEFAULT, java.util.List.of(folioParagraph(section + " ODD header, folio")));
		d.addHeader(org.docx4j.wml.HdrFtrRef.EVEN, java.util.List.of(folioParagraph(section + " EVEN header, folio")));
		d.addFooter(org.docx4j.wml.HdrFtrRef.DEFAULT, java.util.List.of(folioParagraph(section + " ODD footer, folio")));
		d.addFooter(org.docx4j.wml.HdrFtrRef.EVEN, java.util.List.of(folioParagraph(section + " EVEN footer, folio")));
	}

	/**
	 * document-end-break-&lt;a..e&gt; (pagebreak-paragraph-compat's open case F, rules §3.3).  Does a break-only
	 * paragraph ending the document give Word a further page?  page-blank's does (its ninth page), the
	 * pagebreak-paragraph probes' F does not, and they differed in nothing measured.  Each document is one page of
	 * text and then a break-only paragraph as its last paragraph, varying what page-blank has and F has not:
	 * a: one paragraph, no space after; b: the same with 12pt after (page-blank's last paragraph); c: a's paragraph
	 * in a second, nextPage section (page-blank's last section is one); d: c with 12pt after; e: a single section
	 * with a break-only paragraph earlier in it and text after (page-blank's last section has one).  Read: the page
	 * count, and whether the last page holds anything.  @since 17.3.1
	 */
	private static Probe documentEndBreakProbe(char variant) {
		return new Probe("document-end-break-" + variant,
				"a document whose last paragraph holds nothing but a page break, variant " + variant
				+ "; mode 15.  Read the page count and whether its last page is empty", () -> {
			Doc d = Doc.create(15);
			boolean twelveAfter = variant == 'b' || variant == 'd';
			if (variant == 'c' || variant == 'd') {
				d.para("Section one, a paragraph of its own. " + prose(1, 1)).noLabel().after(0).add();
				d.endSection("nextPage", 0);
			}
			if (variant == 'e') {
				d.para("Before the earlier break-only paragraph. " + prose(1, 2)).noLabel().after(240).add();
				d.add(breakOnlyParagraph());
				d.para("After the earlier break-only paragraph. " + prose(1, 3)).noLabel().after(240).add();
			}
			d.para("The last paragraph with text on it, variant " + variant + ". " + prose(2, 4)).noLabel()
					.after(twelveAfter ? 240 : 0).add();
			d.add(breakOnlyParagraph());
			return d.pkg();
		});
	}

	// ---------------------------------------------------------------- CR-001 batch 53 probes (ledger9 §5)
	//
	// One Word run.  A4, 1 inch margins: the column is 9026 twips (451.3pt) wide and the body 13958
	// twips (697.9pt) tall.  Filler lines are exact 14pt (280 twips), so the room a probe leaves at a
	// page foot is known to the point.

	private static final String W_NS = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String M_NS = "xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\"";
	private static final int COLUMN_TWIPS = 11906 - 2 * 1440;
	private static final int BODY_TWIPS = 16838 - 2 * 1440;

	/** A paragraph (or table) from its WordprocessingML; the namespace declarations are added. */
	private static P xmlP(String xml) throws Exception {
		String ns = W_NS + (xml.contains("<m:") ? " " + M_NS : "");
		xml = xml.replaceFirst("<w:p(?=[ >/])", "<w:p " + ns);
		Object o = org.docx4j.XmlUtils.unmarshalString(xml, org.docx4j.jaxb.Context.jc, P.class);
		if (o instanceof jakarta.xml.bind.JAXBElement) o = ((jakarta.xml.bind.JAXBElement<?>) o).getValue();
		return (P) o;
	}

	private static Tbl xmlTbl(String xml) throws Exception {
		xml = xml.replaceFirst("<w:tbl(?=[ >])", "<w:tbl " + W_NS);
		return (Tbl) org.docx4j.XmlUtils.unwrap(org.docx4j.XmlUtils.unmarshalString(xml));
	}

	private static String xmlEscape(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private static String rpr(String font, int halfPts, boolean bold) {
		return "<w:rPr><w:rFonts w:ascii=\"" + font + "\" w:hAnsi=\"" + font + "\" w:eastAsia=\"" + font
				+ "\" w:cs=\"" + font + "\"/>" + (bold ? "<w:b/><w:bCs/>" : "")
				+ "<w:sz w:val=\"" + halfPts + "\"/><w:szCs w:val=\"" + halfPts + "\"/></w:rPr>";
	}

	private static String xrun(String text, String font, int halfPts, boolean bold) {
		return "<w:r>" + rpr(font, halfPts, bold) + "<w:t xml:space=\"preserve\">" + xmlEscape(text) + "</w:t></w:r>";
	}

	/** A paragraph of one exact-height line (14pt unless given), the mark in Liberation Serif 12. */
	private static String exactP(String text, int lineTwips, String extraPPr) {
		return "<w:p><w:pPr>" + extraPPr + "<w:spacing w:before=\"0\" w:after=\"0\" w:line=\"" + lineTwips
				+ "\" w:lineRule=\"exact\"/>" + rpr(SERIF, 24, false) + "</w:pPr>"
				+ (text.isEmpty() ? "" : xrun(text, SERIF, 24, false)) + "</w:p>";
	}

	/** {@code n} exact 14pt filler lines, numbered so the reader can find a page's last one. */
	private static void fillerLines(Doc d, String tag, int n) throws Exception {
		for (int i = 1; i <= n; i++) d.add(xmlP(exactP(tag + " filler line " + i + " of " + n, 280, "")));
	}

	/** Words with no break opportunity but the spaces between them. */
	private static final String[] GRID_WORDS = ("Typesetting engines decide where each line ends and where each page ends "
			+ "while small differences in measurement accumulate quietly across every paragraph of a long document "
			+ "until whole pages move").split(" ");

	/** Whole plain words up to (not over) {@code targetPt} at this font and size. */
	private static String plainWordsUpTo(String font, int halfPts, double targetPt) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; ; i++) {
			String next = (sb.length() == 0 ? "" : sb + " ") + GRID_WORDS[i % GRID_WORDS.length];
			if (Doc.advancePoints(next, font, halfPts) > targetPt) return sb.toString();
			sb.setLength(0);
			sb.append(next);
		}
	}

	private static final int[] GRID_HALF_POINTS = { 14, 15, 16, 17, 18, 20, 21, 22, 23, 24, 26, 28, 30, 32 };
	private static final double[] GRID_LADDER = { -1.2, -0.9, -0.6, -0.4, -0.2, 0.0, 0.2, 0.4, 0.6, 0.9, 1.2 };

	/**
	 * font-size-grid-&lt;serif|sans|carlito&gt; (ledger9 §2, F1).  Does Word lay text out at the font size
	 * rounded to 1/300 inch (7.92, 9.12, 10.08, 11.04, 12, 12.96, 13.92...), and by how much does it scale the
	 * advances?  For each size a heading, then a ladder of left-aligned one-line paragraphs of the same text,
	 * each with a right indent making the measure the text's advance at the nominal size (the font's own
	 * metrics) times 1+k, k = -1.2% ... +1.2%.  The last word wraps where Word's width exceeds the measure,
	 * so the first rung that holds on one line is Word's width ratio at that size.  @since 17.3.1
	 */
	private static Probe fontSizeGridProbe(String tag, String font) {
		return new Probe("font-size-grid-" + tag,
				"F1: for sizes 7-16pt in " + font + ", a heading then 11 one-line left-aligned"
				+ " paragraphs of the same text with measures of its nominal advance x (1+k), k = -1.2, -0.9, -0.6,"
				+ " -0.4, -0.2, 0, +0.2, +0.4, +0.6, +0.9, +1.2%; mode 15.  Read per size: Word's font size in the PDF,"
				+ " and which rungs wrap their last word (the first that holds is Word's width ratio)", () -> {
			Doc d = Doc.create(15);
			for (int hp : GRID_HALF_POINTS) {
				String text = plainWordsUpTo(font, hp, COLUMN_TWIPS / 20.0 * 0.66);
				double w = Doc.advancePoints(text, font, hp);
				d.add(xmlP("<w:p><w:pPr><w:keepNext/><w:spacing w:before=\"160\" w:after=\"40\"/></w:pPr>"
						+ xrun("Size " + (hp / 2.0) + "pt: the text is " + String.format(java.util.Locale.ROOT, "%.3f", w)
								+ "pt at the nominal size", SANS, 16, true) + "</w:p>"));
				for (double k : GRID_LADDER) {
					int rightTwips = (int) Math.round(COLUMN_TWIPS - w * (1 + k / 100.0) * 20.0);
					d.add(xmlP("<w:p><w:pPr><w:keepLines/><w:spacing w:before=\"0\" w:after=\"0\"/><w:ind w:left=\"0\" w:right=\""
							+ rightTwips + "\"/><w:jc w:val=\"left\"/></w:pPr>" + xrun(text, font, hp, false) + "</w:p>"));
				}
			}
			return d.pkg();
		});
	}

	/**
	 * header-empty-paragraph-compat&lt;12|14|15&gt; (ledger9; 6131, 7061).  Does a header part holding one empty
	 * paragraph reserve its line?  Rules §7 measured "no" (mode unrecorded); 6131 and 7061 (mode 12) say yes.
	 * Top margin 45pt, header distance 36pt: reserving the 13.8pt line puts the body at 49.8.  @since 17.3.1
	 */
	private static Probe headerEmptyParagraphCompatProbe(int mode) {
		return new Probe("header-empty-paragraph-compat" + mode,
				"one empty header paragraph (Liberation Serif 12), w:top 45pt, w:header 36pt; mode " + mode
				+ ".  Read the first body baseline: about 45 + ascent if the header reserves nothing, 49.8 + ascent if"
				+ " its line counts", () -> {
			Doc d = Doc.create(mode);
			d.sectPr().getPgMar().setTop(BigInteger.valueOf(900));
			d.sectPr().getPgMar().setHeader(BigInteger.valueOf(720));
			d.addHeader(org.docx4j.wml.HdrFtrRef.DEFAULT, java.util.List.of(xmlP(
					"<w:p><w:pPr><w:spacing w:before=\"0\" w:after=\"0\"/>" + rpr(SERIF, 24, false) + "</w:pPr></w:p>")));
			for (int i = 1; i <= 3; i++) d.para("Body paragraph " + i + ". " + prose(3, i)).noLabel().after(120).add();
			return d.pkg();
		});
	}

	/** A one-column table: a first row of {@code firstRowLines} exact 14pt lines (as header, can't-split or
	 *  plain), then {@code bodyRows} rows of one line; cell margins top and bottom 0. */
	private static String linesTable(String tag, int firstRowLines, String firstRowTrPr, int bodyRows, String styleId) {
		StringBuilder x = new StringBuilder("<w:tbl><w:tblPr>");
		if (styleId != null) x.append("<w:tblStyle w:val=\"").append(styleId).append("\"/>");
		x.append("<w:tblW w:w=\"").append(COLUMN_TWIPS).append("\" w:type=\"dxa\"/><w:tblBorders>");
		for (String side : new String[] { "top", "left", "bottom", "right", "insideH", "insideV" }) {
			x.append("<w:").append(side).append(" w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>");
		}
		x.append("</w:tblBorders><w:tblLayout w:type=\"fixed\"/><w:tblCellMar><w:top w:w=\"0\" w:type=\"dxa\"/>"
				+ "<w:left w:w=\"108\" w:type=\"dxa\"/><w:bottom w:w=\"0\" w:type=\"dxa\"/><w:right w:w=\"108\" w:type=\"dxa\"/>"
				+ "</w:tblCellMar><w:tblLook w:val=\"04A0\" w:firstRow=\"1\" w:lastRow=\"0\" w:firstColumn=\"0\" w:lastColumn=\"0\""
				+ " w:noHBand=\"1\" w:noVBand=\"1\"/></w:tblPr><w:tblGrid><w:gridCol w:w=\"").append(COLUMN_TWIPS).append("\"/></w:tblGrid>");
		x.append("<w:tr>").append(firstRowTrPr == null ? "" : "<w:trPr>" + firstRowTrPr + "</w:trPr>")
				.append("<w:tc><w:tcPr><w:tcW w:w=\"").append(COLUMN_TWIPS).append("\" w:type=\"dxa\"/></w:tcPr>");
		for (int i = 1; i <= firstRowLines; i++) x.append(exactP(tag + " first row line " + i + " of " + firstRowLines, 280, ""));
		x.append("</w:tc></w:tr>");
		for (int r = 1; r <= bodyRows; r++) {
			x.append("<w:tr><w:tc><w:tcPr><w:tcW w:w=\"").append(COLUMN_TWIPS).append("\" w:type=\"dxa\"/></w:tcPr>")
					.append(exactP(tag + " body row " + r, 280, "")).append("</w:tc></w:tr>");
		}
		return x.append("</w:tbl>").toString();
	}

	/** Adds (or replaces) a style given as XML. */
	private static void styleXml(Doc d, String xml) throws Exception {
		xml = xml.replaceFirst("<w:style(?=[ >])", "<w:style " + W_NS);
		org.docx4j.wml.Style style = (org.docx4j.wml.Style) org.docx4j.XmlUtils.unwrap(org.docx4j.XmlUtils.unmarshalString(xml));
		java.util.List<org.docx4j.wml.Style> styles = d.mdp().getStyleDefinitionsPart().getJaxbElement().getStyle();
		styles.removeIf(s -> style.getStyleId().equals(s.getStyleId()));
		styles.add(style);
	}

	/**
	 * table-header-overflow-&lt;tblheader|style&gt; (ledger9; 12301).  A keepNext heading, then a table whose
	 * repeating first row (by w:tblHeader, or by the table style's firstRow) is 168pt tall where 95.9pt are left
	 * on the page.  12301: Word takes the table to the next page and leaves the heading at the foot; FOP runs the
	 * header row off the page.  @since 17.3.1
	 */
	private static Probe tableHeaderOverflowProbe(boolean byStyle) {
		String id = "table-header-overflow-" + (byStyle ? "style" : "tblheader");
		return new Probe(id, "43 exact 14pt lines (95.9pt left), a keepNext heading, then a table whose repeating"
				+ " first row is 12 lines (168pt), by " + (byStyle ? "the table style's firstRow w:tblHeader" : "w:tblHeader")
				+ ", and 8 body rows; mode 15.  Read the page the heading and the table start on, and whether the header"
				+ " row repeats on the next page", () -> {
			Doc d = Doc.create(15);
			if (byStyle) {
				styleXml(d, "<w:style w:type=\"table\" w:styleId=\"ProbeHeaderTable\"><w:name w:val=\"Probe Header Table\"/>"
						+ "<w:tblPr><w:tblCellMar><w:left w:w=\"108\" w:type=\"dxa\"/><w:right w:w=\"108\" w:type=\"dxa\"/></w:tblCellMar></w:tblPr>"
						+ "<w:tblStylePr w:type=\"firstRow\"><w:rPr><w:b/></w:rPr><w:trPr><w:tblHeader/></w:trPr></w:tblStylePr></w:style>");
			}
			fillerLines(d, "THO", 43);
			d.add(xmlP(exactP("THO heading kept with the table", 280, "<w:keepNext/>")));
			d.add(xmlTbl(linesTable("THO", 12, byStyle ? null : "<w:tblHeader/>", 8, byStyle ? "ProbeHeaderTable" : null)));
			d.add(xmlP(exactP("THO after the table", 280, "")));
			return d.pkg();
		});
	}

	/**
	 * table-nested-rowsplit-&lt;a|b|c|d&gt; (ledger9; 12301).  An outer row holding a nested table taller than the
	 * room left (151.9pt): (a) the nested table 280pt, shorter than a page; (b) 840pt, longer; (c) a with the
	 * outer row w:cantSplit; (d) a with keepNext on the row's first paragraph.  12301: Word moves such a row
	 * whole.  @since 17.3.1
	 */
	private static Probe tableNestedRowsplitProbe(char variant) {
		return new Probe("table-nested-rowsplit-" + variant, "39 exact 14pt lines (151.9pt left), then a one-column"
				+ " table: a one-line row, then a row holding a paragraph and a nested table of "
				+ (variant == 'b' ? 60 : 20) + " exact 14pt rows" + (variant == 'c' ? ", the row w:cantSplit" : "")
				+ (variant == 'd' ? ", the row's first paragraph keepNext" : "") + "; mode 15.  Read whether the row"
				+ " starts on page 1 (split) or page 2 (moved whole), and where it breaks", () -> {
			Doc d = Doc.create(15);
			fillerLines(d, "NRS", 39);
			int inner = COLUMN_TWIPS - 400;
			StringBuilder nested = new StringBuilder("<w:tbl><w:tblPr><w:tblW w:w=\"").append(inner).append("\" w:type=\"dxa\"/>"
					+ "<w:tblBorders><w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/><w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
					+ "<w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/></w:tblBorders><w:tblLayout w:type=\"fixed\"/>"
					+ "<w:tblCellMar><w:top w:w=\"0\" w:type=\"dxa\"/><w:bottom w:w=\"0\" w:type=\"dxa\"/></w:tblCellMar></w:tblPr>"
					+ "<w:tblGrid><w:gridCol w:w=\"").append(inner).append("\"/></w:tblGrid>");
			int rows = variant == 'b' ? 60 : 20;
			for (int r = 1; r <= rows; r++) {
				nested.append("<w:tr><w:tc><w:tcPr><w:tcW w:w=\"").append(inner).append("\" w:type=\"dxa\"/></w:tcPr>")
						.append(exactP("NRS nested row " + r + " of " + rows, 280, "")).append("</w:tc></w:tr>");
			}
			nested.append("</w:tbl>");
			String cell = "<w:tc><w:tcPr><w:tcW w:w=\"" + COLUMN_TWIPS + "\" w:type=\"dxa\"/></w:tcPr>";
			String tbl = "<w:tbl><w:tblPr><w:tblW w:w=\"" + COLUMN_TWIPS + "\" w:type=\"dxa\"/><w:tblBorders>"
					+ "<w:top w:val=\"single\" w:sz=\"8\" w:space=\"0\" w:color=\"auto\"/><w:left w:val=\"single\" w:sz=\"8\" w:space=\"0\" w:color=\"auto\"/>"
					+ "<w:bottom w:val=\"single\" w:sz=\"8\" w:space=\"0\" w:color=\"auto\"/><w:right w:val=\"single\" w:sz=\"8\" w:space=\"0\" w:color=\"auto\"/>"
					+ "<w:insideH w:val=\"single\" w:sz=\"8\" w:space=\"0\" w:color=\"auto\"/></w:tblBorders><w:tblLayout w:type=\"fixed\"/>"
					+ "<w:tblCellMar><w:top w:w=\"0\" w:type=\"dxa\"/><w:bottom w:w=\"0\" w:type=\"dxa\"/></w:tblCellMar></w:tblPr>"
					+ "<w:tblGrid><w:gridCol w:w=\"" + COLUMN_TWIPS + "\"/></w:tblGrid>"
					+ "<w:tr>" + cell + exactP("NRS outer row 1", 280, "") + "</w:tc></w:tr>"
					+ "<w:tr>" + (variant == 'c' ? "<w:trPr><w:cantSplit/></w:trPr>" : "") + cell
					+ exactP("NRS outer row 2, before its nested table", 280, variant == 'd' ? "<w:keepNext/>" : "")
					+ nested + exactP("", 280, "") + "</w:tc></w:tr></w:tbl>";
			d.add(xmlTbl(tbl));
			d.add(xmlP(exactP("NRS after the table", 280, "")));
			return d.pkg();
		});
	}

	/**
	 * omml-inline-line-box (ledger9; 7320).  docx4j's inline math (JEuclid) makes a 1.5-spaced 14pt line about
	 * 5pt taller than Word's.  Paragraphs at 14pt, line 360 auto: plain, then each with an inline n-ary with
	 * limits, a subscript, a fraction, wrapping to several lines.  @since 17.3.1
	 */
	private static Probe ommlInlineLineBoxProbe() {
		return new Probe("omml-inline-line-box", "14pt Liberation Serif, line 360 auto: a plain paragraph, then"
				+ " paragraphs holding an inline n-ary sum with limits, an msub, a fraction, each wrapping; mode 15."
				+ "  Read every line's baseline (the pitch where math sits against plain lines)", () -> {
			Doc d = Doc.create(15);
			String pPr = "<w:pPr><w:spacing w:before=\"0\" w:after=\"240\" w:line=\"360\" w:lineRule=\"auto\"/></w:pPr>";
			String text = xrun("Plain text around the formula, set at fourteen points on one and a half lines, " + prose(2, 3) + " ", SERIF, 28, false);
			String tail = xrun(" and the paragraph goes on after it. " + prose(2, 5), SERIF, 28, false);
			String mr = "<m:r><w:rPr><w:rFonts w:ascii=\"Cambria Math\" w:hAnsi=\"Cambria Math\"/><w:sz w:val=\"28\"/></w:rPr><m:t>%s</m:t></m:r>";
			String nary = "<m:oMath><m:nary><m:naryPr><m:chr m:val=\"∑\"/><m:limLoc m:val=\"undOvr\"/></m:naryPr><m:sub>"
					+ String.format(mr, "i=1") + "</m:sub><m:sup>" + String.format(mr, "n") + "</m:sup><m:e><m:sSub><m:e>"
					+ String.format(mr, "x") + "</m:e><m:sub>" + String.format(mr, "i") + "</m:sub></m:sSub></m:e></m:nary></m:oMath>";
			String msub = "<m:oMath><m:sSub><m:e>" + String.format(mr, "x") + "</m:e><m:sub>" + String.format(mr, "i")
					+ "</m:sub></m:sSub></m:oMath>";
			String frac = "<m:oMath><m:f><m:num>" + String.format(mr, "a+b") + "</m:num><m:den>" + String.format(mr, "c")
					+ "</m:den></m:f></m:oMath>";
			d.add(xmlP("<w:p>" + pPr + text + tail + "</w:p>"));
			for (String math : new String[] { nary, msub, frac }) d.add(xmlP("<w:p>" + pPr + text + math + tail + "</w:p>"));
			return d.pkg();
		});
	}

	/**
	 * trailing-empties-overflow-&lt;2|6|20|heading|break&gt; (ledger9; 719).  Does a document's run of trailing
	 * empty paragraphs that overflows the last page give Word a further page?  719 (Word 29 pages, ours 28) says
	 * yes.  Five text lines and 44 empty paragraphs, all exact 14pt (686pt of the 697.9pt body), then a last
	 * empty paragraph sized to overflow by 2, 6 or 20pt; or an empty Heading1-like paragraph (keepNext, 12pt
	 * before, 3pt after); or a break-only paragraph.  @since 17.3.1
	 */
	private static Probe trailingEmptiesOverflowProbe(String variant) {
		return new Probe("trailing-empties-overflow-" + variant, "5 text lines and 44 empty paragraphs, exact 14pt"
				+ " (686pt of 697.9), then " + ("heading".equals(variant) ? "an empty keepNext paragraph with 12pt before"
				: "break".equals(variant) ? "a break-only paragraph" : "an empty paragraph overflowing the body by " + variant
				+ "pt") + "; mode 15.  Read the page count, and what the last page holds", () -> {
			Doc d = Doc.create(15);
			for (int i = 1; i <= 5; i++) d.add(xmlP(exactP("TEO text line " + i, 280, "")));
			for (int i = 1; i <= 44; i++) d.add(xmlP(exactP("", 280, "")));
			if ("heading".equals(variant)) {
				d.add(xmlP("<w:p><w:pPr><w:keepNext/><w:spacing w:before=\"240\" w:after=\"60\" w:line=\"280\" w:lineRule=\"exact\"/>"
						+ rpr(SERIF, 32, true) + "</w:pPr></w:p>"));
			} else if ("break".equals(variant)) {
				d.add(breakOnlyParagraph());
			} else {
				int over = Integer.parseInt(variant);
				int last = BODY_TWIPS - 49 * 280 + over * 20;
				d.add(xmlP(exactP("", last, "")));
			}
			return d.pkg();
		});
	}

	/**
	 * keep-next-table-first-row-&lt;2|4|6|4-cantsplit|4-header&gt; (ledger9; 2451).  A keepNext caption before a
	 * table whose first row is 2, 4 or 6 exact 14pt lines, with room for the caption and about half the row.
	 * 2451: Word moves caption and row to the next page; FOP splits the row.  @since 17.3.1
	 */
	private static Probe keepNextTableFirstRowProbe(String variant) {
		int lines = Integer.parseInt(variant.substring(0, 1));
		int fillers = (BODY_TWIPS - 280 - lines * 140) / 280;
		return new Probe("keep-next-table-first-row-" + variant, fillers + " exact 14pt lines (room for the caption"
				+ " and about half the row), a keepNext caption, then a table whose first row is " + lines + " lines"
				+ (variant.endsWith("cantsplit") ? ", w:cantSplit" : "") + (variant.endsWith("header") ? ", w:tblHeader" : "")
				+ ", and 3 one-line rows; mode 15.  Read the page the caption and the first row start on, and whether the"
				+ " row splits", () -> {
			Doc d = Doc.create(15);
			fillerLines(d, "KNT", fillers);
			d.add(xmlP(exactP("KNT caption kept with the table", 280, "<w:keepNext/>")));
			String trPr = variant.endsWith("cantsplit") ? "<w:cantSplit/>" : variant.endsWith("header") ? "<w:tblHeader/>" : null;
			d.add(xmlTbl(linesTable("KNT", lines, trPr, 3, null)));
			d.add(xmlP(exactP("KNT after the table", 280, "")));
			return d.pkg();
		});
	}

	/**
	 * table-normal-style-rpr&lt;12|15&gt; (ledger9; 1912).  docDefaults 11pt, Normal 12pt: a table's paragraphs
	 * with no pStyle are set at 11pt by Word in 1912 (mode 12), at Normal's 12pt by docx4j.  A TableGrid table and
	 * a table in a custom style without run properties, in modes 12 and 15.  @since 17.3.1
	 */
	private static Probe tableNormalStyleRprProbe(int mode) {
		return new Probe("table-normal-style-rpr" + mode, "docDefaults Liberation Serif 11pt, Normal 12pt; a body"
				+ " paragraph, a TableGrid table and a table in a custom style (no rPr), their paragraphs without pStyle;"
				+ " mode " + mode + ".  Read the size of each table's text in Word's PDF", () -> {
			Doc d = Doc.create(mode);
			d.documentDefaultRun(SERIF, 22);
			for (org.docx4j.wml.Style st : d.mdp().getStyleDefinitionsPart().getJaxbElement().getStyle()) {
				if ("Normal".equals(st.getStyleId())) {
					if (st.getRPr() == null) st.setRPr(new org.docx4j.wml.RPr());
					org.docx4j.wml.HpsMeasure sz = new org.docx4j.wml.HpsMeasure();
					sz.setVal(BigInteger.valueOf(24));
					st.getRPr().setSz(sz);
				}
			}
			styleXml(d, "<w:style w:type=\"table\" w:styleId=\"TableGrid\"><w:name w:val=\"Table Grid\"/><w:basedOn w:val=\"TableNormal\"/>"
					+ "<w:pPr><w:spacing w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr><w:tblPr><w:tblBorders>"
					+ "<w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/><w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
					+ "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/><w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
					+ "<w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/><w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
					+ "</w:tblBorders></w:tblPr></w:style>");
			styleXml(d, "<w:style w:type=\"table\" w:customStyle=\"1\" w:styleId=\"ProbePlainTable\"><w:name w:val=\"Probe Plain Table\"/>"
					+ "<w:basedOn w:val=\"TableNormal\"/><w:tblPr><w:tblBorders><w:top w:val=\"single\" w:sz=\"12\" w:space=\"0\" w:color=\"auto\"/>"
					+ "<w:bottom w:val=\"single\" w:sz=\"12\" w:space=\"0\" w:color=\"auto\"/></w:tblBorders></w:tblPr></w:style>");
			d.add(xmlP("<w:p><w:r><w:t>Body text in Normal, which says 12pt over docDefaults' 11pt.</w:t></w:r></w:p>"));
			for (String style : new String[] { "TableGrid", "ProbePlainTable" }) {
				StringBuilder x = new StringBuilder("<w:tbl><w:tblPr><w:tblStyle w:val=\"" + style + "\"/><w:tblW w:w=\"0\" w:type=\"auto\"/>"
						+ "<w:tblLook w:val=\"04A0\" w:firstRow=\"1\" w:lastRow=\"0\" w:firstColumn=\"1\" w:lastColumn=\"0\" w:noHBand=\"0\" w:noVBand=\"1\"/>"
						+ "</w:tblPr><w:tblGrid><w:gridCol w:w=\"4513\"/><w:gridCol w:w=\"4513\"/></w:tblGrid>");
				for (int r = 1; r <= 3; r++) {
					x.append("<w:tr>");
					for (int c = 1; c <= 2; c++) {
						x.append("<w:tc><w:tcPr><w:tcW w:w=\"4513\" w:type=\"dxa\"/></w:tcPr><w:p><w:r><w:t>" + style + " row " + r
								+ " cell " + c + ": the quick brown fox jumps over the lazy dog</w:t></w:r></w:p></w:tc>");
					}
					x.append("</w:tr>");
				}
				d.add(xmlTbl(x.append("</w:tbl>").toString()));
				d.add(xmlP("<w:p/>"));
			}
			return d.pkg();
		});
	}

	/**
	 * table-style-size-trigger-&lt;a|b&gt; (ledger9 §9; 1912).  table-normal-style-rpr12 found, in mode 12, a
	 * TableGrid table's paragraphs set at docDefaults' 11pt and a custom style's (no pPr, no rPr) at Normal's 12pt.
	 * TableGrid differs from that custom style twice: it is built in, and it has a w:pPr.  These separate the two,
	 * and ask whether a w:rPr without a size, or a conditional format alone, does the same.  docDefaults Liberation
	 * Serif 11pt, Normal 12pt, mode 12, paragraphs without pStyle.  a: TableGrid with its usual w:pPr; b: TableGrid
	 * with none.  Both: a custom style with only a w:pPr (spacing); one with only a w:rPr stating a colour; one with
	 * only a firstRow conditional format (bold); one stating 9pt (the measured exception, a control); one with
	 * neither (12pt, the control).  @since 17.3.1
	 */
	private static Probe tableStyleSizeTriggerProbe(char variant) {
		boolean gridPPr = variant == 'a';
		return new Probe("table-style-size-trigger-" + variant, "docDefaults Liberation Serif 11pt, Normal 12pt, mode 12;"
				+ " tables without pStyle in: TableGrid " + (gridPPr ? "with" : "without") + " its w:pPr, custom styles with"
				+ " only a pPr, only an rPr colour, only a firstRow format, 9pt, nothing.  Read the size of each table's"
				+ " text (the label leads each cell)", () -> {
			Doc d = Doc.create(12);
			d.documentDefaultRun(SERIF, 22);
			for (org.docx4j.wml.Style st : d.mdp().getStyleDefinitionsPart().getJaxbElement().getStyle()) {
				if ("Normal".equals(st.getStyleId())) {
					if (st.getRPr() == null) st.setRPr(new org.docx4j.wml.RPr());
					org.docx4j.wml.HpsMeasure sz = new org.docx4j.wml.HpsMeasure();
					sz.setVal(BigInteger.valueOf(24));
					st.getRPr().setSz(sz);
				}
			}
			String borders = "<w:tblPr><w:tblBorders><w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
					+ "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
					+ "<w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/></w:tblBorders></w:tblPr>";
			styleXml(d, "<w:style w:type=\"table\" w:styleId=\"TableGrid\"><w:name w:val=\"Table Grid\"/><w:basedOn w:val=\"TableNormal\"/>"
					+ (gridPPr ? "<w:pPr><w:spacing w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr>" : "") + borders + "</w:style>");
			String[][] custom = {
					{ "ProbePPrOnly", "<w:pPr><w:spacing w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr>" },
					{ "ProbeRPrColour", "<w:rPr><w:color w:val=\"1F3864\"/></w:rPr>" },
					{ "ProbeFirstRowOnly", null },
					{ "ProbeNinePoint", "<w:rPr><w:sz w:val=\"18\"/><w:szCs w:val=\"18\"/></w:rPr>" },
					{ "ProbeNothing", "" } };
			for (String[] c : custom) {
				String inner = c[1] != null ? c[1] + borders
						: borders + "<w:tblStylePr w:type=\"firstRow\"><w:rPr><w:b/><w:bCs/></w:rPr></w:tblStylePr>";
				styleXml(d, "<w:style w:type=\"table\" w:customStyle=\"1\" w:styleId=\"" + c[0] + "\"><w:name w:val=\"" + c[0] + "\"/>"
						+ "<w:basedOn w:val=\"TableNormal\"/>" + inner + "</w:style>");
			}
			d.add(xmlP("<w:p><w:r><w:t>Body text in Normal, which says 12pt over docDefaults' 11pt.</w:t></w:r></w:p>"));
			java.util.List<String> styles = new java.util.ArrayList<String>();
			styles.add("TableGrid");
			for (String[] c : custom) styles.add(c[0]);
			for (String style : styles) {
				StringBuilder x = new StringBuilder("<w:tbl><w:tblPr><w:tblStyle w:val=\"" + style + "\"/><w:tblW w:w=\"0\" w:type=\"auto\"/>"
						+ "<w:tblLook w:val=\"04A0\" w:firstRow=\"1\" w:lastRow=\"0\" w:firstColumn=\"0\" w:lastColumn=\"0\" w:noHBand=\"1\" w:noVBand=\"1\"/>"
						+ "</w:tblPr><w:tblGrid><w:gridCol w:w=\"9026\"/></w:tblGrid>");
				for (int r = 1; r <= 2; r++) {
					x.append("<w:tr><w:tc><w:tcPr><w:tcW w:w=\"9026\" w:type=\"dxa\"/></w:tcPr><w:p><w:r><w:t>" + style + " row " + r
							+ ": the quick brown fox jumps over the lazy dog</w:t></w:r></w:p></w:tc></w:tr>");
				}
				d.add(xmlTbl(x.append("</w:tbl>").toString()));
				d.add(xmlP("<w:p/>"));
			}
			return d.pkg();
		});
	}

	/** A square-wrapped VML text box, 20pt tall, at a margin-relative x and width, its text "TAG box". */
	private static String vmlSquareBoxRun(String tag, double x, double w, int z) {
		String style = "position:absolute;margin-left:" + x + "pt;margin-top:0;width:" + w + "pt;height:20pt;z-index:" + z
				+ ";mso-wrap-style:square;mso-position-horizontal:absolute;mso-position-horizontal-relative:margin"
				+ ";mso-position-vertical:absolute;mso-position-vertical-relative:text";
		return "<w:r><w:pict xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\""
				+ " xmlns:w10=\"urn:schemas-microsoft-com:office:word\">"
				+ "<v:rect id=\"Box" + z + "\" style=\"" + style + "\" filled=\"f\" stroked=\"t\" strokeweight=\".5pt\">"
				+ "<v:textbox inset=\"0,0,0,0\"><w:txbxContent><w:p><w:pPr><w:spacing w:before=\"0\" w:after=\"0\"/></w:pPr>"
				+ xrun(tag + " box", SERIF, 20, false) + "</w:p></w:txbxContent></v:textbox>"
				+ "<w10:wrap type=\"square\"/></v:rect></w:pict></w:r>";
	}

	/**
	 * actualtext-clusters (fop/CR-019, ActualText per cluster; the fork's §7 measurement and §9 reachability).
	 * Word's PDF gives the text a reader should get for each cluster; docx4j's, for the same lines, shows where
	 * FOP's mark-first order (fop/CR-016 §7) or a split cluster gives it something else.  One labelled paragraph
	 * per case, the label in Calibri and the case's text in its own font:
	 * L1-L3 Latin precomposed and decomposed in Cambria (whose ccmp decomposes every accented letter, docx4j
	 * 3e05db3af), L4-L5 the decomposed line in Calibri and Times New Roman, L6 stacked marks (Vietnamese),
	 * L7-L8 Greek precomposed and decomposed, L9 Cyrillic decomposed (corpus document 3489's й as и + U+0306),
	 * L10-L11 Devanagari in Nirmala UI (a pre-base vowel sign, a conjunct, virama, and "है," whose vowel sign
	 * docx4j's text layer put after the comma on corpus document 394), L12 Bengali (pre-base and two-part vowel
	 * signs), L13 Arabic with harakat and lam-alef, right to left, L14 Hebrew with niqqud, right to left.
	 * Read: each line's extracted text (pdftotext, mutool, PDFBox, a browser's PDF viewer) against the source.
	 * @since 17.3.1
	 */
	private static Probe actualTextClustersProbe() {
		return new Probe("actualtext-clusters", "one labelled line per cluster case: Latin precomposed and decomposed "
				+ "(Cambria, Calibri, Times New Roman), stacked Vietnamese marks, Greek and Cyrillic decomposed, "
				+ "Devanagari and Bengali in Nirmala UI, Arabic and Hebrew right to left.  Read the text each PDF "
				+ "reader extracts per line against the source text", () -> {
			Doc d = Doc.create(15);
			String ns = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
			String[][] cases = {
				{ "L1 Latin precomposed, Cambria", "Cambria", "", "café résumé naïve Ålborg ça ñandú őr" },
				{ "L2 Latin decomposed, Cambria", "Cambria", "", "café résumé naïve Ålborg ça ñandú őr" },
				{ "L3 Latin mixed, Cambria", "Cambria", "", "Médiafigyelés Mediafigyelés Médiafigyelés" },
				{ "L4 Latin decomposed, Calibri", "Calibri", "", "café résumé naïve Ålborg ça ñandú" },
				{ "L5 Latin decomposed, Times New Roman", "Times New Roman", "", "café résumé naïve Ålborg ça ñandú" },
				{ "L6 Vietnamese stacked marks, Times New Roman", "Times New Roman", "", "Việt Nam tiếng Việt người" },
				{ "L7 Greek precomposed, Cambria", "Cambria", "", "άέήίόύώ σήμερα ουσία" },
				{ "L8 Greek decomposed, Cambria", "Cambria", "", "άέή σήμερα ουσία" },
				{ "L9 Cyrillic decomposed, Times New Roman", "Times New Roman", "", "дальнейшую онлайн ёлка й ё" },
				{ "L10 Devanagari, Nirmala UI", "Nirmala UI", "cs", "नमस्ते हिन्दी क्षत्रिय किताब" },
				{ "L11 Devanagari before a comma, Nirmala UI", "Nirmala UI", "cs", "यह क्या है, कि वह है।" },
				{ "L12 Bengali, Nirmala UI", "Nirmala UI", "cs", "বাংলা রাখবেন কোথায় নেই বিদ্যা" },
				{ "L13 Arabic with harakat, Arial", "Arial", "rtl", "بِسْمِ اللَّهِ لا العَرَبِيَّة" },
				{ "L14 Hebrew with niqqud, Arial", "Arial", "rtl", "שָׁלוֹם עוֹלָם בְּרֵאשִׁית" },
			};
			StringBuilder body = new StringBuilder();
			for (String[] c : cases) {
				boolean rtl = c[2].equals("rtl"), cs = rtl || c[2].equals("cs");
				String fonts = "<w:rFonts w:ascii=\"" + c[1] + "\" w:hAnsi=\"" + c[1] + "\" w:cs=\"" + c[1] + "\"/>";
				String rPr = "<w:rPr>" + fonts + (rtl ? "<w:rtl/>" : "") + "<w:sz w:val=\"28\"/>" + (cs ? "<w:szCs w:val=\"28\"/>" : "") + "</w:rPr>";
				String label = "<w:r><w:rPr><w:rFonts w:ascii=\"Calibri\" w:hAnsi=\"Calibri\"/><w:sz w:val=\"20\"/></w:rPr>"
						+ "<w:t xml:space=\"preserve\">" + c[0] + ": </w:t></w:r>";
				// the label first, then the case on a line of its own, so a reader's extraction of the case is not
				// mixed with the label's (and a right-to-left line is a paragraph of its own)
				body.append("<w:p><w:pPr><w:spacing w:after=\"0\"/></w:pPr>").append(label).append("</w:p>");
				body.append("<w:p><w:pPr>").append(rtl ? "<w:bidi/>" : "").append("<w:spacing w:after=\"240\"/></w:pPr>")
						.append("<w:r>").append(rPr).append("<w:t xml:space=\"preserve\">").append(c[3]).append("</w:t></w:r></w:p>");
			}
			org.docx4j.wml.Document doc = (org.docx4j.wml.Document) org.docx4j.XmlUtils.unmarshalString(
					"<w:document " + ns + "><w:body>" + body + "</w:body></w:document>");
			d.para("Clusters for ActualText (fop/CR-019): each case's text on the line after its label.").noLabel().add();
			d.mdp().getContent().addAll(doc.getBody().getContent());
			return d.pkg();
		});
	}

	/**
	 * vml-box-beside-&lt;portrait|landscape&gt; (ledger9 §9; 10855).  docx4j reserves the height of a square-wrapped
	 * text box at least 60% of the column wide (rules §9.2: nothing useful fits beside it), measured on boxes at a
	 * column edge.  10855's box is 68% of the column but offset: 222.75pt free on its left, where Word sets the
	 * anchor paragraph's heading beside it, and docx4j put the heading below.  Each case on a page of its own: an
	 * anchor paragraph (a short heading, then the box), a three-line paragraph, an ordinary last line.  Portrait
	 * (A4, 72pt margins, a 451pt column): (a) flush left 68%; (b) flush right 68%; (c) centred 50%; (d) 80% leaving
	 * 20% on the left; (e) 68% leaving 22% left and 10% right.  Landscape: 10855's own box, x 222.75, w 476.25 in a
	 * 698pt column.  Read: the y of the heading and of the lines after it against the box's top and bottom; whether
	 * they run beside it or below it.  @since 17.3.1
	 */
	private static Probe vmlBoxBesideProbe(boolean landscape) {
		return new Probe("vml-box-beside-" + (landscape ? "landscape" : "portrait"), "square-wrapped VML text boxes 20pt"
				+ " tall, at margin-relative offsets and widths, in a paragraph holding a short heading, then a three-line"
				+ " paragraph; one case a page; mode 15.  Read whether the heading and the lines run beside the box or below"
				+ " it", () -> {
			Doc d = Doc.create(15);
			double[][] cases;
			String[] tags;
			if (landscape) {
				d.pageGeometry(16838, 11906, true, 1440, 1440, 1440, 1440);
				cases = new double[][] { { 222.75, 476.25 } };
				tags = new String[] { "L10855" };
			} else {
				d.pageGeometry(11906, 16838, false, 1440, 1440, 1440, 1440);
				cases = new double[][] { { 0, 307 }, { 144, 307 }, { 113, 225 }, { 90, 361 }, { 99, 307 } };
				tags = new String[] { "Pa", "Pb", "Pc", "Pd", "Pe" };
			}
			for (int i = 0; i < cases.length; i++) {
				String br = i > 0 ? "<w:pageBreakBefore/>" : "";
				d.add(xmlP("<w:p><w:pPr>" + br + "<w:spacing w:before=\"0\" w:after=\"200\"/></w:pPr>"
						+ xrun(tags[i] + " heading text", SERIF, 24, false)
						+ vmlSquareBoxRun(tags[i], cases[i][0], cases[i][1], i + 1) + "</w:p>"));
				d.add(xmlP("<w:p><w:pPr><w:spacing w:before=\"0\" w:after=\"200\"/></w:pPr>"
						+ xrun(tags[i] + " lines: " + prose(3, i), SERIF, 24, false) + "</w:p>"));
				d.add(xmlP(exactP(tags[i] + " last line", 280, "")));
			}
			return d.pkg();
		});
	}

	/**
	 * table-outer-border-stack (ledger9 part B §17; 9919).  docx4j's tables are 0.33-0.37pt short of Word's above
	 * and below where the outer border is sz 6 (0.75pt): FOP's collapsing model puts half of each outer border
	 * outside the table, where Word appears to stack the whole of it.  Exact 14pt lines, then a two-row one-column
	 * table bordered single at sz 4, 8, 12 and 24 (0.5, 1, 1.5, 3pt), its rows auto-height, each holding one exact
	 * 14pt line, then exact lines again; the same four with no inside border.  Read: the baseline of the line before
	 * each table to that of the line after it.  Word's reading (2026-10-07): the whole of the top, inside and bottom
	 * borders is added to the 42pt; docx4j adds half the top and half the bottom.  @since 17.3.1
	 */
	private static Probe tableOuterBorderStackProbe() {
		return new Probe("table-outer-border-stack", "exact 14pt lines around two-row one-column tables of auto-height"
				+ " rows of one exact 14pt line, single borders at sz 4, 8, 12 and 24, with and without inside borders; mode 15.  Read the"
				+ " distance from the line before each table to the line after it", () -> {
			Doc d = Doc.create(15);
			for (boolean inside : new boolean[] { true, false }) {
				for (int sz : new int[] { 4, 8, 12, 24 }) {
					String tag = "S" + sz + (inside ? "i" : "o");
					String b = "w:val=\"single\" w:sz=\"" + sz + "\" w:space=\"0\" w:color=\"000000\"";
					String borders = "<w:tblBorders><w:top " + b + "/><w:left " + b + "/><w:bottom " + b + "/><w:right " + b + "/>"
							+ (inside ? "<w:insideH " + b + "/><w:insideV " + b + "/>" : "") + "</w:tblBorders>";
					StringBuilder t = new StringBuilder("<w:tbl><w:tblPr><w:tblW w:w=\"4000\" w:type=\"dxa\"/>" + borders
							+ "<w:tblLayout w:type=\"fixed\"/><w:tblCellMar><w:top w:w=\"0\" w:type=\"dxa\"/><w:bottom w:w=\"0\" w:type=\"dxa\"/>"
							+ "</w:tblCellMar></w:tblPr><w:tblGrid><w:gridCol w:w=\"4000\"/></w:tblGrid>");
					for (int r = 1; r <= 2; r++) {
						t.append("<w:tr><w:tc><w:tcPr><w:tcW w:w=\"4000\" w:type=\"dxa\"/></w:tcPr>")
								.append(exactP(tag + " row " + r, 280, "")).append("</w:tc></w:tr>");
					}
					d.add(xmlP(exactP(tag + " before", 280, "")));
					d.add(xmlTbl(t.append("</w:tbl>").toString()));
					d.add(xmlP(exactP(tag + " after", 280, "")));
				}
			}
			return d.pkg();
		});
	}

	/**
	 * table-outer-border-atleast (11657; the table-outer-border-stack probe's open case).  Word adds the whole of
	 * a table's outer top and bottom borders outside auto-height rows (table-outer-border-stack) and keeps them
	 * inside an exact row's height; whether a row of at least a height keeps them inside where the minimum
	 * governs is not measured, and docx4j assumes it does (corpus 11657: 1372 rows at least 15pt, which padding
	 * made a row a page too sparse).  The stack probe's tables - two rows of one exact 14pt line, single borders
	 * at sz 8 and 24, inside borders too - with both rows at least 20pt (the minimum governs), at least 10pt
	 * (the line does), and the first at least 20pt over an auto-height second.  Read: the baseline of the line
	 * before each table to row 1, row 1 to row 2 and row 2 to the line after.  @since 17.3.1
	 */
	private static Probe tableOuterBorderAtLeastProbe() {
		return new Probe("table-outer-border-atleast", "exact 14pt lines around two-row one-column tables whose rows of"
				+ " one exact 14pt line are at least 20pt, at least 10pt, or at least 20pt over auto, single borders at sz 8"
				+ " and 24 with inside borders; mode 15.  Read the baselines before, of each row and after", () -> {
			Doc d = Doc.create(15);
			String[][] rows = { { "200", "200" }, { "400", "400" }, { "400", "" } };
			String[] names = { "A10", "A20", "A20x" };
			for (int sz : new int[] { 8, 24 }) {
				for (int c = 0; c < rows.length; c++) {
					String tag = names[c] + "s" + sz;
					String b = "w:val=\"single\" w:sz=\"" + sz + "\" w:space=\"0\" w:color=\"000000\"";
					String borders = "<w:tblBorders><w:top " + b + "/><w:left " + b + "/><w:bottom " + b + "/><w:right " + b + "/>"
							+ "<w:insideH " + b + "/><w:insideV " + b + "/></w:tblBorders>";
					StringBuilder t = new StringBuilder("<w:tbl><w:tblPr><w:tblW w:w=\"4000\" w:type=\"dxa\"/>" + borders
							+ "<w:tblLayout w:type=\"fixed\"/><w:tblCellMar><w:top w:w=\"0\" w:type=\"dxa\"/><w:bottom w:w=\"0\" w:type=\"dxa\"/>"
							+ "</w:tblCellMar></w:tblPr><w:tblGrid><w:gridCol w:w=\"4000\"/></w:tblGrid>");
					for (int r = 1; r <= 2; r++) {
						String h = rows[c][r - 1];
						t.append("<w:tr>").append(h.isEmpty() ? "" : "<w:trPr><w:trHeight w:val=\"" + h + "\" w:hRule=\"atLeast\"/></w:trPr>")
								.append("<w:tc><w:tcPr><w:tcW w:w=\"4000\" w:type=\"dxa\"/></w:tcPr>")
								.append(exactP(tag + " row " + r, 280, "")).append("</w:tc></w:tr>");
					}
					d.add(xmlP(exactP(tag + " before", 280, "")));
					d.add(xmlTbl(t.append("</w:tbl>").toString()));
					d.add(xmlP(exactP(tag + " after", 280, "")));
				}
			}
			return d.pkg();
		});
	}

	/**
	 * shaded-group-keeps-&lt;first|middle|none&gt; (ledger9; 1152).  docx4j wraps a run of shaded paragraphs in
	 * one block that takes the first paragraph's keeps, so a group taller than the room keeps together and opens
	 * the next page.  Ten shaded paragraphs of about three lines after 33 exact lines (235.9pt left), with
	 * keepNext and keepLines on the first, on the fifth, or on none.  @since 17.3.1
	 */
	private static Probe shadedGroupKeepsProbe(String variant) {
		return new Probe("shaded-group-keeps-" + variant, "33 exact 14pt lines (235.9pt left), then ten paragraphs"
				+ " shaded D9D9D9 of about three lines each, keepNext + keepLines on " + ("none".equals(variant) ? "none"
				: "the " + variant + " one") + "; mode 15.  Read where page 1 ends and where the group starts", () -> {
			Doc d = Doc.create(15);
			fillerLines(d, "SGK", 33);
			for (int i = 1; i <= 10; i++) {
				boolean keeps = ("first".equals(variant) && i == 1) || ("middle".equals(variant) && i == 5);
				d.add(xmlP("<w:p><w:pPr>" + (keeps ? "<w:keepNext/><w:keepLines/>" : "")
						+ "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"D9D9D9\"/><w:spacing w:before=\"0\" w:after=\"0\"/></w:pPr>"
						+ xrun("SGK shaded paragraph " + i + ". " + prose(3, i), SERIF, 24, false) + "</w:p>"));
			}
			d.add(xmlP(exactP("SGK after the group", 280, "")));
			return d.pkg();
		});
	}

	/**
	 * list-label-line-multiplier (ledger9; 3493).  docx4j lays a list label out at its full line box, so an item
	 * on a line multiplier below 1 is as tall as a single line (3493: item pitch 23.5 against Word's 20.18).
	 * Numbered and bulleted items at line 180 auto (0.75), one and two lines, empty paragraphs between.
	 * @since 17.3.1
	 */
	private static Probe listLabelLineMultiplierProbe() {
		return new Probe("list-label-line-multiplier", "decimal and bullet items, Liberation Serif 11, line 180 auto"
				+ " (0.75), one- and two-line items with empty paragraphs between, and the same at 240 auto; mode 15."
				+ "  Read every baseline: the item pitch against plain paragraphs'", () -> {
			Doc d = Doc.create(15);
			String lvl = "<w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"%s\"/><w:lvlText w:val=\"%s\"/><w:lvlJc w:val=\"left\"/>"
					+ "<w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr>" + rpr(SERIF, 22, false) + "</w:lvl>";
			d.numberingXml("<w:abstractNum w:abstractNumId=\"0\">" + String.format(lvl, "decimal", "%1.") + "</w:abstractNum>"
					+ "<w:abstractNum w:abstractNumId=\"1\">" + String.format(lvl, "bullet", "•") + "</w:abstractNum>"
					+ "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num><w:num w:numId=\"2\"><w:abstractNumId w:val=\"1\"/></w:num>");
			for (int line : new int[] { 180, 240 }) {
				String sp = "<w:spacing w:before=\"0\" w:after=\"0\" w:line=\"" + line + "\" w:lineRule=\"auto\"/>";
				d.add(xmlP("<w:p><w:pPr>" + sp + "</w:pPr>" + xrun("Plain paragraph at line " + line + ".", SERIF, 22, false) + "</w:p>"));
				for (int numId = 1; numId <= 2; numId++) {
					for (int i = 1; i <= 3; i++) {
						String text = i == 2 ? "Item " + i + ", two lines: " + prose(2, i) : "Item " + i + ", one line.";
						d.add(xmlP("<w:p><w:pPr><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"" + numId + "\"/></w:numPr>" + sp
								+ "</w:pPr>" + xrun(text, SERIF, 22, false) + "</w:p>"));
						d.add(xmlP("<w:p><w:pPr>" + sp + rpr(SERIF, 22, false) + "</w:pPr></w:p>"));
					}
				}
			}
			return d.pkg();
		});
	}

	/**
	 * line-box-bold-run (ledger9; 4025, 13321).  Bold lines pitch short of Word's in docx4j (Tinos Bold 15.59
	 * against 15.84 at 1.15; a bold twin's box 11.30 against 11.52).  Liberation Serif 12 at single and 1.15:
	 * all regular, all bold, and mixed.  {@code line-box-bold} covers 9 and 11pt at single spacing (docx4j
	 * matches it); this adds 12pt and the 1.15 multiplier, where 4025's residue is.  @since 17.3.1
	 */
	private static Probe lineBoxBoldRunProbe() {
		return new Probe("line-box-bold-run", "Liberation Serif 12, line 240 and 276 auto: five-line paragraphs all"
				+ " regular, all bold, and mixed (alternate words bold); mode 15.  Read the line pitch of each", () -> {
			Doc d = Doc.create(15);
			for (int line : new int[] { 240, 276 }) {
				String pPr = "<w:pPr><w:spacing w:before=\"0\" w:after=\"240\" w:line=\"" + line + "\" w:lineRule=\"auto\"/></w:pPr>";
				String text = prose(5, line);
				d.add(xmlP("<w:p>" + pPr + xrun("Regular, line " + line + ". " + text, SERIF, 24, false) + "</w:p>"));
				d.add(xmlP("<w:p>" + pPr + xrun("Bold, line " + line + ". " + text, SERIF, 24, true) + "</w:p>"));
				StringBuilder mixed = new StringBuilder("<w:p>" + pPr);
				String[] words = ("Mixed, line " + line + ". " + text).split(" ");
				for (int i = 0; i < words.length; i++) mixed.append(xrun(words[i] + " ", SERIF, 24, i % 2 == 1));
				d.add(xmlP(mixed.append("</w:p>").toString()));
			}
			return d.pkg();
		});
	}

	/**
	 * section-final-empty-nextpage-&lt;a|b|c&gt; (ledger9; 12317).  A document whose last section holds only
	 * (a) one empty paragraph, nextPage; (b) the same, continuous; (c) a break-only paragraph, nextPage.  12317:
	 * Word gives (a) a page of its own.  @since 17.3.1
	 */
	private static Probe sectionFinalEmptyNextPageProbe(char variant) {
		return new Probe("section-final-empty-nextpage-" + variant, "a section of text, then a last section holding "
				+ (variant == 'c' ? "a break-only paragraph" : "one empty paragraph") + ", " + (variant == 'b' ? "continuous"
				: "nextPage") + "; mode 15.  Read the page count", () -> {
			Doc d = Doc.create(15);
			for (int i = 1; i <= 3; i++) d.para("SFE section one, paragraph " + i + ". " + prose(2, i)).noLabel().after(120).add();
			d.endSection(variant == 'b' ? "continuous" : "nextPage", 0);
			if (variant == 'c') d.add(breakOnlyParagraph());
			else d.add(xmlP("<w:p><w:pPr><w:spacing w:before=\"0\" w:after=\"0\"/>" + rpr(SERIF, 24, false) + "</w:pPr></w:p>"));
			return d.pkg();
		});
	}

	/**
	 * break-at-double-space (ledger9; 8814).  Word does not break a line at a double space where docx4j does
	 * (8814 p21).  Justified and left-aligned paragraphs whose words are separated by two spaces.  @since 17.3.1
	 */
	private static Probe breakAtDoubleSpaceProbe() {
		return new Probe("break-at-double-space", "Liberation Serif 12 paragraphs, justified and left-aligned, every"
				+ " word followed by two spaces; mode 15.  Read every line's first and last word", () -> {
			Doc d = Doc.create(15);
			for (String jc : new String[] { "both", "left" }) {
				for (int i = 1; i <= 3; i++) {
					String text = String.join("  ", (prose(4, i * 7)).split(" "));
					d.add(xmlP("<w:p><w:pPr><w:spacing w:before=\"0\" w:after=\"240\"/><w:jc w:val=\"" + jc + "\"/></w:pPr>"
							+ xrun(text, SERIF, 24, false) + "</w:p>"));
				}
			}
			return d.pkg();
		});
	}
}
