# CR-046: Text extraction behaviour model - one text model for docx4j

Status: PROPOSED 2026-10-17, drafted at Jason's request ("please draft the text-extraction CR
(CR-046) against docs/text_extraction_specification_wd.md, so each phase's acceptance criteria
are REQ numbers"). D1 to D7 decided 2026-10-17 (Jason: "D1–D7: i accept the recommendations").
Phase 0 suite implemented 18 October 2026; the original 52-case Word checks passed.
Cases 53–54 are now added; Jason completed the extended main DOCX r3 open/save check.
Phase 1 implemented 18 October 2026 (§4).
Five phases (§4); decisions in §6.

The specification is the portfolio's *WordprocessingML Text Extraction*, working draft 0.6
(`docx4j-portfolio/docs/text_extraction_specification_wd.md`, commit 97fce95), cited here as
**WD**. It records the decisions D1 to D31 of the portfolio's discussion paper
(`docs/text_extraction_behaviour_model.md`, 2d8fd36), all of which Jason accepted on 17 October
2026. docx4j is its first implementation (D15: Java first, since it cuts the goldens the ports
are held to).

## 0. Summary

docx4j has several text readers, and they disagree:
- `TextUtils` returns the text of whatever it is given;
- `WmlToMarkdown` accepts changes by default, and under its MARKUP policy strikes deletions
  through with `~~`;
- the HTML and FO exporters show markup unless told to accept;
- CR-043's `Trees` knows the accepted and original views, but only of run-level content.

None of them has a scope, a warning, or a defined order for the stories.

This CR gives docx4j-core **one text model**, `org.docx4j.model.text`. A text is
`serialize(scope(view(resolve(package))))` (WD REQ-201):
- **resolve:** `mc:AlternateContent` resolved by the ISO rule on namespace URIs;
- **view:** one of three revision views, defined by Word's Accept All and Reject All (CR-043);
- **scope:** a default scope of stories and content classes;
- **serialize:** plain text, Markdown, HTML, or a block list that docx4j-mcp writes as JSON.

Every text reader docx4j ships is then moved onto it.

**Acceptance is by statement.** Each phase lists the WD statements it must satisfy (§4), and
§5 places every statement of the WD in a phase or outside this CR. A phase is done when each
listed statement is pinned by a test that cites it, and by the probe cases the WD's Appendix A
gives for it.

At the end of phase 3, docx4j claims three of WD §12.1's conformance classes: plain-text
extractor, Markdown extractor and block-model extractor. The inspector class is a later CR (D29).

## 1. What exists today (read 2026-10-17)

