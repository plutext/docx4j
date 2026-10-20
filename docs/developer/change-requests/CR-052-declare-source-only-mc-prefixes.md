# CR-052: a save declares the `mc` prefixes the source bound, wherever the content goes

Status: PROPOSED 2026-10-20. Requested by the docx4j-generated-objects-ts session at Jason's word
("send the CR request, and ask it for its opinion on priority, to be written into the CR"); the same
defect is objects CR-011 (`docs/change-requests/CR-011-declare-source-only-prefixes.md` there, 3db3613).
Drafted by Claude Opus 5.5 (session docx4j-25), with the request's points measured first (§1). Phases
in §5; decisions in §6, for Jason; priority in §7, as asked. Touches the marshal path (`XmlUtils`,
`JaxbXmlPart`), which CLAUDE.md lists among the widely-shared code to change on Fable.

## 0. Summary

An `mc:Ignorable`, `mc:Choice/@Requires`, or a Choice's own `mc:Ignorable` or `mc:MustUnderstand`
names namespace prefixes, so each prefix it names must be declared where the attribute is. docx4j
declares them at marshal from its static prefix table, plus what the part recorded at unmarshal. That
covers a part saved as it was loaded. It does not cover content that moves: a Choice moved or copied
into another part or package, or built in code, loses the declaration of a prefix that the table
lacks, or binds to another namespace, or that nothing else in the destination declares. The saved part
then names a prefix it never declares.

Office's own content is not immune once it moves: Word writes a chartex chart's Choice with
`Requires="cx1"` (or `cx2` and up), and the branch's content uses the `cx` namespace, not `cx1`'s, so a
merge that copies such a chart is exposed (§2, inferred; phase 0 confirms it). Whether Word refuses or
repairs the result has not been checked.

The fix: each Choice carries the bindings its prefixes had in the source, across a move and a copy;
every marshal declares each `mc`-named prefix from the content's own record first, then the table.

## 1. What docx4j does today (measured 2026-10-20 on VERSION_17_3_4 at cf1abf001)

