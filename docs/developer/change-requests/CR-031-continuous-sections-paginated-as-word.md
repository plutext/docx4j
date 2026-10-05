# CR-031: continuous sections paginated as Word paginates them - a page's vertical margins from the section that owns it, and no page for an empty block at a sequence's end

Status: PROPOSED 2026-10-05 (Jason, after batch 52 part 2: "fix the older defect and turn them
on?" - "yes please" to writing this CR). Phase 0 (Word probes) is next.

## 0. Why now

Batch 52 part 2 (CR-001 fidelity work, `../docx4j-portfolio/docs/layout_fidelity_plan.md`) found
two rules that Word's PDFs bear out but that cost class 2 documents when switched on, because each
leans on how docx4j paginates continuous sections. Both were committed off by default:

| property | rule | blocked by |
|---|---|---|
| `docx4j.convert.out.fo.wordLayout.tableTakesPageBreak` (ed3d7b268) | a table after a break-only paragraph starts at the page top | 12802 1.0000 -> 0.72: a continuous section's top margin (D1); 4994 a page short: the page-break line (outside this CR) |
| `docx4j.convert.out.fo.wordLayout.restartParityBlankPage` (b3c39ed0c) | with odd and even headers, a nextPage restart repeating the parity gets a blank page | 9539 a page short -> two over: an extra page docx4j gives its first sequence (D2) |

This CR fixes D1 and D2, then turns those two rules on. The page-break line
(`pageBreakParagraphLine`, the other half of 4994) is a density question, not a section one; it is
triaged separately (§9).

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
read from the margins here, not traced; phase 0 measures it directly.) (Page 2's 53.8 is the line the break-only paragraph
kept above a table, which `tableTakesPageBreak` removes - to 41.1, against Word's 43.1 - and which is
how that rule exposed this.) The hypothesis, to be confirmed in phase 0: **a page takes the top and
bottom margins, and the header and footer distances, of the section that owns its first line.**
Whether its header and footer *text* follow the same section is not measured (D3).

**D2. An empty block at the end of a page-sequence starts a page of its own.** Corpus document
9539: its first page-sequence (A3, sections 0 to 2, merged) ends with "(Queensland Studies
Authority Template, 2012)" at y=758.8..773.1, and then an empty preserved block (10pt, 11.5pt line)
which does not fit above the 782.1pt foot - so FOP starts a second A3 page holding only the running
header and footer. Word's first page ends 4pt lower (767.0..778.3), with 3.8pt left, and has no
second A3 page. The paragraph carrying section 2's break is empty (style `ACversionline`, no runs),
so `rendersNothing` should have dropped it; which paragraph the empty block is, and why it was kept,
is phase 1's first job. That same extra page is what shifts 9539's folio parity under
`restartParityBlankPage`.

**D3 (to be measured).** On a page whose first line belongs to a later continuous section: whose
header and footer text, and whose `w:titlePg` first-page header? And where does a continuous
section's `w:pgNumType/@w:start` take effect - the page the section starts on, or the next? (9539's
sections 0 and 2 both restart at 1; docx4j keeps the first restart of a merged run and loses the
second.)

## 3. Corpus facts (the four corpora on b110, 2026-10-05)

- **34 documents** change the top or bottom `w:pgMar` at a continuous section break; 26 are class 2.
- **9 are off Word's page count**: 12317 (Word 2, docx4j 1), 5507 (4, 3), 2600 (15, 13), 7235 (15, 16),
  1137 (15, 16), 11741 (56, 54), 719 (29, 28), 9539 (22, 21), 11256 (25, 27).
- How many of those nine D1 explains is not known until it is fixed: several also carry other
  differences (7235's table grid was one, fixed in 7a12ed272).
- D2's shape - an empty block ending a page-sequence pushed onto a page of its own - is in 9539.
  Three other documents end on a header-only page (8460, 5320, 11092), but there the page before
  ends 12 to 70pt lower in docx4j than in Word, so they are density, not D2.

## 4. Design

### 4.1 D2: no page for an empty block at a sequence's end

Trace 9539's empty block to the paragraph it comes from, then make the rule `rendersNothing` states
hold for it: an empty block which ends a page-sequence's flow, carries no break, and is not
closing a vertically aligned section is removed (or never written). A block with something on it,
or one in a vertically aligned section (`retainSpaceAfterInAlignedFlow` notes Word counts it
there), is left. Small; phase 1.

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
- *A fork extension* (region margins switched by a marker). It changes FOP's page model, and no
  upstream would take it. Rejected unless 4.2 proves unworkable.

### 4.3 D3: header and footer text, first-page header, restart

To be designed once phase 0 has answered. If a page's header follows the part that owns it, 4.2's
per-part masters carry per-part `fo:region-before`/`-after` names and static content, which
`LayoutMasterSetBuilder` already writes per section. A continuous restart lands where phase 0
says, through the same page map.

## 5. Phase 0: Word probes (for one Word run)

Mode 15, with a mode 14 twin of P1. Exact 24pt lines so baselines read cleanly; each section's
header and footer name the section and print the folio.

- **P1 `continuous-margins-vertical`**: S1 top/bottom 72/72pt, header/footer distances 36/36, a page
  and a half; S2 continuous, top/bottom 144/36, distances 18/18, three pages; S3 continuous, back
  to 72/72, starting mid-page. Read: each page's first and last baselines, its header and footer
  text and their y.
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

## 6. Phases

0. Probes (§5), Word run, readings into §2.
1. D2 (§4.1). Gate: 9539 loses its extra page; nothing else moves.
2. D1 (§4.2), the per-page masters and the passes. Gate: 12802's page tops at Word's, the 34
   documents read one by one.
3. D3 (§4.3), as phase 0 decides.
4. Turn on `restartParityBlankPage` and `tableTakesPageBreak`, gated, and record why each was off.

## 7. Decisions for Jason

1. **Extra layout passes** (§4.2) for documents whose merged continuous sections differ in their
   vertical margins: the render time of those documents doubles or more. The alternative is a fork
   extension (§4.2, rejected above).
2. **Scope of phase 3**, once phase 0 has read D3.

## 8. Risks

- The page map oscillating between passes, where a part's margins move the page on which it takes
  over. Mitigation: at most three passes, keep the last, and log the oscillation; measure on the 34.
- Interaction with the header and footer extent pre-pass, `Paginate`, the two-pass NUMPAGES, the
  page-number-zero hook, folio parity, mirror margins (each master's mirrored twin) and the page
  masters' own first/odd/even/blank alternatives. Each needs a test.
- A merged sequence whose parts also differ in columns: the reference part's left and right
  margins stay as they are; only the vertical margins vary per page.

## 9. Outside this CR

The page-break line (`pageBreakParagraphLine`), the other half of what blocks `tableTakesPageBreak`
(4994), is right by three probes but costs pages wherever docx4j's page is a few points fuller than
Word's. It is triaged on its own: re-gated in batch 52 (b106), five documents lose a page and one
reaches Word's count. Those five will be read for a common cause.

## 10. Effort (rough)

Phase 0: the probes, an hour, plus Jason's Word run. Phase 1: half a day. Phase 2: two to three
days. Phase 3: a day or two, depending on phase 0. Phase 4: half a day with the gates.
