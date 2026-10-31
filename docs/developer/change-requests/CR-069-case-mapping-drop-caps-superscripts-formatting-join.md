# CR-069: what the page shows, in the text: case mapping, drop caps, superscripts, and the formatting-join finding

Status: ACCEPTED 2026-10-31 (Jason: "CR-069 decisions as presented"): D1 to D9 as recommended (§5); D9's condition
ruled the same day (Jason: "17.3.5 will wait for CR-069"): 17.3.5 ships with CR-069, not only phases 0 to 2. Phase 0 DONE 2026-10-31 (§10; Jason: "start CR-069"); phase 1 DONE 2026-10-31 (§11; Jason: "go ahead with phase 1"). Proposed
2026-10-31, drafted by Claude Fable 5.1 (session docx4j-12) from Jason's rulings of the same day on the
four questions the portfolio's review of LlamaIndex's Document Parsing Protocol put to him (paper §7.13; WD 0.44, drafted
and uncommitted: REQ-521, REQ-607, REQ-710, §9.2 `text.formatting_join`, §4 NOTE, Appendix B). Jason's rulings: (a) the
drop-cap join, (b) the formatting-join inspection kind and (c) `<sup>`/`<sub>` with a vertAlign flag, as recommended;
(d) differently: `w:caps` written in capitals by the run's language, per code point, as measured; `w:smallCaps` written
as its STORED characters. Docx4j-side decisions D1 to D9 in §5; phases in §4.

## 0. Summary

Four measured gaps between docx4j's text and what Word shows on the page and writes in its own text save:

1. **Case mapping.** Word's Save As Text and its PDF write `w:caps` text in capitals (`JACKSON`), uppercasing with the
   run's language (tr-TR `istanbul` gives `İSTANBUL`; a run without `w:lang` uses the document default), one code point
   to one (`ß` stays `ß`, the ligature `ﬁ` stays, `ǆ` becomes `Ǆ` U+01C4), and small caps the same as caps. docx4j
   writes the stored case. Jason's ruling (d): caps in capitals by that mapping; small caps as stored, since All Caps
   is a case transform no reader can invert while small caps is a glyph variant that keeps the case distinction in
   the letter heights (CSS draws the same line: `text-transform` against `font-variant`). A run under both is a caps
   run. Small caps as stored is a named departure from Word's text (WD Appendix B); the `word-text` profile sets
   `small_caps: upper`.
2. **Drop caps.** A `w:framePr/@w:dropCap` paragraph holding `T` followed by the paragraph `he modern ...` is two
   paragraphs in every reader and in Word's text save; the page shows `The`. Ruling (a): join them, with a warning.
3. **Superscripts and subscripts.** `48,210` with a superscript `3` is `48,2103` in plain text everywhere, Word's text
   save included; the page shows a footnote-like mark. Ruling (c): `<sup>`/`<sub>` in Markdown and HTML, never a
   Unicode substitution (Word's own cp1252 text save turns a literal `₂` into `?`); plain text stays at baseline; the
   block model carries the flag.
4. **The fabricated number.** Struck `1.316` beside underlined `1.335` in a cell gives `1.3161.335`, a number the page
   never showed. Ruling (b): an inspection kind, `text.formatting_join`, where a struck or super/subscripted run meets
   its neighbour at a digit-to-digit boundary with no separator; UNSEEN under plain text, INFO under Markdown or the
   block model, where the formatting shows. Not an extraction change: CR-068's conversion is the remedy a reviewer runs.

One CR, because the four touch the same places: `TextRun` (flags), `PlainText` and docx4j-markdown's writer (the
mapping, the tags), `Extractor` (the join), `InspectWalk` (the kind), `TextOptions` (two options and the preset), and the
block-model goldens the ports mirror. CR-046 (the text model) and CR-050 (the inspector) are done and are cited, not
reopened.

## 1. What was measured (31 October 2026; Word 365 build 20326.20232; paper §7.13; WD-001)

- `dpp-forks.docx`'s ten cases through eleven readers, Word's PDF and Word's text save (per case, because the full
  file hit WD-001). docx4j's rows and the readers' are in §7.13.
