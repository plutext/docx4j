# CR-063: content-control placeholders written in Word's form

Status: PHASES 1, 2 AND 4 DONE 2026-10-25 (§12, §13; Claude Fable 5.1, session docx4j-25); D8 (a) and D9 (a) decided (Jason,
2026-10-25: "D8 we go (a)", "D9 choose (a)"), D9 done (§15); phase 3's Office-open check passed (§14: the one repair was the fixture's own Flat OPC chunk, pre-CR-063, fixed in the
fixture); check 65's writes on the new output pass (§17, Jason's run 2026-10-25): phase 3 DONE. D10 (the Placeholder Text style in the main
styles part) decided (a) and done 2026-10-25 (§17). Check 71 (a bound picture control whose node is empty, §18, Jason's run 2026-10-25): the
NonXSLT and StAX binders' text placeholder under a picture control is content Word repairs (a defect); D11 PROPOSED, recommended (b). ACCEPTED 2026-10-25 (Jason: "CR-063 D1 to D6 accepted as recommended, please implement"): D1 to D6 as
recommended in §6, D7 decided earlier. PROPOSED 2026-10-24, drafted by Claude Opus 5.5 (session
docx4j-25) at docx4j-core-ts's request, from Jason ("Jason asks that docx4j consider writing Word's form; it's your
decision"): its checks 63 and 64 compared the bare placeholder docx4j's binding writes with Word's form. (Check 63's
lost writes turned out to be Word's Design Mode, so the case for the change is the template's placeholder text and
Word's reading, not lost writes: §11.) Recommended: yes (§6), revised after core-ts's review (§11). Phase 0 done
2026-10-25: core-ts's checks 65 and 66 (§9, §10). D7 decided (a), the same answer as core-ts's D9 (Jason, relayed by
core-ts). The implementation is OpenDoPE binding, which CLAUDE.md gives to Fable.

## 0. Summary

A content control showing its placeholder comes in two forms:
- **Word's form:** `w:placeholder` names a glossary entry (`w:docPart`) holding the placeholder's text, and the
  control's content is a copy of it, with `w:showingPlcHdr`. Every control Word makes is this form, naming
  `DefaultPlaceholder_-1854013440` ("Click or tap here to enter text.").
- **The bare form:** `w:showingPlcHdr` and a Placeholder Text run, with no `w:placeholder`. docx4j's binding writes
  this for an empty value, and drops any `w:placeholder` a template had.

**Corrected 2026-10-25 (§11).** This CR was written on core-ts's check 63, which found that in the bare form Office
JS's writes into a bound control never reached its node and were lost on reopening. Check 63 ran in Word's Design Mode
(its saves carry `<w:formsDesign/>`). Run again with Design Mode off (core-ts checks 67 and 58), the bare form behaves
as Word's form: writes are plain, clear the flag, reach the node and survive the reopen. What still argues for Word's
form:
- the template's own placeholder text survives binding (today docx4j replaces it, and drops the reference);
- Office JS's `placeholderText` reads the entry, where it reads null in the bare form;
- a control emptied in Word shows its entry; in the bare form it shows Word's filler (five spaces, flag on), and
  leaving Design Mode, Word refused over a bare-form run-level placeholder with an empty `w:sdtContent`.

Proposed: docx4j's binders keep `w:placeholder`; an empty value shows the placeholder the control names, from its
glossary entry, as Word does; a control that names none gets Word's default entry, with the glossary part added where
missing. Checks 65 and 66 found what Word needs of the glossary (§9, §10).

## 1. What Word does (core-ts CR-002 §63.2, §63.5; Word 365 16.0.20326.20232, Jason's runs, 2026-10-24)

**Check 63 ran in Design Mode** (corrected 2026-10-25, §11): what follows under it is Word's Design Mode, not Word.
With Design Mode off (checks 67, 58) the bare form behaves as Word's form does under check 64 below.

**Check 63, the bare form** (52 Office JS writes, untracked and tracked; Word's saves in core-ts `test/check63/`):
- A text write into the control replaces the placeholder and clears `w:showingPlcHdr`, but the control's own
  `insertParagraph` leaves the flag on, so Word reads its text as none.
- In a rich text control the Placeholder Text style stays on the new text (with `w:color w:val="auto"` for some
  writes); in a bound `w:text` control it goes.
- Tracked, Word records the new text's insertion and the placeholder run's deletion.
- **A bound control's node is left empty** by every write, in Office JS's read and in Word's save. Reopened, Word
  refilled each control from its node: Jason saw empty controls, and the tracked writes' revisions were gone too.

**Check 64, Word's form** (`w:placeholder` naming `DefaultPlaceholder_-1854013440`, check 34's glossary; and controls
Word made with `insertContentControl`; Word's saves in core-ts `test/check64/`):
- Office JS's `placeholderText` reads the glossary entry's text.
- Every write takes the placeholder away and leaves plain text, with no Placeholder Text style. Tracked, only the
  insertion is recorded.
- **Every write reached the bound node**, an empty node's as much as a value's, and the controls showed the writes on
  reopening.

Word's default entry, as its glossary holds it (check 64's file):

```xml
<w:docPart><w:docPartPr><w:name w:val="DefaultPlaceholder_-1854013440"/><w:category><w:name w:val="General"/>
<w:gallery w:val="placeholder"/></w:category><w:types><w:type w:val="bbPlcHdr"/></w:types><w:behaviors>
<w:behavior w:val="content"/></w:behaviors><w:guid w:val="{7D21F9BC-12A3-463E-B08F-7DD4FFC77A03}"/></w:docPartPr>
<w:docPartBody><w:p><w:r><w:rPr><w:rStyle w:val="PlaceholderText"/></w:rPr><w:t>Click or tap here to enter
text.</w:t></w:r></w:p></w:docPartBody></w:docPart>
```

The glossary there is a full set of parts: document, styles, settings, webSettings, fontTable.

**Not measured when this CR was written** (check 65 answered all four, §9):
- whether Word needs the glossary entry, or only the `w:placeholder` reference (check 65);
- whether a minimal glossary (the document part alone, one entry) is enough (check 65);
- how Word names and stores an entry for placeholder text of its own (check 65);
- whether typing by hand, rather than writing through Office JS, into a bare-form bound control reaches the node. That
  is how most people meet a docx4j-bound document; check 65 asks it too (§5, phase 0).

## 2. docx4j today (a3cfc9789)

- **`w:placeholder` dropped.** `bind.xslt` (line 1220, `<xsl:template match="w:placeholder"/>`, "so missing data
  does not result in Click here to enter text in Word") removes every one in the part, bound control or not. The
  non-XSLT and StAX binders remove it from bound text controls only (`BindingTraverserCommonImpl.postBindSdtPrHygiene`),
  so the binders differ already. The rule's reason no longer holds: since docx4j shows "Click here to enter text."
  for missing data itself, Word showing it changes nothing.
- **An empty value** gets `placeholder.xml`'s run: "Click here to enter text." (Word 2010's wording; Word 365 writes
  "Click or tap here to enter text."), in the Placeholder Text style, plus `w:showingPlcHdr`, added where the
  content is found to carry that style (`bind.xslt` line 1155; `postBindSdtPrHygiene`). The run is configurable
  (`docx4j.model.datastorage.placeholder`, default `OpenDoPE/placeholder.xml` on the classpath).
- **The run comes from `BindingTraverserXSLT.createPlaceholder(rPr, sdtParent)`,** called by:
  - the plain-text inserters (`ValueInserterPlainTextImpl`, `ValueInserterPlainTextForOpenAPI3`; pluggable, so a
    deployment's own inserter may call it too);
  - the XHTML paths (`convertXHTMLtoAltChunk`, `convertXHTML`);
  - the date path (`xpathDate`; CR-058 D3, and `BindingTraverserCommonImpl` for the other binders).

  It sees the run properties, not the `w:sdtPr`, so it can't see what placeholder the control names.
- **The template's own placeholder isn't used.** A control made in Word names an entry whose text the template's
  author may have set ("Enter the customer's name"). Word shows that text when the control is empty; docx4j shows
  "Click here to enter text." and drops the reference, so the entry is left orphaned.
- **The glossary.** docx4j loads and saves a `GlossaryDocumentPart` and reads it (text extraction, the anonymiser),
  but never creates one.
- `RemovalHandler`'s `ALL_BUT_PLACEHOLDERS` keys on `w:showingPlcHdr`, which this CR leaves as it is.

## 3. Proposed approach

1. **Keep `w:placeholder`,** in every binder, on bound and unbound controls alike (D1). The template's reference
   survives binding, so a control later emptied in Word shows the template's placeholder.
2. **An empty value shows the placeholder the control names** (D2): a copy of the content of the glossary entry its
   `w:placeholder` names (its `w:docPartBody`, paragraphs and all), with the control's run properties as today, and
   `w:showingPlcHdr`. The lookup belongs in
   the step that adds `w:showingPlcHdr` (`bind.xslt`'s `w:sdtPr` template; `postBindSdtPrHygiene`), which sees the
   `w:sdtPr` and the content both. `createPlaceholder` keeps its signature, for the pluggable inserters.
3. **A text control (untyped, rich text, plain text) that names no placeholder, or names a missing entry** (D3), gets
   Word's default:
   - `w:placeholder` naming `DefaultPlaceholder_-1854013440` (a missing entry's name is replaced, not given invented
     text);
   - the entry in the glossary, unless it is there already (binding may run more than once);
   - the glossary part itself, where there is none.

   Other kinds naming no entry (date, drop-down list, combo box, picture) keep today's placeholder: Word gives them
   other wording ("Click or tap to enter a date.", "Choose an item.") from entries whose names nobody has measured.

   The entry is needed: Word's save drops a reference without one (check 65, §9). The glossary is the document part
   and a styles part holding Placeholder Text (D7, decided; §10).
4. **The default's wording becomes Word 365's,** "Click or tap here to enter text." (D5), so the content matches the
   entry it names.
5. **A configured placeholder run** (`docx4j.model.datastorage.placeholder`) is still honoured for a control that names
   none, written as an entry of its own, named and stored as check 65 finds Word does (D4): one entry per distinct
   configured run per document, reused across controls and across binding runs.

## 4. What it affects

- **Every document bound by docx4j** (OpenDoPE and plain `w:dataBinding`). Controls keep `w:placeholder`; empty values
  show the template's placeholder text where it has one; documents bound from code-built templates gain a glossary
  part.
- **Word users.** Writes into an empty bound control reach the node and survive reopening, for Office JS (measured)
  and for typing by hand if phase 0 shows that it loses them too.
- **Not affected:** the bound values themselves; `RemovalHandler`; the ImportXHTML and FO paths, which read the
  content.
- **The ports.** core-ts decided the same for its own writer (its CR-002 §63.6, D8). docx4j-python is told.

## 5. Phases

| Phase | What | Size |
|---|---|---|
| 0 | Check 65's result from core-ts (Jason runs it; written 2026-10-24, core-ts `test/fixtures/check65/`, the share's `__tmp/65/`). 65a: controls naming `DefaultPlaceholder_-1854013440` with no glossary part. 65b: a glossary of `document.xml` alone, holding the default entry and one of its own text named with 32 hex digits, and a control naming an entry it lacks. Each repeats check 64's telling writes (the style, the flag, a tracked Replace, a bound node); case 6 sets `placeholderText` through Office JS, so Word's save shows how it names and stores an entry for other text; a control made by `insertContentControl` shows the glossary Word adds where there is none; the last case is a bare-form bound control Jason types into by hand, saves and reopens. No docx4j round of its own. Run 2026-10-25 (§9); check 66, the glossary's styles part, run the same day (§10). Done | with core-ts |
| 1 | The binders keep `w:placeholder` (D1): `bind.xslt`'s template and `postBindSdtPrHygiene`'s removal go; a test that the three binders agree | 1 h |
| 2 | An empty value in Word's form (D2 to D5): the entry the control names, or Word's default with the glossary part and entry added as check 65 requires; `placeholder.xml`'s wording; tests; the Office-open check (Word opens it without repair) | 3 h |
| 3 | Jason's Word check: phase 0's writes on the new output reach the node and survive the reopen. Office-open check §14, writes §17 (check 65c, 2026-10-25). Done | 30 min with Jason |
| 4 | D8 (a), decided 2026-10-25 (§13): a date, drop-down list, combo box, building block gallery or repeating section item naming no entry gets Word's default entry for its kind. Done 2026-10-25 | 1 h |

## 6. Decisions (accepted as recommended, Jason, 2026-10-25)

| # | Question | Options | Recommended |
|---|---|---|---|
| D1 | `w:placeholder` through binding | (a) kept on every control; (b) kept on unbound controls only; (c) dropped, as today | (a): it's Word's form, and the reason for dropping it no longer holds (§2) |
| D2 | What an empty value shows | (a) the entry the control names, its content copied, as Word shows it; (b) `placeholder.xml`'s run always, with the reference kept | (a): (b) shows one text and names another, which Word would replace with the entry's when the control is emptied. core-ts agrees, and proposes following docx4j in copying the entry's content (it writes the entry's text as one run) |
| D3 | A text control naming no placeholder, or a missing entry | (a) Word's default entry added, with the glossary part and entry where missing; (b) the reference alone | (a): check 65 rules (b) out, since Word's save drops a reference no glossary entry holds (§9). Revised after core-ts's review (§11): text controls only, as core-ts does, until a Word check records the entries Word makes for dates and lists; and a missing entry's name replaced by the default's, which core-ts proposes to follow |
| D4 | A configured `docx4j.model.datastorage.placeholder` | (a) honoured for a control naming none, as an entry of its own, as Word makes one (§9: 32 upper-case hex digits, its own `w:guid`, the default's `w:docPartPr`); (b) deprecated | (a): deployments set it to show their own wording. One entry per distinct run per document, reused (§11) |
| D5 | The default wording | (a) Word 365's "Click or tap here to enter text."; (b) "Click here to enter text.", as today | (a): the shown text matches Word's default entry |
| D6 | A switch back to the old behaviour | (a) none; (b) a property | (a): Word's form keeps the template's placeholder text, and is what Word reads and writes; a deployment that wants no placeholder text can configure an empty run (D4). (Revised §11: the bare form doesn't lose writes outside Design Mode) |
| D7 | The glossary docx4j adds (added after check 65) | (a) the glossary document part, a styles part related from it holding Default Paragraph Font and Placeholder Text as Word defines them (§10), and the relationship; (b) the document part alone; (c) Word's six parts | **Decided (a), 2026-10-25**: Jason took core-ts's D9 (a) after check 66 and asked, through core-ts, that docx4j's D7 get the same answer. Word completes the rest on save (§10) |

## 7. Risks

- **A template's placeholder text shows where it didn't** (D2). A Word-made template whose entries hold text meant for
  the template's author, not the reader, will show it for an empty value. That is what Word shows when the control is
  empty, and the change is named in the CHANGELOG.
- **A glossary part is added to code-built templates' output** (D3). Word's glossary is five parts; check 65 says
  whether one will do.
- **A pluggable inserter** that returns its own placeholder run keeps working: the entry lookup replaces the content
  only where it carries the Placeholder Text style, as `w:showingPlcHdr` is decided today.
- **Binding run more than once** must not add the default entry, or a configured run's, twice (§11).
- **The ports:** core-ts's D8 and this CR must agree on the entry for other text (D4); core-ts reviewed D1 to D6 (§11).

## 8. Sources

- docx4j-core-ts CR-002 §63 (`2070694`, unpushed): §63.2 check 63's run, §63.5 check 64's run, §63.6 the decisions;
  Word's saves in `test/check63/`, `test/check64/` with SHA-1 manifests.
- `bind.xslt` lines 1155 and 1220; `BindingTraverserCommonImpl.postBindSdtPrHygiene`;
  `BindingTraverserXSLT.createPlaceholder`; `placeholder.xml`; `docx4j.properties`
  (`docx4j.model.datastorage.placeholder`).
- CR-058 D3 (an empty date value shows the placeholder).
- docx4j-core-ts implemented its counterpart (its CR-002 §63.12, `020c039`, 2026-10-25, unpushed): an empty bound
  value in Word's form for a text control or one naming an entry; entries read without touching the glossary and the
  missing ones written on save, a referenced entry the glossary lacks included; D7's glossary where there is none,
  and a glossary styles part lacking the two styles completed; new entries named with 32 upper-case hex digits. Its
  tests are in `test/placeholders.test.mjs`. A reference for phases 1 and 2 here.
- ECMA-376 Part 1, §17.5.2 (structured document tags: `placeholder`, `showingPlcHdr`) and §17.12 (the glossary document).

## 9. Phase 0: check 65 (Word 365, Jason's run, 2026-10-25; core-ts CR-002 §63.8, §63.9, `ae9215f`)

Two files, read by core-ts. 65a: controls naming `DefaultPlaceholder_-1854013440` and no glossary part. 65b: a
glossary of `document.xml` alone, holding the default entry, one of its own text, and a control naming an entry it
lacks. Word's saves are in core-ts `test/check65/` with a SHA-1 manifest.

1. **The reference alone gives Word's form while the file is open** (65a): every Office JS write behaved as in check 64.
   The flag cleared, the new text had no Placeholder Text style, a tracked write was an insertion only, and the bound
   nodes were written.
2. **But Word's save drops a `w:placeholder` whose entry no glossary holds.** Reopened, 65a is back in the bare form;
   the control naming an entry missing from 65b's glossary lost its reference the same way. So docx4j's binding needs
   the entry, not only the reference (D3).
3. **A glossary of the document part alone is kept,** and Word adds its settings, styles, webSettings and fontTable
   parts. But the entries' runs lose `w:rStyle PlaceholderText`, since the glossary's own styles lack that style, and
   a control Word then makes from the default entry is unstyled. So the glossary needs a styles part holding
   Placeholder Text (D7). core-ts proposes the same (its D9 (a)), after a check 66.
4. **An entry for placeholder text of Word's own** (Office JS's `placeholderText` set): a new entry, named with 32
   upper-case hex digits, with its own `w:guid` and the default's `w:docPartPr` (category General, gallery
   placeholder, type `bbPlcHdr`, behavior content), its body one paragraph with one run in Placeholder Text. The
   control names it, and the old entry stays. In 65a Word made the whole glossary, six parts, to hold it. docx4j's
   entry for a configured placeholder takes that form (D4).
5. **Typed by hand into a bare-form bound control** (no `w:placeholder`, docx4j's output today), the value replaces
   the placeholder, clears the flag and the style, and reaches the node: 65a's save holds `TYPED10`, and in a reopened
   65b save `TYPED14` reached the control and the node. So the loss is Office JS's (check 63's `insertText` and the
   rest), not a person typing. That makes CR-063 less urgent than §0 suggested: it affects add-ins and scripts writing
   into docx4j's output, and the bare form's other oddities (the style kept, the flag left on, the tracked deletion).
   (Corrected 2026-10-25, §11: check 63's loss and oddities were Word's Design Mode; outside it the bare form loses
   nothing.)
6. **Not yet a rule** (one session): typing into the reopened 65b save also gave that control, and four others Word
   hadn't touched, new entries of their own, each holding the text it showed, with a paragraph style for each entry in
   the glossary's styles. Check 66 reopens, types and saves again, to see whether Word keeps controls on the default
   entry when the glossary has its styles.

Phase 0 is complete once check 66 is in: it settles D7, and whether item 6 is Word's rule.

## 10. Phase 0: check 66 (Word 365, Jason's run, 2026-10-25; core-ts CR-002 §63.10, §63.11, `2d080d4`)

66a had D7 (a)'s three parts: the glossary document part, a styles part related from it, and the relationship. 66b had
check 34's glossary, Word's six parts. The two behaved the same at every step (Word's saves in core-ts
`test/check66/`, with a SHA-1 manifest):
- **On opening,** Word kept 66a's entry in Placeholder Text. Word's saved glossary document, styles, fontTable and
  relationships parts are identical for the two files. Only settings and webSettings differ, and Word wrote 66a's in
  compatibility mode 12.
- **Office JS deleting a control's content** brought the entry back, grey, with the flag on. A control Word made named
  the default entry and showed it grey.
- **Every `w:placeholder` reference stayed the same** through the run, through a reopen and save with nothing typed,
  and through typing.
- **Typed by hand,** a bound control in Word's form kept its reference; a bare-form one got no entry; both nodes were
  written.

So check 65's 65b reshuffle (§9 item 6) came of its unstyled glossary, not of reopening or typing. Office JS's
`getOoxml` names a fresh 32-hex entry for a control on every call, where Word's save keeps the default entry, so its
OOXML is no evidence of a control's reference.

**D7 (a), decided.** Where a document has no glossary, docx4j writes:
- the glossary document part, holding the entries: `DefaultPlaceholder_-1854013440` (§1), and any of a control's own
  (D4);
- `word/glossary/styles.xml`, related from it, holding these two styles as Word defines them:

  ```xml
  <w:style w:type="character" w:default="1" w:styleId="DefaultParagraphFont"><w:name w:val="Default Paragraph Font"/>
    <w:uiPriority w:val="1"/><w:semiHidden/><w:unhideWhenUsed/></w:style>
  <w:style w:type="character" w:styleId="PlaceholderText"><w:name w:val="Placeholder Text"/>
    <w:basedOn w:val="DefaultParagraphFont"/><w:uiPriority w:val="99"/><w:unhideWhenUsed/>
    <w:rPr><w:color w:val="666666"/></w:rPr></w:style>
  ```
- the relationship between the two.

Word adds settings, webSettings, fontTable and the other styles on save. Phase 0 is complete; phase 1 waits on D1 to D6.

## 11. core-ts's review of D1 to D6 (2026-10-25; its CR-002 §63.13 to §63.17, `d84f3da`)

At Jason's request, docx4j-core-ts reviewed D1 to D6 against its section 63 decisions (D1 to D9, implemented in
`020c039`). It agrees with each, with one correction to the evidence and three differences.

