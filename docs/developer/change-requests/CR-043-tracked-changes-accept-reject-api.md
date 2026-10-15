# CR-043: Accepting and rejecting tracked changes - a docx4j-core API

Status: IN PROGRESS - proposed 2026-10-15 (drafted at Jason's request, relayed by the portfolio session for its
"Aspose to docx4j" comparison); refined 2026-10-16; phase 0 DONE 2026-10-16 (Jason: "start CR-043 phase 0"): the
fixtures copied in, the Word probe run (Word 16.0.20326) and read, every "unmeasured" row of §3.2 that Word can produce
now measured (§4.4).  For 17.3.3.  Four phases (§5); decisions
in §7.

Refined 2026-10-16 (no implementation yet; Jason: "it would be good to include CR-043 tracked changes in our
17.3.3 release ... do not implement anything yet"), from what docx4j-ts-editor and docx4j-core-ts have built and
measured in Word: core-ts's engine already lists, accepts and rejects most of this markup, held to Word's own
Accept All and Reject All saves by its checks 16 to 36 (§1, §3.2), so most of §3.2's "to settle" rows are now
measured. §4 is the fixture coverage against the schema, and the probes still to make.

## 0. Summary

Aspose.Words has `Document.acceptAllRevisions()` and per-revision `accept()`/`reject()`.
docx4j's answer has been a sample (`TrackChangesAccept`, with `AcceptChanges.xslt`, since 3.3.6)
that only accepts, only reads the main document part, and gets two cases wrong. docx4j-core has
had a better accepting pass since 17.2.0 (`AcceptTrackedChanges`, CR-012). But it is a
conversion preprocessor: it leaves formatting revisions alone and touches only the main document
part, which is right for drawing a PDF and wrong for a document that is saved again.

This CR gives docx4j-core a public API to **list, accept and reject** tracked changes, in every
story of a document, with **Word 365's own Accept All and Reject All as the oracle**. It is a port
of docx4j-core-ts's engine (CR-002 sections 29 and 35: `TrackedChange.mts`, `tableRevisions.mts`),
whose rules Word's saved files settled, onto the JAXB tree, extended where core-ts stops: math,
footnotes and endnotes, text boxes, both branches of `mc:AlternateContent`, fields resolved as
units, and a note removed with its reference.

## 1. What exists today (read 2026-10-15, the TypeScript side 2026-10-16)

| Piece | What it does | Limits |
|---|---|---|
| `docx4j-samples-docx4j/.../TrackChangesAccept.java` + `docx4j-samples-resources/.../AcceptChanges.xslt` (3.3.6) | XSLT over the main part: unwraps `w:ins`, drops `w:del`, drops a `w:tbl` whose first row carries `w:trPr/w:del` | accept only; main part only; formatting revisions survive (Word still shows them); **a deleted paragraph mark is dropped without joining the paragraph to the next**, where Word joins them; **a deleted row other than the whole-table case keeps the row**, where Word removes it |
| `org.docx4j.convert.out.common.preprocess.AcceptTrackedChanges` (17.2.0, CR-012) | the accepted view, over JAXB: `w:del` and `w:moveFrom` removed, `w:ins` and `w:moveTo` unwrapped, a deleted paragraph mark joins its paragraph with the next (first's content, second's properties and `w14:paraId`), a deleted row removed; descends content controls and, through `TraversalUtil` `McMode.ALL`, text boxes and both `mc:AlternateContent` branches | accept only; main part only; formatting revisions left by design; move range markers left |
| `ConversionFeatures.PP_COMMON_ACCEPT_TRACKED_CHANGES` | runs the above on the exporter's working copy; off by default; `Paginate` turns it on | - |
| docx4j-diffx (`Differencer`) | writes the difference of two documents as `w:ins`/`w:del` | nothing to accept or reject it with |
| **docx4j-core-ts** 0.3.x (`src/model/content/TrackedChange.mts`, `tableRevisions.mts`, `tracking.mts`, `comments.mts`, `Body.mts`; CR-002 sections 29, 32, 35, 37, 38) | Office JS's `Word.TrackedChange` over the typed tree: listing grouped as Office JS groups, `accept`/`reject` per change, `acceptAll`/`rejectAll`, and a sweep of what is never listed; runs at any depth of run holder, moves (either half resolves both), paragraph marks, run, mark and paragraph formatting, rows, cells, table and section property records, comments; tested against Word's saves (`test/tracking.test.mjs`, checks 16 to 36) | per `Body`: main body, headers, footers, comments; **not** footnotes, endnotes, text boxes (`w:txbxContent` not walked), math, or the `mc:AlternateContent` branch not chosen at load; a footnote whose reference goes is orphaned (Word removes it, §3.2); an inserted field is resolved in halves (a defect it found, §3.2) |
| **docx4j-ts-editor** (`src/review.mts`, `src/commands/`, `src/preserved.mts`; ED-005 sections 5.5, 5.6, 12.6, 12.44, 12.51) | a review UI: its own accept/reject of body text revisions as ProseMirror transforms, held to Word's Review-tab saves; the kinds it only preserves (table, cell, section records, a mark's `w:rPrChange`) resolved through core-ts | editor-side, not a library; body only (notes and headers read-only) |
| docx4j-python 0.1.0 (`docx4j_py/model/content/tracked_change.py`) | the same vocabulary; runs, moves, marks, `rPrChange`/`pPrChange`, rows | body only; no section, table, cell or numbering records |

