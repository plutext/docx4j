# CR-084: picture colour adjustments drawn - recolour, brightness and contrast, transparency, the transparent colour

Status: ACCEPTED 2026-11-12 (Jason: "CR-084: the seven decisions in section 7 of CR-084-notes/phase0-findings.md as
recommended; commit phase 0; start phase 1"). Phase 0 DONE: its rulings are in §6 and §8; D1, D4, D6 and D7, which section
7 doesn't name, are taken as recommended. Phase 1 DONE 2026-11-12 (Jason: "start CR-084 phase 1", "commit"):
`PictureRecolour` in CR-029's step, an adjusted metafile rasterised at 200 dpi, CR-081's effects from the adjusted
picture. Gate passed: phase 0's 174 probes through docx4j's PDF 0 failing, the fidelity corpora 0 movers (b452 against
b450), and every PDF without a drawn adjustment unchanged but for date fields (`CR-084-notes/phase1-findings.md`). Two
findings: `biLevel`'s threshold is rounded (refined, built); `clrChange` has a tolerance in Word, about a level on a PNG
and at least 16 on a JPEG, measured and built in phase 1b (DONE 2026-11-12: the largest channel within 16 levels on
a JPEG, 1 on a PNG, 0 on a GIF or with `useA="0"`; 13145 and 4102 now as Word's). Phases 2 (the a14 test), 3 (VML)
and 4 (HTML) DONE 2026-11-12 (§6); phase 5 to come.
Proposed 2026-11-10, on Jason's request ("CR-081 D7 make a CR for this"): CR-081's D7 leaves `a:blip`'s colour effects
out of CR-081, which is about `a:effectLst`. Needs nothing from CR-080 to CR-083. Read against docx4j 67b54b7fd, Apache
POI trunk ae62bb5116, and the specifications in §12.
Owner: Jason Harrop. Drafted with Claude Opus 5.5.

Phase 0 started 2026-11-12 (Jason: "now let's do CR-084 phase 0"), by CR-081's route (word-bridge; `CR-084-notes/`):
- Round 1, `cr084-phase0`, staged: 7 files, one probe a page, 132 probes (`build_recolour.py`, `probes.tsv`; checked by
  CR-083's `check_wordart.py`). `lum` brightness by contrast on a 256-level grey ramp; the colour effects (`grayscl`,
  `biLevel`, `duotone`, `hsl`, `tint`, `clrRepl`, `fillOverlay`, `blur`) on a calibration picture (the 6-level RGB cube,
  24 hues, tints and shades); the alpha effects on an alpha calibration picture; `clrChange` near its colour, with and
  without `useA`; two effects in both orders; four effects on the photo (PNG, JPEG), transparent line art, a cropped
  picture, a picture with an outer shadow and an EMF; VML `v:imagedata`'s colour attributes. Each file starts with its
  picture unadjusted.
- The pictures are generated (16 px patches, so that a patch's centre survives a re-encoding), the EMF too (16
  rectangles, written record by record), and shown in front of the text at a fixed place on the page.
- Word's interface presets need Jason's hand: `build_hand_start.py` writes the starting document (43 pages: the 20
  Recolor presets, saturation, tone, transparency, Set Transparent Color, corrections, two artistic effects, Remove
  Background, two on the EMF), on the share as `cr084-recolour/cr084-recolour-start.docx`.
- Jason applied them the same day (`cr084-recolour-hand.docx`); `build_hand_start.py harvest` reads it into
  `presets-word.tsv`:
  - Recolor writes `a:` children of the blip: Grayscale `a:grayscl`; Sepia a `duotone` of black and `D9C3A5` (tint 50%,
    satMod 180%); Washout `a:lum bright="70000" contrast="-70000"`; Black and White `a:biLevel` at 25, 50 and 75%; the
    dark variations duotones of black and a theme colour (`tx2`, `accent1` to `accent6`, tint 45%, satMod 400%); the
    light variations duotones of a theme colour (`bg2`, `accent1` to `accent6`, shade 45%, satMod 135%) and white.
  - Transparency writes `a:alphaModFix` (amount 100% less the preset); Set Transparent Color `a:clrChange` to the same
    colour at alpha 0. Both, and Grayscale, are offered for the EMF too, and written the same way.
  - Saturation, Tone, Corrections (sharpen and soften, brightness and contrast), Artistic Effects and Remove Background
    write `a14:imgEffect`, and **the blip becomes the adjusted picture**, the original kept as `a14:imgLayer` (an HD
    Photo, `.wdp`). Checked by pixels: the original Word kept is ours byte for byte, and saturation 0% turns every patch
    grey. So D3 is settled: docx4j draws the blip and never needs the layer.
  - Saturation's last preset is 400% (`sat="400000"`); a correction with brightness only writes no `contrast`.
  - Three tasks landed one page off by hand: the 4700 K tone is on the 400% saturation page, +40%/-40% brightness and
    contrast on the -40%/-40% page, and a Soften on the Set Transparent Color page; -40%/-40% wasn't recorded.
- Set `cr084-phase0-presets` staged: the hand document as it is, and the 20 Recolor presets' XML on the grey ramp and
  on the photo as a JPEG (42 probes).
- Both sets ran the same day, all 14 jobs silent; read in `CR-084-notes/phase0-findings.md` (readers `read_recolour.py`,
  `fit_recolour.py`; tables `phase0-word.json`, `fits-word.tsv`). In short:
  - every effect's function is named and fitted on the stored sRGB values: `lum` is
    `s (x - 1/2) + 1/2 + b (1 + s) / 2` (36 probes within 1.7 levels), grayscale Rec. 709 luma, duotone a line in luma
    between `DrawingMLColors`' colours (the 15 preset colours within 2), the alpha effects as their names say;
  - Word draws `hsl` black and a blurred blip not at all; applies the effects in written order, but `biLevel` before
    `duotone`; crops before it adjusts; rasterises an adjusted EMF at 200 dpi; and a shadow follows the adjusted alpha;
  - VML's `gain`, `blacklevel` and `gamma` are `lum` again (`blacklevel` = bright / 2), within half a level;
  - the load survey: 46,859 documents with blip adjustments, 10,393 with `a14:imgProps` (all with a layer);
  - proposed CR changes (D2, D3, D5, D8, D9, the tolerance) are in the findings' section 7, for Jason.

## 0. Summary

A picture adjusted in Word comes out of docx4j's PDF and HTML output as the original picture. The adjustments are Picture
Format's Color (recolour: grayscale, sepia, washout, black and white, a colour's tint), Transparency and Set Transparent
Color, and Corrections (brightness and contrast). Word writes them as children of the picture's `a:blip` (`a:grayscl`,
`a:lum`, `a:duotone`, `a:biLevel`, `a:alphaModFix`, `a:clrChange` and so on), or, for its later adjustments, as Word
2010's `a14:imgProps` in the blip's extension list. VML pictures carry the older form: `v:imagedata`'s `gain`,
`blacklevel`, `grayscale` and `bilevel`.

