# CR-081 phase 6: the gap report

Phase 6 (D9) renders every effect in CR-081's probes, and a sample of real documents, and lists what docx4j doesn't
draw as Word draws it, each with its reason (Jason, 2026-11-11: "do CR-081 phases 6 and 7"). It found two gaps that
went unreported. Both now report:
- a picture's colour adjustments (`render.effect.blip_recolour`, D9's own example; CR-084 draws them);
- a metafile picture's frame and effects (`render.effect.metafile`, which phase 1 left for phase 6).

## 1. Method

- **The probes.** `build_phase0.py` rebuilt all 17 sets: 77 files, 1,356 probes. Each file went through docx4j's PDF
  route in a JVM of its own: the layout-fidelity harness's `render`, the FO, then the PDF.
  - The code was 17.3.7-SNAPSHOT as committed through CR-081 phase 5b-2 (59d01d23e), with phase 6's change, on the
    fork's 2.12-docx4j.2 at 90a06d792.
  - Every log line was read.
- **The corpora.** The four fidelity corpora (557 documents, Word's re-saves) were scanned for effects in the body,
  headers and footers, but not the theme. 29 documents have them, 20 distinct (real-c2 repeats real2's). Each of the
  20 was rendered the same way.
- **The load-survey sample isn't in this report.**
  - The portfolio's 240-document Common Crawl sample is gone from this machine, and nothing larger is local.
  - Its counts are queued in the portfolio's `scripts/corpus/OWED-QUERIES.md` (entries "CR-081 (effects)" and
    "CR-084"). They run when the catalogue is copied home, or sooner if Jason picks an S3 sample.
- **No drawing failed.** Every render ended with its PDF, and none logged a drawing failure: no "couldn't be drawn",
  and no exception from the effect code.
  - A code is logged once per drawing per conversion. The harness converts twice, once for its FO dump and once for
    the PDF, so each drawing is listed twice in its log.
  - `PictureEffects` logs each code once per JVM, hence a JVM a file.

## 2. The gap codes

Each is logged as a WARN, through `FOShapes` and `HtmlShapes` (shapes) or `PictureEffects` (pictures).

| code | when | what docx4j draws | probes | corpora |
| --- | --- | --- | --- | --- |
| `render.effect.blip_recolour` (new) | a picture's colour adjustments: `a:blip`'s children, `a:clrChange`, `a:lum`, `a:grayscl`, `a:duotone`, `a:alphaModFix` and so on | the picture as stored | none (CR-084's) | 13 |
| `render.effect.metafile` (new) | a WMF or EMF picture with effects, a line, a geometry or 3-D | the metafile alone, as SVG | none | none |
| `render.effect.3d_shading` | legacyWireframe, or material none | Word's geometry, shaded warmMatte | fxa2-04; fxh4-13 to -16 | none |
| `render.effect.3d_text` | a 3-D shape's text that Word turns with it (a camera other than orthographicFront, no `a:flatTx`) | the shape 3-D, its text flat | fxe1-03, -05, -07, -08, -09, -10 | none |
| `render.effect.shape_text` | a shape's text's effects where they'd show (a shadow or glow with no opaque fill; a reflection) | the shape's effects only | fx80-10, fxr80-10 | none |
| `render.effect.flat_3d` | a 3-D drawing whose picture couldn't be made (past the cap; nothing painted) | the drawing flat | none | none |
| `render.effect.over_limit` | an effect picture past D10's cap even at 0.5 pixels a point | the drawing without it | none | none |
| `render.effect.effect_dag` | an `a:effectDag` (§9) | no effects | none | none |
| `render.effect.group` | a group's own `a:effectLst` | its shapes' effects only | none | none |
| `render.effect.picture_unread` | a picture Java can't read (a CMYK JPEG) | the picture without its effects | none | none |

The `render.shape.*` codes are CR-080's: none was logged.

**blip_recolour** names the first adjustment in its line (`render.effect.blip_recolour: a:grayscl`).
- It goes out from `WordXmlPictureE20` before the metafile and plain-picture returns, in the PDF and the HTML routes
  alike. In the HTML route it was checked on 7042.
- One of the 13 documents, 10976, has its adjustment on an EMF in its headers.
- `PictureRecolourTest` checks the names.

**metafile:**
- **The case.** A WMF or EMF is drawn as SVG and has no pixels to blur or light. So its frame, line, effects and 3-D
  aren't drawn (phase 1 findings §4).
- **Where.** Reported where `PictureEffects.framed` holds for it, in both routes.
- **The corpora** hold 145 metafile pictures, none framed.

## 3. Drawn, but not as Word draws it (measured, not reported)

From the phases' findings, with what each differs by:
- **The inline clip** (phase 1 §3): XSL-FO can clip an inline object only at its own viewport (Enterprise CR-001
  §6.6 item 65).
  - Word cuts an inline drawing's effects at its paragraph's box. docx4j cuts a picture alone in its paragraph at its
    effect extent, and doesn't cut one that shares its paragraph, or an inline shape.
  - fxr50-24's and -30's upward reflections show 28.4 pt less than Word's; fx91-05's reflection is 59.8 pt past
    Word's cut.
  - Where Word's interface wrote the effect extent, it covers every visible pixel, so the cut doesn't bite.
- **Small departures in the effect pictures** (phases 1 and 2): none is visible at a page's scale.
  - A reflection with stPos past endPos (fxr50-11): 0.4 pt, mean 8.8/255.
  - A 25 pt soft edge (fx40-05): an inset of 0.92 rad against the rule's 0.88, 5.8/255.
  - The effect picture's frame is a pixel smaller than Word's and a third of a point off; its content is in place.
  - An opaque unblurred shadow is a vector fill in Word's PDF, a picture in docx4j's, of the same place.
  - A reflection without endA: Word's picture runs 16 pixels past the fade, transparent.
  - A line art's shadow (fs21-04) peaks at 0.169 against Word's 0.149.
- **3-D** (phases 5a, 5b-1 and 5b-2):
  - **Silhouettes:** 270 within 0.60 pt.
  - **Shading:** 357 probes, mean 2.07/255. The seven past 6/255 (worst 8.47) are turned views of metal, powder and
    the translucent materials, at the creases where a bevel meets a side, and flat's rig from lon 300.
  - **A coloured extrusion's highlight** is 12/255 brighter in Word's (fxg1-18).
  - **Translucent materials:** their alpha is within 1.5/255; over a white page they are 4.25 (clear) and 5.00
    (translucentPowder).
  - **Effects:** a 3-D drawing's effects are under it, where Word's one picture holds them; their place is Word's.

## 4. Not drawn, and not reported

- **A turned picture or shape** (`rotWithShape`; phases 1 §4 and 3 §4): its effects are cast unturned. No probe or
  corpus document reaches it in a way docx4j could report apart from the turn itself.
- **Artistic effects** (`a14:imgProps` with `a14:imgEffect`; §9): believed drawn, not measured; CR-084's phase 0 is
  to confirm.
  - Word is expected to write the processed picture as the `a:blip` and the original as `a14:imgLayer`. If so,
    docx4j's picture is already Word's.
  - In 3942 the blip is a PNG and its layer an HD Photo, which fits.
  - Word's PDF of it holds cropped versions, so the pixels couldn't be compared.
  - Five corpus documents have them (3197, 3942, 9677, 11259, 11659).
- **Text effects on runs** (`w14:shadow`, `w14:glow`, `w14:reflection` in a `w:rPr`): out of scope (§9); WordArt's
  are CR-083's.
- **A dashed picture line, a sharp corner of a picture's outline, a soft edge or reflection with a line** (phase 2
  §4): drawn by the rules measured elsewhere, not measured on a picture.

## 5. The corpora

The 20 documents with effects, and what docx4j does with each:

| document | effects | docx4j |
| --- | --- | --- |
| real3 4994 | a 3-D picture style (bevel, twoPt) with its shadow | drawn: silhouette within 0.37 pt, shading 8.86/255 (phase 5b-1) |
| real3 10976 | an outer shadow; `a:lum` on an EMF in its headers | the shadow drawn, within 0.4 pt (phase 2); the adjustment reported |
| real3 13102 | outer shadows | drawn (phase 1) |
| real2 14372 | two shapes' theme shadows (`effectRef idx="1"`) | drawn, within 0.5 pt (phase 3) |
| 1426, 4102, 5135, 6088, 12338, 13145 | `a:clrChange` | reported (blip_recolour) |
| 9458, 11482, 11659, 13459 | `a:lum` | reported |
| 7042 | `a:grayscl` | reported |
| 5320 | `a:duotone`, `a:tint` | reported |
| 3197, 3942, 9677, 11259, 11659 | artistic effects | believed drawn (§4) |

So in the corpora the commonest gap is colour adjustments: 13 of the 20, and all reported now. Every effect CR-081
draws is drawn there, and no other code is logged.

## 6. Open, and whose

- **CR-084:** drawing the colour adjustments, the commonest gap in the corpora; and its phase 0's check of the
  artistic effects.
- **The portfolio's catalogue queries:** the load-survey half of this report.
- **The fork:**
  - an inline drawing's effects cut at its paragraph (Enterprise CR-001 §6.6 item 65);
  - a shape's text drawn into its drawing, which both `shape_text` and `3d_text` need.
- **Not yet planned:** a turned picture's effects (`rotWithShape`); a metafile's frame and effects, which would need
  the metafile rasterised.
