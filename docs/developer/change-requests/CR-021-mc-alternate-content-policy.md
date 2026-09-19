# CR-021: one policy for `mc:AlternateContent` - kept wherever it occurs, one selection rule, and a traversal that knows whether it reads or writes

Status: PROPOSED 2026-09-19 (written at Jason Harrop's direction after the
docx4j-core-ts parity harness found a text box's paragraphs visited twice;
"we're going to need to be consistent about how we handle mc content").
Drafted with Claude Fable 5.1. Owner: Jason Harrop.

Scope: how docx4j loads, walks, renders and saves ECMA-376 Part 3 markup
compatibility content (`mc:AlternateContent` with its `mc:Choice` and
`mc:Fallback`) in WordprocessingML, and what the schema admits; the selection
rule shared by every consumer; the two traversal modes and the audit of the
51 tree walkers; PresentationML last. Not in scope: `mc:Ignorable` and the
other markup-compatibility attributes (JAXB tolerates them today), or any
change to what the exporters draw for a chosen branch.

## 1. Background: what happens today, measured

Four places decide what a `mc:AlternateContent` becomes, with three rules:

1. **Load** (`org/docx4j/jaxb/mc-preprocessor.xslt`, reached when JAXB
   reports "unexpected element ... AlternateContent" through
   `JaxbValidationEventHandler`): outside a run the element is resolved and
   discarded, taking a Choice whose `Requires` is `v` first, otherwise the
   Fallback, otherwise the first Choice; inside a run (`parent::w:r`) it is
   copied through whole, "the exporters choose the branch" (added in 3.3.8,
   2018-05-26, with `mc:AlternateContent` admitted to `EG_RunInnerContent` in
   `xsd/wml/wml.xsd`).
2. **`TraversalUtil.getChildren`** returns every `mc:Choice` and the
   `mc:Fallback` ("we also want to traverse the fallback"), so every walker
   built on it sees a run's text box twice: once as the `wps` shape, once as
   the VML fallback. Fifty-one main-code files walk the tree that way (§3.3).
3. **The exporters** (`AbstractVisitorExporterGenerator.walkJAXBElements`,
   `docx2fo.xslt`) take one branch: the first Choice whose `Requires` is in
   `docx4j.jaxb.mc.preferChoice`, else the Fallback; the property defaults to
   empty, so the Fallback, as measured for 17.1.0 (25 corpus documents with a
   `wps` Choice: choosing it moved two and both fell).
4. **Save** writes back what survived load: a run's element round-trips with
   both branches, a paragraph's does not.

The population (the 454 originals of the three real-document corpora, parsed):
26 documents carry 64 elements, every one a `w:drawing` Choice with a VML
Fallback; 61 sit in a `w:r` (`Requires="wps"`: shapes and text boxes) and 3
in a `w:p` (`Requires="wpg"`: drawing groups). Those three lose their Choice
at load, so a re-save keeps only the VML group - the same construct, a
different fate by parent. Word's own rule (Part 3, 10.2.1) is the first
Choice whose `Requires` it understands, the Fallback only when it understands
none.

The double visit is two different defects depending on the caller:

- a **reader** (text extraction, numbering emulation, a parity harness,
  anything that counts or measures) must see one branch - the one docx4j
  draws - or a text box's paragraphs count twice and a numbered list inside
  one numbers twice;
- a **mutator** (OpenDoPE binding, field update, mail merge, the anonymiser,
  find and replace) must see every branch, or the saved document says one
  thing in Word 2010 and another in Word 365, since each takes its own branch.

So "consistent" is not one rule for all callers. It is one selection rule,
two named traversal modes, and every caller saying which it is.

## 2. Proposal (for Jason's decision)

1. **Keep the element wherever it occurs.** Load stops resolving
   `mc:AlternateContent` anywhere in WordprocessingML; the JAXB tree holds it,
   as it already holds it in runs. Consumers choose at use.
2. **One selection function**, `org.docx4j.mce.McSelection.select(AlternateContent)`,
   returning the content of the branch docx4j draws: the first Choice whose
   `Requires` names only namespaces in `docx4j.jaxb.mc.preferChoice`, else
   the Fallback's content, else (no Fallback) the first Choice's, with the
   warning the preprocessor gives today. The default stays the Fallback (the
   measured 17.1.0 decision); the load-time preference for `v` goes, since
   it is the Fallback by another name for every element the corpora hold.
3. **`TraversalUtil` reads one branch by default** - `getChildren` returns
   `McSelection.select(ac)` - and offers an explicit all-branches mode
   (`TraversalUtil.visitAllMcBranches(...)` or a flag on the visitor) for
   mutators. Every one of the 51 walkers is classified and the mutators opt
   in (§3.3).
