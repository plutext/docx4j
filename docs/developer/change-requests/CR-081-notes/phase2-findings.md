# CR-081 phase 2: a picture's frame, against Word

Phase 2 draws a picture's frame in the PDF (both routes) and in HTML (both routes):
- its outline (`pic:spPr/a:prstGeom`, `a:custGeom`), which clips the picture and the fill behind it;
- the fill behind it (`a:solidFill` and the other fills in its `pic:spPr`);
- its line (`a:ln`), wholly outside it.

The effects follow the outline and the line. The oracle is Word 16.0.20326's PDFs of phase 0:
- the frame probes (`fxr71`, 12);
- the flat picture styles (11 of the 28; the other 17 are 3-D, phase 5) over the photo, the photo as a JPEG and both
  line arts (`fs10`, `fs11`, `fs20`, `fs21`).

`export_effects.py --files FRAMES` writes them to
`docx4j-chart-render/src/test/resources/effects/cr081-word-frames.jsonl.gz`: 56 probes, 56 effect pictures and 34
vector draws (a line's path, a compound line's rings, a fill behind). `PictureFramesTest` compares the renderer with it,
and `compare_effects.py` compares docx4j's PDF of the same files.

## 1. Result

- **Renderer** (`PictureFramesTest`):
  - 56 effect pictures. Mean alpha error 0.76 of 255, median 0.60, 90th percentile 1.80, worst 2.80. Each is within a
    point of Word's place (a coarse pixel of size), its resolution within 1%, its peak alpha within 0.025.
  - 34 vector draws, each within 0.02pt of Word's box (fs*-14's top: 0.18pt, a point of Word's own path, §3).
