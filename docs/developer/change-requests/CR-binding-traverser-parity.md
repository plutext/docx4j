# CR: Binding traverser feature parity (BindingTraverserNonXSLT / BindingTraverserStAX vs BindingTraverserXSLT)

Status: ACCEPTED (2026-08-22) — implementation in progress, following the phase sequencing below
Scope: `org.docx4j.model.datastorage` binding traversal (BindingHandler.applyBindings pathway)
Related: #690, discussion #691, commits 7d2d8ba40 (RptPosCon in NonXSLT/StAX), c85beee97 (StAX preprocess recursion)

## 1. Background

`BindingHandler.applyBindings` selects one of three traverser implementations via the
property `docx4j.model.datastorage.BindingHandler.Implementation`:

- **BindingTraverserXSLT** (default) — bind.xslt drives the traversal; all feature logic is
  in Java extension functions on `BindingTraverserXSLT` called from the stylesheet.
  "Slower, but fully featured."
- **BindingTraverserNonXSLT** — JAXB tree walk. "Faster, but missing some features."
- **BindingTraverserStAX** — streams the part, intercepting outermost sdts; delegates
  each intercepted sdt's subtree to BindingTraverserNonXSLT. So its feature surface is
  NonXSLT's, plus the streaming layer.

The same property also switches OpenDoPEHandler's preprocessing to `ShallowTraversorStAX`
when the part is not yet unmarshalled (fixed to recurse into replacement content in
c85beee97).

The non-XSLT implementations were written by copying simplified versions of the feature
logic rather than reusing it, so they have drifted: some features are absent, and some
present ones are degraded copies. This CR catalogues the differences and proposes a plan.

## 2. Feature matrix

Legend: Y = at parity; P = partial/degraded; N = missing; refs are to current code.

| # | Feature | XSLT | NonXSLT | StAX | Notes |
|---|---------|------|---------|------|-------|
| 1 | Plain `w:dataBinding` text bind | Y | Y | Y | phase 1 shipped 2026-08-22 |
| 2 | `od:xpath` extended bind | Y | Y | Y | phase 1 shipped 2026-08-22 |
| 3 | `w15:dataBinding` bind (Word 2013) | Y (bind.xslt:796) | P | P | `SdtPr.getDataBinding()` returns any `CTDataBinding` (w: or w15:), so the plain-bind fallback branch catches these — but with all the 3.1 degradations, and the w15 branch details (richText/docPartGallery exclusions, sdtPr cleanup) unverified |
| 4 | Multiline (`w:text/@w:multiLine`) | Y | Y | Y | |
| 5 | Hyperlink insertion in bound text | Y | Y | Y | third copy of the logic in each impl |
| 6 | `w:rPr` from sdtPr applied to generated runs | Y | Y | Y | phase 1 shipped 2026-08-22 |
| 7 | Empty result → placeholder restore | Y (ValueInserterPlainTextImpl:33) | Y | Y | phase 1 shipped 2026-08-22 (null result now leaves the sdt alone, all impls) |
| 8 | Pluggable `ValueInserterPlainText` | Y | Y | Y | phase 1 shipped 2026-08-22 |
| 9 | `local-name()` descape + result trim | Y (BindingTraverserXSLT:1410-1414) | Y | Y | phase 1 shipped 2026-08-22 |
| 10 | Picture bind (`w:picture` + `w:dataBinding`), template contains `a:blip` | Y — replaces just `r:embed`, preserving the authored drawing (mode picture3) | P | P | non-XSLT always rebuilds the whole drawing (ExtentFinder size only); alt text, wrapping, effects lost |
| 11 | `od:Handler=picture` (rich text cc containing w:drawing) | Y (bind.xslt:177) | N | N | 3.0.1 feature |
| 12 | `od:Handler=picture` + `width=n\|auto` | Y (bind.xslt:150) | N | N | 11.1.8 feature |
| 13 | Date cc (`w:date` + `w:dataBinding`), formatted per dateFormat/lang | Y (xpathDate) | N | N | |
| 14 | Checkbox cc (`w14:checkbox` + `w:dataBinding`) | Y (w14Checkbox, w14CheckboxAttr) | N | N | sets checked state and glyph run |
| 15 | XHTML import (`od:ContentType=application/xhtml+xml`), ImportXHTML or altChunk fallback | Y (convertXHTML / convertXHTMLtoAltChunk) | N (TODO log) | N (TODO log) | includes bookmark renumbering via BookmarkCounter |
| 16 | FlatOPC injection (`od:progid=Word.Document`) | Y (convertFlatOPC) | N | N | |
| 17 | `od:RptPosCon` | Y | Y | Y | since 7d2d8ba40 |
| 18 | `od:condition` / `od:rptd` pass-through incl. nested | Y | Y | Y | StAX preprocess recursion fixed in c85beee97 |
| 19 | `w15:resultRepeatZero` pass-through | Y | Y | Y | non-XSLT default branch preserves |
| 20 | sdtPr Word2007 hyperlink fix (strip `w:dataBinding`+`w:text`) | Y | P | P | non-XSLT strips on hyperlink in processString; XSLT also covers content-derived cases |
| 21 | Add `w:showingPlcHdr` when content is PlaceholderText-styled | Y (bind.xslt:1133) | N | N | RemovalHandler ALL_BUT_PLACEHOLDERS depends on this |
| 22 | Strip `w:placeholder` from output sdtPr | Y (bind.xslt:1179) | N | N | cosmetic |
| 23 | Text-bind shapes: rebuild `w:tbl` / `w:tr` sdt content | Y | Y | Y | phase 1 shipped 2026-08-22 |
| 24 | XPath result cache (`DomToXPathMap` via BindingTraverserState) | Y | N | N | perf only; BindingHandler only wires it to the XSLT traverser |
| 25 | Structural context tracking (BindingTraverserState enteredTc/Tbl) | Y | n/a | n/a | supports the shape decisions in #23 |

