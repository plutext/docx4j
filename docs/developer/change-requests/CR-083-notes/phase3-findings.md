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
painting (the CR's phase 5 and D13), and the round measures body text beside WordArt (wb70).

**The anchor's effect extent is the file's.** The probes were written with `wp:effectExtent` 0 and Word's save kept
it, which says only that Word doesn't recompute it (CR-081 §3). Jason's hand-made WordArt (wa00) has extents Word wrote
itself (`b="1270"`, `b="2540"`, and `t="971550" b="974090"` on one). docx4j takes the wrap's extent from the file, as
for every drawing; the effect pictures' bounds don't change it.

## 2. The round (`cr083-phase3`)

Built 2026-11-13 by `build_effects.py` from `build_wordart.py`'s helpers (Jason: "commence phase 3"), one probe a page,
checked by `check_wordart.py` (all 13 files valid; its break test failed all six broken copies), staged under the
bridge's `staging/cr083-phase3` for the portfolio to submit (verb `save`, docx and pdf); `probes-effects.tsv` lists each
probe with the line `read_effects.py --text` reads. "WordArt" in Aptos 36 pt in Word's form (`wrap="none"`,
`a:spAutoFit`, stored 144 by 144 pt, Word's insets), anchored at the top, unless said. The text is black and each
effect's opacity at most 0.6 (a shadow black at `w14:alpha` 60000, so 0.40; a glow ED7D31 at 0.40; a reflection's
`stA` 55000), so that `read_effects.py --detail` can tell the ink from the effect. The files are `we` and `wb70`, not
the `fx` and `fb` this section first named: CR-081's rounds hold those names on the bridge.

