# CR-070: embedded objects in the text model, for a host that reads them itself

Status: ACCEPTED 2026-10-31 (Jason: "yes, revise D2 and send it to the portfolio, then CR-070 decisions as
recommended"): D1 to D5 as recommended, D2 revised first (Jason: "why not keep the spec consistent with what we are
adding to the API?"): the additions go into the WD (WD 0.47 REQ-512, REQ-522, REQ-513 and REQ-801, the portfolio's), and docx4j builds to it.
Phase 1 BUILT 2026-10-31 (section 7), to the WD 0.47 draft; complete but for the hand-offs' answers.
Target 17.3.5 (D5). Proposed the same day, drafted by Claude Opus 5.5 (session docx4j-12) at the docx4j-tika
session's request (docx4j-tika-45), which follows from Jason's decision on embedding in docx4j-tika (CR-055 D3 and D4
revised, CR-055 §5b): whether embedded content is in the text or handed to the host follows the host's settings,
and by default the host's own Tika parsers read embedded xlsx, pptx and OLE-package text.

## 0. Summary

Three small additions to the text model (CR-046) and to the WD with it (D2), so that a host such as docx4j-tika
can put an embedded object's text where the object stands, read it with its own parsers, or hand it on, object by
object:

- **(a)** an object run (`TextRun.Kind.OBJECT`) carries a `TextObject`: the object's part (null for a linked
  object), its relationship id, its ProgID and what it is (`EMBED`, `LINK`, `CONTROL`), as an image run carries a
  `TextImage` with its part;
- **(b)** `TextOptions.embeddedFilter(EmbeddedFilter)`: under `embedded: true`, the text model asks the filter, for
  each embedded object it would read, whether to read it (the part, the ProgID, the content type, the address); an
  object not read keeps its object run, with its `TextObject`, so that the host can read it or hand it on;
- **(c)** the `altchunk.not_expanded` warning carries the chunk's part name (`TextWarning.getPartName()`), so that a
  host can parse an unexpanded chunk (RTF; HTML without ImportXHTML) itself, at the warning's address.

## 1. Background

