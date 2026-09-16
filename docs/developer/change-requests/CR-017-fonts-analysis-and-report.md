# CR-017: Font decisions with their reasons — `FontsAnalysis`, the conversion log, and the actions a user can take

Status: IN PROGRESS (2026-09-16) - phases 0 to 4 DONE and merged to
VERSION_17_1_1 (333cdb595): 0 the substitution table (bcd610d0b), 1
`FontDecision` in every pass (b3892457f), 2 and 3 the use walk,
`FontsAnalysis` and its report (a30b52450), 4 the conversion log (9cc6e3314),
each at zero corpus delta; phase 5's probes are on the share (188bc092b), their
Word goldens were measured on 2026-09-16 and the three rows they settle -
Georgia to Gelasio, Segoe UI to Selawik, Segoe UI Light to Selawik Light - are
in `font-substitutes.xml`, gated (5 changed corpus documents, 5 improvements, 0
regressions); phase 6 waits on CR-004. Jason read the
CR and accepted the recommendations the same day ("I am good with your
recommendations, please implement"); Decisions 1 to 5 below are taken as
recommended. Phases 0 to 5 were done by an Opus 5 agent in a worktree with
Fable reviewing each phase before its commit. Written from Jason's question of
2026-09-16 and the answer to it (below, verbatim), after CR-016 (fonts review,
DONE 2026-09-13) and CR-001 batch 42 (the measured substitutes, 2026-09-15).
Owner: Jason Harrop. Drafted with Claude Fable 5.1.

Scope: `org.docx4j.fonts` (`Mapper`, `RunFontSelector`, `FontFallback`,
`WidthFactors`, `WordLineMetrics`), `MainDocumentPart.fontsInUse`, the FO
exporter's conversion log, a new `FontsAnalysis` entry point with a `main`,
and (last) a docx4j-mcp tool. Not in scope: any change to which font is
chosen for what — that is CR-016's and CR-001's, and this CR only *records
and reports* those decisions.

## Background

### The question (Jason, 2026-09-16)

> we know that if fidelity is to be achieved in xsl fo, something like
> metrically compatible fonts with the relevant glyphs need to be substituted.
> (Is this enough from a font perspective?) The log output should clearly
> identify where this is not the case, ideally with a recommended action (ie
> to fix, install font x or a metrically compatible substitute such as y, z).
> Should we also write a FontsAnalysis class, whose sole purpose is to perform
> this type of analysis for a given input docx (so that a user can know what
> concrete steps to take regarding fonts)? What do you think?

### The answer (same day), kept here as the motivation

Short answer: metric compatibility is necessary but not sufficient, the
logging today is the weakest part of the fonts package, and a FontsAnalysis
class is worth doing, provided the converter's logging and the analysis are
the same code, not two.

**Is a metric clone with the glyphs enough?** From what the corpora measured,
four things have to hold, and a "metrically compatible font" only covers the
first:

- **Advances.** A clone gives Word's widths to 0.05% (Tinos against Times New
  Roman). That settles line breaks for the Latin text. Kerning and ligatures
  must also be applied as Word applies them, which the kern and noliga twins
  already do.
- **Faces.** The bold and italic that the document uses must be real faces of
  the substitute, or the width error moves to the headings (Droid Sans bold
  capitals 5.5%). A family with no bold of its own must be emboldened
  synthetically, as Word does for Calibri Light.
- **Coverage per script.** Caladea has no Greek, so Cambria's Greek goes to
  P052 at 2.6% error. There are no metric clones for most non-Latin scripts,
  so the honest statement is "Latin exact, Greek and Cyrillic within a few
  percent, CJK and Georgian at a ceiling".
- **The line box.** This does not come from the substitute at all. It comes
  from the document font's own metrics in word-line-metrics.properties, for
  512 families. An unknown family gets Word's default rule, and its lines may
  differ in height even when the widths are right.

Beyond those, the residual is not a font matter. The triage put 48% of the
deficit on fonts and 33% on layout, and 9919 turned out to be framed
paragraphs.

**Logging.** Today Mapper logs the outcome at debug and says nothing about
why. The useful unit is one line per document font per conversion, with a
grade and an action:

- exact: "Calibri: not installed; drawn in Carlito, its metric clone" at INFO
- near: "Trebuchet MS: not installed; drawn in Droid Sans, a measured
  stand-in, widths within 1 to 5%; install Trebuchet MS for Word's line
  breaks" at WARN
- class only: "Foo: unknown family; drawn in Caladea by Word's own rule for a
  font it lacks; if the author had Foo installed, line breaks and line
  heights will differ; install Foo" at WARN
- per script: "Cambria: Greek drawn in P052 (Caladea has no Greek)"

**FontsAnalysis.** Yes, and I would shape it as a CR with these phases.
First, Mapper records a decision per font with its provenance: which pass
answered (installed, embedded, clone, measured stand-in, altName, class, Word
default), what it tried, and the expected width error where we have measured
one. No behaviour change. Second, a FontsAnalysis class renders those
decisions plus the names walk from CR-016 phase 4 and the coverage check into
a report: what the document uses the font for (scripts, faces, how much
text), what docx4j will draw it with and why, a grade, and the concrete
action. The action needs a small knowledge table: proprietary font, open
clone, and how to get it (the docx4j font jar, or the distro package). Third,
the conversion logs that report's summary, behind a property. Fourth, a CLI
main and an MCP tool, since this is exactly the "describe" question a user
or an agent asks before converting.