**The evidence.** Check 63's first run was in Developer > Design Mode: both its saves carry `<w:formsDesign/>`, and
Jason confirmed it. Run again with Design Mode off, the bare form behaves as Word's form: Office JS's writes are plain
and clear the flag, and every bound node holds its write and shows it on reopening (check 67, 72 writes; check 58
agrees). So this CR's first account (§0, §1, D6's reason: "the old form is the one that loses writes") described Design
Mode. §0 and D6 are corrected above; §1 keeps check 63's findings, marked. The case for Word's form is now the
template's placeholder text, Office JS's `placeholderText`, and what Word shows for a control emptied in it (§0).

**Each decision.**
- D1 (a): agree. core-ts never removes `w:placeholder`, bound or unbound.
- D2 (a): agree. core-ts writes the entry's text as one run (an entry's paragraphs joined); copying the entry's
  `w:docPartBody`, as here, is closer to Word, and core-ts proposes following docx4j.
- D3 (a): agree, with two refinements adopted here. (1) Scope: core-ts names the default entry only for text controls
  (untyped, rich text, plain text) and controls already naming an entry. The default is Word's text entry; Word's date
  and list controls show other wording from entries of their own, unmeasured, so naming the text entry on them would
  show the wrong prompt. (2) A control naming an entry the glossary lacks: core-ts writes an entry under that name with
  the default text; CR-063 points the control at the default instead, so nothing invents text under someone else's
  name. core-ts recommends docx4j's way and proposes following it.