| file | what it varies | probes |
|---|---|---|
| we10-shadow-room | shadow blur 0, 1, 2, 4, 8 pt by dist 0, 2, 6 pt at 90 degrees; blur 4, dist 3 at 0, 45, 135, 180, 225, 270, 315 degrees; sharp shadows with no dist scaled 50% and 150% about tl, ctr, b, sy 50% alone about b, sx 50% alone about l (the frame: the scaled ink's bounds give the point) | 30 |
| we11-glow-reflection-room | glow 1, 2, 3, 5, 8, 12, 18, 25 pt; reflection blur 0, 0.5, 2, 6 pt by dist 0 and 4 pt; sy -50% at dist 0 and 4; endPos 35% and 90%; sharp reflections at dist 1, 2, 8, 16 pt at 90 degrees (with the above, 0 to 16) and 1, 2, 4, 8, 16 at 270; on "Typography" (descenders): glows 3, 8, 18, sharp reflections dist 0 to 16, the default, sy -50% | 40 |
| we12-room-size-face | three shadows (wa30-17's, wa30-19's, blur 8 dist 2 at 90) and glows 5 and 18 pt at 12, 24, 72 pt and in Calibri and Times New Roman at 36 pt; a sharp shadow 12 pt long at 0 and 90 degrees at each, and at Aptos 36 pt (the scale, read exactly) | 37 |
| we13-room-lines | glow 8 pt anchored ctr and b; a fixed box (288 by 144 pt, `noAutofit`, `wrap="square"`) at t, ctr, b; two lines with the glow on the first, the second, both; two runs "Word" "Art", the glow on each; 72 pt beside 24 pt, the glow on each; wa30-17's shadow and a sharp reflection 4 pt down on "Typography" | 14 |
| we14-units | two runs with different effects (orange and blue glows; a glow and a shadow), flat and on textPlain, textWave1, textInflate, and the glows on textArchUp; two lines flat, the same glow and two colours; textArchUp and textCircle with two lines; textButton with three; a glow and a reflection on textCircle | 16 |
| we15-silhouettes | wa30-17's shadow and a glow 5 pt on hollow text (no fill, a 1 pt outline), on a 6 pt outline, on a gradient fill, on a black fill at `w14:alpha` 50000, on no fill and no outline | 10 |
| we16-inner | style 15's inner shadow (`sx="0" sy="0" algn="none"`, black at 50%) with blur 0, 2, 5, 10, dist 0, 2, 8 and dir 45 swept; `sx="0"` alone, `sy="0"` alone; `algn="none"` at 100%; `sx="0" sy="0"` with `algn="tl"` | 12 |
| we17-colours | a glow 8 pt in accent2 with `w14:lumMod`, `lumMod` and `lumOff`, `w14:shade`, `w14:tint`; ED7D31 at `w14:alpha` 0 and 100000 | 6 |
| we18-shape-effects | a WordArt whose `wps:spPr` has `a:effectLst` (an outer shadow; a glow) and no fill, its runs none, with no line and with the grey line: does its text cast them (CR-081 phase 3's `render.effect.shape_text`)? | 4 |
| we19-rotated | the box turned 30 degrees (`a:xfrm rot`) with a sharp shadow 6 pt at 90 degrees, a sharp reflection 4 pt down, a glow, and the shadow on textArchUp: do the shadow's direction and the reflection follow the box or the page? | 4 |
| wb70-body-room | body text: "WordArt" at 36 pt alone on its line, a 14 pt paragraph, then a 36 pt line "Plain WordArt plain" with the effect on its middle run, and a 14 pt paragraph; no effect (the reference), six shadows, four glows, five reflections from we10's and we11's sweeps (for phase 5) | 16 |

189 probes in 11 files, within what the Follow Path round ran (435); 13 jobs and 235 pages, we10 and we14 staged twice
(`-a`, `-b`),
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
   box (section 1). The design no longer names the ink; the frame is the round's (we11's dist sweep, "Typography").
2. **Word draws a text effect at about 0.6 of its stated size** (wrong: the design took CR-081's rules unchanged).
   Section 1; checked by this session before applying (wa30-19's offset, 2.50 for 4.24). The design names one scale
   as the round's to measure (we12's long sharp shadows), applied where the effects are converted, and derives the room
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
   turned box: added (section 2: we11, we12, we13, we19).
8. **The room's order** (risk): with the room from the drawn pictures, the renderer lays the lines out, draws the
   effects in the line's frame, measures their reach, then sizes the box; under `spAutoFit` that changes Follow Path's
   D. The CR's phase 3 says so.
9. **The alpha gate is unmeasurable until the scale and the frame are found** (nit): the gate reads the geometry first.
10. **A hollow silhouette's shadow** (style 14) can't be read from dark pixels: we15 is read with Word's path boxes.

The review's scripts were scratch (this session's `review/`); what they measured is in `read_effects.py` now.

## 4. What the rounds found, and phase 3 built (2026-11-13)

Jason: "commence phase 3". Round `cr083-phase3` (section 2) and a follow-up for what it left open, `cr083-phase3b`
(`build_effects.py`'s second set: we20, a reflection's distance and blur at Aptos 12, 24, 72 and 96 pt and Calibri and
Times New Roman at 36 pt; we21, the scale past 72 pt; we22, warped WordArt at 12 and 72 pt; 34 probes), both through
word-bridge 22479f7, every file silent, Word 16.0.20326.20318. Read by `read_phase3.py` (each probe's parameters from its
own docx; `read_effects.py`'s measures; each picture's clip path, colour and place in the drawing order; body text's
baselines) into JSON lines, and checked against docx4j's PDF by `gate_effects.py`. Word is deterministic: we10's and
we14's two runs agree in every number read. Word's data stay in the session's scratch (Word's PDFs of purpose-built
probes, rebuildable from `build_effects.py`).

