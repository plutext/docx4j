# CR-041: Tables placed and sized as Word does - right-aligned and `bidiVisual` tables, a generator's stale grid, `w:jc` end and distribute, percentages below mode 15, and two table-style conditions

Status: ACCEPTED 2026-10-14 (Jason: "D1: phases 1 and 2 in 17.3.3; D2: (a); D3 to D6 agreed"; D7
open); phase 0 started the same day.  Proposed 2026-10-14, raised by a user report (via Jason, 2026-10-14: "table content
alignment (left, right, and center) is broken in 17.x in our xsl-fo output", and the reporter
"fixed the table width issue by using percentages instead of inches").  Measured the same day
with a new probe pair cut by Word (§1).  Reviewed the same day by the docx4j-b0 session at
Jason's request; its points are folded in (§12).  Owner: Jason Harrop.  Seven phases (§5);
decisions in §6.

Scope: what the probe found where docx4j's PDF output (and, through shared code, HTML) places,
sizes or aligns a table or its cell text differently from Word: eight findings (§2.1 to §2.5).
Two are the likeliest causes of the report: finding 1 (right-aligned tables at the left
margin) and finding 2 (a table drawn at a stale `w:tblGrid` instead of its stated width).  A
third candidate the probe does not rule out, a change between docx4j versions rather than a
difference from Word, is in §2.7.  Not in scope: what the probe found to match (§2.6); floating
tables (CR-032); cell margins and spacing (`word-layout-rules.md` §6.2, §6.6, unchanged).

Labels: the findings are numbered 1 to 8; F1 to F16, G1 to G16 and so on are the probe's cases.

## 1. The probe

`docx4j-layout-fidelity`'s `TableAlignmentProbes` (new, uncommitted; registered in `Corpus` as
`table-align-survey-compat15` and `-compat14`): one document in compatibility mode 15 and,
without `overrideTableStyleFontSizeAndJustification` as older documents are, in mode 14.  A4,
1in margins (text column 9026 twips), Liberation Serif 12pt, every table bordered, every cell's
text starting with its case id.  Sections:

| | cases |
|---|---|
| A | a cell paragraph's direct `w:jc`: left, center, right, none, start, end, both, distribute |
| B | its paragraph style's `w:jc`, and direct `w:jc` over the style |
| C | the table style's `w:pPr/w:jc` (CR-030) against Normal, a style stating left, a style stating right, direct |
| D | conditional formats: firstRow + firstCol with `w:tblLook` on and off; band1Horz (style AlignBand) |
| E | `w:vAlign` direct; and from the table style, whole table center with a firstRow bottom |
| F | 16 widths: `w:tblW` auto / dxa / pct, the grid and `w:tcW` agreeing and not, autofit and fixed, over-wide, `pct` cells, none |
| G | 16 positions: `w:tblPr/w:jc` left, center, right, start, end; `w:tblInd`; pct and auto widths; the table style's `w:jc` |
| H, I, J | merged cells; a nested centred table; `w:bidiVisual` with no `w:jc`, `w:jc` left, `w:jc` right |

On the share in `fidelity/corpus`; Word's PDFs in `goldens-nofields`, its re-saves in
`resaved-nofields` (Jason, 2026-10-14).  Read by matching each cell's text word by word in
`pdftotext -bbox` output (the harness's line pairing is thrown by Word's letter-spaced
distributed text); y measured from each case's caption.  Positions below are the first glyph's
x in points; the text column is 72.0 to 523.3.

The grids are the generator's, which Word recomputes on open (`word-layout-rules.md` §6.3's
`table-cell-pct` note).  That is the point here: it is the situation of a document generated
with docx4j and converted without Word, which the reporter's is.  Word's re-saved grid is
Word's layout in twips, and is quoted where it decides a question.

**What was measured, and what is inferred.**  The page geometry is the PDF's (FO, Apache FOP
renderer).  The HTML was generated from the same documents (`FLAG_EXPORT_PREFER_NONXSL`) and
its markup read, not rendered in a browser: where this CR says HTML "writes" something, that
is the markup; what a browser then draws is not measured.  Findings 4 and 6 to 8 are said of
HTML only where the code is shared, and are marked so.

## 2. Findings (measured 2026-10-14, docx4j 17.3.3-SNAPSHOT at 5eeddbe0f)

### 2.1 Where the table sits

