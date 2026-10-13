# CR-039: Chartex hooks for the Enterprise jar - a `ChartExRenderer` SPI and the cx Choice, an exported `org.docx4j.chart.render.api`, and `ChartCreator`'s host steps opened

Status: PROPOSED 2026-10-14.  Drafted at the request of the Enterprise session
(plutext-enterprise-java-11-44), from enterprise CR-004 (`../Plutext-Enterprise-Java-11/docs/developer/change-requests/CR-004-chartex.md`)
§4.1, §4.3 and §4.6.  That session relays Jason's decision on enterprise CR-004: phase 0 starts
now; the product is a module of the Enterprise reactor; docx4j 17.3.3 is not released until it
contains what CR-004 needs from docx4j, which is this CR.  Jason, 2026-10-14: "commit CR-039 then
implement it, with its four decisions as recommended" (D1 to D4, §5).  Owner: Jason Harrop.
Three phases; phase 1 first, since the Enterprise skeleton compiles against it.

Scope: docx4j-core (`org.docx4j.model.chart`, `org.docx4j.jaxb.McSelection`, the exporters' choice
of branch) and docx4j-chart-render (a new exported package).  Not in scope: anything that draws,
reads the computed layout of, edits or creates a chartex chart: that is the Enterprise jar's
(charts plan D1 (d)).  Not in scope either: the stored `layoutPr` settings in `ChartExModel`
(binning, statistics, parentLabelLayout), which the Enterprise jar reads from `Series.getSource()`
for now (enterprise CR-004 §2.1); a separate small CR if the ports want them.

## 1. Why

Enterprise CR-004 draws, edits and creates Office 2016's chart types (waterfall, histogram, Pareto,
box and whisker, treemap, sunburst, funnel) in a commercial jar.  It needs three things that docx4j
does not offer yet:

1. **Its own renderer.**  Today a second `ChartRenderer` provider would compete with
   docx4j-chart-render by classpath order, and `ChartImages` refuses a chartex chart outright.
2. **The open renderer's frame.**  The axis scaling, text measuring, drawing primitives, the SVG
   writer and title and legend placement are in docx4j-chart-render, which exports nothing
   (CR-037 D7: promoted when the Enterprise jar needs them; it needs them now).
3. **The host steps of `ChartCreator`.**  These are the embedded workbook, the docx run, the
   pptx frame, the xlsx anchor and the part naming.  They are private, so a `ChartExCreator`
   can't reuse them.

## 2. Where we are (read 2026-10-14, `VERSION_17_3_3`)

- **SPI.**  `ChartRenderer` (core): `check`, `draw`, `toSvg` and a default `toImage`, all on
  `ChartModel`.  Its lookup is `ServiceLoader`, tried first with the thread's loader and then with
  core's.  The first provider wins, and `setRenderer` overrides.  `module-info` has
  `uses org.docx4j.model.chart.ChartRenderer`.
- **The outputs.**  `ChartBox` (FO, HTML, pptx SVG-in-HTML) draws through `ChartImages.svg` and
  `.image`.  `ChartImages.unrenderable()` refuses when `ChartRef.isChartEx()`, so a chartex chart's
  box stays empty.
- **`ChartExModel`.**  It has `getSource()` (the `CTChartSpace`), but no `getPart()`, so a renderer
  can't reach the workbook.  An xlsx's chartex has no caches, only `_xlchart.v1.N` names.
- **The cx Choice.**  `McSelection.selectedBranch(ac)` takes a Choice only when every `Requires`
  prefix is listed in `docx4j.jaxb.mc.preferChoice` (default empty, so the Fallback).
  - It has no part, so it can't read the chart a Choice names.
  - Office's `Requires` prefix varies by type: `cx1` to `cx8` are the chartex marker namespaces in
    `NamespacePrefixMappings`, and the waterfall writes `cx1`.  A rule keyed on `cx1` alone would
    miss some types.
  - **The callers that draw:**
    - `AbstractVisitorExporterGenerator.walkJAXBElements` (the FO and HTML visitors, which know
      `conversionContext.getCurrentPart()`);
    - `docx2fo.xslt` and `docx2xhtml-core.xslt` (`$conversionContext`);
    - `pptx2svginhtml.xslt` (the `SvgExporter` context, whose resolved layout gives the slide).
  - **The callers that don't draw:** `TraversalUtil` in READ mode, `TextUtils`, the slide, layout
    and master placeholder walks, the load preprocessor (`mc-preprocessor.xslt`), and markdown,
    which already reads a chartex Choice's table itself.
