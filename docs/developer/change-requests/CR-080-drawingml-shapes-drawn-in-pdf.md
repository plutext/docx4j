# CR-080: DrawingML shapes drawn in PDF - preset and custom geometry, fills and lines, groups, canvases and connectors

Status: PROPOSED 2026-11-09. Drafted on Jason's request ("let's draft CRs for the following items with the given
suggested scope ... please evaluate what if any of that is worth porting"), one of four: this CR (shapes and geometry),
CR-081 (picture and shape effects), CR-082 (SmartArt from its drawing cache), CR-083 (WordArt). Numbered in the order
proposed for implementation: CR-081 to CR-083 draw with this CR's geometry. Not started; D1 to D10 open (§8). Read
against docx4j c0a66b610 (VERSION_17_3_7), Apache POI trunk ae62bb5116 (`../poi`) and docxide-pdf 9508da2
(2026-11-08), and the specifications in §12. Reviewed 2026-11-09 by a Claude Fable 5.1 session at Jason's request
("now have fable review the CRs please"). Its findings were applied on Jason's word ("apply the review's findings"):
- the [MS-OI29500] notes on `spPr` and `rect` quoted, with the text rectangle needing an `xfrm`;
- geometry limits against hostile input (D10);
- gap codes through the existing message writer and the gate's event lines (D9);
- wrapping as a dispatch to the picture path (D4);
- stacking order and counts corrected.

Owner: Jason Harrop. Drafted with Claude Opus 5.5.

## 0. Summary

docx4j's PDF output draws a DrawingML shape's text and nothing else. A rectangle, an arrow, a callout, a line, a group of
shapes or a drawing canvas has no fill and no outline on the page, and a shape with no text is absent. The fidelity
register has the fill and outline gap open as `drawingml-fills-not-painted` (CR-034 D4).

This CR draws them as Word does: the 187 preset geometries and `a:custGeom`, solid, gradient, pattern and picture fills,
lines with dashes, joins, caps and arrowheads, shapes in groups (`wpg:wgp`) and canvases (`wpc:wpc`) with their nested
transforms, and connectors. The drawing goes into the FO as SVG, as charts do (CR-037). A shape's text stays FO text,
painted over its shape, and a shape with no text takes the place and wrapping of an anchored picture.

The geometry engine is Apache POI's `org.apache.poi.sl.draw.geom`, repackaged as CR-011 repackaged HWMF and HEMF. The
painting is docx4j-chart-render's: its `Fills`, `Drawing` and `SvgWriter` already draw a DrawingML `a:spPr` to SVG,
measured against Office's PDFs. docxide-pdf is a cross-check, and nothing is ported from it (§3).

## 1. What docx4j does today (read from the code at c0a66b610)

