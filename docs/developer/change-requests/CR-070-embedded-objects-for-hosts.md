# CR-070: embedded objects in the text model, for a host that reads them itself

Status: PROPOSED 2026-10-31, drafted by Claude Opus 5.5 (session docx4j-12) at the docx4j-tika session's request
(docx4j-tika-45, the same day), which follows from Jason's decision on embedding in docx4j-tika (CR-055 D3 and D4
revised, CR-055 §5b): whether embedded content is in the text or handed to the host follows the host's settings,
and by default the host's own Tika parsers read embedded xlsx, pptx and OLE-package text. Decisions D1 to D5 await
Jason (§5); nothing is built. Target: 17.3.5, if Jason wants it there (D5).

## 0. Summary

Three small additions to the text model (CR-046), docx4j API only, so that a host such as docx4j-tika can put an
embedded object's text where the object stands, read it with its own parsers, or hand it on, object by object:

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
  (docx4j-tika-45, 31 October 2026). Not part of the WD's block model, so not in its JSON and nothing for the ports
  (D2).
- **(b) `EmbeddedFilter`** (`org.docx4j.model.text`, a functional interface): `boolean read(TextObject object, String
  address)`, called once per object the text model would read under `embedded: true`, before it is queued; `false`
  and the object is not read, its run kept. Default: none (read every one, as now). A filter that throws is a
  failure of the reading, as the image describer's is.
- **(c)** `altchunk.not_expanded` with the chunk's part name; the message and the other fields unchanged.

## 4. Phases

| Phase | What | Size |
|---|---|---|
| 1 | (a), (b), (c); tests in docx4j-core-tests (the extraction probe's object cases, a filter refusing one of two objects, a linked object, a control, the warning's part name); CHANGELOG; the hand-off to docx4j-tika | ½ day |

## 5. Decisions for Jason

| # | Decision | Recommendation |
|---|---|---|
| D1 | The object's description | A `TextObject` on the run (part, relationship id, ProgID, kind, content type; an OLE 1.0 package's label, file name and command), parallel to `TextImage`, rather than `getPart()` on `TextRun` alone: a linked object has no part, the kind and the id are what a host writes, and a package's names are what a host's filter matches |
| D2 | Specification | docx4j API only: not in the WD's block model or its JSON, nothing for the ports; the portfolio told, so that the WD can take it later if another host asks |
| D3 | The filter | `TextOptions.embeddedFilter(EmbeddedFilter)`, asked per object under `embedded: true`; an object not read keeps its run and `TextObject`; no filter reads every one (today's behaviour) |
| D4 | The warning | `altchunk.not_expanded` carries the chunk's part name (`TextWarning.getPartName()`) |
| D5 | Release | Into 17.3.5 (additive; docx4j-tika builds against 17.3.5) |

## 6. Sources

- docx4j-tika-45's request (31 October 2026) and docx4j-tika `docs/embedding-behaviour.md` (commit 3716f15).
- CR-055 §5b (D3 and D4 revised); CR-046 (the text model), REQ-512 (embedded), REQ-513 (altChunk), REQ-514 (the
  image describer).
