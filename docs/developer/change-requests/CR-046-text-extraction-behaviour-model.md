# CR-046: Text extraction behaviour model - one text model for docx4j

Status: DONE 2026-10-18, all five phases, for 17.3.4 - proposed 2026-10-17, drafted at Jason's request ("please draft the text-extraction CR
(CR-046) against docs/text_extraction_specification_wd.md, so each phase's acceptance criteria
are REQ numbers"). D1 to D7 decided 2026-10-17 (Jason: "D1–D7: i accept the recommendations").
Phase 0 suite implemented 18 October 2026; the original 52-case Word checks passed.
Cases 53–54 are now added; Jason completed the extended main DOCX r3 open/save check.
Phase 1 implemented 18 October 2026 (§4). Phase 2 implemented 18 October 2026 (§4), with CR-047; its listed-format rules for
spanning headers ruled the same day (§4), and proposed to the WD. Phase 3 implemented 18 October 2026 (§4);
D12 decided the same day (§6). Phase 4 done 18 October 2026: the hand-offs sent and all five acknowledged (§4).
Five phases (§4); decisions in §6.

The specification is the portfolio's *WordprocessingML Text Extraction*, working draft 0.8
(`docx4j-portfolio/docs/text_extraction_specification_wd.md`, commit 2c00660), cited here as
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
spelling when core-ts's text-model CR settles it (core-ts CR-010's D8, 18 October 2026, gives that CR
the text-box view and its addresses; CR-010 phase 3 is done).

### 2.2 Resolve: `McSelection` by purpose

`McSelection` gains a purpose:

| Purpose | Rule | Used by |
|---|---|---|
| `TEXT` | **New.** The ISO rule (REQ-302): the first Choice whose every `Requires` prefix resolves to a URI in the understood set, else the Fallback, else (an element with no understood Choice and no Fallback) the first Choice from the kept element (REQ-305). | The text model (phase 1); every other reader (phase 3: `McMode.READ`, `TraversalUtil`'s default). |
| `DRAW` | **Unchanged:** `docx4j.jaxb.mc.preferChoice`, empty by default, so the Fallback is drawn. It stays so until the FO exporter draws an inline `wps` text box (CR-021 §8.6). | The FO and HTML exporters' layout, and the walks that serve it (`McMode.DRAW`, phase 3). |
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
| charts | `ChartTable`, with CR-047's `ChartDescription` (the kind, axis titles, secondary series) | REQ-511 |
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

Review (Claude Fable 5.1, docx4j-44, 18 October 2026, of f71cdd299). It confirmed the tests,
the 420/513 split, the listener's end-tag position on the JDK's StAX reader (a prefix declared on the
`mc:Choice` itself included), the join rule against `Resolver.joinWithNext`, and the reflective walk
on the module path. Its findings, and what was done:
- REQ-204 was broken for a package with no styles part: asking for a `PropertyResolver` added a default
  styles part and its relationship. The walk now asks only when the package has a styles part
  (`TextsTest.noStylesPartAdded`).
- The understood set is now read relative to `McSelection`'s class. `org.docx4j.jaxb` is opened
  unconditionally in `module-info`, so the class loader's lookup worked too, but the relative form
  does not depend on that.
- Each `Texts.of` reads the package's tracked changes once, whatever it is given. That is now
  documented in `Texts`. A reader that keeps the view across calls is left until docx4j-mcp needs one
  (phase 4).
- Residual risk, recorded: the listener's end-tag position was checked on MOXy by hand only
  (core-tests runs the reference implementation). Its guard turns a MOXy change into the prefix table,
  silently, which is the right default but would hide a regression.

The choices phase 1 made where WD 0.6 was silent are statements in WD 0.7 (portfolio 14e43a6,
corrected in b871d1f):
- REQ-505: under `field_codes: true`, the instruction as it stands, then the result, with no braces
  or other added characters.
- REQ-504: a comment at its `w:commentReference`, its text plain inside `{>>...<<}`; a reply after its
  parent, never at its own reference.
- REQ-502: a section's headers first-page, default, even; each part once, at the first section that
  displays it.
- REQ-503: footnotes and endnotes numbered apart, both in Arabic numerals; a custom-mark reference
  unnumbered, its note still read in reference order.
- REQ-404's table: a `w:delText` outside any holder is, in `markup`, a deletion with no author.
- REQ-306: the published list's path and composition (core-ts's 107, then docx4j's 11; OpenDoPE,
  content types and the XML namespace left out on purpose).
- REQ-1201: the harness's two rules. No occurrence inside a longer token of the suite counts. A check
  made under an option belongs to that option's statement as well as the row's, and is pending
  until the option is implemented.
- A NOTE under REQ-303: the save defect with a rebound `Requires` prefix is CR-021's, and is why
  REQ-406's copy is a reload, not a clone.

Not WD matters, and unchanged: until phase 2's listed and grid forms, every table is written
tab-separated, a multi-paragraph cell's paragraphs a space apart; a block's `text` holds no list
label, and a table block's text is its tab-separated form.

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

**Phase 2 implementation, 18 October 2026:**
- The suite: 876 assertions pass, none fail, 57 are pending (phase 1: 420 and 513). What is pending:
  the inspector's rows (REQ-902), Markdown's (phase 3), and case 44's "xhtml importer present" rows
  (the class path; below). Case 31's header passes under the listed format's ruled rules (below). Case 34's axis title passes with
  CR-047 (implemented the same day; `TextsAdapter` claims it).
  The harness's two "xhtml importer" scenarios are now capabilities of the class path, which the
  adapter advertises (`ExtractionConformance.XHTML_IMPORTER_PRESENT`, `_ABSENT`). `ViewAgreementTest`
  stays 544/544. `TextsPhase2Test` pins what the token rows don't (18 tests). No fixture changed.
