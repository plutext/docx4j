# CR: Paginate: rewrite `w:lastRenderedPageBreak` from Apache FOP's area tree

Status: PROPOSED (2026-09-11). Requested by the docx4j-core-ts editor design
(`plutext/docx4j-core-ts`, CR-003 appendix D): a browser editor that does not paginate shows
page boundaries from the `w:lastRenderedPageBreak` markers Word leaves in a file, and needs a
"re-paginate" service to refresh them after edits. docx4j has the machinery in substance
(`TocGenerator.getPageNumbersMapViaFOP`, export-fo, FOP's area tree); this CR turns it into a
general operation on a package, `Paginate`, usable from Java, from `docx4j-mcp` as a tool, and
from the TOC generator itself.
Scope: computing, for each paragraph of the main document, the page it starts on and the
character offsets at which later pages begin, from an XSL-FO rendering by Apache FOP; writing
the result back as `w:lastRenderedPageBreak` items in the runs, replacing the ones there;
returning the same map to callers. Not in scope: making FOP paginate like Word (fonts, keeps
and floats are what they are), headers and footers content, or fields other than what
export-fo already resolves (§7).
Phases: 1. `Paginate` over paragraph starts (block-level `prod-id`), writes markers at
paragraph boundaries; 2. in-paragraph breaks from the area tree's line areas, markers at the
right run offset; 3. `TocGenerator` uses `Paginate`; 4. `docx4j-mcp` tool.

## 1. Background: what exists

### 1.1 `w:lastRenderedPageBreak`

ECMA-376 17.3.3.13: an empty run item recording that "the previous rendering of this
document" broke a page here. Word writes one into the run at every page boundary of the
layout it last computed, so a document saved by Word carries its own pagination as data.
Consumers that cannot lay out (docx4j's exporters, the TypeScript editor) can show page
boundaries from it. Word regenerates the markers on save; nothing else in the file depends
on them. docx4j today ignores them: `TraversalUtil` and `MainDocumentPart`'s text traversal
skip the element, `PageBreak` (`convert.out.common.preprocess`) lists it among the run items
it steps over, and no code writes it.

### 1.2 The TOC page-number pass

`org.docx4j.toc.TocGenerator.getPageNumbersMapViaFOP()` (docx4j-core, since 3.x) renders the
package with export-fo (`Docx4J.toFO`, `FLAG_EXPORT_PREFER_NONXSL` since 17.0.4) with
`FOSettings.setApacheFopMime("application/X-fop-areatree")`, so that `FORendererApacheFOP`
asks FOP for its **area tree** (`MimeConstants.MIME_FOP_AREA_TREE`) instead of a PDF, and
parses the result with `TocPageNumbersHandler`: `pageViewport` elements give page numbers
(`formatted-nr`, else `nr`, keyed by `key`), and the TOC entries' internal links
(`inlineparent/@internal-link="(Pnn, key)"`) give, per link, the page viewport of its
destination. The map from bookmark name to page number then fills the TOC's page numbers.
`FOPAreaTreeHelper` (3.1.0) uses the same area-tree output to size header and footer regions.

So the pipeline package → FO → FOP area tree → SAX handler → map exists and is exercised by
the TOC tests. What it lacks for pagination is generality: it locates only the TOC's link
destinations, at paragraph granularity, and it writes nothing back.

### 1.3 The area tree

FOP's area tree XML (`org.apache.fop.render.xml.XMLRenderer`; read back by
`AreaTreeParser`) is a page-by-page tree: `pageViewport` → `page` → `regionViewport` →
`regionBody` → `mainReference` → `span` → `flow` → `block` → `lineArea` → `text` (holding
`word` and `space`) / `inlineparent` / `viewport`. An area produced by a formatting object
that had an `id` attribute carries `prod-id="<id>"` (the `Trait.PROD_ID` name, which
`XMLRenderer.addTraitAttributes` writes and `AreaTreeParser` reads). A block split across
pages appears as block areas with the same `prod-id` on both pages; its line areas hold the
text of each line, which is the information for in-paragraph breaks.

**Decision (Jason, 2026-09-11): the CR targets the FOP version in the tree, 2.9, and does
not support older FOP versions.** The names above are 2.9's, checked in its jar
(`org.apache.fop.area.Trait` names the trait `prod-id`; `XMLRenderer` emits `pageViewport`,
`formatted-nr`, `lineArea`, `text`, `word`, `space`, `inlineparent`); nothing in the design
hedges for the older renderer's `text`-only line areas.

## 2. Gap and value

- Nothing in docx4j can answer "which page is this paragraph on" except through the TOC
  generator, and nothing can refresh `w:lastRenderedPageBreak`.
- The TypeScript editor's page display depends on markers being present and refreshable;
  documents produced by docx4j, by that editor, or by some other producers have none.
- `docx4j-mcp` agents that reason about "page 3" have no tool for it.
- The TOC generator itself would be simpler over a general pagination map (§6 phase 3).

## 3. Design

### 3.1 API

```java
package org.docx4j.model.pagination;   // docx4j-core: the model and the writer

public final class PaginationMap {
    /** Page number (as FOP formatted it, honouring w:pgNumType) each paragraph starts on, by paragraph key. */
    Map<String, Integer> pageOf;
    /** For paragraphs that span pages: the character offsets (in the paragraph's text, TextUtils order) at which each later page begins. */
    Map<String, int[]> breaksIn;
    int pageCount;
}

public final class Paginate {
    /** Lays the document out through export-fo and FOP's area tree and returns the map. Needs docx4j-export-fo on the classpath (reflective, as TocGenerator). */
    public static PaginationMap compute(WordprocessingMLPackage pkg, PaginateSettings settings) throws Docx4JException;
    /** Removes every w:lastRenderedPageBreak from the main document part and writes one at each boundary of the map. Returns the count written. */
    public static int applyLastRenderedPageBreaks(WordprocessingMLPackage pkg, PaginationMap map);
    /** compute then apply. */
    public static PaginationMap paginate(WordprocessingMLPackage pkg, PaginateSettings settings) throws Docx4JException;
}
```

The paragraph key is `w14:paraId` where the paragraph has one; otherwise a key docx4j
assigns for the run (`P<n>` by document order) and reports in the map. The map also covers
paragraphs in tables (they are blocks in the FO); headers, footers, footnotes and text boxes
are not keyed (their pages follow the body).

`PaginateSettings` wraps `FOSettings` (font mapper, features) plus `boolean lineBreaks`
(phase 2; off gives paragraph granularity only) and `boolean writeParaIds` (assign
`w14:paraId` to paragraphs lacking one, so the keys are stable in the file; off by default,
as it changes the document).

### 3.2 Ids into the FO

The non-XSLT exporter (`FOExporterVisitorGenerator`, the "best since 17.0.4" path) emits an
`fo:block` per paragraph; it gains an `id` attribute equal to the paragraph key when a
conversion feature `PP_FO_PARAGRAPH_IDS` is set (off by default: ids must be unique per FO
document and the feature costs a little). `BookmarkStartWriter` already sets `id` on the
inline it writes for `w:bookmarkStart`; the two must not collide, so paragraph ids are
prefixed (`p-<key>`). The XSLT path (`docx2fo.xslt`) gets the same template change so both
paths agree, but only the visitor path is required for this CR.

### 3.3 Reading the area tree

A new `PaginationAreaTreeHandler` (SAX, like `TocPageNumbersHandler`) walks the area tree:

- `pageViewport`: current page number (`formatted-nr`, else `nr`), page count.
- `block` with `prod-id="p-<key>"`: the first sighting of a key records `pageOf[key]`; a
  later sighting on a different page records a break. For phase 1 the break offset is the
  character count of the paragraph's text seen so far, i.e. the sum of the lengths of the
  `text`/`word`/`space` content of the `lineArea`s under earlier sightings; for phase 2 that
  count is kept exactly and mapped to a run offset (§3.4). Nested blocks (a table cell's
  paragraphs inside the table's block) are tracked by a stack of open `prod-id`s.
- Regions other than the body (headers, footers, footnote regions) are skipped, and so are
  areas under `block-container`s that export-fo uses for text boxes and floats, which is why
  those are not keyed.

The text seen in line areas is FOP's, after its own hyphenation and with tabs and leaders
expanded, so the count is approximate in paragraphs with tabs, fields or images. Phase 2
reconciles it against the paragraph's `TextUtils` text by aligning on the longest common
prefix of the line's first word, which is exact in the common case (plain runs) and lands
within a word otherwise.

### 3.4 Writing the markers

`applyLastRenderedPageBreaks` walks the main document part's paragraphs in document order
(`TraversalUtil`), removes every existing `R.LastRenderedPageBreak`, and for each break
offset in `breaksIn[key]` finds the run and offset by the same text walk `TextUtils` uses;
if the offset falls inside a `w:t`, the run is split there (a copy of `w:rPr`, the text
divided) and the marker becomes the first item of the second run, which is where Word puts
it. A paragraph whose page differs from the previous paragraph's gets a marker at its start
only when the previous paragraph did not end with a page break or `w:pageBreakBefore`
(Word writes no marker after an explicit break). Runs inside hyperlinks, content controls
and insertions are reached the same way; `w:del` content is skipped, as Word does not
render it. The part is marked changed as any edit does.

### 3.5 Use from the TOC generator and from docx4j-mcp

`TocGenerator.getPageNumbersMapViaFOP` becomes `Paginate.compute` plus a lookup of the
bookmark's paragraph key, removing `TocPageNumbersHandler` (phase 3; behaviour-preserving,
checked by the existing TOC tests). `docx4j-mcp` adds a `paginate` tool: input path, output
path, `line_breaks`; result the map as JSON and the docx with markers rewritten (phase 4, in
that repository's CR).

## 4. Alternatives considered

- **Bookmarks per paragraph instead of ids on `fo:block`.** What the TOC does today, and
  what the editor design first sketched. It changes the document before rendering (insert,
  then remove, bookmarks) and gives paragraph granularity only; the `id` on the block is
  cheaper and gives the line areas for phase 2. Rejected.
- **PDF and a text extractor.** Render to PDF, extract per-page text, align with the
  document's text. Works with any renderer (documents4j, LibreOffice), which is a real
  advantage for fidelity, but alignment is fragile with hyphenation and fields. Kept as a
  possible second backend (`PaginateSettings.backend`), not in this CR.
- **Word itself** (documents4j with Word installed). The only exact answer; a backend for
  the same API where Word is available. Out of scope here.

## 5. Value assessment

Small, contained change on top of existing machinery (the handler and writer are a few
hundred lines; the id emission is a feature flag in the visitor). It gives docx4j a
pagination query it never had, the TOC generator a simpler core, `docx4j-mcp` a `paginate`
tool, and the TypeScript editor its "re-paginate" without building a layout engine.
Accuracy is FOP's, which for the intended use (page boundaries a reader can trust to a line
or two, page numbers for navigation) is enough, and is the same accuracy the TOC's page
numbers have had for years.

## 6. Phases

1. **Paragraph granularity.** `PaginationMap`, `Paginate.compute` and
   `applyLastRenderedPageBreaks` at paragraph boundaries, `PP_FO_PARAGRAPH_IDS` in the
   visitor exporter, `PaginationAreaTreeHandler`. Tests: a fixture with known pages (the
   TOC tests' documents), round trip (markers written, document still opens in Word, TOC
   pages unchanged), a paragraph in a table cell.
2. **Line granularity.** Break offsets from line areas, run splitting in the writer, the
   alignment against `TextUtils` text. Tests: a long paragraph across two pages, a paragraph
   with a tab and a field, a run inside a hyperlink.
3. **TOC over Paginate.** `TocGenerator` uses `Paginate.compute`; `TocPageNumbersHandler`
   removed or reduced. Existing TOC tests unchanged.
4. **docx4j-mcp `paginate` tool** (that repository).

## 7. Risks and notes

- The area tree names (`prod-id`, `formatted-nr`, `key`, `word`, `space`) are FOP 2.9's XML
  renderer's; the handler is written against that version only (§1.3) and covered by a
  test that fails loudly on a missing attribute, which is what a future FOP upgrade trips.
- Fonts: FOP paginates with the fonts its font mapper resolves, so pages drift from Word's
  where fonts differ in metrics. This CR does not change that; `PaginateSettings` exposes the
  font mapper so callers with the document's fonts get closer.
- `w:lastRenderedPageBreak` inside a content control or hyperlink run is legal; inside a
  `w:del` run it is not written; inside a field result it is written like any run.
- The markers are advisory; consumers must treat them as "last rendering", not truth, and
  Word will replace them. The TypeScript editor draws them muted after an edit until the
  next `Paginate`.
- Documents with no `w14:paraId` get transient `P<n>` keys unless `writeParaIds` is set;
  callers that need stable keys across calls set it and accept the document change.

## 8. References

- ECMA-376 Part 1, 17.3.3.13 `lastRenderedPageBreak`.
- `org.docx4j.toc.TocGenerator#getPageNumbersMapViaFOP`, `org.docx4j.toc.TocPageNumbersHandler`,
  `org.docx4j.convert.out.fo.renderers.FORendererApacheFOP` (`MIME_FOP_AREA_TREE`),
  `org.docx4j.convert.out.fo.FOPAreaTreeHelper`, `org.docx4j.convert.out.fo.BookmarkStartWriter`.
- Apache FOP `org.apache.fop.render.xml.XMLRenderer` and `org.apache.fop.area.AreaTreeParser`.
- `plutext/docx4j-core-ts` CR-003 appendix D (the consumer).
