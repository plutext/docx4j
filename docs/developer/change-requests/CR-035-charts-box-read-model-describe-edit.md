# CR-035: charts - the chart's box kept in PDF and HTML, a public read model, a chart described in markdown and MCP, and a chart's data edited in xlsx (charts plan phases 0, 2, 3 and 4)

Status: PROPOSED 2026-10-11, not started. Drafted from the charts plan
(`../docx4j-portfolio/docs/charts_plan.md`, revised 2026-10-11, portfolio 1174e65) on Jason's
request ("please draft CR-034 (theme-colour resolver) and CR-035 (charts phases 0, 2, 3 and 4).
Do not implement anything yet."). The plan's D1 (option (d): the classic chart types open in
every respect, the Office 2016 types commercial) and D2 (docx4j's own renderer) are taken.
Reviewed the same day by the docx4j-13 session (Claude Fable 5.1, at Jason's request): approved
with four amendments, applied (the corpus counted for charts, §3, which moved phase 0's gate onto
probes; the generated package names, §4.1; automatic colours reported, §4.2; a copied-then-saved
fixture in the round-trip gate, §4.1, where the review's premise that the copy plugin needs
changing did not hold). The decisions of §8 are open. Owner: Jason Harrop. Drafted with Claude
Opus 5.5.

Phase numbers are the plan's: phase 1, the theme-colour resolver, is CR-034, so this CR has
phases 0, 2, 3 and 4, and no phase 1.

Scope, all open source under the plan's D1 (d):
- **Phase 0, groundwork:** every output keeps a chart's box (an empty space of the drawing's
  extent) instead of dropping the chart, so the text after it stays where Word puts it; the
  chart style, colour style and chartex parts typed; empty list attributes no longer written;
  chartex fixtures from Word and PowerPoint.
- **Phase 2, the read model:** a format-neutral, public view of a chart in xlsx, docx and pptx,
  which the ports take as their oracle and the Enterprise jar builds on.
- **Phase 3, describe:** a chart's title and data as a table in markdown; the description the
  MCP server returns.
- **Phase 4, edit data:** change a chart's data in a workbook and keep cells, table, references
  and caches consistent, for any host but tested on xlsx.

Not in scope: creating charts (plan phase 5), docx and pptx editing (phase 6: the embedded
workbook's lifecycle), drawing charts (phases 7 and 8), chartex beyond its caches (phase 9,
commercial), the ports' work (phase 10).

## 1. What docx4j does today (read from the code, 2026-10-11)

### 1.1 Every output drops a chart

| Output | Where | What happens |
|---|---|---|
| FO and HTML, visitor (the default since 17.0.4) | `AbstractVisitorExporterGenerator.apply`, the final `else` (565-577) | `log.warn("Need to handle " + ...CTRelId)`; nothing written outside debug mode |
| FO, XSLT | `docx2fo.xslt` 607-611, the `wp:inline|wp:anchor` template's `otherwise` | `notImplemented(' without pic:pic ')`: a warning, nothing written |
| HTML, XSLT | `docx2xhtml-core.xslt` 392-396 | the same |
| pptx SVG-in-HTML | `pptx2svginhtml.xslt` 226-232, the catch-all (no `p:graphicFrame` template) | a red "NOT IMPLEMENTED: support for graphicFrame" `div`, always, with no position or size |
| Markdown | `WmlToMarkdown.drawing` 1003-1009 | `log.debug("Dropping non-picture drawing")` |

The walk reaches the chart as `org.docx4j.dml.chart.CTRelId` (the `c:chart` element's `r:id`):
`TraversalUtil.handleGraphicData` returns `graphicData.getAny()` for anything but a picture or a
text box (349-351). At every docx site the drawing is in scope: the visitor still holds it in
`anchorOrInline` (set at 462-465, cleared only for a picture at 477), and the XSLT templates have
it as the context node; so `wp:extent` (`cx`, `cy` in EMU), `wp:docPr` and the relationship id
are all reachable. In pptx the frame's geometry is `p:xfrm` (not `p:spPr/a:xfrm`).

The picture code can't take a chart: `WordXmlPictureE20.createWordXmlPictureFromE20` returns null
when there is no `pic:pic` (259-264), and `createXslFoImgE20` would then throw on it (401).

What exists to build the box from:
- `WordLayoutFixups.reserveUnpaintablePictures` (2709-2735) already keeps the extent of a
  picture FOP can't paint, as a 1×1 transparent PNG scaled to it.
- `XsltFOFunctions.createShapeTextBox` (406-432) builds an `fo:block-container` from a drawing's
  extent through `FOTextBoxes.createContainer`, and for a `wp:anchor` stamps the anchor hints.
- `WordXmlPictureE20.stampAnchorHints` (441-543) writes the `docx4j-anchor` attributes that
  `WordLayoutFixups.anchorImages` (1544-1559) positions, for an `fo:external-graphic` or an
  `fo:instream-foreign-object` alike.
- CR-011's metafile route places SVG as `fo:instream-foreign-object` (`AbstractWordXmlPicture`
  206-217, sized by `sizeGraphic`) or inline `<svg>` in HTML.

`mc:AlternateContent`: every route applies `McSelection` (CR-021). Its understood prefixes are the
property `docx4j.jaxb.mc.preferChoice`, empty by default, so a chartex Choice (`Requires="cx1"`)
loses to its Fallback, as does `c14`. A chartex chart in a docx sits as
`w:r/mc:AlternateContent/{mc:Choice|mc:Fallback}/w:drawing` (`w:drawing` itself doesn't admit
`mc:AlternateContent`). The visitor logs "selecting Fallback ie ignoring mc:Choice" whichever
branch it takes (554-557), a stale line.

### 1.2 The parts

| Part | Type today |
|---|---|
| `DrawingML.Chart` | `JaxbDmlPart<CTChartSpace>` |
| `DrawingML.ChartShapePart` | `JaxbDmlPart<JAXBElement<CTDrawing>>` (`c:userShapes`) |
| `DrawingML.ChartStylePart<E>`, `ChartColorStylePart<E>`, `ChartExSpacePart<E>` | generic `JaxbXmlPart<E>`, made raw by `ContentTypeManager` (543-548); at run time they hold `CTChartStyle`, `CTColorStyle` and the chartex `CTChartSpace`, all in `Context.jc`. `WordprocessingML.ChartStylePart` and the other two are deprecated subclasses. |
| `DrawingML.ThemeOverridePart` | `JaxbXmlPartXPathAware<JAXBElement<CTBaseStylesOverride>>`, no helpers |
| `SpreadsheetML.ChartsheetPart` | `JaxbSmlPart<CTChartsheet>` |
| `DrawingML.Drawing` (a worksheet's drawing) | `JaxbDmlPart<spreadsheetdrawing.CTDrawing>` |
| `WordprocessingML.EmbeddedPackagePart` | `BinaryPart`; `extractPackage()` loads a detached `OpcPackage`; changes go back through `setBinaryData` |

**The empty `mods=""`** (plan §2): `xsd/bindings.xjb` sets
`collectionType="org.docx4j.list.ArrayListDocx4j"` globally, and with a collection type xjc
initialises every list field eagerly (`protected List<String> mods = new ArrayListDocx4j<>(this);`,
CTStyleEntry.java:78-79), so an attribute list is never null, and JAXB writes an empty list as an
empty attribute. 48 attribute-list fields in the generated code have the same initialiser, among
them `org.xlsx4j.sml.Row.spans` and the `sqref` of `CTSelection`, `CTConditionalFormatting` and
`CTDataValidation`; which of them reach the output empty is to be measured (phase 0). A part
docx4j hasn't unmarshalled is written back from its bytes, so only touched parts show it.

### 1.3 Reading and editing

- **No main code navigates a `CTChartSpace`** apart from the anonymiser (`ScrambleText`
  466-500, `MarkupScrubber` 404). The series classes have no common interface (`CTBarSer`,
  `CTLineSer`, `CTPieSer`, `CTAreaSer`, `CTScatterSer`, `CTBubbleSer`, `CTRadarSer`,
  `CTSurfaceSer`); the plot area is `getAreaChartOrArea3DChartOrLineChart()`, a `List<Object>`.
  `CTChartSpace.getStyle()` is null in Office's files (the `c:style` is inside
  `mc:AlternateContent`, kept since CR-024).
- **Nothing follows `c:externalData` to the workbook**; the sample `EditEmbeddedCharts` finds the
  parts by hard-coded name.
- **xlsx4j** has read-only getters (`org.xlsx4j.model.CellUtils`; its private `getFormat` knows
  only the built-in number formats) and the repackaged POI `DataFormatter`, which formats a number
  by any format code (`formatRawCellContents(double, int, String)`, DataFormatter.java:812). There
  is no helper to write a cell, add a shared string (`SharedStrings` has none, and nothing in main
  code appends to it), find or add a number format (`Styles` has `getXfByIndex` only), reach a
  worksheet's tables (`WorksheetPart` has no accessor; `TablePart` has no helpers) or a defined
  name, and **no A1 reference utility anywhere**. `SpreadsheetMLPackage.createPackage` adds no
  styles or shared-strings part.
- **Drawings in xlsx:** `Worksheet.getDrawing()` gives an `r:id`, resolved by hand to the
  `Drawing` part; its `getEGAnchor()` mixes `twoCellAnchor`, `oneCellAnchor`, `absoluteAnchor` and
  `mc:AlternateContent`; `GraphicData` has `getPic()` and `getWordprocessingShape()` but no
  `getChart()`.
- **Tests and fixtures:** `ChartDataDisplayOptionsTest` and
  `DrawingAlternateContentKeptTest.chartStyleKeptInWordPowerPointAndExcel` round-trip
  `loadAndSave.{docx,pptx,xlsx}`; the anonymiser's tests use `anon/chart.docx`,
  `anon/pptx-chart.pptx`, `anon/strict-chart.xlsx`, `strict/strict-chart.docx`. **No chartex
  document exists anywhere in the repository.** The layout-fidelity harness neither counts nor
  skips charts.
- **The MCP server** (`../docx4j-mcp`): one class per tool, listed in `Docx4jMcpServer` (76-84);
  `describe_template` (`TemplateInspector.summary`, 354-376) counts paragraphs and tables, and no
  tool mentions charts.

## 2. What Office writes (the plan's §3, and the goldens)

The probes are on the share, `Office 2016/office_charts`: six Word *Insert Chart* defaults;
their embedded workbooks with the same chart made in Excel, two of them then edited (axis
titles, data labels and a number format in `bar chart.xlsx`; the table grown in
`stacked column.xlsx`); and `waterfall.xlsx`. With them, Word's PDFs (`goldens-word\`) and
Excel's PDFs and chart PNGs (`goldens-excel\`). The plan's findings 1 to 7 hold here; those
that shape this CR:

- **The data lives in a table** (`Sheet1!A1:D5`, `Table1`); the chart holds references and
  caches. Editing keeps four things consistent: the cells, the table's range, the chart's
  references, the caches; and a new series gets a fresh `c16:uniqueId` (finding 3).
- **Excel grows a chart with its table** (finding 4): in `stacked column.xlsx` a row and a
  column were added to the table, and every reference, the table and a fourth series followed.
  Excel's own edit is phase 4's oracle; the state before it was not saved, so phase 4 asks for
  the pair (§5).
- **Chartex** (finding 5): a separate part, data through hidden defined names
  (`_xlchart.v1.N`), and a Fallback text shape in `mc:AlternateContent`.
- **Titles** (read from Word's goldens, 2026-10-11): each default chart has a `c:title` with no
  `c:tx` and `c:autoTitleDeleted val="0"`. Word prints "Chart Title" over the five charts with
  three series, and the series name, "Sales", over the pie, which has one; *Chart Style 2*
  prints it in capitals (`cap="all"` in its `c:txPr`). So the title Office shows is derived, and
  the read model has to say what it is (§4.2).

## 3. Corpus facts

Counted 2026-10-11 (a zip listing of the four real-document corpora, counts only): **none of the
561 documents has a chart part**, and no part name in any of them contains "chart". Their
embeddings are 91 `.bin`, 6 `.xls`, 1 `.xlsx` and 1 `.vsd` (OLE objects and packages, not chart
workbooks). So the corpora can't show the box working: over them phase 0's gate is a control
that nothing moves, and the box itself is gated on chart probes (§4.1, §5).

## 4. Design

### 4.1 Phase 0: the box, the parts, the empty attributes

**The box.** A chart is written as an empty box of its drawing's extent, placed as a picture of
that extent would be: inline in the line, or floating through the same anchor hints.
- FO (visitor and XSLT): an `fo:instream-foreign-object` holding an empty `<svg>` with
  `width`/`height` in points, sized by `sizeGraphic`'s rule, with `fox:alt-text` from
  `wp:docPr` (`descr`, else `name`: "Chart 1"); for a `wp:anchor`, `stampAnchorHints`, so
  `WordLayoutFixups.anchorImages` positions it as it does a floating picture. Phase 8 later puts
  the drawing into the same element, so nothing about placement changes then (§8 D1).
- HTML (visitor and XSLT): an empty inline `<svg>` with the same size and an `aria-label`.
- pptx SVG-in-HTML: a `p:graphicFrame` holding a chart becomes an absolutely positioned empty
  `div` at its `p:xfrm`, as the `p:txBody` template already sizes text (`cx*96 div 914400` px);
  frames holding tables, diagrams or OLE objects keep the catch-all (§8 D2).
- Markdown: nothing in phase 0; phase 3 describes the chart there.
- **Probes for the box**, since the corpora have no chart (§3): built in the fidelity harness by
  copying a probe's chart, with its style, colour and embedded workbook parts, into documents
  with text around it: inline mid-paragraph, alone in a paragraph, floating with square,
  top-and-bottom and behind-text wrapping, in a table cell, in a header, two on one page, and
  one that pushes the text after it onto the next page. Word's goldens from one
  `WordGoldenRunner` run.
- One helper builds it, `org.docx4j.model.chart.ChartBox` (from an `Inline`/`Anchor`, or the
  XSLT's node), so the visitor and the two XSLTs share it. The visitor recognises the chart by the
  object it visits: `org.docx4j.dml.chart.CTRelId`, or
  `org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex.CTRelId` if a user prefers the
  `cx1` Choice. The stale McSelection log line goes.

A chartex chart, by default, prints its Fallback: Office 2013's view of it, as D1 (d) intends. The
phase 0 fixture shows what Word's Fallback in a docx is (a text shape, as in Excel, or a
picture); if it is the text shape, its sentence appears in docx4j's PDF where Word draws a chart.

**The parts.** `ChartStylePart extends JaxbXmlPart<CTChartStyle>` and `ChartColorStylePart
extends JaxbXmlPart<CTColorStyle>`, both classes in
`org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle` (with `CTStyleEntry`);
`ChartExSpacePart extends JaxbXmlPart<CTChartSpace>`, the class in
`org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex` (whose `CTRelId` is the
`cx:chart` reference). There is no `org.docx4j.dml.chartex` or `chartstyle` package. Made typed
by `ContentTypeManager`; the deprecated `WordprocessingML` subclasses follow. Dropping the type
parameter breaks code that declares one (§8 D3).

**Empty attribute lists.** Measure first: round-trip the chart fixtures and the corpus and list
which of the 48 fields come out as empty attributes. Then fix all 48 the same way in
docx4j-generated-objects: no initialiser on an `@XmlAttribute` list (the getter's lazy creation
stays), by an XJC plugin step beside the existing ones, and regenerate (§8 D4). The deep copy
needs no change: the generated `copyTo` makes a new target and calls the target's getter only for
each item it copies (CTStyleEntry.java 358-362, 425-429), so an empty list copies as null once
the initialiser is gone. The round-trip gate includes a part deep-copied and then saved, to
confirm it.

**Fixtures.** Jason's office_charts probes go into `docx4j-core-tests/src/test/resources/charts/`
(13 files, 15-28 KB each; hyphenated names) with Word's and Excel's goldens kept on the share (§8
D5), plus two new ones from Word and PowerPoint: a waterfall chart inserted in each, the first
chartex documents in the repository.

**Left as it is:** the `c16r2` binding waits until Office is seen writing an element in it.

### 4.2 Phase 2: the read model (`org.docx4j.model.chart`)

Public API (§8 D6). It reports what the file says and, where Office derives what it shows, what
Office shows, each named as such. It holds the JAXB objects it read, so a caller can always go
deeper.

**Finding charts.** `Charts.in(OpcPackage)` returns each placed chart in document order as a
`ChartRef`: its host part (main document, header, footer, footnotes; slide, layout, master;
worksheet drawing; chartsheet), its anchor (`wp:inline`/`wp:anchor`, `p:graphicFrame`,
`xdr:twoCellAnchor`/`oneCellAnchor`/`absoluteAnchor`) and extent, its name and description, its
chart part (`Chart` or `ChartExSpacePart`), and whether it was found in an `mc:AlternateContent`
Choice. `ChartRef.of(Part host, Object anchor)` serves the exporters' sites.

**The model.** `ChartModel.read(ChartRef)` (and `read(Chart, ThemeColors)` for a part alone):
- **Chart groups** in document order, each with its kind (area, bar, bubble, doughnut, line,
  of-pie, pie, radar, scatter, stock, surface, and the 3-D variants), bar direction, grouping
  (standard, clustered, stacked, percent stacked), `varyColors`, the kind's settings (gap width,
  overlap, hole size, first slice angle, scatter and radar style, markers, smoothing) and its
  axes. Several groups sharing axes (combination charts) and a second pair of axes are modelled
  from the start, as the plan requires, though D3 of the plan defers drawing them.
- **Series**, through one adapter over the eight series classes: `idx`, `order`, name, categories
  (string, number or multi-level: `c:multiLvlStrRef` is read, where POI fails), values (and
  x/y values, bubble sizes), each as a reference (`c:f`) plus points **keyed by `c:pt/@idx`**
  with `ptCount` (POI's positional reading is wrong for a cache with gaps), the format code, data
  point overrides by index, data labels (the series' over the group's), the `c16:uniqueId`, and
  the shape properties with the fill and line colour resolved through CR-034. A series (or
  point) with no fill of its own is reported as **automatic**, with the chart's style number and
  the series' index, not as null, so the renderer, the Enterprise jar and the ports share one
  answer; the colours an automatic series gets are phase 7's, from the style and colour parts.
- **Axes:** id, kind (category, value, date, series), the axis it crosses, position, deleted,
  scaling (min, max, orientation, log base), units, number format (and `sourceLinked`), crossing
  (`crosses`/`crossesAt`, `crossBetween`), tick marks and label position, gridlines, title.
- **Title, legend:** the title's text as written, or, with no `c:tx`, as Office shows it (§2:
  "Chart Title", or the only series' name; none when `autoTitleDeleted`); the text's capitals
  as its properties say. Legend position, overlay, deleted entries.
- **Chart-wide:** `dispBlanksAs`, `plotVisOnly`, `date1904`, `roundedCorners`, the style number
  (`c14:style` or `c:style`, through McSelection), `c:clrMapOvr`.

**Where values come from** (§8 D7): the caches by default, which are what Office draws until the
data is edited and all a docx or pptx guarantees (an embedded workbook can be missing or
external). On request, the cells: in xlsx the host workbook's own sheets, in docx and pptx the
embedded workbook (`extractPackage`); with both, the differences are reported per point.

**Chartex, the open side of D1 (d):** a `ChartExModel` with the series' layouts (waterfall,
funnel, ...), names, and data read from the caches in the part (`cx:strDim`/`cx:numDim`,
`cx:lvl`/`cx:pt`), enough to describe the chart and tabulate its data. Resolving the hidden
`_xlchart.v1.N` names to cells, and the layouts Excel computes (bins, quartiles, treemap and
sunburst areas), are phase 9's, commercial.

**A JSON form** (`ChartModel.toJson()`), with a version field and a fixed field order: the
ports' oracle (goldens for every fixture, in `docx4j-core-tests`, which core-ts and python
compare against, as they do the strict fixtures) and the MCP server's payload (§8 D8).

