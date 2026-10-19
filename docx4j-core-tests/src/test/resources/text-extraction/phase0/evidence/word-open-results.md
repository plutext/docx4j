# Word-open results, 18 October 2026

Jason: "extraction-probe-r2.docx opened fine. i declined to update fields."

- Main DOCX r2: open check passed; no repair reported.
- Field updates: declined.
- Word: Word 365 check as requested; full version/build not supplied.
- Authored 52-case fixture: `checked-52/extraction-probe.docx`, identical to the staged r2 input.
- SHA-256: `9854395e9b74e5e2d70d17390dc6673a2751bc12334494c0258f4a04a7e1e025`.
- R2 re-save: supplied after all, on the share as `extraction-probe-saved.docx`, the name the v1
  repaired save had (that save was copied here first). Retained as
  `extraction-probe-r2-word-save.docx`; see "Word's saves" below.
- External DOCM: check completed after declining the blank mail-merge SQL prompt; no repair
  problem reported. Recorded as passed from Jason's "done" response to the requested check.
- External DOCM SHA-256: `87b82a2bb1ec336eaa65cb3c8527fd0edbe0bdd0f967b0df627f8535d0702bd9`.
- External DOCM save: on the share as `extraction-probe-external.word365.docm`; retained as
  `extraction-probe-external-word-save.docm`.

The initial failed input and repaired save are retained separately in this directory.
Both original 52-case Word-open checks are recorded as passed. The subsequent main DOCX r3 check with
cases 53–54 has also been completed (see below). The blank SQL prompt is expected
from case 50's mail-merge data source; the command was declined, not executed.

## Word's saves (read 18 October 2026)

Both saves report only `AppVersion` 16.0000, so the full Word build is still not recorded. Neither
save is a conformance oracle for the extended suite.

**Main DOCX r2**: `extraction-probe-r2-word-save.docx`, SHA-256
`a23f2957e2619f4774cb1843223e19d0399fdf03c560fd23b61c3bae83661391`.
- It is the r2 save, not a second v1 save. Its core properties were modified at
  2026-10-17T19:11:00Z, after the v1 save's 19:04Z, and case 46 is its marker paragraph, not the
  literal Flat OPC XML of the v1 save.
- Case 42's control is gone again. r2 has one `w:control` and `word/activeX/activeX42.xml` and
  `.bin` (ACX42_CAPTION is in the `.bin`). The save has no `w:control`, no ActiveX part and no
  ACX42_CAPTION, only the preview picture. Word opens r2 without repair but does not keep the
  control on save, so r2's control storage is not shown to persist. The cause is not established.
- Cases 39 to 41: all three `w:object`s survive, their parts renamed (`oleObject1.bin`,
  `Microsoft_Excel_Worksheet1.xlsx`, `Microsoft_Word_Document.docx`).
- Cases 43 to 47: every chunk is expanded into ordinary paragraphs, each marker once in
  `document.xml`, including the RTF chunk (45) and case 47's nested chunk.
- No other marker of cases 39 to 49 changed its count (a token count over the XML parts; the
  embedded packages were not opened).

**External DOCM**: `extraction-probe-external-word-save.docm`, SHA-256
`20efb2af4e540d18173d56e930a6205091d0ceddb72ab3c90b93d853221c4faf`.
- Kept: the macro-enabled main content type; the linked-picture (LNK50_MARK) and OLE-link
  (OLE50_LINK_MARK) relationships; the custom property CPR52_CUSTOM and the application property
  APP52_COMPANY.
- Dropped:
  - `word/vbaProject.bin` (case 51), and its content type;
  - `word/_rels/settings.xml.rels`, and with it the attached template (TPL50_MARK) and the
    mail-merge data source (MRG50_MARK);
  - the settings' `w:attachedTemplate`, `w:mailMerge` and `w:updateFields`.
- Declining the mail-merge SQL prompt may account for the mail-merge loss. The cause of none of
  the four losses is established.


## Main DOCX r3: cases 53–54

Jason replied "done" to the request to open r3 normally, decline field updates, and save as
`extraction-probe-r3-saved.docx`. The named save was present on the share and copied here as
`extraction-probe-r3-word-save.docx`. No unreadable-content/repair issue was reported; the
requested open check is recorded as passed.

