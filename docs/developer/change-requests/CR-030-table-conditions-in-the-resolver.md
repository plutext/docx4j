# CR-030: table styles behind the resolver - a paragraph's table context handed to `PropertyResolver`, the synthetic styles reduced to names

Status: IN PROGRESS. Phase 2 done 2026-10-03 (§6), so 17.3.1 can ship. D1, D2, D3, D4, D6,
D7, D8 and D9 are fixed; the round trip finds no difference on the corpus; the three D1
documents improve, and nothing regresses. Phases 3 to 5 remain. Phase 1 done the same day.
Along the way, before phase 2:
- 17.3.0's broken XSLT-pathway PDF export was fixed (3a81e4485);
- export-fo-tests went back into the reactor, with its five stale tests updated (83009bfcc).
Phase 0 is done: T1 to T8 were cut and read (§5.1, §5.3), and no decision is outstanding. Proposed
2026-10-03; phase 0 started the same
day (Jason: "start phase 0"). Word's goldens for the six probes were cut and read the same day
(§5.1):
- D1 and D2 are confirmed;
- D4 is settled: Word goes by the style's *name*;
- a sixth defect is found (D6);
- three follow-up probes are proposed (§5.2).
The decisions of §7 are open.
Reviewed 2026-10-03 against the code (Claude Fable 5.1, at Jason's request), and the findings
folded in: D4's unmeasured half and D6's absent case (§2), D7 and D8 (new), §4.2 to §4.6,
two more follow-up probes and their timing (§5.2), phase 1's handling of D6 (§6), decisions 2,
3, 6 and 7 (§7), and §8.
After the review (Jason: "fold them in"): D7's STYLEREF case measured, the D6 counts
reconciled, the style-chain cache added to §4.2, and decision 7's round-trip condition
added, with its phase 2 gate.
Second review 2026-10-03 (the same reviewer) folded in:
- D9 (new, measured: a missing style in a cell aborts the preprocess);
- decision 7's fallback made all or nothing;
- the phase 1 harness's difference classes completed;
- the cache key a structured tuple;
- the chain cache copied before its merge and cleared by `refresh()`;
- D1's count firmed up: 11 paragraphs in 3 documents, not about 498 in 7;
- stale text corrected, and the effort revised.

Decisions (Jason, 2026-10-03: "I will go with the recommendations"):
- decisions 1, 3, 4, 6 and 7 are accepted as recommended;
- decision 2 is reworded after the reviewer's last note and accepted. Its release question
  is settled too: phase 2 lands before 17.3.1 ships (Jason, 2026-10-03), so no stopgap is
  needed.

The follow-up probes T6 to T8 were added the same day: six documents, on the share, Word
goldens awaited (§5.2). docx4j's renderings of T8 found D10, a footnote that takes the
formatting of the paragraph it is referenced from. That is an FO-layer defect outside this
CR's resolver scope, so it is recorded but not planned here. The six goldens were cut and
read the same day (§5.3):
- T6: the exception lives below mode 15 and not in it;
- T7: a renamed default table style applies as written, with nothing beneath it;
- T8: Word keeps every note plain.

Phase 1 started on Jason's word.
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
  paragraphs should be numbered 1 to 4, and Word numbers them so (§5 T4).

  Corpus, firmed up 2026-10-03 after the second review: 11 paragraphs in 3 of 446 documents
  (5, 4 and 2), and each of those lists' later items is miscounted too. The count is docx4j's
  own: `ParagraphStylesInTableFix.process` and `Emulator.numRefFor` were run on each
  document, counting paragraphs numbered before the preprocess and unnumbered by it. The
  first estimate, about 498 in 7, came from a scan by style id, which counted cells of
  tables naming no style under a default named "Normal Table". Those get no synthetic style
  and keep their numbers, as T4's (g) does; one such document has 496 numbered cell
  paragraphs and loses none.
- **D2. A text box anchored in a cell takes the cell's table formatting.**  `TraversalUtil`
  descends into `w:txbxContent`, and the walk's table stack is still pushed there, so the text
  box's paragraph got `Normal-Grid-firstRow-BR` (bold). A text box is a story of its own, and
  Word does not apply the table style to it: in T3 the box text is 12pt regular in a 16pt
  bold header cell (§5.1). Corpus: 10 documents have a text box anchored in a table cell.
- **D3. DEBUG logging aborts the preprocess.**  With DEBUG on for `StyleRenamer`,
  `getCellPStyle` marshals each applicable `CTTblStylePr` for its log line. That type has no
  `@XmlRootElement`, so the marshal throws. The exception escapes the walk, and `process`
  catches and logs it, so the rest of the body and every header and footer are left
  unrenamed. It happens only with that
  logger at DEBUG, which is exactly when someone is investigating table formatting.
- **D4. Two rules for which table style applies** (§1), settled by T1 and T2 (§5.1): Word goes
  by *name*.
  - A style named "Normal Table" is Word's built-in Normal Table, whatever its own
    definition says and whether or not it is the default. It gives cell margins of 108 twips
    left and right, and nothing to text.
  - The `w:default` table style applies to a table naming no style, whatever its name.
  - On re-saving, Word merges a non-default style named "Normal Table" into its built-in, and
    writes the default style's id onto the table that named none.

  So the preprocess's rule (by name) is right for text. `getEffectiveTableStyle`'s rule (by
  the default style's *id*) is wrong where the two differ: T2's table (a) names a non-default
  style called "Normal Table", and docx4j gives it no cell margin where Word gives it 108.
  No corpus document has such a case, so the fix moves nothing on the corpus. The six
  documents whose Normal Table carries run properties (Times New Roman 10pt) are rendered
  right already: Word ignores those properties too (T1).

  The second half, measured by T7 (§5.3): a default style *not* named "Normal Table" applies
  as written, with nothing beneath it. Stating margins of 300, its tables start their text
  300 twips in; stating none, they start at 0. T2's golden could not tell this from "the
  built-in underneath", because its My Default stated 108 itself. `getEffectiveTableStyle`
  gives 108 in both of T7's variants, so it is wrong there as well as for T2's table (a).
- **D5 (a gap, not a defect).**  Footnotes, endnotes and comments are not walked, so a table in
  a note gets no table style. The corpus has none. It follows once the context comes from the
  walk that renders the note, but that walk is also where a new leak can arise: the FO visitor
  renders a note's body at its reference, so a note referenced from a cell is rendered while
  the cell's context is current (§4.6).
- **D6. docx4j applies [MS-DOCX]'s 12pt-and-left exception in compatibility mode 15, where Word
  does not.**  Found by T5 (§5.1).
  - Word: with `overrideTableStyleFontSizeAndJustification` stated 0 in a mode-15 document,
    the output is identical to the same document stating 1. Normal's 12pt and left win over
    the table style in every cell, which is ECMA-376's order, and Word's re-save rewrites the
    setting to 1.
  - docx4j: it applies the exception. Normal's cells come out 14pt centred in the header and
    9pt right-aligned in the body, and the two styles that state no `w:jc` are centred and
    right-aligned too.
  - docx4j also takes an *absent* setting as 0 (the preprocess's `defaultSetting`), so a
    mode-15 document with no setting, as a producer other than Word may write it, gets the
    exception too. Measured by T6 (§5.3): Word does not apply it there, as with the stated 0.
  - No corpus document is affected: all 171 mode-15 documents, and 125 others, state 1.
    (296 counts all 454 files. §3's 288 counts the 446 whose main part is at
    `word/document.xml`; the other 8 all state 1, so both counts are right. Reconciled
    2026-10-03.)
  - The five corpus documents where the exception can fire are Word 2007 documents (no
    `compatibilityMode`) with the setting absent. T6 (§5.3) shows Word 365 applies the
    exception to them, and to a mode-14 document, exactly as docx4j does: its output
    matches docx4j's to the point. So D6 is the mode-15 case only, stated 0 or absent, and
    the rule is: the exception applies below mode 15 where the setting is not on, and never
    in mode 15.
  - The `PStyle12PtInTable*OverrideFalse` tests pin the exception as Word 2010 measured it,
    but since 2026-09-19 `createPackage()` gives them mode-15 documents. They now assert the
    exception in exactly the case where Word 365 does not apply it. Pinned to mode 14, they
    are right again (T6).
- **D7. The synthetic id reaches other code that reads a paragraph's style id.**  Found in
  review from the code. The STYLEREF case was then measured (2026-10-03, scratch probe,
  `FLAG_NONE` and `FLAG_EXPORT_PREFER_XSL` alike):
  - The document has two Heading 1 paragraphs, the second in a TableGrid cell, and a header
    holding `STYLEREF "Heading 1"`.
  - Only the first heading gets a marker; the heading in the cell gets none.
  - So on a page whose latest heading is in a cell, the header shows an earlier heading.

  The contextual-spacing and HTML cases are read from the code. `Emulator` (D1) is one
  consumer of several:
  - `XsltFOFunctions.createBlock` writes `pStyleVal`, the synthetic id, into the
    `HINT_PSTYLE` attribute of the block (line 1189);
  - `StyleRefMarkers` matches blocks to a STYLEREF's style on that attribute (line 184), so a
    STYLEREF cannot find a heading that sits in a table cell (measured, above);
  - `WordLayoutFixups`' `w:contextualSpacing` pairing compares the same hint, so two
    paragraphs of one style in a cell whose paragraph-level `w:cnfStyle` differ are not "of
    the same style";
  - the HTML visitor passes the id on as `pStyleVal` (`HTMLExporterVisitorGenerator`, line
    330).

  (`ListsToContentControls`' heading test by style name is safe: `PP_HTML_COLLECT_LISTS` runs
  before the rename.)
- **D8. Synthetic ids are ambiguous.**  Found in review, read from the code. The id is
  `styleVal-tableStyle[-key]-BR`, joined with hyphens, and style ids may contain hyphens: a
  paragraph style `A-B` in a table styled `C` and a paragraph style `A` in a table styled
  `B-C` both give `A-B-C-BR`. `cellPStyles` then hands the second paragraph the first one's
  style. For the same reason the source style cannot be recovered by parsing the id. Corpus
  (scanned after the second review): no collision. There are 44 (paragraph style, table
  style) pairs with a hyphen in either id, none of which collides with another pair or with
  a style of its document.
- **D9. A cell paragraph naming a missing style aborts the preprocess.**  Found in the second
  review and measured (2026-10-03, scratch probe, reproduced here). A TableGrid table's
  second cell paragraph names `w:pStyle "Gone"`.
  - `getCellPStyle` walks the style's `w:basedOn` chain without checking for a missing style,
    and throws a NullPointerException at line 659 (`thisStyle.getBasedOn()`).
  - `process` catches and logs it, and the walk stops. Paragraph 1 is renamed
    `Normal-TableGrid-BR` and paragraph 2 keeps `Gone`. Paragraph 3 and every table after it,
    headers and footers included, keep their own styles and lose the table style.
  - A chain whose `w:basedOn` names a missing style fails the same way.
  - Same effect as D3, without needing DEBUG. The resolver's own comment calls a deleted
    style named by a paragraph "common in real documents".
  - Corpus (scanned, chains included): no document has such a paragraph in a table that gets
    a synthetic style.
  - The resolver resolves a missing style as the default (CR-015), so the in-context answer
    is right from phase 1. The preprocess stops composing in phase 2, and D3 and D9 go with
    `getCellPStyle`.

- **D10. A footnote takes the bold and italic of the paragraph it is referenced from (FO
  layer; outside this CR).**  Found 2026-10-03 when docx4j rendered T8 (§5.2), on the
  visitor pathway:
  - a note referenced from T8's bold italic header cell comes out bold italic, one from an
    italic body cell italic, and one from a plain body paragraph plain;
  - the same with no table at all: a note referenced from a paragraph whose style is bold
    and italic comes out bold italic.

  The visitor puts the note's body inside the referencing paragraph's `fo:block`, and
  XSL-FO's property inheritance carries every inheritable property the note does not state
  itself into it. Size and font do not leak, because the probe's note states both. It is
  not the resolver's, so this CR's phase 3 story-boundary reset does not touch it. It needs
  its own fix in the FO writer: the footnote body resets the inheritable properties, or the
  note's blocks state them. Word's T8 golden confirms it: all three notes upright and
  regular (§5.3). No corpus count has
  been taken yet.

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
| style-numbered paragraphs in cells of a styled table, level linked to the style | 3 | D1: 11 paragraphs (§2; the first scan's 7 included tables that get no synthetic style) |

## 4. Design

### 4.1 The rule

Restated from CR-015's Layering: what properties apply to a paragraph or run, *given where it
sits*, is `PropertyResolver`'s. Where the resolver lacks the context, the caller hands it the
context. The document is not rewritten to suit the resolver. The table style is that context's
first and largest case.

### 4.2 A table context and a cell context (`org.docx4j.model.table`)

- **`TableContext`**, built by the resolver from a `w:tbl` (`resolver.tableContext(tbl)`), holds:
  - the effective table style (`getEffectiveTableStyle`, the one rule for which style
    applies). As T1 and T2 measured it: the table's `w:tblStyle` if it names a style, else the
    `w:default` table style, whatever its name. Walking the chain, a style *named* "Normal
    Table" contributes Word's built-in (108 twips left and right, nothing to text) in place of
    its own definition, and the walk ends there. Every other style applies as written,
    the default style included, with nothing beneath it: measured for text by T2 and for
    cell margins by T7 (§5.3);
  - the look (the table's `w:tblLook`, else the style's, else Word's default 04A0);
  - the band sizes;
  - each row's index and each cell's first grid column and span. This is one implementation
    of the grid arithmetic, replacing the preprocess's and the writers' copies:
    `w:gridBefore`, then the spans, nested tables excluded.
- **`CellContext`**, an immutable value: `tableContext.forParagraph(tr, tc, pPr)` (or
  `forCell(tr, tc)` for the writers). It holds the table style id, the conditions
  (`TableStyleConditions.resolve` with the row, cell and paragraph `w:cnfStyle` caches), the
  applicable `w:tblStylePr` entries in precedence order, and the cache key of §4.2.
  `TableStyleConditions.key`'s string stays for naming synthetic styles only.
- **The resolver keeps no `TableContext`.**  `tableContext(tbl)` builds a new one on each
  call, and the caller (a walker, a writer) holds it for as long as it is in the table. A
  `TableContext` is a reading of the *content* (rows, spans, look), and the resolver's
  contract (CR-015) covers the styles part only: a context cached by `Tbl` would go stale
  when a row is added, with nothing to refresh it. And since the resolver lives as long as
  the package, a cache keyed by `Tbl` would keep every table's content reachable for that
  long. What the resolver caches depends on styles alone:
  - the composition, keyed by a structured tuple: (paragraph style id, effective table style
    id, condition set), a value object with `equals` and `hashCode`. It is never a
    concatenated string, which would repeat D8 inside the resolver;
  - each table style's merged `w:basedOn` chain, per style id. That is the half of
    `getEffectiveTableStyle` which today is recomputed, a new `Style` each call. The table's
    own `w:tblPr` goes over it in the `TableContext`. `getEffectiveTableStyle` keeps its
    contract of a new `Style` each call: since its merge of the table's `w:tblPr` works in
    place, it copies the cached chain first. `refresh()` clears this cache with the others.

  So a `TableContext` costs a walk of the table's rows and nothing for its style, which is
  what makes `cellContextOf` affordable for one paragraph (§4.4). (Added 2026-10-03, after
  the review.)

### 4.3 The resolver composes it

New overloads; a null context means outside a table, which is exactly today's behaviour:

    getEffectivePPr(PPr, CellContext)
    getEffectiveRPr(RPr, PPr, CellContext)
    getEffectiveParagraphMarkRPr(PPr, CellContext)

    effectivePPr = docDefaults.pPr + table(ctx).pPr + chainPPr(styleOf) [+ compat rule] + direct
    effectiveRPr = docDefaults.rPr + level(table(ctx).rPr, chainRPr(styleOf)) [+ compat rule]
                   + level(character style) + direct
    table(ctx)   = the table style's own pPr/rPr, then ctx's applicable conditions in order

The composition is the preprocess's, moved, not reinvented. The compat rule applies only below
compatibility mode 15, and there only where the setting is off or absent. In mode 15 Word
ignores a stated 0 and re-saves it as 1 (T5, D6). `CompatibilityOptions` already resolves a
document's mode, and the preprocess stops writing `1` into the exporter's settings. Below
mode 15 Word 365 does apply the exception (T6, §5.3), including in a document with no
`compatibilityMode` at all, which is mode 12. Caches are
keyed by §4.2's tuple, which is the same cardinality as the synthetic styles today:
`ConcurrentHashMap`s holding immutable values, under CR-015's live-object contract.

### 4.4 How a caller gets the context

1. **A walker** extends or composes a **`TableContextTracker`**, a `TraversalUtil` callback
   that keeps the table stack: the preprocess's walk, made reusable. It starts an empty stack
   inside `w:txbxContent`: T3 shows Word gives a text box anchored in a cell none of the
   table's formatting (D2).
2. **One paragraph in a loaded tree**: `resolver.cellContextOf(P)` follows parent pointers
   (`P > Tc > Tr > Tbl`). Measured 2026-10-03: the pointers are there after unmarshal, after
   `WordprocessingMLPackage.clone()` and after `XmlUtils.deepCopy`. They are absent for
   content built with `ObjectFactory` and added through `getContent().add()`, and in the
   XSLT pathways' fragments. The method returns null where the chain breaks.
   - The climb is to the *nearest* `Tc`, then the nearest `Tr`, then the nearest `Tbl`,
     through whatever lies between: `w:sdt` around paragraphs, cells or rows (8 corpus
     documents), `w:customXml`, `w:smartTag`, `mc:AlternateContent`. The bare chain
     `P > Tc > Tr > Tbl` is the simple case only.
   - It stops, returning null, at a story root reached before a `Tc`: `CTTxbxContent`
     (which suits T3), a footnote, endnote or comment, a header or footer, the body.
   - It costs one `TableContext` per call, which is a walk of the table (§4.2: the resolver
     keeps none). That is right for "what formatting does this paragraph have"; code
     resolving every paragraph of a document uses the tracker, and the javadoc says so.
     Calling it per paragraph over a large table is quadratic.
3. **The XSLT pathways** have neither, and use names (4.5).

### 4.5 Synthetic styles become names the resolver hands out

The preprocess stays for the HTML pathways and for the FO XSLT pathway
(`FLAG_EXPORT_PREFER_XSL`). For HTML that is representation: a style id is a CSS class
(Layering rule 3).

- **Naming:** the preprocess asks `resolver.styleIdFor(styleId, ctx)` for the id, named as
  today. The resolver records id -> (source style, context), and where a name is already
  taken by a different pair, or by a style of the document, it hands out a distinct one (D8).
  The record survives `refresh()`: the ids are already written into the document.
- **Resolution:** a `w:pStyle` naming such an id resolves as (source style, context). The XSLT
  pathways therefore resolve correctly with no context of their own.
- **Content:** the `w:style` written into the styles part (for `HtmlCssHelper`/`StyleTree`'s
  CSS) is built from the resolver's answer, so nothing composes table properties a second way.
- **Which of the two is the authority** is decision 7. Resolution through the record and a
  real style of the same content are two answers to one question, and they can drift. The
  alternative is to keep the style real, resolved the plain way as today (docDefaults plus
  its own flattened content), with the record serving `sourceStyleOf` and uniqueness only.
- **The document's own style:** code that needs it calls `resolver.sourceStyleOf(id)`.
  `Emulator` does, which fixes D1 on every pathway, and so do D7's consumers: the
  `HINT_PSTYLE` attribute carries the source id, so STYLEREF and the contextual-spacing
  pairing see the document's style.

### 4.6 The FO visitor needs no synthetic styles

The visitor walks the `w:tbl`/`w:tr`/`w:tc` objects themselves, so it keeps a
`TableContextTracker` and resolves in context. It sets the current `CellContext` on the
conversion context on entering a cell and clears it on leaving. The shared FO code
(`XsltFOFunctions.createBlockForPPr`, `handleRPr`, `RunFontSelector`'s line-height pPr) reads
it from there. It is null on the XSLT pathway, where the name carries it.
`PP_COMMON_TABLE_PARAGRAPH_STYLE_FIX` then leaves the FO visitor pathway's feature set: one
whole-document walk and rename fewer.

- **The call sites are more than those three.**  Counted in review, the resolver is asked for
  effective properties at about a dozen places in `XsltFOFunctions` (lines 231, 887-923,
  1703, 1761, 3405, 3647, 4038, 4046), and in `FOExporterVisitorGenerator` (458, 805),
  `XsltCommonFunctions` (437, the paragraph mark), `HiddenText` (72) and `RunFontSelector`
  (1039, 1887). Phase 3 starts by listing every one and deciding, for each, whether it takes
  the context. The zero-delta gate only catches a missed site that the corpus exercises (a
  table style that hides text, for one, may not be there).
- **The context is cleared at every story boundary, not only `w:txbxContent`.**  The visitor
  renders a footnote's body at its reference (`handleFootnoteReference`), so a note
  referenced from a cell is walked while that cell's context is current. Footnote, endnote
  and comment bodies and text boxes each start with no context, and the cell's is restored
  on the way out. T8 (§5.2) pins it.
- With no synthetic id on this pathway, `pStyleVal` and `HINT_PSTYLE` are the document's own
  style id again, which closes D7 here without `sourceStyleOf`.
- Once the preprocess no longer rewrites the settings, the stack-trace test at the top of
  `ParagraphStylesInTableFix.process` (it returns when called under `FOPAreaTreeHelper`,
  "especially changing overrideTableStyleFontSizeAndJustification") has lost that reason.
  Whether it can go is checked in phase 2.

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
| synthetic style id | preprocess composes the style | resolver names it and builds its content; which object answers for it is decision 7 |
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

### 5.1 Word's readings (goldens cut 2026-10-03, Word 365, fields not updated)

Read with PDFBox from the goldens, and from Word's re-saves where the question was what Word
*computed*. Method note: PDFBox's `getFontSizeInPt` reads Word's PDFs low at some sizes (10pt
as 9.0, 16pt as 15.0) though not at others (12, 20). So sizes here come from glyph widths,
taken relative to a 12pt line in the same PDF.

| probe | Word | docx4j today | so |
|---|---|---|---|
| T1 `tables-normal-table-text` | (a), (b), (c) all 12pt Serif with no 30pt gap: the document's Normal Table text properties apply nowhere. Margins 108 for (a) and (b), 0 for (c), as in P5 | the same | docx4j is right; the six corpus documents are fine |
| T2 `tables-named-normal-table` | (a), the non-default style named "Normal Table": 12pt Serif *and margins of 108*. (b), no style, default "My Default": 20pt Sans. (c), based on the default: 20pt Sans. The re-save drops (a)'s `w:tblStyle`, merges the style into the built-in TableNormal, and writes `w:tblStyle MyDefault` on (b) | text the same; (a)'s margin 0 | the name rule (D4); the resolver's margins are wrong for (a) |
| T3 `tables-textbox-in-cell` | cell text (a) 16pt bold, (c) 16pt; the text in all three boxes 12pt regular | (b)'s box 16pt bold, (d)'s 16pt | D2 confirmed: the table style does not reach a text box |
| T4 `tables-numbered-in-cell` | 1 to 8, (c) bold | (c), (d) unnumbered, (h) 6 | D1 confirmed |
| T5 `tables-compat-size-jc` (setting 0, mode 15) | (a) 12pt, (b) 10pt, (c) 16pt in both rows, all left. The re-save writes the setting as 1 | (a) 14pt centred / 9pt right; (b), (c) centred / right | D6: Word ignores the setting in mode 15 |
| T5 `tables-compat-size-jc-on` (setting 1, mode 15) | identical to the setting-0 document | as Word | ECMA-376's order |

### 5.2 Follow-up probes (added 2026-10-03; goldens cut and read the same day, §5.3)

Three probes in six documents, for one Word run, and **before phase 1**. T6 decides whether
the compat rule is moved into the resolver at all; T7 what `getEffectiveTableStyle`'s rule
is; T8 the story boundary. They are in `Corpus.java` after T5.

The T6 documents state exactly one `compatibilityMode`, or none. That needed a workaround:
`Doc.create(mode)` now leaves a document stating 15 and then its own mode, because
`createPackage()` has written mode 15 since 9de10aac9 (2026-09-19). The probes already on
the share predate that change and state a single mode. The harness itself is not fixed
here.

**T6 `tables-compat-size-jc-mode12` and `-mode14`**: T5's document in compatibility mode 12
(no `compatibilityMode`, as Word 2007 writes it) and in mode 14, with the setting absent. It
answers whether Word 365 applies the 12pt-and-left exception below mode 15 at all:
- if it does, the rule is "below mode 15 and the setting not on", and the
  `PStyle12PtInTable*OverrideFalse` tests pin mode 14;
- if not, the exception goes, and with it those tests' expectations and the five Word 2007
  corpus documents' current rendering.

A third document, T5's in mode 15 with the setting *absent*, measures D6's absent case
directly instead of inferring it from the stated 0.

- Ids: `tables-compat-size-jc-mode12` (no `w:compatSetting` at all),
  `tables-compat-size-jc-mode14` and `tables-compat-size-jc-absent`.
- docx4j today, all three: the exception applied, (a) 14pt centred in the header and 9pt
  right in the body, and (b) and (c) centred and right, as in T5's stated 0.

**T7 `tables-renamed-default-margins`**: T2's document with My Default stating cell margins
of 300 left and right (as P5's Table Normal did), and a second default-style variant stating
no `w:tblCellMar` at all. Tables: no `w:tblStyle`; a style based on My Default; a style with
no `w:basedOn`. Read the first cell's text offset, as in P5.
- 300 and 0 for the two variants: the default applies as written with nothing beneath, which
  is §4.2's rule as drafted;
- 108 in either: the built-in still underlies the default style, and §4.2's rule changes.
- Ids: `tables-renamed-default-margins` and `tables-renamed-default-nomargins`.
- docx4j today, both variants: (a) and (b) at 108, (c) at 0. The default style's own layer
  is skipped for the built-in, so the stated 300 is not used. If Word starts (a) and (b) at
  300, docx4j is wrong there.

**T8 `tables-footnote-in-cell`**: a table style giving the whole table italic and 16pt and
its `firstRow` bold, with a footnote referenced from a header-row cell, one from a body-row
cell, and a third from the body. Each note's run states Liberation Serif 10pt, so leakage
shows as italic or bold rather than as size.
- Expected: all three notes alike, upright and regular. Pins §4.6's story boundary.
- docx4j today: notes (a) bold italic, (b) italic, (c) regular. The review expected no
  current pathway to get this wrong; this leak is D10, from FO inheritance rather than from
  the resolver.

### 5.3 Word's readings of the follow-up probes (goldens cut 2026-10-03)

Sizes are from glyph widths, as in §5.1; positions are each cell's first-line x, with the
table's left edge at 72pt.

| probe | Word | docx4j today | so |
|---|---|---|---|
| T6 `tables-compat-size-jc-mode12` (no `w:compatSetting`) | the exception applied: (a) 14pt centred / 9pt right; (b) 10pt centred / right; (c) 16pt centred / right. Each cell's x is within 0.3pt of docx4j's | the same | the exception lives in Word 2007 documents, and docx4j renders them right |
| T6 `tables-compat-size-jc-mode14` (setting absent) | the same as mode 12 | the same | and in mode 14 |
| T6 `tables-compat-size-jc-absent` (mode 15) | ECMA-376's order: (a) 12pt, (b) 10pt, (c) 16pt, all left | the exception applied | D6's absent case: not in mode 15, absent or stated 0 |
| T7 `tables-renamed-default-margins` (My Default states 300) | (a) and (b) 15pt in (x 87.3), (c) 0 | (a), (b) 108 (x 77.7) | the default applies as written (D4) |
| T7 `tables-renamed-default-nomargins` (My Default states none) | (a), (b), (c) all 0 (x 72.5) | (a), (b) 108 | nothing beneath a renamed default (D4) |
| T8 `tables-footnote-in-cell` | the cells' text 16pt, (a) bold italic and (b) italic, and the reference marks with them; all three notes upright and regular, 10pt | notes (a) bold italic, (b) italic | D10 confirmed; the table style stops at the note, as §4.6's story boundary has it |

So the compat rule as phase 1 writes it is: the exception applies below compatibility mode 15
(a document with no `compatibilityMode` is mode 12) where the setting is not on, and never in
mode 15. And `getEffectiveTableStyle`'s rule is §4.2's, now fully measured.

## 6. Phases

**Phase 0 - probes.**  T1 to T5 in `Corpus.java`, then goldens from Word. No library change.
Goldens cut and read 2026-10-03 (§5.1), and committed with their manifest lines. The
follow-up probes T6 to T8 (§5.2) were cut and read the same day (§5.3). DONE 2026-10-03.

**Phase 1 - the resolver takes the context (additive; no exporter change).**
`TableContext`, `CellContext`, `TableContextTracker`, `cellContextOf`; the three overloads;
the compat rule read from settings; the composition moved from `getCellPStyle`. That method
goes in phase 2, taking D3 and D9 with it.
- The compat rule goes in in its final form, as T5 and T6 measure it: applied below mode 15
  where the setting is not on, never in mode 15 (§5.3). The
  preprocess is untouched in this phase and keeps its ungated rule until phase 2. So the
  resolver and the preprocess are *meant* to differ for a mode-15 document with the setting
  off or absent; no corpus document is one.
- Tests: a resolver-level twin of `ParagraphStylesInTableFixConditionalTest` with the same
  expected values; T5's outcomes in mode 15; the `PStyle12PtInTable*` expectations at
  resolver level in mode 14, where T6 finds the exception (not on `createPackage()`'s
  mode-15 documents, where they would contradict T5).
- Gate: core-tests and export-fo-tests green.
- Gate: an **equivalence harness** over the three corpora. For every paragraph, run and
  paragraph mark in a table, the in-context answer must equal the answer for the synthetic
  style the preprocess writes. Every difference is recorded here and explained. Expected
  classes, each to be confirmed as that and nothing else:
  - T1/T2 (which table style applies);
  - the compat rule's gate, above (none in the corpus);
  - numbering indents: the preprocess merges the paragraph style's chain with
    `StyleUtil.apply(Style, Style)`, without the numbering part, where the resolver's
    `applyPPr` folds the level's indent in per layer;
  - heading outline levels: the preprocess does not go through `headingLayer`;
  - text boxes anchored in cells (D2): the tracker starts an empty stack inside
    `w:txbxContent` and the preprocess does not. The 10 documents with one differ by design;
  - a cell paragraph naming a missing style, or whose chain breaks (D9): the preprocess
    aborts there and leaves the rest unrenamed, and the resolver answers with the default
    style. No corpus document has one;
  - colliding synthetic ids (D8): none in the corpus, by the scan of §2.

DONE 2026-10-03.
- Built, in `org.docx4j.model.table`: `TableContext`; `CellContext`, with a structured `Key`;
  and `TableContextTracker`.
- Built, on `PropertyResolver`:
  - `getEffectivePPr(PPr, CellContext)`, `getEffectiveRPr(RPr, PPr, CellContext)` and
    `getEffectiveParagraphMarkRPr(PPr, CellContext)`;
  - `tableContext(Tbl)` and `cellContextOf(P)`;
  - `getTableStyleIdOf(TblPr)`, and `getTableStyleChain(String)`: the name rule, cached per
    id, cleared by `refresh()`;
  - `appliesTableStyleSizeJcException()`: below mode 15, with the setting not on.

  The composition is cached per (paragraph style id, `CellContext.Key`). The preprocess and
  the exporters are untouched.
- Departures from §4, both without effect on the corpus (the harness below):
  - The tracker's story boundary is the DrawingML anchor or inline and the VML text box,
    the objects through which `TraversalUtil` reaches a text box's content, together with a
    check of the paragraph's parent, rather than `w:txbxContent` itself.
  - `TableContext` counts rows and cells over the branch `TraversalUtil` reads by default
    from `mc:AlternateContent`, where the preprocess walks every branch.
- Tests: `TableContextResolutionTest`, 13 tests.
  - A twin of `ParagraphStylesInTableFixConditionalTest`, with its expected values.
  - A paragraph-by-paragraph comparison with the preprocess's synthetic styles on that
    fixture: all equal.
  - T5 and T6 in modes 15, 14, 12 and none; the name rule of T1, T2 and T7.
  - The tracker and `cellContextOf` through `w:sdt`, through text boxes, and without parent
    pointers.
  - D8's collision, D9's missing style, and `refresh()`.
- Gate, core-tests: 1,372 run, 0 failures, 0 errors (13 skipped).
- Gate, export-fo-tests: **not green, and not because of this phase.**
  - 280 of 662 fail with HEAD's docx4j-core and the same 280 with phase 1's (one
    export-fo-tests build, run both ways; the failure lists are identical). Surefire's own run
    counts 279 of 669.
  - 275 are the XSLT pathway. 230 produce FO that does not parse, because
    `fox:gsub-features` is written without its namespace declaration; 45 fail to export a PDF,
    very likely the same cause. docx4j depends on the released 2.11-docx4j.2, which has that
    capability, so `FLAG_EXPORT_PREFER_XSL` PDF output may be broken in 17.3.0 too.
  - The other four are the no-ligature font twins (two), glyph widths, and an off-page shape.
  - Outside this CR, and reported to Jason.
- Gate, the **equivalence harness**:
  - Coverage: 452 documents of the three corpora (one more is refused by docx4j's zip-bomb
    limit), with 254,652 paragraphs, 160,768 of them in tables.
  - Every paragraph's `w:pPr`, mark and runs, in context, equal what the synthetic style gives,
    except 21 paragraphs in 2 documents. All 21 are D2: text boxes anchored in cells.
  - Counted as equal: `<w:b w:val="true"/>` against `<w:b/>` (likewise `w:i` and `w:bCs`),
    the same value written two ways. Before that normalisation, 135 more paragraphs in 8
    documents differed on it alone.
  - None of the other expected classes occurs in the corpus: numbering indents, outline
    levels, T1/T2, the compat gate, D8, D9.
  - The harness is a scratch program, not committed; it is re-run for phase 2's round trip.

**Phase 2 - names, and the defects.**  The preprocess uses `styleIdFor`, which hands out
unique ids (D8); the synthetic style's content comes from the resolver; `Emulator` goes
through `sourceStyleOf` (D1); a sweep of every reader of a paragraph's style id on the HTML
and FO XSLT pathways, starting from D7's list, each moved to `sourceStyleOf` or shown not to
need it, with a STYLEREF-to-a-heading-in-a-cell test; the text-box reset (D2); the rule in
`getEffectiveTableStyle` as T2 and T7 measure it (D4); the compat rule gated in the
preprocess as it already is in the resolver (D6), with the `PStyle12PtInTable*OverrideFalse`
tests pinned to mode 14, where T6 finds the exception; the
`FOPAreaTreeHelper` stack-trace test in `process` (§4.6).
- Gate: the corpora scored against the current baseline. Expected: the three
  numbered-in-cell documents (11 paragraphs, and their lists' later items) and the ten
  text-box documents improve, none regresses (D4, D6, D8 and D9 touch no corpus document),
  and the probes match their goldens.
- Gate, if decision 7 goes to the real style: the **round trip** over the three corpora.
  Plain resolution of each synthetic style must equal the in-context answer for every
  paragraph, run and paragraph mark that uses it. Each difference is fixed in the flattening
  (carrying `w:outlineLvl`, for one) until the round trip passes. If one cannot be fixed,
  decision 7 falls to the record, everywhere.

DONE 2026-10-03.
- Built, on `PropertyResolver`:
  - `styleIdFor`: named as since 17.2.0, unique (a numeric suffix where the name is taken),
    the record kept across `refresh()`;
  - `syntheticStyle`: the style built from the in-context composition, so the flattening is
    the resolver's;
  - `sourceStyleOf`;
  - `getEffectiveTableStyle` and `reachesDefaultTableStyle` by name, over the cached chain,
    copied (D4).
- `ParagraphStylesInTableFix` is a `TableContextTracker` which names and composes nothing.
  - It renames exactly the paragraphs 17.2.0 renamed: in a table which names a style, or
    which takes a default not named "Normal Table".
  - It no longer rewrites the settings part.
  - The `FOPAreaTreeHelper` guard is kept: a second run is harmless now (the same ids come
    back), and the guard saves the walk.
- `Emulator.resolve` goes through `sourceStyleOf` (D1), and so does the FO block's
  `HINT_PSTYLE` (D7, for STYLEREF and contextual spacing).
- The sweep found no other reader needing `sourceStyleOf`:
  - numbering, HTML's included, goes through `Emulator`;
  - HTML's class name is representation (the synthetic id is the CSS class);
  - `ListsToContentControls` and `Containerization` run before the rename.
- `PStyle12PtInTable*` are pinned to mode 14 in `setSetting`.
- Tests:
  - `SyntheticTableStylesTest` (7): D1, D2, D9, D8; a second run and the settings left
    alone; D6 in modes 15 and 14; a table taking Normal Table not renamed.
  - `PropertyResolverTableStyleTest`, two more: T2 (a), and T7 in both variants.
  - `StyleRefHeaderTest`, two more: a heading in a table cell, on both pathways.
- Gate, tests (reactor): core-tests 1,381/0, export-fo 240/0, export-fo-tests 671/0.
- Gate, the **round trip** (decision 7):
  - 452 documents, 254,652 paragraphs: plain resolution of every synthetic style equals the
    in-context answer. **No difference.**
  - D2's 21 paragraphs are gone, the preprocess now leaving text boxes alone.
  - So the real style stays the authority; no part of the flattening needed fixing.
- Gate, scored against 83009bfcc, the released fork 2.11-docx4j.2 on both sides:
  - real and real3: identical.
  - real2: the three D1 documents and no other, 9 lines in all.
    `12_ru-RU_sdt_num_tbl_2422` 0.9502 -> 0.9564, `14_en-US_fields4_num_tbl_9623`
    0.9924 -> 0.9956, `12_fr-FR_fields54_num_tbl_8695` 0.7700 -> 0.7706.
  - No regression anywhere.
  - The ten text-box documents do not move: their boxes' lines matched before, and D2 changes
    only their formatting.
  - Probes: `tables-numbered-in-cell` 0.25 -> 1.0, `tables-compat-size-jc` 0.875 -> 1.0,
    `tables-compat-size-jc-absent` 0.889 -> 1.0. `tables-textbox-in-cell` has one candidate
    line fewer (the box text now at 12pt), with parity unchanged. The rest are identical.

**Phase 3 - the FO visitor resolves in context.**  The preprocess comes off `FLAG_NONE` FO
output. First the list of resolver call sites (§4.6), each marked as taking the context or
not; the story-boundary reset, with T8.
- Gate: corpora zero-delta against phase 2; export-fo-tests on both pathways; T8 matches its
  golden.

**Phase 4 - the writers share `TableContext`.**
- Gate: zero-delta; `TableStyleConditionalWriterTest`, `TableStyleConditionsTest`.

**Phase 5 - consumers, docs and hand-offs.**  Markdown (decision 4); the
`PropertyResolver` class javadoc; `docx4j-export-fo/docs/word-layout-rules.md`'s
table-conditions section; CHANGELOG; the hand-offs of §9.

## 7. Decisions for Jason

1. **Phase 3 at all?**  It changes no output, by its gate. Recommended: yes, so the default
   PDF pathway stops depending on a document rewrite. The review adds a reason: with no
   synthetic ids on that pathway, D7's whole class (STYLEREF, contextual spacing, whatever
   else reads the id) cannot occur there. Accepted (Jason, 2026-10-03).
2. **D1, and what 17.3.1 needs** (reworded after the reviewer's last note, 2026-10-03).
   D1 is fixed in phase 2, by `sourceStyleOf`. At 11 paragraphs in 3 corpus documents, not
   the 498 in 7 first estimated, it no longer makes a case for a fix ahead of the CR. (Were
   one wanted, it would not strip the synthetic suffix, since the ids are ambiguous (D8); it
   would be `sourceStyleOf` brought forward.) The 17.3.1 question is about the preprocess's
   defects as a set:
   - **If 17.3.1 ships before phase 2:** D3 and D9 each get a small local guard in
     `getCellPStyle`. For D3, the log line does not marshal the unrooted `CTTblStylePr`; for
     D9, a missing style is treated as the default. Both defects silently drop table styling
     for the rest of the document, so they are worth more than D1 as a stopgap.
   - **If phase 2 lands first:** none of the three needs a stopgap.

   Accepted (Jason, 2026-10-03). Settled the same day: phase 2 lands before 17.3.1 ships, so
   no stopgap.
3. **`cellContextOf(P)` through parent pointers**, or explicit contexts only?  Recommended:
   include it, documented as null where the chain is broken, and as costing a walk of the
   table per call (§4.4). It is what docx4j-mcp and user code can use. Accepted (Jason,
   2026-10-03).
4. **Markdown**: GFM has no table styling, and a styled header row is already the markdown
   header. Recommended: markdown keeps ignoring table-style formatting. The resolver makes it
   available should that change. Accepted (Jason, 2026-10-03).
5. **The probe set** T1 to T5 as listed, or trimmed. Done: all six cut, read in §5.1.
6. **The follow-up probes T6, T7 and T8 (§5.2).**  Recommended: yes, all in one Word run, and
   before phase 1. T6 decides whether the exception code stays at all; T7 decides the rule
   phase 2 writes into `getEffectiveTableStyle`, half of which is so far inferred; T8 pins
   the story boundary before phase 3 can get it wrong. Accepted (Jason, 2026-10-03): the
   probes were added, cut and read the same day (§5.3).
7. **Which is the authority for a synthetic name (§4.5)**: the resolver's record, resolving
   the id as (source style, context), or the real style in the styles part, resolved the
   plain way?  Recommended: the real style, with the record kept for `sourceStyleOf` and
   unique naming only. It is today's mechanism, the XSLT pathways and the CSS read the same
   object, and there is one answer to drift from. The cost is that a synthetic style stays a
   flattened copy, stale if a style is modified after the preprocess, as it is today.

   The condition (added 2026-10-03, after the review): a flattened style loses the level
   structure that some of the resolution depends on, so phase 2 gates on a **round trip**.
   Resolving each synthetic style the plain way must give, for the paragraphs, runs and
   paragraph marks that use it, the same answer as resolving them in context. The points
   where flattening can lose something:
   - the toggles (§17.7.3): a character style's level over the paragraph and table levels,
     where the flattened style has merged the two;
   - the numbering layer: a level's indent folded in once over the flattened `w:pPr`,
     rather than at the paragraph style's own layer;
   - a heading's outline level, which `headingLevelByName` finds by the style's *name*, and
     a synthetic style is named by its id, so the flattened `w:pPr` has to carry
     `w:outlineLvl` itself.

   Any difference the round trip finds is where the flattening is lossy, and the flattening
   is fixed until the round trip passes; carrying `w:outlineLvl` is the obvious case. If a
   difference cannot be fixed, the record becomes the authority *everywhere*, not for that
   case alone. Answering by the record only where the style is lossy would split the
   authority per style, which is the drift this decision exists to avoid. (All or nothing:
   the second review, 2026-10-03.) Accepted (Jason, 2026-10-03).

## 8. Risks

- **The equivalence harness finds differences** between the preprocess's composition and the
  resolver's, for example in where numbering's indent layer falls. Each one is a finding, to be
  settled by a probe, not by preference.
- **Synthetic ids stay visible on the HTML and FO XSLT pathways.**  Code there that compares
  `w:pStyle` values sees names, as it does today. `sourceStyleOf` is the remedy each time.
  D1 (numbering) and D7 (STYLEREF, contextual spacing) are the cases found so far.
- **A document style whose id equals a generated name.**  Today the generated name wins, as
  `activateStyle(Style)` replaces; so does the first of two (paragraph style, table style)
  pairs that generate the same name (D8). `styleIdFor` hands out a distinct id in both cases,
  which changes an id only where today's is wrong.
- **A resolver call site missed in phase 3** resolves a cell's paragraph without its table
  style, silently, where the preprocess used to cover every site at once. The list of §4.6 is
  the guard, and the zero-delta gate catches only what the corpus exercises.
- **Context leaking across a story boundary** on the visitor pathway (§4.6, T8).
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

Revised after the second review (phase 0 gained three probes; phase 2 the id sweep and the
round-trip gate):
- Phase 0: done (T1 to T8).
- Phase 1: a day and a half: the overloads and contexts, then the equivalence harness and
  the explanation of its difference classes.
- Phase 2: two to three days: unique names, the style-id sweep with its tests, D1, D2, D4
  and D6, the corpus gate and the round-trip gate.
- Phase 3: a day: the call-site list and the story-boundary reset, with T8.
- Phase 4: half a day.
- Phase 5: half a day, plus the hand-offs.
