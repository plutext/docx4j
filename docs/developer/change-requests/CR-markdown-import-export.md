# CR: Markdown import/export (markdown→docx and docx→markdown)

Status: IN PROGRESS (2026-09-01) — phases 0-4 complete; naming/placement DECIDED
2026-09-01 (jharrop): the module is **`docx4j-markdown`**, a **reactor module**.
Scope: a NEW reactor module `docx4j-markdown` — import (markdown→wml) and
export (wml→markdown); docx4j-core changes limited to whatever small hooks the
mappings need
Related: CR-mcp-server.md (a `markdown_to_docx` / `docx_to_markdown` tool pair is
that CR's natural phase-2+ extension — agents author in markdown even more
naturally than HTML); docx4j-ImportXHTML (the existing rich-text import pathway,
and the fallback for embedded HTML); CR-fo/html-exporter-parity (the lesson
below about single implementations)

## 1. Background

Markdown is the lingua franca of programmatic and AI-generated content; docx is
what that content is delivered in.  Today docx4j users bridge the two via HTML
(markdown → HTML → ImportXHTML), which works but is indirect: fidelity is
HTML/CSS-shaped (lists as indents rather than real numbering, styles via CSS
mapping) and the ImportXHTML stack is a heavy dependency for the purpose.
There is no docx→markdown at all.

### Parsing: use commonmark-java; do not write a parser

Decision proposed up front, since it was the originating question:

- **commonmark-java** (`org.commonmark:commonmark`, 0.30.x) is the
  reference-quality Java implementation: conformant against the CommonMark spec
  suite, small core, **BSD 2-Clause**, actively maintained, and **Java 11+ — the
  same baseline as docx4j**.  Extension artifacts cover exactly what a docx
  mapping wants: GFM tables, strikethrough, task-list items, footnotes, YAML
  front matter, autolink.  It also has a `MarkdownRenderer` (AST → markdown
  text), which matters for the export side (§4).
- BSD-2-Clause is in Apache's own Category A of permitted licenses: an ASLv2
  project may depend on it and bundle it, with attribution preserved.  No
  copyleft concern; practically indistinguishable from an Apache-2.0 dependency
  for our purposes.  (Strictly-Apache-2.0 alternatives are weaker: JetBrains'
  intellij-markdown drags in Kotlin; txtmark is unmaintained.)
- Writing our own parser buys nothing: CommonMark conformance (emphasis
  delimiter runs, HTML blocks, link reference definitions, list tightness, tab
  rules — on the order of 650 spec cases) is a permanent tax, and the
  differentiated work is entirely in the wml mapping, which no parser provides.

### Prior art: flexmark-java

