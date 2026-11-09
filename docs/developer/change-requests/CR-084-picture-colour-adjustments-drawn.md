# CR-084: picture colour adjustments drawn - recolour, brightness and contrast, transparency, the transparent colour

Status: PROPOSED 2026-11-10. Drafted on Jason's request ("CR-081 D7 make a CR for this", 2026-11-10): CR-081's D7
leaves `a:blip`'s colour effects out of CR-081, which is about `a:effectLst`. Not started; D1 to D9 open (§8). Needs
nothing from CR-080 to CR-083. Read against docx4j 67b54b7fd, Apache POI trunk ae62bb5116, and the specifications in §12.
Owner: Jason Harrop. Drafted with Claude Opus 5.5.

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
  - **Gate:** phase 0's probes through docx4j's PDF within the tolerance; every picture without an adjustment
    unchanged, byte for byte (the fidelity corpora's PDFs, and CR-029's tests).
- **Phase 2, Word 2010's adjustments.** If phase 0 confirms the blip is already adjusted, a test that docx4j draws the
  blip and never the `a14:imgLayer`, and the CR records it. Otherwise saturation, colour temperature, brightness and
  contrast, sharpen and soften are drawn, and artistic effects reported.
- **Phase 3, VML** (D4). `v:imagedata`'s attributes through the same operations.
- **Phase 4, HTML** (D6). The same adjusted picture, as a data URI or a stored file.
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
- **D3. Word 2010's adjustments** (`a14:imgProps`): phase 2, which draws them only if phase 0 finds the blip isn't
  already adjusted. Recommendation: yes.
- **D4. VML in this CR** (phase 3). Recommendation: yes. The same operations, and the corpora have VML pictures with
  them.
- **D5. Vector pictures with an adjustment** are drawn unadjusted and reported (`render.picture.recolour_vector`),
  unless phase 0 shows Word rasterises them. In that case they are rasterised at CR-029's resolution, as Word does.
  Recommendation: follow phase 0.
- **D6. HTML output too** (phase 4). Recommendation: yes. A browser's CSS filters are not Word's arithmetic.
- **D7. Encoding.** PNG where the result has transparency, otherwise the source's format, at CR-029's quality.
  Recommendation: yes.
- **D8. The order: crop, recolour, resample.** The recolour is exact on the source's pixels, and costs no more than the
  crop leaves. Recommendation: yes, unless phase 0 shows Word recolours after resampling.
- **D9. Gaps reported** with stable codes (`render.picture.recolour_vector`, `render.picture.artistic` and so on),
  through CR-080 D9's two mechanisms. Recommendation: yes.

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