**The scale.** Word draws a flat run's shadow, glow and inner shadow at **s = (size / 72)^0.7** of their stated lengths,
whatever the face: a sharp shadow 12 pt long takes 3.40, 5.55, 7.40, 12.00 and 14.66 pt of room at 12, 24, 36, 72 and 96
pt (the law: 3.42, 5.56, 7.39, 12.00, 14.67), Calibri and Times New Roman at 36 pt as Aptos; drawn, 3.5, 5.5, 7.55, 12.0
and 14.75 pt long. A shadow's blur is a Gaussian of sigma a third of its drawn blur, CR-081's rule (fitted to Word's
pictures with Word's own glyphs moved by the drawn offset: 1/3 to 1/3.5, offsets within half a point). **Warped WordArt**
(any warp but textNoShape) draws its effects at their **stated** lengths, at 12, 36 and 72 pt alike: a 3 pt shadow at 45
degrees is drawn 2.0 pt across and down on textPlain, textWave1, textInflate and textArchUp, its blur's 5% contour 3.25
pt out where flat text's is 1.75; a reflection 4 pt down on textPlain starts 4 pt below the warped glyphs' foot. Flat
text Word draws as outlines (hollow, outlined, a gradient) is scaled as flat text is: the warp decides, not the drawing.

**The frame** is the run's line box, not its ink: its advance across, its ascent (the font's line gap above it) and its
descent up and down. A sharp shadow scaled 50% about tl scales about (the line's start, the ascent: 33.6 to 34.0 pt above
the baseline), about ctr about the middle of the advance and (ascent - descent) / 2, about b about the descent line (10.0
to 10.2 below); scaled 150% it grows the box by exactly half the frame's 43.96 pt (21.95), put above and below as its algn
says; "Typography" takes the room "WordArt" does.