docx4j-tika (CR-055; its own repository since D1's revision) hands Tika what Tika's own Word parser would: text, and
embedded resources to the host's `EmbeddedDocumentExtractor`. Jason's decisions of 31 October 2026 (CR-055 §5b)
make embedding follow the host: a plain host (AutoDetectParser with BodyContentHandler: Spring AI, LangChain4j,
`tika --text`) gets embedded text in place; a recursive host (`/rmeta`, `-J`) gets records; an unpacker gets files.
By default the host's Tika parsers read embedded xlsx, pptx and OLE-package text; docx4j-tika's setting
(`docx4jReads`, working name) opts docx4j's own reading in per type; an embedded docx stays docx4j's. docx4j-tika's
table is its `docs/embedding-behaviour.md` (docx4j-tika commit 3716f15).

For that, docx4j-tika needs to know, at each object run, which part the object is (to write Tika's
`<div class="embedded" id="rIdN"/>` there, and to put a Tika-read object's text in place), and to stop the text
model reading the objects docx4j-tika gives to Tika, inside the one reading. Today an object run carries only its
ProgID, and `embedded: true` reads every embedded package it can.

## 2. What exists

- `Extractor.object(CTObject, ...)`: a `w:object` makes an object run with its ProgID (`objectRun`); under
  `embedded: true` (and not nested) its `EmbeddedPackagePart`, or an OLE1 package's `BinaryPart`, is queued as an
  `EmbeddedObject` read after the story (REQ-512). A `w:control` makes an object run with no ProgID.
- `TextImage` (an image run's): `getPart()`, the describer hook `TextOptions.imageDescriber(ImageDescriber)` (REQ-514)
  as the precedent for a host's callback.
- `TextWarning`'s five-argument constructor with `partName`, and `getPartName()`; `Extractor.altChunk` warns
  `altchunk.not_expanded` with the four-argument one.

## 3. Design

- **(a) `TextObject`** (`org.docx4j.model.text`), on `TextRun.getObject()` for an object run, null on any other:
  `getPart()` (the embedded part; the control's part for an ActiveX control; null for a linked object or where the
  relationship does not resolve), `getRelationshipId()`, `getProgId()` (as `TextRun.getProgId()`), `getKind()`
  (`EMBED`, `LINK` from `o:OLEObject/@Type`, `CONTROL` for `w:control` or an `o:OLEObject` standing for one),
  `getContentType()` (the part's); and for an OLE 1.0 package (ProgID `Package`, a `BinaryPart` holding an
  `Ole10Native` stream) `getPackageLabel()`, `getPackageFileName()` and `getPackageCommand()`, read from its native
  data as `Embedded.ole10Native` reads it (POI's `Ole10Native`), each null where absent and null for any other
  object. Those are the names Tika's own parser gives such a package when it hands it on (Tika 3.3.3
  `AbstractOOXMLExtractor` unwraps the `Ole10Native`: `resourceName` the label, `originalResourceName` the command
  and the file name), so that a host's `FilenameFilter` on `*.txt` meets `notes.txt`, not `oleObject1.bin`
  (docx4j-tika-45, 31 October 2026). In the WD's block model too (D2): REQ-512's object gains the same members.
- **(b) `EmbeddedFilter`** (`org.docx4j.model.text`, a functional interface): `boolean read(TextObject object, String
  address)`, called once per object the text model would read under `embedded: true`, before it is queued; `false`
  and the object is not read, its run kept. Default: none (read every one, as now). A filter that fails is taken as
  declining: the object is not read, and `content.not_read` is raised with the object's part name (WD 0.47 REQ-522,
  as REQ-514 treats a failing describer: the reading goes on).
- **(c)** `altchunk.not_expanded` with the chunk's part name; the message and the other fields unchanged.

## 4. Phases

| Phase | What | Size |
|---|---|---|
| 0 | The WD (the portfolio's; drafted as WD 0.47, registry `portfolio/wd-embedded-objects`, ee8b2d5; D2): REQ-512's object in the block model gains `kind`, `relationshipId`, `part`, `contentType` and an OLE 1.0 package's `label`, `fileName`, `command`; REQ-522 (§5, after REQ-514), the host's per-object filter under `embedded: true` (asked once per object before the reading with its description and address; a decline keeps the block; a failing filter declines and raises `content.not_read` with the part name; no filter reads every one); REQ-513 and REQ-801: `altchunk.not_expanded`'s selector the chunk's part name | the portfolio's |
| 1 | (a), (b), (c) built to the WD text; tests in docx4j-core-tests (the extraction probe's object cases, a filter refusing one of two objects, a linked object, a control, the warning's part name; the block model's JSON); CHANGELOG; the hand-offs: docx4j-tika, and the ports (core-ts, python) for the block model's members and the filter, as their own entries | ½ day |

## 5. Decisions

D1 to D5 accepted as recommended, 2026-10-31 (Jason), D2 as revised the same day.

| # | Decision | Recommendation (accepted) |
|---|---|---|
| D1 | The object's description | A `TextObject` on the run (part, relationship id, ProgID, kind, content type; an OLE 1.0 package's label, file name and command), parallel to `TextImage`, rather than `getPart()` on `TextRun` alone: a linked object has no part, the kind and the id are what a host writes, and a package's names are what a host's filter matches |
| D2 | Specification | ~~docx4j API only: not in the WD's block model or its JSON, nothing for the ports~~. **Revised 2026-10-31** (Jason: "why not keep the spec consistent with what we are adding to the API?"): the WD takes all three, as it already specifies the object run in the block model (REQ-512) and a host's callback (REQ-514's `ImageDescriber`): REQ-512's object gains the `TextObject` members; a requirement beside REQ-514 for the per-object filter; `altchunk.not_expanded`'s selector the chunk's part name. docx4j builds to the WD text (phase 0, WD 0.46, the portfolio's); the ports follow as their own entries, docx4j their oracle |
| D3 | The filter | `TextOptions.embeddedFilter(EmbeddedFilter)`, asked per object under `embedded: true`; an object not read keeps its run and `TextObject`; no filter reads every one (today's behaviour) |
| D4 | The warning | `altchunk.not_expanded` carries the chunk's part name (`TextWarning.getPartName()`) |
| D5 | Release | Into 17.3.5 (additive; docx4j-tika builds against 17.3.5) |

## 6. Sources

- WD 0.44 REQ-512 (embedded objects: the block model's `{type: "object", progId, address}`), REQ-514 (the
  `ImageDescriber`, a host's callback), REQ-801 (warnings and their selectors).

- docx4j-tika-45's request (31 October 2026) and docx4j-tika `docs/embedding-behaviour.md` (commit 3716f15).
- CR-055 §5b (D3 and D4 revised); CR-046 (the text model), REQ-512 (embedded), REQ-513 (altChunk), REQ-514 (the
  image describer).

## 7. Phase 1, as built (31 October 2026)

Built to WD 0.47's text (the portfolio's draft of REQ-512, REQ-522, REQ-513 and REQ-801), Jason's word "do
CR-068 phase 3, then CR-070".

- **(a)** `TextObject` (new) on `TextRun.getObject()` for every object run, whatever `embedded` says:
  `getKind()` (`EMBED`, `LINK`, `CONTROL`), `getRelationshipId()`, `getProgId()`, `getPart()`,
  `getPartName()` (the WD's `part`), `getContentType()`, `getPackageLabel()`, `getPackageFileName()`,
  `getPackageCommand()`, and `getSource()` (the `w:object`, or a `w:control` standing alone), as
  `TextImage` has. Made in `Extractor.object` and `Extractor.control`; the package's names by
  `Embedded.ole10Names`, POI's `Ole10Native` as `Embedded.ole10Native` reads it.
- **(b)** `EmbeddedFilter` (new, functional: `boolean read(TextObject, String address)`) and
  `TextOptions.embeddedFilter(EmbeddedFilter)` / `getEmbeddedFilter()`, null by default; asked in
  `Extractor.admitted` before an `EmbeddedObject` is queued.
- **(c)** `Extractor.altChunk` warns with the chunk's part name; `TextWarning.getSelector()` is the
  branch, else the part name, else the content type.

Where the WD is silent, decided in building (each for the WD, sent to the portfolio):

1. **docx4j's block model is its object API.** docx4j writes no JSON of the block model; phase 1's
   "block model's JSON" test is the `TextObject` getters, whose names map to the WD's members (`kind`
   lower-cased, `part` = `getPartName()`, `label`/`fileName`/`command` = `getPackage*()`).
2. **An ActiveX `o:OLEObject`** (REQ-512's `control`) is one whose part is an ActiveX control's
   (`application/vnd.ms-office.activeX+xml`, or the binary `application/vnd.ms-office.activeX`). A
   `w:control` is always one. A control's ProgID is null for a `w:control` (its class is in its part), as
   `TextRun.getProgId()` already was.
3. **A linked object has no part** even where its relationship is internal, and is never read nor asked
   about.
4. **The filter's address** is REQ-519's object segment, `body/3/object:0`, the prefix of the blocks its
   content is read as. The index counts every object the extractor would read, declined or not, so a
   filter moves no other object's address (`EmbeddedObjectsTest.filterDeclinesOneOfTwo`).
5. **A failing filter** (a `RuntimeException`) raises `content.not_read` at the anchoring paragraph's
   address, as a package that cannot be read does, with the object's part name.
6. **The package's names** are Ole10Native's `label`, `fileName` and `command` (what Tika 3.3.3 reads),
   not POI's UTF-16 `label2`/`fileName2`/`command2`; empty is null; read when the run is made, so that
   the filter has them.
7. **A warning raised one level down** (inside an embedded package or a chunk) that names a part names
   the embedded package's or chunk's part, the one the host package has (`Extractor.nested`, which now
   takes it): REQ-801's rule for `content.not_read`, extended to a nested chunk's
   `altchunk.not_expanded` and a nested `image.not_described`. Until now a chart inside an embedded
   document that could not be read named the inner package's part, against REQ-801. A host given the
   inner name could not find it, or could find another part of the same name (Word names every HTML
   chunk `afchunk.htm`).
8. **The conformance rows move.** WD 0.47's history says no row moves, but `expected-findings.tsv`'s
   twelve `altchunk.not_expanded` rows were keyed by content type; they are now keyed by part name
   (`ExtractionFixtureChecks.findings`, GOLDENS.md). Case 36's row of count 2 under `html reader absent`
   held case 36's chunk and case 47's nested one, both `text/html`; they are now `/word/afchunk.htm` (1)
   and `/word/chunk47.docx` (1, scenario `default`, since the nested chunk is never expanded with a reader
   or without). Every row checks (`conformance.tsv`: all PASS). The ports' harnesses read the same table.
9. **The inspection counts** (`inspect: true`, REQ-903) take every package as read under `embedded: true`,
   whatever the filter says; documented on `embeddedFilter`. Section 9.3's membership could say "the
   packages REQ-512 reads and REQ-522's filter admits"; an inspection is the package's, not the host's,
   so this is left for the WD.

The portfolio folded points 1 to 7 and 9 into the WD 0.47 draft the same day (REQ-512, REQ-522, REQ-801,
REQ-905; the history row corrected, [PROBE] citing 1560ab379 for the table; registry fff933d); point 8 stays
docx4j's. The WD awaits Jason's commit.

Tests: `EmbeddedObjectsTest` (9: the probe's five objects described; an OLE 1.0 package's names, and none
for a binary that is not one; a linked object; a `w:control` in `w:pict` and an ActiveX `o:OLEObject`; no
filter and a null one read every object; a filter declining one of two in a paragraph, the runs and
addresses kept; a filter over the probe, one level; a failing filter; the chunk warning's part name, a
nested chunk's included). `TextsPhase2Test.altChunks` asserts the part names in the warnings.
`ExtractionConformanceTest` checks the moved rows.

