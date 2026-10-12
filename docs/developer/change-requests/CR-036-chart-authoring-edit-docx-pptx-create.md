# CR-036: chart authoring - a chart's data edited in docx and pptx, and charts created in xlsx, docx and pptx (charts plan phases 6 and 5)

Status: Proposed 2026-10-12.  Drafted on Jason's request ("Yes to all those decisions; please draft
CR-036 on this scope"), after the scope proposal that followed CR-035's close.  Taken with that
reply (§8): the plan's D7 (authoring before rendering), D3 (the chart types of §4.3), D6 (Office's
own parts as templates, byte for byte), the builder's shape (an object model in the shape of POI's
XDDF, with a one-call `fromData`) and created data in a table.  Reviewed the same day by the
docx4j-13 session (Claude Fable 5.1, at Jason's request): approved with eight amendments, A1 to A8,
and some smaller points, all accepted by Jason (A6 first, relayed by docx4j-13; then "yes, apply all
eight amendments and the smaller points") and applied (§8 lists them): the embedded workbook kept
live in its part (A1, deciding D2), the definition built of groups (A2), settings writers for
existing charts (A3), a chart deleted (A4), a named comparator and a no-edit control in every gate
(A5), PowerPoint's own PDFs (A6), more ids (A7), D1 left for P4 (A8); 15-19 days.  D1 then taken the
same day (Jason, relayed by docx4j-13): one chart template per type, Excel's, and a host step,
within D6's "byte for byte".  D3 to D6 are open.  Owner: Jason Harrop.  Drafted with Claude Opus
5.5.

The charts plan (`../docx4j-portfolio/docs/charts_plan.md`) numbers authoring as phase 5 (create in
xlsx) and phase 6 (edit and create in docx and pptx).  This CR takes them as its own phases 1 to 3,
in the order that builds least twice: phase 6's editing first, since CR-035's editor already does
the work and only the embedded workbook's coming and going is missing; then creation in xlsx; then
creation in docx and pptx, which needs both.  Phase 0 is the probes, as in CR-035.

Scope, all open source under the plan's D1 (d) (the classic `c:` charts; chartex creation is the
commercial phase 9):
- **Phase 0, probes:** Office creating each chart type in each host, Office's settings (title,
  legend, axis titles, labels, number format), and Word's and PowerPoint's own data edits, in one
  run on the Windows VM; the oracles of phases 1 to 3 and the templates of phases 2 and 3.
- **Phase 1, edit in docx and pptx:** `ChartDataEditor` on a chart in a docx or pptx, its embedded
  workbook opened, edited and written back.
- **Phase 2, create in xlsx:** a chart made from data, or from cells already in a sheet, as Excel's
  *Insert Chart* makes it.
- **Phase 3, create in docx and pptx:** the same chart with an embedded workbook made as Word and
  PowerPoint make it, placed inline in a docx or in a slide's frame.

The asked-for use is in issue #664 (March 2026, closed unanswered): "a chart that remains editable
in Microsoft Word, similar to charts inserted directly via Insert → Chart", with *Edit Data*
working, asking whether to build one or to fill a template.  Phase 1 answers the template way,
phase 3 the other.

## 1. What docx4j does today (read from the code, 2026-10-12)

- **Editing** (CR-035 phase 4): `ChartDataEditor` takes a chart and the workbook its data is in,
  so nothing in it is xlsx's own; `of(Chart)` accepts only a chart in an xlsx.
  `ChartCells.of(Chart)` already opens a docx's or pptx's embedded workbook (`c:externalData` →
  the relationship → `EmbeddedPackagePart.extractPackage()`), so the read side is done; nothing
  writes a workbook back (`BinaryPart.setBinaryData` would).  `Charts.in` finds the charts of all
  three hosts.
- **The pptx sample** `docx4j-samples-pptx4j/.../EditEmbeddedCharts` is the state of the art for a
  user: part names hard-coded, the caches and the cells set by hand separately, no table, no
  references, no series added.
- **Creating**: nothing.  No code places a drawing in a worksheet (`CTTwoCellAnchor` is used only to
  read and to anonymise); `Tables` resizes a table and can't make one; there is no chart template.
  For pictures, `BinaryPartAbstractImage.createImageInline` builds a `wp:inline` from ids the
  caller gives, and `PartName.generateUniqueName` names a new part: the patterns phase 3 follows.
