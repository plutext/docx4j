# CR-081: picture and shape effects drawn - outer and inner shadow, glow, soft edges, reflection

Status: PROPOSED 2026-11-09. Drafted on Jason's request (the four CRs of 2026-11-09; see CR-080's Status). Not
started; D1 to D10 open (§8). Phase 1 (pictures in a rectangle) needs nothing from CR-080. Phases 2 and 3 draw with its
geometry. Read against docx4j c0a66b610, Apache POI trunk ae62bb5116 and docxide-pdf 9508da2, and the specifications in §12.
Revised 2026-11-09 (Jason: "add a rendering phase to the CRs where appropriate: WordArt or SmartArt we can't output is
a gap"):
- phase 5 draws 3-D (bevels, depth, lighting), which 3-D WordArt (CR-083) and 3-D SmartArt styles (CR-082) need,
  where the earlier draft left it out (D5);
- preset shadows are drawn whatever their count (D6);
- phase 6 reports what is still not drawn (D9).

Reviewed 2026-11-09 by a Claude Fable 5.1 session at Jason's request. Its findings were applied on Jason's word
("apply the review's findings"):
- a cap on an effect picture's size (D10);
- the picture kept as an SVG `<image>`, so that FOP passes a JPEG through (§5.4);
- the JPEG statement corrected (§7);
- theme effect style 3's shadow (§5.5);
- `wp:effectExtent` applies to inline pictures only (§1);
- 3-D behind a go/no-go after phase 0 (D5, §11).

Owner: Jason Harrop. Drafted with Claude Opus 5.5.

## 0. Summary

A picture with a Word picture style (a shadow, a glow, soft edges, a reflection, a rounded or oval frame) comes out of
docx4j's PDF as the bare rectangle. This CR draws a DrawingML `a:effectLst` as Word draws it in a PDF: as pictures
under or over the drawing, blurred, in the effect's colour and alpha. It covers `a:outerShdw`, `a:innerShdw`, `a:glow`,
`a:softEdge` and `a:reflection`, on pictures and, once CR-080 draws them, on shapes and text boxes. A picture's frame
geometry (`pic:spPr/a:prstGeom` other than `rect`) clips it, which most picture styles need.

Most of the drawing exists. docx4j-chart-render draws an outer shadow (distance, direction, scale, skew, alignment) and
a glow as blurred PNG pictures, measured against Office's PDFs (CR-037 finding 6). This CR moves that into the general
drawing and adds the other three effects.

## 1. What docx4j does today

- **Pictures** (`FOPictWriter*`, `AbstractWordXmlPicture`): the picture is drawn in its extent, `a:srcRect` cropped. Its
  `pic:spPr` is otherwise not read: no `a:effectLst`, no frame geometry, no outline (`a:ln`). `wp:effectExtent` is applied
  to an inline picture only, as padding on its graphic (`WordXmlPictureE20.stampEffectExtent`). An anchored picture's
  effect extent is left to the anchor's wrap geometry, by that method's own javadoc.
- **Charts** (docx4j-chart-render, CR-037): `Fills.shadows(CTShapeProperties)` reads `a:outerShdw` and `a:glow`.
  `Drawing.Shadow` draws either as the shape's outline moved, scaled and skewed about its alignment point (or, for a
  glow, grown by its radius), filled, blurred and written as a PNG with alpha through `SvgWriter`. FOP writes the alpha
  as the image's `SMask`. Its constants: a glow spreads 0.5 of its radius and blurs with sigma 0.25 of it (r10, against
  Excel's picture: 0.15% of pixels off).
- **The model**: every effect is bound (`org.docx4j.dml.CTOuterShadowEffect`, `CTInnerShadowEffect`, `CTGlowEffect`,
  `CTSoftEdgesEffect`, `CTReflectionEffect`, `CTPresetShadowEffect`, `CTEffectList`, `CTEffectStyleItem`), and colours
  resolve through CR-034.

## 2. What Word does (measured for charts; for pictures and shapes, what the standard says, for phase 0 to confirm)

- Office draws shadows, glows and bevels in a PDF as pictures, and gradients as vector shadings (CR-037 §4.5, for
  charts).
- An effect follows the drawing's outline, not its bounding box: a shadow under an oval picture is oval.
- `a:softEdge rad` fades the drawing's own alpha inward by its radius, only in the band between the outline and the
  inset ([MS-OI29500] 2.1.1306; §12). It changes the picture, not what is under it.
- An outer shadow in a `spPr` is drawn only at `sx` and `sy` of 100% (2.1.1303). Phase 0 checks this against Word's
  scaled picture-style shadows.
