# CR-083 phase 0: what Word does with WordArt (findings)

Round 1 (`cr083-phase0`, 19 jobs, 467 probes and Jason's first WordArt document) and the gallery-styles round
(`cr083-phase0-styles`, 5 jobs, 60 probes and Jason's hand-made styles document) ran on the word-bridge on 2026-11-12
(07:29 to 07:36, the VM's go 196; Word 16.0.20326.20232, printer FX DocuPrint C2255). All 28 jobs, controls included, opened silent
and saved docx and PDF; the only dialog was the Compatibility Checker on `wv10-dml-mode12`, answered Continue.

Readers: `survey_wordart.py` (a line a page: text, fills, strokes, pictures; `survey-word.tsv`), `warp_model.py` (the
preset evaluator, the warp, the font's outlines, Word's page), `measure_warps.py` (`warps-word.tsv`). The font is the
Aptos subset Word embedded, whose outlines are those of both Aptos files in the VM's cloud fonts.

## 1. Text or outlines (D2)

Word keeps WordArt's text as text wherever it can, and otherwise draws the glyphs as paths with the text written again
invisibly.

| What the run has | Word's PDF |
|---|---|
| a solid `w14:textFill` (no warp) | real text, in the fill's colour (`w14:textFill` wins over `w:color`) |
| a solid fill with alpha | the glyphs as a filled path, with the alpha, and **no text at all** (WordArt and body text alike) |
| any `w14:textOutline` with a fill | the glyphs as a filled path (the text's colour) and the outline as a stroked path (or, compound or gradient, as filled paths), over invisible text |
| a gradient `w14:textFill` | the glyphs as a path filled with a shading pattern, over invisible text |
| `w14:shadow`, `w14:glow`, `w14:reflection` alone | real text, visible; the effect is a picture |
| no fill and no outline | nothing at all: no path and no invisible text |
| a warp (any preset but `textNoShape`) | the glyphs as paths, over invisible text |
| 3-D on WordArt (`a:scene3d`/`a:sp3d` in `bodyPr`, `w14:props3d`) | one picture of the whole text, 200 dpi, and **no text at all**, visible or invisible |
| 3-D on body text (`w14:props3d`) | the text visible, and a picture for each run |

- **The invisible text** is written in a tagged `Span` (with the run's language), set where the unwarped text would be
  (the line's natural layout in the text rectangle), at the run's size (Follow Path: the scaled size), clipped to its
  line, and made invisible by transparency: an `ExtGState` with `ca 0` and `CA 0`. Word doesn't use text rendering
  mode 3.
- **The glyph paths** are in an `Artifact` marked-content sequence, untagged.
- **`w14:alpha` is a transparency**, not an opacity: a solid fill with `w14:alpha w14:val="40000"` is drawn with
  `ca .6`. DrawingML's `a:alpha` (CR-081) is an opacity; the w14 colours read the other way. (MS-DOCX's "alpha"; Word's
  interface shows the gallery shadows' 60000 as 60% transparency.)
- **The outlines are the font's own**, unhinted: Word's unwarped outline of the hollow "WordArt" (wa30-03) is the
  embedded font's within 0.15 pt, a constant offset of (-0.07, -0.10) pt from the text's origin.
- **Body text** with an outline or a gradient is treated as WordArt is: paths over invisible text. Word doesn't use PDF
  text rendering modes 2 or 7 for it (D3's phase 5 assumed it might).
- A warped WordArt's page also carries a fully transparent picture the size of the text rectangle (200 dpi, alpha 0
  everywhere), the artefact CR-080 saw on turned text. Nothing to reproduce.

## 2. The box

From wa20 (fromWordArt, autofit and wrap, each way) and the warps' boxes, which the 0.25 pt grey line shows:
- **`wrap="none"`: the box is as wide as the text**, its natural width plus the left and right insets (142.6 = 128.2 +
  14.4; 128.1 with no insets), whatever the autofit and the stored width (288 pt stored, 142.6 drawn).
  `wrap="square"` keeps the stored width.
- **`a:spAutoFit`: the box is as tall as the text**, its lines plus the top and bottom insets (51.3 = 44.1 + 7.2 for one
  line of Aptos 36 pt; Aptos's ascent and descent give 43.95). `a:noAutofit` and `a:normAutofit` keep the stored
  height. Word's own form (fit-to-text, stored at 144 by 144 pt) is therefore laid out at the text's size: the stored
  extent is stale, and Word's save leaves it so.
- **Under `spAutoFit` the box grows for some effects**: gallery style 1's shadow takes it from 51.3 to 55.0 pt,
  style 7's reflection to 52.2; bold widens it with the text. (Measured on wg11; the rule is phase 1's.)
- **`fromWordArt` changes nothing** in any of the 12 pairs (0 and 1 alike). normAutofit, with text that fits, is
  noAutofit.

## 3. The warp, two paths (D5)

**ISO/IEC 29500-1 §20.1.9.19 is what Word does**, with these readings, measured on the 33 two-path presets in six
forms (Word's form and a fixed box; default and adjusted; one and two lines: 198 probes):
- the presets are evaluated on the **text rectangle** (the box Word drew, less the insets), not the box;
- the rectangle mapped is the **ink of the whole block of lines** (the glyphs' outlines: no side bearings, and spaces at
  the ends left out; "wave", with no ascender, fills the height as "Warpy" does);
- a point's horizontal fraction is measured **by arc length** along each path, and its vertical fraction **down the
  straight line** joining the two points;
- the outline is warped **densely** (as a curve), not by its control points alone.

`warps-word.tsv`: 157 of 198 within 0.5 pt of Word's outline (the worst point either way), 168 within 1.0 pt; the
median worst point is 0.06 pt in Word's form and 0.11 to 0.18 pt in the fixed box. Rejected, with their numbers in
the reading's history (each worse on most presets): the font's height for the rectangle (1.0 to 1.6 pt mean error),
sampling the paths as y(x) (docxide-pdf's reading: up to 87 pt on textCirclePour), each command's own parameter, the
box as the frame (about 2 pt), a blended path's own arc length, mapping control points only (lines kept straight, or
each segment made one cubic), and subdividing the outline before mapping.

**Word's departures from the shipped definitions:** textDeflate and textDeflateBottom take `ss` as the height, not
min(w, h): read so, textDeflateBottom in the tall box goes from 3.90 to 0.38 pt and textDeflate from 1.59 to 0.16 pt;
in Word's form `ss` is the height anyway.

**Not yet explained** (41 probes over 0.5 pt, in 11 presets): textCirclePour (5 of 6 forms, up to 8.1 pt), textRingInside
and textRingOutside (5 of 6, up to 7.3), textCanUp (4) and textCanDown (3) (up to 3.1), textStop (4, up to 3.2),
textArchDownPour (4) and textArchUpPour (2) (up to 4.3), textWave1 and textWave2 (4 each, 0.6 to 0.8), and textTriangle
adjusted (4.1). Their tops and bottoms (the glyph points at the ink's top and bottom) are on the paths; the error is in
between, and at the ends of closed and near-closed arcs. A different preset geometry doesn't explain textStop (fitting
its two guides leaves 0.93 pt mean). Phase 2 takes these up with the probes as its oracle.

## 4. Follow Path: the three one-path presets

textArchUp, textArchDown and textCircle draw **each glyph as its own path, turned to the path's direction**, and
**scale the text**:
Word's invisible text is at the scaled size. The scale depends on the box and on the text, not by the arc's length:

| text | text rectangle | lines | Word's size (from 36 pt) |
|---|---|---|---|
| "WordArt" | 128.2 x 44.1 (Word's form) | 1 | 26.18 |
| "WordArt" | 128.2 x 136.8 | 1 | 26.66 |
| "WordArt" | 273.6 x 136.8 | 1 | 30.62 |
| "WordArt" | 128.1 x 144.0 (no insets) | 1 | 27.00 |
| "wave" | 80.0 x 136.8 | 1 | 28.56 |
| "Warpy" | 98.2 x 136.8 | 1 | 25.03 |
| "WordArt", 18 pt | 64.2 x 136.8 | 1 | 15.31 (of 18) |
| "WordArt", 72 pt | 256.0 x 136.8 | 1 | 52.37 (of 72) |
| "WordArt" (textCircle) | 128.2 x 208.8 | 1 | 29.28 |
| two lines | 143.0 x 88.1 and 143.0 x 136.8 | 2 | 21.53 (both) |
| three lines | 143.0 x 136.8 | 3 | 17.93 |
| "WordArt", adjusted (+60 degrees) | 128.2 x 136.8 | 1 | 14.2 (arches), 26.65 (circle) |

The rule (and how the glyphs are spaced along the path) is phase 2's, fitted to these. office-open-xml-viewer's "Follow
Path keeps the natural width" hypothesis doesn't hold: Word shrinks the text in every probe.

## 5. The presets with more than two paths

Each line of text takes a band, by eye (wa11, wa14, wa16): textButton puts line 1 on the top arc (Follow Path), line 2
straight across the middle and line 3 on the bottom arc; textButtonPour and textDeflateInflateDeflate put a line in
each band; textDeflateInflate, with two bands and three lines, puts the first two lines in the top band. Phase 2
measures them as it does section 3.

## 6. Effects

- **Shadow, glow, reflection**: pictures (200 dpi, as CR-081's), the text itself visible. On Follow Path text, **one
  picture for each glyph** (gallery style 1 on textArchUp: seven).
- **Gallery style 15** writes `w14:shadow` with `sx="0" sy="0" algn="none"` (dir 225 degrees, blur 5 pt, black at 50%):
  Word draws it as an **inner shadow**, inside the letters. w14 has no inner-shadow element; this is how Word writes one.
- **Outline**: a stroke of the given width, cap, join and dash; compound and gradient outlines as filled paths.
- **Gradient fill**: a shading pattern over the glyphs' path.
- Their pictures' sizes and blurs are to be compared with CR-081's rules (outer shadow sigma blurRad/3, glow rad/6) in
  phase 3.

## 7. 3-D (D10)

- **On WordArt**: one opaque picture of the whole text, 200 dpi, whatever the form (Word's Bevel: `bodyPr`'s
  `a:scene3d` and `a:sp3d` with `w14:props3d` on the runs; 3-D Rotation: `bodyPr`'s camera alone; `w14:props3d` with a
  `w14:scene3d` on the runs). **The text is lost**: no invisible text, so it can't be searched or copied in Word's
  PDF.
- `a:flatTx` keeps the text flat and real.
- On Follow Path text, a `w14:props3d` bevel isn't drawn: the glyphs are flat paths in the fill's colour.
- **On body text**: a picture for each run (36 pt run: 237 by 31 pt; 14 pt run: 81 by 11 pt), and the text visible.

## 8. VML WordArt (D6)

- **Word draws VML WordArt from its shapetype's path and formulas, not from its `o:spt`.** The 40 shapetypes carrying
  only an `o:spt` (136 to 175) drew nothing, and Word's save rewrote each as `o:spt="100"` with `path=""`.
- A type-136 shapetype with its top line slanted (`path="m0,0l21600,10800m0,21600l21600,21600e"`) drew the text
  slanted, and Word kept that path.
- The whole type-136 shapetype, placed after an spt-only 136 in the same document, also drew nothing (its save:
  `o:spt="100"`, `path=""`). So Word may take a type's definition from the first shapetype of that `o:spt` it reads.
  To test alone in round 2.
  **Corrected 2026-11-13** (phase 4 findings §1.2): a probe fault, not a Word rule. That shapetype kept the id
  `_x0000_t136`, which the spt-only 136 already had, and the shape pointed at `#_x0000_t136w`, which no shapetype has;
  so wv20-41 shows only that a shape whose `type` doesn't resolve draws nothing. The question stays for round 2.
- **Word's save in Word 2007 mode drops the warp**: each wps WordArt became a plain VML text box (`o:spt="202"`,
  `v:textbox`), with no `v:textpath` and no `a:prstTxWarp`. Word writes no VML WordArt from DrawingML, so D6's
  "Word's own conversion" oracle doesn't exist.
- **Text watermarks** (type 136 in a header, as Word writes them) are drawn as filled outlines in the fill's colour,
  turned by `rotation`. **They aren't in the PDF's text**, visible or invisible.

So phase 4's approach changes: docx4j evaluates the VML shapetype's own `path`, `formulas` and `adj` (and `textpath`'s
fit attributes), as Word does, rather than mapping `o:spt` to a DrawingML preset; a shapetype with no path draws
nothing, as in Word. The `MSOSPT` table becomes unnecessary for drawing. Round 2 needs Word's own shapetypes for the
other 39 types, which no machine here has (section 10).

## 9. Word's own form, and the gallery

From Jason's two documents (`cr083-wordart-jason.docx`, `cr083-styles-hand.docx`; `styles-word.tsv`):
- A WordArt is a wps text box (`txBox="1"`), rect, no fill, no line; `bodyPr` `wrap="none"`, `fromWordArt="0"`, default
  insets, `a:spAutoFit`, `a:prstTxWarp` (a Transform included, with an empty `avLst`); stored at 144 by 144 pt.
- The `mc:Fallback` is a VML `v:textbox`: a VML reader gets the text box without its warp or effects.
- The 15 WordArt styles and the 15 Text Effects styles write the same run properties, style for style. Styles 5 and 10
  (3-D) add `a:scene3d` and `a:sp3d` to a WordArt's `bodyPr`; on body text, `w14:props3d` alone, with no camera.
- Styles 4 and 14 also set `w:outline`; 9 and 15 set `w:spacing w:val="10"`.
- Bevel's first preset is "Round" in Word's interface (`prst="circle"`).
- Word's 3-D Rotation, applied in the first document, is `bodyPr`'s camera alone (`isometricLeftDown`).

## 10. For Jason: CR changes proposed

Made in the CR on 2026-11-12, as recommended (Jason: "make the phase 0 findings section 10 proposed CR changes").

1. **D2: settled by Word.** Glyph outlines with invisible text, exactly where Word uses them (section 1's table), and
   real text elsewhere. Where Word drops the text altogether (3-D WordArt, a fill with transparency), docx4j can
   keep it invisibly (recommended: search and copy keep working, nothing visible changes). Word makes its text invisible with transparency (`ca 0`), not mode 3; the CR's `fill="none"`
   (mode 3) is equivalent for search and copy. Recommend: keep mode 3 (simpler in FOP), and say so in D2.
2. **D3, phase 5: Word doesn't use PDF text rendering modes** for body text. An outline or a gradient on a body run is
   paths over invisible text, as on WordArt; a shadow, glow or reflection is a picture under real text. The fork's
   modes 2 and 7 would look the same and keep the text real. Recommend: keep the fork route, now as a known departure
   from Word's mechanism (same appearance), and say so.
3. **D5: confirmed**, with Word's two `ss` departures and the block-of-lines rectangle written into phase 2; the 11
   unexplained presets are phase 2's to resolve against these probes.
4. **D6: rewritten.** VML is drawn from its own path and formulas (a VML formula evaluator in phase 4), not via an
   `o:spt` table, since Word does so.
5. **D10**: Word drops 3-D WordArt's text from the PDF. docx4j can keep invisible text even there (better than Word for
   search) or match Word. Recommend: keep the invisible text; it costs nothing visible.
6. **Follow Path's scale and the multi-path bands** are phase 2's, from sections 4 and 5.

## 11. Open: round 2 and the counts

- Word's own shapetypes for VML types 137 to 175: none on this machine. Two routes: the load survey's documents (the
  distinct `v:shapetype` definitions by `o:spt`, which are Microsoft's data, not documents' content, through the
  portfolio), or Jason inserting legacy WordArt by hand in a Word 2007 mode document (40 shapes). Then one VML probe for
  each, one type to a document where the merge question (section 8) matters.
- The load survey's counts (CR-083 phase 0's owed queries): in the portfolio's kit.
- Follow Path's rule, the multi-path bands and the 11 presets: measured in phase 2 against these probes, with a further
  round only if they need one.
