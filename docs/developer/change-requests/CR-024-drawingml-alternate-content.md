# CR-024: mc:AlternateContent in DrawingML hosts - spreadsheet drawings and charts kept whole, as CR-021 keeps WordprocessingML, SpreadsheetML and PresentationML

Status: DONE 2026-09-20 - both phases (§10; the follow-through in §9). Phase 1
landed 2026-09-20 in two commits (the admissions, the
retain list, six prefixes, `DrawingAlternateContentKeptTest`; the three losses of
§1 closed under the forced round trip; then the Excel check's finding fixed - every
root's `mc:Ignorable` prefixes declared, `IgnorablePrefixesDeclaredTest`; core and
export-fo suites green). Gate PASSED 2026-09-20: Word and PowerPoint opened their
chart re-saves; the three Excel re-saves were repaired at first (the Ignorable
finding, §10), re-cut after the fix, and opened clean. Proposed
2026-09-20 (Jason Harrop: "draft a DrawingML AlternateContent CR", after the
objects-ts session's round-trip probe found `cr022-checkbox.xlsx`'s drawing part
emptied by a load-and-save, and the loss was confirmed here).
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
| `xl/drawings/` | `xdr:oneCellAnchor` | a14 | `xdr:sp` | `xdr:sp` | `loadAndSave.xlsx` (phase 1 correction: the table first attributed it to the slicers fixture) |
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
| same | `EG_ObjectChoices` (the choice `CT_TwoCellAnchor`, `CT_OneCellAnchor` and `CT_AbsoluteAnchor` share for `sp`/`grpSp`/`graphicFrame`/`cxnSp`/`pic`) | `<xsd:element ref="mc:AlternateContent"/>` as a choice member | Excel writes it in the object's place (measured on `twoCellAnchor` and `oneCellAnchor`; `absoluteAnchor` shares the group, so admitted by construction) |
| same | `CT_GroupShape` (its own repeated choice of the same five - phase 1 found it does **not** use `EG_ObjectChoices`, as this table first said) | `<xsd:element ref="mc:AlternateContent"/>` as a choice member | as CR-021 admitted `p:grpSp` beside `p:spTree` |
| `xsd/dml/dml-chart.xsd` | `CT_ChartSpace` | `<xsd:element ref="mc:AlternateContent" minOccurs="0"/>` beside `style` | Excel writes the c14 style choice in `style`'s position (measured in all three chart parts) |

The JAXB shape: `CTDrawing.getEGAnchor()` is a `List<Object>` already (a
repeated choice), so an `AlternateContent` member joins the anchors, as it
joins `CTGroupShape.getSpOrGrpSpOrGraphicFrame()`; `CTTwoCellAnchor` and its
siblings gain an `alternateContent` property beside `sp`/`graphicFrame`;
`CTChartSpace` gains `alternateContent`. No existing accessor changes.

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

**Phase 1 finding - the entries matter on every re-save, not only a typed one.**
`mc:Choice` is a typed object (`AlternateContent.Choice`), so the
`xmlns:tsle` Excel declares *on the Choice* is not kept there; what keeps the
namespace in scope is its use inside the kept DOM (`tsle:timeslicer` in the
`graphicData`), which the marshaller hoists to the part's root under the
prefix the table gives the URI. With no table entry that prefix is `ns#`, and
`Requires="tsle"` names an undeclared prefix - CR-023's repair prompt, on
every re-save of a timeline. `sle`, `sle15` and `tsle` are therefore
required entries, not tidiness; the test asserts every `Requires` prefix is
declared in the saved part. (`x16r2`'s entry alone did not make `styles.xml`
declare it: the part also had to hand its Ignorable list to the declarator -
the Excel check's finding, §10.) (A Choice whose `Requires` prefix its content
never uses would still be undeclared unless the prefix is pre-declared, as
`a14` and `c14` are on every root; none such was measured.)

## 5. Kept as DOM, and the reader's view

`mce`'s minimal schema is a lax wildcard (CR-021 §8.12), so a kept Choice
or Fallback unmarshals its children typed only if they are **global**
elements of a bound schema. `xdr:twoCellAnchor`, `xdr:sp`,
`xdr:graphicFrame` and `c:style` are local elements (the spreadsheet
drawing schema declares only `from`, `to` and `wsDr` globally), so they
come back as DOM `Element`s - lossless on re-marshal (the element re-declares
the namespaces that were in scope at Excel's root, `xmlns:c16r2` on `c:style`;
not byte-for-byte, but nothing lost), which is the round-trip guarantee this CR
is for. `c14:style` is global in `org.docx4j.dml.chart.x2007` with a named
type, so it comes back typed inside a `JAXBElement` (`XmlUtils.unwrap`). This
is the same state CR-021 left `w:drawing`/`w:pict` in (`w:r`'s are local
elements too).

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

### Phase 2 record (2026-09-20)

- **CHANGELOG**: "Markup compatibility (CR-024 ...)" of 17.1.1: the hosts kept,
  the six prefixes, the Ignorable rule.
- **CR-021 §8.6** item 10 (chart `c14` styles the next candidate) and **CR-022
  §20**'s first leftover (the drawing shapes, `x16r2`, `oel`) marked taken up.
- **The inventory** (`ms-xlsx-schema-inventory.md`): 5.2 and 5.7's status
  (the shape's `mc:AlternateContent` kept whole, the prefix known; the
  schema still unbound) and the "xsd present, unwired" note.
- **Hand-offs** (messages to the sessions, 2026-09-20): objects-ts told the
  four schema references (`EG_Anchor`, `EG_ObjectChoices`, `CT_GroupShape`'s
  choice, `CT_ChartSpace`), the two imports, the six prefixes, and the
  Ignorable rule its probe can now check on every root; the Python port the
  same for its copy-based generation; core-ts told what the oracle keeps in
  drawings and charts, `McSelection`'s default, and the two save-path
  faults its own writer can have (a `Requires` prefix declared only through
  use; an Ignorable prefix bound to the default namespace).
