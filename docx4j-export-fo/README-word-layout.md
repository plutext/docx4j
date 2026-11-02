# Word-style layout

Word-style layout for docx4j's PDF output: line breaking, line placement, list items,
page-number citations, tabs and leaders, Word's hyphenation zone and GDI rounding
(Enterprise CR-001, phase 5 and later). Since 17.3.6 the layout is the **docx4j FO
renderer's** own, in its layout managers (`org.docx4j:docx4j-fo-renderer`, its hook
`word-layout`; fork CR-031, docx4j CR-071). docx4j's copy of FOP's layout managers,
which until 17.3.5 lived in `org.docx4j.fop.wordlayout`, is gone.

It is on by default. Turn it off with the docx4j property
`docx4j.convert.out.fo.wordLayout=false`, which restores plain FOP layout (and the
`docx4j:` attributes below are then not written).

## What it changes

Word breaks lines greedily (first fit): a line takes every word that fits and
breaks at the last opportunity before the first word that does not, with no
look-ahead. FOP's Knuth-Plass total-fit optimises the whole paragraph, so with
identical fonts and widths the two break about a quarter of ragged-right lines
differently, and every later line and page moves.

For justified text Word also compresses the spaces of a line to pull one more
word in, down to about three quarters of their natural width (measured from
Word 365 output; glyphs keep their width). The limit is
`docx4j.convert.out.fo.wordLayout.maxSpaceShrink`, default 0.245.

Measured with the harness in `docx4j-layout-fidelity` against Word 365 goldens:
line-break parity on ragged prose 72% -> 100%, justified prose 63% -> 98%. The
rules and their measurements are in `docs/word-layout-rules.md`; the settings in
`docs/word-layout-settings.md`.

## What docx4j does, and what the renderer does

docx4j writes the FO and hands the renderer its settings; the renderer lays it out.

- **`WordLayoutCustomizer`** (found through `ServiceLoader`) turns the renderer's Word
  layout on for every FopFactory docx4j builds, by
  `FopFactoryBuilder.setWordLayout(Map<String,String>)`: every
  `docx4j.convert.out.fo.wordLayout.*` setting by its full name, a system property
  over `docx4j.properties`. The renderer reads the ones its layout uses and takes the
  default for any not set; the names users set did not change. It also passes,
  through `setWordLayoutFontMetrics`, the run fonts' Word metrics
  (`WordLineMetrics`), which the renderer cannot read for itself. Both calls are made
  by reflection, since this module also compiles against Apache FOP.
- **The `docx4j:` attributes** (namespace `http://docx4j.org/fop/word-layout`; their
  names are `WordLayoutElementMapping`'s constants, and the renderer registers the
  namespace) carry what the layout needs from the document: on each paragraph's
  `fo:block` the line box, baseline and line rule (`docx4j:line-box`,
  `docx4j:baseline`, `docx4j:line-rule`), the tab stops (`docx4j:tabs`,
  `docx4j:tab-default`, `docx4j:tab-ind`), a list label's ascent and separator; on
  `fo:root` the document's hyphenation settings (`docx4j:hyphenation-zone`,
  `docx4j:hyphen-limit`, `docx4j:hyphenate-caps`), the justification limit
  (`docx4j:space-shrink`) and `w:noTabHangInd`; on spans and leaders the small-caps
  scale, a tab, a TOC leader, a page-number placeholder. docx4j writes them only when
  the renderer carries Word layout (`WordLayoutCustomizer.extensionNamespace()`).
- **A table's column widths** are measured by where a line may break
  (`TableWriter`): the renderer's own break opportunities
  (`org.apache.fop.layoutmgr.word.WordBreakOpportunities`), else UAX #14 as FOP
  applies it.

Comments in docx4j that name `WordLineLayoutManager`, `WordListItemLayoutManager`,
`WordPageNumberCitationLayoutManager` or `LBP` refer to what those classes did here
until 17.3.5; the behaviour is now the renderer's (its `LineLayoutManager` in Word
mode and the Word managers beside it), and its tests are the renderer's too.

## On Apache FOP

On Apache FOP (`-Papache-fop`, or a consumer who excludes the docx4j FO renderer)
docx4j lays out as plain FOP does: Knuth-Plass breaking, FOP's line placement, and a
tab after text keeps its stand-in of three no-break spaces. It logs once that Word
layout needs the docx4j FO renderer. The tests of Word layout in docx4j-export-fo-tests
are skipped there (`WordLayoutOn.assume()`).

## Glyph advances (org.docx4j.fop.fonts)

The one copy of FOP code this module still carries is a small one, and it is
worth one line upstream. FOP's `OpenFont.convertTTFUnit2PDFUnit` converts a font's advance
widths to 1/1000 em by integer division —

```java
ret = (n / upem) * 1000 + ((n % upem) * 1000) / upem;   // truncates
```

— so every advance is up to one unit short, and every line FOP measures is up to
about 0.1% narrow: on 12pt Liberation Serif a 30-character line measures 139.164pt
where the font's own metrics give 139.295, and a 74-character one 376.284 against
376.559. Word measures with the font's exact advances, and writes *rounded* widths
into its own PDFs' `/Widths` (CR-001 §10 has the measurement). **The upstream fix is
that one expression, rounded**:

```java
ret = (int) (((long) n * 1000L + upem / 2) / upem);      // rounds
```

docx4j's own copy of that class (`org.docx4j.fonts.fop.fonts.truetype.OpenFont`, in
docx4j-core) does exactly that, which is what its `TextMeasurer` and the table autofit
pass measure with. FOP loads its own fonts with its own copy, so
`org.docx4j.fop.fonts.WordGlyphWidths` corrects the width table of each font FOP
loads, reading the same font file through docx4j's copy
(`org.docx4j.fonts.GlyphAdvances`). `WordWidthsFontCollection` — FOP's
`CustomFontCollection`, registering a `WordWidthsLazyFont` — installs it, and
`NonCachingPdfDocumentHandlerConfigurator` uses it in place of FOP's. Because the
correction goes into the array both FOP's line measure and the PDF renderer's
`/Widths` read, the text layer stays consistent with the glyph positions; after it the
`/Widths` docx4j writes for Liberation Serif are Word's own. It is applied when a font
is first loaded, so a font the document never uses is never read.
`docx4j.convert.out.fo.glyphWidths.round=false` turns it off. Drop the whole package if
FOP fixes the conversion.
