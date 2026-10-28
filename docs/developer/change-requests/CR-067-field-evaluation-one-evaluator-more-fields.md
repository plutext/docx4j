# CR-067: one field evaluator, and the fields worth adding

Status: ACCEPTED 2026-10-28 (Jason: "CR decisions as recommended; please commit then start implementation"): D1 to D11 as recommended, D2 (c); phase 0 DONE 2026-10-28 (§10); phase 1 DONE 2026-10-28 (§11; Word's measurement in §11.3, every case as predicted); phase 2 next.
PROPOSED 2026-10-28, drafted by Claude Fable 5.1 (session docx4j-12) at Jason's word ("draft the CR,
including your review as context"), from a review of `FieldsPreprocessor` and the field code around it (§1, §2) and
of the ECMA-376 field set by category (§3). Recommendations in §5; phases in §6. D2 ruled 2026-10-28 (FldSimpleModel deprecated). Nothing built.

## 0. Summary

docx4j evaluates fields in three places that do not share code: `FieldUpdater` (in place, for DOCPROPERTY, DOCVARIABLE
and the document-information fields), the inspector's `FieldValues` (CR-050, for those and SEQ, REF, QUOTE, the
core-property dates), and the output path's `FldSimpleWriter` handlers (DATE, TIME, PRINTDATE, DOCPROPERTY, and the
layout fields PAGE, NUMPAGES, SECTIONPAGES, PAGEREF, REF, STYLEREF). Each has its own instruction parser. Adding a
field today means adding it three times, and the three already disagree on which fields exist.

The proposal is one evaluator, consumed by all three (§4), built as phase 0 from the code that exists, and then
fields added in tiers by cost and value (§3, §6): the ones one evaluator already handles spread to the others first;
then IF, COMPARE and the `=` formula, which are the highest-value gap and the one category with nothing done; then the
generated and counted fields. `FieldsPreprocessor` keeps its job as the canonicaliser of the complex form; it is sound
for that and its limits are known (§2).

## 1. Today: three evaluators, three parsers

**The in-place path.** `FieldsPreprocessor.complexifyFields` turns every `w:fldSimple` in a part into the complex form
by XSLT (`FieldsSimpleToComplex.xslt`, a DOM round trip of the whole part); `canonicalise(P, List<FieldRef>)` then
rewrites each paragraph's fields into one shape (a begin run, the instruction runs, a result slot, an end run), with
a nested field in the instruction part kept as a child `FieldRef`, and every result kept as read (since 17.3.5). Its
callers: `FieldUpdater` (DOCPROPERTY, DOCVARIABLE, AUTHOR, COMMENTS, KEYWORDS, LASTSAVEDBY, SUBJECT, TITLE),
`FieldUpdaterSEQ`, `AbstractMerger` and the two mergers (MERGEFIELD, NEXT, FORMTEXT), the migrators `FromMergeFields`
and `FromFormText`, and the TOC `CSwitch`.

**The output path.** `FieldsCombiner` goes the other way: it folds complex fields into `w:fldSimple` (splitting a run
that holds a whole field header first; dropping a resultless IF, since nothing can evaluate it and its branches would
otherwise be painted; keeping a resultless PAGEREF, PAGE, NUMPAGES or SECTIONPAGES, since the renderer computes it).
`FldSimpleModel` parses the instruction; `AbstractFldSimpleWriter`'s handlers paint by name, the rest as the cached
result. HYPERLINK has its own writer; STYLEREF in a header or footer is `StyleRefMarkers`.

**The inspector.** `org.docx4j.model.text.FieldValues` computes, under `fields: update` (text extraction WD REQ-506),
"only the fields that need neither layout nor external access": DOCPROPERTY, DOCVARIABLE, SEQ (`\r`, `\c`, `\h`),
REF to a bookmark (plain), QUOTE, and AUTHOR, TITLE, SUBJECT, COMMENTS, LASTSAVEDBY, KEYWORDS, REVNUM, CREATEDATE,
SAVEDATE, through the same resolvers and `FormattingSwitchHelper`.

