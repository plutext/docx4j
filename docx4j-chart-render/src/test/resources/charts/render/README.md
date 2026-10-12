# Chart render oracle (CR-037)

Office's drawings of the render probes, as numbers, so that the renderer's gates (CR-037 phases
1, 2 and 4) run in the build without the Office share (review A3).  Made 2026-10-12: the probes by
docx4j (`org.docx4j.fidelity.corpus.ChartRenderProbes`, `ChartCreator`, seeded; no customer
content), drawn by Excel, Word and PowerPoint (Microsoft 365, build 16.0.20430) on the Windows VM
(`docx4j-layout-fidelity/tools/office-chart-render-probes.ps1`, Jason's runs), read on Linux and
copied here by `chart_score.py fixtures`.  Sets R1 to R10 (phase 0), R11 and R12 (phase 1: the
value axis's unit against its length; the category labels as the room for them shrinks).  `ChartRenderFixturesTest` checks the set; `ChartRenderGateTest`
is the renderer's gate against it.  (Phase 0 put it in docx4j-core-tests; phase 1 moved it here,
beside the renderer it is the oracle of.)

| Path | What it is |
|---|---|
| `probes.tsv` | the probes: name, set (R1 to R10, CR-037 section 4.1), size in points, hosts, theme (2013 or 2023), the Excel gallery style R10 applied, where the chart sits on the PDF page (docx, pptx), what the probe asks |
| `probes/<name>.xlsx` | the chart as docx4j made it, beside its data, in the probe's theme (the docx's theme part copied in) |
| `probes/<name>-styled.xlsx` | R10: Excel's save of it once its gallery style was applied |
| `probes/<name>.pptx` | R1: PowerPoint's chart of the type, as docx4j made it (PowerPoint's template: no title, text 4/3 larger) |
| `excel-layout/<name>.json` | Excel's layout of the chart (its object model): chart area, plot area and its inside, title, legend and its entries and keys, each axis (bounds, unit, labels' orientation, spacing, format, font), each point's box, a pie's `PieSliceLocation`s, data labels' text and boxes; nulls dropped, numbers to 0.001 |
| `powerpoint-layout/<name>.json` | PowerPoint's, for the pptx |
| `word-pdf/<name>.json` | `chart_trace.py`'s reading of Word's PDF of the probe's docx: each line of text (string, font, size, colour, baseline, centre) and each path (colour, box, each subpath's box and vertices), in points from the chart's top left |
| `powerpoint-pdf/<name>.json` | the same of PowerPoint's PDF of the pptx |

Reading the dumps (measured on all of them, CR-037 section 11):

- Office reports a chart's elements (plot area, titles, legend, axes, data labels) from an origin
  4pt inside the chart's box; points from that origin in Excel, but from the box itself in Word
  and PowerPoint.  `chart_score.py` (`ORIGIN`) applies it.  Excel's `ChartArea` is 5pt narrower
  and shorter than the chart; `chartObject` is the size the probe was drawn at.
- With that, Excel's layout and Word's are the same numbers, so Excel's stands for both (each
  probe's docx holds the same chart as its xlsx, but for what the host adds; the docx are on the
  share).
- `font.size` (from `TextFrame2`) mostly reads 18 for a legend or data labels whatever their
  size; `font.size2` (`Font.Size`) is right.
- The series' own properties (name, fill, values, line, markers) are missing from R1 to R10's
  dumps (a variable shadowed in the script, fixed before R11); each point's are there.
- Word draws category labels turned 45 degrees as glyph outlines, not text (filled paths in the
  labels' colour in `word-pdf/`); turned 90 degrees, as text.
- A blank point's box in Excel is nonsense (far off the chart); a pie's point box spans its
  radii's ends and the centre, not its arc (use `slice`: 1 and 3 the radii's outer ends, 2 the
  arc's middle, 5 the centre, 4 and 6 the radii's middles, 7 to 9 a doughnut's inner points).

The docx, Word's and PowerPoint's PDFs, Excel's PNGs (720 × 420 at 432 × 252pt) and the full
baseline (`baseline.tsv`) are on the share, `Office 2016/office_charts/cr037`.
