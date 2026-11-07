# The package-wiring probe family

A package whose wiring has two readings: entries of one name, names that differ only in case, two main parts. Built by
`build_package_wiring.py` (`python3 build_package_wiring.py <folder>`); a rebuild is byte-identical (stored zips, fixed dates).
CR-075 D9. The two-rels files are those of `two-rels/` on the share, 4 November 2026, unchanged.

| File | Holds |
|---|---|
| `control-one-main.docx` | One main part, nothing wrong |
| `two-rels-entries-document-first.docx`, `-other-first.docx` | Two `_rels/.rels` entries, each naming a different main part |
| `two-main-rels-document-first.docx`, `-other-first.docx` | One `_rels/.rels` with two `officeDocument` relationships |
| `two-main-rels-zip-other-first.docx`, `-types-other-first.docx`, `-names-swapped.docx` | One factor changed at a time: the zip entries, the content-type Overrides, the names |
| `one-main-rel-other-override-last.docx`, `one-main-rel-document-override-last.docx` | One relationship, and a second main part no relationship names, its Override last |
| `dup-entry-document.docx` | Two `word/document.xml`, FIRST_ENTRY then SECOND_ENTRY |
| `dup-entry-same-bytes.docx` | Two `word/document.xml`, the same bytes |
| `dup-entry-content-types.docx` | Two `[Content_Types].xml`, the second naming another main part |
| `dup-entry-part-rels.docx` | Two `word/_rels/document.xml.rels`, naming `header1.xml` then `header2.xml` |
| `case-variant-exact-first.docx`, `case-variant-exact-last.docx` | `word/document.xml` and `Word/Document.xml`, each order |
| `dup-central-reversed.docx` | Two `word/document.xml`, the central directory the other way from the local headers |
| `dup-local-only.docx` | Two `word/document.xml` in the local headers, the central directory listing the second alone. Not in the Word round |
| `dup-directory-entry.docx` | The directory entry `word/` twice, which isn't a part. Not in the Word round |

## What Word does

Word 16.0.20326.20232, 7 November 2026, through the bridge, each file twice, the two runs agreeing (`pw-*` in `bridge/portfolio`).
`<name>-word365.docx` is Word's save. The first 17 files were run; the last two weren't.

- `control-one-main` and both `one-main-rel-*` open silently. A part no relationship names is ignored, its Override last or not.
- Every other file offers repair ("Word found unreadable content"), `dup-entry-same-bytes` included: identical copies are still a fault.
- Entries of one name read the first in the central directory. `dup-central-reversed` reads SECOND_LOCAL, so the central directory decides,
  not the local headers. A case variant reads the first too, whichever name the relationship spells.
- **Two `officeDocument` relationships alternate.** A bridge job opens the file once per output in one Word process, repairing each time.
  A fresh Word's first open reads the last relationship's target, the second open in the session the first's, the third the last's, whatever
  the format and whatever the Overrides say: the `open-order` round (`oo-*`), 84 outputs, every one fitting, and `pw-*`'s five files. The hand runs
  of 4 November fit the same alternation; "the Override last" was a coincidence of the order the files were opened in. The `-word365.docx`
  saves of `two-main-rels-*` are each a first open.
