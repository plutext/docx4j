# docx4j-layout-fidelity

Harness for measuring how closely docx4j's XSL-FO/PDF output matches Word's
page layout (Enterprise CR-001, Phase 0). Not in the reactor; not deployed.

Pipeline:

1. `generate` writes a corpus of small probe documents, each isolating one
   layout rule (`org.docx4j.fidelity.corpus.Corpus`). Every paragraph starts
   with a label (`P01 `, `P02 `, ...) so lines can be attributed.
2. Reference PDFs ("goldens") are produced by Word on the Windows VM with
   `WordGoldenRunner` (documents4j-local), together with a manifest.
3. `render` produces docx4j+FOP PDFs (and the intermediate `.fo`) for the
   same corpus on Linux.
4. `compare` pairs up the text lines of each PDF pair (longest common
   subsequence on line text), reports line-break and page-break parity and
   baseline/x deltas, rasterises both and counts differing ink pixels, and
   writes an HTML report with side-by-side pages and a red/blue overlay.
5. `score` does 1-4 without the images over a large corpus of real documents,
   writing a CSV scoreboard and a delta against a previous one (see below).

## Build and run

```bash
# upstream modules must be installed at the current ${revision} first.
# Use `clean`: the XJC plugin only checks xsd/ROOT.xsd for staleness, so after a pull
# that changed an imported schema (xsd/wml/wml.xsd etc.) an incremental build keeps
# the old generated classes and docx4j-core fails with "cannot find symbol".
mvn clean install -DskipTests -Dgpg.skip=true -pl docx4j-export-fo,docx4j-documents4j-local,docx4j-JAXB-ReferenceImpl -am
# then this module (not in the reactor, so build it from its directory);
# packaging also copies the runtime dependencies to target/lib.
# (No -o on a machine that has not built this module before: the dependency plugin
# has to be downloaded once.)
cd docx4j-layout-fidelity && mvn -Dgpg.skip=true -DskipTests package

CP="target/classes:target/lib/*"
java -cp "$CP" org.docx4j.fidelity.Fidelity generate  target/corpus
java -cp "$CP" org.docx4j.fidelity.Fidelity run       target/corpus /path/to/goldens target/report [dpi]
# -Dfidelity.only=footnotes,image-anchored restricts render/compare/run to those probes
```

On Windows the separator is `;`: `-cp "target\classes;target\lib\*"`. (Do not put the
classpath in an environment variable via `set /p`: it truncates at 1023 characters.)

