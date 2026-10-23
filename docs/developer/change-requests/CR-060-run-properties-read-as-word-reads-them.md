# CR-060: run properties read as Word reads them (on/off values, values outside an enumeration, w:color)

Status: ACCEPTED 2026-10-24 (Jason: "recommendations accepted, please commit then implement"): D1 to D6 as
recommended (§6), D1 after the preprocessor alternative was weighed (§3.1). PROPOSED 2026-10-24, drafted by Claude
Opus 5.5 (session docx4j-25) at Jason's word through the portfolio session ("yes please", to "Shall I ask docx4j-25,
the TypeScript session and the Python session each to draft its CR now, giving them these rules and the probes?").
docx4j-core-ts and docx4j-python draft theirs in parallel against the same rules (§1). Phases in §5.

## 0. Summary

Word 365 reads a run property's value by rules docx4j doesn't follow (measured over four rounds of probes, Jason,
24 October 2026; §1). Three of the differences change a document's meaning; the rest are rendering:

- **An on/off value Word reads as off, docx4j reads as on, and saves as on.** `w:val="off"` on `w:b`, `w:i` or any of
  the other on/off properties: docx4j's schema has typed `ST_OnOff` as `xsd:boolean` since 2008 (6cbe2e057), JAXB's
  boolean parse rejects `off` silently (the field is null), and `BooleanDefaultTrue.isVal()` answers true for null.
  A load and save writes a bare `<w:b/>`: the document is bold from then on.
- **An on/off value Word reads as on, docx4j reads as off, and saves as off**: `" false"` and `"false "` (Word matches
  the off words exactly; `xsd:boolean` trims). A single space reads off too.
- **A `w:u` without a valid `w:val` is underlined in HTML**, and one with only a `w:color` in HTML and PDF, where Word
  draws no underline. **`w:color="red"` is not red** in HTML or PDF (docx4j writes `#red`).

Fix: an `ST_OnOff` adapter that reads exactly as Word does (§3.1), with the field's type unchanged; underline,
highlight and colour rendered as Word reads them (§3.2, §3.3); and a save that never changes meaning (§3.4, D2-D4).
Small to medium. The probes become fixtures (§5, phase 0).

## 1. Word's rules (measured)

Word 365, Jason's runs on 24 October 2026, four rounds. Probes, Word's PDFs and its `-word365.docx` saves are in
`~/vbShares/Office 2016/onoff-fun/` with a README; the probes were built by docx4j-portfolio (its `onoff/build.py` and
`build4.py`). The rules as the portfolio read them, checked here against the saves where named:

1. **On/off** (`ST_OnOff`): `b`, `i`, `caps`, `smallCaps`, `strike`, `dstrike`, `outline`, `shadow`, `emboss`,
   `imprint`, `vanish` measured; by extension `bCs`, `iCs` and the rest. **Off for exactly `false`, `off` or `0`**,
   case-sensitive and untrimmed. Anything else is on: bare, `true`, `on`, `1`, `""`, `" "`, `TRUE`, `FALSE`, `OFF`,
   `False`, `" false"`, `"false "`, `00`, `no`, `yes`. No repair prompt. Word saves an on value bare; an off value is
   dropped, or kept as `w:val="0"` where it overrides a style (props-b-styles S2 to S4, S6). The toggle (two bold
   styles, S7) is not bold.
2. **Enumerations** (`u`, `highlight`, `vertAlign`, `em`): a value outside the enumeration is unset, as if absent; the
   case is exact. A bare `<w:u/>`, or one with only `w:color`, is no direct underline: over an underlined style the
   style's underline shows. `none` over a style removes the underline. An invalid `w:u` opens with no repair; a missing
   or invalid value on `highlight`, `vertAlign` or `em` makes Word repair the file, dropping the element.
