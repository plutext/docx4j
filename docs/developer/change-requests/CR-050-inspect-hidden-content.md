# CR-050: Inspect - hidden and near-hidden content as a first-class report

Status: IN PROGRESS. Phase 0 done 2026-10-19 (§5, "Phase 0 record", Jason: "implement CR-050 phase 0"):
the contract (`docx4j-core-tests/src/test/resources/text-extraction/INSPECTION.md`), an oracle of every
finding on the suite's four fixtures, the harness's exact comparison, and a new fixture,
`extraction-probe-inspect.docx` (cases 58 to 67), checked in Word 365. Phase 1 done 2026-10-19 (§5,
"Phase 1 record", Jason: "implement CR-050 phase 1"): `org.docx4j.model.text.Inspect`, every inspector
row passing; D18's tests calibrated on the corpora (for Jason's ruling). Phase 2 done 2026-10-19 (§5,
"Phase 2 record", Jason: "start CR-050 phase 2"): the counts in an extraction's result under docx4j's
`inspect: true`, off by default by D9's rule (the counts cost about four fifths of an extraction); the
rulings before it; REQ-903 checked. Phase 3 to come.
Proposed 2026-10-19, drafted at Jason's request ("consider section 12 ... of
../docx4j-portfolio/docs/text_extraction_behaviour_model.md then draft a CR to create that tool");
revised the same day after three reviews (§9), to state its objective (§0, Jason: "frame it that way"),
and for text an embedded font draws as other text or as nothing (§9, Jason's question).
Four phases (§5); decisions in §6: D1 to D8 and D11 to D20 taken by Jason on 19 October 2026 (D11 with
1,000 findings; D17 revised to the list alone after phase 0's Word check; D16 closed, overtaken by WD
0.17); D9 (a) taken before phase 2, with the faces' bound and the unanchored comment (§5); D10 open.

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
   bidi characters, a text box off the page or of zero size, a collapsed heading's body, characters an
   embedded font draws as nothing or as other letters: all of it is in the default extraction, so an
   agent reads it while a person reviewing the document does not (or, for the font, reads one word
   where a person sees another).
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
| an inventory of every part | the anonymiser (CR-019, `org.docx4j.anon`): `PartsAnalyzer` (each part's class: macro, ActiveX, custom XML, embeddings, people, comments, ...), `JaxbGraphWalker` (every object reachable from a root, `w:rPrChange` authors and settings included), `DmlVmlAnalyzer`, `FieldInstructions` (keywords), `MetadataScrubber.scrubExternalTargets` and `Verify.externalTargets` (every relationship whose TargetMode is External), `Verify` (text-bearing attributes) | positions in the reading order |
| the external fields | WD REQ-506's list; `FieldInstructions`; `TextField` codes | an "external" classification |
| tracked changes | `TrackedChangesReport` (CR-043) | per-occurrence locations |
| effective properties | `PropertyResolver` (CR-015); CR-030's table contexts | a colour comparison |
| geometry | `w:pgSz`, `w:pgMar`; `wp:anchor`/`wp:inline`; VML `style` | an off-page test without layout |
| embedded fonts | `FontTablePart.processEmbeddings`, `ObfuscatedFontPart.deObfuscate` (the `.odttf` faces), `org.docx4j.fonts.fop.fonts.truetype.TTFFile` (cmap, advance widths, glyph boxes); `RunFontSelector` (the font a character is drawn in); `Mapper`'s precedence for docx4j's own output (an installed font of the same name first, then the embedded face) | any check of what a glyph draws; the text model reads a run's font name only (`Extractor.MONOSPACE`), so its text is the characters stored, whatever a font draws for them |
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
   properties, every relationship (external, or to a part of an unknown or unsupported kind; a
   broken one is no kind, D20), every embedded and `altChunk` part, every ink part (`w14:contentPart`, an internal
   relationship to an `InkmlPart`), and every embedded font: each face de-obfuscated and read (its
   cmap, advance widths, outlines, glyph names and substitution lookups) for the characters the runs
   drawn in it hold (D18). Only the package's own faces are read, never an installed font, so a
   finding is the same on every machine.
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
- its story is in scope: a header or footer under `include`, a comment under `comments: true` or in
  the markup view (`Extractor.commentsIncluded`: the markup view writes comments by default), the
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
  only under `field_codes: true`; properties in the block model always (its `metadata` object,
  REQ-515), in plain text and Markdown only under `metadata: true`.

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
`font.embedded`, `ink`, `revision.author`, `rsids`, `people`, `part.unknown`).
One exception to unseen: content an output carries as labelled metadata, apart from the document's
text, is info while it is in the output. That is the properties: the block model's `metadata` object,
and the property lines or front matter of plain text and Markdown under `metadata: true`. The agent
holding them is told they are not the page's text; their `visible` stays `no`, since a person sees
them only through File, Info. So a comment is omitted under the defaults and info under `comments:
true` or in the markup view, `text.hidden` omitted under the defaults and unseen under `hidden: true`,
`text.near_background` unseen where its run is in the output and omitted where it is not, and the
properties info in the block model and omitted from plain text until `metadata: true`.

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
| `text.tiny` | drawn under 2pt: `w:szCs` where the run's characters are drawn in the complex-script slot, `w:sz` otherwise | differently | included | unseen | 18, *new* (exactly 2pt: no finding) |
| `chars.zero_width`, `chars.tag`, `chars.bidi`, `chars.private_use` | U+200B-U+200F, U+2060, U+FEFF; U+E0000-U+E007F; U+202A-U+202E, U+2066-U+2069; U+E000-U+F8FF and planes 15-16, except in a symbol or icon font on D17's list (the document's font for the character, `w:sym`'s or the run's) | no | included | unseen | 48, *new*; 29 the negative |
| `font.glyph_blank` | a character an embedded face maps to a glyph with no outline (whitespace, format and combining characters excepted; a character the face does not map is not tested, since the viewer draws it in another font): in the text, nothing on the page | no | included | unseen | *new* |
| `font.glyph_mismatch` | a character an embedded face draws as something else, on positive evidence from the face itself (D18's four tests: a glyph shared with another letter or digit; a letter's advance zero or over 1.5 em; the glyph named for another character; a substitution Word applies making it another letter's glyph), naming what is drawn where a test says; no evidence, no finding; symbol and icon fonts (D17) excepted | differently | included | unseen | *new* |
| `paragraph.collapsed` | the body of a heading marked collapsed (`w15:collapsed`, Word's "Collapsed by default", through its style or direct; a paragraph with an outline level): every paragraph and table after it until the next paragraph at its outline level or above, not shown on open | differently (the heading and its expand arrow shown, the body on a click) | included | unseen | *new* |
| `shape.zero_size`, `shape.off_page`, `shape.hidden` | a text box or shape of extent 0 in a dimension; wholly off the page for every position its anchor could have on the page (D4); `wp:docPr/@hidden` on an anchored shape (Word 365 shows an inline one, phase 0) or VML `visibility:hidden` | no | included where it holds text (`text_boxes: true` is the default), else unextractable | unseen where it holds text, else info | 48, *new* |
| `smartart.data` | SmartArt data its drawing does not show | no | included (`smartart: true`) | unseen | none (case 35 has no drawing part, and Word lays it out from the data) |
| `text.hidden`, `paragraph.hidden` | `w:vanish` on the run or through its styles; a paragraph whose runs and mark are hidden | no | options: `hidden: true` | omitted | 18 |
| `revision.deleted`, `revision.moved_from` | deleted and moved-from runs and paragraphs (Word's default Simple Markup does not show them) | no | options: `view: original` | omitted | 9-15, 17 |
| `mce.branch` | content in an unselected branch, every other kind looked for in it, its text compared with the selected one's | no (an older Office would show it) | unextractable | omitted | 1, 3-8, *new* |
| `story.unused` | a header or footer no section references; a note no reference reaches (not the separator, continuation-separator and continuation-notice notes every notes part holds unreferenced by design: a `w:type` other than `normal`, which extraction skips too); a comment with no anchor. A note whose only reference is deleted (case 27's) is `note`, in the output under `view: original` | no | unextractable | omitted | 26, *new* |
| `sdt.stored_surface` | a bound control's stored surface where it differs from its value, every other kind looked for in it | no (Word 365 shows the bound value on open, phase 0) | options: `bindings: surface` | omitted | 23, *new* |
| `field.code` | a field's instruction text (every field; the external ones are also `field.external`) | no | options: `field_codes: true` | omitted | 19, 20, 49 |
| `glossary` | the glossary's entries | no | options: `include: glossary` | omitted | 54 |
| `alt_text.description` | a picture's or shape's `descr` (a decorative picture's: unextractable) | no (read aloud by a screen reader) | options: `alt_text: true` in plain text; included in Markdown and the block model | omitted | 33 |
| `embedded.sheet_hidden`, `embedded.sheet_very_hidden`, `embedded.slide_hidden` | inside an embedded workbook or presentation (D10) | no | options: `embedded: true` + `hidden: true` | omitted | 53, 57 |
| `embedded.row_hidden`, `embedded.column_hidden` | a hidden row or column of an embedded workbook (the worksheet reader reads its cells: `Embedded.sheet` checks the sheet's state only) | no | options: `embedded: true` | omitted (unseen under `embedded: true`) | *new* |
| `embedded.notes` | a slide's speaker notes in an embedded presentation | no | unextractable (REQ-512 omits them) | omitted | 53 |
| `field.external` | REQ-506's INCLUDETEXT, INCLUDEPICTURE, LINK, IMPORT, DDE, DDEAUTO; a HYPERLINK under the link rule below (a `\l` bookmark is not external); simple and complex forms, keywords in any case, arguments quoted or not | - | options: `field_codes: true` | active | 49, *new* |
| `settings.update_fields` | `w:updateFields` | - | unextractable | active | 50 |
| `relationship.external` | an external target: a linked image, attached template, mail-merge source, OLE link or subdocument; a `w:hyperlink` under the link rule below | - | unextractable (never fetched) | active | 49, 50 |
| `hyperlink.web` | a hyperlink, either form (`w:hyperlink`'s relationship, a HYPERLINK field), to http, https or mailto: Office fetches nothing until a click | yes (its text; its target on hover) | the target included in Markdown (`[text](target)`, REQ-703); else `field_codes: true` for the field form, unextractable for the relationship's | info | 21 (both forms) |
| `part.macro`, `part.activex` | `vbaProject`; an ActiveX control part | - | unextractable | active | 51, 42 |
| `altchunk.script`, `altchunk.external_ref` | `<script>` or an event handler in an HTML, XHTML or MHT chunk; an external `src` or `href` in one | - | unextractable | active | *new* |
| `comment` | a comment, with its author | yes (its presence; its text on click) | options: `comments: true` or `view: markup` | omitted (info under `comments: true` and in the markup view) | 28 |
| `header_footer`, `watermark` | a header or footer a section uses; a watermark's text | yes | options: `include: headers`, `include: footers` | omitted | 26, *new* (the watermark) |
| `note`, `sdt.placeholder`, `chart.cached` | a footnote or endnote; a control showing its placeholder; a chart's cached strings | yes | included | info | 27, 22, 34 |
| `text.web_hidden` | `w:webHidden` (D8) | yes (not in Web Layout) | included | info | 18 |
| `altchunk`, `object.embedded` | each chunk, with its content type; each embedded object, with its ProgID | yes (as Word converts it; as its preview) | options: `altchunks: true` (default) or `embedded: true` for the types REQ-513 and REQ-512 read; unextractable for RTF and other OLE binaries | info | 36, 39-47, 53 |
| `alt_text.title` | a picture's or shape's title | no | unextractable | omitted | 33 |
| `alt_text.name` | its name: the description where `descr` is empty and the picture is not decorative, else not extracted; a default name (matching `[^0-9]+ [0-9]+` in full) only where it is the description (D19) | no | per occurrence: as `alt_text.description` where it is the description, else unextractable | omitted | 33, *new* (a named picture with no `descr`; a decorative one) |
| `properties` | core, app and custom properties | no (File, Info shows them) | included in the block model (`metadata`, REQ-515); options: `metadata: true` in plain text and Markdown | omitted (info in the block model, and under `metadata: true`: labelled metadata) | 38, 52 |
| `font.embedded` | a font face embedded in the package (`w:embedRegular` and its kin) that runs are drawn in, with the number of characters it draws. What a person sees depends on the viewer: whether it uses embedded fonts, and which it prefers where a font of that name is installed (docx4j's PDF output: the installed one, `Mapper`; Word: not yet measured, §3) | - | unextractable (extraction has characters, never glyphs) | info | *new* |
| `ink` | an ink part (`w14:contentPart`), whose strokes may be handwriting (as a picture's pixels may be text) | yes | unextractable | info | *new* |
| `doc_vars`, `custom_xml` | `w:docVars`; each custom XML part | no | unextractable (`bindings: bound` shows what a control binds) | omitted | *new*, 23 and 38 |
| `people` | `people.xml` (the authors' identities) | no | unextractable | info | *new* |
| `revision.author`, `rsids` | each author of the tracked changes, with the dates' range; the revision-session identifiers, one finding with their number | no | options: `view: markup` + `markup_detail: author_date` for authors; else unextractable | info | 9-17; any Word save |
| `part.unknown` | a part of an unknown or unsupported kind (a broken relationship is no kind: D20) | - | unextractable | info | *new* |

**The link rule**, one for both forms of a hyperlink (`w:hyperlink` and the HYPERLINK field): a
target with the scheme http, https or mailto is `hyperlink.web`, info; any other target (a file or
UNC path, a relative path, any other scheme) is active, as `relationship.external` for the
relationship's form and `field.external` for the field's. A bookmark (`w:anchor`, `\l`) is neither.

### 2.4 Identity, location and counting (D6)

- **A finding is a kind at a stored location.** The location is where the content is stored: the
  chain of containing packages (none for the document itself; an embedded or `altChunk` part's name
  for each level), the part name within the innermost one, an element path within it, and, for text,
  a code-point range within the run at that path (its `w:t`, `w:delText` or `w:instrText` text, in
  document order; for a finding merged across runs, its first run's path and offset and its last's);
  plus the extraction address (REQ-518/519) where extraction has one. Two embedded documents that
  both hold `/word/vbaProject.bin` are two locations. A header used by three sections is stored once
  and found once; a field code is at its `w:instrText` runs; an unselected branch's run is at its
  element path inside the branch.
- **Kinds are kept apart.** One run that is hidden, tiny and holds tag characters is three findings,
  one per kind, at the same range. The exception is a hidden paragraph: a paragraph whose runs and
  mark are all hidden is one `paragraph.hidden` finding, not that and a `text.hidden` for each run, since
  it is one divergence.
- **Collapsed headings.** `paragraph.collapsed` is one finding per collapsed heading: its location runs
  from the body's first paragraph to its last, its summary gives their number, and its excerpt is the
  body's start. A collapsed heading inside another's collapsed body adds nothing. The body's other
  kinds (white text in it, say) are found in their own right.
- **Fonts.** `font.embedded` is found once per embedded face; `font.glyph_blank` and
  `font.glyph_mismatch` per run, merged as below, naming the characters affected.
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
  at most 1,000 findings (D11); 16 MB decoded from any one payload and 64 MB in all (an embedded font's
  de-obfuscated bytes are a payload); 5,000,000 elements walked in all; embedded packages and chunks
  one level deep.
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
image); white text whose background is an image; an embedded font whose glyphs are swapped with
care (each letter given another letter's glyph, every glyph unique, of ordinary width and with no
glyph names), which only recognising each glyph would see (D18): `font.embedded` marks the text such
a font draws; and it does not judge intent: no finding says its text is, or is not, an attack (§0). docx4j has a layout
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
  source and the WD's Appendix A. The adapter advertises REQ-901, REQ-902 and REQ-904 to REQ-907 (WD
  0.17), so the pending inspector rows run with the new ones.
- **A new fixture**, `extraction-probe-inspect.docx` (cases 58 on), built by the docx4j session with
  its own builder class as `extraction-probe-structure.docx` was (3dcbb3764), so the Word-checked
  probes are not rebuilt, with the boundary cases the review lists:

  | Area | Cases |
  |---|---|
  | formatting | a direct `w:vanish w:val="false"` over an inherited hidden; a paragraph part hidden, part visible; text at exactly 2pt (no finding) and at 1.5pt; colours just above and just below the threshold; a collapsed heading's body |
  | geometry | a page-relative shape wholly off the page; one partly on it (no finding); one zero in one dimension only; a paragraph-relative position that is indeterminate (no finding, a limitation) |
  | characters | a supplementary private-use character; a bidi override split across runs; Symbol and Wingdings characters (no finding); a `w:sym` whose `w:font` is a text font (a finding) |
  | fields | INCLUDEPICTURE and LINK, simple and complex, keywords in mixed case, arguments quoted and bare; HYPERLINK `\l` (no finding) beside a file and a UNC target |
  | background | highlight against shading; `auto` and transparent fills; table-style shading; a page colour (`w:background`); a background docx4j cannot determine (no finding, a limitation) |
  | inventory | a part of an unknown content type; a broken relationship (dropped: D20); an unreferenced note and an unanchored comment; the separator notes (no finding); `w:docVars`; `people.xml`; an ink part; an HTML chunk with a `<script>`, an event handler and an external image and stylesheet; an embedded docx holding hidden text |
  | links | case 21's https links in both forms (`hyperlink.web`); a `w:hyperlink` to a file path and one to a UNC path (active) |
  | fonts | an open-licence font, renamed, embedded with its glyphs altered so that one word is drawn as another (a wide glyph drawing the word, the word's own letters blank); the same font named like a font the viewer has installed; a run whose every glyph is blank; a letter mapped to a zero-width glyph that has an outline; a glyph named for another letter; two letters sharing a glyph; a careful swap (unique, ordinary glyphs, names stripped: `font.embedded` only, and the limitation); an embedded font left as its foundry made it (`font.embedded` only); an embedded symbol font and an icon font's private-use characters (no finding); a private-use character in a font of neither kind |
  | geometry, further | a shape placed by `wp:align` (on the page by construction: no finding); a shape in an unused header (no section, so no page size: indeterminate) |

  Case 48's off-page box (`ExtractionPhase0Cases`) is column-relative horizontally and
  paragraph-relative vertically, since its VML style has no `mso-position-*-relative`. D4's rule judges
  it off page for every anchor position (its offset is 5000pt), but the rule's proof is the new
  page-relative case, not case 48. Jason's Word-open check, as for every fixture; for the font cases
  it also records what each viewer shows (Word 365, LibreOffice, Word for the web, docx4j's PDF and
  HTML output), the embedded glyphs or a substitute's, and so Word's precedence where a font of the
  embedded one's name is installed, not yet measured. (CR-046 phase
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
- **Bounds.** A document of many small payloads (2,000 chunks of 40 KB, each under the per-payload
  limit, 80 MB in all), built by the test as it runs and never committed, ends `PARTIAL` at the
  total-bytes limit with the rest in `skipped()`, and its counts marked lower bounds; one payload over 16 MB is stopped while it is decoded, the rest inspected;
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
  reveal contract (D12), the symbol and icon fonts (D17) and the font kinds (D18), proposed to the WD:
  REQ-902 names no kind and leaves the threshold to "a documented threshold", REQ-901's `severity` and
  `how_to_see` become `visible`, `reveal` and the group, and REQ-903's three counts become D14's four.
  And a sentence for the WD's section 11: extraction returns characters, never glyphs; an embedded
  font can draw a character as another or as nothing, and viewers differ in whether they use it; the
  inspection reports it.
- `expected-findings.tsv` (every kind, today's names renamed) and `expected-inspection.tsv`; the
  harness's exact-set comparison.
- `extraction-probe-inspect.docx` (§3), its rows, its Word-open check.

**Acceptance:** the rows run as pending (no inspector yet), the suite's other results unchanged; the
new fixture opens in Word without repair.

**Phase 0 record, 19 October 2026.** Built by the docx4j-25 session (Claude Opus 5.5) at Jason's word
("implement CR-050 phase 0"); uncommitted until he says so.

- *The contract*, `text-extraction/INSPECTION.md`: locations (package chain, part, a path with every
  step positioned, code-point ranges within a run's text), excerpts, identity and merging, the branch
  read, membership and the six named outputs, reveal, groups, and every kind's scope, location and
  excerpt. The choices it makes where this CR left them open are marked "(phase 0)" there, for the
  WD proposals and for review: among them `hyperlink.web` is an inventory kind (info whatever the
  output, as the table above has it); case 27's note is `note` under `view: original`, not
  `story.unused`; text deleted inside an insertion is in the markup view only; a TOC field's
  content needs `toc: true`; a column-relative anchor in a one-column section is at the left margin
  (D4's rule otherwise unchanged); a payload the inspector never decodes (an ActiveX binary, an OLE
  binary, a VBA project, a part of unknown type) is reported by its kind and its content is a
  limitation, while a chunk of an undecoded type (RTF) and content beyond one level are `skipped`.
- *The oracle*: `phase0/expected-inspection.tsv`, 209 findings over the four fixtures (54 kinds; none
  of `smartart.data`, `relationship.broken` or `rsids`, whose fixtures the contract explains), and
  `expected-inspection-status.tsv` (the main probe `PARTIAL`: two payloads beyond one level and the
  RTF chunk; three undecided occurrences in the new fixture). Written by
  `support/inspect_oracle.py`, a second reading of the contract in Python (lxml, fontTools), reviewed
  case by case; `ExtractionInspection` fills the extraction addresses from the text model and checks
  every row against its fixture. `expected-findings.tsv`'s inspect rows (kind, selector, count) are
  derived from it, the eight old kind names renamed to §2.3's (D2).
- *The harness*: `Engine.inspect` and `ExtractionConformance.compareInspection`, exact by occurrence
  (a finding missing, unexpected or doubled fails), then each finding's fields, the status and the
  limitations; an inspect row is checked once (an inspection is the package's, whatever the view). The
  rows need REQ-901, 902 and 904 to 907 (WD 0.17), so all stay pending: the suite reports 1345 assertions passing (972
  before, and 373 from the new fixture's token rows, all passing on CR-046's text model) and 222
  pending (the 9 that were pending besides the inspector's, 209 inspect rows and 4 fixture inspections).
- *The fixture*, `extraction-probe-inspect.docx`, cases 58 to 67 (`ExtractionInspectCases`), §3's
  table but the broken relationship: docx4j's `Load3` refuses a package whose relationship names a
  missing part ("cannot find part"), so phase 1 builds that case in its test, and must load such a
  package to report it. A watermark case was added (no fixture had one). Ten embedded faces made from
  Liberation Sans by `support/inspect-fonts/make_fonts.py`, embedded with `FontEmbedder`.
- *Word 365* (`phase0/evidence/word-open-results.md`): no prompt of any kind on open (Jason confirmed). What it measured,
  and what changed here because of it: a stale bound surface is replaced by the bound value on open,
  so `sdt.stored_surface` is not seen (the table above said "yes until Word refreshes"); the page
  colour is shown (with `w:displayBackgroundShape`), so a page-colour background is `differently`,
  not `indeterminate` (D15's example); an inline shape's `wp:docPr/@hidden` is ignored, so it is no
  finding; `w:cs` draws a run at `w:szCs`; a collapsed heading is collapsed on open; Word applies
  `ccmp` always and `liga` only under `w14:ligatures`; Word uses an embedded face, but prefers an
  installed face of the same name (as docx4j's `Mapper` does), so `FNT65_TWIN` drew CAT where
  `FNT65_WORD` drew DOG; Word made the HTML chunk's external image a linked picture and tried to load
  it (a red cross), which `altchunk.external_ref` reports; Word drops the unanchored comment and the
  unreferenced note on save. One result questions D17: Word did not draw the embedded
  symbol-encoded face (U+F041 and kin came out as other symbols) and did not keep its embedding on
  save, so the embedded-face exemption covers characters Word draws from a substitute (for Jason).
  LibreOffice 26.2.5.2 drew none of the embedded faces and ignored `w:cs` for the size. docx4j's PDF
  drew every embedded face (the symbol face too), applied `liga` but not `ccmp`, and showed the stale
  surface; its HTML embeds no font, so a browser draws the stored characters. What a person sees of
  an embedded font therefore depends on the viewer and the machine, which is why D18's tests read the
  face alone. Word for the web is still to record (Jason, later).
- *A defect in CR-046's text model, found by the oracle's address check and fixed*: every header and
  footer block's address began with its part's content's `toString()`
  (`org.docx4j.wml.Ftr@50bb1c1ffooter:rId9/0`): `Extractor.headerOrFooter`'s local variable shadowed
  the address prefix. Now `header:rId4/0`; `TextsTest.headersBySection` asserts the form.
- *Proposed to the WD* (sent to the portfolio session): §0's objective and D14's groups and counts;
  D2's identifiers; D3, D4, D5, D6, D11, D12, D15, D17, D18; REQ-901's fields, REQ-903's four counts,
  §11's sentence (characters, never glyphs), REQ-902's `altchunk.external_ref`, and the oracle's
  format as the suite's. WD 0.17 (portfolio f79bf8b, the same day) adopted the first eight: section 9
  is now 9.1 REQ-901 (the finding, its location and the display profile, with the Word measurements),
  9.2 REQ-902 (the kinds and their rules), 9.3 REQ-905 (membership and the group) and REQ-903 (the four
  counts), 9.4 REQ-906 (identity), REQ-907 (bounds and status) and REQ-904 (the limitation ids); §11
  has the glyph sentence. It records phase 0's own choices (INSPECTION.md's "(phase 0)") as adopted but
  not yet ratified by Jason, and D8, D13, D17's face test and D19 as pending him. Appendix A.4 (cases
  58 to 67), [PROBE] and REQ-1201's inspection rows follow once phase 0 is committed.

**Rulings, 19 October 2026** (Jason: rule "as recommended"): D1 (a), D7 (a), D8, D13, D19 (b), D20
(c) decided; D17 revised to the list alone; D16 closed; the choices `INSPECTION.md` marks "(phase 0)"
ratified. The oracle follows: 186 findings (24 default names gone by D19; case 65's `FNT65_SYMBOL` a
`chars.private_use` finding by D17), no `relationship.broken` kind.

### Phase 1: the inspector

- `Inspect`, `Inspection`, `InspectFinding`, `Group`, `Visible`, `Reveal` in `org.docx4j.model.text`; the
  stored-story pass on the text model's resolution; the inventory; the payload scanner; bounds and
  status; `limitations()`.
- The adapter advertises REQ-901, REQ-902 and REQ-904 to REQ-907 (WD 0.17).

**Acceptance:** REQ-902 and REQ-904; REQ-901 once the WD has adopted D12's reveal contract (D16).
Every inspector row passes, the exact comparison holds on every fixture, `expected-inspection.tsv`'s
fields hold; the no-access, read-only, cross-check, bounds and nested-location tests pass. D18's
tests, run over the embedded fonts of the layout-fidelity corpora (scored only, never committed),
find nothing in a font as its foundry made it; any that do are recorded here with the threshold or
test changed for them.

**Phase 1 record, 19 October 2026.** Built by the docx4j-25 session (Claude Opus 5.5) at Jason's word
("implement CR-050 phase 1"); uncommitted until he says so.

- *The API* (§2.2): `Inspect.of(WordprocessingMLPackage)`; `Inspection` (`findings()`,
  `counts(options, format, capabilities)` with `Format` PLAIN, MARKDOWN, BLOCKS and `Capability`
  XHTML_IMPORTER, `status()`, `skipped()` and `skippedBeyond()`, `undecided()`, `limitations()` = the
  eight REQ-904 statements); `InspectFinding` (the kind constants, `Location`, `getAddress()`,
  `getVisible()`, `getReveal()`, the excerpt bounded at 200 code points with its length,
  `getSummary()`, `getSubject()` (a target, an author, a face's name), `isIn` and `group` for an
  output); `Group`, `Visible` (`-` is `NOT_CONTENT`), `Reveal`. Bounds as §2.5 (`Inspect.MAX_*`).
- *How it reads* (§2.1): each part as a DOM, a JAXB part marshalled as docx4j writes it. Checked first:
  the oracle run on docx4j's resave of the four fixtures gives the same 186 findings, so the marshalled
  form is the stored one for the inspection. The walk is a port of `support/inspect_oracle.py` (package
  private `InspectWalk`, with `InspectPackage`, `InspectXml`, `InspectFace` (its own TrueType reader:
  cmap, glyf, hmtx, post, GSUB single and multiple substitutions, with work caps for a hostile face),
  `InspectPayloads` (jsoup where present, else a tag scanner that skips comments and script bodies; an
  MHT chunk's MIME parts; a zip payload decompressed once against the budgets before docx4j loads it)
  and `InspectRun`). An `mc:AlternateContent`'s branch is the text model's: the marshal links the
  elements the inspector needs to their objects (a `Marshaller.Listener`, each element to the first
  object of its class not yet taken; JAXB's `Binder` linked no `w:p`), and `McSelection.selectedBranch(TEXT)`
  decides, with Requires prefixes resolved where the part was read (case 6's unconventional prefix).
- *Addresses*: the text model's. `Extractor` gains an inert record (`sources`: each paragraph, table,
  bound control, WordArt shape, chart part, workbook cell and slide shape to the first block made from
  it), and the inspection reads the package through it once for each set of options its findings'
  reveals name, calling no importer (`noImporter`: an XHTML chunk read as HTML, an MHT chunk not at
  all; no block's address depends on a chunk's expansion), with the packages it loaded one level down
  (`packages`, so that `Embedded` and `AltChunks` read the same instances). A finding's address is its
  element's nearest recorded ancestor (a run's paragraph) or first recorded descendant (a text box's
  first paragraph, a header's first block); a hidden column's, its first recorded cell's. A report a
  global limit stopped carries none.
- *Results*: all 186 findings, the four fixtures' status, skipped and undecided entries, exact, under
  docx4j-JAXB-ReferenceImpl and (a hand run) docx4j-JAXB-MOXy; the suite 1535 assertions passing and 9
  pending (the XHTML-importer-present rows; with docx4j-ImportXHTML on the class path the four
  inspections pass too). The whole-fixture item cites REQ-1204 with REQ-901, 902 and 904 to 907, which
  `TextsAdapter` now advertises; its selector for `expected-findings.tsv` is the oracle's rule.
- *Tests* (`InspectTest`, 15; 22 after the review below): read-only (REQ-204's snapshot) on the four fixtures; nothing fetched, a
  loopback listener named by HTML, XHTML and MHT chunks, INCLUDEPICTURE, INCLUDETEXT and HYPERLINK fields,
  an external image and an embedded document's own chunk (also run by hand with docx4j-ImportXHTML's
  no-fetch build on the class path: 17 tests with `AltChunkNoFetchTest`, no request); the anonymiser's
  inventory (19 parts PartsAnalyzer treats, every external relationship `Verify.extract` reads); bounds:
  800 chunks of 100 KB end PARTIAL at `limit.total_bytes` with the rest skipped and the counts lower
  bounds (§3's 2,000 chunks of 40 KB predate D11: at one `altchunk` finding a chunk, the 1,000-findings
  limit stops them first, at 40 MB), a 17 MB chunk and an embedded docx that decompresses to 17 MB each
  stopped alone, 1,200 hidden paragraphs stopped at 1,000 findings, the elements counted as a part is
  marshalled and the inspection stopped past them (with a budget of 500, the 5,000,000 being the same
  code), 1,100 RTF chunks giving 1,000 skipped entries and the rest counted; two embedded documents each with a VBA project and hidden text
  at one path, four locations; `w:rsids`; membership under the views and options (inserted content is
  not in the original view, WD REQ-905, which INSPECTION.md's table lacked and now has); on the fixtures,
  a description, a comment, the properties and a hidden row; counts equal to the groups' tally and
  unchanged by splitting every run in two; the tag scanner agreeing with jsoup. File access is not
  tested apart: no code path resolves a target.
- *A defect in the text model, found by the read-only test and fixed*: `Texts.of` added a
  `DefaultParagraphFont` style to `extraction-probe-structure.docx`, whose styles declare no default
  character style: `PropertyResolver`'s creation called `StyleDefinitionsPart.getDefaultCharacterStyle()`,
  which makes one and adds it to the part. `PropertyResolver.init` now reads the declared default only
  (the id it used only as `activateStyle`'s stop); output and merge paths that call
  `getDefaultCharacterStyle()` still add it. Shared code: for review. `TextsPhase2Test` asserts it.
- *D18's calibration* (§6). The layout-fidelity corpora hold 861 documents with embedded fonts, 97
  unique faces, all TrueType. As first written, the tests found 27 runs in four documents drawn in
  faces as their foundries made them (all-capitals faces draw `telefoonnummer` with the capitals'
  glyphs), and over every letter and digit the faces map, 59 of the 97 faces: ambiguous AGL names
  (Omega names U+2126, mu U+00B5, Tcommaaccent U+0162), stylistic suffixes (`h.sups` for ʰ), `ccmp`
  decomposing À into A and an accent, Arabic presentation forms sharing glyphs, zero-width modifier
  letters. Changed, in `InspectWalk.mismatch`, the oracle and INSPECTION.md (marked "phase 1's
  calibration"): a compatibility character (one NFKC changes) is not tested; "drawn as" another
  character is evidence only where that one is a letter or digit, no compatibility character, and not
  the same letter, two being the same letter when their bases match (NFKD with marks, modifier letters
  and spaces dropped, case folded, ı ȷ ĸ ð đ folded to i j k d d), when either has no base, or when
  their scripts differ (test 1's exceptions, now every test's); a modifier letter's advance is not
  tested, nor a titlecase letter's width; glyph names are read against the full AGL (xmlgraphics-commons'
  `glyphlist.txt`, fontTools' list). Now: none of the 861 documents has a finding, and 4 of the 97
  faces have a letter flagged: a Code 39 barcode face, whose letters are bars 2 em wide (a person sees
  a barcode, not the text: arguably a true finding, for Jason); Cambria's `ccmp` drawing Ӫ and ӫ
  through fita's glyph, a Cyrillic look-alike; a display face sharing glyphs between unrelated Cyrillic
  letters (і and palochka, ѐ and Ѝ); Arial's Arabic high hamza (U+0674) with no advance. Each fires only
  where a document uses that letter in that face. The Java and Python readings agree on all 97 faces (69
  letters). The fixture's ten faces give the same findings as before. To the WD: REQ-902's Embedded
  faces paragraph, as INSPECTION.md now reads.
- *Deviations from the oracle*, none moving a fixture's finding: inserted content's need (above);
  `font.embedded` one finding per face part (the oracle keyed faces by font name; §2.4 says per face),
  its summary the characters it draws; the tag scanner takes single-quoted and bare attribute values.
- *Cost*, for phase 2 (D9): the main probe's inspection takes about 1.9 s on a cold JVM, most of it
  the text model read once per distinct reveal (13 on the main probe); the other fixtures 10 to 150 ms.
  The review below adds: the counts need no addresses (`InspectFinding.group` reads a finding's
  requirements, `visible` and its excerpt's length alone), so phase 2 can run the walk without
  `InspectAddresses`, which is most of that time, and keep the address reads for `Inspect.of`.
- *Review*, 19 October 2026: the docx4j-43 session, at Jason's request, ran 18 classes (144
  tests) green from the working tree, checked the DOM reading, the importer left uncalled, the zip
  budgets, CR-049's prefixes and the `PropertyResolver` change, and found two defects and six lesser
  points, all taken. The oracle follows each, its output unchanged (186 findings); `INSPECTION.md`
  marks them **(phase 1)**; `InspectTest` gains seven tests.
  - `smartart.data` was never made, though REQ-902 names it. Now each point the text model reads
    with a line no paragraph of the diagram's drawing part holds is one finding, at its `dgm:pt`,
    where the data part names a drawing part (`dsp:dataModelExt`); its address is the point's first
    line's block (`Extractor` records each point). Word's save of the main probe has none; that save
    with its drawing made stale has one, at `body/105/smartart:0/2`.
  - `InspectFace`'s cmap work cap was a subtable's: a face whose 65,535 encoding records each name a
    large table cost 65,535 caps. Now one cap a face, at most 64 records read, a table two records
    share read once; and the cap is 262,144 code points (from 2,228,224), nine times the most any of
    the corpora's 97 faces maps (28,737), so that one hostile face's map stays small. A face with
    65,535 records of the whole of Unicode reads in milliseconds.
  - Membership, each checked against the text model's output: a row deleted (inserted) by its own
    `w:trPr` mark, its runs in no revision, and a cell by `w:cellDel` (`w:cellIns`), is out of the
    accepted (original) view, as Accept All (Reject All) removes it, where the walk had it in; content
    of a control showing its placeholder needs `placeholders: true`; a note an unused header alone
    refers to is `story.unused`, and one a used header (footer) alone refers to needs
    `include: headers` (`footers`), where the walk had both as notes of the default output.
  - Chunks: an HTML or XHTML chunk is read in its encoding (a byte order mark, UTF-16 by its first
    `<`, a declared `encoding` or `charset`, else UTF-8), where a UTF-16 one scanned as nothing; a
    `javascript:` URI in a `src`, `href` or `background` is `altchunk.script`; the tag scanner's
    search for a script's end no longer lower-cases the whole chunk at each script.
  - Open, for Jason: the faces' maps are kept for the walk, so a package of many small hostile faces
    (each mapping 262,144 code points, of the order of 20 MB in memory) has no bound across faces. A
    limit on the faces read, or the maps' size charged to `limit.total_bytes`, would close it; either
    is REQ-907's to state. Ruled before phase 2 (below): the latter.

**Rulings before phase 2, 19 October 2026** (Jason):
- D9 (a): `inspect: false`, a docx4j-only option; the measured cost chooses its default (§6).
- Each embedded face's character map counts, by its size, against the 64 MB of `limit.total_bytes`
  (REQ-907), with no new limit: phase 2's first step, the maps held compactly. To the WD.
- A comment with no anchor in the document (no `w:commentReference` and no range, case 63) is left out
  of the text model's comments, the block model's included, since Word drops it on save; the
  inspector's `story.unused` (never in an output) then holds for every format. A text-model change in
  phase 2, and a line for REQ-504 to the WD.

### Phase 2: the counts in every extraction result

- `TextResult.metadata()`'s `inspect` (REQ-903 as D14 has it), the four counts against the result's own
  options for the block model, plain text and Markdown, from the inspection run with the extraction; its cost
  measured on the probe and on the layout-fidelity corpora (the real-document sets), and recorded here.
- `inspect: false`, a docx4j option that skips the counts (D9), shipped with them and documented as
  making the result not an inspector's (WD §12.1); the measured cost chooses its default.
- A result with no package has no counts: `Texts.of(Part, options)` and `Texts.of(Object, options)`
  carry no `inspect` entry, since the stored-story pass and the inventory need the package; their
  javadoc says so.
- First, from the rulings above: each face's character map held compactly and charged to
  `limit.total_bytes`; an unanchored comment left out of the text model's comments.
- The counts need no addresses (`InspectFinding.group` reads a finding's requirements, `visible` and
  its excerpt's length), so the inspection run with an extraction skips `InspectAddresses`.

**Acceptance:** REQ-903 once the WD has adopted D14's counts (D16); the inspector conformance class of
WD §12.1 claimed then, and not before.

**Phase 2 record, 19 October 2026.** Built by the docx4j-25 session (Claude Opus 5.5) at Jason's word
("start CR-050 phase 2"); uncommitted until he says so.

- *The counts* (§2.2, REQ-903, WD 0.17's D14 form): `Texts.of(WordprocessingMLPackage, TextOptions)`,
  under `inspect: true`, inspects the package after extracting it, and its result's metadata carries
  `inspect` (`TextResult.INSPECT`): `status` (`COMPLETE` or `PARTIAL`), `lower_bound`, and for `plain`,
  `markdown` and `blocks` the four counts, unseen first, under the result's own options;
  `TextResult.counts(Inspection.Format)` gives them typed. The capability is docx4j-ImportXHTML on the
  class path. That inspection makes no addresses (`InspectAddresses.findings(.., false)`: a group needs a
  finding's requirements, `visible` and excerpt length alone), so it costs the three passes and no text
  model reading. One that fails (an exception, not a limit) does not fail the extraction: what it found
  is counted, it is `PARTIAL` with `unreadable` at the main document, and it is logged. A part's, a
  fragment's (`Texts.of(pkg, content, ..)`) and a resolved copy's results carry none (javadoc). The
  counts stay out of the text: `PlainText` and `MarkdownExporter` write the core, app and custom
  properties only (REQ-802).
- *`inspect`* (D9 (a)): `TextOptions.inspect(boolean)`, by name through `TextOptions.of(Map)` and
  `set`; outside `NAMES` and `toMap()` (WD §10's), shown by `toString()` when on. **Off by default, by
  D9's rule**, measured with a scratch driver (score only; the corpora are not committed), each
  document loaded, then `Texts.of` with `inspect: false` timed against the counts' inspection alone,
  over two passes with the second timed (a warm JVM):
  - the layout-fidelity corpus, 453 documents (a 454th refused by the loader's zip-bomb guard):
    extraction 11.5 s, inspection 9.5 s, **0.83** of the extraction; per document, median 0.75, 90th
    percentile 0.94, at most 5.0;
  - Word's re-saves of its first batch, 191 documents: 2.3 s and 1.7 s, **0.73**; median 0.74, at most 1.29;
  - the fixtures (median of ten): the main probe 19 ms and 24 ms (phase 1's 1.9 s was the address
    readings, a cold JVM); `extraction-probe-inspect.docx` 8 and 12 ms; the other two 1 to 4 ms.

  So the counts cost about four fifths of an extraction, over D9's fifth, and the cost is the walk
  itself (every story, branch, payload and face up to the bounds, whatever the options), not anything
  optional in it. A host whose reader is an agent asks for them: docx4j-mcp's `extract_text` (phase 3).
  docx4j's default results are therefore not an inspector's (WD §12.1); docx4j claims the inspector class
  under `inspect: true`, as `TextsAdapter` runs.
- *Found by the measurement, for Jason*: two of the 453 documents end `PARTIAL` at `limit.findings`
  (D11's 1,000), each with a long table of contents: one `field.code` finding for each entry's PAGEREF
  field (850 and 1,000) and a `text.web_hidden` for each entry's tab and page number (150). An
  ordinary document's counts are then lower bounds. Options: count a TOC field's own fields and
  web-hidden runs as one finding per TOC (as a collapsed heading's body is one), or raise the limit.
  Recommended: the former, a TOC being generated content; a change to REQ-906 and the oracle. As the
  review below refines it: what the TOC's own code generates (its PAGEREF fields, its HYPERLINK fields
  to a `_Toc` bookmark, the web-hidden runs inside its span) is one `field.code` (at the TOC's begin,
  its excerpt the TOC's own instruction, its summary the number of fields) and one `text.web_hidden` a
  TOC; any other field inside the span (an external HYPERLINK, INCLUDETEXT, every REQ-506 kind) stays
  its own finding, an active kind never swallowed. Raising the limit would only move the cliff.
- *The faces' tables, charged to the total* (the ruling before phase 2; REQ-907): `InspectFace` holds
  its character map as three arrays (the code points sorted, a glyph for each, the indices by glyph in
  the map's order), 10 bytes a code point where a map of objects took some 80; with it are charged its
  outlines and advances (5 bytes a glyph) and its glyph names (each its length and 48 bytes: 65,535
  glyphs given one 255-character name become 65,535 names, fontTools' `#n` making them unique, some
  20 MB from a small face), and its table directory keeps only the nine tables read. What a face holds
  is charged to `limit.total_bytes` as it is read, after its bytes as a payload; one that would pass
  what the total has left stops the inspection, as a payload past the total does. Checked against
  phase 1's map (its code compiled beside the new) on the corpora's 97 faces and the fixture's 11:
  every code point's glyph, every glyph's code points in order and every name equal. The 97 hold 0.22
  of their bytes in all, one face at most 1.5 times its own (431 KB). `InspectCountsTest`: 40 faces of
  half a megabyte, each mapping 262,144 code points, their bytes 21 MB, stop at the total after some
  twenty, where phase 1 held 40 maps of the order of 20 MB each.
- *An unanchored comment* (the ruling): `Extractor` leaves out of `TextResult.comments()` a comment
  with no `w:commentReference` or `w:commentRangeStart` in the main document, a header or footer, or a
  note, in any branch. The inspector's anchors gain the notes too: a comment anchored in a footnote was
  `story.unused`, and is `comment` (oracle, `INSPECTION.md` **(phase 2)**, `InspectWalk`).
- *A note read through its best reference* (a phase 1 defect, from the docx4j-portfolio session's
  question the same day): the walk kept "every reference deleted" and "the best story" apart, so a note
  whose main-document reference is deleted and whose used header's is live needed `notes` alone, in the
  default output, which the text model leaves it out of. Now each reference brings its own
  requirements (its story's option, and what its containers need: its revision's view, a text box, a
  TOC, a stale control's surface, `hidden: true` for a hidden run; one in an unselected branch reaches
  nothing), and the note takes its best reference's: the fewest requirements beyond the defaults, then
  live, inserted, deleted, deleted inside an insertion, then main, header, footer. REQ-905 is a
  conjunction, so two references of different requirements are approximated by the best's: under
  options that meet another's and not the best's, the text holds the note and the counts have it out
  (a note hidden in the body and live in a header, read with `include: headers`). `INSPECTION.md`
  states the rule **(phase 2)**; the oracle follows; its output is unchanged (186 findings).
- *The suite*: a REQ-903 check, for each fixture and each of `expected-inspection.tsv`'s six outputs,
  that the counts a result carries are the tally of the table's groups there, with the fixture's
  status (24 assertions; `Engine.counts`). `TextsAdapter` advertises REQ-903 and extracts with
  `inspect: true`. 1559 assertions pass, 9 pending (the XHTML-importer-present rows, as before).
- *Tests* (`InspectCountsTest`, 9): every result's counts equal `Inspect.of`'s under nine option sets
  on the four fixtures, in each format, the metadata map's form and order with them; the counts follow
  the options and their sum does not; nothing changed (REQ-204's snapshot); `inspect` off by default,
  by name, the text the same either way; parts, fragments and a resolved copy carry none; a face's
  compact map and its allowance; many wide faces reaching the total; an unanchored comment, and one
  anchored in a note; a note's best reference against the text model, under the defaults and its
  reveal's options always, under six option sets where its references agree.
- *Review*, 19 October 2026: the docx4j-43 session, at Jason's request, ran `InspectCountsTest`,
  `InspectTest`, `ExtractionConformanceTest` and `TextsPhase2Test` green from the working tree and found
  no defect to fix before commit; five lesser points, taken or recorded:
  - a format 12 group's start glyph is unsigned: read signed, 0xFFFFFFFE passed the filter and the
    compact map's `char` made it glyph 65534, a real glyph in a full face (phase 1 held -2, no outline).
    Now a glyph read negative is dropped as past the face's glyphs; `InspectCountsTest` builds one;
  - the REQ-903 check tallied the table's groups with no capability while the result's counts take the
    class path's: it now requires the importer absent, pending with docx4j-ImportXHTML present, as the
    goldens do;
  - the WD: the note's best-reference rule supersedes WD 0.19's NOTE (phase 1's rule), and `inspect` is
    proposed as a NOTE to REQ-903 (an implementation MAY make the counts an option of its own under
    REQ-1001, MUST document its default, and claims the inspector class for the extractions made with
    it on, its other results an extractor's; docx4j's name `inspect`, so that the ports share it), with
    the faces' charge (REQ-907) and the unanchored comment (REQ-504): all sent with this phase's commit;
  - under `inspect: true` an embedded package is loaded twice, by the extraction and by the inspection:
    a later saving (the measurement includes it);
  - after a failed walk the package was left on the walking stack: cleared (nothing read it).

  Confirmed by the review: the compact map's packing (fewer than 2^20 pairs under the one budget; code
  points within 21 bits; glyph, place and index unambiguous) and its order, the phase 1 map's;
  `TraversalUtil` read-only for REQ-204 (a parent pointer set in memory only) and reaching every place
  an anchor can be (text boxes of both kinds, controls, both branches, range markers in a body, table,
  row or cell); the walk's hidden test the rule its `text.hidden` applies; the ranking, and a stale
  control's reference given `bindings: surface` (the stale set is filled before the references are
  collected). D9's default stands; the claim's configuration (`inspect: true`, as `TextsAdapter` runs) is
  stated above.

### Phase 3: hand-offs

- **docx4j-mcp**: an `inspect` tool (the WD's verb, D29), the findings as JSON, and `extract_text`'s
  result carrying the four counts and the status, the unseen count first: it is the one an agent
  reading the text needs before it acts on it. `extract_text` turns docx4j's `inspect` on by default
  (phase 2: off in docx4j-core for its cost), its reader being the agent §0 is about.
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
  **Decided** (Jason, 19 October 2026): (a).
- **D2. Kind identifiers.** Options: (a) dotted identifiers (§2.3), proposed to the WD, the suite's
  eight names renamed; (b) keep the suite's phrases and name the rest alike. Recommended: (a), as
  the warnings' (`mce.branches_differ`); a JSON consumer needs stable tokens, and the suite's
  spelling (`colour`) differs from the WD's (`color`).
  **Decided** (Jason, 19 October 2026): (a).
- **D3. The colour threshold.** Options: (a) the WCAG 2 contrast ratio between the run's effective
  colour and its effective background below 1.5:1 (white on white is 1.0, yellow on white 1.07,
  `#EEEEEE` on white 1.16; the palette's "Background 1, Darker 5%" (`#F2F2F2`) 1.12 and "Darker 15%"
  (`#D9D9D9`) 1.41, both flagged; "Darker 25%" (`#BFBFBF`) 1.84, not); (b) a distance in sRGB or CIELAB;
  (c) exact equality only. Recommended: (a), documented in the WD as REQ-902 asks. The effective
  background is the highlight, else the run's shading, else the paragraph's, else the cell's (table
  style in context), else the page colour, else white; an `auto` colour is never near its background
  (Word draws it in contrast); a background docx4j cannot determine gives no finding, and is a
  limitation.
  **Decided** (Jason, 19 October 2026): (a).
- **D4. Off page without layout.** Options: (a) a shape is `off page` when it lies wholly outside the
  page for every position its anchor could have on the page: for an offset relative to the page,
  exactly; relative to the margin, from `w:pgMar`; relative to a column, paragraph, line or character,
  when the offset and the extent put it outside the page wherever on the page the anchor falls
  (case 48's -5000pt); otherwise indeterminate, no finding, and a limitation; (b) only page- and
  margin-relative positions; (c) judge paragraph-relative positions from where the paragraph falls,
  which needs layout. Recommended: (a). (The first draft's (b) would not have found case 48's box,
  which the suite expects.)
  **Decided** (Jason, 19 October 2026): (a). A layout-aware position test, (c), is a possible
  later enhancement (D7).
- **D5. The excerpt.** 200 code points, its original length and a cut flag. Options: 200, 100,
  unbounded. Recommended: 200.
  **Decided** (Jason, 19 October 2026): 200 code points.
- **D6. Identity and counting.** Recommended as §2.4.
  **Decided** (Jason, 19 October 2026): as §2.4.
- **D7. Layout findings.** Options: (a) none here, REQ-904's statement given; (b) a phase using the FO
  pass. Recommended: (a); a layout phase is its own CR with its own gate.
  **Decided** (Jason, 19 October 2026): (a).
- **D8. `w:webHidden`.** Word shows it on the page and hides it only in Web Layout; extraction reads it
  (REQ-507 is `w:vanish`). Recommended: `info`, `visible: yes`, `reveal: included`: a person reading
  the page sees it, so it is not a divergence.
  **Decided** (Jason, 19 October 2026): as recommended.
- **D9. Skipping the counts.** **Decided (a)** (Jason, 19 October 2026, before phase 2; the default
  from phase 2's measurement: **off**, the counts costing 0.73 to 0.83 of an extraction on the
  real-document corpora, §5's phase 2 record; asked for by `inspect: true`). Options: (a) a docx4j-only option (`inspect: false`, outside WD §10, as
  REQ-1001 allows), the result then not an inspector's; (b) counts always. Recommended: (a), to be
  decided now rather than at phase 2, with the default chosen there from the measured cost (off if
  the inspection costs more than a fifth of the extraction on the real-document corpora). The cost is
  structural, not only
  measured: once counts ride on every result, every `Texts.of(pkg)` runs the three passes, opening
  every embedded package, chunk and font up to §2.5's bounds even under `embedded: false` and
  `altchunks: false`, so the bounds that keep a hostile document from exhausting the inspector
  become the price of every extraction, docx4j-mcp's `extract_text` included. A part's or a
  fragment's result carries no counts (§5, phase 2).
- **D10. Spreadsheets and presentations.** Inside a docx, an embedded workbook's and presentation's
  hidden content is inspected (§2.3's `embedded.*` kinds, cases 53 and 57 and new ones). A
  standalone xlsx or pptx is outside the WD, which is WordprocessingML's. Recommended: a later CR for
  those, over the anonymiser's xlsx and pptx coverage.
- **D11. The whole report's bounds.** Options: 10,000 findings and 16 MB decoded per payload, one level
  deep, with `PARTIAL` past either; or no bounds. Recommended: the bounds, so a hostile document cannot
  make the inspection the denial of service.
  **Decided** (Jason, 19 October 2026): the bounds, with at most 1,000 findings (not 10,000).
- **D12. The reveal contract.** Options: (a) §2.3's three forms, with composed option lists, proposed
  to the WD as REQ-901's `how_to_see`; (b) one option name per finding, as REQ-901 reads now.
  Recommended: (a): (b) would tell an agent that `alt_text: true` reveals a picture's title, or that
  `altchunks: true` reveals an RTF chunk, neither of which is so.
  **Decided** (Jason, 19 October 2026): (a).
- **D13. Collapsed headings and rsids** (in the paper's §12.3, not in REQ-902). Recommended: both in, as
  §2.3: a collapsed heading's body is text not shown on open; the rsids are one `info` finding, their
  number, since they identify editing sessions. A collapsed body is one finding per collapsed heading,
  not one per paragraph (§2.4), so that a long section neither floods the counts nor spends D11's
  1,000 findings. Only `w15:collapsed` counts: Word's "Collapsed by default", which the file stores. A
  heading collapsed while editing is view state that Word does not save, so most documents have
  none. Other viewers may ignore it; the fixture's Word-open check records what Word does.
  **Decided** (Jason, 19 October 2026): both in. Word 365 collapsed case 58's heading on open.
- **D14. Groups and counts by objective** (Jason, 19 October 2026: frame the CR by its objective).
  Options: (a) §2.3's four groups, unseen, omitted, active and info, from each finding's `visible` and
  `reveal` against a set of options, with REQ-903's counts `{unseen, omitted, active, info}` against
  the extraction's own options, view and output format; (b) REQ-903 as it reads, `{hidden, external, info}`, fixed
  severities. Recommended: (a), proposed to the WD. Under (b) "hidden" holds both `w:vanish` text,
  which the agent does not hold, and white text, which it does; for an agent reading the text the
  second is the urgent one, and only (a) counts it apart, and counts it again when an option moves
  hidden content into the text. Under (a), content an output carries as labelled metadata (the
  properties) is info, not unseen (§2.3): otherwise every block-model result would report its title
  and author as unseen, for every document.
  **Decided** (Jason, 19 October 2026): (a).
- **D15. Visibility's reference.** Options: (a) §2.3's display profile (Word 365 with its installation
  defaults: Print Layout, Simple Markup, comment markers, hidden text and field codes off, collapsed
  headings collapsed), with `visible: indeterminate` where the document or a user option decides and
  the file cannot settle it statically; (b) each display Word offers, a `visible` per display.
  Recommended: (a). (The previous draft's "Word's defaults, the document's own view settings
  honoured" could conflict with itself; the profile fixes one reading, and `indeterminate` says where
  a static inspector cannot know.) An `indeterminate` occurrence in the output counts as unseen, since
  the warning is that a person may not see it.
  **Decided** (Jason, 19 October 2026): (a).
- **D16. Conformance against the WD as it stands.** WD 0.16's REQ-901 asks for `severity` and
  `how_to_see`, and REQ-903 for `{hidden, external, info}`; D12 and D14 propose a different contract.
  Options: (a) claim REQ-901 and REQ-903 only once the WD has adopted D12 and D14, and until then
  emit, beside the new fields, the WD's current ones by a documented mapping (`severity`: `hidden` for
  unseen and omitted findings, `external` for active ones, `info` for the rest; `how_to_see`: the
  reveal's options, `included`, or `unextractable`; the counts likewise); (b) implement only the WD as
  it stands. Recommended: (a): the gate keeps docx4j honest about what it conforms to, and the mapping
  lets docx4j-mcp ship before the WD moves.
  **Closed** (Jason, 19 October 2026): overtaken. WD 0.17 (portfolio f79bf8b) adopted D12's reveal and
  D14's counts, so docx4j claims REQ-901 to 907 against the WD as it reads, with no mapping to
  `severity` and `how_to_see`.
- **D17. Symbol and icon fonts** (for `chars.private_use` and `font.glyph_mismatch`). A private-use
  character is no divergence where the font it is set in keeps its glyphs there by design. That font
  is the document's font name for the character, as `RunFontSelector` chooses it before any mapping
  to an installed face: a `w:sym`'s `w:font`, else the run's font for the character's slot. A `w:sym`
  has no exemption of its own, since one set in a text font draws a box or nothing, as a run would.
  docx4j already knows five such fonts: `RunFontSelector.symbolFontName` recognises Symbol,
  Webdings, Wingdings, Wingdings 2 and Wingdings 3 (case-insensitively), and `SymbolMapper` holds a
  Unicode table for each, which the HTML and PDF output use. The text model's `symbols: unicode`
  (REQ-601) could use the same tables; `TextOptions` rejects it saying docx4j has none, which is
  wrong, and is a CR-046 follow-up. `PhysicalFonts.isSymbolEncoded` tests a face (glyphs at both
  U+F041 and U+F061; measured on the five Windows faces and the Noto, DejaVu and Liberation sets).
  Options:
  - (a) a documented list, plus an embedded face that passes `isSymbolEncoded`'s test; installed
    fonts are never consulted. The list: Word's symbol fonts (the five, Marlett, MT Extra, Bookshelf
    Symbol 7, MS Outlook, MS Reference Specialty) and the icon fonts that keep their glyphs in the
    private-use area (Segoe MDL2 Assets and Segoe Fluent Icons at U+E700-U+F8FF, Font Awesome's
    families by the prefixes "Font Awesome" and "FontAwesome"), names compared as `symbolFontName`
    does. Proposed to the WD, so the ports share it;
  - (b) (a), with installed faces tested too. This catches a symbol font that is on no list, but
    makes a finding depend on the inspecting machine's fonts, so two machines, or docx4j and a port,
    disagree on one document, and §3's exact comparison cannot hold;
  - (c) the list alone, with no face test. An embedded symbol-encoded font that is not on the list
    is then a false finding;
  - (d) Symbol, Wingdings, Webdings and `w:sym` only, as the second draft had it, which cries wolf on
    every icon font.
  Recommended: (a). Its inputs, the document's font names and the package's own embedded faces,
  are the same on every machine, which (b) gives up; it differs from (c) only where the document
  carries the evidence itself.
  **Decided** (Jason, 19 October 2026): (a). **Revised** the same day, after phase 0's Word check: (c),
  the list alone. Word 365 did not draw case 65's embedded symbol-encoded face (other symbols came
  out) and dropped its embedding on save, so a face's own mapping is no evidence that a person sees
  the characters as the font intends; docx4j's PDF did draw it. An embedded symbol font off the list
  is then a finding, as (c) said; to reopen if a symbol font Word embeds itself is shown drawn.
- **D18. Embedded fonts** (Jason's question: a font whose glyphs are swapped makes one word look like
  another, and only to the viewers that use the embedded font). The text holds the characters stored;
  a font maps each to a glyph that can draw anything, so one glyph can draw a whole word and the
  word's own letters can be blank. A document can only deliver a font by embedding it, so the fonts
  a document names but does not embed are out of scope. Every test reads the package's own faces and
  nothing installed, as D17 does, so a finding is the same on every machine. Options:
  - (a) Three kinds:
    - `font.embedded` (info), once per embedded face that text is drawn in;
    - `font.glyph_blank` (unseen), for a character the face maps to a glyph with no outline;
      whitespace, format and combining characters are excepted, and a character the face does not
      map is not tested, since the viewer draws it in another font;
    - `font.glyph_mismatch` (unseen, visible `differently`), on positive evidence only, from four
      tests:
      - two letters or digits (general categories L and Nd) share a glyph, excepting the same letter
        in another script (Latin A and Greek Alpha) and Unicode-equivalent pairs (NFKC);
      - a letter's advance is zero while its glyph has an outline (it overprints the next), or is
        over 1.5 em;
      - the glyph's name in the face's post table is the Adobe Glyph List name of another character
        (the glyph for A named M). An attacker can strip the names, and then this test finds nothing;
        a name outside the list (glyph23) is no evidence;
      - a substitution lookup replaces a letter with a glyph the cmap gives another letter, in a
        feature Word applies to the run: the ones it applies always, and ligatures and contextual
        alternates as the run's `w14:ligatures` and `w14:cntxtAlts` say. Which ones Word applies
        always is the probe's to measure (§3).

      A finding names what is drawn where a test says (the other letter, the glyph's name). Where no
      test fires there is no finding: a careful swap, with every glyph unique, of ordinary width and
      unnamed, passes them all. That is a limitation (§2.6), with `font.embedded` marking where it
      could be. Symbol and icon fonts (D17) are excepted from the mismatch tests, not from the blank
      one.
  - (b) `font.embedded` and `font.glyph_blank` only.
  - (c) (a), plus a rendering test: each glyph drawn and recognised against a reference font by a
    classifier, the one test that sees a careful swap. A later CR.
  - (d) The previous form of (a): `indeterminate` where no test decides, and a fifth test that
    compares outlines with an installed font of the same name. Not recommended: D15 counts an
    indeterminate occurrence as unseen, so every character of every embedded font would be reported
    as unseen. And the installed comparison makes a finding depend on the inspecting machine, as
    D17's (b) would.

  Recommended: (a) now, and (c) later. The blank test is exact and cheap, and it is the hidden
  instruction's case. The mismatch tests catch the crude swaps on evidence the file itself carries,
  and stay silent otherwise. Phase 1 calibrates them on the real-document corpora (§5): no finding in
  a font as its foundry made it.
  **Decided** (Jason, 19 October 2026): as recommended, (a) now and (c) later. **Calibrated** in phase 1
  (§5, "Phase 1 record"): test 1's exceptions extended to every test with a base-letter comparison,
  compatibility characters untested, modifier letters' advances and titlecase widths untested; four
  faces of 97 still flag a letter (a barcode face among them), none of 861 documents; for Jason.
- **D19. A drawing's name** (phase 0). Every `wp:docPr` carries a name, and most are the ones Word or a
  producer gives ("Text Box 3", "Picture 7"): `alt_text.name` is 28 of the oracle's 209 findings,
  nearly all default names, each counted omitted. Options: (a) every name, as §2.3 reads (the oracle
  today); (b) a name only where extraction can carry it (a picture with no `descr`, its description)
  or where it is not of the form a word or words, a space and a number; (c) only where extraction can
  carry it. Recommended: (b): the omitted count is the signal an agent reads, and a default name is no
  divergence, while a name that is text (case 66's `PIC66_NAME_ONLY`, or an instruction) is kept.
  (b)'s pattern is language-independent but not exact; (c) loses a name holding text on a shape.
  **Decided** (Jason, 19 October 2026): (b), a default name being one that matches `[^0-9]+ [0-9]+`
  in full (ASCII digits). The oracle loses 24 of its 28 names.
- **D20. A broken relationship** (phase 0). docx4j's loader refuses a package whose internal
  relationship names a missing part (`Load3`: "cannot find part"), so `Inspect.of(pkg)` never sees
  one. Options: (a) make `Load3` tolerate a dangling relationship it does not need (widely shared
  code; whether Word opens such a file is unmeasured); (b) an entry point that inspects the zip
  without loading it; (c) drop `relationship.broken`: a package docx4j cannot load gives a load
  error, not an inspection. Recommended: (c).
  **Decided** (Jason, 19 October 2026): (c).

## 7. Dependencies, risks and out of scope

- **Depends on:** CR-046 (done: the resolution, the addresses, the views, the suite); CR-049 (done: the
  read-only snapshot compares declarations).
- **A defect found while drafting, outside this CR:** CR-046's extraction fetches external resources
  through docx4j-ImportXHTML when it is on the class path (§1's last row), against WD §11, REQ-506 and
  REQ-513, and lets its `[MISSING IMAGE ...]` placeholder into the text. docx4j-mcp ships
  ImportXHTML, so its `extract_text` does the same. **Fixed** in CR-046's text model on 19 October 2026
  (an image handler that adds nothing and a user agent that refuses every resource, from
  docx4j-ImportXHTML 17.3.4 through its `setExternalResourcesAllowed(false)`; CR-046 records it).
  docx4j-mcp has the fix once its engine is 17.3.4.
- **False positives** are the risk that matters to an agent: a report that cries wolf is ignored. The
  exact-set comparison, the boundary cases of §3, and the colour rule's resolution of theme colours,
  table-style shading and `auto` before it compares are the guard; for characters and fonts, D17's
  list and D18's exceptions.
- **Cost:** phase 2's inspection on every extraction, measured before D9 is decided.
- **Fixture content:** §3's note on the classifier.
- **Out of scope:** layout-based detection (D7); recognising what a glyph draws by rendering it
  (D18 (c), a later CR); standalone xlsx and pptx (D10); judging intent;
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
- docx4j's fonts: `FontTablePart`, `ObfuscatedFontPart`, `org.docx4j.fonts.fop.fonts.truetype.TTFFile`,
  `RunFontSelector`, `Mapper`; the OpenType specification's cmap, hmtx, glyf and GSUB tables.
- docx4j's anonymiser: `MetadataScrubber.scrubExternalTargets`, `Verify.externalTargets`.

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

**A third review, of e99f79600**, from the docx4j-43 session at Jason's request (Jason had the same
findings): three contract errors, two decisions to revisit, nine nits and an open item, each checked
against the code before it was taken.

1. **Comments in the markup view.** Confirmed: `Extractor.commentsIncluded` is `view == MARKUP ||
   comments`, and the suite reads case 28's comments under markup. The membership bullet and the
   `comment` row now say so.
2. **Properties in the block model.** Confirmed: `Metadata.read` runs for every top-level result
   (REQ-515). As written, every block-model result would have reported the properties as unseen. Taken
   as the review recommends: labelled metadata is info while in the output (§2.3, D14), and `visible`
   stays `no`.
3. **Hyperlinks.** Confirmed: the field form had a scheme rule and the relationship form none, so case
   21's https `w:hyperlink` would have been an unexpected active finding. One link rule for both forms
   (§2.3), and `hyperlink.web` (info) for case 21's two.
4. **D9's cost.** Taken: D9 now recommends deciding (a) at once, the measurement choosing only the
   default; and a part's or fragment's result carries no counts.
5. **Private-use characters.** Taken as D17: a documented list with the icon fonts, and a
   symbol-encoded face a symbol font. D17 was then corrected the same day (Jason asked how docx4j knows
   which symbol font to use). docx4j has Unicode tables for the five fonts it recognises
   (`SymbolMapper`), so `TextOptions`' "no glyph tables" is wrong. Only an embedded face is tested,
   so that a finding does not depend on the inspecting machine. A `w:sym` is judged by its font, like
   a run.

Eight of the nine nits are taken: `story.unused` leaves out the separator notes (`Extractor` skips
every note whose type is not `normal`); `text.tiny` names `w:szCs`; the bounds test builds its
document; a text range is within its run; a hidden paragraph is one finding; D4's `wp:align` and
orphan-header cases are in §3's table; ink is inventoried (`InkmlPart` is a known part, so not `part.unknown`); case 48 is
column- and paragraph-relative. One nit did not hold: §1's citation of `Verify` for external targets
is right (`Verify.externalTargets` reads every relationship whose TargetMode is External), and
`MetadataScrubber.scrubExternalTargets` is now cited beside it. (`Verify.java` holds a NUL byte in a
string literal, at line 291, so grep treats the file as binary and skips it.) The open item, the fetch
defect recorded only here, is answered: CR-046 records it, fixed as 0847b3df3.

**Embedded fonts**, Jason's question the same day: a malicious font whose glyphs are swapped makes a
document show one word in the viewers that use the embedded font and another in those that
substitute, while the extracted text is always the characters stored. Nothing in the draft saw it,
and no character test can, since the characters are ordinary letters. D18, the `font.*` kinds, the
fixture's font cases and the viewer measurement are the answer; a sentence for the WD's section 11 is
among phase 0's proposals. D18 was then revised the same day. As first written it said `indeterminate`
where no test decided, which D15 counts as unseen, so every embedded font would have been reported.
It also compared outlines with an installed namesake, which depends on the machine. It now finds a
mismatch on positive evidence only, adds a test of glyph names, and says what the static tests cannot
see (§2.6). The text model's output is Unicode as stored (Java strings, UTF-8 when
written), neither transcoded nor normalised, so every character the document holds reaches the
agent: private-use, zero-width, bidi and tag characters are reported by the `chars.*` kinds, and a
`w:sym` is its code point as written.
