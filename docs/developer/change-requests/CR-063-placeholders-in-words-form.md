# CR-063: content-control placeholders written in Word's form

Status: PHASES 1 AND 2 DONE 2026-10-25 (§12; Claude Fable 5.1, session docx4j-25); phase 3, Jason's Word check,
next. ACCEPTED 2026-10-25 (Jason: "CR-063 D1 to D6 accepted as recommended, please implement"): D1 to D6 as
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
| 3 | Jason's Word check: phase 0's writes on the new output reach the node and survive the reopen | 30 min with Jason |

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

**D2.** `PlaceholderEntries.referenceFor(pkg, sdtPr)` gives the entry the control shows; `contentOf(pkg, entry, rPr)`
a copy of its `w:docPartBody` paragraphs. What a control shows follows core-ts's §63.18: a block-level control shows
every paragraph, each run as the glossary holds it with the control's `w:rPr` applied over it (as `createPlaceholder`
applies it today); the control's own paragraph keeps its `w:pPr`, and the entry's further paragraphs get a copy of
it. The entry's `w:pPr` (naming the glossary's own styles), proofing marks and bookmarks stay in the glossary. A
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