**More probes** (one Excel run): one workbook per chart kind the model reads that the probes
lack (area, scatter, doughnut, radar, bubble, stock, surface, bar of pie), a combination chart
with a secondary axis, a chartsheet, a chart with a gap in its data and with multi-level
categories, and a chart pasted into PowerPoint.

### 4.3 Phase 3: describe

- **Markdown** (`WmlToMarkdown.drawing`): a chart becomes its title as a paragraph and its data
  as a table, categories down and series across, numbers formatted by their format code through
  the repackaged `DataFormatter`; a chartex chart the same from its caches. A chart with no
  categories (a scatter chart) lists x and y.
- **HTML:** the box only by default; the same table on a property, for readers without the
  picture (§8 D9).
- **MCP:** the docx4j side is the JSON form of §4.2; the tool is the MCP session's (a chart
  count in `describe_template`'s summary, and a tool returning a chart's JSON). Hand-off, §9.

### 4.4 Phase 4: edit a chart's data

Written for any host: it takes a chart and the workbook its data is in, so phase 6 reuses it on
the embedded workbook. Phase 4 builds it, and tests it, on xlsx.

`ChartDataEditor` (`org.docx4j.model.chart`):
- `setValue(series, point, double)`, `setCategory(point, String)`, `setSeriesName(series, String)`:
  write the cell the reference names, then refresh that series' cache.
