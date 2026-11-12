# CR-083 phases 1 and 2: WordArt drawn, flat and warped (findings and gate)

Built 2026-11-12 (Jason: "do CR-083 phases 1 and 2"), on docx4j 3931f871a.

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

## 4. Follow Path (phase 2, fitted)

Fitted to phase 0's single and multiple lines and checked with `gate_wordart.py`; the place of the glyphs, given
Word's scale, by `fit_follow_path.py` (with `kern-aptos.txt`, Java's kerned positions of the probes' texts): every
glyph within 2.5 pt and 4 degrees of Word's, the best of a grid of h_ref and h_adv:
- the text is laid along the preset's path drawn in toward the text rectangle's centre by a factor k, its glyphs
  scaled by s; the path runs 0.15 of the scaled size above the baseline (the glyphs' tops being on its left), the
  advances are measured along the curve 0.2 above the baseline, each glyph whole, turned to the baseline at its middle,
  the line centred on the path by arc length, without its leading and trailing spaces;
- s and k by the path's length over the text's width q (`textCircle`: half its length) and the rectangle's height
  over the text's width h: k = 0.727 to q 1.234, then up through 0.745 at 1.65 and 0.8506 at 2.57, or, in a box
  flatter than its text (h below 0.343, Word's own form), 0.727 + 0.577 (0.343 - h) where that is more (Jason's long
  line, h 0.116: Word's 0.858); s = min(k, max(0.709 q, that flat term)) (an arch adjusted short: k 0.73, s 0.394, as
  Word); each further line 0.823 of s;
- further lines inward by 0.665 of the scaled size, the first moved out by 0.36 of that for each further line.

Where it misses (section 6): the flat term rests on two of Word's lines; the text's height counts in Word ("wave",
no ascender, 0.793; "Warpy", descenders, 0.695; the rule 0.80 and 0.77); the glyphs are a little closer than Word's at
a flat arc's ends, and Jason's long line bends less than Word's. Normal offsets of a very flat ellipse fold at its ends
(a radius of curvature of 2.4 pt there); the points where the offset doubles back are dropped. A round of probes of
text lengths, heights and box shapes on the three presets would settle the scale.

Rejected on the way (variants of `fit_follow_path.py`'s model): the baseline on the path itself (7 to 9 pt); the whole text placed rigidly
and moved along the normal at its middle (mean 1.7 pt, ArchDown worse); offsets made by scaling about the centre (mean
2.7 pt).

## 5. Gate

- **Phase 0's probes through docx4j's PDF**, with Word's Aptos files given to docx4j (`gate_wordart.py`: Word's
  outlines and docx4j's flattened, the largest distance either way, as drawn; the box from the probes' grey line;
  real text by its characters' origins). 412 probe pages, 365 with glyph paths in both.
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
| textButton | 7 | 0 | 0 | 4.0 | 20 (its lines Follow Path's) |
| Follow Path | 45 | 0 | 0 | 2.7 | 9.6 (72 pt) |
| the gallery's styles on a warp, Word's form | 30 | 4 | 5 | 1.3 to 6.6 | 7.2 (effects grow the box: section 3) |

The two-path misses are phase 0's eleven: textCirclePour (to 9.0), textRingInside and textRingOutside (5 to 7),
textArchUpPour and textArchDownPour (to 4.7), textStop (2.5 to 3.6), textCanUp and textCanDown (2.1 to 3.2), an
adjusted textTriangle (4.1), textWave1, textWave2 and textWave4 (0.5 to 1.2). Measuring the arcs by the ellipse's
parameter instead of arc length is worse for every one of them (`tryarc`, in the session's scratch: textCanUp 10.8,
the rings 9.8).

## 7. Open

- The 11 two-path presets phase 0 left unexplained.
- textButtonPour with two or three lines (Word's arched lines narrower than its bands), and textButton's lines.
- Follow Path's scale for long lines and for the text's height; its spacing at a flat arc's ends (a round of probes).
- Effects move the text and grow an `spAutoFit` box (section 3): phase 3's.
- A gradient fill's frame on text (a path gradient's centre differs from Word's on wa30-16; a gradient outline's on
  wa30-12).
- WordArt in a group or canvas stays text.
- Shadow, glow, reflection (phase 3) and 3-D (phase 6) are reported, not drawn.
