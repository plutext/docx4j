# CR: List numbering model (`org.docx4j.model.listnumbering`) — line endings, correctness, and separating definitions from counter state

Status: IN PROGRESS (2026-09-12) — phases 0 (LF, 55c5475e1) and 0b (probes P1-P8, goldens in, table settled) done; next 1 and 3, then 2, 4, 5
Scope: the package `docx4j-core/src/main/java/org/docx4j/model/listnumbering`
(15 files, ~2,200 lines), its driver `NumberingDefinitionsPart` (maps,
`getEmulator`, `restart`), and the tests under
`docx4j-core-tests/src/test/java/org/docx4j/model/listnumbering` and
`.../org/docx4j/listnumbering/ind`.  Consumers (FO/HTML exporters, TOC
generator, markdown exporter) are touched only to adopt new API where a phase
says so.
Phases: 0. Unix line endings (preliminary, no code change); 0b. the
`numbering-model` verification probe set to the share (Word run by Jason;
confirms the code comments the design relies on); 1. fail-soft
formatting + format coverage; 2. `w:lvlRestart`; 3. de-duplicate `Emulator`
resolution; 4. separate definitions from counter state, one state per story
(closes the 2011 TODO in `Emulator`); 5. API hygiene
(naming, `ResultTriple`, `NumberFormat` clash) behind deprecation
Related: CR-001 (Word layout fidelity, Enterprise repo) drives most recent
changes to this package (b2-batch14 level overrides, b2-batch24 style-linked
levels, b2-batch26 level indents, isLgl); batch 40 found the list-label
ascent rule in `WordLineLayoutManager`, which consumes `ResultTriple.getRPr()`.

## Background

The package is a 2008 translation of a portion of `Program.cs` from
Microsoft's OpenXmlViewer (Ms-PL, licence text carried in each translated
file).  Its shape mirrors the schema:

- `AbstractListNumberingDefinition` — one `w:abstractNum`; a `HashMap<String,
  ListLevel>` keyed by `w:ilvl` as a string; remembers `w:numStyleLink`.
- `ListNumberingDefinition` — one `w:num`; copies the abstract levels (sharing
  their `Counter`), then applies `w:lvlOverride` (`w:startOverride`, and a
  full `w:lvl` via `ListLevel.SetOverrides`).  Holds the counter walk
  (`IncrementCounter`: increment un-encountered shallower levels, increment
  this one, reset all deeper ones) and the `w:lvlText` expansion
  (`GetCurrentNumberString`, including `w:isLgl`).
- `ListLevel` — one `w:lvl` (abstract and, optionally, override); start value,
  level text, hAnsi font, `NumberFormat`, and the mutable `Counter`.
- `Emulator` — static `getNumber(pkg, pPr)` / `getInd(...)`: resolves numId and
  ilvl (direct `w:numPr`, else via the paragraph style's effective pPr), checks
  `styleLinkedElsewhere`, increments the counter, and returns a `ResultTriple`
  (number string, font, bullet text, indent, rPr, lvl).
- `NumberFormatter` + `NumberFormat` and nine subclasses — integer to label
  for 13 of the 60 `ST_NumberFormat` values.

The counters live inside `ListLevel` objects which hang off the maps owned by
`NumberingDefinitionsPart`.  `getEmulator(true)` "resets" by rebuilding all
maps from the JAXB tree; `restart(numId, ilvl, val)` appends a new `w:num`
with a `w:startOverride` to the JAXB tree and to the maps.

Consumers (main code, outside the package): `XsltFOFunctions` (numberFor,
numberingIndent, levelRPr, symbolLabelFallback), `XsltHTMLFunctions`,
`ListsToContentControls`, `TocGenerator` / `TocEntry`,
`FormattingSwitchHelper`, `SwitchProcessor`, `WmlToMarkdown`, and
`NumberingDefinitionsPart` itself.

Tests: `NumberingSample1Test`, `StartOverrideTest`, `IsLglTest`,
`StyleLinkedLevelTest`, `IndentationTest`, `NumberFormatLowerLetterTest`
(all under `org.docx4j.model.listnumbering`), `ListNumberIndTest` (under
`org.docx4j.listnumbering.ind` — wrong package), and the base class
`AbstractNumberingTest` (excluded from surefire by name).

## Line endings (the preliminary)

Four of the fifteen files are CRLF throughout; the rest are LF:

| file | CRLF lines |
|---|---|
| `AbstractListNumberingDefinition.java` | 245 (all) |
| `ListLevel.java` | 409 (all) |
| `NumberFormatDecimalZero.java` | 24 (all) |
| `NumberFormatLowerLetter.java` | 25 (all) |

Every substantive edit to one of these files today produces a whole-file diff
if the editor normalises, or a mixed-ending file if it does not (both have
happened in the tree).  `.gitattributes` deliberately carries no `* text=auto`
(legacy CRLF files elsewhere would all be flagged), so the conversion is done
per file, as its own commit, before any code change in this CR — so that the
later, meaningful diffs are readable and `git blame` survives via
`--ignore-rev`.

## Gaps found (review of 2026-09-12)

Ordered by value.

1. **Counter state is per package, not per traversal.**  Two exports of the
   same `WordprocessingMLPackage` at once (HTML and PDF in parallel; two
   threads) interleave increments and mislabel both outputs.  `TocGenerator`
   resets by rebuilding every map.  There is no way to ask "what number would
   this paragraph get" without mutating.  CR-001 work (label measurement,
   keep-with-next of list items) wants exactly that side-effect-free query.
