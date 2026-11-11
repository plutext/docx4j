# CR-084 phase 1: a picture's colour adjustments drawn in PDF output (findings and gate)

Built 2026-11-12 (Jason: "start CR-084 phase 1"), on docx4j 9979172e7.

## 1. What was built

- `org.docx4j.model.images.PictureRecolour` (docx4j-core). `of(CTBlip, ...)` reads `a:blip`'s 17 effect children in the
  order Word applies them, with their colours resolved by `DrawingMLColors`. `apply(BufferedImage, pixelsPerPoint)`
  applies them, each a function of the pixel on the stored sRGB values, as phase 0 fitted them (`fit_recolour.py`).
  - The order is as written, but a `biLevel` comes before a `duotone` written ahead of it.
  - A fully transparent pixel has no colour (black). Word stores pictures premultiplied, which `alphaRepl` and
    `clrChange`'s `useA` show.
  - `hsl` and `blur` are drawn as the standard has them (offsets; a Gaussian, its sigma a third of the radius, as
    CR-081 found Word's shadows' blur) and reported: `render.picture.hsl`, `render.picture.blur` (D2).
  - Also reported: `render.picture.fill_overlay` (a fill overlay other than a solid fill), `render.picture.colour` (an
    effect's colour that can't be resolved), `render.picture.unread` (a picture Java can't read: a CMYK JPEG, an
    animated GIF) and `render.picture.recolour_vector` (a metafile that couldn't be rasterised).
  - An adjustment that changes nothing (`<a:lum/>`, `alphaModFix` at 100%) is none, and isn't reported.
- In CR-029's step: `PictureCompression.compress` crops, adjusts, then resamples (D8). An adjusted picture is always
  re-encoded: PNG where the result has transparency, else the source's format (a JPEG at CR-029's quality; D7).
  - The handler's variant key gains the adjustments' signature only where there are some. So a picture without
    adjustments keeps the key, and the derived part's name, it had.
- A metafile (EMF, WMF) with adjustments is drawn as a picture at 200 dpi of its frame (through the crop), then cropped
  and adjusted as any bitmap. Word does this (D5: 533 px for 192 pt). One without stays vector, as before.
- CR-081's effects take the adjusted picture: the pixels read for a shadow, glow or reflection are adjusted the same
  way. So a shadow follows the adjusted alpha, and a fill behind a picture made transparent shows.
- `render.effect.blip_recolour` (CR-081 phase 6) is now reported only where the adjustments aren't drawn:
  - HTML output (phase 4);
  - an image handler that isn't an `AbstractConversionImageHandler`.

## 2. A finding: `biLevel`'s threshold is rounded

Phase 0 read `biLevel` as "white where the luma, in whole levels, reaches the threshold". That fitted 50%, but at 75%
two patches went the other way: a grey of 191 and (0, 255, 128), luma 191.62, both white in Word.

- Word's rule: the luma in whole levels, rounded down, against the threshold in whole levels, **rounded**:
  - 25% is 64;
  - 50% is 128, so a luma of 127.84 is black;
  - 75% is 191, so a luma of 191 is white.
- This fits every patch of the seven `biLevel` probes (1,792 patches). Every other rule tried misses 1 to 8.
- `fit_recolour.py` now has it (`fits-word.tsv`: rk20-04 and rk50-08 exact).
- VML's `bilevel` (rk70-10) compares the luma as a fraction instead: its tie patch, 127.84, is white. That's phase 3's
  to build.

## 3. The gate

