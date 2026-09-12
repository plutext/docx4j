# CR-015: Property resolution (`PropertyResolver`, `StyleUtil.apply`) — line endings, one property catalogue, the default paragraph style, cache correctness, no mutation, thread safety

Status: IN PROGRESS (2026-09-12) — approved by Jason 2026-09-12; phase 0 DONE
(6f4763d70, LF; 21890c36e pins the line endings and blame-ignores the
conversion); phase 0b DONE (five `styles-*` probes, goldens in and read back
into the table the same day, 83c6865b8); phase 1 DONE (the property
catalogue and the merge rules; 21 corpus documents improved, none regressed);
phase 2 DONE (the resolution order; zero corpus delta bar one improvement);
phase 2b next
Scope: `docx4j-core/src/main/java/org/docx4j/model/PropertyResolver.java`
(1,660 lines) and `ImmutablePropertyResolver.java`; the merge half of
`org/docx4j/model/styles/StyleUtil.java` (the `apply`, `isEmpty` and `unset`
families, 3,501 lines in all) and `isCyclic`; `MainDocumentPart.getPropertyResolver`;
the tests under `docx4j-core-tests/src/test/java/org/docx4j/model/` (the four
`PropertyResolver*Test` classes and `PropertyResolverTestUtils`) and
`.../model/styles/`.  Out of scope: `StyleTree`/`Node`/`Tree` (the HTML CSS
tree, which does not use the resolver) except their line endings;
`ParagraphStylesInTableFix` (table styles stay in the preprocess, see
comment 14 below); `RunFontSelector` (the next review); consumers are touched
only to adopt new API where a phase says so.
Phases: 0. Unix line endings (preliminary, no code change); 0b. the `styles-*`
verification probe set to the share (Word run by Jason); 1. one property
catalogue per properties type, driving `apply`/`isEmpty`/`unset`/direct-formatting
detection, plus the merge rules that replace where they should merge; 2. the
resolution order: the default paragraph style for paragraphs that name none or
name a missing one, style-chain caches that do not depend on call order, the
paragraph mark as an explicit call; 3. no mutation of the document, no
aliasing of style leaves, thread safety; 4. the default table style, logging
and lookup cost, dead code; 5. API hygiene behind deprecation.
Related: CR-014 (list numbering; its `Emulator` resolves numbering through
`getEffectivePPr(styleId)` and its concurrency test deliberately avoided the
style path); CR-001 (Word layout fidelity, Enterprise repo) drove the 17.0.5
to 17.1.1 changes to `StyleUtil.apply` (tabs, indents, frames, autospacing,
numId 0, conditional table formatting) — all kept; #546 phase 1 (67ab3b831)
decided where table styles are applied; the fonts review that follows this
one consumes the effective rPr this CR makes correct.

## Background

`PropertyResolver` (2008, "works out the actual set of properties (paragraph
or run) which apply, following the order specified in ECMA-376") is created
lazily by `MainDocumentPart.getPropertyResolver()` and kept for the life of
the package.  It reads the styles part once (`liveStyles`, a `HashMap` by
styleId, with a 17.0.4 rescan on a miss), the document defaults, and offers:

- `getEffectivePPr(PPr direct)` / `getEffectivePPr(String styleId)` — the
  paragraph properties: document defaults, then the paragraph style's
  `w:basedOn` chain, then direct formatting.  Numbering level indents are
  folded in per layer by `StyleUtil.apply(PPrBase, PPrBase,
  NumberingDefinitionsPart)`.
- `getEffectiveRPr(RPr direct, PPr pPr)` — the run properties: document
  defaults, the paragraph style's run properties, the character style's
  chain, direct formatting.  Plus `getEffectiveRPr(String styleId)`,
  `getEffectiveRPr(String, boolean, boolean, boolean)` (8.2.4) and
  `getEffectiveRPr(RPr)` (11.5.12).
- `getEffectiveTableStyle(TblPr)` — the table style's chain merged into one
  `Style`, then the table's own `w:tblPr`.
- `activateStyle` (KnownStyles.xml), `getStyle`, `refresh()` (17.0.4),
  `getDocumentDefaultPPr/RPr`, `getResolvedDefaultParagraphStyle`.

The merging is `StyleUtil.apply(source, destination)` (Alberto Zerolo, 3.0.0):
one method per properties type and per leaf, "later overrides earlier",
attribute by attribute where a 17.1.0 measurement said Word does so (tabs,
`w:ind`, `w:framePr`, `w:spacing` autospacing).  `StyleUtil.isEmpty` gates
whether a source is applied at all; `StyleUtil.unset` (11.5.12) is its inverse;
`PropertyResolver.hasDirectPPrFormatting/hasDirectRPrFormatting` decide whether
a paragraph or run has direct formatting worth a copy.

Consumers (main code): both FO pathways (`FOExporterVisitorGenerator`,
`XsltFOFunctions`), the HTML pathway only for `getEffectiveTableStyle` and
`HiddenText`, the shared preprocess (`ParagraphStylesInTableFix`,
`Containerization`, `ConversionSectionWrapperFactory`,
`FopWorkaroundReplacePageBreakInEachList`, `AbstractTableWriterModel`),
`Emulator` (CR-014), `TocEntry`/`USwitch`/`SwitchProcessor`,
`BindingTraverserXSLT`/`BindingHyperlinkResolver`, `ListsToContentControls`,
`WmlToMarkdown`/`ImportStyles`, and user code.  Tests today:
`PropertyResolverEffectivePPrFromExpressTest`, `...FromStyleIdTest`,
`PropertyResolverFramePrTest`, `PropertyResolverLiveStylesTest`,
`AutospacingOverrideTest`, `StylesCyclicTest`/`StylesThrowingCyclicTest`,
`IndMergeTest`, `IndFirstLineHangingTest`, `StyleUtilTabsMergeTest`,
`PStyle12PtInTable*Test` (export-fo-tests).

## Line endings (the preliminary)

| file | CRLF lines |
|---|---|
| `model/styles/BrokenStyleRemediator.java` | 71 (all) |
| `model/styles/Node.java` | 141 (all) |
| `model/styles/Tree.java` | 126 (all) |
| `docx4j-core-tests/.../model/PropertyResolverEffectivePPrFromExpressTest.java` | 110 (all) |
| `docx4j-core-tests/.../model/PropertyResolverEffectivePPrFromStyleIdTest.java` | 57 (all) |
| `docx4j-core-tests/.../model/PropertyResolverTestUtils.java` | 90 (all) |

Found on conversion (2026-09-12): `ImmutablePropertyResolver.java` (196 lines)
and the test resource `styles-simple.xml` (96) were CRLF too; both went into
the same commit.  `PropertyResolver.java`, `StyleUtil.java` and
`StyleTree.java` are LF.  As in CR-014: one whitespace-only commit before any
code change (6f4763d70, eight files, `git diff -w --stat` empty), its hash in
`.git-blame-ignore-revs`, and `.gitattributes` rules for
`org/docx4j/model/*.java`, `model/styles/**`, the test directory and the
resource.

