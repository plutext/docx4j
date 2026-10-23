# CR-060: run properties read as Word reads them (on/off values, values outside an enumeration, w:color)

Status: COMPLETE 2026-10-24 (phases 0 to 3, §8 to §11). ACCEPTED 2026-10-24 (Jason: "recommendations accepted, please
commit then implement"): D1 to D6 as recommended (§6), D1 after the preprocessor alternative was weighed (§3.1).
PROPOSED 2026-10-24, drafted by Claude Opus 5.5 (session docx4j-25) at Jason's word through the portfolio session
("yes please", to "Shall I ask docx4j-25, the TypeScript session and the Python session each to draft its CR now,
giving them these rules and the probes?"). docx4j-core-ts and docx4j-python draft theirs in parallel against the same
rules (§1). Phases in §5.

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

## 8. Phase 0 record (2026-10-24, Claude Opus 5.5, session docx4j-25)

Fixtures: the twenty probes and Word's twenty saves, in `docx4j-core-tests/src/test/resources/runprops/word365/`
with a README of provenance (PDFs and screenshots stay on the share).

Tests, red against VERSION_17_3_5 at 5d7564778 and committed `@Ignore`d, each naming the phase that turns it green:

- `org.docx4j.model.properties.run.RunPropertiesWord365Test`. Word's save is the oracle: every marker's reading in a
  probe must equal its reading in Word's save (Word writes back only values it reads one way, which docx4j reads
  right). Through the resolver (65 differ: the on/off values, the colour names and the trimmed hex), the text model
  (8: bold, italic, strike, and `vanish="off"` hidden), the HTML styling of the direct `w:rPr` (99: those, plus every
  `w:u` without a valid value underlined and `background-color:null`), the PDF styling of the effective `w:rPr` (80),
  and a docx4j round trip (73, the D2 to D4 checks included). The HTML and PDF comparisons are of what is drawn: an
  off value is no different from none, colours compare as upper-case hex, and the properties whose values come from
  the document defaults (font size, language), which Word's save adds and a probe lacks, are left out. Two cases are
  stated rather than read from the save, since the save keeps the markup: a colour-only `w:u` is no underline; and
  an invalid or missing `highlight`, `vertAlign` or `em` over a character style leaves the style's value (Word's
  repair drops the element).
- `org.docx4j.wml.OnOffAttributesTest`: each of the 48 attributes through unmarshal with fifteen spellings, and a
  structural check that every `Boolean` attribute in `org.docx4j.wml` is one of the 48 and bound through the adapter.
- `org.docx4j.model.text.InspectOnOffTest`: the inspector's own reading (below).

Found on the way:

- All 48 `xsd:boolean` attributes in `wml.xsd` are `ST_OnOff` in the 1st edition: 46 attributes, and `CT_OnOff/@val`,
  which became `BooleanDefaultTrue/@val` and `BooleanDefaultFalse/@val` (the latter used by no element now). §3.1's
  "47 attributes" counted `CT_OnOff/@val` among them.
- The inspector (CR-050) reads on/off from the XML itself (`InspectXml.on`), case-insensitively: `FALSE` and `OFF` read
  off there. It follows Word in phase 1. No other reader parses the strings: the XSLT stylesheets don't read on/off
  values, and everything else goes through JAXB.
- The resolver already reads an invalid `highlight` as none, by accident: `StyleUtil.apply(Highlight, Highlight)` calls
  `Highlight.setVal`, whose patched body nulls a value outside its colour table (and logs an error). Over a style's
  highlight that wipes the style's value, where Word's repair leaves it. Phase 2 makes `isEmpty(Highlight)` say so.
