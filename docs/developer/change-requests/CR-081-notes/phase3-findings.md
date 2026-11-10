# CR-081 phase 3: a shape's effects, against Word

Phase 3 draws a DrawingML shape's effects (`wps:wsp`) in the PDF (both routes) and in HTML (both routes): outer and
inner shadow, glow, soft edge and reflection. They come from the shape's own `a:effectLst`, else from its style's
`a:effectRef` through the theme. A shape in a group or a canvas has its own effects drawn round it.

The oracle is Word 16.0.20326's PDFs of phase 0's shape files:
- `fxr80`: the effects on a rect, an ellipse, a star, an arrow, an unfilled shape and a shape with text;
- `fxr81` to `fxr83`: `a:effectRef` under the 2013, 2023 and a custom theme.

`export_effects.py --files SHAPES` writes them to
`docx4j-chart-render/src/test/resources/effects/cr081-word-shapes.jsonl.gz`. The four probe files themselves are test
resources beside it (synthetic, 4 to 5 KB each), so that `ShapeEffectsTest` draws each shape from its own document,
its theme resolving the references. `compare_effects.py` compares docx4j's PDF of the same files: a shape's drawing
is its vector fill or line of the frame's size.

## 1. Result

- **Renderer** (`ShapeEffectsTest`): 14 effect pictures. Each is within a point of Word's place, its resolution within
  1%, its peak alpha within 0.025.
- **PDF** (`compare_effects.py` on the FO route's render): the same 14, mean alpha error 0.44 of 255, median 0.23,
  worst 1.15.
- **Corpora**: gate b418 (b417's jars with this docx4j-chart-render) against b417: 0 movers, events equal. b419,
  with the final jar (§3's gap codes refined), against b417: the same.
  - **Population.** One document has a shape with an effect: 14_en-US_num_tbl_14372 (real2), two flowChartPunchedTape
    shapes whose `a:effectRef idx="1"` is its theme's outer shadow.
  - Both shadows are now drawn, each within 0.5pt of Word's golden relative to its shape. Page 7's shape itself is
    12.4pt higher than Word's, before phase 3 as after.

## 2. Rules phase 3 measured

- **A shape's effects follow its silhouette**, as a picture's do: its fill and line as painted (`fxr80-02` to `-04`:
  an ellipse, a star, an arrow). An unfilled shape's shadow is its line's, hollow (`fxr80-09`).
- **Where the effects come from.**
  - The spPr's `a:effectLst` where it states one. An empty one is none, over the style's (`fxr81-05`, `fxr82-05`).
  - Else the style's `a:effectRef`: the theme's effect style `idx`, from 1 (0 is none), its `phClr` the reference's
    colour.
  - In the 2013 and 2023 themes styles 1 and 2 are empty and 3 is a shadow (`fxr81-04`, `fxr82-04`). A custom theme's
    glow at 2 is drawn in the reference's accent (`fxr83-02`), and its reflection at 3 (`fxr83-03`).
- **Order.** The reflection, outer shadow and glow under the shape; the inner shadow over its fill (`fxr80-06`); the
  soft edge as one picture in the shape's place (`fxr80-07`). A shape's line is drawn over its inner shadow, as a
  picture's is: no shape probe has both, so that is phase 2's rule carried over.
- **A shape's text casts the shape's effects** (`fxr80-10`, `-11`). Word draws its text's shadow as a picture of its
  own, under the shape's fill, and the text over the fill as a picture.
  - docx4j's text is the output's (FO, or HTML), not the drawing's, so it casts nothing.
  - Under an opaque fill the text's shadow is hidden, so the difference shows only on a shape with no fill (or a
    transparent one), and for a reflection, which would show the text.
  - Those are reported: `render.effect.shape_text` (D9).

## 3. Gaps reported (D9)

- `render.effect.shape_text`: a shape with text whose effects Word casts from its text where they'd show (§2).
- `render.effect.flat_3d`: a 3-D drawing drawn flat until phase 5. That is a shape's `a:sp3d` bevel, extrusion or
  contour, a camera off the front, or its effect style's. It is also reported for a picture now, so the 17 3-D
  picture styles report it. In the corpora: 15_en-AU_sdt_num_tbl_4994's picture, a bevelled style.
- `render.effect.effect_dag`: an effect list given as `a:effectDag` (§9 of the CR).
- `render.effect.group`: a group's own `a:effectLst`, not drawn. Its shapes' effects are.

## 4. Not in phase 3 (not measured)

- **The text's effects** (§2): docx4j would have to draw the text itself into the drawing.
  - In HTML a CSS `text-shadow` on the text's container could draw the shadow.
  - In the PDF it would need the FO renderer fork.
- **A group's own effects**, reported (§3).
- **A shape's inner shadow under its line** (§2): carried over from pictures.
- **A turned shape**: its effects are cast unturned (`rotWithShape` as in phase 1 §4).
- **An inline shape's effects** aren't cut at its paragraph, as Word cuts an inline drawing's (phase 0 findings §3). A
  picture alone in its paragraph is cut at its effect extent (phase 1); a shape isn't yet.