- `setData(categories, seriesNames, values)`: replace the table's data, growing or shrinking
  every reference and the table as Excel does.
- `addSeries(name, values)`, `removeSeries(series)`, `addCategory(...)`,
  `removeCategory(...)`: a new series is a copy of the group's last one, given `idx` and `order`
  one above the largest in the whole chart (POI reuses them), a fresh `c16:uniqueId`, references
  to its new column, and its colour from the chart's colour style (`cs:colorStyle meth="cycle"`:
  the next accent, with the style's variation each time round), as Excel colours a new series.
- `refreshCaches()`: every cache from the cells: `ptCount`, a `c:pt` per non-blank cell by
  index, the format code from the cell's number format.
- The table grows as finding 4 measured: `ref`, `autoFilter`, `tableColumns` (count, names, ids).

A cell holding a formula is not overwritten unless asked (§8 D10); when an edit changes cells in
a workbook that has formulas, `calcPr/@fullCalcOnLoad` is set so Excel recalculates on open.

**Cell writers** (§8 D11), only what this needs, in `org.xlsx4j.model`: A1 references
(`CellRef`, `AreaRef`, with quoted sheet names and `$`), find or create a row and a cell in
order, write a number or a shared string (creating the shared-strings part if missing), find or
add a number format and its `cellXfs` entry, and a table's range. Not a general cell API.

