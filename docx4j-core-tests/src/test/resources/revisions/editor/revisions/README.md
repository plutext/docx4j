# The Word-made revision fixtures (ED-005 section 13)

E4.c (tracked changes in the model) is tested against revisions Word made, never against markup a
test wrote, which would only test the writer against itself. `revisions-base.docx` is where they
start: built with the engine by `../build/revisions-base.mjs`, it holds no revision, only the text
to revise and, in blue italics, what to do to it in Word, as whom and in which order.

## What to do

On the reference machine, from `revisions-base.docx` each time:

1. **Word 2010**: follow the instructions at the top of the document and in each section (sections
   1 to 12; in section 11 only the two comments, since Word 2010 has no replies or "done"). Save as
   `revisions-word2010.docx`.
2. **Word 15**: the same, all of section 11 included (a comment, a reply, the reply marked done,
   and a comment of Author B's own). Save as `revisions-word15.docx`.

Both saves are in this directory (2026-09-28), which the per-fixture suites read as they read
`opendope/` (`allFixtures` in `../../helpers.mts`), so they round-trip with every other fixture;
`test/revisions.test.mts` checks what each section yields.

## What each section yields

| Section | ED-005 section 5.2 row, or the "preserved" list | Expected markup |
|---|---|---|
| 1 | Inserted and deleted text, two authors | `w:ins`, `w:del` with `w:delText`, authors A and B |
| 2 | An insertion deleted by another author | `w:ins` (A) holding a `w:del` (B) |
| 3 | Moved text | `w:moveFrom`, `w:moveTo`, their range markers (the ends between blocks), the move's `w:name`, and the moved paragraph's mark as `w:pPr/w:rPr/w:moveFrom` and `w:moveTo` |
| 4 | Run formatting changed | `w:rPrChange` in each changed run |
| 5 | Paragraph properties changed | `w:pPrChange` (alignment, indent, style) |
| 6 | Paragraph mark inserted, and one deleted | `w:pPr/w:rPr/w:ins`, `w:pPr/w:rPr/w:del` |
| 7 | Row inserted, and one deleted | `w:trPr/w:ins`, `w:trPr/w:del`, the cells' runs marked |
| 8 | Table properties changed (preserved) | `w:tblPrChange`, `w:tblGridChange`, `w:tcPrChange` on each cell |
| 9 | Numbering changed | Word 2010 and Word 15 both write `w:pPrChange` with the old `w:numPr` (so a paragraph properties change the editor holds, not a preserved `w:numberingChange`) |
| 10 | An inline atom inserted or deleted | a `w:tab` in a `w:del`, a `w:br` and a note reference in a `w:ins` (and a revision in the footnotes part, preserved) |
| 11 | Comments (for E4.e) | `comments.xml`; in Word 15 `commentsExtended.xml` with `w15:paraIdParent` and `w15:done`, and `people.xml` (no `commentsIds.xml`: that is Word 2016's) |
| 12 | Section properties changed (preserved) | `w:sectPrChange` on the final section |

The revision-scale documents for E4.g's figures, at 200 and 1,000 revisions (ED-005 section 9.4), are built
by the engine at test time from `../large/ms-docx-250.docx` (`../build/revisions-scale.mjs`), not kept here.

## Word's accept and reject, for step C4

`check18/`, `check20/`, `check22/`, `check24/`, `check25/`, `check26/`, `check28/` and `check29/` hold files Word 16 saved when
Jason ran core-ts's checks 18, 20, 22, 24, 25, 26 and 28 (docx4j-core-ts `test/README.md`), copied from that repository's
`test/fixtures/revisions/`. They are what Word does when a change is accepted or rejected: a move
from either half, and all at once; edited moves and broken ones; a deleted paragraph, a lone
deleted mark, a deletion across a mark; formatting and a paragraph's properties changed back with
tracking on (checks 24 and 26, held by `test/track.test.mts`); a list turned to bullets and rejected, and an item bulleted and moved a level
(25a, 25b, 25e and 25f: check 25's 25c and 25d tracked nothing, core-ts aeef42e and d68e803). `test/review.test.mts` compares the editor's accept and
reject with them (ED-005 section 12.6). Check 28 (core-ts 57cab79): bold over an italic run and a plain one, one
Formatted change "alpha beta", and two tables' property records; `review.test.mts` holds the listing and the runs'
accept and reject to its files (ED-005 section 12.12). Check 29 (core-ts 25b50f5): Enter at a paragraph's end, tracked and
not, after a heading, a Normal paragraph and a mark Author A inserted, and in the middle of one; Word puts the new mark at the
caret every time, and after a heading records a `w:pPrChange` on the new line; `track.test.mts` compares the editor's saves
with each file (ED-005 section 12.17). Check 30 (run by Jason in Word 16.0.20326.20158 on 2026-09-30; its `result.json`,
copied from core-ts's `test/fixtures/revisions/check30/`, committed there in 426e7b5): the markup a tracked insertion
ended as around each kind of run holder, and what Office JS listed over it; `check30.test.mts` builds a document per
case and holds the listing to it (ED-005 section 12.40). Check 31 (run by Jason on 2026-10-01; its
`result.json` copied from core-ts, committed there in 2414220): rows inserted with tracking on, in Word's
markup and the engine's, with what Office JS listed and what Word's Accept All and Reject All left;
`check31.test.mts` holds the listing and the editor's resolution to it (ED-005 section 12.45).
`allFixtures` does not read these folders.

