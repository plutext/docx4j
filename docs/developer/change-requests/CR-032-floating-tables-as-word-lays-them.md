# CR-032: floating tables laid out as Word lays them - the band at its offset, the wide table's band, the paragraphs behind it

Status: IN PROGRESS - phase 1 DONE 2026-10-08 (4a6ab1d89 on VERSION_17_3_2; gates b181-b190, §4.1: lines
matched real +10, real2 +20, real3 +46, class 2 -13; 8985 +45 and 11398 +35 to Word's pages; residual 3229),
phase 0 DONE the same day (nine probes read, §3). Phase 2 waits on the fork's fop/CR-023 (float-offset, and
a line that does not fit beside a float deferred to its foot); fop/CR-022's first half gated PASS on r17
(b186b). D1, D2 and D3 DECIDED yes by Jason 2026-10-08 (§7). Proposed 2026-10-08.

## 0. Summary

A floating table (`w:tblPr/w:tblpPr`) is a positioned object: Word puts it at an offset from the
paragraph it is anchored to, the page or the margin box, and flows the following text around it.
docx4j has honoured the position since 17.1.0 (rules §6.8): a text-anchored table narrower than 60%
of the column becomes an `fo:float`, a page- or margin-anchored one an absolutely positioned
`fo:block-container`, and everything else is left in the flow. That covers the probes it was built
on. It does not cover what the corpora actually contain. Of the 208 floating tables outside cells in
the four corpora, **184 are at least 60% of the column wide** (median: the full column), their median
offset is 6pt, and **133 are followed by an empty anchor paragraph**, 43 by three or more empties.
That is the "empty paragraphs behind a full-width floating table" pattern, which Word lets overlap
and docx4j stacks below the table. Ledger9 attributes about 14 pages over 13 class 2 documents to
floating tables and anchored objects left in the flow, more than any other cause in the set.

The CR has three parts, each gated on its own:

1. **Phase 0**: eight probes for one Word run, settling what Word does with the wide table, the
   paragraphs behind it, a negative offset, a pair of tables, a table in a cell and a table that
   does not fit its page. Today's knowledge is partly measured (the probes of 2026-09-06) and partly
   read off corpus documents; the CR records which is which (§3).
2. **Phase 1**, docx4j alone: the defaults and declines that are plainly wrong today (the missing
   `vertAnchor` is the margin, not the text; a table in a cell gets its offset and indent, not the
   flow; `topFromText`/`bottomFromText` read), and the wide text-anchored table's offset and the
   empties behind it, as far as that can be done without the laid-out height.
3. **Phase 2**, with a fork hook: a side float whose band starts at an offset below its anchor
   line (so the lines above the offset stay full width), and a float that takes the whole column
   (the following text goes below it, the empty lines sit beside it). With it, the wide table joins
   the narrow one as a float and the band is FOP's to lay out, not an estimate of docx4j's.

Phase 3 collects what the probes may show and the corpus needs rarely: tables split across pages,
overlapping pairs, the negative offset that uses the previous page.

## 1. What docx4j does today (read from the code, 2026-10-08)

Both pathways reach the same code. `TableWriter` (docx4j-export-fo) reads `w:tblpPr` and stamps
`docx4j-tblp-*` hints on the `fo:table`; `WordLayoutFixups.anchorFloatingTables` rewrites the FO
from the hints; `hoistFloats` never moves a float holding a table (FOP paints nothing for one at
flow level). The XSLT has no float logic of its own. Properties: `tables.position` (honour
`tblpPr` at all), `tables.float` (text-anchored tables as floats), `tables.reserveBand` (the hidden
copy a mid-page positioned table reserves its band with), all default true under `wordLayoutFixups`.

