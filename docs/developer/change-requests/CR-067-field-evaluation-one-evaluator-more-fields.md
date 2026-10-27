# CR-067: one field evaluator, and the fields worth adding

Status: ACCEPTED 2026-10-28 (Jason: "CR decisions as recommended; please commit then start implementation"): D1 to D11 as recommended, D2 (c); phase 0 IN PROGRESS 2026-10-28.
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
| D11 | The inspector's set (WD REQ-506) | (a) grows with the evaluator, the WD's list updated each phase through the portfolio; (b) frozen at CR-050's set | (a); the WD's rule ("neither layout nor external access") already describes the evaluator's boundary. |

## 6. Phases

| Phase | Work | Status |
|---|---|---|
| 0 | The enabler: `FieldInstruction`, `FieldContext`, `FieldEvaluator` (D1, D2, D10); `FieldUpdater`, `FieldValues` and the DATE, TIME, PRINTDATE and DOCPROPERTY writer handlers moved onto it with no behaviour change; `FldSimpleModel` and the signatures carrying it deprecated, every in-tree user moved (D2); the `preserveResult` fold (D9); parser tests from the corpus instructions; every existing field test green | in progress 2026-10-28 |
| 1 | Tier 1: CREATEDATE, SAVEDATE, REVNUM, INFO, SECTION, NOTEREF, REF, QUOTE, USERNAME, USERINITIALS, USERADDRESS, each in all three consumers, each with a Word measurement (a probe docx saved by Word, its results the oracle); REF inside a hyperlink if the measurement needs it (D9) | proposed |
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
treats a field as one unit and its CR-012 extraction takes `fields: update` from the WD). Each phase's field list and
Word measurements go to them when the phase lands; objects-ts is unaffected (no schema change). The MCP server's
`extract_text` takes the inspector's set through the WD (D11). The portfolio registers this CR as `docx4j/CR-067` with
phases 0 to 4.

## 9. Unmeasured

Rules Word has not been seen to apply, kept here until a probe round measures them (the CR-043 D5 convention):

- IF and COMPARE: string against numeric comparison; wildcards; case; number parsing per language (D6).
- `=`: the function set, cell and bookmark references, error text (D6).
- A REF with `\n`, `\r`, `\w`, `\p` or `\f` (the inspector keeps the cached result for these today).
- SEQ `\s` (reset at a heading level).
- What Word shows for USERNAME and the other user-information fields when the user is unknown.
- INFO with each of its argument names, against the document-information field of the same name.
