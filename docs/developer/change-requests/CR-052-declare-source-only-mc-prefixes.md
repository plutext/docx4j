# CR-052: a save declares the `mc` prefixes the source bound, wherever the content goes

Status: ACCEPTED 2026-10-20 (Jason: "accept the recommendations and commit the CRs"), D1 to D5
as recommended, D4 as (c) after core-ts's count of Word's saves. Phase 0 DONE 21 October 2026 (Jason:
"start CR-052 phase 0, with the Word round (4) folded into CR-052's own Word check"): §1 corrected and the
mechanism found (§1.1), the tests in core-tests, D4 (c)'s rule as the ports implement it (§4), and Word's
check: Word repairs a file with an undeclared `Requires` prefix, so the priority stays medium (§5, §7).
Phase 1 DONE 21 October 2026 (Jason: "commence CR-052 phase 1"; on Fable, CLAUDE.md's marshal path): the rule
on every marshal path under both runtimes, the record carried through `deepCopy`, 24 tests (§5, §9); Word 365
opens docx4j's saves of every case cleanly (§5, Jason's run); committed 5df2e253e. Phase 2 (hand-offs) DONE 21 October 2026
(Jason: "start CR-052 phase 2"): objects-ts, core-ts and python acknowledged (§5; objects records D1 (b) as a gap
for 0.4.4, python's CR-011 phase B ports the rule, core-ts takes it with the objects release). CR-052 done.
Proposed 2026-10-20. Requested by the docx4j-generated-objects-ts session at Jason's word ("send
the CR request, and ask it for its opinion on priority, to be written into the CR"); the same
defect is objects CR-011 (`docs/change-requests/CR-011-declare-source-only-prefixes.md` there, 3db3613).
Drafted by Claude Opus 5.5 (session docx4j-25), with the request's points measured first (§1). Phases
in §5; decisions in §6; priority in §7, as asked. Touches the marshal path (`XmlUtils`,
`JaxbXmlPart`), which CLAUDE.md lists among the widely-shared code to change on Fable.

## 0. Summary

An `mc:Ignorable`, `mc:Choice/@Requires`, or a Choice's own `mc:Ignorable` or `mc:MustUnderstand`
names namespace prefixes, so each prefix it names must be declared where the attribute is. docx4j
declares them at marshal through its static prefix table, for the prefixes the part recorded at
unmarshal. A part docx4j never unmarshalled is saved from its source bytes and keeps every
declaration. Once it is unmarshalled, which any edit or read of its content does, a prefix the table
lacks loses its declaration though nothing moved: a producer's private prefix, or a second prefix for a
known namespace (§1, corrected in phase 0). Content that moves loses more: a Choice moved or copied
into another part or package, or built in code, also loses a prefix the table knows, since the
destination part never recorded it. The saved part then names a prefix it never declares.

Office's own content is not immune once it moves: Word writes a chartex chart's Choice with
`Requires="cx1"` (or `cx2` and up), and the branch's content uses the `cx` namespace, not `cx1`'s, so a
merge that copies such a chart is exposed (§2; measured in phase 0: Word's chart re-saved keeps `cx1`,
its paragraph copied or merged loses it). Whether Word refuses or repairs the result is phase 0's Word
check (§5).

The fix: each Choice carries the bindings its prefixes had in the source, across a move and a copy;
every marshal declares each `mc`-named prefix from the content's own record first, then the table.

## 1. What docx4j does today (measured 2026-10-20 on VERSION_17_3_4 at cf1abf001)

Scratch drivers against the installed 17.3.4-SNAPSHOT, with an lxml check of each saved part
(every `Requires` prefix resolved against the `mc:Choice`'s in-scope namespaces). The request's seven
points are its reading of the code; these are the measurements behind §2.

**Corrected in phase 0** (21 October 2026, at d2d0a3861). Cases 1 and 2b, as first measured, saved a
part nobody had unmarshalled, and such a part is copied from its source bytes
(`ZipPartStore.saveJaxbXmlPart`: `isUnmarshalled()` is false). Unmarshalled first, both are undeclared.
The table gives both readings and adds Word's chartex chart (cases 5 and 6). Each row is a test in
core-tests' `McSourcePrefixesTest`, which checks every prefix an `mc` attribute names in the saved
`word/document.xml`.

| Case | Result |
|---|---|
| 1. `extraction-probe.docx` (core-tests' `text-extraction/`, 4a89a5967) loaded and saved, the part unread | declared: the save copies the source bytes, `xmlns:zz9` and `xmlns:shape2010` on each `mc:AlternateContent` as the source has them. |
| 1'. The same, the part unmarshalled first (`getContent()`, as any edit does) | **undeclared:** `zz9` (3 Choices) and `shape2010` (1). `wps` and `w14` are declared on the root. The content's `shape2010:wsp` is written `wps:wsp`, while `Requires` still says `shape2010`. |
| 2a. A root `mc:Ignorable="zz9"` with `xmlns:zz9` on the root, the tree set from code (`XmlUtils.unmarshalString`, `setJaxbElement`) and saved | **undeclared:** `mc:Ignorable="zz9"`, no `xmlns:zz9` anywhere. |
| 2b. The same document loaded from its package and saved | unread: declared (its bytes). Unmarshalled: **undeclared,** as 2a. |
| 3. A Choice built in code, `Requires="cx1"` (the fragment declared `xmlns:cx1`), added to a new package and saved | **undeclared,** though the table knows `cx1` (`NamespacePrefixMappings`:572, :968). |
| 4a. The two `zz9`-only paragraphs of case 1 moved (same objects) into a new package | **undeclared.** |
| 4b. The same, deep-copied (`XmlUtils.deepCopy`) | **undeclared.** |
| 4c. Every paragraph of case 1 holding a Choice deep-copied into a new package | `wps` 6 Choices, `w14` 2: declared (`createPackage`'s part declares both). `zz9` 3 Choices and `shape2010` 1: **undeclared.** |
| 5. Word 365's waterfall chart (core-tests' `charts/waterfall.docx`: `Requires="cx1"` over `cx` content, `cx1` declared on the root), unmarshalled and saved | declared on the root: the load recorded `cx1`, and the table knows it. |
| 6. Its chart's paragraph deep-copied into a new package; moved into case 1's document, loaded (a merge) | **undeclared** both ways (the namespaces only: the chart part is not carried). |

So the request's points 1 and 2 hold for any part docx4j unmarshals. Only a part saved unread
escapes them. Points 3 and 4 hold as measured. §1.1 says why.

### 1.1 How the bindings are kept, and lost (phase 0)

- **At unmarshal,** `Docx4jUnmarshallerListener.afterUnmarshal` does two things for each Choice:
  - it adds the Choice's `Requires`, `mc:Ignorable` and `mc:MustUnderstand` values to the part as
    prefix strings, without their URIs (`JaxbXmlPart.addMcChoiceNamespace`, a set of strings);
  - where its reader is a StAX reader standing at the Choice's end tag, it records on the Choice the
    URIs its `Requires` prefixes resolve to (`McSelection.recordRequiresNamespaces`, CR-046). The
    record is a weak map keyed by identity, holds `Requires` only, and is read by text extraction's
    branch selection. The binder path records the same from the DOM (`JaxbXmlPartXPathAware`).
- **The root's `mc:Ignorable`** is kept as its string. Its bindings are recorded nowhere.
- **At marshal,** `JaxbXmlPart.marshal` passes the root's Ignorable and the part's Choice prefixes to
  the prefix mapper (`setMcIgnorable`). Its `getPreDeclaredNamespaceUris` declares on the root each
  prefix the static table resolves (`NamespacePrefixMappings.getNamespaceURIStatic`). A prefix the
  table doesn't resolve is logged ("No mapping for prefix 'zz9'") and left undeclared. JAXB writes the
  content with the table's prefixes, so an alias (`shape2010`) disappears from the content while the
  `Requires` that names it stays.
- **A part never unmarshalled** is copied byte for byte, which is why cases 1 and 2b looked declared on
  20 October.

So the record that carries a source binding exists, per Choice, for `Requires` only. Nothing reads it at
marshal. A deep copy drops it: the map is keyed by identity, so the copy's prefixes resolve through the
table, to nothing for `zz9`. A move keeps it. But a move does not add the Choice's prefixes to the
destination part's set, so even a table prefix (`cx1`, case 6) goes undeclared. Phase 1 has four
things to do:
- read the record at marshal, Choice by Choice, and place each declaration by D4 (c);
- carry the record through `deepCopy` (D2);
- record a Choice's `mc:Ignorable` and `mc:MustUnderstand`, and the root's `mc:Ignorable`, as well (D1 (b));
- declare table prefixes without depending on the part's set.

**As it is after phase 1** (21 October 2026; §9 has the record):
- **The record** is `org.docx4j.jaxb.McSourceBindings`: for each `mc:Choice` the bindings of every prefix
  its `Requires`, `mc:Ignorable` and `mc:MustUnderstand` name, and for each part root the bindings of
  its `mc:Ignorable` prefixes; keyed by the object's identity, weakly. `McSelection.requiresNamespaces`
  (CR-046) reads it. It is written by the StAX listener (a Choice at its end tag; the root from the
  declarations its start tag made, which the reader wrapper keeps, since MOXy fires the root's callback
  only at the end of the document), by the binder paths from the DOM (every Choice, and the root), and by
  `XmlUtils`' DOM unmarshals (D5 (a): the root from the document element; the Choices paired with the
  DOM's `mc:Choice` elements in completion order, checked by `Requires`).
- **A copy carries it** (D2): docx4j-copy's `CopyUtils` tells one listener of each copy it makes, and
  `XmlUtils.deepCopy` reports the object it copies; docx4j-core's listener pairs a copied
  `mc:AlternateContent`'s Choices with the original's by position.
- **At marshal** `McDeclarations.plan` reads the tree once and places every prefix (§4.4): the table's
  on the root through the prefix mapper's Ignorable value as before, a source-only root prefix as a
  (prefix, URI) pair the mapper pre-declares, any other on the Choice. A marshal with a declaration to
  put on a Choice goes through an `XMLStreamWriter`: the marshaller's listener is told of each Choice
  before its start tag and the writer wrapper puts the declarations there, ahead of `Requires`. A
  marshal with none keeps its usual path. The part's set (`addMcChoiceNamespace`) still works and is
  no longer needed for a Choice in the tree.

## 2. What it affects, and what it does not

- **Affected:**
  - any part docx4j unmarshals and saves, moved or not, whose root's Ignorable or a Choice names a
    prefix the table lacks: one private to its producer (`zz9`), or a second prefix for a known
    namespace (`shape2010` for `wps`). Cases 1' and 2b (phase 0);
  - content moved or copied between parts or packages (merges, assembling from fragments,
    `XmlUtils.deepCopy`), and trees built or set in code, for any prefix the destination part did not
    record itself, whether the table knows it or not. Word's chartex charts are the Office case:
    `Requires="cx1"` over `cx` content. Re-saved, they keep it (case 5); copied or merged, they lose it
    (case 6).
- **Not affected:**
  - a part saved unread (case 1), whose bytes are copied;
  - a loaded part's table prefixes (`wps`, `w14`, case 1'; `cx1`, case 5);
  - the prefixes the destination predeclares (case 4c);
  - `ChartHosting` (CR-039), which registers its own `Requires` through `addMcChoiceNamespace`;
  - CR-051's `word` mode, which adds to the root's Ignorable only the prefixes of understood Choices,
    each with a table prefix.
- **Office's own files, unmoved:** objects measured 0 of 341 parts with a source-only prefix, so the
  unmoved case reaches other producers' files, and docx4j's own probes.

## 3. What the specifications say

ECMA-376 Part 3 (Markup Compatibility) defines `Ignorable`, `Requires`, `MustUnderstand` and
`ProcessContent` as lists of namespace prefixes, each identifying a namespace by the declaration in
scope. A prefix with no declaration in scope identifies no namespace, so the attribute does not
conform. Word and Excel repair a file whose root `mc:Ignorable` names an undeclared prefix (CR-023,
CR-024). For an undeclared `Requires` prefix, Word's behaviour is unmeasured.

## 4. Proposal

1. **The rule, on every marshal path** (`Save`/`JaxbXmlPart.marshal`, `getXML()`, the `XmlUtils`
   marshal methods, `FlatOpcXmlCreator`, the binder; CR-049's single contract): each prefix named by
   the root's `mc:Ignorable`, a Choice's `Requires`, or a Choice's own `mc:Ignorable` or
   `mc:MustUnderstand` is declared in scope where it is named.
2. **Resolution order** (D3): the binding the content carries from its source, then the table, then
   the table's aliases. Objects' order, decided there by three Excel VML parts that bind `x` to
   `urn:schemas-microsoft-com:office:excel` while the table maps `x` to SpreadsheetML main; that
   matters for `getNamespaceURIStatic`'s hard-coded `x` too. Where the Choice's own content uses the
   prefix for another namespace, a declaration there would rebind that content, so the content's
   binding stands and a warning is logged. A use elsewhere in the tree does not stop the Choice
   declaring its own (objects CR-012 §3).
3. **The bindings travel with the Choice** (D2): the per-Choice record (CR-046's, or one phase 0 finds
   is the real carrier) is read at marshal from the Choice itself, not from the part it was loaded
   into, and `XmlUtils.deepCopy` carries it to the copy. A root's Ignorable bindings are recorded on
   the part at unmarshal, as case 2b shows they are, and also by `XmlUtils`' unmarshal methods where
   docx4j's listener is set (D5).
4. **Where a prefix is declared** (D4 (c); the rule as both ports implement it, objects CR-012 §3 at
   b03ccb5 and core-ts CR-010 §14.9, folded in at phase 0):
   - a `Requires` prefix the table or its aliases give for that namespace goes on the root, as Word puts
     its fixed set;
   - any other goes on the `mc:Choice` that names it, with the source's binding, declared ahead of
     `Requires` as Word writes it: `<mc:Choice xmlns:zz9="..." Requires="zz9">`. This covers a private
     prefix (`zz9`), an alias (`shape2010`), and a table prefix the source bound to another namespace;
   - but where the marshalled root keeps that same binding anyway, because the root's `mc:Ignorable`
     names the prefix or the tree uses it there, it is declared once, on the root, and not repeated on
     the Choice (Jason, 20 October 2026, for both ports: "one declaration on the root right"). A source
     root's declaration that `mc:Ignorable` does not name moves to the Choice;
   - the root's Ignorable prefixes go on the root, which Word requires (CR-023, CR-024);
   - nothing goes on `mc:Fallback`. Word copies the source's declarations onto it; neither port does;
   - charts are no special case: `c14` is in the table, so it goes on the root;
   - a Choice's own `mc:Ignorable` and `mc:MustUnderstand` (D1 (b)) follow the `Requires` rule on that
     Choice. This is docx4j's extension; the ports cover `Requires` and the root's Ignorable.
5. **Nothing in the JAXB runtime:** docx4j already has the namespace context at each element at
   unmarshal; objects needed jsonix CR-008 for the same thing.

## 5. Phases

### Phase 0: tests, the mechanism, and Word

- §1's cases as JUnit tests in core-tests, each asserting what it measured (the failing ones marked
  as the defect), with `extraction-probe.docx` and a chartex document as fixtures.
  **Done:** `org.docx4j.jaxb.McSourcePrefixesTest`, 11 tests, with `extraction-probe.docx` and Word's
  `charts/waterfall.docx`. Each asserts today's result, and each one phase 1 changes says so.
- Find how case 1 keeps its bindings and why case 4a loses them; record it here.
  **Done:** §1.1. Case 1 never kept them: the part was saved unread.
- A Word check for Jason: a document whose chartex chart's Choice says `Requires="cx1"` with no
  declaration in scope, and one with `Requires="zz9"` undeclared. Does Word refuse, repair or open it?
  The answer sets §7's priority.
  **Built** 21 October 2026, results awaited: `Office 2016/cr052-word-check/` on the Word VM share,
  with a README.
  - `c52-0`: the probe unchanged, the control.
  - `c52-1`: Word's waterfall chart without `xmlns:cx1`.
  - `c52-2`: the probe without its `xmlns:zz9`.
  - `c52-3`: docx4j's own save of the probe once unmarshalled, `zz9` and `shape2010` undeclared.
  - `c52-4`: `c52-3` with `zz9` declared, so only `shape2010` is undeclared.

  **Run** (Jason, 21 October 2026, Word 365 through the fidelity harness's `WordGoldenRunner`,
  fields off, at his word in place of screenshots). The runner hands Word docx4j's load and save of
  each file, which was checked to leave `document.xml` byte for byte. Its script opens a file without
  `OpenAndRepair`, and a file Word does not open cleanly fails with documents4j's "the input file seems
  to be corrupt".
  - `c52-0`, the control: converted and re-saved.
  - `c52-1` to `c52-4`: **all four fail**, the PDF and the re-save alike, three attempts each.
    `c52-2` differs from the control only by its three `xmlns:zz9` declarations, and `c52-3` is
    docx4j's own save today. So Word will not open cleanly a file whose `mc:Choice` names an
    undeclared prefix: a private one, an alias, or the chartex `cx1`.
  - Opened by hand (Jason, the same day): **Word offers to repair each of the four, and the repair
    succeeds.** A repair, not a refusal, so §7's rule keeps the priority at medium. But every
    document docx4j edits and saves with such a prefix now opens with Word's repair prompt (`c52-3`).

  At Jason's word, the round also carries four probes other CRs had queued; their results go to
  those CRs and the text extraction WD:
  - CR-043's U21 to U23 (§11 there): comments when resolving tracked changes empties their range;
  - CR-053's seven unmapped HTML elements (WD REQ-513's NOTE), one host with Word's HTML styles and
    one without.

  Their results: U21 and U22 as docx4j does, U23 not (Word removes the comment; CR-043 §15). The
  seven elements map as `pre` and `code` do, each to its HTML character style: hidden where the host
  lacks the style, shown where it defines it. docx4j hides them in both hosts (for CR-053 and WD 0.29).

### Phase 1: the rule

§4, on every marshal path, under both JAXB runtimes (docx4j-JAXB-ReferenceImpl and -MOXy). Tests:
§1's cases all declared; a chartex chart copied between documents; a Choice built in code; the `x`
conflict case; the copy carries the record. A cross-port reference for case 3: objects has declared a
table-known `Requires` prefix since 0.4.2 (its CR-001 §3.2: a `Requires` prefix counts as used), with
the `cx1` case in `test/smoke.mjs`'s CR-001 §3.2 block.

**Built** 21 October 2026 (§9). `McSourcePrefixesTest` is now 24 tests: §1's cases declared where Word
puts them, the chartex copy and merge, Choices built in code (a table prefix, a private one, one set
with `McSourceBindings.set`), a Choice's own Ignorable and MustUnderstand, the `x` conflict both ways,
every marshal path (`getXML`, `XmlUtils` to a String, a DOM and a stream, Flat OPC, the binder, the
canonicalizing path), the record on load and through `deepCopy`, and the plan read directly. Green on
the RI (the suite) and on MOXy (run by hand, with `McIgnorableMarshalStateTest`, `McSelectionTest`
and `McLoadModeLoadTest`, 53 tests).

**Word check** staged for Jason, `Office 2016/cr052-word-check/phase1/` (README there; the runner's
corpus in `runner-phase1/`): docx4j's new saves of the probe unmarshalled (phase 0's `c52-3`) and
copied, four Choices built in code (cx1, zz9, shape2010, wps, each line naming the branch taken), a root
Ignorable naming zz9, and Word's chartex chart copied beside itself. Expected: all open without a
repair prompt.

