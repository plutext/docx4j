# CR-021: one policy for `mc:AlternateContent` - kept wherever it occurs, one selection rule, and a traversal that knows whether it reads or writes

Status: DONE 2026-09-19 - all four phases (§8.7 to §8.10), with a
SpreadsheetML follow-up (§8.8) and the a14:m schema fix Jason's PowerPoint
check found (§8.9). Open items for other CRs are listed in §8.6.
Proposed 2026-09-19 (written at Jason Harrop's direction after the
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
   discarded - the first Choice whose `Requires` is in
   `docx4j.jaxb.mc.preferChoice` (empty by default, so none), else the
   Fallback's content, else nothing (a warning, and the element is dropped:
   the `copy-of` of the first Choice is commented out); inside a run
   (`parent::w:r`) it is copied through whole, "the exporters choose the
   branch" (added in 3.3.8, 2018-05-26, with `mc:AlternateContent` admitted
   to `EG_RunInnerContent` in `xsd/wml/wml.xsd`). A VML-first branch
   (`mc:Choice[@Requires='v']`) has been in the stylesheet since 2012-12-29
   (2088538f1) but was committed already commented out and has never run; the
   2013-07-07 note beside it points at `SlidePart`'s reason (a re-marshalled
   VML branch lost its `xmlns:v` declaration and PowerPoint 2010 declared the
   file corrupt). The stylesheet's own comment "wps by default" is stale: the
   property's default is empty (`XSLTUtils.mcPreferredChoiceRequires`).
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
2. **One selection function**, `org.docx4j.jaxb.McSelection.select(AlternateContent)`
   (not `org.docx4j.mce`: that package is the generated one in
   docx4j-generated-objects, and a second module cannot share it under JPMS),
   returning the content of the branch docx4j draws: the first Choice whose
   `Requires` names only namespaces in `docx4j.jaxb.mc.preferChoice`, else
   the Fallback's content, else (no Fallback) the first Choice's, with the
   warning the preprocessor gives today. The default stays the Fallback (the
   measured 17.1.0 decision; §8.6 adds a second reason); the never-live
   VML-first branch is deleted with the rest of the template (§3.1).
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
`docx2fo.xslt`, `docx2xhtml-core.xslt` (which until 17.1.1 always took the
Fallback, ignoring the property), the markdown exporter,
`TextUtils.extractText`. The unmarshaller listener's
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
- **The `v` preference at load** - answered by phase 0 (§1 item 1, §8.2): it
  never ran, so nothing depends on it.
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

## 8. Progress

### 8.1 Phase 0: the survey (2026-09-19)

Every XML part of every document was parsed (lxml; the script is not in the
repository), and each `mc:AlternateContent` recorded by part, parent,
grandparent, the `Requires` of its Choices, the first child of the first
Choice and of the Fallback, and nesting. Four populations:

| population | documents | with the element | elements |
|---|---|---|---|
| the three real-document corpora (originals) | 454 | 26 | 64 |
| the harness probes (before this phase) | 146 | 1 (`numbering-stories`) | 1 |
| Word's re-saves of the corpora (the goldens' basis) | 453 | 26 | 64 |
| documents in this repository (test resources, samples, docs) | 235 | 19 | 20 |

By parent and part, corpora originals (the re-saves agree element for element):

| parent | grandparent | part | first Choice `Requires` | Choice holds | Fallback holds | n |
|---|---|---|---|---|---|---|
| `w:r` | `w:p` | document | `wps` | `w:drawing` (`wp:anchor`, every one a `wps:txbx` text box) | `w:pict` (VML) | 33 |
| `w:r` | `w:p` | footer | `wps` | as above | `w:pict` | 15 |
| `w:r` | `w:p` | header | `wps` | as above | `w:pict` | 13 |
| `w:p` | `w:tc` (2), `w:body` (1) | document | `wpg` | `w:drawing` directly under the Choice (no `w:r`) | `w:r`/`w:drawing` (a picture) | 3 |

Nothing nested; nothing in footnotes, endnotes, comments, the glossary or
numbering parts of the corpora; no `mc:AlternateContent` at block level
(body, cell, `w:sdtContent`) anywhere in the 835 documents. The three
`w:p`-level elements are one document (`12_en-US_fields1_tbl_4069`, app.xml
says Microsoft Office Word), and Word's re-save of it keeps them as children
of `w:p`, rewriting the Choice's bare `w:drawing` into a `w:r`/`w:drawing`:
so `w:p` is a parent Word writes, and §3.1's `EG_PContent` admission stands.

The repository's own documents add two parents the corpora lack:

- `w:numPicBullet` in `word/numbering.xml`
  (`numPicBullet-word365-AlternateContent.docx`): Choice `Requires="v"`
  holding `w:pict`, Fallback holding `w:drawing` - the orientation reversed
  from every other element measured (VML is the preferred branch, DrawingML
  the fallback). `NumPicBulletTest` pins today's behaviour: the preprocessor
  keeps the Fallback, so `w:drawing` reaches JAXB and the Choice is lost.
  §3.1's `CT_NumPicBullet` admission stands.
- `w:r` in `word/comments.xml` (`2010-mcAlternateContent-MDP.docx` and its
  sibling): already admitted, runs being runs in every story.
