# CR-037: charts drawn - a chart rendered to SVG and PNG as Office draws it, and drawn in PDF, HTML and pptx output (charts plan phases 7 and 8)

Status: ACTIVE: phase 0 DONE 2026-10-12; phase 1 DONE 2026-10-12 (Jason: "close phase 1 then
start phase 2"): the module, the SPI and the clustered column, 136 of the 141 probes drawn within
tolerance in every element (§11); phase 2 DONE 2026-10-13: the other eleven types, smoothing
and date axes, 328 of the 338 probes drawn within tolerance in every element and Excel's axes at
every swept size but four (§11); phase 3 DONE 2026-10-13 (Jason: "close phase 3"): charts drawn
in PDF, HTML and pptx output, `ChartImages`; P3 14/14 through PDF on both pathways, P4 13/13
through SVG-in-HTML, the box probes and the corpora unmoved (§11); phase 4 IN PROGRESS 2026-10-13:
data labels, axis titles, manual layouts, axis options, fills and effects drawn, R15 and R16 run,
529 of the 582 probes drawn within tolerance in every element, the rest named (§11).  Drafted on Jason's request ("please draft a CR for phases 7 (rendering) and 8
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
  | a text's size | within 0.15pt of Word's PDF | Word draws on a 0.24pt grid (±0.02: 9pt is 9.12 or 9.14); PowerPoint as laid out |
  | a text's place (its centre) | within 0.5pt of Office's | Office's text in its own boxes: ±0.31pt |
  | geometry: the plot area's inside, each bar, marker, line or area vertex, slice point, gridline | within 0.25pt | Office's drawing against its layout: 0.00 (plot area), 0.13 (points), 0.02 (slices) |
  | the value axis | bounds, unit and labels exactly Excel's | exact |
  | colours (solid fills and lines) | exact | exact |
  | the picture against Excel's PNG, at its scale | at most 0.7% of pixels off by more than 32 of 255, 0.15% by more than 64 | 0.65% and 0.12% at most (medians 0.13%, 0.01%) |

  Excel's layout and Word's are identical (0.00), so the layout oracle is one; a drawing that
  Office makes a picture of (style 258's slices) is held to the picture's tolerance only.
  **Corrected** 2026-10-12, at phase 1's first gate run: the text size was set at 0.13pt, half the
  grid, but Word draws 9pt at 9.14 as well as 9.12 (191 texts), so Office's own spread is
  0.12 + 0.02 = 0.14pt and the tolerance, by the rule above, 0.15pt.  An error in reading
  Office's numbers, corrected from Office's numbers, not from docx4j's.
  **Pictures, per probe** (2026-10-12, R12): on dense charts (twelve categories in 144pt, bars
  two pixels wide, labels turned) Excel's picture and Word's PDF differ by up to 0.85% and
  0.32%, more than the 0.7% and 0.15% set from R1 to R10.  So a probe's picture tolerance is the
  larger of those and 1.2 times Office's own difference on that probe (the baseline's), the rule
  above applied probe by probe.
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

**R11** (the value axis's unit against its length, run by Jason the same evening, 63 probes:
Word's column, eight categories and one series with no title or legend at fifteen heights, 14pt
text at eight, a bar chart at ten widths; nothing failed; the series' own properties in the dumps
now): the baseline holds over all 165 probes (points within 0.13pt, pictures 0.65%, every value
axis agreeing).  The rule (finding 9, below), on the 106 column probes: 103 at once, the other
three (a constant series, all zeros, one at an edge) by its refinements.

9. **The value axis's unit**: the finest of 1, 2 and 5 times a power of ten that gives at most ten
   intervals and whose labels, one line each, fit the axis's length with 4.87pt to spare ((n + 1)
   lines <= length + 4.87; measured 4.66 to 5.08); where none does, the coarsest that gives one
   interval.  Word's data never goes to 0.5 at any height (eleven intervals); eight categories
   (0 to 4.7) by 0.5 from 216pt high.  A constant series pads from zero (5: 0 to 6), all zeros is
   0 to 1 by 0.1.  **The unit is chosen with one line of category labels**: R3's eight categories
   at 288pt wide are turned 45 degrees, so the plot is shorter than its unit was chosen for (by a
   one-line band the plot is exactly the 84.84pt of Word's column at that size).  A level value
   axis (the bar chart) needs wider spacing (24.75pt refused, 30.75 taken): phase 2's.
10. **Word draws category labels turned 45 degrees as glyph outlines** (filled paths in the
    labels' colour), not text; turned 90 degrees, as text.  (First read, from R3, as "dropped":
    the trace looks for text.  Corrected on R12.)  For those, Excel's layout and picture, and
    Word's outlines as ink, are the oracle.

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

### Phase 1 (2026-10-12), done: the renderer's frame on the clustered column

- **Built**: in docx4j-core (`org.docx4j.model.chart`), `ChartRenderer` (the SPI: `check`, `draw`
  on a `Graphics2D`, `toSvg`, `toImage` in core with AWT alone; found through `ServiceLoader`,
  `uses` in core's module-info, A8), `ChartRenderCheck` (A1: refused and simplified, with
  reasons) and `ChartFonts` (a chart's theme fonts, `+mn-lt` and `+mj-lt`, made AWT fonts from the
  files docx4j's mapper gives FOP: Calibri is Carlito, measured to Calibri's advances exactly).
  A new module, **docx4j-chart-render** (`org.docx4j.chart.render`, nothing exported, D7):
  `ClassicChartRenderer` (provides the SPI; SVG through Batik's `SVGGraphics2D`, text as text in
  the mapped family, D6), `ChartLayout` (the rules of §11), `AxisScale` (finding 9, R2),
  `Drawing` (what is painted, and the boxes Office reports).  In the reactor; docx4j-export-fo
  depends on it and `requires` it (A8), drawing nothing with it until phase 3.  `check` draws a
  clustered column and refuses the rest (other types, data labels, axis titles, manual layouts,
  secondary axes, 3-D) until the phase that draws it.
- **The oracle moved** beside the renderer: `docx4j-chart-render/src/test/resources/charts/render/`
  (5.9 MB with R11), with `ChartRenderFixturesTest`.
- **The gate** (`ChartRenderGateTest`, `RenderGate`, D4's tolerances): each probe drawn, its layout
  against Excel's (title, legend, entries, keys, plot area, inside, every point, the value axis)
  and its drawing against Word's PDF (each text: string, centre, size, colour, face; each shape:
  colour and box).  First run: 86.5% of the elements; now **86 of the 95 probes drawn are within
  tolerance in every element** (94.0% of 10,229 elements), and the gate fails the build if any of
  them regresses.  Open (each named in the test): R3's and R11's small plots (eight) and R2's set
  maximum (one).  Pictures (rendered at Excel's 1.667 px/pt): 0.04% to 0.33% of the pixels off by
  more than 32 against Excel's PNG on the passing probes (tolerance 0.7%), indistinguishable by
  eye.  `ChartRendererSpiTest`: found through the SPI on the module path, `check`'s refusals, the
  SVG's text in Carlito, the picture.
- **Measured on the way** (each now a rule in `ChartLayout`, with its probes):
  - the frame: the title 2pt into the inner frame, centred, its box its text plus 3pt either side
    and its line plus 1.5pt above and below; the plot area 7pt from the inner frame's edges and 11pt
    from the title and the legend; the legend 2pt from its edge;
  - the legend: entries a line high, each its key (0.45 of a line, inset half a key) twice over
    plus its text; a horizontal legend its entries plus a fifth of each, spread evenly before,
    between and after them (one, two, three and eight entries, Calibri and Aptos); a vertical
    one centred on the plot area, its entries 3pt wider, rows a line plus 5.9pt; keys drawn 0.75pt
    right of their reported box and 0.055 of the text's size lower;
  - the axes: the widest value label's left edge 6.53pt from the chart's edge, the inside 0.7575 of
    a line after its right edge; the inside's top dropping only where the top label would stand
    more than 6pt above the plot area; the category labels' band 1.855 x size - 4.5pt (plus a line
    for each wrapped line), labels wrapped at the category's width, turned 45 degrees where a
    word won't fit; the band only where the category axis is at the plot's bottom (with data of
    either sign the labels hang 14.5pt below the zero line, no band);
  - kerning: text at or above its `kern` size (Office writes 1200) is kerned: R4's long title is
    Carlito's kerned advances to 0.01pt (333.348 against 334.216 plain);
  - a set bound anchors the ticks: a set maximum of 150 by 20 gives -10 (and a set minimum the
    maximum likewise); bars aren't clipped by the scale but by the plot's inside (Excel and Word
    alike hide a bar below a set minimum; Word's PDF has its path, clipped);
  - a blank shown as zero (`dispBlanksAs` zero) is a point of no height.
- **R12** (the category labels as the room for them shrinks: 31 probes, four category counts at
  five widths, two at four heights, one-word labels) is on the share for an Office run: Office
  caps the labels' band on a small plot (R3 at 180 x 108: overlapping on one line; eight
  categories: turned and cut to "C..."; R11 at 90pt high: the band 1.45pt short), the rule for
  which R3 and R11 don't pin.
- **R12** (run by Jason the same night, 31 probes, nothing failed; baseline over all 196 probes:
  points within 0.13pt, every value axis agreeing; pictures as above):
  - **the category labels' ladder**: on one line where each fits its category; wrapped where each
    word does; turned 45 degrees where the category is at least a line / sin 45 wide (15.54pt at
    9pt); turned 90 where it is a line wide; else every second label, every third ..., trying
    again (twelve categories in 144pt: every second, turned 45).  The band: wrapped, a line a line;
    turned 45, (the widest + a line) x sin 45; turned 90, the widest; each plus 0.634 x size - 4.5.
    Every R12 probe's band is Office's.
  - **a small plot caps the band** at 0.56 of the plot area's height: wrapped labels go back on
    one line, overlapping (R3's 180 x 108); turned ones are cut to the band of one line (at least
    a character, and an ellipsis: "C..."); a line too tall overlaps the gap below (R11 at 90pt).
  - **the axis's maximum is padded from the axis's minimum**: a twentieth of (the data's maximum
    less the axis's minimum), not of the data's range (R12's 1.1 to 4.8 is 0 to 6 by 1, Office's;
    by the data's range, 0 to 5 by 0.5).  With the length taken with one line of category labels,
    the rule now agrees with Office on all 137 column probes.
  - **a label turned 45 degrees**: its rotated line's top corner on the labels' top line, its
    end's middle over the category's centre (fitted to Word's outlines on seven labels: 0.1pt).
  - **labels turned 45 degrees push the plot's inside right** where the first would reach past
    the chart's left: the first category's centre lands 32.0 to 33.8pt into the inner frame
    (by chart width and category count), and the first label's ink 7.6 to 9.4pt from the chart's
    edge, neither constant: the rule is not found yet (R13 found it, below).
- **The gate after R12**: 126 probes drawn, **111 within tolerance in every element** (94.1% of
  13,253 elements), the ratchet holding them.  Open (named in the test): the 45-degree push
  (twelve probes), a plot 1pt high (two: Office's inside 0, ours 0.46) and R2's set maximum (one).
  Pictures: 109 of the 111 within their tolerance (0.04% to 0.65% off Excel's); the two others
  are the cut-short labels ("C..." about a point low; 0.81% and 0.65% against 0.7%).
- **R13** (how far labels turned 45 degrees push the plot right; 15 probes, Jason's run, nothing
  failed; baseline over all 211 probes as before): the first label's length, the last's, the
  category count, the chart's width, the value labels' width and the text's size, one at a time.
  The push follows the first label alone (a short first label pushes nothing; the last label,
  long or short, nothing), and **the inside's left is at least 10.93 + the first label's width x
  sin 45 - the plot's right edge / (2n)** (n categories, the right edge from the chart's left):
  Office takes the first category's width as the right edge over n, as if the plot began at
  the chart's edge, so where the plot begins drops out.  On all 31 probes with labels turned
  45 degrees (R3, R12, R13; 9 and 12pt; every label and every second shown) to 0.07pt; the
  constant 10.87 to 10.96.  A label cut short pushes by its drawn width (R12's 108pt-high plots).
- **The gate after R13**: 141 probes drawn, **136 within tolerance in every element** (98.9% of
  14,844 elements).  Open (named in the test): every second label shown with labels turned 45
  degrees (one: Office's inside ends 2.18pt short on the right), a bar Word draws 4.56pt for
  Excel's 4.76 (one: 0.27 from Word's PDF, within 0.13 of Excel's layout), plots 1pt high (two)
  and R2's set maximum (one).  Pictures: 134 of the 136 within their tolerance (median 0.13%); the
  two others are R12's cut-short labels ("C...", about a point low).
- **Closed** 2026-10-12 (Jason: "close phase 1").  The gate (§4.2) is met but for the five probes
  named open, each a corner (a plot 1pt high, a set maximum with the labels near the bottom,
  every second label shown with labels turned, a bar Word's PDF rounds); they stay in the test's
  list, and leave it as they come within tolerance.  **Not probed** in phase 1, and drawn by
  analogy with what was: a legend at the top, the left or the top right (R1 to R13 have it at the
  bottom and the right only), and an axis's number format other than General (R5's probes have
  data labels, phase 4's; its categories are dates on a line chart, phase 2's).  CHANGELOG
  entry for the module.  Hand-offs: none yet (the ports take the axis rule, §9, once phase 2 has
  the level value axis too).
- **For phase 2**: a level value axis (the bar chart) needs wider spacing than an upright one
  (R11's bar probes: 24.75pt between labels refused, 30.75 taken), its rule to find on R11's ten
  widths; the read model, the colour cycle for series with no fill (out of `ChartDataEditor`)
  and the oracle for every type of R1, R6, R7, R8, P1, P3, P4 and P6 are in place.

### Phase 2 (2026-10-12 to 13), done: the other eleven types

- **Built**: `ChartLayout` is now the frame (the title, the legend, the plot area) and the plot of
  each kind: columns and bars clustered, stacked and 100% stacked; lines (markers, gaps and spans)
  and areas over categories or dates; a scatter chart's points over two value axes; pies and
  doughnuts.  `AxisScale` takes the test of whether a scale's labels fit as a parameter, so that
  a horizontal value axis has its own; `Drawing` keeps a pie's slice places as Office reports
  them.  `check` draws the twelve types and still refuses smoothed lines (until R14 shows Office's
  curve), data labels, axis titles, manual layouts, secondary axes, combinations and 3-D.  The
  gate compares a pie's slices by their nine `PieSliceLocation`s, a scatter chart's x axis, and a
  shape's fill and line apart (Word's PDF draws a marker's or slice's as two paths).
- **The gate**: 176 probes drawn (the twelve types of R1, R2's line and bar, R5's dates, R7, R8
  but its labelled pie, R11's bars), **170 within tolerance in every element** (99.1% of 18,558).
  Every probe of the new types passes but r1-scatter, whose kerned title is named open (below);
  the other five open are phase 1's.
- **The gate widened** (same day): R1's twelve pptx against PowerPoint's layout and PDF (the
  oracle had them since phase 0; PowerPoint's template has no title and 11.97pt text); and Office's
  own charts, CR-036's oracles copied into `charts/render/office/` (Word's *Insert Chart* of each
  type, P3, and PowerPoint's, P4, with their PDFs traced; no layout dump, so the drawing alone):
  **215 drawn, 208 within tolerance in every element** (99.3% of 21,881).  Word's 14 and
  PowerPoint's 13 all pass, so the renderer draws Office's own chart XML as Office does, not only
  docx4j's; the seven open are phase 1's five and the kerned "Y-Values" title in Word and
  PowerPoint.
- **Pictures**: our pictures' rectangles were anti-aliased, Excel's are on whole pixels (a bar's
  edge pixel is its colour or the background), which put every probe at about 1% of pixels off by
  more than 32, phase 1's included.  Phase 1's record of 0.04% to 0.33% could not be reproduced
  from its renders with `chart_score.py raster` (the same renders score 0.96% on R1's column);
  how it was measured is not recorded.  `toImage` now paints rectangles and level lines crisp
  (the SVG is unchanged): **169 of the 175 pictures within their tolerance**, every one of the new
  types'; the six others are phase 1's (three open, two with labels cut short, r12-cats12-w180 at
  0.71% against 0.70%).
- **Measured on the way** (each now a rule in `ChartLayout`, with its probes):
  - a bar chart: its categories up the left from the bottom, their labels placed as a column's
    value labels (6.53pt from the chart's edge, 0.7575 of a line from the inside); its values along
    the bottom, labels centred under their gridlines, the inside's right edge in by half the last
    label (r1-bar-*, R11); series from the bottom of each category; the legend of a clustered bar
    chart lists the series from the last (r1-bar-clustered), a stacked one's from the first;
  - stacked bars stack on the bars of their sign; a 100% chart's axis ends at 100% and its
    labels, linked to the source, are `0%`;
  - a horizontal value axis's unit (bars, a scatter chart's x): at 9pt more than 24.75pt an
    interval and at most 25.49 (R11's bar chart, one-digit labels), 35.67 taken (r1-bar-stacked-100,
    "100%"); at 11.97pt 33.76 refused (its pptx): a minimum of 2.821 to 2.832 times the labels'
    text size, which the labels' width and a gap cannot fit; 2.826 until R14's sweeps;
  - labels centred on the inside's edges keep within the plot area: a midCat axis's first and
    last (r1-area's dates), a horizontal value axis's last;
  - a date axis: dates placed by their base unit (months: equally spaced, whatever the days),
    labelled at each major unit; its labels turn 90 degrees where text would turn 45 (R5's daily
    dates: the band the widest label turned 90, to 0.01pt); number format 14 (`m/d/yyyy`) is the
    machine's short date in Excel and Word alike (the VM's `d/mm/yyyy`, so `1/05/2002`), docx4j's
    `docx4j.chart.render.shortDate` property, the gate setting the VM's;
  - tick marks 0.3167 of their axis's text size long (r1-area's date axis: 2.85pt at 9pt in Word's
    PDF, 3.80 at 11.97pt in PowerPoint's);
  - a line series' legend key is 12pt wide (an entry twice the key plus its text, as a square
    key's), its line drawn 1.6 times the key, the marker at its middle; a scatter series with no
    line has a square key with its marker in it (r1-scatter.pptx); a horizontal legend
    spaces its entries by their own widths but Excel reports each at the widest's (r1-pie), and is
    at most 0.9 of the chart's width, its entries closing up (r11-bar-w144);
  - a pie: the largest circle the plot's space holds, centred; slices clockwise from twelve
    o'clock; a series' explosion shrinks the pie by 1 + its share and moves each slice out its
    share of the radius, a point's alone moves its slice without shrinking the pie (R8); a
    doughnut's hole its share of the radius; slices and their legend keys stroked with the
    point's line (white, 1.5pt);
  - kerning: Office kerns r1-scatter's title "Y-Values" by its "Va" pair (0.78pt at 14pt) but not
    by the "Y-" pair Carlito also has (0.80pt), so the title's box is 0.8pt narrower than ours:
    Calibri's pairs are not all Carlito's.  Open until R14's kerning probe.
- **Automatic colours** (§4.2): `ChartColorStyles` in core, out of `ChartDataEditor` (which uses it
  now): a chart's colour style part and its cycle (colour idx mod n, varied by variation idx / n).
  The renderer colours a series, or a point where the colours vary by point, that has no fill of
  its own by it, and a line series with no line of its own in that colour at 2.25pt; where a chart
  has no colour style, Office's colorful style (id 10), to be checked on R14's probes.  The read
  model's colours and the renderer's now resolve through the chart's own theme override alike.
- **R14** (on the share for a VM run; `ChartRenderProbes`, 123 probes: 56 made by docx4j as xlsx
  and docx, 67 of Excel's own CR-036 charts copied as xlsx only): sweeps (the script resizes a
  chart through about 180 widths or heights and reads its axes at each, `excel\<name>.sweep.json`)
  for the horizontal axis's unit against its labels' width and size (bars at three scales and
  three sizes, the 100% bar, the scatter), the vertical unit, and the labels' ladder on a text and
  a date axis; legends (line keys at 7, 12 and 16pt; top, left, top right; the order of a vertical
  legend for five types; legends wider than a row); pies (wide, tall, legend at the right, two
  rings, both explosions); marker symbols and sizes; smoothed lines and a scatter chart's lines;
  date axes (months with a gap, weekdays, years, a major unit set, columns over dates, a long
  format); stacks with negative values, stacked lines and areas; tick marks out, in and across;
  kerning pairs at 14pt; axis number formats; series and a pie's points with no fill of their own,
  with and without a colour style part; and P1's and P6's dates and shapes, so that Excel's
  own charts join the gate (layout and picture; they have no docx).
- **R14's results** (run 2026-10-13; the oracle has them: 334 probes, eleven sweeps of 182 sizes,
  `ChartSweepTest` lays each swept chart out at every size against Excel's axes and inside).
  Several of the rules above were R1 to R13's slice of a different rule; each is replaced:
  - Excel's own charts are in Excel 365's theme, whose body font is **Aptos Narrow**, which mapped
    to Arimo (a class substitute): Intos Narrow (OFL, the same project and commit as the Intos
    Display already in docx4j-export-fo-fonts-theme2023) has Aptos Narrow 2.00's and 2.01's
    advances for all 987 code points the two share, in all four styles, and HarfBuzz's kerned
    widths of seven test lines identical; it is in the jar now, a metric row in
    `font-substitutes.xml`.  P1 and P6 went from none to 58 of their 67 passing on that alone, and
    to all 67 with the rules below.
  - a horizontal value axis's unit is chosen from the **whole plot area's width** (the category
    labels' band included), at least 3.8 times its labels' text size an interval: every bar sweep
    and the scatter's x change unit at the same chart widths whatever the labels (1 to 60000,
    0% to 100%), 3.789 to 3.814 at 9pt, 3.761 to 3.833 at 7pt, 3.778 to 3.819 at 12pt.  Where its
    widest label is wider than an interval on the inside, the unit is doubled and the bounds kept
    (0 to 60000 by 40000).  This replaces the 2.826 x size above;
  - a vertical axis's unit: n intervals of a line each within its length less 9.41pt (8.93 to
    9.89 over 463 tests at 7 to 16pt, the column sweep's 182 heights among them).  Phase 1's
    (n + 1) lines within the length and 4.87pt was R11's fit, its 14pt probes setting it;
  - a small plot's inside is at least half the plot area's height less the category band's gap,
    the band taking the rest, below the top value label's overhang (the column sweep from 72 to
    92pt high, to 0.01pt; r11-text14-h108).  Phase 1's "0.56 of the plot area" was this near 20pt;
  - a date axis's labels each have the inside over their number; they are set level, wrapped
    at a hyphen (only within 3.1% of fitting: "22-Jan" wraps from 23.03pt and is level from its
    23.80), turned 45 where they are 1.193 lines apart square to them (13.10pt at 9pt), turned 90
    where 1.193 lines apart along the axis; else an automatic major unit goes up to the next of 1,
    2 and 7 days (the date sweep's 182 widths, all exact).  Dates with a space turn 45 as text
    does (r14-dates-long; provisional).  So R5's "turned 90 where text would turn 45" was the
    space-free case;
  - labels turned 45 pull the inside's right edge in by a line x sin 45 less the right edge over
    twice the categories, where that is positive: the same estimate as their push on the left (the
    date sweep's 16 sizes, to 0.05pt; r12-cats12-w144, phase 1's open one, to 0.04);
  - a bar chart's category labels take at most 0.55 of the plot area less half its last value
    label, wrapped to fit, a word broken where it reaches the room ("Categor" / "y 4"; the 12pt
    bar sweeps, 0.548 to 0.553);
  - an axis's labels are never kerned, whatever their kern size (r14-kerning: sixteen pairs as 14pt
    category labels, kern 1200, none kerned); titles are.  The title "Y-Values" stays open: Excel
    kerns its "Y-" in Aptos Narrow (p1-scatter) but not in Calibri (r1-scatter), though Calibri's
    GPOS and kern tables have the pair as Carlito's do (-0.0571em);
  - legends: at the top, 6pt below the title; at the top right, below the title with the plot
    area 5pt below it and 6pt higher off the bottom (one probe); a stacked column's or area's
    legend at the side lists the series from the last; entries too wide for 0.9 of the chart go in
    rows, as many to a row as the fewest rows allow, closed up (one probe); a legend's marker no
    larger than its key (3.84pt at 7pt);
  - markers: an x, star or plus is its strokes over a filled and outlined square; a dash a bar a
    fifth of its size high; a dot half that bar, from the point rightwards.  Word's PDF snaps them
    to its 0.24pt grid (6.72pt for 7), PowerPoint's does not (5.04 for 5), so they are drawn at
    their size and the two marker probes stay open;
  - smoothing: Catmull-Rom, each control point a sixth of its neighbours' difference along, the
    ends' a third of the chord (Word's PDF's control points, fitted to 0.006pt); `check` draws
    smoothed lines now;
  - automatic colours: Office shows a series (or point) with no fill of its own in its legacy
    cycle, whatever the colour style: accent (idx mod 6) + 1 shaded or tinted by
    t = floor(140 (cycle + 1) / (cycles + 2)) - 70 percent, cycle idx / 6, cycles count / 6 (eight
    series shaded 76% then tinted 77%, with the colour style part and without; twelve slices
    shaded 65% then plain; every channel exact).  `ChartColorStyles.automatic(idx, count)`
    replaces the colorful-style guess; the colour style stays what a new series is written with;
  - a 100% axis runs from its least share to its greatest, out to the unit, unpadded, within 100%
    either way (-36% to 77% is -40% to 80%); a scatter chart's x axis away from zero is padded a
    twentieth of its range at both ends, not the half range below that KB 214075 gives (46022.5 to
    46026.5), and its first label is kept within the plot area;
  - a pie shows its first series alone (Excel reports the others' slices as if drawn); a title of
    spaces is a line high.
- **Closed** (2026-10-13): **338 probes drawn, 328 within tolerance in every element** (99.7% of
  30,227), every type of R1, P1, P3, P4 and P6 passing but r1-scatter's kerned title; Excel's axes
  and inside at every swept size but four (`ChartSweepTest`); **295 of 299 pictures** within their
  tolerance (r12-cats12-w180, r12-cats8-w216-h108 and r3-eight-180x108, phase 1's, and
  p6-line-numbers, whose long, nearly level 2.25pt lines are anti-aliased differently).  Open, each
  named with its reason in `ChartRenderGateTest.OPEN` and `ChartSweepTest.OPEN`: the "Y-Values"
  title (five probes); Word's marker grid (two); r11-default-h72 and r11-eight-h72 (the inside is
  Office's now, but Word draws a column nothing high as a 0.24pt sliver); r2-max-set (0.55pt); the
  12pt bar sweeps' two narrowest sizes, the legend in two rows (the inside's top 0.55 and 0.93pt
  lower).  Provisional, on one probe or one label width each: the top right legend, legend rows,
  date wrapping, a spaced date's 45.  Leader lines with outside labels wait for data labels
  (phase 4, §4.5).  For phase 3: the renderer is drawn into the outputs as is; nothing in phase 3
  needs a new rule.

### Phase 3 (2026-10-13), done: in the outputs, and an API

- **Built**:
  - `ChartBox` draws: the chart is found from the conversion's current part (a header's chart from
    the header) through `ChartRef`, and the renderer's SVG goes into the
    `fo:instream-foreign-object` CR-035 writes (FO, the visitor and the XSLT alike) or is the
    inline `<svg>` (HTML, both pathways; with `docx4j.convert.out.html.chartAsPng`, an `<img>` of
    the chart's picture, a data URI at twice the box's pixels: D8).  pptx's SVG-in-HTML calls
    `SvgExporter.createChart` from its chart template, a `div` at the frame's place holding the
    SVG.  Where nothing draws the chart (no renderer on the classpath, a chartex chart, a chart
    `check` refuses) the box stays empty, as in CR-035, and the reasons are logged (A1).
  - `ChartImages` (core, public, D7): `check(ChartRef)` (the renderer's reasons, and "no
    renderer" or "chartex" before it), `getSize`, `toSvg`, `writeSvg`, `toImage`,
    `toPng(ChartRef, dpi)` (PNG bytes), `writePng`, each at the anchor's size or one given; a
    chart that can't be drawn throws, the reasons in the message.  An xlsx two-cell anchor is
    sized from its cells (`ChartAnchorSize`: ECMA-376's column width in the Normal font's widest
    digit, in whole pixels, a default column rounded up to 8 pixels; rows as the file gives them,
    a height set by hand in whole pixels; hidden ones nothing), on a screen of so many dots an
    inch, `getSize(chart, dpi)`, 96 (a screen at 100%) unless asked.  **Measured**
    (`charts/sizes/`: ten probes of Excel's own chart with one thing changed each,
    `tools/chart_size_probes.py`; Excel's report of each, `tools/excel-chart-size-check.ps1`, Jason's
    runs 2026-10-13 with the VM's display at 125% and at 100%): Excel lays the sheet out in
    pixels of the screen.  Widths: **all ten exactly Excel's at 120, nine at 96**; the tenth is
    GDI's hinting (Calibri 14 at 96 is set at 19 pixels, where its digits are 10 pixels wide, its
    advances 9.6; docx4j measures 9.46 and rounds to 9).  Heights: a height set by hand is
    Excel's at both, floored to whole pixels (20pt: 19.5 at 96, 19.8 at 120).  A height not set
    by hand Excel takes from the Normal font at the screen (Aptos Narrow 11: 15pt at 96, 14.4 at
    120; Calibri 14: 18.75 and 18; Arial 10: 12.75 and 13.2), which neither the file nor the
    clones' metrics give (no rounding of the fonts' ascent and descent fits all six: GDI's hinting
    of the real faces), so docx4j takes the file's, Excel's at the screen that saved it: Excel's
    own chart, saved at 120, is 252pt high there and 262.2pt at 96.  `ChartAnchorSizeTest`.
    `ChartBox.forChart(ChartRef)` and `toHtml(width, height)` for an output that places
    the box itself.  Sample `ChartsAsImages` (docx4j-samples-docx-export-fo).
  - D6's outlines: `docx4j.chart.render.textAsShapes` writes the SVG's text as glyph outlines
    (one path a text), as `docx4j.convert.out.metafile.textAsShapes` does a metafile's.
- **What the outputs needed**, found on P3 and P4 (no new layout rule; the renderer's drawing as
  phase 2 left it):
  - **kerning**: an SVG's `<text>` carries none, so FOP drew P3's kerned title "Y-Values"
    unkerned, 1.1pt wider than Word's, from a start placed for the kerned width (0.74pt off).
    `Drawing` now writes a kerned text (for an SVG; a picture is AWT's, kerned) as runs split at
    each kerned pair, each from its kerned place: 0.05pt from Word's.
  - **a browser kerns, and makes ligatures**, in an SVG's text by default (Chrome drew P4's legend
    entry "Y-Values" at 11.97pt, below the kern size and unkerned by PowerPoint, 1.8pt narrower).
    The SVG's root says `font-kerning: none; font-variant-ligatures: none`, every run being placed
    already; FOP's Batik reads the style and draws as before.
  - **families**: Batik names the AWT font's family, the face docx4j's mapper gives FOP
    (Carlito); the SVG now names after it the face the chart names and its class's generic
    family, as docx4j's HTML does for a run (`'Carlito', 'Calibri', sans-serif`; `ChartFonts`
    records the face each family stands for), so a reader of the SVG without Carlito draws
    Calibri, at the same widths, or at least a face of the class.  Chrome on Linux resolves
    neither Carlito nor Calibri by name (its own metric-compatibility rules over fontconfig's
    aliases: Carlito installed, Akasia resolved, measured), so on this machine it now draws a
    docx's chart in its sans-serif, not its serif.  `FontFallback` knows no class for Aptos, so an
    Aptos chart's list ends at `'Akasia', 'Aptos'`, as docx4j's HTML of an Aptos run does.  The
    HTML's `docx4j.convert.out.html.fontFamily` order (document face first) is not applied: a
    chart is laid out in the mapped face.
  - **fonts in the PDF**: nothing to do.  `ChartFonts` makes its fonts through
    `Docx4jDrawFontManager` with the document's mapper, which registers each face it draws as a
    late fallback, and `FopConfigUtil.declareFallbackFonts` declares those to FOP once the FO is
    made (CR-011's route for a metafile's fonts, 17.2.0): a chart in DejaVu Serif in a Calibri
    document, the face named nowhere else, comes out embedded, as text.
  - **ids**: Batik writes `genericDefs` and numbers clip paths from 1, and one HTML document
    holds every chart's SVG: each chart's ids, and the references to them, are prefixed with its
    part's name (`docx4j-chart1-...`).  In FO each `fo:instream-foreign-object` is its own SVG.
- **The gate** (§4.4, §5; `tools/chart_outputs.py`, the harness's `ChartOutputs`):
  - **P3** (Word's Insert Chart, 14 documents, every type, two charts in one) through docx4j's PDF
    against Word's PDFs: **14 of 14 within D4's tolerances in every element, by the visitor and
    by the XSLT** (texts within 0.26pt, shapes within 0.23pt), the dates in the VM's short date.
  - **P4** (PowerPoint's, 13) through the SVG-in-HTML, printed by headless Chrome with docx4j's
    font files given to it: **13 of 13 within the same tolerances** (texts 0.31pt, shapes
    0.21pt), not the looser ones §4.4 allowed.  Without docx4j's fonts Chrome draws P4's Aptos
    (Akasia) in its default face, which measures the browser, not docx4j.
  - **The harness's chart probes** against Word's goldens: 0.9358 line parity, 0.9853 page parity,
    every line and page as b222's with the box empty, the charts drawn (in a header, in a table
    cell, floating; seen): their charts have no title, axes or legend, so the drawing adds
    shapes, not lines.
  - **The corpora's control**: b224 (HEAD 2d9fc09a4 with phase 3) against b223 (HEAD), the four
    corpora and the 270 probes: **0 movers, every event count equal, the probes identical row for
    row** (no corpus document has a chart, so this shows only that nothing else moved;
    `~/fidelity-cr030/gate-b223-b224.sh`).  HEAD itself, against b222 (CR-035 phase 0's
    candidate), moves one document, 5361, by 4 lines, not this phase's and not looked into (the
    fidelity register's `documents/5361.md`).
  - **Tests**: docx4j-chart-render's `ChartImagesTest` (the API on Word's, PowerPoint's and
    Excel's charts, a refusal and chartex, the HTML on both pathways, the PNG property, pptx,
    outlines) and `ChartAnchorSizeTest` (Excel's sizes at 120), and
    its phase 1 and 2 gates unchanged; docx4j-export-fo-tests' `ChartBoxFoTest` (the chart drawn
    in its box on both pathways, the box empty without a renderer, the chart's text text in the
    PDF); docx4j-core-tests' chart tests unchanged (no renderer on its classpath: the box).
    Maven: docx4j-chart-render 20/0; docx4j-export-fo-tests 792/0 (9 skipped); docx4j-core-tests
    1545/0 (11 skipped), once `font-substitutes.xml`'s catalogue had Intos Narrow (phase 2's
    1af3088b3 added its metric row but not its catalogue entry, which `FontSubstitutionTableTest`
    requires; phase 2 had run the chart tests alone).
  - **Time**: a chart adds about 10ms to a document's PDF once the JVM is warm (P3's clustered
    column, 31 against 20ms); `toSvg` 1.1ms, `toPng` at 96 dpi 5ms.
- **Open**: A chart on a
  pptx layout or master is looked up in the slide's relationships, as the SVG-in-HTML output
  does for the other content it takes from them.  Converting P3's clustered column logs four
  "Parent of org.docx4j.dml.chart.CTRelId is currently org.docx4j.dml.GraphicData" warnings, at
  HEAD as well: `TraversalUtil.getChildren` gives an `Inline`'s graphic data items as its children
  (skipping `GraphicData`), so the walk's parent check sees the chart reference's grandparent.
  Harmless, not phase 3's; left as it is.
- **Closed** 2026-10-13 (Jason: "close phase 3").  §4.4's gate is met: P3 and P4 within D4's
  tolerances in every element, the box probes and the corpora unmoved, docx4j-export-fo-tests
  green; nothing named open in the outputs.  Hand-offs (§9): the MCP tool can now take
  `ChartImages` (the plan's phase 10); the fidelity register has the two causes and the attempt.
  For phase 4: the outputs draw whatever `check` passes, so each feature phase 4 draws reaches
  PDF, HTML and pptx output without more work here.

### Phase 4 (2026-10-13), in progress: what the settings and the data add

- **R15** (`ChartRenderProbes`, on the share with RUN.txt's section; Jason's run the same day,
  nothing failed): 95 probes made by docx4j (xlsx and docx) and 39 of Excel's own charts copied as
  xlsx alone: CR-036's P2 settings (the ten), P6's axis titles, data labels and number formats on
  every type (29), and CR-035's chart with a series filtered out (`c35-filtered`).  Axis titles on
  each type, beside each legend, long, at 14pt, level, small; data labels at the positions R6 left
  (bars, stacked bars, areas, lines without markers, doughnuts, scatter charts), their contents and
  separators, a legend key, a point's own label and a deleted one, negative values, a pie's best fit
  and outside labels over twelve slices, a label moved; manual layouts in factor mode, with right
  and bottom edges, a pie's, an axis title's; legend entries deleted, overlays; axis options
  (deleted, crossing at a value or the maximum, reversed, tick labels high, low and none, minor
  gridlines and tick marks); the chart area's and plot area's fills and lines, gradients, patterns,
  shadows, transparency, dashes; titles of several runs and paragraphs; invertIfNegative; varyColors
  on one series.  The baseline over all 468 probes holds as before (points and the inside to
  Office's precision); `chart_score.py`'s scoring of a few R15 features (reversed values, patterns)
  is the tool's heuristics, not Office.  The oracle (`charts/render/`, 468 probes) has them.
- **Built** (`ChartLayout`, `Drawing`, `ClassicChartRenderer`, `GradientHandler`, new): `check`
  draws data labels, axis titles and manual layouts, and refuses only §4.6's (other kinds,
  combinations, secondary axes, 3-D); gradients and patterns are drawn, not simplified; a picture
  fill, inner and preset shadows, reflections and soft edges are simplified, and say so.
  - **Data labels**: each point's label from its group's `c:dLbls`, its series', and its own
    `c:dLbl` (deleted, moved by hand, rich text); its parts (series name, category name, value in
    the label's number format or linked to the cache's, percentage), separated as Office does; its
    box its text and its insets (`bodyPr`, Excel's 3pt and 1.5pt), turned a quarter where `rot`
    says (style 202), with its legend key; filled and outlined by its `c:spPr`.
  - **Axis titles**: rich text of several runs, wrapped, turned as `bodyPr` says, beside the plot area.
  - **Text**: titles (and axis titles) as runs, each in its own style, wrapped across runs, each
    paragraph its own lines; capitals (`cap="all"`) and letter spacing (`spc`) in every text.
  - **Manual layouts** of the title, the legend (moved, or sized: its entries in a grid), the plot
    area (its outer box or its inside, the other found around it; a pie's square within its box),
    an axis title and a data label.
  - **Axes**: crossings (`crosses`, `crossesAt`) and reversed orders on column and bar charts,
    tick labels low, high and none, minor gridlines, the category axis's gridlines, minor tick marks.
  - **Fills and lines**: the plot area's fill and line; gradients (linear at an angle, scaled or
    not; radial paths), drawn as AWT paints and written to SVG as `linearGradient` and
    `radialGradient` (`GradientHandler`: Batik's own writes a two-stop `GradientPaint` only), in
    fills and in lines (style 213's gridlines); DrawingML's preset patterns as 8 by 8 tiles at 96
    dpi; outer shadows (moved, scaled, skewed about their alignment, blurred) and glows drawn as
    pictures under their shape, as Office's PDFs have them (finding 6), PNG data in the SVG by the
    JDK's writer (Batik's needs batik-codec, not a dependency); dashed lines (`prstDash`, Office's
    presets); a bar's own line; invertIfNegative; deleted legend entries; overlays.
- **Measured on the way** (each a rule in `ChartLayout`, with its probes; "provisional" where one
  probe or size pins it):
  - a data label's box is its text and insets, `LABEL_GAP` (3pt) from a bar's end or base or a
    marker's edge, centred on a bar or point; a line's labels stand off as from a marker of its
    size, Office's 5 where it shows none (R6, r15-labels-*, to 0.001pt);
  - a pie's label at its centre is half way out its middle ray; inside its end, the box leaves
    the ray 3.352pt inside the arc (R6, R10's 258: all eight to 0.001pt); outside its end, see R16
    below; a pie with labels outside or at best fit, and a doughnut with labels, is smaller by
    1.375pt + 1/16 of its side each side, whatever the labels' size (R16 below);
  - Office separates a label's parts by ", ", but a pie's category and percentage alone, shown by
    the series' labels, by a line (r8; a point's own label shows them with ", ", R16); a pie's
    percentages are whole and add up to 100%, the difference on the largest (style 258: 58%, not 59%);
  - a label's legend key is the legend's square, in a slot of two keys and 3pt before the text;
    a label moved in factor mode is moved by shares of the chart from its own place;
  - an axis title's box is its text and the title's padding, turned with it; it takes the plot
    area's left or bottom edge with no gap, is centred on the inside (its top no higher than the
    plot area's), and the axis's labels on its side are `MANUAL_INSET` (6pt) further in; long ones
    wrap at 0.8 of the chart's side (all 16 axis-title probes, to 0.001pt);
  - manual layouts: edge-mode x and y are shares of the chart from its corner, the title, legend
    and axis titles keeping the room Office gives them; a plot area placed by hand has its labels
    `MANUAL_INSET` further in and the top value label's half line inside; a sized legend's entries
    in a grid of as many columns as hold the widest, each centred in its cell (R9, R15);
  - an axis's `crosses`/`crossesAt` is where it crosses the other axis in the other's units; its
    labels are next to its line, a band at the plot's edge when the line is there, hanging from it
    when within (zero at the maximum, all the data negative, is within: r2-negative); low and high
    force the ends; columns are drawn from the crossing; reversed orders flip the direction and the
    crossing, a reversed bar chart's legend in series order; labels above the plot stand 0.67pt
    higher than a mirror of those below (R15's axis options, all to 0.001pt);
  - a title, its txPr naming no size, is 1.8 times the chart's text and bold unless it says (204);
    a letter-spaced text's width is its kerned advances and `spc` after every character, the last
    included (202, 209, 258); a run's trailing space isn't drawn width (r15-title-runs);
  - minor tick marks are 0.2367 of the text size and at every minor place, the major ones'
    included; a category axis's minor ones at each category's middle and its far end (one probe);
  - a line series' legend key is 4.5 times its line's width, at least 12pt (one width:
    provisional); varyColors on a line series lists its points in the legend, each the series'
    line and marker, and draws the line a piece from each point to the next, all in the series'
    colour; invertIfNegative draws a negative bar white, outlined 0.75pt in black;
  - Word's PDF draws gradients as shadings in the shape's clip, a gradient line as a shading in
    the stroke, and shadows, glows, bevels and patterns as pictures: the gate compares a gradient
    by its box and leaves pictures to the raster comparison (D4);
  - Office interpolates gradients in linear light (a radial gradient's three channels agree there
    and nowhere else); its radial path's falloff isn't linear in the distance from the focus
    (r15-pie-gradient: no simple power fits), so radial gradients stay a picture miss;
  - Excel formats numbers linked to their source as the worksheet's cells, Word as the chart's
    cache (r15-labels-linked-format, whose cache and cells disagree on purpose); docx4j draws the
    cache's, as Word does.
- **R16** (Jason's run the same day, nothing failed; 75 probes, xlsx and docx): a pie with one
  label, on a small first slice turned round the pie by 15 degrees, at outEnd and at best fit; two
  other label sizes at eight angles; best fit on five slice widths; the pie's shrink at four more
  chart shapes and a doughnut's.  **A label outside its pie** hangs from a point beyond the arc on
  its slice's middle ray, 0.0508 of the radius and 0.01 of the label's width across, 0.04 of the
  radius down or up; the box hangs from that point by shares of its width and height that slide
  round its edge with the angle: vertically b = 1/2 + cot/2 between 0 and 1 (its bottom edge on
  the point up to 45 degrees, its middle at 90, its top edge from 135), horizontally its middle at
  the top and bottom sliding to its side three times as fast as tan of the angle (on its side from
  about 18 degrees; one angle between: provisional).  Every R16 outEnd probe, the five pie sizes,
  the three label sizes, and r6's, r8's, R15's twelve slices and moved label to 0.01pt: what
  looked like Excel pushing crowded labels aside was this slide.  **The shrink**: 7/8 of the side
  less 2.75pt (six sides, 73 to 265pt, to 0.01pt).  **Best fit** keeps the label inside the slice
  in every probe (on a 3% slice too); its boxes lie on whole pixels at 96 dpi, edge by edge, and no
  model of R16 fits their places (the farthest corner 1.2pt inside the arc, drawn, is up to 1.7pt
  off): open.
- **The gate** (`ChartRenderGateTest`, R10's styled charts in it now): **582 probes drawn, 529
  within tolerance in every element** (99.4% of 50,790); phase 2's close was 338 and 328.  Every
  probe of R5, R6's bars and lines, R9, R15's axis titles, axis options, legends and fills, R16 but
  best fit, and every P2 and P6 settings chart but the pie's data labels (best fit).  Open (each
  named with its reason in `OPEN`):
  - a pie's labels at best fit (34 probes, R16's among them): above;
  - factor mode for the title, legend and plot area (three): Excel's offsets are not the spec's
    (0.10 is 39.1pt for the title, not 43.2); Excel itself writes edge mode, drawn exactly;
  - text in tx1 lumMod 50% lumOff 50% (styles 202, 254): Office 7F7F7F, docx4j 808080 (CR-034's
    known one-unit luminance cases: rounding half down fixes lum, lumMod and the picker but breaks
    hueMod, satMod and satOff, measured on CR-034's readings);
  - the kerned "Y-Values" title (now eight probes with R15's scatter charts), phase 2's markers,
    plots 1pt high and set maximum, and the linked number format (above).
- **Pictures** (rendered at Excel's 1.667 px/pt against Excel's PNGs, D4's per-probe tolerance):
  **536 of 543 within tolerance**; the seven: phase 1's two small charts, two pies' labels at best fit (open),
  R15's patterns (the tile's phase against Excel's resampling: 3.7% against 2.0%), the radial
  gradient on a pie's slices (above) and the factor-mode plot area (open).  Style 213's glow and
  R15's shadows pass once drawn (a glow follows what is drawn: around an outline alone, not its
  inside).
- **The outputs** (phase 3 drew whatever `check` passes, so phase 4's features reach PDF, HTML and
  pptx with no change there): P3 through docx4j's PDF still 14 of 14 on both pathways
  (`tools/chart_outputs.py`); R15's gradients, shadows, axis titles and labels with keys, and
  style 204 (Word's styled docx), through docx4j's PDF against Word's PDFs, within D4's tolerances
  in every element (gradients reach the PDF as shadings, shadows as pictures, as Word's). Style 204
  first came out with every text bold: a face a chart draws in (CR-011's route since 17.2.0,
  `Docx4jDrawFontManager`'s late fallbacks) was declared to FOP under its family name as the
  family's normal face whatever its weight (`FopConfigUtil.createFontEntrySimulateStyles`'s
  family triplet), so Carlito-Bold answered for "Carlito, normal, 400" and every regular Carlito
  text of the PDF was drawn bold.  FOP's font info names a face's own names normal and 400 and
  gives the family's name its real style and weight ("Carlito Bold,normal,400", "Carlito,normal,700"):
  the family triplet now takes those.  Any late-declared bold or italic face had it (a metafile's
  bold text, a fallback `RunFontSelector` reached for a bold run). The corpora's control, b225
  (b224's jars, docx4j-chart-render phase 4's, with the fix) against b224: **0 movers, every event
  count equal, the probes identical row for row** (the chart box probes too): no corpus document
  had such a face (`~/fidelity-cr030/gate-b224-b225.sh`; the fidelity register's
  `late-face-declared-family-normal`). Test: export-fo-tests'
  `ChartBoxFoTest.aChartsBoldTextLeavesItsRegularTextRegular`, which fails without the fix.
  Batik's SVG generator encodes an embedded picture through batik-codec, not a dependency of
  docx4j-chart-render: the renderer gives it an image handler using the JDK's PNG writer.
- **Tests**: docx4j-chart-render 20/0 (`ChartRendererSpiTest` and `ChartImagesTest` now refuse a
  secondary axis, made in memory, where they refused data labels; `ChartRenderFixturesTest` 543
  probes, skipping a pattern's colour as a gradient's, Excel's carriage-return paragraphs, the
  filtered series).
