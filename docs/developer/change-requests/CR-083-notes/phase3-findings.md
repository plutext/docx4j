# CR-083 phase 3: shadow, glow and reflection on WordArt (design measurements, then findings)

Designed 2026-11-13 (Jason: "design CR-083 phase 3"), on docx4j 838c88c78; reviewed the same day by a Claude Fable 5.1
session at Jason's request, and its findings applied (Jason: "yes, apply those changes"; section 3). Section 1 is what
phase 0's probes already show, read again for this phase with `read_effects.py` (Word's PDFs of `wa30-effects-flat`,
`wa31-effects-warped`, `wg10-styles-flat` to `wg12-styles-wave`, `wb50-body-effects` and `wa00-hand-jason`; the phase 0
round of 2026-11-12). Section 2 is the round the design needs. The design itself is in the CR (phase 3, and D12 to
D15). Later sections hold what the phase finds.

## 1. What phase 0's probes show

All on "WordArt" in Aptos 36 pt, one line, `wrap="none"`, anchored at the top. The effects' parameters are in
`build_wordart.py` (`EFFECTS`) and `styles-word.tsv`. Places are PDF points, y up; the text's baseline, unmoved, is at
552.38; Word's ascent and descent for Aptos 36 pt are 33.8 and 10.3 pt, and the ink of "WordArt" runs from 0.33 below
the baseline to 23.66 above it ("WordArt" has no descender).

**The effects are pictures under real text**: 200 dpi (2.78 to 2.83 pixels a point), Flate with a soft mask (an 18 pt
glow's colour is a JPEG, its soft mask Flate). The text is real text in every probe with a solid fill (D2).

**`w14:alpha` in an effect's colour is a transparency**, as in a fill (phase 0 findings section 1). The pictures'
alpha peaks at the colour's opacity: a shadow at `w14:alpha` 60000 reaches 0.396, at 20000 0.792; a glow at 40000
0.600. A reflection's `stA` and `endA` are opacities, as DrawingML's (`stA` 55000: 0.529, blurred).

**Word draws a text effect at about 0.6 of its stated size** (found by the review, section 3; `read_effects.py
--detail`). A sharp shadow's offset, by the cross-correlation of the text's ink with the shadow's alpha: wa30-19 (dist 6
pt at 45 degrees) 2.50 right and 2.50 down, where 4.24 is stated (CR-081 measured 4.32 on a picture); wg10-12 (dist 3)
and wa30-17 (dist 3, blur 4) 1.25, for 2.12. An 18 pt glow's alpha falls to 50% of its peak 5.25 pt outside the ink
and to 5% at 7.75, where CR-081's rule for a shape (the silhouette grown by 0.485 of the radius, blurred with sigma a
sixth of it) gives about 8.7 and 13.7. The small glows read less cleanly at this resolution (5 pt: 2.25 and 2.75; 3 pt:
1.75 and 2.75). So CR-081's rules don't carry over to text unchanged: one scale on `dist`, `blurRad` and `rad` (about
0.6; 0.625 would be 200 / 320) is the round's to measure, and whether it depends on the size or the face.

**The frame is not the glyphs' ink.** The box in which a shadow's `algn` point, a reflection's mirror line and its fade
are measured (the drawing's box, for CR-081) is, for text, something else, and not one obvious box (the review's
measurements, section 3; `read_effects.py`):
- wa30-20 (sy 23%, kx -20 degrees, `algn bl`): the squashed shadow's 50% band lies 2 to 8 pt below the baseline, so it
  is scaled about a point near the descent line (10.3 below), not the ink's foot; its shear moves it as from there;
- flat reflections start at the ink's foot (wa30-23 and wg10-07: their alpha begins at 552.48), and fade over about
  32 pt times `endPos` (wa30-23, endPos 45.5%: 14.7 pt; wg10-07, 35.5% at sy -90%: 10.3), near the ascent (33.8), not
  the ink's height (23.7) or the line's (44.1);
- wa30-24 (dist 4 pt down): the reflection starts 10.0 pt below the baseline (542.43), not 4 or 0.6 of it;
- on textWave1 (wa31-12) the reflection's alpha begins at 451.92, the warped glyph paths' lowest point (451.59): it is
  mirrored about the warped block's foot, and fades over 0.455 of the warped block's height;