- **FO, the visitor** (`FOExporterVisitorGenerator.apply`): a `wp:anchor` or `wp:inline` is handled only when
  `XsltFOFunctions.shapeTextBox` finds `wps:wsp/wps:txbx`. The text box's content goes into an `fo:block-container`
  that `WordLayoutFixups` positions as it positions an anchored picture (`FOTextBoxes`, since 17.0.5). Otherwise a
  `wps:wsp` writes nothing at all (read from the code; phase 0's probes confirm it).
- **FO, the XSLT** (`docx2fo.xslt`): the same choice (`a:graphicData/wps:wsp/wps:txbx/w:txbxContent`). For VML,
  `w:pict` handles `v:shape/v:imagedata` and `v:shape|v:rect/v:textbox`; anything else writes the comment "TODO: handle
  w:pict containing other than ./v:shape/v:imagedata".
- **Lines and fills**: `XsltFOFunctions.shapeLinePt` reads a line's width, so that a text box's inset counts it, and an
  `a:lnRef` counts as 0.75pt. No fill or stroke colour is written (`drawingml-fills-not-painted`).
- **Groups and canvases**: a `wpg:wgp` or `wpc:wpc` isn't recognised; their shapes' text boxes aren't reached.
- **The model**: `wps:wsp`, `wpg:wgp` and `wpc:wpc` are bound (`org.docx4j.com.microsoft.schemas.office.word.x2010.*`),
  and their `spPr` and `style` are DrawingML's `CTShapeProperties` and `CTShapeStyle`. The 187 preset geometries ship in
  `docx4j-core` (`org/docx4j/model/shapes/presetShapeDefinitions.xml`, the same 187 names as POI's copy), and
  `org.docx4j.model.shapes.PresetGeometries` unmarshals them into `CTCustomGeometry2D`. Only a pptx4j sample calls it
  (`docx4j-samples-pptx4j/.../ShapesPresetToCustom.java`); it dates from 2019. The 40 text-warp presets sit beside it (`presetTextWarpDefinitions.xml`, CR-083's).
- **Colours and styles** (CR-034, since 17.3.3): `org.docx4j.model.color.DrawingMLColors` and `StyleFill` resolve every
  colour kind, and a shape style's `a:fillRef` and `a:lnRef`, through the theme. Drawing is left to a renderer.
- **Drawing** (CR-037, CR-039, since 17.3.3): `docx4j-chart-render`'s `org.docx4j.chart.render.api` draws a
  `CTShapeProperties` to SVG (`Fills`: solid, gradient with any number of stops, linear or along a path, Office's 8 by 8
  patterns, line colour, width and dashes, outer shadow and glow, all through the theme; `Drawing`; `SvgWriter`, which
  also writes gradients). Charts reach the FO as `fo:instream-foreign-object`, and FOP draws them as vectors through
  Batik.

## 2. What Word does (to be measured in phase 0; what is known)

- Word's PDF draws a shape's geometry as vector paths, its gradients as PDF shadings, and its shadows and glows as
  pictures (CR-037 finding 6, measured for charts; the same is expected for shapes).
- A shape's text is laid out in the geometry's text rectangle (`a:rect` of the preset), not in its bounding box, inset
  by `bodyPr`'s `lIns`, `tIns`, `rIns` and `bIns`.
- A shape with no text wraps text as a picture does (expected; docxide-pdf has a fixture named for it,
  `empty-shape-wraps`, against Word for Mac; unmeasured here).
- A group's child shapes are placed by `a:chOff` and `a:chExt` against `a:off` and `a:ext`, with rotation and flips
  composed down the tree.

- Office departs from the standard's text in ways this CR follows ([MS-OI29500], §12):
  - `sqrt` in a guide formula takes the absolute value (the note's angle unit and `at2` are stated for PowerPoint,
    and phase 0 confirms them for Word);
  - a shape's text rectangle is ignored when its `xfrm` is omitted (2.1.1315);
  - a group's child box is the centre of its scaling, rotation and flips;
  - `flipH`, `flipV` and `rot` are ignored on a graphic frame;
  - a shape's `spPr` overrides its style's references;
  - `lnRef` indexes the line styles.

Phase 0 measures these on Word 365 (the VM, 2608) through the bridge.

## 3. Sources evaluated

### 3.1 Apache POI (trunk ae62bb5116; ASL 2.0)

| Part | What it is | Verdict |
|---|---|---|
| `org.apache.poi.sl.draw.geom` (34 files, 3,368 lines) | The DrawingML geometry engine: guides, Formula's 17 operators, the built-in guides, `arcTo`, adjust values, paths, connection sites, the text rectangle, `PresetGeometries` and a StAX `PresetParser` | **Port** (repackage into `org.docx4j.org.apache.poi.sl.draw.geom`, as CR-011 did HWMF and HEMF). Its imports outside `java.*` are StAX, POI's logging (CR-011's shim maps it to slf4j), `XMLHelper` and `PaintStyle`. POI has used it for years to render pptx slides (`SlideShow` to image). |
| `DrawSimpleShape` (443 lines): `computeOutlines`, `drawDecoration` and the head and tail decorations | A geometry's paths scaled to the anchor, and arrowheads (`a:headEnd`, `a:tailEnd`: triangle, stealth, diamond, oval, arrow; three widths and three lengths) | **Port these pieces** (about 200 lines) into docx4j's drawing. The rest of the class is written against POI's `sl.usermodel` interfaces. |
| `DrawShape`, `DrawAutoShape`, `DrawGroupShape`, `DrawTextShape`, `DrawTextParagraph`, `DrawPaint`, `PathGradientPaint` | The Java2D renderer of POI's own shape model | **Don't port.** It draws POI's `sl.usermodel` interfaces, which are a pptx object model. An adapter from docx4j's JAXB to them would be larger than the drawing it buys, and docx4j-chart-render already draws fills, gradients, patterns, dashes and shadows from docx4j's own `CTShapeProperties`. Its shadow is the outline moved and filled in the shadow's colour, with no blur, scale or skew; chart-render's is Office's (CR-081). `DrawPaint` is already in docx4j (CR-011). |
| `sl.usermodel.ShapeType` (246 constants) | Escher/VML shape type numbers (`o:spt`) to DrawingML preset names, the text shape types 136 to 175 included | **Keep for D6**: VML shapes and VML WordArt (CR-083) map through it. |
| `XSLFDiagram` (273 lines) | SmartArt drawn from `dsp:drawing`, converted to a pptx group | **Read, don't port**: CR-082 takes its `txXfrm` text-rotation rule (about 25 lines). |
| HSLF | Legacy binary PowerPoint | **No use for docx**: the one useful piece, the shape-type mapping, is `ShapeType` above, in `poi`, not HSLF. |
| `HwmfPicture`, `HemfPicture` | WMF and EMF | **Already in docx4j** since 17.1.0 (CR-011, `org.docx4j.org.apache.poi.hwmf`/`hemf`). Open there: phase 3 (OlePres000) and phase 4 (the missing EMF and EMF+ records). Nothing new to port. |

POI has no text warp (no `prstTxWarp` anywhere in its sources), no inner shadow, soft edge or reflection, and no
`wpg`/`wpc`/`dsp` reading.

### 3.2 docxide-pdf (github.com/sverrejb/docxide-pdf, 9508da2, 2026-11-08; ASL 2.0, no NOTICE file)

Rust (3.2 MB of `.rs` in `src/`, 3.5 MB with its tools and tests), writing PDF directly. One maintainer, active daily. Most of its code is written by Claude models
(its README), and it is measured against Word for Mac 16.106, mostly through the online "Best for electronic
distribution" export. Its geometry: `src/geometry/formulas.rs` (664 lines), `path.rs` (398) and `mod.rs` (375), with
the 187 presets and 40 warps compiled into Rust tables (`definitions.rs`, `text_warp_definitions.rs`, generated from
the ECMA XML by `tools/src/bin/generate_shapes.rs`). Groups and canvases are `src/docx/group.rs` (634 lines).

Verdict: **port nothing for this CR.** The engine is POI's in docx4j's own language with years of use, and docx4j
already has the definitions as XML. docxide's value here is as a second reading: where docx4j, POI and docxide disagree
on a preset, phase 0's Word PDFs decide. Its roadmap lists what it hasn't done: group-level flipH/flipV/rot except on
linear connectors, dashed outlines (`a:prstDash`), radial and path gradients, and a canvas or group inside a
paragraph-level `mc:AlternateContent`, where only the first `wsp` is taken. If any piece is ported later, the ported
file names its source file and commit, and `legals/NOTICE` gains an entry (§6).

### 3.3 Others considered (2026-11-09, on Jason's second list; each read in its code, not its README)

| Project | What its code shows | For this CR |
|---|---|---|
| pptx-renderer (aiden0z, Apache-2.0, TypeScript, HTML/SVG, 502a4a4) | Most presets are written by hand (`src/shapes/presets.ts`, 215 KB); its README says the spec-compiled runtime holds 29 definitions and that "arbitrary symbolic guides in custom geometry are not yet supported". | No: POI's engine evaluates every preset and every `custGeom` from the definitions. |
| rust-office-art (extend-hq/rust-office, MIT and Apache-2.0, Rust, d665c48, first published 2026-11-05) | A bounded evaluator over POI's own `presetShapeDefinitions.xml` (its `geometry_data/SOURCE.md` names the POI revision), with limits on guides, paths and commands; adjust handles, connection sites and text rectangles not in its API. | No code; it confirms POI's data. Its evaluation limits (1 MiB of XML, 1,024 guides, 256 paths, 8,192 commands; `geometry.rs`) are taken into phase 1 (D10). The crate's licence is MIT AND Apache-2.0 AND OFL-1.1; its OFL font data is never copied. |
| office-open-xml-viewer (yukiyokotani, MIT, TypeScript and Rust, Canvas, a4f7785) | A postfix guide evaluator and path executor (`packages/core/src/shape/preset-geometry/`), arrowheads (`arrow-geometry.ts`). | No for geometry (POI's is the one in Java); its WordArt warp is CR-083's. |
| LibreOffice (MPL-2.0, C++) | Mature DrawingML import (`oox/source/drawingml`). | A reference and an oracle (a document converted through it), not a source: MPL-2.0 is file-level copyleft, so a ported file would stay MPL. |
| pptx-typst (Sovichea, Apache-2.0, Python) | A Typst pipeline that can emit a stored SmartArt cache as SVG; its geometry is one 4 KB file. | No. |

## 4. Corpus facts (2026-11-09; the four fidelity corpora, document parts, headers and footers)

| | real (194) | real2 (157) | real3 (103) | real-c2 (107) |
|---|---|---|---|---|
| `wps:wsp` | 9 docs, 19 | 7, 20 | 10, 25 | 9, 29 |
| of which with `wps:txbx` | all | all | all | all |
| a fill on a `wps:spPr` | 9, 19 | 6, 18 | 9, 22 | 8, 28 |
| a preset other than `rect` | 0 | 1, 2 | 0 | 0 |
| `a:custGeom`, `wpg:wgp`, `wpc:wpc`, connectors | 0 | 0 | 0 | 0 |

The fidelity corpora were chosen for text and hold almost no shapes. In them this CR moves colour (the 32 documents
whose text boxes have fills and lines) and not layout. So phase 0 counts shapes in the load survey's catalogue as well
(3.72M documents; its `parts` column and a feature pass, through the portfolio), and builds probe documents, since
corpus documents can't be the only oracle.

## 5. Approach

1. **Geometry in docx4j-core** (`org.docx4j.model.shapes`, D2). POI's engine is repackaged. Beside it,
   `ShapeGeometry.outlines(CTShapeProperties, width, height)` gives a shape's paths as `java.awt.geom.Path2D`, each with
   its fill mode (`norm`, `none`, `lighten`, `lightenLess`, `darken`, `darkenLess`) and its stroke flag, plus the text
   rectangle and connection sites. It reads a preset with `a:avLst` adjustments or an `a:custGeom`, through an adapter
   from docx4j's `CTCustomGeometry2D` to POI's `CustomGeometry`. One copy of `presetShapeDefinitions.xml` stays,
   docx4j's. Core already needs `java.desktop` for HWMF, so `java.awt.geom` costs nothing new.
2. **Drawing in docx4j-chart-render** (D2): a `ShapeRenderer` beside the chart renderers draws outlines with `Fills`
   (fill modes applied as Office lightens and darkens), lines with `a:prstDash`/`a:custDash`, `cap`, `cmpd` and joins,
   arrowheads (ported from POI), and picture fills (`a:blipFill` with `a:stretch` or `a:tile`), to SVG through
   `SvgWriter`. `a:style` references go through CR-034's `StyleFill`, and the theme's `a:fmtScheme` gives the fill and
   line of `idx` 1 to 3 and the background fills of 1001 and up.
3. **Groups and canvases**: a `wpg:wgp` or `wpc:wpc` is one drawing. Its children are flattened, with each group's
   `a:xfrm` (offset, extent, child offset and extent, rotation, flips) composed down the tree. Fill inheritance
   (`a:grpFill`) and canvas backgrounds (`wpc:bg`, `wpc:whole`) are included. A child's text box becomes its own FO
   container at the flattened position.
4. **Into the FO** (D3): the drawing is an `fo:instream-foreign-object` holding the SVG, in the container
   `WordLayoutFixups` already builds for an anchored picture or text box, under the text. Painting order is document
   order, as FOP has no `z-index`: the shape first, then its text. A shape behind the text (`behindDoc`) in the same
   paragraph is already under its lines: it goes in the zero-height wrapper at the paragraph's top, as a behind-text
   picture does. Two overlapping shapes in one paragraph paint in document order; `wp:anchor/@relativeHeight` is not
   honoured. A shape with no text gets a picture's place and wrapping (D4).
5. **HTML** (D5): the same SVG inline in `Docx4J.toHTML`'s output, as charts already are (CR-037 phase 3).

## 6. Phases

- **Phase 0, the oracle and the counts.** 
  - Probes: every preset at default adjustments, and with non-default adjustments for the 40 that take them most
    visibly; custGeom with each path command; fills of each kind and fill modes; dashes, caps, joins and the nine
    arrowhead sizes of each type; groups nested three deep with rotation and flips; a canvas; connectors (straight, bent,
    curved). Word 365 saves each and makes its PDF (bridge). The numbers come from `mutool trace` of the paths, and
    the raster difference comes second.
  - The load survey's counts (via portfolio).
  - The repackaging decision's licence notes (`legals/NOTICE`; each copied file keeps its ASF header under CR-011's
    Plutext notice).
  - **Gate:** probes and Word's PDFs on the share, the numbers committed.
- **Phase 1, the engine.**
  - POI's `geom` repackaged, with the adapter from `CTCustomGeometry2D`; its `sqrt` made Office's, sqrt(|x|)
    ([MS-OI29500] 2.1.1311, §12).
  - The limits of D10 on every `custGeom`, each tested with a hostile probe, as CR-057 tested its cases.
  - `ShapeGeometry`, tested against phase 0's numbers (every preset's path within 0.1pt of Word's at 100 by 100pt, the
    text rectangles exact).
  - **Gate:** core-tests.
- **Phase 2, drawing a single shape.**
  - `ShapeRenderer` (fills, lines, dashes, arrowheads, picture fills, style references) in docx4j-chart-render.
  - Shapes in the FO, visitor and XSLT alike, with text boxes on top and the geometry's text rectangle used for the
    text. A shape whose `xfrm` is omitted has no text rectangle, as in Office ([MS-OI29500] 2.1.1315), and lays its
    text out in its box.
  - **Gate:** the probes within tolerance; corpora unmoved (0 movers; colour only); the register's
    `drawingml-fills-not-painted` closed.
- **Phase 3, groups, canvases and connectors.**
  - Flattening with composed transforms, `a:grpFill`, canvas backgrounds, children's text boxes.
  - **Gate:** the probes; corpora unmoved.
- **Phase 4, shapes that wrap** (D4).
  - A shape with no text takes a picture's place and wrapping in `WordLayoutFixups`. This is a dispatch change: the
    shape carries the hint attributes `WordXmlPictureE20` writes for a picture, and `WordLayoutFixups`' existing
    `wrapSquare`, `wrapTopAndBottom` and `behindDoc` handling places it. `wp:wrapTight` and `wp:wrapThrough` polygons
    are handled as a picture's are.
  - **Gate:** a fidelity gate against the then baseline, movers read.
- **Phase 5, HTML** (D5).
- **Phase 6, CHANGELOG, Getting Started, the register.**

## 7. Risks

- **Fill modes and Office's lightening.** `darken` and `lighten` are relative to the fill colour, and their exact
  factors aren't in the standard. Phase 0 measures them.
- **Painting order.** FOP has no `z-index`. A shape behind text in a later paragraph can only be painted under the
  lines that follow it in document order, and overlapping shapes in one paragraph ignore `relativeHeight`.
  docxide-pdf names the same limit ("needs a pre-pass"). Phase 0 counts overlaps and later-paragraph `behindDoc` shapes
  in the load survey before any pre-pass is built.
- **Hostile geometry.** A `custGeom` with huge guide lists or paths, or an extent near 2^31 EMU, can cost memory and
  time; D10 bounds them, and the SVG written for one drawing is bounded too.
- **FOP and SVG.** Batik draws SVG paths, gradients and pictures as vectors and PDF images. Chart-render relies on
  this already, so the risk is low, but pattern fills as tiles and very large paths need watching for size.
- **The text rectangle moves text.** A shape's text laid out in its geometry's text rectangle rather than its bounding
  box moves lines in callouts and arrows. That is a layout change and goes through the fidelity gate (phase 2's gate
  counts it).

## 8. Decisions (owner: Jason; recommendations)

- **D1. The geometry engine.** (a) Repackage POI's `sl.draw.geom` with an adapter from docx4j's JAXB; (b) write one over
  `CTCustomGeometry2D` directly; (c) port docxide-pdf's Rust. Recommendation: **(a)**. It is the most used of the
  three, it is Java, and CR-011's repackaging route and attribution are already in place.
- **D2. Where the code lives.** Geometry in docx4j-core (`org.docx4j.model.shapes`: no Batik, usable by HTML, pptx and
  the text model's outlines); drawing in docx4j-chart-render (it already holds `Fills`, `Drawing`, `SvgWriter` and
  shadows, and docx4j-export-fo depends on it). The module's name stays, since renaming an artifact costs consumers;
  its description becomes "DrawingML drawn to SVG: charts and shapes". Recommendation: yes. The alternative is a new
  module that chart-render depends on, which would move CR-039's published API.
- **D3. Shapes as SVG in the FO** (`fo:instream-foreign-object`), with the text staying FO. Recommendation: yes. The
  text keeps docx4j's line breaking, fonts and Word layout, and the drawing is vector.
- **D4. Shapes with no text take space and wrap**, in a phase of their own with its own gate (phase 4), through the
  picture path's existing wrap handling (phase 4's dispatch). Recommendation: yes. It is a layout change, and the
  corpora barely exercise it.
- **D5. HTML output too**, from the same SVG. Recommendation: yes, phase 5.
- **D6. VML shapes** (`v:shape` with `o:spt` or `v:path`, `v:rect`, `v:roundrect`, `v:oval`, `v:line`, `v:polyline`,
  `v:arc`, `v:curve`). Word reads them only where no DrawingML is written. Under CR-051's word mode docx4j reads the
  DrawingML Choice wherever there is one, so VML matters for documents from Word 2007 and earlier and from other
  producers. Recommendation: **a later CR**, once phase 0 has counted them: through POI's `ShapeType` (o:spt to preset,
  with the adjust units mapped) for preset shape types, plus a `v:path` parser.
- **D7. The oracle.** Word's PDFs through the bridge, paths read as numbers (`mutool trace`), then the raster against
  Word's. docxide-pdf's references are Word for Mac's online export, so they are not ours. Recommendation: yes.
- **D8. docxide-pdf.** Port nothing for this CR; read it as a cross-check; any later port names its source file and
  commit in the file's header and gains a `legals/NOTICE` entry. Recommendation: yes.
- **D9. Gaps are reported, not silent.** A shape that can't be drawn (an unknown preset, a malformed `custGeom`) is
  drawn as its bounding box outline, not dropped. It is reported with a stable code (`render.shape.unknown_preset`,
  `render.shape.bad_geometry`, `render.shape.vml` until D6's CR, `render.shape.over_limit`, and so on), once per
  drawing (a `wp:inline` or `wp:anchor`), through the two mechanisms that exist:
  - the conversion's message writer (`AbstractMessageWriter.notImplemented`, which the XSLT's catch-all and the
    visitor already call for a `wps:wsp` with no text box), so that a debug run still marks the place in the output;
  - a log line in the event format the fidelity gate's `events.py` reads (`docx4j-layout-fidelity/README.md`), so that
    gates count gaps with no new plumbing.

  A listener on the conversion's settings may expose the same codes to a caller as a convenience. The codes are
  introduced here and used by CR-081 to CR-083 (Jason, 2026-11-09: what can't be output "is a gap").
  Recommendation: yes.
- **D10. Limits against hostile geometry** (CR-057's concern), as rust-office-art's evaluator has them: at most 1 MiB
  of geometry XML, 1,024 guide evaluations, 256 paths and 8,192 path commands per shape, arc sweeps of 16 turns, and
  finite coordinates within the drawing's bounds. Past a limit the shape is drawn as its box and reported
  (`render.shape.over_limit`). Recommendation: yes, the numbers confirmed against the probes and the load survey's
  largest real `custGeom`.

## 9. Out of scope

3-D (`a:scene3d`, `a:sp3d`) and effects (CR-081); SmartArt (CR-082); WordArt's warps (CR-083); VML (D6); ink
(`w14:contentPart`); pptx and xlsx output (pptx4j's SVG exporter could use the engine later); a shape's text laid
out by anything other than FO.

## 10. Hand-offs

- To the portfolio: four registry entries (CR-080 to CR-083) with their phases and dependencies:
  - CR-081.1 depends on nothing; CR-081.2 on CR-080.1; CR-081.3 on CR-080.2;
  - CR-082.1 on CR-080.1 and CR-080.2; CR-082.3 on CR-080.3 and CR-080.5 (HTML);
  - CR-083.1 on CR-080.1;
  - CR-083.6 on CR-081.5, which CR-082's 3-D styles also wait for (they are reported as gaps until then).
- To Apache POI, through Jason: `GuideIf`'s `sqrt` gives NaN for a negative guide, where Office takes sqrt(|x|)
  ([MS-OI29500] 2.1.1311). The JIRA text is drafted in phase 1.
- To the FO renderer fork's session: none expected. The drawing is SVG, which FOP already draws. If phase 2 finds a FOP
  defect, it goes to Enterprise CR-001 §6.6 as the CLAUDE.md says.
- The ports (core-ts, python): none. Rendering isn't in their scope; the geometry numbers of phase 0 are available if
  one day it is.

## 11. Effort (rough)

Phase 0 two days (probe generator, bridge rounds, trace reader); phase 1 two days; phase 2 three days; phase 3 two days;
phase 4 two days plus the gate; phases 5 and 6 one day. About two weeks.

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
- **Geometry** (§20.1.9, Shape Definitions and Attributes):
  - §20.1.9.18 `prstGeom`, §20.1.9.8 `custGeom`, §20.1.9.5 `avLst`, §20.1.9.12 `gdLst`, §20.1.9.11 `gd`;
  - §20.1.9.16 `pathLst`, §20.1.9.15 `path`, §20.1.9.14 `moveTo`, §20.1.9.13 `lnTo`, §20.1.9.4 `arcTo`,
    §20.1.9.21 `quadBezTo`, §20.1.9.7 `cubicBezTo`, §20.1.9.6 `close`;
  - §20.1.9.22 `rect` (the text rectangle), §20.1.9.10 `cxnLst`, §20.1.9.1 `ahLst`;
  - §20.1.10.27 `ST_GeomGuideFormula`, §20.1.10.37 `ST_PathFillMode`, §20.1.10.56 `ST_ShapeType`.
- **Annex H** (informative), "Example Predefined DrawingML Shape and Text Geometries": its electronic file
  `OfficeOpenXML-DrawingMLGeometries.zip` holds the `presetShapeDefinitions.xml` that docx4j and POI ship (the same 187
  names).
- **Fills and lines** (§20.1.8):
  - fills: §20.1.8.54 `solidFill`, §20.1.8.33 `gradFill`, §20.1.8.46 `path` (path gradient), §20.1.8.14 `blipFill`,
    §20.1.8.47 `pattFill`, §20.1.8.35 `grpFill`, §20.1.8.44 `noFill`;
  - lines: §20.1.2.2.24 `ln`, §20.1.8.48 `prstDash`, §20.1.8.21 `custDash`, §20.1.8.38 `headEnd`, §20.1.8.57 `tailEnd`,
    §20.1.10.33 `ST_LineEndType`.
- **Transforms, groups and connectors**: §20.1.7.6 `xfrm` (individual objects), §20.1.7.5 `xfrm` (grouped objects),
  §20.1.7.1 `chExt`, §20.1.7.2 `chOff`; §20.1.2.2.20 `grpSp`, §20.1.2.2.10 `cxnSp`; §20.1.2.2.35 `spPr`.
- **Style and theme**: §20.1.2.2.37 `style`, §20.1.4.2.10 `fillRef`, §20.1.4.2.19 `lnRef`, §20.1.4.1.14 `fmtScheme`.
- **WordprocessingML drawing** (§20.4): §20.4.2.42 `wsp`, §20.4.2.39 `wgp`, §20.4.2.41 `wpc`, §20.4.2.37 `txbx`,
  §20.4.2.32 `grpSp`, §20.4.2.33 `grpSpPr`, §20.4.2.22 `bodyPr`, §20.4.2.3 `anchor`, §20.4.2.8 `inline`.

**ISO/IEC 29500-3 (ECMA-376 Part 3).** Word writes a shape as `mc:AlternateContent` with the `wps` Choice and a VML
Fallback, so §7.5 `AlternateContent`, §7.6 `Choice`, §7.7 `Fallback` and §7.2 `Ignorable` apply. CR-051's word mode
reads them.

**ISO/IEC 29500-2 (ECMA-376 Part 2).** §6.5 relationships, for picture fills (`a:blip r:embed`).

**ISO/IEC 29500-4 (ECMA-376 Part 4), for D6.** §19.1 VML:
- §19.1.2.19 `shape`, §19.1.2.20 `shapetype`, §19.1.2.14 `path`, §19.1.2.6 `formulas`, §19.1.2.4 `f`;
- §19.1.2.16 `rect`, §19.1.2.17 `roundrect`, §19.1.2.13 `oval`, §19.1.2.12 `line`, §19.1.2.15 `polyline`,
  §19.1.2.1 `arc`, §19.1.2.3 `curve`, §19.1.2.7 `group`.

**[MS-ODRAWXML].** The namespaces Word writes:
- §2.13 `wordprocessingShape` (§2.13.3.3 `CT_WordprocessingShape`), §2.16 `wordprocessingGroup` (§2.16.3.2
  `CT_WordprocessingGroup`), §2.17 `wordprocessingCanvas` (§2.17.3.1 `CT_WordprocessingCanvas`);
- §2.2.7.3 to §2.2.7.5, DrawingML shapes, groups and canvases in WordprocessingML;
- schemas §5.2, §5.4, §5.5.

Part 1 §20.4.2.39 to §20.4.2.42 describe the same elements, and Word writes them in these 2010 namespaces.

**[MS-OI29500]: Office's notes on these sections**
- **2.1.1311 (§20.1.9.11 `gd`):**
  - `mod x y z` is sqrt(x² + y² + z²);
  - `sqrt x` is sqrt(|x|);
  - (stated for PowerPoint) trigonometric formulas take angles in 60000ths of a degree;
  - (stated for PowerPoint) `at2 x y` is atan2(y, x), and 0 at the origin.

  Phase 0 confirms the two PowerPoint statements for Word.

  POI's `GuideIf` agrees on `mod`, `at2` and the angle unit. Its `sqrt` is `sqrt(x)`, which gives NaN for a
  negative guide where Office takes the absolute value. Phase 1 fixes it in the repackaged copy, tests it, and drafts
  the POI report for Jason to file.
- **2.1.1336 (§20.1.10.56 `ST_ShapeType`):** the built-in guides `wd3` and `hd3` are w/3 and h/3 (POI's
  `BuiltInGuide` has both).
- **2.1.1282 (§20.1.7.5, grouped `xfrm`):**
  - `rot` is in 60000ths of a degree;
  - the child bounding box (`chOff`, `chExt`) is the centre for scaling, rotating and flipping;
  - with non-zero child extents, the group scales by `ext`/`chExt`.

  This is the rule phase 3 composes down the tree.
- **2.1.1283 (§20.1.7.6, `xfrm`):** `flipH`, `flipV` and `rot` are ignored on a graphic frame.
- **2.1.1217 (§20.1.2.2.37 `style`):**
  - a shape's `spPr` overrides its style's `lnRef`, `fillRef`, `effectRef` and `fontRef`;
  - `fontRef` isn't used for `pic` or `cxnSp`;
  - a `txBody` overrides `fontRef`.
- **2.1.1264 (§20.1.4.2.19 `lnRef`):** `idx` indexes the theme's `lnStyleLst`; the standard says `fillStyleLst`.
- **2.1.1315 (§20.1.9.22 `rect`):** "The 2D Transform (§20.4.2.43; xfrm) modifies Shape Text Rectangle and is
  required in order to calculate the correct value. Office will ignore the value of Shape Text Rectangle if the 2D
  Transform is omitted." Phase 2 follows it.
- **2.1.1215 (§20.1.2.2.35 `spPr`):** points to the notes for §20.5.2.30 `spPr`, 2.1.1373:
  - Excel and Word don't support `bwMode` on `spPr`;
  - Office uses `spPr` for shapes, connector shapes and pictures alike;
  - `bwMode` says how a group renders in black-and-white mode.

  None changes the drawing here.
- **For D6:** 2.1.1808 (Part 4 §19.1.2.19 `shape`) and 2.1.1809 (§19.1.2.20 `shapetype`: `master` present means
  true, and `chromakey` and `opacity` aren't supported).
