# The inspection oracle (CR-050 phase 0)

`phase0/expected-inspection.tsv` lists every finding an inspector (WD section 9; docx4j CR-050) must
make on the suite's four fixtures, one row a finding; `phase0/expected-inspection-status.tsv` gives
each fixture's status, what was not inspected, and what the inspector could not decide. The harness
(`ExtractionConformance`) compares an engine's findings with them as multisets of (kind, location), so
a finding missing, a finding unexpected, and two findings of one kind where one is expected all fail;
then each matched finding's fields. `expected-findings.tsv` keeps a (kind, selector) count for every
row, derived from this table, for an adapter that cannot report locations.

This file is the contract both tables follow. It was written for CR-050 phase 0 and states, for each
kind of the CR's section 2.3, what is one finding, where it is, what its excerpt is, and when it is in
an output. Where the CR left a choice open, the choice made here is marked **(phase 0)**; Jason
ratified them all on 19 October 2026, with D19, D17's list alone and D20 (CR-050 section 6). What
CR-050 phase 1 changed (D18's calibration; inserted content's membership; and, after docx4j-43's
review, SmartArt data, the marks of rows and cells, placeholder content, notes a header alone reaches,
a chunk's encoding and `javascript:` URIs) is marked **(phase 1)**, for Jason's ruling, or **phase 1's
calibration**, which Jason ratified on 20 October 2026 (D18); none of it moves a finding of the four
fixtures. What phase 2 changed (a note read
through its best reference, after the docx4j-portfolio session's question; a comment anchored in a note)
is marked **(phase 2)**, and moves none either.

## Files and how they are made

- `support/inspect_oracle.py` (Python 3, lxml, fontTools 4.66.1) applies these rules to the fixtures
  and writes both tables, the address column empty. It is a second reading of this contract,
  independent of docx4j's inspector (CR-050 phase 1) and of the ports'; its output was reviewed
  case by case before it was committed, and a change to it is reviewed the same way.
- `ExtractionInspection` (Java, docx4j-core-tests) fills the address column from docx4j's text model,
  derives the inspect rows of `expected-findings.tsv`, and checks every row against its fixture: the
  path resolves, the range lies in the run's text, the excerpt's length is the text's
  (`ExtractionConformanceTest`).
- Both tables are an inspector's **with no HTML reader**: it skips each HTML, XHTML or MHT chunk as
  `not_decoded` (CR-053 phase 3, WD 0.27), so with a reader on the class path their comparison
  pends. An inspector **with one** runs the chunk's import (WD 0.26's REQ-901) and is compared instead on
  `extraction-probe-html.docx` (`ExtractionHtmlCases`: four chunks, A to D) with
  `phase0/expected-inspection-html.tsv` and `expected-inspection-html-status.tsv`, read only under
  `html reader present`. Those rows are written by hand, not by the oracle, which has no reader: each
  cites its statements and the Word probe it rests on (`evidence`), and is checked against Word 365's
  save of the fixture (round 10, 21 October 2026: every row agrees). A row resting on a rule not yet
  in the WD would name the WD it waits for in `pending`, and be left out of the comparison on both
  sides; since WD 0.27 none does. The table adds `parsed` (compared), `statements`, `evidence` and
  `pending`. A chunk's
  `text.hidden` is one finding for its outermost hidden element with text, at the element's path in the
  tree jsoup builds (unprefixed, `/html[1]/body[1]/div[2]`), with no range; its excerpt is the text the
  import made, a block a line feed apart; its address the block that holds its text under
  `hidden: true`, from the same import. `ExtractionInspection` checks each path in the jsoup tree of the
  fixture's chunk.

After `ExtractionProbeBuilder` (into a scratch directory, its text files copied; never its rebuilt
fixtures, which are not byte-identical, `GOLDENS.md`), from the repository root:

```
python3 docx4j-core-tests/src/test/resources/text-extraction/support/inspect_oracle.py \
    docx4j-core-tests/src/test/resources/text-extraction/phase0
java -cp <test classes and dependencies> org.docx4j.textextraction.ExtractionInspection \
    docx4j-core-tests/src/test/resources/text-extraction/phase0
```

## Columns

`expected-inspection.tsv`: `fixture`, `case` (0 for a finding of the fixture as a whole), `kind`,
the location (`chain`, `part`, `path`, `start`, `end_path`, `end`), `address`, `visible`, `reveal`,
the group under six outputs (`plain`, `markdown`, `blocks`, `hidden`, `embedded`, `comments`),
`excerpt_length`, `excerpt_cut`, `selector`, `summary` (informative, not compared). The address is
the REQ-518/519 address of the block that holds the finding's selector in the output its reveal
names, as docx4j's text model gives it; empty for an unextractable finding, a property part, an
author, or a finding no block holds. The selector names what the finding is about: the marker token
in or around it, else its target, its name, its excerpt when short, or its part.

`expected-inspection-status.tsv`: `fixture`, `status` (`COMPLETE` or `PARTIAL`), `skipped` and
`undecided`, each a `; `-separated sorted list of `reason@location`, a location written
`chain|part|path`.

## Locations

A finding is a kind at a stored location (CR-050 section 2.4):

- **chain**: the parts of the containing packages, outermost first, `>` between them; empty for the
  document itself. An embedded package or an `altChunk` part is one level.
- **part**: the part name within the innermost package (`/word/document.xml`).
- **path**: the element, written with the canonical prefixes below whatever prefixes the document
  uses, every step with its 1-based position among its siblings of the same name:
  `/w:document[1]/w:body[1]/w:p[12]/w:r[2]`. Empty for a finding of a whole part, or of a part that
  is not XML.
- **start**, **end**: for a finding within a run, a half-open range of code points in the run's text;
  empty otherwise. **end_path**: for a finding over several runs or paragraphs, the last one's path
  (and `end` is within it); empty when the finding ends where it starts.

A run's text is the concatenation, in document order, of its `w:t`, `w:delText`, `w:instrText`
and `w:delInstrText` text and one code point for each `w:sym` (its `w:char`); a math run's, its
`m:t`. Ranges count code points, not UTF-16 units (U+E0041 is one).

Canonical prefixes: `w` (WordprocessingML main), `r` (relationships in a part), `rel` (a
relationships part's own namespace), `wp`, `a`, `pic`, `c`, `dgm`, `wps`, `wpg`, `mc`, `v`, `o`, `w10`,
`w14`, `w15`, `m`, `x` (SpreadsheetML main), `p` (PresentationML main), `cp`, `dc`, `dcterms`, `ep`
(extended properties), `op` (custom properties), `vt`, `pkg` (Flat OPC). `inspect_oracle.py`'s
`PREFIX` table gives each namespace.

## Text and excerpts

**Display text** is what a run shows: `w:t`, `w:delText`, `w:sym` and `m:t`, not a field's
instruction. An element's **text** is, for each paragraph within it in document order, the display
text of the runs whose nearest paragraph that is, the paragraphs joined by U+000A; for an element
inside a paragraph (an `mc` branch, a control's content), its runs' display text. Content in an `mc`
branch its `mc:AlternateContent` does not select (below) is left out of an element's text, except
the branch's own.

A finding's **excerpt** is: for a range, the text from `start` in the first run to `end` in the
last; for an element, its text; for an attribute (alt text, a docVar), its value; for a
relationship, its target; otherwise as the kind says, or none. `excerpt_length` is its length in code
points before the 200-code-point bound (D5), `excerpt_cut` whether that length exceeds 200.

## Identity

- Kinds are kept apart: a hidden, tiny run is two findings at one range.
- A run finding merges with the one before it when the two runs are adjacent among their paragraph's
  runs with display text, their kind is the same, and every attribute the finding reports is equal:
  the colour and background (`text.near_background`), the drawn size (`text.tiny`), the author and
  date (`revision.*`), the containers' requirements (below) and `visible`. A character kind reports no
  attribute, so a bidi override opened in one run and closed in the next is one finding from the first
  character to the last.
- A paragraph whose mark and every run with display text are hidden is one `paragraph.hidden`
  finding, and no `text.hidden` for its runs.
- A part is found once however many references it has; a relationship is one finding per
  relationship element.

## The branch read, and the outputs

**Selection** (REQ-302): the first `mc:Choice` all of whose `Requires` prefixes name namespaces docx4j
understands (`docx4j-core/src/main/resources/org/docx4j/jaxb/understood-namespaces.txt`), else the
`mc:Fallback`, else the first `mc:Choice`. `wpi` (ink) is not understood, so an ink `mc:Choice`'s
Fallback is read.

**Membership** (CR-050 section 2.3): a finding's containers give its requirements, and it is in an
output when the output meets them all:

| Container | Requirement |
|---|---|
| a header, a footer a section uses | `include: headers` / `include: footers` |
| a footnote or endnote | `notes: true` (default), and what reading its best reference needs **(phase 2)**: a reference in the main document, or in a header (footer) a section uses (`include: headers`, `include: footers`), needs what its own containers need (its revision's view, a text box, a TOC, a stale control's surface, `hidden: true` for a hidden run); one in an unselected branch reaches nothing. The best is the one with the fewest requirements beyond the defaults; then a live reference before an inserted one, an inserted before a deleted, a deleted before one deleted inside an insertion; then the main document's before a header's before a footer's; then the fewest requirements; then the first in document order. Two references of different requirements are approximated by the best's: under options that meet another's and not the best's, the text holds the note and its findings are counted out of the output |
| a comment with an anchor (a `w:commentReference` or `w:commentRangeStart` in the main document, a header or footer, or a note **(phase 2)**) | `comments: true` (or the markup view) |
| a glossary entry | `include: glossary` |
| a story no section or reference uses (a note an unused header alone refers to among them **(phase 1)**); an unselected branch | never |
| deleted or moved-from content | `view: original` (or markup); inside an insertion as well, `view: markup` only |
| inserted or moved-to content | not `view: original` (WD REQ-905; the defaults meet it, so no reveal names it) **(phase 1)** |
| a row deleted (inserted) by its `w:trPr/w:del` (`w:ins`), a cell by `w:cellDel` (`w:cellIns`), its runs in no revision | as deleted (inserted) content: Accept All removes the row, Reject All the inserted one **(phase 1)** |
| a control showing its placeholder (`w:showingPlcHdr`) | `placeholders: true` (default) **(phase 1)** |
| a TOC field's instruction and result (REQ-520) | `toc: true` |
| a bound control's stored surface | `bindings: surface` |
| a hidden run, paragraph, sheet or slide | `hidden: true` |
| a text box | `text_boxes: true` (default) |
| an embedded package | `embedded: true` |
| an `altChunk` | `altchunks: true` (default); an HTML, XHTML or MHT chunk also an HTML reader (CR-053, WD 0.27) |
| a picture's description (or name, where it is the description) | `alt_text: true` in plain text; always in Markdown and the block model |
| a field's instruction | `field_codes: true` |
| a chart's strings | `charts: true` (default) |
| SmartArt's data | `smartart: true` (default) **(phase 1)** |
| the properties | always in the block model; `metadata: true` in plain text and Markdown |

The six outputs are the defaults (accepted view, no importer) in plain text, Markdown and the block
model, and in plain text with `hidden: true`, with `embedded: true`, and with `comments: true`.

**reveal** is the plain-text instruction: `included` where the defaults meet every requirement,
`unextractable` where one is never, else `options: ` and the options needed, `; ` apart, in WD
section 10's order (`view: original; include: headers; hidden: true`).

**The group** under an output: `active` for the active kinds (`field.external`,
`settings.update_fields`, `relationship.external`, `part.macro`, `part.activex`, `altchunk.script`,
`altchunk.external_ref`); `info` for the inventory kinds (`altchunk`, `object.embedded`,
`font.embedded`, `ink`, `revision.author`, `rsids`, `people`, `part.unknown`,
and `hyperlink.web` **(phase 0: the CR's table has it info, which the group rule alone would make
omitted)**); for `properties`, `info` while in the output (labelled metadata) and `omitted` otherwise;
else `unseen` when in the output and `visible` is `no`, `differently` or `indeterminate`, `omitted`
when not in the output and the excerpt is not empty, and `info` otherwise.

## The kinds

Each row: what is one finding, its location, its excerpt (where it is not the element's text), and
`visible` where it is not the CR's.

| Kind | One finding | Location | Notes |
|---|---|---|---|
| `text.hidden`, `text.web_hidden` | a run with display text, `w:vanish` (`w:webHidden`) in effect | run, range: the run | Effective: docDefaults, then the paragraph style's chain, the table style's, the character style's, the run's own; the last that sets it wins **(phase 0: no toggle arithmetic)**. |
| `paragraph.hidden` | as Identity says | `w:p` | |
| `text.near_background` | a run with display text that is not all whitespace, its colour (not `auto`) within WCAG 1.5:1 of its background | run | Background: the highlight; else the run's shading; else the paragraph's (direct or style); else the cell's (direct, then table style); else a text box's solid fill; else the page colour (`w:background`); else white. On the page colour, `visible` is `differently` where the settings have `w:displayBackgroundShape` (Word 365 shows the colour then, measured 19 October 2026) and `indeterminate` otherwise (D15). A shading pattern other than `clear`/`solid`/`nil`, or a gradient, picture or pattern fill, is a background docx4j cannot determine: no finding, and `background.indeterminate@` in `undecided`. A fill of `auto` is no fill. |
| `text.tiny` | a run every non-space character of which is drawn under 2pt | run | The size is `w:szCs` for a character in the complex-script slot (the run's `w:cs` or `w:rtl` on, or the character in a complex script), `w:sz` otherwise; 20 half-points when neither is set. |
| `chars.zero_width`, `chars.tag`, `chars.bidi`, `chars.private_use` | a run whose display text holds characters of the kind | run, range: first such character to the last | The CR's ranges. A private-use character is excepted when its font (the `w:sym`'s `w:font`, else the run's for the character's slot) is on D17's list (the list alone: Word 365 did not draw an embedded symbol-encoded face, so a face's own mapping exempts nothing, D17 as revised). |
| `font.glyph_blank` | a run with characters its embedded face maps to a glyph with no outline (whitespace and categories Cf, Mn, Mc, Me excepted; a character the face does not map is not tested) | run, range | The face: the font for the character's slot, its style from the run's bold and italic; only faces the package embeds. |
| `font.glyph_mismatch` | a run with a letter or digit one of D18's tests finds drawn as another | run, range | The tests consider glyphs with an outline only, and a letter or digit that is no compatibility character (one NFKC changes: a presentation form, a ligature, a width variant, a superscript, whose glyph is a variant by design). "Drawn as" another character `d` is evidence only where `d` is a letter or digit, no compatibility character, and not **the same letter**: two characters are the same letter when their bases are equal, either has no base (a mark-like letter), or their scripts differ (the script read from the character's Unicode name's first word **(phase 0)**). A character's base is its NFKD with marks (Mn, Mc, Me), modifier letters (Lm) and spaces dropped, each character upper- then lower-cased (one-character mappings), and ı, ȷ, ĸ, ð and đ folded to i, j, k, d and d. (1) another character maps to the same glyph; (2) a letter's advance is 0, or over 1.5 em, a modifier letter excepted and a titlecase letter from the second; (3) post format 2 names the glyph for a single other character (fontTools' `agl.toUnicode`: the full AGL, `uniXXXX`, `uXXXX[XX]`, a suffix after a full stop stripped); (4) a single (type 1) or multiple (type 2) substitution, directly in a feature Word applies, gives a glyph the cmap gives another character: `ccmp`, `locl` and `rlig` always **(phase 0, until the Word check measures it)**, `liga`, `clig`, `hlig`, `dlig` as `w14:ligatures` says, `calt` under `w14:cntxtAlts`. Ligature and contextual lookups are not tested. Fonts on D17's list are excepted. The base, compatibility and titlecase rules are **phase 1's calibration** (CR-050 section 5): as first written, the tests found 27 runs in four real documents drawn in faces as their foundries made them (an all-capitals face draws a and A alike), and as calibrated none. |
| `font.embedded` | an embedded face some run's text is drawn in | the font part | |
| `paragraph.collapsed` | a paragraph with an outline level below 9 and `w15:collapsed` (direct, else its style's), not itself in another's collapsed body, with a body | first body block to the last (`end_path`) | The body: the body-level paragraphs and tables after it up to the next paragraph whose outline level is the same or higher. |
| `shape.zero_size` | a `wp:inline`/`wp:anchor` with an extent 0 in a dimension; a VML shape whose style width or height is 0 | the drawing (`wp:inline`/`wp:anchor`), or the VML shape | Excerpt: the shape's text (its `w:txbxContent`s, or a `v:textpath` string). In the output, under `text_boxes`, only where it holds text; else never. |
| `shape.off_page` | an anchored shape wholly outside the page for every position its anchor can have (D4) | as above | Page: the section after the paragraph; a header's or footer's sections (one size, or undecided). Anchors: `page` exact; `margin` from `w:pgMar`; a `column` in a one-column section is the left margin **(phase 0, refining D4)**; `paragraph`, `line`, `character` and a column of several anywhere in the text area (between the margins). `wp:align` is on the page. VML: `position:absolute` with `margin-left`/`margin-top`, `mso-position-*-relative` defaulting to column and paragraph, an `mso-position-*` alignment on the page. Neither provably off nor provably on: `position.indeterminate@`; no section: `position.no_section@`. |
| `shape.hidden` | `wp:docPr/@hidden` on an anchored drawing; VML `visibility:hidden` | as above | Word 365 shows an inline drawing whose `wp:docPr` is hidden (measured 19 October 2026, case 59): no finding **(phase 0)**. |
| `smartart.data` | each point the text model reads (of type `node` or `asst`, from the `doc` point along `parOf` connections, by `@srcOrd`) with a line no paragraph of the diagram's drawing part holds, where the data part names one (`dsp:dataModelExt/@relId`, a relationship of the story part) | the `dgm:pt` in the data part | A line: an `a:p`'s runs' and fields' `a:t`, an `a:br` a line feed; empty lines are none. Each paragraph of the drawing part's `dsp:txBody`s holds one data line. Excerpt: the point's lines the drawing lacks, LF apart. No drawing part, no finding: Word lays the diagram out from the data. No fixture has one: case 35 has no drawing part, Word's save of the probe shows every line, and `InspectTest` makes that save's drawing stale **(phase 1)**. |
| `revision.deleted`, `revision.moved_from` | a run with display text in `w:del` (`w:moveFrom`) | run | A deleted paragraph mark or table-row mark alone is none. |
| `revision.author` | each author of a revision mark (`w:ins`, `w:del`, `w:moveFrom`, `w:moveTo`, `w:*PrChange`, `w:cell*`, `w:numberingChange`) | the first mark by that author | Excerpt: the author. Reveal: `view: markup; markup_detail: author_date`. No address. |
| `mce.branch` | an unselected `mc:Choice` or `mc:Fallback` not itself in another unselected branch | the branch | Every other kind is found inside, never in an output. |
| `story.unused` | a header or footer part no `w:sectPr` uses; a note (`w:type` absent or `normal`) no reference reaches, deleted or not; a comment with no `w:commentReference` or range | the part, or the `w:footnote`/`w:endnote`/`w:comment` | Case 27's note, whose only reference is deleted, is `note`, in the output under `view: original` **(phase 0, departing from the CR's table)**. |
| `note` | a referenced note | `w:footnote`/`w:endnote` | |
| `comment` | an anchored comment | `w:comment` | |
| `header_footer` | a header or footer part a section uses | the part | |
| `watermark` | a VML shape whose id starts `PowerPlusWaterMarkObject` | the shape | Excerpt: its `v:textpath` string. |
| `glossary` | a `w:docPart` | it | Excerpt: its `w:docPartBody`'s text. |
| `sdt.stored_surface` | a bound control (`w:dataBinding` resolving in its custom XML part) whose content's text is not the bound value | `w:sdtContent` | `visible` `no`: Word 365 shows the bound value on open (measured, case 67) **(phase 0: the CR had "yes until Word refreshes")**. |
| `sdt.placeholder` | a control with `w:showingPlcHdr` | `w:sdtContent` | |
| `field.code` | every field: a complex field's instruction runs, every one between `begin` and `separate` (a nested field's too, and it is its own finding); a `w:fldSimple` | first instruction run to the last, range to the end of the last; or the `w:fldSimple` | Excerpt: the instruction as stored. |
| `field.external` | a field whose keyword (case-insensitive) is INCLUDETEXT, INCLUDEPICTURE, LINK, IMPORT, DDE or DDEAUTO, or a HYPERLINK whose target is not `http`, `https` or `mailto`; `\l` is a bookmark, neither | as `field.code` | Arguments quoted (`\\` and `\"` unescaped) or bare. |
| `hyperlink.web` | an external hyperlink relationship, or a HYPERLINK field, to `http`, `https` or `mailto` | the relationship element, or the field | Excerpt: the target (so a field's excerpt is not its range's text). |
| `relationship.external` | every other relationship with `TargetMode="External"`, in any relationships part | the `rel:Relationship` in its part | |
| `alt_text.description`, `alt_text.title`, `alt_text.name` | a non-empty `wp:docPr` `descr`, `title`, `name`; a VML shape's `alt`, an `v:imagedata`'s `o:title` | the `wp:docPr` (or VML element) | A description is extractable for a picture (`pic:pic`) not decorative; a name where it is that picture's description (`descr` empty); a title never. A name is a finding only where it is that description, or where it is not a default name: one matching `[^0-9]+ [0-9]+` in full ("Text Box 3", "Picture 7"), D19 (b). |
| `chart.cached` | a chart a drawing shows | the chart part | Excerpt: its `c:strCache` values and `a:t` text, LF apart. |
| `doc_vars`, `rsids`, `settings.update_fields` | `w:docVars`; `w:rsids`; `w:updateFields` on | the element in the settings part | Excerpts: the variables' values; none; none. |
| `custom_xml` | a custom XML part (`application/xml`, a `customXml` relationship) | the part | Excerpt: its text nodes, trimmed, LF apart. |
| `properties` | a core, app or custom properties part with a value | the part | Excerpt as `custom_xml`. In an embedded package, never in an output. |
| `people` | `people.xml` | the part | Excerpt: the authors. |
| `part.macro`, `part.activex`, `ink`, `part.unknown` | a part of type `vbaProject`, `activeX+xml`, `inkml+xml`, or of none of the known families (`application/vnd.openxmlformats-`, `application/vnd.ms-`, `image/`, `audio/`, `video/`, `text/`, `font/`, `message/rfc822`, `application/xml`, `application/xhtml+xml` (WD 0.27), `application/rtf`, `application/inkml+xml`, `application/x-font`, `application/pdf`, `application/vnd.oasis.opendocument.`) | the part | |
| `altchunk` | an `altChunk` part | the part | Its content, one level deep: a docx or Flat OPC chunk's stories (chain: the chunk); an HTML, XHTML or MHT chunk scanned, read in its encoding (a byte order mark; UTF-16 by its first `<`; an XML declaration's `encoding` or a `meta` element's `charset` in its first 1,024 bytes; else UTF-8; an MHT part by its `charset` first) **(phase 1)**, and, the oracle having no HTML reader, then `not_decoded` (CR-053 phase 3, WD 0.27); plain text read; any other type `not_decoded`. An inspector with an HTML reader runs the chunk's import (WD 0.26's REQ-901) and finds what `display: none` hides as `text.hidden` instead: `expected-inspection-html.tsv`'s. |
| `altchunk.script`, `altchunk.external_ref` | a chunk holding a `<script>`, an `on*` handler, or a `javascript:` URI in a `src`, `href` or `background` (tabs, line breaks and leading spaces and controls ignored, as a browser reads it) **(phase 1)**; a chunk holding a `src`, `href` or `background` that is not a fragment or a `data:`, `cid:`, `mailto:` or `javascript:` URI | the part | One finding a chunk each. |
| `object.embedded` | a part an `o:OLEObject` of `Type="Embed"` refers to | the part | Its content one level deep: a docx's stories, a workbook's and a presentation's kinds. |
| `embedded.sheet_hidden`, `embedded.sheet_very_hidden` | a `x:sheet` of state `hidden`, `veryHidden` | the `x:sheet` in the workbook part | Excerpt: its cells, tab apart, rows LF apart. In the output under `embedded` and `hidden`. |
| `embedded.row_hidden`, `embedded.column_hidden` | a `x:row` or `x:col` with `hidden` on | it, in the worksheet part | Under `embedded` (and `hidden` in a hidden sheet). |
| `embedded.slide_hidden`, `embedded.notes` | a slide with `show="0"`; a notes slide with text (placeholders for the slide image, number, header, footer and date excepted) | the `p:sld`, the `p:notes` | Notes are never in an output (REQ-512). |

## Status

`PARTIAL` when anything is in `skipped`: content beyond one level (`depth@`: a chunk or embedded
package inside an embedded package or a chunk), and a chunk of a type the inspector does not decode
(`not_decoded@`: RTF, and an HTML, XHTML or MHT chunk without an HTML reader, CR-053 phase 3, pending
WD 0.27). An ActiveX control's binary, an OLE binary other than a plain-text package, a
VBA project and a part of an unknown type are reported by their kinds, and their content is a
limitation, not a skip **(phase 0)**. `undecided` lists the occurrences a test could not settle
(`position.indeterminate`, `position.no_section`, `background.indeterminate`); they are not findings,
and do not make the report partial.

## What the fixtures do not hold

`smartart.data` and `rsids` have no row: see their notes above (Word writes `w:rsids` on every save;
a fixture with one is phase 1's to build in its test). Phase 1's `InspectTest` builds both, and the
marks of rows and cells, placeholder content and notes a header alone reaches, which the fixtures hold
none of either **(phase 1)**. There is no `relationship.broken` kind (D20):
docx4j's loader refuses a package whose relationship names a missing part, so no package the
inspector is given has one.
