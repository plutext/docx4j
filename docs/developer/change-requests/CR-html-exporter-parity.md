# CR: HTML exporter feature parity (HTMLExporterVisitor vs HTMLExporterXslt)

Status: IN PROGRESS (2026-09-01) — phase 1 shipped (execution order 1, 4, 2, 3, 5, 6)
Scope: `org.docx4j.convert.out.html` plus the shared visitor base
`org.docx4j.convert.out.common.AbstractVisitorExporterGenerator` (both in docx4j-core)
Related: CR-fo-exporter-parity.md (DONE 2026-08-31) — the same exercise for FO/PDF.
Several of that CR's base-class fixes already apply to the HTML visitor pathway
(see §2 rows marked "via FO CR").  The deprecated `HtmlExporterNG2` /
`HtmlExporterNonXSLT` (pre-3.0 API) are out of scope.

## 1. Background

docx→HTML has two exporter implementations, selected by the `Docx4J.toHTML`
flags argument:

- **HTMLExporterXslt** (default, `FLAG_EXPORT_PREFER_XSL`) — docx2xhtml-core.xslt
  (via docx2xhtml.xslt for XML output method with XHTML doctype, or docx2html.xslt,
  chosen by the `docx4j.Convert.Out.HTML.OutputMethodXML` property) drives the
  traversal; formatting logic lives in `XsltHTMLFunctions`, `XsltCommonFunctions`
  and `SdtWriter`.
- **HTMLExporterVisitor** (`FLAG_EXPORT_PREFER_NONXSL`) — a `TraversalUtil` walk via
  `HTMLExporterVisitorGenerator` + `HTMLExporterVisitorDelegate`.

Shared, so at parity by construction:

- preprocessing (`Preprocess` with DEFAULT_HTML_FEATURES: Containerization,
  ListsToContentControls collection, FieldsCombiner, BookmarkMover, dummy page
  numbering/sections, table-paragraph-style fix);
- the writer registry (`HTMLConversionContext.HTML_WRITER_REGISTRY`): TableWriter,
  SymbolWriter, BrWriter, FldSimpleWriter (incl. the 17.0.3 field-font fix),
  BookmarkStartWriter (in its default `mapTo=a` mode), HyperlinkWriter;
- `RunFontSelector` for `w:t` (spans with font-family style), images
  (`WordXmlPictureE10/E20.createHtmlImg*`), complex-field state;
- from the FO CR's base-class work (17.0.4): mc:AlternateContent renders the
  Fallback only; images append via getCurrentParent (no NPE inside hyperlink
  content); getTextBox accepts any VmlShapeElements; null-image guards; unhandled
  elements are visible in the output in debug mode.

The gaps mirror the FO case — missing `apply()` branches and a drifted
`handlePPr`/`handleRPr` copy — plus one gap the FO exporter didn't have:
**`SdtWriter.registerTagHandler` is a public, documented extension point**
(TagSingleBox for Containerization borders/shading, SdtToListSdtTagHandler for
HTML lists, TagClass, and user-written handlers), and the visitor pathway bypasses
`SdtWriter` entirely, so registered handlers are silently ignored.

As in the FO CR, both implementations produce W3C DOM, and `XsltHTMLFunctions`
already separates its Xalan plumbing (NodeIterator unmarshalling) from the logic,
so the same consolidation pattern applies: JAXB/Node-typed overloads that the
visitor calls, childResults-then-wrap composition for paragraphs and sdts.

## 2. Feature matrix

Legend: Y = at parity; P = partial/degraded; N = missing; refs are to current code.
"Y*" = works in the XSLT pathway only once the user registers the relevant
SdtTagHandler (there is no default registration).

