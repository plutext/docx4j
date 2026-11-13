# CR-083 phase 5: WordArt effects on body text (design measurements, then findings)

Phase 5 draws a body run's Word 2010 text effects: `w14:textFill`, `w14:textOutline`, `w14:shadow`, `w14:glow` and
`w14:reflection`, in body paragraphs, table cells, headers and footers, and in text boxes with no warp (phase 3's D12).
3-D (`w14:props3d`, `w14:scene3d`) is phase 6's. The design is in the CR (phase 5, D3, D20 to D24). This note holds
what is known before the round (section 1), what the FOP fork offers (section 2), the round (section 3), and the
design's review (section 4).

## 1. What is known before the round (2026-11-13)

From phase 0's body-text probes (wb50-body-effects, 25 probes; wg20-styles-body, Word's 15 gallery styles on body
text), phase 3's room probes (wb70-body-room, 16) and the phase 0 and phase 3 findings, read again for this design.

### 1.1 What Word's PDF has (phase 0 findings section 1, wb50 read again)

| The run has | Word's PDF | Today in docx4j |
|---|---|---|
| a solid `w14:textFill` | real text in the fill's colour (`w14:textFill` wins over `w:color`; wb50-01) | `w:color` |
| a solid fill with `w14:alpha` | the glyphs as a filled path at the alpha, **no text** (wb50-02: `alpha=".6"` for 40000) | `w:color`, opaque |
| `w14:textOutline` with a fill | the glyphs as a filled path and the outline as a stroked path, over invisible text (wb50-05; compound or gradient outlines as filled paths) | the text unoutlined |
| `w14:textOutline` with `w14:noFill` | the outline as a stroked path over invisible text (wb50-03, hollow) | the text filled |
| no fill and no outline | nothing visible (wb50-04; the text still there for search) | the text filled |
| a gradient `w14:textFill` | the glyphs as a path filled with a shading, over invisible text | `w:color` |
| `w14:shadow`, `w14:glow`, `w14:reflection` | real text; the effect a picture under it (wb50-17, -21: two pictures a page, one for each run with the effect) | nothing |

The invisible text is the run's own, drawn with alpha 0 (wb50-03, -05, -13: two `fill_text` at alpha 0 a page, one
for each run with the effect), as phase 0 found. **The drawing order** (wb50-25, wg20-04, -09; the review, section 4):
for each run, its pictures, then its filled glyph path, then its stroked path, then its text at alpha 0; the second
run's pictures come after the first run's text. docx4j keeps the text real and visible where Word draws paths over
invisible text, by PDF's text rendering modes (D3: the same appearance, a known departure from Word's mechanism).

### 1.2 The frames

- **A gradient's frame is the run's own box**: across, its advance (wb50-13, linear 0 degrees: a run alone on its line
  72.0 to 312.2, its text 72.02 to 312.34; a run mid-paragraph 250.6 to 331.2); down, its font's ascent to descent,
  not its ink (wb50-14, linear 90 degrees: 150.0 to 193.9 for the 36 pt run, the line's text box 150.06 to 194.05; 218.2
  to 235.0 for the 13.92 pt run). One gradient a run (one pattern a run on each page). The box down is on Word's 0.24
  pt grid (43.9 pt at 36 pt, 1.219 em; 16.8 at 13.92 pt, 1.207 em), so docx4j takes the fork's span box (its pitch
  split by the ascent share, the same metric) rather than computing its own. One face and two sizes: the round adds
  more.
- **Word blends a two-stop DrawingML gradient as it blends a VML one, and any other evenly** (phase 4 findings §10;
  the review, finding 1). Two stops at 0 and 100% (wb50-13, -14, -16): a sampled function (256 samples), in gamma-2.2
  light along phase 4's S-curve, red to blue (238, 0, 106) a quarter of the way and (186, 0, 187) in the middle, where
  an even blend gives (191, 0, 64) and (128, 0, 128). Three stops (wb50-15, 0, 50 and 100%): a stitched function of
  linear pieces, even in sRGB ((255, 128, 0) a quarter of the way, (128, 128, 128) three quarters). Two stops inside
  the run (wg20-06, gallery style 6, at 21% and 88%): even between them ((132, 135, 140) in the middle), held at the
  end colours outside; wg20-07 (0, 50, 100%) and wg20-08 (0, 4, 87%) likewise. So the curve is the two-colour case
  only, as VML's is by construction. Phases 1 to 3 draw WordArt's DrawingML gradients evenly: right for the forms with
  more stops, wrong for two stops at the ends. A shape's own gradient (CR-080, CR-081) is the round's to check, in the
  three forms.
