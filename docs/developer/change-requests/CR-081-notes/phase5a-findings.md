# CR-081 phase 5a: the 3-D geometry, against Word

Phase 5a draws a 3-D drawing's geometry with a simple shading (CR-081 §6, D5). Its oracle is Word 16.0.20326's PDFs
of purpose-built probes:
- `build_phase0.py`'s sets `cr081-phase5a-r1` and `-r2`;
- read with `read_3d.py`, which writes `phase5a-word.tsv`;
- fitted with `fit_3d.py`, which writes `phase5a-cameras.tsv`.

Each drawing but the layout family's is anchored at a fixed place on the page (216 pt, 216 pt from its top left), in
front of the text, so that its frame is known exactly. The readings are points from the frame's centre, x right and
y down.

## 1. Round 1 (`cr081-phase5a-r1`, 2026-11-11)

181 probes in 10 files, each file twice, Word silent on every job (22479f7, 4.6 to 19.7 s a job). Its 175 fixed probes
are pixel for pixel alike in the two copies: Word doesn't differ from itself.

- **The picture.** As phase 0 found, a 3-D drawing is one picture with a soft mask, in place of the drawing.
  - The silhouette is where the mask crosses one half. `read_3d.py` reads it along every row and column, fits a line
    to each side and intersects them.
  - Every flat probe's sides are straight within 0.2 px.
  - The frame's own corners (orthographicFront) read ±72.01 by -54.04 to 53.98 pt: the frame, within 0.04 pt.
- **A picture projects as a shape.** The photo's 8 cameras (fxc7) read as the shape's do, to 0.01 pt.
- **The turn** (fxc8, orthographicFront with `a:rot`).
  - The frame turns about its centre: first by `lon` about the vertical axis (the right side comes forward), then by
    `lat` about the horizontal (the top comes forward), then by `rev` in the picture's plane, counter-clockwise.
  - All 12 rot probes fit that model as written, within 0.34 pt rms (most within 0.15).
  - lat 330 is lat -30; rev 90 makes the 144 by 108 pt frame 108 by 144.
- **Zoom** (fxc8-19, -20) scales about the centre: 50% gives ±35.99 by ±27.1, and 200% gives ±143.99 by ±108.0.
- **A rot overrides the preset's turn.** isometricTopUp with rot 0 0 0 is the plain frame (fxc8-21);
  perspectiveRelaxedModerately with rot lon 30 is perspectiveFront's with lon 30.
- **Perspective.** A point at depth z (towards the viewer, after the turn) is scaled by K D / (D - z), where
  D = C / tan(fov / 2).
  - Fitted on the 8 perspective rot probes: C 452.9 pt, K 1.0011, 0.38 pt rms.
  - D doesn't depend on the drawing's size: the 144 pt and the 72 pt wide shapes fit one D.
  - The default fov is about 45 degrees: the presets fit 43.4 to 45.4, and Heroic Extreme 80.
- **Depth goes behind.** Extrusion keeps the front face at z = 0 and extrudes towards -z (fxc9-11: orthographicFront
  lon 30, depth 36 pt, reaches 80.41 = 62.35 + 36 sin 30).
- **`z` moves the drawing towards the viewer.** Under perspectiveFront, z 36 pt reads ±74.5 and z -36 pt ±69.7, against
  ±72.14 at 0.
- **Oblique** cameras draw a flat drawing as its frame.
  - With depth, the back face is offset towards the named corner: 0.21 of the depth for obliqueTopLeft (fxc9-02, -14),
    and 0.35 for legacyObliqueTopLeft (fxc9-09).
  - The legacy perspective presets draw a flat drawing as perspectiveFront does (scale 1.002).
  - With depth, legacyPerspectiveTopLeft recedes towards the top left (fxc9-10).
