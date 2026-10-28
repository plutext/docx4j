# CR-067: one field evaluator, and the fields worth adding

Status: ACCEPTED 2026-10-28 (Jason: "CR decisions as recommended; please commit then start implementation"): D1 to D11 as recommended, D2 (c); phase 0 DONE 2026-10-28 (§10); phase 1 DONE 2026-10-28 (§11; Word's measurement in §11.3, every case as predicted); the date-forms probe measured 2026-10-28 (§12.2: D13, D14, built); phase 2 STARTED 2026-10-28 (Jason: "start CR-067 phase 2"); the expression probe measured and the evaluator BUILT the same day (§13.2: `=`, COMPARE, IF, SET, nested fields; D15 to D18); the merge conditionals measured and built the same day (§13.4: NEXTIF, SKIPIF, MERGEREC, MERGESEQ, SET, ASK, FILLIN in the mergers; D19 to D21); phase 2 DONE 2026-10-28; phase 3 STARTED 2026-10-28 (Jason: "start phase 3"); the probes measured the same evening and INDEX, NUMWORDS, NUMCHARS, FILENAME, TEMPLATE, FILESIZE, ADDRESSBLOCK and GREETINGLINE BUILT (§14.2; D22 to D25); LISTNUM, AUTONUM* and SYMBOL measured in round 2 from Word's PDFs and BUILT the same evening (§14.4; D27 to D29); phase 3 DONE 2026-10-28; round 3 (Word's own address block and greeting as merged) measured 2026-10-29 and D26 decided and built the same day (§14.5; D30).
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
| D12 (proposed 2026-10-28, python-64's question; **decided 2026-10-28**, Jason through the portfolio's date review: "I confirm option (a) as amended, and 3.5; also the three sub-items", and here directly: "D12 as recommended"; **built the same day**, §12) | The time zone a date field is formatted in (CREATEDATE, SAVEDATE, DATE, TIME, PRINTDATE), against CR-058 D6's rule for a bound date | (a) the JVM's default zone, as built in phase 1 (Word uses the machine's, measured §11.3); (b) nominal, as D6 reads a bound date (the UTC clock time shown as written); (c) a zone the deployment names, `docx4j.model.datastorage.BindingHandler.dateTimeZone` (D6's property) where set, else the JVM default, with `FieldContext.zone(ZoneId)` to override per call | (c). The two stored values differ in kind: a bound date's `Z` is Word's picker writing local midnight, a nominal date, so D6 reads it nominally to keep the day; `dcterms:created` and the clock are real instants, and a nominal reading would show the UTC clock time where Word on any machine outside UTC shows the local one. So a field is always an instant converted to a zone, never nominal; the question is only which zone, and D6 already names the deployment's users' zone for the case that matters (a server outside its users' zone), so one property governs both: a bound date converted as Word does for those users, and a DATE field shown in their zone. Unset, the JVM default, which is what Word does on that machine. The portfolio's cross-port review (`docx4j-portfolio/docs/date_handling_across_ports.md`, 2026-10-28, for Jason) takes (c) as docx4j's part of one portfolio rule (an instant rendered in one zone defaulting to the process's, a bound date nominal, one zone setting per port reaching every instant place) and adds one extension to name here or later: where `dateTimeZone` is set, `TrackedChange.getDate()`'s reading of a `w:date` without `dateUtc` (Word's local clock) would use that zone too. **Jason's direction (28 Oct, the review's §3.4):** "docx4j running on a server shouldn't necessarily use server time. For a given OPC package, it is desirable to be able to specify time zone (i.e. to use the client's time zone)." So one zone per JVM (D6's system property) is not enough for a server serving many clients: the shape becomes a zone settable on the `OpcPackage` (the one object the field evaluator, the binding traverser's D6 conversion and the revisions model's `w:date` reading all have in hand; a per-document policy carrier already, as the font mapper is), read by all three, falling back to the system property and then the JVM default, with `FieldContext.zone(ZoneId)` kept as the per-call override; docx4j-mcp sets it per request from the client. A request attribute, not a document one: nothing of it is saved into the file, and a package reused across clients is reset by its caller. The review's §3.5 (Jason's second question, "is it desirable to mimic Word's warts, or should this be configurable?", pending his confirmation) separates the bound-date conversion from the zone: Word's day-early reading of the picker's local-midnight `Z` becomes an explicit per-package opt-in named for what it does, off by default, so that setting the package's zone for fields and revision stamps does not by itself turn on that wart for bound dates; D6 today ties the conversion to `dateTimeZone` being set, so under §3.5 `BindingTraverserXSLT` gains a second package attribute (or flag) for the conversion, converting in the package's zone. Implementation held to this shape until Jason confirms the review's option (a) as a whole. |
| D26 (proposed 2026-10-28, docx4j-mcp's request after phase 3; **decided 2026-10-29**, Jason: "D26 as recommended"; **built the same day**, §14.5) | Where FILENAME's name and FILESIZE's size come from for the consumers that build their own context (the text model under `fields: update`, the PDF and HTML writers) | (a) two request attributes on `OpcPackage` beside the zone (`setSourceName`, `setSourceSize`; never saved into the file; reset by a caller reusing a package), read by `FieldContext.of` as the defaults, `FieldContext.fileName`/`fileSize` the per-call override; (b) nothing more: only `FieldUpdater`'s context takes them, extraction and the writers keep FILENAME and FILESIZE cached (phase 3 as built, D25) | (a), D12's argument again: a client asking for the text and the PDF of one document under `fields: update` should see the same FILENAME in both, and a reading tool keeping a stale name while a writing tool updates it is a difference no tool description can explain. Small: two fields on `OpcPackage`, two lines in `FieldContext.of`, a test; WD 0.39's "only where the caller supplies them" then reads "the package's source name and size where set". Built: `OpcPackage.setSourceName/setSourceSize` (request attributes, never saved), read by the context's constructor as the defaults. |
| D11 | The inspector's set (WD REQ-506) | (a) grows with the evaluator, the WD's list updated each phase through the portfolio; (b) frozen at CR-050's set | (a); the WD's rule ("neither layout nor external access") already describes the evaluator's boundary. |

## 6. Phases

| Phase | Work | Status |
|---|---|---|
| 0 | The enabler: `FieldInstruction`, `FieldContext`, `FieldEvaluator` (D1, D2, D10); `FieldUpdater`, `FieldValues` and the DATE, TIME, PRINTDATE and DOCPROPERTY writer handlers moved onto it with no behaviour change; `FldSimpleModel` and the signatures carrying it deprecated, every in-tree user moved (D2); the `preserveResult` fold (D9); parser tests from the corpus instructions; every existing field test green | DONE 2026-10-28 (§10) |
| 1 | Tier 1: CREATEDATE, SAVEDATE, REVNUM, INFO, SECTION, NOTEREF, REF, QUOTE, USERNAME, USERINITIALS, USERADDRESS, each in all three consumers, each with a Word measurement (a probe docx saved by Word, its results the oracle); REF inside a hyperlink if the measurement needs it (D9) | DONE 2026-10-28 (§11.2 built, §11.3 measured: 25 of 25 as predicted) |
| 2 | Tier 2: the expression evaluator (`=`, COMPARE, IF within a paragraph) after D6's measurement; then NEXTIF, SKIPIF, SET, ASK, FILLIN, MERGEREC, MERGESEQ in the mergers; the combiner evaluates a resultless IF whose operands it can (a PAGE operand still cannot be decided per page and keeps today's drop) | DONE 2026-10-28: §13.2 (`=`, COMPARE, IF, SET and nested fields in all three consumers, measured 115 for 115) and §13.4 (NEXTIF, SKIPIF, MERGEREC, MERGESEQ, SET, ASK, FILLIN and the conditionals over merge data in the mergers, measured letter for letter) |
| 3 | Tier 3: INDEX generated from XE marks (as `TocGenerator` does for TOC); LISTNUM, AUTONUM, AUTONUMLGL, AUTONUMOUT from the CR-014 numbering model; NUMWORDS, NUMCHARS from the text model; FILENAME, TEMPLATE; SYMBOL; ADDRESSBLOCK, GREETINGLINE | DONE 2026-10-28: measured the same day (§14.2); INDEX, NUMWORDS, NUMCHARS, FILENAME, TEMPLATE, FILESIZE, ADDRESSBLOCK, GREETINGLINE built; LISTNUM, AUTONUM*, SYMBOL measured from Word's PDFs in round 2 and built (§14.4) |
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

- Measured 2026-10-28 (§13.2), off this list: IF and COMPARE (numbers against strings, wildcards, case, the operators,
  the error text) and `=` (the operators, the functions, bookmark and cell references, the error texts). Still open from
  that round: `^` with a non-integer exponent (Word showed 1.4 for `=2^0.5`; docx4j gives 1.41); unary minus against `^`;
  a function given the wrong number of arguments; a bookmark whose text is not a number in a formula (0 here); whether
  an empty cell ends ABOVE or LEFT (skipped here) and `w:gridSpan` in cell addressing; a quoted bookmark name as an IF
  operand (the name here); COMPARE's text for a bad operator (IF's here); a non-integer in a cell with a currency
  sign or spaces.
- From the merge round (§13.4): whether Word prompts an ASK or FILLIN for a record a SKIPIF then skips (Jason counted
  "4 or 5" before Word's "field calculation error in record 2", then 4); what that error was; the space Word 365 (en-AU)
  printed after the sign under `\# "$#,##0.00"` (`$ 150.00`; docx4j prints none); whether `\b`/`\f` go around the value
  before or after `\* Upper`; whether Word removes a paragraph holding only an empty merge field (suppress blank lines:
  the probe's M06 line kept its token text); which other fields Word unlinks in a merged document (DATE, DOCPROPERTY
  unmeasured; `=` is kept as a field, with the nested MERGEFIELD flattened to its text); a REF before the SET that
  defines it in a later record.
- Measured in round 2 (§14.4), off this list: LISTNUM's default level, a heading's own LISTNUM or AUTONUM (the heading's
  level), AUTONUMLGL in a Normal paragraph, L10 to L19, A01 to A15 and S01 to S14, Word's own ADDRESSBLOCK format (real
  line breaks). Still open from phase 3: the paragraph break Word put after the name line in A01 to A03 (the cases with
  `\c` and `\e`) and not in A04, before a hand-written `\n`'s letter n; `\d`'s country formats (it re-laid the New
  Zealand address as name, street, city, country; docx4j ignores `\d`, which with Word's own format made no visible
  difference, round 3); measured in round 3, off this list: the line break Word's own GREETINGLINE carries before
  `_AFTER_` does not print;
  LISTNUM with a list name that is not one of the three (NumberDefault here), its levels 5 to 9 and OutlineDefault's
  4 to 9 (Word's documented forms), a LISTNUM or AUTONUM in a header, footer, note or text box (the cached result kept),
  a paragraph whose outline level comes from `w:outlineLvl` rather than a heading style (read as its level), AUTONUMOUT
  in body text (the count and a period here), AUTONUM's `\s` with more than one character; SYMBOL codes 128 to 159
  (Windows-1252 here), `\u` with a symbol font, `\f` naming a font the machine lacks, the PDF writer's rendering of a
  SYMBOL in a symbol font (the HTML writer's measured through the written `w:sym`); why
  `<<_TITLE0_ >><<_FIRST0_>><< _LAST0_>>` fell back to `\e` for every record (G03); `\c 0` (printed
  the country, as `\c 1` did); the `\p` position in Word's default block (no postal code was matched);
  FILESIZE's moment (16820 bytes for a file saved at 19009; neither the opened size); FILESIZE `\k` by 1000 or 1024
  (one point: 16820 showed 17); INDEX `\h` with a text other than a letter, `\k` and `\d` given, `\s`, `\y`, two
  cross-references on one entry, an entry text beginning with punctuation; `\b` on a range; the `\e` default when
  `\k` alone is given; NUMCHARS's timing (Word's 212 counted the OLD results; its later DOCPROPERTY Characters 211
  counted the first result written); a text box in the word count.
- A REF with `\n`, `\r`, `\w`, `\p` or `\f` (the inspector keeps the cached result for these today).
- SEQ `\s` (reset at a heading level).
- CREATEDATE's and SAVEDATE's default format for a language other than en-US (en-US measured 2026-10-28, §11.3).
- The forms of a core-property date: measured 2026-10-28 (§12.2), off this list: a date alone and a time with no
  offset are UTC, as a `Z` value is, shown in the client's zone (D12) to the minute (D13). PRINTDATE on F9 is
  `cp:lastPrinted` (D14).
- PRINTDATE's text for a document never printed (no `cp:lastPrinted`); docx4j keeps the cached result (D14).
- A date control's date-only or offset-free value's time under the conversion opt-in: the probe's B01 and B02 (and
  B03, whose 03:04Z is 14:04 local the same day) fix the picker's date, not the time; D6's `Z` case is the measured one.
- DOCPROPERTY CreateTime, LastSavedTime and LastPrinted with a date-only or offset-free value (a separate path: the
  resolver's W3CDTF text through the switch formatter; the field evaluators read them per D13).
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

## 12. D12 built (2026-10-28): the client's zone per package

Jason's ruling on the portfolio's date review (`docx4j-portfolio/docs/date_handling_across_ports.md` §3.6, seven points):
option (a) as amended, §3.5, and the three sub-items. docx4j's part, built:

- `OpcPackage.setDateTimeZone(ZoneId)` / `getDateTimeZone()`, `effectiveDateTimeZone()` (the package's, else
  `docx4j.model.datastorage.BindingHandler.dateTimeZone`, else the JVM default), `propertyDateTimeZone()`; javadoc says
  it is the client's zone for this package, a request attribute, never saved.
- `FieldContext.getZone()` / `zone(ZoneId)` / `getTimeZone()`: the per-call override, else the package's; the date
  evaluators (DATE, TIME, PRINTDATE; CREATEDATE, SAVEDATE with and without `\@`) format in it.
  `FormattingSwitchHelper.formatDate(FieldInstruction, Date, String, TimeZone)`; the thread's cached format has its
  zone set on every call, so no zone leaks between callers.
- `TrackedChange.getDate()`: a `w:date` without `dateUtc` (Word's local clock) is read in the package's zone through
  the change's part; `dateOf(value, zone)`; without a part, the property then the JVM default. `dateUtc` stays UTC.
- `BindingTraverserXSLT.conversionZone(pkg)`: the bound-date conversion converts in the package's zone only where the
  package opts in (`OpcPackage.setBoundDatesConvertedAsWord(true)`, or the property
  `docx4j.model.datastorage.BindingHandler.boundDatesConvertedAsWord`, default false); a zone set alone is nominal.
  CR-058 D6's coupling of the conversion to the zone property ends (a 17.3.4 behaviour; CHANGELOG says so).
- Tests: `PackageDateTimeZoneTest` (the fallback chain and a bad property; the fields in Tokyo and the override; a
  revision's local clock in the package's zone and `dateUtc` unchanged; the opt-in's precedence);
  `BoundDateFormatTest` amended (the zone alone nominal, the flag converting).
- Gate (c3e9920e5): core-tests 2203/0/30, export-fo-tests 858/0/9.
- The ports (2026-10-28): python's CR-025 follows the shape (a zone per package for its fields, stamps and `w:date` readers;
  the bound-date opt-in waiting on its CR-014; a dcterms opt-in); core-ts's CR-018 mirrors it (`pkg.dateTimeZone`,
  `effectiveDateTimeZone()`, the zone reaching a revision's and a comment's `w:date` written and read, the opt-in off by
  default, `writeTimesOnSave`, comments stamped to the minute; the runtime's zone the fallback, no JVM-wide property
  there; `PackageDateTimeZoneTest`'s cases and the amended bound-date case ported); docx4j-mcp's §21.
- The sub-items: no change to reading a date-only or offset-free value until a Word probe (one file: the three forms
  in a core property, a revision and a bound control; docx4j builds it when convenient, on the unmeasured list);
  stamps to the minute (docx4j stamps none); `docx4j.dcterms.write` is the shape the ports copy.
- docx4j-mcp sets the package's zone per request from the client (its hand-off; its CR-mcp-server §21, registry
  `mcp/client-time-zone`, depends on CR-067, lands with its engine bump to 17.3.5): an optional `time_zone` argument on
  every tool that shows an instant, an IANA id resolved with `ZoneId.of` and refused when unknown, set with
  `OpcPackage.setDateTimeZone` in its load helper before any tool reads; unset is the JVM default; no server-wide flag;
  `meta.time_zone` carries `effectiveDateTimeZone().getId()`; the bound-date opt-in not exposed.

### 12.1 The date-forms probe (staged 2026-10-28, for Jason)

`DateFormsProbeBuilder` (core-tests, `org.docx4j.model.fields.probe`) wrote `date-forms-r1.docx` (sha256 8a1dd45e…) and its
README to the share, `__tmp/cr067/dates/`: the three forms, a date only (`2024-02-03`), a local time with no offset
(`2025-06-07T08:09:10`) and a UTC time (`2025-01-02T03:04:05Z`), each in a core property (created, modified, lastPrinted,
read by CREATEDATE, SAVEDATE and PRINTDATE with `\@ "yyyy-MM-dd HH:mm:ss"` and without a switch), a revision's `w:date`
(three insertions, the date read off the balloon or the Reviewing Pane) and a bound date control (`w:fullDate`, format
`yyyy-MM-dd HH:mm`). Predictions in the README: a date only is midnight; an offset-free time is as written; a `Z` is
converted to the machine's zone for a core property and a bound date (D6) and read as the local clock for a revision
(CR-043). Two things it also measures: whether Word drops the seconds on these as it did on created (§11.3), and whether
PRINTDATE shows `cp:lastPrinted` (docx4j's PRINTDATE evaluator is the clock, "the conversion is the printing", the
output's convention since 17.1.0; if Word's F9 shows lastPrinted, `FieldUpdater`'s PRINTDATE should read it).

### 12.2 Measured (Jason, 2026-10-28, Word 365 version 2608 build 20326.20232; `__tmp/cr067/dates/results.txt`)

The probe, Word's save, the results and the README with the predictions are the fixtures
`docx4j-core-tests/src/test/resources/fields/cr067-dates/`; `DateFormsProbeWordTest` replays Part 1 through `FieldUpdater`
with the package's zone set to Sydney's (D12), reads Part 2 through `TrackedChanges`, and holds the forms Word's save wrote.

| Case | Word 365 | Taken as |
|---|---|---|
| D01, D04: created `2024-02-03` | `2024-02-03 11:00:00`; `2/3/2024 11:00:00 AM` | a date alone is **UTC midnight** (11:00 at UTC+11), not the local midnight predicted |
| D02, D05: modified `2025-06-07T08:09:10` | `2025-06-07 18:09:00`; `6/7/2025 6:09:00 PM` | a time with no offset is **UTC** (18:09 at UTC+10), not the local clock predicted; read to the minute (the :10 shows as :00, as §11.3) |
| D03, D06: lastPrinted `2025-01-02T03:04:05Z` | `2025-01-02 14:04:00`; `1/2/2025 2:04:00 PM` | as predicted; **PRINTDATE on F9 is `cp:lastPrinted`**, not the clock (Word's Properties pane shows the same, "Last Printed 2/01/2025 2:04 PM") |
| R01: `w:date="2024-02-03"` | `3/02/2024 12:00:00 AM` | a date alone is local midnight |
| R02: `2025-06-07T08:09:10` | `7/06/2025 8:09 AM` | as written: the local clock |
| R03: `2025-01-02T03:04:05Z` | `2/01/2025 3:04 AM` | the `Z` ignored: the local clock (CR-043's rule, seen again) |
| B01 to B03: `w:fullDate` in the three forms | the stored text as it was; the picker on the nominal date (3 Feb 2024, 7 Jun 2025, 2 Jan 2025) | the nominal date for every form; the time is not separated (03:04Z and 14:04 local are the same day) |

Word's save: `created` and `modified` are the save's moment (§11.3 case 5); `lastPrinted` kept to the minute
(`2025-01-02T03:04:00Z`); every `w:date` and `w:fullDate` rewritten as its nominal clock to the minute labelled `Z`
(`2024-02-03T00:00:00Z`, `2025-06-07T08:09:00Z`, `2025-01-02T03:04:00Z`): no zone conversion of a date-only or
offset-free value in either place, consistent with the local-clock reading of a revision and with D6's "local time
labelled `Z`" for a control.

So the three places disagree on what a bare value means: a core property's is UTC; a revision's and a control's are the
local clock. Two decisions, built the same day (the pre-stated consequence in §12.1 for PRINTDATE):

| # | Decision | Built |
|---|---|---|
| D13 | A core property's date with no offset, or a date alone, is UTC: `FieldSources.parseW3CDTF` (the `XMLGregorianCalendar` fallback likewise). The revision reader (`TrackedChange.dateOf`: the local clock, midnight for a date alone) and the bound-date reader (`parseStoredDate`: nominal) were already as measured; unchanged | yes |
| D14 | `FieldUpdater`'s PRINTDATE is `cp:lastPrinted`, to the minute, in the package's zone, with the `\@` switch or Word's default format as CREATEDATE (`CoreDate("LastPrinted")`); a document never printed (no `cp:lastPrinted`) keeps the field's cached result (Word's text there unmeasured, §9). The PDF and HTML writers' PRINTDATE stays the conversion's clock (the conversion is the printing, 17.1.0's convention; Word's own print sets the field to the print's moment), and the text model's `fields: update` does not compute it (its set unchanged) | yes |

The ports (hand-off 2026-10-28): python's and core-ts's core-property readers take D13 (UTC for a bare value); their
revision and control readers stay as they are; an updater's PRINTDATE, where one exists, reads `cp:lastPrinted`.

### 12.3 INFO in the text model (python-64's Q9, 2026-10-28)

python-64, porting phase 1 (its CR-025), read docx4j's `INFO <name>` as asking the whole registry, so the text
extraction's `fields: update` computed `INFO Date`, `INFO Time` and `INFO PrintDate` at the clock while keeping DATE,
TIME and PRINTDATE cached. Unintended: `INFO X` behaves as X does in the same consumer. Built the same day:
`FieldValues` evaluates an INFO only where the field it names is in its own set (`INFO Date` keeps the cached result,
as DATE does; `FieldValuesInfoTest`). `FieldUpdater` and the writers are unchanged (the updater evaluates the clock
fields, so its `INFO Date` is the clock, as its DATE is; the writers' INFO is cached-else-evaluated like the rest of
their set). The WD's REQ-506 set is unchanged (INFO was already "as the field it names").

Gate for §12.2 and §12.3 together: core-tests 2208/0/30, export-fo-tests 858/0/9.

## 13. Phase 2 (started 2026-10-28, Jason: "start CR-067 phase 2")

### 13.1 The expression probe, with the predictions before the run (D6)

`ExpressionProbeBuilder` (core-tests, `org.docx4j.model.fields.probe`) wrote `expr-probe-r1.docx` (sha256 56ca5489…)
and its README to the share, `__tmp/cr067/phase2/`: 115 cases in paragraphs (token, then the field in the complex
form with the cached result `OLD`; a nested field written as a field inside the instruction) and a 4 by 5 table of
cell formulas; a bookmark `x` on the text `5`. Jason opens it, Ctrl+A, F9, saves `-word365.docx`; **Word's saved file
is the oracle** (`ExpressionProbeWordTest` will replay `FieldUpdater` against it case for case), results.txt for the
build, the VM's regional format and anything Word asked. The README carries every case's prediction; the groups and
what each settles:

| Group (tokens) | Cases | Predicted | Settles |
|---|---|---|---|
| Formula operators (F01-F16) | `+ - * / ^ %`, precedence, parentheses, unary minus, `\#`, a comparison inside a formula | 14, 20, 2.5, 1024, 1.41, -3, 0.5, -6, 10.00, 0.333, 1, 0 | the **default number format** (`=1/3` as 0.33, two decimals, is a guess), `=1000000*1000000` (a plain 13-digit number or an overflow text) |
| Formula errors (F17-F21) | `=1/0`, `=1+`, `=1 2`, `=nope`, `=NOPE(1)` | `!Zero Divide`, `!Unexpected End of Formula`, `!Syntax Error, 2`, `!Undefined Bookmark, nope`, the last unmeasured | the error texts, verbatim |
| Functions (F22-F44) | ABS, INT (3.7 and -3.7), ROUND (2.345 to 2; 2.5, -2.5 to 0; 1234.5 to -2), MOD (7,3; -7,3), SIGN, SUM, AVERAGE, MIN, MAX, COUNT, PRODUCT, AND, OR, NOT, TRUE, FALSE, IF(), nested functions | the arithmetic values; INT(-3.7) = -4, ROUND(-2.5,0) = -3, MOD(-7,3) = 2, AVERAGE(1,2,4) = 2.33 | INT's direction for a negative, ROUND's half rule, MOD's sign, the function set itself |
| Number forms (F45-F48) | `1.5`, `1,5`, `1,000`, `007` in a formula | 2.5; an error; 1001 or an error; 8 | whether the list separator or the thousands separator reads a comma (the VM's regional format, en-AU, has `.` decimal and `,` list) |
| Bookmarks in formulas (F49-F54) | `=x*2`, `=3 + x`, `=SUM(x,1)`, DEFINED(x), DEFINED(nope), `={ REF x }+1` | 10, 8, 6, 1, 0, 6 | a bookmark's text as a number; DEFINED |
| COMPARE (C01-C18) | numbers (5 > 3, 10 > 9, quoted "10" > "9", 5 = 5.0, 007 = 7, 1,000 = 1000, -1 < 0), strings (abc < abd, case, `""` = `""`), wildcards (`a*`, `a?c`, on the left), mixed (5 > "abc"), a bare bookmark name, `<>`, `>=`, `<=` | 1 or 0 as the README says; **unmeasured**: quoted numbers as numbers (predicted 1), case ignored (predicted 1), the thousands separator | numeric against string comparison, the case rule, which side takes wildcards |
| IF forms (I01-I15) | quoted and bare texts, no false text, multi-word texts, every operator, no spaces around `=`, a bad operator, extra text, `""` operands, a space inside a string, `\* Upper` on the result | yes/no as the README says; `IF 5 ! 5` gives `Error! Unknown op code for conditional.`; `IF 5=5` unmeasured | the tokenisation and the error text |
| IF numbers against strings (I16-I22) | the COMPARE cases as IF | as COMPARE | that IF and COMPARE share the rule |
| IF case and wildcards (I23-I28) | case, `*`, `?`, `<>` with a wildcard, `<` with a wildcard, a wildcard on the left | yes, yes, yes, no, no, no | the wildcard rule (right operand, `=` and `<>` only) |
| IF with nested fields (I29-I33) | `{ =2+3 }` as an operand, `{ DATE \@ yyyy }`, a nested field as the true text, `{ REF x }`, both operands nested | yes, yes, 2, yes, yes | D4's inside-out evaluation, as Word does it |
| SET and REF (S01-S10) | SET total 42; REF, `=total+1`, IF on REF, DEFINED; a string SET and a wildcard IF on it; a SET whose value is a nested formula | empty for each SET; 42, 43, yes, 1; Hello World, yes; 42 | SET's bookmark as a formula operand and a REF; what a SET shows |
| Table (A4..D5) | SUM(LEFT) over a text and an empty cell, SUM(ABOVE), AVERAGE(ABOVE), SUM(A1:B2), A1+B2, MAX(A1:A3), COUNT(C1:C3) over text/empty/5, SUM(ABOVE) over formula results | 11, 22, 38, 6, 20, 5, 33, 21, 3, 1 (unmeasured: 2 or 3 if COUNT counts cells), 104 | the cell references and ranges, non-numeric cells, formulas over formula results (document order on F9) |

Not in this round (a second probe, with a data source, when the evaluator lands): NEXTIF, SKIPIF, MERGEREC, MERGESEQ
(a mail merge), ASK and FILLIN (dialogs on F9). An IF across paragraphs is D7's phase 4.

The evaluator's shape, to be written against the results: `FieldEvaluators` gains `=` (an expression parser:
numbers, the operators, functions, bookmark and cell references through the context), COMPARE and IF (the operands
tokenised as `FieldInstruction.splitParameters` does, the comparison by the measured rule, the result text with its
nested fields); nested fields evaluated inside-out (D4) by the consumers that hold the nesting (`FieldRef`'s
instruction list for the updater; the text model's and the writer's walks); SET defines a bookmark the context
reads. Then the mergers' NEXTIF, SKIPIF, SET, ASK, FILLIN, MERGEREC, MERGESEQ.

### 13.2 Measured and built (Jason's run 2026-10-28, Word 365 version 2608 build 20326.20232, regional format English (Australia))

Word's saved file is the fixture `docx4j-core-tests/src/test/resources/fields/cr067-phase2/expr-probe-r1-word365.docx`
(with the probe, the README's predictions and `run-notes.txt`); `ExpressionProbeWordTest` replays `FieldUpdater` and the
text model's `fields: update` against it, 115 cases and the table, and both match it case for case (F07 and F20 read
with a tolerance, below). What the run settled, prediction by prediction:

| Group | As predicted | Not as predicted |
|---|---|---|
| Formula operators | 14, 20, 2.5, 0.33, 0.67, 1024, -3, 0.5, -6, 6, 1000000000000, 10.00, 0.333, 1, 0: the **default format is two decimals, trailing zeros dropped**, a whole number plain, no overflow at 10^12 | `=2^0.5` shows **1.4** (unexplained; docx4j gives 1.41, section 9) |
| Formula errors | `!Zero Divide`, `!Unexpected End of Formula`, `!Undefined Bookmark, NOPE` (the name **upper-cased**; Word's text then carries two stray characters U+9F6F U+00EE in runs of their own, a Word bug) | `=1 2` is `!Missing Operator`; `=NOPE(1)` is `!Syntax Error, 1` (the token after the parenthesis) |
| Functions | ABS, ROUND (2.345 to 2 is 2.35; 2.5 is 3; -2.5 is **-3**, half away from zero; 1234.5 to -2 is 1200), MOD(7,3), SIGN, SUM, AVERAGE (2.33), MIN, MAX, COUNT, PRODUCT, AND, OR, NOT, TRUE, FALSE (no parentheses needed), IF(), nested functions | INT(-3.7) is **-3** (toward zero); MOD(-7,3) is **-1** (the dividend's sign) |
| Number forms | `=1.5+1` 2.5; `=1,5+1` is `!Syntax Error, ,` (the comma is the list separator); `=007+1` 8 | `=1,000+1` is **1,001**: the thousands separator is read, and echoed in the result |
| Bookmarks | `=x*2` 10, `=3 + x` 8, `=SUM(x,1)` 6, DEFINED 1 and 0, `={ REF x }+1` 6 | |
| COMPARE | the numbers, `abc < abd`, the wildcards (`a*`, `a?c`; none on the left), `5 > "abc"` 0, `5 = 5.0`, `007 = 7`, `-1 < 0`, `"" = ""`, `<>`, `>=`, `<=` | `"10" > "9"` is **0**: a quoted number is a string; `"abc" = "ABC"` is **0**: **case-sensitive**; `COMPARE x = 5` is **1**: a bare operand naming a bookmark is the bookmark's text; `1,000 = 1000` is 1 |
| IF forms | quoted and bare texts, no false text (empty), multi-word texts, every operator, extra text ignored (`a b c` gives a), `""` operands, a space inside a string, `5 = ""` no, `\* Upper` on the result | `IF 5=5 yes no` (no spaces) is `Error! Unknown op code for conditional.`, as `IF 5 ! 5` is |
| IF numbers, case, wildcards, nesting | as COMPARE; the nested cases (a formula, a DATE, a REF, both operands, a nested true text) all as predicted | `"10" > "9"` no; `"abc" = "ABC"` no; `1,000 > 999` yes |
| SET and REF | REF 42, `=total+1` 43, IF on `{ REF total }` yes, DEFINED 1; the string SET, its REF and its wildcard IF; the SET of a nested formula and its REF | a SET's field **stores its value as the result, under a bookmark of its name** (`w:bookmarkStart` and `End` around the result inside the field), and Word's PDF shows nothing for it: the display is empty, the file is not |
| Table | SUM(LEFT) 11, 22, 38 (the text and the empty cell skipped); SUM(ABOVE) 6 and 5; AVERAGE(ABOVE) 20; SUM(A1:B2) 33; A1+B2 21; MAX(A1:A3) 3; COUNT(C1:C3) **1** (numbers only); SUM(ABOVE) over the four formula results 104 (document order on F9) | |

Word's save also wrote each result with `w:noProof`, and a nested field's result as `w:instrText` (it lies inside the
outer field's instruction).

Decisions, built the same day:

| # | Decision | Built |
|---|---|---|
| D15 | SET: the evaluator's result is the value; `FieldUpdater` writes it and puts a bookmark of the name around the result slot (`FieldRef.bookmarkResult`; a fresh `w:id` above the part's largest); the PDF and HTML writers paint nothing for a SET (`SetHandler`); the text model returns an empty result and, in every mode, never shows a SET's stored result (Word shows nothing; `FieldState.hiddenResult`): a text-model rule beyond `fields: update`, for the WD. The value reaches the context's bookmarks, so a REF, a formula or an IF after it reads it (document order) | yes |
| D16 | A bare IF or COMPARE operand naming a bookmark is the bookmark's text (then a number if it is one); quoted, it is the string; a bare number is a number; quoted numbers are strings; strings compare case-sensitively, wildcards in the right operand for `=` and `<>` (`Conditional`) | yes |
| D17 | Nested fields inside-out (D4): `FieldUpdater` evaluates a `FieldRef`'s nested instructions first and substitutes each result as one operand (`FieldContext.asOperand`: quoted where empty or holding a space or a quote); a nested field with no evaluator or no result leaves the outer's cached result; the nested field's own stored result is not rewritten (the canonical form gives it no result slot; Word rewrites it on its own update). The text model does the same in its walk (a nested result, computed else stored, is appended to the outer's instruction; `w:instrText` after a field's separate is its stored result, Word's form; a `w:t` result inside the outer's code is the nested field's). The output path's combiner is unchanged (it flattens nesting as before) | yes |
| D18 | A formula's and a comparison's number symbols are the document's `w:decimalSymbol` and `w:listSeparator` (Word writes its regional settings there), else `.` and `,`; the grouping separator is whichever of the two the decimal symbol is not. The `\#` switch formats the unrounded value; `\*` applies after | yes |

Also: `FieldRef.getFldName` returns `=` for a formula (it returned `=2+3`); `FieldContext.readBookmarks` reads the
document once and keeps a SET's entries (it skipped the read when a SET had put one first); the writers' cached-else-
evaluated set gains `=`, COMPARE and IF; `FieldContext.Cells` is the updater's view of the field's table
(`FieldSources.cellsOf`, values read fresh at each lookup, `w:gridSpan` not applied), the text model supplies none (a
formula naming a cell keeps its cached result there). Tests: `ExpressionProbeWordTest` (the replay, both consumers),
`FormulaTest` (the operators, switches, error texts, functions, bookmarks, cells, a document with `,` and `;`),
`ConditionalTest` (IF, COMPARE, bare bookmark names, SET, the operand quoting).

Hand-offs: the WD's REQ-506 set gains `=`, COMPARE, IF and SET, and the text model's rule that a SET's stored result
is never shown (WD 0.37, the portfolio); python's `field_values.py` and core-ts's CR-012 extraction take the same set
and rule, their FieldUpdater ports D15 and D17; the mcp server's `extract_text` through the WD.

Gate: core-tests 2225/0/30 (the 17 new tests included), export-fo-tests 858/0/9.

### 13.3 The merge probe, with the predictions before the run (staged 2026-10-28, Jason: "please build the phase 2 probe")

`MergeProbeBuilder` (core-tests, `org.docx4j.model.fields.probe`) wrote to the share, `__tmp/cr067/merge/`:
`merge-probe-r1.docx` (sha256 5506c18e…, the template: 27 cases, token then field, cached `OLD`), `merge-data-r1.docx`
(sha256 9fe05cef…, the data source: one table, Name, Middle, Amount, City; records 1 Ann _ 150 Sydney, 2 Bob J 50
Melbourne, 3 Cat _ 200 Perth, 4 Dan K 300 Hobart, 5 Eve _ 120 Darwin) and the README. Jason attaches the data source
(Mailings, Use an Existing List), merges to a new document (Finish & Merge, Edit Individual Documents, All), accepts
each ASK and FILLIN default, counts the prompts, and saves `merge-probe-r1-merged.docx`: **the merged document is the
oracle** (`MergeProbeWordTest` will replay the mergers with the same records against it, letter by letter). Predicted
three letters: record 1; record 3 whose second half pulls record 4 (NEXTIF on City = Perth); record 5; record 2 skipped
(SKIPIF Amount < 100).

| Cases | Field | Prediction | Measures |
|---|---|---|---|
| M01, M02 | MERGEREC, MERGESEQ | 1; 3; 5 and 1; 2; 3 | whether a skipped record counts in MERGESEQ (unmeasured) |
| M03 to M05 | MERGEFIELD Name, `\* Upper`, `\b "Dear " \f ","` | Ann, ANN, Dear Ann, ... | the switches over merge data |
| M06, M07 | MERGEFIELD Middle (blank for all three), with `\b ( \f )` | empty; empty | `\b`/`\f` only around a non-empty value; whether Word removes the blank line (suppress blank lines) |
| M08, M09 | `\# "$#,##0.00"` on Amount; `={ MERGEFIELD Amount }*2` | $150.00, ...; 300, 400, 240 | a numeric switch and a formula over a merge field |
| M10, M11 | IF Amount >= 100; COMPARE City = "S*" | big, big, big; 1, 0, 0 | the phase 2 conditionals over merge data |
| M12 | SKIPIF Amount < 100 | empty; Bob's record produces no letter | SKIPIF |
| M13, M14 | SET total `{ ={ MERGEFIELD Amount }+1 }`; REF total | empty; 151, 201, 121 | SET over a merge field, per letter |
| M15 | MERGEFIELD Nope | Word's text for a missing column (unmeasured) | the error text |
| M16 to M19 | ASK Greeting `\d Hello \o`; REF; ASK Prompt2 `\d Hi` (no `\o`); REF | empty, Hello, empty, Hi in every letter; asked once, and once per letter | how many prompts; whether the skipped record prompts |
| M20, M21 | FILLIN `\d World \o`; FILLIN `\d Each` | World; Each, in every letter | the same for FILLIN (its result is the field's) |
| M22 to M27 | Name, NEXTIF City = "Perth", Name, MERGEREC, MERGESEQ, City | Ann/Ann/1/1/Sydney; Cat/Dan/4/2 or 3/Hobart; Eve/Eve/5/3 or 4/Darwin | NEXTIF pulling the next record into the same letter; MERGEREC and MERGESEQ after it |

For docx4j: the mergers (`MailMerger`, `MailMergerWithNext`) take the records as maps, so the replay feeds the same five;
what lands after the run is SKIPIF and NEXTIF in the mergers' record loop, MERGEREC and MERGESEQ from the loop's
counters, SET through the evaluator (D15), ASK and FILLIN from a caller-supplied answer (the defaults otherwise; no
dialog on a server), and the switches and conditionals through `FieldEvaluators` with the record as the context's
merge data.

### 13.4 The merge round, measured and built (Jason's run 2026-10-28, Word 365 version 2608 build 20326.20232)

Fixtures `docx4j-core-tests/src/test/resources/fields/cr067-merge/` (the template, the data source, Word's merged
document `merge-probe-r1-merged.docx`, the template as saved with the list attached, the README, Jason's notes);
`MergeProbeWordTest` merges the template over the five records with `MailMerger` and compares the three letters case
for case with Word's (81 of 81, two read with a tolerance, below); `MergeFieldEvaluatorsTest` covers the evaluators
and NEXT.

What Word did:

| Case | Word's merged document | Taken as |
|---|---|---|
| the letters | three: record 1; record 3 with record 4 pulled in; record 5 | as predicted: SKIPIF (Amount < 100) skips record 2 entirely; NEXTIF (City = Perth) continues the letter with the next record |
| M01, M02, M25, M26 | 1/1, 3/2, 5/3; after the NEXTIF 1/1, 4/2, 5/3 | MERGEREC is the record's number; MERGESEQ the letter's, counting neither the skipped nor the pulled-in record |
| M03 to M05 | Ann, ANN, Dear Ann, | the switches; `\b` and `\f` print around the value |
| M06, M07 | empty, empty (the lines kept, with their token text) | nothing around an empty value; the blank-line rule stays unmeasured (§9) |
| M08 | `$ 150.00` | `\# "$#,##0.00"` with a space after the sign (en-AU; docx4j prints `$150.00`; §9) |
| M09 | 300, **kept as a field** `{ =150*2 }`: the nested MERGEFIELD flattened to its text (`w:instrText 150`) | Word's merge unlinks every evaluated field but a formula |
| M10, M11 | big; 1, 0, 0 (plain text) | IF and COMPARE over merge fields, unlinked |
| M12, M23 | the field gone | SKIPIF and NEXTIF leave nothing |
| M13, M14 | the SET gone (no result, no bookmark); REF 151, 201, 121 as text | SET evaluated per letter and removed; a REF to it unlinked to its text |
| M15 | Word stopped with "Invalid Merge Field: this merge field is used in the main document, but it does not exist in the data source" (Remove Field, or replace it); Jason replaced it with Name, so the letters show Ann, Cat, Eve | Word does not merge a MERGEFIELD naming no column; a server cannot ask, so docx4j keeps today's fate (REMOVED drops the field, KEEP leaves it) |
| M16 to M19 | ASK gone; REF Greeting Hello; ASK gone; REF Prompt2 Hi | ASK removed, its bookmark's value read by the REF; `\o` asked once, the other per letter |
| M20, M21 | World; Each (plain text) | FILLIN unlinked to its answer |
| the prompts | "4 or 5", then "A field calculation error occurred in record 2", then 4 more | record 1's four, records 3 and 5's two each; whether the skipped record prompted is not separated (§9) |
| M22, M24, M27 | Ann/Ann/Sydney; Cat/Dan/Hobart; Eve/Eve/Darwin | NEXTIF |

Word's save of the template with the list attached (`merge-probe-r1-use-list.docx`) shows the placeholders Word
paints before a merge: «Name», «NAME» under `\* Upper`, «Amount» (the `\#` not applied), `Dear «Name»,` and `(«Middle»)`
(the `\b`/`\f` texts around the placeholder), «Merge Record #», «Merge Sequence #», «Skip Record If...»,
«Next Record If», «Nope»; the `=`, IF, COMPARE, SET and REF fields already evaluated on attaching (300, big, 1, 151
with the bookmark, 151), the ASK bookmarks not yet defined (`Error! Reference source not found.`).

Decisions, built the same day:

| # | Decision | Built |
|---|---|---|
| D19 | The mergers' record loop (`AbstractMerger.MergeState`): one letter per record; SKIPIF true skips the record (no letter); NEXT and NEXTIF true advance to the next record within the letter (NEXT now in `MailMerger` too, as `MailMergerWithNext` had it); MERGEREC is the record's number, MERGESEQ the letter's (neither a skipped nor a pulled-in record counts); past the last record a NEXT merges nothing. Headers and footers follow the letter's record | yes |
| D20 | What a merge evaluates and unlinks: besides MERGEFIELD, the fields in `MailMerger.MERGE_EVALUATED` (NEXT, NEXTIF, SKIPIF, MERGEREC, MERGESEQ, SET, ASK, FILLIN, IF, COMPARE, REF, `=`), through `FieldEvaluators` with the record in the context (`FieldContext.mergeRecord`, `mergeRecordNumber`, `mergeSequence`; the MERGEFIELD, MERGEREC and MERGESEQ evaluators read them and keep the cached result outside a merge), nested fields inside-out (`FieldEvaluators.instructionOf`, shared with `FieldUpdater`). Under the REMOVED fate the result stays as text and the field goes, SET and ASK go entirely (Word's shape); under KEEP_MERGEFIELD every field stays with its result. One deviation, recorded: Word keeps a `=` as a field with its nested merge field flattened to text; docx4j unlinks it with the rest (the flattening needs run surgery in the canonical form) | yes |
| D21 | ASK and FILLIN on a server: the answer is the caller's (`FieldContext.answers()`, by the ASK's bookmark name or the FILLIN's prompt, through `AbstractMerger.getFieldContext()` before the merge), else the `\d` default, else empty; ASK defines its bookmark and shows nothing, FILLIN's answer is its result; `\o` has no effect (one answer serves every letter). In `FieldUpdater` the same (no dialog) | yes |

Also: the mergers honour a MERGEFIELD's `\b` and `\f` (they did not); `FieldRef.getFldName` names a formula `=`.
Hand-offs: the WD's REQ-506 is not changed by this round (the merge fields are the mergers', not the text model's); the
ports' mergers (python's mail merge; core-ts has none) take D19 to D21 and the measured rules.

Gate: core-tests 2231/0/30, export-fo-tests 858/0/9.

## 14. Phase 3 (started 2026-10-28, Jason: "start phase 3")

### 14.1 The probes, with the predictions before the run (D6)

`Phase3ProbeBuilder` (core-tests, `org.docx4j.model.fields.probe`) wrote four files and one README to the share,
`__tmp/cr067/phase3/`, for three Word sessions (about 20 minutes): `phase3-probe-r1.docx` (sha256 3aa9ca9a…; Ctrl+A,
F9, save), `numwords-probe-r1.docx` (009e1b01…; F9, save) and `address-probe-r1.docx` (f5ad30d5…) merged over
`address-data-r1.docx` (462e8a68…; four records). Every field carries the cached result `OLD`; Word's saved files are
the oracle (a replay test per file, as `ExpressionProbeWordTest` and `MergeProbeWordTest`). The README carries every
case's prediction; the groups and what each settles:

| Group (tokens) | Cases | Predicted | Settles |
|---|---|---|---|
| XE marks (X01-X21), three pages each opened by a PAGE field | plain entries on one, two and three pages; a duplicate on one page; `Banana:Yellow`, `Banana:Green`, `Banana` and `Melon:Water:Honeydew` (sub-entries, a parent with its own page, three levels); `cherry` and `apple` in lower case; `10 items` against `2 items`; `éclair`; `\t "See Cat"` and `\t "See also Apple"`, and a cross-referenced entry with a page too; `\b`, `\i`, `\b \i`; `\r GrapeRange` (a bookmark from page 2 to 3); `\f "b"` | `Apple, 1, 2, 3`; `Banana, 2` then the sub-entries indented and sorted; `Dog. See Cat`; `Egg, 1` with the 1 bold; `Grape, 2–3`; `apple` a separate entry; `Kiwi` only under `INDEX \f "b"` | the sort (case, digits, accents), the separators (`, ` between entry and pages and between pages, `. ` before a cross-reference, an en dash for a range), the page-number formatting, a parent entry with its own page, a cross-reference beside pages, what a page list of consecutive pages looks like (listed, not ranged) |
| INDEX (IDX01-IDX06) | plain; Word's own `\c "2" \z "1033"`; `\e ": " \h "A" \l "; " \g " to " \c "1"`; `\f "b"`; `\r`; `\p "a-c"` | one paragraph per entry in the Index 1/2/3 styles; two columns by continuous section breaks; letter headings in Index Heading; run-in sub-entries; the letter range | the generated shape (styles, the field's paragraphs, the section breaks), the default column count, the switch texts |
| LISTNUM (L01-L19) | plain, `\l 2`, `\l 3`, back to `\l 1`, `\s 10`, two in one paragraph, `LegalDefault` and `OutlineDefault` with levels, `NumberDefault`, `\l 2 \s 5` | `1) 2) a) i) 3) 10) 11) 12) 13)`; `1. 1.1. 1.2. 2.`; `I. A. 1.`; `14) e) f) 15)` | the three built-in lists' formats, the level and start switches, whether `NumberDefault` is the plain list, and how Word saves the result (a run, a `w:numPr`, a simple field) |
| AUTONUM, AUTONUMLGL, AUTONUMOUT (A01-A15) | four AUTONUM in Normal (one `\s :`); AUTONUMLGL on Heading 1/2/2/1/2 (`\e`) and a Normal paragraph; AUTONUMOUT on Heading 1/2/3/2; an AUTONUM after the headings | `1. 2. 3: 4.`; `1. 1.1. 1.2. 2. 2.1`; `I. A. 1. B.`; the Normal AUTONUMLGL and the trailing AUTONUM unmeasured | whether only paragraphs holding the field count, whether the two legal/outline kinds share a counter, what Word saves (these are layout-numbered in older formats) |
| FILENAME, TEMPLATE, FILESIZE (N01-N10) | FILENAME, `\p`, `\* Upper`; TEMPLATE, `\p`; FILESIZE, `\k`, `\m`; DOCPROPERTY Template; NUMPAGES | `phase3-probe-r1.docx` (the name at F9, not the Save As name); the VM's path; `Normal.dotm` and its path; the byte size | the path form, whether the Save As name reaches the field, FILESIZE's moment |
| SYMBOL (S01-S14) | decimal and `0x` hex, `\f "Symbol"`, `\f Wingdings`, `\u` (8364, 0x20AC), `\s 24`, `\f "Arial" \s 12 \h`, 300 without `\u`, 65, `\a`, `\u \f "Courier New"`, `abc`, no argument | ©, ♥ and the Wingdings hand as `w:sym` (unmeasured), €, the size on the result run | how Word stores the result (`w:t` or `w:sym`), the code above 255 without `\u`, the error texts |
| NUMWORDS, NUMCHARS (W01-W07, a separate file) | a controlled body (two texts, seven case lines; a footnote of 2 words; a header of 3), NUMWORDS, NUMCHARS, DOCPROPERTY Words, Characters, CharactersWithSpaces, Paragraphs, Lines | 36 words (38 with the footnote, 41 with the header), 212 characters without spaces (239 with), 9 paragraphs | the word rule (`=`, `Well-known`, `e-mail`, `a@b.com`, `1,000`, `don't` one each), whether NUMCHARS counts spaces, whether the footnote or the header count, and the saved app.xml against the fields |
| ADDRESSBLOCK, GREETINGLINE (A01-A05, G01-G04, a merge over four records with gaps: no suffix, company, second line, state, title or last name) | Word's en-US `\f` format with `\c 2 \e "Australia" \d`, `\c 1`, `\c 0`, a short format, no switches; GREETINGLINE with title + last name and `\e "Dear Sir or Madam,"`, first name, full name, no switches; MERGEFIELD "First Name" and MERGEREC as controls | the lines as the README lists them, empty parts and their separators dropped, the country only where it differs from `\e`; `Dear Mr Smith,`; the fallbacks for a missing title or last name unmeasured | the token grammar (`<<_FIRST0_>>`, literal text inside a token, `\n`), the empty-part rule, `\c`, `\d`, the greeting's fallback rule, the result's shape (line breaks in one paragraph or paragraphs), what Word's no-switch defaults are, and whether Word maps the columns by their standard names |

Not in this round: `\y` (yomi), `\k` and `\d` on INDEX, `\s` (sequence) on INDEX and XE, LISTNUM naming a list style,
`\j` and `\a` codes above 127 on SYMBOL, GREETINGLINE's `\c` (the comma language), and a text box in the word count
(unmeasured, §9).

The evaluator's shape, to be written against the results: INDEX generated from the XE marks as `TocGenerator`
generates a TOC (the entries sorted and grouped by the measured rule, page numbers from the FO renderer where the
caller has one, otherwise the field kept cached as a TOC without page numbers is); LISTNUM through the CR-014
`NumberingState` per list name and level, AUTONUM* counting the paragraphs that hold them; NUMWORDS and NUMCHARS from
the text model with the measured word rule; FILENAME and TEMPLATE from `FieldContext.fileName` (null keeps the cached
result); SYMBOL producing a `w:sym` or a character by the measured rule; ADDRESSBLOCK and GREETINGLINE in the mergers,
their `\f` tokens filled from the record by Word's standard field names (and `w:odso/w:fieldMapData` where the document
carries a mapping).

### 14.2 Measured and built (Jason's run 2026-10-28 18:52 to 19:02, Word 365 version 2608 build 20326.20232)

Fixtures `docx4j-core-tests/src/test/resources/fields/cr067-phase3/` (the four probes, Word's three saves, the word-count
PDF, the README, `run-notes.txt` from the five screenshots). Replay tests: `Phase3ProbeWordTest` (`IndexGenerator` over the
probe's XE marks with a page-break pager against Word's six indexes, paragraph for paragraph; FILENAME, TEMPLATE, FILESIZE from
the context; NUMWORDS and NUMCHARS) and `AddressProbeWordTest` (`MailMerger` over the four records against Word's merged
letters). The counts: six INDEX fields identical to Word's (section paragraphs included); N01 to N03 and N06 to N08 as Word;
W01, W02 as Word; the address cases as Word but for the two deviations below.

What Word did:

| Group | Word's saved file | Taken as |
|---|---|---|
| INDEX, the entries | one paragraph per entry, `Index1`/`Index2`/`Index3` (a parent without pages of its own still on a line: `Banana, 2` then `Green, 2`, `Yellow, 1`; `Melon`, `Water`, `Honeydew, 2`), each paragraph with a right dot-leader tab at the writable width and `noProof` runs; the begin, instruction and separate stay in the token's paragraph, the end closes an otherwise empty paragraph | the shape generated |
| the order | `10 items`, `2 items`, `apple`, `Apple`, `Banana`, `cherry`, `Dog`, `éclair`, `Egg`, `Fig`, `Grape`, `Melon`, `Nut`, `Orange` | digits before letters by character; case and accents ignored in the order; `apple` a separate entry listed before `Apple` (an English collator) |
| the pages | `Apple, 1, 2, 3` (a duplicate mark on page 1 listed once; consecutive pages listed, not ranged); `Grape, 2–3` (`\r`, an en dash); `Egg, **1**`, `Fig, _1_`, `Nut, **_3_**` | `\e` ", ", `\l` ", ", `\g` en dash; the page run bold or italic |
| cross-references | `Dog, 3, See Cat` (a page and a `\t`); `Orange. See also Apple` (no page); under `\e ": " \l "; "`: `Dog: 3; See Cat`, `Orange: See also Apple`; not italic | after pages, `\l` then the text; without, `\k` (". ", or `\e` where `\e` is given) |
| `\h "A"` | `IndexHeading` paragraphs (keepNext) `1`, `2`, `A`, `B`, `C`, `D`, `E` (éclair and Egg), `F`, `G`, `M`, `N`, `O` | one heading per initial, letters upper-cased with accents stripped, each digit its own |
| `\c "2"` (and `\c "1"`) | an empty paragraph carrying the enclosing section's properties before the entries, one carrying `continuous` + `cols num=2 space=720` after them, and the enclosing section itself now `continuous` | generated for any `\c` (one column: `cols` without `num`) |
| `\r` | `Banana, 2; Green, 2; Yellow, 1`; `Melon: Water; Honeydew, 2` | the parent, then `; ` (`: ` where the parent has no page) and the descendants flattened, `; ` between |
| `\f "b"`, `\p "a-c"` | `Kiwi, 2` alone; `apple` to `cherry` (the digit entries excluded) | a typed index lists its type only, the default index the untyped marks; the letter range by first letter |
| FILENAME, TEMPLATE, FILESIZE | `phase3-probe-r1-word365.docx` (the Save As name: the fields were updated after it), `X:\__tmp\cr067\phase3\…` under `\p`, `PHASE3-PROBE-R1-WORD365.DOCX`; `Normal.dotm`, `C:\Users\jharrop\AppData\Roaming\Microsoft\Templates\Normal.dotm`; `16820`, `17`, `0`; NUMPAGES 6 | the name as given (`\p` all of it); app.xml's Template (`\p` kept cached); the size as given, `\k` and `\m` rounded by a thousand |
| LISTNUM, AUTONUM*, SYMBOL | every result still `OLD` in the file; the screenshot shows L01 `i)`, L02 `ii)`, L03 `b)`, L04 `i)`, L05 `2)`, L06 `x)`, L07 `xi)`, L08 `xii) and xiii)`, L09 `2.1.14.` | Word never writes these results (they are laid out); the three named lists share one set of counters (LegalDefault continued at 14), a level started without a parent starts the parent at 1, a level resets the deeper ones, `\s` sets the counter; the default level is not settled (round 2, §14.3) |
| NUMWORDS, NUMCHARS | 36 and 212 (the file as opened: the OLD results in place); DOCPROPERTY Words 36, Characters 211, CharactersWithSpaces 238, Paragraphs 9, Lines 9; the status bar 38 words (the footnote's two); app.xml after the save Words 59, Characters 345 (another count) | words are runs of non-whitespace (`=`, `Well-known`, `e-mail`, `a@b.com`, `1,000`, `don't` one each); NUMCHARS counts characters without spaces; the main story only (no header, no footnote); the field's own result counts |
| ADDRESSBLOCK | Word normalises a column `First Name` to `First_Name` (the probe's `MERGEFIELD "First Name"` raised "Invalid Merge Field"; Jason merged Title); Match Fields matched the standard names but Postal Code (so no postal code anywhere); with `\f`: a token printed only where its field has a value, its literal text around the value (`Sydney, NSW`; no `, ` without a state), the `\n` outside a token a paragraph break, `\c 2 \e "Australia"` omitting Australia and printing New Zealand, `\c 1` and `\c 0` both printing the country; the no-switch default `Acme Pty Ltd / John Smith / 1 Main St / Suite 2 / Sydney NSW / Australia` (company first, no title or suffix, no comma, the country always); `\d` on the New Zealand record gave the same lines as its format | D23; deviations: the `\n` inside a token printed the letter n (docx4j breaks the line; round 2 reads Word's own format), the lines are line breaks in one paragraph (Word writes paragraphs) |
| GREETINGLINE | `Dear Mr Smith,`, `Dear Ms Doe,`, `Dear Pat Lee,` (no title), `Dear Sam,` (no last name); `Dear John,`; the full-name format `To whom it may concern:` for every record; no switches: nothing | D24; the fallbacks first + last, then first, then `\e`; the full-name case a recorded deviation (docx4j fills it) |

Decisions, built the same day:

| # | Decision | Built |
|---|---|---|
| D22 | INDEX is block-level, generated by `org.docx4j.model.fields.index.IndexGenerator` from the XE marks with a `Pager` (the page a paragraph starts on): `IndexGenerator.paginating(pkg)` lays the document out through docx4j-export-fo (`Paginate`, as the TOC generator does), a caller may give its own; `FieldUpdater` regenerates every INDEX in the body before its paragraph pass (`indexPager(Pager)` to supply one), and with no pager keeps the field (D5, reported). The text model and the writers show the cached paragraphs, as for a TOC. An INDEX in a table or a content control is left as it is | yes |
| D23 | ADDRESSBLOCK in the mergers: the `\f` tokens filled from the record, the columns found by Word's address-field names and their usual spellings (`AddressFields.PLACEHOLDERS`; `First_Name` and `First Name` alike), or by the document's `w:odso/w:fieldMapData`; a token prints only with a value; a line break in the format a line (Word's own form, round 2, D29; a hand-written `\n` prints the letter n, as Word prints it); `\c 2` omits the country equal to `\e`, else the country prints; `\d` and `\l` ignored; no `\f`: Word's measured default. The lines are line breaks in the one paragraph (Word writes paragraphs: a recorded deviation, the paragraph split needs run surgery the canonical form does not give) | yes |
| D24 | GREETINGLINE in the mergers: `_BEFORE_` and `_AFTER_` literal (the space after the placeholder dropped), the name tokens filled; where one is empty, first + last, then first, then `\e`; no `\f`: empty | yes |
| D25 | NUMWORDS, NUMCHARS, FILENAME, TEMPLATE, FILESIZE in all three consumers (`FieldValues` computes NUMWORDS, NUMCHARS and TEMPLATE; FILENAME and FILESIZE need the context's `fileName`/`fileSize` and keep the cached result without them; the writers' CACHED_ELSE_EVALUATED set gains the five) | yes |

Hand-offs: WD REQ-506 gains NUMWORDS, NUMCHARS, TEMPLATE (and FILENAME, FILESIZE as context-dependent) for the inspector (WD 0.39,
through the portfolio); the ports take D22 to D25 and the measured rules (python's merger: ADDRESSBLOCK and GREETINGLINE; the
index generator for both).

### 14.3 Round 2, staged 2026-10-28 (LISTNUM's default level, AUTONUM, the PDF, Word's own address format)

`ListNumProbeBuilder` wrote `listnum-probe-r2.docx` (sha256 61bd4536…, 15 cases, no heading before the first) and
`README-round2.txt` to the share beside round 1. Asked: (A) `phase3-probe-r1-word365.docx` saved as PDF (the only record of
L10 to L19, A01 to A15, S01 to S14, since Word writes no result for those fields); (B) the round-2 file updated and saved as
docx and PDF; (C) `address-probe-r1.docx` with the list attached and Word's own Address Block and Greeting Line inserted,
saved as `address-probe-r1-attached.docx` (Word's own `\f` strings and its `w:odso` mapping). Predictions in the README:
R01 `1)` if the default level in a heading-free document is 1 (else `i)`), R04 `b)` under the implicit-parent reading, R05
`3.`, R06/R07 `a)`/`i)` if the default level is the preceding heading's level + 1, R13 `3.` if AUTONUM counts on across
a heading. Then LISTNUM and AUTONUM* through the CR-014 `NumberingState` (one counter set for the three named lists) and
SYMBOL, in all three consumers.

### 14.4 Round 2 measured and built (Jason's saves 2026-10-28 20:54 to 20:58, Word 365 version 2608 build 20326.20232)

The three captures: (A) `phase3-probe-r1-word365.pdf`, the PDF of the round-1 save (L10 to L19, A01 to A15, S01 to S14; the
SYMBOL runs read with mutool for their font and size); (B) `listnum-probe-r2-word365.docx` and `.pdf` (the docx keeps OLD in
all fifteen cases; the PDF has the numbers); (C) `address-probe-r1-attached.docx` (Word's own Address Block and Greeting Line
over the attached list; not merged). Fixtures beside round 1's in `docx4j-core-tests/src/test/resources/fields/cr067-phase3/`
(`run-notes.txt` has the detail).

| Case | Predicted | Word | Rule |
|---|---|---|---|
| L01 to L09 (after a Heading 2) | 1) 2) a) i) 3) 10) 11) 12) 13) 1. | i) ii) b) i) 2) x) xi) xii) xiii) 2.1.14. | the default level is the paragraph's heading level, else the level after the nearest heading before it (Heading 2: level 3), else 1; the three named lists share one counter set; a level started under a parent that has not counted gives the parent 1 (L01 made levels 1 and 2 "1", so L03 at level 2 is b) and L05 at level 1 is 2)); counting a level resets the deeper ones; `\s` sets the count |
| L10 to L15 | 1.1. 1.2. 2. I. A. 1. | 2.2. 2.3. 2.3.1. 2. D. 1. | LegalDefault paints the counts from level 1 with periods; OutlineDefault I. A. 1.; the counters are the same ones (level 2 was at 1, so L10 is 2.2.; level 3 after L12's reset is 1, then L13 counts it to 2) |
| L16 to L19 | 14) e) f) 15) | ii) e) f) i) | NumberDefault is the same list as a bare LISTNUM; `\l 2 \s 5` e), then f); the next bare LISTNUM is at level 3 again, reset by level 2 |
| R01 to R05 (no heading before) | 1) 2) i) b) 3. | 1) 2) i) b) 3. | as predicted: level 1 with no heading; the implicit parent; LegalDefault continues the same counters |
| R06, R07, R10 (after H1, H2, H3) | a) i) (1) | a) i) (1) | the level after the preceding heading's; NumberDefault level 4 is (1) |
| R08, R09 (in a Heading 1; `\l 1`) | 1) or a); the level-1 count + 1 | 4) 5) | a field inside a heading paragraph is at the heading's level, and the counters are shared with the body's |
| A01 to A04 | 1. 2. 3: 4. | 1. 2. 3: 4. | AUTONUM counts and paints a period, or `\s` |
| A05 to A10 (AUTONUMLGL in Heading 1, 2, 2, 1, 2 `\e`, Normal) | 1. 1.1. 1.2. 2. 2.1 then 2.1.1. or 3. or 1. | 1. 1.1. 1.2. 2. 2.1 2.1.1. | one counter per heading level and one for body text; a field counts at its paragraph's level and resets the deeper levels; AUTONUMLGL paints the levels counted so far; `\e` drops the last period; body text after a numbered Heading 2 is a third level |
| A11 to A15 (AUTONUMOUT in H1, H2, H3, H2; AUTONUM in Normal) | I. or III.; A. 1. B.; 5. or 1. | III. A. 1. B. 1. | the AUTONUM family shares its counters (III. follows the two AUTONUMLGL headings; the LISTNUM counters are separate, A05 being 1. where LISTNUM's level 1 stood at 2); A15 restarts because A14 at level 2 reset body text |
| R11 to R15 | 1. 2. 3. or 1.; 1. or 4.; 1.1. or 1. | 1. 2. 3. 1. 1.1. | a heading with no field changes nothing (R13 counts on); AUTONUM in a Heading 1 is that level's first (R14); AUTONUMLGL in body text after it is 1.1. |
| S01, S02, S10, S11 | © © A A | © © A A | the code (decimal or 0x) is a character code; `\a` the default |
| S03, S04 | ♥ as w:sym; the Wingdings hand | the Symbol glyph at F0A9, the Wingdings glyph at F041 (mutool: SymbolMT, Wingdings-Regular, private-use characters) | a symbol-encoded font's code is a glyph, painted from the private-use range (U+F000 and the code), Word's w:sym |
| S05, S06, S12 | € € A in Courier New | € € A (CourierNewPSMT) | `\u` a Unicode code point; `\f` the font |
| S07, S08 | © at 24pt; © in Arial 12 | © Aptos 24; © ArialMT 12 | `\s` the size in points; `\h` nothing visible |
| S09, S13, S14 | the low byte, Ĭ or an error; an error or nothing | ### ### ### (Aptos-Bold) | a code Word cannot read (none, not a number, over 255 without `\u`) paints ### in bold |
| Word's own ADDRESSBLOCK | the documented `\n` form | `\f "<<_COMPANY_⏎>><<_TITLE0_ >><<_FIRST0_>><< _LAST0_>><< _SUFFIX0_>>⏎<<_STREET1_⏎>><<_STREET2_⏎>><<_CITY_>><< _STATE_>><< _POSTAL_>><<⏎_COUNTRY_>>" \l 3081 \c 2 \e "Australia" \d`, the line breaks real (`w:cr` inside the instruction runs), no `\n`; `w:odso` maps First_Name, Last_Name, Address_1, Address_2 and Country (Country or Region), Postal Code again unmapped | a line break in the format is a line; a hand-written `\n` prints the letter n (round 1), inside a token and outside one |
| Word's own GREETINGLINE | one line | a `w:fldSimple`, `\f "<<_BEFORE_ Dear >><<_TITLE0_>><< _LAST0_>>&#xA;<<_AFTER_ ,>>" \l 3081 \e "Dear Sir or Madam,"`, a line break before `_AFTER_` | printed on one line (the break dropped; its rendering unmeasured, section 9) |

Also read again from round 1's merge, against the rule above: Word broke the paragraph after the name line in A01 to A03 (the
cases with `\c` and `\e`) and not in A04, before the letter n of the first hand-written `\n`; and `\d` re-laid the one foreign
address (New Zealand, not the `\e` country) in that country's format, name, street, city and country on four lines. Both stay
unexplained (section 9): docx4j prints the n alone and ignores `\d`; `AddressProbeWordTest` encodes the two as deviations.

Decisions, built the same day:

| # | Decision | Built |
|---|---|---|
| D27 | LISTNUM, AUTONUM, AUTONUMLGL and AUTONUMOUT are computed for a whole document at once, in document order over the main document part, both field shapes (`org.docx4j.model.fields.ListNumbers`, with the measured rules above: two counter sets, implicit parents, the heading level from the effective outline level else the style's name or id), and looked up by the field's node (`FieldContext.node`: its `w:fldSimple`, or the run or `w:fldChar` beginning a complex field; without a node, the next in document order). Word paints these and SYMBOL live and never writes a result for them, so the output path writes them into its working copy before the writers paint (`Preprocess`, `FieldUpdater.only(FieldUpdater.LIVE)`, only where the document holds one); the text model computes them under `fields: update` (FieldValues); `FieldUpdater` writes them. A field in a header or footer keeps its cached result | yes |
| D28 | SYMBOL (`org.docx4j.model.fields.SymbolField`): the character of the code (Windows-1252 for a text font; a symbol-encoded font's glyph as the private-use character and, written, Word's `w:sym`; `\u` a code point), the `\f` font and `\s` size on the result run, `###` in bold for a code Word cannot read; the text model shows the private-use character, as it does a `w:sym` | yes |
| D29 | An ADDRESSBLOCK or GREETINGLINE format's line breaks are real line breaks, as Word writes its own (`w:cr` in the instruction runs, or `&#xA;` in a `w:fldSimple`): `FieldsPreprocessor.extractInstr`, the evaluator's nested-field reader and the text model read a `w:cr` or `w:br` in an instruction as a line break; a hand-written `\n` prints the letter n, as Word prints it; a greeting prints on one line; docx4j's default block format carries real breaks | yes |

Built: `ListNumbers`, `SymbolField`; `FieldContext.node/listNumbers`; `FieldEvaluators` registers the five; `FieldUpdater` (`LIVE`,
`hasLiveFields`, the node per field, SYMBOL's run); `FieldValues` and `Extractor` (the node, `w:cr` in a code); `Preprocess`
(the live fields written before `FieldsCombiner`); `AddressFields` (real breaks, `\n` as n). Tests: `Phase3ProbeWordTest`
(L01 to L19, A01 to A15, S01 to S14 against the PDF; R01 to R15; the text model and the HTML writer), `AddressProbeWordTest`
(Word's own formats; the two deviations). Unmeasured items added to section 9. Phase 3 DONE.

### 14.5 Round 3 (Jason's merge of the attached file, 2026-10-29 06:04) and D26 built

`address-probe-r1-attached-merged.docx` (fixture beside the others): Word's own Address Block and Greeting Line merged over
the four records. The block prints `Acme Pty Ltd / Mr John Smith Jr / 1 Main St / Suite 2 / Sydney NSW` (no country: `\c 2
\e "Australia"`), `Ms Jane Doe / 2 High St / Auckland / New Zealand`, `Beta Inc / Pat Lee / 3 Low Rd / Perth WA`, `Sam /
4 Side Ln / Hobart TAS`, one paragraph a line; the greeting `Dear Mr Smith,` `Dear Ms Doe,` `Dear Pat Lee,` `Dear Sam,` on
one line each. So the line break Word's own greeting format carries before `_AFTER_` does not print (as built), and with
Word's own format `\d` changes nothing visible even for the New Zealand record (the format's own lines are the country
layout's); round 1's `\d` re-layout concerned only the hand-written `\n` format. docx4j's merge of the attached file gives
the same lines, letter for letter (`AddressProbeWordTest.wordsOwnFieldsMergedAsWord`), the block's as line breaks in one
paragraph (D23).

| # | Decision | Built |
|---|---|---|
| D30 | Word writes its own GREETINGLINE as a `w:fldSimple`: the mergers evaluate a simple field whose name is in `MERGE_EVALUATED` (not NEXT, NEXTIF, SKIPIF, SET, ASK, FILLIN, which move the record or prompt and are complex in Word's files) with the record in the context, and unlink it to its text (`MailMerger.mergeSimpleFields`), or give it the text as its result where the fields are kept | yes |

D26 built the same day (Jason: "D26 as recommended"): `OpcPackage.setSourceName(String)` and `setSourceSize(Long)`, request
attributes beside the zone (nothing saved; a server sets them per request); `FieldContext`'s constructor takes them as the
defaults of `fileName` and `fileSize`, the per-call setters overriding; so the text model under `fields: update` and the PDF
and HTML writers show the same FILENAME and FILESIZE as the updater (`Phase3ProbeWordTest.sourceNameAndSizeOnThePackage`).
WD 0.39's "only where the caller supplies them" reads "the package's source name and size where set" (sent to the portfolio).

### 14.6 ASK and FILLIN in the text model (python-64's finding, 2026-10-29)

Drafting its port, docx4j-python found that WD 0.38's REQ-506 says the text model computes ASK and FILLIN under
`fields: update` (an ASK defines its bookmark with its `\d` default and shows nothing; a FILLIN shows its default) while
`FieldValues.COMPUTED` omitted both. The WD's reading is the intended one: `FieldValues` now computes them (an ASK as a SET,
shown as nothing; a FILLIN its default, nothing without one; the text model supplies no answers, so the default is always
what is used), `FieldValuesPromptsTest`. The cached mode is unchanged (an ASK's stored result is read; whether Word shows
an ASK's stored result at all is unmeasured, section 9).

### 14.7 The IF-field probe and the writers' nested and resultless fields (Jason, 2026-10-29, through the portfolio)

Jason's question: can an IF field give different extracted text, updated or not? `IfFieldProbeBuilder` wrote
`iffield-probe-r1.docx` (16 cases, each a field whose stored result is the losing branch: a literal IF, the same as a
`w:fldSimple`, conditions on DOCPROPERTY, on a DATE storing 2030, on a REF whose bookmark was edited after the last update,
a formula and a COMPARE stored wrong, a locked IF, an IF with no result, one with no separate, a nested DOCPROPERTY as the
true branch, a hidden true branch, a hand-edited result, a control, an empty `w:fldSimple`, a wildcard) and a copy with
`w:updateFields`; README with Word's steps (as-is, F9, updatefields yes and no, each as plain text and docx) and the expected
text per mode; `expected.tsv`; fixtures `fields/cr067-iffield` (f45a50943), staged on the share `__tmp/cr067/iffield/`. Word's
four texts await Jason's run; the portfolio runs the third-party extractors; `docx4j-extractors.tsv` has docx4j's.

docx4j's own extractors answered as the WD says: the text model's cached mode gives the stored result in every case, its
update mode the computed one (a locked field kept; DATE never computed, so the date condition reads the stored 2030 and
says EXPIRED where Word, rewriting DATE on open, says VALID), the markup view the stored one. The writers showed two defects,
fixed the same day (`FieldsCombiner`, `IfFieldProbeWritersTest`, `ResultlessIfFieldTest`):

| # | Decision | Built |
|---|---|---|
| D31 | The output path's combiner folds a nested field into its outer field's instruction as one operand (its stored result, else its evaluated one, as the updater and the text model do, D4), and the outer becomes one `w:fldSimple` whose result the writer paints; before, the nested field was combined on its own and painted beside the outer's stored result (`ProbeTITLE_MISMATCH`, `2026STALE_OUTER` in HTML and PDF). A nested field after the outer's separate is part of its result, as before | yes |
| D32 | A complex field with no stored result is combined into an empty `w:fldSimple` where the evaluator knows the field (as the layout fields were), so that the writer evaluates it (D3: a field with no stored result is evaluated); and a resultless IF is dropped before combining only where a nested field no evaluator knows (PAGE, NUMPAGES) makes it undecidable; before, every resultless IF was dropped and any other resultless field painted nothing | yes |

**Word's run (Jason, 2026-10-29 07:34 to 07:37; `scored.tsv`, the four texts and two docx in the fixtures).** Word's open
itself recomputed some fields before any update, and its save wrote the new results into the file (`-asis.docx`): the
literal IFs (C01, C02, C08, C09, C13, C15, the two `w:fldSimple` ones rewritten as complex fields, the resultless ones given
a separate and a result), the IF over a DATE (its nested DATE 2030 rewritten 2026, the outer STALE_OUTER rewritten VALID),
and the hidden-branch IF (its separate and result removed, so nothing shows). It kept the stored result of the IF over a
DOCPROPERTY (C03) or a REF (C11), the nested-branch IF (C10), the formula (C05), the COMPARE (C06), the locked IF (C07)
and the wildcard IF (C16). So Word's as-is text equals its updatefields-no text and is not the file as built; F9 and
updatefields-yes computed every case but the locked one (C04 VALID, where docx4j's update says EXPIRED because the text
model never computes DATE). Against this surface docx4j's cached mode (and the fixed writers) differ on C01, C02, C04, C08,
C09, C12, C13 and C15: the cases Word recomputes on open. Round 2 (`IfFieldProbeRound2Builder`, `iffield-probe-r2.docx`,
18 cases, staged with `README-round2.txt`) separates the hypotheses for that open-time recomputation: string equality
without a wildcard, the wildcard IF with its true branch stored, other numeric operators, a false condition, inequality,
case, an IF over a formula, over PAGE, over NUMPAGES, over a DOCPROPERTY whose stored result is already right, over a
missing DOCVARIABLE, `w:dirty` on a wildcard IF, a formula, a DOCPROPERTY and a simple field, COMPARE with C01's operands,
quoted numbers, the wildcard IF as a simple field. Not built until measured: whether docx4j's cached mode (and the
writers) should recompute what Word recomputes on open is a WD question (D27's rule against DATE applies to the DATE case;
the literal IFs are new).

**Round 2 (Jason, 2026-10-29 07:50 to 07:52; `scored-round2.tsv`).** Word asked whether to update fields on open (the
`w:dirty` cases); answered No, it recomputed none of the 18: every as-is text is the stored result, the string equality,
the other operators, the false condition, the quoted numbers, the wildcard with its true branch stored (kept), the IFs over
a formula, PAGE, NUMPAGES, a right DOCPROPERTY and a missing DOCVARIABLE, COMPARE, the dirty fields (their flag cleared,
their results kept) and the simple fields (rewritten as complex fields) alike; a nested field's stored result was rewritten
as `w:instrText`, Word's own form, with the same text. Yes to the prompt updated every field in the document, not only the
dirty ones (the IF over PAGE then said PAGE_OTHER, where F9 says PAGE_ONE: PAGE before layout). F9 computed every case
(the string comparison case-sensitive, as phase 2 measured). So round 1's open-time rewrite is not a rule about literal
IFs: round 1 held two fields with no result at all (C08, C09), which Word had to compute to show anything, and every
field with the same instruction (C01, C02, C13, C15, `IF 1 = 1 "APPROVED" "REJECTED"`) came out with that result; C04
followed its DATE, which Word updates on open; C12's stays unexplained (section 9). Round 3 (`IfFieldProbeRound3Builder`,
`iffield-probe-r3.docx`, 15 cases, staged) tests that reading: a stored-wrong field alone, stored-wrong fields with a
resultless twin after, before, differing in whitespace, or with other branches, for an IF, a formula, a DOCPROPERTY and
a string IF, and a resultless field of its own.

**Round 3 (Jason, 2026-10-29 08:01; `scored-round3.tsv`).** No prompt; nothing recomputed; every stored result kept, every
resultless field (a separate with nothing after it) shown as nothing and left so in the file; no XML changed. So a resultless
field with a separate does not make Word compute it on open, and nothing is shared by instruction from one. The shape round
3 lacked is round 1's C09, a field with no separate at all, whose instruction is exactly the one every field round 1's open
rewrote (C01, C02, C08, C13, C15: `IF 1 = 1 "APPROVED" "REJECTED"`). Round 4 (`IfFieldProbeRound4Builder`,
`iffield-probe-r4.docx`, 15 cases, staged) tests that: a no-separate field alone; stored-wrong twins of a no-separate field
(after, before, with other branches), for an IF, a formula, a DOCPROPERTY and the wildcard IF; a separate-but-empty twin
as the control; a stored-wrong IF with no twin.