Two things the table should gain while we are there, both to be measured
before shipping: Selawik, Microsoft's own open metric-compatible replacement
for Segoe UI, which we currently send to Arimo, and Gelasio, which is
metric-compatible with Georgia, where we use P052.

What it cannot tell anyone is whether the author's machine had the font. The
fontTable's altName and any embedded subset are the only evidence, and the
report should say so rather than guess.

### This matters because the target is what Word drew for the author

Jason asked (2026-09-16) why it matters whether the author's machine had the
font. Because the target of "fidelity" is what Word drew for the author, and
Word draws a font the machine lacks with a substitute of its own. So for a
font *this* machine lacks there are two different targets, and the docx alone
does not say which one Word used:

- **The author had it.** Word laid the text out in that font's real widths.
  The right thing for docx4j is a clone of that font, or the closest measured
  stand-in, and the action "install Foo" is correct.
- **The author lacked it.** Word substituted by its own rule, which CR-016's
  probe P5 pinned down: Cambria for a roman or unlisted family, Calibri for a
  swiss one, the `w:altName` if it resolves. The author's PDF was drawn in
  Calibri. If docx4j then finds a good stand-in for Foo, it is faithfully
  imitating a rendering that never existed, and "install Foo" would make the
  output *less* like the author's page.

Two documents from batch 42 were first read as showing both sides, and read
wrong: 9919's Nokia Pure Text and the EnBW document's EnBW DIN Pro Light both
carry a real `w:panose1` and `w:sig`, so the machine that saved each document
had the font. What the goldens show - Word drawing Meiryo for the one and
Calibri Bold for the other - is what Word on the fidelity VM did when it lacked
them, which is exactly docx4j's own situation on this machine, and is why
CR-016's rules (the altName, the default for an unknown family, no no-bold alias
for a font Word could not find) are right for the render even though the saving
machine had the fonts. The fontTable evidence and the golden answer two
different questions: the fontTable says what the saving machine had, which is
the page the author saw; the golden says what the rendering machine had. For
fidelity scoring the target is the golden's machine; for a user's document the
report answers the first question, and its action turns round only where the
saving machine itself lacked the font (a name-only entry, or `w:notTrueType`),
because then the page the author saw was already drawn in Word's default. Phase
3 found two refinements while reading real font tables: an all-zero
`w:panose1`/`w:sig` is no evidence, and `w:notTrueType` is a direct statement
that Word had no such font.

It is less unknowable than the answer above says. The fontTable carries
evidence that Word writes only from an installed font: `w:panose1`, `w:sig`
and `w:charset` are read off the font file when the document is saved, so a
name-only `w:font` entry says the saving machine lacked it, while an entry
with panose and signature says it had it; `w:embedRegular` (and the other
three faces) says it had it and embedded it; and a `w:altName` is Word
recording what it would fall back to. That is a three-way inference — had,
lacked, embedded — with a stated basis, and the report carries it as a field
(`authorHad`: `EMBEDDED`, `LIKELY` (panose/sig present), `UNLIKELY`
(name-only entry), `UNKNOWN` (no fontTable entry at all)) with the evidence
named, rather than a caveat. It also changes the action: for `UNLIKELY` the
report says "Word drew this in <its default>; docx4j does the same; installing
Foo would move the output away from the author's page", and for `LIKELY` it
says "install Foo, or its clone". CR-016's P5 probe (d) to (f) are the
verification cases for the rule (an altName that resolves wins; an entry with
only an absent altName goes to Calibri).