- Authored input: `../extraction-probe.docx` (54 cases across the two inputs).
- Input SHA-256: `9d80c527890cf338c77f2373dc6ad2c8faf39450cd9c523b78cbf8afbcca90d5`.
- Returned save SHA-256: `717346f43220c596268ae94320b6aef72d8b15ae29771378841a04a8dc4ad8f5`.
- Case 53: `PowerPoint.Show.12` object kept. Its package was renamed to
  `word/embeddings/Microsoft_PowerPoint_Presentation.pptx`; its bytes match the authored PPTX
  exactly (SHA-256 `3f4d2c2cd81a1fc4ce3bb61664c0f70e856a247e9be7d81f2d2bc0a51674d6fd`).
  All eleven presentation markers remain in the embedded slides/table/notes.
- Case 54: `word/glossary/document.xml` and its glossaryDocument relationship were dropped.
  Neither glossary marker appears in the main story. The cause is not established.
- Neither presentation nor glossary payload text leaked into the main story.
- `AppVersion` is 16.0000; the full Word build was not supplied.

The save is diagnostic evidence and does not replace the authored input or its expectations.
Both current input files have completed their Word-open checks.

## extraction-probe-structure.docx: cases 55–57 (19 October 2026)

Jason opened the new fixture in Word 365: no repair prompt. He saved it unchanged beside it as
`extraction-probe-structure-word365.docx`, copied here.

- Fixture SHA-256: `9137bfdaa23bf032c843ef822e8e095f451e019e376aa5b08631bf9e6a5c5550`.
- Word's save SHA-256: `859da9ac300bf8a40a72a9b06360cf604d9df110f1f4ed051030efbc529f7654`.

What Word kept: case 55's TOC field (`TOC \o "1-3" \h \z \u`), its two entries' links and their
PAGEREF fields, and the headings' `_Toc55001` and `_Toc55002` bookmarks; case 56's numbering (six
items on the first list, one on the second); case 57's object (`Excel.Sheet.12`, Embed), whose workbook
is byte-identical, its sheets' states (`hidden`, `veryHidden`) included. Word renamed the workbook's
part (`Microsoft_Excel_Worksheet.xlsx`) and the preview (`image1.png`), and added the theme, settings,
web settings, font table and document properties a save always adds.

docx4j reads the save exactly as it reads the fixture, in plain text under the defaults, in plain text
under `toc`, `embedded`, `hidden` and `list_labels` together, and in Markdown.

## extraction-probe-inspect.docx: cases 58–67 (CR-050 phase 0, 19 October 2026)

