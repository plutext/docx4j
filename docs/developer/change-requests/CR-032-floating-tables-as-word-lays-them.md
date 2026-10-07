# CR-032: floating tables laid out as Word lays them - the band at its offset, the wide table's band, the paragraphs behind it

Status: PROPOSED 2026-10-08 (Jason: "let's start on the floating-tables CR", after ledger9 §5 named it
the largest page lever left in class 2). Phase 0 (the Word probes) is the next step; nothing is built.
Depends on a fork hook for phase 2 (a new fop CR, to be written by the fork session once Jason says so).

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

Read off corpus documents, to be settled by phase 0:

- **Empty paragraphs sit behind a full-width table.** 11490, 6705, 11092: Word lays the empties in
  the table's band, so the table costs the flow nothing beyond them; non-empty text that does not
  fit beside goes below the table, `bottomFromText` under it.
- **Floating tables may overlap each other** (6705, 8985); Word does not push them apart.
- **A negative `tblpY` pulls the table above its anchor** and may use the previous page's space
  (Vajna, LibreOffice's Word-compatibility notes).
- **A table taller than the room** moves whole to the next page when no row fits, else splits,
  the wrap continuing on the next page (Vajna); `w:doNotBreakWrappedTables` forbids the split.
- **In a cell**, the floating table floats within the cell and the cell's text wraps beside it
  (5936: a 70.5pt table, text at x=149 beside it for six paragraphs).
- **A text-anchored table when the anchor paragraph itself moves to a new page** goes with it.

### Phase 0: the probes

One Word run (WordGoldenRunner, fields off, the share's `corpus/` and `corpus.txt`), eight
probes, built in `docx4j-layout-fidelity`'s `Corpus.java` with `Table.floating(...)`:

| probe | what it settles |
|---|---|
| `table-floating-wide-empties` | a full-width text-anchored table (`tblpY` 0 / 120 / 720 twips) followed by 0, 3 and 8 empty paragraphs then text: where the text resumes (band bottom + `bottomFromText`?), whether the empties take space |
| `table-floating-wide-anchor-text` | the same table with a non-empty anchor paragraph: does the paragraph's text go below the table, and does the offset count from the paragraph's top |
| `table-floating-offset-sides` | a 40% table centred (`tblpXSpec=center`) with `tblpY=1440`: lines above full, both sides used or one |
| `table-floating-negative` | `tblpY=-600` mid-page, and at the top of page 2 (does it use page 1's space) |
| `table-floating-pair` | two 45% tables anchored to the same paragraph, left and right; two full-width tables with three empties between (6705's shape) |
| `table-floating-in-cell` | 5936's shape: a one-cell floating table inside a cell with six paragraphs of text |
| `table-floating-tall` | a text-anchored table taller than the room left on its page, with and without `doNotBreakWrappedTables`; and a page-anchored one |
| `table-floating-br-anchor` | the anchor paragraph opening with `w:br` (6293's shape), and one carrying the `sectPr` |

Each probe's reading goes into §3 as measured, and the design below is corrected before phase 1
builds. The existing `table-floating` and `table-floating-anchor` probes stay as the regression
set.

## 4. Design

### 4.1 Phase 1, docx4j alone

- **Default anchors.** `vertAnchor` absent = margin (probe case 5, §1; the schema states no default,
  so the spec's words are re-read when this builds), `horzAnchor` absent = text. The `tblpY`-with-margin branch then takes the no-`vertAnchor` table
  to the positioned container, as Word does.
- **In a cell.** The floating table inside a `w:tc` is placed within the cell: indent from
  `tblpX` (already), `tblpY` as `space-before`, and - since FOP has no float under a table - the
  cell's following paragraphs are laid below it (no text beside). Not Word's wrap, but the right
  position and no lost lines (5936's +85pt).
- **`topFromText` / `bottomFromText`** read and applied: the band table gets the top gap as
  padding, the bottom gap as `space-after` on the float's block (narrow) or the table (wide).
- **The wide text-anchored table.** Left in the flow as now, with:
  - `tblpY` ≥ 0 as `space-before` on the table (Word's table top is the anchor's top + `tblpY`,
    and the anchor paragraph's text, when there is any, goes below the table: measured by probe 2);
  - the empty paragraphs following it absorbed into the band while their accumulated height is
    at most the table's **estimated** height: rows × the row's line box for single-line cells, the
    `w:trHeight` where stated, or the cell text measured with `TextMeasurer` (CR-017's) against
    the column width. The estimate is conservative: an empty paragraph is only absorbed if the
    whole of it fits in the band. Phase 2 replaces the estimate with FOP's layout.
  - a negative `tblpY` as `space-before` of the same sign where FOP honours it, else 0 (phase 3).
- **The `w:br` decline** (6293): the decline exists because hoisting loses a table float. Since
  dd74eb6fb the XSLT pathway's `w:br` block is wrapped like the visitor's; the remaining case is
  a `w:br` in a run after the float in the same flow. Measure on the fork whether the NPE
  (`TraitSetter.setVisibility`) still occurs with the fork's float edges; if it does not, the
  decline is lifted on a renderer with `side-float-edges`.
- The 1616 case (a full-width page-anchored table opening its section, the next table drawn over
  it): `reservesItsBand` never reserves for a table that opens its page; a wide one should, since
  nothing can go beside it. Gate separately: the cover-page population is where the hidden copy
  was measured to cost pages.

### 4.2 Phase 2, the fork hook

Two things `fo:float` cannot say, which are the whole of the wide-table and offset defects:

1. **A float's band starting below its anchor line.** `fox:float-offset="72pt"`: the float is
   anchored at the line it sits in, but the intrusion into the line boxes begins `offset` below
   that line's top, and the content is placed there; the lines in between keep the full ipd.
   docx4j then writes `tblpY` as the offset instead of padding, and the narrowed-lines-above
   residual of `table-floating` goes (its 0.69 → 1.0, the only defect that probe has).
2. **A float that takes the column.** With `side-float-edges`, a float's edge is the first break
   whose content and glue reach its foot. A float of ipd ≥ the column leaves nothing beside it;
   FOP's behaviour there is unmeasured (phase 0 measures it on plain FOP and the fork before the
   hook is designed). The hook wanted: a line with content goes below the float; a line box with
   no content (an empty paragraph's) is laid beside it, consuming the band, as Word lays the
   empties behind a full-width table. Capability `float-band`.

With both, the wide text-anchored table becomes the same `fo:float` + band table as the narrow
one, and the "60% rule" goes away on the fork (it stays on Apache FOP, where the phase 1 flow
treatment holds). The fork CR is the fork session's to write (fop/CR-023 or whatever number it
takes; fop/CR-022, clear after a side float, is already queued ahead of it) once Jason says so;
docx4j's side is gated by `FopCapabilities` as every hook is, and Apache FOP is unchanged.

### 4.3 Phase 3, what the probes may add

- The negative offset using the previous page (probe 4).
- Overlapping pairs (probe 5): two floats on one side, where Word draws them over each other and
  FOP stacks them; whether to let the second overlap (an absolutely positioned copy) or accept the
  stack.
- Tables split across pages (probe 7) and `doNotBreakWrappedTables`.
- Text on both sides of a centred float (probe 3): §6.6 item 10, the obstacle-aware line manager;
  out of scope here unless the probe shows Word uses one side only, in which case the side rule
  is corrected.

## 5. Gates and acceptance

Every phase is a batch item, gated as the fidelity work is: the four corpora and the probes on
the resaved-nofields basis against the current baseline (b180 at the time of writing), read with
`tools/movers.py`, the attempt recorded in the Enterprise register whichever way it goes. The
export-fo and export-fo-tests suites (`FloatingTablePositionTest` grows a case per rule).

Acceptance for the CR: the 13 ledger9 documents at Word's page count or nearer it, none of the
other floating-table documents down, `table-floating` and the new probes at 1.0 on the fork,
and the Apache FOP output unchanged except where phase 1 applies.

## 6. Out of scope, and the neighbours

- Anchored pictures and text boxes left in the flow (13232, 14236, 10747 of the 13) are the same
  mechanism with their own rules (§9.1, §9.2, the register's `anchor-at-page-foot` and
  `textbox-wide-no-text-beside`); the `float-band` hook serves the wide text box too, which is why
  the text-box band is listed as unblocked once it exists.
- Text on both sides of an object (§6.6 item 10) and wrap around a page-positioned object (11):
  the line-manager project, not this CR.

## 7. Decisions for Jason

1. **D1**: go ahead with phase 0's eight probes (one Word run).
2. **D2**: whether the fork takes the two hooks of §4.2 (a fop CR by the fork session). Without
   it, the CR ends at phase 1's estimate.
3. **D3**: the 60% rule's fate on Apache FOP once phase 2 lands (keep, as the fallback).

## 8. Risks

- The band-height estimate of phase 1 is an estimate; an empty paragraph absorbed wrongly loses a
  line. Conservative by construction, gated, and replaced by phase 2.
- FOP's full-width float behaviour is unknown and may throw (as a float ending inside a table
  did, item 45). Measured first.
- The reserveBand change for 1616 touches cover pages, where a hidden copy cost pages before.

## 9. Hand-offs

None to the ports: layout is docx4j's alone. The fork session gets §4.2 as the brief for its CR
when D2 is taken. The rules document (§6.8, §10) is rewritten as each phase lands.
