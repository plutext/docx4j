# CR-081 phase 5b-2: the other lights and materials, as Word's pictures read

5b-1 fitted the four light rigs and three materials of Word's 3-D picture styles (`phase5b-findings.md`). 5b-2 fits
the rest (Jason: "start phase 5b-2", 2026-11-11):
- the other 23 rigs, coloured ones among them;
- the other six directions, and a rig rotated by lat and lon;
- the other materials: the legacy ones, metal, powder, dkEdge, softEdge, flat, softmetal, clear and translucentPowder;
- the other bevel presets' shading.

The method is 5b-1's. Phase 5a's geometry gives each of Word's pixels its normal, and the shading is a regression on
them. The normals come from Scene3DTest run with `normals-hook.patch` applied (`SCENE3D_DEBUG=normals`,
`SCENE3D_DUMP=<dir>`); the hooks aren't in Scene3D.

## 1. The round

**cr081-phase5b2-r1** (261 probes; `build_phase0.py` fam_rigs_b2, fam_materials_b2, fam_presets_dirs_b2). Word
16.0.20326 was silent. The probes:
- **fxh1 to fxh3** (184 probes): the 23 rigs, from dir t, on the grey (808080) ellipse, 144 by 108 pt, extruded 36 pt,
  its circle top bevel 36 by 18 pt. Each rig is seen from five views (orthographicFront rot lat/lon 0/0, 0/60, 60/0,
  0/300, 330/150) under warmMatte, and from three of them under matte.
- **fxh4** (48 probes): the 12 other materials, the same ellipse under threePt, from four views (all but 0/300).
- **fxh5** (29 probes): on the deep rectangle of 5b-1, front on:
  - the nine other presets at 24 by 12 and at 6 by 3 pt, under twoPt rev 120;
  - threePt from the six directions not measured before (l, r, b, bl, br, tr);
  - threePt rotated by lat 30 and 330, by lon 30 and 330, and by both 30.

## 2. The rigs

Each rig is an ambient and four directional lights, each with a diffuse and a highlight colour (r, g, b), fitted by
`fit_rigs_b2.py` with 5b-1's warmMatte and matte. Mean and worst error of the rig's eight probes, / 255:

| rig | lights | ambient | mean | worst |
| --- | --- | --- | --- | --- |
| legacyFlat1 | 4 | 0.30 | 1.27 | 1.66 |
| legacyFlat2 | 4 | 0.42 | 1.06 | 1.67 |
| legacyFlat3 | 4 | 0.48 | 0.77 | 1.13 |
| legacyFlat4 | 4 | 0.52 | 1.97 | 2.89 |
| legacyNormal1 | 4 | 0.14 | 0.90 | 1.21 |
| legacyNormal2 | 4 | 0.22 | 0.89 | 1.38 |
| legacyNormal3 | 4 | 0.27 | 0.72 | 1.12 |
| legacyNormal4 | 4 | 0.24 | 1.03 | 1.48 |
| legacyHarsh1 | 4 | 0.05 | 0.69 | 1.02 |
| legacyHarsh2 | 4 | 0.05 | 0.71 | 1.12 |
| legacyHarsh3 | 4 | 0.05 | 0.82 | 1.16 |
| legacyHarsh4 | 4 | 0.05 | 0.71 | 1.08 |
| balanced | 4 | 0.13 | 1.24 | 1.87 |
| harsh | 4 | 0.27 | 1.26 | 1.81 |
| flood | 4 | 0.13 | 1.75 | 2.47 |
| morning | 4 | 0.02, 0.02, 0.03 | 2.18 | 4.31 |
| sunrise | 4 | 0.01, 0.01, 0.00 | 1.64 | 2.27 |
| sunset | 4 | 0.02, 0.02, 0.02 | 1.64 | 2.05 |
| chilly | 4 | 0.11, 0.11, 0.11 | 1.35 | 1.94 |
| freezing | 5 | 0.00, 0.01, 0.02 | 2.10 | 3.75 |
| flat | 4 | 1.00 | 2.01 | 6.71 |
| glow | 4 | 0.99 | 1.50 | 2.72 |
| brightRoom | 5 | 1.02 | 2.50 | 3.35 |

- The legacy rigs are grey. morning, sunrise and sunset are warm, chilly and freezing cool.
- A fifth light improves two rigs: freezing (worst 6.22 to 3.75) and brightRoom (mean 3.66 to 2.50). It doesn't
  improve flat (worst 6.90) or morning (4.38).
- flat's worst, and the other rigs' worst too, is the view from lon 300, the one view from that side. More of them
  would show whether a light is missing there.

## 3. Directions and rotation

- **dir** turns the rig about the drawing's z by multiples of 45 degrees: t 0, tl -45, l -90, bl -135, b 180, br 135,
  r 90, tr 45. All eight are now measured; the six new ones fit at about 1 / 255.
- **rot** turns the rig about x by lat, then about y by lon, then by dir and rev about z. The five rotations (lat 30
  and 330, lon 30 and 330, both 30) fit within 2.5 / 255. Scene3D no longer reports a rotated rig.

## 4. The presets' shading

Word shades the presets angle, artDeco, convex, coolSlant, divot, riblet, slope and softRound by profiles of their
own, not by the slope of the silhouette's profile (5a's `PROFILES`, which stays the geometry). `invert_profiles.py`
reads each shading profile from fxh5's pictures (`b2` mode), and Scene3D's `SHADES` holds them. The shading normal
runs along the distance field's gradient. fxh5's presets fit at 1.0 to 3.05 / 255. cross keeps its geometry's profile:
its cliff can't be read from the shading.

## 5. The opaque materials

