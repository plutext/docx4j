# CR-068: manual redline to tracked changes, an explicit, opt-in conversion

Status: ACCEPTED 2026-10-31 (Jason: "CR-068 decisions as recommended; commence phase 0"): D1 to D12 as recommended
(§6); phase 0 DONE 2026-10-31 (§8: the probe, the predictions, Word's run the same day, every prediction held
but the dates, where Word shows `w:date` and ignores `w16du:dateUtc`; Jason's look by hand the same day, §8.6); D13 (the
separating space) and D9 revisited (delete a struck mark whose paragraph converts whole), proposed from phase 0,
ACCEPTED 2026-10-31 (Jason: "D13 and D9 as recommended", §8.5); D14 (the comments opt-in keeps its strike, §8.6)
proposed for phase 2; phase 1 DONE 2026-10-31 (§11: `ManualRedline`, its options and report,
`Texts.addresses`; held to Word 365's Accept All and Reject All of the phase 0 prediction and, §11.6, of docx4j's own
conversion), but for the Getting Started sentence (§11.5); phase 2 BUILT 2026-10-31 (Jason: "do phase 2"; §12:
the opt-ins, D14 to D16 taken as recommended), Word's third run pending.
Proposed the same day, drafted by Claude Fable 5.1 (session docx4j-12) at Jason's direction, relayed by the
portfolio session from its review of LlamaIndex's Document Parsing Protocol (the
paper's §7.13; the `dpp-forks` probe): "strikethrough handling is NOT view-sensitive (Accept All and Reject All leave
struck text; the file does not say it is a deletion), but I suspect we do need an explicit, opt-in conversion that
turns manual redline (strikethrough only by default) into tracked changes with an author such as 'manual redline'".
Phases in §5.

## 0. Summary

A "manual redline" is a document whose author struck the old words through and underlined (or coloured) the new
ones by hand, instead of turning on Track Changes. The file holds formatting, not revisions: Word's Accept All and
Reject All leave the struck text where it is, every text view docx4j has (accepted, original, markup; CR-046) shows
it, and a plain-text reader prints the old number against the new (`1.316 1.335`; in a cell with no space,
`1.3161.335`, a number the page never showed). That is right for an extraction, which reports what the document
says, and it is why the handling is not a view option (Jason, above).

This CR proposes the operation a reviewer runs on purpose: `ManualRedline.toTrackedChanges(pkg, options)` in
`org.docx4j.model.revisions`, beside CR-043's `TrackedChanges`. By default it turns every run struck through by its
own run properties into a `w:del` (its text as `w:delText`) under the author `manual redline`, and removes the
strike from the converted run, so that the deletion carries the meaning: Accept All then drops the old words,
Reject All restores them as plain text, and the markup view shows a deletion. Underlined runs to `w:ins`, runs of a
given colour to `w:ins`, and strike inherited from a style are opt-ins, off by default. It is idempotent, reports
what it converted and what it left, and can preview without changing anything. Word's Accept All and Reject All on
the converted file are the oracle, as for CR-043.

## 1. Background

- The portfolio's review of the Document Parsing Protocol (paper §7.13, 31 October 2026) ran eleven readers over
  `dpp-forks.docx`; its cases C03 (prose: struck `1.316`, a space, underlined `1.335`) and C04 (a cell, no space)
  are the "fabricated number": every reader but pandoc's and markitdown's Markdown (`~~1.316~~ 1.335`) prints both
  numbers as one text. docx4j's rows: plain text `1.316 1.335` and `1.3161.335`; Markdown `~~1.316~~ 1.335`; the
  block model's run has `isStrike()`; no inspection finding.
- The same review proposed an inspection kind for the text model (`text.formatting_join`, name open: a struck or
  super/subscripted run meeting its neighbour at a digit-to-digit boundary with no separator; UNSEEN under plain
  text, INFO under Markdown or the block model). If CR-050 takes it, that finding is what tells a reviewer to
  consider this conversion. This CR does not depend on it.
- Jason's direction (above) settles two things: no text view treats a strike as a deletion, and the conversion is
  explicit and opt-in, strikethrough only by default, with an author that says what it is.

## 2. What exists

- **CR-043** (`org.docx4j.model.revisions`, 17.3.3): `TrackedChanges.in`, `acceptAll`, `rejectAll`, filtered by
  `TrackedChangeFilter` (authors, types, kinds, dates, parts, a content control, a bookmark); `Story` enumerates the
  stories (main document, headers, footers, footnotes, endnotes, comments; text boxes in both `mc` branches;
  equations); `TrackedChangesReport` (counts by kind, author, part; `toJson`). Its rules for what accepting and
  rejecting do are §3.2 there, measured against Word 365. This CR writes the changes those read.
- **The text model** resolves a run's strike through its styles (`Extractor`, the `on(own, styles)` toggle fold)
  into `TextRun.isStrike()`; the markup view marks `w:ins` and `w:del` content; the three views are `TextView`
  ACCEPTED, ORIGINAL, MARKUP.
- **The schema classes**: `RunDel` and `RunIns` (`CTTrackChange`: `w:id`, `w:author`, `w:date`), `DelText`,
  `RunTrackChange`. Nothing in `model/revisions` allocates a revision id today; docx4j-diffx writes `w:ins`/`w:del`
  and is the precedent to read for id, author and date before phase 1.
- `w:strike` and `w:dstrike` are toggle properties (ECMA-376 §17.3.2.37, §17.3.2.9): a run's own `w:strike` with
  `w:val="0"` turns an inherited strike off; `w:del` is §17.13.5.14, `w:ins` §17.13.5.18, `w:delText` §17.3.3.7
  (clause numbers from the 5th edition; to be checked at phase 0).

## 3. Design

### 3.1 The API

```java
ManualRedlineReport r = ManualRedline.toTrackedChanges(pkg);                  // strike only, whole document
ManualRedlineReport r = ManualRedline.toTrackedChanges(pkg, new ManualRedlineOptions()
        .author("manual redline")          // default
        .date(Instant.now())               // default: the operation's time; date(null) writes none
        .styleStrike(true)                 // strike inherited from a style converts too; default false
        .underlineAsInsertion(true)        // direct w:u (not none) becomes w:ins; default false
        .colourAsInsertion("FF0000")       // runs of this colour (hex, or "auto" never) become w:ins; default none
        .comments(true)                    // the comments story too; default false
        .within(sdt) / .withinBookmark(name) / .parts(part...)   // a range, as TrackedChangeFilter's
        .auditProperty(true));             // a custom document property recording the run; default false
ManualRedlineReport r = ManualRedline.preview(pkg, options);                  // the report; nothing changed
```