- The caps probes (`caps-lang6a`, `6b`, `7b`, `7c`, `7d`, `7e`; `Office 2016/dpp-forks/` on the share, texts beside
  them): capitals for `w:caps` and `w:smallCaps`; `İSTANBUL FİLM` under tr-TR for both; `ISTANBUL FILM` under en-US and
  with no `w:lang`; `STRAßE`; `CAFÉ`; `ﬁNE CAFÉ Ǆ`. Every one is a golden for phase 0.
- WD-001 (`docx4j-portfolio/docs/word_defects.md`, commit 2e1ad98): Word's text save fails silently when a run holding or producing a
  character outside the default code page starts at character 246 or later (CR LF units). It bounds how the oracle's
  text saves can be made, not what this CR builds.

## 2. What exists

- `TextRun`: `bold`, `italic`, `strike`, `monospace`, each the run's own property folded with its styles' and
  cancelled where the paragraph style has it (`Extractor.runFormat`, the `on()` fold). No case or vertical-alignment
  flag; `w:caps`, `w:smallCaps` and `w:vertAlign` are read nowhere in the text model.
- `PlainText.of(TextResult)` writes runs' text as stored; docx4j-markdown's `WmlToMarkdown` writes `**`, `*`, `~~`
  from the three flags through its sink (line ~1005) and nothing for vertical alignment.
- HTML output (`Docx4J.toHTML`) applies CSS: `font-variant: small-caps`, `text-transform: uppercase`,
  `vertical-align: super|sub; font-size: smaller` (the `VerticalAlignment` property); no tags.
- `Extractor` has no drop-cap handling; a `w:framePr` paragraph is a paragraph.
- `TextOptions.NAMES` (WD §10 order) and the `WORD_TEXT_VALUES` preset; `InspectFinding` holds 63 kind constants,
  `InspectionReport.PHRASES` one phrase each, `Inspection.counts(options, Format)` already groups by output format.
- The fixtures: `text-extraction/` in docx4j-core-tests (phase 0 probes, `expected-tokens.tsv` with its `accepted`,
  `original`, `markup` columns), the block-model goldens the ports mirror.

## 3. Design

### 3.1 The block model (REQ-607, REQ-710)

`TextRun` gains `isCaps()`, `isSmallCaps()` and `getVerticalAlignment()` (`BASELINE`, `SUPERSCRIPT`, `SUBSCRIPT`),
folded as the other flags are (own, else styles; `w:caps` and `w:smallCaps` toggles; a run under both is caps, D2).
The text stays the STORED text: addresses, offsets and the inspection's excerpts refer to it (WD REQ-607), and the
per-code-point mapping preserves length, so a writer's uppercase never moves an offset.

### 3.2 The writers

- **Plain text** (`PlainText`): a caps run's text is written through `CaseMapping.upper(text, language)`: one code
  point to one, `Character.toUpperCase(int)` with the tr/az rule (`i` U+0069 to `İ` U+0130; `ı` is already
  lowercase-only), never `String.toUpperCase(Locale)` (which expands `ß` to `SS` and `ﬁ` to `FI`, which Word does
  not). Language: the run's `w:lang/@w:val`, else the paragraph's and styles' through the usual fold, else the
  document default (`docDefaults` `w:lang`, else `w:themeFontLang`, else `en-US`, docx4j's documented default where the
  WD leaves it to the implementation), D3. WD 0.44 REQ-607 and REQ-521 were aligned to §3.2 and §3.3 on 2026-10-31. A small-caps run is written
  as stored under the default and uppercased under `small_caps: upper` (D5).
- **Markdown** (docx4j-markdown): the same mapping for caps; `<sup>…</sup>` and `<sub>…</sub>` around a
  super/subscripted run's text (CommonMark inline HTML, what the DPP and GitHub render; pandoc's `^x^` is not
  CommonMark). Inside a code span no tags (code is literal). The tags are the writer's own markup: document text
  cannot form them (the writer escapes `<`).
