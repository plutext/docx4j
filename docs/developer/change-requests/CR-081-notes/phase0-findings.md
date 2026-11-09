# CR-081 phase 0: how Word draws DrawingML effects in a PDF

Word 16.0.20326.20232 (Microsoft 365), through word-bridge 4dcb436, 2026-11-09 and 10. Four rounds, 102 jobs plus their
controls, every open silent and every save done:
- `cr081-phase0`: 18 files, 220 probes.
- `cr081-phase0-3d`: 4 files, 64 probes.
- `cr081-phase0-r2`: 15 files, 197 probes.
- `cr081-phase0-styles`: 5 files, 140 probes: the 28 picture styles (§7).

The probes are `build_phase0.py`'s, listed in `probes.tsv`. Every draw is in `phase0-word.tsv` (`read_phase0.py`, over
`pdfdraws.py`), one row for each thing a probe's page draws besides its drawing, placed against the drawing's box.

**Word is deterministic.** Each file ran twice. All 696 rows are the same in both runs, in placement and in every pixel.
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
| a picture's `a:ln` | a vector stroke wholly outside the picture; a compound line as filled rings (§2) | over the drawing and its inner shadow |

- The soft masks carry `/Matte [0 0 0]`, so the colour is premultiplied against black. `pdfdraws.pixels` divides it out.
- A shape's own fill stays a vector fill, over its effect pictures.
- Every effect follows the outline: an ellipse frame, a star, a transparent picture's own strokes (`fxr72`, `fxr80`).
- The text of a shape casts the shape's outer shadow too, filled or not (`fxr80-10`, `-11`).
- Word chooses a picture's encoding by its content, not its format. The photo (a PNG with no transparency) is a JPEG
  in the PDF, and so is the photo as a JPEG. The opaque line-art PNG stays Flate (`fs20`). The earlier wording here,
  that every PNG with no transparency becomes a JPEG, was wrong (corrected 2026-11-10).
- Word never passes a picture's bytes through: `fs11`'s 11.1 KB JPEG (quality 90) is 6.7 KB in the PDF, the same
  size as the PNG photo's (`fs10`).

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

All of these fit **k = ceil(blur in pixels at 200 dpi / T)**. The picture styles' shadows (§7) fit too: 15 pt gives 3,
20 and 23 pt give 4. Together they put T between 15.97 and 16.7 px: **T = 16**. Word keeps a blur's radius within 16
pixels. This answers D3, and changes its default: Word's resolution follows the blur (§5).

The inner shadow is the exception: always 200 dpi, to a 20 pt blur (`fx20`). Its picture is the drawing's size.

### A picture's outline

- **Outside the picture.** Word strokes a picture's `a:ln` on a path `w`/2 outside the picture's edge, so the whole
  line lies outside it: 3, 6, 7, 10 and 35 pt lines at 1.5, 3, 3.5, 5 and 17.5 pt (`fxr71`, §7). A shape's line is
  centred on its outline (`fx81` to `fx83`).
- **`cap="sq"`** is a projecting cap (`2 J`).
- **A compound line** is filled rings, not strokes. `thickThin` is 3 : 1 : 1 from the outside in (thick, gap, thin):
  10.8, 3.6, 3.6 of 18 pt, and 4.2, 1.4, 1.4 of 7.
- **Order:** the shadow, the fill behind the picture (a `solidFill` in the `pic:spPr`), the picture, its inner shadow,
  then the line.

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
- **Word's interface** does compute it, when a style is applied (Jason's document, §7):
  - always a whole multiple of 1.5 pt (19050 EMU);
  - covering every visible pixel: where the effect picture's padding reaches past it, as in Drop Shadow Rectangle
    and Reflected Rounded Rectangle, the inline clip cuts only zero alpha;
  - a line counts its whole width (it is outside the picture);
  - not a fixed alpha cut-off, nor blurRad: Drop Shadow Rectangle's 23 pt blur gets about 19.5 pt beyond the shifted
    outline, Center Shadow Rectangle's 15 pt gets 15.

## 4. 3-D

- Each 3-D drawing is one lit picture with a soft mask at 200 dpi: a JPEG for a picture, Flate for a shape. It is not
  vector shading. This is what phase 5's go/no-go asked.
- The probes' pictures are kept for the shading model (`fxa0` to `fxa3`: 12 bevels, 6 cameras, 2 camera turns, 22 light
  rigs and directions, 15 materials, depth, contour, z).
- Not yet read: how each preset lights.

## 5. What this changes in the CR (for Jason)

Accepted 2026-11-10 (Jason: "CR-081 decisions as recommended"), and written into the CR (§2, §5, §7, §8, §12).

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
- **§5 (pictures): a picture's line is outside it (§2).** "`a:ln` on a picture is drawn as CR-080 draws a line" holds
  for the stroke, not its place: CR-080 centres a shape's line on the outline, and a picture's is offset `w`/2
  outward. A compound line is rings at 3 : 1 : 1.
