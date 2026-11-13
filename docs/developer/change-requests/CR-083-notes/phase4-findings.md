# CR-083 phase 4: VML WordArt (design measurements, then findings)

Designed 2026-11-13 (Jason: "start phase 4", "write up a design for phase 4"), on docx4j 1a1b555b1. Section 1 is what
is known before the round: phase 0's VML probes read again (Word's PDFs and saves of `wv20-vml-spt-mode12` and
`wv30-watermarks`, the round of 2026-11-12), the one real document with VML WordArt, docx4j's code, and the standard.
Section 2 is the round the design needs; section 3 the questions it answers. The design itself is in the CR (phase 4,
and D16 to D19). Reviewed the same day by a Claude Fable 5.1 session (Jason: "commit then have fable review the
design"), its 13 findings applied (Jason: "apply all 13 to the CR and the findings note, with finding 9 as a revision
to D17"; section 4). Later sections hold what the phase finds.

## 1. What is known before the round

### 1.1 Word's PDF of VML WordArt

- **Glyphs as filled paths, and no text at all.** wv20-42 (type 136 with its top line slanted, "WordArt", Aptos 36 pt,
  black) is seven `fill_path`s, one a glyph, each `winding="eofill"`. The page's text is its two labels, and nothing of
  the WordArt, visible or invisible. wv30's watermarks likewise: the glyphs filled, in silver (`.753`) at alpha
  `.50196` where `v:fill opacity=".5"` (128 of 255), and not in the page's text. So Word writes no invisible text for
  VML WordArt, where it does for DrawingML WordArt drawn as outlines (phase 0 findings §1).
- **The fill rule is even-odd**, and Word's PDF writer fills every path so: VML WordArt (wv20-42, 7 paths; wv30, 9 a
  page), DrawingML WordArt (wa10-01, 7; wa14, 20; wa20-form, 7) and real3's table borders alike (review, finding 3).
  Java2D's glyph outlines are drawn nonzero, which differs only where a glyph's contours overlap: docx4j writes
  `fill-rule="evenodd"` on WordArt's glyph paths, one probe (wv44) checking a glyph that overlaps itself.
- **The text is stretched to the box, by its ink with `trim`** (review, finding 2). wv20-42 (`trim`, `fitshape`,
  `fitpath`; the box 288 by 144 pt at (144, 252)): the glyphs' ink runs from 144.00 to 431.96 across and from 252.00 to
  395.92 down, the box's edges, so the string's ink box is mapped onto the shape's box, about 2.25 times across and 5.3
  down, before the two paths warp it. Phase 2's DrawingML envelope keeps the text's natural width, centred (CR §2).
  wv30-02 (a horizontal watermark, no `trim`, 468 by 117 pt): the ink runs 467.83 of the 468 across but only 76.91 of
  the 117 down, the capitals' tops 7.1 pt below the box's top and the baseline 33 pt above its foot. So without `trim`
  the box down is some font-metric box (the room for ascenders and descenders, which `trim` removes, Part 4 says), not
  the ink. Neither "WordArt" nor "WATERMARK" has a descender, so phase 0 can't say which metrics: the round's strings
  (wv44) can.

### 1.2 Shapetypes

- A shapetype with only its `o:spt` (wv20-01 to -40, types 136 to 175) draws nothing. Word's save rewrites each as
  `o:spt="100"` with `path=""`, its shape pointing at a new `_x0000_mNNNN` shapetype of the same form.
- A shapetype with a path draws that path (wv20-42's slanted line). Word's save writes it as a custom shape:
  `o:spt="100"` on the shapetype and the shape, the path copied onto the shape (`path="m,l21600,10800m,21600r21600,e"`,
  Word's own spelling of the same path) and `adj="10800,,0"`.
- **Correction to phase 0 findings §8.** wv20-41 ("the whole type-136 shapetype, after an spt-only 136, also drew
  nothing") was a probe fault, not a Word rule: `build_wordart.py` gave that shapetype `id="_x0000_t136"`, the id the
  spt-only 136 already had, and pointed the shape at `#_x0000_t136w`, which no shapetype has. What wv20-41 shows is that
  a shape whose `type` doesn't resolve draws nothing (Word's save drops the `type`). Whether Word takes a definition by
  `o:spt`, and which of two shapetypes with one id wins, are untested: section 2, wv43.

### 1.3 Word's watermark

The form (wv30, in each header) is `build_wordart.py`'s copy of the watermarks on this machine's documents
(`Mark.header`), not output from Word's Watermark dialog; Word's save kept it, adding `w10:wrap` (review, finding 5):
- the shapetype 136 whole: 14 formulas, `adj="10800"`, `path="m@7,l@8,m@5,21600l@6,21600e"` (two lines, the top and
  the bottom of the text), `v:path textpathok="t"`, **`v:textpath on="t" fitshape="t"`**, a handle;
- the shape `PowerPlusWaterMarkObject<n>`: `position:absolute`, 468 by 117 pt, `rotation:315` (diagonal) or none
  (horizontal), a negative `z-index` (behind the text), `mso-position-horizontal:center` and
  `mso-position-vertical:center`, both relative to the margin; `o:allowincell="f"`; `fillcolor`, `stroked="f"`;
  `v:fill opacity=".5"` where semitransparent; **`v:textpath style="font-family:&quot;Aptos&quot;;font-size:1pt"
  string="..."`**; `w10:wrap anchorx="margin" anchory="margin"`.

So the text path's `fitshape` comes from the shapetype and its `string` and `style` from the shape, and Word draws the
result: at a 1 pt font the text spans the box's width, which without `fitshape` it would not (it would sit along the
first part of the path). A `v:textpath` is merged with its shapetype's attribute by attribute. For `on` the probe
can't tell: the shapetype's `v:path textpathok="t"` overrides `on` by the standard, so wv30 draws either way; wv43's
`on="f"` and `textpathok="f"` probes settle it, and wv47 adds a watermark Word's own command makes (section 2).

### 1.4 The corpora's one document

real3's `16_en-ZA_num_tbl_10976` (mode 15) is the only document in the four corpora with `v:textpath` (CR §4): three
headers (3, 5, 7), each a `w:object` holding a `v:group` (158.6 by 80.2 pt, `coordsize="3007,1499"`,
`coordorigin="6761,3418"`) of a picture (`_x0000_t75`) and a WordArt:
- the watermark's shapetype 136, the shape at `left:6860;top:4460;width:630;height:180` in the group's units,
  `adj=",10800"` (the first value empty, so the shapetype's);
- `fillcolor="blue" strokecolor="blue"`, `v:stroke joinstyle="miter"`, `v:shadow color="#868686"` with no `on`;
- `v:textpath` in Arial Black 8 pt, `v-text-kern:t`, `trim="t"`, `fitpath="t"`.

**Word doesn't print any of them.** The three headers are the first-page headers (`w:type="first"`) of sections 2 to
4, and those sections have no `w:titlePg`, so a first-page header is never used whatever the section's type (they are
continuous too); Word's PDF (real3's golden, five pages) has no fill in blue and no shadow's grey. docx4j's
`HeaderFooterPolicy` likewise activates a first-page header only with `w:titlePg` (review, finding 4). So the document
is the gate's negative check, and a robust one, not an oracle for drawing. No document on this machine has VML
WordArt that Word prints but the probes; the load survey's counts (c) and (d) (OWED-QUERIES) are the only real-world
measure.

