# [MS-XLSX] schemas against docx4j's SpreadsheetML binding

An inventory of the 45 namespaces in the "Full XML Schema" appendix of
[MS-XLSX] revision 29.1 (2026-05-19, from the specification's table of contents
on learn.microsoft.com) against what docx4j binds, knows by prefix, or carries
unwired, measured on VERSION_17_1_1 on 2026-09-20 (Jason asked "identify any
schema used within it which we don't yet support"), and **updated the same day
after CR-022** (phases 1 to 2b) bound the Excel 2010 and 2013 extensions.
Purposes are from the appendix's own XSD text where it is short, else from
section 2.3's overview and the element names.

## What docx4j binds today

The SpreadsheetML JAXB context (`org.xlsx4j.jaxb.Context.jcSML`) holds ten
packages: `org.xlsx4j.sml` (ECMA-376 4th edition transitional SpreadsheetML),
`org.xlsx4j.schemas.microsoft.com.office.excel_2006.main` (5.1, macro sheets
and, since CR-022, `xm:f`, `xm:ref`, `xm:sqref`), `...excel_2008_2.main`
(`http://schemas.microsoft.com/office/excel/2008/2/main`, an older revision's
macro-sheet schema no longer in the appendix), `...excel.x2010.spreadsheetDrawing`
(5.8), and, **since CR-022 (17.1.1)**, `org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2009.x9.main`
(5.4, x14), `...x2010.x11.main` (5.3, x15), `...x2010.x11.ac` (5.9, x15ac),
`...x2011.x1.ac` (5.6, x12ac), `...x2014.x11.main` (5.10, x16) and
`...x2014.revision` (5.15, xr, with `revisionPtr`). The attribute-only schemas
5.5 (x14ac), 5.12 (xr10), 5.14 (xr6) and 5.16 (xr2) are bound too, as typed
properties of the host types (`Row.getDyDescent()`, `CTFonts.isKnownFonts()`,
`CTRevisionPtr.getUidLastSave()` ...); XJC generates no package for a schema
with no element or type. The main schema admits what Excel writes around
them: `mc:Ignorable` on every root Excel writes it on, `xr:revisionPtr` in
the workbook, the `mc:AlternateContent` around controls, and `extLst`
content in these namespaces unmarshals typed. Nine parts are typed
(`SlicerCachePart`, `SlicersPart`, `TimelineCachePart`, `TimelinesPart`,
`ControlPropertiesPart`, `CustomDataPropertiesPart`, `SurveyPart`,
`CustomDataPart`, `DataModelPart`), and `xsd/xlsx/` holds the schemas, taken
from the current revision's section pages (`docs/developer/adding-a-schema.md`).

Three states short of "bound":

- **prefix known**: in `NamespacePrefixMappings` both ways, so a part's
  `mc:Ignorable` naming it is declared on save (no repair prompt) but its
  content is not typed: xr3, xdr14 (bound), `xvml`, and `xr16`
  (spreadsheetml/2017/revision16, not in the appendix; Excel writes it on
  connections and query tables).
- **xsd present, unwired**: `xsd/xlsx/` still holds the 2013-07-26 copies
  of 5.7 (`office_drawing_2012_timeslicer.xsd`) and 5.8's drawing schema, and
  an empty `office_drawing_2010_slicer .xsd` (5.2); the slicer and timeline
  shapes sit in a drawing's `graphicData` wildcard and round-trip as DOM; since
  CR-024 (2026-09-20) the `a14`/`tsle`/`sle15` `mc:AlternateContent` in a
  drawing is kept whole, both branches (it resolved to its Fallback before),
  and `sle`, `sle15`, `tsle` are in the prefix table. Binding the schemas
  themselves is a CR when a caller wants the shapes typed (CR-024 §6).
- **absent**: everything else.

## Occurrence

Measured on the repository's seven workbooks (test resources and samples, all
Excel-saved): every one declares x14, x14ac, x15, x15ac, xr, xr2, xr3, xr6 and
xr10; `x16r2` (5.11) is in three; `xcalcf` (5.22) in three; `xr9` (5.13) in
one; `xlrd2` (5.20) in one; `workbookCompatibilityVersion` (5.42) in one. None
of the parts-defining namespaces (rich data, threaded comments, named sheet
views, feature property bag) occurs in them - the sample workbooks are simple.
A real-workbook corpus would rank the rest; none exists here yet.

## The 45 namespaces

`status`: bound / prefix / unwired / absent, as above. `seen`: in the
repository's workbooks.

