# Word-open results, 18 October 2026

Jason: "extraction-probe-r2.docx opened fine. i declined to update fields."

- Main DOCX r2: open check passed; no repair reported.
- Field updates: declined.
- Word: Word 365 check as requested; full version/build not supplied.
- Authored fixture: `../extraction-probe.docx`, identical to the staged r2 input.
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
Both requested Word-open checks are now recorded as passed. The blank SQL prompt is expected
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
