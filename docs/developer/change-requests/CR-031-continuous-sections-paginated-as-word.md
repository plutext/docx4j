# CR-031: continuous sections paginated as Word paginates them - a page's vertical margins from the section that owns it, and no page for an empty block at a sequence's end

Status: IN PROGRESS. Proposed 2026-10-05 (Jason, after batch 52 part 2: "fix the older defect and
turn them on?" - "yes please" to writing this CR). Phase 0 DONE 2026-10-05 (Word run; readings in
§2, D3). Phase 1 DONE 2026-10-05 (D2, with `restartParityBlankPage` on by default; gate b111).
Reviewed 2026-10-05 (another session): recommends 4.2b, with six design findings and the
staleness below folded in (§4.2b, §5 P6, §6, §7, §8). **Decision 1 made 2026-10-05 (Jason): 4.2b**,
the fork extension. Phase 2 next (registry `docx4j/CR-031.2`), depending on the fork's **fop/CR-017**
"page masters chosen by the content a page starts with" (written 2026-10-05 and revised after a
review, 8b509a0e8 on `2.11-docx4j.5`; proposed, about five to six days; implementation waits on
Jason's go there). **Phase 2's docx4j side DONE 2026-10-05** (305264ed3,
1ee6dfa8f; §6), gated end to end on fop/CR-017's snapshot (b114 control 0 movers; b115/b118); it
reaches users with the fork release carrying CR-017.
P6 read (§2 D3); P7 proposed.

## 0. Why now

Batch 52 part 2 (CR-001 fidelity work, `../docx4j-portfolio/docs/layout_fidelity_plan.md`) found
two rules that Word's PDFs bear out but that cost class 2 documents when switched on, because each
leans on how docx4j paginates continuous sections. Both were committed off by default:

| property | rule | blocked by |
|---|---|---|
| `docx4j.convert.out.fo.wordLayout.tableTakesPageBreak` (ed3d7b268) | a table after a break-only paragraph starts at the page top | 12802 1.0000 -> 0.72: a continuous section's top margin (D1); 4994 a page short: the page-break line (outside this CR) |
| `docx4j.convert.out.fo.wordLayout.restartParityBlankPage` (b3c39ed0c) | with odd and even headers, a nextPage restart repeating the parity gets a blank page | 9539 a page short -> two over: an extra page docx4j gives its first sequence (D2) |

This CR fixes D1 and D2 and turns `restartParityBlankPage` on (done, phase 1). `tableTakesPageBreak`
also needs the page-break line (`pageBreakParagraphLine`, the other half of 4994), a density
question rather than a section one, so turning it on belongs to that triage (§9), which depends on
this CR's phase 2 for 12802; this CR closes at phase 3 with the property still off.

## 1. What docx4j does today (read from the code, 2026-10-05)

- `ConversionSectionWrapperFactory.processComplete` **merges a run of continuous sections into one
  page-sequence** (one `ConversionSectionWrapper`), unless the continuous section changes the page
  size or orientation, or opens with a paragraph or table that breaks the page (`startsPage`,
  17.2.0); those start a page-sequence of their own.
- The merged sequence's page masters carry **one set of page margins**:
  - left and right from the reference part (`marginReference`: the first part, or the widest
    multi-column part, 17.1.0), every other part carrying its difference as block indents (the
    `XSLT_Ind` container) and spanning all columns where it has fewer (`XSLT_Cols`);
  - top, bottom, header and footer distances from **the first part, always** (`usePartMargins`,
    17.2.0): "a page master has one before-edge".
- Page numbering is the first part's to declare a start or format (`usePartPageNumbering`, 17.2.0).
- The running header and footer are the merged run's (`HeaderFooterPolicy` chained through the
  parts), drawn at the first part's margins (`PageDimensions.headerFooterIndent`, 17.2.0).
- The paragraph carrying a section break is kept only where it renders something (or is all the
  section has, or closes a vertically aligned table): "Word gives it no line at all when it is
  empty ... and where it does not fit, starts a page of its own carrying only the running header"
  (`rendersNothing`, 17.0.5 / 17.1.0).
- Two-pass rendering exists already: `FOPAreaTreeHelper` lays the document out once to measure
  header and footer extents (`LayoutMasterSetBuilder`), and CR-012's `Paginate` reads the area tree
  to place `w:lastRenderedPageBreak`.

## 2. Defects (measured on the corpora, 2026-10-05)

**D1. A continuous section's top and bottom margins are not applied on the pages it owns.**
Corpus document 12802 (mode 15): section 0 has `w:pgMar` top 720 (36pt); the continuous sections 1
to 3 have top 860 (43pt); continuous section 4 has top 617 (30.85pt). Word's first lines:

| page | Word | docx4j (b104) | the top margin Word's first line fits |
|---|---|---|---|
| 2 | 43.1 | 53.8 | 43pt, sections 1 to 3 |
| 3 | 43.3 | 34.9 | 43pt, sections 1 to 3 |
| 4 | 43.4 | 35.5 | 43pt, sections 1 to 3 |
| 5 | 31.1 | 34.4 | 30.85pt, section 4 |

docx4j draws every page on section 0's 36pt. (Which section's text opens each of Word's pages is
read from the margins here, not traced. Page 2's 53.8 is the line the break-only paragraph kept
above a table, which `tableTakesPageBreak` removes - to 41.1, against Word's 43.1 - and which is how
that rule exposed this.)

**An existing golden confirms the rule** - `section-continuous-geometry` (probes, cut before this
CR): five continuous sections, each over a page long; S2 has a 2in top margin, S3 a 290.55pt footer
distance over an empty footer part, S4 a 35.45pt one, S5 a 2in left margin. Word's pages:

