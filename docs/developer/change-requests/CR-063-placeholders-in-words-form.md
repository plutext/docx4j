# CR-063: content-control placeholders written in Word's form

Status: PROPOSED 2026-10-24, drafted by Claude Opus 5.5 (session docx4j-25) at docx4j-core-ts's request, from Jason
("Jason asks that docx4j consider writing Word's form; it's your decision"): its checks 63 and 64 found that Word loses
Office JS's writes into a bound control showing a placeholder in the form docx4j's binding writes. Recommended: yes
(§6). Phase 0 is core-ts's check 65, which takes docx4j's question too (§5). The implementation is OpenDoPE binding, which CLAUDE.md gives to Fable.

## 0. Summary

A content control showing its placeholder comes in two forms:
- **Word's form:** `w:placeholder` names a glossary entry (`w:docPart`) holding the placeholder's text, and the
  control's content is a copy of it, with `w:showingPlcHdr`. Every control Word makes is this form, naming
  `DefaultPlaceholder_-1854013440` ("Click or tap here to enter text.").
- **The bare form:** `w:showingPlcHdr` and a Placeholder Text run, with no `w:placeholder`. docx4j's binding writes
  this for an empty value, and drops any `w:placeholder` a template had.

In the bare form, Word treats the placeholder's runs as the control's content (core-ts check 63). An Office JS write
into a bound control never reaches its custom XML node, so when Word reopens its own save, it refills the control from
the empty node and the write is lost, tracked revisions included. Writes also keep the Placeholder Text style, and the
control's `insertParagraph` leaves `w:showingPlcHdr` on. In Word's form every write takes the placeholder away, reaches
the node and survives the reopen (check 64).

Proposed: docx4j's binders keep `w:placeholder`; an empty value shows the placeholder the control names, from its
glossary entry, as Word does; a control that names none gets Word's default entry, with the glossary part added where
missing. Check 65 settles what Word needs of the glossary (§5, phase 0).

## 1. What Word does (core-ts CR-002 §63.2, §63.5; Word 365 16.0.20326.20232, Jason's runs, 2026-10-24)

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

**Not measured:**
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
2. **An empty value shows the placeholder the control names** (D2): the content of the glossary entry its
   `w:placeholder` names, with the control's run properties as today, and `w:showingPlcHdr`. The lookup belongs in
   the step that adds `w:showingPlcHdr` (`bind.xslt`'s `w:sdtPr` template; `postBindSdtPrHygiene`), which sees the
   `w:sdtPr` and the content both. `createPlaceholder` keeps its signature, for the pluggable inserters.
3. **A control that names no placeholder, or names a missing entry** (D3), gets Word's default:
   - `w:placeholder` naming `DefaultPlaceholder_-1854013440`;
   - the entry in the glossary;
   - the glossary part itself, where there is none.

   Check 65 decides whether the entry is needed and how little of the glossary will do.
4. **The default's wording becomes Word 365's,** "Click or tap here to enter text." (D5), so the content matches the
   entry it names.
5. **A configured placeholder run** (`docx4j.model.datastorage.placeholder`) is still honoured for a control that names
   none, written as an entry of its own, named and stored as check 65 finds Word does (D4).

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
| 0 | Check 65's result from core-ts (Jason runs it; written 2026-10-24, core-ts `test/fixtures/check65/`, the share's `__tmp/65/`). 65a: controls naming `DefaultPlaceholder_-1854013440` with no glossary part. 65b: a glossary of `document.xml` alone, holding the default entry and one of its own text named with 32 hex digits, and a control naming an entry it lacks. Each repeats check 64's telling writes (the style, the flag, a tracked Replace, a bound node); case 6 sets `placeholderText` through Office JS, so Word's save shows how it names and stores an entry for other text; a control made by `insertContentControl` shows the glossary Word adds where there is none; the last case is a bare-form bound control Jason types into by hand, saves and reopens. No docx4j round of its own | with core-ts |
| 1 | The binders keep `w:placeholder` (D1): `bind.xslt`'s template and `postBindSdtPrHygiene`'s removal go; a test that the three binders agree | 1 h |
| 2 | An empty value in Word's form (D2 to D5): the entry the control names, or Word's default with the glossary part and entry added as check 65 requires; `placeholder.xml`'s wording; tests; the Office-open check (Word opens it without repair) | 3 h |
| 3 | Jason's Word check: phase 0's writes on the new output reach the node and survive the reopen | 30 min with Jason |

## 6. Decisions (recommendations; Jason decides)

| # | Question | Options | Recommended |
|---|---|---|---|
| D1 | `w:placeholder` through binding | (a) kept on every control; (b) kept on unbound controls only; (c) dropped, as today | (a): it's Word's form, and the reason for dropping it no longer holds (§2) |
| D2 | What an empty value shows | (a) the entry the control names, as Word shows it; (b) `placeholder.xml`'s run always, with the reference kept | (a): (b) shows one text and names another, which Word would replace with the entry's when the control is emptied |
| D3 | A control naming no placeholder, or a missing entry | (a) Word's default entry added (glossary part and entry where missing, as check 65 requires); (b) the reference alone | (a), or (b) if check 65 shows the reference alone is enough |
| D4 | A configured `docx4j.model.datastorage.placeholder` | (a) honoured for a control naming none, as an entry of its own; (b) deprecated | (a): deployments set it to show their own wording |
| D5 | The default wording | (a) Word 365's "Click or tap here to enter text."; (b) "Click here to enter text.", as today | (a): the shown text matches Word's default entry |
| D6 | A switch back to the old behaviour | (a) none; (b) a property | (a): the old form is the one that loses writes, and its reason no longer holds. A deployment that wants no placeholder text can configure an empty run (D4) |

## 7. Risks

- **A template's placeholder text shows where it didn't** (D2). A Word-made template whose entries hold text meant for
  the template's author, not the reader, will show it for an empty value. That is what Word shows when the control is
  empty, and the change is named in the CHANGELOG.
- **A glossary part is added to code-built templates' output** (D3). Word's glossary is five parts; check 65 says
  whether one will do.
- **A pluggable inserter** that returns its own placeholder run keeps working: the entry lookup replaces the content
  only where it carries the Placeholder Text style, as `w:showingPlcHdr` is decided today.
- **The ports:** core-ts's D8 and this CR must agree on the entry for other text (D4), so core-ts is consulted before
  phase 2.

## 8. Sources

- docx4j-core-ts CR-002 §63 (`2070694`, unpushed): §63.2 check 63's run, §63.5 check 64's run, §63.6 the decisions;
  Word's saves in `test/check63/`, `test/check64/` with SHA-1 manifests.
- `bind.xslt` lines 1155 and 1220; `BindingTraverserCommonImpl.postBindSdtPrHygiene`;
  `BindingTraverserXSLT.createPlaceholder`; `placeholder.xml`; `docx4j.properties`
  (`docx4j.model.datastorage.placeholder`).
- CR-058 D3 (an empty date value shows the placeholder).
- ECMA-376 Part 1, §17.5.2 (structured document tags: `placeholder`, `showingPlcHdr`) and §17.12 (the glossary document).