- `wp:effectExtent`'s `l`, `r` and `t` are the left, right and top edges (2.1.1352).
- `a:reflection` repeats the drawing, flipped, starting `dist` from it along `dir`, faded from `stA` at `stPos` to
  `endA` at `endPos` along `fadeDir`, scaled by `sx` and `sy`, blurred by `blurRad`. That is Office's reading
  ([MS-OI29500] 2.1.1304), which swaps the standard's `dir` and `fadeDir`.
- `a:innerShdw` darkens the inside of the outline from the edge, offset by `dist` along `dir`, blurred by `blurRad`.
- Order: Word draws the outer shadow, glow and reflection under the drawing, and the inner shadow and soft edge on it.

## 3. Sources evaluated

- **docx4j-chart-render** (docx4j's own): outer shadow and glow, measured. **Generalise it** (move `Drawing.Shadow`'s
  rasterising into a `drawing` package of the same module (CR-080 D2), so that charts and shapes share one
  implementation).
- **Apache POI**: `DrawSimpleShape.drawShadow` moves the outline and fills it in the shadow's colour, with no blur, scale,
  skew or alignment, and no other effect. POI has no inner shadow, glow, soft edge or reflection. **Nothing to port.**
- **docxide-pdf** (`src/pdf/images.rs` `embed_shadow`, `embed_reflection`, `embed_image_effects`; `src/pdf/color.rs`
  `generate_shadow_mask`, `generate_inner_shadow_mask`): every effect is drawn on the picture's rectangle (a box,
  not its outline). The blur is three box-blur passes standing in for a Gaussian, at a reduced resolution. A
  reflection is the picture re-embedded with a vertical alpha gradient. Its roadmap says what it hasn't done: effects on
  shapes and text boxes, preset shadows, theme colours in effects (an inner shadow's `a:schemeClr` falls back to black),
  and effects in headers and footers (embedded but never drawn); its soft-edge and reflection masks are built on the
  uncropped source. **Port nothing.** Its reflection's alpha ramp (`stA` at the near edge to `endA` at `endPos`, the
  rows past `endPos` transparent) is the standard's reading, and phase 0 checks it against Word.