## 5. Gates and oracles

- **Phase 0:** the box probes of §4.1 scored against Word's goldens: the text before and after
  each chart at Word's position and on Word's page; the layout-fidelity harness over the four
  corpora against the current baseline, with `tools/movers.py`, as a control (no document has a
  chart, §3, so nothing may move); the round trip of every chart fixture, and of a part
  deep-copied then saved, with no empty attributes; the chartex fixtures open in Word and
  PowerPoint after a docx4j save.
- **Phase 2:** the JSON goldens of every fixture, read against the probes' known data and
  Excel's goldens (titles, axis titles, data labels, the `0.0` format of `bar chart.xlsx`);
  combination, secondary-axis, gap and multi-level probes read right.
- **Phase 3:** markdown of the probes shows their data exactly; the HTML property's table.
- **Phase 4:** the editor's own version of Excel's edit compared with Excel's, part by part,
  ids aside: Excel saves a chart's workbook before and after growing its table (as in
  `stacked column.xlsx`), and the editor makes the same edit to the first. Then Excel opens each
  edited probe without repair, *Select Data* shows the ranges, and its chart PNG
  (`excel-goldens.ps1`) matches the data. Needs Jason on the Windows VM.

## 6. Phases

| Phase | What | Depends on | Effort |
|---|---|---|---|
| 0 | §4.1: the box in the visitor (FO and HTML), the two XSLTs and the pptx SVG-in-HTML; the three parts typed; the empty attributes measured and fixed; the probes committed as fixtures; the box probes (§4.1) and their Word goldens; the chartex fixtures (Jason: a waterfall in Word and in PowerPoint). | | 2-3 days + Word/PowerPoint |
| 2 | §4.2: `Charts`, `ChartRef`, `ChartModel`, `ChartExModel`, the JSON form and its goldens; the extra probes (one Excel run). | CR-034 (its resolver) | 6-8 days + Excel |
| 3 | §4.3: markdown, the HTML property; the MCP hand-off. | 2 | 1-2 days |
| 4 | §4.4: `ChartDataEditor`, the cell writers; the Excel gate. | 2 | 5-7 days + Excel |

