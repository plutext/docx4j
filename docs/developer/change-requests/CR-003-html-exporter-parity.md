# CR: HTML exporter feature parity (HTMLExporterVisitor vs HTMLExporterXslt)

Status: DONE (2026-09-01) — all 6 phases shipped (execution order 1, 4, 2, 3, 5, 6); one defect recorded and fixed 2026-09-18 (list items collide with their marker, §4); a second recorded and fixed for HTML (a hanging indent crosses a left border, §4; the FO exporter's twin is CR-001 batch 48 item 9).
The default flag decision (XSLT remains the default) is recorded under Out of scope.
Scope: `org.docx4j.convert.out.html` plus the shared visitor base
`org.docx4j.convert.out.common.AbstractVisitorExporterGenerator` (both in docx4j-core)
Related: CR-002-fo-exporter-parity.md (DONE 2026-08-31) — the same exercise for FO/PDF.
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
| 1 | `<head>`: meta, style (default CSS + styles CSS + userCSS via styleElementHandler), script (toggleDiv + userScript via scriptElementHandler) | Y | Y | phase 5: single implementation (appendHeadElement), with the caller positioning the section iterator; incidental bugfix: BOTH copies computed hasDefaultFooter from hasDefaultHeader (copy/paste), so div.footer never got its print CSS when there was a footer but no header |
| 2 | userBodyTop / userBodyTail injected into `<body>` | Y | Y | phase 5 shipped 2026-09-01; the visitor requires well-formed markup (a DOM can't hold the raw text the XSLT's disable-output-escaping can emit) — parsed as a fragment, or dropped with a warning |
| 3 | Default header/footer divs (`class="header"/"footer"`) | Y | Y | both support the default header/footer only |
| 4 | `div class="document"` wrapper | Y | Y | |
| 5 | Paragraph `@class` from the style tree | Y | Y | phase 4 shipped 2026-09-01 |
| 6 | Paragraph inline CSS from direct pPr (ignoreBorders) | Y | Y | both read the direct pPr |
| 7 | List numbering text (bullet U+2022 / numString via Emulator) | Y | Y | phase 4 shipped 2026-09-01 (shared getNumberXmlNode; NB with PP_HTML_COLLECT_LISTS on, numbered paragraphs normally become li instead) |
| 8 | Numbering indentation merged into the paragraph's ind (Emulator.getInd + Indent) | Y | Y | phase 4 shipped 2026-09-01 |
| 9 | Empty paragraph → `&nbsp;` (browsers collapse an empty p) | Y | Y | phase 4 shipped 2026-09-01 |
| 10 | mergeSpans: adjacent spans with same class/style merged; empty number spans dropped | Y | Y | phase 4 shipped 2026-09-01 |
| 11 | bookmarkStart `mapTo=id` mode → p/@id | Y | Y | phase 4 shipped 2026-09-01 (via the shared createBlock; not separately tested — the property is read in a static initializer, so it can't be toggled per-test) |
| 12 | Run span `@class` (rStyle, falling back to the default character style) | Y | Y | phase 4 shipped 2026-09-01 |
| 13 | Run span/style composition with the w:t font-selection span (avoid nested spans; style concat) | Y | Y | phase 4 shipped 2026-09-01 |
| 14 | **sdt → SdtWriter + registerTagHandler extension point** (identity default, `*`/`**` hooks, QueryString tag parsing) | Y (toSdtNode) | Y | phase 2 shipped 2026-09-01 |
| 15 | Containerization borders/shading containers (TagSingleBox) | Y* | Y* | phase 2 shipped 2026-09-01 (still requires the handler registration, in both pathways; TAG_RPR remains a TODO in both) |
| 16 | HTML lists: PP_HTML_COLLECT_LISTS + `HTML_ELEMENT` tag → w:p as `li` (createListItemBlockForPPr), ol/ul via SdtToListSdtTagHandler | Y* | Y* | li half: phase 4; ol/ul handler half: phase 2 (both 2026-09-01) |
| 17 | Tracked changes: `w:ins` / `w:moveTo` → span class="ins" (+ .ins CSS) | Y | Y | phase 1 shipped 2026-09-01 |
| 18 | Tracked changes: `w:delText` / `w:moveFrom` → span class="del" | Y | Y | phase 1 shipped 2026-09-01 |
| 19 | moveFromRangeStart/End, moveToRangeStart/End skipped | Y | Y | phase 1 shipped 2026-09-01 (range starts previously reached the bookmark writer, CTMoveBookmark extending CTBookmark) |
| 20 | `w:softHyphen` (U+00AD) | Y | Y | phase 1 shipped 2026-09-01 |
| 21 | `w:noBreakHyphen` (U+2011 non-breaking hyphen — NB different output than FO's hyphen+U+FEFF) | Y | Y | phase 1 shipped 2026-09-01; the XSLT itself was emitting a double-escaped entity (see phase 1 notes), fixed |
| 22 | `w:cr` → `<br clear="all"/>` | Y | Y | phase 1 shipped 2026-09-01 |
| 23 | Footnote/endnote references: numbered, styled span, bidirectional anchors (a name=fs{n} / href=#fn{n}), 17.0.3 generated-text font | Y | Y | phase 3 shipped 2026-09-01 |
| 24 | Footnotes/endnotes divs (`class="footnotes"/"endnotes"`) at end of body | Y | Y | phase 3 shipped 2026-09-01 (appended after the footer div, where the XSLT puts them before it — cosmetic ordering difference) |
| 25 | `w:footnoteRef`/`w:endnoteRef` in the note body: number + link back | Y | Y | phase 3 shipped 2026-09-01 |
| 26 | `w:tab` → 3 nbsp | Y | Y | (no font handling in the HTML tab, unlike FO 17.0.3 — same in both, so parity) |
| 27 | `w:br` | Y | Y | shared BrWriter |
| 28 | `w:sym` | Y | Y | shared SymbolWriter |
| 29 | `w:fldSimple` / complex fields (+ dummy page numbering) | Y | Y | shared FldSimpleWriter, incl. font fix |
| 30 | `w:hyperlink`, `w:bookmarkStart` (default mode), `w:bookmarkEnd` | Y | Y | shared writers |
| 31 | Tables (TableWriter) | Y | Y | shared; the per-table cell CSS (getCssForTableCells) is invoked by neither current pathway (legacy-XSLT-only API), so not a parity gap |
| 32 | Images E10/E20 | Y | Y | same WordXmlPicture methods |
| 33 | VML textboxes | N (notImplemented comment) | N | phase 5 shipped 2026-09-01: verified — worse than a leak, the fallback's foreign-document node threw WRONG_DOCUMENT_ERR and **killed the whole conversion**; the visitor now treats w:pict as image-or-warn like the XSLT, and the base convertToNode imports foreign-document writer results |
| 34 | mc:AlternateContent → Fallback only | Y | Y | via FO CR phase 5 |
| 35 | `w:smartTag` transparent / `w:smartTagPr` skipped | Y | Y/P | smartTagPr warns (cosmetic) |
| 36 | Unhandled-element visibility (debug mode) | Y | Y | via FO CR phase 7 |
| 37 | Output method: XHTML doctype (XML method) or html method by property | Y | Y | phase 5 shipped 2026-09-01 (writeDocument override: doctype + xml/html method per the OutputMethodXML property, indent=no, method set explicitly — the serializer would otherwise auto-select the html method for an html root) |

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
   **SHIPPED 2026-09-01**: SdtWriter gets a JAXB/Node-typed toNode(context,
   SdtPr, Node childResults) (null sdtPr → the no-tag handling, where the XSLT
   form throws), feeding the handlers' NodeIterator signature via
   XmlUtils.singleNodeIterator (promoted from BindingTraverserCommonImpl, which
   now delegates).  The visitor's handleSdt sub-generates the sdt contents and
   dispatches; handlers needed fragment-awareness for visitor-fed childResults:
   SdtTagHandler.attachContents accepts DOCUMENT_FRAGMENT and skips empty spans
   null-safely, TagSingleBox.getNodeByName accepts a fragment and createDiv
   null-guards the first-p lookup.  Test registers TagSingleBox (borders and
   shading divs around the containerized paragraphs), SdtToListSdtTagHandler
   (real ol around the li items) and asserts identity pass-through for an
   unregistered tag; no reset needed since surefire forks a JVM per test class
   (noted in the test).  Output identical between the pathways for the test
   document.  Test: HtmlVisitorParityTest.testPhase2SdtTagHandlers.
3. **Footnotes/endnotes** (rows 23-25): reference spans with bidirectional
   anchors; footnotes/endnotes divs in appendDocumentFooter (fill the existing
   stub); footnoteRef/endnoteRef in the note bodies.  Reuses the FO CR's
   refKind approach for the shared CTFtnEdnRef class.
   **SHIPPED 2026-09-01**: appendNoteReference builds the XSLT's shape (the
   xx-small span holding a name= anchor wrapping a href= anchor around the
   fontSelectorForGeneratedText-styled number); the four prefix pairs (fs/fn,
   es/en and their note-body inverses) cover reference and back-link.  The
   delegate's stub is filled: divs class footnotes/endnotes in the body, notes
   with id 0 skipped as in the XSLT, each note converted by a generator whose
   noteNumber field (cf count(preceding-sibling)-1) is propagated into handleP/
   handleSdt sub-generators.  The divs land after the footer div where the XSLT
   puts them before it — cosmetic, noted in row 24.  w:separator and
   w:continuationSeparator are quiet no-ops (the XSLT emits nothing for them
   either).  Test: HtmlVisitorParityTest.testPhase3FootnotesEndnotes.
4. **Paragraph/run fidelity via consolidation** (rows 5, 7-13): restructure
   apply(P) to childResults-then-wrap via a JAXB-typed createBlockForPPr /
   createListItemBlockForPPr; handleRPr routes through createBlockForRPr's
   core; delete both drifted copies (stub the abstract methods, as the FO
   generator now does).  Brings default-style classes, style numbering,
   numbering indent, empty-p nbsp, mergeSpans and the mapTo=id bookmark
   contract along for free.
   **SHIPPED 2026-09-01**: XsltHTMLFunctions' createBlock split into an unmarshal
   wrapper + a JAXB/Node-typed core (as done for XsltFOFunctions), with JAXB
   createBlockForPPr / createListItemBlockForPPr overloads; the visitor's
   apply(P) sub-generates children into a fragment (prepending the number text
   via the shared getNumberXmlNode, whose NodeIterator parameter turned out to
   be unused anyway) and wraps — a w:p inside an HTML_ELEMENT sdt becomes an li,
   per the w:p template, incl. its in-table-empty-paragraph quirk.  Runs stay
   streamed, but walkJAXBElements(R) post-processes the span after its children
   are walked: no span at all for a run without rPr (as the XSLT), otherwise
   the new XsltHTMLFunctions.composeRunSpan applies the createBlockForRPr
   composition in place (default character class, style concat with the w:t
   font span, child-span merging).  handlePPr/handleRPr are stubs.  Findings:
   mergeSpans and the span-detection had latent plain-DOM hazards — Xalan's
   DTMNodeProxy implements Element for every node type and reports "#text" as a
   text node's local name, while DOM level 1 createElement spans have a null
   local name — fixed with a type/name-safe isSpan used by both pathways; and
   createBlock mutates the pPr it is given (numbering indent merge), so the
   visitor passes a deep copy (the XSLT works on a throwaway unmarshalled copy).
   The visitor's output for the test document is now essentially identical to
   the XSLT's, down to quirks like the double semicolon in composed styles.
   Test: HtmlVisitorParityTest.testPhase4ParagraphRunFidelity.
5. **Document chrome + leakage** (rows 1-2, 33, 37): single `<head>` builder
   used by both (resolving the section-probe drift deliberately); userBodyTop/
   userBodyTail in the delegate; verify and stop the raw-WML fallback for
   unrendered VML (warn/skip); decide doctype/output-method (document as
   intentional difference if not implemented).
   **SHIPPED 2026-09-01**: the delegate's head builder is deleted in favour of
   appendHeadElement (delegate positions the section iterator around the call;
   the shared copy's commented-out probe is resolved into a documented caller
   contract).  Incidental bugfix in the shared builder: hasDefaultFooter was
   computed from hasDefaultHeader in BOTH copies.  userBodyTop/userBodyTail are
   injected (well-formed markup parsed as a fragment; else warn+drop — a DOM
   cannot hold the raw text disable-output-escaping can emit; recorded in row
   2).  Row 33 turned out worse than the suspected leak: the no-writer
   fallback's foreign-document node threw WRONG_DOCUMENT_ERR and killed the
   conversion; the visitor now treats w:pict as image-or-warn like the XSLT,
   and convertToNode defensively imports foreign-document writer results (base
   class, so the FO pathway gains the guard too).  writeDocument now emits the
   XHTML doctype and honours docx4j.Convert.Out.HTML.OutputMethodXML; method
   and indent are set explicitly (the serializer auto-selects the html method
   for an html root, which had it emitting &shy;-style entities, and indentation
   damages subscripts per the stylesheets' note).
   Test: HtmlVisitorParityTest.testPhase5Chrome.
6. **Optional: measurement**: benchmark the two pipelines post-parity (FO CR
   appendix pattern).  Any default-flag change is out of scope.
   **SHIPPED 2026-09-01**: benchmark in the appendix; the default-flag decision
   remains open (and out of scope), as with the FO CR.

### Out of scope

- The deprecated `HtmlExporterNG2` / `HtmlExporterNonXSLT` exporters.
- Changing the default flag in `Docx4J.getHTMLExporter`.
- New features beyond the XSLT's surface (e.g. rendering VML textboxes in HTML,
  per-table cell CSS, TAG_RPR run containers — TODOs in *both* pathways).
- The custom-XSLT extension point (users substituting their own stylesheet) is
  inherently XSLT-only; not a parity target.

### Defect found after DONE: list items collide with their marker (2026-09-18; FIXED the same day)

Found by exporting the OpenDoPE Specification v3 working draft
(`docs/OpenDoPE Specification v3 WD 2026 09 18.docx`; the `.html` sibling is
the output): 63 `<li>` elements - every ListBullet, ListNumber, Note and
Example paragraph - have their first line pulled back onto the browser's
bullet.  Identical from `FLAG_EXPORT_PREFER_XSL` and `_NONXSL`, so it is
shared, not a parity gap.  Not implemented; recorded so the mechanism is on
record.

Symptom.  The element

    <li class="ListBullet Normal DocDefaults " style="display: list-item;">…</li>

under the stylesheet rule

    .ListBullet { display:block;position: relative; margin-left: 28.35pt;text-indent: -14.15pt;margin-bottom: 4pt; }

The browser draws its default outside marker to the left of the content box,
and the negative `text-indent` pulls the first line back over it.

Mechanism, in `org.docx4j.convert.out.html`:

1. `XsltHTMLFunctions.createBlock` already knows: for an `li` it calls
   `HtmlCssHelper.createCss(pkg, pPr, sb, ignoreBorders, isListItem=true)`,
   which skips the `Indent` property ("Avoid indent settings which would
   overwrite the bullet", `HtmlCssHelper` line 271).  That covers only the
   INLINE style.
2. `HtmlCssHelper.createCssForStyles` emits the stylesheet and calls
   `createCss(opcPackage, s.getPPr(), result, false, false)` for every
   paragraph style (lines 133 and 163), with no knowledge that the style
   carries `w:numPr`.  The class rule reintroduces exactly the indent the
   inline path suppressed.  Any paragraph style with `numPr` in its pPr is
   affected: List Bullet, List Number, and in this document Note and Example,
   whose label comes from numbering.

Two gaps in the same path, worth fixing together:

3. No `<ul>`/`<ol>` wrapper and no `list-style-type`: the browser draws a disc
   for every `<li>`.  Ordered lists lose their numbers; the Note and Example
   paragraphs (`numFmt="none"`, `lvlText="NOTE"`) get a disc instead of the
   label.  (The markdown export got the label case in 471f9094b; HTML has
   not.)
4. Because the marker is the browser's, `lvlText`, `numFmt` and the level's
   rPr never reach the output: "Appendix A" (upperLetter with text), the "–"
   at ilvl 1, the text2 colour on the bullet.

Proposed change: (a) in `createCssForStyles`, treat a style whose pPr has
`numPr` as a list item (pass `isListItem=true`, or otherwise drop `Indent`
for it) so the class rule stops fighting the marker; (b) in `createBlock`,
for a numbered paragraph emit the label from the numbering emulator (the same
`ResultTriple` the FO exporter uses) as the first inline - e.g.
`<span class="ListLabel">•</span>` / "1." / "NOTE" with the level's rPr
applied - keep the paragraph's resolved hanging indent as `margin-left` /
`text-indent`, and set `list-style: none` (or emit `<p>` rather than `<li>`).
That makes Word's hanging indent do the job it does in Word, renders labels
and `numFmt` faithfully, and removes the need for `ul`/`ol` nesting.  It is
the approach the XSL FO exporter already takes, so the label plumbing exists.
The published `.html` is re-exported once it lands; the `.md` and the docx
are unaffected.

Fixed 2026-09-18, in `org.docx4j.convert.out.html`, along the lines of (b) with
one further cause found on the way: the default HTML feature set collects
lists into `HTML_ELEMENT` sdts, but the handler which turns those into
`ul`/`ol` (`SdtToListSdtTagHandler`) was registered only by the samples, so by
default the items came out as orphan `li` with no list, and the number text
was suppressed for them ("the li numbers itself").  Now `SdtWriter` registers
the handler by default; `XsltHTMLFunctions.createListLabel` writes the label
span (`class="ListLabel"`, `inline-block`, `min-width` = the hanging indent,
the level's rPr as CSS, a symbol-font bullet mapped to Unicode, `w:suff space`
as a space) at the head of every numbered paragraph, `p` or `li`, in both
pathways; an `li` gets `list-style: none` and keeps its indent (the inline
`Indent` skip is off); the `ul`/`ol` is structural.  `getNumberXmlNode` is a
no-op so the counter advances once.  Tests: `HtmlListLabelTest` (both
pathways: a style-numbered bullet labelled and unmarked with the class rule's
indent intact, NOTE / Appendix A / Appendix B, a direct list counting once) and
the parity test's list assertions.  On the draft: 63 `li` in 47 `ul`/`ol`,
107 labels, no `display: list-item`, 24 NOTE, 5 EXAMPLE, 5 Appendix labels,
identical from both flags.  One defect in that fix, found by rendering the
draft in headless Firefox and fixed the same day: `text-indent` inherits, and
an `inline-block` is a block container with a first line box of its own, so
the paragraph's negative `text-indent` was applied a second time inside the
label and pushed it left out of its box (a NOTE label under a 42.5pt hang
rendered as "OTE" against the page edge; every label sat one hang to the
left of Word's).  The span now carries `text-indent: 0`; `HtmlListLabelTest`
asserts it on the emitted style, since no DOM-level test sees a layout-only
defect.

### Defect found after DONE: a hanging indent crosses a left paragraph border (2026-09-18; HTML FIXED the same day, FO queued)

Found on the same draft, re-exported at ed6380a10 (the list fix holds).  Not
implemented; recorded with the mechanism.

Symptom.  In

    <p class="Requirement Normal DocDefaults "><a name="REQ_013"><span class="RequirementID">REQ-013</span>&nbsp;&nbsp;&nbsp;<span>Each </span>…

under

    .Requirement {display:block;page-break-inside: avoid;position: relative; margin-left: 56.7pt;text-indent: -56.7pt;border-left-style: solid;border-left-width: 0.79mm;border-left-color: #2B6CB0;padding-left: 8pt;margin-top: 4pt;margin-bottom: 8pt;}

the first line has no gap between the vertical bar and the text, while the
continuation lines are 8pt clear of it.  The style is
`<w:pBdr><w:left w:val="single" w:sz="18" w:space="8"/></w:pBdr>` with
`<w:ind w:left="1134" w:hanging="1134"/>`.  The same for the Example style
(left 1134, hanging 850, a sz 6 border) and for any numbered paragraph with a
left border, since the new `ListLabel` sits in the hang.

Mechanism.  `Indent.getCssProperty` emits `margin-left` = `w:left` and
`text-indent` = -hanging (`Indent.java` 178-195); `AbstractPBorder`
independently emits the border and `padding-left` = `w:space` (line 131);
nothing reconciles them.  In the CSS box model the border sits at the margin
edge, so the first line starts `hanging` to the left of the content edge - at
about 10pt, left of the bar at 56.7pt - and the bar is drawn through the
first line, right after the ID and its tab.  Word positions a left border
relative to the leftmost text edge, min(`w:left`, `w:left` - hanging), less
`w:space`: in Word the bar stands left of the ID with an 8pt gap and every line
clears it.

Proposed change.  Where a paragraph (style or direct) has a left border and a
hanging indent, move the hang from margin into padding: `margin-left` = left
- hanging; `padding-left` = space + hanging; `text-indent` = -hanging as
before.  For the Requirement style: margin-left 0, padding-left 64.7pt,
text-indent -56.7pt - the first line 8.79pt from the bar, continuation lines
at 64.7pt, Word's geometry.  No change without a left border, or with a
positive `firstLine`.  `Indent` and `PBorderLeft` are separate `Property`
objects, so the reconciliation belongs where the paragraph CSS is assembled
(`HtmlCssHelper.createCss` for a `PPr`, stylesheet and inline paths alike), or
`Indent` is handed the effective `pBdr`.

**The XSL-FO exporter has the same defect, measured on the same document
(2026-09-18).**  Its text is at Word's x - the ID at 70.90 against Word's
70.82, the continuation line at 127.60 against 127.49 - but the bar is
not: FOP strokes the paragraph's left border at the block's `start-indent`
edge less its padding, page x = 118.48, where Word's bar stands at
59.30..61.46, so ours cuts through the first line between the ID and the
text on every requirement.  The FO box model is the CSS one (`start-indent` is
the content edge, border and padding inside it, a negative `text-indent` runs
back over the border), so the same shift applies: `start-indent` = left -
hanging, `padding-start` = space + hanging.  Queued for CR-001 batch 48 as a
class 1 rule (font-independent; the probe: a bordered paragraph with a
hanging indent, a numbered one, and a `firstLine` control).

HTML fixed 2026-09-18 as proposed, in `HtmlCssHelper.createCss(PPr)` (a new
overload taking the effective `w:pBdr`): where the paragraph has a left border
and a hanging indent, the `Indent` property's CSS is replaced by
`margin-left` = left - hanging and `text-indent` = -hanging, and a
`padding-left` = space + hanging is appended after the border's own so that it
wins; both the stylesheet's class rules and the inline path, the latter handed
the effective borders by `createBlock` so a paragraph's own indent under a
bordered style is shifted as the style's rule is.  Nothing changes without a
left border, with a positive `firstLine`, or for an `li` whose indent is
skipped.  `HtmlBorderHangingTest`, both pathways.  On the draft the
Requirement rule reads `margin-left: 0; text-indent: -56.7pt; ...
padding-left: 8pt; ... padding-left: 64.7pt`.  `HtmlCssHelper` was CRLF and is
now LF.

Two follow-on defects, found by rendering the re-export in headless Firefox
and fixed the same day.  (1) A derived style with its own left border but an
inherited hanging indent regressed: Recommendation basedOn Requirement, its
own pPr a border in another colour and no `w:ind`, emitted `padding-left` =
w:space from its own pPr, and, later in the stylesheet at equal specificity,
that padding overrode the base rule's shifted one - the first line at -56.7pt
with 8pt of padding, the ID clipped off the container's edge.  The trio is
self-consistent only when all three come from one rule, and
`createCssForStyles` derives each rule from the style's own pPr.  Now a
style whose own pPr contributes a left border or a hang ends its rule with
the trio from its effective pPr (`appendBorderHangTrio`, through
`PropertyResolver.getEffectivePPr(styleId)`), and the inline path takes the
effective indent where the paragraph carries its own border and none.
(2) A list label wider than the hang ran straight into the text (EXAMPLE in
bold 9pt Carlito under a 42.5pt hang): `min-width` does nothing for it and
there was no gap.  In Word the level's `w:suff tab` runs on to the next stop;
the label is now `box-sizing: border-box` with `padding-right: 0.5em`
alongside its `min-width` for a tab suffix, so a short label still occupies
exactly the hang and a long one has a gap.  Tests in `HtmlBorderHangingTest`
(the derived style; a paragraph's own border under a hanging style) and
`HtmlListLabelTest`.

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

## Appendix: performance comparison (2026-09-01, post-parity)

Method: same synthetic document as the FO CR's appendix (n three-run justified
paragraphs with a 5x3 table every 50); `Docx4J.toHTML`, fresh package per
iteration, median of 7 after 3 warmups, one JVM (-Xmx1g), one machine.

| n paragraphs | XSLT | Visitor | ratio |
|---|---|---|---|
| 500 | 459 ms | 63 ms | 7.3x |
| 2,000 | 2,649 ms | 240 ms | 11.0x |

The same order-of-magnitude advantage as the FO visitor, with the same caveats
(synthetic document, single-threaded, one machine).  As there, whether
FLAG_EXPORT_PREFER_NONXSL should become the recommended or default flag is NOT
decided here; the XSLT pathway remains the default in `Docx4J.getHTMLExporter`,
and the visitor should get real-world exposure first.
