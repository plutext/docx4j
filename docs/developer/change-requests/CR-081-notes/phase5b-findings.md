# CR-081 phase 5b-1: Word's lights and materials, as its pictures read

Phase 5b was split at its go/no-go (2026-11-11; Jason: "yes, do 5b-1, defer 5b-2"). 5b-1 is the lighting of Word's
3-D picture styles:
- its rigs are twoPt, threePt, contrasting and soft, all dir t, several turned by rev;
- its materials are warmMatte (the default), matte and plastic.

Phase 5a's geometry is Word's within a pixel, so each of Word's pixels has a known normal, and the shading is fitted
to them as a regression.

## 1. The rounds

Rounds 1 to 3 use a grey (808080) drawing, 144 by 108 pt, at a fixed place, with a deep circle top bevel (36 by
18 pt) whose normals run from the face to the outline. Word 16.0.20326 was silent on all five rounds.

- **cr081-phase5b-r1** (fxg1, 21 probes; `build_phase0.py` fam_light):
  - the rectangle front on, under the 4 rigs and 3 materials;
  - twoPt and threePt turned rev 120;
  - threePt from tl;
  - threePt under perspectiveRelaxed and under perspectiveContrastingLeftFacing;
  - threePt extruded 36 pt under isometricOffAxis1Right;
  - a blue fill (4472C4) under warmMatte and plastic;
  - the photo.
