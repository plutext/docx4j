# CR-027: Excel's revision `uid` attributes and Word's bibliography `Version` kept on a round trip - `xr3`, `xr9`, `xr16` bound, `xcalcf` typed

Status: PROPOSED 2026-09-25. Written by `docs/developer/adding-a-schema.md`
step 0; the item CR-022 §20 left for "one small CR" (the remaining revision
attributes and `xcalcf`). Found by docx4j-generated-objects-ts's CR-004 phase
B (237 parts of twelve docx4j test documents through its package) and
confirmed on docx4j's own round trip the same day. Drafted with Claude Fable
5.1. Owner: Jason Harrop.

Scope: attribute declarations Office writes that docx4j's schemas lack, so a
value Excel or Word wrote is dropped the moment the part is unmarshalled and
saved; plus one small element schema every Excel-saved workbook carries,
typed. Nothing structural: no wildcard, no element order, no part.

## 1. What happens today (measured 2026-09-24/25)

With every part forced to unmarshal (`JaxbXmlPart.getContents()` on each) and
the package saved:

| file (docx4j-core-tests resources) | attribute | before | after |
|---|---|---|---|
| `loadAndSave.xlsx` | `xr*:uid` (comments1 1, table1 5, workbook 1, sheet1 2) | 9 | 1 (the worksheet's) |
| `loadAndSave.docx` | `b:Sources/@Version` (customXml/item1.xml) | 1 | 0 |

objects-ts's corpus run put the same loss at 43 of 69 `xr*:uid` instances
across twelve documents; the 26 that survive are exactly the hosts docx4j
declares the attribute on (`CT_Worksheet`; the x14/x15 slicer and timeline
roots, whose schemas declare their own). Same mechanism in both toolchains:
no declaration and no `anyAttribute` on the host type, so JAXB drops it.

Not a repair trigger for Excel or Word: a revision `uid` is the identity a
revision stream or co-authoring session hangs off, and its loss is silent. It
is a fidelity loss, and the ports record it against docx4j by attribute.

## 2. Which hosts, measured on docx4j's own fixtures

`grep` of every `.xlsx` under `docx4j-core-tests/src/test/resources` for the
element carrying each `uid` (the count is instances across the fixtures):

| attribute | namespace ([MS-XLSX] 29.1 section) | host element | host type in `sml_ECMA376_4ed_transitional.xsd` | declared today |
|---|---|---|---|---|
| `xr:uid` | 2014/revision (5.15) | `worksheet` | `CT_Worksheet` | yes (CR-022) |
| | | `autoFilter` | `CT_AutoFilter` | no |
| | | `hyperlink` | `CT_Hyperlink` | no |
| | | `table` | `CT_Table` | no |
| | | `pivotCacheDefinition` | `CT_PivotCacheDefinition` | no |
| | | `pivotTableDefinition` | `CT_pivotTableDefinition` | no |
| | | `comment` | `CT_Comment` | no |
| | | `cellStyle` | `CT_CellStyle` | no |
| | | `dataValidation` (36 in `strict-invoice.xlsx`) | `CT_DataValidation` | no |
| `xr2:uid` | 2015/revision2 (5.16) | `workbookView` | `CT_BookView` | no (the x14 sparkline group has it) |
| `xr3:uid` | 2016/revision3 (5.17) | `tableColumn` | `CT_TableColumn` | no; the schema file does not exist |
| `xr9:uid` | 2016/revision9 (5.13) | `tableStyle` | `CT_TableStyle` | no; the schema file does not exist |
| `xr16:uid` | 2017/revision16 (not in the appendix) | `connection` | `CT_Connection` | no; the schema file does not exist |
| `xr10:uid` | 2016/revision10 (5.12) | the x14/x15 slicer and timeline roots | their own types | yes (CR-022) |

objects-ts's list (its message of 2026-09-25) is the first ten rows less
`cellStyle` and `dataValidation`, which its twelve documents did not carry
and `anon/strict-invoice.xlsx` does; `tableStyle`'s `xr9:uid` likewise. The
inventory (`docs/developer/ms-xlsx-schema-inventory.md` 5.17) says `xr3:uid`
also appears on rows and cells; no fixture here carries that, so `CT_Row` and
`CT_Cell` are **not** touched (admit what Office writes, measured, not what
it might).

And in Word: `<b:Sources SelectedStyle="..." StyleName="APA" Version="6">`
in `loadAndSave.docx` and `2010/w14_mcIgnorable-in-other-parts.docx`;
`CT_Sources` in `xsd/shared/shared-bibliography.xsd` (ECMA-376 2nd ed.)
declares `SelectedStyle`, `StyleName` and `URI` only.

## 3. The schemas and their sources (recipe step 1)

Three attribute-only schemas, one each for `xr3`, `xr9` and `xr16`, the
shape of `xsd/xlsx/office_spreadsheetml_2015_revision2.xsd` (one
`<xsd:attribute name="uid" type="s:ST_Guid"/>`):

- **2016/revision3** (`xr3`): [MS-XLSX] 29.1 §5.17, HTML section page
  `https://learn.microsoft.com/en-us/openspecs/office_standards/ms-xlsx/1c6fdaed-8a40-4086-be07-0135b10b8f90`,
  fetched 2026-09-25: one `uid` of `x:ST_Guid`.
- **2016/revision9** (`xr9`): §5.13,
  `https://learn.microsoft.com/en-us/openspecs/office_standards/ms-xlsx/1df7cd78-d8bc-4ec4-8952-1b61beeaee76`,
  fetched 2026-09-25: one `uid` of `x:ST_Guid`.
- **2017/revision16** (`xr16`): **not in the appendix** (CR-022 §10 found
  the same and added the prefix only; the ToC of 2026-09-25 still has no
  page for it). **Departure from the recipe**: the schema is written from
  measurement - `connection/@xr16:uid` is a GUID in braces in
  `cr022-data-model.xlsx`, the shape of every other `xr*:uid` - and its
  header says so; re-take it from the specification if a page appears.

The element schema, phase 2:

- **2018/calcfeatures** (`xcalcf`): §5.22,
  `https://learn.microsoft.com/en-us/openspecs/office_standards/ms-xlsx/6c4054c8-05a9-49dc-baa9-13e5a6282989`,
  fetched 2026-09-25: `calcFeatures` holding one or more `feature/@name`.
  Excel writes it in the workbook's `extLst`, `ext
  uri="{B58B0392-4F1F-4190-BB64-5DF3571DCE5F}"`, in five of the fixtures
  (`loadAndSave.xlsx`, `strict/strict-comments.xlsx`, the three `anon/*.xlsx`)
  with the names `microsoft.com:RD`, `Single`, `FV`, `CNMTM`, ... It survives a
  round trip today as DOM through `CT_Extension`'s lax wildcard, with its own
  `xmlns:xcalcf` on the `ext`; binding it is typing, not a loss closed.

Left out, with the reason: **2024/workbookCompatibilityVersion** (§5.42,
`version/@setVersion`, `@warnBelowVersion`; the inventory's fourth item of
this group). No fixture or sample here carries it, and the recipe requires an
Office file exercising a schema before it is bound; it also survives as DOM.
Take it when a workbook that has it arrives.

For `b:Sources/@Version`: not in ECMA-376 (any edition) or [MS-DOCX];
Word writes it (`Version="6"` on every bibliography part in the fixtures).
**Departure**: declared from measurement as `xsd:string`, optional, with a
comment saying so, as `w16cex.xsd`'s header records attributes Office writes
that the source omits.

## 4. Where they join the tree (recipe step 2)

- The three new files go under `xsd/xlsx/` beside `office_spreadsheetml_2015_revision2.xsd`,
  named `office_spreadsheetml_2016_revision3.xsd`,
  `office_spreadsheetml_2016_revision9.xsd`,
  `office_spreadsheetml_2017_revision16.xsd`, each importing the shared
  simple types for `s:ST_Guid` (as the revision2 file does; the page's
  `x:ST_Guid` is the same type from the main schema, rewritten to avoid a
  circular import).
- `xsd/sml/sml_root.xsd` imports the three (beside the revision2, 6 and 10
  imports); `sml_ECMA376_4ed_transitional.xsd` declares the three prefixes on
  `xsd:schema` (it has `xr` and `x14ac` there; `xr2` is referenced from the
  x14 schema today, so it gains `xr2` too) and takes the `xsd:attribute ref`
  lines of §2 on the twelve host types, each with the same one-line comment
  `CT_Worksheet`'s has.
- `xcalcf`: `xsd/xlsx/office_spreadsheetml_2018_calcfeatures.xsd`, imported
  from `sml_root.xsd`. It references nothing but `xsd:string`, so it needs no
  import and crosses no context boundary; it is reached through
  `CT_Extension`'s wildcard, so no host admits it by name and no `annox` root
  is needed - the schema's own `xsd:element name="calcFeatures"` is a global
  element, which is what the lax wildcard types by.
- `shared-bibliography.xsd`: one attribute on `CT_Sources`.

Attribute-only schemas generate no package (recipe step 4): the `xr3`,
`xr9` and `xr16` attributes become `uid` properties of the host classes
(`CTTableColumn.getUid()` and so on), the same as `CTWorksheet`'s today.
Nothing to export, open, or add to a context list for them. `xcalcf` is a
package: `org.xlsx4j.com.microsoft.schemas.office.spreadsheetml.x2018.calcfeatures`,
after the pattern of the revision package, with the module-info `exports` and
`opens` lines and the `jcSML` context entry (recipe steps 5 and 6). The Word
context is not touched.

## 5. The prefix table (recipe step 7)

`xr3`, `xr9` and `xr16` are in `NamespacePrefixMappings` both ways already
(CR-022 and CR-019). `xcalcf` is **not** (measured 2026-09-25: no match in the
table); it is added both ways beside the `xr*` entries, so that a typed
`calcFeatures` marshals with Excel's prefix rather than a generated one.
Excel does not name `xcalcf` in any `mc:Ignorable`, so this is cosmetic
rather than a repair matter. The hand-off tells objects-ts, which keeps a copy
of the table.

## 6. Parts (recipe steps 8 and 9)

None. Every host is content of an existing typed part.

## 7. Tests (recipe step 10)

- `org.xlsx4j.RevisionUidAttributesTest`, new: loads
  `cr022-data-model.xlsx` (xr, xr2, xr3, xr16) and `anon/strict-invoice.xlsx`
  (xr on hyperlink, cellStyle, dataValidation, autoFilter; xr9 on tableStyle;
  xr2, xr3), forces every part to unmarshal, saves, and asserts by element
  name that the count of each `uid` attribute after equals the count before
  (the method of §1, now a test); and reads a few through the typed accessors
  (`CTBookView.getUid()`, `CTTableColumn.getUid()`, `CTConnection.getUid()`,
  `CTTableStyle.getUid()`).
- `org.docx4j.openpackaging.parts.WordprocessingML.BibliographyPartTest`
  (or an addition to an existing bibliography test if one exists): `loadAndSave.docx`'s
  bibliography part reads `Version` as `"6"`, and a save and reload keep it.
- Phase 2: `ExcelExtensionsTest` gains a case: `loadAndSave.xlsx`'s workbook
  `extLst` yields a typed `CTCalcFeatures` with its features' names, and the
  round trip writes `xcalcf:` as the prefix.
- The whole `docx4j-core-tests` suite (surefire forks per class, so a context
  problem shows anywhere).
- **The Office-open check**: the round-tripped `cr022-data-model.xlsx`,
  `strict-invoice.xlsx` (transitional out, as a strict input always is) and
  `loadAndSave.xlsx` on the share under `fidelity/cr027-uid/` with a README
  saying what to look at (opens without a repair prompt; the table columns,
  connections and data validations are intact). Jason's verdict recorded in
  §10.

## 8. Hand-offs (recipe step 11)

- **objects-ts**: the commit hash; the four new xsd files and the two edited
  (`sml_ECMA376_4ed_transitional.xsd`, `shared-bibliography.xsd`); the effect:
  `uid` properties on twelve SML types, `version` on `CTSources`, a new
  `calcfeatures` module; the `xcalcf` prefix entry. Its two
  `KNOWN_MISSING_ATTRIBUTES` records (all 22 affected parts) fail when no
  part loses the attribute any more, so its regeneration from the commit is
  the check.
- **Python**: SML is not generated yet (its CR-001 phase D), so the
  SpreadsheetML side does not reach it; `shared-bibliography.xsd` is in WML's
  closure if a bibliography part is, so it is told the file and the one
  attribute for its next re-copy.
- **core-ts**: told, since the values are ones its round trip compares.

## 9. Risks

- A `uid` property already present on a type would collide: none of the
  twelve hosts has one (`CT_Comment` has `guid`, not `uid`). XJC fails the
  build on a collision, so the regeneration is the check.
- `CT_DataValidation`'s 36 instances in one workbook: an attribute is cheap;
  no performance concern.
- The `xr16` schema is docx4j's own text, not the specification's; if
  Microsoft publishes the page with a different type, re-take it. A GUID
  string round-trips either way.
- MOXy: the SML context grows by one package in phase 2; the MOXy selector is
  exercised by the core suite's context check, as CR-022 relied on.

## 10. Phases and gates

**Phase 1 - the attributes.** The three `xr*` schema files and their imports,
the twelve `xsd:attribute ref` lines, `CT_Sources/@Version`; the two tests;
the CHANGELOG line under "Schemas"; one commit (recipe step 12). Gate: the
regeneration builds; the two tests pass; `docx4j-core-tests` green; the
Excel-open check by Jason on the share.

**Phase 2 - `xcalcf` typed.** The schema file and import, the package's
module-info and `jcSML` lines, the prefix both ways, the `ExcelExtensionsTest`
case; one commit. Gate: `docx4j-core-tests` green (the context check runs on
both runtimes' selectors); the round trip of `loadAndSave.xlsx` writes the
`xcalcf` prefix; folded into the same Excel-open check.

Both are for 17.2.1, so the ports regenerate once for the release.

## 11. Effort (rough)

Phase 1 two hours (the schema lines are small; the regeneration and the suite
are the time; Jason's Excel check). Phase 2 an hour.

## 12. References

- CR-022 (§10 the roots and `xr16`; §20 the leftover this CR takes up),
  CR-023 (the Word attributes' precedent: declared from [MS-DOCX] and, for
  what Word writes and the source omits, from measurement), CR-024 §10 (the
  ports' fidelity records as the check that a fix landed).
- `docs/developer/ms-xlsx-schema-inventory.md` (5.13, 5.17, 5.22, 5.42).
- [MS-XLSX] 29.1 §5.13, §5.17, §5.22 (pages above); objects-ts CR-004 §9
  (its measurement).
