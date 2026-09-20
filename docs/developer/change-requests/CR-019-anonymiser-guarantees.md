# CR-019: What `docx4j-docx-anon` can guarantee — the docx gaps, then pptx, then xlsx

Status: IN PROGRESS — phase 1 (docx) DONE 2026-09-21 on VERSION_17_2_0
(implementation record in §"Phase 1 record" below; the Word-open check is
Jason's, files on the share `fidelity/cr019/`); phases 2 (pptx) and 3 (xlsx)
proposed. Requested by the portfolio session working on the Docx4j Enterprise
price list (`Plutext-Enterprise-Java-11/pricing/`, not yet in git), whose
confidentiality clause and two support-tier rows depend on what the anonymiser
actually does; docx4j-mcp's CR-004 lists an `anonymize` tool in its phase 4.
The module's own reading below was re-checked against `docx4j-docx-anon` on
VERSION_17_2_0 (b5c2f417f and later) on 2026-09-16. Owner: Jason Harrop.
Drafted with Claude Fable 5.1; phase 1 implemented with Claude Opus 5.

Scope: `docx4j-docx-anon` (`org.docx4j.anon`: `Anonymize`, `ScrambleText`,
`DmlVmlAnalyzer`, `PartsAnalyzer`, `AnonymizeResult`), a CLI, a test corpus, and
new pptx and xlsx anonymisers. Not in scope: the wording of the price list, which
is the requester's; this CR says what the tool will guarantee so the wording can
say no more than that.

## Background

### Why now

The price list will carry a clause along the lines of: "We use AI-assisted tools
in development and support, under agreements that prohibit training on your
data. Where we can, we anonymise documents before any AI service sees them." Two
rows of its support-tier table depend on the same tool: "Diagnosis of your broken
docx/pptx/xlsx" (all three formats, at every tier) and "Your documents
(anonymised, text scrambled) in our pre-release regression tests" (up to 100
documents at Platinum, retained). The promise is about to be made in writing for
three formats; the tool covers one, with gaps.

### What the module does today (read 2026-09-16)

- `Anonymize(WordprocessingMLPackage)` — docx only. `go()`: `filterMDPRels()`
  removes the customXml, glossaryDocument and stylesWithEffects relationships
  from the main document part; `handleMetadata()` clears docProps core
  (creator, title, subject, keywords, description, category, identifier) and
  extended (company, manager, headingPairs), removes `DocPropsCustomPart` and
  `/docProps/thumbnail.emf`; `PartsAnalyzer.identifyUnsafeParts` classifies
  every part; `detectDmlVmlContent()`; then `ScrambleText` over the main
  document, headers, footers, footnotes, endnotes and comments; images replaced
  with 2x2 pixels (png, gif, jpeg, bmp, tiff). `AnonymizeResult` reports unsafe
  parts and objects and the scripts detected.
- `ScrambleText` scrambles `w:t`, `w:delText`, DrawingML `CTRegularTextRun`
  and VML `CTTextPath` text per Unicode range so the font coverage of the
  output matches the input; MERGEFIELD names in `w:instrText` are replaced
  (`ScrambleText` 139-170; "TODO others eg REF" at 174); **form-field names
  are renamed** to `fieldname<n>` (189-206: the commented-out code is an
  earlier scheme that scrambled them per range, not an omission); **bookmark
  names are renamed** to `bm<n>` (215-231, likewise); a content control loses
  its `w:dataBinding` and `w:tag` (243-248) but keeps its `w:alias` and its
  `w:placeholder`'s document part reference.
- `PartsAnalyzer` flags `PeoplePart`, the chart and diagram parts, ActiveX,
  bibliography, the PresentationML parts and others as unsafe; `Anonymize`
  removes some (customXml, OpenDoPE parts, docProps custom) and leaves the rest
  in the package with the flag in the result.
- **There is no test source tree in the module** (`docx4j-docx-anon/src/test`
  does not exist), so nothing today proves that any original text is gone.