**1. A right-aligned table is drawn at the left margin.**  `w:jc` right and end, direct, from
the table style (TableRight), with a dxa, pct or auto width, and with `w:tblInd`: every one at x
77.7 where Word has it against the right margin (303.7 for a 4500-twip table), **-226pt**.  In
mode 14 Word's right-aligned table overhangs the right margin by the cell margin (309.2),
mirroring §6.1's left shift.  `w:jc` start is left in both, as it should be in a left-to-right
table.

- Cause: `TableWriter.applyStartIndent` (FO) handles `JcEnumeration.CENTER` only; right and end
  fall to indent 0.  Word ignores `w:tblInd` on a right-aligned table, and so does the FO
  writer, but only for `RIGHT`: an `END` table still takes its `w:tblInd` (untested by the
  probe; phase 1 covers it).  `PropertyFactory` has dropped `w:tblInd` on a centred or right
  table since 3.0.1 but never positioned one.  Centring came in 17.0.5 and was extended to
  narrower tables in 17.1.0; right alignment was never implemented.  **Not a 17.x regression.**
- HTML (markup read): no aligned table is positioned at all, centred ones included
  (`html.TableWriter` writes `margin-left` for `w:tblInd` only; G3 and G4 carry no margins).

**8. A centred table nested in a cell is 5.7pt right of Word's**, in mode 15 (mode 14 matches).
Word centres the nested table in the containing cell (inner grid edge 197.65); docx4j's
`(available - width) / 2` takes `available` from the page's text column and applies it from the
cell's content edge (203.4).  The nesting is only known in `WordLayoutFixups`
(`nestedTableGridEdge`, §6.1), so the fix belongs there too.  HTML: not measured.

### 2.2 How wide it is

**2. A table whose every column has a `w:tcW` is drawn at its grid, whatever its own `w:tblW`
says.**  Word lays the table out at its stated width; its re-save rewrites the grid to match.

| probe case | `w:tblW` | grid | `w:tcW` | Word (re-saved grid) | docx4j |
|---|---|---|---|---|---|
| F5 | 5000 pct | 3 x 2000 | 3 x 2000 | 3005/3006/3006 | full width (a percentage is not tested against the grid) |
| F6 | 9026 dxa | 3 x 2000 | 3 x 2000 | 3008/3009/3009 | **6000**, the grid; right cell text -151pt |
| F7 | 9026 dxa | 3 x 2000 | 3 x 3009 | 3008/3009/3009 | **6000**, though the cells agree with `w:tblW` |
| F8 | 6000 dxa | 3 x 3009 | 3 x 2000 | 2000/2000/2000 | **9027**, the grid; +151pt |
| F11 | 9026 dxa, fixed | 3 x 2000 | 3 x 2000 | 3008/3009/3009 | **6000** |
| F12 | 5000 pct, fixed | 3 x 2000 | 3 x 2000 | 3005/3006/3006 | **6000** |
| F15 | 5000 pct | 3 x 3009 | 20% / 30% / 50% | 1803/2705/4509 | equal thirds: the centre cell's text +68pt (its column's start +60pt) |

The consistent cases (F1 to F4, F9, F10, F13, F14, F16) match Word within 0.5pt.

