# CR-080: DrawingML shapes drawn in PDF - preset and custom geometry, fills and lines, groups, canvases and connectors

Status: ACCEPTED 2026-11-10 (Jason: "CR-080 decisions as recommended"): D1 to D11 as recommended (§8), with phase 0's
changes (§6). Proposed 2026-11-09, drafted on Jason's request ("let's draft CRs for the following items with the given
suggested scope ... please evaluate what if any of that is worth porting"), one of four: this CR (shapes and geometry),
CR-081 (picture and shape effects), CR-082 (SmartArt from its drawing cache), CR-083 (WordArt). Numbered in the order
proposed for implementation: CR-081 to CR-083 draw with this CR's geometry. Phase 0 run (`CR-080-notes/`); phase 1
done, the geometry engine and `ShapeGeometry` (§6); phase 2 built, shapes drawn, its D12 (c) open: the `wps` Choice
drawn by default once 9775's text box loss is mended. Read
against docx4j c0a66b610 (VERSION_17_3_7), Apache POI trunk ae62bb5116 (`../poi`) and docxide-pdf 9508da2
(2026-11-08), and the specifications in §12. Reviewed 2026-11-09 by a Claude Fable 5.1 session at Jason's request
("now have fable review the CRs please"). Its findings were applied on Jason's word ("apply the review's findings"):
- the [MS-OI29500] notes on `spPr` and `rect` quoted, with the text rectangle needing an `xfrm`;
- geometry limits against hostile input (D10);
- gap codes through the existing message writer and the gate's event lines (D9);
- wrapping as a dispatch to the picture path (D4);
- stacking order and counts corrected.

