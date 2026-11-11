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

## 4. The second rounds (`fop-cr024-bothsides-r2`, 2026-11-11)

Asked by the fork's session for CR-024 phase B, on Jason's order there ("it is worth it. do the minimum-width round and
the table wider than either gap measurement"). Two files on fb10's geometry (`build_bothsides2.py`,
`probes-bothsides2.tsv`), one case a page, Word silent.

**The narrow segment's minimum is 18 pt** (fb20, "a a a ..." text). The band lines' text, by the gap:

| Cases | The gap | 16 pt | 17 pt | 18 pt and up |
|---|---|---|---|---|
| A, 10 pt text | left, distL 9 pt | empty | empty | used |
| B, 20 pt text | left, distL 9 pt | empty | empty | used |
| C, 10 pt text | right, distR 9 pt | empty | empty | used |
| D, 10 pt text | left, distL 0 | empty | (not probed) | used |

- An 18 pt gap takes two 10 pt words (x 72.02 to 86.03), or one 20 pt word (to 83.10).
- So the minimum doesn't follow the font size or the side, and it is the free gap's, not the object's distance from
  the margin.
- 18 pt is a quarter inch (360 twips). Every band line uses it but the first.

**A table in the band** (fb30: 12 words of anchor paragraph, then the table, then a paragraph):

| Case | Gaps (pt) | Table | Word: where | Word: x, width (pt) |
|---|---|---|---|---|
| T1 | 141.65 / 141.65 | fixed, 200 pt | below the object, its top at the object's foot (227.08) | 72.02, 200.48 |
| T2 | 141.65 / 141.65 | autofit, a 190 pt word | below the object, not shrunk | 72.02, 189.92 |
| T3 | 100 / 183.3 | fixed, 120 pt | the right gap, in the band (top 118.08) | 340.22, 120.53 |
| T4 | 183 / 100.3 | fixed, 120 pt | the left gap, in the band | 72.02, 120.53 |
| T5 | 100 / 183.3 | fixed, 80 pt | the left gap: the first, not the wider | 72.02, 80.45 |
| T6 | 141.65 / 141.65 | 100% | below the object | 72.02, 451.40 |

- Nothing is set beside a table in the band.
- The next paragraph starts below the table, in both segments where it is still in the band (T3, T5: 72.0 to 160.2
  and 340.2 to 522.0).

docx4j (b424's jars, r73):
- T1, T3, T4 and T6 are Word's.
- T2's autofit table is 199.78 pt wide against Word's 189.92.
- T5's table goes in the right gap, where Word takes the left.
- After T3 to T5, the next paragraph's band lines are one piece in one gap (phase A's).

## 5. The third round (`fop-cr024-bothsides-r3`, 2026-11-11)

The rules phase B's design review found unmeasured (fork CR-024 section 12.6), on Jason's order in the fork's session
("Measure the things Fable identified"). One file, fb40-rules (`build_bothsides3.py`, `probes-bothsides3.tsv`), 14
cases. Word silent.

The fork's E3 asked for left 36pt and hanging 24pt "(the first line at the margin)". Those put the first line at
12pt, so E3 is as written and E4 has left 36pt, hanging 36pt.

**E: the first-line indent when the first line's left segment is skipped.**
- **E1** (left gap 16pt, under the minimum; first line 24pt) and **E2** (gap 40pt, first line 24pt, a first word of
  30pt that fits the gap but not the gap less the indent): the indent goes to the right segment.
  - B's first line starts at the right gap plus 24pt: 280.20 = 256.0 + 24 (E1); 304.20 = 280.0 + 24 (E2).
  - The next line's right segment has no indent (256.18; 280.20).
- **E3** (left 36pt, hanging 24pt): a hanging first line doesn't carry. The left segment is skipped (12pt in leaves
  28pt, under the 30pt word), and the first line starts at the right gap's start, 280.20.
- **E4** (left 36pt, hanging 36pt): the first line at the margin uses the left segment (72.02). Later lines' left
  segments, 36pt in, leave 4pt, and are skipped.

**F: the 18pt minimum is the raw gap's, before indents.** Each band line sets text in the narrowed gap:
- F1, left indent 24pt (16pt free): 96.05 to 110.05;
- F2, 20pt (20pt free): 91.97 to 105.97;
- F3, first line 25pt (15pt free): 97.01 to 111.01, and the later lines from 72.02;
- F4, right indent 24pt on the right gap (16pt free): 483.55 to 497.55.

