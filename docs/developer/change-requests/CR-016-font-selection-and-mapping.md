# CR-016: Font selection and mapping (`RunFontSelector`, `Mapper` and its subpackages) — line endings, one resolution, the character-range rules, the mapping order, discovery, cost, API

Status: IN PROGRESS (2026-09-12) — phase 0 done (d643638ad); Jason read the CR
the same day and started the work.  The review below was written that day
against 7563277a0 (CR-015 tidy-up), with every "measured" claim taken from a
scratch test that was run and then deleted (`ScratchFontsProbeTest`, not
committed; the phase tests reproduce each case as an assertion).
Scope: `docx4j-core/src/main/java/org/docx4j/fonts/` — `RunFontSelector`
(2,611 lines), `Mapper` (623), `IdentityPlusMapper` (370),
`BestMatchingMapper` (889), `PhysicalFonts` (1,028), `PhysicalFont`,
`FontFallback` (579), `GlyphCheck`, `GlyphAdvances`, `TextMeasurer`,
`WordLineMetrics` (505), `LanguageTagToScriptMapping`, `CJKToEnglish`,
`FontUtils`, the `substitutions` and `microsoft` subpackages (their JAXB
classes, `FontSubstitutions.xml` with 419 entries, `MicrosoftFonts.xml` with
149, `word-line-metrics.properties` with 512 families) and `foray/Panose`;
the discovery pass `MainDocumentPart.fontsInUse()` and the mapper set-up in
`WordprocessingMLPackage.setFontMapper`, which are the package's only
callers on the way in; the two character visitors in `FOConversionContext`
and `HTMLConversionContext`, which are its output; the tests under
`docx4j-core-tests/.../fonts/` (25 classes; the two under `fonts/fop/util` are out of scope) and the font tests in
`docx4j-export-fo-tests` (`FieldFontTest`, `EmptyParagraphFontTest`,
`GlyphFallbackTest`, `LigatureSuppressionTest`, `SymbolRangeFallbackTest`,
`SymbolNumberingLabelTest`, `SmallCapsLineHeightTest`,
`UnpreservedWhitespaceTest`, `TextBoxTest`, `CreateBlockTest`).
Out of scope: `org/docx4j/fonts/fop/**` (the FOP fork's font code and
`FopConfigUtil`; Jason's exclusion) — where a phase needs it to consume a
new Mapper answer, the change is one call and is named in the phase;
`ThemePart` (a consumer; the theme-language defect is in
`LanguageTagToScriptMapping`, in scope); `PropertyResolver` (CR-015, done);
the *measured* substitutes CR-001 batch 42 wants (Trebuchet MS, Cambria's
Greek, a lighter face for Calibri Light) — this CR settles the *rules* they
plug into and cuts the probes, the measurements stay CR-001's (Decisions 3).
Phases: 0. Unix line endings (preliminary, no code change); 0b. the
`fonts-*` verification probe set to the share (Word run by Jason); 0c. the
corpus scored with `BestMatchingMapper`, and with the font sets a
deployment actually has (no code change); 1. one
resolution: the selector consumes the effective rPr and decides script and
glyph only, `w:cs`/`w:rtl` by value, the theme language, the default font;
2. the character-range dispatch: shared characters, the symbol fonts,
astral characters, the ranges the probes settle, span merging; 3. the
mapping order: one precedence for both mappers, the face order, a document
font with no bold face, the unresolvable face and the `w:altName` chain;
4. discovery as a walk for names, per-run cost, the embedded-font blind
spots, logging; 5. API hygiene behind deprecation, and the HTML
`font-family` decision.
Related: CR-015 (its Layering section assigns this CR its first phase:
"`RunFontSelector` consumes the effective rPr and does script and glyph
selection only"; its `getEffectiveRPr(RPr, PPr)` is the one call this CR
needs, and `getEffectiveRPrUsingPStyleRPr`, which the selector still calls,
is deprecated by it); CR-001 (Word layout fidelity, Enterprise repo) drove
the 17.0.5 to 17.1.1 additions to this package — `FontFallback`, the
metric-compatible and class-based substitutes, `WordLineMetrics`, the
kerned and no-ligature twins, `w:w` scaling, small caps, the glyph-aware
last resort — all kept; its corpus-3 triage (ledger4, 2026-09-12) puts
**48% of the remaining line deficit in this package** (font substitution
for an absent face ~3,050 lines and 27 pages; a `Light` family measured
with a regular-weight clone 1,735 lines) and proposes batch 42 for it;
issues 622, 666 (the complex-script ranges), 632 (Wingdings code points),
the 17.0.3 field-font work (`AbstractFldSimpleWriter.applyFont`,
`XsltCommonFunctions.fontCanRender`), the 17.0.4 punctuation range
(a2eb422d3).

## Background

Two things happen between a `w:r` and a `font-family`:

1. **Mapping** — a document font name to a physical font file.  `PhysicalFonts`
   discovers the machine's fonts once (in the `Mapper` static initialiser:
   `FontFileFinder`, jar fonts under `fonts/`, the `fonts-symbol` jar) and keys
   each by its first FOP triplet, the nameID 4 full name ("Carlito Regular",
   "Arimo Bold").  A `Mapper` — one per package — maps the *document's* names
   (`fontsInUse`) to those: `IdentityPlusMapper` (the default) by name, then
   "`<name> regular`", "`<name> italic`", "`<name> bold`", then the embedded
   forms `FontTablePart.processEmbeddings` registered; `BestMatchingMapper` by
   embedded forms, exact name, the Calibri workaround, panose distance
   (`MATCH_THRESHOLD` 30, name affinity on ties since 17.0.3), then
   `FontSubstitutions.xml`.  `WordprocessingMLPackage.setFontMapper` then
   runs, for both mappers, `addMetricallyCompatibleSubstitutes` (Times New
   Roman to Tinos, Calibri to Carlito, and the 17.0.5-17.1.0 measured
   entries), `addAltNameSubstitutes` (17.1.0, the document's own
   `w:altName`) and, for `IdentityPlusMapper` only, `addClassBasedSubstitutes`
   (17.0.5, `FontFallback.selectByClass`).  Embedded fonts are deliberately
   kept out of `PhysicalFonts` (they are per document) and live on the
   Mapper.  `FopConfigUtil` (out of scope) turns the Mapper's answers for
   `fontsInUse`, plus the late `registerLastResortFallback` set, into the
   FOP configuration.
2. **Selection** — which of a run's four fonts (`w:ascii`, `w:hAnsi`,
   `w:eastAsia`, `w:cs`, each possibly a theme reference resolved through
   `ThemePart.getFont(type, themeFontLang)`) formats each character.
   `RunFontSelector.fontSelector(pPr, rPr, text)` resolves the run's
   effective rPr *itself* (default paragraph style or `w:pStyle` through
   `getEffectiveRPr(styleId)`, then the deprecated
   `getEffectiveRPrUsingPStyleRPr`), captures size, kerning, scaling and
   ligatures for the line metrics, upper-cases for `w:caps`/`w:smallCaps`,
   takes the symbol-font path (Symbol, Wingdings, Webdings: code point to
   Unicode replacement, `SymbolMapper`) or the complex-script path (`w:cs` or
   `w:rtl` present: the whole run in the cs font), else walks the text
   character by character through the [MS-OI29500] 17.3.2.26 range table
   (`unicodeRangeToFont`), calling a `RunFontCharacterVisitor` which builds
   one `fo:inline`/`span` per font change.  `finish()` then post-processes the
   fragment for XSL-FO: glyph coverage per script (`glyphFallback`,
   `FontFallback.selectCovering`), kerned spaces, `w:w` letter-spacing, small
   caps, the no-ligature twin.  A third mode, `DISCOVERY`, drives the same
   walk with a visitor that only collects names, for `fontsInUse()`.

Consumers: both FO pathways (the visitor via
`AbstractVisitorExporterGenerator` 391, with the effective pPr and the
pStyle re-attached by `FOExporterVisitorGenerator` 451-459; the XSLT via
`XsltCommonFunctions.fontSelector`), the HTML pathway (same two), generated
text (`fontSelectorForGeneratedText`, `AbstractFldSimpleWriter.applyFont`,
`XsltFOFunctions.resolveFontFamily` for list labels, tab leaders and empty
paragraphs), `docx4j-docx-anon` (`ScrambleText`, DISCOVERY mode), and user
code through `getFontMapper()`/`PhysicalFonts`.

## Line endings (the preliminary)

