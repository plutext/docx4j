# CR-048: VML attributes Word writes kept on a round trip - `w14:anchorId` on the shapes, `o:gfxdata` beyond `v:shape`

Status: IMPLEMENTED 2026-10-18, Word-open check passed (§5). Accepted the same day (Jason: "i accept the recommendations provided for decisions D1 to D5;
please commit then implement"). Drafted at Jason's request ("the generated-objects-ts session is also
concerned about w14:anchorId; please ask it, then draft a CR addressing the issue"). Reported by the
docx4j-core-ts session (its CR-010 section 10.2, core-ts 328439e) and the docx4j-generated-objects-ts
session (its CR-009 section 8, objects-ts 91196dd; 0ad1487 names this CR as the dependency of its
proposal 2); measured in docx4j for this CR. Reviewed the same day by Claude Fable 5.1 (session
docx4j-44), whose six items are taken in. One phase (§5); decisions in §6. Written by `docs/developer/adding-a-schema.md` step 0, as CR-023 was for
WordprocessingML: no new namespace is bound, but a schema docx4j took from ECMA-376 is patched to admit
what Word writes, which the recipe asks to be named.

## 1. The defect

A VML shape that Word wrote loses attributes when docx4j re-marshals its part. The part is
re-marshalled whenever it is unmarshalled and saved (any edit, `unmarshalAll`, the FO exporter's
working copy). Since CR-021, docx4j keeps `mc:AlternateContent` whole, so these shapes are now mostly
the `mc:Fallback` twins of DrawingML text boxes and shapes.

Measured 2026-10-18 on 17.3.4-SNAPSHOT (HEAD f71cdd299, reference implementation) over every docx in
three resource directories (`docx4j-core-tests/src/test/resources`, `docx4j-samples-resources`,
`docx4j-export-fo-tests/src/test/resources`) whose WordprocessingML parts hold VML: 29 files, 36
parts, 1,839 `v:` elements. Each
part was loaded with `WordprocessingMLPackage.load` and marshalled with `XmlUtils.marshaltoString`,
and its `v:` elements were compared with the source's in document order. Every element matched, and
none gained an attribute.

| Attribute | Element | Occurrences | Files | Outcome |
|---|---|---|---|---|
| `w14:anchorId` | `v:shape` | 32 | 4 | **lost** |
| `w14:anchorId` | `v:shapetype` | 13 | 9 | **lost** |
| `w14:anchorId` | `v:rect` | 1 | 1 | **lost** |
| `o:gfxdata` | `v:oval` | 4 | 4 | **lost** |
| `o:gfxdata` | `v:rect` | 2 | 2 | **lost** |
| `o:spt` | `v:shapetype` | 78 | | rewritten: `75` becomes `75.0` |
| `o:gfxdata` | `v:shape` | 45 | | rewritten: the same bytes, base64 on one line where Word wraps at 76 characters |

Counted in the source over the whole repository instead (every `.docx` and `.docm` outside `target/`
and `bin-testOutput/`, 394 files; the review's count, reproduced), the same attributes are a little more
common, from the samples' copies of the `2010/` files and
`docx4j-samples-docx-export-fo/sample-docs/word/textbox_alternates_contrived.docx`: `w14:anchorId` on
`v:shape` 33 (5 files), on `v:shapetype` 13 (9), on `v:rect` 2 (2); `o:gfxdata` on `v:oval` 8 (8), on
`v:rect` 3 (3), on `v:shape` 46 (10).

The first case, from `docx4j-core-tests/src/test/resources/loadAndSave.docx` (the core-ts session's
example; inside a `w:pict` in an `mc:Fallback`):

```
source:  <v:shapetype w14:anchorId="7969C50B" id="_x0000_t202" coordsize="21600,21600" o:spt="202" path="m,l,21600r21600,l21600,xe">
docx4j:  <v:shapetype path="m,l,21600r21600,l21600,xe" o:spt="202.0" coordsize="21600,21600" id="_x0000_t202">
```

CR-021 recorded the same losses on its 37-document gate (2026-09-19): `w14:anchorId` on
`v:shape`/`v:shapetype` (41), `o:gfxdata` on `v:oval`/`v:rect` (6), re-encoded on `v:shape` (61),
`o:spt` as a float (34). It filed them as "a VML round-trip gap" and left them there. This CR closes
the gap.

**Who writes what** (the producers named in the files' `docProps/app.xml`):
- Word 16 writes `w14:anchorId` on the text-box `v:shapetype` of every Fallback (loadAndSave, the
  CR-043 probes, numPicBullet-word365, the extraction probe's Word saves);
- Word 16 writes `w14:anchorId` on the Fallback's `v:shape` when it regenerates the Fallback from the
  Choice: all eight boxes of the extraction probe's Word saves, which hold an `altChunk` (CR-021 §8.5);
- Word 16 writes `w14:anchorId` on a `v:rect` (`2010/DrawingML_GraphicData_wps.docx`);
- Word 12 and 14 write `o:gfxdata` on `v:oval` and `v:rect`, and no `anchorId` (the `2010/*`
  files, Outlook 12's `jaxb-binder-issue.docx`).

The ports see less, for reasons of their own. core-ts's survey of 459 files found `w14:anchorId` only
on `v:shapetype`, four times. objects-ts holds a kept branch as DOM in 0.3.2, so it loses nothing
yet; the loss would appear with its CR-009, which types `w:pict` in kept branches.

## 2. The cause

docx4j's VML schema is ECMA-376's (`xsd/vml/vml-main.xsd`, `vml-officedrawing.xsd`), and no VML type
admits either attribute where Word writes it:
- `w14:anchorId` is declared in `xsd/wml/w14_word_2010_wordml.xsd` (`w:ST_LongHexNumber`) and admitted
  only on `w:pict` and `w:object` (`CT_Picture`, `CT_Object` in `wml.xsd`). No VML type references
  it, and no VML type has an `xsd:anyAttribute`. In docx4j's whole xsd tree, only four files have one:
  shared-customxmlprops, XAdES and the two InkML schemas. So JAXB has nowhere to put the attribute.
- `o:gfxdata` (`xsd:base64Binary`) is admitted only on `v:shape` (`CT_Shape`, `vml-main.xsd` line
  651), as ECMA-376 has it. It binds to `byte[]`, so JAXB writes the bytes back in its own base64
  layout.
- `o:spt` is `xsd:float` (`vml-officedrawing.xsd` line 34), so it binds to `java.lang.Float`, and
  `202` is written back as `202.0`. That is a valid `xsd:float` lexical form, not a loss. Word opened
  such re-saves without complaint in CR-021's Word-open check (2026-09-19).

## 3. What the specifications say, and the departures (recipe step 1)

- **[MS-DOCX] §2.2.6, "Pict and Object Extensions"**, fetched 2026-10-18
  (learn.microsoft.com/en-us/openspecs/office_standards/ms-docx/30a56bd8-c030-41f6-a0f0-0ee19b3c90da),
  extends only `w:object` (ISO/IEC 29500-1 §17.3.3.19) and `w:pict` (ISO/IEC 29500-4 §14.6.1) with
  `anchorId`, and requires its prefix in `mc:Ignorable`: "To maintain compatibility with ISO/IEC-29500
  implementations, the attribute's namespace prefix MUST be specified in an Ignorable attribute". Its
  page "anchorId" (5ce719e2-76a2-4cab-b01f-26f50d6661d4) defines it as an `ST_LongHexNumber` greater
  than 0 and less than 0x80000000.
  - **Departure 1:** Word also writes it on VML shape elements (§1), which [MS-DOCX] does not list. The
    schema is patched to admit what Word writes, and the patch is recorded in the head comment of
    `vml-main.xsd`, as the recipe's step 1 asks for "attributes Office writes that the source omits".
- **ECMA-376 Part 4's `vml-main.xsd`** admits `o:gfxdata` on `v:shape` only.
  - **Departure 2:** Word 12 and 14 also write it on `v:rect` and `v:oval` (§1). It is admitted on the
    other shape types too, recorded the same way.
- Nothing else in the recipe applies. No namespace is new: `w14` and `o` are bound and in the JAXB
  context. There is no new package, no `module-info` change, no new part, and no change to the JAXB
  context lists or the prefix table.

## 4. Proposal

In `xsd/vml/vml-main.xsd`:
1. Declare `xmlns:w14` and import its schema (`../wml/w14_word_2010_wordml.xsd`; the namespace is
   already reached through `wml.xsd`, whose own head notes an import-order quirk to check).
2. Add `<xsd:attribute ref="w14:anchorId" use="optional"/>` to the attribute group
   `AG_AllCoreAttributes` (D1). The group is referenced by exactly the eleven VML shape types and
   nothing else in the xsd tree: `CT_Shape`, `CT_Shapetype`, `CT_Group`, `CT_Rect`, `CT_RoundRect`,
   `CT_Oval`, `CT_Line`, `CT_PolyLine`, `CT_Arc`, `CT_Curve`, `CT_Image`. XJC inlines attribute groups,
   so the generated classes are the same as with eleven references, and the group stays the one
   statement of what every shape carries.

   This is the form `wml.xsd` uses for `CT_Picture` and `CT_Object`: an explicit reference, not a
   wildcard, which the objects-ts session asked for (typed, visible in its declarations). It generates
   `String getAnchorId()`/`setAnchorId(String)`, as `Pict.getAnchorId()` already is.
3. Add `<xsd:attribute ref="o:gfxdata"/>` to the shape types that lack it (D2), one by one: `CT_Rect`,
   `CT_RoundRect`, `CT_Oval`, `CT_Line`, `CT_PolyLine`, `CT_Arc`, `CT_Curve`, `CT_Image`, `CT_Group`
   (`byte[] getGfxdata()`, as `CTShape`). Not through the group: `CT_Shape` references it directly
   already, and a second reference through the group would be a duplicate attribute, a schema error.
4. Record both patches, with this CR's number, in the schema's head comment.
5. Leave `o:spt` as `xsd:float` in the schema (D3).

Regenerate cleanly (`mvn clean install` of `docx4j-generated-objects`). The xjc-copy plugin
regenerates the deep copies, and the parent-pointer plugin is unaffected. The hand-written interfaces
`VmlAllCoreAttributes` and `VmlAllShapeAttributes` are left alone (D4). Both selector modules, the
reference implementation and MOXy, read the same generated classes, and docx4j-JAXB-MOXy has no
hand-kept VML bindings, so the regeneration covers it.

**Saved namespaces.** [MS-DOCX] requires the `w14` prefix to be listed in `mc:Ignorable`. Since CR-023,
every root keeps Word's `mc:Ignorable` list, and Word's parts carrying these shapes list `w14`. The
test asserts that a re-saved part with a VML `w14:anchorId` declares `w14` and lists it in
`mc:Ignorable`, in `numbering.xml` (numPicBullet) as well as `document.xml`.

## 5. The phase

**Build:** §4, one commit (recipe step 12).

**Tests** (docx4j-core-tests):
- `VmlAttributesRoundTripTest`: for each fixture of §1, load, re-marshal every WordprocessingML part
  holding VML, and compare every `v:` element's attributes with the source's in order. No attribute is
  lost, and `o:gfxdata` keeps its bytes. The rewrites D3 accepts are listed, so that a new one fails.
- `loadAndSave.docx`'s `v:shapetype` keeps `w14:anchorId="7969C50B"`. The probe's Word save keeps all
  eight `v:shape` `anchorId`s, and `DrawingML_GraphicData_wps.docx` keeps its `v:rect`'s `anchorId`
  and `o:gfxdata`.
- The namespace assertion of §4. core-tests runs the reference implementation; the round-trip and
  namespace tests are also run once by hand with the MOXy selector on the classpath, as CR-046 phase 1
  was, and the result recorded here.
- The existing suites (core-tests, export-fo-tests) pass: FO's VML text boxes and pictures read these
  types.

**Word-open check** (Jason): docx4j's re-saves of `loadAndSave.docx`, the extraction probe's Word save
and `2010/2010-mcAlternateContent.docx` open without a repair prompt, and Word's own save of each keeps
the `anchorId`s and `o:gfxdata`. One open of each; no layout measurement.

**Hand-offs**, with the commit hash:
- docx4j-generated-objects-ts regenerates from the xsd and adds the case to its fidelity expectations,
  as it did for CR-040; its CR-009 names this CR as the dependency of its proposal 2, the gate for
  typing `w:pict` in kept branches.
- docx4j-core-ts: its CR-010 survey's four losses close once objects-ts regenerates.
- docx4j-python: unaffected; it keeps VML attributes as raw strings.
- The portfolio registry, through the portfolio session: a `docx4j/CR-048` entry (recipe step 0
  asks for it before review), and `objects-ts/CR-009` depending on it.

**Acceptance:** the tests above, the existing suites, and the Word-open check.

**Implementation, 18 October 2026:**
- `xsd/vml/vml-main.xsd`: `xmlns:w14` and its import (placed before the other imports; the clean
  regeneration resolved it with no complaint), `w14:anchorId` in `AG_AllCoreAttributes`, `o:gfxdata` on
  the nine types, and the head comment. Twenty inserted lines, LF; the file's CRLF lines untouched.
- The regeneration gives all eleven VML shape classes `String getAnchorId()`/`setAnchorId`, and the nine
  `byte[] getGfxdata()`/`setGfxdata`. Compared member by member (`javap -p`, every class) with the
  17.3.3 jar, from which the xsd had not changed: those 60 members are the only difference, and nothing
  is removed. No hand-written code needed changing.
- `VmlAttributesRoundTripTest` (docx4j-core-tests):
  - `wordsVmlAttributesSurvive`: every fixture with VML, 1,839 elements; no attribute lost; `o:spt` and
    `o:gfxdata`'s layout the only differences, and their values equal. Run with the 17.3.3 generated
    classes in place of the new ones, it reports 52 problems: the 46 `anchorId`s and the 6 `o:gfxdata`s
    of §1.
  - `namedCases`: loadAndSave's `7969C50B`; the probe's Word save keeps its nine (eight regenerated
    `v:shape`, one `v:shapetype`); the wps `v:rect` keeps both attributes.
  - `savedPartsListW14Ignorable`: the saved `document.xml` (loadAndSave) and `numbering.xml`
    (numPicBullet-word365) declare `w14`, list it in `mc:Ignorable`, and keep the attribute.
  - The class passes on the reference implementation in the suite, and once by hand with the MOXy
    selector on the classpath (`Context.getJaxbImplementation()` = `ECLIPSELINK_MOXy`).
- The existing suites pass with the regenerated classes (18 October 2026): docx4j-core-tests 1,690
  run, 0 failures, 11 skipped; docx4j-export-fo-tests 852 run, 0 failures, 9 skipped.
- The Word-open check's inputs are on the share in `cr048/` with a `RUN.txt`: docx4j's re-saves of
  loadAndSave, the probe's Word save, `2010-mcAlternateContent` and `DrawingML_GraphicData_wps`.

**Word-open check (Jason, 18 October 2026, Word 16): passed.** All four re-saves opened; no repair prompt
was reported. Word's unedited saves of three of them keep every VML `w14:anchorId`, with the same
values, and every `o:gfxdata`:

| docx4j re-save | `w14:anchorId`, docx4j / Word (values equal) | `o:gfxdata`, docx4j / Word |
|---|---|---|
| loadAndSave (`v:shapetype`) | 1 / 1 | 1 / 1 |
| the probe's Word save, `document.xml` (8 `v:shape`, 1 `v:shapetype`) | 9 / 9 | 9 / 9 |
| the probe's Word save, `header2.xml` | 1 / 1 | 1 / 1 |
| `DrawingML_GraphicData_wps` (`v:rect`) | 1 / 1 | 1 / 1 |

Word could not save the fourth, `2010-mcAlternateContent`, in its own format, and that is the fixture,
not this CR. The file is a Word 2007 document (empty `w:compat`, `AppVersion` 12.0000) holding a Word 2010
DrawingML shape in a comment. Saving it, Word's Compatibility Checker said shapes and text boxes would
be converted; on Continue, it refused with "You can't put drawing objects into a text box, callout,
comment, footnote, or endnote". docx4j's re-save has the same `comments.xml` as the original, element for
element (60 elements, same order), so the original behaves the same. So Word's handling of
`o:gfxdata` on a `v:oval` is not observed. docx4j's keeping it is pinned by `VmlAttributesRoundTripTest`,
and `o:gfxdata` beyond `v:shape` is observed through Word on the wps `v:rect`.

## 6. Decisions

**Decided 2026-10-18: Jason accepted every recommendation below** ("i accept the recommendations
provided for decisions D1 to D5"). Each item is kept as proposed, recommendation included, as the record.

- **D1. Which VML types admit `w14:anchorId`.** Options:
  - the three Word has been seen writing it on (`v:shapetype`, `v:shape`, `v:rect`);
  - every VML shape type and `v:group`.

  Word regenerates a Fallback from the Choice's preset geometry: `v:rect` for a rectangle, `v:shape`
  for a text box. An ellipse or rounded rectangle would plausibly get `v:oval` or `v:roundrect` with
  the same attribute, though no file here shows it. Recommended: every shape type and `v:group`; it is
  optional and costs nothing where it is absent. Declared once, in `AG_AllCoreAttributes` (§4 item 2;
  the review's suggestion), rather than on each type: the group's users are exactly this set, and a
  shape type added to it later inherits the attribute. **The objects-ts session agrees** (2026-10-18): an
  optional declared attribute costs it nothing, the set Word has been seen writing it on keeps
  growing (the regenerated `v:shape` was found only in docx4j's fixtures), and the broad set saves
  it a second regeneration. The cross-namespace reference is already proven through its pipeline:
  `Pict` and `CTObject` bind `w14:anchorId` from `wml.xsd`'s same form.
- **D2. `o:gfxdata` on the other shape types.** Recommended: yes (the objects-ts session agrees: the
  same kind of loss, with the same cheap fix). The attribute is the shape's
  DrawingML, zipped, which Word reads back from VML. Losing it from a `v:rect` or `v:oval` loses what
  Word would restore the shape from, where the VML is all that is left.
- **D3. `o:spt` and `o:gfxdata`'s layout.** Both are lexical rewrites of valid values, and Word reads
  them. Options:
  - leave them;
  - a Java-side print rule (an `XmlAdapter` writing an integral float without `.0`), kept out of the
    xsd by an external bindings file.

  The objects-ts session asks that the xsd type of `o:spt` stay `xsd:float`: a string would change its
  declarations for nothing. Recommended: leave both. The test lists them as known rewrites. Revisit if
  diff or anonymiser output (`docx4j-diffx`, `org.docx4j.anon`) needs byte-stable VML.
- **D4. The hand-written VML interfaces.** `VmlAllCoreAttributes` could gain
  `getAnchorId`/`setAnchorId` so that callers need not cast per type. Recommended: not in this CR; no
  caller needs it yet.
- **D5. An `xsd:anyAttribute` on the VML attribute groups**, as a net for attributes nobody has seen
  yet. Recommended: no. No evidence calls for it; JAXB's `Map<QName, String>` is a weaker API, and it
  would change every VML type's declarations in the ports.

## 7. Risks

- **Import order in the xsd.** `wml.xsd`'s head records that a misplaced `w14` import once failed to
  resolve `w14:anchorId`. The import goes before the other imports, and the clean regeneration is the
  check.
- **The ports' declarations change.** objects-ts regenerates from this xsd, so its VML types gain
  `anchorId` (and `gfxdata`) where D1 and D2 put them. That is the purpose; the hand-off says so.
- **A part whose root does not list `w14` in `mc:Ignorable`.** A producer that writes the attribute
  without listing its prefix is not conformant to [MS-DOCX] §2.2.6. docx4j would then declare `w14`
  without listing it, as it does today for `w:pict`'s `anchorId`. §4's test covers Word's form only.

## 8. Sources

- [MS-DOCX] §2.2.6 and "anchorId" on learn.microsoft.com (fetched 2026-10-18; pages updated
  2024-04-16).
- ECMA-376 Part 4, `vml-main.xsd` and `vml-officedrawing.xsd` (in `xsd/vml/`).
- CR-021 (its phase 2 gate, the VML round-trip gap; §8.5, Word's regenerated Fallbacks); CR-023
  (Word's extension attributes on WordprocessingML types, the precedent).
- docx4j-core-ts CR-010 §10.2 (328439e); docx4j-generated-objects-ts CR-009 §8 (91196dd; 0ad1487
  names this CR) and its session's answers of 2026-10-18.
- The review of 2026-10-18 by Claude Fable 5.1 (session docx4j-44).