3. **`w:color`**: six hex digits, any case, an optional leading `#`, surrounding whitespace trimmed; or a name from the
   classic HTML (Netscape) colour table, case-insensitive (§3.3); `auto` in any case is automatic. Anything else
   (`F00`, `0xFF0000`, five or seven digits, `GG0000`, `""`, names outside the table) is automatic. Word saves names
   and lower-case hex as upper-case hex, and drops what it rejects. Read from the saves here: `red`, `Red`, `RED` →
   `FF0000`; `darkred`, `DarkRed`, `DARKRED` → `8B0000`; `#FF0000`, `"FF0000 "`, `" FF0000"`, `ff0000` → `FF0000`;
   `darkGray` → `A9A9A9`, `lightgrey` → `D3D3D3`, `gray` → `808080`, `dimgray` → `696969`, `slategray` → `708090`,
   `gainsboro` → `DCDCDC`, `aliceblue` → `F0F8FF`, `papayawhip` → `FFEFD5`, `cornflowerblue` → `6495ED`,
   `darkorange` → `FF8C00`, `orange` → `FFA500`, `brown` → `A52A2A`, `gold` → `FFD700`, `pink` → `FFC0CB` and the rest
   of the CSS basic sixteen; dropped: `darkYellow`, `lightGray`, `grey`, `darkgrey`, `dimgrey`, `slategrey`,
   `lightslategrey`, `darkslategrey`, `rebeccapurple`, `transparent`, `currentcolor`, `lightgoldenrod`, `violetred`,
   `navyblue`, `dark red`, `window`, `buttonface`, `windowtext`.

## 2. docx4j today (measured)

docx4j's answers to the same probes (rounds 1 and 2, scratch probes, VERSION_17_3_5 at ea6501956; reported to the
portfolio the same day). All nine round 2 files load without an exception, and nothing about a value is logged.

| Value | Word | docx4j (resolver, text model, HTML, PDF) | docx4j's save |
|---|---|---|---|
| on/off `off` | off | **on** (raw null; `isVal()` true for null) | **bare: on** |
| `on` | on | on (raw null, by the same default) | bare |
| `""`, `TRUE`, `FALSE`, `OFF`, `False`, `Off`, `00`, `no`, `yes` | on | on (raw null) | bare |
| `" false"`, `"false "`, `" "` | on | **off** (`xsd:boolean` trims; `" "` parses false) | `w:val="false"`: **off** |
| `true`, `1` / `false`, `0` | on / off | on / off | `"true"` / `"false"` |
| `<w:u/>` bare, or `w:val` invalid (`""`, `SINGLE`, `Single`, `NONE`, `bogus`, `true`, `1`, `on`, `off`, `0`, `false`) | no direct underline | resolver and PDF: none; **HTML: underlined** (`Underline.getCssProperty`, `Underline.java:73`: a null value is "underline") | `<w:u/>` (no underline in Word: meaning kept) |
| `<w:u w:color="FF0000"/>` (colour only) | none | **HTML and PDF: underlined** (the resolver keeps the `w:u`, `Underline.java:73`, `:89`) | kept |
| `w:color="red"` | red | **not red**: HTML `color:#red`, PDF `color="#red"` (`FontColor`) | `"red"` (Word: `FF0000`) |
| `w:color="ff0000"` | red | red | `"ff0000"` |
| `highlight` `YELLOW`, `bogus` | none (repaired) | none, but HTML writes `background-color:null` (`HighlightColor`: the value is a String, its hex null) | kept (a repair prompt in Word) |
| bare `highlight`, `vertAlign`, `em`, `color`; `vertAlign` `Superscript`/`bogus`; `em` `DOT` | none (repaired) | none | kept, bare (a repair prompt in Word) |

Also measured, not on/off: HTML and PDF draw every underline style (`double`, `words`, `wave`, ...) as a single line,
and docx4j draws emphasis marks (`em`) in neither, the valid `dot` included.