| Layer | What docx4j has | What the WD needs that it lacks |
|---|---|---|
| resolve | `McSelection` (CR-021): one rule for every reader. A Choice is taken when every `Requires` prefix is listed in `docx4j.jaxb.mc.preferChoice`, otherwise the Fallback. The property's default is empty, so the Fallback is the default everywhere. An element with Choices and no Fallback is kept whole, and its first Choice is taken. `mc-preprocessor.xslt` converts Strict and selects Fallbacks when JAXB fails to unmarshal a part. | Matching by namespace URI (REQ-303), not prefix. JAXB keeps the `Requires` string but not the declarations in scope at the element. An understood set for reading (REQ-302, REQ-306) that differs from what the FO exporter can draw. The `mce.branches_differ` warning (REQ-307). |
| view | `org.docx4j.model.revisions` (CR-043): `TrackedChanges.acceptAll`/`rejectAll` on a package, every story, held to Word's saves. `Trees.View` (`ACCEPTED`, `ORIGINAL`) and `Trees.appendText` give run-level text in a view. `acceptAllForConversion` keeps the first paragraph's `paraId` on a join. `Trees` is package-private. | Structural views in a walk: joins, rows, fields as units, notes whose reference is deleted (REQ-402, REQ-404). The `markup` view (REQ-403). The agreement test against the copy route (REQ-406). |
| scope | None as such. `TextUtils` walks the object it is given. `WmlToMarkdown` has a tracked-changes policy (`ACCEPT` by default, deletions dropped; `MARKUP` strikes them through with `~~`), emits footnotes as `[^n]` and charts through `ChartTable`, drops `instrText`, and takes alt text from `wp:docPr/@descr`. `HiddenText` (in `convert.out.common`) decides hidden runs and paragraphs for the exporters. `BindingTraverser` evaluates bindings, but only as a mutator. `FieldUpdater` computes DOCPROPERTY and DOCVARIABLE, also as a mutator. | Story order and the default scope (REQ-501 to REQ-504). Text boxes once, after the anchor (REQ-518). Placeholders, bound values, list labels, charts, SmartArt, WordArt, embedded packages, `altChunk`, image descriptions and metadata, each as REQ-505 to REQ-517 define. |
| serialize | `TextUtils` (plain). `WmlToMarkdown` (`docx4j-markdown`, commonmark; `~~` for deletions under its MARKUP policy). `OmmlToLatex` (in `docx4j-markdown`, though it needs only docx4j's math classes). The HTML and FO exporters show markup by default, and accept with `ConversionFeatures.PP_COMMON_ACCEPT_TRACKED_CHANGES`. | CriticMarkup (REQ-702, REQ-703). The bracket convention for added text (REQ-707). A block list with addresses (REQ-706, REQ-518). A warnings channel (REQ-801). Options under the WD's names (REQ-1001). |

`JaxbXmlPartAltChunkHost.convertAltChunks` converts XHTML and MHT chunks (through
docx4j-ImportXHTML, reached reflectively) and text chunks in place, as a mutator; nothing calls
it. No HTML or RTF chunk is expanded by anything. There is no `ImageDescriber`, no reader of
SmartArt text, and no address grammar. `TextUtils.extractText`'s output stays as it is (WD
REQ-202's note).

Phase 0 measures what each existing reader returns for the probe, so that each change to
today's output is recorded case by case rather than asserted.

## 2. Design

### 2.1 The API

`org.docx4j.model.text`, exported by docx4j-core's `module-info`:

- `TextView`: `ACCEPTED` (the default), `ORIGINAL`, `MARKUP` (REQ-401). `current` is accepted
  as a synonym by name.
- `TextOptions`: a typed builder, one setter per WD §10 option, plus `TextOptions.of(Map<String, ?>)`,
  which takes the WD's option names and values verbatim (REQ-1001). docx4j-mcp passes its
  parameters through that map. Unimplemented values are rejected with a message naming
  REQ-1001 (§2.6).
- `Texts.of(WordprocessingMLPackage | Part | Object, TextOptions)` returns a `TextResult`.
- `TextResult`:
  - `blocks()`: the block model, a list of `TextBlock`;
  - `warnings()`: `TextWarning` items, each `{kind, address, branch}`;
  - `metadata()`: the document properties and `fields_update_on_open`.
- `TextBlock`: `address`, `paraId`, `style`, `text` in the view, runs (in `MARKUP`, each run
  with `revision {type, author, date}`), the WD REQ-706 flags, and typed objects (field, image,
  object, chart, SmartArt).
- Serialisers:
  - `PlainText.of(TextResult)` (WD §7.1, §7.5);
  - Markdown through `docx4j-markdown`, from the same result (phase 3);
  - the block model as JSON, in docx4j-mcp (phase 4).

The block model is the one representation. Plain text, Markdown and the MCP's JSON are all
written from it, so the readers cannot drift apart (REQ-202).

Addresses use docx4j-core-ts CR-002 §3.3's grammar (REQ-518), which docx4j-python and the
editor also adopt. The text-box form `body/3/textbox:0/1` is still a proposal, and may change in
spelling when core-ts CR-010 phase 3 settles it.

### 2.2 Resolve: `McSelection` by purpose

`McSelection` gains a purpose:

| Purpose | Rule | Used by |
|---|---|---|
| `TEXT` | **New.** The ISO rule (REQ-302): the first Choice whose every `Requires` prefix resolves to a URI in the understood set, else the Fallback, else (an element with no understood Choice and no Fallback) the first Choice from the kept element (REQ-305). | The text model (phase 1); every other reader (phase 3). |
| `DRAW` | **Unchanged:** `docx4j.jaxb.mc.preferChoice`, empty by default, so the Fallback is drawn. It stays so until the FO exporter draws an inline `wps` text box (CR-021 §8.6). | The FO and HTML exporters' layout. |
| `ALL` | **Unchanged:** every branch (`McMode.ALL`). | Mutators: binding, field update, anonymisation. |

**The understood set** is a published resource, `org/docx4j/jaxb/understood-namespaces.txt`
(REQ-306):
- one URI per line;
- a README beside it stating its rule: "the namespaces whose content this engine's reader can
  reach";
- its first content is docx4j-core-ts's `UNDERSTOOD_NAMESPACES`, about 110 URIs;
- a test asserts docx4j's set against it, and each port asserts its own set against a copy.

Additions are made to the published list first.

**URIs, not prefixes (REQ-303).** At unmarshal, each `mc:Choice` records the URIs of its
`Requires` prefixes from the namespace context in force. The phase 1 spike settles the
mechanism: an `Unmarshaller.Listener` reading the `XMLStreamReader`'s `NamespaceContext`, or a
StAX pass over parts that contain `mc:AlternateContent`. Where no context is available (an
object built in code, the XSLT pathway), the conventional prefix table is used, and the javadoc
says so, as REQ-303 permits. Two further rules:
- an empty or whitespace-only `Requires` is not understood (REQ-304);
- a part rescued by `mc-preprocessor.xslt` has already been resolved to Fallbacks, and the
  warning names it.

**The branch comparison for REQ-307** runs at read time, since docx4j keeps both branches in
the tree. It compares the branches' texts after REQ-1201's normalisation.

### 2.3 View: the walk, with the copy as its oracle

**The walk is the implementation.** It is a view-aware traversal that does what CR-043's
`Resolver` does to a copy, without making the copy:
- a deleted paragraph mark joins in `ACCEPTED`, an inserted one in `ORIGINAL`;
- a deleted or inserted row is present or absent;
- a field is resolved as a unit;
- a note whose reference is deleted is absent from `ACCEPTED`;
- the joined block takes the first paragraph's `paraId` (REQ-405).

It reuses `Trees` (holders, `revisionOf`, `appendText`). `Trees` stays internal; a small public
class in `org.docx4j.model.revisions` exposes what `org.docx4j.model.text` needs (D2).

**The copy is the reference.** Resolve a `clone()` of the package with
`TrackedChanges.acceptAll` or `rejectAll`, then extract with no view logic. The agreement test
(REQ-406, REQ-1202) holds the two equal over CR-043's fixtures and the probe.

**`MARKUP`** returns every run as stored, with each run's revision (REQ-403), and resolves no
structure.

### 2.4 Scope

**Stories:**
- the main story first;
- each text box's paragraphs once, immediately after the anchoring paragraph, from the branch
  selected for `TEXT`, and after the table for a box anchored in a cell (REQ-518);
- then the footnotes and the endnotes, in reference order, `[n]` on each side (REQ-503);
- under `include`, each section's displayed headers and footers, in section order, with a
  header no section references skipped (REQ-502);
- under `include: glossary`, the glossary document, last, after the endnotes (REQ-501,
  REQ-516);
- comments: in `MARKUP`, at the end of their range (REQ-504).

**Content classes**, each where WD §5.2 puts it:

| Content | Where docx4j gets it | Statement |
|---|---|---|
| hidden text | `HiddenText`: run-level first, then style-resolved through `PropertyResolver` (D8) | REQ-507 |
| placeholders | `w:showingPlcHdr` | REQ-508 |
| bound values | a read-only evaluation of the binding's XPath against its custom XML part, `BindingTraverser`'s rule without its mutation | REQ-509 |
| list labels | the numbering emulator (`model.listnumbering`), off by default | REQ-510 |
| charts | `ChartTable`, axis titles once CR-047 adds them | REQ-511 |
| SmartArt | a new reader of the diagram data part, depth-first from the `doc` point along `parOf` by `@srcOrd`, over `node` and `asst` points | REQ-511 |
| legacy WordArt | its `v:textpath/@string`, as a one-paragraph text box | REQ-511 |
| embedded packages | under `embedded: true`, an `EmbeddedPackagePart` loaded and read at its reference: a WordprocessingML document through the model; a workbook through xlsx4j, each sheet's used range (from the cells holding values, not `dimension`) as a simple table (REQ-605), each cell as displayed (its cached value through xlsx4j's `DataFormatter`, as `ChartTable` uses it; formulas never recomputed), sheets in workbook order, each after its name on its own line; a presentation through pptx4j (`MainPresentationPart.getSlide`), each slide's text in slide order, hidden slides omitted, shapes in `p:spTree` order with groups depth-first, a table as REQ-605, speaker notes omitted; an OLE 1.0 package holding plain text read from its `Ole10Native` stream; no other OLE binary opened. One level deep across kinds: content reached through an embedded package or an `altChunk` has neither expanded | REQ-512 |
| `altChunk` | `convertAltChunks`' type dispatch, applied to a detached result, never the package: text as lines; XHTML and MHT through docx4j-ImportXHTML when it is on the classpath (reflectively, as `convertAltChunks` reaches it); HTML tidied by jsoup when it is on the classpath and read as blocks directly, its tags stripped otherwise (D5); a docx or Flat OPC chunk as a nested package, one level deep across kinds as for embedded packages; RTF never; `altchunk.not_expanded` for whatever is not expanded | REQ-513 |
| image descriptions | a new `ImageDescriber` interface, `AltTextDescriber` the default: `@descr`, else `@name`, else VML `@alt`, and nothing for `adec:decorative`; provenance carried | REQ-514 |
| properties | the `metadata` object always; in the text only under `metadata: true` | REQ-515 |
| glossary | `GlossaryDocumentPart`, only under `include: glossary` | REQ-516 |
| custom XML parts, settings | never text (bound values aside, REQ-509) | REQ-516 |

