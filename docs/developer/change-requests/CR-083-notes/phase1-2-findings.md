# CR-083 phases 1 and 2: WordArt drawn, flat and warped (findings and gate)

Built 2026-11-12 (Jason: "do CR-083 phases 1 and 2"), on docx4j 3931f871a; committed 36e922c7b. Follow Path remade
from the Follow Path round 2026-11-13 (section 4).

## 1. What was built

**docx4j-core** (`org.docx4j.model.shapes`):
- `WordArtText`: which text boxes are WordArt, and their paragraphs and runs read with their effective properties
  (`PropertyResolver`), each run's face (theme fonts resolved), size, bold, italic, capitals, spacing, kerning, colour
  (`WordColors`), Word's line metrics for its face (`WordLineMetrics`, so a substituted font keeps Word's line), and
  its `w14:textFill` and `w14:textOutline` as DrawingML. A run's paint is D2's: `TEXT` (real text in its fill's
  colour), `PATHS` (glyph outlines over invisible text: warped, outlined, a gradient, a fill with transparency) or
  `NONE` (no fill and no outline). Field codes, deleted text, bookmarks and proofing marks are skipped; a table, a
  picture or anything else it can't lay out leaves the text box as text, reported `render.wordart.content`.
- `TextEffects`: w14's fills and outlines converted to DrawingML's elements (marshalled, renamed, read back), with
  `w14:alpha` read as the transparency phase 0 measured (`a:alpha` = 100000 less it).
- `PresetGeometries.getTextWarps()` and `ShapeGeometry.textWarp`: the 40 warp presets evaluated by CR-080's engine on
  the text rectangle, `textDeflate` and `textDeflateBottom` with `ss` read as the height (phase 0, D5).
- `ShapeDrawer.drawWordArt` (a default method) for the HTML output; `HtmlShapes` draws a WordArt as an image of the
  box Word draws (`role="img"`, its text as `aria-label`), its SVG holding the text, and no text container.

**docx4j-chart-render** (`org.docx4j.chart.render.api`, D8):
- `WordArtRenderer`: the line layout (fonts through `ChartFonts`, the files docx4j's mapping gives FOP, D4; glyphs
  outlined at 1000 pt and scaled, so that no hinting reaches them), the box Word draws, the warp, the paint, and the
  SVG: the shape, then the glyphs Word draws as outlines (filled through `Fills`, stroked through `ShapeRenderer`'s
  line, so an outline has a shape line's widths, caps, joins, dashes and compound bands), then the text as SVG
  `<text>` with each character placed, its fill the run's colour where Word keeps it as text and `none` where Word
  draws outlines (FOP writes it in text rendering mode 3: searched and copied, not seen; D2). The text box's own turn
  (`a:xfrm rot`) turns the WordArt with it.
- `TextWarp` (section 20.1.9.19 as phase 0 found Word reads it: the block's ink, arc length, the straight joining line,
  the outline warped densely, each straight piece cut to 0.5 pt before mapping) and `WordArtWarps` (which warp sets
  which lines how: two paths, bands, Follow Path).
- `ShapeSvgDrawer.drawWordArt` for the HTML output.

**docx4j-export-fo**: `XsltFOFunctions.createShapeTextBox` draws a WordArt (`FOShapes.wordArt`) into a container of
the box Word draws, placed and wrapped by its anchor at that size (the anchor's extent replaced in a copy before
`stampAnchorHints`), an inline one in an inline-container of that size; the drawing is the container's first child,
with the text as its `fox:alt-text` (for a tagged PDF: the SVG's own text isn't tagged); the visitor and the XSLT
route both leave the text box's paragraphs out of it, the text being in the drawing.

No code was ported from docxide-pdf or office-open-xml-viewer: the warp is written from section 20.1.9.19 and phase
0's measurements, Follow Path from Word's PDFs, so `legals/NOTICE` needs no entry (§10).

## 2. Which text boxes are WordArt (section 5.1, with one addition)

As section 5.1 has it: a warp (a `prstTxWarp` other than `textNoShape`), or a run Word draws as outlines. **Added in
phase 1, for Jason:** Word's own WordArt form, `prstTxWarp prst="textNoShape"` with `wrap="none"`. Word inserts every
WordArt so (`cr083-wordart-jason.docx`, the gallery), stored at 144 by 144 pt whatever its text, and sizes its box to
its text; docx4j's FO laid its text out in the stored 144 pt and wrapped it ("Hello World - plain" at 36 pt on three
lines). Most of Word's gallery styles (style 1: a shadow and a `noFill` outline) are real text in Word's PDF, so such
a WordArt is drawn as SVG text in its colour, in the box Word draws. `textNoShape` alone isn't the mark: the four
fidelity corpora have 26 text boxes with it, all `wrap="square"` (shapes with text), and those stay text.

