# CR-081 phase 1: a rectangular picture's effects, against Word

Phase 1 draws a picture's outer and inner shadow, glow, soft edge and reflection in the PDF (both routes) and in HTML
(both routes). It checks them against Word's own effect pictures from phase 0's PDFs (Word 16.0.20326):

- The oracle is `export_effects.py`, which writes `docx4j-chart-render/src/test/resources/effects/cr081-word-effects.jsonl.gz`:
  168 probes, 12 families, 179 effects. Each record holds Word's picture place, pixel size, peak alpha, colour, and the
  alpha along three rows and three columns.
- `EffectsTest` compares the renderer (`org.docx4j.chart.render.api.Effects`) with the oracle, drawing the same picture.
- `compare_effects.py` compares docx4j's PDF of the same probe files.

Phase 0's findings §2 hold where they were measured. The comparison also showed six rules phase 0 hadn't stated, and
corrected one (§2).

## 1. Result

- Renderer (`EffectsTest`): 171 effect pictures (the four turned probes left out, §4). Mean alpha error 0.68 of 255,
  median 0.4, 90th percentile 1.2, 99th 3.0. Every picture is within a pixel of Word's place, its resolution within
  1%, its peak alpha within 0.025 and its colour within 6 a channel. The only exceptions are the two understood cases
  in §3.
- PDF (`compare_effects.py` on the FO route's render, Word's fonts): the same 171 pictures, mean 0.70, median 0.42,
  90th percentile 1.23. The anchored probes (`fx90`, 11) and the inline ones in a line of words (`fx91`, 5) are within
  0.3 to 1.5.
- Corpora: gate b413 (and b415 on the final jars) against b412 is 0 movers, with events equal. Four corpus documents
  have picture effects (three distinct, all outer shadows). Each now draws its shadow, and their pages are unchanged.
  In 15_en-AU_sdt_num_tbl_4994 the shadow is within 0.5pt of Word's golden. In 16_en-ZA_num_tbl_10976 it is 3.2pt
  small on every side, because that picture has a 3pt line that Word draws outside it and that casts the shadow too
  (phase 2, §4).

## 2. Rules phase 1 adds or corrects

- **Scale and skew are one matrix**, [sx, tan kx; tan ky, sy] about the alignment point. Neither order of a scale and
  a shear fits:
  - a perspective shadow (sy 23%, kx 20°) leans tan 20° of the picture's whole height (fxr12-21 to 25);
  - a reflection (sy -100%) skewed by kx leans its far end left (fxr50-28);
  - skewed by ky, it leans its right side down (fxr50-29).

  Phase 1's probes all have sx 100% wherever there is a skew, so sx·tan kx can't be told from tan kx. The picture
  styles' Perspective Shadow (sx 97%, kx 15°) can, in phase 2.
- **`algn="ctr"`** is the box's centre. chart-render's `Fills.shadows` reads it as the right edge (`"ctr".endsWith("r")`),
  which only matters for a chart shadow scaled or skewed about its centre. That is a lead for charts (§5).
- **The effects are a chain**, as fxr70 shows:
  - the soft edge, with the inner shadow inside it, or else the inner shadow alone, on the picture;
  - the glow round the picture's own outline, not its soft edge (fxr70-03: 5.7 of 255 out the other way, 0.5 this way);
  - the outer shadow, cast by the picture with its glow (the shadow is the glow's size);
  - the reflection of all of them, mirrored about the picture's own box (fxr70-05's reflection starts 7pt above the
    picture's foot: the shadow, mirrored).
- **The reflection's fade** runs from the mirror line, halfway across the gap, over what is reflected:
  - below a 108pt picture alone, the fade starts at 108 + dist/2 and spans 108 + dist/2 (fxr50-12 to 14; fitted to
    0.47 of 255 at every distance);
  - with a glow and shadow it spans their reflection (fxr70-01 peaks at 0.275 against Word's 0.278).
- **The soft edge**: the outline shrinks by 0.88 of the radius (the fit is 0.82 to 0.92 across radii 2.5 to 50pt) and
  is blurred with sigma rad/3 (fitted 0.32 to 0.33), as findings §2 has it. A radius past half the picture leaves
  nothing (fx40-07).
- **The glow** spreads 0.485 of its radius (its 50% contour is 0.486 to 0.493 rad at 200 dpi) and blurs with
  sigma rad/6.
- **The inline clip** is the paragraph's box, not the picture's line. In `fx91`, a picture in a paragraph that goes on
  below it keeps a shadow 12.6pt below it and a reflection 48pt, with no effect extent. Phase 0's probes had the
  picture alone in its paragraph, where the two are the same.
  - Word's clip, read from fxr50-24's PDF, runs from the previous paragraph's end, part of the space before
    included (27.9 of its 36pt), to the line's descent below the picture (2.3pt at 12pt).
  - docx4j can't clip at a paragraph's box in FO. It cuts at the effect extent's top and bottom only where the picture
    is all its paragraph shows; otherwise it doesn't cut (§3).

## 3. Departures (known, measured)

- **The inline clip.** docx4j cuts a picture that is alone in its paragraph at its effect extent, and doesn't cut one
  sharing its paragraph. XSL-FO can clip an inline object's overflow only at its own viewport, not at its paragraph's
  box: Enterprise CR-001 §6.6 item 65.
  - Alone: Word's cut is 2.3pt lower below (the text's descent) and up to the space before higher above. fxr50-24's
    and -30's upward reflections show 28.4pt less than Word's.
  - Shared: Word cuts at the paragraph's foot. fx91-05's reflection (no effect extent) draws 59.8pt past Word's cut.

  Where Word's interface wrote the effect extent, it covers every visible pixel (findings §3), so the cut doesn't
  bite.
- **fxr50-11** (stPos past endPos): Word's cut-off is 0.4pt later than docx4j's. One profile row lies between the two,
  giving a mean of 8.8 of 255.
- **fx40-05** (soft edge, 25pt): inset 0.92 rad where the rule's 0.88 gives a mean of 5.8 of 255.
- **The frame**: docx4j's effect picture is a pixel smaller than Word's and offset by a third of a point (Word pads
  one more pixel). Its content is where Word's is.
- **An opaque, unblurred shadow** is a vector fill in Word's PDF (fxr11-11) and a picture in docx4j's, of the same
  place.

## 4. Not in phase 1

- **A turned picture** (fxr12-26, -27; fxr50-34, -35). docx4j doesn't turn a picture at all yet, so its effects
  follow the picture unturned. The probes are kept for when it does (`rotWithShape`).
- **A picture's line** (`a:ln`, drawn outside it) and frame geometry are phase 2. Until then the line neither draws
  nor casts its shadow (16_en-ZA_num_tbl_10976: the shadow 3.2pt small on every side).
- **A metafile picture** (WMF, EMF), drawn as SVG, has no pixels to blur: its effects aren't drawn (phase 6 reports
  them). The same goes for a bitmap Java can't read (a CMYK JPEG): `render.effect.picture_unread`.
- **Pictures in groups and canvases** are CR-080's ShapeRenderer, phase 3.

## 5. Leads

- **Charts**: `Fills.shadows` reads `algn="ctr"` as the right edge and applies the scale before the skew (§2). Excel's
  charts haven't been probed for either, and D2 keeps the chart rendering as it is until they are.
