# The compact identification probe

The fewest files that hold the 21 cases separating every pair of readers docx-tool-identifier's library
separates. The brief is from its CR-001 phase 2 (§11; `dti discriminate`, 2 November 2026): 106 reader
configurations, 85 probes, 5,174 pairs. Built by `CompactProbeBuilder` (docx4j-core-tests,
`org.docx4j.textextraction`), run from the repository root:
`CompactProbeBuilder docx4j-core-tests/src/test/resources`. A rebuild is byte-identical.

## Files

| File | Holds |
|---|---|
| `compact-probe.docx` | The token cases 4, 5, 15, 21, 25, 30, 32, 33, 36, 38, 84, 85, and the fields R08, R18, R14. |
| `compact-probe-updatefields.docx` | `w:updateFields`, so Word recomputes every field on open. The token cases 6 and 5, and the fields C04, C16. |
| `../../fields/cr067-iffield/round8/corpus/iffield-probe-r8-printdate.docx` | Round 8's PRINTDATE candidate, reused unchanged: its Y01, and the controls Y02 to Y05. |
| `expected-tokens.tsv` | The token cases' rows, copied from their source tables (phase 0's columns). Only `fixture` changes. |
| `compact-probe.values.tsv`, `compact-probe-updatefields.values.tsv` | The field cases' rows, copied from round 2's and round 1's tables. Each token is an anchor, and the value follows it. |
| `expected-findings.tsv`, `goldens.tsv` | Empty. `ExtractionConformance` reads them. |
| `*-word365-*.txt` | Word's saves from the open check (below). |

Case numbers are the sources'. Two cases are 5 (caps-lang5 and caps-lang7d), so each sits in a different file.

## Cases