**Result** (Jason, 21 October 2026, 10:44, Word 365 through the golden runner, fields off): **all five
open cleanly**, a PDF and a re-save each, where phase 0's `c52-3` had failed with a repair. The probe
re-saved by phase 1 renders the same tokens as phase 0's untouched control (every marker token, identical
counts). `c52p1-3` shows `CX1_CHOICE_TAKEN`, `SHAPE2010_CHOICE_TAKEN`, `WPS_CHOICE_TAKEN` and
`ZZ9_FALLBACK_TAKEN`: Word reads a prefix declared on the Choice (the alias, the private one) as it
reads one declared on the root. Word's own re-saves declare every prefix (the lxml check) and
normalise: the probe's eleven Choices become nine `Requires="wps"` text boxes with VML fallbacks, and
the root's `mc:Ignorable` goes back to Word's fixed set, `zz9` dropped from it. The files were built
before the review round of §9, which changed nothing in their markup but the XML declaration (now
JAXB's own, with `standalone="yes"`, as every other docx4j save has).

### Phase 2: hand-offs

objects-ts (its CR-011 is the same rule; the resolution order agrees; written here as waiting on
jsonix CR-008, which had in fact shipped in @docx4j/jsonix 3.5.0, and CR-011 in objects 0.4.3, as its
acknowledgement corrects), core-ts (it reproduced the editor's case),
python (CR-049's table records its CR-011 Q11 as the same family). Acceptance: each acknowledged.

**Sent** 21 October 2026 (docx4j-25, by session message, at 5df2e253e), each with the rule as built
(the record and what sets it, D3's order, D4 (c)'s placements with the once-on-the-root and
mc:Fallback refinements, the clash, the two clauses not implemented), Word's phase 1 result, the
oracle (`McSourcePrefixesTest`, 24, with its two fixtures) and the CR's sections; to objects-ts with
its Consumers point (the ports now agree on (c)) and its jsonix CR-008 dependency, to core-ts with six
cases worth checks and the note that its weekly parity run sees nothing until the push, to python with
lxml's per-element `nsmap` named as its record and content moved between trees as its risk.

**Acknowledged:**
- objects-ts (21 October 2026): acknowledged; the rule is the same. It ran objects at fd967f4 over the two
  fixtures: in the probe's `document.xml`, `zz9` once on each of its three Choices and `shape2010` on its
  Choice, written `<mc:Choice xmlns:shape2010=".." Requires="shape2010">`, `wps` (6) and `w14` (2) on the
  root, which matches `anUnmarshalledPartDeclaresEveryPrefixWhereWordDoes`; in its `chart1.xml`, `c14`
  on the root; in waterfall, `cx1` on the root; nothing undeclared, no warning. Differences: D1 (b) is a
  gap in objects (on `aChoicesOwnIgnorableAndMustUnderstandAreDeclared`'s input it writes the Choice's
  `mc:Ignorable="zz9" mc:MustUnderstand="w14"` with neither declared and no warning, the table's `w14`
  stripped as otherwise unused), recorded as a known difference in objects CR-012 §7 (fd967f4), with a
  recommendation to Jason that objects close it in 0.4.4 by this rule; objects records only the bindings
  the table and its aliases would not give (a Choice its `Requires`, the root every source-only binding
  in scope, a Choice without a record falling back to the root's), with the same output; its order is
  source, table, `IGNORABLE_PREFIX_ALIASES` (`x` for SpreadsheetML, which docx4j's table holds), then a
  warning; the `x` clash case differs in spelling only (objects writes SpreadsheetML as `s:`, so its
  `<s:sheetData/>` does not clash and the Choice keeps the source's `x`); it has no counterpart of
  `McSourceBindings.set` (a code-built Choice uses the root's record or gets the existing warning).
  Two corrections to this CR, taken: objects CR-011 no longer waits (jsonix CR-008 shipped in
  @docx4j/jsonix 3.5.0, CR-011 in objects 0.4.3), and the Choice placement dates from objects 869b725
  (b03ccb5 recorded Jason's once-on-the-root confirmation). Objects CR-012 is proposed as 0.4.4, not
  pushed or released.

All three acknowledged: phase 2 done, 21 October 2026.
- core-ts (21 October 2026): acknowledged. core-ts writes no prefixes itself; its save is the objects
  facade's (its CR-010 §14.9: "nothing to change but the lockfile"). On objects 0.4.3, the released
  range it locks, a source-only `Requires` prefix is declared from the source's binding on the part root,
  with D3's order (source, table, aliases) but not yet D4 (c)'s placement; objects CR-012 (implemented
  2026-10-20, unreleased) is the (c) placement with the narrowed clash rule (its §7 cites 4b9c71288), and
  core-ts takes it with a range raise once released. It reads objects CR-012 (lines 90 and 130) as not
  declaring a Choice's own `mc:Ignorable` and `mc:MustUnderstand`, which D1 (b) does here: put to
  objects-ts by docx4j-25 to close or record. The six cases are candidates for core-ts tests when the
  raise lands, against a tarball before the objects release; not scheduled (it is on its CR-012 phase 3).
- python (21 October 2026): acknowledged, with its save measured at its HEAD (before its CR-011 phase B,
  in progress). No record for a Choice; the root's `mc:Ignorable` prefixes are re-declared from its table
  or the source root's `nsmap`; nothing goes on a Choice, and lxml's cleanup drops a declaration used
  only in an attribute value. Measured consequences: a re-marshalled save of `extraction-probe.docx`
  leaves `zz9` (3) and `shape2010` (1) undeclared, and of `waterfall.docx` `cx1`, though its source
  declares it on the root (new to python; its CR-011 Q11 had the shape2010 case). Its CR-011 phase B
  (Q3, under way) records each Choice's `Requires` URIs at parse in its xsdata fork and re-declares on
  write, told to follow §4 and §9 (record, order, Word's placement, the clash), held to the applicable
  `McSourcePrefixesTest` cases, closing Q11 too; its user gets a Word check of the two fixtures saved.
  Commits to follow when phase B lands.

## 6. Decisions (for Jason; each with a recommendation)

All five decided as recommended on 20 October 2026 (Jason: "accept the recommendations and commit
the CRs"); D4's recommendation had moved to (c) the same day, on core-ts's count of Word's own
saves.

- **D1. Scope.** (a) `Requires` only; (b) `Requires`, the root's Ignorable, and a Choice's own Ignorable
  and MustUnderstand. **Recommended: (b)**, since docx4j already records all four at unmarshal, and
  objects covers only the first two.
  **Decided** (Jason, 20 October 2026): (b).
- **D2. Copies.** (a) Carry the record across `XmlUtils.deepCopy`; (b) accept the loss and document
  it. **Recommended: (a).** Copying is how merges move content, and case 4b is the common way in.
  **Decided** (Jason, 20 October 2026): (a).
- **D3. Resolution order.** Source, then table, then aliases, the tree's own conflicting declaration
  winning with a warning. **Recommended,** as objects decided it.
  **Decided** (Jason, 20 October 2026): as recommended.
- **D4. Where a `Requires` prefix is declared.** (a) On the `mc:AlternateContent`; (b) on the root;
  (c) where Word 365 puts it: a prefix in Word's fixed root set (docx4j's table) on the root, any other
  on the `mc:Choice` that names it. **Recommended: (c),** after core-ts's count of Word's own saves
  (below); before it, (a). (a) is what docx4j does for a loaded part now: it is local and moves with the
  element. Any local declaration, (a) or (c)'s Choice, also handles one prefix bound two ways in one
  part, which a root declaration can only warn about. objects CR-011 declares on the root, under its
  CR-001 convention; its Consumers section records the difference for Jason. If the ports are to
  agree, objects would change for the prefixes the tree does not use (generated-objects-ts,
  2026-10-20).
  - *Measured* (core-ts, sent at Jason's request 2026-10-20; core-ts CR-010 §14.9, uncommitted):
    2,728 Word 365 saves, 24,075 WordprocessingML parts.
  - A `Requires` prefix in Word's fixed root set is declared on the root, 664 of 664 (`wps` 432,
    `cx1` 140, `wpg` 73, `cx2` 10, `v` 5, `wpi` 2, `wpc` 1, `wp14` 1).
  - A prefix outside the set goes on the `mc:Choice` that names it, 3 of 3. These are all `zz9`, in
    Word's resaves of the fallback-rule probes (`text-extraction/fallback-rule/round2/`, build
    16.0.20326). The sources declare it on `mc:AlternateContent`; Word moved it onto the Choice and
    onto a sibling `mc:Fallback`, and did not add it to `mc:Ignorable`.
  - In a WordprocessingML part, Word never declares anything on `mc:AlternateContent`. In chart
    parts, `xmlns:mc` is on it and `c14` on the Choice, 37 of 37.
  - The three placements are equivalent XML, and Word 365 reads them alike (core-ts check 50, run by
    Jason on 20 October 2026). An alias for `wps`, declared on the root, on `mc:AlternateContent` or on
    `mc:Choice`, showed the text box each time; a declared `Requires="zz9"` showed the Fallback. The
    sample for (c)'s Choice is three saves of docx4j's own probes.
  - The ports' refinements (the binding the root keeps anyway, nothing on `mc:Fallback`, the clash)
    were folded into §4 at phase 0.

  **Decided** (Jason, 20 October 2026): (c), after core-ts's count of Word's own saves.
- **D5. A tree set from code.** (a) `XmlUtils`' unmarshal methods record the root's and the Choices'
  bindings when docx4j's listener is set; (b) document that such a tree's private prefixes are the
  caller's to declare. **Recommended: (a)** where it costs nothing more than the listener already
  does; otherwise (b).
  **Decided** (Jason, 20 October 2026): (a) where it costs nothing more than the listener already
  does; otherwise (b).

## 7. Priority (docx4j-25's view, as the request asked)

**Medium: after CR-051 phase 1 and the altChunk work the text extraction WD 0.23 to 0.25 asks of
docx4j; ahead of CR-051 phase 2. To be raised to high if phase 0's Word check shows Word refusing
such a file rather than repairing it.**

Phase 0 (21 October 2026) found the exposure wider than this section weighed. An edited document loses
a private or alias prefix without any move (§1, case 1'), so "a document saved as it was loaded is
fine" holds only for a part docx4j never read. Office's unmoved files still do not trigger it: their
`Requires` prefixes are the table's. The Word check decides between medium and high.

**Word check (21 October 2026): a repair.** Word 365 offers to repair every file with an undeclared
`Requires` prefix (`c52-1` to `c52-4`, docx4j's own save among them), and the repair succeeds. By this
section's rule the priority stays medium. Against that, the prompt now follows from any edit, not
only from a merge, and Word's own chartex charts are exposed when copied.

**Phase 1 (21 October 2026) closes the exposure:** docx4j's saves of the same cases open cleanly (§5).

- **For a higher place:** the exposure is wider than the source-only case. Content that moves between
  packages loses the declaration (cases 3 and 4), and Word's own chartex charts carry a `Requires`
  prefix their content does not use, so a docx4j merge of a document with a chartex chart is the
  likely real-world trigger. The result is a file Word may refuse.
- **For a lower place:** a document saved as it was loaded is fine (case 1), which is most of
  docx4j's traffic. Office's unmoved files never trigger it (0 of 341). The common `Requires` prefixes
  survive a move (case 4c). Nobody has reported it, and docx4j-created charts are already covered.
- **Against the open work:** CR-051 phase 1 is in progress and changes the same load path. The
  altChunk asks are conformance rulings Jason has made. CR-051 phase 2 (row and cell level in
  `preserve` mode) has a narrower exposure than this.

## 8. Sources

- The request from docx4j-generated-objects-ts, 2026-10-20, and objects CR-011 (3db3613; corrected
  to these measurements in 6a6d57e).
- CR-023, CR-024 (Word's and Excel's repair of an undeclared Ignorable prefix), CR-046 (the per-Choice
  `Requires` record), CR-049 (the single marshal contract), CR-039 (`ChartHosting`), CR-051 (`word`
  mode's Ignorable additions).
- The scratch drivers: `SourcePrefixes.java`, `MoveCopy.java` (session scratchpad, 2026-10-20); phase
  0's `Cases.java`, `Mech.java`, `Mech2.java`, `Readings.java`, `mccheck.py` (the lxml check) and
  `build_round.py` (the Word check), archived with the session's scratch, 2026-10-21.
- core-ts check 50 (core-ts CR-010 §14.9) and objects CR-012 (the Choice placement from 869b725; b03ccb5
  Jason's once-on-the-root confirmation): D4 (c)'s rule as the ports implement it.

## 9. Phase 1 record, 21 October 2026

Built on Fable (CLAUDE.md: `XmlUtils`, `JaxbXmlPart` and the io3 save path are its territory). The
design points the phase 1 brief left open, as decided and measured:

- **How to write a declaration on a specific `mc:Choice`.** JAXB's prefix mapper pre-declares on the
  root only, so a marshal with a Choice declaration goes through an `XMLStreamWriter`: a
  `Marshaller.Listener` is told of each Choice in `beforeMarshal`, and the existing
  `XMLStreamWriterWrapper` (the property path `MarshalToOutputStreamViaXMLStreamWriter`, off by
  default since 6.1.0) writes the declarations right after the element's start, so they come ahead of
  `Requires` as Word writes them. Probed first (scratch `XswProbe`): both runtimes fire the listener
  before the Choice's start tag; the RI's output is `<mc:Choice xmlns:zz9=".." Requires="zz9">`; an
  `XMLStreamWriter` over a `DOMResult` serves the DOM-output paths (`marshal(Node)`,
  `marshaltoW3CDomDocument`, the binder's DOM, Flat OPC) and round-trips; the root carries the same 74
  declarations on either path. A marshal with no Choice declaration keeps its usual path (the RI's
  fast `OutputStream` writer with `MainDocumentPartFilterOutputStream`), so the common case, every
  Office-produced file included, is byte-for-byte what it was apart from the plan's root prefixes.
  The stream-writer route writes `<?xml version="1.0" encoding="UTF-8"?>` without `standalone="yes"`
  and indents through `XMLStreamWriterWrapperIndenting`.
- **Where the record lives, so `deepCopy` carries it.** Three candidates: a field on the generated
  Choice through `<inheritance:extends>` (rejected: the copy plugin runs before `-Xinheritance`, so a
  generated `copyTo` never calls `super.copyTo`; `CTSdtContentRun` shows it), a lockstep walk of source
  and copy after every `deepCopy` (rejected: a walk on a hot path), and a copy hook. Taken: docx4j-copy's
  `CopyUtils` gets a `CopyListener` told of each copy it makes (the members of untyped lists, which is
  where `mc:AlternateContent` lives), and `XmlUtils.deepCopyFast` reports its top-level copy;
  docx4j-core registers the listener at `Context`'s start-up. Not covered: the seven generated classes
  with a typed single-valued `AlternateContent` field (`Numbering`, `CTChartSpace`, the three
  spreadsheet drawing anchors, `CTControlList`, `Workbook`), whose `copyTo` calls `copy()` directly;
  their Choices carry Office's own prefixes, which the table gives, so nothing is lost. A plugin change
  routing typed copies through `CopyUtils` would close it.
- **The root's bindings on MOXy.** The RI fires the root's `afterUnmarshal` at the root's end tag, with
  its declarations in scope; MOXy fires it at `END_DOCUMENT`, where the namespace context is empty
  (measured, scratch `RootProbe`). So the reader wrapper `McBranchSdt.wrap` keeps the root element's
  own declarations from its start tag, and the record is made from those on either runtime.
- **The plan's cost.** `McDeclarations.plan` walks the tree once per marshal (`TraversalUtil`,
  `McMode.ALL`). On a 3.6 MB main document part of 40,835 objects it takes 3.8 ms of an 87 ms marshal
  (4.3%); on the probe 0.6 of 13.7 ms. A first measurement of 36% was `TraversalUtil` writing DEBUG
  log lines under the scratch classpath's default logging; two of its unconditional `log.debug` string
  concatenations are now guarded, and its reflection fallback (the first `List`-returning method of an
  unknown class) is cached per class, which every walk in docx4j gains from.
- **D5 (a) on the DOM paths.** `XmlUtils.unmarshalString` and `unmarshal(Node)` parse a DOM, so the
  root's Ignorable bindings are read from the document element and each Choice's from its
  `mc:Choice` element, the Choices paired with the DOM's elements in completion order (document order
  of end tags) and the pairing checked by `Requires`, abandoned where it does not hold (a Choice inside
  content JAXB kept as a DOM element has no object). The mc-preprocessor retry paths (an XSLT into a
  `JAXBResult`) record nothing: D5 (b) there.
- **The clash (D3).** Declaring a table prefix on a Choice with another binding while the Choice's
  content uses the table's namespace would rebind that content (the stream writer does not consult
  JAXB's namespace context), so the plan checks the Choice's subtree for an element in that namespace
  (JAXB class annotations, `JAXBElement` names, DOM elements and their attributes) and, on a hit, leaves
  the prefix to the table with a warning. Tested with `x` (Excel's VML binding against SpreadsheetML).
- **Two clauses of §4.4 not implemented, as they do not arise here:** "or the tree uses it there" (a
  source-only binding the root keeps because its own content uses it: JAXB-bound content uses only
  namespaces the table knows, and a DOM element kept in lax content declares its own where it is), and
  the `mc:AlternateContent` element's own `mc:Ignorable` and `mc:MustUnderstand`, outside D1 (b)'s
  scope (a Choice's and the root's); the test's check walks every element, so either would show.
- **Not changed:** `JaxbXmlPart.addMcChoiceNamespace` and the subclasses' `setMceIgnorable` overrides
  still feed the plan's root prefixes; `ChartHosting` and `SlidePart` need no change. The
  `docx4j.jaxb.marshal.canonicalize` path declares the plan's root pairs on the DOM and passes every
  declared prefix as the canonicalizer's inclusive list, so a Choice's declaration survives exclusive
  c14n. `FlatOpcXmlCreator` no longer concatenates a null `getMceIgnorable()` into the token `null`.
- **Review** (a `/code-review` pass on the diff, 21 October 2026, ten findings; taken unless said):
  a Choice inside a content control directly in a branch (CR-051 §11.3 binds the control from its
  DOM element after the read) got no record on any path, since the reader had moved on; now the
  listeners keep a Choice they could not record and `McBranchSdt.bind` records those from the
  control's element, and the DOM paths record before the rebind, skipping the renamed controls
  whose Choices JAXB has not bound (which had made the positional pairing fail). The stream
  writer dropped a marshaller's redeclaration of a prefix the plan had put on the Choice, URI
  unchecked; now a different binding displaces the plan's with a warning (D3: the content's
  binding stands), and the plan's own clash check also tests the root's binding of the prefix, not
  the table's only. The copy listener touched the record for every object copied; now only for an
  `mc:AlternateContent` or a Choice, `deepCopy`'s top-level object aside. The binder path asked the
  binder for a wrapped root; unwrapped. One `XMLOutputFactory` is shared; the stream route writes
  JAXB's own XML declaration (`standalone="yes"`) and marshals the rest as a fragment, so a
  marshal's header no longer depends on whether a Choice needed a declaration (its indentation
  still comes from the wrapper); `McSelection` and the mapper use the one prefix tokenizer;
  `McDeclarations.marshal`'s javadoc matches the code. Not taken: the plan's walk on every
  `marshaltoString` (measured above at 4% of a marshal; the canonicalizing branch plans twice, off
  by default). Tests added for the nested control on the three paths, the root-binding clash and
  the writer's displacement.
- **Code:** `McSourceBindings`, `McDeclarations` (new, `org.docx4j.jaxb`); `McSelection` delegates its
  record; `Docx4jUnmarshallerListener`, `McBranchSdt` (the `Collector` and the reader wrapper),
  `JaxbXmlPartXPathAware.afterBinderUnmarshal` record; `JaxbXmlPart.marshal` (both), `XmlUtils`' six
  marshal methods (one helper behind them) and `FlatOpcXmlCreator.marshalBytes` plan;
  `NamespacePrefixMapperUtils.getPreDeclaredNamespaceUris2` adds the plan's root pairs;
  `Docx4jMarshallerListener` and `XMLStreamWriterWrapper` write a Choice's declarations;
  `CopyUtils` (docx4j-copy) gains the listener. Tests: `McSourcePrefixesTest` (24).