| # | Feature | XSLT | Visitor | Notes |
|---|---------|------|---------|-------|
| 1 | `<head>`: meta, style (default CSS + styles CSS + userCSS via styleElementHandler), script (toggleDiv + userScript via scriptElementHandler) | Y | Y | duplicated code (XsltHTMLFunctions.appendHeadElement vs HTMLExporterVisitorDelegate.appendDocumentHeader) with drift: the delegate probes the first section for header/footer via next()/start(), the XSLT-side copy has that commented out ("causes exception") |
| 2 | userBodyTop / userBodyTail injected into `<body>` | Y | N | HTMLSettings honoured by the stylesheet only |
| 3 | Default header/footer divs (`class="header"/"footer"`) | Y | Y | both support the default header/footer only |
| 4 | `div class="document"` wrapper | Y | Y | |
| 5 | Paragraph `@class` from the style tree | Y (always; default paragraph style when no pStyle) | P | visitor sets class only when direct pStyle present |
| 6 | Paragraph inline CSS from direct pPr (ignoreBorders) | Y | Y | both read the direct pPr |
| 7 | List numbering text (bullet U+2022 / numString via Emulator) | Y | P | visitor: only when pPrDirect!=null, so style-based numbering with no direct pPr yields no number; also appends an extra trailing space |
| 8 | Numbering indentation merged into the paragraph's ind (Emulator.getInd + Indent) | Y (XsltHTMLFunctions:612) | N | |
| 9 | Empty paragraph → `&nbsp;` (browsers collapse an empty p) | Y (XsltHTMLFunctions:708) | N | |
| 10 | mergeSpans: adjacent spans with same class/style merged; empty number spans dropped | Y | N | output-size / cosmetic |
| 11 | bookmarkStart `mapTo=id` mode → p/@id | Y (context.getBookmarkStart consumed in createBlock) | N | with `docx4j.Convert.Out.HTML.BookmarkStartWriter.mapTo=id`, the visitor drops the bookmark entirely; default `a` mode is at parity (shared writer) |
| 12 | Run span `@class` (rStyle, falling back to the default character style) | Y | P | visitor: only when direct rStyle; no default-character-style class |
| 13 | Run span/style composition with the w:t font-selection span (avoid nested spans; style concat) | Y (createBlockForRPr) | N | visitor nests the fontSelector span inside the run span |
| 14 | **sdt → SdtWriter + registerTagHandler extension point** (identity default, `*`/`**` hooks, QueryString tag parsing) | Y (toSdtNode) | N | **public API silently ignored by the visitor**; sdts traverse transparently (since FO CR; previously they warned) |
| 15 | Containerization borders/shading containers (TagSingleBox) | Y* | N | unlike FO, not on by default — requires registerTagHandler(Containerization.TAG_BORDERS/TAG_SHADING, new TagSingleBox()); with no handler, the XSLT's identity handler also loses the container styling, so the *default* outputs match; TAG_RPR is a TODO in both (Containerization.java:78) |
| 16 | HTML lists: PP_HTML_COLLECT_LISTS + `HTML_ELEMENT` tag → w:p as `li` (createListItemBlockForPPr), ol/ul via SdtToListSdtTagHandler | Y* (li unconditionally; ol/ul needs the handler) | N | PP_HTML_COLLECT_LISTS is a DEFAULT_HTML_FEATURES member, so the sdts are present for both; the visitor renders their paragraphs as plain p with literal number text (degraded, not numberless) |
| 17 | Tracked changes: `w:ins` / `w:moveTo` → span class="ins" (+ .ins CSS) | Y | Y | phase 1 shipped 2026-09-01 |
| 18 | Tracked changes: `w:delText` / `w:moveFrom` → span class="del" | Y | Y | phase 1 shipped 2026-09-01 |
| 19 | moveFromRangeStart/End, moveToRangeStart/End skipped | Y | Y | phase 1 shipped 2026-09-01 (range starts previously reached the bookmark writer, CTMoveBookmark extending CTBookmark) |
| 20 | `w:softHyphen` (U+00AD) | Y | Y | phase 1 shipped 2026-09-01 |
| 21 | `w:noBreakHyphen` (U+2011 non-breaking hyphen — NB different output than FO's hyphen+U+FEFF) | Y | Y | phase 1 shipped 2026-09-01; the XSLT itself was emitting a double-escaped entity (see phase 1 notes), fixed |
| 22 | `w:cr` → `<br clear="all"/>` | Y | Y | phase 1 shipped 2026-09-01 |
| 23 | Footnote/endnote references: numbered, styled span, bidirectional anchors (a name=fs{n} / href=#fn{n}), 17.0.3 generated-text font | Y | N | **references silently dropped** |
| 24 | Footnotes/endnotes divs (`class="footnotes"/"endnotes"`) at end of body | Y | N | delegate's appendFootnotesEndnotes is a `//TODO:...` stub |
| 25 | `w:footnoteRef`/`w:endnoteRef` in the note body: number + link back | Y | N | moot until 23/24 exist |
| 26 | `w:tab` → 3 nbsp | Y | Y | (no font handling in the HTML tab, unlike FO 17.0.3 — same in both, so parity) |
| 27 | `w:br` | Y | Y | shared BrWriter |
| 28 | `w:sym` | Y | Y | shared SymbolWriter |
| 29 | `w:fldSimple` / complex fields (+ dummy page numbering) | Y | Y | shared FldSimpleWriter, incl. font fix |
| 30 | `w:hyperlink`, `w:bookmarkStart` (default mode), `w:bookmarkEnd` | Y | Y | shared writers |
| 31 | Tables (TableWriter) | Y | Y | shared; the per-table cell CSS (getCssForTableCells) is invoked by neither current pathway (legacy-XSLT-only API), so not a parity gap |
| 32 | Images E10/E20 | Y | Y | same WordXmlPicture methods |
| 33 | VML textboxes | N (notImplemented comment) | P | the visitor finds the textbox and dispatches to a pict writer — but no pict writer is registered for HTML, so the registry's fallback **marshals the raw WML into the HTML output** (verify); parity = warn/skip like the XSLT |
| 34 | mc:AlternateContent → Fallback only | Y | Y | via FO CR phase 5 |
| 35 | `w:smartTag` transparent / `w:smartTagPr` skipped | Y | Y/P | smartTagPr warns (cosmetic) |
| 36 | Unhandled-element visibility (debug mode) | Y | Y | via FO CR phase 7 |
| 37 | Output method: XHTML doctype (XML method) or html method by property | Y | P | visitor always serializes as XML, no doctype; document as intentional or add the doctype |

## 3. Gap detail, grouped

### 3.1 Content silently dropped (rows 18, 20-23; severity: data loss)

Identical disease to the FO CR's phase 1/3: no `apply()` branch for `DelText`,
`R.SoftHyphen`, `R.NoBreakHyphen`, `R.Cr`, or `CTFtnEdnRef`.  Deleted text and
noBreakHyphen are visible text loss; footnotes/endnotes lose the reference marks
*and* (with row 24) the note bodies.  `w:ins` content renders unmarked, so a
tracked-changes document shows neither a markup view nor a clean accepted view.

### 3.2 The sdt extension point is bypassed (rows 14-16; severity: API contract)

`SdtWriter.registerTagHandler` is public API (used per the ConvertOutHtml sample:
TagSingleBox for borders/shading, SdtToListSdtTagHandler for real ol/ul lists,
TagClass, arbitrary user handlers).  The visitor never consults `SdtWriter`, so
registrations silently do nothing under `FLAG_EXPORT_PREFER_NONXSL`.  Note the
*default* behavior (no handlers registered) is closer to parity than in the FO
case: the XSLT's identity handler passes container contents through unstyled too.

### 3.3 Paragraph/run block building is a drifted copy (rows 5, 7-13; severity: fidelity)

`HTMLExporterVisitorGenerator.handlePPr`/`handleRPr` are simplified copies of
`XsltHTMLFunctions.createBlock`/`createBlockForRPr`: no class for default-styled
paragraphs/runs, no style-based numbering, no numbering indent, empty paragraphs
collapse, no span merging (larger output, and nested spans), and the
`bookmarkStart mapTo=id` contract is broken.

### 3.4 Document chrome (rows 1-2, 24, 37)

The `<head>` builders are duplicated Java with an already-visible drift (the
section-probing difference), userBodyTop/userBodyTail are ignored by the visitor,
the footnotes/endnotes divs are a TODO stub, and the visitor emits neither
doctype nor offers the html output method.

### 3.5 Raw WML leakage (row 33)

For a VML textbox the visitor dispatches to a pict writer that is not registered
for HTML; `AbstractWriterRegistry`'s no-writer fallback marshals the WML back and
appends it — into the HTML output.  Inferred from code reading; verify, then make
it warn/skip like the XSLT's notImplemented.

## 4. Proposed approach

**Same principle as the FO CR: stop maintaining two copies; consolidate on the
XSLT pathway's Java logic via JAXB/Node-typed overloads.**  The FO CR proved the
pattern end to end, including the paragraph-level childResults-then-wrap
restructuring of the visitor (`apply(P)` sub-generates children into a fragment,
then wraps) — reuse it directly:

- `XsltHTMLFunctions.createBlock`/`createBlockForRPr` get overloads taking the
  JAXB `PPr`/`RPr` and a DOM `Node` childResults (the NodeIterator forms
  delegate, as done for `XsltFOFunctions` in 17.0.4).
- sdts dispatch through `SdtWriter`: add a `toNode` variant taking the JAXB
  `SdtPr` and a `Node` childResults.  The public `SdtTagHandler` API takes a
  `NodeIterator` — keep it source-compatible by feeding handlers a single-node
  NodeIterator wrapper (the trick used in the binding-traverser CR, phase 5).
  The `HTML_ELEMENT` li case is a dispatch decision in the visitor's `apply(P)`
  (parent sdt's tag contains HTML_ELEMENT → wrap as `li` via
  createListItemBlockForPPr).
- Leaf branches (delText/ins spans, hyphens, cr) go in
  `HTMLExporterVisitorGenerator.apply()` like their FO counterparts.  Where the
  DOM output is genuinely format-specific this stays per-generator; nothing here
  warrants new base-class hooks beyond those the FO CR added.
- The footnote/endnote work reuses the JAXB-typed
  `XsltCommonFunctions.fontSelectorForGeneratedText` (already exists since
  17.0.4) and the delegate stub (`appendFootnotesEndnotes`).

### Phases

**Execution order (DECIDED 2026-09-01, jharrop: sequencing is flexible): 1, 4, 2,
3, 5, 6** — phase 4 runs before phase 2 so the sdt handlers receive
fully-consolidated childResults (see §5).  Phase numbers below are kept stable
for reference.

Each phase separately shippable.  Test pattern: an `HtmlVisitorParityTest` in
docx4j-core-tests `org.docx4j.convert.out.html` (alongside FieldFontTest /
NoteFontTest), running the same assertions against `Docx4J.toHTML` output under
both flags — note this module IS in the reactor, so unlike the FO parity tests
these gate the main build.  Structural equivalence is the bar, not byte equality
(mergeSpans etc. make byte equality meaningless anyway).

1. **Stop dropping content** (rows 18-22 + 19, 35): apply() branches for
   `DelText`/`RunDel` (span class="del"), `RunIns` (span class="ins" wrapper,
   walk-time like the FO ins), `R.SoftHyphen`, `R.NoBreakHyphen` (U+2011),
   `R.Cr` (`<br clear="all"/>`), quiet skips for the move-range markers and
   smartTagPr.  NB `w:moveTo` and `w:moveFrom` both unmarshal to
   `RunTrackChange`, so — as with CTFtnEdnRef in the FO CR — the JAXBElement
   wrapper name in the parent's content list distinguishes them (moveTo → ins
   styling, moveFrom → del styling per the templates).  Small, mechanical.
   **SHIPPED 2026-09-01**: as planned, plus two findings.  (1) A regression fixed
   in the shared base class: the 17.0.1 bidi work extracted
   rtlAwareAppendChildToCurrentP with a base implementation appending to
   parentNode instead of currentP — the FO generator overrode it back, the HTML
   generator didn't, so since 17.0.1 the HTML visitor put run spans NEXT TO their
   p element instead of inside it.  Base now appends to currentP; the FO override
   is deleted as redundant.  (2) The XSLT reference itself output w:noBreakHyphen
   as the double-escaped text &amp;#8209;: its disable-output-escaping does not
   survive the trip through the extension functions' result tree fragments (as
   the stylesheet's own character-entities note warns); the template now emits
   the literal U+2011.  Also: the FO generator's refKind now delegates to a new
   protected base helper jaxbElementName (shared with trackChangeClass here).
   Test: HtmlVisitorParityTest.testPhase1DroppedContent (both exporters).
2. **sdt dispatch through SdtWriter** (rows 14-16): restores the
   registerTagHandler contract in the visitor pathway; covers TagSingleBox
   borders/shading, SdtToListSdtTagHandler ol/ul, and the HTML_ELEMENT li shape
   for paragraphs.  Verify with a handler-registration test (and reset the
   static handler map afterwards — it is global state).
3. **Footnotes/endnotes** (rows 23-25): reference spans with bidirectional
   anchors; footnotes/endnotes divs in appendDocumentFooter (fill the existing
   stub); footnoteRef/endnoteRef in the note bodies.  Reuses the FO CR's
   refKind approach for the shared CTFtnEdnRef class.
4. **Paragraph/run fidelity via consolidation** (rows 5, 7-13): restructure
   apply(P) to childResults-then-wrap via a JAXB-typed createBlockForPPr /
   createListItemBlockForPPr; handleRPr routes through createBlockForRPr's
   core; delete both drifted copies (stub the abstract methods, as the FO
   generator now does).  Brings default-style classes, style numbering,
   numbering indent, empty-p nbsp, mergeSpans and the mapTo=id bookmark
   contract along for free.
5. **Document chrome + leakage** (rows 1-2, 33, 37): single `<head>` builder
   used by both (resolving the section-probe drift deliberately); userBodyTop/
   userBodyTail in the delegate; verify and stop the raw-WML fallback for
   unrendered VML (warn/skip); decide doctype/output-method (document as
   intentional difference if not implemented).
6. **Optional: measurement**: benchmark the two pipelines post-parity (FO CR
   appendix pattern).  Any default-flag change is out of scope.

### Out of scope

- The deprecated `HtmlExporterNG2` / `HtmlExporterNonXSLT` exporters.
- Changing the default flag in `Docx4J.getHTMLExporter`.
- New features beyond the XSLT's surface (e.g. rendering VML textboxes in HTML,
  per-table cell CSS, TAG_RPR run containers — TODOs in *both* pathways).