- Cause, autofit: `AbstractTableWriter.gridIsAuthoritative` (17.1.0, widened in 17.2.0 by
  badc8d883 and a98854400) returns true on "every column has a preferred width of its own"
  before the table's preferred width is examined.  That is right for a Word-written table,
  whose grid is Word's cached layout (§6.3: of 1,474 corpus tables whose rows disagree with the
  grid, Word drew the grid in 1,443).  It is wrong for a generator's grid that the table's own
  width contradicts, a shape §6.4 counts as absent from Word-written files (none of the three
  corpora's 1,280 tables with an absolute `w:tblW` is more than 2% wider than its grid).  The
  one stale-grid refit, `gridContradictsPreferences` (17.2.0), is gated on `tablePreferred <= 0`,
  so it does not reach these.
- Cause, fixed layout: the autofit and page-fit passes both return null for
  `w:tblLayout fixed`, so F11 and F12 are drawn at their grid.
- **A 17.x change** (17.1.0 and 17.2.0): before it the cells' and the table's widths were used.
- HTML (markup read): F6 is written `width: 300pt` with `table-layout: fixed`, the grid's width;
  F5 `width: 100%`.  The path differs from FO: `measureCellContent` returns null in the base
  class, so `computeAutofitColumnWidths` keeps the grid at the first cell ("cannot measure: keep
  the grid") and never reaches `gridIsAuthoritative`.  §3 puts the fix where both writers pass.
- The reporter's likely case: `TblFactory.createTable` writes `w:tblW auto` with the grid and
  every `w:tcW` at the cell width; code that then sets `w:tblW` in twips, or new cell widths,
  and leaves the grid, gets F6 or F7.  `w:tblW` 5000 pct then "fixes it" because a percentage
  is not tested against the grid (F5).
- It also refutes a working rule of the harness: §6.3 and the harness README advise that a
  probe about a grid use `w:tblLayout fixed` "which Word does not recompute".  Word recomputed
  F11's and F12's fixed grids, which their `w:tblW` contradicts (it kept F10's, which agrees).

**3. Below mode 15 a percentage table is a percentage of the text column plus both cell
margins.**  Mode 14, Word's re-saved grid: 100% is 2310+2311+2311+2311 = 9243 = 9026 + 217;
50% is 4622 = half of 9244.  docx4j takes the percentage of 9026, so a 100% table's right text
edge is 10.8pt short of Word's (A, E1, F4, F5, F12, F15, H, I), 5.5pt for 50% (F9, G10).
Mode 15 matches (3005/3006/3006 is within 9 twips of 9026).  This is §6.1's "below mode 15 it is
the cell content that spans the column" (`#s61autofit`), which docx4j applies to a
content-autofit table but not to a percentage width.  It must land with finding 2: on Word's
mode-14 documents a contradiction test using the wrong base would see every 100% table 2.4% off.
FO only: HTML's `autofitGridAllowanceTwips` is zero and its `width: N%` is a browser's.

### 2.3 A paragraph's `w:jc` values

**4. `w:jc` end is drawn left (-67pt), and `distribute` as ordinary justification.**
`model.properties.paragraph.Justification` writes FO `text-align` and CSS only for left,
center, right and both; start, end, distribute, thaiDistribute, the three kashida values and
numTab write nothing, so the paragraph takes the inherited alignment (left in a left-to-right
table, so start happens to come out right).  Word distributes the letters of every line, the
last included (A2's last line runs to the cell's right edge, 517.9).  Longstanding.  HTML: the
same code (inferred).

### 2.4 `w:bidiVisual` tables

**5. A `w:bidiVisual` table is drawn at the left margin and its cells' text right-aligned.**
Word: the columns reversed (docx4j matches); the paragraphs keep their own direction, so a cell
with no `w:jc` is left-aligned (J3, J4); with no `w:jc` or `w:jc` left the table sits against
the **right** margin (J1, J3: grid 223.3 to 523.3), and with `w:jc` right against the left (J4)
- left and right read as start and end.  docx4j: every table at the left margin (-151pt for J1,
-81 for J3; J4's position right), and the text of every cell without its own `w:jc`
right-aligned (+70pt in J4).  A `w:bidi` paragraph's alignment matches Word (J2).
- Cause: `TableWriter` sets `writing-mode="rl-tb"` on the `fo:table` (3.0.2), which reverses
  the columns as wanted and is inherited by every block in them, so the inherited `text-align`
  start is the right.  HTML writes `direction: rtl` on the table (markup read), with the same
  inheritance.  The position is finding 1's.

### 2.5 Two table-style conditions (CR-030's resolver)

**6. Row banding starts at row 1 when the style has no firstRow format.**  Style AlignBand:
band1Horz right-aligned, band size 1, no firstRow conditional; `w:tblLook` firstRow on, row bands
on.  Word right-aligns rows 1 and 3 and its re-save marks row 1 `oddHBand`; docx4j right-aligns
rows 2 and 4 (+-100pt every cell), because `TableStyleConditions.atPosition` leaves row 1 out of
the bands whenever the look has firstRow on, never consulting the style.  The working
hypothesis: Word leaves the first row out of the banding only where the style chain gives
firstRow a format.  A style without one is rare in Word-written files (every built-in table
style has one, and Word-saved tables usually carry `w:cnfStyle`, which the resolver reads
first), so the population is generated tables with custom styles.  HTML: shared (inferred).

**7. In mode 15 Word drew a firstRow `w:vAlign` bottom as the whole table's center.**  Style
VCentre: `w:tcPr/w:vAlign` center for the table, bottom for firstRow; rows at least 0.75in.  Mode
15: the header cells are vertically centred (y 93.0 against a top-aligned neighbour at 72.8);
mode 14: at the bottom (113.2), as the style says.  docx4j draws the bottom in both.  Word's
re-save marks the row firstRow in both modes.  One data point; the rule is not known (vAlign
alone, or every conditional `w:tcPr`; the mode, or the setting).  HTML: shared (inferred).

### 2.6 What matches (within 0.5pt, both modes unless said)

Direct, paragraph-style and table-style `w:jc` in cells (A1, A2 start and both, B, C1 to C3,
including the mode-14 cases CR-030 settled); firstRow and firstCol conditionals (D1, D2);
direct `w:vAlign` (E1, x and y); merged cells (H); the nested table's own cells and the outer
cell's text (I); centred tables of any width (G3, G9, G10, G12, G14); `w:tblInd` (G7); left and
start (G1, G2, G5, G16); every width case whose grid, cells and table agree.  Word's blank pages
4 and 6 are the probe's (its page-break paragraphs fall at the foot of full pages), not a finding.

### 2.7 A candidate not ruled out: the table-style justification exception between versions

The reporter's words were "content alignment".  §2.6 finds cell alignment matching Word, but
a user who compares with an earlier docx4j's output, not with Word, sees what changed between
versions.  [MS-DOCX]'s exception (below the setting `overrideTableStyleFontSizeAndJustification`,
a paragraph style's left justification does not override the table style's `w:jc`) has been in
`ParagraphStylesInTableFix` since 8.x, and moved three times in 17.x:

- **17.0.4** (bfc196ebf): a document stating the setting 0 gets the exception; before, the
  preprocess overwrote the setting before reading it, so such documents lost it.
- **17.2.0** (9de10aac9): `createPackage` writes mode 15 with the setting on, where it wrote
  mode 12 (Word 2007) before, so a newly generated document no longer takes the exception.
- **17.3.1** (CR-030, D6): the exception is never applied in mode 15, where it was applied
  whatever the mode.

Each move brought docx4j to Word's answer (C1 to C3 match in both modes), but a generated
document that aligns cells by paragraph style under a table style carrying a `w:jc` can change
from centred to left, or back, across an upgrade, and look broken to someone who never opens it
in Word.  The probe cannot rule this out; the reporter's compatibility mode, the setting, the
docx4j version they compare with, and a sample would (D7).

## 3. Design

**Finding 1 and finding 8, the table's position (FO and HTML).**  In `applyStartIndent`,
`w:jc` right or end puts the table's grid end on the text column's end:
`start-indent = available - width`, plus one right cell margin below mode 15 (the overhang
measured), `w:tblInd` ignored for both right and end; start reads as left.  An over-wide
right-aligned table (presumably overhanging the left margin) waits for its phase-0 reading; the
in-column case needs nothing beyond §2.  A nested table's centring or right alignment is taken
against the containing cell's content width in `WordLayoutFixups`, beside `nestedTableGridEdge`.
HTML: `margin-left` and `margin-right` auto for center, `margin-left: auto; margin-right: 0` for
right and end.

**Finding 2 and finding 3, the width.**  The grid stays authoritative **unless the table's own
`w:tblW` contradicts it**: an absolute or percentage `w:tblW` whose grid sum differs from it by
**more than 2%** (percentages resolved on finding 3's base below mode 15).  2% is the threshold
of §6.4's corpus count; the existing "the grid sums to `w:tblW` within 1%" test in
`gridIsAuthoritative` is unchanged, so between 1% and 2% today's rules stand.  Such a grid is a
generator's; the table is laid out at its `w:tblW` with the columns in proportion to the cells'
`w:tcW` where every column has one (scaled to fill, as Word does with F6's 2000s and F15's
percentages), else to the grid's, else by content - autofit and fixed alike (F11, F12).
- The test goes at the head of `computeAutofitColumnWidths`, before any cell is measured, and
  the fixed-layout return beside it: a contradicted table's columns come from the cells or the
  grid, so it needs no content measure, and the HTML writer (which measures nothing) takes it
  too.  The page-fit pass then treats the result as it treats any computed columns.
- The shape is absent from Word-written files by §6.4's count in one direction; phase 0 counts
  the other (a grid wider than `w:tblW`, F8) and the percentage case before anything changes.
- `w:tblW auto` tables keep today's rules.
- One switch restores the grid: the existing `docx4j.convert.out.fo.tables.refitStaleGrid=false`
  (D3).
- Finding 3: below mode 15 a percentage `w:tblW` is of the text column plus the table's left and
  right cell margins (FO only).

**Finding 5, `w:bidiVisual` (FO and HTML).**  The columns stay reversed; the cells' content is
set back to left-to-right so a paragraph keeps its own direction (in HTML `direction: ltr` on
the cells).  In FO, an `fo:block-container writing-mode="lr-tb"` per cell if FOP honours it
there (phase 0 checks, on Apache FOP and the docx4j renderer), else an explicit `text-align`
and the start and end indents mapped for each cell block.  The table's position mirrors finding
1: no `w:jc`, left or start against the right margin, right or end against the left, and
`w:tblInd` from the right (to be measured).

**Finding 4, `w:jc` values (FO and CSS, both pathways).**  start and end to `text-align` start
and end (the paragraph's own direction decides, as Word's J2 shows for left and right);
distribute and thaiDistribute to justify with `text-align-last` justify (CSS adds `text-justify:
inter-character`); the kashida values to justify; numTab left.  FOP stretches word spaces only,
so a distributed line of one word stays unspread (D4).  **After finding 5**: in today's `rl-tb`
tables an `end` paragraph writes nothing and inherits the right; written as `end`, it would go
to the left until the cells are left-to-right.

**Findings 6 and 7, table-style conditions (the resolver, `TableStyleConditions`).**  As phase
0's probes settle them: finding 6 most likely "the first row (last row, first column, last
column) is left out of the bands only where the style chain gives that condition a format";
finding 7 whatever the probes show.  These change what `PropertyResolver` reports in a table
context, so the ports that mirror CR-030 (core-ts, python, mcp) follow (D5).

## 4. Population and risk

- Findings 1, 4, 5: no Word-written-file risk beyond the tables they describe; finding 1's
  population is every right-aligned table (centred ones already work), finding 5's every
  `w:bidiVisual` table.
- Finding 2 is the risky one.  §6.3 records the trap: reading a generated probe's grid as Word's
  took `table-cell-pct` 43% to 100% and cost a quarter of the corpus 0.8967 to 0.8771.  The rule
  above is drawn to fire only on a contradiction Word-written files do not have, and phase 0
  checks that on the three corpora and their re-saves before any code.
- Finding 3 moves every percentage table in a mode-12 or mode-14 document by up to two cell
  margins.
- Findings 6, 7: generated tables with custom styles, and (7) wherever the rule turns out to
  reach.
- Phase order: finding 4's mapping regresses `end` in `w:bidiVisual` tables unless finding 5
  lands first (§3).

## 5. Phases

Every phase that changes output is gated as CR-039 and CR-040 were: core-tests and
export-fo-tests green; the share's probes, none down and the phase's `table-align-survey` cases
at Word's position; the three corpora re-scored against the current baseline (b227 or later,
`~/fidelity-cr030/gate-*.sh`), **movers** listed and each explained, none down without a
reason recorded.  Each gate is recorded in the Enterprise fidelity register's `attempts.md`,
kept or reverted.

**Phase 0 - measure; no library change.**
- The register: a cause entry per finding, before its phase.  Related seed entries exist and
  are linked from them: C9 (the compat-14 grid edge, `causes.md` line 104), "E4/E5" (over-wide
  and refitted grids, line 312), G6 (row 1's `w:tcW` against the grid, line 511, fixed by
  0a457a177) and L1 (declared column geometry discarded by autofit, line 1363).  Finding 2 is
  the mirror of G6 and L1, which 17.1.0 and 17.2.0 answered (the grid wins); L1 is proposed
  closed as fixed by badc8d883, and the new cause records where that answer stops.
- Probes, added to `TableAlignmentProbes`, cut by Word with re-saves:
  - widths: unequal `w:tcW` against an unequal grid under a contradicting `w:tblW` (the
    proportions rule); a grid wider than `w:tblW` with content that needs it (does Word keep
    it?); fixed layout with a consistent grid and a contradicted one; mode 12;
  - position: a right-aligned table wider than the column; right and end with `w:tblInd`,
    positive and negative; a nested right-aligned table; mode 12;
  - `w:bidiVisual`: with `w:tblInd`, with `w:jc` center, start and end, in mode 14;
  - `w:jc`: start and end in a `w:bidi` paragraph; distribute on one line and on several;
    thaiDistribute; a kashida value in an Arabic paragraph;
  - finding 6: a style with a firstRow format and row bands; with an empty firstRow
    `w:tblStylePr`; the same for lastRow and for first and last column with column bands;
  - finding 7: the conditional `w:vAlign` with and without a whole-table one, in modes 15, 14
    and 12, and with the setting stated 1 and 0; a conditional `w:shd` beside it.
- Corpus counts over the three corpora and Word's re-saves: tables whose absolute or
  percentage `w:tblW` their grid contradicts by more than 2% in each direction (percentages on
  finding 3's base below mode 15), how many Word kept, and the same for fixed layout;
  `w:bidiVisual` and right-aligned tables; styles without firstRow formats used with a firstRow
  look.
- FOP: whether `writing-mode` on an `fo:block-container` inside an `rl-tb` table's cell is
  honoured, on Apache FOP and on the docx4j renderer (finding 5's design).
- An HTML render of the probe in a browser, to settle what §2 says of HTML from the markup.
- Readings recorded in this CR (§7), and §3's rules confirmed or amended before the phase that
  needs them.

**Phase 1 - the table's position (findings 1, 8), FO and HTML.**  `applyStartIndent` (right and
end, `w:tblInd` ignored for both), the nested case in `WordLayoutFixups`, HTML margins.  The
in-column cases first; the over-wide right-aligned case after its phase-0 reading.  Tests in
export-fo-tests (FO) and core-tests (HTML).

**Phase 2 - the width (findings 2, 3), FO and HTML.**  The contradiction test at the head of
`computeAutofitColumnWidths`, after phase 0's counts; the percentage base below mode 15.  Tests
pinning the seven cases of §2.2 and the two percentages of finding 3; the tests pinning today's
rules (`TableWidthTest`, `PercentageTableWidthTest`, `TableGridAuthoritativeTest`) reviewed case
by case, each kept, re-pinned with the reason, or retired.

**Phase 3 - `w:bidiVisual` tables (finding 5), FO and HTML.**  Before phase 4.

**Phase 4 - `w:jc` values (finding 4).**  `Justification`'s FO and CSS output; both pathways.
Lands with or after phase 3 (§3).

**Phase 5 - table-style conditions (findings 6, 7).**  `TableStyleConditions` (and whatever phase
0 shows finding 7 needs); `TableContextResolutionTest` cases; the hand-offs (D5).

**Phase 6 - the record.**  `word-layout-rules.md` §6.1 (right and nested alignment,
`w:bidiVisual`), §6.3 (the contradiction rule; the fixed-layout advice corrected), §6.4 (the
declared-width case is not only a probe artefact: it is every generated table), §6.10 (findings
6, 7); the harness README's fixed-layout advice; CHANGELOG lines; the register's causes closed;
the D6 helper if taken; the registry.

## 6. Decisions for Jason

**Decided 2026-10-14 (Jason): "D1: phases 1 and 2 in 17.3.3; D2: (a); D3 to D6 agreed.  Commit,
then start phase 0."**  So phases 1 and 2 ship in 17.3.3 (phase 0 first, as phase 2 needs its
counts); the width rule is (a); the switch is `refitStaleGrid`; `distribute` justifies on word
spaces; phase 5 is handed off with its commit; the helper goes in phase 6.  D7 (the reply to the
reporter) is not yet decided.  As proposed:

- **D1. Order and release.**  Recommended: phase 1's in-column cases first (they need nothing
  beyond §2), then phase 0, then phase 2; for 17.3.3, phases 1 and 2 (the reporter's two) if
  phase 0's counts come back clean in time.  Phases 3 and 4 together, in that order; phase 5 as
  it lands.  (Review: phase 1 alone can go first; phase 4 waits for phase 3.)
- **D2. The width rule.**  (a) The grid gives way only where the table's own `w:tblW`
  contradicts it (§3; recommended: it is the one signal a generated table carries and Word's
  files lack).  (b) Decide by producer: trust the grid only where `docProps/app.xml` names Word.
  Not recommended: a template Word wrote and code filled has Word's grid for its original rows
  and the code's widths for its new ones.  (c) Leave the rule, document the workaround (keep
  `w:tblGrid` consistent with `w:tblW` and `w:tcW`, or use percentages), and add D6's helper.
  The reviewer agrees with (a).
- **D3. The switch.**  Reuse `docx4j.convert.out.fo.tables.refitStaleGrid` (default true; false
  draws every grid as written, today's behaviour plus the auto-width refit off), or a new key
  for the contradiction test alone.  Recommended, and the reviewer agrees: reuse, one switch
  for "the grid as written".
- **D4. `distribute`.**  Recommended: justify with the last line justified, word spaces only,
  and say so in §6 of `word-layout-rules.md`.  The alternative is inter-character justification
  in the FOP fork (a §6.6 item and a fork CR); rare outside East Asian and Thai text.
- **D5. Hand-offs.**  Phase 5 changes the resolver's table conditions, which core-ts, python and
  mcp mirror from CR-030; phases 1 to 4 are exporter-only.  Recommended: hand off phase 5's
  rules with their commit, as CR-030 did.
- **D6. A helper for generated tables.**  A small API that sets a table's width and keeps
  `w:tblGrid` and the `w:tcW` consistent with it (for example `TblFactory.setTableWidth(tbl,
  width, type)` rescaling the grid and the cells), so that a generated document shows the same
  in Word, LibreOffice (which lays out on the grid) and docx4j.  Recommended, and the reviewer
  agrees: yes, in phase 6.
- **D7. The reporter.**  Recommended: reply now with what was found and the workaround (D2(c)'s
  wording) for finding 2, that right-aligned tables are being fixed, and ask for their
  documents' compatibility mode and `overrideTableStyleFontSizeAndJustification` setting, the
  docx4j version they compare with, and a sample: that settles findings 1 and 2 against their
  files rather than the probe's, and rules §2.7 in or out.

## 7. Readings

(Phase 0 adds its readings here.)

## 8. Hand-offs

- core-ts, python, mcp: phase 5's condition rules (D5).
- The fidelity register (Enterprise): a cause per finding, before its phase (phase 0).
- `upstream-fop`: only if D4 takes the fork route.

## 9. Not in scope

- Floating tables (`w:tblpPr`): CR-032.
- The autofit sizer's content measure (§6.3's minima), cell margins, cell spacing.
- What Word does with a percentage over 100% beside `tblpXSpec` (register "E4/E5 pct", known).
- Table alignment in pptx and xlsx output.

## 10. Effort (rough)

Phase 0: a day, one Word run.  Phase 1: half a day.  Phase 2: two to three days, most of it the
gate and the existing tests.  Phase 3: a day.  Phase 4: half a day.  Phase 5: a day plus
hand-offs.  Phase 6: half a day.

## 11. Implementation record

(None yet.)

## 12. Review (docx4j-b0 session, 2026-10-14, at Jason's request)

The review confirmed the code claims of findings 1, 2, 4, 6 and 8 at 5eeddbe0f, that
`TableAlignmentProbes` compiles and is registered, and the registry entry.  Folded in:

1. **HTML and phase 2.**  `measureCellContent` returns null in the base class, so the HTML writer
   keeps the grid before `gridIsAuthoritative` is reached.  Checked (line 480): so.  §3 puts the
   contradiction test before the measurement, and §1 says what of HTML was read from markup and
   what is inferred; phase 0 renders the HTML in a browser.  (The markup shows F6 at `width:
   300pt`, `table-layout: fixed`, so HTML's finding 2 is not undone by a browser's auto layout.)
2. **A third candidate.**  The table-style justification exception's moves between versions:
   §2.7, and D7 asks what settles it.  The review dated it 17.2.0; the code's history puts it in
   ParagraphStylesInTableFix since 8.x, moved in 17.0.4, 17.2.0 (createPackage's mode) and
   17.3.1 (CR-030 D6).
3. **Tolerance.**  2% stated, with the existing 1% test left as it is (§3).
4. **Phase order.**  The regression is in `end`, not start: in today's `rl-tb` tables start
   writes nothing and lands right either way, while `end` would move from right to left.  The
   conclusion stands: `w:bidiVisual` (now phase 3) before the `w:jc` values (now phase 4).
5. **Labels.**  The findings are numbered 1 to 8; F, G and the rest are the probe's cases.
6. **The register.**  C9, E4/E5, G6 and L1 named and linked in phase 0; L1 proposed closed.

Smaller points: F15's +68pt is the centred text, +60pt its column's start (both stated); D1 no
longer contradicts §3 on the over-wide right-aligned table; `applyStartIndent` ignores
`w:tblInd` for `RIGHT` only, not `END` (finding 1, phase 1); FOP's `writing-mode` support is
checked in phase 0 before finding 5's design is fixed.