- **PDF** (`compare_effects.py` on the FO route's render):
  - the same 56 pictures, mean 0.75, median 0.56, 90th percentile 1.83;
  - the line paths, rings, fills behind and the outline's clip, each within 0.02pt of Word's.

  The pictures' places on the page are the same as before phase 2 on all 40 pages of `fs10` and `fxr71`.
- **Phase 1's oracle** (`EffectsTest`, 171 pictures): mean 0.67 (from 0.68), with the inner shadow's correction (§2).
- **Corpora**: gate b416 (b415's jars with this docx4j-core and docx4j-chart-render) against b415: 0 movers.
  - **Population.** Six documents have a picture with a line, and 15 a fill behind a picture (all opaque, so
    unchanged). None has a picture geometry other than rect.
  - **16_en-ZA_num_tbl_10976.** Its 3pt line is drawn. Its shadow, cast by the line too, is within 0.4pt of Word's
    golden on every side (3.2pt small in phase 1). The line's box is within 0.1pt.
  - **15_en-US_sdt_tbl_469.** The only events changed: a line 3pt wide (`line` 2 to 3) and page 7's body 67pt over
    (`body` 0 to 1).
    - Its third picture has a crop that moves its top edge out (`a:srcRect t="-33147"`) and a line. Through the SVG it
      is drawn in Word's frame, 757pt tall, as Word's golden has it: the bitmap at 85.3, 276.3 (439 by 558pt), the
      line's box at 73.6, 77.0 (451 by 758pt), each within 0.1pt.
    - Word's frame is taller than the page body and wider than the column too, and FOP now reports both.
    - As an external-graphic (b415) the bitmap was at the frame's top left, and the 199pt band above it was lost
      (§5).

## 2. Rules phase 2 measured

- **The outline clips the picture.** Word draws the picture inside the geometry's path (`W* n`): an ellipse
  (`fxr71-01`), a roundRect (`fs10-05`), a round2DiagRect (`fs10-14`).
- **The fill behind** is the outline filled, under the picture (`fs10-05`, `-08`). It shows through a transparent
  picture (`fs21`). Over an opaque one it changes nothing, so docx4j leaves such a picture as its bitmap alone.
- **The line is wholly outside the picture.** Word strokes it on the outline moved `w`/2 outward:
  - corners mitred, arcs offset (`fs10-14`: the round2DiagRect's 18pt corners are stroked at 21.5pt, its square
    corners square);
  - the stroke's own join is the line's (`fxr71-12`, no join: round, `1 j`; `a:miter`: `0 j`), its cap the line's
    (`cap="sq"`: `2 J`).

  A shape's line is centred on its outline (CR-080).
- **A compound line** is rings from the outline outward, `thickThin` at 3 : 1 : 1 (`fs10-07`: 18 to 7.2pt and 3.6 to
  0pt out). Under a mitred line the rings' corners are square. A shape's compound line keeps CR-080's round corners.
- **The line is part of the drawing for the effects.** It glows (`fxr71-12`) and casts the shadow (`fxr71-11`;
  `fs10-09`, `-12`, `-14`).
  - A shadow's `algn` point is on the box with the line: `fs10-08`'s `sy` 90% about the line's foot. About the
    picture's foot the shadow would be 3pt higher.
  - The line is drawn over the inner shadow, and the inner shadow is the picture's alone. Word's is the picture's
    size (`fs10-07`, `-11`).
- **Correction to phase 1: the inner shadow at each pixel's centre.** Phase 1 sampled the blurred outside at the
  pixel's edge, half a pixel out. With no `dist`, Word's corner is 1 − Φ(0.5 px / σ)² (`fx20-06`: 0.376 to 0.353,
  Word's; `fs10-07`: 0.749 to 0.714, Word's).

## 3. Departures (known, measured)

- **The reflection where `endA` is left out** (`fs*-05`, Reflected Rounded Rectangle). Word's picture ends 16 pixels
  past where the fade leaves nothing: the fade ends at 138.49pt and the picture at 144.27. Where `endA="0"` is written
  (`fxr50-08`), Word's picture is the reflection's whole height.
  - docx4j draws the whole reflection.
  - The rows between are transparent, so the difference is in bytes, not in what is drawn.
- **fs*-14's path** starts 0.18pt above the line's top: Word's first `moveto`, then a `lineto` down to it. docx4j's
  path doesn't have that point.
- **The inline cut** (phase 1 §3) applies to the effect pictures as before. The line, a vector, isn't cut: Word's
  interface writes an effect extent that covers it (phase 0 findings §3).
- **fs21-04**'s shadow of the line art's strokes peaks at 0.169 against Word's 0.149 (23pt blur). Its colour isn't
  compared: premultiplied in 8 bits at that alpha, it is a few levels.

## 4. Not in phase 2 (not measured)

- **A soft edge with a line.** docx4j softens the picture and the line together, in the picture that takes their
  place; no probe has both.
- **A reflection's `algn` with a line.** It is taken on the box with the line, as the shadow's; no flat probe has both.
- **A picture's line in a group or a canvas.** It is still drawn as CR-080 draws a shape's line, centred (phase 3).
- **A dashed picture line.** Its dashes run along the outline moved outward; where Word starts them isn't measured.
- **A sharp corner** (a star's point). The outline moved outward is mitred at Word's limit of 10; not measured on a
  picture.
- **The 17 3-D picture styles** are drawn flat (phase 5). **A turned picture** is as in phase 1 §4.
- **sx·tan kx against tan kx** (phase 1 §2) is still not told apart. The Perspective Shadow (`sx` 97%, `kx` 15°) is a
  3-D style (18), whose shadow is in its one lit picture; no flat style scales and skews at once.

## 5. Leads

- **A crop that moves an edge out, with an effect extent.** CR-029 gives such a picture's external-graphic padding
  for the moved-out share of its frame (`AbstractWordXmlPicture`). `WordXmlPictureE20.stampEffectExtent` then sets
  `padding-*` from the effect extent and replaces CR-029's.
  - The bitmap is drawn at the frame's top left, and the frame shrinks to the bitmap's part. 15_en-US_sdt_tbl_469's
    third picture lost a 199pt band above it in b415.
  - A picture with a frame or effects is drawn through the SVG, which keeps the crop's place, so phase 2 fixes that
    document. A plain picture with both still loses it.
  - The fix is to add the effect extent to CR-029's padding. It is not part of phase 2.