- on Follow Path the frame is the whole block's (below).

**One picture for each effect**, and how many depends on the warp:
- flat text: one picture for each effect (wa30-25, a glow and a shadow: two);
- a two-path warp (textWave1): one picture for the whole warped text (wa31-10 to -12);
- Follow Path (textArchUp): **one picture for each glyph**, drawn in turn with its glyph, picture then glyph (wa31-04 to
  -06: seven, each `fill_image` then the glyph's paths; so a glyph's shadow falls on the glyph before it). A shadow's
  picture is the glyph's own shadow, offset. A reflection's is a cut of **one reflection of the whole block**, flipped
  about the block's foot (wa31-06: the glyph paths' foot 546.12; the W's reflection begins at 545.05, the A's at
  535.21, as flipped about the block's foot and not about their own), with the block's fade: the r and d at the top of
  the arch reflect 35 pt down, past the fade, and Word still writes their pictures, with alpha 0 everywhere. So Word's
  result is one mirrored arch, written glyph by glyph.

**The inner shadow** (gallery style 15: `w14:shadow` with `sx="0" sy="0" algn="none"`, blur 5 pt, dist 4 pt at 225
degrees, black at 50%) is one picture drawn **over** the real text (`mutool trace`: the text, then the picture), alpha
peaking at 0.494, and takes no room.

**Effects take room in the line** (phase 1-2 findings section 3). Word sets the text lower in its box, and an
`spAutoFit` box grows, by an amount that depends on the effect:

| probe | effect | box taller by (`spAutoFit`) | text lower by |
|---|---|---|---|
| wg10-01 (style 1) | shadow, blur 3, dist 1.5 at 45 degrees | 3.70 | 1.20 |
| wg10-02 (style 2) | shadow, blur 3, dist 2 at 90 degrees, `algn ctr` | 3.70 | 0.72 |
| wg10-04 (style 4) | shadow, blur 3, dist 1.8 at 90 degrees | 3.70 | 0.72 |
| wg10-11, -13 | shadow, blur 1, dist 3 at 45 degrees | 1.90 | 0 |
| wg10-12, -14 | shadow, no blur, dist 3 at 45 degrees | 1.30 | 0 |
| wa30-17, -18 | shadow, blur 4, dist 3 at 45 degrees | (fixed box) | 1.20 |
| wa30-19 | shadow, no blur, dist 6 at 45 degrees | (fixed box) | 0 |
| wa30-20 | shadow, blur 6, dist 0, sy 23%, kx -20 degrees, `algn bl` | (fixed box) | 0 |
| wg10-09 (style 9) | glow 3 pt | 2.75 | 1.44 |
| wa30-21 | glow 5 pt | (fixed box) | 2.16 |
| wa30-22 | glow 18 pt | (fixed box) | 8.16 |
| wa30-25 | glow 4 pt and the shadow of wa30-17 | (fixed box) | 2.88 |
| wg10-07 (style 7) | reflection, blur 0.5, sy -90% | 0.90 | 0 |
| wa30-23, -24 | reflection | (fixed box) | 0 |
| wg10-15 (style 15) | the inner shadow | 0 | 0 |

Points in the box's frame. The box's height is the same for the gallery's styles flat, on textArchUp and on textWave1
(wg10, wg11, wg12), so the room is the line's, before any warp. Its width (`wrap="none"`) doesn't change: no room is
taken across.