**The room** (the box's growth): Word gives the line how far each run's effects, worked out on the run's frame before
any warp, reach past it (`TextEffects.reach`):
- a shadow: the frame scaled and skewed about its algn point (`algn="none"`: the middle, we16-10), moved s x dist, grown
  s x blur; one scaled to nothing (sx or sy 0, not the inner shadow) draws nothing and takes its room still;
- a glow: 0.75 x s x its radius, above and below;
- a reflection: below, its move, endPos of the ascent at its |sy|, and its blur at size / 72 (2 pt of blur takes 0.35,
  0.65, 1.0, 2.0 and 2.65 pt at 12, 24, 36, 72 and 96 pt); above, the descent at its |sy| less its move (a reflection
  moved 16 pt up takes 19.55 pt above: the mirrored descent past the ascent);
- an inner shadow: none.
Each run's own reach, the line taking the most (a 24 pt run's glow beside a 72 pt run takes none; the 72 pt run's glow 8
pt takes 6.0 each way, exactly); several lines each their own; anchored with the room as part of the block. The rule fits
the box of 176 of the 180 single-run probes of both rounds within 0.05 pt (Word sizes the box in twentieths of a point),
the shadows and glows all 67 within 0.026; the four left are reflections 16 pt away (below). **Body text takes the same
room** (wb70: the line alone and a run mid-paragraph alike, a reflection 4 pt down pushing the next paragraph 16.08 pt
where WordArt's box grows 16.05), which phase 5 takes up. The text moves down by the room above, on Word's 1/300 inch
grid, whose origin wasn't found (the same room puts Times New Roman a step from Aptos and Calibri); docx4j places it
unrounded, within 0.21 pt.

**A reflection** is mirrored about the baseline (the bottom of the run's ascent box, not its ink: "Typography"'s
descenders are mirrored about the baseline as "WordArt"'s letters are, and what of them would fall above it is cut), moved
dist x 0.266 x the run's descent (Word's: 0.075 of the size in Aptos at 12 to 96 pt, 0.90 to 7.20 times dist; Calibri's
and Times New Roman's within 1% of the same share of their descents), faded linearly from half its move before its near
edge over endPos x (the ascent + half the move), whichever way it is moved (CR-081's rule, which differs from it for a
reflection moved up: Word's moved 16 pt up peaks at 0.082 as one moved down does), its blur at size / 72. Warped: mirrored
about the warped glyphs' foot, faded across their height, moved its stated dist. **A glow** grows the glyphs by **0.44**
of its drawn radius and blurs them by a sixth of it (fitted to Word's pictures with Word's glyphs: 0.42 to 0.44 at 36 to
96 pt, the error half that of CR-081's 0.485 for a shape); it follows the glyphs' coverage, not their fill's transparency,
where the shadow follows the transparency (a black fill at 50%: the glow at its colour's 0.40, the shadow at 0.20).

**The inner shadow** is `w14:shadow` with sx and sy 0 and `algn="none"`, and only that (we16; D15, `TextEffects.isInner`):
drawn after the text, taking no room. **Units**: flat text takes a picture a run on each line; a two-path warp a picture
a run (and an empty one the size of the box, phase 0's); Follow Path a picture a glyph (D14), its reflection the whole
block's mirrored about the block's foot, cut glyph by glyph (we14-16: the first three glyphs' empty, as Word's). **Clip**:
Word draws flat effects within the box across and its text rectangle up and down (its PDF's clip paths: a shadow 12 pt to
the right of 72 pt text, past the box's 7.2 pt inset, and a shadow scaled 150% are cut at the box's side); warped, within
the page. **Colours**: a theme colour's `w14:lumMod`, `lumOff`, `shade` and `tint` as DrawingML's (shade and tint in
linear light: accent2 at shade 50% is 168/78/33); `w14:alpha` 100000 draws nothing and takes its room. **A turned box**
(we19): the effects turn with it, the shadow's direction too (90 degrees drawn at 120). **The shape's own `a:effectLst`**
(we18) on a WordArt with no fill: its outer shadow is cast by the text, at its stated lengths; its glow goes round its line
alone; neither takes room.

**A face's line gap** (found by the reflection's fade): Word sets a line's ascent with the font's line gap above it, as
`WordLineMetrics.wordBaselinePt` does for body text (Times New Roman 36 pt WordArt sits 0.24 pt above Aptos, one grid
step, where phase 1's layout, putting the gap below the descent, set it 1.7 pt higher). Fixed: `WordArtText.Run.getLeading`.

### Built

- docx4j-core: `TextEffects.effects` (a run's w14 glow, shadow and reflection as an `a:effectLst`, `w14:alpha` as a
  transparency, style 15's form as `a:innerShdw`, `algn="none"` as ctr), `scale`, `reach` (the room), `reflectionMove`,
  `reflectionScale`, `scaled` (the lengths as Word draws them); `WordArtText.Run.getEffects`, `getLeading`;
  `render.wordart.effect` no longer reported at reading (only where drawing fails).
- docx4j-chart-render: `Effects.drawText` (CR-081's chain with a frame apart from the ink, the reflection mirrored and
  faded on the ascent box and cut before its mirror line, the glow by coverage at `TEXT_GLOW_SPREAD` 0.44);
  `WordArtRenderer`: the room in each line (`room`), the frame's ascent with the leading, each unit's pictures under its
  glyphs (`effects`, `unit`), an inner shadow in `Drawn.getOver`, written after the text in the SVG, flat pictures
  clipped to the box and its text rectangle, a turned box's pictures turned.
- Tests: `WordArtRendererTest.runEffectsAreAnEffectList`, `theRoomIsWords` (15 of Word's measured boxes, within 0.03
  pt), `effectsAreDrawnWithTheirRoom`.

### Gates

`gate_effects.py`, Word's PDFs against docx4j's (Aptos, Calibri and Times New Roman as Word has them), both rounds:
- box height (the room): within 0.07 pt on every probe but two (we20-19, Calibri's reflection 16 pt down, 0.63: the
  move's 1%; we21-03 and -04, 144 pt, where Word broke "WordArt" over two lines and docx4j didn't);
- text origin: within 0.25 pt on all but we13-03 and -05 (below);
- pictures counted alike on every probe but we13-03 to -05, we18 (below) and we21's 144 pt;
- picture bounds at 50% and 5% of the peak: medians 0.33 to 0.51 pt in every file, the floor of the comparison (a
  picture is 0.36 pt a pixel, Word's grid anchored on the frame and docx4j's on the ink, and docx4j's "WordArt" 0.33 pt
  longer than Word's without any effect); worst under 1.3 pt flat but for reflections 16 pt away (Times New Roman 3.6,
  Aptos 12 pt 4.5 at 5% of a peak of 0.07); Follow Path and the turned arch up to 6.2 pt (textCircle in two lines), the
  glyph places of phase 2 (section 4 of the phase 1-2 findings);
- alpha peaks: within 0.13 everywhere (the first run, before the reflection's fade was moved: 0.46).
Phase 0's effect probes (wa30, wa31, wg10 to wg12) through the same gate: boxes within 0.07 pt, the gallery's styles flat
and waved within 0.84 pt, arched within 2.1 (phase 2's places), styles 5 and 10 one picture in Word (3-D: phase 6).
Corpus gate b469 against b464: 0 movers, events equal; the corpora's PDFs as b468's but for the two documents with time
fields (5361, 11875), the 324 probes' the same. Tests: core-tests 2,653, chart-render 88, export-fo 111, export-fo-tests
966 pass.

### Open

- Times New Roman's reflection is drawn moved 2.29 times its dist at 36 pt where its room says 2.09 (Aptos 2.65 and
  2.70, Calibri 2.49 and 2.55): docx4j draws by the room's rule.
- The text's 1/300 inch grid. A third round (`cr083-phase3c`, we23-grid, 36 probes, 2026-11-13; Jason: "let's run the
  probes"): the box moved down 0 to 0.27 pt in 0.03 pt steps, with no effect and with a glow; the room above swept
  across a step; Aptos at 33 to 40 pt. What it shows: Word floors the box's position to twips (0.05 pt); the text's
  baselines lie on one 1/300 inch grid of the page, not of the box (the box moved down one twip moves the text a whole
  0.24 pt step, which then holds while the box moves on 0.20 pt); and Word draws the type at a size on the same grid (33
  pt as 33.12, 37 as 36.96; 38 to 40 pt 0.024 more than the grid's: 37.944, 39.144, 40.104; body text too: 14 pt as
  13.92), as the fidelity register's open cause has it for body text (causes.md, 2026-10-06, ledger9). No single snap
  fits all 36 baselines: rounding, flooring or ceiling to a 0.24 pt page grid of any phase, after the box, the room and
  the ascent each taken exact or on twips or a 600 dpi grid, at the drawn or the nominal size, leaves half a step on
  some (the box family alone fits ceil at a phase of 0.0675 pt, the room sweep alone fits many rules, the sizes none;
  nor a whole-pixel ascent at 300 dpi). So the snap passes through Word's device font metrics, not measured here.
  docx4j can't follow it in any case without the box's place on the page, which its renderer doesn't have (the drawing
  is placed by the FO and HTML routes): the text stays unrounded, within 0.21 pt (0.24 at most). WordArt's glyphs are
  laid out at the nominal size, not Word's grid size (`XsltFOFunctions.gridSizePt` rounds a leader's), 0.36% wide at 33
  pt: the register's cause decides it for body text first.
- A text box with effects but no warp (D12: not WordArt) is text: Word gives it the room and draws its effects (we13-03
  to -05, fixed boxes): phase 5, with body text.
- The shape's own `a:effectLst` cast by its text (we18; CR-081's `render.effect.shape_text`, reported, not drawn).
- Word draws each unit's pictures just before its glyphs, docx4j all of them first: a Follow Path glyph's shadow doesn't
  fall on the glyph before it, a second line's glow doesn't fall on the first line's text.
- Unmeasured: the room under exact line spacing (none given), a reflection's or shadow's skew on text, a turned box's
  clip, sizes past 96 pt (at 144 pt Word broke the line), warped WordArt's reflection at other sizes than 36 pt.