14-20 working days, as the plan has them. Each phase ends with CHANGELOG, this CR's record, the
registry entry, and the hand-offs it triggers.

## 7. Risks

- **Widely shared code.** Phase 0 touches `AbstractVisitorExporterGenerator`, both XSLTs and the
  generated objects; CLAUDE.md prefers Fable for such changes. The movers gate and the round trip
  bound the risk.
- **The empty-attribute fix regenerates every JAXB class** that has an attribute list; the
  round-trip tests and the corpus round trip are its gate, and core-ts and python, whose
  generators differ, are told what changed.
- **Chartex Fallback text in PDF.** If Word's Fallback is the text shape, docx4j's PDF prints
  "This chart isn't available in your version of …" where Word draws the chart. That is D1 (d)'s
  open behaviour; whether to print the box instead is a question for phase 0's fixture.
- **The derived title and others.** Office's derived values (the title, automatic axis scaling,
  the colours of automatic series) are many; the model names what probes have measured and marks
  the rest as automatic (§4.2), leaving the values to the renderer.
- **Excel's growth rules** are measured on one probe (finding 4); totals rows, tables without a
  header row and charts referring outside a table are left to later probes, and the editor
  refuses what it can't do as Excel would.
- **The read model is a promise.** The ports and the Enterprise jar build on it, so its shape is
  hard to change once 17.3.x ships; phase 2's review is the moment to change it.