- **The presets** (`phase5a-cameras.tsv`): every preset fits the model within 0.03 to 0.35 pt rms. For example:
  - isometricTopUp: lat -35.40, lon -45.25, rev 60.14;
  - isometricOffAxis1Right: lat 17.93, lon -25.87;
  - perspectiveLeft: lon 19.80, fov 44.5;
  - perspectiveAbove: lat 19.83;
  - perspectiveContrastingRightFacing: lat 10.40, lon -43.91, rev 3.56;
  - perspectiveHeroicExtremeLeftFacing: lat -8.10, lon -34.43, rev -2.92, fov 80;
  - perspectiveRelaxedModerately: lat ±35.08.

  But a flat drawing can't tell a view from its mirror: isometricTopUp and isometricBottomUp read alike, as do 7 other
  pairs. Their signs wait on round 2's depth.

## 2. Round 2 (`cr081-phase5a-r2`, 2026-11-11)

78 probes in 4 files, each twice, Word silent (4.3 to 5.1 s a job). Read with `fit_depth.py` (the cameras with depth),
`bevel_profiles.py` (the bevels edge on) and `fit_all.py` (every preset on all its readings at once).

- **The mirror pairs are settled by depth.** For each preset, the view and its mirror were scored against Word's
  extruded silhouette: the right one fits within about 0.1 pt rms, the other 6 to 23 pt off. Examples:
  - isometricTopUp: lat -35.35, lon -45.19, rev 60.09;
  - isometricBottomUp: lat 35.38, lon 45.20, rev 60.11;
  - perspectiveAbove: lat -19.78, where round 1 read +19.83;
  - perspectiveRelaxed: lat -50.31.
- **Oblique** cameras shear the depth towards the corner they name:
  - 0.30 of the depth for an oblique, 0.214 on each axis for a diagonal;
  - 0.50 for a legacy oblique, 0.36 on each axis;
  - legacyObliqueFront doesn't shear.
- **A legacy perspective** is a camera about 720 pt away with its eye moved towards the side it names, 174 pt across
  and 156 pt up or down. A point at depth z is drawn at K x D / (D - z) + e (1 - D / (D - z)).
  - All 9 fit within 0.11 pt.
  - Perspective plus a shear had fitted the same pictures only to 0.9 pt at worst.
- **Every preset**, fitted on all its readings (`phase5a-presets.tsv`), is within 0.42 pt of Word's silhouettes, most
  within 0.31.
  - The turn order is lon, then lat, then rev: each other order is 4 to 32 pt off on the rot family.
  - The perspective presets' fov reads 43.2 to 45.6, and Heroic Extreme 79.6 to 79.9.
- **The bevels.** A top bevel cuts into the drawing: its face stays at z = 0, inset by the bevel's width, and slopes
  back to z = -h at the outline. The extrusion is behind that, then a bottom bevel, narrowing to the back (fxd4-15).
  - Seen edge on, each column of the picture is one depth, so the silhouette's top traces the profile
    (`phase5a-bevels.tsv`, 33 samples each).
  - angle is linear. circle is a quarter circle, within a pixel of the analytic one.
  - The others are Word's own shapes, kept as the measured tables.
  - The profile scales with the bevel's width and height: the circle at 12 by 24 and 24 by 12 pt reads within 0.02.
  - A silhouette shows the profile's envelope: a groove inside it (divot, riblet) doesn't show, and waits on the
    shading (phase 5b).
- **Word's placement.** Every picture's box is on a 1/300 inch grid (a whole number of 0.24 pt), and its pixels aren't
  resampled to the drawing. So a silhouette can be up to 0.24 pt off its geometry: the flat obliques read 0.24 pt
  larger on one or two sides.

## 3. The renderer (`docx4j-chart-render`, `api/Scene3D`)

- The model as read:
  - each preset's turn and projection, generated from `phase5a-presets.tsv`;
  - a rot, fov or zoom over the preset's;
  - the perspective constant C 452.85 pt and scale K 1.0011;
  - the legacy perspectives' eye; the obliques' shear.