**Phase 0's probes through docx4j's PDF** (`gate_recolour.py`; docx4j's PDFs rendered from the probe documents by the
harness's route, `Docx4J.toFO` with `FLAG_NONE`, FOP 2.12-docx4j.2 r83):
- 174 probes, **0 failing**. Every DrawingML probe with patches is within the gate's tolerance:
  - 2 levels;
  - 7 for `tint` and for a semi-transparent pixel's colour;
  - 5 where Word's picture is a JPEG.
- The departures are as decided:
  - `hsl` (rk20-10 to -12: the standard's offsets, where Word draws black);
  - `blur` (rk20-18: docx4j blurs, Word draws nothing);
  - the VML probes (rk70), which are phase 3's.
- `alphaCeiling` on pixels at alpha 17 and 34 is 7 levels off. Word's premultiplied storage quantises their colour
  (an 8-bit premultiply, rounded, fits within 1). This is within the 7 levels the gate allows a semi-transparent colour.
  The control's own quantisation fits a different rounding, so the storage isn't emulated.
- The EMF: unadjusted, vector (no picture), as in Word. Adjusted, a 533 by 533 picture, as in Word. Adjusted with
  `alphaModFix`, with a soft mask.
- The photo as a JPEG stays a JPEG when adjusted, and becomes a PNG with alpha under `alphaModFix`. (Word keeps a JPEG
  and adds a soft mask; D7.)
- The shadow follows the adjusted alpha. Line art with an outer shadow: the shadow's largest alpha is 0.592, and 0.298
  with `alphaModFix` 50%. That's Word's to the third decimal.
- The photos against Word's (rk60, rk82), pixel by pixel:
  - median 0 to 1 level;
  - the 99th percentile 0 to 22, which is Word's JPEG re-encoding of the PNG photo: the unadjusted control already
    differs by 20 there;
  - docx4j's adjusted photo against the model applied to its own unadjusted photo: within 0.5.

**Unit tests** (docx4j-core-tests):
- `PictureRecolourWordTest`: the same 117 probes' patches, Word's numbers (`images/recolour/word-patches.tsv`, from
  `build_test_table.py`), through `PictureRecolour` itself, at the same tolerance.
- `PictureRecolourTest`: the order, the signature, alpha, a grey PNG's stored levels, `clrChange`'s exactness, blur,
  and `compress`'s order and encodings.
- docx4j-core-tests: 2,642 pass. docx4j-export-fo: 111 pass, `PictureCompressionTest` (CR-029) among them.
  docx4j-export-fo-tests: 956 pass.

**The fidelity corpora** (b452, HEAD's jars with this docx4j-core on r83, against b450, HEAD's jars):
- 0 movers, events equal, scoreboard statuses equal.
- The 324 probes' PDFs are byte for byte the same, but for dates and file IDs.
- 32 of the corpora's 556 PDFs changed:
  - 16 have a picture with adjustments that is drawn (11 distinct documents):
    - `clrChange` in 6 (12338, 1426, 5135, 13145, 6088, 4102);
    - `lum` in 3 (9458, 11659, 11482);
    - `grayscl` in 1 (7042);
    - `duotone` in 1 (5320).
  - The other 16 are date fields. Each prints the day it's rendered, 11 November in b450 and 12 in b452.
- Three documents with adjustments are unchanged, as they should be:
  - 2189's is in its theme's fill style;
  - 13459's `<a:lum/>` changes nothing;
  - 10976's Washout EMF watermark is in three headers that no page shows, in Word's PDF as in docx4j's (its FO now names
    the picture rasterised at 200 dpi).
- 5320: its header bar's duotone is Word's colour exactly, (101, 114, 140), where docx4j drew the stored blue,
  (77, 163, 225).

## 4. A finding: `clrChange` has a tolerance, larger on a JPEG

Phase 0's probes, all PNG, found `clrChange` exact. Its nearest probed colour, 4 levels off red, was left as it was.
The corpus says otherwise. Each of its six `clrChange` pictures makes white transparent; five are JPEGs.

- **13145 (a JPEG)**: Word clears every pixel within 16 levels of white on its farthest channel, and keeps every
  pixel beyond. That's 100% agreement over the pixels Word left fully opaque or fully transparent. docx4j's exact
  rule agrees on 94.8%.
  - docx4j leaves 5.7% of the picture opaque where Word made it transparent: near-white JPEG noise, as specks and
    fringes. They show only on a background that isn't white.
  - The picture's content is dark, so this only bounds the tolerance: it's at least about 15 levels on this JPEG.
- **4102 (a PNG)**: Word clears 81% of the pixels 1 level off white on their farthest channel, and none at 2.
- So Word's tolerance looks format-dependent: about a level for a PNG, at least 16 for a JPEG. It might instead depend
  on the colour (white here, red in the probes).
- Not built: the rule is still exact. It needs a probe round (phase 0's section 8 left it open): near-white and
  near-red grids at 1-level steps, as PNG and as JPEG at a few qualities, with and without `useA`.

## 5. Open

- `clrChange`'s tolerance (section 4): a probe round, for Jason.
- 13145's adjusted picture is 1,557 by 608 px in docx4j's PDF and 1,536 by 600 in Word's. Word resampled it; its
  unadjusted pictures at the same resolution it keeps. Not in the probes, and outside this phase.
