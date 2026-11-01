# Metafile test inputs (WMF / EMF / EMF+)

Inputs for `org.docx4j.metafiles.*` — the tests for `org.docx4j.org.apache.poi.hwmf`
and `.hemf` (CR-011 phase 1).

## Provenance

**24 files from Apache POI's `test-data/`** (Apache License 2.0, the same licence as
docx4j; see `legals/NOTICE`). Taken from POI trunk's test-data at the time the HWMF/HEMF
code was repackaged from the POI 5.5.1 release, and two (`bug69924.wmf`,
`clusterfuzz-testcase-minimized-POIFileHandlerFuzzer-5318384433823744.emf`) with the
November 2026 back-ports they test, from trunk `521b26fad9`. Original locations:

| POI path | files |
|---|---|
| `test-data/slideshow/` | `60677.wmf`, `61338.wmf`, `bug69924.wmf`, `64716_image1.wmf`, `64716_image2.wmf`, `64716_image3.wmf`, `clusterfuzz-testcase-minimized-6701721724125184.wmf`, `clusterfuzz-testcase-minimized-POIFileHandlerFuzzer-6060921738035200.wmf`, `clusterfuzz-testcase-minimized-POIFileHandlerFuzzer-6466833057382400.emf`, `crash-7b60e9fe792eaaf1bba8be90c2b62f057cfff142.emf`, `empty-polygon-close.wmf`, `file-45.wmf`, `nested_wmf.emf`, `santa.wmf`, `VHZ2NYFUYUUJNGLABL26ORTQZA76FJEW.emf`, `wrench.emf` |
| `test-data/spreadsheet/` | `61294.emf`, `63327.emf`, `clusterfuzz-testcase-minimized-POIFileHandlerFuzzer-5318384433823744.emf`, `SimpleEMF_mac.emf`, `SimpleEMF_windows.emf` |
| `test-data/document/` | `testException2.doc-2.wmf`, `vector_image.emf` |
| `test-data/xmldsign/` | `jack-sign.emf` |

Several are deliberately malformed (the `clusterfuzz-*` and `crash-*` files, `61294.emf`,
`61338.wmf`, `bug69924.wmf`): they are fuzzer findings kept as regression inputs, and the parse test
expects them to fail cleanly rather than hang or blow up.

**9 files from this repository**, copied from `docx4j-samples-docx4j/sample-docs/metafile-samples/`:
`freehand_emfprinter.emf`, `freehand_picture_saveas.emf`, `freehand_picture_saveas.wmf`,
`freehand_ppt_saveas.emf`, `freehand_ppt_saveas.wmf`, `gradient.emf`, `pptx.emf`,
`star_picture_save_as.emf`, `star_picture_save_as.wmf`.

## Golden images

`golden/*.png` are 96 DPI renderings, downsampled to a 48x48 thumbnail before comparison
(see `MetafileGoldenImageTest` for why the comparison is deliberately fuzzy, and for the
exact tolerance). They are rendered on a magenta canvas rather than a white one, because
some metafiles paint white shapes (`empty-polygon-close.wmf` paints 259 of them) whose
golden would otherwise be an uninformative blank square. To regenerate them after an
intentional rendering change:

```bash
mvn test -pl docx4j-core-tests -Dtest=MetafileGoldenImageTest -Dmetafile.golden.regenerate=true
```

then inspect the resulting PNGs by eye before committing them.