- **HTML.** REQ-710's HTML is the WD's own HTML serialisation of the text model (its §7.3), which docx4j does not
  implement; Markdown is docx4j's surface. Whether `Docx4J.toHTML` (the general converter) adopts `<sup>`/`<sub>`
  elements for `w:vertAlign` in place of its CSS `vertical-align` span is docx4j's own choice, not a WD requirement
  (the portfolio, 2026-10-31): recommended yes, so that text-scraping consumers of the HTML see the mark and the two
  surfaces agree; the CSS for caps and small caps stays (the HTML's text is the stored text), D4.
- **The block model** itself changes only by the flags; the goldens the ports mirror gain three fields.

### 3.3 The drop-cap join (REQ-521)

In `Extractor`, a body-level paragraph P whose `w:pPr/w:framePr/@w:dropCap` is `drop` or `margin`, whose text is at
most one word (no whitespace; Word's dialog applies the drop cap to the first letter, a user can extend the frame),
and whose next sibling in the same story is a paragraph Q (not a table, not a section break alone) is joined: Q's
first run is preceded by P's runs, no separator, one block with Q's address; P's own paragraph properties (the frame)
are dropped and P's run properties kept on its runs (so the block model shows the `T` at 40pt as a run). A
`TextWarning` `paragraph.drop_cap_joined` at P's address says two `w:p` became one. Option `drop_caps` `join|asis`,
default `join`; the `word-text` preset sets `asis` (Word's text save writes two paragraphs). Under `asis` nothing
changes from today. Not joined (and no warning): a frame paragraph with more than one word, a drop cap whose next
sibling is not a paragraph, a drop cap inside a table cell with no following paragraph in the cell.

### 3.4 The formatting-join finding (WD §9.2)

`InspectWalk`: for each paragraph's run sequence as the plain text joins it, a boundary between two adjacent runs
with no whitespace or punctuation between where the left run's last character and the right run's first are both
digits (`Character.isDigit`), and the two runs differ in strike (`w:strike`/`w:dstrike`) or in vertical alignment,
is a finding `text.formatting_join`: location the right run, excerpt the joined token (`1.3161.335`, `48,2103`),
subject the two parts, `Visible.DIFFERENTLY`, reveal by format (`Inspection.counts(options, Format)`): UNSEEN under
`PLAIN`, INFO under `MARKDOWN` (strike shows as `~~`, vertAlign as the tags once phase 3 lands) and `BLOCKS` (the
flags). Hidden runs and runs inside `w:del` are not boundaries (the plain text does not join them). Letter boundaries
(`H2O`, `x2`, `1st`) do not fire, by design: ordinals read the same either way (D6). `InspectionReport.PHRASES`
gains the phrase; CR-050 §2.3's table gains the row by reference to this CR.

### 3.5 Options and the preset

`TextOptions` gains `small_caps` (`asis` default, `upper`) and `drop_caps` (`join` default, `asis`), in WD §10's
order in `NAMES`; `WORD_TEXT_VALUES` gains `{"small_caps","upper"}`, `{"drop_caps","asis"}`. `toMap()`/`toString()`
follow; `InspectionReport.optionsPhrase` shows them when non-default.

### 3.6 Tests and the oracle

- **Fixtures** (phase 0): `dpp-forks.docx` and Word's re-save `dpp-forks-word365.docx`, the caps probes and their
  Word text saves (default encoding and UTF-8 where made), the bisect `C0n.txt` saves, copied from the share into
  `docx4j-core-tests/src/test/resources/text-extraction/dpp/`; `expected-tokens.tsv` rows for the new tokens
  (`ROBERTS` under plain text, §10.3 item 4; `İSTANBUL FİLM` for the tr-TR case; `Jackson` stored under
  `small_caps: asis` and `JACKSON` under `upper`; the drop-cap `The` and `C02_`; `48,210` and `3` as the block
  model's two runs); the `word-text` profile's expected text equal to Word's saves byte for byte where WD-001 let
  Word write one.
- **Tests**: `CaseMappingTest` (the table of §1, the tr/az rule, length preserved, `String.toUpperCase` differences
  pinned); `DropCapJoinTest` (join, warning, `asis`, the not-joined shapes); `FormattingJoinTest` (C03 none, C04
  fires, C07 fires, C06 none; counts by Format); Markdown and HTML tag tests in their modules; the block-model goldens
  regenerated and the ports told.

## 4. Phases

| Phase | What | Size |
|---|---|---|
| 0 | Fixtures and goldens from the share into core-tests; the tsv rows; WD 0.44 cross-checked against the CR | ½ day |
| 1 | `TextRun` flags, `CaseMapping`, plain-text and (as built, §11) Markdown caps mapping, `small_caps` option and preset; `CaseMappingTest`; block goldens regenerated; hand-off to the ports (the python note: `str.upper()` expands `ß` and ignores tr; the per-code-point mapping is needed) | 1 day |
| 2 | The drop-cap join, its warning, `drop_caps` option and preset; `DropCapJoinTest` | ½ day |
| 3 | docx4j-markdown: `<sup>`/`<sub>` (its caps mapping built in phase 1, §11); `Docx4J.toHTML` `<sup>`/`<sub>` if D4 is taken | ½ day |
| 4 | `text.formatting_join` in `InspectWalk`, the phrase, `FormattingJoinTest`; CR-050 §2.3 row by reference | ½ day |
| 5 | CHANGELOG; Getting Started's Text extraction chapter (a sentence each on caps, drop caps, `<sup>`); hand-offs: the ports (CR-046-style), docx4j-mcp (nothing new in the contract beyond the flags and the kind), docx4j-tika (the XHTML gains `<sup>`/`<sub>` and the capitals; its parity baseline moves) | ½ day |

## 5. Decisions

Jason has ruled (a) to (d) as the Status line says; these are the docx4j-side decisions under them, all nine
accepted as presented, 2026-10-31 (Jason).

| # | Decision | Recommendation (accepted) |
|---|---|---|
| D1 | Shape | One new CR (this), not addenda to CR-046 and CR-050; both are done and are cited |
| D2 | Flags | `TextRun.isCaps()`, `isSmallCaps()`, `getVerticalAlignment()` (`BASELINE`, `SUPERSCRIPT`, `SUBSCRIPT`); own-else-styles fold as for bold; a run under caps and small caps is caps |
| D3 | Mapping | Per code point, `Character.toUpperCase(int)` plus the tr/az `i` to `İ` rule; language from the run's `w:lang/@w:val`, else the styles', else the document default (`docDefaults`, `w:themeFontLang`, `en-US`); never `String.toUpperCase(Locale)` |
| D4 | HTML | docx4j's choice, not a WD requirement (REQ-710's HTML is the WD's own serialisation): `<sup>`/`<sub>` elements for `w:vertAlign` in `Docx4J.toHTML` (the `font-size: smaller` kept on them), so the surfaces agree; caps and small caps stay CSS |
| D5 | Options | `small_caps` `asis|upper` (default `asis`), `drop_caps` `join|asis` (default `join`); `word-text` preset `upper` and `asis` |
| D6 | The finding's boundary | Digit-to-digit only, strike or vertical alignment differing, no whitespace or punctuation between; letters never |
| D7 | Oracle | Word's text saves from the share (the files WD-001 let Word write) are the goldens, byte for byte under `word-text` |
| D8 | Offsets | The block model and every address keep the stored text; uppercase is the writers' (REQ-607), and the mapping preserves length |
| D9 | Order | Phases 0 to 2 before 17.3.5 ships if Jason wants the caps change in it (it changes plain-text output for every document with `w:caps`); phases 3 and 4 may follow in 17.3.6 . **Ruled 2026-10-31** (Jason: "17.3.5 will wait for CR-069"): the whole CR goes into 17.3.5 |

## 6. Risks

- **Output change.** Every document with All Caps runs gets capitals in plain text and Markdown from this release.
  That is Word's text and the page's, so it is the point, but indexes re-indexing will see it (as docx4j-tika's README
  already says for other departures from Tika's text). CHANGELOG names it.
- **Language resolution.** A document whose runs carry no `w:lang` but whose default is tr-TR uppercases `i` to `İ`
  everywhere caps applies; that is Word's behaviour (measured for the no-`w:lang` run under an en-US default; the
  tr-TR-default case is unmeasured and goes on the unmeasured list).
- **Drop-cap false joins.** A frame paragraph used for a pull quote with `dropCap` set would be joined; the one-word
  limit guards it; `drop_caps: asis` is the way out.
- **Tags in Markdown.** A consumer that strips HTML from Markdown loses the marks; they keep the baseline digits
  (`48,2103`), which is what they have today.
- **Ports.** Python's `str.upper()` and JavaScript's `toUpperCase()` expand `ß` and ignore the language; both need
  the per-code-point mapping with the tr/az rule to match the goldens.

## 7. Unmeasured (the running list, CR-043 D5's convention)

- Caps under a tr-TR document default with no run `w:lang` (predicted `İ`, as the run-level case).
- `w:caps` on text already in capitals with `ß` (`STRAßE` predicted unchanged).
- Small caps under the `word-text` preset against Word's save: measured (capitals); the default's stored case is a
  deliberate departure, not an unmeasured one.
- A drop cap whose frame holds two words; a drop cap followed by a table.
- A paragraph style's `w:caps` in Word's text save (phase 1 applies it, §11.2 item 2; the probes measured direct
  formatting only).
- A caps run's field code under `field_codes: true`, and a list label whose level's run properties have `w:caps`
  (phase 1 writes both as stored, §11.2 item 4).

## 8. Hand-offs

- **The portfolio**: the registry entry (`docx4j/CR-069`, proposed); WD 0.44 is the normative text this CR builds to.
- **docx4j-python, docx4j-core-ts**: at phase 1 (flags, mapping, goldens) and phase 2; the mapping note above.
- **docx4j-mcp**: the flags appear in the block model it returns; the kind in inspections; no contract change.
- **docx4j-tika**: at phase 3; its `TikaParityIT` expected tokens move (capitals) and its XHTML gains the tags.
- **CR-068**: this CR's finding is the pointer to that CR's conversion; nothing else shared.

## 9. Sources

- The paper, `../docx4j-portfolio/docs/text_extraction_behaviour_model.md` §7.13 (the DPP review, the rows, the
  measurements, the seventy caps-lang probes), docx4j-portfolio commit 09502ce.
- WD 0.44 (`docs/text_extraction_specification_wd.md`, the same commit 09502ce): REQ-521, REQ-607, REQ-710,
  §9.2 `text.formatting_join`, §4 NOTE, Appendix B.
- WD 0.45 (docx4j-portfolio commit 3693c44): REQ-711, a serialization's encoding (UTF-8 without a byte order mark
  unless the caller names one; a character the encoding lacks replaced and reported as `text.unencodable`, never
  lost silently), and Appendix B's Encoding row with the caps-lang measurement (Word's default cp1252, `?` for what
  it lacks, a UTF-8 save with a byte order mark): how Word's text saves, this CR's goldens, are to be read.
- WD-001, `docs/word_defects.md` with the three reproducers in `docs/word_defects/` (caps-lang17-R246a refused,
  caps-lang17-R245b saved, caps-lang18-P2 refused), docx4j-portfolio commit 2e1ad98.
- The probes and Word's saves: `~/vbShares/Office 2016/dpp-forks/` (`dpp-forks.docx`, `dpp-forks-word365.docx`,
  `dpp-forks.pdf`, `bisect/`, `caps-lang*.docx` and `.txt`).
- CR-046 (the text model), CR-050 (the inspector; §2.3 the kinds table), CR-068 (manual redline).
- ECMA-376 Part 1 (5th edition, 2016) §17.3.2.5 `caps`, §17.3.2.33 `smallCaps`, §17.3.2.42 `vertAlign`,
  §17.3.1.11 `framePr` (`dropCap`, `lines`), and §17.3.2.20 `lang`, §17.3.2.37 `strike`, §17.3.2.9 `dstrike`;
  checked against the PDF at phase 0.

## 10. Phase 0 (2026-10-31; Jason: "start CR-069")

### 10.1 What was built

The suite is `docx4j-core-tests/src/test/resources/text-extraction/dpp/`; its `README.md` is the record of the files
and of how they are read.

- **Fixtures**, from the share as Word left them: `dpp-forks.docx` and Word's re-save `dpp-forks-word365.docx` (every
  row is on the re-save); `bisect/`, the ten single-case files with Word's text save of each and the request Jason ran;
  `caps/`, the ten caps probes Word saved (`4b`, `5`, `6a`, `6b`, `7b` to `7g`) with their twelve saves, windows-1252
  and, where made, UTF-8 with a byte order mark. The probes WD-001 kept from saving are not there; nor is Word's PDF,
  read only for the paper. `.gitattributes` marks the saves `-text`, so they stay Word's bytes.
