# CR-045: `w:val="on"` and `"off"` read as their booleans (ST_OnOff)

Status: PROPOSED 2026-10-16 (Jason's request, relayed by the docx4j-core-ts session: "write a change
request for the ST_OnOff on/off bug ... implementing it waits for his decision").  One phase (§4);
decisions in §6.

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