**Three parsers of one grammar.** `FldSimpleModel.setupNameParameterString` (name, then a parameter list split on
spaces with quoting), `FieldRef.getFldName` through `FormattingSwitchHelper.getFldSimpleName` (the text before the
first nested field, since #634), and `FieldsCombiner`'s instruction buffer. The only registry of field names is the
anonymiser's `FieldInstructions`. TOC stands apart, rightly: `TocGenerator` builds a TOC from the heading paragraphs
and the renderer supplies its page numbers; it is a layout product, not a field evaluation.

## 2. FieldsPreprocessor reviewed

The idea is sound and has held for a decade of edge cases: one canonical shape, results preserved, nesting modelled
only where it matters (the instruction part; a nested field in a result is regenerated with the result). Its limits,
in order of weight:

1. **Paragraph scope.** A field whose begin and end are in different paragraphs is never canonicalised (the class
   says so). An IF with a paragraph break in a branch, INCLUDETEXT, and any block-level conditional are out of reach.
   This is the ceiling for anything beyond leaf fields.
2. **Dead branches.** `preserveResult` became a constant `true` in 17.3.5 (CHANGELOG, #697 follow-ups), so every
   `inParentResult` / `preserveParentResult` branch takes the same path. Folding them halves the state machine and
   removes the hazard its own comment warns of ("newR persists between invocations").
3. **Hyperlinks** are not descended into: a field inside `w:hyperlink` (a TOC's PAGEREF, a REF in a link) is left
   alone. The combiner on the output side handles that case.
4. **The XSLT step** exists only to make simple fields complex. The state machine could read `w:fldSimple` natively
   and the DOM round trip would go.
5. **Results with several runs** are flattened to one on `setResult` ("Multiple runs in results slot?"). Right for a
   merge value; wrong for a REF to formatted text.
6. `ProofErr` is dropped; everything else not a run is kept in place, which is what keeps bookmarks right.

None of these needs fixing for today's callers. They are the shape of what can be added without rework (leaf fields
whose result is text), and what cannot (block-level fields, formatted results).

## 3. The field set, by ECMA-376 category, against what docx4j does

ECMA-376 Part 1 §17.16.5 (the online copy's §2.16.5 numbering) groups the fields as below. "Evaluated" is by any of
the three evaluators of §1, named where it matters. The tiers are §5's: 1 low cost and real value, mostly spreading
what one evaluator already does; 2 medium cost, the highest value; 3 medium cost, medium value; "cached" means the
field keeps its stored result and this CR proposes nothing.

| Category | Evaluated today | Tier 1 | Tier 2 | Tier 3 | Cached |
|---|---|---|---|---|---|
| Date and Time | DATE, TIME, PRINTDATE (output); CREATEDATE, SAVEDATE (inspector only) | CREATEDATE, SAVEDATE everywhere | | | EDITTIME |
| Document Automation | DOCVARIABLE | | IF, COMPARE | | GOTOBUTTON, MACROBUTTON, PRINT |
| Document Information | DOCPROPERTY, AUTHOR, COMMENTS, KEYWORDS, LASTSAVEDBY, SUBJECT, TITLE; NUMPAGES (output) | INFO (the same lookup under one name) | | FILENAME, TEMPLATE (a supplied name); NUMWORDS, NUMCHARS (the text model) | FILESIZE |
| Equations and Formulas | none | | `=` formula | SYMBOL | ADVANCE, EQ |
| Form Fields | FORMTEXT, FORMCHECKBOX, FORMDROPDOWN (output; the mergers) | | | | |
| Index and Tables | TOC (the generator); XE, TC hidden at output | | | INDEX from XE marks | RD, TA, TOA |
| Links and References | HYPERLINK, PAGEREF, REF (output); REF, QUOTE (inspector); STYLEREF (headers); INCLUDEPICTURE (a sample) | REF, QUOTE everywhere; NOTEREF | | | AUTOTEXT, AUTOTEXTLIST, BIBLIOGRAPHY, CITATION, INCLUDETEXT, LINK (D8) |
| Mail Merge | MERGEFIELD, NEXT (the mergers) | | NEXTIF, SKIPIF, SET, ASK, FILLIN, MERGEREC, MERGESEQ (after IF) | ADDRESSBLOCK, GREETINGLINE composed from their switches | DATABASE |
| Numbering | PAGE, SECTIONPAGES (output); SEQ (its updater; inspector) | SECTION, REVNUM | | LISTNUM, AUTONUM, AUTONUMLGL, AUTONUMOUT (the CR-014 model) | BARCODE |
| User Information | none | USERNAME, USERINITIALS, USERADDRESS (from the context) | | | |

Two things the table shows. Tier 1 is mostly plumbing: the inspector already computes half of it. And Equations and
Formulas, the one category with nothing done, is what tier 2 depends on: IF needs the same expression code as `=`.

## 4. Proposal: one evaluator

- **`FieldInstruction`** (D2): one parser for the grammar the three share, replacing `FldSimpleModel` (deprecated, Jason 2026-10-28). Name; arguments, quoted or bare; switches
  (`\x`, with a value where the grammar gives one, `\* MERGEFORMAT` and `\* CHARFORMAT` among them); and a placeholder
  per nested field, in order, so an outer field's evaluator sees where a nested result goes. `FldSimpleModel` keeps
  its API and delegates; `FieldRef.getFldName` and `getInstr` delegate; the combiner's buffer becomes one.
- **`FieldContext`** (D10): what an evaluator may ask for. The package and the part; the field's position (for SEQ,
  SECTION, NOTEREF, a REF's bookmark text); the document's language (number and date formats); a clock (DATE, TIME);
  the user (USERNAME, USERINITIALS, USERADDRESS); a file name; a merge record; a resolver for external content
  (absent by default, D8). Defaults are what the package gives; the rest is supplied by the caller or absent, and a
  field whose input is absent keeps its cached result.
- **`FieldEvaluator`** (D1): a registry by name, each entry `String evaluate(FieldInstruction, FieldContext)` returning
  the result text, or null to keep the cached result (D5), with `FormattingSwitchHelper` applied once afterwards for
  `\*`, `\#` and `\@` as today. Nested fields are evaluated first and their text substituted (D4).
- **The consumers.** `FieldUpdater` writes the result back through `FieldRef.setResult` (complex) or the simple
  field's content, as today; `FieldValues` returns it; `AbstractFldSimpleWriter` paints it for every non-layout
  field it has no handler of its own for (D3). The layout fields stay with the renderer: PAGE, NUMPAGES,
  SECTIONPAGES, PAGEREF, STYLEREF, and TOC.

The point of the split is that a field added once reaches update, extraction and output together, with one Word
measurement behind it.

## 5. Decisions

| # | Question | Options | Recommendation |
|---|---|---|---|
| D1 | Where the evaluator lives | (a) `org.docx4j.model.fields` (`FieldEvaluator`, `FieldContext`, `FieldInstruction`, one class per field or family); (b) inside `FieldUpdater`; (c) the inspector's `FieldValues` promoted | (a). `FieldValues` is the best of the three and becomes a thin consumer; `FieldUpdater`'s report and write-back stay as they are. |
| D2 | One instruction parser | (a) a new `FieldInstruction`, the three parsers delegating, `FldSimpleModel` kept; (b) extend `FldSimpleModel`; (c) `FieldInstruction` replaces `FldSimpleModel`, which is deprecated | (c), **Jason 2026-10-28** ("we could deprecate the FldSimpleModel API if that were preferable; yes it is public but I doubt anyone outside docx4j uses it"). `FldSimpleModel` is a parser with a field name and a parameter list, nothing a proper model adds; keeping it means a second type for every signature. In phase 0 it is `@Deprecated` and delegates to `FieldInstruction` (same results, the switch-helper tests unchanged), and so are the signatures that carry it: `FormattingSwitchHelper.applyFormattingSwitch` and the four `formatDate`s gain `FieldInstruction` overloads; the writer handler interfaces (`FldSimpleNodeWriterHandler.toNode`, `getSampleText`, `FldSimpleStringWriterHandler.toString`) gain `FieldInstruction` default methods that the deprecated ones call, so a third-party handler still compiles. Every in-tree user (`AbstractFldSimpleWriter`, the HTML and FO `FldSimpleWriter`s, `AbstractHyperlinkWriter` and its model, `RefHandler`, `AbstractPagerefHandler`, `PageNumberInformationCollector`, `FieldUpdater`, `FieldUpdaterSEQ`, the mergers, `CSwitch`, `FieldValues`, five test classes) moves in phase 0. Removal in the next minor after 17.3. Parser tests from the corpus's instructions (CR-050 phase 0's fixtures hold the shapes Word writes, split runs included). |
| D3 | Which consumer takes which fields | the renderer keeps PAGE, NUMPAGES, SECTIONPAGES, PAGEREF, STYLEREF and TOC; every other field evaluated goes through the evaluator in all three consumers | yes. A field the evaluator computes at output is painted computed only where Word paints it computed (the `dateCachedResult` and `docPropertyCachedResult` properties stay). |
| D4 | Nested fields | (a) evaluate inside-out, substituting text into the outer instruction; (b) outer only | (a). A nested field no evaluator handles makes the outer keep its cached result; the `FieldRef` instruction list already carries the nesting. |
| D5 | A field that cannot be evaluated | (a) keep the cached result (REQ-506's rule); (b) Word's error text | (a) for the updater and the inspector; the output path paints Word's error only where it does today (a REF or PAGEREF to a missing bookmark, `docx4j.convert.out.fo.fieldErrors`). |
| D6 | IF, COMPARE, `=` semantics | measured in Word before coding: string against numeric comparison, `=` `<>` `<` `<=` `>` `>=`, wildcards `?` and `*`, number parsing by the document's language, the functions `=` accepts (SUM, AVERAGE, MIN, MAX, COUNT, ABS, INT, MOD, ROUND, PRODUCT, AND, OR, NOT, DEFINED, TRUE, FALSE), bookmark and table-cell references (`ABOVE`, `LEFT`, `A1`), division by zero and overflow text | a probe docx per group, Word's save read back as the oracle, results recorded in §9 as the probe rounds of CR-066 were; nothing inferred. |
| D7 | Fields spanning paragraphs | (a) a body-level canonicaliser in this CR; (b) out of scope, recorded | (b) for phases 0 to 3; phase 4 (optional) if a measured document needs an IF across paragraphs. The paragraph scope is `FieldsPreprocessor`'s ceiling (§2.1), and the output path's combiner has the same limit. |
| D8 | External and DDE fields (INCLUDETEXT, INCLUDEPICTURE, LINK, DATABASE, the DDE forms) | (a) never evaluated unless the caller supplies a resolver in `FieldContext`; (b) evaluated from the file system | (a). A field that reads a file is CR-057's hostile-input surface; the sample `FieldINCLUDEPICTURE` shows the opt-in shape. |
| D9 | `FieldsPreprocessor` clean-up | (a) fold the dead `preserveResult` branches now, with the existing tests as the gate; replace the XSLT and descend into hyperlinks only when a phase needs it; (b) leave it | (a). The fold is behaviour-preserving (the constant is already `true`); the other two change where fields are found and wait for a field that needs them (REF inside a hyperlink, phase 1, is the first candidate). |
| D10 | What `FieldUpdater` updates by default | (a) every field the evaluator can, as Word's update-all does, locked fields excepted; (b) today's set, the rest opt-in | (a), with a name filter (`FieldUpdater.only(Set<String>)`) for a caller that wants less. A field whose context input is absent keeps its cached result, so a caller that supplies nothing new sees nothing new change except the fields of tier 1 that read the package itself. CHANGELOG names them. |
| D12 (proposed 2026-10-28, python-64's question) | The time zone a date field is formatted in (CREATEDATE, SAVEDATE, DATE, TIME, PRINTDATE), against CR-058 D6's rule for a bound date | (a) the JVM's default zone, as built in phase 1 (Word uses the machine's, measured §11.3); (b) nominal, as D6 reads a bound date (the UTC clock time shown as written); (c) a zone the deployment names, `docx4j.model.datastorage.BindingHandler.dateTimeZone` (D6's property) where set, else the JVM default, with `FieldContext.zone(ZoneId)` to override per call | (c). The two stored values differ in kind: a bound date's `Z` is Word's picker writing local midnight, a nominal date, so D6 reads it nominally to keep the day; `dcterms:created` and the clock are real instants, and a nominal reading would show the UTC clock time where Word on any machine outside UTC shows the local one. So a field is always an instant converted to a zone, never nominal; the question is only which zone, and D6 already names the deployment's users' zone for the case that matters (a server outside its users' zone), so one property governs both: a bound date converted as Word does for those users, and a DATE field shown in their zone. Unset, the JVM default, which is what Word does on that machine. The portfolio's cross-port review (`docx4j-portfolio/docs/date_handling_across_ports.md`, 2026-10-28, for Jason) takes (c) as docx4j's part of one portfolio rule (an instant rendered in one zone defaulting to the process's, a bound date nominal, one zone setting per port reaching every instant place) and adds one extension to name here or later: where `dateTimeZone` is set, `TrackedChange.getDate()`'s reading of a `w:date` without `dateUtc` (Word's local clock) would use that zone too. |
| D11 | The inspector's set (WD REQ-506) | (a) grows with the evaluator, the WD's list updated each phase through the portfolio; (b) frozen at CR-050's set | (a); the WD's rule ("neither layout nor external access") already describes the evaluator's boundary. |

## 6. Phases

| Phase | Work | Status |
|---|---|---|
| 0 | The enabler: `FieldInstruction`, `FieldContext`, `FieldEvaluator` (D1, D2, D10); `FieldUpdater`, `FieldValues` and the DATE, TIME, PRINTDATE and DOCPROPERTY writer handlers moved onto it with no behaviour change; `FldSimpleModel` and the signatures carrying it deprecated, every in-tree user moved (D2); the `preserveResult` fold (D9); parser tests from the corpus instructions; every existing field test green | DONE 2026-10-28 (§10) |
| 1 | Tier 1: CREATEDATE, SAVEDATE, REVNUM, INFO, SECTION, NOTEREF, REF, QUOTE, USERNAME, USERINITIALS, USERADDRESS, each in all three consumers, each with a Word measurement (a probe docx saved by Word, its results the oracle); REF inside a hyperlink if the measurement needs it (D9) | DONE 2026-10-28 (§11.2 built, §11.3 measured: 25 of 25 as predicted) |
| 2 | Tier 2: the expression evaluator (`=`, COMPARE, IF within a paragraph) after D6's measurement; then NEXTIF, SKIPIF, SET, ASK, FILLIN, MERGEREC, MERGESEQ in the mergers; the combiner evaluates a resultless IF whose operands it can (a PAGE operand still cannot be decided per page and keeps today's drop) | proposed |
| 3 | Tier 3: INDEX generated from XE marks (as `TocGenerator` does for TOC); LISTNUM, AUTONUM, AUTONUMLGL, AUTONUMOUT from the CR-014 numbering model; NUMWORDS, NUMCHARS from the text model; FILENAME, TEMPLATE; SYMBOL; ADDRESSBLOCK, GREETINGLINE | proposed |
| 4 | Optional: block-level fields (an IF across paragraphs; INCLUDETEXT with a supplied resolver) by a body-level canonicaliser (D7) | not sized |

Cost, rough: phase 0 two to three days (the refactor is of code with tests, and the gate is the existing field suites
plus export-fo's and the inspector's); phase 1 a day a field once the probe round is run; phase 2 a week, most of it
the measurement and the expression grammar; phase 3 per item, INDEX the largest.

## 7. Risks

- **Behaviour change in `FieldUpdater`** (D10): a document whose AUTHOR or CREATEDATE field held a stale result is
  now updated, as Word updates it. Named in CHANGELOG; the name filter is the opt-out; locked fields are respected
  already (17.3.5).
- **The refactor touching the mergers.** `AbstractMerger` and the migrators call the preprocessor, not the
  evaluator, and are not changed in phase 0; the `FieldRef` shape is unchanged. The field suites
  (`CanonicalisationTests`, `FieldLockRespectedTest`, `FieldResultKeptTest`, `MailMergerCellRuleTest` and the rest)
  are the gate.
- **IF semantics inferred rather than measured.** D6 forbids it; the §9 list records what no round has measured.
- **Output parity.** A field newly painted computed at output moves PDF and HTML output for documents with that field.
  Painted only where Word paints it computed (D3); the fidelity gate (Enterprise CR-001) reads the movers.

## 8. Hand-offs

The TypeScript and Python ports have their own field handling (python's mail merge and update; core-ts's CR-002
treats a field as one unit and its CR-012 extraction takes `fields: update` from the WD). Phase 0's hand-off (2026-10-28):
python-64 has no evaluator and no FieldUpdater port (its field code is `docx4j_py/model/fields/formatting.py` and
`resolvers.py`, and the text model's `field_values.py`), so FieldInstruction, FieldContext and FieldEvaluators arrive
there as one new CR when phase 1's field list and Word measurements go over; no python entry depends on CR-067 yet.
Corrected by python-64 after phase 1 (2026-10-28): its `docx4j_py/model/text/field_values.py` ports `FieldValues` at 198d015a3
behind `fields: update` (the same set, locks and argument rule); what it lacks is the write-back half (no FieldUpdater, no
writers). Phase 1 reaches it as INFO, SECTION, NOTEREF and the user fields (a caller-supplied user its FieldValues has no
context for yet) added there, the CREATEDATE/SAVEDATE parsing and default once the probe settles it, DATE/TIME/PRINTDATE
a question for its user, and a FieldUpdater port as a separate item. It waits for the probe's results before a CR. Each phase's field list and
Word measurements go to them when the phase lands; objects-ts is unaffected (no schema change). The MCP server's
`extract_text` takes the inspector's set through the WD (D11). The portfolio registers this CR as `docx4j/CR-067` with
phases 0 to 4.

## 9. Unmeasured

Rules Word has not been seen to apply, kept here until a probe round measures them (the CR-043 D5 convention):

- IF and COMPARE: string against numeric comparison; wildcards; case; number parsing per language (D6).
- `=`: the function set, cell and bookmark references, error text (D6).
- A REF with `\n`, `\r`, `\w`, `\p` or `\f` (the inspector keeps the cached result for these today).
- SEQ `\s` (reset at a heading level).
- CREATEDATE's and SAVEDATE's default format for a language other than en-US (en-US measured 2026-10-28, §11.3).
- The zone and the forms of a core-property date: Word reads its own W3CDTF (always UTC, `Z`) in the machine's zone (measured,
  §11.3), and docx4j formats every date field in the JVM's default zone (`TimeZone.getDefault()`; no zone on
  `FieldContext`). A date-only value (`2024-02-03`) or a local time with no offset, which another producer may write, docx4j
  reads as local midnight or local time in that zone; what Word shows for those is unmeasured (python-64's question).
- What Word shows for USERNAME when Word has no user name at all (a blank address measured: empty, §11.3).
- Measured 2026-10-28 (§11.3), off this list: INFO against the field it names (the same, and `Error! Unknown info
  keyword.` for no name or an unknown one); QUOTE with no argument (empty); a REF's formatting (the bookmark's);
  NOTEREF `\f` (the reference style); the error texts for a missing bookmark.

## 10. Phase 0 built (2026-10-28)

- **`FieldInstruction`** (D2): `parse(String)` gives the name (upper case; `=` for a formula), the parameter text, the
  parameters as Word tokenises them (a quoted one keeps its quotes), `getArgument()` (the first parameter where it is not
  a switch, unquoted), `hasSwitch`, `getSwitchValue` (`\* MERGEFORMAT` skipped as the switch helper skips it),
  `getSwitchValues`, `indexOfSwitch`, `valueAt`. The tokeniser is `FldSimpleModel`'s, moved; `FieldInstructionTest`
  reads 31 shapes through both and compares every accessor, so nothing a consumer read has changed.
- **`FieldContext`** (D10, D8): package, part, clock (now by default), language (null by default, as the updater
  passed; `documentLanguage(pkg)` for the writers), user, file name, `External` resolver (null by default), lazy
  `DocPropertyResolver` and `DocVariableResolver`, and the pass's SEQ counters and bookmark texts.
- **`FieldEvaluator`** and **`FieldEvaluators`** (D1): the registry by name, `register`, `get`, `has`, `names`,
  `evaluate(instruction, context)` applying `FormattingSwitchHelper` unless the evaluator `formatsItself()` (the date
  fields read their own `\@`). Registered: DOCPROPERTY, DOCVARIABLE, AUTHOR, COMMENTS, KEYWORDS, LASTSAVEDBY, SUBJECT,
  TITLE, REVNUM, CREATEDATE, SAVEDATE, SEQ, REF, QUOTE, DATE, TIME, PRINTDATE: the union of what the three evaluators
  computed, so the registry has the capability now and each consumer keeps its own set until phase 1.
- **Consumers.** `FieldUpdater` evaluates through the registry with a name filter (`only(Set)`; the default is its
  set before this CR: DOCPROPERTY, DOCVARIABLE and the six document-information fields), and keeps its policies (a
  locked field, a document-information field with an argument, MERGEFORMAT's first-run formatting, `formatNewResult`
  for DOCPROPERTY). The inspector's `FieldValues` is a thin consumer with the WD REQ-506 set (DATE, TIME and PRINTDATE
  excluded there, as before). `AbstractFldSimpleWriter`'s DATE, TIME, PRINTDATE and DOCPROPERTY handlers evaluate
  through the registry with the document's language; the cached-result preferences (`dateCachedResult`,
  `docPropertyCachedResult`) stay the writer's.
- **Deprecations** (D2): `FldSimpleModel` (delegating; `of(instruction, field, content)` for a handler on the old
  interface), `FormattingSwitchHelper.applyFormattingSwitch` and the four `formatDate`s on it (`FieldInstruction`
  overloads), the handler interfaces' `toNode`, `toString` and `getSampleText` on it (`FieldInstruction` + cached-result
  `Node` forms, each pair of defaults delegating to the other so a third-party handler on either compiles),
  `AbstractFldSimpleWriter.applyStyle`, `paintStoredResult` and `cachedResultText` on it (`CTSimpleField` and `Node`
  forms), `AbstractHyperlinkWriterModel.build` on it. Every in-tree user moved: the HTML and FO `FldSimpleWriter`s,
  `RefHandler`, `AbstractPagerefHandler`, `AbstractHyperlinkWriter`, `PageNumberInformationCollector`, the two mergers,
  `FieldUpdaterSEQ`, `CSwitch`, `FieldUpdater`, `FieldValues`.