- `c:chartSpace` in chart parts (`chart.docx`, `strict-chart.docx`,
  `loadAndSave.docx`): `Requires="c14"`, a DrawingML chart's style. Out of
  this CR's scope (not WordprocessingML; the chart part has its own JAXB
  context) - noted so the PresentationML phase knows the shape.
- `wpg` groups inside `w:r` (`textbox_alternates_contrived.docx`, the
  OpenDoPE boolean semantics doc): admitted already, kept at load.

**Schema list for phase 2**, decided by the survey: `EG_PContent` and
`CT_NumPicBullet`. Not `EG_BlockLevelElts`: no document of the 835 has a
block-level element, and §3.1 said nothing speculative.

### 8.2 What load does today, measured

Four documents loaded through `WordprocessingMLPackage.load` with every
document, header, footer, numbering and comments part unmarshalled and
re-marshalled:

| document | result |
|---|---|
| `12_en-US_fields1_tbl_4069` (three `w:p`-level `wpg`) | each resolved to its Fallback ("Selecting w:r"): the `wpg` groups are gone after load, as §1 said |
| `numPicBullet-word365-AlternateContent` | resolved to the Fallback `w:drawing`; the VML Choice is gone |
| `textbox_alternates_contrived` (`wpg` in `w:r`) | kept whole: one element, both branches |
| `2010-mcAlternateContent-in-header` (`wps` in `w:r`) | kept whole in the header part |

(The harness's "re-save" that Word's goldens are cut from does not unmarshal
untouched parts, so the re-saves of §8.1 still held the `w:p`-level
elements for Word to rewrite - which is how Word's own shape for them was
observed.)

The git history question of §5 is answered in §1 item 1: the VML-first branch
never ran.

### 8.3 The walker audit

Seventy-two main-code files in docx4j-core name `TraversalUtil`; five are
the framework itself (`TraversalUtil`, `AbstractTraversalUtilVisitorCallback`,
`CompoundTraversalUtilVisitorCallback`, `SingleTraversalUtilVisitorCallback`,
`TraversalUtilVisitor`), three only mention it (`Preprocess`,
`WordprocessingMLPackage`, `TocHelper`), and twelve are finder callbacks
whose mode is their caller's (below). Outside core: three in docx4j-docx-anon,
three walkers in docx4j-export-fo (`LayoutMasterSetBuilder`, `PdfConversion`
and `XSLFOExporterNonXSLT` mention it in comments only), none in
docx4j-markdown (`WmlToMarkdown` recurses over `getContent()` itself and has
no `AlternateContent` case at all: a text box is dropped from the markdown
whichever branch is meant). Each file read; the classification:

**READ** (one branch, the one docx4j draws):

| file | what it walks |
|---|---|
| `convert/out/common/AbstractVisitorExporterGenerator`, `AbstractVisitorExporterDelegate` | already one branch (`walkJAXBElements` override); unchanged, delegates to `McSelection` |
| `convert/out/html/HTMLExporterVisitorGenerator`, `HTMLExporterVisitorDelegate`, `HtmlExporterNonXSLT` | the HTML visitor pathway, through the generator above |
| `convert/out/fo/FOExporterVisitorGenerator`, `FOExporterVisitorDelegate` (export-fo) | the FO visitor pathway, through the generator above |
| `convert/out/fo/FOPAreaTreeHelper` - its `SectPrFinder` walk of the header/footer package | reads section properties for the area-tree pass |
| `convert/out/common/wrappers/ConversionSectionWrapperFactory` | collects `w:sectPr` and block sdts for the section model of the copy the exporter draws |
| `convert/out/common/preprocess/PageNumberInformationCollector` | reads page-number fields |
| `convert/out/common/writer/AbstractTableWriterModel`, `model/table/TableModel` | `TrFinder`/`TcFinder` over one table for the table writer |
| `convert/out/common/AbstractWmlConversionContext.hasBookmark` | `RangeFinder` for the bookmark names the drawn branch may cite |
| `model/structure/DocumentModel`, `HeaderFooterPolicy` | section structure; "is this header empty" |
| `model/pagination/Paginate` - `bodyParagraphs`, the paraId map, `insertMarker` | pairs paragraphs with an area tree that was rendered from one branch |
| `Docx4J` helpers other than `FindContentControlsVisitor` | - |
| `TextUtils.extractText` (not a TraversalUtil walker: a SAX marshal) | today emits both branches' text (measured on the probes); a `McSelection` caller in phase 1 |
| `WmlToMarkdown` (docx4j-markdown) | as above; a `McSelection` caller in phase 1 |
| the ports' parity harness | READ |

**ALL** (mutates, or must see every reference):

