# CR: FO exporter feature parity (FOExporterVisitor vs FOExporterXslt)

Status: IN PROGRESS (2026-08-31) — phases 1-3 shipped; see the per-phase notes below
Scope: `org.docx4j.convert.out.fo` (docx4j-export-fo) plus the shared visitor base
`org.docx4j.convert.out.common.AbstractVisitorExporterGenerator` (docx4j-core)
Related: CR-binding-traverser-parity.md (same "stop maintaining two copies" principle,
applied there to OpenDoPE binding traversal)

## 1. Background

docx→FO (and hence docx→PDF via FOP) has two exporter implementations, selected by the
`Docx4J.toFO`/`toPDF` flags argument:

- **FOExporterXslt** (default, `FLAG_EXPORT_PREFER_XSL`) — docx2fo.xslt drives the
  traversal; the formatting logic lives in Java extension functions
  (`XsltFOFunctions`, `XsltCommonFunctions`) called from the stylesheet.
  "XSLT: Fully featured."
- **FOExporterVisitor** (`FLAG_EXPORT_PREFER_NONXSL`) — a `TraversalUtil` walk of the
  JAXB tree via `FOExporterVisitorGenerator` (extending the shared
  `AbstractVisitorExporterGenerator`).  "Non XSLT: Faster, but fewer features."

A lot is already shared, so is at parity by construction:

- preprocessing (`Preprocess`, incl. Containerization, FieldsCombiner, PageBreak,
  BookmarkMover) and `ConversionSectionWrappers` — both run through
  `AbstractFOExporter`/`AbstractWmlExporter`;
- the writer registry (`FOConversionContext.FO_WRITER_REGISTRY`): TableWriter,
  SymbolWriter, BrWriter, FldSimpleWriter (incl. the 17.0.3 field-font fix, wired via
  `setCurrentPPr` on the visitor side), BookmarkStartWriter (incl. the `_GoBack` skip),
  HyperlinkWriter, FOPictWriter;
- `LayoutMasterSetBuilder` incl. the header/footer extent measurement pass (each
  pipeline measures using itself: `fixExtents(..., useXSLT)`);
- the FOP rendering / 2-pass page-count postprocess (`AbstractFOExporter.postprocess`,
  `FOConversionContext.isRequires2Pass`);
- `RunFontSelector` for `w:t` content, images (`WordXmlPictureE10/E20`), complex-field
  state tracking, and the issue-660 bidi block-container logic (present in both, as
  two copies).

The gaps are in what the stylesheet templates do beyond dispatching to shared code.
`AbstractVisitorExporterGenerator.apply()` has no case for several element types (they
hit the final `else` → "Need to handle" warn, and their output is silently dropped),
and `FOExporterVisitorGenerator` carries drifted private copies of the paragraph/run
formatting logic in `XsltFOFunctions`.

Unlike the binding-traverser CR, both implementations here already produce W3C DOM, so
consolidation does not need a DOM→JAXB bridge; the obstacle is instead the different
composition order (XSLT transforms children first and wraps them; the visitor creates
the parent element first and streams children into it).

## 2. Feature matrix

Legend: Y = at parity; P = partial/degraded; N = missing; refs are to current code.

