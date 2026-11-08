# CR-083: WordArt drawn - text warps (`a:prstTxWarp`), text outlines, fills, shadows and glows, and VML WordArt

Status: PROPOSED 2026-11-09. Drafted on Jason's request (the four CRs of 2026-11-09; see CR-080's Status). Not
started; D1 to D11 open (§8). Needs CR-080 phase 1 (the geometry engine evaluates the warp presets), and CR-081 phase 1
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

Owner: Jason Harrop. Drafted with Claude Opus 5.5.

## 0. Summary

Word's WordArt is a text box (`wps:wsp` with `wps:txbx`) whose body properties name a text warp
(`wps:bodyPr/a:prstTxWarp`, one of 40 presets besides `textNoShape`) and whose runs carry Word 2010's text effects
(`w14:textOutline`, `w14:textFill`, `w14:shadow`, `w14:glow`, `w14:reflection` in `w:rPr`). docx4j's PDF lays the text
out flat, in the run's colour, with none of the effects.

This CR draws such a text box as glyph outlines: laid out with the font file FOP embeds, warped through the preset's
paths (CR-080's engine evaluates `presetTextWarpDefinitions.xml`, which docx4j already ships), filled and outlined as
its effects say, shadowed and glowing with CR-081's effects. The text also goes in the PDF as invisible text, so that
it can still be searched and copied (D2). Legacy VML WordArt (`v:textpath`) maps to the same presets (D6).

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

## 2. What Word does (phase 0 measures it)

Not yet measured here:
- whether Word's PDF writes warped WordArt as glyph outlines or as text;
- how a line's glyphs are spread along the path (by arc length, or by the path's parameter);
- how a multi-line WordArt divides itself between paths;
- what `fromWordArt` and `a:normAutofit` change;
- how `w14:textOutline` joins and dashes, and gradient `w14:textFill`, look in a PDF.

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
  docx4j writes its own table for D6 from [MS-ODRAW] §2.4.24 `MSOSPT`, which numbers them `msosptTextPlainText`
  (0x88, 136) to `msosptTextCanDown` (0xAF, 175). It maps each to its `ST_TextShapeType` name, checked against Word
  in phase 0. POI's is at most a cross-check on the result.
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
    start for that much arc length; it isn't stretched over the whole path.
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
does (phase 0). Its rectangle is the text's ink, not docxide-pdf's normalised font height; phase 0 decides which
Word uses.

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
   a `w14:textOutline` or a non-solid `w14:textFill` (D3), is drawn as WordArt. Any other text box stays FO text.
2. **Layout**: the box's paragraphs are laid out as lines of glyphs with `java.awt.Font` loaded from the font file
   docx4j's `PhysicalFonts` maps the run's font to, the same file FOP embeds (D4). Each run has its own font, size and
   effects. This is a line layout of a few lines, not a paragraph layout engine: alignment, the paragraph's
   indentation, and line breaks where Word put them (`w:br`) or at the box's width.
3. **Warp**: CR-080's engine evaluates the preset with its `a:avLst` at the box's size. For two paths, every glyph
   outline point is mapped as §20.1.9.19 describes (§3): by its fractions of the text's rectangle, measured along each
   path by length. For one path, glyphs follow it by arc length (Follow Path, if phase 0 confirms it for Word). For more
   than two, it is whatever phase 0 shows Word does with the bands.
4. **Paint**: fills and outlines from `w14:textFill` and `w14:textOutline` (solid, gradient; width, dash, join, cap),
   via chart-render's `Fills` path for DrawingML fills (w14's are DrawingML fills in another namespace). Shadow, glow
   and reflection go through CR-081's effects on the glyphs' outline. The result is one SVG in the text box's place.
5. **Invisible text** (D2): the text again as SVG text with `fill="none"` (and no stroke) at the line positions,
   unless phase 0 shows Word's PDF keeps WordArt as real text, in which case docx4j does what Word does. The fork's
   `PDFTextPainter.beginTextObject` sets the text rendering mode from the fill and stroke paints, so `fill="none"`
   gives PDF mode 3 (neither filled nor stroked: invisible, but found by a search and copied). A zero fill opacity
   would go through a transparency graphics state instead.
6. **VML** (D6): `v:textpath` with its `v:shapetype`'s `o:spt` mapped through docx4j's own `MSOSPT` table to a preset, its
   `string`, `style` (font and size), `fitshape` and `fitpath` read, its `v:fill` and `v:stroke` as the paint.

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
- **Phase 2, the warps.**
  - The 33 envelopes, the 3 paths, the 4 multi-path presets, as phase 0 measured.
  - **Gate:** each preset's probes within tolerance (glyph-outline points against Word's, after alignment).
- **Phase 3, effects on WordArt** (needs CR-081.1): shadow, glow and reflection on the glyphs.
- **Phase 4, VML WordArt** (D6).
  - `v:textpath` mapped to its preset through docx4j's `MSOSPT` table (§3) and drawn by phases 1 to 3, with Office's
    `textpath` rules (§12).
  - **Gate:** the probe and real3's document (6 `v:textpath`) within tolerance.
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
  - HTML: a body run's outline as CSS `-webkit-text-stroke`, and a gradient fill as `background-clip: text`. Shadow
    and glow as `text-shadow` where CSS can carry them; anything CSS can't is reported (D9).
  - First, before the fork release: a solid `w14:textFill` as the text's colour, docx4j-side only, in PDF and HTML.
  - **Gate:** the body-text probes within tolerance; a fidelity gate (layout unmoved, since only painting changes).
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
  starts. The tolerance per preset comes from Word against itself.