flexmark-java (also BSD-2) ships `flexmark-docx-converter`, built **on docx4j**.
So markdown→docx via docx4j already exists in the ecosystem.  Why first-party
anyway: control of the mapping (styles, real numbering, footnotes — consistent
with how docx4j users expect documents to be built), support under one roof,
docx→markdown (flexmark doesn't do that direction), and the MCP/website story.
We deliberately do NOT compete on dialect breadth — CommonMark + GFM extensions
only; users needing exotic dialects keep using flexmark.  BSD-2 permits studying
its mapping choices with attribution where helpful.

### Lesson carried over from the exporter-parity CRs

One implementation per direction.  The export is a single visitor-style
serializer over the JAXB tree (TraversalUtil); there is no XSLT twin, ever.

## 2. Module shape

- **`docx4j-markdown`**, in the reactor (unlike the MCP server there is no
  Java-floor conflict: commonmark-java is Java 11+).  Depends on docx4j-core +
  `org.commonmark:commonmark` + the ext artifacts used (tables, strikethrough,
  task-list-items, footnotes, yaml-front-matter).  ImportXHTML remains optional
  (reflection), used only for embedded-HTML handling if configured (§3).
- Entry points: `MarkdownImporter` (markdown string/stream + options →
  content list added to a package/part) and `MarkdownExporter` (package/part +
  options → markdown string/stream).  `Docx4J` facade hooks
  (`toMarkdown`/`fromMarkdown` via reflection, like the FO/documents4j
  exporters) can come once the API settles — phase 5.
- Both directions take an **options object** (target styles, image handling,
  embedded-HTML policy, extension toggles) — no Docx4jProperties globals for
  per-call behavior.

## 3. Import mapping (markdown AST → wml)

Route: walk the commonmark AST directly into wml (not via HTML).  The importer
builds into a caller-supplied `WordprocessingMLPackage` (or a fresh
`createPackage()`), so a **styles template docx** can supply house styles; the
importer ensures the styles it references exist (adding minimal definitions
where absent, via the styles part).

| Markdown (CommonMark) | wml |
|---|---|
| Heading level n | `w:pStyle` `Heading1..6` (styleIds — stable across localized Word UIs, unlike display names) |
| Paragraph | Normal (or the template's default) |
| Emphasis / Strong | `w:i` / `w:b` runs |
| Inline code | a character style (create `CodeChar` if absent: mono font, shading) |
| Fenced / indented code block | a paragraph style (`SourceCode`-like: mono, shading, no proofing); lines separated by `w:br` within one paragraph (keeps the block one unit) — decision to revisit in phase 1 vs one-para-per-line |
| Block quote | `Quote` style; nesting via additional indentation |
| Bullet / ordered lists | **real numbering**: one abstractNum for bullets, one for decimal, `w:ilvl` from nesting depth; new `w:num` per top-level list so ordered lists restart correctly; tight vs loose lists → spacing (contextualSpacing) |
| Link | `w:hyperlink` + rel (external); link reference definitions resolved by the parser |
| Image | pluggable handler (cf `ConversionImageHandler` precedent): local paths and data URIs embedded via BinaryPartAbstractImage; **remote URLs are NOT fetched by default** (security §6) — emitted as hyperlinked alt text unless the caller supplies a fetching handler |
| Thematic break | paragraph with bottom `w:pBdr` (NOT the legacy `v:rect o:hr` idiom, which the FO/HTML exporters can't render) |
| Hard/soft line break | `w:br` / space |
| HTML block / inline HTML | policy option: `DROP` (default), `LITERAL` (as text), or `IMPORT_XHTML` (route through ImportXHTML when on classpath) |

GFM extensions (phase 2): tables → `w:tbl` with a table style (header row from
the delimiter row, per-column alignment via `w:jc`); strikethrough → `w:strike`;
task lists → checkbox glyphs (☐/☒) or optionally `w14:checkbox` sdts;
footnotes → **real footnotes** (footnotesPart + `w:footnoteReference` — the
machinery is well understood after the exporter-parity work); YAML front
matter → core document properties (title/author/keywords) where keys match,
else ignored.

## 4. Export mapping (wml → markdown)

A TraversalUtil-based walker builds a **commonmark AST**, then
`MarkdownRenderer` serializes it — escaping (literal `*`, `_`, `|` in cells,
etc.) is the genuinely fiddly part of markdown *generation*, and the renderer
does it correctly for free.  Phase 0 must verify how much extension coverage
MarkdownRenderer has (tables in particular); where an extension has no markdown
renderer, fall back to hand-emitting that construct into a raw-text node.

Reverse mappings, with the important detection choices:

- **Headings via effective `outlineLvl`** (PropertyResolver), not style-name
  matching — robust across templates and localized style names; outlineLvl 0-5
  → `#`..`######`.
- Bold/italic/strike/code from **effective rPr** (code = mono font or the
  CodeChar-style); avoids missing style-carried formatting.
- Lists via the numbering model (Emulator/ListNumbering): numFmt bullet →
  `-`, decimal → `1.`; ilvl → nesting; anything fancier (roman, multilevel
  text) degrades to the nearest of the two with the literal number text lost
  (documented).
- Tables → GFM pipes; block content inside cells flattens to inline with
  `<br>`; vMerge/gridSpan degrade (top-left wins, empties elsewhere) — GFM has
  no spans.  Documented lossiness.
- Hyperlinks → `[text](target)`; images → extracted to an images directory
  (like the HTML export's imageDirPath) with relative links, or data URIs by
  option.
- Footnotes → the footnotes extension syntax.
- Explicitly lossy and documented: headers/footers ignored; fields → their
  cached result text; content controls → their content; textboxes/VML →
  dropped with a warning; tracked changes → option `ACCEPT` (default) or
  `MARKUP` (`~~del~~` / ins as plain).

**Fidelity bar and test strategy**: round-trip stability — for documents within
the CommonMark+GFM subset, md → docx → md must be idempotent after the first
trip.  Golden round-trip tests are the backbone (in docx4j-core-tests or the
module's own test tree), plus docx-side assertions for the import mapping
(styles, numbering, footnotes present) in the HtmlVisitorParityTest style.

## 5. Phases

0. **Spike** (S): commonmark AST walking + MarkdownRenderer extension coverage
   (tables!); confirm the ext artifacts' licenses match core (BSD-2); module
   skeleton in the reactor.  Findings recorded here.

   **DONE 2026-09-01.  Findings** (against commonmark-java **0.30.0**, the
   latest release, 2026-08-06):
   - **MarkdownRenderer covers every extension we use.**  All five ext
     artifacts ship a `MarkdownNodeRenderer`: `TableMarkdownNodeRenderer`,
     `StrikethroughMarkdownNodeRenderer`, `TaskListItemMarkdownNodeRenderer`,
     `FootnoteMarkdownNodeRenderer`, `YamlFrontMatterMarkdownNodeRenderer`.
     Tables render with per-column alignment preserved.  The hand-emission
     fallback contemplated in §4/§6 is NOT needed.
   - **Round trip is idempotent after the first trip** (parse→render→parse→
     render is a fixed point) for a document exercising all extensions.
     Both findings are pinned as executable assertions in
     `CommonmarkSpikeTest`, so a commonmark upgrade that regresses them fails
     the build.
   - **Licenses**: all artifacts BSD-2-Clause (Bundle-License in each
     manifest).  Bytecode is class-file 55 (Java 11) including
     `module-info.class`, so the parent's enforce-bytecode-version rule passes.
   - **JPMS**: proper modules (not just Automatic-Module-Name):
     `org.commonmark`, `org.commonmark.ext.gfm.tables`,
     `org.commonmark.ext.gfm.strikethrough`,
     `org.commonmark.ext.task.list.items`, `org.commonmark.ext.footnotes`,
     `org.commonmark.ext.front.matter`.
   - **Module skeleton** built and installed in the reactor (after
     docx4j-docx-anon in `<modules>`): pom, `module-info.java`, README
     (incl. the flexmark comparison note §6 asks for), and the entry-point
     API surface (`MarkdownImporter`/`MarkdownExporter` +
     `MarkdownImportOptions`/`MarkdownExportOptions`) with conversion methods
     stubbed to throw until phases 1/3.
1. **Import core** (M): CommonMark constructs (headings, paragraphs, emphasis,
   lists with real numbering, code, quotes, links, hr, line breaks), styles
   template support, HTML policy option (DROP/LITERAL only).  Tests: mapping
   assertions per construct.

   **DONE 2026-09-01.**  `MarkdownToWmlVisitor` (AbstractVisitor over the
   commonmark AST) + `ImportStyles` (KnownStyles activation via
   PropertyResolver; minimal `CodeChar`/`SourceCode` definitions created only
   when the template lacks them) + `ImportNumbering`.  Decisions taken:
   - **Code block shape DECIDED: one paragraph with `w:br` is the default**
     (`MarkdownImportOptions.CodeBlockShape.SINGLE_PARAGRAPH`); both shapes
     are implemented and tested, `PARAGRAPH_PER_LINE` selectable per call —
     so the trade-off (block unity vs per-line styling) is the caller's.
   - **Numbering**: each top-level list is pre-scanned for a per-depth
     bullet/ordered *signature* from which a 9-level abstractNum is built —
     so mixed nesting (ordered-in-bullet etc.) gets correct markers at every
     level.  Any list with an ordered level gets its own abstractNum+num
     (restart correct by construction; `start` honoured via `w:start` on
     lvl 0); bullet-only lists share one num per signature.  Tight/loose →
     `w:contextualSpacing` true/false; follow-on paragraphs in an item get
     matching `w:ind` but no numPr.
   - Block quotes win over list context (a quote inside a list item is
     styled `Quote`, no numPr); nesting depth ≥2 adds 720 twips indent each.
   - Images (ahead of phase 2's handler) degrade to hyperlinked alt text —
     the documented no-fetch default.
   - 19 mapping tests incl. a save-round-trip (marshal) check and a
     styles-template-not-clobbered check.  NB the default (createPackage)
     styles part already defines Heading1-4/Hyperlink; Quote, Heading5-6 and
     ListParagraph come from KnownStyles.xml activation.
2. **Import extensions + images** (M): GFM tables, strikethrough, task lists,
   footnotes, front matter; image handler with embed-local/link-remote default;
   IMPORT_XHTML policy via optional ImportXHTML.

   **DONE 2026-09-01.**  Options gained per-extension toggles
   (`MarkdownImportOptions.Extension`, all on by default) and a pluggable
   `MarkdownImageHandler`.  Notes:
   - **Tables**: `TableGrid` style (default part has it; minimal bordered
     definition created if a template lacks it), header row gets
     `w:tblHeader` + bold runs, per-column alignment from the delimiter row
     as `w:jc` on cell paragraphs (LEFT omitted).
   - **Footnotes**: real footnotes — `ImportFootnotes` lazily initialises
     the footnotes part (separator/continuationSeparator) + settings
     `w:footnotePr` (FootnoteAdd-sample idiom); definitions realised on
     first reference (repeat references share one footnote id); first
     definition paragraph restyled `FootnoteText` with the
     footnoteRef-marker run prepended.  Inline footnotes (`^[..]`) also
     handled if the parser produces them.
   - **Task lists**: glyphs ☒/☐ (the marker precedes the item's paragraph in
     the AST, so it is held and prepended when the paragraph starts);
     the `w14:checkbox` sdt alternative was NOT implemented (revisit on
     demand).
   - **Front matter**: title/author/keywords → dc:title/dc:creator/
     cp:keywords; other keys ignored.  NB commonmark's YAML support is
     minimal: block lists yes, flow sequences (`[a, b]`) stay literal.
   - **Images**: `DefaultMarkdownImageHandler` embeds data URIs (base64)
     and local files (relative paths only when a baseDir is supplied);
     http(s) is never fetched — declines, and the importer emits
     hyperlinked alt text.  Custom handlers return run content (w:drawing).
   - **IMPORT_XHTML**: reflection into
     `org.docx4j.convert.in.xhtml.XHTMLImporterImpl` for HTML *blocks*;
     inline HTML arrives tag-by-tag so it cannot be routed and is dropped
     under this policy (documented in code).
   - 10 further tests (29 total in the module).
3. **Export core + round-trip** (M): CommonMark subset out, via AST +
   MarkdownRenderer; golden round-trip suite established.

   **DONE 2026-09-01.**  `WmlToMarkdown` — a direct recursive walk of the
   JAXB content tree building a commonmark AST (same one-implementation
   spirit as "TraversalUtil-based"; a flat visitor doesn't fit tree
   building), rendered by MarkdownRenderer so escaping is the reference
   implementation's.  Detection as specified in §4, plus findings:
   - **Heading check must precede the numbering check**: Word's built-in
     Heading styles (KnownStyles Heading5/6) carry legacy `w:numPr`, which
     otherwise exports headings as absurd nested bullets.
   - **Baseline-relative formatting**: bold/italic/code compare the run's
     effective rPr against the *paragraph style's* effective rPr
     (`getEffectiveRPr(null, directPPr)`), so a Heading's inherent bold
     doesn't become `**markers**`.  NB pass the DIRECT pPr — 
     `getEffectiveRPr(rPr, pPr)` resolves via `pPr.getPStyle()`, which the
     effective pPr no longer has.
   - Inline code: `CodeChar` rStyle or effective mono ascii font (small
     font-name allowlist) where the baseline isn't mono.
   - Lists: state machine over consecutive numbered paragraphs; per-level
     bullet/ordered from the numbering model (`ListLevel.IsBullet()`);
     gotcha: **`ListLevel.getStartValue()` is `w:start` minus one** (counter
     semantics) — add 1 back.  Tight/loose from effective
     contextualSpacing; ListParagraph follow-on paragraphs rejoin the item.
   - Synthesized `ThematicBreak` needs `setLiteral("---")` (else the
     renderer emits `___`); Strong nests INSIDE Emphasis so `***x***`
     round-trips (the other nesting renders `**_x_**`).
   - Fields: begin/separate/end tracked, instruction text skipped, cached
     result kept; fldSimple content kept; SDTs contribute their content;
     empty paragraphs dropped (markdown has none); tables logged+dropped
     until phase 4.
   - **Round-trip bar implemented as canonical-equality**: for in-subset
     documents, export-after-import must equal commonmark's own
     `render(parse(md))` — a stronger, self-maintaining form of the
     idempotence requirement (13 golden tests + 7 export-detection tests).
4. **Export extensions** (S-M): tables, strikethrough, footnotes, image
   extraction; tracked-changes option.

   **DONE 2026-09-01.**  As specified in §4, with these notes:
   - **Tables**: first row is the header; gridSpan pads with empty cells so
     columns stay aligned; vMerge continuations become empty cells (top-left
     wins); multi-paragraph cells flatten with `<br>` (HtmlInline node);
     nested tables dropped with a warning.  Header-row bold is suppressed
     (it's convention, not markup) so imported tables round-trip.
   - **Footnotes**: `w:footnoteReference` → `[^id]` (labels are normalized
     to footnote ids — a `[^note]`-style name doesn't survive the docx trip);
     referenced definitions are converted from the footnotes part (with the
     part's own rels active for links/images inside) and appended in
     reference order; the footnoteRef marker run's leftover space is
     trimmed.  Endnotes are not mapped (`CTFtnEdnRef` is shared — the
     JAXBElement name distinguishes footnoteReference from
     endnoteReference).
   - **Images**: data URI by default (self-contained, no filesystem writes);
     `setImageDirPath` extracts to files (`image1.png`, ...) with
     `setImageTargetUri` as the link prefix (mirrors the HTML export's
     imageDirPath).  Alt text from docPr descr (falling back to name).
     A data-URI image round-trips byte-identical.
   - **Tracked changes**: ACCEPT (default) keeps `w:ins` content and drops
     `w:del`; MARKUP renders deletions as `~~strikethrough~~`.  Gotcha:
     **`w:delText` unmarshals to `org.docx4j.wml.DelText`, which does NOT
     extend `Text`** — it needs its own instanceof.
   - Task lists are import-only: the ☒/☐ glyphs export as literal text, not
     `[x]` markers (documented lossiness).
   - 3 more golden round-trip tests (table/strikethrough/footnotes are
     canonical-equal) + 7 export-extension tests (59 total in the module).
5. **Integration** (S): `Docx4J.toMarkdown`/`fromMarkdown` facade hooks
   (reflection); MCP tools `markdown_to_docx` / `docx_to_markdown` in the MCP
   server CR's tool surface; docs + website mention; CHANGELOG.

## 6. Risks / open questions

- **Remote images**: fetching URLs from inside a library invites SSRF and
  supply-chain surprises; default is link-don't-fetch, with fetching only via a
  caller-supplied handler.  Same posture as the MCP server CR.
- **MarkdownRenderer extension gaps**: if tables (or others) can't render to
  markdown, the export needs hand-emission for those nodes — contained risk,
  verified in phase 0.
- **Code block shape** (one paragraph with w:br vs paragraph-per-line):
  affects copy/paste behavior in Word and round-trip fidelity; decide in
  phase 1 with both prototyped.
- **List numbering edge cases**: ordered-list `start` values, interrupted
  lists, and the tight/loose distinction are where markdown↔numbering.xml
  impedance shows; the round-trip suite is the guard.
- **flexmark coexistence**: message discipline — docx4j-markdown is
  CommonMark+GFM with first-party mapping quality; flexmark remains the answer
  for exotic dialects.  A short comparison note in the module README avoids
  confused bug reports.
- **Scope creep**: markdown has no styles, columns, sections, or floating
  anything; resist inventing syntax.  Everything outside CommonMark+GFM is
  lossy by design and documented, not extended.
- **Naming**: DECIDED 2026-09-01 (jharrop): `docx4j-markdown`, as a reactor
  module (not a separate repo).

## 7. Suggested sequencing and effort (rough)

| Phase | Effort | Value |
|-------|--------|-------|
| 0 Spike | S | De-risks renderer coverage + module shape |
| 1 Import core | M | The most-wanted half (agents/pipelines author markdown) |
| 2 Import extensions | M | Tables/footnotes are where HTML-route fidelity hurts today |
| 3 Export core | M | The half nobody else offers on docx4j |
| 4 Export extensions | S-M | Completes round-trip |
| 5 Integration | S | Facade + MCP + docs |

If only phases 0-2 are ever done, that is already the headline feature:
first-party markdown→docx with real styles, numbering, tables and footnotes —
no HTML detour, no third-party converter.