| # | Feature | XSLT | Visitor | Notes |
|---|---------|------|---------|-------|
| 1 | Sections, page-sequence master-reference/format/initial-page-number | Y | Y | FOExporterVisitorDelegate.createSectionRoot |
| 2 | `force-page-count` (Word section type evenPage/oddPage emulation, since 3.2.2) | Y (getForcePageCount) | Y | phase 1 shipped 2026-08-31 |
| 3 | Headers/footers (first/even/default static-content) | Y | Y | |
| 4 | Footnote separator static-content (`xsl-footnote-separator` rule) | Y (docx2fo.xslt:236) | Y | phase 3 shipped 2026-08-31 |
| 5 | `w:footnoteReference` → fo:footnote (number + body), sequential numbering, 17.0.3 font styling | Y | Y | phase 3 shipped 2026-08-31 |
| 6 | `w:endnoteReference` superscript number; Endnotes block appended to last flow | Y | Y | phase 3 shipped 2026-08-31 (like the XSLT, the Endnotes block is emitted per section, though Word's default position is end of document) |
| 7 | `w:footnoteRef`/`w:endnoteRef`/`w:continuationSeparator` handling inside notes | Y | Y | phase 3 shipped 2026-08-31 |
| 8 | Paragraph block from effective pPr/rPr | Y | Y/P | logic duplicated (handlePPr vs createBlock); see rows 9-12 for the drift |
| 9 | Paragraph-mark `sz`/`lang` affect block line-height (fontSzOnlyRPr, 2018-05 fix) | Y (XsltFOFunctions:386) | N | visitor passes null to getEffectiveRPr |
| 10 | Empty paragraph keeps its height (preserve + space + paragraph-mark font, 17.0.3) | Y (XsltFOFunctions:542) | N | empty w:p renders as an empty fo:block |
| 11 | `hyphenate="true"` when docx4j.convert.out.fo.hyphenate set | Y (XsltFOFunctions:170) | Y | phase 1 shipped 2026-08-31 |
| 12 | `text-align-last="justify"` when block contains a leader (ptab case) | Y (foContainsElement) | Y | phase 1 shipped 2026-08-31 (set where the visitor emits the ptab leader) |
| 13 | List items (fo:list-block, label via Emulator + RunFontSelector, indent/pdbs) | Y | P | near-duplicate logic; visitor nests block/list-block ("That's different to XSL" comment) and hangs pPr attributes on the outer block, XSLT on the block in list-item-body |
| 14 | Containerization containers: `w:sdt` XSLT_PBdr/XSLT_Shd → borders/shading on the container block; margin fix for shading; nested-sdt and inline (XSLT_RPr, run w:bdr) cases | Y (createBlockForSdt / createInlineForSdt) | Y | phase 2 shipped 2026-08-31 |
| 15 | Tracked changes: `w:ins` blue underline | Y (docx2fo.xslt:411) | Y | phase 1 shipped 2026-08-31 |
| 16 | Tracked changes: `w:delText` red line-through | Y (docx2fo.xslt:422) | Y | phase 1 shipped 2026-08-31 |
| 17 | `w:softHyphen` (U+00AD) | Y | Y | phase 1 shipped 2026-08-31 |
| 18 | `w:noBreakHyphen` (hyphen + U+FEFF) | Y | Y | phase 1 shipped 2026-08-31 |
| 19 | `w:cr` (line break block) | Y | Y | phase 1 shipped 2026-08-31 (empty preserve block, matching what the XSLT actually emits) |
| 20 | `w:ptab[@w:alignment='right']` leader | Y | Y | phase 1 shipped 2026-08-31; other alignments warn, as the XSLT's no-match template does |
| 21 | `w:tab`: dot-leader / default 3-nbsp | Y | Y/P | logic mirrored, but visitor omits the 17.0.3 font-family on the leader and the nbsp run (measured in renderer default font); visitor reads effective tabs where XSLT reads direct pPr (minor, arguably better) |
| 22 | `w:br` incl. consecutive-br special case | Y | Y | visitor mirrors the template logic |
| 23 | `w:sym` | Y | Y | shared SymbolWriter |
| 24 | `w:fldSimple` (PAGE/NUMPAGES/DATE... + font resolution) | Y | Y | shared FldSimpleWriter; 2-pass shared |
| 25 | Complex fields (fldChar state; result runs only) | Y | Y | |
| 26 | `w:hyperlink`, `w:bookmarkStart` (+`_GoBack` skip), `w:bookmarkEnd` | Y | Y | shared writers |
| 27 | Tables (incl. vMerge etc. via TableWriter) | Y | Y | shared TableWriter |
| 28 | Images E20 (`w:drawing`) and E10 (`w:pict` v:imagedata) | Y | Y | same WordXmlPicture methods |
| 29 | Textbox in `v:shape/v:textbox` | Y | Y | both via FOPictWriter |
| 30 | Textbox in `v:rect/v:textbox` (3.0.1; e.g. o:hr horizontal rules) | Y | N | getTextBox only recognizes CTShape → falls into "assume image", likely NPE or dropped (verify) |
| 31 | `mc:AlternateContent` → Fallback only | Y | P | TraversalUtil.getChildrenImpl returns Choice(s) **and** Fallback → warns, and risk of duplicate/garbled output |
| 32 | `w:smartTag` transparent traversal | Y | Y | visitor warns but children render |
| 33 | Unhandled-element visibility (red message block via FO_MESSAGE_WRITER) | Y (notImplemented) | P | visitor only logs |
| 34 | Bidi / issue-660 RTL paragraph block-container | Y | Y | two copies of the logic |
| 35 | `w:ind`/`w:jc`/bidi-jc-swap/shading/toc-tab on blocks | Y | Y | two copies (createFoAttributes in XsltFOFunctions and in the generator) |

## 3. Gap detail, grouped

### 3.1 Content silently dropped (rows 5-6, 16-20; severity: data loss)

`AbstractVisitorExporterGenerator.apply()` has no branch for `CTFtnEdnRef`
(footnote/endnote references), `DelText`, `R.SoftHyphen`, `R.NoBreakHyphen`, `R.Cr`,
or `R.Ptab`.  Each logs "Need to handle" and produces nothing.  Footnotes are the
worst: the reference mark *and the note body* vanish from the PDF.  `w:noBreakHyphen`
loses a visible character of text.  `w:delText` is dropped, while `w:ins` content is
shown unmarked — so a document with tracked changes renders neither the markup view
(XSLT behavior) nor a clean accepted/rejected view, but an inconsistent mix.

### 3.2 Paragraph borders and shading containers (row 14; severity: layout)

The shared Containerization preprocess wraps adjacent bordered/shaded paragraphs in
`w:sdt` containers tagged `XSLT_PBdr`/`XSLT_Shd` (and bordered runs in `XSLT_RPr`),
leaving `w:pBdr`/`w:shd` on the paragraphs.  The XSLT renders the container as a block
carrying the borders/shading (with the margin fix for shading strips) and suppresses
top/bottom borders on inner paragraphs.  The visitor has no sdt handling at all:
`handlePPr` is always called with `sdt=false`, so `ignoreBorders` is always true —
**every paragraph's top/bottom borders are dropped** (left/right survive, which looks
worse than dropping all four), the shading merge/margin behavior is lost, and run
borders disappear.