| # in the brief | File | Case | Source | Tokens or anchor |
|---|---|---|---|---|
| 1 | content | 25 | 38-case probe | `TBX25_FLOAT`, `_INLINE`, `_VML_ONLY`, `_IN_CELL`, `_IN_HEADER` (section 1's default header), `WA25_TEXTPATH` |
| 2 | content | 84 | opaque fixture | `PDF84_ACROBAT_TEXT` (`opaque84.bin`, the committed bytes) |
| 3, 10 | content | 21, 4 | 38-case probe | `FLD21_*`; `CHOICE1_UNKNOWN_ZETA`, `CHOICE2_WPS_ETA`, `FALLBACK_EPSILON`. The identifier derives the decoyct package from this file. |
| 4, 21 | content | R18, R08 | IF-field round 2 | `R18_DOCVARIABLE`, `R08_OVER_PAGE` (on page 1, as in round 2: an IF over PAGE) |
| 5, 20 | updateFields | C04, C16 | round 1 with `w:updateFields` | `C04_DATE_COND`, `C16_WILDCARD` |
| 6 | content | 5 | dpp caps-lang5 | `ROBERTS`, `JACKSON`, `H₂O` |
| 7 | printdate | Y01 | round 8 | `Y01_PRINTDATE` (the identifier's round 8 rules) |
| 8 | content | 85 | opaque fixture | `PIX85_DRAWN_TEXT` (`drawn85.png`, the committed bytes) |
| 9, 11, 13, 14, 15, 18 | content | 32, 15, 36, 38, 30, 33 | 38-case probe | as in its table |
| 12, 17 | content | 36, 33 | 38-case probe | The identifier derives the allmoved package from this file. |
| 16 | updateFields | 6 | dpp bisect C06 | `H2O`, `H<sub>2</sub>O`, `x<sup>2</sup>`, `1<sup>st</sup>`, `H₂O`, `x²` |
| 19 | updateFields | 5 | dpp caps-lang7d | `ﬁNE CAFÉ Ǆ`, `FINE`, `ǅ` |
| (added) | content | R14 | round 2 | `R14_SIMPLE_DIRTY`: its `w:dirty` makes Word ask on open whether to update fields. The asis reading answers No, the dirty-yes reading Yes, as in round 2. |

## Where the cases are, and why

- **The 38-case and opaque cases** are written by the builders' own helpers, so each case's XML is its
  source's apart from drawing, shape, revision and relationship ids. This was checked against
  `extraction-probe.docx` (4a89a5967) for every case. The caps, C06 and field paragraphs are copied from
  their source files.
- **The parts the cases use are copied byte for byte from the committed sources:** case 33's pictures and
  SVG, case 36's altChunk, case 38's core properties and custom XML part, case 84's storage and preview,
  and case 85's PNG.
- **WD-001 (docx4j-portfolio `docs/word_defects.md`).** Word's Save As Plain Text writes nothing, and says
  nothing, when a character outside Windows-1252 lies at character 256 or later, counting from 0 with a paragraph
  mark as two: a stored character, a case-mapped one or a `w:sym`'s, wherever its run starts. (The rule as first
  written, a run starting at 246, was revised on 4 November 2026.) So caps-lang5 comes first in the content file
  (its `₂` run starts at 145). In the updateFields file C06 comes first (96), then caps-lang7d (152). The builder
  fails if any such character is placed later.
- **The literal `H₂O` of caps-lang5 and of C06** are in different files, so each file's count is its source's.
- **R08** is on page 1, before case 30's page break.

## docx4j's check

`ExtractionConformance` over this folder (`CompactProbeTest`) gives 213 rows passing. The three that pend
are case 36 with an HTML reader present, as on `extraction-probe.docx`. So every copied row reads in its
compact file as it does in its source.

## Word

Jason ran the check on both files on 2 November 2026, in Word for Microsoft 365 on the VM (build 20326.20232 when
last recorded, 28 October). He reported no repair message. Each file saved as Plain Text (windows-1252, CR LF), so
WD-001 did not trip. Case 30's `w:noBreakHyphen`
came out as Word's 0x1E, not as a character outside the code page.

| Save | File | What it shows |
|---|---|---|
| `compact-probe-word365-asis.txt` | the content file; "update fields?" answered No | Nothing recomputed: R08 `PAGE_OTHER`, R18 `VAR_X`, R14 `3`, as in round 2's as-is save. Caps-lang5's lines are the same as its own save (`JACKSON`, `ROBERTS`, `H?O`). |
| `compact-probe-updatefields-word365-yes.txt` | the updateFields file; answered Yes | C04 `VALID` and C16 `WILD_MATCH`, as in round 1's updatefields-yes save. C06's line is the same as its bisect save. Caps-lang7d is `?NE CAFÉ ?` in the code page (its source was saved as UTF-8). |

Every case line in these saves is the same as the line in Word's save of the source file.

Jason's further readings the same day, for docx-tool-identifier (each taken as for its source):

| Save | File | How |
|---|---|---|
| `compact-probe-word365-asis-utf8.txt` | content | No to the prompt; Save As Plain Text, UTF-8 with BOM |
| `compact-probe-word365-f9.txt` | content | No; Ctrl+A, F9; Save As Plain Text |
| `compact-probe-word365-dirty-yes.txt` | content | Yes to the prompt (the w:dirty field); Save As Plain Text |
| `compact-probe-word365.pdf` | content | No; Save As PDF |
| `compact-probe-updatefields-word365-asis-utf8.txt` | updateFields | No; Save As Plain Text, UTF-8 with BOM |
| `compact-probe-updatefields-word365-f9.txt` | updateFields | No; Ctrl+A, F9; Save As Plain Text |
| `compact-probe-updatefields-word365-no.txt` | updateFields | No; Save As Plain Text |

The identifier found every one equal to Word's reading of the source on every case both measured.

## Known differences from the sources

docx-tool-identifier's local check (2 November 2026; 80 configurations over the two files and the rewired
packages) separates every pair of locally run readers the library separates. 25 of 2,636 readings differ from
their source case, and none of them costs a pair:

- **Case 25's header box** is counted 3+ by LibreOffice's PDF and liteparse (1 in the source), and 1 in docx4j's
  HTML (0 in the source). Here the header is the only section's default. In the 38-case probe it is section 1's,
  of two, beside a first-page header.
- **`H₂O` (caps-lang5 and C06), C06's `H2O`, and caps-lang7d's ligature line** go from 0 to 1 in docx4j's PDF and in
  liteparse. These files default to Calibri; the dpp files use Word 365's theme fonts.
- **Pandoc 3.6.1 and 3.12.1** put R08's nested `PAGE` result (`7`) in place here, but at the start of the paragraph
  in round 2's file. The XML is the same apart from round 2's indentation.