| file | why |
|---|---|
| `anon/Anonymize`, `DmlVmlAnalyzer`, `ScrambleText` (docx4j-docx-anon) | rewrites text in every branch, or a Word 2010 reader sees the original |
| `model/datastorage/BindingHandler` with `BindingTraverserNonXSLT`, `BindingTraverserStAX`, `BindingTraverserCommonImpl` (`findBlip`); `BindingTraverserXSLT` walks by XSLT, all branches by nature | binding results must land in every branch |
| `model/datastorage/OpenDoPEHandler` (`ShallowTraversor`, `DeepTraversor`, `TableObjectFinder`), `OpenDoPEHandlerComponents`, `OpenDoPEReverter`, `UpdateXmlFromDocumentSurface`, `BookmarkRenumber`, `CustomXmlDataStoragePartSelector`, `migration/FromFormText`, `FromMergeFields`, `FromVariableReplacement`, `VariablePrepare` | conditions, repeats, reverts and migrations edit; the selector and the surface reader must see a control in either branch (`UpdateXmlFromDocumentSurface`: if the branches disagree the last wins - the bound-sdt probe's re-save shows what Word itself keeps) |
| `model/fields/FieldUpdater`, `seq/FieldUpdaterSEQ`, `formtext/FORMTEXTMerger`, `merge/MailMerger`, `merge/MailMergerWithNext` | write field results and merged text |
| `model/bookmarks/BookmarksIntegrity` | checks and repairs bookmark pairs wherever they are |
| `fonts/FontsAnalysis` | a face used only in a fallback is still used and still embedded |
| `convert/out/common/preprocess/AcceptTrackedChanges`, `BookmarkMover`, `Containerization`, `FieldsCombiner`, `ParagraphStylesInTableFix`; `convert/out/html/ListsToContentControls`; `convert/out/fo/FOPAreaTreeHelper.FloatingDrawingRemover` | rewrite the exporter's copy; rewriting the drawn branch alone would do, every branch is the uniform, safe choice |
| `model/pagination/Paginate` - `removeLastRenderedPageBreaks`, the ids-in-use set | removes marks everywhere; an id in any branch is taken |
| `Docx4J.FindContentControlsVisitor` | the first bound control decides the store item, whichever branch holds it |
| `openpackaging/packages/ProtectDocument` | strips formatting |
| `openpackaging/parts/JaxbXmlPartAltChunkHost` | converts `w:altChunk` in place |
| `openpackaging/parts/WordprocessingML/MainDocumentPart.FontAndStyleFinder` | fonts and styles in use, for saving them |
| `openpackaging/parts/DrawingML/DiagramDataPart`, `DiagramDrawingPart` | rewrite model ids (DrawingML, no `mc` expected; mutators regardless) |
| `toc/TocGenerator`, `TocIntoSdt`, `TocFinder`, `switches/CSwitch`, `SwitchProcessor` | replace the TOC |

**Callbacks whose mode is the caller's** (they collect; the invocation
decides): `finders/ClassFinder`, `CommentFinder`, `InstrTextFinder`,
`RangeFinder`, `SdtFinder`, `SectPrFinder`, `SectPrFindFirst`, `TableFinder`,
`TcFinder`; `utils/AltChunkFinder`; `model/fields/ComplexFieldLocator`,
`SimpleFieldLocator`.

Against §3.3's working reading: `Paginate` is mixed (two ALL walks, three
READ); `ListsToContentControls` and `FOPAreaTreeHelper`'s remover move to
ALL (they rewrite the copy); `Docx4J`'s content-control finder and
`CustomXmlDataStoragePartSelector` are ALL; `MainDocumentPart`'s finder is
ALL for the reason given. Counting invocations rather than files: 52 walks
in core, 5 in export-fo, 3 in the anonymiser.

### 8.4 Where the mode has to live (a design consequence for phase 1)

Every walker reaches the branch list through one static method,
`TraversalUtil.getChildrenImpl(Object)`: `CallbackImpl.getChildren`
delegates to it, every custom `walkJAXBElements` override (fourteen of them)
calls `getChildren` or `getChildrenImpl`, and eleven classes call the static
directly (`OpenDoPEHandler.ShallowTraversor`, which implements `Callback`
itself; `BindingTraverserCommonImpl`; `BindingTraverserNonXSLT`;
`HeaderFooterPolicy`; `AcceptTrackedChanges`; `FOPAreaTreeHelper`;
`DiagramDataPart`; `DiagramDrawingPart`; `TocFinder`; `TocIntoSdt`;
`DmlVmlAnalyzer`). So phase 1's mode is:

- an overload `getChildrenImpl(Object, McMode)`, the one-argument form
  meaning READ;
- a field on `CallbackImpl` (default READ) that its `getChildren` passes,
  set by the `TraversalUtil` constructor and `visit` overloads that take a
  mode - so a finder inherits the mode of the walk it is used in, which is
  what the third group above needs;
- the eleven direct callers passing the mode explicitly, each with the
  one-line comment §4 phase 1 asks for.

`TraversalUtilVisitor`-based walks (`Compound`/`SingleTraversalUtilVisitorCallback`)
get the field through `AbstractTraversalUtilVisitorCallback`.

### 8.5 The two probes

Cut in `docx4j-layout-fidelity` (`Corpus.java`; `Doc.textBox` gained a form
whose two branches are given separately), generated, on the share
(`corpus.txt` 148, manifest `probes=148`); goldens cut by Jason 2026-09-19
and read below. Both branches DIFFER in each box, so the drawn branch is
read from the golden's text.

- `mc-textbox-branches-numbered`: three body items of list A (`w:num` 80);
  a text box whose wps Choice holds two items of A and one of a list B
  (`w:num` 81) seen nowhere else before it, and whose VML Fallback holds
  three of A and two of B; three body items of A after, then one of B.
  Measures which branch Word draws, how the box's items are numbered, and
  whether the body's count after the box is advanced by the box at all,
  once or twice. `numbering-stories`' golden already says a text box is its
  own story (box items 1-3, body continues 4-6), so the expected golden is
  the wps text with A 1, 2 and B 1 in the box, then 4, 5, 6 and B 1 after it.
