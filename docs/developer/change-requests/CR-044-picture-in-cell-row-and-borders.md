# CR-044: A floating picture in a table cell - the row it grows, the borders it clears - and table borders drawn where Word draws them

Status: IN PROGRESS - proposed 2026-10-16 (Jason: "let's make a CR for this, recording what we have done for
17.3.3 shipping on .7, and what we might do in .8").  D1, D2 and D3 DECIDED by Jason 2026-10-16 ("D1 and D2 yes,
please commit; D3 make the border probe now"), §6.  Phase 0 DONE (probes anchor-in-cell-row-height and -compat14,
Word goldens 2026-10-16 05:43, §2); phase 1 DONE for 17.3.3 on 2.11-docx4j.7 (§3.1, §8; gates b275 and b276);
phase 2 DONE the same day (the border probe read, §7.1: Word's rule needs no renderer change, and docx4j's cell text
is off it today in mode 14 and beside unequal borders); phases 3 and 4 PROPOSED, route (b) - docx4j alone - now
recommended (§3.2) and DECIDED (D4, Jason 2026-10-16: "D4 yes; do the census first"); the census DONE the same day
(§5); a renderer change would have been 2.11-docx4j.8 only (Jason, 2026-10-16: "any such change would be part of
2.11-docx4j.8, not the imminent 2.11-docx4j.7").  D5 open (§6).  Owner: Jason Harrop.

Scope: found on a user's letter (local only, not in the corpora; "the letter" below).  Its page 2 opens with a
one-row table of two 284.4pt cells whose second holds a 139.65 x 36pt logo, floating (`wp:anchor`, wrapSquare,
`behindDoc="1"`, `layoutInCell="1"`, 139.14pt from the margin and 0.04pt from the page, which Word measures from
the cell: register cause anchor-in-cell-from-page, docx4j ad0312d2d).  Two differences from Word: the row (§1.1) and
the borders round the logo (§1.2).  Not in scope: §9.

## 1. What was wrong (docx4j HEAD aadc002c8 on the fork's r46)

### 1.1 The row's height

Word grows the row to the logo's foot: its borders at 18.0pt and 54.5pt, the logo 18.5pt to 54.5pt.  docx4j placed
the logo in the cell but in a zero-height `fo:block-container` (`overflow="visible"`), so it took no room: the row
ended at its empty paragraph (32.3pt), and the rest of page 2 stood 22pt high ("List of Acceptable Documents" at
36.1pt against Word's 58.7pt).

### 1.2 The borders under the logo

With the row grown (§3.1), the logo still covered the cell's top and right borders (Jason's screenshot, about
211 dpi), where Word's borders circumscribe it.  Not paint order: Word, like FOP, paints the borders first and the
picture after (page 2's content stream: 17 border fills, then the image).  Geometry:

- **Word draws each border on the far side of its grid line**, to the right of a vertical one and below a
  horizontal one, so a table's right and bottom borders lie outside its grid and a cell's content starts after
  its whole left and top border.  The letter's page 2:

  | border | grid line | Word draws it at | FOP drew it at |
  |---|---|---|---|
  | left (table) | 21.6 | 21.6 to 22.1 | 21.36 to 21.84 |
  | between the cells | 306.0 | 306.1 to 306.6 | 305.76 to 306.24 |
  | right (table) | 590.4 | 590.6 to 591.1 | 590.16 to 590.64 |
  | top | 18.0 | 18.00 to 18.48 | 18.00 to 18.48 |
  | bottom | 54.5 | 54.50 to 54.98 | 54.52 to 55.00 |

- **FOP centres a collapsed border on its grid line** (XSL 1.1's model).  docx4j already moves a table down by half
  its outer border (`WordLayoutFixups.outerBordersOutsideTables`, 17.1.x: the top and bottom borders are Word's) and gives
  the half-borders' charge back to the cell's text measure (`cellLineWidth`, the end padding 4.93pt for Word's
  5.4pt; `clipExactRows` likewise), so the text is where Word's is; nothing moves the *vertical* borders, which are
  painted half their width left of Word's.  The logo ends at the table's last grid line, 590.4pt, in Word and in
  docx4j: clear of Word's right border, 0.28pt into ours.
- **Every positioned picture sat 0.045pt above its stated top.**  Its holder is a 0pt line of a 0.1pt font, on
  which FOP sets the image that much above the line's top (the area tree: a 35.929pt line for a 36pt image).  In
  the letter, 18.475pt for the cell's text top (18.48) + 0.04: 0.005pt into the top border.

At screen resolutions a pixel the picture and a border share shows the picture, painted later.  Sampled with mutool
at 144 and 211 dpi, Word's three borders round the logo show; docx4j's top and right ones did not.

## 2. What Word does with a picture in a cell (phase 0)

Probes `anchor-in-cell-row-height` (mode 15) and `-compat14` (docx4j-layout-fidelity `Corpus`; Word's goldens
2026-10-16 05:43): a one-row two-cell table (225.65pt cells, 0.5pt borders, no top or bottom cell margin), the first
cell one 14pt line, the second an empty paragraph holding a floating picture 100 x 60pt (column 10pt, paragraph 0,
distL/distR 9pt) unless stated; then four lines.  The row's foot (the bottom border's top, pt) and docx4j's before
(r46, HEAD) and after (phase 1):

| case | the anchor | Word, mode 15 | Word, mode 14 | before | after |
|---|---|---|---|---|---|
| 1 | wrapSquare | 146.7 | 146.7 | 100.7 | 146.7 |
| 2 | wrapSquare, behindDoc 1, margin 20pt / page 0 (the letter's) | 146.7 | 146.7 | 100.7 | 146.7 |
| 3 | wrapTopAndBottom | 160.6 | 160.6 | 160.7 | 160.7 |
| 4 | wrapNone, in front | 100.6 | 100.6 | 100.7 | 100.7 |
| 5 | wrapNone, behind | 100.6 | 100.6 | 100.7 | 100.7 |
| 6 | wrapTight | 146.7 | 146.7 | 100.7 | 146.7 |
| 7 | wrapSquare, layoutInCell 0 | 146.7 | table below the picture (146.0 to 160.6) | 100.7 | 146.7 / 100.7 |
| 8 | wrapNone, layoutInCell 0 | 100.6 | 100.6 | 100.7 | 100.7 |
| 9 | wrapSquare, 20pt below its paragraph | 166.6 | 166.6 | 100.7 | 166.7 |
| 10 | wrapSquare, distB 10pt | 156.5 | 146.7 | 100.7 | 156.7 / 146.7 |
| 11 | wrapSquare, cell margins top and bottom 5pt | 156.5 | 156.5 | 110.8 | 156.8 |
| 12 | wrapSquare, the first cell six lines (84pt) | 170.7 | 170.7 | 170.7 | 170.7 |
| 13 | wrapSquare, the row exact 20pt | 106.1 | 106.1 | 106.0 | 106.0 |
| 14 | wrapSquare, the row at least 20pt | 146.7 | 146.7 | 106.7 | 146.7 |

**Word's rule**: an object Word lays out in its cell and wraps the cell's text round (square, tight, through)
grows the row to its foot, whatever `behindDoc` says; with no wrap it does not.  Below the foot: its distB in mode
15, not in mode 14; then the cell's bottom margin.  An exact row keeps its height; an at-least one grows.
Top-and-bottom puts the paragraph below the object (docx4j already did).  In mode 14 an object with `layoutInCell`
0 leaves the cell: placed at the column, the table below it (case 7; §9).

## 3. Design

### 3.1 17.3.3, on 2.11-docx4j.7 (phase 1, docx4j only)

1. **The row grows.**  `WordXmlPictureE20.stampAnchorHints` stamps `HINT_ANCHOR_CELL_FOOT`
   (`docx4j-anchor-cell-foot`) on a square, tight or through anchor Word lays out in its cell (mode 15, or
   `layoutInCell` 1): its distB in mode 15, 0 below.  `WordLayoutFixups.anchorImage` -> `growRowToContain`: the
   `fo:table-row`'s `height`, which FOP takes as the least content height of each cell (padding and borders added),
   becomes at least the object's top below the cell's text top + its height + the hint.  The top is `placeInCell`'s
   for a page- or margin-relative position, else the paragraph's (a line for each block before it in the cell, a
   lower bound) + the offset.  Not for an exact row, nor a cell spanning rows.  Pictures and charts (both go
   through `anchorImage`); text boxes were not measured.
2. **The picture at its top.**  In `anchorImage`'s positioned branch (a zero-height wrapper and an absolutely or
   fixed positioned container: pictures in cells and page-positioned ones) the holder's font is 0pt, which puts
   the image at the container's top (18.52 in the letter, where it was 18.475).  A normal line put it 0.014pt below,
   into the bottom border under a row grown to it.  Floats and reserved bands keep their holder: their line's height
   is their height.
3. **Clear of the cell's end border** (a stopgap for §1.2's geometry; phase 4 removes it).  `clearOfCellEnd`: an
   object placed from its cell's text that reaches into the cell's end border by no more than that border's width
   is cut there (`overflow="hidden"`, the width short by the excess; the letter's logo by 0.284pt of its white
   margin).  One reaching further, which Word draws over its border too, is left.  At 211 dpi the right border then
   shows; at 144 dpi it is still faint (a 242 grey for Word's 59), since FOP's border starts at the picture's edge
   pixel.

### 3.2 Borders where Word paints them (phases 3 and 4)

Proposed before phase 2's probe, which settled it (§7.1): Word's model is FOP's - every vertical border centred on a
grid line - with the grid moved, so route (b) is exact, needs no renderer change and is not tied to .8; and it
also puts the cell text where Word's is in the two cases docx4j has it wrong today.  The two routes as proposed:

- **(a) The renderer** paints a collapsed border on Word's side of its grid line, opted into by a property docx4j
  writes (a `fox:` extension on `fo:table`, so the renderer's XSL behaviour is unchanged for anyone else), with the
  cells' content left where it is: docx4j's compensations (§1.2) already put the text where Word's is, so if the
  probe shows Word's difference is in painting only, only painting changes.
- **(b) docx4j alone**: move each table right by half its start border and take that from the cells' start padding
  (giving it to the end padding), as `outerBordersOutsideTables` does vertically.  No renderer change, but per cell and
  inexact where border widths differ along a column or cells span.

Recommended before the probe: (a).  **Recommended after it: (b)**, which the probe shows is exact (§7.1):

- mode 15: move the table right by half its left outer border (the grid's lines then lie where Word centres its
  borders), as `outerBordersOutsideTables` moves it down by half its top one;
- modes 14, 12 and 11 (§7.1: Word draws the three alike): outdent the table by the larger of the first cell's start
  margin and half the left border (today: the margin), so its text stays at the margin + `w:tblInd`;
- each cell's start padding the larger of its cell margin and half its start border, less half that border (FOP
  adds the half back); the end padding likewise (`cellLineWidth` already does the end);
- an object aligned to its cell's edge (`placeInCell`'s "edge:") measured from the borders' inner edges;
- `clearOfCellEnd` removed: the right border is then Word's, clear of a picture ending at the grid line.

(a) would move the painted borders only, leaving the text errors of §7.1.  Either way `clearOfCellEnd` goes.

## 4. Phases

- **Phase 0 - measure.**  DONE 2026-10-16 (§2).
- **Phase 1 - 17.3.3 on .7.**  DONE 2026-10-16 (§3.1, §8): docx4j 0614d1133, the probes 92e4ce623.
- **Phase 2 - the border probe** (Word; D3; DONE 2026-10-16, §7.1 - `table-border-geometry` and `-compat14`, 12
  cases each; `-compat12` and `-compat11` read the same day, as mode 14): `table-border-geometry`, modes 15 and 14 -
  single borders 0.5, 1.5,
  3 and 6pt; double; a cell's border wider than its neighbour's (the collapsed winner on each side); nil borders;
  outer and inside; a table with `w:tblInd` and with `w:jc` right; a picture flush with each cell edge.  Read each
  border's painted extent against its grid line, and the text against the grid.
- **Phase 3 - the borders and the cell text** (D4, D5): route (b), recommended - docx4j alone (§3.2): a census of
  the corpora's bordered tables (mode 14; mode 15 with a column's start border unlike the table's left one; no cell
  margins), then the table's shift or outdent, the cells' start and end padding, `placeInCell`'s edges, and a gate
  read by border rectangles as well as text.  Or route (a): the renderer, 2.11-docx4j.8 only, a fop CR the fork
  session opens.
