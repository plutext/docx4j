# CR-081 phase 0: how Word draws DrawingML effects in a PDF

Word 16.0.20326.20232 (Microsoft 365), through word-bridge 4dcb436, 2026-11-09. Three rounds, 92 jobs plus their controls,
every open silent and every save done:
- `cr081-phase0`: 18 files, 220 probes.
- `cr081-phase0-3d`: 4 files, 64 probes.
- `cr081-phase0-r2`: 15 files, 197 probes.

The probes are `build_phase0.py`'s, listed in `probes.tsv`. Every draw is in `phase0-word.tsv` (`read_phase0.py`, over
`pdfdraws.py`), one row for each thing a probe's page draws besides its drawing, placed against the drawing's box.

**Word is deterministic.** Each file ran twice. All 516 rows are the same in both runs, in placement and in every pixel.
So a tolerance can't come from Word against itself, as §7 of the CR assumed. It comes from the pixel grid (0.36 pt at
200 dpi) and the 8-bit alpha.

Round 1 wrote `wp:effectExtent` as zero. That showed the clipping rule (§3), and it cut off the vertical sweeps. Round 2
re-ran those families with a generous effect extent. The numbers below are round 2's where the round matters.

## 1. What the PDF holds

| Effect | Drawn as | Where |
|---|---|---|
| `outerShdw` | an RGB picture with a soft mask, 200 dpi | under the drawing |
| `glow` | the same | under the drawing |
| `reflection` | the same; the whole drawing flipped, with the fade in its mask | under the drawing |
| `innerShdw` | the same, clipped to the outline | over the drawing |
| `softEdge` | the picture itself, re-encoded as JPEG (a picture) or Flate (a shape) with a soft mask | in place of the drawing |
| `prstShdw` | vector fills in the shadow's colour, unblurred; presets 13, 17 and 18 add a second fill at 40% grey | under the drawing |
| opaque `outerShdw` with no blur | a vector fill | under the drawing |
| 3-D (`scene3d`, `sp3d`) | one lit picture with a soft mask, 200 dpi: JPEG for a picture, Flate for a shape | in place of the drawing |

- The soft masks carry `/Matte [0 0 0]`, so the colour is premultiplied against black. `pdfdraws.pixels` divides it out.
- A shape's own fill stays a vector fill, over its effect pictures.
- Every effect follows the outline: an ellipse frame, a star, a transparent picture's own strokes (`fxr72`, `fxr80`).
- The text of a shape casts the shape's outer shadow too, filled or not (`fxr80-10`, `-11`).
- Word embeds a PNG picture with no transparency as a JPEG.

## 2. The rules, measured

### Blur

Every blur is a Gaussian. The 10-90% rise of each edge was measured, giving sigma as 10-90 / 2.563. The rms against the
erf is 0.002 to 0.005, apart from 1 pt, which is pixels.

| Effect | Sigma | Measured over | 50% contour | The picture reaches |
|---|---|---|---|---|
| `outerShdw` | **blurRad / 3** (0.327 to 0.337) | 2 to 32 pt (`fxr11`) | on the shifted outline | blurRad beyond it, plus a pixel |
| `innerShdw` | **blurRad / 3** (0.325 to 0.336) | 2 to 20 pt (`fx20`) | on the outline shifted by `dist` | the drawing's box |
| `glow` | **rad / 6** (0.163 to 0.167; 0.176 at 2 pt) | 5 to 32 pt (`fxr30`) | 0.44 to 0.47 rad outside the outline | rad beyond it |
| `softEdge` | **rad / 3** (0.331 to 0.337) | 2.5 to 25 pt (`fx40`) | 0.86 to 1.08 rad inside the outline | the drawing's box |

- **Soft edge.** The outline is transparent, and the picture is opaque again only at about 1.7 rad. So the blur reaches
  inside the inset, which isn't what [MS-OI29500] 2.1.1306 says ("the inside is untouched"). At 50 pt the 108 pt
  picture's centre is at alpha 0.40. At 78.7 pt it is gone.
