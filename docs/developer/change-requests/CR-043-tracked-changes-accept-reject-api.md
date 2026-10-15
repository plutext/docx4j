# CR-043: Accepting and rejecting tracked changes - a docx4j-core API

Status: PROPOSED 2026-10-15 (drafted at Jason's request, relayed by the portfolio session for its
"Aspose to docx4j" comparison; implementation needs Jason's word).  Four phases (§5); decisions in
§7.

## 0. Summary

Aspose.Words has `Document.acceptAllRevisions()` and per-revision `accept()`/`reject()`.
docx4j's answer has been a sample (`TrackChangesAccept`, with `AcceptChanges.xslt`, since 3.3.6)
that only accepts, only reads the main document part, and gets two cases wrong. docx4j-core has
had a better accepting pass since 17.2.0 (`AcceptTrackedChanges`, CR-012). But it is a
conversion preprocessor: it leaves formatting revisions alone and touches only the main document
part, which is right for drawing a PDF and wrong for a document that is saved again. docx4j-python
0.1.0 already ships both halves over Office JS's `Word.TrackedChange` vocabulary (python CR-003
§3.8, Phase F).

This CR gives docx4j-core a public API to **list, accept and reject** tracked changes, in every
story of a document, with **Word 365's own Accept All and Reject All as the oracle**. It is mostly
a port of python's model onto the Java tree, generalising `AcceptTrackedChanges`. On top of that
come the cases neither has yet: section, table, row, cell and numbering property changes, cell
insertions and deletions, move ranges, the other parts, and both branches of
`mc:AlternateContent`.

## 1. What exists today (read 2026-10-15)

| Piece | What it does | Limits |
|---|---|---|
| `docx4j-samples-docx4j/.../TrackChangesAccept.java` + `docx4j-samples-resources/.../AcceptChanges.xslt` (3.3.6) | XSLT over the main part: unwraps `w:ins`, drops `w:del`, drops a `w:tbl` whose first row carries `w:trPr/w:del` | accept only; main part only; formatting revisions survive (Word still shows them); **a deleted paragraph mark is dropped without joining the paragraph to the next**, where Word joins them; **a deleted row other than the whole-table case keeps the row**, where Word removes it |
| `org.docx4j.convert.out.common.preprocess.AcceptTrackedChanges` (17.2.0, CR-012) | the accepted view, over JAXB: `w:del` and `w:moveFrom` removed, `w:ins` and `w:moveTo` unwrapped, a deleted paragraph mark joins its paragraph with the next (first's content and `w14:paraId`, second's properties), a deleted row removed; descends content controls and, through `TraversalUtil` `McMode.ALL`, text boxes and both `mc:AlternateContent` branches | accept only; main part only; formatting revisions left by design (the current properties are what a renderer shows); move range markers left |
| `ConversionFeatures.PP_COMMON_ACCEPT_TRACKED_CHANGES` | runs the above on the exporter's working copy; off by default (PDF and HTML show the markup, deletions struck through); `Paginate` turns it on | - |
| docx4j-diffx (`Differencer`) | writes the difference of two documents as `w:ins`/`w:del` | nothing to accept or reject it with |
| docx4j-python 0.1.0 (`docx4j_py/model/content/tracked_change.py`, `Body.get_tracked_changes`, `accept_all`, `reject_all`) | `TrackedChange` (Office JS `Word.TrackedChange`: type, author, date, text, `get_range`, `accept`, `reject`); covers `w:ins`, `w:del`, `w:moveFrom`, `w:moveTo`, paragraph-mark `ins`/`del`, `w:rPrChange`, `w:pPrChange`, row `ins`/`del`; accept follows `AcceptTrackedChanges` and also drops `rPrChange`/`pPrChange`; reject is the mirror; reverse document order | body only (as far as read); no section, table, cell or numbering property changes; no cell `ins`/`del`/merge |
| docx4j-ts-editor (`app/review.ts`) | a review UI that accepts and rejects in the editor | editor-side; not a library API |

## 2. What is missing

1. **Reject**: nothing in Java.
2. **Formatting revisions** for a saved document: `w:rPrChange`, `w:pPrChange`, `w:sectPrChange`,
   `w:tblPrChange`, `w:tblGridChange`, `w:trPrChange`, `w:tcPrChange`, `w:numberingChange`. They
   survive an accept today, so Word still shows them as tracked.
