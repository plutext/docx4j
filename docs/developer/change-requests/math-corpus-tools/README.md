# Math corpus tools (for CR-math-omml-mathml)

One-off tooling to build the OMML ⇄ MathML test corpus by using **Word as a
black-box oracle** — never Microsoft's `OMML2MML.XSL` / `MML2OMML.XSL` source.
See `../CR-math-omml-mathml.md` for the clean-room principle and why the corpus
is built this way.

The pipeline turns MathML inputs into paired fixtures:

```
corpus/mml/NAME.mml   (input MathML, from the W3C MathML Test Suite, W3C/BSD)
   │  MathmlToDocx.bas  (Word: paste MathML → OfficeMath equation)
   ▼
mml-outputs/NAME.docx       Word's OMML     (git-ignored intermediate)
corpus/word-mml/NAME.word.mml  Word's MathML  (export oracle)
   │  OmmlFixtureExtractor.java  (docx4j: pull the <m:oMath> out)
   ▼
corpus/omml/NAME.omml.xml   the standalone <m:oMath> fixture
```

Per case you end up with a triple:

| file | who produced it | used as |
|---|---|---|
| `corpus/mml/NAME.mml` | W3C suite (or other permissive source) | input to `MathMLToOmml` |
| `corpus/omml/NAME.omml.xml` | Word → extractor | expected output of `MathMLToOmml`; input to `OmmlToMathML` |
| `corpus/word-mml/NAME.word.mml` | Word | reference for `OmmlToMathML`'s output (compare semantically) |

## Layout

- `MathmlToDocx.bas` — Word VBA macro: batch MathML → `.docx` (+ captures
  Word's MathML). **Windows + Word only.**
- `OmmlFixtureExtractor.java` — docx4j utility: `.docx` → `NAME.omml.xml`.
  **Tested** (against `2010-sample1.docx`); runs against the published fat jar.
- `corpus/` — the committed fixtures (`mml/`, `omml/`, `word-mml/`) and
  `ATTRIBUTION.md`.
- `mml-outputs/` — Word's throwaway `.docx` (git-ignored; regenerate any time).

## Prerequisites

- **Word for Windows, build 2605 or later** — older builds don't reliably
  auto-convert pasted MathML to an OfficeMath equation.
- In Word: **Equation Tools → Conversions → "Copy MathML to the clipboard as
  plain text"** ticked (for the `.word.mml` capture). Set once.
- A JDK, and the docx4j fat jar (`docx4j-bundle-<version>.jar`) for the
  extractor — no other classpath assembly needed.

## Steps

### 1. Assemble the MathML inputs

`corpus/mml/` already holds 25 curated cases (see `corpus/ATTRIBUTION.md`); this
step documents how they were chosen and how to extend the set. Source is the
**W3C MathML Test Suite, Version 2** (`mml2-testsuite`, Presentation chapter;
W3C 3-clause BSD + W3C Document Licence — redistributable). Pick cases covering
the element-correspondence table in the CR (fractions, radicals, scripts, n-ary
+ limits, fences, matrices/tables, accents/bars, under/over, text vs identifier);
drop cases Word can't represent (elementary-math notation, linebreaking,
deliberate error cases). Add the MathML namespace to each `<math>` (the raw suite
islands omit it) and name the file `NAME.mml`.

### 2. Generate the docx + Word MathML (Windows/Word)

Open `MathmlToDocx.bas` in the VBA editor (Alt+F11 → File → Import File), point
`IN_FOLDER` at `corpus/mml` and `OUT_FOLDER` at `mml-outputs` (git-ignored), and
run `BatchConvert`. It reports how many files became equations and lists any that
didn't (do those by hand — see Fallback). Output: `mml-outputs/NAME.docx` and,
for the oracle, `NAME.word.mml` (move those to `corpus/word-mml/`).

**`.word.mml` came out as `1/2`, `√(-1)`, `■(…&…@…)` instead of MathML.** That's
Word's *UnicodeMath linear text*, produced when the **"Copy MathML to the
clipboard as plain text"** setting (Equation Tools → Conversions) is off. The
`.docx` (the OMML) are unaffected and correct; only the export oracle is wrong.
Tick that setting, then run **`CaptureMathMLOnly`** — it re-captures the MathML
for every existing `.docx` without re-importing (and warns if any capture still
lacks a `<math>` tag, i.e. the setting is still off).

**Math characters show as `?` (e.g. `<mo>?</mo>` where ∫ or ∞ should be).** The
macro writes files as UTF-8 (via `ADODB.Stream`); if you see `?` you're on an
older copy that used VBA's `Open`/`Print`, which writes the system ANSI codepage
and destroys non-ANSI Unicode (∫ U+222B, ∞ U+221E, braces U+FE37/8, combining
marks). Re-import the current macro and run `CaptureMathMLOnly` again (it
overwrites the `.word.mml`).

**"OpenClipboard Failed" at `PutInClipboard`.** The clipboard is a single-owner
resource; `OpenClipboard` fails intermittently when another process holds it —
usually a clipboard-history/manager tool (Windows Clipboard History, Ditto, …),
or Word's own Copy/Paste from the previous iteration. The macro retries with a
short backoff (`CLIP_RETRIES` × `CLIP_WAIT_MS`) and only errors out if the
clipboard stays busy the whole time. If you still hit it, close clipboard-history
tools and rerun — the macro is **resumable** (it skips any `NAME.mml` whose
`NAME.docx` already exists), so it continues from where it stopped rather than
redoing the ones already done.

### 3. Extract the OMML fixtures (any OS)

```
javac -cp docx4j-bundle-<v>.jar OmmlFixtureExtractor.java
java  -cp docx4j-bundle-<v>.jar:. OmmlFixtureExtractor mml-outputs corpus/omml
```

(Windows: use `;` as the classpath separator.) This writes `corpus/omml/NAME.omml.xml`
for each `.docx` — one `<m:oMath>` per equation (`NAME.0.omml.xml`, `NAME.1…` if a
doc holds several), declaring only the `m` and `w` namespaces.

### 4. Land the fixtures

The committed `corpus/` (`mml/`, `omml/`, `word-mml/`) is the canonical corpus.
When the converters are implemented, point their tests at it (or copy it into the
module's test resources) — OMML→MathML tests in `docx4j-core`, MathML→OMML tests
in `docx4j-ImportXHTML`. Licensing is recorded in `corpus/ATTRIBUTION.md`: MathML
inputs © W3C (BSD); `omml/` and `word-mml/` are Word's black-box output, Apache-2.0
as docx4j fixtures.

## Fallback (manual, for stubborn cases)

If a MathML file doesn't convert on paste: open Word, position the cursor, paste
the MathML (Ctrl+V) — it should become an equation; if it pastes as text, use
Insert → Equation first, then paste inside it. Save as `.docx`. With the MathML
clipboard setting on, select the equation and Ctrl+C, then paste into a text
editor to get the `.word.mml`.

## Status / caveats

- `OmmlFixtureExtractor.java` is **tested** and produces clean, namespace-pruned
  fixtures.
- `MathmlToDocx.bas` is **authored against the documented Word behaviour but not
  run here** (no Word/Windows in this environment). Validate it on two or three
  files on your Word build before running the whole corpus; the paste-auto-
  convert step is the version-dependent part.
- These are throwaway generators, deliberately kept out of the Maven build
  (plain `.java`/`.bas`, run by hand). The *fixtures* they produce are what gets
  checked in and tested.
