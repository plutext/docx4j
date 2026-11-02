# CR-074: clause numbers: full-context labels, a lookup from a label, and REF's paragraph-number switches

Status: PROPOSED 2026-11-03. Asked for by the portfolio session, relaying Jason's rulings of 3 November 2026
(portfolio 8e6a8fc; `mcp_strategy.md` §12.3 S1 to S6 "as recommended", `mcp_tool_vocabulary.md` §3, the clause
address form): S2 is docx4j's first, and the oracle for S5's cross-references. The portfolio offered a small CR or a
phase of CR-046's model. It is a CR of its own because it reaches past the text model into the numbering model
(CR-014's package) and the field evaluator (CR-067). Phase 0 DONE 2026-11-03 (Jason's Word run): Word's rules for
the full, no and relative context, `\t`, `\p`, `\d`, the tracked-change view and its own insertion are measured
(§3.1). Decisions in §5, for Jason, updated for the results, with D11 (the revision view) from Jason's rule of 3 November; phases in §6.

## 0. Summary

Lawyers cite clauses by number, and Word computes the numbers when it draws the page, so "2.01(a)" appears nowhere in
the file. Jason ruled (S1) that a clause number becomes an address across the portfolio, `clause:2.01(a)`, resolved
by the engines: docx4j first, then core-ts and docx4j-python port it to docx4j's goldens, and docx4j-mcp (S3) and
ts-editor (S4) use it. docx4j already computes each paragraph's own label, "(a)" (WD REQ-510, CR-014). Three things
are missing:

1. **The full-context label**: "(a)" with the levels above it, as Word's cross-reference "paragraph number (full
   context)" (REF `\w`) shows it: `2.01(a)`; `Article III` for a numbered heading styled so.
2. **A lookup**: from a label to its paragraph, with an error naming the candidates when no paragraph carries the
   label, or several do (a restarted list). It never guesses. Then the **scope**: the paragraph and everything after
   it up to the next label at its level or above.
3. **REF's paragraph-number switches** (`\w`, `\r`, `\n`, `\t`) in CR-067's evaluator. docx4j refuses them today
   and keeps the stored result. They are Word's own definition of item 1, and S5's oracle: ts-editor will replace
   typed text ("see clause 4.2") with a REF field over a bookmark on the numbered paragraph. docx4j-python asked for
   one probe to cover both items 1 and 3, so that one port closes both in python (portfolio relay, 3 November).

Word's join of the levels is not documented beyond "full context", so phase 0 measures it. Word updates 655 REF
fields over bookmarks on 79 numbered paragraphs in twelve list shapes, and inserts its own cross-references for the
shape of field and bookmark it writes (§3). docx4j then reproduces Word's results exactly (§4).

## 1. What exists (read 2026-11-03, 17.3.6-SNAPSHOT)

- **The label.** `Extractor` counts each numbered paragraph through `Emulator.getNumber` with the story's
  `NumberingState`. `TextBlock.getListLabel()` (under `list_labels: true`) holds the level's text with its counters
  filled in (`ListNumberingDefinition.getCurrentNumberString`, `w:isLgl` applied) **followed by the level's suffix**
  (the tab). `TextNumbering` holds the `numId`, level, format, level text, start and `value`, and the list (the
  `w:abstractNum`). On cn-1 docx4j's labels are those the levels define: `(a)` for L6, `Section 2.01` for A6,
  `1.02` for S4 (whose level 1 was skipped, then counted, as CR-014 measured), and `1.` for both R1 and R4.
- **Nothing joins the levels.** `getCurrentNumberString(level j, state)` gives the label level j would show now, so
  the parts are computable. No code puts them together, and nothing says which parts Word keeps.
- **REF.** `FieldEvaluators` refuses `\n`, `\r`, `\w`, `\p` and `\f` (`REF_UNSUPPORTED`), so those fields keep
  their stored result. `FieldContext` knows each bookmark's text, not the paragraph it is in, and not where the field
  stands. `\r` and `\p` need both.
- **No lookup.** The text model's addresses are ordinals and `w14:paraId` (REQ-405, REQ-518).
- **The view.** The text model counts in its view (`accepted` by default), so cn-1's T2 (a deleted numbered
  paragraph) is not counted. Whether Word counts it depends on what the window shows. Phase 0 measures all three
  views.

## 2. What Word documents, and what it doesn't

Word's field reference for REF: `\w` "the paragraph number in full context from anywhere in the document"; `\r` "in
relative context, or relative to its position in the numbering scheme, without trailing periods"; `\n` "the entire
paragraph number ... without trailing periods; no information about prior levels unless included in the current
level"; `\t` makes `\n`, `\r` and `\w` "suppress non-delimiter and non-numerical text"; `\p` appends "above" or
"below"; `\h` makes the result a hyperlink; `\d` gives the separator for sequence numbers. Unmeasured, and needed:

