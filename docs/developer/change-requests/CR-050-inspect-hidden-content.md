# CR-050: Inspect - hidden and near-hidden content as a first-class report

Status: PROPOSED 2026-10-19, drafted at Jason's request ("consider section 12 ... of
../docx4j-portfolio/docs/text_extraction_behaviour_model.md then draft a CR to create that tool").
Four phases (§5); decisions in §6, none taken yet.

The specification is the portfolio's *WordprocessingML Text Extraction*, WD 0.16
(`docx4j-portfolio/docs/text_extraction_specification_wd.md`, dae90bf), cited as **WD**: its §9,
REQ-901 to REQ-904, and the inspector conformance class of §12.1. The design is the behaviour model
paper's §12 (`docs/text_extraction_behaviour_model.md`), whose D29 Jason accepted on 17 October 2026
with D1 to D31: "an `inspect` report ... a portfolio verb and a docx4j CR on the anonymiser's walker;
`extract_text` carries only counts, the report carries the findings". This is that CR. CR-046 (the
text model, done) left the inspector out (its §5 table: "REQ-901 to REQ-904 ... the inspect CR (D29)");
the conformance suite's 42 pending rows are its rows.

## 0. Summary

An extractor returns what a reader of the page sees, so its defaults keep out what the page does
not show: `w:vanish` text, deleted text, comments, headers, an unselected `mc` branch. Some of
that is a prompt-injection channel (the paper's §12.1: white on white, 1pt and `w:vanish` text in
the July 2026 Copilot-for-Word worm), and the extraction defaults cannot carry it without handing
the agent the injection. The answer is a second, read-only operation, `inspect`, which lists what
the text leaves out, each item addressed and bounded; and a count of those items in every
extraction result, so an agent learns in one line that the text it holds is not the whole file.
Word has had the operation since 2007 as File, Info, Check for Issues, Inspect Document; this CR
gives docx4j its static equivalent, with Word's two caveats kept (no layout: it cannot see an object
covered by another, or text the page does not show for reasons only a layout knows).

The CR adds:
- `org.docx4j.model.text.Inspect`, a read-only operation over a `WordprocessingMLPackage` returning
  an `Inspection`: findings `{kind, address, severity, text or summary, how_to_see}` (REQ-901) of
  every kind REQ-902 lists, and their counts by severity;
- the counts, `inspect: {hidden, external, info}`, in every `TextResult`'s metadata (REQ-903), never
  the findings;
- the statement REQ-904 asks for, in the API and in the result;
- the conformance suite's inspector rows, completed for every kind (most against the existing
  fixtures, a few in a new one), and the adapter that checks them;
- hand-offs to docx4j-mcp (an `inspect` tool, the counts on `extract_text`), the ports and the editor.

Nothing in extraction changes: no default moves, no option is added to WD §10 (REQ-904).

## 1. What exists today (read 2026-10-19 on VERSION_17_3_4 at 76d1ae504)