This CR applies them to the picture's pixels in the step CR-029 added, where docx4j already crops and resamples the
picture it hands FOP. The arithmetic of each is measured against Word's PDFs, since the standard leaves it open. They
occur in 29 of the 561 documents of the four fidelity corpora (§4), against 4 with a picture effect (CR-081).

## 1. What docx4j does today (read from the code, 2026-11-10)

- `WordXmlPictureE20` reads the blip's `r:embed` or `r:link`, and its `a:srcRect` as CR-029's `PictureCrop`. The blip's
  effect children, and its `a:extLst`, are not read.
- `AbstractConversionImageHandler.compressedVariant` and `handleCompressed` (CR-029) are the one place docx4j changes a
  picture's pixels for XSL-FO output. `PictureCompression.compress` crops and resamples, and stores the result as a
  derived part keyed by a variant signature.
- A VML picture's `v:imagedata` is read for its crop attributes (`PictureCrop`). Its colour attributes are not.
- HTML output gets the picture's bytes as they are. CR-029 is PDF only.
- `DrawingMLColors` resolves a DrawingML colour with its modifiers, which `duotone`'s and `clrChange`'s colours need.
- docx4j's `DrawPaint` (from POI, CR-011) recolours a pattern fill's grey image for `a:duotone`, indexing by the red
  channel. It isn't on the picture path, and a colour picture's red channel isn't its luminance.

## 2. What Word does (phase 0 measures it)

