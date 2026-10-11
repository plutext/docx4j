# Theme colour vectors (CR-034)

What Office draws for colour references, measured, for docx4j's resolver (`org.docx4j.model.color`)
and for the ports (docx4j-core-ts, docx4j-python), which read the same files.  See
`docs/developer/change-requests/CR-034-theme-colours-resolved.md`, §5 (the probes) and §11 (the
readings).

| File | What it is |
|---|---|
| `tc-p1-precedence.docx` | WordprocessingML colours whose written value disagrees with their theme reference: runs, shading (one a 25% pattern), top borders, an underline, a run shading |
| `tc-p2-colour-map.docx` | `w:clrSchemeMapping` with the light and dark slots swapped; runs and DrawingML shapes in the mapped colours |
| `tc-p3-transforms.docx`, `tc-p3-transforms.pptx` | the same 403 DrawingML cases in Word and in PowerPoint: five base colours with each transform at up to three values, Office's picker pairs, transforms in both orders, the other colour kinds, shapes coloured by a style reference |
| `tc-p4-spreadsheet.xlsx`, `tc-p4-spreadsheet-palette.xlsx` | SpreadsheetML fills: theme 0 to 9 at nine tints, indexed 0 to 65, and indexed against a custom `x:indexedColors` |
| `tc-p5-map-override.pptx` | a slide overriding the master's colour map, and a slide keeping it |
| `cases.tsv` | every case: probe, label, kind, what is written (the colour XML or the attributes), a note |
| `readings.tsv` | what Office drew for each label, from its PDF (`tools/office_colours.py`): probe, label, page, fill, stroke, text colour, rules (thin lines near the label: borders, underlines).  Colours are RRGGBB, with `/alpha` (0 to 1) where the PDF gives one below 1. |
| `word-tint-shade.tsv` | the 117 distinct (theme colour, tint or shade, byte, value Word wrote) found in four real-document corpora by `tools/theme_colour_scan.py --triples`: no document content |

All seven probes carry the Office 2023 theme (`accent1` 156082 ... `hlink` 467886).  They were
made by `org.docx4j.fidelity.corpus.ThemeColourProbes` (docx4j-layout-fidelity) and rendered by
Word, PowerPoint and Excel 365 on 2026-10-12 (`tools/office-colours.ps1`).  Each case's label is
text Office drew inside or beside the coloured thing; a test finds the case by its label, resolves
it, and compares with `readings.tsv` (`ThemeColourProbesTest`).

Known differences between docx4j's resolver and Office, in `readings.tsv` terms: the saturation
transforms on the grey `bg2` (`T290` to `T298`: Office gives a grey a hue near 40 degrees); some
hue, luminance and picker cases one unit off; `P1-10` (a paragraph shading's tint whose written
value is unusable) one unit off.
