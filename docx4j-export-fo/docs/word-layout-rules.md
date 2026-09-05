# Word's layout rules, and how docx4j reproduces them

How Word lays out a page, as measured against Word 365, and what docx4j does about it when
converting a docx to PDF via XSL-FO and Apache FOP. A reference, organised by topic: each
rule is stated once, with the measurement that established it and the property that turns
it off where there is one. Describes docx4j 17.0.6 with FOP 2.11.

For what to do when FOP is upgraded, see [README-word-layout.md](../README-word-layout.md),
which documents the copied `LineLayoutManager` and how to re-derive it.

---

## 1. Scope, method and switches

### 1.1 What this covers

PDF output via XSL-FO (`Docx4J.toPDF`, or `toFO` plus FOP). Both exporter pathways - the
visitor exporter, which is the default, and the XSLT one (`FLAG_EXPORT_PREFER_XSL`) -
behave the same unless a rule says otherwise. A few rules (paragraph borders, hidden text,
`w:keepLines`, caps, default cell margins) apply to HTML output too, and say so.

Nothing here needs an extra jar, bar hyphenation patterns (§4.7). The FOP layout managers
live in docx4j-export-fo, package
`org.docx4j.fop.wordlayout`, and are installed on every FopFactory docx4j builds through
the `FopFactoryCustomizer` SPI (`ServiceLoader`, `META-INF/services`). That SPI remains
available for an application's own FopFactory customisations.

### 1.2 How the rules were established

Each rule was measured before it was implemented: a synthetic probe document isolating the
rule is rendered by Word 365 on Windows (that PDF is the golden) and by docx4j with FOP;
the two PDFs' text lines are paired by longest common subsequence on the line text, and
line-break parity, page-break parity, baseline deltas and x deltas are reported, with a
rasterised overlay per page.

The harness is `docx4j-layout-fidelity` (in the repository, not in the reactor, not
deployed; see its README). 45 probes have Word goldens:

`spacing-adjacent`, `spacing-contextual`, `spacing-autospacing`,
`spacing-autospacing-context`, `spacing-page-top`, `spacing-section-start`,
`spacing-in-table`, `spacing-char`, `line-auto`, `line-exact-atleast`, `line-mixed`,
`break-ragged`, `break-justified`, `widow-orphan`, `kern-title`, `footnotes`,
`footnote-space-after`, `image-inline`, `image-anchored`, `ptab-right`,
`page-header-footer`, `page-header-footnotes`, `page-first-even-odd`,
`page-first-even-odd-heights`, `page-tall-header`, `page-landscape-margins`,
`table-fixed`, `table-autofit`, `table-width`, `table-span`, `table-nested`,
`table-floating`, `table-cellspacing`, `table-rowheight`, `table-indent-compat14`,
`table-indent-compat15`, `hyphenation`, `hyphenation-zone`, `tab-jc`, `pbdr-space`,
`table-grid-edge-compat14`, `table-grid-edge-compat15`, `table-autofit-wrap`,
`table-floating-anchor`, `columns-unequal`.

A rule a probe could not settle on its own was checked against a corpus of 194 real
documents scored against Word's own PDFs of them, so a change is accepted only when the
aggregate - documents with Word's page count, share of lines matching Word exactly, mean
and median per-document line parity - does not fall and no document regresses beyond
noise. Over the 17.0.5 cycle that went from 69.7% of lines matching Word exactly to 82.5%,
and from 128 to 152 of 190 scored documents having Word's page count.

### 1.3 Compatibility modes

Several of Word's rules changed with its 2013 layout engine. Word records which engine
lays a document out in `word/settings.xml`, as
`w:compat/w:compatSetting[@w:name="compatibilityMode"]/@w:val`; docx4j reads it with
`DocumentSettingsPart.getCompatibilityMode()`. **A document with no such setting is mode
12**, not mode 15 (verified against Word goldens), and takes the older rules.

Rules that depend on it: the table grid edge (§6.1), space-before after a hard page break
(§3.3), space-after at the bottom of a table cell (§3.5), and space compression on
justified lines (§4.2).

### 1.4 Properties

| Property | Default | Effect |
| --- | --- | --- |
| `docx4j.convert.out.fo.wordLayout` | `true` | Word's layout managers: greedy line breaking, Word's line box and leading placement, tab-stop resolution, justified-space compression. `false` restores plain FOP layout, and the `docx4j:` foreign attributes are then not written either. |
| `docx4j.convert.out.fo.wordLayout.maxSpaceShrink` | `0.24` | How far the spaces of a justified line may be compressed to pull one more word in, as a fraction of their natural width. Only read when `wordLayout` is on; set explicitly, it applies whatever the compatibility mode. |
| `docx4j.convert.out.fo.wordLayout.maxHyphenSpaceShrink` | `0.10` | The same, for taking a longer **hyphenation fragment** rather than a whole word; Word pays much less for one (§4.7). Capped by `maxSpaceShrink`. |
| `docx4j.convert.out.fo.wordLayout.hyphenationZone` | `false` | `true` enforces `w:hyphenationZone` as the largest gap tolerated before hyphenating, which is what docx4j did to 17.0.5. Measured against Word, the zone never fires (§4.7). |
| `docx4j.convert.out.fo.wordLayoutFixups` | `true` | The DOM pass over the generated FO (`WordLayoutFixups`): Word's spacing edge rules, the line-box attributes, exact-height rows, anchored pictures, text boxes. `false` gives the FO docx4j 17.0.4 produced. |
| `docx4j.convert.out.fo.kerning` | `false` | `false`: fonts are declared unkerned, with a kerned twin that only the runs Word kerns are sent to (§5.4). `true`: every font kerns, as before 17.0.5. |
| `docx4j.convert.out.fo.ligatures` | `false` | `false`: Latin runs asking for neither ligatures nor kerning are set in a `+noliga` declaration to which FOP applies no OpenType feature (§5.5). `true`: FOP's own behaviour, GSUB `liga` everywhere. |
| `docx4j.convert.out.fo.tables.position` | `true` | A floating table's `w:tblpPr`: the grid edge at `tblpX`/`tblpXSpec`, and a page- or margin-anchored table which opens its section placed absolutely (§6.8). `false` lays every table out in the flow, as 17.0.5 did. |
| `docx4j.convert.out.fo.pictures.float` | `true` | Whether a picture Word wraps text around may be an `fo:float`. `false` lays such pictures out in the flow (no text beside them, but immune to the FOP float defect, §10). Text boxes are never floats whatever this says. |
| `docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.ImageMagickExecutable` | unset | Names an ImageMagick/GraphicsMagick executable. Set, a picture FOP cannot paint (EMF) is converted to PNG and painted; unset, its space is reserved but it is not drawn (§9.4). |
| `docx4j.convert.out.fo.pictures.convertDensity` | `300` | Pixels per inch that converter rasterises a metafile at. |
| `docx4j.convert.out.printHiddenText` | `false` | Hidden text (`w:vanish`) is not rendered and takes no space, as Word prints it. `true` renders it. PDF and HTML. |
| `docx4j.convert.out.fo.hyphenate` | unset | Overrides the document's own `w:autoHyphenation`: `true` hyphenates every paragraph that does not suppress hyphenation, `false` hyphenates nothing. Unset, the document decides (§4.7). |

The foreign attributes the layout managers read are in the namespace
`http://docx4j.org/fop/word-layout`, registered with FOP by `WordLayoutElementMapping` so
that FOP keeps them rather than rejecting the FO. They are written only when the installed
`FopFactoryCustomizer` reports that namespace, so `wordLayout=false` leaves the FO free of
them. They are listed in the appendix.

---

## 2. Line height and leading

### 2.1 The pitch of a single-spaced line

Word's "single" spacing is neither 1.2 x the font size (XSL-FO's `normal`) nor the
ascender/descender FOP places its own baselines from. It is GDI's text metric height plus
external leading, from the font's `hhea` and `OS/2` tables:

```
tmHeight          = usWinAscent + usWinDescent
tmExternalLeading = max(0, (hhea.ascender - hhea.descender + hhea.lineGap) - tmHeight)
single            = (tmHeight + tmExternalLeading) / unitsPerEm * fontSize
```

Verified against Word 365: Liberation Serif 13.80pt at 12pt, Carlito 13.44pt at 11pt,
Liberation Sans 11.52pt at 10pt, DejaVu Sans 11.64pt at 10pt. `org.docx4j.fonts.
WordLineMetrics` computes it; the pitch agrees with Word's to 0.05pt over four fonts and
five multiples. Sizing lines as a percentage of the block's font size instead (12pt single
came out at ~11.7pt) put 15-20% more lines on a page than Word has.

### 2.2 `w:spacing/@w:lineRule`

- **`auto`**: `single` x `w:line`/240.
- **`exact`**: `w:line`/20 pt, whatever the font.
- **`atLeast`**: the larger of `single` and `w:line`/20 pt.

### 2.3 Where the leading goes

For an `auto` multiple, Word puts everything beyond the single-spacing pitch **below** the
text and **drops it at the bottom of a page**: the last line of a page fits if its text box
does, and a page's first line starts with its ascent. Measured: Word let 10.4pt of an
11.6pt leading hang below the bottom margin. FOP centres the leading, so with 1.5 or double
spacing the page's last line moved to the next page and every baseline sat half a leading
out.

Each paragraph block carries `docx4j:line-box` (the text box), `docx4j:baseline` (ascent +
external leading) and `docx4j:line-rule`. `WordLineLayoutManager` makes each line that box
and emits the leading as glue after the break possibility that follows the line, so FOP's
page breaker discards it when it takes the break; `WordFlowLayoutManager` moves a
paragraph's last leading behind the break the flow adds after the block, and
`WordListItemLayoutManager` does the same for a list item (FOP's list-item manager
otherwise folds that glue into its boxes, so the last bullet of a page would not fit when
its text does). Result: baselines within 0.1pt of Word's on every prose probe, median
0.02pt, from a median of -1.4pt.

For **`exact`**, the baseline sits at `usWinAscent / (usWinAscent + usWinDescent)` of the
box and the line is clipped to it - measured 0.80 for Liberation Serif at 9, 12 and 24pt,
where the metric ratio gives 0.81. For **`atLeast`**, a shortfall goes above the text. An
`auto` multiple below 1 shrinks the box from the top. (`line-exact-atleast` within 0.1pt,
from a 0.75pt median and 4.6pt maximum.)

### 2.4 A line's height comes from the runs on it

Word sizes a line from the largest ascent and the largest descent among the runs on it, not
from the paragraph's font: a line holding a 24pt run in a 10pt paragraph is 27.6pt
(`line-mixed` within 0.4pt, was 5.8), a footnote's first line is sized by the 11pt number
it holds (`footnotes` within 1.6pt, was 4.8), and a line holding only a picture is the
picture's height with no descent (`image-inline` within 0.3pt, was 2.9). A raised or
lowered run counts its full height, not its shifted height. Each run's `fo:inline` carries
Word's pitch for its own font and size, and the line manager takes the maximum; a line with
no runs takes the paragraph's font.