## 8. Decisions for Jason

- **D1. The box's form:** an `fo:instream-foreign-object` holding an empty SVG (HTML: an empty
  inline SVG), which phase 8 fills; or a transparent PNG, as `reserveUnpaintablePictures` does.
  Empty, with the chart's name as alternative text, rather than a frame or the title, since Word
  draws no frame and phase 8 replaces it. Recommendation: the empty SVG.
- **D2. pptx SVG-in-HTML in phase 0:** a chart's frame becomes a positioned empty box instead of
  the red "NOT IMPLEMENTED" text; other frames unchanged. Recommendation: yes.
- **D3. Typing the three parts:** drops their type parameter, a source change for code that
  declares one; noted in CHANGELOG. Recommendation: yes.
- **D4. Empty attribute lists:** fix all 48 fields in the generated objects, after measuring,
  rather than the three `cs:` ones. Recommendation: all 48.
- **D5. The probes as fixtures:** commit Jason's 13 office_charts files (made from scratch in
  Office, no customer content). Recommendation: yes.
- **D6. Package:** `org.docx4j.model.chart` in docx4j-core, no new dependency; the renderer
  (phase 7) later in its own module, since it needs Batik. Recommendation: yes.
- **D7. Values:** from the caches by default, the cells on request, differences reported.
  Recommendation: yes.