## 2. What is missing in docx4j

1. **Reject**: nothing in Java.
2. **Formatting revisions** for a saved document: `w:rPrChange` (a run's and a paragraph mark's),
   `w:pPrChange`, `w:sectPrChange` (the body's and a paragraph's), `w:tblPrChange`,
   `w:tblGridChange`, `w:trPrChange`, `w:tcPrChange`, `w:tblPrExChange`. They survive an accept today.
3. **Moves**: the range markers, and the moved paragraph marks (`w:pPr/w:rPr/w:moveFrom|moveTo`).
4. **Paragraph marks**: an inserted mark on reject; the deleted mark's join needs its mirror.
5. **Rows and cells**: an inserted row on reject, a deleted row on reject; `w:cellIns`,
   `w:cellDel`, `w:cellMerge`.
6. **Fields** as units, and **side effects** on notes, comments, bookmarks and section breaks.
7. **Parts and stories**: headers, footers, footnotes, endnotes, comments; content controls,
   text boxes, both branches of `mc:AlternateContent`, math (`m:r` inside `w:ins`/`w:del`, and an
   `m:r`'s own `w:rPr/w:ins|del`).
8. **Legacy forms** Word no longer writes but may read: `w:numberingChange` (in `w:numPr` and in
   `w:fldChar`), `w:numPr/w:ins`, the `w:customXml*Range*` markers.
9. **Scope** (author, date, kind, part, range) and **a report**.

## 3. Design

### 3.1 The API

A new package, `org.docx4j.model.revisions` (name to settle, D1):

- `TrackedChanges.in(WordprocessingMLPackage)` lists every tracked change, part by part in document
  order, as `TrackedChange` views: `getType()` (Office JS's `ADDED`, `DELETED`, `FORMATTED`),
  `getKind()` (docx4j's finer kind: `RUN_INSERTION`, `PARAGRAPH_MARK_DELETION`, `MOVE`,
  `ROW_INSERTION`, `TABLE_PROPERTIES`, `SECTION_PROPERTIES`, `CELL_INSERTION`, ...),
  `getAuthor()`, `getDate()`, `getText()`, `getPart()`, `getPieces()`, `accept()`, `reject()`.
- **What one change is** is Office JS's grouping, measured (checks 22, 23, 27, 28, 30, 31): pieces
  that touch, of one type (all insertions, all deletions, or all formatting), by one author, whatever
  their dates, are one change; a different author or type splits; a revision nested in another is
  listed apart; moves are never grouped; a group's date is its first piece's. A paragraph mark is a
  piece whose text is `\r`; inserted or deleted rows group with the inserted or deleted text touching
  them; a run's and a mark's `w:rPrChange` that touch are one change; a table's property records are
  one change per table (or per row where only a row's cells changed); a section's is one change, its
  text the section's followed by `\f`. A row's text is its cells a tab apart, then `\r\n`.
- **Dates**: `w16du:dateUtc` where present (Word 365 writes both); else `w:date`, which Word means
  as local wall-clock time (core-ts `revisionDateOf`; check 36).
- `TrackedChanges.acceptAll(pkg)` and `rejectAll(pkg)`, each returning the count, with an optional
  `TrackedChangeFilter`: author, date range, kinds, parts, a range (a content control's or a
  bookmark's extent). **Order** (core-ts's, and why): last change first, so a paragraph join never
  disturbs a change still to be done; within a group, runs last first, then marks last first; then
  **list again and repeat until nothing is left**, since accepting a move carries a deletion nested
  in it out as a change of its own and resolving a broken move leaves plain `w:ins`/`w:del`; then
  **sweep** what is never listed (a `w:cellMerge`, a `w:numberingChange`, records that change
  nothing).
- `TrackedChanges.report(pkg)`: counts by kind, author and part, with a JSON form for docx4j-mcp.

The vocabulary is Office JS's, as python's and core-ts's are, so that the ports agree and
docx4j-mcp's tool can present one model. Where Office JS and Word's Review tab differ (core-ts
`docs/word-known-issues.md`: Office JS cannot list a move or a `w:cellMerge`, hides a table's inner
insertions while its formatting change is pending, omits another author's inserted middle row), the
**Review tab is the oracle**.

### 3.2 What accepting and rejecting do

Each row's source is the Word check that measured it (core-ts `test/README.md` checks N; ED-005's
section; this CR's own probe, cases CNN, §4.4) or "unmeasured", in which case the rule is core-ts's choice (D5).

| Markup | Accept | Reject | Source |
|---|---|---|---|
| `w:ins` around runs, at any depth of run holder (`w:hyperlink`, inline `w:sdt`, `w:smartTag`, `w:customXml`, `w:fldSimple`, `w:dir`/`w:bdo`) | unwrap | remove | 18, 30 |
| `w:del` around runs | remove | unwrap; `w:delText` to `w:t`, `w:delInstrText` to `w:instrText` | 18, 22 |
| `w:del` nested in another author's `w:ins` | accepting the deletion removes the text for real | rejecting the deletion leaves the text, still inserted | 22 |
| a **field** whose begin and end both lie in insertions (Word writes `w:ins` around begin + instruction and around end; separate and result plain) | keep it whole | **remove the whole field**, separate and result included | 30; C01 (PAGE), C16 (a hyperlink, which the object model inserts as a HYPERLINK field) (core-ts resolves it in halves and leaves a broken field: a defect it found) |
| a deleted field (`w:del` around the whole field, `w:delInstrText`) | remove it whole | restore it, `w:delInstrText` to `w:instrText` | C02 |
| **moves**: `w:moveFrom`/`w:moveTo`, the four range markers, the moved marks | either half accepts the whole move: the destination unwrapped, the source removed, content and mark; all four markers go | either half rejects the whole move, mirrored | 16-18 |
| move pairing | by `w:name` on the two range starts; each start pairs with its end by id; a half belongs to the move whose start of its side is open where it stands | | 18 |
| edited move | accept leaves text typed inside it (`w:ins`) and deletions nested in it pending | removes everything in the destination's range, typed text and nested deletions too, and restores the source plain (the Conflict dialog's default, "Keep original location text") | 20 |
| broken move | a destination with no source is an insertion (kept either way); halves without range markers resolve as plain `w:ins`/`w:del`; what remains of a broken pair becomes plain `w:ins`/`w:del` | as accept, mirrored | 20 |
| deleted paragraph mark | join with the next paragraph: the next's content moves in; **the result keeps the next paragraph's `w:pPr` and `w14:paraId`**, for markup Word did not write as well (a Heading 1 joined to a right-aligned Normal: Normal, right) | drop the marker | 18, 22, 33; C10b |
| deleted mark of a paragraph that is not a cell's last | join within the cell | drop the marker | C09 |
| a whole paragraph deleted (text and mark) | the paragraph goes; nothing carries to the next | drop the markers | 33 |
| deleted mark before a table, or at a cell's end | drop the marker; the paragraph stays | drop the marker | **Word refuses both edits** (C08 recorded nothing; C09's cell end: "Cannot edit Range"), so no Word file has them; the rule is core-ts's for other producers' markup |
| inserted paragraph mark | drop the marker | join with the next paragraph; **the result keeps the next paragraph's properties** (core-ts's and the editor's code alike: `removeParagraphMark` in the editor's `review.mts`) | C10 |
| `w:rPrChange` on a run | drop | replace the run's `w:rPr` with the recorded one, wholesale | 24, 28 |
| `w:rPrChange` on a mark (`w:pPr/w:rPr`) | drop | replace the mark's run properties, keeping its `w:ins`/`w:del`/`w:moveFrom`/`w:moveTo` | 27 |
| `w:pPrChange` | drop | replace `w:pPr` with the recorded `CT_PPrBase`, keeping the current `w:rPr` and `w:sectPr`; for a list paragraph, a restored `w:ind` equal to the level's is dropped, and a `w:numPr` with `w:numId` and no `w:ilvl` gets `w:ilvl` 0 | 18 section 9, 25, 26 |
| a record whose properties equal the current ones | no change: dropped by either | | 24, 26 |
| `w:sectPrChange` (the body's and a paragraph's) | drop | **merge**: lay the recorded members over the section, the rest left (Word records only what changed); a recorded `w:docGrid` with pitch 0 means none; header and footer references untouched | 18, 27; a paragraph's section C07 |
| `w:tblPrChange`, `w:tblGridChange`, `w:tcPrChange`, `w:trPrChange`, `w:tblPrExChange` (Word writes them as a set: every cell of the affected rows recorded, the old value even where unchanged; a `w:trPrChange` only for a row that had properties; a row's indent writes `w:tblPrExChange` with its row's `w:trPrChange` and `w:tcPrChange` and a merged grid, C23) | drop every record; a `w:trPr` left empty goes | **replace wholesale**: table, grid, row and cell properties back; a row in the change with no `w:trPrChange` has its `w:trPr` cleared (its `w:jc`, `w:trHeight`, `w:cnfStyle`); a merged grid collapsed (grid columns no row keeps apart become one, the spans recounted: C23's five columns back to two) | 27, 28; C23 |
| inserted row (`w:trPr/w:ins`) | drop the marker | remove the row; a table left without rows goes (check 30: Word also removed the paragraph before an inserted table whose mark was inserted) | 30, 31; an inserted table, C25 |
| deleted row (`w:trPr/w:del`) | remove the row; a table left without rows goes | drop the marker | base section 7; a table deleted whole (every row, cell text and mark deleted), C13 |
| a column added | the table change plus a `w:ins` of the new cells' text (no `w:cellIns`); added through the object model with its cells empty, nothing is recorded at all (C14) | Reject All left the column, empty | 27; C14 |
| `w:cellIns` / `w:cellDel` (Word no longer writes them) | insertion: drop the marker; deletion: remove the cell, the cell before (else after) spanning its grid column (`w:gridSpan` added, `w:tcW` added when both are `dxa`) | mirrored | 27 (handwritten, through Script Lab) |
| `w:cellMerge` | drop | restore `w:vMerge` from `w:vMergeOrig` | unmeasured: docx4j's reading (Office JS cannot list it) |
| `w:numberingChange` (`w:numPr`, `w:fldChar`) | drop | drop (it records the old number's text, not its definition) | Word writes `w:pPrChange` for a list change (18, 25) |
| `w:numPr/w:ins` | drop the marker | remove the `w:numPr` | unmeasured: docx4j's reading |
| `w:customXmlInsRangeStart`/`End` (Word's form around an inserted content control: a pair at the control's start and one at its end, its text in `w:ins`) | a piece of the insertion: the markers go, the control stays | **the whole control goes** | 30, 34; C16b |
| `w:ins`/`w:del` in math (around `m:r`) | as runs | as runs | `tracked-changes-equations.docx` (CR-010), Word's accept and reject 2026-10-16 |
| an inserted math structure (a `w:ins` in its `m:ctrlPr`, `EG_RPrMath`: `m:sSup` in that file) | the `w:ins` goes, its `w:rPr` kept | **the structure goes whole**, as a paragraph goes with its inserted mark | the same; a deleted structure by symmetry (unmeasured) |
| **comments** | a comment whose range start lies inside removed content goes: markers, reference run and entry | the same | 32 (a comment's own text: Word does not track edits to it, C18) |
| | a comment whose markers lie outside: the range markers inside removed content go, the reference run and entry stay | | the editor, Word 2010 and 15 |
| **footnotes and endnotes** | a note whose reference run goes is **removed from its part** (Word also marks the note's own content deleted or inserted with its reference) | the same | the editor's `review/` saves (footnotes, Word 2010 and 15); endnotes C12, C12b |
| a **bookmark** with its start or its end inside removed content | **removed whole** (C11: start before a deletion, end inside it, accepted) | the same (C11b: a bookmark around an insertion, rejected) | C11, C11b (core-ts drops only the markers inside, which leaves half a bookmark) |
| a drawing inside removed content | the run goes; **its relationship and part go** when nothing else references them (Word's saves keep no unreferenced image, C04, C05) | | C04, C05 |
| a deleted **section break** (a deleted mark whose `w:pPr` carries `w:sectPr`) | the join takes the next paragraph's `w:pPr`, so the two sections merge: **the merged section has the following section's properties, header and footer references included**; the first section's headers are dropped | drop the marker | C06 |

Everything above applies in every story: the main document, headers, footers, footnotes,
endnotes, comments, and inside content controls, text boxes (DrawingML and VML), and **both**
branches of `mc:AlternateContent` (`McMode.ALL`, CR-021: docx4j keeps both branches in the tree,
where core-ts keeps only the one chosen at load). Word's Accept All and Reject All cover the notes:
check 18's `accept-all.docx` and `reject-all.docx` leave no revision in `footnotes.xml`, and this CR's probe log
counts none left in any story (main, endnotes, comments, text boxes, headers, footers) after either; a text box's
revisions are written in both branches (C22).

**Word's save normalises**, so the comparison with its files must allow: an accepted HYPERLINK field becomes a
`w:hyperlink` (C16); a row's `w:tblPrEx` indent becomes `w:gridBefore`/`w:wBefore` (C23); note, comment and revision
ids are renumbered; every section is written with explicit header and footer references.

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

### 3.4 The JAXB port: what to mind

- **The recorded properties are not the live types.** `CTRPrChange.getRPr()` is an inner class
  `CTRPrChange.RPr`, `ParaRPrChange.getRPr()` a `CTParaRPrOriginal`, `CTPPrChange.getPPr()` a
  `PPrBase`, `CTSectPrChange.getSectPr()` a `SectPrBase`, `CTTblPrChange` a `CTTblPrBase`,
  `CTTblGridChange` a `TblGridBase`, `CTTrPrChange` a `CTTrPrBase`, `CTTcPrChange` a `TcPrInner`,
  `CTTblPrExChange` a `CTTblPrExBase`. Reject copies member by member into the live `RPr`, `PPr`,
  `SectPr`, `TblPr`, `TblGrid`, `TrPr`, `TcPr` (deep copies, `XmlUtils.deepCopy`), keeping what the
  base type lacks (`w:pPr`'s `w:rPr`, `w:sectPr`, `w:pPrChange`).
- **Holders are `JAXBElement`s where the schema has a choice**: `w:moveFrom`/`w:moveTo` are
  `JAXBElement<RunTrackChange>`, the range starts `JAXBElement<CTMoveBookmark>`, `w:delText` and
  `w:delInstrText` `JAXBElement<Text>` in `R.getContent()`; run-level revisions can stand in a
  paragraph, at block level (`Body`, `Tc`, `CTSdtContentBlock`: `EG_BlockLevelElts` admits
  `EG_RunLevelElements`) and inside run holders. Remove from the live list that holds the element
  now (core-ts's `currentOwner`), and fix up the `Child` parents after splicing.
- **Math**: `org.docx4j.math.CTR.getContent()` holds the math run's `w:rPr` with its `w:ins`/`w:del`
  (`EG_RPrMath`), and `CTMathRunTrackChange` the run-level kind.
- **Walking**: one resolver over every story part (main, headers, footers, footnotes, endnotes,
  comments), descending `w:txbxContent`, both `mc:AlternateContent` branches, `w:sdtContent` and
  `m:oMath` - what core-ts would do if starting again, its own words.
- **Ids**: resolution mostly removes markup. Where it rewrites a revision (what remains of a broken
  move becomes a plain `w:ins` or `w:del`) the revision keeps its id; any new id comes from above the
  package's highest annotation id (revisions, bookmarks, comments and range markers share one space,
  and Word 2010 refuses large ids).

### 3.5 Tests and the oracle

- **Word's saved files are the oracle**, compared structurally (each paragraph's text, style,
  alignment, indent, list level and runs' formatting; each table's rows and properties; comments;
  notes), as the editor's `test/word-review.test.mts` does; serialisation differences do not count.
- The Word-made fixtures of §4.1, copied into `docx4j-core-tests/src/test/resources/revisions/`
  with their provenance (Jason's test files, made from engine-built bases; no business documents).
- Unit tests per row of §3.2; handwritten markup only for the forms Word no longer writes.
- **diffx as a second oracle**: `accept(diff(A, B))` should equal B and `reject(diff(A, B))` A.
- **The ports agree**: core-ts's `acceptAll`/`rejectAll` and python's over the same fixtures, each
  difference either a docx4j bug or one of theirs, recorded (§8).

## 4. Phase 0: the fixtures

### 4.1 What exists, Word-made

All on Jason's reference machine (Word 2010, Word 15, and Word 365 16.0.20326/20430); paths under
`../docx4j-core-ts/test/fixtures/revisions/` unless stated (the editor's
`packages/editor-model/test/fixtures/` keeps copies and `revisions/`, `review/`, `comments/`).

| Fixture | What it holds | Word's accept / reject saves |
|---|---|---|
| editor `revisions/revisions-word2010.docx`, `-word15.docx` (from `revisions-base.docx`, 12 sections) | runs inserted and deleted by two authors; an insertion deleted by another; a moved paragraph with range markers and moved marks; `w:rPrChange`; `w:pPrChange` (alignment, indent, style, numbering); inserted and deleted marks; inserted and deleted rows; `w:tblPrChange` + `w:tblGridChange` + `w:tcPrChange`; a tab, a break and a note reference inside revisions, and a footnote's own revision and mark `w:rPrChange`; comments; the final section's `w:sectPrChange` | check18 (`accept-all`, `reject-all`, each half of the move) |
| check20 (39 files) | edited and broken moves | each case's `-accept` and `-reject` |
| check21, check22 | typing inside another author's revision; an insertion deleted by another | check22 whole and across |
| check24, 25, 26 | run formatting, list changes, paragraph properties, changed and changed back | their saves |
| check28 | bold over two runs; two tables' property records, `w:trPrChange` included | `28a-accept-all`, `28a-reject-all` |
| check29, check33 | Enter at a paragraph's end; a whole paragraph deleted | 33b accepted |
| check30, 31, 32 (`result.json`; check32 also a docx) | run holders and inserted fields; inserted tables; comments in resolved revisions | Office JS listings with Word's resolution recorded |
| check34 (`34b-tracked.docx`) | a data-bound control changed with tracking on: `w:customXmlInsRangeStart`, block-level `w:ins`/`w:del` in `w:sdtContent` | - |
| check36 | `w16du:dateUtc`, declared and undeclared | - |
| editor `review/review-for-word-word2010.docx`, `-word15.docx` | the editor's file through Word: note references in an insertion and in a deletion, a comment range in an insertion | `-accepted`, `-rejected` in both Words |
| docx4j `docx4j-core-tests/src/test/resources/tracked-changes-equations.docx` (CR-010) | `w:ins`/`w:del` around `m:r` in equations, `w16du:dateUtc` | none |

### 4.2 Against the schema: what no fixture has

From every `CT_TrackChange` use in `xsd/wml/wml.xsd` (the elements of `EG_RunLevelElts`,
`EG_ParaRPrTrackChanges`, `EG_RangeMarkupElements`, `EG_CellMarkupElements`, `EG_RPrMath`,
`EG_MathRunTrackChanges`, and the `*Change` records of `CT_RPr`, `CT_ParaRPr`, `CT_PPr`,
`CT_SectPr`, `CT_TblPr`, `CT_TblGrid`, `CT_TrPr`, `CT_TcPr`, `CT_TblPrEx`, `CT_NumPr` and
`CT_FldChar`), inventoried over the 198 fixture files 2026-10-16. JAXB binds them all, `w16du:dateUtc`
included (`CTTrackChange.getDateUtc()`, CR-023). Not in any Word-made fixture:

| Gap | Kind | Probe? (closed 2026-10-16: §4.4) |
|---|---|---|
| revisions in a **header, footer, endnote** or a **comment's text** | story | closed: C19, C20, C21 (a comment's text is not tracked, C18) |
| revisions in a **text box** (DrawingML `wps:txbx` in `mc:Choice` with its VML `mc:Fallback`) | story, both branches | closed: C22 |
| a deleted or inserted **field code** (`w:delInstrText`: in no fixture at all), a field straddling a revision's edge | field | closed: C01, C02, C16 (a straddling field not made) |
| a **drawing** inside an insertion or deletion | side effect | closed: C04, C05 |
| a **deleted section break**; a `w:sectPrChange` on a paragraph's section (only the body's is in a file) | section | closed: C06, C07 |
| a deleted mark **before a table**, at a **cell's end**, at a story's end | paragraph mark | closed: C08, C09 (Word refuses both) |
| an **inserted mark rejected**: whose properties survive (§3.2; a slip in the editor's account had them disagree) | paragraph mark | closed: C10 (the next's), C10b |
| a **bookmark** inside removed content; an **endnote** reference likewise | side effect | closed: C11, C11b, C12, C12b |
| a table **deleted whole**; an inserted table and an added column as docx (checks 27, 31 hold only JSON) | table | closed: C13, C25, C14 |
| a mark's `w:rPrChange` in the body (check 27 found it, in a footnote only in a file) | formatting | closed: C17 |
| a move of several paragraphs, into or within a table, and typing into a move's source then accepting | move | closed but typing into a source: C15 |
| run holders inside revisions (hyperlink, `w:fldSimple`, inline control) as docx (check 30 holds JSON) | holder | closed: C16, C16b (`w:fldSimple` not made) |
| Word's accept and reject of `tracked-changes-equations.docx` (math) | math | closed |
| `w:tblPrExChange` | table | closed: C23 (Word writes it for a row's indent) |
| `w:cellIns`, `w:cellDel`, `w:cellMerge`, `w:numberingChange`, `w:numPr/w:ins` | legacy (Word no longer writes them: checks 27, 18, 25) | no: handwritten, with check 27's rules |
| `w:customXmlDel*`, `w:customXmlMove*`, `w:conflictIns`/`w:conflictDel` | legacy | no (§6) |

### 4.3 The probe

Built 2026-10-16 (phase 0), in `docx4j-core-tests/src/test/resources/revisions/cr043/` and on the share at
`X:\revisions\cr043\` with its `RUN.txt`. The portfolio's `docs/office_output_methods.md` method 2 (PowerShell over
COM: one action for Jason, every step logged, a rerun resuming), rather than method 4 (by hand), since what is
measured here is Word's resolution of its own markup, not a gesture's record:

- **The base**, `cr043-base.docx`, built by `org.docx4j.model.revisions.Cr043ProbeBuilder` (test sources): a header,
  a footer, an endnote part (two notes), a comment, three sections, a PAGE field, an inline picture, a bookmark, and
  tables, each case tagged in its text.
- **Two cases tracked already**, written by the builder, which Word only resolves: C10, a Heading 1 paragraph's
  mark inserted before a Normal, right-aligned one (the join §3.2 had as disputed), and C10b, the same with the mark
  deleted (does Word's accept take the next paragraph's properties for markup it did not write itself, such as
  diffx's?).
- **`cr043-probe.ps1`** adds a text box untracked, turns tracking on, and makes 26 tracked edits through the object
  model: C01 a field inserted, C02 one deleted, C04 a picture inserted, C05 one deleted, C06 a section break deleted,
  C07 a section's margin changed (the paragraph's `w:sectPrChange`), C08 a mark deleted before a table, C09 one in a
  cell and the cell's end mark tried, C11 a bookmarked word deleted, C11b a bookmark put on inserted text, C12 an
  endnote reference deleted, C12b an endnote inserted, C13 a table deleted whole, C14 a column added, C15 three
  paragraphs cut and pasted into a cell (a move, if Word records one), C16 a hyperlink and C16b a content control
  inserted, C17 a paragraph bolded with its mark, C18 a comment's text edited, C19 the header and C20 the footer
  edited, C21 an endnote's text edited, C22 the text box's text edited, C23 a row's indent changed (`w:tblPrExChange`,
  if Word writes one), C25 a table inserted. It saves `cr043-tracked.docx`, then Word's
  `Document.AcceptAllRevisions()` and `RejectAllRevisions()` (the whole document, every story) of it as
  `cr043-accept-all.docx` and `cr043-reject-all.docx`, and the same for `tracked-changes-equations.docx`; its log
  records Word's build, each case's outcome, and the revisions left in each story after each resolution.

### 4.4 Phase 0's run (2026-10-16, Word 16.0.20326 on the VM)

Jason ran `cr043-probe.ps1` at 10:17: 25 of the 26 edits made, the 26th (C09's cell end) refused by Word ("Cannot
edit Range"); 22 revisions in the main story, 5 in the endnotes, 2 in the text box, 1 in each header and footer story,
none in the comments. Accept All and Reject All left none in any story, the equations document's 12 included. The
outputs and the log are in `docx4j-core-tests/src/test/resources/revisions/cr043/`. Read on Linux (each case's
markup tracked, accepted and rejected), they settle §3.2's rows as marked there; in short:

- **The two paragraph-mark joins**: both take the next paragraph's properties (C10 rejected insertion,
  C10b accepted deletion), for markup Word did not write too.
- **Word refuses** to delete a mark before a table (nothing recorded) and a cell's end mark.
- **Fields go whole**: an inserted PAGE field and an inserted hyperlink (a HYPERLINK field, from the object model)
  rejected lose their separator and result too; a deleted field accepted goes whole.
- **A deleted section break** merges the sections into the following one, its header and footer references with it.
- **Notes**: an endnote goes with its reference, as a footnote does; Word marks the note's own content too.
- **Bookmarks**: one with its start or end in removed content goes whole.
- **Pictures**: the image part goes with the last reference to it.
- **Content controls**: an inserted control rejected goes whole.
- **Math**: an inserted structure (its `m:ctrlPr` holding the `w:ins`) rejected goes whole.
- **Tables**: a row's indent is recorded as `w:tblPrExChange` with a merged grid, and restored wholesale; a table
  deleted whole goes; an inserted one goes on reject; a column added through the object model with its cells empty
  is not tracked at all.
- **A comment's text** is not tracked when edited.
- **A move** of three paragraphs into a cell was recorded as one move; accepted, the source's last mark, not part
  of the move, stays as an empty paragraph.

## 5. Phases

| Phase | Content | Estimate |
|---|---|---|
| 0 | DONE 2026-10-16: §4.1's fixtures copied in with their provenance (`docx4j-core-tests/src/test/resources/revisions/`, its `README.md`); §4.3's probe built, run by Jason and read (§4.4); §3.2's rows settled | 1 day + Jason's Word run |
| 1 | the API (`TrackedChanges`, `TrackedChange`, grouping, ordering, the sweep, report) for runs at any holder depth, fields as units, moves, paragraph marks, run, mark and paragraph formatting, rows; the side effects (comments, notes, bookmarks, drawings); every story part, text boxes, both `mc:AlternateContent` branches, content controls, math | 3 days |
| 2 | table, grid, row, cell and section property records; cell insertions and deletions, the legacy forms; the filters | 1.5 days |
| 3 | `AcceptTrackedChanges` on the new code; the samples repointed and `TrackChangesReject` added; the XSLT retired; Getting Started; the ports' agreement run; hand-offs | 1 day |

About a week and a half in all: less than proposed, since core-ts's checks have settled the rules
and its code is the model; the critical path to 17.3.3 is phase 0's Word run.

## 6. Out of scope

- **Recording** edits as tracked changes (python's `tracking` module, core-ts's tracker, the
  editor's); a separate CR if wanted.
- Comparing documents (docx4j-diffx's job; this CR only consumes its output).
- Legacy markup Word no longer writes or reads beyond §3.2's rows: `w:customXmlDel*` and
  `w:customXmlMove*` ranges, `w:conflictIns`/`w:conflictDel`, Word 2003 XML.
- Showing tracked changes differently in PDF or HTML.
- pptx and xlsx.
- Office JS's own quirks (§3.1): docx4j follows the Review tab.

## 7. Decisions for Jason

1. **D1**: the package and names (`org.docx4j.model.revisions`, `TrackedChanges`).
2. **D2**: whether a conversion with `PP_COMMON_ACCEPT_TRACKED_CHANGES` also accepts in headers,
   footers and notes once the code can, which would change PDF and HTML output for documents
   tracked there.
3. **D3**: retire `AcceptChanges.xslt` outright, or keep it deprecated for a release.
4. **D4**: the forms Word no longer writes (`w:cellIns`, `w:cellDel`, `w:cellMerge`,
   `w:numberingChange`, `w:numPr/w:ins`): resolve them by check 27's rules and docx4j's reading,
   with handwritten tests. Recommended: yes (a few hours; Word still reads them; core-ts does).
5. **D5**: where Word is unmeasured and phase 0 does not reach, follow core-ts's choice so the
   ports agree. Recommended: yes, except where Word's files say otherwise (notes, fields).
6. **D6**: 17.3.3's scope: the whole CR, or phases 0, 1 and 3 with phase 2 (table, section and cell
   records, filters) after. Recommended: the whole CR if phase 0's Word run comes early; phase 2 is
   the smaller.
7. **D7**: a drawing removed with its run: remove its relationship and image part when nothing
   else references them, or leave them. Word's saves keep no unreferenced image (C04, C05).
   Recommended: remove.

## 8. Hand-offs

- **docx4j-core-ts** (sent 2026-10-16): Word removes a footnote whose reference goes, on accept and
  reject (measured on the editor's `review/` saves; core-ts orphans it). Its inserted-field defect is
  its own (check 30). After phase 0: the new probe's files, and any rule docx4j settles differently.
- **docx4j-ts-editor** (sent 2026-10-16): the footnote finding; after phase 0, the paragraph-mark, bookmark, field and
  control rules. Its code already takes the next paragraph's properties on both joins ("the first's" was a slip in its
  account, it corrected 2026-10-16); a bookmark half inside removed content it keeps, a known limitation of its own.
- **docx4j-core-ts** (sent 2026-10-16, after phase 0): where Word differs from its code - a bookmark touching removed
  content goes whole, an inserted math structure goes whole, endnotes go with their reference - and what phase 0
  confirmed of its rules.
- **docx4j-python**: the measured rules of §3.2 against its 0.1.0 accept and reject (it covers runs,
  moves, marks, formatting and rows; tables, sections, cells, notes and fields are new to it).
- **docx4j-mcp**: a `tracked-changes` tool over `report`, `acceptAll` and `rejectAll` (the
  portfolio's proposed `mcp/tracked-changes-tools`).
- **The portfolio**: the registry entry (its session holds CR-043's entry).

## 9. Sources

- docx4j-core-ts: `docs/change-requests/CR-002-content-api.md` sections 29, 32, 35, 37, 38;
  `test/README.md` checks 16 to 36; `docs/word-known-issues.md` entries 1 to 8;
  `test/tracking.test.mjs`; its session's answers of 2026-10-16.
- docx4j-ts-editor: `docs/change-requests/ED-005-e4-implementation.md` sections 5.2, 5.5, 5.6, 6.9,
  12.3 to 12.7, 12.44, 12.45, 12.51; `packages/editor-model/test/fixtures/revisions/README.md`;
  `docs/word-divergences.md`; its session's answers of 2026-10-16.
- docx4j: `xsd/wml/wml.xsd` (the revision types and their containers), the generated
  `org.docx4j.wml` and `org.docx4j.math` classes; CR-010, CR-012, CR-021, CR-023.
