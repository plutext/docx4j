# CR-083 phase 5: WordArt effects on body text (design measurements, then findings)

Phase 5 draws a body run's Word 2010 text effects: `w14:textFill`, `w14:textOutline`, `w14:shadow`, `w14:glow` and
`w14:reflection`, in body paragraphs, table cells, headers and footers, and in text boxes with no warp (phase 3's D12).
3-D (`w14:props3d`, `w14:scene3d`) is phase 6's. The design is in the CR (phase 5, D3, D20 to D24). This note holds
what is known before the round (section 1), what the FOP fork offers (section 2), the round (section 3), the
design's review (section 4), step 1 with the fork's CR (section 5), the round read (section 6), and step 2, the
painter (section 7).

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
- **Corrected by the fork's session** (2026-11-14, drafting fop/CR-043 against e06e12a37; section 5.2):
  - Glyph ids: under subset embedding, the default, `MultiByteFont.mapCodePoint` returns the subset's selector
    (`cidSet.mapCodePoint`), not the file's glyph id, so the ids `drawTextWithDX` writes are not the file's. The fork
    hands the painter `MultiByteFont.findGlyphIndex(cp)`, the file's ids, which `createGlyphVector(frc, int[])` draws
    from the same file. With the written ids the painter would have drawn the wrong glyphs in nearly every subset font.
  - The artifact needs no new painter method: with no structure element in the IF context `PDFPainter.drawImage(Document)`
    gets ARTIFACT from `addImageContentItem(null)`, and `PDFImageHandlerSVG` wraps the drawing in `/Artifact BMC`; the
    fork resets the element around the effect's draw and restores the text's (its Span and ActualText kept).
  - Simulated bold: `startSimulateStyle` writes a raw `2 Tr` with a stroke width outside `PDFTextUtil`'s mode tracking;
    under mode 3 the fork skips it (else the invisible text is stroked), the painter told and drawing the style itself;
    the SVG-table font path (`drawSVGText`) is skipped too.
  - JDK types: docx4j's `WordLayoutCustomizer` sets `setWordLayoutFontMetrics` by reflection with a `BiFunction`, so that
    docx4j loads against Apache FOP; a fork interface for the painter would break that.
  - The `wordLine` line numbers are one off on that tree: the maxima at 1365 and 1366, atLeast 1425, auto 1433.

## 3. The round: cr083-phase5, 88 probes (staged 2026-11-14)

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

Built 2026-11-14 by `build_body.py` (`probes-body.tsv`): wb80-body-lines 25, wb81-body-room 29 (its two header probes
last, each its own section; each page a plain line first, so that it holds its own reference), wb82-body-forms 32 (a
styles part with a character and a paragraph style, a numbering part, a hyperlink to a bookmark, a text box and three
shapes), wb83-body-hyphen 2 (`w:autoHyphenation`); the runs' line breaks checked against Aptos's own advances. Staged as
`cr083-phase5`, verified and announced by the portfolio (to-vm 288, controls `cr083n-mu-control-a`/`-b`), on bridge
22479f7; results awaited.

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

## 5. Step 1 and the fork's CR (2026-11-14)

Jason: "commence phase 5". The round staged (section 3), the fork's CR drafted by its session (5.2), and step 1, which
waits on neither, built (5.1).

### 5.1 Step 1

- **A solid fill as the run's colour** (`TextFill`, from `PropertyFactory.createProperties`, after `FontColor`, both FO
  routes and HTML): the fill's colour through the theme, over `w:color` (wb50-01); its `w14:alpha` (a transparency) as
  "#RRGGBBAA" in FO, which FOP draws at that alpha, none left written 01 (FOP draws 00 opaque); rgba() in HTML. The
  paragraph mark's fill colours its number (unmeasured; the round's wb82-30 and -31). A gradient or no fill keeps
  `w:color` in PDF until the painter draws them. Word draws a transparent run as a filled path over invisible text;
  docx4j keeps it real (D20).