- **D8. The JSON form as public API**, the ports' oracle and the MCP payload, versioned.
  Recommendation: yes.
- **D9. HTML:** the box only by default, the data table on a property. Recommendation: yes.
- **D10. Formula cells and recalculation:** an edit refuses a formula cell unless asked; sets
  `fullCalcOnLoad` when the workbook has formulas. Recommendation: yes.
- **D11. Cell writers:** public in `org.xlsx4j.model`, limited to §4.4's list, since phases 5 and 6
  need them too; or package-private in `org.docx4j.model.chart`. Recommendation: public and
  limited.

## 9. Hand-offs

- **docx4j-core-ts, docx4j-python:** the read model and its JSON goldens (phase 2), the editor
  (phase 4); python's CR-004 phase F and CR-005 phase G (chart creation) later build on them.
- **docx4j-mcp:** the chart count and the chart description tool (phase 3).
- **Enterprise:** chartex beyond its caches (plan phase 9, commercial) builds on the read model,
  as the open renderer (phases 7 and 8) will.
- **The layout-fidelity work:** the corpora have no chart (§3), so the box doesn't move their
  baselines; the box probes join the probe set.

## 10. Effort (rough)

14-20 working days, plus Office runs: Word and PowerPoint (phase 0's chartex fixtures), Excel
(phase 2's probes, phase 4's gate).