| file | CRLF lines |
|---|---|
| `fonts/CJKToEnglish.java` | 41 (all) |
| `fonts/IdentityPlusMapper.java` | 370 (all) |
| `fonts/GlyphCheck.java` | 157 (all) |
| `fonts/substitutions/FontSubstitutions.java` | 357 (all) |
| `fonts/substitutions/ObjectFactory.java` | 66 (all) |
| `fonts/microsoft/MicrosoftFonts.java` | 648 of 667 (mixed) |
| `fonts/microsoft/MicrosoftFontsRegistry.java` | 90 (all) |
| `fonts/microsoft/ObjectFactory.java` | 72 of 91 (mixed) |
| `docx4j-core-tests/.../fonts/RunFontSelectorArabicTest.java` | 580 of 585 (mixed) |
| `docx4j-core-tests/.../fonts/RunFontSelectorRussian1Test.java` | 558 of 563 (mixed) |
| `docx4j-core-tests/.../fonts/RunFontSelectorRussian2Test.java` | 552 of 557 (mixed) |
| `resources/org/docx4j/fonts/substitutions/FontSubstitutions.xsd` | 56 (all) |
| `resources/org/docx4j/fonts/microsoft/MicrosoftFonts.xsd` | 87 (all) |

`RunFontSelector`, `Mapper`, `BestMatchingMapper`, `PhysicalFonts`,
`FontFallback`, `WordLineMetrics`, the 17.x classes and the other 24 tests
are LF; so are the three data files (`FontSubstitutions.xml`,
`MicrosoftFonts.xml`, `word-line-metrics.properties`).  As before: one
whitespace-only commit before any code change, `git diff -w --stat` empty,
its hash in `.git-blame-ignore-revs`, `.gitattributes` rules for
`org/docx4j/fonts/*.java`, the two subpackages, the test directory's
`*.java` and the two `.xsd`.

## Gaps found (review of 2026-09-12)

Ordered by value.  "Measured" means the scratch test on this box (Linux,
Carlito/Tinos/Arimo/DejaVu/Noto Symbols installed, no Microsoft fonts) on
2026-09-12; "corpus" means CR-001's three real-document corpora and the
ledger4 triage.  Word's side is [MS-OI29500] 17.3.2.26 (which the class
javadoc cites), ECMA-376, or one of the probes below.

