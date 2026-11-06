# CR-078: `w:stylePaneFormatFilter`'s fifteen attributes, kept on a save

Status: ACCEPTED 2026-11-06 (Jason: "commit CR-078 and start the change"; the objects session relayed it first, and he confirmed here: "yes"). Registry: `docx4j/style-pane-format-filter`.
The design is decided in docx4j-generated-objects-ts's CR-019 (1f165ed, D1 to D3, Jason, 2026-11-06), so this CR has nothing new to
decide. It is here because `adding-a-schema.md` asks for a CR, reviewed and committed, before a schema changes. Phases in §4.

## 0. Summary

`xsd/wml/wml.xsd` types `w:stylePaneFormatFilter` as `CT_ShortHexNumber`, which holds `w:val` alone. Word also writes fifteen attributes
there, and a save of a settings part that has been read loses them. This CR gives the element a type of its own, so they survive.

## 1. What exists

`NumberingImplicitNumId.docx` and `headers_footers/same_as_previous_numbering.docx` in docx4j-core-tests, and `toc.docx` in
`docx4j-samples-docx4j/sample-docs`, each hold all sixteen in `word/settings.xml`:

```xml
<w:stylePaneFormatFilter w:val="3F01" w:allStyles="1" w:customStyles="0" w:latentStyles="0" w:stylesInUse="0" ... />
```

The attributes repeat `w:val`'s bits (CR-019 §2.2). In all three they compute to `3F01`. Which Word believes when the two disagree isn't
measured, and neither is whether it reads `w:val` when the attributes are absent.

No hand-written Java or stylesheet names `getStylePaneFormatFilter()`, checked in this tree. The element's name stays in
`mc-preprocessor.xslt`'s `wml-names`, which holds element names alone, so `UnknownElementsTest`'s check of it doesn't move.

## 2. The change

In `wml.xsd`, a new type, and `CT_Settings/stylePaneFormatFilter` takes it:

```xml
<xsd:complexType name="CT_StylePaneFilter">
  <xsd:attribute name="allStyles" type="s:ST_OnOff"/>
  <xsd:attribute name="customStyles" type="s:ST_OnOff"/>
  <xsd:attribute name="latentStyles" type="s:ST_OnOff"/>
  <xsd:attribute name="stylesInUse" type="s:ST_OnOff"/>
  <xsd:attribute name="headingStyles" type="s:ST_OnOff"/>
  <xsd:attribute name="numberingStyles" type="s:ST_OnOff"/>
  <xsd:attribute name="tableStyles" type="s:ST_OnOff"/>
  <xsd:attribute name="directFormattingOnRuns" type="s:ST_OnOff"/>
  <xsd:attribute name="directFormattingOnParagraphs" type="s:ST_OnOff"/>
  <xsd:attribute name="directFormattingOnNumbering" type="s:ST_OnOff"/>
  <xsd:attribute name="directFormattingOnTables" type="s:ST_OnOff"/>
  <xsd:attribute name="clearFormatting" type="s:ST_OnOff"/>
  <xsd:attribute name="top3HeadingStyles" type="s:ST_OnOff"/>
  <xsd:attribute name="visibleStyles" type="s:ST_OnOff"/>
  <xsd:attribute name="alternateStyleNames" type="s:ST_OnOff"/>
  <xsd:attribute name="val" type="ST_ShortHexNumber"/>
</xsd:complexType>
```

- **`s:ST_OnOff`, not `xsd:boolean`** (CR-019 D2). As ISO has them, and as `CT_TblLook` does in the same file. It generates the `STOnOff`
  enum, which keeps the source's spelling: `1` stays `1`. This replaces the registry's old note that docx4j follows docx4j-python, which
  has no objection.
- **`w:val` stays, optional, and nothing keeps it in step** (D3). A save writes what it was given.
- **`w:stylePaneSortMethod` stays on `CT_ShortHexNumber`.** It round-trips as it is.
- **`getStylePaneFormatFilter()`'s return type changes**, from `CTShortHexNumber` to `CTStylePaneFilter`. A caller outside this repository who
  uses it has to change, so the CHANGELOG says so.

**Departures from `adding-a-schema.md`:** the recipe is for a namespace docx4j doesn't bind, and this is a type in one it does. So there is
no schema to bring in, and no package, context, prefix or Part step. What applies is step 0, the regeneration, the tests with the Office
check, the hand-offs and one commit.

## 3. Out of scope

- `w:documentProtection` and `w:writeProtection`, whose attributes are the same kind of gap (CR-019 D4). They wait on a catalogue count, and
  would follow as an addition if they occur.
- Anything that reads these attributes. Nothing does, and the point is that they survive.

## 4. Phases

- **Phase 1, the change.**
  - The xsd, then a clean regeneration: a change in an included xsd doesn't rerun XJC without `clean`, so check the generated file's date.
  - A test on the three files: load, read the settings part, save, and all sixteen stand in the saved `settings.xml` with the source's
    spelling. Then the other spellings of `ST_OnOff`, an element with `w:val` alone, and one with attributes alone.
  - All the suites. No corpus gate is needed: settings' style pane filter plays no part in layout.
  - The Office check, through the bridge: docx4j's save of those files opens in Word with no dialog.
  - A CHANGELOG entry.
  - One commit, its hash to the objects session, whose regeneration then waits on Jason's word.
- **Phase 2, the ports.** objects regenerates `modules/`, and docx4j-python can retire its patch at its next refresh. Those are theirs.

## 5. Risks

- **A value `ST_OnOff` doesn't hold,** `maybe` say. Today it is lost with the rest of the attribute, silently. As an enum it raises a validation
  event, so the part takes the preprocessing route and the value goes, as it does for `CT_TblLook`. A test should show the part still reads.
- **A caller of `getStylePaneFormatFilter()`,** as §2 says.