- **Tables** in phase 0's protocol (`GOLDENS.md`, the same columns, fixtures named relative to the folder): 34 token
  rows (12 on the probe, 22 on the caps probes, case 5 for each), 8 findings rows (`paragraph.drop_cap_joined` on the
  probe and on `bisect/C02`, `text.formatting_join` on C04 and C07 of the probe and of the bisect files,
  `text.near_background` on C08 and C09), 2 goldens (the probe's plain text and Markdown in the default options, cut by
  hand from today's output with this CR's changes made), and a new table, `word-saves.tsv`, 22 of Word's saves against
  the text under `profile: word-text`.
- **Harness** (`ExtractionConformance`): the optional `word-saves.tsv` and `verifyWordSave`, WD REQ-1002's NOTE made
  exact (the save decoded by its byte order mark, else the code page the row names; the extraction passed through that
  encoding, so that a character the code page lacks is `?` on both sides; lines compared, a line ending at CR LF, CR,
  LF or tab, which is Word's CR LF paragraphs and CR cells against the specification's LF and tabs; every character
  and every break must agree); the scenarios `small_caps`, `drop_caps` and `profile` (REQ-607, REQ-521, REQ-1002); a
  pending row names the CR-069 phase that builds it (REQ-607 1, REQ-521 2, REQ-710 3, the capability
  `text.formatting_join` 4). `TextsAdapter` advertises REQ-1002 (the preset since WD 0.41; its `drop_caps` and
  `small_caps` rows also cite REQ-521 and REQ-607) and reads a Word-saved package without the builder's fixed ZIP
  timestamp, which only `ExtractionFixtureChecks` asks of the built probes.