| Case | Today | Where |
|---|---|---|
| Horizontal position (`horzAnchor`, `tblpX`, `tblpXSpec`) | applied to every floating table, whether or not it leaves the flow; the frame is the page or the text column | `TableWriter.applyFloatingPosition` |
| `vertAnchor=page` / `margin` with `tblpY` or `tblpYSpec` | `fo:block-container absolute-position="fixed"`; a hidden copy reserves the band only when the table does not open its page, is not narrow and sits in the lower half of the page | `WordLayoutFixups.anchorFloatingTable`, `reservesItsBand` |
| **`vertAnchor` absent** | treated as text-anchored | `applyFloatingPosition` :389-413 |
| `vertAnchor=text`, width ≤ 60% of the column, one column, inside the column, no `w:br` run after | `fo:float` (side by the table's centre) holding a one-row band table `{leftFromText, table, rightFromText}`; `tblpY` is `padding-top` inside the float | `floatBesideText`, `floatFloatingTable` |
| **`vertAnchor=text`, width > 60%** | left in the flow; `tblpY` dropped | `floatBesideText` declines |
| Multi-column section | left in the flow (FOP drops a float from a multi-column region silently) | `floatBesideText` |
| **In a cell**, header, footer, footnote | left in the flow; the horizontal position is applied as indent | `floatFloatingTable` :1940 |
| Anchor paragraph followed by a `w:br` inside a run | left in the flow (the float would need hoisting, which loses a table float) | `blockInsideInlineAfter` |
| `topFromText`, `bottomFromText` | never read | - |
| Two floating tables anchored to the same paragraph | two floats, same side: FOP stacks them | - |
| A floating table taller than the room on its page | the float or container is FOP's to break; `doNotBreakWrappedTables` IGNORED | settings doc :217 |

Two things the probes show about the parts that work (b180, `table-floating`, `table-floating-anchor`):

- **The offset narrows the lines above it.** `tblpY=1440` (72pt) is padding inside the float, so the
  table lands within 0.2pt of Word's y, but FOP anchors the float to the anchor paragraph's first line
  and narrows that paragraph's five lines, which Word runs full width (rules §6.8 `s68tblpy`, §10).
  The probe is 0.6875 for that alone.
- **A table with no `vertAnchor` is anchored to the margin.** `table-floating-anchor` case 5
  (`horzAnchor=margin tblpX=1000 tblpY=2000`, no `vertAnchor`): Word draws the table at y=173 =
  72 (margin) + 100 (`tblpY`), below the whole of P05, with nothing beside it. docx4j floats it at
  P05's first line and loses the page break after it (W6/O5 pages).

## 2. The corpus (the four corpora's `resaved-nofields` basis, scanned 2026-10-08)

`w:tblpPr` in body, header and footer parts, by corpus (nested ones in parentheses):

| corpus | documents | tables | documents with one | nested in a cell | `vertAnchor` text / page / margin-or-absent |
|---|---|---|---|---|---|
| real | 191 | 32 | 28 | 0 | 26 / 5 / 1 |
| real2 | 157 | 73 | 25 | 5 | 66 / 5 / 2 |
| real3 | 102 | 49 | 26 | 5 | 41 / 7 / 1 |
| real-c2 | 107 | 64 | 23 | 7 | 56 / 7 / 1 |

Over the 208 outside cells (25 more inside one):

| fact | count |
|---|---|
| width ≥ 60% of the text width (the "wide" rule leaves them in the flow) | **184** |
| width 40-60% | 8 |
| width < 40% (today's float) | 16 |
| text-anchored, \|`tblpY`\| ≤ 15pt | 142 |
| text-anchored, \|`tblpY`\| > 15pt | 38 (39 at 36pt or more) |
| text-anchored, negative `tblpY` | 22 |
| anchor paragraph (the one after the table) empty | **133** |
| three or more empty paragraphs follow the table | 43 |
| anchor paragraph holds a `w:br` | 13 |
| anchor paragraph carries the `sectPr` | 8 |
| followed directly by another table | 21 |
| `tblpYSpec` stated | 3 (`bottom`) |
| `leftFromText`/`rightFromText` | 180 twips (9pt) on most; 141 (7.05pt) and 0 the rest |

So the shipped float covers 16 of 208, and the dominant case is a full-width table with a small
offset and empty paragraphs behind it: cover pages, letterheads, forms and CVs built out of
floating tables. The 13 documents ledger9 names: 5075 11092 10747 6705 13232 14236 1616 6293 937
11490 8985 11741 11398 (three of them anchored pictures and text boxes, the same mechanism; §6).
What the register records for the table ones:

- **937**: body opens with a floating table, `tblpY=786` (39.3pt), 500pt wide, left in the flow
  with the offset dropped; every page break moves.
- **11490**: 26 empty paragraphs after a full-width text-anchored table (`tblpY=-94`, 457pt in
  510) left in the flow.
- **6705**: nine `w:p/` between two `w:tblpPr` tables, which Word overlaps (819pt).
- **11741**, **11398**, **12502**: `tblpY` dropped when left in the flow (134, 89, -218 twips).
- **8985**: a side-by-side pair stacked.
- **1616**: a full-width page-anchored section-opening table taken out of the flow, the next
  table drawn over it (+2 pages).
- **6293**: text-anchored, `tblpY=4369` (218pt), 56% of the column, declined because the anchor
  paragraph opens with a `w:br`; laid at the anchor.
- **11092**: table floats right (`tblpX=3969 tblpY=-33`), nine empties flow beside it in Word; ours
  stacks table then empties (+62pt a page).
- **5936** (real3, G5): a one-cell floating table nested in a cell, laid in flow; ≈215 lines and
  2 pages across 6293, 5936, 11334.

## 3. What Word does

Measured (the 2026-09-06 probes, rules §6.8):

- The frame is the page for `horzAnchor=page`, else the text column; the grid edge goes at `tblpX`
  or where `tblpXSpec` says, with no compatibility-mode adjustment.
- A text-anchored table's top is `tblpY` below the top of the paragraph it precedes; that
  paragraph's lines above the table run full width; the lines beside it stop `leftFromText` /
  `rightFromText` short of it.
- `tblpYSpec=bottom`, `center` and the page anchors land where §6.8 says.
- A table with no `vertAnchor` is anchored to the margin box (probe case 5, §1).

**Measured 2026-10-08, the phase 0 probes** (Word 365 on the VM, fields off; docx4j = HEAD on the
released 2.11-docx4j.5, rendered from Word's re-saves; `y` = pdftotext's top of the word, page margins
72pt; the probes are the regression set from here on):

- **Empty paragraphs do not sit behind a full-width table.** `table-floating-wide-empties` (a 451pt
  table in a 451pt column, `leftFromText`/`rightFromText` 9pt): with `tblpY` 0 and three empties, Word
  draws the table at the anchor's position and the three empties *below* it, the next text at y 386.4;
  docx4j, which leaves the table in the flow, has it at 385.3. **The in-flow treatment of a
  full-width table is Word's layout** to within a point; the register's "empties behind" readings
  (11490, 6705, 11092) were of tables narrower than the column, where a zero-width line fits beside.
- **`tblpY` is honoured, and the lines that fit above the table fill the offset.** The same probe with
  `tblpY` 36pt and eight empties: Word's table is 36.9pt lower than docx4j's, but the text after is only
  10.3pt lower (462.7 against 452.4): two of the eight empties were laid in the 36pt gap above the table,
  six below it. `tblpY` 6pt: table and following text both 7.0pt lower than ours. So the offset's cost to
  the flow is `tblpY` less the lines that fit in it.
- **The offset is measured from the anchor paragraph's top including its space-before.**
  `table-floating-offset-sides` case 4: 24pt of space-before on the anchor moves its first line from 107.9
  to 131.9 but leaves the table at 180.4, where case 1's is (107.9 + 72.5). docx4j measures from the first
  line (195.5).
- **The anchor paragraph's own text runs above the offset, full width, and continues below the table.**
  `table-floating-wide-anchor-text` case 2 (`tblpY` 30pt): the anchor's first two lines above the table,
  the rest below; the following paragraph lands 3.5pt lower than docx4j's (313.6 vs 310.1), since docx4j
  puts the table first and the whole paragraph after. Case 1 (`tblpY` 0): Word = docx4j within a point.
- **Text runs on both sides of a float, and takes any room at all.** `offset-sides` case 1 (a 40% table
  centred): Word's lines run on the left *and* the right of it; case 3 (`tblpX` 1000, a 41pt sliver on
  the left): single words in the sliver and the rest on the right. `wide-anchor-text` case 3 (a 75%
  table, 104pt beside): the anchor's text beside it, word by word. There is no minimum width; whatever
  fits a line segment goes beside. docx4j floats one side only (the side of the table's centre) and
  narrows from the anchor's first line: the following paragraph is 96pt (seven lines) lower than Word's
  in case 1, 54pt in case 3, 15pt in the 75% case.
- **Two floats on one anchor**: left and right (`pair` case 1, two 100pt tables): side by side at the
  same y, the anchor's text between them (x 181.5 to 414). Same side (case 2): Word stacks the second
  below the first (108.4 and 371.7) with the text beside both; it does not overlap them. Two full-width
  tables with three empties between (case 3, 6705's shape as read): first table, three empties, second
  table, text - docx4j identical (P08 300.9 vs 301.9). docx4j's case 1 puts the second table 221pt
  lower than Word's and the anchor text beside it (329.3), and spills to a second page.
- **A negative offset pulls the table above its anchor into the preceding paragraph and into the top
  margin.** `negative` case 1 (`tblpY` -30pt mid-page): the table sits inside the preceding paragraph,
  whose intersecting lines wrap below it; the anchor follows; net 6.6pt on the text after. Case 2 (the
  anchor first on page 2): the table at y 42.8, in the top margin, the anchor at 99.0; docx4j at 71.9 and
  128.1 (29pt).
- **In a cell**: Word floats the one-cell table and wraps the cell's first paragraph beside it (two
  lines beside, two below); docx4j puts that paragraph below. From the second paragraph on, identical
  (P03 171.8 vs 171.7, P08 419.3 vs 419.1): a line's worth of difference, not 5936's two pages, which
  therefore come from something else in that document (its nested table's 36/186 gaps and 70.5pt width
  against a cell, to be re-read).
- **A text-anchored table taller than its page splits across pages exactly as FOP splits it**
  (rows 1-5 / 6-22 / 23-39 / 40 on both; the anchor paragraph after it at 114.9 vs 113.5), and
  **`w:doNotBreakWrappedTables` changes nothing** in Word (`tall-nobreak` identical to `tall`). A
  page-anchored table running past the foot continues on the following pages in both; Word fits 14 rows
  on its first page where docx4j fits 16 (28pt rows against 30pt: the positioned container's row height,
  to be checked).
- **An anchor paragraph opening with `w:br`** (`br-anchor` case 1, 6293's shape): Word floats the 40%
  table and wraps the text beside it, the break giving an empty first line; docx4j declines the float
  (the line-break rule, §1) and the following paragraph is 70pt lower (337.3 vs 267.5). **An anchor
  carrying a continuous section's `sectPr`** (case 2, full width): Word = docx4j within a point.

Still read off corpus documents, not probed:

- Floating tables overlapping each other (6705 as the register reads it; the probe's same-side pair did
  not overlap). To be re-read on the document.

### Phase 0: the probes

One Word run (WordGoldenRunner, fields off, the share's `corpus/` and `corpus.txt`), nine
probes (the tall case split in two for its compatibility setting), built in `docx4j-layout-fidelity`'s
`Corpus.java` with `Table.floating(...)`; cut 2026-10-08 06:27, read the same day (above):

| probe | what it settles |
|---|---|
| `table-floating-wide-empties` | a full-width text-anchored table (`tblpY` 0 / 120 / 720 twips) followed by 0, 3 and 8 empty paragraphs then text: where the text resumes (band bottom + `bottomFromText`?), whether the empties take space |
| `table-floating-wide-anchor-text` | the same table with a non-empty anchor paragraph: does the paragraph's text go below the table, and does the offset count from the paragraph's top |
| `table-floating-offset-sides` | a 40% table centred (`tblpXSpec=center`) with `tblpY=1440`: lines above full, both sides used or one; a fourth case with 24pt of space-before on the anchor: the offset from the paragraph's top including its space, or from its first line |
| `table-floating-negative` | `tblpY=-600` mid-page, and at the top of page 2 (does it use page 1's space) |
| `table-floating-pair` | two 45% tables anchored to the same paragraph, left and right; two full-width tables with three empties between (6705's shape) |
| `table-floating-in-cell` | 5936's shape: a one-cell floating table inside a cell with six paragraphs of text |
| `table-floating-tall` | a text-anchored table taller than the room left on its page, with and without `doNotBreakWrappedTables`; and a page-anchored one |
| `table-floating-br-anchor` | the anchor paragraph opening with `w:br` (6293's shape), and one carrying the `sectPr` |

The readings are above; the design below was corrected on them before phase 1 builds. The nine join
`table-floating` and `table-floating-anchor` as the regression set.

## 4. Design (redirected 2026-10-08 on the phase 0 readings)

What the probes changed: the full-width table in the flow *is* Word's layout, so the corpus's dominant
case needs no float and no hook, only its offset. The losses are in (a) the offset, which docx4j drops
for a wide table and applies from the wrong point for a narrow one, (b) the one-sided, from-the-first-
line float where Word wraps both sides from the offset down, (c) the 60% rule, which keeps tables of
60-99% in the flow where Word wraps text beside them, (d) the declines (`w:br`, no `vertAnchor`), and
(e) two floats on one anchor. Nothing in Word's splitting, in-cell or `sectPr` behaviour needs work.

### 4.1 Phase 1, docx4j alone

- **Default anchors.** `vertAnchor` absent = margin (probe `table-floating-anchor` case 5; the schema
  states no default, so the spec's words are re-read when this builds), `horzAnchor` absent = text.
  **Bounded on gate b182**: the positioned container reserves its band only in the lower half of the
  page (`reservesItsBand`), and positioning upper-half tables with no band ran their following text
  under them (3387 lost a page, 7490 26 lines), so the margin default is taken only where the band is
  reserved; the rest keep the text-anchored treatment they had. The probe's case 5 (y 100pt) stays as
  it was until a float can start at an offset.
- **The offset of a table in the flow** (`WordLayoutFixups.inFlowOffset`). A text-anchored table left
  in the flow gets its `tblpY`: the empty paragraphs after it whose line pitch (plus their spacing) fits
  in the gap are moved above it, Word's rule (§3), and the rest of the gap is a spacer block-container
  before the table - a spacer, not `space-before`, because FOP resolves a space against the previous
  paragraph's space-after (the larger wins) where Word's offset adds to it (probe case 2: 8pt after +
  6pt offset). A text anchor keeps only the gap's remainder modulo its line pitch, since Word fills
  the gap with the paragraph's own lines, which docx4j cannot split; the following text then lands
  where Word's does (probe -wide-anchor-text case 2: 312.5 against 313.6). Probes: -wide-empties
  304.7/461.5 against Word's 305.7/462.7. A negative `tblpY` of up to a line and a half is a negative `space-before` on the table (FOP pulls it up
  over the text before, which is Word's result for a small offset: 11398's three, -10.9/-10.9/-2.25pt,
  +35 lines and its pages to Word's 37, gate b190; the bound takes the previous block's pitch, else the
  anchor's, else 13.8pt); a larger one is **not applied** (3653's -580pt lost 100 lines in b182; Word's
  rule there, the preceding lines wrapping below the table, is phase 3).
- **The 60% rule becomes "2in of room beside"** on a renderer with `side-float-edges`: a wider table
  floats when `column - width - leftFromText - rightFromText ≥ 2in` (`TableWriter.FLOAT_MIN_ROOM`).
  The first cut floated on any positive room (Word puts a word in a 41pt sliver), and gate b182 showed
  why FOP cannot: a word that does not fit its narrowed line overflows it where Word moves it below
  the float, so the slivers cost lines (6705's 36pt and 54pt -9, 1616's 22pt -11, 9832's 142pt -11,
  8236's 1pt -2) and one document hit a fork NPE (4083, `PageBreakingAlgorithm.handleFloat`,
  reproducer `~/fidelity-cr030/repro/float-edge-npe-4083.fo`, sent to the fork session for
  fop/CR-023). The full-width table stays in the flow, which is Word's layout. On Apache FOP the 60%
  rule stays (D3). The 75% probe table (104pt beside) is lost to the threshold until the fork defers a
  line that does not fit, which is back in fop/CR-023 as the general case of the withdrawn ipd-0 hook.
- **The `w:br` decline** (6293, probe case 1: 70pt a page): the fork session rendered its three
  recorded reproducers of the `TraitSetter.setVisibility` NPE on .5/.6 with no exception (fop/CR-011's
  fix), and the probe on the released .5 matches Word within 0.7pt with the decline lifted; lifted on
  `side-float-edges` (gate b181: 8985 +21, 6293 +2, 6131 +2). **Guarded**: 4083's table (tblpY 51pt
  at the page's left) floated from the anchor's first line and cut the question above it into a
  column beside the table (-2 lines, a garbled page), so this newly floated shape floats only when its
  offset is within a line and a half of the anchor; the offset defect is the fork's `float-offset`.
- **Two floats on one anchor.** Both go into the anchor paragraph, each at its side, instead of the
  second trailing the first table's "next sibling" (the probe's 221pt and a spilt page); FOP lays a left
  and a right float side by side. Two on the same side: FOP stacks them, as Word does.
- **In a cell**: no change (a line's worth on the probe). 5936's two pages are re-read on the document.
- **Positioned (page/margin) tables**: unchanged, except the 1616 case (a full-width page-anchored
  table opening its section, the next table drawn over it): `reservesItsBand` never reserves for a
  table that opens its page; a wide one should, since nothing can go beside it. Gated separately on the
  cover-page population.

### 4.2 Phase 2, the fork hooks (fop/CR-023, revised)

- **`float-offset`** (unchanged in kind, settled in semantics): a side float whose intrusion into the
  line boxes begins `offset` below the **top of the anchor paragraph's block including its
  space-before** (probe case 4), the lines in between keeping the full ipd, the content placed at the
  offset. docx4j writes `tblpY` as the offset. This closes `table-floating` (0.69 → 1.0) and the
  narrow-float share of every text-anchored table with an offset (38 of 180 state more than 15pt).
- **Both sides.** Word runs text down both sides of a float and between two floats, taking any
  segment a word fits. `fo:float` is single-sided; the fork would need a line manager that lays a line
  as two segments around an obstacle - §6.6 item 10's project, which the probes now show is a floating-
  table matter as much as a picture one (`offset-sides` cases 1 and 3, 96pt and 54pt a page). Listed
  here as the hook's second half, for the fork session to size; not a condition of phase 1.
- **`float-band` is withdrawn.** The reading it rested on (empties behind a full-width table) is
  refuted (§3); the full-width table is right in the flow, and a narrower one leaves positive room
  where FOP's lines already wrap. What remains of it - a content line beside a float with ipd 0 - cannot
  arise under the "no room beside" rule.

### 4.3 Phase 3, what the probes left

- The negative offset beyond a line and a half, and into the top margin (`negative` case 2: Word at y
  42.8, 29pt above the margin).
- The anchor paragraph's text above a wide table's offset (3.5pt on the probe) once `float-offset`
  exists on the fork: the wide table then floats too, with `float-offset` and room ≤ 0, if the hook's
  ipd-0 case is made to defer content lines; otherwise it stays in the flow with the §4.1 rule.
- The page-anchored container's row height (Word 28pt rows against 30pt, `tall` pages 5-7).
- 5936 and 6705 re-read on the documents (the in-cell wrap and the overlap the register recorded).

## 5. Gates and acceptance

Every phase is a batch item, gated as the fidelity work is: the four corpora and the probes on
the resaved-nofields basis against the current baseline (b180 at the time of writing), read with
`tools/movers.py`, the attempt recorded in the Enterprise register whichever way it goes. The
export-fo and export-fo-tests suites (`FloatingTablePositionTest` grows a case per rule).

Acceptance for the CR: the 13 ledger9 documents at Word's page count or nearer it, none of the
other floating-table documents down, `table-floating` and the nine probes at 1.0 on the fork where
the rules reach (both-sides wrap excepted until the fork has it), and the Apache FOP output unchanged
except where phase 1 applies.

## 6. Out of scope, and the neighbours

- Anchored pictures and text boxes left in the flow (13232, 14236, 10747 of the 13) are the same
  mechanism with their own rules (§9.1, §9.2, the register's `anchor-at-page-foot` and
  `textbox-wide-no-text-beside`); the wide text box's band (`textbox-wide-no-text-beside`) is now a separate question, since
  `float-band` is withdrawn (§4.2).
- Text on both sides of an object (§6.6 item 10) and wrap around a page-positioned object (11):
  the line-manager project, not this CR.

## 7. Decisions for Jason

1. **D1**: go ahead with phase 0's eight probes (one Word run). **DECIDED yes (Jason, 2026-10-08).**
2. **D2**: whether the fork takes the two hooks of §4.2 (a fop CR by the fork session). Without
   it, the CR ends at phase 1's estimate. **DECIDED yes (Jason, 2026-10-08)**; the fork session
   briefed with §4.2 the same day.
3. **D3**: the 60% rule's fate on Apache FOP once phase 2 lands (keep, as the fallback).
   **DECIDED keep (Jason, 2026-10-08).**

## 8. Risks

- The empties-above-the-table rule of phase 1 moves paragraphs in the FO; a wrong line pitch puts an
  empty line on the wrong side of the table (one line). Gated; phase 2's offset makes it exact.
- Lowering the 60% rule to "no room beside" floats 60-99% tables for the first time (about 20 of
  208 in the corpora); FOP's narrowed lines beside a wide float are measured sound (ipd > 0), but the
  edge cases (a float ending inside a table, item 45) are the fork's `side-float-edges`, so the rule is
  gated on that capability.
- The reserveBand change for 1616 touches cover pages, where a hidden copy cost pages before.

## 9. Hand-offs

None to the ports: layout is docx4j's alone. The fork session gets §4.2 as the brief for its CR
when D2 is taken. The rules document (§6.8, §10) is rewritten as each phase lands.