## 3. Measured on the way

- **Word kerns WordArt** whatever `w:kern` says: phase 0's boxes are as wide as the kerned text ("WordArt" in Aptos
  36 pt: 127.92 kerned, 129.09 not; Word's box 142.55 = 127.92 + 14.4 insets + the 0.25 pt line), and Word's real
  text has "Wo" 0.90 pt closer. Text in a box with a `prstTxWarp` is kerned; a text box WordArt only by its runs'
  effects takes `w:kern`, as body text does (not measured).
- **The box takes half its line** in its insets, as other text boxes do (17.3.1): the probes' 0.25 pt line makes the
  box 0.25 pt wider and taller than text and insets.
- **Bands** (findings section 5, now from Word's PDFs): line i in band i; with more lines than bands the first band
  takes the extra ones; with fewer, the last bands stay empty (one line on `textDeflateInflateDeflate`: the top band
  only). `textButton`'s three paths are three single paths, a line following each. A band's lines are mapped across
  the **whole block's ink** (a line narrower than the widest doesn't stretch across its band) and down a range **as
  tall as the tallest band's ink, centred on their own** (a 24 pt line beside a 48 pt one fills half its band): each
  line's own ink made two lines 7 pt off Word's and three lines on two bands 26 pt; this, 0.32 to 0.85 pt. Aligned at
  the top or the foot instead: 11 and 26 pt.
- **Effects move the text**: with a glow or a shadow Word sets the text lower in its box (wa30: glow 5 pt, 2.2 pt
  lower; 18 pt, 8.2; the gallery's shadow, 1.2), the room that also grows an `spAutoFit` box (findings section 2).
  Phase 3's, with the effects.
- **Follow Path's invisible text** is each glyph's own, where the glyph is, turned (Word's stext origins follow the
  arc), not the unwarped line's as for the envelopes.

## 4. Follow Path (phase 2): the Follow Path round

Phase 2 first fitted Follow Path to phase 0's twelve points (a scale by the path's length over the text's width, a
flat-box term, lines 0.665 of the size apart: median 2.7 pt). Jason, 2026-11-13: "commit then do the Follow Path
probes". The round (`build_followpath.py`, read by `read_followpath.py`, `KernInk.java`, `check_followpath.py`): two
sets through word-bridge, both silent:
- **cr083-followpath** (prefix fp, 4 of its files run): its boxes `wrap="none"`, so Word drew each as wide as its
  text (findings section 2, phase 0's), the regime of Word's own form: text lengths, ink heights and box heights;
- **cr083-followpath2** (prefix wp, 10 files, 297 probes): `wrap="square"` boxes, no insets: the text's length, its
  ink, a grid of box widths 144 to 432 pt by heights 36 to 288 pt, each preset's adjustment swept, font sizes 9 to
  96 pt, one to four lines (unequal lengths, unequal sizes, an empty line), Word's own form, alignment, letter
  spacing, bold, Aptos Narrow and Display (Georgia Pro: Word put Calibri, not on the VM), insets, the vertical anchor,
  textButton. Each probe's parameters are read from its own docx, so the two sets read alike.

What Word does (435 probes; each glyph's place, size and turn from its invisible text, which sits on its outline):
- **The scale is one formula: k = D / (D + 2T)**, D the text rectangle's larger side, T the lines' ink heights added
  up, unscaled (the glyph outlines' extent, not the font's ascent and descent: "_____" 0.977, "-----" 0.965,
  "wave" 0.802, "WordArt" 0.750, "Warpy" 0.706 in the same box). The path is scaled by k about the rectangle's centre,
  and the glyphs too: within 0.003 on 408 probes, every preset alike, phase 0's twelve as well (26.18, 30.62, 15.31
  of 18, 52.37 of 72, 21.53 and 17.93 for two and three lines, 29.28 on textCircle). As if the path, grown by the
  text's ink on each side, were scaled to fit the rectangle's larger side. It replaces the fitted q and flat-box terms
  and the 0.823 a further line. The font size, the box's height alone, the anchor and letter spacing don't enter.
- **Less where the text is longer than its curve**: on a default arch, the scaled text's width is the scaled half
  ellipse's length (1.003, 1.000); on an arch adjusted short, its sweep on a circle of the scaled half-width (a 60
  degree arch: 129 pt where Word took 125.4, 49.6 where it took 50.4); an arch adjusted longer, or "WordArt" on a 60
  degree arch, isn't shrunk at all. A circle adjusted short is shrunk by a rule not found (3 probes: 0.55, 0.76 and
  0.54 of their arcs).
