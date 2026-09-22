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

The module docx4j-docx-anon held this code and corpus until 17.2.1 (its
history is on the moved files).