- **Tests** (`ExtractionConformanceTest`): `dppSuiteIsCheckedOrPendingOnItsPhase` (the suite run; what holds now
  passes, every pending row names a CR-069 phase) and `wordSaveIsReadByItsEncodingAndComparedByLine` (the decoding, the
  code page's `?`, a lost cell break, the invariant mapping and a mislabelled encoding each fail). 14/14 in the class;
  phase 0's own suite unchanged at 1736 passed, 36 pending.

### 10.2 Today's docx4j against the oracle

docx4j 17.3.5-SNAPSHOT (1a0b5315c) under `profile: word-text`: nine of the ten single-case saves agree with Word's
(control, C02 to C04, C06 to C10; C02 only because nothing joins yet); C05 differs by the case mapping alone
(`Jackson`, `Roberts` against Word's `JACKSON`, `ROBERTS`). Of the caps probes, `4b` (a `w:lang` with no case
property) agrees in both encodings, and every other differs on exactly its caps and small-caps runs. On these files
the comparison has nothing to absorb beyond the residue REQ-1002's NOTE names, and nothing differs that this CR does
not change. The suite reports 11 assertions passed (control, C03, C04, C08, C09 and C10's saves; the two
`text.near_background` rows; the literal `H₂O` of `caps-lang5` in three views) and 181 pending: 118 on phase 1, 7 on
phase 2, 52 on phase 3 (the two goldens among them), 4 on phase 4.

### 10.3 Departures from §3.6 as written

1. **"Byte for byte" (D7)** is read as REQ-1002's NOTE reads a Word save (WD 0.45 onward): decoded, then compared
   with the residue made exact (§10.1). Nothing the page or Word's text shows is excused: every character, Word's `?`
   included, and every break between paragraphs and cells.
2. **Their own tables, not `phase0/expected-tokens.tsv`.** Phase 0's tables are the builder's
   (`ExtractionProbeBuilder` writes them, a test compares them with its output) and keyed by the built probe's cases;
   these fixtures are Word's files. The protocol and columns are the same, so a port's harness runs the folder as it
   is, and `word-saves.tsv` is optional to it as `expected-order.tsv` is.
3. **No token rows for the drop cap** (`The`, `C02_`): REQ-1201 removes whitespace, so no token or golden can tell a
   joined drop cap from two paragraphs. The join is checked by the warning row, by Word's C02 save under `word-text`
   (`drop_caps: asis`: `T`, then `he C02_ ...`) and by `DropCapJoinTest` (phase 2), which reads the blocks.
4. **`JACKSON` is not "accepted under plain text"**, as §3.6 said: it is the small-caps run, so by ruling (d) the
   default writes `Jackson` and `small_caps: upper` writes `JACKSON` (the rows; §3.6 corrected).
5. **A `text.formatting_join` row's selector is its excerpt**, the joined token (`1.3161.335`, `48,2103`), since the
   adapter names a finding by its subject first and §3.4's subject ("the two parts") has no form yet. Phase 4 settles
   it; if the finding gets a subject, the four rows' selector becomes it.
6. **A caps probe's label can hold its token** (`caps-lang6b`'s `(STRASSE or STRAßE)`, `caps-lang7e`'s `on istanbul
   film`): the counts are of the whole file, labels included. Every caps row was checked against Word's own save before
   it was written (the ones whose token the code page cannot hold, against the UTF-8 reading of the `?`).

### 10.4 The WD cross-checked (the CR cites 0.44; the WD is 0.49, docx4j-portfolio a993e46)

REQ-521, REQ-607, REQ-710, §9.2's `text.formatting_join` row and the `drop_caps` and `small_caps` option rows are
unchanged from 0.44 to 0.49; REQ-801's list gained `text.unencodable` (0.45) beside `paragraph.drop_cap_joined`. Four
points for the phases:

- **REQ-710's last sentence**: "A `w:footnoteReference` or `w:endnoteReference` is a reference (REQ-503), not a
  superscript." §3 did not say so. Phases 1 and 3 build to it: a note reference's run is read as a reference whatever
  its style's `w:vertAlign` (the `FootnoteReference` style raises it), so no `<sup>` around `[n]` in Markdown and no
  `text.formatting_join` at it; the phase 0 probe's note cases are where phase 3 tests it.
- **REQ-607's language chain** is the run's `w:lang`, else its styles', else the document defaults', else
  `w:themeFontLang`, else the implementation's default, as D3 has it. §3.2's "the paragraph's" means the paragraph
  style's run properties: the paragraph mark's own `w:rPr` applies to the mark, not to the runs.
- **REQ-1002's preset** names `math: unicodemath` (since 0.41), which docx4j does not implement and documents
  (`TextOptions.Profile`); not this CR's.
- **REQ-711** (0.45) governs only bytes docx4j writes; Word's saves here are read under its NOTE, as §10.3 says.

### 10.5 Hand-offs

The registry (`docx4j/CR-069` in progress, phase 0 done) to the portfolio session. The ports hear at phase 1, as §8
says, now with the folder to copy: the four tables, the fixtures and Word's saves, and the comparison rule
(`dpp/README.md`); the portfolio may want the folder as the WD suite's fixture for these statements (WD 0.44's history
row: "[PROBE] gains no fixture yet (the `dpp-forks` Word re-save is the candidate)").

## 11. Phase 1 (2026-10-31; Jason: "go ahead with phase 1")

### 11.1 What was built

- **`TextRun`**: `isCaps()`, `isSmallCaps()`, `getVerticalAlignment()` (the enum `VerticalAlignment`: `BASELINE`,
  `SUPERSCRIPT`, `SUBSCRIPT`) and `getLanguage()`, set in `Extractor.formatOf` for text runs. Each takes the run's own
  property, else its styles' (the resolver's effective run properties, which XOR the toggles `w:caps` and
  `w:smallCaps` through the style chain, ECMA-376 §17.7.3). A run under both is caps and not small caps. Runs merge
  only when the four agree.
- **`CaseMapping`** (public, `org.docx4j.model.text`): `upper(text, language)`, each code point to
  `Character.toUpperCase(int)` (Unicode's simple mapping) and `i` to `İ` where the language's primary subtag is `tr` or
  `az`; `isUpper` and `written` for a run, a block and a comment under the options; `DEFAULT_LANGUAGE` `en-US`.
- **Language**: the run's `w:lang/@w:val`, else its styles' together with the document defaults' (one effective
  `w:rPr`), else the settings' `w:themeFontLang`, else `en-US`. Set on cased runs only.
- **`TextOptions.small_caps`** (`SmallCaps.ASIS`, `UPPER`; default `ASIS`), after `symbols` in WD §10's order, in
  `toMap()`, `copy()` and by name; the `word-text` preset sets `upper`. `InspectionReport` names it when it is not the
  default, as it does any option.
- **Writers**: `PlainText` (a text run; a comment's text through `CaseMapping.written(TextComment, ...)`, since
  `TextComment.getText()` is the stored text) and docx4j-markdown's `WmlToMarkdown` (a text run, a code paragraph, a
  comment's text). `TextBlock.getText()` now runs through a package-private `text(Function)`, which the block and
  comment forms use.
- **Harness**: `TextsAdapter` advertises REQ-607.
- **Tests**: `CaseMappingTest` (10): Word's table; the dotted-i rule; the differences from `String.toUpperCase`
  pinned (`ß`, `ﬁ`, U+1FB3, the titlecase digraph, a supplementary letter); the length kept for every code point in
  both encodings; the flags and the language on Word's probe; the paragraph style's caps; a run under both; runs
  merged by language; the language of last resort (no defaults' `w:lang`: `w:themeFontLang`, else `en-US`; a `tr-TR`
  theme language gives `İ`, the unmeasured prediction of §7); a note reference is baseline; the option.
  `ExtractionConformanceTest`'s dpp test now asserts the phase 1 rows pass and that none pends on REQ-607.

### 11.2 Decisions taken in the phase (docx4j's; none changes the WD)

1. **`getLanguage()` is docx4j's addition to the block model.** The WD's run carries the two flags; the writer needs
   the language. It is null on an uncased run, so a `w:lang` alone never splits a run. WD 0.51 (docx4j-portfolio
   12f9bda) adopts it as the run's `lang`, and makes REQ-607 say that a style's `w:caps` applies with no cancelling
   rule (item 2), its NOTE recording that Word's saves have not measured a style's caps.
2. **The paragraph style does not cancel the case**, as it cancels bold, italic and strike: those flags are Markdown
   emphasis, a heading style's bold being the heading's, while a style's caps is the text the page shows. On the
   unmeasured list (§7).
3. **A code point whose uppercase would change its UTF-16 length is kept.** None does in Java 21's tables (checked
   over every code point); the guard keeps D8's offsets on any JDK.
4. **Text runs only**: a field code (under `field_codes: true`) and a list label are written as stored, as before
   (the extractor sets formatting on text runs alone). On the unmeasured list.
5. **The Markdown half of REQ-607 is built here, not in phase 3.** The statement covers plain text and Markdown, and
   the suite's Markdown rows cite it, so advertising it needed both. Phase 3 keeps the tags. In Markdown a code span
   and a code paragraph get the capitals too: §3.2 keeps tags out of code; the capitals are the text.
6. **REQ-710's note reference** (§10.4) holds by construction: the formatting is set on text runs only, so a note
   reference is baseline whatever its style raises. The test pins it on phase 0's probe.
7. **The Unicode version is the running JDK's**, unpinned: Unicode 10.0 on Java 11 (docx4j's floor), 15.0 on the
   OpenJDK 21.0.12 the tests ran on (measured: U+1E030 defined, U+2FFC not). A character whose case pair is newer
   maps differently by runtime (U+1C8A to U+1C89 is Unicode 16.0's, so Python 3.14 maps it and JDK 21 does not;
   python-64 asked, 2026-10-31). The tests and the dpp rows map only characters whose simple uppercase has been stable
   since before Unicode 10.0, and the whole-range test asserts only that the length is kept. Word's own table is
   Windows', its version unmeasured.

### 11.3 Results

- The dpp suite: 129 assertions passed and 63 pending (phase 2: 7, phase 3: 52, phase 4: 4); none pends on REQ-607.
  Of Word's 22 saves, 19 now agree, every caps probe and C05 among them; C02 waits on phase 2, C06 and C07 on phase 3.
- Phase 0's suite unchanged at 1736 passed, 36 pending: its fixtures have no cased run.
- "Block goldens regenerated" (§4) had nothing to do here: docx4j has no block-model goldens (no serialised block
  model in its tests or fixtures; docx4j-mcp does not return one yet). The flags are tested in `CaseMappingTest`.
- Gate: docx4j-core-tests 2320/0/30 (was 2307/0/30 before this CR), docx4j-markdown 172/0/0.

### 11.4 Hand-offs

- **docx4j-core-ts, docx4j-python**: the flags, the language, the mapping (Unicode's simple uppercase plus the tr/az
  rule, with the reason neither language's own upper will do), `small_caps`, and the `dpp/` folder to copy (WD 0.50,
  docx4j-portfolio 4b1590d, made it [PROBE]'s sixth fixture).
- **docx4j-tika**: `XhtmlWriter` writes `TextRun.getText()`, so its text keeps the stored case. REQ-607 says HTML
  carries CSS and the stored characters, while Tika's consumers mostly read the text without the CSS: whether its
  XHTML uses `CaseMapping.written` is that project's choice.
- **docx4j-mcp**: FYI. It does not read the text model yet (its `text-model` entries are proposed); when it does,
  `small_caps` is one more option by name.
- **The portfolio**: the registry note (phase 1 done).
