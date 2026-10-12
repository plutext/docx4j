# CR-037: charts drawn - a chart rendered to SVG and PNG as Office draws it, and drawn in PDF, HTML and pptx output (charts plan phases 7 and 8)

Status: PROPOSED 2026-10-12.  Drafted on Jason's request ("please draft a CR for phases 7
(rendering) and 8 (integration) of the charts plan.  Do not implement yet."); reviewed the same day
by the docx4j-13 session (Claude Fable 5.1, at Jason's request): approved with nine amendments, all
accepted by Jason ("apply all nine") and applied (§8).  Nothing is implemented.  Owner: Jason
Harrop.  Drafted with Claude Opus 5.5.

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
    chart (R3).
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
  the PDFs and PNGs stay on the share, for the raster comparison, run from the harness.

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

(none yet)
