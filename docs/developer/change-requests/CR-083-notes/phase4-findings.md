# CR-083 phase 4: VML WordArt (design measurements, then findings)

Designed 2026-11-13 (Jason: "start phase 4", "write up a design for phase 4"), on docx4j 1a1b555b1. Section 1 is what
is known before the round: phase 0's VML probes read again (Word's PDFs and saves of `wv20-vml-spt-mode12` and
`wv30-watermarks`, the round of 2026-11-12), the one real document with VML WordArt, docx4j's code, and the standard.
Section 2 is the round the design needs; section 3 the questions it answers. The design itself is in the CR (phase 4,
and D16 to D19). Later sections hold what the phase finds.

## 1. What is known before the round

### 1.1 Word's PDF of VML WordArt

- **Glyphs as filled paths, and no text at all.** wv20-42 (type 136 with its top line slanted, "WordArt", Aptos 36 pt,
  black) is seven `fill_path`s, one a glyph, each `winding="eofill"`. The page's text is its two labels, and nothing of
  the WordArt, visible or invisible. wv30's watermarks likewise: the glyphs filled, in silver (`.753`) at alpha
  `.50196` where `v:fill opacity=".5"` (128 of 255), and not in the page's text. So Word writes no invisible text for
  VML WordArt, where it does for DrawingML WordArt drawn as outlines (phase 0 findings §1).
- **The fill rule is even-odd** on both. Phase 0 didn't record the rule of a wps WordArt's glyph paths; the round reads
  both (a self-overlapping glyph, section 2).

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

Word's own form (wv30, as Word 365 saves its Watermark dialog's output, in each header):
- the shapetype 136 whole: 14 formulas, `adj="10800"`, `path="m@7,l@8,m@5,21600l@6,21600e"` (two lines, the top and
  the bottom of the text), `v:path textpathok="t"`, **`v:textpath on="t" fitshape="t"`**, a handle;
- the shape `PowerPlusWaterMarkObject<n>`: `position:absolute`, 468 by 117 pt, `rotation:315` (diagonal) or none
  (horizontal), a negative `z-index` (behind the text), `mso-position-horizontal:center` and
  `mso-position-vertical:center`, both relative to the margin; `o:allowincell="f"`; `fillcolor`, `stroked="f"`;
  `v:fill opacity=".5"` where semitransparent; **`v:textpath style="font-family:&quot;Aptos&quot;;font-size:1pt"
  string="..."`**; `w10:wrap anchorx="margin" anchory="margin"`.

So the text path's `on` and `fitshape` come from the shapetype and its `string` and `style` from the shape: a
`v:textpath` is merged with its shapetype's attribute by attribute, and Word draws the result. With `fitshape` and a
1 pt font, the text fills the box, as [MS-OI29500] 2.1.1812 says.

### 1.4 The corpora's one document

real3's `16_en-ZA_num_tbl_10976` (mode 15) is the only document in the four corpora with `v:textpath` (CR §4): three
headers (3, 5, 7), each a `w:object` holding a `v:group` (158.6 by 80.2 pt, `coordsize="3007,1499"`,
`coordorigin="6761,3418"`) of a picture (`_x0000_t75`) and a WordArt:
- the watermark's shapetype 136, the shape at `left:6860;top:4460;width:630;height:180` in the group's units,
  `adj=",10800"` (the first value empty, so the shapetype's);
- `fillcolor="blue" strokecolor="blue"`, `v:stroke joinstyle="miter"`, `v:shadow color="#868686"` with no `on`;
- `v:textpath` in Arial Black 8 pt, `v-text-kern:t`, `trim="t"`, `fitpath="t"`.

**Word doesn't print any of them.** The three headers are the first-page headers of sections 2 to 4, all continuous,
so no page shows them: Word's PDF (real3's golden, five pages) has no fill in blue and no shadow's grey. So the
document is the gate's negative check (docx4j draws none of the three either, which rests on its header selection for
continuous sections), not an oracle for drawing. No document on this machine has VML WordArt that Word prints but the
probes; the load survey's counts (c) and (d) (OWED-QUERIES) are the only real-world measure.

