# CR: Markdown math (LaTeX equations → native OMML, and back)

Status: DONE (2026-09-01) — all phases (a-d) implemented and tested (114
tests in the module).  Outstanding: eyeball QA in Word (nary limit
placement, stretchy delimiters, and especially whether eqArr renders `&` as
alignment or literally — see phase c note).
Scope: extend **docx4j-markdown** with equation support — `$...$` / `$$...$$`
(and `\(...\)` / `\[...\]`) recognized at parse time, and a **deliberately
restricted LaTeX subset** translated to Word's native OMML
(`org.docx4j.math`, which the generated object model already covers in full).
docx4j-core is untouched.
Related: CR-markdown-import-export.md (the host module; DONE 2026-09-01).

## 1. Background

The motivating experience (jharrop, converting a course of markdown+MathJax
material): the standard route is Pandoc, whose `texmath` parser translates TeX
math to OMML but supports only a subset of LaTeX — and anything it cannot
parse **silently disappears, degrades to literal text, or becomes malformed**.
Working around that means maintaining a "DOCX-normalisation pass" over the
canonical markdown (`\[...\]`→`$$`, strip `\boxed`, pull equations out of
lists/tables, split `aligned`, …) before Pandoc ever runs.

Observations that shape this CR:

- Most of the failures are **presentation syntax, not exotic mathematics**:
  conventional physics/engineering math (`\frac`, `\sqrt`, `\sum`, sub/sup,
  greek, `\text`) is a small, stable core.
- Several of the things one must normalise away for Pandoc are **not DOCX
  weaknesses at all**: OMML has a native `\boxed` (`m:borderBox`) and a native
  `aligned` (`m:eqArr`); and in our importer equations inside tables, list
  items and quotes are just paragraph content — nothing special breaks.
- The weak link is purely "arbitrary LaTeX → OMML".  The fix is to not
  attempt arbitrary LaTeX: define the subset, translate it well, and make
  everything outside it fail **loudly and losslessly**.

## 2. Design

### Recognition (markdown side)

commonmark-java has no math extension, so we ship one in the module
(`org.docx4j.markdown.math.MathExtension`):

