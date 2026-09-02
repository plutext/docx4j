# Maths from Markdown: docx, HTML and PDF with docx4j

*Reviewed against docx4j 17.0.4 (September 2026).*

## Why this matters

Large language models — ChatGPT, Claude and the rest — increasingly produce
**maths-heavy Markdown**: LaTeX in `$…$` and `$$…$$`, headings, lists, tables.
That's a fine interchange format, but people rarely want to *keep* it as
Markdown. They want:

- a **Word document** they can edit — with real, editable equations, not
  pictures of equations;
- an **HTML page** they can publish — with accessible, reflowable maths;
- a **PDF** they can hand out — with the equations properly typeset.

docx4j does all three from one Markdown source, **entirely on the JVM**, with no
external toolchain — no LaTeX/TeX installation, no Node or headless browser, no
pandoc binary — and no proprietary Microsoft stylesheets. It is Apache-2.0.

```
                      docx4j-markdown            docx4j-core            docx4j-export-fo
LLM Markdown ($-math) ───────────────► .docx ────────────► HTML ───┐    (jeuclid-fop)
                        LaTeX → OMML   (editable   OmmlToMathML     ├──► PDF
                                        Word eqns)  → MathML        │    MathML → SVG → FOP
                                                                    ┘
```

The `.docx` is the canonical artefact: equations in it are **native Word
OfficeMath (OMML)**, so a user can open the file and edit the maths in Word. HTML
and PDF are derived from the same model.

## How to do it

Add the module you need and call the facade.

**Markdown → docx** (needs `docx4j-markdown` on the classpath):

```java
WordprocessingMLPackage pkg = Docx4J.fromMarkdown(markdownString);
pkg.save(new File("out.docx"));
```

`$x^2+y^2$` and `$$\frac{-b\pm\sqrt{b^2-4ac}}{2a}$$` become native OMML equations
(a published LaTeX subset; see *Limitations*). Anything outside the subset falls
back to its literal source and is reported, so a stray exotic formula never
breaks the conversion.

**docx → HTML** (in `docx4j-core`; the default exporter):

```java
HTMLSettings s = Docx4J.createHTMLSettings();
s.setOpcPackage(pkg);
Docx4J.toHTML(s, outputStream, Docx4J.FLAG_NONE);
```

Equations come out as **native MathML** — no JavaScript needed to view them,
browser-rendered, reflowable and accessible.

**docx → PDF** (needs `docx4j-export-fo`, which pulls in the `jeuclid-fop`
renderer):

```java
Docx4J.toPDF(pkg, outputStream);
```

Equations are emitted as MathML inside `fo:instream-foreign-object` and rendered
to vector graphics by JEuclid, then placed by Apache FOP. If the jeuclid jars are
excluded, equations degrade to text rather than failing.

So the full chain is just:

```java
WordprocessingMLPackage pkg = Docx4J.fromMarkdown(md);   // → editable .docx
Docx4J.toHTML(html, htmlOut, Docx4J.FLAG_NONE);          // → MathML HTML
Docx4J.toPDF(pkg, pdfOut);                               // → math PDF
```

There is also an import direction — **XHTML (with MathML) → docx** — in the
`docx4j-ImportXHTML` module, for round-tripping web content into Word.

## What's under the hood (for the curious)

All of the maths conversion is native Java, clean-room from the specs
(ECMA-376 §22.1 for OMML, the W3C MathML spec), so nothing depends on Microsoft's
non-redistributable `OMML2MML.XSL` / `MML2OMML.XSL`:

- `docx4j-markdown` — Markdown ⇄ docx, incl. `$`-math ⇄ OMML (a LaTeX subset).
- `org.docx4j.convert.out.mathml.OmmlToMathML` — OMML → MathML (used by HTML and
  PDF; both HTML pathways emit it out of the box).
