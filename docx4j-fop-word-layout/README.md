# docx4j-fop-word-layout

Word-style line breaking for docx4j's PDF output via Apache FOP (Enterprise
CR-001, Phase 5). Add the jar to the classpath; docx4j-export-fo finds it
through `ServiceLoader` and installs it on every FopFactory it builds. Turn it
off with the docx4j property `docx4j.convert.out.fo.wordLineBreaking=false`.

## What it changes

Word breaks lines greedily (first fit): a line takes every word that fits and
breaks at the last opportunity before the first word that does not, with no
look-ahead. FOP's Knuth-Plass total-fit optimises the whole paragraph, so with
identical fonts and widths the two break about a quarter of ragged-right lines
differently, and every later line and page moves.

For justified text Word also compresses the spaces of a line to pull one more
word in, down to about three quarters of their natural width (measured from
Word 365 output; glyphs keep their width). The limit is
`docx4j.convert.out.fo.wordLineBreaking.maxSpaceShrink`, default 0.24, which is
the value at which the harness's justified probe breaks 98% of its lines as
Word does.

Measured with the harness in `docx4j-layout-fidelity` against Word 365 goldens:
line-break parity on ragged prose 72% -> 100%, justified prose 63% -> 98%; 14 of
22 probes at 100%.

## How

FOP exposes no first-fit option and its line manager's breaking algorithm is a
private inner class, so this jar carries a copy of FOP 2.11's
`LineLayoutManager` (`WordLineLayoutManager`, Apache License 2.0) whose
breaking loop is greedy, a `WordBlockLayoutManager` that creates it, and a
`WordLayoutManagerMaker` that FOP is given through
`FopFactoryBuilder.setLayoutManagerMakerOverride`. Two package-private FOP
members are reached by reflection (`LBP`); fop-core is an automatic module, so
that needs no `--add-opens`.

The copy is tied to FOP 2.11 internals. When FOP is upgraded, re-derive it:
copy the new `LineLayoutManager.java`, rename, extend `LineLayoutManager`,
route `LineBreakPosition` and `AlignmentContext` construction through `LBP`,
and re-apply the greedy members of the inner `LineBreakingAlgorithm`
(`findBreakingPoints`, `considerLegalBreak`, `fitsByShrinkingSpaces`,
`commit`, `greedyBreakPoints`) and the single-pass
`findOptimalBreakingPoints`. `GreedyLineBreakingTest` checks the result on a
monospace font where every expected break can be counted.
