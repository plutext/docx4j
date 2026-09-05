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
deployed; see its README). 36 probes have Word goldens (`hyphenation` and
`hyphenation-zone` are newer and have none yet):

`spacing-adjacent`, `spacing-contextual`, `spacing-autospacing`,
`spacing-autospacing-context`, `spacing-page-top`, `spacing-section-start`,
`spacing-in-table`, `spacing-char`, `line-auto`, `line-exact-atleast`, `line-mixed`,
`break-ragged`, `break-justified`, `widow-orphan`, `kern-title`, `footnotes`,
`footnote-space-after`, `image-inline`, `image-anchored`, `ptab-right`,
`page-header-footer`, `page-header-footnotes`, `page-first-even-odd`,
`page-first-even-odd-heights`, `page-tall-header`, `page-landscape-margins`,
`table-fixed`, `table-autofit`, `table-width`, `table-span`, `table-nested`,
`table-floating`, `table-cellspacing`, `table-rowheight`, `table-indent-compat14`,
`table-indent-compat15`.

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
| `docx4j.convert.out.fo.wordLayoutFixups` | `true` | The DOM pass over the generated FO (`WordLayoutFixups`): Word's spacing edge rules, the line-box attributes, exact-height rows, anchored pictures, text boxes. `false` gives the FO docx4j 17.0.4 produced. |
| `docx4j.convert.out.fo.kerning` | `false` | `false`: fonts are declared unkerned, with a kerned twin that only the runs Word kerns are sent to (§5.4). `true`: every font kerns, as before 17.0.5. |
| `docx4j.convert.out.fo.ligatures` | `false` | `false`: Latin runs asking for neither ligatures nor kerning are set in a `+noliga` declaration to which FOP applies no OpenType feature (§5.5). `true`: FOP's own behaviour, GSUB `liga` everywhere. |
| `docx4j.convert.out.fo.pictures.float` | `true` | Whether a picture Word wraps text around may be an `fo:float`. `false` lays such pictures out in the flow (no text beside them, but immune to the FOP float defect, §10). Text boxes are never floats whatever this says. |
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

### 2.5 The paragraph mark

Word ignores the paragraph mark's size when sizing the lines of a **non-empty** paragraph
(a 36pt mark on a 12pt paragraph used to give 36pt lines). An **empty** paragraph keeps the
mark's font and size, and is one line high.

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

**Contextual spacing.** `w:contextualSpacing` (ECMA-376 17.3.1.9) zeroes the gap between
two same-style paragraphs when **either** carries it, not only on the flagged paragraph's
side: a contextual paragraph followed by a non-contextual one of the same style with 12pt
before gets no gap.

<a id="s33"></a>**Hard page breaks.** A paragraph holding only a page break leaves no empty
line at the top of the new page. The next paragraph's space-before is dropped there **from
compatibility mode 15**, and kept below mode 15.

**HTML auto spacing.** `w:beforeAutospacing` / `w:afterAutospacing` is **14pt**, combined
by "larger of" like any other spacing, and honours `w:doNotUseHTMLParagraphAutoSpacing`. It
is dropped between consecutive list items (so a list gets 14pt before the first item and
after the last) and at the top and bottom of a table cell.

<a id="s35"></a>**Table cells.** A paragraph's space-before applies at the cell top, and its
space-after at the cell bottom in compatibility mode 15.

**Borders as padding.** A paragraph's borders and shading are resolved through the style
hierarchy, not read only from its direct `w:pPr` (Word's default Title style has a bottom
border). A border's `w:space` - the gap between the text and the border - is the padding on
that side: measured, Word's Title with 4pt space and a 1pt border starts the next paragraph
5pt lower, and so does docx4j, to 0.1pt. PDF and HTML.

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
holding a tab is laid out from the left whatever the paragraph's `w:jc`. Measured: six
consecutive tabs advance 216pt in Word, where the former three-no-break-space stand-in
advanced 54pt.

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