3. **Moves**: the `w:moveFromRangeStart`/`End` and `w:moveToRangeStart`/`End` markers are left
   behind by `AcceptTrackedChanges`.
4. **Paragraph marks**: an inserted mark (`w:pPr/w:rPr/w:ins`) on reject, and on accept the
   marker's removal. The deleted mark's join exists (accept) and needs its mirror (reject: drop
   the marker only).
5. **Rows**: an inserted row (`w:trPr/w:ins`) on reject (the row goes, its content with it); a
   deleted row on reject (the marker goes).
6. **Cells**: `w:cellIns`, `w:cellDel`, `w:cellMerge`.
7. **Parts**: headers, footers, footnotes, endnotes, comments. **Stories inside the content**:
   content controls (done), text boxes (done), both branches of `mc:AlternateContent` (done for
   accept), math (`m:r` carries `w:ins`/`w:del` since CR-010).
8. **Scope**: by author, by date, by kind, or for a range (a content control, a bookmark).
9. **A report** of what a document contains (counts by kind, author and part) before anything is
   decided, which is also what an MCP tool needs.

## 3. Design

### 3.1 The API

A new package, `org.docx4j.model.revisions` (name to settle, D1):

- `TrackedChanges.in(WordprocessingMLPackage)` lists every tracked change, in document order per
  part, as `TrackedChange` views: `getType()` (Office JS's `Word.TrackedChangeType` names:
  `ADDED`, `DELETED`, `FORMATTED`, plus docx4j's finer `getKind()`, e.g. `RUN_INSERTION`,
  `PARAGRAPH_MARK_DELETION`, `ROW_INSERTION`, `TABLE_PROPERTIES`, `MOVE_FROM`), `getAuthor()`,
  `getDate()`, `getText()`, `getPart()`, `getElement()`, `accept()`, `reject()`.
- `TrackedChanges.acceptAll(pkg)` and `rejectAll(pkg)`, each returning the count, with an optional
  `TrackedChangeFilter`: author, date range, kinds, parts, and a range (a content control's or a
  bookmark's extent). They work in reverse document order per part (python's rule), so that a
  paragraph join never disturbs a change still to be done.
- `TrackedChanges.report(pkg)`: counts by kind, author and part, as a plain object with a JSON
  form for docx4j-mcp.

The vocabulary is python's and Office JS's, so that the ports agree and docx4j-mcp's tool can
present one model. Java naming conventions apply.

### 3.2 What accepting and rejecting do

The rule per markup. Each row is a probe in phase 0 with Word's Accept All and Reject All as the
goldens (§4); rows marked "to settle" are where the spec is silent and Word decides.