- **The text follows the whole ellipse**, centred on the arc's middle: an adjusted arch places uncapped text exactly
  as the default does (textArchUp at 180, 210 and 240 degrees: the same glyphs). Left-aligned text starts at the arc's
  start, right-aligned ends at its end; textCircle's default arc starts on the left, so its centred text is on the
  right.
- **Each glyph's middle is at its advance along the scaled path itself** (arc length; pair spacing within about
  0.2 pt where the curve is tight), its baseline 0.14 to 0.15 of its size inside the path along the normal there
  (Aptos, Narrow, Display and bold alike; Calibri 0.158), the glyph turned to the path.
- **Lines** stand apart by their inks (a line's ink bottom on the next line's ink top), the innermost line on the path
  (the last on textArchUp and textCircle, the first on textArchDown: the one on the side of the arc's centre), the
  others moved as a whole along the normal at the text's middle (two equal lines' glyphs share their x, as on a
  rigid copy). Lines of unequal sizes are about 1.5 pt from this.
- **textButton**'s lines each follow their own path by the same rule, T one line's ink (0.857 for one to three lines).

Not found: Word measured 141 of the 320 one-line probes' advances as if along a curve about 0.06 em outside the path
(their spacing about 2% closer, the text a little back along it), and the outer line of two (textArchDown's second,
a longer first line over "Wo") the same way. Neither the box, the text, the preset nor the scale tells these apart
(textArchUp and textArchDown differ in one box; 144 by 144 pt does it, 144 by 72 doesn't): the model follows the
other 179 (measuring at 0.06 em for all was worse: median 3.7 pt).

Rejected on the way: a rigid offset of the innermost line too (median 4.0 pt); measuring at 0.03 or 0.06 em
(2.5, 3.7); the fitted model's mapping by arc fraction between offset curves, which is what drifted in tight curves
(the old model's 7 pt). `fit_follow_path.py` and `kern-aptos.txt` are the first fit's, superseded.

## 5. Gate

- **Phase 0's probes through docx4j's PDF**, with Word's Aptos files given to docx4j (`gate_wordart.py`: Word's
  outlines and docx4j's flattened, the largest distance either way, as drawn; the box from the probes' grey line;
  real text by its characters' origins). 412 probe pages, 365 with glyph paths in both.
- **The Follow Path round** (section 4): docx4j's glyphs against Word's on its 435 probes, through docx4j's PDF with
  Word's fonts: places median 1.11 pt (7.07 with the first fit), 266 within 2 pt (38); outlines (`gate_wordart.py
  --probes`) median 1.25 pt (7.73), 260 within 2 pt (23). Phase 0's 64 Follow Path and textButton pages: median 1.36 pt
  (2.85), 48 within 2 pt (21). b467 against b464: 0 movers, events equal, the PDFs the same but for date fields.
- **The fidelity corpora**: b465, then b466 on the final code, each against b464 (b464's classpath with this
  docx4j-core, docx4j-chart-render and docx4j-export-fo as classes): 0 movers, events equal; the corpora's PDFs the
  same but for the two time fields (5361, 11875); the probes' 324 the same; `render.wordart` logged 0 times. No corpus
  text box is WordArt by the rule.
- **Tests**: chart-render's `WordArtRendererTest` (14: which text boxes, runs and fields, w14's alpha, the outline as a
  line, the 40 presets by their paths, textDeflate's ss, Word's form sized to its text, a warp's outlines over
  invisible text, no fill and no outline, bands, Follow Path's scale and its text, HTML by both routes);
  export-fo-tests' `WordArtFoTest` (3: the box Word draws and no text container by both routes, an ordinary text box
  unchanged, the PDF's invisible text in mode 3 and found by extraction). core-tests 2,653, chart-render 75,
  export-fo 111, export-fo-tests 966 pass.

## 6. Numbers (`gate-all.tsv` of the final render)

| probes | n | within 0.5 pt | within 1 pt | median | worst |
|---|---|---|---|---|---|
| the box (all but the gallery's, whose effects grow it) | 351 | | | | 0.05 pt (Word's form and fixed alike) |
| real text, flat (characters' origins) | 10 | | | | 0.20 pt but under an effect (8.2: section 3) |
| flat outlines, gradients, transparency | 15 | 12 | 12 | 0.26 | 2.8 (wa30-25: a gradient outline over a gradient) |
| two paths | 239 | 180 | 207 | 0.28 | 9.0 (the 11 presets) |
| textDeflateInflate, textDeflateInflateDeflate | 15 | 12 | 15 | 0.30 to 0.37 | 0.83 |
| textButtonPour | 7 | 3 | 3 | 26 | 39 (one line 0.18; two and three lines not Word's) |
| textButton (the first fit) | 7 | 0 | 0 | 4.0 | 20 (its lines Follow Path's; with the round's rule wp80's 6 within 0.8 pt, phase 0's adjusted one 21.8) |
| Follow Path (the first fit) | 45 | 0 | 0 | 2.7 | 9.6 (72 pt) |
| Follow Path, the round's rule (glyph places, the round's 435) | 426 | | 266 within 2 | 1.11 | 74.7 (a circle adjusted short) |
| the gallery's styles on a warp, Word's form | 30 | 4 | 5 | 1.3 to 6.6 | 7.2 (effects grow the box: section 3) |

The two-path misses are phase 0's eleven: textCirclePour (to 9.0), textRingInside and textRingOutside (5 to 7),
textArchUpPour and textArchDownPour (to 4.7), textStop (2.5 to 3.6), textCanUp and textCanDown (2.1 to 3.2), an
adjusted textTriangle (4.1), textWave1, textWave2 and textWave4 (0.5 to 1.2). Measuring the arcs by the ellipse's
parameter instead of arc length is worse for every one of them (`tryarc`, in the session's scratch: textCanUp 10.8,
the rings 9.8).

## 7. Open

- The 11 two-path presets phase 0 left unexplained.
- textButtonPour with two or three lines (Word's arched lines narrower than its bands); textButton adjusted short.
- Follow Path: the 141 probes Word measures about 0.06 em outside the path (section 4: the trigger not found); a
  circle adjusted short (its cap); lines of unequal sizes (about 1.5 pt); the baseline's 0.145 em for other fonts.
- Effects move the text and grow an `spAutoFit` box (section 3): phase 3's.
- A gradient fill's frame on text (a path gradient's centre differs from Word's on wa30-16; a gradient outline's on
  wa30-12).
- WordArt in a group or canvas stays text.
- Shadow, glow, reflection (phase 3) and 3-D (phase 6) are reported, not drawn.
