# First extended probe: Word repair evidence

Jason reported unreadable content on 18 October 2026, then supplied
`X:\text-extraction\cr046-phase0\extraction-probe-saved.docx` after repair and save.
No part name or line number was provided by Word. The full build has not been recorded.
These files are diagnostic evidence, not conformance inputs or reviewed goldens. The share's file of
that name was later replaced by Word's save of r2 (below); the v1 save had been copied here first.

- `extraction-probe-v1-rejected.docx`: exact authored bytes used in the first check.
- `extraction-probe-v1-word-repaired.docx`: exact returned repaired save.

Case 42's ActiveX XML and its control relationship are gone. The control object became an
ordinary preview picture. Cases 39–41 retain all three embedded objects (parts renamed).
Case 43 becomes two text paragraphs, case 44 becomes its HTML text, and case 45 becomes its
RTF text. Case 46 becomes literal Flat OPC XML text rather than just its marker. Case 47
becomes both the document marker and its nested HTML marker. Other normalizations include
Fallback regeneration, numbering/relationship changes and hyperlink-field conversion.

The removal of the ActiveX control is a strong lead, not proof that it alone caused repair.
A separate r2 file replaces its hand-built property bag with Word-authored binary Label
storage, adds Flat OPC's Word identification PI, and corrects the DOCX chunk MIME type.
Word must still open that retry without repair.

extraction-probe-v1-rejected.docx SHA-256: `b74988ccea5aef78024b7e2858addcd5248fe3e46636166922f8feebb1714f5e`

extraction-probe-v1-word-repaired.docx SHA-256: `86789a68afcf750843cd7faff35a7d63d0678ddc1df107b3130f0b8e16a5502b`


## Word's saves of r2 (18 October 2026)

- `extraction-probe-r2-word-save.docx`: Word's save of the r2 main DOCX (on the share as
  `extraction-probe-saved.docx`).
- `extraction-probe-external-word-save.docm`: Word's save of the external DOCM (on the share as
  `extraction-probe-external.word365.docm`).

r2 opens without repair, but Word's save drops case 42's control again. The DOCM save drops the
`vbaProject`, the attached template, the mail-merge data source and `w:updateFields`. The
details and hashes are in `word-open-results.md`.
