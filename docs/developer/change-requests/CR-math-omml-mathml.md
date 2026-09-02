# CR: Native OMML ⇄ MathML (drop the Microsoft XSLT dependency)

Status: **PROPOSED** 2026-09-02. No code yet.

## 1. Background

Word stores equations as OMML (Office Math Markup Language,
`http://schemas.openxmlformats.org/officeDocument/2006/math`, ECMA-376 Part 1
§22.1). The web/interchange vocabulary for the same content is W3C MathML. The
long-standing bridge between the two has been a pair of XSLT stylesheets shipped
*inside Microsoft Office*:

- `OMML2MML.XSL` — Word OMML → MathML (the export direction)
- `MML2OMML.XSL` — MathML → Word OMML (the import direction)

docx4j has pointed users at these stylesheets for ~15 years (forum threads
t293 2009, t966 2012, t2275 2015, t2293 2015, t2691 2018 for export; xhtml-import
t3065 2022 and t3133 2025 for import). The recommended recipe was to drop
Microsoft's `.XSL` into the docx4j resources, rename it (Maven resource handling
and, in `docx2xhtml-core.xslt`, an `xsl:include`), and change its output
encoding from UTF-16 to UTF-8.

**The problem is licensing, not capability.** Those stylesheets are part of
Microsoft Office and are *not redistributable* under docx4j's Apache-2.0 licence.
So today:

- Export: `docx4j-core/.../convert/out/html/docx2xhtml-core.xslt` carries the
  MathML handling **commented out**, in three blocks, with the note *"this file
  is part of Microsoft Office, and not provided in docx4j. Change it to output
  UTF-8"*. Math → MathML only works if the user obtains `OMML2MML.xslt`
  themselves and uncomments.
- Import (`docx4j-ImportXHTML`): the MathML → OMML path has historically needed
  the user to supply `mml2omml.xsl` (the odd `.xslZ` / rename arrangements seen
  in later versions are workarounds around the same redistribution barrier).

So math conversion does not work out of the box in either direction, and the
friction is entirely a redistribution-licence artefact.

**Precedent that native Java is the right fix.** docx4j already manipulates OMML
natively as JAXB (`org.docx4j.math.*`: `CTOMath`, `CTF`, `CTNary`, `CTRad`,
`CTSSub`/`CTSSup`/`CTSSubSup`, `CTD`, `CTM`/`CTMr`, `CTEqArr`, `CTR`/`CTText`,
…). The `docx4j-markdown` module (2026-09, CR-markdown-math) already contains
`LatexToOmml` and `OmmlToLatex`, which build and read that model in both
directions for a LaTeX subset. Re-implementing OMML ⇄ MathML in Java is squarely
in the same territory — and easier, because both OMML and MathML are structured
XML trees with close semantic correspondence, so there is no string grammar to
parse (unlike LaTeX).

## 2. Goal / non-goals

Goal: two Apache-licensed, dependency-free Java converters that make math
conversion work out of the box, removing the Microsoft-XSLT requirement:

- **OMML → MathML** (export): `CTOMath` / `CTOMathPara` → MathML (as a W3C DOM
  fragment, with a string convenience). Consumed by the HTML exporters; also a
  building block for OMML → MathML → LaTeX/SVG pipelines.
- **MathML → OMML** (import): a MathML DOM subtree → `CTOMath`. Lives in
  `docx4j-ImportXHTML`, replacing the XSLT it invokes today.

Non-goals: MathML *content* markup (only Presentation MathML is in scope, which
is what Word round-trips); LaTeX (already covered by docx4j-markdown, and
reachable by chaining); rendering to SVG/PNG (out of scope — MathML is the
interchange target and browsers/FOP render it).

## 3. Legal design principle (clean-room) — load-bearing

The entire point is to escape Microsoft's licence, so the implementation **must
not be a translation of `OMML2MML.XSL` / `MML2OMML.XSL`**. A line-by-line port
of their logic would be a derivative work of a copyrighted stylesheet and would
reintroduce exactly the taint we are removing.

Therefore:

- Implement **clean-room from the public specifications**: ECMA-376 Part 1 §22.1
  (and the MS-OMML notes) for OMML semantics and defaults; the W3C MathML Core /
  MathML 3 Presentation spec for MathML.
- Microsoft's stylesheets (or Word itself) may be used only as a **black-box
  conformance oracle** — feed known OMML through them to obtain expected MathML,
  and keep those input/output pairs as test vectors — never as a source to read
  and transcribe.
- Consequently, when Jason offers copies of the XSLTs: they are useful for
  *generating test vectors and spotting behaviours to cover*, but should not be
  the implementation basis. Sample OMML↔MathML *pairs* (produced by Word) are the
  cleaner artefact to work from.

This principle is called out here so it is not quietly eroded during
implementation.

## 4. Design

### Placement and API