### 2.5 Characters and serialisation

**Characters.** `Trees.itemText` already maps tab, break, non-breaking hyphen, soft hyphen and
`w:sym` the way REQ-601 does. The text model adds:
- `w:cr` and `w:ptab`;
- no folding or trimming of any kind (REQ-602);
- one line per paragraph and per break (REQ-603).

**Math (REQ-604)** is LaTeX in `$...$`. `OmmlToLatex` moves from `docx4j-markdown` to
docx4j-core (D3), so that plain text can use it; it needs only docx4j's math classes.
Ruby gives its base text.

**Tables (REQ-605):**
- a simple table is tab-separated;
- any other table is in the listed format, after the note line `[table: listed format]`;
- `tables: grid` writes pandoc grid tables.

**Plain text** (REQ-701, REQ-702, REQ-707):
- the `markup` view uses CriticMarkup: `{++ ++}`, `{-- --}`, `{>> <<}`;
- `markup_detail` adds the author, or the author and date, inside the marker;
- whatever the extractor adds uses the WD's bracket convention: `[n]`, `[image: ...]`,
  `[page n]`, `[table: listed format]`.

**Markdown** (REQ-703, REQ-704) is `WmlToMarkdown` rewritten on the block model (phase 3):
- CriticMarkup replaces `~~`;
- a HYPERLINK field's target becomes the link target whatever `field_codes` says;
- images take the describer's text;
- `metadata: true` gives YAML front matter.

**HTML** (REQ-705) marks `<ins>` and `<del>` with `data-author` and `datetime`.

The HTML and FO exporters take the same `view` parameter. Their default stays `markup` (D11):
the WD makes no statement on rendering, and a rendering that hides markup would mislead.

### 2.6 Options this CR does not implement

These are documented, as REQ-1001 allows, and rejected with a message naming the statement:
- `math: unicodemath`: no OMML-to-UnicodeMath converter exists;
- `symbols: unicode`: docx4j has no glyph tables for Symbol and Wingdings yet.

**`fields: update` (REQ-506)** computes on a copy (REQ-204):
- DOCPROPERTY and DOCVARIABLE through `FieldUpdater`;
- REF, SEQ and QUOTE where docx4j already computes them;
- any other no-layout, no-fetch field the WD lists that docx4j cannot compute is documented by
  name.

