# CR-081 phase 4: the preset shadows, against Word

Phase 4 draws `a:prstShdw`, the 20 preset shadows (D6). They are drawn on pictures and on shapes, in the PDF (both
routes) and in HTML (both routes).

Word draws them as unblurred vector fills of the drawing's outline (phase 0 findings §1), and so does docx4j:
- in a shape's drawing, `Drawing.Area`s under the shape;
- for a picture, an SVG of its own (`ShapeDrawer.PictureDrawing.getShadow`), after the effect pictures under the
  picture.

The oracle is Word 16.0.20326's PDFs of every preset, over a 144 by 108pt picture (`fxr60`) and a shape (`fxr61`),
`dist` 6pt at 45 degrees, black. `export_effects.py --files PRESETS` writes it to
`docx4j-chart-render/src/test/resources/effects/cr081-word-presets.jsonl.gz`: 40 probes, 46 fills.

## 1. Result

- **Renderer** (`PresetShadowTest`): the 46 fills, each within 0.02pt of Word's path's box, its grey Word's.
- **PDF** (the FO route's render of both files): the same 46, the worst 0.01pt from Word's, greys equal.
- **Corpora**: no document has a preset shadow. Gate: §4.

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
drawn under the first, in grey 0.4 where the shadow is black. docx4j takes it as the colour 40% of the way to white,
which is 0.4 for black.

Each fill is in the shadow's colour, its alpha the colour's.

## 3. Not measured

- **Another `dist` or `dir`.** Every probe is 6pt at 45 degrees, so the move along `dir` is read from one direction.
- **Another size.** The shear and the scales are taken as proportional to the box, as on the 144 by 108pt probes.
- **The second fill's colour under a colour other than black.**
- **A preset shadow on a non-rectangular outline, or a transparent picture.** docx4j fills the outline (with the
  line, as phase 2's shadow) moved; Word's on a rect is the box.
- **A preset shadow with other effects.** It is not in what a reflection reflects, nor what a glow surrounds.
- **A turned drawing**: as phase 1 §4.

## 4. Gate

b420 (b419's jars with this docx4j-core and docx4j-chart-render) against b419: 0 movers, events equal. No corpus
document has a preset shadow, so nothing is drawn differently.