- Content classes (§2.4), all read into blocks apart from the package (REQ-204):
  - bound values (`BoundValues`): a plain-text control's (`w:text`) binding, by its own XPath
    evaluation over the custom XML part's DOM (or the core or application properties, by their store
    item ids), since `BindingHandler.xpathGetString` gives "" both for a node that is empty and for
    none. It resolves when the part exists and the XPath selects a node. An empty value where the
    control shows its placeholder gives the placeholder, with no warning. `sdt.binding_differs` is
    raised under either `bindings` value. A bound block-level control is one paragraph.
  - charts: a `CHART` block after the paragraph, carrying CR-047's `ChartDescription` (REQ-706's kind,
    axes and secondary series, `getChart()`), and `ChartTable`'s title and rows; plain text writes the
    description's note before the title (REQ-511, REQ-707).
  - SmartArt (`Drawings.smartArt`): REQ-511's walk; a point whose paragraphs are all empty gives
    nothing. Legacy WordArt: a `TEXT_BOX` paragraph, under `text_boxes`.
  - embedded packages (`Embedded`): as §2.4 says. Also: a hidden sheet (`state` not visible) and a
    hidden slide are read under `hidden: true`, as hidden text is; a chart sheet is skipped; an
    OLE 1.0 package is opened only for ProgID `Package`, and read only when its file is `.txt`; a
    workbook's used range keeps the empty rows and columns inside it.
  - `altChunk` (`AltChunks`, `HtmlText`): dispatch by the declared content type, then the
    extension, then the bytes (a zip, a Flat OPC package). An XHTML chunk without docx4j-ImportXHTML
    is read as HTML. jsoup is an optional dependency of docx4j-core (D5), and a test dependency of
    docx4j-core-tests. The importer runs on a scratch package (it adds parts to the one it is given).
  - images: `ImageDescriber`, `AltTextDescriber`, `ImageDescription` (with its `Provenance`) and
    `TextImage` (public); a picture is an `IMAGE` run, an object an `OBJECT` run with its ProgID.
    `TextOptions.imageDescriber` is the host's hook (REQ-514's "MAY replace or wrap").
  - metadata (`Metadata`): `TextResult.metadata()` gains `core`, `app` and `custom` maps, by element
    or property name. The statistics are left out.
  - the glossary: each `w:docPart`'s body, `glossary:i/j`, after the endnotes.
- `fields: update` (`FieldValues`) computes in the walk from `FieldUpdater`'s resolvers and
  `FormattingSwitchHelper`, not on a copy as §2.6 said: the result is the same, and REQ-204 holds
  without a copy to discard. Computed: DOCPROPERTY, DOCVARIABLE, AUTHOR, TITLE, SUBJECT, KEYWORDS,
  COMMENTS, LASTSAVEDBY, REVNUM, CREATEDATE, SAVEDATE, SEQ (`\r`, `\c`, `\h`; not `\s`), REF (from a
  first reading of the document, since a REF may come before its bookmark; not with `\n`, `\r`,
  `\w`, `\p` or `\f`) and QUOTE; and FORMTEXT as Word's update leaves it (below, review item 1).
  Not computed: MERGEFIELD (no data is supplied), and everything REQ-506 excludes.
- Math (D3): `OmmlToLatex` and its exception are `org.docx4j.model.math` in docx4j-core, with the
  symbol tables they share with `LatexToOmml` (`LatexSymbols`). docx4j-markdown's
  `org.docx4j.markdown.math.OmmlToLatex` forwards to it, deprecated. `OmmlToLatex.keep(Predicate)`
  lets the text model read an equation in its view. In `markup`, an equation with tracked changes is
  its Reject All form deleted and its Accept All form inserted, each from CR-043's plan for that view.
  An equation outside the converter's subset is its runs' text inside the delimiters.
- Tables (REQ-605): the listed and grid forms in `PlainText`; `tables: tsv` writes a cell's non-empty
  paragraphs a space apart.
- `[page n]` (REQ-708) from 2 at the first marker, main story only; `layout: indent` (REQ-709) four
  spaces a 720-twip stop of the first line's indent (left plus first line less hanging), rounded,
  outside tables.
- `TextOptions.checkImplemented` refuses only `math: unicodemath` and `symbols: unicode`.
- `ResolvedView.unwraps(item)` (new): a content control inserted under tracking is gone after Reject
  All, its content left in its place, and its binding with it. The agreement test found it on core-ts's
  `check34/34b-tracked.docx`.

Where WD 0.8 is silent, phase 2 chose as follows; each is sent to the portfolio for the WD:
- addresses: a chart `body/3/chart:0`; SmartArt's lines `body/3/smartart:0/1`; WordArt
  `body/3/textbox:0/0`; an embedded package `body/3/object:0/` and its own address (`body/0`,
  `sheet:0`, `sheet:0/table`, `slide:0/2`); a chunk `body/7/altchunk:0/` and its own; the glossary
  `glossary:0/1`.
- REQ-605, a simple table: a cell is merged or split when it crosses a column edge another row has,
  or is in a vertical merge. A span every row shares merges nothing (Word leaves such columns behind;
  CR-043's `cr043-tracked.docx` C23, whose rows Reject All restores without spans). Empty
  paragraphs don't count toward "one paragraph per cell" (a block control in a cell comes with Word's
  empty paragraph).