2. **Every story shares one set of counters.**  `Emulator`'s own header
   comment (a TODO dated 2011-02-23, repeated in `ListLevel`) already names
   this: numbering also occurs in headers/footers, comments, footnotes and
   endnotes, and each story needs its own counters.  Fifteen years on,
   nothing resets or partitions state per part: the only reset call in main
   code is `TocGenerator.numberParagraphs`.  So a numbered paragraph in a
   footer or footnote continues from wherever the main story's counter stood
   when that part happened to be converted, and the label depends on
   conversion order.  (Text boxes are a story in Word too, though they are
   inside the main part's XML; whether Word numbers them with the main story
   or separately is one of the probe questions below.)
3. **`w:lvlRestart` is not read.**  `IncrementCounter` always resets every
   deeper level; the schema default.  `w:lvlRestart w:val="0"` (never restart)
   and `w:val="n"` (restart only after level n-1) are used by legal and
   technical templates and are silently ignored.  Neither `w:legacy` nor
   `w:lvlPicBulletId` is read here either (`ListsToContentControls` warns on
   the latter itself).
4. **Formatting throws.**  `NumberFormatRomanAbstract.format` throws
   `NumberFormatException` above 3999 and below 1; `NumberFormatDecimalEnclosedCircle`
   above 20.  Nothing between `Emulator.getNumber` and the exporters catches it
   (checked: the two `NumberFormatException` catches in `XsltFOFunctions` are
   unrelated number parsing), so one long list aborts a PDF.  Word falls back
   to decimal.
5. **13 of 60 number formats handled; unknown ones log an error per label.**
   Missing formats seen in real documents: `ordinal`, `cardinalText`,
   `ordinalText`, `hex`, `chicago`, `numberInDash`, `decimalFullWidth`,
   `decimalHalfWidth`, `japaneseCounting`, `aiueo`, `iroha`, `hebrew1`,
   `hebrew2`, `arabicAlpha`, `arabicAbjad`, `thaiLetters`, `thaiNumbers`,
   `russianLower`/`russianUpper`, `upperLetter` (handled by upper-casing the
   lower-letter converter — fine).  `NumberFormatter` allocates a new converter
   per call; the converters are stateless.