- The comment on `go()`: "TODO: Info leakage via external parts eg hyperlinks".

### The requester's reading, checked

| # | claim | code | verdict |
|---|---|---|---|
| 1 | author identities survive: `w:comment/@w:author`, `@w:initials`, revision authors on `w:ins`/`w:del`/`w:moveFrom`/`w:moveTo` and the `*Change` elements, `CommentsExtendedPart`, `people.xml` | no `setAuthor`/`getAuthor`/`getInitials` anywhere in the module; `PartsAnalyzer` 156 flags `PeoplePart` only | **confirmed** — and since CR-018 the typed `CommentsExtensiblePart` (durable ids, UTC dates) and the w15 `commentsEx` part are further comment metadata |
| 2 | hyperlink targets survive; HYPERLINK field instructions untouched | the `go()` TODO; `ScrambleText` handles only MERGEFIELD in `w:instrText` | **confirmed** |
| 3 | bookmark and form-field names not scrambled; content-control tag/alias to check | renamed to `bm<n>` / `fieldname<n>`; tag and binding removed, alias kept | **partly wrong**: names are neutralised already; the alias is the gap |
| 4 | `settings.xml` untouched (`w:mailMerge` data source and query, `w:docVars`) | no reference to `DocumentSettingsPart` in the module | **confirmed** |
| 5 | media beyond five raster types not replaced (EPS, EMF/WMF, OLE binaries, altChunk, VBA) | the image handling names png/gif/jpeg/bmp/tiff; `PartsAnalyzer` knows `ImageEpsPart`; nothing replaces OLE, altChunk or VBA parts | **confirmed** |
| 6 | chart and diagram parts flagged, not scrubbed, including embedded workbooks | `PartsAnalyzer` 63-74 adds them to `unsafeParts` | **confirmed** |
| 7 | the result is advisory, not fail-closed | `AnonymizeResult` carries sets; `go()` returns it and saves regardless | **confirmed** |
| 8 | no verification that original text is gone, no test corpus, no CLI | no test tree; no `main` | **confirmed** |
| pptx | not supported | the constructor takes `WordprocessingMLPackage` | **confirmed** (the `PartsAnalyzer` PresentationML cases classify only) |
| xlsx | not supported | as above | **confirmed** |

## Gaps

1. **Identity.** Comment authors and initials, revision authors and dates on
   every tracked-change element, `people.xml` (author, presence provider and
   user id), `commentsEx`/`commentsIds`/`commentsExtensible` metadata, and the
   docProps `lastModifiedBy` (core: `lastModifiedBy` is not in the cleared
   list). In legal and banking documents these names are the most sensitive
   content in the file, and they are the one thing scrambled text does not
   touch.
2. **External targets.** Relationship targets of `TargetMode="External"` in
   the document, headers, footers, notes and comments (hyperlinks, linked
   images, linked OLE), `HYPERLINK`/`INCLUDEPICTURE`/`INCLUDETEXT`/`REF`/`DOCPROPERTY`
   field instructions, and the `w:hyperlink/@w:tooltip`.
3. **Settings and properties.** `w:mailMerge` (data source, connect string,
   query, header source), `w:docVars`, `w:attachedTemplate`'s relationship,
   `w:writeProtection`/`w:documentProtection` hashes and salts (not secret,
   but not the customer's to give away either), custom XML data (removed
   today) and content-control aliases.
4. **Non-raster media and embedded objects.** EPS, EMF/WMF (which can carry
   text as glyph runs, see CR-011), OLE object binaries (`oleObject<n>.bin`,
   which are whole Excel or Visio files), `altChunk` parts (whole HTML or docx
   files), VBA projects, embedded fonts (a subset reveals the glyphs used),
   `docProps/thumbnail.*` in formats other than emf.
5. **Charts and diagrams.** Chart parts carry the plotted numbers, category
   labels and titles as text, plus an embedded workbook; SmartArt data parts
   carry the text. Today they are flagged and left.