- **Reflection.** The blur pads its picture by blurRad, as the shadow's does. Its sigma isn't measured yet (§6).

### Resolution

Effect pictures are 2.78 px/pt (200 dpi) unless the blur is wide: then the picture is made coarser by a whole factor k.

| blurRad (shadow, reflection) | 4 pt or less | 6 and 8 pt | 16 pt | 32 pt |
|---|---|---|---|---|
| k | 1 | 2 | 3 | 6 |

The glow's k counts rad / 2 as its blur: 11 pt gives 1, 18 pt gives 2, 32 pt gives 3.

All of these fit **k = ceil(blur in pixels at 200 dpi / T)**, with T between 14.8 and 16.7 px, likely 16. Word keeps a
blur's radius within about 16 pixels. This answers D3, and changes its default: Word's resolution follows the blur
(§5).

### Outer shadow geometry

- **The offset** is `dist` along `dir`, clockwise from +x (0 is to the right, 5400000 down), on the 0.36 pt pixel grid.
  Dist 1, 3, 6, 12 and 24 pt gave 0.63, 2.16, 4.32, 8.40 and 17.04 pt each way at 45 degrees, against 0.71, 2.12, 4.24,
  8.49 and 16.97 (`fxr10`).
- **Scale and skew are drawn in a picture's `spPr`.** [MS-OI29500] 2.1.1303 says a scaled shadow there isn't drawn, but
  Word 365 draws it (`fxr12`).
  - `sx` and `sy` scale about the `algn` point of the drawing's box: tl at 120% grows 28.8 pt right and 21.6 pt down.
  - `kx` and `ky` shear by tan(angle) times the other side.
  - A negative scale mirrors.
  - The five perspective shadows come out as sheared parallelograms.
- **`rotWithShape`** changes a turned picture's shadow (`fxr12-26`, `-27`), not yet modelled.

### Inner shadow

`dir` is measured from the left ([MS-OI29500] 2.1.1300 holds). The shadow lies along the inside edges that face `dir`:
0 puts it on the right, 5400000 at the bottom. Each edge moves by the projection of `dist`: 2.75 to 2.9 pt for 4 pt at
225 degrees, against 2.83.

### Reflection

All measured in `fxr50`; [MS-OI29500] 2.1.1304 holds.
- **The flip.** The whole drawing is flipped by `sy` (-100%) about the `algn` point: bl, b or br puts it below, tl
  above, and ctr over the drawing.
- **The offset.** It is moved by `dist` along `dir`: a gap of dist minus about 1 pt of blur padding.
- **The fade.** Alpha is linear from `stA` at `stPos` to `endA` at `endPos`, along `fadeDir`. 16200000 runs it upward;
  0 and 10800000 run it across.
- **Past `endPos`** alpha holds at `endA`. The base's 0.3% rounds to nothing in 8 bits; 50% holds to the end.
- **`stPos` past `endPos`** holds `stA` to `stPos`, then nothing.
- **`kx` and `ky`** skew it, as for the shadow.

### Colour

- Scheme colours resolve through the document's theme, with their modifiers (`lumMod`, `satMod`).
- The alpha in the colour is the effect's peak alpha.

### effectRef

- `wps:style/a:effectRef idx` resolves to the document theme's effect style, with the reference's colour standing for
  `phClr` (`fxr81` to `fxr83`).
- idx 1 and 2 are empty in the 2013 and 2023 themes; idx 3 is their shadow (alpha 63%, blur 4.5 pt, dist 1.5 pt down).
- A custom theme's glow at 2 and reflection at 3 are drawn: the glow in accent2, from `phClr`.
- An empty `a:effectLst` in the `spPr` overrides the reference.

## 3. Layout

- **Inline drawings are clipped.** An inline drawing's effects are clipped to its paragraph's height, across the whole
  page's width. `wp:effectExtent` grows the paragraph, and so the clip.
  - A shadow 24 pt down, with effect extent zero, is cut 2.3 pt under the picture.
  - An anchored drawing's effects aren't clipped.