- REQ-605, the listed format (ruled by Jason, 18 October 2026, on the recommendation of this session
  and of a Fable 5.1 review, session docx4j-44):
  - every cell is labelled by the header over its first grid column, or rather the first of its
    columns' headers that has text, once a cell: a cell spanning two headed columns takes the first's
    header, and a header spanning two columns labels each cell under it (`AB: P1; P2; AB: N`), so that
    every cell's start shows where a cell's paragraphs, also `; ` apart, end;
  - several header rows' texts are joined ` / ` (`Sales / 2024: 1`); a cell under a header with no
    text is its value alone;
  - a row whose one cell spans every grid column, a header row or a body row (a title, a divider such
    as "Part A"), is that cell's text alone, and labels nothing; when every header row is such a row
    the body is unlabelled: the WD's "else the first row" is for a table with no `w:tblHeader` row,
    and no data row is promoted to header;
  - a table with no labels (a table of header rows only, or whose headers span the grid or have no
    text) has its cells a tab apart, as the simple form does, `; ` staying between a cell's
    paragraphs;
  - a cell with no value is left out; a vertical merge's value is on every row it covers.

  An earlier rule (a spanning header labelling the cells under it once a row) wrote case 31's
  `TBL31_HEADER` once per row; the probe, cut before the listed format, expects it once, as the ruled
  rules write it: `[table: listed format]`, `TBL31_HEADER`, then `TBL31_CELL_P1; TBL31_CELL_P2`, a
  tab, `TBL31_NESTED`, then `TBL31_MERGED`, a tab, `TBL31_LAST_CELL`. `TextsPhase2Test.listedFormatHeaders`
  pins each rule.
- REQ-605, `tables: grid`: pandoc's grid table, column and row spans drawn, the header separator
  after the `w:tblHeader` rows else the first row, a nested table as a grid in its cell.
- REQ-515: the plain-text block is a line a kind of property that has any, before the body:
  `[core properties: title: ...; subject: ...]`, `[application properties: Company: ...]`,
  `[custom properties: name: value]`.
- REQ-604: the markup form of an equation with tracked changes (above).
- REQ-512: hidden sheets and slides under `hidden: true`; the OLE 1.0 rule (above).

WD 0.9 adopted each of these as a statement (REQ-519 new; REQ-605, 515, 604, 512). Jason confirmed on
19 October 2026 that they are decided and normative, not docx4j's choices awaiting a ruling (the listed
format he ruled on 18 October). Checking them then found two gaps, both fixed:
- The suite hadn't tested REQ-512's `hidden: true` branch, and the WD's Appendix A row for case 53 said
  the hidden slide was absent under every option. A scenario may now combine options `; ` apart
  (`ExtractionConformance.scenarioRequirements`, `TextsAdapter.options`, GOLDENS.md's adapter
  contract), and `EMB53_HIDDEN_SLIDE`'s row (`ExtractionAdditionalCases`, `expected-tokens.tsv`,
  README) is read under `embedded: true; hidden: true`: 893 pass, 51 pending. Hidden sheets have no
  case in the fixture, so `TextsPhase2Test.hiddenSheets` tests them (hidden and veryHidden left out,
  read under `hidden: true`, each sheet keeping its workbook index in its address). The Appendix A row
  is corrected in the WD.
- REQ-519 stated the text-box form `body/3/textbox:0/1` as settled. A NOTE in WD 0.15 (portfolio
  ac06ed7) now calls it a proposal that follows core-ts's text-model CR, to which core-ts CR-010's D8
  gives the text-box view and its addresses (section 2.1).

**A third fixture, 19 October 2026** (Jason: "build the separate fixture"). The three cases left for
the next probe build are in `extraction-probe-structure.docx`, built by `ExtractionStructureCases`
(registered with `ExtractionProbeBuilder`, which writes all three fixtures; only the new one was copied
in, and the committed `extraction-probe.docx` and `.docm`, which Word checked, are unchanged):
- case 55, a table of contents: a TOC field whose cached entries link the headings' bookmarks, and a
  TOC Heading paragraph that is not the field's. `HEAD55_ONE` and `HEAD55_TWO` are read once by
  default and twice under `toc: true` (REQ-520, REQ-505);
- case 56, a list interrupted by a margin paragraph, an indented one, an empty one, a margin paragraph
  inside its nested list, and a second list straight after: one line each in plain text, and REQ-703's
  rules in the Markdown golden (`2.`, the follow-on, `4. 2.`, `1)`; REQ-510's labels `4.2.`);
- case 57, an embedded workbook with a hidden and a very hidden sheet among two visible ones, read under
  `embedded: true; hidden: true` (REQ-512, REQ-507), absent under `embedded: true` alone.
The suite: 972 pass, 51 pending (75 rows and 4 goldens new); the eight goldens cut byte-identical. The
`toc` option's statement joins the harness (`scenarioRequirements`, `TextsAdapter`'s REQ-520). Jason's
Word-open check passed the same day: no repair prompt. His unchanged save is in `phase0/evidence/`
(`extraction-probe-structure-word365.docx`): Word kept every case, and docx4j reads it as it reads the
fixture (`word-open-results.md`).