- `mc-textbox-branches-bound-sdt`: a custom XML part (`root/name` = FRESH
  NAME, `root/box` = FRESH BOX, store item id fixed); a plain-text control
  bound to `root/name` in the body saying STALE NAME; a text box whose
  Choice holds a control bound to `root/box` saying STALE CHOICE and whose
  Fallback holds one saying STALE FALLBACK; a control box with no binding
  whose branches say CHOICE TEXT and FALLBACK TEXT. Measures which branch
  Word draws, whether it refreshes bound text from the part, and (in the
  re-save on the share) what Word writes into the branch it did not draw.

**Word's answers** (goldens `mc-textbox-branches-numbered.pdf`,
`mc-textbox-branches-bound-sdt.pdf`, and the re-saves on the share):

- Word 365 draws the wps Choice in every box: the numbered box shows the
  Choice's items, the bound box FRESH BOX under the Choice's label, the
  control box CHOICE TEXT. The Fallback's text appears nowhere.
- Numbering: the box's list A is numbered 1, 2 and its list B 1; the body
  continues 4, 5, 6 after the box and its list B item is 1. A text box is
  its own story (as `numbering-stories` said) and the undrawn branch counts
  for nothing - so a READ walker that takes the drawn branch, and a
  story-aware emulator, reproduce Word; a walker that counts both branches
  cannot.
- Binding: Word refreshed every bound control it drew from the part (FRESH
  NAME in the body, FRESH BOX in the box).
- **What Word writes into the branch it did not draw** (the re-save):
  - the bound box, which Word touched (the refresh changed its text): the
    Fallback was REGENERATED from the Choice - it now holds the Choice's
    paragraph, label P03 and all, with the same bound control saying FRESH
    BOX; the authored Fallback paragraph (P04, STALE FALLBACK) is gone;
  - the control box and the numbered box, which Word did not touch: the
    Fallback is VERBATIM, byte for byte the authored one - the control box
    still says FALLBACK TEXT, the numbered box's Fallback still holds its
    five items;
  - and verbatim means stale: Word renumbered the lists on save (`w:num`
    80 and 81 became 1 and 2 in the numbering part and in every paragraph
    it processed, the Choice's included), but the untouched Fallback still
    names 80 and 81, which no longer exist. Word treats an undrawn branch
    as opaque bytes, references and all.

  So Word's rule is: the drawn branch is authoritative; a shape it edits
  gets its fallback regenerated; a shape it does not edit keeps its
  fallback untouched even when that leaves the fallback dangling. For
  docx4j this settles 8.6 item 4 and adds two consequences, 8.6 items 6
  and 7.

docx4j today, measured on the two:

| pathway | result |
|---|---|
| `TraversalUtil` (a `CallbackImpl` counting `w:p`) | 18 paragraphs on the numbered probe (10 body + 3 Choice + 5 Fallback): both branches, the defect the ports' harness reported. One branch would give 13 or 15. |
| `TextUtils.extractText` | both branches' text |
| PDF via FO, default (Fallback) | draws the VML box; the box's list A numbered 1-3 as its own story, the body continues 4, 5, 6 - the renderer and the numbering emulator are already one-branch and story-aware; nothing to fix there for numbering |
| PDF via FO, `preferChoice=wps` | the box's paragraphs are not drawn at all: an INLINE `wps` text box (`wp:inline`/`wps:txbx`) has no FO output. The corpus population is anchored (`wp:anchor`, all 64), where 17.1.0 measured two documents falling; a second, sharper reason the default stays the Fallback until the FO exporter draws inline wps boxes (8.6) |
| HTML, default (Fallback) | the box's paragraphs are not drawn: `WordXmlPictureE10` treats the Fallback's `w:pict`/`v:shape` as a picture ("Couldn't find imageData") and its `v:textbox` content is lost - on `numbering-stories` too, so this is today's HTML for every Word 2010+ text box |
| HTML, `preferChoice=wps` | the box's paragraphs are drawn |

### 8.6 Findings for the later phases

1. **HTML drops the VML fallback text box** (8.5): the HTML visitor's
   `w:pict` handling has no `v:textbox` case. Either the HTML exporter draws
   `v:textbox` content, or `preferChoice` defaults to `wps` for HTML alone.
   Phase 1 or a CR of its own; not phase 0's to fix.
2. **FO draws no inline wps text box** when the wps branch is preferred
   (8.5). Until it does, `preferChoice=wps` is unsafe as a default for PDF;
   §5's open question waits on this and on the anchored measurement.
3. **`WmlToMarkdown` and `TextUtils`** have no `mc` handling of their own
   (8.3); both become `McSelection` callers in phase 1.
4. **`UpdateXmlFromDocumentSurface`** under ALL sees two bound controls for
   one XPath when a text box carries a binding. Word's re-save (8.5) makes
   the drawn branch authoritative and regenerates the fallback from it, so
   "last wins" is wrong in general: the surface reader should take the
   value from the branch `McSelection` selects (READ), and only fall back
   to the other branch when the selected one has no control. Phase 1
   classifies it READ-with-fallback rather than ALL.
5. The preprocessor's stale "wps by default" comment goes with the template
   in phase 2.
6. **A Word-saved document's Fallback can be stale** (8.5): its numbering
   ids, and by the same token its bookmark and comment ids, may name things
   the drawn branch's renumbering removed. Readers of the Fallback (the
   default `preferChoice`, so every docx4j render) must tolerate a `w:numId`
   with no `w:num` (since the phase 1 follow-up of 2026-09-19,
   `Emulator.numRefFor` answers "no w:num for numId N" and `getNumber`
   returns null, as for numId 0; before, an empty result) and a dangling
   bookmark or comment reference; the ports' READ walkers need the same
   tolerance when they select the Fallback (core-ts answers the same
   reason).
