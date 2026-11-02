# docx4j adapter for docx-platform-tests

docx4j's adapter for [open-agreements/docx-platform-tests](https://github.com/open-agreements/docx-platform-tests),
adapter protocol version 1: one operation on one document, as the suite's runner calls it. Not in docx4j's reactor;
not deployed. `install.sh` builds one runnable jar against docx4j 17.3.5 from Maven Central (`-Ddocx4j.version=...`
measures another release); `run-adapter.sh` runs it. Registration:

```json
{
  "adapterName": "docx4j",
  "adapterCommand": ["bash", "adapters/docx4j/run-adapter.sh"],
  "adapterVersionCommand": ["bash", "adapters/docx4j/run-adapter.sh", "--print-library-version"]
}
```

**What is library API and what is glue.** docx4j is a typed content tree (JAXB) over the package plus operations
on it. The adapter calls an operation where docx4j has one. Otherwise it locates the target (the first paragraph
holding a text, the n-th table in the body) and edits the typed tree in place, which is how docx4j edits a document.
It declines anything needing an algorithm docx4j does not provide.

| Operation | docx4j |
|---|---|
| `acceptAllTrackedChanges`, `rejectAllTrackedChanges` | library: `TrackedChanges.acceptAll` / `rejectAll` (Word's projections, notes, comments, bookmarks and parts that go with removed content included) |
| `composeDocumentWith…` (paragraphs, table, header, numbered list, compatibility mode, hyperlink) | library: `WordprocessingMLPackage.createPackage`, `TblFactory.createTable`, `HeaderPart` and `addTargetPart`, the default numbering definitions, `DocumentSettingsPart.setCompatibilityMode`; a hyperlink is a relationship and a `w:hyperlink` in the typed tree |
| `appendParagraphWithText`, `insertParagraphAfterAnchorText`, `applyParagraphStyleToAnchor`, `applyNumberingToAnchorParagraph`, `setDefaultFooterText` | glue: a paragraph, a `w:pStyle`, a `w:numPr` or a footer part set in the typed tree |
| `appendTableRow`, `deleteTableRowAtIndex`, `setTableCellText` | glue: a row added from the table's grid, a row removed, a cell's content replaced (its properties kept) |
| `replaceFirstTextOccurrence` | glue within one `w:t`; **declined** where the match spans `w:t` elements (docx4j has no find-and-replace across runs) |
| `addCommentOnFirstTextOccurrence`, `formatFirstTextOccurrence` | glue: the anchor is whole runs, or the one run whose `w:t` holds the text, split in three; **declined** where it spans runs |
| `mergeTableCellsInRow` | **declined**: docx4j has no cell-merge operation |
| `removeAllComments` | **declined**: docx4j has no operation removing comments with their anchors and parts |

Exit codes as the protocol says: 0 written, 1 an error (the stack trace on stderr), 2 declined (the reason on
stdout, nothing written), 3 another protocol version. The operation's JSON is read by a small reader in the adapter
(`Json.java`), so the jar needs nothing beyond docx4j.

**Maintenance policy:** best-effort, by the docx4j project (Plutext). The pom names a released docx4j; a new
release is measured by changing `docx4j.version`.