4. **Save is unchanged**: both branches are written, as they are for runs.
5. **PresentationML** (`CT_GroupShape`, `CT_ControlList` already admit the
   element; SlidePart has its own history) follows the same function, last.

## 3. Design

### 3.1 The schema

JAXB keeps an element only where the parent's content model admits it, which
is the whole mechanism: `mc:AlternateContent` is admitted in `wml.xsd` at
`EG_RunInnerContent` alone (line 6867, "added docx4j 3.3.8"), so it survives
in `w:r` and fails everywhere else, and the failure is what routes the part
through the preprocessor. Keeping the element wherever it occurs therefore
means admitting it wherever Word writes it, as CR-018 admitted the w16cex
elements: `<xsd:element ref="mc:AlternateContent"/>` in

- `EG_PContent` (paragraph content: the `wpg` groups measured in `w:p`),
- `EG_BlockLevelElts` (body, cell, text-box content, `w:sdtContent`,
  footnote, header and footer bodies: Word writes block-level
  `mc:AlternateContent` for some SmartArt and chart wrappers; phase 0
  measures whether any corpus document has one),
- `CT_NumPicBullet` (the 2025 note at `wml.xsd` line 14517 already records
  Word writing it there),
- and nothing speculative: phase 0's survey of the corpora and the harness
  probes lists the parents actually seen, and the schema admits those.

`docx4j-generated-objects` regenerates from `xsd/ROOT.xsd`; the TypeScript
objects package regenerates from the same file (CR-018's pattern: tell
objects-ts the CR number and commits when it lands). The `mce` schema itself
(`xsd/mce/markup-compatibility-2006-MINIMAL.xsd`) is unchanged: `Choice` and
`Fallback` already hold `xsd:any`.

The preprocessor keeps its other two jobs (validity fixes, Strict import) and
its `mc:AlternateContent` template becomes a pass-through for WordprocessingML
once the schema admits the element everywhere the survey found it; the
`v`-first branch is deleted, not kept behind a property.

### 3.2 The selection function and its callers

`McSelection.select` is the one place the rule lives; `XSLTUtils.mcPrefersChoice`
(the XSLT hook) delegates to it. Callers: `TraversalUtil.getChildren` (the
default mode), `AbstractVisitorExporterGenerator.walkJAXBElements`,
`docx2fo.xslt`, the HTML exporter's XSLT if it has its own template, the
markdown exporter, `TextUtils.extractText`. The unmarshaller listener's
namespace bookkeeping (`Docx4jUnmarshallerListener.afterUnmarshal`, which
records every Choice's `Requires` so the part can declare `mc:Ignorable` on
save) is unaffected and must stay all-branches: it runs at unmarshal, before
any selection.

### 3.3 The two traversal modes and the audit

`TraversalUtil` gains a mode: READ (default; one branch per
`McSelection`) and ALL (every Choice and the Fallback, the behaviour of
today). Phase 0 classifies each of the 51 walkers; the working reading, to be
confirmed file by file:

| mode | walkers |
|---|---|
| ALL (mutate, or must see every reference) | `anon/Anonymize`; `model/datastorage/*` (BindingHandler, the three BindingTraversers, BindingTraverserCommonImpl, OpenDoPEHandler, OpenDoPEReverter, UpdateXmlFromDocumentSurface, BookmarkRenumber, the three `migration/*`); `model/fields/*` (FieldUpdater, FieldUpdaterSEQ, FORMTEXTMerger, MailMerger, MailMergerWithNext); `model/bookmarks/BookmarksIntegrity`; `fonts/FontsAnalysis` (a face used only in a fallback must be reported and embedded); `convert/out/common/preprocess/*` (AcceptTrackedChanges, BookmarkMover, Containerization, FieldsCombiner, ParagraphStylesInTableFix - they rewrite the copy the exporter will walk, so every branch must be rewritten); `openpackaging/parts/JaxbXmlPartAltChunkHost`; `openpackaging/parts/WordprocessingML/MainDocumentPart`; `openpackaging/parts/DrawingML/DiagramDataPart`, `DiagramDrawingPart`; `toc/*` (TocGenerator, TocIntoSdt, TocFinder, the switches: they edit) |
| READ (one branch) | `convert/out/common/AbstractVisitorExporterGenerator` and `AbstractVisitorExporterDelegate` (already one branch); `convert/out/fo/*` and `convert/out/html/*` (the visitor generators and delegates, FOPAreaTreeHelper, HtmlExporterNonXSLT, ListsToContentControls); `convert/out/common/preprocess/PageNumberInformationCollector`; `convert/out/common/wrappers/ConversionSectionWrapperFactory`; `convert/out/common/writer/AbstractTableWriterModel`; `model/table/TableModel`; `model/structure/DocumentModel`, `HeaderFooterPolicy`; `model/pagination/Paginate`; `Docx4J` (its helpers) |