- **OMML → MathML** in `docx4j-core`, e.g.
  `org.docx4j.convert.out.mathml.OmmlToMathML`:
  - `Document toMathMLDocument(CTOMath oMath)` / `toMathMLDocument(CTOMathPara)`
    → a `math` element in namespace `http://www.w3.org/1998/Math/MathML`.
  - `String toMathMLString(...)` convenience.
  - No new dependencies: reads the JAXB model, writes a W3C DOM via `XmlUtils`.
- **MathML → OMML** in `docx4j-ImportXHTML`, e.g.
  `org.docx4j.convert.in.xhtml.math.MathMLToOmml`:
  - `CTOMath toOmml(Element mathElement)` — takes the `<math>` DOM subtree the
    XHTML importer already has, returns `CTOMath` for insertion into a run.

Both are pure model↔DOM transforms; no I/O, no engine.

### Wiring

- HTML export: the **visitor** exporter (`AbstractVisitorExporterGenerator`,
  now the default since 17.0.4) gains a case for `m:oMath` / `m:oMathPara` that
  calls `OmmlToMathML` and appends the MathML — giving the non-XSLT pathway
  native MathML the XSLT pathway never had out of the box. (The XSLT pathway's
  commented-out include can stay as-is, or be pointed at nothing; not required.)
- XHTML import: the importer's MathML handling calls `MathMLToOmml` instead of
  applying `mml2omml.xsl`, and drops the bundled/renamed stylesheet.

### Element correspondence (representative, not exhaustive)

| MathML (presentation) | OMML (`org.docx4j.math`) |
|---|---|
| `mi` / `mn` / `mo` (text) | `CTR` + `CTText`, with `mathPr`/`sty` from `mathvariant` |
| `mrow` | grouping (often implicit; `CTOMathArg` content list) |
| `mfrac` | `CTF` (num `CTNum`, den `CTDen`; `CTFPr` bar/skewed/linear) |
| `msqrt` / `mroot` | `CTRad` (`degHide` for msqrt) |
| `msub`/`msup`/`msubsup` | `CTSSub` / `CTSSup` / `CTSSubSup` |
| `munder`/`mover`/`munderover` | `CTLimLow`/`CTLimUpp`, or `CTNary` limits, or `CTAcc`/`CTBar` |
| `mfenced` / paired stretchy `mo` | `CTD` (`begChr`/`endChr`/`sepChr`) |
| big operator + limits (`msubsup`/`munderover` on `mo`) | `CTNary` (`chr`, `limLoc`, `subHide`/`supHide`) |
| `mtable`/`mtr`/`mtd` | `CTM`/`CTMr`/`CTE` (matrix) or `CTEqArr` (aligned equations) |
| `mover`/`munder` accents/bars | `CTAcc` (`chr`), `CTBar` (`pos`) |
| `mtext` | `CTR` text with normal (`nor`) style |
| named entities (`&alpha;`, `&sum;`, …) | Unicode text (entity → codepoint map) |

### Character / style handling

- A named-entity → Unicode table (MathML entities like `&alpha;`, `&rightarrow;`,
  operator chars) for the import direction; the export direction emits Unicode
  (or numeric entities) directly.
- `mathvariant` (normal/italic/bold/script/…) ⇄ OMML run `mathPr` (`sty`,
  `scr`, `nor`). Word's default is italic for single-letter identifiers; the
  converters must apply the same defaults so round-trips are stable.

### Failure policy

Follow the CR-markdown-math precedent: never crash the whole conversion on a
math construct outside the supported set. On something unmappable, fall back
(export: emit the equation's text/`mtext` or skip with a marker; import: keep the
MathML as-is or drop with a logged event) and report via an issue/event listener,
so a document with one exotic equation still converts.

### Reuse

- Both directions use the existing `org.docx4j.math` JAXB model and `XmlUtils`
  for DOM.
- OMML-building helpers overlap with `docx4j-markdown`'s `LatexToOmml`. If the
  overlap is substantial, factor the shared OMML construction/reading helpers
  into `docx4j-core` (e.g. `org.docx4j.math.util`) so markdown, the MathML
  converter, and ImportXHTML share one implementation. Decide during Phase 1.

### Test corpus (and its own licensing)

Two classes of corpus, kept separate because the ready-made ones are copyleft:

