# CR-046 phase 0 suite and golden protocol

The original 38-case probe, its README/TSV and its checked Word re-save remain in this
folder unchanged. `phase0/` contains the replacement 54-case suite. Jason confirmed the
earlier 52-case r2 main DOCX opens without repair on 18 October 2026, declining field updates. The external
DOCM check also passed after declining its blank mail-merge SQL prompt; no repair was reported. Jason also completed the main DOCX r3 check with cases 53–54; the external DOCM is unchanged.
Use `phase0/expected-tokens.tsv` with its two named input files.
Do not use the original Word re-save as a re-save of the extended probe.

The builder regenerates both documents, README, token and finding tables, and case
statement mappings together. Build into a scratch directory first; JAXB namespace
ordering means that rebuilt document bytes need not match checked files. The checked extended
suite remains in `phase0/`, preserving the original suite and its matching Word re-save.
`support/appendix-a-statements.tsv` is an independent transcription of WD 0.3 Appendix A
(portfolio e8f14e1); `support/additional-case-statements.tsv` records Jason's approved
53–54 additions against WD 0.5. The test compares every case's mapping and README rows
against those independent tables.

## Build and run

Run from the repository root. With the repository's dependencies installed:

```
mvn -q -pl docx4j-core-tests test-compile
mvn -q -pl docx4j-core-tests dependency:build-classpath -Dmdep.outputFile=cp.txt
```

Use `docx4j-core-tests/target/test-classes` plus that classpath to run
`org.docx4j.textextraction.ExtractionProbeBuilder <scratch-output-directory>`.
`run-phase0.sh <classpath> <scratch-output-directory>` also compiles and runs the builder
against the brief's released 17.3.3 core/JAXB jars. The classpath must include logback.
It compiles the helper sources together; Java's single-source-file launch is no longer sufficient.
The VBA support file, LegacyForms.docx ActiveX seed and Hier2Level.glox are read relative to the repository root.

```
mvn -pl docx4j-core-tests -Dtest=ExtractionConformanceTest test
```

This validates both packages and the fixture protocol and writes
`target/text-extraction/conformance.tsv` (relative to the test working directory).
A standalone run of `ExtractionConformance <fixture-directory> <report.tsv>` does the
same extraction checks. Every unsupported assertion appears as PENDING with its
requirements and implementation phase. No extraction conformance is claimed by phase 0.