7. **Mutators under ALL do better than Word**, not merely as well: renumbering
   both branches keeps the Fallback consistent where Word leaves it
   dangling. That is the right outcome and costs nothing; it is recorded so
   that a future "match Word exactly" reading does not undo it.
8. **The anonymiser's `DmlVmlAnalyzer`** (found applying the audit) overrides
   `getChildren` with its own list of types and has no `AlternateContent`
   case, so the pictures inside a Word 2010+ text box's branches may never
   reach its shape inspection; `ScrambleText`, which inherits the standard
   walk, does reach them in ALL. One for CR-019 (the anonymiser
   guarantees), not this CR.
9. **`Containerization`** has no `TraversalUtil` walk at all (its comment
   mentions one); it recurses over content itself and so sees the
   `AlternateContent` object as an opaque child. It groups block-level
   content, which is not inside a run, so nothing is lost today.
10. **A kept branch is lossless only if the schema binds its whole content**
   (found by Jason's PowerPoint check in phase 3, §8.9): resolving to a
   Fallback that is a picture loses nothing a picture can show, while
   keeping a Choice whose content the unmarshaller partly drops loses that
   part silently. Every parent admitted by this CR therefore needs its
   branch content checked against the corpus - the equation case is fixed;
   ink (`a14`/`a16` content parts), `w14`/`w15` extensions inside kept
   `wps` shapes and chart `c14` styles are the next candidates, and the
   preprocessor's warning for an unexpected element inside a kept branch is
   the signal to watch in the logs. (Chart `c14` styles, and the spreadsheet
   drawing hosts this CR did not reach: taken up by CR-024, done 2026-09-20 -
   kept whole, `c14:style` typed, the anchors DOM.)

### 8.7 Phase 1 (2026-09-19, at Jason's go)

Coded:

- `org.docx4j.jaxb.McMode` (READ, ALL) and `org.docx4j.jaxb.McSelection`
  (`PROPERTY`, `preferredChoiceRequires`, `prefersChoice`, `selectedBranch`,
  `select`, `branches`), with the 17.1.0 measurement and §8.5's inline-box
  finding in its javadoc.
- `TraversalUtil`: `getChildrenImpl(Object)` is READ; `getChildrenImpl(Object,
  McMode)`; `CallbackImpl` carries the mode (`getMcMode`/`setMcMode`, default
  READ) and its `getChildren` passes it; a constructor and five `visit`
  overloads take a mode. The class javadoc names the behaviour change.
- The audit applied (§8.3): every ALL site carries a `// CR-021: ALL - why`
  line (46 sites in core, 3 in export-fo's `FOPAreaTreeHelper`, 2 in the
  anonymiser); the four classes implementing `Callback` directly
  (`OpenDoPEHandler.ShallowTraversor`, the anonymous callbacks of
  `DiagramDataPart` and `DiagramDrawingPart`) pass ALL in their own
  `getChildren`; `UpdateXmlFromDocumentSurface` stays READ with the reason
  from §8.5 in a comment.
- Callers of the one rule: `AbstractVisitorExporterGenerator` (through
  `selectedBranch`), `XSLTUtils` (delegating), `docx2xhtml-core.xslt` (now the
  same template as `docx2fo.xslt`; it always took the Fallback before),
  `TextUtils.TextExtractor` (a SAX stream: skips the branches not selected;
  since the follow-up below it applies the last resort too), `WmlToMarkdown`
  (draws a `w:drawing` from the selected branch).
- `McSelectionTest` (docx4j-core-tests): the property rule, the branch
  selection, `branches` by mode, the traversal default and ALL, the visit
  overloads and `setMcMode`, one-branch text extraction.
- CHANGELOG under "Markup compatibility (CR-021)".

**Gate (passed 2026-09-19 12:29):**

| step | result |
|---|---|
| docx4j-core, docx4j-docx-anon, docx4j-markdown, docx4j-export-fo | clean-install, no warnings of note |
| targeted suites (McSelectionTest, AlternateContentPreprocessorTest, NumPicBulletTest, every `model.datastorage` test, numbering, TOC, pagination, fields, bookmarks) | 222 tests, 0 failures |
| docx4j-core-tests, the whole suite | 1189 tests, 0 failures, 11 skipped |
| docx4j-export-fo-tests, the whole suite | 669 tests, 0 failures, 8 skipped |
| the 146 probes, Apache FOP and the fork renderer | every row identical to b53-batch49 on both |
| the three corpora (192, 158, 104 rows incl. totals), Apache FOP and the fork | every row identical to b53-batch49 on both; means 0.9309 / 0.9127 / 0.9394 unchanged |

So rendering was one-branch already, as §1 said, and the change is confined
to the walkers. The two new probes score 0.73 (numbered) and 0.40
(bound-sdt) against Word by design: docx4j draws the Fallback where Word
drew the Choice, and does not refresh a bound control at render where Word
did. They measure the `preferChoice` default and binding refresh (§5, §8.6
items 1 and 2), not this phase, and are class 3 until those are decided.
The b54 score directories were identical to the baseline and were deleted;
b53-batch49 remains the baseline for the next batch.

**Hand-off:** the docx4j-core-ts peer told the commit range; its parity
goldens regenerate with a text box's paragraphs visited once. The Python
port follows.

### 8.8 Phase 2 (2026-09-19, at Jason's go, after the phase 1 push)

Coded:

- `xsd/wml/wml.xsd`: `<xsd:element ref="mc:AlternateContent"/>` in
  `EG_PContent` (so a paragraph's content list may hold the element, as a
  run's may since 3.3.8) and in `CT_NumPicBullet`'s choice beside `pict` and
  `drawing`; the header's change log line. Regenerated: `P.getContent()`
  admits `AlternateContent` through an `@XmlElementRef`;
  `Numbering.NumPicBullet` gains `getAlternateContent()`.
- `mc-preprocessor.xslt`: the retain branch now covers every parent the
  schema admits (`w:r`, `w:p`, `w:numPicBullet`), logging the parent. For
  any other parent - none in the 835 documents of the survey - the element
  is still resolved to the one branch docx4j draws (McSelection's rule via
  `XSLTUtils.mcPrefersChoice`, else the Fallback), now with a warning that
  names the parent so the schema can be extended to it. §3.1 said "a
  pass-through"; a pass-through for an unadmitted parent would fail the
  part's unmarshal, so the resolution stays as the last resort. The
  never-live VML-first branch is deleted.
- `JaxbValidationEventHandler` unchanged: its trigger is simply no longer
  reached for these parents.
- Tests: `AlternateContentKeptTest` (a paragraph-level `wpg` element in
  the shape of Word's re-save is kept with both branches and marshals with
  both; the Word 365 numPicBullet document keeps the element, neither
  branch is unwrapped into the bullet, and it round-trips);
  `NumPicBulletTest` updated to the kept element (its concern, the bullet
  never saved empty, kept).
- CHANGELOG under "Markup compatibility (CR-021)", the loading entry.

**Round trip** (the 26 corpus documents with the element, 4069 among them,
plus the 11 repository documents: 37 in all): every document loads with
every WordprocessingML part unmarshalled, not one preprocessor warning
(so every element was admitted by the schema), and saves. Comparing every
`mc:AlternateContent` in the input with its twin in the output, part by
part: the same count everywhere, the same parent and `Requires`, no
element lost or gained in any branch. Three elements (4069's paragraph-level
groups) have their `wps:wsp` children re-ordered to schema order, as Word's
own re-save does. The attribute differences are VML's pre-existing JAXB
round-trip losses, the same a run-level element always had: `w14:anchorId`
on `v:shape`/`v:shapetype` (41), `o:gfxdata` on `v:oval`/`v:rect` (6) and
re-encoded on `v:shape` (61), `o:spt` written as a float (34) - none of
them this phase's doing; recorded as a VML round-trip gap for the anonymiser
and diff work to know about. The saved parts declare the namespaces the
kept branches use (`xmlns:v` on numbering.xml, `xmlns:wpg` on 4069's
document.xml) and keep their `mc:Ignorable`.

**Word-open check** (Jason, 2026-09-19): the two re-saves on the share
(`cr021-phase2-check`: 4069 and the numPicBullet document) open in Word
without a repair prompt; 4069 shows its one visible group, the
"Photograph / PP size" box, as Word's golden of the original does (the
other two groups are 10 by 7 point rectangles in table cells).

**Gate (passed 2026-09-19 13:10):**

| step | result |
|---|---|
| docx4j-generated-objects regenerated, docx4j-core rebuilt | clean |
| docx4j-core-tests, the whole suite | 1193 tests, 0 failures, 11 skipped |
| docx4j-export-fo-tests, the whole suite | 669 tests, 0 failures, 8 skipped |
| the 146 probes, Apache FOP and the fork renderer | every row identical to b53-batch49 on both |
| the three corpora, Apache FOP and the fork | every row identical to b53-batch49 on both (4069, whose three paragraph-level groups are now kept and drawn from their Fallback, among them) |

The b55 score directories were identical to the baseline and were deleted;
b53-batch49 remains the baseline.

**Hand-off:** objects-ts told the commit (the two `wml.xsd` admissions and
the regeneration they need); core-ts told (its typed model gains the
element in `P.getContent()` and `NumPicBullet`).

**SpreadsheetML, asked by Jason after the gate** ("does SpreadsheetML not
use mc:AlternateContent?"). It does: the repository's 7 xlsx carry it in
`xl/workbook.xml` (Excel's `x15` absPath, a Choice with no Fallback; 4
workbooks), in chart parts (`c14` style, 3) and once in a spreadsheet
drawing (`xdr:oneCellAnchor`, `a14`). What docx4j did with them, measured:
`sml.xsd` has admitted the element in `CT_Workbook` since 8.1.1, so a
workbook that unmarshals cleanly keeps it - but a workbook that reaches the
preprocessor for any other reason (the two non-strict test workbooks do,
for an `xr:revisionPtr` the schema lacks) had it dropped ("Missing
mc:Fallback! Dropping"), because the retain branch knew only `w:r`. The
retry path in `JaxbXmlPartXPathAware` applies the WordprocessingML
stylesheet to every package format, so that stylesheet is the shared
resolver and its retain list must name every admitted parent. Fixed in the
same commit series: `parent::x:workbook` retained; the `sml.xsd` admission
made `minOccurs="0"` (it was `required` in the generated `Workbook`, which
no docx4j-built workbook satisfies and which a typed port would have to
carry); `WorkbookAlternateContentKeptTest`. Chart parts (`c:chartSpace`,
DrawingML, not admitted by `dml-chart.xsd`) and the spreadsheet drawing's
anchor are still resolved to one branch at load. PresentationML: `pml.xsd`
admits the element in `CT_GroupShape` (`p:spTree`, `p:grpSp`) and
`CT_ControlList`, and `loadAndSave.pptx`'s slide carries one in `p:spTree`
(`a14`), resolved today because the slide part reaches the preprocessor
for an `a14:m` the schema lacks - phase 3 adds those parents to the retain
list together with the pptx4j consumers. The preprocessor's warnings no
longer claim "not admitted by the schema" (untrue for PML); they say what
is kept. Also seen, unrelated: `xl/drawings/vmlDrawing1.vml` in
`loadAndSave.xlsx` fails to unmarshal (an unexpected `xml` element) - a
pre-existing xlsx4j VML part issue, noted for the xlsx4j backlog.

### 8.9 Phase 3: PresentationML (2026-09-19, at Jason's go)

What PowerPoint writes, measured on the repository's five pptx: one
`mc:AlternateContent` in a slide's `p:spTree` (`loadAndSave.pptx`, slide 2:
a Choice `Requires="a14"` holding the shape that carries the slide's text,
an equation among it, and a Fallback holding a picture of that shape with a
single nbsp), and the chart parts' `c14` style elements. `pml.xsd` has
admitted the element in `CT_GroupShape` (`p:spTree`, `p:grpSp`) and
`CT_ControlList` (`p:controls`) all along, so a slide that unmarshals
cleanly kept it; that slide does not - an `a14:m` the schema lacks sends the
part through the shared preprocessor, which resolved the element to its
Fallback and lost the equation's shape. `pptx2svginhtml.xslt` had no
template for the element, so a kept one was drawn once per branch by the
built-in rule; the placeholder walks (`SlideMasterPart`, `SlideLayoutPart`,
`ResolvedLayout`) iterate the shape tree by `instanceof Shape` and passed
the element by, so a placeholder inside one was invisible to layout
resolution.

Coded:

- `mc-preprocessor.xslt`: `p:spTree`, `p:grpSp` and `p:controls` join the
  retain list (with `w:r`, `w:p`, `w:numPicBullet`, `x:workbook`); the
  warnings name the full list.
- `McSelection.selectedContent(List)`: a content list with each element
  replaced by its selected branch's content, recursively - the one-branch
  view for a caller that iterates by `instanceof`. The three placeholder
  walks use it; the slide's own list keeps the element whole for the save.
- `pptx2svginhtml.xslt`: an `mc:AlternateContent` template on the one rule
  (`XSLTUtils.mcPrefersChoice`), as `docx2fo.xslt` and
  `docx2xhtml-core.xslt` have.
- On save, the Choice's `Requires` prefix is declared on the slide root by
  the existing bookkeeping (`Docx4jUnmarshallerListener` records every
  Choice's `Requires`; `JaxbXmlPart` declares them through
  `NamespacePrefixMappings`, which knows `a14`), which is what PowerPoint
  2010 needs (`SlidePart`'s history); `SlidePart.setMceIgnorable`'s
  unconditional `v` stays.
- `docx4j.jaxb.mc.preferChoice` documented as the one switch for every
  format and reader (`McSelection`'s javadoc, the sample
  `docx4j.properties`): a PowerPoint equation or ink shape is drawn with
  `a14` in the list, its picture Fallback otherwise - the same default as
  Word's text boxes, and the same reason (draw what the producer wrote for
  a reader that understands nothing extra, unless told otherwise).
- `SlideAlternateContentKeptTest`: the element kept in the shape tree with
  both branches, `selectedContent`'s one-branch view, both branches written
  back with `xmlns:a14` on the root, a load-save-load round trip; the SVG
  export drawing the Fallback by default (the shapes beside it present, the
  Choice's "Equation" absent) and the Choice once with `a14` preferred.
- CHANGELOG under "Markup compatibility (CR-021)".

The chart parts' elements (`c:chartSpace`, DrawingML) are still resolved at
load, in every format; `dml-chart.xsd` does not admit the element and no
chart consumer reads it. Not in this CR.

**Gate (passed 2026-09-19):** the targeted suites (the new slide test,
LoadAndSaveTests, the mc tests) 18/0; docx4j-core-tests, the whole suite,
1196 tests, 0 failures, on jars verified as this tree's. No WordprocessingML
rendering path changed, so the corpora were not re-scored. The docx4j
re-save of `loadAndSave.pptx` is on the share (`cr021-phase2-check/
loadAndSave-phase3-resave.pptx`, slide root declaring `xmlns:a14`) for a
PowerPoint-open check: no repair prompt and slide 2 showing its text and
equation = pass.

**PowerPoint-open check, first pass (Jason, 2026-09-19): FAILED - no equation
on slide 2.** The re-save had lost it: the Choice's shape carries the
equation as `a14:m` (holding `m:oMath`) inside a DrawingML text paragraph,
and `dml-textParagraph.xsd`'s `EG_TextRun` did not admit `a14:m` - nor did
`oart14docprop.xsd`'s `CT_TextMath` hold anything (an empty type) - so the
unmarshaller dropped it as an unexpected element and the slide was saved
with the text but no equation. Before phase 3 the element was resolved to
its Fallback, a picture of the shape, which still showed the equation. So
keeping a branch is only lossless when the schema binds everything the
branch holds; this is the phase 0 rule ("admit what Word writes") applied
one level down, and the reason a kept Choice must be checked in the
producing application, not only round-tripped. Fixed: `EG_TextRun` admits
`a14:m`, and `CT_TextMath` holds its equation (MS-ODRAWXML 2.3.1: an
`m:oMathPara` or `m:oMath`) as a lax wildcard over the math namespace
rather than element references - the OMML types reach into WordprocessingML
(`m:r` holds `w:rPr`), and referencing them made the PresentationML and
SpreadsheetML JAXB contexts, whose package lists do not carry those classes,
fail to initialise at all (`IllegalAnnotationsException` on `w15:appearance`,
measured). With the wildcard the equation is kept as DOM in those contexts
and written back as it came, and typed in the WordprocessingML context;
regenerated; `SlideAlternateContentKeptTest` asserts the equation survives;
objects-ts told (two more xsd changes). The general lesson is recorded in
§8.6 as item 10. **Second pass (Jason, 2026-09-19 14:3x): PASSED - "the
equation is visible now".**

### 8.10 Phase 4: follow-through (2026-09-19, at Jason's go)

- The Getting Started guide (`docs/Docx4j_GettingStarted.docx`, canonical;
  the derived `.md`, `.html` and `.pdf` regenerated with
  `etc/GenGettingStartedDocs.java`): the "Specification versions"
  paragraph that said a saved docx loses Word 2010 content ("effectively a
  Word 2007 docx") rewritten, with a "Markup compatibility
  (mc:AlternateContent)" paragraph after it - what Word writes twice, that
  docx4j keeps both branches and draws one, the property and its default;
  a "mc:AlternateContent in a traversal" paragraph closing "Traversing a
  document" (READ by default, ALL for a callback that edits); one sentence
  in "Text extraction". The regenerated Markdown also shows the markdown
  exporter's own recent changes (TOC entries dropped, code fenced) reaching
  this document for the first time.
- CHANGELOG: the "Markup compatibility (CR-021)" block carries the API
  (TraversalUtil, McMode, McSelection), the loading change, SpreadsheetML,
  PresentationML and the schema follow-ups; the numbering follow-ups sit
  beside the numId-0 entry.
- Registry: `docx4j/CR-021` and its four phases done.

What this CR leaves for others (§8.6): the HTML exporter's VML text box
(item 1), the FO exporter's inline wps box and the `preferChoice` default
(item 2, §5), the anonymiser's shape analyzer (item 8), the VML JAXB
round-trip losses (§8.8), and the check of every kept branch's content in
the producing application (item 10).

### 8.11 Follow-up: the stream extractor applies the whole rule (2026-09-19)

Jason asked whether McSelection's rule is a problem for the SAX-based
`TextUtils`. It was not for any Word document, but the stream could not
apply the last resort - Choices and no Fallback take the first Choice -
because it cannot know a Fallback is absent until the element ends, and
such an element (Excel's absPath is the one seen) contributed nothing.
`TextExtractor` now holds back the first unpreferred Choice's text and
writes it when the element ends if nothing else was taken, discarding it
when a Fallback or a preferred later Choice is taken; nested elements
inside a held-back Choice are captured with it. So every docx4j reader
applies one rule, and the ports' textOf no longer differs from
`TextUtils` on rule 3. `McSelectionTest` covers it.

### 8.12 Follow-up: the mce wildcards are lax (2026-09-19)

Proposed by docx4j-generated-objects-ts from its regeneration, measured
there: `xsd/mce/markup-compatibility-2006-MINIMAL.xsd` declared the
`mc:Choice` and `mc:Fallback` wildcards `processContents="strict"`, which a
compiler that follows the word turns into typed-only content, so a kept
branch holding anything the model cannot type (a `w:tbl`, a local element
in that model; any untyped namespace) refused to unmarshal at all. XJC had
bound "strict" as `@XmlAnyElement(lax = true)` all along, so the Java model
already kept what it knows typed and the rest as DOM; the word is now
`lax`, which is what ECMA-376 Part 3 means and what JAXB did. The
regenerated Java sources differ only in javadoc and an unused import; the
mc tests are green. objects-ts regenerates from this commit.