Findings outside CR-046, not fixed here:
- docx4j-ImportXHTML (17.1.1-SNAPSHOT, f8c8695): `MHTContentHandler.body` keeps mime4j's body stream
  and reads it after `body()` returns, when mime4j has consumed it, so `convertMHT` gives an empty
  document ("Premature end of file"). With it on the class path, case 44 is still not expanded (and
  warns); its "present" rows would fail. **Fixed** in docx4j-ImportXHTML (VERSION_17_1_1, uncommitted on
  19 October 2026): the first text/html part, decoded by mime4j. mime4j has closed the body stream
  when body() returns since 0.8.9, and the bump to 0.8.10 (7dddaaf, in 11.5.3) broke it. Verified on 19
  October with its compiled classes on the class path: all nine "xhtml importer present" rows pass
  (case 44 in three views; case 47). The run found two gaps in this suite's harness, fixed the same day:
  a findings, order or golden row now needs its scenario's capability as a token row does, and a golden
  (cut without an importer) needs "xhtml importer absent". With the importer present: 888 pass, 56
  pending; without it, as Maven runs it: 893 and 51.
- The generated `FldChar.isFldLock()` and `CTSimpleField.isFldLock()` answer true when `w:fldLock` is
  absent, as `isCustomMarkFollows()` does. The walk reads the attribute as written. `FieldRef` copies
  `isFldLock()` (and `isDirty()`), so it reports every complex field locked.
