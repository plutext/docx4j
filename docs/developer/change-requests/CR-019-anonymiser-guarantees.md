# CR-019: What `docx4j-docx-anon` can guarantee — the docx gaps, then pptx, then xlsx

Status: PROPOSED (2026-09-16). Requested by the portfolio session working on the
Docx4j Enterprise price list (`Plutext-Enterprise-Java-11/pricing/`, not yet in
git), whose confidentiality clause and two support-tier rows depend on what the
anonymiser actually does; docx4j-mcp's CR-004 lists an `anonymize` tool in its
phase 4. Nothing coded. The module's own reading below was re-checked against
`docx4j-docx-anon` on VERSION_17_1_1 (b5c2f417f and later) on 2026-09-16.
Owner: Jason Harrop. Drafted with Claude Fable 5.1.

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
   17.1.1 (artifact ids are what users depend on), rename at the next minor
   if at all.

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
