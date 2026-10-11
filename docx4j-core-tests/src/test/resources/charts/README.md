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

On the share the docx `clustered-column.docx` was `clustered columnsdocx.docx`; the other names
had spaces where these have hyphens.