- `XmlUtils`' marshal methods set a `wml.Document`'s `mc:Ignorable` prefixes on the shared prefix
  mapper and never reset them (`JaxbXmlPart`'s path does), so what is marshalled next declares those
  namespaces. `TextsPhase2Test`'s REQ-204 snapshot leaves namespace declarations out for this reason. **Fixed by CR-049**
  (19 October 2026): a marshal now leaves the prefix mapper as it found it and declares what its own
  root's `mc:Ignorable` names, and the snapshot compares every part's declarations again.

Review, 18 October 2026 (Fable 5.1, session docx4j-44, at Jason's request): it found no correctness
defect in the walk. It checked the suite (876 PASS, 57 PENDING), docx4j-markdown with `OmmlToLatex`
moved, REQ-509, 511, 512, 513, 514 and 708 against WD 0.8, `ResolvedView.unwraps` against
`Resolver.holder`, and `TextOptions.copy()`. It raised six items:
1. REQ-506 names "FORMTEXT defaults" among the fields `fields: update` recomputes, and docx4j keeps
   what was typed. Which is right turns on what Word's update does to a FORMTEXT field (it is believed
   to reset it to its default text, unless the form is protected), so an Office check is staged on the
   share (`Office 2016/cr046-formtext/`: a FORMTEXT with a default, one without, a `= 2+3` control,
   unprotected and protected for forms). The code, or the WD, follows its answer. First answer
   (Jason, 18 October 2026): in the unprotected document Ctrl+A, F9 reset the FORMTEXT to its default
   text (FT_DEFAULT), the one with none to a blank form field, and the control to 5, so the WD is
   right and docx4j follows it. Word 365 has no "Update fields before printing" (only "Allow fields
   containing tracked changes to update before printing"), so in the protected document the print
   preview updated nothing (the control stayed WRONG). Second answer, the same day: Word's save after
   F9 holds five en spaces (U+2002) for the blank; and on open, with `w:updateFields`, Word updated the
   unprotected document as F9 does, while in the one protected for filling in forms it updated the
   control (5) and kept what was typed. So under `fields: update` a FORMTEXT is its default text, or
   five en spaces where it has none, except in a document protected for forms
   (`w:documentProtection w:edit="forms"`, enforced), where it keeps what was typed. A text input of
   another type (number, date, calculated) or with a format keeps its cached result: Word's update of
   those hasn't been seen. `w:enforcement` is read as written: like `isFldLock()` (#697), the
   generated `isEnforcement()` answers true when it's absent. The three documents are fixtures
   (`text-extraction/formtext/`, `TextsPhase2Test.formTextAsWordUpdatesIt`, Word's save after F9 the
   oracle).
4b. WD 0.9 (portfolio a195eae) took up item 4 as two kinds, implemented here: `content.not_read`
   (REQ-801), where a chart, a SmartArt data part, or an embedded package or a part inside it can't
   be read, which then contributes nothing (a workbook that fails halfway no longer keeps its first
   sheets), the part's name the selector (the embedded package part's, for a part inside it) and the
   anchor's address the address; and `image.not_described` (REQ-514), where the describer throws, the
   image part's name the selector. `TextWarning.getSelector()` gives a warning's selector, whatever its
   kind (`TextsPhase2Test.contentNotRead`, `imageNotDescribed`).
2. A computed field's cached result was only partly left unread: its text was, but a copy of a note
   reference there was written and numbered, and a comment reference, a picture, a nested field's code
   (under `field_codes`) and the nested field's record came through. Fixed: nothing in it is read
   (`TextsPhase2Test.computedResultSuppressed`).
3. Cost: `fields: update` reads a package twice, and `markup` reads its tracked changes twice more
   for an equation with tracked changes; now in `Texts`' "Cost". A part's or content's route has no
   first reading, so a REF there is computed only when its bookmark comes before it; now said on those
   overloads. That route recorded a bookmark's text only once a field had been computed before it
   ended; fixed (`TextsPhase2Test.refOnThePartRoute`).
4. A chart that fails to read is left out with a log line and no warning: the WD has no kind for it, a
   gap sent to the portfolio (a `chart.not_read`?).
5. The tree holds three pieces of work, to be committed apart, in order: CR-047, the D3 move of
   `OmmlToLatex`, and phase 2.
6. The three findings above deserve issues of their own (the ImportXHTML one is now with its own
   session).

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

**Phase 3 implementation, 18 October 2026:**
- Tests: core-tests 1734 (11 skipped), docx4j-markdown 156, export-fo-tests 853 (9 skipped), none failing.
- The suite: 890 assertions pass, none fail, 51 are pending: the inspector's rows (REQ-902, 42) and
  case 44's "xhtml importer present" rows (9, the class path). Markdown's six rows pass, and so do the
  eight goldens (below). `ViewAgreementTest` stays 544/544. The adapter advertises REQ-703, REQ-704,
  REQ-705 and REQ-1203; docx4j-core-tests has docx4j-markdown as a test dependency.
- Markdown (REQ-703, REQ-704, D6): `WmlToMarkdown` writes a `TextResult`, so its text is the text
  model's. It keeps its presentation rules, now read from the block model, which gains what they need:
  - a run's `isBold()`, `isItalic()`, `isStrike()` and `isMonospace()`, each what the run adds to its
    paragraph's style (the rule `WmlToMarkdown` had); `getLink()`, a `w:hyperlink`'s target (its
    relationship's, then `#` and its anchor) or a HYPERLINK field's (its address, then `#` and its `\l`
    bookmark), whatever `field_codes` says; `isMath()` and `isDisplayMath()`;
  - a block's `getOutlineLevel()`, `getNumbering()` (`TextNumbering`: the `w:numId`, level, format,
    level text and start), `getContextualSpacing()` and `hasBottomBorder()`; every block of a note,
    a table's and a chart's too, carries the note's type, number and id.

  `MarkdownExporter.export(TextResult)` (and `export(TextResult, pkg)`, the package asked for a quote
  style's own indent) writes a reading the caller already has. `MarkdownExportOptions.getTextOptions()`
  holds the text model's options; `setView` is the view, `TrackedChangesPolicy` its deprecated alias.
  Under `metadata: true` the Markdown begins with YAML front matter.
- The `toc` option (D12): `TextOptions.toc`, `isToc()`, the name `toc` after `fields`. A TOC field
  under `toc: false` is suppressed as a recomputed field's cached result is (nothing in it read, no
  record, no `field.no_result`); its code, shown under `field_codes` until the field is known to be a
  TOC at its separator, is taken out again; a paragraph the field touches and nothing else fills is
  left out (`TextsTocTest`). `WmlToMarkdown` no longer drops TOC1 to TOC9 paragraphs itself.
- What changes in docx4j's Markdown (each pinned by a docx4j-markdown test; §2.8 listed the first four):
  - the `markup` view writes CriticMarkup, not `~~`; comments are `{>> <<}` at their reference;
  - the `original` view;
  - hidden text is left out;
  - a text box's paragraphs follow the paragraph it is anchored in (before: not written);
  - endnotes are notes, `[^e1]` (before: left out); a HYPERLINK field's result is a link (before: its
    text);
  - a chartex chart's Choice is the one branch read (REQ-301), so Word's picture of the chart, its
    Fallback, is no longer written before the chart's table (`MarkdownChartTest.waterfall`);
  - an equation outside `OmmlToLatex`'s subset is its runs' text inside `$...$` (phase 2's REQ-604
    rule), not plain text;
  - the GFM header is the `w:tblHeader` rows, else the first row (before: always the first); several
    header rows are one, their cells' texts ` / ` apart; a nested table's paragraphs are in its cell
    (before: dropped); a cell's empty paragraphs leave no `<br>`; a column's alignment is its topmost
    header cell's first paragraph's effective `w:jc`, empty or not (GFM aligns a column from its
    header cell; before: that paragraph's own `w:jc`);
  - a table of contents is left out by the text model's rule (D12, `toc`): a TOC field's result, not a
    paragraph in a TOC style, so a table of contents whose field was unlinked is now written;
  - fields, bound values, SmartArt, WordArt, `altChunk`s and the options of WD section 10 are the text
    model's.
- Where WD 0.10 is silent, phase 3 chose as follows. WD 0.11 adopted each (portfolio 69779bf, corrected
  in 336ff86 with the alignment rule and the definition of a monospaced font, `Extractor.MONOSPACE`'s
  list), but D12, which it recorded as open until decided (below):
  - REQ-703, notes: a footnote is `[^n]`, an endnote `[^en]` (the two are numbered apart, REQ-503), a
    note whose reference carries a custom mark `[^f-<id>]` or `[^e-<id>]`; a note's definitions are where
    the text model reads the note, after the main story;
  - REQ-703, presentation beyond the WD's list, all of it `WmlToMarkdown`'s before: an outline level
    above 5 is a paragraph; a numbering level that is a label (`none`, or no `%n`: "NOTE") is that
    label in bold before the paragraph; a paragraph in the Quote or IntenseQuote style is a block quote,
    nested one level for each 720 twips of indent beyond the style's own; a paragraph in a code style,
    or in a named style whose own font is monospaced, is a fenced code block, consecutive ones in one
    fence; an empty paragraph with a bottom border of its own is a thematic break;
  - D12's `toc` option, a TOC field's result left out by default in every serialisation (docx4j's
    Markdown left out TOC1 to TOC9 paragraphs instead);
  - REQ-704: the front matter is a mapping for each kind of property, `core`, `app` and `custom`, each
    property's value a double-quoted string;
  - REQ-1203: a Markdown golden's image destinations aren't compared (REQ-703 writes `![alt](…)`).
- HTML (REQ-705, D11): insertions and moves to are `<ins>`, deletions and moves from `<del>`, each with
  `data-author` and `datetime` and its class kept (`ins`, `del`) for the stylesheet, on both pathways
  (before: spans); a `w:delText` in a `<del>` is its text. `AbstractConversionSettings.setView(TextView)`
  and `getView()` are the HTML and FO exporters' `view`: `markup` the default (as before), `accepted`
  through `PP_COMMON_ACCEPT_TRACKED_CHANGES`, `original` through the new
  `PP_COMMON_REJECT_TRACKED_CHANGES` and `TrackedChanges.rejectAllForConversion`, each on the
  exporter's working copy (`HtmlViewTest`, docx4j-export-fo-tests' `FoViewTest`, both pathways).