Part 4 §19.1.2.18 gives `v:shadow`'s `on` a default of true, which would draw real3's grey shadow. Office's legacy
WordArt writes `<v:shadow color="#868686"/>` on styles that have no shadow, so Word likely reads a missing `on` as false;
the round measures it (wv45).

### 1.5 What docx4j does today

- **w:pict** with a `v:textpath`: the visitor's `w:pict` branch finds no `v:textbox`, takes it for a picture
  (`createImage(IMAGE_E10)`), and `WordXmlPictureE10` logs "Couldn't find imageData!" at error level; nothing is drawn.
  The XSLTs (`docx2fo.xslt`, `docx2xhtml-core.xslt`) write "TODO: handle w:pict containing other than
  ./v:shape/v:imagedata".
- **w:object**: `vmlShape` returns the `v:group`, `hasImageData` looks at the group's own children only, and so
  "nothing to render": real3's pictures and WordArt alike.
- **No VML geometry**: nothing in docx4j evaluates a `v:formulas` or a VML `path` (CR-080 D6 left VML shapes to a later
  CR).
- **What exists to build on**:
  - VML placement: `XsltFOFunctions.anchorVmlPicture` and `FOTextBoxes.createVmlPictureContainer` (17.1.0), with
    `WordLayoutFixups`' page, margin and centred positions, behind and in front, in table cells (the placement
    properties are those a shape doesn't take from its shapetype: section 1.6);
  - VML colours: `PictureRecolour.vmlColour` (CR-084 phase 3);
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
- **§19.1.2.14 `path`**: `m l c x e t r v nf ns ae al at ar wa wr qx qy qb`, values or `@n`/`#n` references.
- **§19.1.2.18 `shadow`**: `on` (default true), `type` (single, double, emboss, perspective), `offset` (default
  2pt,2pt), `offset2`, `color`, `color2`, `opacity`, `matrix`, `origin`, `obscured`.

## 2. The round

Two sets through word-bridge, in order. Names `wv40` up (phase 0 used `wv10` to `wv30`; the bridge's staging and
portfolio folders have no `wv4`).

**`cr083-phase4a`, Word's shapetypes** (D16), one file:
- wv40-shapetypes: the 40 legacy WordArt, types 136 to 175, each "WordArt" in Aptos 36 pt as Word inserts it, one a
  page, in a mode-12 document; outputs docx and pdf. The docx gives Word's own 40 shapetypes (path, formulas, adj,
  `v:path`, `v:textpath`, handles), and the shape's attributes as Word's insert writes them; the PDF is Word's drawing of
  each at its defaults, the first oracle file. `build_vml.py harvest` reads them into `shapetypes-word.xml`, the
  round's input and the tests' fixture (D16).

