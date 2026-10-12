# Chart fixtures (CR-035)

Made from scratch in Office by Jason Harrop on 2026-10-11 (Word and Excel, Microsoft 365), as
the charts plan's probes (`../docx4j-portfolio/docs/charts_plan.md`, section 3); no customer
content. Word's and Excel's renderings of them (PDFs, and Excel's chart PNGs) stay on the Office
share, `Office 2016/office_charts` (`goldens-word\`, `goldens-excel\`).

| File | What it is |
|---|---|
| `clustered-column.docx`, `stacked-column.docx`, `bar-chart.docx`, `line-chart.docx`, `pie-chart.docx` | Word's *Insert Chart* default of each type: an inline chart, 6 × 3.5 in, its data in an embedded workbook (`Sheet1!A1:D5`, table `Table1`) |
| `bar-chart-style-2.docx` | the bar chart with *Chart Style 2* applied (style 217) |
| `*.xlsx` of the same names | each docx's embedded workbook, with the same chart inserted in Excel |
| `bar-chart.xlsx` | ... then given axis titles, data labels and a `0.0` value-axis format in Excel |
| `stacked-column.xlsx` | ... then its table grown by a row and a column in Excel, which extended every reference and added a fourth series |
| `waterfall.xlsx` | a waterfall chart (chartex, `xl/charts/chartEx1.xml`), and the line chart of a first attempt |
| `waterfall.docx`, `waterfall.pptx` | *Insert Chart > Waterfall* in Word and in PowerPoint (2026-10-12): the chartex part in `mc:AlternateContent` (`Requires="cx1"`) whose Fallback is a picture of the chart Office drew (`image1.png`), not the text shape Excel writes |

On the share the docx `clustered-column.docx` was `clustered columnsdocx.docx`; the other names
had spaces where these have hyphens.

## Phase 2's probes (made by Excel, 2026-10-12)

`p2-*.xlsx` and `p2-pasted.pptx` were made by Excel itself from Word's default chart data, by
`docx4j-layout-fidelity/tools/excel-chart-probes.ps1` (Jason's run on the Windows VM), for the
read model (CR-035 phase 2): the chart kinds the files above lack (`p2-area`, `-doughnut`,
`-radar`, `-surface`, `-bar-of-pie`, `-3d-column`, `-scatter`, `-bubble`, `-stock`), a date axis
(`-date-axis`), a combination chart with its line on a secondary axis (`-combo-secondary`), a
chartsheet (`-chartsheet`), a blank cell and a #N/A (`-gaps`), two levels of categories
(`-multilevel`), a series and a category hidden with Excel's chart filter (`-filtered`), and a
chart copied into PowerPoint (`-pasted.pptx`).  Excel's renderings (a PDF of each workbook, a PNG
of each chart) are on the share, `office_charts\phase2\goldens-excel`, and PowerPoint's of the
pasted chart in `goldens`.

`json/` holds the JSON goldens of the read model (`ChartModel.toJson()`, `ChartExModel.toJson()`)
for every chart part of every fixture, named after the fixture and the part: docx4j's TypeScript
and Python ports compare their readings against them.  `ChartModelTest` and `ChartsTest` check
them, and write them afresh with `-Ddocx4j.chartGoldens.write=<this json directory>`.

## Phase 4's edits (made by Excel, 2026-10-12)

`edit/` holds Excel's own edits of a chart's data, the oracle of `ChartDataEditor`
(`ChartDataEditorTest`): `docx4j-layout-fidelity/tools/excel-chart-edit-probes.ps1` (Jason's run)
put Word's default chart data in a table with a clustered column chart, saved the workbook
(`<case>-before.xlsx`), made one edit as a user would, and saved it again (`<case>-after.xlsx`,
with `SaveCopyAs`, so ids and uids are the same on both sides).  `<case>.txt` is the script's
report: the table's range and each series' formula before and after, and whether Excel grew the
table itself.  The cases: `e1-value`, `e2-labels` (a category label and a series name),
`e3-add-category` and `e4-add-series` (typed under and beside the table, which Excel grew),
`e5-remove-series` and `e6-remove-category` (the table shrunk), `e7-blank-and-error`.  The second
round, `r1` to `r14` (the same script, the same day): a table's middle column and row deleted
(`r1`, `r2`); a chart plotted by row with a row and a column added and deleted (`r3` to `r6`); a
duplicate header (`r7`); a row typed under dates in column A, which Excel plotted as a series
(`r8b`); a series added to a line chart (`r9`), after a series with data labels (`r10`) and after
one coloured red (`r11`); Excel's chart filter with a category and a series added (`r12`, `r13`);
two levels of categories with a row added (`r14`).

## CR-036's probes (made by Office, 2026-10-12)

`authoring/` holds Office creating charts and editing their data, made by
`docx4j-layout-fidelity/tools/office-chart-authoring-probes.ps1` (Jason's runs on the Windows VM,
Microsoft 365), the oracles and templates of CR-036 (chart authoring).  For each of the twelve
types (`column-clustered`, `-stacked`, `-stacked-100`, `bar-...`, `line`, `line-markers`, `pie`,
`doughnut`, `area`, `scatter`):

- `p3-<type>.docx`: Word's *Insert Chart* (`InlineShapes.AddChart2`) with Word's default data;
  `p3-two-charts.docx` (how a second chart's parts are named) and `p3-data-set.docx` (a chart
  whose data was then set to North/South by Plan/Actual; its chart part is the oracle, its
  workbook lost its table to the script's edits); `p3-no-chart.docx` (Word's document with no
  chart, "No chart.": what a document is once its chart is deleted, CR-036 phase 3).
- `p1-<type>.xlsx`: Excel's chart of the same data (Word's embedded workbook, charted by
  `AddChart2`), with `p1-date-axis.xlsx` (a "Date" header over d-mmm dates).  `p1-*.txt` give each
  chart's type and series formulas.
- `p4-<type>.pptx`: PowerPoint's insert (`Shapes.AddChart2`), and `p4-data-set.pptx`.
- `p2-<setting>.xlsx`: Excel's settings on a clustered column, a file each: `title-text`,
  `no-title`, `legend-bottom`/`-top`/`-left`/`-right`, `no-legend`, `axis-titles`,
  `data-labels`, `number-format` (the value axis's `0.0`).
- `p5w-<edit>-before/after.docx` and `p5p-...pptx`: Word's and PowerPoint's own edits of a
  default chart's data (`value`, `add-category`, `add-series`, `remove-series`, and in Word
  `pie-add-category`), each file saved by Word or PowerPoint itself.  (The script waits a few
  seconds after an edit before closing the chart's data: closed at once, Office saved a chart
  whose cache lacked the last cell typed, or had no caches at all.)

- `p6-*.xlsx` (CR-036 phase 2's readings, each a new Excel workbook with its data in Table1,
  charted by `AddChart2`): `p6-<type>-<setting>.xlsx`, P2's settings on the other eleven types;
  `p6-<type>-dates.xlsx` (a "Date" header over d-mmm dates), `p6-area-text.xlsx`,
  `p6-scatter-text.xlsx`, `p6-<type>-numbers.xlsx` (numbers under a " " header: Excel takes them
  as a series); other shapes of data, `-two-by-two`, `-one-by-one`, `-eight-series`,
  `-eight-points`; and `p6-range-*.xlsx`, charts over plain ranges as Excel reads them
  (`other-sheet`, `wide`, `numbers`, `numbers-header`, `no-header`, `text-corner`), the
  `.txt` beside each giving the series' formulas.  The oracles of `ChartCreatorTest`.  (Office
  writes a typed title's run in the machine's keyboard language: en-AU in this run.)

Their PDFs (Word's and PowerPoint's own) and Excel's chart PNGs are on the share,
`office_charts\cr036`.