Recently closed gaps, for the record: od:RptPosCon (7d2d8ba40); run-level direct-run
content shape (7d2d8ba40); hdr/ftr/footnote/endnote interception contexts + null-context
NPE (7d2d8ba40); per-instance SdtStAXHandler stack (588c1838c); StAX preprocess nested
recursion (c85beee97).

## 3. Gap detail, grouped

### 3.1 Plain text binding is a degraded copy (rows 1-2, 6-9, 23)

Both non-XSLT impls carry a private copy of `xpathGenerateRuns` which: passes `rPr=null`
(bound-text formatting lost — probably the most user-visible gap); does not restore the
placeholder on an empty result and can NPE on a null result; bypasses the pluggable
`ValueInserterPlainText`; and skips trim/local-name handling. They also only rebuild
p / tc / direct-run shapes; the XSLT additionally rebuilds tbl and tr sdt shapes.

### 3.2 Content-control types absent (rows 10-14)

Date, checkbox, and the richer picture behaviors are simply not implemented; picture
binding falls back to a rebuild-the-drawing approach that discards authored drawing
properties.

### 3.3 Rich-content injection absent (rows 15-16)

XHTML import and FlatOPC injection are TODO stubs. These are the features the class
javadocs already flag ("TODO add HTML import, FlatOPC support").

### 3.4 Output hygiene (rows 20-22)

Mostly cosmetic, except `w:showingPlcHdr` (row 21), which RemovalHandler's
ALL_BUT_PLACEHOLDERS quantifier relies on: documents produced by the non-XSLT pathway
behave differently under that RemovalHandler mode.

## 4. Proposed approach

**Principle: stop maintaining three copies of feature logic.** The XSLT pathway's features
already live in plain Java static methods (`BindingTraverserXSLT.xpathGenerateRuns`,
`xpathDate`, `w14Checkbox`, `convertFlatOPC`, `convertXHTML`, `xpathInjectImageRelId`,
`createPlaceholder`, and `ValueInserterPlainText`) which return DOM `DocumentFragment`s.
The non-XSLT traversers can call the same methods and bridge the result into the JAXB tree
with `XmlUtils.unmarshal(node)` per fragment child (cost is negligible relative to the
binding work itself). Where a method takes a `NodeIterator`/state object only for XSLT's
benefit, add an overload taking the JAXB `SdtPr` directly (the innermost
`xpathGenerateRuns` overload already has this form).

What stays per-implementation is only *dispatch* (which branch a given sdt falls into) and
*content-shape rebuilding* (tbl/tr/tc/p wrapping), which in the XSLT lives in stylesheet
markup. Transcribe the shape rules once into a small shared helper (eg
`BindingTraverserCommonImpl`) used by NonXSLT; StAX inherits everything via delegation.
Fold the duplicated hyperlink/multiline run-building in NonXSLT/StAX into calls to
`ValueInserterPlainText` and delete the copies.

