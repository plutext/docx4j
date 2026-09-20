# CR-024: mc:AlternateContent in DrawingML hosts - spreadsheet drawings and charts kept whole, as CR-021 keeps WordprocessingML, SpreadsheetML and PresentationML

Status: PROPOSED 2026-09-20 (Jason Harrop: "draft a DrawingML AlternateContent
CR", after the objects-ts session's round-trip probe found `cr022-checkbox.xlsx`'s
drawing part emptied by a load-and-save, and the loss was confirmed here).
Written by `docs/developer/adding-a-schema.md` step 0 where it applies (no
new namespace is bound in phase 1; §7 names the departures). Drafted with
Claude Fable 5.1. Owner: Jason Harrop.

Scope: the `mc:AlternateContent` elements Office writes inside DrawingML
parts, which CR-021 did not reach (it covered `w:r`, `w:p`,
`w:numPicBullet`, `x:workbook`, `x:worksheet`, `x:controls`, `p:spTree`,
`p:grpSp`, `p:controls`): the spreadsheet drawing's root and anchors
(`xdr:wsDr`, `xdr:twoCellAnchor`, `xdr:oneCellAnchor`, `xdr:absoluteAnchor`)
and the chart's `c:chartSpace`. Same policy as CR-021: the element is
admitted where Office writes it, kept whole on a round trip, and one
selection rule (`McSelection`) decides which branch a reader sees. Not in
scope: typing the branches' content (the anchors are local elements, so
kept content is DOM - §5), and binding the slicer and timeline shape
schemas (`sle`, `tsle`, `sle15`; their content sits inside `a:graphicData`,
a lax wildcard, and is DOM either way - §6). Two prefixes come along (§4).

## 1. The measurement (2026-09-20)

Every `mc:AlternateContent` in the repository's 82 Office files and the
fidelity corpus's 342 real documents, by parent (the survey script
`survey-ac.py` walks every XML part with a namespace-aware parser):

