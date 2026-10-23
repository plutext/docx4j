# CR-059: the boolean attribute defaults docx4j added to wml.xsd, checked against ECMA-376

Status: COMPLETE 2026-10-24 (phases 0-2; ac97b56c5). PROPOSED 2026-10-24, drafted by Claude Opus 5.5 (session docx4j-25) at Jason's request ("please write the CR to
check the remaining 32"), after issue #697 found five such defaults (fixed separately, uncommitted at writing: §2.4).
The count is 38, not 32 (§2): a line-by-line grep had missed declarations written over several lines. ACCEPTED
2026-10-24 (Jason: "D1 to D5 recs make sense. Commit all, then implement CR-059"): D1 to D5 as recommended (§6).
Phases in §5. COMPLETE 2026-10-24: phase 0 (§8, Jason's Word 365 run) and phase 1 (§9) in ac97b56c5, pushed;
phase 2, the hand-offs, sent the same day (§10).

## 0. Summary

docx4j's copy of the WordprocessingML schema, `xsd/wml/wml.xsd`, declares a default on boolean attributes that
ECMA-376 leaves without one. XJC turns each default into the generated getter's answer for an absent attribute, so
`isTentative()`, `isMultiLine()`, `isLocked()` and their like answer `true` for an attribute the document doesn't
carry, where the spec reads the absence as off, as "inherit from the parent", or as "the application decides".

- **Origin.** Not ECMA. The ECMA-376 1st edition schema, imported unchanged on 2008-04-29 (670a17507), has no
  `default` on any attribute, and neither has ISO/IEC 29500-4:2016 transitional. The next day's commit 6cbe2e057
  ("Commit all enhancements to original wml.xsd") replaced the string type `ST_OnOff` with `xsd:boolean` for
  JAXB-friendly getters, and gave 41 of them `default="true"` (and 3 `default="false"`), carrying over the rule for an *element* such as
  `<w:b/>` (no `w:val` means on, which `BooleanDefaultTrue` models correctly) to standalone *attributes*, where it
  does not hold (§1).
- **What is left.** 38 declarations: 34 `default="true"` and 4 `default="false"` (§2). Read against the spec's
  prose (§2.2): 24 are off when absent, 3 inherit from `w:latentStyles`, 5 are left to the application, 2 the prose
  doesn't settle (`w:cols`' `equalWidth` and `sep`), and of the 4 `false` ones 2 agree with the prose and 2 (the
  autospacing pair) agree with ISO's schema but not with its prose.
- **What it affects.** Two getters that docx4j's own code reads, `CTColumns.isEqualWidth()` (column layout in PDF
  and HTML) and `CTSdtText.isMultiLine()` (binding), would change docx4j's output, so both go through Word first
  (phase 0). `isSubsetted()` has a workaround that can go. The rest have no caller in docx4j; their change is to
  what a user's code reads.
