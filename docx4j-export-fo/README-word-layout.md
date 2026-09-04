# Word-style layout (org.docx4j.fop.wordlayout)

Word-style line breaking and line placement for docx4j's PDF output via Apache
FOP (Enterprise CR-001, Phase 5). The code is part of docx4j-export-fo, in the
package `org.docx4j.fop.wordlayout` — there is no separate jar to add. It is on
by default: docx4j-export-fo finds `WordLayoutCustomizer` through
`ServiceLoader` (`META-INF/services`) and installs it on every FopFactory it
builds. Turn it off with the docx4j property
`docx4j.convert.out.fo.wordLayout=false`, which restores plain FOP layout (the
`docx4j:` attributes below are then not written either).

## What it changes

Word breaks lines greedily (first fit): a line takes every word that fits and
breaks at the last opportunity before the first word that does not, with no
look-ahead. FOP's Knuth-Plass total-fit optimises the whole paragraph, so with
identical fonts and widths the two break about a quarter of ragged-right lines
differently, and every later line and page moves.

For justified text Word also compresses the spaces of a line to pull one more
word in, down to about three quarters of their natural width (measured from
Word 365 output; glyphs keep their width). The limit is
`docx4j.convert.out.fo.wordLayout.maxSpaceShrink`, default 0.24, which is
the value at which the harness's justified probe breaks 98% of its lines as
Word does.

Measured with the harness in `docx4j-layout-fidelity` against Word 365 goldens:
line-break parity on ragged prose 72% -> 100%, justified prose 63% -> 98%; 14 of
22 probes at 100%.

## How

FOP exposes no first-fit option and its line manager's breaking algorithm is a
private inner class, so this package carries a copy of FOP 2.11's
`LineLayoutManager` (`WordLineLayoutManager`, Apache License 2.0) whose
breaking loop is greedy, a `WordBlockLayoutManager` that creates it, and a
`WordLayoutManagerMaker` that FOP is given through
`FopFactoryBuilder.setLayoutManagerMakerOverride`. A few package-private or
private FOP members are reached by reflection (`LBP`: `LineBreakPosition`,
`AlignmentContext`'s constructor and line-height, `InlineLayoutManager.font`);
fop-core is an automatic module, so that needs no `--add-opens`.

Since CR-001 §6.9 these managers also place lines as Word does. docx4j writes three
attributes on each paragraph's `fo:block` in the namespace
`http://docx4j.org/fop/word-layout`, which `WordLayoutElementMapping` registers
with FOP (through `META-INF/services/org.apache.fop.fo.ElementMapping`) so FOP
keeps them as the block's foreign attributes instead of rejecting them:

- `docx4j:line-box`: the text box of a line (the paragraph font's
  single-spacing pitch: usWinAscent + usWinDescent + external leading);
- `docx4j:baseline`: the baseline within it (ascent + external leading);
- `docx4j:line-rule`: `auto`, `exact` or `atLeast` (`w:lineRule`).

`WordLineLayoutManager` sizes each line from the runs on it (each run's span
carries Word's pitch as its `line-height`, read from the alignment context by
reflection, and its font's ascent share from `WordLineMetrics`), then applies
the rule: for `auto` the block's `line-height` / box is the multiple and the
extra goes *below* the text as a `LeadingGlue` emitted after the break
possibility that follows the line, so FOP's page breaker drops it when it takes
the break (the last line of a page keeps only its text box, as in Word).
`WordFlowLayoutManager` moves a paragraph's last leading behind the break the
flow adds after the block. `exact` clips the line to the box; `atLeast` puts a
shortfall above the text. docx4j writes the attributes only when
`WordLayoutCustomizer.extensionNamespace()` reports the namespace (Word layout
on), because FOP rejects them without the mapping. `WordLeadingTest`
checks the page fit on a monospace font.

The copy is tied to FOP 2.11 internals. When FOP is upgraded, re-derive it:
copy the new `LineLayoutManager.java`, rename, extend `LineLayoutManager`,
route `LineBreakPosition` and `AlignmentContext` construction through `LBP`,
and re-apply the greedy members of the inner `LineBreakingAlgorithm`
(`findBreakingPoints`, `considerLegalBreak`, `fitsByShrinkingSpaces`,
`commit`, `greedyBreakPoints`) and the single-pass
`findOptimalBreakingPoints`. `GreedyLineBreakingTest` checks the result on a
monospace font where every expected break can be counted.
