# CR-030: table styles behind the resolver - a paragraph's table context handed to `PropertyResolver`, the synthetic styles reduced to names

Status: IN PROGRESS. Proposed 2026-10-03; phase 0 started the same day (Jason: "start phase 0"):
the six probes are in `Corpus.java` and on the share, and their Word goldens are awaited. The
decisions of §7 are still open.
Carried forward from CR-015 ("Layering", Jason 2026-09-12: "we should be getting it right in
the resolver so we are not layering fix upon fix"). Drafted with Claude Opus 5.5.
Owner: Jason Harrop.

Scope: which properties a table style gives the paragraphs and runs of its table, worked out
in one place, `PropertyResolver`, from a context the caller hands it, the way the numbering
part is already handed to it. In: `PropertyResolver`, `org.docx4j.model.table`
(`TableStyleConditions` and a new table/cell context), the preprocess
`ParagraphStylesInTableFix`, the FO visitor's paragraph and run handling, the table writers'
own copy of the condition arithmetic (`AbstractTableWriterModel`), and the numbering
`Emulator`'s style-id test. Out: how the table is laid out once its properties are known
(region borders, header rows, column widths: the writers and the FO layer, as now); HTML's
use of style ids as CSS classes (kept, see §4.5).

Related: CR-015 (the resolver; its Layering section names this CR); #546 phases 1-3
(67ab3b831, 7125a1ab0: conditional formatting, in the preprocess and the writers); CR-014
(numbering; `Emulator.styleLinkedElsewhere`); CR-021 (the walk's `McMode.ALL`); the ports,
which reproduce the resolver and left table conditions out pending this CR
(core-ts CR-001, python CR-002 item 6).

## 1. What docx4j does today (read from the code, 2026-10-03)

**The resolver knows nothing of tables.**  `getEffectivePPr(PPr)` and
`getEffectiveRPr(RPr, PPr)` compose document defaults, the paragraph style chain, the
character style and direct formatting. A paragraph in a table gets nothing from the table
style, neither its own `w:pPr`/`w:rPr` nor its conditional formats (`w:tblStylePr`).
Measured: the resolver gives a header-row paragraph of a table whose style makes `firstRow`
bold `b = false`.

**The exporters are shielded by a preprocess that is a second resolver.**
`ParagraphStylesInTableFix` (in the FO and HTML feature sets, both pathways of each) walks the
main document, headers and footers with a stack of tables. For each paragraph in a table it
builds a *synthetic paragraph style*, one per (paragraph style, table style, applicable
conditions), named e.g. `Normal-PlainTable1-firstCol-firstRow-nwCell-BR`, with no
`w:basedOn`. It writes that id over the paragraph's `w:pStyle` and activates the style in the
resolver and the styles part. The synthetic style is composed there, not in the resolver:

    document defaults
    + the table style's own pPr/rPr, then the conditions in ECMA-376-1 §17.7.6 order
    + the paragraph style's chain, as a level (the toggles XOR with the table level, §17.7.3)
    + MS-DOCX overrideTableStyleFontSizeAndJustification: when the setting is off (the
      default), a 12pt default paragraph style does not override the table style's size,
      and a left-aligned one does not override its justification

and it then writes `overrideTableStyleFontSizeAndJustification = 1` into the exporter's copy
of the settings, since the rule has already been applied.

**The table writers compute the same conditions again.**  `AbstractTableWriterModel` takes the
table style from `PropertyResolver.getEffectiveTableStyle(tblPr)` and works out each row's
and cell's conditions itself (its own look, band sizes and row index) for the conditional
`w:trPr`/`w:tcPr`/`w:tblPr` (#546 phases 2 and 3). So the look, band and grid arithmetic is
done twice, by two walks.

**And they choose the table style by different rules.**  The preprocess applies the
document's default table style to a table naming none, unless that style is *named*
"Normal Table", and stops a `w:basedOn` chain at any style so named.
`getEffectiveTableStyle` uses Word's *built-in* Normal Table, by *id* (the default style's),
in place of the document's definition. That rule was measured in CR-015 probe P5, for cell
margins only.

**Who misses out.**  Anything that resolves without running the export preprocess gets no
table style at all: `WmlToMarkdown`, docx4j-mcp's tools, user code asking "what formatting
does this paragraph have", and the ports' parity goldens, which docx4j's resolver generates.
The XSLT pathways could not be handed a context anyway: they unmarshal `w:pPr` fragments
with no parents. That was the reason given in 67ab3b831 for putting the conditions in the
preprocess.

## 2. Defects found (measured 2026-10-03 with a scratch probe, not committed)

The probe was a document with a `ListNumber` paragraph style numbered through its own
`w:numPr`, whose numbering level names it back (`w:lvl/w:pStyle`, as Word's built-in list
styles do). It has two such paragraphs outside a table and two inside, a table style `Grid`
with `firstRow` bold, and a VML text box anchored in a first-row cell. It was run through the
preprocess, `Docx4J.toHTML` and `Docx4J.toFO` (PDF), on 17.3.1-SNAPSHOT.

- **D1. Style-numbered paragraphs in a cell lose their number, and the list's count skips
  them.**  The cell paragraph's `w:pStyle` becomes `ListNumber-Grid-firstRow-BR`, which has no
  `w:basedOn`. `Emulator.styleLinkedElsewhere` walks the paragraph's style chain looking for
  the style the level names, does not find `ListNumber`, and reports "level 0 of numId 1 is
  linked to a paragraph style other than 'ListNumber-Grid-firstRow-BR'". Result: PDF and HTML
  both print `1. outside one`, the two cell paragraphs unnumbered, then `2. outside two`. The
  paragraphs should be numbered 1 to 4 (§5 T4 is the Word check). Corpus: about 498 paragraphs
  in 7 of 446 documents, one of them 458. That count comes from a scan by style id and does
  not check that numId and level match.
- **D2. A text box anchored in a cell takes the cell's table formatting.**  `TraversalUtil`
  descends into `w:txbxContent`, and the walk's table stack is still pushed there, so the text
  box's paragraph got `Normal-Grid-firstRow-BR` (bold). A text box is a story of its own, and
  Word is not expected to apply the table style to it (§5 T3). Corpus: 10 documents have a
  text box anchored in a table cell.
- **D3. DEBUG logging aborts the preprocess.**  With DEBUG on for `StyleRenamer`,
  `getCellPStyle` marshals each applicable `CTTblStylePr` for its log line. That type has no
  `@XmlRootElement`, so the marshal throws. The exception escapes the walk, and `process`
  catches and logs it, so the rest of the body and every header and footer are left
  unrenamed. It happens only with that
  logger at DEBUG, which is exactly when someone is investigating table formatting.
- **D4. Two rules for which table style applies** (§1). On the corpus they agree on every
  table: no document has a default table style not named "Normal Table", or a non-default
  style so named. They also agree on what they ignore. Six documents (five de-DE) give their
  Normal Table run properties (Times New Roman 10pt, `de-DE`), and both readings ignore them.
  Whether Word does is §5 T1. The name-against-id case is T2.
- **D5 (a gap, not a defect).**  Footnotes, endnotes and comments are not walked, so a table in
  a note gets no table style. The corpus has none. It follows for free once the context comes
  from the walk that renders the note.

## 3. Corpus facts (the three real-document corpora, 446 documents; scanned 2026-10-03)

| | documents | |
|---|---|---|
| with tables | 384 | 5,973 tables, 2,713 naming no `w:tblStyle` |
| a table style giving the whole table `w:pPr`/`w:rPr` | 176 | the synthetic style's original job |
| a table style with conditional `w:pPr`/`w:rPr` | 31 | #546 phase 1 |
| nested tables | 42 | each nested table has its own context |
| tables inside text boxes | 5 | |
| a text box anchored in a cell | 10 | D2 |
| `w:sdt` around rows or cells | 8 | |
| paragraph `w:cnfStyle` | 23 | joins the conditions (67ab3b831) |
| `overrideTableStyleFontSizeAndJustification` = 1 | 288 | absent in the other 158 |
| setting off, tables, and the 12pt rule can fire | 2 | table style sets `w:sz`, default paragraph style 12pt |
| setting off, tables, and the left-`w:jc` rule can fire | 3 | |
| style-numbered paragraphs in cells, level linked to the style | 7 | D1 |

## 4. Design

### 4.1 The rule

Restated from CR-015's Layering: what properties apply to a paragraph or run, *given where it
sits*, is `PropertyResolver`'s. Where the resolver lacks the context, the caller hands it the
context. The document is not rewritten to suit the resolver. The table style is that context's
first and largest case.

### 4.2 A table context and a cell context (`org.docx4j.model.table`)

- **`TableContext`**, built by the resolver from a `w:tbl` (`resolver.tableContext(tbl)`), holds:
  - the effective table style (`getEffectiveTableStyle`, the one rule for which style applies,
    with T1 and T2 applied);
  - the look (the table's `w:tblLook`, else the style's, else Word's default 04A0);
  - the band sizes;
  - each row's index and each cell's first grid column and span. This is one implementation
    of the grid arithmetic, replacing the preprocess's and the writers' copies:
    `w:gridBefore`, then the spans, nested tables excluded.
- **`CellContext`**, an immutable value: `tableContext.forParagraph(tr, tc, pPr)` (or
  `forCell(tr, tc)` for the writers). It holds the table style id, the conditions
  (`TableStyleConditions.resolve` with the row, cell and paragraph `w:cnfStyle` caches), the
  applicable `w:tblStylePr` entries in precedence order, and a key (the
  `TableStyleConditions.key` the synthetic ids use today).

### 4.3 The resolver composes it

New overloads; a null context means outside a table, which is exactly today's behaviour:

    getEffectivePPr(PPr, CellContext)
    getEffectiveRPr(RPr, PPr, CellContext)
    getEffectiveParagraphMarkRPr(PPr, CellContext)

    effectivePPr = docDefaults.pPr + table(ctx).pPr + chainPPr(styleOf) [+ compat rule] + direct
    effectiveRPr = docDefaults.rPr + level(table(ctx).rPr, chainRPr(styleOf)) [+ compat rule]
                   + level(character style) + direct
    table(ctx)   = the table style's own pPr/rPr, then ctx's applicable conditions in order

The composition is the preprocess's, moved, not reinvented. The compat rule reads the
setting through `DocumentSettingsPart.overrideTableStyleFontSizeAndJustification()`, which
already exists, and the preprocess stops writing `1` into the exporter's settings. Caches are
keyed (styleOf, ctx key), which is the same cardinality as the synthetic styles today:
`ConcurrentHashMap`s holding immutable values, under CR-015's live-object contract.

### 4.4 How a caller gets the context

1. **A walker** extends or composes a **`TableContextTracker`**, a `TraversalUtil` callback
   that keeps the table stack: the preprocess's walk, made reusable. It starts an empty stack
   inside `w:txbxContent` if T3 confirms D2.
2. **One paragraph in a loaded tree**: `resolver.cellContextOf(P)` follows parent pointers
   (`P > Tc > Tr > Tbl`). Measured 2026-10-03: the pointers are there after unmarshal, after
   `WordprocessingMLPackage.clone()` and after `XmlUtils.deepCopy`. They are absent for
   content built with `ObjectFactory` and added through `getContent().add()`, and in the
   XSLT pathways' fragments. The method returns null where the chain breaks, and a text box's
   paragraph stops at `CTTxbxContent`, which suits T3.
3. **The XSLT pathways** have neither, and use names (4.5).

### 4.5 Synthetic styles become names the resolver hands out

The preprocess stays for the HTML pathways and for the FO XSLT pathway
(`FLAG_EXPORT_PREFER_XSL`). For HTML that is representation: a style id is a CSS class
(Layering rule 3).

- **Naming:** the preprocess asks `resolver.styleIdFor(styleId, ctx)` for the id, named as
  today.
- **Resolution:** a `w:pStyle` naming such an id resolves as (source style, context). The XSLT
  pathways therefore resolve correctly with no context of their own.
- **Content:** the `w:style` written into the styles part (for `HtmlCssHelper`/`StyleTree`'s
  CSS) is built from the resolver's answer, so nothing composes table properties a second way.
- **The document's own style:** code that needs it calls `resolver.sourceStyleOf(id)`.
  `Emulator` does, which fixes D1 on every pathway.

### 4.6 The FO visitor needs no synthetic styles

The visitor walks the `w:tbl`/`w:tr`/`w:tc` objects themselves, so it keeps a
`TableContextTracker` and resolves in context. It sets the current `CellContext` on the
conversion context on entering a cell and clears it on leaving. The shared FO code
(`XsltFOFunctions.createBlockForPPr`, `handleRPr`, `RunFontSelector`'s line-height pPr) reads
it from there. It is null on the XSLT pathway, where the name carries it.
`PP_COMMON_TABLE_PARAGRAPH_STYLE_FIX` then leaves the FO visitor pathway's feature set: one
whole-document walk and rename fewer.

### 4.7 The writers share the table context

`AbstractTableWriterModel` takes row and cell conditions from the resolver's `TableContext`,
and its own look, band and index computation goes. What the writers do with the conditional
`w:trPr`/`w:tcPr`/`w:tblPr` stays theirs: region borders, header rows, the spanning-header
guard.

### 4.8 Where each rule lives afterwards

| rule | today | after |
|---|---|---|
| which table style applies | preprocess (by name) and resolver (by id) | resolver (one rule, T1/T2) |
| look, bands, grid position, `w:cnfStyle` | preprocess and writer model | `TableContext` |
| table level under the paragraph style, toggles | preprocess | resolver |
| overrideTableStyleFontSizeAndJustification | preprocess (and it rewrites settings) | resolver (reads settings) |
| synthetic style id | preprocess composes the style | resolver names it, resolves it, builds its content |
| conditional trPr/tcPr/tblPr applied | writers | writers (conditions from `TableContext`) |

## 5. Word probes (phase 0)

In `docx4j-layout-fidelity`'s `Corpus.java` after the `styles-*` set. On the share since
2026-10-03, so the README's fields-off `WordGoldenRunner` command cuts these six and nothing
else; Jason runs Word, and the goldens go to `goldens/word/` as before.

Common to all six:
- the cell runs carry no `w:rPr` and the paragraphs no direct `w:spacing`, so a cell's text
  shows what the styles give it and nothing else;
- docDefaults are Liberation Serif 12pt;
- every table has direct single borders and `w:tblLook` 0620 (first row on, the rest off).

Each case below gives the expected Word reading first (a guess until the golden is in), then
docx4j 17.3.1-SNAPSHOT today (PDF, `FLAG_NONE`, read with PDFBox on 2026-10-03).

**T1 `tables-normal-table-text`.**  The default table style, named "Normal Table", is given
Liberation Sans 20pt and `w:spacing w:after 600`. Three tables, two paragraphs per cell:
(a) no `w:tblStyle`; (b) Grid2, based on it; (c) Custom, no `w:basedOn`.
- Expected (by analogy with P5): none applied.
- docx4j today: none applied (12pt Serif, no 30pt gap).
- Settles D4 for the six corpus documents.

**T2 `tables-named-normal-table`.**  The default table style is renamed My Default, and a
non-default style OtherNormal is named "Normal Table"; both are given Sans 20pt. Tables:
(a) OtherNormal; (b) no style; (c) Grid2, based on the default.
- Expected: unknown, which is the question.
- docx4j today: (a) 12pt Serif, ignored by *name*, and gets no cell margin (its chain does not
  reach the default by *id*); (b) and (c) 20pt Sans. D4 in one document.

**T3 `tables-textbox-in-cell`.**  A table style gives the whole table `w:sz 32` (16pt), and
its `firstRow` condition makes text bold. Header row: (a) cell text, (b) a text box anchored in
the cell; body row: (c) cell text, (d) a text box; (e) a text box after the table. Each box is
`mc:AlternateContent`, the wps and VML branches alike.
- Expected: (a) 16pt bold; (c) 16pt; the box text of (b), (d) and (e) all 12pt regular.
- docx4j today: (b)'s box text 16pt bold, (d)'s 16pt, (e)'s 12pt, so D2 is visible. docx4j
  also draws the box text over its anchor paragraph's line, a separate matter.

**T4 `tables-numbered-in-cell`.**  ProbeListNumber numbers through its own `w:numPr`
(`w:num` 95), and level 0 names it back. (a), (b) are in the body; (c) is in the header row and
(d) in a body row of a table whose style makes the first row bold; (e), (f) are in the body;
(g) is in a table naming no style; (h) is in the body.
- Expected: 1 to 8.
- docx4j today: (c) and (d) unnumbered, then 3, 4, 5 and (h) 6. (g) is numbered because a
  table naming no style gets no synthetic style. D1.

**T5 `tables-compat-size-jc` and `tables-compat-size-jc-on`.**  Two documents, because the
setting is a document setting: `overrideTableStyleFontSizeAndJustification` is off in the
first and on in the second. Normal states 12pt and `w:jc left`. Table style ProbeCompat: whole
table 9pt `w:jc right`; `firstRow` 14pt `w:jc center`. Header and body row, three columns:
(a) Normal; (b) ProbeA, based on ProbeB (10pt), based on Normal, stating no size; (c) ProbeC,
stating 16pt.
- Setting on, expected (ECMA-376's own order): 12, 10 and 16pt, all left.
  docx4j today: the same.
- Setting off, expected: unknown, which is the question.
  docx4j today, header: (a) 14pt centred, (b) 10pt centred, (c) 16pt centred.
  docx4j today, body: (a) 9pt right, (b) 10pt right, (c) 16pt right.
  So the size exception reaches (a) only, and the justification exception every column, since
  none of the three styles states `w:jc`.

## 6. Phases

**Phase 0 - probes.**  T1 to T5 in `Corpus.java`, then goldens from Word. No library change.
IN PROGRESS: the six probes of §5 are written (2026-10-03) and on the share; the goldens are
awaited. Then each probe's Word reading goes into §5 and settles the question it asks.

**Phase 1 - the resolver takes the context (additive; no exporter change).**
`TableContext`, `CellContext`, `TableContextTracker`, `cellContextOf`; the three overloads;
the compat rule read from settings; the composition moved from `getCellPStyle`, which loses
D3 on the way.
- Tests: a resolver-level twin of `ParagraphStylesInTableFixConditionalTest` with the same
  expected values; the `PStyle12PtInTable*` expectations at resolver level; T5's outcomes.
- Gate: core-tests and export-fo-tests green.
- Gate: an **equivalence harness** over the three corpora. For every paragraph, run and
  paragraph mark in a table, the in-context answer must equal the answer for the synthetic
  style the preprocess writes. Every difference is recorded here and explained; none is
  expected beyond T1/T2.

**Phase 2 - names, and the defects.**  The preprocess uses `styleIdFor`; the synthetic
style's content comes from the resolver; `Emulator` goes through `sourceStyleOf` (D1); the
text-box reset if T3 says so (D2); T1/T2 applied to the one table-style rule (D4).
- Gate: the corpora scored against the current baseline. Expected: the seven
  numbered-in-cell documents improve (and the ten text-box documents if T3 agrees), none
  regresses, and the probes match T1 to T5.

**Phase 3 - the FO visitor resolves in context.**  The preprocess comes off `FLAG_NONE` FO
output.
- Gate: corpora zero-delta against phase 2; export-fo-tests on both pathways.

**Phase 4 - the writers share `TableContext`.**
- Gate: zero-delta; `TableStyleConditionalWriterTest`, `TableStyleConditionsTest`.

**Phase 5 - consumers, docs and hand-offs.**  Markdown (decision 4); the
`PropertyResolver` class javadoc; `docx4j-export-fo/docs/word-layout-rules.md`'s
table-conditions section; CHANGELOG; the hand-offs of §9.

## 7. Decisions for Jason

1. **Phase 3 at all?**  It changes no output, by its gate. Recommended: yes, so the default
   PDF pathway stops depending on a document rewrite.
2. **D1 in 17.3.1 ahead of the CR?**  The principled fix is phase 2's `sourceStyleOf`, and
   phase 1 is additive and short. Recommended: in phase 2, unless 17.3.1 is to ship first, in
   which case a narrow `Emulator` fix (strip the synthetic suffix back to the source id) goes in
   now and phase 2 replaces it.
3. **`cellContextOf(P)` through parent pointers**, or explicit contexts only?  Recommended:
   include it, documented as null where the chain is broken. It is what docx4j-mcp and user
   code can use.
4. **Markdown**: GFM has no table styling, and a styled header row is already the markdown
   header. Recommended: markdown keeps ignoring table-style formatting. The resolver makes it
   available should that change.
5. **The probe set** T1 to T5 as listed, or trimmed.

## 8. Risks

- **The equivalence harness finds differences** between the preprocess's composition and the
  resolver's, for example in where numbering's indent layer falls. Each one is a finding, to be
  settled by a probe, not by preference.
- **Synthetic ids stay visible on the HTML and FO XSLT pathways.**  Code there that compares
  `w:pStyle` values sees names, as it does today. `sourceStyleOf` is the remedy each time,
  and D1 was the case found.
- **A document style whose id equals a generated name.**  The generated name wins, as
  `activateStyle(Style)` replaces today.
- **The resolver's API grows.**  The ports mirror it after phase 1, and their goldens gain
  the cell context.
- **Cost:** one `TableContext` per table and cached compositions per key, the same order as
  the synthetic styles and their resolution today. The FO visitor saves the preprocess walk.

## 9. Hand-offs

- **docx4j-core-ts** and **docx4j-python**: the overloads, `TableContext` and `CellContext`;
  goldens per table paragraph gain (table style id, condition key) and the in-context
  properties. Unblocks core-ts CR-001's and python CR-002's "table conditional formatting
  stays out".
- **docx4j-mcp**: tools reporting effective formatting use `cellContextOf`.
- **Enterprise CR-001**: none beyond the corpus scores of phase 2.

## 10. Effort (rough)

Phase 0: half a day plus the Word run. Phase 1: a day, the equivalence harness included.
Phase 2: a day with the corpus gate. Phases 3 and 4: half a day each. Phase 5: half a day plus
the hand-offs.
