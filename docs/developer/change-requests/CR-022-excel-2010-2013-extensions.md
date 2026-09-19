# CR-022: Excel 2010 and 2013 extensions - the x14 and x15 schemas and their mc namespaces, bound in xlsx4j

Status: IN PROGRESS - proposed 2026-09-20 (Jason Harrop: "add support for
Excel 2010 and 2013 extensions", after the [MS-XLSX] inventory of
`docs/developer/ms-xlsx-schema-inventory.md`), accepted by Jason the same day
(package convention confirmed, "let's commence"); phase 0 DONE 2026-09-20
(§16); phase 1 in progress. Written by `docs/developer/adding-a-schema.md`
step 0. Drafted with Claude Fable 5.1. Owner: Jason Harrop.

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
timeline, sparkline, extended conditional format or form control, so the
files this CR needs were looked for on the web (2026-09-20, the open-source
test corpora: Apache POI, LibreOffice, ClosedXML, openxlsx2, excelize,
Open-XML-SDK, Open-Xml-PowerTools; SheetJS's is disabled) and measured after
download. Excel-saved files exist for everything but slicers and timelines:

| file (staged on the share, `fidelity/cr022/`) | source, licence | saved by | carries |
|---|---|---|---|
| `Sparklines.xlsx` | LibreOffice `sc/qa/unit/data/xlsx`, MPL-2.0 | Excel 16 | three `x14:sparklineGroups`, `x14ac` attributes, the `xr*` prefixes, `xcalcf` |
| `complex_icon_set.xlsx` | LibreOffice | Excel 15 | two `x14:conditionalFormattings` (icon sets) |
| `condformat_databar.xlsx` | LibreOffice | Excel 15 | `x14:conditionalFormattings` (a data bar) and `x14:id` on the base rule |
| `data_validation_test.xlsx` | LibreOffice | Excel 15 | four `x14:dataValidations` of type list |
| `checkbox-form-control.xlsx` | LibreOffice | Excel 16 | a control-properties part (`xl/ctrlProps/ctrlProp1.xml`), and three `mc:AlternateContent` parents new to the survey: `worksheet` (Choice `x14` holding `controls`), `controls` (Choice `x14` holding `control`), and the drawing's `wsDr` (Choice `a14` holding `twoCellAnchor`) |
| `tdf167689_x15_namespace.xlsx` (174 KB) | LibreOffice | Excel 16 | a data model part (`xl/model/item.data`), `x15:dataModel`, two `x15:connection`, `xr16` on connections and query tables |

Every one of these is Excel-saved (the `Application` of `docProps/app.xml`);
LibreOffice's own `databar.xlsx` was not (LibreOffice 4.1) and is not staged.
Two candidates were retired on 2026-09-20 once the slicer workbook existed:
POI's `ConditionalFormattingSamples.xlsx` (655 KB; measured, its eighteen
sheets hold only 2006-style conditional formats, no `x14` rule) and
LibreOffice's `invalid_ext_data_validation.xlsx` (a subset of
`data_validation_test.xlsx`). `Sparklines.xlsx` stays: its groups are column
and stacked with negative points and markers, where the slicer workbook's one
group is a default line. **Decision (Jason, 2026-09-20): the LibreOffice
files are used temporarily** - from the share, during phases 0 to 2, as the
Excel-saved evidence the measurements and the development tests run against -
**and are not committed**. Once the new code exists, docx4j generates its own
sample workbooks through it (a sparkline group of each kind, an icon-set and
a data-bar rule, a list validation, a checkbox control, a data model where
docx4j can write one), each opened and re-saved by Excel 365 on the share to
become the committed, Excel-saved test resource; the LibreOffice files are
then discarded. `cr022-slicers-timelines.xlsx` is committed as it is (Jason's
own, Excel-saved). Generating those samples is the creation half of the gate:
a feature docx4j can only read is not "supported".

**Slicers and timelines were not found** in any open corpus (LibreOffice and
POI do not support them, so their test files have none; the libraries that do
read them keep no fixture). One workbook from Excel 365 is still needed, and
this is how to make it (Excel 365, Windows; ten minutes):

