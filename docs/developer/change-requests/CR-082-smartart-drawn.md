# CR-082: SmartArt drawn - from its drawing cache (`dsp:drawing`), and by its own layout definition where there is none

Status: PROPOSED 2026-11-09. Drafted on Jason's request (the four CRs of 2026-11-09; see CR-080's Status). Not
started; D1 to D10 open (§8). Needs CR-080's phases 1 and 2 (geometry and drawing a shape). Read against docx4j
c0a66b610, Apache POI trunk ae62bb5116 and docxide-pdf 9508da2, and the specifications in §12. Revised 2026-11-09
(Jason: "add a rendering phase to the CRs where appropriate: WordArt or SmartArt we can't output is a gap"):
- phase 4 draws a diagram that has no cache by running its own layout definition (D1, D8);
- phase 5 gives docx4j's own SmartArt a cache (D10);
- phase 6 reports every diagram still not drawn as a gap (D9).

Reviewed 2026-11-09 by a Claude Fable 5.1 session at Jason's request. Its findings were applied on Jason's word
("apply the review's findings"):
- ChristopherVR/ooxml's engine described as it is: one of three layout paths, trusted for an allow-list. Phase 4 now
  measures it against Word first, behind a go/no-go;
- the gallery files stay on the share;
- a stale-cache check (D4);
- the registry dependencies aligned.

Owner: Jason Harrop. Drafted with Claude Opus 5.5.

## 0. Summary

A SmartArt diagram in a docx comes out of docx4j's PDF as an empty paragraph. The fidelity register has the Enterprise
manual's case open (`open-leads.md`, 2026-10-29): a 432 by 100pt diagram drawn by Word over 110pt and by docx4j as one
25pt line, so everything below moves up 75pt and two headings change pages.

When Word saves a SmartArt it also saves the result of laying it out: the drawing part (`word/diagrams/drawingN.xml`,
`dsp:drawing`), a tree of ordinary DrawingML shapes, each with its geometry, fill, line, style, text and text
rectangle. This CR draws that tree with CR-080's shape drawing and its text as DrawingML text.

