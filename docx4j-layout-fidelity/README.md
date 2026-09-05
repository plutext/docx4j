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
| `compatMode` | the leading number of the name (`12_en-AU_...` → Word compatibility mode 12), blank if the name does not start with one |
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

## Reading the report

- **line parity**: reference lines that have an identical line in the candidate.
  Below 100% means line breaks differ; fix that before reading dy.
- **page parity**: matched lines that are also on the same page.
- **dy / dx**: candidate minus reference baseline / start-x, in points, over
  lines matched on the same page. The median is the systematic offset, the max
  the worst case.
- **pixel diff**: differing ink pixels over the union of ink pixels, worst page.
- Overlay: red = reference only, blue = candidate only, black = both.
