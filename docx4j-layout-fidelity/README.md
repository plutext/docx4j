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

The runner disables documents4j's PowerPoint bridge, because it converts nothing but
docx and PowerPoint has to run in the foreground: a PowerPoint first-run or activation
dialog stops the run before Word has been asked anything, and looks exactly like a hang.
`-Dpptx4j.documents4j.MicrosoftPowerpointBridge.enabled=true` puts it back. Each document
is named before it is handed to Word (`[2/194] resaving <id> (18 KB)`), so a stall says
which one, and a document Word refuses is named, recorded in the manifest with its whole
cause chain, and stepped over rather than ending the run.

**The resave goes through the same mechanism as the golden PDF**, which succeeds on every
document in the corpus: docx4j loads the package and documents4j saves it to a local temp
file for Word to open. Handing Word the corpus file instead - `updateDocx(File, ...)` -
fails on most of the corpus with `ConversionInputException: The input file seems to be
corrupt` (documents4j's report of `Documents.Open` returning an empty handle), identically
on every retry, whether the file is on the share or copied to a local disk first, and while
the very same document converts to PDF without complaint.

Nothing else separates the two paths. The content parts are **byte-identical** between a
corpus file and docx4j's re-save of it - `document.xml`, `styles.xml`, `settings.xml`, the
media - and the three package-metadata parts (`[Content_Types].xml` and the two `.rels`)
differ only in attribute order. So this is not docx4j repairing a broken document, and the
cause is still unknown; `-Dfidelity.resaveOriginalBytes=true` hands Word a local copy of
the file itself, for anyone who wants to chase it.

It does mean Word is shown docx4j's re-save rather than the corpus bytes - but so is the
golden PDF, so the two are consistent, and docx4j's save marshals the `w:tblGrid` it read
without recomputing it, which is what `GridDiff` asks about.

**Which means the goldens are Word's rendering of docx4j's re-save, not of the corpus
file.** `WordGoldenRunner` loads the docx with docx4j and passes the *package*, and
`Documents4jLocalServices.export(pkg, ...)` calls `Docx4J.save` into a temp file before
Word sees it. For comparing layout that is arguably the fairer reference - both sides
start from the same XML - but it does mean a defect in docx4j's *save* path is baked into
the reference and can never be scored against. Left as it is, because changing it would
invalidate every number measured so far; worth knowing before trusting a golden on a
question about what docx4j writes.

Two things to know before reading a diff. Word rewrites a great deal that is not a
computation - `w:rsid`s, attribute order, `w:proofState`, its own `w:compat` block - so
diff the elements you are asking about rather than the file. And **the resave does not
carry Word's updated field results**, although the PDF does. An earlier version of this
note said the opposite - that because the resave goes through the same conversion script
as the PDF, a ToC-updating `word_convert.vbs` would update the fields in the resaved docx
too. Verified, it does not: the re-saved `document.xml` holds the same stored results as
the original - a 179-page corpus document whose golden PDF prints `Error! Bookmark not
defined.` on every line of its table of contents has none of that string in either file,
and the stored `NUMPAGES` result is unchanged in every corpus document that has one. The
field update is done to the document Word converts, and what it saves back is the stored
result. So the field floor (a document's stored results against Word's recomputed ones)
is still there on the re-saved basis, and a question about what Word computed for a field
has to be asked of the PDF.

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

`ColumnError` turns the same comparison into a number:

```bash
java -cp "$CP" org.docx4j.fidelity.golden.ColumnError <corpusDir> <resavedDir> [out.csv]
```

`GridDiff` says *whether* Word rewrote a grid; this says how far docx4j's own answer is
from Word's. The reference is the resaved document's `w:tblGrid` — what Word computed,
in twips — and the candidate is the `fo:table-column` set docx4j produces for the same
table, read out of an in-process render of the **original** to XSL-FO. Neither side is a
rendering, so it measures the column sizer on its own: no fonts, no FOP, no page.

Tables are paired by index, Word's in document order against ours in FO pre-order, with
the same `ClassFinder` traversal `GridDiff` uses so nested tables line up. A document
whose two counts differ is skipped whole rather than compared out of step (a table in a
header, a footnote or a text box is in the FO but not in the main document part), and the
wrapper tables `WordLayoutFixups` builds around a floating table or an image are not
counted — only the table writer puts `column-number` on a column, which is how they are
told apart. It reports each table's per-column ratio, `maxAbsRatioErr` and total twip
error, worst first, then an aggregate split by whether Word kept the declared grid: where
it did, an error is docx4j declining to use a grid Word was happy with; where it did not,
it is one sizer against the other. The optional CSV lets two runs be diffed.

**What a number from it is, and is not.** A grid Word kept is a real reference - checked
against the table's container it is consistent with it, a nested table's kept grid being
the containing cell's width (or that less one pair of cell margins) and a top-level one
the section's text column or the percentage of it the table asks for. What a kept grid
does *not* license is reading the twips as a cost. **Column error in twips is close to
uncorrelated with the line parity a document loses**: joined to the `b28-l1b` scoreboards
over 350 documents, Pearson r between a document's kept-grid error and its line-parity
deficit is 0.05. Three reasons, all measured:

- **A column only costs lines if its content can wrap.** One corpus document is a form of
  nested digit boxes - one character per cell - and carries 39,888 tw of error over 22
  tables at line parity 0.9286. Its cells cannot wrap at any width, so the whole of that
  error is free. (docx4j is genuinely wrong there - it resolves a nested table's
  `w:tblW pct` against the page rather than the containing cell - but that document is the
  wrong place to look for what fixing it would be worth.)
- **`maxAbsRatioErr` is a ratio, so a hairline column dominates it.** One table scores 29.6
  on a 7-twip column while the table as a whole is 781 tw out. Rank by `sumAbsDiffTwips`;
  use `maxAbsRatioErr` only to see the *shape* of a disagreement.
- **The aggregate is dominated by single documents.** 45% of all the column error over the
  three real-document corpora is one document, and 705 of the 719 tables in the largest
  single error class are that same document. A rule inferred from the totals is often a
  rule inferred from one author's template; count the documents a class spans before
  believing it.

So use it to say *which sizing rule* docx4j has wrong and over how many documents, and go
to the scoreboard - never to this CSV - for what fixing it is worth.

`ShortfallFit` goes one step further in, for the tables Word had to *squeeze*:

```bash
java -cp "$CP" org.docx4j.fidelity.golden.ShortfallFit out.csv <corpusDir> <resavedDir> [<corpusDir> <resavedDir> ...]
```

`ColumnError` sees only what docx4j chose; this sees what the column sizer *saw*. It renders
each document with `docx4j.convert.out.fo.wordLayout.dumpAutofit` set, which has
`AbstractTableWriter` append one record per table - the per-column content minima, maxima,
preferred widths and floors (cell margins plus any picture) it measured, the width it fitted
them into, and what it chose - joins each record to the Word grid `ColumnError` pairs it with
(by the final widths, which are the `fo:table-column` widths in twips exactly), and keeps the
tables whose measured minima exceed the width Word gave the table. Two populations come out:
the tables docx4j's content pass sized, and the autofit tables whose cached grid Word rewrote
narrower on re-save. Over both it scores a list of candidate distribution rules - proportional
to the minima, equal, powers of the minima, floor-plus-proportional, `AutofitLayout.squeeze` -
on *shares*, so the total (which the page fit decides) does not enter, and prints per rule
the per-column and per-table error against Word's grid and how many tables land within 1% and
5%. The dump property costs nothing when unset and changes no output when set; the CSV holds
both populations so a new rule can be tried without re-rendering. The finding it was built
for is in `docx4j-export-fo/docs/word-layout-rules.md` §6.5: the population is tiny (seven
multi-column tables in four documents, and six in three), and Word keeps each column's cell
margins whole and squeezes only the text.

`-Dfidelity.allTablesCsv=<file>` has `ShortfallFit` also write every table it paired and
joined - shortfall or not - with the sizer's inputs beside Word's grid and whether Word
rewrote it, so a question about the whole population (does a candidate rule reproduce the
grids Word *kept*?) can be asked of one CSV without rendering again.

`RowGridDiff` asks about the rows rather than the table:

```bash
java -cp "$CP" org.docx4j.fidelity.golden.RowGridDiff out.csv <corpusDir> <resavedDir> [<corpusDir> <resavedDir> ...]
```

A row's `w:tcW` need not agree with the `w:tblGrid`, and the question was whether Word then
lays that row out on its own widths - which would give a table more than one column geometry,
and XSL-FO none of the means to draw it. For every table it computes each row's boundaries
from its `dxa` `w:tcW` (with `w:gridBefore`/`w:wBefore` and the spans) against the grid
columns the cells cover, counts the rows that disagree (by more than max(10 twips, 1%)) and
the distinct geometries among them, and reads what Word drew off the re-saved grid - which
has one geometry per table, so a row laid out on its own widths would show as a grid of more
columns carrying the union of the rows' boundaries. Word's grid is compared, as a set of
interior boundaries, with the declared grid, the widest disagreeing row, the union of the rows
and the row's widths scaled to Word's total, by Hausdorff distance in twips. The finding is in
`word-layout-rules.md` §6.3: 1,474 of 5,866 tables have a disagreeing row, Word draws the grid
for 1,443 of them, and it changed a grid's column count in two - one geometry per table,
always.

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

## What to score: the re-saved docx, not the corpus file

**Score docx4j's rendering of the docx Word itself wrote.** The question the harness exists to
answer is whether docx4j lays out a Word-produced document the way Word lays it out; that a
third-party corpus file also renders well is a bonus, not the test. `score` takes the corpus
directory as an argument, so this is a matter of pointing it at the `resaved` directory beside
the goldens rather than at `corpus`:

```bash
java -cp "$CP" -Dfidelity.hyphenate=false org.docx4j.fidelity.Fidelity score \
    <shared>/real2/resaved <shared>/real2/goldens <outDir> <baseline.csv>
```

What follows from it is that Word's re-save normalises styles, numbering, the `w:tblGrid` and
`w:compat`, which removes docx4j's *interpretation of ambiguous input* from the comparison and
leaves the layout engine on its own. The corollary is that a defect which only bites on
unnormalised input no longer shows up, so a finding of that kind is worth recording where it
will not be lost.

What does **not** follow - and an earlier draft of this section said it did - is that the field
floor goes away. The re-save does not carry Word's updated field results: the re-saved
`document.xml` holds the same stored results as the original (see "Having Word save the docx
too" for the verification), so a TOC or a `NUMPAGES` that the document stored stale is still
scored against the value Word recomputed for the PDF, on this basis as on the old one.

One asymmetry to know about: the golden PDF was cut from docx4j's re-save of the **original** (see
"Having Word save the docx too"), while the `resaved` docx is Word's save of that same package. The
two are very close but not literally the same bytes, so a discrepancy that looks like it comes from
that gap rather than from layout is a reason to re-cut the goldens from the re-saved files with
`WordGoldenRunner`, not a reason to chase it in the exporter.

### The b35-resaved baseline, and what the change of basis was worth

Every baseline up to b34 scored the corpus file and is void on this basis. `b35-resaved` is the new
zero: the re-saved documents against the same goldens, with the NBSP and line-number normalisations
below in, hyphenation off for the corpora (their goldens were cut without proofing tools) and as the
document asks for the probes (theirs were not - `-Dfidelity.hyphenate=false` on the probes wrecks the
two hyphenation probes, 0.95 and 1.00 to 0.25 and 0.30, and is an artefact of the flag, not of the
basis). The three documents Word refused to re-save have no row.

| | scored | same page count | lines matched | median | mean | >= 0.98 |
| --- | --- | --- | --- | --- | --- | --- |
| corpus 1 | 191 | 167 (87.4%) | 36317/40339 (90.0%) | 0.9318 | 0.8958 | 57 |
| corpus 2 | 156 | 120 (76.9%) | 60860/71695 (84.9%) | 0.9231 | 0.8721 | 29 |
| corpus 3 | 102 | 67 (65.7%) | 172191/186225 (92.5%) | 0.9293 | 0.9034 | 24 |
| probes | 76 | 75 (98.7%) | 4581/4670 (98.1%) | 1.0000 | 0.9784 | 64 |

What the basis alone was worth - the same documents, the same harness, the corpus file rendered
against the re-saved one (`rescore` of the b34-seam renders against `score` of the re-saved files,
so the extractor is the same on both sides):

| | same page count | lines matched | mean parity | >= 0.98 | moved > 0.02 |
| --- | --- | --- | --- | --- | --- |
| corpus 1 | 165 -> 167 | 36216 -> 36317 | 0.8947 -> 0.8958 | 54 -> 57 | 7 up, 3 down |
| corpus 2 | 117 -> 120 | 60328 -> 60860 | 0.8691 -> 0.8721 | 30 -> 29 | 6 up, 5 down |
| corpus 3 | 65 -> 67 | 171852 -> 172191 | 0.9015 -> 0.9034 | 24 -> 24 | 3 up, 1 down |
| probes | 76 -> 75 | 4526 -> 4581 | 0.9695 -> 0.9784 | 58 -> 64 | 5 up, 1 down |

So the change of basis is worth about +0.001 to +0.003 of mean parity and one to three page counts
per corpus - small in aggregate, and that is the finding: most of the gap to Word is layout, not our
reading of the input. It is not uniform, though, and the documents that moved are the list of places
where the input was being mis-read rather than mis-laid-out. Sixteen documents rose by more than
0.02 (up to +0.36 on one whose page count went 19 to 16 against Word's 15, and +0.15 on one that
went 51 to 54 pages against Word's 54): re-saved, their `w:tblGrid`, numbering and compatibility
settings are Word's, and our rendering of Word's version is closer. Nine fell (up to -0.11 on a
one-page table document, and one that was 1.0000 on the corpus file and is 0.9427 on the re-save):
those are the documents where Word's normalisation exposes a rule we get wrong on *normalised*
input while an accident of the original hid it, and they are the ones to look at next. Among the
probes, `table-cell-pct` goes 0.53 to 1.00 - the grid Word wrote is the one it draws, exactly the
finding "Having Word save the docx too" was built on - and `table-floating-anchor` loses a page.

For the two normalisations that shipped with it, measured on their own over the old renders, see
`-Dfidelity.nbspNormalise` and `-Dfidelity.lineNumberNormalise` under "How a line is extracted and
paired". Together they were worth corpus 1 0.8937 -> 0.8947, corpus 2 0.8589 -> 0.8691 and corpus 3
0.8929 -> 0.9015 of mean parity, almost all of it the two line-numbered documents, with no document
down and no line lost anywhere but the number-only lines those two drop from the reference.

## Is Word's rendering invariant under its own re-save? (`ResaveInvariance`)

Two questions this harness cannot answer from what it has already measured, and one tool
that answers both. It runs on the Windows VM, because it needs Word:

```
java -cp "%CP%" org.docx4j.fidelity.golden.ResaveInvariance <docxDir> <goldensDir> <outDir> [count | --only=<id>[,<id>...]] [--force] [--no-word]
```

**Question one: is `PDF(original)` the same as `PDF(resaved)`?** Word normalises a document as
it loads it - styles, numbering, the autofit grid, `w:compat` - and it writes both the PDF and
the re-saved docx out of that one in-memory model, so the two renderings ought to be the same
pages. They had better be: the harness now scores docx4j's rendering of the **re-saved** file
against goldens cut from the **original** (see "Having Word save the docx too"), which is only
like-for-like if Word's rendering is invariant under its own round trip. If it is, nothing has
to be re-cut. If it is not, then whatever differs is something Word computes at render time and
does not persist into the docx - which is worth knowing precisely, because a docx4j render can
only work from what was persisted.

**Question two: what does Word do with fields here?** The goldens carry Word's recomputed field
results if the conversion script updated them; the docx on both bases carries the stored ones
(the resave does *not* carry the updated results - see the same section). The visible form of
that gap is field-error text: one corpus document has lost its bookmarks, so its golden is full
of `Error! Reference source not found.` - about 1,160 lines of it - which appears nowhere in the
docx and can never match. So the tool counts the field-error lines on each side and says whether
they agree.

It picks a handful of documents (five, or `count`) that have both an `<id>.docx` in `docxDir` and
an `<id>.pdf` in `goldensDir`, **by size**: the smallest, the largest and an even spread of ranks
between, printed with the reason each was picked. Alphabetical order carries no information about
a document, so the first five of it can easily be five short ones, and a one-page document cannot
show a page break moving.

**`docxDir` is the re-saved directory for the question and the corpus directory for the control**,
and `--only=<id>[,<id>...]` (or the ids as positional arguments) names the documents outright
instead of sampling. Use it whenever the two bases are being compared: size sampling is
deterministic but *not* comparable across bases, because Word's re-save changes a document's size
and so its rank, and the first control run therefore picked five different documents from the run
it was meant to control - the document that differed was never controlled at all. Running one
named document from `corpus` and from `resaved` against the same golden settles it: if the corpus
rendering is identical and the re-saved one differs, the re-save is the variable.

Two values in a PDF belong to the run rather than to the document, and both are masked on both
sides before the comparison - and reported, because silently dropping content is worse than
saying what was dropped. One is documents4j's temp file name: the package is saved to
`docx_<random>.docx` before Word is given it, so a `FILENAME` field prints a different name in
every PDF ever cut, and one of the first run's two "differences" was nothing but that (plus a
`DATE` field). The other is the date itself, collapsed by the extractor's own normalisation, the
same one `score` relies on - so `-Dfidelity.dateNormalise=false` will make a `DATE` field read as
a divergence, and the run says so when it is set.

Each PDF is cut by the route `WordGoldenRunner` uses - docx4j loads the package and documents4j
hands Word a temp file, because handing Word the corpus file itself fails on most of the corpus -
into `outDir`. Each document is named before Word is given it, so a stall says which one, and a
document Word refuses is stepped over rather than ending the run. A PDF already in `outDir` is
kept unless `--force`; `--no-word` skips the conversion and just re-reads a finished run, which
works on a machine with no Word.

Reading the output. Per document, either

```
  <id>: identical - 24 pages, 1204 lines
```

or the three things that differ, from the coarsest in: page count, the pages whose line count
differs, and the first line that differs with its page, y and text on each side. Then the same
document scored the way `score` scores it (`LayoutComparison` over `PdfLayoutExtractor`'s lines,
which is literally the scoreboard's own comparison), so a document that is *not* identical is
described in the numbers the scoreboard uses. Then the field-error census for that document.
Finally a total per question: how many documents rendered identically, and whether the two sides
agreed about field errors.

A run also warns, loudly, when there is **no `docx4j.properties` on the classpath**. That matters
more than it looks: the field-updating conversion script is named in `docx4j.properties`, so
without it the tool reports "documents4j's default script (no field update)" and Word updates
nothing - which may not be how the goldens were cut, and would make any field difference the run
reports an artefact of the run. Put a directory holding `docx4j.properties` first on the
classpath (`-cp "conf;target\classes;target\lib\*"`), or name the script with
`-Dfidelity.fieldUpdateScript=`.

**A field error is not by itself evidence that Word updated anything**, which is why the two
outcomes are counted apart. Where both sides carry the *same* errors they are the document's own:
an error string the author saved as the stored result, which every renderer prints because it is
what the docx says - one control document showed fifteen of them on both sides with no field
update configured at all. Where the golden carries *more*, a field update recomputed them when it
was cut, and the reference is showing text the docx does not contain, which docx4j can never
match: the document whose golden holds some 770 of them against stored results that are not
errors is that case. "The document is broken" and "the reference is wrong" are opposite findings,
so the run reports them as two numbers.

Two limits on the field census, both stated in its output. A line carrying an error string is
counted once, so an error that wrapped onto a second line counts once and not twice; and only the
English and French wordings are matched exactly (`Error! Reference source not found.`,
`Error! Bookmark not defined.`, `Erreur ! Source du renvoi introuvable.`,
`Erreur ! Signet non défini.`). A line in another language is counted separately as a *marker*,
on the `Error!` / `Erreur !` / `Fehler!` it opens with, which is weak evidence on its own - an
ordinary sentence can begin that way too.

### Turning the field update on and off

Which is the measurement that decides the field question, so the tool selects the conversion
script itself:

```
rem with Word's field update
java -cp "%CP%" -Dfidelity.updateFields=true  org.docx4j.fidelity.golden.ResaveInvariance <resaved> <goldens> <out>\fields-on 5
rem without it
java -cp "%CP%" -Dfidelity.updateFields=false org.docx4j.fidelity.golden.ResaveInvariance <resaved> <goldens> <out>\fields-off 5
```

Two PDFs of the same document, and the difference between them is exactly what Word's field
update does to the page. (Use two output directories: the PDF is named `<id>.pdf` in both.)
Every run prints which script it used, whichever way it was chosen.

The mechanism, because it has three traps in it:

- The script is `com.documents4j.conversion.msoffice.word_convert.vbs`, a **system** property.
  `Documents4jLocalServices` sets it from `docx4j.properties` if it is not already set, so
  a value in `docx4j.properties` is the fallback, not the winner: a system property beats it.
- documents4j materialises the conversion script **once**, in the bridge constructor
  (`AbstractMicrosoftOfficeBridge`), which is to say when the `LocalConverter` is built. So the
  choice has to be made before the first conversion and cannot be changed within a run - hence
  one mode per run and two runs to compare, rather than a flag per document.
- `-Dfidelity.updateFields=false` therefore cannot simply clear the property: `docx4j.properties`
  would put the configured script back. Instead documents4j's own bundled `word_convert.vbs` is
  unpacked from the classpath into `outDir` and pointed at explicitly. That script opens, saves
  and closes, and touches no field.

`-Dfidelity.updateFields=true` uses the script named by `-Dfidelity.fieldUpdateScript=<path>` if
you give one, and otherwise writes one into `outDir` - documents4j's default plus a field update.
Writing our own rather than using the sample in `docx4j-samples-resources` is deliberate: that
sample calls `wordDocument.TablesOfContents(1).Update` inside the `On Error Resume Next` block
that precedes its `Err` check, so **a document with no table of contents quits -2**, which
documents4j reports as `The input file seems to be corrupt`. The generated script updates every
story range's fields (the body, but also the headers, footers and footnotes, which
`Document.Fields` alone does not reach) and every TOC, then clears `Err`, so a document with
neither is converted rather than failed.

Leaving `fidelity.updateFields` unset changes nothing about the machine's configuration and
prints what that resolves to - which is itself worth running once, since **whether the goldens
were cut with a field-updating script has never been checked**, only assumed.

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

`rescore` is `score` without the render:

```bash
java -cp "$CP" org.docx4j.fidelity.Fidelity rescore <corpusDir> <refPdfDir> <candPdfDir> <outDir> [baseline.csv]
```

`candPdfDir` is a previous run's `fop` directory, so a change to the extractor or the pairing
- a measuring change, not a rendering one - can be re-baselined on the very renders the
baseline was cut from, and its worth read on its own. That is how the two normalisations
below were separated from the change of basis they shipped with. A document with no
candidate PDF is one `error` row.

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
- **`-Dfidelity.nbspNormalise=false`** turns off the folding of the three no-break
  spaces (U+00A0, U+2007, U+202F) to an ordinary space before a line's whitespace
  is collapsed. `\s+` is exactly the set `Character.isWhitespace` accepts, which
  excludes those three by definition, so a `1 000` written with the document's own
  U+00A0 on our side and with a plain space in Word's PDF read identically and never
  paired. It is done to both sides in the text assembly, so the reported text is the
  folded one too. Measured by `rescore` over the b34-seam renders: on the 95 corpus
  documents holding ten or more of them, mean parity 0.8351 -> 0.8478, +145 lines
  matched, ten documents up and none down, one of them 0.105 -> 0.947; 173 documents
  contain at least one.
- **`-Dfidelity.lineNumberNormalise=false`** keeps Word's **line numbers**
  (`w:lnNumType`) in the reference. **This one is a declared floor, not a
  measurement fix.** docx4j cannot render line numbering - there is no support for
  it, and XSL-FO and FOP have no facility for it - so a document that numbers its
  lines has a number in Word's margin on every line and nothing on ours; the
  extractor reads Word's number as a prefix on every line (or as a line of its own
  where the gap to the text is 20pt or more, and always on an empty paragraph), and
  not one line pairs. Dropping the numbers is the harness choosing not to count a
  defect it knows about. On each page the text's left edge is the leftmost ink that
  is not a leading run of one to three digits; a leading run whose glyphs lie wholly
  left of that edge is dropped from its line, and a number-only line there is
  dropped whole - but only where the page has at least two such lines, their numbers
  increase down the page and their right edges agree, which is what line numbers do
  (Word sets them flush right at a fixed distance from the text) and a column of
  data or a page of `1 Scope` entries does not. A heading that starts with a number
  sits at the edge, not left of it, and is untouched. Symmetric, as the rest are:
  ours never has a number to drop, but the same test runs on both sides. The two
  corpus documents that set it score **0.1661 and 0.1158 with the numbers counted,
  0.7386 and 0.9913 with them dropped** (b34-seam renders, `rescore`); the rest of
  the corpora is untouched by it. Anyone measuring what line-numbering support
  would be worth should switch it off first.
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

All of these are measuring, not rendering: **re-baseline** (`rescore` the corpora
over the previous baseline's renders with the harness change alone) before scoring a
rendering change against them.

## Reading the report

- **line parity**: reference lines that have an identical line in the candidate.
  Below 100% means line breaks differ; fix that before reading dy.
- **page parity**: matched lines that are also on the same page.
- **dy / dx**: candidate minus reference baseline / start-x, in points, over
  lines matched on the same page. The median is the systematic offset, the max
  the worst case.
- **pixel diff**: differing ink pixels over the union of ink pixels, worst page.
- Overlay: red = reference only, blue = candidate only, black = both.