### 2.7 `TextUtils`

`TextUtils.extractText` and `getText` are deprecated in phase 3, with their output unchanged.
The javadoc says they are not the text model and points to `Texts`. No shipped reader uses them
after phase 3 (REQ-202's terms for a compatibility getter).

### 2.8 What changes for existing users

| Reader | Change |
|---|---|
| `WmlToMarkdown` | Its default is already the accepted view (policy `ACCEPT`). Its MARKUP policy changes from `~~` to CriticMarkup (D6). It gains the `original` view. Hidden text is excluded. Text boxes appear once, after their anchor. |
| HTML export | Gains `<ins>`/`<del>` attributes and the `view` parameter; its default is unchanged. |
| FO export | Gains the `view` parameter; its default is unchanged. |
| `TextUtils` | Deprecated; output unchanged. |
| `TraversalUtil` and other `READ` walkers | Take the `TEXT` purpose in phase 3. A document whose Fallback and Choice differ is then read from the Choice where docx4j understands it. That changes numbering emulation and any counting walk, so phase 3 runs the layout-fidelity gate before committing. |

## 3. Tests and the oracle

- **The probe** (`docx4j-core-tests/src/test/resources/text-extraction/phase0/`, c0c72ecef) is
  the conformance suite of WD §12.2. `expected-tokens.tsv` gives each marker token's count per view and under
  its gating option.
- **`ExtractionConformanceTest`** reads the table, extracts with `Texts`, applies REQ-1201's
  normalisation, and asserts each row. Rows whose statements belong to a later phase are listed
  as pending and reported, not skipped silently.
- **The agreement test** (REQ-406, REQ-1202) compares the walk with the copy route on every
  CR-043 fixture and on the probe, in both views.
- **The goldens** (REQ-1203): for each fixture, `.accepted.txt`, `.original.txt`, `.markup.txt`
  and `.md`. They are cut by the implementation in phase 3 and reviewed by hand against WD §3 to
  §7 before they are committed. The ports compare against them verbatim.
- **Word's evidence** (the paper's §8, WD Appendix B) documents where the WD departs from Word.
  It is not an oracle for this CR's defaults.

## 4. Phases

### Phase 0: the conformance suite