- `org.docx4j.convert.in.xhtml.math.MathMLToOmml` — MathML → OMML (XHTML import).
- `de.rototor.jeuclid:jeuclid-fop` — MathML → SVG for the PDF/FO pathway
  (Apache-2.0; ~540 KB net on top of the FO module's existing Batik/FOP).

Design notes live in `docs/developer/change-requests/` (CR-006-markdown-math,
CR-007-math-omml-mathml, CR-008-math-pdf-fo).

## How it compares

For getting maths-heavy Markdown into docx / HTML / PDF, the open-source (and one
notable commercial) options group like this:

| Approach | Runtime | docx (editable eqns) | HTML maths | PDF maths | Notes |
|---|---|---|---|---|---|
| **docx4j (this)** | **JVM only** | native OMML | native MathML (server-side) | OMML→MathML→SVG (JEuclid)→FOP | Apache-2.0; no external toolchain |
| Pandoc → LaTeX | pandoc + **TeX distro (~GB)** | native OMML | MathJax/KaTeX/MathML | LaTeX (gold standard) | most faithful PDF maths |
| Pandoc → Typst | pandoc + typst binary | via pandoc | — | Typst (fast, no LaTeX) | modern, growing ecosystem |
| Pandoc → browser | pandoc + Chromium/WeasyPrint | via pandoc | MathJax/KaTeX | rendered by browser | needs a browser engine |
| Quarto | pandoc + extras | via pandoc | KaTeX/MathJax | LaTeX/Typst | rich scholarly features |
| Node (md-to-pdf, mdpdf, …) | Node + Chromium | weak / via pandoc | KaTeX/MathJax | headless Chrome | HTML-first |
| **Aspose.Words** | JVM only | native OMML | MathML/image/text | **own engine (high fidelity)** | **commercial (~$1,175+/platform)**; markdown *LaTeX* import weak |

### Where docx4j is distinctive

It is, as far as we can tell, **the only free, open, pure-JVM, no-external-
toolchain path that produces all three — an editable-equation `.docx`, MathML
HTML, and a maths PDF — from maths-heavy Markdown.**

- **Pandoc** is the other tool that does all three well with editable docx maths,
  but it is an external binary, and its PDF path needs a **TeX distribution, or
  Typst, or a browser** — none a small dependency. docx4j is add-a-jar and deploy
  inside a Java service: no subprocess, no system installs.
- **Aspose.Words** matches the pure-JVM, no-toolchain profile and likely renders
  PDF maths *more faithfully* (its own dedicated engine), but it is **commercial
  and closed**, and — unusually — its Markdown importer does **not** turn `$`-math
  into equations (it drops to plain text), which is exactly docx4j's entry point.

### Where the others are stronger (honest)

- **PDF typographic fidelity**: pandoc→LaTeX is the gold standard; MathJax and
  Typst are excellent; Aspose's engine is strong. JEuclid (2010-era) is
  *good enough* — it rendered a 1,150-page, ~10,700-equation document cleanly in
  ~22 s — but not LaTeX-grade.
- **Maths dialect breadth**: pandoc/`texmath` cover a wide, mature LaTeX subset;
  docx4j's is a published (growing) subset with graceful fallback.
- **Scholarly features**: pandoc/Quarto win on citations, cross-references,
  filters and the sheer number of output formats.

### Choosing

- *"Markdown with maths → editable-equation docx, free, in the JVM, no external
  tools"* → **docx4j** (pandoc can too, but needs the binary; Aspose can't import
  the maths).
- *"docx with equations → the most faithful PDF/HTML, budget no object"* →
  **Aspose.Words**.
- *"Best-in-class maths typesetting, external toolchain acceptable"* →
  **pandoc → LaTeX / Typst** (or **Quarto** for scholarly work).

## Limitations

- **LaTeX subset**: the Markdown `$`-maths supported is a documented subset
  (fractions, scripts, roots, sums/integrals with limits, delimiters, matrices,
  common symbols, …). Out-of-subset maths falls back to its literal text and is
  reported, not silently dropped.
- **PDF fidelity**: JEuclid is solid but not LaTeX-grade; the renderer is
  pluggable in principle if higher fidelity is ever needed.
- **Long display equations don't line-wrap in PDF**: an equation becomes one
  atomic graphic in `fo:instream-foreign-object`, so a very long single equation
  overflows the page rather than wrapping. Everything within the page width
  renders correctly.

## See also

- `docs/Docx4j_from_Python.md` — driving all of the above from Python via JPype.
- `docx4j-markdown` module README — Markdown import/export options.
- `docs/developer/change-requests/CR-006-markdown-math.md`,
  `CR-007-math-omml-mathml.md`, `CR-008-math-pdf-fo.md` — the design records.