Phase 1 supplies the `ExtractionConformance.Engine`: `TextsAdapter`, the default
(`-Dtextextraction.adapter=<public-class-with-no-arg-constructor>` names another;
`-Dtextextraction.adapter=none` runs the protocol checks with every row pending). The adapter calls
`Texts`, translates the scenario into typed options, and advertises only implemented
statements (CR-046 phases 1 and 2's). A scenario's option needs its own statement too (WD section 10's
table: `embedded: true` needs REQ-512, whatever the row cites). A scenario may combine options, `; `
apart (`embedded: true; hidden: true`, case 53's hidden slide): the adapter sets each, and the row needs
each one's statement. Scenario `default` has no HTML reader. `html reader present/absent`
requires the corresponding classpath; it is not a TextOptions key. Each is a capability the adapter
advertises when an HTML reader is (or is not) on the class path, for docx4j jsoup and docx4j-ImportXHTML
both (CR-053, WD 0.27, which folds the earlier `xhtml importer present/absent` into it), so a run
checks one of the two. The goldens are cut without a reader, so a golden is checked only by an engine
advertising `html reader absent`, and a findings, order or golden row needs its scenario's
capability as a token row does (19 October 2026: a run with the importer present had checked the
importer-absent rows, and compared case 44's expanded MHT chunk with goldens that hold none). Missing advertised capabilities remain pending. Exceptions from enabled assertions
fail the test. The axis-title row additionally requires capability `CR-047`. Output finding keys are
`channel<TAB>kind<TAB>selector`; values are counts or literal metadata values.

An inspector (CR-050) also implements `Engine.inspect(fixture)`, returning its findings with the
columns of `phase0/expected-inspection.tsv`, the fixture's status, what it skipped and could not
decide, and its limitations. Every inspection row (the `inspect` channel's (kind, selector) counts,
checked once, the view being irrelevant to an inspection, and each fixture's exact comparison) needs
REQ-901, REQ-902 and REQ-904 to REQ-907 (WD 0.17's section 9), and stays pending until an engine
advertises them (CR-050 phase 1).
The exact comparison is by occurrence: the multiset of (kind, chain, part, path, start, end_path,
end), then each finding's address, visible, reveal, groups and excerpt length, then the status. Its
contract, and how the tables are made, is `INSPECTION.md`.

## Expected tables

Token rows contain case, token, accepted/original/markup expectation, gating option,
with-option expectation, explanatory note, fixture and REQ statements. Numeric
expectations are exact occurrence counts. `ins`, `del`, `cmt`, and `del+ins` require
CriticMarkup of that kind before normalization, as well as the correct total count.
Default MHT counts refer to importer absent; the gating scenario refers to importer present.
Warnings, inspection findings and metadata live separately in `expected-findings.tsv`.
A warning selector is its chunk content type; an inspector's external selector is its target.

Comparison follows REQ-1201: strip whitespace, CriticMarkup delimiters, Markdown emphasis
characters, backslash, braces and dollar sign from both text and marker. An occurrence inside a
longer marker token of the suite is not counted: once underscores are stripped, `FLD19_RESULT` is
also found inside `FLD19_RESULT_TWO`, and `MCE8_OUTER` inside `MCE8_OUTER_FALLBACK` (found by
CR-046 phase 1, 18 October 2026; the ports' harnesses need the same rule). No Unicode
normalization, hyphen folding or removal of U+200B/tag characters is performed.
Case 48 additionally checks the tag-character suffix verbatim. Supplemental scenarios
pin nested exclusions, disabled altChunks, and non-evaluation of external fields.

## Published goldens (REQ-1203)

`phase0/goldens.tsv` is the manifest. UTF-8, tab-separated, with this header:

```
fixture	case	view	scenario	format	golden	statements
```

Views are `accepted`, `original`, `markup`; formats are `txt`, `md`. Case `0` means a
whole-fixture comparison. Paths are relative to the fixture directory. Plain-text names
are `<stem>.accepted.txt`, `<stem>.original.txt`, `<stem>.markup.txt`; the Markdown
is `<stem>.md`, accepted/default unless its manifest row specifies otherwise. Files use
UTF-8 without BOM and LF, with one terminal LF. A row names all implementation statements
needed for that golden, so it can remain pending before its phase is enabled.

The reader compares the complete serialization after REQ-1201 normalization and fails
on any difference. Tests exercise both a matching golden and a deliberate mismatch.
A Markdown golden's image destinations are not compared: WD REQ-703 writes `![alt](…)`,
leaving the destination to the implementation (docx4j writes a data URI, or a file under
`imageDirPath`), so the golden stores `…` and the reader writes every image's destination
`…` on both sides before normalizing; the alt text is compared.
The builder preserves an existing golden manifest.
Legacy baseline outputs must never be copied into this manifest as conformance goldens.

**The goldens (cut 18 October 2026, CR-046 phase 3).** For each of the two fixtures, its
plain text in the three views (`extraction-probe.accepted.txt`, `.original.txt`,
`.markup.txt`; `extraction-probe-external.*`) and its Markdown in the `accepted` view
(`extraction-probe.md`, `extraction-probe-external.md`), all under the defaults (no
HTML reader; recut 20 October 2026 for CR-053 phase 2, which leaves case 36's HTML chunk unexpanded without
one: the diff was case 36's line alone). They were cut by `ExtractionGoldens <fixture-directory>` (docx4j-core-tests'
test classes; it writes the files and the manifest through `TextsAdapter`), and reviewed by
hand against WD 0.10 sections 3 to 7 before they were committed. A recut is a change to what
the ports are held to: cut into a scratch copy of the directory, diff against the committed
files, and review each difference before replacing them.

**The third fixture's goldens (cut 19 October 2026).** `extraction-probe-structure.docx` (cases 55
to 57, `ExtractionStructureCases`) has the same four: `extraction-probe-structure.accepted.txt`,
`.original.txt`, `.markup.txt` (the three alike: it has no tracked changes) and
`extraction-probe-structure.md`, whose lists are REQ-703's interrupted-list rules (WD 0.14). They were cut
the same way, the other eight cutting byte-identical beside them, and reviewed against WD 0.15. The
fixture is its own file so that the main probe is never rebuilt: the builder writes all three
fixtures, but only the new one was copied in (the committed `extraction-probe.docx` and `.docm` are
the files Word checked).

## Legacy baseline

`phase0/baseline/` records all three readers' defaults for each authored fixture, with
raw output and per-marker counts (including zeroes), grouped by case in `counts.tsv`.
TextUtils receives the main document JAXB element; Markdown and HTML receive a fresh
package per reader with their normal defaults. HTML uses an inline-image handler;
its text count excludes attributes, scripts and styles, with a separate raw-serialization
count for diagnosis. Markdown's count is against its serialization, including link targets.
A failed conversion writes a `.failed.txt` and ERROR rows rather than zero counts.

Capture explicitly with `ExtractionBaseline <fixture-directory> <output-directory>`,
adding docx4j-markdown and its commonmark dependencies to the runtime classpath.
The recorded baseline uses core and Markdown sources from docx4j 23cb5bf662c10e4b459944a71f8ae860fca964a1,
compiled with `javac --release 11`, run on Java 21.0.12. All 1,420 core source files
were compiled into a fresh scratch directory; existing Eclipse output was not used.
Released 17.3.3 JAXB/generated-object dependencies supplied the fixture APIs.
`manifest.txt` records fixture hashes, class hashes, source-tree hashes and classpath provenance.
These measurements describe existing behavior, not WD expectations.

## Office check

Follow `phase0/RUN.txt`. No PDF, layout measurements, Office JS, field evaluation or
macro execution is needed. The two authored files must open without a repair prompt.
Security/update prompts are recorded separately. Keep returned saves separate from inputs.

## Specification notes for the portfolio hand-off

Case 39 intentionally pins the brief's narrow embedded-XLSX behavior despite CR-046's
broader SpreadsheetML exclusion. This needs an explicit wording exception in that CR.
Cases 40 and 47 test same-kind nesting at one level. Cross-kind recursion depth remains
unspecified and is not silently decided by these fixtures. Case 41 is implemented;
there was no need to use the allowed one-hour drop exception.

Both questions are answered in WD 0.4 (portfolio 2062e86, 18 October 2026), and CR-046 follows
it. REQ-512 now defines how an embedded workbook is read (each sheet's used range as a simple
table, after its name), the one SpreadsheetML reading in scope. Nesting is one level across
kinds: content reached through an embedded package or an `altChunk` has neither expanded
(REQ-512, REQ-513).

## Word repair and r2 retry (18 October 2026)

Jason reported unreadable content in the first extended DOCX and supplied its repaired save.
The exact input and returned file are retained in `phase0/evidence/`, with the comparison
findings there. Word discarded case 42's control, leaving its preview picture; all three OLE
objects survived. Word expanded case 47 and its nested chunk, while case 46 became literal
XML text. This evidence points to the control, but does not establish the cause independently.

The r2 retry changes case 42 to Word-authored Label control storage with the marker as its
caption, gives case 46 an XML declaration and the Word processing instruction emitted by
FlatOpcXmlCreator, and corrects case 47's import MIME type. The last correction is required by
[Microsoft's SDK definition](https://github.com/dotnet/Open-XML-SDK/blob/main/src/DocumentFormat.OpenXml/Packaging/AlternativeFormatImportPartType.cs),
which uses `application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml`
for WordprocessingML altChunks; embedded DOCX packages retain their package MIME type.
A regression test rejects the previous mismatch.

Only four existing ZIP members change: content types, the case 42 heading, ActiveX XML and
Flat OPC payload. Two members are added for ActiveX binary storage and its relationship.
All other input members are byte-identical. Baselines were recaptured for the r2 bytes.
Jason confirmed `extraction-probe-r2.docx` opened fine on 18 October 2026 and declined
field updates. The checked bytes are archived in `phase0/evidence/checked-52/extraction-probe.docx`; the main DOCX open
check passes. Jason then completed the external DOCM check after declining the blank SQL
prompt, with no repair problem reported. Both open checks pass. Word's saves of both r2 files
are retained in `phase0/evidence/`; the main save drops case 42's control again. The full Word
build is still not recorded. See `phase0/evidence/word-open-results.md`.


## Cases 53–54 (18 October 2026)

Jason requested both additions after the 52-case suite's Word checks passed. Their new input
is staged as `extraction-probe-r3.docx`; the canonical authored copy is
`phase0/extraction-probe.docx`. The external DOCM remains byte-identical and retains its check.
The checked 52-case input and catalogues are preserved under `phase0/evidence/checked-52/`.

Case 53 embeds an actual PPTX with two visible slides, a hidden slide between them, speaker
notes, nested groups, and a 2x2 table. Visible markers are absent in every default view and
appear once under `embedded: true`; the hidden-slide and speaker-note markers stay absent.
The glossary in case 54 has two entries, excluded by default and included once each under
`include: glossary`. It must follow endnotes and keep entry order.

`expected-order.tsv` adds whole-output ordering assertions in all three views. Its columns
are case, fixture, scenario, tokens (separated by ` > `), and statements. Case 53 pins
slide/shape/depth-first/table-cell order; case 54 pins endnote-before-glossary and entry order.
These assert exact single occurrences and sequence after REQ-1201 normalization. Tests enable
an adapter to prove the order assertions execute, and deliberately reverse shapes to check
that incorrect ordering fails. The fixture checks also read the nested PPTX package and verify
its slide list, hidden state, groups, table, notes, and glossary payloads.

WD 0.6 (portfolio 97fce95) records these cases in Appendix A.2. The approved additions map to:

| Case | Construct | Statements |
|---|---|---|
| 53 | Embedded presentation, slide order, groups, table, hidden slide and notes | REQ-512, REQ-605 |
| 54 | Two glossary entries, opt-in inclusion after endnotes | REQ-501, REQ-516 |

WD 0.6 adopted these rows.


### R3 Word result

Jason completed the requested r3 open/save check on 18 October 2026; no repair issue was
reported. Its returned `extraction-probe-r3-saved.docx` is retained as
`phase0/evidence/extraction-probe-r3-word-save.docx`. Word preserves the embedded presentation
bytes exactly, renaming its part to `Microsoft_PowerPoint_Presentation.pptx`. All slide/group/
table/hidden-slide/notes markers remain in that package. The glossary part and its relationship
are absent from the save, with neither glossary marker inserted into the body. The cause of
that loss is not established. The authored fixture still contains the glossary and remains
the conformance input; Word's save is evidence of its behavior, not the expected extraction.
The full Word build remains unknown (`AppVersion` is only 16.0000).
