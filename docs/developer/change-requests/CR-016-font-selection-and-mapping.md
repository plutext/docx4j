# CR-016: Font selection and mapping (`RunFontSelector`, `Mapper` and its subpackages) — line endings, one resolution, the character-range rules, the mapping order, discovery, cost, API

Status: IN PROGRESS (2026-09-12) — phases 0 (d643638ad), 0b (the eight probes
and their goldens, the verification table settled), 0c (the mapper matrix and
the font environments; the jar-discovery fix b1eb9e61e), 1 (one resolution,
`w:cs` by value, the theme language, the Office theme faces; b999477be), 2 (the
dispatch; 613c44b1c), 3 (the mapping order; fd4eb61b8) and 4 (discovery as a
names walk, one scratch Document, the embedded-font blind spots, logging;
gaps 16 and 17 found and fixed) done; 5 open.
Jason read the CR the same day and started the work.  The review below was written that day
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

15. **Only one font jar was ever discovered** (found by phase 0c, fixed at once
    since the matrix could not be run without it: b1eb9e61e).
    `PhysicalFonts.getFontUrls` used `ClassLoader.getResource("fonts")`, which
    answers with one classpath root, so of `docx4j-export-fo-fonts-croscore`
    and `-crosextra` - both under `fonts/` - only the jar first on the
    classpath was walked.  Measured with system fonts off: 15 physical fonts
    (Carlito, Caladea, the symbol jar) and not one of Arimo, Tinos or Cousine;
    27 after the fix.  On the deployments that matter most this was the whole
    font supply: the stock `ubuntu:24.04`, `debian:12`, `fedora:41` and
    `alpine:3.20` images ship **no font files at all**, and `eclipse-temurin:21`
    eight DejaVu files (Sans, Sans Mono, Serif; no italics) - so Times New
    Roman, Arial and Courier New had no metric substitute there.
    `JarFontDiscoveryTest` pins it.