- `TraversalUtil` (§2.8): `McMode.READ`, the default, takes the `TEXT` purpose
  (`McSelection.branches(ac, READ)` is `selectedBranch(TEXT, ac)`); the new `McMode.DRAW` is the old
  READ, the drawn branch (`docx4j.jaxb.mc.preferChoice`). The layout-fidelity gate was run first, as
  §2.8 asks: b282 (HEAD 187e5a367, the released renderer 2.11-docx4j.7) equals b281, 0 movers; b283,
  the switch alone, and b284, as built (with the walks below), each 0 movers on the four corpora, every
  event count equal, the probes unchanged (`~/fidelity-cr030/gate-b282-b283.sh`, `-b284.sh`). A
  review of every walk in READ (Claude Fable 5.1, a subagent of this session) found the exporters'
  own walks drawing already (`AbstractVisitorExporterGenerator.walkJAXBElements` selects the drawn
  branch itself), no numbering or fonts pass on the FO path that the switch reaches (numbering is
  computed in the exporter's walk; `FontsAnalysis` and `fontsInUse` are `ALL`), and these, now explicit:
  - `DRAW`, walks serving an output's layout: `PageNumberInformationCollector`,
    `AbstractWmlConversionContext.hasBookmark` (a reference must target what is drawn),
    `Paginate.bodyParagraphs`, `HeaderFooterPolicy`'s emptiness test, `ConversionSectionWrapperFactory`'s
    control finder, the table models' row and cell finders, the visitor exporters' generators (their
    constructor), and the layout-fidelity harness's table walks;
  - `ALL`, mutators: `CompressPictures` (a latent fault: it re-cropped one branch's picture of a
    bitmap it cut, the other keeping the old crop), `ChartCreator`'s removal searches,
    `FieldHyperlinkStyle`;
  - `READ` kept, with the reason said: `isMissingBookmark` (Word looks in the branch it reads) and
    `UpdateXmlFromDocumentSurface` (a control's value is in the branch Word reads, now the Choice).

  On Word's files READ now reads the Choice almost everywhere (the understood set holds `wps`, `wpg`,
  `wpc`, `w14`, `wp14`, `w15`, VML and chartex; `wpi` and `a14` fall back). A walk over a deep copy
  (the exporters' working copies) matches `Requires` by the conventional prefix table, since the
  recorded URIs are keyed by the original's Choice objects; Word's own prefixes make that the same.
- `TextUtils` is deprecated, its output unchanged; its javadoc says it is not the text model and points
  to `Texts`. Its remaining callers are no text readers (the anonymiser's scramble length, the binding's
  surface written back, a core property's literal) and keep it, the deprecation suppressed.
- The goldens (REQ-1203): `phase0/goldens.tsv` names eight, each fixture's plain text in the three views
  and its Markdown, cut by `ExtractionGoldens` and reviewed by hand against WD 0.10 sections 3 to 7
  (GOLDENS.md). Nothing in them departs from the WD.

**After the phase 3 commit (f59828e62), 18 October 2026, at Jason's request ("fix the markdown drop
now"):** the Markdown export dropped a picture with no image part, alt text and all. That happened to
an SVG picture written as docx4j's `ImageAddSVG` sample writes it (an `a:blip` with only its
`asvg:svgBlip`), and to a linked or missing picture (issue 699). Now:
- the text model's picture part is `a:blip/@r:embed`'s, else its svgBlip's (`TextImage.getPart()`: an
  `image/svg+xml` `DefaultXmlPart`, as docx4j loads one);
- Markdown writes an SVG part's SVG (a `data:image/svg+xml` URI, or `imageN.svg` under `imageDirPath`),
  and a picture whose image the package doesn't hold (linked, or a missing relationship) as its alt
  text with an empty destination, `![alt]()`. The external target is never written: a renderer would
  fetch it (WD section 11, "no serialization follows a link").

One golden changes with it: `extraction-probe-external.md` gains case 50's linked picture,
`![Picture 17](…)`, recut by `ExtractionGoldens` and reviewed (the other seven are unchanged).

**Review of phase 3 (Claude Fable 5.1, session docx4j-43, at Jason's request, 18 October 2026):** sound,
matching this record and D12. Two follow-ups, fixed:
1. `TextNumbering` read the abstract definition's level, so a `w:lvlOverride` (a `w:startOverride`, an
   overriding `w:lvl`) wasn't seen; it now reads the num instance's level, the overrides applied
   (`ListNumberingDefinition.getLevel`), the abstract's only where that is missing
   (`MarkdownTextModelTest.numberingOverrides`). `WmlToMarkdown` before phase 3 had the same fault.
2. Under `field_codes: true`, a TOC's code taken out left the paragraph's merge floor raised, so the text
   after the field began a new run; the floor is put back (`TextsTocTest`).
3. Asked by the portfolio for the WD (REQ-510): the numbering-style indirection (`w:numStyleLink` to a
   numbering style whose `w:numPr` names the `w:num` holding the levels) is followed, and a `w:numPr`
   from the paragraph's style counts (the effective `w:numPr`, through `w:basedOn`). But a level linked to
   a paragraph style (`w:lvl/w:pStyle`) gave a list marker to a paragraph whose other style merely carries
   the same `w:numPr`, where Word gives it no number and the list labels gave none; `TextNumbering` now
   applies the labels' rule (`MarkdownTextModelTest.numberingThroughStyles`).
4. Jason asked for proof of item 3's rule, and two Word goldens followed (layout-fidelity probes
   `numbering-linked-level-other-style`, `numbering-unlinked-level-claim` and
   `numbering-shared-abstract-styles`, 18 October 2026; the
   documents in core-tests' `LevelOwnerTest`, the rule in CR-014's row 5 and word-layout-rules
   section 2.8). A level belongs to one paragraph style: the one its `w:pStyle` names, else the first
   in styles.xml whose own `w:numPr` names it. Any other style carrying it is unlabelled and uncounted,
   with `w:ilvl 0` or none, and through a second `w:num` on the same `w:abstractNum`; and a style
   that a level names, but whose `w:numPr` names another level, is read at its `w:numPr`'s level (not
   at the naming level, as ECMA-376 17.9.24 has it). The labels and `TextNumbering` had every
   case right except a level that names no style: both numbered every style carrying it. Both now
   share `Emulator.levelStyleId`. The unlabelled paragraph keeps the level's indent, as Word's re-save
   writes it, which the text model's indents already gave.