- **The wrap** goes round the extent grown by `wp:effectExtent`, plus `distL`/`distR`. The effects themselves count for
  nothing (`fx90`).
  - Effect extent zero or omitted: the text comes to 9 pt (`distR`) from the picture, under its shadow.
  - r of 16 pt: the gap is 25.1 pt.
  - A glow with 18 pt each side: 27.3 pt.
- **Word's save** keeps `wp:effectExtent` as written, and never recomputes it (an omitted one is saved as zeros). Its
  other changes:
  - it drops attributes at their defaults (`dist`, `dir`, `blurRad` 0; `sx`, `sy` 100%; `algn` b; `rotWithShape` 1;
    `stA` 100%, `endA` 0, `endPos` 100%; `bevelT` w and h 76200 with prst circle);
  - it puts attributes in schema order.

  docx4j therefore has to take the effect extent from the file. It can't derive it from the effects.

## 4. 3-D

- Each 3-D drawing is one lit picture with a soft mask at 200 dpi: a JPEG for a picture, Flate for a shape. It is not
  vector shading. This is what phase 5's go/no-go asked.
- The probes' pictures are kept for the shading model (`fxa0` to `fxa3`: 12 bevels, 6 cameras, 2 camera turns, 22 light
  rigs and directions, 15 materials, depth, contour, z).
- Not yet read: how each preset lights.

## 5. What this changes in the CR (for Jason)

- **§2 and §12: 2.1.1303.** "An outer shadow in a `spPr` is drawn only at `sx` and `sy` of 100%": Word 365 draws scaled
  and skewed shadows in a picture's `spPr`. Follow Word.
- **§2 and §12: 2.1.1306.** The soft edge's blur reaches about 0.7 rad inside the inset. Follow Word: erode by about
  rad, blur with sigma rad / 3.
- **§7: tolerances.** Word against itself differs by nothing. The tolerance is the pixel grid and 8-bit alpha:
  - placement within 0.36 pt;
  - alpha within 2/255, which is the rms of the fits.
- **D2: one implementation.** chart-render's constants aren't Word's:
  - chart-render's shadow is sigma = blur / 2, and Word's picture shadow blurRad / 3;
  - chart-render's glow spreads 0.5 rad and blurs with 0.25 rad (CR-037, against Excel), and Word's spreads about
    0.45 rad and blurs with rad / 6.

  Either the renderer takes its constants from its host (Excel's for a chart in a workbook, Word's in a document) or a
  probe of a chart in Word decides. Charts' output stays as it is until then.
- **D3: resolution.** Word's rule is 200 dpi, made k times coarser to keep a blur within about 16 px. Recommendation:
  that rule, with the property kept as an upper bound. The D10 cap then rarely bites, since wide blurs are coarse.
- **D5: 3-D.** It is a picture at 200 dpi (§4), so phase 5's estimate can be set.
- **D6: preset shadows.** They are vector fills, unblurred, one or two of them, at fixed preset geometry. Cheaper than
  the outer shadow. The geometry of each of the 20 presets is in `phase0-word.tsv` (`fxr60`, `fxr61`).
- **Phase 1 needs the inline clip (§3).** The FO output must clip an inline picture's effects to its paragraph, or a
  docx4j PDF draws what Word cuts.

## 6. Not yet measured

- **Reflection.** `dir` with a non-zero `dist`: every `dir` probe had `dist` 0, so they show nothing. Also its blur's
  sigma.
- **Resolution.** The exact threshold T, from blurs between 5.3 and 6 pt.
- **Glow.** Its spread (0.44 to 0.47 rad, not quite 0.5): a fit of the dilation and blur together.
- **`rotWithShape`** on a turned drawing.
- **Text in a shape**, which casts the shape's shadow: whether glow and reflection apply to it too.
- **The 28 picture styles and the JPEG** (`cr081-phase0-styles`), from Jason's hand-made document.
- **3-D shading** (§4).
- **Reflection beside the wrap.** Whether lines beside a reflection's effect extent wrap.