6. **`Emulator.getNumber` and `Emulator.getInd` duplicate ~80 lines** of
   numId/ilvl-from-style resolution, and they differ: `getNumber` uses
   `getEffectivePPr(pStyleVal)`; `getInd` reads the raw style's pPr first and
   only follows `basedOn` if the style has a `w:numPr` without a `w:numId`.
   A style whose numbering comes entirely from `basedOn` therefore numbers
   but may indent differently.  (`Emulator` line 403 carries its own "TODO
   refactor.  Remove duplicated code.")  There is in fact a third path:
   `NumberingDefinitionsPart.getInd(NumPr)` (used by `StyleUtil` and the FO
   exporter), whose 17.1.0 javadoc fixes the order "level's own `w:ind`
   first, linked style's second" and carries a "TODO styles it is based on";
   `Emulator.getInd` (HTML only) has the opposite 2024 TODO ("indent in
   style should trump indent in style's numbering definition, attribute by
   attribute").  Three implementations, two open TODOs pulling opposite
   ways.
7. **`org.docx4j.model.listnumbering.NumberFormat` shadows
   `org.docx4j.wml.NumberFormat`**, which the same package imports; the local
   abstract class is reachable only by fully-qualified name in
   `NumberFormatter` and `ListLevel`.
8. **API hygiene.**  C#-style method names from the translation
   (`IncrementCounter`, `ResetCounter`, `SetOverrides`, `IsBullet`, `GetFont`,
   `GetCurrentNumberString`, `LevelExists`); `ResultTriple` is a non-static
   inner class (`em.new ResultTriple()`) with six fields; levels are keyed by
   `String` and parsed back to `int` inside loops; `Counter.getCurrentValue`
   logs by string concatenation on every read in the export hot path without
   an `isDebugEnabled` guard; no `package-info.java`; the deliberate sharing of
   one `Counter` between every `w:num` that references the same
   `w:abstractNum` (Word's behaviour: such lists continue one sequence unless
   a `w:startOverride` intervenes) is documented only by a one-line comment in
   the `ListLevel` copy constructor.

## What the code's own comments record (read 2026-09-12)

The package carries fifteen years of in-line decisions.  Those that bind the
design:

- **Per-part numbering was already the intended fix.**  `ListLevel`'s copy of
  the 2011 TODO adds "2012 01 10: numbering set up on a per-part basis seems
  the most sensible approach."  Phase 4 adopts exactly that.
- **One counter per abstract list, shared by every `w:num` that references
  it** (`ListLevel`: "The counter is kept at the abstract level, since each
  instance definition shares a single counter").  A `w:startOverride` is
  "only given effect the first time the instance is encountered in the
  document" and its start value is deferred "until the list is actually
  encountered in the main document part, since otherwise earlier numbering
  (using the same abstract number) would use this startValue"
  (`ListLevel.IncrementCounter`, `startAtUsed`).  So the first-encounter
  flags are state, not definition, and live in the per-story
  `NumberingState`.
- **A `w:numStyleLink` list is a separate list.**
  `NumberingDefinitionsPart.resolveLinkedAbstractNum`: "This list is treated
  as a separate list by Word (ie its numbers are incremented independently),
  and this code honours that."  Therefore the state key is the
  *referencing* `w:abstractNumId`, never the resolved target's.
- **The override level's `w:rPr` is deliberately not returned.**
  `Emulator.getNumber` (17.1.0): the exporters apply one rPr to label and
  text together, so an override rPr carrying `w:b` would bold the whole
  paragraph where Word bolds the number only; splitting label from text
  formatting "is left for a batch of its own" (CR-001).  Phase 4's merged
  `LevelDefinition` must not change what `ResultTriple.getRPr()` returns
  (the abstract level's rPr) until that split lands; it exposes the merged
  rPr under a new name (`getLabelRPr()`) for the exporter to adopt when it
  does.
- **Two "not numbered" outcomes are Word rules, not errors.**
  `Emulator.styleLinkedElsewhere` (17.1.0, measured): a level whose
  `w:lvl/w:pStyle` names a different style than the one that brought the
  numbering paints no label and does not count.  `NumberingDefinitionsPart.getInd`:
  "If there is a style reference in pPr, but not also one as a sibling of
  pPr, then no number appears at all!  TODO: throw ShouldNotBeNumbered??".
  The phase 3 resolver returns an explicit *not numbered* result for both
  rather than null-with-a-log, and `peek`/`next` honour it.
- **The default paragraph style is assumed unnumbered.**
  `Emulator.getNumber(pkg, pPr)`: "Assumes default p style isn't numbered!".
  The resolver falls back to the `w:default="1"` paragraph style when the
  paragraph names none (cheap; `PropertyResolver` already knows it).
- **`LevelExists` exists because of Word's referential-integrity bugs**
  ("in testing we've seen some referential integrity issues due to Word
  bugs"): a `w:num` may point at a missing `w:abstractNum` or level.  The
  new model stays tolerant: missing definition means not numbered, logged
  once per numId.
- **`ResultTriple` is an acknowledged misnomer** ("If we had our time again,
  wouldn't include 'Triple' in the name of this class").  Phase 5 renames it
  `NumberingResult` with `ResultTriple` kept as a deprecated subclass.
- **`NumberFormatter` already asks "What about? ordinal, cardinalText,
  ordinalText"** — phase 1's list.  `w:styleLink` (the inverse of
  `numStyleLink`) is ignored on purpose ("there is also
  abstractNum.getStyleLink(), but ignore that"); unchanged.

### Are the comments accurate?  Verification status

A comment records what someone believed; the CR relies only on claims that
are confirmed, either by reading what the code actually does (inspected
2026-09-12, cited by method) or by a Word golden (the layout-fidelity probe
corpus on the share, `$S/goldens-nofields`, 96 goldens).  Claims about Word
that no golden covers are collected into one probe set below, so Jason runs
Word once, before phase 2.

| # | claim (source) | code does (inspected) | Word evidence | status |
|---|---|---|---|---|
| 1 | Every `w:num` referencing one `w:abstractNum` continues a single sequence (`ListLevel` copy ctor) | `ListLevel(ListLevel)` assigns `this.counter = masterCopy.counter`; `AbstractListNumberingDefinition.readLevel` creates one `Counter` per abstract level | none in corpus; ECMA-376 17.9.15/17.9.16 imply it, the built-in "Continue numbering" UI relies on it | CONFIRMED (code + golden P1: 1 2 3 4 5 6 across the interleaved w:num) |
| 2 | `w:startOverride` applies once, on the first encounter of that `w:num` (`ListNumberingDefinition` ctor; `ListLevel.IncrementCounter`) | `setStartValue` clears `startAtUsed`; `IncrementCounter` resets the shared counter to the start value the first time and sets `encounteredAlready`; `StartOverrideTest` covers it with `startOverride.docx` (a Word document) | `StartOverrideTest` asserts the labels Word shows for that docx | CONFIRMED (code + test docx) |
| 3 | A `w:numStyleLink` abstractNum is a separate list with independent counters (`NumberingDefinitionsPart.resolveLinkedAbstractNum`) | `updateDefinitionFromLinkedStyle` calls `readListLevelsFromAbsNode(linkedNum)`, which builds fresh `ListLevel` objects (fresh `Counter`s) for the referencing abstractNum; the JAXB `w:lvl`s are copied into it. `StyleLinkedLevelTest` exercises the resolution but not counter independence | none | CONFIRMED (code + golden P2: Y 1 2 3, X 1 2 3, Y 4 5 6) |
| 4 | The override level's `w:rPr` is deliberately not returned; Word bolds the number only, not the text (`Emulator.getNumber`, 17.1.0, "measured") | `triple.lvl = listLevel.getJaxbAbstractLvl()`; `triple.rPr = triple.getLvl().getRPr()` — abstract level only | the 17.1.0 measurement was on a corpus document (not redistributable); no probe golden | HALF RIGHT (golden P3): Word does format the number only, not the text - but with the override level's rPr (labels Calibri Bold 18pt from w:b + w:sz 36; text 12pt regular), which docx4j never applies (12pt labels). The override REPLACES the abstract level's rPr: abstract w:i + override w:b gives bold, no italic run in the PDF. Phase 4's `labelRPr` |
| 5 | A level whose `w:lvl/w:pStyle` names a different style than the one that brought the numbering paints no label and is not counted (`Emulator.styleLinkedElsewhere`) | implemented 17.1.0; returns null before `IncrementCounter` | golden `numbering-label-ilvl0.pdf` on the share (probe in `Corpus.java` line 1803); b62-batch41 scores it parity 1.0000, 3/3 pages | CONFIRMED (golden) |
| 6 | "style reference in pPr but not as a sibling of pPr → no number appears at all; TODO ShouldNotBeNumbered" (`NumberingDefinitionsPart.getInd`) | the method returns null, which callers read as *no indent*, not *not numbered*; the numbering decision is made in `Emulator`, where #5 now covers the style-linked case | none for the pPr-vs-sibling wording; it predates #5 and reads as an earlier description of the same rule | SETTLED (golden P4): a level's `w:pPr/w:pStyle` numbers nothing by itself (LevelStyle paragraphs with no w:numPr paint no label, x = margin) and does not stop a direct w:numPr painting the label at the level's indent (label 99pt, text 126pt = 1080/540); docx4j = Word on both, so getInd's null is right and the TODO closes |
| 7 | Level's own `w:ind` before the linked style's (`NumberingDefinitionsPart.getIndFromLvl`, 17.1.0, "measured") | as documented: `lvl.getPPr().getInd()` first, then the style's | 17.1.0 measurement on a corpus document; `IndentationTest` / `ListNumberIndTest` cover the code paths against saved XML, not Word | CONFIRMED (the 17.1.0 measurement for attributes both state; golden P5 for disjoint ones: level w:hanging + style w:left combine, both ways round, label 126pt / text 144pt, docx4j = Word) |
| 8 | `Emulator.getInd` 2024 TODO: style indent should trump the level's, attribute by attribute | not implemented (a TODO) | contradicted by #7's measurement for the case measured | REJECTED by #7 for the precedence; golden P5 CONFIRMS the attribute-by-attribute half: the level's w:ind and the style's merge per attribute, the level winning where both state one (#7) |
| 9 | `getNumber(pkg, pPr)` assumes the default paragraph style is not numbered | `pStyleVal` stays null when `pPr.getPStyle()` is null; the style branch is never entered | none | CODE CONFIRMED (the assumption is real) and WRONG for Word: golden P6 numbers pStyle-less paragraphs 1 2 3 through the default style's w:numPr (the FO exporter already does, via the effective pPr; `getNumber(pkg, pPr)` does not). Phase 3's resolver falls back to the default paragraph style |
| 10 | Numbering in headers/footers/footnotes/comments needs per-story counters (2011 TODO; 2012 per-part note) | nothing partitions by part; the only reset is `TocGenerator` | none | CONFIRMED gap; golden P7 gives the story map: body 1 2 3 and 4 5 6 after the notes; header 1 2 3 and the same section's footer 4 5 6 (one counter for header and footer); the footnotes part one counter across notes (1 2 3, 4 5 6); the endnotes part its own (1 2 3); a text box its own (1 2 3) without touching the body's; the comment is not in the PDF. docx4j today runs one counter over everything in part order |
| 11 | `w:isLgl` applies at any level and decimalises inherited levels (`GetCurrentNumberString`, 17.1.0) | as documented | `IsLglTest` with `article-section-isLgl.docx` / `-NotIsLgl.docx` (Word documents) | CONFIRMED (code + test docx) |
| 12 | `LevelExists` guards Word referential-integrity bugs | returns false and logs when `levels` is null or the ilvl is absent | n/a (tolerance) | CODE CONFIRMED |
| 13 | `w:styleLink` is ignored on purpose | not read anywhere | n/a: it is the inverse pointer, informational | CODE CONFIRMED |
| 14 | (this CR) `w:lvlRestart` semantics per ECMA-376 17.9.11 | not read | golden P8 | SETTLED: w:val is the 1-based level whose use restarts this one and shallower ones do too (val 1 = ilvl 0 only: 1.2.3 after a level-1 item, 2.1.1 after a level-0 one); val 0 never restarts (1.2.3, 2.1.4); the design's rule holds. Also measured: a level reset but not yet used shows its w:start in deeper labels (Word 2.1.4, docx4j 2.0.1 today) |

**Probe set `numbering-model` (one docx per line, generated by the harness,
copied with `corpus.txt` to `$S/corpus/`; Jason runs Word; goldens back to
`$S/goldens-nofields`).  Each probe prints labels only, one-line paragraphs,
so the golden's text layer is the answer:**

- P1 `numbering-shared-abstract`: two `w:num` on one `w:abstractNum`, no
  overrides, three items each, interleaved A A B B A B.  Expect 1 2 3 4 5 6
  in document order if the sequence is shared.
- P2 `numbering-numstylelink-separate`: a numbering style; abstractNum X
  with `w:numStyleLink` to it; the style's own `w:num` (on abstractNum Y)
  used for three items, then a `w:num` on X for three items, then Y again.
  Expect Y: 1 2 3, X: 1 2 3, Y: 4 5 6 if X is a separate list.
- P3 `numbering-override-rpr`: abstract level 0 with no rPr; `w:num` with
  `w:lvlOverride/w:lvl` carrying `w:rPr/w:b`; three items of regular text.
  Expect bold label, regular text (PDF font names per glyph run).
- P4 `numbering-level-pstyle-in-ppr`: a level with `w:pPr/w:pStyle` only
  (no `w:lvl/w:pStyle`), used from a paragraph of another style.  Does the
  label appear?
- P5 `numbering-level-vs-style-indent`: level `w:ind w:hanging="360"` only,
  linked style `w:ind w:left="1440"` only; then the reverse.  Which
  attributes win, per attribute or as a block?
- P6 `numbering-default-style-numbered`: the `w:default="1"` paragraph
  style carries `w:numPr`; three paragraphs with no `w:pStyle`.  Are they
  numbered?
- P7 `numbering-stories`: the phase 4 probe (body, default header, default
  footer, two footnotes, an endnote, a comment, a text box; same `w:num`,
  three items each).  Which restart?
- P8 `numbering-lvlrestart`: three-level list; in list A level 2 carries
  `w:lvlRestart w:val="0"`, in list B `w:val="1"`; sequence 0 1 2 2 1 2 0 2.

**Probe status (2026-09-12):** all eight are generated by the harness
(`Corpus.java`, ids `numbering-shared-abstract`, `-numstylelink-separate`,
`-override-rpr`, `-level-pstyle-in-ppr`, `-level-vs-style-indent`,
`-default-style-numbered`, `-stories`, `-lvlrestart`) and are on the share
with `corpus.txt` (94 ids); the goldens landed the same day (fields off,
`$S/goldens-nofields`, with Word's resaves in `$S/resaved-nofields`).  `Doc` gained footnote/endnote/comment/text-box helpers that take real
paragraphs, and a numbering-style helper, for P7 and P2.  Two probes carry a
case beyond the list above: P3 also runs the same override over an abstract
level carrying `w:rPr/w:i` (bold italic = merged, bold only = replaced); P4
also has LevelStyle paragraphs with no `w:numPr` (does the level's
`w:pPr/w:pStyle` link the style to the list?) and a plain-level control.
P7's comment labels are visible only in Word's own window: Word does not
export markup to PDF, so that story is read by eye.

**Word's answers (goldens 2026-09-12)**, against what docx4j prints today
(harness `render`, 17.1.1-SNAPSHOT, before any of the phases):

- P1: Word 1 2 3 4 5 6. docx4j the same.
- P2: Word Y 1 2 3, X 1 2 3, Y 4 5 6. docx4j the same.
- P3: Word's labels are Calibri Bold 18pt in both lists (no italic run in
  the PDF), the text 12pt regular: the override level's rPr formats the
  label and replaces the abstract level's rPr. docx4j: 12pt labels.
- P4: Word paints 1 2 3 at the level's indent (label 99pt, text 126pt) for
  the direct w:numPr; nothing for LevelStyle paragraphs with no w:numPr
  (text at 72pt); control 1 2 3. docx4j the same, to the point.
- P5: Word label 126pt, text 144pt, continuation 144pt in BOTH
  arrangements: the style's w:left 1440 and the level's w:hanging 360
  combine per attribute, either way round. docx4j the same.
- P6: Word numbers the three pStyle-less paragraphs 1 2 3; the Unlinked
  control is not numbered. docx4j (FO) the same.
- P7: Word: header 1 2 3, footer 4 5 6; body 1 2 3, then 4 5 6 after the
  notes; footnote one 1 2 3, footnote two 4 5 6; endnote 1 2 3; text box
  1 2 3. docx4j: one counter over everything (below).
- P8: Word list A (val 0): 1. / 1.1. / 1.1.1. / 1.1.2. / 1.2. / 1.2.3. /
  2. / 2.1.4.; list B (val 1): ... / 1.2.3. / 2. / 2.1.1. docx4j (below)
  restarts level 2 after level 1 in both and prints 2.0.1.

docx4j today, in full:

- P1: 1 2 3 4 5 6 (the counter is the abstract list's).
- P2: Y 1 2 3, X 1 2 3, Y 4 5 6 (the `numStyleLink` list is separate).
- P3: "1. 2. 3." at the text's size in both lists: the override's `w:rPr` is
  not applied to the label (claim #4 as implemented).
- P4: direct `w:numPr` on the pStyle-in-pPr level paints 1 2 3 at the
  level's indent; LevelStyle paragraphs with no `w:numPr` paint nothing; the
  control paints 1 2 3.
- P5: labels painted in both lists; the positions are read off the PDF.
- P6: the three `w:pStyle`-less paragraphs are numbered 1 2 3 by the FO
  exporter (which resolves the effective pPr), the Unlinked control is not.
- P7: one counter across every story, in the order the exporter meets the
  parts (Word: the map above): header 1-3, footer 4-6, body 7-9, footnotes 10-15, text box 16-18,
  body after the notes 19-21, endnote 22-24; the comment is not traversed.
  (The inline `wps` text box is also drawn over the body lines that follow
  it - a rendering defect outside this CR, noted for CR-001.)
- P8: list A prints 1. / 1.1. / 1.1.1. / 1.1.2. / 1.2. / 1.2.1. / 2. / 2.0.1.
  and list B the same: `w:lvlRestart` is unread, level 2 restarts whenever
  level 1 is used, and a level-2 item straight after a level-0 one prints
  the untouched level-1 counter as 0.

Status is updated in the table as goldens land; a claim still "Word: probe"
when its phase starts is implemented per the code-confirmed behaviour and
flagged in the phase's commit message.

## Design

### Definitions immutable, state per traversal (phase 4)

    NumberingDefinitions            (built once from the part, no counters)
      AbstractListDefinition        w:abstractNum -> levels[0..8]
      ListDefinition                w:num -> abstract + overrides
      LevelDefinition               w:lvl merged (abstract, override): start,
                                    numFmt, lvlText, lvlRestart, isLgl, rPr,
                                    pPr/ind, suff, legacy, lvlPicBulletId
    NumberingState                  (one per STORY per traversal; counters
                                    and encountered flags keyed by the
                                    REFERENCING abstractNumId + ilvl, with
                                    per-numId start overrides applied on the
                                    first encounter of that numId in this
                                    story, exactly as today's startAtUsed /
                                    encounteredAlready)
      next(numId, ilvl) -> Label    increments, returns label + level
      peek(numId, ilvl) -> Label    no side effect
      reset()
    Label / NumberingResult         text, bullet, level, rPr (abstract level's,
                                    as today), labelRPr (new: the override
                                    level's rPr when the w:num overrides that
                                    level, else the abstract level's - a
                                    replacement, not a merge, per P3), ind,
                                    notNumbered (style-linked elsewhere etc.)
    NumberingStates                 (one per traversal: Map<Part, NumberingState>
                                    plus the main story; `forPart(part)` hands
                                    the exporter the right state as it enters
                                    a header, footer, footnote, endnote or
                                    comment part)

A story is a part: the main document, each header/footer part, the
footnotes part, the endnotes part, the comments part.  Measured by P7
(2026-09-12): the footnotes part is ONE story (footnote two continues
footnote one's count), the endnotes part another; a section's header and
footer share one counter (header 1 2 3, footer 4 5 6 - one section probed;
whether every section's headers and footers form one story or one per
section is unprobed, and `forPart` keys them together until a document
says otherwise); each text box is its own story and the body's count runs
past it untouched; comments were not measurable in the PDF.  So the key is
the part (headers and footers folded into one), never part + note id, and
text boxes get a fresh state each.

`Emulator.getNumber(pkg, pPr)` keeps its signature and semantics by using a
state object stored on `NumberingDefinitionsPart` (as today); the new
overload `getNumber(pkg, pPr, NumberingState)` is what exporters migrate to,
each traversal owning its `NumberingStates` and passing the state for the
part it is currently in (the FO and HTML conversion contexts already track
`getCurrentPart()`, see `FldSimpleWriter`'s header/footer test).  `getEmulator(true)` becomes "new state",
not "rebuild definitions".  `restart(...)` still edits the JAXB tree and adds
a `ListDefinition`; definitions are rebuilt only when the part's JAXB tree
changes (today's `initialiseMaps` triggers stay).

The `Counter` sharing rule is preserved: state is keyed by abstract list, and
a `w:num` with a `w:startOverride` for a level resets that abstract counter
the first time that `w:num` is encountered at that level (this is exactly what
`ListLevel.startAtUsed` / `Counter.encounteredAlready` implement today; the
refactor names it).

### Label formatting (phase 1)

`NumberFormatter` becomes a registry `EnumMap<org.docx4j.wml.NumberFormat,
LabelFormatter>` of stateless singletons.  `LabelFormatter.format(int)` never
throws: out-of-range falls back to decimal and logs once per format (a
`ConcurrentHashMap`-backed "warned" set), matching Word.  The abstract class
is renamed `LabelFormatter` (removing the `wml.NumberFormat` clash); the old
`NumberFormat` name stays as a deprecated empty subclass for one release.

New formatters, each a few lines plus a table test: `ordinal` (1st),
`cardinalText` (One), `ordinalText` (First), `hex`, `chicago` (* † ‡ §),
`numberInDash` (- 1 -), `decimalFullWidth`/`decimalHalfWidth`,
`upperLetter` (own class, not a `toUpperCase` of lower), `russianLower/Upper`,
`hebrew1`, `arabicAlpha`, `thaiLetters`.  Japanese/CJK counting styles are
added if a real document needs them; the two Chinese ones stay.

### `w:lvlRestart` (phase 2)

`LevelDefinition.lvlRestart` (nullable int).  ECMA-376 17.9.11: the value
is the 1-based number of the shallowest level whose use restarts this one;
omitted means any shallower level restarts it; `0` means it never restarts.
So in the counter walk, after incrementing level L (0-based ilvl), a deeper
level D is reset iff `D.lvlRestart == null || (D.lvlRestart > 0 && L <=
D.lvlRestart - 1)`.  Confirmed by P8 (2026-09-12): val 1 restarts level 2
after a level-0 item only (2.1.1) and not after a level-1 one (1.2.3); val
0 never (1.2.3, 2.1.4).  P8 also measured what a reset level shows before
it is next used: its `w:start` (Word 2.1.4 where docx4j prints 2.0.1), so a
reset puts the counter AT start with a first-use flag that suppresses the
next increment - the same shape as today's `startAtUsed` - not at zero.
`NumberingRestartTest` replays P8's walk against the golden's labels.

### `Emulator` de-duplication (phase 3)

One private `resolve(pkg, pStyleVal, numId, ilvl) -> NumRef {numId, ilvl,
direct, notNumbered}` using `getEffectivePPr` for both callers, falling back
to the default paragraph style, and applying `styleLinkedElsewhere`;
`Emulator.getInd` becomes a three-line wrapper.  Indent resolution collapses
onto `NumberingDefinitionsPart.getInd(NumPr)`'s 17.1.0 order (level's own
`w:ind`, then the linked style's, following `basedOn` — closing that TODO);
`Emulator.getInd`'s 2024 "style trumps level, attribute by attribute" TODO
is resolved the other way by the 17.1.0 measurement recorded in that
javadoc and is deleted.  `ListNumberIndTest` (moved into the right package) is
the regression net; its four `abstract_*`/`override_*` cases cover exactly
the style-vs-direct combinations that differ today.

## Plan

### Phase 0 — Unix line endings (preliminary; no code change)

Convert the four CRLF files to LF, one commit, nothing else in it:

    cd docx4j-core/src/main/java/org/docx4j/model/listnumbering
    sed -i 's/\r$//' AbstractListNumberingDefinition.java ListLevel.java \
        NumberFormatDecimalZero.java NumberFormatLowerLetter.java
    git diff --stat        # four files, every line, and nothing else
    git diff -w --stat     # must be empty apart from the header: whitespace only

Also check the test directory (`docx4j-core-tests/.../listnumbering` and
`.../listnumbering/ind`) and convert any CRLF `.java`/`.xml` there in the same
commit.  Add the commit hash to `.git-blame-ignore-revs` (create the file if
absent; document `git config blame.ignoreRevsFile .git-blame-ignore-revs` in
`docs/developer/README` or CONTRIBUTING).  Consider adding to `.gitattributes`:

    docx4j-core/src/main/java/org/docx4j/model/listnumbering/** text eol=lf

so the package cannot regress (the file's header says to add targeted rules
as needs arise; this is one).  Exit criterion: `grep -rl $'\r' <package>`
returns nothing; `mvn -o -q -Dgpg.skip=true install -pl docx4j-core
-DskipTests` unchanged.

### Phase 0b — verification probes (no code change)

Generate the eight `numbering-model` probes listed under "Are the comments
accurate?" via `Corpus.java` / `Fidelity generate`, copy them and
`corpus.txt` to `$S/corpus/`, wait for the Word run, copy goldens back,
and update the verification table's status column.  Phases 1 and 3 do not
depend on the answers; phases 2 and 4 do (P8; P1, P2, P7).

### Phase 1 — fail-soft formatting and format coverage

`LabelFormatter` registry as designed; fallback-to-decimal with one-time
warning; new formatters with a single table-driven test
(`LabelFormatterTest`: numFmt, value, expected) covering the boundaries
(Roman 1/3999/4000, circle 20/21, letter 26/27/702/703, ordinalText
teens/hundreds).  `NumberFormatter.getCurrentValueFormatted` keeps its
signature and delegates.  Ship with a CHANGELOG line.

### Phase 2 — `w:lvlRestart`

As designed.  Needs Word verification of the two-case test document (put it
on the fidelity share; Jason runs Word) — or rely on the spec wording plus
a quick manual check in Word; decide when the phase starts.

### Phase 3 — de-duplicate `Emulator` resolution

As designed; move `ListNumberIndTest` to `org.docx4j.model.listnumbering`.
Behaviour change to record in CHANGELOG: indents for paragraphs whose
numbering is inherited purely via `basedOn` now come from the same level the
number does.

### Phase 4 — separate definitions from counter state

The large one.  New classes beside the old; `Emulator`, `ListNumberingDefinition`
and `ListLevel` re-implemented over them with their public methods kept (and
deprecated where phase 5 renames them).  Exporters migrate to the
state-per-traversal overload: `XsltFOFunctions.numberFor`,
`XsltHTMLFunctions`, `TocGenerator.numberParagraphs` (drops
`getEmulator(true)`), `WmlToMarkdown`.  Each exporter obtains the state for
the part it is entering, so a footer's list starts at its own start value
regardless of when the footer is converted.

Probe first (fidelity share, Jason runs Word): one docx with the same `w:num`
used in the body, the default header, the default footer, two footnotes, an
endnote, a comment and a text box, three items each; plus a second `w:num`
on the same `w:abstractNum` in the body only.  Questions: does each
header/footer part restart; do the two footnotes share a sequence or each
restart; does the text box continue the body's sequence; does the second
`w:num` continue the first (expected yes, the shared-counter rule).  The
answers fix the story key and become a `NumberingStoriesTest`.

Gate: all existing tests, plus a new test that numbers the same package from
two threads and gets identical labels, plus a `peek` test, plus
`NumberingStoriesTest`.  Re-score the CR-001 corpora (all three, plus
probes) against the current baseline before pushing — labels are on the
critical path of every numbered paragraph's width and keep behaviour; the
expected delta is zero.

### Phase 5 — API hygiene

Java-style aliases with `@Deprecated` on the old names; `ResultTriple`
becomes a static nested class (source-compatible for `Emulator.ResultTriple`
users; the `em.new` construction is internal) and is renamed
`NumberingResult`, `ResultTriple` remaining as a deprecated subclass; `int` level keys internally;
guard the debug logging; `package-info.java` explaining the model, the
shared-counter rule, and the state-vs-definition split.  One release of
deprecation before removal (i.e. remove no earlier than 17.2).

## Decisions

- 2026-09-12 (Jason): write the CR; phase 0 (Unix line endings) is the
  explicit preliminary step, done before any code change.
- 2026-09-12: phase 0b probes P1-P8 cut, copied to the share, and the
  goldens read back the same day; every "Word: probe" row of the table is
  settled (see "Probe status").  Three facts the design did not have:
  the override level's rPr formats the label and replaces the abstract's
  (P3, phase 4); a section's header and footer share one counter and the
  footnotes part is one story (P7, phase 4); a reset level shows its start
  value until used (P8, phase 2).  docx4j already matches Word on P1, P2,
  P4, P5 and P6.
- 2026-09-12: phase 0 done (55c5475e1, whitespace-only, 14 files: the four
  main-source files, five test classes and the five `ind/*.xml` fixtures).
  Hash recorded in `.git-blame-ignore-revs`; `.gitattributes` pins the
  package and its test directories to LF.

## Risks

- **Phase 4 changes label sequencing if the shared-counter rule is
  mis-transcribed.**  Mitigation: keep `StartOverrideTest`,
  `NumberingSample1Test`, `StyleLinkedLevelTest` untouched as the oracle, and
  re-score the CR-001 corpora (zero expected delta) before pushing.
- **Per-story state changes labels in headers, footers and footnotes** for
  any document that has numbered paragraphs there (today they continue the
  body's count).  That is the fix, but it shows up as a corpus delta: check
  the WORSE lines of the re-score are all in those stories and match Word.
- **Phase 3 changes indents** for style-inherited numbering (the documented
  divergence between `getNumber` and `getInd`).  Recorded as a CHANGELOG
  behaviour change; FO test suite plus corpus re-score cover it.
- **Phase 1's fallback hides authoring errors** (a Roman list past 3999 is
  almost certainly a runaway counter).  The one-time warning names the
  numId/ilvl so it stays diagnosable.
- **Public API surface.**  Everything in the package is public and has been
  for 15 years; nothing is removed in this CR, only added and deprecated.
- **Ms-PL attribution** must stay on any file that still contains translated
  code; new files carry the standard Plutext header.  Phase 4's new classes
  are new code; the re-implemented old classes keep their headers.