- **Fix.** Per attribute: `default="false"` where absent is off (keeps the `boolean` getter, as #697 did); no
  default where absent means inherit or application-defined (the getter becomes `Boolean`, null when absent),
  subject to D2; Word decides the open ones. The `Style.isCustomStyle()` source patch (#641) retires.

## 1. Origin: the schemas compared

| Schema | Attributes with a `default`, all types | `fldLock` on `w:fldChar` |
|---|---|---|
| ECMA-376 1st edition (2006), as imported 2008-04-29 (670a17507) | none | `ST_OnOff`, no default |
| docx4j after 6cbe2e057 (2008-04-30) | 42 boolean `default="true"` (one the element type `BooleanDefaultTrue`'s `val`), 3 `default="false"` | `xsd:boolean`, `default="true"` |
| ISO/IEC 29500-4:2016 transitional (`wml.xsd`) | a few, e.g. `nlCheck` "off", autospacing "off" | `s:ST_OnOff`, no default |
| docx4j today | 35 `default="true"` (one the element type `BooleanDefaultTrue`), 10 `default="false"` | `default="false"` (#697) |

The comment 6cbe2e057 left on `BooleanDefaultTrue` gives the reasoning: it "Replaces CT_OnOff, for more intuitive
field generation by JAXB", and offers an "Equivalent `type="xsd:boolean" default="true"` formulation" for
attributes. A side effect, noted there: `xsd:boolean` takes `true`, `false`, `1`, `0`, not the 1st edition's `on` and
`off`. That is out of scope here (Word writes `1`/`0`/`true`/`false`).

Other schemas, compared the same way against ISO 2016 (attribute by enclosing type): `sml` matches ISO for all 774
attributes with a default; `pml` and `dml-chart` differ only in percentage literals (`100` against ISO's `100%`, an
edition change in `ST_Percentage`), not in booleans; the DrawingML main files have 76 attributes docx4j restructured
that the comparison can't place. None is the 2008 pattern; out of scope (§4).

## 2. The inventory

Method: every `xsd:attribute` of type `xsd:boolean` with a `default` in `wml.xsd`, by an XML parse (a line grep
miscounts both ways: it misses declarations written over several lines and counts the schema's comments); ISO 2016's declaration by enclosing type; the spec's prose for the attribute, ECMA-376 1st edition text
as mirrored on webapp.docx4java.org (section numbers are the 1st edition's Part 4; later editions renumber, e.g.
`lsdException` is 17.7.4.8); docx4j's callers by a grep for the getter in every module's `src/main`.

"Reading" is what absence means by the prose: **off**; **inherit** (from the named parent); **application** (the
prose leaves it to application settings); **open** (the prose gives no rule). For a port without Java's `boolean`
getters (objects through jsonix's default reader), "inherit" and "application" are "no default": `undefined`, not
`false`.

### 2.1 `default="true"`, 34

| Element | Attribute | ISO schema | Prose when omitted | Reading | docx4j callers |
|---|---|---|---|---|---|
| border elements (`w:top`, `w:left`, ... in `w:pBdr`, `w:tcBorders`, ...) | `shadow` | none | "the border is not given the shadow effect" | off | none |
| border elements | `frame` | none | "the border is not given any frame effect" | off | none |
| `w:eastAsianLayout` | `combine` | none | "this run shall not be displayed on two sub lines" | off | none |
| `w:eastAsianLayout` | `vert` | none | "the contents of this run shall not be rotated" | off | none |
| `w:eastAsianLayout` | `vertCompress` | none | "text shall not be compressed" | off | none |
| `w:framePr` | `anchorLock` | none | "this frame shall not have a locked anchor position" | off | `StyleUtil` equality (both sides alike; unaffected) |
| `w:activeWritingStyle` | `nlCheck` | **"off"** | no rule in the prose | off (ISO's schema) | none |
| `w:documentProtection` | `formatting` | none | "no formatting restrictions shall be applied" | off | none |
| `w:revisionView` | `markup` | none | shown "based on application-level settings" | application | none |
| `w:revisionView` | `comments` | none | the same | application | none |
| `w:revisionView` | `insDel` | none | the same | application | none |
| `w:revisionView` | `formatting` | none | the same | application | none |
| `w:revisionView` | `inkAnnotations` | none | the same | application | none |
| `w:cols` | `equalWidth` | none | defines only "present and true" and "present and false" | open → **on** (Word, §8): equal columns; `default="true"` stays | `UnequalColumns`, `LayoutMasterSetBuilder` (column widths in HTML and PDF), unchanged |
| `w:cols` | `sep` | none | "If set to true ... a vertical line shall be drawn"; no rule for absence | open → **off** (Word, §8) | none |
| `w:text` (in `w:sdtPr`) | `multiLine` | none | "shall not allow soft line breaks" | off | `BindingTraverserCommonImpl`, `UpdateXmlFromDocumentSurface` (a bound value's line breaks) |
| `w:hyperlink` | `history` | none | "its value shall be assumed to be false" | off | none |
| `w:footnoteReference`, `w:endnoteReference` | `customMarkFollows` | none | "the reference mark shall not be skipped" | off | none |
| `w:writeProtection` | `recommended` | none | "user interface recommending ... should not be provided" | off | none |
| `w:lvlText` | `null` | none | "the null string shall not be used" | off | none |
| `w:legacy` | `legacy` | none | the properties apply "if the legacy attribute is set" | off | none |
| `w:lvl` | `tentative` | none | `off` "is the default value ..., and is implied when this attribute is omitted" | off | none |
| `w:embedRegular` etc. | `subsetted` | none | "shall not be handled as though it is subsetted" | off | `FontEmbedder` reads the XML around the getter (its comment); the workaround can go |
| `w:types` (`w:docPartPr`) | `all` | none | "its default value shall be assumed to be false" | off | none |
| `w:name` (`w:docPartPr`) | `decorated` | none | "its value shall be assumed to be false" | off | none |
| `w:style` | `customStyle` | none | "the style shall be assumed to be a built-in style" | off | getter already patched to false (#641) |
| `w:lsdException` | `locked` | none | "determined by the `defLockedState` attribute on the parent `latentStyles`" | inherit | none |
| `w:lsdException` | `semiHidden` | none | "determined by ... `defSemiHidden`" | inherit | none |
| `w:lsdException` | `unhideWhenUsed` | none | "determined by ... `defUnhideWhenUsed`" | inherit | none |
| `w:latentStyles` | `defLockedState` | none | "shall be false" | off | none |
| `w:latentStyles` | `defSemiHidden` | none | "shall be false" | off | none |
| `w:latentStyles` | `defUnhideWhenUsed` | none | "shall be false" | off | none |
| `w:caption` (settings) | `chapNum` | none | "chapter numbers shall not be included" | off | none |
| `w:caption` | `noLabel` | none | "the name shall be added to the caption" | off | none |

### 2.2 Tally

| Reading | Count | Attributes |
|---|---|---|
| off | 24 | every row marked off above |
| inherit | 3 | `lsdException`: `locked`, `semiHidden`, `unhideWhenUsed` |
| application | 5 | `revisionView`'s five |
| open | 2 | `cols`: `equalWidth` (Word: on, unchanged), `sep` (Word: off) |

### 2.3 `default="false"`, 4 besides #697's

| Element | Attribute | ISO schema | Prose when omitted | Reading |
|---|---|---|---|---|
| `w:style` | `default` | none | absent on a style, it is not the default style | off: agrees |
| `w:latentStyles` | `defQFormat` | none | "shall be false" | off: agrees |
| `w:spacing` (in `w:pPr`) | `beforeAutospacing` | "off" | "determined by the setting ... at any level of the style hierarchy" | inherit by the prose; ISO's schema says off. Open → **inherit** (Word, §8): no default |
| `w:spacing` | `afterAutospacing` | "off" | the same | the same |

docx4j's callers of the autospacing pair: `PropertyFactory`, `XsltFOFunctions` (twice), `ConversionSectionWrapperFactory`.

### 2.4 Already done: #697

`w:fldChar` and `w:fldSimple` `fldLock` and `dirty`, `w:documentProtection` `enforcement`: `default="false"`
(their prose: off), with `FieldRef` reading the lock from the field's `w:fldChar`. Built and tested 2026-10-24
(`FieldLockAttributeTest`, docx4j-core-tests in full), to be committed as the #697 fix; not part of this CR's phases.

## 3. What it affects

- **docx4j's output**, through two getters only: `isEqualWidth()` decides whether `w:col` widths are honoured in
  HTML and PDF (today an absent `equalWidth` reads as equal widths, so `w:col` children are ignored); `isMultiLine()`
  decides whether a bound value's line breaks become `w:br` in a plain-text control (today an absent `multiLine` reads
  as allowing them). Each changes what a user sees, so Word measures both first (phase 0).
- **Callers' code**: every getter in §2.1 answers differently for an absent attribute. Most have no caller in
  docx4j; a user's code that tests, say, `lvl.isTentative()` gets the spec's answer instead of `true` for every level.
  Where the default goes entirely (D2), the getter's return type changes from `boolean` to `Boolean`: source
  compatible except where it is unboxed into a `boolean` while null.
- **The ports**: objects generates its mapping from this xsd (jsonix's compiler copies every default as inert
  metadata today); jsonix proposes a reader that would apply them, and asks this CR to name, per attribute, whether the
  reading is a value or "no default" (§2's Reading column is that answer). python and core-ts read the XML, not
  generated defaults (to confirm at hand-off).
- **Round trip**: none. JAXB writes only what was set; an absent attribute stays absent whatever its default.

## 4. Out of scope

- The element form `BooleanDefaultTrue`/`BooleanDefaultFalse` (`w:b`, `w:i`, ...): correct as it is.
- `on`/`off` literals (§1).
- Other schemas (§1): their defaults come from the ECMA editions they were taken from; the percentage-literal
  differences in `pml` and `dml-chart` are a separate, older question.

## 5. Phases

| Phase | What | Size |
|---|---|---|
| 0 | DONE 2026-10-24 (§8). Word probes for the open ones, by a generated docx in the share's style: `w:cols w:num="2"` with two `w:col` of different widths and no `equalWidth` (does Word use the `w:col` widths?), and no `sep` (a line?); a plain-text control without `multiLine`, bound to a value with a newline (does Word show the line break?); a paragraph whose style sets `beforeAutospacing="1"` and whose own `w:spacing` omits it (inherited or off?). Red tests for §2's readings, one per reading class, through the getters | 2 h + Jason's Word run |
| 1 | BUILT 2026-10-24 (§9). The xsd: `default="false"` for every "off"; per D2 for "inherit" and "application"; per phase 0 for the open ones. Clean regeneration of docx4j-generated-objects; the `Style-customStyle-default` patch removed from `ModifyGeneratedSources`; `FontEmbedder`'s workaround simplified; callers adjusted to phase 0's results; tests green; CHANGELOG (one entry naming every getter whose answer changes) | ½ day |
| 2 | DONE 2026-10-24 (§10). Hand-offs: objects-ts and jsonix (§2's Reading column, "no default" for inherit and application), python, core-ts; the portfolio | 1 h |

## 6. Decisions (accepted as recommended, Jason, 2026-10-24)

| # | Decision | Recommendation |
|---|---|---|
| D1 | An attribute that is off when absent | `default="false"`, as #697: the `boolean` getter stays, its answer becomes the spec's |
| D2 | One that inherits (`lsdException`) or is left to the application (`revisionView`) | remove the default: the getter becomes `Boolean`, null when absent, so a caller can tell "inherit" or "application" from an explicit false. No docx4j code calls them. The alternative, `default="false"`, keeps the signature but gives a wrong answer for `lsdException` (where `w:latentStyles` says true) |
| D3 | `equalWidth`, `sep` and the autospacing pair | follow Word's phase 0 result; until then unchanged |
| D4 | `multiLine` | off, per the prose, if Word agrees in phase 0 (a newline in the bound value then becomes what Word shows without `multiLine`) |
| D5 | Release | 17.3.5, with #697: the same defect, and most getters have no caller; CHANGELOG names each |

## 7. Sources

- `xsd/wml/wml.xsd`; commits 670a17507 and 6cbe2e057 (2008); `docx4j-generated-objects/ModifyGeneratedSources.java`
  (the `Style-customStyle-default` patch, #641).
- ISO/IEC 29500-4:2016 transitional schemas (a copy on this machine, used for the comparison).
- ECMA-376 1st edition Part 4 text, mirrored at webapp.docx4java.org/OnlineDemo/ecma376/WordML/ (pages
  `top_1`, `eastAsianLayout`, `framePr`, `activeWritingStyle`, `documentProtection`, `revisionView`, `cols`, `text`,
  `hyperlink_2`, `footnoteReference`, `writeProtection`, `lvlText`, `legacy`, `lvl_1`, `embedRegular`, `types`,
  `name_4`, `style_1`, `lsdException`, `latentStyles`, `caption`, `spacing_1`).
- Issues #697 (this pattern on fields), #641 (`customStyle`); jsonix-d2's questions of 2026-10-24 (the "no default"
  distinction for the ports).

## 8. Phase 0 record (2026-10-24, Claude Opus 5.5, session docx4j-25)

Probes on the share, `Office 2016/cr059/` (README there), written by a zipfile script (scratch `cr059/probe/build.py`):

- `cr059-cols.docx`: four sections of two columns. C1 has two unequal `w:col` and neither `equalWidth` nor `sep`;
  C2 has the same with `equalWidth="0" sep="0"` and C3 has `equalWidth="1" sep="1"`, both controls; C4 has neither
  attribute and no `w:col`.
- `cr059-multiline.docx`: three plain-text controls bound to `ML_LINE_ONE` + newline + `ML_LINE_TWO`. ML1 has
  `w:text` with no `multiLine`; ML2 has `multiLine="1"` and ML3 has `multiLine="0"`, both controls.
- `cr059-autospacing.docx`: style "Auto Spaced" sets both autospacing attributes. AS1 uses the style alone; AS2 has
  its own `w:spacing before="0" after="0"` without the autospacing attributes; AS3 sets both to `"0"`, a control;
  AS4 has only `w:line`. Jason reads Format > Paragraph for each.

What docx4j does today with them (before phase 1 touches these five):

| Probe | docx4j today |
|---|---|
| C1, C4 (`equalWidth`, `sep` absent) | `isEqualWidth()` true, `isSep()` true: C1's `w:col` widths ignored |
| ML1 (`multiLine` absent) | **the traversers disagree**: XSLT binds `ML_LINE_ONEML_LINE_TWO`, the newline dropped and the words run together; NonXSLT writes a `w:br` (it reads `isMultiLine()`, true) |
| ML3 (`multiLine="0"`) | both traversers run the words together, which Word is unlikely to do (to see) |
| AS2 (autospacing omitted on the paragraph) | `PropertyResolver`'s effective spacing has both autospacing true: inherited from the style, the prose's reading, not ISO's schema default |

**Word 365's answers** (Jason, 2026-10-24; `cr059-cols.pdf`, the two PNGs, the `-word365.docx` saves):

| Probe | Word 365 | Word's save | Reading |
|---|---|---|---|
| C1 (`equalWidth`, `sep` absent, two unequal `w:col`) | two **equal** columns, no line | `<w:cols w:num="2" w:space="720"/>`: the `w:col` dropped | `equalWidth` absent = on; `sep` absent = off |
| C2 (`equalWidth="0" sep="0"`) | unequal, as the `w:col` say | `equalWidth="0"` and the `w:col` kept, `sep` dropped | control |
| C3 (`equalWidth="1" sep="1"`) | equal, a line | `w:sep="1"`, `equalWidth` **dropped** | Word itself writes an absent `equalWidth` for equal columns |
| C4 (neither, no `w:col`) | equal, no line | unchanged | as C1 |
| ML1 (`multiLine` absent) | `ML_LINE_ONEML_LINE_TWO`: the newline dropped, the lines run together | `<w:text/>`, the same text | `multiLine` absent = off |
| ML2 (`multiLine="1"`) | two lines | a `w:br` between | control |
| ML3 (`multiLine="0"`) | as ML1 | `multiLine="0"` dropped: `<w:text/>` | Word writes off as absent |
| AS1, AS2, AS4 (autospacing absent on the paragraph) | Auto spacing before and after (the screenshot's gaps) | AS2's own `w:spacing before="0" after="0"` dropped as redundant | the attribute absent = **inherit** from the style |
| AS3 (`beforeAutospacing="0" afterAutospacing="0"`) | no space | kept | control |

So docx4j's XSLT traverser was right about ML1 and ML3, and the NonXSLT traverser's `w:br` was the defect. Correcting
§8's first reading: running the words together is Word's own behaviour, not a defect.

## 9. Phase 1 record (2026-10-24, Claude Opus 5.5, session docx4j-25)

- `xsd/wml/wml.xsd`: 23 attributes `default="true"` → `"false"` (§2.1's "off" rows except `multiLine`); 8 lose
  their default (`lsdException`'s three, `revisionView`'s five), so their getters are now `Boolean`, null when
  absent. After phase 0: `sep` and `multiLine` → `"false"`; the autospacing pair loses its default (`Boolean`,
  null when absent, inherited); `equalWidth` keeps `"true"`, which Word confirms. An XML parse confirms nothing else
  in the file defaults to true except `BooleanDefaultTrue`'s own `val` and `equalWidth`.
- The autospacing getters' callers unboxed them and are now null-safe (`Boolean.TRUE.equals`): `PropertyFactory`
  (two), `ConversionSectionWrapperFactory`, `XsltFOFunctions` (four). `AutospacingAccess` (17.1.0), which read the
  raw field to get the inherit reading round the old default, now answers as the getters do and stays for callers.
- `CTSdtText.isMultiLine()` false when absent puts the NonXSLT traverser where Word and the XSLT traverser are; test
  `MultiLineBindingTest` (both traversers, the probe as fixture `OpenDoPE/text-control-multiline.docx`).
- `docx4j-generated-objects/ModifyGeneratedSources.java`: the `Style-customStyle-default` patch (#641) removed; XJC
  now generates the getter it needed. `FontEmbedder`'s and `FontSubsetTest`'s comments no longer describe the old
  default.
- Test `org.docx4j.wml.WmlBooleanDefaultsTest`: every "off" attribute absent reads false (32 getters, #697's five
  included), every "inherit" and "application" one reads null (10), an absent `equalWidth` reads true, a written
  value reads as written, and an absent attribute is not written back.
- Runs: before phase 0's five, the full clean reactor build, BUILD SUCCESS, 3493 tests, 0 failures; after them, the
  defaults, autospacing, binding, output and text suites, 552 tests, 0 failures, and the full clean reactor build
  again: BUILD SUCCESS, 3495 tests, 0 failures (docx4j-core-tests 2047).

## 10. Phase 2 record, hand-offs (2026-10-24)

Jason: "commit then push (closing those issues) then do phase 2". Phases 0 and 1 committed as ac97b56c5 and pushed
with #697 (77ba784d6), #236/#256 (23f2a32ae), CR-052's update (c37da7ec6) and the accepted CR (1fffc20a3); the
issues closed on GitHub with comments naming the commits. Sent:

- jsonix-d2: the final readings for the open five (`equalWidth` a real true; `sep`, `multiLine` off; the autospacing
  pair no default), and that with D2 the regenerated mapping reads right everywhere, no default-removing compiler
  extension needed for wml (its CR-009 §8 cites §2).
- docx4j-generated-objects-ts: regenerate wml from this xsd; it changes inert metadata only until jsonix CR-009's
  reader lands; its CR-007 `customStyle` override becomes redundant.
- docx4j-python: its `schemas/wml/wml.xsd` copy carries 40 of the old `default="true"` as `schema_default` metadata
  (its fields default to None, so absent stays absent); regenerate from this xsd; the `multiLine` rule Word measured;
  #697 and #236/#256.
- docx4j-core-ts: the `multiLine` rule, the autospacing inheritance, `equalWidth`/`sep`; its extractor already reads
  `fldLock` as written.
- docx4j-portfolio: the phase status.
