# CR-068: manual redline to tracked changes, an explicit, opt-in conversion

Status: PROPOSED 2026-10-31, drafted by Claude Fable 5.1 (session docx4j-12) at Jason's direction, relayed by the
portfolio session the same day from its review of LlamaIndex's Document Parsing Protocol (the paper's §7.13; the
`dpp-forks` probe): "strikethrough handling is NOT view-sensitive (Accept All and Reject All leave struck text; the
file does not say it is a deletion), but I suspect we do need an explicit, opt-in conversion that turns manual redline
(strikethrough only by default) into tracked changes with an author such as 'manual redline'". Decisions D1 to D12
await Jason (§6); nothing is built. Phases in §5.

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

## 6. Decisions for Jason

| # | Decision | Recommendation |
|---|---|---|
| D1 | Name and place | `ManualRedline.toTrackedChanges(pkg[, options])` and `preview`, in `org.docx4j.model.revisions`, beside `TrackedChanges`; `ManualRedlineOptions`, `ManualRedlineReport` |
| D2 | Scope by default | The whole document: body, tables, headers and footers, footnotes and endnotes, text boxes (both `mc` branches), content controls; not comments (opt-in) |
| D3 | Author | `manual redline` by default, configurable |
| D4 | Date | The operation's time (UTC, to the second) by default; a fixed `Instant` for reproducible output; `null` writes no date |
| D5 | What converts by default | A run with its own `w:strike` or `w:dstrike` on; style-derived strike only under `styleStrike(true)` |
| D6 | The mark after conversion | Removed from the converted run (strike, and for the opt-ins the underline or colour), so the tracked change alone carries the meaning and Reject All gives plain text; under `styleStrike`, `w:strike w:val="0"` written |
| D7 | Underline and colour as insertions | Opt-ins, off by default; the colour named by the caller, never guessed; strike wins over underline on one run |
| D8 | Grouping | Adjacent converted runs in a paragraph under one `w:del` (one `w:ins`), as Word groups |
| D9 | A struck paragraph mark | Left alone in phase 1 and measured in the probe (M11); a rule only if Word's behaviour on the converted file asks for one |
| D10 | Fields, math, tracked content | Left alone and reported (`field`, `math`, `in_tracked_change`); a struck hyperlink converts inside the hyperlink |
| D11 | Audit | The author name is the record; a custom property `docx4j:manualRedline` only under `auditProperty(true)` |
| D12 | Settings | `w:trackRevisions` untouched; converting is not turning tracking on |

## 7. Out of scope

Detecting a redline (a heuristic that decides a document is one): the reviewer decides, the text model's finding
(if CR-050 takes it) points. Deleted-row and deleted-cell marks. Moves (`w:moveFrom`/`w:moveTo`) from struck and
re-inserted text. Property changes (`w:rPrChange`). Turning Track Changes on. Any change to extraction defaults or
views (Jason: not view-sensitive).

## 8. Phase 0 record

(Not started.)

## 9. Hand-offs

- **The portfolio**: the registry entry (`docx4j/CR-068`, proposed); the paper's §7.13 may cite this CR; the
  `mcp_tool_vocabulary` verb when the CR is accepted.
- **docx4j-mcp, core-ts, python**: at phase 3; nothing until then.

## 10. Sources

- The paper, `../docx4j-portfolio/docs/text_extraction_behaviour_model.md` §7.13 (31 October 2026; docx4j-portfolio
  commit 09502ce, with WD 0.43 and 0.44), and the `dpp-forks.docx` probe (Office 2016/dpp-forks/ on the share);
  docx4j's rows and views in that section.
- CR-043 (`CR-043-tracked-changes-accept-reject-api.md`) §3.1 (the API), §3.2 (what accept and reject do), §3.5
  (tests and the oracle), §4 (the fixtures and the probe).
- CR-046 (the text model's three views), CR-050 (the inspection; the proposed `text.formatting_join` kind).
- ECMA-376 Part 1 §17.13.5.14 (`del`), §17.13.5.18 (`ins`), §17.3.3.7 (`delText`), §17.3.2.37 (`strike`),
  §17.3.2.9 (`dstrike`); clause numbers to be checked at phase 0.