**G: a word wider than both gaps goes below the object.** Each 200pt word starts the first line below the object
(236.5, the object's foot at 226.97). The band lines between are empty, and the text after the word runs on there.
- G1: mid-paragraph.
- G2: as paragraph B's first word.
- G3: the URL-like token, not broken at its "/".

**H: shading and borders in the band.**
- **Shading is one fill a line a segment.** Each fill runs about 1.4pt past its segment's text edges: 70.6 to 215.1
  and 380.3 to 524.9 in the band, 70.6 to 524.9 above it. Nothing is under the object (H1).
- **A box's left and right borders run down the paragraph's margins**, through the band (x 69.1 and 525.8). Nothing
  goes round the object.
- **The top border spans the left segment's width only** (69.6 to 215.1), though the first line, above the band, is
  the column's width (H2, H3).
- **A bottom border in the band is split by segment**: H3's paragraph ends in the band, and its bottom border is at
  212.4, from 69.6 to 215.1 and from 380.3 to 525.8.

docx4j (b424's jars, r73):
- E1, E2 and E4's first lines are Word's. E3's hanging indent is applied to the right segment (292.0, where Word
  starts at 280.2).
- In F, the left gap is never used (phase A's one-piece lines).
- G is Word's: the word below the object.
- In H, the band's shading is one fill on the right side (381.6 to 523.3), and the box's sides run round the
  paragraph's whole block.

## 6. The fourth round (`fop-cr024-bothsides-r4`, 2026-11-11)

What Word does with a paragraph that starts with a tab or an inline picture beside a gap too narrow to use. The fork's
CR-024 section 13.9 item 7 left it unmeasured: r80 keeps the old restart for a paragraph opening with a tab or an
inline object. Jason ordered it: "measure what Word does with a paragraph that starts with a tab or an inline picture
beside a too-narrow gap".
- One file, fb50-leading (`build_bothsides4.py`, `probes-bothsides4.tsv`), 18 cases on fb10's geometry, Word's
  default tab stops (36pt).
- A narrow gap is 16pt, under the 18pt minimum, so its segment isn't used. The right segment then starts at 256.0.
- Word was silent, 4.5 s.

**Tabs at the start of the first line in a segment are measured from the margin.**
- The line starts in the segment beside the skipped gap, and its tab goes to the next stop past the segment's start,
  counted from the margin, not from the segment:
  - **K1:** default stops, the text at 288.1. The stop at 216pt (x 288), the first past 256.
  - **K2:** a left stop at 226.8pt, the text at 298.9 (72 + 226.8). This is 2823's label tab: Word's 297.72 there.
  - **K4:** two tabs, 324.1: the stops at 288, then 324.
- **A stop under the object is skipped** (K3: a left stop at 120pt, x 192, under the object). The tab goes to the
  next default stop, 288.1.
- **A right stop at the column's end** right-aligns there (K5: "page 12" 487.1 to 523.2).
- **A centre stop whose text can't centre**, too long for it, leaves the text at the segment's start (K6: 256.2).
- **With the left gap used** (K7, 40pt): the tab and the first word don't fit the left segment, so the line goes to
  the right segment, its tab to the stop at 288 (288.1). The text is then broken as usual (to 469.9).
- **With the narrow gap on the right** (K8): the line starts at the margin, its tab to 108.0, as in a full line.

**An inline picture at the start is set as a word would be.**
- It goes at the start of the first segment it fits:
  - Q1, 60pt: at 256.0, the text after it at 319.1;
  - Q2, 260pt (the segment is 267.3): at 256.0, alone on its line, the text below it at 147.4;
  - Q6, 30pt with the left gap 40pt: in the left gap, at 72.0, the text in the right segment (280.2);
  - Q7, 60pt with the left gap 40pt: in the right segment, at 280.0, the text after it from 343.1 to 478.1;
  - Q8, the narrow gap on the right: at the margin, 72.0.
- **A picture wider than every segment and no wider than the column clears the band, as a long word does.** It goes
  at the margin below the object (y 227.0, the object's foot 226.97), and the line's text runs on after it:
  - Q3, 300pt: the text from 375.0;
  - Q4, 451.3pt: the column's width, the text on the next line;
  - Q5, 300pt alone.
- **A tab then a picture** (Q9): the tab to 288, the picture at 288.0, the text after it from 351.0.

**docx4j, on the fork's r80 (HEAD's jars):** 10 of the 18 cases are Word's within 0.4pt (W1, K5, K6, K8, Q1 to Q6,
Q8). The other 8 are off:
- **A leading tab is measured from the segment, not the margin.** docx4j writes a tab as a leader of fixed width,
  from the paragraph's start against the column, and the renderer lays it from the segment's start:
  - K1 292.0 against Word's 288.1;
  - K4 328.0 against 324.1;
  - Q9's picture 292.0 against 288.0;
  - K2 482.8 against 298.9, the stop's width laid from the segment;
  - K3 376.0 against 288.1, the skipped stop still taken.
- **Two first lines aren't broken, and run past the column:**
  - K7, a tab with the left gap used: 316.0 to 558.4, against Word's 288.1 to 469.9;
  - Q7, a 60pt picture with the left gap used: 342.8 to 585.2, against Word's 343.1 to 478.1.
- **Who fixes them.** Both are the renderer's (fop CR-024): a tab's stop beside an object counted from the margin,
  and the line after a leading tab or object re-broken in its segment. docx4j would need its tab leader computed
  against the segment's start, which only the renderer knows.
