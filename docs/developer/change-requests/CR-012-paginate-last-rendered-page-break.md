# CR: Paginate: rewrite `w:lastRenderedPageBreak` from Apache FOP's area tree

Status: IN PROGRESS. Phase 1 shipped 2026-09-11 (§6). Requested by the docx4j-core-ts editor design
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

**Decision (Jason, 2026-09-11): the CR targets the FOP version in the tree, 2.11
(`docx4j-export-fo/pom.xml`), and does not support older FOP versions.** The names above
are 2.11's, checked in its jar
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
`w14:paraId` to paragraphs lacking one, so the keys are stable in the file).

**Decision (Jason, 2026-09-11): `lineBreaks` defaults on.** The editor is the consumer and
wants the in-paragraph markers; a caller wanting paragraph granularity turns it off.

**Decision (Jason, 2026-09-11): the layout is made as if all tracked changes were
accepted.** Word's marker records "the previous rendering", which is whichever markup view
Word was showing (with All Markup, deletions are laid out inline and take space; with No
Markup they do not); no Word-written document in the test corpus has both tracked changes
and markers, so its choice is not evidenced here, and the accepted view is the one that is
right for the editor. Consequences: deleted content (`w:del` runs, deleted paragraph marks,
deleted table rows, `w:moveFrom`) is removed and insertions (`w:ins`, `w:moveTo`) unwrapped
on the export's working copy before the layout (a conversion feature in the common
preprocess, phase 2: today the FO exporter draws `w:delText` red and struck through, which
takes space); the writer, which already passes over deleted runs, never puts a marker inside
`w:del`; a paragraph whose mark is deleted merges into the next in the layout, so its key is
absent from the map, which the writer treats as "continues the page" (§3.4).

**Decision (Jason, 2026-09-11): `writeParaIds` is on by default for `paginate` and
`applyLastRenderedPageBreaks`**, which rewrite the document's runs anyway, so that the keys
the markers were written against are in the file; `compute` alone is a query and assigns
transient `P<n>` keys without touching the document unless the caller sets it. Assigned ids
follow ECMA-376 17.3.1.20 as Word writes them: eight hex digits, below `0x80000000`, unique
in the document, on `w:p` (`w14:paraId` and `w14:textId` alike, the way Word pairs them).

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

**Revised for phase 2 (2026-09-11, after phase 1).** Aligning FOP's line text against the
paragraph's text is fragile: FOP's lines carry list labels, field results as FOP computed
them, footnote reference numbers and the hyphens it inserted, and drop the space at a line
end and the tab characters (rendered as leaders). Instead the offset is anchored per run:

- The exporter already writes an outer `fo:inline` per run (the area tree shows
  `lineArea > inlineparent > inlineparent > text > word`, the inner one being the font
  span), and FOP writes traits on inline areas as it does on blocks (the TOC reads
  `internal-link` there). With `PP_FO_PARAGRAPH_IDS` on, that inline gets
  `id="r-<key>-<n>"`, `n` the run's index among the paragraph's run items.
- The handler tracks the open run anchor as it does the open paragraph and records a
  break as (run `n`, characters of that run seen before the page boundary). The
  alignment problem shrinks to the one run's own `w:t` text, where FOP's text is exact
  except for a hyphen FOP appended at a line end and the trailing space it dropped, both
  corrected by looking at the run's text.
- The map still reports offsets in the paragraph's text; the writer maps them back to run
  and offset by walking the run items the same way.
- `TextUtils` is not the text model: it concatenates every text node of the marshalled
  XML, so it includes `w:instrText` and `w:delText` and gives nothing for a tab or break.
  Phase 2 defines its own per-run-item model, shared by exporter, handler and writer:
  `w:t` characters, one character for `w:tab`, `w:br`, `w:sym`, `w:noBreakHyphen`,
  `w:softHyphen`; nothing for drawings, pictures and field characters; field codes and
  deleted text excluded.
- Field results (PAGE, NUMPAGES, PAGEREF) render as FOP computed them, not as cached in
  the file, so an offset inside a field result can be off by the length difference.
  Documented, not corrected.

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