- Word's saves drop a `w:u` whose value is invalid or missing (`bogus`, `SINGLE`, `""`, `true`, bare; S11 over an
  underlining style too, whose underline then shows), and keep a colour-only one. That is evidence for phase 3's
  `w:u` (D3; the portfolio's caveat: an invalid value over a style stating `w:u` is still unmeasured).
- Out of scope, noted: `w:tblLook`'s `firstRow` and the rest are `ST_OnOff` in the 2nd edition's shared types, which
  docx4j binds as an enumeration of the six spellings (`STOnOff`): a value outside them reads null, so `TRUE` reads
  absent where Word presumably reads on. And `LsdException.isQFormat()` (patched to inherit) throws a
  `ClassCastException` for an element whose parent is not a `w:latentStyles`; an unmarshalled document never has one.

## 9. Phase 1 record (2026-10-24, Claude Opus 5.5, session docx4j-25)

- `org.docx4j.wml.OnOffAdapter` (`XmlAdapter<String, Boolean>`): exactly `false`, `off` or `0` read `FALSE`, any other
  string `TRUE`; `marshal` writes `true` or `false` (D2). It lives in docx4j-generated-objects, beside the generated
  classes that name it, not in `org.docx4j.jaxb` as §3.1 had it: that package is docx4j-core's, and a package split
  across two modules is refused on the module path. `OnOffAdapter.read(String)` is the rule for other readers.
- Bound in `wml.xsd` with an annox `target="field"` annotation on each attribute, the mechanism 39641e21c used for
  `ST_VerticalJc`. Generated: `@XmlJavaTypeAdapter(OnOffAdapter.class)` on the field; the getters, setters, field
  types and CR-059's defaults are unchanged. RI measured: the adapter receives the value untrimmed.
- 46 of the 48 attributes. Departure: `w:activeWritingStyle/@w:checkStyle` and `w:readModeInkLockDown/@w:actualPg`
  are `use="required"` without a default, so XJC generates a primitive `boolean`, which an adapter cannot bind (the
  JAXB context refused it: "Adapter OnOffAdapter is not applicable to the field type boolean"). Making them `Boolean`
  would change two public signatures, against §3.1's "no API change"; both are settings Word keeps for itself, which
  docx4j never reads. They keep `xsd:boolean`'s reading; the test pins them as primitives.
- The inspector's `InspectXml.on` reads through `OnOffAdapter.read` (it ignored case).
- Tests: `OnOffAttributesTest` (both), `InspectOnOffTest`, and `RunPropertiesWord365Test.textModelAsWordReadsIt`
  green; `WmlBooleanDefaultsTest` (CR-059) unchanged. The full core-tests suite on this phase: 2063 run, 0
  failures, 36 skipped (six of them this CR's, for phases 2 and 3).

## 10. Phase 2 record (2026-10-24, Claude Opus 5.5, session docx4j-25)

- Colour: `WordColors.normalise(String)`, in `org.docx4j.model.color` beside CR-034's colour code rather than a new
  `WordColours` in `model.properties.run` (§3.3's name was open): trim; six hex digits in either case with an optional
  leading `#` give the upper-case hex; else a name in `ColorTables.HTML`, the 140 classic HTML (Netscape) names,
  case-insensitive (D5); else null, automatic (`auto` among them). Used by `WordColors.of(Color)` and `of(U)` (the
  resolver's `resolveColor`) and `FontColor` (HTML and PDF). Automatic writes nothing in HTML (it wrote `color:#auto`,
  which a browser ignores, and `color:#F00` for `F00`, which a browser draws red) and black in PDF, as `auto` did. The
  other colour attributes (`w:shd`'s fill, the borders) are unchanged: Word's reading of a name there is unmeasured.
  Nor is it measured whether an invalid `w:color` over a style's colour is automatic or leaves the style's: docx4j
  keeps what it did (the direct value, so automatic).
- Underline: `Underline` draws nothing for a `w:u` whose `w:val` is null (bare, colour-only, or a value JAXB rejected),
  in HTML, PDF and the DrawingML conversion.
- Highlight: `HighlightColor` writes nothing for a value outside `ST_HighlightColor` (it wrote
  `background-color:null`), and `StyleUtil.isEmpty(Highlight)` says such a value is empty, so a style's highlight shows
  beneath it. The list is `WordRepairs.isHighlightColor` in docx4j-generated-objects (phase 3 needs it there).
- Tests: `RunPropertiesWord365Test` all green, with an end-to-end check through both HTML writers.

## 11. Phase 3 record (2026-10-24, Claude Opus 5.5, session docx4j-25)

- D2: `OnOffAdapter.marshal` writes `true` or `false` (phase 1).
- D3: `WordRepairs.HighlightOnSave`, `VertAlignOnSave` and `EmOnSave`, adapters on those fields of `RPr` and `ParaRPr`
  (placed by `ModifyGeneratedSources`: the elements are declared in `EG_RPrBase`, which `CT_ParaRPrOriginal` and
  `w:rPrChange`'s `w:rPr` repeat as a list, so an annotation in `wml.xsd` would land on those lists too). Reading is
  identity; marshalling returns null for a missing or invalid value, which JAXB leaves out. The object in memory keeps
  the element; only what is written changes, on every marshal path. A revision's former run properties are a list
  and are written as they are. `w:u` is kept as JAXB writes it (bare, which Word reads as no direct underline).
  Open, from the portfolio and core-ts (their D5): Word's saves drop an invalid or bare `w:u` (§8), but whether an
  invalid value over a style stating `w:u` overrides it is unmeasured; if it does, the bare write changes meaning
  there. docx4j wrote it bare before this CR too. A round-5 probe would settle it.
- D4: a colour's source value is kept (`red` stays `red`); rendered normalised (phase 2).
- MOXy (docx4j-JAXB-MOXy, EclipseLink 4.0.9, scratch probe): on/off read as Word reads it for eight spellings, a
  save drops the repaired-away elements and keeps them in memory, and the probes load and save the same.
- CHANGELOG. Hand-offs sent the same day to core-ts, docx4j-python, objects-ts and jsonix (no schema structure
  changed: annotations only), and the portfolio.
- Suites with phases 2 and 3: core-tests 2064 run, 0 failures, 30 skipped; docx4j-export-fo-tests 858, 0, 9;
  docx4j-diffx-tests 9, 0, 1.

## 12. Round 5: values over styles (built and run 2026-10-24)

Built at Jason's word ("build the round-5 probe") by docx4j-25, for §11's open item and the ports' matching ones
(core-ts CR-015 D5, docx4j-python CR-018): four probes on the share beside rounds 1 to 4 (`onoff-fun/`, round-5
section of its README; builder `build5.py` there). `props-u-styles`: an invalid, bare or colour-only `w:u` over
character and paragraph styles that underline double. `colour-styles`: an invalid, empty or bare `w:color` over
styles that colour the text. `props-enum-styles`: an invalid or bare `highlight`, `vertAlign` or `em` over styles
stating all three (a repair expected). `colour-elsewhere`: colour names in `w:u/@w:color`, `w:shd/@w:fill` and a run
border's `w:color`. LibreOffice opens all four.

docx4j's reading before Word's (868a1797b), to compare with Word's saves when they come:

- Every invalid, bare or colour-only `w:u` over either style reads as the style's double underline (HTML leaves it to
  the style's class; PDF underlines). A save writes it bare.
- `w:color` `F00`, `bogus`, `grey`, `0xFF0000`, `FF00000` over a style's colour read automatic (black in PDF); `""` and
  a bare `<w:color/>` read as the style's colour (`StyleUtil.isEmpty(Color)` treats an empty value as absent).
- Every invalid or bare `highlight`, `vertAlign` and `em` over either style reads as the style's value.
- `w:shd/@w:fill` and a border's `w:color` are not normalised: `red`, `Aqua` and `F00` are written into HTML and FO as
  `#red`, `#Aqua`, `#F00` (CSS draws `#F00` red). An underline's colour is not drawn by either writer.

Word's results (Jason's run, 24 October 2026: PDFs and `-word365.docx` saves on the share; only `props-enum-styles`
offered a repair, as expected). Read from the saves and the PDFs:

1. **`w:u`.** Over a character or a paragraph style underlining double, a bare `w:u` and the values `bogus`, `Single`,
   `SINGLE`, `""` and `NONE` read as absent: the style's double underline shows, and Word's save drops the direct
   `w:u`. docx4j reads them so, and writes them bare, which Word reads as absent: the save keeps meaning (core-ts
   D5's question). A colour with no valid value (`w:color="FF0000"` alone, or with `bogus`) gives the style's double
   underline in red, as docx4j's merge has it. **But `true` removes the underline**: Word draws none and saves
   `w:val="none"`. docx4j reads `true` as absent (JAXB rejects it), so it draws the style's underline, and its save
   writes a bare `w:u`, which turns the underline on in Word. Whether `1`, `on`, `false`, `0`, `off` or `TRUE` act as
   `true` does over a style is unmeasured (round 2 tested them only without one, where none and absent look alike).
2. **`w:color`.** Over a character style's red and a paragraph style's green, `F00`, `bogus`, `""`, a bare
   `<w:color/>`, `grey`, `0xFF0000` and seven digits read as absent: the style's colour shows, and the save drops
   them. `auto` overrides (black). docx4j reads the five invalid values that aren't empty as automatic: its PDF draws
   them black (its HTML happens to show the style's colour, writing nothing). `""` and bare already read as absent.
   Whether `Auto` or `AUTO` overrides as `auto` does, or reads as absent, is unmeasured (round 3 had no style).
3. **`highlight`, `vertAlign`, `em`.** An invalid or bare value over either style is repaired away and the style's
   value shows (superscript, dot emphasis; the paragraph style's yellow), as docx4j reads them since phase 2.
   **Found: Word drew no highlight from the character style** (M1, the style alone, has none), and its save writes
   `w:highlight w:val="none"` on every run with that style (except M6, whose own `green` shows), leaving the style
   as it was. The paragraph style's highlight shows. Whether that is Word's rule for a character style's highlight or
   the repair's doing is not known from this file: a probe without a repair would tell. docx4j applies a character
   style's highlight.
4. **Colour names elsewhere.** Word reads the names in `w:u/@w:color` (`red`, `Navy`), `w:shd/@w:fill` (`red`,
   `Aqua`) and a border's `w:color` (`red`), and saves them as hex (`FF0000`, `000080`, `00FFFF`). An invalid `F00`:
   the underline's colour is automatic (the save drops it), the shading is dropped (no shading, the save drops the
   `w:shd`), the border's colour is automatic (the save writes `auto`). docx4j normalises the underline's colour
   (not drawn by either writer) but not a fill or a border colour: HTML and PDF get `#red`, `#Aqua`, `#F00`.

## 13. Round 6: what round 5 left (built 2026-10-24, awaiting Word)

Built at Jason's word ("do round 6 first", before the fixes §12 points to) by docx4j-25: four probes on the share
(`onoff-fun/`, round-6 section of its README; builder `build6.py`, which uses `build5.py`). `props-u-onoff-styles`:
the on/off words (`true`, `True`, `TRUE`, `1`, `on`, `yes`, `false`, `0`, `off`, `no`) as a `w:u` value over
character and paragraph styles underlining double. `colour-auto-styles`: `auto`, `Auto`, `AUTO`, `" auto"`,
`"auto "`, `#auto` over a style's colour. `highlight-char-styles`: a character style's highlight with nothing to
repair (alone, with bold, inherited through `basedOn`, beside a character style's shading and a paragraph style's
highlight). `colour-elsewhere-styles`: an invalid, named or `auto` shading fill, border colour and underline colour
over character styles that make them red. LibreOffice opens all four.

docx4j's reading before Word's (round 5's commit): every on/off word reads as absent (the style's double); `Auto`,
`AUTO`, spaced and `#auto` read automatic, as `auto` does; a character style's highlight is applied (alone, with bold,
through `basedOn`); a fill or border colour that isn't six hex digits is written as is (`#F00`, `#bogus`, `#Aqua`,
`#Navy`), and `fill="auto"` is no shading; the underline's colour is not drawn.