- **office-open-xml-viewer** (yukiyokotani, MIT, `packages/core/src/shape/effects.ts`, a4f7785): outer shadow, inner
  shadow, soft edge and reflection on a shape's **silhouette** (its fill and stroke painted opaque, then blurred and
  composited on auxiliary canvases), not on its rectangle. That is the approach here, in Canvas terms. **A reference
  for the compositing order**, and nothing is ported: Java2D's `BufferedImage` operations do the same, and chart-render
  already does it for outer shadow and glow. Its `packages/core/src/image/blip-effects.ts` (49,761 bytes in the repository
  tree at a4f7785; not in this review's sparse clone) covers D7's colour effects if that follow-up is taken.
- **pptx-renderer** (aiden0z, Apache-2.0, `src/renderer/ReflectionRenderer.ts`): a reflection kept in the shape's
  local coordinates, with the alpha ramp on a separate layer so that the fade keeps its direction under a negative
  vertical scale. Its README says its outer shadows and reflections are validated against PowerPoint for the
  combinations in its matrices, and approximated otherwise. **A cross-check** on the reflection's geometry.

## 4. Corpus facts (2026-11-09, the four fidelity corpora)

A picture with an effect: real 0, real2 0, real3 3 documents (3), real-c2 1 (1). A shape with an effect: none. The
fidelity corpora don't exercise this, so the oracle is probes (phase 0), plus the load survey's count of `a:effectLst`
under `pic:spPr` (through the portfolio).

## 5. Approach

1. **One effect renderer** (docx4j-chart-render, `org.docx4j.drawing.render.Effects`, D2):
   `draw(Shape outline, CTEffectList, theme)` returns the pictures to draw under and over, each a PNG with alpha and its
   place in points. The picture itself is not passed through it, except for a soft edge (§5.4). The blur is a separable Gaussian with sigma from the radius, as
   chart-render's (one rule, measured).
2. **The resolution of an effect picture** (D3) is a property, `docx4j.convert.out.effects.pixelsPerPoint`, default 2.
   A blur is low-frequency, so the picture can be coarser than the page. Phase 0 checks file size against Word's PDF.
   Its pixel size is capped (D10).
3. **Pictures**: the frame geometry (CR-080's `ShapeGeometry`, phases 2 and 3) clips the picture and gives the effects
   their outline. Before CR-080, phase 1 handles the rectangle. `a:ln` on a picture is drawn as CR-080 draws a line.
4. **Into the FO**: a picture with effects becomes one SVG in the container the picture already has, so
   `WordLayoutFixups` places it unchanged. The SVG holds:
   - the effect pictures;
   - the picture itself as an SVG `<image>` (a data URI or the resolved part), clipped by an SVG `clipPath` from the
     frame geometry;
   - the outline.

   An `<image>` keeps FOP's JPEG passthrough: `PDFImageElementBridge` takes the raw JPEG to
   `PDFGraphics2D.addNativeImage` and `ImageRawJPEGAdapter`. A `BufferedImage` drawn into the SVG would be re-encoded
   as a Flate bitmap for every picture with any effect. Only a soft edge needs the picture's own pixels changed (§7).
   For an inline picture, `wp:effectExtent` already reserves the space the effects take beyond the extent. For an
   anchored one, the effects can reach past the wrap box. Word's wrap uses the extent plus the effect extent
   ([MS-OI29500] 2.1.1352's edges), and phase 1 measures and follows that.
5. **Style references**: `a:effectRef idx` resolves to the theme's `a:effectStyleLst` (CR-034's theme reading). In all
   three themes docx4j ships (`theme-2007.xml`, `theme-2013.xml`, `theme-2023.xml`), styles 1 and 2 are empty and
   style 3 is an `a:outerShdw` (`blurRad` 57150, `dist` 19050, `dir` 5400000, black at 63%). So `effectRef idx="3"`
   draws a shadow under every Office theme docx4j ships, and phase 0 probes it.

## 6. Phases

- **Phase 0, the oracle.**
  - Probes: one photo and one line-art PNG, each with every Word picture style (28 presets); then each effect alone,
    swept over its parameters: outer shadow distance, direction, blur, scale, skew and alignment; inner shadow; glow
    radius; soft edge radius; reflection with its eight attributes.
  - Also: a shape with `a:effectRef idx="3"` under the 2013 and 2023 themes; an anchored picture with a shadow and
    square wrapping, to see where Word's wrap goes; a JPEG with a picture-style shadow, to compare file size with
    Word's.
  - Hostile probes for D10: an extent near 2^31 EMU with a shadow, and a blur radius at the schema's maximum.
  - Word's PDFs through the bridge.
  - The effect pictures are extracted (`mutool extract`), and their pixel size, offset and alpha profile read as
    numbers.
  - **Gate:** numbers committed; the blur rule for each effect named, or the tolerance it needs.
- **Phase 1, pictures in a rectangle.**
  - Outer shadow and glow (chart-render's, moved), inner shadow, soft edge, reflection, on a rectangular picture, in
    FO (visitor and XSLT) and HTML; inline and anchored, the anchored wrap as phase 0 measured; the D10 cap and its
    hostile probes.
  - **Gate:** probes within tolerance; corpora unmoved.
- **Phase 2, picture frames** (needs CR-080.1).
  - `pic:spPr/a:prstGeom` and `a:custGeom` clip the picture; effects follow the outline; `a:ln` drawn.
  - **Gate:** the 28 picture styles within tolerance.
- **Phase 3, shapes and text boxes** (needs CR-080.2).
  - The same effects on a `wps:wsp`'s drawing, `a:effectRef` resolved.
  - **Gate:** probes; corpora unmoved.
- **Phase 4, preset shadows** (D6): the 20 `a:prstShdw` presets as outer shadows with Word's parameters, measured in
  phase 0.
- **Phase 5, 3-D** (D5): `a:scene3d` and `a:sp3d` on pictures, shapes, SmartArt (CR-082) and WordArt (CR-083, from
  `w14:props3d`/`w14:scene3d`).
  - Drawn: the camera (presets, `rot`); the light rig (type, direction); top and bottom bevels (the 12
    `ST_BevelPresetType` profiles, width and height); extrusion and contour (depth, colour); the material presets.
  - It is drawn as Word's PDF draws it. Phase 0 adds 3-D probes and settles whether Word writes a lit picture (as
    expected) or vector shading, and at what resolution.
  - The renderer builds a height field from the outline and the bevel profile, shades it per light rig and material,
    lays extrusion layers under the camera's projection, and writes a picture with alpha, as the other effects are.
  - Until this phase, a 3-D drawing is drawn flat and reported (D9). That flat drawing is the floor that ships
    whatever happens to this phase.
  - **Go/no-go first:** after phase 0's 3-D probes, the estimate is set from what Word's PDF holds (a picture at what
    resolution, or shading), and Jason decides whether the phase goes ahead.
  - **Gate:** the 3-D probes within a tolerance set from Word against itself; corpora unmoved.
- **Phase 6, the gap report** (D9): every effect in the probes and a load-survey sample rendered; what isn't drawn as
  Word draws it is listed with its reason (`a:blip` colour effects until D7's follow-up, for example).
- **Phase 7, CHANGELOG, Getting Started.**

## 7. Risks

- **Word's blur isn't a Gaussian of the stated radius.** CR-037 found a glow's sigma to be 0.25 of its radius, after a
  0.5 spread. Each effect may have its own rule. Phase 0 measures each, and the tolerances come from how far Word is
  from itself across two exports.
- **File size.** One PNG per effect per picture. A document with many styled pictures grows. The property (D3) and
  deflate keep it bounded; phase 0 compares with Word's sizes.
- **Soft edge on a JPEG.** FOP can't give a JPEG a soft mask: `ImageRawJPEGAdapter` has none, unlike the PNG and
  rendered-image adapters. A soft edge therefore re-encodes the picture as a bitmap with alpha, and the `SMask`
  belongs to that bitmap. Only a soft edge does this. Every other effect leaves the JPEG passed through (§5.4).
- **Memory.** An effect picture is a `BufferedImage` of the extent plus the blur's padding. Uncapped, a huge or
  hostile extent exhausts the heap. D10 caps it.

## 8. Decisions (owner: Jason; recommendations)

- **D1. Effects as pictures**, as Office draws them in a PDF, rather than SVG filters. FOP rasterises SVG filters at
  its own resolution, which can't be controlled per effect. Recommendation: yes.
- **D2. One implementation**, chart-render's, moved to a `drawing` package of the same module and used by charts,
  pictures and shapes. Recommendation: yes. Charts' output must stay byte-identical (gate: CR-037's probes).
- **D3. Resolution** as a property, default 2 pixels per point. Recommendation: yes, the default confirmed by phase 0.
- **D4. Effects on shapes and text boxes** in this CR (phase 3), not later. Recommendation: yes. They are the same
  code once CR-080 draws the outline.
- **D5. 3-D** (`a:scene3d`, `a:sp3d`: bevels, extrusion, rotation, lighting) in this CR, as phase 5, the last
  drawing phase. Until then it is drawn flat and reported. It is a renderer of its own, but 3-D WordArt and 3-D
  SmartArt styles can't be output without it, and those are gaps (Jason, 2026-11-09). Cross-checks:
  - rust-office-art's `scene3d.rs` and `model3d.rs` (MIT and Apache-2.0, about 65 KB of Rust with its preset tables);
  - pptx-renderer's "bounded static DrawingML 3D", validated against PowerPoint (Apache-2.0);
  - LibreOffice (MPL-2.0, reference only).

  rust-office-art's crate is MIT AND Apache-2.0 AND OFL-1.1; if any preset table is taken from it, its OFL font data
  is not. Recommendation: yes, behind phase 5's go/no-go after phase 0; flat and reported meanwhile.
- **D6. Preset shadows** (`a:prstShdw`, 20 presets): phase 4, whatever the count. Recommendation: yes. They are cheap
  once the outer shadow exists.
- **D7. `a:blip` colour effects** (`a:grayscl`, `a:duotone`, `a:alphaModFix`, `a:lum`, `a:clrChange`, `a:biLevel`):
  not in this CR, which is about `a:effectLst`. A follow-up, if wanted, recolours the picture's pixels before
  embedding. Recommendation: yes, separately.
- **D8. Effects in headers and footers** are drawn as in the body: the same code path. Recommendation: yes. docxide-pdf
  names this as a gap of its own.
- **D9. Gaps are reported** with stable codes (`render.effect.flat_3d`, `render.effect.blip_recolour`,
  `render.effect.over_limit` and so on), once per drawing, through CR-080 D9's two mechanisms: the message writer and
  the gate's event lines. Recommendation: yes.
- **D10. A cap on an effect picture.** At most 4,096 pixels a side and 16 million pixels in all; past it the picture
  is made at a coarser resolution, down to 0.5 pixels per point; past that the effect is not drawn and is reported
  (`render.effect.over_limit`). The numbers are set from phase 0's largest real picture style. Recommendation: yes.

## 9. Out of scope

`a:blip` colour effects (D7, reported meanwhile); `a:effectDag`; text effects on runs (`w14:shadow`, `w14:glow`,
`w14:reflection` in a `w:rPr`), whose WordArt case is CR-083's; artistic effects (`a14:imgProps` with `a14:imgEffect`:
blur, pencil sketch and so on), where the `a:blip` is expected to be the already rendered picture and `a14:imgLayer` the source (phase 0 to confirm).

## 10. Hand-offs

The portfolio: the registry entry with its phases (CR-081.1 depends on nothing; CR-081.2 on CR-080.1; CR-081.3 on
CR-080.2; CR-083.6 and CR-082's 3-D styles depend on CR-081.5). None to the
ports or the FO renderer fork.

## 11. Effort (rough)

Phase 0 three days (with the 3-D probes); phase 1 three days; phase 2 one day; phase 3 one day; phase 4 one day; phase 5
(3-D): estimated after phase 0, behind its go/no-go (the earlier guess was two to three weeks); phases 6 and 7 two
days. About two weeks without phase 5. Phases 0 to 4 are a usable first release, with 3-D drawn flat and reported.

## 12. Specifications

Editions read (2026-11-09):
- ECMA-376 5th edition, aligned with ISO/IEC 29500:2016: Part 1 (December 2016), Part 2 (December 2021), Part 3
  (December 2015) and Part 4 (December 2016). Local copies: `~/git/safe-docx/spec-compliance/ecma-376/source-artifacts/`,
  with their `SHA256SUMS`.
- [MS-ODRAWXML] 35.0 (2026-10-06) and [MS-OI29500] 25.0 (2026-08-18), read on learn.microsoft.com (`office_standards`)
  on 2026-11-09.
- [MS-DOCX] 23.0 (2026-08-18), local copy `~/Downloads/[MS-DOCX]-260818.docx`.
- Not used: [MS-ODRAW], the binary OfficeArt format of .doc and .ppt, which docx doesn't carry.

**ISO/IEC 29500-1 (ECMA-376 Part 1)**
- **Effects** (§20.1.8): §20.1.8.26 `effectLst`, §20.1.8.45 `outerShdw`, §20.1.8.40 `innerShdw`, §20.1.8.32 `glow`,
  §20.1.8.53 `softEdge`, §20.1.8.50 `reflection`, §20.1.8.49 `prstShdw` with §20.1.10.52 `ST_PresetShadowVal` (D6).
- **Theme**: §20.1.4.2.8 `effectRef`, §20.1.4.1.12 `effectStyleLst`.
- **Pictures**: §20.2.2.5 `pic`, §20.2.2.6 `spPr`, §20.2.2.1 `blipFill`, §20.1.8.13 `blip` (D7's colour effects are its
  children), §20.1.8.55 `srcRect`.
- **Space the effects take**: §20.4.2.6 `effectExtent`.
- **3-D** (D5, phase 5): §20.1.4.1.26 `scene3d`, §20.1.5.12 `sp3d`, §20.1.5.5 `camera`, §20.1.5.9 `lightRig`,
  §20.1.5.4 `bevelT`, §20.1.5.3 `bevelB`, §20.1.5.7 `extrusionClr`, §20.1.5.6 `contourClr`, §20.1.5.2 `backdrop`,
  §20.1.5.11 `rot`; §20.1.10.9 `ST_BevelPresetType`, §20.1.10.47 `ST_PresetCameraType`, §20.1.10.30
  `ST_LightRigType`, §20.1.10.29 `ST_LightRigDirection`, §20.1.10.50 `ST_PresetMaterialType`.

**[MS-OI29500]: Office's notes on these sections**
- **2.1.1303 (§20.1.8.45 `outerShdw`):** in an `effectLst` under `spPr`, the shadow is drawn only when `sx` and `sy`
  are 100%; any other scale needs the `effectLst` in a `grpSpPr`. Word's picture styles write scaled shadows
  (perspective shadows) in a picture's `spPr`, so phase 0 checks what Word draws for them. Chart-render's scaled
  shadow (CR-037) may apply only to charts.
- **2.1.1300 (§20.1.8.40 `innerShdw`):** `dir` is measured clockwise from the left.
- **2.1.1304 (§20.1.8.50 `reflection`):**
  - `algn` and `dist` are the reflection's, not a shadow's;
  - `fadeDir` is the direction of the alpha ramp, and `dir` the direction of the offset: the reverse of the
    standard's wording.

  §2's description follows Office.
- **2.1.1306 (§20.1.8.53 `softEdge`):** `rad` is an inset. Only the band between the outline and the inset is
  blurred; the inside is untouched.
- **2.1.1352 (§20.4.2.6 `effectExtent`):** `l`, `r` and `t` extend the left, right and top edges. The standard says
  the bottom for all of them.

**[MS-ODRAWXML]** §2.3 (`drawing/2010/main`): §2.3.1.9 `imgProps` (artistic effects, out of scope, §9),
§2.3.1.4 `hiddenEffects`, §2.3.1.12 `shadowObscured`.