| § | prefix | namespace (`http://schemas.microsoft.com/office/` + ...) | purpose | status | seen |
|---|---|---|---|---|---|
| 5.1 | xm | excel/2006/main | Excel 2007 macro sheets (`macrosheet` and its children); `f`, `ref`, `sqref` used by x14 and x15 content | bound (f/ref/sqref: CR-022) | |
| 5.2 | sle | drawing/2010/slicer | the slicer shape in a drawing part (Excel 2010) | absent (empty file in `xsd/xlsx`); the shape's `mc:AlternateContent` kept whole and the prefix known since CR-024 | yes (CR-022 fixture) |
| 5.3 | x15 | spreadsheetml/2010/11/main | Excel 2013: the data model and its connections, timelines and their caches, table slicer caches, pivot filter and calculated-member extensions, workbook and worksheet `extLst` content | **bound (CR-022)**: typed extLst content; `TimelineCachePart`, `TimelinesPart`, `SurveyPart` | yes |
| 5.4 | x14 | spreadsheetml/2009/9/main | Excel 2010: sparklines, slicers and slicer caches, conditional-formatting and data-validation extensions (extended formulas, icon sets, data bars), protected ranges, pivot and table extensions, form-control properties | **bound (CR-022)**: typed extLst content; `SlicerCachePart`, `SlicersPart`, `ControlPropertiesPart`, `CustomDataPropertiesPart` | yes |
| 5.5 | x14ac | spreadsheetml/2009/9/ac | Excel 2010 attributes carried through `mc:Ignorable`: `dyDescent` on rows and `sheetFormatPr`, `knownFonts` on the font table | **bound (CR-022)**: typed properties of `Row`, `CTSheetFormatPr`, `CTFonts` | yes |
| 5.6 | x12ac | spreadsheetml/2011/1/ac | Excel 2010 SP1: the `list` element of a data validation's `mc:AlternateContent` Choice | **bound (CR-022)**: the element and prefix; `CT_DataValidation` admits no `mc:AlternateContent` yet (no file shows one) | |
| 5.7 | tsle | drawing/2012/timeslicer | the timeline slicer shape in a drawing part (Excel 2013) | unwired xsd; the shape's `mc:AlternateContent` kept whole and the prefix known since CR-024 | yes (CR-022 fixture) |
| 5.8 | xdr14 | excel/2010/spreadsheetDrawing | Excel 2010 drawing content parts (ink) | bound | |
| 5.9 | x15ac | spreadsheetml/2010/11/ac | Excel 2013 attributes through `mc:Ignorable`: `absPath` on the workbook | **bound (CR-022)** | yes |
| 5.10 | x16 | spreadsheetml/2014/11/main | time grouping of data-model pivot fields (`modelTimeGroupings`) | **bound (CR-022)** | |
| 5.11 | x16r2 | spreadsheetml/2015/02/main | one attribute, `formatCode16`, an extended number-format code on a number format | absent; the prefix known since CR-024 (Excel names it in `styles.xml`'s `mc:Ignorable`) | yes |
| 5.12 | xr10 | spreadsheetml/2016/revision10 | `uid`, `uidLastSave`, revision tracking | **bound (CR-022)**: attributes of `CTRevisionPtr` and the x14/x15 roots | yes |
| 5.13 | xr9 | spreadsheetml/2016/revision9 | one `uid` attribute, revision tracking | absent | yes |
| 5.14 | xr6 | spreadsheetml/2016/revision6 | `coauthVersionLast`/`coauthVersionMax`, co-authoring | **bound (CR-022)**: attributes of `CTRevisionPtr` | yes |
| 5.15 | xr | spreadsheetml/2014/revision | `revisionPtr` on the workbook and `uid` attributes: the revision stream that co-authoring and version history hang off | **bound (CR-022)**: `Workbook.getRevisionPtr()`, `uid` on worksheet, table and the rest | yes |
| 5.16 | xr2 | spreadsheetml/2015/revision2 | `uid` attributes (book views, sparkline groups) | **bound (CR-022)**: attribute of `CTSparklineGroup`; prefix for the rest | yes |
| 5.17 | xr3 | spreadsheetml/2016/revision3 | `uid` attributes (rows, cells, columns) | prefix | yes |
| 5.18 | xr5 | spreadsheetml/2016/revision5 | an empty stub in the current revision | absent | |
| 5.19 | xpdl | spreadsheetml/2016/pivotdefaultlayout | default pivot table layout settings in the workbook | absent | |
| 5.20 | xlrd2 | spreadsheetml/2017/richdata2 | rich value types and structures (linked data types: stocks, geography, images) | absent | yes |
| 5.21 | xlrd | spreadsheetml/2017/richdata | rich values themselves (the `rdrichvalue` part) and the cell-metadata link to them | absent | |
| 5.22 | xcalcf | spreadsheetml/2018/calcfeatures | calculation-feature flags on the workbook (dynamic-array era compatibility) | absent | yes |
| 5.23 | xltc | spreadsheetml/2018/threadedcomments | the threaded comments part (one per sheet) and the persons part it references | absent | |
| 5.24 | | spreadsheetml/2018/08/main | one attribute, `misleadingFormat` | absent | |
| 5.25 | xda | spreadsheetml/2017/dynamicarray | dynamic array formula properties (spill) via cell metadata | absent | |
| 5.26 | xnsv | spreadsheetml/2019/namedsheetviews | the named sheet views part: per-user filter and sort views of a sheet | absent | |
| 5.27 | | spreadsheetml/2019/extlinksprops | `externalLinksPr` (`autoRefresh`) on the workbook | absent | |
| 5.28 | | spreadsheetml/2020/richdatawebimage | web-image rich values | absent | |
| 5.29 | | spreadsheetml/2020/pivotNov2020 | pivot table extensions (2020) | absent | |
| 5.30 | xltc2 | spreadsheetml/2020/threadedcomments2 | threaded comments extensions | absent | |
| 5.31 | | spreadsheetml/2020/richvaluerefresh | rich value refresh properties | absent | |
| 5.32 | | spreadsheetml/2022/pivotVersionInfo | pivot table version information | absent | |
| 5.33 | | spreadsheetml/2022/pivotRichData | pivot tables over rich data | absent | |
| 5.34 | | spreadsheetml/2021/extlinks2021 | `alternateUrls` of an external book (absolute and relative, cloud drive and item ids) | absent | |
| 5.35 | | spreadsheetml/2022/richvaluerel | the rich value relationships part (a list of `r:id`) | absent | |
| 5.36 | xfpb | spreadsheetml/2022/featurepropertybag | the feature property bag part (checkbox cells and other feature state) | absent | |
| 5.37 | | spreadsheetml/2023/msForms | a Microsoft Forms link on the workbook | absent | |
| 5.38 | | spreadsheetml/2023/externalCodeService | the external code service (Python in Excel's service) | absent | |
| 5.39 | | spreadsheetml/2023/python | Python in Excel formulas | absent | |
| 5.40 | | spreadsheetml/2023/pivot2023Calculation | pivot table calculation extensions (2023) | absent | |
| 5.41 | | spreadsheetml/2024/pivotAutoRefresh | pivot table auto-refresh | absent | |
| 5.42 | | spreadsheetml/2024/workbookCompatibilityVersion | `version` (`setVersion`, `warnBelowVersion`): the workbook's compatibility version | absent | yes |
| 5.43 | | spreadsheetml/2023/showDataTypeIcons | a sheet-view flag, data type icons visible | absent | |
| 5.44 | | spreadsheetml/2025/externalCodeService2 | external code service, second schema | absent | |
| 5.45 | | spreadsheetml/2025/pivotDataSource | pivot table data source extensions (2025) | absent | |

## What this suggests, for a CR to decide

- **First** - **implemented via CR-022 (17.1.1, 2026-09-20)**: the Excel 2010
  and 2013 schemas every Excel-saved workbook carries (5.3, 5.4, 5.5, 5.9, 5.6,
  with 5.10 and the revision schemas they import), re-taken from the current
  revision's section pages; sparklines, slicers, timelines, the data model's
  metadata and the rest are typed, nine parts exist, and docx4j writes them
  (`org.xlsx4j.samples.Excel2010ExtensionsSamples`; fixtures
  `cr022-*.xlsx` in docx4j-core-tests, Excel-saved).
- **Second** - **partly via CR-022**: `revisionPtr` with 5.12, 5.14, 5.15 and
  5.16 bound (the round-trip loss on `CT_Workbook` is closed). Remaining: 5.13
  (`xr9`), 5.17 (`xr3`, prefix only; its `uid`s on rows and cells are dropped
  today, which a `CT_BookView`/`CT_Row` attribute reference would keep), 5.22
  (`xcalcf`) and 5.42 (`workbookCompatibilityVersion`) - small, present in most
  workbooks, and one CR together.
- **Third**: the parts a modern workbook adds - threaded comments and persons
  (5.23, 5.30), rich data (5.20, 5.21, 5.35, 5.28, 5.31), named sheet views
  (5.26), the feature property bag (5.36): each is a Part class by the recipe,
  and each wants an Excel-saved file exercising it in the test resources.
- The rest (pivot extensions, Python, Forms, single attributes) as demand
  arises; each is small.
- **The drawing shapes** (5.2 `sle`, 5.7 `tsle`, and `sle15`,
  drawing/2012/slicer, which is [MS-ODRAWXML]'s): the slicer and timeline
  shapes in a worksheet drawing sit inside `mc:AlternateContent` Choices
  requiring `a14`, `tsle` or `sle15`, which the preprocessor resolves to
  their Fallback `xdr:sp` today. A DrawingML CR (the preprocessor's retain
  list gaining `xdr:wsDr`/`xdr:twoCellAnchor`, and the shape schemas bound).

Each of these is a change request first (`docs/developer/adding-a-schema.md`,
step 0), with a real workbook corpus to rank them by occurrence.