On the Windows VM (Word installed, and the corpus fonts installed as system
fonts: Liberation Serif and Liberation Sans from `docx4j-export-fo-fonts-liberation`,
Carlito from `docx4j-export-fo-fonts-crosextra`, and DejaVu Sans, which is not in
any docx4j font module; use the host's `/usr/share/fonts/TTF/DejaVuSans*.ttf`):

```
java -cp "%CP%" org.docx4j.fidelity.golden.WordGoldenRunner <sharedFolder>\corpus <sharedFolder>\goldens
```

Commit the goldens and `golden-manifest.properties` together. Regenerate them
only when the corpus changes or Word on the VM is updated.

## Having Word save the docx too

Give the runner a third directory and it also opens each document in Word and saves
it back as a docx there:

```
java -cp "%CP%" org.docx4j.fidelity.golden.WordGoldenRunner <shared>\corpus <shared>\goldens <shared>\resaved
```

The PDF says where Word *put* the text; the resaved docx says what Word *computed*,
and some questions can only be answered by the second. The `w:tblGrid` is the case
that forced this. In a Word-authored autofit table the grid is Word's own cached
layout, so where the grid and the cells disagree the grid is what Word draws - but in
a document this harness generated, the grid is whatever the generator wrote and Word
recomputes it on open. **A probe's grid therefore proves nothing about what Word does
with a grid until Word has written one**, which cost this project a wrong conclusion:
`table-cell-pct` says the cells beat the grid, implementing that took the probe from
43% to 100%, and it cost a quarter of the real-document corpus 0.8967 to 0.8771 of
mean line parity.

Word recomputes more than tables - the section's `w:sectPr`, style and numbering
normalisation, field results, fonts it substitutes - so the resaved corpus is worth
having for any "what does Word actually compute here" question, and a diff against the
original is a measurement in twips rather than one read off a page image.

The resave is checked *before* the PDF's own skip, so it can be added to a golden set
that is already cut without `--force` re-cutting every PDF. The golden PDF is always
made from the **original** docx: rendering Word's own output would be measuring Word
against Word.

Two things to know before reading a diff. Word rewrites a great deal that is not a
computation - `w:rsid`s, attribute order, `w:proofState`, its own `w:compat` block - so
diff the elements you are asking about rather than the file. And the resave goes through
the same conversion script as the PDF, so if
`com.documents4j.conversion.msoffice.word_convert.vbs` points at the ToC-updating script
in `docx4j-samples-resources`, the resaved docx has its fields updated too; that is
usually what you want from a round trip, but it means field results in the resave are
Word's, not the document's.

`GridDiff` reads the table half of it, and is the direct way to ask what Word thinks a
column should be:

```bash
java -cp "$CP" org.docx4j.fidelity.golden.GridDiff <corpusDir> <resavedDir>
```

It prints one line per table whose grid Word rewrote, the widths before and after and
the per-column ratio, then how many documents and tables were affected. A table Word
leaves alone is one whose grid it agrees with.

`CompatDiff` does the same for `w:compat`, and it cuts two ways:

```bash
java -cp "$CP" org.docx4j.fidelity.golden.CompatDiff <corpusDir> <resavedDir>
```

**As a check on every measurement made from a golden.** The settings-sensitivity table
in `docx4j-export-fo/docs/word-layout-settings.md` assumes the document Word laid out is
the document we read. If Word changes `compatibilityMode` on open, or drops a flag, then
the golden PDF was produced under settings which are not the ones in the docx, and a rule
keyed on the declared flag was measured against the wrong thing. Such a document is called
out by name.

**And as Word's own mode defaults, measured rather than inferred.** Where Word *adds* a
flag the document did not state, it has written down the value it was already using -
which is exactly what that table records as MODE-DEFAULTED and has so far had to take
from the specification. Grouped by the mode the document declared, the additions are that
table, derived from Word rather than from the spec. Reflection over `CTCompat`'s getters
means a flag nobody has thought about yet is still reported.

## Hyphenation patterns (licence note)

FOP ships no hyphenation patterns, so the `hyphenation` and `hyphenation-zone`
probes would render unhyphenated without them. This module therefore depends on
`net.sf.offo:fop-hyph`, which is **not** under the Apache licence (the OFFO
distribution is under the LaTeX Project Public Licence, together with the
licences of the individual TeX pattern files). That is acceptable here because
this harness is not in the reactor and is never deployed; it is a test-scope
dependency of docx4j-export-fo for the same reason, and a dependency of no
published docx4j module.

Because the patterns are here, the real-document corpus now hyphenates the four
documents that set `w:autoHyphenation` (de-AT, de-DE, sl-SI, pt-BR), while Word
on the reference VM hyphenated none of them - it needs the proofing tools for the
text's language installed, and the English machine had none. That costs those
documents line parity and shows up in `score` as a regression which is an
artefact of the reference, not of the layout. Re-golden them on a VM with the
German proofing tools before reading it as one, or drop this dependency while
scoring.

Note that `target/lib` is only added to, never cleaned, by `copy-dependencies`:
after a version bump it holds both versions of every docx4j jar and the old one
may win on the wildcard classpath. Delete the stale ones (or `target/lib`
itself) after a bump.

A stand-in reference for plumbing checks (never the target):

```bash
soffice --headless --convert-to pdf --outdir target/goldens-lo target/corpus/*.docx
```

## Scoring a real-document corpus

`run` is for the hand-built probes: it renders everything, stops at the first
exception, and writes an HTML report with page images. `score` is for a corpus of
hundreds of real documents, where some will fail and the question is not "what
does this one document do" but "did this change help across the corpus".

```bash
CP="target/classes:target/lib/*"
java -cp "$CP" org.docx4j.fidelity.Fidelity score <corpusDir> <refPdfDir> <outDir> [baseline.csv]
# -Dfidelity.timeoutSeconds=120  per-document conversion timeout (default 120)
# -Dfidelity.only=id,id          restricts the run to those documents
```

Every `<id>.docx` in `corpusDir` that has an `<id>.pdf` in `refPdfDir` (the Word
PDF of the same basename) is rendered to `<outDir>/fop/<id>.pdf` (and `.fo`) and
compared against it. Documents are done smallest first, so a run that is cut
short has still covered the most documents. A document that throws is one `error`
row and a document that hangs past the timeout is one `timeout` row; neither stops
the run. No page images and no per-document HTML report are produced — at this
scale the CSV is the report.

Outputs, in `outDir`:

- **`scoreboard.csv`** — one row per document, plus a final `TOTAL` row carrying
  the aggregate (its numeric columns are a convenience; the whole aggregate is
  also spelled out as text in that row's `firstDivergence` column, and reading a
  scoreboard back recomputes the aggregate from the document rows).
- **`scoreboard.txt`** — the aggregate, the delta if a baseline was given, then
  one line per document, worst line parity first.

Columns:

| column | meaning |
| --- | --- |
| `id` | the docx basename |
| `compatMode` | the leading number of the name (`12_...` → Word compatibility mode 12), blank if the name does not start with one |
| `sizeBytes` | size of the docx |
| `status` | `ok`, `error` (threw), `timeout` (no result in time), `noref` (no Word PDF, not scored) |
| `refPages` / `candPages` | page count, Word / docx4j |
| `refLines` / `candLines` | text lines extracted from each PDF |
| `lineParity` | reference lines that have an identical line in the candidate, 0-1 — the headline number |
| `pageParity` | matched lines that are also on the same page |
| `matched` | matched line count |
| `medianDy` / `maxDy` | candidate minus reference baseline, in points, over lines matched on the same page |
| `firstDivergence` | the first line- or page-break difference |
| `error` | first line of the exception, for `error` / `timeout` rows |

The aggregate is: documents scored / errors / timeouts / no-reference; documents
with the same page count; lines matched over lines total; median and mean line
parity; and documents at line parity >= 0.98.

The loop for a layout change:

```bash
java -cp "$CP" org.docx4j.fidelity.Fidelity score corpus goldens target/score
cp target/score/scoreboard.csv target/baseline.csv     # keep the "before"
# ... make the layout change, rebuild docx4j-export-fo and this module ...
java -cp "$CP" org.docx4j.fidelity.Fidelity score corpus goldens target/score target/baseline.csv
```

The second run prints the aggregate before and after side by side, then every
document whose line parity moved by more than 0.02, or whose status or
page-count equality changed — regressions (biggest drop) first, then
improvements. Accept the change only if no aggregate figure falls and no
document regresses beyond that noise floor; a change that lifts the mean while
breaking a handful of documents needs those documents looked at individually
(`run` with `-Dfidelity.only=<id>` gives the page images).

## JUnit

`FidelityTest` generates and renders the corpus unconditionally, and compares
against goldens when `-Ddocx4j.fidelity.golden=<dir>` (or the
`DOCX4J_FIDELITY_GOLDEN` environment variable) is set. No tolerances fail the
build yet. `ScoreboardTest` covers the `score` mode's CSV, aggregate and delta
arithmetic on synthetic comparison results, so it needs no documents.

## How a line is extracted and paired

Both PDFs go through the same PDFBox-based extractor
(`extract/PdfLayoutExtractor`), so anything it does is done to Word's PDF as
well as to docx4j's; four knobs are worth knowing about.

- **`-Dfidelity.wordGapEm=`** (default `0.25`) is how wide a gap between two
  glyphs, as a fraction of the font size, is read as a word space where the PDF
  has no space glyph. It was 0.15, which split words: Word's own PDF of one
  corpus document has a heading PDFBox read as `Inte ntion of the 5 week
  Program`, where `mutool draw -F stext` reads `Intention` with the glyphs
  running 12.75..54.91 contiguously. A space is never that narrow - an ordinary
  one is 0.25 to 0.28 em in these documents' fonts, and a justified line
  compresses one to about 0.19 em at worst - so 0.25 is below the narrowest
  space and above the widest intra-word gap measured. The same threshold is the
  median word gap the line splitter compares a wide gap against, so it also
  stops a tabbed line being split in two where Word's PDF keeps it whole.
  Measured over the goldens of five table-of-contents-heavy documents, it
  changes 0 / 2 / 0 / 4 / 24 lines.
- **`-Dfidelity.leaderNormalise=false`** turns off the leader-run
  normalisation in `PdfLayout.Line.key()`, which is what the LCS pairs on. Runs
  of three or more leader glyphs (`.`, `·`, `‧`, `_`), however spaced, collapse
  to one token together with the whitespace around them, so a dot leader whose
  dots differ by one still pairs: Word writes `duty ...... 5`, where its dots
  sit on the reference area's grid, and docx4j's start on the text, so the two
  extracted `duty ...... 5` and `duty......5` although the geometry agreed to
  0.2pt. Measured, it is worth +0.113, +0.031, +0.021, +0.017 and +0.014 of
  line parity on five documents.
- **`-Dfidelity.dateNormalise=false`** turns off the date/time normalisation in
  `PdfLayout.Line.key()`. A `DATE`, `CREATEDATE`, `PRINTDATE`, `SAVEDATE` or
  `TIME` field prints the day the PDF was made, so a golden cut on one day never
  pairs with a render made on the next: the line drifts out of the LCS, takes
  its neighbours with it, and the document's score falls by a line a day for
  reasons that have nothing to do with layout. A date - `05.09.2026`,
  `2026-09-05`, `9/5/2026`, `5 September 2026`, `5. September 2026`,
  `5 de septiembre de 2026`, `5 сентября 2026`, `September 5, 2026` - therefore
  collapses to one placeholder token, and a clock time (`14:05`, `14:05:32`,
  `2:05 PM`) to another. Month names cover the locales the corpora are written
  in; a four-digit year is required, so a section number (`1.2.34`) or a money
  amount is not a date. Both sides go through it, so nothing is hidden that is
  not equally hidden on Word's side, and the geometry of a paired line is still
  compared in full — a date we lay out in the wrong place is still counted
  against us.

  It touches **1111 of 71610 golden lines in 105 of 156 documents** on batch 2
  (1121 of 71818 in our renders), **503 of 40308 in 103 of 191** on batch 1
  (498 of 40048), and **12732 of 185685 in 73 of 102** on batch 3 (12686 of
  190751). Re-baselining the three corpora on it moved the aggregate very
  little, which is the point — it removes a drift rather than adding a score:
  batch 2 lines matched 54510 -> 54516 and mean parity 0.8393 -> 0.8394, batch 1
  34546 -> 34547 and 0.8751 -> 0.8752, batch 3 161321 -> 161430 and
  0.8644 -> 0.8649 (one document +0.030, `parity >= 0.98` 14 -> 15). Without it
  those numbers would have fallen again the next day.
- **`-Dfidelity.rowTolerancePt=`** (default `3`, `0` turns it off) is how far
  apart two lines' baselines may be and still be read as one row, which is
  ordered left to right rather than by baseline. The comparison is an LCS over
  the line list in order, so a pair of lines the two PDFs put in the opposite
  order is a pair it cannot match, and it loses both. That happens wherever a
  label cell sits beside the text it labels: a `w:vAlign` label and the first
  line of the body column beside it are within a line of each other, and which
  has the smaller baseline is decided by a fraction of a point - Word puts one
  corpus document's `Assessment of student` 0.7pt *above* the `Diagnostic
  assessment: KWL chart` it labels and docx4j puts it 7.0pt *below*, so the
  strict baseline order disagrees although both renders paint the same row.
  Measured over eight label-heavy documents, 8086 reference lines: 6458 lines
  matched at 0, 6725 at 2pt, **6730 at 3pt**, 6722 at 4pt, 6686 at 5pt, 6738 at
  8pt (but median parity 0.8620 against 3pt's 0.8697), 6526 at 12pt, where a row
  starts swallowing the next line of a column. A column's line pitch is at
  least 9pt in the densest of those tables. Over the three corpora the change is
  worth +0.4 to +1.0% of matched lines and +0.004 to +0.007 of mean parity, with
  no page count moved (it is a measuring change, not a rendering one).
- **`-Dfidelity.minSplitPt=`** (default `20`, `0` turns it off) is how wide a
  gap must be before it can split a baseline into two lines at all. The other
  split tests are relative (0.7 em, three median word gaps), so a short tab gap
  splits a line whenever it is written as a real gap and does not when it is
  written as space glyphs, and Word and docx4j do not agree on which: Word's
  PDF writes the tab between a list label and its text as space glyphs, so
  `1. Introduction` is read as one line, where ours writes no glyph in the gap
  and it is read as two. It is nearly inert - four reference lines and one
  extra match out of 8086 on those eight documents - and kept only because the
  asymmetry it closes is systematic, and it can only merge, on both sides
  alike. The vertical-rule split (a real cell boundary) is not subject to it.

  Both ways of resolving that same asymmetry have now been measured over a
  whole corpus and neither pays, so **do not re-open it without new evidence**.
  Merging harder - raising this threshold - is inert: over an unbiased quarter
  of the 191-document corpus, mean line parity is 0.8967 at 20pt, 0.8973 at
  32pt and 0.8950 at 72pt, and turning the column-gutter rule off moves it by
  0.0001. Splitting instead - `-Dfidelity.inkGap=true`, below - wins big on the
  documents the hypothesis was written from and loses across the corpus.

- **`-Dfidelity.inkGap=`** (default `false`) measures the gap that decides a
  split from the last glyph that put *ink* on the page rather than from the
  last glyph of any kind, which is symmetric: a tab Word writes as a space
  glyph and one we write as nothing are then read alike. On the twelve
  documents the triage ledgers named for this it is a clear win (lines matched
  91.6% -> 93.2%, mean parity 0.9147 -> 0.9235, four documents to 1.0,
  including the one whose 278-against-362 line inflation motivated it, which
  becomes 362/362). Over the whole corpus it costs: reference lines
  40339 -> 41840, matched 35053 -> 36251, so the newly separated lines match at
  80% against the corpus's 86.9%, mean parity falls 0.8816 -> 0.8711 and 34
  documents fall against 22 that rise. The extra splits are real cell
  boundaries and the exposure is honest, but it moves the yardstick without
  improving the layout and breaks comparability with every earlier scoreboard.
  Kept, off, so the measurement can be repeated.

- **`-Dfidelity.clusterTolerancePt=`** (default `1`) is the floor on how far
  apart two glyphs' baselines may be and still be read as one line; the
  tolerance actually used is the larger of it and 0.3 em. It is **inert**,
  because 0.3 em already exceeds it for any body text: 1, 1.5 and 2pt score
  identically and 2.5pt and above are worse. Line-grouping instability at ~2pt
  was reported by all three corpora, and this is the measurement that says it
  is not the cause - the inflation is horizontal, and is `inkGap` above.

- **`-Dfidelity.columnGutterPt=`** (default `10`, `0` turns it off), with
  **`-Dfidelity.columnGutterLines=`** (`8`), **`-Dfidelity.columnSideFraction=`**
  (`0.3`) and **`-Dfidelity.columnBalance=`** (`0.6`), splits a line at the
  page's **column gutters**. The per-line tests cannot see a two-column page: a
  gap splits a baseline only where it is more than three times *that line's*
  median word gap, and a justified line's word gaps are stretched - on the page
  which motivated this, to 4.7pt against the same font's natural 2.5pt space -
  so the 51pt gutter between the columns is under three of them and the two
  columns are read as one line, where Word's PDF (whose two columns' baselines
  are a fraction of a point apart) reads them separately and every line of the
  page fails to match.

  The page's geometry says where the columns are without reference to any one
  line: a run of x that no glyph on the page puts ink in, at least 10pt wide,
  with at least 8 lines each side, each side spanning at least 0.3 of the page's
  ink and at least 0.6 of the other side's. It is measured from the same glyph
  boxes on both sides, so Word's PDF and ours find the same bands, and it can
  only ever split a line further - never merge two.

  Each guard earns its place. Without the *side fraction* the rule fires on
  every hanging indent (the band between a bullet and its text is 13pt wide and
  repeats on every line): two columns span about 0.47 of the page's ink each, a
  bullet column 0.01 and a label column 0.15, and without it the aggregate fell
  on all three corpora. Without the *balance* it fires where a narrow label
  column sits beside full-width prose (0.45-0.51), and there whether the band is
  clear on a given page is decided by one long line, so the two PDFs find it on
  different pages and the split is one-sided, which loses lines rather than
  winning them (-0.084 and -0.051 on two documents). Word's own columns are
  equal unless `w:cols/@w:equalWidth="0"`, and the measured pairs are 0.76 to
  0.99; the cost is that a genuinely unequal section (157/318pt is 0.49) is left
  to the per-line tests.

  Re-baselining the three corpora on it: mean parity 0.8802 -> **0.8812** on
  batch 1 and 0.8473 -> **0.8487** on batch 2, both with no page count moved,
  and 0.8808 -> 0.8802 on batch 3 - where the whole of the fall is one document
  whose render has four pages to Word's five, so the gutter on Word's page 5 has
  no counterpart in ours. Ten documents gained, up to +0.17 and +0.15; six lost.
- **An invisible rule is not a rule.** `PdfLayoutExtractor.verticalRuleBetween`
  splits a baseline cluster at a thin vertical box, which is how a table row is
  read as one line per cell on both sides. Word paints its cell borders as
  *filled* rectangles, so they arrive with a real width; FOP paints them as
  *strokes*, whose bounding box is zero-wide for a vertical line - so the width
  alone cannot tell a genuine border from one that paints nothing. The colour
  can: a document which collapses its cell boundaries had FOP stroke 52 of them
  a page in **6pt white on white paper**, where Word's PDF of the same page
  draws nothing at all, and every one split a row into one line per cell -
  7784 candidate lines against an unchanged golden's 222 pages became 15362.
  A box whose paint converts to RGB above 250/255 on every channel is therefore
  not a rule. Its visible neighbours, and the 364 grey 1pt strokes of another
  document's table, are unaffected; measured, one document gained 0.26 of line
  parity and one lost 0.07 (its rows now pair whole rather than cell by cell).

All six are measuring, not rendering: **re-baseline** (rescore the corpora with
the harness change alone) before scoring a rendering change against them.

## Reading the report

- **line parity**: reference lines that have an identical line in the candidate.
  Below 100% means line breaks differ; fix that before reading dy.
- **page parity**: matched lines that are also on the same page.
- **dy / dx**: candidate minus reference baseline / start-x, in points, over
  lines matched on the same page. The median is the systematic offset, the max
  the worst case.
- **pixel diff**: differing ink pixels over the union of ink pixels, worst page.
- Overlay: red = reference only, blue = candidate only, black = both.