- **`FieldsPreprocessor`** (D9): the `preserveResult` branches folded (the constant was already `true`); the two
  methods gone; the class comment says what the result rule is.
- Gate (2026-10-28): the sixteen field test classes (228 tests) unchanged and green; core-tests 2187/0/30 (the eleven new
  tests included); export-fo-tests 858/0/9. The diffx suite does not touch fields and was not rerun.

## 11. Phase 1 (started 2026-10-28, Jason: "start phase 1 please")

### 11.1 The Word probe, with the predictions before the run

`FieldProbeBuilder` (core-tests, `org.docx4j.model.fields.probe`) writes `field-probe-r1.docx` (sha256 d0f40b29…) and its
README to the share, `__tmp/cr067/phase1/`: 25 cases, every tier-1 field in the complex form with the cached result
`OLD`, core properties title Probe Title, author Probe Author, revision 7, created 2024-02-03T04:05:06Z, modified
2025-06-07T08:09:10Z, a bold bookmark `bm1` ("Bookmarked text"), a bookmark `bmNote` on a footnote reference, a
section break before the last two cases. Jason opens it, Ctrl+A, F9, saves `-word365.docx` and a PDF. Predicted:

| Case | Field | Prediction | Measures |
|---|---|---|---|
| 1, 2 | CREATEDATE, with `\@ "yyyy-MM-dd HH:mm"` | the created time; the default format is Word's locale date-time | the default format; zone handling (the probe's times are UTC) |
| 3, 4 | SAVEDATE, with `\@ "d MMMM yyyy"` | the modified time, or Word's save moment after its save | whether F9 reads the file's value or the session's |
| 5 | REVNUM | 7, or 8 after Word's save | |
| 6, 7 | INFO Title, INFO Author | Probe Title, Probe Author | INFO as the named field |
| 8, 9 | INFO, INFO Nope | Word's error text (unmeasured, §9) | the error texts |
| 10, 24 | SECTION in sections 1 and 2 | 1, 2 | |
| 11, 12 | QUOTE "hello world", QUOTE | hello world; empty or an error (unmeasured) | |
| 13 to 16 | REF bm1, `\* Upper`, REF nope, REF bm1 `\h` | Bookmarked text (bold kept); BOOKMARKED TEXT; Error! Reference source not found.; the text as a link | the formatting of a REF result |
| 17 to 19 | NOTEREF bmNote, `\f`, NOTEREF nope | 1; 1 superscript; Error! Bookmark not defined. | |
| 20 to 22 | USERNAME, USERINITIALS, USERADDRESS | the Word user; what a blank one shows is unmeasured (§9) | |
| 23 | KEYWORDS | alpha, beta (a control, measured for #277) | |
| 25 | CREATEDATE `\@ "yyyy-MM-dd"` as `w:fldSimple` | 2024-02-03 | the simple form updates like the complex |

### 11.2 Built (2026-10-28, before the probe's results)

- **Evaluators** (`FieldEvaluators`): CREATEDATE and SAVEDATE parse the core property's W3CDTF (`FieldSources.parseW3CDTF`:
  offset, local, date-only, XML Schema forms) and format it with the field's `\@` switch, or without one as
  `FormattingSwitchHelper.formatDate` formats a DATE (provisional: the probe measures Word's default, 11.1 cases 1 and 3);
  REVNUM through the document-information map; INFO evaluates the field its argument names (`INFO Title \* Upper` is
  `TITLE \* Upper`; no name, an unknown name or `INFO INFO` keeps the cached result); SECTION reads
  `FieldContext.getSection()`, which the consumer sets; NOTEREF reads `FieldContext.noteNumbers()` (the main part walked
  once: references counted per kind in document order, a custom mark not counted, the open bookmarks named); QUOTE and
  REF as phase 0 had them; USERNAME, USERINITIALS, USERADDRESS from the context, absent by default.
- **Sources** (`FieldSources`, package-private): the walk over the main part for bookmark texts (REF, `readBookmarks()`)
  and note numbers, `sectionOf(part, node)` (the paragraphs with a `w:sectPr` before the node's top-level ancestor in the
  body; null outside the main part), the date parser.
- **Consumers.** `FieldUpdater` updates every registered field by default (D10; `only(Set)` narrows), reads the main part's
  bookmarks once per part, and sets the section per field from its paragraph. The inspector's `FieldValues` adds INFO,
  SECTION, NOTEREF and the user fields to REQ-506's set, and the walk passes the main story's section (its `sections`
  list as it ends them); DATE, TIME and PRINTDATE stay out of the inspector's set, as the WD has it. The writers take
  the renderer's current section into the context and paint sixteen names cached-else-evaluated
  (`AbstractFldSimpleWriter.CACHED_ELSE_EVALUATED`): CREATEDATE, SAVEDATE, REVNUM, INFO, SECTION, QUOTE, NOTEREF, the
  three user fields and the six document-information fields; REF keeps `RefHandler` (cached text, `\h` as a link).
- **Tests.** `FieldEvaluatorsTest` gains the dates, REVNUM and INFO; SECTION, NOTEREF (footnotes and endnotes counted
  apart, `\f` the same number) and the user; `FieldUpdater` on a document with all of them (the user field keeps OLD
  until a user is supplied); the default set now including DATE. The extraction harness's `fields: update` cases pass
  unchanged (no fixture holds a tier-1 field).
- **Not in this phase:** a REF's result formatting (the updater writes plain text; Word keeps the bookmark's
  formatting, 11.1 case 13), NOTEREF's `\f` superscript, `\h` on REF and NOTEREF in the updater (text only), a REF
  inside a hyperlink in the updater (D9; measured by case 16 first), the user fields' Word defaults (§9).