- **Registry**: `docx4j/CR-024` and both phases done.

**Left for other CRs**: typing the branches (§5: `twoCellAnchor` and the
rest as global elements, with `a14:legacySpreadsheetColorIndex` and
`mc:Ignorable` on `CT_SRgbColor`); binding `sle`, `tsle`, `sle15` (§6);
`CT_OleObjects`' per-object `mc:AlternateContent` (CR-022 §20); running the
MOXy mapper's `getPreDeclaredNamespaceUris2` (written, not run here); the
xlsx4j VML gap (the forced re-saves skip `vmlDrawing` parts).

**Commits** (VERSION_17_1_1): d4d81eab2 (proposed, phase 0), bcb4c7f57
(phase 1), 85f37d05e (phase 1, the Excel check's fix), and this phase's.

## 10. Phases and gates

0. (Done above.) The measurement: the table of §1; the losses of §1.
1. **The admissions, the retain list, the prefixes, the tests**: one
   commit. Gate: the three losses of §1 closed under the forced round trip;
   core and export-fo suites green; Excel 365 opens the slicer and check
   box re-saves with the shapes drawn; Word, PowerPoint and Excel open the
   chart re-saves with the style intact.
2. **Follow-through**: CHANGELOG, this CR closed, the hand-offs.

### Phase 1 record (2026-09-20)

One commit. `dml-spreadsheetDrawing.xsd`: the `mce` import; the reference in
`EG_Anchor`, in `EG_ObjectChoices`, and in `CT_GroupShape`'s own choice (the
group does not use `EG_ObjectChoices` - §2 corrected). `dml-chart.xsd`: the
import; the reference beside `style`. `mc-preprocessor.xslt`: the six parents
and the `xdr`/`c` declarations. `NamespacePrefixMappings`: `x16r2`, `oel`,
`sle`, `sle15`, `tsle` both ways (the three shape prefixes were absent; §4's
finding says why they are needed). No context list changed: `org.docx4j.mce`
reaches every context through the element references, as it does for `w:r`.
No walker changed: nothing in docx4j-core walks an anchor list or a
`CTChartSpace` beyond the two part classes.

`org.docx4j.dml.DrawingAlternateContentKeptTest`, five tests: the check box
(one `AlternateContent` in `getEGAnchor()`, a14 Choice holding the
`twoCellAnchor`, empty Fallback kept as `<mc:Fallback/>`; `McSelection`
selects nothing by default and the shape with `preferChoice=a14`), the
slicers and timeline (three `twoCellAnchor`s with Requires a14/tsle/sle15,
`graphicFrame` Choice, `sp` Fallback, `xmlns:tsle` and `xmlns:sle15` declared
in the saved parts), the `oneCellAnchor` (§1 corrected: it is in
`loadAndSave.xlsx`), the chart style in all three formats (`c14:style` typed,
`c:style` DOM, the Fallback selected by default, the Choice with `c14`), and
the five prefixes both ways. The forced round trip skips the legacy VML
drawing of a form control, which docx4j cannot unmarshal (the xlsx4j backlog
item), as the ordinary path does.

Gate: the three losses of §1 closed under the forced round trip (the test);
`docx4j-core-tests` and `docx4j-export-fo-tests` green. Jason's opens
(`vbShares/Office 2016/fidelity/cr024-resaves/`): `loadAndSave-resaved.docx`
and `.pptx` opened with the chart's style intact; **the three `.xlsx` were
repaired** - not for the drawings, which Excel accepted, but for `styles.xml`
(`x16r2`), every worksheet and table (`xr3`), and the slicer, slicer cache and
timeline parts (`x`): their roots named a prefix in `mc:Ignorable` that nothing
declared (the CR-023 repair, on SpreadsheetML). Cause: only a part overriding
`JaxbXmlPart.setMceIgnorable` (every WordprocessingML root since CR-023; the
workbook) handed its Ignorable list to the prefix declarator, so any other
SpreadsheetML root declared a prefix only when its content happened to use
the namespace - which CR-022's fixtures did, so its `assertDeclares` passed.
The ordinary path never showed it (an untouched part is written from its
bytes); the forced re-save marshals every part.

Fix (the second commit): (1) `JaxbXmlPart.getMceIgnorable`/`setMceIgnorable`
read the root object's `getIgnorable()` when its class has one (reflection,
cached per class) - the general rule, so a root that gains `mc:Ignorable` in
the schema is covered without a part override; (2) the prefix `x`, which Excel
binds to the SpreadsheetML main namespace on its x14/x15 slicer and timeline
parts, is in the table's reverse direction (the forward direction keeps the
default namespace); (3) both runtime mappers implement
`getPreDeclaredNamespaceUris2` for the Ignorable prefixes the preferred-prefix
path cannot produce (`x`, whose namespace docx4j writes as the default) -
measured on jaxb-runtime 4: `xmlns="..."` and `xmlns:x="..."` on one root, no
duplicate attribute (the Java 6 failure that had kept the method unused arose
with every prefix returned). `IgnorablePrefixesDeclaredTest` re-saves eight
fixtures with every part unmarshalled and checks, on every root that carries
`mc:Ignorable`, that each prefix resolves. MOXy: the override is written the
same way but not run here (core-tests run on the RI).

Jason's second look (2026-09-20): the three re-cut `.xlsx`
(`cr022-checkbox-resaved.xlsx`, `cr022-slicers-timelines-resaved.xlsx`,
`loadAndSave-resaved.xlsx`) open properly in Excel. Gate passed.

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