| page | sections on it | Word: first line / last line | docx4j (b110) |
|---|---|---|---|
| 3 | S2 | 144.4 / 729.1 (S2's) | 143.4 / 729.8 |
| 4 | S2, then S3 from mid-page | 144.4 / 729.1 (**S2's**, though S3 starts on it) | 143.4 / 729.8 |
| 5 | S3 | 72.4 / 508.9 (**S3's**: its footer distance ends the page high) | 143.4 / 729.8 |
| 6 | S3, then S4 | 72.4 / 508.9 (S3's) | 143.4 / 729.8 |
| 7 | S4 | 72.4 / 755.9 (S4's) | 143.4 / 729.8 |
| 9 | S5 | 72.4, lines at x=144.1 (S5's) | 143.4, x=144.0 |

So **a page takes the vertical margins - top, bottom and footer distance - of the section that owns
its first line**, and a section starting mid-page changes nothing until the next page. docx4j merges
S2 to S5 into one sequence on S2's margins, and its pages 5 to 8 are 71pt low and 220pt long at
the foot. (S2 itself opens a page in both, because the probe writes 11906x16838 there against the
document's 11907x16839: a page size change, which starts a page.) This golden is phase 2's
acceptance test. Phase 0 confirmed the rule, with the header and footer distances, and found the
header and footer text following the same section (D3, below).

**D2. An empty block at the end of a page-sequence starts a page of its own.** Corpus document
9539: its first page-sequence (A3, sections 0 to 2, merged) ends with the paragraph
"(Queensland Studies Authority Template, 2012)<w:br w:type="page"/>". `PageBreak` splits it at the
break, and its empty second half ends the flow. `WordLayoutFixups.mergePageBreakParagraphs` removes
that half's break, since another section follows and starts a page anyway (17.2.0). But it keeps
the block, a preserved space on an 11.5pt line, and the block does not fit above the 782.1pt foot
(the text ends at 773.1). So FOP starts a second A3 page holding only the running header and
footer, which Word does not have (Word's text ends 4pt lower, at 778.3, and the page has no
second line).

The 17.2.0 comment there records this very document. Removing the block as well was measured, and
held back: it took the page, but line parity fell, because Word also has two blank pages
(before two nextPage sections restarting at 7, with odd and even headers) which docx4j does not -
"the two belong together". Those blank pages are what `restartParityBlankPage` (b3c39ed0c) now
writes. **Measured together on 2026-10-05**, with the block removed where it holds nothing:

| | pages (Word 22) | line parity | page parity |
|---|---|---|---|
| neither (b110) | 21 | 0.8880 | 0.3410 |
| the block removed | 20 | 0.9145 | 0.8398 |
| `restartParityBlankPage` | 24 | 0.8904 | 0.3361 |
| **both** | **22** | **0.9145** | **0.9048** |

That page is also why `restartParityBlankPage` went two over on its own: it shifted the folio
parity by one. **The corpus gate, b111 against b110** (four corpora and the probes): three movers,
all to Word's page count and none worse - 9539 21 -> 22 (line parity 0.8880 -> 0.9145), 6749 24 ->
25 (evenAndOddHeaders and a nextPage restart at 2: the parity blank page alone), and the
`page-number-restart-parity-eo` probe 10 -> 12 (Word 12; b110's 8 there predates cand39). Class 2
LTR: 367 documents on Word's page count, from 365.

**D3, measured in phase 0 (Word run 2026-10-05).** The probes of §5, read from Word's PDFs
(`pdftotext -bbox-layout`, top of the line box; 72pt margins put the first line at 80.5):

| probe | page | first line | Word: header text / y | first line y | footer text / y |
|---|---|---|---|---|---|
| P1 (both modes) | 1 | S1 1 | S1, folio 1 / 36.0 | 80.5 | S1 / 792.5 |
| | 2 | S1 30 (S2 starts at line 17) | **S1**, folio 2 / 36.0 | 80.5, last 752.6 | **S1** / 792.5 |
| | 3 | S2 14 | S2, folio 3 / **18.0** | **152.5**, last 776.6 | S2 / **810.5** |
| | 5 | S2 68 (S3 starts at line 4) | **S2**, folio 5 / 18.0 | 152.5 | **S2** / 810.5 |
| P2 at-top | 2 | S2 1 (S2 opens the page) | **S2**, folio 2 / 18.0 | 152.5 | S2 / 810.5 |
| P3 restart | 2 | S1 30 (S2 starts mid-page) | S1, folio **2** | | S1, folio 2 |
| | 3, 4 | S2 | S2, folio **2**, **3** | | |
| P4 titlePg | 2 | S1 30 (S2 starts mid-page) | **S1** default | | |
| | 3, 4 | S2 | S2 **default** (never the first-page header) | | |
| P5 sect-break foot | 1-6 | | each case 2 pages, 6 in all; every "after" line at the next page's top, 80.5 | | |
| P6 restart, evenAndOddHeaders | 2 | S1 30 (S2 starts mid-page) | S1 EVEN, folio 2 | | S1 EVEN, folio 2 |
| | 3 | S2 19 | S2 ODD, folio **1** | | S2 ODD, folio 1 |
| | 4, 5 | S2 | S2 EVEN, folio 2; S2 ODD, folio 3 | | |
| P6 mirror twin | 1-5 | as P6 | as P6; body and headers at x=108 on pages 1, 3, 5 and x=36 on 2, 4 | | |

1. **D1's rule holds, and takes the headers and footers with it.** A page takes everything from
   the section that owns its first line: top and bottom margins, header and footer distances, and
   the header and footer text. A section starting mid-page changes nothing on that page, and one
   starting at a page top owns it (P2). Modes 14 and 15 are identical, to the point.
2. **A continuous restart counts from the page the section starts on - but not in a document with
   `w:evenAndOddHeaders`** (P6, read 2026-10-05). Without them (P3) the folios are 1, 2, **2**, 3:
   page 2 is S2's page 1, and shows S1's footer with S1's folio, 2. With them (P6, the same shape
   otherwise; the re-saves differ only in that setting) the folios are 1, 2, **1**, 2, 3: S2's page
   1 is page 3, the first page S2 owns. Why the origin moves is not yet known. Two readings fit:
   (H1) with odd and even headers the count starts at the first page the section owns; (H2) Word
   puts the origin where the folio's parity matches the physical page's - here page 3, odd, takes
   the odd start number 1. Probe P7 (§5, proposed) separates them.
3. **A continuous section's first page is the page it starts on**, so where it starts mid-page its
   `w:titlePg` first-page header is never printed (P4): that page shows the section before's
   header, and the next page the section's default.
4. **An empty section-break paragraph at a full page's foot takes no line on the next page**,
   whatever the next section's break (continuous, continuous on another page size, nextPage): P5's
   "after" lines all open their page at 80.5, and no page holds only a header. That is D2's rule,
   for the section-break paragraph itself as well as for the split break half.
5. **After a continuous restart, odd/even headers and mirrored margins follow one parity together**
   (P6 and its mirror twin: the odd header and the inside margin on the left, x=108, on pages 1, 3
   and 5; the even ones, x=36, on 2 and 4). But in P6 the printed folio and the physical page have
   the same parity on every page, so which of the two governs is still open; P7's first document
   puts an even folio on an odd page under H1.

## 3. Corpus facts (the four corpora on b110, 2026-10-05; page counts brought to b112)

- **34 documents** change the top or bottom `w:pgMar` at a continuous section break; 26 are class 2.
  In two of them (9539, 11741) the break also changes the page size, which starts a page-sequence
  with its own masters already, so **D1's population is 32**: documents with a merged run whose
  parts differ in top or bottom margin.
- Two of the 32 (719 and 11256, one Turkish template) have **side floats** (`fo:float`, six each) in
  that run, all in part 1, before the one part boundary (part 1 top margin 0, part 2 70.85pt, the
  last fifth of each). fop/CR-017's test 13 decides whether a marker after a side float is honoured
  or reported and ignored; in the second case those two keep today's margins.
- **9 were off Word's page count on b110**: 12317 (Word 2, docx4j 1), 5507 (4, 3), 2600 (15, 13),
  7235 (15, 16), 1137 (15, 16), 11741 (56, 54), 719 (29, 28), 9539 (22, 21), 11256 (25, 27). On b112,
  **8**: 9539 is at Word's 22 (phase 1), and 11741 at 55 of 56 (a558bfcbf, the size-change header);
  the other seven are unchanged.
- How many of those nine D1 explains is not known until it is fixed: several also carry other
  differences (7235's table grid was one, fixed in 7a12ed272).
- D2's shape - an empty block ending a page-sequence pushed onto a page of its own - is in 9539.
  Three other documents end on a header-only page (8460, 5320, 11092), but there the page before
  ends 12 to 70pt lower in docx4j than in Word, so they are density, not D2.

## 4. Design

### 4.1 D2: no page for an empty block at a sequence's end

Where `mergePageBreakParagraphs` takes the break off the empty half of a split paragraph that ends
a flow before another section, it removes the half as well if it holds nothing (`blankBlock`).
Done together with `restartParityBlankPage` on (phase 1, §2's table). Small. **As committed
(48852f8ea)** the half stays where it carries an `id` attribute (something may name it) or where it
also opens its flow (it would be the flow's only block, and an `fo:flow` must hold one). A half
holding a bookmark is never collected at all - its `fo:inline id` makes it non-empty - and keeps its
break, as before this CR.

### 4.2 D1: page masters chosen per page from a layout pass

A page-sequence's region margins cannot change mid-sequence, and FO chooses a page master only by
position (first, rest, last), parity and blankness, never by content. So the page each merged part
first owns has to come from a layout:

1. **Pass 1** renders as today, with an id on the first block of each merged part.
2. From the area tree (as `FOPAreaTreeHelper` and `Paginate` already read it), find for each page
   of a merged sequence the part that owns its first line. Most sequences give a short map:
   pages 1-1 part A, pages 2-4 part B, then part C.
3. **Pass 2** gives that sequence a `fo:page-sequence-master` listing those masters explicitly:
   `fo:single-page-master-reference` (or a bounded `fo:repeatable-page-master-reference`) per run
   of pages, ending in the last part's ordinary alternatives (first/rest, odd/even, blank). Each
   part's master is the sequence's master with that part's vertical `w:pgMar`; left and right
   stay the reference part's, with the indents as today.
4. New margins change how much a page holds, so the map can move. Re-run until it stops moving,
   at most three passes, keeping the last; measure how often it takes more than one.

Only documents with a merged run whose parts' vertical margins differ pay the extra passes (34 of
the corpora's ~550). The header and footer extent pre-pass and `Paginate` already re-lay the
document out, so this joins an existing pattern rather than adding a new one.

**Alternatives considered:**
- *Split the page-sequence where a part takes over.* A page-sequence always starts a page, so the
  split must fall exactly on a page boundary that only a layout knows. It still needs pass 1, and
  splits paragraphs across sequences. Rejected.
- *A fork extension*: explored in 4.2b (2026-10-05), at Jason's word that the fork is maintained
  indefinitely in any case. It is no longer rejected; decision 1 (§7) chooses between it and 4.2.

### 4.2b D1 as a fork extension: page masters chosen by the content a page starts with

Read from the fork (`../xmlgraphics-fop-plutext`, branch `2.11-docx4j.5`), read-only; the fork-side
CR and the code are the fork session's. **That CR is fop/CR-017** (2026-10-05), which fixes what
docx4j builds against:
- the attribute `fox:page-sequence-master-reference="<master-name>"` on a block-level FO in the main
  flow (block, block-container, list-block, table) whose ancestors up to the flow are blocks or
  block-containers - never inside a table cell, list item, footnote, float or inline (reported and
  ignored); pages before the first marker keep the sequence's own `master-reference`, so the first
  part needs none;
- the named master must be one unbounded `fo:repeatable-page-master-alternatives` (what docx4j
  writes now), its conditions tested per page with no walk state; any other form, or a master of
  another body width or column count, or whose region-body's region-name is not the flow's
  flow-name, is reported once and ignored (docx4j's region-body states no region-name, so it is
  `xsl-region-body`, the flow's name);
- the capability `page-master-by-content` (`Docx4jFop.PAGE_MASTER_BY_CONTENT`);
- blank padding pages take the previous page's owner and its blank alternative;
- exact for single-column flows; on multi-column pages consistent but not guaranteed optimal;
- a part starting right after a page break (9539's shape) has its still-empty page replaced with its
  own master once the list shows its first box;
- no restart offset until P6 is read (a later step of fop/CR-017, about a day);
- the extent hook (phase 5) is not in fop/CR-017: it gets its own fork CR when phase 5 is taken up,
  on the same `PageProvider` seam.

The design as first written here follows; fop/CR-017 refines it (and its §3.3 states the every-column
rule for a list starting mid-page).

**What FOP does now.** A page's master is chosen by position alone:
`PageProvider.cacheNextPage` asks `PageSequence.getNextSimplePageMaster(index, first, last, blank)`,
which walks the page-sequence-master's alternatives. The page breaker asks how tall each page is
through `PageBreakingAlgorithm.getLineWidth(line)` -> `PageProvider.getAvailableBPD(index)`, by page
(part) number. So FOP already lays out pages of different body heights - a first-page master with a
taller region-body works today - as long as the height is a function of the page's number. What it
cannot do is let the height depend on what the page starts with.

**Why it fits the algorithm.** FOP's page breaker is Knuth's total-fit. Every candidate page is
measured from an active node, and in all five places the page height is asked for
(`computeDifference`, three times, and `createFootnotePages`, twice) that node is in hand, with its
`position`: the element where the page before ended. The part owning the first box after that
position is the part owning the page's first line - Word's rule. A height that depends on the
starting node is sound in Knuth's model: candidates that end at the same element compete, and they
start the next page at the same place, so they agree on its height. The search stays exact: every
candidate page is measured with the margins it would really get, in one pass, with nothing to
oscillate.

**The extension (fork side).**
1. An attribute on a block in the main flow, `fox:page-sequence-master-reference="s2"` (fop/CR-017): pages whose first line lies at or after this block, up to the next such block,
   take their masters from that page-sequence-master instead of the sequence's own. FOP already keeps
   unknown attributes on every FO (`FObj.addForeignAttribute`), so the FO tree needs nothing new.
2. When the breaker builds an element list, record where each marked block's first element falls
   (the elements' layout managers lead to their FOs), carrying the current part across lists - FOP
   splits a flow into lists at forced breaks and span changes. docx4j marks each part's outermost
   block: the `span="all"` block (`XSLT_Cols`) where the part has fewer columns, else the part's
   first block-level FO. (`XSLT_Ind` has no container: since 17.0.5 both pathways add the margin
   difference to the part's own paragraphs and tables, `XsltFOFunctions.shiftIndents`, because a
   block-container in a multi-column flow made FOP throw when balancing; corrected 2026-10-05, this
   step first said "indent container".) Not an FO that generates no boxes - an absolutely positioned
   block-container (a floating table or picture) or an `fo:float` - since parts are located by
   walking boxes to their marked FOs: the marker goes on the part's first in-flow block-level FO.
   fop/CR-017 (c910fcdcf, test 9) adds a safety net: a marked FO that produces no box (an absolute
   block-container) starts its part at the next box rather than being lost; a marker on an
   `fo:float` is reported and ignored. (An empty `fo:block` carries a zero-width box of its own, so
   its marker is seen directly: fop/CR-017's second review, 969a56765.)
3. `PageBreakingAlgorithm.getLineWidth` takes the node: the page it starts is owned by the part of
   the first box after `node.position`. For a column that is not its page's first, follow
   `node.previous` back to the node which started the page. The five call sites change; nothing else
   in the algorithm does. Two cases have no first box of their own:
   - **A list that starts mid-page** (review finding 1). After a span change FOP starts a new element
     list on a page that already exists (`startColumnOfCurrentElementList`), and
     `getAvailableBPD` answers with that page's `getRemainingBPD()`. That page already has an owner,
     so the first page of a continuing list keeps the owner of the page it continues on, and its
     height is the remaining height as now - for every column of that page, not only the list's
     first node; ownership is decided afresh only from the list's next page. (The fork CR is to say
     so explicitly.) This is the common case, not a corner: a part with fewer columns is a span change exactly
     at a part boundary (`XSLT_Cols`), on both sides of it, so the part after a column change in
     either direction starts its list mid-page, on a page the part before owns.
   - **A page holding only footnote bodies** (`createFootnotePages`, two call sites; finding 3):
     it inherits the owner of the page before it. Not measured in Word; a probe can confirm it if a
     corpus document shows the shape.
4. `PageProvider` gives the height of page `index` under a given part's master without caching a
   page for it (several candidates ask about the same index with different parts), and when areas are
   added (`PageBreaker.startPart`) it is told the owner of the page being started, and replaces a
   cached page whose master differs - the replacement path it already has for blank, last-page and
   span mismatches (`newPageVP.replace(oldPageVP)`, `IDTracker.replacePageViewPort`). Its one-entry
   height cache (`lastRequestedIndex` / `lastReportedBPD`) is keyed by index alone and would hand one
   part's height to a candidate asking for another (finding 2): it is keyed by index and part.
5. "First page" for a part means the page its first line opens, so a part starting mid-page never
   uses its `first` master (P4), and one starting at a page top does.
6. Optionally (phase 3), a restart on the marker: pages the part owns print its start number plus
   the pages since the page it started on, which reproduces P3's 1, 2, 2, 3. **P6 (read 2026-10-05)
   showed that is not the whole rule**: with `w:evenAndOddHeaders` the count starts at the first
   page the part owns (1, 2, 1, 2, 3), and which parity governs the odd/even master is still
   unseparated (§2, D3 items 2 and 5). P7 (§5) reads both before the restart step is designed.
7. Inert unless the attribute is present ("changes nothing FOP does on its own"), with a capability in
   `Docx4jFop`.

Constraints: the parts' masters keep the reference part's width and column count, as docx4j's merged
masters do now, so FOP's IPD-change restart (`restartAtLM`) is never triggered. Odd and even masters
whose body heights differ make a page's height depend on its folio as well. That is still a function
of the node chain, but it is the fiddliest case.

**docx4j side (needed for 4.2 as well).** Per-part masters for a merged sequence, each with that
part's top and bottom margins and header and footer distances. The attribute goes on each merged
part's first block (the factory knows the boundaries: `MergedPart`). Gated by `FopCapabilities`: on
Apache FOP nothing new is written, and the output is today's. **As built (phase 2, 2026-10-05)** the
parts' masters keep the page-sequence's region names, so its pages show the page-sequence's own
headers and footers, as they do today; giving each part its own static content (its own
`HeaderFooterPolicy`, its own region names) is phase 3's, with `w:titlePg` and the restart. That
split also makes the extents free: the same static content in the same width is the same height,
so each part master takes the extent measured for the page-sequence's master of its kind, with its
own vertical margins for the body's edges, and the pre-pass needs no change (§6 phase 2).

**Against 4.2:**

| | 4.2 layout passes | 4.2b fork extension |
|---|---|---|
| render time, affected documents | 2x to 4x | unchanged (one pass) |
| exactness | a fixed point if one is reached, else the last of three | exact for single-column flows; multi-column pages consistent, not guaranteed optimal (fop/CR-017) |
| Apache FOP | works | today's behaviour (capability-gated) |
| code | docx4j: the pass loop, an explicit page-sequence-master per sequence | fork: about five call sites, `PageProvider`, the attribute; docx4j: the attribute |
| restart folios (D3) | literal folios in pass 2, or a split sequence | a numbering offset in the extension |
| upkeep | docx4j only | fork code in `PageBreakingAlgorithm`/`PageProvider`, which change little upstream; carried through each merge (fop/CR-009's pattern) |
| upstream | - | not expected to be taken as it stands; a JIRA can describe the need |

**Risks:** the page cache replacement (pages made during breaking with the wrong master, IDs
registered against them); footnotes and floats, whose code reads the page height too; balanced
columns at span changes (`BalancingColumnBreakingAlgorithm` has its own height); the last-page
re-layout (docx4j writes no `page-position="last"`, so out of scope at first). Each needs a FOP-level
test before docx4j gates it. The P1 probes and the section-continuous-geometry golden are the
acceptance tests on the docx4j side, unchanged.

**A companion hook: the header and footer extents measured in FOP (2026-10-05).** 4.2b decides
*which* master a page takes; it does not decide how large the body is *inside* a master. That is
the header and footer extent pre-pass (`LayoutMasterSetBuilder.fixExtents`, `FOPAreaTreeHelper`): a
partial deep copy of the package, its body replaced by filler (`trimContent`), floating drawings
removed from its headers and footers, STYLEREF painted from its cached result, converted to FO and
rendered by FOP to an area tree; each simple-page-master's region-before and region-after heights
are read from that tree (`calculateHFExtents`), and `adjustLayoutMasterSet` writes Word's rule into
the masters - body top = max(top margin, header distance + header height), and the mirror at the
foot - with docx4j's exceptions (no reserve for a header or footer docx4j invents for `w:titlePg`
or `w:evenAndOddHeaders`, a negative top margin fixing the body top, a footer distance past a
quarter of the page not honoured, an empty footer part unlike an absent one). It runs on every PDF
conversion, not only on the documents this CR is about.

4.2b makes `PageProvider` the one place that answers "how tall is this page's body?", so a second
hook fits the same seam: the first time a master is used, FOP lays out that master's region-before
and region-after static content (its `StaticContentLayoutManager`, which otherwise runs when the page
is finished), measures it, and sets the body's edges by the rule, where the FO asks for it (an
attribute on the simple-page-master carrying the top and bottom margins; the header and footer
distances are the master's own margins). docx4j keeps its exceptions by encoding them in the FO -
no attribute where nothing is reserved, a fixed body top for a negative margin, the margin alone for
an implausible footer distance - rather than applying them after a measurement.

- **Every document gains**: one FOP render fewer per conversion (and the deep copy and the trimmed
  document's FO conversion with it).
- **A class of defect goes**: the pre-pass measures only the masters its filler pages reach, and its
  merging must match the real pass's - when it did not, a 179-page document came out as 1,429 pages
  (fixed in 17.2.0 by `followedByPageStart`). 4.2b's per-part masters are each used only on pages a
  part owns, which filler would reach only with more such contrivance; measured in FOP, each master
  is measured on the page that uses it.
- **Closer to Word, later**: STYLEREF in a header is an `fo:retrieve-marker` (`StyleRefMarkers`),
  whose text, and so whose height, varies from page to page; the pre-pass takes the field's cached
  result. A per-page measurement is possible in FOP, but circular - the header's markers come from
  the page's body, whose height depends on the header - so per master first (today's behaviour), per
  page as a later refinement once Word's behaviour there is probed.

Caveats: the pre-pass measures a doctored copy, and the in-FOP measurement must leave out the same
things - floating drawings in headers and footers (marked by docx4j, or emitted so they take no
height) and the edge spacing of paragraphs emptied by their removal. On Apache FOP the pre-pass stays,
gated by `FopCapabilities` like 4.2b, so its code remains as the fallback. It can go into the same
fork CR as 4.2b, as a second hook sharing the `PageProvider` change, but it is **gated and landed
separately** (review finding 5): it changes every PDF conversion where D1 touches some 34 documents,
so it is its own phase (§6, phase 5) with its own corpus gate, and phase 2 does not depend on it.
Phase 2 therefore measures the per-part masters with the existing pre-pass, which needs each part's
masters reached by filler: the trimmed copy gives each merged part a page-sequence of its own
(followedByPageStart's pattern), since a part's header and footer heights do not depend on its
being merged. Gate: the corpora and probes on the fork with the hook
against the pre-pass, every master's extents equal to the point (they are the same layout of the
same static content) except where the doctored copy and the real header differ.

### 4.3 D3: header and footer text, first-page header, restart

Phase 0 has answered (§2, D3), and all three follow the owner of the page, so they ride on whatever
decides the owner - 4.2b's per-page master choice (recommended), or 4.2's page map:

- **Header and footer text**: a page's header follows the part that owns it, so 4.2's per-part
  masters carry per-part `fo:region-before`/`-after` names and static content, which
  `LayoutMasterSetBuilder` already writes per section.
- **`w:titlePg`**: a part's first-page master is used only where the part owns the page it starts
  on (starts at a page top); one starting mid-page goes straight to its default (or odd/even)
  master. Under 4.2b that is step 5's meaning of "first page"; under 4.2 the explicit
  `fo:page-sequence-master` expresses it.
- **Restart**: the folio printed on a page the restarting part owns is its start number plus the
  pages since the page it started on (P3: 1, 2, 2, 3). One `fo:page-sequence` has one counter,
  and `fo:page-number` takes no offset. Under 4.2b the extension carries the offset (step 6); under
  4.2 either pass 2 writes those folios literally into the part's static content (the page map
  knows them), or the sequence is split at the page where the part takes over. Odd/even after such
  a restart waits on probe P6. Decision 2 (§7); only documents with a restart at a continuous break
  pay it.
- **Done ahead of phase 3**: a continuous break which changes the page size already ends the
  page-sequence (§7 of the rules), so the section after it owns its first page, and it now takes
  its own headers and footers there (`ConversionSectionWrapperFactory`, `followingStartsPage`).
  P5's case B: Word's landscape page shows that section's header, docx4j showed the one before.
  Gate b112 against b111: 11741 54 -> 55 pages (Word 56), line parity 0.9397 -> 0.9678, and the
  probe +1 line; nothing else moved.

## 5. Phase 0: Word probes (for one Word run)

Mode 15, with a mode 14 twin of P1. Exact 24pt lines so baselines read cleanly; each section's
header and footer name the section and print the folio.

D1's rule is already measured (§2); the probes read what it leaves open - the header and footer
distances and texts together with it, mode 14, a section starting at a page top, restarts,
`w:titlePg`, and D2.

- **P1 `continuous-margins-vertical-compat15` / `-compat14`**: S1 top/bottom 72/72pt, header/footer
  distances 36/36, a page and a half; S2 continuous, top/bottom 144/36, distances 18/18, three pages;
  S3 continuous, back to 72/72, starting mid-page. Read: each page's first and last baselines, its
  header and footer text and their y.
- **P2 `continuous-margins-at-top`**: S1 fills its last page exactly, so S2 (continuous, other
  margins) starts at a page top. Read: that page's margins.
- **P3 `continuous-restart`**: S2 continuous with `w:pgNumType w:start="1"`, starting mid-page, and
  running two pages. Read: the folios.
- **P4 `continuous-titlepg`**: S2 continuous with `w:titlePg` and a first-page header, starting
  mid-page. Read: which header the next page carries.
- **P5 `section-break-paragraph-foot`**: a page filled to 3pt above its foot, then an empty paragraph
  carrying a section break whose next section is (a) continuous, same page size; (b) continuous
  with a page size change (9539); (c) nextPage. Read: the page count and any page holding only the
  header.
- **P6 `continuous-restart-evenodd`** and **`-evenodd-mirror`** (review finding 4; cut and read
  2026-10-05, readings in §2 D3 items 2 and 5): P3's shape -
  S2 continuous, `w:pgNumType w:start="1"`, starting mid-page 2 and running to page 5 - with
  `w:evenAndOddHeaders`, each section's odd and even headers naming themselves, and a mirror-margins
  twin (inside 108pt, outside 36pt). Read: which header (odd or even) and which side margins each
  page takes after the restart, where the folios run 1, 2, 2, 3, 4 - by the printed folio, or by the
  physical page.

- **P7** (proposed 2026-10-05, after P6; for the restart step, phase 3), all with
  `w:evenAndOddHeaders` and each section's odd and even headers and footers naming themselves:
  - `continuous-restart-evenodd-start2`: P6 with `w:start="2"`. H1 puts folio 2 (even) on physical
    page 3 (odd); H2 counts from page 2, so page 3 prints 3. Under H1, page 3's header (EVEN or ODD)
    says whether the header follows the folio or the physical page.
  - `continuous-restart-evenodd-start2-mirror`: the same with `w:mirrorMargins` (inside 108pt,
    outside 36pt): page 3's x says whether the margins follow the folio or the physical page.
  - `continuous-restart-evenodd-oddstart`: S1 15 lines, so S2 (`w:start="1"`) starts mid-page 1,
    a physical odd page. H1: page 2, the first S2 owns, prints 1 (an odd folio on an even page);
    H2: counting from page 1, page 2 prints 2.

## 6. Phases

0. Probes (§5), Word run, readings into §2. **DONE** (P1-P5). P6 cut 2026-10-05, awaiting the Word
   run; read before phase 3.
1. D2 (§4.1), with `restartParityBlankPage` turned on: they belong together (§2). Gate: 9539 to
   Word's 22 pages; the even/odd folio probe to Word's 12. **DONE** (b111, §2).
2. D1, by **4.2b** (decision 1, 2026-10-05): fop/CR-017 (§4.2b steps 1-5, 7), released or as a
   gated snapshot; in docx4j, per-part masters with their own vertical margins and distances,
   the marker on each part's outermost block, all behind `FopCapabilities`. **docx4j side BUILT
   2026-10-05**: `ConversionSectionWrapperFactory` tags each part at which the vertical margins
   change (`XSLT_Part=m`, outermost, so the `span="all"` block is what gets marked) and hands the
   distinct sets to the wrapper (`getPartVerticalMargins`); both FO pathways stamp the part's
   top-level FOs (`XsltFOFunctions.stampPart`); `LayoutMasterSetBuilder.addPartMasters` writes
   `s<n>-p<m>` (one unbounded repeatable-page-master-alternatives, the section's kinds without
   the first-page one, the page-sequence's region names; mirrored even masters as for the
   section); `FOPAreaTreeHelper.adjustLayoutMasterSet` sizes them from the page-sequence's measured
   extents; `WordLayoutFixups.pageMastersByContent`, last before `stripHints`, puts
   `fox:page-sequence-master-reference` on the first allowed FO where the master changes. Property
   `docx4j.convert.out.fo.wordLayout.pageMasterByContent` (`auto` = the capability; `true`;
   `false`); never in the extent pre-pass. Test `PageMasterByContentTest` (both pathways). Forced
   on, on today's renderer: the markers fall at P1's S2 and S3, 12802's sections 1 and 4, and
   section-continuous-geometry's S3, S4 and S5, and nothing renders differently (the renderer
   ignores the attribute).
   **Measured on fop/CR-017** (renderer r10, 2026-10-05). Control b114 (markers off) against b113:
   0 movers. Measurement b115 (on) against b114: no exception; the P1 probes' page parity 0.917 ->
   0.959, continuous-margins-at-top 0.920 -> 0.947; 12802's pages 3 and 4 start at 41.9 and 42.5
   (Word 43.3, 43.4; were 34.9, 35.5); 7235 +22 lines (its pages at Word's 35.9 top, were 27.1; one
   line spills to a 17th page, density); 5507 +1, 14067 page parity up. The other 29 of the 32 do
   not move in the scores, which tolerate a few points (12802's 7pt did not register either). Two
   misses:
   - 12802's page 5 kept the masters before (section 4's block opens it with a space-before, whose
     box FOP's space resolver makes without a layout manager, and CR-017 took it for the part's
     first box): fixed in the fork, 7c6f43b07. Re-gated on renderer r11 (d5a7616b9): page 5 starts
     at 29.2 (Word 31.1) and ends at 753.8 (754.3), pages 3 to 6 within 2pt of Word; b118 (cand43 on
     r11) against b116 (on r10): 0 movers, so everything b115 moved holds.
   - section-continuous-geometry's pages 5 and 6 took S3's top but not its foot, and the probe lost
     a page (9 -> 8). Two causes. docx4j's 17.1.0 clamp ignored a footer distance past a quarter of
     the page over an empty footer part - read off corpus document 5507's page 3, which its second
     section owns (D1): its first section's pages honour the 290.55pt. Corrected (gate b116 on r10
     against b115: 5507 3 -> 4 pages, Word's 4, line parity 1.0000; b117 on r9 against b113, the
     clamp kept on a merged run with one master: 0 movers). And the probe's S3 declares its own
     (empty) footer where the run's first section declares none, so its pages, showing the
     run's footers, reserve nothing: that is phase 3's (each part's own headers and footers), and
     pages 5 and 6 of that golden wait on it; its pages 3, 4, 7 and 8 have Word's tops now.
   **Net, b112 -> b118** (cand43 on r11): 5507 +2 lines, 3 -> 4 pages (Word's 4), line parity
   1.0000; 7235 +22 lines; section-continuous-geometry 9 -> 8 pages (phase 3, above); class 2 LTR
   documents on Word's page count 367 -> 368; nothing else moves in the scores. Phase 2 is DONE on
   docx4j's side; it reaches users with the fork release carrying CR-017 and docx4j's renderer
   version bump (export-fo's `fo.renderer.version`). **4.2**: the pass loop and the explicit page-sequence-master. Either way the
   gate is the same: P1's pages at Word's margins and headers' distances, the
   section-continuous-geometry golden's pages 5 to 8, 12802's page tops at Word's, the 34
   documents read one by one, nothing else moving; on 4.2b, both renderers (Apache FOP unchanged).
3. D3 (§4.3): each part's own header and footer text on the pages it owns (per-part region names
   and static content in both pathways, and the pre-pass measuring them - phase 2 keeps the
   page-sequence's), `w:titlePg` (step 5), the restart folio (step 6, or 4.2's literal folios),
   with P7 read first. Scope per decision 2.
4. *(moved out)* Turning on `tableTakesPageBreak` needs the page-break line as well (4994), which
   is outside this CR (§9). It moves to that triage, which depends on phase 2 here (for 12802).
   This CR closes at phase 3 with the property still off (review finding 6).
5. The companion extent hook (end of §4.2b), if the fork CR carries it: the pre-pass retired on the
   fork for every document. Independent of phases 2 and 3, with its own gate - the corpora and
   probes on the fork, hook against pre-pass, every master's extents equal except where the
   pre-pass's doctored copy differs from the real header - since it touches every PDF conversion
   (review finding 5).

## 7. Decisions for Jason

1. **How D1 is done** - **DECIDED 2026-10-05 (Jason): 4.2b.** Extra layout passes in docx4j (§4.2; the render time of the 34 or so affected
   documents doubles or more, and it works on Apache FOP), or a fork extension choosing page masters
   by the content a page starts with (§4.2b; one pass and exact, but on Apache FOP those documents
   keep today's margins). Recommendation (2026-10-05): **4.2b**, since docx4j renders on the fork by
   default (since 17.3.0) and the fork is maintained indefinitely in any case. If agreed, the fork
   session writes a fop/CR for it and phase 2 depends on that CR; the same CR can carry the
   companion hook that measures header and footer extents in FOP and retires the extent pre-pass
   on the fork (end of §4.2b), landed and gated separately as phase 5. The review (2026-10-05)
   agrees: it checked that all five call sites have the node in hand, and that nodes are already
   kept apart by line, so the odd/even case is covered too.
2. **Phase 3's restart and `w:titlePg`**: is `w:titlePg` at a continuous break in scope (a part
   starting at a page top shows its first-page header; one starting mid-page never does - P4)?
   And for the restart folio: under 4.2b, the extension's offset (step 6); under 4.2, literal folios
   written in pass 2 or a split sequence. Odd/even after the restart waits on P6 either way.
3. **Phase 5** (the extent hook in FOP): in the same fork CR as phase 2's hook, or later; and
   whether docx4j then drops the pre-pass on the fork for every document (§4.2b, end).

## 8. Risks

Either way:
- Interaction with the header and footer extent pre-pass (per-part masters to reach), `Paginate`,
  the two-pass NUMPAGES, the page-number-zero hook, folio parity, mirror margins (each master's
  mirrored twin) and the page masters' own first/odd/even/blank alternatives. Each needs a test.
- A merged sequence whose parts also differ in columns: the reference part's left and right
  margins stay as they are; only the vertical margins vary per page.

4.2 (passes):
- The page map oscillating between passes, where a part's margins move the page on which it takes
  over. Mitigation: at most three passes, keep the last, and log the oscillation; measure on the 34.
- Render time: 2x to 4x on the affected documents.

4.2b (fork extension; each needs a FOP-level test before docx4j gates it):
- The page cache: pages made while breaking carry the position-chosen master, and ids are
  registered against them; replacing them when areas are added must keep `IDTracker` right.
- Lists that start mid-page (span changes at part boundaries, §4.2b step 3): the continuing page
  keeps its owner. The common case for merged column changes, so it is tested first.
- The height cache keyed by index and part (step 4).
- Footnote-only pages inheriting the page before's owner, and floats, whose code reads the page
  height too.
- Balanced columns at span changes: `BalancingColumnBreakingAlgorithm` has its own height.
- Odd and even masters whose body heights differ (header extents of different heights): the height
  then depends on the folio as well as the owner.
- The last-page re-layout (`page-position="last"`): docx4j writes none, so out of scope at first.
- Upkeep: the change lives in `PageBreakingAlgorithm` and `PageProvider`, carried through each
  upstream merge (fop/CR-009's pattern).

## 9. Outside this CR

The page-break line (`pageBreakParagraphLine`), the other half of what blocks `tableTakesPageBreak`
(4994), is right by three probes but costs pages wherever docx4j's page is a few points fuller than
Word's. It is triaged on its own: re-gated in batch 52 (b106), five documents lose a page and one
reaches Word's count. Those five will be read for a common cause. Turning on `tableTakesPageBreak`
(this CR's former phase 4) moves to that triage, which needs this CR's phase 2 for 12802.

## 10. Effort (rough)

Phase 0: the probes, an hour, plus Jason's Word run. Phase 1: half a day. Phase 2: two to three
days by 4.2; by 4.2b, about two days in docx4j plus fop/CR-017 (the fork session's estimate after its
review: five to six days to a gated snapshot, and a day more for the restart step after P6). Phase 3: a day or two, plus P6 and its Word run.
Phase 5: a fork CR of its own (numbered when taken up), and about a day in docx4j with the gate.