- **Phase 4 - tidy**: remove `clearOfCellEnd`; under route (a) also the property, the export-fo bump to .8 and a gate
  against a .8 control.

## 5. Population and risk

- Phase 1: DrawingML anchors in table cells, three corpora (real, real2, real3): 60 in 30 documents - wrapNone 33,
  square 19, tight 3, through 2, topAndBottom 3.  Rows grow only for square, tight and through.  The positioned
  holder (§3.1 item 2) moves every positioned picture down 0.045pt.
- Phases 3 and 4: every bordered table's painted borders move by up to half a border width.  The fidelity score
  compares text only, so a gate will not see them: reading them needs the border rectangles (mutool's trace) or a
  raster comparison - a harness check to add with phase 3.
- **The census for phase 3** (2026-10-16, from docx4j's own FO of the b276 gate - the cells' borders and padding as
  resolved, styles and conditional formats included; real, real2 and real3, 449 documents, 391 with tables; a
  table's own rows only; 30 separate-border tables left out).  "Text off" is the cell text's distance from Word's
  under §7.1's rule:

  | mode | documents with tables / bordered | tables / with vertical borders | cells | text off <0.3pt | 0.3 to 0.8 | >=0.8 | padding under half the border |
  |---|---|---|---|---|---|---|---|
  | 15 | 144 / 120 | 2443 / 1593 | 43090 | 1858 | 4519 | 138 | 672 (19 documents) |
  | 14 | 112 / 86 | 2161 / 1710 | 57074 | 45761 | 2916 | 39 | 652 (19) |
  | 12 | 114 / 92 | 1051 / 818 | 25121 | 13917 | 7059 | 235 | 2853 (23) |
  | 11 | 21 / 18 | 391 / 353 | 11637 | 2446 | 170 | 6649 | 6802 (5) |

  In mode 15 the text is off only beside a start border unlike the table's left one (in 55 documents at most), but
  the painted vertical borders of 1568 tables move (1427 by under 0.3pt, the 0.5pt borders; 135 by 0.3 to 0.8; 6
  more).  Below mode 15 nearly every bordered cell's text is off by half its start border - 0.24pt for the usual
  0.5pt.  Modes 12 and 11 are counted under mode 14's rule (39% of the documents with tables), which the
  `-compat12` and `-compat11` probes since confirm (§7.1).  The cell measure is unchanged (`cellLineWidth`), so the text moves sideways without rewrapping: the
  line scores should barely move, which is why the gate must read x as well.

## 6. Decisions for Jason

- **D1** Commit phase 1 for 17.3.3.  DECIDED yes (Jason, 2026-10-16).
- **D2** Ship item 3 (`clearOfCellEnd`) in 17.3.3, though phase 4 removes it.  Recommended: yes - it is what keeps
  a logo flush with its cell's edge off the border on .7.  DECIDED yes (Jason, 2026-10-16).
- **D3** Phase 2's probe now: it is the input to the fork's .8 design.  DECIDED: now (Jason, 2026-10-16).
- **D4** Route (a), an opt-in renderer property for .8, or (b), docx4j alone.  Recommended after phase 2: (b) (§3.2,
  §7.1).  DECIDED (b) (Jason, 2026-10-16), the census first.
- **D5** If (b): when.  It needs no renderer, so it could go in 17.3.3 (on .7) or wait; it moves every bordered table's
  painted vertical borders by up to half a border width and, in mode 14 and beside unequal borders, its cell text by
  up to half one (1.5pt for a 3pt border).  A census of the corpora's bordered tables and a gate come first.

## 7. Readings

### 7.1 Phase 2: where Word paints a table's borders (probes table-border-geometry and -compat14)

Word's goldens 2026-10-16 07:07; A4, 1in margins (72pt), a table of three 100pt columns, so the grid's vertical
lines are at 72, 172, 272, 372pt before any shift.  Extents in pt (Word's are fills; FOP's strokes, given as their
painted extent); text x is the first glyph's origin (Word's carry about +0.13pt in mode 15, +0.02 in mode 14 of
column jitter).  docx4j is HEAD with phase 1 on r46.

Mode 15:

| case | borders | left border: Word / docx4j | column 1-2 border: Word / docx4j | text c1, c2: Word / docx4j |
|---|---|---|---|---|
| 1 | 0.5pt | 72.02-72.50 / 71.76-72.24 | 172.15-172.63 / 171.76-172.24 | 77.78, 177.67 / 77.65, 177.65 |
| 2 | 1.5pt | 72.02-73.46 / 71.28-72.72 | 172.15-173.59 / 171.28-172.72 | 78.26, 178.15 / 78.13, 178.13 |
| 3 | 3pt | 72.02-74.90 / 70.56-73.44 | 172.15-175.03 / 170.56-173.44 | 78.98, 178.87 / 78.85, 178.85 |
| 4 | 6pt | 72.02-78.02 / 69.00-75.00 | 171.91-177.91 / 169.00-175.00 | 80.42, 180.55 / 80.41, 180.41 |
| 6 | 3pt outer, 0.5pt inside | 72.02-74.90 / 70.56-73.44 | 173.35-173.83 / 171.76-172.24 | 78.98, 178.87 / 78.85, **177.65** |
| 7 | 1.5pt outer, inside nil | 72.02-73.46 / 71.28-72.72 | - | 78.26, 178.15 / 78.13, **177.41** |
| 8 | 0.5pt, r1c1's right 3pt | 72.02-72.50 / 71.76-72.24 | 170.95-173.83 / 170.56-173.44 | 77.78, 177.67 / 77.65, **178.85** |
| 11 | 3pt, no cell margins | 72.02-74.90 / 70.56-73.44 | 172.15-175.03 / 170.56-173.44 | **75.14** / **73.44** |

Mode 14:

| case | borders | left border: Word / docx4j | column 1-2 border: Word / docx4j | text c1, c2: Word / docx4j |
|---|---|---|---|---|
| 1 | 0.5pt | 66.26-66.74 / 66.36-66.84 | 166.39-166.87 / 166.36-166.84 | 72.02, 172.15 / **72.25**, 172.25 |
| 3 | 3pt | 65.06-67.94 / 65.16-68.04 | 165.19-168.07 / 165.16-168.04 | 72.02, 172.15 / **73.45**, **173.45** |
| 4 | 6pt | 63.38-69.38 / 63.60-69.60 | 163.51-169.51 / 163.60-169.60 | 72.02, 172.15 / **75.01**, **175.01** |
| 8 | 0.5pt, r1c1's right 3pt | 66.26-66.74 / 66.36-66.84 | 165.19-168.07 / 165.16-168.04 | 72.02, 172.15 / 72.25, **173.45** |
| 11 | 3pt, no cell margins | **69.14-72.02** / 70.56-73.44 | - | 72.02 / **73.44** |

**Word's rule**, both modes: every vertical border is centred on a grid line (two widths meeting at one edge, case
8, are both centred on it; a double border is its three widths, case 5), and a cell's text is at its grid line plus
the larger of the cell margin and half the cell's start border (case 11), whatever the other borders are.  What
differs is where the grid lies: **in mode 15 the table's left border starts at the margin + `w:tblInd`**, so every
grid line is half the left border to the right of where docx4j has it; **in mode 14 the first column's text is at
the margin + `w:tblInd`**, so the grid lies the larger of the cell margin and half the left border to its left.
Horizontal borders match docx4j's within 0.2pt (`outerBordersOutsideTables`); so do `w:tblInd` (case 9) and `w:jc` right
(case 10) apart from the same shift.