- **Vendored into docx4j (Apache-safe, checked in):**
  - **W3C MathML Test Suite** (W3C 3-clause BSD + W3C Document Licence —
    Apache-compatible). Use the **MathML 2 suite** (`.../Math/testsuite/
    mml2-testsuite/`), **Presentation chapter only** — it splits General/
    Presentation/Content/Characters cleanly and names cases by element
    (`mfrac1.xml`, `msub1.xml`, `mtable1.xml`…), so curating the subset that
    matches §4's correspondence table is trivial. Skip Content (Word converts
    presentation, not content markup), `maction`, and most error/stress cases;
    keep a few Characters cases to exercise the entity→Unicode map. The MathML 3
    "full" suite (build/main, ~1,675 cases) is character/conformance-heavy and
    not worth the filtering; the presentation vocabulary is identical across
    v2/v3 anyway. MathML-only, so no OMML pairs; we author the expected OMML.
  - **Our own Word-derived pairs** — the primary, clean corpus, generated by
    running MathML through Word as a black-box (never MS's XSLT source). Word
    gives us *both* directions:
    - **MathML → OMML**: paste each MathML case into Word (Word 2605+ auto-
      converts pasted MathML to an OfficeMath equation, internally via
      `MML2OMML.XSL`), save the `.docx`, and docx4j extracts the OMML. Input
      MathML from the W3C suite (and other permissively-licensed math pages) →
      Word-produced OMML = an import pair.
    - **OMML → MathML**: enable Word's *"Copy MathML to the clipboard as plain
      text"* (Equation Tools, one-time), copy each equation, capture Word's
      MathML = the export oracle for that OMML.
    Each W3C case thus yields a triple — original MathML → Word → OMML → Word →
    Word-MathML — so `MathMLToOmml` is checked against Word's OMML and
    `OmmlToMathML` against Word's MathML. Batch it with a one-off Word VBA macro
    over a folder of `.mml` files (Windows + Word); curate ~100 representative
    cases by hand first. We own and Apache-licence these output fixtures (the W3C
    MathML inputs credited to W3C); this is the gold oracle for Word's `mathPr`
    defaults and is clean-room-safe. Note: reliable conversion is via clipboard
    *paste* of the MathML, not opening an HTML page in Word (whose web import does
    not dependably turn MathML islands into OfficeMath).
- **External cross-check oracles only (NOT vendored — copyleft):**
  - **texmath** (jgm) `test/reader/{omml,mml}`, `test/writer/{omml,mml}`,
    `test/roundtrip/*.native` — an independent (non-Microsoft) OMML↔MathML
    implementation with paired golden files. **GPL-2.0**, so we must not copy its
    files or code into the repo; use it out-of-tree during development to compare
    our output on shared inputs. Running it as a black-box oracle does not make
    our code a derivative work.
  - **fiduswriter/mathml2omml** (JS, MathML→OMML) — **LGPL-3.0**; reference for
    import behaviour only, same not-vendored rule.

## 5. Phases

1. **Test corpus + skeleton.** DONE (commit 4342c45f7). 25-case corpus committed
   under math-corpus-tools/corpus (mml/omml/word-mml); tooling
   (MathmlToDocx.bas, OmmlFixtureExtractor.java); copyleft suites left as
   out-of-tree oracles.
2. **OMML → MathML core.** DONE. `org.docx4j.convert.out.mathml.OmmlToMathML` +
   `MathConversionException` in docx4j-core: runs (mi/mn/mo/mtext) with
   mathvariant, fractions (incl. bevelled/linear/no-bar), radicals (msqrt/mroot),
   sub/sup/subsup, n-ary with sub/undOvr limits, delimiters. Emits a W3C DOM /
   string; styles as `mathvariant` (Word uses the Alphanumeric Symbols block —
   equivalent). NOT yet wired into the HTML visitor exporter (follow-up).
3. **OMML → MathML advanced.** DONE (same pass): matrices (CTM/mtable),
   eqArr, prescripts (CTSPre/mmultiscripts), function apply (CTFunc), accents,
   bars, limLow/limUpp, groupChr, phantom, box/borderBox. All 25 corpus cases
   convert to well-formed MathML (`OmmlToMathMLTest`), many byte-identical to
   Word's oracle. Remaining: wire into the visitor HTML exporter; broaden beyond
   the corpus subset as bugs surface.
4. **MathML → OMML core + advanced** (in `docx4j-ImportXHTML`): the reverse of
   phases 2–3; replace the `mml2omml.xsl` invocation and remove the bundled
   stylesheet. Round-trip tests (OMML → MathML → OMML) for stability.
5. **Docs + wiring cleanup.** Update the commented-out blocks in
   `docx2xhtml-core.xslt` (native path now covers it), the ImportXHTML README,
   and the forum-referenced "bring your own OMML2MML.XSL" guidance. CHANGELOG.

## 6. Risks / notes

- **Fidelity of edge cases.** Operator stretchiness/spacing, nary limit
  placement, matrix/eqArr alignment, and Word's implicit mathPr defaults are the
  intricate parts. Mitigated by the Word-oracle test corpus; accept that not
  every exotic construct round-trips byte-identically (semantic equivalence is
  the bar).
- **Round-trip stability.** OMML → MathML → OMML should be idempotent for the
  supported set; guard with tests. MathML from other producers (KaTeX, LibreOffice)
  varies (mfenced vs mo pairs, explicit vs implicit mrows) — normalise on import.
- **Scope discipline.** Presentation MathML only. Content MathML, if ever
  encountered on import, falls back per the failure policy.
- **Two repos.** OMML→MathML in docx4j-core; MathML→OMML in the
  docx4j-ImportXHTML sibling repo. Keep the shared model/helpers in core so the
  import repo depends only on published core artefacts.
- **Legal.** See §3. The clean-room constraint is the reason this CR exists;
  do not undercut it by transcribing the Microsoft stylesheets.
