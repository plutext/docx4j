# Test corpus for docx4j-docx-anon (CR-019)

Every document here is docx4j's own (copied from docx4j-core-tests or the
sample modules), never a customer's: the anonymiser's test corpus carries real
documents only in the form the tool itself produced and `Verify` passed
(CR-019 decision 4), and the generated probes in `AnonymizeProbesTest` carry
the coverage of each gap.

| file | from | exercises |
|---|---|---|
| loadAndSave.docx | docx4j-core-tests | chart with an embedded workbook, comments with people.xml / commentsExtensible / commentsIds / commentsExtended, headers and footers, foot/endnotes, an SVG, a sensitivity label (docMetadata), customXml |
| tracked-changes-equations.docx | docx4j-core-tests | tracked changes with authors and UTC dates, OMML |
| ole-inserted-doc.docx | docx4j-core-tests (OLE/inserted doc.docx) | an OLE object (embedded docx) with its EMF preview |
| LegacyForms.docx | docx4j-core-tests | legacy form fields (names, defaults, help text) |
| vml-textbox.docx | docx4j-core-tests (vml/textbox.docx) | VML text box |
| chart.docx | docx4j-samples-docx4j/sample-docs | chart |
| embedded-fonts.docx | docx4j-samples-docx-export-fo (embedded fonts/embedded.docx) | an embedded (obfuscated) font and the fontTable embed elements |
| MERGEFIELD.docx | docx4j-core-tests | MERGEFIELD instructions |
| probe.emf | docx4j-core-tests (metafiles/61294.emf) | a metafile the media probe embeds |
