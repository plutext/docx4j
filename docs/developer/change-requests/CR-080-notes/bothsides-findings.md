# Text on both sides of an object: Word's rules, measured (fop-cr024-bothsides)

The fork's CR-024 §12.3 asked eight questions about text on both sides of an anchored object (Enterprise CR-001 §6.6
item 10). `build_bothsides.py` built 22 cases, one to a page (`probes-bothsides.tsv`). Word 16.0.20326 opened the file
silently and saved it as PDF on 2026-11-11 (bridge set `fop-cr024-bothsides`, file `fb10-bothsides`); it ran on Jason's
word. The PDF was read with mutool: each visual line's words by baseline, split into segments where the gap between
words is over 30pt, and the object's fill from the content stream.

The geometry, unless a case says otherwise:
- A4 with 1in margins (column x 72 to 523.3), Arial 10pt single (11.5pt lines);
- the object is 150 by 120pt at x 222.65 to 372.65, y 106.97 to 226.97 from the page's top, square both sides, distL
  and distR 9pt;
- the left gap is x 72 to 213.65 and the right gap x 381.65 to 523.3, 141.65pt each.

A glyph's box starts about 0.05pt right of its origin, so 72.02 is the margin.

## 1. The answers

1. **A paragraph ending in the left segment** (1a: a third in; 1b: filling it): the next paragraph starts on the
   next visual line, at its left segment. The right segment of the line where the paragraph ended stays empty. The
   one-word paragraphs of 5a, 5c and 5d do the same.
2. **A line break, `w:br`, goes to the next segment, not the next line.**
   - 2a: the break after abcde028, the first word of a band line's left gap: abcdefgh029 starts that line's right
     segment (x 381.74).
   - 2b: the break in the right gap: the next word starts the next line's left segment.
3. **Indents.**
   - 3a (left 36, right 36, first line 24pt): the first line's left segment starts at 132.05, the margin plus 36 plus 24.
     Later left segments start at 108.05. The left segments run to the gap's end: 212.56 is reached, past 177.65.
   - The right segment starts at the gap's start (381.74), with no indent. It ends at the right indent: 486.25, where
     ab027 would have fitted without it.
   - 3b (left 36, hanging 36): the first line's left segment starts at the margin (72.02), later ones at 108.05, and
     right segments at 381.74.
   - So the left and first-line indents apply to the left segment only, and the right indent to the right segment
     only.
4. **Tab stops are measured from the margin, as on any line, and run across the object.**
   - Stops at 72pt and 330pt (x 144 and 402): a tab from the left gap reaches the stop at 402, in the right gap. The
     text moves to the right segment at that stop (ab018 at 402.17), not to the segment's start.
   - The same holds when the text has passed the 72pt stop (ab030 at 402.17).
   - Default stops (every 36pt from the margin): a tab whose next stop is past the left gap (216) goes to the first
     default stop in the right gap, 396 (ab033 at 396.17, abcde037 at 396.17). The stops under the object are skipped.
   - A tab whose text doesn't fit the right segment goes to the next line with the tab: the line starts at the first
     default stop, 108.05.
5. **Justification is per segment; the paragraph's last line is its last segment.**
   - 5b (justified): the visual line holding the paragraph's last word has its left segment stretched to the gap's
     end (213.52). The right segment, the paragraph's last, is set at its natural width (516.74).
   - 5c (centred, one word): centred in the left segment: x 120.77, the gap's centre giving 120.86.
   - 5d (right-aligned, one word): ends at the left gap's end (169.75, against 169.72 computed).
6. **The narrow segment: there is a minimum width between 15 and 30pt.** "Any room at all" holds above it.
   - 15pt left gap (6a, 6b): nothing is set there, even "a" (5.56pt), "to" (8.34) and "the" (13.9), which fit. Every
     band line starts at the right gap (255.22).
   - 30pt (6c, 6d) and 60pt (6e, 6f): the gap is used for any word that fits ("the a", 22.24pt; ab006, 27.81pt).
   - A word that doesn't fit skips the segment: 6d's abcde010 (43.93pt) goes to the right segment of the same
     line, and the next line's left segment takes ab015.
7. **A table after the anchor paragraph goes in the band, in the left gap, beside the object** (7a: auto, 118pt
   wide; 7b: fixed 100pt).
   - Its top is at 118.08, under the anchor paragraph's last line. It starts at the margin.
   - Nothing is set in the right gap beside the table. The next paragraph starts below the table, in both segments.
8. **A list label sits in the left segment.**
   - 8 (left 36, hanging 18): the label "1." is at 90.05 and its text from 108.05. Later lines' left segments start
     at 108.05.
   - The right segments start at the gap's start (381.74), as 3b.

Two cases outside the eight questions:
- **9, the object positioned on the page at 1a's place**: every line is as 1a's. How the object is anchored doesn't
  change the segments.
- **10, a right-to-left paragraph** (`w:bidi`, Hebrew; `w:jc="left"`, which Word sets at the start, the right):
  - each band line fills its right segment first, then its left (line 2 holds 012 to 014 in the right gap, 015 to 017
    in the left);
  - each segment is aligned at its own start: right edges at 523.53 and 213.81, ragged left.

## 2. Not measured

- **The narrow segment's minimum**: it is between 15 and 30pt. Whether it is a fixed width or depends on the font
  size needs a further round (gaps 16 to 29pt; 10 and 20pt text).
- **5a, 5c and 5d's paragraph B is one word.** The aim's window (a third of the gap, give or take) admitted a single
  word.
  - Justification of a non-last segment is seen in 5b only.
  - Centring and right alignment are seen on a line with one segment used only.
- **A table wider than a gap.** The gaps are equal, so it is not known whether Word takes the left gap or the wider
  one.

## 3. docx4j's render (b412, the same with b420's jars)

- **1a to 2b don't draw the object.** The anchor paragraph's `fo:float` is set into the flow and never drawn, and the
  lines run the column's full width.
- **3a onwards draw it.** Each band line is one piece on the right of the object (the `fo:float` is on the left), so
  the left gap stays empty.
- 3a's first line therefore starts at 441.65, the right gap plus the left and first-line indents.
- 6a (a 15pt left gap) matches Word, by that.