<a id="s24pic"></a>The line box the line manager works from is written by
`applyBlockLineHeight`, which sizes it from the **text** runs on the line - so a paragraph
whose only content is a picture had none at all, and the line manager then left the line
to FOP, which puts the paragraph font's descent below the picture. Measured on a real
document: a 67.5pt logo above a 14pt paragraph put the next baseline at y=298.2 against
Word's 293.9. `WordLayoutFixups.imageOnlyLineBox` gives such a paragraph the picture's own
height as its line box (an *inline* picture only: one about to be lifted into a positioned
container takes no line of its own, and its paragraph gets the paragraph mark's line,
[§2.5](#s25)).

**A picture's size is fractional.** `wp:extent` is in EMU, and 12700 EMU is exactly one
point, so a picture Word sizes at 857250 EMU is 67.5pt. docx4j wrote `content-height` and
`content-width` with `Integer.toString`, which threw the fraction away - "67px", which FOP
reads as 67pt at its default 72dpi - and everything below the picture moved up half a
point. 291 such attributes in 75 of 156 corpus documents. The size is now written in
points, with its decimals.

<a id="s25"></a>
### 2.5 The paragraph mark

Word ignores the paragraph mark's size when sizing the lines of a **non-empty** paragraph
(a 36pt mark on a 12pt paragraph used to give 36pt lines). An **empty** paragraph keeps the
mark's font and size, and is one line high.

"Empty" means *nothing on a line*, not "no runs": a paragraph whose runs came to no inline
content gets the same line. Two shapes reach the FO as a block FOP can build no line area
from - a run holding only an empty `w:t`, and a run whose only content is an anchored
picture or a text box, which is lifted out into a positioned container. Measured: a
document whose first body paragraph holds only a `wrapNone` anchored picture had every line
15.44pt (that block's own line height) above Word's, and a table whose three spacer rows
each hold one paragraph with an empty `w:t` lost 33.7pt of Word's row height at the third
line of the document. `WordLayoutFixups` gives such a block the same
`white-space-treatment="preserve"` space a run-less paragraph gets; it already carries the
mark's font and line-box attributes. A paragraph all of whose runs and whose mark are
hidden text still gets no line ([§9.3](#93-hidden-text)).

### 2.6 Superscripts and subscripts

`w:vertAlign` is drawn at 65% of the run's size, raised by 0.36 of that size, or lowered by
0.16 for a subscript (measured). The FO root carries
`line-height-shift-adjustment="disregard-shifts"`, because FOP otherwise grows the line by
the shift - one footnote reference made its line 8-10pt taller.

### 2.7 Line height when a substitute renders the font

Word takes `usWinAscent`, `usWinDescent` and `lineGap` from **the font the document
names**, installed or not. A metric-compatible clone matches in advance widths but not
always vertically: Caladea's single-spacing factor is 1.300 to Cambria's 1.172, DejaVu
Serif's 1.164 to Symbol's 1.225, Cousine's 1.133 to Consolas's 1.171. Sizing lines from the
substitute made every heading, bullet and code line drift.

docx4j therefore ships the vertical metrics of **512 Microsoft font families**,
`org/docx4j/fonts/word-line-metrics.properties` in docx4j-core, which `WordLineMetrics`
consults before the physical font's own tables. One line per family:

```
family = unitsPerEm;usWinAscent;usWinDescent;hheaAscender;hheaDescender;hheaLineGap
```

Keys are lower-cased and taken from both the legacy family name documents use in
`w:rFonts` (name table id 1, e.g. `calibri light`, `aptos display`) and the typographic
family (id 16). The table was read from the font files of an Office 365 installation
together with Office's downloaded cloud fonts (`FontCache/CloudFonts`), where Microsoft
365's current default fonts live.

So the line manager can apply this per run, the run font selector stamps the document font
on each span (`docx4j:font`): a run of Tahoma set in Arimo keeps Tahoma's line height. A
font that cannot be read at all falls back to a factor of 1.2, FOP's `normal`.

<a id="s27alias"></a>**A document font in neither the table nor the machine** falls back to
the physical substitute's own metrics, and those are not the metrics of the font it stands
in for. Measured on a 9pt single-spaced Helvetica document: Word's line pitch is 10.34pt
(1.149 em) and docx4j's was 12.89pt, +24.6% on every line, 249 reference lines against our
320, five Word pages against six. The 12.89pt is Arimo's OS/2 `usWinAscent`/`usWinDescent`,
2136/797 over 2048 units, a factor of 1.432; Arial's are 1854/434 with a 67-unit external
leading, 1.150 - which is what Word gives Helvetica, because Windows substitutes Arial for
it. So the rule is not "use hhea when the win metrics look large": it is that Word uses the
metrics of the font it substitutes, and `WordLineMetrics` therefore carries a short list of
such aliases (Helvetica and Helvetica Neue take Arial's row). A font with no entry and no
alias still takes the physical font's, which is all there is to go on.

### 2.8 List labels

Word raises a list item's first line by the amount the label's ascent exceeds the text's -
**unmultiplied** (the paragraph's line multiple is not applied to it) and **ignoring the
label's descent**. Measured: a Symbol bullet on Calibri 11pt makes the line 16.04pt rather
than 15.44pt; a Courier New `o`, whose descent exceeds Calibri's but whose ascent does not,
leaves it at 15.44pt. The label block gets the combined box and baseline, and the body
block carries `docx4j:label-ascent` for the line manager to add after the multiple.

---

## 3. Paragraph spacing at the edges

Word combines space-before and space-after by "larger of". The rules below are about where
a space is kept or dropped; all are applied by the `WordLayoutFixups` pass over the
generated FO (`docx4j.convert.out.fo.wordLayoutFixups=false` skips them).

**Page top and section start.** Word applies a paragraph's space-before at the top of the
first page of a section; XSL-FO discards space at the start of a reference area, so it has
to be put back. At a section start the first paragraph's space-before is reduced by the
space-after of the previous section's last paragraph: measured, 36pt before, after 0 / 10 /
20pt of after, gives 36 / 26 / 16pt; 6pt before after 20pt of after gives 0.

**HTML auto spacing is the exception there.** `w:beforeAutospacing` is a margin, and a
margin collapses out at the top of the body: measured on a document whose first paragraph
carries it, every line of page 1 was exactly +14.0pt (Word 73.5 / 86.7 / 98.2 / 109.7 /
121.3 / 132.8, docx4j 87.5 / 100.7 / 112.2 / 123.7 / 135.2 / 146.7) and its 9 Word pages
were 10. An explicit `w:spacing w:before` is honoured at a flow start - that is what the
`spacing-page-top` probe measures, 36pt on the first paragraph of a document - so only the
automatic value is dropped. The first flow block carried a retained space-before in 17 of
99 documents of the long-document corpus.

**Contextual spacing.** `w:contextualSpacing` (ECMA-376 17.3.1.9) zeroes the gap between
two same-style paragraphs when **either** carries it, not only on the flagged paragraph's
side: a contextual paragraph followed by a non-contextual one of the same style with 12pt
before gets no gap.

<a id="s33"></a>**Hard page breaks.** Word breaks the page **at** the break: where a
`w:br w:type="page"` follows content in its own paragraph, what precedes it stays on the
page it is on and what follows opens the next. docx4j moved the break to the front of the
paragraph (`w:pageBreakBefore`, the `w:br` dropped), which took the text before it to the
next page as well: measured with `mutool draw -F trace` on a document whose cover picture
and page break share a paragraph, Word puts the 270x225pt picture on page 1
(`transform="270 0 0 225 162.65 347.59"`) and docx4j put it on page 2 at y=80.58, so every
line of page 2 sat 268.4pt low and 54 Word pages came out as 44. 21 of 99 documents of a
corpus of long real documents hold such a paragraph. The paragraph is split in two at the
break (`convert/out/common/preprocess/PageBreak`), and because the two halves are one
paragraph nothing is doubled between them: the first keeps the space-before and loses the
space-after, the second the other way about, and the second takes neither the numbering
label - Word numbers the paragraph once - nor the first-line indent. A `w:sectPr` belongs
to the paragraph's end, so it goes with the second half.

A paragraph holding only a page break leaves no empty
line at the top of the new page. The next paragraph's space-before is dropped there **from
compatibility mode 15**, and kept below mode 15. The break moves to the next paragraph, or
to the next container which takes no space (a floating table or a picture already positioned
out of the flow, §6.8 and §9.1) - measured on `table-floating-anchor`, where the page of a
page-anchored table began with an empty line docx4j put 25.5pt above Word's first paragraph.
It does not move to a **table**: measured on a corpus document whose hard break is followed
by one, Word keeps that line, and dropping it lost a page of the nineteen.

**A page break inside a table** belongs to the table, not to the paragraph: Word takes a
`w:pageBreakBefore` on the paragraph which opens the table, and ignores one anywhere else in
it. Measured on two documents: a mail-merge template with sixteen of them spread over the
rows of a single table is four pages in Word and was twelve here, the extra pages carrying
one line each; a report with one on the first paragraph of each of two tables is five pages
in Word, and is five only because those two breaks are taken. `WordLayoutFixups` moves an
opening break to the `fo:table` and drops the rest (FOP otherwise breaks the table wherever
it finds one in a cell). A nested table cannot carry the break - it would land inside the
outer table - so there it is dropped.

**HTML auto spacing.** `w:beforeAutospacing` / `w:afterAutospacing` is **14pt**, combined
by "larger of" like any other spacing, and honours `w:doNotUseHTMLParagraphAutoSpacing`. It
is dropped between consecutive list items (so a list gets 14pt before the first item and
after the last) and at the top and bottom of a table cell.

An explicit `w:beforeAutospacing="0"` in direct formatting switches off the
`w:beforeAutospacing="1"` of the style the paragraph uses, and likewise for
`w:afterAutospacing`. That needs the attribute's three states - true, false, absent - which
XJC's `isBeforeAutospacing()` cannot report, since it returns a primitive `boolean`
(`org.docx4j.wml.AutospacingAccess` reads the field itself). Style resolution carried only
true until 17.0.6, so the override did nothing: measured on a document whose `NormalWeb`
style carries it and which overrides it on 20 paragraphs, 17 came out 14pt low - Word's
first divergence at y=171.4, docx4j's at 186.2, and the next gap 30.7pt against 58.9.

<a id="s35"></a>**Table cells.** A paragraph's space-before applies at the cell top, and its
space-after at the cell bottom in compatibility mode 15. XSL-FO drops space at the end of a
reference area, so `WordLayoutFixups` pins it there with
`space-after.conditionality="retain"` - and the cell's edges are boundaries
`w:contextualSpacing` cancels the space at, the **only** paragraph of a cell included.
Cancelling it walked the cell's paragraphs in pairs, so a single-paragraph cell was never
examined at all: measured on a planner whose cells hold one contextual paragraph each
against docDefaults `w:after="200"`, Word's row pitch is 10.1pt (the 9.199pt line box plus
`w:trHeight` 199) and docx4j's was 19.9pt, +9.0pt on the first row and +9.5 on every row
after it - 37 Word pages came out as 43.

**The paragraph a nested table forces.** OOXML requires a `w:p` after a `w:tbl` inside a
`w:tc`, and Word gives that one no line at all. Measured on a mode-14 header whose outer
row 1 holds three nested rows (baselines 23.5 / 33.8 / 43.9, pitch 10.2): Word's next outer
row starts at 54.7, 10.8pt later, with no room for an 11.5pt line, where docx4j went 43.7
-> 64.8. The header table then ended 28.9pt low and the body started 35.7 to 37.0pt low,
which turned Word's two pages into four. Only the *cell-final* paragraph that follows the
table: an empty paragraph anywhere else in a cell keeps its line, as [§2.5](#s25) says.

**Borders as padding.** A paragraph's borders and shading are resolved through the style
hierarchy, not read only from its direct `w:pPr` (Word's default Title style has a bottom
border). A border's `w:space` - the gap between the text and the border - is the padding on
that side: measured, Word's Title with 4pt space and a 1pt border starts the next paragraph
5pt lower, and so does docx4j, to 0.1pt. PDF and HTML.

<a id="s3pbdr"></a>That is the rule **above and below**, and it holds inside a table cell
too. Measured on the `pbdr-space` probe (a 0.5pt border at `w:space` 0, 1 and 4pt, in the
flow and in a narrow cell): Word adds the border and the space above and below the
bordered paragraph, and between two consecutive paragraphs sharing a border, one border
and one space. docx4j's baselines track Word's to 0.1pt across each of the probe's three
groups; adjacent baselines wobble by up to 0.3pt in either direction, which is the
goldens' own rounding - Word draws a 0.5pt border 0.48pt wide. (A real document had
suggested Word adds nothing at all in a cell; it does.)

**Left and right borders cost no text width.** Word draws a paragraph's left and right
borders **outside** the text area and `w:space` widens that gap rather than narrowing the
text: measured on the same probe, the bordered paragraph's text starts at x=72.0 in the
flow and 77.8 in the cell for `w:space` 0, 1 and 4 alike - the same x as an unbordered
paragraph - and its right border is drawn 1.8pt past the cell's own content edge. FO puts
them outside too, for a block whose `start-indent` is inherited, so nothing had to change
here; what did break the probe was the **cell's** borders (§6.3).

**Container wrappers.** Adjacent paragraphs sharing a border or shading are wrapped in one
block by the `Containerization` preprocess, and that wrapper is built from the **first**
paragraph's properties, its spacing included. Space is combined by "larger of", so the
duplicate normally costs nothing - but where a rule above removes a paragraph's spacing the
wrapper's copy survives and puts the gap back. Measured: a planner whose shaded cells carry
`w:contextualSpacing` against 10pt of docDefaults space-after had every row 9.5pt too tall
(Word's row pitch 36.5 -> 46.6 -> 57.1, docx4j's 35.7 -> 55.6 -> 75.3) and 37 Word pages
came out as 43. The wrapper's space-before and space-after now follow its first and last
paragraph.

**List items.** Space-before and space-after belong on the `fo:list-block`, not on the
block inside `fo:list-item-body` where FOP does not apply them.

**Widow control.** `w:widowControl` off maps to `widows="1" orphans="1"`; on is the default
on both sides. Word's widow and orphan decisions match FOP's once the line breaking does
(`widow-orphan`).

<a id="s39"></a>**Keeping a paragraph's lines together.** `w:keepLines`, which every
built-in heading style sets, maps to `keep-together.within-page="always"` in FO and
`page-break-inside: avoid` in CSS. Without it a heading broken over two lines by a `w:br`
could straddle a page, because the `w:br` is a nested block, which ends FOP's line sequence
and puts widow control out of reach (§10).

<a id="s310"></a>**Space-after against a footnote area (open).** One measured data point,
not yet a rule docx4j applies: where a paragraph's last line would fit at the foot of a
page but its space-after would reach into the footnote area, Word declines that line (its
box fitted by 5pt without the 8pt space-after), and widow control then moves the line
before it as well. FOP discards space-after at any region end, so it keeps the line. The
`footnote-space-after` probe (24pt space-after against a footnote area, and against a plain
page bottom as a control) exists for this.

---

## 4. Line breaking and justification

Applied by the layout managers in `org.docx4j.fop.wordlayout`; all off with
`docx4j.convert.out.fo.wordLayout=false`.

### 4.1 Greedy breaking

Word breaks lines greedily (first fit): a line takes every word that fits and breaks at the
last opportunity before the first word that does not, with no look-ahead; a trailing space
hangs past the end of the line. FOP's Knuth-Plass algorithm optimises the whole paragraph,
so with identical fonts and widths the two break about a quarter of ragged-right lines
differently, and every later line and page moves. Because FOP exposes no first-fit option
(its breaking algorithm is a private inner class of `LineLayoutManager`), docx4j-export-fo
carries a copy of FOP 2.11's `LineLayoutManager` with a greedy loop. Measured: ragged prose
line parity 72% -> 100%, justified 63% -> 98%.

### 4.2 Space compression on justified lines

Word compresses a justified line's spaces - to about three quarters of their natural width,
the glyphs unchanged - to pull one more word onto the line, and does so **only from
compatibility mode 15**. Measured across the corpus goldens, 3,498 justified lines: in mode
15, 34 of 50 documents have lines whose spaces are down to 0.76 of their natural width; in
modes 11, 12 and 14, and in documents with no mode at all, not one line in 2,102 goes below
0.94, which is within the goldens' own rounding.

`docx4j.convert.out.fo.wordLayout.maxSpaceShrink` is the limit as a fraction of the spaces'
natural width; the default 0.24 is the value at which the justified probe breaks 98% of its
lines as Word does (0.20 gives 78%, 0.30 gives 74%). For a document below mode 15 docx4j
writes `docx4j:space-shrink="0"` on `fo:root` and the line manager caps the allowance with
it. Setting the property explicitly applies it to every document.

### 4.3 Break opportunities Word does not take

Word does not break after a solidus, where UAX #14 lets FOP break: a URL, or a pair of
words joined by a slash, goes whole to the next line. Word does not break a line at a tab
either.

A literal U+00AD in a `w:t` is dropped from the FO: FOP does not break at a soft hyphen and
most fonts have no glyph for one, so it was painted as a notdef box in the middle of a
word. Word shows one only where it breaks the line there. A `w:br` at the end of a
paragraph gets the empty line Word gives it.

### 4.4 Tab stops

Where a tab starts is not known when the FO is written, so a mid-line tab is an `fo:leader`
of no length which the line manager sizes when the greedy loop reaches it; the paragraph's
block carries the stops it needs (`docx4j:tabs`, as `pos:align:leader` in twips from the
left margin, plus `docx4j:tab-default` and `docx4j:tab-ind`).

The stops, measured against Word's PDFs:

- the paragraph's `w:tabs`, the numbering's tabs, and the implicit stop a hanging indent
  makes at the left indent; beyond the last of them a grid at `w:defaultTabStop` (720 twips
  where absent);
- **all measured from the left margin**, not from the paragraph's indent;
- a custom stop clears the default grid stops before it, and the grid resumes past the last
  custom stop;
- a stop exactly at the current x is not the next stop.

What each alignment does with the text between this tab and the next (or the paragraph's
end): **left** - the text starts on the stop; **right** - the text's end sits on the stop,
its trailing space hanging past it; **centre** - the text's middle sits on the stop;
**decimal** - its `w:decimalSymbol` (default `.`) sits on the stop, and text holding none
is right-aligned on it.

Word cannot move backwards: a stop the text has already passed, and a right or centre stop
whose text does not fit before it, advance nothing. A stop beyond the paragraph's right
indent is still honoured, and the line runs into the indent rather than wrapping. A line
holding a tab is **sized** from the left indent whatever the paragraph's `w:jc` - the stop
a tab reaches is the same one it would reach on a left-aligned line - and the line, its
tabs' widths counted in, is then **aligned as a whole** by the `w:jc`. Measured: six
consecutive tabs advance 216pt in Word, where the former three-no-break-space stand-in
advanced 54pt.

<a id="s44jc"></a>**Alignment.** Measured on the `tab-jc` probe (A4, Times New Roman 12pt,
1in margins: a 451.3pt line, centred on 297.65, ending at 523.35). A trailing tab after
87.7pt of text takes 20.3pt to reach the 180pt default stop, so the line is 108pt: Word
draws the text at 243.7..331.4 centred (the 108pt line centred on 297.65) and at
415.6..496.6 right-aligned (the 108pt line ending at 523.35). With a custom left stop at
6000 twips the line is 300pt whatever text it holds, and Word starts it at 147.7 centred
and 223.5 right-aligned. A leading
tab is a fixed leader inside the line and was already aligned with it; a mid-line tab is
sized from the left indent too, and the whole line is then aligned. A **justified**
paragraph is the exception: the tab absorbs the slack and Word lays the line out from the
start (x=72 in the probe). A stop that reaches past the available width fills the line, so
`w:jc` cannot move it any further - Word cannot move backwards.

docx4j drew every such line flush left before 17.0.6 (`tab-jc` max dx 372.4pt -> 0.36pt).
Three documents of a 40-document corpus slice have a centred or right-aligned paragraph
containing a tab.

**Leaders.** `w:leader` `dot` and `middleDot` draw dots (FOP repeats the font's own dot, as
Word does; a dotted rule does not match); `hyphen`, `underscore` and `heavy` draw a rule;
anything else nothing. Which stop a tab will reach is unknown when the FO is written, so
the n-th tab of a paragraph takes the leader of the n-th stop, and the line manager blanks
a leader whose resolved stop has none.

**A leading tab** - one before any visible content on the line - is instead an `fo:leader`
of fixed length to the next stop, computed at FO-generation time, so code blocks and
hanging first lines indent as Word indents them. A fixed-length leader does not set
`text-align-last="justify"`; only a stretching one needs it.

**Table-of-contents entries** (first stop right-aligned with a dot leader) keep the
stretching leader and `text-align-last="justify"` they have always had: their stop is the
right margin, and only a stretching leader can absorb the width an unresolved
`fo:page-number-citation` loses when it resolves. See §10 for the limitation this leaves.

A right `w:ptab` is resolved as a right tab stop at the end of the line, by the same line
manager. With `wordLayout=false` no stops are written, and a mid-line tab keeps the
three-space stand-in.

### 4.7 Automatic hyphenation

Off unless the document asks for it: `w:settings/w:autoHyphenation`. A paragraph whose
effective `w:pPr` carries `w:suppressAutoHyphens` is never hyphenated. The rules the line
manager then applies, all of them Word's:

- **A word that does not fit is hyphenated**, at the **last** hyphenation point that fits
  (greedy, like the line breaking itself).
- **`w:consecutiveHyphenLimit`** caps how many lines in a row may end in a hyphen; 0, and
  the absent case, mean no limit. Counted within a paragraph, which is as far as one line
  manager sees.
- **`w:doNotHyphenateCaps`** leaves a word written entirely in capitals whole. Applied
  where the whole word is known, so no hyphenation point is inserted in it at all.
- The **last line** of a paragraph cannot end in a hyphen, because the greedy loop's last
  break is the paragraph's own forced break. Word has no rule against hyphenating the
  *second to last* line, and neither does this.

<a id="s47zone"></a>**The hyphenation zone does not fire.** `w:hyphenationZone` is
documented as the largest gap Word tolerates at a line end before it hyphenates, and
docx4j applied it that way until 17.0.6. Measured against Word 365's goldens for the
`hyphenation` (zone 360 twips = 18pt) and `hyphenation-zone` (zone 720 = 36pt) probes,
which hold the same prose: Word's line breaks are **identical** in the two documents
except where `w:consecutiveHyphenLimit=2` or `w:doNotHyphenateCaps` - the other two
settings the second probe carries - explains the difference, and Word hyphenated lines
whose gap without the hyphen was 16.71pt to 34.09pt, well inside the 36pt zone. So the
zone never decided anything. Enforcing it cost 12 of the 13 first divergences in probe 2
and 1 of the 5 in probe 1 (line parity 47% and 83%).

The plumbing stays: `docx4j:hyphenation-zone` still travels on `fo:root`, and
`docx4j.convert.out.fo.wordLayout.hyphenationZone=true` restores the 17.0.5 behaviour.
Its default, where the element is absent, is Word's UI default of 0.25 inch (360 twips)
in US measurements - 0.75 cm in metric ones; all four corpus documents that switch
hyphenation on carry `w:hyphenationZone w:val="425"` explicitly, and ECMA-376 17.15.1.44
gives no default.

<a id="s47shrink"></a>**Two space-compression limits.** §4.2's `maxSpaceShrink` is what
Word pays to pull a **whole word** onto a justified line (measured at up to 20.5% on
these goldens, and 0.24 is the corpus default). It pays much less to take a longer
**hyphenation fragment** - the piece of the word that stays on the line - when it is
hyphenating anyway: fitted to the goldens' 124 lines with exact glyph widths, Word
accepted fragments costing 1.2% and 6.0% of the line's spaces and rejected 13.5%, 14.5%,
22.0% and 25.6%. `docx4j.convert.out.fo.wordLayout.maxHyphenSpaceShrink` is that limit,
default **0.10**; it is capped by `maxSpaceShrink` like everything else. It accounted for
3 of the 5 first divergences in probe 1.

<a id="s47caps"></a>**Pattern lookup folds case badly.** FOP's `Hyphenator` matches the
patterns through the pattern file's own class table, which is lossy for a word written in
capitals: with fop-hyph's en patterns, APPROPRIATIONS came back AP-PRO-PRIATIONS where
Word breaks it APPROPRI-ATIONS, and DEPARTMENTS gained a spurious DEPARTMEN-TS. The word
is lowercased before the lookup (the offsets are unchanged, and a mapping that changed
the length is discarded so they stay valid), so capitals break where the same word in
lower case breaks.

**Minimum letters either side.** Word's are 2 before the hyphen and 3 after
(de-scribes, re-sponsibilities, ex-ceeded in the goldens). FOP's
`hyphenation-remain-character-count` / `-push-character-count` default to 2/2, which is
what docx4j leaves them at: raising the push count to 3 would lose breaks Word takes.

**Where this leaves the probes.** 121 of Word's 124 lines (`hyphenation` 95% line parity
from 83%, `hyphenation-zone` 100% from 47%), and the same line breaks in both, as Word has
them. The residual is one dictionary difference at the end of `hyphenation`, which costs
the three lines after it: Word breaks TRANSFOR-MATION where the en patterns give
TRANSFORMA-TION.

**Patterns.** FOP hyphenates from TeX pattern files, and neither FOP nor docx4j ships any:
without them nothing is hyphenated, whatever the document says. The usual source is
`net.sf.offo:fop-hyph`, which is not under the Apache licence, so an application that
wants hyphenated output adds it (or its own patterns) to its own classpath. It is a
test-scope dependency of docx4j-export-fo and a dependency of the layout-fidelity harness,
and of no published docx4j module.

**Language.** FOP chooses patterns by the block's `language` and `country` properties,
which docx4j has always written from the paragraph's effective `w:lang`
(`org.docx4j.model.properties.run.Lang`). Two limitations follow: Word chooses per run,
while FOP reads its hyphenation properties from the block, so a paragraph mixing languages
is hyphenated in the paragraph's own; and a `w:lang` carried only by the paragraph mark
never reaches the effective `rPr`, because `StyleUtil.isEmpty(RPr)` does not count
`w:lang`.

**Word needs a dictionary too.** Measured: of the four corpus documents which set
`w:autoHyphenation` (de-AT, de-DE, sl-SI, pt-BR), Word 365 on the English reference
machine hyphenated not one word - the only hyphens at its line ends are hyphens the text
itself contains. Word hyphenates only where the proofing tools for the run's language are
installed, which the docx does not record; docx4j hyphenates wherever it has patterns.

`docx4j.convert.out.fo.hyphenate` overrides the document either way: `true` hyphenates
every paragraph that does not suppress hyphenation (what the property did from 8.3.3), and
`false` hyphenates nothing. Unset, the document decides.

Probes: `hyphenation` (zone 360, no limit) and `hyphenation-zone` (zone 720, limit 2,
`w:doNotHyphenateCaps`), the same prose in both; both have Word 365 goldens.

### 4.5 Runs of spaces

Word renders every space of a run of spaces; documents use them to line things up,
typically after tabs (measured on one line, eight spaces drawn as one ended 21pt short of
Word). The paragraph's `fo:block` carries `white-space-collapse="false"`. It must go on the
block, not on the `fo:inline` holding the spaces: FOP's `XMLWhiteSpaceHandler` reads the
property from the nearest ancestor `fo:block`.

`white-space-treatment` stays at its default (`ignore-if-surrounding-linefeed`) on a
paragraph whose content does not begin with whitespace, which is both what makes this safe
and what Word does - FOP then drops glue at the start and end of every line, so a run of
spaces at a line end hangs there and the wrapped line starts flush. Measured with FOP 2.11:
a run of n spaces becomes a single glue n space widths wide, so it cannot break in the
middle and is discarded whole at a line boundary. (The unwanted indent after a line wrap
which had ruled this out before came from `white-space-treatment="preserve"`, which used
always to be set with it, not from the collapse setting.)

<a id="s45lead"></a>**Whitespace the paragraph starts with is a different matter**, and
turning collapsing off is not enough for it: FOP's `XMLWhiteSpaceHandler` treats the start
of a block, and the position after a nested block (which is what a `w:br` becomes), as
"after a linefeed", so the default treatment *deletes* those characters. Word paints them.
Measured: a run whose `w:t` is `xml:space="preserve"` with eighteen leading spaces at 13pt
Times New Roman started 58.8pt left of Word's line, and five leading spaces in a
right-aligned cell cost 19.96pt - Word right-aligns the text *and its leading spaces* on
the content edge. This applies to a paragraph whose only
in-flow content is spaces as well (typically one which also anchors a text box): Word
gives it a line, and FOP built none, since the spaces were gone before layout.

<a id="s45nbsp"></a>**But not with `white-space-treatment="preserve"`**, which docx4j set
on the block until 17.0.6: it also keeps the space that falls at a **line-break
opportunity**, so every wrapped line starts one space to the right of Word's, and on a
justified line that space is stretched too. Measured on a document whose first body
paragraph is justified and begins with ten literal spaces, Word's continuation lines all
start at x=113.3 where ours ran 119.0 / 117.0 / 117.9 / 120.0 - a spread of up to 4.6pt, on
92 of that document's 142 matched runs of lines and on six documents of the long-document
corpus. Isolated on the same block in a probe FO: `preserve` buys the ten leading spaces
(x0 144.7 against Word's 146.9, the default's 113.3) and costs one space, 2.8-3.0pt, at
every wrap. An `fo:inline` carrying the property instead is not honoured - FOP reads it
from the nearest ancestor block, as above - so the **leading whitespace itself** becomes an
`fo:leader` of exactly its measured width, which is what a leading tab already is (§4.4):
it reserves the width, is not a break opportunity, and the block goes back on the default
treatment. `WordLayoutFixups.leadingWhitespaceLeader` does it, rather than the block writer,
because the block's `font-family` is not settled until that writer returns. Where the width
cannot be measured - no font, no size, or whitespace other than plain spaces - the property
stands, which is what 17.0.5 did.

A leader also keeps the PDF's **text layer** as Word's is. No-break spaces were tried first
and reproduce the geometry exactly - the painted lines are identical to the point - but they
are glyphs, so the extracted text gains five or eight leading spaces that Word's PDF does
not have, which the harness scores as a line that does not match. The empty-paragraph
placeholder of [§2.5](#s25), whose whole in-flow content is one space, keeps `preserve`:
that space is the line, not an indent.

**And it must not be inherited.** `white-space-treatment` is an inherited property which
FOP reads from the nearest ancestor `fo:block`, so it has to be on the paragraph's block -
but where that block is a *container*, a paragraph whose objects have been lifted into
positioned `fo:block-container`s (an anchored picture, a text box), every block inside
those containers inherits it and keeps its own leading whitespace. Measured: one paragraph
enclosing forty positioned text boxes moved every continuation line inside them from
x=72.0 to 74.2 - one 8pt Arimo space, 0.2778 em - and the narrower measure re-broke the
text; 174 such blocks in 53 of 156 corpus documents. `WordLayoutFixups` puts each
out-of-flow child of such a block back on the XSL-FO default, which stops the inheritance
without moving the placeholder off the block that needs it. The same applies to the
empty-paragraph placeholder of [§2.5](#s25).

### 4.6 Character spacing (`w:spacing` on a run) and scaling (`w:w`)

Word adds the expansion after **every** character, spaces included, which is also how FOP
renders it. The problem is how FOP *measures* it: FOP 2.11's complex-script text path
(`GlyphMapping.processWordMapping`, taken for every embedded OpenType font with GSUB/GPOS
tables, i.e. all of docx4j's) leaves the letter spaces out of a word's width when breaking
lines, while its plain path (`processWordNoMapping`) counts `wordLength - 1` plus one for a
following non-space break character. So expanded text overflowed the right margin -
measured 95pt over at 3pt spacing, and one word too many in Word's Title style at 0.25pt -
and condensed text stopped short.

The Word line manager re-derives the plain path's count for each word box. Measured on
`spacing-char` (0.25 / 1 / 3pt expanded, 0.5pt condensed): every line breaks as Word's bar
one 0.3pt marginal case, line parity 29% -> 79%, and `kern-title` 93% -> 100%. Counting a
letter space after every character when measuring, rather than the plain path's count,
broke a word early on every line, so Word's fit rule is the plain path's. Upstream FOP
report candidate.

<a id="s46w"></a>**`w:w`, character scaling** (ECMA-376 17.3.2.43) multiplies the run's
glyph advances by a percentage. Neither XSL-FO nor FOP can scale text horizontally: there
is no property for it, `font-stretch` is CSS and picks a different face rather than
scaling one, and scaling only the *measurement* would leave FOP painting glyphs wider than
the space reserved for them. What can be reproduced exactly is the effect that matters for
layout, the run's total advance, so the difference is spread over the run's characters as
`letter-spacing`, which FOP both measures and renders: the glyphs keep their shape, the
words fall where Word puts them and the lines break where Word breaks them. The amount is
measured rather than estimated - each span's natural width is taken from the font FOP will
use (`org.docx4j.fonts.TextMeasurer`) and the letter space is
`width x (w/100 - 1) / characters` - and it is applied in `RunFontSelector`, so both
pathways get it. **Limitation**: a run which also carries `w:spacing` keeps only the
character spacing, since the two share the one property; Word applies both.
Measured on a document of 186 scaled runs (`w:w` 102/103/105) whose font mapping is exact
(Arial->Arimo, Times->Tinos, Courier->Cousine), so that `w:w` is the whole error: the
median width ratio of our lines to Word's over twenty long matched lines was 0.9516, e.g.
a line Word draws 72.5..520.3 (447.8pt) came out 72.4..497.2 (424.8pt); it scores 0.746 ->
0.937 of Word's lines. Programmatically, a 94% run's painted line is now 94% of its natural
width to within 2%.

---

## 5. Fonts

### 5.1 Substitution order

For a font the machine does not have, docx4j chooses in this order:

1. **Metric-compatible clone**, from `Mapper.addMetricallyCompatibleSubstitutes`: Calibri,
   Calibri Light, Cambria, Arial, Times New Roman, Courier New, Tahoma, Verdana, Comic Sans
   MS, Trebuchet MS, Segoe UI, Segoe UI Light, Arial Black, Gadugi, Helvetica, Helvetica
   Neue, Georgia, Garamond, Book Antiqua, Palatino Linotype, Bookman Old Style, Arial
   Narrow, Century Gothic, Consolas, Lucida Console.
2. **Class-based**: whatever is left unmapped takes a font of its own class (sans, serif,
   monospace) from the classes and candidate lists in `FontSubstitutions.xml`.
3. **Glyph-aware, per script**: the run font selector then picks, per script segment of the
   text, a font that can actually render it - preferring the document font's class, caching
   the choice per (font, script), and warning once per font and script rather than once per
   glyph (`org.docx4j.fonts.FontFallback`).

<a id="s51cover"></a>Step 3 applies to a step-1 substitute too. A metric clone is chosen
for its advance widths, and several of them carry the Latin alphabet alone: Caladea, which
stands in for Cambria, has neither Greek nor Cyrillic (`fc-query`:
`20-7e a0-161 164-17f 192 1fa-1ff 218-21b 237 2c6-2c7 ...`). Until 17.0.6 the coverage check
skipped Greek and Cyrillic outright, on the assumption that any conventional stand-in
covers them, so step 1's choice was never questioned. Measured on a 68-page Greek document
set in Cambria: **48% of the glyphs docx4j painted were notdef** and its line parity was
0.072, the worst of a 103-document corpus, while Carlito - already loaded for the same
document's Calibri - covers both scripts. The check now runs for every script but Latin,
Common and Inherited, so the clone renders what it can and the rest falls through to a face
that can draw it (Greek Cambria reaches Tinos here, a serif with Greek; the residual is the
width difference, and the document goes to 0.248 with Word's page count within 3 of ours).

Without the last two, an unmapped font fell back to the document's default font whatever
the script, so Georgian, Ethiopic or CJK text came out as notdef boxes even on a machine
that had a font for it, and a sans came out in a Times clone or the other way about. Line
heights always come from the **document** font's metrics (§2.7), whichever physical font
renders it.

### 5.2 Measured advance-width comparisons

Line breaking cares about advance widths, so substitutes were chosen by measuring them
against the real font:

| Document font | Substitute | Measured |
| --- | --- | --- |
| Century Gothic | URW Gothic Book (URW base 35) | identical to the unit over 6,743 characters (0.00%); Nimbus Sans +3.13%, Arimo and Liberation Sans +3.00%, DejaVu Sans +7.22%, Noto Sans -2.11%, Carlito -14.24%. URW Gothic Demi likewise matches Century Gothic Bold. |
| Arial Narrow | Liberation Sans Narrow, else Nimbus Sans Narrow | Nimbus Sans Narrow agrees with Arial Narrow to within one unit per 1000 over letters, digits and punctuation (0.02% mean, bold likewise), against 14% for Carlito and 22% for Arimo. |
| Segoe UI Light | Source Sans 3 (Arimo as last resort) | Arimo is systematically 11.8% wider on letters, so every line breaks early; Source Sans 3 has no systematic bias (+0.4% mean signed). Neither is a metric clone. |
| Consolas, Lucida Console | Cousine, else Liberation Mono | The stand-ins advance 0.6em to Consolas's 0.55em, so code lines longer than about 97 characters at 8pt wrap where Word's did not. Line heights still follow Consolas's own metrics. |
| Cambria | Caladea | Left as it is: Caladea is 3.9% narrower on the regular face and 2.8% on the bold, and no installed face is closer. |
| Verdana | DejaVu Sans (Arimo as last resort) | Verdana is much wider than Arial: measured on real documents' lines whose text matches Word's exactly, Word's Verdana lines are 1.141 x our Arimo ones, and a 14% narrow font re-breaks every line. DejaVu Sans is 1.14 x Arimo over a mixed Latin sample. Tahoma stays on Arimo: on an all-Tahoma document the median ratio is 1.006. |
| Comic Sans MS | Noto Sans (DejaVu Sans, then Arimo) | Word's Comic Sans lines are 1.153 x our Carlito ones (the class-based fallback reached Carlito); Noto Sans is 1.15 x Carlito. |
| Georgia, Book Antiqua, Palatino Linotype | P052, URW's Palladio (Tinos as last resort) | Word's Book Antiqua lines are 1.087-1.114 x our Tinos ones and its Georgia lines 1.076-1.112 x; P052 is 1.09 x Tinos. P052 is in the URW base 35 (ghostscript-fonts). |

Each of these falls back through the choice it replaced, so a machine with only the
Liberation jar behaves as before.

**Sylfaen** (a Georgian face) rendered wide through the glyph-aware pass's DejaVu Serif:
measured, our Georgian lines are 8.6% wider than Word's (a line Word ends at x=525.5 ran to
546.6, 21pt past its right edge) and over 82 exact-match lines of a second document the
ratio Word/ours is 0.876. **DejaVu Serif Condensed** measures 0.900 - the only
Georgian-covering face within 3%, where Noto Serif Georgian is 0.999 of DejaVu Serif, i.e.
no better - so `FontFallback` carries it as a measured per-(font, script) preference,
consulted before the class defaults. Sylfaen's *Cyrillic* has no such answer here: Caladea
measures closest to it (1.0288 against Tinos's 1.041) but has no Cyrillic at all, so Tinos
stands and the residual is 4%.

Some fonts are deliberately **left unmapped**, each measured over the corpus to be better
off with the document default than with any available stand-in: condensed faces generally,
Lato, PostScript-style names (a name no system has a family for, so Word does not resolve
it either), a name whose only clue is that it ends in "Sans" or "Serif", and Arial Narrow
where neither of its twins is installed.

<a id="s52faces"></a>**All four faces of a substitute family are declared, not just the
regular one.** `MicrosoftFontsRegistry`, which is how docx4j finds a family's bold and
italic files, knows only Microsoft's own families - so for every substitute above, and for
Carlito, Caladea and the Liberation and URW families, the bold and italic faces came back
null and `FopConfigUtil` declared the family as the regular file with
`simulate-style="true"`. FOP then synthesised the bold by re-stroking the regular glyphs:
the ink looked bold, but every advance width was the regular face's. Measured against
Word's own PDFs of five corpus documents, bold text came out 11-18% narrow ("Partita IVA"
42.2pt against Word's 49.6, "Dato da sincronizzare" 86.9 against 97.1) while the regular
weight of the same documents measured 0.9996 of Word's, and a centred Verdana title was
240.0pt against Word's 270.7 - it is 269.8 now, and the PDF embeds `DejaVuSans-Bold` where
before it held only `DejaVuSans`. The faces are found by the family's own name ("DejaVu
Sans" + " Bold", "Carlito Regular" -> "Carlito" + " Bold") and, failing that, by file name,
since a whole URW family reports one name and is told apart only by its file
(P052-Roman.otf -> P052-Bold.otf). Both are exact lookups in the maps font discovery built,
so a family which really has no such face still gets none - and the `+noliga` and `+kern`
twins follow, because each declaration gets its own.

### 5.3 Families whose faces all report one name

In the URW base 35, URW Gothic Book, Demi, Book Oblique and Demi Oblique all call
themselves "URW Gothic", and FOP's font detection reports each as upright weight 400, so
nothing but the file name tells them apart and the family registered whichever file the
file system handed over last. On one machine "URW Gothic" resolved to URWGothic-DemiOblique,
"Nimbus Roman" to NimbusRoman-BoldItalic, "Nimbus Mono PS" to NimbusMonoPS-BoldItalic and
"C059" to C059-BdIta - a bold italic 3.5% wide of the face it stood in for. Font discovery
now keeps the plainest face of such a family; naming a font file explicitly still gets
exactly that file.

### 5.4 Kerning

Word kerns a run **only** when `w:kern` is present and its threshold (in half-points) is at
or below the run's size (ECMA-376 17.3.2.19); its default template sets `w:kern` only on
the Title style. FOP kerns per font, so it kerned everything - and a kerned line is a
fraction of a point shorter, so a word Word wraps stayed on the line and every later line
moved.

docx4j declares each font unkerned, and a second time as `<name>+kern` with kerning on (FOP
embeds a declared font only when it is used, so a document with no kerned runs pays
nothing); `RunFontSelector.isKerned` sends the runs Word kerns to the twin, and
`PhysicalFonts.get` ignores the suffix so line metrics resolve to the same font.

Word also kerns pairs involving the **space** glyph - in Liberation Serif `A ` is -50/1000
em, ` A` -60, and likewise `T `, `Y `, `V `, `W ` - which FOP skips because its spaces are
glue. Such a space is wrapped in an inline whose `word-spacing` carries the pair value, so
it stays a break opportunity.

Measured on `kern-title`: kerned lines within 0.13pt of Word's width (they had been 3-5pt
wider), unkerned lines identical, and the same line breaks for every threshold case (28 at
12pt not kerned, 24 at 12pt and 28 at 14pt kerned, a Title style with a 12pt direct size
not kerned). Four long-prose probes went from 88-94% to 100% line parity.
`docx4j.convert.out.fo.kerning=true` kerns everything, as before 17.0.5.

### 5.5 Ligatures

Word applies no standard OpenType ligature unless the run asks for one (`w14:ligatures`).
FOP applies GSUB `liga` to every font that has it. In Calibri, and in its metric twin
Carlito, that turns `ti` and `tt` into ligature glyphs which have no cmap entry of their
own, so FOP mints a private-use code point and the PDF's `ToUnicode` maps the ligature to
U+E000: the ink lands in the right place, but the text cannot be extracted, searched or
read by a screen reader.

FOP 2.11 has no per-run, per-script or per-feature switch for this (§10). What does work
per declaration is `encoding-mode="single-byte"`, which loads the font as a simple TrueType
font - one implementing neither `Substitutable` nor `Positionable`, so no OpenType feature
is applied to it at all. docx4j declares each TrueType font a second time that way, under
the family name plus `+noliga`, and sends runs of **Latin** text which ask for neither
ligatures nor kerning to it.

The restriction to Latin is measured: a single-byte font chains 256-glyph encodings for
whatever its primary encoding does not hold, and a whole non-Latin alphabet then loses
characters from the PDF's text layer (the ink stays right). A run which asks for ligatures
keeps the ordinary declaration; a CFF/OpenType font gets no twin either, since FOP would
then misdescribe it in the PDF as TrueType.

Side effect: this also stops FOP kerning those runs from the font's GPOS table, which
`kerning=false` could not - FOP's `kerning` attribute governs only the legacy `kern` table,
and a font like Carlito has none. `docx4j.convert.out.fo.ligatures=true` restores FOP's
behaviour. `w14:ligatures` survives style resolution, so a character or paragraph style can
ask for ligatures.

### 5.6 Declaring fonts to FOP

Two rules, both of which showed up as FOP logging `Font ... not found. Substituting with
any` and quietly setting text in a default font:

- **Every font actually used must be declared**, not only the fonts the document's runs
  name: a font reached only through a paragraph mark, an empty paragraph, a style or a
  fallback was missing. The run font selector registers each physical font it uses, and the
  configuration built before the FO existed is topped up with the fonts chosen during
  conversion.
- **Entries for one font file must be merged.** Configuration entries keyed by
  `@embed-url` meant two document fonts sharing a file (Times New Roman and Tinos, or
  several unmapped fonts sharing a substitute) lost the first one's font-triplet. Entries
  for a file are merged, triplets and all, in both the main pass and the late fallback
  pass.

Over the corpus that removed all 55 warnings about document fonts; what remains are FOP's
own missing bold base-14 Symbol and ZapfDingbats.

### 5.7 Spans, scripts and symbols

FOP kerns and letter-spaces within a span, not across two, so how text is split into
`fo:inline`s matters. Consecutive characters of one script that [MS-OI29500] 17.3.2.26 does
not list - Georgian, Armenian, Ethiopic, Tibetan, Mongolian, Greek Extended - share a span;
each used to start one of its own, so such a word was one span per letter. General
Punctuation (U+2000-U+218F, and likewise U+2C00-U+2EFF) always stays in the run's own font;
symbol substitution applies to U+2190-U+2BFF.

The Wingdings 0xD8 bullet maps to the Dingbats U+27A2, as Word's own PDF output shows, and
not to U+2B9A, the "equilateral" arrowhead Unicode 7.0 added; its up, down and left
counterparts keep U+2B99, U+2B9B and U+2B98.

`w:caps` and `w:smallCaps` have no XSL-FO equivalent (`text-transform` and `font-variant`
are CSS), so the text itself is upper-cased, in the run's `w:lang`, since Turkish and
Lithuanian case differently; for small caps the originally lower-case stretches go in an
inline at 80% of the size. HTML gets `text-transform` / `font-variant`.

---

## 6. Tables

### 6.1 Where the table sits

Normally the table's grid edge sits at the text margin + `w:tblInd`, and the first column's
text one left cell margin further right. **In compatibility mode 14** - Word 2010's layout
engine, and that mode alone - it is the first column's *text* that lands on the text margin
+ `w:tblInd`, so the grid edge is one left cell margin further back.

Measured with `table-indent-compat14` / `-compat15`: the first cell's text at 72.0 / 72.0 /
77.3pt for no `w:tblInd`, `w:tblInd` 0 and `w:tblInd` 108 in mode 14, and at 77.8 / 77.8 /
83.1pt in mode 15.

**Below mode 14 the shift does not apply**, although docx4j applied it there until 17.0.6.
There is no Word probe for the older modes; the measurement is a corpus document with no
`compatibilityMode` setting at all (so mode 12) whose first table row is a single
`w:gridSpan="3"` cell holding a centred paragraph: Word centres that text on 297.65pt, the
exact centre of a 595.3pt page, where taking the shift centred it on 292.25 - 5.4pt, one
cell margin, left. Fitting the rule to mode 14 alone also took that document from 13 pages
to Word's 15. A mode-11 or mode-12 probe would settle it properly.

<a id="s61nested"></a>**A nested table takes no shift either.** Word puts the grid edge of
a table inside a `w:tc` on the containing cell's **content** edge, and adds the nested
table's own cell margin on top of that. Measured on a mode-14 first-page header (page
margin 28.35pt, outer `w:tblInd` 108, cell margin 108 both levels): Word's clip for the
nested table runs from 33.9 = 28.35 + 5.4, and its first cell's text is at 39.1, where
docx4j drew it at 34.0 - one cell margin left, on every cell of every nested table. The
outer table of the same document matched Word exactly, which is what proves the rule is
about nesting and not about the mode. There were 45 such tables across 11 corpus documents,
and it was the first divergence in four of them. Whether a table is nested is not known
where the indent is computed - in the XSLT pathway the `w:tbl` reaching the table writer
was unmarshalled on its own, so it has no parent - so the shift is stamped on the
`fo:table` and `WordLayoutFixups.nestedTableGridEdge` gives it back to the tables that turn
out to be inside an `fo:table-cell`.

A `w:jc="center"` table wider than the text column is **centred by Word, overhanging both
margins**; its start-indent is the negative half of the overflow.

### 6.2 Cell margins

Word applies default cell margins of 0.08in (108 twips) left and right when neither the
table nor its style sets any - a document need not define a "Normal Table" style, and
docx4j's own default styles part does not. Measured: cell text starts at the border centre
+ half the border width + 5.4pt. Applies to HTML output too.

### 6.3 Autofit column widths

Word's default table layout sizes columns from their content, and honours `w:tblGrid` only
for fixed layout (`w:tblLayout="fixed"`) or when every cell has a preferred width. Its
widths match the classic HTML automatic table layout: minimum = the widest word plus the
cell margins, maximum = the unwrapped content, a `w:tcW` fixes its column, then the maxima
if they fit, the minima if they do not, otherwise the slack shared in proportion to
(max - min).

`org.docx4j.model.table.AutofitLayout` implements it, and the FO table writer measures each
cell's *converted* content with `org.docx4j.fonts.TextMeasurer` (glyph widths from the font
FOP will use, unkerned, as Word measures). Measured against Word: 34.9 / 65.9pt for columns
Word gave 34.3 / 65.5pt of 451.3pt.

**Pictures count.** A cell holding nothing but a picture is measured at the picture's
width; measuring only text collapsed such a cell to its margins, and where every `w:tcW` is
auto the columns that did hold text then took the whole width and wrapped one word per
line.

**Column-spanning cells.** Non-spanning cells size their columns first; a spanning cell
widens the columns it spans only when their sum falls short of what it needs, sharing by
flexibility. Word kept 31 / 385 / 30pt outer columns under two two-column spans, where
splitting the span's width evenly gave the outer columns +25pt.

<a id="s63fit"></a>**The line a content-sized column holds.** A column Word's autofit pass
sized holds its widest cell content on one line, because that content is what set the
width, and Word's fit test there does not take the cell's borders off. Measured on
`table-autofit-wrap` P04: Word's columns are 127.2 / 137.8 / 148.6pt (grid line to grid
line), the widest content in each is 116.2 / 126.8 / 137.5pt, the cell margins are 5.4 +
5.4, the first cell's text starts at the grid line + 5.4 with no border allowance, and
each of those lines is drawn whole with a fifth of a point to spare. FOP subtracts the
borders from the cell's content width as well (half of each
collapsed border, all of a separate one), so exactly the lines that sized the columns were
re-broken: all three columns of that probe wrapped, and in `pbdr-space` a cell holding
47.1pt of text in a 47.1pt column came out on two lines. `WordLayoutFixups.cellLineWidth`
gives the border allowance back as a smaller **end** padding, so the text's start - the
grid edge plus half the border plus the left cell margin (§6.2) - does not move, and the
content may reach as far past the right cell margin as Word lets it. Probe line parity 88%
-> 100% and 75% -> 100%.

Only for a table whose columns docx4j sized from the content. Where the `w:tblGrid`
decides the width - `w:tblLayout="fixed"`, every cell with a preferred width, or a grid
scaled to the page (§6.5) - Word charges the borders too: measured on `table-fixed` and
`table-cellspacing`, a 150pt column with 5.4pt margins breaks a 139.2pt line, which fits
in 150 - 10.8 = 139.2 but not in that less the 0.5pt border. The FO table writer stamps
`docx4j-content-sized` on the `fo:table` for the fixup to read.

### 6.4 Widening to the preferred width

A preferred table width (`w:tblW` in dxa, or as a percentage of the text column) wider than
the columns sized from their content widens them until the table is that wide. The
proportions are the columns' **content** where every cell is auto-width - measured, content
of 67.4 and 74.1pt in a 400pt table gave Word 190.7 and 209.3pt - and the **`w:tblGrid`**
where any cell declares a width of its own (a `w:tcW` in `pct` is the common case): Word
lays such a table out on its grid however little content a column holds. A column with an
absolute `w:tcW` keeps it and the rest share what is left; `w:tblLayout="fixed"` uses the
grid as it stands.

### 6.5 Over-wide tables

Widths **docx4j chooses for itself** are scaled down to the text column, since an autofit
pass sizing columns to a `w:tblW` wider than the column could run a table off the page
(117pt past the edge on one document, where Word kept it inside).

A table's **own `w:tblGrid`** is left alone even when it is wider than the text column:
measured over the corpus, Word draws tables whose grid is 3% to 19% wider than the text
column overhanging the right margin, at their grid width, and fitting them cost line parity
on eight documents.

That exemption is unconditional only for a table which states a width of its own - a
`w:tblW` in `dxa` or `pct`, or `w:tblLayout="fixed"`. For an **autofit** table (`w:tblW`
absent or `auto`, layout not fixed) the grid is only the layout Word cached the last time it
laid the table out, and Word recomputes it against the page it is on now: measured, autofit
grids 1.35 to 2.7 times the text column are drawn by Word *inside* it (one 956.45pt grid on
a 453.6pt column came out 505.3pt wide), while docx4j painted half that document past the
page edge and lost 7 of Word's 15 pages. Such a grid is scaled to the column. The cut is at
**1.25** (`AbstractTableWriter.GRID_OVERHANG_LIMIT`), above the 19% Word was measured
overhanging and below the narrowest refitted case: fitting autofit grids only 1-2% over
re-broke cells Word does not break, and cost line parity on two documents.

### 6.6 `w:tblCellSpacing`

Word puts a whole gap (2 x the spacing) between the table border and the outer cells, and
each column's cell is `gridCol - 3 x spacing` wide; FO's separate-border model puts half a
gap at the edges instead. Padding on the `fo:table` plus columns narrowed by the spacing
reproduce Word's geometry with the table width unchanged: measured, cell text 3.6pt further
in for 72 twips of spacing.

### 6.7 Rows

- A `w:tr` with nothing of its own to write - every cell continuing a vertical merge, or no
  `w:tc` at all - is drawn by Word as part of the merged cell. docx4j drops the row,
  shortens the merges that covered it by a row, and gives its height to the row above. (FOP
  rejects an empty `fo:table-row`: the content model is `table-cell+`.)
- `w:gridAfter` and `w:gridBefore` do not remove a column from the row they apply to.
- Where `w:tblGrid` declares fewer `w:gridCol` than a row has cells, the grid follows the
  widest row, as Word does; an added column takes the cell's own width, else an equal share
  of what is left of the table's width.
- **A cell which produced no block gets an empty one.** `fo:table-cell`'s content model is
  `marker* (%block;)+`, and FOP fails the whole export with "fo:table-cell is missing child
  elements" where a cell holds none. A cell whose every paragraph is hidden text produces
  none - [§9.3](#93-hidden-text)'s rule that such a paragraph leaves no line is right in the
  flow but empties the cell. Word prints the row with the cell empty, its height coming from
  the other cells, which is what an empty `fo:block` gives (it generates no line, so no
  height). One document of a 103-document corpus has eleven such cells and lost its whole
  export to them.

<a id="s68"></a>**Exact row heights.** `w:trHeight` with `w:hRule="exact"`: Word keeps the
row at that height and lets the text overflow over the rows below, where FOP treats the
height as a minimum and grows the row. docx4j clips the cell content to the exact height
(an `fo:block-container` with `overflow="hidden"`), so the rows below sit where Word puts
them; the overflowing text is clipped rather than drawn over them.

<a id="s69"></a>
### 6.8 Floating tables

A table with `w:tblPr/w:tblpPr` is a frame: Word gives it a position of its own and flows
the surrounding text around it.

**Horizontally** the frame is the page for `horzAnchor="page"` and the text column
otherwise, and the table's **grid edge** goes at `tblpX` within it, or where `tblpXSpec`
says (`left`/`inside`, `center`, `right`/`outside`). Measured on `table-floating` (mode 15,
72pt margins, `horzAnchor="margin" tblpX=4500`): Word's first cell text is at 302.7pt =
72 + 225 + one 5.4pt cell margin, so the grid edge is at margin + `tblpX` with no
compatibility-mode adjustment - unlike `w:tblInd` (§6.1). Measured on a real cover page
(`horzAnchor="margin" tblpXSpec="center"`, a 450.05pt table on a 594pt page with no
margins): Word's first cell text is at 77.8 = (594 - 450.05)/2 + 5.4. This applies to every
floating table, whether or not it is taken out of the flow; before 17.0.6 the probe's table
sat at the margin, 225pt left of Word's.

**Vertically** Word measures the position from the page (`vertAnchor="page"`), from the
margin box (`vertAnchor="margin"`, or any `tblpYSpec`) or - the default, and 136 of the 152
`w:tblpPr` of the corpora - from the paragraph the table is anchored to
(`vertAnchor="text"`), which is the paragraph the `w:tbl` precedes. The three are
reproduced differently.

**Anchored to the text**: an `fo:float` at that paragraph, at the edge of the column the
table is nearer, with the text flowing past it. Measured on `table-floating` (a 200pt table
at `horzAnchor="margin" tblpX=4500 tblpY=1440` in a 451.3pt column): Word's anchor paragraph
begins at y=111.7 and the table's top edge is at 183.7 = 111.7 + 72 (`tblpY`), its first
cell text at 302.7; that paragraph's own lines run the full width above the table and the
next paragraph's stop at 285.8, one `w:leftFromText` (9pt) short of it. docx4j laid the
table out in the flow, taking the whole column width and pushing everything after it 102pt
down the page.

FOP gives a float the ipd of its content and ignores the padding of the block inside it -
measured, a right float's `padding-right` does not move the table and its `padding-left`
pushes it past the margin - so the float holds a **one-row table whose columns are the gap
to the text, the table, and what is left of the column**, which reserves exactly the band
Word keeps clear and puts the table at its `tblpX` within it. FOP anchors a side float to a
line and drops one which has none, so the float goes inside the anchor paragraph rather than
at flow level.

Left in the flow, where the text follows the table rather than running beside it: a table
filling more than 60% of the column (nothing useful fits beside it; measured on a CV built
out of twelve floating tables, one of them 73% of the column, where Word puts the next table
below it and FOP fitted it into the rest of the band, costing a page), one whose horizontal
position falls outside the text column, and one in a section of more than one column, since
FOP drops a float from a multi-column region silently.
`docx4j.convert.out.fo.tables.float=false` leaves every one of them in the flow.

**Anchored to the page or the margin box**: an absolutely positioned `fo:block-container`
(the anchored-picture machinery, §9.1), which takes no space in the flow. `tblpY` is
measured to the table's top edge: measured, a table with `vertAnchor="page" tblpY=3136`
(156.8pt) has Word's first cell line at y=167.6. Where the docx states a `tblpYSpec` the
table's height is not known before layout, so the container is the whole frame with
`display-align` on it: measured, a cover table with `tblpYSpec="bottom"` and no
`w:vertAnchor` on an A4 page with a 70.9pt bottom margin has its last line at y=765.4, i.e.
its bottom edge on the **bottom margin**, so the frame for a `tblpYSpec` without
`vertAnchor="page"` is the margin box.

Nothing flows beside a positioned container, so where the flow needs the band the table is
drawn in, the two are drawn on top of each other, and only two shapes are positioned: a
table which **opens its section's flow** (the cover page, the letterhead), and one which
**opens a page** - everything before it on the page is empty, back to a hard break - and is
narrow enough (60% of the column) for text to fit beside it. Measured on
`table-floating-anchor`, whose page-anchored tables each follow a page break: Word puts the
paragraph after the table at the top of the page (y=83.1) and the table at its anchor
(168.3 for `tblpY=3136`, 697.4 for `tblpYSpec="bottom"`, 390.4 for `tblpYSpec="center"` on
the page), where docx4j left the table in the flow at 108.9 and the paragraph below it at
192.4. A full-width table which opens a page stays in the flow: measured on a corpus letter
whose page-anchored table (`tblpY=1891`, 97% of the column) has Word's next table below it
at y=257.6, positioning ours drew the two on top of each other and cost 0.08 of line parity.
A page-anchored table **mid-page** stays in the flow for the same reason - measured, Word
floats a narrow one beside the text (probe page 6: the table at y=225.9 x=368.5, the lines
beside it stopping at 339.6), which a container cannot do and a float cannot be put at an
absolute y.

A positioned table keeps its columns, cell margins and borders; its own `start-indent` is
reset, because the container (or the float's one-row table) carries the position.
`docx4j.convert.out.fo.tables.position=false` lays every table out in the flow, as 17.0.5
did.

`leftFromText` and `rightFromText` are the gaps the float leaves; `topFromText` and
`bottomFromText` are not used, and nothing wraps beside a *positioned* table (§10).

---

## 7. Sections, columns, headers and footers

**The paragraph that carries the section break** belongs to the section it *ends*, not to
the next one; at the head of the next section it rendered as an empty line at the top of
that section's first page (in HTML output too). Where such a paragraph is **empty**, Word
gives it no line at all - the mark is all it is. An empty block at the end of a section's
flow cost a line height at every mid-page break, and where it did not fit made FOP start a
page carrying only the running header. A paragraph with content of its own is still
rendered, as is one which is all the section has (a flow with no block is invalid FO); the
section break still decides the next page master.

**Continuous sections with different column counts.** XSL-FO fixes the column count on the
page master, so a run of continuous sections has to share one. The page-sequence takes the
**largest** count, and each narrower part is wrapped in a container both FO exporters
render as `fo:block span="all"`. FOP balances the columns before such a block, which is
what Word does at a continuous break (a next-page break balances neither side). Sections
whose counts agree are untouched. The common case is a stretch of two-column text inside a
one-column document; taking the last section's count put everything before it into two
columns.

<a id="s73"></a>**The column gap** comes from the columns' own `w:cols/w:col/@w:space` where
`w:cols` has `w:col` children; `w:cols/@w:space` is the equal-columns value only. With
columns of different widths the container's value is commonly nothing like the real gap -
measured, 7.7pt where the real gap was 51.25pt - so every line in the section broke
differently. Where a run of continuous sections is merged, the count and the gap come from
the same section.

**Columns of different widths.** XSL-FO's region-body columns are all the same width, so a
section with `w:cols/@w:equalWidth="0"` and `w:col` children of different widths is
rendered as a **one-row `fo:table` whose cells are the columns**, a spacer column carrying
each `w:col/@w:space`; the page-sequence is then single-column for that stretch. Measured on
a certificate whose columns are 157 and 318pt with a 24pt gap: Word's second column starts
at x=232.2, where equal columns put ours at 312.5 and every line in it broke differently.
A table cannot flow content from one column into the next, so where the document itself
says where the columns divide - a `w:br w:type="column"` per boundary - that is where they
are divided. Word divides the paragraph the break is *in*: what precedes the break ends the
column and what follows it opens the next - measured on a letterhead whose address block is
one paragraph with the break in the middle of it, where Word puts the text after the break
at the top of column 2 (y=79.5, x=397.4).

The half that opens the next column takes a line **even when the break ends the paragraph
and nothing is left of it but its mark**, and the paragraph's space-after goes with that
mark: measured on `columns-unequal`, whose break ends its paragraph, Word starts column 2 at
y=183.4, 19.4pt below column 1's 164.0 - one 13.4pt line plus the paragraph's 6pt
space-after - and starts the next section 13.9pt below column 1's last line, i.e. with no
space-after there at all. The two halves are one paragraph, so its space-after goes with the
half that ends it, set to zero on the other rather than removed (removed, the style's own
spacing - 10pt in Word's default style - would apply instead). Its space-before stays on
both halves, which is where Word puts it: taking it off the second half cost four corpus
documents 2 to 6 points of line parity each.

**Without a column break** Word balances the columns itself, and the division is estimated:
each word's advance is taken as half its font size per character, the words are filled
greedily into each column's measure, and the split is the first word at which column one is
no shorter than column two - Word gives the odd line to the first column. What the estimate
has to get right is the *ratio* of the columns' capacities, which is the ratio of their
measures whatever the per-character figure is. Measured on `columns-unequal`, whose third
section is one paragraph in columns of 157 and 318pt: Word gives each column eight lines,
dividing the paragraph inside itself ("... quis nostrud" ends column 1), and so does docx4j,
one word short of Word's division. A paragraph is divided only where every one of its runs
is plain text; a stretch holding a table, a break of its own, or more content than fits the
page is left to the region body, whose balancing can do what a one-row table cannot
(`Balance.java`). Before 17.0.6 every such section was.

Columns within 5% of each other (4716/4715, 4680/4860 in the corpora) are Word's own
rounding of equal columns and are left to the region body.
`org.docx4j.convert.out.common.wrappers.UnequalColumns` builds the table.

<a id="s74"></a>**Margins of merged sections.** A page master can carry only one set of
margins, so a merged run of continuous sections takes the **first** section's `w:pgMar`, as
Word starts the page, and the difference is added to the indents of the paragraphs and
tables of each part that differs. Taking the last section's margins laid the earlier content
out 2 to 8pt out of place; the text now starts within 0.03pt of Word's x. An
`fo:block-container` carrying the indents would be tidier, but a block-container in a
multi-column flow makes FOP throw when it balances the last page's columns.

A part's measure is the region body less its own indents, whatever the region body is, so
which section's margins the masters carry does not move any line - except for the one thing
an indent cannot move, the **columns**, which the region body bounds. Where the merged run
still has a multi-column part (one whose columns are equal, since unequal ones are now a
table) whose own text column is wider than the first part's, the masters are built on that
part instead.

A negative `end-indent` given to a table this way used to be inherited by every paragraph in
every cell, which then ran that far past the cell's edge; `fo:table-body` now resets
`end-indent` as well as `start-indent`.

<a id="s75"></a>**Vertical alignment of a section.** `w:sectPr/w:vAlign` - Word's Page Setup
"Vertical alignment" - is `display-align` on `fo:region-body`: `center` for `center` and for
`both` (XSL-FO has no justified equivalent), `after` for `bottom`, nothing for `top`. It
costs nothing on a full page, so it applies to the whole section as Word applies it.
Measured on a 179-page specification whose title section carries
`<w:vAlign w:val="center"/>`: every line of page 1 was 112.5pt above Word's (Word's first
line at y=275.9, docx4j's at 163.4), and is now within 6pt.

**Where the body starts.** Word starts the body at the top margin and moves it down only
where the header itself reaches further, i.e. `max(top margin, header distance + header
height)`. Using the header distance alone pushed the body down by `w:pgMar/@w:header` minus
`w:pgMar/@w:top` wherever the distance was larger and the header empty (13.9pt on every
line of one document). Header and footer extents come from an area-tree pre-pass
(`FOPAreaTreeHelper`), which is how their real heights are known. Measured:
`page-first-even-odd-heights` - a three-line first-page header, a one-line even header, a
five-line odd header containing a picture, one-line odd and three-line even footers, six
pages - matches Word on 7 of 7 pages and 318 of 318 lines.

**Where the body ends** is the mirror of it: the bottom margin, pulled up only where the
footer reaches further, i.e. `max(bottom margin, footer distance + footer height)`. Where
the section has **no footer part** there is nothing to reserve and the footer distance
alone must not shorten the body. Measured on a document whose 44 `w:sectPr` all say
`w:pgMar w:bottom="0" w:footer="720"` and which has no `footerReference` (nor any
`headerReference`): Word's body runs to the foot of the A4 page, 841.9pt, and puts each
section's last line - a hand-made "Pagina N Van 22" - at y=827.3 to 828.2, while docx4j
reserved the 36pt footer distance and ended the body at 805.9. Twenty-one of the 22
sections spilled that one line onto a page of its own for that reason alone: Word's 24
pages came out as 44, and are 26 now.

---

## 8. Footnotes

- **Notes are found by `w:id`, not by position.** Word numbers its separator `w:id="-1"`
  and its continuation separator `w:id="0"`, so the note with `w:id` 1 sits at position 2:
  fetching by position rendered the note *before* the one the reference pointed at.
- **Layout as Word's**: the note's paragraphs go directly in the footnote body, with the
  number (`w:footnoteRef`) inline in the first of them - not a list-block with an 18pt
  hanging indent and 6pt after each note.
- **The separator** is Word's 2in 0.6pt rule, vertically centred in a line of the separator
  note's own font, not a full-width 0.5pt leader.
- **Note numbers take their formatting from their runs alone**: Word's FootnoteReference /
  EndnoteReference styles are what make them superscripts, so raising and shrinking them
  unconditionally double-applied it. The endnote loop skips separators by type as well as
  by id.

Measured: `footnotes` at 98% line parity; on `page-header-footnotes`, headers, footers,
footnote lines and the separator rule all within 0.5pt of Word's. The one remaining miss on
that probe is the space-after rule of [§3.10](#s310).

---

## 9. Pictures and text boxes

### 9.1 Anchored pictures

A `wp:anchor` picture is placed from its own geometry rather than rendered inline at the
end of its paragraph:

| `wp:wrap` | Rendered as |
| --- | --- |
| square, tight, through | an `fo:float` at the nearer edge, padded so the picture sits where Word puts it (text on the other side only) |
| topAndBottom | a block-container as tall as the picture, at the paragraph's top |
| none (behind or in front of text) | an absolutely positioned block-container that takes no space |
| position relative to page or margins | fixed on the page, no wrapping |

Measured: all four probe pictures within 0.3pt of Word's position. Word flows text down
**both** sides of a picture in the middle of a column; XSL-FO's floats are single-sided, so
that case is not reproducible.

FOP details worth knowing if you touch this: a picture's block needs `font-size` 0.1pt and
`line-height` 0 to sit on its container's top; `space-before` inside a float is discarded;
a block-container inside a right float lands at the left edge, so a plain `fo:block` is
used. `docx4j.convert.out.fo.pictures.float=false` lays these pictures out in the flow
instead; see §10 for why you might want that.

**A VML picture** (`w:pict/v:shape` holding a `v:imagedata`) whose shape style says
`position:absolute` is placed the same way, from the same shape properties a VML text box
uses (§9.2). Word writes an absolutely positioned picture exactly as it writes a text box;
docx4j rendered every VML picture inline at the end of its paragraph, which took a line the
picture does not take. Measured: a first-page header with a 66pt picture at
`position:absolute` plus ten right-aligned address lines had Word's first header line at
y=34.3 x=510.5 and docx4j's at y=94.3 x=255.1 - 60pt down and off its alignment - and the
header's measured extent (§7) was 203.4pt against Word's ~111. A picture whose shape states
no position is still laid out in the line, as Word lays it out.

### 9.2 Text boxes

Both VML text boxes (`w:pict/v:shape/v:textbox`) and DrawingML shape text
(`w:drawing/wp:anchor|wp:inline/.../wps:wsp/wps:txbx/w:txbxContent`) are rendered, through
the same anchoring path as pictures. A VML box takes its position from the `v:shape` style
(`mso-position-horizontal(-relative)`, `mso-position-vertical(-relative)`, `margin-left`,
`margin-top`, `width`, `height`), its `v:textbox` insets as padding, and its border unless
the shape is `stroked="f"`; a DrawingML shape takes the anchor's own geometry and its
`wps:bodyPr` insets; a shape inside `mc:AlternateContent` renders the fallback.

**A text box is laid out from its own edges, and nothing in it is paginated.** Its blocks
would otherwise inherit the anchoring paragraph's `text-align` and indents, which are about
the paragraph rather than the box: measured on a 222-page letter whose letterhead is a VML
box anchored in a right-aligned cell paragraph, Word starts all seven of the box's lines at
x=346.0 where each of ours was right-aligned inside the box, from 312.4 to 438.9. And Word
paginates nothing inside a box - it is a frame, not part of the flow - while FOP, given
`break-before="page"` inside an absolutely positioned container, paints only the **last**
container of a run of them: measured on three boxes in zero-height wrappers, only the third
was drawn, and without the breaks all three were. A 335-page mail merge of 2345 boxes, every
paragraph of which carries `w:pageBreakBefore`, came out with one line a page against Word's
nine - 3167 reference lines against our 335, line parity 0.101, and 0.974 once the
pagination properties are stripped. So the positioned container resets `text-align`,
`text-indent` and the indents, and `break-*` and `keep-*` are dropped from everything inside
it; a paragraph of the box which states its own `w:jc` keeps it, since that goes on its own
block.

**A text box is never given to `fo:float`**, whatever its wrapping style: a float discards
the box's position, which is the one thing the docx states exactly, and FOP's side floats
are unreliable (§10). Instead, a box in front of or behind the text, and a wrapped box
narrower than 60% of the column, is positioned where Word puts it and takes no space; a
wider wrapped box reserves its height at its paragraph. The 60% rule is measured: Word
flows text beside a narrow box, and a page built from several such boxes would otherwise
cost a page each. **Limitation**: text does not flow beside a text box.

### 9.3 Hidden text

`w:vanish` renders nothing and takes no space, as Word prints it; a paragraph whose runs
and paragraph mark are all hidden leaves no line at all. Word's "print hidden text" is an
application printing option rather than part of the docx, so it is the docx4j property
`docx4j.convert.out.printHiddenText` (default `false`). PDF and HTML.

### 9.4 A picture FOP cannot paint

Word draws every picture. FOP paints only the formats it has a loader for, and when it has
none it drops the viewport with the picture, so the space Word gives it collapses and
everything below moves up the page. Two kinds hit that: **EMF** (FOP can size it from the
metafile header but there is no EMF loader for PDF output) and **bytes that are no image at
all** - Word stores the web server's error page as the picture part when a linked picture
cannot be fetched, and one document of a 157-document corpus held eighteen of those.

Such a picture is pointed at a transparent 1x1 PNG (a `data:` URI, which FOP resolves) and
given `scaling="non-uniform"`, so the extent the document declares is reserved exactly -
which is what the layout needs - and one line is logged for the document rather than an
error per picture. Where
`docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.ImageMagickExecutable`
names a converter, the metafile is converted to PNG and painted instead.

The formats FOP does paint, with what docx4j already depends on (xmlgraphics-commons, the
JDK's ImageIO, Batik): PNG, JPEG - baseline, progressive and CMYK alike, since FOP passes
JPEG through to the PDF unchanged - GIF, BMP, TIFF, EPS, SVG and WMF (Batik's loader). No
extra ImageIO plugin is needed for any of them; measured, not assumed. A palette TIFF with
LZW compression is the one gap: the JDK's own TIFF reader throws
`UnsupportedOperationException` from `TIFFImageReader.readRaster` on it, which fails the
whole export; the TwelveMonkeys `imageio-tiff` plugin on the classpath reads it.

---

## 10. Known FOP defects and limitations

Worked around here, and worth knowing about:

- **Letter-spacing is left out of the measured width** on FOP's complex-script text path
  (`GlyphMapping.processWordMapping`) while still being rendered, so expanded text
  overflows and condensed text stops short. The line manager restores the plain path's
  count (§4.6). Upstream report candidate.
- **`fo:float` throws.** A side float of any height followed by content that overflows the
  page (a table row taller than the space left, say) makes FOP throw
  `java.util.NoSuchElementException` from `LMiter.next` under
  `PageBreaker.handleFloatLayout`, and the export fails. Minimal case: one `fo:float` with
  a block in it, then a table whose row does not fit. Anchored pictures with
  square/tight/through wrapping are the only floats docx4j emits;
  `docx4j.convert.out.fo.pictures.float=false` avoids them. The default stays `true`
  because the wrapping is right far more often than the defect bites.
- **`fo:float` plus a block inside an inline throws.** A float nested in an `fo:block`
  sharing a flow with an `fo:block` nested in an `fo:inline` makes FOP throw
  `NullPointerException` from `TraitSetter.setVisibility`, called with the null
  `curBlockArea` of a `BlockLayoutManager` that produced no area; the export fails.
  A block inside an inline is how the visitor pathway emits a line break inside a run, so
  the combination is common: three documents of a 157-document corpus failed on it.
  Minimal case: `<fo:block><fo:float float="right"><fo:block/></fo:float><fo:inline>
  <fo:block>x</fo:block></fo:inline></fo:block>`. The float lays out correctly as a direct
  child of `fo:flow`, and `WordLayoutFixups` moves it there in a document which has both
  (only then: at flow level the float anchors slightly higher, which measures a little
  further from Word).
- **`fo:float` plus a line that has to break beside it throws.** FOP 2.11's
  `LineLayoutManager$LineBreakingAlgorithm.updateData2` (line 403) reads
  `curChildLM.getFObj()` without checking `curChildLM`, which is null on the float
  re-layout pass under `PageBreaker.handleFloatLayout`, so a line broken in what is left
  beside a wide float throws `NullPointerException` and the export fails. It takes a side
  float, a following block mixing block-level and inline children - which is what an
  anchored picture or text box lifted into a positioned container beside a run of text
  produces - and a line that overflows; two documents of a 103-document corpus were lost to
  it. Plain FOP 2.11 throws the same on the same FO, so it is upstream, but the copy of
  `LineLayoutManager` docx4j carries (§4.1) has the null check, and the block it guards only
  reports the overflow. Upstream report candidate: one null check.
- **A float holding a table renders nothing at flow level**, and a float with no line to
  anchor to is dropped: a float which is a direct child of `fo:flow` and holds an
  `fo:table` produces no area at all, silently (measured, same minimal case with a table in
  the float). So a floating table's float goes inside the paragraph it is anchored to, and
  a floating table which the defect above would move to flow level is left in the flow
  instead - losing the wrap rather than the table ([§6.8](#s69)).
- **A block-container in a multi-column flow** makes FOP throw when it balances the last
  page's columns, which is why merged sections carry their margin differences as indents
  ([§7](#s74)).
- **`advanced="false"` on a font declaration is parsed and then discarded** on the PDF path
  (`LazyFont`), so it cannot be used to switch OpenType features off. Reported upstream.
- **No per-run, per-script or per-feature GSUB switch**: the features are a private static
  list in `DefaultScriptProcessor`, and the renderer's complex-scripts option turns off
  Arabic and Indic shaping as well. Hence the `+noliga` declaration (§5.5).
- **`ToUnicode` maps one character per CID**, so a ligature glyph with no cmap entry cannot
  be mapped back to the characters it stands for.

Limitations that remain in docx4j's output:

- **Unequal column widths** are a one-row table ([§7](#s73)), which fixes the widths but
  cannot flow content from one column into the next: where the document does not divide
  itself with a `w:br w:type="column"` the division is estimated from the text's width
  rather than laid out, and a stretch that does not fit the page is left as equal columns.
- **Text does not flow beside a text box** (§9.2), nor down both sides of a picture in the
  middle of a column (§9.1).
- **Right and dot-leader tab stops** still cost line parity in documents full of
  tables of contents: `docx4j:tab` leaders whose resolved stop is right-aligned or carries
  a dot leader are the largest remaining item of the long-document corpus (a zero-length
  leader appears in 64 of 99 of its documents, and a `right` or `decimal` stop in 26).
  The stops themselves are resolved (§4.4); what is not settled is where the residual
  comes from - measured cases include a table-of-contents line 26pt short of Word's, an
  after-tab fragment laid out on a line of its own, and a line running past the page edge -
  and no rule has been derived for them yet.
- **Page references in tabbed text**: FOP measures a line containing an unresolved
  `fo:page-number-citation` with an `MMM` placeholder, so a right, centre or decimal stop
  whose text holds a page reference lands a few points off. Table-of-contents entries are
  unaffected, because their stretching leader absorbs the difference (§4.4).
- **Widow control across a `w:br`**: a `w:br` is a nested block, which ends FOP's line
  sequence, so a paragraph without `w:keepLines` can still be split there where Word's
  widow control would not ([§3](#s39)).
- **Space-after against a footnote area** is not yet applied as Word applies it
  ([§3](#s310)).
- **Floating tables** (`w:tblpPr`, [§6.8](#s69)): the horizontal position is always
  applied. A table anchored to the **text** floats, with the text beside it, but FOP
  anchors a float to the line it sits at, where Word's frame starts `tblpY` below the top
  of the anchor paragraph: the lines beside that offset are narrowed where Word leaves them
  full width (`tblpY` is within 15pt of zero for 103 of the 132 text-anchored tables of the
  corpora which state one, and the table-floating probe's 72pt costs it two points of
  parity). Word runs text down both
  sides of a frame; an `fo:float` is single-sided.
  A table anchored to the **page or the margin box** is positioned only where it opens its
  section or opens a page and is narrow; positioned mid-page it would be drawn over the
  content Word puts below it, and mid-page Word wraps text beside it, which a positioned
  container cannot do. `topFromText`/`bottomFromText` are ignored.
- **Exact-height rows clip** their overflowing content rather than drawing it over the rows
  below ([§6](#s68)).

---

## Appendix: the `docx4j:` attributes

Written on the generated FO in the namespace `http://docx4j.org/fop/word-layout`, only when
Word layout is on, and read by the layout managers:

| Attribute | On | Meaning |
| --- | --- | --- |
| `docx4j:line-box` | paragraph block | the text box of a line (§2.1) |
| `docx4j:baseline` | paragraph block | the baseline within it (ascent + external leading) |
| `docx4j:line-rule` | paragraph block | `auto`, `exact` or `atLeast` (`w:lineRule`) |
| `docx4j:font` | run span | the document font, for per-run line sizing (§2.7) |
| `docx4j:label-ascent` | list item body block | the label's ascent (§2.8) |
| `docx4j:tabs`, `docx4j:tab-default`, `docx4j:tab-ind` | paragraph block | the resolved tab stops (§4.4) |
| `docx4j:tab` | `fo:leader` | this leader is a tab, to be sized during layout |
| `docx4j:space-shrink` | `fo:root` | `0` below compatibility mode 15 (§4.2) |
| `docx4j:hyphenation-zone` | `fo:root` | `w:hyphenationZone` in twips (§4.7) |
| `docx4j:hyphen-limit` | `fo:root` | `w:consecutiveHyphenLimit`, where it is not 0 |
| `docx4j:hyphenate-caps` | `fo:root` | `false` for `w:doNotHyphenateCaps` |

`WordLayoutFixups` also stamps hints of its own, without a namespace prefix (Xalan drops
the declaration when it copies a fragment in the XSLT pathway), and strips every one of
them before the FO reaches FOP; two on an `fo:table` are `docx4j-content-sized` (the
columns came from the content-based autofit pass, §6.3) and `docx4j-grid-shift` (the cell
margin the mode-14 grid edge was moved back by, [§6.1](#s61nested)).