**The measurement this rested on is now made** (phase 5's `fonts-author-had`
probe, Word's golden 2026-09-16): the same sentence in two made-up families no
machine has, one with a name-only `w:font` entry and one carrying
`w:family="swiss"`, `w:charset`, `w:panose1` and a real `w:sig`. `pdffonts`
lists two fonts in the golden, LiberationSerif and **Calibri**; Word drew
**both** absent-font paragraphs in Calibri, they broke at the same word, and
their pen advances differ by 0.011pt over 357pt (0.003%, the position rounding
the probe's own control shows). So Word's substitution *at render time* does
not read the entry: a name-only entry and a fully signed one for the same
absent family are drawn identically, in Word's own default — and the default
it chose for each is the one `Mapper.wordDefaultFor` already returns (no
`w:family` and no resolvable altName falls through to Calibri; swiss is
Calibri; only the *absence* of an entry gives Cambria). The fontTable evidence
therefore speaks only of the machine that saved the document, which is exactly
what `authorHad` claims, and the render is P5's rule in both cases. That is
why the field changes the *action* and never the mapping: on this probe docx4j
draws both in Carlito (Word's Calibri at 0.9988 of `TextMeasurer`'s Carlito,
the known clone error), and the report says "nothing to do" for the name-only
entry and "install it" for the signed one.

### Where the evidence for that answer comes from

Every number above is from CR-016's probes and gates, the CR-001 corpus
ledgers, or batch 42 (`~/fidelity-real3/triage/batch42.md`, outside the
repo):

| claim | measured where |
|---|---|
| a metric clone is exact | 7396: Tinos reproduces Times New Roman's widths to 0.05% over 1024 lines (ledger4 §2 rank 15) |
| the faces matter as much as the family | 11741: Arimo 3% narrow over Trebuchet's body and 9% wide over its bold capitals at once; Droid Sans 1.0% / 5.5% (batch 42 item 1) |
| a family with no bold face is emboldened at its own advances | CR-016 probe P7: Word's Calibri Light `w:b` is +0.3% wide, Carlito Bold +2.7%; `Mapper.addNoBoldFaceAliases` |
| a weight of a cloned family needs a factor | Calibri Light 0.987 of Carlito (batch 42 item 3; `WidthFactors`) |
| coverage per script has no clone | 8371: Cambria's Greek in P052 at 0.9744; Sylfaen's Georgian in DejaVu Serif Condensed at 0.900, the only face within 3% (`FontFallback.measuredForScript`) |
| the line box is the document font's, not the substitute's | `WordLineMetrics`, 512 families; Helvetica's 1.150 against Arimo's 1.432 (its class comment) |
| unknown family: Word's own default, and the box may still differ | CR-016 probe P5 (Cambria for roman / no entry, Calibri for swiss); 12630's box 5.4% short where Word drew a script in Calibri |
| where an altName resolved, the box is already right | batch 42 §28.1: 3044 0.3% at parity 1.0000, 13459 0.15%, 6541 1.5%; one of ten (9919) wrong, and that one is framed paragraphs |
| the rest is layout | ledger4 §3: 48% fonts, 33% layout, 19% harness |

### What the code records today, and what it does not

- `Mapper` holds the *result* (`fontMappings`, the bold/italic forms, the
  `wordDefaulted` set, the per-conversion line-metrics aliases since batch 42),
  but not the *reason*: which pass answered, what the earlier passes tried,
  whether the substitute is a clone, a measured stand-in, a class default or
  Word's own default. The passes log at DEBUG ("Mapping X to Y (w:altName Z)"),
  and the FOP configuration logs a font it cannot find at WARN with no
  advice.
- `WidthFactors` (batch 42) and the comments in
  `Mapper.addMetricallyCompatibleSubstitutes` and
  `FontFallback.measuredForScript` carry the measured width errors of the
  stand-ins, in prose, where no code can read them.
- `MainDocumentPart.fontsInUse()` (CR-016 phase 4) walks the names: every
  `w:rFonts` slot on runs, paragraph marks and `w:sdtPr`, `w:sym`, the
  theme-resolved slots, the styles in use with their `basedOn` chains,
  numbering. It says which fonts a document *names*, not what it uses each
  for (which scripts, which faces, how much text), which is what a report
  has to say to grade the substitution.
- `RunFontSelector.documentFontFor(pPr, rPr, codePoint)` answers "which font
  does this character go to" one character at a time; `GlyphCheck` answers
  coverage. Between them a text walk can attribute every character to a
  (document font, script, face) triple.
- The `-Dfidelity.fonts=jars` cell of CR-016 phase 0c showed the headless
  case (issue #695): the jars alone are the whole supply, and a user of a
  container has no `fc-list` to consult. The report is the only way such a
  user learns which jar to add.

## Gaps

1. **No provenance.** A user (or the triage) cannot learn from the log or the
   API *why* a font was drawn in what it was drawn in. Batch 42's step (a)
   spent its first hours reconstructing, per document, which pass had
   answered; the FO's `pdffonts` output was the only evidence.
2. **No grade.** "Mapped" covers a metric clone (exact), a measured stand-in
   (1 to 5%), a class default (unknown error) and Word's default for an
   unknown family (line box may differ too). They are logged alike.
3. **No action.** The log says a font is missing; it does not say that
   Carlito would make it exact, that the crosextra jar carries Carlito, or
   that no open clone exists and the font itself must be installed.
4. **The knowledge is in comments.** The clone table, the measured ratios and
   the per-script answers live in prose beside the code that uses them. A
   report cannot cite them, and a port (the TypeScript and Python ports treat
   this repository as their oracle) cannot read them.
5. **The names walk is not a use walk.** `fontsInUse()` gives names; the
   grade needs scripts and faces per font. The selector's per-character
   answer exists but nothing aggregates it.
6. **The author's fonts are inferred silently.** `addWordDefaultSubstitutes`
   assumes the author lacked an unknown family and had a known one, and
   nothing reports which assumption a mapping rests on, although the
   fontTable's panose, signature, embedding and altName say so per font.
7. **Two candidate clones are not in the table.** Selawik (Microsoft, OFL,
   metric-compatible with Segoe UI by its own description) and Gelasio
   (metric-compatible with Georgia). Segoe UI goes to Arimo today and Georgia
   to P052 (1.09x Tinos, measured). Both need measuring against a Word golden
   before they replace what is there — "metric-compatible" on a project page
   is a claim, not a measurement (P052's own comment records why). **Closed by
   phase 5**: measured, and both claims held where the face exists; the three
   rows are in the table.

## Design

**One source, three renderings.** The mapping passes record a
`FontDecision` per document font; `FontsAnalysis` reads those decisions plus
a use walk and produces a `FontReport`; the report is rendered (a) as the
conversion log, (b) as text and JSON from a `main`, (c) as an MCP tool's
answer. No log string is written anywhere else.

### `FontDecision` (in `Mapper`)

Recorded by each pass that maps a font, replacing the DEBUG lines:

| field | values |
|---|---|
| `documentFont` | the name as the docx has it |
| `source` | `INSTALLED`, `EMBEDDED`, `METRIC_CLONE`, `MEASURED_STAND_IN`, `ALT_NAME`, `CLASS`, `WORD_DEFAULT`, `MAPPER_OWN` (BestMatchingMapper's panose / FontSubstitutions.xml), `UNMAPPED` |
| `physical` | the `PhysicalFont` chosen, and its bold / italic / bold-italic forms or `synthetic` |
| `via` | the chain for `ALT_NAME` (each hop), the class for `CLASS`, Word's default family for `WORD_DEFAULT` |
| `widthError` | the measured error of the stand-in where one is known (from a table this CR extracts out of the comments: Droid Sans for Trebuchet 1 to 5%, P052 for Georgia ~1%, DejaVu Sans for Verdana ~0.1% ...), the `WidthFactors` factor where one applies, `unknown` for a class default |
| `lineBox` | `documentFont` (the table knows it), `alias:<family>` (per-conversion alias), `wordDefault:<family>`, `substitute` (nothing known; the physical font's own metrics) |
| `perScript` | the coverage answers recorded by the selector during conversion, where a script went to another face (`FontFallback.measuredForScript` and the coverage pass): script, face, measured error where known |

`Mapper.getDecisions()` returns them; the selector adds the per-script
entries as it goes (it already keys `fallbackByScript` on font and script).

### The knowledge table

A resource beside `word-line-metrics.properties`, read by the passes and by
the report — the one place the clone knowledge lives:

| column | example |
|---|---|
| document font | Calibri |
| open clone(s), in order | Carlito |
| clone quality | `metric` / `measured:<error>` / `class` |
| where to get it | `docx4j-export-fo-fonts-crosextra` (jar); `fonts-crosextra-carlito` (Debian/Ubuntu); `google-crosextra-carlito-fonts` (Fedora); `ttf-carlito` (Arch) |
| licence of the clone | OFL |
| scripts the clone covers | Latin, Greek, Cyrillic (Carlito); Latin only (Caladea) |

`Mapper.addMetricallyCompatibleSubstitutes` keeps its shape and reads the
table (the comments stay as the measurement record). The package names are
data, not a promise: they are what a user types, and they go stale, so the
report says "for example".

### `FontsAnalysis`

```
FontReport report = FontsAnalysis.analyse(WordprocessingMLPackage pkg);        // this machine's fonts
FontReport report = FontsAnalysis.analyse(pkg, PhysicalFonts.jarsOnly());       // a headless deployment's
```

For each document font, in order of how much text it carries:

- **use**: characters and runs by script and by face (regular / bold / italic
  / bold italic), from a walk that asks `documentFontFor` per character —
  the CR-016 phase 4 scratch-`Document` per selector keeps this cheap (23 us
  per run measured);
- **decision**: the `FontDecision`;
- **grade**: `EXACT` (installed, embedded, or a metric clone covering every
  script and face used), `NEAR` (a measured stand-in, or a clone with a
  script going elsewhere: the error is named), `CLASS` (a class default or
  Word's default: line breaks will differ, and the line box may), `NONE`
  (unmapped: FOP's base-14 fallback);
- **action**: from the knowledge table — "install Calibri (Microsoft,
  proprietary) or Carlito (its metric clone, OFL: the crosextra jar, or
  package X)"; for a stand-in, "install Trebuchet MS: no open clone exists;
  Droid Sans is the closest measured (1 to 5%)"; for an unknown family,
  "install Foo; docx4j has no metrics or clone for it, and drew Word's own
  default for a font it lacks — if the author had Foo, line breaks and line
  heights will differ"; for a script, "Greek in a Cambria document: install
  Cambria; Caladea has no Greek and P052 is 2.6% out";
- **authorHad**: `EMBEDDED` / `LIKELY` / `UNLIKELY` / `UNKNOWN`, from the
  fontTable evidence (embedded faces; `w:panose1` and `w:sig` present; a
  name-only entry; no entry), with the evidence named — see "This matters
  because" above. `UNLIKELY` turns the action round: Word drew the font in
  its own default, docx4j does the same, and installing the named font would
  move the output away from the author's page.

A `main` (`java -cp ... org.docx4j.fonts.FontsAnalysis in.docx [--jars-only]
[--json]`) prints it. The text form is the one the conversion logs.

### The conversion log

`Docx4J.toFO` / `toPDF` log the report's summary once per conversion, after
`setFontMapper` has run and the selector has done its first pass — one line
per document font, INFO for `EXACT`, WARN for `NEAR`, `CLASS` and `NONE`,
with the action in the WARN line. `docx4j.fonts.report.log=summary|full|off`
(default `summary`). The DEBUG lines the passes write today go; the FOP
"font not found" WARN stays (it is FOP's).

### The MCP tool (last)

docx4j-mcp's `describe` gains the report as a section, and a `fonts` tool
returns the JSON form, so an agent can say "this document needs Carlito"
before it converts. Depends on CR-004's placement decisions.

## Plan

0. **Extract the knowledge table** from the comments in `Mapper`,
   `FontFallback` and `WidthFactors` into the resource, with a test that the
   passes' answers do not change (the CR-016 `MapperPrecedenceTest` /
   `ClassBasedSubstituteTest` and the corpus at zero delta). No behaviour
   change. — **DONE** (bcd610d0b).
   `org/docx4j/fonts/font-substitutes.xml`, beside `word-line-metrics.properties`,
   read by `FontSubstitutionTable` with a plain DOM parser (no JAXB, no xsd:
   nothing marshals it; XML rather than properties because four of the six
   columns are lists carrying commas and parentheses, and a properties key
   would have to escape its spaces). 31 `<font>` rows over 81 `<substitute>`
   entries — the 26 document fonts of
   `addMetricallyCompatibleSubstitutes` in its own order, the four
   `FontFallback.measuredForScript` rows in theirs, the one `WidthFactors`
   factor — plus a catalogue of 40 substitutes with licence, docx4j jar,
   example packages and scripts. The measurements stay in the comments. Gate:
   `docx4j-core-tests` 1052/0 (1042 + the 10 of the new
   `FontSubstitutionTableTest`), `docx4j-export-fo-tests` 602/0; the three
   corpora against `b65-nestedp` at **0 changed documents** with
   byte-identical scoreboards; the five fonts probes at their CR-016 / batch 42
   values (`fonts-light-bold` 100%, `fonts-unresolvable` 88%,
   `fonts-missing-slots` 100%, `fonts-theme-lang` 100%,
   `fonts-symbol-and-emoji` 17%).
1. **`FontDecision` recorded by every pass**, `Mapper.getDecisions()`, the
   selector recording per-script answers. Tests: one decision per pass, with
   its `via`. No behaviour change; corpus zero delta. — **DONE**
   (b3892457f). `FontDecision` carries the source, the `via`, the width
   error (the table's, with the `WidthFactors` factor where one applies), the
   physical font with its bold / italic / bold-italic faces or `synthetic`,
   the line box (`documentFont` / `alias:` / `wordDefault:` / `substitute`)
   and the per-script choices; the volatile half is read off the mapper when
   the decision is handed out, since a later pass re-maps. A tenth source,
   `SYMBOL`, was added for the face `PhysicalFonts.getWDingsFont` /
   `getSymbolFont` picks inside the selector: no mapper pass chooses it, and
   the decision has to name the face on the page. Six DEBUG lines gone. Gate:
   `docx4j-core-tests` 1066/0 (1052 + the 14 of `FontDecisionTest`),
   `docx4j-export-fo-tests` 602/0, the five fonts probes unchanged, the three
   corpora at **0 changed documents** with byte-identical scoreboards.
2. **The use walk** (`FontsAnalysis.usage(pkg)`): characters and runs per
   (font, script, face), measured for cost on the 311-page document of
   CR-016 phase 4 (fontsInUse was 8 ms there; this walks text, so expect
   more, and it must stay well under the conversion's own time). — **DONE**
   (a30b52450, with phase 3). One selector per part, the run's effective
   properties resolved once per run (a new
   `documentFontFor(pPr, rPr, codePoint, rPrIsEffective)` overload) and each
   run's answers memoised by code point. Measured against each document's own
   `toFO` in the same JVM: 12301 (311 pages, 196,093 characters in 15,572
   runs) **166 ms cold / 137 ms warm against 54,989 ms, 0.30% / 0.25%**; 11875
   (144 pages, 120,324 characters in 21,878 runs) **115 / 160 ms against
   282,108 ms, 0.04% / 0.06%**.
3. **`FontsAnalysis.analyse` and `FontReport`**, text and JSON, the `main`,
   the `jarsOnly` environment, the `authorHad` inference from the fontTable
   (9919 and the EnBW document as the worked examples). Tests on the CR-016 probe documents
   (`fonts-unresolvable`, `fonts-light-bold`, `fonts-symbol-and-emoji`,
   `fonts-theme-lang`): each has a known answer and a known grade. — **DONE**
   (a30b52450). Built from the use walk's fonts plus any `UNMAPPED` or
   `SYMBOL` decision, never the mapper's whole map. `FontEnvironment` (this
   machine, the jars, a directory) is in core and docx4j-layout-fidelity calls
   it. The `authorHad` rules gained two refinements read off real font tables:
   an all-zero `w:panose1`/`w:sig` is no evidence, and `w:notTrueType` says
   Word had no such font; and the worked examples were re-read (see "This
   matters because"). docx4j-core-tests cannot depend on the harness, so the
   probe cases are built in the test with the probes' own shapes and cite
   them; the corpus documents are cited by id only. Gate:
   `docx4j-core-tests` 1083/0 (1066 + the 17 of `FontsAnalysisTest`),
   `docx4j-export-fo-tests` 602/0, the five fonts probes unchanged, the three
   corpora at **0 changed documents** with byte-identical scoreboards.
4. **The conversion log** rendering the summary, the property, the DEBUG
   lines removed. Gate: export-fo-tests, and the log of one corpus run read
   for noise (one line per font, not per run). — **DONE** (9cc6e3314).
   `Docx4J.toFO` calls `FontsAnalysis.logReport` after the export - so
   `toPDF` through it, and so the report carries the per-script choices the
   selector made during the conversion - at INFO for `EXACT` and WARN
   otherwise, behind `docx4j.fonts.report.log=summary|full|off` (default
   `summary`, documented in docx4j.properties). The logger is
   `org.docx4j.fonts.FontsAnalysis`, so a deployment can silence it by name,
   and the rendering is `FontReport`'s, so no log string is written anywhere
   else. Three more DEBUG lines went (`populateFontMappings`' "already
   mapped", the metric pass's "the document embeds it",
   `RunFontSelector`'s "not mapped; using fallback"); BestMatchingMapper's
   remain, since they report what was tried and failed, which no decision
   records. Gate: `docx4j-core-tests` 1088/0 (1083 + the 5 of
   `FontReportLogTest`), `docx4j-export-fo-tests` 602/0 with core and
   export-fo both installed first, and real3 at **0 changed documents** with
   a byte-identical scoreboard. Its log read for noise: 86 of the 102
   documents report at all, 1 to 9 lines per conversion (median 3; the
   harness converts each document twice, once to FO and once to PDF), and no
   font twice within a conversion.
5. **Selawik and Gelasio**, each measured against a Word golden before it
   enters the table (a probe per face on the share; Jason runs Word), then
   the corpus gate as batch 42 did it. Either may fail the measurement and
   stay out. The same Word run takes the `authorHad` probe: one absent font
   named by a name-only `w:font` entry and by an entry carrying `w:panose1`
   and `w:sig`, to see whether Word's substitution at render time differs
   between the two - i.e. whether Word reads the entry when it lacks the font.
   — **probes on the share 2026-09-16** (188bc092b); **the three goldens
   measured 2026-09-16**, and the answers are below and in
   "Phase 5's probes, and what their goldens measured". In short: the Carlito
   control calibrates at **1.0002** (0.99974 and 0.99956 on the two long control
   lines, i.e. 0.03%, as batch 42's 0.04%); **Gelasio** is 0.9994 / 0.9995 /
   0.9995 against Word's Georgia regular, bold and italic where **P052** is
   0.9838 / 1.1119 / 1.1234, so it passes the bar on every face and should go
   into the table; **Selawik** is 0.9994 regular, 0.9997 bold and 0.9995 Light
   against Arimo's 1.0028 / 0.9978 and Source Sans 3's 0.9938, but it **ships no
   italic face**, so Segoe UI's italic would be FOP's oblique of the regular at
   0.9737 against today's Arimo Italic 0.9769 — 0.3pp worse on one face, which
   fails the bar as written for `Segoe UI` while `Segoe UI Light` (one face)
   passes outright. Both candidates are Latin only, where Arimo and P052 carry
   Greek and Cyrillic, so those scripts now go through the coverage pass.
   **Jason took the italic exception** ("take Selawik, Arimo second", Decision 4
   below), so all three rows went into `font-substitutes.xml`, each with the
   pre-17.1.1 answer behind it for a machine without the new face. The two new
   probes went from **50% to 100%** (`fonts-georgia`, and 7 of Word's 8 lines
   matched to 8 of 8) and **80% to 100%** (`fonts-segoe-ui`), the five older
   fonts probes are unchanged, and the corpora moved only where a document
   really uses Georgia or Segoe UI. Gate: `docx4j-core-tests` **1105/0**,
   `docx4j-export-fo-tests` **602/0**, the seven fonts probes as above, and the
   three corpora against `b65-nestedp` (run `c17-p5`, both families installed
   for the run and removed afterwards) at **5 changed documents, 5 improvements,
   0 regressions**: real 3 (mean line parity 0.9059 to 0.9068), real2 2 (0.8810
   to 0.8823), real3 **0**, its scoreboard byte-identical. The no-break-space fix
   and the alias were gated again on top (`c17-p5b` against `c17-p5`): **0 changed
   documents in all three corpora, every scoreboard byte-identical**, the seven
   probes unchanged, `docx4j-core-tests` 1108/0, `docx4j-export-fo-tests` 602/0. Every mover is a
   document Word drew in Georgia or Segoe UI, read off the PDFs: P052 to Gelasio
   in all three faces, Arimo to Selawik with the Cyrillic of a Russian document
   going through the coverage pass (Selawik being Latin only), and one
   Segoe UI Light document from Source Sans 3 to Selawik Light. Five more
   documents changed face without changing a line break, and one gained Noto
   Sans Symbols 2 for a list bullet Selawik lacks. The move also found a
   **coverage defect of its own**, fixed here: **a no-break space is a space**
   (below).
6. **docx4j-mcp** `fonts` tool and the `describe` section.

### Phase 5's probes, and what their goldens measured

The three probes are in `docx4j-layout-fidelity`'s `Corpus.java`; Word's
goldens for them are in `docx4j-layout-fidelity/goldens/word/` (2026-09-16):

- **`fonts-segoe-ui`** — the same sentence in Segoe UI, Segoe UI with `w:b`,
  Segoe UI with `w:i`, Segoe UI Light, and Carlito.
- **`fonts-georgia`** — the same sentence in Georgia, Georgia with `w:b`,
  Georgia with `w:i`, and Carlito.
- **`fonts-author-had`** — the same sentence in two made-up families no machine
  has, one with a bare `w:font` entry (name and nothing else) and one with
  `w:family="swiss"`, `w:charset`, and the `w:panose1` and `w:sig` of a real
  sans (Liberation Sans's own OS/2 values, read from the font file in
  docx4j-export-fo-fonts-liberation), plus a Liberation Serif control.

**What the first two are for.** Selawik is Microsoft's own open replacement for
Segoe UI (OFL 1.1; its README says "an open source replacement for Segoe UI",
and notes that it lacks Segoe UI's kerning), and Gelasio's README says it is
"metrics compatible with Georgia in its Regular, Bold, Italic and Bold Italic
weights" and deliberately carries no kerning (OFL 1.1). Both are claims on a
project page, which is what P052's comment warns about, so neither enters
`font-substitutes.xml` until a golden says otherwise. Today Segoe UI goes to
Arimo, Segoe UI Light to Source Sans, and Georgia to P052.

**How.** For each face, take Word's **pen advance** for the probe sentence from
the golden PDF — the distance from the first glyph origin to the last, which
carries no side bearing — and compare it with `TextMeasurer`'s advance for the
same string in the candidate file, **at the nominal size, not the size the PDF
reports**. That is batch 42's method note: `mutool` reports the text-matrix
size, which Word writes on a 1/300 inch grid (10pt as 10.08, 11 as 11.04), so
measuring a candidate at the reported size makes it up to 1 per cent too wide
and the ratio correspondingly low — which is how that batch's first pass at the
Calibri Light factor read 0.985 instead of 0.987. The Carlito line in each
probe is the calibration: Word draws Carlito itself on the fidelity VM, and its
pen advance agreed with `TextMeasurer`'s Carlito to 0.04 per cent, so a
candidate's ratio can be trusted to about that. A face enters the table only if
it beats what is there now on every face the probe sets (regular, bold, italic),
and then the three corpora are re-scored as batch 42 did it, at zero changed
documents or with every changed document read.

**The control, measured.** Over the eleven Carlito segments of the two goldens,
Word's pen advance divided by `TextMeasurer`'s Carlito at the nominal 11pt is
**1.0002**; on the two long control lines (99 characters each) 0.99974 and
0.99956, i.e. within **0.03%**, and `fonts-author-had` gives an independent
second control in another family Word itself drew (Liberation Serif, 0.99966).
The short lead segments scatter ±0.3%, which is Word's glyph-position rounding.

**What the goldens said** (Word / candidate; above 1 the candidate is too
narrow. `madev%` is the mean absolute per-character deviation, whose noise floor
is the 1.40% the Carlito control scores against itself — it separates a family
that matches glyph by glyph from one whose per-character errors merely cancel
over a line, which is batch 42's Trebuchet lesson):

| Word drew | candidate | W/cand | madev% |
|---|---|---|---|
| Segoe UI | **Selawik Regular** | **0.9994** | 1.37 |
| Segoe UI | Arimo Regular *(today)* | 1.0028 | 6.19 |
| Segoe UI bold | **Selawik Bold** | **0.9997** | 1.07 |
| Segoe UI bold | Arimo Bold *(today)* | 0.9978 | 4.05 |
| Segoe UI italic | Selawik Regular (no italic face; FOP obliques it) | 0.9737 | 4.66 |
| Segoe UI italic | **Arimo Italic** *(today)* | **0.9769** | 6.50 |
| Segoe UI Light | **Selawik Light** | **0.9995** | 1.42 |
| Segoe UI Light | Source Sans 3 *(today)* | 0.9938 | 11.97 |
| Georgia | **Gelasio Regular** | **0.9994** | 0.91 |
| Georgia | P052 *(today)* | 0.9838 | 3.79 |
| Georgia bold | **Gelasio Bold** | **0.9995** | 1.28 |
| Georgia bold | P052 Bold *(today)* | 1.1119 | 11.00 |
| Georgia italic | **Gelasio Italic** | **0.9995** | 0.88 |
| Georgia italic | P052 Italic *(today)* | 1.1234 | 12.37 |

With both families installed, the probes themselves say the same thing at the
page level: `fonts-georgia` goes from **50% line parity (7 of Word's 8 lines
matched) to 100% (8 of 8)** and `fonts-segoe-ui` from **80% to 100%**, the Segoe
UI Light line being the one that had no match.

**Gelasio passes** on every face, by 1.6, 11.2 and 12.3 points of a percent:
P052 is passable on Georgia's regular and badly narrow on its bold and italic,
which is where headings live. **Selawik passes on the faces it has** — four to
seven times closer than Arimo on regular and bold, and matching per character —
but the release carries no italic (`selawk`, `selawkb`, `selawkl`, `selawksb`,
`selawksl`), so Segoe UI's italic is FOP's oblique of the regular at the
regular's advances, 0.3pp worse than today's Arimo Italic. On the bar as
written that keeps `Segoe UI` as it was; **Jason took the exception** for it
(Decision 4), and `Segoe UI Light`, which has one face, passes the bar itself.
Coverage is the other half of the answer and the table's
`scripts` column: both candidates are **Latin only** (Gelasio adds Vietnamese),
where Arimo and P052 carry Greek and Cyrillic, so a Cyrillic or Greek document
in these families moves to a per-script fallback — the Caladea-has-no-Greek
pattern, with the machinery already in place. Both families were installed on
the development machine for the gate (a corpus can only measure a face the
machine has) and removed afterwards, so the rows have to fall through to the
pre-17.1.1 answers where the faces are absent — which they do, first available
wins, and that is the case for any machine that has not fetched them.

### A no-break space is a space (the defect the corpus run found)

Scoring the corpus with the new rows, one document gained a face the PDF does
not carry: `15_de-DE_tbl_1660`, set entirely in Segoe UI Light, rendered a
**base-14 Helvetica** where before it had only Source Sans 3. Read from the FO,
the cause is not bold and not the substitution:

- the document's header has a paragraph whose only content is a **no-break
  space** (U+00A0), a spacer line;
- **Selawik and Selawik Light have no U+00A0 at all** (nor do 32 other faces on
  the development machine, among them several Noto families) — measured from the
  `cmap`, and through `GlyphCheck`;
- so the coverage pass found no face for that run, and the FO block came out
  with **no `font-family` attribute whatever** — FOP then drew it in its base-14
  default, a font not embedded in the PDF, which fails PDF/A as well as looking
  wrong.

The fix is in `GlyphCheck.hasCodepoint`: **U+00A0 is read as U+0020 where the
face has no glyph of its own for it** — the two differ in where a line may
break, not in what is drawn, and `TextMeasurer.glyphWidthPt` already reads a tab
the same way. `NoBreakSpaceCoverageTest` holds both halves: the coverage answer,
and a run of only a no-break space keeping its own face in the FO. On 1660
`pdffonts` now lists **`Selawik-Light` and nothing else**, and its bold headings
still draw in Selawik Light, synthesised from the regular as Word does for a
Light family.

**A finding for its own item: the base-14 faces already in the corpus.** Asked
for every non-embedded face in the three corpora's renders (`pdffonts`, the
`emb` column), **37 of 449 documents carry one** — `Helvetica` or
`Helvetica-Bold` in 8, one of the `Times` four in 25, and four documents with a
face reported as a subset name and not embedded (`EAAAAB+Carlito-Regular` and
friends). The same 37, with the same faces, are in the pre-CR `b65-nestedp`
renders, so none of it is this CR's; 1660's, which was, is gone. Each one is a
run that reached FOP with a `font-family` it could not resolve, drawn in a face
nobody chose and not carried in the PDF — the same shape of defect as the
no-break space, and worth a pass of its own.

Two other things this chased down and ruled out, recorded so no one repeats them:
the bold was innocent (the headings drew in Selawik Light throughout), and so was
the anchored drawing in that header (removing either leaves the Helvetica; removing
the no-break space's paragraph is what moves it). `addNoBoldFaceAliases` now also
applies where the substitute has **no bold sibling to withhold** — FOP synthesises
the bold either way, since such a family takes the simulate-style branch of the
configuration, but without the alias the decision reported the physical font's own
face as the bold where nothing of the kind is on the page.

**What the third is for.** `authorHad` infers from the fontTable that the
machine which saved a document had the font (`w:panose1` and `w:sig`, which
Word reads off the font file) or lacked it (a name-only entry, or
`w:notTrueType`), and turns its action round in the second case. The probe
answers the question that inference rests on: whether Word's substitution *at
render time* differs between the two entries for the same absent font — i.e.
whether Word reads the entry when it lacks the font. Read it from the golden
with `pdffonts` (which face draws each paragraph) and from the line breaks.
**Answered**: `pdffonts` lists LiberationSerif and Calibri and nothing else;
Word drew both absent-font paragraphs in Calibri, breaking at the same word,
their pen advances 0.003% apart. Word does not read the entry when it lacks the
font, and the default it chose for each entry is the one `Mapper.wordDefaultFor`
returns — see "This matters because" above, where the consequence for
`authorHad` is written out.

The two candidate font files were downloaded and deliberately **not installed**
until they had been measured, so that no corpus or probe render could pick them
up by accident before the goldens said what they were worth: Selawik 1.01 from
`github.com/microsoft/Selawik/releases` (its `LICENSE.txt` is the OFL 1.1 with
Reserved Font Name Selawik) and Gelasio from `github.com/SorkinType/Gelasio`
(`OFL.txt`, Copyright 2022 The Gelasio Project Authors).

Phases 0 to 4 are `docx4j-core` and `docx4j-export-fo`; each is its own
commit with the corpus at zero delta (this CR changes nothing that renders,
until phase 5).

## Decisions (recommended 2026-09-16; all five accepted by Jason the same day, as recommended)

1. **Grades as four words** (`EXACT`, `NEAR`, `CLASS`, `NONE`) rather than a
   number: the measured errors are per face and per script and do not add
   up to one figure honestly. Recommended.
2. **Package names in the table**: they help the user most and go stale
   fastest. Recommended in, marked "for example", with the jar named first
   because that is the answer docx4j controls.
3. **Log level for `NEAR`**: WARN (recommended: the user asked to know) or
   INFO (quieter for the common Trebuchet case). The property can silence it
   either way.
4. **Selawik and Gelasio**: measure first, as above; the CR does not assume
   either is in. **Measured 2026-09-16, and decided the same day.** Gelasio
   passes the bar on every Georgia face and goes in as `quality="metric"` (the
   0.2% rule). Selawik passes on `Segoe UI Light` outright. On `Segoe UI` it
   fails the bar as written, on the italic alone — the release has no italic
   face, so FOP obliques the regular at 0.9737 where Arimo Italic is 0.9769 —
   and Jason took the exception: **"take Selawik, Arimo second"**, because the
   regular and the bold are what carry the text (0.9994 and 0.9997 against
   Arimo's 1.0028 and 0.9978, and at the measurement's per-character floor
   where Arimo is 6.2% and 4.1% out). So the rows are Selawik then Arimo
   Regular then Liberation Sans; Selawik Light then Source Sans 3, Source Sans
   Pro, Arimo Regular, Liberation Sans; Gelasio Regular then P052, Tinos
   Regular, Liberation Serif — each falling through to the pre-17.1.1 answer on
   a machine without the new face, which is the common case, since neither font
   is in a docx4j jar or in most distributions. Both new families are **Latin
   only** (Gelasio adds Vietnamese), so Greek and Cyrillic in a Segoe UI or
   Georgia document go through the coverage pass (`FontFallback`, as Cambria's
   Greek does) instead of being carried by the substitute itself, which Arimo
   and P052 did.
5. **The `main`'s home**: `org.docx4j.fonts.FontsAnalysis` in docx4j-core
   (recommended; it needs no FOP), with docx4j-export-fo's fonts jars on the
   classpath for the `--jars-only` view.

## Risks

- **A second source of truth.** If a pass keeps a special case the table
  does not know, the report lies. Phase 0's test (the answers do not change)
  and phase 1's (every mapping has a decision) are the guard; a mapping with
  no decision fails the build.
- **Cost of the use walk** on very large documents. Measured in phase 2
  before it is wired to the log; the log can use the names walk alone if the
  use walk is too slow, and grade "per script" only when asked.
- **The `authorHad` inference is wrong for some producers.** A non-Word
  producer may write panose and signature it copied from somewhere, or none
  at all for a font it had. The field always names its evidence, `UNKNOWN`
  is a legitimate answer, and the probe in phase 5 tests Word's own reading
  of the entry before the action is turned round on `UNLIKELY`.
- **The DEBUG lines are someone's diagnostics.** Removed only once the
  decisions carry everything they said; the CR-016 gap-16 style bisect must
  still be possible from `getDecisions()`.