- **cr081-phase5b-r2** (fxg2, 15 probes; fam_light_ellipse): the same rigs and materials on an ellipse, whose normals
  face every way (a rectangle's show four directions only), and the blue fill.
- **cr081-phase5b-r3** (fxg3, 42 probes; fam_light_turned): the ellipse extruded 36 pt in grey, seen turned and from
  behind (orthographicFront rot lat/lon 0/60, 0/300, 60/0, 300/0, 330/150, 30/210), under each rig:
  - warmMatte from all six views, matte from three, plastic from two (twoPt and threePt);
  - a turned rig under a turned camera, as the picture styles have them.
- **cr081-phase5b-r4** (fxg4, 10 probes; fam_small_bevel): corpus 4994's picture style taken apart, after its bevel
  was found drawn without the highlight the fit gave (section 5). Its 2 by 1.5 pt bevel, its rig (twoPt rev 120) and
  its 7 pt line with no fill, apart, on the photo and a grey shape; a 6 by 4.5 pt bevel; the style's spPr whole.
- **cr081-phase5b-r5** (fxg5, 14 probes; fam_bevel_cuts): circle bevels from 1.8 to 24 pt wide (the picture styles'
  sizes among them), hardEdge 24 by 12 pt and relaxedInset 30 by 9 pt, on a grey shape under twoPt rev 120.

Word writes a 3-D picture as Flate or as JPEG (16 and 5 of round 1's), apparently by its content.
`read_light.py` reads either (mutool decodes the JPEG; the soft mask's /Matte un-premultiplied).

## 2. Word's model

The shading is a sum of directional lights on the drawing's colour, with white highlights added:

    colour = min(255, c (A + sum_i D_i max(0, n.L_i)) + 255 ks sum_i S_i max(0, R_i.v)^p)

- c is the drawing's colour: the fill, the picture's pixel, the extrusion's or the contour's colour.
- A is the rig's ambient.
- L_i are the rig's lights, D_i their diffuse strengths and S_i their highlights' strengths.
- R_i is L_i reflected about n (where n.L_i > 0), and v the view (Phong's highlight; section 3).
- ks and p are the material's highlight strength and exponent.
- On a circle bevel, the colour is lit at the cuts of its profile and blended between them (section 5).

The fill's colour multiplies the diffuse light only:
- under threePt warmMatte, the blue face is (75, 123, 208) and the grey 138, both c 1.039 + 4.3;
- the blue probes fit, every channel, with the grey probes' lights.

Measured rules:
- **matte has no highlight.** Under contrasting, a matte drawing is its fill's colour exactly, everywhere (128 on all
  four sides): contrasting's lights give highlights only.
- **plastic's highlight is about twice warmMatte's, and sharper.** This holds across the rigs; fitted rig by rig,
  the exponents agree within a few units (with Blinn's half vector, the front views' fit: warmMatte about 31,
  plastic 45; with Phong's, the final fit: 8.0 and 11.5).
- **A rig turned by rev turns its lights clockwise on the page** (fxg1-13, -14: rev 120 gives 0.75 and 1.55 / 255
  turned clockwise, 27.6 and 26.8 anticlockwise).
- **dir tl turns them 45 degrees anticlockwise from t** (fxg1-15: 1.56 / 255; 24.6 the other way, 9.7 and 21.6 at
  90 degrees). The other directions are taken by the same rule, unmeasured.
- **The lights turn with the drawing, not with the camera.** Under perspectiveRelaxed, perspectiveContrastingLeftFacing
  and isometricOffAxis1Right (fxg1-16 to -18), lights fixed to the camera are 42.1, 29.1 and 33.3 / 255 off Word's.
  Fixed to the drawing, so that the camera turns round a lit drawing, they are 9.2, 2.8 and 8.8.
- **The rigs have lights behind the drawing that front views barely show.** The extruded drawing's left wall
  (fxg1-18, 1F3864) is (71, 85, 111) in Word's picture: its colour times 0.58, plus 53 of white highlight. Lights
  fitted to front views give it (11, 20, 35). Round 3 shows the sides and the back.

## 3. The fit

- **Front views** (rounds 1 and 2; `fit_light.py` a rig at a time, `fit_light_joint.py` the four together):
  - three lights a rig and the materials shared fit every front-on probe within 2.3 / 255;
  - the normals are the bevel's: a quarter ellipse, 36 by 18 pt, by the distance in from the outline;
  - the circle bevel's measured table (phase 5a) reads as a quarter circle within its pixels, but its steps band the
    shading, so Scene3D now draws the circle as the quarter circle itself.
- **Every view** (rounds 1 to 3; `fit_light_model.py`): each of Word's points gets its normal from docx4j's own
  geometry.
  - The source is Scene3D's normal map: a hook painted the normal in the drawing's space, (n + 1) / 2, in place of
    the colour, and the test wrote each probe's map, place and view direction. The hook was removed once the fit was
    done; 5b-2 would put it back.
  - The normal map's points are kept where they and their neighbours are opaque and the normal doesn't turn (no
    crease); docx4j's pictures cover at least 99.4% of Word's opaque points on every view.

| Model | Lights a rig | Mean error (77 probes, / 255) | Worst |
|---|---|---|---|
| Blinn | 3 | 6.11 | 25.8 |
| Blinn | 4 | 2.51 | 7.6 |
| Blinn | 5 (grown from 4) | 2.24 | 7.3 |
| **Phong** | **4** | **1.64** | **7.0** |

- **The highlight is Phong's**: the reflection of the light with the view, R.v. With Blinn's half vector the
  front views fit as well but the turned ones don't. Phong's exponents are warmMatte 8.0 and plastic 11.5, plastic's
  strength 1.91 warmMatte's.
- **The lights** (`phase5b-light.tsv`, in the drawing's space for dir t):
  - **threePt:** a key light from the top left in front (diffuse 0.81), a fill from below in front (0.41), another
    from the top in front (0.29), and a strong back light from below behind (1.00), which front views couldn't see.
  - **twoPt:** three lights; the fitted fourth is nil. The strongest light (0.61) is from the bottom left in front, so
    twoPt from dir t lights a drawing from below. The picture styles turn it by rev 120 or 130 degrees, bringing it
    round to the top.
  - **contrasting:** an ambient of 1.00 and four lights with highlights only, at the drawing's sides, nearly in its
    plane.
  - **soft:** an ambient of 0.43; lights from the front (0.38), from the top in the drawing's plane (0.33), from the
    right side (0.27) and from above in front (0.17).
- **The worst probe** (7.0), round 1's extrusion under isometricOffAxis1Right, has walls of its extrusion colour,
  1F3864, where the fit took its fill. Taken as the wall's colour:
  - its face and bevel are 0.90 / 255 off Word's and its walls 11.9;
  - Word's walls are a uniform 12 brighter in each channel: a coloured extrusion takes more highlight than the grey
    walls of round 3 do (section 7).

## 4. docx4j

`Scene3D.lighting` and `Scene3D.shade` follow section 2:
- the rig's lights are turned about the drawing's z by its dir and its rot's rev, then into the camera's space with
  the drawing;
- each light adds its diffuse share of the colour and its Phong highlight, white, with the view (0, 0, 1);
- the face, the bevels, the extrusion's walls and back, and the contour's rim are shaded alike, each in its own
  colour;
- the walls' normal now points out. Phase 5a's pointed in, which only its placeholder shading had used;
- a circle top bevel is lit at its cuts and blended (`Scene3D.cuts`, `blended`; section 5);
- hardEdge's and relaxedInset's profiles are their true ones, grooves and all (section 6).

`render.effect.3d_shading` (`Scene3D.SHADING`) is now reported only where the rig, the material or the rig's rot
isn't fitted (phase 5b-2). Those are drawn with threePt's lights or warmMatte.

**The gate** (`Scene3DTest.theShadingIsWords`, oracle `cr081-word-3d-light.jsonl.gz` from `export_3d_light.py`):
- every probe of the five rounds but r4's white line and whole style: 100, the photo among them;
- docx4j's picture against Word's: at every point of a 1.5 pt grid where both are opaque, round it, the mean colour
  difference is within 6 / 255;
- the mean is 1.82 / 255 and the worst 4.25 (fxg3-32, soft, warmMatte, lon 60).

The 270 silhouettes are unchanged (none past 0.60 pt).

**Corpus 4994's 3-D picture** (twoPt rev 120, a 2 by 1.5 pt bevel, the photo), docx4j's PDF against Word's, at every
0.5 pt where both are opaque:

| docx4j | All | The bevel (2.5 pt band) | Inside |
|---|---|---|---|
| b427 (phase 5a's placeholder shading) | 10.42 | 7.61 | 10.66 (signed -4.14) |
| the fitted lights, lit per pixel | 9.04 | 8.85 | 9.06 (signed -0.24) |
| the fitted lights, the cuts (b430) | 8.86 | 6.57 | 9.06 (signed -0.24) |

What remains inside is the photo, resampled by each, and Word's JPEG.

## 5. A circle bevel is lit at its cuts (rounds 4 and 5)

Word draws 4994's 2 pt bevel without the highlight that lighting each pixel gives: its top side peaks at 144 where
the model has 179.
- **Not the line:** the 7 pt line with no fill changes nothing (fxg4-04 and -05 are identical).
- **Not the rig or the picture:** a 6 by 4.5 pt bevel under the same rig shows the highlight (174 against 179), and
  the photo is lit as the grey shape is.
- **The bevel's scale.** Word's 6 pt bevel is piecewise linear pixel by pixel: rising steadily to a sharp peak at
  3.6 pt in, then falling steadily, its kinks at 1.8, 3.6 and 6.0 pt. Those are where a quarter circle cut into four
  pieces of 22.5 degrees has its vertices (1.76, 3.70, 6.0).

So Word cuts the circle's quarter circle into pieces of equal angle, lights it at the cuts, and blends the colour
between them by the distance in (Gouraud's shading). A narrow bevel has few pieces, and its highlight, between cuts,
is lost. Round 5 counts them (`fit_cuts.py`, the error against Word's four sides for each count):

| Circle bevel's width | Pieces | Error at that count (/ 255) | Lit per pixel |
|---|---|---|---|
| 1.8, 2 pt | 2 | 2.32, 1.99 | 5.48, 7.79 |
| 3, 4, 5, 6, 7.5 pt | 4 | 0.82 to 1.40 | 1.55 to 2.62 |
| 8, 10, 12, 16, 24 pt | 8 | 0.70 to 1.10 | 0.97 to 1.41 |
| 36 pt (rounds 1 to 3) | 16 or more | at the per-pixel fit's | |

- The count goes by the width: 4 by 1.3 pt and 7.5 by 2.5 pt have 4, and 8 by 8 pt has 8.
- Where it doubles between 2 and 3 pt, and between 24 and 36 pt, isn't measured; Scene3D takes 2.5 and 30.
- The cut at angle phi is at t = 1 - cos phi of the width in. Its normal is the circle's there.

## 6. hardEdge and relaxedInset have grooves (round 5)

Phase 5a's profiles are the silhouette's envelope, edge on. On round 5, hardEdge (24 by 12 pt) and relaxedInset
(30 by 9 pt) lit by those profiles were 4.5 and 6.6 / 255 off Word's. Word's colours show surfaces facing in, past
the ridge.

`invert_profiles.py` reads each profile from the shading. With the fitted lights, each depth's slope is the one whose
colours on the four sides are nearest Word's (rms about 0.5 / 255 a depth). The slopes are integrated, and the profile
scaled so that its ridge is the bevel's height (the envelope's 1).
- **hardEdge:** a straight chamfer, slope 2.55 on 24 by 12 pt, to 0.18 of the width; flat to 0.62; then a concave
  fall of about a fifth of its height to the face.
- **relaxedInset:** a rounded rise to a ridge at half the width, then a fall of about a third of its height to the
  face.

So their faces sit below their ridges. Scene3D now has these profiles (65 heights each). They are 1.99 and 2.43 / 255
off Word's, and the silhouettes are unchanged. The other presets' grooves (divot, riblet and any others) are phase
5b-2's; this round read the two the picture styles use.

## 7. Not measured

- **A coloured extrusion's highlight**: 12 / 255 brighter in Word's than the model gives (fxg1-18, 1F3864). Grey
  walls fit.
- **The directions other than t and tl**: taken by the same rule, the rig turned by 45 degrees a step.
- **A perspective camera's view**: the model takes the view at the drawing's centre (0, 0, 1). Word's highlights
  under perspectiveRelaxed fit within 3.7 / 255 that way.
- **Where a circle bevel's cuts double** between 2 and 3 pt, and between 24 and 36 pt (section 5).
- **hardEdge's and relaxedInset's profiles at other sizes**: taken to scale, as their envelopes do.
- **Phase 5b-2** (registry CR-081.5c, deferred): the other 23 rigs, the other 12 materials, a rig's rot by lat or lon,
  the other presets' grooves and cuts.
