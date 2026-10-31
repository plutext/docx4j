# CR-045: `w:val="on"` and `"off"` read as their booleans (ST_OnOff)

Status: SUPERSEDED 2026-11-01 by CR-060 (COMPLETE 2026-10-24, shipping in 17.3.5), which fixed this defect on Word's
measured reading; checked item by item in §8 (Jason: "if you are satisfied that CR-045 is entirely superseded by
CR-060, then please mark it so"). Nothing of this CR is to be built. PROPOSED 2026-10-16 (Jason's request, relayed by
the docx4j-core-ts session: "write a change request for the ST_OnOff on/off bug ... implementing it waits for his
decision").  One phase (§4); decisions in §6.

## 1. The defect

A WordprocessingML on/off property written with the literal `on` or `off` - `<w:b w:val="off"/>`,
`<w:wordWrap w:val="off"/>` - loads in docx4j as **on**, and a re-save writes it as on (`<w:b/>`,
`<w:wordWrap/>`). `off` is inverted silently; `on` comes out right only by accident (§2).

Measured 2026-10-16 on 17.3.3-SNAPSHOT (HEAD fc6059ed3), through `WordprocessingMLPackage.load` of a
docx whose styles part is the test case below, and through `XmlUtils.unmarshalString`:

| JAXB runtime | `isVal()` for `off` / `on` (field) | events | re-marshal |
|---|---|---|---|
| ReferenceImpl (jaxb-runtime 4.0.9) | true / true (`null`, `null`) | none: silent | `<w:pPr><w:wordWrap/></w:pPr><w:rPr><w:b/><w:i/></w:rPr>` |
| MOXy (eclipselink 4.0.9) | true / true (`null`, `null`) | `JaxbValidationEventHandler` logs `[ERROR]` / `[(non)FATAL_ERROR]` ConversionException ("The object [off] ... could not be converted to [class java.lang.Boolean]"), `JaxbXmlPartXPathAware` warns; the part still loads | the same |

The test case (not the user's file, which is a business document):

```xml
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:style w:type="paragraph" w:styleId="S">
    <w:pPr><w:wordWrap w:val="off"/></w:pPr>
    <w:rPr><w:b w:val="off"/><w:i w:val="on"/></w:rPr>
  </w:style>
</w:styles>
```

**Where it comes from**: pandoc 3.6.1 writes `<w:wordWrap w:val="off"/>` in its built-in "Source Code"
style (the file that found it, through docx4j-ts-editor). Older generators and Word 2003-era converters
write `on`/`off` too (the editor's report, not measured). Word opens such files, and ECMA-376
Transitional allows them: its `ST_OnOff` is `true`, `false`, `on`, `off`, `0`, `1`.

## 2. The cause

docx4j binds `CT_OnOff` as `BooleanDefaultTrue` (and `BooleanDefaultFalse`), whose `val` is declared
`xsd:boolean` in `xsd/wml/wml.xsd` "for more intuitive field generation by JAXB" - a `Boolean` field
and an `isVal()` that answers true for null. `xsd:boolean`'s literals are `true`, `false`, `1`, `0`; the
RI's `DatatypeConverterImpl._parseBoolean` returns null for `on` and `off` (core-ts ran it in jshell),
MOXy raises a ConversionException and leaves the field null. So both runtimes give null, and null means
on. Nothing in docx4j reads on/off before JAXB (no preprocess, no adapter). The schema's own comment on
`BooleanDefaultTrue` foresaw it: "a third party processor could choose to use 'on' or 'off', even where
Word doesn't. This would cause trouble."

Not affected: math's on/off (`org.docx4j.math.CTOnOff`, bound to the `STOnOff` enum, which has `on` and
`off`), `w:tblLook`'s flags (`STOnOff`), and w14's `CTOnOff` (Word 2010's own `ST_OnOff` admits only
`true`, `false`, `0`, `1`).

## 3. Scope

- **`BooleanDefaultTrue.val` and `BooleanDefaultFalse.val`**: every `CT_OnOff` element (`w:b`, `w:i`,
  `w:caps`, `w:vanish`, `w:keepNext`, `w:wordWrap`, `w:widowControl`, `w:bidi`, ... - 33 generated wml
  classes use the two types). This is the defect as found.
- **The other 43 `xsd:boolean` attributes of `wml.xsd` that ECMA types `ST_OnOff`** (optional, D1):
  `BooleanDefaultTrue@legacy`, `CT_Border@shadow`/`frame`, `CT_EastAsianLayout@combine`/`vert`/
  `vertCompress`, `CT_FramePr@anchorLock`, `CT_PPrBase@beforeAutospacing`/`afterAutospacing`,
  `CT_SimpleField@fldLock`/`dirty`, `CT_FldChar@fldLock`/`dirty`, `CT_Columns@equalWidth`/`sep`,
  `CT_SdtText@multiLine`, `CT_FtnEdnRef@customMarkFollows`, `CT_Lvl@legacy`/`tentative`,
  `CT_FontRel@subsetted`, `CT_DocProtect@formatting`/`enforcement`, `CT_TrackChangesView@*`,
  `CT_WritingStyle@*`, `CT_DocParts@*`, `CT_Caption@chapNum`/`noLabel`, and the rest of the 45.

## 4. Design (one phase)

- An `XmlJavaTypeAdapter<String, Boolean>` (say `org.docx4j.jaxb.OnOffAdapter`) on the `val` attribute
  of `BooleanDefaultTrue` and `BooleanDefaultFalse`, put in through `xsd/bindings.xjb` (or the xsd's
  annox annotations) so that it survives regeneration. It reads `true`, `1`, `on` as true and `false`,
  `0`, `off` as false, and anything else as null with a warning (so the default applies, as today); it
  writes `true`/`false`, as docx4j writes now.
- **The spellings agree with the TypeScript side**: jsonix-CR-007 (jsonix master d01237f, local, 3.4.1
  proposed) reads `on`/`off` in its `Boolean.parse` and still rejects `On`, `OFF`, padded values, `yes`,
  `no` and the empty value; docx4j's adapter takes exactly the same set (D2).
- **Tests** in docx4j-core-tests, under ReferenceImpl and MOXy (the MOXy suite): the test case loaded
  through `WordprocessingMLPackage.load` and `XmlUtils.unmarshalString` (`off` false, `on` true, absent
  `val` true), a round trip writing `w:val="false"` for `off`, no validation event under MOXy, and
  `PropertyResolver` resolving the style's run as not bold.
- **The regeneration** goes through `docx4j-generated-objects` (a clean `mvn install`), the module-info
  exports the adapter's package if it is new, and the ports' parity goldens are re-cut where a fixture
  holds `on`/`off` (none does today: core-ts's parity goldens are docx4j's answers).

## 5. Risk

- The adapter runs for every on/off property of every document: cost to measure on a large document
  (it replaces the runtime's own boolean parse, so it should be in the noise).
- A document that relied on `off` reading as on - none should; it is the defect.
- MOXy and the RI apply `XmlJavaTypeAdapter` to an attribute alike, but MOXy's binding to the generated
  classes is its own: the MOXy suite is the check.

## 6. Decisions for Jason

1. **D1**: the `CT_OnOff` elements only (the defect as found), or all 45 `xsd:boolean` attributes ECMA
   types `ST_OnOff`. Recommended: all, in one regeneration, since the cause is the same.
2. **D2**: accept exactly jsonix-CR-007's spellings (`true`, `false`, `1`, `0`, `on`, `off`), so docx4j
   and the TypeScript packages read the same files the same way. Recommended: yes.
3. **D3**: when: 17.3.3. It is a silent inversion of formatting in files Word opens.

## 7. Hand-offs

- docx4j-core-ts: this CR's number (it asked); once it lands, a parity fixture with `on`/`off` can be
  added on both sides.
- docx4j-python, docx4j-generated-objects-ts: the same spellings, if their parsers differ.

## 8. Superseded by CR-060 (2026-11-01)

CR-060 (run properties read as Word reads them; COMPLETE 2026-10-24, in 17.3.5) took this defect up from Word 365's
measured reading and built it. Each item of this CR against it:

- **§3, `BooleanDefaultTrue.val` and `BooleanDefaultFalse.val`**: bound to `OnOffAdapter` (docx4j-generated-objects,
  annox on the field, the field types and getters unchanged). Checked 2026-11-01 on VERSION_17_3_5:
  `<w:b w:val="off"/>` and `<w:wordWrap w:val="off"/>` read `isVal()` false and re-marshal as `w:val="false"`.
- **§3, the other `ST_OnOff` attributes (D1, "all")**: 46 of the 48 `xsd:boolean` attributes, CR-060 §8. Its recorded
  departure: `w:activeWritingStyle/@w:checkStyle` and `w:readModeInkLockDown/@w:actualPg`, required attributes XJC
  makes primitive `boolean`, which an adapter cannot bind without changing two public signatures; settings Word keeps
  for itself, which docx4j never reads.
- **§4 and D2, the spellings**: this CR proposed jsonix-CR-007's set (`true`, `false`, `1`, `0`, `on`, `off`; anything
  else null with a warning). CR-060 measured Word instead (§1, rule 1): off for exactly `false`, `off` or `0`,
  case-sensitive and untrimmed; anything else on (`" false"`, `OFF`, `no` among it). Word's reading is the one to
  follow, and CR-060 handed it to core-ts, docx4j-python, objects-ts and jsonix the same day.
- **§4, the tests**: CR-060's `OnOffAttributesTest` (every attribute, fifteen spellings), `InspectOnOffTest` and
  `RunPropertiesWord365Test` (the resolver, the text model, HTML and PDF following); `WmlBooleanDefaultsTest` (CR-059)
  unchanged. MOXy: CR-060 ran a scratch probe; checked again 2026-11-01 under docx4j-JAXB-MOXy (EclipseLink 4.0.9):
  `off` reads false and saves `false`, with no `ConversionException` and no validation event, where §1 measured both.
- **§5, the risks**: the adapter replaces the runtime's own boolean parse (CR-060 §8: no cost of note); MOXy checked.
- **D3, when**: 17.3.5, by CR-060.
- **§7, the hand-offs**: CR-060's, the same day; this CR's number, which core-ts asked for, now points here.