- **The hyphenation zone** (`w:hyphenationZone`, twips) is the largest gap Word tolerates
  at the end of a line. When the next whole word does not fit, Word hyphenates it only
  where the space left on the line is **greater than** the zone, taking the last
  hyphenation point that fits; otherwise it leaves the ragged edge and breaks before the
  word. Where nothing whole has fitted on the line at all, the gap is the whole line, so
  the zone is met and an overlong word is hyphenated.
- **`w:consecutiveHyphenLimit`** caps how many lines in a row may end in a hyphen; 0, and
  the absent case, mean no limit. Counted within a paragraph, which is as far as one line
  manager sees.
- **`w:doNotHyphenateCaps`** leaves a word written entirely in capitals whole. Applied
  where the whole word is known, so no hyphenation point is inserted in it at all.
- The **last line** of a paragraph cannot end in a hyphen, because the greedy loop's last
  break is the paragraph's own forced break. Word has no rule against hyphenating the
  *second to last* line, and neither does this.

**The zone's default.** ECMA-376 17.15.1.44 gives none. Word's UI default is 0.25 inch in
US measurements and 0.75 cm in metric ones; all four corpus documents that switch
hyphenation on carry `w:hyphenationZone w:val="425"` (0.75 cm) explicitly. docx4j uses 360
twips where the element is absent.

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
`w:doNotHyphenateCaps`), the same prose in both.

### 4.5 Runs of spaces

Word renders every space of a run of spaces; documents use them to line things up,
typically after tabs (measured on one line, eight spaces drawn as one ended 21pt short of
Word). The paragraph's `fo:block` carries `white-space-collapse="false"`. It must go on the
block, not on the `fo:inline` holding the spaces: FOP's `XMLWhiteSpaceHandler` reads the
property from the nearest ancestor `fo:block`.

`white-space-treatment` stays at its default (`ignore-if-surrounding-linefeed`), which is
both what makes this safe and what Word does - FOP then drops glue at the start and end of
every line, so a run of spaces at a line end hangs there and the wrapped line starts flush.
Measured with FOP 2.11: a run of n spaces becomes a single glue n space widths wide, so it
cannot break in the middle and is discarded whole at a line boundary. (The unwanted indent
after a line wrap which had ruled this out before came from
`white-space-treatment="preserve"`, which used always to be set with it, not from the
collapse setting.)

### 4.6 Character spacing (`w:spacing` on a run)

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

---

## 5. Fonts

### 5.1 Substitution order

For a font the machine does not have, docx4j chooses in this order:

1. **Metric-compatible clone**, from `Mapper.addMetricallyCompatibleSubstitutes`: Calibri,
   Calibri Light, Cambria, Arial, Times New Roman, Courier New, Tahoma, Verdana, Trebuchet
   MS, Segoe UI, Segoe UI Light, Arial Black, Gadugi, Helvetica, Helvetica Neue, Georgia,
   Garamond, Book Antiqua, Palatino Linotype, Bookman Old Style, Arial Narrow, Century
   Gothic, Consolas, Lucida Console.
2. **Class-based**: whatever is left unmapped takes a font of its own class (sans, serif,
   monospace) from the classes and candidate lists in `FontSubstitutions.xml`.
3. **Glyph-aware, per script**: the run font selector then picks, per script segment of the
   text, a font that can actually render it - preferring the document font's class, caching
   the choice per (font, script), and warning once per font and script rather than once per
   glyph (`org.docx4j.fonts.FontFallback`).

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

Some fonts are deliberately **left unmapped**, each measured over the corpus to be better
off with the document default than with any available stand-in: condensed faces generally,
Lato, PostScript-style names (a name no system has a family for, so Word does not resolve
it either), a name whose only clue is that it ends in "Sans" or "Serif", and Arial Narrow
where neither of its twins is installed.

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

This rule changed in Word 2013. **From compatibility mode 15** the table's grid edge sits
at the text margin + `w:tblInd`, and the first column's text one left cell margin further
right. **Below mode 15** it is the first column's *text* that lands on the text margin +
`w:tblInd`, so the grid edge is one left cell margin further back.