| Markup | Accept | Reject |
|---|---|---|
| `w:ins` (runs) | unwrap | remove |
| `w:del` (runs) | remove | unwrap; `w:delText` to `w:t`, `w:delInstrText` to `w:instrText` |
| `w:moveFrom` / `w:moveTo` | remove from / unwrap to | unwrap from / remove to |
| move range markers | removed | removed |
| paragraph mark deleted (`w:pPr/w:rPr/w:del`) | join with the next paragraph (as `AcceptTrackedChanges`) | drop the marker |
| paragraph mark inserted (`w:pPr/w:rPr/w:ins`) | drop the marker | join with the next (python's rule; which properties survive, to settle) |
| `w:rPrChange` / `w:pPrChange` | drop | put the recorded properties back |
| `w:sectPrChange` | drop | put the recorded section properties back (header and footer references are not in `CT_SectPrBase`, so they stay) |
| `w:tblPrChange`, `w:tblGridChange`, `w:trPrChange`, `w:tcPrChange` | drop | put back |
| `w:numberingChange` | drop | to settle (it records the old number's text, not its definition) |
| row inserted (`w:trPr/w:ins`) | drop the marker | remove the row |
| row deleted (`w:trPr/w:del`) | remove the row (a table left without rows goes too) | drop the marker |
| `w:cellIns` / `w:cellDel` / `w:cellMerge` | to settle (does Word 365 still write them?) | to settle |
| `w:ins` / `w:del` in math (`m:r`) | as runs | as runs |

Everything above applies in every story: the main document, headers, footers, footnotes,
endnotes, comments; and inside content controls, text boxes (DrawingML and VML), and **both**
branches of `mc:AlternateContent`, so that an Office reading either branch sees the same result
(`McMode.ALL`, CR-021). Bookmarks and comment ranges inside deleted content go where Word puts
them (to settle). Adjacent runs left with identical properties may be merged afterwards (python
tidies them); Word does not require it.

### 3.3 What becomes of the existing pieces

- `AcceptTrackedChanges` becomes a client of the new code. The conversion keeps its own semantics
  (the accepted view, main part, formatting revisions left as they are), so PDF and HTML output
  with `PP_COMMON_ACCEPT_TRACKED_CHANGES` do not change unless D2 says the other parts should be
  accepted for a conversion too.
- The `TrackChangesAccept` sample is repointed to `TrackedChanges.acceptAll`, and a
  `TrackChangesReject` sample is added. `AcceptChanges.xslt` is retired (D3), its two bugs noted
  in the CHANGELOG.
- `w:trackRevisions` in the settings part is not changed by accepting or rejecting; an option
  turns tracking off.

### 3.4 Tests and the oracle

- Unit tests per row of §3.2, both pathways of nothing (this is model code), and a round-trip
  check that Word opens every result.
- **diffx as a second oracle**: for documents A and B, `accept(diff(A, B))` should equal B and
  `reject(diff(A, B))` should equal A, modulo diffx's own normalisation.
- python's `accept_all`/`reject_all` run over the same probes, so the two ports agree or the
  difference is recorded.

## 4. Phase 0: the probes

One probe document per group, made in Word 365 with tracking on (real Word markup, not
docx4j-built, so that the forms are Word's): run insertions and deletions, field codes,
paragraph-mark insertions and deletions, moves, run and paragraph formatting, section
properties, table, row and cell properties and grids, row insertions and deletions (one row,
all rows), cell insertion, deletion and merge (if Word 365 still produces them), numbering
changes, and the same in a header, a footer, a footnote, an endnote, a comment, a content
control, a text box, and an equation. Jason saves each, then has Word accept all and reject all
through COM (`Revisions.AcceptAll()` / `RejectAll()`, the methods in the portfolio's
`docs/office_output_methods.md`), saving the two results as the goldens. Comparison is on the
XML after both are re-saved by Word, so that serialisation differences do not count.

## 5. Phases

| Phase | Content | Estimate |
|---|---|---|
| 0 | the probes and Word's Accept All and Reject All goldens; the §3.2 "to settle" rows settled; python run over them | 1 day + Jason's Word run |
| 1 | the API (`TrackedChanges`, `TrackedChange`, report) for runs, moves and their markers, paragraph marks, run and paragraph formatting, rows; every story part; content controls, text boxes, `mc:AlternateContent`, math | 3 to 4 days |
| 2 | section, table, grid, row, cell and numbering property changes; cell insertions, deletions and merges (as Word 365 writes them); the filters (author, date, kind, part, range) | 2 to 3 days |
| 3 | `AcceptTrackedChanges` on the new code; the samples repointed and `TrackChangesReject` added; the XSLT retired; Getting Started; hand-offs | 1 day |

About two weeks in all, most of it in phases 1 and 2.

## 6. Out of scope

- **Recording** edits as tracked changes (python's `tracking` module is the writing half); a
  separate CR if wanted.
- Comparing documents (docx4j-diffx's job; this CR only consumes its output).
- Legacy and rare markup: `w:customXmlInsRangeStart` and its kin, `w:conflictIns`/`w:conflictDel`
  (Word's legacy conflict markup), Word 2003 XML.
- Showing tracked changes differently in PDF or HTML (the markup view stays the default; the
  accepted view stays a conversion feature).
- pptx and xlsx (PowerPoint has no tracked changes in this sense; Excel's shared-workbook
  revisions are a different model).

## 7. Decisions for Jason

1. **D1**: the package and names (`org.docx4j.model.revisions`, `TrackedChanges`).
2. **D2**: whether a conversion with `PP_COMMON_ACCEPT_TRACKED_CHANGES` also accepts in headers,
   footers and notes once the code can, which would change PDF and HTML output for documents
   tracked there.
3. **D3**: retire `AcceptChanges.xslt` outright, or keep it deprecated for a release.
4. **D4**: phase 2's cell-level changes only if phase 0 shows Word 365 still writes them.

## 8. Hand-offs

docx4j-python: the new cases (section, table, cell, numbering, the other parts) and any
difference phase 0 finds. docx4j-core-ts and docx4j-ts-editor: the same model. docx4j-mcp: a
`tracked-changes` tool over `report`, `acceptAll` and `rejectAll` (the portfolio's proposed
`mcp/tracked-changes-tools`). The portfolio session adds the registry entry.