Where a walker both reads and writes (`FieldUpdater` reads results and
writes them), ALL. The ports' harness and `TextUtils` are READ. The
`DrawingPropsIdTracker` and the namespace bookkeeping are fed at unmarshal
and need no traversal.

### 3.4 Exporters

No behaviour change for a branch once chosen: the exporters already take one
branch by the same rule. What changes is that a paragraph-level element
(`wpg`) reaches them at all, and takes the Fallback by default as a run's
does; `preferChoice=wps wpg` is then the one switch to draw the DrawingML
side of both.

### 3.5 Save

Unchanged. Both branches are marshalled; the `mc:Ignorable` declaration on
the part comes from the unmarshal-time bookkeeping as today. A document loaded
and saved by docx4j is, for this element, byte-equivalent in structure to its
input wherever the schema now admits it.

## 4. Phases

0. **Survey and probes.** Parse every part of the corpora and the harness
   probes for `mc:AlternateContent` by parent element and first Choice
   `Requires` (the §1 numbers, extended to headers, footers, footnotes,
   glossary and numbering parts); classify the 51 walkers file by file
   (§3.3, confirmed by reading each); two probes for the harness with a Word
   golden: a text box with a VML fallback holding a numbered list, and one
   holding a bound content control, so numbering, binding and rendering are
   each measured against Word. Written to this CR. No code.
1. **The selection function and the traversal modes.** `McSelection`;
   `TraversalUtil` READ default and ALL mode; the exporters and `XSLTUtils`
   delegating; the audit applied (each mutator opts into ALL, with a one-line
   comment saying why). **Gate**: core-tests, export-fo-tests, the OpenDoPE
   suites; the corpora and probes on both renderers identical (rendering was
   already one-branch); the numbered-text-box probe's numbering after the box
   now Word's; the ports' harness goldens (docx4j-core-ts) regenerate with the
   text box's paragraphs once.
2. **Keep the element wherever it occurs.** The schema additions of §3.1 for
   the parents phase 0 found; XJC regeneration; the preprocessor's template
   made a pass-through for WordprocessingML; `JaxbValidationEventHandler`'s
   trigger no longer reached for these parents. **Gate**: the 26 documents
   (and any phase 0 finds) load with the element in place, render as before
   on both renderers, and round-trip with both branches (a structural diff of
   input and output XML for the element); objects-ts told the commits.
3. **PresentationML** by the same function (SlidePart's fallback handling
   revisited), and `docx4j.jaxb.mc.preferChoice` documented as the one switch
   for both formats.
4. **Follow-through**: the Getting Started note on markup compatibility, and
   the CHANGELOG under "Loading" and "API".

## 5. Risks and open questions

- **`TraversalUtil` is widely shared code**: changing its default touches
  every walker, including users' own callbacks, which today see both
  branches. The READ default is the right default for a library that renders
  and extracts, and users who mutate through their own callbacks need the
  ALL mode named in the CHANGELOG and in `TraversalUtil`'s javadoc. This is
  the one behaviour change a user could notice; phase 1's message says so.
- **Schema additions must match what Word writes**, not what the standard
  allows; phase 0's survey decides the list, and the README of CR-018 is the
  precedent for the regeneration and the objects-ts hand-off.
- **Memory**: keeping both branches everywhere is what runs already do; the
  three paragraph-level elements measured are not a cost.
- **The `v` preference at load** exists for some reason lost to history (a
  document whose Choice was VML and whose Fallback was worse?); phase 0 asks
  the git log and keeps a note, but the corpora hold no such element.
- Open: whether `preferChoice` should default to `wps wpg` once the
  fidelity work can measure the DrawingML side against Word again (the
  17.1.0 measurement was two documents, both falling by less than 0.6pt).

## 6. Effort (rough)

Phase 0 a day; phase 1 two to three days including the audit and the gate;
phase 2 a day plus the regeneration round; phase 3 a day; phase 4 hours.

## 7. References

- ECMA-376 Part 3 (Markup Compatibility and Extensibility), 10.2.1.
- `xsd/wml/wml.xsd` lines 35 and 6860-6872 (the 3.3.8 admission in runs),
  14512-14520 (the numPicBullet note); CR-018 (schema gaps and regeneration).
- `org/docx4j/jaxb/mc-preprocessor.xslt` lines 302-368;
  `JaxbValidationEventHandler.UNEXPECTED_MC_ALTERNATE_CONTENT`;
  `TraversalUtil.getChildren` (the AlternateContent branch);
  `AbstractVisitorExporterGenerator.walkJAXBElements`; `docx2fo.xslt`'s
  `mc:AlternateContent` template; `XSLTUtils.mcPrefersChoice`.
- `docx4j.jaxb.mc.preferChoice` in `docx4j-samples-resources/.../docx4j.properties`.
- The docx4j-core-ts parity harness report of 2026-09-19 (the double visit).