16. **A document's alias made a family "known" for every later document in
    the JVM** (found by the phase 4 gate, in phase 3's code).
    `addWordDefaultSubstitutes` and `addAltNameSubstitutes` register the
    alias they resolve with `WordLineMetrics.registerAlias`, a static per-JVM
    map, so that the line box is the alias's; `isKnownFamily` asked
    `WordLineMetrics.hasTableEntry`, which answers through that map.  So once
    one document had mapped Vrinda to Calibri's clone, Vrinda was a "known"
    family for the next document, which therefore skipped the Word-default
    pass and left it unmapped.  Seen as three documents whose mapping differed
    between the before and after dumps of the FOP configuration (the same
    document maps Vrinda to Carlito in a fresh JVM); bisected to
    `15_pt-BR_num_tbl_4055` preceding `12_en-US_num_tbl_1426`.  Fixed in
    phase 4: `isKnownFamily` asks `WordLineMetrics.isTableFamily`, the table
    and its built-in aliases only.  `MapperPrecedenceTest` pins it.
17. **The no-bold alias was given to fonts Word does not have** (found by the
    phase 4 gate, in phase 3's rule).  `addNoBoldFaceAliases` took every family
    whose name ends in a weight word; a font Word itself could not find is
    substituted whole in Word - "EnBW DIN Pro Light" is Calibri, and its `w:b`
    Calibri Bold - so the alias (bold synthesised at the regular advances) is
    wrong for a Word-defaulted font.  Measured on the one corpus document in
    that font: 0.8441 with the alias, 0.8783 without.  Phase 3's own gate had
    not seen it because of gap 16: the harness converts each document twice in
    one JVM and the second pass, the one scored, had lost the Word-default
    mapping to the alias leak and fallen to the default font.  Fixed in phase
    4: the Word-default pass records what it mapped and the alias pass skips
    it; `MapperPrecedenceTest.aWordDefaultedFontKeepsTheSubstitutesBold`.
    Four corpus documents' mappings change (EnBW DIN Pro Light and Medium,
    Nokia Pure Headline Light and Text Light), one of them scored.

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
| 2 | `w:cs`/`w:rtl` present means the cs font (1439) | presence, not value; `w:val="0"` takes the cs path | ECMA 17.3.2.7/17.3.2.30 (a value); golden P1: (b) `w:cs w:val=0`, (c) the style's cs overridden, (d) `w:rtl w:val=0` all Carlito; (a) `w:cs` and (e) `w:rtl` on Latin text Courier New | WRONG (golden; phase 1) - a false value is off, and `w:rtl` alone does take the cs font |
| 3 | `themeFontLang` picks one theme font per bucket by language (javadoc) | via `LanguageTagToScriptMapping`, whose substring test sends `et`/`mn`/`wo` to Ethi/Beng | golden P4: Carlito on every line, with and without `w:lang` | MECHANISM RIGHT, MAPPING WRONG (golden; phase 1) |
| 4 | The ASCII font formats 0-127 (javadoc) | except U+0020, which joins the current span | golden P2: the space between two MS Gothic words is 2.64-2.88pt at 12pt, Carlito's 0.22em space, not MS Gothic's half em (6pt); the digits are Carlito, the ideographic comma MS Gothic | CONFIRMED (golden): the space takes `w:ascii` (phase 2) |
| 5 | Hebrew/Arabic without cs: "Word ... often uses TNR" (1944); table says ascii | Times New Roman if it has the glyph, else nothing (the fallback) | golden P3: Hebrew with nothing in **Liberation Sans, the `w:ascii` font**; `w:rtl` and `w:cs` in Liberation Serif (cs); Arabic with nothing in **Arial** and with `w:cs` in **Times New Roman** - Liberation Sans/Serif have no Arabic, and Word substitutes per script *within the class* (a sans for the sans, a serif for the serif) | WRONG (golden): the table's `ascii` is right, and coverage falls back by class (phase 2) |
| 6 | Emoji check "doubt this works for high surrogate" (1776) | confirmed: always false; and no coverage pass for astral | golden P8 (c): Segoe UI Emoji; (d) `→` in Carlito, `❑` `✔` in Segoe UI Symbol | CONFIRMED gap (golden; phase 2) |
| 7 | Symbol fonts recognised by name (1380) | case-sensitive | golden P8: `symbol` and `wingdings` in lower case draw SymbolMT and Wingdings exactly as the title-case controls | CONFIRMED gap (golden; phase 2) |
| 8 | Default font: TNR when docDefaults name none; Calibri when a theme font has no theme part (202, 220) | as stated, and computed before `themeFontLang` is known | golden P6: (a) no `w:rFonts` anywhere: Times New Roman; (b) theme references with an explicit name and no theme part: **Calibri** (the explicit `w:ascii` is not the fallback; Word supplies the Office theme, Calibri/Cambria - not Word 365's own Aptos); (c) an absent `w:hAnsi` slot: Times New Roman for the Latin-1 characters, and the ASCII letters between them back in the `w:ascii` font; (d) an absent `w:ascii`: Times New Roman | CONFIRMED (golden): both constants right, and "theme trumps explicit" right; the ordering is a CODE gap (phase 1); (c) also shows the Latin-1 range reset (phase 2) |
| 9 | `hint=eastAsia` sends U+02B0-U+04FF to eastAsia (1920) | `fontAction(null)` when the run has no eastAsia font, i.e. the fallback | table: eastAsia "if defined", else hAnsi; not probed | GAP (phase 2) |
| 10 | Space and shared characters follow the previous font in the coverage pass (`isShared`, 17.1.0) | as stated | golden P2: the space is the ascii font's | SETTLED with row 4 (phase 2); shaping is unaffected |
| 11 | "prefer the physical font if present" for bold forms (`Mapper` 294) vs the regular form's two orders | inconsistent between the mappers | no probe can decide it (needs a font absent from the VM) | DECISION 2 |
| 12 | Face order regular, italic, bold (`IdentityPlusMapper` 147) | as stated; italic wins over bold | a missing plain face: bold upright is nearer than italic | GAP (phase 3) |
| 13 | An absent face falls to the document default's class or font (`FontFallback`, 106) | as stated; the fontTable's `w:family`/`w:panose1` unread | golden P5 (docDefaults Liberation Serif): (a) no fontTable entry **Cambria**; (b) `w:family="swiss"` + Arial's panose **Calibri** (not Arial: panose is not matched, the family is); (c) `w:family="roman"` **Cambria**; (d) `w:altName="Arial"` Arial; (e) an entry with only an absent altName **Calibri**; (f) absent altName whose own entry says swiss: Calibri; (g) Cyrillic and Greek in Calibri, one face throughout; (h) Liberation Serif | SETTLED (golden): never the document default; `roman` and no-entry go to Word's default serif (Cambria), `swiss` and an entry without a family to its default sans (Calibri), an altName that resolves wins; per character only where the substitute lacks a script (P3) (phase 3) |
| 14 | A `w:b` run in Calibri Light is Carlito Bold | `renderedFace` takes the substitute's real bold | golden P7: (c) is drawn by the **Calibri-Light** font object (synthetic bold): the sentence is 378.55pt against 377.50 unbolded, +0.3%; Word's Calibri Light is 0.9875 of its Calibri (377.50 / 382.28); docx4j's Carlito Bold for (c) measures 388.70, +2.7% | CONFIRMED (golden): a family with no bold face is emboldened at its own advances (phase 3) |
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

**Goldens in (2026-09-12, Word run "done 8"; `docx4j-layout-fidelity/goldens/word/fonts-*.pdf`
and the manifest).**  Word's answers, read with `mutool draw -F stext` (the face
drawing each character) and `pdftotext -bbox-layout`, against docx4j's render
(`Fidelity compare`, line parity):

| probe | Word | parity today |
|---|---|---|
| P1 | (a) Courier New; (b) (c) (d) **Carlito**; (e) `w:rtl` on Latin text **Courier New**, set right to left; (f) Courier New; (g) Carlito | 46% |
| P2 | the CJK words in MS Gothic, the **space between them 2.64-2.88pt** (Carlito's 0.22em at 12pt; MS Gothic's would be 6.0); digits Carlito; `、` MS Gothic; (d) each `日本` in MS Gothic, its trailing space Carlito | 17% (docx4j's spaces happen to measure 2.7pt because Source Han Sans has a proportional space; with MS Gothic mapped they would be 6pt) |
| P3 | (a) Hebrew with nothing: **Liberation Sans** (`w:ascii`), width 321.5pt (docx4j's Tinos 303.1); (b) (c) Liberation Serif (docx4j the same, 284.6/286.1 both sides); (d) Arabic with nothing: **Arial**; (e) Arabic with `w:cs`: **Times New Roman** - the named faces have no Arabic, and Word's per-script substitute keeps the class | 29% |
| P4 | Carlito throughout | 100% |
| P5 | (a) Cambria; (b) Calibri; (c) Cambria; (d) Arial; (e) Calibri; (f) Calibri; (g) Calibri, one face for Cyrillic and Greek; (h) Liberation Serif.  Line boxes follow the face Word chose (12.94pt Cambria, 12.60 Calibri, 12.38 Arial, 12.22 Liberation Serif at 11pt) | 75% |
| P6 | (a) Times New Roman; (b) **Calibri** (the theme reference resolved against the Office theme, the explicit name unused); (c) `caf` Liberation Sans, `é ü` Times New Roman, `ber fa` **Liberation Sans**, ... - every ASCII letter returns to `w:ascii`; (d) Times New Roman; (e) Liberation Sans | 100% (the faces differ in (b) and (c), the line breaks do not) |
| P7 | (a) Calibri Light, sentence 377.50pt; (b) Calibri, 382.28 (Light = 0.9875 of regular); (c) **Calibri-Light**, 378.55 (synthetic bold, +0.3%); (d) Calibri Bold, 390.65 | 50% |
| P8 | (a) SymbolMT; (b) Wingdings; (c) Segoe UI Emoji; (d) `→` Carlito, `❑` `✔` Segoe UI Symbol; (e) (f) as (a) (b) | 17% (docx4j's Noto Sans Symbols 2 stands in for Segoe UI Symbol; the misses are (a), (b), (c)) |

What the goldens settle beyond the rows: Word's font for an unresolvable name
is **not** the document default and not "the UI font" as such - it is Word's
default serif or sans by the fontTable's `w:family` (Cambria/Calibri, the
Office theme pair, on a Word 365 whose own theme is Aptos), Cambria when
there is no entry at all, Calibri when there is an entry without a family;
panose is not consulted (Arial's exact panose still gave Calibri); an altName
that resolves wins, one that does not is ignored.  A document with no theme
part gets the Office theme's Calibri for its theme references, which is what
`RunFontSelector`'s "No theme part - default to Calibri" constant assumed and
what `StyleUtil.apply(RFonts)`'s "theme trumps explicit" requires - so P6 (b)
is a *selector* fix (resolve a theme reference to Calibri/Cambria when the
theme part is absent, at every level, not only for the document default), not
a merge-rule change.  Where the chosen face lacks a script, Word substitutes
per script within the class (Arial for a sans, Times New Roman for a serif),
which is `FontFallback.selectCovering`'s preference already.

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

### Phase 0b — verification probes (no code change) — DONE 2026-09-12 (goldens in, Word run "done 8", table settled)

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

### Phase 0c — score `BestMatchingMapper`, and the font environments — DONE 2026-09-12 (one code change after all: the jar-discovery fix, gap 15)

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

**The font environments, enumerated 2026-09-12** (`docker run` on the official
images; the desktop sets by installing the `fonts-*` packages
`ubuntu-desktop-minimal` / `task-gnome-desktop` would pull in, and Fedora's
`fonts` group, then copying `/usr/share/fonts` out to `~/fidelity-fonts/`):

| environment | font files | what is there (families) |
|---|---|---|
| `ubuntu:24.04`, `debian:12`, `fedora:41`, `alpine:3.20` (headless) | **0** | nothing: docx4j's jars are the whole supply |
| `eclipse-temurin:21` | 8 | DejaVu Sans, Sans Mono, Serif (regular and bold; no italics) |
| docx4j's jars alone (`-Dfidelity.fonts=jars`) | 27 | Arimo, Tinos, Cousine (croscore), Carlito, Caladea (crosextra), DejaVu Serif, Noto Sans Symbols, Noto Sans Symbols 2 (symbol); the Liberation jar is the alternative to croscore, not on this classpath |
| Ubuntu 24.04 desktop (11 packages: fonts-dejavu-core/-mono, -droid-fallback, -liberation, -liberation-sans-narrow, -noto-cjk/-color-emoji/-core/-mono, -ubuntu, -urw-base35) | 344 | Liberation Sans/Serif/Mono/Sans Narrow, DejaVu Sans/Serif/Mono, Noto Sans/Serif and ~150 per-script Noto faces, Noto Sans CJK, Ubuntu, URW base 35 (Nimbus Sans/Roman/Mono, C059, P052, URW Gothic, URW Bookman, Z003, D050000L), Droid Sans Fallback |
| Debian 12 desktop (12 packages: fonts-cantarell, -dejavu/-core/-extra, -droid-fallback, -liberation2, -noto-color-emoji, -noto-mono, -opensymbol, -quicksand, -symbola, -urw-base35) | 85 | Liberation Sans/Serif/Mono (2.x), DejaVu Sans/Serif/Mono (+Condensed, Light), Cantarell, URW base 35, Quicksand, Symbola, OpenSymbol, Noto Mono, Droid Sans Fallback; **no Noto Sans/Serif** |
| Fedora 41 Workstation (`fonts` group: 126 packages, mostly `default-fonts-<lang>` meta-packages) | 73 | Noto Sans/Serif and their CJK and per-script faces, Noto Sans Mono, Cantarell, STIX Two, Vazirmatn, Padauk, Jomolhari; **no Liberation, no DejaVu**, no URW |

Two things this says before any scoring: a Fedora desktop has neither of
the two families the class defaults lean on first (Liberation and DejaVu),
so its Latin fallbacks come from the jars or from Noto; and a Debian desktop
has no Noto Sans/Serif, so a document font whose only stand-in is Noto falls
through.  The stock desktops all carry the URW base 35 (Ubuntu and Debian) or
STIX (Fedora), which the measured substitutes (Century Gothic, Georgia, Book
Antiqua, Palatino, Arial Narrow) already use.

**The matrix, scored 2026-09-12** (mean line parity over the three corpora,
191 / 156 / 102 documents, against `p4-tables` = IdentityPlusMapper on this
box, 0.9051 / 0.8799 / 0.9162; one cell is about six minutes, not thirty):

| environment | IdentityPlusMapper | BestMatchingMapper |
|---|---|---|
| this box (1,246 physical fonts) | **0.9051 / 0.8799 / 0.9162** (baseline) | 0.8941 / 0.8634 / 0.8996 |
| docx4j's jars alone (27), the headless images | 0.8818 / 0.8597 / 0.9077 | 0.8682 / 0.8463 / 0.9005 |
| eclipse-temurin:21 (jars + 8 DejaVu) | 0.8826 / 0.8639 / 0.9087 | 0.8688 / 0.8500 / 0.9026 |
| Ubuntu 24.04 desktop (+ jars) | 0.9050 / 0.8796 / 0.9135 | 0.8906 / 0.8642 / 0.8973 |
| Debian 12 desktop (+ jars) | 0.9035 / 0.8798 / 0.9160 | 0.8907 / 0.8685 / 0.9115 |
| Fedora 41 Workstation (+ jars) | 0.8829 / 0.8608 / 0.9077 | 0.8680 / 0.8401 / 0.8895 |

What it says:

- **BestMatchingMapper is behind IdentityPlusMapper in every environment**, by
  0.010 to 0.020 of mean line parity on this box and on the desktops, and by
  0.014 to 0.021 on the bare sets - the panose step is a net loss against the
  identity-then-shared-passes order everywhere it was measured.  Decision 7
  now has its number.
- **An Ubuntu or Debian desktop reproduces this box's baseline** with
  IdentityPlusMapper (0.9050 / 0.8796 / 0.9135 and 0.9035 / 0.8798 / 0.9160):
  everything the fidelity work relies on is in Liberation, DejaVu, the URW
  base 35 and the jars, which both carry.
- **The jars alone, eclipse-temurin and Fedora Workstation sit about 0.02
  lower, and for one reason**: none of the three has Liberation Sans or DejaVu
  Sans (Fedora ships Noto and Cantarell; temurin's DejaVu is Sans/Serif/Mono
  regular and bold only), so the class defaults that follow Arimo/Tinos
  (`FontFallback.SANS_DEFAULTS` ...) and the measured substitutes that name
  DejaVu Sans (Verdana), Nimbus/URW faces (Century Gothic, Georgia, Palatino,
  Arial Narrow) and Noto Sans find nothing.  Where the jars are the whole
  supply the document falls to the metric clones or the default serif.  A
  deployment on a headless image therefore gets the jars-only column, and the
  thing to improve there is the jars' own coverage (Decisions 7 follow-up), not
  the mapper.
- **Named**: the jars-only and Fedora cells regress the same sixteen documents
  of corpus 1, and the five worst (0.90 -> 0.12 and a page over, 0.88 -> 0.11
  and a page over, 0.90 -> 0.46, 0.92 -> 0.48, 0.95 -> 0.53) all set text in
  **Arial Narrow**, which the baseline draws in Nimbus Sans Narrow (352 to 601
  inlines each) and the jars-only render in Tinos or Carlito - Arial Narrow is
  left unmapped by design where no condensed face is installed (see
  `FontFallback.isCondensed`), and a condensed face's lines overflow into pages
  when set in a normal one.  Century Gothic (URW Gothic, 294 inlines in
  14_es-MX_tbl_14140) and Georgia (P052, 270 in 16_es-ES_num_tbl_12308) are the
  same loss.  All three stand-ins are the **URW base 35** (ghostscript-fonts),
  which Ubuntu and Debian desktops carry and the jars, temurin and Fedora do
  not.  So the jars' coverage question for headless deployments is, first, a
  condensed sans, and then the URW faces the measured table already names
  (a licensing question as much as a packaging one: the 2017 URW base 35
  release is AGPL-3.0 with a font exception).
- The `p4-tables` baseline is reproducible: identity-ubuntu's real2 figure is
  0.8796 against 0.8799, within the scorer's noise.


against `p4-tables`, with `-Dfidelity.hyphenate=false`, one detached run
per cell (about ten minutes each).  What the numbers answer: how far
BestMatchingMapper is behind IdentityPlusMapper today (row 14); whether
either mapper is usable on a box that has nothing but the jars, which is
the deployment the samples never mention; and which of the panose answers
are worth keeping when phase 3 merges the two precedence orders.  No code
in this package changes until the matrix is read.

### Phase 1 — one resolution; `w:cs`/`w:rtl` by value; the theme language; the default font — DONE 2026-09-12

As designed, with these particulars: the FO visitor keeps the effective rPr
`handleRPr` already computes (`AbstractVisitorExporterGenerator.effectiveRPr`)
and passes it with the new `fontSelector(pPr, rPr, text, rPrIsEffective)`;
the XSLT pathway and every other caller resolve inside the selector with one
`getEffectiveRPr(rPr, pPr)`.  A `themeFont(STTheme)` helper answers every
theme reference (the theme part for the document's language, or the Office
theme's Calibri/Cambria where the document has no theme part - P6 (b)), and the
explicit attribute stands where a reference resolves to nothing.  Two things
learned in the doing: a run with no `w:rFonts` must not get the default font in
its *eastAsia* slot, since a Times New Roman there fires the table's preamble
rule and sets the whole run in one span (the synthesized rFonts names ascii and
hAnsi only); and a complex-script run whose cs font resolves to nothing used to
reach `Mapper.get(null)` and throw - it now falls through to the range
dispatch, and `Mapper.get` tolerates null.  `RunFontSelector.java` carried a
literal NUL byte in a string (the `fallbackFor` cache key), which made `grep`
treat the file as binary; it is a space now.  Tests: `FontsTestSupport` (a
package whose document fonts are names of our own mapped to the two Liberation
faces), `RunFontSelectorCsValueTest`, `LanguageTagToScriptMappingTest`,
`RunFontSelectorThemeLangTest` (Estonian; and the default font with
`ja-JP`), `RunFontSelectorNoThemePartTest`, `RunFontSelectorNoRFontsTest`;
the fonts package's 79 tests green.  Gate (2026-09-12): `docx4j-core-tests`
988 run, 0 failures (one run of `TemporaryImageCleanupTest` failed while the
matrix's scorer was writing temporary images beside it, and passed alone);
`docx4j-export-fo-tests` 600 run, 0 failures; the three corpora against
`p4-tables`: 0.9051 / 0.8799 / 0.9162 unchanged, **0 changed documents**
(no corpus document carries a false `w:cs`, an Estonian theme language or a
theme reference without a theme part where the line breaks would see it,
and the one-resolution change is equivalent by construction); probes:
`fonts-cs-off` 46% -> 92% (the remaining mismatch is the right-to-left
case's line order), the other seven and the five `styles-*` unchanged.
Per-run cost not re-measured (the DOM per run, phase 4's, is untouched).
Original gate text: the 27 fonts tests and both test modules green; corpus
scored against `p4-tables` with `-Dfidelity.hyphenate=false`, every changed
document read (the cs-value fix is the only expected mover; the Estonian
fix cannot show on this box).  Measure `fontSelector` per run before and
after (the scratch number is 23 µs).

### Phase 2 — the character-range dispatch — DONE 2026-09-12

As designed, with one structural change beyond it: the walk no longer tracks a
"current range" at all.  The table is one function, `fontFor(codePoint, hint,
langEastAsia, eastAsia, ascii, hAnsi, cs)`, and the walk asks it for every code
point and starts a new span only where the answer changes.  That is what the
span merge was for, and it also removes the range-reset bug (P6 (c)) at its
root: a character joins a span because it has the same font, never because it
fell in the same range as its predecessor; the `defaultRange`/`LISTED_RANGES`
gap machinery of 17.0.5 and the `setMustCreateNewFlag(true)` calls of the
hint branches go with it.  The answers from the goldens: U+0020 is ascii
(P2), U+0590-U+07BF is ascii and the Times New Roman heuristic is gone (P3),
an East Asian reference with no East Asian font is hAnsi in every branch
(never a null `fontAction`), the symbol-font names are matched case-
insensitively through `symbolFontName` (P8), an emoji is checked by code
point and is its own coverage group with Word's face first in the candidate
list (`FontFallback.isEmoji`, `EMOJI_GROUP`), a symbol-font run is cut where
the two Wingdings substitutes change (`symbolSegments`), and `arabicNumbering`
gates on any Arabic-script `bidi` language.  In the coverage pass, two rule
changes forced by the merge and by the goldens: a character the span's font
covers keeps it - a space included (P2: the space between two substituted
CJK words is the Latin font's; until 17.1.1 a covered shared character went
with the substitute before it) - and the whole-span shortcut applies only
where the font drew nothing, since a span now holds Latin beside CJK when
both share a document font; an uncovered shared character goes with its
neighbours' substitute (P2 (c), the ideographic comma).  `hasGlyph` asks by
code point.  Tests: `RunFontSelectorDispatchTest` (10: the table's answers,
the joins, the names, the groups, the gate), `RunFontSelectorDefaultRangeTest`
rewritten for joining by font, `RunFontSelectorIndicTest` for one span per
Indic word with the spaces in the ascii font.

What the corpus gate taught, in three iterations on the same day (each
scored on all three corpora):

1. Joining by font alone put a run's Greek inside its Latin span, nested by
   the coverage pass, and the Greek-in-Cambria document (8371) went from 68
   pages to 69; the Georgian documents moved either way.  Two causes, both
   in consumers of the span structure: `XsltFOFunctions.collectRunFonts`
   credited a span's font with its nested inlines' text when choosing the
   block's font (fixed: it counts the text an inline draws itself), and
   FOP's line stacking differs for a nested inline.  So a span is cut where
   the *script* changes between two non-shared characters as well as where
   the font does (`spanScript`; the CJK scripts one group), and the
   coverage pass emits its stretches as sibling spans cloned from the
   original rather than nested inside it - the structure the range cut used
   to produce.
2. Whether a covered shared character (a space, a dot leader) keeps the
   span's font or follows the substitute before it: the 17.1.0 rule sent it
   with the substitute, measured then on the Georgian document; the P2
   golden says the run's font.  The deciding measurement was Sylfaen's
   space in the Georgian golden: **0.250 em**, Tinos's (the document font's
   substitute) and not DejaVu Serif Condensed's 0.286 (the Georgian
   substitute's), so the run's font is right; and the Greek document's dot
   leaders, which are U+2026 (shared) after a Greek word, must stay in
   Caladea (Cambria's clone) rather than P052.  The rule is now: a covered
   character keeps the span's font; an uncovered shared one takes its
   neighbours' substitute.
3. With both, every corpus improved and nothing regressed: 0.9051 -> 0.9052
   (Georgian 425: 0.9396 -> 0.9664), 0.8799 -> 0.8804 (two documents),
   0.9162 -> 0.9179 (the Greek 8371: 0.4984 -> 0.6025 at 68 pages; 6864
   0.8611 -> 0.9167).  Probes unchanged (their misses are faces this box
   lacks, and the RTL text layer).  `docx4j-core-tests` and
   `docx4j-export-fo-tests` green on the final code.

### Phase 3 — the mapping order — DONE 2026-09-12

As designed, with the goldens' answers in it:

- **One precedence, in `Mapper.populateFontMappings`** (no longer abstract): the
  installed font by its name, else the document's embedded form (regular, bold,
  italic, bold italic), else the mapper's own `resolveDocumentFont` - the name
  variants for `IdentityPlusMapper` (regular, bold, italic, bold italic: bold
  before italic now), the panose match then `FontSubstitutions.xml` for
  `BestMatchingMapper` (whose Calibri workaround and `lastSeenNumberOfPhysicalFonts`
  re-check are gone, and whose panose match now skips a face that cannot draw
  Basic Latin - the 2009 TODO about Segoe UI matching a Tamil font).  Decision 2
  as recommended: the installed font wins over the embedded one in both.
- **The shared passes for both mappers** (`wantsClassBasedSubstitutes` is true
  for BestMatchingMapper now): the metric-compatible table, the `w:altName`
  chain (the alternate's alternate, a few hops, never a cycle), a face of the
  same class, and two new ones.
- **`addWordDefaultSubstitutes`**: for a family none of docx4j's tables know
  (`isKnownFamily`: MicrosoftFonts.xml, word-line-metrics, FontSubstitutions.xml,
  the name heuristic), Word's own answer per P5 - Cambria for `w:family="roman"`
  or no fontTable entry, Calibri for `swiss` or an entry naming no family (the
  altName chain looked through for a family), Courier New for `modern`; mapped
  to whatever the mapper maps that font to (Caladea, Carlito), and
  `WordLineMetrics` told the alias so the line box is Cambria's or Calibri's.
  A *known* family the machine merely lacks keeps the class-based substitute:
  Word on the author's machine had it, so its widths are the target, not
  Cambria's.
- **`addNoBoldFaceAliases`** (P7): a document font with no bold face of its own
  (`hasBoldFace`: the registry entry's `<bold>`, else a name ending in a weight
  word - Light, Semilight, Semibold, Black, Thin ...) is re-mapped to an alias
  of its physical font (`PhysicalFont.noBoldFaceAlias`, name `+nobold`,
  stripped by `PhysicalFonts.get` like the kerned and no-ligature suffixes)
  which reports no bold sibling, so `FopConfigUtil` declares it with
  `simulate-style` and FOP synthesises the bold at the regular advances, as
  Word does for Calibri Light (378.55pt against Carlito Bold's 388.70).  No
  change in `fonts/fop`: the alias goes through the existing
  `getBoldForm` path.  The HTML pathway strips the suffix.
- `Mapper.get(null)` tolerates null (phase 1).  Not done: the family-name
  alias in `PhysicalFonts` (optional; nothing in the corpus asked for it).

Tests: `MapperPrecedenceTest` (the installed font over the embedded in both
mappers, the embedded form where the machine lacks the font, the face order,
the altName chain and a cycle, Word's default per P5's cases through the
mapper with the line-box alias, the no-bold alias); `ClassBasedSubstituteTest`
now asserts both mappers take the passes.

Gate (2026-09-12), and what it taught: the first run found the no-bold alias
losing its line boxes wherever the FO layer had stacked a second suffix on its
name (`Tinos Regular+nobold+noliga`; `PhysicalFonts.get` stripped one), which
cost four Calibri Light and Sylfaen documents; every suffix is stripped now.
The BestMatchingMapper cells then showed its panose step, first in its order,
claiming Myriad and CorpoS for a legacy Indic face carrying Arial's panose
(the Basic-Latin check does not catch a font with glyphs *at* the Latin code
points) before the metric and class passes could map them as IdentityPlusMapper
does; so the panose match and FontSubstitutions.xml moved behind the measured
passes as `addMapperSubstitutes`, a hook `setFontMapper` calls before Word's
default.  Final numbers, three corpora: IdentityPlusMapper 0.9052 -> 0.9053,
0.8804 -> 0.8804, 0.9179 -> 0.9182, no document regressed (the Calibri Light
document 740 to Word's single page); **BestMatchingMapper 0.8941 -> 0.9008,
0.8634 -> 0.8761, 0.8996 -> 0.9174** against its own matrix cells, 25 documents
improved and one regressed - within 0.001 to 0.005 of IdentityPlusMapper now,
where the matrix had it 0.010 to 0.021 behind.  Probes: `fonts-light-bold` 50%
-> 83%, `fonts-unresolvable`'s worst offset 23pt -> 16pt (Cambria's line box).
`docx4j-core-tests` 1004/0, `docx4j-export-fo-tests` 600/0.  The measured substitutes
(Trebuchet MS, Cambria's Greek, a Light face) are *not* in this phase
(Decisions 3); the rules they need are.

### Phase 4 — discovery, cost, blind spots, logging — DONE 2026-09-12

As designed, with what the measurements added:

- **Discovery** (`MainDocumentPart.fontsInUse`) is the names walk: the
  `FontAndStyleFinder` collects every `w:rFonts` on runs, paragraph marks and
  `w:sdtPr`, and `w:sym`, over the body, headers, footers, notes and
  comments (one traversal, which also yields the styles in use); then the
  styles in use with their `basedOn` chain (table styles' `tblStylePr` run
  properties included), the document defaults, the numbering levels (all
  four slots), and the default font.  A `FontNames` collector resolves each
  rFonts through `RunFontSelector.documentFontsOf(rFonts, themePart,
  themeFontLang)` - the selector's own resolution, extracted as a static -
  and maps a CJK name to its English name as the old visitor did.
  `RunFontSelector.defaultFontOf` is the static form of `getDefaultFont`.
  The `FontDiscoveryCharacterVisitor` and the DISCOVERY branches of the
  selector are gone; `DISCOVERY` is `@Deprecated` (Decisions 5), and a
  selector in that mode produces no output but still tells its visitor each
  span's font, which is what `RunFontSelectorIndicTest` and `-KhmerTest` use
  it for.  `fontsInUse` no longer creates the `PropertyResolver` as a side
  effect (the walk needs none).
- **`documentFontFor(pPr, rPr, codePoint)`**: the resolution of
  `fontSelector` without a visitor - the symbol font by canonical name, the
  cs font where `w:cs`/`w:rtl` is on and names one, the preamble rule, else
  `fontFor`.  The body of `fontSelector` is now `effectiveRPr` + `rFontsOf` +
  `symbolRun` | `complexScriptFont` | `resolvedSlots` + `preambleRule` +
  `singleFont` | `unicodeRangeToFont`, and both entry points use the same
  helpers.  The cs-font and preamble cases go through the visitor
  (`singleFont`) as the dispatch does, instead of building their span
  directly; measured identical FO on the 311-page document (16,983,089
  bytes before and after, byte for byte).  `docx4j-docx-anon`'s
  `ScrambleText` calls it; it had been reading the font from a visitor it
  never gave the selector (a second `RunFontCharVisitorMinimal`), so its
  glyph check never had a font.  `RunFontSelectorDocumentFontForTest`.
- **The FOP configuration, before and after**, dumped for the 561 documents
  of the four corpora (`FopConfigDump`, a scratch tool: the discovered names
  with their mapping, and every declared triplet with its file): 273
  documents identical; 288 gained names and 240 gained declarations; **no
  document lost a name**.  What was gained is what the old pass could not
  see: `Times New Roman` in 123 documents, `Arial` in 62, `Calibri` in 37,
  `Cambria` 24, `MS Mincho` 22, `SimSun` 19, `Arial Unicode MS` 19 - the
  fonts of paragraph marks and runs with no `w:t` (an example document has
  its three Times New Roman runs there and nowhere else), of the eastAsia
  and cs slots, and of styles' `basedOn` chains.  The declarations are
  `Tinos`/`Arimo`/`Carlito`/`Caladea` faces in the same proportions.  FOP
  loads a declared font lazily (`LazyFont`: metrics read on first use), so a
  declaration the FO never names costs a configuration entry and nothing
  else; the corpora's render time did not move (see the gate).  Three
  documents whose *mapping* changed turned out to be gap 16, pre-existing and
  order-dependent; fixed, and the dump re-run over the 561 documents shows no name removed and
  no mapping changed other than the four the no-bold correction below makes.
- **Cost**: one scratch `Document` per selector (`scratchDocument`, any
  stale element dropped on entry).  The 311-page corpus document
  (`15_es-AR_sdt_num_tbl_12301`, five timed runs after a warm-up):
  `fontsInUse` 165 ms -> 8 ms median; `Docx4J.toFO` 4,502 ms -> 4,082 ms
  median (4,214 -> 3,650 min), the difference being mostly the discovery
  pass the mapper set-up no longer runs and the per-run `Document`.
- **The symbol run**: found while moving it onto the scratch document - a
  Wingdings run whose characters need both substitute faces appended its
  second span to the `Document` beside the first, which Xerces refuses
  (`HIERARCHY_REQUEST_ERR`), so phase 2's per-face split had never worked for
  a mixed run.  `symbolRun` builds the spans into a fragment.
  `SymbolRunSubstituteFacesTest` (in `docx4j-export-fo-tests`, where the
  symbol jar is on the classpath) renders Wingdings `a`-`z` through the
  visitor exporter: two families, 26 characters.
- **The embedded-font blind spots**: `applyLineHeight` takes the
  `PhysicalFont` its callers already hold (`setAttribute`'s resolved font,
  the fallback's, `symbolSetAttribute`'s, `setFallbackFamily`'s); `kernSpaces`,
  `applyScaling`, `noLigatures` and `glyphFallback` resolve a span's
  font-family through the new `Mapper.physicalFontNamed` (the document's
  mappings, embedded faces and last-resort fallbacks by physical name, then
  `PhysicalFonts`; the suffix stripping moved to
  `PhysicalFonts.stripSuffixes`); `XsltCommonFunctions.fontCanRender` uses
  the same.  `EmbeddedFontMetricsTest` loads `FontEmbedded.docx`, renames its
  embedded Calibri to a name no table knows, and asserts the run's
  line-height is the embedded file's metrics, not the 1.2 fallback, and that
  the run got the no-ligature twin.
- **The selector's own fonts**: `Segoe UI Symbol` (the face Word 2016 uses
  for a symbol the run's font lacks) and the configured emoji font are never
  among the document's names, so the mapper never mapped them and
  `symbolBlockFont` could not find them; `ownFont` maps either on demand
  where the machine has it (and `registerUsedFont` declares it late).
- **Logging**: `warnedOnce` per selector - a font not mapped once (it was
  once per run: `physicalFontResolved`), a symbol with no replacement once
  per font and code point, the theme part unreadable once at WARN without
  the stack trace (it was `log.error` with the trace per run);
  `getCssProperty`'s `printStackTrace` and the discovery visitor's "Got
  null" trace are gone.
- **Line endings**: `ScrambleText.java` was CRLF; converted in its own
  commit (26a46491b, in `.git-blame-ignore-revs`; `.gitattributes` keeps it)
  before the change.

- **The no-bold alias for a Word-defaulted font** (gap 17, found by this
  gate): `addWordDefaultSubstitutes` records the names it maps and
  `addNoBoldFaceAliases` leaves them alone.

Gate (2026-09-12), run twice - before and after the gap 17 correction, the
second the one that stands: `docx4j-core-tests` 1012/0 (the fonts package
104/0 after the correction), `docx4j-export-fo-tests` 601/0.  Three corpora
against `p3b-mapping` (phase 3's end state), IdentityPlusMapper: 0.9053 ->
0.9051, 0.8804 -> 0.8805, 0.9182 -> 0.9181; two documents changed.
`15_ru-RU_tbl_5218` improved 0.9730 -> 1.0000 (both mappers): the preamble
path used to emit an empty inline for an empty run, and the block-font choice
counted it; through the visitor it emits nothing, as the range path always
did.  `15_en-US_sdt_num_3475` 0.9592 -> 0.9184, the one regression, is gap
16 in the baseline: the harness renders each document twice in one JVM and
phase 3's second pass had lost `USPSIMBCompact` (a barcode font Word does not
have, `w:family` modern) to the alias leak - unmapped, so the default font's
Carlito, 37 characters a line - where the rule gives Courier New's clone
Cousine, 32 a line; Word's own line is 37, so the accident scored better than
the rule.  The rule stands (the document is the only one in that font; the
FO is byte-identical before and after, only the mapping differs).  Before the
gap 17 correction `14_de-DE_sdt_num_tbl_278` showed the same shape (0.8783 ->
0.8441, its baseline the second pass without the alias); with it the document
is unchanged.  BestMatchingMapper against its own cells: 0.9008, 0.8761 ->
0.8762, 0.9174, the same improvement and no regression.  Probes: unchanged
from phase 3 (92/29/83/100/17/17/100) except `fonts-unresolvable` 75% -> 88%
(16/16 lines, worst offset 15.8pt -> 1.3pt), gap 16 again - the probes render
in one JVM too.  Corpus baseline for phase 5: `p4b-nobold` (identity) and
`c4b-best-all` (best).

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
   its meaning (document names).  Done in phase 4: `docx4j-docx-anon` itself
   no longer uses it (`documentFontFor`).
6. **Order against CR-001**: batch 42 (fonts) waits for phase 3 of this CR;
   batch 41's harness items and batch 43 are independent of it.
7. **No decision on `BestMatchingMapper`'s future until it has been scored**
   (Jason, 2026-09-12).  Scored the same day (the matrix above): behind in
   every environment, by 0.010-0.021.  The decision itself is still Jason's;
   the CR's recommendation now is IdentityPlusMapper everywhere, the samples
   and the Getting Started text to say so, BestMatchingMapper kept and given
   the shared passes in phase 3 so that it stops losing what it need not
   (done: with them, and its panose step behind them, it scores within 0.001
   to 0.005 of IdentityPlusMapper on all three corpora, where it was 0.010 to
   0.021 behind), and - the larger finding - the jars' own coverage for headless deployments
   (a Liberation-or-croscore choice that leaves neither DejaVu Sans nor a
   condensed face nor the URW faces available) to be looked at as its own
   item.  Phase 0c measures how far behind IdentityPlusMapper
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