Revised 2026-11-09 (Jason: "make the LibreOffice wording black-box only in all four CRs; rewrite POI's txXfrm
text-rotation rule and its VML shape-type table"):
- LibreOffice is a black-box oracle only (CR-080 D11);
- POI's `txXfrm` rule (CR-082) and its `ShapeType` table (CR-083, CR-080 D6) are written independently and not
  ported.

Phase 0 begun 2026-11-10 (Jason, asked whether CR-080 should come before more CR-081 work: "CR-080 next"). The route
is CR-081's, through word-bridge.
- `CR-080-notes/build_shapes.py` built round 1, `cr080-phase0`: 13 files and 1,284 probes, each file staged once, since
  Word was deterministic over CR-081's 718 measured draws. Each shape is anchored at a fixed place on its page, with no
  wrap, so that its frame in the PDF is known exactly. Every file is valid against the ECMA-376 transitional schemas.
- What round 1 holds:
  - every preset at 100 by 100 pt and at 144 by 72;
  - the 123 presets with adjustments, at half and at double their defaults;
  - custom geometry: each path command, arcs, the guide formula operators and the built-in guides;
  - lines: widths, dashes, caps, joins, compound lines, alignment and line fills;
  - the arrowheads, in every type and size;
  - fills: solid, gradient, the 54 patterns and picture fills;
  - style references under the 2023 and 2013 themes;
  - every preset's text rectangle.
- Three presets use the guides `hd10` and `wd12` undefined, though neither the standard nor POI lists them as
  built-in. Round 1 probes them last.
- Groups, canvases and connectors inside them are round 2.
- Round 1 ran 2026-11-10, and its results are in `CR-080-notes/phase0-word.json` and `.tsv` (`read_shapes.py`, over
  `pdfpaths.py`): 1,283 probes.
- One file opened but Word couldn't save it (`fp60`). Two more rounds found why, a bisection (`cr080-phase0-b`) and a
  strict pair (`cr080-phase0-c`, Jason: "stage the strict two-file pair for the save failure"). Word can't save a
  shape that has a text box and no `a:xfrm`: "A file error has occurred", and no file is written. It is the Word
  defects register's WD-003 (Jason: "the save failure is worth an entry"). So a producer must write an `a:xfrm` for
  any `wps:spPr` that holds a text box, and phase 2 has docx4j do so.
- Word draws no text in 14 presets: `line`, `lineInv`, `straightConnector1`, the bent and curved connectors,
  `chartPlus`, `chartStar` and `chartX`. Its save turns `line` and the connectors into connectors (`wps:cNvCnPr`) and
  drops their text box.
- Round 2, `cr080-phase0-r2` (3 files, 36 probes: transforms, groups, canvases), ran silent on 2026-11-10. 1,319
  probes are read in all.
- `CR-080-notes/phase0-findings.md` has the rules measured: lines, dashes, arrowheads, fill modes, style references
  by theme, text, groups and canvases. Its §5 lists what they change in phase 2, for Jason. Phase 0 still needs the
  load survey's counts (queued with the portfolio).
- `CR-080-notes/licence-notes.md` (2026-11-10; Jason: "draft the licence notes") has the licence notes. The geometry
  engine is copied from POI 5.5.1 rather than trunk, since trunk's copy needs Java 16. It brings `PaintStyle` with
  it, and `legals/NOTICE` gains the text in its §5. Along the way it found that docx4j's own
  `presetShapeDefinitions.xml` breaks `leftArrow`, and that `PresetGeometries` lacks its 4(b) notice.

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
| `sl.usermodel.ShapeType` (246 constants) | Escher/VML shape type numbers (`o:spt`) to DrawingML preset names, the text shape types 136 to 175 included | **Not ported** (Jason, 2026-11-09). docx4j writes its own table from [MS-ODRAW] §2.4.24 `MSOSPT` (203 shape types by number), mapped to `ST_ShapeType` and `ST_TextShapeType` names and checked against Word (CR-083 phase 0; D6's later CR). POI's is at most a cross-check on the result. |
| `XSLFDiagram` (273 lines) | SmartArt drawn from `dsp:drawing`, converted to a pptx group | **Not ported** (Jason, 2026-11-09). CR-082 writes its own `txXfrm` text-rotation rule from Word's measurements. |
| HSLF | Legacy binary PowerPoint | **No use for docx.** The shape-type numbers it relies on are [MS-ODRAW]'s, which docx4j reads directly. |
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
| LibreOffice (MPL-2.0, C++) | Mature DrawingML import. | A black-box oracle only (CR-080 D11): probe documents converted with `soffice` and the output compared, its source not read. MPL-2.0 is file-level copyleft, and a translation of its code into Java would be covered by it, so docx4j's code is not written from it. |
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
    ([MS-OI29500] 2.1.1311, §12; Word confirmed it in phase 0). From POI 5.5.1 with `2acb5cf66a`, and with
    `PaintStyle` (D1).
  - docx4j's `presetShapeDefinitions.xml` repaired: `leftArrow`'s `y1` back to ECMA's `dy1`
    (`CR-080-notes/licence-notes.md` §3). `org.docx4j.model.shapes.PresetGeometries` replaced by the repackaged
    engine, or given its 4(b) notice.
  - The limits of D10 on every `custGeom`, each tested with a hostile probe, as CR-057 tested its cases.
  - `ShapeGeometry`, tested against phase 0's numbers (every preset's path within 0.1pt of Word's at 100 by 100pt, the
    text rectangles exact).
  - **Gate:** core-tests.
  - **Done 2026-11-10** (Jason: "commit then start phase 1", and asked before phase 2, "Commit both").
    - **The engine.** POI 5.5.1's `sl.draw.geom` in `org.docx4j.org.apache.poi.sl.draw.geom`, 32 of its 34 files, each
      under the Plutext notice, with `2acb5cf66a`, and `PaintStyle` with only its `PaintModifier`
      (`CR-080-notes/licence-notes.md`). POI's `PresetGeometries` and `PresetParser` aren't copied: docx4j's own
      `PresetGeometries` reads the one copy of the definitions into JAXB, and `GeometryReader` takes that, or a
      document's `a:custGeom`, into POI's `CustomGeometry`. docx4j's changes to the copied files, each in its notice:
      - `sqrt` of |x|;
      - the built-in guides `wd12` and `hd10`;
      - an arc of no radius leaves the point where it is;
      - an arc on a flat ellipse runs along it, as Word draws it. The arc's start was `atan2(rx·sin θ, ry·cos θ)`, and
        `sin(180°)` in doubles is 1.2e-16, not 0, so with `ry` 0 it gave 90° for 180°, and `donut`, `leftBrace` and
        `rightBrace` at their limits started a quarter turn out (`fp13-039`, `-056`, `-087`). It is now the converted
        start angle, and where the width is 0 the conversion gives 90° for a vertical angle, not 0.
    - **The API.** `org.docx4j.model.shapes.ShapeGeometry.outlines`, of a `CTShapeProperties`, `CTPresetGeometry2D` or
      `CTCustomGeometry2D`, at the shape's extent in EMU, gives `ShapeOutlines`, in points from the shape's top left,
      y down:
      - its paths, each a `Path2D` with its fill mode (`STPathFillMode`), stroke and `extrusionOk`;
      - its text rectangle, the box where the geometry has none;
      - its connection sites;
      - `getProblem()`: `render.shape.unknown_preset`, `render.shape.bad_geometry` or `render.shape.over_limit` (D9),
        with the box in place of the geometry.
    - **D10's limits, as built.**
      - 1 MiB, counted as characters of the geometry's names, formulas and points, as the XML is parsed before
        docx4j sees it.
      - 1,024 guides and adjust values, a preset's adjustments counted too.
      - 256 paths, and 8,192 commands over them.
      - An arc's sweep of 16 turns, checked as each arc is drawn, as a sweep can be a guide.
      - Every coordinate finite and within ±2^31 EMU in the shape's space: the text rectangle and the connection sites
        too. "Within the drawing's bounds" is read as the 32-bit coordinate space (§7's "an extent near 2^31 EMU"),
        not the shape's box, as a callout's leader reaches past it (`fp10-001`).
      - The most work they allow, 256 paths each in its own coordinates and so each evaluating 1,024 guides again,
        takes 0.5 s.
    - **The definitions, corrected** (licence notes §3).
      - `leftArrow`'s `y1` named an undefined `dy`; it is ECMA's `dy1` again.
      - `pie`'s text rectangle: ECMA's file swaps `t` and `r` (`t="ir" r="it"`), and so did docx4j's and POI's copies.
        Word's glyphs sit in `il`, `it`, `ir`, `ib`.
      - `upArrow`, which ECMA's file lacks (it has `upDownArrow` twice): docx4j's of 2014 had `upDownArrow`'s formulas,
        which hold the one head to half the height. Word draws it as `downArrow` mirrored, its head to the full
        height (`fp13-115`), which docx4j's now is.
      - Otherwise docx4j's file is ECMA's 5th edition's, preset for preset.
    - **`PresetGeometries`** has its 4(b) notice, and is read once by a holder, where its lazy start wasn't safe
      between threads. `legals/NOTICE` names the geometry, POI commit `2acb5cf66a` and `PresetGeometries`.
    - **Against Word.** `ShapeGeometryTest` holds phase 0's paths (`shapes/cr080-word.tsv.gz`, written by
      `CR-080-notes/export_test_paths.py`). It compares fills and strokes as outlines, by the farthest any point of
      either is from the other:

      | Probes | Count | Farthest from Word's |
      |---|---|---|
      | Every preset at its defaults, 100 by 100 and 144 by 72 pt (`fp10`, `fp11`) | 374 | 0.038 pt (the gate: 0.1) |
      | Every preset's adjustments at half and double (`fp12`, `fp13`) | 246 | 0.042 pt, but for Word's three departures below |
      | Custom geometry: each formula, command and path attribute, the built-in guides, `sqrt` of a negative (`fp20`, `fp21`) | 38 | 0.009 pt |
      | Text rectangles: 173 presets with text, at the top left and the bottom right (`fp70` to `fp77`) | 346 | 0.39 pt from where Word's glyph puts it |

      - **The text rectangles can't be exact,** as the gate had it. Word lays its text out on a grid of 1/300 inch
        (0.24 pt), and a glyph shows the rectangle only to within two steps of it. No one rounding of the edges
        fits: rounded inward, 551 of the 692 edges agree to 0.035 pt; the left or top rounded to the nearest step
        with the width or height added, rounded too, fits better, but not all. The placing is phase 2's, with the
        text.
      - **Word departs from the definitions** in the wedge callouts with their tip inside the shape
        (`wedgeEllipseCallout`, `wedgeRectCallout`, `wedgeRoundRectCallout`, with `adj1` and `adj2` at half their
        defaults: `fp12-121` to `-123`). Word puts the tip on the edge and draws no tail, where the definitions notch
        in to it. That is one probe for each, so docx4j keeps the definitions, and the test names the three.
      - A fill with no area paints nothing. Word's PDF has none for `line` and `lineInv`, and one for `diagStripe` at
        100000.
      - `curvedLeftArrow`'s connection site has `ang="cd3"`, undefined in ECMA's file and POI's. A site isn't drawn,
        so its angle is NaN and the shape is drawn.
    - **Hostile and broken geometry,** a test for each limit at it and past it, and for each code: an undefined guide,
      an unknown formula, a command with no current point, a Bézier with too few or too many points, a bad
      adjustment, a `prstGeom` with no `prst`.
    - **Mutants:** 27, against a passing baseline, one rule each, the definitions' three corrections among them; 25
      caught. The two that survived showed code doing nothing, now gone:
      - a branch for an arc of no height, which the conversion already handled exactly, and which showed the first
        account of the arc's fault, `tan(180°)`, to be wrong (`sin(180°)` was the cause);
      - mapping a path's absent `w` to -1, which `ShapeGeometry` reads as not stated already.

      The two arc mutants were run again on the code as it stands: both caught.
    - **The suites:** core-tests 2624 (30 skipped) pass. `ShapeGeometryTest`, 29 tests, takes 7 s, its outline
      distances read through a grid of half a point.
  - **Unmeasured, with docx4j's reading meanwhile** (phase 1's list, for a later round):
    - A guide in a path with its own `w` and `h`: POI's reading, that the guides' `w` and `h` are the path's, which a
      test holds. `fp20-030` has numbers only.
    - The rule by which Word places text on its 1/300-inch grid (phase 2).
    - The wedge callouts' tip beyond one probe each.
    - A `spPr` with both `a:custGeom` and `a:prstGeom`, which the schema doesn't allow: the custom one is taken.
    - What Word does with a geometry past D10's limits, or one that can't be evaluated: docx4j draws the box (D9).
