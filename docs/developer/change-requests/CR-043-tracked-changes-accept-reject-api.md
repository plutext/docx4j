# CR-043: Accepting and rejecting tracked changes - a docx4j-core API

Status: DONE 2026-10-16, all four phases (phase 3: Jason, "commit phase 2 then start phase 3"; D2 and D3 decided) -
proposed 2026-10-15 (drafted at Jason's request, relayed by the portfolio session for its
"Aspose to docx4j" comparison); refined 2026-10-16; phase 0 DONE 2026-10-16 (Jason: "start CR-043 phase 0"): the
fixtures copied in, the Word probe run (Word 16.0.20326) and read, every "unmeasured" row of §3.2 that Word can produce
now measured (§4.4); phase 1 DONE 2026-10-16 (Jason: "start CR-043 phase 1"): `org.docx4j.model.revisions` lists,
accepts and rejects every kind but the table, cell and section records, held to Word's saves by 66 tests (§12);
phase 2 DONE 2026-10-16 (Jason: "commence phase 2"; D4 and D6 decided): those records, cells inserted and deleted, the
legacy forms and the filters (§13); phase 3 DONE 2026-10-16: `AcceptTrackedChanges` on the new code in every story
(D2), the samples, the XSLT deprecated (D3), Getting Started, the ports' agreement run (§14). For 17.3.3, the whole CR
(D6).  Four phases (§5); decisions in §7.

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
  `TrackedChangeFilter`: authors, types, kinds, a date range, parts, a range (a content control's content or a
  bookmark's extent, read once before anything is resolved). Every condition set must hold, and they apply to the
  single changes: of a group only the pieces that match are resolved (U19). **Order** (core-ts's, and why): last change first, so a paragraph join never
  disturbs a change still to be done; within a group, runs last first, then marks last first; then
  **list again and repeat until nothing is left**, since accepting a move carries a deletion nested
  in it out as a change of its own and resolving a broken move leaves plain `w:ins`/`w:del`; then
  **sweep** what is never listed (a `w:cellMerge`, a `w:numberingChange`, records that change
  nothing).
- **Text boxes are listed once**: Word writes a text box's revisions in both `mc:AlternateContent` branches with
  different ids (C22: 50 and 51 in `mc:Choice`, 52 and 53 in `mc:Fallback`). `in()` lists the Choice branch's; a
  per-change accept or reject also resolves the Fallback's twin, paired by position in the twin `w:txbxContent` (the
  n-th revision of its kind), never by id; `acceptAll`/`rejectAll` walk both branches (§10 item 2).
- **A `TrackedChange` view is valid until the next accept or reject on its package** (a join removes a paragraph;
  Office JS throws on a stale one); `acceptAll` relists as it goes.
- `TrackedChanges.report(pkg)`: counts by kind, author and part, with a JSON form for docx4j-mcp. (Between phases 1
  and 2 it also counted the table and section records as unresolved, §10 item 5; phase 2 resolves them and that
  count went.)

The vocabulary is Office JS's, as python's and core-ts's are, so that the ports agree and
docx4j-mcp's tool can present one model. Where Office JS and Word's Review tab differ (core-ts
`docs/word-known-issues.md`: Office JS cannot list a move or a `w:cellMerge`, hides a table's inner
insertions while its formatting change is pending, omits another author's inserted middle row), the
**Review tab is the oracle**.

### 3.2 What accepting and rejecting do

Each row's source is the Word check that measured it (core-ts `test/README.md` checks N; ED-005's
section; this CR's own probe, cases CNN, §4.4) or "unmeasured", in which case the rule is core-ts's choice or
docx4j's reading (D5), and the row is on §11's running list.

| Markup | Accept | Reject | Source |
|---|---|---|---|
| `w:ins` around runs, at any depth of run holder (`w:hyperlink`, inline `w:sdt`, `w:smartTag`, `w:customXml`, `w:fldSimple`, `w:dir`/`w:bdo`) | unwrap | remove | 18, 30 |
| `w:del` around runs | remove | unwrap; `w:delText` to `w:t`, `w:delInstrText` to `w:instrText` | 18, 22 |
| `w:del` nested in another author's `w:ins` | accepting the deletion removes the text for real | rejecting the deletion leaves the text, still inserted | 22 |
| a **field** whose begin and end both lie in insertions (Word writes `w:ins` around begin + instruction and around end; separate and result plain) | keep it whole | **remove the whole field**, separate and result included | 30; C01 (PAGE), C16 (a hyperlink, which the object model inserts as a HYPERLINK field) (core-ts resolves it in halves and leaves a broken field: a defect it found) |
| a deleted field (`w:del` around the whole field, `w:delInstrText`) | remove it whole | restore it, `w:delInstrText` to `w:instrText` | C02 |
| a field straddling a revision's edge (begin or end alone in a revision) | the halves resolve as runs | the same | unmeasured (D5; not made in phase 0) |
| **moves**: `w:moveFrom`/`w:moveTo`, the four range markers, the moved marks | either half accepts the whole move: the destination unwrapped, the source removed, content and mark; all four markers go | either half rejects the whole move, mirrored | 16-18 |
| move pairing | by `w:name` on the two range starts; each start pairs with its end by id; a half belongs to the move whose start of its side is open where it stands | | 18 |
| edited move | accept leaves text typed inside it (`w:ins`) and deletions nested in it pending | removes everything in the destination's range, typed text and nested deletions too, and restores the source plain (the Conflict dialog's default, "Keep original location text") | 20 |
| broken move | a destination with no source is an insertion (kept either way); halves without range markers resolve as plain `w:ins`/`w:del`; what remains of a broken pair becomes plain `w:ins`/`w:del` | as accept, mirrored | 20 |
| deleted paragraph mark | join with the next paragraph: the next's content moves in; **the result keeps the next paragraph's `w:pPr` and `w14:paraId`**, for markup Word did not write as well (a Heading 1 joined to a right-aligned Normal: Normal, right) | drop the marker | 18, 22, 33; C10b |
| deleted mark of a paragraph that is not a cell's last | join within the cell | drop the marker | C09 |
| a whole paragraph deleted (text and mark) | the paragraph goes; nothing carries to the next | drop the markers | 33 |
| deleted mark before a table, or at a cell's end | drop the marker; the paragraph stays | drop the marker | **Word refuses both edits** (C08 recorded nothing; C09's cell end: "Cannot edit Range"), so no Word file has them; the rule is core-ts's for other producers' markup |
| a deleted or inserted mark with **no following `w:p` in the same content list** (a story's end: the body's last paragraph before `w:sectPr`, a cell's, a note's, a text box's, a block `w:sdtContent`'s last; or a next sibling that is a table or a block control) | drop the marker, no join; **a join never crosses a container boundary** | drop the marker | unmeasured (D5; §10 item 4) |
| inserted paragraph mark | drop the marker | join with the next paragraph; **the result keeps the next paragraph's properties** (core-ts's and the editor's code alike: `removeParagraphMark` in the editor's `review.mts`) | C10 |
| `w:rPrChange` on a run | drop | replace the run's `w:rPr` with the recorded one, wholesale | 24, 28 |
| `w:rPrChange` on a mark (`w:pPr/w:rPr`) | drop | replace the mark's run properties, keeping its `w:ins`/`w:del`/`w:moveFrom`/`w:moveTo` | 27 |
| `w:pPrChange` | drop | replace `w:pPr` with the recorded `CT_PPrBase`, keeping the current `w:rPr` and `w:sectPr`; for a list paragraph, a restored `w:ind` equal to the level's is dropped, and a `w:numPr` with `w:numId` and no `w:ilvl` gets `w:ilvl` 0 | 18 section 9, 25, 26 |
| a record whose properties equal the current ones | no change: dropped by either | | 24, 26 |
| `w:sectPrChange` (the body's and a paragraph's) | drop | **merge**: lay the recorded members over the section, the rest left (Word records only what changed); a recorded `w:docGrid` with pitch 0 means none; header and footer references untouched | 18, 27; a paragraph's section C07 |
| `w:tblPrChange`, `w:tblGridChange`, `w:tcPrChange`, `w:trPrChange`, `w:tblPrExChange` (Word writes them as a set: every cell of the affected rows recorded, the old value even where unchanged; a `w:trPrChange` only for a row that had properties; a row's indent writes `w:tblPrExChange` with its row's `w:trPrChange` and `w:tcPrChange` and a merged grid, C23) | drop every record; a `w:trPr` left empty goes | **replace wholesale**: table, grid, row and cell properties back; a row in the change with no `w:trPrChange` has its `w:trPr` cleared (its `w:jc`, `w:trHeight`, `w:cnfStyle`); a merged grid collapsed (grid columns no row keeps apart become one, and those past every row's last cell go with the rows' `w:gridAfter` and `w:wAfter`; the spans recounted: C23's five columns back to two). Either way, a row's `w:tblPrEx` left saying only what the table's `w:tblPr` says goes (Word wrote none for C23's row after either) | 27, 28; C23 |
| inserted row (`w:trPr/w:ins`) | drop the marker | remove the row; a table left without rows goes (check 30: Word also removed the paragraph before an inserted table whose mark was inserted) | 30, 31; an inserted table, C25 |
| deleted row (`w:trPr/w:del`) | remove the row; a table left without rows goes | drop the marker | base section 7; a table deleted whole (every row, cell text and mark deleted), C13 |
| a column added | the table change plus a `w:ins` of the new cells' text (no `w:cellIns`); added through the object model with its cells empty, nothing is recorded at all (C14) | Reject All left the column, empty | 27; C14 |
| `w:cellIns` / `w:cellDel` (Word no longer writes them) | insertion: drop the marker; deletion: remove the cell, the cell before (else after) spanning its grid column (`w:gridSpan` added, `w:tcW` added when both are `dxa`) | mirrored | 27 (handwritten, through Script Lab) |
| `w:cellMerge` | drop | restore `w:vMerge` from `w:vMergeOrig` | unmeasured: docx4j's reading (Office JS cannot list it) |
| `w:numberingChange` (`w:numPr`, `w:fldChar`) | drop | drop (it records the old number's text, not its definition) | Word writes `w:pPrChange` for a list change (18, 25) |
| `w:numPr/w:ins` | drop the marker | remove the `w:numPr` | unmeasured: docx4j's reading |
| `w:customXmlInsRangeStart`/`End` (Word's form around an inserted content control: a pair at the control's start and one at its end, its text in `w:ins`) | a piece of the insertion: the markers go, the control stays | **the whole control goes** | 30, 34; C16b |
| `w:ins`/`w:del` in math: Word writes them **inside** `m:r` (`m:r` > `w:ins` > `m:rPr`, `w:rPr`, `m:t`; `JAXBElement<CTMathRunTrackChange>` in `CTR.getContent()`) | `w:ins`: unwrap its content into the `m:r`; `w:del`: remove it (an `m:r` left empty goes) | mirrored | `tracked-changes-equations.docx` (CR-010), Word's accept and reject 2026-10-16 (§10 item 1) |
| an inserted math structure (a `w:ins` in its `m:ctrlPr`, `EG_RPrMath`: `CTCtrlPr.getIns()`, a `CTRPrChange`; `m:sSup` in that file) | the `w:ins` goes, its `w:rPr` kept (copied from the `CTRPrChange.RPr` list form into a live `RPr`, `CTCtrlPr.setRPr`) | **the structure goes whole** - the `m:ctrlPr`'s grandparent (`m:sSup`, `m:d`, `m:acc`, ...) removed from the list that holds it - as a paragraph goes with its inserted mark | the same; a deleted structure by symmetry (unmeasured) |
| `w:ins`/`w:del` holding `m:r` or `m:oMath` (`m:EG_OMathMathElements` in `CT_RunTrackChange`) | as runs | as runs | schema-legal, in no fixture (§10 item 1) |
| **comments** | a comment whose range start lies inside removed content goes: markers, reference run and entry; with it its entries in `commentsExtended`, `commentsIds` and `commentsExtensible`, and its replies (`w15:paraIdParent`) | the same | 32 (a comment's own text: Word does not track edits to it, C18); the extended parts and replies unmeasured (§10 item 4) |
| | a comment whose markers lie outside: the range markers inside removed content go, the reference run and entry stay | | the editor, Word 2010 and 15 |
| **footnotes and endnotes** | a note whose reference run goes is **removed from its part** (Word also marks the note's own content deleted or inserted with its reference) | the same | the editor's `review/` saves (footnotes, Word 2010 and 15); endnotes C12, C12b |
| a **bookmark** with its start or its end inside removed content, or around content that goes and left empty by it | **removed whole** (C11: start before a deletion, end inside it, accepted) | the same (C11b: a bookmark around an insertion, rejected); a bookmark empty before stays | C11, C11b (core-ts drops only the markers inside, which leaves half a bookmark); a deletion accepted under a bookmark, U15 |
| a drawing (or any related part) inside removed content | the run goes; **its relationship and part go** when nothing else references them - counting every part's relationships, not the story's alone, and every relationship the removed subtree references: `a:blip` `r:embed`/`r:link`, `v:imagedata` `r:id`, chart parts, `w:object`'s OLE part and preview (Word's saves keep no unreferenced image, C04, C05) | | C04, C05; the wider scope unmeasured (D7, §10 item 4) |
| a deleted **section break** (a deleted mark whose `w:pPr` carries `w:sectPr`) | the join takes the next paragraph's `w:pPr`, so the two sections merge: **the merged section has the following section's properties, header and footer references included**; the first section's headers are dropped, and their parts with them (D7: the tracked file's six header and six footer parts are three and three in Word's accepted save) | drop the marker | C06 |

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

- `AcceptTrackedChanges` becomes a client of the new code (done, phase 3): with D2 it accepts in every story, so PDF
  and HTML output with `PP_COMMON_ACCEPT_TRACKED_CHANGES` - Paginate and a TOC's page numbers too - lay out accepted
  headers, footers, notes, text boxes and equations. It runs `TrackedChanges.acceptAllForConversion`, which on a
  conversion's working copy keeps the first paragraph's `w14:paraId` on a join (Paginate keys by it) and touches
  nothing outside the story parts (the copy shares its content types, relationship objects and the parts not copied
  deep with the original).
- The `TrackChangesAccept` sample is repointed to `TrackedChanges.acceptAll`, and a
  `TrackChangesReject` sample is added. `AcceptChanges.xslt` stays, with a comment saying it is deprecated since
  17.3.3 and what to use (D3), its limits noted in the CHANGELOG.
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
  `JAXBElement<RunTrackChange>`, the range starts `JAXBElement<CTMoveBookmark>`, `w:delInstrText` a
  `JAXBElement<Text>` in `R.getContent()` (but `w:delText` is a class of its own, `DelText`, not a `Text`: phase 1); run-level revisions can stand in a
  paragraph, at block level (`Body`, `Tc`, `CTSdtContentBlock`: `EG_BlockLevelElts` admits
  `EG_RunLevelElements`) and inside run holders. Remove from the live list that holds the element
  now (core-ts's `currentOwner`), and fix up the `Child` parents after splicing.
- **Math**: three forms (§3.2): `w:ins`/`w:del` inside `m:r` as `JAXBElement<CTMathRunTrackChange>` in
  `CTR.getContent()`; in a structure's `m:ctrlPr` as `CTCtrlPr.getIns()`/`getDel()` (`CTRPrChange`, its `getRPr()`
  the list form); and `RunIns`/`RunDel` holding math elements. `AcceptTrackedChanges` handles none of them today, so
  phase 3 also fixes PDF output of tracked equations.
- **Also** (§10 item 6): export `org.docx4j.model.revisions` in docx4j-core's `module-info.java`; a structural
  comparator in docx4j-core-tests for §3.5 (none exists), merging adjacent runs of equal `w:rPr` (Word resplits runs on
  save) and allowing §3.2's normalisations; `getDate()` tolerates a null date (diffx writes `w:date=""`);
  `w:customXmlInsRangeStart` is a `JAXBElement<CTTrackChange>` declared per scope (`P`, `Tr`, ...); `ParaRPr` carries
  `moveFrom`/`moveTo` as `CTTrackChange`; handle nesting generically (no fixture has a mark with both `w:ins` and
  `w:del`).
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
| 1 | DONE 2026-10-16 (§12): the API (`TrackedChanges`, `TrackedChange`, grouping, ordering, the sweep, report) for runs at any holder depth, fields as units, moves, paragraph marks, run, mark and paragraph formatting, rows; the side effects (comments, notes, bookmarks, drawings); every story part, text boxes, both `mc:AlternateContent` branches, content controls, math | 3 days |
| 2 | DONE 2026-10-16 (§13): table, grid, row, cell and section property records; cell insertions and deletions, the legacy forms; the filters | 1.5 days |
| 3 | DONE 2026-10-16 (§14): `AcceptTrackedChanges` on the new code; the samples repointed and `TrackChangesReject` added; the XSLT deprecated (D3); Getting Started; the ports' agreement run; hand-offs | 1 day |

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

1. **D1**: the package and names (`org.docx4j.model.revisions`, `TrackedChanges`). DECIDED: as proposed (Jason,
   2026-10-16: "D1: ok").
2. **D2**: whether a conversion with `PP_COMMON_ACCEPT_TRACKED_CHANGES` also accepts in headers,
   footers and notes once the code can, which would change PDF and HTML output for documents
   tracked there. DECIDED: yes (Jason, 2026-10-16).
3. **D3**: retire `AcceptChanges.xslt` outright, or keep it deprecated for a release. DECIDED: keep it - it is
   harmless - with a comment saying that since 17.3.3 it is deprecated and what to use instead (Jason, 2026-10-16).
4. **D4**: the forms Word no longer writes (`w:cellIns`, `w:cellDel`, `w:cellMerge`,
   `w:numberingChange`, `w:numPr/w:ins`): resolve them by check 27's rules and docx4j's reading,
   with handwritten tests. Recommended: yes (a few hours; Word still reads them; core-ts does). DECIDED: yes (Jason,
   2026-10-16).
5. **D5**: where Word is unmeasured and phase 0 does not reach, follow core-ts's choice so the
   ports agree. Recommended: yes, except where Word's files say otherwise (notes, fields). DECIDED: yes, with a
   running list of what is unmeasured kept (Jason, 2026-10-16: "D5: ok, but keep a running list of things which are
   unmeasured"): §11.
6. **D6**: 17.3.3's scope: the whole CR, or phases 0, 1 and 3 with phase 2 (table, section and cell
   records, filters) after. Recommended: the whole CR if phase 0's Word run comes early; phase 2 is
   the smaller. DECIDED: the whole CR (Jason, 2026-10-16).
7. **D7**: a drawing removed with its run: remove its relationship and image part when nothing
   else references them, or leave them. Word's saves keep no unreferenced image (C04, C05).
   Recommended: remove. DECIDED: remove (Jason, 2026-10-16: "D7: yes"), in the wider scope of §3.2's row.

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
  moves, marks, formatting and rows; tables, sections, cells, notes and fields are new to it), and what the agreement
  run found (§14).
- **docx4j-core-ts** (after phase 3): the agreement run's findings (§14): C23's grid columns no row reaches and a row's
  redundant `w:tblPrEx`, a bookmark left empty around removed content (C11b), unreferenced parts (D7), and its known
  scope (notes, text boxes, math).
- **docx4j-mcp**: a `tracked-changes` tool over `report`, `acceptAll`, `rejectAll` and `TrackedChangeFilter` (the
  portfolio's proposed `mcp/tracked-changes-tools`): the API is in docx4j 17.3.3.
- **The portfolio**: the registry entry (CR-043 and its phases done, this session's commits).

## 9. Sources

- docx4j-core-ts: `docs/change-requests/CR-002-content-api.md` sections 29, 32, 35, 37, 38;
  `test/README.md` checks 16 to 36; `docs/word-known-issues.md` entries 1 to 8;
  `test/tracking.test.mjs`; its session's answers of 2026-10-16.
- docx4j-ts-editor: `docs/change-requests/ED-005-e4-implementation.md` sections 5.2, 5.5, 5.6, 6.9,
  12.3 to 12.7, 12.44, 12.45, 12.51; `packages/editor-model/test/fixtures/revisions/README.md`;
  `docs/word-divergences.md`; its session's answers of 2026-10-16.
- docx4j: `xsd/wml/wml.xsd` (the revision types and their containers), the generated
  `org.docx4j.wml` and `org.docx4j.math` classes; CR-010, CR-012, CR-021, CR-023.

## 10. Review before phase 1 (2026-10-16, a second session, Fable 5.1)

Read against the generated sources, the fixtures and the probe's files. Phase 0 is as described; every type claim in
§3.4 holds; the id-space claim holds (the Word 15 fixture numbers revisions 0 to 45, then the bookmark 48 and the
comments from 49). The corrections and the rules still unstated, for the implementer:

1. **Math: §3.2's rows and §3.4's bullet describe the wrong structure.** Word writes `w:ins`/`w:del` *inside*
   `m:r`, not around it: `tracked-changes-equations.docx` has `m:r` > `w:ins` > (`m:rPr`, `w:rPr`, `m:t`), bound
   as `JAXBElement<org.docx4j.wml.CTMathRunTrackChange>` in `org.docx4j.math.CTR.getContent()` (CR-010's
   `CT_MathRunTrackChange`, `EG_MathRunTrackChanges`). Three forms, all to handle:
   - inside `m:r`: that wrapper. Accept `w:ins` = unwrap its content into the `m:r`; accept `w:del` = remove it
     (an `m:r` left empty goes); reject mirrored.
   - in `m:ctrlPr` (`EG_RPrMath`): `CTCtrlPr.getIns()`/`getDel()`, typed `CTRPrChange`, whose `getRPr()` is the
     `CTRPrChange.RPr` list form - so "its `w:rPr` kept" on accept is the list-to-live-`RPr` copy of §3.4's first
     bullet (`CTCtrlPr.setRPr` takes `RPr`). Rejecting an inserted structure removes the ctrlPr's grandparent
     (`m:sSup`, `m:d`, `m:acc`, ...) from the list that holds it (`m:oMath`'s content or an enclosing `m:e`).
   - `RunIns`/`RunDel` holding `m:r` or `m:oMath` elements (`m:EG_OMathMathElements` in `CT_RunTrackChange`,
     `JAXBElement`s in `getCustomXmlOrSmartTagOrSdt()`): schema-legal, in no fixture; as runs.
   The existing `AcceptTrackedChanges` handles none of these (it knows `RunIns`/`RunDel`/`Tr` only), so phase 3
   also fixes PDF output of tracked equations.
2. **Text boxes list twice unless paired.** In C22 the `mc:Choice` branch's revisions have ids 50 and 51 and the
   `mc:Fallback`'s twins 52 and 53: different ids. Rule: `TrackedChanges.in()` lists the Choice branch's only; a
   per-change accept or reject also resolves the Fallback twin, paired by position in the twin `w:txbxContent`
   (the n-th revision of that kind), never by id; counts are per listed change; `acceptAll`/`rejectAll` walk both
   branches (`McMode.ALL`) as §3.2 says.
3. **§7 records recommendations, not decisions.** Phase 1 needs D1, D5 and D7 answered (or "take the
   recommendations" from Jason) before code; D2 and D3 by phase 3; D4 and D6 by phase 2.
4. **Rules the table leaves unstated** (state them in §3.2 as unmeasured, D5):
   - a deleted or inserted mark with no following `w:p` in the same content list - a story's end (the body's last
     paragraph before `w:sectPr`, a cell's, a note's, a text box's, a block `w:sdtContent`'s last), or a next sibling
     that is a table or a block control: drop the marker, no join. A join never crosses a container boundary
     (§4.2's "at a story's end" is closed by nothing: C08 and C09 are the table and the cell).
   - a field straddling a revision's edge (not made, §4.2): the halves resolve as runs.
   - D7's "nothing else references them": count every part's relationships, not the story's alone, and cover every
     relationship referenced from the removed subtree - `a:blip` `r:embed`/`r:link`, `v:imagedata` `r:id`, chart
     parts, `w:object`'s OLE part and its preview - not only pictures.
   - a removed comment's entries in commentsExtended, commentsIds and commentsExtensible go with it; its replies
     (commentsExtended `w15:paraIdParent`) go with the parent. Unmeasured (C18 did not reach it).
   - a dropped section's header and footer parts (C06) are left unreferenced: they follow D7.
5. **API contract.** A `TrackedChange` view is valid until the next accept or reject on the package (a join removes
   a paragraph; Office JS throws on a stale one); `acceptAll` relists. Phase 1's `acceptAll` leaves table, grid, row,
   cell and section records in the file, so Word still shows the document as tracked until phase 2 lands: `report`
   counts them under a kind of their own (`TABLE_PROPERTIES`, `SECTION_PROPERTIES`) marked unresolved, so the ports'
   agreement run is not misread.
6. **Implementer's checklist.** Export `org.docx4j.model.revisions` in docx4j-core's `module-info.java`
   (docx4j-core-tests has no module-info). No structural comparator exists in docx4j-core-tests: write one for §3.5,
   merging adjacent runs of equal `w:rPr` (Word resplits runs on save) and allowing §3.2's normalisations. The diffx
   oracle meets diffx's known `w:date=""` output, so `getDate()` must tolerate a null date. `w:customXmlInsRangeStart`
   is a `JAXBElement<CTTrackChange>` declared per scope (`P`, `Tr`, ...); `ParaRPr` carries `moveFrom`/`moveTo` as
   `CTTrackChange`. No fixture has a mark carrying both `w:ins` and `w:del`; handle nesting generically rather than
   by case.

Folded into §3.1, §3.2 and §3.4 the same day by the first session (items 1, 2, 4, 5, 6; the math structure was
checked against `tracked-changes-equations.docx`: 28 revisions inside `m:r`, none around it). Item 3 - D1, D5 and D7
before phase 1 - is Jason's.

## 11. Unmeasured: the running list (D5)

Where docx4j follows a rule Word has not been seen to apply, it is listed here (Jason, 2026-10-16, D5), with where
the rule comes from and how it could be measured. Phases 1 to 3 add to it as they meet cases, and a probe that
settles one moves it into §3.2 with its source. Kept current; dated where added after 2026-10-16.

| # | Case | The rule docx4j follows | From | How to measure |
|---|---|---|---|---|
| U1 | a mark with no following `w:p` in its content list (a story's, cell's, note's, text box's or block control's last paragraph; a mark before a table or block control) | drop the marker, no join; never across a container boundary | core-ts; §10 | Word refuses the table and cell-end cases (C08, C09): only a story's or a block control's last paragraph is left to probe, by hand markup |
| U2 | a field straddling a revision's edge | the halves resolve as runs | core-ts; §10 | a probe case deleting from text into a field's result |
| U3 | a removed comment's `commentsExtended`/`commentsIds`/`commentsExtensible` entries and its replies | go with it | §10 | a probe case with a reply on a comment inside a deletion (Word 365, not the object model's C18 edit) |
| U4 | D7's scope beyond images: `v:imagedata`, chart parts, `w:object`'s OLE part and preview, `r:link` | removed with the last reference | §10; Word's saves for images (C04, C05) | a probe case with a chart and an OLE object inside a deletion |
| U5 | a dropped section's header and footer parts (C06) | removed when unreferenced (D7) | - | **settled 2026-10-16**: Word's accepted save keeps three of six header and three of six footer parts; in §3.2 |
| U6 | a deleted math structure (`w:del` in its `m:ctrlPr`) | accept removes the structure whole | symmetry with the inserted one | **settled 2026-10-16** (phase 1): `tracked-changes-equations.docx` has one (a deleted `m:acc`), and Word's Accept All removed it whole, its Reject All kept it with its `m:ctrlPr`'s run properties |
| U7 | `w:ins`/`w:del` holding `m:r` or `m:oMath` | as runs | the schema; §10 | Word does not write it (checks so far): handwritten markup, Word's resolution |
| U8 | `w:cellMerge` | accept drops it; reject restores `w:vMerge` from `w:vMergeOrig` | docx4j's reading (Office JS cannot list it); phase 2's `legacyFormsSwept` | handwritten markup through COM, Word's resolution |
| U9 | `w:numberingChange`, `w:numPr/w:ins` | `w:numberingChange` dropped either way; `w:numPr/w:ins`: accept drops the marker, reject removes the `w:numPr` (neither listed: swept) | core-ts; docx4j's reading; `legacyFormsSwept` | handwritten markup through COM |
| U10 | `w:cellIns`/`w:cellDel` in a saved file | check 27's rules (Office JS, handwritten): listed, one change per cell; the cell removed widens the cell before it (else after) | check 27; `cellsInsertedAndDeleted` | handwritten markup through COM, Word's file-level resolution |
| U11 | a mark carrying both `w:ins` and `w:del` (an inserted mark another author deleted) | resolved as nested revisions are (check 22's rule for runs) | §10; check 22 | a probe: Enter by one author, the mark deleted by another |
| U12 | typing into a move's source, then accepting | the typed `w:ins` left pending, as for the destination (check 20) | core-ts leaves it alone | a probe after check 20's form |
| U13 | `w:sectPrChange` rejected: what becomes of header and footer references | untouched (the record is `CT_SectPrBase`) | core-ts | a probe changing a section's headers with tracking on |
| U14 | a run-level `w:fldSimple` inside a revision | as a run holder (check 30's listing) | check 30 (Office JS); core-ts | a probe case with a simple field inserted |
| U15 | a bookmark around a deletion that is accepted (added 2026-10-16, phase 1) | goes with it, left empty, as one around a rejected insertion does (C11b); a bookmark that was empty already stays | C11b by symmetry | a probe case bookmarking deleted text whole |
| U16 | an equation argument's own `m:ctrlPr` (an `m:e`'s) carrying `w:ins`/`w:del` (added 2026-10-16) | the marker goes and its run properties stay, either way: no structure is two up | docx4j's reading | Word does not write it (the equations fixture has none): handwritten markup |
| U17 | a run holder (`w:hyperlink`, `w:smartTag`, `w:fldSimple`) left empty by resolving (added 2026-10-16) | stays, empty; a hyperlink's relationship stays while it does | core-ts (it keeps them too) | a probe rejecting a hyperlink's text inserted inside an existing link; Word's save may drop the empty element |
| U18 | a move's listing (added 2026-10-16) | each half of each moved paragraph is listed as a change of its own (Office JS cannot list a move, checks 16 and 17); resolving any one resolves the whole move, and the others are then stale | core-ts | Word's Review pane: how many "Moved" entries a three-paragraph move (C15) shows |
| U19 | a filter on a group (added 2026-10-16, phase 2) | applies to its single changes: a group partly matched has only its matching pieces resolved (a kind filter on a deleted paragraph resolves its text and leaves its mark, or the other way round) | docx4j's choice; Word's Review tab has no such filter (its "show changes by" reviewer filter is display only) | none needed: an API choice |
| U20 | a table's records that change nothing, a grid's record alone (added 2026-10-16) | not listed (checks 24 and 26's rule for runs and paragraphs, core-ts's for tables); dropped by the sweep either way | core-ts | a probe changing a table property and back with tracking on |

## 12. Phase 1 (2026-10-16)

**What was built**, in `docx4j-core` `org.docx4j.model.revisions` (exported in `module-info.java`):

- `TrackedChanges`: `in(pkg)` (the listing, story by story: main document, footnotes, endnotes, comments, then headers
  and footers in the order the sections reference them; a text box's changes after the story it is in, its
  `mc:Fallback` twins attached, not listed), `acceptAll`/`rejectAll` (last first, relisting until nothing is left,
  both branches walked, the stray `w:customXmlIns*` markers swept; each returns how many `in` listed), `report`.
- `TrackedChange`: `getType()` (`ADDED`, `DELETED`, `FORMATTED`), `getKind()` (the CR's kinds, plus `INSERTION`,
  `DELETION` and `FORMATTING` for a group whose pieces are of different kinds, `FIELD_INSERTION`,
  `CONTENT_CONTROL_INSERTION`, `MATH_STRUCTURE_INSERTION`/`DELETION`), `getAuthor()`, `getDate()` (an `Instant`, null
  when there is none), `getId()`, `getText()`, `getPart()`, `getPieces()`, `isResolvable()`, `accept()`, `reject()`.
  A stale view throws `IllegalStateException`; a phase 2 kind `UnsupportedOperationException`.
- `TrackedChangesReport`: total, unresolved (the phase 2 kinds, `Kind.isResolvable()` false), by kind, author and
  part; `toJson()`.
- Inside: `Lister` (the walk and Office JS's grouping), `Resolver` (the operations, moves included), `Removals` (what
  removed content takes with it, settled once a resolution is done: notes whose references went, comments whose
  anchors went with their extended entries and replies, bookmarks touching removed content, relationships nothing
  references and the parts only they targeted), `Props` (a recorded `w:rPr`/`w:pPr` back into the live type through
  its XML), `Trees` (the live lists, names, text, and a reflective walk for what a subtree references).

**How it is held to Word** (docx4j-core-tests, 66 tests, and docx4j-diffx-tests):

- `TrackedChangesWordTest`: docx4j's Accept All and Reject All against Word's on the probe (`cr043-tracked.docx`), the
  equations, check 18, the editor's review file (both Words), checks 26, 28 and 33, compared by
  `DocumentDescription` (each story's paragraphs - style, alignment, indent, list, the mark's revision - and their
  runs of one formatting and revision merged, tables, notes, comments, text boxes in every branch, equations,
  bookmarks, each section's effective headers and footers, the image, header and footer parts). It allows what Word's
  save normalises: ids, dates, rsids, field codes, inherited header references, the order of comments, list numbers
  (Word renumbers them), the empty headers, footers and notes parts Word's save adds. Table, cell and section
  properties are left out until phase 2 (`DocumentDescription.of(pkg, true)` shows them). And every Word-made fixture
  (about 120) accepted all and rejected all, then resolved one change at a time, relisting after each: nothing
  resolvable left, the listing shrinking each time, the result saved and loaded again.
- `TrackedChangesOneChangeTest`: Accept This Change and Reject Change on check 18's move from either half, check 20's
  edited and broken moves (from the destination; case 14 from both), check 22's deleted paragraph and deletion across
  a mark, check 25's list levels.
- `TrackedChangesTest`: the contract (grouping across authors and types, dates, a stale view, a piece resolved on its
  own, the phase 2 kinds and the report) and the unmeasured rules on handwritten markup (U1, U2, U3, U15, a hyperlink's relationship in a
  deleted row, the text box twin, an equation structure); the equations list as 12 changes, as Word counts them.
- docx4j-diffx-tests `AcceptRejectOracleTest` (§3.5's second oracle): every pair of diffx's eight test paragraphs, and
  two bodies, accepted give the newer's text and rejected the older's.

**Found on the way:**

- Word removes a bookmark put around content that goes when that content goes (C11b; §3.2's row said only "a bookmark
  with its start or its end inside"): docx4j drops a bookmark left empty by a removal, one that was empty before stays.
- The equations fixture settles U6 (a deleted structure accepted goes whole).
- docx4j-diffx, as the second oracle found it: a paragraph the newer has and the older does not is written with no
  `w:ins` (so rejecting cannot take it away; the oracle's `anInsertedParagraph` is `@Ignore`d for it); a deleted
  paragraph's mark is never marked, so accepting leaves an empty paragraph; deleted text is written as `w:t`, not
  `w:delText`, inside `w:del`; and the runs of its `w:ins` and `w:del` lose their run properties. Its own limitations,
  for a diffx CR if wanted; the oracle compares text and ignores empty paragraphs.
- §3.4 had `w:delText` as `JAXBElement<Text>`: it is `DelText`, a class of its own (corrected above).

**Left for phase 2** as planned (done, §13): table, grid, row, cell and section records (listed, unresolved),
`w:cellIns`/`w:cellDel`, the legacy forms (`w:cellMerge`, `w:numberingChange`, `w:numPr/w:ins`), the filters. The
existing `AcceptTrackedChanges` and the samples are untouched until phase 3.

## 13. Phase 2 (2026-10-16)

**What was built:** `Tables` (docx4j-core, inside the package): a table's records listed as one change when they
change anything (compared as canonical XML, the markers and records left out), its text the table's rows where its
own properties or grid changed, else the changed rows'; accepted, every record dropped and an emptied `w:trPr` with
it; rejected, restored wholesale (through the records' XML, as phase 1's run and paragraph records) and a merged grid
collapsed; cells inserted and deleted (`removeCell`, check 27's widening); a section's record laid over the section
(C07, check 18). The sweep now also takes a table's records that change nothing, `w:cellMerge`, `w:numberingChange`
(in `w:numPr` and `w:fldChar`) and `w:numPr/w:ins` (D4). `TrackedChangeFilter` (public): authors, types, kinds, a date
range, parts, `within(SdtElement)`, `withinBookmark(name)`; `TrackedChanges.in(pkg, filter)`,
`acceptAll(pkg, filter)`, `rejectAll(pkg, filter)` (no sweep with a filter: what is swept belongs to no change). Phase
1's `Kind.isResolvable()` and the report's unresolved count are gone: every kind listed is resolved.

**How it is held to Word:** `DocumentDescription` now shows table, row, cell and section properties (canonical XML)
and the records' presence, and every Word test compares them: the probe's C23 (a row indent: `w:tblPrExChange`,
`w:trPrChange`, `w:tcPrChange` and a merged grid) and C07 (a paragraph's section) both ways, check 18's table and the
body's section, check 28's two tables. What Word's save adds was found on the way and is allowed for in the comparator,
not done by the API: a `w:tblLook` on a table with none, `w:tcW` auto 0 on a cell with no width, `w:cols w:space=720`
on a section with none, and a row's `w:tblPrEx` of zero top and bottom cell margins (C25's inserted rows, accepted).
`TrackedChangesTest` adds the records (listed, accepted, rejected, reported; records that change nothing), a section
merge, cells inserted and deleted, the legacy forms swept, and each filter (author and type, a kind splitting a
group, dates, a bookmark's extent, a control's content). 75 tests in all, and every fixture still resolves whole and
one change at a time.

**Found:** C23's reject needs more than core-ts's grid collapse: Word dropped the grid columns no row's cells reach
(the first row's `w:gridAfter` went with them), and a row's `w:tblPrEx` repeating the table's `w:tblPr` (in §3.2's
row now; for core-ts, with phase 3's hand-offs).

## 14. Phase 3 (2026-10-16)

**The conversion.** `AcceptTrackedChanges.process(pkg)` is `TrackedChanges.acceptAllForConversion(pkg)`: every story
accepted (D2), as `acceptAll` does, except that nothing outside the story parts is touched and a join keeps the first
paragraph's `w14:paraId`. Both found by reading the callers: `PartialDeepCopy` hands the working copy the original's
`ContentTypeManager`, the same `Relationship` objects and the parts it does not copy deep (the comments' extended parts
among them), so removing an override or an extended comment entry there would change the document being converted;
and Paginate keys a joined paragraph by its first (its `fo:block` id), where Word's rule, which `acceptAll` follows,
keeps the next's. Its seven tests pass unchanged; `everyStoryOnTheWorkingCopyOnly` adds that the probe's working copy
is left with no change in any story while the original keeps its changes, parts, content types and relationships. The
list-only `process(List, Object)` is deprecated, unchanged. docx4j-export-fo-tests 852/0 (Paginate and the TOC's page
numbers through it).

**The rest.** The samples (`TrackChangesAccept` lists, reports, accepts and saves; `TrackChangesReject` rejects one
author's with a filter, then one change at a time, then the rest), run on the probe. `AcceptChanges.xslt` keeps its
templates under a comment saying it is deprecated and what to use (D3). Getting Started: a section "Tracked changes:
accept and reject" after Pagination, in the docx (inserted with docx4j, its TOC updated with `Docx4J.updateToc`) and
patched into the md and html as earlier sections were; the PDF follows at release (`etc/GenGettingStartedDocs.java`).
`ConversionFeatures.PP_COMMON_ACCEPT_TRACKED_CHANGES`'s Javadoc and the CHANGELOG say what changed.

**The ports' agreement run** (`docx4j-core-tests/src/test/resources/revisions/agreement/`, `PortsAgreement`): docx4j's
Accept All and Reject All of eleven Word-made fixtures against docx4j-core-ts 0.4.0's (its main body, headers and
footers) and docx4j-python 0.1.0's (its body), compared by `DocumentDescription`. Lines differing, accept / reject:

| Fixture | core-ts | python |
|---|---|---|
| `cr043-tracked` (the probe) | 10 / 14 | 42 / 60 |
| `tracked-changes-equations` | 10 / 10 | 10 / 10 |
| `revisions-word15` (check 18's) | 2 / 0 | 15 / 24 |
| `review-for-word` (the editor's) | 0 / 0 | 2 / 4 |
| check 28 | 0 / 0 | 12 / 12 |
| check 25 `25a`, `25e` | 0 / 0, 0 / 0 | 0 / 6, 0 / 2 |
| checks 22, 26 (two), 33 | 0 | 0 |

Every difference is a port's: each fixture above but `25a`'s and `25e`'s accept (which no Word save covers, and where
all three agree) is one of the Word tests docx4j passes, so Word's own save takes docx4j's side. core-ts: its known
scope (notes, text boxes and math are not walked; parts are not removed, D7), and C23 (its grid collapse keeps the
column no row reaches and the first row's `w:gridAfter`). python: moves Word wrote (with range markers between the
paragraphs) are left unresolved; check 25's list rules on reject (a restored `w:ind` equal to the level's dropped, the
recorded `w:ilvl` 0 written) are not applied; a note whose reference went is left; an emptied comment range is kept;
table, section and header/footer changes, bookmarks, the content-control markers and text boxes are outside what it
resolves. No difference was docx4j's. The hand-offs (§8) carry them.
