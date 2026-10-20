# CR-052: a save declares the `mc` prefixes the source bound, wherever the content goes

Status: ACCEPTED 2026-10-20 (Jason: "accept the recommendations and commit the CRs"), D1 to D5
as recommended, D4 as (c) after core-ts's count of Word's saves. Phase 0 DONE 21 October 2026 (Jason:
"start CR-052 phase 0, with the Word round (4) folded into CR-052's own Word check"): §1 corrected and the
mechanism found (§1.1), the tests in core-tests, D4 (c)'s rule as the ports implement it (§4), and Word's
check: Word repairs a file with an undeclared `Requires` prefix, so the priority stays medium (§5, §7).
Phase 1 next.
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

### Phase 2: hand-offs

objects-ts (its CR-011 is the same rule; the resolution order agrees; it waits on jsonix 3.5.0,
jsonix CR-008, so the hand-off points at it as it stands), core-ts (it reproduced the editor's case),
python (CR-049's table records its CR-011 Q11 as the same family). Acceptance: each acknowledged.

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
- core-ts check 50 (core-ts CR-010 §14.9) and objects CR-012 (b03ccb5): D4 (c)'s rule as the ports
  implement it.