6. **Fail-open result.** The caller saves whatever came out; nothing forces a
   decision on the flagged parts.
7. **No proof.** No test corpus, no check that no original word survives, no
   CLI, so support cannot run it without writing code and cannot show it ran.
8. **pptx and xlsx** are not supported.

## Design

**The guarantee, stated as the tool will enforce it.** A document that
`Anonymize` returns with `result.isClean()` contains: no text from the
original in any text-bearing part (scrambled per Unicode range, as today);
no author, initials, user id or presence information; no external target or
field instruction argument; no metadata beyond what the package needs to
open; no media but placeholder pixels; no embedded object, chart data,
diagram text, macro, custom XML or altChunk. What it keeps, by design, is the
document's *structure* — paragraphs, runs, tables, sections, styles,
numbering, fonts, sizes, the shape of fields and content controls — because
that is what a layout, a corruption or a converter bug lives in. Where the
tool cannot make a part clean it removes the part and records it, or (in
`keep` mode) keeps it and refuses to call the result clean.

**Fail-closed by default.** `Anonymize.go()` returns an `AnonymizeResult`
whose `isClean()` is the single answer; `Anonymize.Mode.STRICT` (default for
the CLI and for docx4j-mcp) removes every part it cannot scrub, `Mode.KEEP`
keeps them and reports. The report is machine-readable (JSON) and lists, per
part, what was done: scrambled / cleared / removed / kept-unsafe, with the
reason.

**One walk, shared across formats.** The scrambling of text per Unicode
range is `ScrambleText`'s and stays; what changes is who feeds it. A
`TextBearing` visitor per format (WML today, PML and SML new) enumerates the
parts and the elements holding text, identities, targets and instructions;
the same `ScrambleText`, `MetadataScrubber` (docProps, settings, people) and
`MediaReplacer` serve all three. `PartsAnalyzer`'s classification becomes
the table the walk consults: per part class, one of scramble / clear /
replace / remove / keep-unsafe.