- **The join.** Which characters of each level's label survive (the period of `1.`, the parentheses of `(a)`, a
  colon), and whether a level that already shows the levels above (`%1.%2`) repeats them. Also: words in the level
  text (`Article %1`), a level with no `%n` (`NOTE`), a bullet, an empty level text, `w:isLgl` at a level above,
  and a level whose parent level never appeared.
- **Relative context.** Which levels `\r` drops for a field in a sibling, a cousin, a deeper or shallower paragraph,
  another list, or outside any list.
- **Bookmarks.** A bookmark on a paragraph that is not numbered, an empty one, and one that crosses paragraphs.
- **Word's insertion.** The field code Word writes for each Insert Cross-reference choice, and the hidden `_Ref`
  bookmark (its name, where it starts and ends, and whether a second reference reuses it).

## 3. Phase 0: the Word probe (BUILT 2026-11-03, staged for Jason's run)

`docx4j-core-tests/src/test/resources/clause-numbers/` (README.md there has the cases). It is staged at
`~/vbShares/Office 2016/clause-numbers/` (`X:\clause-numbers` on the VM) with `RUN.txt`.

- **`cn-1-labels.docx`** holds twelve list shapes: legal 1. / 1.01 / (a) / (i); Word's outline 1. / a. / i.; the
  same with parentheses; 1. / 1.1. / 1.1.1.; Article I / Section 1.01 / (a), linked to heading styles; a list
  style; `w:isLgl` under upper roman; skipped levels; a restarted list; words over a level with no `%1`; not
  numbers (a bullet, `Step %1:`, `NOTE`, an empty level text, `1-1`); tracked changes. 79 numbered paragraphs, each
  id bookmarked. After the lists come field lines: for every paragraph, `\w`, `\n`, `\r`, `\w \t`, `\n \t` and
  `\r \t`, and for four of them also `\p`, `\r \p`, `\w \d "-"` and `\w \h`. Inside chosen numbered paragraphs sit
  `\r` fields to others in their case, which give relative context from inside a list. Three edge bookmarks.
  655 fields in all.
- **`cn-2-insert.docx`** holds the same lists without bookmarks or fields. Word inserts its own cross-references
  there (Reference type "Numbered item": full, no and relative context; hyperlink or not; above/below; a separator;
  the paragraph text).
- **`clause-numbers-probe.ps1`**, the method of `cr068-probe.ps1`, logs the following: every paragraph's label as
  Word draws it (`ListFormat.ListString`); the dialog's numbered items (`GetCrossReferenceItems`); every field's
  code and result after `Fields.Update`; case T again with no markup and with the original shown; every bookmark,
  hidden ones included. It saves `cn-1-labels-word365.docx` and `cn-2-insert-word365.docx`.

The Word saves become the goldens: every REF result in `cn-1-labels-word365.docx` is one assertion for phases 1 and
2, and `cn-2-insert-word365.docx` is the shape phase 3 writes. The ports take both, as they took CR-073's pairs.

### 3.1 Results (DONE 2026-11-03, Jason's run; the probe's README has the table)

All 655 fields were updated, with no step failing. Word's drawn labels equal docx4j's for all 79 paragraphs. The
rules §2 asked for:

- **Full context**: the paragraph's own label after its parent's full context. The parent is the nearest earlier
  paragraph of the same list at a shallower level, and a skipped level contributes nothing (`1(a)`, `1.02(i)`,
  `II(a)`). A level whose text shows the levels above (`%1.%2`, `%1.%2.%3.`) has no parent part. The parts are
  joined with nothing between them (`Article I1`, `Step 1:a`, `1-1A`, `1)a)i)`). A period ending a part is dropped
  before a part starting with `(` (`1(a)`) and kept before a letter (`1.a.i`); one trailing period at the end is
  dropped. A bullet gives its character, `NOTE` gives `NOTE`, and an empty level, or a bookmark on a paragraph that
  is not numbered, gives `0`.
- **No context**: the own part, its trailing period dropped.
- **Relative context**: drops the leading parts whose paragraphs are ancestors of the reference's position, matched
  by paragraph, not value (a restarted list's `1.` is not R1's). The position is the field's paragraph if it is in
  the target's list, else the last paragraph of that list before the field.