5. Markdown and an interrupted list (Jason chose option (c) of three, 18 October 2026). The export
   had closed a list at any paragraph that wasn't an item, the next item opening a new list at `1.`,
   so Word's count was lost (Word: 1. note 2.). Now:
   - an unlabelled paragraph indented at least to an open item's text (its left indent, not at the
     margin), or in the ListParagraph style as before, is a follow-on paragraph of the deepest such
     item, the lists deeper than it closed;
   - any other paragraph ends the lists, and the list goes on at its own count where it resumes
     (`2. Pears`); a nested list resumes under an empty item showing its parent's count
     (`2. 2. B.2`), since Markdown can't continue a nested list without its parent item;
   - an empty paragraph neither ends a list nor joins it;
   - an ordered list's items are consecutive from its start, so an item whose count isn't the next
     one begins a new list at its count; two `w:num` on one `w:abstractNum` are one list (CR-014 P1),
     and a level drawn as the other kind (bullet or number) is a list of that kind;
   - a list directly after another of its kind takes the other delimiter (`1.` or `1)`, `-` or `*`),
     which CommonMark reads as a new list. Before, two such lists ran together: in
     `numberingThroughStyles` "3. Linked" and "3. Styled", Word's III. and III., rendered as 3 and 4.
   The block model gains `TextNumbering.getValue()` (the count the label shows, from the emulator,
   which now counts once per paragraph for the label and the value alike) and `getAbstractNumId()`.
   Tests: `MarkdownTextModelTest` (six), `LevelOwnerTest.theTextModelsCountIsTheLabels`; the eight
   goldens are unchanged (recut, byte-identical).
It also found Eclipse's classes in docx4j-core-tests' `target/test-classes` compiled without
docx4j-markdown (a Maven project update in Eclipse is needed), which fails a maven run without `clean`.

Tests: docx4j-markdown 160 (`svgPictureWithNoPngCopy`, `pictureWithNoImageKeepsItsAltText`,
`numberingOverrides`, `numberingThroughStyles`); core-tests 1734 (11 skipped); the suite 890/0/51.
With item 4, `LevelOwnerTest` (6).
`AnonymizeXlsxProbesTest.textNumbersFormulasAndNames` failed once and passed on three reruns: its
scramble's `Random` is unseeded, so a two-letter value can come back unchanged; not this work's.

### Phase 4: hand-offs

The goldens' commit goes to:
- **docx4j-core-ts**, for its text-model CR (the next free number: its CR-011 is tracked changes in
  every story): structural `getText` and `outline`, text boxes, hidden
  text, notes, the agreement test;
- **docx4j-python**, for its CR-011: one text model in place of five;
- **docx4j-generated-objects-ts**: `textOf` follows the rules, or core-ts stops using it;
- **docx4j-mcp**: the engine bump to 17.3.x (D14), then `view`, `include` and the booleans on
  `extract_text`, and the block model's JSON (REQ-706's verbs are the MCP's);
- **the editor**: the same parameters on its verbs.

The portfolio registry is updated through the portfolio session.

**Acceptance:** no statements. The hand-offs are sent with the commit hashes, and each
receiving session has acknowledged.

**Sent 18 October 2026** (Jason: "let's do CR-046 phase 4"), against WD 0.14 (portfolio cf56600)
and the goldens at d9acdf5e2. Every message names the phase commits (c0c72ecef, 65a527d47,
f71cdd299, 8da15bb8c, 608e40023, 187e5a367, f59828e62, d9acdf5e2; from 608e40023 on not yet
pushed, so the local clone is the oracle), the goldens and fixtures in `text-extraction/phase0/`,
and the harness to mirror (`ExtractionConformance`, `TextsAdapter`). (The messages put GOLDENS.md in
`phase0/`; it is in `text-extraction/`, corrected to every receiver the same day.) Each also names what WD 0.14
added since the last sync: REQ-510's numbering ownership with its three Word goldens
(`LevelOwnerTest`), REQ-706's numbering object, REQ-703's list rules and REQ-520's `toc`.

Every receiver acknowledged the same day, which is the acceptance; phase 4 is done. What each opened
or proposes is its own, and waits on Jason.

