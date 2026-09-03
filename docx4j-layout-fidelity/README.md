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

## Build and run

```bash
# upstream modules must be installed at the current ${revision} first.
# Use `clean`: the XJC plugin only checks xsd/ROOT.xsd for staleness, so after a pull
# that changed an imported schema (xsd/wml/wml.xsd etc.) an incremental build keeps
# the old generated classes and docx4j-core fails with "cannot find symbol".
mvn clean install -DskipTests -Dgpg.skip=true -pl docx4j-export-fo,docx4j-documents4j-local,docx4j-JAXB-ReferenceImpl -am
# then this module (not in the reactor, so build it from its directory)
cd docx4j-layout-fidelity && mvn -o -Dgpg.skip=true -DskipTests package dependency:build-classpath -Dmdep.outputFile=target/cp.txt

CP="target/classes:$(cat target/cp.txt)"
java -cp "$CP" org.docx4j.fidelity.Fidelity generate  target/corpus
java -cp "$CP" org.docx4j.fidelity.Fidelity run       target/corpus /path/to/goldens target/report [dpi]
```

On the Windows VM (Word installed, and the corpus fonts installed as system
fonts: Liberation Serif and Liberation Sans from `docx4j-export-fo-fonts-liberation`,
Carlito from `docx4j-export-fo-fonts-crosextra`, and DejaVu Sans, which is not in
any docx4j font module; use the host's `/usr/share/fonts/TTF/DejaVuSans*.ttf`):

```
java -cp "%CP%" org.docx4j.fidelity.golden.WordGoldenRunner <sharedFolder>\corpus <sharedFolder>\goldens
```

Commit the goldens and `golden-manifest.properties` together. Regenerate them
only when the corpus changes or Word on the VM is updated.

A stand-in reference for plumbing checks (never the target):

```bash
soffice --headless --convert-to pdf --outdir target/goldens-lo target/corpus/*.docx
```

## JUnit

`FidelityTest` generates and renders the corpus unconditionally, and compares
against goldens when `-Ddocx4j.fidelity.golden=<dir>` (or the
`DOCX4J_FIDELITY_GOLDEN` environment variable) is set. No tolerances fail the
build yet.

## Reading the report

- **line parity**: reference lines that have an identical line in the candidate.
  Below 100% means line breaks differ; fix that before reading dy.
- **page parity**: matched lines that are also on the same page.
- **dy / dx**: candidate minus reference baseline / start-x, in points, over
  lines matched on the same page. The median is the systematic offset, the max
  the worst case.
- **pixel diff**: differing ink pixels over the union of ink pixels, worst page.
- Overlay: red = reference only, blue = candidate only, black = both.