1. **An absent document font is the largest fidelity cost left, and the rule
   is unsettled.**  Corpus: font substitution for a face the machine lacks
   (ledger4 class 1: `Trebuchet MS`, `Times New Roman CYR`, `Nokia Pure
   Text` via an absent `w:altName`, Cambria's Greek, Sylfaen) costs ~3,050
   lines and 27 pages, and a `Light` family measured with its regular-weight
   clone (class 2: `Calibri Light` set in Carlito Regular, and its `w:b`
   runs in Carlito *Bold* where Word paints the Light face's advances)
   1,735 more — 48% of everything the corpus still loses.  What the code
   does today for a name nothing resolves: `addClassBasedSubstitutes`
   (`FontFallback.classOf`, from `FontSubstitutions.xml` or the name) picks a
   face of the same class, else the run falls to `fallbackFont`, the
   *document default's* physical font (`RunFontSelector` 106), whatever the
   fontTable says about the face (`w:family`, `w:panose1`, `w:charset`,
   `w:altName` — only the last is read, 17.1.0).  The ledger measured Word
   twice (7320, 12630) as falling back "to the UI font, per character", with
   that font's line box (1.2217 against Tinos's 1.150); what Word actually
   consults is the fontTable's own description of the missing face, which
   no probe has yet varied.  Probe P5 varies it; phase 3 implements the
   answer in one place (`FontFallback`/`Mapper`), and phase 3 also adds the
   "no bold face" rule for families like Calibri Light (probe P7): a
   document font the registry knows to have no bold member is declared to
   FOP with `simulate-style` on its substitute rather than the substitute's
   real bold (a one-line consumer change in `FopConfigUtil.renderedFace`).
2. **A second resolver.**  `fontSelector` (1282-1341) resolves the run's
   properties again: default paragraph style or `w:pStyle` to
   `getEffectiveRPr(styleId)`, then `getEffectiveRPrUsingPStyleRPr` (which
   CR-015 deprecated: "`getEffectiveRPr(RPr, PPr)` composes the paragraph
   style itself"), and `captureLineSpec` resolves the pPr once more for
   `w:spacing`.  The visitor hands it an *already effective* pPr with the
   pStyle re-attached for exactly this reason (`FOExporterVisitorGenerator`
   452-459, "RunFontSelector needs it to apply the paragraph style's run
   properties"), and since CR-015 phase 2 every run's inline carries its own
   resolved rPr, so the selector resolves what its caller has already
   resolved.  Measured: 23 µs per run in `fontSelector`, of which the two
   resolver calls are 2-4 µs and a fresh DOM `Document` per run
   (`XmlUtils.getNewDocumentBuilder().newDocument()`, 1358) 8 µs.  The
   layering decision in CR-015 puts "which properties apply" in the resolver
   alone; phase 1 makes the selector consume the effective rPr and keep only
   script and glyph selection, the caps transform and the line-metric
   capture (which are per character or per output format, so are its).
3. **`w:cs` and `w:rtl` are read for presence, not value** (1445: `if
   (rPr.getCs()!=null || rPr.getRtl()!=null)`).  `<w:cs w:val="0"/>` is how
   Word turns an inherited complex-script flag off, and
   `StyleUtil.apply(RPr)` carries the element through with its value (2525),
   so the effective rPr of such a run holds `cs=false` and the run is set,
   whole, in the cs font.  Measured: a run with `w:cs w:val="0"` and
   `w:cs="Arial"` renders "Hello" in Arimo, the same as `<w:cs/>`; with a
   character style saying `<w:cs/>` and the run saying `w:val="0"`, Arimo
   again; `<w:rtl w:val="0"/>` likewise.  ECMA-376 17.3.2.7: the property is
   the *value*; probe P1 measures Word's widths for the four cases.
4. **The theme language maps Estonian to Ethiopic.**
   `LanguageTagToScriptMapping` (95, 105, 110, 158) tests membership with
   `"ti,bwo,eth,kxh,mdy".contains(lang)`, so `et` (Estonian) matches inside
   `eth`, `mn` (Mongolian) inside `mni`, `wo` (Wolof) inside `bwo`.
   `ThemePart.getFont(MINOR_H_ANSI, themeFontLang)` uses `w:val` for the
   Latin slots, and Office's default theme lists `<a:font script="Ethi"
   typeface="Nyala"/>`.  Measured: with `<w:themeFontLang w:val="et-EE"/>`
   and docDefaults naming `minorHAnsi`, the theme part answers **Nyala** for
   the Latin slot and every theme-font run resolves to it (Vrinda for
   `mn-MN`); on this box Nyala is unmapped so the run fell to the default
   font's Carlito, on a Windows box (Nyala ships with it) an Estonian
   document's Latin text is set in an Ethiopic face.  Probe P4 confirms Word
   uses Calibri; the fix is an exact-match set.  Found beside it: the
   constructor computes `fallbackFont = getPhysicalFont(getDefaultFont())`
   (106) *before* it reads `themeFontLang` (120), and `getDefaultFont()`
   caches its answer, so the default font — the fallback for every
   unresolvable run and for an absent `w:ascii`/`w:hAnsi` — ignores the
   document's theme language for the life of the selector (measured:
   `asciiFontName(null)` stays Calibri while the runs go to Nyala).
5. **Characters shared between scripts follow the previous character.**
   `unicodeRangeToFont` joins a space to the current span whatever its range
   (1802, `c==' '`), so the space between two CJK words takes the East Asian
   font (measured: `[Source Han Sans CN|日本 語 ][Carlito|abc ][Source Han
   Sans CN|語]`); U+0020 is in the ASCII range, whose font is `w:ascii`, and
   an East Asian font's space is commonly half an em where Calibri's is
   0.226 em — a visible difference in every mixed line.  The 17.0.5
   `defaultRange` change and `glyphFallback`'s `isShared` rule (a shared
   character "goes with what precedes it, so a Georgian phrase doesn't come
   apart at its spaces") are the same instinct applied to the last-resort
   pass, where it is right for shaping and wrong for width.  Probe P2
   measures the gap; the two passes then agree with it.
6. **The complex-script ranges without `w:cs`.**  U+0590-U+07BF (Hebrew,
   Arabic, Syriac, Thaana, NKo) goes to "Times New Roman" if that font has
   the glyph, else nothing (1941-1946: "Word doesn't use Arial Unicode MS
   (where specified), so I assume it wouldn't use most other fonts either.
   It often uses TNR, so the following is good enough"; the table says
   `w:ascii`).  Measured: Hebrew in a run with `w:ascii="Arial"
   w:hAnsi="Calibri" w:cs="Tahoma"` and no `w:cs`/`w:rtl` renders in Tinos —
   none of the three fonts the run names.  Probe P3 settles which slot Word
   uses (the table's `ascii`, or the cs font as for the Indic ranges, issues
   622/666); phase 2 implements the answer and removes the hard-coded name.
7. **The blind spots for embedded fonts, again.**  17.0.3 fixed the glyph
   checks (`physicalFontFor`, via the Mapper), but four 17.0.5-17.1.0
   post-processes still look the physical font up by *name* in
   `PhysicalFonts`, where an embedded font is deliberately absent:
   `applyLineHeight` (818: the line height of an embedded font comes from
   `WordLineMetrics.get(documentFont, null)`, i.e. the Microsoft table if
   the family is in it, else the 1.2 fallback — never the embedded file's
   own OS/2 metrics), `kernSpaces` (602), `applyScaling` (687) and
   `noLigatures` (325).  `setAttribute` (386) has the `PhysicalFont` in hand
   and passes only its name on.  A corporate font embedded in a document is
   exactly the case the table cannot know.
8. **The discovery pass resolves nothing and drives the wrong machinery.**
   `fontsInUse()` walks the document with a `RunFontSelector` in DISCOVERY
   mode, before `processEmbeddings` and `populateFontMappings`
   (`WordprocessingMLPackage` 350-395, the 2026-08-19 analysis, "two passes
   ... a restructure of this method ... not doing the two-pass fix unless a
   bug report shows the config gap mattering").  Twelve DISCOVERY branches in
   the selector exist for it, every glyph check in that pass answers false,
   and numbering levels contribute `w:ascii` only (351).  Since 17.0.5 the
   second half of the two-pass fix exists: `registerUsedFont` declares to
   FOP, late, every font the conversion actually put in a `font-family`.
   With that, discovery needs no selection at all: the union of the four
   slots of every `w:rFonts` in use (document, headers, footers, notes,
   comments, the styles in use, numbering levels, `w:sym`, the theme fonts
   `themeFontLang` selects) is the input `populateFontMappings` wants, the
   DISCOVERY mode goes, and the FOP configuration is what the document
   names plus what the conversion reached.
9. **The two mappers disagree on precedence.**  `IdentityPlusMapper` prefers
   an installed font to the document's embedded one (`PhysicalFonts.get`
   first, `regularForms` after); `BestMatchingMapper` the reverse (embedded
   forms first, "bypass panose for these"); `Mapper.getBoldForm` prefers
   installed ("we could do this the other way around, or make it
   configurable", 296); `addMetricallyCompatibleSubstitute` defers to
   embedded (`isEmbedded`, 17.0.3).  One order, stated once (Decisions 2).
   Beside it: the face order "`regular`, `italic`, `bold`" (147-160) gives a
   family whose plain face is absent its *italic* before its bold, so
   upright text comes out italic (Franklin Gothic Demi, 2026-08-18);
   `Mapper.get(null)` throws (96); `getSubstituteFontXsltExtension` (146) is
   the iText-era family heuristic with no caller in the tree.
10. **Astral characters and the symbol names.**  A surrogate pair is
    glyph-checked as a `char` (1776, "doubt this works for high surrogate"):
    `GlyphCheck.hasChar(pf, char)` is always false for it, so without the
    `EmojiFont` property the emoji goes to `w:hAnsi` — and `glyphFallback`
    then skips it, since `FontFallback.needsCoverage` asks about scripts and
    an emoji is `COMMON` (only U+2190-U+2BFF are treated as symbols).
    Measured: `a😀b` in Calibri renders `[Carlito|a][Carlito|😀][Carlito|b]`,
    the emoji as notdef, where Noto Color Emoji or Segoe UI Emoji would do.
    The symbol-font test is case-sensitive (1380, `equals("Symbol")`) where
    font names are not (the Mapper lower-cases): measured, `w:hAnsi="symbol"`
    renders "abg" in Carlito, `"Symbol"` renders αβγ in DejaVu Serif.  The
    `hint=eastAsia` branches for U+02B0-U+04FF, U+F900-U+FAFF and
    U+FE30-U+FE6F call `fontAction(eastAsia)` with `eastAsia` possibly null,
    which the visitors turn into the *fallback* font (measured: Cyrillic
    with `w:hint="eastAsia"` and no `w:eastAsia` renders in Carlito, the
    default, not in the run's Arial); the table says `hAnsi`.
11. **The HTML pathway writes the machine's font, not the document's.**
    `getCssProperty` (2413) emits `font-family:'Carlito Regular'` for a run
    in Calibri — the *physical* name the server resolved — or nothing at all
    when it resolved none (`CSS_NULL`).  The reader's browser is not on the
    server: it has Calibri and not Carlito, or neither.  Word's HTML and
    every browser expect the document name first and a generic class last
    (`font-family:'Calibri','Carlito',sans-serif`; `FontFallback.classOf`
    knows the class).  A behaviour change for HTML users, so Decisions 1.
12. **Per-run cost and one inline per character run.**  Beyond the DOM per
    run (gap 2), the dispatch emits a new span at every range boundary even
    when the font is the same (measured: `a§bé` in one font as three
    inlines; the U+0000-U+007F range is re-entered after every exception
    character), and FOP kerns and letter-spaces *within* an inline, not
    across two (the 17.0.5 `defaultRange` comment).  `finish()` already
    post-processes the fragment; merging adjacent inlines with identical
    attributes there is cheap and shrinks the FO.
13. **Odds and ends, each small.**  `arabicNumbering` (2569-2578) runs only
    when `themeFontLang/@w:bidi` is exactly `ar-SA`, so `ar-EG`, `fa-IR`,
    `ur-PK` documents never get native digits ("rules below were inferred
    based on testing which always included w:bidi"); `symbolSetAttribute`
    (426) chooses the substitute from the first code point of the text ("this
    assumes that each char in textValue uses the same font"), where the
    Wingdings ranges are split across two substitute fonts by design
    (`getWDingsFont`, `getWDingsFont2`); Hangul Jamo with no `w:eastAsia`
    names "Gungsuh" outright (2007, "TODO what if not present?"); a null cs
    font name prints a stack trace to stderr (1455); `nullRPr` (247) sets the
    whole text in the default font with no range dispatch when the effective
    rPr has no `w:rFonts` at all; `spacePreserve` is instance state the
    `String` overload does not reset (1196; the 17.0.3 gotcha every
    generated-text caller has to know); the per-character `TODO` warning in
    the symbol block (2126-2134) is the only WARN in a document's conversion
    that fires per glyph.

14. **The fidelity work has only ever measured one of the two mappers.**
    The harness renders through `Docx4J.load` and `Docx4J.toFO` without
    `setFontMapper`, so every corpus score, every ledger entry and every
    "measured against Word's PDF" substitute of 17.0.5-17.1.1 is an
    `IdentityPlusMapper` number (the default, `WordprocessingMLPackage`
    453); the export-fo tests are the same (bar `PdfMultipleThreads`).  The
    fidelity commits placed their logic accordingly: the metric-compatible
    table and the `w:altName` pass in `Mapper`, shared by both (and the
    table's unconditional `put` overwrites a panose answer for those
    families, so the mappers agree there); the coverage pass, line metrics,
    kerning and the late FOP declaration in `RunFontSelector`, mapper-
    independent; and the class-based pass with its measured exclusions
    withheld from `BestMatchingMapper` (a66cc1416: "BestMatchingMapper is
    unchanged"; `wantsClassBasedSubstitutes` false).  So a BestMatchingMapper
    user gets, for any family outside the metric table, a panose match or a
    `FontSubstitutions.xml` entry and never the class fallback, and how far
    that is behind the default mapper on the corpus is unknown — while the
    samples still comment it "Good for Linux (and OSX?)"
    (`ConvertOutPDFviaXSLFO` 162), a claim the fidelity work has quietly
    overtaken on a box with the croscore, crosextra and Noto fonts.
    `FontSubstitutions.xml`, once BestMatchingMapper's private data, now
    serves `FontFallback` too, with different semantics (class and
    candidate order there; substitute search here).  Phase 0c scores the
    corpus with BestMatchingMapper, on this box and on the font sets a
    deployment actually has (Decisions 7), before phase 3 touches either.

## What the code's own comments record (read 2026-09-12)

- **The class javadoc**: the [MS-OI29500] 17.3.2.26 table, "validated against
  it on 2026-08-19", and the Tristan Davis description of `themeFontLang`
  (classify, then one theme font per bucket by language).  Both accurate;
  the language step is where gap 4 lives.
- **"Known deliberate divergences from that table"** (1707-1722): Indic,
  Thai, Lao, Myanmar, Khmer use cs (issues 666, 622); U+2190-U+2BFF
  glyph-checks and substitutes (observed Word 2016); Hebrew/Arabic use a
  Times New Roman heuristic "where the table says ascii"; `hint=eastAsia`
  on U+03D0-U+03FF and U+27C0-U+2E7F "contrived cases, kept for continuity";
  and **"the table's preamble rule (where eastAsia is 'Times New Roman' and
  ascii equals hAnsi, use ascii) is not implemented"**.  The last is stale:
  `fontSelector` implements it at 1594-1597 (measured: CJK text with
  `w:eastAsia="Times New Roman" w:ascii="Arial" w:hAnsi="Arial"` is
  dispatched to Arial, then to a CJK face by glyph coverage).
- **"The ASCII font formats all characters in the ASCII range (0-127)"**
  (javadoc, and 1782-1787) — accurate; the space (gap 5) is the exception the
  code makes to it.
- **"If the run has the cs element ... or the rtl element ... then the cs
  (or cstheme if defined) font is used, regardless of the Unicode character
  values"** (1439-1443) — the implementer notes' rule; the code's test is
  presence (gap 3).
- **"TODO use effective rPr, but don't inherit theme val"** (1338) — the
  first half is CR-015's Layering decision; the second is moot: the
  effective rPr *should* carry the theme attributes (`StyleUtil.apply(RFonts)`
  keeps them, "theme trumps non theme"), since only here, with
  `themeFontLang` and the theme part, can they be resolved per document.
- **"TODO: At present, we set a font on each and every span; if we set a
  default on eg body, this wouldn't be necessary"** (254) — settled the
  other way by 17.0.5: the line height, `HINT_FONT`, the kerned and
  no-ligature twins and the coverage pass all live on the span, and
  `AbstractFldSimpleWriter`'s 17.0.3 note records why a block font is only
  an approximation.  The comment closes.
- **"pStyle.getRPr() should inherit from basedOn"** (1309, commented-out
  code) and **"Do we need boolean major?? Can work that out from pStyle"**
  (1327) — superseded: the chain is `getEffectiveRPr(styleId)`'s, and
  major/minor is what the theme attribute *is* (`majorHAnsi` vs
  `minorHAnsi`).  Phase 1 removes both with the code they annotate.
- **"or the character set of the eastAsia (or eastAsiaTheme if defined)
  font is Chinese5 or GB2312" TODO** (1894, 1902; U+0100-U+02AF and
  U+1E00-U+1EFF under `hint=eastAsia`) — the table's rule needs the font's
  OS/2 code-page bits, which FOP's `OpenFont` reads; kept as a TODO with
  that pointer (no probe: needs the Chinese fonts).
- **"TODO: doubt this works for high surrogate"** (1776) — confirmed (gap
  10); **"quick n dirty" emoji check** (1767) tests `c=='\uD83D' ||
  c=='\uD83D' || c=='\uD83E'` — the duplicate is a typo for `\uD83C`.
- **"checked with russian/cyrillic"** (1924, U+02B0-U+04FF to hAnsi) —
  accurate, table agrees.
- **"Word doesn't use Arial Unicode MS ... It often uses TNR, so the
  following is good enough"** (1941-1946) — gap 6, probe P3.
- **"Word is generally unable to substitute a suitable font"** for the
  private-use area (2206), and the F000-F0FF symbol expectation — accurate;
  `XsltFOFunctions.symbolLabelFallback` (17.1.0) covers the label case.
- **"compare empty, which RunFontSelectorChinese2Test is sensitive to"**
  (1371) and `StyleUtil`'s "if we don't return null, it creates empty
  rFonts element; see comment at line 401 of RunFontSelector" — the two
  comments point at each other across a line number that has moved; the
  behaviour they protect (an rPr whose only rFonts content is `w:hint` still
  dispatches by range) is right and is pinned by that test.  Phase 1 routes
  the no-rFonts case through the dispatch too (default font in all four
  slots), so the distinction stops mattering.
- **"Yes, Times New Roman is still buried in Word 2007"** (202) and **"No
  theme part - default to Calibri"** (220) — the built-in defaults when
  docDefaults name no font, or name a theme font with no theme part.
  CR-015 P6 measured the default *size*; the font is probe P6 here.
- **"Rules below were inferred based on testing which always included
  w:bidi"** (2569, `arabicNumbering`) — accurate as far as it goes; the
  `ar-SA` gate is the gap (13); Word's rule is its Numeral option, which no
  probe on the share can vary, so it stays as documented with the gate
  widened to any Arabic-script `bidi` language.
- **`IdentityPlusMapper`: "Brush Script MT vs brush script mt italic"**
  (148) — the reason italic is tried; the order is the gap (9).
- **`Mapper.getBoldForm`: "prefer the physical font if present on the
  system (this potentially helps if we need a glyph which is not embedded)
  ... we could do this the other way around, or make it configurable"**
  (294-296) — Decisions 2.
- **"Held back: the Nokia Pure family"** (`Mapper` 390-410) — the measured
  case for the `w:altName` chain (gap 1); its conclusion "without a
  measurement of Nokia Pure's own advances there is nothing to choose the
  substitute by" stands; the *class* of the altName's fontTable entry is
  what P5 (f) asks about.
- **"Deliberately conservative: a condensed face (Arial Narrow) is left
  unmapped"** and `FontFallback.leaveUnmapped` (Lato, PostScript names) —
  measured, kept.
- **`BestMatchingMapper`: "We rely on this [panose] almost exclusively"**
  (346), **"TODO - only do this for latin fonts!"** (355), **"Exclude non
  latin fonts from Panose match eg Segoe UI matching ... TAMu_Kalyani.ttf"**
  (58) — still true, and cheap to close: a panose candidate that cannot
  draw Basic Latin is no match for a Latin document font (phase 3).
  **"Temp workaround for Calibri to use Carlito Regular"** (298) — behind
  exact and embedded matches since 17.0.3; `addMetricallyCompatibleSubstitutes`
  does the same for both mappers, so the workaround is redundant.
  **"TODO - set this up properly!"** (245, `lastSeenNumberOfPhysicalFonts`) —
  the docx4all re-population guard; harmless.
- **`PhysicalFonts`: "So we only get the first [triplet]"** (450) — the
  family-name triplets (nameID 1 and 16) are never registered, so a document
  asking for a typographic family ("Franklin Gothic" for framd.ttf) misses
  (2026-08-18).  Phase 3, optional: register a family name as an alias when
  nothing is registered under it and the face is the plainest
  (`styleRank` 0).
- **`GlyphCheck.hasChar(String, char)`**: "Couldn't get font" — blind to
  embedded and substituted fonts (17.0.3 lesson); still public.  Phase 5
  deprecates it in favour of the `PhysicalFont` overloads.
- **`WordprocessingMLPackage.setFontMapper`'s 2026-08-19 analysis** —
  accurate, and its "not unless a bug report shows the config gap mattering"
  is overtaken by gap 8: the restructure is now a simplification, not a
  cost.

### Are the comments accurate?  Verification status

| # | claim (source) | code does (inspected / measured 2026-09-12) | Word evidence | status |
|---|---|---|---|---|
| 1 | The preamble rule (eastAsia TNR, ascii = hAnsi) "is not implemented" (1718) | implemented at 1594-1597 | table | COMMENT STALE (phase 2 fixes the comment) |
| 2 | `w:cs`/`w:rtl` present means the cs font (1439) | presence, not value; `w:val="0"` takes the cs path | ECMA 17.3.2.7/17.3.2.30 (a value); probe P1 | WRONG (phase 1) |
| 3 | `themeFontLang` picks one theme font per bucket by language (javadoc) | via `LanguageTagToScriptMapping`, whose substring test sends `et`/`mn`/`wo` to Ethi/Beng | probe P4 (expect Calibri for `et-EE`) | MECHANISM RIGHT, MAPPING WRONG (phase 1) |
| 4 | The ASCII font formats 0-127 (javadoc) | except U+0020, which joins the current span | probe P2 (the space's width) | PROBE |
| 5 | Hebrew/Arabic without cs: "Word ... often uses TNR" (1944); table says ascii | Times New Roman if it has the glyph, else nothing (the fallback) | probe P3 (ascii, hAnsi, cs, TNR, or the theme's bidi font) | PROBE |
| 6 | Emoji check "doubt this works for high surrogate" (1776) | confirmed: always false; and no coverage pass for astral | Word uses Segoe UI Emoji (P8 (c)) | CONFIRMED gap (phase 2) |
| 7 | Symbol fonts recognised by name (1380) | case-sensitive | font names are case-insensitive (Word; `Mapper.get`); P8 (a) | GAP (phase 2) |
| 8 | Default font: TNR when docDefaults name none; Calibri when a theme font has no theme part (202, 220) | as stated, and computed before `themeFontLang` is known | probe P6 | PROBE; the ordering is a CODE gap (phase 1) |
| 9 | `hint=eastAsia` sends U+02B0-U+04FF to eastAsia (1920) | `fontAction(null)` when the run has no eastAsia font, i.e. the fallback | table: eastAsia "if defined", else hAnsi | GAP (phase 2) |
| 10 | Space and shared characters follow the previous font in the coverage pass (`isShared`, 17.1.0) | as stated | right for shaping; width per P2 | PROBE (same answer as row 4) |
| 11 | "prefer the physical font if present" for bold forms (`Mapper` 294) vs the regular form's two orders | inconsistent between the mappers | no probe can decide it (needs a font absent from the VM) | DECISION 2 |
| 12 | Face order regular, italic, bold (`IdentityPlusMapper` 147) | as stated; italic wins over bold | a missing plain face: bold upright is nearer than italic | GAP (phase 3) |
| 13 | An absent face falls to the document default's class or font (`FontFallback`, 106) | as stated; the fontTable's `w:family`/`w:panose1` unread | ledger4: "UI font, per character" (7320, 12630); probe P5 varies the fontTable | PROBE (phase 3) |
| 14 | A `w:b` run in Calibri Light is Carlito Bold | `renderedFace` takes the substitute's real bold | ledger4 M2: Word's line is one Calibri-Light font object; probe P7 | PROBE (phase 3) |
| 15 | Embedded fonts' line metrics (17.0.5 `applyLineHeight`) | by name in `PhysicalFonts`: the table or the 1.2 fallback, never the embedded file | Word uses the embedded file's metrics (§2.7 of CR-001) | GAP (phase 4; unit test on `FontEmbedded.docx`) |
| 16 | Discovery "can't resolve any font" (setFontMapper) | as stated; DISCOVERY mode and 12 branches exist for it | n/a | CODE CONFIRMED; phase 4 removes the need |
| 17 | `arabicNumbering` for `bidi="ar-SA"` only (2578) | as stated | Word: the Numeral option, any Arabic-script language | GAP, no probe (phase 2 widens the gate) |
| 18 | `symbolSetAttribute` "assumes each char uses the same font" (426) | first code point decides | the two Wingdings substitutes split the ranges | GAP (phase 2) |
| 19 | Family-name triplets not registered (`PhysicalFonts` 450) | as stated | Word resolves both names | OPTIONAL (phase 3) |
| 20 | Panose can match a Tamil face for Segoe UI (`BestMatchingMapper` 58) | still can | n/a | GAP (phase 3) |

**Probe set `fonts-*` (one docx per line, generated by the harness's
`Corpus.java`, copied with `corpus.txt` to `$S/corpus/`,
`S="/home/jharrop/vbShares/Office 2016/fidelity"`; Jason runs Word; goldens
back to `$S/goldens-nofields`).  Each probe sets the same sixty-character
sentence in the fonts under test, so the golden's text layer (widths,
`pdffonts` names) decides; every font named is on a Windows 10 / Office
2016 box (Calibri, Calibri Light, Arial, Tahoma, Courier New, Times New
Roman, MS Gothic, Nyala, Segoe UI Emoji, Symbol, Wingdings):**

| id | document | answers |
|---|---|---|
| P1 `fonts-cs-off` | docDefaults Calibri 11 (`w:ascii`/`w:hAnsi`) with `w:cs="Courier New"`; character style CsOn with `<w:cs/>`.  Runs of the sentence: (a) `<w:cs/>` (control: Courier New); (b) `<w:cs w:val="0"/>`; (c) `w:rStyle="CsOn"` and direct `<w:cs w:val="0"/>`; (d) `<w:rtl w:val="0"/>`; (e) `<w:rtl/>` on Latin text; (f) `<w:cs/>` on Arabic text (control) | which of (b)-(e) are Calibri (row 2) |
| P2 `fonts-space-cjk` | ascii/hAnsi Calibri 12, eastAsia MS Gothic: (a) `日本 語 abc 語` and the same with `w:hint="eastAsia"`; (b) `日本2013年、語` (digits, an ideographic comma); (c) `abc 日本` (a Latin word's trailing space before CJK) | the space's advance (2.7pt Calibri, 6pt MS Gothic at 12pt); the digits' font (row 4, 10) |
| P3 `fonts-hebrew-no-cs` | `w:ascii="Arial" w:hAnsi="Calibri" w:cs="Tahoma"`, theme bidi Times New Roman.  Hebrew text (a) with no `w:cs`/`w:rtl`; (b) `<w:rtl/>`; (c) `<w:cs/>`; Arabic text (d) none, (e) `<w:cs/>` | `pdffonts`: which face draws (a) and (d) (row 5) |
| P4 `fonts-theme-lang` | `<w:themeFontLang w:val="et-EE"/>`; docDefaults `minorHAnsi`; theme minor Latin Calibri with Office's script list (Ethi Nyala); two paragraphs of the sentence, one with `w:lang="et-EE"` on its runs | Calibri, not Nyala (row 3) |
| P5 `fonts-unresolvable` | docDefaults Times New Roman 11.  Runs in a made-up family "Docx4j Probe Sans" whose fontTable entry is (a) absent; (b) `w:family="swiss"` with Arial's `w:panose1` and `w:charset="00"`; (c) `w:family="roman"` with Times's panose; (d) `w:altName="Arial"`; (e) `w:altName="Docx4j Probe Serif"`, itself absent and without an entry; (f) as (e) but the alternate has an entry with `w:family="swiss"`; (g) as (b) with Cyrillic and Greek text | `pdffonts` per run: the docDefault (Times), the UI font (Calibri/Segoe UI), a panose match (Arial), the altName, or its class (row 13); whether (g) changes face per character |
| P6 `fonts-missing-slots` | (a) docDefaults with no `w:rFonts`, no theme part; (b) docDefaults `w:asciiTheme="minorHAnsi"` *and* `w:ascii="Arial"`, no theme part; (c) `w:ascii="Arial"` only, text with é and ü (the hAnsi range); (d) `w:hAnsi="Arial"` only, ASCII text | Word's built-in defaults and its fallback per empty slot (row 8) |
| P7 `fonts-light-bold` | (a) Calibri Light 11; (b) Calibri 11; (c) Calibri Light with `<w:b/>`; (d) Calibri with `<w:b/>`; same sentence | (a)/(b) widths (ledger: expect 0.975); (c)'s `pdffonts` face and advances (row 14) |
| P8 `fonts-symbol-and-emoji` | (a) `w:ascii="symbol" w:hAnsi="symbol"` (lower case), text `abg`; (b) `wingdings`, U+F0FC; (c) `a😀b` in Calibri; (d) `a→b` and `a❑b` in Calibri (the U+2190-U+2BFF substitution, the existing behaviour) | (a) αβγ or abg (row 7); (c) Segoe UI Emoji (row 6); (d) Segoe UI Symbol |

**Probe status (2026-09-12):** the eight probes are in `Corpus.java` (after
the CR-015 set; helpers `fontsPara`, `rFonts`, `allFour`, `csOn`/`csOff`/
`rtlOn`/`rtlOff`, `docDefaultsRFonts`, `fontTable`/`fontEntry`, `themePart`,
`themeFontLang`), generated, and copied with `corpus.txt` (107 ids) to
`$S/corpus/`.  docx4j's own answers today, read from the harness render's
FO (`font-family` per inline) and PDF:

| probe | docx4j today |
|---|---|
| P1 | (a) to (e) **all in Cousine** (the cs font): `w:cs w:val="0"`, the character-style override and `w:rtl w:val="0"` are read as on, and `w:rtl` on Latin text takes the cs font too; (f) the Arabic in DejaVu Sans Mono (Cousine has no Arabic; the coverage pass found a mono that has); (g) Carlito |
| P2 | (a) and (b) one span: `日本 語 ` x8 wholly in the East Asian substitute (Source Han Sans), spaces included; (c) the digits in Carlito, the ideographic comma U+3001 in **Noto Sans Mongolian** (an uncovered shared character is sent to the first covering font by name, not to the substitute its CJK neighbours got); (d) each `日本 ` with its trailing space in Source Han Sans, `def abc ` in Carlito |
| P3 | (a) Hebrew with nothing: **Tinos** (the Times New Roman heuristic), split at the ASCII comma and full stop into Liberation Sans; (b) `w:rtl` and (c) `w:cs`: Liberation Serif (the cs font); (d) Arabic with nothing: the span is left in Liberation Sans and **renders as `#` boxes** (FOP's not-found glyph; the coverage pass did not rescue it, though (e) shows it can: `w:cs` gives Noto Kufi Arabic); (f) Liberation Sans |
| P4 | (a) and (b) Carlito - by accident: the run resolved to **Nyala** (sixteen "Font 'Nyala' is not mapped" warnings) and fell back to the default font, which is the theme's Carlito; on a box with Nyala the text is set in it |
| P5 | (a), (b), (c), (e), (f) all **Liberation Serif**, the document default: the fontTable's `w:family` and `w:panose1` are unread and the made-up name has no class; (d) `w:altName="Arial"` gives Arimo; (g) Cyrillic and Greek in Liberation Serif; (h) Liberation Serif |
| P6 | (a) Tinos (Times New Roman, the built-in default); (b) theme references *and* an explicit `w:ascii`, no theme part: **Tinos** - `StyleUtil.apply(RFonts)` keeps the theme attribute and drops the explicit name ("theme trumps non theme"), and with no theme part the theme attribute resolves to nothing; (c) `w:ascii` only with Latin-1 text: `caf` in Liberation Sans, then `é ` and **`über fa`, `çade na`** in Tinos - after a Latin-1 character the dispatch sets the current range to U+0000-U+007F, so the ASCII letters that follow join the hAnsi span instead of returning to `w:ascii` (a dispatch bug of its own, invisible while ascii and hAnsi name the same font); (d) `w:hAnsi` only: Tinos; (e) Liberation Sans |
| P7 | all four in Carlito; (c) and (d) both in Carlito Bold (`pdffonts`): Calibri Light's `w:b` takes the substitute's real bold |
| P8 | (a) `symbol` lower case: **`abgdpw` in Carlito** (not the symbol path); (b) `wingdings` lower case: the private-use code points left in Carlito; (c) the emoji in Carlito (notdef); (d) `→` in Carlito (it has it), `❑` and `✔` in Noto Sans Symbols 2; (e) `Symbol`: αβγδπω in DejaVu Serif; (f) `Wingdings`: ✓🗹▪ in Noto Sans Symbols 2 |

Three things the render found that the review had not: the Latin-1 range
resets the current range to ASCII (P6 (c)), an uncovered *shared*
character is sent to whichever installed font covers it first rather than
to its neighbours' substitute (P2 (c)), and a Hebrew/Arabic run without
`w:cs` can reach FOP with no covering font at all (P3 (d)).  All three are
phase 2's.  The theme+explicit case (P6 (b)) is a `StyleUtil.apply(RFonts)`
question - whether Word keeps the explicit name as the fallback for a theme
it cannot resolve - and its answer goes to CR-015's merge rule, not here.

Not probed: embedded fonts (the harness cannot embed; row 15 gets a unit
test on `docx4j-samples-docx4j/sample-docs/FontEmbedded.docx`, which embeds
its three fonts as obfuscated parts), the Chinese code-page rule (needs the fonts), the
Numeral option (a Word setting).

## Design

### One resolution, then script and glyph (phase 1)

`fontSelector(PPr, RPr, Text)` keeps its signature and its callers.  Inside,
the two-step walk (1282-1341) becomes one call,
`propertyResolver.getEffectiveRPr(rPr, pPr)` — the visitor's pPr carries the
pStyle it re-attached, the XSLT pathway's is the raw pPr, and
`paragraphStyleOf` reads either.  A new overload takes an rPr the caller has
already resolved (`fontSelector(PPr, RPr effectiveRPr, Text, boolean
resolved)`, or a small `Selection` parameter object — the phase decides which
reads better), so the visitor, which resolves every run since CR-015 phase
2, passes its answer and the selector resolves nothing.  `captureLineSpec`
keeps its per-pPr cache but takes `w:spacing` from the pPr it is given when
that is already effective.  What stays in the selector is what is per
character or per output: the theme lookups (`themeFontLang` is the
selector's, and phase 1 moves it above the default-font computation in the
constructor), the range dispatch, the caps transform, the line-metric
capture, the fragment post-processes.

`w:cs` and `w:rtl` are read with `isOn` (the `BooleanDefaultTrue` value,
default true when present without `w:val`).  `LanguageTagToScriptMapping`
becomes a `Map<String, String>` of exact tags (`Set.of` per script), with a
test that `et`, `mn` and `wo` map to nothing.  `nullRPr` and the
`rFonts==null` early return route through `unicodeRangeToFont` with the
default font in all four slots, so a CJK or Georgian run in a document with
no `w:rFonts` anywhere still gets the coverage pass.  The dead code and the
two superseded comments go.  Tests: `RunFontSelectorCsValueTest` (P1's
cases as assertions with mapped fonts), `LanguageTagToScriptMappingTest`,
`RunFontSelectorThemeLangTest` (Estonian, the constructor order),
`RunFontSelectorNoRFontsTest`.  Gate: the fonts tests, both test modules,
corpus zero-delta against `p4-tables` (the CR-015 end state) except the
documents the cs-value fix touches, each read.

### The character-range dispatch (phase 2)

- **Shared characters** per P2: if Word gives U+0020 the ASCII font (the
  table's reading), the dispatch stops joining a space to the current span
  (1802) and `isShared` in the coverage pass gives a shared character the
  *ASCII* slot's font when that font covers it, the preceding font only
  when it does not (shaping is unaffected: a space is not shaped).  If Word
  keeps it in the preceding font, the code stays and the comments say why.
- **The complex-script ranges without cs** per P3: the slot Word uses,
  implemented as a table row like the Indic ones, and the Times New Roman
  name goes.
- **Symbol fonts** by case-insensitive name, in `fontSelector` and
  `symbolSetAttribute`, and `symbolSetAttribute` chooses per code point
  where the two substitutes split a run (one inline per substitute, as the
  coverage pass already does).
- **Astral characters**: `hasGlyph` by code point (`GlyphCheck.hasCodepoint`),
  the emoji test on the code point's block (U+1F000-U+1FAFF and the
  Miscellaneous Symbols and Pictographs, not a surrogate value),
  `FontFallback.needsCoverage` true for any code point above U+FFFF and for
  U+2600-U+27BF, so the coverage pass finds Noto Color Emoji / Segoe UI
  Emoji / Symbola as it finds Noto Sans Georgian; the `EmojiFont` property
  stays as the first candidate.
- **`hint=eastAsia` with no eastAsia font** falls to `hAnsi` in every
  branch (a null `fontAction` is never emitted); Hangul Jamo likewise (no
  "Gungsuh").
- **`arabicNumbering`** gates on the `bidi` language's script (Arabic script:
  `ar`, `fa`, `ur`, `ps`, `ug`, `sd`, `ks`), not on `ar-SA`.
- **Span merging** in `finish()`: adjacent inlines with identical attributes
  and text-only content are merged before the coverage pass, so a Latin
  run with an exception character is one inline, and FOP kerns across it.
  Corpus gate: zero-delta expected; measured, not assumed.
- The stale divergence note (1718) and the `\uD83D` duplicate are fixed
  with the code around them.  Tests: one per bullet, each a document
  fragment and the expected `font-family` sequence, with fonts mapped
  explicitly (the `RunFontSelectorGeneralPunctuationTest` pattern:
  `getFontMappings().clear()` then `put`).

### The mapping order (phase 3)

One precedence, written in `Mapper`'s javadoc and implemented once as a
template both mappers call, the mapper-specific step (identity, or panose
then `FontSubstitutions.xml`) in the middle:

1. the document's embedded form or the installed font, in the order Decisions
   2 fixes (the same order for regular, bold, italic and bold-italic);
2. the mapper's own step;
3. `addMetricallyCompatibleSubstitutes` (both mappers; the Calibri workaround
   in `BestMatchingMapper` goes);
4. `addAltNameSubstitutes`, following the chain to the alternate's alternate
   and, where the chain ends unresolved, to the *class* of the last entry
   that has one (P5 (e)/(f));
5. `addClassBasedSubstitutes` for both mappers (the `wantsClassBasedSubstitutes`
   opt-out stays available), using the fontTable's `w:family` and `w:panose1`
   before the name heuristic (P5 (b)/(c));
6. the unresolvable face, per P5 (a): whatever Word does — the document
   default's class as now, or the UI font — implemented in `FontFallback` so
   that both the mapper and the selector's last resort agree, and the line
   box follows the face chosen (`WordLineMetrics` already sizes from the
   document font when it knows it; for an unresolvable face it must size
   from the substitute, which is the ledger's 1.2217-against-1.150 case).

Beside it: the face order regular, bold, italic, bold italic; `Mapper.get`
tolerates null; a `Mapper.hasBoldFace(documentFontName)` /
`hasItalicFace` from `MicrosoftFontsRegistry` (Calibri Light, Segoe UI
Light and the other single-weight families have no `<bold>` entry) and the
sibling check, which `FopConfigUtil.renderedFace` consults (the one out-of-
scope call: declare the substitute with `simulate-style` rather than its
real bold when the *document* font has none), per P7; panose candidates
that cannot draw Basic Latin are skipped for a Latin document font; the
family-name alias, if cheap.  Tests: `MapperPrecedenceTest` (a fake
`PhysicalFonts` entry set, both mappers, same answers), `FaceOrderTest`,
`NoBoldFaceTest`, `AltNameChainTest`; corpus gate read document by
document, since this phase moves line breaks on the batch-42 population
(11741, 8371, 13743, 2065, 12630, 9919, 7320 are the ledger's test set).

### Discovery, cost, blind spots, logging (phase 4)

`fontsInUse()` becomes a walk for names: the four slots of every `w:rFonts`
on the runs, paragraph marks and styles in use (headers, footers, footnotes,
endnotes, comments, text boxes, numbering levels — all four slots, not
`w:ascii` alone), each theme reference resolved through the theme part for
the document's `themeFontLang` (the same code the selector uses, extracted
as `RunFontSelector.documentFontsOf(RPr)`), `w:sym` fonts, the default
font.  It needs no `RunFontSelector` and no glyph check; the DISCOVERY mode
and its twelve branches go (kept as a deprecated no-op constructor argument
for `docx4j-docx-anon`, whose `ScrambleText` runs the selector in that mode
over one character to learn which document font it resolves to, so that
the replacement it scrambles in has a glyph there — it gets a public
`documentFontFor(pPr, rPr, codePoint)` that answers without a visitor).  The FOP configuration is then the document's names through the
Mapper plus `registerLastResortFallback`, as 17.0.5 intended; a gate script
diffs the generated FOP configuration before and after over the corpus (a
font declared that was not, or the reverse, is read).

Per-run cost: one scratch `Document` per selector (the callers `treeCopy`
or `importNode` the fragment anyway), measured before and after on the
311-page corpus document; the merge from phase 2 counts here too.

The embedded-font blind spots: `setAttribute` passes the `PhysicalFont` it
resolved into `applyLineHeight`, and `kernSpaces`, `applyScaling` and
`noLigatures` resolve their span's font through the Mapper's values (as
`XsltCommonFunctions.physicalFontNameOf` does), falling back to
`PhysicalFonts` by name; `WordLineMetrics.get(PhysicalFont)` then reads the
embedded file.  Test: `FontEmbedded.docx`'s line height for a run in an embedded font
equals the metrics `WordLineMetrics.readMetrics` gives for the de-obfuscated
file (`ObfuscatedFontPart.extract` writes it to the temp dir).

Logging: the stack trace at 1455 becomes a WARN once per font; the
per-glyph symbol-block WARN becomes once per (font, code point); the
`FontDiscoveryCharacterVisitor`'s "Got null" with a stack trace goes with
the visitor.

### API hygiene, and the HTML decision (phase 5)

- `spacePreserve` becomes a parameter of the private worker; the public
  `String` overload passes `false` (the XSLT pathway and the anonymiser
  pass a `String` for text they never want preserved; reviewed one by one),
  the `Text` overload its own.  The 17.0.3 gotcha ("pass generated text as a
  Text with no xml:space") is then a non-issue and its comments go.
- Deprecated: `RunFontActionType.DISCOVERY`, `GlyphCheck.hasChar(String,
  char)`, `Mapper.getSubstituteFontXsltExtension` (both forms),
  `PhysicalFonts.getPhysicalFont(OpcPackage, String)` (no caller; one
  commented-out reference in `XsltFOFunctions`),
  the `getFontMappings()`/`getPhysicalFonts()` live maps (already marked).
- `package-info.java` for `org.docx4j.fonts` describing the two steps and
  the precedence, and the properties the package reads
  (`docx4j.fonts.*`, `docx4j.convert.out.fo.kerning`/`ligatures`,
  `docx4j.MicrosoftWord.Numeral`, `docx4j.MicrosoftWindows.Region.Format.Numbers.NativeDigits`,
  `docx4j.fonts.RunFontSelector.EmojiFont`).
- **HTML `font-family`** per Decisions 1: `getCssProperty` emits the
  document font, then the physical font's family, then the generic class
  (`FontFallback.classOf`), and never `CSS_NULL` — a run in Calibri yields
  `font-family:'Calibri','Carlito',sans-serif` — behind
  `docx4j.convert.out.html.fontFamily` = `document` (new default) |
  `physical` (today).  `FieldFontTest`'s HTML half stops being
  environment-dependent.

## Layering

CR-015's rule, restated for this package: **which properties apply** is
`PropertyResolver`'s (phase 1 takes the second resolver out); **which of a
run's fonts formats each character, and which physical face draws it** is
this package's, in two steps that must not know about each other's
internals — selection asks the Mapper for a `PhysicalFont` and never
`PhysicalFonts` by name (the embedded-font rule, gaps 7 and 10); mapping
never looks at text (its one glyph check, the Basic Latin test for panose
candidates, is about the face, not the run); **how the face is declared to
FOP** (`FopConfigUtil`, out of scope) consumes the Mapper's answers
(`getRegularForm`, the bold/italic forms, `hasBoldFace`,
`getLastResortFallbacks`) and adds nothing of its own.  The output
representation — `fo:inline` attributes, CSS — is the two visitors' and the
`setAttribute`/`getCssProperty` pair, which is why the HTML decision is a
representation decision, not a mapping one.

## Plan

### Phase 0 — Unix line endings (preliminary; no code change) — DONE 2026-09-12 (d643638ad)

    sed -i 's/\r$//' docx4j-core/src/main/java/org/docx4j/fonts/{CJKToEnglish,IdentityPlusMapper,GlyphCheck}.java \
        docx4j-core/src/main/java/org/docx4j/fonts/substitutions/{FontSubstitutions,ObjectFactory}.java \
        docx4j-core/src/main/java/org/docx4j/fonts/microsoft/{MicrosoftFonts,MicrosoftFontsRegistry,ObjectFactory}.java \
        docx4j-core-tests/src/test/java/org/docx4j/fonts/RunFontSelector{Arabic,Russian1,Russian2}Test.java \
        docx4j-core/src/main/resources/org/docx4j/fonts/substitutions/FontSubstitutions.xsd \
        docx4j-core/src/main/resources/org/docx4j/fonts/microsoft/MicrosoftFonts.xsd
    git diff -w --stat     # empty apart from the header

One commit, nothing else in it; hash into `.git-blame-ignore-revs`;
`.gitattributes` gains `docx4j-core/src/main/java/org/docx4j/fonts/*.java`,
`.../fonts/substitutions/**`, `.../fonts/microsoft/**`,
`docx4j-core-tests/src/test/java/org/docx4j/fonts/*.java` and the two
`.xsd`.  Exit: no CR in those paths; `mvn -o -q -Dgpg.skip=true install -pl
docx4j-core -DskipTests` unchanged.

### Phase 0b — verification probes (no code change) — probes cut and on the share 2026-09-12; goldens pending

Add the eight `fonts-*` probes to `Corpus.java` (`Doc` has `documentDefaultRun`,
`font`, `addParagraphStyle`, the run customisers; new helpers: a character
style with an rPr customiser is there from CR-015, `fontTableEntry(name,
family, panose, altName)` edits `mdp().getFontTablePart()`, `themeFontLang`
edits the settings part, `noThemePart` removes the theme relationship),
`Fidelity generate`, copy the docx and `corpus.txt` to `$S/corpus/`, wait for
the Word run, copy goldens back to `docx4j-layout-fidelity/goldens/word/`,
read them (`pdffonts`, `pdftotext -bbox-layout`), update the table's status
column.  Phase 1 needs P1 and P4 only for its expected values (the ECMA
reading is implemented meanwhile); phase 2 waits for P2, P3 and P8; phase 3
for P5 and P7; phase 4 needs none.

### Phase 0c — score `BestMatchingMapper`, and the font environments (no docx4j code change)

The harness gains `-Dfidelity.fontMapper=identity|best` (a `setFontMapper`
before `toFO`; today's default stays `identity`) and a font-environment
switch: `-Dfidelity.fonts=all` (this box, today's baseline),
`-Dfidelity.fonts=jars` (`docx4j.fonts.discoverPhysicalFonts.enabled=false`,
so the only fonts are the ones docx4j's own jars ship: Liberation and
Symbol/DejaVu Serif/Noto Sans Symbols by default, croscore Arimo/Tinos/
Cousine and crosextra Carlito/Caladea when those two jars are on the
classpath), and `-Dfidelity.fonts=<dir>` (a directory of font files
standing in for a distribution's stock set, through
`PhysicalFonts.setRegex` or a private discovery path).  The stock sets are
*enumerated, not assumed*: a script runs `fc-list : family file` in the
official container images (`ubuntu:24.04`, `debian:12`, `fedora:40`,
`alpine`, `eclipse-temurin` — the headless images most deployments start
from, where the answer may well be no fonts at all) and on the desktop
variants (`ubuntu-desktop`, Fedora Workstation: DejaVu, Liberation, Noto,
Cantarell/Ubuntu and whatever else they carry), and copies the files into
per-distribution directories on this box, with the inventory recorded in
the CR.  Then the three corpora are scored in the matrix

| mapper | this box | jars only | each stock desktop set | each headless image |
|---|---|---|---|---|
| IdentityPlusMapper | `p4-tables` (baseline) | | | |
| BestMatchingMapper | | | | |

against `p4-tables`, with `-Dfidelity.hyphenate=false`, one detached run
per cell (about ten minutes each).  What the numbers answer: how far
BestMatchingMapper is behind IdentityPlusMapper today (row 14); whether
either mapper is usable on a box that has nothing but the jars, which is
the deployment the samples never mention; and which of the panose answers
are worth keeping when phase 3 merges the two precedence orders.  No code
in this package changes until the matrix is read.

### Phase 1 — one resolution; `w:cs`/`w:rtl` by value; the theme language; the default font

As designed.  Gate: the 27 fonts tests and both test modules green; corpus
scored against `p4-tables` with `-Dfidelity.hyphenate=false`, every changed
document read (the cs-value fix is the only expected mover; the Estonian
fix cannot show on this box).  Measure `fontSelector` per run before and
after (the scratch number is 23 µs).

### Phase 2 — the character-range dispatch

As designed, after P2, P3 and P8 are read.  Gate as phase 1; the span merge
is expected zero-delta and is scored on its own commit.

### Phase 3 — the mapping order

As designed, after P5 and P7 and after the phase 0c matrix is read.  Gate as
phase 1, plus the FOP-configuration diff, and scored with *both* mappers from
here on; the batch-42 documents read individually.  The measured substitutes
(Trebuchet MS, Cambria's Greek, a Light face) are *not* in this phase
(Decisions 3); the rules they need are.

### Phase 4 — discovery, cost, blind spots, logging

As designed.  Gate: FOP-configuration diff over the corpus (zero-delta
expected apart from fonts the late registration now declares), the
311-page timing, the `FontEmbedded.docx` test, both test modules.

### Phase 5 — API hygiene and the HTML decision

As designed; CHANGELOG entries for the HTML change and the deprecations.

## Decisions

1. **HTML `font-family`: the document font first, the physical family second,
   the generic class last; never empty** — recommended, as the new default,
   with a property to restore today's physical-only output.  Word's HTML,
   every browser stack and the reader's machine all argue for it; the one
   case for the physical name is HTML rendered *on the server* (a
   headless browser for PDF), which the property keeps.  Jason to confirm.
2. **Embedded versus installed: the installed font wins, for all four
   faces, in both mappers** — recommended: it is `IdentityPlusMapper`'s
   order today (the default mapper), `getBoldForm`'s, and the reason its
   comment gives ("a glyph which is not embedded" — a subset is usually
   smaller than the installed font); `BestMatchingMapper` changes.  No probe
   can decide it on a box that has the font.  The alternative (the embedded
   font wins, as the author's intended outlines) is one line either way.
3. **CR-001 batch 42's measured substitutes stay CR-001's.**  This CR settles
   the rules (no bold face; the unresolvable face; the altName chain) and
   cuts the probes those measurements also need; a substitute chosen by
   measuring advances against Word's PDF is fidelity work, scored on the
   corpus in CR-001's own batch, after this CR.
4. **The space after CJK, and the Hebrew/Arabic slot, are decided by P2 and
   P3**, not by the table's reading alone (the Indic ranges taught that the
   table and Word differ).
5. **`RunFontActionType.DISCOVERY` is deprecated, not removed**, for
   `docx4j-docx-anon` and user code; `fontsInUse()` keeps its signature and
   its meaning (document names).
6. **Order against CR-001**: batch 42 (fonts) waits for phase 3 of this CR;
   batch 41's harness items and batch 43 are independent of it.
7. **No decision on `BestMatchingMapper`'s future until it has been scored**
   (Jason, 2026-09-12).  Phase 0c measures how far behind IdentityPlusMapper
   it is on this box, and how both mappers fare with only the fonts docx4j
   ships and with the default English/European font sets of Ubuntu and the
   other popular distributions — the environments users actually convert
   in.  What follows from the matrix (keep it as the panose-first mapper and
   give it the shared passes; or recommend IdentityPlusMapper everywhere and
   keep BestMatchingMapper for compatibility; and what the samples and the
   Getting Started text should say) is decided then, not now.

## Risks (as written 2026-09-12)

- **Behaviour change in the FO output from phases 1-3**, intended (towards
  Word) but broad: every run with `w:cs w:val="0"`, every mixed CJK line
  (the space), every Hebrew run without `w:cs`, every unresolvable face.
  The corpus gate is the control, read document by document; the probes
  settle each rule before its code ships.
- **The unresolvable-face rule may contradict the ledger's reading** ("the
  UI font, per character") once the fontTable is varied — as CR-015's P5
  contradicted its TODO.  The CR's shape absorbs it: phase 3 implements what
  P5 says, and the design above names both candidates.
- **HTML users** see different `font-family` values (Decisions 1).  A
  property restores the old output; CHANGELOG says so.
- **The discovery restructure changes the FOP configuration** — fonts
  declared that were not (theme fonts for scripts the document never uses,
  numbering levels' other slots) cost FOP a font parse each at
  `FopFactory` build.  The configuration diff over the corpus measures it;
  the walk declares styles *in use* only, as today.
- **The Windows VM's fonts**: P4 needs Nyala, P7 Calibri Light, P8 Segoe UI
  Emoji and Symbol — all in a stock Windows 10 / Office 2016 install; P2's
  MS Gothic is in `msgothic.ttc`, present since Windows 7.  A missing font
  on the VM turns a probe into a substitution test of Word's own, which the
  golden's `pdffonts` would show.
- **Thread safety** is unchanged by this CR: one selector per conversion
  context, the Mapper's `ConcurrentHashMap`s, `GlyphCheck`'s cache;
  `PhysicalFonts`' static `HashMap` is written only during discovery (the
  Mapper static initialiser) and by `addPhysicalFont` callers, which is
  documented as not concurrent.  Not made worse; not fixed here.
- **Performance** is measured this time (phase 1 and 4), on the 311-page
  document CR-015 named and did not measure.
- **The mapper matrix is a lot of rendering** (two mappers x five or six
  font sets x three corpora, ten minutes a cell) and the stock font sets
  have to be enumerated from real images, not from memory.  Run as
  detached scripts; the headless images will mostly answer "no fonts",
  which is itself the finding (the jars are the whole font supply there),
  so those cells are cheap.