1. **Data**: on `Sheet1`, type a header row `Date, Region, Product, Sales`
   and a dozen rows of made-up data with dates across several months and
   two or three regions and products. Select it and *Insert > Table* (tick
   "My table has headers").
2. **Table slicer**: with a cell of the table selected, *Table Design >
   Insert Slicer*, tick `Region`, OK. (A `slicerCache` part for the table
   and a `slicers` part for the sheet: x14 with x15's `tableSlicerCache`.)
3. **Pivot table**: *Insert > PivotTable > From Table/Range*, new worksheet,
   OK; drag `Product` to Rows, `Sales` to Values, `Date` to Columns (Excel
   groups it by month).
4. **Pivot slicer**: with the pivot selected, *PivotTable Analyze > Insert
   Slicer*, tick `Region`, OK. (A second `slicerCache`, pivot-sourced.)
5. **Timeline**: *PivotTable Analyze > Insert Timeline*, tick `Date`, OK;
   drag a range of months in it. (A `timelineCache` part for the workbook and
   a `timelines` part for the sheet: x15.)
6. **Sparkline** (optional, covered above): back on `Sheet1`, select an
   empty cell beside a row, *Insert > Sparklines > Line*, data range that
   row's numbers.
7. *File > Save As*, `.xlsx`, a name like `cr022-slicers-timelines.xlsx`,
   to the share's `fidelity/cr022/` folder. No personal data: the made-up
   rows only, and *File > Info > Check for Issues > Inspect Document* to
   remove document properties if you like.

**Done, 2026-09-20**: `cr022-slicers-timelines.xlsx` (Excel 365, 16.0300;
its table sheet was built with docx4j and opened in Excel without repair, a
creation check in passing) is staged beside them. Measured: a table slicer
and a pivot slicer (two `slicerCache` parts, two `slicers` parts), a timeline
(`timelineCache` and `timelines` parts), a pivot table with its cache, and a
sparkline group; `extLst` content `x14:slicerCaches`, `x15:slicerCaches`,
`x15:timelineCacheRefs`, `x14:workbookPr` (workbook), `x14:slicerList`,
`x15:timelineRefs`, `x14:sparklineGroups` (sheets), `x15:tableSlicerCache`
(the table's slicer cache), `x14:pivotTableDefinition`, `x15:pivotFilter`,
`xpdl:pivotTableDefinition16` (pivot tables), `x14:pivotCacheDefinition`;
`mc:Ignorable` on every slicer, timeline and cache root is `"x xr10"` (the
main SpreadsheetML namespace itself declared ignorable - a curiosity, and a
reason those roots need the attribute); and in the two drawings three
`mc:AlternateContent` on `twoCellAnchor` with Choices requiring `a14`,
`tsle` and `sle15` (the slicer and timeline shapes; `sle15` is a
drawing/2012/slicer namespace not in [MS-XLSX]'s appendix - [MS-ODRAWXML]'s),
which confirms leaving the drawing shapes to a DrawingML CR.

Phase 0 measures the staged files and that one (parents, `ext` uris,
`mc:Ignorable` roots, `mc:AlternateContent` parents) before phase 1 admits
anything. The checkbox file's three `mc:AlternateContent` parents already
extend §2's host list: `CT_Worksheet` (a `controls` Choice), the `controls`
element itself, and the spreadsheet drawing's `wsDr` (a DrawingML CR's, not
this one's, unless the phase 0 survey shows it is common).

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
| TimelineCachePart | `application/vnd.ms-excel.timelineCache+xml` | `.../office/2011/relationships/timelineCache` | `x15:timelineCacheDefinition` | workbook |
| TimelinesPart | `application/vnd.ms-excel.timeline+xml` | `.../office/2011/relationships/timeline` | `x15:timelines` | worksheet |
| ControlPropertiesPart | `application/vnd.ms-excel.controlproperties+xml` | `.../officeDocument/2006/relationships/ctrlProp` | `x14:formControlPr` | a worksheet's control |
| CustomDataPropertiesPart | `application/vnd.openxmlformats-officedocument.customDataProperties+xml` | `.../officeDocument/2006/relationships/customDataProps` | `x14:datastoreItem` | workbook |
| CustomDataPart (binary) | `application/binary` | `.../officeDocument/2006/relationships/customData` | add-in data | custom data properties |
| SurveyPart | `application/vnd.ms-excel.Survey+xml` | `.../office/2010/relationships/Survey` | `x15:survey` | workbook |
| DataModelPart (binary) | `application/vnd.openxmlformats-officedocument.model+data` | [MS-XLDM]'s | binary ([MS-XLDM]) | workbook |

**Excel's strings, not the specification's pages, for the timeline
parts**: [MS-XLSX] §2.1.7 and §2.1.8 print `application
/vnd.ms-excel.TimelineCache+xml`, `.../office/2010/relationships/TimelineCache`
and `Timeline`; Excel 365 writes `timelineCache+xml`, `timeline+xml` and
`.../office/2011/relationships/timelineCache`, `.../timeline` (measured on
`cr022-slicers-timelines.xlsx`, 2026-09-20). The constants take Excel's
form; matching content types case-insensitively is not something
`ContentTypeManager` does, so the spec's form is not registered.

Today each of these loads as a `DefaultXmlPart` (or a binary part) and
round-trips as bytes; the change is that they become typed. The two binary
parts are `BinaryPart`s and need no schema; the data model's relationship
type is taken from [MS-XLDM] in phase 0.

## 10. Tests (recipe step 10)

In `docx4j-core-tests`, on `cr022-slicers-timelines.xlsx` and on the
docx4j-generated samples of §1 (the LibreOffice files serve the same tests
during development, from the share, until the generated samples replace
them): every part above typed and
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
2b. **docx4j's own samples**: a generator (a sample program under
   `docx4j-samples-xlsx4j`, kept as the worked example of the API) writes
   the workbooks of §1 through the new code; Jason opens and re-saves each in
   Excel 365; the re-saves replace the LibreOffice files as test resources
   and the tests of §10 run on them. Gate: Excel opens every generated
   workbook without repair and shows the feature; the LibreOffice files
   discarded from the share.
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

## 16. Phase 0: the measurement (2026-09-20)

The seven staged workbooks (Jason's `cr022-slicers-timelines.xlsx` and the
six LibreOffice ones of §1), every `xl/` part parsed. What the schemas must
admit, confirmed against files rather than the specification's prose:

**`mc:Ignorable` roots** (part, value): `workbook` "x15 xr xr6 xr10 xr2" or
"x15"; `worksheet` "x14ac xr xr2 xr3" or "x14ac"; `styleSheet` "x14ac x16r2
xr" or "x14ac"; `table` "xr xr3"; `pivotTableDefinition`,
`pivotCacheDefinition`, `pivotCacheRecords` "xr"; `connections`,
`queryTable` "xr16"; `x14:slicers`, `x14:slicerCacheDefinition`,
`x15:timelines` "x xr10"; `x15:timelineCacheDefinition` "xr10". Only
`CT_Workbook` has the attribute today; the other eleven types get it. `xr16`
(spreadsheetml/2017/revision16) is not in the appendix and is added to the
prefix table only, so the declaration survives a save.

**Extension attributes** (host, attribute, count): `x:row` `x14ac:dyDescent`
114; `x:sheetFormatPr` `x14ac:dyDescent` 13 (not in §2's list - added);
`x:fonts` `x14ac:knownFonts` 7; the `xr*:uid` attributes on `worksheet`,
`table`, `tableColumn`, `autoFilter`, `workbookView`, `connection`,
`queryTable`, `pivotTableDefinition`, `pivotCacheDefinition`,
`x14:sparklineGroup`, `x14:slicer`, `x14:slicerCacheDefinition`,
`x15:timeline`, `x15:timelineCacheDefinition` (the x14/x15 schemas declare
their own; the main schema's `xr:uid` references exist for worksheet, table,
and the rest are the revision CR's); `xr:revisionPtr` with `xr6:coauthVersionLast`,
`xr6:coauthVersionMax`, `xr10:uidLastSave` (4 workbooks).

**`mc:AlternateContent`** (part: parent, Requires, Choice child): `workbook`:
`x:workbook` x15 `x15ac:absPath` (5, kept since CR-021); `sheet`:
`x:worksheet` x14 `x:controls` (1) and `x:controls` x14 `x:control` (1), both
**with no Fallback** - the checkbox's control, which the preprocessor drops
today ("Missing mc:Fallback! Dropping"), so `CT_Worksheet` and `CT_Controls`
admit the element and `x:worksheet`, `x:controls` join the retain list;
`drawing`: `xdr:wsDr` a14 and `xdr:twoCellAnchor` a14/tsle/sle15 (the slicer
and timeline shapes, a DrawingML CR's). **No `x12ac`** element in any file
(list validations came as `x14:dataValidations` in `extLst`), so
`CT_DataValidation` is not touched and x12ac is bound for its prefix and
schema only.

**`extLst` content** (host, child): `x:workbook` > `x14:workbookPr`,
`x15:workbookPr`, `x14:slicerCaches`, `x15:slicerCaches`,
`x15:timelineCacheRefs`, `x15:dataModel`, `xcalcf:calcFeatures`;
`x:worksheet` > `x14:sparklineGroups`, `x14:conditionalFormattings`,
`x14:dataValidations`, `x14:slicerList`, `x15:timelineRefs`; `x:cfRule` >
`x14:id`; `x:styleSheet` > `x14:slicerStyles`, `x15:timelineStyles`;
`x:pivotTableDefinition` > `x14:pivotTableDefinition`,
`xpdl:pivotTableDefinition16`; `x:filter` > `x15:pivotFilter`;
`x:pivotCacheDefinition` > `x14:pivotCacheDefinition`; `x:connection` >
`x15:connection`; `x14:slicerCacheDefinition` > `x15:tableSlicerCache`. All
through `CT_Extension`'s lax wildcard: typed once the elements exist.

**Parts** (content type, relationship type, target): as §8's table, with
Excel's strings for the timeline parts; and the data model's relationship
type, not in [MS-XLSX]'s part page, is
`http://schemas.openxmlformats.org/officeDocument/2006/relationships/powerPivotData`,
target `model/item.data`.

**What docx4j does with the seven today**: loads all but three parts;
`revisionPtr` rejected in four workbooks (dropped); the checkbox's controls
`mc:AlternateContent` dropped; the slicer file's drawing elements resolved to
their Fallbacks; `xl/drawings/vmlDrawing1.vml` fails to unmarshal (the
xlsx4j VML gap of `xlsx4j-backlog`); and in the data-model workbook
`xl/connections.xml` and both `xl/queryTables/` parts **fail to marshal** -
`CT_Connections` and `CT_QueryTable` have no `@XmlRootElement`, so
`ConnectionsPart` and `QueryTablePart` cannot save what they loaded. A
pre-existing xlsx4j defect; fixed in phase 1 (two annox annotations) since
the workbook exercising the data model needs it.

**The schemas' own dependencies, from their current text** (the recipe's
step 2): x14 imports x15, xr, xr2, xr10, xm (`xm:f`, `xm:sqref`) and the main
schema; x15 imports x14, x16, xr10, xm (`xm:f`) and the main schema; xr
imports xr6 and xr10; so the set is x14, x15, x14ac, x15ac, x12ac, x16, xr
(refreshed: it now declares `revisionPtr`, `CT_RevisionPtr` and `uid`), xr2,
xr6, xr10 - ten files - plus the 2006 `xm` namespace, whose docx4j copy
(`xsd/offmacro/office-excel-2006-main.xsd`, the macro-sheet schema) lacks the
`f`, `ref` and `sqref` declarations [MS-XLSX] §5.1 gives that namespace;
those are **merged into the existing file** (same namespace, same package) -
a departure from "re-take the file", since the macro-sheet declarations
docx4j has are not in §5.1.

**Scope additions decided by the measurement** (each a departure from §1's
list, recorded here): the refreshed xr schema binds `revisionPtr`, and
`CT_Workbook` admits it after the `mc:AlternateContent` (Excel's order:
`fileVersion`, `workbookPr`, `mc:AlternateContent`, `revisionPtr`,
`bookViews`, ...), so the workbook round-trip loss of the inventory's
second group closes here; xr6 comes for the pointer's attributes;
`CT_SheetFormatPr` takes `dyDescent`; `CT_Worksheet` and `CT_Controls` admit
`mc:AlternateContent`; the two root annotations; `xr16` in the prefix table.

Gate of phase 0 met: the host list of §2 is confirmed and extended by the
above; phase 1 proceeds on it.

## 17. Phase 1: the schemas, host admissions, regeneration, contexts, prefixes (2026-09-20)

Coded, on the phase 0 measurement:

- **The ten schemas**, each taken from its [MS-XLSX] 29.1 HTML section page
  on 2026-09-20 and cited in its header with the changes made to the page's
  text (import locations mapped to the tree; `x:ST_Xstring` and `x:ST_Guid`
  written `s:`, as the main schema has them; the package; root annotations;
  `mc:Ignorable`): `xsd/xlsx/office_spreadsheetml_2009_9_main.xsd` (x14),
  `..._2009_9_ac.xsd`, `..._2010_11_main.xsd` (x15), `..._2010_11_ac.xsd`,
  `..._2011_1_ac.xsd` (x12ac, default namespace patched), `..._2014_11_main.xsd`
  (x16), `..._2015_revision2.xsd`, `..._2016_revision6.xsd`,
  `..._2016_revision10.xsd`, and `xsd/sml/sml_2014_revision.xsd` refreshed
  (xr: `revisionPtr`, `CT_RevisionPtr`, `uid`). Packages as §3. Root-element
  annotations on `slicerCacheDefinition`, `slicers`, `formControlPr`,
  `datastoreItem`, `timelineCacheDefinition`, `timelines`, `survey`;
  `mc:Ignorable` on the four of them phase 0 saw it on.
- **xm**: `f`, `ref`, `sqref` with `ST_Ref`, `CT_Ref`, `ST_Sqref`, `CT_Sqref`
  merged into `xsd/offmacro/office-excel-2006-main.xsd` (§16); the stale,
  unwired 2013 copy `xsd/xlsx/office_excel_2006_main.xsd` of the same
  namespace removed.
- **The main schema** (`sml_ECMA376_4ed_transitional.xsd`): `mc:Ignorable` on
  `CT_Worksheet`, `CT_Stylesheet`, `CT_Table`, `CT_Comments`,
  `CT_pivotTableDefinition`, `CT_PivotCacheDefinition`, `CT_PivotCacheRecords`,
  `CT_Connections`, `CT_QueryTable`; `x14ac:dyDescent` on `CT_Row` and
  `CT_SheetFormatPr`, `x14ac:knownFonts` on `CT_Fonts` (the schema imports
  x14ac); `xr:revisionPtr` in `CT_Workbook` after the `mc:AlternateContent`;
  `mc:AlternateContent` (unbounded) in `CT_Worksheet` after `controls` - the
  position Excel wraps `oleObjects` and `controls` at - and in `CT_Controls`
  beside `control` (whose `minOccurs` becomes 0, since Excel writes every
  control wrapped); root annotations on `CT_Connections` and `CT_QueryTable`.
  `sml_root.xsd` imports the nine `xsd/xlsx/` files.
- `mc-preprocessor.xslt` retains `x:worksheet` and `x:controls`.
- `module-info.java`, `org.xlsx4j.jaxb.Context.jcSML`: six packages (x14,
  x15, x15ac, x12ac, x16, xr). **Departure from §4**: x14ac, xr2, xr6 and
  xr10 declare attributes only, and XJC generates no package for an
  attribute-only schema (the xr package did not exist before this phase
  either), so there are six, not eight or ten. The attributes are typed
  properties of their hosts in `org.xlsx4j.sml`.
- `NamespacePrefixMappings`: `x12ac`, `x16`, `xr16`, both ways, beside their
  kin.
- `docx4j-core-tests`: `org.xlsx4j.ExcelExtensionsTest` (10 tests) and the
  workbook `cr022-slicers-timelines.xlsx` as a test resource. On it: the
  workbook's `mc:Ignorable`, `revisionPtr` (with the xr6 and xr10
  attributes), both `slicerCaches`, `timelineCacheRefs` and `x14:workbookPr`
  typed; the worksheet's `dyDescent` (row and `sheetFormatPr`), `slicerList`,
  `timelineRefs`, `sparklineGroups`; the style sheet's `knownFonts`,
  `slicerStyles`, `timelineStyles`; the table's `mc:Ignorable`; a save and a
  reload keeping them all, with every prefix each of seven roots' `mc:Ignorable`
  names declared on that root; `x12ac:list` and `x16:modelTimeGroupings`
  marshalled with Excel's prefixes. On the LibreOffice workbooks (from a
  directory named by `-Dcr022.samples`, skipped otherwise; not committed):
  `sparklineGroups`, `conditionalFormattings` with the `cfRule`'s `x14:id`,
  `dataValidations` typed; the checkbox's controls `mc:AlternateContent`
  kept whole and written back (two nested, no Fallback); the data-model
  workbook saved with its `connections.xml` (typed `x15:connection` inside)
  and both query tables, each declaring `xr16`.
- CHANGELOG under "Schema (CR-022 ...)".

Found on the way:

- The section pages indent with non-breaking spaces (U+00A0); the schema
  parser rejects them ("Non-whitespace characters are not allowed in schema
  elements"). Replaced with spaces - recorded here for the recipe's
  "obtaining the schema" section (a departure to note when re-taking a page).
- x14's text imports x15 (`xl15.xsd`, `xlslicercache15.xsd`) but references
  no x15 type; only x15 references x14, so the pair is not circular, and
  the x14 file imports x15 no more.
- `x15:slicerCaches` is of x14's `CT_SlicerCaches` type, so both `slicerCaches`
  extensions of a workbook unwrap to the x14 class; tell them apart by the
  `ext` uri.
- `CT_OleObjects` also gets, in Excel's output, each `oleObject` in its own
  `mc:AlternateContent` (Choice Requires="x14" with `objectPr`, Fallback
  without). No phase 0 file has an OLE object, so by CR-021's rule it is not
  admitted here; the preprocessor resolves it to the Fallback, dropping the
  `objectPr`, until a file shows it. Follow-up for the xlsx4j backlog.
- The slicer, slicer cache, timeline and timeline cache parts load as
  `DefaultPart` (ContentTypeManager warns): phase 2.
- Nothing in docx4j-core read `CTExtension.getAny()` as a DOM `Element`
  (§13's risk re-checked by grep: no caller in `org.xlsx4j` or the
  SpreadsheetML parts), so the typed content changes no docx4j behaviour;
  the CHANGELOG records it for callers.

**Re-saves for the Excel check** (on the share, `fidelity/cr022/`):
`cr022-slicers-timelines-phase1-resave.xlsx`,
`lo-checkbox-form-control-phase1-resave.xlsx`, `lo-Sparklines-phase1-resave.xlsx`,
`lo-tdf167689_x15_namespace-phase1-resave.xlsx` - every XML part well formed
with its prefixes declared (xmllint), the slicer workbook's 31 entries all
present. Jason: open each in Excel 365; the gate's last row is "no repair
prompt; the checkbox still a checkbox; slicers and timeline present" (the
slicer and timeline drawings resolve to their Fallback shapes until the
DrawingML CR, so the drawings may render as plain shapes - that is expected
and not this phase's).

**Gate:**

| step | result |
|---|---|
| docx4j-generated-objects + docx4j-core clean install | BUILD SUCCESS; installed jars' md5 = target jars' |
| `ExcelExtensionsTest`, with the LibreOffice samples | 10 tests, 0 failures |
| docx4j-core-tests, the whole suite (with `-Dcr022.samples`) | 1208 tests, 0 failures, 11 skipped (2026-09-20) |
| docx4j-export-fo-tests | not run: no docx change, Jason waived it for this phase ("no need for export-fo-tests") |
| Excel 365 opens the four re-saves | passed (Jason, 2026-09-20): no repair prompt, checkbox, slicers and timeline intact |

**Hand-offs (2026-09-20, after the commit):** objects-ts regenerated from
16844ff03 (its commit 141f6bd: six new modules, 102 in total; the
attribute-only schemas generate no module there either; x14ac properties,
`ignorable` on the nine roots and `Workbook.revisionPtr` verified in its
declarations; x12ac, x16, xr16 in its prefix table, 127 entries level with
docx4j's). The Python port recorded the file list, packages, prefixes and
the U+00A0 gotcha for its CR-001 phase D (not scheduled; lands with its
schema re-copy). core-ts told what the oracle now keeps (its Excel
acceptance run is the consumer); objects-ts notes phase 2's parts are
core-ts's, not its own.

Gate of phase 1 met (2026-09-20); phase 2 (the parts) may proceed at Jason's go.

## 18. Phase 2: the parts (2026-09-20, at Jason's go)

Coded, as §8 planned:

- Nine part classes in `openpackaging/parts/SpreadsheetML/`: `SlicerCachePart`
  (`CTSlicerCacheDefinition`), `SlicersPart` (`CTSlicers`),
  `TimelineCachePart` (`CTTimelineCacheDefinition`), `TimelinesPart`
  (`CTTimelines`), `ControlPropertiesPart` (`CTFormControlPr`),
  `CustomDataPropertiesPart` (`CTDatastoreItem`), `SurveyPart` (`CTSurvey`),
  each a `JaxbSmlPart`; `CustomDataPart` and `DataModelPart` extend
  `BinaryPart`.
- Nine content types (`ContentTypes.SPREADSHEETML_SLICER_CACHE` and so on)
  and nine relationship types (`Namespaces.SPREADSHEETML_SLICER_CACHE` and so
  on), Excel 365's strings for the timeline pair (§8) and [MS-XLDM]'s
  `powerPivotData` relationship for the data model (§16).
- Registration: the seven XML parts in `JaxbSmlPart.newPartForContentType`,
  reached because `ContentTypeManager`'s SpreadsheetML branch now also
  matches their content types (they are `application/vnd.ms-excel.*` and
  `...customDataProperties+xml`, not `...spreadsheetml.*`); the data model
  by its content type; the custom data by `application/binary` **and** its
  relationship type, since that content type is generic. All sit in the
  chain where the pivot cache parts are, as §9 asked.
- `org.xlsx4j.ExcelExtensionPartsTest` (4 tests): on
  `cr022-slicers-timelines.xlsx`, the two slicer caches and the timeline
  cache reached from the workbook's relationships as typed parts (names,
  source names, `mc:Ignorable` "x xr10" / "xr10"), the slicers and the
  timeline from sheet 2's, and by part name; a save keeping every part
  typed, its content-type override and its relationship, with each root
  declaring the prefixes its `mc:Ignorable` names (`x`, the main namespace,
  included); on the LibreOffice workbooks, the checkbox's
  `ControlPropertiesPart` (`objectType` CheckBox) and the data-model
  workbook's `DataModelPart` (its bytes kept through a save). No
  "DefaultPart used" warning is logged for the seven workbooks any more.
- CHANGELOG: a bullet under the CR-022 "Schema" block.

Not exercised by any sample: `CustomDataPropertiesPart`, `CustomDataPart`
and `SurveyPart` (no workbook of §1 has an add-in's custom data or a
survey); they are wired by the same pattern and typed by the schemas of
phase 1, and a sample is a phase 2b candidate if one can be made.

**Re-saves for the Excel check** (on the share, `fidelity/cr022/`):
`cr022-slicers-timelines-phase2-resave.xlsx`,
`lo-checkbox-form-control-phase2-resave.xlsx`,
`lo-tdf167689_x15_namespace-phase2-resave.xlsx` - written by the typed
parts this time (the phase 1 re-saves copied those parts as bytes). Jason:
the gate's last row is "no repair prompt; slicers and timeline working
(they now come from re-marshalled parts); the checkbox a checkbox".

**Gate:**

| step | result |
|---|---|
| docx4j-core clean install | BUILD SUCCESS; installed jar md5 = target jar's |
| `ExcelExtensionPartsTest` + `ExcelExtensionsTest`, with the LibreOffice samples | 14 tests, 0 failures |
| docx4j-core-tests, the whole suite (with `-Dcr022.samples`) | 1212 tests, 0 failures, 11 skipped (2026-09-20) |
| Excel 365 opens the three re-saves, slicers and timeline working | Jason |