So docx4j today: in mode 15 paints every vertical border half the left border's width left of Word's (the letter's
right border, §1.2), and puts the text of a column whose start border is wider or narrower than the table's left
border off Word's by half the difference (cases 6 to 8: up to 1.2pt); in mode 14 puts every cell's text half its
start border right of Word's (0.25pt at 0.5pt borders, 1.5pt at 3pt).  With no cell margins (case 11) both modes
differ.  Pictures aligned to a cell's edge (case 12) sit inside Word's borders - the right-aligned one ends at the
right border's inner edge (372.0, the border from 372.14), the left-aligned one starts at the left border's (73.5) -
where docx4j's overlap its borders by up to 0.72pt.

**Modes 12 and 11** (probes `table-border-geometry-compat12` and `-compat11`, the same document as `-compat14` with
only `compatibilityMode` changed - modes 12 and 11 carry none of Word 2010's compat settings; Word's goldens
2026-10-16 07:30): Word draws them exactly as mode 14 - every word's position (`pdftotext -bbox`) and every drawing
operation (`mutool draw -F trace`) on all twelve pages is the same in the three PDFs.  docx4j (b276's jars on r46)
does the same: its FO differs between the three modes only in an unused footnote separator and the pictures'
temporary file names, and its PDFs read the same.  So mode 14's rule above is Word's below mode 15, and phase 3
applies it to modes 14, 12 and 11 alike.

### 7.2 The letter

The letter's page 2 (Word's PDF on the share's `__tmp`, local): §1.  On phase 1 (gate-b276 jars, r46): the row 18.0
to 54.76 (stroke centres), the logo 18.52 to 54.52, "List of Acceptable Documents" 58.6 (Word 58.7); sampled at 144
and 211 dpi the top and bottom borders show in both, the right one at 211 dpi, faint at 144.  The row-height probes:
§2.

## 8. Implementation record

### Phase 1 (2026-10-16; docx4j 0614d1133, harness 92e4ce623, register Enterprise 0d983e8, registry portfolio e98e06e)

- docx4j-core `WordXmlPictureE20` (`HINT_ANCHOR_CELL_FOOT`); docx4j-export-fo `WordLayoutFixups` (`growRowToContain`,
  `paragraphTopInCell`, `clearOfCellEnd`, the positioned holder's 0pt font; the hint in `ANCHOR_HINTS`).
- Tests: `AnchorPositionedFromCellTest` +5 (`theRowGrowsToThePicturesFoot`, `aParagraphRelativePictureGrowsItsRow`,
  `distBCountsInMode15Only`, `noWrapLeavesTheRow`, `anExactRowKeepsItsHeight`), 14/14.  export-fo-tests 849, 8
  failures - all eight in `FloatingTablePositionTest` (5) and `FramePrTest` (3), and the same on HEAD with only the
  export-fo pom's 2.11-docx4j.7 (checked in a worktree): they expect .6's reserved bands and need to branch on
  `FopCapabilities` before the .7 bump is committed.
- Harness: `Doc.positionedImage` with `behindDoc`, `layoutInCell` and wrapTight stated (the old form delegates; the
  other probes generate byte for byte as before); the two probes in `Corpus`.
- Gates (the fork's r46; `~/fidelity-cr030/gate-b273-b275.sh`, `gate-b275-b276.sh`): b274, the probes on HEAD's jars
  (0 movers against b273); **b275** (item 1) against b273 (corpora) and b274 (probes): 0 line movers anywhere - both
  probes were already at line parity 1.0000; by position their maxDy 66.24 to 0.62 (mode 15) and 66.24 to 60.24
  (mode 14: case 7, §9); 14714 (real2, real-c2) median dy -0.57 to 0.11; 5936 page parity 0.1654 to 0.0904 with line
  parity unchanged - of its 204 changed lines none was within 3pt of Word's y before and 154 are after, all a page
  late from the extra page it already had (12 pages against Word's 11 in both builds, from page 1).  **b276** (items
  2 and 3) against b275: 0 line movers, events equal, and no document's median or worst dy, line or page
  parity moved by more than 0.05pt, the corpora and the probes (the holder's 0.045pt is below that).
- Register (Enterprise, uncommitted): attempts 2026-10-16, cause anchor-in-cell-from-page (findings, status).

## 9. Not in scope

- Mode 14, `layoutInCell` 0 with a wrap: the object leaves the cell and the table goes below it (case 7).  No corpus
  anchor has that shape (the corpora's `layoutInCell` 0 anchors in cells are all wrapNone).
- Text boxes in cells (not measured); an object aligned in its cell other than at the top (the cell's height is not
  known); an anchor in a cell spanning rows; the top of an anchor's paragraph below other content in its cell (a
  lower bound, §3.1).

## 10. Hand-offs

- The fork: phase 3, for 2.11-docx4j.8 only, once phase 2's rule is read.  Nothing for .7.
- The ports (core-ts, python): none; they write no PDF.