`ManualRedlineReport`: how many deletions and insertions were made, per story and per kind (strike, double
strike, underline, colour), the addresses (the text model's, `body/12`, `header:rId8/2`), and what was left with
its reason (`in_tracked_change`, `field`, `style_strike`, `math`, `paragraph_mark`); `toJson()` and `toString()`
as `TrackedChangesReport`. `preview` returns the same report without touching the package.

### 3.2 What converts (the default)

A run (`w:r`) whose **own** `w:rPr` holds `w:strike` or `w:dstrike` on (absent `w:val`, `true`, `1`, `on`), in
any story but comments, in a paragraph, a table cell, a content control's content, a hyperlink, a text box (both
`mc` branches, kept twins as CR-043 keeps them), a footnote or endnote:

1. the run is wrapped in a `w:del` with a fresh `w:id` (above the document's largest annotation id), the author
   and the date; each `w:t` becomes `w:delText` (`xml:space` kept); `w:tab`, `w:br`, `w:sym`, `w:noBreakHyphen`
   and the rest stay in the run, as Word's own deletions hold them; a drawing or an object in a struck run stays in
   the run too (Word deletes it with the run);
2. `w:strike` and `w:dstrike` are removed from the run's properties (an empty `w:rPr` removed with them), so the
   deletion, not the formatting, carries the meaning: Reject All gives plain text, as the author would have had
   with Track Changes on (D6);
3. adjacent converted runs in one paragraph go under one `w:del` (D8), as Word groups a contiguous deletion; a run
   that is not converted between them ends the group.

**Left alone, and reported:** a run already inside `w:ins`, `w:del`, `w:moveFrom` or `w:moveTo` (reason
`in_tracked_change`; a struck insertion is somebody's later strike of tracked text, not a manual redline); the runs
of a field from `w:fldChar begin` to `end`, and a `w:fldSimple` (`field`: a struck result is regenerated on update,
and a struck instruction is not a deletion of text); `m:r` (`math`); a run whose strike comes only from a style, a
table style or the paragraph mark's style (`style_strike`, unless `styleStrike(true)`); a run whose own
`w:strike w:val="0"` turns an inherited strike off (nothing to do); a struck **paragraph mark** (`w:pPr/w:rPr/w:strike`),
left as it is in phase 1 and measured in the probe (D9: a wholly struck paragraph whose every run converted could
take `w:pPr/w:rPr/w:del`, which joins it to the next on accept; Word's own redline practice strikes text, not
marks, and a wrong join is worse than a surviving empty paragraph).

**Idempotent:** a second run converts nothing: the strike is gone from converted runs, and `w:del` content is left
alone. `w:trackRevisions` in settings is not touched (D12): converting is not turning tracking on.

### 3.3 The opt-ins

- **`styleStrike(true)`**: a run whose resolved strike (the text model's fold, `TextRun.isStrike()`) is on with no
  own `w:strike` converts too; the own property is then written as `w:strike w:val="0"` on the converted run, so
  that Reject All shows plain text here as well (D6 carried through).
- **`underlineAsInsertion(true)`**: a run whose own `w:u` is on (`w:val` other than `none`) is wrapped in `w:ins`
  (id, author, date) and the `w:u` removed (D7: Word draws an insertion underlined on its own; a kept underline
  would survive Accept All as formatting the author never meant). A run both struck and underlined is a deletion
  (strike wins; reported `strike_and_underline`).
- **`colourAsInsertion(hex...)`**: a run whose own `w:color` is one of the given values and which carries no other
  own mark (no strike, no underline, no highlight, no shading) is wrapped in `w:ins` and the colour removed (D7).
  The caller names the colour: no default, since red text is also just red text. A run coloured by a style only
  does not convert.
- **`comments(true)`**: the comments story is included; `within`, `withinBookmark`, `parts`: the range, with
  `TrackedChangeFilter`'s semantics (a run partly inside a bookmark counts as inside).
- **`auditProperty(true)`**: a custom document property `docx4j:manualRedline` whose value is the date and the
  options' `toString()`; off by default (D11: the author name is the record a reviewer sees in Word).

### 3.4 What the text model shows, before and after

Before: all three views show the struck text (and the underlined), and plain text prints `1.3161.335`. After the
default conversion: `accepted` drops the struck text (`1.335`), `original` shows it as plain text (`1.316`,
not struck), `markup` marks it deleted. That is the point: the document now says what the author meant, and every
reader of it, Tika and the ports included, follows without an option. The conversion is never an extraction
option and never a default (Jason).

### 3.5 Tests and the oracle

- **The probe fixture** `manual-redline.docx` (phase 0), made with docx4j like `dpp-forks.docx`, then Jason's Word
  run: M01 the C03 shape in prose; M02 the C04 shape in a cell, no separator; M03 a contract clause: a struck
  sentence and its underlined replacement; M04 a struck price in a table with the new price underlined beside it;
  M05 a character style carrying strike (not converted by default); M06 a run under that style with its own
  `w:strike w:val="0"`; M07 struck text inside a `w:ins`; M08 a struck field result (DATE); M09 a struck hyperlink;
  M10 struck text in a text box (both branches), a header and a footnote; M11 a wholly struck paragraph with its
  mark struck (D9); M12 a red run beside black text (the colour opt-in); M13 `w:dstrike`; M14 two adjacent struck
  runs with different sizes (one `w:del`); M15 a struck run holding a tab and a break; M16 struck text in a
  comment (not converted by default).
- **Predictions before the run**, as CR-043 did: the converted file opens in Word 365 without repair; Review shows
  the changes under `manual redline`; Accept All leaves the text the author meant (M01 `Rate: 1.335 per cent.`;
  M02 `1.335`); Reject All leaves the old text plain, not struck; `TrackedChanges.acceptAll`/`rejectAll` (CR-043)
  give the same text as Word's, measured with `Texts.of` under the three views before and after; M05 to M08, M11
  and M16 unchanged by the default; the second run changes nothing.
- **Tests** in docx4j-core-tests (`org.docx4j.model.revisions`): the probe through the default and each opt-in, the
  report's counts and reasons, idempotence, the three views before and after, the ids unique and above the existing
  ones, the schema (a `w:del` directly in `w:p`, `w:hyperlink`, `w:sdtContent`, `w:smartTag` only), Word's resolved
  texts from the phase 0 run as goldens.

## 4. Risks

- **A strike that is not a redline.** "Was $40" in a price list, a struck item in a to-do list, a joke. The
  conversion is opt-in and whole-document by default; `preview` and the range options are the reviewer's tools, and
  the author name keeps the conversion reversible in Word (Reject All by author, CR-043's filter).
- **A wholly struck paragraph** (D9): leaving the mark gives an empty paragraph after Accept All; deleting the mark
  joins paragraphs. Measured in the probe before a rule is written.
- **Struck table rows** (every cell struck): Word's deleted-row mark (`w:trPr/w:del`) is not attempted here; the
  cells' runs convert, the row survives Accept All empty. Out of scope for phase 1, named in §7.
- **Ids.** Word wants `w:id` unique among a document's annotations (changes, comments, bookmarks share the space
  in practice); phase 1 allocates above the largest id in every story and the comments part.
- **Fields** and **hyperlinks**: a `w:del` inside `w:hyperlink` is valid and Word writes it; a deletion spanning a
  field's result is not attempted (the field is left alone and reported).

## 5. Phases

| Phase | What | Size |
|---|---|---|
| 0 | The probe fixture (§3.5), the predictions written into §8, Jason's Word run (open, Review pane, Accept All, Reject All, Save As Text of each), the results recorded | ½ day + Jason's run |
| 1 | `ManualRedline.toTrackedChanges` and `preview`, the default scope (direct strike and double strike, every story but comments), the report, id allocation, idempotence; tests against the phase 0 goldens; CHANGELOG; a Getting Started sentence in the tracked-changes chapter | 1 to 2 days |
| 2 | The opt-ins: style strike, underline and colour as insertions, comments, the range, the audit property; tests | 1 day |
| 3 | Hand-offs: the ports (core-ts, python; as CR-043 was ported, the probe is their oracle), docx4j-mcp (a tool; the portfolio adds the verb to `mcp_tool_vocabulary`), the paper's §7.13 pointer; the WD takes no requirement (the conversion is not extraction) | ½ day |

## 6. Decisions

D1 to D12 accepted as recommended, 2026-10-31 (Jason); from phase 0, D13 and D9 revisited accepted as recommended the same day (Jason), D4 clarified, D14 proposed (§8.5, §8.6).

| # | Decision | Recommendation (accepted) |
|---|---|---|
| D1 | Name and place | `ManualRedline.toTrackedChanges(pkg[, options])` and `preview`, in `org.docx4j.model.revisions`, beside `TrackedChanges`; `ManualRedlineOptions`, `ManualRedlineReport` |
| D2 | Scope by default | The whole document: body, tables, headers and footers, footnotes and endnotes, text boxes (both `mc` branches), content controls; not comments (opt-in) |
| D3 | Author | `manual redline` by default, configurable |
| D4 | Date | The operation's time (UTC, to the second) by default; a fixed `Instant` for reproducible output; `null` writes no date |
| D5 | What converts by default | A run with its own `w:strike` or `w:dstrike` on; style-derived strike only under `styleStrike(true)` |
| D6 | The mark after conversion | Removed from the converted run (strike, and for the opt-ins the underline or colour), so the tracked change alone carries the meaning and Reject All gives plain text; under `styleStrike`, `w:strike w:val="0"` written |
| D7 | Underline and colour as insertions | Opt-ins, off by default; the colour named by the caller, never guessed; strike wins over underline on one run |
| D8 | Grouping | Adjacent converted runs in a paragraph under one `w:del` (one `w:ins`), as Word groups |
| D9 | A struck paragraph mark | Left alone in phase 1 and measured in the probe (M11); a rule only if Word's behaviour on the converted file asks for one. **Revisited at phase 0 and accepted (§8.5)**: the mark is deleted too when it is struck and the paragraph converts whole |
| D10 | Fields, math, tracked content | Left alone and reported (`field`, `math`, `in_tracked_change`); a struck hyperlink converts inside the hyperlink |
| D11 | Audit | The author name is the record; a custom property `docx4j:manualRedline` only under `auditProperty(true)` |
| D12 | Settings | `w:trackRevisions` untouched; converting is not turning tracking on |
| D13 | The separating space (proposed at phase 0, §8.3, §8.5; accepted 2026-10-31) | One space taken into the deletion where the converted text has whitespace on both sides (else the preceding space before punctuation or the paragraph end), reported `separator_space`; `separatorSpace(false)` turns it off |

## 7. Out of scope

Detecting a redline (a heuristic that decides a document is one): the reviewer decides, the text model's finding
(if CR-050 takes it) points. Deleted-row and deleted-cell marks. Moves (`w:moveFrom`/`w:moveTo`) from struck and
re-inserted text. Property changes (`w:rPrChange`). Turning Track Changes on. Any change to extraction defaults or
views (Jason: not view-sensitive).

## 8. Phase 0 record

### 8.1 The probe (built 2026-10-31)

In `docx4j-core-tests/src/test/resources/revisions/cr068/` and on the share at `X:\revisions\cr068\` with its
`RUN.txt`; built by `org.docx4j.model.revisions.ManualRedlineProbeBuilder` (test sources), byte for byte the same
on a rebuild (every entry dated 1980). Every text is ASCII, so Word's Save As Plain Text cannot meet its code-page
defect (the portfolio's `docs/word_defects.md`, WD-001).

- **`manual-redline.docx`**, the base: M01 to M16 as §3.5 lists them, with three departures. M04's table also has a
  row struck whole (`Gizmo`, `$99.00`), the §4 risk. M08 is a QUOTE field (`Version 2`), every run of it struck,
  not a DATE: a DATE result changes when Word updates it, and the case is the struck field, not the date; M08b
  adds a struck run inside a `w:fldSimple`. **M17** is new: an inline content control holding a struck run and its
  underlined replacement (D2 names content controls; §3.5 had no case for them). The base's own annotations: the
  comment (id 0) and M07's insertion (id 100, author `CR-068 base`).
- **The conversions, as predicted**, written by hand by the same builder (phase 1 does not exist yet), each
  deletion and insertion by `manual redline`, ids from 101 in document order, each text box branch with its own
  ids (as Word's own: `cr043-tracked.docx` C22 has 50 and 51 in the Choice, 52 and 53 in the Fallback), the date
  as §8.3 F1 says: `manual-redline-default.docx` (D2, D5, D6, D8, D9, D10), `manual-redline-optins.docx` (every
  opt-in of §3.3 at once: style strike, underline and `FF0000` as insertions, the comments story) and
  `manual-redline-d9.docx` (the default with M11's struck mark deleted too: `w:pPr/w:rPr/w:del`, its strike
  removed; D9's alternative).
- **`manual-redline-dates.docx`**: four deletions differing only in their date attributes (§8.3 F1).
- **`cr068-probe.ps1`** (method 2 of the portfolio's `docs/office_output_methods.md`, as CR-043's): first Word
  makes its own tracked deletion of the texts the default conversion deletes, in the base (M11's paragraph whole,
  its mark with it), saved as `manual-redline-word.docx`, for the shape Word gives a deletion; then for the base,
  the three conversions and Word's own, it lists every revision story by story (type, author, date, text: the
  Review pane's content), saves the file as plain text as it is, and saves Word's Accept All and Reject All of it
  as docx and as plain text (`word-<key>-asis.txt`, `word-<key>-accept-all.docx/.txt`,
  `word-<key>-reject-all.docx/.txt`); last it lists the dates probe's revisions. Jason then opens the default and
  opt-ins files by hand for the repair prompt and the Review pane.

### 8.2 Predictions (written before Word's run)

From docx4j's own reading of the predicted files (CR-043's `TrackedChanges.acceptAll` and `rejectAll`, then the
text model's accepted view; the three views before), which is what Word is held against:

1. **Opening.** Word opens the default, opt-ins and D9 files without a repair prompt. Its Review pane lists the
   changes under `manual redline` (and M07's insertion under `CR-068 base`): default 13 deletions in the main
   story, 1 in the text box, 1 in the footnote, 1 in the header; opt-ins those plus M05's deletion and 7
   insertions in the main story (M01, M02, M03, M04, M12, M13, M17), 1 insertion each in the text box and the
   footnote, 1 deletion in the comment; D9 the default's plus M11's mark (or one fewer if Word reports M11's text
   and mark as one revision). The dates are 31 October 2026 11:00 on the VM (Sydney, +11:00).
2. **Accept All, default** (docx4j's text):

   | Case | Text after Accept All |
   |---|---|
   | M01 | `M01 Rate:  1.335 per cent.` (two spaces: §8.3 F2) |
   | M02 | `1.335` |
   | M03 | `... of the Order Date. Late delivery incurs a penalty of 2 per cent a week.` |
   | M04 | ` $35.00` (a leading space); the Gizmo row survives, its two cells empty (§4) |
   | M05 to M08b | unchanged (style strike, strike off, tracked content, fields: left alone); M07's insertion accepted |
   | M09 | `M09 See  example.com/new-terms for the terms.` (the first hyperlink emptied; two spaces) |
   | M10 | text box `the price is  $35.00.`, footnote `the rate is  1.335.`, header `M10 header: Final version` |
   | M11 | an empty paragraph where the struck one was (its mark not deleted, D9) |
   | M12 | unchanged (red text) |
   | M13 | `M13 Fee:  EUR 450 a month.` |
   | M14 | `M14 kept words, end.` (the spaces were inside the struck runs) |
   | M15 | `M15 Before after.` (the tab and the break deleted with their run) |
   | M16 | unchanged (the comment is not converted by default) |
   | M17 | `M17 Party:  Acme Holdings Pty Ltd.` (inside the content control) |

3. **Reject All, default**: the base's text, M07's insertion rejected (`M07  after the insertion.`), every
   converted run plain, not struck (D6): Word's reject-all docx has no `w:strike` or `w:dstrike` on M01, M02,
   M03, M04, M09, M10, M13, M14, M15 and M17's old text; M11's mark keeps its strike (only the runs converted).
4. **Opt-ins**: Accept All as the default but M05's style-struck words gone (`M05  then plain words.`), the
   underlined and red insertions kept and no longer underlined or red, the comment `M16 comment: kept remark.`;
   Reject All as the default's but the insertions gone (`M01 Rate: 1.316  per cent.`, `M12 The party shall notify
   the other.`) and M05 plain (its `w:strike w:val="0"` over the style).
5. **D9**: Accept All joins M11's emptied paragraph to the next (`M11 after: ...`, no empty paragraph); Reject All
   as the default's.
6. **Word's own deletions** (`manual-redline-word.docx`): the deleted runs keep their `w:strike` (Word tracks the
   deletion and leaves the formatting), so Word's Reject All gives the struck text back, where the conversion's
   gives plain text (D6 is a choice, not Word's own behaviour); M14's two adjacent struck runs in one `w:del` (D8;
   Word-made fixtures in `revisions/core-ts/` hold multi-run deletions); M15's tab and break inside the deleted
   run; M11's mark deleted with its text (`w:pPr/w:rPr/w:del`), so that Word's own Accept All joins it as the D9
   variant does.
7. **Dates**: Word shows D1 (w:date `00:00Z` alone) as 31 October 00:00, D2 (dateUtc alone) as 11:00, D3 (both,
   disagreeing) as 11:00 (dateUtc wins), D4 (as the conversions write them) as 11:00. That is docx4j's reading
   (`TrackedChange.getDate`, CR-067 D12) of the same file.
8. **Plain text as it is** (a file with tracked changes, unresolved): not predicted; recorded.

### 8.3 Findings before the run

- **F1, D4's date as written.** Word 365 writes `w:date` as its machine's wall clock with a `Z`, and the instant
  in `w16du:dateUtc` (`cr043-tracked.docx`: `w:date="2026-10-16T10:17:00Z"`, `dateUtc="2026-10-15T23:17:00Z"`);
  docx4j reads them the same way (`TrackedChange.getDate`: dateUtc where present, else `w:date` as local time in
  the package's zone). A UTC instant written into `w:date` alone would show 11 hours early on the VM. So D4's
  "the operation's time (UTC, to the second)" is written as Word writes it: `w16du:dateUtc` the instant,
  `w:date` its wall clock in `OpcPackage.effectiveDateTimeZone()`, `w16du` declared and ignorable. The dates probe
  (§8.2 item 7) checks that Word prefers dateUtc.
- **F2, the separating space (D13 below).** A struck value between spaces, as manual redlines are typed
  (`Rate: ~~1.316~~ 1.335`), leaves two spaces after Accept All (M01, M04, M09, M10, M13, M17): the space the
  author typed to separate the old value from the new separates nothing once the old one is gone, and it shows
  on the page. §3.5 predicted `Rate: 1.335 per cent.` without noticing. Word's own editing does not leave it
  (selecting a word selects its trailing space; smart cut and paste adjusts the spacing).
- **F3, a text-model aside, not this CR's.** docx4j's markup view does not mark a deletion inside a comment
  (opt-ins M16: `{>>M16 comment: withdrawn remark, kept remark.<<}`), where the accepted and original views
  resolve it. Noted for CR-046.
- **F4, the clause numbers**, checked against ECMA-376 Part 1, 5th edition (December 2016): `del` (deleted run
  content) §17.13.5.14, `ins` (inserted run content) §17.13.5.18, `delText` §17.3.3.7, `strike` §17.3.2.37,
  `dstrike` §17.3.2.9; a paragraph mark's `del` is §17.13.5.15 (deleted paragraph).

**D13 (for Jason): the separating space.** (a) Leave it: the conversion converts the strike and nothing else,
and Accept All leaves two spaces. (b) Take one space into the deletion where the converted text has whitespace on
both sides (the following space; where the deletion ends the paragraph or meets punctuation, the preceding one),
splitting the run that holds it, reported as `separator_space`; Accept All then gives `Rate: 1.335 per cent.` and
Reject All still gives `1.316 1.335`. (c) (b) behind an option. Recommendation: (b) by default, with
`separatorSpace(false)` to turn it off: the conversion's point is the text the author meant, and the double space
is an artefact of redlining by hand, not something the author wrote; Word's own deletion of a word takes its space.
No Word measurement is needed (a deleted space accepted is gone); phase 1's goldens follow the ruling. The
opt-ins (phase 2) meet the same space from the other side: between a deletion and the insertion that replaces it,
the separator belongs to neither outcome (opt-ins Reject All today: `Rate: 1.316  per cent.`), and only a space
both inserted and deleted (`w:ins` holding a `w:del`, as Word writes another author's deletion of an insertion)
drops out of both; phase 2 measures that before writing it.

### 8.4 Word's run (2026-10-31, Word 365 16.0.20326 on the VM)

Jason ran `cr068-probe.ps1` at 13:37 (the portfolio session relayed "cr068 done"): the thirteen tracked deletions of
Word's own pass made, every input opened, every Accept All and Reject All leaving 0 revisions, every save written.
The outputs and `cr068-log.txt` are in `docx4j-core-tests/src/test/resources/revisions/cr068/` beside the inputs.
Jason opened the default and opt-ins files by hand afterwards (14:33; his screenshots `cr068-default.PNG` and
`cr068-optins.png` beside the outputs, All Markup): §8.6.

**As predicted (§8.2 items 1 to 6):**

- **Revisions** (item 1): default 17 (main story 13 by `manual redline` + M07's by `CR-068 base`, footnote 1, text
  box 1, header 1), opt-ins 28 (the comment's deletion in story 4), D9 17 (M11's text and mark listed as one
  revision, its text ending in the mark), all `manual redline` at 31 October 2026 11:00.
- **Texts** (items 2 to 5): Word's Accept All and Reject All of the base, the three conversions and Word's own,
  read by the text model (accepted view, notes, comments, headers and footers), equal docx4j's
  `TrackedChanges.acceptAll` and `rejectAll` of the same input read the same way: 10 of 10 (Word's own differs
  only by an empty default footer Word's first pass added and its next save dropped, a harness artefact). Word's
  plain-text saves agree case by case with item 2's table, the double spaces of F2 included: `M01 Rate:  1.335 per
  cent.`, `M09 See  example.com/new-terms`, `M13 Fee:  EUR 450`, `M17 Party:  Acme Holdings Pty Ltd.`; the Gizmo
  row survives with its cells empty; M11 leaves an empty paragraph under the default and joins under D9; M15's tab
  and break go with their run. Word's text save of a tracked file as it is gives its Accept All text.
- **D6** (items 3, 4): Word's Reject All of the three conversions leaves every converted run plain (no `w:strike`
  or `w:dstrike`), and M05 under the opt-ins plain over its style.
- **Word's own deletions** (item 6, `manual-redline-word.docx`): every deleted run keeps its `w:strike` or
  `w:dstrike`, so Word's own Reject All gives the struck text back (struck, in its reject-all docx); D8 holds: M14's
  two adjacent runs in one `w:del`, M15's run split by Word at its tab and break into three runs inside one
  `w:del`, M01's struck run and the space after it in one; M11's mark deleted with its text
  (`w:pPr/w:rPr/w:del`, its own id, the strike kept on mark and run), and Word's Accept All of it joins M11 to the
  next paragraph, as the D9 variant does. Word wrote the old hyperlink as a HYPERLINK field
  (`fldChar`/`instrText`) once it held a deletion; the deletion sits in the field's result.

**Not as predicted:**

- **The dates** (item 7): D1 (`w:date` 00:00Z alone) 31 October 00:00, as predicted; **D2** (`w16du:dateUtc`
  alone) no date at all (the object model's 1899-11-30, its empty date), not 11:00; **D3** (both, disagreeing)
  00:00, `w:date`'s wall clock, not `dateUtc`'s 11:00; D4 (as the conversions write them) 11:00. Word 365's
  object model (`Revision.Date`) shows `w:date` as local wall-clock time and ignores `dateUtc`. For the
  conversion F1 stands (write both, the same instant), with the emphasis moved: `w:date` is the one Word shows and
  must always be written; `dateUtc` is written because Word writes it. For reading, docx4j's
  `TrackedChange.getDate` (and core-ts's and python's, the portfolio's date rule: `dateUtc` where present,
  Jason's decision of 2026-09-28 for core-ts, not a measurement of Word) disagrees with Word wherever the two
  attributes disagree, which no Word-made file does; reported to the portfolio session, which owns that rule
  (`docs/date_handling_across_ports.md`); nothing in CR-068 changes. Office JS's reading (the add-in's
  `TrackedChange.date`) was not measured here.

**New evidence on the open questions:**

- **F2 and D13, the separating space.** Word's own deletion through the object model (`Range.Delete` of exactly the
  struck text) took the space after it into the deletion, as a separate run in the same `w:del`, in M01, M04,
  M13 and M17 (the main story: prose, a cell, a content control; the log lists `1.316 `, `$40.00 `, `EUR 500 `,
  `Acme Pty Ltd `), so Word's own Accept All gives `M01 Rate: 1.335 per cent.`; it did not in the footnote and
  the text box (`1.316`, `$40.00`, the same shape as M01), nor after the old hyperlink (the space follows the
  field's end), nor in M02 (no space). D13 (b) is what Word's own editing does in the main story; the
  recommendation stays (b), applied in every story alike (the author's meaning does not change with the story),
  Word's footnote and text-box behaviour noted.
- **D9, the struck paragraph mark.** Word's own deletion of a wholly struck paragraph deletes its mark too, and
  both Word's Accept All of that and of the D9 variant join the emptied paragraph cleanly to the next, which keeps
  its own text and properties; the default's Accept All leaves an empty paragraph behind. §8.5 asks to revisit D9.

### 8.5 Decisions from phase 0

D13 and D9 (revisited) accepted as recommended, 2026-10-31 (Jason: "D13 and D9 as recommended"); D4 as clarified.

| # | Decision | Recommendation |
|---|---|---|
| D13 | The separating space (F2; Word's own evidence in §8.4) | (b): one space taken into the deletion where the converted text has whitespace on both sides (the following space; where the deletion ends the paragraph or meets punctuation, the preceding one), in every story, reported `separator_space`; `separatorSpace(false)` turns it off. The opt-ins' space between a deletion and its insertion goes to phase 2 (§8.3) |
| D9 (revisited) | A struck paragraph mark | Delete the mark too (`w:pPr/w:rPr/w:del`, its strike removed) when the paragraph's own mark is struck and every run of the paragraph converts, as Word's own deletion of the paragraph does and as the D9 variant measured; otherwise leave it (a struck mark under unconverted text, a mark Word will not delete: a cell's last paragraph, one before a table, CR-043 §4.4), reported `paragraph_mark` |
| D4 (clarified) | The date as written | `w:date` the operation's instant as wall-clock time in `OpcPackage.effectiveDateTimeZone()` with a `Z` (the one Word shows), and `w16du:dateUtc` the instant (as Word writes it), `w16du` made ignorable on the part's root; `date(null)` writes neither |

### 8.6 By hand, and phase 0's status

Both files open in Word without a repair prompt (none reported). Under All Markup every conversion is drawn as
Word draws a tracked change by one author: deletions in red strikethrough, the opt-ins' insertions in red underline
(M12's `promptly`, its own red removed, now red as an insertion), change bars beside each; the cases the
conversion leaves alone show as formatting (the default's M05 and M16, M08 and M08b in black strikethrough, M12 in
its own red); M07's insertion by `CR-068 base` in a second author's colour; the header's deletion dimmed with the
header; M14's larger second run, M15's tab and break inside the deletion. As predicted.

One thing the object model could not show: **the comment card.** Word 365's comment card shows the comment's text
without tracked-change marks. In the default it shows `withdrawn remark,` struck (the run's own strike, not
converted); in the opt-ins it shows `withdrawn remark, kept remark.` plain, the deletion invisible, because D6 took
the strike away (Word's text saves still resolve it: Accept All `M16 comment: kept remark.`). So under
`comments(true)` a reviewer looking at the card sees the withdrawn remark as kept until Accept All. D14 below.

| # | Decision | Recommendation |
|---|---|---|
| D14 | The comments story under `comments(true)` (phase 2) | Keep the strike on a converted run in a comment (D6 not applied there), so that Word's comment card, which shows no tracked-change marks, still shows the text struck; Reject All then gives the base back as it was; to be confirmed by Word in phase 2 |

**Phase 0: DONE 2026-10-31.** Phase 1 waits on D13 and D9 (revisited); its goldens are Word's saves here, read by
the text model, and Word's reject-all docx for D6.

## 9. Hand-offs

- **The portfolio**: the registry entry (`docx4j/CR-068`, in progress since acceptance, portfolio 07ed846); the
  paper's §7.13 may cite this CR; the `mcp_tool_vocabulary` verb `manual_redline_to_tracked_changes` (portfolio
  3e58a4e, §5 there: parameters after the Java options, `dry_run: true` = `preview`, the report as the change
  report, no `view` parameter).
- **docx4j-mcp, core-ts, python**: at phase 3; nothing until then.

## 10. Sources

- The paper, `../docx4j-portfolio/docs/text_extraction_behaviour_model.md` §7.13 (31 October 2026; docx4j-portfolio
  commit 09502ce, with WD 0.43 and 0.44), and the `dpp-forks.docx` probe (Office 2016/dpp-forks/ on the share);
  docx4j's rows and views in that section.
- CR-043 (`CR-043-tracked-changes-accept-reject-api.md`) §3.1 (the API), §3.2 (what accept and reject do), §3.5
  (tests and the oracle), §4 (the fixtures and the probe).
- CR-046 (the text model's three views), CR-050 (the inspection; the proposed `text.formatting_join` kind).
- ECMA-376 Part 1, 5th edition (December 2016), §17.13.5.14 (`del`), §17.13.5.15 (a paragraph's `del`),
  §17.13.5.18 (`ins`), §17.3.3.7 (`delText`), §17.3.2.37 (`strike`), §17.3.2.9 (`dstrike`); checked at phase 0.

## 11. Phase 1 (2026-10-31)

### 11.1 What was built

- **`ManualRedline`** (`org.docx4j.model.revisions`): `toTrackedChanges(pkg[, options])` and `preview(pkg[, options])`
  (D1); `preview` converts a clone of the package (`OpcPackage.clone()`, its zone carried) and returns that report.
- **`ManualRedlineOptions`**: `author` (D3), `date` (D4; `date(null)` writes none), `separatorSpace` (D13, on). The
  opt-ins of §3.3 come with phase 2.
- **`ManualRedlineReport`**: one `Change` per thing deleted (kind `strike`, `double_strike`, `separator_space`,
  `paragraph_mark`; part, address, text, runs, whether it is a deletion of its own), one `Left` per struck run or
  mark left (reason `in_tracked_change`, `field`, `math`, `style_strike`, `paragraph_mark`, `comment`; consecutive
  runs of one paragraph and reason one entry, so that a field is one), counts by kind, part and reason,
  `getDeletions()` (each `w:del` and each mark; a separating space that went into its struck text's `w:del` is not
  another), `toJson()`.
- **`Texts.addresses(pkg, options)`** (`org.docx4j.model.text`, public): what the text model reads, mapped by
  identity to its block's address; the report's addresses, read before the conversion. A text box's paragraph has
  its own (`body/11/textbox:0/0`); a twin branch's, which the text model does not read, takes its outer paragraph's.

### 11.2 The rules as built

- **Stories** (D2): Lister's (CR-043): the main document, footnotes, endnotes, headers and footers, each text box
  in every `mc:AlternateContent` branch (the first branch holding boxes reported, the others converted as twins,
  with their own ids, as Word's are), block- and paragraph-level `mc:AlternateContent` likewise (the branch the text
  model reads reported); the comments walked and their struck runs reported (`comment`).
- **Left** (D10): a run in `w:ins`, `w:del`, `w:moveFrom`, `w:moveTo`, or whose strike is itself a tracked
  formatting change (`w:rPrChange`), `in_tracked_change`; a complex field's runs, `begin` to `end`, the depth
  carried across the story's paragraphs, and a `w:fldSimple`'s, `field`; math, `math`; a run with no strike of its
  own that its paragraph and character styles strike (resolved by `PropertyResolver`: the defaults, the paragraph
  style, the run style; a table style is not consulted), `style_strike`. A struck run holding nothing to delete
  (only a comment reference, a note's own mark, a separator, a rendering hint) is not converted and not reported.
- **Grouping** (D8): adjacent converted runs of one content list in one `w:del`; a `w:proofErr` between them does not
  part them; anything else does, a bookmark or a comment range among them, so that no bookmark is pulled into a
  deletion (CR-043 §4.4: one with an end in removed content goes whole on accept).
- **The separating space** (D13): where the struck text neither starts nor ends with whitespace, (a) with
  whitespace or the paragraph's start before it and a space after it, that space; (b) otherwise, where it ends the
  paragraph or meets punctuation (`.,;:!?)]}` and closing quotes) with a space before it, that space; taken only
  from a plain run (not tracked, not a field's), split off with the run's formatting, into the struck text's `w:del`
  where the run is beside it (as Word's own: §8.4), else a `w:del` of its own (M09: the space after the hyperlink).
- **The paragraph mark** (D9 as revisited): a struck mark (its own `w:pPr/w:rPr`), no text left shown in the
  paragraph after the conversion, the next item of its container a `w:p`, no `w:sectPr` in the paragraph: the mark
  takes `w:del` and loses its strike; otherwise, where the text converted whole, it is left and reported
  (`paragraph_mark`): a cell's last paragraph, one before a table or a control, the last of its story, a section's.
- **Dates and ids** (D4 as clarified, §8.5): `w:date` the instant's wall clock in
  `OpcPackage.effectiveDateTimeZone()` with a `Z`, `w16du:dateUtc` the instant, both to the second, and `w16du`
  added to each changed part's `mc:Ignorable` (`McSplice.addIgnorable`; the marshal declares it); ids from one
  above the largest annotation id in the story parts (every `CTMarkup`: tracked changes, comments, bookmarks and
  ranges; the comment ranges and references).
- **Idempotent**: a converted run has no strike, a deleted mark no strike, so a second conversion finds nothing.

### 11.3 On the probe

`toTrackedChanges` of `manual-redline.docx` (date 2026-10-31T00:00:00Z, Sydney): 18 deletions (16 `w:del` of struck
runs, M09's separating space, M11's mark), 17 runs, 7 separating spaces (6 into their struck text's `w:del`); left:
M05 `style_strike`, M07 `in_tracked_change`, M08 and M08b `field`, M16 `comment`. Accept All: `M01 Rate: 1.335 per
cent.`, `Widget	$35.00`, `M09 See example.com/new-terms for the terms.`, `M13 Fee: EUR 450 a month.`, `M17 Party:
Acme Holdings Pty Ltd.`, the footnote and text box alike, M11 joined to the next paragraph; no two spaces anywhere.
Reject All: the base's text, every converted run plain. The XML holds a deletion as Word's own does (M01: the struck
run and the space in one `w:del`). Saved as `manual-redline-phase1.docx` (the builder writes it, byte for byte the
same on a rebuild) for Word's second run (§11.5).

### 11.4 Tests

`ManualRedlineTest` (docx4j-core-tests, 13 tests). The measured one: with `separatorSpace(false)` the conversion of
the base is the D9 prediction Word resolved in phase 0, and docx4j's Accept All and Reject All of it describe
(`DocumentDescription`, CR-043's structural comparison: text, formatting, marks, tables, notes, text boxes) exactly
as Word's `word-d9-accept-all.docx` and `word-d9-reject-all.docx` do. With the default, D13's texts on the probe,
Reject All as Word's; the three views equal Accept All and Reject All; the report; idempotence; `preview` changes
nothing and reports what the conversion does; ids unique and above the base's; every `w:del` in a paragraph,
hyperlink or inline control's content; the dates in Sydney, in UTC and none, the author; `w16du` made ignorable and
declared on a document that had no `mc:Ignorable`; the separating space's cases (punctuation, the paragraph's
start and end, two struck words apart, digits meeting, a space inside the strike, off, a space in an insertion not
taken); left alone (a tracked strike, a struck comment reference); the paragraph marks (joined; before a table, a
cell's last, left and reported; not whole, kept unreported).

### 11.5 Open

- **The gate**: docx4j-core-tests 2278 run, 0 failures, 30 skipped (clean install of docx4j-core and core-tests).
- **The Getting Started sentence** (§5's phase 1 row): the docx is under Jason's review (uncommitted in the tree;
  it is canonical, the md and html derived), so the sentence waits for his review to land.

### 11.6 Word's second run (2026-10-31, Word 365 16.0.20326 on the VM)

Jason ran `cr068-probe.ps1` again at 15:22 ("cr068 phase1 done"); it took `manual-redline-phase1.docx`, docx4j's own
conversion, and skipped what the first run had made. Word opened it and listed 18 revisions, all `manual redline`
at 11:00 but M07's: the main story's 15 (the separating spaces inside their struck text's revisions, `1.316 `,
`$40.00 `, `EUR 500 `, `Acme Pty Ltd `, as Word's own deletion holds them; M09's space a revision of its own; M11's
text and mark one), the footnote's (`1.316 `), the text box's (`$40.00 `), the header's. Accept All and Reject All
left none. Word's Accept All text has no two spaces anywhere (`M01 Rate: 1.335 per cent.`, `M09 See
example.com/new-terms for the terms.`, `M13 Fee: EUR 450 a month.`, `M17 Party: Acme Holdings Pty Ltd.`, the footnote
and text box alike), M11 joined to the next paragraph; its Reject All is the base's text. The dates probe, read
again, gave the same four dates as the first run.

The goldens `word-phase1-accept-all.docx` and `word-phase1-reject-all.docx` (with their text saves) are in
`revisions/cr068/`, and `ManualRedlineTest.asWordResolvedTheConversion` holds docx4j to them: its conversion of the
base, accepted and rejected by docx4j, describes as Word's (`DocumentDescription`); `thePhase1FileIsTheConversion`
holds the file Word was given to what the conversion writes (its story parts as docx4j reads them; the bytes depend
on the marshal's formatting properties, which the builder's classpath and the tests' set differently). 15 tests,
green in Maven too.

**Phase 1: DONE**, but for the Getting Started sentence (§11.5).

## 12. Phase 2 (2026-10-31; Jason: "do phase 2")

### 12.1 What was built

- **`ManualRedlineOptions`**: `styleStrike`, `underlineAsInsertion`, `colourAsInsertion(hex...)`, `comments`,
  `within(control)`, `withinBookmark(name)`, `parts(part...)`, `auditProperty` (§3.3), each off by default.
- **`ManualRedlineReport`**: the kinds `style_strike`, `underline`, `colour`; each change's `type` (`deletion`,
  `insertion`, or `both`, the space between a deletion and its insertion) and whether it is a change of its own;
  `getInsertions()`; `getStrikeAndUnderline()`; in `toJson()`, `strikeAndUnderline` and each change's `type` and
  `own`.
- **`preview`** is now a dry run of the same walk, every decision made and nothing written, instead of phase 1's
  conversion of a clone (`OpcPackage.clone()` saves and reloads, so a range given as a content control of the
  package would not be found in the clone). The tests hold the preview's report to the conversion's.

### 12.2 The rules as built

- **Style strike**: a run with no strike of its own whose styles strike it (§11.2's resolution) converts, kind
  `style_strike`, and is given its own `w:strike w:val="0"` (or `w:dstrike`, as the style set), so that Reject All
  shows it plain (D6 carried through, §3.3).
- **Underline**: a run whose own `w:u` names a pattern other than `none` becomes a `w:ins`, the `w:u` removed (D7).
  A `w:u` with no pattern inherits (ECMA-376 §17.3.2.40: "determined by the setting previously set at any level of
  the style hierarchy") and is not taken. A run both struck and underlined is a deletion: the strike wins, the
  underline stays on the deleted run (so Reject All gives it back as it was), and the report counts it.
- **Colour**: a run whose own `w:color` is one of the named colours (RRGGBB, any case; `auto` refused, a theme
  colour not taken), with no strike, underline, highlight or shading of its own, becomes a `w:ins`, the colour
  removed (D7).
- **Grouping**: insertions as deletions (D8); a deletion and an insertion side by side are two changes.
- **Comments** (D14, taken as recommended with "do phase 2"): the comments story converts; a converted run in a
  comment keeps its strike, underline or colour, and a deleted mark there its strike.
- **The separating space with insertions**: an insertion takes its separating space by the rejected text, as a
  deletion does by the accepted (D16 below: N01 `shall <u>promptly</u> notify` becomes `shall {++promptly ++}notify`,
  Reject All `shall notify`); the one space between a deletion and the insertion replacing it (a run holding that
  space alone, nothing else shown between them) is written `w:ins` holding `w:del`, both by the author, so that
  neither outcome keeps it (D15 below: M01 `Rate: 1.316 per cent.` rejected, `Rate: 1.335 per cent.` accepted; M04,
  M13, M17, N02, N03 in reverse, the footnote, the text box, N10's comment).
- **Range and parts**: `TrackedChangeFilter`'s range, read before anything is converted; a run converts only inside
  it, a mark only if its paragraph is; `parts` limits the story parts.
- **Audit**: `docProps/custom.xml` gains `docx4j:manualRedline`, its value the instant and the options
  (`2026-10-31T00:00:00Z; author=manual redline, date=..., separatorSpace=true, auditProperty=true`); a preview
  writes none.

### 12.3 On the probe

The first base with every opt-in but the range and the audit property (`ManualRedlineProbeBuilder.phase2()`):
26 deletions, 15 insertions, 28 runs; left M07 (`in_tracked_change`), M08 and M08b (`field`). Accept All
`M01 Rate: 1.335 per cent.`, `M05 then plain words.`, `M12 The party shall promptly notify the other.`; Reject All
`M01 Rate: 1.316 per cent.`, `Widget	$40.00`, `M12 The party shall notify the other.`; no two spaces in either but the
base's own M07. The second base, `manual-redline-2.docx` (N01 to N10, §12.5), the same way: N01 to N03, N07 to N10
converted, N04 (red but highlighted), N05 (a colour not named), N06 (`w:u none`) left; one run struck and underlined.
Both conversions saved for Word's third run: `manual-redline-phase2.docx`, `manual-redline-2-phase2.docx`.

### 12.4 Tests

`ManualRedlineOptInsTest` (10): both bases' texts after Accept All and Reject All; N07's own `w:dstrike w:val="0"`
over its style; N02's deletion keeping its underline; the space between a deletion and its insertion as `w:ins`
holding `w:del`; D14 in the comment (strike and underline kept) against the main story (marks removed); idempotence
and the preview; colours (two named, `auto` refused, any case); a bookmark's range; a control's range (M17); parts;
the audit property, and none from a preview. With `ManualRedlineTest`'s 15, 25.

### 12.5 Decisions from phase 2 (taken as recommended with "do phase 2"; Jason may overrule)

| # | Decision | As built |
|---|---|---|
| D14 | The comments story under `comments(true)` | Converted runs keep their marks (§8.6) |
| D15 | The space between a deletion and the insertion replacing it (§8.3 left it to phase 2) | `w:ins` holding `w:del`: gone in either outcome; measured in Word's third run |
| D16 | An insertion's separating space | Taken into the insertion as D13 takes a deletion's, by the rejected text, so that Reject All leaves one space |

The gate: docx4j-core-tests 2290 run, 0 failures, 30 skipped.

### 12.6 Word's third run

(Pending: Jason runs `cr068-probe.ps1` again (`RUN.txt`, third part) and looks at the comment cards.)