## Gaps found (review of 2026-09-12)

Ordered by value.  "Measured" here means a scratch test run on 2026-09-12
against the current tree (`ScratchResolverProbeTest`, not committed; phase 1
and 2 tests reproduce each case as an assertion).  Word's side is either the
spec order (ECMA-376 17.7.2, which the class's own header quotes) or one of
the probes below.

1. **Four hand-maintained property lists disagree, and a fifth exists.**
   `StyleUtil.apply(RPr, RPr)` carries 36 members; `isEmpty(RPr)` checks 35
   and omits `w:lang` and the 17.0.5 `w14:ligatures`; `unset(RPr)` mirrors
   `isEmpty`; `PropertyResolver.hasDirectRPrFormatting` — whose comment says
   "taken directly from RPr, and so is comprehensive" — tests 19 and has the
   other 21 commented out (`rtl`, `position`, `szCs`, `w`, `kern`, `cs`, `bCs`,
   `iCs`, `dstrike`, `outline`, `shadow`, `emboss`, `imprint`, `noProof`,
   `snapToGrid`, `webHidden`, `effect`, `fitText`, `em`, `eastAsianLayout`,
   `specVanish`, `oMath`); and `ImmutablePropertyResolver` (no caller in the
   repo) has a fifth copy.  The `ParaRPr` variants are pasted three more times.
   `PPrBase` likewise: `hasDirectPPrFormatting` tests 17 of 34 members (each
   17.0.5-17.1.0 fidelity batch added one more: `contextualSpacing`,
   `suppressAutoHyphens`, `framePr`).  Consequences, measured:
   - a run whose only direct formatting is `w:rtl`, or `w:position`, is
     resolved as having none: the effective rPr lacks it (with `w:b` beside it,
     it is kept).  `PropertyFactory` reads `getRtl()` off the effective rPr.
   - a paragraph whose only direct formatting is `w:mirrorIndents` or
     `w:textDirection` loses it the same way.
   - a character or paragraph style whose `w:rPr` states only `w:lang`
     (`bidi="he-IL"`) is "empty" and never applied.
   - a direct `<w:numPr><w:ilvl w:val="1"/></w:numPr>` is "empty" (`isEmpty(NumPr)`:
     "only the id is checked"), so the paragraph stays at the style's level 0.
     Were it not empty, `apply(NumPr)` would set `numId` to null instead.
2. **Runs in paragraphs that name no style do not get the default paragraph
   style's run properties, and a paragraph naming a missing style gets
   nothing.**  `getEffectivePPr(PPr)` falls back to the `w:default="1"`
   paragraph style; `getEffectiveRPr(RPr, PPr)` does not: with Normal saying
   14pt and the document defaults 11pt, a run in a paragraph without
   `w:pStyle` resolves to 11pt (with `w:pStyle="Normal"`, 14pt).  Word never
   writes `w:pStyle` for Normal, so this is the common case.  Both exporters
   are shielded by `ParagraphStylesInTableFix` (lines 872-877), which writes
   the default style's id onto every paragraph lacking one, in tables or not —
   the FO output for the scratch document is right, the resolver's answer is
   not; markdown, TOC, binding, `HiddenText` outside the exporters, and user
   code get the wrong one.  A `w:pStyle` naming a style that does not exist
   returns null (`getEffectivePPr`) or, with direct formatting, a pPr without
   even the document defaults, and runs get the defaults only; Word treats it
   as Normal (probe P1).
3. **The character-style cache depends on call order.**
   `getEffectiveRPr(styleId, applyDocDefaultsRFonts, applyDocDefaultsSz,
   applyDocDefaultsLang)` caches its result under the styleId alone, though
   the result depends on the three flags.  Measured: call the public
   `getEffectiveRPr("S")` first (all defaults in), then resolve a run with
   `w:rStyle="S"` in a paragraph whose style says Arial 20pt: Calibri 11pt,
   where Word gives Arial 20pt bold.  The other way round, `getEffectiveRPr("S")`
   returns an rPr with no `rFonts` and no `sz` at all.  `BindingTraverserXSLT`
   (838) and `TocEntry` (559) call the two variants of the same resolver a PDF
   export then uses.  Related: the default character style's cache entry is
   seeded with the default *paragraph* style's rPr ("since default font size
   might be in there"), so a run with `w:rStyle="DefaultParagraphFont"` in a
   20pt heading comes out at Normal's 14pt.
4. **Merges that replace where Word inherits.**
   - `apply(STLineSpacingRule)` returns `AUTO` for a null source: a style's
     `w:line="480" w:lineRule="exact"` becomes 480 *auto* (double spacing
     instead of 24pt lines) under any direct `w:spacing` that states only
     `w:after="0"` — which is most direct spacing.  ECMA-376 17.3.1.33 ties the
     `auto` default to the presence of `w:line`.
   - `apply(CTLanguage)` replaces the whole element (a `w:lang w:bidi` wipes an
     inherited `w:val`/`w:eastAsia`); `apply(CTTblPPr)` sets the four
     anchor/spec enums unconditionally (a `w:tblpPr` stating only `w:tblpX`
     wipes the inherited anchors — measured; the 17.0.5 fix d5f067249 repaired
     `isEmpty(CTTblPPr)`, not `apply`); `apply(CTShd)` and `apply(CTBorder)`
     set `themeColor`/`themeFill` unconditionally; `apply(U)` and
     `apply(Highlight)` carry `val` only (`w:u` has a `w:color`).
5. **Resolution mutates the document.**  `init()` writes `w:sz w:val="20"`
   into the styles part's `w:rPrDefault` when it has none ("Make Word default
   explicit" — measured: the JAXB tree changes, so a docx saved after any
   export gains it); `fillPPrStackInternal` rewrites a heading style's
   `w:outlineLvl` to match its id ("Heading2" declaring level 5 becomes 1) and
   injects a `w:numId` into a style whose `w:numPr` lacks one.  And the
   effective objects alias the definitions: `apply(BooleanDefaultTrue)` and the
   other leaf merges return the source object, so `effective.getB()` *is* style
   S's `w:b` (measured), and `getEffectivePPr(PPr)` with no direct formatting
   returns the cache entry itself.  "What is returned is a live object.  If
   you want to change it, you should clone it first!" is the documented
   contract; a caller who forgets edits the styles part.
6. **No thread story.**  The resolver is shared by everything that touches the
   package for its lifetime; `liveStyles`, `resolvedStylePPrComponent` and
   `resolvedStyleRPrComponent` are plain `HashMap`s written lazily while other
   threads read them; resolution itself mutates styles (gap 5);
   `MainDocumentPart.getPropertyResolver` is unsynchronised (two threads can
   build two resolvers).  Two exports of one package at once (HTML and PDF)
   are supported by CR-014 for numbering and by nothing here; CR-014's
   `NumberingConcurrencyTest` avoided styles on purpose.