- **The front** is a height field over the drawing's outline at the picture's resolution:
  - the distance in from the outline is computed exactly (Felzenszwalb and Huttenlocher's transform);
  - the bevel's height is the profile at that distance;
  - the face's colours are the drawing as painted, at 200 dpi.
- **The sides** are the outline extruded from the bevel's foot to the back. They start at the first grid sample's
  height, so that a steep profile (softRound, slope) reaches its foot at the outline. Then come the back's bevel and
  face.
- **Drawing:** triangles z-buffered at twice the picture's resolution, then averaged down. An antialiased edge's
  partial alpha is composited, so that it leaves no seam over the sides behind it.
- **The shading** is simple: a diffuse light from the light rig's direction, normalised so that a face turned to the
  viewer keeps its colour. `render.effect.3d_shading` reports it (phase 5b).
- **The contour**: section 7.
- **`Scene3DTest`**:
  - every fixed probe of both rounds, 251 (contours aside), is drawn and read as Word's are;
  - each silhouette's hull vertices are measured against the other's boundary;
  - 242 are within a pixel (0.36 pt), none past 0.60 pt (a pixel plus Word's 0.24 pt placement grid; the gate, Jason 2026-11-11: "0.60pt is ok"), and the worst
    is 0.49 pt (fxc8-05).
  - The oracle is `cr081-word-3d.jsonl.gz`, from `export_3d.py`.

## 4. In the outputs

`Scene3D` is called from both drawing paths, so a document's 3-D drawing is drawn in the PDF and HTML outputs, by
the visitor and the XSLT alike.
- **A shape** (`ShapeRenderer.effects`): a single shape, and each shape of a group or canvas.
  - Its scene3d and sp3d are its spPr's, else those of the theme effect style its `effectRef` names, phClr the
    reference's colour.
  - Its fill, line and picture fills as painted are the 3-D picture's face, and the picture takes their place.
- **A picture** (`ShapeSvgDrawer.drawPicture`, core's `PictureEffects`):
  - the picture in its frame, its line and the fill behind it are the face;
  - the 3-D picture is a REPLACE effect picture, so that the picture, the fill and the line aren't drawn under it;
  - core counts a 3-D picture as framed, and reads its pixels for the face.
- **Its effects** (shadow, glow, reflection, soft edge) are cast by the 3-D picture. They are computed over its bounds,
  which may reach past the frame. Word's one picture holds them; docx4j's are under it.
- **The face's edge.** It ends exactly at the outline: a signed distance moves each edge cell's outside corners onto
  it, so that the supersampling alone antialiases the edge.
  - An edge cell's partial alpha had antialiased it twice: a two-pixel ramp, and corners rounder than Word's.
  - The bevel's inset is measured from the outline too, half a cell nearer than before.

Checks:
- **The PDF route.** Round 1's fxc1, fxc3, fxc7 and fxc9 were rendered through docx4j's PDF and read as Word's are.
  Every 3-D probe but the two contours is within 0.27 pt of Word's silhouette by hull distance.
- **Gate b425** against b424: 0 movers, events equal, probes unmoved.
  - One corpus drawing is 3-D: 4994's (real3) picture style, a 1.5 pt top bevel. It was drawn flat before; its
    silhouette is now within 0.37 pt of Word's.
- **Tests:**
  - `Scene3DTest.a3dShapeIsOnePicture`;
  - `PictureEffectsHtmlTest.a3dPictureIsOnePictureInItsPlace` and `PictureEffectsFoTest.a3dPictureIsOnePictureInItsPlace`
    (both routes);
  - chart-render 58, export-fo-tests 951, core-tests 2631.

## 5. Round 3 (`cr081-phase5a-r3`, 2026-11-11): a 3-D shape's text, and its effects

25 probes in 2 files, each twice, Word silent (4.6 to 5.4 s a job; Jason: "please do the measures: text on a 3-D
shape, and how a shadow on a 3-D drawing is aligned").

**The text** (fxe1: AB, bold 22 pt, on a light shape):
- **A front camera** (orthographicFront, with a bevel): the text is the PDF's own text, flat, where a flat shape's is
  (58.4 to 85.8 pt across the frame, 40.7 to 55.3 down).
- **Any other camera**: the text isn't in the PDF's text layer. It is drawn into the 3-D picture, turned with the face.
  - isometricTopUp, orthographicFront lon 45, perspectiveContrastingRightFacing and lat 30 lon 30 rev 30: the text's
    pixels are on the face, centred on the frame.
  - Under lon 45 it is 18.3 pt across against 27.4 flat, about cos 45.
  - The obliques too, though an oblique doesn't turn the face (obliqueTopLeft with depth).
- **`a:flatTx`** (keep text flat, in the bodyPr): the text is the PDF's own text again, flat, at its unturned place,
  under isometricTopUp and lon 45 alike.

**The effects** (fxe2: an unblurred black shadow at 50%, 18 pt at 45 degrees, on a light shape).
- **The shadow is the frame's, cast in the drawing's plane, and projected with it.** The frame is moved by dist along
  dir and scaled about its algn point there, then turned by the camera. `read_shadow_3d.py` and the hypotheses against
  the visible shadow (alpha about one half):

  | Probe | Object plane, projected | 3-D silhouette, moved on the page | the frame, moved on the page |
  |---|---|---|---|
  | isometricTopUp | 0.968 | 0.307 | 0.242 |
  | isometricTopUp, the photo | 0.968 | 0.307 | 0.242 |
  | perspectiveContrastingRightFacing | 0.956 | 0.706 | 0.539 |
  | orthographicFront lon 45 | 0.963 | 0.925 | 0.930 |
  | orthographicFront, bevel 6 pt | 0.970 | 0.943 | 0.943 |
  | isometricTopUp, blurred 6 pt | 0.930 | 0.283 | 0.208 |

  (the overlap with Word's shadow, intersection over union).
  - The shadows scaled 50% about ctr, tl and br stay inside the frame, so they're hidden behind the drawing, as the
    object-plane reading has it. On the page, about tl or br, they would show beyond the rhombus.
  - With depth, the shadow is behind the face: obliqueTopLeft's fits better on the back face's plane (0.858) than on the
    front's (0.817). isometricOffAxis1Right's is all but hidden; not fitted.
- **The reflection and the glow are the same.**
  - The reflection, mirrored below the frame in the drawing's plane and projected, overlaps Word's at 0.891; mirrored on
    the page, at 0.003.
  - The glow, round the frame in its plane and projected: 0.62, against 0.45 round the silhouette on the page (a rough
    model of the glow's edge either way).
- **Word's one picture holds them**, drawn under the drawing.

**What docx4j does now** (Jason: "yes and yes"):
- **A 3-D drawing's effects are the flat drawing's, drawn in its plane.** `Effects.draw` casts them from the flat
  drawing in its frame, as for a flat one. Then:
  - a soft edge (in the face's place) and an inner shadow (over it) are composited into the face;
  - the reflection, the preset shadow, the outer shadow and the glow, in Word's order, are drawn by `Scene3D` as
    pictures on the drawing's plane, behind its back where it has depth, and z-buffered, so that the drawing hides
    what is behind it;
  - all of it is in the one picture, as Word's is.
  - Through docx4j's PDF, against Word's of fxe2:

    | Probe | The drawing | The effect |
    |---|---|---|
    | isometricTopUp (and the photo) | 0.991 | 0.930 |
    | perspectiveContrastingRightFacing | 0.991 | 0.941 |
    | lon 45 | 0.993 | 0.925 |
    | obliqueTopLeft with depth | 1.000 | 0.993 |
    | the blurred shadow | 0.991 | 0.931 |
    | the reflection | 0.991 | 0.969 |
    | the glow | 0.990 | 0.822 |

    The shadows hidden behind the drawing are hidden in both.
  - `Scene3DTest.theEffectsAreInTheDrawingsPlane` compares the renderer's with Word's pictures
    (`cr081-word-3d-effects.jsonl.gz`, from `export_3d_effects.py`) on a grid a third of a pixel apart. At a pixel's
    pitch, a sliver a pixel wide turns on the grid's phase (0.99 or 0.76).
- **A 3-D shape's text is reported where Word would turn it.** Under a camera other than orthographicFront, with text
  and without a:flatTx, `render.effect.3d_text` is reported (`Scene3DTest.turnedTextIsReported`). docx4j's text is the
  output's, flat.

## 6. The layout (round 1's fxca, read 2026-11-11)

Word lays a 3-D drawing out by its frame, not by its 3-D picture.
- **Inline in a line of words** (fxca-01 to -03): the text is the same with isometricTopUp's 179 pt wide picture as
  with the flat 144 pt control. The words after the drawing start at 435.29 in both, the frame's width on from 288.43.
- **Square wrap** (fxca-04, -05): the wrapped text is the same with and without the 3-D. isometricTopUp's picture
  overlaps it by about 8 pt on the right and 1.7 pt on the left.

docx4j does the same: its 3-D picture doesn't move its text. b426's render of fxca has the same lines with the 3-D as
with the flat controls.
- Its flat controls differ from Word's, for reasons that aren't 3-D's:
  - an inline shape in a line of words is set off the line where Word sets it (x 288.4, after "Words before"), and
    the paragraph's words start lower;
  - the square wrap runs text on one side of the drawing, where Word runs it on both (the FOP fork's CR-024 phase B).

## 7. The contour (rounds 4 and 5, `cr081-phase5a-r4`, `-r5`, 2026-11-11)

Jason: "yes" to the contour round. Round 4: 11 probes, a red contour (contourW, contourClr) on a blue shape. Round 5:
6 probes, the contour seen edge on (lon 90) and from above (lat 90), so that its section shows, as the bevels' did.
Word was silent on both.

- **Seen from the front, the contour is a band of its width centred on the outline.** The silhouette grows by half
  the width, and the face is the contour's colour inside the outline by the other half.
  - 3 pt: the silhouette is ±73.8 pt on the frame's ±72; the blue face ends at ±70.6.
  - 6 pt: 68.8 to 75.2.
  - 12 pt: 65.5 to 78.1.
- **Seen edge on, it is a rim round the sides, behind the face.**
  - With no depth and no bevel, a 6 pt contour spans depth 0 to 6 pt and a 12 pt one 0 to 12. The rim reaches half
    the width out at its middle (57.09 and 59.97 on the frame's 54), and less towards its ends.
  - With 36 pt of depth it runs the whole extrusion (0 to 42 pt), half the width out between rounded ends.
  - With a 12 pt top bevel it is at the bevel's foot. The bevel in front of it keeps the face's colour.
  - Turned, its side that comes forward reaches further (round 4, lon 45: 5.3 pt past the turned frame against
    1.0 pt).
- **The rim's section** has its reach out against its depth from its middle, both in half-widths, alike for 6 and
  12 pt: 1 at 0, 0.88 at 0.25, 0.77 at 0.45, 0.66 at 0.6, 0.55 at 0.75, 0.44 at 0.87, then a blunt end at 0.9.
  - A quarter circle reaches too far at its ends: 0.87 pt off Word's on the 12 pt contour edge on. A 0.9 ellipse
    still gave 0.65 pt.

**docx4j** (`Scene3D.contourRim`):
- the sides, from the top bevel's foot (or half the width behind the face, where there is no bevel) to the back, are
  swept with that section, outwards along the outline's normal;
- the corners are rounded, the normal turned between a vertex's two segments;
- the face's outer half-width is the contour's colour where there is no top bevel;
- the effects' plane is behind the rim.

All 17 contour probes are now in `Scene3DTest.theSilhouettesAreWords`. With the earlier 253: 270 silhouettes, 256 within
a pixel, none past 0.60 pt, the worst 0.48 (fxc8-05, not a contour).

## 8. Not measured yet

Gathered with the other phases' open items in phase 6's gap report (`phase6-gap-report.md`, sections 3 and 4).