Measured with `table-indent-compat14` / `-compat15`: the first cell's text at 72.0 / 72.0 /
77.3pt for no `w:tblInd`, `w:tblInd` 0 and `w:tblInd` 108 in mode 14, and at 77.8 / 77.8 /
83.1pt in mode 15. A document with no compatibilityMode setting is mode 12 and takes the
older rule.

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

<a id="s68"></a>**Exact row heights.** `w:trHeight` with `w:hRule="exact"`: Word keeps the
row at that height and lets the text overflow over the rows below, where FOP treats the
height as a minimum and grows the row. docx4j clips the cell content to the exact height
(an `fo:block-container` with `overflow="hidden"`), so the rows below sit where Word puts
them; the overflowing text is clipped rather than drawn over them.

Floating tables (`w:tblpPr`) are measured but not reproduced - see §10.

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
the same section. **Limitation**: XSL-FO's region-body columns are all the same width, so a
section with `w:cols/@w:equalWidth="0"` and `w:col` children of different widths is
rendered as equal columns, with the first column's gap.

<a id="s74"></a>**Margins of merged sections.** A page master can carry only one set of
margins, so a merged run of continuous sections takes the **first** section's `w:pgMar`, as
Word starts the page, and the difference is added to the indents of the paragraphs and
tables of each part that differs. Taking the last section's margins laid the earlier content
out 2 to 8pt out of place; the text now starts within 0.03pt of Word's x. An
`fo:block-container` carrying the indents would be tidier, but a block-container in a
multi-column flow makes FOP throw when it balances the last page's columns.

**Where the body starts.** Word starts the body at the top margin and moves it down only
where the header itself reaches further, i.e. `max(top margin, header distance + header
height)`. Using the header distance alone pushed the body down by `w:pgMar/@w:header` minus
`w:pgMar/@w:top` wherever the distance was larger and the header empty (13.9pt on every
line of one document). Header and footer extents come from an area-tree pre-pass
(`FOPAreaTreeHelper`), which is how their real heights are known. Measured:
`page-first-even-odd-heights` - a three-line first-page header, a one-line even header, a
five-line odd header containing a picture, one-line odd and three-line even footers, six
pages - matches Word on 7 of 7 pages and 318 of 318 lines.

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

### 9.2 Text boxes

Both VML text boxes (`w:pict/v:shape/v:textbox`) and DrawingML shape text
(`w:drawing/wp:anchor|wp:inline/.../wps:wsp/wps:txbx/w:txbxContent`) are rendered, through
the same anchoring path as pictures. A VML box takes its position from the `v:shape` style
(`mso-position-horizontal(-relative)`, `mso-position-vertical(-relative)`, `margin-left`,
`margin-top`, `width`, `height`), its `v:textbox` insets as padding, and its border unless
the shape is `stroked="f"`; a DrawingML shape takes the anchor's own geometry and its
`wps:bodyPr` insets; a shape inside `mc:AlternateContent` renders the fallback.

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

- **Unequal column widths** render as equal columns ([§7](#s73)). Rendering such a stretch
  as a one-row table would fix the widths but stop the content flowing from one column to
  the next.
- **Text does not flow beside a text box** (§9.2), nor down both sides of a picture in the
  middle of a column (§9.1).
- **Page references in tabbed text**: FOP measures a line containing an unresolved
  `fo:page-number-citation` with an `MMM` placeholder, so a right, centre or decimal stop
  whose text holds a page reference lands a few points off. Table-of-contents entries are
  unaffected, because their stretching leader absorbs the difference (§4.4).
- **Widow control across a `w:br`**: a `w:br` is a nested block, which ends FOP's line
  sequence, so a paragraph without `w:keepLines` can still be split there where Word's
  widow control would not ([§3](#s39)).
- **Space-after against a footnote area** is not yet applied as Word applies it
  ([§3](#s310)).
- **Floating tables** (`w:tblpPr`) are not expressible in XSL-FO at their stated position.
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
