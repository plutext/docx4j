# The Word session of E4.g (ED-005 section 9.2)

One session on the reference machine, in Word 2010 and Word 15 (and current Word where it says so).
Nothing here is hand-written markup: the editor and the companion write the files, and Word writes
the saves. The saves come back into this folder, where step G5 reads them (`allFixtures` gains it
then) and holds the editor to them.

## Building the files

From the repository root, on the development machine, with Node 20 or later (the system Node 18
fails in Vite's build with "crypto.hash is not a function"):

```
export PATH=$HOME/.nvm/versions/node/v24.15.0/bin:$PATH
npm run word-session
```

It writes into `packages/editor-mcp/test-results/word-session/` (git ignores it):

| File | Built by | What it is |
|---|---|---|
| `review-for-word.docx` | `packages/editor-mcp/scripts/review-for-word.mjs` | A document made in a room by two people (Author A, Author B, through the editor's commands with tracking on) and the agent (Claude, through the companion's tools): every revision kind the editor writes, comments with a reply, a resolved thread and a deleted comment, and a style, a numbering format and the default tab stop changed through the parts panel |
| `review-for-word.txt` | the same | The changes and comments as the editor lists them, to compare with Word's Reviewing Pane |
| `measure-base.docx` | `../build/measure-base.mjs` | Five questions for Word, each with its instructions in blue italics |
| `ids/footnote-N.docx`, `ids/comment-N.docx` | `../build/ids-for-word.mjs` | Twelve files, one footnote or one comment each, its `w:id` set to N |

Copy the folder to the reference machine.

## 1. The editor's file, resolved in Word

In **Word 2010**, then in **Word 15** (and current Word if you like). In Word 15, do not convert the
document out of compatibility mode if it offers.

1. Open `review-for-word.docx`. If Word reports "problems with the contents", note its words and the
   part and position it names: that is the first finding.
2. Open the Reviewing Pane (Review > Reviewing Pane) and compare it with `review-for-word.txt`: how
   many insertions, deletions and formatting changes, by whom, and the five comments. Word counts
   and names things its own way (it may list a paragraph's properties as "Formatted: Centered", or a
   row as its cells), so note only what seems to be missing, extra or by the wrong author.
3. Optional (ED-005 section 6.3): a screenshot of the page in All Markup, to set beside the editor's
   drawing of the same changes.
4. Save it untouched as `review-for-word-word2010.docx` (in Word 15, `review-for-word-word15.docx`).
5. Review > Accept > Accept All Changes (in Document), and save as
   `review-for-word-word2010-accepted.docx` (`-word15-accepted.docx`).
6. Close it, open `review-for-word.docx` again, Review > Reject > Reject All Changes (in Document),
   and save as `review-for-word-word2010-rejected.docx` (`-word15-rejected.docx`).

Things the editor wrote that Word may treat differently, and which the accepted and rejected saves
will show: the link made over words with tracking on (section 9) is listed by the editor as a
formatting change (its character style), not as an insertion; the agent's comment in section 12 is
anchored on its whole insertion (ED-005 section 12.29 item 3).

## 2. The measurements

Open `measure-base.docx` and do what its sections say: sections 1 to 3 in **Word 15**, saved as
`measure-word15.docx`; then, from `measure-base.docx` again, sections 1 to 3 in **Word 2010**, saved
as `measure-word2010.docx`. Sections 4 and 5 are optional, in Word 15, and are screenshots.

| Section | The question | Left by |
|---|---|---|
| 1 | Where Word puts a comment's markers on one word of a pending insertion: inside the `w:ins` (the editor) or outside it (the engine) | ED-005 section 12.29 item 3 |
| 2 | Whether Ctrl+L with tracking off removes a direct `w:jc` when the style is left-aligned (the editor writes `w:jc="left"`) | ED-005 section 12.7 item 1, core-ts check 26 |
| 3 | New Comment at a caret in right-to-left text, and in an empty paragraph | ED-005 proposal 32 |
| 4 (optional) | No Markup over a deleted paragraph mark after a heading | ED-005 section 6.12 |
| 5 (optional) | Original over a formatting change | ED-005 section 6.12 |

## 3. Word 2010's id limit

In **Word 2010** only, open each file in `ids/` and note whether it opens without a problems or
recovery message, **with its footnote or comment still there** (the note's number in the text and
"The footnote." at the foot of the page; the comment "The comment." in the margin). No save is
needed. A recovery prompt, or a note or comment missing, counts as refused: note the message and
the part and position it names. Current Word is optional: the files are in its own mode
(`compatibilityMode` 15), not compatibility mode. The limit lies between the largest id that opens and the smallest that does not; if that
is not at a power of two, say so, and a second round of files between the two is made with
`node packages/editor-model/test/fixtures/build/ids-for-word.mjs <out-dir> <N> <N> …`.

| Id | Footnote opens? | Comment opens? |
|---|---|---|
| 32,767 | | |
| 32,768 | | |
| 65,535 | | |
| 65,536 | | |
| 16,777,216 (2^24) | | |
| 536,870,912 (2^29) | | |

## 4. Optional: core-ts check 30

core-ts's check 30 (a Script Lab snippet, docx4j-core-ts `test/README.md`, commit `e1d6f47`) can run
in the same session. It says where Word puts the `w:ins` in a content control and a smart tag, which
the companion's `trackInserted` followed until core-ts 0.3.1 retired it (ED-005 section 12.50).

## Where the saves go

Into this folder, under the names above, **with the `review-for-word.docx` the saves were made
from**: each build of it has other dates and ids, and step G5 compares Word's accepted and rejected
saves with the editor's own Accept all and Reject all over that same file. Then:
`review-for-word-word2010.docx`,
`review-for-word-word2010-accepted.docx`, `review-for-word-word2010-rejected.docx`, the three
`-word15` ones (and `-word365` ones if you ran current Word), `measure-word15.docx`,
`measure-word2010.docx`. The screenshots, the Reviewing Pane notes and the ids table go in a note
beside them (`session-notes.md`), or to me directly.
