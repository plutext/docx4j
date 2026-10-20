# CR-053: HTML, XHTML and MHT altChunks, written as Word opens them and read as Word reads them

Status: ACCEPTED 2026-10-20 (Jason: "accept the recommendations and commit the CRs"), D1 to D10
as recommended. Phase 0 done (Jason: "start phase 0"): tests, design checks and Word
probes, rounds 5 to 7 (§10). D9 is (iii), D2 stands, D11 is decided (50,000) and D4's rewrite
(§10.7) is approved; phase 1 may land alone. Proposed 2026-10-20 (Jason: "lets start
the altchunk work"), reviewed the same day by docx4j-64 (§9). It gathers his ruling on adding a
chunk (strip the XML declaration, 2026-10-20) and what the text extraction WD 0.23 to 0.25 asks of
docx4j (portfolio 6cdb966, 016963e, 1865f10). Drafted by Claude Opus 5.5 (session docx4j-25), with
§1 measured first. Phases in §5; decisions in §6.

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