| part | parent | Requires | Choice holds | Fallback holds | files |
|---|---|---|---|---|---|
| `xl/drawings/` | `xdr:wsDr` | a14 | `xdr:twoCellAnchor` (a check box's shape) | **empty** | `cr022-checkbox.xlsx` |
| `xl/drawings/` | `xdr:twoCellAnchor` | a14 / tsle / sle15 | `xdr:graphicFrame` (a slicer or timeline) | `xdr:sp` (a "works in Excel 2010 or higher" box) | `cr022-slicers-timelines.xlsx` (3) |
| `xl/drawings/` | `xdr:oneCellAnchor` | a14 | `xdr:sp` | `xdr:sp` | `cr022-slicers-timelines.xlsx` |
| `*/charts/` | `c:chartSpace` | c14 | `c14:style` (`val="102"`) | `c:style` (`val="2"`) | `loadAndSave.docx`, `.pptx`, `.xlsx` (one chart each) |
| `ppt/slides/` | `p:spTree` | a14 | `p:sp` | `p:sp` | `loadAndSave.pptx` - **kept since CR-021 phase 3** |

The corpus (Word documents) has none of these: its `mc:AlternateContent`
is all `w:r`/`w:p` (CR-021's). Every host in the table above that CR-021 did
not reach is resolved by the load-time preprocessor to its Fallback (the
warning "not kept by the preprocessor ... selecting its Fallback" is logged
for each), which is what Office 2007 would see. **Measured with every JAXB
part forced to unmarshal** (docx4j writes an untouched part back from its
bytes, so the ordinary path hides the loss until something touches the
part):

| file, part | before | after | what a consumer loses |
|---|---|---|---|
| `cr022-checkbox.xlsx`, `drawing1.xml` | one anchor, the check box's shape | **a bare `<xdr:wsDr/>`** - the Fallback is empty | the drawing (the control itself survives through its VML legacy drawing, so Excel still shows a check box) |
| `cr022-slicers-timelines.xlsx`, `drawing1.xml` and `drawing2.xml` | 3 `graphicFrame`s (two slicers, a timeline) | 3 `xdr:sp` boxes reading "works in Excel 2010 or higher" | the slicer and timeline shapes; Excel then draws the boxes, and the slicer/timeline parts are orphaned on the sheet |
| `loadAndSave.docx`, `chart1.xml` | `c14:style` 102 | `c:style` 2 | Excel 2010's chart style; the chart restyles |

The empty Fallback is the interesting one: it is legal (a Fallback may be
empty - the 2007 reader is meant to draw nothing) and it turns "select the
Fallback" into "delete the drawing".

## 2. Hosts and the schema changes (recipe step 2's host side)

The three DrawingML schemas gain the `mce` import (as `wml.xsd`, `sml` and
`pml` have it) and these references:

| schema | host | change | why there |
|---|---|---|---|
| `xsd/dml/dml-spreadsheetDrawing.xsd` | `EG_Anchor` (the choice `CT_Drawing` repeats) | `<xsd:element ref="mc:AlternateContent"/>` as a fourth choice member | Excel writes it beside the anchors, as a sibling of `twoCellAnchor` (measured: `xdr:wsDr` > AC) |
| same | `EG_ObjectChoices` (the choice `CT_TwoCellAnchor`, `CT_OneCellAnchor`, `CT_AbsoluteAnchor` and `CT_GroupShape` share for `sp`/`grpSp`/`graphicFrame`/`cxnSp`/`pic`/`contentPart`) | `<xsd:element ref="mc:AlternateContent"/>` as a choice member | Excel writes it in the object's place (measured on `twoCellAnchor` and `oneCellAnchor`; `absoluteAnchor` and `grpSp` share the group, so admitted by construction, as CR-021 admitted `p:grpSp` beside `p:spTree`) |
| `xsd/dml/dml-chart.xsd` | `CT_ChartSpace` | `<xsd:element ref="mc:AlternateContent" minOccurs="0"/>` beside `style` | Excel writes the c14 style choice in `style`'s position (measured in all three chart parts) |

The JAXB shape: `CTDrawing.getEGAnchor()` is a `List<Object>` already (a
repeated choice), so an `AlternateContent` member joins the anchors;
`CTTwoCellAnchor` and its siblings gain an `alternateContent` property
beside `sp`/`graphicFrame`; `CTChartSpace` gains `alternateContent`. No
existing accessor changes.

**`CT_ChartSpace` in the three contexts**: `org.docx4j.dml.chart` and
`org.docx4j.dml.spreadsheetdrawing` are shared by `jc`, `jcPML` and
`jcSML`; `org.docx4j.mce` is in all three already (SpreadsheetML and
PresentationML admit the element since CR-021), so the reference resolves
in each. Checked in phase 1 by the three contexts initialising (the core
suite's context check).

## 3. The preprocessor's retain list

`mc-preprocessor.xslt`: `parent::xdr:wsDr`, `parent::xdr:twoCellAnchor`,
`parent::xdr:oneCellAnchor`, `parent::xdr:absoluteAnchor`,
`parent::xdr:grpSp`, `parent::c:chartSpace` join the list (with `xmlns:xdr`
and `xmlns:c` declared on the stylesheet); the warning's list of kept
parents updated. The preprocessor is reached only when a first unmarshal
fails; with the hosts admitted it will not be, for these, but the list is
the record of policy.

## 4. The prefix table (recipe step 7)

Two prefixes Excel names in `mc:Ignorable` that the table lacks, found by
objects-ts's probe (2026-09-20) - an undeclared ignorable prefix is a
repair prompt, as CR-023's `cr` showed: `x16r2`
(`http://schemas.microsoft.com/office/spreadsheetml/2015/02/main`,
[MS-XLSX] 5.11, `formatCode16`; Excel names it on `styles.xml`) and `oel`
(`http://schemas.microsoft.com/office/2019/extlst`, an `extLst` namespace
Word 365 declares on every part). Also to check in phase 1, and add if
absent: `sle` (drawing/2010/slicer), `tsle` (drawing/2012/timeslicer),
`sle15` (drawing/2012/slicer) - the `Requires` values of the kept Choices,
which the kept branches carry as declarations (they are on the Choice
elements in Excel's output, so a kept-whole DOM keeps them; the table entry
matters if anything re-marshals them typed). `a14` and `c14` are present.

## 5. Kept as DOM, and the reader's view

`mce`'s minimal schema is a lax wildcard (CR-021 §8.12), so a kept Choice
or Fallback unmarshals its children typed only if they are **global**
elements of a bound schema. `xdr:twoCellAnchor`, `xdr:sp`,
`xdr:graphicFrame` and `c:style` are local elements (the spreadsheet
drawing schema declares only `from`, `to` and `wsDr` globally), so they
come back as DOM `Element`s - lossless, byte-for-byte on re-marshal, which
is the round-trip guarantee this CR is for. `c14:style` is global in
`org.docx4j.dml.chart.x2007` and comes back typed. This is the same state
CR-021 left `w:drawing`/`w:pict` in (`w:r`'s are local elements too).

A **reader** (an exporter, a walker in READ mode) sees one branch, chosen by
`McSelection`: with `docx4j.jaxb.mc.preferChoice` empty (the default) the
Fallback - for the check box, nothing; for a slicer, the "works in Excel
2010" box; for the chart, style 2. That is Office 2007's view, unchanged
from today, and the property (`a14`, `c14`, `tsle`, `sle15`) selects the
modern branch. Nothing in docx4j-core renders a spreadsheet drawing, so
the reader's view matters only to callers.

**Typing the branches** (making `twoCellAnchor`, `oneCellAnchor`,
`absoluteAnchor`, `sp`, `graphicFrame`, `grpSp`, `pic`, `cxnSp` and
`c:style` global elements and referring to them, as `sml` does for its
part roots) is the follow-on if a caller needs the kept shape as objects.
It brings its own admissions: the check box's a14 shape has
`a:srgbClr mc:Ignorable="a14" a14:legacySpreadsheetColorIndex="65"`, an
ignorable attribute on a non-root element, which `CT_SRgbColor` would have
to admit (with `mc:Ignorable`) or lose. Left out of this CR, named here.

## 6. The shape schemas, left unbound

`sle` (2010/slicer, [MS-ODRAWXML]), `tsle` (2012/timeslicer, [MS-XLSX] 5.7)
and `sle15` (2012/slicer, not in [MS-XLSX]'s appendix) describe the
`a:graphicData` content of a slicer's or timeline's `graphicFrame` - a
`graphicData` wildcard's content is DOM whether or not the schema is bound,
so binding buys typing only, and nothing in docx4j reads it. `xsd/xlsx/`
still holds 2013 copies of `tsle` and an empty `sle` file (the inventory's
"unwired"); they stay as they are. A CR when a caller wants them.

## 7. Departures from the recipe, and risks

- **No new package** in phase 1 (the recipe's steps 3 to 6 do not apply);
  the change is three references, an import, a retain list and two prefix
  entries.
- **API growth only**: `alternateContent` properties on four DrawingML
  classes and a new member type in `CTDrawing.getEGAnchor()`'s list. A
  caller that walks anchors with an exhaustive `instanceof` chain (the
  `Xlsx4JAddImage` sample only adds one) sees `AlternateContent` and should
  ask `McSelection` for the branch.
- **`TraversalUtil`** already handles `AlternateContent` (CR-021: READ
  selects the branch, ALL visits both); nothing to add unless a walker
  special-cases `CTDrawing` (phase 1 checks the ALL-site list of CR-021
  §8.3 for DrawingML types).
- **Excel's empty Fallback** stays empty: kept whole, it round-trips; a
  reader in the default mode sees nothing, as before.
- **The chart's `c14:style`**: Word, PowerPoint and Excel charts all carry
  it, so this touches every document with a chart - the export-fo suite
  (charts render through the chart exporter) is the check that nothing
  downstream reads `getStyle()` and is surprised by `null` where the
  Fallback used to supply it. If something does, `McSelection.selectedContent`
  gives it the Fallback's `c:style` as before.
- **MOXy**: the three DrawingML schemas' packages are in every context; the
  core suite's context check.

## 8. Tests (recipe step 10)

`org.docx4j.dml.DrawingAlternateContentKeptTest` (docx4j-core-tests):

- `cr022-checkbox.xlsx`: every part forced to unmarshal, saved; `drawing1.xml`
  keeps its `mc:AlternateContent` with the a14 Choice's `twoCellAnchor`
  and the empty Fallback (before: a bare root); reloaded,
  `CTDrawing.getEGAnchor()` holds one `AlternateContent`, whose Choice
  requires `a14` and whose Fallback is empty.
- `cr022-slicers-timelines.xlsx`: the three anchors' `mc:AlternateContent`
  kept with both branches (Requires `a14`, `tsle`, `sle15`; Fallback `sp`),
  the `oneCellAnchor`'s too; a re-save opens in Excel 365 with the slicers
  and timeline **drawn as slicers and a timeline**, not as boxes (Jason -
  the gate that CR-022 could not have, since its re-saves left the
  drawing parts untouched).
- `loadAndSave.docx`, `.pptx`, `.xlsx`: the chart's `mc:AlternateContent`
  kept, `c14:style` typed in the Choice (an `org.docx4j.dml.chart.x2007`
  object), `c:style` in the Fallback; `McSelection.selectedBranch` gives
  the Fallback by default and the Choice with `preferChoice=c14`; a
  re-save of each opens in its application with the chart's style intact.
- `McSelectionTest` extended with a DrawingML host, if its fixtures are
  synthetic.
- `mc:Ignorable` on `styles.xml` naming `x16r2` declared on a re-save
  (`ExcelExtensionsTest.assertDeclares` already checks that part; it
  passes today because the marshaller declares a namespace in use - the
  table entry makes it explicit).
- The whole `docx4j-core-tests` and `docx4j-export-fo-tests` suites.

## 9. Follow-through and hand-offs (recipe step 11)

CHANGELOG under "Markup compatibility (CR-024)"; this CR per phase; CR-021
§8.6's leftover list and CR-022 §20 marked as taken up; the inventories
(`ms-xlsx-schema-inventory.md`'s drawing-shape item); objects-ts told the
three schema references and two prefixes (it regenerates once; its probe
is where this came from); the Python port told; core-ts told what the
oracle now keeps in drawings and charts.

## 10. Phases and gates

0. (Done above.) The measurement: the table of §1; the losses of §1.
1. **The admissions, the retain list, the prefixes, the tests**: one
   commit. Gate: the three losses of §1 closed under the forced round trip;
   core and export-fo suites green; Excel 365 opens the slicer and check
   box re-saves with the shapes drawn; Word, PowerPoint and Excel open the
   chart re-saves with the style intact.
2. **Follow-through**: CHANGELOG, this CR closed, the hand-offs.

## 11. Effort (rough)

Phase 1 half a day (the schema edits are small; the regeneration and two
suites are the time; Jason's four application checks).

## 12. References

- CR-021 (the policy; §8.6 items for DrawingML; §8.12 the lax wildcards),
  CR-022 §16 and §20 (the drawings measured, the loss recorded after the
  close), CR-023 (the undeclared-prefix repair).
- [MS-ODRAWXML] (a14, sle, sle15), [MS-XLSX] 5.7 (tsle), 5.11 (x16r2),
  [MS-OI29500] on `mc:Fallback` being empty.
- ECMA-376 Part 3 (markup compatibility).
