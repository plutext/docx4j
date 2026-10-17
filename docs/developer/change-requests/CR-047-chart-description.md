# CR-047: A chart's description - its kind, axis titles and each series' axis, beside `ChartTable`

Status: ACCEPTED 2026-10-18 (Jason: "D1 to D7, each with a recommendation - accepted"). Drafted the same
day at his request ("please draft CR-047"). Numbered 17 October 2026
(Jason: "this will be its CR-047"), as the extension of the text extraction paper's D18. CR-046 phase 2
depends on it for the probe's case 34 axis title. One phase (§5); decisions in §6.

## 0. Summary

`ChartTable` (CR-035 phase 3) describes a chart for a reader without the picture: its title and its
data, categories down and the series Office shows across. It leaves out what a reader needs to make
sense of the numbers:
- **what kind of chart it is** (a pie's numbers are shares; a stacked column's add up);
- **the axis titles** (a value axis titled "Revenue ($m)" over General-format numbers is the only place
  the unit is);
- **which axis each series is on** in a combination chart (a "Share" line on a secondary axis in
  percent, beside columns in dollars).

docx4j's chart model already reads all three: `ChartGroup.getKind()`, `getBarDirection()`,
`getGrouping()` and `getAxisIds()`; `ChartAxis.getKind()`, `getPosition()`, `isDeleted()` and
`getTitle()` (with Office's default "Axis Title"). This CR adds `ChartDescription`, which puts them
into words beside the `ChartTable`, so that the text model (WD REQ-511) and Markdown write the same
thing, and docx4j stays the oracle the ports follow. It also reads a chartex chart's kind and axis
titles, which `ChartExModel` does not read yet.

## 1. What exists (read 2026-10-18)

- **`ChartTable.of(ChartModel)` / `of(ChartExModel)`** (`org.docx4j.model.chart`, since 17.3.3): title,
  header, rows. Numbers are formatted by their format codes (`DataFormatter`, `Locale.US`), multi-level
  categories take a column each, and scatter and bubble charts get x, value and size columns.
  `ChartTableTest` pins it.
- **`WmlToMarkdown.chart`** writes the title as a paragraph and the table as a GFM table after the
  anchoring paragraph.
- **The model** (`ChartModel`, CR-035 phase 2, with JSON goldens in `charts/json/`): groups in document
  order with their kind, bar direction, grouping and axis ids; axes with their kind (category, value,
  date, series), position, deleted flag and title. `ChartTitle.getShownText()` gives Office's default
  where the file states none: "Chart Title", "Axis Title", or a single series' name.
- **`ChartExModel`**: series with their `layoutId` (`getLayout()`), data, hidden flag and name, and the
  chart's title. **No axes.** The binding has them (`chartex.CTAxis`, `CTAxisTitle`).
- **WD 0.7 REQ-511**: "the chart's title on its own line followed by its data as tab-separated rows,
  header row included: the content docx4j's `ChartTable` defines (...; docx4j CR-047 adds the chart
  kind, the axis titles and each series' axis)". REQ-707 limits extractor-added text to `[n]`,
  `[image: ...]`, `[page n]`, `[table: listed format]` and the metadata block, each before what it is
  about.
- **The probe's case 34** (`text-extraction/phase0/extraction-probe.docx`, `word/charts/chart1.xml`): a
  clustered column chart (`c:barChart`, `barDir` col, grouping clustered) whose value axis is titled
  `CHT34_AXIS_TITLE`. `expected-tokens.tsv` expects the token once in every view, 0 under
  `charts: false`, and the harness keeps the row pending until an engine claims capability `CR-047`.

Fixtures with what this CR describes:
- `charts/bar-chart.xlsx`: a clustered bar with both axis titles at Office's default, "Axis Title".
- `charts/p2-combo-secondary.xlsx`: a clustered column (Series 1, Series 2) on the primary axes and a
  line (Share) on a secondary value axis at the right, with a deleted secondary category axis.
- `charts/p2-3d-column.xlsx`: a 3-D column with a depth (series) axis.
- The other `p2-*` probes cover every kind, and `waterfall.*` and `chartex/*` the chartex charts.

## 2. Design

### 2.1 `ChartDescription`

A new class in `org.docx4j.model.chart`, beside `ChartTable`. **`ChartTable` is unchanged** (D1): its API
is public since 17.3.3, and its table is what REQ-511's rows are.

```java
ChartDescription d = ChartDescription.of(ChartModel m);    // and of(ChartExModel)
d.getKind();        // "clustered column and line": every group's kind name, in group order (§2.2)
d.getGroups();      // per group: its kind name, element kind, bar direction, grouping, series shown,
                    //   and whether it is on the secondary axes
d.getAxes();        // the shown axes, in reading order, each with its role (§2.3), position and title
d.getSecondarySeries(); // the series on secondary axes, by shown name (empty for most charts)
d.getTable();       // ChartTable.of(m), as before
d.getNote();        // the bracket note of §2.4: "[chart: clustered column and line; secondary axis: Share]"
d.toJson();         // the description as JSON, for the ports' goldens (§2.5)
```

**Primary and secondary.** A group is on the secondary axes when its value axis is not the first group's
value axis. Its series are the secondary series. That is how Office places a combination chart's second
group (`p2-combo-secondary`: the line's value axis is at the right).

### 2.2 The kind's name (D2)

Excel's English chart-type names, from the group's element, bar direction and grouping, the same
whatever the document's language:

| Element | Grouping or style | Name |
|---|---|---|
| `c:barChart`, `barDir` col | clustered / stacked / percentStacked / standard | clustered column / stacked column / 100% stacked column / column |
| `c:barChart`, `barDir` bar | as above | clustered bar / stacked bar / 100% stacked bar / bar |
| `c:bar3DChart` | as above | "3-D " before the name: 3-D clustered column, ..., 3-D column |
| `c:lineChart` / `c:line3DChart` | standard / stacked / percentStacked | line / stacked line / 100% stacked line; 3-D line |
| `c:areaChart` / `c:area3DChart` | standard / stacked / percentStacked | area / stacked area / 100% stacked area; 3-D ... |
| `c:pieChart` / `c:pie3DChart` | | pie / 3-D pie |
| `c:ofPieChart` | `ofPieType` pie / bar | pie of pie / bar of pie |
| `c:doughnutChart` | | doughnut |
| `c:scatterChart` | | scatter |
| `c:bubbleChart` | | bubble |
| `c:radarChart` | `radarStyle` filled / other | filled radar / radar |
| `c:stockChart` | | stock |
| `c:surfaceChart` / `c:surface3DChart` | `wireframe` | contour / wireframe contour; 3-D surface / wireframe 3-D surface |
| `cx:` series `layoutId` | waterfall, funnel, boxWhisker, sunburst, treemap, regionMap, clusteredColumn | waterfall, funnel, box and whisker, sunburst, treemap, map, histogram |
| `cx:`, a `paretoLine` series beside a `clusteredColumn` one | | pareto |

The grouping and the styles come from `ChartGroup.getSettings()` (`ofPieType`, `radarStyle`, `wireframe`)
and the bar direction and grouping from their getters. A chartex chart takes its first series' layout,
except that Excel's Pareto chart, a histogram and its `paretoLine`, is one kind.

Markers, smoothing and scatter styles are not named: they change the drawing, not how the numbers
read. A combination chart names its groups in order, joined by "and".

### 2.3 The axes (D3)

Only axes Office shows, and only their titles: an axis with `c:delete val="1"` is left out. Whether
Office then still draws a title the deleted axis carries is to be measured (§5's Office check); the
rule follows the measurement. Roles, in this order:
- for a category chart: category axis (`c:catAx` and `c:dateAx`), value axis, then secondary value
  axis and secondary category axis where shown, then depth axis (`c:serAx`);
- for scatter and bubble, both axes are `c:valAx`: x axis (the one at the bottom or top), y axis, and
  then secondary x and y axes.

A title is its shown text, Office's default "Axis Title" included, as `ChartTitle.getShownText()`
gives it. An axis with no `c:title` has none. Pie, doughnut and of-pie charts have no axes.

For chartex, `ChartExModel` gains the axes it lacks (id, hidden, title from `cx:axis/cx:title`), and the
same rule applies.

### 2.4 How text writes it (D4; a WD matter)

One bracket note per chart, before the chart's title, since REQ-707 puts extractor-added text before
what it is about:

```
[chart: clustered column; value axis: CHT34_AXIS_TITLE]
CHT34_TITLE
	CHT34_SERIES_ONE	CHT34_SERIES_TWO
CHT34_CAT_A	...
```

- The note always gives the kind. Then it gives each shown axis title as `<role>: <title>`, and, for a
  combination chart, `secondary axis: <series>, <series>`.
- An axis title is document text inside an extractor's bracket, as an image's alt text is inside
  `[image: ...]`.
- Examples: `[chart: pie]`; `[chart: clustered bar; category axis: Axis Title; value axis: Axis Title]`;
  `[chart: clustered column and line; secondary axis: Share]`.

This needs REQ-707 to admit `[chart: ...]` and REQ-511 to describe the note. That goes to the portfolio
session as a proposal; the WD decides the spelling. The block model carries the same facts as fields
(kind, axes, secondary series), never as text (REQ-707).

**Markdown** (D6): the note as a paragraph before the title, in `WmlToMarkdown` now, so that docx4j's two
text readers agree until CR-046 phase 3 moves Markdown onto the block model.

### 2.5 JSON for the ports (D5)

`ChartDescription.toJson()`, with a golden per chart part of every fixture, in `charts/description/`,
beside the model's `charts/json/`: `{kind, groups: [{kind, element, barDirection, grouping, series,
secondary}], axes: [{role, position, title}], secondarySeries, note}`. The table stays `ChartTable`'s.
docx4j-core-ts's CR-009 (proposed, not scheduled) ports the model; it takes the description with it
when scheduled.

## 3. What does not change

- `ChartTable`'s API and output, and so `ChartTableTest`.
- `ChartModel` and its JSON goldens. The model already holds everything; only `ChartExModel` gains axes,
  and its JSON gains them, so the chartex goldens are re-cut, axes added.
- Drawing (`docx4j-chart-render`), authoring (`ChartCreator`) and the HTML and FO exporters.

## 4. Consumers

- **CR-046 phase 2** (REQ-511): the text model writes the note, the title and the table after the
  anchoring paragraph, and the block model's chart object carries the description's fields. The probe's
  `CHT34_AXIS_TITLE` row then passes, and `TextsAdapter` claims capability `CR-047`.
- **`WmlToMarkdown`** (§2.4).
- **docx4j-mcp**, whose chart description tools can return `toJson()`.

## 5. The phase

**Build:**
- `ChartExModel` reads the axes.
- `ChartDescription` with §2.2's and §2.3's rules and `toJson()`.
- `WmlToMarkdown` writes the note.
- A CHANGELOG entry: Markdown's chart output gains a line.

**Tests:**
- `ChartDescriptionTest`:
  - `bar-chart.xlsx`: clustered bar, both default titles;
  - `p2-combo-secondary.xlsx`: clustered column and line; secondary axis: Share; the deleted
    secondary category axis left out;
  - `p2-3d-column.xlsx`: 3-D column with its depth axis;
  - a pie with no axes; `p2-scatter.xlsx`: x and y axes;
  - the probe's chart: `[chart: clustered column; value axis: CHT34_AXIS_TITLE]`;
  - a waterfall.
- Every `p2-*` probe's kind name against §2.2.
- The JSON goldens, written once by the implementation and reviewed by hand before they are committed.
- `WmlToMarkdown`'s chart test gains the note.

**Office check** (Jason, Excel): one workbook with a value axis that carries a title and is deleted
(`c:delete val="1"`). Does Excel show the title? §2.3's rule for deleted axes follows the answer. The
docx4j session builds the workbook and stages it on the share.

**Hand-offs:**
- the portfolio session: the WD proposal of §2.4 (REQ-511's note, REQ-707's `[chart: ...]`) and the
  registry (docx4j/CR-047, which docx4j/CR-046.2 depends on);
- docx4j-core-ts: its CR-009, when scheduled, ports the description and holds to its goldens;
- the text model: CR-046 phase 2.

**Acceptance:** the tests above, and the Office check's answer reflected in §2.3.

## 6. Decisions

**Decided 2026-10-18: Jason accepted every recommendation below** ("D1 to D7, each with a recommendation -
accepted"). Each item is kept as proposed, recommendation included, as the record. D4's form is proposed to
the WD, which decides its spelling (§2.4).

- **D1. Beside `ChartTable`, or in it.** Options:
  - a `ChartDescription` that holds the `ChartTable`;
  - new getters on `ChartTable` (`getKind()`, `getAxisTitles()`, ...).

  Recommended: beside. `ChartTable` stays the table that REQ-511's rows are, its output and its tests
  unchanged; the description is the context around it.
- **D2. The kind's vocabulary.** Options:
  - Excel's English chart-type names (§2.2), as a reader knows them;
  - the elements' own names ("bar, col, clustered").

  Recommended: Excel's names, in English whatever the document's language. Markers, smoothing and
  scatter styles are not named. The block model and the JSON also carry the element, bar direction and
  grouping, for a machine.
- **D3. Which axes.** Recommended: only the axes Office shows, titles as `getShownText()` gives them
  (Office's "Axis Title" default included), in §2.3's order and roles. Whether a deleted axis's title
  shows is settled by the Office check.
- **D4. Plain text's form.** Options:
  - a `[chart: ...]` note before the title (§2.4);
  - the axis titles as plain lines after the title, and the kind only in the block model;
  - the category axis title as the table's first header cell.

  Recommended: the note. It says which axis a title belongs to. It keeps the kind, which changes how a
  reader takes the numbers, in plain text. It follows REQ-707's bracket discipline, with the
  `[image: ...]` precedent for document text inside a bracket. The WD decides; this CR proposes.
- **D5. JSON goldens for the ports.** Recommended: yes, as CR-035 gave the model goldens, so that
  core-ts and python port the description to the same words.
- **D6. Markdown now.** Options:
  - `WmlToMarkdown` writes the note in this CR;
  - it waits for CR-046 phase 3.

  Recommended: now. The change is a paragraph, and docx4j's two readers would otherwise disagree for a
  release.
- **D7. Chartex.** Recommended: in scope. The kind comes from the series' `layoutId`, the axis titles
  from `cx:axis`, so that a waterfall or a histogram reads as one.

## 7. Risks

- **The kind names are English.** Office shows localized names, and so do its default titles ("Axis
  Title" is the English default, which docx4j already writes). A reader of another language gets English
  kind names. The JSON carries the element names too.
- **A combination chart with more than two groups**, or with the first group on the secondary axes.
  Office allows both. The rule (secondary = not the first group's value axis) names the groups apart
  correctly; `p2-combo-secondary` is the only fixture, so a second combination probe may be worth asking
  Excel for.
- **The WD may spell the note differently** (§2.4). Only `getNote()` and the goldens' `note` change.

## 8. Sources

- The text extraction paper, `docx4j-portfolio/docs/text_extraction_behaviour_model.md` (2d8fd36): row
  20a and D18, which named this CR.
- WD 0.7 (`docx4j-portfolio/docs/text_extraction_specification_wd.md`, b871d1f): REQ-511, REQ-706,
  REQ-707.
- CR-035 (the chart model, `ChartTable`); CR-039 (chartex); CR-046 §2.4 and phase 2.
- docx4j-core-ts CR-009, the chart read model (proposed 2026-10-12).