**What the standard leaves open.** §20.1.8 defines each effect in a sentence, without its arithmetic:
- `grayscl`: "a shade of gray, corresponding to their luminance", with no weights given;
- `duotone`: `clr1` and `clr2` combined "through a linear interpolation", without saying of what;
- `lum`: brightness "linearly shifts all colors closer to white or black", contrast "scales all colors to be either
  closer or further apart", with no formula;
- `biLevel`: black below a luminance threshold, white at or above it, again without the luminance's definition;
- `clrChange`: instances of `clrFrom` replaced by `clrTo`, with no tolerance stated.

So each is measured as a function from a source pixel to Word's.

**What Word's interface writes** (expected; phase 0 confirms each against Jason's hand-made document):
- Recolor's Grayscale, Sepia, Washout, and Black and White at 25, 50 and 75%;
- its colour variations (dark and light, for each of the theme's colours);
- Transparency's presets;
- Set Transparent Color;
- the Corrections, Saturation, Tone, Sharpen and Soften presets;
- Artistic Effects;
- Remove Background.

The first four groups are expected as `a:` effects, the rest as `a14:imgEffect`.

**What the corpora show (§4).** Every blip with `a14:imgProps` also has `a14:imgLayer`, Word's link to the original
picture. That supports CR-081 §9's reading: Word stores the adjusted picture as the blip itself, and the original beside
it. If phase 0 confirms it, the `a14` adjustments need nothing drawn; the picture is already adjusted.

**What phase 0 also settles:**
- the order of several effects on one blip, the order in which they are written or a fixed order;
- whether the adjustments come before or after the crop;
- whether a transparent picture's shadow (CR-081) follows its adjusted alpha;
- how Word encodes the adjusted picture in its PDF. By content, as CR-081 found: JPEG for a photo, Flate for line art.

## 3. Sources evaluated

| Source | What it has | Use |
|---|---|---|
| Apache POI (ae62bb5116) | `DrawPaint.colorizePattern`: `a:duotone` on a pattern fill's grey image only. `XSLFPictureShape` reads `a:alphaModFix` as a whole-picture alpha. Nothing else of §20.1.8's blip effects. | Little: docx4j already has `DrawPaint` (CR-011). The duotone arithmetic is measured, not taken from it. |
| LibreOffice (MPL-2.0) | Draws these effects. | A black-box oracle only (CR-080 D11): probe documents converted with `soffice` and compared, its source not read. |
| ECMA-376, [MS-ODRAWXML] | The elements and attributes (§12). | The vocabulary; the arithmetic is Word's, measured. |

## 4. Corpus facts (2026-11-10, the four fidelity corpora)

Documents with any of the adjustments (counted in the main document, headers and footers): real 2 of 194, real2 11
of 157, real3 7 of 103, real-c2 9 of 107. 29 of 561 in all.

| Element (documents, occurrences) | real | real2 | real3 | real-c2 |
|---|---|---|---|---|
| `a:clrChange` | | 5, 24 | 1, 1 | 3, 22 |
| `a:lum` | 1, 1 | 1, 1 | 3, 12 | 2, 9 |
| `a:grayscl` | | 1, 2 | | 1, 2 |
| `a:duotone` | 1, 1 | | | |
| `a:tint` | 1, 1 | | | |
| `a14:imgProps` | | 3, 6 | 2, 4 | 2, 3 |
| `v:imagedata` `gain` and `blacklevel` | | 1, 1 | 1, 2 | |
| `v:imagedata` `grayscale` | | 1, 1 | | 1, 1 |

The `a14:imgEffect` kinds: saturation 7, brightness and contrast 5, sharpen and soften 5, background removal 3, colour
temperature 1, photocopy (artistic) 1. Each of the 13 blips with `a14:imgProps` has an `a14:imgLayer`.

Phase 0 adds the load survey's counts (3.72M documents), through the portfolio.

## 5. Approach

1. **One recolour step**, `org.docx4j.model.images.PictureRecolour` in docx4j-core, beside `PictureCrop`:
   - `of(CTBlip)` and `of(v:imagedata)` give the operations in order, with their colours resolved
     (`DrawingMLColors`);
   - `apply(BufferedImage)` gives an ARGB picture;
   - each operation is a per-pixel function with phase 0's constants.
2. **In CR-029's step.** The recolour joins the crop in `compressedVariant`'s signature, and `PictureCompression`
   applies it: crop, recolour, resample (D8). A picture with no adjustment takes the path it takes now, byte for byte.
3. **The encoding** follows CR-029's rules (D7): PNG where the result has transparency, otherwise the source's format.
4. **CR-081's effects** take the adjusted picture, so that an effect follows its adjusted alpha, if phase 0 finds Word
   does so.
5. **HTML output** gets the same adjusted picture (D6).
6. **What can't be drawn is reported** with stable codes, through CR-080 D9's mechanism (D9).

## 6. Phases

- **Phase 0, the oracle.** Through word-bridge, as CR-081's phase 0 (its `CR-081-notes` tools).
  - Jason applies each interface preset (§2) by hand, once, to copies of a calibration picture in a starting document,
    as for the picture styles, and saves it. The harvest takes Word's XML for each.
  - The calibration picture is a grid of hues by lightness, plus a grey ramp, so that each operation can be fitted as a
    function of the source pixel. Phase 0 checks whether Word keeps it lossless (Flate), as it does line art.
  - The generator writes each harvested adjustment over:
    - the calibration picture;
    - a photo, as PNG and as JPEG;
    - a transparent picture;
    - a cropped picture;
    - a picture with an outer shadow;
    - an EMF.
  - It also writes sweeps of what the interface doesn't reach:
    - `lum` over a grid of brightness and contrast;
    - `biLevel` thresholds;
    - `duotone` colour pairs;
    - `alphaModFix` amounts;
    - `clrChange` on colours near `clrFrom`, with and without `useA`;
    - two effects in both orders;
    - each of the remaining elements once (`alphaBiLevel`, `alphaCeiling`, `alphaFloor`, `alphaInv`, `alphaMod`,
      `alphaRepl`, `blur`, `clrRepl`, `fillOverlay`, `hsl`, `tint`);
    - VML `v:imagedata` with `gain`, `blacklevel`, `gamma`, `grayscale`, `bilevel` and `chromakey`.
  - Read from Word's PDF: each picture's pixels against the source's (`pdfdraws.pixels`); whether an `a14` blip is
    already adjusted (the PDF's picture against the blip's own).
  - The load survey's counts, through the portfolio.
  - **Gate:** the numbers committed; each operation's function named, with its fit to 2/255, or the tolerance it needs;
    the table of interface presets to XML.
- **Phase 1, the `a:` effects in PDF output.** `PictureRecolour` for the effects Word's interface writes, and the rest
  as phase 0 finds them, in CR-029's step. Unit tests against phase 0's numbers.
  - The functions are phase 0's (`CR-084-notes/phase0-findings.md` §3, `fit_recolour.py`), on the stored sRGB values;
    `duotone`'s colours through `DrawingMLColors`.
  - The order is the order written, except that `biLevel` comes before `duotone` (phase 0, decided 2026-11-12).
  - `hsl` and `blur` follow the standard (offsets; a Gaussian blur), and are reported as departures from Word, which
    draws `hsl` black and a blurred blip not at all (`render.picture.hsl`, `render.picture.blur`; D2).
  - A vector picture with an adjustment is rasterised at 200 dpi, as Word does; one without stays vector (D5).
  - **Gate:** phase 0's probes through docx4j's PDF within 2 levels of 255, but `tint` and semi-transparent colours
    within 7 (Word's premultiplied storage), and the `biLevel` tie decided as Word's integer luma; every picture
    without an adjustment unchanged, byte for byte (the fidelity corpora's PDFs, and CR-029's tests).
  - **Built and gated 2026-11-12** (`CR-084-notes/phase1-findings.md`):
    - the probes: 174, 0 failing; the departures are `hsl` and `blur` (D2) and VML (phase 3);
    - `PictureRecolourWordTest`: Word's numbers for 117 probes through `PictureRecolour` itself;
    - b452 against b450: 0 movers, events equal, the probes' PDFs the same byte for byte;
    - the corpora's PDFs: only the 16 with a drawn adjustment and 16 date fields changed;
    - the test suites pass: core-tests 2,642, export-fo 111, export-fo-tests 956.
  - `biLevel`'s threshold is in whole levels, rounded: 25% is 64, 50% is 128, 75% is 191. It fits all seven `biLevel`
    probes; phase 0's reading missed two patches at 75%.
  - `clrChange` is exact, as phase 0 found on PNG. The corpus shows Word with a tolerance (findings §4): about a level
    on a PNG, at least 16 levels on a JPEG, where five of the corpus's six pictures are. A probe round, for Jason,
    before it's built.
- **Phase 1b, `clrChange`'s tolerance** (Jason, 2026-11-12: "do the Set Transparent Color tolerance round").
  - The round, `cr084-clrchange-tol`, was staged 2026-11-12 (`CR-084-notes/build_tolerance.py`,
    `tolerance-probes.tsv`): 4 files, 35 probes, schema-valid, drawn by docx4j.
  - Colours near a target (white, red, mid-grey, 336699), each channel moved alone, all three together, and two at
    once, by up to 80 levels, made transparent:
    - as PNG, JPEG 75 4:2:0 (as the corpus's) and JPEG 100 4:4:4;
    - for white also JPEG 50, a PNG with an alpha channel, a palette PNG and a GIF, and to an opaque colour, with and
      without `useA`.
  - Each patch is one JPEG MCU, so it decodes flat. A JPEG's levels are its decoded patch centres
    (`tolerance-sources.json`).
  - Then the rule is built in `PictureRecolour`, and phase 1's gate is run again.
  - **DONE 2026-11-12** (`CR-084-notes/phase1-findings.md` §6). All 4 jobs ran silent. The measure is the largest
    channel's difference from `clrFrom`; the tolerance is the picture format's:
    - a JPEG 16 (quality 50 to 100, 4:2:0 and 4:4:4);
    - a GIF 0;
    - a PNG 1, or 0 with `useA="0"`.
  - Built: `PictureRecolour.apply(..., sourceFormat)`.
  - Found on the way: a compressed variant's name dropped its source's extension, so two pictures differing only by
    it shared one file (a defect since 17.3.0, now fixed).
  - Gate:
    - the round's 4,864 patches as Word takes them, but 20 JPEG 4:2:0 patches at the boundary (the decoders);
    - phase 0's probes 0 failing;
    - b461 against b459: 0 movers, only the `clrChange` documents' PDFs changed;
    - 13145's and 4102's pictures transparent as Word's (0.890, 0.565).
- **Phase 2, Word 2010's adjustments.** Phase 0 confirmed the blip is already adjusted (findings §2; D3): a test that
  docx4j draws the blip, with its `a:` children, and never the `a14:imgLayer`.
  - **DONE 2026-11-12** (Jason, via the portfolio: "please do each phase 2, 3, 4 in order, as far as possible without
    asking for my input"). `PictureImgLayerTest` (docx4j-export-fo-tests) uses a red blip with a blue `a14:imgLayer`
    and checks:
    - both PDF routes draw red, and the layer not at all, as does the HTML;
    - an `a:grayscl` beside the extension greys the blip.
  - No code change was needed: docx4j reads the blip's `r:embed` and never the extension.
  - Jason's hand-made presets document (rk80) through docx4j's PDF, against Word's: on the a14 pages, the pictures are
    Word's pixel for pixel. That covers saturation 0% and 300%, colour tone, soften, sharpen, the four brightness and
    contrast pages, and Pencil Sketch.
    - Blur and Remove Background differ by Word's JPEG encoding of them alone. Remove Background's alpha is Word's
      exactly.
    - Set Transparent Color over a Soften (an `a:clrChange` on an a14 blip) is Word's exactly, alpha and colour.
- **Phase 3, VML** (D4). `v:imagedata`'s attributes through the same operations.
  - **DONE 2026-11-12.** `PictureRecolour.ofVml`, set by `WordXmlPictureE10`, takes phase 0's functions (findings §5):
    - `chromakey`: the colour made transparent, with `clrChange`'s tolerance for the format;
    - `gamma`, `gain` and `blacklevel` together: `gain (x^(1/gamma) - 1/2) + 1/2 + blacklevel (1 + gain)`;
    - `grayscale`: Rec. 709;
    - `bilevel`: the luma as a fraction against a half (VML's tie goes to white, DrawingML's to black).
  - The order of the four kinds is taken, not measured: phase 0 probed each alone.
  - Gate:
    - phase 0's 12 VML probes through docx4j's PDF, each within a level of Word's (most exact), and all 174 probes 0
      failing; `PictureRecolourWordTest` takes the 12 as well (`word-patches.tsv`, 129 probes);
    - b462 against b461: 0 movers, events equal, the probes byte for byte;
    - the corpora's VML pictures: 8785's Word 2003 Washout is Word's exactly (p99 0), and 12363's grayscale grey as
      Word's (its picture is 1 px wider than Word's, as before).
- **Phase 4, HTML** (D6). The same adjusted picture, as a data URI or a stored file.
  - **DONE 2026-11-12.** HTML output goes through the image handler's variant step, as the PDF's does
    (`AbstractConversionImageHandler.compressedVariant`). It gets the adjustments only: no crop, no resampling, which
    stay the PDF's (CR-029). So a picture without adjustments reaches the HTML byte for byte, as before.
    - An adjusted metafile is rasterised at 200 dpi there too.
    - The effects (CR-081) are drawn from the adjusted pixels.
    - `render.effect.blip_recolour` is now reported only where an image handler of another kind leaves the
      adjustments undrawn.
  - Gate:
    - phase 0's probes through docx4j's HTML (`gate_recolour.py --html`): 119 compared, 0 failing, the `hsl`
      departures as decided;
    - `PictureImgLayerTest` checks a DrawingML and a VML picture's adjustments in both outputs, and an unadjusted
      picture's bytes in HTML (its two new assertions fail on b461's jars);
    - b463 against b462: 0 movers, the corpora's PDFs and the probes' the same (but the clocks);
    - core-tests 2,645, export-fo 111, export-fo-tests 963.
- **A review of phases 1 to 4** (Fable, 2026-11-12; Jason: "fix all 6"). Six findings, all fixed
  (`CR-084-notes/phase1-findings.md` section 7):
  - a wide blur bounded;
  - an adjusted picture's pixels bounded, and running out of memory leaves it as stored;
  - adjustments taken as drawn only once the handler has drawn them (a linked picture's are reported);
  - the derived names' change named;
  - VML's short hex and `rgb()` colours;
  - a derived picture that can't be written leaves the stored one.
  - Gate: b464 against b463 0 movers, the probes' PDFs and HTML the same.
- **Phase 5, CHANGELOG, Getting Started, the register.**

## 7. Risks

- **Size.** An adjusted picture is re-encoded, so it loses CR-081's JPEG passthrough, as a cropped one already does
  under CR-029. Word re-encodes every picture anyway (CR-081 findings §1).
- **Colour space.** Word may adjust in linear light or in sRGB. Phase 0's grey ramp decides, and the code follows.
- **Pictures with an ICC profile, or CMYK JPEGs.** They are converted to sRGB first, as CR-029 does.
- **Memory.** The recolour runs after the crop, on the cropped picture, at its size before resampling. CR-029's limits
  apply.
- **Vector pictures** (EMF, WMF, SVG) can't be recoloured per pixel without rasterising them (D5).

## 8. Decisions (owner: Jason; recommendations)

- **D1. In the pixels, in docx4j**, in CR-029's step, rather than SVG filters or in FOP. As CR-081's D1 (decided
  2026-11-10): Word's arithmetic is measured and then reproduced exactly, and tested without FOP. Recommendation: yes.
- **D2. Scope: all 17 effect children of `a:blip`.** The ones Word's interface writes are gated against Word. The rest
  follow phase 0's probes of Word's PDF, or the standard's text where Word draws nothing. Recommendation: yes. The
  elements are few, and each is a per-pixel function.
  **Decided 2026-11-12**: yes; `hsl` and `blur`, which Word draws black and not at all, follow the standard and are
  reported (`render.picture.hsl`, `render.picture.blur`).
- **D3. Word 2010's adjustments** (`a14:imgProps`): phase 2, which draws them only if phase 0 finds the blip isn't
  already adjusted. Recommendation: yes. **Settled by phase 0 (decided 2026-11-12)**: the blip is already adjusted, so
  phase 2 is a test that docx4j draws it and never the layer.
- **D4. VML in this CR** (phase 3). Recommendation: yes. The same operations, and the corpora have VML pictures with
  them.
- **D5. Vector pictures with an adjustment** are drawn unadjusted and reported (`render.picture.recolour_vector`),
  unless phase 0 shows Word rasterises them. In that case they are rasterised at CR-029's resolution, as Word does.
  Recommendation: follow phase 0. **Decided 2026-11-12, from phase 0**: Word rasterises an adjusted EMF at 200 dpi
  and keeps an unadjusted one vector; docx4j does the same.
- **D6. HTML output too** (phase 4). Recommendation: yes. A browser's CSS filters are not Word's arithmetic.
- **D7. Encoding.** PNG where the result has transparency, otherwise the source's format, at CR-029's quality.
  Recommendation: yes.
- **D8. The order: crop, recolour, resample.** The recolour is exact on the source's pixels, and costs no more than the
  crop leaves. Recommendation: yes, unless phase 0 shows Word recolours after resampling. **Decided 2026-11-12**: yes;
  phase 0 found Word crops, then adjusts.
- **D9. Gaps reported** with stable codes (`render.picture.recolour_vector`, `render.picture.artistic` and so on),
  through CR-080 D9's two mechanisms. Recommendation: yes. **Decided 2026-11-12**: CR-081 phase 6's
  `render.effect.blip_recolour` stays until phase 1 draws the effects; then only what stays undrawn is reported, under
  `render.picture.*`. Built in phase 1:
  - `render.picture.hsl` and `render.picture.blur`;
  - `render.picture.fill_overlay`: a fill overlay other than a solid fill;
  - `render.picture.colour`: a colour that can't be resolved;
  - `render.picture.unread`: a picture Java can't read;
  - `render.picture.recolour_vector`: a metafile that couldn't be rasterised;
  - `render.effect.blip_recolour` stays where nothing is drawn: an image handler of another kind (in HTML output too,
    since phase 4).

## 9. Out of scope

CR-081's effects (`a:effectLst`); text effects; applying the adjustments to the picture stored in the docx, as a
package operation like CR-029's Compress Pictures (a follow-up if wanted); PowerPoint and Excel output.

## 10. Hand-offs

- The portfolio: the registry entry with its phases. CR-084.1 depends on CR-084.0; CR-084.2, .3 and .4 on CR-084.1;
  CR-084.5 on the rest. CR-081's D7 now points here.
- CR-081: its phase 1 takes the adjusted picture (§5.4) once both exist. Neither waits for the other.
- The ports, and the FO renderer fork: none. The adjusted picture is an ordinary picture to FOP.

## 11. Effort (rough)

Phase 0 one day, and Jason's hand-made document; phase 1 two days; phase 2 half a day, or two if the blips aren't
already adjusted; phase 3 one day; phase 4 half a day; phase 5 half a day.

## 12. Specifications

Editions as CR-081 §12: ECMA-376 5th edition (Part 1 December 2016, Part 4 December 2016), local copies in
`~/git/safe-docx/spec-compliance/ecma-376/source-artifacts/`; [MS-ODRAWXML] on learn.microsoft.com.

**ISO/IEC 29500-1 (ECMA-376 Part 1)**
- §20.1.8.13 `blip`, whose effect children are:
  - alpha: §20.1.8.1 `alphaBiLevel`, §20.1.8.2 `alphaCeiling`, §20.1.8.3 `alphaFloor`, §20.1.8.4 `alphaInv`,
    §20.1.8.5 `alphaMod`, §20.1.8.6 `alphaModFix`, §20.1.8.8 `alphaRepl`;
  - colour: §20.1.8.11 `biLevel`, §20.1.8.16 `clrChange` (with §20.1.8.17 `clrFrom` and §20.1.8.19 `clrTo`),
    §20.1.8.18 `clrRepl`, §20.1.8.23 `duotone`, §20.1.8.34 `grayscl`, §20.1.8.39 `hsl`, §20.1.8.42 `lum`,
    §20.1.8.60 `tint`;
  - other: §20.1.8.15 `blur`, §20.1.8.29 `fillOverlay`.
- §20.1.8.55 `srcRect` (the crop, CR-029).

**ISO/IEC 29500-4 (ECMA-376 Part 4, transitional)**
- §19.1.2.11 `imagedata` (VML): `gain`, `blacklevel`, `gamma`, `grayscale`, `bilevel`, `chromakey`.

**[MS-ODRAWXML]**
- `a14:imgProps`, `a14:imgLayer`, `a14:imgEffect` and its children (`saturation`, `colorTemperature`,
  `brightnessContrast`, `sharpenSoften`, `backgroundRemoval`, the artistic effects): sections to be cited when phase 0
  reads them.