| Need | Where docx4j has it | What it lacks for `inspect` |
|---|---|---|
| a walk of every story, scope and view, with addresses | the text model (`org.docx4j.model.text`, CR-046): `Extractor`, `TextBlock` addresses in REQ-518/519's grammar, the three views, `McSelection` by the TEXT purpose, hidden runs and paragraphs resolved through styles (`TextRun`/`TextBlock` hidden flags), text boxes, notes, comments, headers and footers, alt text, charts, SmartArt, `altChunk`s, embedded packages, the glossary, bound controls with their surface (`sdt.binding_differs`), `mce.branches_differ` (REQ-307) | colour, shading, size and drawing geometry are read for formatting only, not kept; no finding type |
| an inventory of every part and what it is | the anonymiser (CR-019, `org.docx4j.anon`): `PartsAnalyzer` (each part's class and treatment: macro, ActiveX, custom XML, embeddings, people, comments, ...), `JaxbGraphWalker` (every object reachable from a root, including `w:rPrChange` authors and settings), `DmlVmlAnalyzer` (what a drawing does beyond showing a picture), `FieldInstructions` (field keywords, `KEYWORDS`), `Verify` (external relationship targets, text-bearing attributes) | addresses: it visits objects, not positions in the reading order |
| resolved `w:vanish` | `org.docx4j.convert.out.common.HiddenText` (the exporters'), and the text model's own resolution | - |
| the external fields | WD REQ-506's list; `FieldInstructions` parses keywords; the text model's `TextField` carries each field's code | no "external" classification |
| tracked-change counts | `TrackedChangesReport` (CR-043): by kind, author and part, with `toJson()` | per-occurrence addresses |
| effective run and paragraph properties | `PropertyResolver` (CR-015); table-style shading in context (CR-030's `CellContext`) | a colour comparison |
| page and drawing geometry | `sectPr` (`w:pgSz`, `w:pgMar`); `wp:anchor`/`wp:inline` extents and offsets; VML `style` | an off-page or zero-size test without layout |
| the probe | `text-extraction/phase0/`: `expected-findings.tsv` has 14 inspector rows (channel `inspect`: `activex`, `colour near background`, `zero-width characters`, `tag characters`, `off page`, `zero size`, `external`, `macro`), pending in three views each (42 of the suite's 51 pending) | rows for the other kinds; fixtures for a few |

So the paper's "the operation is `anonymize` with reporting instead of scrubbing" is half right in the
code: the anonymiser knows every *part*, but only the text model knows where in the *reading order*
a run is, which REQ-901's `address` needs, and how it resolves (style, view, branch). §2.1 combines
them (D1).

## 2. Design

### 2.1 Two sources, one result

- **Content findings** (anything with a place in the reading order: a hidden run, a deleted run, a
  near-background run, a tiny run, a character class, a shape, a field, a comment's anchor, a
  placeholder, a bound control, an alt text) come from the text model's walk, run with every scope
  on (`include: headers, footers, comments, glossary`, `text_boxes`, `notes`, `comments`,
  `embedded`, `altchunks`, `charts`, `smartart`, `alt_text`) in the `markup` view, which holds both
  the inserted and the deleted text. The walk gains an inspection sink: where it resolves a run,
  paragraph, field, drawing or control, it reports to the sink what it saw. The block model does
  not change (REQ-706); the sink is internal.
- **Package findings** (anything that is a part or a relationship: `vbaProject`, ActiveX, custom
  XML parts, `people.xml`, `w:docVars`, `w:updateFields`, the properties, external relationships,
  embedded and `altChunk` parts) come from the anonymiser's inventory: `PartsAnalyzer`'s classes
  and the relationship parts, read, not changed. Their address is the part name (`/word/vbaProject.bin`),
  or the source part and relationship id for an external target.

The two are one `Inspection`, ordered: content findings in reading order, then package findings by
part name.

### 2.2 The API

```java
Inspection i = Inspect.of(pkg);              // read-only; the package is unchanged (REQ-204's snapshot is the test)
List<InspectFinding> f = i.findings();       // kind, address, severity, text or summary, howToSee
Map<Severity, Integer> c = i.counts();       // hidden, external, info (REQ-903)
List<String> l = i.limitations();            // what it does not detect (REQ-904)
```

`InspectFinding` is `{String kind; String address; Severity severity; String text; String summary;
String howToSee}`: `text` for a finding that has text (bounded, D5), `summary` otherwise (a part's
size and content type, a relationship's target). `howToSee` is the WD §10 option and value that
reveals the content in extraction (`hidden: true`, `view: original`, `include: headers`,
`comments: true`, `alt_text: true`, `bindings: surface`, `metadata: true`, `embedded: true`), or
`unextractable`. `TextResult.metadata()` gains `inspect`, a map of the three counts (REQ-903,
phase 2). The JSON form is docx4j-mcp's (CR-046 D7).

### 2.3 The kinds

Each REQ-902 kind, its identifier (D2: dotted, as `TextWarning`'s are), its severity (the paper's
grouping: `hidden` for content present but not shown, `external` for content that fetches or
executes, `info` for what extraction keeps out by default), what it is read from, its
`how_to_see`, and the probe case that holds it (a new case is marked *new*).

| Kind | Severity | Read from | how_to_see | Case |
|---|---|---|---|---|
| `text.hidden` (`w:vanish` on the run or through its styles) | hidden | the walk's resolved hidden flag | `hidden: true` | 18 |
| `paragraph.hidden` (runs and mark hidden: no line) | hidden | the same | `hidden: true` | 18 |
| `text.web_hidden` (`w:webHidden`) | hidden | the run's effective properties | unextractable (D8) | 18 |
| `mce.branch_differs` (an unselected branch's text differs) | hidden | REQ-307's comparison, promoted | unextractable | 1, 3-7 |
| `revision.deleted`, `revision.moved_from` | hidden | the `markup` view's deleted and moved-from runs | `view: original` | 9-15, 17 |
| `shape.zero_size` | hidden | `wp:extent`, VML `width`/`height` of 0 | `text_boxes: true` (on by default) | 48 |
| `shape.off_page` (wholly outside the page by its own geometry, D4) | hidden | `wp:positionH`/`positionV` relative to the page or margin, VML `margin-left`/`top` with `mso-position-*-relative`, against `w:pgSz`/`w:pgMar` | `text_boxes: true` | 48 |
| `shape.hidden` (`wp:docPr/@hidden`, VML `visibility:hidden`) | hidden | the drawing's own properties | unextractable | *new* |
| `text.near_background` (colour within the threshold of its effective background, D3) | hidden | the run's effective `w:color` (theme colours resolved) against its effective background: `w:highlight`, run, paragraph and cell shading (table styles in context), else the page colour (`w:background`), else white | unextractable (it is extracted; the finding says it is not seen) | 18, 48, *new* |
| `text.tiny` (drawn under 2pt) | hidden | the effective `w:sz` (or `w:szCs` where the run is complex script) under 4 half-points | unextractable (extracted) | 18 |
| `chars.zero_width`, `chars.tag`, `chars.bidi`, `chars.private_use` | hidden | the run's characters: U+200B-U+200F, U+2060, U+FEFF; U+E0000-U+E007F; U+202A-U+202E, U+2066-U+2069; U+E000-U+F8FF and the supplementary private-use planes outside a symbol font (`w:sym`, Symbol, Wingdings, Webdings) | unextractable (extracted) | 48, *new*; 29 is the negative case |
| `field.external` (REQ-506's INCLUDETEXT, INCLUDEPICTURE, LINK, IMPORT, DDE, DDEAUTO; HYPERLINK with a scheme other than http, https or mailto) | external | the walk's `TextField` codes, keywords as `FieldInstructions` parses them | `field_codes: true` | 49, *new* |
| `settings.update_fields` | external | `w:updateFields` | unextractable | 50 |
| `relationship.external` (hyperlink targets, linked images, attached template, mail-merge source, OLE links, subdocuments) | external | the relationship parts' `TargetMode="External"` | unextractable (never fetched, REQ-506) | 49, 50 |
| `part.macro` (`vbaProject`) | external | `PartsAnalyzer` | unextractable | 51 |
| `part.activex` | external | `PartsAnalyzer`, the `w:control` reference | unextractable | 42 |
| `altchunk` (with its content type), `altchunk.script` (an HTML chunk with `<script>`) | external | the walk's chunks; the chunk's HTML | `altchunks: true` (on by default) | 36, 43-47, *new* |
| `object.embedded` (with its ProgID) | info | the walk's object runs | `embedded: true` | 39-41, 53 |
| `comment` (and its author) | info | the comments part, the walk's anchors | `comments: true` | 28 |
| `header_footer`, `watermark` | info | the walk's header and footer stories | `include: headers, footers` | 26 |
| `note` | info | the walk's notes | `notes: true` (on by default) | 27 |
| `alt_text` (descr, title, name of pictures and shapes) | info | the walk's images and shapes | `alt_text: true` | 33 |
| `sdt.placeholder` | info | the walk's controls | `placeholders: true` (on by default) | 22 |
| `sdt.stale_surface` (a bound control's surface differs from its value) | info | `sdt.binding_differs`'s comparison | `bindings: surface` | 23 |
| `chart.cached` (a chart's cached strings) | info | the walk's charts | `charts: true` (on by default) | 34 |
| `smartart.data` (data not in the drawing) | info | the walk's SmartArt | `smartart: true` (on by default) | 35 |
| `glossary` | info | the glossary part | `include: glossary` | 54 |
| `properties` (core, app, custom) | info | the properties parts | `metadata: true` | 38, 52 |
| `doc_vars` (`w:docVars`) | info | settings | unextractable | *new* |
| `custom_xml` (each custom XML part) | info | `PartsAnalyzer` | unextractable (`bindings: bound` shows what a control binds) | 23, 38 |
| `people` (`people.xml`) | info | `PartsAnalyzer` | unextractable | *new* |
| `revision.author` (each author and date range of the tracked changes) | info | `TrackedChangesReport`, the walk | `view: markup`, `markup_detail: author_date` | 9-17 |

Granularity (D6): one finding per occurrence for content kinds (a run, a shape, a field), merged
over adjacent runs of the same kind in one paragraph; one per author for `revision.author` and
comment authors; one per part or relationship for package kinds.

### 2.4 What it does not detect (REQ-904)

`Inspection.limitations()` and the class javadoc say, as Word's inspector says of itself: it does not
detect an object covered by another object, text placed where the page does not show it for a
reason only a layout knows (a shape positioned relative to a paragraph or line, text clipped by a
fixed-height row or frame, text behind an image), or white text whose background is an image; and
it does not judge intent. docx4j has a layout (the FO pass), so a later CR can add position
findings; this one does not use it (D7).

## 3. Tests and the oracle

- **The conformance suite.** `expected-findings.tsv` gains a row for each kind against the fixtures
  that hold it (§2.3's last column), each row's selector a probe marker token, a target or a part
  name, as today's 14 are. `TextsAdapter` (or an `InspectAdapter` beside it) advertises REQ-901 and
  REQ-902 and returns the findings keyed `inspect<TAB>kind<TAB>selector` with their counts, so the
  42 pending rows run, and the new rows with them. Today's eight kind names are renamed to the
  identifiers of §2.3 (D2), in the TSV, the builder's source and the WD's Appendix A.
- **A new fixture** for the kinds no fixture holds, `extraction-probe-inspect.docx` (cases 58 on),
  built by the docx4j session with its own builder class, as `extraction-probe-structure.docx` was
  (3dcbb3764), so the Word-checked probes are not rebuilt: a shape with `docPr/@hidden` and a VML
  one with `visibility:hidden`; text in the page colour (`w:background`) and in a theme colour near
  its shading; a bidi override; a private-use character in a text font beside one in Symbol;
  `w:docVars`; `people.xml`; an HTML `altChunk` holding a `<script>`; INCLUDEPICTURE and LINK
  fields; a HYPERLINK field with a non-http scheme. Jason's Word-open check, as for every fixture.
  (CR-046 phase 0's cases 39-52, which carry macro, DDE and ActiveX content, were stopped in an
  earlier docx4j session by the model's safety classifier and built by another agent; if a case
  here is refused the same way, it is built elsewhere and recorded so.)
- **Read-only.** The REQ-204 snapshot (`TextsPhase2Test.snapshot`, which since CR-049 compares
  declarations too) before and after `Inspect.of` on every fixture.
- **The anonymiser's inventory as a cross-check.** Every part `PartsAnalyzer` classes as macro,
  ActiveX, custom XML, embedding, `altChunk` or people, and every external relationship `Verify`
  reads, is a package finding; the test fails on one missed. This is the paper's "what `inspect`
  lists is what `anonymize` would change", restricted to the package kinds, where it holds.
- **No false positives on the plain probe text**: every finding the fixtures produce is in
  `expected-findings.tsv` (an unexpected kind or selector fails), so case 29's Symbol characters,
  auto-coloured text and ordinary shapes stay quiet.
- **Counts** (phase 2): `TextResult.metadata().inspect` equals `Inspect.of(...).counts()` for every
  fixture and view, and no finding's text reaches `PlainText.of` or the Markdown (REQ-903).

## 4. What changes for existing users

Nothing in extraction's output: REQ-904. From phase 2, `TextResult.metadata()` has one more entry,
`inspect`; the plain text and the Markdown do not show it (REQ-802), and a caller that writes the
metadata (docx4j-mcp's JSON, the YAML front matter under `metadata: true`) gains the three counts.

## 5. Phases

### Phase 0: the kinds and their fixtures

- The kind identifiers (D2), the colour threshold (D3), the off-page rule (D4), the text bound (D5)
  and the granularity (D6), proposed to the WD (REQ-902 lists the kinds but names none and leaves
  the threshold to "a documented threshold").
- `expected-findings.tsv`: a row for every kind, against the existing fixtures where they hold it;
  today's eight kind names renamed.
- `extraction-probe-inspect.docx` (§3), its rows, its Word-open check.

**Acceptance:** the rows run as pending (no inspector yet), the suite's other results unchanged; the
new fixture opens in Word without repair.

### Phase 1: the inspector

- `Inspect`, `Inspection`, `InspectFinding`, `Severity` in `org.docx4j.model.text`; the walk's
  inspection sink; the package inventory over `PartsAnalyzer` and the relationship parts.
- Every kind of §2.3; `how_to_see`; the bound; `limitations()`.
- The adapter advertises REQ-901, REQ-902 and REQ-904.

**Acceptance:** REQ-901, REQ-902, REQ-904. Every inspector row of the suite passes (the 42, and phase
0's); the read-only snapshot and the anonymiser cross-check pass; no unexpected finding on any
fixture.

### Phase 2: the counts in every extraction result

- `TextResult.metadata()`'s `inspect` (REQ-903), from a second, all-scope walk run with the
  extraction; its cost measured on the probe and on the layout-fidelity corpora (the real-document
  sets), and recorded here.
- A docx4j option to skip the counts where the cost matters (D9), documented as making the result
  not an inspector's (WD §12.1).

**Acceptance:** REQ-903; the inspector conformance class of WD §12.1 claimed.

### Phase 3: hand-offs

- **docx4j-mcp**: an `inspect` tool (the WD's verb, D29), the findings as JSON, and `extract_text`'s
  result carrying the counts; its CR-mcp-server §19 has the engine bump this needs.
- **docx4j-core-ts** (its CR-012, the text model), **docx4j-python** (its CR-011) and **the editor**
  (ED-008's verbs): the kinds, the fixtures and the rows; each says what its engine can and cannot
  see (the ports have no layout, nor does docx4j's inspector).
- **The portfolio**: the WD proposals of phase 0, the registry.

**Acceptance:** no statements; each receiver has acknowledged.

## 6. Decisions

Each with a recommendation; Jason decides.

- **D1. One walk or the anonymiser's.** Options: (a) content findings from the text model's walk,
  package findings from the anonymiser's inventory (§2.1); (b) everything from the anonymiser's
  `JaxbGraphWalker`, as the paper's §12.4 and D29 have it. Recommended: (a). REQ-901 needs each
  finding's `address`, in REQ-518/519's grammar, and REQ-902's kinds need the resolution the text
  model already does (style-resolved `w:vanish`, the views, the TEXT-purpose branch selection, the
  bound control's value): the anonymiser's walker visits objects without their place in the reading
  order, and would have to re-derive all of it. Its inventory is still the source for every part and
  relationship, and its test (§3) still holds for those. This refines D29's "on the anonymiser's
  walker"; the paper's §12.4 would say so.
- **D2. Kind identifiers.** Options: (a) dotted identifiers like the warnings' (`text.hidden`,
  `field.external`, §2.3), proposed to the WD, the suite's eight names renamed; (b) keep the suite's
  phrases (`colour near background`, `zero size`) and name the rest alike. Recommended: (a): the
  warnings (`mce.branches_differ`, `content.not_read`) set the style, a JSON consumer needs stable
  tokens, and the suite's spelling (`colour`) differs from the WD's (`color`).
- **D3. The colour threshold.** Options: (a) the WCAG 2 contrast ratio between the run's effective
  colour and its effective background below 1.5:1 (white on white is 1.0, yellow on white 1.07, `#EEEEEE` on
  white 1.16; the palette's "Background 1, Darker 5%" (`#F2F2F2`) 1.12 and "Darker 15%" (`#D9D9D9`)
  1.41, both flagged; "Darker 25%" (`#BFBFBF`) 1.84, not); (b) a distance in
  sRGB or CIELAB; (c) only exact equality. Recommended: (a), with the ratio documented in the WD as
  REQ-902 asks: it is the measure readers of the finding already know, and it treats a light colour
  on white and a dark one on black alike. `auto` colour is never near its background (Word draws it
  in contrast).
- **D4. Off page without layout.** Options: (a) a shape is `off page` when its rectangle, from its own
  offsets relative to the page or the margins and its extent, lies wholly outside the page
  (`w:pgSz`); a position relative to a paragraph, line, character or column is not judged (REQ-904's
  statement covers it); (b) also judge paragraph-relative positions from the paragraph's place,
  which needs layout. Recommended: (a).
- **D5. The text bound.** Options: 200 characters of a finding's text, then an ellipsis and the full
  length; or 100; or unbounded. Recommended: 200: enough to recognise an instruction, short enough
  that a findings list does not become a second copy of the hidden content.
- **D6. Granularity.** Recommended as §2.3 says: per occurrence for content kinds, adjacent runs of one
  kind in a paragraph merged; per author for authors; per part or relationship for package kinds.
- **D7. Layout findings.** Options: (a) none in this CR, REQ-904's statement given; (b) a phase using
  the FO pass for covered and clipped content. Recommended: (a): a static inspector is what every
  engine can share, and a layout phase is its own CR with its own gate.
- **D8. `w:webHidden`.** Word shows it on the page and hides it in a web view; the text model
  extracts it (REQ-507 is `w:vanish`). Recommended: report it as `hidden`, `how_to_see:
  unextractable` (it is extracted; the finding says a web view would not show it), as the paper's
  §12.3 lists it.
- **D9. Skipping the counts.** Options: (a) a docx4j-only option (`inspect: false` in `TextOptions`,
  outside WD §10, as REQ-1001 allows) that skips the second walk, the result then not an
  inspector's; (b) counts always. Recommended: decide at phase 2 from the measured cost; (a) if the
  second walk costs more than a fifth of the extraction on the real-document corpora.
- **D10. Spreadsheets and presentations.** The paper's "format siblings" (hidden sheets, rows and
  columns; hidden and off-slide content; notes) are outside the WD, which is WordprocessingML's.
  Options: a later CR over the anonymiser's xlsx and pptx coverage; or a phase here. Recommended: a
  later CR. Inside a docx, an embedded workbook's hidden sheets and a presentation's hidden slides
  are reported here, as `hidden` with `how_to_see: embedded: true; hidden: true` (cases 53, 57),
  since the text model already reads them.

## 7. Dependencies, risks and out of scope

- **Depends on:** CR-046 (done: the walk, the addresses, the views, the suite); CR-049 (done: the
  read-only snapshot compares declarations). Nothing outside docx4j blocks it; the WD proposals of
  phase 0 go to the portfolio.
- **False positives** are the risk that matters to an agent: a report that cries wolf is ignored.
  The fixtures' negative cases (Symbol characters, auto colour, ordinary shapes, case 29's bullets)
  and the "no unexpected finding" test guard the kinds most prone to them; the colour test resolves
  theme colours and table-style shading before it compares.
- **Cost:** phase 2's second walk. Measured before D9 is decided.
- **Fixture content:** §3's note on the classifier.
- **Out of scope:** layout-based detection (D7); xlsx and pptx (D10); judging intent; changing any
  extraction default or option (REQ-904); a CLI (the anonymiser's `AnonymizeCli` could gain an
  `--inspect` mode later).

## 8. Sources

- WD 0.16 §9 (REQ-901 to 904), §10, §12.1; REQ-307, REQ-506, REQ-507, REQ-518, REQ-519, REQ-706.
- The behaviour model paper, §12 (12.1 to 12.4) and D29; §11.8 on the July 2026 worm.
- Microsoft's Document Inspector documentation, as the paper's §12.2 quotes it.
- docx4j: `org.docx4j.model.text` (CR-046); `org.docx4j.anon` (CR-019: `PartsAnalyzer`,
  `JaxbGraphWalker`, `DmlVmlAnalyzer`, `FieldInstructions`, `Verify`); `HiddenText`;
  `TrackedChangesReport` (CR-043); `PropertyResolver` (CR-015); CR-030's table contexts.
- The suite: `text-extraction/phase0/expected-findings.tsv` and the 57 cases' README; GOLDENS.md's
  adapter contract.
- WCAG 2.2, the contrast ratio's definition (relative luminance).
