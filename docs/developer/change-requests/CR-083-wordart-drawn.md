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
  "TODO: handle w:pict containing other than ./v:shape/v:imagedata").

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
- a VML `v:textpath` centres by default and fills its parent shape's box whatever its `font-size`;
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
(53), real3 3 (11), real-c2 2 (39), in text boxes and body text alike. VML `v:textpath`: real3 1 document (6). WordArt
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
   do (step 3). A shapetype with no path draws nothing, as in Word. Its `string`, `style` (font and size), `fitshape`
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
- **Phase 4, VML WordArt** (D6).
  - First, round 2 (findings §11): Word's own shapetypes for types 137 to 175, and a VML probe of each.
  - `v:textpath` drawn from its shapetype's own `path`, `formulas` and `adj`, through a VML formula evaluator, and then
    by phases 1 to 3, with Office's `textpath` rules (§12). A shapetype with no path draws nothing, as in Word. Text
    watermarks are drawn as filled outlines.
  - **Gate:** the probes and real3's document (6 `v:textpath`) within tolerance.
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
    docx4j gives the run's line the room, from phase 3's round (fb10), and the fidelity gate's movers are read as that.
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
  - Whatever the count: one real3 document has six, and Word's text watermark is VML WordArt.
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

  If a FOP limitation is found, it is recorded in Enterprise CR-001 §6.6, as the CLAUDE.md says.
- `legals/NOTICE`: an entry for any ported code (D5), with the MIT permission notice where the code is
  office-open-xml-viewer's, to Jason for review before the first ported file is committed.
- None to the ports.

## 11. Effort (rough)

Phase 0 three days; phase 1 three days (the line layout and invisible text); phase 2 three days; phase 3 four days
(revised by its design: the round one, the code two, the gate one); phase 4 two days; phase 5 three days docx4j-side,
plus the fork's CR; phase 6 two days after CR-081.5; phase 7 one day; phase 8 half a day. About four weeks.

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