### 3.3 Section-level output (rows 2, 4, 6)

`force-page-count` (mimicking Word's behavior of not inserting blank pages except for
odd/even section types), the footnote-separator static-content, and the Endnotes block
at the end of the last section are emitted only by the XSLT/extension-function path.
All three are cheap to add to `FOExporterVisitorDelegate` since the logic already
exists in Java (`XsltFOFunctions.getForcePageCount`, `XsltCommonFunctions.getEndnotes`).

### 3.4 Typographic fidelity of generated content (rows 9-11, 21)

The 17.0.3 generated-text font work and the 2018-05 line-spacing fix landed on the
XSLT side only: empty paragraphs collapse (no preserve+space, no paragraph-mark font),
tab leaders/dummies are measured in the renderer's default font, paragraph-mark `sz`
doesn't influence line height, and the hyphenation property is ignored.

### 3.5 Odd traversal semantics (rows 30-31)

`mc:AlternateContent`: generic traversal descends into every `mc:Choice` *plus* the
`mc:Fallback`, where the XSLT deliberately selects the Fallback only.  And a textbox
hosted in `v:rect` (Word's horizontal rule idiom, among others) isn't recognized by
`getTextBox`, so it falls into the "assume it contains an image" branch.

### 3.6 Duplicated logic (rows 8, 13, 34-35; severity: drift risk)

`FOExporterVisitorGenerator.handlePPr`/`handleRPr`/`createFoAttributes` and its list
item code are hand-maintained copies of `XsltFOFunctions.createBlock`/
`createBlockForRPr`/`createListBlock`/`createFoAttributes`.  They have already
drifted (rows 9-13); every future formatting fix must be made twice or the gap grows.
This is the same disease the binding-traverser CR cured in `model.datastorage`.

## 4. Proposed approach

**Principle (same as CR-binding-traverser-parity): stop maintaining two copies of
feature logic.**  Here the consolidation is easier than it was for binding: both
pipelines emit W3C DOM, and `XsltFOFunctions` already separates its XSLT plumbing from
JAXB-typed cores — e.g. the inner
`createBlock(wmlPackage, runFontSelector, pStyleVal, childResults, sdt, pPrDirect, pPr, rPr, rPrParagraphMark)`
takes JAXB objects, with only `childResults` being a Xalan `NodeIterator`.

Direction of consolidation: make the JAXB-typed cores in `XsltFOFunctions` the single
implementation, give each an overload that accepts already-built DOM children (or a
parent `Element` to fill) instead of a `NodeIterator`, and have the visitor call them.
The composition-order mismatch (XSLT wraps pre-transformed children; the visitor
creates the parent first and streams children in) is confined to `handlePPr`'s
contract: it can keep returning the element children should stream into, while the
attribute/structure building behind it is shared.

Where a gap is a missing `apply()` branch on a leaf element with trivial output
(softHyphen, cr, delText...), add the branch to the FO generator directly — or, where
the same gap exists for the HTML visitor exporter (which shares
`AbstractVisitorExporterGenerator`), add an overridable hook in the base class.
Fixes at the base-class level (AlternateContent, sdt dispatch, delText) benefit the
HTML visitor pathway for free; verifying HTML parity is out of scope here but the
hooks should be designed with it in mind.

### Phases

Each phase is separately shippable.  Test pattern: a parity test (in
docx4j-export-fo-tests; note that module is on disk but not in the reactor — tests run
manually / via its own pom) that converts a feature docx with `Docx4J.toFO` under both
`FLAG_EXPORT_PREFER_XSL` and `FLAG_EXPORT_PREFER_NONXSL` and asserts structural
equivalence of the FO DOMs (masking acceptable differences), plus targeted assertions
(e.g. "footnote body text present", "fo:block has border-top").  As with the binding
CR, byte-for-byte identity is NOT the bar; structural/feature equivalence is.

1. **Stop dropping content** (rows 16-20 + quick wins 2, 11): add apply() branches for
   `DelText` (+ `w:ins`/`w:del` inline styling per the templates), `R.SoftHyphen`,
   `R.NoBreakHyphen`, `R.Cr`, `R.Ptab`; set `force-page-count` in createSectionRoot;
   honour the hyphenate property.  Small, mechanical, high value.
   **SHIPPED 2026-08-31**: FOExporterVisitorGenerator gets an apply() override for the
   leaf elements (delText red strike-through, softHyphen U+00AD, noBreakHyphen
   hyphen+U+FEFF, cr, right-aligned ptab leader + text-align-last on its block) and a
   walkJAXBElements override wrapping w:ins runs in the blue/underline inline (state
   preserved, so pPr-based font selection keeps working inside the ins, unlike a
   sub-generator would); RunDel traverses transparently.  handlePPr honours the
   hyphenate property; createSectionRoot sets force-page-count via the existing
   `XsltFOFunctions.getForcePageCount` (section iterator state matches the XSLT's
   moveNextSection timing).  Finding: the XSLT's w:cr template emits an *empty*
   preserve block (its literal space is stripped as stylesheet whitespace); the
   visitor matches that.  Rows 16-20, 2, 11 → Y; row 15 → Y; row 12 → Y for the ptab
   case.  Test: VisitorParityTest (docx4j-export-fo-tests), same assertions run
   against both exporters' FO output.
2. **Borders/shading containers** (row 14): dispatch `SdtBlock`/`SdtRun` in the FO
   generator; XSLT_PBdr/XSLT_Shd containers render via a shared
   `createBlockForSdt`-equivalent (container gets the borders/shading + shading margin
   fix; inner paragraphs keep ignoreBorders), XSLT_RPr via the inline path; non-XSLT_
   sdts stay transparent.  This is the phase that most changes visible layout.
   **SHIPPED 2026-08-31**: rather than a visitor-side copy, the consolidation
   direction of §4 was applied directly: XsltFOFunctions' DOM `createBlock` was split
   into an unmarshal wrapper plus a JAXB-typed `createBlock(context, PPr pPrDirect,
   pStyleVal, Node childResults, sdt)` holding the effective-props logic (so the
   fontSzOnly/paragraph-mark computation now exists once), and JAXB-typed
   `createBlockForSdt`/`createInlineForSdt` overloads were added.  The visitor's
   apply() intercepts XSLT_-tagged sdts (a pure `containerTag` check also drives
   shouldTraverse), converts the contents into a fragment with a sub-generator
   (mirroring the XSLT's childResults-then-wrap composition), and wraps via those
   overloads; the container-shape cases (first w:p's pPr / nested sdt's first w:p /
   sdtPr rPr) mirror the w:sdt template.  Non-XSLT_ sdts now traverse silently
   (previously a "Need to handle" warn).  Note: this also fixes the always-dropped
   paragraph top/bottom borders, since bordered paragraphs are always containerized.
   Test: VisitorParityTest.testPhase2Containers (paragraph borders on one container
   block, no repeated inner borders, shading container with zero margins, run-border
   container inline).
3. **Footnotes and endnotes** (rows 4-7): footnote-separator static-content in the
   delegate; `CTFtnEdnRef` branch building the fo:footnote structure (share the
   number-sequencing via the existing context counters and the
   fontSelectorForGeneratedText styling); Endnotes block after the last section's
   body content.
   **SHIPPED 2026-08-31**: XsltCommonFunctions.fontSelectorForGeneratedText gets a
   JAXB-typed overload (the NodeIterator form delegates), used for the note numbers
   with the 17.0.3 glyph-check behavior.  w:footnoteReference/w:endnoteReference
   share CTFtnEdnRef, so the visitor tells them apart via the JAXBElement wrapper in
   the run's content list.  The footnote branch mirrors the template's fo:footnote
   structure and uses the same by-position footnote lookup as the XSLT's getFootnote;
   the note body is converted with a sub-generator.  The Endnotes block is appended
   to the flow in appendSectionFooter — per section, matching the XSLT (a latent
   XSLT oddity for multi-section documents, Word's default being end-of-document;
   left as-is per the parity bar).  w:endnoteRef renders the note's own number via
   an endnoteNumber field the delegate sets per endnote (equivalent to the XSLT's
   count(preceding-sibling)-1); w:footnoteRef, w:separator and w:continuationSeparator
   are skipped as in the XSLT.  Test: VisitorParityTest.testPhase3FootnotesEndnotes.
4. **Paragraph fidelity** (rows 9-10, 13, 21): fontSzOnlyRPr in handlePPr; empty-block
   preserve+space+font; font-family on tab leaders/dummies (reuse
   `XsltFOFunctions.getFontFamily`'s core); reconcile the list-item structural
   difference (or document it as intentional).  Best done *by* doing phase 6's
   consolidation for handlePPr — consider merging.
5. **Traversal semantics** (rows 30-31): AlternateContent → Fallback-only for export
   (hook in the base generator; don't change TraversalUtil.getChildrenImpl, other
   callers may want both); recognize `v:rect` (and check `v:oval` etc.) textboxes in
   getTextBox; verify/fix the null-image NPE path.
6. **Consolidation** (row 35 and §3.6): fold the duplicated createFoAttributes /
   createBlock / createListBlock / bidi-wrap logic into single shared methods in
   XsltFOFunctions (or a new shared helper class if the XSLT-specific entry points are
   kept thin), and delete the copies in FOExporterVisitorGenerator.  After this,
   formatting fixes land once.
7. **Optional: visibility + measurement**: emit the notImplemented red message block
   from the visitor's unhandled-element branch (parity of row 33); then benchmark the
   two pipelines (the binding CR's appendix pattern) to decide whether the visitor,
   once at parity, should become the recommended/default flag — that decision is out
   of scope for this CR and would be its own recorded decision.

### Out of scope

- HTML visitor exporter parity (benefits incidentally from base-class fixes; own
  gap analysis if wanted later).
- Changing the default flag in `Docx4J.getFOExporter` (XSLT remains default until a
  measured decision after parity; cf. the binding CR where NonXSLT became default
  only after the phases + performance appendix).
- A docx4j.properties switch for exporter selection (today it is API flags only);
  could be considered alongside the default decision.
- XSLT-side TODOs (cross-references, `w:pict` variants the XSLT itself doesn't
  handle) — parity means matching the XSLT, not exceeding it.

## 5. Risks / open questions

- **handlePPr contract**: the visitor's stream-children-into-parent model means the
  shared block-building code must be callable before children exist.  Prototype the
  shared-attributes refactor on handleRPr (simplest) before committing to the shape.
- **Borders phase changes existing visitor output** significantly (paragraphs that
  used to render left/right-only borders get full borders; spacing near shaded runs
  changes).  That is the point, but release-note it.
- **AlternateContent**: selecting Fallback only in the exporter must not regress
  documents whose Fallback is empty but whose Choice is renderable; XSLT behavior
  (Fallback always) is the reference, keep to it.
- **Footnote numbering** uses conversion-context counters shared with the XSLT path;
  ensure the 2-pass render (which runs the exporter twice) resets them identically in
  both pipelines.
- **v:rect NPE** (row 30) is inferred from code reading, not yet reproduced —
  verify with a horizontal-rule docx (one is at the repo root:
  docx4j-samples-docx4j 'HorizontalRuleRemove' inputs) before/while fixing.
- docx4j-export-fo-tests is outside the reactor; parity tests there don't gate the
  main build.  Acceptable (matches existing PStyle*/FieldFontTest practice), but note
  it in RELEASE_HOWTO verification steps if the visitor default ever changes.

## 6. Suggested sequencing and effort (rough)

| Phase | Effort | Value |
|-------|--------|-------|
| 1 Stop dropping content | S | High — silent text/content loss today |
| 2 Borders/shading containers | M | High — visible layout defect on ordinary documents |
| 3 Footnotes/endnotes | M | High for affected documents (notes vanish entirely) |
| 4 Paragraph fidelity | M | Medium — line heights, empty paragraphs, leader fonts |
| 5 Traversal semantics | S-M | Medium — correctness on AlternateContent/VML shapes |
| 6 Consolidation | M-L | Structural — ends the double-maintenance |
| 7 Visibility + measurement | S | Informs any future default change |

If only one phase is ever done, do phase 1: smallest effort, and it removes the cases
where the visitor pathway silently loses document text (noBreakHyphen, delText,
footnote references at least warn today but produce nothing).
