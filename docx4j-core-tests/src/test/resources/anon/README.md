# Test corpus for the anonymiser, org.docx4j.anon (CR-019)

`AnonymizeCorpusTest` runs the anonymiser over docx4j's own documents, never a
customer's: the anonymiser's test corpus carries real documents only in the form
the tool itself produced and `Verify` passed (CR-019 decision 4), and the
generated probes in `AnonymizeProbesTest` carry the coverage of each gap. Most
of the corpus is docx4j-core-tests' existing resources, used where they are;
this directory holds the ones that came from the sample modules, and the
metafile the media probe embeds.

| file | exercises |
|---|---|
| ../loadAndSave.docx | chart with an embedded workbook, comments with people.xml / commentsExtensible / commentsIds / commentsExtended, headers and footers, foot/endnotes, an SVG, a sensitivity label (docMetadata), customXml |
| ../tracked-changes-equations.docx | tracked changes with authors and UTC dates, OMML |
| ../OLE/inserted doc.docx | an OLE object (embedded Word 97-2003 document) with its EMF preview; the media probe borrows its bytes |
| ../LegacyForms.docx | legacy form fields (names, defaults, help text) |
| ../vml/textbox.docx | VML text box |
| chart.docx (from docx4j-samples-docx4j/sample-docs) | chart |
| ../strict/strict-smartart.docx | SmartArt (diagram data, drawing, layout) in a strict (ISO 29500 strict) document |
| embedded-fonts.docx (from docx4j-samples-docx-export-fo, embedded fonts/embedded.docx) | an embedded (obfuscated) font and the fontTable embed elements |
| ../MERGEFIELD.docx | MERGEFIELD instructions |
| probe.emf (docx4j-core-tests metafiles/61294.emf) | the metafile the media probe embeds |

`AnonymizePptxCorpusTest` (CR-019 phase 2) does the same for presentations:

| file | exercises |
|---|---|
| ../loadAndSave.pptx | 2018 ("modern") comments and their authors part (name, initials, email user id), a chart with an embedded workbook, an SVG, a sensitivity label, a thumbnail, a hyperlink, an a14:m equation in an mc:AlternateContent |
| AutoShapes.pptx (from docx4j-samples-pptx4j/sample-docs) | 187 slides of preset shapes with text: the DrawingML walk at volume |
| pptx-chart.pptx (from docx4j-samples-pptx4j/sample-docs) | a chart with its embedded workbook, printer settings |
| table.pptx (from docx4j-samples-pptx4j/sample-docs) | a DrawingML table with a table style id |
| strict.pptx (from docx4j-samples-pptx4j/sample-docs/strict) | an ISO 29500 strict presentation (the preprocessor's PML mapping) |
| probe.mp4, probe.wav (ffmpeg: one second of blue, 0.2 s of silence) | the media the pptx media probe embeds, real so the probe deck itself opens in PowerPoint |

The pptx probes (`AnonymizePptxProbesTest`) are generated: legacy and 2018
comments with authors and presence info, notes, hyperlinks, tags, section
names, custom shows, the modify verifier, table style names, an OLE object with
its preview, a video with its poster and p14:media, a transition sound, an
ActiveX control, an embedded font, a .ppsx main part.

`AnonymizeXlsxCorpusTest` (CR-019 phase 3) does the same for workbooks:

| file | exercises |
|---|---|
| ../loadAndSave.xlsx | a threaded comment with its person (email user id), a legacy comment with its VML shape (an `<xml>` root the binding cannot read: scrubbed as DOM), a table, a chart, an SVG, a sensitivity label, headers and footers |
| ../cr022-checkbox.xlsx | a form control: VML shape and ctrlProps (linked-cell formula) |
| ../cr022-conditional-formatting.xlsx, ../cr022-data-validation.xlsx, ../cr022-sparklines.xlsx | rules, validations and sparklines whose formulas must still point at their cells |
| ../cr022-data-model.xlsx | pivot tables on the data model, connections, customXml: the pivots and model go, the connections stay with `Provider=None` |
| ../cr022-slicers-timelines.xlsx | pivot tables with slicers and timelines and their caches: all removed, with every reference from the workbook, sheets and drawings |
| pivot.xlsm (from docx4j-samples-xlsx4j/sample-docs) | a pivot table with a pivot chart: the chart becomes a plain chart |
| comments.xlsx (from docx4j-samples-xlsx4j/sample-docs) | legacy comments and their VML shapes |
| strict-chart.xlsx, strict-invoice.xlsx, strict-simple.xlsx (from docx4j-samples-xlsx4j/sample-docs/strict) | ISO 29500 strict workbooks (the preprocessor's SML mapping); the invoice has a table, an SVG, printer settings, customXml |

The xlsx probes (`AnonymizeXlsxProbesTest`) are generated: two sheets, a table
with a calculated column, formulas with literals, a defined name, cross-sheet
references, numbers of every shape, hyperlinks, a validation, a conditional
format, header codes, protection and file sharing; legacy and threaded
comments with a VML part; connections, a query table and an external link; an
OLE object with an EMF preview.

The module docx4j-docx-anon held this code and corpus until 17.2.1 (its
history is on the moved files).