7. **The default table style is not applied.**  A table with no `w:tblStyle`
   gets an empty `w:tblPr` ("Generated empty tblPr", logged at INFO per
   table), not the `w:default="1"` table style's (`TableNormal`: `w:tblInd 0`,
   `w:tblCellMar` 108 left/right); `AbstractTableWriter.WORD_DEFAULT_CELL_MARGIN_TWIPS
   = 108` (17.0.5) covers the common case by constant.  Whether Table Normal
   also underlies a custom table style with no `w:basedOn` is probe P5.
8. **Lookup cost and log noise.**  A `w:pStyle` naming a missing style (common
   in real documents) costs one full rescan of the styles list per lookup (the
   17.0.4 miss-path rescan has no "nothing changed" check) and an ERROR log per
   paragraph; `apply(SectPr)` logs "TODO: implementation is incomplete" at WARN
   for every direct pPr that carries a `w:sectPr`, and the merged sectPr is read
   by no caller (`ConversionSectionWrapperFactory` reads the direct one).
9. **Dead and duplicated code.**  Fields `themePart`, `themeFontLang`,
   `wordMLPackage` are assigned and never read; 150 lines of commented-out
   font-name resolution at the foot of the class; the `w:basedOn` walk is
   written three times (pPr, rPr, table) with the same cycle handling;
   `getLvlFromHeadingStyle` is duplicated in `toc/StyleBasedOnHelper`;
   `ImmutablePropertyResolver` is a fifth property list with no caller.

## What the code's own comments record (read 2026-09-12)

Design notes in the two classes that bind this CR, with what the code does:

- **"What is returned is a live object.  If you want to change it, you
  should clone it first!"** (`getEffectivePPr`, both overloads).  Kept as the
  contract — a deep copy per call would cost every exporter — but phase 3
  removes the aliasing (gap 5) so that a forgotten clone corrupts a cache
  entry, not the styles part.
- **"NB, any rPr is intentionally ignored, since pPr/rPr is not applicable to
  anything except the paragraph mark"** (`hasDirectPPrFormatting`) beside
  **"Check Paragraph rPr (our special hack of using ParaRPr to format a
  fo:block).  2013 10 02: doubts whether this is right?"**
  (`getEffectiveRPr(RPr, PPr)`).  The hack applies the mark's rPr to a run
  when the run has no rPr *and the paragraph names no style* (measured: bold
  20pt from a mark; with `w:pStyle="Normal"` present, not).  It serves the
  block-level callers that pass `null` on purpose to size an empty paragraph
  (`XsltCommonFunctions` 419, `XsltFOFunctions` 1657 and 3117,
  `HiddenText.isHiddenParagraph`); it reaches no exporter run only because
  the visitor calls the resolver just for runs that have a `w:rPr`
  (`AbstractVisitorExporterGenerator` 339-342) and the markdown exporter
  short-circuits rPr-less runs (`WmlToMarkdown` 815).  Phase 2 makes the mark
  an explicit method and takes the hack out of the run path.
- **"we want to ignore doc default contrib, if rFonts or sz is set in the
  pStyle"** (the three flags, 8.2.4).  The intent is Word's order; the cache
  is what breaks it (gap 3).  Phase 2 caches the style *chain* without
  document defaults and composes, so no flags are needed.
- **"Some styles contain numPr, without specifying their numId!  In this
  case you have to get it from the numPr in their basedOn style.  To save
  numbering emulator from having to do that work, we make the numId explicit
  here"** (`fillPPrStackInternal`, "Injected numId").  A per-element
  `apply(NumPr)` (phase 1) gives the effective pPr the inherited `w:numId`
  without writing into the style; `Emulator` (CR-014 phase 3) reads the
  effective pPr, and `NumberingDefinitionsPart.resolveLinkedAbstractNum`
  reads the numbering style's own `w:numPr`, which Word writes complete.
  Phase 3 removes the injection after checking both.
- **"For heading styles, check the outline level is as expected ... must use
  the outline level appropriate to this heading!"** and
  `getLvlFromHeadingStyle`: **"this is done using the style ID, not its
  OutlineLevel, since Word does it purely on the name of the style!"**  Word
  identifies built-in headings by `w:name` ("heading 1", the same in every
  locale), not by `w:styleId` (localised: "berschrift1", "Titre1"); the code
  keys on the id prefix "Heading", so it fires for English documents only,
  and it fires by rewriting the style.  Phase 3 computes the level in the
  resolved pPr, by `w:name`, without mutation.  Consumers:
  `PropertyFactory.OutlineLevel` (FO/HTML) and `USwitch` (TOC `\u`).
- **"Make Word default explicit"** (`init`, `w:sz 20` when the document
  defaults have none).  Right idea, wrong place (gap 5); the value is probe
  P6.
- **"defaults to auto"** (`apply(STLineSpacingRule)`).  Wrong when the source
  states no `w:line` (gap 4, probe P3).
- **"theme trumps non theme, but here destination is 'lower' than source"**
  (`apply(RFonts)`), and **"if we don't return null, it creates empty rFonts
  element; see comment at line 401 of RunFontSelector"**.  Unchanged here;
  the fonts review owns `RunFontSelector`.
