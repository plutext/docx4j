# CR-083: WordArt drawn - text warps (`a:prstTxWarp`), text outlines, fills, shadows and glows, and VML WordArt

Status: ACCEPTED 2026-11-12 (Jason: "do CR-083 phases 1 and 2", after D1 to D11 were decided). Proposed 2026-11-09,
drafted on Jason's request (the four CRs of 2026-11-09; see CR-080's Status). Phase 0 run and read 2026-11-12
(073bce285); D1 to D11 decided the same day (§8): D2, D3, D5, D6 and D10 from its findings, the rest as recommended.
Phases 1 and 2 built 2026-11-12 (`CR-083-notes/phase1-2-findings.md`): WordArt laid out, sized, warped and
painted as D2 has it, in PDF (both routes) and HTML. One addition to §5.1 for Jason: Word's own WordArt form
(`textNoShape` with `wrap="none"`) is drawn too. Boxes within 0.05 pt of Word's; the two-path warps as phase 0's model
(median 0.28 pt) and textDeflateInflate's and textDeflateInflateDeflate's bands within 0.83 pt; the 11 presets phase 0
left unexplained and textButtonPour's further lines are still off. Fidelity gates b465 and b466: 0 movers (36e922c7b).
Follow Path remade 2026-11-13 from a round of 435 probes (Jason: "commit then do the Follow Path probes"; findings §4):
Word scales the path and the text by D / (D + 2T), the text rectangle's larger side over itself plus twice the lines'
ink, and spaces the glyphs by arc length along the scaled path; glyph places median 1.11 pt (7.07 with the first fit);
textButton within 0.8 pt; gate b467: 0 movers.
Phase 3 designed 2026-11-13 (Jason: "design CR-083 phase 3"; `CR-083-notes/phase3-findings.md`): phase 0's effect
probes read again (pictures under real text, one for each glyph on Follow Path, an inner shadow written as a shadow
scaled to 0, and room in the line that grows an `spAutoFit` box, in body text too); a round of probes first.
D12 to D15 decided as recommended the same day (Jason: "decisions 12 to 15 as recommended"). Reviewed the same day
by a Claude Fable 5.1 session (Jason: "have fable review the design"), its findings applied (Jason: "yes, apply those
changes"; findings §3): Word draws a text effect at about 0.6 of its stated size and in a frame that isn't the glyphs'
ink, both now the round's to measure (about 190 probes), and D14's reflection is one mirrored block, cut glyph by
glyph.
Phase 3 built 2026-11-13 (Jason: "commence phase 3"; findings §4) from two rounds (`cr083-phase3`, 235 pages, and
`cr083-phase3b`, 34): Word draws a flat run's shadow and glow at (size / 72)^0.7 of their lengths, a warped run's at
their stated lengths, a reflection moved 0.266 of the run's descent per point of dist; the frame is the run's line box,
and the room it takes in its line, Word's rule on that frame (`TextEffects.reach`), fits 176 of 180 probes' boxes within
0.05 pt, body text's alike; docx4j draws them through `Effects.drawText` (picture bounds at the comparison's floor, about
0.4 pt median); a face's line gap now set above its ascent, as Word sets it; gate b469 0 movers. Reviewed the same day
by a Claude Fable 5.1 session (Jason: "commit, then have fable conduct a review of the implementation"; findings §5):
a scaled reflection's fade fixed (sy -50% now as Word's), a reflection's `algn="none"` read as `b`, a budget on Follow
Path's pictures, and four smaller; gate b470 0 movers.
Phase 4 designed 2026-11-13 (Jason: "start phase 4", "write up a design for phase 4"; `CR-083-notes/phase4-findings.md`):
Word draws VML WordArt as filled glyph paths with no text in its PDF, and merges a `v:textpath` with its shapetype's
attribute by attribute (its watermark needs it); phase 0's "first shapetype of an `o:spt` wins" was a probe fault; real3's
one document's three VML WordArt never print, so the oracle is a round: Word's own 40 shapetypes first, then about 300
probes. D16 to D19 decided as recommended the same day (Jason: "decisions 16 to 19 as recommended"). Reviewed the
same day by a Claude Fable 5.1 session (Jason: "commit then have fable review the design"), its 13 findings applied
(Jason: "apply all 13 ..."; phase 4 findings §4): a geometry source so that the renderer takes VML; Word stretches the
text's ink box (`trim`) or a metric box onto the shape's box before the warp; every path filled even-odd; real3's
WordArt unprinted for want of `w:titlePg`; the evaluator's open points stated; a VML turn in FO; D17 revised to go by
the part, not the id; the round about 285 probes, phase 4 eight days.
Phase 4 built 2026-11-13 from Word's own 40 shapetypes and two rounds (`cr083-phase4`, 263 probes; `cr083-phase4b`,
17), reviewed by a Claude Fable 5.1 session and its open items triaged by another (findings §5 to §10): 228 of 263
within 0.7 pt of Word's ink, Word's 40 shapetypes within 1.5, gates b471 to b478 0 movers; closed the same day (Jason:
"close phase 4"), a watermark centred on the margin under a header that overflows it split off as phase 4b, the rest
left to phase 7's gap report.
Phase 5 designed 2026-11-14 (Jason: "close phase 4; design phase 5, then give your design to fable for review";
`CR-083-notes/phase5-findings.md`): phase 0's and phase 3's body-text probes read again (Word's gradient on a body run
spans the run's advance and font box, blended by phase 4's curve; the room is phase 3's rule) and the fork read (the
`docx4j:` namespace and the Word line take a span's room; foreign attributes don't reach text yet); D20 to D24
proposed: docx4j paints a body run's glyph paths and pictures through a callback the fork calls as it draws the run, the
text kept real (D20, revising D3's modes); a round of about 70 probes first. Reviewed the same day by a Claude Fable
5.1 session, its 13 findings applied (phase 5 findings §4): Word's curve is the two-stop gradient's only, any other
blended evenly; the painter draws an SVG document as an artifact, from glyph ids at the fork's positions, its effects
found per render; the room under each spacing rule stated. D20, D21 and D23 decided as recommended, D22 and D24 as the
review has them (Jason: "D20 as recommended; D21 as recommended; D23 as recommended; D22 and D24 as per fable
review").
Phase 5 begun 2026-11-14 (Jason: "commence phase 5"; findings §5): the round `cr083-phase5` built and staged (88
probes, `build_body.py`); the fork's CR drafted by its session (fop/CR-043: design only, its build Jason's to order;
four corrections to findings §2, the subset's glyph ids among them); step 1 built: a solid `w14:textFill` the run's
colour in PDF (both routes) and HTML, HTML's CSS for the rest (D23), Word's curve for a two-stop gradient (D24), and
flat WordArt's gradient spanning the run's frame, not its ink (wa30-14: 43.9 pt where docx4j's spanned 23.9); WordArt's
gradients now Word's shadings within 1 of 255 and 0.2 pt; gates b479 and b480 0 movers, one corpus document's run in
its fill's colour, as Word draws it. The round read the same day (findings §6): the frame a run's part on each line
in one font, decorations painted as the glyphs, a right-to-left gradient reversed, the room phase 3's rule at body
sizes (atLeast moving the baseline), and a shape's two-stop gradient on Word's curve too (applied; gate b481 0 movers).
Phases 1 and 2 reviewed by another Claude Fable 5.1 session the same day and its nine findings fixed (Jason: "fix all 9 as
suggested"; phase 1-2 findings §8): decorations drawn, a warp's points capped, empty paragraphs, gate b468 0 movers.
Needs CR-080 phase 1 (the geometry engine evaluates the warp presets), and CR-081 phase 1
for a WordArt's shadow and glow. Read against docx4j c0a66b610, Apache POI trunk ae62bb5116 and docxide-pdf 9508da2, and the specifications in §12.
Revised 2026-11-09 (Jason: "add a rendering phase to the CRs where appropriate: WordArt or SmartArt we can't output is
a gap"):
- phase 5 draws WordArt effects on body text through the FO renderer fork (D3);
- phase 4 draws VML WordArt whatever the count (D6);
- phase 6 draws 3-D WordArt with CR-081's 3-D (D10);
- phase 7 reports what is still not drawn (D9).

Reviewed 2026-11-09 by a Claude Fable 5.1 session at Jason's request. Its findings were applied on Jason's word
("apply the review's findings"):
- HTML output (phase 1, D8);
- pptx-renderer's arch warps (§3);
- the MIT permission notice (§3, §10);
- invisible text as `fill="none"` (§5.5);
- the fork's existing text rendering modes (phase 5);
- headers and footers (D11).

Revised 2026-11-09 (Jason: "make the LibreOffice wording black-box only in all four CRs; rewrite POI's txXfrm
text-rotation rule and its VML shape-type table"):
- LibreOffice is a black-box oracle only (CR-080 D11);
- POI's `txXfrm` rule (CR-082) and its `ShapeType` table (CR-083, CR-080 D6) are written independently and not
  ported.

Phase 0 started 2026-11-12 (Jason: "do CR-083 phase 0"), by CR-081's and CR-080's route (word-bridge, Word's re-save
and PDF of purpose-built probes; `CR-083-notes/`):
- Round 1, `cr083-phase0`, staged: 19 files, one probe a page, 467 probes (`build_wordart.py`, `probes.tsv`; checked by
  `check_wordart.py`, which adds the VML schemas, the header parts and a w14 schema check of the run effects to
  CR-081's `check_probes.py`, and catches 7 of 7 deliberate breaks), and Jason's document as it is (below). The 40 warps
  in Word's form and in a fixed box with their default adjustments, and with one other in the fixed box, on one and two
  lines; three lines on the multi-path presets; `fromWordArt`, autofit, wrap, insets and size;
  the text's rectangle (ink or the font's height); the w14 effects flat and warped; two runs; 3-D; the same effects on
  body text (D3, D10); and, for D6, the 40 warps as wps WordArt in a Word 2007 mode document (Word's save converts them
  to VML or not: its o:spt numbers and shapetypes), one VML WordArt for each o:spt 136 to 175 with a shapetype carrying
  only its o:spt (does Word draw by o:spt or by the path?), and text watermarks.
- The text is Aptos, whose files Word's VM has in its cloud fonts (the share's snapshot), so that a warped outline can
  be compared with the exact font.
- The 15 WordArt gallery styles, Transform and 3-D applied through the interface, and the 15 Text Effects styles on
  body text need Jason's hand, as CR-081's picture styles did: `build_hand_start.py` writes the starting document (36
  pages). Jason applied them on 2026-11-12 (`cr083-styles-hand.docx`); `build_hand_start.py harvest` reads it into
  `styles-word.tsv`. The WordArt gallery and the Text Effects gallery write the same run properties, style for style
  (asserted for all 15); styles 5 and 10 (3-D) add `a:scene3d` and `a:sp3d` to a WordArt's `bodyPr`, while body text
  gets `w14:props3d` alone, with no camera. Style 15 writes a `w14:shadow` with `sx="0" sy="0" algn="none"` (the
  gallery's inner shadow, which w14 has no element for: the PDF will show what Word makes of it); styles 4 and 14 also
  set the legacy `w:outline`, and 9 and 15 `w:spacing`. Bevel's first preset is "Round" in Word's interface
  (`prst="circle"`). The 3-D Rotation page wrote no camera. Set `cr083-phase0-styles` (5 files: the hand document as
  it is, and the 15 styles on WordArt flat, as Follow Path and as a Warp, and on body text; 60 probes) is staged after
  round 1.
- Word's own form, from a document Jason made by hand on 2026-11-11 (`cr083-wordart-jason.docx`, seven WordArt,
  staged as it is): a `wps` text box (`txBox="1"`) with no fill and no line; `bodyPr` `wrap="none"`,
  `fromWordArt="0"`, default insets, `a:spAutoFit` and `a:prstTxWarp`, a Transform (Follow Path) included; stored at
  144 by 144 pt whatever its text. Bevel is written twice, as `bodyPr`'s `a:scene3d` and `a:sp3d` and as `w14:props3d`
  on each run (no `w14:scene3d`); 3-D Rotation is `bodyPr`'s camera alone. The `mc:Fallback` is a VML `v:textbox`, not a
  `v:textpath`, so a VML reader loses the warp. No Word-written warped WordArt was found on this machine before that
  (the only hits were the standard's own examples). The generated WordArt carries a 0.25 pt grey line, so that the
  box Word used shows in its PDF.
- Found on the way: the VML WordArt on this machine's documents is nearly all Word's text watermark (o:spt 136 in a
  header, `PowerPlusWaterMarkObject`), which docx4j doesn't draw today; phase 4 (D6) covers it, and the load survey's
  counts will say how common it is.
- Both sets ran on 2026-11-12, all 28 jobs silent; read the same day: `CR-083-notes/phase0-findings.md` (readers
  `survey_wordart.py`, `warp_model.py`, `measure_warps.py`; tables `survey-word.tsv`, `warps-word.tsv`). In short:
  - D2: Word draws the glyphs as paths over invisible text (transparency, not mode 3) for a warp, an outline, a
    gradient fill; real text for a solid fill, and under shadow, glow and reflection pictures; for 3-D WordArt and a
    fill with transparency, a picture or paths with no text at all.
  - The box: `wrap="none"` sizes its width to the text, `spAutoFit` its height; `fromWordArt` changes nothing.
  - D5: §20.1.9.19 is Word's method (the block's ink, arc length, the straight joining line, on the text rectangle):
    157 of 198 two-path probes within 0.5 pt; textDeflate and textDeflateBottom read `ss` as the height; 11 presets
    not yet explained. Follow Path scales the text by a rule still to fit; multi-path presets give each line a band.
  - `w14:alpha` is a transparency, the reverse of `a:alpha`.
  - D6: Word draws VML WordArt from its shapetype's path and formulas, not its `o:spt` (spt-only shapetypes draw
    nothing), and writes no VML WordArt from DrawingML, so phase 4 evaluates VML geometry instead of a table.
  - The CR changes the findings proposed (their section 10) were made on 2026-11-12, as recommended (Jason: "make the
    phase 0 findings section 10 proposed CR changes"): D2, D3, D5, D6 and D10 (§8), and §0, §2, §3, §5 and phases 2,
    4 and 5 to match.

Owner: Jason Harrop. Drafted with Claude Opus 5.5.

## 0. Summary

Word's WordArt is a text box (`wps:wsp` with `wps:txbx`) whose body properties name a text warp
(`wps:bodyPr/a:prstTxWarp`, one of 40 presets besides `textNoShape`) and whose runs carry Word 2010's text effects
(`w14:textOutline`, `w14:textFill`, `w14:shadow`, `w14:glow`, `w14:reflection` in `w:rPr`). docx4j's PDF lays the text
out flat, in the run's colour, with none of the effects.

This CR draws such a text box as glyph outlines: laid out with the font file FOP embeds, warped through the preset's
paths (CR-080's engine evaluates `presetTextWarpDefinitions.xml`, which docx4j already ships), filled and outlined as
its effects say, shadowed and glowing with CR-081's effects. Where Word's PDF draws the glyphs (a warp, an outline, a
gradient or transparent fill), the text also goes in the PDF as invisible text, so that it can still be searched and
copied; elsewhere it stays real text, as in Word (D2). Legacy VML WordArt (`v:textpath`) is drawn from its shapetype's
own path and formulas, as Word draws it (D6).

The same effects applied by Word's WordArt gallery to ordinary body text are drawn too, by the FO renderer fork from
attributes docx4j writes, so that the text stays text in docx4j's layout (D3). 3-D WordArt is drawn flat until
CR-081's 3-D renderer exists, then in 3-D (D10). Whatever is still not drawn as Word draws it is reported (D9).

The warp itself is described step by step in ISO/IEC 29500-1 §20.1.9.19, and this CR writes it from there. docxide-pdf
and office-open-xml-viewer each implement part of it and serve as cross-checks; POI has none (§3).

## 1. What docx4j does today

- A WordArt text box is a text box: its text in an FO container (`FOTextBoxes`, CR-080 §1). `a:prstTxWarp` is ignored,
  and so is `wps:bodyPr fromWordArt`.
- `w14:textOutline`, `w14:textFill`, `w14:shadow`, `w14:glow`, `w14:reflection` and `w14:props3d` are bound in `RPr`
  (`org.docx4j.w14.*`) and nothing in the FO output reads them. A `w14:textFill` solid colour is not the text's colour
  in the PDF; `w:color` is.
- The 40 warp presets ship in docx4j-core (`org/docx4j/model/shapes/presetTextWarpDefinitions.xml`). By path count:
  3 have one path (`textArchUp`, `textArchDown`, `textCircle`: text on a path), 33 have two (an envelope between a
  top and a bottom path), and 4 have more (`textButton` 3, `textDeflateInflate` 4, `textButtonPour` and
  `textDeflateInflateDeflate` 6: bands for lines of text).
- VML WordArt (`v:shape` with `v:textpath` and a `v:shapetype` of `o:spt` 136 to 175) is not drawn at all (the XSLT's
  "TODO: handle w:pict containing other than ./v:shape/v:imagedata"; the visitor takes it for a picture and logs
  "Couldn't find imageData!"). In a `w:object`'s `v:group` neither it nor the group's pictures are drawn. Nothing in
  docx4j evaluates VML formulas or paths (phase 4 findings §1.5).

## 2. What Word does (phase 0 measured it)

Measured in phase 0, 2026-11-12 (`CR-083-notes/phase0-findings.md`):
- Word's PDF draws a warp, any outline, a gradient fill and a fill with transparency as the font's own glyph outlines
  over invisible text; a solid fill stays real text, as does text under a shadow, glow or reflection picture (its §1;
  D2). Body text is treated alike (D3). 3-D WordArt is one picture, with no text (its §7; D10).
- A two-path warp maps the ink of the whole block of lines, by arc length along each path and down the straight line
  joining them, on the text rectangle: §20.1.9.19 (its §3; D5). Follow Path draws each glyph turned to the path and
  scales the text; a multi-path preset gives each line a band (its §4, §5; phase 2).
- `fromWordArt` changes nothing, nor `a:normAutofit` with text that fits; `wrap="none"` sizes the box's width to the
  text, and `a:spAutoFit` its height (its §2).
- An outline is a stroke of its width, cap, join and dash (compound and gradient ones filled paths), and a gradient
  fill a shading over the glyphs' path (its §6).
- VML WordArt is drawn from its shapetype's path and formulas, not its `o:spt` (its §8; D6).

Office's notes ([MS-OI29500], §12) settle three things before measuring:
- `fromWordArt` defaults to false;
- a VML `v:textpath` centres by default and fills its parent shape's box whatever its `font-size` (phase 4 findings
  §1.1: across, and down to its ink with `trim`, to a font-metric box without);
- `textCircle` and `textDeflateTop` have Office's illustrations.

docxide-pdf's reading, from Word for Mac's online export:
- a warp's text height is normalised by `size x ascender / (ascender - descender)`, so capitals fill the shape;
- a DrawingML warp keeps the text's natural width, centred, and a VML WordArt with `fitshape` stretches to the box's width.

## 3. Sources evaluated

- **Apache POI**: no text warp anywhere (no `prstTxWarp` in its sources, no warp definitions file). Its text drawing
  (`DrawTextParagraph`, `DrawTextShape`, 1,000 lines) lays flat DrawingML text out with Java2D's `TextLayout` over
  POI's own model. Java2D gives that directly, and the WordArt text here is WordprocessingML. `sl.usermodel.ShapeType`
  names the VML text shape types (`TEXT_PLAIN_TEXT` 136 to `TEXT_CAN_DOWN` 175). **Not ported** (Jason, 2026-11-09).
  Phase 0 found that Word draws VML WordArt from its shapetype's path, not its `o:spt` (D6), so docx4j needs no such
  table to draw it. [MS-ODRAW] §2.4.24 `MSOSPT` numbers the types `msosptTextPlainText` (0x88, 136) to
  `msosptTextCanDown` (0xAF, 175), names a report can use.
- **docxide-pdf** (`src/pdf/wordart.rs` 792 lines, `src/docx/wordart.rs` 313; ASL 2.0, no NOTICE):
  - **Envelope** (`render_warped_textbox`, `warp_point`, `SampledBoundary`): glyph outlines from the font
    (`ttf_parser`), each point mapped by its x share across the width and its y share between the top and bottom
    paths, each path sampled as y as a function of x.
  - **Text on a path** (`render_text_on_path`, `ArcLengthPath`): the path parameterised by arc length, each glyph placed
    at its advance and turned to the tangent.
  - **Invisible text** (`invisible_text`): the text drawn again in PDF rendering mode 3 for search and copy.
  - **Reading**: `w14:textOutline`, `w14:textFill` (solid and gradient), `w14:shadow` and `w14:glow`; VML
    `v:textpath` with its fill, stroke and font.

  From the code, what it doesn't do:
  - it takes the **first two paths** of every preset, so the four multi-path presets are drawn as their first band;
  - y-as-a-function-of-x is wrong where a boundary turns back on itself (`textCirclePour`'s circles);
  - every glyph takes the **first run's** font, size, colour and effects (`collect_text_info`);
  - its roadmap lists VML gradient and pattern fills, VML shadow, and VML shapetype-to-preset mapping as open.

  **A cross-check, and the source of the invisible-text idea.** Where the warp departs from §20.1.9.19 (see below),
  the standard's method replaces it:
  - each path is measured by arc length, not sampled as y(x);
  - every path is used;
  - formatting is per run.

- **office-open-xml-viewer** (yukiyokotani, MIT, `packages/core/src/shape/text-warp.ts`, 18 KB, a4f7785;
  PowerPoint-measured, with probe and visual tests).
  - **Boundaries by arc length**: each warp path is flattened to a polyline, and both edges are sampled at the same
    arc-length fraction *u*. This is the correction docxide-pdf needs.
  - **Follow Path for the three single-path presets**: the text keeps its natural width and runs from the path's
    start for that much arc length; it isn't stretched over the whole path. (Phase 0: not Word's; Word shrinks the
    text in every probe.)
  - **More than two paths**: the first and last paths are taken as the outer edges ("faithful enough").
  - **Glyphs** are placed whole, turned to the local slope and scaled to the gap, because Canvas 2D can't deform an
    outline. docx4j's SVG can, as docxide-pdf does.

  Its docx side uses only VML `v:textpath` (`packages/docx/src/layout/vml-text-path.ts`). **Take its arc-length
  sampling and Follow Path rule as phase 0's first hypotheses**, measured against Word, which may differ from
  PowerPoint. Its multi-path simplification is checked against Word's bands.
- **ChristopherVR/ooxml** (Apache-2.0, TypeScript): text warp in its pptx UI (`src/ui/src/pptx/dom/elements/text-warp.ts`,
  `render/text-warp-arch-fit.ts`) with an envelope parity test. **A cross-check**, read in phase 2.
- **LibreOffice** (MPL-2.0): Fontwork, text along a path, and its import of `prstTxWarp`. A black-box oracle only (CR-080 D11): probe documents converted with `soffice` and the output compared, its source not read,
  as a Java translation of MPL-2.0 code would stay MPL.
- **pptx-renderer** (Apache-2.0): only `textArchUp` and `textArchDown`, for single-line text
  (`src/renderer/ShapeRenderer.ts`, `getSupportedTextWarpPreset`). **Not a source**, but a third cross-check for the
  Follow Path hypothesis on the arch presets.

**The standard describes the algorithm** (ISO/IEC 29500-1 §20.1.9.19, `prstTxWarp`, twelve steps, found on
cross-checking 2026-11-09):
1. take the tightest rectangle around the unwarped text, with no white space except space characters;
2. give every end point and control point of the glyphs' quadratic and cubic Béziers its horizontal and vertical
   fraction of that rectangle;
3. measure the horizontal fraction along the top path and along the bottom path, each flattened and measured by its
   own length;
4. join the two points with a line, which need not be vertical;
5. go down that line by the vertical fraction; that is the new point;
6. apply this to the whole body of text.

This is outline deformation by arc length. docxide-pdf deforms outlines but samples the paths as y(x).
office-open-xml-viewer measures by arc length but places whole glyphs. Each is half of the standard's method. The
standard speaks of "two paths" only: the 3 one-path presets and the 4 presets with more paths are left to what Word
does (phase 0). Its rectangle is the text's ink, not docxide-pdf's normalised font height; phase 0 found Word uses
the ink of the whole block of lines (D5).

So the warp is written from §20.1.9.19 (D5). docxide-pdf and office-open-xml-viewer are cross-checks, and their
rules for the cases the standard leaves open (Follow Path for one path; the outer edges for more than two) are phase
0's first hypotheses. If code is ported from either, the file names its source and commit in its header (for example
"after docxide-pdf src/pdf/wordart.rs at 9508da2, Apache License 2.0; changes by Plutext"), and `legals/NOTICE` gains
an entry for each project used. For the MIT project that is its copyright line ("Copyright (c) 2026 Yuki Yokotani")
and the MIT permission notice. The licence requires both in copies, so `legals/NOTICE` carries the licence text or
points to a copy bundled with it, as it does for other third-party code.

## 4. Corpus facts (2026-11-09, the four fidelity corpora)

`a:prstTxWarp` other than `textNoShape`: 0 documents in 561. w14 text effects in a run: real 2 documents (4), real2 4
(53), real3 3 (11), real-c2 2 (39), in text boxes and body text alike. VML `v:textpath`: real3 1 document (6: three
WordArt and their shapetypes, in first-page headers of sections with no `w:titlePg`, which Word doesn't print; phase 4
findings §1.4). WordArt
is rare in these corpora, which were chosen for text. Phase 0 counts warps and run effects in the load survey's
catalogue (through the portfolio).

## 5. Approach

1. **Which text boxes**: a `wps:wsp` whose `bodyPr` has a `prstTxWarp` other than `textNoShape`, or whose runs carry
   a `w14:textOutline`, a non-solid `w14:textFill` or a solid one with transparency (D2, D3), is drawn as WordArt.
   Added in phase 1 (for Jason; phase 1-2 findings §2): Word's own WordArt form, `prstTxWarp prst="textNoShape"` with
   `wrap="none"`, whose box Word sizes to its text where FO wrapped it in the stale 144 pt stored; `textNoShape` alone
   isn't the mark (the corpora have 26 such text boxes, all `wrap="square"`). Any other text box stays FO text, and so
   does a WordArt holding a table or a picture (`render.wordart.content`).
2. **Layout**: the box's paragraphs are laid out as lines of glyphs with `java.awt.Font` loaded from the font file
   docx4j's `PhysicalFonts` maps the run's font to, the same file FOP embeds (D4). Each run has its own font, size and
   effects. This is a line layout of a few lines, not a paragraph layout engine: alignment, the paragraph's
   indentation, and line breaks where Word put them (`w:br`) or at the box's width.
3. **Warp**: CR-080's engine evaluates the preset with its `a:avLst` on the text rectangle (the box less its insets).
   For two paths, every glyph outline point is mapped as §20.1.9.19 describes (§3), as phase 0 found Word reads it
   (D5): by its fractions of the ink of the whole block of lines, measured along each path by arc length and down the
   straight line joining the two points, the outline warped as a curve, not by its control points alone.
   `textDeflate` and `textDeflateBottom` take `ss` as the height. For one path (Follow Path), each glyph is its own
   path turned to the path's direction, and the text is scaled by a rule phase 2 fits to phase 0's probes (findings
   §4). For more than two, each line of text takes a band (findings §5), measured in phase 2.
4. **Paint**: fills and outlines from `w14:textFill` and `w14:textOutline` (solid, gradient; width, dash, join, cap),
   via chart-render's `Fills` path for DrawingML fills (w14's are DrawingML fills in another namespace). Shadow, glow
   and reflection go through CR-081's effects on the glyphs' outline. The result is one SVG in the text box's place.
5. **Invisible text** (D2): where the glyphs are drawn as outlines, the text again as SVG text with `fill="none"` (and
   no stroke), where the unwarped text would be (the line's natural layout in the text rectangle) and at the run's size
   (Follow Path: the scaled size), as Word places its own; also where Word's PDF has no text at all (3-D WordArt, a
   fill with transparency). Elsewhere the text stays real text, as Word keeps it. The fork's
   `PDFTextPainter.beginTextObject` sets the text rendering mode from the fill and stroke paints, so `fill="none"`
   gives PDF mode 3 (neither filled nor stroked: invisible, but found by a search and copied). Word makes its text
   invisible with transparency instead (an `ExtGState` with `ca 0`); the two are the same for search and copy, and
   mode 3 is simpler in FOP (D2).
6. **VML** (D6): a `v:textpath`'s shape is drawn from its `v:shapetype`'s own `path`, `formulas` and `adj` (and the
   shape's `adj`), evaluated by a VML formula evaluator, as Word draws it; its paths then warp the text as a preset's
   do (step 3), once the text's ink box (`trim`) or metric box is stretched onto the shape's box (phase 4 findings
   §1.1). A shapetype with no path draws nothing, as in Word. Its `string`, `style` (font and size), `fitshape`
   and `fitpath` are read, its `v:fill` and `v:stroke` are the paint. Text watermarks (type 136 in a header) are drawn
   as filled outlines turned by `rotation`, as Word draws them.

## 6. Phases

- **Phase 0, the oracle.**
  - Probes: each of the 40 presets with default adjustments and one other, one line and two lines; with
    `fromWordArt` on and off; each WordArt style of Word's gallery (15) flat; outline, gradient fill, shadow and glow
    alone; two runs of different size and colour; one VML WordArt for each text shape type, 136 to 175, saved in Word 2007 compatibility mode (the oracle for D6's
    table). Word's PDFs
    through the bridge.
  - Also: the 15 gallery styles applied to ordinary body text, which writes the same `w14` effects on body runs
    (D3), and Word's 3-D text effects (bevel, depth, rotation) on WordArt and on body text (D10).
  - Read: whether glyphs are outlines or text (`mutool trace`/`stext`); glyph positions; the band rule; for body
    text, whether Word's PDF keeps outlined and gradient text as text (PDF text rendering modes) or draws it.
  - The load survey's counts (via portfolio).
  - **Gate:** numbers committed; D2 settled by what Word does.
- **Phase 1, flat WordArt.**
  - The layout, outlines, fills and invisible text, with no warp (D3's text boxes with run effects), in FO (visitor and
    XSLT) and in HTML: the same SVG inline, as charts are (`HTMLExporterVisitorGenerator.createChartBox`,
    `ChartBox.createHtml`; `docx2xhtml-core.xslt`).
  - **Gate:** probes within tolerance; corpora unmoved except their WordArt boxes (colour, not layout).
  - Built 2026-11-12 (`CR-083-notes/phase1-2-findings.md`), with phase 2: the box as Word draws it (within 0.05 pt on
    phase 0's probes), Word's kerning of WordArt (measured on the way: whatever `w:kern` says), real text within
    0.06 pt of Word's, invisible text found by search (mode 3), the effects of phases 3 and 6 reported.
- **Phase 2, the warps.**
  - The 33 envelopes, by §20.1.9.19 as phase 0 found Word reads it (D5; findings §3): the text rectangle, the ink of the
    whole block of lines, arc length along each path, the straight joining line, the outline warped densely; and
    `textDeflate` and `textDeflateBottom` with `ss` as the height. The 11 presets phase 0 left unexplained
    (`textCirclePour`, `textRingInside`, `textRingOutside`, `textCanUp`, `textCanDown`, `textStop`, `textArchDownPour`,
    `textArchUpPour`, `textWave1`, `textWave2`, and `textTriangle` adjusted) are resolved against its probes
    (`warps-word.tsv`).
  - Follow Path, the 3 one-path presets: each glyph its own path, turned to the path's direction; the text's scale and
    its spacing along the path fitted to phase 0's probes (findings §4).
  - The 4 multi-path presets: a band for each line, as Word gives them (findings §5), measured as the envelopes are.
  - **Gate:** each preset's probes within tolerance (glyph-outline points against Word's, after alignment).
  - Built 2026-11-12 with phase 1 (`CR-083-notes/phase1-2-findings.md`, `gate_wordart.py`): the envelopes as phase
    0's model (180 of 239 probes within 0.5 pt, median 0.28; the misses the 11 presets still unexplained); Word's band
    rule read from its PDFs (line i in band i, extra lines in the first; a band's lines across the whole block's ink and
    down a range as tall as the tallest band's), textDeflateInflate's and textDeflateInflateDeflate's within 0.83 pt;
    Follow Path fitted (a scale and a path drawn in, by the path's length and the box's flatness over the text's width;
    lines 0.665 of the scaled size apart), median 2.7 pt. Open: the 11 presets, textButtonPour's further lines and
    textButton, Follow Path for the text's height and long lines (a round of probes would settle it).
  - Follow Path remade 2026-11-13 from the Follow Path round (cr083-followpath and cr083-followpath2, 435 probes;
    findings §4, `build_followpath.py`, `read_followpath.py`, `check_followpath.py`): the scale D / (D + 2T) (408 probes
    within 0.003), the text along the whole ellipse centred on the arc's middle, each glyph's middle at its arc length
    along the scaled path and its baseline 0.145 of its size inside, further lines apart by their inks; capped on an arc
    adjusted short. Glyph places median 1.11 pt (7.07), outlines 1.25 pt (7.73); phase 0's Follow Path pages 1.36
    (2.85); textButton within 0.8 pt. Open: 141 probes Word measures about 0.06 em outside the path (no trigger found),
    a circle adjusted short, lines of unequal sizes.
- **Phases 1 and 2 reviewed** 2026-11-13 by a Claude Fable 5.1 session (Jason: "have fable review the phase 1 and phase
  2 implementations"), read-only, aimed at what the gates don't reach; its nine findings fixed (Jason: "fix all 9 as
  suggested"; phase 1-2 findings §8): a run's underline, strikes, highlight, shading and vertical alignment, which the
  line layout dropped, drawn on flat WordArt as the FO output draws them and reported on warped
  (`render.wordart.decoration`); a warp's points capped (a piece a hundredth of its glyph's size past 50 pt, at most
  1,000,000 points, past which it is drawn unwarped, `render.wordart.over_limit`) and running out of memory leaving the
  text box as text; an empty paragraph a line as high as its mark; lines broken at a geometry's narrower text
  rectangle; one space where a line wraps in the alternative text; silent drops reported (`render.wordart.fill`,
  `render.wordart.outline`, `render.wordart.font` for AWT's fallback); a quote in a family's name; the styles not
  cached; a box of no width left as text. Gates: phase 0's probes and the Follow Path round as before against Word
  (one file's warped outlines cut more coarsely, unmoved against Word); b468 against b464 and b467, 0 movers.
- **Phase 3, effects on WordArt** (needs CR-081.1, done): a run's `w14:shadow`, `w14:glow` and `w14:reflection` drawn
  on its glyphs as Word draws them. Designed 2026-11-13 (Jason: "design CR-083 phase 3";
  `CR-083-notes/phase3-findings.md`); reviewed the same day by a Claude Fable 5.1 session, its findings applied
  (Jason: "yes, apply those changes"; findings §3).
  - **What Word does**, from phase 0's probes read again (findings §1):
    - the effects are pictures at 200 dpi under real text, an inner shadow over it; `w14:alpha` in an effect's colour
      is a transparency, as in a fill;
    - **at about 0.6 of their stated size**: a sharp shadow 6 pt long is drawn 2.50 pt across and 2.50 down, where
      4.24 is stated, and an 18 pt glow fades to 5% 7.75 pt outside the ink, where CR-081's rule for a shape gives
      13.7. So CR-081's rules don't carry over to text unchanged; the scale (one on `dist`, `blurRad` and `rad`, or
      not) is the round's;
    - **in a frame that isn't the glyphs' ink**: a squashed shadow is scaled about a point near the descent line; a
      flat reflection starts at the ink's foot, fades over about 32 pt times `endPos` (near the ascent), and moved 4 pt
      down starts 10 pt down; a warped one is mirrored about the warped block's foot. The frame is the round's;
    - flat text and a two-path warp take one picture for each effect over the whole text; Follow Path takes one for
      each glyph, drawn in turn with it (D14), each a cut of the whole block's effect: its reflection is one mirrored
      arch, written glyph by glyph;
    - `w14:shadow` with `sx` and `sy` 0 and `algn="none"` (the gallery's style 15) is drawn as an inner shadow (D15);
    - effects take room in the line: the text sits lower in its box and an `spAutoFit` box grows (style 1's shadow:
      3.70 pt taller, the text 1.20 lower; a glow of 18 pt: 8.16 lower), the same flat and warped, and nothing across.
      Phase 0's 15 points fit the drawn effect's reach past the line (0.62 of a shadow's, 0.45 of a glow's radius,
      adding, in steps of 0.24 pt), which is likely the 0.6 above: a hypothesis for the round. A reflection's room
      isn't explained (D13). Body text takes room too, which phase 5 assumed it wouldn't.
  - **A round first**, `cr083-phase3` (findings §2, about 190 probes in 11 files, `build_effects.py` to write): the
    scale (sharp shadows 12 pt long at each size and face); the frame (a reflection's distance swept, a word with
    descenders); the room (shadow, glow and reflection swept; sizes and faces; anchors, lines and runs); which glyphs
    share a picture (runs, lines, each kind of warp); the silhouette (hollow, gradient and transparent text); what Word
    reads as an inner shadow; theme colours; a shape's own `a:effectLst` on a WordArt (does its text cast it, CR-081
    phase 3's `render.effect.shape_text`); a turned box (do the shadow's direction and the reflection follow the box or
    the page); and body text's room, for phase 5. Read by `read_effects.py` (extended by the review: empty pictures
    kept, Word's glyph paths, a shadow's drawn offset, a glow's contours).
  - **Model** (docx4j-core): `TextEffects.effects(RPr)` gives the run's `w14:shadow`, `w14:glow` and `w14:reflection`
    as a DrawingML `a:effectLst` (`a:outerShdw`, or `a:innerShdw` where Word reads the shadow as inner; `a:glow`;
    `a:reflection`), converted as its fills are (marshalled, renamed, read back), `w14:alpha` read as a transparency,
    and Word's scale for text applied to its lengths once the round has measured it; `WordArtText.Run.getEffects()`.
  - **Drawing** (docx4j-chart-render, `WordArtRenderer`):
    - the units: the glyphs that share one picture (the whole text, flat or on two paths, unless the round shows runs
      or lines apart). Each unit goes through CR-081's `Effects.draw`, with a painter of its glyphs as drawn (filled
      and stroked; a run Word keeps as text painted as its glyphs, for the silhouette) and, as the drawing's box, the
      frame the round finds (the candidates: the lines' ascent-to-descent box flat, the warped block's ink warped).
      Follow Path draws the whole block's effects in the block's frame and cuts a picture for each glyph from them
      (D14);
    - the room (D13), taken from the drawn effects' reach: the lines are laid out, their effects drawn in the line's
      frame and their reach past each line measured, and then the box is sized and the warp maps the text into it.
      Under `spAutoFit` the box's growth changes Follow Path's D (its scale, D / (D + 2T));
    - the SVG in Word's order: under each unit its reflection, shadow and glow, then its glyph paths, then the text,
      then an inner shadow; on Follow Path each glyph's pictures and the glyph in turn, a picture Word writes empty
      left out. `Drawn.getBounds()` takes the pictures in, which pass the box; the wrap's extent is the file's
      `wp:effectExtent`, as for every drawing, so the pictures don't change it;
    - CR-081's resolution (D3: 200 dpi, coarser by blur) and cap (D10) unchanged, each picture capped; past the cap,
      `render.effect.over_limit`.
  - **Routes**: nothing new. The FO routes place the drawing by its bounds (`FOShapes.addWordArt`), and HTML embeds the
    same SVG, its pictures as `data:` URIs, as CR-081's.
  - **Reports**: `render.wordart.effect` only for what isn't drawn as Word draws it (a shadow the round doesn't
    explain). 3-D WordArt is drawn flat with its effects until phase 6 (D10), still reported `render.wordart.flat_3d`.
  - **Not here**: a text box whose only mark is a run's effect stays text, its effects phase 5's (D12).
  - **Gate:**
    - the round's probes and phase 0's effect probes (wa30-17 to -25, wa31, wg10 to wg12, wa00) through docx4j's PDF
      with Word's fonts, read by `read_effects.py` for both. First the geometry: the pictures Word draws (count and
      order), a shadow's offset, a glow's contours, a reflection's mirror line and fade, the box within 0.05 pt and the
      real text within 0.06 pt (or a step of 0.24 pt, if that is Word's rounding). Then the alpha: each picture's
      against Word's at the page's pixels, within 1 of 255 on average flat (CR-081's measure, `compare_effects.py`:
      0.44 to 0.76 there), warped as their glyphs' outlines are;
    - the fidelity corpora unmoved (none of their text boxes is WordArt: phase 1-2 findings §5), events equal;
    - tests: `WordArtRendererTest` (the conversion, w14's alpha and the scale, the units and their frame, the room, the
      inner shadow) and `WordArtFoTest` (an effect picture under real text, the text still extracted).
  - **Built** 2026-11-13 (findings §4), as designed but for these, which the rounds decided:
    - the lengths: a flat run's at (size / 72)^0.7 (`TextEffects.scale`), a reflection's blur at size / 72 and its move
      0.266 of the descent per point of dist; a warped run's as stated;
    - the frame: the run's line box (its ascent with the font's line gap above it) flat; a reflection mirrored about the
      baseline and faded across the ascent, nothing of it before its mirror line; the warped glyphs' box warped;
    - the room from Word's rule on the frame (`TextEffects.reach`), not from the drawn pictures' reach: the rule is exact
      to Word's rounding where the pictures' reach would carry their blur's tail;
    - a glow at 0.44 of its radius (`Effects.TEXT_GLOW_SPREAD`), following the glyphs' coverage;
    - flat pictures clipped to the box across and its text rectangle up and down, as Word's PDF clips them;
    - each unit's pictures before all the glyphs and text, not each before its own (Word's order, a Follow Path glyph's
      shadow on the glyph before it, isn't kept); an inner shadow after the text (`Drawn.getOver`);
    - a face's line gap above its ascent, as Word and `WordLineMetrics.wordBaselinePt` set it (phase 1 put it below).
    Gates: `gate_effects.py` on both rounds and phase 0's effect probes; b469 0 movers. Open (findings §4): Times New
    Roman's drawn reflection move, the text's 1/300 inch grid, the shape's own `a:effectLst` cast by its text
    (`render.effect.shape_text`), and a text box whose effects don't make it WordArt (D12), phase 5's.
- **Phase 4, VML WordArt** (D6).
  - First, round 2 (findings §11): Word's own shapetypes for types 137 to 175, and a VML probe of each.
  - `v:textpath` drawn from its shapetype's own `path`, `formulas` and `adj`, through a VML formula evaluator, and then
    by phases 1 to 3, with Office's `textpath` rules (§12). A shapetype with no path draws nothing, as in Word. Text
    watermarks are drawn as filled outlines.
  - **Gate:** the probes and real3's document (6 `v:textpath`) within tolerance (revised by the design below: real3's
    never print, so that document checks that docx4j draws none of them either).
  - **Designed** 2026-11-13 (Jason: "start phase 4", "write up a design for phase 4"; `CR-083-notes/phase4-findings.md`),
    D16 to D19 decided as recommended (Jason: "decisions 16 to 19 as recommended"). Reviewed the same day by a Claude
    Fable 5.1 session (Jason: "commit then have fable review the design"), its 13 findings applied (Jason: "apply all
    13 to the CR and the findings note, with finding 9 as a revision to D17"; findings §4). What is known (findings
    §1): Word draws VML WordArt as glyph paths with no text in its PDF, visible or invisible, watermarks included, and
    fills every path even-odd, DrawingML WordArt's too; it stretches the text to the shape's box before warping it,
    the ink box with `trim` (wv20-42's ink is the box to 0.04 pt) and a font-metric box without (wv30-02's ink 76.9 of
    117 pt tall), where phase 2's envelope keeps the natural width; a `v:textpath` is merged with its shapetype's
    attribute by attribute (`fitshape` on the shapetype, `string` and `style` on the shape); phase 0's "first
    shapetype of an `o:spt` wins" was a probe fault (an unresolved `type`); real3's three VML WordArt are first-page
    headers of sections with no `w:titlePg` and never print, so that document is the gate's negative check and the
    probes are the oracle; `WordArtRenderer.draw` takes a wps shape at every step; nothing in FO reads a VML `rotation`
    or `flip`. The design:
  - **The round first** (findings §2): Word's own 40 shapetypes, its drawing of each, and a watermark its own command
    makes (`cr083-phase4a`, D16: a word-bridge script, harvested by `o:spt`), then about 285 probes in seven files
    (`cr083-phase4`): each type at its default and another adj, one and two lines; the geometry's inheritance and ids,
    custom arcs and formulas; `textpath`'s attributes and style, and descender-only and capitals-only strings under
    `trim` on and off in three faces; paint, shadow and 3-D; placement, turned and flipped, in a group and a
    `w:object`; watermarks. Findings §3 lists what it answers, first the stretch and whether the warp after it is phase
    2's.
  - **Model** (docx4j-core):
    - `VmlShapes` (new): a shape resolved against its shapetype, by its `type`'s id in the shapetypes of the shape's
      part (the main document, each header and footer, the notes), groups included, an unresolved `type` drawing
      nothing (a duplicated id by Word's rule, from the round): `coordsize`, `coordorigin`, `adj` (an empty entry the
      shapetype's), `path`, `v:formulas` and `v:path`, and `v:textpath`, `v:fill`, `v:stroke`, `v:shadow` and
      `o:extrusion` merged attribute by attribute, the shape's winning; the placement from the shape's own style (the
      properties Part 4 says a shape doesn't take from its shapetype);
    - `WordArtText.ofVml`: the model phases 1 to 3 draw, from it: a paragraph for each line of `string`, a run each
      with the style's font, size, weight, slant, kerning (`v-text-kern`) and spacing; `v:fill` and `v:stroke` as
      DrawingML (`a:solidFill`, `a:gradFill`, `a:noFill`, `a:ln`, `opacity` as alpha; Word's default stroke where the
      shape states none, legacy WordArt being outlined by default) and `v:shadow` as an `a:effectLst`, its lengths in
      points, as far as D19 goes; beside them the text path's fit (`fitshape`, `fitpath`, `trim`, `xscale`,
      `v-text-align` with Office's default of center, `v-same-letter-heights`, `v-rotate-letters`). `WordArtText` no
      longer requires a wps shape;
    - VML colours read once, `PictureRecolour.vmlColour` moved to a shared reader (named and system colours,
      `#RRGGBB [n]`, and the `fill darken(n)` forms).
  - **Geometry** (docx4j-chart-render, `VmlGeometry`, new): the formulas (Part 4 §19.1.2.4's 18 operations, with Word's
    `prod` for the standard's `product`, integer results rounded as the standard says, the named values) and the path
    (§19.1.2.14's commands), evaluated in `coordsize` and mapped to the shape's box; a group's coordinates likewise
    (D18). It states what the standard leaves open (findings §1.6): `ae`/`al` angles in fd and sizes as semi-axes, the
    sweep's sign in a y-down space, `qx`/`qy` alternating, a repeated parameter list repeating its command, an omitted
    value 0, the delimiters, a forward `@n`, a `#n` past the adj list, division by zero, `if` at 0, overflow,
    `coordsize` 0 or absent, and values for `pixelWidth`, `pixelHeight`, `pixelLineWidth` and `lineDrawn`; the round's
    arc and formula probes check the ones Word's PDF can show. Its result is the `e`-delimited sets, each with its
    `nf`/`ns` flags, and the text box rectangle, so that CR-080 D6's later CR (VML shapes) reuses it. Bounded: 128
    formulas, as the standard has, and caps on a path's tokens and sub-paths (each sub-path of a text path is a band).
    A formula or path that can't be read draws nothing and is reported.
  - **Drawing** (`WordArtRenderer`):
    - a geometry source (review, finding 1): the warp's paths, the box and its drawing, the text rectangle, the turn
      and the fit, behind one interface, with a DrawingML source (today's `draw`, moved: `bodyPr` insets, `spPr`,
      `a:xfrm`, `prstTxWarp`) and a VML one (the text path's sub-paths, the shape's whole box with no insets, its
      style's `rotation` and `flip`, the textpath's fit); `WordArtWarps.warp` unchanged, going by the paths' count (one
      Follow Path, two an envelope, more bands), the `textCircle` flag given by the source (for VML, a closed single
      path);
    - the stretch, the step DrawingML hasn't: the lines laid out, then their ink box (`trim`) or the metric box the
      round finds (no `trim`) mapped onto the box, then the warp; Follow Path by `fitpath` as the round finds it;
    - no path, nothing, as Word; then phases 1 and 3's paint and effects, a `v:shadow` at its stated lengths (VML
      WordArt is always on a path, where phase 3 found Word draws effects as stated); the glyph paths written with
      `fill-rule="evenodd"`, as Word fills them, on DrawingML WordArt too (a change only where a glyph overlaps
      itself);
    - text: none in a header or footer, invisible elsewhere (D17, D2).
  - **Routes**: the visitor's `w:pict` and `w:object` branches, and the XSLTs' `w:pict` templates (their TODO), send a
    shape with an `on` text path (or a group holding one, D18) to the WordArt route ahead of the picture route. FO
    places it with the VML picture container (`XsltFOFunctions.anchorVmlPicture`,
    `FOTextBoxes.createVmlPictureContainer`: page, margin, centred), an inline one in its line: the container keeps the
    unturned box, which Word places, and the SVG goes at the drawing's bounds inside it, overflow visible, as
    `FOShapes.addWordArt` does. FOP has no z-order: a header's watermark comes out under the body's text because FOP
    paints region-before first, but a header's shape in front of the text is under it too, and a body shape behind the
    text paints in document order; a limitation, stated. HTML writes the same SVG in its line, where it writes a VML
    picture (it positions none), and nothing from a header or footer (D17).
  - **Reports**: `render.wordart.fill` (a picture fill), `render.wordart.outline` (a compound line),
    `render.wordart.shadow` (an emboss shadow, or one the round doesn't explain), `render.wordart.flat_3d`
    (`o:extrusion`, drawn flat as D10 has it), `render.wordart.bad_geometry`, `render.wordart.over_limit`; a group's
    other shapes, and a turned or flipped group, `render.shape.vml` (D18). A shapetype with no path isn't reported: Word
    draws nothing either.
  - **Gate:**
    - the round's probes through docx4j's PDF with Word's fonts (`gate_vml.py`, from `gate_wordart.py`): glyph outlines
      against Word's (two-path warps within phase 2's 0.5 pt, Follow Path to phase 2's median), colour and alpha
      exact, a shadow's offset within 0.1 pt, placement within 0.5 pt, the watermark pages by pixel;
    - real3's document: none of its three WordArt drawn, as in Word; the fidelity corpora otherwise unmoved (but where a
      DrawingML WordArt glyph overlaps itself, if any), events equal but for the new codes;
    - tests: `VmlGeometryTest` (each operation's rounding, the standard's examples and the open points docx4j states,
      Word's 40 shapetypes against the round's paths), `WordArtRendererTest` (a VML WordArt, a watermark, a group, both
      geometry sources), `WordArtFoTest` (a watermark in a header, turned, behind the text, with no text in the PDF; a
      VML WordArt in the body with invisible text), and HTML's.
  - **Built in part** 2026-11-13, before the round (Jason: "commit then start implementation"; findings §5): the
    model, geometry, drawing and routes as designed but for these:
    - `VmlGeometry` in docx4j-core beside CR-080's `ShapeGeometry`, returning its `ShapeOutlines`;
    - the geometry source as a second entry, `WordArtRenderer.drawVml`, sharing the layout, warp, paint and effects
      with three hooks (a fitted run takes no room; a two-path warp's frame down; the style's turn), so DrawingML's
      `draw` is unchanged;
    - `fill-rule="evenodd"` left until wv44: a run's glyphs are one path, where even-odd would cut holes between
      overlapping glyphs.
    Phase 0's VML probes through docx4j: nothing where Word draws nothing; wv20-42's slanted path within 0.04 pt of
    Word's ink; the horizontal watermark within 0.1 pt; the turned watermarks within 0.7 pt once a turned drawing's ink
    is centred down its box, as Word's are (findings §6). Word's own 40 shapetypes (wv40, D16 (a)): all 40 within
    1.7 pt of Word's ink (39 within 0.7, median 0.1), the arcs' direction confirmed, a shadow with no `on` off; Follow
    Path (an odd number of text paths, whatever `fitshape` says) lays the text along the whole path, a line 0.22 em
    above the baseline on it (findings §6). Provisional until the round: the no-`trim` em box and Follow Path's rise
    (one face each), gradients, and the rest findings §5 lists. Tests: core-tests
    2,676, chart-render 103, export-fo 111, export-fo-tests 971 pass. Gates b471, b472 and b473 (the last build)
    against b464: 0 movers,
    events equal, the corpora's PDFs as b470's but for the two time fields (5361, 11875; real3's VML WordArt document
    the same: its three still not drawn, as in Word), the probes' 324 the same.
  - **The round** (cr083-phase4, 263 probes, `build_vml.py`, all silent; findings §7; Jason: "build the cr083-phase4
    round with build_vml.py", "go ahead with reading each of these ..., fixing them, writing findings §7, then a
    gate"): within 0.7 pt of Word's ink 182 probes, then 223; past 3 pt 39, then 10. Word's rules, now docx4j's: a
    default `coordsize` of 21600; `prod` by 0 its first argument; an adj held to its handle's range; `on` and
    `textpathok` not stopping it; `nf`/`ns` reaching the text; `fitshape="f"` along the first path; without `trim` the
    advance across and an em box down, its foot the face's larger descent (three faces within 0.2 pt); Follow Path's
    line that box's middle, several lines at one scale and moved apart along the normal; a VML shadow a copy of the
    glyphs; a turn then the mirror; turned ink centred down its box only where the shape is centred. Open: a watermark
    on unequal margins (12.4 pt), the pour types' two lines, two shapetypes of one id, `qb`, the style on a shapetype
    only, `v-same-letter-heights`, and the reported gaps (3-D, emboss, a compound line, a tile picture, a turned group).
    Gate b475 0 movers.
  - **Reviewed** 2026-11-13 by a Claude Fable 5.1 session (Jason: "commit then have fable conduct a review including a
    look at those unexplained misses"; findings §8), its findings applied (Jason: "apply them"): the shapetype cache per
    conversion (a static one held every converted document); two shapetypes of one id, the last; gradients' angle in
    the box's square (`scaled="1"`); `v-same-letter-heights`; the pour types' bands at one scale; the no-`trim` box on
    bands; a builder bug that had confounded two probes. 226 of 263 within 0.7 pt; gate b476 0 movers. Not applied: a
    watermark centred on the margin under a header that overflows it (the placement is written before the header's
    height is known). A follow-up round, cr083-phase4b (17 probes, Jason: "can we do a probe for qb?"), settles `qb`
    and the rules the review found rest on one or two probes.
  - **Settled** 2026-11-13 by cr083-phase4b (findings §9): `qb` anywhere in the path, Word draws nothing; a shape's
    shapetype is the last of its id before it in the part, one after it not counting; `v-same-letter-heights` every
    glyph the box's height, mapped across by its advance; a turned shape aligned down its reference aligned by its
    ink (bottom as well as centre). 12 of the 17 within 0.7 pt, one at 0.8, the four blank in both; the round 227 of
    263 within 0.7.
  - **Gradients and emboss** 2026-11-13 (findings §10; Jason: "do as fable recommends", on a Fable triage of the open
    items): a VML gradient runs from `color2` at its focus to `color` at its ends (docx4j had no focus and 100%
    backwards), a `colors` list read, the blend Word's curve in gamma-2.2 light unless the method is `none`; an emboss
    a double shadow, light above and to the left. The round 228 of 263 within 0.7 pt; gate b478 0 movers. Left: the
    watermark under an overflowing header (wv47-14: later, with the fork), Circle on two lines, o:spt 156's second
    adj, a radial gradient's rectangle, the reported gaps.
  - **Closed** 2026-11-13 (Jason: "close phase 4"). The watermark under an overflowing header is phase 4b. The rest
    go to phase 7's gap report with findings §10's numbers: Circle on two lines (wv41-11, 6.6 pt), o:spt 156's second
    adj (wv42-26, 4.9), a radial gradient drawn as a circle where Word's rings are rectangles, a perspective shadow,
    `o:extrusion` drawn flat, a compound line drawn single, a tile picture drawn in its colour, a turned group drawn
    unturned.
- **Phase 4b, a VML shape centred on the margin under a header that overflows it** (split from phase 4, 2026-11-13;
  findings §8 finding 5, §10).
  - Word centres such a shape (wv47-14, a watermark with `mso-position-vertical:center` relative to the margin, top
    margin 36 pt under a header that runs past it) between the body's top, where the header ends, and the bottom
    margin: its ink centre 397.3 = (60.6 + 733.9) / 2, where docx4j's is 12.4 pt off. With the header inside the
    margin (wv50-03, -04) docx4j is within 0.2.
  - docx4j writes the placement into the FO before the header's height is known; the FOP fork learns it as it lays the
    page out. So the fix is an FO hint the fork resolves against the body region, perhaps shared with DrawingML
    anchors relative to the margin (unverified): a design first, with the fork's session, then a fork release.
  - For a Claude Fable 5.1 session (its triage, findings §10: it crosses the FO writer and the fork).
  - **Gate:** wv47-14 within 0.7 pt; a fidelity gate.
- **Phase 5, WordArt effects on body text** (D3; with the FO renderer fork).
  - The text stays FO text in docx4j's layout. docx4j writes a run's `w14:textOutline`, `w14:textFill`,
    `w14:shadow`, `w14:glow` and `w14:reflection` as extension attributes on its `fo:inline`, and the fork's PDF
    painter draws them:
    - outlines with PDF text rendering mode 2 (fill, then stroke), in the outline's width, colour, dash and join;
    - gradient fills with mode 7 (the glyphs as a clip) and a shading;
    - shadow, glow and reflection as pictures under the line, from the glyphs' positions in the area tree, with CR-081's
      effect renderer.
  - The fork already has the modes: `PDFTextUtil` has `TR_CLIP = 7` and `setTextRenderingMode(int)`. So the fork's
    CR is scoped to the `fo:inline` extension attributes and the painter, not to the PDF text utility.
  - Word's own PDF doesn't use text rendering modes: it draws an outlined or gradient body run as glyph paths over
    invisible text, as on WordArt (phase 0 findings §1). The fork's modes 2 and 7 look the same and keep the text
    real, searchable and tagged: a known departure from Word's mechanism, not from its appearance (D3).
  - HTML: a body run's outline as CSS `-webkit-text-stroke`, and a gradient fill as `background-clip: text`. Shadow
    and glow as `text-shadow` where CSS can carry them; anything CSS can't is reported (D9).
  - First, before the fork release: a solid `w14:textFill` as the text's colour, docx4j-side only, in PDF and HTML.
  - **Gate:** the body-text probes within tolerance; a fidelity gate (layout unmoved, since only painting changes).
  - **Revised by phase 3's design** (2026-11-13; phase 3 findings §1, D13): Word gives a body run's shadow, glow or
    reflection room in its line, as in WordArt (wb50: a glow of 18 pt sets its line 8.16 pt lower and the next
    paragraph 20.9; a reflection the next paragraph up to 21.4), so this phase changes layout as well as painting:
    docx4j gives the run's line the room, from phase 3's round (wb70), and the fidelity gate's movers are read as that.
    Measured (phase 3 findings §4, wb70): the room is WordArt's rule, `TextEffects.reach`, for the line alone and for a
    run within it.
  - **Designed 2026-11-14** (phase 5 findings; D20 to D24 proposed). The bullets above are D3's first plan; where they
    differ, this design holds.
    - A round first, cr083-phase5 (about 70 probes, phase 5 findings §3): what body text adds to phases 0 and 3 (a run
      broken across lines, neighbouring runs' pictures, decorations, the room at body sizes and under each spacing
      rule, in cells and headers, effects from styles), and whether a shape's gradient is blended by the same curve.
    - docx4j-side, before the fork's release (step 1): a solid `w14:textFill` as the text's colour, over `w:color`, its
      `w14:alpha` as the colour's alpha (FOP writes an `ExtGState` for 1 to 254); HTML's CSS (D23); a DrawingML gradient
      of two stops at 0 and 100% blended by Word's curve wherever docx4j paints one, any other evenly (D24).
    - The run's effect on its span (D21): docx4j writes `docx4j:text-effect` (an id) on the run's `fo:inline`, in both
      FO routes, from a new run property the property factory makes from the effective `w:rPr`, and keeps the run's
      effects by that id for the render; and `docx4j:effect-room` (above and below, in points, from
      `TextEffects.reach` at the run's size and metrics) where the run has a shadow, glow or reflection (D22). Both
      only where the renderer advertises a capability of its own, `text-effects` (as `word-layout` is advertised
      today), so that this docx4j writes nothing for a fork without it.
    - The fork (its CR, drafted by its session from this design): the room added to the span's ascent and descent in
      `WordLineLayoutManager.wordLine` (D22); the effect id copied from the innermost inline onto the run's `TextArea`
      (`TextLayoutManager`, into the area's foreign attributes) and handed to the painter by `IFRenderer.renderText`;
      a text-effect painter, found through the render (the `FOUserAgent`), not set on the factory, which a caller may
      share across conversions and threads; called for each text area with an id, under its text (after its
      background) and over it, with its glyph ids, their positions as the fork sets them (dx, dp, letter and word
      spacing, the hyphen), the font, size, any simulated style, and the span's box; it returns an SVG document and
      its rectangle, which the fork draws with `IFPainter.drawImage(Document, Rectangle)` as an artifact, not a
      Figure; and the text drawn in mode 3 where the painter says it drew the glyphs (PDF inside the text object
      `drawText` opens; Java2D, the fill skipped; the IF serializer, an attribute on `<text>`) (D20).
    - docx4j's painter (step 2, with the fork's release): a third entry in `WordArtRenderer` beside `draw` and
      `drawVml`, taking a run's glyphs as placed (by glyph id, from D4's same font file; a font whose ids aren't the
      file's reported, not drawn) and drawing them as phases 1 to 3 draw a flat run, in Word's order (phase 5
      findings §1.1): the pictures (`Effects.drawText`, at (size / 72)^0.7), then, where the run has an outline, a
      gradient or a transparent fill, its glyph paths filled and stroked (`Fills`, the outline as phase 1's); an inner
      shadow over the text. The gradient's frame is the run's advance on its line and the fork's span box down; a run
      split into several text areas on a line takes the enclosing inline's rectangle or their union, as the round
      finds Word does.
    - An option, decided by the gate's size numbers: PDF text rendering mode 2 for the common case, a solid outline
      over a solid fill, with no glyph paths (D20).
    - Reports: a run's effects not drawn as Word draws them (`render.wordart.body_effect`, D9); none where the renderer
      lacks the capability (the effects are then dropped as today, the solid fill kept).
    - **Gate:** the round's probes within phase 3's tolerances (the box 0.05 pt, the pictures as phase 3's); a fidelity
      gate whose movers are among the corpora's 12 documents with effects (phase 5 findings §1.4), each read line by
      line; their PDF size and render time against b478, any past a threshold reported.
  - **Begun 2026-11-14** (Jason: "commence phase 5"; findings §5).
    - The round `cr083-phase5` (88 probes in four files, `build_body.py`, `probes-body.tsv`): staged, verified and
      announced by the portfolio (to-vm 288), its results awaited.
    - The fork's CR: fop/CR-043, drafted by its session from §10 (design only; its build Jason's to order). Its four
      corrections to findings §2 and its answers are findings §5.2; the painter is a JDK `BiFunction` on the
      `FOUserAgent`, set by reflection as `setWordLayoutFontMetrics` is; four days, not three. Registry: CR-083.5
      depends on fop/CR-043 (2a23d82). Enterprise CR-001 §6.6 item 70 records the limitation (85031ab).
    - Step 1 built: `TextFill` (a solid fill the run's colour over `w:color`, its alpha as "#RRGGBBAA", none left as
      01; the paragraph mark's for its number), `TextOutline` and `TextShadow` (HTML only, D23), from
      `PropertyFactory.createProperties`; `GradientCurve` (the curve, shared with `VmlWordArt`) and
      `Fills.wordGradient` (D24, WordArt's runs, and shapes' fills once the round showed Word blends them alike); on
      flat WordArt a gradient spans the run's frame (`Drawing.Area.setGradientFrame`), found when D24 was measured.
      Gates b479, b480 and b481: 0 movers; findings §5.1, §6.4.
    - The round read the same day (findings §6): a run's effects drawn line by line, its frame the run's part on a
      line in one font (spanning a tab, soft hyphens, a line-end hyphen and trailing spaces; split by a font
      fallback), down the line box; decorations painted as the glyphs; a right-to-left run's gradient reversed; the
      room phase 3's rule at body sizes, auto's factor applied to it, exact none, atLeast absorbing it but moving the
      baseline up by the room below, a superscript's at its stated size unraised; D24's condition confirmed, and a
      shape's two-stop gradient on the curve too. The fork told what changes for fop/CR-043 (the painter once per
      run, font and line; decorations in its first build; the direction; the atLeast baseline).
- **Phase 6, 3-D WordArt** (D10; needs CR-081.5).
  - `w14:props3d` and `w14:scene3d` mapped to DrawingML's `sp3d` and `scene3d`, and drawn with CR-081's 3-D.
    Until then, phases 1 to 3 draw such text flat, with its fill, outline and effects, and report it (D9).
  - **Gate:** the 3-D probes within the tolerance CR-081.5 sets.
- **Phase 7, the gap report** (D9).
  - Every WordArt in the probes and a load-survey sample (through the portfolio) is rendered. Each one not drawn as
    Word draws it is listed with its reason.
  - **Gate:** none undrawn among the probes; the rest listed with their counts in the record of the phase.
- **Phase 8, CHANGELOG, Getting Started.**

## 7. Risks

- **Word's mapping isn't the obvious one.** Word's warp may map by the path's parameter rather than by arc length, may
  stretch per line, and may treat `normAutofit` differently from a plain text box. Phase 0 decides before phase 2
  starts. The tolerance per preset comes from Word against itself. (Phase 0: by arc length, as §20.1.9.19 has it, and
  `normAutofit` with text that fits is `noAutofit`.)
- **Fonts.** A glyph outline needs the font file. A font FOP substitutes is drawn in the substitute, as the rest of
  the PDF is. Cloud fonts Word has and this machine doesn't move the comparison: the probes use installed fonts.
- **Searchability.** Outlines alone lose the text; invisible text restores search and copy but not tagging. The
  tagged-PDF work, if any, has to place it in the structure. Noted, not solved here.
- **Line breaking.** WordArt text is laid out by this CR's line layout, not by FOP. A long WordArt that Word wraps
  inside a narrow box may break differently. Phase 0's two-line probes measure it.

## 8. Decisions (owner: Jason; recommendations)

- **D1. Glyph outlines warped in Java, drawn as SVG paths**: no PDF-level text warping exists, and FOP lays text out
  only flat. Recommendation: yes.
  **Decided 2026-11-12** (Jason: "D1, D4, D7, D8, D9, D11 as recommended"): yes.
- **D2. Invisible text for search and copy**, unless Word's PDF keeps the text. Recommendation: do what Word does,
  measured in phase 0; invisible text if Word writes outlines.
  **Decided 2026-11-12, from phase 0** (findings §1, §10): glyph outlines with invisible text exactly where Word uses
  them (a warp, any outline, a gradient fill, a fill with transparency), and real text elsewhere (a solid fill; text
  under a shadow, glow or reflection picture). Where Word's PDF has no text at all (3-D WordArt, a fill with
  transparency), docx4j keeps it, invisibly: search and copy keep working, and nothing visible changes. Word makes its
  text invisible with transparency (`ca 0`); docx4j uses text rendering mode 3 (`fill="none"`, §5.5), the same for
  search and copy and simpler in FOP.
- **D3. WordArt effects on body text** (phase 5).
  - A text box whose runs carry `w14:textOutline` or a gradient `w14:textFill` is drawn by this CR's code with no warp
    (phase 1).
  - The same effects on runs in body paragraphs (Word's WordArt gallery applied to ordinary text) are drawn by the FO
    renderer fork, from extension attributes docx4j writes. The text stays text, in docx4j's layout, searchable and
    tagged. PDF draws stroked text and text used as a clip natively (text rendering modes 2 and 7). The shadow, glow
    and reflection pictures are placed from the area tree.
  - A solid `w14:textFill` becomes the text's colour first, docx4j-side only.

  Recommendation: yes. The earlier draft left body text out, which is a gap. The fork's side is a CR in
  `../xmlgraphics-fop-plutext`, drafted by that session (§10); this phase depends on it.
  **Decided 2026-11-12, from phase 0** (findings §1, §10): Word doesn't use PDF text rendering modes for body text. An
  outlined or gradient body run is glyph paths over invisible text, as on WordArt, and a shadow, glow or reflection is
  a picture under real text. The fork's route stays: modes 2 and 7 look the same and keep the text real. It is a known
  departure from Word's mechanism, not from its appearance.
  **Revised 2026-11-14 by D20** (phase 5's design and its review): docx4j paints the glyph paths and pictures through
  a callback the fork calls as it draws the run, over the text drawn invisibly (mode 3), which is Word's own
  mechanism; modes 2 and 7 are no longer the route (mode 2 kept as an option for a solid outline over a solid fill).
- **D4. The same font file FOP embeds**, loaded into `java.awt.Font` through `PhysicalFonts`, not AWT's lookup by family
  name. Recommendation: yes.
  **Decided 2026-11-12** (Jason: "D1, D4, D7, D8, D9, D11 as recommended"): yes.
- **D5. Write the warp from ISO/IEC 29500-1 §20.1.9.19**, which describes it step by step. docxide-pdf's outline
  deformation and office-open-xml-viewer's arc-length and Follow Path rules are cross-checks and the hypotheses for
  the presets the standard doesn't cover. Code is ported from them only where that saves work, with acknowledgement
  (Apache-2.0, MIT). Java2D's `GlyphVector.getOutline` and `PathIterator` give the outlines that docxide-pdf reads
  with `ttf_parser`, so little of its code is needed. Recommendation: yes. This replaces the earlier draft's "port
  docxide-pdf's algorithms", written before §20.1.9.19 was read.
  **Confirmed 2026-11-12 by phase 0** (findings §3, §10): §20.1.9.19 is Word's method, read on the text rectangle with
  the ink of the whole block of lines, by arc length along each path and down the straight joining line, the outline
  warped densely: 157 of 198 two-path probes within 0.5 pt of Word's. Word departs from the shipped definitions in
  `textDeflate` and `textDeflateBottom`, which take `ss` as the height. Both are written into phase 2, and the 11
  presets not yet within 0.5 pt are phase 2's to resolve against these probes.
- **D6. VML WordArt in this CR** (phase 4), drawn from its `v:shapetype`'s own `path`, `formulas` and `adj`, as Word
  draws it. **Rewritten 2026-11-12, from phase 0** (findings §8, §10); it had mapped `o:spt` to a preset through a
  table from [MS-ODRAW] §2.4.24.
  - Word draws VML WordArt by its shapetype's path, not its `o:spt`: a shapetype carrying only its `o:spt` draws
    nothing, and a type-136 shapetype with a slanted path draws the text slanted.
  - So docx4j evaluates the shapetype's path and formulas with a VML formula evaluator (phase 4), and needs no `o:spt`
    table to draw. A shapetype with no path draws nothing, as in Word.
  - Word writes no VML WordArt from DrawingML (its Word 2007 mode save turns WordArt into a plain text box), so the
    earlier oracle, Word's own conversion, doesn't exist. The oracle is Word's PDF of VML probes; round 2 needs Word's
    own shapetypes for types 137 to 175 (findings §11).
  - Whatever the count: one real3 document has six, and Word's text watermark is VML WordArt. (Phase 4's design found
    that real3's three never print: first-page headers of sections with no `w:titlePg`; phase 4 findings §1.4.)
- **D7. A WordArt's text in the text model** stays as it is: the text box's text (CR-046), unaffected by the warp.
  Recommendation: yes.
  **Decided 2026-11-12** (Jason: "D1, D4, D7, D8, D9, D11 as recommended"): yes.
- **D8. Where the code lives**: with CR-080's drawing (docx4j-chart-render), the glyph layout beside chart-render's
  `TextWrap`. The SVG serves the FO and HTML outputs alike, as charts' does. Recommendation: yes.
  **Decided 2026-11-12** (Jason: "D1, D4, D7, D8, D9, D11 as recommended"): yes.
- **D9. Gaps are reported** with stable codes (`render.wordart.unknown_preset`, `render.wordart.flat_3d`,
  `render.wordart.css_effect` and so on), once per drawing, through CR-080 D9's two mechanisms: the message writer and
  the gate's event lines. Recommendation: yes.
  **Decided 2026-11-12** (Jason: "D1, D4, D7, D8, D9, D11 as recommended"): yes.
- **D10. 3-D WordArt** (`w14:props3d`, `w14:scene3d`): drawn flat with its fill, outline and effects from phase 1, so
  it is never missing. Its bevel, depth and rotation come in phase 6, with CR-081's 3-D renderer (CR-081 phase 5).
  Recommendation: yes.
  **Decided 2026-11-12, from phase 0** (findings §7, §10): Word draws 3-D WordArt as one picture of the whole text, at
  200 dpi, and drops the text from its PDF. docx4j keeps the text there, invisibly: better than Word for search and
  copy, and nothing visible changes.

- **D11. WordArt in headers and footers** is drawn as in the body: the same code path, as CR-081 D8 and CR-082's
  phase 3 have it. Recommendation: yes.
  **Decided 2026-11-12** (Jason: "D1, D4, D7, D8, D9, D11 as recommended"): yes.

Proposed with phase 3's design, 2026-11-13 (phase 3 findings §1), and decided the same day as recommended:

- **D12. A run's shadow, glow or reflection alone doesn't make a text box WordArt.** §5.1 marks WordArt by a warp, by
  Word's own form (`textNoShape` with `wrap="none"`, which every WordArt Word inserts has, its gallery styles
  included), or by a run Word draws as outlines. A text box with none of these but a run with a shadow (Word's text
  effects applied to the text in a shape) stays FO text: this CR's line layout isn't a paragraph layout engine, and
  such a box can hold anything. Its run effects are phase 5's, through the fork, and are reported until then.
  Recommendation: yes.
  **Decided 2026-11-13** (Jason: "decisions 12 to 15 as recommended"): yes.
- **D13. The room effects take.** In WordArt (phase 3): Word's rule, from the round. If the round finds none that fits
  every probe, the best fit, its residual stated in the findings, as Follow Path's 0.06 em regime was. In body text
  (phase 5): the room changes docx4j's layout, which phase 5 assumed it wouldn't; it gives the run's line the room
  docx4j-side, from the round's body probes. Recommendation: yes, both.
  **Decided 2026-11-13** (Jason: "decisions 12 to 15 as recommended"): yes, both.
- **D14. Follow Path: a picture for each glyph**, as Word draws it: each glyph's effects drawn with the glyph, in turn,
  so that a glyph's shadow falls on the glyph before it, and its reflection mirrored about the glyph. One picture of the
  whole path's text would be fewer pictures but not Word's look: its reflection one mirrored arch, where Word's is a
  row of mirrored letters. Recommendation: as Word.
  **Decided 2026-11-13** (Jason: "decisions 12 to 15 as recommended"): as Word.
  **Its rationale corrected 2026-11-13** by the design's review (findings §1, §3 item 4; Jason: "yes, apply those
  changes"): Word's per-glyph reflection isn't mirrored about the glyph. Each glyph's picture is a cut of one
  reflection of the whole block, flipped about the block's foot and faded over the block, so Word's look *is* one
  mirrored arch, written glyph by glyph (wa31-06; the glyphs at the arch's top get empty pictures). The decision stands
  for the drawing order, a picture for each glyph in turn; the effects' geometry is the whole block's.
- **D15. The inner shadow.** w14 has no inner shadow. Word's gallery writes one as `w14:shadow` with `sx="0" sy="0"
  algn="none"` (style 15), and Word draws it inside the letters, over the text. docx4j reads it as `a:innerShdw` (its
  blur, distance, direction and colour) in the forms the round shows Word reads as inner, and draws any other
  shadow scaled to 0 as Word does (nothing, if that is what Word draws). Recommendation: as Word measures.
  **Decided 2026-11-13** (Jason: "decisions 12 to 15 as recommended"): as Word measures.

Proposed with phase 4's design, 2026-11-13 (phase 4 findings §1), and decided the same day as recommended:

- **D16. Word's own shapetypes, the round's input.** No machine here has Word's definitions of types 137 to 175 (phase
  0 findings §11), and docx4j draws from them (D6). Three routes:
  - (a) a word-bridge script: Word makes the 40 legacy WordArt through its object model in a mode-12 document
    (`Shapes.AddTextEffect`, then each `TextEffect.PresetShape`) and saves docx and PDF. The bridge runs only scripts
    on its trusted list, so the VM session writes it and Jason installs it: minutes of his time, and a tool for later
    rounds;
  - (b) Jason inserts them by hand in a Word 2007-mode document (Insert, WordArt gives the legacy gallery; the 40 shapes
    by Change Shape): about half an hour;
  - (c) the load survey's query (e), the definitions seen identically in five or more documents: waits on the copy
    home.

  The definitions are kept in `CR-083-notes/shapetypes-word.xml` and as test fixtures: Microsoft's shape data as Word
  writes it into every such document, not any document's content. docx4j ships no table of them (D6).
  Recommendation: (a), falling back to (b) if Word's object model won't make legacy WordArt; (c), when it comes, as a
  check that Word's definitions are the same across its versions.
  **Decided 2026-11-13** (Jason: "decisions 16 to 19 as recommended"): (a), then (b) if need be; (c) as a check.
  The script, from the design's review (finding 6; Jason: "apply all 13 to the CR and the findings note, with finding
  9 as a revision to D17"): it also makes a watermark by Word's own command; the harvest goes by each shapetype's
  `o:spt`, `MsoPresetTextEffectShape`'s order not being `o:spt`'s; and if Word's save holds wps shapes, the script sets
  compatibility mode 12 and tries again (phase 4 findings §2).
- **D17. Watermarks: no text, and not in HTML.** By D2 docx4j keeps WordArt's text invisibly where Word's PDF has none,
  and Word's PDF has none for any VML WordArt (phase 4 findings §1.1). A watermark is on every page, so its text would
  be a search hit on every page and in every page's copied and extracted text, which Word's PDF doesn't have.
  Recommendation:
  - a watermark (VML WordArt whose shape id starts `PowerPlusWaterMarkObject`, as Word's Watermark dialog writes it:
    CR-050's test) is drawn with no text, as Word; other VML WordArt keeps D2's invisible text;
  - HTML doesn't write watermarks: it has no page to centre them on, and docx4j's HTML shows a header only in paged
    media.

  Alternatives: no text for any VML WordArt (as Word throughout); or text for watermarks too (D2 unchanged).
  **Decided 2026-11-13** (Jason: "decisions 16 to 19 as recommended"): a watermark has no text and isn't in HTML;
  other VML WordArt keeps D2's invisible text.
  **Revised 2026-11-13** by the design's review (finding 9; Jason: "apply all 13 to the CR and the findings note, with
  finding 9 as a revision to D17"): the test is the part, not the id. What argues for no text is that the text repeats
  on every page, which holds for any VML WordArt in a header or footer, and a renamed or hand-copied watermark lacks
  the `PowerPlusWaterMarkObject` id (other producers write it, a hand copy needn't). So VML WordArt in a header or
  footer part is drawn with no text and left out of HTML; VML WordArt elsewhere keeps D2's invisible text.
- **D18. VML groups.** A VML WordArt can be one shape of a `v:group`, as real3's three are (beside a picture, in a
  `w:object`), and docx4j draws no VML group today (CR-080 D6 left VML shapes to a later CR). Recommendation: phase 4
  draws a group's WordArt and its pictures (`v:imagedata`), each placed by the route it would take alone at its box
  mapped through the group's `coordsize`, `coordorigin` and its own `left`, `top`, `width` and `height`; a group's
  other shapes, and a turned or flipped group, are reported (`render.shape.vml`) until CR-080 D6's CR. And ask the
  load survey's (c) to count VML WordArt inside a `v:group` (only real3's are known, and Word doesn't print them).
  Alternative: groups wait for CR-080 D6's CR, their WordArt reported.
  **Decided 2026-11-13** (Jason: "decisions 16 to 19 as recommended"): a group's WordArt and pictures drawn, its other
  shapes reported; the survey asked.
  From the design's review (finding 11; Jason: "apply all 13 ..."): groups within groups mapped in turn; a group's
  children placed by their `left` and `top` through the group's `coordorigin` and `coordsize` (`createVmlContainer`
  reads only `margin-left` and `margin-top`); and a group's picture given its own entry to the picture route, which
  takes a `w:pict`, not a child `v:shape`.
- **D19. VML paint, shadow and 3-D.** VML paints a shape with `v:fill` (solid, gradient, gradientRadial, pattern, tile,
  frame; opacity), `v:stroke` (weight, colour, dash, join, cap, compound lines, opacity) and `v:shadow` (single,
  double, perspective, emboss), and adds 3-D with `o:extrusion`. Recommendation: solid and gradient fills, opacity,
  and every stroke but compound lines, as Word draws them on glyphs (the round), through phase 1's DrawingML paint;
  single, double and perspective shadows as Word draws them, through CR-081's effects (or as paths, if Word's are);
  picture fills, compound lines and emboss reported; `o:extrusion` drawn flat with its fill and shadow and reported
  `render.wordart.flat_3d`, as D10 has it, its 3-D with phase 6 if CR-081's renderer takes it.
  **Decided 2026-11-13** (Jason: "decisions 16 to 19 as recommended"): yes.
  From the design's review (findings 11, 12; Jason: "apply all 13 ..."): VML gradients' `angle` and `focus` (-50 to
  100, mirrored) mapped to DrawingML's as the round finds Word draws them; `#RRGGBB [n]` and `fill darken(n)` colours
  read; legacy WordArt's default stroke drawn (the probes copy Word's insert, which states it); a shadow's offset as a
  percentage read; a `v:shadow`'s lengths in points, not phase 3's (size / 72)^0.7, checked on two box sizes.

Proposed with phase 5's design, 2026-11-14 (phase 5 findings §1, §2):

- **D20. Who paints a body run's effects: docx4j, through a callback the fork calls** (revises D3's modes). D3 had the
  fork's PDF painter draw an outline in text rendering mode 2 and a gradient in mode 7 with a shading, and the effect
  pictures from the glyph positions with CR-081's renderer. Two things argue against it. The fork would need its own
  copy of what phases 1 to 3 measured (Word's outline widths and joins, gradients and their curve, compound and gradient
  outlines, transparent fills, the pictures' scale, frame and fade); and CR-081's renderer is docx4j's, which the fork
  can't call. Options: (a) D3 as written; (b) the fork calls a text-effect painter docx4j sets on `FopFactoryBuilder`,
  for each text area with an effect, under and over its text, and docx4j's painter draws the pictures and, where the
  run has an outline, a gradient or a transparent fill, the glyphs as paths over the text drawn in mode 3, which is
  Word's own mechanism (phase 0 findings §1; wb50: paths over the run's text at alpha 0); (c) (b) with real visible
  text under the paths. Recommendation: (b). The text stays real, searchable and tagged (invisible, as WordArt's is,
  D2); the paint is phases 1 to 3's code; the fork's change is generic (an id carried to the painter, a callback, mode
  3). The cost: visible body glyphs become paths where the run has an outline or gradient, as in Word's PDF.
  **Decided 2026-11-14** (Jason: "D20 as recommended"): (b). From the review (phase 5 findings §4, findings 2, 4 to 6,
  11): the painter returns an SVG document and its rectangle, drawn by the fork's `IFPainter.drawImage(Document,
  Rectangle)` as an artifact, so every output takes it; it draws from glyph ids at the fork's positions, with any
  simulated style; under and over are one moment where the text is invisible, two calls kept for an inner shadow;
  mode 2 kept as an option for a solid outline over a solid fill, if the gate's PDF sizes call for it. (c) is wrong:
  paths over visible text cover twice, changing alpha and edges.
- **D21. How the effect reaches the fork: an id.** Options: (a) the run's `w14` effect elements serialised into the
  attribute; (b) an id (`docx4j:text-effect="fx3"`) into a table the conversion keeps, which docx4j's painter reads.
  Recommendation: (b): the fork never parses `w14`, the FO stays small; the cost is that FO saved and rendered later
  outside docx4j loses the effects (reported where the painter finds no table).
  **Decided 2026-11-14** (Jason: "D21 as recommended"): (b). From the review (finding 8): the table is the render's,
  reached through the `FOUserAgent`, not the factory's.
- **D22. The room through the fork's Word line.** docx4j computes each run's reach (`TextEffects.reach`, phase 3) and
  writes it on the run's span (`docx4j:effect-room="above below"`); the fork's `WordLineLayoutManager.wordLine` adds it
  to the span's ascent and descent. Without Word mode no room is given. Recommendation: yes; the round says whether
  auto spacing's multiple applies to it and what exact and atLeast do.
  **Decided 2026-11-14 as the review has it** (Jason: "D22 and D24 as per fable review"; finding 9): the room added to
  `alt` and `pitch - alt` in `wordLine` before the maximum; exact spacing gives none (`wordLine` skips it); atLeast
  absorbs the room in any slack today and auto's multiple applies to it, both as the round finds Word does, the fork
  told which; `TextEffects.reach` needs the run's width only for a shadow skewed vertically (`ky`), whose room docx4j
  takes at the run's whole advance.
- **D23. HTML: CSS as far as it goes.** A solid fill as `color` (with its alpha); an outline as
  `-webkit-text-stroke`; a gradient as `background-clip: text` with the curve's stops; a shadow and a glow as
  `text-shadow` at Word's drawn sizes; a reflection, an inner shadow and a compound or gradient outline reported (D9);
  no room. Recommendation: yes.
  **Decided 2026-11-14** (Jason: "D23 as recommended"): yes; the curve's stops for a two-stop gradient only (D24).
- **D24. DrawingML gradients blended by Word's curve.** Word blends a body run's DrawingML gradient as it blends a VML
  one (phase 5 findings §1.2): in gamma-2.2 light along an S-curve. docx4j blends WordArt's DrawingML gradients evenly
  (phases 1 to 3). Recommendation: blend WordArt's and body text's by the curve now (`VmlWordArt.SIGMA`, shared), and
  shapes' (CR-080, CR-081) if the round's shape probe shows Word does there too.
  **Decided 2026-11-14 as the review has it** (Jason: "D22 and D24 as per fable review"; finding 1): the curve for a
  gradient of two stops at 0 and 100% only; any other (more stops, or stops inside the run) blended evenly in sRGB
  between its stops and held at its end colours outside, as Word does (wb50-15, wg20-06 to -08), and as docx4j does
  today. Shapes' after the round's three shape probes. A VML `colors` list with the default method keeps phase 4's
  curve between its colours (unmeasured).

## 9. Out of scope

`w14:ligatures`, `w14:numForm`, `w14:numSpacing`, `w14:stylisticSets` and `w14:cntxtAlts`, which are OpenType
features, not effects; pptx text warps (pptx4j could use the same code later); PowerPoint's transform on autofit text.

## 10. Hand-offs

- The portfolio: the registry entry (depends on CR-080.1; phase 3 on CR-081.1; phase 5 on the fork's CR; phase 6 on
  CR-081.5).
- The FO renderer fork's session (`../xmlgraphics-fop-plutext`): a CR for text effects in the PDF painter, drafted by
  that session from phase 0's numbers and the extension attributes' names (D3):
  - stroked text, text as a clip with a shading;
  - effect pictures from glyph positions.

  Revised by phase 5's design (D20 to D22, decided 2026-11-14): the capability `text-effects`; the run's
  `docx4j:effect-room` added to its span's ascent and descent in the Word line (exact none; atLeast and auto as the
  round finds); its `docx4j:text-effect` id copied onto the text area and handed to the painter; a text-effect painter
  found through the `FOUserAgent`, called under and over each such text area with its glyph ids, positions, font, size,
  simulated style and span box, returning an SVG document drawn by `IFPainter.drawImage(Document, Rectangle)` as an
  artifact; mode 3 for text the painter draws over, in each painter and the IF serializer.

  Drafted 2026-11-14 by that session as fop/CR-043 (design only; its build Jason's to order); the limitation recorded
  as Enterprise CR-001 §6.6 item 70 (phase 5 findings §5.2).

  If a FOP limitation is found, it is recorded in Enterprise CR-001 §6.6, as the CLAUDE.md says.
- `legals/NOTICE`: an entry for any ported code (D5), with the MIT permission notice where the code is
  office-open-xml-viewer's, to Jason for review before the first ported file is committed.
- None to the ports.

## 11. Effort (rough)

Phase 0 three days; phase 1 three days (the line layout and invisible text); phase 2 three days; phase 3 four days
(revised by its design: the round one, the code two, the gate one); phase 4 eight days (revised by its design and its
review: the shapetypes and the round two, the code five (the geometry source, the evaluator, the stretch, groups and
paint, the routes), the gate one); phase 5 six days docx4j-side (revised by its design and its review: the round one,
step 1 one, the painter and the routes three, the gate one), plus the fork's CR (fop/CR-043: about four days, its session's, each
painter and the serializer and the subset's glyph ids included; mode 2 half a day more if the gate calls for it); phase 6 two days after
CR-081.5; phase 7 one day; phase 8 half a day. About six weeks.

## 12. Specifications

Editions read (2026-11-09):
- ECMA-376 5th edition, aligned with ISO/IEC 29500:2016: Part 1 (December 2016), Part 2 (December 2021), Part 3
  (December 2015) and Part 4 (December 2016). Local copies: `~/git/safe-docx/spec-compliance/ecma-376/source-artifacts/`,
  with their `SHA256SUMS`.
- [MS-ODRAWXML] 35.0 (2026-10-06) and [MS-OI29500] 25.0 (2026-08-18), read on learn.microsoft.com (`office_standards`)
  on 2026-11-09.
- [MS-DOCX] 23.0 (2026-08-18), local copy `~/Downloads/[MS-DOCX]-260818.docx`.
- [MS-ODRAW] (the binary OfficeArt format of .doc and .ppt) for §2.4.24 `MSOSPT` only: the text shape-type numbers
  VML WordArt's `o:spt` uses, as names (D6 draws from the shapetype's path, not the number).

**ISO/IEC 29500-1 (ECMA-376 Part 1)**
- **Warps**: §20.1.9.19 `prstTxWarp` (its twelve steps are the algorithm: §3, D5), §20.1.10.76 `ST_TextShapeType`.
  (Advice of 2026-11-09 cited "§21.1.2.1.38" for `prstTxWarp`; the 2016 edition has no such section. Microsoft's Open
  XML SDK page for `PresetTextWrap` is said to carry the same text; the standard is cited here.) Annex H (informative) and its
  `OfficeOpenXML-DrawingMLGeometries.zip` hold the `presetTextWarpDefinitions.xml` docx4j ships (40 presets).
- **Body properties**: §20.4.2.22 `bodyPr` (the shape's), §21.1.2.1.1 `bodyPr` (DrawingML text's, with
  `fromWordArt`), §21.1.2.1.3 `normAutofit`, §21.1.2.1.4 `spAutoFit`.
- **Geometry** the warps are evaluated with: CR-080 §12.

**ISO/IEC 29500-4 (ECMA-376 Part 4), for D6.** §19.1.2.23 `textpath`, §19.1.2.20 `shapetype`, §19.1.2.19 `shape`,
§19.1.2.5 `fill`, §19.1.2.21 `stroke`, §19.1.2.18 `shadow`.

**[MS-DOCX]** §2.6 (`word/2010/wordml`, the `w14` run effects):
- elements: §2.6.1.25 `textFill`, §2.6.1.26 `textOutline`, §2.6.1.16 `glow`, §2.6.1.23 `shadow`, §2.6.1.21
  `reflection`, §2.6.1.20 `props3d`;
- types: §2.6.3.36 `CT_TextOutlineEffect`, §2.6.3.6 `CT_Glow`, §2.6.3.30 `CT_Shadow`, §2.6.3.24 `CT_Reflection`,
  §2.6.3.23 `CT_Props3D`.

**[MS-OI29500]: Office's notes on these sections**
- **2.1.1343 (§20.1.10.76 `ST_TextShapeType`):** gives the illustrations the standard lacks for `textCircle` and
  `textDeflateTop`, and defines `wd3` and `hd3` (w/3, h/3).
- **2.1.1379 (§21.1.2.1.1 `bodyPr`):** `fromWordArt` defaults to false; missing attributes are inherited (CR-082 §12);
  Word evaluates `rot` only from build 15609.10000.
- **2.1.1381 (§21.1.2.1.3 `normAutofit`):** Word decides autofit from a hierarchy of text styles, not the element
  alone.
- **2.1.1812 (Part 4 §19.1.2.23 `textpath`), for D6:**
  - `v-text-align` defaults to center, not left;
  - only some `style` properties are supported (font, font-family, font-size, font-style, font-variant, font-weight
    and the others the note lists), and `mso-text-shadow` defaults to none;
  - the text fills the parent shape's `width` and `height`, so `font-size` alone doesn't size it;
  - `text-decoration` and `font-variant` are kept on save but not drawn.
- **2.1.1809 (§19.1.2.20 `shapetype`):** `master` present means true.
