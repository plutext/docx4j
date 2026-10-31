# CR-069: what the page shows, in the text: case mapping, drop caps, superscripts, and the formatting-join finding

Status: PROPOSED 2026-10-31, drafted by Claude Fable 5.1 (session docx4j-12) from Jason's rulings of the same day on the
four questions the portfolio's review of LlamaIndex's Document Parsing Protocol put to him (paper §7.13; WD 0.44, drafted
and uncommitted: REQ-521, REQ-607, REQ-710, §9.2 `text.formatting_join`, §4 NOTE, Appendix B). Jason's rulings: (a) the
drop-cap join, (b) the formatting-join inspection kind and (c) `<sup>`/`<sub>` with a vertAlign flag, as recommended;
(d) differently: `w:caps` written in capitals by the run's language, per code point, as measured; `w:smallCaps` written
as its STORED characters. Nothing is built; Jason has not said to start. Docx4j-side decisions D1 to D9 in §5; phases in §4.

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
- WD-001 (`docx4j-portfolio/docs/word_defects.md`): Word's text save fails silently when a run holding or producing a
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
  (`JACKSON`, `ROBERTS` accepted under plain text; `İSTANBUL FİLM` for the tr-TR case; `Jackson` stored under
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
| 1 | `TextRun` flags, `CaseMapping`, plain-text caps mapping, `small_caps` option and preset; `CaseMappingTest`; block goldens regenerated; hand-off to the ports (the python note: `str.upper()` expands `ß` and ignores tr; the per-code-point mapping is needed) | 1 day |
| 2 | The drop-cap join, its warning, `drop_caps` option and preset; `DropCapJoinTest` | ½ day |
| 3 | docx4j-markdown: caps mapping and `<sup>`/`<sub>`; `Docx4J.toHTML` `<sup>`/`<sub>` if D4 is taken | ½ day |
| 4 | `text.formatting_join` in `InspectWalk`, the phrase, `FormattingJoinTest`; CR-050 §2.3 row by reference | ½ day |
| 5 | CHANGELOG; Getting Started's Text extraction chapter (a sentence each on caps, drop caps, `<sup>`); hand-offs: the ports (CR-046-style), docx4j-mcp (nothing new in the contract beyond the flags and the kind), docx4j-tika (the XHTML gains `<sup>`/`<sub>` and the capitals; its parity baseline moves) | ½ day |

## 5. Decisions

Jason has ruled (a) to (d) as the Status line says; these are the docx4j-side decisions under them.

| # | Decision | Recommendation |
|---|---|---|
| D1 | Shape | One new CR (this), not addenda to CR-046 and CR-050; both are done and are cited |
| D2 | Flags | `TextRun.isCaps()`, `isSmallCaps()`, `getVerticalAlignment()` (`BASELINE`, `SUPERSCRIPT`, `SUBSCRIPT`); own-else-styles fold as for bold; a run under caps and small caps is caps |
| D3 | Mapping | Per code point, `Character.toUpperCase(int)` plus the tr/az `i` to `İ` rule; language from the run's `w:lang/@w:val`, else the styles', else the document default (`docDefaults`, `w:themeFontLang`, `en-US`); never `String.toUpperCase(Locale)` |
| D4 | HTML | docx4j's choice, not a WD requirement (REQ-710's HTML is the WD's own serialisation): `<sup>`/`<sub>` elements for `w:vertAlign` in `Docx4J.toHTML` (the `font-size: smaller` kept on them), so the surfaces agree; caps and small caps stay CSS |
| D5 | Options | `small_caps` `asis|upper` (default `asis`), `drop_caps` `join|asis` (default `join`); `word-text` preset `upper` and `asis` |
| D6 | The finding's boundary | Digit-to-digit only, strike or vertical alignment differing, no whitespace or punctuation between; letters never |
| D7 | Oracle | Word's text saves from the share (the files WD-001 let Word write) are the goldens, byte for byte under `word-text` |
| D8 | Offsets | The block model and every address keep the stored text; uppercase is the writers' (REQ-607), and the mapping preserves length |
| D9 | Order | Phases 0 to 2 before 17.3.5 ships if Jason wants the caps change in it (it changes plain-text output for every document with `w:caps`); phases 3 and 4 may follow in 17.3.6 |

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

## 8. Hand-offs

- **The portfolio**: the registry entry (`docx4j/CR-069`, proposed); WD 0.44 is the normative text this CR builds to.
- **docx4j-python, docx4j-core-ts**: at phase 1 (flags, mapping, goldens) and phase 2; the mapping note above.
- **docx4j-mcp**: the flags appear in the block model it returns; the kind in inspections; no contract change.
- **docx4j-tika**: at phase 3; its `TikaParityIT` expected tokens move (capitals) and its XHTML gains the tags.
- **CR-068**: this CR's finding is the pointer to that CR's conversion; nothing else shared.

## 9. Sources

- The paper, `../docx4j-portfolio/docs/text_extraction_behaviour_model.md` §7.13 (the DPP review, the rows, the
  measurements, the seventy caps-lang probes); `docs/word_defects.md` WD-001.
- WD 0.44 (uncommitted at drafting): REQ-521, REQ-607, REQ-710, §9.2 `text.formatting_join`, §4 NOTE, Appendix B.
- The probes and Word's saves: `~/vbShares/Office 2016/dpp-forks/` (`dpp-forks.docx`, `dpp-forks-word365.docx`,
  `dpp-forks.pdf`, `bisect/`, `caps-lang*.docx` and `.txt`).
- CR-046 (the text model), CR-050 (the inspector; §2.3 the kinds table), CR-068 (manual redline).
- ECMA-376 Part 1 §17.3.2.5 `caps`, §17.3.2.33 `smallCaps`, §17.3.2.42 `vertAlign`, §17.3.1.11 `framePr`
  (`dropCap`, `lines`); clause numbers to be checked at phase 0.