- **Reading what a created chart needs**: the read model (`ChartModel`), the colour style's cycle
  and Office's series numbering (`ChartDataEditor.addSeries`), the cell writers (`SheetCells`,
  `CellRef`, `AreaRef`, `Tables`).

## 2. What Office writes (measured on the fixtures, 2026-10-12)

1. **Nearly one chart part per type, whatever the host.**  Word's chart and Excel's chart of the
   same type on the same data (Jason's inserts; the `clustered-column` and `line-chart` pairs; the
   stacked column and bar were edited in Excel afterwards, so don't compare) are the same XML but
   for three things: the random `c16:uniqueId` suffix, the random axis ids, and the host's element
   at the end (Word: `c:externalData r:id` with `c:autoUpdate val="0"`; Excel: `c:printSettings`).
   **The pie differs a fourth way**: Word gives each of its varied-colour points (`c:dPt`) a
   `c16:uniqueId` of its own (numbered 1, 3, 5 and 7, a suffix other than the series'), Excel none.
   Their style and colour parts are byte for byte the same (`style1.xml` per type, `colors1.xml` id
   10, `meth="cycle"`, in every fixture).
2. **A script gets Office's own chart.**  Excel's `Shapes.AddChart2(-1, xlColumnClustered, ...)`
   from a script (the phase 4 probes) wrote exactly the chart, style and colour parts Jason's insert
   through the user interface did, the random ids aside.  So templates can be captured by script,
   for every type.
3. **The default style per type**: `cs:chartStyle` id 201 (clustered column), 297 (stacked column),
   216 (clustered bar), 227 (line), 251 (pie), 217 (*Chart Style 2* on a bar); `c14:style` 102 and
   `c:style` 2 throughout.
4. **Word's insert** (`clustered-column.docx`): `word/charts/chart1.xml` with `style1.xml` and
   `colors1.xml` beside it; the workbook `word/embeddings/Microsoft_Excel_Worksheet.xlsx` (a
   `package` relationship from the chart; `Default Extension="xlsx"` in the content types); in the
   body a `wp:inline` of 5,486,400 × 3,200,400 EMU (6 × 3.5 in), `docPr` a random id and the name
   "Chart 1", `wp14:anchorId`/`editId`, the `c:chart` relationship.  The workbook is the minimum:
   one sheet `Sheet1`, `Table1` over A1:D5, A1 a space, no chart in it.
5. **Excel's insert** (the phase 4 probes): `xl/drawings/drawing1.xml`, a `twoCellAnchor` whose
   `graphicFrame` (`macro=""`, `cNvPr id="2" name="Chart 1"` with an `a16:creationId`, an empty
   `xfrm`) holds the `c:chart` relationship; the sheet's `drawing` relationship; the chart's style
   and colour parts as above.
6. **PowerPoint**: the only fixture (`p2-pasted.pptx`) is a chart *pasted* from Excel, which is
   **linked**: its `c:externalData` names an `oleObject` relationship with `TargetMode="External"`
   ("Book16").  A chart inserted in PowerPoint, with its workbook embedded, is unmeasured: phase 0.

## 3. Corpus facts

None: no document of the four corpora has a chart part (CR-035 §3).  Every gate is on probes.

## 4. Design

### 4.1 Phase 0: the probes

One script, `docx4j-layout-fidelity/tools/office-chart-authoring-probes.ps1`, for one run on the
Windows VM (Excel, Word and PowerPoint 365), saving each file into the share's
`office_charts/cr036/`:

- **P1, Excel's insert of each type** (`AddChart2(-1, type)` on Word's default data in `Table1`):
  the twelve types of §4.3; and a date axis, the data's header "Date" over dates formatted `d-mmm`
  (closing CR-035's r8, which put a space there and so got no categories).  The templates of
  phase 2, and its oracle.
- **P2, Excel's settings** on a clustered column, a file each: a title's text; no title; the legend
  at the bottom, top, left, right and none; axis titles; data labels; the values' number format
  `0.0`.  What phase 2 writes for each setting.
- **P3, Word's insert of each type** (`InlineShapes.AddChart2`), its data then set through
  `Chart.ChartData.Workbook`; two charts in one document (how the second's parts are named).  Phase
  3's oracle; a check of finding 1 on all twelve.
- **P4, PowerPoint's insert of each type** (`Shapes.AddChart2`), the same: the embedded workbook's
  name and the chart part against Excel's (finding 6).
- **P5, Word's and PowerPoint's own edits** of a default chart, before and after, through
  `ChartData.Activate()` and its workbook: a value, a category added, a series added, a series
  removed; and a category added to Word's pie, whose points carry their own `c16:uniqueId`
  (finding 1: does the new point get a `c:dPt` and an id?).  Phase 1's oracle.  Each before and
  after file saved by Word or PowerPoint itself, not re-saved by the script, so that the oracle is
  Office's own save, as CR-035's edit pairs were (review).  (`AddChart2` and `ChartData` are
  documented in Word's and PowerPoint's object models; the run shows whether they behave from a
  script as Excel's did.)

Each file is made from scratch, no customer content; the useful ones join
`docx4j-core-tests/src/test/resources/charts/` as fixtures, as phase 4's did.

### 4.2 Phase 1: a chart's data edited in docx and pptx

- `ChartDataEditor.of(Chart)` accepts a chart in a docx or pptx: it opens the embedded workbook the
  chart's `c:externalData` names, and the editor works on it exactly as on an xlsx.
- **The embedded workbook, live** (review A1, deciding D2): `EmbeddedPackagePart` gains
  `getPackage()` (extracted once, then kept) and `setPackage()`, and writes its live package when
  the host package is saved, as every other part writes its content.  So the readers
  (`ChartCells.of`, `Charts.in`, `ChartModel.read`, each of which loads the part's bytes afresh
  today) and an editor share one package, and a save after an editor left open loses nothing.
  The editor is still `AutoCloseable`: `close()` and `flush()` write the workbook into the part's
  bytes explicitly; for an xlsx they do nothing.  So `try (ChartDataEditor e =
  ChartDataEditor.of(chart)) { ... }`.
- **What docx4j's save does to Word's workbook** (A1): re-zipped and re-marshalled, Word's minimal
  workbook is no longer byte for byte Word's; phase 1 measures the difference, and the no-edit
  control (§5) gates that Word's *Edit Data* accepts it.
- Refused, with a message: a **linked** chart (`c:externalData` naming an external target, as the
  pasted fixture's does; issue #126's case), and a chart with no workbook.
- `c:externalData/c:autoUpdate` is left as it is (Word writes 0).
- The pptx sample `EditEmbeddedCharts` rewritten on the editor, and a docx sample beside it.

### 4.3 Phase 2: charts created in xlsx

**The types** (the plan's D3, taken): column and bar, each clustered, stacked and 100% stacked;
line and line with markers; pie; doughnut; area; scatter (markers only).  Twelve, as `ChartType`
(Excel's names: `COLUMN_CLUSTERED` for `xlColumnClustered`, ...).  Combination charts, secondary
axes and the other variants (3-D, stacked lines and areas, smooth scatter) come later.

**The object model** (the builder's shape, taken), in XDDF's terms: a chart with its title and
legend, and one or more **groups**, each a chart type, its series (from data sources) and its pair
of axes (each with a title and a number format).  The fluent `.categories()` and `.series()` are
shorthand for the first group, so that combination charts and secondary axes later add a group
(`.group(ChartType.LINE, g -> g.series(...).secondaryAxis())`) without breaking anything, and the
ports mirror the group form from the start (review A2).  This CR creates charts of one group.  The
definition is assembled first and then made from Office's parts:

```java
ChartDefinition d = ChartDefinition.of(ChartType.COLUMN_CLUSTERED)
    .categories("North", "South")              // or numbers, or dates (serials, with a format)
    .series("2025", 1.0, 2.0)
    .series("2026", 3.0, 4.0)
    .title("Sales")                            // or noTitle(); left out: Office's derived title
    .legend(LegendPosition.BOTTOM)             // or noLegend(); left out: the type's default
    .valueAxis(a -> a.title("Units").numberFormat("0.0"))
    .dataLabels(true);
// the one-call form
ChartDefinition d = ChartDefinition.fromData(ChartType.COLUMN_CLUSTERED, categories, seriesNames, values);
```

- For a scatter chart the categories are the x values: `xValues(...)` is the same as
  `categories(...)`, as the editor's `addCategory` already documents (A2).
- Dates as categories are serial numbers with a date format: the format written into the cells
  (`SheetCells`), the chart from the date-axis template (P1) (A2).

**Making it** (`ChartCreator`, `org.docx4j.model.chart`):
- `ChartCreator.inSheet(worksheet, definition, CellRef dataAt, AreaRef placeAt)`: the data written
  at `dataAt` as a new table (D3), refused over cells that hold something; the chart placed over
  `placeAt` in the sheet's drawing (made if missing), a `twoCellAnchor` as Excel writes it.
- `ChartCreator.fromCells(worksheet, AreaRef data, ChartType type, AreaRef placeAt)`: a chart over
  data already in cells, as Excel's *Insert Chart* over a selection: the first row and column read
  as Excel reads them (series names across, categories down, a blank corner), no table made.  The
  data may be on another sheet than the chart's (the `AreaRef` carries its sheet).  `inSheet`
  refuses an empty definition (no series, or no categories) (review).
- The chart, style and colour parts are copied from the type's template (D6, taken: Office's own,
  byte for byte, captured by P1); the axis ids and the `c16:uniqueId` suffix made fresh, random as
  Office's (D6 below); the series made by `ChartDataEditor.setData` from the template's default
  data, so a created chart's series, references, caches, table and colours follow the rules phase 4
  measured; then the definition's settings written as P2 shows Excel writes them.  Since every
  template carries Word's three series and four categories, `setData`'s shrinking path is on every
  created chart's way: phase 2 tests one series, one category, and one of each, explicitly
  (review).
- **Settings on an existing chart** (review A3): the writers of the definition's settings (title,
  legend, axis titles, number format, data labels) public as `ChartSettings`, on any chart, made
  or loaded, so that a filled template's title is set as its data is (issue #664's users want
  both).

### 4.4 Phase 3: charts created in docx and pptx

- `ChartCreator.inDocument(wordPackage, definition, long cx, long cy)` returns a `w:r` holding the
  `w:drawing` (`wp:inline`, Word's default 6 × 3.5 in where no size is given), for the caller to put
  in a paragraph (D4), with a convenience that appends a paragraph and returns the new chart's
  `ChartRef`, so that an editor can be opened on it at once; `ChartCreator.inSlide(slidePart,
  definition, x, y, cx, cy)` returns the `p:graphicFrame` it adds to the slide's tree.
- The chart, style and colour parts from the type's template, Excel's (D1, taken), with the host
  step: the trailing `c:printSettings` dropped and `c:externalData` appended (the new workbook's
  `r:id`, `c:autoUpdate val="0"`); for a pie in a docx, the id source adds Word's per-point
  `c16:uniqueId`s.  The workbook made from Word's (or PowerPoint's) own embedded workbook, kept as a
  template (D6, taken), its data written by `setData`; named, related and typed as P3 and P4 show.
- Ids (D6): `docPr` (a random id unused in the document, as Word's), `wp14:anchorId` and
  `wp14:editId`, `cNvPr` in a slide (the next free) and its `a16:creationId`, all from the seedable
  source (A7); the chart parts and the workbook named after any the document already has (P3's
  two charts), checked in phase 3 (A7).
- **A chart deleted** (review A4): `ChartCreator.delete(ChartRef)` removes its drawing (the
  `w:drawing`'s run, the slide's frame, the sheet's anchor), its chart, style and colour parts, its
  embedded workbook, their relationships, and the content types' `xlsx` default where no other
  embedded workbook is left: for a template's placeholder chart, and for any host.

**Left for later** (not in this CR): a chart copied with its workbook into another part or
package; linked charts; floating (anchored) charts in a docx; charts created in headers, footers,
layouts and masters (editing works in all of them); combination charts and secondary axes (the
definition's groups are ready for them, A2); an existing chart's other properties (its type, its
style, its axes' scaling; its settings are A3's); chartex (commercial, the plan's D1 (d)).

## 5. Gates and oracles

**The comparator** (review A5): CR-035's gate (`ChartDataEditorTest`'s comparison) made a shared
test helper.  The chart part marshalled by docx4j on both sides, then normalised: the
`c16:uniqueId` suffixes, the axis ids (`c:axId`, `c:crossAx`), the drawing ids (`docPr`, `cNvPr`,
`a16:creationId`, `wp14:anchorId`, `wp14:editId`); numbers compared as numbers.  A workbook, the
host's or the embedded one, compared as CR-035 compared Sheet1: cells by their text, styles,
formulas, rows' spans, the dimension, the table.  What docx4j's own save changes in Office's XML
(booleans written `true`/`false` where Office writes `1`/`0`, which marshalling both sides hid
in CR-035's gate) is what the control measures.

**The control, in every phase** (A5): Office's own file loaded and saved by docx4j with no edit
(in phase 1, the editor opened and closed with no edit) passes the same comparison against the
original and opens in Office without repair, with *Edit Data* working, so that a gate tells a
defect of the edit or the creation from an artefact of the save.

- **Phase 0:** the probes read: each file opens, its chart is what was asked (the type, the data),
  finding 1 holds or the differences are listed for D1.
- **Phase 1:** P5's pairs: Word's and PowerPoint's own edit against the editor's same edit on the
  before-file, part by part as in CR-035's gate (the chart part, and the embedded workbook's cells,
  table, spans and dimension).  Word and PowerPoint open each edited file without repair and *Edit
  Data* opens the new data; Word's PDF of each against Word's PDF of its own edit, by raster
  difference.  The linked fixture refused.
- **Phase 2:** every P1 type and P2 setting: Excel's chart against `ChartCreator`'s of the same data
  and settings, part by part, the random ids aside.  `excel-chart-edit-check.ps1` on the
  outputs: no repair, *Select Data*'s ranges, the PNG against Excel's own, pixel for pixel.  And
  Office's tools on a created chart work: a style from the gallery, *Change Chart Type*, *Edit Data*
  (a scripted check where Excel's object model allows, Jason's eye where not).
- **Phase 3:** P3 and P4: Word's and PowerPoint's chart of the same type and data against
  `ChartCreator`'s, part by part, the embedded workbook included; each opens without repair and
  *Edit Data* opens it; Word's PDF of each docx against Word's PDF of its own, and PowerPoint's own
  PDF export (`SaveAs` as PDF, scripted) of each pptx against PowerPoint's PDF of its own, by raster
  difference (review A6).

## 6. Phases

| Phase | What | Depends on | Effort |
|---|---|---|---|
| 0 | §4.1: the probe script; Jason's run; the probes read and committed as fixtures. | CR-035 | 1 day + Office |
| 1 | §4.2: the editor on docx and pptx, write-back, refusals, the samples; P5's gate. | 0 | 3-4 days |
| 2 | §4.3: `ChartType`, `ChartDefinition` (groups), `ChartCreator` for xlsx, `ChartSettings`, the templates, `Tables` creating a table, the sheet's drawing; P1/P2's gate. | 0 | 7.5-9.5 days + Excel |
| 3 | §4.4: docx and pptx creation, the embedded workbook template, a chart deleted; P3/P4's gate. | 1, 2 | 3.5-4.5 days + Word, PowerPoint |

15-19 working days (the plan's phases 5 and 6, 14-18, and a day for the review's A3 and A4), plus
Office runs.  Each phase ends with
CHANGELOG, this CR's record, the registry entry, and the hand-offs it triggers.

## 7. Risks

- **Word's and PowerPoint's object models from a script.**  `ChartData.Activate()` opens Excel
  inside Word; it may want a visible session, or fail headless.  The probe script runs `-Visible`
  if so; failing that, Jason makes P5's edits by hand, saving before and after.
- **PowerPoint's chart part may differ from Excel's** (text sizes, the `c:txPr` defaults) where
  Word's doesn't: then per-host templates (D1), measured in P4.
- **A shared part's save path** (A1): `EmbeddedPackagePart` writing a live package changes how a
  part reaches the zip, in the io3 save path CLAUDE.md counts as widely shared; the round trip of
  every fixture with an embedded package (docx, pptx, the OLE and package embeddings of the
  corpora) is its gate, and the no-edit control (§5) its check against Word.
- **The templates are Office's, version by version.**  Excel 365's parts as of the run; an older
  Office opens them (they carry `mc:Ignorable` extensions it skips), but their look is 365's.
- **Fresh random ids** can't collide in practice, but a document's `docPr` ids must stay unique;
  phase 3 checks the document's.
- **The API is a promise** (the ports mirror it, §9): `ChartDefinition`'s shape is the one to get
  right; phase 2's review is the moment to change it.

## 8. Decisions

Taken (Jason, 2026-10-12, "Yes to all those decisions", on the scope proposal):
- the plan's **D7**: authoring (phases 5 and 6) before rendering (phases 7 and 8);
- the plan's **D3**: the twelve types of §4.3 first; combinations and secondary axes later;
- the plan's **D6**: Office's style, colour (and here chart and embedded workbook) parts shipped as
  templates, byte for byte;
- **the builder**: an object model in XDDF's shape plus a one-call `fromData`;
- **created data in a table**, as Word always puts it.

Reviewed 2026-10-12 by the docx4j-13 session (Claude Fable 5.1, at Jason's request): approved,
with eight amendments and some smaller points.  **A6 accepted** first (Jason, relayed by
docx4j-13); then **all accepted** (Jason: "yes, apply all eight amendments and the smaller points
to the CR and commit"), and applied:
- **A1** the embedded workbook kept live in its part, written on the host's save (§4.2; decides D2);
  what docx4j's save does to Word's workbook measured.
- **A2** the definition built of groups, `.series()` the first group's; scatter's x values and
  dates named (§4.3).
- **A3** the settings writers public for existing charts, `ChartSettings` (§4.3).
- **A4** a chart deleted (§4.4).
- **A5** the comparator named, with its normalisations, and a no-edit control in every phase (§5).
- **A6** PowerPoint's own PDF export in phase 3's gate (§5).
- **A7** `wp14:anchorId`/`editId` among the ids; part numbering across a document checked (§4.4).
- **A8** D1 decided after P4; D1 was then taken the same day, keeping A8's escape hatch (below).
- Smaller points: `setData`'s shrinking path tested explicitly (§4.3); `fromCells` takes data on
  any sheet, `inSheet` refuses an empty definition (§4.3); P5 saved by Office itself (§4.1); the
  docx convenience returns the `ChartRef` (§4.4).

This CR's own (owner: Jason): D1 is taken and D2 decided (A1); D3 to D6 are open, with
recommendations:
- **D1. Taken (Jason, 2026-10-12, relayed by docx4j-13): one chart template per type, Excel's,
  and a host step**, which sits within D6's "byte for byte": for a docx or pptx the trailing
  `c:printSettings` dropped and `c:externalData` appended (the new workbook's `r:id`,
  `c:autoUpdate val="0"`), nothing for an xlsx; the style and colour parts shared, as measured.
  Excel's, Word's and PowerPoint's chart parts per type are still captured (P1, P3, P4), as the
  oracles.  A8's escape hatch kept: if P4 shows PowerPoint's part differing beyond the trailing
  element (its `c:txPr` defaults), per-host templates for pptx.  For a pie in a docx, the id
  source adds Word's per-point `c16:uniqueId`s (finding 1).
- **D2. Writing an embedded workbook back: decided by A1** (Jason, 2026-10-12): the part keeps a
  live package, written when the host is saved; `close()` and `flush()` write it explicitly (§4.2).
  Neither after every edit (re-zipping per `setValue` is slow in a loop) nor on `close()` alone (a
  save without it lost the edit).
- **D3. Where created data goes in an xlsx**: at a cell the caller names, as a new table, refused
  over cells that hold something; and `fromCells` for data already there, with no table made, as
  Excel's *Insert Chart* over a selection, its data on any sheet; an empty definition refused.
  Recommendation: both (the review agrees).
- **D4. Placing a chart in a docx**: return the run holding an inline drawing for the caller to
  place (as `createImageInline` returns the inline), with a convenience that appends a paragraph.
  Inline only: Word's *Insert Chart* is inline; floating charts later.  The convenience returns
  the new chart's `ChartRef`.  Recommendation: yes (the review agrees).
- **D5. Names**: `ChartType`, `ChartDefinition`, `ChartCreator` in `org.docx4j.model.chart` (not
  `ChartData`, which is the read model's).  Recommendation: as named.
- **D6. Ids**: axis ids, the `c16:uniqueId` suffix, `docPr`, `wp14:anchorId` and `wp14:editId`,
  `cNvPr`'s `a16:creationId`, random as Office makes them, with a seedable source for reproducible
  output.  Recommendation: yes.

## 9. Hand-offs

- **docx4j-python**: its CR-004 phase F and CR-005 phase G (chart creation) can mirror
  `ChartDefinition`, its group form included (python-pptx's `CategoryChartData` and `add_chart` are
  the same idea), and ship the same template parts; phase 2's fixtures are its oracle.
- **docx4j-core-ts**: the same, when it creates charts.
- **docx4j-mcp**: a chart-creating tool beside `mcp/chart-tools`' describing one.
- **Enterprise**: chartex creation (the plan's phase 9, commercial) plugs its own templates into
  `ChartCreator`.

## 10. Effort (rough)

15-19 working days, plus Office runs: one for phase 0's probes (all three applications), and the
open checks of phases 1 to 3.

## 11. Implementation record

(none yet)
