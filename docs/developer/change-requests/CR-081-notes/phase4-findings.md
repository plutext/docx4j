# CR-081 phase 4: the preset shadows, against Word

Phase 4 draws `a:prstShdw`, the 20 preset shadows (D6). They are drawn on pictures and on shapes, in the PDF (both
routes) and in HTML (both routes).

Word draws them as unblurred vector fills of the drawing's outline (phase 0 findings §1), and so does docx4j:
- in a shape's drawing, `Drawing.Area`s under the shape;
- for a picture, an SVG of its own (`ShapeDrawer.PictureDrawing.getShadow`);
- for a transparent picture, pictures of its strokes, as Word draws them (§3).

Each is drawn after the reflection and before the outer shadow and glow, as Word draws it.

The oracle is Word 16.0.20326's PDFs of every preset, over a 144 by 108pt picture (`fxr60`) and a shape (`fxr61`),
`dist` 6pt at 45 degrees, black, and the round after phase 4 (§3). `export_effects.py --files PRESETS` writes it to
`docx4j-chart-render/src/test/resources/effects/cr081-word-presets.jsonl.gz`: 89 probes.

## 1. Result

- **Renderer** (`PresetShadowTest`): phase 0's 46 fills, each within 0.02pt of Word's path's box, its grey Word's;
  with the round's (§3), 92 vector fills within 0.05pt, 5 with alpha, 2 pictures.
- **PDF** (the FO route's render of phase 0's two files): the same 46, the worst 0.01pt from Word's, greys equal.
- **Corpora**: no document has a preset shadow. Gate: §6.

## 2. The presets, measured

Each preset is the outline moved `dist` along `dir` (clockwise from +x). Some are first scaled or sheared about a
point of the box. Word draws the presets named for another direction (shdw1 is "top left") along `dir` as well. A
picture's and a shape's are the same.

| Preset | Before the move |
|---|---|
| shdw1, 2, 5, 6, 14 | none |
| shdw3, 11 | half its height up from its foot, leaning left: its top edge tan 60° of that half across (93.53pt on 108) |
| shdw4, 12 | the same, leaning right |
| shdw7, 15 | half its height mirrored down from its foot, leaning left |
| shdw8, 16 | the same, leaning right |
| shdw9 | 75% about its top left |
| shdw10 | 125% about its bottom right |
| shdw19 | half its height up from its foot |
| shdw20 | mirrored down from its foot |

shdw13 adds a second fill moved twice as far; shdw17 and 18 add one moved as far the other way. The second fill is
drawn under the first, each channel of the shadow's colour plus 0.4, to white at most:
- black gives 0.4 grey;
- red gives FF6666;
- accent1 (156082) gives 7BC6E8;
- blue gives 6464FF.

Each fill is in the shadow's colour, its alpha the colour's.

## 3. The round after phase 4 (`cr081-phase4-r1`, 2026-11-11)

49 probes in 5 files, each file twice, Word silent on every job, the two copies alike on every page. Read with
`export_effects.py --files PRESETS` (89 probes in all, with phase 0's) and compared by `PresetShadowTest`:
- 92 vector fills, each within 0.05pt of Word's, their colours Word's;
- 5 fills with alpha against Word's pictures of them;
- 2 pictures of a transparent picture's strokes.

- **Other dists and dirs** (`fxp1`: 12pt at 135 degrees, 3pt at 270, 6pt at 0, none): every preset is the move along
  `dir` after its transform, as phase 0's reading had it.
- **Colours** (`fxp2`): the second fill is the colour plus 0.4 in each channel (§2). phase 0's black alone had
  fitted "40% to white" as well; the round corrected it.
  - Where the colour has alpha, Word draws each fill as a picture of a uniform colour at that alpha (0.502 for 50%).
  - docx4j draws a vector fill of the same colour and alpha: the same paint.
- **Outlines** (`fxp3`). The preset is the drawing's own outline, transformed, its control points too:
  - an ellipse frame's ellipse, a star's star;
  - an unfilled shape's line, a ring, transformed with its stroke (its vertical width halved by shdw3);
  - the transform's point is on the box with the line, as phase 2's.

  A transparent picture's is a picture of its strokes, at 200 dpi, unblurred. docx4j now does the same (§4).
- **With other effects** (`fxp4`):
  - Word draws the preset after the reflection and before the outer shadow and glow, and docx4j now does the same.
  - The reflection reflects the preset: fxp4-01's is 114.24pt tall, 4.5pt more than without it.
  - A glow and an outer shadow don't follow the preset; they are as without it.
  - With a soft edge the preset is the picture's box, as without one.
  - With an inner shadow Word draws the preset as a picture of the box: the same paint as the vector.
  - With a picture's 6pt line Word draws the box and the line's ring as two fills; docx4j draws their union, the
    same paint.
- **Sizes** (`fxp5`: 72 by 144pt, 288 by 72pt): the lean is tan 60 degrees of half the box's height, and the scales
  are about the box, as on 144 by 108pt.

## 4. What the round changed

- The second fill's colour: plus 0.4 in each channel, where docx4j had taken 40% of the way to white. They agree on
  black, and differ on every other colour.
- A transparent picture's preset is a picture of its strokes (`Effects.presetShadowPictures`), where docx4j had
  filled the box.
- The order: the preset after the reflection, before the outer shadow and glow, where docx4j had put it after them.
- The reflection reflects the preset (`Effects.draw`'s preset painter).

## 5. Not measured

- **A turned drawing**: as phase 1 §4.
- **A shape filled with a transparent picture**: its preset is the shape's outline; Word's is not probed.

## 6. Gate

b420 (b419's jars with this docx4j-core and docx4j-chart-render) against b419: 0 movers, events equal. No corpus
document has a preset shadow, so nothing is drawn differently. After the round, b421 (b420's jars with the round's
docx4j-core and docx4j-chart-render) against b420: the same.
