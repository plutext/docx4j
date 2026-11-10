# CR-080 phase 4: Word's wrap round a shape with no text (round cr080-phase4)

Run 2026-11-10 on Word 16.0.20326.20232 through word-bridge 22479f7 (portfolio, the VM session's go 170), all three
files silent and complete: `fw10-shapes`, `fw11-pictures`, `fw12-shape-only` (`build_wrap.py`, `probes-phase4.tsv`).
Read with `read_wrap.py`: on each page, every run of the filler words on one baseline, split at gaps wider than 20pt,
by the word that opens it, its x and its baseline; and the object's drawn box. docx4j's PDFs are the fidelity harness's
render (`Fidelity render`, Word's fonts from `~/fidelity-fonts/vm-common`) with phase 4's export-fo (gate b404's jars).

## 1. A shape with no text is wrapped as a picture: settled, exactly

Word's 21 shape pages (fw10) and their picture twins (fw11) are the same: every segment, its words, its x and its
baseline, to 0.0pt; the objects' boxes too (the turned ones within 0.01pt). In every wrap: square on both sides, one
side and largest, at the left, the right and centred; top and bottom; tight and through, a box polygon and an
ellipse's; behind the text and in front, with no wrap and square; against the page; effect extents; turned; inline;
in a table cell. So D4's rule holds as Word applies it. docx4j's shape and picture pages are the same too, 21 of 21,
to 0.0pt: phase 4's dispatch gives a shape exactly a picture's treatment.

## 2. The other four readings, from Word's pages against each other

| Question | Pages | Word |
|---|---|---|
| A shape that paints nothing keeps its space | fw12-01 against fw10-01 | the same, to 0.0pt: it keeps it |
| The effect extent is in the wrap | fw12-03 (0) against fw10-01 (r = b = 1pt); fw10-16 (3pt each side) | it is: the text beside starts at 226.18 with r = 1pt, 225.22 with none (216 + 9); with 3pt on every side at 228.1, and the wrap's foot a line lower (abcde076 beside, not below) |
| The line is in the wrap | fw12-04 (6pt line) against fw12-03 (1pt), effect extent 0 | it isn't: the same, to 0.0pt. Only the effect extent counts (fw12-05, r = b = 6pt, moves the text) |
| Tight and through wrap the polygon | fw10-11 (ellipse polygon) against fw10-10 (box polygon); fw12-09 | the polygon is followed (8 and 9 line breaks differ); the stored polygon, not the geometry (an ellipse with a box polygon wraps as a rect with one, fw12-09) |
| Tight and through take the effect extent | fw10-08, fw10-09, fw10-10 against square | they don't: 0.96pt nearer than square's, the polygon in the extent's box |
| A turned shape wraps by its turned bounds | fw10-17 (turned, r = b = 1pt) against fw10-01 (unturned) | it doesn't: the same, to 0.0pt. The turned corners run over the text; only an effect extent written for them moves it (fw10-18) |
| An ellipse or a group wraps as a rect | fw12-08, fw12-10 against fw10-01 | the same, to 0.0pt |
| Behind the text, square | fw10-13 against fw10-01 (in front, square) | the same, to 0.0pt: Word wraps it, in compatibility mode 15 |

So Word's wrap box for square and top and bottom is the extent, its effect extent added on each side, then its
distances; its alignments place that box (aligned right, the box's right edge with r = 1pt sits 1pt in from the
column's edge: fw10-02 at 522.3, docx4j's at 523.3; centred, 0.5pt left, fw10-03).

## 3. Where docx4j's picture treatment departs from Word (pictures and shapes alike)

docx4j matches Word on the inline probes (fw10-19, fw10-20: within 0.24pt, both effect extents) and with no wrap,
behind or in front (fw10-12, fw10-14: within 0.1pt). Elsewhere, the same on the shape page and the picture page:

- **The effect extent** isn't in an anchored object's wrap or its alignment (§2): the text beside 1pt nearer than
  Word's (225.0 against 226.18 on fw10-01), an aligned object 1pt or 0.5pt off.
- **The wrap's top leaves out distT.** The float starts at the object's offset, where Word's wrap starts distT (3.6pt)
  above it: the line that straddles that 3.6pt is beside the object in Word and full width in docx4j, so every line
  beside it after is one line later (fw10-01, 02, 04, 08, 09, 16, 17; fw12-01 to 05, 08, 10).
- **Top and bottom keeps the lines above.** Word sets the anchor paragraph's lines that end above the band's top
  (offset less distT) above it, and the rest below; docx4j reserves the band at the paragraph's top, so all of them go
  below it (fw10-07: the first line 135.6pt low; fw12-06, 07, the lines with no height: 27.6pt).
- **Behind the text, square, wraps** in Word (mode 15); docx4j's 17.1.0 rule takes such an object out of the flow
  (fw10-13), measured then on one corpus document. The register's ledger8 found the rule false for wrapTight in mode
  14 as well, with a probe proposed for modes 14 and 15.
- **Text on both sides** of a centred object (fw10-03, 10, 11) and around a polygon (fw10-11): FOP's float takes one
  side, the box (CR-001 §6.6, open).
- **Against the page** (fw10-15) and **in a table cell** (fw10-21): not read further here.

None of these is phase 4's: a picture anchored the same way departs the same, since 17.0.5. Each is a change to
WordLayoutFixups.anchorImage, a layout change for every anchored picture in the corpora, so each wants its own gate.
