# CR-040: A histogram's bins kept - chartex `cx:binSize` and `cx:binCount` bound as Excel writes them (`@val`), not as [MS-ODRAWXML]'s schema has them (text)

Status: phase 2 PROPOSED 2026-10-15 (`cx:externalData`'s `cx:autoUpdate`, §7; Jason: "a CR-040 follow-up phase"); phase 1 DONE 2026-10-14 (§6); the Office check passed (Jason).  Proposed the same day, raised by the Enterprise session (plutext-enterprise-java-11-44)
from enterprise CR-004 phase 0's measurements on Excel 16.0.20326, and verified here the same
day.  Jason, 2026-10-14: "commit then implement CR-040 with its three recommended decisions" (D1
to D3, §3).  Owner: Jason Harrop.  One phase.

## 1. The finding

- **What Excel writes.** A histogram's or Pareto's bins:
  - `<cx:binning intervalClosed="r"><cx:binSize val="10"/></cx:binning>`;
  - `<cx:binning intervalClosed="r"><cx:binCount val="6"/></cx:binning>`.
  - Both are in files Excel saved itself, on the share at
    `office_charts/enterprise-cr004/variants/manual-binwidth-10.xlsx` and `manual-bincount-6.xlsx`.
- **What the schema says.** `xsd/odrawxml/office_drawing_2014_chartex.xsd` lines 405-409
  ([MS-ODRAWXML] revision 21.0, a88b49347) has `binSize` as an element of `xsd:double` and
  `binCount` of `xsd:unsignedInt`, with text content.  Microsoft's Open XML SDK has them the same
  way.  Excel refuses a file written so: the Enterprise session tried seven encodings, and only the
  `@val` forms open.
- **What docx4j does today** (measured here on those two files):
  - it logs an `UnmarshalException` ("For input string: \"\"") as it reads the chart part;
  - it saves `<cx:binning intervalClosed="r"/>`.
  - So **a histogram's bin width or bin count is lost on any load and save**, silently, and Excel
    then bins automatically.  Nothing in docx4j reads `cx:binning`; the loss is the round trip's.
- **The rest of the schema has no other such trap.**  Its other simple-typed elements are strings
  (`cx:v`, `cx:separator`, a header's or footer's text, `cx:copyright`, `cx:binary`), which are text
  in Office's files too.  Every other value-carrying element is already a type with an attribute
  (`CT_SubtotalIndex`'s `@val`, for one).

## 2. The change (the recipe, `docs/developer/adding-a-schema.md`)

- **Step 1, the schema: departure.**  It is patched from its source.  The xsd gets two types:
  - `CT_BinSize`, with `val` of `xsd:double`, required;
  - `CT_BinCount`, with `val` of `xsd:unsignedInt`, required.
  - `CT_Binning`'s two elements take them.  A comment at the change says what Excel writes, what
    revision 21.0 says, and this CR.
- **Steps 2 to 7 don't apply**: same namespace, same package, no new part, prefix or context.
  `org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex` gains `CTBinSize` and `CTBinCount`.
- **The API change (D2).**  `CTBinning.getBinSize()` returns a `CTBinSize` (was a `Double`), and
  `getBinCount()` a `CTBinCount` (was a `Long`).  These are public and released.  Nothing in docx4j
  calls them, and with the old types they could never hold Excel's value.  CHANGELOG entry.
- **Regeneration**, clean (`docx4j-generated-objects`).
- **Tests (core-tests).**
  - Excel's two histograms as fixtures (`charts/chartex/`).  Their data is synthetic: the forty
    "Score" values of the Enterprise session's `CR-004-support/chartex-probes.ps1`, the chart
    Excel's, the bins set by Jason in Excel; the Enterprise session agrees to their use.  Each
    read with `binSize` 10 or `binCount` 6, saved, and read back the same, its XML Excel's
    `val` form; no unmarshal error logged.
  - A `CTBinning` made in code marshals to Excel's form.
- **Office check (Jason's VM):** Excel opens each docx4j-saved file without repair, its bins as
  Excel's (Format Axis shows bin width 10 and number of bins 6).
- **Hand-offs:** docx4j-generated-objects-ts and docx4j-python bind the same xsd (each has its own
  copy, `modules/org_docx4j_com_microsoft_schemas_office_drawing_x2014_chartex.d.ts` and
  `schemas/odrawxml/office_drawing_2014_chartex.xsd`), so each gets the finding and the patch.

## 3. Decisions for Jason

1. **A CR of its own** (this one), not a phase of CR-039, which is done.  Recommended: it is a
   binding correction with a released API change and hand-offs, not part of the SPI.
2. **Typed elements** (`CTBinSize`, `CTBinCount`, as docx4j binds every other `val` element), not an
   adapter keeping `Double` and `Long`.  Recommended: consistent, and nothing calls the old getters.
3. **In 17.3.3**, beside CR-039.  Recommended: enterprise CR-004's phase 1 reads the bins through
   `Series.getSource()`, and every docx4j user's round trip of a histogram loses them today.

## 4. Not in scope

Reading `cx:layoutPr` (binning, statistics, visibility, aggregation, parentLabelLayout) into
`ChartExModel`.  The Enterprise jar reads it from the binding.  The Enterprise session's
measurements of Excel's defaults (box and whisker, Pareto, treemap, sunburst) are in its CR-004,
should the ports want a read model of them later.

## 5. Effort

Half a day, plus the Office check.

## 6. Implementation record (2026-10-14)

- **The xsd.**  `CT_BinSize` and `CT_BinCount` (`val`, required) are added before `CT_Binning`,
  whose two elements take them, with a comment naming revision 21.0, Excel's form and this CR.
- **The regeneration** was clean.  Against a checksum of every generated source taken before it,
  the change is `CTBinning`, the new `CTBinSize` and `CTBinCount`, and the chartex
  `ObjectFactory`.  Twelve VML classes, `dml.diagram.CTConstraint` and the episode file also
  differ, but they differ between two runs on the same xsd too: XJC orders attribute groups
  differently from run to run, and the episode carries a timestamp.  That predates this CR.
- **Fixtures.**  `charts/chartex/manual-binwidth-10.xlsx` and `manual-bincount-6.xlsx`, copied
  from the share, their metadata already anonymised ("Author A").
- **`ChartExBinningTest` (3).**
  - Excel's bin width and bin count are read (10, 6, `intervalClosed` r), saved as Excel's form
    and read back the same.
  - Bins made in code marshal to Excel's form.
  - Nothing is logged as an `UnmarshalException`.
- **docx4j writes `val="10.0"`** for a bin width (JAXB's `xsd:double`); the Office check says
  whether Excel takes it.
- **Hand-offs** sent to docx4j-generated-objects-ts-82 and docx4j-python-cd, both of which confirmed
  the trap in their own bindings.  Both wait for the commit's hash.
  - objects-ts: the facade reads and writes `<cx:binning intervalClosed="r"/>`; jsonix's
    unexpected-attribute hook can't see an attribute on an element bound as a simple type.
  - python: its engine keeps the bins (an lxml tree), but its generated binding drops them and
    raises on writing them back.

**Gate:**
- the reactor installed clean;
- core-tests 1558/0 (11 skipped), export-fo-tests 794/0 (9 skipped), docx4j-chart-render 23/0
  (the chart gate 531 of 665), docx4j-markdown 145/0;
- the corpora and the probes against b226 (b227, jars frozen 09:56): 0 movers, every event count
  equal, the probes' scoreboard byte-identical;
- **the Office check** passed (Jason, 2026-10-14), on the share's `fidelity/cr040-binning/` (the
  two files docx4j saved, and `CHECK.txt`): Excel opens each without repair, and shows bin width 10
  and 6 bins.  So Excel takes `val="10.0"`.

## 7. Phase 2 (proposed 2026-10-15): `cx:externalData`'s `cx:autoUpdate`

**The finding** (the Enterprise session, enterprise CR-004 phase 3, 2026-10-15; verified here the same
day). Word and PowerPoint write a chart's link to its embedded workbook as
`<cx:externalData r:id="rId1" cx:autoUpdate="0"/>`, the attribute **qualified** with the chartex
namespace. The xsd declares it unqualified (`CT_ExternalData`, under the schema's
`attributeFormDefault="unqualified"`), so `CTExternalData` binds `@XmlAttribute(name = "autoUpdate")`
with no namespace. Unmarshalling passes Office's attribute by, `isAutoUpdate()` is null, and docx4j's
save writes `<cx:externalData r:id="rId1"/>`. It is phase 1's mechanism: Office's form against the
published schema.

- **Where Office writes it:** in all 14 chartex parts on this machine that carry the element (the
  core-tests fixtures `charts/waterfall.docx` and `waterfall.pptx`, and the Enterprise session's
  Word and PowerPoint inserts), always qualified. No other chartex attribute is written qualified
  in them.
- **What it costs:** little. The Enterprise session's Office pass (Office 16.0.20326) opened every
  docx4j-saved chartex file without repair and drew it from the caches, and Office's own re-save
  writes `cx:autoUpdate="0"` back. It is a round-trip fidelity fix: a document docx4j loads and
  saves should keep what Office wrote.

**The change** (the recipe of §2):

- **The xsd:** `autoUpdate`'s declaration takes `form="qualified"`, with a comment naming Office's
  form and this CR, so that `CTExternalData` binds
  `@XmlAttribute(name = "autoUpdate", namespace = "http://schemas.microsoft.com/office/drawing/2014/chartex")`.
  Office's form is the only one seen; an unqualified attribute, should a producer write one, would
  then be dropped as Office's is today. Not worth a second binding unless one is found.
- **Regeneration**, clean, checked against the checksums of every generated source as phase 1 was.
- **Tests (core-tests):** both waterfall fixtures loaded and saved with no edit keep
  `cx:autoUpdate="0"`; `isAutoUpdate()` reads `false`; a `CTExternalData` made in code with
  `setAutoUpdate(false)` marshals to Office's form.
- **No API change:** `isAutoUpdate()`/`setAutoUpdate()` keep their types.
- **Office check:** optional, given the Enterprise session's result. A docx4j-saved waterfall that
  keeps the attribute should open as before.
- **Hand-offs:** docx4j-generated-objects-ts and docx4j-python, which bind the same xsd; the
  Enterprise session (enterprise CR-004's log refers to this phase).

**Effort:** two to three hours, including the regeneration and the gate (core-tests; the chart
gate).