- **Inline**: `$...$` via a custom `InlineContentParserFactory` (trigger `$`),
  with GitHub-style guards so currency doesn't false-positive: the opening
  `$` must not be followed by whitespace, the closing `$` must not be
  preceded by whitespace nor followed by a digit; `\$` escapes.  `\(...\)`
  likewise (trigger `\`).  `$$...$$` appearing inline in a mixed line is
  treated as inline math.  **`\[...\]` is a display BLOCK only, never
  inline**: markdown's own escaping writes literal brackets as `\[x\]`, so
  an inline bracket form turns escaped brackets into math (found by the
  existing escaping round-trip test in phase a).
- **Display**: a custom block parser for a line starting `$$` (content until
  the closing `$$` line; single-line `$$ x $$` allowed), and the same for
  `\[ ... \]`.  Accepting `\[...\]` directly removes the first rule of any
  normalisation pass.
- Nodes `InlineMath` / `DisplayMath` carry the raw LaTeX source; the
  extension also implements `MarkdownRendererExtension`, so the nodes render
  back to `$...$` / `$$...$$` (needed by the exporter and the round-trip
  suite).
- Toggle: `MarkdownImportOptions.Extension.MATH`, on by default like the
  other extensions (GitHub renders `$` math, so this stays within the
  module's CommonMark+GFM-adjacent dialect discipline).

### Translation (LaTeX subset → OMML)

**Direct to `org.docx4j.math` JAXB, no MathML detour.**  SnuggleTeX is
unmaintained; Microsoft's `MML2OMML.XSL` is not redistributable under ASLv2;
and the generated object model already has all 82 OMML classes.  A small
recursive-descent parser (`LatexToOmml`) over the published grammar:

| LaTeX (supported subset) | OMML |
|---|---|
| `\frac{a}{b}`, `\frac12` | `m:f` |
| `x_i`, `U^3`, `U_i^3` | `m:sSub` / `m:sSup` / `m:sSubSup` |
| `\sqrt{x}`, `\sqrt[n]{x}` | `m:rad` |
| `\sum`, `\int`, `\prod` (+ `_`/`^` limits) | `m:nary` (empty base; limits hidden when absent) |
| `\left( ... \right)` (incl `[ ] \{ \} \| .`) | `m:d` |
| `\text{...}` | `m:r` with `m:nor` (normal text) |
| `\mathrm{...}`, `{\rm ...}` | `m:r` with `m:sty` p (roman) |
| `\mathbf` / `\mathit` | `m:sty` b / i |
| `\begin{aligned}...\end{aligned}` (`\\` rows) | `m:eqArr` |
| `\boxed{...}` | `m:borderBox` |
| `\hat \bar \vec \tilde \dot \ddot` | `m:acc` |
| `\overline` / `\underline` | `m:bar` |
| greek, arrows, operators, `\infty` etc. | command→Unicode table in `m:t` |
| `\,` `\;` `\quad` `\qquad` | Unicode spaces |
| `\sin \cos \log \lim` … | upright function-name runs |

Plain `()[]` stay literal characters (as texmath does); `&` alignment marks
in `aligned` are handled per what Word actually round-trips (verified during
implementation).  Everything else — unknown macros, unsupported
environments — is a **parse failure**, never a partial translation.

### Failure policy (the point of the exercise)

- A failed equation falls back **per-equation** to its literal source, boxed
  in the `CodeChar` style with delimiters preserved — nothing is ever lost.
- Every fallback (and every dropped construct generally) is reported through
  a new `MarkdownImportIssueListener` on the options (issue = construct kind,
  source snippet, reason).  Default listener logs a warning; callers get the
  machine-readable "pandoc --verbose" equivalent by collecting into a list.
- `MarkdownImportOptions.MathPolicy`: `OMML` (default) or `LITERAL` (never
  attempt conversion).

### Placement in wml

- Inline math → `m:oMath` (JAXBElement, `math:ObjectFactory.createOMath`) in
  the paragraph's run sequence — works unchanged inside table cells, list
  items, quotes, headings.
- Display math → its own paragraph containing `m:oMathPara` (Word centers
  display math itself).  A display equation inside a list item becomes a
  paragraph of the item, as follow-on paragraphs already do.

### Export side (docx → markdown)

The reverse walk: `m:oMath`/`m:oMathPara` encountered by `WmlToMarkdown` is
translated OMML→LaTeX for the same subset into `InlineMath`/`DisplayMath`
nodes (rendered `$...$`/`$$...$$` by the extension).  Unsupported OMML nodes
flatten to their `m:t` text with a warning.  Until this phase lands, math in
exported documents degrades explicitly (flattened `m:t` text + warning),
documented as lossiness.

## 3. Phases

a. **Recognition + fallback + report** (S): MathExtension (inline `$`,
   `\(...\)`; blocks `$$`, `\[...\]`), renderer support, `Extension.MATH`
   toggle, `MarkdownImportIssueListener` plumbing, literal fallback for all
   math (conversion arrives in b).  Tests: delimiter guards, block shapes,
   fallback fidelity, report contents.

   **DONE 2026-09-01.**  `org.docx4j.markdown.math` (exported):
   `MathExtension` (Parser + MarkdownRenderer extension), `InlineMath`
   (with a display hint for inline `$$..$$`), `DisplayMath`.  Notes:
   - Single-line `$$x$$` is deliberately NOT claimed by the block parser
     (commonmark's `BlockContinue.finished()` consumes the current line, so
     a block completing on its opening line would swallow the next one);
     it parses as `InlineMath(displayHint)` instead, which phase b places
     as display.
   - Closing `$$` may share a line with trailing math content (captured in
     `tryContinue` before `finished()`); an unclosed block runs to EOF.
   - Inline `\[...\]` dropped (see §2 — collides with bracket escaping);
     `\(...\)` kept, both normalize to `$`-forms on render.
   - The renderer side declares `$` a special character, so literal
     dollars in exported text are `\$`-escaped and can't turn into math on
     re-parse.
   - Issue plumbing is generic (`MarkdownImportIssue`/`Listener` on the
     options, default logs); in this phase every equation reports one
     issue and falls back to CodeChar-styled literal source with
     delimiters preserved (display blocks as a `$$`-fenced paragraph with
     `w:br` line breaks).
   - 13 tests incl. equations inside list items (fine, per design) and
     currency/spacing/escape guards.
b. **Core translator** (M): `LatexToOmml` — runs/symbols, `\frac`, sub/sup,
   `\sqrt`, nary, `\left/\right`, `\text`/`\mathrm`/`{\rm}`, fonts, spacing,
   function names; `MathPolicy` option; inline `m:oMath` + display
   `m:oMathPara` placement.  Tests: per-construct OMML assertions, the REWS
   diagnostic equation as a golden case, failure→fallback+report, save/marshal.

   **DONE 2026-09-01.**  `LatexToOmml` (public, in the math package): a
   recursive-descent parser over a cursor, sequences tracked as *atoms* so
   `_`/`^` bind to the preceding atom only (LaTeX semantics — so `ab^2`
   scripts the `b`); adjacent same-format runs merged into one `m:r`.
   Implementation notes:
   - `\frac12`-style single-token arguments supported (LaTeX allows braceless
     single-char args).
   - nary operators consume their own `_`/`^` into `m:sub`/`m:sup` (hidden
     via subHide/supHide when absent); `limLoc` undOvr for ∑-family, subSup
     for ∫-family; the operand is NOT grouped (empty `m:e`, content flows
     after — texmath's convention, and renders correctly).
   - `\left./\right.` → empty `m:begChr/m:endChr` (invisible delimiter);
     `\{ \} \langle`… mapped; `\left`-scanning recognises `\right` only when
     not a longer command name.
   - `\text` → `m:nor` (prose, spaces preserved); `\mathrm`/`\operatorname`
     and bare function names (`\sin`, `\log`, `\lim`, …) → `m:sty` "p";
     `{\rm …}`/`\bf`/`\it` are group-scoped style switches; `\mathbf`/
     `\mathit` argument-scoped.
   - Spacing macros are real Unicode: `\,`→U+2009, `\;`/`\:`→U+2005,
     `\quad`→U+2003, `\qquad` doubled; `\!` dropped (no OMML equivalent).
   - ~90 symbol commands (greek incl. var-forms, operators, relations,
     arrows, dots, sets/logic); `'` → prime (U+2032).
   - `MathPolicy` (OMML default / LITERAL) on the options; placement:
     inline → `m:oMath` in the paragraph (works in table cells/list items —
     tested), display → own paragraph with `m:oMathPara`; a paragraph that
     IS a single `$$..$$` (the single-line form, an inline node with
     display hint) is promoted to display.
   - Failure is all-or-nothing per equation: `LatexMathException` → issue
     (construct, source, reason incl. the offending command) + literal
     CodeChar fallback; a good and a bad equation in one paragraph behave
     independently (tested).
   - 20 tests incl. the REWS golden equation; a QA docx was generated for
     eyeballing in Word (nary limits, stretchy delimiters).
c. **Structures** (S): `aligned`→`m:eqArr`, `\boxed`→`m:borderBox`,
   accents→`m:acc`, `\overline`→`m:bar`.

   **DONE 2026-09-01.**
   - `\begin{aligned}` (also `align`/`align*`) → `m:eqArr`, rows split on
     `\\`; a trailing `\\` before `\end` adds no empty row; env names must
     match.  **`&` alignment marks are kept as literal `&` characters in
     the row runs** — Word's linear-format convention for equation-array
     alignment; flagged for the Word eyeball QA (if Word shows literal
     ampersands instead of aligning, drop them at parse time instead).
   - `\boxed{...}` → `m:borderBox` — the motivating
     atmosphere→site-wind→rotor-wind example now imports as-is, no
     normalisation pass needed.
   - Accents via combining chars in `m:accPr/m:chr`: `\hat` U+0302,
     `\tilde` U+0303, `\bar` U+0305, `\vec` U+20D7, `\dot`/`\ddot`,
     `\check`, `\breve`, `\acute`, `\grave`; `\overline`/`\underline` →
     `m:bar` pos top/bot.
   - 10 tests.  NB for future work: `\underline` etc in a Java *comment*
     is an illegal unicode escape (`\u`+non-hex) — cost one compile cycle.
d. **Export** (M): OMML→LaTeX reverse for the subset; math joins the golden
   round-trip suite (normalized-form inputs: the translator regenerates
   `\frac{1}{2}`, not `\frac12`).

   **DONE 2026-09-01.**  `OmmlToLatex` (public) walks the OMML element
   lists; `WmlToMarkdown` emits `InlineMath` for `m:oMath` in a run
   sequence and `DisplayMath` for a paragraph that IS one `m:oMathPara`
   (an `m:oMathPara` mixed with other inline content degrades to inline).
   Notes:
   - **Normalized output** is the canonical form: braced arguments
     (`U^{3}`), shortest symbol command on collisions (`\le` not `\leq`,
     `\to` not `\rightarrow`), no math-mode whitespace, display math on
     one line, `\[..\]`→`$$`.  Script bases stay bare only for a single
     plain character.  A `LatexBuilder` inserts the space after a
     letters-command only when a letter follows (`\rho AU^{3}`).
   - Reverse symbol/nary/accent maps derive from `LatexToOmml`'s tables
     (now package-visible) — one source of truth.
   - `&` re-exports bare inside an eqArr (alignment mark), `\&`-escaped
     elsewhere.
   - **Flatten fallback**: OMML outside the subset (`OmmlMathException`)
     flattens to its `m:t` text with a warning — implemented by
     marshalling the node and extracting `m:t` runs, because
     `TraversalUtil.getChildrenImpl`'s reflective fallback returns only
     the FIRST List-returning method and misses most OMML containers
     (found by test).
   - 6 golden round-trip tests (canonical-equality, incl the REWS display
     equation, aligned, boxed, and math-plus-currency in one line) + 5
     translator/exporter tests; 16 translator fixed-point strings verified
     idempotent.

## 4. Risks / notes

- **Subset creep** is the real risk (texmath's failing was pretending to
  generality).  Defence: the grammar above is the contract, published in the
  module README; everything else fails loudly into the report.
- **Word rendering QA needs eyeballs**: nary limit placement, stretchy
  delimiters, eqArr alignment.  The test suite asserts OMML structure;
  someone must open the output in Word once per phase.
- `$` guards follow GitHub's rules; documents that used literal `$...$`
  spans matching those rules will now parse as math — the MATH toggle (or
  `\$`) is the out.
- Font: Word applies Cambria Math to `m:r` automatically; we set no fonts.
