# CR-017: Font decisions with their reasons — `FontsAnalysis`, the conversion log, and the actions a user can take

Status: IN PROGRESS (2026-09-16). Jason read the CR and accepted the
recommendations the same day ("I am good with your recommendations, please
implement"); Decisions 1 to 5 below are taken as recommended. Phases 0 to 4
are delegated to an Opus 5 agent in a worktree with Fable reviewing each
phase before its commit; phase 5's measurement waits on a Word run; phase 6
waits on CR-004. Written from Jason's question of 2026-09-16 and the answer
to it (below, verbatim), after CR-016 (fonts review, DONE 2026-09-13) and
CR-001 batch 42 (the measured substitutes, 2026-09-15).
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

Two documents from batch 42 show both sides. In 9919, Word drew Meiryo, the
`w:altName` of Nokia Pure Text, and the golden embeds Meiryo: the author
lacked the named font and had the alternate. In the 17.1.1 CHANGELOG's EnBW
case, "EnBW DIN Pro Light" came out as Calibri Bold in Word, so the no-bold
rule for Light families must not fire there, because Word never treated it as
a Light family at all. The mapping rules already encode a guess
(`Mapper.addWordDefaultSubstitutes`, CR-016 phase 3): a family docx4j knows
keeps the class substitute (assume the author had it, so its widths are the
target), an unknown family gets Word's default (assume the author lacked it
too).

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
only an absent altName goes to Calibri), and a probe with a name-only entry
against one carrying panose and sig for the same absent font, run through
Word, would settle whether Word itself reads the entry that way — it is the
one measurement this section still needs.

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
   is a claim, not a measurement (P052's own comment records why).

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
   (COMMIT_HASH). `FontDecision` carries the source, the `via`, the width
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
   more, and it must stay well under the conversion's own time).
3. **`FontsAnalysis.analyse` and `FontReport`**, text and JSON, the `main`,
   the `jarsOnly` environment, the `authorHad` inference from the fontTable
   (9919 and the EnBW document as the worked examples). Tests on the CR-016 probe documents
   (`fonts-unresolvable`, `fonts-light-bold`, `fonts-symbol-and-emoji`,
   `fonts-theme-lang`): each has a known answer and a known grade.
4. **The conversion log** rendering the summary, the property, the DEBUG
   lines removed. Gate: export-fo-tests, and the log of one corpus run read
   for noise (one line per font, not per run).
5. **Selawik and Gelasio**, each measured against a Word golden before it
   enters the table (a probe per face on the share; Jason runs Word), then
   the corpus gate as batch 42 did it. Either may fail the measurement and
   stay out. The same Word run takes the `authorHad` probe: one absent font
   named by a name-only `w:font` entry and by an entry carrying `w:panose1`
   and `w:sig`, to see whether Word's own substitution reads the entry.
6. **docx4j-mcp** `fonts` tool and the `describe` section.

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
   either is in.
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