- The custom-XSLT extension point (users substituting their own stylesheet) is
  inherently XSLT-only; not a parity target.

## 5. Risks / open questions

- **SdtWriter's handler map is static/global** (shared across exporters and
  threads).  Routing the visitor through it is consistent with the XSLT pathway,
  but tests must reset registrations, and the thread-safety caveat (pre-existing)
  should be noted in javadoc rather than "fixed" here.
- **mergeSpans changes the visitor's output shape** noticeably (fewer, flatter
  spans).  That is the point (and shrinks output), but release-note it; any
  consumer post-processing visitor HTML will see different structure.
- **Numbering behavior in phase 4** must keep the XSLT's quirks (literal number
  text inside the p, bullet always rendered as U+2022) rather than "improving"
  them — improvements would be a separate CR against both pathways.
- The `w:p`-template quirks worth deciding consciously during phase 4: the
  empty-paragraph-in-table skip (docx2xhtml-core.xslt:182), and the
  single-child+pPr "empty paragraph still numbered" case (:197).
- Phase 2 before phase 4 means sdt childResults briefly use the current
  handlePPr output; acceptable (they compose), but if sequencing is flexible,
  4 before 2 gives cleaner childResults to the handlers.  DECIDED 2026-09-01:
  sequencing is flexible; running 4 before 2 (see execution order above).

## 6. Suggested sequencing and effort (rough)

| Phase | Effort | Value |
|-------|--------|-------|
| 1 Stop dropping content | S | High — silent text loss today |
| 2 SdtWriter dispatch | M | High — public extension point currently ignored |
| 3 Footnotes/endnotes | M | High for affected documents |
| 4 Paragraph/run fidelity | M | Medium-High — classes, numbering, empty paragraphs, output size |
| 5 Chrome + leakage | S-M | Medium — API completeness (userBody*), and the WML leak is ugly |
| 6 Measurement | S | Informs any future default change |

If only one phase is ever done, do phase 1 (as with the FO CR): smallest effort,
removes the silent-text-loss cases.  The FO CR's benchmark (visitor ~10x faster
FO generation) suggests the same order-of-magnitude win is available here for
users who can move to `FLAG_EXPORT_PREFER_NONXSL` once parity lands.
