# CR-084 phase 0: what Word does with a picture's colour adjustments (findings)

Two sets ran on the word-bridge on 2026-11-12 (Word 16.0.20326.20232, printer FX DocuPrint C2255; the VM's go 198 and
199): `cr084-phase0` (7 files, 132 probes: `build_recolour.py`) and `cr084-phase0-presets` (Jason's hand-made presets
document as it is, and the 20 Recolor presets' XML on the grey ramp and on the photo: 42 probes). All 14 jobs, controls
included, opened silent and saved docx and PDF, with no dialog.

Readers: `read_recolour.py` (each probe's pictures in Word's PDF, and each calibration patch's colour beside the
source's: `phase0-word.json`), `fit_recolour.py` (each effect's model, scored against its probes: `fits-word.tsv`),
`build_hand_start.py harvest` (Jason's document: `presets-word.tsv`).

Levels are of 255. A probe's error is the largest difference over its 256 patches between the model and Word; Word
re-encodes some pictures as JPEG (listed in `fits-word.tsv`), which adds a level or two.

## 1. Word's interface writes (`presets-word.tsv`)

| Interface | XML |
|---|---|
| Recolor: Grayscale | `a:grayscl` |
| Recolor: Sepia | `a:duotone`: `prstClr black`, `srgbClr D9C3A5` with `tint 50000`, `satMod 180000` |
| Recolor: Washout | `a:lum bright="70000" contrast="-70000"` |
| Recolor: Black and White 25%, 50%, 75% | `a:biLevel thresh="25000"`, `"50000"`, `"75000"` |
| Recolor: the 7 dark variations | `a:duotone`: `prstClr black`, `schemeClr` `tx2`, `accent1` to `accent6` with `tint 45000`, `satMod 400000` |
| Recolor: the 7 light variations | `a:duotone`: `schemeClr` `bg2`, `accent1` to `accent6` with `shade 45000`, `satMod 135000`; `prstClr white` |
| Transparency n% | `a:alphaModFix amt` = 100% less n |
| Set Transparent Color | `a:clrChange`: the colour clicked, to itself at `alpha 0` |
| Color Saturation, Color Tone, Corrections, Artistic Effects, Remove Background | `a14:imgProps` with `a14:imgLayer` and its `a14:imgEffect`s (`saturation`, `colorTemperature`, `sharpenSoften`, `brightnessContrast`, `artistic...`, `backgroundRemoval`) |

- Saturation's last preset is 400% (`sat="400000"`); a correction with brightness alone writes no `contrast`.
- Grayscale and Transparency are offered for the EMF, and written the same way.
- Three of Jason's tasks landed a page off by hand; the -40%/-40% correction wasn't recorded (not needed: §2).

## 2. Word 2010's adjustments are already in the picture (D3)

For every `a14` preset, **the blip is a new picture with the adjustment applied, and the original is kept as
`a14:imgLayer`** (an HD Photo, `.wdp`). Checked by pixels:
- the original picture Word kept for the `a:` presets is ours byte for byte (1,065 bytes);
- saturation 0% turns all 256 patches grey in the stored blip; Pencil Sketch, too;
- **Word's PDF draws the stored blip as it is**: exact on the 11 pages Word kept lossless, JPEG noise on the 2 it
  re-encoded (Blur, Remove Background);
- an `a:` child beside the `a14` extension still applies, on the adjusted blip (Set Transparent Color over a Soften).

So D3 is settled: docx4j draws the blip, with its `a:` children, and never needs the layer.

## 3. The arithmetic (`fits-word.tsv`)

**On the stored sRGB values**, not in linear light: grayscale with Rec. 709's weights on the stored values is within
1.0 of Word, in linear light 73 levels off.

| Effect | Word's function | Probes, worst |
|---|---|---|
| `grayscl` | Rec. 709 luma (0.2126, 0.7152, 0.0722) of the stored values | 4, 1.0 (2.4 to 4.0 in pairs, JPEG) |
| `lum` | `s (x - 1/2) + 1/2 + b (1 + s) / 2`, per channel, clipped; `s = 1 + c` for c <= 0, `1 / (1 - c)` for c > 0 | 36, 1.7 (median 0.9) |
| `biLevel` | white where the luma reaches the threshold, else black; at 50% a patch of luma 127.84 is black and one of 130.85 white, so Word compares an integer luma, or `thresh x 256` | 5, exact but the one tie patch |
| `duotone` | `clr1 + (clr2 - clr1) x luma`; the colours as `DrawingMLColors` resolves them (tint and shade in linear light, satMod on the sRGB HSL unclamped: all 15 preset colours within 2) | 6, 3.9 (JPEG) |
| `alphaModFix` | `alpha x amt` | 6 alpha within 0.8 (0% draws nothing) |
| `alphaBiLevel` | opaque where alpha reaches the threshold, else transparent | 1, alpha exact |
| `alphaCeiling`, `alphaFloor` | any alpha above 0 opaque; any below 1 transparent | 1 each, alpha exact |
| `alphaInv` (no colour) | black, with alpha `1 - alpha` | 1, exact |
| `alphaMod` | alpha times the container's result on the picture itself (with `alphaModFix 50%`: `0.5 alpha^2`) | 1, alpha 0.5 |
| `alphaRepl` | alpha replaced; a fully transparent pixel's colour comes back black | 1, alpha 0.5 |
| `clrChange` | the exact colour only (the nearest probed, 4 levels off, unchanged); with `useA="1"` alpha must match too, with `useA="0"` the target's alpha is ignored (red to transparent changes nothing) | 7 |
| `clrRepl` | every pixel the colour, alpha kept | 1, exact |
| `fillOverlay` | `mult`: product; `screen`: `1 - (1 - a)(1 - b)` | 2, exact |
| `tint` | each hue turned `amt` of the way to `hue`, the shorter way (an exactly opposite hue by the sign of the plain difference); lightness and saturation kept | 2, 7 |
| `hsl` | **Word draws the picture black**, whatever the attributes (3 probes) | |
| `blur` | **Word draws no picture at all** | |

- **Alpha pictures**: Word stores a picture premultiplied, so a semi-transparent pixel's colour comes back up to 7
  levels off (alpha right within a level), and `clrChange` misses a semi-transparent pixel whose colour, so
  stored, is no longer exact (5 of 15 red alphas).
- **Order**: the effects apply in the order written (`grayscl` and `lum`, `duotone` and `lum`, both ways, within 2.4;
  `alphaModFix` commutes with the colour effects), **except that `biLevel` comes before `duotone`** in either order.

## 4. Pictures

- **Crop first**: a cropped picture is cropped in the PDF (128 by 128 px for the middle quarter), then adjusted.
- **Vector pictures (D5)**: an EMF without adjustment stays vector (a Form XObject); with any adjustment Word draws it
  as a picture at 200 dpi (533 px for 192 pt), the adjustment applied: the 16 blocks within 1 level of the model.
- **A shadow follows the adjusted alpha** (CR-081): with `alphaModFix 50%`, the line art's shadow's alpha halves
  (largest 0.592 to 0.298).
- **The photo**: the same models on Word's own unadjusted picture against its adjusted one: median 0.5 to 0.7 levels,
  the 99th percentile 2 to 11 (JPEG at edges).
- **Encoding**: by content, as CR-081 found: the photo JPEG, the line art and most calibration pictures Flate, some
  calibration pictures JPEG; a picture with alpha gets a soft mask; a JPEG source with `alphaModFix` stays JPEG with
  a soft mask.

## 5. VML `v:imagedata` (D4)

| Attribute | Word's function | Worst |
|---|---|---|
| `gain` | a slope about mid-grey: `gain (x - 1/2) + 1/2` | 0.5 |
| `blacklevel` | a shift of `blacklevel (1 + gain)`: DrawingML's `lum` with `blacklevel = bright / 2` | 0.0 |
| `gamma` | `x ^ (1 / gamma)` | 0.5 |
| `grayscale` | Rec. 709 luma | 0.5 |
| `bilevel` | `biLevel` at 50% | exact |
| `chromakey` | the exact colour made transparent | exact |

Word 2003's Washout (`gain 0.3`, `blacklevel 0.35`) is the current Washout (`lum` bright 70%, contrast -70%) exactly.

## 6. The load survey (3.24M documents; portfolio, 2026-11-12)

- `a:blip`: 1,344,006 documents. **With adjustments: 46,859 documents (95,508 blips)**: `lum` 21,448, `grayscl` 11,905,
  `clrChange` 9,121, `alphaModFix` 5,934, `biLevel` 5,016, `duotone` 4,654. The other 11 effects are below these.
- `a14:imgProps`: 10,393 documents, every one with an `a14:imgLayer`; `imgEffect`: saturation 3,850,
  brightnessContrast 3,830, sharpenSoften 2,930, colorTemperature 1,349, backgroundRemoval 1,315, artistic 223 and below.
- `v:imagedata`: 181,512 documents; with `blacklevel` 9,744, `gain` 9,500, `grayscale` 1,683, `chromakey` 976,
  `bilevel` 541, `gamma` 147.

## 7. For Jason: CR changes proposed

**Decided 2026-11-12** (Jason: "the seven decisions in section 7 ... as recommended"); recorded in the CR's §6 and §8.

1. **D3 settled**: phase 2 becomes a test that docx4j draws the blip (with its `a:` children) and never the layer;
   nothing to draw for `a14`.
2. **D5 settled by Word**: an adjusted vector picture is rasterised at 200 dpi (as Word does), an unadjusted one stays
   vector. Recommend: yes (replaces "drawn unadjusted and reported").
3. **D8 confirmed**: crop, then recolour (then resample).
4. **D2, `hsl` and `blur`**: Word draws `hsl` black and a blurred blip not at all. Neither is in the survey's list
   above. Recommend: follow the standard for both (hue, saturation and luminance offsets; a Gaussian blur), and report
   them (`render.picture.hsl`, `render.picture.blur`) as departures from Word, rather than copy Word's faults.
5. **The order**: written order, with `biLevel` before `duotone`; record it in phase 1.
6. **The gate's tolerance**: 2 levels for every effect but `tint` (7) and semi-transparent colours (Word's premultiplied
   storage, 7); the `biLevel` tie at exactly the threshold decided as Word's integer luma.
7. **D9's codes and CR-081 phase 6's `render.effect.blip_recolour`**: keep CR-081's code until phase 1 draws the
   effects, then report only what stays undrawn under CR-084's `render.picture.*` codes.

## 8. Open

Nothing that needs another round for phase 1. A finer `clrChange` tolerance (1 to 3 levels) and `tint`'s 7 levels can
be probed in phase 1 if its gate needs them.