Part 4 §19.1.2.18 gives `v:shadow`'s `on` a default of true, which would draw real3's grey shadow. Office's legacy
WordArt writes `<v:shadow color="#868686"/>` on styles that have no shadow, so Word likely reads a missing `on` as
false; the round measures it (wv45).

### 1.5 What docx4j does today

- **w:pict** with a `v:textpath`: the visitor's `w:pict` branch finds no `v:textbox`, takes it for a picture
  (`createImage(IMAGE_E10)`), and `WordXmlPictureE10` logs "Couldn't find imageData!" at error level; nothing is drawn.
  The XSLTs (`docx2fo.xslt`, `docx2xhtml-core.xslt`) write "TODO: handle w:pict containing other than
  ./v:shape/v:imagedata".
- **w:object**: `vmlShape` returns the `v:group`, `hasImageData` looks at the group's own children only, and so
  "nothing to render": real3's pictures and WordArt alike.
- **No VML geometry**: nothing in docx4j evaluates a `v:formulas` or a VML `path` (CR-080 D6 left VML shapes to a later
  CR).
- **The renderer is DrawingML's** (review, finding 1): `WordArtRenderer.draw` takes the wps shape for the insets
  (`ShapeText.bodyInsets`), the box (`ShapeRenderer.draw` of its `spPr`), the text rectangle (`ShapeText.textInsets`),
  the turn (`ShapeRenderer.turn` of its `a:xfrm`) and the warp (`ShapeGeometry.textWarp` of its `prstTxWarp`), and
  `WordArtText` is built on the wps. Only `WordArtWarps.warp` is neutral: it goes by the paths' count, the preset's name
  flagging only `textCircle`.
- **No VML turn or flip in FO**: nothing in docx4j-export-fo reads a VML style's `rotation` or `flip` (`FOTextBoxes`'
  rotation is `layout-flow:vertical`'s). FOP has no z-order: it paints region-before before region-body
  (`AbstractRenderer.renderPageAreas`), so a header's watermark comes out under the body's text, and a region is
  clipped only with `overflow="hidden"`; but a header's shape in front of the text is under it too, and a body shape
  behind the text paints in document order (review, finding 8). HTML's `createHtmlImgE10` doesn't position a VML
  picture: it sits in its line.
- **Shapetypes in the tree**: Word writes a shapetype once in a part, in the first `w:pict` that uses it, later ones
  carrying only `type`; real3's are inside its groups. docx4j's JAXB keeps them (`Pict.getAnyAndAny` holds
  `CTShapetype`, `CTGroup.getEGShapeElements` the nested ones). Under CR-051's word mode an `mc:Fallback` beside a
  Choice is dropped at load, so a shapetype only in an earlier Fallback is gone; Word reads the Choice too, so nothing
  is lost, but the lookup must take an unresolved `type` (it draws nothing, as Word's save of wv20-41 shows; review,
  finding 10).