- **§5.4: JPEG passthrough isn't Word's.** Word re-encodes every picture (§1). docx4j's passthrough keeps the source's
  bytes: a larger PDF, and no second loss. Recommendation: keep it; the difference is in bytes, not in what is drawn.
- **The picture styles and the order of the work (§7).** 8 of the 28 are flat rectangles, within phase 1. 3 more need
  CR-080.1's geometry (phase 2). 17 are 3-D (phase 5), 7 of them round or oval. A style is usually 3-D, so D5's
  go/no-go decides most of the gallery.

## 6. Not yet measured

- **Reflection.** `dir` with a non-zero `dist`: every `dir` probe had `dist` 0, so they show nothing. Also its blur's
  sigma.
- **Resolution.** T is 16 to within 15.97 to 16.7 px (§2); a blur of 5.75 to 6 pt would settle it.
- **Glow.** Its spread (0.44 to 0.47 rad, not quite 0.5): a fit of the dilation and blur together.
- **`rotWithShape`** on a turned drawing.
- **Text in a shape**, which casts the shape's shadow: whether glow and reflection apply to it too.
- **3-D shading** (§4), now with the gallery's 17 3-D styles (§7) among its pictures.
- **Reflection beside the wrap.** Whether lines beside a reflection's effect extent wrap.
- **The interface's effect extent** (§3): its rule. docx4j reads the extent from the file, so only an API that adds a
  styled picture would need it.

## 7. The picture styles (`cr081-phase0-styles`, 2026-11-10)

No COM call applies a gallery style. So Jason applied the 28, once each, to 28 copies of the photo in
`build_styles_start.py`'s document, and saved it (`cr081-styles-hand.docx`). `build_styles_start.py harvest` took
Word's `pic:spPr` for each style, and the effect extent Word gave it, into `styles-word.tsv`. The set ran his document
as it is (`fs00`), then each style over the photo (`fs10`), the photo as a JPEG (`fs11`), the line art (`fs20`) and
the line art on a transparent ground (`fs21`).

- **The harvest is complete.** Each style over the photo draws as in Jason's document, to the pixel grid. Only the
  vertical phase differs (up to 0.24 pt, and one pixel row in eight pictures), since his document's styles part places
  the line differently. So a style is its `pic:spPr` and its effect extent; nothing else in the document matters.
- **The pictures don't change the geometry.** The photo, the JPEG and both line arts give the same boxes and pixel
  sizes, to a pixel.
- **Transparency.** Without a fill behind the picture, the effects follow the line art's strokes, as in `fxr72`. With
  one (11 styles have a `solidFill` in the `pic:spPr`), the picture's box is opaque and the effects follow the box.
  Soft Edge Rectangle and Soft Edge Oval erase the transparent line art entirely: the erosion (§2) is wider than its
  strokes.
- **3-D.** A 3-D style is one lit picture in place of the drawing, at 200 dpi, whatever its shadow's blur. Its shadow
  and reflection are in it: there is no other draw. The photo's is a JPEG, the line art's Flate. Its frame is estimated
  from the layout (`read_phase0.py`), within 0.24 pt on the flat pages.
- **Rotated, White** doesn't turn the picture's `a:xfrm`. It turns the 3-D camera (`rev` 6 degrees).
- **The gallery's own shadows are scaled and skewed in a picture's `spPr`**: Thick Matte, Black (`sy` 90%), Beveled Oval,
  Black (`sx` -80%, `sy` -18%), and the perspective styles. Word's interface writes what [MS-OI29500] 2.1.1303 says
  isn't drawn (§5).
- **Resolution** as §2: shadows of 15, 20 and 23 pt at k 3, 4 and 4; the 6 pt inner shadows at 200 dpi.

Over the photo (`fs10`). "Out" is how far a line's path lies outside the picture; k as §2.