- **Shadow, glow and reflection**: phase 3's rules, measured on WordArt and body text alike (phase 3 findings §4): the
  frame is the run's line box, the lengths drawn at (size / 72)^0.7 of their stated lengths for flat text, a
  reflection mirrored about the baseline. `Effects.drawText` draws them for WordArt today.

### 1.3 The room (phase 3 findings §2, §4; D13)

Word gives a body run's shadow, glow or reflection room in its line, as in WordArt: the line moves down by the room
above, and the next line or paragraph by the room above and below (wb50-22: a glow of 18 pt sets its line 8.16 pt
lower, the next paragraph 20.88, its baselines 183.89 to 192.05 and 231.19 to 252.07; wb50-24: a reflection whose
`dist` is 4 pt, its own line 0.24 pt higher, one grid step, and the next paragraph 21.36 lower). Phase 3's rule
(`TextEffects.reach`) fits wb70's body text as it fits WordArt (a reflection with a `dist` of 4 pt pushing the next
paragraph 16.08 pt where WordArt's box grows 16.05): each run's own reach, the line taking the most. Unmeasured: the room under exact
and atLeast line spacing, a superscript's, an empty paragraph's mark, a run of spaces alone, small body sizes (wb70 is
36 pt; the reach was fitted at 12 to 96 pt on WordArt).

### 1.4 How common

12 of the four corpora's 557 documents have a `w14` text effect in a body part or a style (outlines in 8, shadows in
5, fills in 4; none with a glow or reflection): the gate's movers are to be read against them. Phase 0's survey of
Word's gallery (wg20) gives the forms people get from Word's interface: an outline with a solid fill, a gradient fill,
a shadow, a glow, a reflection, and the combinations of styles 1 to 15.

## 2. What the FOP fork offers (2026-11-14)

Read in `../xmlgraphics-fop-plutext` at 2.12-docx4j.2 (e06e12a37).

- **The `docx4j:` namespace** (`http://docx4j.org/fop/word-layout`, `WordLayoutElementMapping`) is registered, so its
  attributes on `fo:block`, `fo:root` and the inline FOs are accepted and kept as the FO's foreign attributes; the
  Word-mode managers read them (`docx4j:line-box`, `docx4j:baseline`, `docx4j:line-rule` on a block; `docx4j:font`,
  `docx4j:small-caps` on a run's span, read off the innermost inline by `WordLineLayoutManager`'s
  `innermostForeignAttribute`). docx4j writes them only where the renderer advertises `word-layout`.
