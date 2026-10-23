# Tracked-change fixtures (CR-043)

The oracle for docx4j's accept/reject API (`docs/developer/change-requests/CR-043-tracked-changes-accept-reject-api.md`)
is what Word does: these are documents Word made with tracking on, and Word's own Accept All and Reject All of them,
saved. None is a business document: each began as a test file a docx4j port's engine built, edited in Word by Jason.

## `core-ts/checkNN/` - docx4j-core-ts's Word checks

Copied 2026-10-16 from docx4j-core-ts `test/fixtures/revisions/` at ab9041f (committed files only). Each check's
procedure and findings are in that repository's `test/README.md` ("Word acceptance (manual)", check NN) and its
`docs/change-requests/CR-002-content-api.md` (sections 29, 32, 35, 37, 38); Word 365 16.0.20326.20158 for checks
18 to 31, 16.0.20430.20118 from check 32 (portfolio `docs/office_output_methods.md` section 2).

| Check | What | Word's resolution |
|---|---|---|
| 18 | a moved paragraph (the editor's `revisions-word15.docx`, section 3) | `accept-all`, `reject-all`, and each half of the move accepted and rejected |
| 20 | edited and broken moves | each case's `-accept` and `-reject` |
| 21 | typing inside another author's revision | - |
| 22 | an insertion deleted by another author | whole and across, accepted and rejected |
| 24, 25, 26 | run formatting, list changes, paragraph properties, changed and changed back | their saves |
| 28 | bold over two runs; two tables' property records (`w:trPrChange` included) | `28a-accept-all`, `28a-reject-all` |
| 29 | Enter at a paragraph's end | - |
| 30, 31 | run holders and inserted fields; inserted tables (Office JS listings, JSON) | recorded in the JSON |
| 32 | comments inside resolved revisions (a docx and JSON) | recorded in the JSON |
| 33 | a whole paragraph deleted | `33b-accepted` |
| 34 | a data-bound control changed with tracking on (`w:customXmlInsRangeStart`, block-level revisions in `w:sdtContent`) | - |
| 36 | `w16du:dateUtc`, declared and undeclared | - |
| 54 | tracked writes of 43 cases (whole paragraphs deleted before tables and controls, a control's last paragraph, ranges across a control's edge, controls deleted whole; copied 2026-10-23 from `test/check54/` at core-ts 87bbb38; Word 365 16.0.20326.20232) | `-word365-accepted` (Reject All not saved) |
| 57 | whole equations deleted in Word's form, with fields (copied 2026-10-23 from `test/check57/` at core-ts 906d5e3; Word 365 16.0.20326.20232) | `-word365-accepted`, `-word365-rejected` |

## `editor/` - docx4j-ts-editor's fixtures

Copied 2026-10-16 from docx4j-ts-editor `packages/editor-model/test/fixtures/` at 3a515ef.

- `revisions/`: `revisions-base.docx` (the editor's engine-built base, its twelve sections' instructions in it),
  `revisions-word2010.docx` and `revisions-word15.docx` (Word 2010's and Word 15's edits of it); its `README.md`
  says what each section yields.
- `review/`: the editor's review session through Word 2010 and Word 15, each untouched, `-accepted` and
  `-rejected`; its `README.md`. The two notes referenced from inside an insertion and a deletion show that Word
  removes a note whose reference goes (CR-043 section 3.2). `review-for-word-word365-rejected.docx` is Word 365's
  Reject All of `review-for-word.docx` (Jason, 21 October 2026, for docx4j-python): it removes the comment on the
  inserted paragraph whose markers lie outside the `w:ins`, which Word 2010 and Word 15 kept (CR-043 section 15).

## `python/artefact8/` - docx4j-python's acceptance artefact 8

Copied 2026-10-21 from Jason's share (`__tmp/python-8/`). `8-tracked-changes.docx` is docx4j-python's tracker's
output before its fix 1 (a replace_text, a comment on its inserted half with every marker outside the `w:ins`, a
table, a deleted paragraph, formatting); `8-word-accepted.docx` and `8-word-rejected.docx` are Word 365's Accept All
and Reject All of it. Reject All removes the comment, and with the last comment the comments, commentsExtended and
commentsIds parts and `people.xml` (CR-043 section 15).

## `cr043/` - CR-043 phase 0's own probe

`cr043-base.docx` is built by `org.docx4j.model.revisions.Cr043ProbeBuilder` (docx4j-core-tests' test sources), one
case per paragraph or table, each tagged C01, C02, ...: the cases no fixture above holds (CR-043 section 4.2).
`cr043-probe.ps1` makes each case's tracked edit through Word's object model on the VM and saves Word's Accept All
and Reject All; `RUN.txt` is how it is run (`X:\revisions\cr043\` on the share). Word's outputs, from Jason's run of
2026-10-16 10:17 (Word 16.0.20326): `cr043-tracked.docx`, `cr043-accept-all.docx`, `cr043-reject-all.docx`,
`equations-accept-all.docx` and `equations-reject-all.docx` (Word's resolution of `../tracked-changes-equations.docx`,
CR-010's equations), and `cr043-log.txt` (Word's build, each case's outcome, the revisions left in each story). What
they show is CR-043 section 4.4.

`comments/`: CR-043's U21 to U23 (section 15), probes built by the docx4j-25 session (`build_round.py`, with CR-052's
Word check) and resolved by Jason in Word 365 on 21 October 2026. `c43-u21-accept-all-deletion-comment-outside.docx`
and its Accept All, `c43-u21-word365-accepted.docx`: a comment on a deletion, its markers outside the `w:del`, goes
whole. `c43-u22-reject-one-last-comment-revisions-left.docx` and `c43-u22-word365-rejected-one.docx`, its one
insertion rejected: the only comment goes with its parts, and `people.xml` stays while a deletion is left.
`c43-u23-reject-all-two-paragraph-insertion-commented.docx` and its Reject All, `c43-u23-word365-rejected.docx`: a
comment on two inserted paragraphs, its start in the first and its end in the second, goes whole.
