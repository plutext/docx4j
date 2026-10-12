# CR-035: charts - the chart's box kept in PDF and HTML, a public read model, a chart described in markdown and MCP, and a chart's data edited in xlsx (charts plan phases 0, 2, 3 and 4)

Status: DONE 2026-10-12, all four phases. Phase 4 DONE 2026-10-12 (Jason: "the edit probes have
run"; D10 and D11 as recommended): `ChartDataEditor` and the cell writers, gated on 21 of Excel's
own edits; Office check passed (no repair; 19 of 21 charts pixel for pixel Excel's, r1 and r5 as D12
says); D12 (a middle series removed) decided the editor's way (Jason: "D12 recommendation
accepted."; §8, §11).  Phase 3 DONE 2026-10-12 (Jason: "please commit then continue"; D9 as
recommended): a chart described in markdown and, on a property, in HTML; the MCP hand-off in the
registry (§11).  Phase 2 DONE 2026-10-12 (Jason: "Next is CR-035 phase 2"; D6 to D8 built as
recommended): the read model, `org.docx4j.model.chart`, with its JSON form and goldens for every
fixture; MS-OI29500's notes on §21.2 read first; 16 probes made by Excel itself (§11).  Phase 0
built 2026-10-12 (Jason: "do Phase 0 of each CR"; D1 to D5 built as recommended): the chart's box in
FO, HTML and pptx's SVG-in-HTML, the three parts typed, the 48 attribute lists starting null
(measured first: only the chart style parts' mods), the fixtures, three box probes; gate b221 to
b222 0 movers; against Word's goldens the box probes go from 0.8449 to 0.9358 line parity,
chart-box-inline to 1.0000.  Word's and PowerPoint's chartex Fallback is a picture of the chart,
which docx4j already draws; Word and PowerPoint open docx4j's re-saves of them (§11).  Phase 0 DONE
2026-10-12; phase 2 next (it needs CR-034's resolver).  Proposed 2026-10-11. Drafted from the charts
plan (`../docx4j-portfolio/docs/charts_plan.md`, revised 2026-10-11, portfolio 1174e65) on Jason's
request ("please draft CR-034 (theme-colour resolver) and CR-035 (charts phases 0, 2, 3 and 4). Do
not implement anything yet."). The plan's D1 (option (d): the classic chart types open in every
respect, the Office 2016 types commercial) and D2 (docx4j's own renderer) are taken. Reviewed the
same day by the docx4j-13 session (Claude Fable 5.1, at Jason's request): approved with four
amendments, applied (the corpus counted for charts, §3, which moved phase 0's gate onto probes; the
generated package names, §4.1; automatic colours reported, §4.2; a copied-then-saved fixture in the
round-trip gate, §4.1, where the review's premise that the copy plugin needs changing did not hold).
The decisions of §8 are open. Owner: Jason Harrop. Drafted with Claude Opus 5.5.

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

What the file says comes from ISO/IEC 29500-1 §21.2 (each element's meaning and default); what
Office shows starts from [MS-OI29500]'s notes on §21.2, read before phase 2's probes are cut, so
probes measure only what the notes leave open (the charts plan's §11, added 2026-10-12).

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
- **Filtered series and categories** (added 2026-10-12, from the specifications review): a series
  hidden with Excel's chart filter is not a `c:ser` but a `c15:filteredBarSeries` (or one of its
  seven siblings, [MS-ODRAWXML] §2.6.3) in the group's `c:extLst`, and hidden categories are
  `c15:categoryFilterExceptions`; all are bound, in
  `org.docx4j.com.microsoft.schemas.office.drawing.x2012.chart`.  The model reads them as series
  and categories marked hidden, not dropped: a description shows the data Office shows, and the
  editor (§4.4) sees every series that holds an `idx`.
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
categories, a chart with a series and a category filtered out, and a chart pasted into
PowerPoint.

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
  one above the largest in the whole chart, filtered series included (they share the `idx` and
  `order` sequences; POI reuses them), a fresh `c16:uniqueId`, references
  to its new column, and its colour from the chart's colour style (`cs:colorStyle meth="cycle"`:
  the next accent, with the style's variation each time round), as Excel colours a new series.
- `refreshCaches()`: every cache from the cells: `ptCount`, a `c:pt` per non-blank cell by
  index, the format code from the cell's number format; a filtered series' caches and
  references too, so that showing it again in Excel shows the edited data.
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
  (Answered 2026-10-12: Word's and PowerPoint's Fallback is a picture of the chart, which docx4j
  draws; only Excel writes the sentence, and docx4j renders no workbooks.  §11.)
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
- **D12. A middle series removed** (raised by phase 4's probes, §11): deleting a table's middle
  column, Excel keeps the chart's series by position (the second `c:ser`, its idx and colour, then
  plots what was the third's data, and the last `c:ser` goes); `removeSeries(series)` removes the
  series it is given, its formatting with it, and moves the cells as Excel does.  Keep the
  editor's way, or follow Excel's?  Recommendation: the editor's: its caller names the series to
  remove, and a series' own formatting (a highlight in red) shouldn't move to its neighbour's
  data.  `setData`, which removes series from the end, comes to the same as Excel either way.
  **Decided 2026-10-12: the editor's way** (Jason: "D12 recommendation accepted.").

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

## 11. Implementation record

### Phase 0 (2026-10-12), done

Jason: "do Phase 0 of each CR".  D1 to D5, the decisions phase 0 rests on, are built as
recommended (the empty SVG; pptx's box; the parts typed; all 48 attribute lists; the probes as
fixtures); none was confirmed separately, so any can be revisited before the push.

**The box.** `org.docx4j.model.chart.ChartBox` (docx4j-core, package exported) makes it from a
`wp:inline` or `wp:anchor`, or the XSLT's node: an `fo:instream-foreign-object` with
`content-width`/`content-height` of the extent, `scaling="non-uniform"`, the drawing's name as
`fox:alt-text` and an empty SVG; for a `wp:anchor` the anchor hints, so `WordLayoutFixups`
floats it as it does a picture.  HTML: an empty inline `<svg>` sized as a picture's `<img>` is
(points as pixels), with `role="img"` and `aria-label`.  Wired in:
- the visitor: `AbstractVisitorExporterGenerator.apply` matches `ChartBox.isChartReference`
  (a `c:chart`, or the chartex `CTRelId`) and calls `createChartBox`, which the FO and HTML
  generators override; the stale "selecting Fallback ie ignoring mc:Choice" log line now says
  what happens, at debug;
- `docx2fo.xslt` and `docx2xhtml-core.xslt`: a `when` for `a:graphicData/@uri` chart or chartex
  before the `notImplemented`;
- `pptx2svginhtml.xslt`: a `p:graphicFrame` holding a chart becomes an absolutely positioned
  empty `div` (`role="img"`, `aria-label` from `p:cNvPr`) at its `p:xfrm`.

Tests: `ChartBoxTest` (core: Word's default chart is 432 x 252pt "Chart 1"; HTML on both
pathways one svg of that size, no "NOT IMPLEMENTED"; the pptx frame at 637 x 453px, (511, 163),
"Chart 3"), `ChartBoxFoTest` (export-fo: the FO element on both pathways; in the PDF the text
after an inline chart, and after a top-and-bottom anchored one, 252 to 300pt below the text
before it).  No "Need to handle" or parent-mismatch warning is logged on either pathway.

**The parts** are typed (`ChartStylePart extends JaxbXmlPart<CTChartStyle>`, and so on); the
deprecated `WordprocessingML` subclasses follow.  Enterprise MergeDocx uses all three raw and
compiles unchanged.

**Attribute lists, measured then fixed.** Round-tripping the 13 chart fixtures,
`loadAndSave.{docx,pptx,xlsx}`, the anonymiser's xlsx, pptx and xlsm fixtures and the CR-022
workbooks with every JAXB part read: the only attribute that came out empty was `mods`, on the
chart style parts, 2,646 times (1,029 in docx, 1,470 in xlsx, 147 in pptx), the same with every
part deep-copied.  The four corpora (561 docx): none of the 48 fields.  The fix is a step in
`ModifyGeneratedSources.java` after XJC: an `@XmlAttribute` list field loses its initialiser (48
fields; the getter still creates the list).  After a clean regeneration: none, plain or copied
(`ChartStylePartsTypedTest`, which also checks the `mods` Office wrote are kept).

**Fixtures:** `docx4j-core-tests/src/test/resources/charts/` (the 13 probes, hyphenated, with a
README); a copy of `bar-chart.docx` in docx4j-export-fo-tests.

**The box probes** (`Corpus`): `chart-box-inline` (alone, mid-line, two on a page, one that
doesn't fit after 20 exact lines), `chart-box-floating` (square at the right margin,
top-and-bottom, behind text, square at an offset), `chart-box-places` (in the header, in a table
cell), each chart a copy of Word's default (`chart-template.docx`, the harness's own copy of
`clustered-column.docx`) with title, axes and legend deleted, so every scored line is the
document's.  `Doc.inlineChart`/`anchoredChart` copy the chart part with its style, colour and
workbook parts; `Doc.rehome` re-relates a chart to a header.  docx4j's renders read as intended
(4, 4 and 2 pages; the header's chart pushes the body down to 91pt).  On the share's
`fidelity/corpus` with their `corpus.txt` lines; Word's goldens wait for Jason's run.

**Gate (control).** b221 is HEAD before phase 0 (aa91f312e, built in a separate worktree), b222
phase 0, both on the released renderer 2.11-docx4j.6, over the four corpora and the 270 scored
probes: `movers.py b221 b222` reports 0 movers and every event count unchanged; the probe
scoreboards are identical cell for cell.  (No corpus document has a chart, §3, so this shows
only that nothing else moved.)  Against the previous baseline b219, b221 itself moves one
document, 5075 (in real2 and real-c2), by one line: a change between 17.3.2's b219 and HEAD,
not phase 0's, and not investigated here.  Tests: docx4j-core-tests 1,421 (0 failures, 11
skipped), docx4j-export-fo-tests 788 (0, 9 skipped), docx4j-markdown, docx4j-diffx-tests and
docx4j-digsig green, on the regenerated objects.

**The box probes against Word** (Jason's `WordGoldenRunner` run, 2026-10-12), scored before the
box (b221's jars) and with it (b222's):

| Probe | Line parity | Pages (Word's) |
|---|---|---|
| chart-box-inline | 0.9111 → **1.0000** (45 of 45 lines) | 3 → 4 (4) |
| chart-box-floating | 0.6757 → 0.8514 | 4 → 4 (4) |
| chart-box-places | 0.9853 → 0.9853; page parity 0.9265 → 0.9853 | 2 → 2 (2) |
| all three | 0.8449 → 0.9358; page parity 0.9111 → 0.9853 | |

chart-box-floating's remainder is its fourth case, a square-wrapped chart 1in from the column's
edge: Word sets text down both sides of it, docx4j only on the right.  That is the anchored
picture's behaviour, not the box's (both-sides wrap, listed for the fork in CR-032), so the box
adds nothing to fix there.  (The VM's harness has docx4j 17.1.1-SNAPSHOT, whose trailing-bytes
check falsely reported two of the probes, those over 64 KB; fixed in 17.2.1.)

**The chartex fixtures** (`charts/waterfall.docx`, `charts/waterfall.pptx`: *Insert Chart >
Waterfall* in Word and in PowerPoint, Jason, 2026-10-12).  The chartex part is in
`mc:AlternateContent` with `Requires="cx1"`, as in Excel, but **the Fallback is a picture of the
chart Office drew** (`pic:pic`, a 577 x 337 PNG in Word, 855 x 571 in PowerPoint), not Excel's
"This chart isn't available in your version of …" text shape.  So in a docx or pptx docx4j's
default output already shows a chartex chart, as Office's picture of it: the PDF of
`waterfall.docx` draws the PNG at 432 x 252pt (checked), and `ChartBoxTest` checks the HTML on
both pathways.  §7's risk of the Fallback's sentence in a PDF applies to none of docx4j's
outputs.  The picture is as old as Office's last save, so a chart whose data changes later
(phase 6) leaves it stale: for the plan's phase 9 and D5.  The fixtures are typed-part fixtures
too (`ChartStylePartsTypedTest`: 17 style and colour parts, 3 chartex).

**docx4j's re-saves of the two open in Office** (Jason, 2026-10-12, `office-colours.ps1` on
`fidelity/cr035-chartex/resaved/`, every XML part read and written back): no repair prompt, and
Word and PowerPoint each drew the waterfall from its chartex part, as live text and shapes ("Chart
Title", the legend, the axis values), not the Fallback picture.  Phase 0 is done.

### Phase 2 (2026-10-12), done

Jason: "Next is CR-035 phase 2".  D6 (the package), D7 (values from the caches, the cells on
request) and D8 (the JSON form, versioned, as public API) are built as recommended, none
confirmed separately.

**The specifications first.**  [MS-OI29500]'s 172 notes on ISO/IEC 29500-1 §21.2 (2.1.1428 to
2.1.1599), with MS-OE376's and [MS-ODRAWXML]'s on the chart extensions, were read before any probe
was cut (a research agent; the summary and the note map are in `CR-035-notes/`).  What the model
takes from them: a title with no text shows a localized default, or a single series' name
(2.1.1561); a series with no name is "Series" and its idx plus one (2.1.1565); varyColors is
ignored on area, 3-D area and stock charts, on filled radar charts, and with more than one series
(2.1.1571); a pie chart shows its first series only (2.1.1519); the data label positions Office
uses where none is stated (2.1.1456) and the layering group, series, label (2.1.1457); the blank
cell methods each kind supports and its default (2.1.1452); scatterStyle as Office reads it
(2.1.1594); c:style is c14:style less 100 ([MS-ODRAWXML] 2.7.3.5); axPos is ignored (2.1.1433, so
the model reports it as stated).  No note says what Office does where a boolean element is absent
altogether, and MS-OE376's "default false" notes are Office 2007's: not used.

**The probes** (`docx4j-layout-fidelity/tools/excel-chart-probes.ps1`, Jason's run 2026-10-12):
Excel made 15 workbooks and a chart pasted into PowerPoint, rendered with `excel-goldens.ps1`;
committed as fixtures (`charts/p2-*`, README).  Measured from them: Excel records a category
hidden with its chart filter by narrowing every series' `c:f` to the cells still shown, a union
(`(Sheet1!$A$2:$A$3,Sheet1!$A$5)`), its cache to their points, and the whole range as
`c15:fullRef`, not with `categoryFilterExceptions`; a series hidden is a `c15:filteredBarSeries`
whose `c:f` is a `c15:formulaRef`.  A chartex part in a workbook has no caches, only formulas
naming hidden defined names (`_xlchart.v1.N`); Word's and PowerPoint's have them.  In an xlsx or
a pptx, docx4j leaves the content of an `mc:AlternateContent` Choice it does not select as DOM
elements (in a docx, as JAXB objects), so a chartex chart's frame is read from the DOM there.

**`org.docx4j.model.chart`** (docx4j-core):
- `Charts.in(OpcPackage)`: every placed chart in document order as a `ChartRef` (host part,
  anchor, extent, name, description, chart or chartex part, whether in a Choice): a docx's body,
  headers, footers, notes and comments; a pptx's slides, layouts and masters (groups and
  AlternateContent included); an xlsx's sheets in order, worksheets and chartsheets.
  `ChartRef.of(host, anchor)` serves one anchor; `getHostTheme()` is the host's (CR-034).
- `ChartModel.read(Chart, ThemeColors)`: chart-wide settings (style and c14:style, date1904,
  roundedCorners, lang, plotVisOnly, dispBlanksAs, showDLblsOverMax), title (`ChartTitle`: text as
  written, its cell, the text shown), legend (`ChartLegend`), groups (`ChartGroup`: kind, bar
  direction, grouping, varyColors and whether Office varies colours, the kind's settings, axis ids,
  data labels, the blank cell method Office uses, the series shown), series (`ChartSeries`, one
  adapter over the eight classes: idx, order, name and the name shown, categories, values, x and y
  values, bubble sizes as `ChartData` keyed by `c:pt/@idx` with `ptCount`, references, full
  references and point format codes, multi-level categories innermost first; hidden; c16:uniqueId;
  fill and line as `ChartFill`, automatic where unstated, a solid one's colour resolved; point
  overrides; smooth, marker, explosion, invertIfNegative; data labels, the series' over the
  group's, `ChartDataLabels`), axes (`ChartAxis`: kind, id, crossing, position, deleted, scaling,
  units, number format, crosses, crossBetween, tick marks, tick labels, gridlines, title).
- `ChartExModel`: a chartex chart's series (layout, hidden, uniqueId, formatIdx, name and its
  formula, subtotals), each with its data dimensions (type, formula, levels from the caches),
  axes, legend, and the title shown.
- `ChartCells`: the cells a reference names (quoted sheet names, `$`, unions), in the workbook or
  the workbook embedded with the chart (`c:externalData`), and every point where they and the
  caches differ.
- `toJson()` on both models: version 1, a fixed member order, cache numbers as the file writes
  them, gaps as null.

Not in it yet: a title's capitals from its text properties; the default colours of automatic
series (phase 7's, from the style and colour style parts); multi-level categories in
`ChartCells`' comparison.

**Gate.**  JSON goldens for every chart part of every fixture (29 charts and 3 chartex, in
`charts/json/`), and against Office's renderings: every probe's title as Excel or Word drew it
("Chart Title" for several series; "Sales", "Visits", "Y", "Series 1" for one, the doughnut's
PNG checked; none where the chart has no `c:title`); "Axis Title" on bar-chart.xlsx's axes; the
`#,##0.0` data labels; the colours in the host's theme (`4472C4` in the docx, `156082` in the
workbook and the pasted chart's slide); the filtered series and categories; the gaps; the two
category levels; the combination chart's secondary value axis at the right and its category axis
deleted; the doughnut's varied colours; the three chartex placements (Word, PowerPoint, Excel),
each in a Choice.  Office's caches agree with their cells in every fixture checked, the docx's
embedded workbook included.  Tests: `ChartModelTest` (10), `ChartsTest` (6), `ChartCellsTest` (3);
docx4j-core-tests 1,466 run, 0 failed (11 skipped); docx4j-export-fo-tests 790, 0 failed (9 skipped).

### Phase 3 (2026-10-12), done

Jason: "please commit then continue" (after phase 2's commit).  D9 built as recommended: the box
only by default in HTML, the data as a table on a property.

- **`ChartTable`** (docx4j-core, `org.docx4j.model.chart`): a chart as its title as Office shows
  it and its data, categories down and the series Office shows across (a filtered series and
  category left out, a pie's first series only, every group of a combination chart), numbers
  formatted by their format codes through the repackaged `DataFormatter`, in the US locale so that
  a description reads the same wherever it is made (`4.4000000000000004` is `4.4`, a date
  `2026-01-01` or `1-Jan` as its format says), a gap empty, an error as written; multi-level
  categories a column a level, the outermost first; a chart with no categories (scatter, bubble)
  one x column where the series share their x values, else each series' x beside its y.  A chartex
  chart the same from its caches.
- **Markdown** (`WmlToMarkdown`): a chart's title as a paragraph and its data as a GFM table, after
  the paragraph the chart is in (a chart is inline in Word, a table is a block), or after the table
  where the chart is in a cell; value columns right-aligned.  A chartex chart in a Choice keeps
  Word's picture of it (the Fallback, which docx4j already exports) and its table follows.
- **HTML**: `docx4j.convert.out.html.chartDataTable` (default false; in the reference
  docx4j.properties) follows the chart's box with `<table class="docx4j-chart-data">`, the title its
  caption, on both pathways (`ChartBox.createHtml`).  It sits where the chart is, inside its
  paragraph, where an HTML parser closes the paragraph before it.
- **MCP**: the docx4j side is the JSON form (phase 2) and `ChartTable`; the tool is the MCP
  repository's, handed over as the registry entry `mcp/chart-tools` (no MCP session was running).

**Gate.**  The tables of the probes against their data as excel-chart-probes.ps1 and Word made it
(`ChartTableTest`, 9: categories and series, gaps and #N/A, dates by format code, two category
levels, a scatter's shared x, the filtered series and category left out, a combination chart, the
chartex waterfall, and the HTML table on the property, both pathways); markdown of Word's pie and
waterfall (`MarkdownChartTest`, 2); docx4j-markdown 145 tests green; docx4j-core-tests 1,475 run, 0 failed
(11 skipped); docx4j-export-fo-tests 790, 0 failed (9 skipped).

Phase 4's oracle, ready to run: `docx4j-layout-fidelity/tools/excel-chart-edit-probes.ps1` (on the
share as `office_charts/phase4/`) has Excel make seven edits to Word's default chart data in a
table (a value; a category label and a series name; a category added and a series added, typed
beside the table; a series and a category removed; a blank and an error), saving each workbook
before and after, with a report of the table's range and the series' formulas each side.

### Phase 4 (2026-10-12), in progress

Jason ran `excel-chart-edit-probes.ps1` (Excel 365, `office_charts/phase4/`): seven edits to Word's
default chart data in a table (`Table1`, A1:D5, A1 a space as Word writes it), each workbook saved
before and after with `SaveCopyAs`, so ids and uids are the same on both sides and every difference
is the edit's.  What Excel changed, part by part:

- **A value** (e1): the cell and that point of the cache, nothing else.
- **A category label and a series name** (e2): the cells; each series' category cache and the
  series' name cache; the table column's `name`, which follows its header cell (the `xr3:uid` and
  `id` kept).  Excel rewrote the shared strings on save (unused ones dropped, the rest renumbered),
  a save's doing, not the edit's.
- **A category added**, typed into the row under the table (e3): Excel grew the table itself
  (`ref`, `autoFilter/@ref`), the sheet's `dimension`, a new row with `spans` and the row above's
  `x14ac:dyDescent`; every series' `c:cat` and `c:val` reference one row longer, `ptCount` 5 and a
  fifth point.  The new cells took no style, as the cells above have none.
- **A series added**, typed into the column beside the table (e4): the table grew a column
  (`tableColumns/@count`, a `tableColumn` with `id` one above the largest, a new random `xr3:uid`,
  the header's text as `name`), `spans` 1:5 on every row of the block; a fourth `c:ser` with
  `idx` and `order` 3, its `c:tx` the header cell, its `c:cat` the same as the others', its `c:val`
  the new column, `a:schemeClr accent4` where the third has accent3 (the colour style's cycle,
  `colors1.xml` id 10), and **`c16:uniqueId` `{00000003-C3FB-...}`: every series' id is its `idx`
  as eight hex digits followed by a suffix the chart's series share** (so in every fixture).
- **A series removed**, the table shrunk by its last column and its cells cleared (e5): the
  `c:ser` gone, the table column gone, `dimension` and `spans` narrowed.
- **A category removed**, the table shrunk by its last row and its cells cleared (e6): every
  reference a row shorter, the point gone from every cache, the row element removed.
- **A blank and an error** (e7): the cleared cell's element removed and its point dropped from the
  cache, `ptCount` kept at 4; the `=NA()` cell `t="e"` with `#N/A`, cached as the point `#N/A`;
  Excel added a `calcChain` part for the formula.
- **Number text**: Excel writes some numbers with 17 significant digits (2.2 as
  `2.2000000000000002`, 4.4 as `4.4000000000000004`) and others short (3.1, 4.3, 2.4); the rule
  isn't evident, and both forms read back as the same double, so the editor writes the shortest
  form and the gate compares numbers as numbers.
- **Format codes**: a cache's `formatCode` for a built-in number format is Excel's own spelling of
  it (`d\-mmm` in p2-date-axis for format 16, which POI spells `d-mmm`), so a cache's existing format
  code is kept rather than rebuilt from the cell's style.

**Round 2** (Jason's second run, the same day): fourteen more edits, `r1` to `r14`, each against
Excel's own; the editor was changed where they showed it wrong (r10, r11).

- **A table's middle column deleted** (r1; plotted by row, its middle row, r5): the cells after it
  move, the table loses that column's entry (the others keep their ids), and **Excel keeps the
  chart's series by position**: the second `c:ser`, with its idx, colour and uniqueId, now plots
  what was the third's data, and the last `c:ser` goes.  The editor removes the series it is
  given instead (D12); cells, table and the chart's data by position are Excel's.
- **A middle row deleted** (r2; by row, a middle column, r6): every reference a line shorter, the
  points after it moved up, the table a row (or column) smaller.
- **Plotted by row** (r3, r4): a row typed under the table is a fifth series (idx 4, accent5), a
  column typed beside it a category, every series' references a column longer, the table's new
  column named from its header cell, the category's label.
- **A duplicate name** (r7): "Series 1" typed over "Series 2" is made "Series 12", in the cell and
  the table column, as `Tables.nameColumns` does.
- **Dates** (r8, r8b): with A1 a space and dates in A2:A5, Excel plotted column A as a fourth
  series, so date categories weren't measured; r8b still shows a cell typed under the table
  taking the style of the one above it (the date format, `s="1"`), the editor's rule for every new
  cell.  r8 (text typed into that series) isn't kept as a fixture.
- **A line chart** (r9): the new series' line, marker fill and marker line all accent4.
- **Data labels on the last series** (r10): not the new series'.  The editor copied them; now
  it doesn't.
- **A colour of the last series' own** (red, r11): the new series takes its automatic colour,
  accent4.  The editor copied the red; now a new series takes its formatting from the last series
  still in its automatic colour, recoloured.
- **Excel's chart filter** (r12, r13): a category added grows each union's last area and each
  `c15:fullRef`, the filtered series' as well; a new series takes the same union in its own
  column, and idx 3, after the filtered series' 1 and the shown 2.
- **Two levels** (r14): the new quarter in B6, the region's cell left empty, the multi-level
  cache five points long.

**Built** (D10 and D11 as recommended):

- **Cell writers**, public in `org.xlsx4j.model` and limited to what chart editing needs:
  `CellRef` and `AreaRef` (A1 references, quoted sheet names, unions); `SheetCells` (a cell found
  or made with its row, each in order; a number, a shared string, the shared-strings part made if
  missing, or an error written; a cell cleared, its element removed unless styled, an empty row
  removed; a block's row or column deleted with the cells after it moved; `tidy()` sets the
  `dimension`, the `spans` of each 16-row block, the shared strings' counts, and drops the
  calculation chain where a formula was overwritten); `Tables` (a sheet's tables, one resized, a
  column added (id one above the largest, a fresh `xr3:uid`) or removed, every column named from
  its header cell, made unique as Excel does).  No number-format writer: a new cell takes the
  style of the cell before it, as Excel's table gives it (r8b), which is all the editor needs.
- **`ChartDataEditor`** (`org.docx4j.model.chart`): `setValue`, `setBlank`, `setError`,
  `setCategory` (text, or a number for numbers and dates), `setSeriesName`, `addCategory`,
  `removeCategory`, `addSeries`, `removeSeries`, `setData`, `refreshCaches`; series as the read
  model has them (`getSeries()`).  It takes a chart and the workbook its data is in (`of(Chart)`
  for an xlsx), works on series in columns or in rows, references that are unions (the chart
  filter) and multi-level categories, and refreshes every cache after each edit (a cache's
  `formatCode` kept, a number cached as its cell holds it, an error as its code).  Refused, with a
  message, rather than guessed: a formula cell overwritten unless `setOverwriteFormulas(true)`
  (D10; `fullCalcOnLoad` set whenever the workbook has formulas), formula cells moved, a table
  with a totals row or without a header row resized, a bubble chart's series added, data on
  more than one sheet reshaped, new cells over cells that hold something.

**Gate.**  `ChartDataEditorTest` (27): each of Excel's edits made by the editor on Excel's
before-workbook and compared with Excel's after-workbook, part by part: the chart part (numbers as
numbers), Sheet1's cells (strings by their text, styles, formulas), rows' `spans` and the
`dimension`, the table; and the caches against the cells.  e1 to e7 and r2 to r14 are equal; r1
and r5 equal in their cells, table and chart data, the series kept as D12 says.  A control (a
different edit told apart), the formula rules, a middle series and category removed, `setData`,
and every chart fixture Excel made edited each way with its caches and cells agreeing afterwards
(refused only for the bubble chart's new series and the doughnut, whose next column holds Word's
other data).  `CellWritersTest` (4).  docx4j-core-tests 1,492 run, 0 failed (before round 2's 14).

**Office check, round 1, passed** (`excel-chart-edit-check.ps1`, Jason's run): Excel opened the
seven workbooks the editor wrote without a repair, its *Select Data* ranges are those of Excel's
own edits, and each chart's PNG is pixel for pixel Excel's own edit's (ImageMagick, 0 pixels
differ; each edit had changed Excel's picture by 388 to 35,790 pixels).

**Office check, round 2, passed** (the same script over all 21 workbooks the editor wrote, Jason's
run): every one opened without a repair, with *Select Data* ranges equal to those of Excel's own
edit; 19 charts are pixel for pixel Excel's.  r1 and r5 differ (4,740 and 8,625 pixels) only where
D12 says: the series left after the deleted one keeps its own colour in docx4j's (green, accent3),
where Excel's takes the deleted series' (orange, accent2); bars, values, legend and layout are the
same.

**Done** (2026-10-12): D12 decided the editor's way (Jason: "D12 recommendation accepted."), so
r1 and r5 stand as they are.  docx4j-core-tests 1,506 run, 0 failed (11 skipped).  With phase 4,
CR-035 is done; hand-offs (section 9): the editor to docx4j-core-ts and docx4j-python after the
commit.