- **Fonts.** A glyph outline needs the font file. A font FOP substitutes is drawn in the substitute, as the rest of
  the PDF is. Cloud fonts Word has and this machine doesn't move the comparison: the probes use installed fonts.
- **Searchability.** Outlines alone lose the text; invisible text restores search and copy but not tagging. The
  tagged-PDF work, if any, has to place it in the structure. Noted, not solved here.
- **Line breaking.** WordArt text is laid out by this CR's line layout, not by FOP. A long WordArt that Word wraps
  inside a narrow box may break differently. Phase 0's two-line probes measure it.

## 8. Decisions (owner: Jason; recommendations)

- **D1. Glyph outlines warped in Java, drawn as SVG paths**: no PDF-level text warping exists, and FOP lays text out
  only flat. Recommendation: yes.
- **D2. Invisible text for search and copy**, unless Word's PDF keeps the text. Recommendation: do what Word does,
  measured in phase 0; invisible text if Word writes outlines.
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
- **D4. The same font file FOP embeds**, loaded into `java.awt.Font` through `PhysicalFonts`, not AWT's lookup by family
  name. Recommendation: yes.
- **D5. Write the warp from ISO/IEC 29500-1 §20.1.9.19**, which describes it step by step. docxide-pdf's outline
  deformation and office-open-xml-viewer's arc-length and Follow Path rules are cross-checks and the hypotheses for
  the presets the standard doesn't cover. Code is ported from them only where that saves work, with acknowledgement
  (Apache-2.0, MIT). Java2D's `GlyphVector.getOutline` and `PathIterator` give the outlines that docxide-pdf reads
  with `ttf_parser`, so little of its code is needed. Recommendation: yes. This replaces the earlier draft's "port
  docxide-pdf's algorithms", written before §20.1.9.19 was read.
- **D6. VML WordArt in this CR** (phase 4), through docx4j's own table of `o:spt` 136 to 175, from [MS-ODRAW]
  §2.4.24 and checked against Word (phase 0). The mapping's oracle is one VML WordArt per shape type, saved by Word in
  Word 2007 compatibility mode. Each is then read two ways:
  - Word's own conversion of the document to the current format, if it rewrites the WordArt as `a:prstTxWarp`;
  - otherwise, Word's PDF against the preset the name gives. Word writes VML
  WordArt for documents in Word 2007 compatibility mode (12) or earlier. Recommendation: yes, whatever the count. One
  real3 document already has six, and leaving them undrawn is a gap.
- **D7. A WordArt's text in the text model** stays as it is: the text box's text (CR-046), unaffected by the warp.
  Recommendation: yes.
- **D8. Where the code lives**: with CR-080's drawing (docx4j-chart-render), the glyph layout beside chart-render's
  `TextWrap`. The SVG serves the FO and HTML outputs alike, as charts' does. Recommendation: yes.
- **D9. Gaps are reported** with stable codes (`render.wordart.unknown_preset`, `render.wordart.flat_3d`,
  `render.wordart.css_effect` and so on), once per drawing, through CR-080 D9's two mechanisms: the message writer and
  the gate's event lines. Recommendation: yes.
- **D10. 3-D WordArt** (`w14:props3d`, `w14:scene3d`): drawn flat with its fill, outline and effects from phase 1, so
  it is never missing. Its bevel, depth and rotation come in phase 6, with CR-081's 3-D renderer (CR-081 phase 5).
  Recommendation: yes.

- **D11. WordArt in headers and footers** is drawn as in the body: the same code path, as CR-081 D8 and CR-082's
  phase 3 have it. Recommendation: yes.

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

Phase 0 three days; phase 1 three days (the line layout and invisible text); phase 2 three days; phase 3 one day; phase
4 two days; phase 5 three days docx4j-side, plus the fork's CR; phase 6 two days after CR-081.5; phase 7 one day;
phase 8 half a day. About four weeks.

## 12. Specifications

Editions read (2026-11-09):
- ECMA-376 5th edition, aligned with ISO/IEC 29500:2016: Part 1 (December 2016), Part 2 (December 2021), Part 3
  (December 2015) and Part 4 (December 2016). Local copies: `~/git/safe-docx/spec-compliance/ecma-376/source-artifacts/`,
  with their `SHA256SUMS`.
- [MS-ODRAWXML] 35.0 (2026-10-06) and [MS-OI29500] 25.0 (2026-08-18), read on learn.microsoft.com (`office_standards`)
  on 2026-11-09.
- [MS-DOCX] 23.0 (2026-08-18), local copy `~/Downloads/[MS-DOCX]-260818.docx`.
- [MS-ODRAW] (the binary OfficeArt format of .doc and .ppt) for §2.4.24 `MSOSPT` only: the text shape-type numbers
  VML WordArt's `o:spt` uses (D6).

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
