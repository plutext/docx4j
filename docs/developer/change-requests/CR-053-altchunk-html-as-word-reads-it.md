# CR-053: HTML, XHTML and MHT altChunks, written as Word opens them and read as Word reads them

Status: DONE 2026-10-21, all five phases (0 to 4; 17.3.4). ACCEPTED 2026-10-20 (Jason: "accept the
recommendations and commit the CRs"), D1 to D10 as recommended. Phase 0 done (Jason: "start phase
0"): tests, design checks and Word probes, rounds 5 to 7 (§10). D9 is (iii), D2 stands, D11 is
decided (50,000) and D4's rewrite (§10.7) is approved. Phase 1 built (Jason: "start CR-053 phase 1";
§11): the strip, the UTF-8 binding and D6, its five calls confirmed; Word opens what it writes
(`p1-9`'s repair is the binding template's own Flat OPC chunk). Phase 2 built (on ImportXHTML's
notice, Jason's standing instruction; §12), against ImportXHTML's CR-002 phases 1 to 3 (468a760);
Word opens its conversions cleanly, with the hidden text of its own import (§12.5). Phase 3 built
(Jason's ruling on REQ-901, 21 October: "the inspector can run the same HTML import that text
extraction runs", WD 0.26; §13); its suite rows and phase 2's are WD 0.27's (approved 21 October),
and Word's save of its fixture agrees with every row (§13.5). WD 0.28's rules (rounds 11 and 12) in
198d015a3 (§13.7, §13.8). Phase 4 (Jason: "start CR-053 phase 4"; §14): the hand-offs sent and
acknowledged. Proposed 2026-10-20 (Jason: "lets start the altchunk work"), reviewed the same day by
docx4j-64 (§9). It gathers his ruling on adding a chunk (strip the XML declaration, 2026-10-20) and
what the text extraction WD 0.23 to 0.25 asks of docx4j (portfolio 6cdb966, 016963e, 1865f10).
Drafted by Claude Opus 5.5 (session docx4j-25), with §1 measured first. Phases in §5; decisions in
§6.

## 0. Summary

Word 365 refuses an HTML or XHTML chunk that begins with an XML declaration, whatever its declared
content type, and repairs the document. docx4j writes what it is given, so a caller's declaration, or
a bound XHTML value that has one, produces a file Word repairs. docx4j is to strip the declaration
when it adds such a chunk (Jason's ruling).

On the reading side, Word reads all three kinds as HTML. It hides what `display: none` hides, inline
or through a rule in the chunk's own `style` elements, and nothing else. It reads an XHTML chunk
that is not well-formed as HTML. docx4j's text model does neither: an HTML chunk's `display: none`
text comes out as ordinary text, and an XHTML or MHT chunk read through docx4j-ImportXHTML loses that
text entirely, so `hidden: true` cannot bring it back. The inspector reports no hidden text in a
chunk at all. Without an HTML parser, the text model strips an HTML chunk's tags and so emits text Word
hides; the WD now says such a chunk is not expanded.

The proposal (§4, D3): all three kinds are read through docx4j-ImportXHTML. docx4j parses each chunk
itself, as the WD says: a well-formed XHTML chunk as XML, everything else with jsoup's WHATWG parser.
It hands the importer the tree. The importer learns to emit what `display: none` hides as hidden
text, as Word's own import does, and to say which element each hidden piece came from. The inspector
runs the same import. That needs REQ-901 relaxed (§7). `convertAltChunks` gains HTML chunks on the way.

## 1. What docx4j does today (measured 2026-10-20 on VERSION_17_3_4 at 86f8f3e93)

Scratch drivers against the installed 17.3.4-SNAPSHOT, with jsoup 1.22.1, without and with
docx4j-ImportXHTML (17.1.1-SNAPSHOT, its target jar, with openhtmltopdf 1.1.31 and mime4j). Each
chunk was added with `addAltChunk`. Its body was `<p>VISIBLE_A</p><p style='display:none'>INLINE_NONE</p>
<div class='h'><p>CLASS_NONE</p></div><p>VISIBLE_<span style='display:none'>SPAN_NONE</span>B</p>`, and
its `<style>` held `.h{display:none}`.

| Case | Default extraction | `hidden: true` | Inspector |
|---|---|---|---|
| 1. `text/html` (jsoup; with or without the importer) | **all four hidden texts as ordinary text** | the same, none flagged hidden | `altchunk` only |
| 2. `application/xhtml+xml`, no importer | the same as 1 (read by jsoup) | the same | `altchunk` only |
| 3. `application/xhtml+xml`, importer present | `VISIBLE_A`, `VISIBLE_B` | **the same: the hidden text is gone** | `altchunk` only |
| 4. as 3, with `<?xml version='1.0' encoding='UTF-8'?>` | the same as 3 (the importer reads it) | the same | `altchunk` only |
| 5. XHTML not well-formed (unclosed `<p>`, stray `</div>`), importer present | the importer fails; jsoup reads it, repairs as Word does, its `display:none` text as ordinary text | the same | `altchunk` only, no `parsed` |
| 6. Any chunk added with a leading declaration | written byte for byte: Word repairs the document | | |

Also found:

- The importer drops a `display: none` element because openhtmltopdf builds no box for it. Nothing in
  docx4j-ImportXHTML handles `display: none` (read 2026-10-20 at c8dc938). It reads `display` only to
  classify table elements (`XHTMLImporterImpl`:1124), and tests `none` only for a border style
  (`TableHelper`:485).
- openhtmltopdf's default stylesheet (`XhtmlNamespaceHandler.css`, 1.1.31) hides only `head`,
  `script` and a `select`'s options. So `template`, `noscript`, the `hidden` attribute and `details`
  show through the importer, as they do in Word.
- `convertMHT` gives the MHT's first `text/html` part to the XML parser. An MHT's HTML that is not
  well-formed XML, which is the usual case, therefore fails. `convert(Node)` takes a DOM as it is,
  with no parse of its own.
- The text model decodes an HTML chunk with `Embedded.decode`: a byte order mark, else UTF-8, else
  windows-1252. It ignores an XML declaration's `encoding` and a `meta` `charset`. The inspector decodes
  the same bytes by the WD's Chunks rule (`InspectPayloads.decode`): the BOM, UTF-16 sniffing, the
  declared encoding within 1,024 bytes, else UTF-8. So the two can read one chunk differently.
- Without jsoup, `HtmlText.stripped` strips the tags. It drops `head`, `script`, `style`, `template` and
  `noscript`, so it also drops `template` and `noscript` text, which Word shows.
- The OpenDoPE XHTML binding (`BindingTraverserXSLT`:648) writes the bound value as a `text/html`
  chunk with `r.getBytes()`, the platform charset, so it is not UTF-8 on Java 11 or 17 under Windows.
  A bound value that begins with a declaration gets Word's repair (case 6).
- The memory note "a loaded `AlternativeFormatInputPart` has no `AltChunkType`" (CR-051 §11.2) did not
  reproduce. Every type built by `addAltChunk`, and the CR-051 test's own part, loads with its type,
  in both load modes. The type comes from the part name's extension, though, and a chunk whose
  extension docx4j does not know, or that disagrees with the declared content type, gets the wrong
  type or none: `convertAltChunks` then skips it, or treats it as the wrong kind.

### 1a. What each reader gives the text model (measured 2026-10-20)

Each reader was given one XHTML chunk holding:
- an `h2`;
- an `ol` with a nested `ol type='a'` and an `li value='7'`;
- an `ol start='4'` with `list-style-type: lower-roman`;
- a nested `ul`;
- `b`, `em`, `a` and `code` in one paragraph;
- a `blockquote` and an `hr`;
- a table with a `caption`, a `thead`, a `rowspan='2'` cell and a `colspan='2'` cell.

The driver is `Structure.java`, run with `list_labels: true`; `IxRaw.java` printed the importer's own
OpenXML.

| | `HtmlText` (jsoup) | ImportXHTML 17.1.1-SNAPSHOT |
|---|---|---|
| Heading | a plain paragraph | a bold run, no outline level |
| Lists | plain paragraphs, no level or label | levels, formats, `start`; `type='a'` and `value='7'` ignored (`1.`, `2.`, `3.`) |
| Inline | plain text | bold, italic, link, monospace |
| Quote, rule | plain paragraph; nothing | plain paragraph; an empty paragraph, not marked as a rule |
| Table, no caption | header row, column span, a row-span start, **no continuation cell**: the second row is a column short | header row, column span, the row span's continuation cell |
| Table with a caption | as above | **a `w:tbl` directly inside a `w:tbl`** (schema-invalid); the text model reads the caption row and loses the rest of the table |

## 2. What Word does (Jason's probes, 20 October 2026, paper §7.10)

- A chunk beginning `<?xml ...?>` is refused and repaired ("unreadable content"), whether it is
  declared `application/xhtml+xml` or `text/html`; the same chunk without the declaration opens
  (`altchunk-fun/xhtml-bisect/`, 13 variants). A Flat OPC chunk begins with one and opens; docx4j's
  binding adds one to Flat OPC on purpose (`BindingTraverserXSLT`:2062).
- HTML, XHTML and MHT alike: inline and class `display: none` is hidden text (the class becomes a
  style with `w:vanish`); `visibility: hidden`, the `hidden` attribute, `opacity: 0`, off-screen,
  zero height, `noscript`, `template` and closed `details` all show; the head, comments, attributes
  and input values are dropped.
- An XHTML chunk that is not well-formed opens cleanly and is read as HTML, repairs included
  (`xhtml-14`, and `xhtml-15` as the `text/html` control).
- Not measured: selectors other than a class; `@media` rules; `display: none` on a table row or
  cell; how Word decodes a chunk with no BOM, declaration or `meta` charset; whether a BOM before the
  declaration, or white space before it, changes the refusal. All measured in phase 0 (§10.6).

## 3. What the WD says (0.25, portfolio 1865f10)

- **REQ-513**: an HTML chunk is tidied by an HTML parser that implements WHATWG parsing, where one is
  present, and read as blocks; an XHTML or MHT chunk is read through an XHTML importer where one is
  present. `display: none`, inline or by a rule in the chunk's own `style` elements, is hidden text
  (REQ-507): out by default, in under `hidden: true`. No other CSS property hides anything, and no
  user-agent hiding does either. Whatever is not expanded raises `altchunk.not_expanded`; with no
  HTML parser, an HTML chunk is not expanded. A NOTE records the round-4 result: Word reads an XHTML
  chunk that is not well-formed as HTML.
- **REQ-901**: a location in an HTML or MHT chunk is the path in the WHATWG-parsed tree, repairs
  included. In a well-formed XHTML chunk it is the path in the XML tree; an XHTML chunk that is not
  well-formed is parsed as HTML. Elements are unprefixed: `/html[1]/body[1]/div[2]`.
- **REQ-902**: `text.hidden` covers an element with text that `display: none` hides in an HTML, XHTML
  or MHT chunk, one finding for the outermost such element. The `altchunk` finding gains `parsed`:
  `xml`, `html`, or absent where the chunk was not decoded. Chunks: the encoding rule above. Without
  an HTML parser, the inspector still finds scripts and references, and lists the chunk as skipped
  (`not_decoded`, REQ-907).

## 4. Proposal

**Writing.** `addAltChunk` and the OpenDoPE XHTML binding remove a leading XML declaration from an `Html` or `Xhtml` chunk, after any
BOM, and keep the content's encoding (D2). An `Mht` chunk begins with MIME headers, and a Flat OPC
(`Xml`) chunk keeps its declaration. Bytes a caller puts into a part themselves, and a loaded chunk,
are left as they are: the inspector and the anonymiser see what is stored. The binding encodes the bound value as
UTF-8. A loaded chunk's `AltChunkType` comes from its declared content type, with the extension as
the fallback (D6). The sites are listed under D1.

The anonymiser is not affected by any of this. `MarkupScrubber` replaces a `w:altChunk` with a marker
paragraph (`MarkupScrubber`:386), and `PartsAnalyzer` removes or keeps the part whole. Neither reads
a chunk's content.

**Reading (text model).** docx4j-ImportXHTML reads all three kinds (D3); docx4j parses them first
and hands over the tree.
- The bytes are decoded by the WD's Chunks rule (`InspectPayloads.decode`, the inspector's).
- An XHTML chunk that is well-formed XML is parsed as XML (which parser, and what a DOCTYPE means, is
  D9).
- An HTML chunk, an XHTML chunk that is not well-formed, and an MHT chunk's first `text/html` part
  (read by `InspectPayloads.mhtHtml`, in its MIME charset) are parsed by jsoup, the WHATWG algorithm,
  through `JsoupHtml.parse` (`Jsoup.parse`, then `W3CDom.fromJsoup`).
- Before the import, the tree is made safe to lay out (§4b): `colspan` clamped to 1,000 and `rowspan`
  to 65,534, as WHATWG clamps them, and the nesting depth capped.
- The tree goes to the importer through `convert(Node)`, with its new `display: none` option on and
  never fetching. The importer therefore never parses a chunk, and the tree it lays out is the tree
  whose paths the inspector reports.
- What `display: none` hides comes back as hidden runs, and a block wholly hidden as a hidden
  paragraph (D5), and REQ-507's filter applies as it does to a document's own hidden text.
- Each expanded chunk records which parser read it (`xml` or `html`).
- Without the importer, a chunk of these kinds is not expanded and raises `altchunk.not_expanded`.
  Without jsoup, neither is an HTML or MHT chunk, nor an XHTML chunk that is not well-formed.
  `HtmlText` goes; `JsoupHtml` stays as the parse step (D7).
- The import is guarded as §4b says, in extraction as in inspection: an `Error` from it, a stack
  overflow included, skips the chunk with `altchunk.not_expanded`.

**Inspecting.** The inspector runs the same import (REQ-901 relaxed, §7), guarded as §4b says.
REQ-907's limits are checked before the import: the payload's bytes, and the element count of the tree
the importer will lay out, after jsoup's repairs, not of the source text.
- For each outermost `display: none` element that holds text, the importer's hook names the element.
  The finding's location is the chunk part, then that element's path in the parsed tree. Its address
  is the block that holds the element's text under `hidden: true`, from the same import. The
  inspector numbers the chunk's blocks as the `hidden: true` extraction does, hidden paragraphs
  included; a hidden `span`'s finding takes its paragraph's block, as `text.hidden` does in the
  document itself. Its reveal is `options: hidden: true`.
- `display` is not inherited, so an element inside a hidden one is not itself `display: none` unless
  a rule makes it so. "One finding for the outermost element" (REQ-902) is the hook's dedupe by marked
  ancestor.
- The `altchunk` finding carries `parsed`.
- Without the importer or the parser a chunk needs, the chunk is skipped as `not_decoded`. Its scripts
  and references are still found by the tag scanner, as they are today.
- A chunk the guard stops, an `Error` from the import included, is skipped as `unreadable` (or as the
  limit it hit).

**`convertAltChunks`.** HTML chunks, skipped today, are parsed by jsoup and converted through the
importer. An XHTML chunk that is not well-formed falls back to jsoup instead of being skipped. Whether
`convertAltChunks` turns the `display: none` option on is D8. Nothing in docx4j's main code calls
`convertAltChunks` except the sample `AltChunkXHTMLRoundTrip`; the exporters do not. So D8 changes
the output only of a caller who converts chunks explicitly, and not PDF or HTML output.

### 4a. What docx4j-ImportXHTML is asked for (its own repository; released as 17.3.4)

A release is due in any case: c8dc938, `setExternalResourcesAllowed`, is unreleased. The asks:
1. **`display: none` as hidden text,** behind an option that is off by default. openhtmltopdf builds no
   box for such an element, so a pre-pass is needed:
   - find, by openhtmltopdf's own cascade, each element whose `display` is `none` by an author rule
     or a `style` attribute (not by the default stylesheet's `head` and `script`). In openhtmltopdf
     1.1.31: `StyleReference.getCascadedStyle(Element, boolean)`, whose `PropertyDeclaration` for
     `display` gives `getOrigin()` (`StylesheetInfo.AUTHOR`, an inline style included) and
     `isImportant()`;
   - give it back its default `display`, taken from a second cascade over the default stylesheet
     alone rather than from a hand-made table, as `display:<default> !important` in its `style`
     attribute (inline `!important` beats a stylesheet's), mark it, and set the document again;
   - emit its content with `w:vanish`: on the runs, and on the paragraph mark of a paragraph wholly
     inside it.
   Re-showing changes geometry, which is wanted for the text but has to be checked: a `td` re-shown
   changes the grid, and an `li` re-shown as a `list-item` takes a number (phase 0's probe includes
   both). openhtmltopdf evaluates `@media` as `print` by default, so a rule under `@media screen` does
   not apply (reported in review; phase 0 confirms it, and D4 follows Word).
2. **A hook** that names the source element of each outermost hidden element's content, so that docx4j
   can report a path and an address. The hook dedupes by marked ancestor: an element inside a hidden
   one is reported through the outer one.
3. **The caption defect** (§1a): a table with a `caption` comes out as a `w:tbl` directly inside a
   `w:tbl`. Word would most likely repair such a file, and `convertAltChunks` users get it today.
4. Asked, not required by this CR: an `ol`'s `type` and an `li`'s `value`; `h1` to `h6` mapped to
   heading styles by default; `hr` as a bottom border; and `convertMHT` reading an HTML part that is
   not well-formed through jsoup where it is present.

docx4j reaches the importer by reflection and uses the option and the hook where they are present.
An earlier importer leaves `display: none` content out, as today; the inspector then lists the chunk
as undecided.

### 4b. Running a layout engine on a hostile chunk

The extraction and the inspector both run the import, and the inspector exists for hostile
documents. REQ-907's byte and element limits bound neither time nor the stack. These pass both
limits:
- deep nesting (recursive layout, a stack overflow);
- a `rowspan` or `colspan` near 2^31, which jsoup does not clamp;
- absurd widths, font sizes, line heights or margins;
- thousands of floats;
- a `data:` image, which `XHTMLImporterImpl.addImage` still admits with external resources
  disallowed (:2030), so an image bomb reaches the decoder.

Today `AltChunks.imported` catches `Exception | LinkageError` only (:160). It reaches the importer
through `Method.invoke`, though, which wraps an `Error` in an `InvocationTargetException`, so a
`StackOverflowError` is caught and the chunk falls back to jsoup with no warning (phase 0, §10.2).
Anything that calls the importer directly, as the inspector may, has no such wrapping. The guard, in
both:
- before the import:
  - the span clamps;
  - a nesting-depth cap;
  - `img` elements dropped, or a no-op image handler for the import, as `neverFetch` already sets for
    the docx4j handler. Phase 0 checks whether openhtmltopdf's own image loading honours the access
    controller for `data:` URIs;
- around it: `catch (Throwable)`, the chunk then skipped (`unreadable` in the inspector,
  `altchunk.not_expanded` in the extraction);
- a wall-clock budget is D10.

## 5. Phases

### Phase 0: tests, and Word

- §1's cases as JUnit tests in core-tests, asserting today's results where they stay and marking the
  ones that change.
- Map the suite rows that change (cases 36, 44 and 45, and the "xhtml importer present/absent"
  scenario), and agree them with the portfolio session, whose suite it is.
- Two checks the design rests on:
  - jsoup's W3C DOM (`W3CDom`, namespace-aware by default in 1.22.1) laid out by the importer
    through `convert(Node)` gives the same blocks as the same markup as well-formed XHTML. Also:
    `noscript` and `template` children survive the conversion, and attribute names jsoup had to
    rename (names XML rejects) do not matter;
  - §4b's vectors, each under the guard: deep nesting, huge spans, a `data:` image bomb, absurd
    lengths, many floats, with the limits at their maxima. Measure the worst time (D10);
  - under D9 (i), an entity-expansion chunk (nested internal entities in an internal subset) is
    refused within the JDK's limits.
- A probe set for Jason, `altchunk-fun/encoding/`:
  - a UTF-8 chunk with non-ASCII text and no BOM, declaration or `meta` charset;
  - the same with a UTF-8 BOM;
  - a windows-1252 chunk with no declaration;
  - a UTF-8 BOM followed by a declaration;
  - white space followed by a declaration;
  - a `meta` charset that disagrees with the bytes, with and without a BOM (D2);
  - a well-formed XHTML chunk with `<div style="display:none"/>` followed by `<p>AFTER</p>`, with and
    without a DOCTYPE. As XML the `div` is empty and `AFTER` shows; as HTML, `AFTER` is inside the
    `div` and hidden. So it tells whether Word reads well-formed XHTML as XML or as HTML (D9);
  - `display: none` set by an element selector, an id selector, a descendant selector, inside
    `@media screen` and `@media print`, overridden by a later or more specific rule, and on a `tr`,
    a `td` and an `li`.

  The answers settle D2, D4 and D9, and §2's unmeasured list.

### Phase 1: writing

The declaration strip (D1, D2), UTF-8 for the bound value, and `AltChunkType` from the content type
(D6). Tests: each of the four `addAltChunk` paths with a declared UTF-8, UTF-16 (BOM) and non-UTF-8
chunk; the binding with a bound value that has a declaration; Flat OPC untouched; a loaded chunk
whose extension disagrees with its content type. Office check: Word opens a chunk added with a
declaration, with no repair, and its non-ASCII text reads right. CHANGELOG entry. Phase 1 depends on
nothing in phases 2 to 4, and lands and is committed alone.

Built 20 October 2026 (§11).

### Phase 2: reading

Needs ImportXHTML's §4a items 1 and 2, at least as an installed snapshot. §4's reader (D3, D5, D7):
- the shared decoder;
- docx4j's parsing, and `convert(Node)` with the option on;
- `parsed` recorded;
- no importer or no parser meaning not expanded;
- `HtmlText` removed, `JsoupHtml` kept (D7);
- §4b's guard;
- `convertAltChunks` for HTML chunks (D8).

Tests:
- §1's cases 1 to 5 as the WD says;
- §1a's chunk through the new path;
- the cascade cases from phase 0's probe;
- a chunk whose encoding is declared by `meta` or a declaration;
- an MHT whose HTML is not well-formed;
- an importer without the option;
- the shared decoder giving the text model and the inspector the same characters for a chunk whose
  declaration or `meta` charset is not UTF-8 (today they differ, §1);
- an `Error` from the importer is caught and the chunk skipped;
- a stylesheet a `link` or `@import` names does not hide anything (D4), reusing CR-046 phase 3's
  no-fetch test or Jason's `altchunk-html-fetch` probe with a loopback listener;
- an Office check: Word opens what `convertAltChunks` makes from an HTML chunk with `display: none`
  (D8).

ImportXHTML's own suite is to stay unchanged with the option off (its side).

The suite rows from phase 0 pass or are recorded as pending, with the reason.

Built 20 October 2026 (§12).

### Phase 3: the inspector

The import in the inspector (REQ-901 as relaxed, REQ-907's limits before it). `text.hidden` in chunks
through the hook, `parsed`, and the `not_decoded` skip, with the counts that follow (REQ-903,
REQ-905).

Tests:
- the outermost-element rule (a `display: none` inside another gives one finding);
- an element with no text gives none;
- the location under each parse (HTML repairs, the XML tree, an MHT's first part);
- `parsed` per chunk kind;
- no importer, and no jsoup;
- a chunk over each limit is skipped before the import.

The inspector's address agrees with the extraction under `hidden: true`, block for block.

Built 21 October 2026 (§13).

### Phase 4: hand-offs

- The docx4j-ImportXHTML session (`docx4j-importxhtml-4f`): §4a. This goes out once the CR is
  accepted, ahead of phase 2, which needs it; its work is that repository's own CR and release.
- The portfolio session: REQ-901's relaxation and the MHT location (§7), sent with the ImportXHTML
  ask and before phase 3, which needs the relaxation; then the suite rows, and the registry entry
  (`docx4j/CR-053`, phases 0 to 4, with phase 2's dependency on the ImportXHTML work).
- docx4j-mcp, if any of its tools adds an HTML or XHTML chunk: the strip, and the type rule (D6).
- The python and core-ts ports: they treat docx4j as their oracle for REQ-513 and REQ-902. They have
  no ImportXHTML, so their chunk rows stay under the "importer absent" capability.

Acceptance: each acknowledged.

## 6. Decisions (for Jason; each with a recommendation)

All ten decided as recommended on 20 October 2026 (Jason: "accept the recommendations and commit
the CRs").

- **D1. Where the declaration is stripped.** (a) The four `addAltChunk` paths and the XHTML binding,
  for `Html` and `Xhtml`; (b) also in `AlternativeFormatInputPart.setBinaryData`, which would catch a
  caller's own part, and a loaded one as well. **Recommended: (a).** The sites:
  - `JaxbXmlPartAltChunkHost`:79, 113, 150 and 182, behind `AltChunkInterface`'s eight signatures;
  - `BindingTraverserXSLT`:648-652 (`text/html`, `r.getBytes()`).

  Unchanged: `BindingTraverserXSLT`:2066, which adds Flat OPC's declaration, and
  `OpenDoPEHandlerComponents`:551, which adds a `.docx` chunk. Loaded parts are built by
  `ContentTypeManager`:350 and typed by extension at `AlternativeFormatInputPart`:42-46. It covers what docx4j writes.
  Changing a loaded chunk's bytes would hide from the inspector what the file holds, and would repair a
  file the caller only meant to read and resave.
  **Decided** (Jason, 20 October 2026): (a).
- **D2. The encoding a stripped declaration named.** The options:
  - (a) Cut the declaration and nothing else.
  - (b) Cut it; where it names an encoding other than UTF-8, transcode the chunk to UTF-8, and rewrite
    a `meta` charset (or `http-equiv` content type) that names the old encoding. A UTF-16 chunk with
    a BOM is cut in UTF-16 and keeps its BOM.
  - (c) As (b), and add a UTF-8 BOM where there is none. WHATWG's BOM outranks a `meta`, so a missed
    `meta` is then moot, if Word agrees.

  **Recommended: (c), with (b)'s `meta` rewrite, subject to phase 0's probe.** Under (a), a chunk
  declared ISO-8859-1 loses the only statement of its encoding. Transcoding alone would leave a UTF-8
  body whose `meta` declares ISO-8859-1, and whether Word honours a `meta` over the bytes is
  unmeasured (§2).
  **Decided** (Jason, 20 October 2026): (c), with (b)'s `meta` rewrite, subject to phase 0's probe.
  The probe confirms it (§10.7). The strip also takes leading white space before the declaration.
- **D3. How the three kinds are read for text.** **Recommended: (a'), with REQ-901 relaxed** (Jason's
  direction, 2026-10-20, once (b) had been sized and REQ-901's relaxation and the ImportXHTML release
  were on the table). The options:
  - (a) Keep docx4j-ImportXHTML for XHTML and MHT, with §4a's `display: none` option. HTML chunks stay
    with `HtmlText`.
  - (a') All three kinds through the importer, after docx4j parses them (§4), with §4a's option and
    hook. The inspector runs the same import.
  - (b) docx4j reads all three kinds with its own HTML reader, and ImportXHTML leaves the text model.

  Why (a'):
  - **The structure is already built.** §1a measured it: the importer gives list levels, formats and
    `start`, inline emphasis, links and code, and a row span's continuation cell. `HtmlText` gives
    none of these. Under (b), building them is about 500 to 600 lines:
    - lists, about 150 to 200 (`type`, `start`, `value`, `reversed`, `list-style-type`);
    - the WHATWG table grid, about 100;
    - headings, quotes, rules and code, about 50;
    - inline formatting and links, about 80;
    - a cascade widened to the properties these read, about 100.

    That is on top of the `display` cascade itself.
  - **One CSS engine.** openhtmltopdf's cascade decides what is hidden for `convertAltChunks`, for
    extraction and for the inspector. With REQ-901 relaxed, the inspector runs the same import, so its
    addresses are the extraction's. Under (a) or (b), docx4j would keep a second cascade beside
    openhtmltopdf's.
  - **The default stylesheet hides what Word hides.** openhtmltopdf hides only `head` and `script`
    (§1), so `template`, `noscript`, `hidden` and `details` show, as in Word. `HtmlText` skips
    `template` and `noscript`.
  - **`convertAltChunks` gains HTML chunks,** which it skips today, with `display: none` as hidden
    text, as Word's import makes it. The OpenDoPE XHTML binding writes `text/html` chunks, so a bound
    document's chunks can then be converted without Word.
  - **An ImportXHTML release is due anyway** (c8dc938, unreleased).
  - **The WD barely moves.** REQ-513 already says an HTML chunk is tidied to XHTML and an XHTML or
    MHT chunk read through an importer; only REQ-901's ban on running an importer changes (§7).

  Costs accepted:
  - **Dependencies.** Chunk text needs ImportXHTML and openhtmltopdf, both LGPL 2.1, with PDFBox and
    its family and mime4j, which are Apache 2.0. HTML and MHT chunks need jsoup (MIT) too. Without them, a chunk is not
    expanded (D7). Under (b) it would have needed jsoup alone.
  - **Bounds.** A layout engine cannot be bounded mid-run, so REQ-907's limits become checks made
    before the import.
  - **Hostile input.** The inspector runs a layout engine on what it inspects; §4b is what makes that
    defensible. Since docx4j parses, the importer's XML loader (which logs "Unable to disable XML
    External Entities" on docx4j's class path) is no longer reached. The no-fetch user agent
    (0847b3df3, a579d927f) stays.
  - **The ports** have no ImportXHTML. Their chunk rows stay under the "importer absent"
    capability, and some rows will encode the importer's own choices, its list labels for one.
  - **The importer's gaps** (§1a: `type`, `value`, headings, the caption defect) become asks of
    ImportXHTML (§4a). Until they are met, its output is docx4j's.
  - **Two repositories.** docx4j uses the option and the hook by reflection where present, so the
    two releases need no lockstep. Full conformance needs ImportXHTML 17.3.4.

  Not chosen:
  - (a): HTML chunks would stay with `HtmlText`, which would need its own cascade and §1a's
    structural work anyway.
  - (b): the earlier recommendation, made while REQ-901 kept the importer out of the inspector. It
    costs the 500 to 600 lines above, a second CSS engine, and `convertAltChunks` would still skip
    HTML chunks.

  **Decided** (Jason, 20 October 2026): (a'), with REQ-901 relaxed.
- **D4. Which CSS applies.** openhtmltopdf's cascade, as the importer applies it: the selectors it
  supports, specificity, order, `!important`, and the `style` attribute. Nothing is fetched, so a
  stylesheet a `link` or `@import` names does not apply. **Recommended,** with `@media` as
  openhtmltopdf treats it, to be compared with Word in phase 0's probe; where they differ, the
  pre-pass follows Word.
  **Decided** (Jason, 20 October 2026): as recommended; `@media` follows Word once phase 0's probe has
  measured it. Measured (§10.6): Word ignores `@media`, and several selectors openhtmltopdf applies;
  the rewrite in §10.7 follows Word. Jason approved the rewrite the same day ("D4: ok").
- **D5. Granularity.** A hidden `span` makes hidden runs in its paragraph; a hidden block makes hidden
  paragraphs, their marks hidden too; a table keeps its shape, with the text of a hidden row or cell
  hidden. **Recommended,** since it is what Word builds (the class becomes a style with `w:vanish`);
  the importer emits it (§4a). A row or cell is to be checked against the phase 0 probe.
  **Decided** (Jason, 20 October 2026): as recommended; a row, a cell and a list item are checked
  against phase 0's probe. Checked (§10.6): they agree, a hidden cell keeping its column.
- **D6. A loaded chunk's type.** (a) From the declared content type, with the extension as the fallback;
  (b) as now, the extension only. **Recommended: (a).** It changes `convertAltChunks` for a chunk whose
  name and type disagree, which today it converts as the wrong kind or skips. The text model already
  decides by content type first and extension second (`AltChunks`:82-95), so (a) brings
  `convertAltChunks` into line.
  **Decided** (Jason, 20 October 2026): (a).
- **D7. Without the importer.** (a) An HTML, XHTML or MHT chunk is not expanded
  (`altchunk.not_expanded`), and `HtmlText` is removed; `JsoupHtml`, the parse step, stays. (b)
  `HtmlText` stays as a fallback, with its own `display` cascade and §1a's fixes. **Recommended: (a).** It means one reader
  and one CSS engine. The text model has not shipped (no tag contains 608e40023), so nothing released
  regresses. The cost: a caller without ImportXHTML gets the warning, which names the chunk's content
  type, and no text.
  **Decided** (Jason, 20 October 2026): (a).
- **D8. `convertAltChunks` and `display: none`.** (a) It turns the option on, so a converted document
  carries the hidden text as Word's import makes it; (b) it leaves the option off, as today, and the
  text is dropped. **Recommended: (a),** as Word does. It changes the output for a caller who
  converts an XHTML chunk with `display: none` content: today that content is dropped, and with (a)
  it is kept as hidden text.
  **Decided** (Jason, 20 October 2026): (a).
- **D9. Which XML parser reads a well-formed XHTML chunk, and what a DOCTYPE means.**
  `XmlUtils.getNewDocumentBuilder` sets `disallow-doctype-decl` (`XmlUtils`:277). A real XHTML chunk
  usually carries a DOCTYPE, and named entities (`&nbsp;`) that only the XHTML DTD defines. Under that
  builder every DOCTYPE'd chunk is a fatal error and would go to jsoup, getting `parsed: html` and
  HTML-tree paths, where REQ-901 and REQ-902 give a well-formed XHTML chunk `xml` and XML-tree paths.
  The two trees differ: the HTML parser adds `html`, `head`, `body` and `tbody`. Today's importer
  avoids this because openhtmltopdf's loader resolves the XHTML entity sets from bundled copies.
  - (i) A builder of the chunks' own: it allows a DOCTYPE, loads no external DTD, resolves the XHTML
    public identifiers and entity sets from a local copy, and has the JDK's entity-expansion limits on.
    An internal subset is then possible, so phase 0's entity-expansion test applies.
  - (ii) docx4j's builder as it is: "well-formed" comes to mean "parses without a DOCTYPE", and a
    DOCTYPE'd XHTML chunk is read as HTML. REQ-901's wording changes to match.
  - (iii) Every chunk by the WHATWG parser, `parsed` always `html`, if Word reads well-formed XHTML as
    HTML. REQ-901's XML-tree rule then goes.

  **Recommended: settle it with phase 0's `<div style="display:none"/>` probe.** If Word hides
  `AFTER`, it reads XHTML as HTML: (iii), the simplest, with no XML parser at all. If not, (i), which
  follows the WD as written. (ii) is the fallback if (i)'s entity handling proves fragile.
  **Decided** (Jason, 20 October 2026): settled by phase 0's probe: (iii) if Word hides `AFTER`, else
  (i); (ii) the fallback. Word hides it (§10.6), so (iii).
- **D10. A wall-clock budget for the import.** openhtmltopdf cannot be interrupted, so a watchdog can
  only abandon the thread, which runs on to its end.
  - (a) No budget: §4b's caps bound the work by the element limit.
  - (b) An optional timeout that abandons the import on a daemon thread, the chunk then skipped.

  **Recommended: (a), unless phase 0's worst case, with the limits at their maxima, takes longer than
  a few seconds; then (b), as an opt-in for servers.** An abandoned thread keeps its CPU, so (b) is
  not a default.
  **Decided** (Jason, 20 October 2026): (a), unless phase 0's worst case takes more than a few
  seconds; then (b), as an opt-in. Phase 0 measured minutes (§10.4), so (b) applies.
- **D11. An element limit for the import** (from phase 0, §10.4). **Recommended: 50,000 elements,
  set by a docx4j property.**
  **Decided** (Jason, 20 October 2026): 50,000.

## 7. Questions for the WD (to the portfolio session)

- **REQ-901, relaxed** (D3). Today an inspector "MUST NOT fetch, import or compute: ... no importer
  run". Proposed: "no field is updated, no external target resolved; an importer that the extraction
  runs MAY run, provided it fetches nothing and REQ-907's limits are checked before it runs". REQ-907's
  "each limit enforced while decoding or walking" gains the case of an import.
- **An MHT chunk's location.** REQ-901 says "the path in the tree that [HTML]'s parsing algorithm
  builds", but an MHT has several MIME parts. Proposed: the first `text/html` part's tree, the part
  extraction reads. A `display: none` element in another part is never shown, so it is not reported
  as hidden. The scan for scripts and references still covers every part.
- **REQ-513's NOTE** on docx4j's fetching through ImportXHTML stays. A new NOTE could record that
  docx4j parses each chunk itself and gives the importer the tree.

## 8. Sources

- Jason's ruling, 2026-10-20 (through the portfolio): strip the declaration when adding an HTML or
  XHTML chunk; Flat OPC may keep it.
- WD 0.23 (6cdb966), 0.24 (016963e), 0.25 (1865f10); paper §7.10; Jason's probes in
  `~/vbShares/Office 2016/altchunk-fun/` (README.txt) and `xhtml-bisect/` (rounds 1 to 4).
- CR-046 (the text model; D5, jsoup optional), CR-050 (the inspector; §2.1), CR-051 §11.2 (the
  `AltChunkType` note corrected in §1).
- docx4j-ImportXHTML at c8dc938 (`ListHelper`, `TableHelper`, `convert(Node)`, `convertMHT`);
  openhtmltopdf-core 1.1.31's `XhtmlNamespaceHandler.css`.
- The scratch drivers `ChunkNow.java`, `LoadedType.java`, `LoadedType2.java`, `Structure.java`, `IxRaw.java` (session scratchpad,
  2026-10-20).

## 9. Review

Reviewed 2026-10-20 by docx4j-64 (Claude Fable 5.1), read-only. It made 16 findings, five of them
defects in the draft, and all are taken:
- the XHTML parser and DOCTYPE (D9);
- the licences (D3);
- the `meta` charset under transcoding (D2);
- the §1 wording on `display`;
- the hostile-input guard (§4b, D10).

Its suggestions are taken where they fall: the cascade API, the dedupe and geometry (§4a), the
`W3CDom` checks (phase 0), the address numbering (§4), the element count after repairs, `JsoupHtml`
kept (D7), the D6 precedent, the D1 sites, the anonymiser, `convertAltChunks`' callers (§4, D8), the
phase ordering and tests. It verified §1's readings against the code, the unshipped text model and
inspector, ImportXHTML's unreleased HEAD, the default stylesheet, `setExternalResourcesAllowed`, and
the WD text. It did not rerun the drivers or check §1a.

## 10. Phase 0 results (20 October 2026)

Measured on VERSION_17_3_4 at 0f6e60d00, with the installed 17.3.4-SNAPSHOT, jsoup 1.22.1, and
docx4j-ImportXHTML at c8dc938 compiled from its commit (its target jar is older), with openhtmltopdf
1.1.31. The scratch drivers (`W3CDomCheck`, `Hostile`, `SoeToday`, `EpsChunk`, `ProbeCheck`) are
kept with the earlier ones outside the repository.

### 10.1 Tests of today

`AltChunkHtmlTest` (core-tests, `org.docx4j.model.text`) holds §1's cases as the build can run them:
jsoup present, ImportXHTML absent. The tests that read a chunk are skipped where the importer is on
the class path. Each test says which phase changes its result, and what the result becomes:

| Test | Today | Changed by |
|---|---|---|
| `htmlChunkDisplayNoneIsOrdinaryText` (case 1) | the four hidden texts are ordinary text, with `hidden: true` too; the inspector has `altchunk` only | phase 2 (not expanded without the importer), phase 3 |
| `xhtmlChunkWithoutTheImporterIsReadAsHtml` (case 2) | the same, and the inspector also reports `part.unknown` (§10.2) | phase 2; the WD (§10.5) |
| `xhtmlChunkNotWellFormedIsRepairedAsHtml` (case 5, no importer) | jsoup's repairs, `display: none` text ordinary | phase 2 |
| `mhtChunkWithoutTheImporterIsNotExpanded` | not expanded, `altchunk.not_expanded`; the inspector scans it | phase 3 (inspector: `not_decoded`) |
| `htmlChunkDropsTemplateAndNoscript` | `template` and `noscript` text dropped; `hidden` and closed `details` text kept | phase 2 (all four kept, as Word keeps them) |
| `htmlChunkRowSpanHasNoContinuationCell` (§1a) | the second row is a cell short | phase 2 |
| `extractionAndInspectionDecodeAChunkDifferently` | 0xD0 under `meta charset='ISO-8859-5'`: `Ð` in the text, `а` in the inspector | phase 2 (one decoder) |
| `addAltChunkWritesTheDeclaration` (case 6) | all four `addAltChunk` paths write `Html`, `Xhtml` and Flat OPC byte for byte | phase 1 (`Html`, `Xhtml`); Flat OPC stays |
| `boundXhtmlValueKeepsItsDeclaration` | the binding's `text/html` chunk keeps the declaration, in the platform charset | phase 1 |
| `loadedChunkTypeFollowsTheExtension` | `.htm` declared XHTML is `Html`; `.dat` declared `text/plain` has no type, and `convertAltChunks` leaves it, though the text model reads it | phase 1 (D6) |

### 10.2 Found on the way

- **The `Error` is caught today, by accident.** A well-formed XHTML chunk of 2,000 nested `div`s
  overflows the stack in openhtmltopdf, but `Texts.of` returns its text: `Method.invoke` wraps the
  `StackOverflowError`, `AltChunks` catches the wrapper, logs a warning and reads the chunk with
  jsoup, which caps the nesting (§10.4). The result carries no `TextWarning`. §4b is corrected.
  Phase 2's `catch (Throwable)` still stands, and the chunk is then not expanded, with the warning,
  rather than read another way.
- **The inspector reports an XHTML chunk's part as `part.unknown`.** REQ-902's known families have no
  `application/xhtml+xml`, and docx4j follows them; every XHTML chunk `addAltChunk` makes draws the
  finding. A question for the WD (§10.5). No fixture holds an XHTML chunk, so the suite never met it.
- **`convertAltChunks` stops at the first XHTML or MHT chunk when the importer is missing.** It
  returns rather than skipping that chunk, so the chunks it has not reached yet (it works from the
  last) stay unconverted, a plain-text chunk included. For phase 2, which changes this method.
- `addAltChunk` names a new chunk `/chunk.<ext>`, at the package root, not under `/word/`. Word opens
  it; nothing in this CR depends on it.

### 10.3 The jsoup tree through `convert(Node)` (§5's first check)

Three chunks, each well-formed XHTML, went through the importer three ways: parsed as XML, parsed by
jsoup through `W3CDom`, and parsed by the importer itself. The output was the same each way:
- §1a's chunk (lists, inline formatting, a row and column span), plus a table without `tbody`, to
  which jsoup adds one;
- `template`, `noscript`, `hidden` and a closed `details`: the `template` and `noscript` content
  survives `W3CDom` and is imported;
- CSS by class, child selector and `style` attribute.

jsoup renames or drops attribute names XML rejects (`a<b` became `a_b`, `foo"bar` became `foo_bar`,
`1x` was dropped). The paragraph's `class` and `style` still applied, and the import was unharmed.
The elements of an HTML chunk carry the XHTML namespace in `W3CDom`'s tree, as the importer needs.
ImportXHTML's CR-002 adds that `convert(Node)` lays out the caller's own tree only when given a
`Document`, so docx4j passes the `Document`.

### 10.4 §4b's vectors, and D10

Each vector ran in its own JVM (`-Xmx2g`, the default 1 MB stack), through jsoup, `W3CDom` and
`convert(Document)` with external resources off, under `catch (Throwable)`. The start-up and an
empty import take about 1.3 seconds; the times are the import's.

| Vector | Result |
|---|---|
| Nesting, parsed by jsoup | jsoup's tree builder caps the depth at about 512 and makes deeper elements siblings: 5,000 nested `div`s import in 2 s. With a 256 KB stack, even that depth overflows. |
| Nesting, parsed as XML | no cap: 900 nested `div`s (10 KB) overflow the stack in openhtmltopdf's style and box building (`CalculatedStyle.valueByName`, `BoxBuilder.createChildren`); 800 do not |
| `rowspan` or `colspan` 2^31−1 | a 163-byte chunk exhausts a 2 GB heap; 65,534 and 1,000, WHATWG's clamps, import in under 1.5 s |
| Widths, font sizes, line heights, margins, indents of 2×10^9 | harmless |
| Floats | quadratic: 20,000 in 7 s, 50,000 (1.1 MB) in 41 s. `float:none !important` in each element's `style` attribute brings 20,000 back to 2 s |
| Paragraphs | linear, about 0.09 ms and 4.8 KB of heap each: 200,000 in 19.5 s (0.9 GB), 500,000 (4 MB) in 45 s (2.4 GB) |
| Table cells | 100,000 in 7.7 s |
| A `data:` pixel bomb (a 25,000² RGBA PNG, 2.4 MB) | not decoded: the importer's default image handler reads its header (100 MB of heap), and the no-op handler adds nothing |
| A `data:` image in CSS | refused by openhtmltopdf under the deny-all access controller, as ImportXHTML reports |
| A `data:` EPS image, default handler | refused by the importer's user agent ("Unrecognized Image format"), so it never reaches `BinaryPartAbstractImage`'s conversion through ImageMagick |

What follows for phase 2's guard:
- the span clamps are needed;
- the depth cap is needed for a chunk parsed as XML (D9 (i)), and below 512 for small stacks.
  Recommended: 256, deeper elements made siblings as jsoup does;
- the pre-pass adds `float:none !important` to every element's `style`. It changes no text;
- `img` needs nothing beyond the no-op image handler `neverFetch` already sets.

**D10.** Its condition is met: at REQ-907's maxima, a 16 MB chunk of paragraphs would take about three
minutes and ten gigabytes. So (b), the opt-in timeout, applies as decided. But a timeout abandons a
thread that keeps its heap, and the heap runs out first.

- **D11 (new; decided by Jason the same day: 50,000). An element limit for the import.** A chunk whose tree, after the parse and
  the repairs, has more elements than the limit is not imported: the extraction raises
  `altchunk.not_expanded`, and the inspector skips the chunk as `limit.elements`. **Recommended:
  50,000 elements (about 5 seconds and 250 MB at worst, floats defused), set by a docx4j property.**
  REQ-907's own limit (5,000,000) stays for the rest of the walk. The WD needs a line on it (§10.5).

### 10.5 The suite rows that change, and questions for the WD

The suite is the portfolio's. In docx4j's build run (jsoup present, ImportXHTML absent), phases 2
and 3 change these rows (sent to the portfolio session):
- **Case 36** (`ALT36_CHUNK_TEXT`, an HTML chunk) and **case 63's** `CHK63_SCRIPTED`: not expanded
  without the importer. They take case 44's form: 0 in the default rows, which need the capability
  "an HTML parser absent", and 1 under "an HTML parser present". docx4j advertises a parser only with
  both jsoup and ImportXHTML, since it reads HTML through the importer. Each gains a warning row,
  `altchunk.not_expanded` `text/html`. `altchunks: false` joins the supplemental scenario for both
  (`ExtractionConformance`:249 and :262-266).
- **The goldens** of `extraction-probe.docx` (`.accepted.txt`, `.original.txt`, `.markup.txt`,
  `.md`) hold `ALT36_CHUNK_TEXT`. docx4j can no longer make them without the importer, and with the
  importer, case 44's MHT chunk expands too. Recommended: recut them with neither an HTML parser nor
  an importer, so case 36's line goes. A port then compares the goldens in a run with neither.
- **The inspection.** Without the importer, the HTML and MHT chunks are skipped as `not_decoded`:
  `extraction-probe.docx` adds `not_decoded@|/word/afchunk.htm|` and
  `not_decoded@|/word/chunk44.mht|`, and `extraction-probe-inspect.docx` goes from `COMPLETE` to
  `PARTIAL` with `not_decoded@|/word/chunk63.htm|`. Its counts become lower bounds. Case 63's
  `altchunk.script` and `altchunk.external_ref` stay (the tag scanner). With the importer, each
  `altchunk` finding carries `parsed`, which needs a column or a field in `expected-inspection.tsv`.
- **Case 45** (RTF) does not change in docx4j, which has no RTF reader. Its note, "never expanded",
  predates WD 0.23.
- Case 44's MHT rows and case 47's nested HTML chunk (one level) are unchanged.

Questions for the WD:
- `application/xhtml+xml` among REQ-902's known families (§10.2);
- the capability "an HTML parser present" in the suite, with REQ-1201's note that an implementation
  may need more than a parser for it;
- the goldens recut as above (REQ-1203);
- REQ-907: a line for an import's own element limit (D11).

The portfolio's answer (2a8eb26, the same day), for Jason to rule on with WD 0.26. It agrees, with
five changes, which this CR takes:
- the capability is named "an HTML reader present", defined in REQ-513 as a parser that implements
  [HTML]'s parsing algorithm plus whatever reads its tree as blocks. As REQ-513 stands, an
  implementation with a parser present that does not expand would break it, rather than lack a
  capability. The capability takes in "an XHTML importer present" (case 44, REQ-1203, REQ-1204),
  since all three kinds go through the same reader under D3;
- the goldens are recut with no HTML reader present, and the diff is case 36's line alone;
- the inspection oracle stays reader-absent, with the new `not_decoded` skips, so `parsed` needs no
  column there. A supplemental inspection scenario under "an HTML reader present" checks phase 3's
  result (`text.hidden` inside a chunk, the outermost element, `parsed`). Its new fixture holds an
  HTML chunk with inline and class `display: none`, and a well-formed XHTML chunk;
- `application/xhtml+xml` joins REQ-902's known families;
- if Jason takes D11, the WD fixes the number for conformance, as with REQ-907's other limits, and
  REQ-513 gains the extraction's warning over the limit.

Two caveats, sent back:
- The capability fits docx4j exactly under D9 (iii). Under D9 (i), docx4j with ImportXHTML but
  without jsoup still expands a well-formed XHTML chunk, so that configuration advertises neither
  "present" nor "absent".
- docx4j's build cannot check the reader-present rows, since core-tests has no ImportXHTML. A phase 2
  question: a test-scope ImportXHTML (17.3.4, once released) in a second surefire execution, or a
  small test module of its own.

### 10.6 The Word probes

`~/vbShares/Office 2016/altchunk-fun/encoding/`, 16 files, with a README in the style of the earlier
rounds:
- for D2: `enc-1` to `enc-11`, covering UTF-8 with and without a BOM; windows-1252 with nothing
  declared, by `meta charset` and by `http-equiv`; a `meta` that disagrees with the bytes, with and
  without a BOM; a BOM or white space before the declaration; UTF-16 with a BOM; XHTML with a BOM;
- for D9: `d9-1` to `d9-3`, the empty `display:none` `div` as XHTML without and with a DOCTYPE, and
  as `text/html`;
- for D4 and D5: `css-1-selectors` (element, id, descendant, child, attribute, two classes,
  `:first-child`, `@media screen` and `print`, later and more specific rules, `!important` against
  `style`, a `style` element in the body) and `css-2-table-list-nesting` (a row, a cell, a list item,
  a `display:block` inside a hidden block, a span).

Jason ran them in Word 365 the same day; his screenshots and `*-word365.docx` saves are in the
folder. Everything opened cleanly except `enc-8` and `enc-9`.

**Encoding.**
- **Opened and read right:**
  - `enc-1`, UTF-8 with nothing declared;
  - `enc-2`, UTF-8 with a BOM;
  - `enc-3`, windows-1252 bytes with nothing declared;
  - `enc-4` and `enc-5`, windows-1252 declared by `meta charset` and by `http-equiv`;
  - `enc-7`, UTF-8 with a BOM and a `meta` naming windows-1252: the BOM wins;
  - `enc-10`, UTF-16LE with a BOM;
  - `enc-11`, XHTML with a UTF-8 BOM.
- **Garbled:** `enc-6`, UTF-8 bytes with no BOM and a `meta` naming windows-1252, came out
  `cafÃ©` (Word followed the `meta`).
- **Refused:**
  - `enc-8`, a BOM before the declaration: unreadable content, repaired as plain paragraphs with
    the `title` among them;
  - `enc-9`, white space before the declaration: unreadable content, not recovered (the chunk is
    lost).

So Word decodes a chunk by its BOM, then a `meta` charset in either form, then as UTF-8 where the
bytes are valid UTF-8, else as windows-1252. Windows-1252 may be the machine's ANSI code page rather
than a fixed choice; one English-language machine cannot tell the two apart.

**D9.** `D9_AFTER` is hidden in all three, with and without the DOCTYPE: Word reads well-formed XHTML
as HTML.

**`display: none`.** In `css-1`, Word hides:
- the id selector (`#hid`);
- the descendant selector (`div.box p`);
- `!important` against the `style` attribute;
- the `style` element in the body.

It does not hide:
- `article { display: none }`. `article` is an HTML5 element; an HTML4 element is unmeasured;
- the child selector (`>`);
- the attribute selector;
- two classes (`.a.b`);
- `:first-child`;
- anything inside `@media`, `screen` or `print`.

The overrides behave as a browser's:
- a later rule wins over an earlier one;
- `p.os { display: block }` wins over a later `.os { display: none }`;
- `style="display:block"` wins over a class rule.

Word writes those last two as `w:vanish w:val="0"` over a style that has `w:vanish`.

In `css-2`:
- the hidden row's text is hidden, and the row carries `w:trPr/w:hidden`;
- the hidden cell stays in the grid, so `C2_R3_B` is in the second column, and its text is hidden;
- the hidden list item stays numbered, with formatting marks shown, so `C2_ITEM_THREE` reads `3.`.
  The number with the marks hidden is unmeasured;
- `C2_BLOCK_INSIDE_HIDDEN`, a `display:block` paragraph inside a `display:none` `div`, shows;
- the span is a hidden run.

openhtmltopdf's answers differ from Word's in these places:
- it hides `article`, the child, attribute, two-class and `:first-child` rules, and `@media print`.
  Word hides none of them;
- it ignores a `style` element in the body, which Word applies;
- it drops the block inside the hidden `div`, and the hidden cell with its column. Word shows the
  first and keeps the second.

### 10.7 What the probes settle

- **D2** stands at (c): Word lets a BOM outrank a `meta` (`enc-7`), and follows a `meta` over the
  bytes (`enc-6`). So a transcoded chunk needs the BOM, or the rewritten `meta`. Phase 1's strip
  removes the declaration after a BOM and after leading white space too (`enc-8`, `enc-9`), and keeps
  or adds the BOM.
- **Phase 2's shared decoder** is Word's: the BOM, then a `meta` charset (`charset` or
  `http-equiv`), then UTF-8 where valid, else windows-1252. Neither of today's decoders is that rule:
  - `Embedded.decode` has no `meta`;
  - the WD's Chunks rule (`InspectPayloads.decode`) falls back to UTF-8 rather than windows-1252.

  The second is a WD question (below).
- **D9** is (iii), as decided for this answer:
  - every chunk is parsed by jsoup, and `parsed` is always `html`. REQ-901's XML-tree rule goes,
    and so does `parsed: xml` (a WD question);
  - the D9 (i) entity-expansion test is dropped;
  - the depth cap is jsoup's own: `Parser.setMaxDepth` exists in 1.22.1. Recommended 256, set
    where the method is present, since jsoup is optional and an older one may lack it;
  - the portfolio's first caveat (§10.5) no longer arises.
- **D4.** Word's cascade is narrower than openhtmltopdf's, and D4 says the pre-pass follows Word.
  Recommended: before the import, docx4j rewrites the chunk's `style` elements as Word reads them:
  - rules inside `@media` dropped;
  - rules whose selector has a child or sibling combinator, an attribute selector, a pseudo-class,
    or two classes on one element dropped;
  - `style` elements in the body moved to the head.

  - elements Word does not know unwrapped, their content kept in their place (`css-4`). The set
    Word knows is taken to be HTML 4.01's elements. Its only measurements are `article`,
    `section`, `zz` and `yy` outside it, and `p`, `span`, `address` and `h6` (`css-3`) and
    `noscript` inside it; `template` and `details` showing in round 1 fit.

  Unwrapping drops the element's own attributes with it (`class`, `id`, `style`), which is what
  the portfolio's wording of transparency says (measured in `css-6`). REQ-901's locations still count
  the tree jsoup built, `/html[1]/body[1]/article[2]/p[1]`, so docx4j records each element's path
  before the rewrite, and the rewrite moves nodes rather than copying them.

  - a type or single-class rule dropped where the host's styles part already defines the
    paragraph style Word maps it to (`host-1`, `host-2`): Normal (Web) for `p`, Heading *n* for
    `h`*n*, HTML Address for `address`, the class's name for a class. The table beyond the three
    measured elements is Word's HTML style set, by inference. The rule's other properties go with
    it, which is unmeasured.

  The rewrite applies to every property, not only `display`. Word's handling of other properties
  under those selectors is unmeasured, but a rule it ignores for `display` it presumably ignores for
  the rest. It keeps one CSS engine. Type selectors on elements Word knows count (`css-3`).
- **D5** stands, with Word's structure:
  - a hidden row's text hidden;
  - a hidden cell kept in its column, its text hidden. Re-showing the cell, as ImportXHTML's CR-002
    does, restores Word's grid;
  - a hidden list item kept and numbered.
- **A new rule for ImportXHTML.** Word treats `display: none` as hiding that an element's own
  explicit `display` (by `style` or a rule) undoes for its content: `C2_BLOCK_INSIDE_HIDDEN` shows,
  where `d9`'s plain `p` inside the hidden `div` is hidden. CR-002's dedupe by marked ancestor hides
  both. It is sent to that session.

Questions for the WD (with §10.5's):
- REQ-513's NOTE ("selectors other than a class were not measured"): the measured list above;
- the Chunks rule's last step: UTF-8 where valid, else windows-1252, and `http-equiv` beside
  `charset`;
- D9 (iii): `parsed` is always `html`, and REQ-901 counts the HTML tree for every chunk;
- REQ-513's hiding rule: which selectors count, `@media` ignored, a body `style` element counted, and
  an explicit `display` on a descendant showing its content.

**Round 6**, the same folder, built the same day at the portfolio's and ImportXHTML's asks:
- `css-3-type-selectors`: type selectors on HTML4 elements (`p`, `span`, `address`, `h6`). Is
  `article`'s rule ignored because type selectors do not count, or because Word does not know
  `article`?
- `css-4-html5-and-unknown-elements`: `article` with no rule and with a class rule, `section`, and
  unknown elements with and without a rule;
- `css-5-reveal-and-table-groups`: `display:inline` and `display:inherit` inside a hidden `div`, a
  hidden span inside a re-shown block (ImportXHTML's CR-002 D11), and a hidden `table`, `thead` and
  `tbody`.

openhtmltopdf drops every marker these hide, and the three nested ones of `css-5` besides.

Word's answer to `css-5` (Jason, the same day):
- inside a `display:none` `div`:
  - the `div`'s own text is hidden;
  - a `display:inline` span shows, as round 5's `display:block` paragraph did;
  - a `display:inherit` span stays hidden (it inherits `none`);
  - a `display:none` span inside a re-shown block is hidden.

  So an explicit `display` other than `none` or `inherit` undoes an ancestor's hiding for its own
  content, and a `display: none` inside it hides again. ImportXHTML's CR-002 D11 proposes this rule.
- a hidden `table`, `thead` or `tbody`: its rows stay, each with its text hidden and
  `w:trPr/w:hidden`. A wholly hidden table's column collapses to almost nothing, and Word puts a
  paragraph with a hidden mark between adjacent tables.

Word's answer to `css-3`:
- `p`, `span`, `address` and `h6` with `display: none` are all hidden, and the text around the
  span shows. So type selectors on HTML4 elements count, and `css-1`'s `article` rule most likely
  failed because Word does not know the element;
- Word writes these rules into its built-in paragraph styles: `w:vanish` in Normal (Web), HTML
  Address and Heading 6. The probe has no styles of its own. Whether such a rule also hides the main
  document's own paragraphs in those styles is unmeasured.

Word's answer to `css-4`: an element Word does not know (`article`, `section`, and the made-up `zz`
and `yy`) is transparent:
- its content is kept. The paragraph inside an `article` stays a paragraph. The text of `zz` and
  `yy`, which stand in the body, runs together into one paragraph, as inline content does;
- no rule aimed at the element applies. Neither `section { display: none }` nor
  `zz { display: none }` hides anything, nor does a class rule on an `article` (`.ak`).

openhtmltopdf knows `article` and `section` as blocks, and applies all three rules. An inline
`style="display:none"` on an unknown element is unmeasured.

Jason ran round 6 the same day.

**Round 7**, at the portfolio's ask: can a chunk's rule hide the host document's own text? `css-3`
showed that Word writes a type rule into a built-in style, and round 2 that a class rule becomes a
style named after the class. Each of the two hosts has a styles part, with paragraphs before and
after the chunk in the styles a chunk rule targets:
- `host-1-heading6-and-normal-web`: the host's Heading 6 and Normal (Web) paragraphs, and a chunk
  hiding `h6` and `p`;
- `host-2-custom-style-named-like-a-class`: the host's custom style named `hid`, and a chunk hiding
  `.hid`;
- `css-6-unknown-element-own-attributes`: whether an unknown element's own `style` attribute and `id`
  count for nothing, as css-4's rules did. The portfolio's wording of transparency predicts they do.

If Word hides the host's paragraphs, the WD needs a rule: an inspector reports the host's text that
a chunk's rule hides.

Word's answer to `css-6`: all three markers show, with no `w:vanish`. An unknown element's own
`style` attribute and `id` count for nothing, as its class did in `css-4`. So Word's transparency is
total, as the portfolio predicted, and unwrapping the element follows it.

Word's answer to `host-1` and `host-2` is the reverse of the risk the probes tested for. Nothing is
hidden anywhere, the host's text or the chunk's:
- in `host-1`, the chunk's `h6` and `p` show, in the host's Heading 6 (italic) and Normal (Web);
- in `host-2`, the chunk's `.hid` paragraph shows, in the host's `hid` style (bold);
- the saved styles are the host's definitions, with no `w:vanish`. Word renamed the host's styleId
  `Hid` to `hid` on save, and the host's paragraphs moved with it (the portfolio's check). In
  `host-2`, whose host had no Normal (Web), Word made one for the chunk's plain `p` elements.

So where the host already defines the style Word maps a rule onto, the host's definition wins, and
the rule's `display: none` is lost. A chunk cannot hide the host's text this way. Its own text that
such a rule hides in a host without the style (`css-3`, round 2) shows in a host with it.

Word applies a type rule on `p`, `h6` and `address`, and a class rule, through a paragraph style
(Normal (Web), Heading 6, HTML Address, a style named after the class). It applies the inline style,
the id selector, the descendant selector and the type rule on `span` as direct formatting, which a
host style cannot override. Unmeasured:
- whether a defined but unused host style counts;
- whether the name match is case-sensitive (the host's `hid` had styleId `Hid`, and its name matched);
- which other elements map to built-in styles. Word's HTML styles suggest `h1` to `h6`, `pre`
  (HTML Preformatted), and character styles for `code`, `kbd`, `samp`, `var`, `cite`, `dfn`;
- whether a rule's other properties are lost with its `display`.

The probes are done, and with them phase 0. The JUnit tests stand as they are.

## 11. Phase 1 (20 October 2026)

Built on VERSION_17_3_4 at 5c691fc1f.

### 11.1 The strip

`AltChunkHtml` is new and public, in `org.docx4j.openpackaging.parts.WordprocessingML`, so that
docx4j-mcp and a caller building their own part can use it. The four `addAltChunk` paths in
`JaxbXmlPartAltChunkHost` call it for `Html` and `Xhtml` (D1 (a)); the two stream paths read the
stream and take the byte path. `withoutXmlDeclaration`:
- skips a UTF-8 or UTF-16 BOM, then XML white space, and cuts a `<?xml` declaration that follows
  (`enc-8`, `enc-9`), with the white space after it. A chunk with no declaration is left as it is, its
  BOM and white space included. So is `<?xml-stylesheet ...?>`, which is a processing instruction;
- keeps the encoding the declaration stated (D2 (c), with (b)'s rewrite):
  - after a UTF-8 BOM, the bytes as they are. The BOM decides, for Word (`enc-7`) as for an XML
    reader, even against a declaration naming another encoding;
  - UTF-16, with or without a BOM, is cut in UTF-16 and keeps or gains its BOM. Word reads UTF-16LE
    with a BOM (`enc-10`); big-endian is in the Office check;
  - UTF-8, declared or by the XML default: a UTF-8 BOM, where the bytes are valid UTF-8;
  - another encoding: transcoded to UTF-8, with a BOM. Each `meta` charset, and each `http-equiv`
    content type's charset, that names an encoding other than UTF-8 is rewritten to `UTF-8`. D2 says
    "names the old encoding"; after the transcoding any other name is as wrong. ISO-8859-1 and
    US-ASCII are read as windows-1252, as WHATWG's labels have them, so a Latin-1 chunk's curly
    quotes survive;
  - where the declared encoding is one Java does not know, one the bytes cannot be in (UTF-16 named
    in single bytes), or one they are not in (UTF-8 declared, windows-1252 bytes), only the
    declaration is cut, with a warning. Word's own order then reads the bytes.

**The open point: where the BOM goes.** D2's wording left open whether a chunk declared UTF-8 gains a
BOM, or only a transcoded one. Taken: every chunk docx4j cuts gains one (UTF-8 bytes permitting), so
Word reads it as the declaration said even where a `meta` disagrees (`enc-6`: the `meta` would win).
A chunk docx4j does not cut is not touched. Narrowing it to transcoded chunks would be a one-line
change.

Jason confirmed this, with phase 1's other four calls, the same day ("your 5 calls are sound"):
- the binding's BOM and `meta` rewrite, always (§11.2);
- the rewrite of any `meta` naming an encoding other than UTF-8, not only the old one;
- ISO-8859-1 and US-ASCII read as windows-1252;
- `text/xml` left unmapped (§11.3).

### 11.2 The binding

`BindingTraverserXSLT`:648 writes `AltChunkHtml.utf8WithoutXmlDeclaration(r)`. That is the bound
value without a leading BOM character, white space or declaration, with its `meta` charsets naming
UTF-8, in UTF-8 with a BOM. The BOM is always there, since docx4j chose the encoding. Without it, a
bound value's own `meta` naming another charset would win (`enc-6`). The Flat OPC declaration
(:2066) and `OpenDoPEHandlerComponents`' `.docx` chunk are unchanged.

### 11.3 D6

`AltChunkType.forContentType` maps each type's own content type, and `application/rtf`, with
parameters and case ignored. `text/xml` is not mapped: it could be XHTML or Flat OPC, so the
extension decides. `AlternativeFormatInputPart` overrides `setContentType` to apply it:
- the loader builds a part by its name and then sets the declared content type
  (`ContentTypeManager`:349). So a loaded chunk takes the type it declares, with the extension as the
  fallback;
- the declared content type is kept as declared: `application/rtf` stays, and does not become
  `text/rtf`;
- being in the setter, it also covers Flat OPC import, and a copy that builds a part by name and sets
  the content type, as Docx4j Enterprise's MergeDocx copies a chunk. Each then agrees with a load.

The constructor's "Unrecognized type" warning for an extension docx4j does not know is now a debug
line, since the content type set next usually types the part; `convertAltChunks` still warns about a
chunk left with no type.

What changes for a loaded chunk:
- `convertAltChunks` converts a `text/plain` chunk whose extension it did not know;
- a `.htm` chunk declared `application/xhtml+xml` is now `Xhtml`, so it needs the importer, as the
  text model already treated it;
- `ZipPartStore.shouldCompress` leaves a docx chunk with an unknown extension uncompressed.

### 11.4 Tests

`AltChunkHtmlTest`'s three writing tests are flipped:
- `addAltChunkStripsTheDeclaration`;
- `boundXhtmlValueIsUtf8WithoutItsDeclaration`, whose value now carries a `meta` naming windows-1252;
- `loadedChunkTypeFollowsTheContentType`, with six chunks: the content type winning, the extension
  as the fallback, the declared content type kept, parameters and case ignored.

`AltChunkHtmlWritingTest` (core-tests, `org.docx4j.openpackaging.parts.WordprocessingML`) is new,
with 11 tests. Each case goes through all four paths and both types:
- UTF-8, declared and by default;
- a declaration after a BOM and after white space;
- UTF-16LE and BE, each with and without a BOM;
- ISO-8859-1, with six `meta` forms, and Shift_JIS;
- an unknown encoding, a wrong one, and UTF-16 declared in single bytes;
- no declaration, `<?xml-stylesheet`, a DOCTYPE, an empty chunk;
- Flat OPC, MHT, plain text and RTF as given;
- a caller's own part as given;
- a save and load round trip, read by the text model;
- the binding's markup;
- `forContentType`.

`McBlockLevelConsumersTest` no longer gives its loaded chunk a type itself, which its comment, wrong
since CR-051 §11.2 (§1), said it had to. It now asserts the type the load gives.

The whole of core-tests passes: 1,874 tests, none failing, 13 skipped (assumptions, as before).

### 11.5 Office check

`~/vbShares/Office 2016/altchunk-fun/phase1/`, with a README: nine files that docx4j itself wrote,
each chunk given to it with a declaration:
- UTF-8, declared, as HTML and as XHTML;
- ISO-8859-1 with two `meta` forms;
- a BOM, then the declaration;
- white space, then the declaration;
- UTF-16LE and UTF-16BE with a BOM;
- Shift_JIS;
- the binding, with a declared value and a `meta` naming windows-1252.

Only UTF-16BE and the binding's encoding are new to Word; the rest repeat round 5's measured cases in
the form docx4j now writes.

Jason's run, the same day (screenshots and `*-word365.docx` saves in the folder):
- `p1-1` to `p1-8` open cleanly, with the text right. That includes UTF-16BE, Shift_JIS transcoded,
  and the two cases Word refused as given, a declaration after a BOM or after white space;
- `p1-9` reports unreadable content and recovers. Word's save keeps every `P9` marker, and the bound
  value's text reads right, but it loses `FLATOPCTEXT`. That text is in the template's other chunk:
  the Flat OPC chunk the binding makes from another bound value, which phase 1 does not touch. That
  value is a `pkg:package` holding only `/word/document.xml`, with no `/_rels/.rels` to name the main
  document. Two variants separate the chunks. `p1-9b`, the HTML chunk alone, opens cleanly, and
  `p1-9c`, the Flat OPC chunk alone, reports unreadable content and recovers (Jason, the same day).

So Word opens everything phase 1 writes. The Flat OPC value is `TextBindParityTest`'s fixture, which
checks only that the chunk holds the text. docx4j's text model agrees with Word about it: the chunk
is not expanded (`altchunk.not_expanded`), since `FlatOpcXmlImporter` needs `/_rels/.rels` (:209).

## 12. Phase 2 (20 October 2026)

Built on VERSION_17_3_4 after 35e304770, against docx4j-ImportXHTML's installed 17.1.1-SNAPSHOT with
its CR-002 phases 1 to 3 (then uncommitted there; 468a760 since, from the same sources), jsoup 1.22.1
and openhtmltopdf 1.1.31. Started on ImportXHTML's notice that its phase 2 was installed, as Jason asked.

### 12.1 The reader

`HtmlChunkImport` (public, `org.docx4j.model.text`) is the one path for the text model and
`convertAltChunks`; `HtmlChunks` (package-private) holds what names neither jsoup nor the importer.
For a chunk:
- **decoded** by the WD's Chunks rule with Word's last step (`HtmlChunks.decode`, which
  `InspectPayloads.decode` now is): the BOM; UTF-16 where the first two bytes are `<` so encoded; an
  XML declaration's or a `meta`'s charset (`charset` or `http-equiv`) within 1,024 bytes; UTF-8 where
  valid; else windows-1252. The labels ISO-8859-1 and US-ASCII are read as windows-1252, by WHATWG
  Encoding's label table, not measured in Word. An MHT chunk's first HTML part is decoded in its MIME
  charset by the same labels. The WD's last step is the portfolio's ask (f), in WD 0.26;
- **parsed** by jsoup whatever its kind (D9 (iii)), nested no deeper than 256 where jsoup can be told
  (1.22.1), and with a leading U+FEFF dropped: ImportXHTML found that jsoup keeps it as text, closes
  the head before it, and so loses the chunk's `style` elements;
- **rewritten** as Word reads it (§10.7, D4), in place, nodes moved and never copied:
  - a `style` element in the body moves to the head;
  - each `style` element keeps the rules Word applies. `@media` and the other conditional group
    rules go, and so does `@import`. A selector goes where it has a child or sibling combinator, an
    attribute selector, a pseudo-class or pseudo-element, `*`, or two classes on one element. A
    type selector alone or a class selector alone goes where the host's styles part defines, by
    name and in any case, the style Word would apply it through: Normal (Web) for `p`, Heading *n*,
    HTML Address, the HTML styles by inference beyond those three, or the class's own name;
  - an element outside HTML 4.01, or outside the XHTML namespace (`svg`, `math`), is unwrapped, its
    attributes with it;
  - `colspan` is clamped to 1 to 1,000, `rowspan` to 0 to 65,534, by WHATWG's integer parsing,
    and a value that is not a number removed; each element in the body gains
    `float: none !important`;
- **limited**: a tree of more elements than `docx4j.model.text.HtmlChunkImport.maxElements`
  (50,000, D11), counted after the rewrite, is not imported;
- **imported** through `convert(Node)` with the `Document`, so the importer lays out docx4j's own
  tree, and with `setDisplayNoneAsHidden(true)` where the importer has it. An earlier importer leaves
  what `display: none` hides out, as before;
- **guarded**: anything the import throws, an `Error` included, is caught and the chunk not read;
  with `docx4j.model.text.HtmlChunkImport.timeoutMillis` set (D10 (b), off by default), an import that
  takes longer is abandoned on its daemon thread;
- **renumbered**: the target's numbering maps are rebuilt from what the import wrote (below).

Choices made where the measurements stop, each unmeasured in Word:
- a selector list keeps the selectors Word applies (Word may instead drop the whole rule);
- `*` and the conditional group rules other than `@media` go with what was measured;
- the host-style match is by name, case-insensitive.

**Found on the way: an `li value` left later lists without labels.** In docx4j's model, the lists
after an `li value` had no labels, in the text model and in `convertAltChunks`' output alike, until a
save and a reload. ImportXHTML's XML is right. Its session found the cause, which is older than
`value`:
- the importer adds a `w:num` before its abstract's levels;
- `NumberingDefinitionsPart` copies the levels when the `w:num` is added, so the cached instance
  definition has none;
- a `restart()` (`value` uses one) builds the maps mid-import, and the lists after it stay stale.

Without a restart, docx4j's first reading rebuilt the maps and hid it. ImportXHTML fixed it in the
same day's snapshot (in 468a760): it calls `initialiseMaps()` at the end of a convert that changed the numbering.
`HtmlChunkImport` also calls it after each import, for an importer before that fix.

### 12.2 The text model, the inspector and `convertAltChunks`

- **The text model (D3, D5, D7).** An HTML, XHTML or MHT chunk is expanded through the reader into
  a scratch package, never fetching; without jsoup or the importer it is not expanded, with
  `altchunk.not_expanded`. `HtmlText` is removed; `JsoupHtml` is the parse step and jsoup's presence
  check. The inspector's own extraction still calls no importer (CR-050; phase 3 changes that).
- **The capability.** `Inspection.Capability.XHTML_IMPORTER` is `HTML_READER`: jsoup and the importer
  both, as the portfolio's map has it (§10.5). Its reveal reads `html reader: present`. The text model
  has not shipped, so nothing released changes.
- **`convertAltChunks` (D8).** HTML, XHTML and MHT chunks go through `HtmlChunkImport.convert`. With
  jsoup, that is the reader, with hidden text kept. Without jsoup, an XHTML or MHT chunk is read by the
  importer's own XML parser as before, with the option on, and an HTML chunk is left. It goes on
  fetching what a chunk refers to, as Word does (`altchunk-html-fetch`, round 1). A chunk it cannot
  convert is left, and the others are still converted, where it returned before (§10.2).

### 12.3 Tests

Two configurations:
- the build's, with jsoup and without the importer;
- a hand run with the importer, with ImportXHTML's class path appended:
  `mvn test -pl docx4j-core-tests -am -Dmaven.test.additionalClasspath=<its jars, comma-separated>`.

Each reading test asserts its result in both: with a reader, Word's result; without one, not expanded.
- `AltChunkHtmlTest`'s seven reading tests now assert phase 2's results, and their Javadoc says what
  phase 0 found:
  - `htmlChunkDisplayNoneIsHiddenText`;
  - `xhtmlChunkIsReadAsHtml`;
  - `xhtmlChunkNotWellFormedIsRepairedAsHtml`;
  - `mhtChunkIsReadAsItsHtml`;
  - `htmlChunkShowsTemplateAndNoscript`;
  - `htmlChunkRowSpanHasItsContinuationCell`;
  - `extractionAndInspectionDecodeAChunkAlike`.

  The inspector's parts are unchanged; they are phase 3's.
- `AltChunkHtmlReadingTest` is new, with 15 tests:
  - with jsoup alone: the decoder in Word's order, the CSS filter and the host-style rule, the
    rewrite (nodes moved, unwrapping, clamps, floats), the element limit, an `Error` caught, the
    timeout, and `convertAltChunks` going on past a chunk it cannot convert;
  - with the importer: css-1's cascade, css-2's and css-5's structure, host-2's style, encoding by
    `meta` and by a loaded chunk's declaration, an MHT chunk that is not well-formed, §1a's chunk with
    its labels, and `convertAltChunks` keeping hidden text (D8).
- `AltChunkNoFetchTest`: a stylesheet that `link` and `@import` name, served with
  `.fetched { display: none }`, hides nothing, and nothing is requested (D4).
- `TextsPhase2Test.altChunks` and `AltChunkHtmlWritingTest`'s round trip: the HTML chunk is expanded
  only with a reader.

Not tested: an importer without the option. The only one to hand was ImportXHTML's own target jar, and
its rebuild has replaced it. The path is a `NoSuchMethodException` that is caught and logged.

Measured through the reader, each with its guard, timings taken while the suite ran:
- 5,000 nested `div`s: 1.7 s;
- 20,000 floats: 1.9 s (7 s in phase 0);
- spans of 2^31−1, clamped to 1,000 by 65,534: 6.6 s;
- 60,000 paragraphs: refused by the element limit in 0.1 s.

### 12.4 The suite (pending WD 0.26)

Along the portfolio's 2a8eb26 map, which its answer on 20 October confirmed while WD 0.26 waits on
Jason:
- `html reader present` and `html reader absent` replace both `xhtml importer` scenarios;
- `ALT36_CHUNK_TEXT` and `CHK63_SCRIPTED` are gated on `html reader present`, as `ALT44_MHT_CHUNK`
  is. A gated row's default scenario runs reader-absent, and `altchunks: false` joins the
  supplemental scenario for both;
- the warnings: `html reader absent` gives 2 `text/html` in `extraction-probe.docx` (case 36's chunk
  and case 47's nested one) and 1 in `extraction-probe-inspect.docx`. Case 47's single nested warning
  now needs `html reader present`;
- the goldens are recut with no reader. Regenerated into a scratch copy, the diff was case 36's line
  alone, as the portfolio said it would be. The fixtures (`.docx`) are not regenerated.

Results:
- in the build: 1,565 checks passed, 18 pending (the reader-present rows);
- with the importer: 1,529 passed, 54 pending (the reader-absent rows, the goldens and the counts).

`ExtractionConformanceTest` accepts the counts pending for `html reader absent` when a reader is
present. Before, it failed whenever the importer was on the class path.

### 12.5 Office check

`~/vbShares/Office 2016/altchunk-fun/phase2/`, with a README (round 9). There are four pairs, each a
chunk for Word to import and docx4j's `convertAltChunks` of it:
- inline, class and span hiding;
- css-1's cascade;
- css-2's structure;
- XHTML that is not well-formed.

Run by Jason on 21 October 2026. Word opened all four converted files cleanly. His saves are in the
same directory (`-word365`), with screenshots. For each pair, a comparison of Word's import of the
chunk with its save of docx4j's conversion found:
- the same text in the same order;
- the same runs hidden, whether by `w:vanish` or a style: P1_INLINE_NONE, P1_CLASS_NONE, SPAN_NONE,
  P2_ID_HIDDEN, P2_DESCENDANT_HIDDEN, P2_IMPORTANT_HIDDEN, P2_BODY_STYLE_HIDDEN, the hidden row, cell
  and list item, P3_PLAIN_HIDDEN and P4_NONE;
- every other marker shown, including P3_BLOCK_SHOWS inside the hidden div;
- the same table: four rows of two cells, with P3_R3_B in the second column;
- the hidden list item kept in the list.

Formatting differs, as the README said it would: docx4j-ImportXHTML's styles and table borders, not
Word's HTML styles.

## 13. Phase 3 (21 October 2026)

Built on VERSION_17_3_4 after 1d108c0af, against the same ImportXHTML (468a760), on Jason's ruling the
same day: "the inspector can run the same HTML import that text extraction runs". The portfolio
carried it into WD 0.26 (e683d0c) with D9 (iii): REQ-901 lets an inspector run the import its
extraction runs, fetching nothing, changing nothing, REQ-907's limits on the chunk checked first, and
every chunk's location counts the HTML tree.

### 13.1 The reading

`InspectChunks` (package-private) reads an HTML, XHTML or MHT chunk for the inspector, after the tag
scan, which is unchanged and still finds the scripts and references:
- **no HTML reader** (jsoup and the importer, as the extraction needs): the chunk is skipped as
  `not_decoded`;
- **parsed** as the extraction parses it (`HtmlChunkImport.parsed`, which `tree` now calls): the
  decoder and jsoup. `HtmlChunks.paths` records each element's path in the tree jsoup built, from the
  document element down, each step its local name in lower case with its position among its siblings
  of that name. jsoup failing makes the chunk `unreadable`;
- **limited** before the rewrite and the import: the payload's bytes as before; the tree's elements,
  as parsed, counted against the inspection's 5,000,000 (past it the inspection stops, as for any
  walk); a tree over `HtmlChunkImport.maxElements` (D11) skipped as `limit.elements`;
- **rewritten** (D4, `HtmlChunkImport.rewritten`). The rewrite moves nodes, so the importer's keys,
  which are docx4j's own elements, find the paths recorded before it;
- **imported** as the text model imports it (`HtmlChunkImport.inspected`): a scratch package, never
  fetching, `setDisplayNoneAsHidden(true)`, guarded, the time limit applying. The importer's
  `getHiddenContent()` gives each outermost hidden element that produced content. An import that fails
  or is abandoned is `unreadable`. An importer without `getHiddenContent` (before 17.3.4) makes the
  chunk `not_decoded`;
- **kept**: the scratch package goes into `InspectRun.chunks`, by the chunk's part. The address
  reading (`InspectAddresses.record`) reads the chunk from it (`Extractor.chunks`, which replaces
  `noImporter`), and reads no chunk the inspector skipped. So an address is a block of the same import.

Each chunk's scratch package is held until the inspection ends, and each chunk costs an import, as it
does in the extraction. Nothing bounds the number of chunks but REQ-907's findings limit (each chunk is
an `altchunk` finding) and the total bytes. A document of thousands of small HTML chunks is slow to
inspect, as it is to extract; the time limit (D10) bounds each import, not their sum. Open.

### 13.2 The findings

- **`text.hidden`**, one for each key of `getHiddenContent()` whose content has text that is not all
  white space. The importer reports only the outermost hidden element, unless an element between
  them shows its content again (Word's rule, CR-002 D11). That is REQ-902's "one finding for the
  outermost such element", and a nested one inside a revealed block is its own:
  - location: the chunk's part, the element's path, and no range;
  - excerpt: the content's `w:t`, a run's paragraph the same as the last run's or else a line feed
    apart, a paragraph or table its paragraphs a line feed apart;
  - address: the block of the first paragraph that holds text (a run's paragraph, or a table's first
    recorded paragraph) in the reading under `hidden: true`;
  - needs `altchunks`, `hidden` and `html_reader`. Its reveal is `options: hidden: true; html reader:
    present`: §4 had `options: hidden: true`, but REQ-905 puts a chunk's content in an output only with
    the reader, and the reveal has named the capability since CR-050 (`InspectFinding`'s table);
  - visible: `no`.
- **`parsed`** (`InspectFinding.getParsed()`, `PARSED_HTML`): `html` on an HTML, XHTML or MHT chunk's
  `altchunk` finding once jsoup has parsed it, a chunk then skipped for a limit or a failed import
  included; absent where it was not decoded. WD 0.26 drops `parsed: xml`.
- The counts follow (REQ-903, REQ-905): the finding is omitted under the defaults and unseen under
  `hidden: true`, with the reader; without it, omitted. `Inspect.counts` imports each chunk for the
  inspection, beside the extraction's own import.

Choices, each where the WD or the CR left it open:
- "holds text" is text that is not all white space. ImportXHTML reports a white-space-only run, and
  the white space between a chunk's elements is no text a person reads. In the document itself any
  display text counts (REQ-902);
- an importer before 17.3.4 makes the chunk `not_decoded`, not undecided as §4a had it. The WD has no
  undecided reason for it, and REQ-902 gives the parser's absence `not_decoded` for the same reason:
  the inspector cannot tell what `display: none` hides;
- a chunk over D11's limit is `limit.elements`, as the portfolio proposes for WD 0.27.

### 13.3 Tests

- `AltChunkHtmlInspectTest` (new, 8):
  - jsoup alone (the build): the paths count jsoup's tree before the rewrite, including an unwrapped
    `article`, a moved body `style`, jsoup's `tbody` and an unclosed `p`; without a reader the chunk is
    `not_decoded`, with no `parsed` and its script and reference found;
  - with the importer: one finding for the outermost element, and an inner one where a block is
    revealed; no finding for an element with no text; the location under each parse (HTML repairs,
    well-formed XHTML with a DOCTYPE as HTML, an MHT's first part); the address against the extraction
    under `hidden: true` (a paragraph, a span, a cell, a list item); the counts; the D11 limit
    (`limit.elements`, parsed, not imported) and the time limit (`unreadable`).
- `AltChunkHtmlTest`: the HTML, XHTML and MHT reading tests assert the inspector in both
  configurations, with paths, addresses and reveals.
- `InspectTest`: the total-bytes test uses plain-text chunks, which are decoded and counted as HTML
  ones are. With a reader, an HTML chunk is imported too, and the total doesn't need the imports. The
  payload test's small HTML chunk is `not_decoded` without a reader.

### 13.4 The suite (pending WD 0.27)

Along the portfolio's answer of 21 October:
- **reader absent**: `inspect_oracle.py` skips each HTML, XHTML or MHT chunk as `not_decoded` after its
  scan, and its reveal table names `html reader: present`. Regenerated, then addressed by
  `ExtractionInspection`, only the status table moves, as §10.5 said:
  - `extraction-probe.docx` gains `not_decoded@|/word/afchunk.htm|` and `not_decoded@|/word/chunk44.mht|`;
  - `extraction-probe-inspect.docx` goes from `COMPLETE` to `PARTIAL` with `not_decoded@|/word/chunk63.htm|`.

  The whole-fixture comparison now needs `html reader absent`, as the counts already did.
- **reader present**: `extraction-probe-html.docx` (`ExtractionHtmlCases`, four chunks: A what 0.26
  rules on, B what is unruled, C d9's XHTML, D an MHT) with `expected-inspection-html.tsv` and its
  status table, read only under `html reader present`. The 18 rows are written by hand. Each cites
  its statements and the probe it rests on, and adds `parsed`, which is compared. Six were `pending`
  WD 0.27 and left out on both sides, until 0.27 (§13.6):
  - the revealed block;
  - the hidden cell, row and list item;
  - the MHT location;
  - `application/xhtml+xml`'s `part.unknown`.

  `ExtractionInspection.check` resolves each path in jsoup's tree of the fixture's chunk. The adapter
  computes a finding's groups with the class path's capability.
- the "pending WD 0.26" notes are now "pending WD 0.27": 0.26 carries REQ-901 and D9 only.

Results:
- full core-tests without the importer: 1,897 run, 0 failures, 26 skipped (phase 2's 20, and the six
  of `AltChunkHtmlInspectTest` that need the importer);
- with the importer, the text model's and the suite's tests and the altChunk writing tests: 158 run,
  0 failures, 3 skipped (those that need it absent);
- the suite: 1,565 checks passed and 19 pending in the build (the HTML fixture's inspection the new
  one); 1,526 and 64 with the importer, the HTML fixture's inspection passing, its six rows pending
  WD 0.27, and the four reader-absent inspections pending. After 0.27 (§13.6), 1,526 and 58: every
  row of the HTML fixture checked; the full core-tests again 1,897 run, 0 failures, 26 skipped.

### 13.5 Office check

`~/vbShares/Office 2016/altchunk-fun/phase3/`, with a README (round 10): the fixture, for Jason to
open and save as `extraction-probe-html-word365.docx`. The portfolio scans the save for effective
`w:vanish` and checks every row of the table against it.

Jason saved it the same day, with a screenshot; Word opened it cleanly. A scan of the save, the
effective `w:vanish` resolved through the styles, agrees with every row:
- A: `INLINE`, `CLASS`, `OUTER`, `NESTED` and `SPAN_HIDDEN` hidden; the rest shows;
- B: `REVEALED` shows and `STAYS_HIDDEN` is hidden. `CELL_HIDDEN` is hidden and keeps its column, so
  `CELL_SHOWN` is second. The hidden row's two texts are hidden. `ITEM_HIDDEN` is hidden and keeps its
  number: the same list, shown as 2.;
- C: `INLINE` and `AFTER` hidden, so Word read the XHTML as HTML;
- D: `FIRST_PART` hidden; `SECOND_PART` absent.

One structural detail, not a text finding: Word puts `w:trPr/w:hidden` on the row that holds the hidden
cell, as well as on the hidden row. docx4j-ImportXHTML marks only a hidden row.

### 13.6 WD 0.27

Jason approved draft 0.27 in the portfolio's session ("draft 27: good", 21 October; portfolio
804007d). It takes the questions of §10.5 and §10.7, and docx4j aligns with it:
- **the element count is as parsed** (REQ-513, REQ-907): `HtmlChunkImport.tree` and the inspector
  count before the rewrite, which unwraps the elements Word does not know. Phase 2 counted after it.
  `AltChunkHtmlReadingTest` has a chunk of 22 elements as parsed, 8 unwrapped;
- **the decoder has no XML-declaration step** (the Chunks rule): Word refuses a chunk with a
  declaration, so a declaration names nothing. `HtmlChunks.decode`, the extraction's and the
  inspector's, reads only a `meta`. The oracle's `decode_html` follows the rule in full: a `meta`
  alone, [ENCODING]'s labels for a `meta` and a MIME charset, and windows-1252 where the bytes are not
  UTF-8. The UTF-16 step stays, unmeasured, as the WD's NOTE says;
- **the host-style map is the WD's**: `p`, `h1` to `h6` and `address`. Phase 2 also had `pre`,
  `code`, `kbd`, `samp`, `var`, `cite`, `dfn`, `acronym` and `tt`, by inference;
- **`application/xhtml+xml` is a known family** (`InspectWalk`, the oracle): an XHTML chunk is no
  `part.unknown`;
- the suite's "pending WD 0.27" notes cite 0.27. The HTML table's five pending rows are checked, and
  C's `part.unknown` row is gone, leaving 17.

Still docx4j's own, and sent to the portfolio, both unmeasured in Word:
- docx4j drops `*`, pseudo-elements and two ids on one element;
- docx4j drops the rules inside `@supports`, `@layer`, `@container` and `@document`. 0.27 excludes
  `@media` alone;
- docx4j keeps the selectors of a list that Word applies, where 0.27's "a rule whose selector has"
  could mean the whole rule goes.

A probe would settle them.

### 13.7 Round 11 (21 October 2026)

At the portfolio's ask: what 0.27 left unmeasured, in `~/vbShares/Office 2016/altchunk-fun/round11/`
(14 files, `build11.py` among the phase 0 drivers). Each case is its own chunk, and a case that turns on
the host's styles has its own host. Jason saved all 14. All opened cleanly but `r11-9`, which Word
repaired. Word's answers:
- **a selector list** with one selector Word ignores (`div > p`) loses the whole rule, in either order.
  docx4j kept the other selectors, so it changes;
- **`*`** counts for nothing (`.w *`, `div *`), as docx4j has it;
- **an at-rule's block** is ignored, `@supports`, `@layer` and an unknown `@foo` as well as `@media`. The
  rule after the block applies. docx4j already does this;
- **`p.k`** hides the `p` and not a `div` of the class, as docx4j has it;
- **the host's styles**:
  - a defined style counts though no paragraph uses it;
  - a latent style alone does not;
  - an `h1` rule goes through Heading 1, as `h6`'s through Heading 6;
  - a class matches a style's name in its exact case: against a host's `HID`, `.hid` hides, Word making a
    style `hid` of its own. docx4j matched case-insensitively, so it changes;
- **`pre` and `code`** go through HTML Preformatted and HTML Code: a host defining them wins. docx4j cut
  them for 0.27 (§13.6), so they come back;
- **a rule's other properties** go with its `display` where the host's style wins: `R11_7_P` is not red;
- **a hidden `tfoot`'s** text is hidden. Word keeps it where the source has it, before the body.
  docx4j-ImportXHTML lays it out last: openhtmltopdf's `BoxBuilder.reorderTableContent` moves the first
  header and footer group. ImportXHTML's CR-003 (`docs/change-requests/CR-003-table-row-groups-in-source-order.md`,
  accepted by Jason the same day, phase 1 committed as docx4j-ImportXHTML dd7b913) adds
  `setTableRowGroupsInSourceOrder`, off by default, which docx4j sets
  for chunks (§13.8);
- **a UTF-16LE chunk with no byte order mark** is not read as UTF-16: Word read the bytes as 8-bit text
  and repaired the document. The Chunks rule's UTF-16 step is not Word's.

docx4j's changes (the selector list, the case, `pre` and `code`, the UTF-16 step) wait for WD 0.28,
which follows Word, and land in their own commit.

### 13.8 WD 0.28

Jason approved 0.28 the same day (portfolio f2046e5): round 11's answers as rules. docx4j follows:
- **`HtmlChunks.css`**: a rule whose selector list has any selector Word ignores (`applied`: a child or sibling
  combinator, an attribute selector, a pseudo-class or pseudo-element, `*`, two classes or ids on one element)
  goes whole. A rule inside any at-rule goes, `@font-face`, `@page`, `@charset` and `@namespace` excepted, which
  hold no rule;
- **the host's styles** (`lostToHost`): a class matches the style named exactly as the class, case included; a
  built-in style its name in any case. `hostStyles` keeps the names as written. The map gains `pre` (HTML
  Preformatted) and `code` (HTML Code, a character style);
- **the decoder**: no UTF-16 step. A chunk in UTF-16 with no byte order mark is read as its bytes say, as Word
  reads it: its markup comes out as text (`r11-9`). The oracle's `decode_html` likewise;
- **a table's row groups** in source order: `HtmlChunkImport` sets docx4j-ImportXHTML's
  `setTableRowGroupsInSourceOrder` (its CR-003) by reflection, beside `setDisplayNoneAsHidden`. An importer
  without it lays a `tfoot` out last.

**Round 12** (21 October 2026, built here for ImportXHTML's CR-003 §4, Jason's choice;
`altchunk-fun/round12/`): six cases of a table's row groups, each its own chunk. Word 365 keeps source order in
every one: a visible `tfoot` before the body, a `thead` after it, two `tfoot`s, two `thead`s between bodies, a body
styled as a footer group, bare rows around a `tfoot`. docx4j through the setter gives the same order, row for row.
Word marks every `thead` row `w:tblHeader`, wherever it stands (CR-003's D3 records it).

Tests: `AltChunkHtmlReadingTest` (`cssFollowsRound11`, the host rule's case and map, the decoder,
`tfootStaysWhereTheSourceHasIt`); `InspectTest`'s UTF-16 chunk without a byte order mark holds no script now.
Round 11 through docx4j with the importer agrees with Word's saves on all 94 markers, and `r11-9` gives its
markup as text, as Word's save does. With the importer, 160 run, 0 failures; the suite, 1,526 passed and 58
pending, as before.

## 14. Phase 4: hand-offs (21 October 2026)

Jason: "start CR-053 phase 4". Sent the same day:
- **docx4j-ImportXHTML**: §4a was its CR-002 (468a760, phases 1 to 3), and r11-8 its CR-003 (dd7b913,
  `setTableRowGroupsInSourceOrder`), both answered already; their release is 17.3.4, waiting on Jason.
- **The portfolio**: REQ-901's relaxation and the MHT location went into WD 0.26 to 0.28 (e683d0c, 804007d,
  f2046e5), with the suite rows and the registry; nothing more is owed.
- **docx4j-mcp**: `html_to_docx`'s `mode: altchunk` adds an `Xhtml` chunk of jsoup's XML output, which has no
  declaration (checked with jsoup 1.22.1), so it opens in Word on its pinned docx4j 17.1.0. No change is needed.
  The message gives it the strip and `AltChunkHtml` for any other write, D6, Word reading XHTML as HTML, and
  what 17.3.4's text model and inspector do with a chunk where jsoup and ImportXHTML are present, as they are
  there (expanded, `display: none` hidden).
- **docx4j-python and docx4j-core-ts**: neither adds an HTML chunk or extracts text yet. The messages set out
  docx4j as the oracle under WD 0.28: the writing rule (D1, D2, D6), REQ-513's reader, decoder and hiding rule,
  the inspector's `not_decoded` without a reader and `text.hidden` with one, the 50,000-element limit, and the
  suite's fifth fixture and harness (`htmlInspections`, `Run.pend`). Their rows are "html reader absent".

Acceptance: each acknowledged. So far:
- **the portfolio** (21 October, registry 4c7ef08): its part done. WD [PROBE] still cites 4a8833eea; 198d015a3's
  oracle change goes in at its next revision, nothing a port compares against having moved;
- **docx4j-core-ts** (21 October): recorded in its CR-012 §13.7 for its phase 5e. Nothing changes there now: it
  writes no chunk (its html module converts XHTML), and its `AlternativeFormatInputPart` keeps the declared
  content type, so D6 holds. Its runner has `htmlInspections` and `pend`, and its suite copy is at 198d015a3.
  Its 1,584 rows equal this run's, in order.
- **docx4j-python** (21 October): nothing changes now; it adds no chunk and has no extractor or inspector yet. Its
  proposed text model (CR-011, 723ac0a) records REQ-513's reader rule, "html reader absent" and the decoder, and
  asks its user (Q12) whether html5lib, an [HTML]-algorithm parser, comes as an optional extra; its earlier plan's
  lxml.html is not one.
- **docx4j-mcp** (21 October): recorded in its CR-mcp-server.md §19 and §20. Its jsoup (1.18.3, pinned) writes no
  declaration either. Applying `enc-6` to its own output, it found a defect of its own (§20): `normalise` keeps an
  input's `meta charset="windows-1252"` while writing UTF-8, which Word then misreads, and `input_path` is read
  as UTF-8 alone. Its fix decodes in Word's order and rewrites the `meta`. For the text model: with jsoup and
  ImportXHTML present it is "html reader present" on 17.3.4, so its goldens with a reader are its phase 2's,
  and it sets `timeoutMillis` (its T7).

All acknowledged: phase 4 done.