**`cr083-phase4`, the probes** (built by `build_vml.py` from `shapetypes-word.xml`, with `build_wordart.py`'s
`Vml` helpers; "WordArt" in Aptos 36 pt, a 288 by 144 pt box anchored to the page at (144, 252) as phase 0's, unless
the probe varies it). About 300 probes, one a page, in eight files:
- **wv41-types** (80): each type at its default adj; again with "Word" over "Art" on two lines.
- **wv42-adj** (60): each type with an adj, one other value (two for the types with two), and one out of its handle's
  range (does Word clamp to the handle's `xrange`/`yrange`?).
- **wv43-geometry** (25): the shape's own `path` over its shapetype's; the shape's `adj` with empty entries
  (real3's `,10800`); `coordsize` other than 21600 and a `coordorigin` not 0; a shape with no `type` and its own path
  (Word's save of wv20-42); a dangling `type`; two shapetypes with one id (which wins); the shapetype after the shape;
  a shapetype only in another part; a definition by `o:spt` only after a whole one of the same `o:spt` (phase 0's
  question, done right); `v:textpath` attributes on the shapetype only, on the shape only, and conflicting; `on="f"`;
  `textpathok="f"`.
- **wv44-textpath** (45): `fitshape`, `fitpath`, `trim`, `xscale` each on and off on a two-path and a one-path type;
  `font-size` 1, 12, 36 and 72 pt on one box (the same drawing?); bold, italic; `v-text-align`'s six values on two lines
  of unequal length; `v-same-letter-heights`; `v-rotate-letters`; `v-text-kern` on and off ("AVATAR"); `v-text-spacing`
  80 and 150 in both modes; `v-text-reverse`; `text-decoration:underline` (kept, not drawn, says 2.1.1812); three
  lines; an empty string; a 60-character string; a glyph that overlaps itself (the fill rule).
- **wv45-paint** (40): `fillcolor` named, hex, a system colour, `fill darken(118)`; `filled="f"`; `opacity` 0.5 and
  0.2; gradients (`type="gradient"` at angles 0, 45 and 90, `color2`, `focus` 0, 50, 100 and -50, a `colors` list,
  `type="gradientRadial"` with `focusposition` and `focussize`); `type="pattern"`, `tile` and `frame` with a picture;
  `stroked="f"`, `strokeweight` 0.25, 2 and 6 pt, `strokecolor`, `dashstyle`, `joinstyle`, `endcap`,
  `linestyle="thickThin"`, stroke `opacity`; `v:shadow` with no `on` (real3's), `on="t"` at its defaults, offsets 4 pt
  and -3 pt, colour and opacity, `type="double"` with `offset2` and `color2`, `type="perspective"` with a `matrix` and
  `origin`, `type="emboss"`, `obscured`; `o:extrusion on="t"` (3-D).
- **wv46-placement** (30): inline in a line (no `position`); anchored to the page, margin, column and paragraph;
  centred; `rotation` 30, 90 and 315; `flip` x, y and xy (does the text read mirrored?); wrap none, square and
  topAndBottom, behind (negative `z-index`) and in front; in a table cell; in a `v:group` with a picture (real3's
  form, in the body and in a header that prints), and in a `w:object` holding one; the same WordArt in a mode-12 and a
  mode-15 document.
- **wv47-watermarks** (25), Word's own form (section 1.3) in the header of a page with body text: diagonal and
  horizontal; semitransparent and not; Aptos, Calibri and Times New Roman; `font-size` 1 pt (Word's "Auto") and fixed
  sizes 36, 72 and 144 pt with the box Word's dialog would give; colours; landscape and A4 pages; unequal margins (what
  it's centred on); first-page and even headers; two sections with different watermarks.
- **wv48-wps** (5): DrawingML WordArt with a self-overlapping glyph, for the fill rule of section 1.1.

Read by `read_vml.py <results> <docxdir> <out.jsonl>`: each probe's parameters from its own docx, and from Word's PDF
its glyph paths (points, fill rule, colour, alpha), stroke, any shadow path or picture, and its placement (the paths'
box on the page); `gate_vml.py` compares docx4j's PDF with Word's probe by probe, from `gate_wordart.py`'s outline
comparison. Every file is expected silent; none goes on the stop list.

## 3. What the round answers

1. Whether Word's VML envelope and Follow Path are phase 2's (§20.1.9.19 on the shape's box, by arc length; Follow
   Path's D / (D + 2T)), or legacy WordArt's own: `fitshape` and `fitpath` stretching the text to the box and along the
   path, which DrawingML has no attribute for.
2. Whether `font-size` changes anything once the text is fitted (the watermark's 1 pt says not, under `fitshape`).
3. How the path count maps to a warp kind for each of the 40 types (one path Follow Path, two an envelope, more bands),
   and which types phase 2's 11 unexplained presets correspond to.
4. Word's merge of a shape with its shapetype (attribute by attribute, as section 1.3 shows for `v:textpath`; and for
   `v:fill`, `v:stroke`, `v:shadow` and `adj`), its rule for an unresolved or duplicated id, and whether the handle's
   range clamps an adj.
5. The paint: VML gradients on glyphs (one gradient across the text, or a glyph each?), picture fills, the stroke's
   join and dash, and the shadow: drawn as a path (a sharp shadow is a copy of the glyphs) or a picture; a missing `on`.
6. 3-D (`o:extrusion`): a picture of the whole, as wps 3-D WordArt (D10), or something else.
7. Placement: whether the VML picture route's measurements hold for WordArt, a turned box's place (turned about its
   centre, placed by its unturned box?), flip, and a watermark's centring relative to the margin.
8. The fill rule of wps WordArt's glyph paths, in case docx4j's (nonzero, from Java2D's outlines) differs from Word's.