- **`ChartRef`** already reads a frame inside a Choice, as JAXB or as DOM (`inChoice`).
- **docx4j-chart-render** (4,135 lines):
  - `AxisScale` (package-private, static `of(...)` overloads, fields `min`/`max`/`unit`/`log`,
    `values()`, `fraction()`, `Fits`, `Bounds`);
  - `Drawing` (package-private: `Area`, `Text`, `Gradient`, `Pattern`, `Shadow`, `paint`, the
    `boxes` and `slices` the gate's dump reads);
  - `GradientHandler` (package-private, Batik);
  - `ChartLayout.Style` (text measuring: face, size, kern, `spc`, caps, `width()`, ascent, line),
    `wrap`, `words`, `wrapBroken`;
  - the `a:spPr` to fill, line, gradient, pattern, shadow and dash translation (private instance
    methods of `ChartLayout`);
  - title and legend placement inline in `ChartLayout.layout()`, interleaved with the plot
    rectangle;
  - the SVG writer inside `ClassicChartRenderer.toSvg` (Batik `SVGGraphics2D`, `GradientHandler`,
    a PNG image handler, the `TEXT_AS_SHAPES` property, the root's size, viewBox and kerning
    style).
- **`ChartCreator`** (1,277 lines) keeps these steps private:
  - `embedded()`: the workbook part named and typed, the template's bytes, `c:externalData`;
  - `fill()`: the data at A1 in the workbook's table, fresh ids, author and times;
  - the docx run (an XML string with `c:chart`), the pptx graphic frame, and `place()`, the xlsx
    drawing and two-cell anchor;
  - `next()` and `free()` part naming, `embedding()`, `wordId()`, `guid()`, `frameId()`;
  - `template()`, which reads core's own resources `templates/<name>`.

## 3. Design

### 3.1 `ChartExRenderer` (core, phase 1)

A second interface in `org.docx4j.model.chart`, the same shape as `ChartRenderer` but on
`ChartExModel`:

```java
public interface ChartExRenderer {
    ChartRenderCheck check(ChartExModel chart, ThemeColors theme);
    void draw(ChartExModel chart, ThemeColors theme, ChartFonts fonts, double width, double height, Graphics2D g);
    Document toSvg(ChartExModel chart, ThemeColors theme, ChartFonts fonts, double width, double height);
    default BufferedImage toImage(ChartExModel chart, ThemeColors theme, ChartFonts fonts, double width, double height, double dpi) { ... }
    static ChartExRenderer getRenderer() { ... }
    static void setRenderer(ChartExRenderer r) { ... }
}
```

- **Its own lookup.**  It has a `Holder` and `ServiceLoader` lookup like `ChartRenderer`'s, and
  core's `module-info` gains `uses org.docx4j.model.chart.ChartExRenderer`.  The Enterprise jar
  provides only this interface, and docx4j-chart-render is untouched, so the two never compete.
  If two providers of the same interface are found (two Enterprise jars, say), the first is kept
  and a warning names both.  `ChartRenderer`'s lookup gets the same warning.
- **`toImage`'s body** moves to a package-private helper that both interfaces' defaults call, so
  the rendering hints and the white ground stay one copy.
- **`ChartExModel.getPart()`** is added: the `ChartExSpacePart` it was read from, or null when it
  was read from a `CTChartSpace` alone.  `ChartExModel.read(part)` keeps it.  Through the part's
  package the renderer can reach the embedded workbook, or in an xlsx the workbook itself.
- **Dispatch.**  `ChartImages` dispatches on `ChartRef.isChartEx()` everywhere it reaches a
  renderer: `check`, `toSvg`, `toImage`, `toPng`, `writeSvg`, `writePng`, and the outputs'
  `svg`/`image`.  A chartex chart goes to `ChartExRenderer.getRenderer()` with
  `ref.readChartEx()`, a classic chart to `ChartRenderer` as today.  The font post-pass
  (`documentFaces`) applies to both.  `unrenderable()` says "no chartex renderer on the
  classpath" in place of today's refusal.  `ChartBox` needs nothing beyond `ChartImages`.

### 3.2 Fallback against Choice (core, phase 1)

**The property.**  A new property, `docx4j.chart.chartex.branch`, takes `auto`, `fallback` or
`choice`; `auto` is the default.  It governs any Choice whose content is a chartex graphic frame,
meaning `a:graphicData/@uri` is `http://schemas.microsoft.com/office/drawing/2014/chartex`, whatever
its `Requires` prefix.  It is read through `ChartRef.of(host, anchor, true)`, which handles JAXB and
DOM content alike.

| Value | The cx Choice is drawn |
|---|---|
| `auto` | when a `ChartExRenderer` is present **and** its `check` of that chart `isDrawn()`; else the Fallback |
| `fallback` | never (today's behaviour) |
| `choice` | always (the box stays empty where nothing draws it) |

- **Without a renderer, nothing changes.**  In `auto` with no `ChartExRenderer` the content test
  isn't even run, so a user of the open library sees exactly today's output.
- **`preferChoice` keeps its meaning.**  A `cx` prefix listed there still takes the Choice, as
  `ChartBox`'s javadoc says today: it acts as `choice` for that prefix.
- **The selection gets the part.**  `McSelection.selectedBranch(AlternateContent ac, Part host)`
  applies the existing rule first, then this one.  The callers that draw pass the host:
  - `AbstractVisitorExporterGenerator` passes `conversionContext.getCurrentPart()`;
  - `docx2fo.xslt`, `docx2xhtml-core.xslt` and `pptx2svginhtml.xslt` use a new
    `XSLTUtils.mcSelectsChoice(context, choiceNode)`.
  - The callers that don't draw keep the property-only rule (§2), and so take the Fallback in
    `auto`.
- **The `check` is the licence gate.**  An Enterprise renderer whose licence has lapsed refuses,
  and the Fallback picture is drawn, as enterprise CR-004 §4.4 proposes.
- **A chartex part that doesn't read** is logged, and the Fallback is drawn.

### 3.3 `org.docx4j.chart.render.api` (docx4j-chart-render, phase 2)

A new package, exported (`exports org.docx4j.chart.render.api;`).  `ChartLayout` and
`ClassicChartRenderer` move onto it, so there is one implementation, not copies.  `ChartLayout`
itself stays internal: it is 3,000 lines and still moving (best fit and factor mode are open).
The list is the one given to the Enterprise session on 2026-10-13 (enterprise CR-004 §4.3), with
the two departures marked:

| Class | What it is | From |
|---|---|---|
| `AxisScale` | Excel's automatic axis: `of(...)` as today, `Fits`, `Bounds`, `getMin()`/`getMax()`/`getUnit()`/`isLog()`, `values()`, `fraction(v)`, `RESERVE`, `MAX_INTERVALS` | `AxisScale` |
| `TextStyle` | a face at a size, kern, `spc`, caps; `width(s)`, `getAscent()`, `getLine()`, `getFont()`, `shown(s)`; made from `ChartFonts` and values, or from an `a:txPr` (`CTTextBody`) inheriting from a parent; `a:txPr` is DrawingML in both `c:` and `cx:` | `ChartLayout.Style`, `style(...)` |
| `TextWrap` | Excel's wrapping: `words`, `wrap`, `wrapBroken`; rich text as segments and lines (`Seg`, `Line`, `wrap(paragraphs, max)`) | `ChartLayout` |
| `Drawing` | the primitives, public: `Area`, `Text`, `Gradient`, `Pattern`, `Shadow` (constructors and getters; setters for the mutable fills, shadows and dash), `paint(g)`, `paint(g, raster)`, `getBoxes()`, `getSlices()` (the gate's dump) | `Drawing` |
| `Fills` **(departure 1)** | an `a:spPr` made an `Area`'s fill, line, width, gradient, pattern, shadows and dashes, against a `ThemeColors`; `cx:spPr` is the same `a:spPr` | `ChartLayout` |
| `SvgWriter` **(departure 2)** | a `Drawing` as an SVG `Document`: Batik with `GradientHandler`, the PNG image handler, `TEXT_AS_SHAPES`, the root's size, viewBox and kerning style; `GradientHandler` stays unexported behind it | `ClassicChartRenderer.toSvg` |
| `ChartFrame` | title and legend placement as functions: chart size, title lines, overlay, manual layout; legend entries' widths and key kinds, position (`t`, `b`, `l`, `r`, `tr`), overlay, manual layout; returns the title box, the legend box and its entries' places, and the plot area's outer rectangle | `ChartLayout.layout()` |

- **Number formats** need nothing new.  They are core's already: xlsx4j's port of POI's
  `DataFormatter`.
- **Chartex's own placement.**  A chartex's `cx:title` and `cx:legend` take a position *and* an
  alignment, which a classic chart's don't.  `ChartFrame` offers the measured classic rules.
  Whether Excel places the 2016 types' titles and legends the same way is for enterprise CR-004's
  phase 2 to measure.  If it doesn't, the Enterprise jar places them itself, and the api doesn't
  grow chartex rules.
- **Stability.**  The api is public from 17.3.3.  Signatures change only with a deprecation, but
  the numbers behind them improve as the open items close: factor mode moves `ChartFrame`'s
  rectangles when it is solved.

### 3.4 `ChartCreator`'s host steps opened (core, phase 3)

A new public class `ChartHosting` in `org.docx4j.model.chart`, made with a `Random` (Office's ids,
seeded for repeatable files, as `ChartCreator`'s).  `ChartCreator` moves onto it, so again one
implementation:

- **Part names:**
  - `next(pkg, prefix, suffix)`, `free(pkg, prefix, n)`;
  - the embedding's name (`/word/embeddings/` or `/ppt/embeddings/`
    `Microsoft_Excel_Worksheet[n].xlsx`).
- **The embedded workbook:**
  - `embedWorkbook(Part chart, Part host, byte[] template)` names the part, types it by the
    `xlsx` default as Office does, relates it from the chart part, and returns the relationship;
  - the caller sets its own `c:externalData` or `cx:externalData` from the relationship.
- **The workbook's data:** `fill()` split so that what is written is the caller's grid, not a
  `ChartDefinition`:
  - the sheet cleared;
  - rows of cells (text, number, date) written at A1, as `inSheet` writes them;
  - `Table1` set over them, with its columns;
  - Office's random ids made afresh, and the author and times from `docx4j.properties`.
  - A treemap's hierarchy is several text columns, which a grid holds as it is.
- **The hosts:** each takes the graphic data's `uri` and element and an optional Fallback, and
  writes the frame inside an `mc:AlternateContent` with that Fallback and the given `Requires`:
  - **docx:** a run holding an inline drawing, with `docPr` from the package's tracker and the
    `wp14` anchor and edit ids;
  - **pptx:** a graphic frame added to the slide's shape tree, with the next shape id,
    `a16:creationId` and `p14:modId`;
  - **xlsx:** `place()`'s drawing part (made if missing) and its two-cell anchor.
  - This is how Office writes a chartex: a Choice holding the `cx:chart` frame; the Fallback a
    picture (Word, PowerPoint) or a text shape (Excel).
- **Not opened: the template loader (departure 3).**  `template()` reads core's own resources
  through core's class.  The Enterprise jar brings its own templates and loads them from its own
  module, and under JPMS core's resource packages aren't open to it anyway.  What is opened is
  everything after the bytes are loaded.
- **`ChartCreator.delete(ChartRef)`** is tested on a chartex in each host.  It is to take the
  `mc:AlternateContent` whole, with the Fallback's picture part.  It is changed only if it fails.

## 4. Phases

**Phase 1: the SPI, the dispatch, the Choice** (§3.1, §3.2).  This is what the Enterprise
skeleton compiles against, so it goes first and its names are frozen when it lands.

- **Tests (core-tests and export-fo-tests).**  A test renderer is set through `setRenderer` and
  draws a box, run on CR-035's `charts/waterfall.{docx,pptx}`:
  - no renderer: the Fallback picture, as today;
  - a renderer that draws: the Choice's chart in PDF, HTML and pptx SVG-in-HTML;
  - a renderer that refuses: the Fallback;
  - `fallback` and `choice` forced;
  - both renderers present: a classic chart is drawn by `ClassicChartRenderer`, a chartex by the
    test's;
  - `ChartImages.toSvg` and `.toPng` of a chartex `ChartRef`.
- **Gate:**
  - core-tests, export-fo-tests and chart-render pass;
  - the chart gate unchanged (665 drawn, 531 within tolerance);
  - the real-document corpus with zero movers against b225: without a renderer nothing may
    change.

**Phase 2: the api** (§3.3).  This is a refactor, so its gate is identity, not pass counts:

- **Gate:**
  - every probe's layout dump byte-identical before and after (all 665);
  - the pictures' PNGs identical (626);
  - `ChartRenderFixturesTest` 626.
- **Plus** a test that builds a small drawing through the api alone (scale, text, fills, frame)
  and writes its SVG.

**Phase 3: the host steps** (§3.4).

- **Gate:**
  - CR-036's creation tests unchanged, including their part-for-part comparisons with Office's
    templates;
  - a test that re-hosts CR-035's waterfall (its `chartEx1.xml`, style and colour parts, and
    workbook) through `ChartHosting` alone, into a new docx, pptx and xlsx, with the Fallback
    picture in the docx and pptx and Excel's text shape in the xlsx.
- **Office check (Jason's VM):** Word, PowerPoint and Excel open each re-hosted file without
  repair and show the chart, and *Edit Data* opens its workbook.

Phases 2 and 3 are independent of each other.  All three are in 17.3.3, per Jason's decision as
relayed.

## 5. Decisions for Jason

1. **The property** `docx4j.chart.chartex.branch` = `auto|fallback|choice`, default `auto`
   (recommended: without a renderer it is today's behaviour).
2. **`GradientHandler` behind `SvgWriter`, unexported** (departure 2; recommended: one class less
   to keep stable), or exported as listed.
3. **The template loader not opened** (departure 3; recommended, for §3.4's reason), or opened.
4. **`Fills` in the api** (departure 1; recommended: a chartex's `a:spPr` is the same, and the
   gradients, patterns and shadows were R15's measured work), or left out, so that the Enterprise
   jar translates `a:spPr` itself.

## 6. Risks

- **Phase 2 moves a probe.**  The guard is the dumps' identity, not the pass count.
- **The pre-passes and the exporter see different branches.**  `TraversalUtil` in READ mode
  (numbering, bookmarks, layout fix-ups) sees the Fallback while the exporter draws the Choice.
  Word writes the Fallback picture at the chart's extent, so the page doesn't move; phase 1's
  tests check the boxes' sizes on the fixtures.
- **Selection reads chart parts.**  In `auto`, with a renderer present, each cx Choice's chart is
  read once to be checked and once to be drawn.  Chartex charts are few in a document; a cache
  can come if measurement asks for it.
- **The Enterprise jar compiles against a moving snapshot.**  Phase 1's names are frozen when it
  lands; phases 2 and 3 add classes and don't rename them.
- **JPMS.**  The Enterprise module has `requires org.docx4j.chart.render` (for the api) and
  `provides org.docx4j.model.chart.ChartExRenderer`.  docx4j-chart-render's `module-info` gains
  the export, and core's the `uses`.

## 7. Effort

| Phase | Days |
|---|---|
| 1 | 1.5 |
| 2 | 2-3 |
| 3 | 2, plus the Office check |

## 8. Registry and hand-offs

`docx4j/CR-039`, with phases `.1` to `.3`, `proposed`.  Enterprise CR-004's `depends_on` points at
it (the Enterprise session's to set).  The ports: nothing to hand off.  The SPI, the api and the
host steps are Java's, and the outputs change only where a `ChartExRenderer` is present (charts
plan phase 10: rendering stays Java's).