- **HTML's CSS (D23)**: a gradient as the run's background clipped to its text (`-webkit-text-fill-color:
  transparent`, so an underline keeps `w:color`); DrawingML's angle plus 90 degrees, a path gradient radial about its
  focus (a circle, others an ellipse, reported); no fill transparent text; an outline as `-webkit-text-stroke` (width
  0 as 0.25 pt; compound and dashed drawn solid, gradient not drawn, reported); a shadow and a glow as `text-shadow` at
  `TextEffects.scale` of the run's size (a shadow's blur radius 2/3 of `blurRad`, Word's sigma being `blurRad` / 3 and
  CSS's half the radius; a glow's radius its own, CSS having no spread), in em at 11 pt's scale where the run states
  no size; a reflection and an inner shadow reported (`render.wordart.body_effect`, once a JVM at warn).
- **Word's curve (D24)**: `GradientCurve` (the table and blend `VmlWordArt` had) for a gradient of exactly two stops at
  0 and 100%; `Fills.wordGradient` for WordArt's runs (and, after the round, shapes' fills: section 6.4), 17 stops. Measured on phase 0's probes (Word's PDFs, shading
  functions sampled at quarters, `grad.py`): wa30-13, -14, -16 (linear 0 and 90 degrees, path circle) Word (238, 0,
  106), (186, 0, 187), (106, 0, 238), docx4j now (238, 0, 105), (186, 0, 186), (105, 0, 238), each within 1 (before
  the even blend, (191, 0, 64) a quarter of the way); wa30-15 (three stops) and wg10-06 to -08 (stops inside, gallery
  styles 6 to 8) unchanged, already within 1. Shapes' gradients took the curve after the round (section 6.4).
- **Found measuring it: flat WordArt's gradient spans the run's frame, not its ink.** Word's axis at 90 degrees on
  wa30-14 and wg10-06 to -08 runs 255.6 to 299.5 (43.9 pt at 36 pt, the run's ascent to descent, as on body text,
  section 1.2), docx4j's ran 265.9 to 289.8 (the ink of "WordArt"). Now `Drawing.Area.setGradientFrame` carries the
  run's frame (its advance across, `getAscent` to `getDescent` down, over all its lines) on unwarped text: every axis
  within 0.2 pt of Word's (wa30-13 151.3 to 279.2 against 151.2 to 279.1; wa30-15 at 45 degrees likewise), the radial
  radius 67.627 pt against Word's 67.625. A warp's frame is unmeasured and stays the ink.
- **Tests**: core-tests `TextEffectPropertiesTest` (14: the curve, its condition, the FO colour and alpha, the theme,
  a gradient keeping `w:color`, the mark, each CSS form, both HTML routes and a character style); export-fo-tests
  `TextFillFoTest` (both FO routes: fill, alpha, scheme, character style, gradient); chart-render
  `WordArtRendererTest` (the curve's 17 stops, three and inside stops even; the frame on flat text, the ink on a
  warp). Suites: core-tests 2,697, chart-render 111, export-fo 111, export-fo-tests 972 pass.
- **Gates** (against b464): b479 (step 1 without the frame) 0 movers, events equal; against b478 the corpora's PDFs
  the same but for the time fields (5361, 11875), today's date in nine documents' DATE fields (b478 ran the day
  before), and 5075, whose two "Costco" runs carry `w14:textFill` 002D56 at `lumMod` 75%: now #002240, Word's PDF
  (0, 34, 65), before `w:color`'s #002D56. b480 (the final build) 0 movers, events equal; against b479 the time fields
  and 14114, one inline's letter spacing 0.223 to 0.212 pt, a flake: rendered alone, both builds write 0.223 (no
  WordArt in it). The probes' 324 PDFs the same.

### 5.2 The fork's CR