| # | Style | Geometry | Line | Effects | 3-D | Word's extent l t r b (pt) | In the PDF |
|---|---|---|---|---|---|---|---|
| 1 | Simple Frame, White | rect | 7 FFFFFF | outer blur 4.33, dist 1.42 at 90° (fill behind) | bevel | 9 9 9 12 | one lit picture, 448x347 px |
| 2 | Beveled Matte, White | rect | 15 FFFFFF | outer blur 3.94 (fill behind) | bevel | 12 13.5 12 13.5 | one lit picture, 467x367 px |
| 3 | Metal Frame | rect | 15 C8C6BD | outer blur 20 | bevel hardEdge, perspectiveFront | 22.5 24 22.5 24 | one lit picture, 554x452 px |
| 4 | Drop Shadow Rectangle | rect | - | outer blur 23, dist 11 at 45° | - | 12 12 28.5 28.5 | under: picture, k 4 (clipped: zero alpha) |
| 5 | Reflected Rounded Rectangle | roundRect | - | reflection blur 1, stA 38%, end 28% (fill behind) | - | 1.5 0 1.5 33 | under: picture, k 1 (clipped: zero alpha); under: fill behind |
| 6 | Soft Edge Rectangle | rect | - | soft edge 8.86 | - | 0 0 0 0 | the picture alone, soft-masked, 200 dpi |
| 7 | Double Frame, Black | rect | 18 000000 thickThin | inner blur 6 | - | 18 18 18 18 | over: picture, k 1; over: rings 18.00 out |
| 8 | Thick Matte, Black | rect | 35 000000 | outer blur 20, dist 15 at 45°, sy 90% (fill behind) | - | 42 36 63 63 | under: picture, k 4; under: fill behind; over: stroke 17.50 out |
| 9 | Simple Frame, Black | rect | 3 000000 | outer blur 4, dist 3 at 45° | - | 6 6 10.5 10.5 | under: picture, k 1; over: stroke 1.50 out |
| 10 | Beveled Oval, Black | ellipse | 5 333333 | outer blur 30, dist 23 at 90°, sx -80%, sy -18% | bevel | 6 6 6 67.5 | one lit picture, 502x504 px |
| 11 | Compound Frame, Black | rect | 7 000000 thickThin | inner blur 6 | - | 7.5 7.5 7.5 7.5 | over: picture, k 1; over: rings 7.00 out |
| 12 | Moderate Frame, Black | rect | 10 000000 | outer blur 4.5, dist 4 at 45° | - | 12 12 18 18 | under: picture, k 1; over: stroke 5.00 out |
| 13 | Center Shadow Rectangle | rect | - | outer blur 15 | - | 15 15 15 15 | under: picture, k 3 |
| 14 | Rounded Diagonal Corner, White | round2DiagRect | 7 FFFFFF | outer blur 20 | - | 24 24 25.5 25.5 | under: picture, k 4; over: stroke 3.50 out |
| 15 | Snip Diagonal Corner, White | snip2DiagRect | 7 FFFFFF | outer blur 7 (fill behind) | bevel | 13.5 13.5 13.5 12 | one lit picture, 462x362 px |
| 16 | Moderate Frame, White | rect | 15 FFFFFF | outer blur 4.33, dist 1.42 at 90° (fill behind) | bevel | 12 12 12 15 | one lit picture, 470x370 px |
| 17 | Rotated, White | rect | 15 FFFFFF | outer blur 5.12, dist 4 at 215°, kx 3.25°, ky 2.42° (fill behind) | bevel, camera rev 6° | 24 19.5 24 21 | one lit picture, 532x433 px |
| 18 | Perspective Shadow, White | rect | 10 FFFFFF | outer blur 6, dist 7.5 at 175°, sx 97%, sy 23%, kx 15° | bevel | 45 9 9 13.5 | one lit picture, 544x350 px |
| 19 | Relaxed Perspective, White | rect | 8 FDFDFD | outer blur 4.5, dist 2.95 at 126°, sy 98%, kx 1.83°, ky 3.33° (fill behind) | bevel, perspectiveRelaxed, camera lat 316° | 10.5 0 13.5 3 | one lit picture, 495x282 px |
| 20 | Soft Edge Oval | ellipse | - | soft edge 8.86 | - | 0 0 0 0 | the picture alone, soft-masked, 200 dpi |
| 21 | Bevel Rectangle | roundRect | - | outer blur 6, dist 3 at 130° | bevel relaxedInset | 10.5 6 6 10.5 | one lit picture, 437x337 px |
| 22 | Bevel Perspective | roundRect | - | outer blur 12, dist 0.94 at 15°, sy 98%, kx 1.83°, ky 3.33° | bevel, perspectiveRelaxed, camera lat 330° lon 20° rev 347° | 9 0 18 15 | one lit picture, 540x348 px |
| 23 | Reflected Perspective Right | rect | - | reflection blur 1, stA 30%, end 30% | bevel, perspectiveContrastingLeftFacing, camera lat 5° lon 330° | 0 4.5 0 39 | one lit picture, 374x426 px |
| 24 | Bevel Perspective Left, White | rect | 15 FFFFFF | outer blur 2.85, dist 1 at 190° (fill behind) | bevel, perspectiveContrastingLeftFacing, camera lat 9° lon 35° | 0 18 4.5 21 | one lit picture, 397x411 px |
| 25 | Reflected Bevel, White | roundRect | 6 EAEAEA | reflection blur 1, stA 33%, end 28% (fill behind) | bevel | 7.5 7.5 7.5 37.5 | one lit picture, 426x422 px |
| 26 | Reflected Bevel, Black | roundRect | 6 292929 | reflection blur 1, stA 28%, end 28% (fill behind) | bevel | 7.5 6 7.5 39 | one lit picture, 426x426 px |
| 27 | Metal Rounded Rectangle | roundRect | 15 C8C6BD | outer blur 8, dist 4 at 120° | bevel hardEdge, perspectiveFront | 16.5 12 12 18 | one lit picture, 489x393 px |
| 28 | Metal Oval | ellipse | 15 C8C6BD | outer blur 10 | bevel hardEdge, perspectiveFront | 16.5 16.5 16.5 16.5 | one lit picture, 500x403 px |