**Verification.** `Verify.noOriginalText(original, anonymised)`: extract
every text run, every attribute value the walk knows about, every
relationship target and every string in every XML part of the *anonymised*
package, and assert none of them (tokens of three or more letters) appears
in the original's corresponding extraction — the test corpus proves it on
documents built to contain each gap above (a probe per gap, in the style of
`docx4j-layout-fidelity`'s `Corpus.java`: generated, not customer files) and
on the anonymised form of a few real documents (checked in only after the
tool itself has cleaned them and the check has passed). `AnonymizeResult`
records that verification ran.

**CLI.** `java -jar docx4j-docx-anon.jar in.docx out.docx [--keep] [--json report.json]`
(and pptx/xlsx by extension once phases 2 and 3 land), exit code 0 only when
`isClean()`; the same entry point docx4j-mcp's `anonymize` tool calls.

**The workflow the price list describes.** The tool's default path is the
anonymised one; the report says exactly what was removed or kept, so support
can see whether the reported problem could survive anonymisation
(a broken table, a numbering bug: yes; a corrupt embedded object, a font
subset problem: often no) and ask the customer for the original only then,
with the report as the reason.

## Plan

### Phase 1 — docx: close the gaps, prove it, ship the CLI

1. Identity: clear `w:author`/`w:initials`/`w:date` on comments and on every
   tracked-change element (`w:ins`, `w:del`, `w:moveFrom`, `w:moveTo`,
   `w:rPrChange`, `w:pPrChange`, `w:sectPrChange`, `w:tblPrChange`,
   `w:trPrChange`, `w:tcPrChange`, `w:tblGridChange`, `w:numberingChange`,
   `w:cellIns`/`w:cellDel`/`w:cellMerge`), replacing authors with `Author <n>`
   consistently so the review structure survives; `people.xml` authors and
   presence info; `commentsEx`, `commentsIds` and `commentsExtensible` kept
   (they carry ids and dates, no names) with dates normalised; docProps
   `lastModifiedBy`, `revision`, dates.
2. External targets and instructions: every `TargetMode="External"`
   relationship in every part gets a placeholder target (`http://example.invalid/<n>`)
   and the tooltip is cleared; `HYPERLINK`, `INCLUDEPICTURE`, `INCLUDETEXT`,
   `REF`, `DOCPROPERTY`, `DOCVARIABLE`, `ASK`, `FILLIN` instructions keep the
   keyword and switches and lose their arguments (the MERGEFIELD pattern,
   generalised); `w:fldSimple/@w:instr` the same.
3. Settings and properties: `w:mailMerge` removed, `w:docVars` removed,
   `w:attachedTemplate` relationship removed, protection hashes cleared,
   content-control alias scrambled.
4. Media and objects: EMF/WMF/EPS replaced by a placeholder of the same
   class (a 2x2 raster where the consumer allows it, else removed with the
   drawing left pointing at the placeholder); OLE binaries, altChunk parts,
   VBA projects, embedded fonts and every thumbnail removed with their
   relationships (STRICT) or kept and flagged (KEEP).
5. Charts and diagrams: chart parts have their string caches, titles and axis
   titles scrambled and numeric caches replaced by a fixed series (the
   *shape* of the chart survives), embedded workbooks removed; SmartArt data
   parts scrambled.
6. `AnonymizeResult.isClean()`, the modes, the JSON report; `Verify`; the
   probe corpus under `docx4j-docx-anon/src/test` (the module gets a test
   tree); the CLI `main`.

### Phase 2 — pptx

`Anonymize(PresentationMLPackage)`: text in slides, layouts, masters, notes
slides and notes masters (`a:t` in every `a:p`, through `DmlVmlAnalyzer`'s
DrawingML path which already knows `CTRegularTextRun`); comments and
comment-authors parts (text, `name`, `initials`, `userId`); tags parts; chart
and embedded-workbook parts as in phase 1.5; media and OLE as in 1.4;
`docProps`; slide-level and presentation-level external relationships;
speaker notes; `presProps`/`viewProps` (harmless, kept). Same result, same
verification, same CLI by extension.

### Phase 3 — xlsx

`Anonymize(SpreadsheetMLPackage)`: shared strings and inline strings
scrambled; **numeric cells: a decision** (Decisions 3) — the default replaces
every numeric value with a fixed value per cell type so column widths and
formats survive, an option keeps numbers; formula literals (string and number
constants inside `<f>`) scrambled, cached values treated as cells; defined
names renamed with the references kept; comments (text and authors) and
threaded comments; pivot cache definitions and records removed (they are
copies of source data); query tables, connections and external links removed
(connection strings can carry credentials); table parts' names scrambled;
headers and footers scrambled; charts as in phase 1.5; `docProps`; the
workbook's external relationships.

Each phase is its own commit series with the test corpus growing alongside;
`docx4j-core-tests` is not affected. Nothing renders differently, so the
fidelity corpora are not involved.

## Decisions (for Jason)

1. **Fail-closed by default** (STRICT removes what it cannot scrub; KEEP is
   opt-in and never reports clean). Recommended, because the promise in the
   price list is only honest if the default path is the safe one.
2. **Authors become `Author <n>`, consistently**, rather than being deleted:
   the review structure (who replied to whom, which changes are one
   reviewer's) is often the bug being diagnosed. Recommended.
3. **xlsx numbers replaced by default**, kept by option: the numbers are
   usually the confidential part of a spreadsheet, and a layout or corruption
   bug rarely depends on their values; the option exists for the bug that
   does (a number format, a formula result). Recommended.
4. **Real documents in the test corpus only in their anonymised form**, and
   only after `Verify` has passed on them, per the existing rule that
   customer and corpus documents are never committed; the generated probes
   carry the coverage.
5. **Where the module lives**: `docx4j-docx-anon` keeps its name and gains the
   two packages, or is renamed `docx4j-anon`; recommended: keep the name for
   17.2.0 (artifact ids are what users depend on), rename at the next minor
   if at all.


## Phase 1 record (2026-09-21, commit 27515ece3)

### What shipped

All in `docx4j-docx-anon`, package `org.docx4j.anon`; the module gained a test
tree (`src/test`, 31 tests) and a `Main-Class` manifest entry.

| class | role |
|---|---|
| `Anonymize` | the orchestrator; `Mode.STRICT` (default) / `Mode.KEEP`; `setVerify(boolean)` (on by default); `go()` returns the result |
| `AnonymizeResult` | `isClean()`, `getActions()` (per part: SCRUBBED / CLEARED / REPLACED / REMOVED / KEPT / KEPT_UNSAFE with a reason), `getNotes()` (element-level removals), `getLeaks()`, `getVerified()`, `toJson()`, `summary()`; `isOK()` deprecated to `isClean()` |
| `PartsAnalyzer` | the table: `classify(Part)` → SAFE / SCRUB / METADATA / REPLACE_IMAGE / REMOVE / UNSCRUBBABLE; `identifyUnsafeParts` deprecated, kept |
| `JaxbGraphWalker` | a reflective walk over every object a JAXB part holds (lists, wrappers, every generated class; DOM nodes handed to the visitor), pre-order, each object once, with REMOVE and REPLACE by list or setter — see "departures" |
| `Placeholders` | the labelled "OLE object removed" image (java.awt, 480x240, scaled by Word to the object's extent) and the altChunk marker paragraph |
| `ScrambleText` | now a walker visitor: w:t, w:delText, instructions (via `FieldInstructions`), m:t, a:t and a:fld, VML text paths and shape alt/title/href, chart string and number caches and formulas (c: and cx:), chart headers/footers, pivot source names, content-control aliases/list items/placeholders, form-field defaults/entries/help/status text, smart-tag and customXml attributes, drawing names/descriptions/titles, custom style names and aliases, level text, theme/colour/font/format scheme names, diagram placeholder text, DOM t/v/f text and name-like attributes |
| `MarkupScrubber` | the second visitor: every `CTTrackChange` (author → `Author n`, date → fixed, dateUtc dropped), move bookmarks, comment initials, people (author, presence provider "None", userId → the person's name, contact dropped), commentsExtensible dates; bookmark and form-field names → `bm<hash>`; hyperlink tooltip/docLocation cleared and anchors mapped; sdt tag and data binding removed, date-picker values fixed; fldData removed; custom style ids → `s<hash>` at the definition and every reference (pStyle, rStyle, tblStyle, basedOn, next, link, numbering pStyle/styleLink/numStyleLink); settings: mailMerge, docVars, every `CTRel` (attached template, printer settings, data sources) removed, protection hashes/salts/crypt attributes cleared with the protection kept, w14/w15 docIds reset; STRICT only: w:altChunk, o:OLEObject, w:control, chart externalData and geography caches, font-table embed elements removed |
| `MetadataScrubber` | docProps core (everything descriptive, lastModifiedBy, lastPrinted, version; created/modified → the fixed date; revision → 1) and app (company, manager, template, heading pairs, titles of parts, hyperlink base and list, digital signature, total time), cover-page props; every `TargetMode="External"` relationship → `http://example.invalid/<n>` |
| `MediaReplacer` | raster images → 2x2 PNG in place (as before); EMF/WMF/EPS/JPEG XR/WebP/broken images → the part removed and every relationship retargeted to one shared placeholder PNG (`/word/media/anon-placeholder.png`); SVG → a blank 2x2 SVG; `removePart(pkg, part)` removes a part with every relationship to it, its own targets, and its content-type override |
| `FieldInstructions` | keyword and switches kept, format-switch pictures kept, bookmark arguments (REF/PAGEREF/NOTEREF/ASK/SET/SEQ, HYPERLINK `\l`, TOC `\b` `\c`) mapped, STYLEREF built-in names kept, formulas keep functions and cell references and map plain identifiers, form-field instructions untouched, every other argument scrambled (letters only, so paths and switch values keep their shape) |
| `Names` | the shared renamings: authors/initials in order of first appearance, `bm`/`s` + hash (stable across parts, no pre-pass), built-in style names from KnownStyles.xml plus the document's own latent-style list, `FIXED_DATE` = 2000-01-01T00:00:00Z |
| `Verify` | before/after token extraction (every text node, the attribute pairs in `Verify.ATTRIBUTES`, every external target) and the comparison less the tool's own vocabulary (lorem fragments, field keywords, date/number pictures, built-in style words where style names appear, a dozen fixed words) |
| `AnonymizeCli` | `in.docx out.docx [--keep] [--json report.json] [--no-verify] [--no-fonts]`; exit 0 only when clean, 1 not clean, 2 usage, 3 failure; sets an `IdentityPlusMapper` unless `--no-fonts` |
| `DmlVmlAnalyzer` | unchanged in role (the inventory: fields present, VML, objects of interest); math and non-picture graphic data are inventory now, not unsafe, since the walk reaches their text |

Test corpus: `src/test/resources` holds nine docx4j-owned documents (README
there says what each exercises: chart with embedded workbook, comments with
people/commentsExtensible/Ids/Extended, an SVG, a sensitivity label, tracked
changes with UTC dates and OMML, an OLE object with its EMF preview, legacy
form fields, a VML text box, an embedded font, MERGEFIELDs) and one EMF;
`AnonymizeProbesTest` builds one document per gap (identity; external targets
and instructions; settings and content controls; media and objects, STRICT and
KEEP; diagram data; custom styles, numbering, digits and Cyrillic) and checks
the planted words against every XML and relationships part of the output;
`AnonymizeCorpusTest` runs STRICT over the corpus (clean, verified, reloads)
plus the per-document specifics; `FieldInstructionsTest` and `AnonymizeCliTest`
are what they say.

### Departures from the plan, and why

- **A reflective walk instead of TraversalUtil.** TraversalUtil's children
  rules are written for the content tree; they do not reach paragraph and run
  properties (w:rPrChange and w:pPrChange carry authors), settings, chart
  caches, diagram data, or DrawingML `handleGraphicData` does not list. The
  guarantee needs every object, so `JaxbGraphWalker` walks every public getter
  (lists first, so a convenience getter such as `SdtPr.getTag()` cannot hide a
  list member from a removal). `ScrambleText` no longer extends
  `TraversalUtil.CallbackImpl` (API change, noted for the CHANGELOG).
- **Digits are randomised** (each digit becomes another digit). The plan said
  "scrambled per Unicode range, as today", and today kept ASCII digits, which
  is where account numbers, amounts and dates live. Width class survives.
- **Latin-1 Supplement letters are randomised within their case block**
  (until now kept, so the accented letters of a French or German word stayed
  in place). Upper-case ASCII letters now stay upper-case.
- **Verification is built into `go()`**: the tokens are extracted before any
  change and again at the end, so the caller need not keep the original;
  `setVerify(false)` skips it. The lorem-fragment rule (an output Latin word
  is always a fragment of one lorem word, so such fragments cannot be told
  from a leak) is the check's stated blind spot; the tokens in it carry
  nothing.
- **Bookmark, form-field and sequence names hash** (`bm<hash>`) rather than
  count, so a REF in a header matches a bookmark in the body with no pre-pass,
  and the mapping is the same in every part.
- **Custom style ids are renamed too** (`s<hash>`), not only the names: an id
  like `AcmeBodyText` is text. Built-in styles keep name and id (Word
  identifies them by name); built-in = KnownStyles.xml ∪ the document's own
  `w:lsdException` names.
- **Non-raster images are retargeted, not overwritten in place**: a PNG under
  an `.emf` name relies on Word sniffing bytes through the metafile path,
  which nothing proves; a shared placeholder PNG part is what every raster
  path already reads.
- **A removed object leaves a footprint that says what it was** (Jason,
  Word check round 1, 2026-09-21: "replace with text saying [OLE object
  removed]?"). Page flow is what the anonymised document is for, so an OLE
  object or ActiveX control keeps its picture at its own size, but that
  picture becomes a generated image labelled "OLE object removed by
  docx4j-docx-anon" (`Placeholders`, `/word/media/anon-object-removed.png`;
  `MarkupScrubber` records which picture relationships stood for an object,
  `MediaReplacer` gives those the labelled image instead of the 2x2 pixels).
  An altChunk has no footprint of its own and becomes the marker paragraph
  "[altChunk removed by docx4j-docx-anon]" (the walker gained a REPLACE
  action for it). The markers' words are in `Verify`'s vocabulary.
- **Also removed, not in the plan's list**: sensitivity labels
  (`/docMetadata/`), digital signatures (`/_xmlsignatures/`), the
  bibliography part (names and titles), printer settings, ink, web
  extensions, chart geography caches; theme/colour/font scheme names and the
  w14/w15 document ids are scrambled/reset; the docProps `TitlesOfParts`
  (the document's headings) and `HLinks` are cleared.
- **Dates become one fixed instant** rather than being shifted: the ordering
  of revisions is lost; simpler, and the report says so.

### Decisions taken (from the list above)

1 fail-closed default: taken (STRICT). 2 `Author <n>`: taken (initials `A<n>`;
people.xml user ids become the person's name). 3 (xlsx numbers): phase 3.
4 corpus of generated probes plus docx4j's own files only: taken. 5 module
name kept: taken.

### Findings on the way

- `strict-smartart.docx` (docx4j-samples-docx4j/sample-docs/strict) would not
  load: its styles part failed docx4j's strict→transitional preprocessing
  (`NumberFormatException: "12.95pt"`) — and so did every strict document
  Word saves, since the value is Word's docDefaults. **Fixed in 7892cb501**
  (mc-preprocessor.xslt converts every WordprocessingML point value; new
  `StrictLoadTest` in docx4j-core-tests over the four strict samples), and
  the document is back in the anonymiser's corpus (5b97306e8). The
  anonymiser still handles an unreadable part as it should (STRICT removes
  it and records why; KEEP keeps it unsafe), and its font selection degrades
  to no glyph check when the styles part is missing. Word check round 2 then
  found the same converter's second gap — strict text boxes (`wp:wsp` and its
  children) left in the transitional `wp` namespace, which Word refuses —
  **also fixed** (the preprocessor maps them to `wps:`/`wpg:`/`wpc:`).
- `FontTablePart.processEmbeddings` (run when a font mapper is first asked
  for) logged an NPE if an embedded font relationship was gone
  (`RelationshipsPart.getPart(String)` dereferenced a missing relationship
  in a debug line), and the NPE aborted the registration of every later
  embedded font. **Fixed in 7892cb501** (null returned with a warning; the
  font is treated as not embedded); the visitors (and with them the font
  selector) are still created before any part is removed.
- On a box without Windows fonts, `IdentityPlusMapper` maps none of Aptos,
  Arial or Times New Roman, so the non-Latin glyph check is a no-op there; the
  scramble still stays in the character's Unicode block.

### Gate

| step | result |
|---|---|
| `docx4j-docx-anon` compiles (JPMS module, `requires java.xml` added) | BUILD SUCCESS against the installed 17.2.0-SNAPSHOT core |
| module suite | 32 tests, 0 failures (`AnonymizeProbesTest` 11, `AnonymizeCorpusTest` 8, `FieldInstructionsTest` 10, `AnonymizeCliTest` 3) |
| every corpus document, STRICT | clean, verified, reloads with docx4j |
| LibreOffice renders every STRICT output | 9 of 9 to PDF (structural sanity only) |
| the two findings fixed (7892cb501): docx4j-core-tests | 1239 tests, 0 failures, 11 skipped (2026-09-21) |
| Word check round 2: `strict-smartart-anon` would not open — a third converter defect, not the anonymiser: strict keeps the 2010 shape elements in the wordprocessingDrawing namespace (`wp:wsp`) and the preprocessor mapped them to transitional `wp:`, where they do not exist; fixed (mapped to `wps:`/`wpg:`/`wpc:` by root name or nearest ancestor; `StrictLoadTest.textBoxesBecomeWordprocessingShape`) | docx4j-core-tests re-run: see below |
| Word 365 opens the outputs on the share (`fidelity/cr019/`, README there) | round 1 (2026-09-21): the corpus outputs opened; `probe-media-original` and `-keep` did not ("The operation is cancelled") — the probe's OLE bytes were junk, Word refuses the document itself. Round 2: the probe embeds a real Word 97-2003 document, the labelled footprints in; the probe files opened (`-keep` showed the kept altChunk text, trimmed to "SecretHtml paragraph" at Jason's ask); `strict-smartart-anon` did not open — the converter's `wp:wsp` defect, fixed. Round 3 (Jason, 2026-09-21): the SmartArt documents open fine. **Passed.** |

### CHANGELOG entry (for Jason to place)

    docx4j-docx-anon (CR-019 phase 1): a verified, fail-closed anonymiser.
    Anonymize is STRICT by default (parts it cannot make clean are removed;
    Mode.KEEP keeps and reports them); result.isClean() is the one answer;
    identities (comment and revision authors, people.xml, docProps), external
    targets, field-instruction arguments, settings (mail merge, docVars,
    attached template, protection hashes), content-control aliases/tags/
    bindings, charts (caches scrambled, workbook removed), diagrams, EMF/WMF/
    EPS/SVG, OLE, altChunk, VBA, embedded fonts, thumbnails and custom XML are
    covered; digits randomised; a before/after Verify; a JSON report; a CLI
    (org.docx4j.anon.AnonymizeCli, the jar's Main-Class). API: ScrambleText is
    a JaxbGraphWalker.Visitor now, not a TraversalUtil callback;
    AnonymizeResult.isOK() is deprecated to isClean().

### Left for later

- Phases 2 and 3 as planned; the walker and both visitors are format-neutral
  (they dispatch on JAXB classes), so pptx and xlsx are mostly a part table
  and a package type each.
- The lorem-fragment blind spot of `Verify`; a formula field whose bookmark
  is inside an expression token (`(Price+Tax)*1.1`) keeps the name (Verify
  flags it, so the result is not clean rather than wrong).
- Strings in mixed content and DOM extension text outside `t`/`v`/`f` are not
  scrambled (Verify sees the text nodes and flags them).
- docx4j-mcp's `anonymize` tool (CR-004 phase 4) can now call
  `AnonymizeCli.run` or `Anonymize` directly.

## Risks

- **A scrambled document that no longer reproduces the bug.** Scrambling
  changes glyph widths only within a Unicode range's font coverage, so line
  breaks can still move; the report names what changed so the customer can
  be asked for the original with a reason. Not a new risk; the price list's
  "ask the customer" path is the mitigation.
- **Verification that passes on tokens but leaks structure**: a table of
  salaries with scrambled names and replaced numbers still shows a salary
  table's shape. The guarantee is stated in terms of content, not shape, and
  the price list should say "text scrambled, metadata removed", not
  "unrecognisable".
- **pptx and xlsx are larger than they look**: charts, embedded workbooks,
  pivot caches and connections are where the data hides, and each is its own
  part family. The phases are ordered so the docx guarantee, which the
  regression-test row needs first, ships alone.
- **Removing parts can break the package for Word**: a removed OLE or chart
  part must take its relationship and its content-type override with it, and
  the referring drawing must be left pointing at a placeholder or removed too.
  The test corpus opens every output with docx4j and, for a sample, with Word
  on the VM (the fidelity share's resave runner exists).