**Bring the probe to the WD.**
- Revise `expected-tokens.tsv` (ExtractionProbeBuilder writes it, and the README):
  - ALT36_CHUNK_TEXT becomes 1/1/1, gated by `altchunks: false`;
  - WA25_TEXTPATH is gated by `text_boxes: false`, not `smartart: false`;
  - CHT34_AXIS_TITLE is expected once CR-047 lands;
  - MTH32 stays under the LaTeX default (REQ-1201's normalisation now drops `\ { } $`).
- The README gains a statements column per case, matching WD Appendix A.

**Add the cases of WD Appendix A.2**, with the conventions of 4a89a5967: one token per construct,
a labelled paragraph per case, the builder self-checking, and a Word-open check by Jason.
- Cases 39 to 49 go in `extraction-probe.docx`:
  - embedded xlsx, docx (nested) and an OLE 1.0 package;
  - an ActiveX control;
  - plain-text, MHT, RTF, Flat OPC and docx `altChunk`s;
  - the inspection kinds of case 48;
  - the fetching fields of case 49.
- Cases 50 to 52 go in a second file, `extraction-probe-external.docm`:
  - `w:updateFields`;
  - external template, mail-merge, linked-picture and OLE-link targets;
  - a `vbaProject`;
  - custom and application properties.
- If building a valid `Ole10Native` (case 41) takes more than an hour, it is dropped and the
  drop recorded, as the portfolio session allowed.

**The harness and the baseline.**
- `ExtractionConformanceTest`, as §3 describes.
- A recorded baseline: what `TextUtils`, `WmlToMarkdown` and the HTML exporter return for each
  case today, as the record of every output change §2.8 makes.

**Acceptance:**
- REQ-1201: the suite is complete. Every row of the table names the statements it tests, and
  the README's per-case statements equal WD Appendix A.
- REQ-1203: the golden file format is defined and the harness reads it.
- The Word-open check of both probe files passes.

The commit is the one WD's [PROBE] cites.

**Phase 0 implementation, 18 October 2026 (both Word-open checks passed):**
- The suite (initially 52 cases, now extended to 54) is in
  `docx4j-core-tests/src/test/resources/text-extraction/phase0/`; the original checked
  38-case probe and its Word re-save remain unchanged in the parent directory.
- The builder writes both documents, token/finding tables, README and case-to-REQ mappings.
  Case 41 is implemented with Ole10Native; the optional drop was not needed. Case 42 uses
  Word-authored Label control storage (the initial property-bag attempt was removed by Word). Case 51 has a rebuilt VBA project with a comment marker,
  attribute-only modules and no executable procedures.
- `ExtractionConformanceTest` validates the packages and statement mappings, exercises
  normalization, revision marking and the golden reader, and reports every unsupported
  extraction/inspection assertion as pending. Later phases connect `Texts` through its
  test adapter. REQ-902 remains outside this CR; CR-047 is an explicit axis-title dependency.
- Raw TextUtils, Markdown and HTML baselines and per-case token counts are recorded in
  `phase0/baseline/`. The baseline was cut from fresh compilations of this checkout's core
  and Markdown source, with released 17.3.3 generated-object/JAXB dependencies.
- The golden manifest is header-only until phase 3's reviewed goldens exist. The format,
  adapter contract, capture commands and two specification questions are in
  `text-extraction/GOLDENS.md`; WD 0.4 answered both. Case 38's core properties now also have metadata-option rows.
- Jason confirmed the r2 main DOCX opened without repair on 18 October 2026, with field
  updates declined. Jason also completed the external DOCM check after declining its blank
  mail-merge SQL prompt; no repair problem was reported. Both open checks pass. The first extended DOCX triggered unreadable-content repair; its repaired save is retained
  under `phase0/evidence/`. An r2 retry changes case 42's control storage, the Flat OPC
  identification and the DOCX altChunk MIME type; r2 opens without repair, but Word's save of
  it drops case 42's control again. Neither the old Word re-save nor the legacy reader baseline is a
  conformance oracle for the extended suite. The checked suite remains under `phase0/`
  alongside the preserved original suite in the parent directory.
- Word's saves of both r2 files are retained under `phase0/evidence/` (18 October 2026), with
  what each lost in `word-open-results.md`:
  - the main DOCX save drops case 42's control (its `w:control` and ActiveX parts), keeping only
    the preview picture;
  - the DOCM save drops the `vbaProject` (case 51), the attached template, the mail-merge data
    source and `w:updateFields`.
  The causes are not established. The full Word build is still not recorded: both saves give
  only `AppVersion` 16.0000.

### Phase 1: the text model in plain text

**Build:**
- `org.docx4j.model.text` (§2.1);
- the `TEXT` purpose and the published understood set (§2.2);
- the view walk with the agreement test (§2.3);
- the stories and the content classes that need no new reader (§2.4);
- characters, plain text with CriticMarkup, the bracket convention, the warnings channel and
  the block list with addresses;
- `TextOptions.of(Map)` accepting every WD option name.

**Acceptance:**
- REQ-201, REQ-202 (for `Texts`), REQ-203, REQ-204;
- REQ-301 to REQ-307;
- REQ-401 to REQ-406;
- REQ-501 to REQ-505, REQ-507, REQ-508, REQ-510, REQ-517, REQ-518;
- REQ-601 to REQ-603, REQ-606, with simple tables tab-separated;
- REQ-701, REQ-702, REQ-706 (the block's fields, in Java), REQ-707 (`[n]`);
- REQ-801 (`mce.branches_differ`, `field.no_result`), REQ-802;
- REQ-1001 (the names accepted, the unimplemented values rejected as §2.6 says);
- REQ-1202;
- REQ-1201 for every row whose statements are in this list: cases 1 to 22 (case 21's
  plain-text rows; its Markdown row is phase 3's), 24 and 26 to 30, and the parts of 25 that
  are text boxes.

**Phase 1 implementation, 18 October 2026:**
- `org.docx4j.model.text`, exported: `TextView`, `TextOptions` (every WD option, `of(Map)`, `set`),
  `Texts` (`of` a package, a story part, content in a package, content alone; `ofResolved` for the copy
  route), `TextResult`, `TextBlock`/`TextRow`/`TextCell`, `TextRun`, `TextField`, `TextComment`,
  `TextRevision`, `TextWarning`, and `PlainText`. The walk is `Extractor`, package-private.
- Resolve: `McPurpose` (`TEXT`, `DRAW`, `ALL`) and `McSelection.selectedBranch(McPurpose, AlternateContent)`
  (purpose first: `selectedBranch(ac, null)` would otherwise be ambiguous for existing callers),
  `select`, `branches`, `understands`, `requiresNamespaces`, `understoodNamespaces`.
  - The spike settled on the existing `Docx4jUnmarshallerListener`: `JaxbXmlPart` hands it the
    `XMLStreamReader` it unmarshals from, and at each `mc:Choice`'s end tag (checked, so a runtime that
    calls back elsewhere records nothing) the reader's namespace context resolves the `Requires`
    prefixes. The URIs are kept by identity in a weak map. Measured on the reference implementation
    and on MOXy. A Choice built in code, deep-copied, or unmarshalled from a DOM (the binder, the
    `mc-preprocessor.xslt` rescue) falls back to the conventional prefix table, as REQ-303 permits;
    the javadoc says so.
  - `org/docx4j/jaxb/understood-namespaces.txt` and its README: core-ts's 107 URIs, then 11 that
    docx4j's JAXB contexts bind and core-ts's list lacks (`dgm1611`, `w16sdtdh`, `w16du`, `w16sdtfl`,
    `x14ac`, `xr2`, `xr3`, `xr6`, `xr9`, `xr10`, `xr16`). Left out on purpose: the six OpenDoPE
    namespaces (custom XML only, REQ-516), content types, the XML namespace.
    `UnderstoodNamespacesTest` asserts the format, the set equal to the file, and every namespace the
    three contexts bind in it but those eight.
- View (D2): `org.docx4j.model.revisions.ResolvedView`, public, answers `removes(item)`,
  `removesMark(p)` and `removesComment(id)` from CR-043's own `Lister`, recording what `Resolver`
  would do without doing it. Moves are replayed per story in `resolveAll`'s order (last half first),
  since the outcome depends on it: a source in no range after an orphaned destination turns the
  destination into a plain insertion (`check20/moveFrom-no-ranges.docx`). A comment whose range
  started on a revision taken away goes with it once its range is empty, as `removeRevision` does.
- `ViewAgreementTest` (REQ-406, REQ-1202): 136 files (CR-043's `revisions/` and the probe's
  `text-extraction/`), both views, the default scope and every story included: 544 comparisons, all
  equal. The copy is the fixture loaded again, not `clone()`: docx4j's save drops the declaration of
  a `Requires` prefix bound to an unconventional namespace (probe case 6, `shape2010`), so a saved copy
  takes the Fallback. That save defect is CR-021's and is not fixed here.
- The suite: `TextsAdapter` is the harness's default engine. 420 assertions pass, none fail, 513 are
  pending (phases 2 and 3, the inspector). Two harness fixes: an occurrence of a marker inside a
  longer marker of the suite no longer counts (`FLD19_RESULT` inside `FLD19_RESULT_TWO`,
  `MCE8_OUTER` inside `MCE8_OUTER_FALLBACK`), and a scenario's option needs its own statement
  (`embedded: true` ran on case 51's row, which cites only REQ-902). Case 25's text-box rows are
  phase 1 (section 4); its WordArt row stays phase 2. No fixture changed.
- Unit tests: `MarkupCompatibilityTest` (REQ-301 to REQ-305, REQ-307), `TextsTest` (REQ-203, REQ-204,
  REQ-401, REQ-402, REQ-405, REQ-501 to REQ-505, REQ-507, REQ-508, REQ-510, REQ-518, REQ-601 to
  REQ-603, REQ-702, REQ-706, REQ-707, REQ-801, REQ-802, REQ-1001), on the reference implementation and
  on MOXy.

Choices the WD leaves open, made here and to be confirmed (portfolio):
- `field_codes: true` writes the instruction as written, then the result, with no braces (braces
  would be extractor-added text, which REQ-707 does not list).
- A comment is rendered at its `w:commentReference` (Word writes it right after the range end), its
  text plain inside `{>>...<<}`; a reply after its parent, never at its own reference.
- A section's headers are written first-page, default, even; each part once, at the first section
  that displays it.
- Footnotes and endnotes are numbered apart, in Arabic numerals (Word shows endnotes as i, ii, ... by
  default). A custom-mark reference has no number; its note is still read, in reference order.
- Until phase 2's listed and grid forms, every table is written tab-separated, a multi-paragraph
  cell's paragraphs a space apart.
- In `markup`, a `w:delText` outside any holder is a deletion with no author or date.
- A block's `text` holds no list label; a table block's text is its tab-separated form.

### Phase 2: the remaining content classes and options

**Build:**
- `fields: update` on a copy, and `fields_update_on_open`;
- bound values;
- charts, SmartArt and WordArt;
- embedded packages: documents through the model, workbooks through xlsx4j, presentations
  through pptx4j;
- the glossary under `include: glossary`;
- `altChunk` expansion;
- `ImageDescriber`;
- metadata;
- math (`OmmlToLatex` moved, D3) and ruby;
- the listed and grid table formats;
- page markers and `layout: indent`.

**Acceptance:**
- REQ-506, REQ-509, REQ-511 (case 34's axis title once CR-047 has landed), REQ-512, REQ-513,
  REQ-514, REQ-515, REQ-516;
- REQ-604, REQ-605;
- REQ-707 (the remaining added text), REQ-708, REQ-709;
- REQ-801 (`sdt.binding_differs`, `altchunk.not_expanded`, `fields_update_on_open`);
- REQ-1001 (every option implemented but the two of §2.6);
- REQ-1201 for every row of both probe files.

`layout: indent` waits on a case of hand-indented clauses that the portfolio has yet to
request; REQ-709 is pinned by a unit test until then.

### Phase 3: every reader on the model

**Build:**
- `WmlToMarkdown` on the block model;
- the HTML exporter's `<ins>`/`<del>` and `view` parameter;
- the FO exporter's `view` parameter (D11);
- `TraversalUtil`'s `READ` and the other reading walkers on the `TEXT` purpose (the
  layout-fidelity gate run first, §2.8);
- `TextUtils` deprecated;
- the goldens cut and reviewed.

**Acceptance:**
- REQ-202 (complete: every reader docx4j ships as a text reader goes through the model);
- REQ-703, REQ-704, REQ-705;
- REQ-1203.

With that, docx4j conforms as a plain-text, Markdown and block-model extractor (WD §12.1).
CHANGELOG and Getting Started record the output changes of §2.8.

### Phase 4: hand-offs

The goldens' commit goes to:
- **docx4j-core-ts**, for its CR-011: structural `getText` and `outline`, text boxes, hidden
  text, notes, the agreement test;
- **docx4j-python**, for its CR-011: one text model in place of five;
- **docx4j-generated-objects-ts**: `textOf` follows the rules, or core-ts stops using it;
- **docx4j-mcp**: the engine bump to 17.3.x (D14), then `view`, `include` and the booleans on
  `extract_text`, and the block model's JSON (REQ-706's verbs are the MCP's);
- **the editor**: the same parameters on its verbs.

The portfolio registry is updated through the portfolio session.

**Acceptance:** no statements. The hand-offs are sent with the commit hashes, and each
receiving session has acknowledged.

## 5. Every statement of WD 0.6, and where it is met

| Statement | Subject | Phase | Probe cases (WD Appendix A) |
|---|---|---|---|
| REQ-201 | the four layers, in order | 1 | all |
| REQ-202 | every shipped text reader through the layers | 1 (`Texts`), 3 (the rest; `TextUtils` deprecated) | - |
| REQ-203 | Strict converted before the layers | 1 | core-tests `strict/` fixtures |
| REQ-204 | the package unchanged; the copy discarded | 1 | all |
| REQ-301 | one branch | 1 | 1, 8 |
| REQ-302 | the first understood Choice, else the Fallback | 1 | 1 to 5, 7 |
| REQ-303 | `Requires` matched by URI | 1 | 6 |
| REQ-304 | an empty `Requires` not understood | 1 | unit test |
| REQ-305 | the first Choice as the last resort; the element kept whole at load | 1 (CR-021 already does this) | unit test |
| REQ-306 | the published understood set | 1 | the list test |
| REQ-307 | `mce.branches_differ` | 1 | 1, 4, 6, 8 |
| REQ-401 to REQ-403 | the three views | 1 | 9 to 17 |
| REQ-404 | the rules by construct | 1 | 10, 13 to 17 |
| REQ-405 | the joined paragraph's `paraId` | 1 | 11 |
| REQ-406 | the agreement test | 1 | CR-043 fixtures, all |
| REQ-501 | the default stories | 1 (default), 2 (glossary option) | 1 to 8, 17, 25, 27, 54 |
| REQ-502 | headers and footers per section | 1 | 25, 26 |
| REQ-503 | notes and `[n]` | 1 | 17, 27 |
| REQ-504 | comments | 1 | 28 |
| REQ-505 | field codes, cached results, `field.no_result` | 1 | 19, 20 |
| REQ-506 | `fields: update`; `fields_update_on_open` | 2 | 49, 50 |
| REQ-507 | hidden text | 1 | 18, 48 |
| REQ-508 | placeholders | 1 | 22 |
| REQ-509 | bound values | 2 | 23 |
| REQ-510 | list labels | 1 | 29 |
| REQ-511 | charts, SmartArt, WordArt | 2 (case 34's axis title after CR-047) | 25, 34, 35 |
| REQ-512 | embedded objects; a document, workbook or presentation read under `embedded: true`; one level across kinds | 2 | 39 to 42, 53 |
| REQ-513 | `altChunk`; one level across kinds | 2 | 36, 43 to 47 |
| REQ-514 | image descriptions | 2 | 33 |
| REQ-515 | properties and metadata | 2 | 38, 52 |
| REQ-516 | custom XML and settings: no text; the glossary only under `include`, last | 2 | 38, 54 |
| REQ-517 | transparent containers | 1 | 24 |
| REQ-518 | text boxes after the anchor; the address grammar | 1 | 1, 17, 25, 48 |
| REQ-601 | run content | 1 (`symbols: unicode` not implemented) | 29, 30 |
| REQ-602 | no normalisation | 1 | 30, 48 |
| REQ-603 | one line per paragraph and break | 1 | 30 |
| REQ-604 | math as LaTeX; ruby's base | 2 (`math: unicodemath` not implemented) | 32, 37 |
| REQ-605 | tables | 1 (simple), 2 (listed, grid, embedded presentation) | 13, 24, 31, 53 |
| REQ-606 | story and document order | 1 | all |
| REQ-701, REQ-702 | plain text; CriticMarkup | 1 | 9 to 17, 28 |
| REQ-703, REQ-704 | Markdown; front matter | 3 | 21, 38, 52, all |
| REQ-705 | HTML `<ins>`/`<del>` | 3 | 9 to 17 |
| REQ-706 | the block model | 1 (the fields, in Java); the JSON in docx4j-mcp | all |
| REQ-707 | added text | 1 (`[n]`), 2 (the rest) | 27, 31, 33 |
| REQ-708 | page markers | 2 | `extraction-probe.word-resave.docx` |
| REQ-709 | `layout: indent` | 2 | a unit test, until the case is requested |
| REQ-801, REQ-802 | warnings and metadata | 1, 2 | 1, 19, 23, 36, 43 to 47, 50 |
| REQ-901 to REQ-904 | inspection | not in this CR: the inspect CR (D29), on the anonymiser's walker, after phase 1 | 18, 42, 48 to 51 |
| REQ-1001 | option names | 1 (names), 2 (values) | - |
| REQ-1201 | the token suite | 0 (defined), 1 (its rows), 2 (every row) | all |
| REQ-1202 | the agreement test | 1 | CR-043 fixtures, all |
| REQ-1203 | the goldens | 0 (format), 3 (cut and reviewed) | all |

## 6. Decisions

**Decided 2026-10-17: Jason accepted every recommendation below** ("D1–D7: i accept the
recommendations"). Each item is kept as proposed, recommendation included, as the record.

- **D1. The phases.** This follows the paper's §10 row, with one change: the `TEXT` purpose and
  the understood set come with phase 1, not phase 3. The text model cannot pass cases 1 to 8,
  or REQ-302, while selection is the drawing default (the Fallback). Moving the other readers
  (`TraversalUtil`, numbering) stays in phase 3, behind the layout-fidelity gate.
  Recommended: as written. **Accepted.**
- **D2. Where the view walk lives.** Options: a small public class in
  `org.docx4j.model.revisions` over `Trees`, or `Trees` made public. Recommended: the small
  class; `Trees` stays internal. **Accepted.**
- **D3. `OmmlToLatex` moves** from `docx4j-markdown` to docx4j-core, so that plain text can
  write LaTeX (REQ-604). It needs only docx4j's math classes. Recommended: yes, with a
  forwarding class left in `docx4j-markdown` for one release. **Accepted.**
- **D4. Unimplemented options.** `math: unicodemath` and `symbols: unicode` are documented
  under REQ-1001. Recommended: yes; they are later work. **Accepted.**
- **D5. HTML `altChunk`s.** REQ-513 tidies HTML "where an HTML parser is present". Your comment
  on the paper (17 October 2026) named jsoup (MIT). docx4j-ImportXHTML has no tidier, and parses
  strictly through openhtmltopdf's XML loader. Recommended: jsoup as an **optional** dependency
  of docx4j-core (`<optional>true</optional>`, `requires static` in `module-info`). Where it is
  present, an HTML chunk is tidied and read as blocks by the text model directly; text and
  Markdown don't need ImportXHTML. Its tags are stripped only where jsoup is absent. For
  rendering, ImportXHTML's `convert(Node)` (fixed 17 October 2026, uncommitted) is the hook a
  tidied DOM goes through. The alternative is no new dependency, and tags always stripped. **Accepted: jsoup, optional.**
- **D6. Markdown's markup changes from `~~` to CriticMarkup**, and `MarkdownExportOptions`'
  tracked-changes policy maps onto the view (`ACCEPT` is `accepted`, `MARKUP` is `markup`, and
  `original` is new). The default is already `accepted`. Recommended: yes, as REQ-703 requires,
  with the policy kept as a deprecated alias of the view and a CHANGELOG entry. HTML and FO keep
  their default (D11). **Accepted.**
- **D7. The JSON form of the block model** lives in docx4j-mcp, not docx4j-core.
  Recommended: yes; docx4j-core carries the Java blocks. **Accepted.**

### 6.1 Questions for the WD (18 October 2026)

Phase 0 left four points open in WD 0.4. The portfolio session put them in WD 0.5 (0069d6a):

- **Q1. Where the glossary goes.** Ruled: last, after the endnotes (REQ-501).
- **Q2. An embedded workbook's cells.** Ruled (REQ-512):
  - each cell as the spreadsheet application displays it, its cached value through its number
    format, with formulas never recomputed. docx4j uses xlsx4j's `DataFormatter` with
    `Locale.US`, as `ChartTable` does;
  - the used range taken from the cells that hold values, not the `dimension` element.
- **Q3. An embedded presentation's reading order.** Ruled (REQ-512): shapes in `p:spTree` order,
  group shapes depth-first, a table in REQ-605's form, speaker notes and hidden slides
  (`show="0"`) omitted.
- **Q4. Cases for an embedded presentation (53) and a glossary (54).** Accepted by Jason
  on 18 October 2026 ("can we add case 53 ... and case 54 ..."). Both are now in the main
  DOCX, with regenerated token/statement tables and legacy baselines. Case 53 includes
  visible slides, nested groups, a 2x2 table, a hidden slide and speaker notes; case 54 has
  two glossary entries. `expected-order.tsv` pins their reading order. WD 0.6 (97fce95) records
  them in Appendix A.2.
  Jason completed the r3 Word-open/save check. Its save preserves the embedded PPTX byte-for-byte
  (part renamed), but drops the glossary part and relationship. The cause of the glossary loss
  is not established; the authored input remains the conformance fixture. The external DOCM
  is unchanged. The returned save and observations are retained in `phase0/evidence/`.


## 7. Dependencies, risks and out of scope

**Dependencies:**
- CR-047, the chart kind, axis titles and series axes, for case 34's axis title in phase 2.
- The address grammar's text-box form (core-ts CR-010 phase 3) may rename the form; at most a
  spelling change.

**Risks:**
- **JAXB drops namespace declarations.** URI matching needs them recorded at unmarshal; the
  phase 1 spike decides how. Parts rescued by `mc-preprocessor.xslt` are already resolved to
  Fallbacks before any reader sees them, and the warning names them.
- **The walk and the copy can disagree.** The agreement test is the guard. The copy is the
  reference, so where they differ the walk is wrong.
- **Phase 3's `TEXT` purpose changes what counting walks see** (numbering emulation, layout
  measurement), so it runs the layout-fidelity gate before it lands.
- **ImportXHTML is LGPL.** docx4j-core reaches it only reflectively, as `BindingHandler` does.

**Out of scope:**
- the inspect operation (WD §9; a later CR, D29);
- the JSON verbs `outline`, `find` and `read` (docx4j-mcp);
- SpreadsheetML and PresentationML as documents in their own right (an embedded workbook or
  presentation is read, REQ-512);
- rendering;
- evaluating fields that need layout or fetching.

## 8. Sources

- WD 0.6: `docx4j-portfolio/docs/text_extraction_specification_wd.md`, 97fce95.
  - 0.2 (2566012) answered this CR's first questions.
  - 0.3 (e8f14e1) added REQ-307's citation of REQ-1201's normalisation.
  - 0.4 (2062e86; [PROBE] corrected in c52b031) answered phase 0's questions: embedded
    workbooks and presentations, nesting across kinds, `include` against custom XML.
  - 0.5 (0069d6a) ruled on §6.1's Q1 to Q3.
  - 0.6 recorded cases 53 and 54 in Appendix A.2 and [PROBE].
- The paper: `docx4j-portfolio/docs/text_extraction_behaviour_model.md`, decisions §9 (2d8fd36),
  where it lands §10.
- CR-043 (the views and their fixtures); CR-021 §8.5 (Word and the Fallback; the `altChunk`
  rule, 17 October 2026).
- The probe: `docx4j-core-tests/src/test/resources/text-extraction/`, 4a89a5967, and its
  README's Word section; the phase 0 suite in its `phase0/` directory, c0c72ecef.
- docx4j-core-ts CR-002 §3.3 (addresses) and its `UNDERSTOOD_NAMESPACES`.