A fit to these 15 points, **a hypothesis for the round, not a rule**:
- the room is how far the effect, **as Word draws it** (at about 0.6, above), reaches past the line: a shadow's above,
  max(0, blur - dy), below, blur + dy (dy its offset down), at 0.62 (styles 1, 2, 4, 11 and 12 within 0.03 pt of the
  box's growth); a glow's 0.45 of its radius above and below (style 9: 2.70 for 2.75), near its drawn 5% contour. A
  squashed shadow that never reaches above the ink takes none (wa30-20), which a rule on the attributes alone gets
  wrong (it would give 3.7);
- effects add: wa30-25's glow and shadow, 1.80 + 1.16 = 2.96 above;
- the text moves down by the room above, in steps of 0.24 pt (1/300 inch: every move in the table is a whole number of
  them, 1.20 = 5, 8.16 = 34), rounded to the nearest; the step may be the position's rather than the move's (the same
  shadow moves body text 0.96, below);
- a reflection's room (0.90 for style 7) isn't explained; an inner shadow takes none.

So the room should come from the drawn pictures' reach rather than from a rule of its own: the round reads both, and
whether the scale depends on the size or the face, and how the room is placed under another anchor or with more lines
(section 2).

**Body text takes room too** (wb50: a 36 pt line whose one run has the effect, then a paragraph whose middle run has
it; against wb50-01, which has none):

| probe | effect | the effect's line lower by | the next paragraph lower by |
|---|---|---|---|
| wb50-17 | wa30-17's shadow | 0.96 (WordArt: 1.20) | 5.28 |
| wb50-19 | wa30-19's shadow, no blur | 0 | 2.64 |
| wb50-21 | glow 5 pt | 2.16 (WordArt: 2.16) | 5.76 |
| wb50-22 | glow 18 pt | 8.16 (WordArt: 8.16) | 20.88 |
| wb50-23 | reflection, as wa30-23 | 0 | 5.52 |
| wb50-24 | reflection, as wa30-24 | -0.24 | 21.36 |

Above the line, the room is WordArt's within a step; below it, a reflection takes a great deal (21.4 pt for wa30-24's,
whose `dist` is 4 pt and `endPos` 60%), where in WordArt style 7's took 0.90. So phase 5's lines move, not only its
painting (the CR's phase 5 and D13), and the round measures body text beside WordArt (fb10).

**The anchor's effect extent is the file's.** The probes were written with `wp:effectExtent` 0 and Word's save kept
it, which says only that Word doesn't recompute it (CR-081 §3). Jason's hand-made WordArt (wa00) has extents Word wrote
itself (`b="1270"`, `b="2540"`, and `t="971550" b="974090"` on one). docx4j takes the wrap's extent from the file, as
for every drawing; the effect pictures' bounds don't change it.

## 2. The round (`cr083-phase3`, to be built)

Built by `build_effects.py` (to write) from `build_wordart.py`'s helpers, one probe a page, checked by
`check_wordart.py`, staged under the bridge's `staging/cr083-phase3` for the portfolio to submit (verb `save`, docx and
pdf). "WordArt" in Aptos 36 pt, `wrap="none"`, `a:spAutoFit`, anchored at the top, unless said. The text is black and
each effect's opacity at most 0.6, so that `read_effects.py --detail` can tell the ink from the effect.

