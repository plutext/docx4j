# CR-072: bound content controls in the text model, read as Word shows them (REQ-509, D34)

Status: PROPOSED 2026-11-02. Drafted by Claude Opus 5.5 (session docx4j-12) on Jason's ruling of the same day on the
portfolio paper's D34 ("i'd rule D34 for Word, unless it has any impact on our opendope processing???"; checked:
none, §2; then "yes" to the ruling with the two exceptions of §4 and to this CR). WD 0.62 (docx4j-portfolio
59a3ae0) rewrites REQ-509 to the ruling. Target 17.3.6.

## 0. Summary

The text model (CR-046) reads a data-bound control's bound value only where the control is a plain-text one
(`w:text`). Word shows the bound value for every control it binds: a control with no type element (a rich-text
one, which Word turns into a plain-text one), a date formatted, a drop-down's item, a combo box's value, a check
box's glyph, and a repeating section's items one per XML node. docx4j's own generator (`Docx4J.bind`) already
writes what Word shows in every one of these cases (§1), so today the text model disagrees with both. This CR
makes it read what they write, with the same code where the generator's is reusable, and keeps the stored text
in two cases nobody has measured (§4).

## 1. Measured: Word, the generator, the text model

The portfolio's probes of 22 and 23 October (`~/vbShares/Office 2016/bind-fun/`; Word 365's readings in the
paper's §12a.3, its saves beside them). Generator: docx4j 17.3.6-SNAPSHOT, `Docx4J.bind(pkg, null,
FLAG_BIND_BIND_XML)`, then the text model over the result. Text model: `Texts.of` over the probe, 2 November.

| Probe, control | Word 365 | Generator | Text model today |
|---|---|---|---|
| bind-plain B1 to B7, B9 (header) | value | value | value (B7's store item ID resolved, CR-058 D4) |
| bind-props PR1 to PR4 | value | value | value (17.3.4) |
| bind-plain B8 (no type element) | value `B10_XML_VALUE`, save adds `w:text` | value | **surface** `B8_RICH_STALE_SURFACE` |
| bind-rich-flatopc RT1 (no type element, escaped Flat OPC) | the escaped XML as characters | the same | **surface** `RT1_STALE_SURFACE` |
| bind-typed DT1 date, XML `2026-10-22` | `22 October 2026` | `22 October 2026` | **surface** `1 January 2001` |
| bind-typed DD2 drop-down, XML `v2` | `DD2_SECOND_ITEM` (the item's text) | the same | **surface** `DD2_FIRST_ITEM` |
| bind-typed CB3 combo box | `CB3_XML_FREE_TEXT` | the same | **surface** `CB3_FIRST_ITEM` |
| bind-typed CK4 check box, XML `true` | ☒ | ☒ | **surface** ☐ |
| bind-repeating-ns0, three nodes | three items, `RS_ITEM_A` to `C` | the same | **one item**, `RS_ITEM_A` |
| bind-repeating (prefix `p`), three nodes | three items, none filled (a Word defect, §12a.3) | three items, filled | one item |
| bind-repeating-zero, no node | the stored item | the same | the same |
| opendope-template OD1 to OD7 | OD1's value; the rest as authored | (generated view) | the same as Word |

## 2. OpenDoPE: no effect on processing

Jason's condition. The generator (`Docx4J.bind`, `OpenDoPEHandler`, `BindingHandler`, `bind.xslt`,
`BindingTraverser*`) never calls the text model: nothing in `org.docx4j.model.datastorage`, `org.opendope` or
`Docx4J` uses `org.docx4j.model.text`, and `BoundValues` is used only inside it. So no generated document changes.
What changes is what `Texts`, the Markdown export and the inspector report, and §1 shows that it moves toward what
the generator writes. The OpenDoPE template probe reads the same before and after: OpenDoPE's own constructs
(`od:xpath` without a `w:dataBinding`, conditions, `od:repeat`) are not Word's, Word shows them as authored, and
the text model's default view is Word's (the paper's D19).

## 3. The change

In `Extractor.bound()` (now: `isPlainText(pr)` or nothing), a control with a `w:dataBinding` that resolves is read
as Word shows it, by its type:

- **`w:text`, or no type element**: the value, as today for `w:text` (B8, RT1). Word's save gives such a control a
  `w:text`; the text model writes nothing.
- **`w:date`**: the value formatted as the generator formats it, by the same code: `w:dateFormat` in the DATE
  field's picture language (`FormattingSwitchHelper.formatDate`), the language `w:lid` else the document's default
  (`BindingTraverserXSLT.dateLanguage`, CR-058 D2), the stored time nominal unless the package opts into Word's
  conversion (`conversionZone(pkg)`, CR-058 D6; D1). A value that is not a date shows as stored; an empty one shows
  the placeholder, as an empty text binding does. The generator's date pieces are package-private or return a run;
  phase 1 exposes the string they produce, once, for both.
- **`w:dropDownList`**: the item's display text (`ListControls.displayTextFor`, the generator's); a value no item
  has, as stored.
- **`w:comboBox`**: the value.
- **`w14:checkbox`**: the checked or unchecked glyph (D3).
- **`w15:repeatingSection`** with a `w15:dataBinding` (or `w:dataBinding`) selecting N nodes: its first item read N
  times, each item's inner bindings evaluated against the i-th node, as the generator's `processRepeat` rewrites
  them; with no node, the stored item (W15RepeatZeroException's rule). Phase 2 (D4).

Unchanged: a control whose binding does not resolve, or whose value is empty and shows its placeholder, reads its
surface; `bindings: surface` reads every surface; `sdt.binding_differs` is raised wherever the value shown differs
from the surface, for the new types too.

**The inspector** (`InspectWalk.loadBindings`) flags `sdt.stored_surface` by comparing the raw XML value with the
surface, for every bound control. For a date control bound correctly that is a false finding today (`2026-10-22`
against `22 October 2026`); for a drop-down, the value `v2` against its item's text. Phase 1 compares the text the
text model shows instead, so the two agree.

## 4. The two exceptions (surface, until measured)

1. **An explicit `<w:richText/>` with a `w:dataBinding`.** The generator leaves it unbound by design
   (`bind.xslt`: `w:dataBinding and not(w:sdtPr/w:richText)`); what Word does is unmeasured (no probe has an
   explicit `w:richText`; B8 and RT1 have no type element). Phase 0 builds the one-case probe.
2. **OpenDoPE's own rich-content controls**: a `w:tag` holding `od:ContentType=application/xhtml+xml` or
   `od:progid=Word.Document`. They carry no `w:dataBinding` by design (the XPath is in the tag, `bind.xslt`'s
   comments), so the change does not reach them; where one does carry a binding, the generator lets the tag decide,
   and the text model reads the surface.

Both are in WD 0.62's NOTE to REQ-509. Exception 1 is this CR's one unmeasured rule (CR-043 D5's convention), until phase 0.

## 5. Decisions

| # | Question | Recommendation |
|---|---|---|
| D1 | A bound date-time's zone | As the generator: nominal unless the package opts in (`OpcPackage.setBoundDatesConvertedAsWord`, or the property); then the package's zone. One document gives one text everywhere by default, and the text model never disagrees with what the generator would write for the same package. |
| D2 | The repeating section whose bindings use a prefix Word rewrites without its mapping (bind-repeating, prefix `p`) | Fill the items, as the generator does; Word's empty items are a defect of Word's (the paper's §12a.3: "it looks like a Word defect"), not a rule. |
| D3 | The check box's glyph | The `w14:checkedState` / `w14:uncheckedState` character where the control declares one (Word draws that one), else ☒ / ☐. The generator writes ☒ / ☐ always (its TODO, `checkboxRun`); the two agree on every control that declares the defaults, as Word writes them. Making the generator use the declared glyph is a follow-up, not this CR. |
| D4 | How the text model expands a repeating section without changing the package | Read-only, in the text model: the item read once per node, with a binding context that rewrites the inner XPaths as `processRepeat` does, the rewrite rule shared with the generator (phase 2 factors it out). Not a deep copy run through the generator: its steps mutate, and need the integrity step around them. |
| D5 | Scope of the inspector change | The main document part, as `loadBindings` today; headers stay unflagged (a separate, older gap). |

## 6. Phases

- **Phase 0: the Word probe for exception 1.** One document: an explicit `<w:richText/>` bound to a plain value,
  block and inline, beside a control with no type element for comparison; Jason opens and saves it in Word. The
  result settles whether exception 1 becomes the value (then the generator's `not(w:richText)` is a question for
  OpenDoPE) or stays.
  **Built 2026-11-02**: `~/vbShares/Office 2016/bind-fun/bind-rich-explicit.docx`, with its builder
  `build_bind_rich_explicit.py` beside it (bind-plain's package: its store item and item properties, no header). RX1
  inline and RX2 block-level carry an explicit `<w:richText/>`; RX3 is B8's shape (no type element) for comparison; and,
  at core-ts's request (CR-020), CB4 is a combo box bound to `c2`, the value of the item shown as `CB4_SECOND_ITEM`, and
  CB5 the converse, bound to an item's display text. Each surface is stale. docx4j's generator (`Docx4J.bind`, bind
  only) leaves RX1 and RX2 as they are, writes RX3's value, and shows CB4 and CB5 as `CB4_SECOND_ITEM` and
  `CB5_SECOND_ITEM`; the text model reads every surface. Waiting for Jason's Word open and save.
  **Prior evidence** (core-ts check 56, 2026-10-23, Word 365 16.0.20326.20232; core-ts CR-002 §61.3; its
  `test/fixtures/check56/56-writes.docx` and Word's save `test/check56/56-writes-word365.docx`, read here): sixteen
  bound block controls, four untyped, eight `w:text`, four explicit `w:richText`; in Word's save all sixteen carry
  `w:text`, the explicit `w:richText` ones included, and Office JS reports each as PlainText. An explicit `w:richText`
  block holding two paragraphs (56c) would not open; Word's repair gave it `w:text` and one paragraph refilled from its
  node. So Word binds an explicit `w:richText` control as plain text, and exception 1 is likely to become the value.
  What check 56 does not show, and the probe adds: whether an ordinary open refreshes a stale surface (56-writes'
  surfaces equalled their nodes), and a control at run level (RX1). If Word binds them, docx4j's generator, which
  leaves an explicit `w:richText` unbound (`bind.xslt`), differs from Word there; core-ts's `applyBindings` already
  binds RX2 as plain text (its CR-012 §27.3). That is a generator question for a CR of its own, not this one's.
- **Phase 1: plain-text and typed controls.** `Extractor.bound()` by type (§3), the date and drop-down text from
  the generator's code, the check box's glyph, the inspector's comparison. Tests: the bind-plain B8 and the
  bind-rich-flatopc RT1 shapes; `typed-controls.docx` (the bind-typed probe, already a fixture) through `Texts.of`,
  expected Word's readings; the two exceptions read their surfaces; `bindings: surface` unchanged; the inspector no
  longer flags a correctly bound date.
- **Phase 2: repeating sections** (D4). Tests: the three repeating probes' shapes, three items and the zero case;
  the generator's output and the text model's reading of the template equal.
- **Phase 3: hand-offs.** WD 0.62 is in place; the extraction suite's bound cases (REQ-509) are revised to it, the
  ports told (core-ts, docx4j-python; docx4j-mcp reads through docx4j).

## 7. Risks

- A bound value can be long (RT1: a whole Flat OPC package as characters). That is what Word shows; the text model
  already reads long plain-text values.
- The date text now depends on the package's opt-in (D1), as the generator's does; a reader who wants the stored
  text has `bindings: surface`.