- **The 17.1.0 measured merges** — `apply(Tabs)` (cumulative, `clear`
  removes), `apply(Ind)` (`firstLine`/`hanging` are one property),
  `apply(CTFramePr)` (per attribute), `apply(Spacing)` (autospacing survives),
  `apply(PPrBase, ..., ndp)` (`w:numId 0` drops the level's indent), and 17.1.1
  `apply(List<CTTblStylePr>)` (per condition) and `apply(TrPr)` — each cites
  its document or golden and has a test.  All kept; the catalogue of phase 1
  wraps them, it does not rewrite them.
- **"TODO - if the paragraph is in a table?"** (twice) — settled by
  67ab3b831's rationale: "The resolver is handed a w:pPr and knows nothing
  of the table the paragraph is in"; table styles and their conditions are
  carried into synthetic paragraph styles by `ParagraphStylesInTableFix` for
  both exporters.  The TODOs close with a pointer to that decision.
- **"TODO, jump up to add default table style (if not part of the cycle)"**
  (`fillTableStyleStackInternal`) and **"Generated empty tblPr"** — gap 7,
  probe P5.
- **"This map also contains the rPr component of a pPr"**
  (`resolvedStyleRPrComponent`) — paragraph-style and character-style entries
  share one map; ids are unique per part, so no clash; kept.
- **"TODO: implementation is incomplete"** (`apply(SectPr)`) — gap 8; the
  merged sectPr has no reader; phase 4 drops `w:sectPr` from the pPr merge.
- **"hardcoded limit on deep basedOn hierarchies"** (`isCyclic`, 32) and the
  `docx4j.openpackaging.exceptions.CyclicStylesException.throw` property —
  kept; `StylesCyclicTest` covers it.
- **"NB once created, this is kept for the life of the package"**
  (`MainDocumentPart.getPropertyResolver`) — gap 6.
- **"see similar ImmutablePropertyResolver"** (`StyleUtil` apply section) —
  Adam Schmideg's subclass copies references instead of objects; with
  `StyleUtil.apply` doing the same for leaves today it adds nothing, and it
  has no caller.  Phase 5 deprecates it.

### Are the comments accurate?  Verification status

| # | claim (source) | code does (inspected / measured 2026-09-12) | Word evidence | status |
|---|---|---|---|---|
| 1 | Effective objects are live; clone before changing (`getEffectivePPr` javadoc) | the cached object is returned when nothing is direct; its leaves are the style definitions' own objects | n/a | CODE CONFIRMED; contract kept, aliasing removed (phase 3) |
| 2 | `hasDirectRPrFormatting`'s list "is comprehensive" | 19 of 40 members tested; `w:rtl`-only and `w:position`-only direct rPr resolve as no direct formatting | direct formatting is the last layer for every property (17.7.2) | REJECTED (phase 1) |
| 3 | `pPr/rPr` applies to the paragraph mark only (`hasDirectPPrFormatting`); the 2013 doubt about applying it to runs | applied to a run with no rPr in a paragraph with no `w:pStyle`; unreachable from the exporters' run paths by their own guards | ECMA 17.3.1.29; measured 17.0.5 (CR-001 line-mixed probe): the mark sizes an empty paragraph only | CONFIRMED for the block, WRONG as a run rule; phase 2 splits the API |
| 4 | Doc-default rFonts/sz/lang must not override the paragraph style's (flags, 8.2.4) | true in `getEffectiveRPrUsingPStyleRPr`; the cache ignores the flags, so the public overloads poison it either way | golden P1 (e): the `w:rStyle="S"` run in H is 20pt Sans bold (glyph box 22.2pt, pitch 26.4), as (g) | INTENT CONFIRMED (golden), IMPLEMENTATION WRONG (phase 2) |
| 5 | The default character style's entry needs the default paragraph style's rPr ("default font size might be in there") | seeded so; a run with `w:rStyle="DefaultParagraphFont"` in a 20pt style resolves to Normal's 14pt - and the FO exporter renders it so (P1 (f): a 14pt run after a 20pt lead-in) | golden P1 (f): every line of (f) is 20pt Sans (22.2pt boxes, 26.4 pitch); Default Paragraph Font adds nothing | WRONG (golden) for runs, RIGHT for `getEffectiveRPr(RPr)` (no paragraph); phase 2 separates them |
| 6 | Paragraphs naming no style use the default paragraph style (`getEffectivePPr`) | pPr yes; rPr no (11pt where Normal says 14pt); a missing style resolves to nothing | golden P1: (a) no `w:pStyle`, (b) Normal, (c) a `w:b` run and (d) `w:pStyle="Missing"` are all 14pt Serif (15.4pt boxes, 18.5 pitch): Normal applies to a paragraph naming no style and to one naming a missing one | CONFIRMED (golden), HALF IMPLEMENTED (phase 2); exporters shielded by `ParagraphStylesInTableFix` for (a), not for (d) (docx4j: 11pt runs) |
| 7 | numId injection saves the emulator work; `isEmpty(NumPr)` "only the id is checked" | injection mutates the style; an `ilvl`-only direct numPr is dropped (docx4j labels P4 (b) "2." at level 0) | golden P4: (b) direct `w:ilvl 1` only is level 1 ("1.1." at x=126, the level's 1440 indent); (c) `w:numId` only is level 0 ("2."); (d) L2 (style-level `w:ilvl` only) is level 1 ("2.1."); (e) control "2.2." | SETTLED (golden): `w:numId` and `w:ilvl` each inherit; per-element merge (phase 1), injection removed (phase 3) |
| 8 | Heading level from the style *name* (`getLvlFromHeadingStyle`) | keyed on the id prefix "Heading"; rewrites `w:outlineLvl` in the style | Word matches built-in styles by `w:name`; no golden can show it (Word's PDF export of the goldens writes no `/Outlines`: 0 of 36) | CODE CONFIRMED as a mutation; by-name rule from Word's behaviour, no probe (phase 3) |
| 9 | Word's default size is 10pt when nothing states one (`init`) | writes `w:sz 20` into the styles part | golden P6: (a) no `w:sz` anywhere has 12.1pt glyph boxes and 13.9pt pitch, identical to (b) explicit 10pt ((c) 11pt 13.3/15.4, (d) 12pt 14.4/16.8) | VALUE CONFIRMED (10pt, golden); the write into the styles part is the gap (phase 3) |
| 10 | `w:lineRule` "defaults to auto" (`apply(STLineSpacingRule)`) | a direct `w:after="0"` turns an inherited `exact` into `auto`: docx4j renders P3 (b) at 26.85pt pitch | golden P3: (a) 24.00pt pitch, (b) direct `w:after 0` only **24.00pt** (the exact rule is inherited), (c) direct `w:line 240` only 13.44pt (auto) | WRONG (golden; phase 1) |
| 11 | rFonts: theme trumps explicit within one element; a source with only `w:hint` still applies (`apply(RFonts)`) | as documented | `RunFontSelectorChinese2Test`; the fonts review | CODE CONFIRMED, unchanged |
| 12 | Tabs cumulative, `clear` removes (17.1.0) | as documented | two corpus documents in the javadoc; `StyleUtilTabsMergeTest` | CONFIRMED (golden), unchanged |
| 13 | `w:ind` firstLine/hanging one property; `w:framePr` per attribute; autospacing survives; `w:numId 0` drops the level indent; `w:tblStylePr` per condition (17.1.0-17.1.1) | as documented | measured on corpus documents; `IndFirstLineHangingTest`, `PropertyResolverFramePrTest`, `AutospacingOverrideTest`, `ParagraphStylesInTableFixConditionalTest` | CONFIRMED, unchanged |
| 14 | "TODO - if the paragraph is in a table?" | not here; `ParagraphStylesInTableFix` (67ab3b831) | measured there (9,245 bold lines) | SETTLED by that decision; TODOs close |
| 15 | "TODO, jump up to add default table style"; "Generated empty tblPr" | a style-less table gets nothing from `TableNormal`; the writer's constant 108 stands in, for every table | golden P5, with the document's Table Normal saying `w:left 300`: (a) no `w:tblStyle` and (c) Grid2 based on Table Normal both start the first cell's text 5.76pt in (108 twips + the border; docx4j 5.64), **not** 15pt; (b) Custom, a table style with no `w:basedOn`, starts it 0.48pt in - no cell margin at all (docx4j 5.64) | SETTLED (golden), differently from the TODO: the 108 is Word's built-in Table Normal, applied whatever the document's definition says, to tables whose chain reaches the default table style or that name no style; a chain that does not reach it gets no margin. Phase 4 keeps the constant and withholds it from that case |
| 16 | `apply(SectPr)` "implementation is incomplete" | WARN per direct pPr with a `w:sectPr`; merged sectPr unread | n/a | CODE CONFIRMED; dropped from the merge (phase 4) |
| 17 | The resolver is kept for the life of the package (`MainDocumentPart`) | unsynchronised lazy creation; `HashMap` caches; mutation during resolution | n/a | CODE CONFIRMED gap (phase 3) |
| 18 | `w:tblpPr` anchors merge per attribute (d5f067249 fixed `isEmpty`) | `apply(CTTblPPr)` still nulls the four enums when the source omits them | the b2-batch3 measurements | HALF FIXED (phase 1) |
| 19 | A style's `w:rPr` of only `w:lang` applies (schema: `w:lang` is a run property like any other) | `isEmpty(RPr)` omits `lang`; never applied | ECMA 17.3.2.20; no probe (invisible in a PDF's text layer) | GAP (phase 1) |

**Probe set `styles-*` (one docx per line, generated by the harness's
`Corpus.java`, copied with `corpus.txt` to `$S/corpus/`; Jason runs Word;
goldens back to `$S/goldens-nofields`).  Each probe is a few short paragraphs
whose size, indent or pitch is the answer, so the golden's text layer and the
harness's line positions decide it:**

| id | document | answers |
|---|---|---|
| P1 `styles-default-pstyle` | docDefaults Calibri 11pt; Normal `w:rPr` Times New Roman 14pt; style H Arial 20pt; character style S `w:b` only.  Paragraphs: (a) no `w:pStyle`, run without rPr; (b) `w:pStyle="Normal"`; (c) no `w:pStyle`, run with `w:b`; (d) `w:pStyle="Missing"` (no such style); (e) H with a run `w:rStyle="S"`; (f) H with a run `w:rStyle="DefaultParagraphFont"` | (a)=(b)=(c) 14pt Times (row 6); (d) as Normal (row 6); (e) Arial 20 bold (row 4); (f) Arial 20 (row 5) |
| P3 `styles-linerule` | style X `w:spacing w:line="480" w:lineRule="exact"`; three-line paragraphs in X: (a) nothing direct; (b) direct `w:spacing w:after="0"`; (c) direct `w:spacing w:line="240"` (no lineRule) | (a)=(b) 24pt pitch; (c) single auto (row 10) |
| P4 `styles-numpr-ilvl-only` | a two-level list (`%1.` at 720/360, `%1.%2.` at 1440/360); style L `w:numPr` numId N ilvl 0; L2 basedOn L with `w:numPr` of `w:ilvl 1` only.  Paragraphs: (a) L; (b) L + direct `w:numPr` of `w:ilvl 1` only; (c) L + direct `w:numPr` of `w:numId N` only; (d) L2 | (b) and (d) level 1 of list N; (c) level 0 (row 7) |
| P5 `styles-table-default` | the `w:default="1"` table style given `w:tblCellMar` left 300 (15pt).  Tables: (a) no `w:tblStyle`; (b) style Custom with no `w:basedOn` and no margins; (c) style Grid2 basedOn TableNormal | first-cell text x per table (row 15) |
| P6 `styles-no-size-anywhere` | docDefaults with `w:rFonts` but no `w:sz`; Normal without `w:sz`; one paragraph of prose | the size Word uses (row 9) |

P2 is deliberately absent: the paragraph-mark rule is already measured
(17.0.5) and is the definition of the mark (17.3.1.29).

**Probe status (2026-09-12):** the five probes are in `Corpus.java` (after
the CR-014 set; helpers `stylesPara`, `bareRun`, `addCharacterStyle`,
`addTableStyle`, `numPrOf`, `twoLevelAbstract`), generated, and copied with
`corpus.txt` (99 ids) to `$S/corpus/`.  docx4j's own answers today, from the
harness render (`Fidelity render`, pdftotext line tops):

| probe | docx4j today |
|---|---|
| P1 | (a) = (b) = 14pt Serif, 18.5pt pitch (the exporter's shield, gap 2); (c) bold at 14pt; (d) `Missing`: the run at 11pt (longer lines) under a 14pt block; (e) 20pt Sans bold; (f) the `DefaultParagraphFont` run at **14pt** after a 20pt lead-in (row 5, visible in the exporter); (g) 20pt Sans |
| P3 | (a) 24.00pt pitch (exact); (b) **26.85pt** = 480 *auto* (row 10, visible in the exporter); (c) 13.43pt single |
| P4 | (a) 1. (b) **2.** at level 0 (the direct `w:ilvl` dropped, row 7); (c) 3. level 0; (d) 3.1. (numId injection); (e) 3.2. |
| P5 | (a) and (b) first-cell text one character in (the writer's 108 constant); (c) three characters in (Grid2 inherits the 300) |
| P6 | (a) 10pt Carlito, 14.04pt pitch, same as (b) |

**Goldens in (2026-09-12, Word run "done 5"; `docx4j-layout-fidelity/goldens/word/styles-*.pdf`
and the manifest).**  Word's answers, from the goldens' line boxes (pdftotext
`-bbox-layout`), against docx4j's render (`Fidelity compare`, line parity):

| probe | Word | parity today |
|---|---|---|
| P1 | (a) (b) (c) (d) all 14pt Serif: Normal applies with no `w:pStyle` and with a missing one; (e) 20pt Sans bold; (f) 20pt Sans on every line (Default Paragraph Font adds nothing); (g) 20pt Sans.  Two pages | 73% (Word 2 pages, docx4j 1: (d) and (f) are smaller) |
| P3 | (a) 24.00pt; (b) **24.00pt**; (c) 13.44pt | 100% lines, but (b)'s pitch is 26.85 in docx4j |
| P4 | (a) 1. (b) **1.1.** (c) 2. (d) 2.1. (e) 2.2. | 33% ((b) mislabelled, the rest shifted) |
| P5 | (a) 5.76pt in, (b) **0.48pt** in, (c) 5.76pt in - the document's Table Normal `w:left 300` is not what Word applies | 100% lines; (b) is 5.2pt off |
| P6 | (a) = 10pt exactly | 100% |

## Design

### One property catalogue per properties type (phase 1)

For each of `RPr`/`ParaRPr` (same members, different XJC classes, no common
supertype beyond `Child`), `PPrBase`, `CTTblPrBase`, `TcPr`: a static table of
entries `(name, getter, setter, merge)` — one line per schema member, in
schema order — from which `apply`, `isEmpty`, `unset` and a new
`hasDirectFormatting(T, ignored...)` are derived by iteration.  The
`PropertyResolver.hasDirect*Formatting` methods become
`StyleUtil.hasDirectFormatting` with the members that are not formatting
named once (`rStyle`, `rPrChange` for runs; `pStyle`, `rPr`, `sectPr`,
`pPrChange` for paragraphs — the mark-only rPr stays ignored, as the comment
says).  `ParaRPr`/`RPr` cross-application goes through the same table with a
generic accessor pair, replacing the four pasted methods.  The per-member
merge functions are today's leaf `apply` methods, unchanged where they carry a
17.1.0 measurement, corrected where row 10, 18 and gap 4 say so:

- `STLineSpacingRule`: taken from the source only if the source states
  `w:line`; otherwise inherited.  (ECMA 17.3.1.33.)
- `CTLanguage`: `val`, `eastAsia`, `bidi` each inherit.
- `NumPr`: `numId` and `ilvl` each inherit; `isEmpty(NumPr)` is true only
  when both are absent.
- `CTTblPPr`, `CTShd`, `CTBorder`: enums only when the source states them.
- `U`, `Highlight`: `val` and `color` each inherit.

Every leaf merge returns a *copy* of the source object where it returned the
source itself (gap 5): `BooleanDefaultTrue`, `CTLanguage`, `HpsMeasure`,
`CTSignedHpsMeasure`, `CTTextScale`, `CTSignedTwipsMeasure`, `Color`, ... —
`XmlUtils.deepCopy` per leaf is too slow; a `copyOf` per type in the
catalogue entry (they are one- or two-attribute objects).

Test: one table-driven `PropertyCatalogueTest` that, for every entry, builds a
source with only that member set and asserts `!isEmpty`, `hasDirectFormatting`,
that `apply` carries it into an empty destination, that `unset` removes it, and
that the carried object is not the source's instance.  Plus the specific merge
cases as assertions (S3, S4, S11, S12 of the scratch run).

### Resolution order made explicit (phase 2)

```
effectivePPr(direct)        = docDefaults.pPr ⊕ chainPPr(styleOf(direct)) ⊕ direct
effectiveRPr(direct, pPr)   = docDefaults.rPr ⊕ chainRPr(styleOf(pPr))
                              ⊕ chainRPr(direct.rStyle) ⊕ direct
paragraphMarkRPr(pPr)       = docDefaults.rPr ⊕ chainRPr(styleOf(pPr)) ⊕ pPr.rPr
```

where `styleOf(pPr)` is the paragraph's `w:pStyle` if it names a style that
exists, else the `w:default="1"` paragraph style (row 6), and `chainPPr` /
`chainRPr(styleId)` is the `w:basedOn` chain merged root-first, *without*
document defaults, cached per styleId (immutable once built).  The public
API keeps its signatures:

- `getEffectivePPr(PPr)`, `getEffectivePPr(String)` — as today, computed as
  above (the styleId form includes docDefaults, as today).
- `getEffectiveRPr(RPr, PPr)` — as above; never applies the paragraph mark.
- `getEffectiveParagraphMarkRPr(PPr)` (new) — for the block-level callers,
  which are migrated in the same phase (`XsltCommonFunctions` 419,
  `XsltFOFunctions` 1657/3117, `HiddenText.isHiddenParagraph`).
- `getEffectiveRPr(String)` — docDefaults ⊕ chain (a paragraph style's block
  rPr, or a character style resolved with nothing under it), cached under a
  key distinct from the chain's.
- `getEffectiveRPr(String, boolean, boolean, boolean)` — deprecated; computed
  from the chain on each call (no cache), so it can no longer poison anything.
- `getEffectiveRPr(RPr)` (11.5.12, no paragraph) — docDefaults ⊕
  chainRPr(default paragraph style) ⊕ chainRPr(rStyle) ⊕ direct, which is
  what its seeded cache entry approximated (row 5).
- `getEffectiveRPrUsingPStyleRPr(RPr, RPr)` — kept, deprecated (its only
  caller is the class itself).

`ParagraphStylesInTableFix`'s writing of the default `w:pStyle` onto every
paragraph stays in 17.1.1 (it also carries the table conditions); it becomes
removable outside tables once the resolver falls back itself — decision 2.

Test: `PropertyResolverOrderTest` — the scratch run's S1 (a-d), S5 in both
call orders, S6, S15, S2 through both APIs; expected values from the P1
golden where it applies.

### No mutation, no aliasing, thread safety (phase 3)

- The resolver works on private copies of the document defaults (one
  `deepCopy` at init); the 10pt default (row 9, value per P6) is applied to
  the copy.
- The heading outline level is computed when a chain pPr is built: a style
  whose `w:name` is `heading N` (case-insensitive, N in 1..9) gets
  `w:outlineLvl N-1` in the *resolved* pPr, whatever the style declares; the
  style is not touched.  `getLvlFromHeadingStyle(String)` stays (public)
  and gains a by-name overload; `toc/StyleBasedOnHelper` adopts it.
- numId injection removed (phase 1's `NumPr` merge carries the inherited
  `w:numId`); checked against `Emulator` and
  `NumberingDefinitionsPart.resolveLinkedAbstractNum` (which read the
  effective pPr and the numbering style's own `w:numPr` respectively), and
  by the CR-014 numbering tests and probes.
- Caches become `ConcurrentHashMap`s.  Resolution is pure once the mutations
  are gone, so two threads computing the same entry do duplicate work and
  store equal values; no `computeIfAbsent` recursion (a chain build does not
  call back into the cache).  `liveStyles` rescans only when the styles
  list's size differs from the size last scanned (the 17.0.4 late-add case
  still works; a missing style costs one comparison after the first miss).
  `refresh()` builds new maps and swaps them.  `MainDocumentPart.getPropertyResolver`
  creates under a lock.
- Test: `PropertyResolverConcurrencyTest` (N threads resolving a random walk
  of styles and runs while one thread adds a style; every answer equals the
  single-threaded one) and `PropertyResolverNoMutationTest` (marshal the
  styles part before and after a full resolution of every style and a
  document's paragraphs; byte-equal), plus the CR-014 concurrency test
  extended to resolve through styles.

### The default table style, cost, dead code (phase 4)

- `getEffectiveTableStyle`: P5 answered the TODO the other way round.  Word
  applies its *built-in* Table Normal (108 twips left and right, 0 top and
  bottom, `w:tblInd 0`) to a table naming no style and to one whose chain
  reaches the default table style, and not the document's own definition of
  that style (left 300 was ignored); a style whose chain does not reach it
  contributes no cell margin at all.  So the stack is not rooted at the
  document's default table style; instead `getEffectiveTableStyle` reports
  whether the chain reaches it (or there is no style), and
  `AbstractTableWriter` applies `WORD_DEFAULT_CELL_MARGIN_TWIPS` only then
  (today: always; P5 (b) is 5.2pt off).  "Generated empty tblPr" to DEBUG.
  A follow-up probe (Table Normal restating `w:left 0`, and a style based on
  it restating margins) would say whether the document's definition is
  ignored entirely or only when it enlarges the margin; not needed for the
  fix above.
- `w:sectPr` leaves the pPr merge (`apply(PPr, PPr)` no longer calls
  `apply(SectPr)`; that method stays, deprecated, its WARN gone).
- Missing-style lookups: logged once per styleId per resolver.
- The three `w:basedOn` walks become one `ancestry(styleId)` (root-first
  list, cycle-checked once, the "DocDefaults" virtual-style special case
  kept); dead fields and the commented font code removed.

### API hygiene (phase 5)

Deprecations (nothing removed): the four-flag `getEffectiveRPr`,
`getEffectiveRPrUsingPStyleRPr`, `ImmutablePropertyResolver`,
`StyleUtil.apply(SectPr, SectPr)`, `PropertyResolver.hasDirectRPrFormatting`
(delegates to the catalogue).  Javadoc on the class stating the resolution
order, the live-object contract, thread safety and `refresh()`;
`package-info` for `org.docx4j.model.styles`; CHANGELOG.

## Layering (Jason, 2026-09-12: "we should be getting it right in the resolver so we are not layering fix upon fix")

What kept the FO output right while the resolver's answers were wrong is not
a layout fix-up but two accidents and a second resolver:
`ParagraphStylesInTableFix` writes `w:pStyle` = the default style onto every
paragraph lacking one (a resolver rule implemented in a preprocess); the
visitor never asks the resolver about a run with no `w:rPr`
(`AbstractVisitorExporterGenerator` 339-342), so such runs inherit the block
and the paragraph-mark hack never fires; and `RunFontSelector` walks the
paragraph style for fonts, size and `w:kern` itself, from the effective pPr
with the pStyle re-attached (`FOExporterVisitorGenerator` 451-459).

The rule for where a rule lives, from that discussion:

1. **What properties apply to a paragraph or run** - given the document
   defaults, the styles, the numbering, and where it sits - is
   `PropertyResolver`'s, always.  Where a rule needs context the resolver is
   not handed today, hand it the context (as the numbering part already is)
   rather than rewrite the document to suit the resolver.
2. **How Word lays out given those properties** is the FO layout layer's
   (the line manager, `WordLayoutFixups`); not resolution.
3. **How resolved properties are represented in an output format** is the
   exporter's or a preprocess's.  That is `ParagraphStylesInTableFix`'s
   legitimate job: HTML output uses style ids as CSS classes, so
   table-conditional formatting has to become a named style there.  Computing
   those properties itself, and shielding the resolver's default-style gap,
   are not.

Consequences for this CR and after it:

- **Phase 2 also has the visitor resolve every run**, `w:rPr` or not, once the
  mark is an explicit call.  Each inline then carries its own resolved size,
  which should make `XsltFOFunctions.pinInheritedFontSize` unnecessary;
  verified on the corpus, not assumed.
- **Phase 2b (new): the default-`w:pStyle` injection comes out for paragraphs
  outside tables**, with its own corpus gate, so the corpus measures the
  resolver rather than the shield.  (Was decision 2, "a later release".)
- **Table conditions behind the resolver** - `getEffectivePPr(pPr, tableContext)`
  and the rPr counterpart, `TableStyleConditions` supplying the context;
  `ParagraphStylesInTableFix` keeps naming synthetic styles for HTML but takes
  their content from the resolver, and the FO pathway needs no synthetic
  styles at all.  Reopens 67ab3b831's placement, so it is a CR of its own
  after this one, not phase 4.
- **`RunFontSelector` consumes the effective rPr** and does script and glyph
  selection only: the fonts review, which is why it follows phase 2.

## Plan

### Phase 0 — Unix line endings (preliminary; no code change) — DONE 2026-09-12 (6f4763d70)

    sed -i 's/\r$//' docx4j-core/src/main/java/org/docx4j/model/styles/{BrokenStyleRemediator,Node,Tree}.java \
        docx4j-core-tests/src/test/java/org/docx4j/model/{PropertyResolverEffectivePPrFromExpressTest,PropertyResolverEffectivePPrFromStyleIdTest,PropertyResolverTestUtils}.java
    git diff -w --stat     # empty apart from the header

One commit, nothing else in it; hash into `.git-blame-ignore-revs`;
`.gitattributes` gains `docx4j-core/src/main/java/org/docx4j/model/styles/**
text eol=lf` and the test directory.  Exit: `grep -rl $'\r'` over the package
and `docx4j-core-tests/.../model/` returns nothing; `mvn -o -q -Dgpg.skip=true
install -pl docx4j-core -DskipTests` unchanged.

### Phase 0b — verification probes (no code change) — DONE 2026-09-12 (goldens in, table settled)

Add the five `styles-*` probes to `Corpus.java` (`Doc` already has
`addParagraphStyle(id, basedOn, pPr, rPr)`, `documentDefaultRun`,
`numberingXml`, `addNumberingStyle`, the `Table` builder; P5 edits the
default table style through `mdp().getStyleDefinitionsPart()`), `Fidelity
generate`, copy the docx and `corpus.txt` to `$S/corpus/`, wait for the Word
run, copy goldens back to `docx4j-layout-fidelity/goldens/word/`, update the
table's status column.  Phase 1 depends on P3 and P4 only for its expected
values (the spec answers are implemented meanwhile); phase 3 on P6 for the
default size; phase 4 on P5.

### Phase 1 — the property catalogue and the merge rules — DONE 2026-09-12

As designed, with two things learned in the doing: `apply(RFonts)` gave a
null source an empty `w:rFonts` in the destination (so every merged rPr read
as directly formatted once `hasDirectFormatting` was catalogue-driven); it now
leaves the destination alone for a null source and keeps the empty-element
behaviour for a non-null empty one (the `RunFontSelectorChinese2Test` case the
comment names).  `isEmpty(Spacing)` now counts an explicit autospacing
attribute, as `apply(Spacing)` already carried it.  The catalogue-coverage
test (`PropertyCatalogueTest.cataloguesCoverTheGeneratedMembers`) reflects
over the generated setters, so a member added to the schema classes and not to
the catalogue fails the build.  `CTTblPrBase` and `TcPr` keep their existing
`apply`/`isEmpty` pairs (no drift found there; a later phase may fold them in).
Tests: `PropertyCatalogueTest` (4), `StyleUtilMergeRulesTest` (13).

Gate (2026-09-12): `docx4j-core-tests` 958 run, 0 failures;
`docx4j-export-fo-tests` 600 run, 0 failures; corpus score `p1-catalogue`
against `p4-stories-h2`, `-Dfidelity.hyphenate=false`:

| corpus | changed documents | mean line parity | lines matched |
|---|---|---|---|
| real (191) | 6 improved, 0 regressed | 0.9032 -> 0.9051 | 36486 -> 36634 of 40299 |
| real2 (156) | 7 improved, 0 regressed | 0.8763 -> 0.8794 | 61784 -> 61925 of 71671 |
| real3 (102) | 8 improved, 0 regressed | 0.9105 -> 0.9162 | 172717 -> 173367 of 186199 |

The improved documents are the `num_tbl` population the rules predicted
(exact line spacing under a direct `w:after`, `ilvl`-only numbering, `w:rtl`
in the ru-RU set).  One document to watch: `12_ru-RU_sdt_fields1_num_tbl_11928`
gains a page (Word 32, docx4j 32 -> 33) while its line parity rises 0.9491 ->
0.9526; the extra page is a keep or spacing consequence of a line now
correctly taller, to be read in the CR-001 way when that work resumes.  Gate: `docx4j-core-tests` and `docx4j-export-fo-tests` green
(the `PStyle12PtInTable*` and CR-001 tests are the guard for the kept
merges); corpus re-score against `b63-precr-h2` / `p4-stories-h2` with
`-Dfidelity.hyphenate=false` — expected movement, all towards Word: documents
with `w:rtl`-only or `w:position`-only runs, exact line spacing under a direct
`w:after`, `ilvl`-only direct numbering, `w:tblpPr` stating one attribute.
Any document moving the other way is a finding to record before merging.

### Phase 2 — the resolution order — DONE 2026-09-12

As designed: `chainPPr`/`chainRPr` (the `w:basedOn` chain merged root-first,
no document defaults) and `effectivePPrByStyle`/`effectiveRPrByStyle`
(defaults plus chain) replace the two seeded maps; `paragraphStyleOf(pPr)`
supplies the default paragraph style for a paragraph naming none or a
missing one; `getEffectiveParagraphMarkRPr(PPr)` is the mark; the flagged
overload computes per call and is deprecated, as is
`getEffectiveRPrUsingPStyleRPr`.  Two of the three block-level callers
migrate (`XsltCommonFunctions` and `XsltFOFunctions`'s ParaRPr branches);
`XsltFOFunctions` 1657 wanted the run baseline of generated text, and keeps
the run call.  The visitor calls `handleRPr` for every run (the HTML one is a
no-op; the FO one now resolves rPr-less runs, whose inlines carry their own
attributes).  Tests: `PropertyResolverOrderTest` (10; the P1 golden's cases
a-g, both call orders, the mark, the other overloads).

Gate (2026-09-12): `docx4j-core-tests` 968 run, 0 failures;
`docx4j-export-fo-tests` 600 run, 0 failures; corpus score `p2-order`
against `p1-catalogue`: real 0 changed, real2 1 improved / 0 regressed
(`15_en-US_num_tbl_9888` 0.8865 -> 0.9555: a two-column compliance table
whose rPr-less runs now carry their own size), real3 0 changed - the
zero-delta the shield predicted.  Probes: `styles-default-pstyle` 73% -> 100%
line parity, two pages as Word; `styles-numpr-ilvl-only` 33% -> 50%, every
label now Word's (1. 1.1. 2. 2.1. 2.2.), the direct `ilvl`-only paragraph
still at level 0's indent because `StyleUtil.apply(PPrBase)` looks the
level indent up from the source's `w:numPr` alone (no `numId`) - a
follow-up commit looks it up from the merged one.

### Phase 2b — the shield comes out (added 2026-09-12, see Layering)

`ParagraphStylesInTableFix` stops writing the default `w:pStyle` onto
paragraphs outside tables (inside tables the synthetic style ids remain the
HTML mechanism), and the visitor resolves runs with no `w:rPr` through the
resolver like any other.  Gate: corpus zero-delta against the phase 2 score
(any movement is a resolver defect the shield was hiding, to be fixed in the
resolver, not by restoring the shield); `pinInheritedFontSize` removed if the
corpus agrees it is redundant.

### Phase 3 — no mutation, no aliasing, thread safety

As designed.  Gate: the two new tests; corpus zero-delta; the CR-014
numbering tests and the `numbering-*` probes unchanged (numId injection
removed); a saved docx byte-equal in its styles part after an export.

### Phase 4 — default table style, cost, dead code

As designed.  Gate: corpus (tables without `w:tblStyle` should not move,
since the constant already matched; a document whose default table style
says something other than 108 may); `TableStyleConditionsTest`,
`ParagraphStylesInTableFixConditionalTest`.

### Phase 5 — API hygiene

Deprecations, javadoc, `package-info`, CHANGELOG, CR status.

## Decisions

1. **Live objects stay the contract** (clone before changing); aliasing of
   style leaves is removed instead of copying on every call.  Recommended;
   the alternative (a deep copy per `getEffective*` call) costs every
   exporter for the benefit of callers who ignore the javadoc.
2. ~~**`ParagraphStylesInTableFix` keeps writing the default `w:pStyle` onto
   every paragraph in 17.1.1**~~ — superseded 2026-09-12 by phase 2b (Jason:
   the resolver gets it right, the shield comes out, in this CR).
3. **Probe set**: the five above; P2 (paragraph mark) omitted as already
   measured.  Jason to confirm or add.
4. **Phase 1's spec-derived rules (lineRule, ilvl-only) ship before their
   goldens if the Word run lags**, with the golden read back into the table
   when it arrives; the corpus gate is the guard meanwhile.
5. **`w:sectPr` leaves the effective pPr** (phase 4).  Any caller wanting it
   reads the direct pPr, which every caller already does.
6. **Order of the CR-015 phases against the fonts review**: the fonts review
   starts after phase 2 at the earliest, since it consumes the effective rPr
   (rows 4-6 change what `RunFontSelector` is handed).

## Risks

- **Behaviour change in the exporters from phase 1's merge rules** —
  intended (towards Word) but the lineRule fix touches most documents with
  exact or atLeast spacing in a style.  The corpus gate is the control;
  P3's golden settles the rule.
- **Consumers relying on the mutations**: a caller reading a style's
  `w:numPr/w:numId` after resolution (injection), or a heading style's
  rewritten `w:outlineLvl`.  Grepped: none in main code beyond the two
  named in the design; user code may.  CHANGELOG notes it.
- **Performance**: leaf copies in every merge (small objects, many calls) and
  the `deepCopy` of docDefaults at init.  Measure phase 1 and 3 on the
  311-page corpus document used for 67ab3b831 before and after.
- **`ConcurrentHashMap` and null keys**: `defaultParagraphStyleId` can be
  null (a styles part with no default); today's `HashMap` tolerates the null
  key.  The new code uses an explicit sentinel.
- **The shield in `ParagraphStylesInTableFix`** hides row 6 from the corpus;
  phase 2's correctness rests on its unit tests and the markdown/TOC
  consumers, not on the corpus.