| file | what it varies | probes |
|---|---|---|
| fx10-shadow-room | shadow blur 0, 1, 2, 4, 8 pt by dist 0, 2, 6 pt at 90 degrees; blur 4, dist 3 at 0, 45, 135, 180, 225, 270, 315 degrees; sx, sy 50% and 150% about tl, ctr, b | 30 |
| fx11-glow-reflection-room | glow 1, 2, 3, 5, 8, 12, 18, 25 pt; reflection blur 0, 0.5, 2, 6 pt by dist 0 and 4 pt, sy -100% and -50%, endPos 35% and 90%; reflection dist 0, 1, 2, 4, 8, 16 pt at dir 90 and 270 degrees; the same with "Typography" (descenders) | 40 |
| fx12-room-size-face | three shadows and two glows at 12, 24 and 72 pt, and in Calibri and Times New Roman at 36 pt; a sharp shadow 12 pt long at 0 and 90 degrees at each size and face (the scale, read exactly) | 35 |
| fx13-room-lines | anchor ctr and b, `noAutofit`; two lines with the effect on the first, the second, both; two runs on one line, the effect on one; a 72 pt run beside a 24 pt run with the glow; the shadow and the reflection on "Typography" | 14 |
| fx14-units | two runs with different effects, flat and on textPlain, textWave1, textInflate; two lines flat; textArchUp and textCircle with two lines; textButton; a glow and a reflection on textCircle | 16 |
| fx15-silhouettes | a shadow and a glow on hollow text (no fill, a 1 pt outline), on a 6 pt outline, on a gradient fill, on a fill with transparency, on no fill and no outline | 10 |
| fx16-inner | `sx="0" sy="0" algn="none"` with blur, dist and dir swept; `sx="0"` alone, `sy="0"` alone; `algn="none"` at 100%; `sx="0" sy="0"` with `algn="tl"` | 12 |
| fx17-colours | an effect colour as a theme colour with `w14:lumMod`, `w14:lumOff`, `w14:shade`, `w14:tint`; alpha 0 and 100000 | 6 |
| fx18-shape-effects | a WordArt whose `wps:spPr` has `a:effectLst` (outer shadow, glow) and no fill, its runs none: does its text cast them (CR-081 phase 3's `render.effect.shape_text`)? | 4 |
| fx19-rotated | the box turned 30 degrees (`a:xfrm rot`) with a shadow at 90 degrees, a reflection, a glow, and the shadow on textArchUp: do the shadow's direction and the reflection follow the box or the page? | 4 |
| fb10-body-room | fx10's and fx11's sweeps on a body run, alone on its line and in the middle of a paragraph (for phase 5) | 16 |

About 190 probes in 11 files, within what the Follow Path round ran (435). fx10 and fx14 are staged twice (`-a`, `-b`),
so that the tolerances come from Word against itself, as CR-081's were.

**What each probe is read for** (`read_effects.py`, extended by the review): the box and its height; the text's origin
where it is real text, and Word's glyph paths' union where it draws paths (a warp, an outline, a gradient); each
picture in order, empty ones included (flagged, the warped WordArt's transparent artefact told apart), with its alpha's
peak and its bounds at 50% and 5%; and with `--detail`, a shadow's drawn offset and a glow's 50% and 5% contours
outside the ink. Each probe passes its own text (`--text`). The gate reads the geometry first (offset, contours, mirror
line, fade, the room), then the alpha, which can't agree until the scale and the frame do.

## 3. The review (2026-11-13)

A Claude Fable 5.1 session reviewed the design read-only at Jason's request ("have fable review the design"), against
Word's phase 0 PDFs and the code; Jason: "yes, apply those changes". Its findings, and what was done:

1. **The frame is not the ink** (wrong): a shadow's `algn`, a reflection's mirror and fade are measured in another
   box (section 1). The design no longer names the ink; the frame is the round's (fx11's dist sweep, "Typography").
2. **Word draws a text effect at about 0.6 of its stated size** (wrong: the design took CR-081's rules unchanged).
   Section 1; checked by this session before applying (wa30-19's offset, 2.50 for 4.24). The design names one scale
   as the round's to measure (fx12's long sharp shadows), applied where the effects are converted, and derives the room
   from the drawn effects' reach; this likely explains the fit's 0.62 and 0.45.
3. **wa31-12's reflection was misread** (wrong): it starts at the warped glyphs' foot, not 3.1 pt below the text
   rectangle (PDF y runs up). Corrected in section 1.
4. **Follow Path's reflection is one mirrored block, written glyph by glyph** (D14's rationale contradicted, the
   decision not): section 1. D14 stands for the drawing order; its geometry is the block's (the CR's D14 says so).
5. **`wp:effectExtent` "stays 0"** was overclaimed: section 1 now says docx4j takes it from the file.
6. **`read_effects.py` dropped empty pictures** (5 counted on wa31-06 where Word drew 7) and found the text only as
   real "WordArt": extended (section 2), and checked against the review's numbers (wa30-19 2.50, wg10-12 1.25, the
   18 pt glow's 5% contour 7.75).
7. **The round lacked** a reflection distance sweep, a word with descenders, long sharp shadows for the scale, and a
   turned box: added (section 2: fx11, fx12, fx13, fx19).
8. **The room's order** (risk): with the room from the drawn pictures, the renderer lays the lines out, draws the
   effects in the line's frame, measures their reach, then sizes the box; under `spAutoFit` that changes Follow Path's
   D. The CR's phase 3 says so.
9. **The alpha gate is unmeasurable until the scale and the frame are found** (nit): the gate reads the geometry first.
10. **A hollow silhouette's shadow** (style 14) can't be read from dark pixels: fx15 is read with Word's path boxes.

The review's scripts were scratch (this session's `review/`); what they measured is in `read_effects.py` now.
