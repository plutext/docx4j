# CR-022: Excel 2010 and 2013 extensions - the x14 and x15 schemas and their mc namespaces, bound in xlsx4j

Status: PROPOSED 2026-09-20 (Jason Harrop: "add support for Excel 2010 and
2013 extensions", after the [MS-XLSX] inventory of
`docs/developer/ms-xlsx-schema-inventory.md`). Written by
`docs/developer/adding-a-schema.md` step 0: to be reviewed and committed
before any code. Drafted with Claude Fable 5.1. Owner: Jason Harrop.

Scope: bind the five namespaces every Excel-saved workbook carries and docx4j
knows only by prefix - [MS-XLSX] §5.4 `x14`, §5.3 `x15`, §5.5 `x14ac`, §5.9
`x15ac`, §5.6 `x12ac` - with the small schemas their current text imports
(§5.10 `x16`, and the revision attribute schemas §5.15 `xr`, §5.16 `xr2`,
§5.12 `xr10`); admit in the main SpreadsheetML schema the attributes and
`mc:Ignorable` roots those extensions ride on; and add the parts they define
(slicer caches and slicers, timeline caches and timelines, control properties,
custom data properties, survey, the binary data model). Not in scope: the
drawing-part shapes of slicers and timelines (§5.2, §5.7), the revision
*elements* (`xr:revisionPtr`, the inventory's second group), and everything
from 2016 on.

## 1. The schemas and a file exercising them (recipe step 1)

**Source**: [MS-XLSX] revision 29.1 (2026-05-19), from the HTML section pages
of the appendix on learn.microsoft.com (the recipe's preferred source; each
page cited in the xsd header):

| § | prefix | namespace | what it binds | size (current) |
|---|---|---|---|---|
| 5.4 | x14 | spreadsheetml/2009/9/main | Excel 2010: sparklines, slicers and slicer caches, conditional-formatting and data-validation extensions, protected ranges, ignored errors, pivot and table extensions, form-control properties, `datastoreItem` | 918 lines, 32 global elements, 85 types |
| 5.3 | x15 | spreadsheetml/2010/11/main | Excel 2013: the data model and its connections, timelines and their caches, table slicer caches, pivot filters and calculated members, web extensions, surveys, `workbookPr`, `timelineStyles` | 487 lines (2013 copy), 81 global elements |
| 5.5 | x14ac | spreadsheetml/2009/9/ac | two attributes: `dyDescent` (rows), `knownFonts` (the font table) | 2 attributes |
| 5.9 | x15ac | spreadsheetml/2010/11/ac | `absPath` on the workbook (inside an `mc:AlternateContent`, kept since CR-021) | 1 element |
| 5.6 | x12ac | spreadsheetml/2011/1/ac | `list`, a data validation's list inside an `mc:AlternateContent` Choice | 1 element |
| 5.10 | x16 | spreadsheetml/2014/11/main | `modelTimeGroupings` (data-model pivot time grouping); imported by x15 | 3 types |
| 5.15 | xr | spreadsheetml/2014/revision | the `uid` attribute (already `xsd/sml/sml_2014_revision.xsd`); imported by x14 | attribute |
| 5.16 | xr2 | spreadsheetml/2015/revision2 | the `uid` attribute; imported by x14 | attribute |
| 5.12 | xr10 | spreadsheetml/2016/revision10 | the `uid` attribute; imported by x14 and x15 | attribute |

The tree already holds 2013-07-26 copies of the first seven (`xsd/xlsx/`,
commit d12fbee47, JAXB-annotated in b45e62afe) that nothing imports. The
current x14 text differs from the 2013 copy by its imports of the three
revision schemas (attribute references on its elements) and otherwise
matches it in size; x15's has grown (the 2013 copy is 487 lines). Phase 1
re-takes all of them from the current revision rather than patching the
copies.

**Occurrence**, measured on the repository's seven Excel-saved workbooks
(2026-09-20): `x14ac:dyDescent` on 183 rows and `x14ac:knownFonts` on five
font tables; `x14:workbookPr`, `x15:workbookPr`, `x14:slicerStyles`,
`x15:timelineStyles` and `x14:table` as `extLst` content; every workbook
declares all five prefixes. None of the sample workbooks has a slicer,
timeline, sparkline, extended conditional format or form control, so **the
file this CR needs does not exist in the repository**: Jason to save one from
Excel 365 with a sparkline group, a table with a slicer, a pivot table with a
slicer and a timeline, a data validation with a list, a 2010-style conditional
format (a data bar with a negative-value colour, an icon set), a form control
(a checkbox, so a control-properties part), and, if Power Pivot is available,
a data model - no personal data, small, committable to
`docx4j-core-tests/src/test/resources/`. Phase 0 measures that file (parents,
`ext` uris, `mc:Ignorable` roots, `mc:AlternateContent` parents) before phase
1 admits anything.

## 2. Where they join the tree, and their dependencies (recipe step 2)

`xsd/sml/sml_root.xsd` gains imports of the new schemas, which live in
`xsd/xlsx/` (the directory that has held them, refreshed; **departure**: the
recipe's "under the tree it belongs to" would be `xsd/sml/`, but `xsd/xlsx/`
already exists for exactly these files and moving them adds nothing).

Dependencies, from the current schemas' imports:

- x14 and x15 import each other (x14 uses x15's slicer-cache types, x15 uses
  x14's) - they come as a pair;
- both import the main SpreadsheetML schema (the source's `xlbasictypes.xsd`,
  `xlsheet.xsd`, `xlpivot.xsd`, `xlworkbook.xsd`, `xlsupbook.xsd`,
  `xlsst.xsd`, `xlextconns.xsd` all map to
  `../sml/sml_ECMA376_4ed_transitional.xsd`), the relationships schema
  (`orel.xsd` to `../relationships.xsd`) and `xm`
  (`excel/2006/main`, bound already: `../offmacro/office-excel-2006-main.xsd`);
- x14 imports xr, xr2 and xr10 (`xlrevexignore*.xsd`); x15 imports x16
  (`xlpivot16.xsd`) and xr10. xr exists (`sml_2014_revision.xsd`, `uid`
  only); xr2, xr10 and x16 are new small files;
- the ac schemas import only the main schema and relationships.

No import crosses a JAXB context boundary: everything resolves inside
`jcSML` (the main schema, relationships, `xm`). No `xsd:any` substitution is
needed.

**The host side** - what the main schema must admit for the extensions to
survive a load and a save, measured (CR-021 §8.8 and the core-ts Excel
acceptance run):

- `mc:Ignorable` on the roots Excel writes it on: `CT_Worksheet`,
  `CT_Stylesheet`, `CT_Table`, `CT_Comments` (measured: `worksheet`
  "x14ac xr xr2 xr3", `styleSheet` "x14ac x16r2 xr", `table` "xr xr3",
  `comments` "xr"); `CT_Workbook` has it. Without it the attribute is dropped
  and, once the x14ac attributes are typed, a saved worksheet would carry
  them undeclared as ignorable.
- `x14ac:dyDescent` on `CT_Row`, `x14ac:knownFonts` on `CT_Fonts`: explicit
  attribute references, so they are typed properties (not an
  `anyAttribute`).
- `mc:AlternateContent` in `CT_DataValidation` for `x12ac:list` (Excel writes
  the validation's `formula1` twice, in a Choice requiring x12ac with the
  `list` element and in a Fallback), and `x:dataValidation` on the
  preprocessor's retain list - **to be confirmed on the phase 0 file** before
  it is admitted, per CR-021's rule (admit what Office writes).
- `extLst` content needs no schema change: `CT_Extension` is a lax wildcard,
  and once the x14/x15 global elements exist in `jcSML`, JAXB unmarshals
  `x14:sparklineGroups` and the rest typed where it now keeps DOM. That is
  the one behaviour change a caller can notice (§7).

## 3. The xsd annotations (recipe step 3)

Each file: the JAXB namespaces, `jaxb:version="3.0"`, the package annotation,
`annox` root-element annotations on the part roots
(`CT_SlicerCacheDefinition`, `CT_Slicers`, `CT_TimelineCacheDefinition`,
`CT_Timelines`, `CT_FormControlPr`, `CT_DatastoreItem`, `CT_Survey`), and
`mc:Ignorable` on those roots if the phase 0 file shows Excel writes it there.

**Packages** (departure from the 2013 copies' annotations,
`org.xlsx4j.spreadsheetml.main200909` and the like, which were never
generated so nothing depends on them): the tree's newer convention, that of
`sml_2014_revision.xsd`, `org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.<path>`
with a digit-leading segment prefixed `x`:

| namespace | package |
|---|---|
| spreadsheetml/2009/9/main | `org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main` |
| spreadsheetml/2009/9/ac | `...spreadsheetml.x2009.x9.ac` |
| spreadsheetml/2010/11/main | `...spreadsheetml.x2010.x11.main` |
| spreadsheetml/2010/11/ac | `...spreadsheetml.x2010.x11.ac` |
| spreadsheetml/2011/1/ac | `...spreadsheetml.x2011.x1.ac` |
| spreadsheetml/2014/11/main | `...spreadsheetml.x2014.x11.main` |
| spreadsheetml/2015/revision2 | `...spreadsheetml.x2015.revision2` |
| spreadsheetml/2016/revision10 | `...spreadsheetml.x2016.revision10` |

(Jason to confirm; the older `org.xlsx4j.schemas.microsoft.com.office.excel...`
form of the three bound packages is the alternative.)

**A source defect to patch, recorded in the header**: the x12ac schema's
default namespace is `xmlns="xl12AcSchemaUri"` in the specification (a
placeholder never replaced), while its `targetNamespace` is right; the copy
sets both to the namespace. The 2013 copy already did.

## 4 to 6. Regeneration, module-info, contexts (recipe steps 4 to 6)

`mvn clean install -pl docx4j-generated-objects,docx4j-core`; eight
`exports` and `opens` pairs in the generated-objects `module-info.java`; the
eight packages appended to `org.xlsx4j.jaxb.Context.jcSML`'s list (not the
Word or PresentationML contexts - nothing there holds this content).

## 7. The prefix table (recipe step 7)

Present already, both ways: x14, x14ac, x15, x15ac, xr, xr2, xr3, xr6, xr10.
To add, beside them: `x12ac` (spreadsheetml/2011/1/ac) and `x16`
(spreadsheetml/2014/11/main), which is `x16` in Excel's own output. objects-ts
and the Python port keep copies (hand-off, §11).

## 8 and 9. Parts and their registration (recipe steps 8 and 9)

From [MS-XLSX] §2.1, each a `JaxbSmlPart<T>` in
`openpackaging/parts/SpreadsheetML/` beside the pivot parts, with its
constants in `ContentTypes` and `Namespaces` and its `else if` low in
`ContentTypeManager`'s chain next to the pivot cache parts:

| part | content type | relationship type | root | from |
|---|---|---|---|---|
| SlicerCachePart | `application/vnd.ms-excel.slicerCache+xml` | `.../office/2007/relationships/slicerCache` | `x14:slicerCacheDefinition` | workbook |
| SlicersPart | `application/vnd.ms-excel.slicer+xml` | `.../office/2007/relationships/slicer` | `x14:slicers` | worksheet |
| TimelineCachePart | `application/vnd.ms-excel.TimelineCache+xml` | `.../office/2010/relationships/TimelineCache` | `x15:timelineCacheDefinition` | workbook |
| TimelinesPart | `application/vnd.ms-excel.Timeline+xml` | `.../office/2010/relationships/Timeline` | `x15:timelines` | worksheet |
| ControlPropertiesPart | `application/vnd.ms-excel.controlproperties+xml` | `.../officeDocument/2006/relationships/ctrlProp` | `x14:formControlPr` | a worksheet's control |
| CustomDataPropertiesPart | `application/vnd.openxmlformats-officedocument.customDataProperties+xml` | `.../officeDocument/2006/relationships/customDataProps` | `x14:datastoreItem` | workbook |
| CustomDataPart (binary) | `application/binary` | `.../officeDocument/2006/relationships/customData` | add-in data | custom data properties |
| SurveyPart | `application/vnd.ms-excel.Survey+xml` | `.../office/2010/relationships/Survey` | `x15:survey` | workbook |
| DataModelPart (binary) | `application/vnd.openxmlformats-officedocument.model+data` | [MS-XLDM]'s | binary ([MS-XLDM]) | workbook |

Today each of these loads as a `DefaultXmlPart` (or a binary part) and
round-trips as bytes; the change is that they become typed. The two binary
parts are `BinaryPart`s and need no schema; the data model's relationship
type is taken from [MS-XLDM] in phase 0.

## 10. Tests (recipe step 10)

In `docx4j-core-tests`, on the phase 0 file: every part above typed and
reachable by relationship type; the worksheet's `x14:sparklineGroups`,
`x14:conditionalFormattings`, `x14:dataValidations` and the workbook's
`x15:timelineCacheRefs` typed inside `extLst`; `x14ac:dyDescent` read from a
row and `knownFonts` from the font table; a save and a reload keeping them
all, with every `mc:Ignorable` prefix declared on each root; the seven
existing sample workbooks loading and saving as before (`LoadAndSaveTests`);
and the re-saved file opening in Excel 365 without a repair prompt, slicers
and timelines working (Jason, from the share). The whole `docx4j-core-tests`
suite; `docx4j-export-fo-tests` is unaffected (no docx change) but is run
once as the MOXy/RI context check.

## 11. Follow-through and hand-offs (recipe step 11)

CHANGELOG under "Schema" (17.1.1 or the next release Jason names); this CR
updated per phase; objects-ts told the xsd files, packages and the two prefix
entries (it regenerates once from the final commit); the Python port told
(its phase D, PML/SML generation, is when it takes them; the file list and
prefix entries go into its hand-off now); core-ts told, since its Excel
acceptance run is where the x14ac and Ignorable losses were seen.

## 12. Phases and gates

0. **The file and its measurement** (Jason's Excel 365 workbook; the survey
   of its parts, `ext` uris, `mc:Ignorable` roots and `mc:AlternateContent`
   parents; the data model relationship type). No code. Gate: the list of
   host admissions of §2 confirmed against the file.
1. **The schemas, host admissions, regeneration, contexts, prefixes**: the
   nine xsd files taken from the current revision, `sml_root.xsd` importing
   them, the main schema's `mc:Ignorable` roots and the two x14ac attribute
   references (and `CT_DataValidation`'s `mc:AlternateContent` if confirmed),
   module-info, `jcSML`, the two prefix entries. Gate: the seven sample
   workbooks and the phase 0 file load, the `extLst` content and attributes
   typed and round-tripping, `mc:Ignorable` complete on every root, core
   suite green, Excel opens the re-save.
2. **The parts**: the nine part classes, constants and registrations, with
   the typed-part tests. Gate: the parts typed and reachable, Excel opens the
   re-save with slicers and timelines working.
3. **Follow-through**: CHANGELOG, this CR closed, the three hand-offs. Phases
   1 and 2 are one commit each (recipe step 12; phase 0 commits only the
   test file and this CR's update).

## 13. Risks and departures, in one place

- **`extLst` content becomes typed** (§2): code that reads an `ext`'s child
  as a DOM `Element` today - none found in docx4j-core's xlsx4j code by grep,
  to be re-checked in phase 1 - would see a JAXB object instead. Recorded in
  the CHANGELOG.
- **The 2013 copies are stale**: re-taken, not patched (§1).
- **Package naming** (§3): the newer convention; Jason to confirm.
- **`xsd/xlsx/` kept** as the home (§2).
- **x12ac's default namespace** patched (§3).
- **Excluded**: the slicer and timeline drawing shapes (§5.2, §5.7: their
  content sits in a drawing's `graphicData` wildcard and round-trips as DOM
  today; a DrawingML CR), `xr:revisionPtr` and the revision elements (the
  inventory's second group), the 2016+ pivot and rich-data schemas.
- **MOXy**: `jcSML` grows by eight packages; the MOXy selector module is
  exercised by the core suite's context check.

## 14. Effort (rough)

Phase 0 an hour once the file exists; phase 1 a day (the schema copies and
the regeneration round dominate); phase 2 a day; phase 3 hours.

## 15. References

- [MS-XLSX] 29.1: §2.1.1 to §2.1.9 (parts), §2.2.4, §5.3, §5.4, §5.5, §5.6,
  §5.9, §5.10, §5.12, §5.15, §5.16 (schemas); [MS-XLDM] (the data model).
- `docs/developer/adding-a-schema.md` (the recipe),
  `docs/developer/ms-xlsx-schema-inventory.md` (the 45-namespace inventory),
  CR-018 (the w16cex precedent), CR-021 (`mc:AlternateContent`; §8.8 the
  workbook, §8.6 item 10 the kept-branch rule).
- `xsd/xlsx/` (the 2013 copies), `xsd/sml/sml_root.xsd`,
  `org.xlsx4j.jaxb.Context`, `NamespacePrefixMappings`,
  `openpackaging/parts/SpreadsheetML/JaxbSmlPart`.