- D4 (a): no problem; it matches core-ts's `placeholderText` setter. Caution adopted: one entry per distinct configured
  run per document, reused across controls and binding runs, or the glossary grows by an entry per control per run.
- D5 (a): agree. If docx4j changes `placeholder.xml`'s wording, core-ts's non-text kinds follow.
- D6 (a): agree there should be no switch back; the reason is replaced.
- D7 (a): as core-ts's D9 (a), decided.

**Timing.** docx4j writing the entries during binding, where core-ts writes them on save, gives the same output;
binding run twice, or over a glossary already holding the default, must not add it again.

**Still to measure** (a short Word check, before dates and lists get Word's form): the entries Word makes for a date
picker, a drop-down list and a combo box, their names and text.

## 12. Phases 1 and 2: the implementation (2026-10-25, Claude Fable 5.1, session docx4j-25)

Built together, since D2 and D3 share the step D1 clears. `PlaceholderEntries` (new,
`org.docx4j.model.datastorage`) holds the rules for all three binders; bind.xslt and
`BindingTraverserCommonImpl` call it where `w:showingPlcHdr` is decided, which sees the `w:sdtPr` and the content.

**D1.** bind.xslt's `<xsl:template match="w:placeholder"/>` and `postBindSdtPrHygiene`'s
`removeFromSdtPr(CTPlaceholder)` are gone; `w:placeholder` survives binding on every control, bound with a value,
bound and empty, or unbound.

**D2.** `PlaceholderEntries.referenceFor(pkg, sdtPr)` gives the entry the control shows; `contentOf(pkg, entry)`
a copy of its `w:docPartBody` paragraphs. What a control shows follows core-ts's §63.18: a block-level control shows
every paragraph, each run as the glossary holds it (at first with the control's `w:rPr` applied over it, as
`createPlaceholder` applies it; D9, §15, took that out the same day: Word applies nothing); the control's own
paragraph keeps its `w:pPr`, and the entry's further paragraphs get a copy of it. The entry's `w:pPr` (naming the glossary's own styles), proofing marks and bookmarks stay in the glossary. A
run-level control shows the first paragraph's runs. In bind.xslt the content goes through
`BindingTraverserXSLT.placeholderContent`, which replaces the control's own placeholder run (Placeholder Text style,
not inside a nested `w:sdt`); the `w:sdtPr` template gets an `ownContent` parameter, true at the bound text and
date branches, so a condition's template content (whose placeholder runs belong to nested controls) is left alone, as
before. In the other binders `postBindSdtPrHygiene` replaces the contents; `applyBoundContent` places the further
paragraphs after the control's.

**D3.** `referenceFor`: the entry the control names, where the glossary holds it; else, for a text control (no
`w:date`, list, picture, docPart, equation, citation, group, bibliography, `w14:checkbox`, `w15:repeatingSection`),
`DefaultPlaceholder_-1854013440`, written into the glossary where missing (the CR §1 entry, GUID
`{7D21F9BC-12A3-463E-B08F-7DD4FFC77A03}`), and named in the control, replacing a missing entry's name; null for
any other kind naming none, which keeps today's run and whatever reference it had. Idempotent: the entry is looked
up by name before it is added.

**D4.** `BindingTraverserXSLT.configuredPlaceholderRun()` is the configured run where it differs from
`placeholder.xml`'s (compared marshalled), null otherwise. For it, `ensureEntryFor` reuses the glossary entry
whose body is one paragraph holding that run, else adds one named with 32 upper-case hex digits (a new GUID's), its
own `w:guid`, the default's `w:docPartPr`. One entry per distinct run per document, across controls and binding
runs. Where a run is configured, it is the default for a missing entry too (a deployment that configures its own
wording wants it everywhere; the CR said "replaced by the default").

**D5.** `placeholder.xml` says "Click or tap here to enter text."; the parity fixture's assertions moved with it.

**D7.** `ensureGlossary`: a `GlossaryDocumentPart` with an empty `w:docParts`, added to the main document part,
where there is none; `/word/glossary/styles.xml` (a `StyleDefinitionsPart`) related from it, holding Default
Paragraph Font and Placeholder Text as §10 gives them, added where the glossary has no styles part; the two styles
added to a styles part that lacks them. A glossary whose entry the control names is not touched.

**Also fixed on the way.** `createPlaceholder(rPr, sdtParent)` for a block-level parent replaced the run's
`w:rPr` with the control's, losing the Placeholder Text style (so no `w:showingPlcHdr`, and now no entry); it
applies the control's over the run's, as the run-level case does. The `w:sdtPr` template no longer copies a
template's own `w:showingPlcHdr` beside the one it adds.

**Tests** (`docx4j-core-tests`, all three binders): `PlaceholderEntriesTest` (code-built templates: the default
entry and glossary made, round-trip and a second binding run adding nothing; a two-paragraph entry shown
paragraphs and all, block and run-level, D1 on bound and unbound controls; a missing entry replaced; a date control
left alone; the binders agree), `PlaceholderConfiguredRunTest` (D4; its own class, since the run is read once per
JVM), and `TextBindParityTest` (the fixture's `XEmpty` names the default, the rank control keeps its reference, the
glossary's two entries stay two). Full suites green: core-tests, export-fo-tests, diffx.

**Not done here.**
- Phase 3: Jason's Word check that Word opens the output without repair and that check 65's writes reach the node;
  sample outputs are at `/home/jharrop/vbShares/Office 2016/__tmp/cr063/` (see the hand-off).
- The OpenDoPE specification WD (`docs/OpenDoPE Specification v3 WD 2026 09 18.docx`) still says a processor MUST
  remove `w:placeholder` (REQ-025) and insert placeholder text (REQ-024); the WD is Jason's to revise (the docx is
  canonical; its md is derived). Getting Started's binding example quotes the old wording.
- Dates, drop-down lists and combo boxes naming no entry: Word's entries for them unmeasured (§11).
- Ports: core-ts follows docx4j on copying `w:docPartBody` and on replacing a missing entry's name (§11);
  docx4j-python told.

## 13. Word's default entry for each kind of control (Word 365, Jason's run, 2026-10-25; D8 proposed)

§11 left the entries Word gives dates and lists unmeasured. Jason made one control of each kind from the Developer tab
in Word 365 and saved; the file is on the share at `Office 2016/placeholders.docx` (SHA-1
`9cd6fb70e5af3b6d52eab2e95bd13acfbcf3c2c8`; its settings carry `w:formsDesign`, so Design Mode was on, which bears on
edits into a placeholder, not on what Word makes). Read by core-ts and by this session, which agree:

| Kind | `w:placeholder` names | Entry text |
|---|---|---|
| rich text (no kind element), plain text (`w:text`) | `DefaultPlaceholder_-1854013440` | Click or tap here to enter text. |
| date picker (`w:date`) | `DefaultPlaceholder_-1854013437` | Click or tap to enter a date. |
| drop-down list, combo box | `DefaultPlaceholder_-1854013438` | Choose an item. |
| building block gallery (`w:docPartList`) | `DefaultPlaceholder_-1854013436` | Choose a building block. |
| repeating section item (`w15:repeatingSectionItem`) | `DefaultPlaceholder_-1854013435` | Enter any content that you want to repeat, including other content controls. You can also insert this control around table rows in order to repeat parts of a table. |
| repeating section (`w15:repeatingSection`, the outer control) | none, no flag | |
| picture (`w:picture`) | none; `w:showingPlcHdr` and a stock `w:drawing` (`word/media/image1.png`) | |
| check box (`w14:checkbox`) | none, no flag; the ☐ glyph in MS Gothic | |

Every entry has the same `w:docPartPr` as the text default (§1): category General, gallery placeholder, type
`bbPlcHdr`, behavior content, a `w:guid` of its own; each body is one `w:p` with one run in Placeholder Text. Each
control names its entry, has `w:showingPlcHdr`, and its content is that run. Word also gives a drop-down list and a
combo box a first `w:listItem` with `w:value="Choose an item."` and no `w:displayText`. `-1854013439` is absent
(group, citation or equation, not made).

**D8: the other kinds get Word's entry for them. Decided (a), Jason, 2026-10-25 ("D8 we go (a)").**

| # | Question | Options | Recommended |
|---|---|---|---|
| D8 | A date, list, building-block or repeating-section-item control naming no entry (or a missing one), empty | (a) Word's entry for its kind, from the table, added to the glossary as D3 adds the text default; picture and check box as today (Word names none for them); group, citation, equation, bibliography (unmeasured) as today; (b) as now: today's run, no reference | (a): the same rule as D3, with Word's own wording; a date no longer says "enter text". The configured run (D4) stays the text kinds' default only, since its wording is written for text. The list controls' first `w:listItem` is Word's authoring, not emulated: the template's list is kept |

**Phase 4, done 2026-10-25:** `PlaceholderEntries.defaultEntryFor(sdtPr)` maps the kind to a `DefaultEntry` (name,
text, Word's GUID from `placeholders.docx`): `TEXT`, `DATE`, `LIST`, `BUILDING_BLOCK`, `REPEATING_SECTION_ITEM`; null
for picture, check box, the outer repeating section, and the unmeasured equation, citation, group, bibliography,
`w:docPartObj` and entity picker, which keep today's run. `referenceFor` writes the kind's entry as D3 writes the text
default; the configured run (D4) applies to `TEXT` only. The date path reaches it through the existing lookup, the
lists through the text branch. The list controls' first `w:listItem` is not emulated. Tests: `PlaceholderEntriesTest`
(date, drop-down and combo box get their entries with Word's GUIDs and the template's items kept; a picture keeps
today's run; the binders agree). core-ts and python told.

## 14. Phase 3: the Office-open check (Jason, 2026-10-25)

Of the samples at the share's `__tmp/cr063/`, the code-built ones (1-, 2-) opened; the bound parity fixture
(`3-text-bind-parity-bound-*.docx`, all three binders) needed repair. Word's repaired saves kept every control and
reference, converted the two altChunks (as it does), and gave `XEmpty` a fresh 32-hex entry of its own (as check 65's
65b did) beside the default it named. No structure the binders write there is one Word is known to repair (cell
last-block rule, altChunk placement, nesting all checked). Bisect files for Jason in `__tmp/cr063/bisect/`: A = the
output with every `w:placeholder` removed; B = the output with the altChunks removed; C = the same fixture bound by
docx4j at ea5461ee9, before CR-063.

**Round 1 (Jason, 2026-10-25): A and C needed repair, B opened clean.** So the repair is the fixture's altChunks and
predates CR-063; the placeholders in Word's form are not it. Word's repaired saves of A and C converted the XHTML
chunk (`XHTMLCONTENT` kept) and emptied the Flat OPC control (`FLATOPCTEXT` gone): the fixture's Flat OPC value is a
`pkg:package` holding `/word/document.xml` alone, with no `/_rels/.rels` part, which Word's Flat OPC always carries.
Round 2 files: B1 = the HTML chunk removed, the Flat OPC kept; B2 = the Flat OPC chunk removed, the HTML kept; D =
both kept, the Flat OPC chunk given a `/_rels/.rels` part. Expected: B1 repairs, B2 and D open clean, D showing
`FLATOPCTEXT`.

**Round 2 (Jason, 2026-10-25): B1 needed repair; B2 and D opened clean.** So the repair was the fixture's Flat OPC
altChunk, a `pkg:package` without its `/_rels/.rels` part, and it predates CR-063. Fixed in the fixture's data
(`text-bind-parity.docx`, `customXml/item4.xml`'s `flatOpc` value gains the relationships part, as D had it); the parity
test's assertions are unchanged. Whether `convertFlatOPC` should add the relationships part to a package that lacks one
is left as a separate question (Word's own Flat OPC always carries it; without it Word repairs and drops the content).
**Phase 3's Office-open check passes:** the code-built samples opened clean from the first; the bound fixture opens
clean without the broken chunk (B2) and with it mended (D). Check 65's writes on the new output remain for Jason.

## 15. Check 68: Word shows an entry as the glossary holds it (Word 365, Jason's run, 2026-10-25; D9 proposed)

core-ts's check 68 (its CR-002 §63.20; Word 365 16.0.20326.20232, Design Mode off; saves in core-ts `test/check68/`,
read by this session too): a control in Word's form with `w:sdtPr/w:rPr` bold, holding a bold value, has its content
deleted. Word brings back the entry it names exactly as the glossary holds it: the default entry in Placeholder Text
alone, a custom entry in Placeholder Text plus its own italic, and no bold (cases 21 to 24). The control's bold stays
in its `w:sdtPr`. A write into a control showing its placeholder (Office JS `insertText`, Replace and End, both forms)
takes the control's `w:rPr` and nothing of the placeholder run's (cases 1 to 20), which is what docx4j's inserters
already do for a value.

So D2's "with the control's run properties as today" (carried over from `createPlaceholder`, unmeasured) is not
Word's rule for the shown entry; §12's implementation and core-ts's §63.18 both apply the control's `w:rPr` over the
entry's runs.

| # | Question | Options | Recommended |
|---|---|---|---|
| D9 | The run properties of a shown entry | (a) the entry's runs as the glossary holds them, the control's `w:sdtPr/w:rPr` not applied (Word, check 68); (b) the control's `w:rPr` applied over them, as §12 did | (a): the CR's aim is what Word shows; the control's `w:rPr` still governs a value written into it. **Decided (a), Jason, 2026-10-25 ("D9 choose (a)"); done the same day:** `PlaceholderEntries.contentOf(pkg, entry)` no longer takes or applies the rPr; `PlaceholderEntriesTest`'s colour and italic assertions inverted (the rPr stays in the `w:sdtPr`). The non-text kinds' own run (placeholder.xml's under the control's rPr) is unmeasured and stays |

core-ts follows the same rule since its c275a2a.

## 16. An added entry's `w:guid` is new to the document (core-ts, 2026-10-25)

core-ts read every Word-made glossary under its `test/`: `DefaultPlaceholder_-1854013440` has seven different
`w:guid`s across seven Word files that don't descend from one another (`placeholders.docx` `{F8BBB6E8-...}`, check 65a's
run where Word added the entry `{343D98B3-...}`, check 54, cr043-tracked, check 34c, check 34a `{7D21F9BC-...}`, check
35a). Where a file holds a GUID already, Word's runs and saves keep it; where Word adds the entry, the GUID is new to
that file. So Word gives each document's default entry a GUID of its own. §12 and phase 4 had docx4j write fixed GUIDs
(check 34a's for the text default, `placeholders.docx`'s for the other kinds), the same into every document, which Word
doesn't do; nothing measured shows it matters (no check has copied content between two documents holding the same
GUID). Changed to follow Word: an entry docx4j adds gets a new GUID (`PlaceholderEntries.newGuid`), for the per-kind
defaults as for a configured run's entry; an entry the glossary holds keeps its GUID, untouched. core-ts has had this
rule from the start, so the ports now agree on the entries entirely.

## 17. Phase 3, second half: check 65's writes on docx4j's output (check 65c; Word 365 16.0.20326.20232, Jason's run, 2026-10-25)

The share's `__tmp/cr063/` samples are not in check 65's shape (its snippet reads regions `BEGIN<n> <op> <shape>
<mode>` ... `END<n>`, controls tagged `c65-<n>`, bound nodes `value<n>` in `urn:check65`), so a file was built in that
shape and bound by docx4j at adf0201e1: `__tmp/cr063/65c/` holds the template (bare-form bound text controls with no
`w:placeholder` and stale content, a date control, and a glossary of one entry of its own, two paragraphs with a bold
run, which cases 9 and 10 name), the three binders' outputs, check 65's snippet unchanged, and the README. The three
outputs differ only in the order of `w:sdtPr`'s children, so the snippet ran on the XSLT output
(`65c-docx4j-xslt.docx`, sha1 d7f847f3) and the other two were opened only. The fourteen cases: 1 `insertText` End,
2 `insertParagraph` End, 3 tracked `insertText` Replace, 4 untracked Replace, 5 run-level Content-range Replace, 6
`placeholderText` set, 7 and 8 left alone (block and run), 9 a write over the two-paragraph entry, 10 that entry left
alone, 11 the date control left alone, 12 typed by hand, 13 a control holding a value, 14 a control Word makes with
`insertContentControl`. Word's saves: `65c-xslt-opened-word365.docx` (300c4e69), `-run-` (4fdac201, after the run and
the typing), `-reopened-` (511333e8); the snippet's JSONs `result-c-report.txt` (37876cd3), `result-c-run.txt`
(78de6de9). Design Mode off.

**Every write reached the node and survived the reopen.** No repair on any of the three outputs or on Word's saves.
- Cases 1 to 5 and 9: the write is a plain run (no Placeholder Text style), the flag is cleared, the node holds the
  text, in the run's report, in Word's save and after reopening. Case 3's tracked write is an insertion only; nothing
  is marked deleted. Case 2 (`insertParagraph` End on a `w:text` control) shows one paragraph `NEW2`, but Word wrote
  the node as `\r\nNEW2`, a line break before the text (Word's, not docx4j's; noted).
- Case 12, typed by hand: `TYPED12` replaced the placeholder (Jason), the save holds it plain with the flag cleared, the
  node holds `TYPED12`.
- Case 13 kept `VALUE13`; cases 7, 8, 10 and 11, left alone, kept their references and placeholders.
- **Every `w:placeholder` reference docx4j wrote was kept**, through the run, the save and the reopen, and **every
  entry docx4j added kept its GUID** (the two defaults and the template's own). Word completed the glossary to its
  parts (document, styles, settings, webSettings, fontTable).
- Case 6: Word gave the control an entry of its own (`9700B94B...`, 32 hex, its own GUID, Placeholder Text run)
  beside docx4j's default, and showed it grey. Case 14: the control Word made names `DefaultPlaceholder_-1854013440`,
  docx4j's entry, reused; Word added no second default.
- Office JS `getOoxml` named a fresh 32-hex entry for every control on every call, as §10 found; Word's saves show
  the real references.

So phase 3 is done: docx4j's output is Word's form to Word, for Office JS and for typing.

**The finding: the Placeholder Text style in the main styles part.** The template was code-built
(`WordprocessingMLPackage.createPackage()`), whose styles part has no `PlaceholderText` style. docx4j wrote the
placeholder runs in that style (as `placeholder.xml` always has) and the glossary's styles part holding it (D7), but
the main styles part was left as it was. Word's opened save shows the result: **every placeholder run docx4j wrote lost
its `w:rStyle PlaceholderText`** on opening (the style wasn't defined in the document), and the placeholders showed
**black** (Jason; the screenshots show cases 10 and 11 black). The glossary entries kept the style, since the
glossary's styles define it. Word's own placeholders in the same file (cases 6 and 14) showed **grey**: when Word wrote
them it added `PlaceholderText` to the main styles part (`basedOn DefaultParagraphFont`, `uiPriority 99`,
`unhideWhenUsed`, colour `666666`, as in §10), and the run save holds it. Nothing of the writes depends on this; it is
the look of an empty control, and only in a document whose styles lack the style (a Word-made template has it from
the first control). It predates CR-063: the bare form's run had the same style reference, and the same template
would have shown it black. docx4j already has the definition (`PlaceholderEntries.placeholderText()`, and
`KnownStyles.xml`).

| # | Question | Options | Recommended |
|---|---|---|---|
| D10 | The main styles part when docx4j writes a placeholder run and it lacks `PlaceholderText` | (a) add Placeholder Text (and Default Paragraph Font, if that is missing too) to the main styles part, as Word does when it writes a placeholder, and as D7 does for the glossary's; (b) leave the main styles part alone | (a): without it Word drops the style from every run docx4j wrote and shows the placeholder black, which is not Word's form; one call in `PlaceholderEntries` shared by the three binders, applied for every kind (the non-text kinds' own run carries the same style); a style the part already holds is left as it is. core-ts has had this rule since its CR-002 §40 (2026-10-04, from check 35: Word 2010 dropped the style from placeholder runs the same way; 65c is the first Word 365 measurement of it): every placeholder run it writes adds Placeholder Text, and Default Paragraph Font where missing, to the main styles part, with the definition Word added to 65c's run save; a part that defines them keeps its bytes; a file it only opens and saves is left alone. **Decided (a), Jason, 2026-10-25** ("D10 ask core-ts what it thinks and if it agrees with (a), then my decision is (a)"; core-ts agrees); **done the same day** |

**D10 done (2026-10-25).** `PlaceholderEntries.ensurePlaceholderStyle(pkg)`: the main styles part gains Default
Paragraph Font where missing and Placeholder Text as Word defines it (the definition of §10 and of 65c's run save,
colour 666666; not `KnownStyles.xml`'s older 808080), a style the part defines left as it is, a document with no
styles part left alone. Called from `referenceFor` (every kind, both the entry and the own-run cases, so the NonXSLT
and StAX binders through `postBindSdtPrHygiene`) and from `bind.xslt`'s `w:sdtPr` template wherever a control's
content carries a Placeholder Text run (`BindingTraverserXSLT.placeholderStyle`), own content or a condition's. A
document given no placeholder gains nothing. The XSLT binder writes no run at all for an empty picture value
(`xpathInjectImage` returns null), where the other two write today's run, so there the style follows the run;
`PlaceholderEntriesTest.thePlaceholderStyleIsAddedToTheMainStylesPart` pins all of it for the three binders.

**Unmeasured: an empty picture value.** Three behaviours, none measured in Word: docx4j's XSLT binder leaves the
`w:sdtContent` empty (`xpathInjectImage` returns null); its NonXSLT and StAX binders write today's placeholder run
(`placeholder.xml` under the control's rPr); core-ts keeps the control's existing picture and reports a note (its
CR-005 §9). Unchanged by CR-063; a Word check would settle which Word shows for a bound picture whose node is empty
(core-ts changes nothing unless Jason asks).

## 18. Check 71: a bound picture control whose node is empty (Word 365 16.0.20326.20232, Jason's run, 2026-10-25)

Jason asked for the check (§17's unmeasured item). The share's `__tmp/cr063/71/` holds the file docx4j built
(`71-bound-picture-empty-node.docx`, `Cr063Check71.java` in the session's scratchpad; one red 64x64 PNG as the existing
picture, a blue one for the node), the snippet in check 65's shape (`check71-script-lab.js`: regions, tags `c71-<n>`,
node `pic<n>` in `urn:check71`; it identifies every image part and inline picture of a region as red, blue or other by
its bytes, and writes a node through `CustomXmlPart.setXml`), the bisect files, and Word's saves. The cases, each a
`w:picture` control with a `w:dataBinding`: 1 an existing picture over an empty node (core-ts's output); 2 empty
content over an empty node (docx4j's XSLT binder); 3 docx4j's text placeholder run under the control over an empty
node (the NonXSLT and StAX binders); 4 an existing picture over a node holding the blue PNG; 5 an empty paragraph where
Jason binds a fresh picture control to the empty node through the XML Mapping Pane; 6 as 1, the snippet then writing
the blue PNG into the node; 7 as 4, the snippet then emptying the node; 8 an empty paragraph where the snippet calls
`insertContentControl('Picture')`. Design Mode off.

**Round 1: the file needed repair; the cause is case 3.** Bisect: A (2 and 3 removed) and C (3 removed) opened clean;
B (2 removed, 3 kept) and D (the four pictures removed, 2 and 3 kept) needed repair. So **a text run inside a
`w:picture` control is content Word repairs**, and that is exactly what the NonXSLT and StAX binders write for a
picture control whose node is empty (`placeholder.xml`'s run; §15's "stays" and §17's unmeasured item): a defect, not
a rule to measure. Word's repaired save (`71-repaired-word365.docx`, 368149f1) kept case 3's text as a plain run,
style and flag stripped. The XSLT binder's empty content (case 2) is accepted.

**Round 2** (`71-v2-no-case-3.docx` 49b29f34; Word's saves `-opened-` 336c8389, `-run-` 33a724af, `-reopened-`
195b1a96; the snippet's `result-71-report.txt` b7b2ab17, `result-71-run.txt` aeded601). No repair anywhere.
- **Word's form for a bound picture control whose node is empty:** `w:showingPlcHdr` in the `w:sdtPr`, and the
  content one run holding an inline drawing of Word's stock placeholder picture: `wp:extent` 1270000 x 1270000 EMU,
  `wp:docPr` "Picture n", `pic:cNvPr` id 0, `a:blip` with `a14:useLocalDpi 0`, `a:srcRect`, `a:stretch/a:fillRect`,
  `a:noFill`; the image part a 1010-byte PNG, sha1 77e89b28ce48..., byte-identical to the one in Jason's
  `placeholders.docx` (whose picture control has the flag and extent 1273175). Word gave this form on opening to
  every empty-node control whatever it held: case 1's **existing picture was replaced** by it, case 2's empty content
  was filled with it, and case 5, bound by hand through the XML Mapping Pane, has it too (block-level, around the
  paragraph; no alias or tag; `w:prefixMappings` with a trailing space). So **the node rules**: what the control holds
  in the file does not matter to Word, and no binder's output is shown as it was written.
- **A node holding a picture** (cases 4 and 7 on opening): Word shows the node's picture. It kept docx4j's drawing
  (docPr "red", extent 609600) and replaced the image part's bytes with the node's (blue); docx4j's red part was
  dropped from the save; no flag.
- **Office JS reads** on every picture control, open or after the run: `type` Picture; `placeholderText` null, flag on
  or off; `showingPlaceholderText` undefined in this build (as in check 65c; the property is not there).
- **Office JS writes to the node** reach the control at once. Case 6, the node set to the blue PNG through `setXml`:
  the control showed blue (Jason, screenshot), the flag cleared, the placeholder drawing kept its shape (extent
  1270000, docPr "Picture 1") with its image part swapped to blue. Case 7, the node emptied: the placeholder came back,
  flag on, a new drawing ("Picture 4"). The save holds both nodes as written (`pic6` blue, `pic7` an empty element).
- **`insertContentControl('Picture')`** is accepted by this Word (first way tried): Word's form, block-level, flag on.
- **Reopened:** every control as the run left it.

**For docx4j.** (1) The NonXSLT and StAX binders' text placeholder under a `w:picture` control must go: Word repairs
it. (2) Word fills an empty-node picture control with its own form on opening, whatever the file holds, so the
binders' output for an empty picture node is seen by Word only until it opens the file; it is seen as written by
other consumers (docx4j's PDF and HTML output, other editors). core-ts's "keep the existing picture" is replaced by
Word on opening too (core-ts: it will propose its change after this check).

| # | Question | Options | Recommended |
|---|---|---|---|
| D11 | What the binders write for a picture control whose node is empty | (a) Word's form: the flag and an inline drawing of a placeholder image: (a1) Word's stock 1010-byte PNG (Microsoft's artwork, shipped in every Word file with a picture control; whether docx4j may ship it is Jason's call), or (a2) an image of docx4j's own; (b) empty content, as the XSLT binder writes today, no flag (measured: Word accepts it and fills its form on opening); (c) the existing picture kept (core-ts today; Word replaces it on opening) | (b), for all three binders: measured clean, the same in Word as (a) once opened, no image to ship, nothing stale shown elsewhere; the control's picture is the node's or nothing. (a2) if a placeholder should show outside Word. The flag with empty content is unmeasured, so (b) writes none. Awaits Jason |
