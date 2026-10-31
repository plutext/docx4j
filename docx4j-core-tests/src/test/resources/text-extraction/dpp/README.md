# CR-069's suite: what the page shows, in the text

The fixtures of docx4j CR-069 (case mapping, drop caps, superscripts and the formatting-join finding), with Word's
own text saves as the oracle. They come from the portfolio's review of LlamaIndex's Document Parsing Protocol
(docx4j-portfolio `docs/text_extraction_behaviour_model.md` section 7.13, commit 09502ce) and Jason's runs in Word
for Microsoft 365 (build 20326.20232) on 31 October 2026, copied from the share (`Office 2016/dpp-forks/`) the same
day. The statements are the WD's (0.44 and later): REQ-521 (the drop-cap join), REQ-607 (`w:caps`, `w:smallCaps`),
REQ-710 (`<sup>`, `<sub>`), section 9.2's `text.formatting_join`, and REQ-1002 (`profile: word-text`).

## Files

- `dpp-forks.docx`: the probe as built, ten cases between the markers `C01_` and `C10_`: C02 a drop cap
  (`w:framePr/@w:dropCap`), C03 a struck value beside an underlined one in prose, C04 the same in a table cell with no
  space, C05 `w:smallCaps` on `Jackson` and `w:caps` on `Roberts`, C06 subscript and superscript runs (`H2O`, `x2`,
  `1st`) beside the literal characters `x²` and `H₂O`, C07 a superscript `3` after `48,210`, C08 and C09 black text on
  a black highlight and a black shading, C10 a red run.
- `dpp-forks-word365.docx`: Word's re-save of it, unedited. Every row is on this file.
- `bisect/`: each case alone (`C02.docx` to `C10.docx`, `control.docx` with none) and Word's Save As Plain Text of
  each (`.txt`, the default encoding, windows-1252, CR LF), with the request Jason ran (`README.txt`). Word saves no
  text file of the whole probe and shows no message (WD-001, docx4j-portfolio `docs/word_defects.md`), so its text is
  known case by case.
- `caps/`: Word's measurements of the case mapping, one property and one language at a time, and Word's saves of each
  (`.txt` windows-1252; `-utf8.txt` UTF-8 with a byte order mark, chosen in Word's File Conversion dialog where the
  code page could not show the answer): `4b` `w:lang` tr-TR alone; `5` C05 beside a literal `₂`; `6a` caps under
  tr-TR and en-US and small caps under tr-TR; `6b` caps on `straße` (de-DE), `café` (en-US) and with no `w:lang`
  (the document default, en-US); `7b` `straße`; `7c` tr-TR caps; `7d` the ligature `ﬁ` and the digraph `ǆ`; `7e`
  tr-TR small caps; `7f` and `7g` caps and small caps in the tenth paragraph. The probes Word refused to save as text
  (WD-001's) are not here.
- `expected-tokens.tsv`, `expected-findings.tsv`, `goldens.tsv`: phase 0's protocol (`../GOLDENS.md`), the same
  columns, fixtures named relative to this folder. Case numbers are the probe's (5 for every caps probe).
- `word-saves.tsv`: Word's saves against the text under `profile: word-text` (below).
- `dpp-forks-word365.accepted.txt`, `dpp-forks-word365.md`: the whole fixture's plain text and Markdown in the default
  options, cut by hand on 31 October 2026 from docx4j 17.3.5-SNAPSHOT's output (1a0b5315c) with CR-069's three
  changes made: the drop cap joined (`The C02_ modern`), `ROBERTS` in capitals, the tags in Markdown.

## How the rows are checked

`ExtractionConformance <this folder> <report.tsv>` runs the four tables (docx4j: `ExtractionConformanceTest`'s
`dppSuiteIsCheckedOrPendingOnItsPhase`). A row is checked once the engine advertises its statements, and is reported
pending until then with the CR-069 phase that builds it: REQ-607 phase 1, REQ-521 phase 2, REQ-710 phase 3, the
capability `text.formatting_join` phase 4. What holds before CR-069 (Word's saves of the cases it does not touch,
the inspector's `text.near_background`, the stored characters) is checked now.

REQ-1201's comparison removes whitespace, so no token or golden can tell a joined drop cap from two paragraphs. The
join is checked by the warning `paragraph.drop_cap_joined` (`expected-findings.tsv`), by Word's C02 save under
`word-text` (which sets `drop_caps: asis`: `T`, then `he C02_ ...`), and by CR-069's own tests. A
`text.formatting_join` row's selector is its excerpt, the joined token (`1.3161.335`, `48,2103`): phase 4 gave the
finding no subject (CR-069 section 14), so the four rows stand as phase 0 wrote them.

## Word's saves (`word-saves.tsv`)

Columns `fixture`, `case`, `save`, `encoding`, `scenario` (`profile: word-text`), `statements`, `note`. The
comparison is WD REQ-1002's NOTE made exact:

1. The save is decoded by its byte order mark (`encoding` `UTF-8`, which must have one), else in the saving system's
   ANSI code page, which the row names (`windows-1252`, and the file must have no mark).
2. The extraction is passed through the same encoding, so that a character the code page lacks is Word's `?` on both
   sides (`H?O` for a literal `₂`, `?STANBUL F?LM` for tr-TR capitals).
3. Both are split into lines at CR LF, CR, LF and tab, and trailing empty lines dropped: Word ends a paragraph CR LF
   and a cell CR, where the specification writes LF and a tab (the residue the NOTE names). Every character and every
   break between paragraphs or cells must then agree.

The files are Word's bytes and must stay so: `.gitattributes` marks them `-text`.