| Receiver | Session | Sent | Acknowledged |
|---|---|---|---|
| docx4j-core-ts: its text-model CR (CR-012 the next free number) | docx4j-core-ts-5b | yes | yes, 18 October 2026, receipt only: opening CR-012 is Jason's call. Noted for it: the agreement test, REQ-519's address kinds, a TSV adapter. REQ-510 will move labels in its Emulator's parity goldens, a port item apart from the text CR |
| docx4j-python: its CR-011 | docx4j-python-cd | yes | yes, 18 October 2026: commits, goldens and WD 0.14 checked in the local clone; CR-011 being written (its first phase an adapter over the TSVs), implementation on Jason's go |
| docx4j-generated-objects-ts: `textOf` follows the rules, or core-ts stops using it (theirs and core-ts's to agree) | docx4j-generated-objects-ts-82 | yes | yes, 18 October 2026. It proposed the second, mirroring section 2.7. **Decided by Jason, 19 October 2026:** `textOf` stays as it is, TextUtils's counterpart with its output unchanged, and is not deprecated: its callers are core-ts's Office JS readers (`Paragraph.text`, `ContentControl.text`, tables' text), held to what Word's Office JS returns, not to the WD. The text model goes into core-ts as its CR-012. objects-ts c31cb83 documents `textOf` as not the text model; no objects-ts CR |
| the editor: the section 10 options on its text verbs | docx4j-ts-editor-50 | yes | yes, 18 October 2026, with ED-008 (editor 37c8565, proposed): section 10's options on outline, find and read; an unimplemented value refused naming its statement, as section 2.6 does; every reader on core-ts's text model once its CR lands, the editor adding nothing to the text |
| docx4j-mcp: the engine bump (on 17.1.0; 17.3.4 not released), `extract_text`'s options, the block model's JSON | docx4j-mcp-a1 (started for it) | yes | yes, 18 October 2026, with CR-mcp-server §19 (proposed, uncommitted). Three phases: the bump to 17.3.4 (33 tests pass on a trial against the snapshot); `extract_text` and `docx_to_markdown` on the model, with the goldens copied verbatim; the block model's JSON and outline / find / read, matching ED-008. Its decisions T1 to T6 wait on Jason. It may ask docx4j-markdown for per-block address comments |

## 5. Every statement of WD 0.8, and where it is met

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

- **D12 (proposed in phase 3, 18 October 2026). A table of contents in Markdown.** `WmlToMarkdown`
  has left out a paragraph in a TOC1 to TOC9 style since 17.2.0 (CR-005, `tocEntriesAreDropped`): its
  entry links to a Word bookmark no Markdown renderer has, with a tab and a page number, and the
  headings it lists are navigable in Markdown anyway. Plain text keeps them (REQ-505: a field's result
  is text), and REQ-202 asks every reader for the same text. Options: (a) keep leaving them out, and
  propose the WD allow a Markdown serialisation to omit a TOC field's entries, as it re-renders headings
  and lists; (b) write them, each a link to its `#_Toc` anchor; (c) a scope option for every
  serialisation. Recommended: (a), the rule stated by style as docx4j has it. **Decided 18 October 2026:
  (c), an option `toc`, false by default** (Jason: "maybe we should have an option (applicable to all
  formats) which specifies whether the TOC is included or not?", then "i agree with false"). A table of
  contents is the result of a TOC field (a table of figures too), not a paragraph style: under
  `toc: false` nothing of the field is read, its code included under `field_codes`, and a paragraph
  with nothing else in it is no paragraph; a TOC heading is not the field's; an unlinked table of
  contents is text. The probe has no table of contents, so no golden turns on it. WD 0.12 states it as
  REQ-520 (portfolio c5e4236), with `toc` in §10 after `fields`.

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
- CR-047, the chart kind, axis titles and series axes, for case 34's axis title in phase 2. Met:
  CR-047 implemented 18 October 2026, its Office check answered the same day; case 34's rows pass.
- The address grammar's text-box form (core-ts's text-model CR, by CR-010's D8) may rename the form;
  at most a spelling change.

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

- WD 0.8: `docx4j-portfolio/docs/text_extraction_specification_wd.md`, 2c00660.
  - 0.2 (2566012) answered this CR's first questions.
  - 0.3 (e8f14e1) added REQ-307's citation of REQ-1201's normalisation.
  - 0.4 (2062e86; [PROBE] corrected in c52b031) answered phase 0's questions: embedded
    workbooks and presentations, nesting across kinds, `include` against custom XML.
  - 0.5 (0069d6a) ruled on §6.1's Q1 to Q3.
  - 0.6 (97fce95) recorded cases 53 and 54 in Appendix A.2 and [PROBE].
  - 0.7 (14e43a6, REQ-1201 corrected in b871d1f) made phase 1's choices statements and recorded its
    findings (the published list, the harness's rules, the `Requires` save defect).
  - 0.8 (2c00660) adopted CR-047's chart note: its spelling in REQ-511, `[chart: ...]` in REQ-707,
    the chart block's fields in REQ-706, case 34's expected note.
- The paper: `docx4j-portfolio/docs/text_extraction_behaviour_model.md`, decisions §9 (2d8fd36),
  where it lands §10.
- CR-043 (the views and their fixtures); CR-021 §8.5 (Word and the Fallback; the `altChunk`
  rule, 17 October 2026).
- The probe: `docx4j-core-tests/src/test/resources/text-extraction/`, 4a89a5967, and its
  README's Word section; the phase 0 suite in its `phase0/` directory, c0c72ecef.
- docx4j-core-ts CR-002 §3.3 (addresses) and its `UNDERSTOOD_NAMESPACES`.
