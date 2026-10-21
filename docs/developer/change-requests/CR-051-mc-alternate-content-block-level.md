# CR-051: `mc:AlternateContent` at block level: read as Word reads it on load, or kept whole on request (body, cells, rows, headers, notes, text boxes, content controls)

Status: ACCEPTED 2026-10-20 (Jason: "accept recommendations for CR-051's D1 to D5"), to implement as
recommended. Phase 0 done 2026-10-20 (the walker audit, §10); D6, the exporters' copy, decided (a)
the same day (Jason: "D6 (a)"). Five of the six defects of today the audit found were fixed the same
day (§10.5, c4894c5b0); the sixth went with phase 1. Phase 1 done 2026-10-20 (Jason: "make the fixes,
then phase 1"): the listener fix 6687cc4b8, the rest 1cff2cd5c (§11). Phase 3 (the hand-offs) done
2026-10-21, each receiver acknowledged (§5). Phase 2 (row and cell level in `preserve` mode) done
2026-10-21 (Jason: "do CR-051 phase 2"; §12; the Word check of the `preserve` saves passed); its
hand-offs go with it.
D5 revised 2026-10-21 (Jason): `preserve` becomes the default at 17.4.0, after phase 2; 17.3.x keeps
`word` (D5, "Revised"); the switch is recorded in §12.4 for the 17.4.0 branch to make. Proposed 2026-10-20, drafted at Jason's request ("write
the cr to keep body-level mc through load. also check whether we do/should keep mc in sdt content")
by Claude Opus 5.5 (session docx4j-25), after the portfolio session's measurement of Jason's
`mc-fun` probes (paper §7.8).
Revised the same day:
- with Word's reading and saving measured (§5, phase 0);
- around a load-time mode (§3.2), at Jason's suggestion: "a docx4j property which by default mimics
  Word's behaviour, but can be switched to preserve. we can't rely on mc preprocessing xslt step to
  manage this, since it is only triggered by jaxb".

Reviewed the same day by the docx4j-64 session (Claude Fable 5.1) at Jason's request: nine findings,
one a defect today. All are taken (§9). It reviewed D6 after phase 0, at Jason's request: (a), with
three must-fix points, all taken (§9).

Three phases and hand-offs (§5); decisions in §6, D1 to D5 decided by Jason on 20 October 2026 as
recommended, D5 being his own proposal; D6, from phase 0's audit (§10), decided (a) the same day.
It changes
the generated object model and the load path, which CLAUDE.md lists among the widely-shared code to
change on Fable.

## 0. Summary

docx4j keeps an `mc:AlternateContent` through load only where its schema admits the element: in
`w:r` (3.3.8) and, since CR-021 phase 2 (17.2.0), in paragraph content (`EG_PContent`) and
`w:numPicBullet`. Anywhere else in WordprocessingML, JAXB rejects it, and the part goes through
`mc-preprocessor.xslt`. That resolves the element to the Choice `docx4j.jaxb.mc.preferChoice` names
(none by default), else the Fallback, else nothing. CR-021 had proposed admitting it at block level
(its §3.1), and dropped that because its survey of 835 documents found none there (§8.1: "nothing
speculative").

Jason's probes supply the evidence the survey lacked. Every desktop Word reads a block-level
`mc:AlternateContent` by ISO's rule (paper §7.8, 20 October 2026):

- **original order** (w14 first): Word 365, Word for Mac, Word 2013 and Word 2010 all show `w14`;
- **newest first:** Word 365 and Word for Mac show `w16sdtfl`, Word 2013 `w15` and Word 2010 `w14`.

Word 365 reads it the same way in a cell, a table, a row and every kind of content control (§1).
docx4j shows "fallback" in both orders; at run and paragraph level it agrees with Word 365 in both
orders. The block-level loss is not confined to text:

- **Text** (WD REQ-302): the Fallback where Word shows a Choice, and no `mce.branches_differ`
  warning, since the branches are gone before extraction.
- **The inspector** (CR-050): no `mce.branch` finding; the Choices are invisible to it too.
- **Save:** a docx4j load and save writes the Fallback alone (§1). After it, Word shows "fallback"
  where it showed `w16sdtfl`, so docx4j has changed the document.
- **No Fallback:** the preprocessor drops the whole element, every Choice with it. That loses
  content, against REQ-305, which requires such an element to be kept whole.
- **Collateral:** whenever any block-level element sends a part through the preprocessor, it also
  resolves `mc:AlternateContent` that JAXB would have kept, in a run-level content control, a
  hyperlink, a simple field (§1). That is a defect today, whatever is decided here.

Word itself reads block-level alternate content and never writes it. Its save resolves the element to
the branch it read, and writes again, at run level, only what needs alternatives there: a text box
(§5, phase 0).

The proposal (§3):

- **The schema admits the element** in block content, and (D2) in row and cell content, so JAXB
  builds it wherever Word reads it.
- **A load-time mode decides what the tree holds** (§3.2; the property and its default are D5):
  - **`word`** (the default): right after a part is unmarshalled, each block-, row- or cell-level
    element is replaced by the branch Word reads. docx4j's tree then holds what Word's holds, and a
    save writes what Word's save writes, text boxes apart (§3.5). The element taken out is kept on
    the part, so the inspector and the text model still see every branch.
  - **`preserve`**: the element stays whole in the tree, consumers choose a branch at use (CR-021's
    rule), and a save writes every branch.
- **Run and paragraph level are unchanged** in both modes: kept whole, as since 3.3.8 and 17.2.0.
- **The preprocessor no longer chooses** a branch for an admitted element: its keep rule follows the
  schema (fixing the collateral resolution), and an element it must still resolve is never dropped.

## 1. What docx4j does today (measured 2026-10-20 on VERSION_17_3_4 at 86449c013)

The probes are Jason's three newest-first files and seven built by this session from his
`mc-block-newest-first.docx`: the same ten Choices newest first, every other part unchanged. They are
on the share beside his (`Office 2016/mc-fun/`), for phase 0's Word check. "Kept" means the JAXB tree
holds the element after load; text is `Texts.of` with the defaults. The Word 365 column is the branch
Word kept when Jason saved each probe (`<name>-word365.docx`, 20 October 2026), or paper §7.8's
reading where marked.

| Probe | `mc:AlternateContent` in | Its group in `wml.xsd` | Kept | Text | Choices after load and save | Word 365 |
|---|---|---|---|---|---|---|
| `mc-text-newest-first` | `w:r` (each branch a `w:t`) | `EG_RunInnerContent` | yes | `I prefer w16sdtfl` | 10 of 10 | w16sdtfl (§7.8) |
| `mc-run-newest-first` | `w:p` (each branch a `w:r`) | `EG_PContent` | yes | `I prefer w16sdtfl` | 10 of 10 | w16sdtfl (§7.8) |
| `mc-sdt-run-newest-first` | a run-level `w:sdtContent` | `EG_PContent` (`CT_SdtContentRun`) | yes | `I prefer w16sdtfl` | 10 of 10 | w16sdtfl |
| `mc-block-newest-first` | `w:body` (each branch a `w:p`) | `EG_BlockLevelElts` | no | `fallback` | 0 of 10 | w16sdtfl (§7.8) |
| `mc-sdt-block-newest-first` | a block-level `w:sdtContent` | `EG_BlockLevelElts` (`CT_SdtContentBlock`) | no | `fallback` | 0 of 10 | w16sdtfl |
| `mc-tc-block-newest-first` | `w:tc` (each branch a `w:p`) | `EG_BlockLevelElts` (`CT_Tc`) | no | `I prefer` / `fallback` | not run | w16sdtfl |
| `mc-row-newest-first` | `w:tbl` (each branch a `w:tr`) | `EG_ContentRowContent` | no | `row` / `fallback` | not run | w16sdtfl |
| `mc-sdt-row-newest-first` | a row-level `w:sdtContent` | `EG_ContentRowContent` (`CT_SdtContentRow`) | no | `row` / `fallback` | not run | w16sdtfl |
| `mc-cell-newest-first` | `w:tr` (each branch a `w:tc`) | `EG_ContentCellContent` | no | `I prefer` / `fallback` | not run | w16sdtfl |
| `mc-sdt-cell-newest-first` | a cell-level `w:sdtContent` | `EG_ContentCellContent` (`CT_SdtContentCell`) | no | `I prefer` / `fallback` | not run | w16sdtfl |
| `mc-block-nofallback` | `w:body`, ten Choices and no Fallback | `EG_BlockLevelElts` | no: dropped whole | nothing (the element's text is gone) | 0 of 10 | w16sdtfl (the first Choice) |

Where it is not kept, the load log says so, naming the parent. For example: "mc:AlternateContent in
w:body is not kept by the preprocessor (CR-021/CR-024: kept in w:r, w:p, w:numPicBullet, ...);
selecting its Fallback w:p". With no Fallback the log says "Missing mc:Fallback! Dropping the
mc:AlternateContent entirely." The text model raises no warning in either case: it never sees the
element.

**The collateral resolution.** The preprocessor is reached when JAXB rejects anything in the part,
and then it transforms the whole part. Its keep test names three WordprocessingML parents
(`parent::w:r or parent::w:p or parent::w:numPicBullet`), but the schema admits the element in every
member of `EG_PContent`'s family: `w:p`, a run-level `w:sdtContent`, `w:hyperlink`, `w:fldSimple`,
`w:smartTag`, a run-level `w:customXml`, `w:dir` and `w:bdo`. A probe (`mc-mixed`) holds one element
in each of a run-level content control, a hyperlink and a paragraph, each with a `w14` Choice and a
Fallback:

- **alone,** all three are kept, and the text is `SDTRUN_W14`, `HLINK_W14` and `INP_W14`;
- **with a fourth element added in the body,** the content control's and the hyperlink's are resolved
  too. The text becomes `SDTRUN_FALLBACK`, `HLINK_FALLBACK`, `INP_W14`, and save keeps 1 of the 4
  elements. Word 365 kept the `w14` branch of all four, the body's included (`BODY_W14`).

**A second defect today, on the binder path** (from the code; found in docx4j-64's review, §9).
`Docx4jUnmarshallerListener` does the unmarshal-time bookkeeping:
- each Choice's `Requires` prefixes (`addMcChoiceNamespace`) and namespaces;
- each drawing's id (`DrawingPropsIdTracker`).

It is set on the StAX path and on two of `JaxbXmlPartXPathAware`'s binder paths (lines 475 and 731,
through `BinderListenerUtils`). On the third it is commented out (line 627, "TODO: binder doesn't
support listener"). That third path is the one a part takes when StAX rejects it and it goes through
the preprocessor into a binder. Every part that extends `JaxbXmlPartXPathAware` takes it: the
`JaxbXmlPartAltChunkHost` parts (the main document, headers and footers), footnotes, endnotes,
comments, settings, styles, numbering, and others. So such a part, rejected for any reason, loses
that bookkeeping. Under this CR it would also lose the records §3.2 splices from, and `word` mode
would do nothing there.

**So, for content controls (Jason's question):**

- **Run-level content:** docx4j keeps the element, unless something else in the part sends it
  through the preprocessor (the collateral defect above).
- **Block-level content:** not kept today. Its content model is the body's (`EG_BlockLevelElts`), so
  admitting the body admits it. §3 reads it as Word does, or keeps it whole (D5).
- **Row and cell content:** not kept. Their content models are `w:tbl`'s and `w:tr`'s, which is D2.

Should they be kept? Wherever Word reads the element, yes, for the reasons of §0. Word 365 reads it at
every level measured: run, block, row and cell content controls, and `w:tc`, `w:tbl` and `w:tr`
directly. Each time it kept the first understood Choice, `w16sdtfl`, the newest, which docx4j's
understood set also covers. Older Word versions were not run on the new probes.

## 2. What the specifications say

- **[ISO29500-3]:** markup compatibility is processed before the host vocabulary's schema applies, so
  `mc:AlternateContent` may appear wherever the producer puts it. §9.3 selects the first understood
  Choice, else the Fallback.
- **WD REQ-301 and REQ-302** apply at every level; neither is confined to runs.
- **WD REQ-305:** with no understood Choice and no Fallback, a reader takes the first Choice, and an
  implementation that resolves alternate content on load "MUST keep such an element whole rather than
  promote the branch". The dropping branch satisfies neither half. `word` mode resolves on load, so it
  keeps such an element whole (§3.2).
- **docx4j's own rule (CR-021 §2, item 1):** "Keep the element wherever it occurs ... Consumers choose
  at use". `preserve` mode applies it where CR-021's survey found no occurrence. `word` mode departs
  from it at block, row and cell level, for the reasons of D5.

## 3. Design

### 3.1 The schema

In `xsd/wml/wml.xsd`, add `<xsd:element ref="mc:AlternateContent" minOccurs="0"
maxOccurs="unbounded"/>` to `EG_ContentBlockContent`, with a comment as CR-021 gave `EG_PContent`'s,
and a line in the file's header history. Through `EG_BlockLevelChunkElts` and `EG_BlockLevelElts`,
that one admission reaches every block container:

| Container | Its type |
|---|---|
| `w:body` | `CT_Body` |
| `w:hdr`, `w:ftr` | `CT_HdrFtr` |
| `w:tc` | `CT_Tc` |
| `w:footnote`, `w:endnote` | `CT_FtnEdn` |
| `w:comment` | an extension of `CT_TrackChange` |
| `w:txbxContent` | `CT_TxbxContent` |
| a block-level `w:sdtContent` | `CT_SdtContentBlock` |
| a block-level `w:customXml` | `CT_CustomXmlBlock` |
| a glossary entry's body (`w:docPartBody`) | `CT_Body` |

With D2 (a), the same element goes into `EG_ContentRowContent`:
- `w:tbl`;
- a row-level `w:sdtContent` (`CT_SdtContentRow`) and `w:customXml` (`CT_CustomXmlRow`).

It also goes into `EG_ContentCellContent`:
- `w:tr`;
- a cell-level `w:sdtContent` (`CT_SdtContentCell`) and `w:customXml` (`CT_CustomXmlCell`).

The content lists are `List<Object>` already, so no signature changes. `docx4j-generated-objects`
regenerates from `xsd/ROOT.xsd`, and objects-ts regenerates from the same file (CR-018's and
CR-021's hand-off). The admission is needed in both modes: `word` mode resolves an element JAXB has
built, and `preserve` mode keeps it.

### 3.2 The load-time mode

**The property** (D5): `docx4j.jaxb.mc.blockLevel`, with two values:

- `word` (the default): each element is resolved as Word reads it;
- `preserve`: each element is kept whole.

**A load option, and a package-level override:** `Docx4jProperties` is one setting for the JVM, and one
process may hold an inspector and an editor that want different modes. So a package carries its own
setting, read when each part is unmarshalled. It is given at load, in phase 1:
- a setter on `Load3`, and an overload of `WordprocessingMLPackage.load` and `Docx4J.load`, which give
  no hook today. The loader sets it on the package, as it sets `OpcPackage.setWasStrict`;
- parts unmarshal lazily (`JaxbXmlPart.getJaxbElement`, from the part store), so a package-level
  setter made after load also works, for a part not yet touched. It stays for packages built another
  way.

The tools that need `preserve` (the anonymiser, diffx, an edit tool that saves) need it reliably, so
they give it at load.

**Scope:** elements whose parent is a block container (§3.1's table) and, with D2, a row or cell
container (`w:tbl`, `w:tr`, a row-level or cell-level `w:sdtContent` or `w:customXml`). Elements
in run and paragraph content stay whole in both modes, as they have since 3.3.8 and 17.2.0:

- Word keeps a run-level text box's authored Fallback on save (`mc-txbx-run`, phase 0);
- elsewhere Word regenerates a VML Fallback from the Choice, which docx4j cannot do;
- so resolving there would lose Fallbacks that Word keeps.

**Which branch:** `McSelection.select(McPurpose.TEXT, ac)`. That is ISO's rule over docx4j's
published understood set (CR-046), matching each Choice's `Requires` by namespace URI where the
unmarshal recorded the namespaces in scope. It agreed with Word 365 on every namespace measured. It
is not `docx4j.jaxb.mc.preferChoice`, which is the exporters' drawing preference for the elements
that stay whole.

**The mechanism**, in four steps:

1. **Collect.** `Docx4jUnmarshallerListener.afterUnmarshal(target, parent)` records each
   `AlternateContent` whose parent is in scope, on the part. The record holds the element alone,
   since step 2 reads the parent live. The listener must be on every unmarshal path:
   - the StAX path (`JaxbXmlPart.getConfiguredUnmarshaller`);
   - the DOM the preprocessor produces;
   - the binder paths (`JaxbXmlPartXPathAware`, through `BinderListenerUtils`), including the one where
     it is commented out today (§1's binder-path defect, phase 1's first item).

   It already records each Choice's `Requires` namespaces there (CR-046). Nothing is edited inside the
   callback: it fires child first, while the parent's content list may still be filling.
2. **Apply,** when `unmarshal` returns (`JaxbXmlPart.unmarshal(InputStream)`, `unmarshal(Element)`
   and the binder variant).
   - **In `word` mode:** each recorded element is replaced, at its index in its parent's content
     list, by the content of the branch the rule selects, re-parented.
   - **The parent is read live,** as `((Child) ac).getParent()` when the record is applied, not as it
     was at unmarshal. `ArrayListDocx4j` re-parents what is added or set
     (`ArrayListDocx4j.java:112-163`), so content moved by an earlier splice already answers with its
     new parent.
   - **Nested elements:** the records arrive innermost first, so they are applied in reverse,
     outermost first.
     - An element inside a kept branch then has a live parent in the tree, and is applied there.
     - An element inside a discarded branch is skipped: going up its parents, the first
       `AlternateContent` it meets is one already taken out. It stays whole inside that element's
       discarded branch, in the record, so the inspector still sees the inner element's branches.
     - Only outermost elements and elements in kept content enter the record.
   - **In `preserve` mode,** nothing is applied.
   - **Cost:** only the records. A part without alternate content pays nothing, and no tree walk is
     needed.
3. **The binder.** After the splice, `binder.updateXML(jaxbElement)`, so that the DOM an XPath query
   reads (`getJAXBNodesViaXPath`) matches the tree. It is slow, but the binder path is a DOM path
   already, and rare.
4. **What the splice also does:**
   - **REQ-305:** the splice tests the condition itself, before selecting: no Choice understood and no
     Fallback.
     - Such an element stays whole, in `word` mode too. Readers apply REQ-305's first-Choice rule at
       use, and a save writes the element as it came.
     - It cannot rely on the selection, since `McSelection.selectedBranch(McPurpose.TEXT, ac)`
       already applies REQ-305's reading rule and returns the first Choice (`McSelection.java:332-347`).
   - **An element with no branch at all** (neither a Choice nor a Fallback) shows nothing in any
     reader. In `word` mode it is removed, as Word's save would leave it out, and recorded. In
     `preserve` mode it is kept. The preprocessor drops it today.
   - **Namespace declarations, relied on:** the listener already passes every Choice's `Requires`,
     `Ignorable` and `MustUnderstand` prefixes to `addMcChoiceNamespace`, and the save declares them
     (CR-049 settled the state).
     - In `word` mode, the prefixes of discarded Choices are still declared. That is harmless, Word
       declares its whole set too, and it needs no new code.
   - **`mc:Ignorable`, new:** the root's `mc:Ignorable` value comes from the source, not from that
     bookkeeping. Promoted content can hold elements of the selected Choice's namespace
     (`w14:checkbox`, `w16du:*`), no longer under a Choice that requires it. So where the root does
     not already list the selected Choice's `Requires` prefixes, the splice adds them to its
     `mc:Ignorable`, as Word lists the namespaces of what it writes. Without them, an older Word may
     refuse a namespace it does not know.
   - **The record:** each element taken out is kept on the part with where it stood (a getter, name
     to settle: `getResolvedAlternateContent()`), so nothing is lost to inspection. Its two readers
     are phase 1 work of their own, not "as in `preserve`":
     - **CR-050's inspector** marshals the JAXB tree to a DOM (`InspectXml.marshal`) and finds branches
       by DOM element (`InspectWalk`, `iter(root, MC, "AlternateContent")`). In `word` mode it must
       marshal each recorded element too, located at the promoted content, so that its `mce.branch`
       findings carry the address they would have in `preserve` mode.
     - **The text model's** `compareBranches(ac, address)` takes an address. It must be given the
       promoted content's, to raise `mce.branches_differ` from the record.
   - **Logging:** one line per part, at INFO, with the number resolved.
   - **The binder path, optionally:** there, `binder.getXMLNode(choice)` gives each Choice's DOM element,
     so `Requires` can be matched by URI (`lookupNamespaceURI`) instead of falling back to the prefix
     table (REQ-303).

**Why after unmarshal, and not in the preprocessor:**
- once the schema admits the element, JAXB no longer rejects it, so `mc-preprocessor.xslt` never runs
  for it. The preprocessor runs only when JAXB rejects something in the part;
- it sees element names and conventional prefixes, not the namespaces in scope;
- it re-serialises the whole part.

**Alternatives considered:**
- **A StAX filter in front of JAXB.** It would work without the schema admission, but:
  - it must choose at each Choice's start tag, and REQ-305's case needs lookahead buffering;
  - the binder path parses its own DOM, so it needs a second implementation of the same rule;
  - `preserve` mode needs the admission anyway.
- **Resolving at save.** The tree would hold block-level elements by default: the compatibility cost
  of §3.6. And JAXB cannot easily drop an element during marshal, so the written XML would have to
  be filtered, or the tree deep-copied first.
- **A walk of the whole tree after unmarshal,** instead of the listener's records. It is simpler,
  but costs every part a walk; with the records, a part without alternate content pays nothing.

### 3.3 The preprocessor

- **Keep rule (D3):** `mc-preprocessor.xslt` passes the element through whole wherever the schema
  admits it. With D1 (a) and D2 (a), `w:sdtContent` and `w:customXml` admit it at every level, so the
  rule is a flat list of parents, literal to the schema:
  - `w:r`;
  - the `EG_PContent` family: `w:p`, `w:hyperlink`, `w:fldSimple`, `w:smartTag`, `w:dir`, `w:bdo`;
  - `w:numPicBullet`;
  - §3.1's block containers, `w:tbl` and `w:tr`;
  - `w:sdtContent` and `w:customXml`;
  - `mc:Choice` and `mc:Fallback`, for nested elements.

  Everything else (property containers) is resolved per D4. Only if D2 is (b) does the rule need
  levels: a `w:sdtContent` or `w:customXml` is then kept only when it is not row or cell content (its
  `w:sdt` or `w:customXml` parent not a `w:tbl` or `w:tr`).

  The rule fixes the collateral resolution of §1. It also means that a part sent through the
  preprocessor for any other reason (Strict import, a validity fix) reaches §3.2's splice with its
  elements whole, so the branch is chosen in one place on every path.
- **Not admitted, and no Fallback (D4):** an element whose parent the schema does not admit (in
  properties, which no survey has seen) is resolved to its first Choice, with the warning, instead of
  being dropped.
- **Its other jobs** (validity fixes, Strict import) are unchanged.

### 3.4 The consumers

**In `word` mode,** no block-, row- or cell-level element from a loaded part reaches a walker,
except REQ-305's, which are kept whole. Code that walks `Body.getContent()` by type therefore sees
what it always saw, with the branch Word reads in place of the Fallback.

The splice happens in `JaxbXmlPart`'s unmarshal, so content unmarshalled outside a part never passes
it, and a block-level element there stays whole in both modes. Such content includes:
- `XmlUtils.unmarshalString`, `unmarshal(InputStream)` and `unmarshallFromTemplate`;
- diffx's output. Its inputs are loaded parts, so the mode applies to them, and both must be loaded
  in the same mode (§10.4);
- an `altChunk`'s conversion;
- OpenDoPE's templates, and the trees its binding steps set after load (§10.4).

**In `preserve` mode,** a block-level element reaches every walker of block content, as CR-021 phase
2 made a paragraph-level one reach the walkers of paragraph content. CR-021's machinery applies
unchanged:

- `McSelection` chooses at use: `McPurpose.TEXT` for reading, the drawing rule for the exporters;
- `TraversalUtil`'s `McMode` gives READ walkers one branch and ALL walkers every branch;
- the Ignorable bookkeeping at unmarshal declares each Choice's prefixes on save.

Already block-aware, from reading the code:

- the text model: `Extractor.blocks` selects through `McPurpose.TEXT` and compares the branches;
- the visitor exporters: `AbstractVisitorExporterGenerator.walkJAXBElements` walks the branch
  `McSelection` chose, at any depth;
- the inspector: `InspectWalk` finds every `mc:AlternateContent` in the part's DOM.

Phase 0's audit (§10) read the rest, for `preserve` mode and for REQ-305's kept elements in either
mode:
- **Already right:** every walker that goes through `TraversalUtil`, and both XSLT exporters, meet
  the element as their mode says.
- **What fails** is code that reads a container's list by type, or edits through a block's parent,
  which inside a branch is a `Choice` or `Fallback`:
  - in the exporters' pipeline, one splice on their copy closes every site (D6, §10.2);
  - elsewhere, phase 1's list (§10.3). The field updaters and mergers throw there today.

At row and cell level, `preserve` mode also needs the table walks that read rows and cells by type
to take one branch (phase 2, §10.3). `TableModel` already does, and with D6 the table writers meet no
element. `word` mode needs none of that: the splice leaves ordinary rows and cells.

### 3.5 Save

- **`word` mode:** the tree holds Word's reading, so a save writes it, as Word's save does (phase
  0). The one difference is a text box in the selected branch.
  - Word writes it as a new run-level element, with a VML Fallback it regenerates from the Choice;
  - docx4j writes the branch as it stands: a bare `wps` drawing where the branch had one.
  - Word 2010 and later read a bare `wps` drawing; Word 2007 does not.
- **`preserve` mode:** every branch is written, and the Choices' prefixes are declared through the
  unmarshal-time bookkeeping (CR-021 §3.5).

### 3.6 Compatibility

**`word` mode (the default):**
- **What changes:** today's default already resolves block-level elements at load, but to the
  Fallback. `word` mode resolves them to Word's branch.
  - Text, PDF and HTML output change only for documents that hold block-level alternate content
    (none of CR-021's 835).
  - Content lists of loaded parts hold no `AlternateContent` at block level, save REQ-305's. Content
    unmarshalled outside a part keeps its elements whole (§3.4).
- **What doesn't:** code that walks content by type keeps working.

**`preserve` mode** is opt-in:
- code that reads `Body.getContent()` (or a cell's, header's or content control's) sees one
  `AlternateContent` where it saw the Fallback's paragraphs;
- `McSelection.selectedContent(List)` flattens a list to the branches docx4j draws.

The CHANGELOG entry names the property, the override and `selectedContent`.

## 4. Tests

- **`word` mode, the splice.** For each container in §3.1's table (and, with D2, the row and cell
  containers):
  - the Choice Word reads replaces the element at its index;
  - with no Fallback and an understood Choice, that Choice;
  - with neither (REQ-305), the element stays whole;
  - nested elements resolve outermost first;
  - the selected Choice's prefixes are in the root's `mc:Ignorable`;
  - the record holds each element taken out, with its parent and index;
  - the binder path's XPath finds the promoted paragraph, not the element;
  - the same, on the path a part takes when StAX rejects it for another reason (§1's binder-path
    defect). The probe holds w14 content where the schema does not admit it, beside a block-level
    element; the drawing ids and Choice prefixes are recorded there too;
  - an element with no branch at all is removed and recorded.
- **`word` mode, against Word.** A load and save of each of Jason's probes gives the text Word 365's
  save gave (`<name>-word365.docx`). For the `mc-txbx-*` probes, it gives the Choice paragraph, with a
  bare `wps` box where Word wrote a regenerated Fallback (§3.5).
- **`preserve` mode.** For each admitted parent:
  - an element survives load;
  - a load and save writes every Choice and the Fallback;
  - the Choices' prefixes stay declared;
  - `mc-mixed`'s four elements are all kept.
- **Both modes:**
  - `Texts.of` takes the first understood Choice at block level, in a cell, a header and a block
    content control, and raises `mce.branches_differ`;
  - with REQ-305's element, it takes the first Choice;
  - the inspector makes an `mce.branch` finding for each unselected block-level branch, and the
    findings' addresses in `word` mode equal `preserve` mode's for the same probe;
  - the load option and the package-level override win over the property.
- **The extraction probe** gains block-level cases (numbers assigned when built: CR-050 D21 also
  proposes cases), Word-checked as every probe is. The conformance suite's REQ-301 and REQ-302 rows
  then cover block level.
- **Exporters:**
  - in `word` mode, the branch Word reads is drawn;
  - in `preserve` mode, FO and HTML (visitor and XSLT) draw the Fallback by default and the Choice
    under `preferChoice`;
  - Markdown follows the text model;
  - with D6 (a), in `preserve` mode: a feature set without the step's constant (the DOCUMENT-only
    set) still gets the copied header, footer, note and comment parts and the splice, and the caller's
    package keeps every element. A section break in a branch's paragraph gives the same sections to
    the pre-pass and the real pass. Run twice, on a copy already spliced, the step changes nothing.
- **MOXy:** the load and save tests in both modes under docx4j-JAXB-MOXy.
- **Regression:**
  - the full core-tests and export-fo-tests suites;
  - the fidelity corpus's `mc-textbox-branches-*` probes, unchanged (run level).

## 5. Phases

### Phase 0: Word's reading, and the audit

- **Jason's Word check** of the seven new probes and `mc-block-nofallback`:
  - in Word 365, and where convenient Word 2013, Word 2010 and Word for Mac, which branch each level
    shows;
  - Word's save of `mc-block-newest-first` and `mc-sdt-block-newest-first`: whether it keeps the
    element or resolves it, as the extraction probe's re-save resolved every non-text-box element
    (its README).
- **The audit of §3.4,** each walker classified READ or ALL, or as a reader of block content by type
  that must learn the element.

**Acceptance:** the table of §1 gains Word's column; D1 and D2 can be decided. Met for Word 365 on
20 October 2026 (the progress below). The audit was done the same day (§10).

**Progress, 20 October 2026:**

- **Reading: done for Word 365.** Jason's saves of the paragraph probes show the branch Word kept at
  every level (§1's column).
- **Saving: the paragraph probes cannot settle it, as Jason pointed out.** A paragraph is the same at
  every capability level, so Word has no reason to keep alternatives for it. Every element was
  resolved on save, the run-level ones included (`mc-sdt-run-newest-first-word365`).
- **The save question moves to four text-box probes** (`mc-txbx-*`, on the share). Each branch holds
  content Word itself writes as alternate content: a `wps` text box in the Choice, a VML one in the
  Fallback. The markers on each branch's paragraph and box (`<TAG>_CHOICE_PARA`, `<TAG>_CHOICE_BOX`,
  `<TAG>_FALLBACK_PARA`, `<TAG>_FALLBACK_BOX`) show what Word's save keeps:
  - `mc-txbx-block`: in `w:body`;
  - `mc-txbx-sdt-block`: in a block-level content control;
  - `mc-txbx-tc-block`: in `w:tc`;
  - `mc-txbx-run`: the control, Word's own form, with the element in a run and its branches the boxes.

  Word may keep the block-level element whole, move it into its run-level form (keeping the Choice
  paragraph, its box as the Choice and a VML copy of it as the Fallback), or resolve it. docx4j today
  reads the Fallback paragraph and its VML box at block level, and the `wps` box at run level.
- **Saving: Word 365 moves a block-level element into its own run-level form** (Jason's
  `mc-txbx-*-word365.docx`, 20 October 2026), the same in the body, a block-level content control and
  a cell:
  - it keeps the Choice's paragraph (`<TAG>_CHOICE_PARA`) as an ordinary paragraph, where the
    element was;
  - it writes the paragraph's text box as a new run-level `mc:AlternateContent`: the `wps` box as the
    Choice, and a VML Fallback that it regenerates from the Choice, so `<TAG>_CHOICE_BOX` appears
    twice;
  - the authored Fallback is gone, its paragraph and its box (`<TAG>_FALLBACK_PARA`,
    `<TAG>_FALLBACK_BOX`).

  The control, `mc-txbx-run`, comes back as it went in, its authored Fallback (`RUN_FALLBACK_BOX`)
  kept: with no `altChunk` in the document, Word keeps an unedited box's own Fallback (CR-021 §8.5).
  So Word reads block-level alternate content, and never writes it: it resolves the element to the
  branch it reads, and writes again, at run level, only what needs alternatives there.

### Phase 1: block level, and row and cell level in `word` mode

**Done 2026-10-20:** 6687cc4b8 (the listener fix), 1cff2cd5c (the rest); the record is §11.

- **First, the listener on the binder path** where it is commented out (§1's binder-path defect). It
  is a defect today, so it can land before the rest, on its own.
- **The schema** (§3.1), with D2 (a) the row and cell groups, and the regeneration.
- **The load-time mode** (§3.2):
  - the property, the load option and the package-level override;
  - the listener's records and the splice;
  - the binder update;
  - REQ-305's kept elements, and elements with no branch;
  - `mc:Ignorable`;
  - the record's getter.
- **The preprocessor** (§3.3: the level rule and D4).
- **What the audit changes in the mechanism** (§10.4): the StAX pipe, the record kept across a
  replaced tree, and a fresh binder in place of `updateXML`.
- **The consumers** the audit names, for block level (§10.3), and D6's step on the exporters' copy,
  which shares §3.2's splice (§10.2).
- **The defects of today** the audit found (§10.5) landed before phase 1, all but item 3, the revisions
  model at paragraph level, which goes with this phase's revisions item (§10.3, item 3).
- **The two readers of the record** (§3.2), each with its test:
  - CR-050's inspector: the recorded elements marshalled and located at the promoted content, giving
    the same `mce.branch` addresses as `preserve` mode;
  - the text model: `mce.branches_differ` raised from the record.
- **The tests** of §4, and the CHANGELOG.

Until phase 2, `preserve` mode splices row-level and cell-level elements as `word` mode does, and the
CHANGELOG says so.

**Acceptance:** §4 green; the suites green.

### Phase 2: row and cell level in `preserve` mode (if D2 is (a))

A row or cell in an unselected branch is not part of the table, so each walk that reads rows and
cells by type takes one branch (§10.3):
- the text model's (`Extractor.rows` and `cells`, with `McPurpose.TEXT`);
- the revisions model's;
- OpenDoPE's row removal (`RemovalHandler.xslt`) and its table finder.

`TableModel` already builds its grid from the drawn branch, and with D6 (a) the table writers meet no
element. Then `preserve` mode keeps those elements whole.

Two shapes to check here, from core-ts's audit of the same work (its CR-013 §7.3, 10b20a6,
21 October 2026): a mutator that counts a table's rows or cells without seeing the element, and so
deletes the table when the rows it does not count are all inside a row-level element (core-ts's
accept-all of a deleted row and its column deletion both did); and a tracked-change listing that
misses a revision inside a block-level branch (§10.3 item 3 found docx4j's revisions model has that
gap). Phase 2 also flips the default to `preserve` for 17.4.0 (D5, "Revised").

**Acceptance:** the row and cell probes kept, extracted and drawn as Word shows them (phase 0); the
§10 walkers green in `preserve` mode on the mc-fun probes; the default switched.

**Done** 2026-10-21 (§12): the four sites and the two shapes closed, the probes green on every walker
(§12.3); the switch itself is a 17.4.0 change, recorded in §12.4, since this branch is 17.3.4.

### Phase 3: hand-offs

- **objects-ts:** regenerate from the schema; the commits.
- **docx4j-core-ts** and **docx4j-python:** their readers at block level, against the new probe
  cases, and whether their load models want the two modes. The editor session reports core-ts does
  not understand w16sdtdh, w16sdtfl or w16du, so it takes `w16cex` newest first.
- **docx4j-mcp:** which mode its tools load in. An edit tool that saves may want `preserve`, so that
  a small edit does not also resolve alternate content the agent never touched.
- **The portfolio:**
  - the registry;
  - paper §7.8 can record docx4j agreeing with Word at block level once phase 1 lands;
  - the WD needs no change, since REQ-305's keep-whole is respected in both modes.

**Acceptance:** each receiver has acknowledged.

- **docx4j-python acknowledged** (21 October 2026, to docx4j-25, ahead of the hand-off at Jason's
  request): taken into its CR-012 (f4e3f4c, proposed; implementation on Jason's go). It measured on
  the 25 mc-fun probes that its schema copy (ours at 58cce65a8) admits `mc:AlternateContent` only in
  run and paragraph content and `numPicBullet`; at block, row and cell level its parser skips the
  element, strict mode raises, and a part read and saved loses it whole (14 of the 25 probes). It has
  the `McBranchSdt` defect too (a run-level control directly in a paragraph's branch loses its runs),
  and its `mce_preprocess` takes `w16cid` where Word takes `w16sdtfl`, its understood set lacking
  `w16sdtfl`, `w16sdtdh`, `w16du` and `w16cex`. Its CR-012 phase A ports phase 1: the three schema
  admissions (a schema refresh at 1cff2cd5c or later), the branch `w:sdt` by its container's kind,
  `LoadOptions(mc_block_level="word"|"preserve")` as an lxml splice over the published list (REQ-305
  keep-whole, the no-branch removal, `mc:Ignorable` gaining the selected prefixes, a record on the
  part), `mce_preprocess` on the list, a readers' audit for `preserve`, and the paragraph-level
  revisions twins; its probes are copies of `mc-fun/`.

**Hand-offs sent** 21 October 2026 (Claude Fable 5.1, docx4j-25, Jason: "do CR-051 phase 3"), each naming
the commits (6687cc4b8, c4894c5b0, 1cff2cd5c, 86f8f3e93), the CR's sections, `mc-fun/` (16 probes, 14 Word
365 saves) and the oracle tests. Each acknowledged the same day:

- **objects-ts acknowledged:** regenerated from `xsd/wml/wml.xsd` at 1cff2cd5c (objects fe6f972, unpushed,
  to ship with 0.4.4 after core-ts's go): fifteen content lists now name `mc:AlternateContent` (Body, Hdr,
  Ftr, Tc, CTFtnEdn, Comments.Comment, CTTxbxContent, SdtContentBlock, CTCustomXmlBlock, Tbl,
  CTSdtContentRow, CTCustomXmlRow, Tr, CTSdtContentCell, CTCustomXmlCell) and admit
  `TypedNamedValue<AlternateContent>`; no module appeared or disappeared; all 341 corpus hashes identical,
  since Jsonix already resolved the global element there (the regeneration changes the explicit lists and
  the TypeScript declarations). A `w:sdt` directly in a branch stays DOM there and round-trips whole;
  binding it by its container's kind is core-ts's to do.
- **core-ts acknowledged,** with measurements on the 16 probes (from its main 2782d2b, nothing committed):
  its default load keeps every element at every level and saves it whole (the `mceKeep` pass, with
  `mce.branches_differ`), which is docx4j's `preserve` at all levels; `mcePreprocess: true` resolves on
  the DOM before unmarshal at every level, run and paragraph included, by load's understood set, which
  deliberately lacks w16sdtdh, w16sdtfl and w16du (CR-010 §15 kept load's set to what the model binds),
  so it took `w16cex` where Word 365 takes `w16sdtfl` on all 13 newest-first probes (superseded the
  same day: under its CR-013 D3, as recommended, `mcePreprocess` resolves by the readers' set and takes
  `w16sdtfl` as Word 365 does; implemented, uncommitted there, core-ts 21 October 2026). Its text model
  (default load) reads block level, block-in-sdt, block-in-cell and the four text-box probes as Word 365
  does (w16sdtfl, each with `mce.branches_differ` at the promoted address), reads `mc-block-nofallback`
  whole (REQ-305) and `mc-mixed` as Word; but at row and cell level (the element as a `w:tbl` or `w:tr`
  child, plain or in an sdt) the content was not read and no warning raised, a reader gap (closed by its
  CR-013 phase 1, 10b20a6: the text model reads row and cell level, all 16 probes as Word 365's saves). Its Office JS
  readers (`Paragraph.text`, `Body.text`, `Body.paragraphs`) do not see a kept block-, row- or cell-level
  element at all (`TableCell.values` does, inconsistently), and miss run-level text (its CR-012 §12, phase
  4). The readers have counted w16sdtdh, w16sdtfl, w16du and x14ac as understood since CR-010 §15 (the
  answer to ED-009 §3.8). A `w:sdt` directly in a branch, built both ways, is read and kept, no loss.
  Round trip keeps every element and Choice on all 16 probes. Jason then had it open **core-ts CR-013**
  ("mc:AlternateContent at block, row and cell level: every reader takes the selected branch"; proposed,
  uncommitted, hash to follow): D1 the default stays, every branch kept; D2 addresses, the element's form
  `body/2/0` recommended; D3 `mcePreprocess` keeps load's set; D4 a view in a branch read-only; phases 0
  (audit, probes copied), 1 (the text model's rows and cells), 2 (the content API's readers), 3 (the
  mutators). It relayed Jason's view on docx4j's default (D5): "the default here is as it is since we want
  the branches in the editor ... in fact i think docx4j ought to change its default to match what we do
  here", that is `preserve` by default with `word` on request; put to Jason directly in this session,
  not acted on (see D5).
- **docx4j-mcp acknowledged:** decision T8 in its CR-mcp-server.md §19.5 (uncommitted; hash to follow;
  nothing changes until its engine bump from docx4j 17.1.0, where every tool gets the Fallback): `word`
  (the default, no code) for the read-only tools (extract_text, docx_to_markdown, convert_to_pdf,
  convert_to_html, describe_template, update_toc, and the planned outline, find, read and inspect),
  for markdown_to_docx and html_to_docx (a styles template, body cleared) and, departing from the
  recommendation with its reasons, for fill_template (its output is a new document for a person to open
  in Word, and the binders fill the branch Word shows; in `preserve` they would meet the element and
  §11.2's bind.xslt gap); `preserve` per load for the future tools that save their input with a change
  (tracked-changes edits, anonymize, compare with both inputs in one mode; merge_documents with its
  design); the property never set. It copied `mc-block-newest-first.docx` for extract_text ("fallback" on
  17.1.0, "w16sdtfl" on 17.3.4-SNAPSHOT) and builds fill_template's cases in code after `McProbeDocx`.
- **docx4j-python:** the formal hand-off sent, pointing at the record above; no new ask, none raised.
- **The portfolio:** registry updated (CR-051.3 in progress, the three acknowledgements in its notes).
  Paper §7.8: docx4j's six cells measured (`Texts.plainText` after `WordprocessingMLPackage.load(file,
  McBlockLevel.WORD)` on Jason's `mc-text`, `mc-run`, `mc-block` and their newest-first copies): `w14`,
  `w14`, `w14`, `w16sdtfl`, `w16sdtfl`, `w16sdtfl`, Word 365's row exactly; a docx4j row is with Jason
  (the paper has carried no docx4j results so far, by his rule); `McBlockLevelProbesTest.paperSection78`
  asserts the six cells, the three original-order probes added to `mc-fun/`. The WD needs no change,
  agreed.

## 6. Decisions

Each with a recommendation; Jason decides. D1 to D5 decided as recommended on 20 October 2026; D6,
from phase 0's audit, decided (a) the same day.

- **D1. Block level.**
  - **Options:**
    - (a) admit the element in `EG_ContentBlockContent`: every block container (§3.1);
    - (b) a body-only copy of the group, the other containers still resolved;
    - (c) admit nothing, and have the preprocessor select by the text rule.
  - **Against (b) and (c):** (b) splits a group the ISO schema shares, and leaves cells, headers and
    content controls with today's loss. (c) puts the choice back in the step that runs only when
    JAXB rejects something (§3.2), and leaves no way to preserve.
  - **Recommended:** (a). Word 365 reads the element in a cell and a block content control as it does
    in the body (§1).
  **Decided** (Jason, 20 October 2026): (a).
- **D2. Row and cell level.**
  - **Options:**
    - (a) admit the element in `EG_ContentRowContent` and `EG_ContentCellContent`:
      - phase 1 gives `word` mode, which costs only the admission and the containers in the splice's
        scope;
      - phase 2 gives `preserve` mode, which needs `TableModel` and the table writers;
    - (b) not now: the preprocessor keeps resolving there (with D4's fix), and a later CR takes it up.
  - **Recommended:** (a). Word 365 reads the element in `w:tbl` and `w:tr` and in row and cell
    content controls (§1), and in `word` mode the table code sees ordinary rows and cells.
  **Decided** (Jason, 20 October 2026): (a).
- **D3. The preprocessor's keep rule.** Recommended: follow the schema's admissions (§3.3). With D1
  (a) and D2 (a) that is a flat list of parents; only D2 (b) needs levels. This fixes the collateral
  resolution of §1, a defect today, so it lands in phase 1 whatever else is decided.
  **Decided** (Jason, 20 October 2026): as recommended, a flat list of the schema's admitted
  parents.
- **D4. No Fallback where the element is still not admitted** (in properties; none seen).
  - **Options:**
    - (a) resolve to the first Choice, with the warning;
    - (b) keep dropping the element.
  - **Recommended:** (a). REQ-305 asks for the element kept whole, which only admission gives. Of the
    two resolutions, the first Choice is the one REQ-305's reading rule would take, and dropping
    loses content. Within admitted parents, REQ-305 is met in both modes (§3.2).
  **Decided** (Jason, 20 October 2026): (a).
- **D5. The load-time mode: the property and its default** (Jason's proposal).
  - **Options:**
    - (a) a property (`docx4j.jaxb.mc.blockLevel`) with a package-level override, default `word`,
      `preserve` on request;
    - (b) the same property, default `preserve`;
    - (c) no property: always `preserve` (this CR's first draft);
    - (d) no property: always `word`.
  - **Recommended:** (a), for these reasons:
    1. **docx4j holds what Word holds.** Text, PDF and HTML output, field updates, bindings and a save
       then agree with what a person sees in Word. That is the measure every docx4j output is judged
       by, and the one an agent reading the text needs (CR-050).
    2. **It is what Word's own save does.** Word reads block-level alternate content and never writes
       it (phase 0). A document docx4j saves in `word` mode is the one Word would have saved, text
       boxes apart (§3.5), so no file docx4j writes holds a shape Word never writes.
    3. **It changes the least.** Today's default already resolves block-level elements at load, to
       the Fallback; `word` mode changes only which branch. Content lists still hold no
       `AlternateContent` at block level, so code that walks them by type is unaffected (§3.6). The
       audit's risk, that a walker skips an element it does not expect, falls on `preserve`, which is
       opt-in.
    4. **`preserve` is for tools that must reproduce their input:**
       - the anonymiser, whose output is a reproduction of the document that caused a problem;
       - diffx;
       - an editor or an agent's tool that saves after a small edit and must not change what it did
         not touch;
       - anyone asking what the producer wrote.

       They opt in, per package where one process holds both kinds.
    5. **Nothing is lost to inspection in either mode.** The record keeps each element taken out
       (§3.2), so CR-050's inspector and the text model's warning see every branch in `word` mode
       too.
  - **Against the others:**
    - (b) would have every default user meet block-level elements in content lists and save files
      Word never writes;
    - (c) is (b) with no way out, and keeps the compatibility cost of §3.6 for everyone;
    - (d) leaves the tools of reason 4 with no way to keep what the producer wrote.
  - **The name** is open: `docx4j.jaxb.mc.blockLevel` says that run and paragraph level are not
    governed. Its values are `word` and `preserve`.
  **Decided** (Jason, 20 October 2026): (a): the property with a load option and a package-level
  override, default `word`, `preserve` on request. The name `docx4j.jaxb.mc.blockLevel` stands
  unless phase 1 finds a reason to change it.
  **Revised** (Jason, 21 October 2026, after core-ts relayed his view and he put it here: "we should
  change the default to preserve since we are a developer tool/library"): **(b), `preserve` the
  default, taking effect at a version boundary, 17.4.0**; 17.3.x keeps `word`. The reasons:
  1. **A library should not drop what it did not touch.** docx4j is a developer tool; a load and a
     save with nothing edited should reproduce the producer's alternate content, as the python and
     TypeScript ports do from the start (they keep every branch and select on read). Reason 4's tools
     (the anonymiser, diffx, an editor, an agent's save) are the common case, not the exception.
  2. **Why not in 17.3.4.** Before this CR docx4j already resolved block-level alternate content at
     load (to the Fallback), so no user code has ever met a block-level `AlternateContent` in a
     content list; a `preserve` default puts one there for the first time, and every walker that
     dispatches by type, docx4j's and users', skips it silently (the audit's risk, §10). A silent loss
     of content is not a point-release change. And phase 2 (row and cell level in `preserve` mode) is
     open, so docx4j's own readers do not yet give the Word view of preserved content at every level.
  3. **The sequence.** Phase 2 is undertaken soon (Jason). The default changes at 17.4.0 once phase 2
     is done and the §10 walkers (the exporters' `Preprocess` step of D6, the text model, the binder,
     the field updater, numbering, the anonymiser, diffx) are shown green in `preserve` mode on the
     mc-fun probes. 17.3.4's CHANGELOG announces the coming change and the property, so a user who
     needs `word` sets it before upgrading. Phase 2 records the switch (`McBlockLevel.DEFAULT`,
     `Docx4jProperties`' default, the javadoc of `WordprocessingMLPackage.load` and `Docx4J.load`, the
     reference `docx4j.properties`, the hand-offs to the ports and docx4j-mcp, which loads in `word`
     mode by choice, T8).
  The ports, told the same day: core-ts's default has kept every branch since its CR-010 phase 4 and
  its CR-013 D1 kept it, so from 17.4.0 the two agree; docx4j-python's CR-012 (ae992e0, proposed) now
  recommends `preserve` as its default, its Q7 moved from (a) to (b) for D5's reason and because it
  has no existing walkers to protect (a block-level element is lost on save there today), its readers
  (text model, Markdown, outline, describe, the content API) to read Word's branch, an edit inside a
  kept branch refused until a later CR, row and cell level passed by until phase 2.
- **D6. The exporters' copy** (from phase 0's audit, §10.2; reviewed by docx4j-64, §9). **Decided:
  (a)** (Jason, 20 October 2026: "D6 (a)").
  - **Options:**
    - (a) a `Preprocess` step, immediately after the deep copy, splices each block-, row- and
      cell-level element of the copy to the branch the exporter draws;
    - (b) teach each exporter site the element: about fifteen fixes (§10.2), and the table writers in
      phase 2.
  - **For (a):**
    - one step closes every exporter site, in both modes;
    - the header and footer pre-pass and the real pass then agree on the sections;
    - the exporters' share of phase 2 goes away;
    - no caller sees a change, since the exporters already draw one branch.
  - **How (a) runs:**
    - **The rule** is the host-aware drawing rule, `McSelection.selectedBranch(ac, part)` with the
      copy's part. It is the one the XSLT templates apply (`docx2fo.xslt`:1107,
      `docx2xhtml-core.xslt`:769: `mcPrefersChoice` or `mcSelectsChoice`, the latter
      `ChartExBranch.takesChoice`). With nothing preferred and no Fallback it already takes the first
      Choice (`McSelection.java`:159-163), so REQ-305's element needs no clause of its own.
    - **Its own feature constant,** in both default arrays. `ConversionFeatures.checkParams` adds it,
      as it adds a section feature, since the section model depends on it. Its javadoc says it is
      forced on: a name for tests and logs, not a switch a caller can turn off. `createRelationshipTypes`
      then deep-copies DOCUMENT, HEADER, FOOTER, FOOTNOTES, ENDNOTES and COMMENTS. Without that, a
      caller's own feature set copies the main part alone, and every other part is shared with the
      caller by reference (`PartialDeepCopy`:272-277). The step would then splice the caller's live
      headers, in `preserve` mode losing what the mode keeps.
    - **The parts it visits** are exactly those of the copy: the main document part, the headers,
      the footers, the footnotes, the endnotes and the comments. These are every part both exporter
      families draw, and both go through `Preprocess.process`. The glossary document is not drawn.
    - **Its own finder,** since the copy was never unmarshalled and has no listener records. A
      `TraversalUtil` walk in `McMode.DRAW` records each element whose live parent is a block, row or
      cell container. DRAW descends into the drawn branch only, so an element in a discarded branch is
      never recorded. The splice is §3.2's, applied outermost first through the live parent, with the
      rule as a parameter (TEXT at load, DRAW with the host here): one implementation, two callers.
    - **Idempotent, and assumes no copy:** the header and footer pre-pass runs `Preprocess` again,
      in place, on its own copy with `PP_COMMON_DEEP_COPY` removed (`FOPAreaTreeHelper`:564-565,
      591). The step finds nothing then.
    - **Immediately after the deep copy, before any other step.** Today that is before
      `AcceptTrackedChanges`, or the REJECT branch. `FieldHyperlinkStyle` and `BookmarkMover`, which
      are ALL walks counting across branches, then meet no element either.
    - **With `PP_COMMON_DEEP_COPY` off** it resolves the caller's package permanently, which in
      `preserve` mode loses what the mode keeps. The javadoc says so, and an INFO line names each
      part it splices then, with the count.
    - **Not gated on the mode** (Jason asked, 2026-10-20). `word` mode still leaves elements for it:
      - REQ-305's, kept whole in both modes;
      - content that never passed a part's unmarshal (§3.4): fragments from `XmlUtils`, the trees the
        OpenDoPE binding steps set after load (`Docx4J.toPDF` on a bound document), diffx's output,
        an `altChunk`'s conversion, and content a caller builds or copies in from a package loaded
        in `preserve`.

      What the walk finds is the gate: usually nothing, at the cost of one walk of the copied parts.
      The forced copy costs extra only for a caller's own DOCUMENT-only feature set, since the
      defaults already copy those parts. If that is ever measured as significant, a read-only scan of
      the original could choose the parts to copy; not built without a measurement.
  - **Left alone:** the REF fields' bookmark check reads the original package by design (§10.4 e).
  - **Recommended:** (a).

## 7. Dependencies, risks and out of scope

- **Depends on:** CR-021 (done: `McSelection`, `McMode`, the walker audit's classification); CR-046
  (done: the understood set and the namespaces recorded at unmarshal).
- **Risks:**
  - **The understood set.** `word` mode's branch is Word's only while docx4j's published understood
    set matches Word's. A namespace Word understands and the list lacks, or the reverse, gives
    another branch. The list is maintained with the text model's (CR-046), and the probes of §1 are
    the check.
  - **A block walker that phase 0's audit misses** skips the element's content in `preserve` mode.
    The audit read docx4j-core, docx4j-export-fo, docx4j-markdown and docx4j-diffx (§10).
  - **The package-level override and lazy unmarshal:** a part touched before the override is set has
    already been resolved by the global setting. The load option (§3.2) removes the trap for the
    tools that need `preserve`, and the setter's javadoc says so.
  - **A `w:customXml` directly inside a branch** comes back as a DOM `Element`, not a JAXB object:
    `CTCustomXmlBlock`, `CTCustomXmlRow` and `CTCustomXmlCell` carry no `@XmlRootElement`, and a
    branch's content is `@XmlAnyElement(lax = true)`. `Tbl`, `Tr`, `Tc`, `SdtBlock`, `CTSdtRow` and
    `CTSdtCell` carry one; `Tbl`, `Tr` and `Tc` bind, but the four `w:sdt` classes share one root
    element name, so a lax binding picks one of them for every level (corrected in phase 1, §11.3:
    the element is bound by its container's kind). The splice would put that `Element` into a
    content list. Phase 1 unmarshals it, or the walkers must expect it.
  - **The splice adds bare objects** (a `Tbl`, a `Tc`) to lists whose annotations expect `JAXBElement`
    wrappers (`Body`, `Tr`). docx4j does this everywhere, and its walkers unwrap; the javadoc says
    so.
  - **The binder's `updateXML`** costs a DOM pass in binder mode.
  - **The regeneration** touches every block container's class.
  - **MOXy's handling** of the new element reference and of the listener, checked by §4's MOXy test.
- **Out of scope:**
  - resolving run-level and paragraph-level elements, which stay whole in both modes (§3.2);
  - regenerating a VML Fallback for a text box, as Word's save does;
  - property-level elements (in `w:rPr`, `w:pPr`, `w:tblPr`, `w:sectPr`), which no survey has seen;
  - SpreadsheetML and PresentationML, whose admissions CR-021 phase 3 and CR-024 settled;
  - which branch the exporters draw for an element kept whole.

## 8. Sources

- Paper §7.8 (`docx4j-portfolio/docs/text_extraction_behaviour_model.md`, 52fd4e5): the readers'
  table.
- Jason's probes and their Word 365 saves, `Office 2016/mc-fun/` on the share; this session's are
  beside them.
- CR-021 (§2, §3.1, §3.5, §8.1, §8.2, §8.5) and CR-024.
- `xsd/wml/wml.xsd` (`EG_ContentBlockContent`, `EG_BlockLevelElts`, `EG_PContent`,
  `EG_ContentRowContent`, `EG_ContentCellContent`).
- `docx4j-core/src/main/resources/org/docx4j/jaxb/mc-preprocessor.xslt`.
- `JaxbXmlPart.unmarshal`, `JaxbXmlPartXPathAware`, `Docx4jUnmarshallerListener`,
  `BinderListenerUtils`, `McSelection`.
- WD REQ-301, REQ-302, REQ-303 and REQ-305.

## 9. Review, 20 October 2026

By the docx4j-64 session (Claude Fable 5.1) at Jason's request, against the code. It confirmed:
- the design;
- §1's measurements and §3.1's schema groups and containers;
- the preprocessor's keep rule and drop branch;
- CR-021 §8.1;
- the `McSelection` API and the `Extractor`'s block-level handling;
- the w16 namespaces' presence in the understood set.

`P` binds `AlternateContent` directly (no `JAXBElement`), so a splice by identity works. Nine
findings, none changing D1 to D5, all taken:

1. **A defect today:** the listener is commented out on one binder path
   (`JaxbXmlPartXPathAware.java:627`). That path is every `JaxbXmlPartXPathAware` part's when StAX
   rejects it. Taken into §1, as phase 1's first item, and with a splice test on that path (§4).
2. **REQ-305 was described wrongly:** `selectedBranch(TEXT)` already returns the first Choice when
   nothing is understood and there is no Fallback. The splice tests the condition itself. An element
   with no branch at all is now settled: removed in `word` mode, kept in `preserve` (§3.2).
3. **The Choice prefixes' declaration is existing behaviour** (`addMcChoiceNamespace`, CR-049): now
   relied on, with the discarded Choices' prefixes still declared. The new work is the root's
   `mc:Ignorable` value, which that bookkeeping does not set (§3.2).
4. **Nested elements need the live parent:** `ArrayListDocx4j` re-parents. The record holds the
   element only. It is applied outermost first, skipping elements inside a discarded branch, which
   stay whole in the record (§3.2).
5. **The record's readers are phase 1 work:** the inspector marshals the tree, and `compareBranches`
   takes an address. Named in phase 1, with the test that `word` and `preserve` give the same
   `mce.branch` addresses (§3.2, §4, §5).
6. **The load option belongs in phase 1:** `WordprocessingMLPackage.load` and `Docx4J.load` give no
   hook, and the tools that need `preserve` need it reliably. Taken, on the precedent of
   `setWasStrict` (§3.2). The review's further argument, that the eager-binder property
   (`...binder.eager.MainDocumentPart`) closes the post-load window, was withdrawn by its author. The
   property is read inside the lazy unmarshal and only chooses the binder path, and no load path
   unmarshals a main document part early.
7. **D3 is simpler:** with D1 (a) and D2 (a), a flat list of parents literal to the schema, including
   `mc:Choice` and `mc:Fallback` (§3.3, D3).
8. **Content unmarshalled outside a part** is never spliced, so its elements stay whole in both modes
   (§3.4, §3.6).
9. **Smaller points:**
   - a `w:customXml` in a branch binds as a DOM `Element` (§7);
   - bare objects in wrapper-annotated lists (§7);
   - URI matching on the binder path (§3.2);
   - D5 marked as Jason's proposal (the Status).

**D6, reviewed after phase 0** by docx4j-64 at Jason's request, against the code: (a) is right and
closes the sites §10.2 lists. Three must-fix points and five smaller ones, all taken into D6 and §10:
1. **The copy is partial.** `PartialDeepCopy` deep-copies only the parts whose relationship types the
   features ask for, and shares the rest by reference. The DOCUMENT-only features copy the main part
   alone. The defaults are safe, since `PP_COMMON_MOVE_BOOKMARKS` asks for the headers, footers, notes
   and comments, but a caller's own feature set may not be. So the step has its own feature constant,
   which asks for those parts, and it visits exactly them.
2. **The host-aware rule,** `selectedBranch(ac, part)`, which the XSLT templates apply. It takes the
   first Choice for REQ-305's element, so the separate reading-rule clause went.
3. **Its own finder,** sharing §3.2's splice, since the copy has no listener records.
4. **Smaller points:**
   - the REF fields' bookmark check reads the original package in READ, by design (§10.4 e);
   - the pre-pass runs `Preprocess` again, in place, so the step is idempotent;
   - the slot is "immediately after the deep copy", so a reorder keeps it;
   - every part both exporter families draw is in the copy. docx4j-markdown reads the text model,
     not this pipeline;
   - an INFO line when it splices a package it did not copy.

The choice between a feature constant and an unconditional step was left open by the review. D6
takes the constant and has `checkParams` add it, so it can be tested by name and a caller's feature
set cannot drop it. docx4j-64 agreed: `checkParams` runs before `createRelationshipTypes`
(`Preprocess.java`:77-78), so the parts are copied, and the pre-pass's trimmed feature set gets it
back too.

## 10. Phase 0 record: the walker audit, 20 October 2026

By the docx4j-25 session (Claude Opus 5.5), at Jason's word ("start CR-051 phase 0"), reading the code
at 9263b669f. Four slices were read in parallel by forks of the session:
- the exporters (`convert/out`, docx4j-export-fo, docx4j-markdown);
- OpenDoPE and fields;
- the model, the TOC, the parts and the anonymiser;
- docx4j-diffx, XPath callers and parent casts.

The session checked every finding recorded here as throwing, losing content or a defect today against
the code. Line numbers are at 9263b669f. Nothing was run but one check: after a load, the `getParent()` of a
branch's child is the `AlternateContent.Choice` or `.Fallback` (`mc-run-newest-first`, the StAX path).

### 10.1 What holds everywhere

- **`TraversalUtil` walkers see the element already.** `getChildrenImpl` expands an `AlternateContent`
  at any level by `McMode` (`TraversalUtil.java:599`), and reaches a branch's children by reflection
  (`getAny()`). So each walker of CR-021 §8.3 meets a block-level element as its mode says:
  - READ: the branch the text model reads;
  - DRAW: the branch the exporters draw;
  - ALL: every branch.

  Every one keeps CR-021's class. The only mode corrections are those below: `Paginate.bookmarkKeys`
  (10.3, item 5), and the walks that count across branches (`FieldHyperlinkStyle`, `BookmarkMover`,
  `BookmarksIntegrity`, `FieldUpdaterSEQ`).
- **The XSLT exporters see it already.** The `mc:AlternateContent` templates of `docx2fo.xslt` (:1104)
  and `docx2xhtml-core.xslt` (:767) match at any level. Every block container's template applies
  templates to all its children: `w:body` and a section's content, `w:sdtContent/*`, `w:tbl`, `w:tr`,
  `w:tc`, `w:hdr`, `w:ftr`, the notes. They choose by the drawing rule.
- **The visitor exporters, the text model and the inspector** see it, as §3.4 said
  (`AbstractVisitorExporterGenerator.walkJAXBElements`:247-260, `Extractor.blocks`:633, `InspectWalk`).
  docx4j-markdown reads the text model (`WmlToMarkdown`:121), so it does too; CR-021 §8.3's "no
  `AlternateContent` case" is out of date.
- **Climbs up the tree pass through.** `Choice`, `Fallback` and `AlternateContent` are all `Child`, so
  `PropertyResolver.cellContextOf`, `AbstractTableWriter.inBody` and the like reach the container.
- **Nothing in the exporters throws.**

What fails is code that does one of three things:
- **(B) reads a container's list by type itself:** `instanceof P` or `Tbl` or `SdtBlock`, the first or
  last item, a count. It skips the element's content.
- **(P) edits through a block's parent**, as `(ContentAccessor) p.getParent()`. Inside a branch the
  parent is a `Choice` or `Fallback`, which is not a `ContentAccessor` (its list is `getAny()`). Some
  of these throw; some log and skip.
- **(M) pairs walks in two modes,** READ's data with DRAW's. They disagree wherever the two rules do.
  At block level that is every understood Choice that `preferChoice` does not name (`w16sdtfl` newest
  first).

All of it matters in `preserve` mode, and in `word` mode for REQ-305's kept elements only.

### 10.2 The exporters: one splice on their copy (D6)

Every B site in the exporters is in the pipeline that draws one branch, after `Preprocess`'s deep copy.
`PartialDeepCopy` keeps each element whole, and the copy is never spliced, since it is not unmarshalled.

| Site | In `preserve` mode |
|---|---|
| `ConversionSectionWrapperFactory.getSectPrs`:1207-1225 (top-level `w:p` only) | a section break in a branch's paragraph is never seen: that section's page setup, columns and headers are lost |
| `FOPAreaTreeHelper.trimContent`:107-109 (`SectPrFinder`, READ) | the header and footer pre-pass does find it, so its sections no longer line up with the real pass's: every later section gets another section's header and footer extents |
| `ConversionSectionWrapperFactory.removeContentControls`:1178, `startsPage`:688, `textLength`:613 | a block content control holding a `w:sectPr` in a branch is not unwrapped (logged); a page start or a column-balancing weight in a branch is not seen |
| `PageBreak.movePageBreaks`:134 (`w:p` and block `w:sdt` only) | a page break in a branch's paragraph is lost |
| `Containerization.groupBodyContent`:214 | the branch's paragraphs are drawn inside the previous paragraph's border group, and nothing is grouped within the branch |
| `ListsToContentControls.groupContent`:460 ("TODO: handle") | an HTML list crossing the element restarts; list paragraphs in a branch are not grouped |
| `CoverPageSectPrMover`:55, `FopWorkaroundDisablePageBreakOnFirstParagraph`:50 (`body.get(0)`) | a leading element defeats them; the second guards FOP's empty-flow exception |
| `FopWorkaroundReplacePageBreakInEachList`:85, `ColumnBreaks`:75, `UnequalColumns`:165 | a branch's content is not processed |
| `FOPAreaTreeHelper`'s flatten (:388) and `FloatingDrawingRemover` (:244), `StyleRefMarkers.paintStoredResults`:351 | header content in a branch is measured wrong |
| `BookmarkMover`:73 (ALL) | a pending bookmark moves into the next paragraph in walk order, across branches |
| `FieldHyperlinkStyle`:127 (ALL, one stack) | a field opened before the element and closed in each branch is popped twice |
| `FOExporterVisitorGenerator.firstP`:558 | a container starting with the element takes the next paragraph's properties |
| `AbstractWmlConversionContext.enterParagraph`:396 | a paragraph in a branch, in a text box in a cell, gets the cell's conditional formatting |
| rows and cells: `ConversionSectionWrapperFactory.firstParagraph(Tbl)`:713, `Containerization.groupTable`:153, `ParagraphStylesInTableFix`'s row counts, `AbstractTableWriter.cellWidthFromOuterGrid`:1691 | phase 2 |

One `Preprocess` step, immediately after the copy, would close every one of them, the row and cell
sites included. It splices each block-, row- and cell-level element of the copy to the branch the
exporter draws (`McSelection.selectedBranch(ac, part)`, the XSLT templates' rule, which takes the first
Choice for REQ-305's element). D6 says how: the parts the copy must include, the finder, and the
second run on the pre-pass's copy. With it:
- the exporters then never meet such an element, in either mode;
- the pre-pass and the real pass agree, since the pre-pass copies the preprocessed package
  (`LayoutMasterSetBuilder`:122, :144-159);
- the step goes before any other, `AcceptTrackedChanges` included, so the revisions model meets none
  either (10.3, item 3);
- the visitor and XSLT `mc` templates go on serving run and paragraph level, as today.

The alternative is about fifteen fixes, each a branch case of its own, and phase 2 for the table
writers. That is D6.

### 10.3 The rest: phase 1's list

These sites throw or lose an edit. Each is for phase 1, in `preserve` mode and for REQ-305's elements.

1. **The parent's list.** One helper giving a parent's live content list fixes this family. It covers
   `ContentAccessor`, `List`, `SdtElement`, `CTTxbxContent`, `CTTextbox`, and a `Choice`'s or
   `Fallback`'s `getAny()`; `Resolver.listOf` is the pattern. Its users:
   - **`TraversalUtil.replaceChildren`:678-711.** For a `Choice` or `Fallback` it logs "Don't know how
     to replaceChildren" and drops the new list. `OpenDoPEHandler`'s `ShallowTraversor` (:634) and
     `OpenDoPEHandlerComponents`' `FindComponentsTraversor` (:403) rely on it. For a control directly
     in a branch:
     - a repeat shows one instance, its binding already stripped;
     - a false condition is not removed (without the reverter);
     - an `od:component` is not replaced.

     It happened at paragraph level already, and is fixed for both levels (10.5, item 2).
   - **`FieldUpdater.updateComplex`:216-235, `FieldUpdaterSEQ.updateComplex`:206-222 and
     `AbstractMerger.canonicaliseStarts`:451-481** (`MailMerger`, `MailMergerWithNext`,
     `FORMTEXTMerger`). Each throws `Docx4JException` "Unexpected parent: ...AlternateContent$Choice"
     for a field's paragraph directly in a branch, so the part's whole field update or merge fails.
   - **`migration/FromFormText`:91 and `FromMergeFields`:90:** an unguarded cast, so a
     `ClassCastException`.
   - **Logged and skipped:** `OpenDoPEReverter.replaceConditions`:331-357 and `handleRepeats`:380-441;
     `BookmarkRenumber`'s `_GoBack` removal (:141-223); `BookmarksIntegrity.check`:75-104.
2. **`SdtStAXHandler`:110-127** serves the StAX binder (`BindingTraverserStAX`) and OpenDoPE's
   `ShallowTraversorStAX`. It picks a control's class by its parent's name. Under `mc:Choice` or
   `mc:Fallback` it logs "TODO stack.peek() Choice" and streams the control through unprocessed. Fix:
   look past the `mc` wrappers on the stack. It matters in `word` mode too (10.4, a).
3. **The revisions model** (CR-044):
   - `Trees.blockItems`:162-178 has no `AlternateContent` case, so `Lister.visitBlocks`:243-276 never
     enters a block-level element, and Resolver's move pairing (:694) and `Texts.cellText` (:98-113)
     miss it too;
   - so tracked changes in a branch are not listed, accepted or rejected;
   - in the exporters, `AcceptTrackedChanges` (`TrackedChanges.acceptAllForConversion`) would print
     the deleted text, unless D6's splice comes first;
   - fix: list one branch and the others' changes as twins, as `Lister.alternate`:673 does for text
     boxes. `Trees.runItems` has the same gap at paragraph level (10.5, item 3), and the two levels are
     fixed together.
4. **The TOC.** `TocHelper.getAllElementsFromObject`:57-82 recurses through `ContentAccessor` only, so
   `TocGenerator` (:375) leaves out a heading in a branch and does not number it. Fix: an
   `AlternateContent` case descending one branch, the one Paginate's page numbers come from (item 5).
5. **`Paginate.bookmarkKeys`:395 (M)** walks READ, while its paragraph index comes from
   `bodyParagraphs` (DRAW, :372). A bookmark in a Choice's paragraph is dropped, so its TOC entry gets
   no page. One line: DRAW.
6. **The bound-content shapers:**
   - `BindingTraverserNonXSLT.xpathInjectImage`:512-546 and its StAX twin choose a picture's shape by
     the control's parent. Under a `Choice`, `null` goes into the content list and the picture is lost
     (on the path taken when `replaceBlipEmbed` fails). Fix: shape by the control's own class.
   - `BindingTraverserCommonImpl.applyBoundContent`:513-549 and `shapeHints`:487-494, and
     `bind.xslt`'s shape tests, read only the first child of `w:sdtContent`. Content that starts with
     an element is taken as run-level.
   - `OpenDoPEHandler.processW15Repeat`:982 casts the first item to `SdtElement`: a
     `ClassCastException`.
7. **`AbstractMerger.recursiveRemove`:535-576** recurses through `ContentAccessor` and `JAXBElement`
   only, so an emptied merge paragraph in a branch is never removed.
8. **Small readers by type:**
   - `Extractor.lastParaId`:2187, and `Removals` at :253 and :299: a comment whose last paragraph is in
     an element gets the wrong paraId, and its reply thread breaks;
   - `HeaderFooterPolicy.reservesNothing`:321 counts the element as one item;
   - `AbstractMerger`:195-200 and `getDocumentSeparator`:255-266.
9. **`BookmarksIntegrity.inspectBookmarks`:158** (ALL) counted twins in a `Choice` and a `Fallback` as
   duplicates. Fixed for every level (10.5, item 5); the parent edit in `check` is item 1's.
10. **`JaxbXmlPartAltChunkHost`:213-221, with :332.** For the same `altChunk` `r:id` in two branches,
    the first conversion deletes the part and the second throws a `NullPointerException`.
11. **diffx:** `diffx2wml.xslt`:157 flattens a deleted block-level element. Its wrappers are lost and
    every branch's paragraphs are shown deleted, so the text appears once per branch. An inserted or
    edited element is kept whole, with marks in each branch.
12. **Positional notes,** each fixed by a climb past the `mc` wrappers:
    - `AbstractWmlConversionContext.enterParagraph`:396 (closed by D6 for the exporters);
    - `TableContextTracker.cellContext`:80 (covered already by its story-boundary frame);
    - `HTMLExporterVisitorGenerator.isInHtmlElementSdt`:403;
    - `AbstractVisitorExporterGenerator.jaxbElementName`:231: null for a block-level `w:moveFrom`
      directly in a branch.
13. **XPath.** `getJAXBNodesViaXPath` has no caller in the main code. In `preserve` mode `//w:p`
    returns every branch's paragraphs, and `w:body/w:p` none inside a branch. A javadoc note.

**Row and cell level** (phase 2, `preserve` mode):
- `Extractor.rows`:984-1008 and `cells`:1019-1041: `blockItems` returns null for an element, so its
  text is lost silently;
- `Lister`'s rows and cells (:301, :319);
- `RemovalHandler.xslt`:203-216: a row whose cells all sit in a cell-level element is deleted;
- OpenDoPE's `TableObjectFinder` (:841, :1277): a `w:tc` found in a cell-level branch is lifted out of
  it.

`TableModel` already builds its grid from the drawn branch (`TrFinder` and `TcFinder` with
`McMode.DRAW`, `TableModel.java`:323 and :560), and with D6 the table writers meet no element. So phase
2 is smaller than §5 first said.

### 10.4 What the audit changes in §3.2's mechanism (phase 1)

a. **The StAX pipe is not an unmarshal.** For a part not yet unmarshalled, `JaxbXmlPart.pipe`
   (:400-420, :462-475) streams the stored part, so the splice never sees it. The StAX binder (opt-in,
   `docx4j.model.datastorage.BindingHandler.Implementation`) therefore meets block-level elements in
   both modes.
   - With item 2's fix, its bindings land in every branch, and the later unmarshal splices a bound
     branch.
   - Without it, `word` mode splices a branch whose controls were never bound.
b. **The tree is replaced after load, and the record must survive that:**
   - `BindingHandler` (:397) and the `RemovalHandler` and `OpenDoPEIntegrity` steps set a new tree,
     unmarshalled through `XmlUtils` without the listener (so outside a part, §3.4);
   - `getBinder()` on a part whose tree came from StAX marshals it and unmarshals it again
     (`JaxbXmlPartXPathAware.java`:138-146), through the splice again.

   So the record should hold where each element stood as an address, worked out at splice time, not as
   live references into the tree. An unmarshal that resolves nothing must not clear it.
   `McSelection`'s namespaces, keyed by `Choice` instance (`REQUIRES_NAMESPACES`), do not survive those
   copies either. The Choices are then matched by prefix (REQ-303), as today at every level.
c. **The binder update.** `XmlUtils` documents `binder.updateXML` as once only (JAXB issue 459,
   :1636-1650; not checked against today's JAXB), and a user's later `getJAXBNodesViaXPath(...,
   refreshXmlFirst=true)` would be the second. A fresh `Binder.marshal` of the spliced tree into a new
   DOM keeps the same JAXB objects and avoids the question.
d. **diffx's inputs are loaded parts.** `Differencer.diff(Body, Body)` marshals the caller's trees, so
   the mode applies to them; only diffx's output is unmarshalled outside a part (§3.4, corrected). Both
   inputs must be loaded in the same mode, or a kept element never pairs with the other side's spliced
   paragraphs.
e. **READ against DRAW (M).** With D6, the exporters agree among themselves. `DocumentModel`'s sections
   (`SectPrFinder`, READ) follow the text model's branch, which is Word's. Wherever the exporters draw
   another branch, a caller comparing them with a PDF's sections sees the difference; which branch the
   exporters draw is left out of scope (§7). One exporter read stays on the original package, by
   design: `AbstractWmlConversionContext.isMissingBookmark` (:181-200) collects the REF fields'
   bookmark names from the document as handed over, in READ, since Word looks in the branch it reads
   (CR-046 phase 3). D6 leaves it there. Pointed at the copy, it would lose a form field's bookmark,
   which `FieldsCombiner` collapses with its field.

### 10.5 Defects today, at every level

These turned up on the way. They are not block-level, and none waited for this CR. Five were fixed on
20 October 2026, before phase 1 (Jason: "make the fixes, then phase 1"). Each has a test that fails
on 9263b669f's code: `McBranchTwinsTest`, and `AnonymizeProbesTest.inventoryReadsTheFallback`. They
share a new helper (since 17.3.4):
- `McSelection.branchPaths` gives each object's branches, outermost first;
- `McSelection.twins` tells whether two objects are copies in two branches of one element.

1. **The anonymiser's inventory never read a Fallback.** Fixed.
   - `DmlVmlAnalyzer` has its own `getChildren` (:133-398). It ignored the `McMode.ALL` that
     `Anonymize` (:442) sets, and for an `AlternateContent` fell to reflection. Reflection returns
     `getChoice()`, its only `List` getter. It now returns `McSelection.branches(ac, getMcMode())`.
   - So `containsVML()`, the fields present and the inventory missed what is only in a Fallback. That
     is where Word writes VML text boxes and pictures.
   - **Corrected:** the audit said that an image's `o:title` in a VML Fallback survived
     anonymisation. A run showed otherwise: `ScrambleText`:457-459 clears every `v:imagedata` title,
     in every branch, through its own walk. Nothing leaked.
   - CR-021 §8.4 lists it among `getChildrenImpl`'s direct callers; it is not one.
2. **`replaceChildren` dropped a `Choice`'s or `Fallback`'s new list** (10.3, item 1). An OpenDoPE
   repeat, condition or component in a run-level control inside a paragraph-level branch lost its
   result. Fixed: both are edited through `getAny()`.
3. **Tracked changes inside a paragraph-level element are not listed, accepted or rejected.**
   `Trees.runItems`:142-155 has no `AlternateContent` case, and `Lister.runLevel` enters an element
   only inside a `w:r`, for text boxes. **Not fixed yet.** The fix is the design §10.3's item 3 needs
   at block level: list the branch Word reads, and the other branches' changes as twins, as
   `Lister.alternate` does for text boxes. So both levels go together, in phase 1.
4. **`FieldUpdaterSEQ` counted each branch.** Its ALL walk fed the counter, so a SEQ caption in a text
   box counted twice and the numbering skipped. Fixed:
   - each branch is numbered from the counters as they were at the element;
   - after the element, the counters are those the branch Word reads left;
   - so a caption only in a branch Word does not read is numbered, but not counted.
5. **`BookmarksIntegrity` counted a text box's twin bookmarks as duplicates** (10.3, item 9). With
   remediation it removed the Fallback's. Fixed: a name or id seen again is a duplicate unless the
   earlier one is its twin. Two text boxes holding one bookmark are still duplicates.
6. **`OpenDoPEReverter`'s template maps were keyed by control id** (:204-205). Same-id controls in a
   Choice and a Fallback got the same replacement object, put into both lists. Fixed:
   - the key is the id and the position of each branch the control is in (`sdtKey`), so each twin is
     restored from its own branch of the template;
   - a condition with no replacement is left alone, as a repeat already was, rather than set to null.

Seen in passing, not to do with `mc`:
- `BindingTraverserNonXSLT.xpathInjectImage`'s fallback path returns null for a control in a header,
  footer, note or text box. Its parent tests stop at `Body`, `Tc`, `SdtContentBlock`, `Tr`, `P` and
  `SdtElement`.
- `Containerization.process`:110-132 never containerizes notes or comments. For notes, its list holds
  the notes, not their paragraphs. For comments, the grouped list is a throwaway copy.

## 11. Phase 1 record

### 11.1 Stage A: the load mechanism, 20 October 2026

By a Claude Fable 5.1 subagent of the docx4j-25 session, at Jason's word ("make the fixes, then
phase 1"). Stage A is the listener fix, the schema, the load-time mode, the preprocessor and §4's
tests of them; stage B takes the consumers (§10.3), D6's step, the revisions model, the record's
two readers, the exporter tests and the CHANGELOG.

**What landed, and where it departs from §3.2 and §10.4:**

- **The listener fix** (§1's binder-path defect) is its own change, in
  `JaxbXmlPartXPathAware.unmarshal(InputStream, boolean)`'s rejected-part branch, with
  `BinderPathListenerTest`: a part StAX rejects (a `w14` element in `w:pPr`) beside a run-level
  text box; before the fix it lost the Choice's `wps` prefix (so a save left `xmlns:wps` off the
  root) and the drawing's id.
- **The schema:** `mc:AlternateContent` in `EG_ContentBlockContent`, `EG_ContentRowContent` and
  `EG_ContentCellContent`; the regeneration changed no signature, and each of §3.1's containers
  (and D2's) binds the element.
- **The mode** is `org.docx4j.jaxb.McBlockLevel` (`WORD`, `PRESERVE`; the property, a package's
  own setting through `OpcPackage.setMcBlockLevel`, read when each part unmarshals). The load
  option is `Load3.setMcBlockLevel`, `OpcPackage.load(File|InputStream, McBlockLevel)`,
  `WordprocessingMLPackage.load(..., McBlockLevel)` and `Docx4J.load(..., McBlockLevel)`; a Flat
  OPC load gets it through `FlatOpcXmlImporter.setMcBlockLevel`.
- **The splice** is `org.docx4j.jaxb.McSplice.apply(elements, rule)`, one implementation with the
  rule as a parameter, as D6 asks: at load `McSplice.WORD_READS` (TEXT's branch; an element with
  no understood Choice and no Fallback kept whole, REQ-305); stage B's step gives its own rule
  (`McSelection.selectedBranch(ac, part)`) and its own finder, with `McSplice.inScope` as the
  scope test. Elements are applied outermost first (by depth), through the live parent; one inside
  a branch the splice discarded, or inside an element it kept whole, is skipped and stays whole in
  there. An element with no branch at all is removed and recorded.
- **The record** is `ResolvedAlternateContent` on `JaxbXmlPart.getResolvedAlternateContent()`:
  the element whole, the branch promoted, the container's name, its address from the part's root
  (`w:document/w:body[0]/w:tbl[3]/w:tr[0]/w:tc[1]`: each step an element name with its index in
  the parent's content list where the parent has one), the index the element stood at and the
  count promoted. Appended to, never cleared, so it survives the re-unmarshal a late binder makes
  (§10.4 b). The promoted branch's objects are moved, not copied: the record's Choice still lists
  them, and their parent is now the container (on a binder path, after the binder is re-created,
  they are no longer the tree's objects; see the next item).
- **The binder, a departure from §10.4 c and §3.2 step 3.** Measured on jaxb-runtime 4.0.x (the
  RI) with a scratch test: `Binder.marshal` into a new DOM associates neither the root nor the
  members of an element list (`getXMLNode(root)` null), and `Binder.updateXML(root)` leaves the
  root associated with the node it removed, so an XPath from the root finds nothing afterwards
  (JAXB issue 459's "once only" is never, for the root). So after a splice on a binder path the
  binder is re-created as `createBinderAndJaxbElement` does: the spliced tree marshalled to a DOM
  and unmarshalled again through a new binder, with new JAXB objects, before the unmarshal
  returns (so a caller never sees the objects change). The re-unmarshal finds no element and the
  record stays. On the binder paths each recorded Choice's `Requires` is first matched by URI
  through `binder.getXMLNode(choice)`'s declarations (§3.2's optional item).
- **`mc:Ignorable`:** the conventional prefix of each namespace a promoted Choice's `Requires`
  names (the marshal writes the promoted content with the conventional prefixes), added to the
  root where not already listed, through its `setIgnorable`; set where the root had none (Jason's
  probes).
- **`preserve` until phase 2:** row- and cell-level elements are spliced as in `word` mode, unless
  they lie inside an element kept whole (there they stay whole with it). Lifted by phase 2 (§12): in
  `preserve` mode nothing is spliced at any level.
- **The preprocessor** (D3, D4): the keep rule is the flat list of §3.3 (the `EG_PContent` family,
  every block container, `w:tbl`, `w:tr`, `w:sdtContent`, `w:customXml`, `mc:Choice` and
  `mc:Fallback`); an element it still resolves takes its first Choice when it has no Fallback.
- **The StAX pipe** (§10.4 a): nothing to change in the mechanism; `JaxbXmlPart.pipe`'s javadoc
  says a pipe meets the element whole and the splice applies at the later unmarshal.
  `SdtStAXHandler` (§10.3 item 2) is stage B's.
- **A `w:customXml` directly in a branch** (§7) is unmarshalled by the splice as the container's
  kind (`CTCustomXmlBlock`, `CTCustomXmlRow`, `CTCustomXmlCell`) and wrapped as the container's
  binding expects; tested.
- **Content lists and types:** `Comments.Comment`, `CTFtnEdn` and the `CTCustomXml*` classes are
  not `ContentAccessor`s; `McSplice.contentOf` knows them. Phase 1's helper for §10.3 item 1 can
  hoist it.

**Tests** (docx4j-core-tests, `org.docx4j.jaxb`): `BinderPathListenerTest` (1);
`McBlockLevelLoadTest` (16: every container of §3.1 and D2 in both modes, no Fallback, REQ-305,
nested, `mc:Ignorable`, no branch, custom XML in a branch, the eager and late binder paths, the
rejected part's path, the text model in both modes, the option and the override over the
property); `McBlockLevelProbesTest` (6: Jason's probes, copied to
`docx4j-core-tests/src/test/resources/mc-fun/` with their Word 365 saves; a load and save in
`word` mode reads as each Word save reads, the block-level probe's tree and record,
`mc-block-nofallback`, `mc-mixed`'s three run-level elements kept, every branch kept in `preserve`
mode). `mc-block-newest-first` has no Word save (paper §7.8's reading, `w16sdtfl`, is asserted
directly). `VmlAttributesRoundTripTest` now loads its fixtures in `preserve` mode: the default
resolves the `mc-txbx-*` probes' block-level VML Fallbacks away, as Word's save does, and that
round trip is about reproducing the input. MOXy: the project has no MOXy test profile; the two
modes, the saves against Word's and the binder paths were run under docx4j-JAXB-MOXy (4.0.9, on
the classpath) with a scratch driver over the probes, all passing.

### 11.2 Stage B: the consumers, D6 and the record's readers, 20 October 2026

By a Claude Fable 5.1 subagent of the docx4j-25 session, on stage A's tree. Stage B is D6's step,
the consumers of §10.3 at block level (with the revisions model at both levels), the tools that
load in `preserve`, the two readers of the record, §4's exporter tests and the CHANGELOG.

**What landed, and where it departs from §3.2, §6 and §10.3:**

- **D6's step** is `ResolveAlternateContent` (`convert/out/common/preprocess`), its constant
  `ConversionFeatures.PP_COMMON_RESOLVE_ALTERNATE_CONTENT` in both default arrays and added by
  `checkParams`; `createRelationshipTypes` copies DOCUMENT, HEADER, FOOTER, FOOTNOTES, ENDNOTES and
  COMMENTS for it; `Preprocess.process` runs it immediately after the copy, before
  `AcceptTrackedChanges` or the REJECT branch. Its finder is a `TraversalUtil` walk in DRAW over
  each drawn part (the main part, then the headers, footers, notes and comments the main part relates
  to), recording each element whose effective container is in scope; the splice is `McSplice.apply`
  with `McSelection.selectedBranch(ac, part)`. It adds the promoted Choices' prefixes to the root's
  `mc:Ignorable`, as the load does, and logs each part it changes at INFO when the deep copy is off.
  It does not add to the part's `getResolvedAlternateContent()` record (the copy is thrown away; with
  the copy off, the drawing rule's choices are not the load's). Nothing is gated on the mode.
- **Nesting, beyond stage A.** Stage A's listener recorded an element only where its direct parent
  is in scope, so an element directly inside another's Choice was left whole in the container after
  the outer one was promoted. Now the listener also records an element whose parent is a Choice or
  Fallback (its branch's own parent is not yet set when the callback fires), and the part's resolve
  keeps those whose *effective* container (`McSplice.effectiveContainer`: up through the `mc`
  wrappers) is in scope. Applied outermost first, the inner element then stands in the container and
  is spliced there; inside a discarded branch, or under a run- or paragraph-level element, it stays
  whole. D6's finder uses the same test.
- **The parent's list** (§10.3 item 1) is `McSplice.contentOf`, which now also answers for a
  `List`, a content control, an `SdtContent`, a VML `v:textbox`, and the lists of a footnotes,
  endnotes, comments or `w:docParts` root (so addresses index notes and comments). Its users:
  `FieldUpdater.updateComplex`, `FieldUpdaterSEQ.updateComplex`, `AbstractMerger.canonicaliseStarts`,
  `FromFormText`, `FromMergeFields`, `OpenDoPEReverter.replaceConditions` and `handleRepeats`,
  `BookmarkRenumber`'s three removals, `BookmarksIntegrity.check`.
- **The other consumers:** `SdtStAXHandler.contextName()` looks past the `mc` wrappers on its
  stack, for its sdt kinds and for `BindingTraverserStAX`'s picture shaping (item 2);
  `TocHelper.getAllElementsFromObject` descends the drawn branch (item 4); `Paginate.bookmarkKeys`
  walks DRAW (item 5); `BindingTraverserNonXSLT.xpathInjectImage` shapes by the effective container,
  and `BindingTraverserCommonImpl`'s shapers and `OpenDoPEHandler.processW15Repeat` look through an
  element at the start of a control's content into the drawn branch (item 6; `bind.xslt`'s shape
  tests are left as they are, a `w:sdtContent` starting with an element still shaped as run-level on
  the XSLT binder); `AbstractMerger.recursiveRemove` searches every branch (item 7);
  `Extractor.lastParaId`, `Trees.lastParaId` (for `Removals`), `HeaderFooterPolicy.reservesNothing`
  (through `McSelection.selectedContent`) and `AbstractMerger`'s last-block lookups (item 8);
  `JaxbXmlPartAltChunkHost` deletes each `altChunk` part once every `altChunk` naming it is converted,
  and skips one naming no part (item 10); `diffx2wml.xslt` keeps a deleted element's wrappers
  (item 11); the XPath javadoc note (item 13). Item 12's exporter sites are closed by D6.
- **The revisions model** (item 3, both levels): `Trees.blockItems` and `runItems` answer an
  element with the branch the text model reads, so `Texts.cellText`, Resolver's walks and the row and
  cell finders take it; `Lister` lists that branch's changes where the element stands, kept apart
  as a block control's are, and pairs each other branch's changes to them as twins by kind and
  position (`pairTwins`, as a text box's Fallback), listing them on their own for acceptAll. Text
  boxes in another branch become twins of the listed branch's by position.
- **The tools:** `AnonymizeCli` loads in `PRESERVE` (and `Anonymize`'s javadoc says so);
  docx4j-diffx itself loads nothing (its callers do), so the sample `CompareDocuments` loads both
  sides in `PRESERVE`.
- **The readers of the record.** The text model: `Extractor.blocks` matches the container it walks
  against each record's address (`McSplice.address`, worked out at the splice and again at the walk,
  only when the part has records) and raises `mce.branches_differ` from the record at the promoted
  content's address (`body/1`, as the element's would be in `preserve`). The inspector:
  `InspectXml.restore` puts each record back into the part's marshalled DOM, last record first, the
  element marshalled into that document (`InspectXml.locate` walks the address: an indexed step is
  that content child, skipping property elements; an unindexed one the first child of that name)
  and the promoted elements moved into the branch they came from, so they keep their links to the
  live objects; the walk then finds the element and its unselected branches where `preserve` mode
  has them. For that, `McSplice.address` names the DrawingML and VML chain to a text box properly
  (`wp:inline/a:graphic/a:graphicData/wps:wsp/wps:txbx/w:txbxContent`, `w:pict/v:shape/v:textbox`):
  the element name comes from the parent field's JAXB annotation, or the field's name where it has
  none. **A correction to §4's wording:** an `mce.branch` finding carries no extraction address in
  either mode (its content is never extracted; CR-050's NEVER), so what the modes agree on is the
  finding's location (part and path) and excerpt, which the test compares.
- **A `w:p` in a branch has different text-model addresses in the two modes** (`body/1/0` in
  `preserve`, `body/1` in `word`), since the element is a container level in `preserve`. Only the
  element's own address is the same; §4 asks no more.

**Tests** (docx4j-core-tests unless said): `McBlockLevelReadersTest` (the text model's warnings and
the inspector's findings equal across the modes for elements in the body, a cell, a block content
control, a text box, a header, a footnote and a comment; an element directly in a branch);
`McBlockLevelConsumersTest` (a DOCPROPERTY field, a bookmark remediation, the TOC helper, Paginate's
keys, a header's blank lines, `contentOf`); `ResolveAlternateContentTest` (a DOCUMENT-only feature
set copies the header and splices, the caller keeps every element, run twice changes nothing, the
caller's own package with the copy off, and the HTML exporters in both modes and under
`preferChoice`); `TrackedChangesAlternateContentTest` (both levels: listed once with twins, accept
and reject resolve the twin, acceptAll resolves every branch); `AnonymizeCliPreserveTest`;
docx4j-export-fo-tests `AlternateContentSectionTest` (a section break in a branch's paragraph lays
out on two pages with each section's header, both exporters, both modes, and under `preferChoice`);
docx4j-markdown `MarkdownAlternateContentTest`; docx4j-diffx-tests `AlternateContentDeletedTest`.
`McBlockLevelConsumersTest` also covers, in `preserve` mode with the element at block level: the
mail merger (a MERGEFIELD paragraph in each branch merged; one emptied by a field with no value
removed from its branch), `FieldUpdaterSEQ` (each branch numbered from the counter at the element),
the StAX binder (a bound control directly in a Choice bound as an `SdtBlock`, checked on the
binder's output in the part store), `OpenDoPEReverter` (a condition control whose parent is a
Choice or Fallback replaced), `JaxbXmlPartAltChunkHost` (one text/plain `altChunk` named in both
branches, converted in both, its part deleted once; the test sets the loaded part's `AltChunkType`,
though a loaded part has one already, from its extension: CR-053 §1, measured 2026-10-20) and
`FromMergeFields` (a MERGEFIELD paragraph in each branch migrated; `FromFormText`
shares the helper). Nine of the class's thirteen tests were seen to fail with the fixes reverted in
the working tree (`contentOf` answering null for a Choice or Fallback, `recursiveRemove` without
its branch case, `SdtStAXHandler` back to `stack.peek()`, the altChunk host deleting inside its
loop without the null guard), and the diff was byte-identical after the restore. Not shown failing:
`BookmarkRenumber` (its entry is package-private). The bookmark remediation test first placed its
orphan start inside a paragraph, whose parent is a `ContentAccessor` either way; it now stands
directly in the Choice. The extraction probe's block-level cases (§4) are for the WD suite, with
Word checks.

**`OpcPackage.clone()` keeps the mode:** it saves and reloads, and loaded through `load(InputStream)`
alone a `preserve` package's clone would be resolved by the property's default; the migrators
clone. Now the clone is loaded with this package's own setting where it has one (§3.2's
package-level setting).

**A gap found by these tests, stage A's, open for Jason: a `w:sdt` directly inside a Choice or
Fallback does not survive an unmarshal.** A branch's content is `@XmlAnyElement(lax = true)`, so
JAXB binds a child by its root element name alone, and four classes carry
`@XmlRootElement(name = "sdt")` (`SdtBlock`, `CTSdtRow`, `CTSdtCell`, `SdtRun`): the one the
context's root map answers with is `CTSdtRow`, whose `w:sdtContent` admits `w:tr` only. Measured on
a block-level control (a `w:p` in its content) in the body's element: with only a `w:id` in its
properties the part loads in both modes and the control's paragraphs are *silently dropped* (the
text model reads nothing of the branch); with a `w:tag` or a `w:dataBinding` the unmarshal fails,
the preprocessor keeps the element whole (D3) and fails the same way, and the part does not load
("Preprocessing exception"). `XmlUtils.unmarshalString` fails alike. §7's note that these classes
"carry one, and bind" was wrong: they bind, but all to one name. Before CR-051 the preprocessor
resolved such an element to its Fallback before any branch was unmarshalled, so this is a load
regression for a document with a content control directly in a block-level branch (none in
CR-021's 835, and none of Jason's probes: `mc-sdt-block-newest-first` has the element inside the
control). The same holds since 17.2.0 for a run-level control directly in a paragraph-level branch.
A design, not built here since it touches every unmarshal route: on the way in, make such a
`w:sdt` unbindable (a private namespace on that one element, through a `StreamReaderDelegate` on
the StAX path and a DOM pass on the preprocessor and binder paths), so it arrives as a DOM
`Element` as a `w:customXml` does; then bind it by its effective container's kind (`SdtBlock`,
`CTSdtRow`, `CTSdtCell`, `SdtRun`, with `unmarshal(Node, Class)`, which ignores the root name) in
`McSplice.bind` for promoted content and in the part's resolve for elements kept whole, in both
modes. The two tests that first needed such a control (the StAX binder's, the reverter's) build it in
memory with a declared-type unmarshal; §11.3 closes the gap.

### 11.3 A `w:sdt` directly in a branch, 20 October 2026

Jason chose the design of §11.2 ("ok, (a) it is"), built by the same subagent as the last part of
phase 1.

**The measured failure** (§11.2): a block control directly in a block-level branch, with only a
`w:id`, loaded in both modes with its paragraphs silently dropped; with a `w:tag` or a
`w:dataBinding` the part failed to load; `XmlUtils.unmarshalString` failed alike; a run control
directly in a paragraph-level branch had failed the same way since 17.2.0. Before the fix,
`McBranchSdtTest` failed on every route: three paths dropping the block control's content, the
`XmlUtils` and DOM-binder cases with "Preprocessing exception".

**The fix** is `org.docx4j.jaxb.McBranchSdt`:
- **On the way in,** a `w:sdt` that is a direct child of `mc:Choice` or `mc:Fallback` is put in
  the private namespace `McBranchSdt.NAMESPACE` (`urn:docx4j:mc-branch-sdt`, prefix `mcsdt`), so the
  lax any-list delivers it as a DOM `Element`, as it does a `w:customXml`. The URN is a name nobody
  else writes: no producer declares it, no schema docx4j binds names it, and it is never written
  out, so it cannot collide. The routes:
  - the StAX path, through `McBranchSdt.wrap`, a `StreamReaderDelegate` that tracks only whether
    each open element is a branch and answers the private namespace for that one element's start
    and end (a name test per element, nothing else): `JaxbXmlPart.inputStreamToXSR`, which every
    part's StAX unmarshal uses, and `XmlUtils.unmarshal(InputStream)`;
  - the DOMs, through `McBranchSdt.rename`: the preprocessor's result (`JaxbXmlPart.transformToDom`,
    both `mc-preprocessor` retries in `JaxbXmlPart.unmarshal(Element)` and
    `JaxbXmlPartXPathAware`), the binder paths' DOMs (`unwrapUsually`, the eager binder, the
    rejected-part retry), `XmlUtils.unmarshalString` (both overloads, so `unmarshallFromTemplate`
    too), `XmlUtils.unmarshal(Node)` (both overloads) and the two `XmlUtils` preprocessor retries,
    which now go through a `DOMResult` instead of a `JAXBResult` (SAX straight into JAXB gave no
    hook). A caller's DOM (`unmarshal(Node)`, `JaxbXmlPart.unmarshal(Element)`) is put back after
    the unmarshal, JAXB having copied the subtree it keeps.
  - Left alone: MOXy's own binder listener path is the same code; nothing else in docx4j-core
    unmarshals WordprocessingML content by another route (the StAX binder streams, it does not
    unmarshal, and `SdtStAXHandler` already picks the control's class by context).
- **Bound by its container's kind** with `Unmarshaller.unmarshal(Node, Class)`, which ignores the
  root name: `SdtBlock` in block content, `SdtRun` in a paragraph's, `CTSdtRow` in a table's,
  `CTSdtCell` in a row's (`McBranchSdt.kindFor`, from `McSplice.effectiveContainer`), bare in a
  branch's list and wrapped as `w:sdt` where a container's list wants a `JAXBElement`
  (`wrapFor`). Where: the listener (`Docx4jUnmarshallerListener.takeBranchSdtElements`) notes each
  element holding one, and the part binds them when the unmarshal returns
  (`JaxbXmlPart.bindBranchSdts`, before the splice, with an unmarshaller of the part's own, whose
  listener records what is nested in a bound control: further such elements, bound in their turn,
  and block-level elements, handed to the splice); `XmlUtils` with `McBranchSdt.Collector`; and
  `McSplice.bind` for an element promoted out of a branch. So an element kept whole (`preserve`
  mode, REQ-305, every paragraph-level element) holds typed controls, which closes the 17.2.0
  paragraph-level case too.
- **Never in output:** a control that cannot be bound is put back in the `w` namespace
  (`McBranchSdt.restore`) and kept as DOM, so a save writes `w:sdt`; the test checks every saved
  part and `getXML()` for the URI.
- **A binder's DOM** keeps a kept branch's controls in the private namespace: a restored `w:sdt`
  node there has no object association, so an XPath result for it would throw
  (`XPathBinderAssociationIsPartialException`); hidden, it is skipped, which the
  `getJAXBNodesViaXPath` javadoc says. The promoted controls, ordinary content after the splice,
  are found.

**Tests:** `McBranchSdtTest` (5): the StAX, preprocessor (a part StAX rejects) and eager-binder
paths and `XmlUtils.unmarshalString`/`unmarshal(InputStream)`, in both modes, with a block control
with a tag and one with a data binding, a run control in a paragraph-level element, a row control
in a table's and a cell control in a row's: the content survives, each control has its class, a
save writes `w:sdt` in the `w` namespace and no part nor `getXML()` holds the URI, a reload reads
the same; and the default (DOM) binder binds a bound control in each kept branch. Row and cell
level were still resolved in `preserve` mode until phase 2, which the expectations allowed for (now
kept whole: §12).

## 12. Phase 2 record: row and cell level in `preserve` mode, 21 October 2026

By Claude Fable 5.1 (session docx4j-25), at Jason's word ("do CR-051 phase 2"), on the tree at
296a3b7bd. Phase 1 had left row- and cell-level elements spliced in `preserve` mode (§11.1), since
the walks that read rows and cells by type would have lost their content. This phase teaches those
walks the element and lifts the carve-out.

### 12.1 What changed

- **The load path.** `JaxbXmlPart.resolveAlternateContent` splices nothing in `preserve` mode, at
  any level. The record (`getResolvedAlternateContent()`) is therefore empty for a `preserve` load.
  The javadocs of `McBlockLevel` and the record say so; the log line names the mode.
- **The text model** (`Extractor.rows` and `cells`): an `mc:AlternateContent` among a table's rows
  or a row's cells is compared with its other branches (`mce.branches_differ`, at the element's
  address) and the branch Word reads (`McPurpose.TEXT`) is walked in its place, as `blocks` does at
  block level. Both walks also read the `word`-mode record for their container, so a row- or
  cell-level element taken out at load raises the same warning at the same address as in `preserve`
  mode (the inspector already did, through `InspectXml.restore`, whose address walk skips `tblPr`
  and `tblGrid`). Before this, `blockItems` answered null for the element and its text was lost
  silently (§10.3).
- **The revisions model** (`Lister`, `Resolver`, `Texts`):
  - `Lister.tableTokens` walks the rows through `rowItems` and the cells through `cellItems`, each
    with an `AlternateContent` case (`alternateRows`, `alternateCells`): the branch Word reads is
    listed where the element stands and each other branch's changes become twins by kind and position
    (`pairTwins`), listed on their own for acceptAll, as `alternateBlocks` does at block level. A row
    or cell that is itself a revision in another branch is the twin of the listed branch's, so
    accepting or rejecting one resolves both.
  - `Resolver.row` removes a table left with no rows only when no branch has one
    (`Lister.hasRows`, every branch): `rowsOf` reads one branch, and a row in another branch still
    makes the table one. This is core-ts's first shape (its CR-013 §7.3: a mutator that counts rows
    without seeing the element deleting the table); docx4j's `Lister.rowsOf` already saw the element
    through `Trees.blockItems`, so the table was wrongly removed only where the read branch's rows
    were all deleted and another branch's were not.
  - `Texts.rowText` and `cellText` take the rows and cells through `Lister.rowsOf` and `cellsOf`
    (content controls, custom XML and a kept element), where they tested `w:tr` and `w:tc` directly.
  - The second shape (a tracked-change listing missing a revision inside a branch) was closed at block
    and paragraph level in phase 1 and now at row and cell level.
- **OpenDoPE:**
  - `RemovalHandler.xslt`'s row template counts the row's cells wherever they stand
    (`count($results//w:tc)`, where it counted direct children), so a row whose cells are all in a
    cell-level element, a cell-level content control or custom XML element is no longer deleted. A
    nested table's cells only ever stand inside a cell of the row, so the descendant count is the
    direct count wherever the direct count was right.
  - `OpenDoPEHandler.emptyKeepingTableShape` (new, package-private; the two identical blocks that
    emptied a repeat-of-zero or a false condition's control now call it): where the control's content
    is a cell-level element kept whole, each branch keeps its own cell, emptied to one paragraph with
    its `tcPr`, and the element stays, so a save writes every branch. Before, the first `w:tc` found
    in any branch was lifted out of the element into the control and the other branches were lost.
- **Not changed, by survey** (every `instanceof Tr` and `instanceof Tc` in docx4j-core,
  docx4j-markdown and docx4j-diffx):
  - `TableModel`, `TcFinder`, `PropertyResolver`'s parent climb (it passes over a `Choice` and the
    element to reach the `w:tr`), the exporters' `Containerization`, `AcceptTrackedChanges` and
    `TableContextTracker` (after D6's splice), `BindingTraverserCommonImpl`'s shapers and
    `BindingTraverserNonXSLT` (phase 1's effective container), `AbstractMerger`;
  - `UpdateXmlFromDocumentSurface.getAsDocx` reads a cell control's first item for a `w:tc`; with a
    kept cell-level element first, the element goes into the clone's body as block content. A gap of
    that tool alone, for a bound cell control whose content is alternate content, left;
  - `bind.xslt`'s shape tests (phase 1 left them) and `RemovalHandler`'s `JAXBResult` path, which
    unmarshals the stylesheet's output straight into JAXB (a `w:sdt` directly in a branch of that
    output would meet §11.3's lax binding there; the handler removes controls, so none should remain).

### 12.2 Tests

`McBlockLevelLoadTest.preserveModeKeepsBlockLevelElements` now holds every container of §3.1 and D2
to the same expectation (kept whole, every branch and prefix written, whole after a round trip,
Word's branch in the text in both modes); `McBlockLevelProbesTest.preserveModeKeepsEveryBranch`
without its row and cell exception, and `preserveModeReadsAsWordsSave` (each probe with a Word 365
save reads, in `preserve` mode and after a `preserve` save, as Word's save reads);
`McBranchSdtTest` expects the row and cell controls of both branches typed (`CTSdtRow` twice,
`CTSdtCell` twice) and no `w:sdt` found by the binder's XPath in `preserve` mode;
`McBlockLevelReadersTest`'s probe gains a row-level and a cell-level element (`body/3/1`,
`body/3/2/1`), the warnings and `mce.branch` findings equal across the modes;
`TrackedChangesAlternateContentTest` gains row and cell level (listed once with twins, accept and
reject resolve the twin, acceptAll resolves every branch) and the two row-removal cases (a deleted
row in every branch accepted removes the table; a deleted row in the read branch with a plain row in
the Fallback keeps it); `McRowCellLevelOpenDoPETest` (new, `org.docx4j.model.datastorage`): the
removal keeps a row whose only cell is in a kept element and removes the control in the next row;
emptying a cell control keeps each branch's cell with its `tcPr`; a control without an element is
emptied as before. The nine classes (94 tests, with `ResolveAlternateContentTest`,
`McBlockLevelConsumersTest` and `TrackedChangesTest`) pass; the full docx4j-core-tests, export-fo,
markdown and diffx suites were run (the result in the commit message).

### 12.3 The §10 walkers on the probes

A scratch driver over the 19 `mc-fun/` probes (every one but the Word saves), each loaded in
`preserve` mode: the text model (`Texts.plainText` and its warnings), the inspector (`Inspect.of`),
tracked changes (`TrackedChanges.in`, `acceptAll`), `FieldUpdater`, the HTML exporter, the PDF
exporter (docx4j-export-fo), a save, a reload in `preserve` mode, and the anonymiser (`Anonymize`)
on a fresh load. On every probe: the text equals `word` mode's and, where a Word 365 save exists,
Word's (`w16sdtfl` on the newest-first probes at row and cell level too); one `mce.branches_differ`
per element; one `mce.branch` finding per unselected branch (10 per probe, 9 for the no-Fallback
probe, 1 per text box); no exception in any walker; the save writes every element and Choice; the
reload reads the same; the anonymiser's output keeps every element. The exporters draw by their own
rule (D6, §7): the Fallback by default and the Choice `docx4j.jaxb.mc.preferChoice` names, so with
Word's namespace preferred (`w16sdtfl`, `w14`, `wps`) the HTML text equals `word` mode's on every
block-, row-, cell- and text-box probe; run- and paragraph-level probes draw by the same rule in both
modes, as they are not governed. The PDFs of the row and cell probes were rendered without error.
The `preserve`-mode saves of the five row and cell probes are on the share (`mc-fun/<name>-docx4j-
preserve.docx`) for Jason's Word check: each should read as its Word 365 save does, since the XML
written is the input's with every branch (`REQ-305`, CR-049's declarations). **Checked** by Jason the
same day in Word 365: each reads `w16sdtfl` ("Each should read w16sdtfl: yes").

### 12.4 The 17.4.0 switch

This branch is 17.3.4, where `word` stays the default (D5, "Revised"). The 17.4.0 branch makes one
change, with its CHANGELOG line: `McBlockLevel.fromProperty()` answers `PRESERVE` where the property
is unset or names neither mode (its two `WORD` returns today); the javadocs of `McBlockLevel`,
`McBlockLevel.PROPERTY`, `WordprocessingMLPackage.load(.., McBlockLevel)`, `Docx4J.load(..,
McBlockLevel)`, `Load3.setMcBlockLevel` and `OpcPackage.setMcBlockLevel`, the reference
`docx4j.properties` (docx4j-samples-resources: `docx4j.jaxb.mc.blockLevel=preserve`, with the
comment that `word` gives Word's view in the tree) and the test `McBlockLevelLoadTest`'s property
case follow. The §10 walkers are green in `preserve` mode on the probes (§12.3), which D5 asked
for before the switch. The ports and docx4j-mcp hear of it with this phase's hand-off (docx4j-mcp
loads in `word` by its T8, unaffected).