Some diagrams have no drawing part: docx4j's own `CreateDocxWithSmartArt` writes none, and other producers may not.
For these, phase 4 runs the diagram's own layout definition (`dgm:layoutDef`, which every SmartArt carries in its
layout part) over its data. That produces the tree Word would have cached, which is then drawn the same way. The
engine's source is ChristopherVR/ooxml (Apache-2.0), measured first against Word's own caches, which are its oracle,
before the port goes ahead (phase 4's go/no-go). The text list of D1 is the floor that ships whatever the outcome.

A diagram that still can't be drawn as Word draws it is reported as a gap with its reason (D9), never silently
dropped.

It also fixes a defect found while drafting: `DiagramDrawingPart` is declared to hold a `CTDataModel` but holds the
drawing's `CTDrawing`, so its typed `getJaxbElement()` throws (§1).

## 1. What docx4j does today

- **FO**: a `wp:inline` or `wp:anchor` whose `a:graphicData` has `uri` `.../drawingml/2006/diagram` writes an empty
  block of the paragraph's line height. Nothing of the diagram is drawn, and its extent isn't reserved.
- **The parts**: `DiagramDataPart`, `DiagramLayoutPart`, `DiagramStylePart`, `DiagramColorsPart` and
  `DiagramDrawingPart` exist (`org.docx4j.openpackaging.parts.DrawingML`). The drawing part's root unmarshals as
  `org.docx4j.dml.diagram2008.CTDrawing` (`dsp:drawing`, with `CTShape` and `CTGroupShape`: `spPr`, `style`, `txBody`,
  `txXfrm`, all DrawingML types). But the class is `JaxbDmlPart<CTDataModel>`, so
  `DiagramDrawingPart.getJaxbElement()` throws `ClassCastException: CTDrawing cannot be cast to CTDataModel`. Measured
  2026-11-09 with the 17.3.6 bundle on `text-extraction/extraction-probe.word-resave.docx` and
  `strict/strict-smartart.docx`; the untyped `JaxbXmlPart.getJaxbElement()` returns the `CTDrawing`.
- **Finding the drawing**: `InspectWalk` (CR-050) already follows the data part's `dsp:dataModelExt relId` to the
  drawing part, a relationship of the story part, for the text model.
- **Creating SmartArt**: `CreateDocxWithSmartArt` (and glox4j) writes the data, layout, style and colours parts and
  no drawing part. Word lays such a diagram out at open, and docx4j's PDF has nothing to draw from (D1).

## 2. What Word does

- Word re-lays a diagram out from its data and layout parts at open, and saves the drawing part as its result.
  docxide-pdf's case37 (its roadmap, 2026-11-08) shows the consequence: a hand-built SmartArt with an empty data model
  beside a full drawing cache is drawn by Word as five default blocks, not as the cache. So the cache is Word's drawing
  only for a file Word saved last (D4).
- A diagram's shapes carry `dsp:txXfrm`, the text's rectangle, separate from the shape's. For a rotated shape, PowerPoint
  and Word turn the text so that it reads upright (POI's `XSLFDiagram` reproduces the rule: about 25 lines).
- SmartArt text is DrawingML text (`a:txBody`: `a:bodyPr` with insets, anchor, `vert`, `rot` and `a:normAutofit
  fontScale lnSpcReduction`; `a:p`, `a:r`, bullets), not WordprocessingML.

- Word doesn't treat a missing `normAutofit` as "no autofit" ([MS-OI29500] 2.1.1381), and a `bodyPr` attribute
  that isn't given is inherited from the theme (2.1.1379). The stored `fontScale` is what Word applied. See §12.

Phase 0 measures, through the bridge, how Word's PDF draws a diagram and its text, and how its saved cache compares.

## 3. Sources evaluated

- **Apache POI `XSLFDiagram`** (273 lines, `@Beta`): turns a pptx's `dsp:drawing` into an `XSLFGroupShape` (each
  `dsp:sp` becomes a `p:sp`, `txXfrm` replacing the text anchor, with the text's rotation recomputed). Its drawing is
  POI's pptx renderer (CR-080 §3.1). **Port the `txXfrm` rotation rule only**, with the ASF header and a
  `legals/NOTICE` line.
- **docxide-pdf** (`src/docx/smartart.rs` 361 lines, `src/pdf/smartart.rs` 584): reads `dsp:sp` (geometry, fill,
  line, `txXfrm`, a text body with bullets) and draws with its geometry engine, the text in the preset's text rectangle.
  Its roadmap says what it doesn't do: `dsp:grpSp` groups, `dsp:cxnSp` connectors, a layout engine ("not planned"),
  anchored SmartArt wrapping other than `wrapNone` (laid out inline), and SmartArt pictures drawn through their own
  path. **Port nothing.** CR-080's engine and chart-render's text do the same, and docxide's handling of text is its own
  layout of its own model. Read it as a cross-check on which shape properties matter.
- **docx4j-chart-render**: `TextStyle` and `TextWrap` lay out and wrap DrawingML text as SVG text, measured against
  Office for chart text (CR-037). They are the obvious base for SmartArt text (D3).

Considered on Jason's second list (2026-11-09), each read in its code:

- **rust-office-art and rust-docx** (extend-hq/rust-office, MIT and Apache-2.0, Rust, d665c48; four days old at
  reading). These draw a saved SmartArt for docx and pptx through one shape pipeline. `docs/smartart-coverage.md`
  records a pass over 229 PowerPoint-authored gallery files covering 176 layout types, from ChristopherVR/ooxml
  (Apache-2.0), with their caches transplanted unchanged into Word packages. It reports every diagram drawn and the
  cached text exact. Pixel parity with Word is not claimed. Three of its findings apply here directly:
  - the drawing's relationship id is the **host part's**: a data-part image relationship of the same id once shadowed
    it;
  - extents must stay integral EMUs;
  - cached text is authoritative, with the data model's text used only when there is no cache at all.

  **Port nothing** (Rust). **Take the findings, and the gallery as a supplement to phase 0's Word probes** (it is
  PowerPoint's, not Word's).
- **office-open-xml-viewer** (yukiyokotani, MIT, `packages/pptx/parser/src/smartart_fallback.rs`, 905 lines). It
  replays the cache. With no cache, it draws the data model's `node` and `asst` points' text as a list indented by
  depth in the diagram's box, a deliberate degradation rather than a layout. **Take the degradation** (D1).
- **pptx-renderer** (aiden0z, Apache-2.0): a cache replay. `GroupRenderer.ts` special-cases particular layouts
  (`layout/cycle8`'s sectors and arrows). **No.**
- **pptx-typst** (Python): emits a stored cache as static SVG inside a Typst pipeline. **No.**
- **A full layout engine** (D1's follow-up, for diagrams with no cache):
  - **ChristopherVR/ooxml** (Apache-2.0, TypeScript, created 2026-10-01, active). The most complete found, but not a
    single general engine.
    - `src/core/diagram/engine/` (about 240 KB without its tests, 279 KB with them) has the `lin`, `snake`, `cycle`,
      `hier`, `pyra`, `composite` and connector algorithms, constraints and rules, text fit and font search, for docx
      and pptx.
    - Its own `engine-first-allowlist.ts` routes a layout to that engine only where the engine "is measurably at least
      as good as the legacy family-based interpreter (`smartart-layout-interpreter.ts`) on EVERY dataset fixture of
      that layout in the 229-fixture COM gallery corpus". Every other layout falls back to the legacy interpreter, and
      then to an algorithmic heuristic.
    - So the project has three layout paths, measured against PowerPoint's caches, not Word's. It is five weeks old,
      and AI-assisted (its repository carries `CLAUDE.md` and `AGENTS.md`). It also ships
    PowerPoint's built-in layout definitions, taken from PowerPoint-saved files (Microsoft's `dgm:layoutDef` content).
    A reader needs none of those, because every SmartArt carries its own layout part.
  - **aurochs** (trkbt10, MIT, TypeScript, last pushed 2026-05-11): `packages/@aurochs-office/diagram/src/layout-engine/`,
    about 8,700 lines with its tests, registering `lin`, `sp`, `hierChild`/`hierRoot`, `cycle`, `snake`, `pyra`,
    `composite`, `conn` and `tx`.
  - **LibreOffice** (MPL-2.0): `oox/source/drawingml/diagram/diagramlayoutatoms.cxx` (90 KB, every algorithm). A
    reference, not a source, because MPL-2.0 is file-level copyleft.

  None is measured against Word in its repository.

## 4. Corpus facts (2026-11-09)

The four fidelity corpora hold no SmartArt (`dgm:relIds` in 0 of 561 documents). The Enterprise manual has one
(above). docx4j-core-tests has six files with a drawing part. Phase 0 counts SmartArt with and without a drawing part
in the load survey's catalogue (its `parts` column, through the portfolio).

## 5. Approach

1. **Phase 0 fix**: `DiagramDrawingPart` becomes `JaxbDmlPart<CTDrawing>` (D2).
2. **Locate**: from `dgm:relIds r:dm`, the data part, its `dsp:dataModelExt relId`, then the drawing part, a relationship
   of the part that holds the drawing (body, header, footer, note), as `InspectWalk` already does, and then of the
   main document part. [MS-ODRAWXML] §2.1.3 names only the main document as a WordprocessingML source, so phase 0
   saves a SmartArt in a header and in a footnote to see which part Word relates it from. Never a relationship of the
   data part: rust-office found a data part's image relationship of the same id shadowing the
   drawing.
3. **Draw**: the diagram is one SVG of the inline's or anchor's extent. Its `dsp:spTree` is flattened as CR-080
   flattens a group (`dsp:grpSp` included), each `dsp:sp` drawn with CR-080's `ShapeRenderer` (geometry, fills, lines,
   style references resolved against the diagram's own `a:style` and the theme) and CR-081's effects where present.
   Picture fills (`a:blipFill` in a `dsp:spPr`) resolve through the drawing part's relationships.
4. **Text** (D3): each shape's `a:txBody` laid out in its `dsp:txXfrm` (or the geometry's text rectangle when absent),
   with `normAutofit`'s stored `fontScale` and `lnSpcReduction` applied as Word stored them (not recomputed), drawn as
   SVG text with chart-render's `TextWrap`, turned upright by POI's rule. Theme fonts (`+mn-lt`, `+mj-lt`) and the
   diagram's `a:fontRef` resolve through the theme; fonts map through `PhysicalFonts` as the rest of the PDF does.
5. **Into the FO**: the SVG goes into the place the inline or anchor already has (`WordLayoutFixups`, as a picture),
   so the 432 by 100pt diagram reserves its extent. That is a layout change, gated.

## 6. Phases

- **Phase 0, the fix and the oracle.**
  - `DiagramDrawingPart` typed `CTDrawing` (D2), with a test on the six core-tests files.
  - Probes: one SmartArt in a header and one in a footnote, to see which part Word relates the drawing from
    ([MS-ODRAWXML] §2.1.3 names only the main document); then each SmartArt category (list, process, cycle,
    hierarchy, relationship, matrix, pyramid, picture) in two
    layouts, with one-level and two-level text, rotated shapes (a cycle) and a picture layout; Word saves each and
    makes its PDF (bridge). The numbers come from `mutool trace` (paths) and text positions (`mutool stext`).
  - The load survey's count (via portfolio).
  - ChristopherVR/ooxml's gallery of 229 PowerPoint-authored diagrams (Apache-2.0), with its caches moved into docx
    packages as rust-office did, as a breadth test: every diagram draws, and its cached text is complete.
    - Each file carries Microsoft's own `dgm:layoutDef` in its layout part. So the transplanted gallery stays on the
      share, as the corpora do, and is not committed; rust-office drew the same line ("Installed template XML stays in
      local artifacts").
    - The repository keeps to small fixtures, as docx4j-core-tests' six SmartArt files do.
  - **Gate:** core-tests; numbers committed.
- **Phase 1, the shapes.**
  - Locate, flatten and draw every `dsp:sp` and `dsp:grpSp`, with no text yet, in FO (visitor and XSLT); the
    diagram's extent reserved.
  - **Gate:** probes' shapes within CR-080's tolerance; a fidelity gate against the then baseline (only documents with
    SmartArt should move); the Enterprise manual's p40 checked by page.
- **Phase 2, the text.**
  - `a:txBody` as SVG text in `txXfrm`, autofit as stored, bullets, upright rotation.
  - **Gate:** probes' text positions within tolerance (to be set from Word against itself).
- **Phase 3, the rest.**
  - `dsp:cxnSp` connectors (CR-080.3), anchored diagrams wrapping as pictures do, headers and footers, HTML output
    (CR-080 D5).
  - **Gate:** probes; corpora.
- **Phase 4, diagrams without a cache: the layout engine** (D1, D8).
  - **First, the measurement and the go/no-go.**
    - ChristopherVR/ooxml's three paths (the engine on its allow-list, the legacy interpreter and the heuristic) are
      run, as they are, against Word's caches for the gallery and phase 0's Word-saved probes, with each drawing part
      removed.
    - Listed: which layouts each path reproduces within tolerance.
    - From that list, Jason decides what is ported: the engine alone for the layouts it reproduces, the interpreter as
      well, or nothing.
    - The estimate is set then.
  - Then the port: the chosen paths ported to Java. It takes the file's own layout definition
    (layout nodes, `alg`, `choose`/`if`, `forEach`, `constrLst`, `ruleLst`, `presOf`, `shape`) with the data model,
    quick style and colours, and gives the `dsp:spTree` Word would have cached. Phases 1 to 3 draw it.
  - The oracle is Word's own caches. Each Word-saved probe and gallery diagram has its drawing part removed, and the
    engine's tree is compared with the cache Word wrote, shape by shape: preset, offset, extent, rotation, text and
    font size, within tolerance. Then Word's PDF of the same file, which Word lays out afresh at open.
  - A layout or algorithm the engine can't run falls back to D1's text list and is reported (D9).
  - **Gate:** the gallery's layouts reproduced within tolerance, each failure named with its algorithm; core-tests;
    corpora unmoved.
- **Phase 5, a cache for docx4j's own SmartArt** (D10).
  - `CreateDocxWithSmartArt` and glox4j write the drawing part from the engine, so that a reader that draws only the
    cache (LibreOffice, viewers, docx4j's own earlier versions) draws the diagram too.
  - **Gate:** Word opens each file silently (bridge), and the cache in Word's re-save matches docx4j's within
    phase 4's tolerance.
- **Phase 6, the gap report** (D9).
  - Every SmartArt in the probes, the gallery and a load-survey sample (through the portfolio) is rendered. Each one not
    drawn as its cache or as Word draws it is listed with its reason, for example: an algorithm not implemented, a
    3-D style drawn flat until CR-081's phase 5, a stale cache (D4), or a relationship that points nowhere.
  - **Gate:** no undrawn diagram among Word-saved files; the rest listed, with their counts, in the CR's record of the
    phase.
- **Phase 7, CHANGELOG** (with the `DiagramDrawingPart` API note), Getting Started, the register's lead closed.

## 7. Risks

- **A stale cache.** A file edited by a program that changes the data part but not the drawing draws the old diagram.
  Word draws the new one. The file doesn't say whether its cache is current, but a cheap check finds most stale ones
  (D4).
- **DrawingML text layout is not Word's paragraph layout.** Word lays SmartArt text out with the DrawingML text engine
  (the one PowerPoint uses), whose line breaking and autofit differ from Word's paragraphs. Chart-render's `TextWrap`
  was measured against chart text, which is the same engine. Phase 2's tolerance comes from Word against itself.
- **Fonts**: the text is drawn as SVG text through Batik. Where FOP can't resolve the font, Batik draws outlines
  (searchable text lost); this is the same behaviour `BatikMetafileSvgProvider` documents for metafiles.
- **The layout engine is large, partial in its source, and Word's is undocumented beyond §21.4.** The engine is about
  240 KB of TypeScript and trusted by its own project only for an allow-list. Reproducing the project's results needs
  its legacy interpreter and heuristic too, and its text fitting depends on font metrics, as Word's does. Phase 4
  measures before it ports. Word's caches are an exact oracle shape by shape, so how
  far the port is from Word is measured, not guessed. Layouts it can't reach are listed (phase 6), not hidden.

## 8. Decisions (owner: Jason; recommendations)

- **D1. The cache where there is one, the layout engine where there isn't** (phase 4), and the data model's text as
  a list only as a last resort. The list holds the `node` and `asst` points, indented by depth in the diagram's box, as
  office-open-xml-viewer and rust-office do. It is used for a layout or algorithm the engine can't yet run, and it is
  always reported (D9). Recommendation: yes. A SmartArt that docx4j can't output is a gap (Jason, 2026-11-09), and the
  earlier draft left the layout engine to a later CR.
- **D2. `DiagramDrawingPart` typed `CTDrawing`.** It changes a public class's generic type. The typed getter throws
  today, so no caller can be using it successfully. Recommendation: yes, in phase 0, with a CHANGELOG API note.
- **D3. SmartArt text as SVG text** (chart-render's `TextWrap` and `TextStyle`), not FO. It is DrawingML text, as a
  chart's is; SVG turns it at any angle, and FO only by 90 degrees. Recommendation: yes.
- **D4. A stale cache is drawn as it is, and reported when a check finds it stale.** The check:
  - compares the data part's `dgm:pt` model ids, and their text, with the drawing's `dsp:sp` `modelId`s and `a:t`
    text;
  - a mismatch reports `render.smartart.stale_cache`.

  Where the layout engine reproduces that layout (phase 4), a stale cache is laid out afresh instead, as Word does at
  open. Optionally, `docx4j.convert.out.smartart.cache=ignore` reserves the extent and draws nothing, for callers who
  want that. Recommendation: draw it and report it, with the property, off by default.
- **D5. Where the code lives**: with CR-080's drawing (docx4j-chart-render). Recommendation: yes.
- **D6. pptx**: pptx4j's SVG export could draw `p:graphicFrame` SmartArt from the same cache. Recommendation: not in
  this CR. Note it in §10.
- **D7. The text model** keeps reading SmartArt's text from the data part (CR-046/CR-050), as Word's text does, not
  from the cache. Recommendation: yes, unchanged.
- **D8. The layout engine: measure ChristopherVR/ooxml's three paths against Word's caches, then port what reproduces
  Word** (Apache-2.0, TypeScript). It is the most complete found (§3), but its own project trusts its general engine
  only for an allow-list of layouts, so the port's scope is decided from phase 4's measurement, not assumed. Its `NOTICE` is reproduced in `legals/NOTICE`, as Apache-2.0 §4(d) requires, and each ported file names
  its source and commit. Its built-in layout library is not ported: that is PowerPoint's own `dgm:layoutDef` content,
  and a reader needs none because every file carries its own layout part. aurochs (MIT) and LibreOffice (MPL-2.0,
  reference only) are cross-checks. Recommendation: yes, the measurement first, behind its go/no-go.
- **D9. Gaps are reported, not silent.** Each drawing not drawn as Word draws it is reported through the log, with a
  stable code (`render.smartart.no_engine`, `render.smartart.flat_3d`, `render.smartart.stale_cache` and so on), once
  per diagram, through CR-080 D9's two mechanisms: the message writer and the gate's event lines. A caller and phase
  6's gate can count them. The same mechanism serves CR-083's WordArt and CR-081's 3-D. Recommendation: yes.
- **D10. A cache for SmartArt docx4j creates** (phase 5). Recommendation: yes. It costs little once the engine exists,
  and it makes docx4j's SmartArt readable by consumers that only replay caches.

## 9. Out of scope

Editing a diagram's data through an API, or regenerating the cache of a file whose cache exists (D4); pptx and xlsx
output (D6). SmartArt with 3-D styles is drawn flat until CR-081's phase 5 draws 3-D, and is reported as a gap
meanwhile (D9).

## 10. Hand-offs

- The portfolio: the registry entry (depends on CR-080.1 and CR-080.2; phase 3 on CR-080.3 and CR-080.5 for HTML; the
  3-D styles on CR-081.5); the register lead
  ("A SmartArt (dgm) inline drawing is not drawn") to point at this CR.
- `legals/NOTICE`: ChristopherVR/ooxml's `NOTICE` text, for Jason's review before the first ported file is
  committed (D8).
- docx4j-generated-objects-ts and the Python port: the `DiagramDrawingPart` typing (D2), as a note, if they mirror the
  part classes.
- pptx4j (D6): a note for later.

## 11. Effort (rough)

Phase 0 two days; phase 1 two days with the gate; phase 2 two days; phase 3 two days. Phase 4: the measurement about a
week, and the port estimated from it, behind its go/no-go (the earlier guess of three to four weeks is not safe).
Phase 5 two days; phase 6 two days; phase 7 half a day. About two and a half weeks before phase 4's port, after
CR-080's phases 1 and 2. Phases 0 to 3 are a usable first release: every SmartArt Word saved is drawn, and every
other one shows its text as a list and is reported.

## 12. Specifications

Editions read (2026-11-09):
- ECMA-376 5th edition, aligned with ISO/IEC 29500:2016: Part 1 (December 2016), Part 2 (December 2021), Part 3
  (December 2015) and Part 4 (December 2016). Local copies: `~/git/safe-docx/spec-compliance/ecma-376/source-artifacts/`,
  with their `SHA256SUMS`.
- [MS-ODRAWXML] 35.0 (2026-10-06) and [MS-OI29500] 25.0 (2026-08-18), read on learn.microsoft.com (`office_standards`)
  on 2026-11-09.
- [MS-DOCX] 23.0 (2026-08-18), local copy `~/Downloads/[MS-DOCX]-260818.docx`.
- Not used: [MS-ODRAW], the binary OfficeArt format of .doc and .ppt, which docx doesn't carry.

**ISO/IEC 29500-2 (ECMA-376 Part 2).** §6.5 relationships, §6.5.4.2 a part's relationships part (the locate step).

**ISO/IEC 29500-1 (ECMA-376 Part 1)**
- **The parts** (§14.2): §14.2.3 Diagram Colors, §14.2.4 Diagram Data, §14.2.5 Diagram Layout Definition, §14.2.6
  Diagram Style; §11.3.10 Main Document.
- **Diagrams** (§21.4): §21.4.2.22 `relIds`, §21.4.2.10 `dataModel`, §21.4.3.6 `ptLst`, §21.4.3.5 `pt`, §21.4.3.8 `t`,
  §21.4.3.3 `cxnLst`, §21.4.3.2 `cxn`, §21.4.3.4 `prSet`, §21.4.3.7 `spPr`.
- **Layout definitions** (D1's follow-up): §21.4.2.16 `layoutDef`, §21.4.2.3 `alg`, §21.4.7.1 `ST_AlgorithmType`.
- **DrawingML text** (§21.1.2): §20.1.2.2.40 `txBody`, §21.1.2.1.1 `bodyPr`, §21.1.2.1.3 `normAutofit`,
  §21.1.2.4.12 `lstStyle`, §21.1.2.2.6 `p`, §21.1.2.2.7 `pPr`, §21.1.2.2.5 `lnSpc`, §21.1.2.2.10 `spcBef`,
  §21.1.2.3.8 `r`, §21.1.2.3.9 `rPr`, §21.1.2.3.7 `latin`.
- **Shapes**: as CR-080 §12.

**[MS-ODRAWXML]**
- **§2.1.3 Diagram Layout**, the drawing cache's part. Its content type is
  `application/vnd.ms-office.drawingml.diagramDrawing+xml`, its relationship type is
  `http://schemas.microsoft.com/office/2007/relationships/diagramDrawing`, and its root is `dsp:drawing`. It is "the last
  successful layout of a diagram". For WordprocessingML it MUST be the target of a relationship from the **Main
  Document** part (§11.3.10); no header, footer or notes part is listed. See §5's locate step.
- **§1.3.4 Diagrams:** the cache is referenced from the data model's `dsp:dataModelExt`. Its `minVer` names the
  minimum application version that can run the layout, and an application that can't run it shows the saved
  layout, which is what this CR does.
- §2.10 (`drawing/2008/diagram`): §2.10.1.2 `drawing`, §2.10.3.2 `CT_Drawing`, §2.10.1.1 `dataModelExt`,
  §2.10.3.1 `CT_DataModelExtBlock`, schema §5.6;
- §1.3.4, §2.2.4 and §3.4, Diagrams, with §2.2.4.1 and §3.4.1, Diagram Layout;
- §2.9 (`drawing/2010/diagram`);
- §2.28 and §2.32 (the 2016 diagram extensions, among them §2.28.3.1 `CT_DiagramAutoBullet` and §2.28.3.2
  `CT_NumberDiagramInfo`, which bear on bullets and numbering in phase 2's text).

**[MS-OI29500]: Office's notes on these sections**
- **2.1.1379 (§21.1.2.1.1 `bodyPr`):**
  - an attribute not given is inherited, in the order the theme's `txDef`, `lnDef`, `spDef`, then `bodyPr`'s
    default;
  - `anchorCtr`, `forceAA`, `fromWordArt` and `rtlCol` default to false;
  - Word doesn't evaluate `rot` for text rotation in builds before 15609.10000 (Windows).
- **2.1.1381 (§21.1.2.1.3 `normAutofit`):** Word and Excel don't treat an omitted `normAutofit` as no autofit, as the
  standard says. They decide from a hierarchy of text styles. Phase 2 applies the stored `fontScale` and
  `lnSpcReduction` and doesn't recompute.
- **2.1.1283 (§20.1.7.6 `xfrm`):** `flipH`, `flipV` and `rot` are ignored on a graphic frame, the SmartArt's frame
  included.
- **2.1.1217 (§20.1.2.2.37 `style`):** a shape's `spPr` and `txBody` override its style's references (CR-080 §12).
