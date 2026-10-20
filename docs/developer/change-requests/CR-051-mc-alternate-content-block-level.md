# CR-051: `mc:AlternateContent` at block level: read as Word reads it on load, or kept whole on request (body, cells, rows, headers, notes, text boxes, content controls)

Status: ACCEPTED 2026-10-20 (Jason: "accept recommendations for CR-051's D1 to D5"), to implement as
recommended. Phase 0 done 2026-10-20: the walker audit (§10) leaves one decision for Jason before
phase 1, D6 (the exporters' copy). Proposed 2026-10-20, drafted at Jason's request ("write
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
recommended, D5 being his own proposal; D6, from phase 0's audit (§10), open. It changes
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
- **The defects of today** the audit found (§10.5) can each land on their own, before the rest, as
  the listener fix can.
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

**Acceptance:** the row and cell probes kept, extracted and drawn as Word shows them (phase 0).

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

## 6. Decisions

Each with a recommendation; Jason decides. D1 to D5 decided as recommended on 20 October 2026; D6,
from phase 0's audit, is open.

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
- **D6. The exporters' copy** (from phase 0's audit, §10.2; reviewed by docx4j-64, §9). Open, for
  Jason.
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
    `CTSdtCell` carry one, and bind. The splice would put that `Element` into a content list. Phase
    1 unmarshals it, or the walkers must expect it.
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

     It happens at paragraph level already (10.5).
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
     boxes. `Trees.runItems` has the same gap at paragraph level (10.5).
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
9. **`BookmarksIntegrity.inspectBookmarks`:158** (ALL) counts twins in a `Choice` and a `Fallback` as
   duplicates; it should count per branch. The same happens at run level today (10.5).
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

### 10.5 Defects today, at every level (from the code, not run)

These turned up on the way. They are not block-level, and none waits for this CR.

1. **The anonymiser never analyses a Fallback.**
   - `DmlVmlAnalyzer` has its own `getChildren` (:133-398). It ignores the `McMode.ALL` that
     `Anonymize` (:442) sets, and for an `AlternateContent` falls to reflection, which returns
     `getChoice()`, its only `List` getter.
   - It is also the only code that scrubs `v:imagedata`'s `o:title` (:108), so an image's title in a VML
     Fallback survives anonymisation, and the unsafe-object report misses Fallback content. Text is
     scrambled in every branch (`JaxbGraphWalker`).
   - Fix, one line: `return McSelection.branches(ac, getMcMode())`.
   - CR-021 §8.4 lists it among `getChildrenImpl`'s direct callers; it is not one.
2. **`replaceChildren` drops a `Choice`'s or `Fallback`'s new list** (10.3, item 1). It drops the
   result for an OpenDoPE repeat, condition or component in a run-level control inside a
   paragraph-level branch.
3. **Tracked changes inside a paragraph-level element are not listed, accepted or rejected.**
   `Trees.runItems`:142-155 has no `AlternateContent` case, and `Lister.runLevel` enters an element only
   inside a `w:r`, for text boxes.
4. **`FieldUpdaterSEQ` counts each branch.** Its ALL walk feeds the counter (:120-138), so a SEQ caption
   in a text box, written as a `wps` Choice and a VML Fallback, counts twice and the numbering skips.
5. **`BookmarksIntegrity` counts a text box's twin bookmarks as duplicates** (10.3, item 9).
6. **`OpenDoPEReverter`'s template maps are keyed by control id** (:204-205), so same-id controls in a
   Choice and a Fallback get the same replacement object, put into both lists.

Seen in passing, not to do with `mc`:
- `BindingTraverserNonXSLT.xpathInjectImage`'s fallback path returns null for a control in a header,
  footer, note or text box. Its parent tests stop at `Body`, `Tc`, `SdtContentBlock`, `Tr`, `P` and
  `SdtElement`.
- `Containerization.process`:110-132 never containerizes notes or comments. For notes, its list holds
  the notes, not their paragraphs. For comments, the grouped list is a throwaway copy.