- **What exists to build on**:
  - VML placement: `XsltFOFunctions.anchorVmlPicture` and `FOTextBoxes.createVmlPictureContainer` (17.1.0), with
    `WordLayoutFixups`' page, margin and centred positions, behind and in front, in table cells (the placement
    properties are those a shape doesn't take from its shapetype: section 1.6);
  - VML colours: `PictureRecolour.vmlColour` (CR-084 phase 3), which doesn't yet read `#RRGGBB [n]` or the
    `fill darken(n)` forms (review, finding 11);
  - phases 1 to 3: the line layout, the warps by path count (`WordArtWarps.warp`: one path Follow Path, two an envelope,
    more bands), the paint (DrawingML fills and outlines), the effects (CR-081's, through `Effects.drawText`);
  - the watermark test: `InspectWalk` (CR-050) finds a watermark by the `PowerPlusWaterMarkObject` id.

### 1.6 The standard and Office's notes

ISO/IEC 29500-4 (5th edition, December 2016):
- **§19.1.2.23 `textpath`**: `on` (default false; `v:path`'s `textpathok` overrides it), `fitshape` (stretch the text
  to the shape's whole box; false: along the first part of the path), `fitpath` (size the text to fill its path),
  `trim` (remove the room for ascenders and descenders), `xscale` (a straight path instead of the shape's), `string`,
  `style`. The style's text properties: `font`, `font-family`, `font-size`, `font-style`, `font-variant`, `font-weight`,
  `text-decoration`, `mso-text-shadow`, `v-rotate-letters`, `v-same-letter-heights`, `v-text-align` (left, right,
  center, justify, letter-justify, stretch-justify; default left, which [MS-OI29500] 2.1.1812 says Office reads as
  center), `v-text-kern`, `v-text-reverse`, `v-text-spacing-mode` (tightening, tracking), `v-text-spacing` (100ths of
  single spacing, default 100). Not inherited from a shapetype: `flip`, `height`, `left`, `margin-left`, `margin-top`,
  `position`, `rotation`, `top`, `visibility`, `width`, `z-index`.
- **§19.1.2.4 `f`**: up to 128 formulas; `val`, `sum`, `product` (Word writes `prod`), `mid`, `abs`, `min`, `max`, `if`,
  `mod`, `atan2`, `sin`, `cos`, `cosatan2`, `sinatan2`, `sqrt`, `sumangle`, `ellipse`, `tan`; angles in fd (degrees ×
  2^16); each result a 32-bit integer (`product` rounds half up, `mid` toward 0, the inexact ones down); arguments
  `@n` (an earlier formula), `#n` (adj 0 to 7), constants 0 to 65535, and named values (`width`, `height`, `xcenter`,
  `ycenter`, `xlimo`, `ylimo`, `hasfill`, `hasstroke`, `lineDrawn`, `pixelLineWidth`, `pixelWidth`, `pixelHeight`,
  `emuWidth`, `emuHeight`, `emuWidth2`, `emuHeight2`).
- **§19.1.2.14 `path`**: `m l c x e t r v nf ns ae al at ar wa wr qx qy qb`, values or `@n`/`#n` references. Left open
  by the standard, and so for docx4j to state (review, finding 7): `ae`/`al` take their angles in fd and their sizes as
  semi-axes, with the sweep's sign in a y-down space; repeated `qx`/`qy` pairs alternate their tangent; repeated
  parameters repeat the command, an omitted value is 0, and `@` and the command letters delimit (`m@7,l@8,`,
  `@2@14`); a forward `@n`, a `#n` past the adj list, division by zero in `prod`, `if` at 0, 32-bit and fd overflow;
  `coordsize` 0 or absent; `nf` and `ns` for each `e`-delimited set; and values for `pixelWidth`, `pixelHeight`,
  `pixelLineWidth` and `lineDrawn` (real3's `_x0000_t75` uses them). `prod`'s half-up rounding can't be seen (1/21600
  of 144 pt is 0.007 pt), so the standard's is taken.
- **§19.1.2.18 `shadow`**: `on` (default true), `type` (single, double, emboss, perspective), `offset` (default
  2pt,2pt), `offset2`, `color`, `color2`, `opacity`, `matrix`, `origin`, `obscured`.

## 2. The round

Two sets through word-bridge, in order. Names `wv40` up (phase 0 used `wv10` to `wv30`; the bridge's staging and
portfolio folders have no `wv4`).

**`cr083-phase4a`, Word's shapetypes** (D16), one file from a word-bridge script:
- wv40-shapetypes: the 40 legacy WordArt, types 136 to 175, each "WordArt" in Aptos 36 pt as Word inserts it, one a
  page, in a mode-12 document; and in a header, a text watermark as Word's Watermark command makes it (it records as
  `Shapes.AddTextEffect` on the header's shapes); outputs docx and pdf. The docx gives Word's own 40 shapetypes (path,
  formulas, adj, `v:path`, `v:textpath`, handles), and the shape's attributes as Word's insert writes them, its default
  stroke among them (legacy WordArt is outlined by default: real3's has `strokecolor` and no `stroked="f"`); the PDF is
  Word's drawing of each at its defaults, the first oracle file. `build_vml.py harvest` reads them into
  `shapetypes-word.xml`, the round's input and the tests' fixture (D16), by each shapetype's `o:spt`, never by the
  order made: `MsoPresetTextEffectShape`'s order isn't `o:spt`'s (CascadeUp and CascadeDown are 39 and 40 but types
  154 and 155; CurveUp and CurveDown 17 and 18 for 152 and 153; CanUp and CanDown 19 and 20 for 174 and 175; the waves
  21 to 24 for 156 to 159).
- The script (review, finding 6): `Shapes.AddTextEffect` (it returns a legacy `TextEffect` shape; it is what Word's
  Watermark command records, VML even in mode 15), then `TextEffect.PresetShape` 1 to 40, `TextEffect.FontName` and
  `FontSize`, `NormalizedHeight` false; each at (144, 252) on the page, 288 by 144 pt, a label paragraph above it. If
  the save holds wps shapes, `SetCompatibilityMode 12` and again; if Word still won't, D16's route (b).

**`cr083-phase4`, the probes** (built by `build_vml.py` from `shapetypes-word.xml`, with `build_wordart.py`'s
`Vml` helpers; "WordArt" in Aptos 36 pt, a 288 by 144 pt box anchored to the page at (144, 252) as phase 0's, unless
the probe varies it; the shape's other attributes, its stroke included, as Word's insert writes them in wv40, so that
only what a probe varies differs from Word's own). About 285 probes, one a page, in seven files (resized by the
review, finding 13):
- **wv41-types** (60): each type at its default adj; again with "Word" over "Art" on two lines, for 20 of them (the
  types with more than two paths, and a sample of the others).
- **wv42-adj** (45): each type with an adj at one other value, the types with two adj a second value for the second,
  and a few out of their handle's range (does Word clamp to the handle's `xrange`/`yrange`?).
- **wv43-geometry** (32): the shape's own `path` over its shapetype's; the shape's `adj` with empty entries
  (real3's `,10800`); `coordsize` other than 21600 and a `coordorigin` not 0; a shape with no `type` and its own path
  (Word's save of wv20-42); a dangling `type`; two shapetypes with one id (which wins); the shapetype after the shape;
  a shapetype only in another part; a definition by `o:spt` only after a whole one of the same `o:spt` (phase 0's
  question, done right); `v:textpath` attributes on the shapetype only, on the shape only, and conflicting; `on="f"`;
  `textpathok="f"`; `coordsize` 0 and absent; a custom arc (`al` and `ae` swept each way: a sign error shows as
  mirrored text); five custom formulas each moving a visible line (a forward `@n`, a `#n` past the adj list, a
  division by zero in `prod`, `if` at 0, an fd overflow).
- **wv44-textpath** (57): `fitshape`, `fitpath`, `trim`, `xscale` each on and off on a two-path and a one-path type;
  a string of descenders only ("gyp") and one of capitals only ("WAVE"), each with `trim` on and off, in Aptos,
  Calibri and Times New Roman (which box the text is mapped from: section 1.1);
  `font-size` 1, 12, 36 and 72 pt on one box (the same drawing?); bold, italic; `v-text-align`'s six values on two lines
  of unequal length; `v-same-letter-heights`; `v-rotate-letters`; `v-text-kern` on and off ("AVATAR"); `v-text-spacing`
  80 and 150 in both modes; `v-text-reverse`; `text-decoration:underline` (kept, not drawn, says 2.1.1812); three
  lines; an empty string; a 60-character string; a glyph that overlaps itself (the fill rule).
- **wv45-paint** (34): `fillcolor` named, hex, `#RRGGBB [n]`, a system colour, `fill darken(118)`; `filled="f"`;
  `opacity` 0.5 and 0.2; gradients (`type="gradient"` at angles 0, 45 and 90, `color2`, `focus` 0, 50, 100 and -50, a
  `colors` list, `type="gradientRadial"` with `focusposition` and `focussize`); one picture fill (`type="tile"`);
  `stroked="f"` and Word's default stroke, `strokeweight` 0.25, 2 and 6 pt, `strokecolor`, `dashstyle`, `joinstyle`,
  `endcap`, one compound line (`linestyle="thickThin"`), stroke `opacity`; `v:shadow` with no `on` (real3's),
  `on="t"` at its defaults, offsets 4 pt and -3 pt and as a percentage, colour and opacity, one shadow on two box sizes
  (are its lengths points, whatever the text's size?), `type="double"` with `offset2` and `color2`,
  `type="perspective"` with a `matrix` and `origin`, `type="emboss"`, `obscured`; `o:extrusion on="t"` (3-D).
- **wv46-placement** (30): inline in a line (no `position`); anchored to the page, margin, column and paragraph;
  centred; `rotation` 30, 90 and 315; `flip` x, y and xy (does the text read mirrored?); wrap none, square and
  topAndBottom, behind (negative `z-index`) and in front; in a table cell; in a `v:group` with a picture (real3's
  form, in the body and in a header that prints), and in a `w:object` holding one; the same WordArt in a mode-12 and a
  mode-15 document.
- **wv47-watermarks** (26), the form of section 1.3 and the one Word's own command made in wv40, in the header of a
  page with body text: diagonal and
  horizontal; semitransparent and not; Aptos, Calibri and Times New Roman; `font-size` 1 pt (Word's "Auto") and fixed
  sizes 36, 72 and 144 pt with the box Word's dialog would give; colours; landscape and A4 pages; unequal margins (what
  it's centred on); first-page and even headers; two sections with different watermarks.

Read by `read_vml.py <results> <docxdir> <out.jsonl>`: each probe's parameters from its own docx, and from Word's PDF
its glyph paths (points, fill rule, colour, alpha), stroke, any shadow path or picture, and its placement (the paths'
box on the page); `gate_vml.py` compares docx4j's PDF with Word's probe by probe, from `gate_wordart.py`'s outline
comparison. Every file is expected silent; none goes on the stop list.

**As built** (2026-11-13, `build_vml.py`, Jason: "build the cr083-phase4 round with build_vml.py"; `probes-vml.tsv`):
8 files, 263 probes, staged as `cr083-phase4`. Since wv40 had already drawn each type at its defaults, wv41 is the 40
types on two lines; wv42 51 probes (each type's adj moved, the second value of the eight two-value types, three past
their handle's range); wv43 31, wv44 54, wv45 38, wv46 24, wv47 20; and wv48 five probes in a mode-15 document, kept
apart from wv47's sections, whose headers would draw over them. Each probe copies Word's insert form for its type (the
three forms section 6 reads), so that only what it varies differs from Word's own. A tile fill and a group's picture
use one 24 by 24 PNG. All 8 files pass `check_wordart.py`; docx4j renders each at a page a probe.

## 3. What the round answers

1. The stretch before the warp (section 1.1): with `trim` the ink box onto the shape's box; without it, which metric box
   (ascent and descent, with the line gap or not, in each face); and then whether the two paths warp it as phase 2's
   envelope does (§20.1.9.19, by arc length) and Follow Path as phase 2's (D / (D + 2T)) or by `fitpath` along the path.
2. Whether `font-size` changes anything once the text is fitted (the watermark's 1 pt says not, under `fitshape`), and
   so whether phases 1 to 3's size-bound rules mean anything here: Word's kerning of WordArt is `v-text-kern`'s, the
   line gap matters only to the box without `trim`, and phase 3's (size / 72)^0.7 has no size to take, VML WordArt
   being always on a path, where phase 3 found Word draws effects at their stated lengths (review, finding 12).
3. How the path count maps to a warp kind for each of the 40 types (one path Follow Path, two an envelope, more bands),
   and which types phase 2's 11 unexplained presets correspond to.
4. Word's merge of a shape with its shapetype (attribute by attribute, as section 1.3 shows for `fitshape`; `on`
   against `textpathok`; and for `v:fill`, `v:stroke`, `v:shadow` and `adj`), its rule for an unresolved or duplicated
   id, whether the handle's range clamps an adj, and what the edge cases of the formulas and the arcs come to
   (section 1.6).
5. The paint: VML gradients on glyphs (one gradient across the text, or a glyph each? `angle` and `focus`, -50 to 100
   and mirrored, against `a:lin`'s conventions), picture fills, Word's default stroke, the stroke's join and dash, and
   the shadow: drawn as a path (a sharp shadow is a copy of the glyphs) or a picture, its offset in points or a
   percentage, a missing `on`.
6. 3-D (`o:extrusion`): a picture of the whole, as wps 3-D WordArt (D10), or something else.
7. Placement: whether the VML picture route's measurements hold for WordArt, a turned box's place (turned about its
   centre, placed by its unturned box?), flip, and a watermark's centring relative to the margin.
8. (Answered before the round: Word fills every path even-odd, section 1.1.)

## 4. The review (2026-11-13)

A Claude Fable 5.1 session reviewed the design read-only (Jason: "commit then have fable review the design"), against
phase 0's PDFs and saves, real3's document and its golden, Part 4 and the code; it ran no Word. Its 13 findings, each
checked here before applying (the numbers of 2, 3, 4, 5 and 8 measured again), were applied the same day (Jason:
"apply all 13 to the CR and the findings note, with finding 9 as a revision to D17"):

1. **The renderer is DrawingML's** (section 1.5): the design named no way in for VML. Applied: a geometry source for
   `WordArtRenderer` (the warp's paths, the box and its drawing, the text rectangle, the turn and the fit), with a
   DrawingML source (today's code, moved) and a VML one, and a VML form of the `textCircle` flag (CR phase 4).
2. **The fit isn't phase 2's envelope**: with `trim` the ink box is stretched to the shape's box (wv20-42), without it a
   font-metric box (wv30-02) (section 1.1). Applied: the stretch is a step of the design, and wv44 has descender-only
   and capitals-only strings under `trim` on and off in three faces. The CR's "fills its parent shape's box" (§2) said
   too much.
3. **Word fills every path even-odd**, DrawingML WordArt too (section 1.1). Applied: §3 item 8 answered, wv48 dropped,
   `fill-rule="evenodd"` on WordArt's glyph paths.
4. **real3's reason is `w:titlePg`**, not continuity (section 1.4). Applied.
5. **wv30's watermark is the builder's copy, not Word's dialog's**, and it can't tell a merged `on` from
   `textpathok`'s override (section 1.3). Applied: wv40 makes a watermark by Word's own command, wv43 settles `on`.
6. **D16 (a) is feasible** (about 75% sure, from Word's object model as documented), with two cautions: harvest by
   `o:spt`, not by `MsoPresetTextEffectShape`'s order; retry in mode 12 if the save holds wps shapes. Applied to wv40's
   script (section 2).
7. **The evaluator's rules** the standard leaves open (section 1.6), and its result: `e`-delimited sets with their
   `nf`/`ns` flags and the text box rectangle, if CR-080 D6's CR is to reuse it, not "each sub-path an outline".
   Applied: the CR's geometry step, and wv43's arc and formula probes.
8. **FO placement**: sound, but nothing reads a VML `rotation` or `flip`, and FOP has no z-order (section 1.5).
   Applied: the container keeps the unturned box, which Word places, the SVG at the drawing's bounds inside it,
   overflow visible, as `FOShapes.addWordArt` does; z-order stated as a limitation; HTML's VML WordArt inline.
9. **D17's test should follow its argument**: repetition on every page holds for any VML WordArt in a header or
   footer part, and a renamed or hand-copied watermark lacks the `PowerPlusWaterMarkObject` id (LibreOffice and Aspose
   write it, a hand copy needn't). Applied as a revision to D17: by the part, not the id.
10. **Shapetype lookup** by part, groups included; an `mc:Fallback` dropped under word mode (section 1.5). Applied to
    `VmlShapes`.
11. **D18 and D19's gaps**: nested groups; a group's children placed by `left` and `top` through `coordorigin`
    (`createVmlContainer` reads `margin-left` and `margin-top` only); a group's picture needs its own entry, the picture
    route taking a `w:pict`; VML gradients' `angle` and `focus` conventions; `#RRGGBB [n]` and `fill darken(n)`;
    legacy WordArt's default stroke (the probes now copy Word's insert); a shadow's offset as a percentage. Applied to
    D18, D19 and the round.
12. **Phases 1 to 3's size-bound rules under fit**: kerning is `v-text-kern`'s; the line gap matters only without
    `trim`; (size / 72)^0.7 has no size to take, and VML WordArt is always on a path, where Word draws effects at their
    stated lengths. Applied: `v:shadow`'s lengths are points, wv45 checks a shadow on two box sizes.
13. **The round's size**: cut wv48, wv42 to about 45, wv45's picture fills and compound lines to one each, wv41's two
    lines to 20 types; add findings 2, 5 and 7's probes. About 285 probes. The effort was tight: phase 4 is now eight
    days.

## 5. Built before the round (2026-11-13)

Jason: "commit then start implementation", on 779c69303. The parts that don't wait on the round, built and tested; the
round (`cr083-phase4a`, then `cr083-phase4`) decides what is marked provisional below.

**Where the design changed in the building:**
- `VmlGeometry` is in docx4j-core (`org.docx4j.model.shapes`, beside CR-080's `ShapeGeometry`, whose `ShapeOutlines`
  it returns), not in docx4j-chart-render: the geometry engine is core's.
- The geometry source (review finding 1) is a second entry, `WordArtRenderer.drawVml`, beside `draw`, the review's
  alternative: it shares the line layout, the warp (`WordArtWarps.warp`, by the paths' count), the paint and the
  effects, with three hooks: a VML run is `fitted` (its effects take no room in its fixed box), a two-path warp can
  take a frame down in place of the lines' ink (the stretch without `trim`), and the turn is the style's `rotation`
  and `flip` about the box's centre. DrawingML's `draw` is unchanged, so the corpora can't move by it.
- `fill-rule="evenodd"` (review finding 3) isn't applied: the renderer joins a run's glyphs into one path so that a
  gradient spans the run, and even-odd on that path would cut holes where two glyphs overlap (a script face, tight
  kerning), which Word's one path a glyph never shows. Left until wv44's self-overlapping glyph shows what Word's PDF
  does; an even-odd fill a glyph is the change then.

**Built:**
- docx4j-core: `VmlGeometry` (the 18 operations with Part 4's rounding, the path's commands, `e`-delimited sets with
  `nf`/`ns`, the text box rectangle, the named values, the bounds of section 1.6, `subPaths` for the text paths);
  `VmlShapes` (a part's shapetypes indexed, groups included, cached by its root; a shape's attributes and children
  read over its shapetype's one attribute at a time; `isTextPath`); `VmlWordArt` (the model: a paragraph a line of
  `string`, the style's face, size, weight, slant and kerning, `v-text-align` with Office's center; `v:fill` and
  `v:stroke` as DrawingML, legacy WordArt's default stroke drawn; `v:shadow` as a sharp outer shadow; `o:extrusion`
  reported flat); `VmlColours` (shared with `PictureRecolour`, which now also reads system colours); `WordArtText`'s
  `getVml()` and `Run.isFitted()`; `ShapeDrawer.drawVmlWordArt` (HTML's way to the renderer).
- docx4j-chart-render: `WordArtRenderer.drawVml` and its hooks (`VML_EM_ABOVE`, `vmlTurn`); `ShapeSvgDrawer`.
- Routes: the FO visitor and XSLT (`FOShapes.vmlWordArt`, `XsltFOFunctions.createVmlWordArt`, `docx2fo.xslt`'s
  `w:pict` and `w:object` templates), placed by the VML picture container; a group holding WordArt (D18) placed by its
  own style, each WordArt and picture in a container at its box through the group's coordinates, other shapes and a
  turned group reported (`render.shape.vml`); HTML's visitor and XSLT (`HtmlShapes.vmlWordArt`, `docx2xhtml-core.xslt`),
  in its line, nothing from a header or footer (D17). The visitor no longer descends into a `w:pict` whose WordArt it
  drew (its `v:shapetype`, `v:path` and `v:textpath` were warned of as "Need to handle", and at debug level a
  `v:shapetype`'s marshalling threw).

**Phase 0's VML probes through docx4j** (`wv20-vml-spt-mode12`, `wv30-watermarks`, with Word's Aptos):
- wv20-01 to -41 (only an `o:spt`, or an unresolved `type`): nothing, as Word;
- wv20-42 (the slanted path, `trim`): ink 144.00 to 432.00 by 252.00 to 396.00, Word's 144.00 to 431.96 by 252.00
  to 395.92; the text again invisibly (mode 3);
- wv30-02 (horizontal, no `trim`): 63.65 to 531.65 by 369.56 to 446.46, Word's 63.73 to 531.56 by 369.58 to 446.49
  (the em-box rule was fitted to this probe, so it is no test of it); silver at alpha .5019608, Word's .50196;
- wv30-01 and -03 (turned 315 degrees): the same size as Word's to 0.1 pt ("WATERMARK" 385.3 by 372.7, Word's 385.2
  by 372.6) but 14.3 and 15.5 pt higher. Not a rotation-centre error of docx4j's alone: the offset differs with the
  text, so Word turns or places a turned VML shape by something docx4j doesn't model. For wv46's and wv47's turned
  probes; open.
- No watermark text in the PDF, as Word; red `#c00000` opaque, as Word.

**Provisional until the round** (each a constant or a branch to change): the arc's angle direction
(`VmlGeometry.ANGLE_SIGN`, GDI's); the no-`trim` em box (`WordArtRenderer.VML_EM_ABOVE` = 0.718, one probe); Follow
Path as phase 2's, a closed single path as `textCircle`; a `v:shadow` with no `on` off; a gradient's angle (0 blending
down) and focus; a picture fill drawn in its colour, a compound line single, a double or perspective shadow single,
emboss not drawn, all reported; `v-text-spacing` not applied; Arial and 36 pt where the style gives none; two
shapetypes of one id, the first; `on` needed whatever `textpathok`; a `coordsize` side 0 or absent, 1,000.

**Not built:** the turned placement above; HTML for a group's WordArt (nothing, as before); the evaluation of Word's
40 shapetypes against Word's drawing of them (wv40, awaited).

**Tests:** `VmlGeometryTest` (17), `VmlShapesTest` (6) in core-tests; `VmlWordArtRendererTest` (9, HTML both routes
included) in chart-render; `VmlWordArtFoTest` (4, both routes: a header watermark with no text, body WordArt with
invisible text, a shapetype with only its `o:spt`, a group) in export-fo-tests. core-tests 2,676, chart-render 102,
export-fo 111, export-fo-tests 971 pass.

**Gate b471** against b464: 0 movers, events equal; the corpora's PDFs as b470's but for the two time fields (5361,
11875), real3's VML WordArt document among the same (its three WordArt still not drawn, as in Word: section 1.4); the
probes' 324 the same. **b472** (the turned watermarks' placement, section 6) and **b473** (Follow Path, section 6; the
last build) likewise: 0 movers, events equal, the PDFs as b471's but for the time fields, the probes the same.
chart-render's tests 103 with `onePathTheTextGoesAlongIt`.

## 6. Word's shapetypes: cr083-phase4a (2026-11-13)

The word-bridge script `legacy-wordart` (written by the VM session, installed by Jason; D16 (a)) ran as job wv40:
silent, script exit 0, 15.8 s, Word 16.0.20326.20318, mode 12 legacy at the first try. `wv40-shapetypes` holds 40
`v:shape`, each with its own `v:shapetype`, `o:spt` 136 to 175 once each; `wv40-watermark` a text watermark made by
`Shapes.AddTextEffect` in the header (not Word's recorded Watermark macro: its leftover `v:shadow color` and a
`v:stroke r:id` to a JPEG pattern are COM's). The definitions are in `shapetypes-word.xml` (by `o:spt`).

**What Word's own form is** (`harvest.py`'s read of the docx): the shape gives only `type` and `style` (and its text
path), so VML's defaults hold, and Word's PDF draws them: the glyphs filled white and stroked black at 0.75 pt with
round joins. The text path's `style` is the face and `v-text-kern:t`, no `font-size`; `trim="t" fitpath="t"`. Each
shape carries `<v:shadow color="#868686"/>` with no `on`, and Word draws no shadow (its PDF's fills are all white):
**a missing `on` is off**, as section 5 took it. The shapetypes' text paths: 136 to 143 and 148 to 175 `fitshape`
(152, 153, 156 to 167 `xscale` too); 144 to 147 (the curves: Arch Up, Arch Down, Circle, Button) `fitpath` with
`v-text-kern`. Their paths: two sub-paths for most; `al` arcs with formula angles for 144 to 151 (adj in fd, 144's
11796480 = 180 degrees); 147, 151 three or four; 166 four and 167 six (bands).

**docx4j against Word's PDF, each type at its defaults** (`cmp40.py`, the glyphs' ink box): 36 of the 40 within 0.7 pt
(30 within 0.2), from Word's own definitions with no change to the code: the two-path types, the bands (166, 167),
the pour types' arcs (148 to 151, within 0.2 pt), which settles the arcs' direction (`ANGLE_SIGN`, GDI's). The four
Follow Path types (144 to 147) were wrong by 110 to 285 pt: Word lays the text along the whole path, its ink reaching
40 pt past the box (144: 103.7 to 461.8 across for a box of 144 to 432), where phase 2's DrawingML rule (D / (D + 2T))
shrinks it. Read from Word's glyph by glyph paths (`glyphs.py`): each glyph upright to the path, its middle at its
advance, the text sized to the path's length; the glyphs' centres 1.03 to 1.05 of the ellipse, so it is a line through
the glyphs, not their baseline, that is on the path. What decides it is the number of text paths, not `fitshape`:
Word's own curve shapes carry `fitshape="t"` (the shapetype `fitpath`) and draw along their paths, so docx4j lays the
text along each path where there is an odd number (as DrawingML's one-path presets), sized by `fitpath`
(`WordArtWarps.along`), the line `VML_ALONG_RISE` = 0.22 em above the baseline on the path (fitted: 0.2 gives 2.0,
1.7 and 3.6 pt on 144, 145, 146; 0.22 gives 0.3, 0.2 and 1.7). With one line Button (147) takes its first path, as
Word's PDF has it (the same page as 144). **All 40 types within 1.7 pt of Word's ink (39 within 0.7 pt, median
0.1 pt).** Provisional: one face, one line; the round's faces and lines decide.

**Turned watermarks**: Word centres the turned ink's bounding box down the box (wv30-01, -03 and wv40-watermark, all
within 0.3 pt), while across its turn about the box's centre is docx4j's (within 0.55 pt). docx4j now does so
(`drawVml`: a trial paint, then the turn moved down by the ink's offset; provisional, for wv46's turns and other
anchors): wv30-01 within 0.23 pt, wv30-03 0.70, wv40-watermark 0.31; wv30-02 (unturned) as before.

## 7. What the round found, and what changed (2026-11-13)

cr083-phase4 ran on bridge 22479f7, Word 16.0.20326.20318, 165 s: all 8 files and both controls silent, wv43 too
(Word rewrote its odd geometry on save: 31, 61 and 30 `v:shape`, `v:textpath` and `v:shapetype` in, 35, 59 and 24 out,
each shape's effective geometry written on it). Jason: "go ahead with reading each of these against Word's per-glyph
paths and saved files, fixing them, writing findings §7, then a gate". The measure (`cmpall.py`): each page's filled
paths' box, Word's against docx4j's; per glyph where it mattered (`glyphs.py`, `sets.py`, Word's save).

**Before and after**, of 263 probes: within 0.7 pt 182, then 223; 0.7 to 3 pt 23, then 13; past 3 pt 39, then 10;
both drawing nothing 14, then 15; one drawing nothing 5, then 2. Word's 40 shapetypes (wv40) within 1.5 pt (1.7); phase
0's VML probes within 0.2 pt (0.7); the watermarks (wv47) 19 of 20 within 0.4 pt.

**What Word does, and docx4j now does:**
- **The default `coordsize` is 21600**, not the standard's 1000: Word saves a shapetype without one as 21600,21600 and
  draws one of 0,0 alike (wv43-14, -15: a path in 1000s 13.3 by 6.7 pt). `VmlGeometry.DEFAULT_COORDSIZE`.
- **`prod` by 0 is its first argument** (wv43-23: the line at 5400). 
- **An adj is held to its handle's range** (`xrange`, `yrange`): wv42-49 (136's 23313, its range 6629 to 14971) drawn
  as 14971; Word's save keeps 23313. `VmlShapes.Resolved.getAdj`.
- **`on` and `textpathok` don't stop it**: Word draws a shape whose text path says `on="f"` (wv43-11) and one whose
  shapetype's `v:path` says `textpathok="f"` (wv43-12; its save drops the attribute). WordArt is a text path with text;
  a shape with no path draws nothing (wv20, wv43-06, -30). `isTextPath`.
- **`nf` and `ns` reach the text**: a set that says `nf` leaves the text unfilled, `ns` unstroked (wv43-28: one each,
  nothing drawn). `VmlWordArt.paintBySets`.
- **`fitshape="f"` lays the text along the first path**, sized to it by `fitpath` (wv43-13, wv44-01: along 136's top
  line, 285.6 pt of 288), as Part 4 says.
- **Without `trim`**, Word maps the text's advance across (side bearings kept: "gyp" in Aptos 150.5 to 422.0 where its
  ink, trimmed, is the box) and down an em box whose foot is the baseline plus the face's larger descent
  (max(usWinDescent, hhea descender): Aptos 0.282, Calibri 0.269, Times New Roman 0.216; phase 0's single 0.718 was
  Aptos's), its top an em above (wv44-41 to -51: within 0.2 pt in all three faces, 5.6 to 10 pt before).
  `Run.getEmDescent`, `WordArtRenderer.emBox`.
- **Follow Path's line is the em box's middle** on the path, 0.5 - emDescent above the baseline (Aptos 0.218, wv40's
  fitted 0.22): Calibri within 0.3 pt, Times New Roman 1.1 (1.3 and 5.2 with 0.22). `WordArtWarps.rise`.
- **Several lines on Follow Path**: one scale for them all, the least of a band's path's length over its longest line;
  each line aligned along its path; a band's lines moved apart along the path's normal by their places in the block,
  the middle of their rise points on the path (wv43-29: three paths, three lines, one size, within 0.4 pt; wv41-09
  and -10: two lines on Arch Up and Arch Down, "Two lines" filling the arch inside it and "WordArt" centred outside,
  within 0.7 and 0.2; Button 0.2). They are placed by the path's own length and moved, not laid on offset curves.
- **A VML shadow is a copy of the glyphs**, filled and unstroked, drawn under them, not an effect picture (wv45: 7
  shadow paths and 7 glyph paths a probe): gray (`#808080`) at 2pt,2pt by default; an offset a percentage of the
  shape's width and height (10%: 28.8 and 14.4 pt); a double shadow's `color2` copy at `offset2` first, then the
  first; `obscured` as single. Emboss (a light and a dark copy) and perspective (a squashed one) not drawn, reported.
  `VmlWordArt.shadows`; `drawVml` draws them from a trial paint's glyph paths.
- **A turn, then the mirror**: Word turns a flipped shape and then mirrors it, the glyphs too (wv46-09 to -11: flipped
  across, "WordArt" reads from the right; wv46-12, turned 30 and flipped, is the unflipped turn's mirror). `vmlTurn`.
- **A turned shape's ink is centred down its box only where the shape is centred down its reference** (the
  watermarks: `mso-position-vertical:center`); one placed at an offset turns about its box's centre alone (wv46-08,
  wv47-16: section 6's rule applied to them had put them 2.8 and 14.1 pt off). `drawVml`'s `centredDown`.

**Open, and why:**
- wv47-14 (a watermark on unequal margins: top 36, bottom 108): 12.4 pt low in docx4j's terms; its equal-margin
  siblings are within 0.4. Likely the header's own extent moving the top margin Word centres on; not measured.
- wv41-16 (151, Button Pour, two lines): 66 pt; the pour types' two-line bands are phase 2's open `textButtonPour` too.
- wv41-11 (Circle, two lines): 6.6 pt; a closed path's two lines.
- wv43-07 (two shapetypes of one id): Word's save keeps the first, as docx4j does, yet its ink starts 17.7 pt lower.
- wv43-27 (`qb` with a control point above the box): Word draws nothing; docx4j draws.
- wv43-31 (the text path's style on the shapetype only): 7.2 pt.
- wv44-26 (`v-same-letter-heights`): not drawn (2.4 pt).
- wv45-17 (a tile picture fill: Word draws the picture, docx4j the colour, reported), wv45-26 (a compound line drawn
  single, reported), wv45-35 (emboss, reported), wv45-38 (`o:extrusion`, drawn flat, reported: Word draws 288 paths of
  3-D).
- wv46-22 (a turned group): drawn unturned, reported (D18).
- wv46-18 (a table cell): the box measure takes in the cell's borders, which Word draws as filled paths; not read.
- Small residuals of 0.8 to 1.6 pt: six adjusted types (wv42-03, -09, -12, -17, -20), 156's second adj (4.9), `qx`/`qy`
  (1.3), a WordArt in its line (1.1), centred across (0.8) and down the page (1.5), 171 on two lines (0.9).

**Gate b475** (this build) against b464: 0 movers, events equal, the corpora's PDFs as b473's but for the two time
fields (5361, 11875), the probes' 324 the same. (b474, the same build, showed one mover: real3's
15_es-ES_fields1_num_tbl_2432 scored 0 for "no result within 120s", the scorer timing out while Maven's suites ran
beside it, its PDF the same as b473's; run again alone, b475.) Tests: core-tests 2,678 (`VmlShapesTest` 8, the clamp and
on="f"), chart-render 104 (`VmlWordArtRendererTest`: the shadow copies, the turn's order), export-fo 111, export-fo-tests
971 pass.

## 8. The review of the implementation (2026-11-13)

A Claude Fable 5.1 session reviewed 76635c0f9 and fb8390dbd read-only, with Word's results of the round, Word's saves
and docx4j's renders (Jason: "commit then have fable conduct a review including a look at those unexplained misses").
Each finding was checked before applying (Jason: "apply them"):

1. **The shapetype cache held every converted document** (must): a static `WeakHashMap` keyed by the part's root whose
   index's shapetypes lead back to the root by their parents, so no entry was ever freed. Now the conversion's own
   cache (`AbstractWmlConversionContext.getVmlShapetypes`, `VmlShapes.resolve(shape, root, cache)`); with none, the
   part is indexed for the call.
2. **Two shapetypes of one id: Word draws the last** (must): wv43-07's glyph tops rise as the second definition's top
   line does, though Word's save writes the first; docx4j took the first. `Index.add` keeps the last. (wv43-07 within
   0.2 pt, 17.7 before.)
3. **A builder bug** (should): wv43-08's probe captured its shapetype's id after it had moved on, so its shape pointed
   at an id no shapetype had (the question "a shapetype after its shape" untested), and wv43-31 held two shapetypes of
   one id, the first slanted, which docx4j drew. Fixed (`build_vml.py` binds the id); wv43-31 within 0.1 pt now (7.2),
   Word's save confirming the text path's style merged from the shapetype; the question is in cr083-phase4b.
4. **Gradients' angle is in the box's own square** (should): Word's 45 degrees in a 2:1 box is 63.4 on the page
   (wv45-10's shading from (230.5, 208.8) to (345.5, 439.1)); `a:lin scaled="1"`. The box measure read no gradient
   (Word draws a shading, not a filled path); the focus probes are still unread.
5. **The unequal-margins watermark** (wv47-14): Word centres between the body's top, where the header runs past the
   top margin (35.4 pt down and a line: the body at about 60.6 pt), and the bottom margin: its ink centre 397.3 =
   (60.6 + 733.9) / 2. Not applied: the VML picture container is placed when the FO is written, before the header's
   height is measured (the fork measures it while it lays the page out), so the fix is the placement's, for any VML
   shape centred on the margin in a page whose header overflows; open. cr083-phase4b has its sibling with the header
   inside the margin.
6. **An unresolved type indexed the part again each time** (should): a miss is now remembered, the part indexed again
   once.
7. **`v-same-letter-heights` stretches each lowercase glyph to the cap height** (should; a 37 pt miss the box measure
   saw as 2.4): `WordArtRenderer.sameHeights`, before the stretch; the probe's glyph tops now Word's.
8. **The no-trim box reached only two paths** (could): bands too, now (`WordArtWarps.vmlBands`).
9. **Dead code** (could): `drawVml`'s `closed` and the shadow's discarded `a:effectLst` gone; the em box always four
   values.
10. **`PictureRecolour` now reads system colours** through `VmlColours` (could): kept, noted (a picture's chromakey of
    `windowText`, null before).
11. **The XSLTs tested the shape's own text path only** (could): they also find it on the shapetype now.

**The misses it explained, and what changed:** the pour types' two lines (wv41-16, 66 pt): Word lays each band's line
at one scale across the bands, centred on its band, not stretched over the arch; `vmlBands`, within 0.3 pt.
wv43-07 and -31 (findings 2 and 3). wv46-18 (the table cell): read without the cell's borders, within 0.9 pt. Not
found: wv41-11 (Circle, two lines: one glyph's reach), the small residuals. `qb` (wv43-27): Word keeps the path and draws
nothing; one probe can't say whether `qb` or its control point outside the box is the cause: cr083-phase4b.

**Over-fitted rules, and the probes for them** (cr083-phase4b, 17 probes, Jason: "can we do a probe for qb?"): `qb`
inside the box and on the bottom path; `prod 5400 3 0`; the shapetype after its shape; one id before and after its
shape (nearest or last); a trapezoid (x between paths of unequal span); Button Pour on one long line and with the wider
line first; Arch Up and Circle with the wider line first; a shadow on a turned shape; `v-same-letter-heights` with
descenders; a watermark aligned to the bottom, one with lopsided ink, and unequal margins with the header inside them.

**Round after the review** (`cmpall.py`): within 0.7 pt 226 of 263 (223), past 3 pt 7 (10). Gate b476 against b464:
0 movers, events equal, PDFs as b475's but for the time fields, the probes the same. Tests: VmlShapesTest 10 (the last
id wins, the conversion's cache), chart-render 104, the FO WordArt tests 8.

## 9. The settling round: cr083-phase4b (2026-11-13)

Two files (`build_vml.py`'s cr083-phase4b set, `probes-vml-phase4b.tsv`): wv49-settle, 13 probes in mode 12, and
wv50-settle-marks, 4 watermarks in mode 15; all silent on bridge 22479f7 (Word 16.0.20326.20318), with the control
cr083m-mu-control-a/-b. Read as section 7's round was: the page's filled paths' box against docx4j's
(`cmpall.py`), per glyph where it mattered, and Word's saves. Before: 9 of 17 within 0.7 pt, one at 0.8, Word drawing
nothing for four that docx4j drew, three past 3 pt (17.9, 5.2, 114). After: 12 within 0.7, one at 0.8 (wv49-11), the
four blank in both.

**What Word does:**

- **`qb` anywhere in the path: nothing** (wv49-01, control point inside the box; -02, `qb` on the bottom path only, the
  top straight; -03, above the box, wv43-27 again). Not the glyphs, not the shape's stroke: no path on the page. Word's
  save keeps the path (copied onto the shape too, `l` written as `r`). So it is `qb` that stops it, not where its
  control point is. `VmlWordArt.isUndrawn`, read by `WordArtRenderer.drawVml` before the geometry: an empty drawing,
  nothing reported, as for a shape with no path. Left to `VmlGeometry` as it was, since VML other than WordArt is
  unmeasured.
- **A shapetype is read in the part's order: the last of its id before the shape.** wv49-05 (the shapetype after its
  shape in the same `w:pict`): Word draws nothing, and its save writes the shape with no `type` and no path. wv49-06
  (one id before the shape, again in a later `w:pict`): Word draws the first's (its "W" rises to 252 at the left, the
  first's top line down to the right), and its save points the shape at the first. With wv43-07 (two before the shape:
  the second) that is the last before it. `VmlShapes.Index` numbers a part's shapetypes and shapes as it walks them,
  groups included, and `get(type, shape)` takes the last of the id before the shape; a copy of the shape (the XSLT
  routes unmarshal their node) is found by its `id`, `o:spid`, `type` and `style`; a shape not found, the last. A
  shapetype no shape follows in its `w:pict` Word saves as a hidden 50 pt `v:shape` (`_x0000_s1041`, `_x0000_s1039`:
  wv49's "three shapes with no type" are these two and wv49-05's), which draws nothing.
- **`prod 5400 3 0` is 5400** (wv49-04, within 0.1 pt): a divisor of 0 gives the first argument, as section 7 had it,
  not `v` times the second.
- **`v-same-letter-heights`: every glyph's ink the box's height, across by the advance** (wv49-13 "Gyp ax": G, y, p, a
  and x each 252.0 to 395.9; their widths each 0.0485 pt a unit, 288 / 5,936, the line's advance; the G's left side
  bearing kept, its ink from 149.2). wv44-26 ("Wordart") agrees: its ink ends at 429.6, the last advance at 432, where
  section 8's rule, lowercase to the cap height and the ink across, left it 2.4 pt long. With trim alone the ink is
  mapped across (wv44-40, "gyp": 144.0 to 431.9, the g's side bearing cut). `WordArtRenderer.sameHeights` (each glyph's
  ink stretched to the cap height from the baseline) and `WordArtWarps.advanceInkBox` (the advance across, the ink
  down); the frame a warp maps is now a rule over a set of lines, so a band takes the same rule for its own lines.
  Without trim, the em box (unmeasured).
- **A turned shape aligned down its reference is aligned by its ink** (wv50-01, aligned to the bottom of the margin:
  the ink's foot at 769.6, the margin's at 769.9; docx4j's, turned about the box's centre, was 114 pt lower and ran off
  the page). Section 6's centring (the ink centred down the box when `mso-position-vertical:center`) is one case of
  it: `drawVml` takes the alignment, `center` the ink's centre at the box's, `bottom` its foot at the box's, `top` its
  top at the box's (by symmetry; unmeasured). One placed at an offset is still turned about its box's centre (wv46-08,
  wv47-16).
- **Held, as section 7 and 8 had them:** a trapezoid (wv49-07, paths of unequal span: within 0.1 pt); Button Pour on
  one long line and with the wider line first (wv49-08, -09: 0.3, 0.7); Arch Up and Circle with the wider line first
  (wv49-10, -11: 0.2, 0.8); a shadow on a turned shape, moved on the page after the turn (wv49-12: 0.1); a two-line
  watermark whose ink is lopsided in its box, centred by its ink (wv50-02: 0.4); unequal margins with the header inside
  the top margin, diagonal and level (wv50-03, -04: 0.2, 0.0), so wv47-14's 12.4 pt is the header that overflows the
  margin, as section 8 (finding 5) took it.

**The round again** (`rerun.sh`, Word's 263): within 0.7 pt 227 (226), 16 blank in both (15): wv43-27 (`qb`) now blank
as Word's, wv44-26 0.1 (2.4). Word's 40 shapetypes as before (worst 1.5). Still open: wv47-14 (a watermark centred on
the margin under a header that overflows it: the placement is written before the header's height is known); wv41-11
(Circle on two lines, 6.6 pt, where wv49-11 with the wider line first is within 0.8); wv42-26 (o:spt 156's second adj,
4.9); the table cell (wv46-18, measured without the cell's borders); the reported gaps (3-D, emboss, a compound line,
a tile picture, a turned group); the gradient focus probes, unread. Unmeasured: `top` alignment, `v-same-letter-heights`
without trim, `qb` outside WordArt.

Tests: `VmlShapesTest` (a shapetype after its shape, the one before the shape, a copy found in the part),
`VmlWordArtRendererTest` (`qb` draws nothing, same letter heights, a turned shape's ink aligned).
