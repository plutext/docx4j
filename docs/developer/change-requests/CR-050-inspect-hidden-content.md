# CR-050: Inspect - hidden and near-hidden content as a first-class report

Status: PROPOSED 2026-10-19, drafted at Jason's request ("consider section 12 ... of
../docx4j-portfolio/docs/text_extraction_behaviour_model.md then draft a CR to create that tool");
revised the same day after two reviews (§9), and to state its objective (§0, Jason: "frame it that way").
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

**The objective.** Report, without judging intent, where the text an agent holds diverges from the
document a person sees and from the file Office would open. Three divergences, each its own
group of findings:

1. **Unseen: in the text, not seen by a person.** White-on-white and 1pt text, zero-width, tag and
   bidi characters, a text box off the page or of zero size, a collapsed heading's body: all of it is
   in the default extraction, so an agent reads it while a person reviewing the document does not.
   This is the prompt-injection channel: the July 2026 Copilot-for-Word worm's white and tiny text
   (the paper's §12.1) reaches an agent through an ordinary extraction today; `w:vanish` text is the
   exception, since REQ-507 keeps it out by default.
2. **Omitted: in the file, not in the text.** `w:vanish` text, deleted text, an unselected
   `mc` branch, a header no section uses, a note no reference reaches, a bound control's stored
   surface, field codes: content the default extraction leaves out. The agent should know its text
   is partial and what would reveal the rest; and an agent that turns an option on (`hidden: true`)
   moves that content into the first group, since it then reads what a person does not see.
3. **Active: what the file would fetch or run in Office.** Macros, DDE and other external fields,
   external relationships, ActiveX, scripts in chunks, `w:updateFields`. Extraction never triggers
   any of it; Office would.

The rest REQ-902 asks for (comments, headers, notes, properties, custom XML, revision authors, ...)
is an **info** inventory: the file's other surfaces, seen by a person or of low stakes.

**What it is not: an attack detector.** A finding is a channel, not an attack: a run of white text is
reported whatever it says. Whether a finding's text is an injected instruction is a judgement about
its content, left to the agent, a person, or a classifier run on the findings' excerpts; the
inspector supplies the evidence (the excerpt, where it is stored, whether a person sees it, what
would put it in the text) and no verdict (the paper's §12.3, "It does not judge intent").

**Why a second operation.** An extractor returns what a reader of the page sees, and its defaults are
tuned for that; carrying the divergences in the text would hand the agent the injection. So
`inspect` is read-only and separate, and every extraction result carries the counts only, computed
against that extraction's own options and output format: "the text you hold contains N things a person does not see; M
things in the file are not in it; the file holds K active items". Word has had the operation since
2007 as File, Info, Check for Issues, Inspect Document; this is its static equivalent, with Word's
caveats kept (no layout: it cannot see an object covered by another, or text the page does not show
for reasons only a layout knows).

Three properties shape it, each from the review (§9):
- **It inspects what is stored, not what extraction selects.** Even with every scope on, an
  extraction walk selects one `mc` branch, reads only the headers a section uses and the notes a
  reference reaches, and puts a bound value in place of a control's surface. The inspector walks
  every stored story and branch before any selection, reusing the text model's property resolution
  and addresses (§2.1).
- **It never fetches, imports or computes.** Fields stay cached; no external target is resolved; no
  importer runs; a payload it cannot decode is still reported (§2.1).
- **It is exact.** Each finding says truthfully whether a person sees it and what would put it in the
  text (§2.3), has a stable identity so the counts do not move with traversal or run splitting
  (§2.4), and the report says whether it is complete (§2.5).

The CR adds `org.docx4j.model.text.Inspect` (findings of every kind REQ-902 lists, REQ-901), the
counts in every `TextResult`'s metadata (REQ-903, regrouped as D14 proposes), the statement of
REQ-904, the suite's inspector rows and an exact-set check for them, and hand-offs to docx4j-mcp, the
ports and the editor. Nothing in extraction changes (REQ-904).

## 1. What exists today (read 2026-10-19 on VERSION_17_3_4 at 5ecd41c68)

| Need | Where docx4j has it | What it lacks for `inspect` |
|---|---|---|
| addresses, property resolution, views | the text model (`org.docx4j.model.text`, CR-046): `TextBlock` addresses in REQ-518/519's grammar, the three views, `McSelection` by the TEXT purpose, hidden runs and paragraphs resolved through styles, text boxes, notes, comments, headers, alt text, charts, SmartArt, `altChunk`s, embedded packages, the glossary, bound controls (`sdt.binding_differs`), `mce.branches_differ` (REQ-307) | it reads what it selects: one branch, the headers sections use (case 26's `HDR26_ORPHAN` is read under no option), the notes references reach, a bound control's value in place of its surface; colour, shading, size and geometry are not kept |
| an inventory of every part | the anonymiser (CR-019, `org.docx4j.anon`): `PartsAnalyzer` (each part's class: macro, ActiveX, custom XML, embeddings, people, comments, ...), `JaxbGraphWalker` (every object reachable from a root, `w:rPrChange` authors and settings included), `DmlVmlAnalyzer`, `FieldInstructions` (keywords), `Verify` (external targets, text-bearing attributes) | positions in the reading order |
| the external fields | WD REQ-506's list; `FieldInstructions`; `TextField` codes | an "external" classification |
| tracked changes | `TrackedChangesReport` (CR-043) | per-occurrence locations |
| effective properties | `PropertyResolver` (CR-015); CR-030's table contexts | a colour comparison |
| geometry | `w:pgSz`, `w:pgMar`; `wp:anchor`/`wp:inline`; VML `style` | an off-page test without layout |
| the probe | `expected-findings.tsv`: 14 inspector rows (channel `inspect`), pending in three views (42 of the suite's 51 pending) | rows for the other kinds; fixtures for a few; and the harness checks only the expected keys, so an unexpected finding passes unnoticed |
| no external access | WD §11, REQ-506, REQ-513 | met by the text model since 19 October 2026. Before, when docx4j-ImportXHTML was on the class path, an `application/xhtml+xml` or MHT `altChunk` went through `XHTMLImporterImpl`, whose default image handler and user agent fetched `<img src>` and `<link href>` (measured: one stylesheet and four image requests to a local listener from one `Texts.plainText` call, and `[MISSING IMAGE: x, x ]` in the text). A defect of CR-046's extraction, fixed there (§7); the inspector never calls an importer |

## 2. Design

### 2.1 What is walked, and how

Three passes make one `Inspection`:

1. **The stored stories, before selection.** Every story part the package holds, not only those
   extraction reads: the main document; every header and footer part, referenced by a section or
   not (case 26's orphan); every footnote and endnote, referenced or not (case 27's, whose
   reference is deleted); every comment, anchored or not; text boxes; the glossary; each
   `altChunk`'s and each embedded package's own content, one level deep. In each, every
   `mc:AlternateContent` branch is walked (`McMode.ALL`), not only the selected one; a bound
   control's stored surface is walked as well as its value; field codes are walked as well as
   their results; every picture's and shape's `descr`, `title` and `name` is read, used by
   extraction or not. The walk reuses the text model's property resolution (style-resolved hidden,
   effective run properties, table-style context) and its addresses; content extraction has no
   address for (an unselected branch, an orphan header, a field code, a surface) is located
   physically (§2.4).
2. **The package inventory**, from the anonymiser's `PartsAnalyzer` and the relationship parts:
   macros, ActiveX, custom XML, `people.xml`, `w:docVars`, `w:updateFields`, `w:rsids`, the
   properties, every relationship (external, broken, or to a part of an unknown or unsupported
   kind), every embedded and `altChunk` part.
3. **The payloads**, decoded by the inspector itself and never converted: an HTML or XHTML chunk is
   scanned for `<script>`, event-handler attributes and external `src`/`href` references (jsoup's
   parser where present, else a tag scanner); an MHT chunk's parts are read for the same; a docx,
   Flat OPC or plain-text chunk and an embedded docx, xlsx or pptx are inspected one level deep. No
   importer is called, no field is recomputed (`fields: cached` always), no external target is
   resolved or fetched, and a payload that cannot be decoded is still reported, with the reason
   (§2.5).

### 2.2 The API

```java
Inspection i = Inspect.of(pkg);                       // read-only (REQ-204's snapshot is the test)
List<InspectFinding> f = i.findings();                // §2.3, ordered as §2.4 says
Counts c = i.counts(options, format, capabilities);   // unseen, omitted, active, info (§2.3), and whether they are lower bounds (§2.5)
Inspection.Status s = i.status();                     // COMPLETE, or PARTIAL with i.skipped() (§2.5)
List<String> l = i.limitations();                     // what it does not detect (REQ-904, §2.6)
```

`InspectFinding`: `kind`; `visible` (whether a person sees it under §2.3's display profile: `yes`, `no`,
`differently`, as text in the page colour is there but not legible, or `indeterminate` where a static
reading cannot establish it, D15); `reveal` (instructions for obtaining it, §2.3); `location` (§2.4);
`excerpt` (bounded text, §2.5) or `summary`. Its group is not stored: it follows from whether the
occurrence is in a given output (§2.3's membership), so `counts` takes the extraction's options, its
output format (plain text, Markdown, the block model) and its capabilities (an XHTML importer present
or absent, REQ-513). `TextResult.metadata()` gains `inspect`: the counts for the block model and for
plain text and Markdown under that result's own options, and the status (REQ-903 as D14 proposes,
phase 2); a serializer that delivers one format (docx4j-mcp's `extract_text`) carries that format's
counts. The JSON form is docx4j-mcp's (CR-046 D7).

### 2.3 Kinds, visibility, reveal and group

**Membership** decides each finding's group. An occurrence is *in the output* for given options,
format and capabilities when every container on its chain lets it through:
- its story is in scope: a header or footer under `include`, a comment under `comments`, the
  glossary under `include: glossary`, a note under `notes`, a text box under `text_boxes`; an unused
  story never is;
- its branch is the selected one (REQ-302); an unselected branch's content never is;
- its revision is in the view: deleted and moved-from content in `original` and `markup`, inserted
  and moved-to content in `accepted` and `markup` (REQ-401);
- its control shows what extraction reads: the bound value under `bindings: bound`, the stored
  surface under `bindings: surface`; a placeholder under `placeholders`;
- it is not inside hidden content (a hidden run, paragraph, sheet or slide), unless `hidden: true`;
- its chunk or embedded package is expanded: `altchunks` for the types REQ-513 converts (an MHT chunk
  only with an XHTML importer present), `embedded` for the packages REQ-512 reads;
- the format writes it: an image's description in plain text only under `alt_text: true`, in Markdown
  always (as the image's alt text), in the block model always (its `description`); a field's code
  only under `field_codes: true`; properties only under `metadata: true`.

White text inside a hidden run in an unused header is therefore not in the output under any options,
whatever `text.near_background`'s reveal says; deleted text is in the output under `markup` as well as
`original`. **`reveal`** is instructions for obtaining the content, never the membership
calculation: the composition of what every container on the chain needs (`include: headers` +
`hidden: true` + `view: original` for deleted hidden text in a header), or one of:
- **`included`**: in the default extraction already;
- **`unextractable`**: no extraction shows it (an RTF chunk, an OLE binary other than REQ-512's
  plain-text package, an unselected branch, a decorative picture's description).
A reveal is per occurrence, not per kind: a picture's `name` is extractable where it is the picture's
description (its `descr` empty, the picture not decorative: `AltTextDescriber`), and not elsewhere.

**The group**, in this order of precedence: **active** for the kinds the table marks so, whatever the
options; else **unseen** when the occurrence is in the output and `visible` is `no`, `differently` or
`indeterminate` (an agent is warned of what a person may not see); else **omitted** when it has text
and is not in the output; else **info** (in the output and seen, or an inventory kind with no text of
its own: an `altchunk` or `object.embedded` container, whose content is found in its own right,
`revision.author`, `rsids`, `people`, `relationship.broken`, `part.unknown`). So a comment is omitted
under the defaults and info under `comments: true`, `text.hidden` omitted under the defaults and
unseen under `hidden: true`, `text.near_background` unseen where its run is in the output and omitted
where it is not.

**The display profile** (D15) behind `visible`: Word 365 opening the file with its installation
defaults: Print Layout; revisions as Simple Markup (deleted text not shown, insertions shown);
comments as margin markers (a comment's presence shown, its text not); hidden text and field codes not
shown; collapsed headings collapsed (Word's "Expand all headings when opening" off). Where the
document or a user option decides what this profile shows and the file cannot settle it statically
(a page colour's display, a revision view the settings store, text behind an image), `visible` is
`indeterminate`.

The kinds, their identifiers (D2), visibility, reveal, group under the defaults in plain text with
no importer, and the probe cases that hold them (*new*: a case §3 adds):

| Kind | What | Visible | Reveal | Group (defaults, plain text) | Case |
|---|---|---|---|---|---|
| `text.near_background` | colour within the threshold of its effective background (D3) | differently | included | unseen | 18, 48, *new* |
| `text.tiny` | drawn under 2pt | differently | included | unseen | 18, *new* (exactly 2pt: no finding) |
| `chars.zero_width`, `chars.tag`, `chars.bidi`, `chars.private_use` | U+200B-U+200F, U+2060, U+FEFF; U+E0000-U+E007F; U+202A-U+202E, U+2066-U+2069; U+E000-U+F8FF and planes 15-16 outside a symbol font (`w:sym`, Symbol, Wingdings, Webdings) | no | included | unseen | 48, *new*; 29 the negative |
| `paragraph.collapsed` | the body of a heading marked collapsed (`w15:collapsed`), not shown on open | no | included | unseen | *new* |
| `shape.zero_size`, `shape.off_page`, `shape.hidden` | a text box or shape of extent 0 in a dimension; wholly off the page for every position its anchor could have on the page (D4); `wp:docPr/@hidden` or VML `visibility:hidden` | no | included where it holds text (`text_boxes: true` is the default), else unextractable | unseen where it holds text, else info | 48, *new* |
| `smartart.data` | SmartArt data its drawing does not show | no | included (`smartart: true`) | unseen | 35 |
| `text.hidden`, `paragraph.hidden` | `w:vanish` on the run or through its styles; a paragraph whose runs and mark are hidden | no | options: `hidden: true` | omitted | 18 |
| `revision.deleted`, `revision.moved_from` | deleted and moved-from runs and paragraphs (Word's default Simple Markup does not show them) | no | options: `view: original` | omitted | 9-15, 17 |
| `mce.branch` | content in an unselected branch, every other kind looked for in it, its text compared with the selected one's | no (an older Office would show it) | unextractable | omitted | 1, 3-8, *new* |
| `story.unused` | a header or footer no section references; a note no reference reaches; a comment with no anchor | no | unextractable | omitted | 26, 27, *new* |
| `sdt.stored_surface` | a bound control's stored surface where it differs from its value, every other kind looked for in it | yes (until Word refreshes the binding) | options: `bindings: surface` | omitted | 23, *new* |
| `field.code` | a field's instruction text (every field; the external ones are also `field.external`) | no | options: `field_codes: true` | omitted | 19, 20, 49 |
| `glossary` | the glossary's entries | no | options: `include: glossary` | omitted | 54 |
| `alt_text.description` | a picture's or shape's `descr` (a decorative picture's: unextractable) | no (read aloud by a screen reader) | options: `alt_text: true` in plain text; included in Markdown and the block model | omitted | 33 |
| `embedded.sheet_hidden`, `embedded.sheet_very_hidden`, `embedded.slide_hidden` | inside an embedded workbook or presentation (D10) | no | options: `embedded: true` + `hidden: true` | omitted | 53, 57 |
| `embedded.row_hidden`, `embedded.column_hidden` | a hidden row or column of an embedded workbook (the worksheet reader reads its cells: `Embedded.sheet` checks the sheet's state only) | no | options: `embedded: true` | omitted (unseen under `embedded: true`) | *new* |
| `embedded.notes` | a slide's speaker notes in an embedded presentation | no | unextractable (REQ-512 omits them) | omitted | 53 |
| `field.external` | REQ-506's INCLUDETEXT, INCLUDEPICTURE, LINK, IMPORT, DDE, DDEAUTO; a HYPERLINK to a scheme other than http, https or mailto, or to a file or UNC path (a `\l` bookmark is not external); simple and complex forms, keywords in any case, arguments quoted or not | - | options: `field_codes: true` | active | 49, *new* |
| `settings.update_fields` | `w:updateFields` | - | unextractable | active | 50 |
| `relationship.external` | an external target: hyperlink, linked image, attached template, mail-merge source, OLE link, subdocument | - | unextractable (never fetched) | active | 49, 50 |
| `part.macro`, `part.activex` | `vbaProject`; an ActiveX control part | - | unextractable | active | 51, 42 |
| `altchunk.script`, `altchunk.external_ref` | `<script>` or an event handler in an HTML, XHTML or MHT chunk; an external `src` or `href` in one | - | unextractable | active | *new* |
| `comment` | a comment, with its author | yes (its presence; its text on click) | options: `comments: true` | omitted (info under `comments: true`) | 28 |
| `header_footer`, `watermark` | a header or footer a section uses; a watermark's text | yes | options: `include: headers`, `include: footers` | omitted | 26 |
| `note`, `sdt.placeholder`, `chart.cached` | a footnote or endnote; a control showing its placeholder; a chart's cached strings | yes | included | info | 27, 22, 34 |
| `text.web_hidden` | `w:webHidden` (D8) | yes (not in Web Layout) | included | info | 18 |
| `altchunk`, `object.embedded` | each chunk, with its content type; each embedded object, with its ProgID | yes (as Word converts it; as its preview) | options: `altchunks: true` (default) or `embedded: true` for the types REQ-513 and REQ-512 read; unextractable for RTF and other OLE binaries | info | 36, 39-47, 53 |
| `alt_text.title` | a picture's or shape's title | no | unextractable | omitted | 33 |
| `alt_text.name` | its name: the description where `descr` is empty and the picture is not decorative, else not extracted | no | per occurrence: as `alt_text.description` where it is the description, else unextractable | omitted | 33, *new* (a named picture with no `descr`; a decorative one) |
| `properties` | core, app and custom properties | no (File, Info shows them) | options: `metadata: true` | omitted | 38, 52 |
| `doc_vars`, `custom_xml` | `w:docVars`; each custom XML part | no | unextractable (`bindings: bound` shows what a control binds) | omitted | *new*, 23 and 38 |
| `people` | `people.xml` (the authors' identities) | no | unextractable | info | *new* |
| `revision.author`, `rsids` | each author of the tracked changes, with the dates' range; the revision-session identifiers, one finding with their number | no | options: `view: markup` + `markup_detail: author_date` for authors; else unextractable | info | 9-17; any Word save |
| `relationship.broken`, `part.unknown` | a relationship whose internal target is missing; a part of an unknown or unsupported kind | - | unextractable | info | *new* |

### 2.4 Identity, location and counting (D6)

- **A finding is a kind at a stored location.** The location is where the content is stored: the
  chain of containing packages (none for the document itself; an embedded or `altChunk` part's name
  for each level), the part name within the innermost one, an element path within it, and, for text,
  a code-point range within the paragraph's stored text; plus the extraction address (REQ-518/519)
  where extraction has one. Two embedded documents that both hold `/word/vbaProject.bin` are two
  locations. A header used
  by three sections is stored once and found once; a field code is at its `w:instrText` runs; an
  unselected branch's run is at its element path inside the branch.
- **Kinds are kept apart.** One run that is hidden, tiny and holds tag characters is three findings,
  one per kind, at the same range.
- **Runs are merged, so run splitting does not count.** Adjacent stored runs in one paragraph are one
  finding when their kind and every attribute the finding reports (author and date, colour, size,
  branch) are equal; adjacent revisions of different authors are two.
- **Parts, relationships and references are counted apart.** A part is found once (`part.activex`
  for an ActiveX part referenced twice), its references are named in its summary, not counted;
  a relationship is one finding per relationship; where both the stored-story pass and the
  inventory meet one object (an `altChunk` part and its `w:altChunk`), the inventory's finding
  stands and the reference adds its location to it.
- **Order:** stored stories in the order of §2.1's first pass (main document, then headers and
  footers by part name, notes, comments, text boxes, glossary, chunks, embedded), each in document
  order; then the inventory by part name.
- **Counts** are the number of findings in each group for an output (options, format, capabilities).
  The findings depend on the stored content alone, the groups on the output only through §2.3's
  membership. So for a given output the counts are fixed, whatever the traversal or the run
  boundaries; a change of option or format moves findings between unseen, omitted and info, never in
  or out of the report.

### 2.5 Bounds and completion (D5, D11)

- An excerpt is at most 200 Unicode code points, and records its original length in code points and
  whether it was cut.
- The inspection as a whole is bounded, each limit enforced while decoding or walking, not after:
  at most 10,000 findings; 16 MB decoded from any one payload and 64 MB in all; 5,000,000 elements
  walked in all; embedded packages and chunks one level deep.
- A per-payload limit stops that payload and the inspection goes on. A global limit (findings, total
  bytes, total elements) stops the inspection: the sources not yet inspected are listed in
  `skipped()`.
- `skipped()` is bounded too: its first 1,000 entries, each a location and a reason (`not_decoded`,
  `limit.payload_bytes`, `limit.total_bytes`, `limit.elements`, `limit.findings`, `depth`,
  `unreadable`), then the number of the rest.
- `status()` is `COMPLETE` or `PARTIAL`. A partial report never reads as a clean one: an unreadable
  payload or an exhausted limit is `PARTIAL`, however few findings were made, and the counts of a
  partial report are lower bounds and say so (`Counts.isLowerBound()`, carried into REQ-903's counts).

### 2.6 What it does not detect (REQ-904)

`limitations()` and the class javadoc say, as Word's inspector says of itself: it does not detect an
object covered by another object; text placed where the page does not show it for a reason only a
layout knows (a shape whose position relative to its paragraph or line leaves it on the page or not
depending on where that paragraph falls, text clipped by a fixed-height row or frame, text behind an
image); white text whose background is an image; and it does not judge intent: no finding says its
text is, or is not, an attack (§0). docx4j has a layout
(the FO pass), so a later CR can add position findings; this one does not use it (D7).

## 3. Tests and the oracle

- **Exact comparison, by occurrence.** The harness gains, for the `inspect` channel, a comparison of
  the findings the engine produces with the expected ones as multisets of `(kind, full location)`
  (§2.4: the package chain, part, element path and range), so an unexpected finding fails, a missing
  one fails, and two findings of one kind in one paragraph are not one. `expected-findings.tsv` keeps
  its `(kind, selector)` rows for the ports' simpler adapters; `expected-inspection.tsv` holds each
  expected finding's full location, extraction address, visibility, reveal, group for named outputs
  (the defaults in plain text, Markdown and the block model; `hidden: true`; `embedded: true`;
  `comments: true`), excerpt length and cut flag, and each fixture's status and limitations, all
  asserted.
- **The rows.** `expected-findings.tsv` gains a row for every kind against the fixtures that hold it
  (§2.3); today's eight kind names are renamed to §2.3's identifiers (D2), in the TSV, the builder's
  source and the WD's Appendix A. The adapter advertises REQ-901, REQ-902 and REQ-904, so the 42
  pending rows run with the new ones.
- **A new fixture**, `extraction-probe-inspect.docx` (cases 58 on), built by the docx4j session with
  its own builder class as `extraction-probe-structure.docx` was (3dcbb3764), so the Word-checked
  probes are not rebuilt, with the boundary cases the review lists:

  | Area | Cases |
  |---|---|
  | formatting | a direct `w:vanish w:val="false"` over an inherited hidden; a paragraph part hidden, part visible; text at exactly 2pt (no finding) and at 1.5pt; colours just above and just below the threshold; a collapsed heading's body |
  | geometry | a page-relative shape wholly off the page; one partly on it (no finding); one zero in one dimension only; a paragraph-relative position that is indeterminate (no finding, a limitation) |
  | characters | a supplementary private-use character; a bidi override split across runs; Symbol and Wingdings characters (no finding) |
  | fields | INCLUDEPICTURE and LINK, simple and complex, keywords in mixed case, arguments quoted and bare; HYPERLINK `\l` (no finding) beside a file and a UNC target |
  | background | highlight against shading; `auto` and transparent fills; table-style shading; a page colour (`w:background`); a background docx4j cannot determine (no finding, a limitation) |
  | inventory | a part of an unknown content type; a broken relationship; an unreferenced note and an unanchored comment; `w:docVars`; `people.xml`; an HTML chunk with a `<script>`, an event handler and an external image and stylesheet; an embedded docx holding hidden text |

  Case 48's off-page box is column-relative (its VML style has no `mso-position-*-relative`); D4's
  rule judges it off page for every anchor position (its offset is 5000pt), but the rule's proof is the
  new page-relative case, not case 48. Jason's Word-open check, as for every fixture. (CR-046 phase
  0's cases 39-52, which carry macro, DDE and ActiveX content, were stopped in an earlier docx4j
  session by the model's safety classifier and built by another agent; if a case here is refused the
  same way, it is built elsewhere and recorded so.)
- **No external access.** Inspecting the fixtures, the HTML, XHTML and MHT chunks among them holding
  external images and stylesheets, with a local HTTP listener named in every external URL and with
  docx4j-ImportXHTML on the class path: the listener receives nothing, and no file outside the
  package is opened.
- **Read-only.** The REQ-204 snapshot (`TextsPhase2Test.snapshot`, declarations included since
  CR-049) before and after `Inspect.of` on every fixture.
- **The anonymiser's inventory as a cross-check.** Every part `PartsAnalyzer` classes as macro,
  ActiveX, custom XML, embedding, `altChunk` or people, and every external relationship `Verify`
  reads, is a package finding.
- **Membership and counts.** For each named output, every finding's group is the one
  `expected-inspection.tsv` gives, and the counts are their tally: white text inside a hidden run is
  omitted until `hidden: true` and unseen after; deleted text is in the output under `markup` and
  `original`; a picture's description is omitted from default plain text and in the Markdown; a
  hidden row's cells are omitted until `embedded: true`; an MHT chunk's content is omitted without an
  importer and in the output with one. The counts are unchanged by re-splitting a fixture's runs, and
  (phase 2) `TextResult.metadata().inspect` equals `counts` for the result's own options and formats.
- **Inspection adds nothing to extraction.** Every fixture's plain text, Markdown, HTML and block model
  are byte-identical with the inspection counts on and off, except the block model's `inspect`
  metadata entry; the document's own content appears in them as it always did (a white-text run is in
  the plain text, hidden text under `hidden: true`). No test searches an output for an excerpt.
- **Bounds.** A fixture of many small payloads (2,000 chunks of 40 KB, each under the per-payload
  limit, 80 MB in all) ends `PARTIAL` at the total-bytes limit with the rest in `skipped()`, and its
  counts marked lower bounds; one payload over 16 MB is stopped while it is decoded, the rest inspected;
  a document past the findings limit ends `PARTIAL`; `skipped()` stops at 1,000 entries and counts the
  rest.
- **Nested locations.** Two embedded documents each holding `/word/vbaProject.bin` and hidden text at
  the same element path give four findings at four distinct locations.
- **Visibility**: each kind's `visible` is §2.3's; where it rests on Word's display (Simple Markup's
  deletions, a collapsed heading, a page colour), the new fixture's Word-open check records what Word
  showed.

## 4. What changes for existing users

Nothing in extraction's output: REQ-904. From phase 2, `TextResult.metadata()` has one more entry,
`inspect` (the four counts against that result's options, and the status); the plain text and the
Markdown do not show it (REQ-802), and a
caller that writes the metadata (docx4j-mcp's JSON, the YAML front matter under `metadata: true`)
gains them.

## 5. Phases

### Phase 0: the kinds, the contract and the fixtures

- The objective (§0), the groups and their counts (D14), the kind identifiers (D2), the colour
  threshold (D3), the off-page rule (D4), the bounds (D5, D11), identity and counting (D6) and the
  reveal contract (D12), proposed to the WD: REQ-902 names no kind and leaves the threshold to "a
  documented threshold", REQ-901's `severity` and `how_to_see` become `visible`, `reveal` and the
  group, and REQ-903's three counts become D14's four.
- `expected-findings.tsv` (every kind, today's names renamed) and `expected-inspection.tsv`; the
  harness's exact-set comparison.
- `extraction-probe-inspect.docx` (§3), its rows, its Word-open check.

**Acceptance:** the rows run as pending (no inspector yet), the suite's other results unchanged; the
new fixture opens in Word without repair.

### Phase 1: the inspector

- `Inspect`, `Inspection`, `InspectFinding`, `Group`, `Visible`, `Reveal` in `org.docx4j.model.text`; the
  stored-story pass on the text model's resolution; the inventory; the payload scanner; bounds and
  status; `limitations()`.
- The adapter advertises REQ-901, REQ-902 and REQ-904.

**Acceptance:** REQ-902 and REQ-904; REQ-901 once the WD has adopted D12's reveal contract (D16).
Every inspector row passes, the exact comparison holds on every fixture, `expected-inspection.tsv`'s
fields hold; the no-access, read-only, cross-check, bounds and nested-location tests pass.

### Phase 2: the counts in every extraction result

- `TextResult.metadata()`'s `inspect` (REQ-903 as D14 has it), the four counts against the result's own
  options for the block model, plain text and Markdown, from the inspection run with the extraction; its cost
  measured on the probe and on the layout-fidelity corpora (the real-document sets), and recorded here.
- A docx4j option to skip the counts where the cost matters (D9), documented as making the result
  not an inspector's (WD §12.1).

**Acceptance:** REQ-903 once the WD has adopted D14's counts (D16); the inspector conformance class of
WD §12.1 claimed then, and not before.

### Phase 3: hand-offs

- **docx4j-mcp**: an `inspect` tool (the WD's verb, D29), the findings as JSON, and `extract_text`'s
  result carrying the four counts and the status, the unseen count first: it is the one an agent
  reading the text needs before it acts on it.
- **docx4j-core-ts** (its CR-012), **docx4j-python** (its CR-011) and **the editor** (ED-008's verbs):
  the kinds, the reveal contract, the fixtures and the rows; each says what its engine can and cannot
  see.
- **The portfolio**: the WD proposals of phase 0, the registry.

**Acceptance:** no statements; each receiver has acknowledged.

## 6. Decisions

Each with a recommendation; Jason decides.

- **D1. Where the findings come from.** Options: (a) §2.1: the stored stories walked before
  selection on the text model's resolution and addresses, plus the anonymiser's inventory and an
  own payload scanner; (b) everything from the anonymiser's `JaxbGraphWalker`, as the paper's §12.4
  and D29 have it. Recommended: (a). REQ-901 needs each finding's location in the reading order
  where it has one, and REQ-902's kinds need the resolution the text model does (style-resolved
  `w:vanish`, effective colour and size, table-style shading); the anonymiser's walker visits
  objects without either. This refines D29's "on the anonymiser's walker"; the paper's §12.4 would
  say so.
- **D2. Kind identifiers.** Options: (a) dotted identifiers (§2.3), proposed to the WD, the suite's
  eight names renamed; (b) keep the suite's phrases and name the rest alike. Recommended: (a), as
  the warnings' (`mce.branches_differ`); a JSON consumer needs stable tokens, and the suite's
  spelling (`colour`) differs from the WD's (`color`).
- **D3. The colour threshold.** Options: (a) the WCAG 2 contrast ratio between the run's effective
  colour and its effective background below 1.5:1 (white on white is 1.0, yellow on white 1.07,
  `#EEEEEE` on white 1.16; the palette's "Background 1, Darker 5%" (`#F2F2F2`) 1.12 and "Darker 15%"
  (`#D9D9D9`) 1.41, both flagged; "Darker 25%" (`#BFBFBF`) 1.84, not); (b) a distance in sRGB or CIELAB;
  (c) exact equality only. Recommended: (a), documented in the WD as REQ-902 asks. The effective
  background is the highlight, else the run's shading, else the paragraph's, else the cell's (table
  style in context), else the page colour, else white; an `auto` colour is never near its background
  (Word draws it in contrast); a background docx4j cannot determine gives no finding, and is a
  limitation.
- **D4. Off page without layout.** Options: (a) a shape is `off page` when it lies wholly outside the
  page for every position its anchor could have on the page: for an offset relative to the page,
  exactly; relative to the margin, from `w:pgMar`; relative to a column, paragraph, line or character,
  when the offset and the extent put it outside the page wherever on the page the anchor falls
  (case 48's -5000pt); otherwise indeterminate, no finding, and a limitation; (b) only page- and
  margin-relative positions; (c) judge paragraph-relative positions from where the paragraph falls,
  which needs layout. Recommended: (a). (The first draft's (b) would not have found case 48's box,
  which the suite expects.)
- **D5. The excerpt.** 200 code points, its original length and a cut flag. Options: 200, 100,
  unbounded. Recommended: 200.
- **D6. Identity and counting.** Recommended as §2.4.
- **D7. Layout findings.** Options: (a) none here, REQ-904's statement given; (b) a phase using the FO
  pass. Recommended: (a); a layout phase is its own CR with its own gate.
- **D8. `w:webHidden`.** Word shows it on the page and hides it only in Web Layout; extraction reads it
  (REQ-507 is `w:vanish`). Recommended: `info`, `visible: yes`, `reveal: included`: a person reading
  the page sees it, so it is not a divergence.
- **D9. Skipping the counts.** Options: (a) a docx4j-only option (`inspect: false`, outside WD §10, as
  REQ-1001 allows), the result then not an inspector's; (b) counts always. Recommended: decide at
  phase 2 from the measured cost; (a) if the inspection costs more than a fifth of the extraction on
  the real-document corpora.
- **D10. Spreadsheets and presentations.** Inside a docx, an embedded workbook's and presentation's
  hidden content is inspected (§2.3's `embedded.*` kinds, cases 53 and 57 and new ones). A
  standalone xlsx or pptx is outside the WD, which is WordprocessingML's. Recommended: a later CR for
  those, over the anonymiser's xlsx and pptx coverage.
- **D11. The whole report's bounds.** Options: 10,000 findings and 16 MB decoded per payload, one level
  deep, with `PARTIAL` past either; or no bounds. Recommended: the bounds, so a hostile document cannot
  make the inspection the denial of service.
- **D12. The reveal contract.** Options: (a) §2.3's three forms, with composed option lists, proposed
  to the WD as REQ-901's `how_to_see`; (b) one option name per finding, as REQ-901 reads now.
  Recommended: (a): (b) would tell an agent that `alt_text: true` reveals a picture's title, or that
  `altchunks: true` reveals an RTF chunk, neither of which is so.
- **D13. Collapsed headings and rsids** (in the paper's §12.3, not in REQ-902). Recommended: both in, as
  §2.3: a collapsed heading's body is text not shown on open; the rsids are one `info` finding, their
  number, since they identify editing sessions.
- **D14. Groups and counts by objective** (Jason, 19 October 2026: frame the CR by its objective).
  Options: (a) §2.3's four groups, unseen, omitted, active and info, from each finding's `visible` and
  `reveal` against a set of options, with REQ-903's counts `{unseen, omitted, active, info}` against
  the extraction's own options, view and output format; (b) REQ-903 as it reads, `{hidden, external, info}`, fixed
  severities. Recommended: (a), proposed to the WD. Under (b) "hidden" holds both `w:vanish` text,
  which the agent does not hold, and white text, which it does; for an agent reading the text the
  second is the urgent one, and only (a) counts it apart, and counts it again when an option moves
  hidden content into the text.
- **D15. Visibility's reference.** Options: (a) §2.3's display profile (Word 365 with its installation
  defaults: Print Layout, Simple Markup, comment markers, hidden text and field codes off, collapsed
  headings collapsed), with `visible: indeterminate` where the document or a user option decides and
  the file cannot settle it statically; (b) each display Word offers, a `visible` per display.
  Recommended: (a). (The previous draft's "Word's defaults, the document's own view settings
  honoured" could conflict with itself; the profile fixes one reading, and `indeterminate` says where
  a static inspector cannot know.) An `indeterminate` occurrence in the output counts as unseen, since
  the warning is that a person may not see it.
- **D16. Conformance against the WD as it stands.** WD 0.16's REQ-901 asks for `severity` and
  `how_to_see`, and REQ-903 for `{hidden, external, info}`; D12 and D14 propose a different contract.
  Options: (a) claim REQ-901 and REQ-903 only once the WD has adopted D12 and D14, and until then
  emit, beside the new fields, the WD's current ones by a documented mapping (`severity`: `hidden` for
  unseen and omitted findings, `external` for active ones, `info` for the rest; `how_to_see`: the
  reveal's options, `included`, or `unextractable`; the counts likewise); (b) implement only the WD as
  it stands. Recommended: (a): the gate keeps docx4j honest about what it conforms to, and the mapping
  lets docx4j-mcp ship before the WD moves.

## 7. Dependencies, risks and out of scope

- **Depends on:** CR-046 (done: the resolution, the addresses, the views, the suite); CR-049 (done: the
  read-only snapshot compares declarations).
- **A defect found while drafting, outside this CR:** CR-046's extraction fetches external resources
  through docx4j-ImportXHTML when it is on the class path (§1's last row), against WD §11, REQ-506 and
  REQ-513, and lets its `[MISSING IMAGE ...]` placeholder into the text. docx4j-mcp ships
  ImportXHTML, so its `extract_text` does the same. **Fixed** in CR-046's text model on 19 October 2026
  (an image handler that adds nothing and a user agent that refuses every resource; CR-046 records
  it). docx4j-mcp has the fix once its engine is 17.3.4.
- **False positives** are the risk that matters to an agent: a report that cries wolf is ignored. The
  exact-set comparison, the boundary cases of §3, and the colour rule's resolution of theme colours,
  table-style shading and `auto` before it compares are the guard.
- **Cost:** phase 2's inspection on every extraction, measured before D9 is decided.
- **Fixture content:** §3's note on the classifier.
- **Out of scope:** layout-based detection (D7); standalone xlsx and pptx (D10); judging intent;
  changing any extraction default or option (REQ-904); a CLI (`AnonymizeCli` could gain an
  `--inspect` mode later).

## 8. Sources

- WD 0.16 §9 (REQ-901 to 904), §10, §11, §12.1; REQ-307, REQ-502, REQ-503, REQ-506, REQ-507, REQ-512,
  REQ-513, REQ-514, REQ-518, REQ-519, REQ-706.
- The behaviour model paper, §12 (12.1 to 12.4) and D29; §11.8 on the July 2026 worm.
- Microsoft's Document Inspector documentation, as the paper's §12.2 quotes it.
- docx4j: `org.docx4j.model.text` (CR-046); `org.docx4j.anon` (CR-019); `HiddenText`;
  `TrackedChangesReport` (CR-043); `PropertyResolver` (CR-015); CR-030's table contexts;
  docx4j-ImportXHTML's `XHTMLImageHandlerDefault` and `Docx4jUserAgent`.
- The suite: `text-extraction/phase0/expected-findings.tsv`, the cases' README, GOLDENS.md's adapter
  contract.
- WCAG 2.2, the contrast ratio (relative luminance).

## 9. Review, 19 October 2026

A review of the first draft (5ecd41c68), passed on by Jason, raised seven points and a fixture list.
Each was checked against the code and the suite; all seven are taken:

1. **Inspect stored content beyond the extraction path.** Confirmed: the suite's `HDR26_ORPHAN` is read
   under no option, and a note whose reference is deleted is gone from the accepted view
   (`FN27_DELETED_REF_NOTE`); extraction selects one branch and substitutes a bound value. §2.1's first
   pass walks the stored stories before selection; kinds `mce.branch`, `story.unused`,
   `sdt.stored_surface`, `field.code`, `alt_text.title`/`name` added; a physical location where no
   address exists (§2.4).
2. **Independence from importers and external access.** Confirmed, and wider than the inspector:
   measured, extraction itself fetched through ImportXHTML (§1, §7; fixed in CR-046). §2.1's third pass decodes
   payloads itself and never converts, resolves or fetches; §3's no-access test.
3. **The `how_to_see` contract.** Taken: three forms with composed options (§2.3, D12), each kind's
   reveal corrected (near-background, tiny and webHidden text are `included`; RTF chunks, titles and
   names, and OLE binaries are `unextractable`).
4. **Kinds the paper names.** Collapsed headings and rsids added (D13); D10's embedded kinds named, with
   case 53's speaker notes and 57's sheets.
5. **Identity, addresses and counting.** Taken as §2.4: a kind at a stored location, kinds kept apart,
   runs merged on equal attributes, parts, relationships and references counted apart.
6. **The harness beyond counts.** Confirmed: `ExtractionConformance` checks only the expected keys.
   §3's exact comparison and `expected-inspection.tsv`; the counts follow §2.3's membership for each
   view, option and format (the second review below replaced "the same across views and options",
   since grouping by objective moves findings between groups by design).
7. **Bounding the whole inspection.** Taken as §2.5 and D11: code points, original length, cut flag;
   findings, bytes and depth bounded; a `PARTIAL` status with reasons.

**The objective, 19 October 2026.** Asked whether the aim was to list what the text leaves out, to
detect prompt injection, or both, the draft turned out to mix three objectives under severities that
did not follow them ("hidden" held both `w:vanish` text and white text). Jason: "frame it that way":
the objective is the three divergences of §0 (unseen, omitted, active, with info for the rest),
explicitly not an attack detector; §2.2, §2.3, §2.4, D8 and the tests follow it, and D14 and D15 are new.

The fixture list is §3's table. Case 48's off-page box is column-relative, as the review says; D4 is
revised to judge it (off the page wherever its anchor falls), and the new page-relative case is the
rule's proof.

**A second review, of the objective-framed revision**, passed on by Jason the same day, raised six
points and two clarifications. Its three code claims were checked and hold: `PlainText` writes an
image description only under `alt_text: true` while `WmlToMarkdown` writes it as the alt text always;
`Embedded.sheet` reads a hidden row's and column's cells (it checks only the sheet's state);
`AltTextDescriber` uses a picture's `name` where its `descr` is empty. All taken:

1. **Grouping by membership, not reveal.** §2.3's per-occurrence membership through the containing
   story, branch, revision, control, hidden content, chunk and format; `reveal` is instructions only;
   the precedence of the groups stated, so a comment or header is omitted, not info, while out of the
   output (the earlier table contradicted the rule).
2. **The output format.** `counts(options, format, capabilities)`; REQ-903's counts per format in the
   block model's metadata, a single-format serializer carrying its own; an importer's presence is a
   capability.
3. **Two reveals corrected.** Hidden rows and columns are extractable under `embedded: true`; a
   picture's name is extractable where it is the description, a decorative picture's description
   never; positive and negative cases. (That the text model reads hidden rows and columns while
   REQ-512 omits hidden sheets "as hidden text is" is a question for the WD, raised apart.)
4. **The serialization test.** Replaced by output identity with the counts on and off; no excerpt
   search; §9's earlier "counts the same across views and options" corrected.
5. **Total bounds.** §2.5's aggregate byte and element budgets, enforced while decoding; a global
   limit stops the inspection; `skipped()` bounded; a partial report's counts are lower bounds; the
   many-small-payloads test.
6. **Nested locations.** §2.4's package chain; the exact comparison by occurrence, with multiplicity.

And D15's display profile with `indeterminate`, and D16's gate on the WD's adoption of D12 and D14.