fop/CR-043 (`../xmlgraphics-fop-plutext/docs/developer/change-requests/CR-043-text-effects.md`, 18bb8d136, updated
45687e5c7), drafted by the fork's session from the CR's §10 and this note's sections 2 and 4; a design only, its build
Jason's to order in that session. Its corrections to section 2 are there. Its questions, answered:
- (a) The painter: `FOUserAgent.setTextEffectPainter(BiFunction<String, Map<String, Object>, Map<String, Object>>)`,
  JDK types only, set by reflection; called with the id and phase (under, over), glyphs (the file's ids, or null for
  a single-byte or Type 1 face: reported, not drawn), x and y, text, font-uri, font-name, font-size, bold-simulated,
  italic-shear, color, area (the text area's advance and span box) and run-on-line (the enclosing inline's rectangle
  on the line); returning svg, rect and, under, glyphs-drawn. The keys are constants in the fork's
  `org.apache.fop.docx4j.TextEffectKeys`; docx4j copies the strings.
- (b) The frame: both area and run-on-line passed; the painter takes the one the round shows.
- (c) The room kept apart from the plain ascent and descent (the line max(plain, alt + above)), the same as adding it
  before the maximum, so that `declaredBaselineLine` and the list-label rule still see a declared line; atLeast
  absorbing it and auto's factor applying, as D22 has them, until the round (wb81) says otherwise.
- (d) Decorations painted as the glyphs: a return key, later, if wb80-17 to -19 show it.
- (e) Four days, the subset's ids and the room kept apart the extra; mode 2 half a day more.
- PostScript, AFP and PCL have no invisible mode: there the text isn't drawn where the painter draws the glyphs (the
  pictures are); a known limit in the fork's CR. The IF carries `docx4j:invisible` on `<text>` and the SVGs as
  artifact images, so an IF rendered later comes out the same.
- Enterprise CR-001 §6.6 item 70 (85031ab): FOP draws no text effects and gives a run no room for them; docx4j's
  workaround step 1.

## 6. The round read (2026-11-14)