Scratch drivers against the installed 17.3.4-SNAPSHOT, with an lxml check of each saved part
(every `Requires` prefix resolved against the `mc:Choice`'s in-scope namespaces). The request's seven
points are its reading of the code; these are the measurements behind §2.

| Case | Result |
|---|---|
| 1. `extraction-probe.docx` (core-tests' `text-extraction/`, 4a89a5967) loaded and saved | **declared.** All 12 `Requires` prefixes resolve, including the two `zz9`-only Choices, whose content does not use `zz9`. docx4j writes `xmlns:zz9` (and `xmlns:shape2010`) on each `mc:AlternateContent`. |
| 2a. A root `mc:Ignorable="zz9"` with `xmlns:zz9` on the root, the tree set from code (`XmlUtils.unmarshalString`, `setJaxbElement`) and saved | **undeclared:** `mc:Ignorable="zz9"`, no `xmlns:zz9` anywhere. |
| 2b. The same document loaded from its package and saved | **declared** on the root. |
| 3. A Choice built in code, `Requires="cx1"` (the fragment declared `xmlns:cx1`), added to a new package and saved | **undeclared,** though the table knows `cx1` (`NamespacePrefixMappings`:572, :968). |
| 4a. The two `zz9`-only paragraphs of case 1 moved (same objects) into a new package | **undeclared.** |
| 4b. The same, deep-copied (`XmlUtils.deepCopy`) | **undeclared.** |
| 4c. Every paragraph of case 1 holding a Choice deep-copied into a new package | `wps` 6 Choices, `w14` 2: declared. `zz9` 3 Choices and `shape2010` 1: **undeclared.** |

So the request's point 1 (Requires from the source) already holds for a part saved as loaded, but
not once the content moves (points 3 and 4 hold). Its point 2 (the root's Ignorable) holds for a tree
set from code, not for a loaded part. How the loaded part keeps its bindings, which of the two
unmarshal-time records (the CR-046 per-Choice `McSelection.REQUIRES_NAMESPACES`, or the part's
`addMcChoiceNamespace` string) the marshal reads, and why case 4a loses it, is phase 0's first job: it
decides whether the record already exists per Choice and only the marshal and the copy miss it.

## 2. What it affects, and what it does not

- **Affected:** content moved or copied between parts or packages (merges, assembling from
  fragments, `XmlUtils.deepCopy`), and trees built or set in code, where a Choice's `Requires` prefix,
  or the root's Ignorable prefix, is:
  - private to its producer (`zz9`), or a second prefix for a known namespace (`shape2010` for `wps`);
  - or known to the table but neither predeclared in the destination nor used by the branch's content.
    Word's chartex charts are the Office case: `Requires="cx1"` over `cx` content (inferred from cases 3
    and 4; phase 0 measures a chart copied between documents).
- **Not affected:** a part saved as it was loaded (case 1); the prefixes the destination predeclares
  or the branch's content uses (`wps`, `w14`, case 4c); `ChartHosting` (CR-039), which registers its
  own `Requires` through `addMcChoiceNamespace`; CR-051's `word` mode, which adds to the root's Ignorable
  only the prefixes of understood Choices, each with a table prefix.
- **Office's own files, unmoved:** objects measured 0 of 341 parts with a source-only prefix.

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
   matters for `getNamespaceURIStatic`'s hard-coded `x` too. Where the tree itself uses the prefix for
   another namespace, the tree's declaration stands and a warning is logged.
3. **The bindings travel with the Choice** (D2): the per-Choice record (CR-046's, or one phase 0 finds
   is the real carrier) is read at marshal from the Choice itself, not from the part it was loaded
   into, and `XmlUtils.deepCopy` carries it to the copy. A root's Ignorable bindings are recorded on
   the part at unmarshal, as case 2b shows they are, and also by `XmlUtils`' unmarshal methods where
   docx4j's listener is set (D5).
4. **Where a `Requires` prefix is declared** (D4): on the `mc:AlternateContent`, as docx4j writes it
   for a loaded part today (case 1), so the declaration moves with the element; the root's Ignorable
   prefixes on the root, which Word requires.
5. **Nothing in the JAXB runtime:** docx4j already has the namespace context at each element at
   unmarshal; objects needed jsonix CR-008 for the same thing.

## 5. Phases

### Phase 0: tests, the mechanism, and Word

- §1's cases as JUnit tests in core-tests, each asserting what it measured (the failing ones marked
  as the defect), with `extraction-probe.docx` and a chartex document as fixtures.
- Find how case 1 keeps its bindings and why case 4a loses them; record it here.
- A Word check for Jason: a document whose chartex chart's Choice says `Requires="cx1"` with no
  declaration in scope, and one with `Requires="zz9"` undeclared. Does Word refuse, repair or open it?
  The answer sets §7's priority.

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

- **D1. Scope.** (a) `Requires` only; (b) `Requires`, the root's Ignorable, and a Choice's own Ignorable
  and MustUnderstand. **Recommended: (b)**, since docx4j already records all four at unmarshal, and
  objects covers only the first two.
- **D2. Copies.** (a) Carry the record across `XmlUtils.deepCopy`; (b) accept the loss and document
  it. **Recommended: (a).** Copying is how merges move content, and case 4b is the common way in.
- **D3. Resolution order.** Source, then table, then aliases, the tree's own conflicting declaration
  winning with a warning. **Recommended,** as objects decided it.
- **D4. Where a `Requires` prefix is declared.** (a) On the `mc:AlternateContent`; (b) on the root.
  **Recommended: (a):** what docx4j does for a loaded part now, local, and it moves with the element.
  A local declaration also handles one prefix bound two ways in one part, which a root declaration can
  only warn about. objects CR-011 declares on the root, under its CR-001 convention; its Consumers
  section records the difference for Jason. If the ports are to agree, objects would change for the
  prefixes the tree does not use (generated-objects-ts, 2026-10-20).
- **D5. A tree set from code.** (a) `XmlUtils`' unmarshal methods record the root's and the Choices'
  bindings when docx4j's listener is set; (b) document that such a tree's private prefixes are the
  caller's to declare. **Recommended: (a)** where it costs nothing more than the listener already
  does; otherwise (b).

## 7. Priority (docx4j-25's view, as the request asked)

**Medium: after CR-051 phase 1 and the altChunk work the text extraction WD 0.23 to 0.25 asks of
docx4j; ahead of CR-051 phase 2. To be raised to high if phase 0's Word check shows Word refusing
such a file rather than repairing it.**

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
- The scratch drivers: `SourcePrefixes.java`, `MoveCopy.java` (session scratchpad, 2026-10-20).