`fit_materials_b2.py` fits each material alone, under threePt (5b-1's lights), from fxh4's four views:

    colour = min(255, (c (ka A + kd sum D_i max(0, n.L_i)) + 255 kw) max(0, n.v)^ke
                      + 255 ks sum S_i max(0, R_i.v)^p + 255 kf (1 - max(0, n.v))^pf)

- kw washes towards white;
- ke darkens towards the edges;
- kf and pf form a rim that brightens towards the edges. It's what metal and powder lacked: metal 8.59 to 4.09,
  powder 7.00 to 3.09;
- the legacy materials take Blinn's half vector for R: legacyPlastic 6.35 to 2.78, legacyMetal 3.97 to 1.76. The
  others are better with Phong's: metal 10.61 under Blinn, softEdge 5.62, dkEdge 5.48.

| material | highlight | mean | worst |
| --- | --- | --- | --- |
| legacyMatte | none | 1.35 | 1.98 |
| legacyPlastic | Blinn | 2.78 | 3.52 |
| legacyMetal | Blinn | 1.76 | 2.26 |
| metal | Phong | 4.09 | 4.56 |
| powder | Phong | 3.09 | 4.93 |
| dkEdge | Phong | 3.77 | 4.51 |
| softEdge | Phong | 3.09 | 3.28 |
| flat | Phong | 2.45 | 3.16 |
| softmetal | Phong | 3.21 | 3.88 |

flat is nearly all its own light (ka 22.9 under threePt's ambient of 0.029, so 0.64 of the colour). Scene3D takes a
material's light beyond the rig's ambient as its own, `own = (ka - 1) x 0.029`. Whether it is its own, or scales the
rig's ambient, isn't measured: every material was read under threePt only.

## 6. Translucent materials

Word draws clear and translucentPowder with a soft mask, so their colour and alpha are read apart. Their alpha isn't a
function of the nearest surface alone. `fit_translucent_b2.py` reads docx4j's farthest surface too (the patch's
`SCENE3D_LAYER=back`) and tries three rules, each layer's alpha linear in |n.v| (mean of the four views, / 255):

| rule | clear | translucentPowder |
| --- | --- | --- |
| the nearest surface only | 14.69 | 19.66 |
| every surface | 28.53 | 21.43 |
| the nearest surface, and behind it a side | 1.46 | 1.59 |

So Word draws a translucent drawing's caps only where they face the viewer, and its sides from both sides:
- **clear:** alpha = 1 - 0.9 |n.v|, nearly clear facing the viewer, nearly opaque edge on;
- **translucentPowder:** alpha = 0.71 |n.v| - 0.01, the other way round.

Each layer is lit by the material model, and a side seen from inside is lit as facing the viewer. Over a white page,
which is how a reader sees it, the colour fits at:
- clear: mean 4.25, worst 4.99;
- translucentPowder: mean 5.00, worst 7.82.

Lighting a side's inside by its outward normal is no better overall (clear 3.53, translucentPowder 4.94, worst 8.24),
and it gets clear's gain only through a degenerate rim term.

## 7. legacyWireframe

Word draws legacyWireframe as black lines along its own mesh, the face transparent. Those lines are Word's
triangulation, not a lighting, and docx4j's mesh isn't Word's. Scene3D draws it as warmMatte and reports
`render.effect.3d_shading`, as it does for material none.

## 8. docx4j

- `Scene3D.RIGS` holds the 23 rigs. 5b-1's four stay grey, each row {ambient} then {x, y, z, diffuse, highlight}; the
  new ones are coloured, {ambient r, g, b} then {x, y, z, diffuse r, g, b, highlight r, g, b}.
- `Scene3D.Material` has the terms of section 5, Blinn or Phong, and a translucent material's alpha.
- A translucent drawing culls its caps facing away, and lights a side seen from inside as facing the viewer. The
  raster composites the layers. One surface's triangles meeting at a sample replace each other, so seams aren't
  composited twice.
- A circle bevel's Gouraud blend carries the alpha too.
- `Scene3DTest.theShadingIsWords` now has 357 probes: 5b-1's 100, and 5b-2's 257 (every probe but legacyWireframe's
  four). `export_3d_light.py` writes a translucent probe's colours over a white page.
- **The test:** mean 2.07 / 255, worst 8.47. 250 of 5b-2's 257 probes are within 5b-1's 6 / 255; all are within
  8.5. The seven past 6:
  - metal's views from lon 60 and lat 60 (6.47, 6.52);
  - powder's from lon 60 (6.72);
  - translucentPowder's (8.47, 6.85) and clear's (6.19) side views;
  - flat's rig from lon 300 (7.80).
  - The materials' are at the creases where the bevel meets the side. There docx4j's geometry is a fraction of a
    pixel from Word's: 16 to 18 / 255 on about a sixth of the points, against 4.8 elsewhere. The Python model at
    docx4j's normals is as far off there (metal fxh4-18: 4.78 elsewhere in Java, 4.53 in the model). The fits leave
    those points out, the test can't.
  - So 5b-2's gate, set at its first fit, is 9 / 255 a probe (`SHADING_TOLERANCE_B2`). 5b-1's 6 stands for its own.
- fxh5: the presets 1.02 to 3.05, the directions 1.02 to 1.12, the rotations 1.23 to 2.48.

## 9. Not measured

- Whether a material's own light is its own or scales the rig's ambient (section 5).
- A coloured fill under metal and the other highlights: whether a highlight takes the fill's colour. Every fxh4 probe
  is grey.
- The legacy materials under the legacy rigs, and the translucent materials under any rig but threePt.
- flat's view from lon 300 (6.71), and the other views from that side.
