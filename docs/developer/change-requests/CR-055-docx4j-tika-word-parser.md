# CR-055: docx4j-tika, a Tika parser for Word documents over the text model

Status: ACCEPTED 2026-10-22 (Jason: "CR-055: recommendations accepted for D1, D2, D3, D4, D5, D6, D7, D8. Commit it,
then do phase 0"); phase 0 DONE 2026-10-22 on branch `docx4j-tika` off `VERSION_17_3_4` (the docx4j-25 session, Claude
Fable 5.1; record in §8); phases 1 to 3 open. D1 REVISED 2026-10-31 (Jason: "let's revise D1, folding these
considerations into the CR"): `docx4j-tika` is its own repository on GitHub, not a module in this reactor; the
arguments and the consequences in §5a, the move is phase 0b: DONE 2026-10-31 (§8b; the repository is
`plutext/docx4j-tika`, its CR-001 carries phases 1 to 3; D9 accepted 2026-10-31, phase 1 under way there). PROPOSED 2026-10-22, drafted by Claude Opus 5.5 (session docx4j-portfolio-9d) at Jason's request
("write the docx4j CR"), after a prototype built and measured the same day ("build a prototype against
17.3.4-SNAPSHOT and compare it with Tika on the probes"). Reviewed by docx4j-25 the same day: the number
confirmed, and four notes taken (§3.1, §3.7, §4 phase 0, §6). Its commit is the docx4j session's. Phases in §4,
decisions in §5 (D1 to D8 accepted as recommended, 2026-10-22).

## 0. Summary

Apache Tika reads a Word document through its own `OOXMLParser`: since 3.3.2 a SAX handler of Tika's over
POI's package layer (`useSAXDocxExtractor=true`), and in 4.x nothing else. Its defaults are the ones
docx4j-portfolio's text extraction paper measured and departed from:
- every `mc:Choice` read, `Requires` never evaluated;
- hidden text in;
- every header part read, those no section uses included;
- a bound control's stored surface;
- an SVG's `<text>` read as text, and the PNG beside it OCR'd where Tesseract is installed.

The JVM frameworks inherit Tika's defaults without an option (paper §7.4): Spring AI, LangChain4j's Tika
parser, txtai, Open WebUI's Tika engine, Extractous, and Elasticsearch's and OpenSearch's ingest-attachment.

This CR proposes `docx4j-tika` (D1; accepted as a module in this reactor, its own repository since D1 was
revised on 2026-10-31, §5a), holding one Tika `Parser` for the four Word OOXML types.
It loads the package with docx4j, reads it with `Texts.of` (CR-046, the specification's behaviour, WD 0.31)
and writes Tika's XHTML and metadata.
- **Registration:** Tika sorts its own `org.apache.tika.*` parsers first and lets a later parser for the same
  type win, so the jar on the class path is enough. xlsx and pptx stay with Tika.
- **Prototype:** the one built on 2026-10-22 (§2) matched docx4j's own conformance answers on all 177
  expected tokens of the phase 0 probes, where Tika 3.3.3 gave 171 and 76 it should not have.

## 1. Background

Tika's Word path, read and measured by docx4j-portfolio (paper §7.8 to §7.10, §12a and Appendix A, 20 to
22 October 2026):

- **`mc:AlternateContent`.** `OOXMLWordAndPowerPointTextHandler` skips the `Fallback` under
  `preferAlternateContentChoice=true` (the default) and reads every `Choice`. On the ten-`Choice` probes it
  gives all ten at text, run, block, row and cell level, where Word 365 shows one.
- **Stories.** Headers and footers are in by default, every header part included. Comments are in, and
  embedded objects are parsed recursively after the body.
- **Hidden text and bindings.** `w:vanish` text is in. A bound control gives its stored surface, not the
  value Word shows.
- **`altChunk`.** HTML and MHT chunks are parsed as embedded documents after the body: `display:none` text
  included, and an MHT's unreferenced second part. A Flat OPC or docx chunk's inner header and hidden text are
  included too, and an RTF chunk's `\v` text.
- **Pictures.** A picture with an `asvg:svgBlip` hands both images to the embedded parser: the SVG's text comes
  out through `DcXMLParser`, and the PNG's pixels through `TesseractOCRParser` where Tesseract is installed.
- **What a user can set.** `OfficeParserConfig`'s flags: deleted content, move-from content, headers and
  footers, shape-based content, the `Choice`/`Fallback` preference. None of them evaluates `Requires`, reads
  a bound value, or keeps hidden text out.

docx4j's text model already answers each of these as the specification asks and Word shows. Filing Tika
issues is one route (Appendix A.4 of the paper; Jason's decision). This CR is the other: a parser any
classpath-based Tika user can drop in, today.

## 2. The prototype and its measurements (2026-10-22)

**What it was.** About 200 lines, built in docx4j-portfolio's session scratch (`tikadocx4j/`): the parser
class (`org.docx4j.tika.Docx4jWordParser`), a `META-INF/services/org.apache.tika.parser.Parser` file, a
harness calling `AutoDetectParser` with a `BodyContentHandler` as the inheriting frameworks do, and two
scorers.
- **Class path:** tika-app 3.3.3, docx4j-core 17.3.4-SNAPSHOT as installed on 2026-10-22 at 10:06 and its
  32-jar runtime class path. For a second run, docx4j-ImportXHTML 17.1.1-SNAPSHOT with openhtmltopdf.
- **The parser:** maps the text model's blocks to `p`, `h1` to `h6`, `table`/`tr`/`td`/`th` (with `colspan`),
  hyperlinks to `a`, and the stories to Tika's `div class="header"`, `"footer"`, `"footnote"`, `"endnote"`,
  `"comment"`. Charts become `div class="chart"`, holding the chart's description line, title and data table.
  The text model's warnings go to metadata as `docx4j:warning`.
- **Defaults:** the specification's. An `OfficeParserConfig` in the `ParseContext` maps headers and footers,
  deleted content (the `markup` view) and shape-based content.

**Registration.** With the jar on the class path and no configuration, `X-TIKA:Parsed-By` named the docx4j
parser for every Word file. Tika's `ServiceLoaderUtils.sortLoadedClasses` (`CompareUtils.compareClassName`)
puts `org.apache.tika.` classes first, and `CompositeParser` keeps the last parser for a type.

**On the 29 probes**, every one parsed without an exception:

| Probes | Tika 3.3.3 | docx4j in Tika |
|---|---|---|
| phase 0 conformance fixtures (`extraction-probe`, `-structure`, `-inspect`, `-external`; 177 tokens expected under `accepted`) | 171 found; **76 tokens it should not give**: 29 that no setting of the specification gives, 44 where Tika's default differs from one of its options (headers, comments, hidden text, embedded objects, the glossary), 3 right where an HTML reader is present | **177 found, none extra**; with ImportXHTML present, the 3 chunk tokens too, as that environment's rows ask |
| the ten-`Choice` probes, text, run, block, row and cell level, both orders (8) | every `Choice`, in all 8 | Word 365's branch, in all 8 |
| bound controls (`bind-fun`, 6) | the stored surface throughout | Word's value for plain-text controls in the body, core and extended properties, and the OpenDoPE template. The surface where REQ-509 reads it (typed controls, rich-text bindings) or Word does more (a store item ID repaired, a repeating section expanded), and for the cover-page Abstract, a defect found here and fixed by the docx4j session the same day (`BoundValues.document` now marshals a `JaxbXmlPart` custom XML part; uncommitted when this was written) |
| HTML, XHTML and MHT chunks (4) | the shown text, the `display:none` text, an MHT's second part | with ImportXHTML, the shown text and nothing hidden; without it, `altchunk.not_expanded` |
| Flat OPC and docx chunks (2) | adds the inner hidden text and the inner header | as Word shows them |
| RTF chunk | read, `\v` text included | not read, `altchunk.not_expanded` (no RTF reader) |
| a picture with an SVG and a PNG | the SVG's `<text>`, and the PNG by OCR | neither (REQ-514) |

**Timing.** Over the 29 files: Tika 3.8 s; docx4j 1.2 s, of which the first file's 0.7 s is JAXB start-up;
docx4j with ImportXHTML 1.7 s. This is not a benchmark: Tika also parsed the embedded objects and ran OCR,
which the prototype does not hand back (§3.4).

**Not tried:** Tika 4.x; tika-server; memory on large documents; a corpus beyond the probes.

## 3. Proposal

### 3.1 The module

`docx4j-tika` (D1 as revised 2026-10-31, §5a: its own repository, `plutext/docx4j-tika` on GitHub, with docx4j a
released dependency; until then the module on branch `docx4j-tika` of this reactor, beside `docx4j-markdown`):
- one public class, `org.docx4j.tika.Docx4jWordParser implements org.apache.tika.parser.Parser`, and its
  service file;
- a small `Docx4jTikaConfig` that a caller may put in the `ParseContext` (§3.3);
- dependencies: docx4j-core, and docx4j-JAXB-ReferenceImpl at runtime (both at a released docx4j version, named
  once in the pom and in the README, since the revised D1), with the README saying how to swap it
  for docx4j-JAXB-MOXy, so a host does not pick up whatever JAXB runtime it happens to carry; Tika as
  `provided`, so the host's Tika is the one that runs (D2).

Supported types:
- `application/vnd.openxmlformats-officedocument.wordprocessingml.document` and `.template`;
- `application/vnd.ms-word.document.macroenabled.12` and `vnd.ms-word.template.macroenabled.12`.

Flat OPC Word XML is a later question.

### 3.2 Reading and output

`WordprocessingMLPackage.load(stream)`, then `Texts.of(pkg, options)`, then the blocks to Tika's XHTML through
`XHTMLContentHandler`, as in §2. Phase 1 completes the mapping the prototype began:
- lists as `ul`/`ol`/`li` from the numbering model, and headings by outline level;
- text boxes in a `div class="textbox"` after their anchor, and notes and comments in Tika's classes;
- images as Tika emits them (`img` with `src="embedded:..."` and `alt`), math as the text model's LaTeX;
- the inspect counts (REQ-903) in metadata.

The text the downstream sees is the specification's, and the shape is Tika's.

### 3.3 Options

The defaults are the specification's (D3).
- **Tika's own flags:** where a caller has put an `OfficeParserConfig` in the `ParseContext`, its flags map
  where they correspond. Headers and footers map to `include`, deleted content to the `markup` view, and
  shape-based content to `text_boxes`. `preferAlternateContentChoice` has no counterpart: the `mc` rule is
  not an option.
- **Everything else:** a `Docx4jTikaConfig` carries a `TextOptions` for the rest (`hidden`, `bindings`,
  `comments`, `alt_text` and so on). In a Tika 3 `tika-config.xml` the parser takes the same as `<params>`.

### 3.4 Embedded content and images

Tika's contract is that a parser hands embedded resources to the `EmbeddedDocumentExtractor` in the
`ParseContext`, which recursion (`RecursiveParserWrapper`) and OCR rely on. The parser should do the same (D4):
- embedded packages, OLE binaries and images handed to the extractor;
- the specification's `embedded: false` respected for the text itself.

For a picture with both an `a:blip` and an `asvg:svgBlip`, only the image Word draws is handed on, the SVG,
and it is not handed to a text parser. That keeps REQ-514 and avoids the double reading of §1 (D5).

### 3.5 Metadata

Tika's OOXML keys, so existing mappings keep working (D6):
- `dc:title`, `dc:creator`, `dc:subject`, `dc:description`, `meta:keyword`, `cp:category`, `cp:revision`,
  `dcterms:created`, `dcterms:modified` and `meta:last-author`;
- `extended-properties:*` from the application properties, `custom:*` from the custom properties.

The text model's warnings (`mce.branches_differ`, `sdt.binding_differs`, `altchunk.not_expanded`, and so on)
go in as `docx4j:warning` values, and the inspect counts as `docx4j:inspect:*`.

### 3.6 HTML chunks and the LGPL

The text model expands an HTML, XHTML or MHT `altChunk` only with both jsoup (MIT) and docx4j-ImportXHTML
(LGPL) present (`AltChunks`, `JsoupHtml`).
- **Without ImportXHTML**, the chunk is reported, not read.
- **The proposal (D7):** `docx4j-tika` depends on neither LGPL piece. Its README says what adding
  ImportXHTML (the ImportXHTML-bundle of ImportXHTML's CR-004) changes, so an Apache-only deployment stays
  Apache-only.
- **Later:** the paper's §5 #20d asks that text and Markdown read an HTML chunk's blocks with no importer at
  all. That would lift the dependency, and is a text-model change of its own, not this CR's.

### 3.7 Limits and safety

- **Tika's write limit** applies through the caller's `ContentHandler` (`WriteLimitReachedException`), as
  for any parser.
- **Package limits:** docx4j's `ZipPartStore` limits (`MAX_RATIO.unzip.error` 500, 50 MB uncompressed, logged
  on load) and its XML parsing with DTDs off cover the package. They are documented in the module's README,
  with how to set them.
- **Thread safety:** one parser instance serves concurrent parses: no state outside `parse`.
  `WordprocessingMLPackage.load` and `Texts.of` are per call, but `Docx4jProperties` is static and JVM-wide,
  so a host that sets docx4j properties (the zip limits among them) sets them for every parse in that JVM.

## 4. Phases

| Phase | What | Size |
|---|---|---|
| 0 | The prototype captured on a branch (D8): the module skeleton, the parser as it stands, the service file, and the comparison harness as an integration test (`TikaParityIT`) over the phase 0 fixtures and docx4j-portfolio's `mc-fun`, `bind-fun`, `altchunk-fun` and `svg-fun` probes, with §2's numbers as its baseline. The test runs against tika-core (`provided`) and tika-parsers-standard-package in test scope, the class path a Maven host has, not tika-app's fat jar, and asserts `X-TIKA:Parsed-By` for every Word file (§6). **DONE 2026-10-22** on branch `docx4j-tika` (§8) | ½ day |
| 0b | The move (D1 revised, §5a): the `docx4j-tika` directory from branch `docx4j-tika` into its own repository with its history (`git subtree split`, two commits); a parent-less pom (groupId `org.docx4j`, docx4j-core and docx4j-JAXB-ReferenceImpl at a released version in one property, Tika `provided`, failsafe as now); LICENSE, NOTICE and the README; a GitHub Actions build running `TikaParityIT`, its drift check against docx4j-core-tests' fixtures changed to compare with a docx4j checkout beside the repository when there is one; the module line and the branch removed from this reactor once the repository builds. **DONE 2026-10-31** (§8b) | ½ day |
| 1 | The production parser: §3.2's full mapping, §3.3's options, §3.4's embedded hand-off, §3.5's metadata, errors as `TikaException`. Tests: the phase 0 conformance suite through `AutoDetectParser` (every claimed row), and Tika's own Word test documents for crashes and metadata parity | 3 to 4 days |
| 2 | Packaging and documentation: Maven Central on its own release line (D9; was: with docx4j's release); tika-app and tika-server usage; a `tika-config.xml` example; notes for Spring AI and LangChain4j; what ImportXHTML adds (§3.6) | 1 day |
| 3 | Tika 4.x: its JSON configuration (parsers named by key; how a third-party parser registers its name is unchecked), the API changes since 3.x | 1 to 2 days, after phase 1 |

Not in scope: Elasticsearch's and OpenSearch's ingest-attachment plugins. They ship their own Tika inside the
plugin, so reaching them means a modified plugin, not a class-path drop (unverified in their source).

## 5. Decisions (D1 to D8 accepted as recommended, 2026-10-22; D1 revised 2026-10-31, §5a; D9 proposed and accepted 2026-10-31)

| # | Decision | Recommendation |
|---|---|---|
| D1 | Name and place | `docx4j-tika`, a module in this reactor, Apache-2.0. **Revised 2026-10-31** (Jason): `docx4j-tika`, its own repository on GitHub (`plutext/docx4j-tika`), Apache-2.0, groupId `org.docx4j`, depending on released docx4j; §5a |
| D2 | Tika versions | Compile against Tika 3.3.x, `provided`; Tika 4.x in phase 3 |
| D3 | Defaults | The specification's (Word's view), not Tika's, with `OfficeParserConfig` mapped where it is given; the point of the module is Word's text |
| D4 | Embedded content | Hand embedded resources to Tika's `EmbeddedDocumentExtractor`, as Tika's parsers do; the text itself keeps `embedded: false` |
| D5 | Pictures with an SVG | Hand on only the image Word draws, never to a text parser (REQ-514) |
| D6 | Metadata | Tika's OOXML keys, plus `docx4j:warning` and `docx4j:inspect:*` |
| D7 | HTML chunks | No LGPL dependency; ImportXHTML documented as an optional addition |
| D8 | Phase 0 | Capture the prototype on a new branch off `VERSION_17_3_4`, its tests marked integration, before phase 1 changes it |
| D9 | Versioning (proposed 2026-10-31; **accepted** 2026-10-31, Jason: "D9 yes", to the docx4j-tika session) | Its own version line from 1.0.0, not docx4j's; the docx4j version it builds against in one pom property and named in the README; a Tika change ships as a docx4j-tika release, a docx4j release as a bump of that property once the parity tests pass on it |

### 5a. D1 revised (31 October 2026): a standalone repository

Jason asked what the arguments for a standalone GitHub repository were (2026-10-31), and on reading them said
to revise D1. The arguments, as put, for the record:

- **Release cadence.** Tika moves on its own clock (3.3.x now, 4.x in phase 3, and hosts pin their own). As a
  module, a fix for a Tika change waits for the next docx4j release and carries docx4j's version number. On its
  own, docx4j-tika releases against a released docx4j whenever Tika needs it.
- **Dependency footprint.** Phase 0 pulls `tika-parsers-standard-package` in test scope, and phase 1 adds
  tika-app and tika-server as shipped: a very large transitive tree (POI, PDFBox, Tesseract bindings) with the
  version clashes §6 names on commons-io, commons-compress and slf4j. In the reactor that slows every
  `mvn install`, widens the dependency-check surface, and risks the version convergence of modules that have
  nothing to do with Tika.
- **Testing shape.** The parity tests are integration tests by nature, a matrix over Tika versions and host
  frameworks. That matrix belongs in a repository's own CI, not in the failsafe run that gates each docx4j
  release.
- **Audience and tracker.** Tika users look for a Tika parser and find a repository with its own README,
  releases and issues, and file Tika integration questions there. Inside docx4j it is one of twenty modules
  and its issues land among docx4j's.
- **Licence boundaries.** D7 keeps LGPL out. A separate repository can offer an optional ImportXHTML flavour as
  its own artifact or profile without touching docx4j's dependency policy, and Tika's NOTICE obligations stay
  out of docx4j's.
- **Precedent.** docx4j-mcp (its CR-004, placement decided 2026-09-17) and docx4j-ImportXHTML are separate
  repositories, and the portfolio registry already tracks dependencies across them. docx4j-tika is a consumer
  of the text model like the MCP server, not part of the oracle the ports mirror.
- **Contributors.** Tika-community contributors can be given rights on docx4j-tika without rights on docx4j.
- **Nothing is gained from the reactor.** docx4j-bundle excludes it, and its published pom depends on
  docx4j-core either way.

Against, weighed and accepted:

- **The text model API is still moving.** CR-046, CR-050 and CR-067 all changed `org.docx4j.model.text` since
  this CR was accepted. A reactor module fails at compile time in the session that changes the API; a separate
  repository finds out at its next build. Mitigation in §6 (API drift).
- **One more release to run**, with its own version line (D9), tag, signing and deployment.
- **Fixtures.** The branch already copies the phase 0 fixtures and the portfolio's probes, with a drift test
  against docx4j-core-tests' copies, so it is nearly self-contained now; phase 0b keeps the drift test and
  makes it conditional on a docx4j checkout beside the repository.

Consequences:

- Phase 0b (§4) moves the directory with its history: branch `docx4j-tika` holds f1cfe21f8 (phase 0) and
  40f0632b0 (CR-056 phase 3's `HostileLinksTest`), and outside the directory only the reactor pom's module
  line. The branch is deleted after the move, not merged; nothing of docx4j-tika ships in a docx4j release.
- Phase 2's "Maven Central with docx4j's release" becomes its own release line (D9).
- The first release builds against docx4j 17.3.5 (the text model with CR-067's fields and CR-050's
  inspection), so phase 1 waits for it, or builds against the SNAPSHOT and releases after it.
- This CR stays here as the record of the decision and of phase 0. The repository carries its own
  `docs/developer/change-requests/`, its first CR citing this one and taking phases 1 to 3; the registry
  (`docx4j-portfolio/tasks.yaml`) gets a repository key for it and the phase entries move under that key,
  with `docx4j/CR-055` closing on phase 0b. How the registry records this is the portfolio session's call.
- docx4j's hand-off list for a change to `org.docx4j.model.text` (the ports, docx4j-mcp) gains docx4j-tika.

## 6. Risks

- **Class path.** The prototype ran with tika-app's bundled libraries ahead of docx4j's on the class path,
  without a conflict on the probes. A host with other versions of commons-io, commons-compress, JAXB or
  slf4j may differ, so phase 0's test uses the Maven shape (tika-core and tika-parsers-standard-package),
  and phase 1 also tests tika-app and tika-server as shipped.
- **Registration rests on undocumented behaviour.** That a third-party parser overrides Tika's for the same
  type follows from `ServiceLoaderUtils.sortLoadedClasses` (Tika's own classes first) and `CompositeParser`
  keeping the last, neither of which Tika documents. A Tika release that reorders would silently restore
  `OOXMLParser`, so the integration test asserts `X-TIKA:Parsed-By`, and the README gives the explicit
  `tika-config.xml` route as well.
- **Memory and start-up.** docx4j builds a JAXB tree where Tika streams, and the first parse pays JAXB's
  start-up (0.7 s measured). Large documents and high-throughput indexing need measuring before anyone is told
  it is a drop-in.
- **Behaviour change for an index.** Text that Tika used to extract and the specification excludes (hidden
  text, headers, unselected branches) disappears from an existing index on re-indexing. That is the intent,
  and it needs saying in the README.
- **API drift** (since D1 was revised). Out of the reactor, a change to the text model is found at
  docx4j-tika's next build, not in the session that makes it. The repository's CI builds against docx4j's
  latest SNAPSHOT as well as the released version it names, so a break shows within a day; the docx4j session
  hands a text-model change off to docx4j-tika as it does to the ports; and a docx4j release is taken up only
  after `TikaParityIT` passes on it.
- **Tika's evolution.** Tika 4 removed the DOM parsers and moved to JSON configuration. The parser's
  contract (`Parser`, `XHTMLContentHandler`, `ParseContext`) is stable, but its registration may not be.

## 7. Sources

- docx4j-portfolio `docs/text_extraction_behaviour_model.md` §7.4, §7.8 to §7.10, §12a and Appendix A (commits
  950f40a, fb937a2, 096ffd4, 5a30fbb, f3c1db5); `docs/text_extraction_specification_wd.md` 0.31 (30489aa).
- The prototype: docx4j-portfolio session scratch `tikadocx4j/` (`Docx4jWordParser.java`, `TikaText.java`,
  `score.py`, `score2.py`, `RESULTS.md`), 2026-10-22.
- Tika 3.3.3 (tika-app jar): `org.apache.tika.utils.ServiceLoaderUtils`, `CompareUtils`,
  `parser.CompositeParser`, `parser.microsoft.ooxml.OOXMLParser`; Tika 4.1.0's
  `tika-config-default-single-file.json`.
- `OfficeParserConfig` Javadoc 3.3.2 and 4.0.0:
  https://tika.apache.org/3.3.2/api/org/apache/tika/parser/microsoft/OfficeParserConfig.html ,
  https://tika.apache.org/4.0.0/api/org/apache/tika/parser/microsoft/OfficeParserConfig.html
- This repository: `docx4j-core` `model/text/Texts.java`, `TextResult.java`, `TextBlock.java`, `AltChunks.java`,
  `JsoupHtml.java`, `BoundValues.java`; `openpackaging/io3/stores/ZipPartStore.java`.

## 8. Phase 0 record (2026-10-22, docx4j-25)

The module `docx4j-tika` on branch `docx4j-tika` off `VERSION_17_3_4` (fe77bea88), in the reactor after
`docx4j-markdown`. Built with `mvn -Dgpg.skip=true install -pl docx4j-tika` against docx4j-core as reinstalled
from the branch head; the integration test green, 11 of 11.

- **The parser** is the prototype as it stands (`Docx4jWordParser`, 220 lines with the header), plus
  `package-info` and a `module-info` (`provides org.apache.tika.parser.Parser`, beside the SPI file). Tika's
  jars are automatic modules (`org.apache.tika.core`, `org.apache.tika.parser.microsoft`), so failsafe runs
  the test on the class path (`useModulePath=false`), which is the host the CR targets anyway.
- **Dependencies** as D1, D2 and D7: docx4j-core; docx4j-JAXB-ReferenceImpl at runtime scope (the README
  says how to swap in MOXy); tika-core 3.3.3 `provided`, and `tika-parser-microsoft-module` 3.3.3 `provided`
  too, because `OfficeParserConfig` (§3.3) lives there, not in tika-core. A host with tika-core alone would
  fail on `context.get(OfficeParserConfig.class)` with `NoClassDefFoundError`: phase 1 guards that (a
  reflective lookup, or the mapping in a class loaded only when the module is present). Test scope:
  `tika-parsers-standard-package` 3.3.3 and logback. No jsoup, no ImportXHTML.
- **`TikaParityIT`** (failsafe, runs at `verify` and `install`, not at `test`): `AutoDetectParser` with a
  `BodyContentHandler(-1)` over 29 files, the five phase 0 fixtures and the portfolio's 24 probes, copied
  into `src/test/resources/phase0` and `probes` (a test fails if the phase 0 copies drift from
  docx4j-core-tests'). Eleven tests: `X-TIKA:Parsed-By` names the docx4j parser for every Word file and
  never `OOXMLParser`; an xlsx and a pptx (made by docx4j in a temp file) go to `OOXMLParser`; the phase 0
  tokens by `expected-tokens.tsv`'s `accepted` column (score.py's rule: the token with no word character
  either side), the rows an HTML reader changes counted when jsoup and ImportXHTML are both present; the
  eight `mc` probes show Word's branch and no Fallback; the binding probes show Word's value except where
  the table below says; HTML, XHTML and MHT chunks are not read without the reader (and read, nothing
  hidden, with it); Flat OPC and docx chunks as Word shows them; the RTF chunk reported, the text chunk
  read; the SVG picture's text and the PNG's OCR both absent.
- **The numbers**, against §2's table: phase 0 177 of 177 found, none extra; `mc` 8 of 8; altChunk and SVG
  as §2 says. Time 1.2 s over the 29 files, the first 1.0 s of it JAXB start-up (§2 measured 0.7 s under
  tika-app; this is the Maven shape, with logback at WARN).
- **The binding probes, measured** (the test's `DOCX4J_BIND_DEPARTS`; each a departure from Word the text
  model makes on purpose, or a known gap, never a silent one):

  | Probe | Word shows | The text model | Why |
  |---|---|---|---|
  | `bind-plain` B7 | `B7_XML_VALUE` | `B7_STALE_SURFACE` | the store item ID names no part; Word repairs it |
  | `bind-plain` B8 | `B10_XML_VALUE` | `B8_RICH_STALE_SURFACE` | a rich-text control bound to a plain value: REQ-509 reads the surface |
  | `bind-typed` | `DD2_SECOND_ITEM`, `CB3_XML_FREE_TEXT` | `DD2_FIRST_ITEM`, `CB3_FIRST_ITEM` | typed controls: REQ-509 reads the surface |
  | `bind-repeating-ns0` | items A, B, C | item A | Word expands the repeating section |
  | `bind-props`, `bind-repeating-zero`, `opendope-template`, the rest of `bind-plain` | | as Word | (`PR4_COVER_ABSTRACT` since fe77bea88) |

  The portfolio's `score2.py` had `B10_XML_VALUE` under `bind-plain`'s shown list with no B10 case in the
  probe: it is the B8 control's bound value (`/p:root/p:b10`), and the row above says so.
- **Not in phase 0** (phase 1): lists, images, text boxes placed after their anchor, embedded content handed
  to the extractor (D4, D5), the full metadata set (D6: only title, creator, modifier and revision now),
  `Docx4jTikaConfig`, errors beyond the two `TikaException` wraps, tika-app and tika-server as shipped.
- **Elsewhere:** CHANGELOG line under 17.3.4 on the branch; whether docx4j-tika ships with 17.3.4 or after is
  Jason's call. Release verification (`release-verification-docx4j.md`) gains a module to cover once it ships.
  **Overtaken 2026-10-31** by the revised D1 (§5a): docx4j-tika ships from its own repository on its own
  release line, never in a docx4j release; the branch (rebased onto `VERSION_17_3_5` at 67c44d66a on
  2026-10-23; phase 0 is f1cfe21f8, plus 40f0632b0) is the source of the move in phase 0b.

## 8b. Phase 0b record (2026-10-31, the docx4j session)

Jason created `plutext/docx4j-tika` on GitHub (an initial commit holding the LICENSE) and cloned it beside this
repository as `../docx4j-tika`; then: "please do CR-055 phase 0b with it as the target".

- **History moved.** `git subtree split -P docx4j-tika docx4j-tika` gave the two commits as rooted at the
  directory (cd3e264 = f1cfe21f8's content, bce3a42 = 40f0632b0's); merged onto the initial commit
  (`--allow-unrelated-histories`, 861e05c there). Trees compared equal, then branch `docx4j-tika` (and the
  split branch) deleted here, as §5a says: not merged, nothing of it in any docx4j release, the reactor
  pom's module line gone with it. The originals stay in this clone's reflog for a while.
- **The repository** (its commit 4e65fd3, unpushed; Jason pushes): parent-less pom `org.docx4j:docx4j-tika`
  1.0.0-SNAPSHOT (D9, accepted later the same day; recorded in its CR-001 and pom, 01e8b7b there), release 11, docx4j-core and docx4j-JAXB-ReferenceImpl
  at one property `version.docx4j` = 17.3.5-SNAPSHOT until 17.3.5 is on Central, Tika 3.3.3 `provided`,
  surefire and failsafe 3.5.4 on the class path, JUnit and logback declared (they came from this parent), a
  `release` profile (sources, javadoc, gpg). NOTICE, `.gitignore`, CLAUDE.md, CHANGELOG, a README with its
  origin, version line and build; `docs/developer/change-requests/CR-001-the-parser-from-docx4j-cr-055.md`
  (Status: ACTIVE; phase 0b DONE; phases 1 to 3 as §4 here states them; D1 to D9 cited, not restated).
  GitHub Actions (`build.yml`): on push, pull request and nightly, check out this repository's
  `VERSION_17_3_5`, install docx4j-core and the RI selector from source, `mvn verify` against that SNAPSHOT
  (§6, API drift); a second job against the docx4j the pom names, manual until that is a release.
  `TikaParityIT.copiesMatchTheOriginals` compares with `../docx4j` when it is there.
- **The drift the first build found**, nine days after phase 0, both items docx4j's movement and not the
  parser's: (1) `extraction-probe-inspect.docx` and `expected-tokens.tsv` had changed in docx4j-core-tests
  (CR-066 phase 4's `value.out_of_range` rows RNG75 to RNG82): recopied, token baseline 188 (191 with the HTML
  reader), was 177 (180); (2) `bind-plain`'s B7 (a `w:storeItemID` naming no part) is Word's value now, since
  CR-058 resolves it as Word repairs it, so §8's departures table loses its B7 row (B8, the typed controls and
  the repeating section remain). Then green against the 17.3.5-SNAPSHOT core installed 2026-10-30:
  `HostileLinksTest` 2 of 2, `TikaParityIT` 11 of 11, 188 of 188 tokens and none extra, `mc` 8 of 8,
  `X-TIKA:Parsed-By` the docx4j parser for every Word file; the jar holds `module-info.class` and the SPI file.
- **Registry.** `docx4j/CR-055.0b` done; this CR closes on it, with phases 1 to 3 under the new repository's
  key once the portfolio creates it (its CR-001's Status line is the one to baseline).
- **Release verification** here loses nothing: docx4j-tika was never a module of a docx4j release.

