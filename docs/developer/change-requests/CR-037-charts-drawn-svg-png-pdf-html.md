# CR-037: charts drawn - a chart rendered to SVG and PNG as Office draws it, and drawn in PDF, HTML and pptx output (charts plan phases 7 and 8)

Status: ACTIVE: phase 0 DONE 2026-10-12; phase 1 not started (Jason: "do not commence phase 1
yet").  Drafted on Jason's request ("please draft a CR for phases 7 (rendering) and 8
(integration) of the charts plan.  Do not implement yet."); reviewed the same day by the
docx4j-13 session (Claude Fable 5.1, at Jason's request): approved with nine amendments, all
accepted by Jason ("apply all nine") and applied (§8).  D1 to D9 taken as recommended (Jason, 2026-10-12);
D4's tolerances set from phase 0's baseline (§8); what phase 0 means for phases 1 to 4 is in
§11.  Owner: Jason Harrop.  Drafted with Claude Opus 5.5.

The charts plan (`../docx4j-portfolio/docs/charts_plan.md`) took docx4j's own renderer (its D2)
and put the classic chart types, rendering included, in the open library (its D1 (d)); CR-034
(theme colours), CR-035 (the box, the read model, describe, edit) and CR-036 (authoring) are
done.  This CR draws a chart: from CR-035's read model to `java.awt.Graphics2D`, to SVG through
Batik and to PNG through a `BufferedImage` (plan phase 7), and into docx4j's PDF, HTML and pptx
outputs in the box CR-035 keeps for it, with an API to save any chart as SVG or PNG (plan phase
8).  Office's own drawings are the oracle, measured as numbers: the text and shapes of Word's
PDFs, and the layout Excel's object model reports.

## 1. What docx4j does today (read from the code, 2026-10-12)

- **Every output keeps a chart's box, empty** (CR-035 phase 0, `org.docx4j.model.chart.ChartBox`):
  in FO (visitor and XSLT) an `fo:instream-foreign-object` of the drawing's extent holding an empty
  SVG, with the chart's name as `fox:alt-text` and, for a `wp:anchor`, the anchor hints
  export-fo's `WordLayoutFixups` places a floating picture by; in HTML (visitor and XSLT) an empty
  inline `<svg>` with an `aria-label`; in pptx SVG-in-HTML an empty positioned box at the frame's
  `p:xfrm`; in markdown the chart's data as a table (phase 3).  `ChartBox` was built for this CR:
  "the drawing, when docx4j can make one, goes into the same element", so pagination and placement
  stay as they are once something is drawn.
- **The read model** (CR-035 phase 2, `org.docx4j.model.chart`, public, the ports' oracle):
  `Charts.in` finds every chart, `ChartModel` reads one: groups (kind, bar direction, grouping,
  `varyColors`, gap width, overlap, hole size, first slice angle, markers, smoothing), series
  (name, categories, values by `c:pt/@idx`, format codes, point overrides, data labels, fill and
  line colours resolved through CR-034, *automatic* where the series has none of its own, with the
  style number and the series' index), axes (kind, position, deleted, orientation, min, max, log
  base, units, number format and `sourceLinked`, crossing, tick marks, label position), title and
  legend (with what Office shows where the file leaves it to Office: "Chart Title", the only
  series' name), data labels (with the position Office shows), `dispBlanksAs`.  Each object keeps
  the JAXB it read (`getSource()`), for what the model doesn't carry: text properties (`c:txPr`),
  gridlines' and the chart area's `c:spPr`, manual layouts.
- **Colours**: CR-034's resolver (`org.docx4j.model.color`), every DrawingML transform measured
  against Office; a chart resolves in its host's theme (`ChartRef.getHostTheme()`).  The colour
  style's cycle for automatic colours (`meth="cycle"`, with the variations Office applies from the
  seventh series, CR-036 phase 2 finding 4) is in `ChartDataEditor`, private.
- **Number formats**: a port of POI's `DataFormatter` (`org.xlsx4j.org.apache.poi.ss.usermodel`),
  which the read model's data table and the editor's caches use; Excel's own spellings of the
  built-in formats (`SheetCells.chartFormatCode`, CR-036).
- **Drawing to SVG**: CR-011's route for metafiles.  An SPI in docx4j-core
  (`org.docx4j.model.images.MetafileSvgProvider`, found through `ServiceLoader`), implemented in
  docx4j-export-fo by `BatikMetafileSvgProvider` with Batik's `SVGGraphics2D` (batik-svggen, a
  dependency of export-fo and not of core); text kept as `<text>` under the family names docx4j's
  font mapping gives (the mapping FOP is configured with), outlines on a property; HTML falls back
  to a PNG through ImageIO where no provider is present.
- **Fonts**: docx4j's mapper (`org.docx4j.fonts.Mapper`, `PhysicalFonts`) and the font jars with
  metric-compatible faces (Carlito for Calibri in docx4j-export-fo-fonts-crosextra; Akasia for
  Aptos in docx4j-export-fo-fonts-theme2023, measured equal to Aptos in its advances).
- **Nothing draws a chart.**  A chartex chart's `mc:Fallback` is what the outputs see by default,
  and Word's and PowerPoint's Fallback is a PNG picture of the chart (CR-035 phase 0), which docx4j
  already draws: a chartex in a docx or pptx prints Office's own picture of it.

## 2. What Office draws (measured 2026-10-12, and what the share holds)

1. **The oracles exist for every type.**  On the share (`Office 2016/office_charts`): CR-035's
   `goldens-excel` (Excel's chart PNGs and PDFs) and `goldens-word`; CR-036's `cr036`: Excel's
   PNG of 106 charts (the twelve types, P2's settings, P6's settings on every type, dates, text
   and numbers as categories, other shapes; 720 × 421 px from `Chart.Export`), Word's PDF of its
   insert of each type (P3), PowerPoint's (P4), and Word's and PowerPoint's PDFs of their own data
   edits (P5).  Since CR-036, docx4j makes charts Office draws exactly as its own (106 of 106
   pictures, 28 of 28 PDFs), so new probes can be made by docx4j and drawn by Office.
2. **Word's PDF of a chart is vectors and text** (`p3-column-clustered.pdf`): seven filled and
   four stroked paths, no image; twenty-three words (sixteen text spans) in Calibri, each with its
   box (`pdftotext -bbox`):
   "Chart Title" at x 222.7, y 82.5; the value axis's 0 to 6 at x 42.5, 28.15 pt apart; the
   categories and the legend's entries below.  So docx4j's chart in a PDF can be compared with
   Word's in numbers: each text's string, face, size and box; each path's geometry, fill and line
   (`mutool trace`, read as CR-034's `docx4j-layout-fidelity/tools/office_colours.py` reads it).
3. **Office computes what the file leaves automatic**: the value axis's bounds and unit (Word's
   default column chart, data from 1.8 to 5, is drawn from 0 to 6 by 1), the plot area's place,
   the category labels' rotation and skipping, the legend's layout, the data labels' places, a
   title's wrapping.  The specifications describe what is stored, not this (the plan's §11).
   Excel's object model reports much of it as numbers (the review confirmed it, with the caveats
   phase 0 takes in, §4.1): `Axis.MinimumScale`, `MaximumScale`, `MajorUnit` (the values in use,
   set or automatic),
   `PlotArea.InsideLeft`/`InsideTop`/`InsideWidth`/`InsideHeight`, the title's, the legend's and
   each data label's `Left`/`Top`/`Width`/`Height`, each point's (`Point.Left` ... from Excel 2013),
   `TickLabels.Orientation`, `Axis.TickLabelSpacing`.
4. **A chart is drawn in its host's theme** (the plan's §3): in a docx the document's theme fonts
   and colours (in the P3 fixtures Office 2013's theme: Calibri, accent 1 `4472C4`), not those of
   the embedded workbook (Aptos and the 2023 accents).  The chart's text names theme fonts
   (`+mn-lt`), resolved in the host's theme.
5. **PowerPoint's chart is another chart** (CR-036 phase 0): no title, a transparent chart area,
   text 4/3 larger.  It is drawn by the same rules; its PDFs are the oracle for pptx.

## 3. Corpus facts

None: no document of the four corpora has a chart part (CR-035 §3).  Every gate is on probes; over
the corpora, phase 3's integration is a control that nothing moves.  The fidelity harness's chart
probes (CR-035 phase 0: a chart inline mid-paragraph, alone, floating with each wrapping, in a
table cell, in a header, two on a page, one pushing text over a page; parity against Word 0.9358
with the box empty) measure the integration.

## 4. Design

### 4.1 Phase 0: Office's drawings as numbers, and the tools that compare

- **The probes** (`office-chart-render-probes.ps1`, one Windows VM run; the charts made by docx4j
  where it can, by Excel's `AddChart2` otherwise), each chart at a fixed size, drawn three ways:
  Excel's object model dumps its layout (§2 finding 3) to JSON, Excel exports its PNG, Word saves
  the chart in a docx as PDF (and PowerPoint, for the pptx sets).
  - **One theme for each probe's drawings** (review A2): a workbook docx4j makes has no theme
    part, so Excel would draw it in Office 2023's theme (Aptos, `156082`) where Word draws the
    docx in 2013's (Calibri, `4472C4`), a font and six colours apart by design.  Every probe's
    workbook and document share one theme part (the document's copied into the workbook, or the
    other way), so that Office against Office measures layout, not themes.
  - **The scale**, recorded with each probe: Excel's layout dump is in points of the chart area;
    `Chart.Export`'s pictures are 1.667 px a point (720 × 421 px for 6 × 3.5 inches).
  - **The dump** (the review's caveats): taken once Excel has laid the chart out (a visible
    window, or `Chart.Refresh` after sizing); a point with nothing drawn (a blank) may have no
    geometry; it takes `ChartArea.Width` and `Height` too, and the text sizes in use
    (`TickLabels.Font.Size`, `ChartTitle.Font.Size`), since Excel shrinks automatic text on a small
    chart (R3; measured: it doesn't where the sizes are written, §11).
  The sets:
  - R1 the twelve types, Word's default data (P1, P3 and P4 have them; the layout dump is new),
    and one chart with its `c:txPr` stripped, to see the text Office draws when the chart names
    none (review A5);
  - R2 the value axis's scaling: data from 0 to 1, 0 to 100, all negative, either sign, 95 to 100
    (where Excel stops starting at zero), 1e6, 0.001, a constant series; a log axis; a minimum, a
    maximum or a unit set.  It starts from Microsoft's published rule (review A4; KB 214075, "How
    Excel determines the axis scale": the bounds from the data's range by a table of round
    numbers, the minimum at zero unless the data's range is less than a sixth of its maximum, the
    95 to 100 case) as the hypothesis the probes confirm or correct, and the rule found is recorded
    as a finding with its probes, a pure function the ports can take (§9);
  - R3 size: one chart at four sizes (label rotation, skipping, the legend wrapping);
  - R4 text: long category names, a long title, a legend of eight entries, three text sizes;
  - R5 number formats on the axis and the labels: General, `0.0`, `0%`, currency, thousands,
    `m/d/yyyy`, `d-mmm`;
  - R6 data labels at each position each type allows, and best fit on a pie;
  - R7 blanks: `dispBlanksAs` gap, zero and span on a line and a column;
  - R8 pie and doughnut: first slice angle, an exploded point, twelve slices, labels outside;
  - R9 manual layout: the title, the legend and the plot area moved and sized;
  - R10 a few of Excel's gallery styles (gradients, shadows, as they come).
- **The tools** (`docx4j-layout-fidelity/tools/`, kept): `chart_trace.py` reads a chart's region of
  a PDF (`mutool trace`) into texts (string, face, size, box) and shapes (path, fill, line, box);
  `chart_score.py` compares docx4j's with Office's after aligning on the chart's box: texts matched
  by string (missing, extra, distance), shapes by colour and box, and, against a PNG, the raster
  difference within a tolerance.  The same shape as CR-034's colour tools and the layout harness's
  scoreboards.
- **The baseline, Office against Office**: Excel's layout against Word's PDF of the same chart at
  the same size; Excel's PNG against Word's PDF rasterised.  How far Office is from itself sets the
  tolerances of every later gate (D4).
- **The fixtures** (review A3): the probes' files, Excel's layout dumps and `chart_trace.py`'s
  reading of each Office PDF (texts with their boxes, shapes with their colour and box, a small
  JSON each) join `docx4j-core-tests/src/test/resources/charts/render/` (made from scratch, no
  customer content), so that the gates of phases 1, 2 and 4 run in the build without the share;
  the PDFs and PNGs stay on the share, for the raster comparison, run from the harness.  (As
  built, with Jason's agreement: the probes' xlsx, R1's pptx and R10's styled xlsx, not the docx,
  whose chart is the xlsx's; §11.)

### 4.2 Phase 1: the renderer's frame, drawn on the clustered column

**Where** (D1): a new module, **docx4j-chart-render**, package `org.docx4j.chart.render` (not under
`org.docx4j.model.chart`: one package can't be split across two JPMS modules), depending on
docx4j-core and on batik-svggen and batik-dom (already docx4j-export-fo's, same versions), as
CR-035's D6 foresaw.  docx4j-export-fo depends on it, so PDF output draws charts with nothing to
configure; docx4j-core alone keeps the empty box.

**The SPI** (in docx4j-core, `org.docx4j.model.chart.ChartRenderer`, found through
`ServiceLoader` as `MetafileSvgProvider` is), on the read model (review A7): `check` and `draw`
take a `ChartModel`, its `ThemeColors`, the size in points and a font resolver, and the `ChartRef`
forms are built on them, so that a chart can be drawn from a model without its package and the
oracle fixtures drive the renderer directly.  (Drawing from the model's JSON form, for the MCP
server or a port, would need more than a reader, which doesn't exist: the form, public and
versioned (CR-035's D8), describes the chart's structure, not what drawing needs (it says that an
axis has gridlines, not their line; it carries no text properties and no manual layout), so it
would need a richer version too.  The model takes those from the JAXB it holds.  Left until one is
asked for.)
- **`check`** (review A1) says, as a result object rather than a boolean, what the renderer would
  refuse and what it would draw simplified.  **Structural** features it doesn't draw (a chart
  kind, an axis kind, a shape of data, 3-D, a secondary axis, a combination) refuse: the outputs
  keep the empty box, since a chart drawn wrong is worse than a box.  **Cosmetic** ones degrade:
  effects dropped, a gradient drawn as one colour, a pattern as its foreground's tint, a picture
  fill as a solid; the chart is drawn, each simplification logged and reported.  The reasons
  reach the API and the MCP tool, which can say why a chart is boxed or plain.  (Without it, a
  gallery style's one shadow, say Jason's `bar chart style 2` with its `narVert` pattern fills and
  inner shadows, would keep the box through phase 4.)
- **`draw`**, in points, the chart's top left at the origin; `toSvg` and `toPng` on top.
The open renderer implements it for the classic types it knows; the Enterprise jar implements it
for chartex (the plan's D1 (d), phase 9).

**The module, a checklist** (review A8): docx4j-core's `module-info` gets `uses
org.docx4j.model.chart.ChartRenderer` (CR-033's lesson: without it `ServiceLoader` throws on the
module path), the new module `provides` it, docx4j-export-fo `requires` the new module; the new
module's tests create AWT fonts from the mapper's files headless.  No cycle: the AWT font manager
(`Docx4jDrawFontManager`) is in docx4j-core.  docx4j-bundle doesn't include docx4j-export-fo, so
the new module needn't join it.

**What phase 1 draws** (on R1's clustered column, R2, R3 and R4): the chart area (fill, line,
rounded corners); text (each run of a `c:rich`, or the derived text, with its properties from
`c:txPr` and the theme's fonts, measured with an AWT font made from the very file docx4j's mapper
gives FOP, so widths in the SVG and in the PDF agree; where a chart has no `c:txPr`, Office's
default, 10 pt in the theme's minor font for Word's and Excel's charts, verified on R1's stripped
chart (review A5), while PowerPoint's larger sizes are written in its template's XML, CR-036 §2);
the title (placed, wrapped as Office wraps it); the legend (keys for each series, entries laid out
in rows or a column at each position, the plot area shrinking for it); the plot area's automatic
place, from the space the title, the legend and the axes' labels and titles take; the value axis's
automatic bounds and unit as Excel's (R2 is its oracle), crossing, orientation, log scale; the
category axis, `between` or `midCat`, its labels placed, rotated or skipped as Excel does (R3, R4);
tick marks, gridlines, number formats (the `DataFormatter` port, with Excel's own rules for an
axis's General measured on R2 and R5); the bars (gap width, overlap), their fills and lines resolved
through CR-034, the automatic ones through the colour style's cycle (taken out of `ChartDataEditor`
into a shared helper).

**Gate**: on R1's column and R2 to R4, against Excel's layout and Word's PDF: every text Office
draws drawn, with the same string; the value axis's bounds and unit exactly Excel's; the plot
area, the title, the legend, each bar and each label within D4's tolerance; every colour exact.
A chart drawn simplified is gated by the same oracle, its known cosmetic residual named.

### 4.3 Phase 2: the twelve types

The rest of the plan's D3 (taken, CR-036): bar (categories from the bottom up, as Excel draws them),
stacked and 100% stacked (the percentage axis), line and line with markers (marker symbols and
sizes, smoothing, `dispBlanksAs`), area (over dates, the P1 template's), pie and doughnut (first
slice angle, explosion, hole size, leader lines with outside labels), scatter (two value axes,
markers only), date axes (the base unit's spacing, CR-036's measured `baseTimeUnit`).  Gate: every
type of R1, P1, P3, P4 and P6's dates and shapes, as phase 1's.

### 4.4 Phase 3 (the plan's phase 8): in the outputs, and an API

- **PDF** (visitor and XSLT): `ChartBox` puts the renderer's SVG into its
  `fo:instream-foreign-object`, the element it writes now, unless `check` refuses; FOP draws the
  SVG as vectors and its text as text (D6), through CR-011's route and font mapping.
- **HTML** (visitor and XSLT): the inline `<svg>` it writes now, drawn (D8).
- **pptx SVG-in-HTML**: the frame's box holds the SVG.
- **xlsx**: docx4j has no PDF output of its own for a workbook (documents4j has Excel's); the API.
- **The API**, in docx4j-core, finding the renderer through the SPI: `ChartImages.toSvg(ChartRef)`,
  `writeSvg`, `toPng(ChartRef, dpi)`, `writePng`; the size from the chart's anchor (or given), the
  theme and fonts from its host package.  For any chart in any host, a workbook's included.
- **A picture in the document** (the plan's D5): not in this CR (D5 below).
- **Gate**: the fidelity harness's chart probes against Word's goldens (parity, from 0.9358 with
  the box empty), P3's documents through docx4j's PDF against Word's PDFs (`chart_score.py`), P4's
  through the SVG-in-HTML against PowerPoint's (looser: the HTML's own fonts), the corpora's
  nothing-moves control, docx4j-export-fo-tests, the time a chart takes.

This phase comes before the features of phase 4 (D9), so that drawn charts reach PDFs once the
twelve types are drawn; `check` keeps the box for a chart with a structural feature not yet
drawn, and draws one simplified that lacks only a cosmetic one (A1).

### 4.5 Phase 4: what the settings and the data add

Data labels (each position each type allows, number format, separator, legend key, value,
category name, series name, percentage; best fit on a pie), axis titles (turned on a vertical
axis), number formats linked to the cache's or set, manual layouts (`c:manualLayout` for the title,
the legend and the plot area, inner and outer, edge and factor), deleted legend entries, axis
options (deleted, crossing at a value, tick label positions high, low and none, minor gridlines,
reversed order), the chart area's and plot area's fills and lines, gradients and patterns, the
shadows Excel's gallery styles use, titles of several runs, hidden (filtered) series,
`invertIfNegative`, `varyColors` on a single series.  Gate: P2's settings, P6's on every type, R5
to R10; each feature moves from refused or simplified to drawn.

### 4.6 Left for later (not in this CR)

3-D charts; combination charts and secondary axes (modelled by CR-035, drawn later: the plan's D3);
radar, stock, bubble, surface, bar and pie of pie; trendlines, error bars, data tables, up and down
bars, drop and high-low lines; picture fills; a chart's user shapes (`c:userShapes`); charts in
Office 2007's automatic styles with no `c:spPr` (`c:style` 1 to 48; the probes have none and the
corpora no chart, so they are measured when a document brings one); East Asian and complex-script
text in a chart (a font for each run, as `RunFontSelector` chooses for body text; the fidelity
work's focus is left-to-right European text first) and right-to-left charts (review A9); chartex
(commercial, the plan's phase 9, through the SPI).

## 5. Gates and oracles

| Phase | Oracle | Measure |
|---|---|---|
| 0 | Office against Office: Excel's layout dump, Excel's PNG, Word's and PowerPoint's PDFs of the same chart | the tolerances (D4) |
| 1, 2, 4 | the same, per probe, from the committed numbers (A3) in the build, the raster from the share | texts: every string Office draws, each box within tolerance; the value axis's bounds and unit exact; shapes: colour exact, box within tolerance; the raster difference against Excel's PNG within tolerance; a simplified chart's cosmetic residual named (A1) |
| 3 | Word's goldens of the harness's chart probes; Word's and PowerPoint's PDFs of P3 and P4 | parity; `chart_score.py` on docx4j's PDF; the corpora unchanged |

**The control, in every phase** (as CR-036's): the scoring tools run on Office's own drawing
against itself and against Office's other drawing of the same chart (Excel against Word), so that a
score tells docx4j's error from Office's own variation.

## 6. Phases

| Phase | What | Depends on | Effort |
|---|---|---|---|
| 0 | §4.1: the probes and the layout dump (Office runs), the tracing and scoring tools, the Office-against-Office baseline | CR-035, CR-036 | 3-4 days + Office |
| 1 | §4.2: the module, the SPI, text and fonts, title, legend, plot area, axes and their scaling, number formats, the clustered column | 0 | 9-12 days |
| 2 | §4.3: the other eleven types, date axes | 1 | 7-10 days |
| 3 | §4.4: in PDF, HTML and pptx output; `ChartImages` | 2 | 4-6 days + Word |
| 4 | §4.5: data labels, axis titles, manual layouts, the rest of the settings | 2 | 6-9 days |

29-41 working days (the plan's 24-36 for phases 7 and 8, with phase 0's measuring and tools added),
plus Office runs.  Each phase ends with CHANGELOG, this CR's record, the registry entry, and the
hand-offs it triggers.

## 7. Risks

- **Office's layout rules are unpublished.**  The bounds of an axis, the plot area's margins, when
  labels turn or skip: measured, not specified, and measured only as far as the probes reach.
  Mitigation: the layout dump gives numbers, not pictures; each rule is recorded as a finding with
  its probe, as the layout-fidelity work records Word's; `check` keeps a chart docx4j can't draw
  out of the outputs.
- **Text widths.**  Office measures text with its own engine; docx4j with AWT and FOP.  The fonts
  are the same files (or metric clones) and the widths agree to a fraction of a point for Calibri
  and Aptos (the fidelity work's measure), but a decision taken at a width (a label turned, a title
  wrapped) can still fall the other way at the margin.  Mitigation: R3 and R4 probe those margins.
- **Excel and Word may draw the same chart differently** (screen rendering, PDF export).  Phase 0
  measures it; Word's PDF is the oracle for docx output, Excel's layout for the API's drawing of an
  xlsx chart.
- **SVG text in FOP** needs the families FOP resolves; CR-011 and CR-001's non-embedded fonts fix
  made metafile text work, and the chart takes the same route.
- **A public surface the Enterprise jar builds on** (the plan's D1): its chartex renderer will want
  the axis, legend, text and number-format code.  D7 keeps all of it internal in this CR.
- **Size.**  29-41 days is large; each phase stands alone (phases 1 and 2 already put a drawing
  where a box is), and `check` lets a phase ship what it draws well.
- **Headless and JPMS**: AWT fonts headless, `module-info` for the new module; the metafile route
  met these, and §4.2's checklist (A8) names them.

## 8. Decisions (owner: Jason; recommendations)

**Reviewed** 2026-10-12 by the docx4j-13 session (Claude Fable 5.1, at Jason's request): approved,
with nine amendments, **all accepted** (Jason: "apply all nine") and applied:
- **A1** `check` two-tiered, structural features refused and cosmetic ones drawn simplified, its
  reasons a result the API and the MCP tool report (§4.2);
- **A2** each probe's drawings in one theme, and the scale recorded (§4.1);
- **A3** the oracle's numbers committed, so the gates run in the build without the share (§4.1,
  §5);
- **A4** R2 starts from Microsoft's published axis-scale rule as its hypothesis (§4.1);
- **A5** the text Office draws where a chart names none, 10 pt in the minor font, verified (§4.1,
  §4.2);
- **A6** twenty-three words in §2's PDF, not sixteen (sixteen spans);
- **A7** the SPI on the read model, with the `ChartRef` forms on top (§4.2);
- **A8** the module's checklist: `uses`, `provides`, `requires`, headless fonts (§4.2);
- **A9** East Asian, complex-script and right-to-left chart text named as left for later (§4.6).
The review confirmed D1 (no module cycle; the bundle unaffected), the oracle and scoring design
with A2 and A3, that Excel's object model reports the computed layout (its caveats are in §4.1),
D9's order and §4.6's scope, and found nothing the plan expects that the CR misses.

**Taken** 2026-10-12 by Jason: D1 to D9 as recommended ("I accept the recommendations for
decisions D1 to D9"), with phase 0's departure from §4.1 (the fixtures without the docx: "the
departure from §4.1 is all right").

- **D1. Where the renderer lives**: a new module, docx4j-chart-render (package
  `org.docx4j.chart.render`), with batik-svggen and batik-dom, which docx4j-export-fo depends on;
  or inside docx4j-export-fo; or the drawing in docx4j-core (AWT only) with the SVG through an
  export-fo SPI.  Recommendation: the new module (CR-035's D6): core stays without Batik, a user
  who wants a chart's PNG or SVG needs neither FOP nor the FO output, and PDF output gets charts
  with nothing to configure.
- **D2. The oracle**: Excel's layout dump and Word's PDFs, as numbers, first; the raster difference
  against Excel's PNGs second, since anti-aliasing and text rasterising differ by design.
  Recommendation: yes.
- **D3. The first cut's scope**: §4.2 to §4.5, the twelve types with what their settings add;
  §4.6 later.  Recommendation: yes.
- **D4. Tolerances**: set from phase 0's Office-against-Office baseline (for instance, a text's box
  within the distance Excel's and Word's drawings of one chart differ by), recorded in the CR
  before phase 1's gate is run, not tuned to docx4j's results.  Recommendation: yes.
  **Set** 2026-10-12 from the baseline (§11), each Office's own spread rounded up:

  | Measure | Tolerance | Office against Office |
  |---|---|---|
  | each text Office draws | the same string, font family and colour | the same |
  | a text's size | within 0.13pt of Word's PDF | Word draws on a 0.24pt grid (±0.02); PowerPoint as laid out |
  | a text's place (its centre) | within 0.5pt of Office's | Office's text in its own boxes: ±0.31pt |
  | geometry: the plot area's inside, each bar, marker, line or area vertex, slice point, gridline | within 0.25pt | Office's drawing against its layout: 0.00 (plot area), 0.13 (points), 0.02 (slices) |
  | the value axis | bounds, unit and labels exactly Excel's | exact |
  | colours (solid fills and lines) | exact | exact |
  | the picture against Excel's PNG, at its scale | at most 0.7% of pixels off by more than 32 of 255, 0.15% by more than 64 | 0.65% and 0.12% at most (medians 0.13%, 0.01%) |

  Excel's layout and Word's are identical (0.00), so the layout oracle is one; a drawing that
  Office makes a picture of (style 258's slices) is held to the picture's tolerance only.
- **D5. A picture written into the document** for consumers that can't draw charts (the plan's
  D5): Office writes none for a classic chart.  Recommendation: not in this CR; `ChartImages`
  gives a caller the picture to insert, and a method for it can follow if one is asked for.
- **D6. Text in the SVG**: as text, under the families docx4j's mapping gives FOP, so a PDF's chart
  text is searchable and selectable; outlines on a property, as for metafiles.  Recommendation:
  text.
- **D7. The public surface**: the SPI (`ChartRenderer` and its check result, in core) and
  `ChartImages` public; the renderer's insides not exported from the new module until the Enterprise
  jar needs them (phase 9), when what it needs is promoted and kept stable.  Recommendation: yes.
- **D8. HTML**: the inline SVG (as metafiles), or a PNG data URI.  Recommendation: SVG, with PNG
  on a property.
- **D9. Order**: the outputs (phase 3) after the twelve types and before the settings, `check`
  keeping the box for what isn't drawn yet (and drawing simplified what lacks only a cosmetic
  feature, A1); or the outputs last.  Recommendation: before the
  settings, so that drawn charts reach PDFs a phase sooner.

## 9. Hand-offs

- **Enterprise**: chartex rendering (the plan's phase 9) implements `ChartRenderer`; what it needs
  of the open renderer is promoted then (D7).
- **docx4j-mcp**: a tool returning a chart's PNG or SVG through the Java server (the plan's phase
  10).
- **docx4j-core-ts, docx4j-python**: rendering stays Java's (the plan's phase 10); the ports may
  take the axis-scaling rule (a pure function, measured here) for their own describing.
- **The layout-fidelity work**: the chart probes' parity moves in phase 3; findings about FOP's
  drawing of the SVG go to the fidelity register, by cause, as other FO findings do.

## 10. Effort (rough)

29-41 working days, plus Office runs: one for phase 0's probes (Excel, Word, PowerPoint), and a
Word run of the harness's chart probes in phase 3.

## 11. Implementation record

### Phase 0 (2026-10-12), done

- **The probes**: `org.docx4j.fidelity.corpus.ChartRenderProbes` (docx4j-layout-fidelity), 102
  charts made by `ChartCreator`, seeded, each an xlsx and a docx, R1's twelve types a pptx too
  (217 files, 4.9 MB), on the share `office_charts\cr037\probes` with `probes.tsv`.  R1 15 (the
  twelve types in Word's default data, the stripped `c:txPr` (A5), a column and a pie in the 2023
  theme), R2 21 (KB 214075's cases and the edges of its sixth: data 49, 50 and 51 to 60), R3 8
  (Word's column and an eight-category one at four sizes), R4 9, R5 9 (seven formats on the axis
  and the labels, two date formats on categories), R6 16 (each position on the column, the
  stacked column, the line with markers, the pie), R7 6, R8 7, R9 4, R10 7 (Excel's gallery
  styles 202, 204, 207, 209, 213 on the column, 254, 258 on the pie, applied by Excel and by Word
  on the VM, the styled files kept).  Settings `ChartCreator` doesn't make (an axis's bounds,
  unit and log base, label positions, `dispBlanksAs`, slice angles and explosions, hole size,
  manual layouts, text sizes) are written into each host's chart part alike.
- **One theme (A2), and one chart**: every probe's docx, xlsx (which docx4j makes with none) and
  pptx carry the same theme part, Office 2013's (Calibri, accent 1 `4472C4`) or, for two probes,
  2023's.  Each probe's xlsx and docx charts were compared: 101 of 102 the same chart, but for
  what the host adds (the xlsx's `c:printSettings`, the docx's `c:externalData` and Word's pie
  points' `c16:uniqueId`s); the one left, the scatter, differs in its series' line width, a line
  with no fill.  A docx's chart is inline at 36pt, 36pt on a page of its size plus margins; a
  pptx's at 72pt, 72pt.
- **The VM script** `tools/office-chart-render-probes.ps1` (RUN.txt on the share): Excel sizes each
  chart, applies an R10 style, refreshes, exports the PNG, dumps the layout (§2 finding 3's
  properties, each point's box, a pie's `PieSliceLocation`s, data labels, fonts) to JSON and dumps
  it again after a second refresh; Word saves each docx as PDF and dumps its own chart's layout;
  PowerPoint the same for R1's pptx.
- **The tools**: `tools/chart_trace.py` (a chart's box of a PDF as texts and shapes, each bar,
  slice, marker and gridline a part with its vertices, curves sampled to 0.02pt; a shading's or
  picture's box and outline from its clip) and `tools/chart_score.py` (`traces`, `layout`,
  `raster`, `baseline`, `fixtures`; README's "Chart render probes and tools").  The control,
  Word's PDF of its own chart against Word's PDF of docx4j's identical chart (CR-036's P3: column,
  pie, doughnut, line with markers, area, scatter; and PowerPoint's P4): every text and part
  matched, every distance 0.00, before and after the run's changes to the tools.
- **The run** (Jason's, 2026-10-12, Microsoft 365 build 16.0.20430): Excel and Word on all 102
  probes, PowerPoint on R1's 12; nothing failed; no second dump moved (the layout was settled at
  the first).  One defect of the script: a helper's parameter named `$s` hid the series' variable,
  so the series' own properties (name, fill, values, line, markers) are null in the dumps; each
  point's are there.  Fixed after the run; nothing in phases 1 to 4 needs them (the file has them),
  and `-Apps excel` would fill them in.
- **What the run taught the tools** (each change checked against the control): Office's frame
  (finding 1); the legend's line keys aren't gridlines (lines at least half the longest of their
  direction are); a pie's points by their `PieSliceLocation`s on the drawn outline (a doughnut's
  centre is in its hole); an area's and a line's points (boxes of no width) by the nearest vertex,
  a scatter's by its marker's centre; Excel's box for a blank point is far off the chart (no
  geometry); a zero bar isn't drawn; a deleted axis has no labels though Office reports its
  bounds; a category axis over dates has bounds too; date-formatted value axes counted, not
  parsed; kerning closes up more than 1pt at 18.6pt and a style's capitals are letter-spaced (a
  space only where the gap passes 0.3 of the size; Word writes its spaces).
- **The baseline** (`chart_score.py baseline`; `baseline.tsv` and `baseline-summary.txt` on the
  share), Office against Office:
  - Excel's layout and Word's are the same numbers: with finding 1's frame, 102 of 102 dumps
    identical (the pies' `PieSliceLocation`s follow the points' frame).  PowerPoint's chart is
    another chart (CR-036) but laid out by the same rules and frame.
  - Office's drawing against its layout (Word's PDF against the dump): the plot area's inside
    against the gridlines and axis lines drawn, 0.00pt on every edge (84 charts); each bar,
    marker, line and area vertex within 0.13pt (100 charts; PowerPoint's 0.06pt); each slice's
    locations on its drawn outline within 0.02pt; every value axis's bounds, unit and labels as
    Excel reports them; text: a title's and a data label's drawn centre 0.1pt across and about a
    tenth of its size (0.104 to 0.106) down from its box's centre, within ±0.31pt (287 texts; the gallery styles' padded
    labels apart).
  - Excel's picture against Word's PDF drawn at its scale (1.667 px/pt, 720 × 420 for 432 × 252pt):
    pixels off by more than 32 of 255, median 0.13%, p95 0.30%, at most 0.65%; by more than 64, at
    most 0.12%.
  - Set aside: Excel's blank points (no geometry), the zero bars, style 258's slices (pictures).
  D4's tolerances (§8) are these, rounded up.
- **The fixtures** (A3, with the departure Jason agreed): `docx4j-core-tests/src/test/resources/
  charts/render/` (3.9 MB): `probes.tsv`, the 102 xlsx, R1's 12 pptx, R10's 7 Excel-styled xlsx,
  Excel's 102 and PowerPoint's 12 layout dumps (nulls dropped, numbers to 0.001), the traces of
  Word's 102 and PowerPoint's 12 PDFs; README there.  `ChartRenderFixturesTest`: every probe has
  its files, and the read model agrees with Office on all 102 charts and 12 slides (the title it
  says Office shows, the legend, the series, the first series' solid colour, resolved by CR-034
  in the probe's theme).  The docx, the PDFs, the PNGs and the baseline stay on the share.  No
  CHANGELOG entry: nothing in the library changed.

**Measured** (findings, each with its probes):

1. **Office's frame.**  The object model reports a chart's elements (plot area, titles, legend
   and its entries, axes, data labels) from an origin 4pt inside the chart's box; points from that
   origin in Excel, from the box itself in Word and PowerPoint (all 102 probes, 12 slides).  Word
   and PowerPoint report `ChartArea.Left`, `Top` as 4; Excel its `ChartArea` 5pt narrower and
   shorter than the chart.  A legend's key is drawn 0.5 to 0.7pt in from the box reported for it.
2. **Text sizes in Word's PDF** are the nearest multiple of 0.24pt (1/300 in), ±0.02: 7 → 6.96,
   9 → 9.12, 12 → 12.00, 14 → 13.92, 16 → 16.08, 18 → 18.00 (and a paragraph's 11 → 11.04).  Not
   0.48, as this record first said from 9.12, 13.92 and 11.04 alone.  The layout is at the
   nominal size (the dumps say 9 and 14); PowerPoint's PDF draws the size as laid out (12, 18.6).
3. **No shrinking**: text keeps its size on the 180 × 108pt chart (R3), so §4.1's caveat doesn't
   arise where the sizes are written (every chart Office or docx4j makes writes them).
4. **No `c:txPr`** (A5): 10pt in the theme's minor font, as the review said, but black (`000000`,
   where the style's text is `595959`), and the title 18pt bold.
5. **The value axis (R2), KB 214075 confirmed, with its edges**: the minimum is zero only where
   the data's range is more than a sixth of its maximum (49 to 60: 0; 50 to 60, exactly a sixth:
   44); else the data's minimum less half its range, down to the unit (50 to 60: 44 by 2; 51 to
   60: 46; 95.2 to 99.4: 93 by 1); the maximum is the data's plus a twentieth of its range, up to
   the unit (every probe: 5 → 6, 10 → 12, 0.93 → 1, 4.9e6 → 6e6, the stacked 12.3 → 14); data of
   either sign is padded a twentieth at both ends (−35 to 60: −40 to 80 by 20); all negative is
   mirrored (−93 to −12: −100 to 0); a constant 5 gives 0 to 6, all zeros 0 to 1 by 0.1; a log
   axis 1 to 10,000 by 10.  Not in the KB: **the unit depends on the plot's height** (R3: Word's
   data 0 to 10 by 10 at 180 × 108, 0 to 6 by 1 from 288 × 168 up; eight categories by 5, by 1 and
   by 0.5 as the chart grows), and **a maximum set moves the automatic minimum** (150: −10, by 20).
6. **Effects in Word's PDF**: gradients are vector shadings in the shape's clip (styles 207,
   254); shadows and glow are pictures (204, 209, 213); 3-D bevels make the shape a picture
   through a soft mask (258, every slice).
7. **A pie's point box** spans its radii's outer ends and the centre, not its arc;
   `PieSliceLocation` 1 and 3 are the radii's outer ends, 2 the arc's middle, 5 the centre, 4 and
   6 the radii's middles, 7 to 9 a doughnut's inner points.  `TextFrame2`'s font size for a
   legend or data labels mostly reads 18 whatever the text's size (196 of 220 labels, 98 of 101
   legends, all of them 8 to 10pt); `Font.Size` is right.
8. **Placement**: PowerPoint puts a chart 0.04pt off its frame (99.96 for 100); Word draws the
   chart area's border last and an invisible 0.14pt stroke of the box first.

**What phase 0 means for the design** (advice for Jason, 2026-10-12; nothing changed in §4 yet):

- **One layout, one oracle.**  Excel, Word and PowerPoint lay a chart out by the same rules to the
  same numbers; a host changes the chart's XML (PowerPoint's template), not the drawing.  The
  renderer needs one layout for every host, Excel's dump is the layout oracle for docx and pptx
  too, and Word's PDF checks only what drawing adds (sizes, colours, z-order).  D2 stands, simpler.
- **The tolerances are Office's precision, which is near exact** (D4 above), so §7's "text
  widths" risk becomes the main one: a rule docx4j has that isn't Office's will show as a miss.
  Advice: keep them (that is D4), let each gate report the share of elements within tolerance as
  well as pass or fail, so a phase's progress shows; if a phase can't meet them, decide then with
  the numbers rather than loosen them now.
- **The plot area and the axis scale must be laid out together** (finding 5): the unit depends on
  the plot's height, the plot area on the labels' widths, and those on the unit and the format.
  Phase 1's layout is a fixed point (labels for a trial unit, the plot area, the unit again), not
  a sequence.  R3's four sizes show it but don't pin it: advice, a probe set of one chart at ten
  or so heights (an Excel-only run of a few minutes) at the start of phase 1, to find the rule
  for the unit (labels' height and spacing against the axis's length).
- **The axis rule handed to the ports** (§9) is still a pure function, but of the axis's length
  and its labels' size as well as the data.
- **Effects** (A1, §4.5): Office itself draws gradients as vector shadings and makes pictures of
  shadows, glow and bevels.  So in phase 4 a gradient can be drawn as a gradient (SVG gradients,
  which Batik and FOP draw) rather than "as one colour", and a shadow or glow drawn as a picture
  is what Office does, not a degradation; A1's tiers stand, the targets move.
- **Text defaults** for phase 1 (findings 2 to 4): lay out at the nominal size (Word's 0.24pt grid
  is its PDF's, not its layout's, and is within the size tolerance), no shrinking, and A5's
  defaults with black text and the 18pt bold title.
- **The frame** (finding 1) is phase 1's starting point: elements placed from 4pt inside the
  chart's box.
- **The read model needs nothing more for phase 1**: it agrees with Office on every probe; the
  automatic colour cycle (series with no `c:spPr`, out of `ChartDataEditor` as §4.2 says) and a
  gradient's colours (phase 4) are as planned.