Jason installed `support/inspect-fonts/inspect-probe-twin-installed.ttf` (family Inspect Probe Twin,
Liberation Sans's glyphs unaltered), opened the fixture in Word 365, recorded what Word showed against
the checklist (`WORD-CHECK.txt` on the share), took a screenshot of cases 65 to 67, and saved it
unchanged as `extraction-probe-inspect-word365.docx`. Word showed no prompt of any kind on open (Jason:
"confirmed Word showed no prompt of any kind on open").

- Fixture SHA-256: `bedf3c265487cecd63be797d602be5cb5c6a5ed2c34b6458dea27387119e44d0`.
- Word's save SHA-256: `d1ae1f79f64cebfc761888b1a17be4bcc722b3e3c94ab3fb471281486835e57a`. **Not
  committed**: with `w:embedTrueTypeFonts` on, Word embedded the installed fonts the document uses,
  Microsoft's Calibri, Aptos, Aptos Display and Segoe MDL2 Assets among them (1.9 MB), which are not
  ours to redistribute. It stays on the share; what it shows is recorded here.

What Word showed (Print Layout, Simple Markup):

| Case | Item | Word 365 |
|---|---|---|
| 58 | `VAN58_SHOWN` (`w:vanish` false over a hidden character style) | shown |
| 58 | `SZ58_COMPLEX` (`w:cs`, `w:sz` 24, `w:szCs` 3) | tiny: `w:cs` draws the run at `w:szCs` |
| 58 | `COL58_NEAR` (1.497:1), `COL58_FAR` (1.512:1) on white | both legible, "just" |
| 58 | `HEAD58_COLLAPSED` (`w15:collapsed`) | collapsed on open, its body hidden |
| 59 | `GEO59_HIDDEN`: an **inline** box whose `wp:docPr` has `hidden="1"` | **shown**; Word's save keeps `hidden="1"` |
| 59 | `GEO59_OFF_PAGE`; `GEO59_FLAT` | not visible anywhere; a line only |
| 62 | the page colour (`w:background`, `w:displayBackgroundShape`) | shown on screen |
| 62 | `BG62_HIGHLIGHT_OVER_SHADING` (white on a white highlight over black shading) | invisible: the highlight is drawn |
| 63 | after `CHK63_SCRIPTED` | a red cross: Word made the chunk's external image a linked picture (`r:link` to `http://cr050-missing.invalid/probe63.png`, in the save) and could not load it |
| 63 | the ink; the header's `HDR63_USED` and its diagonal `WMK63_WATERMARK` | shown |
| 63 | `CMT63_UNANCHORED` in Review, Comments | not listed; Word's save drops it, and the unreferenced `NOTE63_UNREFERENCED` |
| 65 | `FNT65_WORD` (stored CAT) | **DOG**: Word draws the embedded face |
| 65 | `FNT65_TWIN` (stored CAT, a face named like the installed one) | **CAT**: Word prefers the installed face to the embedded one, as docx4j's `Mapper` does |
| 65 | `FNT65_BLANK`, `FNT65_ZERO`, `FNT65_NAMED`, `FNT65_SHARED`, `FNT65_CAREFUL`, `FNT65_PLAIN` | blank; X over E; AMT; FFF; ZISE; PLAIN |
| 65 | `FNT65_SYMBOL` (U+F041 U+F061 U+F06B in an embedded symbol-encoded face) | other symbols (≙ ↦): Word did not draw the embedded face, and its save drops that face's embedding (the font table keeps the entry, charset 02) |
| 65 | `FNT65_CCMP` (stored BOB, `ccmp` B to P) | **POP**: Word applies `ccmp` with no setting |
| 65 | `FNT65_LIGA` (stored DOT, `liga` D to O, `w14:ligatures` standard); `FNT65_NO_LIGA` (ODD, no setting) | OOT; ODD: `liga` only as `w14:ligatures` says |
| 65 | `FNT65_PUA_PLAIN` (U+E123, unmapped) | a box |
| 67 | `MCE67_CHOICE` and not `MCE67_FALLBACK_WHITE` | as expected |
| 67 | the bound control holding the stale `SDT67_SURFACE` | **SDT67_VALUE**: Word refreshes the binding on open |

What the oracle (`expected-inspection.tsv`, `../INSPECTION.md`) took from this: a stale surface is
not seen (`sdt.stored_surface` visible `no`, not the CR's "yes until Word refreshes"); the page colour
is seen where the settings have `w:displayBackgroundShape` (`visible` `differently`, not
`indeterminate`); a `wp:docPr` hidden on an inline shape is no finding (anchored ones are hidden by
the Selection Pane, which lists floating objects only); `ccmp` is a feature Word always applies.
Word for the web is still to be checked (Jason, later).

Other viewers, measured here the same day:

- LibreOffice 26.2.5.2 (headless, a fresh profile) drew none of the embedded faces (every case 65
  word in a substitute, so CAT reads CAT), drew `SZ58_COMPLEX` at 12pt (ignoring `w:cs` for the
  size), and showed `GEO59_HIDDEN`.
- docx4j's PDF output (docx4j-export-fo 17.3.4-SNAPSHOT, FOP 2.11-docx4j.7) drew every embedded
  face, with no installed namesake on this machine: DOG for both `FNT65_WORD` and `FNT65_TWIN`, the
  blank run blank, X over E, AMT, FFF, ZISE, and the symbol face's A a k (which Word did not draw); it
  did not apply `ccmp` (BOB) and applied `liga` under `w14:ligatures` (OOT; ODD without). It showed
  the stale `SDT67_SURFACE` (no binding refreshed), and read case 67's Fallback (white) where Word
  read the Choice (`McSelection`'s default for drawing prefers the Fallback).
- docx4j's HTML output names the fonts (`font-family: 'Inspect Probe Word'`) and embeds none (no
  `@font-face`), so a browser draws the stored characters (CAT) in a face of its own; the stale
  surface and case 67's Fallback as in the PDF.
