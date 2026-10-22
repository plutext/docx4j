# CR-055: docx4j-tika, a Tika parser for Word documents over the text model

Status: ACCEPTED 2026-10-22 (Jason: "CR-055: recommendations accepted for D1, D2, D3, D4, D5, D6, D7, D8. Commit it,
then do phase 0"); phase 0 to follow on a branch `docx4j-tika` off `VERSION_17_3_4`, by the docx4j-25 session (Claude
Fable 5.1). PROPOSED 2026-10-22, drafted by Claude Opus 5.5 (session docx4j-portfolio-9d) at Jason's request
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

This CR proposes a module, `docx4j-tika` (name D1), holding one Tika `Parser` for the four Word OOXML types.
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

`docx4j-tika` (D1) in this reactor, beside `docx4j-markdown`:
- one public class, `org.docx4j.tika.Docx4jWordParser implements org.apache.tika.parser.Parser`, and its
  service file;
- a small `Docx4jTikaConfig` that a caller may put in the `ParseContext` (§3.3);
- dependencies: docx4j-core, and docx4j-JAXB-ReferenceImpl at runtime, with the README saying how to swap it
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
| 0 | The prototype captured on a branch (D8): the module skeleton, the parser as it stands, the service file, and the comparison harness as an integration test (`TikaParityIT`) over the phase 0 fixtures and docx4j-portfolio's `mc-fun`, `bind-fun`, `altchunk-fun` and `svg-fun` probes, with §2's numbers as its baseline. The test runs against tika-core (`provided`) and tika-parsers-standard-package in test scope, the class path a Maven host has, not tika-app's fat jar, and asserts `X-TIKA:Parsed-By` for every Word file (§6) | ½ day |
| 1 | The production parser: §3.2's full mapping, §3.3's options, §3.4's embedded hand-off, §3.5's metadata, errors as `TikaException`. Tests: the phase 0 conformance suite through `AutoDetectParser` (every claimed row), and Tika's own Word test documents for crashes and metadata parity | 3 to 4 days |
| 2 | Packaging and documentation: Maven Central with docx4j's release; tika-app and tika-server usage; a `tika-config.xml` example; notes for Spring AI and LangChain4j; what ImportXHTML adds (§3.6) | 1 day |
| 3 | Tika 4.x: its JSON configuration (parsers named by key; how a third-party parser registers its name is unchecked), the API changes since 3.x | 1 to 2 days, after phase 1 |

Not in scope: Elasticsearch's and OpenSearch's ingest-attachment plugins. They ship their own Tika inside the
plugin, so reaching them means a modified plugin, not a class-path drop (unverified in their source).

## 5. Decisions (D1 to D8 accepted as recommended, 2026-10-22)

| # | Decision | Recommendation |
|---|---|---|
| D1 | Name and place | `docx4j-tika`, a module in this reactor, Apache-2.0 |
| D2 | Tika versions | Compile against Tika 3.3.x, `provided`; Tika 4.x in phase 3 |
| D3 | Defaults | The specification's (Word's view), not Tika's, with `OfficeParserConfig` mapped where it is given; the point of the module is Word's text |
| D4 | Embedded content | Hand embedded resources to Tika's `EmbeddedDocumentExtractor`, as Tika's parsers do; the text itself keeps `embedded: false` |
| D5 | Pictures with an SVG | Hand on only the image Word draws, never to a text parser (REQ-514) |
| D6 | Metadata | Tika's OOXML keys, plus `docx4j:warning` and `docx4j:inspect:*` |
| D7 | HTML chunks | No LGPL dependency; ImportXHTML documented as an optional addition |
| D8 | Phase 0 | Capture the prototype on a new branch off `VERSION_17_3_4`, its tests marked integration, before phase 1 changes it |

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