- **`\t`** drops the level texts' words and their spaces, keeping numbers and punctuation (`I`, `1.01(a)`, `1:`);
  empty is `0`. **`\p`** appends ` above` or ` below`. **`\d "-"`** separates the parts (`2.01-(a)`).
- **View**: the accepted view's numbers whatever the window shows. A bookmark in a paragraph whose mark is deleted
  resolves to the paragraph it merges into.
- **Word's insertion**: a hidden `_Ref` followed by nine digits, over the paragraph's text without the label or the
  mark. It is reused while it still spans the whole text. The code is ` REF _Ref… \w \h ` (`\n`, `\r`; `\p`
  and `\d` before or after `\h` as the dialog orders them), with no MERGEFORMAT: a complex field as a hyperlink,
  a `w:fldSimple` otherwise. The result is computed at the insertion point.

Unmeasured, and left so unless a document needs them: a period before a digit or punctuation other than `(`; a level
text that shows some levels above but not all (`%2.%3` at level 2); RTL lists.

## 4. Approach (proposed; the rules come from phase 0)

**4.1 `ParagraphNumber`, in `org.docx4j.model.listnumbering`.** This is an immutable snapshot taken when the numbering
model counts a paragraph: the list (`w:abstractNum`), the `w:num`, the level, the counters of levels 0 to that level,
and the levels' definitions. It answers `label()` (REQ-510's, no suffix), `noContext()` (`\n`), `fullContext()`
(`\w`), `relativeTo(ParagraphNumber from)` (`\r`, with `from` null for a field outside any list), each also in the
`\t` form. All of these follow Word's rules exactly as phase 0 measures them. `Emulator.NumberingResult` gains it, so
every consumer counting through the emulator has it.

**4.2 The clause, in the text model.** `TextNumbering.getClause()` returns the full-context label (null where Word
gives none: a bullet, an empty level). It goes into the JSON output's `numbering` object, as a WD addition for the
portfolio to write (D9). Plain text and Markdown do not change: `list_labels` keeps the level's own label.

**4.3 `Clauses`, in `org.docx4j.model.text`.** `Clauses.of(TextResult)` lists every clause: label, block, address,
level and list. `find(String)` returns one clause, or throws `ClauseLookupException` naming the candidates: every
paragraph that carries the label when several do, with their addresses; the near misses when none does (D3).
`Clause.scope()` returns the blocks from the clause's paragraph up to the next paragraph of its list at its level or
above (D4). `Clauses.of` counts in the result's view, so in `accepted` a deleted numbered paragraph carries no clause
and those after it renumber (D6). The same label can name different paragraphs in different views, so every clause and
every lookup error names its view (D11). docx4j-mcp passes the exception's message through as its tool's refusal (its CR §23.7,
c5ece24), so the message is written for an agent to read: the query, the candidates with their addresses, the near
misses.

**4.4 REF in CR-067's evaluator.** `\n`, `\r`, `\w` and `\t` are evaluated from one walk of the main document in
order, as `ListNumbers` walks it for LISTNUM. The walk maps each bookmark to the paragraph it starts in, and each
field to the paragraph it stands in, so `\r` (and `\p`, if measured) has its position. `REF_UNSUPPORTED` shrinks to
what phase 0 leaves unmeasured (`\f` at least).