**Phase 2 writer, detailed (2026-09-11):** the split copies `w:rPr` and sets
`xml:space="preserve"` on both halves; the marker goes before the first character of the
new page's line, not after the space FOP dropped at the end of the previous one (Word:
`<w:t xml:space="preserve">end of line </w:t></w:r><w:r><w:lastRenderedPageBreak/><w:t>start`);
an offset at a run boundary puts the marker at the next run's start without a split; a
run inside a hyperlink, content control or insertion is split in its own parent's content
list (`Child.getParent()`); no split inside a field's code. The two shapes phase 1 left:
a paragraph "page break then text" gets the marker between the two (a split after the
`w:br`), and of two break-only paragraphs in a row the second gets one in its run, as
Word writes it. Table rows split across pages need nothing extra: each cell paragraph is
keyed and gets its own break.

**A paragraph split by the preprocessing** (`PageBreak.split`, a page break inside it)
becomes two `fo:block`s; today the second half is a `new P()` without a paraId, so the
`p-<key>~<n>` continuation ids of phase 1 never fire in practice. Phase 2 copies the paraId
onto the second half; its runs are numbered per part (`r-<key>~<part>-<n>`), and the
writer segments the paragraph's run items at its page breaks the same way. If the number
of parts in the map and in the document disagree (the preprocessing has more nuanced
rules than "split at every break": `keepBreakLine`, the compat setting, a break in first
position), that paragraph falls back to a paragraph-level marker.

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

   **Shipped 2026-09-11, commit bf2a8c908** (`org.docx4j.model.pagination` in docx4j-core; the id emission in
   `FOConversionContext.paragraphFoId` and `FOExporterVisitorGenerator.handleP`; tests
   `PaginationAreaTreeHandlerTest` in docx4j-core-tests and `PaginateTest` in
   docx4j-export-fo's own tests, in package `org.docx4j.convert.out.fo` because under JPMS
   a test in that module cannot join the core module's package). What the implementation
   settled, where it departs from the text above:

   - **Markers after explicit breaks (§3.4 corrected).** Word *does* write a marker after
     an explicit page break or section break: `header-no-rels.docx` has
     `<w:br w:type="page"/></w:r></w:p><w:p><w:r><w:lastRenderedPageBreak/><w:t>Second page`
     and the same after a `w:sectPr` paragraph; `odd_even_different_first_page.docx` has a
     marker in a paragraph whose only content is a page break. Its rule is run-based: the
     marker goes in the first run that starts on the page. The writer follows that.
   - **Where FOP puts a break paragraph.** The export's `PageBreak` preprocessing moves a
     break at the head of a paragraph in front of its block (break-before), or onto the
     next block when the paragraph holds nothing else, so the map has a break-only
     paragraph on the page *after* its break, or not at all; Word renders the break as the
     last line of the page before. Measured (`PaginateTest`): breaks before text vanish
     from the map, a break before a table or before another break paragraph is placed on
     the next page. So the writer gives no marker to a paragraph beginning with a page
     break (its first run starts on the page before) and takes a break-only paragraph to
     end on the page before the map's, so the following paragraph gets the marker, as in
     Word. Two break-only paragraphs in a row: Word marks the second (its break run is the
     first on its page); phase 1 does not, the page after is marked. A paragraph "break
     then text" needs its marker between the two, inside the run: phase 2.
   - **Page index and page number.** `PaginationMap` reports both: `getPageIndex` (1-based
     rendering order, what page boundaries are decided from) and `getPage` (FOP's
     formatted number, as the TOC shows it; not unique across numbering restarts).
     `getLastPageIndex` and `getBreaks` describe a paragraph that spans pages; the offsets
     are already populated (§3.3's character count), approximate until phase 2.
   - **Repeated blocks.** Where the preprocessing writes a paragraph twice (split at a
     page break inside it), the later blocks get `p-<key>~<n>` (`PaginationAreaTreeHandler.foId`),
     which FOP accepts (ids must be unique; strict validation is on) and the handler reads
     as the same paragraph, so the split records as a break. Two *different* paragraphs
     sharing a paraId (copy-paste) are handled before the layout: `compute` keys the
     second `P<n>` for the call, `paginate` gives it its own id.
   - **Text boxes.** Excluded in the exporter, not the handler: `AbstractWmlConversionContext`
     counts text-box nesting (`enterTextBox`/`exitTextBox`, around the VML and DrawingML
     text box traversals) and `paragraphFoId` writes no id inside one. The handler's first
     cut skipped absolutely positioned blocks instead, which dropped every table: the
     export wraps tables in a positioned block-container too.
   - **Empty paragraph starting a page** gets a `w:r` to hold its marker (Word writes none
     there, having no run; a consumer drawing boundaries needs it).
   - **`writeParaIds`** is a `Boolean` on `PaginateSettings`: null takes the entry point's
     default (§3.1 decision), and `applyLastRenderedPageBreaks(pkg, map)` assigns ids while
     the three-argument form lets a caller decline. `compute` sets the transient `P<n>`
     keys as the paragraphs' paraId for the duration of the layout only (the exporter
     reads the paraId), restoring them after. A map is refused (`IllegalArgumentException`)
     when the document's paragraphs no longer line up with its keys.
   - **Not done in this phase:** `PaginateSettings.lineBreaks` (phase 2 adds it with the
     in-paragraph writer); the `docx2fo.xslt` template change (the visitor pathway is the
     one required; the XSLT pathway emits no ids). `Paginate.parse(byte[])` is public for a
     caller with an area tree of their own.
2. **Line granularity.** Break offsets from line areas, run splitting in the writer, the
   alignment against `TextUtils` text. Tests: a long paragraph across two pages, a paragraph
   with a tab and a field, a run inside a hyperlink.

   **Plan revised 2026-09-11** (see §3.1, §3.3 and §3.4 as revised): run anchors
   `r-<key>-<n>` on the run's `fo:inline` in the exporter (~30 lines); the handler
   records (run, characters) per break and reads the continuation parts (~80 lines); the
   per-run-item text model and the writer's split (~200 lines); the accepted-view
   preprocess feature (deletions removed, insertions unwrapped, on the working copy);
   `PaginateSettings.lineBreaks`, default on; the paraId copied onto `PageBreak.split`'s
   second half. Tests: a long paragraph across pages (the two halves of the split run
   concatenate to the original text, `xml:space` kept), a tab and a field, a run inside a
   hyperlink, a hyphenated line end (fop-hyph is in test scope), a table row split across
   pages, "page break then text", two break-only paragraphs in a row, deleted text taking
   no space. About one focused session; the risk is the split-paragraph numbering, which
   the fallback keeps safe.
3. **TOC over Paginate.** `TocGenerator` uses `Paginate.compute`; `TocPageNumbersHandler`
   removed or reduced. Existing TOC tests unchanged.
4. **docx4j-mcp `paginate` tool** (that repository).

## 7. Risks and notes

- The area tree names (`prod-id`, `formatted-nr`, `key`, `word`, `space`) are FOP 2.11's XML
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
- Documents with no `w14:paraId` get transient `P<n>` keys from `compute`; `paginate` and
  `applyLastRenderedPageBreaks` assign ids by default (§3.1), so a document that has been
  paginated once keys stably from then on. Assigning ids needs the `w14` namespace declared
  and listed in `mc:Ignorable` on `w:document`, which docx4j's marshalling handles.

## 8. References

- ECMA-376 Part 1, 17.3.3.13 `lastRenderedPageBreak`.
- `org.docx4j.toc.TocGenerator#getPageNumbersMapViaFOP`, `org.docx4j.toc.TocPageNumbersHandler`,
  `org.docx4j.convert.out.fo.renderers.FORendererApacheFOP` (`MIME_FOP_AREA_TREE`),
  `org.docx4j.convert.out.fo.FOPAreaTreeHelper`, `org.docx4j.convert.out.fo.BookmarkStartWriter`.
- Apache FOP `org.apache.fop.render.xml.XMLRenderer` and `org.apache.fop.area.AreaTreeParser`.
- `plutext/docx4j-core-ts` CR-003 appendix D (the consumer).
