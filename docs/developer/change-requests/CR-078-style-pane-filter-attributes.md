# CR-078: `w:stylePaneFormatFilter`'s fifteen attributes, kept on a save

Status: DONE 2026-11-06 (Jason: "commit CR-078 and start the change"; the objects session relayed it first, and he confirmed here: "yes"). Phase 2 is the ports'. Registry: `docx4j/style-pane-format-filter`.
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

The attributes repeat `w:val`'s bits (CR-019 §2.2). In all three they compute to `3F01`.

**What Word reads** (Jason: "measure which of the two Word believes when w:val and the attributes disagree, and whether it reads w:val when
the attributes are absent"). Word 16.0.20326.20232, through the bridge, 64 purpose-built files, 84 saves, all with no dialog; the 20 files
with `w:val` and attributes both, or an older mode, were saved twice, each pair the same. Word holds one value: in each of the 83 saves with
the element, `w:val` and the attributes agree, whatever the file said.

- **`w:val` counts only with no attribute beside it.** Then Word reads it, each bit as CR-019's table has it. `0x0010`, which has no
  attribute, goes: `0010` saves as `0000`, `FFFF` as `FFEF`.
- **With any attribute there, `w:val` is ignored.** `w:val="A5C2"` with the attributes for `3F01` reads `3F01`.
- **An attribute left out reads as on,** so this isn't a merge with `w:val`. `w:val="3F01" w:allStyles="0"` reads `FFEE`, not `3F00`, and
  `w:val="A5C2"` with the seven low attributes for `01` reads `FF01`. An empty element reads `FFEF`.
- **Order and mode change nothing.** `w:val` last or in the middle gives the same, as do compatibility modes 12 and 14, and Word writes all
  sixteen in every mode. A file with no element stays without one.

This rule fits all 63 files with the element. `w:val` winning fits 45, attributes over `w:val` bit by bit 58, and an attribute left out being
off 40. `CR-078-notes/` has the builder, Word's values and that check. A save shows the value Word holds; that the Styles pane uses it is
inferred, not seen.

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
- **`w:val` stays, optional, and nothing keeps it in step** (D3). A save writes what it was given. D3 was decided before §1 was measured,
  and §5's first risk follows from it. Jason confirmed it with that in hand: "D3: keep it, since keeping what the file gave is the point of the
  CR".
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
  - **Results.** `StylePaneFilterTest`, 6 tests. Markdown 180, core-tests 2486 (30 skipped), diffx 9 (1 skipped), digsig 138 (1 skipped),
    export-fo 111, export-fo-tests 908 (9 skipped) pass. docx4j's saves of the three files hold all sixteen. Word 16.0.20326.20232, through
    the bridge, each save twice: all six opened with no dialog, and each of Word's saves holds the sixteen, the same values as docx4j's,
    `w:val="3F01"`. What Word reads when they disagree: §1.
- **Phase 2, the ports.** objects regenerates `modules/`, and docx4j-python can retire its patch at its next refresh. Those are theirs.

## 5. Risks

- **`setVal` on a file that has the attributes.** `getStylePaneFormatFilter().setVal(...)` compiles on both types. Today it changes what Word
  reads, because the save loses the attributes. With this CR they stay, so Word ignores the new `w:val`, and nothing says so: a file at
  `3F01` given `A5C2` has the shape of `d-A5C2-as3F01`, which Word reads as `3F01`. No code in this repository calls it. To change the value, a
  caller sets all fifteen attributes, as one left out reads as on, or clears them all and sets `w:val`. The CHANGELOG says so.
- **A value `ST_OnOff` doesn't hold,** `maybe` say. Today it is lost with the rest of the attribute, silently. As an enum it raises a validation
  event, so the part takes the preprocessing route and the value goes, as it does for `CT_TblLook`. `aValueOutsideTheType` shows the part
  still reads: that attribute is null, and `w:val` and the others stand.
- **A caller of `getStylePaneFormatFilter()`,** as §2 says.