Where it is decided: `xsd/wml/wml.xsd`, `BooleanDefaultTrue` (line 349) and `BooleanDefaultFalse` with `val` typed
`xsd:boolean`, and the 47 attributes the 1st edition typed `ST_OnOff` (48 boolean attributes now; one `ST_OnOff`
string type left); the generated `BooleanDefaultTrue.isVal()`; JAXB RI's lexical boolean parse; and, for rendering,
`docx4j-core/.../model/properties/run/Underline.java`, `HighlightColor.java`, `FontColor.java` (HTML builds a run's CSS
from its direct `w:rPr`; PDF from the resolver's effective one). `U`'s merge (`StyleUtil.apply(U, U)`) already merges
`w:val` and `w:color` separately, so a colour-only `w:u` over an underlined style keeps the style's `val` in the
resolver.

## 3. Proposed approach

### 3.1 On/off: an adapter that reads as Word does

A JAXB `XmlAdapter<String, Boolean>`, `org.docx4j.jaxb.OnOffAdapter`, bound in `wml.xsd` (an `xjc:javaType` with
`adapter=`, or a `jaxb:javaType` with parse and print methods; the schema already uses `jaxb:javaType` at line 261) to
every attribute that ECMA-376 types `ST_OnOff`:
`BooleanDefaultTrue/@val`, `BooleanDefaultFalse/@val` and the 47 attributes. `unmarshal`: exactly `false`, `off` or
`0` give `FALSE`; any other string, the empty one included, gives `TRUE`. An absent attribute never reaches the
adapter, so CR-059's defaults (what absence means) are untouched. The field and its getter keep their types: no API
change. Every reader (the resolver's toggles, the text model, both HTML writers, PDF) follows, since all read through
`isVal()` and the attribute getters.

One alternative (D1) restores `ST_OnOff` as the field's string type with typed accessors beside it: it keeps the
source's spelling on a save without further work, but changes every generated `getVal()`/`setVal()` and each of the
47 attributes' accessors, a wide API break for little gain once the reading is right.

A second alternative (Jason, 2026-10-24) is the preprocessor (`mc-preprocessor.xslt`, or the user's
`custom-preprocessor.xslt`, which replaces it): a template rewriting each on/off value to `0` or `1` as Word reads
it. The preprocessor runs only when the first unmarshal fails, and these values don't fail it: measured (JAXB RI, an
event handler counting every event), `w:b w:val="off"`, `w:u w:val="bogus"` and `w:vertAlign w:val="bogus"` raise no
`ValidationEvent` and unmarshal to null, where `w:sz w:val="12.5"` throws (`NumberFormatException`), which is how the
number repairs (`w:w`, `w:ind/@w:hanging`) are reached. docx4j already has the way to make a value fail: since 11.5.13
(39641e21c) `ST_VerticalJc` carries an adapter that throws `UnmarshalException` for a value outside the enumeration,
so the preprocessor is reached and the samples' `custom-preprocessor.xslt` can drop BIRT's
`<w:vAlign w:val="baseline"/>`; its TODO is the same adapter on every enumeration through the bindings. Measured on
VERSION_17_3_5: it fires (one event, the first unmarshal fails, the preprocessor runs); with the default
`mc-preprocessor.xslt`, which has no `vAlign` template, the second pass gives the same null as before. And the same
`custom-preprocessor.xslt` already has an on/off template (2024, #585: POI writes `w:val="off"`): `on` to `true`, `off`
to `false`, on every `@w:val`, reached only when something else fails or the part is listed in
`docx4j.jaxb.preprocess.always`.

So the route is a throwing adapter on the `Boolean` fields plus a template. The recommendation stays the reading
adapter, for on/off, because:

- The throwing adapter has to tell exactly `true`, `false`, `1` and `0` from everything else, which is the reading
  adapter's work; the template then gives the answer the adapter could have. The rule would live in two places, and
  the template has to name, by element and attribute, every place the type is used (168 element names in `wml.xsd`
  take `BooleanDefaultTrue` or `BooleanDefaultFalse`, besides the 47 attributes), where the binding reaches them by type.
- The values aren't rare or invalid in Word's eyes: POI writes `off` (#585), the 1st edition allowed `on`/`off`, and
  Word opens them without a repair prompt. Through the preprocessor, every such part is parsed twice and logs an error
  with a stack trace (`JaxbXmlPart.unmarshal`), where the reading adapter costs nothing.
- The reading would depend on the configured preprocessor. A `custom-preprocessor.xslt` replaces `mc-preprocessor.xslt`
  whole; a user's copy taken from the samples since 2024 reads `on`/`off` but still reads `" false"` as off, and one
  taken before reads `off` as on.

The adapter sees the value untrimmed (measured with `ST_VerticalJc`'s: `" top"`, `"top "` and `"Top"` each raise the
event), so it can tell `" false"` from `false`, as Word's rule needs.

Where 39641e21c's pattern fits is the enumerations Word repairs (D3): values that are invalid, rare, and whose repair
a user may want to change (as BIRT's `baseline`). It reaches an invalid value but not a missing one (no attribute, no
adapter call), so it would cover half of D3 by itself.

### 3.2 Enumerations

- `w:u`: a direct `w:u` whose `w:val` is null (bare, or a value JAXB rejected, so outside `ST_Underline`) is no
  direct underline. `Underline.getCssProperty` and `setXslFO` stop answering "underline" for null: they write nothing,
  so a style's underline still shows through its class (HTML) or the merged value (PDF). The resolver already keeps a
  style's `val` under a colour-only direct `w:u`; with a null `val` and no style, nothing is underlined.
- `highlight` (a String in docx4j): a value outside `ST_HighlightColor`, compared case-exactly, is unset: the
  resolver and `HighlightColor` treat it as absent (no `background-color:null`).
- `vertAlign`, `em`: JAXB already gives null for a value outside the enumeration; the resolver and output already
  treat that as absent. Nothing to change for reading.
- What a save writes for an invalid or missing value: D3.

### 3.3 Colour

One normaliser, `org.docx4j.model.properties.run.WordColours.normalise(String)` (name to be settled), giving upper-case
six-digit hex or null (automatic) by §1's rule 3: trim; an optional `#`; six hex digits in any case; else a name in the
classic HTML (Netscape) colour table, case-insensitive; `auto` in any case is null; anything else is null. `FontColor`
(HTML and PDF) and the underline colour use it. The table: the classic HTML list of about 140 names as Word measured it
(the gray spellings with an a, `lightgrey` with an e, no `grey`, no CSS 4 names, no system colours); the measured
names and Word's hex (§1) are the oracle, and the rest of the list is taken on the pattern four rounds established (D5).

### 3.4 What a save writes

The portfolio's rule, which this CR adopts: a save never changes meaning. With §3.1 the reading is right, so what
remains is spelling (D2, D4) and the elements Word repairs away (D3):

- On/off (D2): JAXB writes `Boolean` as `true`/`false`, so `off` comes back `false` and `" false"` comes back
  `true`: meaning kept, spelling normalised, as `1`/`0` already are today. Word's own form (bare for on, `0` for off
  over a style, an off value dropped elsewhere) is not needed for meaning.
- Colour (D4): keeping the source value (`red`, `ff0000`) keeps meaning, since Word reads it the same; writing Word's
  upper-case hex is what Word does.
- An invalid or missing enumeration value (D3): keeping it keeps the repair prompt Word shows for the source; dropping
  the element is Word's repair and changes no meaning (Word reads it as absent).

## 4. What it affects

- Every on/off getter's answer for a value that isn't `true`, `false`, `1`, `0` or bare. In practice: files whose
  producer writes `on`/`off` (the 1st edition's `ST_OnOff` allowed them; Apache POI 5.2.2 writes `off` for
  `XWPFRun.setStrikeThrough(false)`, #585, and Word 2019 and 365 show it not struck; no wider survey), and
  hand-edited or templated XML with stray whitespace or case. Their meaning changes in docx4j's reading and rendering to Word's; files Word wrote are
  unaffected (Word writes `0`, `1`, `true`, `false` or bare).
- HTML: `w:u` without a valid value no longer underlines; a colour-only `w:u` no longer underlines (PDF too); colour
  names and hex spellings render; no `background-color:null`.
- Round trip: `off` no longer becomes bold; `" false"` no longer becomes not-bold.
- The ports: core-ts and python draft their CRs against §1; this CR's fixtures (phase 0) and tests are docx4j's half
  of the shared oracle.

## 5. Phases

| Phase | What | Size |
|---|---|---|
| 0 | Fixtures: the portfolio's probes and Word's saves copied into core-tests `src/test/resources/runprops/word365/` with a README of provenance (built by docx4j-portfolio, run by Jason, 24 October 2026; the round files `onoff-*.docx`, `props-*.docx`, `colour-*.docx` and their `-word365.docx`; PDFs left on the share). Red tests: each marker's reading against §1 (getter, resolver, text model where it carries the property, HTML and PDF styling for underline and colour), and the round trip's meaning | 2 h |
| 1 | §3.1: `OnOffAdapter`, its bindings in `wml.xsd`, a clean regeneration, every reader through it; tests green | ½ day |
| 2 | §3.2 and §3.3: `Underline`, `HighlightColor`, `FontColor` and the colour normaliser; tests green | ½ day |
| 3 | §3.4 per D2-D4: what a save writes; CHANGELOG; hand-offs (core-ts, python, objects-ts for the schema change, the portfolio) | 2 h |

## 6. Decisions (accepted as recommended, Jason, 2026-10-24)

| # | Decision | Recommendation |
|---|---|---|
| D1 | On/off: an adapter on the `Boolean` fields; `ST_OnOff` restored as the field's string type with typed accessors; or the MCE preprocessor rewriting the values (§3.1) | the reading adapter: the reading becomes Word's on every path, no API change, one class and the bindings. The preprocessor would need 39641e21c's throwing adapter on the same fields, then parses each part with such a value twice, and the reading would depend on the configured preprocessor (§3.1) |
| D2 | What a save writes for on/off | `true`/`false` (JAXB's form, as `1`/`0` today): meaning kept. Word's own form (bare, `0`, dropped) and the source's spelling are both possible later; neither changes meaning |
| D3 | An invalid or missing value on `highlight`, `vertAlign`, `em` (Word repairs, dropping the element) and on `w:u` (no repair) | read as absent (§3.2); on save, keep `w:u` as JAXB writes it (bare, which Word reads as no direct underline) and drop `highlight`, `vertAlign` and `em` elements whose value is missing or invalid, as Word's repair does, so a docx4j save opens without a repair prompt |
| D4 | What a save writes for a colour | keep the source value (meaning kept); render it normalised. Writing Word's upper-case hex can come with D2's "Word's form" if wanted |
| D5 | The colour name table | the classic HTML (Netscape) list in full, as the measured pattern fits; the 60-odd measured names as tests. A further probe of the remaining names is cheap if Jason wants each one measured |
| D6 | The rendering gaps found on the way: underline styles drawn single; emphasis marks not drawn | out of scope here, named for a small CR of their own (CSS `text-decoration-style`, `text-emphasis`; FO's narrower support). This CR is about reading values |

## 7. Sources

- Probes, Word's PDFs and saves: `~/vbShares/Office 2016/onoff-fun/` and its README (docx4j-portfolio's build scripts
  `onoff/build.py`, `build4.py`); docx4j-portfolio's summaries of Jason's runs, 24 October 2026.
- docx4j's measurements: rounds 1 and 2 reported to the portfolio the same day (scratch `onoff/`, `props/`, `colour/`).
- Code: `xsd/wml/wml.xsd` (`BooleanDefaultTrue`, the `ST_OnOff`-origin attributes; `ST_VerticalJc`'s throwing adapter,
  39641e21c, 11.5.13); commit 6cbe2e057 (2008); `docx4j-samples-resources/.../custom-preprocessor.xslt` (BIRT `vAlign`,
  POI's `on`/`off`, #585); `JaxbXmlPart.unmarshal` (the preprocessor on `UnmarshalException`);
  `model/properties/run/Underline.java`, `HighlightColor.java`, `FontColor.java`; `StyleUtil.apply(U, U)`.
- ECMA-376 Part 1: the simple types `ST_OnOff`, `ST_Underline`, `ST_HighlightColor`, `ST_HexColor`; CR-059 (what
  absence means, done).