- **The room has a place to go.** `WordLineLayoutManager.wordLine` sizes a Word line from its runs: each span's ascent
  and descent (its pitch, split by its font's ascent share), the line taking the largest of each, then the spacing rule
  (auto's multiple, atLeast, exact). A span's effect room added to its own ascent and descent there is phase 3's rule
  (each run's own reach, the line the most), added to `alt` and `pitch - alt` before the maximum (lines 1364 and
  1365). Three consequences (the review, finding 9): exact spacing skips the loop (1254), so it gives no room;
  atLeast takes `max(ascent + descent, lineHeight)` (1427), so the room is absorbed by any slack, where phase 3 took it
  as given on top; auto's multiple is applied as leading below (`box x factor - box`, 1431 to 1433), so it applies to
  the room unless the fork excludes it. The round decides atLeast and auto, and the fork is told which.
- **Foreign attributes don't reach text.** `AbstractLayoutManager.transferForeignAttributes` is called only by
  `AbstractGraphicsLayoutManager` (images); `TextLayoutManager` and `InlineLayoutManager` put none on their areas, and
  `IFRenderer.renderText` sets no IF context for the painter (it does for images and page sequences:
  `establishForeignAttributes`). So the fork needs: the run's effect attribute on its `TextArea`, and `renderText`
  handing it to the painter. `AreaTreeObject`, a `TextArea`'s ancestor, already keeps a foreign-attribute map (line
  38): `TextLayoutManager` (its `addAreas`) copies the innermost inline's attribute onto the area, as the line manager
  reads `docx4j:font`; no new trait. (`transferForeignAttributes` is also called by `BlockContainerLayoutManager` and
  `ExternalDocumentLayoutManager`, not for text.)
- **Where an effect would be drawn.** `IFRenderer.renderText` draws a text area's background and borders first
  (`renderInlineAreaBackAndBorders`), then its words (`TextUtil.flush`, `IFPainter.drawText(x, y, letterSpacing,
  wordSpacing, dp, text)`), then its decorations. One text area is one run's part on one line, so a picture drawn
  after the background and before the words is under that run's text and over the runs drawn before it: Word's order
  (section 1.1, measured). A run can be several text areas on one line (split by a tab, a font fallback or a hyphen):
  the gradient's frame is then the enclosing `InlineParent`'s rectangle (`renderInlineParent` computes it) or the union
  of the areas with one id on one line; the round's tab probe says which Word takes.
- **Drawing on the painter**: `IFPainter.drawImage(Document, Rectangle)` draws an SVG document at a rectangle, in PDF,
  PostScript, Java2D (PNG), AFP and the IF serializer; WordArt reaches the page as SVG already (`FOShapes`, through
  `fo:instream-foreign-object`), its gradients becoming PDF shadings through `PDFGraphics2D`. Under accessibility,
  `PDFPainter.drawImage(Document)` makes a Figure (`prepareImageMCID`, lines 366 to 371), where Word marks its paths
  and pictures as Artifact: the fork needs an artifact variant. The text keeps its Span and ActualText (`drawText`,
  503 to 509).
- **Glyphs**: `drawTextWithDX` maps each character through `font.mapCodePoint` (line 580) after FOP's own GSUB and
  GPOS, and `startSimulateStyle` (558) shears or thickens a face FOP has no italic or bold of. So the painter is handed
  glyph ids (a MultiByteFont's are the file's, which `java.awt.Font.createGlyphVector(FontRenderContext, int[])` draws
  from D4's same file; a single-byte or Type 1 font's aren't, reported and not drawn), the positions as the fork sets
  them (dx, dp, letter and word spacing, the hyphen), never shaped again, and any simulated style.
- **One factory, many conversions**: `FORendererApacheFOP` (lines 129 to 143) reuses a caller's `FOP_FACTORY`, so a
  painter set on the `FopFactoryBuilder` serves every thread and document; `setWordLayoutFontMetrics` gets away with it
  because its function is pure. A run's effects are per conversion: the painter finds them through the render (the
  `FOUserAgent`, read from the IF context), not on the factory.
- **Text rendering modes** exist (`PDFTextUtil.TR_STROKE` 1, `TR_INVISIBLE` 3, `TR_STROKE_CLIP` 5, `TR_CLIP` 7,
  `setTextRenderingMode`), unused by the painter today.
- **A callback from docx4j has a precedent**: `FopFactoryBuilder.setWordLayoutFontMetrics(BiFunction<String, String,
  Double>)`, through which docx4j's `WordLayoutCustomizer` gives the line manager Word's ascent share for a font. A
  text-effect painter would be set the same way.
- **A colour's alpha**: FOP writes an `ExtGState` for a fill colour's alpha from 1 to 254 (`PDFColorHandler.writeAlpha`,
  `#RRGGBBAA` parsed by `ColorUtil`), never for 0, so a fully transparent colour is drawn opaque: invisible text needs
  mode 3.

## 3. The round (proposed): cr083-phase5, about 70 probes

Word 365, mode 15, through the word-bridge as the earlier rounds; read by the page's paths, shadings, pictures and the
text's baselines (`grad.py`, `fills.py`, `read_effects.py`). What body text adds to phases 0 and 3:

- **wb80-body-lines**: a run with each kind (outline, gradient at 0 and 90 degrees, shadow, glow, reflection) broken
  across two and three lines (one gradient across the run or one a line; one picture a line); justified, with letter
  spacing, a tab and a trailing space inside it; two neighbouring runs whose shadows and glows overlap (the drawing
  order); an underlined and a struck run outlined and gradient-filled (is the line painted as the glyphs?); highlight
  and shading under a glow; a run's gradient with `scaled="1"`; a path gradient.
- **wb81-body-room**: the room at body sizes (10, 11, 12, 14 pt) for a shadow, a glow and a reflection; under auto 1.15
  and double spacing, exact and atLeast; a superscript with a glow; an empty paragraph whose mark has a glow; a run of
  spaces alone with one; a run in a table cell (the row's height); in a header (the header's height); a paragraph at
  the top of a page (the room against the top margin).
- **wb82-body-forms**: effects from a character style and a paragraph style; a hyperlink's result outlined; outlines of
  0.25 to 6 pt on 11 pt text; an outline with alpha; a text box with effects and no warp (phase 3's we13) in an autofit
  box; a shape's own gradient fill in the three forms (two stops at the ends, two inside, three), to see whether
  Word blends a shape's as a run's (section 1.2).
- From the review (section 4, finding 12): a run's gradient in each stop form and at a second face and size (the
  frame); a run split by hyphenation, by a soft hyphen and by a font fallback (the frame across text areas); a face
  with no bold or italic of its own (FOP's simulated style); a right-to-left run; a numbered paragraph whose mark
  carries the effect (Word paints the number; FOP's label is a block of its own); two runs with unequal effects on one
  line under atLeast spacing.
- Resting on few probes before the round: the gradient's frame (wb50-13, -14: one face, two sizes), the curve's
  condition (five probes), the drawing order (three).

## 4. The design's review (2026-11-14)

A Claude Fable 5.1 session reviewed the design, read-only, against Word's PDFs and saves for wb50, wb60, wb70, wg20
and we13, docx4j's code and the fork's (Jason: "design phase 5, then give your design to fable for review"). 13
findings, each checked here; applied (Jason: "D20 as recommended; D21 as recommended; D23 as recommended; D22 and D24 as
per fable review. apply all 13 then commit etc"):

1. (must) The curve is the two-stop case only (wb50-15, wg20-06 to -08: even in sRGB; checked here on wb50-15 and
   wg20-06): section 1.2, D24.
2. (should) The drawing order is measured, not to be confirmed (wb50-25, wg20-04, -09); wb50-22's and -24's numbers
   stated exactly: sections 1.1, 1.3, 2. Where the text is invisible, under and over are one moment; two calls are kept
   for an inner shadow (phase 3: drawn after real text).
3. (could) The run's box down is on Word's 0.24 pt grid: the fork's span box, not docx4j's own (section 1.2). The
   fork's area already keeps foreign attributes (section 2).
4. (must) The painter's surface: an SVG document and its rectangle, drawn by `IFPainter.drawImage(Document,
   Rectangle)` in every output and the serializer; mode 3 is the only painter-specific part (PDF inside the text object
   `drawText` opens; Java2D, skip the fill; the serializer, an attribute on `<text>`): section 2, the CR's phase 5, D20.
5. (must) Accessibility: an artifact variant of that draw, not a Figure for every run on every line: section 2, D20.
6. (should) Glyph ids, the fork's positions and its simulated style handed to the painter: section 2, D20.
7. (should) A run split into several text areas on a line: the frame by the enclosing inline or a union, the round's
   tab probe deciding: sections 2, 3.
8. (should) The run's effects found per render (the `FOUserAgent`), not on a factory shared across conversions; a
   capability of its own (`text-effects`), so a docx4j that has it writes nothing for a fork that hasn't: section 2,
   D21, the CR's phase 5.
9. (should) D22's consequences under exact, atLeast and auto spacing stated; `TextEffects.reach` needs the run's
   width only for a shadow skewed vertically (`ky`), whose room docx4j takes at the run's whole advance: section 2,
   D22.
10. (should) The gate adds PDF size and render time on the corpora's 12 documents against b478, and their movers are
    read line by line: the CR's phase 5.
11. (could) PDF text rendering mode 2 kept as an option for the common case, a solid outline over a solid fill (no
    glyph paths), decided by the gate's size numbers: D20.
12. (should) The round's additions: section 3.
13. (should) The painter and the routes three days, not two; the fork's three days take in each painter and the
    serializer: the CR's §11.