- **Phase 2, drawing a single shape.**
  - `ShapeRenderer` (fills, lines, dashes, arrowheads, picture fills, style references) in docx4j-chart-render.
  - Shapes in the FO, visitor and XSLT alike, with text boxes on top and the geometry's text rectangle used for the
    text. A shape whose `xfrm` is omitted has no text rectangle, as in Office ([MS-OI29500] 2.1.1315), and lays its
    text out in its box.
  - Phase 0's rules (`CR-080-notes/phase0-findings.md` §5, accepted 2026-11-10):
    - an `a:xfrm` written in any `wps:spPr` that holds a text box, or Word can't save the file (WD-003);
    - no text drawn in the 14 presets where Word draws none;
    - the line rules: the 0.14 pt hairline, flat caps and round joins by default, no miter limit from `lim`, the dash
      table, the arrowhead sizes, and compound and inside lines as filled outlines;
    - the fill-mode factors, and the line widths of each theme's style references.
  - **Gate:** the probes within tolerance; corpora unmoved (0 movers; colour only); the register's
    `drawingml-fills-not-painted` closed.
  - **Built, committed 2026-11-10** (Jason: "commence phase 2 when ready", then "Commit both"). Not done: D12 (c)'s
    second step, the `wps` Choice drawn by default (9775 is mended, b399), and its register gate waits on it.
    - **`ShapeRenderer`** (docx4j-chart-render, `org.docx4j.chart.render.api`). It draws a shape's `spPr` and
      `wps:style` at its extent, through `ShapeGeometry`, into a `Drawing`, and as SVG over the bounds of all it
      paints, which can reach past the box.
      - **The fill:** the shape's own where it states one, else its style's `fillRef` through the theme, `phClr`
        the reference's colour. Solid with alpha, gradient, pattern, and picture, stretched (into its `fillRect`,
        cropped by its `srcRect`) or tiled (scale, offset, alignment, flip).
      - **The line:** the shape's own `a:ln` over its style's `lnRef`, attribute by attribute, so that a width alone
        keeps the style's colour (`fp50-012`).
      - **The turn and flips:** the shape's own `a:xfrm` turn and flips, about its centre.
      - `Drawing` gains a stroke of an area's own, a pattern line, any fill paint, and a `Picture` item;
        `SvgWriter` a canvas from a bounds; `Fills` its theme's getter.
    - **What Word does, found in drawing it** (phase 0's PDFs, read again):
      - **Joins by preset.** Word strokes 31 presets with a mitred join where none is stated, and the other 156
        round: `rightArrow` mitred, `leftArrow` round, all the stars, the math shapes, `chevron`, `cube`, `moon`.
        That looks like Office's legacy shape types. docx4j has the 31 as a table.
      - **Fills** are even-odd (the PDF's `f*`).
      - **Rectangles** are the PDF's `re`, from the bottom left, so a rectangle's dashes start there.
      - **Compound lines** are filled bands, their outer corners round: `dbl` 1:1:1, `thickThin` 3:1:1 with the
        thick band outside, `thinThick` 1:1:3, `tri` 1:1:2:1:1. An inside line (`algn="in"`) is all inside.
      - **Arrowheads.** With s the larger of the width and 2pt, a head's length L and width W are 2, 3 or 5 s.
        - A `triangle`'s tip is the end, and the line stops s/2 inside its base.
        - A `stealth`'s notch is s, 2s or 3s back, and the line stops there.
        - A `diamond` and an `oval` are centred on the end.
        - An `arrow` is an open V of the line's width with round ends, its apex mitred so that its outer edges
          meet at the end. Each arm ends 2s, 3s or 4.5s back and 1.25s, 1.75s or 2.5s across from the apex, and
          the line stops at the apex.
        - On a curve a head lies along the chord over its own length.
      - **Dashes with a round cap:** each dash a width shorter, each gap a width longer.
      - **Two path corrections in core's `ShapeGeometry`,** where Word draws otherwise than the definitions read
        literally:
        - a line of no length goes, as the engine appends one before each arc and a cap or join formed round it
          (`rightBrace`, `moon`, the curved arrows);
        - a close straight after a move closes the subpath at its end: the callouts' accent bar is `moveTo`,
          `close`, `lnTo` in ECMA's definitions, and Word draws it as a closed line.
    - **Against Word.**
      - `ShapeRendererTest`, against Word's PDFs of `fp10`, `fp11`, `fp20`, `fp21`, `fp30`, `fp31`, `fp40`, `fp50`
        and `fp51` (662 probes; `shapes/cr080-word-drawn.jsonl.gz`, from `CR-080-notes/export_drawn.py`): each probe's painted
        region and colours. The region is compared by the thickness of where the two paintings disagree, from a
        distance transform at 40 pixels a point. An outline distance was fooled by the edges of no width a Java
        `Area` keeps at a stroke's inner joins.
      - Within 0.085 pt in all but two. An inside line is 0.30 pt off, as Word's sits 0.24 pt further out (its
        1/300-inch grid); a triangle head on an arc is 0.133 pt off. The test names both.
    - **In the FO, by the visitor and the XSLT alike** (`FOShapes`, `XsltFOFunctions`, `docx2fo.xslt`).
      - The drawing is the first child of the shape's container, an absolutely positioned block-container at its
        bounds. FOP paints it before the text that follows, so the text is on top.
      - The text is laid out in the geometry's text rectangle, by the container's insets, and anchored by
        `bodyPr`'s `anchor` (top, middle or foot). docx4j had read no `anchor`, and set every shape's text at its
        top, where Word sets an inserted shape's, `anchor="ctr"`, in its middle.
      - A shape with no `a:xfrm` keeps its text in the box (2.1.1315).
      - Word's 14 presets show no text.
      - An anchored shape with no text is drawn out of the flow, in the same container. One that takes space, and
        an inline one, are phase 4's.
      - `ShapeDrawingFoTest` holds each, by both routes.
    - **The probe gate, on the PDF.** docx4j's PDFs of the seven files, read as Word's are (`read_shapes.py --out`,
      `export_drawn.py --json --out`), against Word's (`ShapeRendererTest`, `-Dcr080.pdfDraws`): all 662 within
      0.067 pt but the two above, the colours Word's. The text in the eight text-rectangle files, 346 probes: within
      0.42 pt of Word's glyph at both corners, where the bottom-anchored text had been 98.75 pt out.
    - **WD-003's rule.** docx4j's own code writes no `wps:wsp`, so there is nothing to give an `a:xfrm` to. A
      document read with a text box and no `a:xfrm` keeps its `spPr` on a save; adding one there would be a repair
      at save, a question for Jason.
    - **D9.** A gap is logged with its code once a drawing: `render.shape.unknown_preset`, `bad_geometry`,
      `over_limit`, and `render.shape.picture_missing`, a picture fill whose picture ImageIO can't read. The
      gate's `events.py` counts FOP's warnings only, so it needs a kind for these; the message writer isn't called
      yet.
    - **Not done, or unmeasured** (phase 2's additions to the list above):
      - a compound line on an open path, its bands mirrored;
      - dashes with a square cap, left as the preset's;
      - a turned shape's text, which stays level;
      - a turned shape's picture fill, which stays square to the page;
      - a pattern line;
      - the `rect` and `shape` gradient paths, drawn as radial;
      - a shape in front of text in an earlier paragraph, painted under it (FOP paints in document order, §5.4).
    - **Gate:** b396 against b395: 0 movers, events equal, every corpus's scoreboard and the probes' identical, no gap
      logged. That is because none of the corpora's 92 shapes (in 34 of 557 documents) reached the new code. By
      default docx4j draws the VML Fallback of a shape in `mc:AlternateContent` (`McSelection`, CR-021 §8.5), and
      Word writes every shape so, beside its `wps` Choice. Drawing the Choice (`docx4j.jaxb.mc.preferChoice=wps`) is
      D12's question:
      - **b397, the Choice drawn,** against b396: 2 movers. `14_en-US_num_tbl_14372` gains a line (0.9802 to
        0.9818); `15_en-GB_sdt_tbl_9775` loses 5 (0.9951 to 0.9707). 9775's loss is the DrawingML text box path's,
        older than phase 2: scored alone with the Choice it is 0.9707 on b395's jars too.
      - **On the page** (`15_en-US_tbl_5075`): the Choice gives Word's dark bar, grey box and rule, where the
        Fallback draws its VML strokes as outlined boxes and no fill. But a white-filled text box that docx4j places
        otherwise than Word now hides body text, as FOP paints in document order.
      - The register: `drawingml-fills-not-painted` stays open (Enterprise, attempts b396 and b397).
      - **9775 mended** (2026-11-10; diagnosed by a Fable 5.1 agent of this session, Jason: "if you can't
        figure it out, run it in a fable session"). The loss is the FOP fork's page obstacles, not the drawing: with
        `docx4j.convert.out.fo.wrapPageAnchored=false` the Choice scores 0.9951 too. Section 2's first paragraph
        anchors three page text boxes wrapped both sides; as DrawingML boxes the fixups make them obstacles (a VML box
        never is: its beside hint has three tokens, the wrap wants four, so CR-032 §4.5's "a DrawingML or VML text box
        likewise" holds only for DrawingML). The fork lays a `both` obstacle's lines on its wider side and combines a
        band's obstacles by the largest start and end intrusions. The two wide boxes chose the end, the third the
        start, the band had -17.4pt, and its empty paragraphs cleared below: the table under them 33.8pt down, its
        first row's text boxes at x 709.8 and off the page (Enterprise CR-001 §6.6 item 63).
        - The workaround: a page text box wrapped `both`, with at least `TEXT_BESIDE_MIN_PT` (72pt) on each side, is
          no obstacle; its text runs under it, as before CR-032 phase 5 (`WordLayoutFixups.anchorTextBox`;
          `WordLayoutFixupsTest.aPageTextBoxWithRoomOnBothSidesIsNoObstacle`, which fails without it). `largest`,
          `left`, `right`, and a box with less room on a side keep the obstacle.
        - Gate b398 (by default) against b396: 0 movers, events equal. b399 (the Choice) against b397: 9775 +5 lines,
          0.9951; nothing else. b399 against b398, what drawing the Choice by default now moves: 14372 +1, nothing
          else.
        - Not measured: whether Word sets a text box's text in both gaps, as it does a float's (CR-032 §3, tables);
          the probe is described in the register's `page-obstacles-opposite-sides`. The fork's fix (combine the
          band's free segments) and phase B's are the fork's.
    - **Mutants:** 25, one rule each, against a passing baseline: 24 caught.
      - Three first survived, and the tests were strengthened until they were caught: the even-odd rule wanted
        `fp20-033`'s nested subpaths, one way round, in the oracle; a shape with no text that wraps wanted one wider
        than 60% of the column, as the fixups take a narrower box out of the flow anyway; a shape painting nothing
        wanted the FO's containers counted.
      - The 25th, a triangle's line stopping s/2 inside its base where it could stop at it, is equivalent for an
        opaque line, as the head covers both. A transparent line would show it, painted twice under the head, and no
        probe has one; the measured rule stays.
    - **The suites:** core-tests 2624 (30 skipped), chart-render 25 (1 skipped, the PDF gate), export-fo 111,
      export-fo-tests 914 (9 skipped; `ShapeDrawingFoTest`'s 6 among them) pass.
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
  **Decided 2026-11-10: (a), as recommended** (Jason, 2026-11-10: "CR-080 decisions as recommended"). Copied from POI 5.5.1 with `2acb5cf66a`, not trunk, for Java 11,
  with `PaintStyle` (`CR-080-notes/licence-notes.md`).
- **D2. Where the code lives.** Geometry in docx4j-core (`org.docx4j.model.shapes`: no Batik, usable by HTML, pptx and
  the text model's outlines); drawing in docx4j-chart-render (it already holds `Fills`, `Drawing`, `SvgWriter` and
  shadows, and docx4j-export-fo depends on it). The module's name stays, since renaming an artifact costs consumers;
  its description becomes "DrawingML drawn to SVG: charts and shapes". Recommendation: yes. The alternative is a new
  module that chart-render depends on, which would move CR-039's published API.
  **Decided 2026-11-10: as recommended** (Jason, 2026-11-10: "CR-080 decisions as recommended").
- **D3. Shapes as SVG in the FO** (`fo:instream-foreign-object`), with the text staying FO. Recommendation: yes. The
  text keeps docx4j's line breaking, fonts and Word layout, and the drawing is vector.
  **Decided 2026-11-10: as recommended** (Jason, 2026-11-10: "CR-080 decisions as recommended"). Turned text stays text, turned, though Word draws it as glyph outlines
  (phase 0 findings §5): it looks the same and stays searchable.
- **D4. Shapes with no text take space and wrap**, in a phase of their own with its own gate (phase 4), through the
  picture path's existing wrap handling (phase 4's dispatch). Recommendation: yes. It is a layout change, and the
  corpora barely exercise it.
  **Decided 2026-11-10: as recommended** (Jason, 2026-11-10: "CR-080 decisions as recommended").
- **D5. HTML output too**, from the same SVG. Recommendation: yes, phase 5.
  **Decided 2026-11-10: as recommended** (Jason, 2026-11-10: "CR-080 decisions as recommended").
- **D6. VML shapes** (`v:shape` with `o:spt` or `v:path`, `v:rect`, `v:roundrect`, `v:oval`, `v:line`, `v:polyline`,
  `v:arc`, `v:curve`). Word reads them only where no DrawingML is written. Under CR-051's word mode docx4j reads the
  DrawingML Choice wherever there is one, so VML matters for documents from Word 2007 and earlier and from other
  producers. Recommendation: **a later CR**, once phase 0 has counted them: through docx4j's own table from [MS-ODRAW]
  §2.4.24 `MSOSPT` (o:spt to preset, with the adjust units mapped and checked against Word) for preset shape types,
  plus a `v:path` parser.
  **Decided 2026-11-10: as recommended** (Jason, 2026-11-10: "CR-080 decisions as recommended"): a later CR, once the load survey's counts (queued) are in.
- **D7. The oracle.** Word's PDFs through the bridge, paths read as numbers (`mutool trace`), then the raster against
  Word's. docxide-pdf's references are Word for Mac's online export, so they are not ours. Recommendation: yes.
  **Decided 2026-11-10: as recommended** (Jason, 2026-11-10: "CR-080 decisions as recommended"). Phase 0 read the paths with `CR-080-notes/pdfpaths.py`, not
  `mutool trace`.
- **D8. docxide-pdf.** Port nothing for this CR; read it as a cross-check; any later port names its source file and
  commit in the file's header and gains a `legals/NOTICE` entry. Recommendation: yes.
  **Decided 2026-11-10: as recommended** (Jason, 2026-11-10: "CR-080 decisions as recommended").
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
  **Decided 2026-11-10: as recommended** (Jason, 2026-11-10: "CR-080 decisions as recommended").
- **D10. Limits against hostile geometry** (CR-057's concern), as rust-office-art's evaluator has them: at most 1 MiB
  of geometry XML, 1,024 guide evaluations, 256 paths and 8,192 path commands per shape, arc sweeps of 16 turns, and
  finite coordinates within the drawing's bounds. Past a limit the shape is drawn as its box and reported
  (`render.shape.over_limit`). Recommendation: yes, the numbers confirmed against the probes and the load survey's
  largest real `custGeom`.
  **Decided 2026-11-10: as recommended** (Jason, 2026-11-10: "CR-080 decisions as recommended"). The probes are within the limits; the load survey's largest
  `custGeom` (queued) confirms them.
- **D11. MPL-2.0 sources are black-box only**, in all four CRs (Jason, 2026-11-09: "make the LibreOffice wording
  black-box only"). LibreOffice is run, not read: probe documents are converted with `soffice` and its output
  compared with Word's and docx4j's. Its source is not read while docx4j's code is written, because a Java translation
  of MPL-2.0 code would be a Modification under MPL-2.0 and would stay MPL. Apache OpenOffice, LibreOffice's
  Apache-licensed ancestor, offers no way round this: its `oox/source/drawingml/diagram/diagramlayoutatoms.cxx` is a
  3.5 KB stub, and the SmartArt layout work came after the fork. Recommendation: yes.
  **Decided 2026-11-10: as recommended** (Jason, 2026-11-10: "CR-080 decisions as recommended").
- **D12. Draw the `wps` Choice by default** (raised in phase 2, 2026-11-10). Word writes every shape in
  `mc:AlternateContent`, a `wps` Choice beside a VML Fallback, and docx4j draws the Fallback by default
  (`docx4j.jaxb.mc.preferChoice`, CR-021 §8.5), so phase 2's drawing reaches no shape Word wrote unless the Choice is
  preferred. Options:
  - (a) Keep the Fallback, and document `docx4j.jaxb.mc.preferChoice=wps`.
  - (b) Prefer the `wps` Choice for drawing by default now: b397's 2 movers, one better and one worse (9775, the
    DrawingML text box path's, older than phase 2), and a white text box placed otherwise than Word's can hide text.
  - (c) As (b), once 9775's loss is understood and mended in the DrawingML text box path, with its own gate.

  Recommendation: **(c)**. It is what makes phase 2 reach Word's documents, and the one regression is a known path's,
  measured on its own.
  **Decided 2026-11-10: (c), as recommended** (Jason, 2026-11-10: "(c) After mending 9775"). Phase 2 stays open
  for it: 9775's loss in the DrawingML text box path, then the Choice drawn by default, each with its gate.

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
- [MS-ODRAW] (the binary OfficeArt format of .doc and .ppt) for §2.4.24 `MSOSPT` only: the shape-type numbers VML's
  `o:spt` uses (D6; CR-083's table).

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