- Gate (2026-10-28): core-tests 2190/0/30 (the extraction harness included), export-fo-tests 858/0/9.
- Open until the probe (11.3): CREATEDATE's and SAVEDATE's default format, INFO's and QUOTE's error texts, the user fields' defaults, a REF's formatting.

### 11.3 Measured (Jason, 2026-10-28, Word 365 version 2608 build 20326.20232; `__tmp/cr067/phase1/results.txt`)

Every one of the 25 cases as predicted in 11.1. The probe, Word's save and the results are the fixtures
`docx4j-core-tests/src/test/resources/fields/cr067-phase1/`, and `FieldProbeWordTest` replays the probe through
`FieldUpdater` against Word's results, case for case (the VM's zone is Sydney's; the dates are compared there).

| Case | Word 365 on F9 | Taken as |
|---|---|---|
| 1, 3 | `2/3/2024 3:05:00 PM`, `6/7/2025 6:09:00 PM` for created 04:05:06Z and modified 08:09:10Z | the default is the short date and the long time (en-US as the VM has it); the file's time is read **to the minute** (the seconds show as 00) in the machine's zone; SAVEDATE on F9 is the file's modified time, not the session's |
| 2, 4, 25 | `2024-02-03 15:05`, `7 June 2025`, `2024-02-03` | the `\@` switch as expected; the simple field updates as the complex |
| 5 | 7 | REVNUM is the file's revision; Word's Save As then wrote revision 2, created and modified both the save's moment (the saved file is a new document to Word) |
| 6, 7 | Probe Title, Probe Author | INFO is the field it names, no difference in any case |
| 8, 9 | `Error! Unknown info keyword.` | the error text for INFO with no name or an unknown one; docx4j keeps the cached result (D5) |
| 10, 24 | 1, 2 | SECTION |
| 11, 12 | hello world; **empty** | a QUOTE with no argument is empty, not an error: docx4j gives "" |
| 13, 14, 16 | Bookmarked text **in the bookmark's bold**; BOOKMARKED TEXT; Bookmarked text (bold, no hyperlink in the result) | a REF's result carries the bookmark's run formatting: `FieldUpdater` writes the bookmark's runs (`FieldRef.setResultRuns`) where the field has no `\*` switch; `\h` changes nothing in the result's XML |
| 15, 19 | `Error! Reference source not found.`, `Error! Bookmark not defined.` | the error texts, as the output path already paints them; the updater keeps the cached result (D5) |
| 17, 18 | 1; 1 in the `FootnoteReference` character style | NOTEREF; with `\f` the result run carries `w:rStyle FootnoteReference`: `FieldUpdater` sets it |
| 20 to 22 | before F9: OLDUSER in all three (Word does not evaluate the user fields on open); after: Author A, AA, **empty** | the cached result is what a reader sees until F9 (so the writers' cached-else-evaluated is right); a user whose address is blank shows an empty result: docx4j gives "" for initials or address where a user is supplied without them |
| 23 | alpha, beta | the control |

What changed on the measurement: `FieldEvaluators`' CoreDate (minute truncation, the default format), QUOTE's empty
result, the user fields' empty results, `FieldSources.bookmarkRuns` and `FieldRef.setResultRuns` for a REF's
formatting, the NOTEREF `\f` style. Gate after them: core-tests 2192/0/30, export-fo-tests 858/0/9. Unmeasured still (§9): the default date format for a language other than en-US
(the locale's short date and medium time is a guess), SEQ `\s`, REF's `\n`, `\r`, `\w`, `\p`, `\f`.