**4.5 A writer for Word's cross-reference (D8).** `CrossReferences.toParagraphNumber(target paragraph, where,
context, hyperlink)` adds the hidden `_Ref` bookmark to the target, or reuses one, and writes the REF field with
Word's switches and its result, all as `cn-2-insert-word365.docx` shows Word writing them. core-ts ports this for
S5's engine half, ts-editor's manual-to-real cross-reference.

## 5. Decisions (for Jason; recommendations in bold)

| # | Decision | Recommendation |
|---|---|---|
| D1 | What the clause is | **Exactly what Word's REF `\w` shows for the paragraph**, as phase 0 measures it, not a docx4j join. The address then agrees with every cross-reference Word computes |
| D2 | Where the API lives (docx4j-mcp's question 1) | **`ParagraphNumber` in the numbering model; `TextNumbering.getClause()`; a `Clauses` class over a `TextResult` for the lookup and the scope** (§4.1 to §4.3). The ports mirror the same three |
| D3 | How a label matches | **Exact match on the clause; failing that, on the `\t` form (`3.01` for `Section 3.01`) where exactly one paragraph's matches. The query is trimmed and one trailing period dropped (an agent reading REQ-510's `1.` may cite it). No case folding.** None or several matches is an error naming the candidates; near misses (other case, other form) are listed as suggestions and never resolved |
| D4 | What a scope holds, and where it ends (docx4j-mcp's question 2) | **Every block, tables included, from the clause's paragraph to the next paragraph of the same list (`w:abstractNum`) at its level or above, or the end of the story.** Alternatives: (b) a numbered paragraph of any list ends it (a bullet list inside clause 3 would then cut clause 3); (c) a paragraph of a higher outline level ends it (an unnumbered "Background" heading would). With (a), the last clause's scope runs to the end, over schedules in a list of their own |
| D5 | Which paragraphs carry a clause | **Those whose level shows a number**: a `%n` in the level text and a format other than `bullet` or `none`. Word's `\w` gives a bullet its character, `NOTE` its word and an empty level `0` (§3.1); none of these is an address an agent would cite, and `0` or `NOTE` would be ambiguous at once. REF evaluation (D7) still gives Word's result for them |
| D6 | Stories and views | **The main document's blocks as the text model walks them** (body, tables, content controls; not text boxes, headers or notes), **in the text model's view**. Word's REF results are the accepted view's in every display (§3.1), which is the text model's default, so the default clauses are Word's |
| D7 | REF's switches | **`\n`, `\r`, `\w`, `\t`, `\p` and `\d` evaluated** from the same model, as §3.1 measures them; `\h` changes nothing in the result; `\f` stays unsupported. One oracle for the label and the field, as python asked |
| D8 | A cross-reference writer | **Yes, small (§4.5)**, so that S5's engine half ports a tested writer, not a description. Alternative: goldens only |
| D9 | The WD | **Ask the portfolio for a `clause` member in the JSON `numbering` object and a REQ for its rule** (the full-context form is not in REQ-510) |
| D11 | The revision view (Jason, 3 November 2026, portfolio e314871, `docs/clause_numbers.md` §2 V1: "semantics of 'find clause 10.3' when there are tracked changes must depend on the specified view (as if accepted, rejected etc)") | **A clause belongs to a paragraph under a view, and the view is the `TextResult`'s.** `Clauses.of(TextResult)` counts in the view the result was extracted under (`TextOptions.view`, `accepted` by default), and `Clauses.of(pkg, TextView)` is the shorthand. Every `Clause` and every `ClauseLookupException` names its view (`getView()`; the message says "under the accepted view"), so a hit says how it was resolved. `TextNumbering.getClause()` is the block's clause in its result's view. The markup view counts as accepted (WD 0.64, REQ-510); docx4j's markup view counts a paragraph whose mark is deleted today (case T: T2 `2.`, T4 `3.`), and that fix is its own item, waiting on Jason. The original view counts the document as if every change were rejected; Word's drawn count there is unmeasured (the hand look under Review > Original is open with Jason) |
| D10 | The suffix (docx4j-mcp's question 3) | **The clause holds no suffix**, nor does `ParagraphNumber.label()`. `TextBlock.getListLabel()` keeps the tab it carries today, which is what plain text emits under `list_labels` |

## 6. Phases

0. **The Word probe.** DONE 2026-11-03 (§3, §3.1): Jason's run, Word's saves and log committed beside the probe,
   the readings in its README, D5 to D7 updated for them.
1. **`ParagraphNumber`** and REF `\n`, `\r`, `\w` and `\t` (§4.1, §4.4). Every REF result in
   `cn-1-labels-word365.docx` is asserted, docx4j re-evaluating Word's saved fields. Its labels equal Word's
   `ListString`.
2. **The clause and the lookup** (§4.2, §4.3), with tests on cn-1. Each clause equals Word's `\w` result; R's
   duplicates raise the ambiguity error with both candidates; D3's forms and D4's scopes are tested.
3. **The writer** (§4.5), held to `cn-2-insert-word365.docx`: the bookmark, the field code and the result. Word
   opens docx4j's output without a repair and updates the field to the same result (an Office check).
4. **Hand-offs** through the portfolio: core-ts (the S2 port and S5's engine half), docx4j-python (the S2 port and
   REF's switches), docx4j-mcp (S3, waiting on this reaching Central in 17.3.6), ts-editor (S4 and S5, through
   core-ts), and the WD addition (D9).

## 7. Risks and out of scope

- Word's join may depend on things the probe holds constant (the document's language, a level's `w:lvlJc`,
  right-to-left lists). The probe is en-AU and left to right. RTL lists and lists in other languages are out of
  scope until a document needs them.
- Out of scope, as ruled (S5): resolving a typed mention ("see clause 4.2") to a paragraph, and warning when an edit
  renumbers what the text cites. LISTNUM fields are not numbered items, and their numbers (CR-067 phase 3) are not
  clauses.
- A numbered paragraph inside a deleted or moved region is counted as the view counts it (D6). Word's behaviour
  there is case T's.
