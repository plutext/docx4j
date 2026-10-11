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