### Phases

Each phase is separately shippable, with a 3-implementation parameterized test (pattern:
RptPosConTraverserImplsTest / NestedConditionRptPosConTest) asserting identical output
text/structure across traversers, plus a golden comparison against the XSLT output for the
feature docx.

1. **Text-bind correctness** (rows 1-2, 6-9, 23; biggest user impact, moderate effort):
   route non-XSLT text binding through `ValueInserterPlainText` with the sdtPr's rPr;
   placeholder restore + null-safety; trim/local-name; add tbl/tr shapes; then delete the
   duplicated run-building code from both classes.
   **SHIPPED 2026-08-22** ("text binding routes through ValueInserterPlainText" commit):
   shared `generateBoundContent`/`applyBoundContent` in BindingTraverserCommonImpl bridge
   the XSLT pathway's `xpathGenerateRuns` DocumentFragment output into the JAXB tree;
   both traversers' duplicated run-building deleted; binding resolved od:xpath-id-first
   (matching bind.xslt), falling back to w:dataBinding (which covers w15:dataBinding);
   also fixed a latent NPE in `xpathGenerateRuns` on a null XPath result.
   Test: TextBindParityTest (asserts rPr application, placeholder restore, tbl shape,
   and full-text equality across all three implementations).
2. **w15:dataBinding** (row 3): verify actual behavior with a Word-2013-bound sample
   (the fallback branch likely already binds the value once phase 1 lands); align the
   richText/docPartGallery exclusions and sdtPr handling with bind.xslt:796.
3. **Date + checkbox** (rows 13-14): call `xpathDate` / `w14Checkbox` via new
   JAXB-friendly overloads.
4. **Picture parity** (rows 10-12): blip-replacement mode when the template content has
   an `a:blip` (reuse `xpathInjectImageRelId`), then the `od:Handler=picture` variants.
5. **XHTML + FlatOPC** (rows 15-16): reuse `convertXHTML`/`convertXHTMLtoAltChunk`/
   `convertFlatOPC`; wire `BookmarkCounter` to the traversers' existing (currently
   unused) bookmark id state.
6. **Output hygiene** (rows 20-22): showingPlcHdr first (functional), then the cosmetic
   bits, or explicitly document them as intentional differences.
7. **Optional, perf**: wire `DomToXPathMap` into the shared text-bind path (BindingHandler
   already builds it; only the XSLT traverser consumes it today).

### Out of scope

- Making StAX interception cope with contexts it streams through today (nested-table
  block sdts etc.) beyond what 7d2d8ba40 added — separate concern.
- OpenDoPEHandler preprocessing parity — believed complete after c85beee97, but phase
  tests should run against the StAX preprocess path too (they do, via the property).

## 5. Risks / open questions

- **DOM→JAXB bridging**: `DocumentFragment` children must unmarshal to the right
  declared types in each container context (run vs block level); the shape helper must
  choose the wrap. Prototype in phase 1 before committing to the pattern.
- **Behavior bar (DECIDED 2026-08-22, jharrop)**: byte-for-byte match with the XSLT
  output is NOT required. The bar is structural equivalence asserted in tests, with
  exact-match only where downstream code (RemovalHandler, Reverter) depends on it.
- **XHTML import** requires docx4j-ImportXHTML on the classpath (reflection); the
  altChunk fallback keeps phase 5 self-contained.
- The three-way property switch also changes preprocessing; tests must keep exercising
  both preprocess paths (they do implicitly, since the parts are freshly loaded).

## 6. Suggested sequencing and effort (rough)

| Phase | Effort | Value |
|-------|--------|-------|
| 1 Text-bind correctness | M | High — silent formatting/data loss today |
| 2 w15 verification | S-M | Medium |
| 3 Date + checkbox | S | Medium |
| 4 Picture parity | M | Medium |
| 5 XHTML + FlatOPC | M-L | Low-Medium (users needing these likely stay on XSLT) |
| 6 Output hygiene | S | Low (except showingPlcHdr) |
| 7 Cache wiring | S | Perf only |

If only one phase is ever done, do phase 1: it removes the silent-loss cases and, by
consolidating on `ValueInserterPlainText`, shrinks the code that must be kept in sync
from three copies to one.