`cr083-phase5`'s six jobs silent and complete on bridge 22479f7 (Word 16.0.20326.20318); read from Word's PDFs with
`read_body.py` (new: each page's drawing in order from `mutool trace`, text extents and baselines, shadings' axes,
pictures), `grad.py` (the shadings' colours) and `read_effects.py` (which pictures are empty). Word's saves: wb80 two
identical shadowed runs merged (wb80-15 is one run), runs split at a tab; wb82 a soft-hyphenated run one run a syllable,
the internal hyperlink a HYPERLINK field, VML fallbacks for two shapes; wb81 note parts added.

### 6.1 Lines and frames (wb80, wb82, wb83)

- **A run broken across lines is drawn line by line**: on each line its pictures, its paths (or its shading), its text
  at alpha 0 (wb80-01 to -09), one picture a line for a shadow, glow or reflection (wb80-04, -05, -06, -09), so the
  next line's picture lies over the line before's text. A gradient is one a line, never one across the run.
- **The frame is the run's part on its line, per font**: across, from its first glyph's origin to its last advance,
  including a trailing space (wb80-02: 344.40 to 508.08, its text 344.54 to 507.96; mid-line wb80-14), the last glyph's
  letter spacing (wb80-11: 490.08, the glyphs 486.96), a line-end hyphen (wb83-01, wb82-23) and a justified line's
  stretched spaces (wb80-10); spanning a tab (wb80-12: two text objects, one shading 344.40 to 523.92) and soft hyphens
  (wb82-23: Word splits the text at each, one shading a line); split by a font fallback (wb82-24: "stars ", the
  Segoe UI Symbol "★★★" and "and " each its own frame, the fallback's in its own font box). Down, the line box (wb80-03:
  121.92 to 151.20, the pitch exactly; three lines' frames contiguous, wb80-08). Ends on Word's 0.24 pt grid. The
  effect pictures group the same way (wb80-13: one glow over the tab; wb82-25: one a font).
- **Word's shading encoding**: some shadings are mirrored, the function running there and back over twice the axis
  (blue, red, blue), only the frame's half on the page (shapes, wb82-13 to -15: -144 to 432 for a shape at 144 to 432;
  underlines; wb82-19; wb83-01's last line). The visible gradient is the same; a reader must sample the function, not
  take the axis.
- **Decorations are painted as the glyphs**: an underline and a strike-through filled with the run's gradient over the
  run's frame and stroked with its outline, after the run's text (wb80-17, -18); a hollow run's underline stroked only
  (wb80-19); a hyperlink's text and underline in its colour, outlined (wb82-03).
- **A right-to-left run's gradient is reversed**: at 0 degrees it starts at the run's right end, its reading start
  (wb82-28: blue at the left, red at the right).
- Highlight and shading drawn before the glow picture, over the line box including the room (wb80-20, -21: 51.36 at
  36 pt with a glow of 8). 45 degrees with `scaled` 0 and 1 as docx4j has them (wb80-22, -23). Path gradients `rect`
  and `shape` are pictures in Word's PDF, not shadings (wb80-24, -25). Simulated bold: the glyphs filled and stroked
  (wb82-26); simulated italic sheared paths (wb82-27). Outlines 0.25 to 6 pt at their stated widths at 11 pt (wb82-04
  to -09); an outline's alpha its stroke's (wb82-10: 0.498). A text box's runs as body text's (wb82-11, -12).
- Word emits empty pictures (alpha 0 throughout) for a paragraph mark with a glow (wb81-23, wb82-30's second) and for a
  run of spaces alone (wb81-24): nothing to draw.
- Unexplained, one probe each: a gradient from a character style with a glow (wb82-01) framed by the ink (346.08 to
  444.48, the ink 345.60 to 444.41), not the advance; a paragraph style's outlined text with no invisible text in
  Word's PDF (wb82-02). A follow-up pair would separate the style from the glow.

### 6.2 The room (wb81, wb82-32)

Phase 3's rule (`TextEffects.reach`) holds at body sizes within one 0.24 pt grid step. Plain pitch (Aptos) 12.24,
13.44, 14.64, 17.04 at 10, 11, 12, 14 pt; room above and below measured (predicted):

| Aptos | shadow blur 4, 3 at 45 degrees | glow 5 | reflection sharp, 4 down |
|---|---|---|---|
| 10 pt | 0.48, 1.44 (0.47, 1.54) | 0.96, 0.72 (0.94, 0.94) | 0, 4.32 (0, 4.46) |
| 11 pt | 0.48, 1.68 (0.50, 1.64) | 0.96, 0.96 (1.01, 1.01) | 0, 4.80 (0, 4.90) |
| 12 pt | 0.72, 1.68 (0.54, 1.75) | 0.96, 1.20 (1.07, 1.07) | 0, 5.28 (0, 5.35) |
| 14 pt | 0.48, 2.16 (0.60, 1.95) | 1.20, 1.20 (1.19, 1.19) | 0, 6.24 (0, 6.24) |

- **Spacing rules** (14 pt, a glow of 8: 1.91 each side; a reflection 6.24 below): auto 1.15 and double apply their
  multiple to the room (wb81-13: 21.60 and 22.08 against 21.50 and 22.07 so; wb81-14, -18 within 0.3); exact gives
  none (wb81-15, -19: every pitch 24.0); atLeast under the line's height as single spacing (wb81-16); **atLeast with
  slack keeps its height, the room absorbed, but the baseline moves up by the room below** (wb81-17: 28.08 then 31.94;
  wb81-20, a reflection: 23.76 then 36.24, 30 - 6.24 and 30 + 6.24); two runs unequal the line the larger (wb82-32:
  4.32, glow 18's 4.29).
- **A superscript or subscript takes the room of its run at its stated size, unraised** (wb81-21, -22: 1.92 above and
  below at 14 pt, where the raised small glyph would take 2.08 above and none below).
- An empty paragraph whose mark has an effect takes the room (wb81-23: 3.86); a run of spaces alone takes none, its
  line the mark's (wb81-24). A table row grows by the room, the cell's text moved down by the room above (wb81-25:
  3.84, 1.92). The first line of a page moves down from the margin by the room above (wb81-26, -27: 3.60 at 36 pt); a
  header grows by it (wb81-28, -29: the body 3.84 lower).
- Unexplained, one probe: a numbered paragraph whose mark has a glow got room above (2.88) and none below (wb82-30); the
  mark's glow paints the number (a picture) and a gradient on the mark fills it (wb82-31).

### 6.3 The curve (wb82)

D24's condition confirmed on more faces and sizes: two stops at 0 and 100% by the curve (wb82-16, Calibri 20 pt; -20,
Times New Roman 48 pt: (238, 0, 106), (186, 0, 187), (106, 0, 238)); two stops inside the run even (wb82-17: (145, 0,
110) halfway), two stops at 0 and 60% even with the end colour held past 60% (wb82-21: (149, 0, 106) a quarter of the
way, (42, 0, 212) halfway), three and four stops even (wb82-18, -22).

### 6.4 Shapes' gradients (wb82-13 to -15)

A shape's own gradient fill is blended as a run's: two stops at 0 and 100% by the curve ((187, 0, 186) halfway, where an
even blend gives (128, 0, 128)), two stops inside the shape even ((145, 0, 110)), three even. So D24's second half is
decided by measurement: `Fills.wordGradient` (renamed from `textGradient`) now serves a shape's fill in
`ShapeRenderer` too; a line's gradient and a chart's stay even (unmeasured). Test: chart-render `ShapeHtmlTest` (a
shape's two-stop gradient 17 stops, its middle (186, 0, 186); three stops three), chart-render 112 pass. Gate b481
against b464: 0 movers, events equal; against b480 the time fields and 14114 back to b479's letter spacing (b480's
0.212 the flake); no corpus document has a two-stop shape gradient at the ends. The probes' 324 the same.

### 6.5 To the fork (sent 2026-11-14)

From 6.1 and 6.2, for fop/CR-043: the room as built (auto's factor on it, exact none, atLeast absorbed) with the
baseline under atLeast moved up by the room below (to be confirmed in its `wordLine`); the room relative to the
unraised baseline (a superscript's); an empty paragraph's room from its block; the painter called once per group (the
run's id, one font, one line: its areas' glyphs together, their union), under before its first area and over after its
last, in place of area and run-on-line; decorations in the first build (the underline's and strike's positions and
thicknesses, a return key so that FOP skips its own); the area's direction for a reversed gradient; a background
extended by the room (low priority).

## 7. Step 2: the painter (2026-11-14)

Jason: "do step 2", on the fork's r85 (fop/CR-043, merged to 2.12-docx4j.2 at a3a0ceb10) and then r86 (fop/CR-043.1,
the grouping below; unmerged at writing).

### 7.1 Built

- **The marking** (docx4j-export-fo `TextEffectsFo`, where the renderer advertises `text-effects`, a new
  `FopCapabilities` entry): a run Word draws more of than its text in its colour (`WordArtText.hasBodyEffect`: an
  outline, a gradient, a transparent fill, none, a shadow, a glow, a reflection) gets an id on its `fo:inline` in
  both FO routes (the visitor's `handleRPr`, the XSLT route's `createBlockForRPr`), its effects in the conversion's
  table as a WordArt run of its formatting (`WordArtText.bodyRun`, the run factory phases 1 to 3 use, now shared);
  a shadow, glow or reflection also its room, phase 3's rule at the run's stated size, unraised; an empty paragraph's
  mark its room on its `fo:block`. Written as hints, which `WordLayoutFixups.lineBoxAttributes` turns into
  `docx4j:text-effect` and `docx4j:effect-room` as it does a run's font; the two names in `TextEffectsFo`, not in
  docx4j's `WordLayoutElementMapping`, which names the attributes of the renderer docx4j is built against
  (2.12-docx4j.1: `WordLayoutAttributesTest`), until docx4j moves to the one with them.
- **The painter** (docx4j-chart-render `TextEffectPainter`, a `BiFunction` of maps, the fork's `TextEffectKeys`
  strings copied), set per conversion on the `FOUserAgent` by `FORendererApacheFOP.textEffectPainter`, by reflection:
  the group's glyphs from the font file (`Font.createFonts`, a collection's face by name; ids the file's, from the
  fork), at the fork's positions, a simulated italic sheared; drawn by `WordArtRenderer.drawBody`, a third entry
  beside `draw` and `drawVml`: under the text the effect pictures (`unit`, phase 3's, at `TextEffects.scale`), then,
  where Word draws the run as paths, the glyphs filled and stroked (`paintGlyphs`, now shared with WordArt's loop) and
  the underline and strikes painted with them; over the text an inner shadow. The gradient's frame is the group's
  FRAME with the suppressed trailing space and the last letter spacing; a right-to-left run's gradient mirrored. The
  SVG returned with its rectangle; GLYPHS_DRAWN where Word draws paths or nothing, DECORATIONS_DRAWN where the run's
  decorations were painted. A face whose glyphs can't be had (no file, no ids) isn't painted: the text is drawn as
  without effects, reported once (`render.wordart.body_effect`).
- **Not built at first**: a numbered paragraph's mark painting its number (wb82-30, -31), built after (7.6). The
  highlight over the room (the fork's, low priority). Mode 2. Phase 7's gap report takes these.

### 7.2 Against Word

- **Drawing, page by page** (counts of shadings, pictures, filled and stroked paths, invisible text; Word's PDFs
  against docx4j's, wb50, wb80, wb82, wg20, r86): every page alike but for: wb80-15 (Word merged the two identical
  runs on save: one picture to docx4j's two), wb80-24, -25 (Word draws a path gradient other than a circle as a
  picture, docx4j as a radial shading), wb82-24, -25 (the telephone a colour emoji picture in Word), wb82-26 (a
  simulated bold: the thickening's own shading), wb82-02 (docx4j keeps the text Word drops), wb82-30, -31 (the
  number, above), wg20-05, -10 (3-D bevels, phase 6).
- **Frames**: on r85 a run's chunks (docx4j's inner inlines, a tab's leader, the syllables of soft hyphens) were
  groups of their own (wb80-12: two frames, 346.30 to 413.54 and 432.00 to 523.75, where Word has one, 344.40 to
  523.92; wb82-23: 29 where Word has 3). The fork's r86 groups a line's consecutive areas of one id and one font across
  inner inlines, leaders and spaces: wb80-12's one frame 346.30 to 523.75, wb80-13's two pictures, wb82-23's three
  frames, as Word's. The remaining offsets are the text's own (docx4j's line starts 1.9 pt later on wb80-02).
- **The room** (wb81, stext baselines): every pitch within 0.25 pt of Word's at 10 to 14 pt, under auto 1.15, double,
  exact and atLeast (wb81-17: 28.09 and 31.91 against 28.08 and 31.94; wb81-20: 23.76 and 36.24 against 23.76 and
  36.26), a superscript's and a subscript's, an empty paragraph's mark (37.99 against 37.94), a table row (21.38
  against 21.36), a header's line (1.91 against 1.92) and a page's first line (3.69 against 3.84). A paragraph of
  spaces alone is 2.5 pt taller than Word's with or without the glow (its line sized by its run, not its mark): not the
  effects'.

### 7.3 Found and fixed

- **A simulated bold's thickening as a union**: the glyphs and their stroke made one `Area`, which split every curve:
  corpus 4102's one run ("LTD CLEAN WORLD", Sylfaen bold italic, outlined, underlined) wrote 61,801 path operators,
  its PDF 305 KB to 718 KB and its render 132 to 1,040 ms. Now the stroke is filled on its own, not outlined (as the
  renderer strokes the face): 353 KB, 172 ms.
- **A test that left a property off**: `PageBreakParagraphLineTest` restored `pageBreakParagraphLine` to "false"
  where it had been unset, which `Preprocess` reads as on; any test after it in the JVM ran without the break
  paragraph's line, and `FloatingTablePositionTest`'s two `anchorsPageBreak` tests failed once a new test file moved
  them after it (bisected with JUnitCore over the 101 classes before it). It now removes the property.

### 7.4 Tests and gates

- Tests: chart-render `TextEffectBodyTest` (7: a gradient's paths over its frame by the curve, mirrored right to left;
  a glow a picture under real text; no fill nothing; an inner shadow over; decorations and a bold's thickening; the
  painter's answers; the decorations' rectangles); export-fo-tests `TextEffectsFoTest` (5: the room by phase 3's rule
  and a superscript's at its size, which runs are marked, the conversion's table, the hints to attributes). Suites:
  core-tests 2,697, chart-render 119, export-fo 111, export-fo-tests 977 pass.
- Gates (the corpora, 12 threads; docx4j bins against the fork's jars):
  - b483: step 2 on r85 against b482 (HEAD on r85): one mover, real's 15_de-DE_sdt_79 +65 lines, 0.7949 to 0.9775 (an
    empty paragraph whose mark has a shadow: its room, 1.00 above and 2.10 below, the only change in its FO); events
    equal. PDFs changed: 79 and 14 (that room), 14372 (4 runs painted, +11%), 252 (14 shadowed runs, +38%, render
    1.02 to 1.37 s), 4102 (+135%: the union above).
  - b485: HEAD's bins on r86 against b482 (r85): 0 movers, events equal, 37 hooks, the PDFs the same but the time
    fields, the probes the same. r86's control passed.
  - b486: step 2 (final) on r86 against b485: the one mover 79 (+65), events equal; PDF size 14372 +11%, 252 +38%, 4102
    +15%, 14 and 79 unchanged; render times within the run's noise (the corpora's totals -1.6% to +1.7%); the probes'
    324 the same.

### 7.5 Two of the gaps closed (2026-11-14)

Jason: "can we fix the 2 things still open in phase 5? feel free to delegate to fable". The numbered paragraph's mark
went to a Claude Fable 5.1 session (7.6); these two here:
- **Runs Word draws as one**: two neighbouring runs of one formatting are one run to Word, one picture and one frame
  (wb80-15: one shadow picture for "Shadow" and "Next"; Word's save merges them). `TextEffectsFo.mark` writes a key of
  the run's effective properties beside its id (`HINT_TEXT_EFFECT_KEY`), and `WordLayoutFixups.lineBoxAttributes` gives
  a run whose key is its previous sibling run's that run's id, so the fork's group runs across both. wb80-15 one picture,
  as Word's; corpus 252's 14 marked runs 5 groups, 14372's 4 two.
- **A rect or shape path gradient on text**: Word draws it as a picture whose rings are rectangles about the focus, the
  colour by the larger of the distances across and down (wb80-24, -25: a diagonal point 0.61 of the way out the colour
  of the row's at 0.61; an ellipse would put it 0.86 out), blended by the two-stop curve. `Drawing.Gradient.rectangular`
  and `picture` (2 pixels a point, Word's 1.33), painted clipped to the glyphs by `Drawing.paint`; `paintGlyphs` makes a
  run's non-circular path gradient rectangular. Against Word's pictures: within 4 of 255 across the middle, within 9 on
  the diagonal. A shape's path gradients are unmeasured and unchanged.
- **Not a text effect**: a colour emoji is drawn in colour by Word, as a picture in its own PDF export (wb82-24, -25:
  the telephone) and as the font's colour layers when printed (the probe `fonts-symbol-and-emoji`, no effect); FOP
  draws a face's monochrome glyph in the text's colour. A renderer limit for all text, outside CR-083: recorded as
  Enterprise CR-001 section 6.6 item 71 (Jason: "we should record the emoji issue somewhere").
- Tests: chart-render `TextEffectBodyTest` (a rect path gradient's rings; a circle stays a radial shading), export-fo-tests
  `TextEffectsFoTest` (identical neighbours share an id); chart-render 120, export-fo 111, export-fo-tests 978 pass.
  Gate b487 against b486: 0 movers, events equal; 14372 and 252 smaller (132,916 to 132,360 and 176,476 to 170,238
  bytes, fewer groups); the probes' 324 the same.

### 7.6 A numbered paragraph's mark painting its number (2026-11-14)

Built by a Claude Fable 5.1 session in a worktree of its own (Jason: "feel free to delegate to fable"), its patch read
and merged here.
- **The label**: the renderer reads an id from the inlines above a text, never from the label's `fo:block`, so where
  the label's effective properties (the mark's, under the level's `w:rPr` where it has one) have an effect, the number
  or bullet goes into an `fo:inline` carrying the id (`TextEffectsFo.labelInline`, `XsltFOFunctions.createListBlock`'s
  `setLabelText`, both routes sharing it; the conversion settings threaded through `createBlock`, `createListBlock`,
  `createInlineLabel`); a centred or right-aligned number, already an inline on the text's line, gets the id only
  (`markInlineLabel`). Without the capability, or without an effect, the FO is byte for byte as before.
- **The room**: the one numbered line measured (wb82-30) had the mark's room above (2.88 pt, phase 3's rule's 2.78 on
  the grid) and none below, which is the label rule's own shape (its ascent raises the line, its descent counts for
  nothing). So the room above rides on the label's ascent (`HINT_LABEL_ROOM` on the label's block, which
  `WordLayoutFixups.listLabelLines` adds to `labelAscent`: into the label's line box and the body's `label-ascent`,
  outside auto's multiple, absorbed by atLeast's slack, none under exact), not `effect-room` on the inline, which the
  renderer would read as room on both sides of the label's line alone. Provisional: the round cr083-phase5b (16 probes,
  `build_body.py` set b, `probes-body-b.tsv`: glow, shadow and reflection on the mark at 24 and 12 pt, a bullet, the mark
  and the text both, the text alone, the level's `w:rPr`, each spacing rule, an unnumbered mark, a centred number) is
  staged and sent to the portfolio.
- **Against Word**: wb82-30 the number's glow picture (Word's second picture, the mark's, is empty), the line 2.78
  lower (Word 2.91), the next paragraph at the plain pitch (29.29; Word 29.28); wb82-31 the number filled with the
  mark's gradient over its advance, 72.00 to 91.68 as Word's, its text invisible.
- **Found on the corpora: effects that draw nothing.** The first gate with it (b488) painted 1 to 8 heading numbers in
  five documents (2394, 9838, 9031, 8992, 6251) for nothing: their numbering levels write "no effect" as an inner
  shadow with no blur and no distance, a glow of radius 0 and a reflection with every opacity and its height 0 (and a
  hollow outline of width 0, already none). `WordArtText.hasBodyEffect` now counts none of them (`drawsNothing`), nor
  does HTML's report: b490 against b487 the corpora's PDFs the same but the time fields. So no corpus document has a
  number with an effect.
- **Found, the fork's (to confirm with cr083-phase5b's wb84-09)**: a numbered paragraph whose text has a glow, in
  docx4j's render: the text's baseline not moved down by the room above, and the next paragraph pushed by twice the
  room (a pitch of 34.86 where single room gives 29.28 + 2.78); wb81 measured only unnumbered lines. Seemingly the room
  above lost against the label's declared baseline in the renderer's list item; to the fork once Word's number is in.
- Tests: export-fo-tests `NumberedLabelEffectsFoTest` (6: the number in an inline with the id in both routes, no
  effect-room, the label's ascent raised by the room; a gradient without room; a centred number; byte-identical FO
  without the capability or an effect; the hint consumed), `TextEffectsFoTest` (effects that draw nothing are none);
  core-tests 2,697, chart-render 120, export-fo 111, export-fo-tests 985 pass. Gates: b488 (with the numbers, before
  `drawsNothing`) 0 movers, the five documents' numbers painted for nothing; b489 (the shadow and glow) 0 movers, four
  left; b490 (final) against b487 0 movers, events equal, the corpora's PDFs the same but the time fields, the probes'
  324 the same.

