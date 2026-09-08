# Word's layout rules, and how docx4j reproduces them

How Word lays out a page, as measured against Word 365, and what docx4j does about it when
converting a docx to PDF via XSL-FO and Apache FOP. A reference, organised by topic: each
rule is stated once, with the measurement that established it and the property that turns
it off where there is one. Describes docx4j 17.1.0 with FOP 2.11.

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
deployed; see its README). 60 probes have Word goldens:

`spacing-adjacent`, `spacing-contextual`, `spacing-autospacing`,
`spacing-autospacing-context`, `spacing-page-top`, `spacing-section-start`,
`spacing-in-table`, `spacing-char`, `line-auto`, `line-exact-atleast`, `line-mixed`,
`break-ragged`, `break-justified`, `widow-orphan`, `kern-title`, `footnotes`,
`footnote-space-after`, `image-inline`, `image-anchored`, `ptab-right`,
`page-header-footer`, `page-header-footnotes`, `page-first-even-odd`,
`page-first-even-odd-heights`, `page-tall-header`, `page-landscape-margins`,
`page-blank`, `table-fixed`, `table-autofit`, `table-width`, `table-span`,
`table-nested`, `table-floating`, `table-cellspacing`, `table-rowheight`,
`table-indent-compat14`, `table-indent-compat15`, `hyphenation`, `hyphenation-zone`,
`tab-jc`, `tab-clamp-right`, `tab-leader-resolved`, `tab-leader-trailing`,
`tab-toc-pageref`, `pbdr-space`, `table-grid-edge-compat11`,
`table-grid-edge-compat12`, `table-grid-edge-compat14`, `table-grid-edge-compat15`,
`table-grid-overwide`, `table-autofit-wrap`, `table-floating-anchor`,
`columns-unequal`, `table-grid-edge-signed-compat12`, `-compat14`, `-compat15`,
`page-empty`, `picture-header-cell`, `tab-run-font`, `picture-anchor-negative`.

A rule a probe could not settle on its own was checked against a corpus of 194 real
documents scored against Word's own PDFs of them, so a change is accepted only when the
aggregate - documents with Word's page count, share of lines matching Word exactly, mean
and median per-document line parity - does not fall and no document regresses beyond
noise. Over the 17.0.5 cycle that went from 69.7% of lines matching Word exactly to 82.5%,
and from 128 to 152 of 190 scored documents having Word's page count.

<a id="s13malformed"></a>
### 1.3 Malformed input Word renders anyway

Some producers write nesting that is in no content model, and Word renders it: a `w:r`
directly inside a `w:r`, and a `w:p` directly inside a `w:r` or a `w:hyperlink`. JAXB
reports "unexpected element" for each, docx4j falls back to its preprocessing pass
(`org/docx4j/jaxb/mc-preprocessor.xslt`, the same one that resolves `mc:AlternateContent`)
and lets unmarshalling continue - and the whole offending subtree was then discarded
silently. Measured on a document holding 76 runs nested in runs and 76 paragraphs nested
in hyperlinks: the text docx4j extracted was 119,726 characters against 145,483 in the
document, one word appearing 302 times against 472 and another 11 times against 21, and
the FO held none of the first at all; Word's 54 pages came out as 44.

The preprocessor now hoists such content into the legal position around it. The run holding
it is **split**: each nested run becomes a sibling run keeping its own `w:rPr`, each of the
outer run's own children is wrapped in a run carrying the outer `w:rPr`, and a nested
paragraph contributes the runs it holds (its `w:pPr` goes, since there is nowhere legal to
carry it). Flattening the nested content into the outer run instead - which is what 17.1.0
first did - loses both the nested runs' character formatting and their whitespace: measured
on the document above, whose hyperlinks hold `w:hyperlink/w:r/(w:rPr, w:r, w:r, …)` with
the words in `w:rStyle="Highlight"` runs and the spaces between them in runs of their own,
Word paints `Orbán Viktor » Mondatok` in the highlighted style and we painted
`OrbánViktor» Mondatok`, the lone-space runs gone, on 466 lines. Only a **direct** child is
matched: a `w:r` deeper inside a `w:r` is the ordinary shape of a text box
(`w:r/w:pict/v:textbox/w:txbxContent/w:p/w:r`), which is perfectly legal. 10 documents of
the three corpora hold the shape; the one measured above went from 0.383 to 0.463 of
Word's lines and from 44 pages to 51 of Word's 54.

**`mc:AlternateContent`: Word draws the first `mc:Choice` it understands.** ECMA-376
Part 3 §10.2.1 makes the `mc:Fallback` the last resort, not the first choice, and docx4j
always took it. For `Requires="wps"` - a DrawingML shape, whose text box both FO pathways
do render (`wps:wsp/wps:txbx/w:txbxContent`) - that meant taking the VML `w:pict` twin
Word never looks at, with the position it carries: measured on a corpus header holding
one such pair, Word draws the box at x=528.0..581.8 and the fallback's
`style="left:605.25pt"` put ours at 519.9..803.5, most of it off the page. The prefix
list is `docx4j.jaxb.mc.preferChoice`, and it is applied in three places which have to
agree: the visitor exporters (`AbstractVisitorExporterGenerator.walkJAXBElements`, so
HTML takes it too), `docx2fo.xslt`, and the unmarshalling preprocessor for the
`mc:AlternateContent` that is not inside a `w:r` (which the docx4j content model does not
keep).

**It ships off** (the list is empty), because that is what measured better. Of the 25
documents of the three corpora that carry an `mc:Choice Requires="wps"`, choosing it
moved two and left 23 untouched, and both of those two fell - 0.932 -> 0.894 and
0.927 -> 0.897, a sum of -0.067. In each, the Choice's DrawingML box lands within 0.6pt
of the fallback's and of Word's (Word's page-number box y=791.3 x=474.7..524.8, the
fallback's 790.7 x=476.5..524.5, the Choice's 791.0), so nothing visible is gained, while
the header text and the page-number box then share one baseline where Word's are 4.8pt
apart. `docx4j.jaxb.mc.preferChoice=wps` takes the Choice where a document's fallback is
missing or wrong.

**White space in a `w:t` with no `xml:space="preserve"`.** `xml:space` is what makes an
element's leading and trailing white space significant, and Word writes it whenever the
space matters - so a `w:t` without it whose text has leading or trailing space came from
another producer, and Word trims it. Measured against Word 365: a centred paragraph of
`<w:t>Chantier\nd'enlèvement d'amiante</w:t>` then `<w:t>\n${caze.descriptive}</w:t>`,
neither with `xml:space`, is 136.6..458.6 = 322.0pt in Word and was 132.7..458.5 = 325.9
for us, and being centred it also started 3.9pt to the left; `<w:t>WEIGHT: </w:t>` is
72.0..218.6 in Word and was 72.0..220.4 (that document 0.967 -> 1.000). The rule is in
the `w:t` emission path both the FO and the HTML exporters share
(`RunFontSelector.fontSelector(PPr, RPr, Text)`), so the load path is untouched and a
document read and written back is unchanged. Property
`docx4j.fonts.runFontSelector.trimUnpreservedWhitespace`.

*A `w:t` of nothing but white space keeps it.* Emptying one changes what the paragraph
is rather than how wide it is, and the FO layer then treats the block as having no
content at all and gives it a line of its own: measured, page 12 of an 80-page corpus
document opened 20.6pt below Word's (133.8 against 113.3) because one such paragraph
gained a line. Word paints no glyph for it either way.

### 1.4 Compatibility modes

Several of Word's rules changed with its 2013 layout engine. Word records which engine
lays a document out in `word/settings.xml`, as
`w:compat/w:compatSetting[@w:name="compatibilityMode"]/@w:val`; docx4j reads it with
`DocumentSettingsPart.getCompatibilityMode()`. **A document with no such setting is mode
12**, not mode 15 (verified against Word goldens), and takes the older rules.

Rules that depend on it: the table grid edge (§6.1), space-before after a hard page break
(§3.3), space-after at the bottom of a table cell (§3.5), and space compression on
justified lines (§4.2). Of those, space-before after a page break keys on the
`w:suppressSpBfAfterPgBrk` flag with the mode as its default (§1.6); the other three have
no `w:compat` flag at all, so the mode is their only key.

### 1.5 Properties

| Property | Default | Effect |
| --- | --- | --- |
| `docx4j.convert.out.fo.wordLayout` | `true` | Word's layout managers: greedy line breaking, Word's line box and leading placement, tab-stop resolution, justified-space compression. `false` restores plain FOP layout, and the `docx4j:` foreign attributes are then not written either. |
| `docx4j.convert.out.fo.wordLayout.maxSpaceShrink` | `0.24` | How far the spaces of a justified line may be compressed to pull one more word in, as a fraction of their natural width. Only read when `wordLayout` is on; set explicitly, it applies whatever the compatibility mode. |
| `docx4j.convert.out.fo.wordLayout.maxHyphenSpaceShrink` | `0.10` | The same, for taking a longer **hyphenation fragment** rather than a whole word; Word pays much less for one (§4.7). Capped by `maxSpaceShrink`. |
| `docx4j.convert.out.fo.wordLayout.tocStretchingLeader` | `true` | A table-of-contents entry (first stop right-aligned with a dot leader) keeps the stretching `fo:leader` and `text-align-last="justify"`. `false` lays its tabs out against the stops like any other tab, which also gives its dots Word's grid phase; measured, the two are a wash (§4.4). |
| `docx4j.convert.out.fo.wordLayout.hyphenationZone` | `false` | `true` enforces `w:hyphenationZone` as the largest gap tolerated before hyphenating, which is what docx4j did to 17.0.5. Measured against Word, the zone never fires (§4.7). |
| `docx4j.convert.out.fo.wordLayout.justifySoftReturn` | `true` | A justified line that ends in a soft return (`w:br` with no type) is justified, as Word justifies it unless `w:compat/w:doNotExpandShiftReturn` is set (§4.2). `false` restores 17.0.5's behaviour, which was the flag-on behaviour for every document. |
| `docx4j.convert.out.fo.wordLayout.emptyLineAfterBreak` | `true` | A `w:br` whose new line holds nothing that paints still takes a line box, as Word gives it (&sect;2.5). `false` restores 17.0.5's behaviour. |
| `docx4j.convert.out.fo.wordLayout.emergencyBreak` | `true` | A word too long for a line of its own is broken inside it, at the last character that fits, as Word breaks one (§4.3). `false` paints it whole, off the page, as FOP does. |
| `docx4j.convert.out.fo.wordLayout.emergencyBreakTolerance` | `72` | How far past the measure, in points, such a word may run before it is broken. 8 is worth about +0.004 of mean line parity but breaks three documents' page counts, whose columns we size wrongly (§4.3). |
| `docx4j.fonts.wordLineMetrics.deviceGrid` | `false` | `true` rounds a font's single line height to Word's 600 dpi layout grid, 1/600 inch, which is what Word does (§2.1) - but measured over the corpora it moves page breaks and costs more than the 0.02pt a line it wins. |
| `docx4j.convert.out.fo.wordLayoutFixups` | `true` | The DOM pass over the generated FO (`WordLayoutFixups`): Word's spacing edge rules, the line-box attributes, exact-height rows, anchored pictures, text boxes. `false` gives the FO docx4j 17.0.4 produced. |
| `docx4j.convert.out.fo.glyphWidths.round` | `true` | Glyph advances are rounded to the nearest 1/1000 em, as Word measures and as Word's own PDFs record them; FOP truncates them, which runs every line it measures up to 0.1% narrow ([§10](#s10advances)). `false` leaves FOP's width table - and the `/Widths` written from it - alone. |
| `docx4j.convert.out.fo.kerning` | `false` | `false`: fonts are declared unkerned, with a kerned twin that only the runs Word kerns are sent to (§5.4). `true`: every font kerns, as before 17.0.5. |
| `docx4j.convert.out.fo.ligatures` | `false` | `false`: Latin runs asking for neither ligatures nor kerning are set in a `+noliga` declaration to which FOP applies no OpenType feature (§5.5). `true`: FOP's own behaviour, GSUB `liga` everywhere. |
| `docx4j.convert.out.fo.tables.position` | `true` | A floating table's `w:tblpPr`: the grid edge at `tblpX`/`tblpXSpec`, and a page- or margin-anchored table which opens its section placed absolutely (§6.8). `false` lays every table out in the flow, as 17.0.5 did. |
| `docx4j.convert.out.fo.frames.position` | `true` | A paragraph whose `w:framePr` is anchored to the page or to the margin is lifted into a positioned block-container, and the flow keeps the band of a frame no text may run beside (§9.5). `false` lays every framed paragraph out where it falls, as 17.0.5 did. |
| `docx4j.convert.out.fo.pictures.float` | `true` | Whether a picture Word wraps text around may be an `fo:float`. `false` lays such pictures out in the flow (no text beside them, but immune to the FOP float defect, §10). Text boxes are never floats whatever this says. |
| `docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.ImageMagickExecutable` | unset | Names an ImageMagick/GraphicsMagick executable. Set, a picture FOP cannot paint (EMF) is converted to PNG and painted; unset, its space is reserved but it is not drawn (§9.4). |
| `docx4j.convert.out.fo.pictures.convertDensity` | `300` | Pixels per inch that converter rasterises a metafile at. |
| `docx4j.fonts.altName.enabled` | `true` | Whether a font this machine does not have may be resolved through the `w:altName` its own font table gives it (§5.1). `false` goes straight to the class-based fallback. |
| `docx4j.convert.out.printHiddenText` | `false` | Hidden text (`w:vanish`) is not rendered and takes no space, as Word prints it. `true` renders it. PDF and HTML. |
| `docx4j.convert.out.fo.pgNumType.oddEvenParityFix` | `true` | A section whose `w:pgNumType/@w:start` FOP has to clamp (0, which XSL-FO forbids) gets its ODD and EVEN page-master alternatives swapped, so the headers land on the side Word puts them (§7). |
| `docx4j.convert.out.fo.mirrorMargins` | `true` | `w:settings/w:mirrorMargins`: each page master gains a mirrored twin, chosen on even pages, whose left and right margins are the other way round (§7). |
| `docx4j.convert.out.fields.docPropertyCachedResult` | `true` | A `DOCPROPERTY` keeps the result the document cached, which is what Word paints until the field is updated (&sect;7). `false` evaluates the property. Also HTML. |
| `docx4j.convert.out.fields.dropResultlessIf` | `true` | An `IF` field with no `w:fldChar w:fldCharType="separate"` has no result and paints nothing, its branches being field instruction (§7). Also HTML. |
| `docx4j.convert.out.fields.formFieldResults` | `true` | A legacy form field paints the state its `w:ffData` holds: a `FORMDROPDOWN` the `w:listEntry` its `w:result` selects, a `FORMTEXT` with nothing typed into it its `w:default` ([§7](#s7formfield)). `false` paints nothing, as before 17.1.0. Also HTML. |
| `docx4j.convert.out.fo.wordLayout.boundKeepChains` | `true` | A `keep-with-next` chain taller than a page has its keeps reduced to a finite penalty, so the breaker may break inside it as Word does, instead of FOP running the whole chain off the bottom of one page ([§3](#s39keepchain)). |
| `docx4j.convert.out.fo.wordLayout.keepChainPenalty` | `900` | The penalty such a keep is reduced to, against FOP's infinite 1000: high enough that the breaker still keeps the blocks together wherever they fit. |
| `docx4j.convert.out.fo.wordLayout.keepChainTolerance` | `3.0` | How many times the page a keep chain must exceed before it is bounded. The flow-level height sum over-estimates what the page must hold, and a chain 12% over is one Word fits ([§3](#s39keepchain)). |
| `docx4j.jaxb.mc.preferChoice` | empty | The `mc:Choice/@Requires` prefixes we claim to be able to draw; the first `mc:Choice` naming only those wins over the `mc:Fallback`, as it does in Word. Empty (the default, and what measured better) always takes the fallback, as docx4j always has (§1.3). Also HTML. |
| `docx4j.fonts.runFontSelector.trimUnpreservedWhitespace` | `true` | The leading and trailing white space of a `w:t` with no `xml:space="preserve"` is dropped, as Word drops it (§1.3). Also HTML. |
| `docx4j.convert.out.fo.hyphenate` | unset | Overrides the document's own `w:autoHyphenation`: `true` hyphenates every paragraph that does not suppress hyphenation, `false` hyphenates nothing. Unset, the document decides (§4.7). |

<a id="s16settings"></a>
### 1.6 Settings sensitivity

Which `word/settings.xml` settings each rule here is sensitive to, and which of them docx4j
reads, is audited in [word-layout-settings.md](word-layout-settings.md) - the `w:compat`
flags one by one, the `w:compatSetting` names, and the top-level children, with the
compatibility-mode bundle each flag's default comes from.

**The policy is: a rule keys on the flag, and the compatibility mode supplies the flag's
default where the document does not state one - which, measured, is "off".** The settings
document's §2 tabulates what Word *writes* at each mode, and 17.1.0 measured that neither
half of that implies a behaviour: a mode-11 corpus document which omits
`useWord2002TableStyleRules` from an otherwise complete Word-2003 compat block has Word
applying the modern table style rules to it, and so do two more which *state* the flag.
So every legacy flag defaults off, and the three rules keyed on one that a real document
states - `growAutofit`, `useWord2002TableStyleRules`, `forgetLastTabAlignment` - were
measured and unkeyed again ([settings §4(e)](word-layout-settings.md)). The rules that do
read a flag are `doNotExpandShiftReturn` (§4.2), `noTabHangInd` (§4.4),
`allowSpaceOfSameStyleInTable` (§3.5 - resolved, but measured to change nothing in Word 365), `splitPgBreakAndParaMark` and
`suppressSpBfAfterPgBrk` (§3.3); the last two have defaults which are themselves
measurements of Word 365.

`org.docx4j.model.CompatibilityOptions` is the single implementation of that resolution -
built once per conversion, hung on the conversion context, and passed into
`WordLayoutFixups` beside `HyphenationSettings` - so §2 of the settings document has
exactly one place in the code. A flag stated as
`w:val="0"` beats the mode default; a flag not stated at all does not (the three states
of `CT_OnOff` that `BooleanDefaultTrue.isVal()` cannot report on its own). Flags the FOP
layout managers need travel to them as `docx4j:` attributes on `fo:root`, as the
hyphenation settings do (Appendix).

The rules below name the flag they key on where they have one. Everything else the audit
lists as IGNORED is a behaviour docx4j implements one way and a flag that would change it,
which is a measurement waiting for a Word golden rather than a rule.

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

<a id="s21grid"></a>**Word's `single` is a whole number of 1/600 inch, and ours is not**
(measured 17.1.0, **not** applied by default). Word lays a page out in 600 dpi device units
- its A4 page is 595.32 x 841.92pt, which is 4961 x 7016 of them - and a single line is a
whole number of them. All four of the `line-auto` golden's fonts confirm it, and the
arithmetic above is 0.001 to 0.021pt short of every one of them:

| font / size | exact | Word | units |
|---|---|---|---|
| Liberation Serif 12 | 13.7988 | **13.80** | 115 |
| Carlito 11 | 13.4277 | **13.44** | 112 |
| Liberation Sans 10 | 11.4990 | **11.52** | 96 |
| DejaVu Sans 10 | 11.6406 | **11.64** | 97 |

Half a unit goes to the even one. What `w:spacing` then makes of `single` is **not** rounded
again: Word's own pitch inside one paragraph alternates between the two units either side of
the exact value - Liberation Serif 12pt at `w:line="276"` is 115 x 276/240 = 132.25 units
and the golden's four pitches are 15.96 / 15.84 / 15.87 / 15.84, averaging 15.8775 against
the exact 15.87 - so it is the accumulated *position* Word rounds, not the pitch. (Rounding
the pitch as well was tried and is worse again: it moved a page break in this very probe.)

`docx4j.fonts.wordLineMetrics.deviceGrid=true` applies the rounding, and it is **off by
default**. 0.02pt a line is smaller than what the rest of the layout still gets wrong, and
moving it changes which line falls at a page bottom: measured over the 156 documents of the
hardest corpus, rounding cost 124 matched lines (54491 -> 54367 of 71610), took the median
document from 0.9065 to 0.9035, and lost 0.146 on one seven-page document whose page breaks
it flipped. Worth revisiting once page breaks are not already decided by other errors.

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

<a id="s24picbox"></a>**And the rule applies even where the paragraph already has a line
box.** `applyBlockLineHeight` very often writes one for such a paragraph - from the run the
picture sits in (a picture inside a run which carries `w:i` gets an `fo:inline` with a
line-height of its own), or from the paragraph mark - and `imageOnlyLineBox` used to skip
any block which had one, i.e. exactly the paragraphs the rule was written for. The
picture's line then came out as the paragraph's height *plus* the picture. Measured:
a 125.04pt picture in a block of `line-box="13.799pt"` put every line of the document a
flat **+4.8pt** below Word's (207.0 against 211.8) with x exact to 0.1pt - 13.799 - 11.203
is 2.6pt of added descent, plus rounding. The larger of the two now wins, the baseline
sits at the foot of the box (which is how the line manager knows the line has no descent
at all), and **the paragraph's line-spacing multiple does not apply to the picture**: with
`line-box="13.428pt" line-height="20.142pt"` around a 269.68pt diagram, the extra leading
was half the *picture* - the caption after it at y=515.8 against Word's 383.2, **+132.6pt**
- and `mutool draw -F trace` counted two diagrams on each of Word's pages 65-67 against one
on each of ours; 88 Word pages came out as 105, and are 83 now. The multiple is only
cancelled where there is one to cancel (a `line-height` beyond the box); a single-spaced
paragraph's `line-height` is its box, and raising that grew a header holding a picture by
10.3pt, which re-centred the picture in its vertically-centred cell 5.1pt below Word's.

<a id="s24picmult"></a>**The multiple still adds its own leading, though - `(m - 1)` times
the paragraph font's *natural* pitch.** Measured on `picture-header-cell`: an 11pt
paragraph (natural pitch 13.428pt) at `w:spacing w:line="276" w:lineRule="auto"`
(m = 1.15) holding a 24pt picture puts the next baseline 40.1pt below the previous one,
against 26.1 for the same picture on an exact 12pt line - so its line is 26.0pt =
24 + 0.15 x 13.428 (2.01). Cancelling the multiple outright gave 24.0 and 37.8, 2.3pt
short of Word on every such paragraph. The line box stays the picture (that is what tells
the line manager the line has no descent) and the leading is carried as the `line-height`
the manager reads as a factor over it.
An **exact** line rule still clips a picture as it clips anything else. A block whose only content is a
`external-graphic` taller than its own line box occurs in 142 documents of the three
corpora, 961 blocks.

<a id="s24piconly"></a>**What else may be on a picture-only line.** A **space leader** - a
tab, or the leader §4.5 writes for leading whitespace - reserves width and paints nothing,
and an **anchored** picture is about to be lifted out of the flow altogether, so neither is
on the line and neither makes it taller than the picture. Both used to disqualify the
paragraph. Measured on a body paragraph of [anchored 62.25pt logo][1.7pt tab][inline 25.5pt
logo], with `w:pgMar w:top="584"` (29.2pt): Word's first baseline is **63.1** - the
paragraph is exactly the inline picture's 25.5pt - where ours was 66.8, the picture plus the
13pt run's descent and line gap; it is 63.0 now. A leader that does paint (dots, a rule)
still disqualifies the line, and where the anchored picture is the paragraph's only one the
rule still does nothing (the paragraph gets the mark's line, [§2.5](#s25)).

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

<a id="s25brline"></a>**A `w:br` whose new line holds nothing that paints still takes a
line** (17.1.0) - the same rule one level down. `BrWriter` writes a line break as a nested
`fo:block line-height="0pt" linefeed-treatment="preserve"`, so where what follows on the new
line is empty (an empty run, a field with no result) nothing sizes the line and the 0pt
line-height stands. Measured on a paragraph reading
`555 test <br/> asfdsdfsdf <fld/> <br/> <fld/> <br/> <fld/> <br/> <fld/>` whose fields have
no result: Word runs y=200.7 to 293.9 - **six line boxes** - where ours ran 200.5 to 247.3,
three lines and 46.5pt lost. Such a break's own block gives up its 0pt line-height, so it
measures the paragraph's line box like any other line. (Putting content on the new line
instead does not work in both pathways: where the break's block is a direct child of the
paragraph's, which is the XSLT pathway's shape, a space there is an anonymous block of its
own and the default white-space treatment drops it against the linefeed the break itself
is.) A break immediately followed by **another** break is already right and is left alone -
the postprocessor takes the 0pt line-height off the second of a contiguous pair.
`docx4j.convert.out.fo.wordLayout.emptyLineAfterBreak=false` restores 17.0.5's behaviour.
**748 such breaks in 50 documents of the three corpora.**

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

**And a run takes no size from the mark either** (17.1.0). A `w:r` with no `w:rPr` gets no
`font-size` of its own in the FO - the XSLT pathway does not even wrap it in an
`fo:inline` - so it inherits the block's, which starts as the paragraph's effective size
(docDefaults, then the paragraph style's `w:rPr`). That was right until
[§2.4](#24-a-lines-height-comes-from-the-runs-on-it) replaced the block's size with the
dominant run's, at which point the sizeless runs silently changed size with it. Word does
not: a run takes its size from the style chain, and the mark's `w:rPr` sizes the mark alone.
Measured on a corpus header of [115.8pt picture][45 spaces][24pt text] whose mark carries
`w:sz="48"`: Word's spaces are 45 x 2.5 = 111.6pt and ours were 45 x 5.42 = **244.1**, which
wrapped the heading, made the header two lines on every page, and made 8 Word pages 10 of
ours. The block still takes the dominant run's size for its lines; its children keep the
size they were measured at.

<a id="s25emptyfont"></a>**An empty paragraph is measured in its paragraph mark's font.**
The block's content is a single space standing for the mark, and the font for it was asked
for with that space as the sample - but `RunFontSelector` adds a space to whatever span it is
building without ever firing a font action (a space belongs to the run it falls in), so for
text that is *only* a space there is no span and no font, and the block went out with no
`font-family` at all. FOP's initial value for the property is `sans-serif`, which its base-14
collection answers with **Helvetica**: that measured the line and was written into the PDF.
Measured on a document whose first paragraph is empty, its line box is **10.2pt short** of
Word's and every line of page 1 carried the -10.2 (Word 143.1 / 340.7 / 472.9 against 132.9 /
330.5 / 462.9). **137 of the three corpora's 449 renders referenced base-14 Helvetica** (121
of them drawing nothing but blanks in it), every one of the 137 has such a block, and 465 of
465 of those blocks carried no `docx4j:line-box` either, since the line-box pass has no font
to read metrics from. A whitespace-only sample now falls back to a character the selector
does act on (`XsltFOFunctions.resolveFontFamily`, `EmptyParagraphFontTest`).

A separate base-14 fallback, **Times**, is what FOP renders a `font-family` it cannot resolve
in. Across the same 449 documents exactly one document font name reaches it - a corporate
sans whose `w:altName` is itself absent and whose name matches none of `FontFallback`'s class
keywords, so it gets no class default, and whose own theme names it, so even the last-resort
document default is circular. A measured substitute for it is
[held back](#s5nokia). The other 17 documents drawing base-14 Times draw it inside `<svg>`,
where Batik resolves the AWT logical family `Dialog` itself and docx4j's mapper is never
consulted.

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

A document which declares its own alias in `w:altName` (§5.1) adds one of these too: where
that alias is what resolves the font, Word is using the alternate font outright, so the
line metrics are the alternate's.

### 2.8 List labels

Word raises a list item's first line by the amount the label's ascent exceeds the text's -
**unmultiplied** (the paragraph's line multiple is not applied to it) and **ignoring the
label's descent**. Measured: a Symbol bullet on Calibri 11pt makes the line 16.04pt rather
than 15.44pt; a Courier New `o`, whose descent exceeds Calibri's but whose ascent does not,
leaves it at 15.44pt. The label block gets the combined box and baseline, and the body
block carries `docx4j:label-ascent` for the line manager to add after the multiple.

<a id="s28def"></a>**Which level definition the label comes from.** A numbering instance may
carry a level definition of its own (`w:num/w:lvlOverride/w:lvl`, ECMA-376 17.9.8), which
replaces the abstract one; docx4j read the **abstract** level only, so an override's
`w:ind` was lost. Measured on a document whose `w:num` 32 overrides ilvl 0
with `<w:ind w:left="397" w:hanging="113"/>`: Word puts the bullet at 14.2pt and the item's
text at 19.85pt, where docx4j had no list indent at all to work from, fell back to the
paragraph's tab stops and gave the label a width of **453.6pt** - the list text then ran off
the page and Word's two pages came out as four. Where the override states no indent the
abstract level's stands. 20 documents
of the three corpora carry a `w:lvlOverride/w:lvl` at all.

<a id="s28pstyle"></a>**A level which names a paragraph style numbers only that style.**
`w:pStyle` inside a `w:lvl` (ECMA-376 17.9.24) links the level to a paragraph style.
Where the numbering reaches a paragraph through a *different* style's `w:numPr`, Word
paints no label at all and does not count the paragraph. Measured on
`numbering-label-ilvl0`, whose level 0 of numId 20 is linked to the style `NumLinked`: a
paragraph using a second style which carries the same `w:numPr` gets no number from Word
and no indent from the level (every one of its lines starts at the margin, x=72.0), and
the next paragraph of the list is numbered 6 where docx4j had counted it and reached 7.
Direct formatting is never suppressed - a paragraph whose **own** `w:numPr` names a
numbering whose level is linked to a style it does not use is numbered (the probe's numId
22, linked to a style nothing uses, prints "1."), and neither is a style the level names
or one it is based on. `Emulator.styleLinkedElsewhere`; the FO writer tells the two apart
by whether the `w:numId` came from the paragraph's own `w:pPr`
(`XsltFOFunctions.numberFor`), and HTML already passed the direct value.

<a id="s28effind"></a>**The label sits on the *effective* indent**, which style resolution
has built from the level's `w:ind`, the paragraph style's own and the paragraph's direct
formatting, in that order of precedence. The FO writer used the **level's** indent with the
paragraph's direct one over it, so a style stating a `w:ind` beside its `w:numPr` lost it:
measured on `numbering-label-ilvl0`, whose style carries both through `w:basedOn` as
`w:ind w:left="227" w:hanging="227"`, Word draws the label at x=72.0 and the wrapped line at
83.3 where docx4j used the level's 720/360 and drew them at 90.0 and 108.0.

It applies to a paragraph's **own** `w:numPr` as well. A gate which sent a direct `w:numPr`
to the level's indent instead was carried through several rounds of 17.1.0 because the
effective indent was wrong on two documents; the cause is
[the rule below](#s28lvlpstyleind) - `NumberingDefinitionsPart.getInd` read a *style-linked*
level's indent from the style it names rather than from the level, so a style the paragraph
does not use reached its effective indent. With that fixed the effective indent is right in
both, measured glyph by glyph: on a level whose own `w:ind` is 198/198 beside a `w:pStyle`
naming a List Bullet style whose `w:ind` is `left="0" firstLine="0"`, Word draws the bullets
at x=79.46 and docx4j now at 79.40, where the style's indent has no hanging indent at all,
so the label column fell back to the default tab stop and put the bullet at 13.05; and on a
level with no `w:ind` of its own, where the paragraph style's applies, Word puts the label at
27.12 and its text at 32.90 against our 27.25 and 32.90 exactly, where the level's (absent)
indent had given 13.05 and 34.35. The change moved four documents of the three corpora, two
of them 21pt of x closer to Word.

The paragraph's direct `w:ind` is merged over the result by `StyleUtil`/`Indent` attribute by
attribute, except that `w:firstLine` and `w:hanging` are one property, so a `w:ind` stating
only `w:left` leaves an inherited hanging indent exactly where it was - which is Word's own
rule, since its Paragraph dialog offers "Special: (none) / First line / Hanging" rather than
two boxes. `IndMergeTest` records it.

<a id="s28lvlpstyleind"></a>**A level's own `w:pPr/w:ind` is the level's indent**, and the
paragraph style its `w:pStyle` names does not supply one in its place (ECMA-376 17.9.24: a
`w:lvl/w:pPr` states the properties applied to a paragraph *at this level*; the `w:pStyle`
only links the level to a style). `NumberingDefinitionsPart.getIndFromLvl` read the linked
style first, which put that style's `w:ind` into every paragraph whose own `w:numPr` named
the numbering - measured above. Where the level states no indent of its own the linked
style's is still used, which is what this did from 2.7. `StyleLinkedLevelTest`.

**`w:numId 0` takes the level's `w:ind` with its label** (ECMA-376 17.9.18). That rule was
written for 17.1.0 and never fired: `StyleUtil.apply(NumPr, NumPr)` writes into the
destination object, so the inherited `w:numPr` it read to find the level had already become
the `w:numId 0`. On the probe, a paragraph switching its numbered style's numbering off
kept a 567-twip hanging indent Word does not draw.

<a id="s28rpr"></a>**A level's `w:rPr` formats the number alone** (ECMA-376 17.9.24); the
paragraph's text is formatted by the paragraph. docx4j wrote the level's rPr on the
`fo:list-item-body` as well as on the label, so a level `<w:b/>` made the whole paragraph
bold - measured on a real document, Word draws the number in Tahoma-Bold and the text after
it in Tahoma on one line, and reading the level's rPr for both cost that document 0.074 of
line parity. Only the label carries it now; the body keeps the **paragraph mark's** rPr,
which is what it had before any level rPr was read. Merging the two for the label is
unchanged - the level's, with the paragraph mark's applied over it, the font excepted (it
comes from the numbering, since anything else would change the bullet). The other order
was tried against the corpora and dropped: it cost three documents 0.002 to 0.065 of line
parity and two of them their page count, and gained nothing.

<a id="s28ind"></a>**A partial `w:ind` merges with the level's, attribute by attribute.**
A paragraph stating only `<w:ind w:right="22"/>` keeps the level's `w:left` and
`w:hanging`; the paragraph's own values win where it states them (`IndentRightTest`).
**Where the level has no hanging indent** the label's width does come from the paragraph's
tab stops, and there a `w:val="clear"` entry is not a stop at all - it removes an inherited
one (ECMA-376 17.3.1.37) - and `w:tabs` need not be in position order: the nearest stop past
the label is taken, cleared entries skipped.

<a id="s28lgl"></a>**`w:isLgl`** (Word's "Legal style numbering") displays the levels a
number **inherits** in decimal, whatever their own `w:numFmt`, at whatever level it is
stated; the level carrying it keeps its own format (Word's built-in Article/Section
numbering, whose ilvl 1 is `decimalZero` with `w:isLgl`, prints "Section 1.01" - the
`IsLglTest` docx). docx4j honoured it at ilvl 1 only, and rewrote `%1` alone: measured on a
document whose abstractNum carries `<w:isLgl/>` at ilvl 1, 2 and 3 with `w:lvlText`
`%1.%2.` and `%1.%2.%3.` over an `upperRoman` ilvl 0, Word prints "3.6.2." where docx4j
printed "III.6.2.", so every second- and third-level heading failed to match.

<a id="s28inline"></a>**A centred or right-aligned numbered paragraph** is laid out by Word
as **one line, number included**: the number, the `w:suff` separator, and the text are
centred (or right-aligned) together. An `fo:list-block` cannot do that - it puts the label
in a column of its own at the list's start-indent - so such a paragraph is written as a
plain `fo:block` with the number as an `fo:inline` at the head of it
(`XsltFOFunctions.createInlineLabel`), the block keeping the list's own geometry: it begins
at the level's number position (`w:left` less `w:hanging`) and its first line is not
indented again. The separator is `w:suff` (ECMA-376 17.9.29): a tab, which is an
`fo:leader` of the level's hanging width less the label's measured width, so that the text
starts at the level's text position; a space; or nothing.

Measured on three corpus documents. On a centred TOC entry in a 4920tw cell Word puts "1."
at x=**129.4** and the entry's text at **147.5** - 18.1pt apart, the level's 18pt hanging
indent - and the three entries' first lines all share one centre, 198.6, which is the
centre of the whole cell rather than of the cell less the list indent. docx4j put every
label at the cell's content edge, x=**90.0**, while centring the text alone: -39.4, -26.0
and -77.8pt on the three labels. On a right-aligned Cyrillic heading the label was
**152.1pt** left of Word's, and in a third document's table cell 5.2pt outside the page's
own left margin. Left-aligned and justified paragraphs keep the hanging-indent geometry,
where the label does stand at the list indent.

<a id="s28wide"></a>**A label wider than the hanging indent** does not overprint the text:
Word keeps the label's natural width and sends the text to the first tab stop past it (or,
per `w:suff`, one space past it, or straight after it). Measured on a document whose
paragraphs are `<w:ind w:left="40" w:hanging="6"/>` with a `<w:tab w:val="left"
w:pos="358"/>` and a `(%1)` label: Word draws "(" at x=56.66, "2" at 60.02, ")" at 65.54, a
space at 68.90 and the text's "M" at **72.98** - the 358tw stop - where our 0.3pt label
column produced "(M2i)tverpachtet". The label column
(`provisional-distance-between-starts`) is therefore the hanging indent only where the
label fits inside it; otherwise it is what `w:suff` says, on the same crude label-width
estimate the no-hanging case already used. 10 documents of the three corpora have a label
column under 5pt.

*And it is unconditional*, `w:compat/w:doNotUseIndentAsNumberingTabStop` notwithstanding.
That flag ("Ignore Hanging Indent When Creating Tab Stop After Numbering") would send the
text to the first real stop past the label instead, and keying the rule on it was measured
over the three corpora in 17.1.0: **all eight documents it moved got worse and none
better** - 0.922 to 0.778 of Word's lines, 0.855 to 0.702, and six more losing 0.02 to
0.04 - so Word's own PDFs of them show the hanging indent standing. See
word-layout-settings.md §4(d).

<a id="s28numid0"></a>**`w:numId w:val="0"` takes the paragraph out of the list** (ECMA-376
17.9.18), so neither the label nor the level's `w:ind` applies - only the paragraph's own.
docx4j suppressed the label but kept the level's indent: measured on a heading styled with
a style whose numbering (numId 2, ilvl 5) carries a 57.6pt hanging indent and which is
overridden with `<w:numPr><w:ilvl w:val="0"/><w:numId w:val="0"/></w:numPr>` and
`<w:ind w:left="720"/>`, Word draws it at x=78.5..541.2 and docx4j drew the same 462pt of
text at 20.9..483.1 - exactly 57.6pt left, on ten paragraphs. Only the components the
inherited level contributed are dropped, so a `w:ind` the style states itself survives.

<a id="s28font"></a>**A level `w:rPr` which names no font** - Word writes
`<w:rFonts w:hint="default"/>`, which says only how to choose between fonts it does not
state - leaves the label in the paragraph's own font. Reading it as a font of its own reset
the label to the document default: measured, Word draws the whole line in Arial-BoldMT
where docx4j drew the label in Tinos-Bold beside an Arimo-Bold heading.

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

<a id="s3ctxdefault"></a>**Two paragraphs which state no `w:pStyle` are of the same
style** - the default paragraph style - so the rule pairs them. `docx4j:pstyle` is `""` for
such a paragraph and the comparison read `""` as "unknown" and skipped it, so a document
whose paragraphs are all plain Normal never got the rule at all. Measured on a planner whose
shaded Normal cells carry `w:contextualSpacing` against 10pt of docDefaults space-after:
Word's grid of baselines is 93.9 / 103.0 / 112.3 / 121.5 where ours split into 93.6 / 102.8 /
112.0 for columns 1, 3, 5 against 92.8 / 102.0 / 111.2 for 2 and 4 - **+9.9pt** - and 37
Word pages came out as 39.

<a id="s33"></a>**Hard page breaks.** Word breaks the page **at** the break: where a
`w:br w:type="page"` follows content in its own paragraph, what precedes it stays on the
page it is on and what follows opens the next. docx4j moved the break to the front of the
paragraph (`w:pageBreakBefore`, the `w:br` dropped), which took the text before it to the
next page as well: measured with `mutool draw -F trace` on a document whose cover picture
and page break share a paragraph, Word puts the 270x225pt picture on page 1
(`transform="270 0 0 225 162.65 347.59"`) and docx4j put it on page 2 at y=80.58, so every
line of page 2 sat 268.4pt low and 54 Word pages came out as 44. 21 of 99 documents of a
corpus of long real documents hold such a paragraph. The paragraph is split in two at the
break where **`w:compat/w:splitPgBreakAndParaMark`** ("Always Move Paragraph Mark to Page
after a Page Break") resolves on, which is what Word 365 was measured doing in every mode
and so is its default in every mode ([§1.6](#s16settings)); a document stating
`w:val="0"` takes the whole paragraph to the next page, as docx4j did to 17.0.5
(`convert/out/common/preprocess/PageBreak`). Because the two halves are one
paragraph nothing is doubled between them: the first keeps the space-before and loses the
space-after, the second the other way about, and the second takes neither the numbering
label - Word numbers the paragraph once - nor the first-line indent. A `w:sectPr` belongs
to the paragraph's end, so it goes with the second half.

Both of those - the split, and the plain `w:pageBreakBefore` conversion where the break
comes first - are done by a walk which visited the **body's own children only** until
17.1.0, so a paragraph inside a `w:sdt` (a table of contents, a cover page, any building
block) was missed and its break stayed nested in an `fo:inline`, where FOP ignores it.
Measured on a document whose contents control wraps two such paragraphs: FOP laid the
paragraph's 12pt space-before down on the **previous** page and the leading block-level
child ate its first-line indent - Word's page 2 heading at `y=97.0 x=72.0` against docx4j's
`85.1 / 89.8`, and every line of that page carried the -11.9. The walk now descends into
block-level content controls; it still does not descend into a **table**, where a page
break belongs to the table (below).

<a id="s33each"></a>**Every break costs a page boundary of its own.** Where two of them
are consecutive, what lies between them - nothing but a paragraph mark - is a page with
nothing on it. Measured on `page-empty`, where Word has 13 pages and docx4j had 11:
Word gives an empty page to two `w:br w:type="page"` in one paragraph, to two paragraphs
each holding one, and to an empty paragraph carrying a **typeless `w:sectPr`** followed by
a break-only paragraph - the section break opens the page, the break paragraph's mark is
the whole of it, and its own break opens the next. The paragraph is split at each of its
breaks now (`convert/out/common/preprocess/PageBreak` used to convert the first and leave
the rest nested in an `fo:inline`, where FOP ignores them), and
`WordLayoutFixups.mergePageBreakParagraphs` folds a break paragraph into what follows it
only where that is not itself an empty break paragraph, and - for the flow-opening case -
only where what follows does not break the page on its own account (a section whose first
paragraph holds a break with text after it is split in two, and the second half carries
the one page Word gives).

**A `w:pageBreakBefore` paragraph after a page break is not one of them**: it is already
at the top of a page and Word adds none for it (`page-empty`'s S6). Reading any
`break-before="page"` on the next block as a second break cost two spurious pages of
fourteen on a corpus document whose Heading 1 style carries `w:pageBreakBefore`, and one
of seven on another.

One corpus document disagrees with the probe and has not been reconciled: its two
consecutive break-only paragraphs cost it a page Word does not give (Word's 22 pages
became 23), while its line parity rose from 0.613 to 0.682 on the same change. Over the
three corpora the rule is a clear gain - 97 more matched lines and +0.0010 mean parity on
that corpus, a page-count match gained on another, no document regressing - so it stands,
and what makes Word skip that one page is open.

A paragraph holding only a page break leaves no empty
line at the top of the new page. The next paragraph's space-before is dropped there where
**`w:compat/w:suppressSpBfAfterPgBrk`** ("Do Not Use Space Before On First Line After a
Page Break") resolves on, which is **from compatibility mode 15** where the document does
not state the flag, and kept below mode 15 ([§1.6](#s16settings)). The break moves to the next paragraph, or
to the next container which takes no space (a floating table or a picture already positioned
out of the flow, §6.8 and §9.1) - measured on `table-floating-anchor`, where the page of a
page-anchored table began with an empty line docx4j put 25.5pt above Word's first paragraph.
It does not move to a **table**: measured on a corpus document whose hard break is followed
by one, Word keeps that line, and dropping it lost a page of the nineteen.

<a id="s33cell"></a>**A `w:br w:type="page"` inside a table cell is ignored outright** -
wherever it stands, and however many of them there are. Word paginates on the paragraph
property, not on the break run. `page-break-in-cell` varies the position and the count one
at a time, each case a table of its own introduced by a paragraph: a single break at the
head of the first cell's first paragraph, two of them there, one in a later paragraph of
the cell, one in a cell which is not the first, and one in the second row. Word gives none
of them a page - every table shares a page with its introduction - and its seventh and
last page is the one the `w:pageBreakBefore` case opens. 17.1.0 had promoted a single head
break to the table, on the strength of two corpus documents whose page 1 had looked short,
and had eight pages (`WordLayoutFixups.dropPageBreaksInTableCells`). H10's two corpus
measurements the other way stand: their tables share page 1 with the paragraph above them
in Word, and both those documents gained Word's page count on the change. One document of
corpus 3 lost a page to it (Word's 19 against our 18) and is the residual; its break is
the only one in the document, and what replaces the page there is not established.

Whether a break run is a `w:br` or a paragraph property is not visible in the FO - both
are `break-before="page"` - and the nesting that used to tell them apart differs between
the two exporters, so `BrWriter` marks the block it writes for a `w:br`
(`WordLayoutFixups.HINT_BREAK_RUN`, stripped before FOP sees it).

<a id="s33sect"></a>**A page break at the end of a section costs no page.** Where the break
has nothing left in its section to move onto - the paragraph holding it and the
section-break paragraph are the last two, which is what a document whose every section ends
in a page break looks like - the section that follows opens a page anyway, and Word does not
add one for the break as well. Measured on `tab-toc-pageref`, whose third section ends that
way: Word's document is 6 pages and its fourth section opens page 4 at the top (its first
heading at y=84.3, the same as on every other page), where docx4j had a blank page 4
carrying nothing but the empty block the break had been moved onto, and 7 pages. At the end
of the **document** the page is Word's own - `page-blank` ends in a page break and Word
gives it a ninth page - so a break with no section after it keeps its page
(`WordLayoutFixups.mergePageBreakParagraphs`). The blank page was not
`force-page-count`: docx4j writes `no-force` on every page-sequence already, and Word's own
`w:pgNumType w:start` restart (98 in that probe) costs no page either.

The rule was re-measured in 17.1.0 against the real documents which hold that shape, since
its page counts had been read as a regression. It is right, and the page count was the
coincidence: on a 26-page document with four such section ends, the **old** output was 26
pages with **four** blank pages (12, 22, 24 and 26) where Word has 26 pages with **one**
(page 26, the document's own end, which the rule keeps). The rule removes exactly the three
Word does not have; that document is now 23 pages because its content is three pages
shorter than Word's for unrelated reasons. A second document went 34 pages to 32 against
Word's 31, and a third kept Word's 56 exactly. 20 documents of the three corpora hold a
page break with nothing but a `w:sectPr` paragraph after it. (Word does emit genuinely
empty pages elsewhere - one 22-page document has two and one 56-page document six, none of
them at a section end - which §10 records.)

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
true until 17.1.0, so the override did nothing: measured on a document whose `NormalWeb`
style carries it and which overrides it on 20 paragraphs, 17 came out 14pt low - Word's
first divergence at y=171.4, docx4j's at 186.2, and the next gap 30.7pt against 58.9.

<a id="s35autospacecell"></a>**A cell disagrees, and is not settled.** The rule above drops
auto spacing at the top and bottom of a table cell, which is measured. One corpus document
disagrees: its `Data Updated` cells hold bulleted paragraphs carrying
`<w:spacing w:before="100" w:beforeAutospacing="1" w:after="100" w:afterAutospacing="1"/>`
and Word's event-row pitch is **34.8pt** (Oct 13 at y=277.3, Oct 14 at 312.1) where ours is
21.5 - **-13.3pt per row** over about 100 rows, which is a page of its five. Our FO writes
`space-before="14pt" space-after="0pt"` on the `fo:list-block` there: the *after*-autospace
comes out zero, and the 14pt before-space is then discarded by FOP at the start of the
cell's reference area. Whether the discriminator is the list, the mode, or the explicit
`w:before`/`w:after` beside the autospacing is not established, so nothing is changed; 9
documents of the three corpora have autospacing inside a cell.

<a id="s35"></a>**Table cells.** A paragraph's space-before applies at the cell top, and its
space-after at the cell bottom - **in every compatibility mode**. docx4j pinned the
space-after only from mode 15 until 17.1.0; measured on a mode-14 document whose cell
paragraphs carry `w:spacing w:before="60" w:after="60"` (3pt each), Word's row pitch is
119.6 -> 137.6 -> 155.6 = **18.0pt** = 3 + 11.5 + 3, where docx4j's was 110.7 -> 125.7 ->
139.2 (15.0 / 13.5, the space-before only) and the deficit grew to -25.4pt by y=360 on page
1, with x matching to 0.3pt throughout. Confirmed on a second mode-14 document, a
one-page form: Word's row baselines are 21.73 / 51.05 / 73.17 / 101.74 (pitches 29.3 /
22.1 / 28.6), docx4j's were 18.46 / 45.71 / 66.29 / 91.63 (27.25 / 20.6 / 25.3) and are
18.46 / 47.42 / 69.43 / 97.49 (28.96 / 22.0 / 28.1). 120 documents of the three corpora
are below mode 15 with a cell paragraph carrying `w:after`; a handful of them, whose rows
were the right height only because the space was missing, gain a page where their last
row now spills. XSL-FO drops space at the end of a
reference area, so `WordLayoutFixups` pins it there with
`space-after.conditionality="retain"` - and the cell's edges are boundaries
`w:contextualSpacing` cancels the space at, the **only** paragraph of a cell included,
whatever **`w:compat/w:allowSpaceOfSameStyleInTable`** says: the compat-breaks probe
pair, identical but for that flag, renders identically in Word 365 (row pitch 13.7pt
either way), so the flag is resolved but ignored ([§1.6](#s16settings)).
Cancelling it walked the cell's paragraphs in pairs, so a single-paragraph cell was never
examined at all: measured on a planner whose cells hold one contextual paragraph each
against docDefaults `w:after="200"`, Word's row pitch is 10.1pt (the 9.199pt line box plus
`w:trHeight` 199) and docx4j's was 19.9pt, +9.0pt on the first row and +9.5 on every row
after it - 37 Word pages came out as 43. It is the **only** paragraph of a cell because
where the cell holds several there is no next paragraph for the "same style" test to be
about, and Word applies the last one's space-after: measured on a document whose cells end
in a bulleted List Paragraph carrying `w:contextualSpacing` and `w:after="200"`, Word's row
pitch is 25.0pt and suppressing the space gave 19.9.

<a id="s33rowtop"></a>**Levelling that space across the row was measured and not shipped.**
Word appears to start every cell of a row on the same baseline: on a label/value table whose
two cells of a row carry `w:spacing w:before="120"` and `w:before="60"`, Word puts `Client:`
and its value on one baseline, y=123.2, where the per-cell space put ours at 122.3 and 119.3
- the 3.0pt difference exactly - and the extractor then reads two lines where Word reads one.
Taking the row's largest retained space-before for every cell of it does fix that line, but
over the three corpora it wins one line on that document and loses **0.042 and 0.025 of line
parity** on two others whose cells differ by 3.95 and 2.65pt and where Word does *not* level
(a 1pt dead band, which removes the sub-point cases, does not save them). What decides it -
`w:trHeight`, `w:vAlign`, whether the cells' first paragraphs share a style - is not
established, so the per-cell space stands. 33 rows in 11 documents of the three corpora carry
differing retained space at a row's top.

**The paragraph a nested table forces.** OOXML requires a `w:p` after a `w:tbl` inside a
`w:tc`, and Word gives that one no line at all. Measured on a mode-14 header whose outer
row 1 holds three nested rows (baselines 23.5 / 33.8 / 43.9, pitch 10.2): Word's next outer
row starts at 54.7, 10.8pt later, with no room for an 11.5pt line, where docx4j went 43.7
-> 64.8. The header table then ended 28.9pt low and the body started 35.7 to 37.0pt low,
which turned Word's two pages into four. Only the *cell-final* paragraph that follows the
table: an empty paragraph anywhere else in a cell keeps its line, as [§2.5](#s25) says.

Word writes that paragraph with a **run** in it whose text is empty, so it reaches the FO
as a block holding an empty `fo:inline` (plus the preserved space the empty-line rule
above gives a block which would otherwise build no line area), and a blank test that
stopped at the first element child never fired on the shape the rule was written for.
Measured on `table-grid-edge-compat12`, whose P03 row holds a nested table in its left
cell: Word's next paragraph is at 283.3, 27.4pt below the nested row's baseline, where
docx4j went to 294.8 - one 11.5pt line and its spacing too many, after each of the probe's
two nested tables. Empty inline wrappers and whitespace-only text are now blank; a
graphic, a leader, a nested block or a positioned container is content, and the block
keeps its line.

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

<a id="s3contmargin"></a>**The wrapper must not write the `margin` shorthand where it has
spacing.** A shading container carries `margin-top="0in" margin-bottom="0in"` so that no
white strip appears between shaded paragraphs; the same element carries the paragraph's
`space-before`/`space-after`, and FOP 2.11 lets the shorthand win. Proved by running two
blocks through FOP: one with `margin-top="0in" space-before="30pt"
space-before.conditionality="retain"` renders at y=80.6, level with its sibling cell; the
identical block without `margin-top` renders at 122.6, exactly 30pt down. Measured on two
corpus documents of one template whose row-1 cells Word puts on baseline 145.3 and docx4j
put at 142.0: the -3.3pt grew to -14.0pt by y=695 and moved the closing paragraph off the
page Word's own `w:lastRenderedPageBreak` puts it on. The shorthand is now written only on
the side which has no space to lose.

<a id="s3contindent"></a>**The wrapper must not pass on its first paragraph's indents.**
`start-indent`, `end-indent` and `text-indent` are inherited XSL-FO properties, so every
later paragraph of the group which does not set its own was displaced by the first one's.
Measured: a shaded group opening with a `ListParagraph` (`w:ind w:left="1440"
w:hanging="360"`) put the body paragraphs after it at x=126.0..236.0 where Word draws them
at 72.0..182.3 - the same width, 54pt right (72 inherited less the 18pt text-indent) - on
eight such blocks in one document. The shading wrapper is reset to zero and the paragraphs
inside keep their own.

<a id="s3bdrrun"></a>**One box for a run of identically bordered paragraphs, whatever their
shading.** `Containerization` groups by border and then, *inside* that group, by shading,
which is the right nesting - but both wrappers are built from the same paragraph's
properties, so the inner one repeated the outer's top and bottom borders and their
`w:space` padding. With a 0.5pt border at `w:space="1"` that is 2 x (0.51 + 1) = **3.02pt
per shading change**. Measured on a planner whose cells hold three identically bordered
paragraphs in three different fills: Word's row pitch is 61.0 -> 70.1 -> 79.2, i.e. 9.1pt,
the bare Arial 8pt line box, so Word adds **nothing** at the shading change, where docx4j
went 61.3 -> 70.5 -> 82.7 and the drift reached +68pt by the foot of page 1; 16 Word pages
came out as 21, and are 16. The inner container now drops the border and padding the outer
one draws (its left and right borders stay, since those are drawn outside the text either
way and cost no width, above). A paragraph carrying both a border and shading of its own is
the same shape with one paragraph in it, and was 3.02pt too tall for the same reason. 33
documents of one corpus have a bordered wrapper directly wrapping another block.

**List items.** Space-before and space-after belong on the `fo:list-block`, not on the
block inside `fo:list-item-body` where FOP does not apply them - and so does a **hard page
break** inside the paragraph, for the same reason: FOP laid the list block's space-before
down on the page the break leaves. Measured on a document whose `Heading1` carries
`<w:spacing w:before="360" w:after="240"/>` and whose first run is `<w:br w:type="page"/>`:
Word's heading is at y=85.0 - the 56.7pt top margin plus 18pt of space-before plus its
ascent - where docx4j's block top was 56.75, the top margin exactly, and every line of that
page carried the -18.2. The `break-before` moves to the `fo:list-block`, which then needs
`space-before.conditionality="retain"`, since XSL-FO discards space at the start of a
reference area and [§3.3](#s33) is Word's rule here: space-before is honoured at the top of
a page after an *explicit* break (an automatic one never writes `break-before`).

**Widow control.** `w:widowControl` off maps to `widows="1" orphans="1"`; on is the default
on both sides. Word's widow and orphan decisions match FOP's once the line breaking does
(`widow-orphan`).

<a id="s39"></a>**Keeping a paragraph's lines together.** `w:keepLines`, which every
built-in heading style sets, maps to `keep-together.within-page="always"` in FO and
`page-break-inside: avoid` in CSS. Without it a heading broken over two lines by a `w:br`
could straddle a page, because the `w:br` is a nested block, which ends FOP's line sequence
and puts widow control out of reach (§10).

<a id="s39keepchain"></a>**A keep Word cannot satisfy it drops; FOP overflows the page.**
`w:keepNext` becomes `keep-with-next.within-page="always"`, which FOP writes as a penalty
of `KnuthElement.INFINITE` between the two blocks. Where a whole run of paragraphs carries
it - a numbered clause list whose every item keeps with the next, which is how a contract
template is written - the breaker has no legal break anywhere in the run, and rather than
break it FOP puts the lot on one page and lets it run off the bottom. Measured on a
document with 88 `keep-with-next="always"`: **one page held 1,037 lines and ran to
y=7693.5 on a 792pt page**, where Word spreads that content over fourteen; a second, with
10,011 of them, ends 42 pages short of Word (Word's page 50 ends at y=521.7, ours at
675.2). Between them, **52 pages** - the largest single page deficit of the three corpora.

Word applies `w:keepNext` locally: the paragraph is kept with the next where the two fit
on a page together, and where they do not the keep is simply ignored at that point - the
heading stays with the first lines of its paragraph and the rest flows on. So the keep
chain is bounded (`WordFlowLayoutManager.boundKeepChains`, on the element list the flow
returns, after space resolution has turned the `BreakElement`s into penalties): where the
height accumulated since the last break the breaker may take is more than three times the
page's available BPD, the infinite penalties of that chain are reduced to a large but
finite one (900 against FOP's 1000). The breaker can then break inside the chain, and
because the penalty is still large it breaks there only where it must. Nothing changes for
a keep chain that fits, and that is every keep in a document Word lays out the same way.

**Three times the page, not one.** The height summed there is the flow's own element list,
where a table's rows are boxes beside the block that holds them and where line heights each
a little taller than Word's accumulate, so it over-estimates what the page must hold.
Measured on a 71-page document: its widest keep chain sums to 726859 against a 650900 body
- 12% over - and Word puts all 166 lines of it on one page; bounding that chain gave the
document a 72nd page and cost the corpus a page-exact document. At three times the page
that document and the 1037-line overflow above are both at Word's own page count exactly
(**45/45**, where the overflow gave 35), and 2.5x, 4x and 6x are each worse on one of the
two (46, 44 and 42 pages). Properties
`docx4j.convert.out.fo.wordLayout.boundKeepChains`, `...keepChainPenalty` and
`...keepChainTolerance`.

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

Being inside that cap is **not** enough. Word compresses only when the line it would
otherwise leave is very loose: measured on three corpus lines it refused compressions of
22.5%, 15.1% and 13.3% and took an alternative stretched by only 38.5%, 16.1% and 12.8%
instead, while every line it did compress in the `break-justified` golden had a far looser
alternative again. `docx4j.convert.out.fo.wordLayout.minStretchToCompress` is how stretched
that alternative has to be, as a fraction of the spaces' natural width; swept on the probe,
0 and 0.1 break 98% of its lines as Word does, **0.2 and 0.3 break 100%**, 0.5 gives 83%
and 0.7 gives 57%. The default is 0.30, which is also the maximum over the batch-1
documents carrying those lines (77.5% -> 80.2% of lines matched). 0 restores 17.0.5's
behaviour.

<a id="s42shiftreturn"></a>**A line that ends in a soft return is justified**
(`w:compat/w:doNotExpandShiftReturn`, 17.1.0). Word stretches the spaces of a line ending
in a `w:br` with no type, inside a `w:jc="both"` paragraph, exactly as it stretches any
other line of it - unless the document sets that flag, which is one of the seven the Word
2007 default block writes and which no Word 2010 or later document carries: 21 documents of
the three corpora state it and keep the old behaviour, and every other document gains the
rule ([settings §2](word-layout-settings.md)). docx4j had the flag-on behaviour hard-wired for
every document: `BrWriter` writes the break as a nested `fo:block`, so the line before it
is the last line of a line sequence, and FOP aligns a last line by `text-align-last`,
whose value for a justified block is `start`.

The paragraph's block is marked `docx4j:justify-soft-return` by the fixups - only where
its **only** block-level content is soft returns, so that a line before a positioned
picture or a text box is not justified as well - and `WordLineLayoutManager` justifies the
last line of each of its line sequences bar the paragraph's own last one. It has to be the
line manager rather than the FO, because `text-align-last` is inherited and there is no way
to address the anonymous block a nested `fo:block` splits a paragraph into; and it has to be
done when the sequence is closed rather than when it is broken, since FOP puts the
infinite-stretch filler glue of an unjustified last line in at `Paragraph.endSequence()`.
The two exporter pathways put the break in different places - a sibling of the paragraph's
content in the XSLT one, nested in the run's `fo:inline` in the visitor one - which is why
the rule is expressed twice: as the line manager's own `knuthParagraphs` split (visitor)
and as `WordBlockLayoutManager` telling the manager that a block follows it (XSLT).
`CompatFlagsTest` measures both. Property
`docx4j.convert.out.fo.wordLayout.justifySoftReturn`.

**Measured over the three corpora.** The rule marks **36 documents** and about 130
paragraphs, and the harness's own aggregate does not move at all - no document's page
count, line count or matched-line count changes (one corpus gains 4 matched lines, the
other two are identical) - because justifying a line changes where its glyphs sit and not
where it breaks. The measurement is therefore the lines' right edges against Word's:
**49 lines move, 46 of them closer to Word and not one further**, recovering **2,878pt of
x**. Two lines that had ended 57.6pt and 3.5pt short of Word's right edge now land within
0.4pt of it. The rule fires only where a paragraph's whole block-level content is soft
returns - one that also holds a positioned picture, a text box or a column break is left
alone - which is why 36 documents carry it and fewer lines move.

### 4.3 Break opportunities Word does not take

Word does not break after a solidus, where UAX #14 lets FOP break: a URL, or a pair of
words joined by a slash, goes whole to the next line. Word does not break a line at a tab
either.

Word does break **before** a word which begins with a solidus, where UAX #14 does not (rule
LB13 forbids a break before class SY), so FOP made the space and the slash-led word one
unbreakable unit: measured, an 84.2pt header cell holding "Roll Number /Registration
Number" was painted "Roll" and then a 93.5pt "Number /Registration" running 6pt outside the
table, where Word sets "Roll Number " and "/Registration ". FOP's elements for it are
`box("Number") box(" ") box("/")` - the space is a non-breaking box - so the line manager
puts a zero penalty after it (and relaxes the infinite penalty in the justified form).

A `w:br` which **opens** a paragraph gets a line of its own, as one which ends it does:
measured, Word's gap after a one-line paragraph followed by
`<w:p><w:r><w:br/></w:r><w:r>...` is 25.7pt - two 12.85pt lines - where ours was 13.1pt.

A literal U+00AD in a `w:t` is dropped from the FO: FOP does not break at a soft hyphen and
most fonts have no glyph for one, so it was painted as a notdef box in the middle of a
word. Word shows one only where it breaks the line there. A `w:br` at the end of a
paragraph gets the empty line Word gives it.

#### The emergency break: a word too long for any line (17.1.0)

When no legal break will do, Word breaks the word itself. UAX #14, which FOP follows,
offers no break inside `KONS_ADATOK_SZERZODO_ADATAI_TERM_SZEMELY_LAKCIM_VAROS` or a rule of
underscores, so FOP painted the whole of it however narrow the measure: over the three
corpora **1959 lines were painted outside their page in 73 documents**, against Word's 207
in 20 - and Word's are deliberate overhangs.

Measured on a corpus golden, an insurance template of long placeholder tokens in a 279pt
cell. Word does two things, in this order:

1. **The word gets a line of its own.** The line before
   `tartóval):ELSEENDIFIF_csak_az_uzembentartoval_THEN(megegyezik` ends at x=453.9 with 85pt
   of the measure unused; Word did not fill it with the word's head.
2. **Then the word is broken wherever the measure falls** - mid-token, at the character, and
   with no hyphen: `...THEN(m` to the cell edge at 538.8, then `egegyezik...` on the next
   line. `KONS_ADATOK_SZERZODO_ADATAI_TERM_SZEMEL` / `Y_SZUL_HELY,` is the same.

So a word wider than the whole measure is split into one glyph mapping per character with a
zero-width break between each pair, and the greedy loop may take one of those breaks only
once the line holds nothing but that word - which is what makes step 1 happen, since until
then the ordinary break before the word is the last one that fitted.

Three limits:

- A word may be **several boxes** - a punctuation-led token like `«${(entries.…`, or a URL,
  which FOP maps to a run of boxes joined by infinite penalties - and every splittable box
  in the run is broken, all of them marked as one word so that "only once the word has a
  line to itself" still means the whole word (17.1.1). Before that the rule required a
  word to be a single box and such tokens were never seen as over-long at all: one ran to
  x=623.0 on a 595.3pt page. A box whose mapping cannot be split is left whole but still
  counted as part of the word, so it does not block a break in its neighbours.
- A mapping which carries a **substituted glyph sequence** - a complex script, or an
  OpenType feature FOP applied - is left alone: such a word cannot be rebuilt from its
  characters and the fragments would render unshaped. (Inert on every corpus document the
  rule touches.)
- **The word must be over the measure by more than an inch.** The rule has to be
  conservative, because a word which does not fit is very often a measure *we* got wrong
  rather than a word Word breaks, and breaking it then hides the real defect and costs a
  line. Measured over every corpus document carrying the shape, each word that overflowed by
  less than an inch was one Word fitted or let overhang - `BALES` by 2.6pt and `'A'` by
  1.4pt in a certificate whose columns Word autofits a fraction wider than we do (that
  document scores 1.000 without the rule and 0.826 with a 1pt tolerance); `CANTIDAD` by 20pt
  in a cell whose text Word turns on its side, where our measure is the unrotated width;
  `Telecomunicaciones.` by 20pt in a table whose grid we still fit wrongly. The words Word
  does break overflow by 78 to 345pt. The `table-autofit` probe's 23.976pt column against a
  23.988pt word - 0.012pt - is the smallest of them.

Kerning and glyph positioning inside the word are lost, which is the price of breaking it;
the word's total width is preserved exactly (the residual goes on the last character).
`docx4j.convert.out.fo.wordLayout.emergencyBreak=false` turns the rule off.

**The inch is now known to be the single most valuable number left in this rule, and why it
cannot yet be lowered** (b2-batch28). At 8pt instead of 72 the mean line parity rises 0.8816
to 0.8879 on the 191-document corpus, 0.8496 to 0.8532 on the 156 and 0.8810 to 0.8836 on
the 102 long ones - 1013 more lines matched, the medians up on all three, the probes
byte-identical, the corpus's largest single deficit 0.626 to 0.738 and four documents up by
0.12 to 0.26. On an unbiased quarter of the first corpus it is monotone (0.8967 at 72pt,
0.9005 at 36, 0.9011 at 18, 0.9051 at 8, 0.9052 at 1) with four documents up and none down.

What stops it is three documents whose **page count** it breaks, and they break because the
measure it is handed is wrong, not because the word is long. Instrumented, the worst fires
**166 times into measures of 5.2pt and 23.2pt**, gaining 529 lines and five pages: a column
that narrow holds one character, so breaking into it shatters the word rather than wrapping
it. The documents the rule is *for* fire 3 to 21 times into measures of 36 to 268pt. Neither
an absolute threshold (36pt leaves two of the three page counts broken) nor a relative one
(the largest win overflows its measure by only 6.7%) separates them, because the difference
is not in the word - it is that we sized those columns wrongly, and that document is the one
the triage ledger names for the content-autofit defect. **Lower the tolerance to 8pt once
that is fixed**; `docx4j.convert.out.fo.wordLayout.emergencyBreakTolerance` sets it in
points, so it can be re-measured without a build.

Measured over the three corpora, the conservative rule takes the lines painted outside their
page from 1959 in 73 documents to 1915 in 70, against Word's 207 in 20. Most of what remains
is not this rule's to fix: it is text beside a picture whose wrap we do not narrow (E24), a
dropped `fo:float` (E27), an over-wide grid, and cells whose text Word turns on its side.
What it does buy is line parity where the token *is* the content: two documents of one corpus
go 0.500 -> 0.661 and 0.559 -> 0.701, and a third 0.579 -> 0.673.

### 4.4 Tab stops

Where a tab starts is not known when the FO is written, so a mid-line tab is an `fo:leader`
of no length which the line manager sizes when the greedy loop reaches it; the paragraph's
block carries the stops it needs (`docx4j:tabs`, as `pos:align:leader` in twips from the
left margin, plus `docx4j:tab-default` and `docx4j:tab-ind`).

The stops, measured against Word's PDFs:

- the paragraph's `w:tabs`, the numbering's tabs, and the implicit stop a hanging indent
  makes at the left indent - which **`w:compat/w:noTabHangInd`** ("Do Not Create Custom Tab
  Stop for Hanging Indent") removes, travelling to the line manager as
  `docx4j:no-tab-hang-ind` ([§1.6](#s16settings)); beyond the last of them a grid at
  `w:defaultTabStop` (720 twips where absent);
- **all measured from the left margin**, not from the paragraph's indent;
- a custom stop clears the default grid stops before it, and the grid resumes past the last
  custom stop;
- a stop exactly at the current x is not the next stop;
- <a id="s44merge"></a>**a paragraph's own `w:tabs` merge with the ones it inherits** from
  its style chain, rather than replacing them (ECMA-376 17.3.1.38), and a
  `<w:tab w:val="clear" w:pos="..."/>` **removes** the inherited stop at that position
  instead of being copied through as a stop of its own (17.3.1.37).

What each alignment does with the text between this tab and the next (or the paragraph's
end): **left** - the text starts on the stop; **right** - the text's end sits on the stop,
its trailing space hanging past it; **centre** - the text's middle sits on the stop;
**decimal** - its `w:decimalSymbol` (default `.`) sits on the stop, and text holding none
is right-aligned on it.

Word cannot move backwards: a stop the text has already passed, and a right or centre stop
whose text does not fit before it, advance nothing. A **left** stop beyond the paragraph's
right indent - but still inside the text column - is honoured, and the line runs into the
indent rather than wrapping.

`StyleUtil.apply(Tabs, Tabs)` did the opposite - `destination.getTab().clear()`, on the
reasoning that "tabs are relative to each other" - which lost the style's stops wherever a
paragraph added one of its own, and left every `clear` in the result as an ordinary stop.
Measured against Word 365: a paragraph declaring `clear 6804` and `left 6379` on a style
which declares `clear 284` and `left 6804, 7938, 9072` and `right 10348` has Word putting
the text after its third tab at x=**466.65** (stop 9072), where falling through to
`w:defaultTabStop` 709 put ours at 403.0, on 30 blocks of that document; another, whose
style declares left stops at 284 and 8930 and whose paragraph adds 5103 and 7655, has Word
painting `"1.<tab>Artikelbezogene Prüfpunkte ..."` as one line with the text at x=28.3..485.0
where ours jumped to 283.5..499.2, and is 28.4..484.8 now. 25 documents of the three corpora
add tabs to a style which already declares some.

<a id="s44break"></a>**A tab which can reach no stop breaks the line.** Beyond the end of
the **reference area** - the text column, or the table cell - there is no stop for a tab to
reach, and Word ends the line there: the content after the tab starts the next line, with
the tab measured again from that line's start. Measured on `tab-clamp-right`, whose left
stop at 9355 twips is 539.75pt from the page's left edge, 16.4pt past an A4 page's 523.35pt
text column: Word writes "left stop 9355:" on one line and "SHORT" on the next at x=72, and
between them is a line of its own carrying nothing but the tab (which reaches nothing from
the line's start either) - the paragraph's two visible lines are 22.6pt apart where its
pitch is 9.1, and the trace shows the tab's glyph on a baseline 12.1pt below the first, in
the tab run's own font (the run has no `w:rPr`, so Word draws it in the document default,
11pt against the paragraph's 8pt). docx4j ran the text into the margin instead, to
539.8..567.7. The same golden's **centre and right** lines do not break: such a stop is
clamped at the right indent instead ([below](#s44clamp)).

`tab-leader-trailing` measures where the tab lands after the break. Its third entry is
"Two trailing tabs and a number`<tab><tab>`12" against stops `360:left;540:left;851:left;
9000:right:dot`: the first tab reaches the dot stop and its dots end at 521.3, the second
can reach nothing (the next default stop past 522.0 is 540pt, and the column ends at 523.35)
and so breaks the line, and the "12" after it is drawn at **x=90.0** - not at the left
indent, but on the 360-twip stop (72 + 18) which the re-measured tab reaches from the start
of the new line. docx4j had broken a word earlier, before "number".

Three kinds of tab do **not** break the line, each measured on a corpus document the rule
first cost a page:

- a tab whose stop draws a **leader** fills to the end of the line instead. A prospectus
  whose table-of-contents entries sit in cells 443pt wide against a `12000:left:dot` stop
  150pt past the cell: Word draws every entry's dots to the cell's edge (x=651.0) on the
  entry's own line, and breaking there put the dots on a line of their own and cost a page
  of the 23.
- a tab with **nothing after it** has nothing to move to the next line. A header whose
  paragraph is a picture and seven tabs, the last two of which reach nothing (the grid past
  the last custom stop is 14.5pt past the header's width): Word's header is shorter than one
  line of that paragraph, so it gives them no line; breaking made the header 38pt taller and
  pushed the document onto a second page. Word *does* give a trailing tab a line in
  `tab-leader-trailing`, whose second trailing tab starts 1.35pt short of the line's end
  rather than on it - not enough of a difference to rest a rule on, so the probe keeps a
  13.2pt gap there.
- where what precedes the tab **does not itself fit**, the ordinary break wins: the line
  breaks at the last opportunity that did and the tab is measured again on the line it lands
  on. Measured on a form whose cell holds `1.1<tab>Technische Freigabe erteilt<tab>o`, which
  Word breaks before "erteilt". (The allowance an earlier tab's stop past the available
  width bought the line is not room for more text, so it does not count towards the fit.)

In the line manager: a tab whose stop lies past `lineWidth + endIndent` commits the line
before the tab's own Knuth elements, re-measures it, and - where it reaches nothing from
there either - commits again after it, so the tab has a line to itself. A line holding only
a tab takes its height from the block's font rather than from the leader's rule thickness
(which is a hairline; a form whose section rules are leader-only paragraphs was 12.7pt above
Word's lines at the top of its first page and is now 1.6pt below them).

<a id="s44clamp"></a>A **centre, right or decimal** stop beyond the right indent is **clamped**
instead, so that the text it aligns ends on the indent. Measured on a centred footer whose
stops are `4252:clear;8504:clear;9355:center` on a 595.35pt page with 56.7pt margins - a
481.95pt content width, and a centre stop at 467.75pt from the margin, so the centred text
would end 11.3pt past the content edge: Word draws **one** line filling the width, 56.7 to
538.6, where the unclamped tab overflowed and wrapped a fragment onto a second line (18 Word
pages against our 20, the footer region 18.4pt tall instead of 9.2). In the line manager:
`width = min(stop - x - alignedOffset, lineWidth - x - followingWidth)`, and a clamped tab does
not raise the overhang that lets a line run past the indent. A line holding a tab is **sized**
from the left indent whatever the paragraph's `w:jc` - the stop a tab reaches is the same one
it would reach on a left-aligned line - and the line, its tabs' widths counted in, is then
**aligned as a whole** by the `w:jc`. Measured: six consecutive tabs advance 216pt in Word,
where the former three-no-break-space stand-in advanced 54pt.

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

docx4j drew every such line flush left before 17.1.0 (`tab-jc` max dx 372.4pt -> 0.36pt).
Three documents of a 40-document corpus slice have a centred or right-aligned paragraph
containing a tab.

**`w:compat/w:forgetLastTabAlignment`** ("Ignore Width of Last Tab Stop When Aligning
Paragraph If It Is Not Left Aligned") is exactly what would turn this rule off, and reading
it was measured and rejected in 17.1.0: the one corpus document which states it fell when
§4.4jc was switched off for it ([settings §4(e)](word-layout-settings.md)). The rule is
unconditional, and the `compat-breaks` probe pair carries the shape for a golden.

<a id="s44jcboth"></a>**...and the justified exception is narrower than that.** Word draws
the distinction at what follows the **last** tab. Where a tab is the last thing on the line,
or the words after it do not reach the end, the tab absorbs the slack and the line is laid
out from the start - which is the `tab-jc` probe, whose trailing tab Word leaves at x=72.
Where text *follows* the tab, Word justifies that text to the right indent as it would on
any other line: measured on a numbered clause reading `1.1.<tab>Настоящий договор ...` in a
`w:jc="both"` paragraph, the words and the tab position are ours exactly (both put the text
at the same x) and Word stretches the spaces to **x1=560.1** where the start-aligned line
stopped at 530.8 - **29.3pt of stretch lost**, on every such line of the document. The
stretch is FOP's, applied to every stretchable area of the line, so the line manager keeps
`JUSTIFY` only where nothing stretchable sits *before* the last tab - which is the ordinary
shape, a label or a number and then the tab; otherwise the tab's settled width would no
longer put the text on its stop. **784 blocks in 83 documents of the three corpora carry
both a tab and `w:jc="both"`.**

<a id="s44leader"></a>**Leaders.** `w:leader` `dot` and `middleDot` draw dots (FOP repeats
the font's own dot, as Word does; a dotted rule does not match); `hyphen`, `underscore` and
`heavy` draw a rule; anything else nothing. The leader a tab draws is the leader of **the
stop it reaches**, which is settled at layout time like the width. Until 17.1.0 the n-th
tab of a paragraph took the leader of the n-th stop, which is right only where the tabs and
the stops correspond one to one: a table of contents whose stops are
`360:left;540:left;851:left;9990:right:dot` gives its one-tab entries ("Foreword" then the
page number) the *first* stop's leader, so Word's dots ran the width of the page and ours
painted nothing - 150 such lines in one corpus document, and the class covers at least
seven of 157. The trailing-tab shape (`…<w:tab/><w:t/><w:tab/>`) is the same defect from
the other end: the tab that reaches the dot stop is the first of the two.

Since the FO cannot know which stop a tab will reach, it asks FOP for the **paragraph's**
leader - the first of its stops that draws one - and the line manager settles each tab
against the stop it resolved: it keeps that area, blanks it where the stop has no leader,
or, in the rare paragraph mixing dot and rule stops, builds the other kind
(`LBP.setLeaderPattern`). `docx4j:tabs` on the block, which carries every stop's leader, is
the single source of truth.

<a id="s44phase"></a>**Where the dots start.** Word's leader dots sit on a **grid fixed to
the reference area**, not on the end of the text: a leader holds exactly the whole grid
cells which fall inside its tab, so it opens with a blank of between nothing and one dot and
ends the same distance short of the stop. Measured on `tab-leader-trailing` and
`tab-leader-resolved`, whose dots step 3.121pt: all seven leader runs across the two
documents begin and end on one grid anchored at the 72.02pt left margin, to within the
0.05pt the PDF's own rounding allows, and every run's dot count is exactly the cells inside
it. So Word writes "1. Scope ....." where docx4j wrote "1. Scope.....".

`leader-alignment="reference-area"` is what XSL FO offers for this and docx4j writes it, but
FOP 2.11 reads the property in its RTF renderer alone (§10) and begins each leader's dots at
the leader's own left edge. The phase is therefore applied in the line manager: a dot
leader's area becomes a blank of `(-x) mod period` followed by FOP's own repeating area
(`LBP.PhasedLeaderArea`; `period` is the `FilledArea`'s unit width, and `x` the tab's start
from the left margin). The dots then land within 0.1pt of Word's on the corpus TOC lines
measured. A **rule** leader is continuous and has no phase.

**A leading tab** - one before any visible content on the line - is instead an `fo:leader`
of fixed length to the next stop, computed at FO-generation time, so code blocks and
hanging first lines indent as Word indents them. A fixed-length leader does not set
`text-align-last="justify"`; only a stretching one needs it.

**...but only where the stop it reaches is a left one** (17.1.0). A fixed leader of the
stop's own offset lays a right stop out as a left one: the text after the tab *begins* on
the stop where Word makes it *end* there. Measured on a corpus footer whose only content is
`<w:tab w:val="right" w:pos="9356"/>` then "Page 1 von 2": Word ends that text at x=540.3,
and the 467.8pt leader began it at 539.8, 46pt past the margin. It went unnoticed while such
a line merely overflowed; the [rule above](#s44break) that a tab which can reach no stop
breaks the line turned the overflow into a wrap and cost a footer line on every page of
three documents. A leading tab reaching a centre, right or decimal stop therefore takes the
ordinary zero-length `docx4j:tab` leader and the line manager settles it, exactly as it does
for such a stop anywhere else in the line. The hanging indent's implicit stop and the
default grid are left stops, so a second leading tab landing on the grid keeps its fixed
leader. 325 blocks in 60 documents of the three corpora carry a right, centre or decimal
stop in a block which is not itself right-aligned.

**Table-of-contents entries** (first stop right-aligned with a dot leader) keep the
stretching leader and `text-align-last="justify"` they have always had: their stop is the
right margin, and a stretching leader absorbs the width an unresolved
`fo:page-number-citation` loses when it resolves.

<a id="s44tocstop"></a>**...but the leader ends on the entry's own stop, not on the
paragraph's right indent** (17.1.0). `text-align-last="justify"` stretches to the block's
end-indent; Word stretches to the stop the tab reaches, and where that stop lies outside
the text column Word lets the entry overhang the margin. Measured on an A4 document with
72pt margins - a right edge at x=523.35 - whose `TOC1` style declares
`<w:tab w:val="right" w:leader="dot" w:pos="9350"/>` (x=539.5): all **308** of Word's entry
lines end at 539.6 where ours ended at 523.3, and being 16.2pt short of Word's measure six
entries took two lines where Word takes one. The block's `end-indent` is set to
`text column - stop`, clamped at the page edge, and only ever *outwards*: the stop tested is
the paragraph's **first**, and where it lies well inside the text column a later stop - or
the right indent - is what the entry's tab reaches. (Pulling the end-indent in fired on 72
blocks of one corpus document and cost it 0.025 of line parity.) **9 documents of the three
corpora declare a TOC stop more than 2pt from their text column**; in the rest the stop is
the right edge and nothing changes.

<a id="s44toc"></a>Its one cost is the phase above: a stretching leader's width is settled
by FOP's justification, after the line manager has been and gone, so its dots begin on the
text where Word's begin on the grid. 18 documents of the three corpora and about 797
lines carry the shape. Since 17.1.0 moves the page number's width to the tab itself
([below](#s44pageref)), such an entry can go through the ordinary resolved-tab path
instead - and get the phase with it -
and `docx4j.convert.out.fo.wordLayout.tocStretchingLeader=false` does that.
**Measured, the two are a wash**, which is why the stretching leader is still the default.
On the corpus document with the most of them, Word's first dot on five consecutive
entries is at 132.05 / 126.77 / 248.26 / 190.15 / 142.61; the stretching leader puts it at
132.45 / 126.07 / 249.59 / 190.92 / 143.24 (mean error 0.77pt) and the resolved tab at
132.00 / 127.00 / 247.00 / 189.50 / 144.50 (0.82pt). Line parity did not move on any of
the four documents holding the most of them (0.8494, 0.7651 and 0.9212 unchanged, 0.9240
to 0.9220). What those lines had been losing was not the geometry but the harness pairing
them on their exact dot count, which its leader-run normalisation now settles.

<a id="s44pageref"></a>**A page number is put on its stop, whatever FOP measured it as.**
Every other right, centre or decimal stop whose text holds a page reference used to land
short, because FOP measures an unresolved `fo:page-number-citation` as the placeholder
`MMM` (`AbstractPageNumberCitationLayoutManager`, fop-core 2.6 to 2.11) and the line
manager subtracted that width from the tab. Measured against Word's PDF of a 311-page
document (stops `1320:left;9350:right:dot`, DejaVu Sans 8pt): Word puts the number's right
edge on the stop, 539.74pt, on every line, where ours ended 529.0 - 10.7pt short, which is
`MMM` 20.71pt against "61" 10.18pt; another document was 25pt short on 53 lines, and 37 of
the 236 corpus documents with placed tabs have the shape. Word's rule is that the width a
page number gives up when it resolves belongs to **the tab**, not to the line's right end,
so the line manager pairs such a tab with the number that follows it and widens the tab by
exactly what the number loses when FOP resolves it (`TabPageNumberWidth`, a `Resolvable`
registered beside FOP's own; the tab's notification also puts back what the number's took
off the line). FOP's own redistribution could not do it: it applies a variation factor to a
justified line's stretchable areas, and a tab of a settled width on a start-aligned line has
neither.

**A page reference whose bookmark is gone keeps its cached result.** Word paints the result
it cached for a field whose target has been deleted - which editing leaves behind
routinely: one corpus document emits 150 `PAGEREF` fields and holds not one of the
bookmarks they name. An `fo:page-number-citation` whose `ref-id` is never emitted is
painted as **nothing at all** by FOP, so all 150 numbers vanished (and the entries' dots
with them, since the line ended early). Both pathways now check the document for the
bookmark first (`AbstractWmlConversionContext.hasBookmark`) and keep the field's cached
runs where it is absent, which is also what HTML output wanted: the link had nowhere to go.

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
docx4j applied it that way until 17.1.0. Measured against Word 365's goldens for the
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
on the block until 17.1.0: it also keeps the space that falls at a **line-break
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

FOP's *word space* is wrong the other way: `SpaceVal.makeWordSpacing` adds the letter space
to it **twice** (its own TODO says "Adding 2 letter spaces here is not 100% correct"), so
every space in a letter-spaced run is measured one letter space too wide. Measured on a
corpus document at Times New Roman 11pt whose space runs carry `w:spacing w:val="19"`
(0.95pt): the natural space is 2.75pt, Word's advance 3.87 and ours was 4.65 = 2.75 + 2 x
0.95; over 620 such runs a 447.4pt line came out 458.5pt. FOP's *painting* already follows
Word here (`addMappingAreas` sets the word-space adjust to
`wordSpaceIPD - spaceCharIPD - 2 x letterSpace`), so the correction goes on the glyph
mapping - which is what both the Knuth element and the text area's width come from - and
leaves FOP's `wordSpaceIPD` alone.

With that corrected, Word's own rule holds on both sides: **one character space after every
character, the word's last and the spaces included**. Measured on the `spacing-char` golden,
Times New Roman 12pt, `w:spacing w:val="20"` (1pt): "expanded" advances 6.228 6.948 7.068
6.228 6.948 7.068 6.228 7.068 - the final "d" is 6.0 + 1.068 - and the space after it is
3.0 + 0.948. So the line manager brings both of FOP's paths (the complex one, which counts
none, and the plain one, which counts `wordLength - 1`) to `wordLength`. Until 17.1.0 it
used the plain path's count, which was right only while the doubled word space made up the
difference. Line parity on the probe 29% -> 79%, `kern-title` 93% -> 100%; a corpus
document of 620 letter-spaced space runs went 0.39 -> 0.98. Upstream FOP report candidate
(two: `SpaceVal.makeWordSpacing` and `GlyphMapping.processWordMapping`).

**What is left on `spacing-char` (64%) is not the letter-space count.** Glyph by glyph
against the golden, the count is confirmed on both a word's last character and a space -
at 3pt spacing the golden's space advance is 5.988 (3.0 + one space) and ours is 6.000, and
a 6.0pt glyph advances 8.988 against our 9.000. The residual is a **sub-font-unit
difference in the advances themselves**, about 0.0075pt per character, and it is *not*
proportional to the spacing: at 0.25pt spacing the line is 0.6pt wider than Word's and at
3pt it is 0.5pt wider, while the unspaced control lines match to 0.2pt. Word's glyph
positions are exact multiples of one font unit (0.012pt at 12pt), so Word rounds each
advance while FOP accumulates exact fractions, and Word's *measure* rounds the other way
from its painting: on the 1pt paragraph Word ends line 2 at 469.8 with "of", refusing
"dividing", which by the golden's own advances would end at 522.68 against a text column
of 451.32 + 72 = 523.32 - Word is 0.64pt short of taking a word we take, and one word
lost cascades through the rest of the paragraph (the probe's whole 36%). Making this match
means reproducing Word's per-character rounding in the measure, which touches every line of
every document rather than letter-spaced text; it is not attempted here.

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
pathways get it.

**A run carrying both `w:spacing` and `w:w` gets both.** They share the one XSL-FO
property, and `letter-spacing` is *inherited*: the run's own `fo:inline` carries the
character spacing and the per-font selection inline inside it carries the scaling, so the
inner value simply replaced the outer one. Measured on a document whose every run carries
`w:w="94"` and a `w:spacing`, and whose FO read `<inline letter-spacing="-0.2pt"><inline
docx4j:font="Arial" letter-spacing="-0.244pt">`: "All payments should be made by cash or
Cheque." is 57.1..294.7 = **237.6pt** in Word and was 57.0..302.7 = 245.7 - **+8.1pt**,
which is 0.2pt over the line's 38 characters; it is 57.0..296.0 now. `RunFontSelector`
marks a span whose letter-spacing is scaling alone and `WordLayoutFixups.combineLetterSpacing`
adds the nearest ancestor's to it; a value the exporter merely repeats on a nested inline
carries no mark and is left alone. 4325 nested pairs in 22 documents of the three corpora.

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
2. **The document's own `w:altName`**, from `word/fontTable.xml` (ECMA-376 17.8.3.1, "the
   name of an alternate font which shall be used if the font specified is not available").
   That is the author's answer to a missing font, and it is what Word uses; the alternate
   is resolved as any document font is - itself if the machine has it, else its own metric
   clone. Measured on a document whose Normal style is
   `<w:rFonts w:ascii="HelveticaNeue LT 55 Roman"/>` with
   `<w:altName w:val="Times New Roman"/>`: Word's PDF embeds **TimesNewRomanPSMT** where
   docx4j's FO said `font-family="Arimo Regular+noliga"` **2194 times** - the theme's
   `minorHAnsi` - so our lines measured 1.0721 x Word's over 140 matched lines and the
   document's line parity was 0.242, the corpus's second worst; it is 0.759 now, with 26
   pages against Word's 29 where it had been 32. 142 documents of the three corpora name a
   font carrying a `w:altName`. It also supplies the line metrics (§2.7), since Word is
   using the alternate font outright. `docx4j.fonts.altName.enabled=false` skips this step.
3. **Class-based**: whatever is left unmapped takes a font of its own class (sans, serif,
   monospace) from the classes and candidate lists in `FontSubstitutions.xml`.
3a. **Which theme font a run resolves to** is decided by
   **`w:settings/w:themeFontLang`**: a `w:rFonts` naming `minorHAnsi`, `minorEastAsia` or
   `minorBidi` is resolved against the theme through the language that setting names
   (`org.docx4j.model.PropertyResolver`), before any of the substitution above applies.
4. **Glyph-aware, per script**: the run font selector then picks, per script segment of the
   text, a font that can actually render it - preferring the document font's class, caching
   the choice per (font, script), and warning once per font and script rather than once per
   glyph (`org.docx4j.fonts.FontFallback`).

<a id="s51cover"></a>Step 4 applies to a step-1 substitute too. A metric clone is chosen
for its advance widths, and several of them carry the Latin alphabet alone: Caladea, which
stands in for Cambria, has neither Greek nor Cyrillic (`fc-query`:
`20-7e a0-161 164-17f 192 1fa-1ff 218-21b 237 2c6-2c7 ...`). Until 17.1.0 the coverage check
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
| Arial Black | Noto Sans Black (Arimo as last resort) | Arial Black is far heavier and wider than Arial: a centred Arial Black title Word draws 281.2pt wide came out 247.9pt on the same centre in Arimo - Word/ours 1.134 - where Noto Sans Black measures 1.1122 x Arimo over a mixed Latin sample. |
| Tw Cen MT | Arimo | A geometric sans with no entry at all, so it fell through to the *serif* default: on a document where every y and x matched Word to 0.3pt its labels were 3-4% narrow. Arimo is 1.0605 x Tinos, so the residual is about 2%. |
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

<a id="s52measurednochange"></a>**Three substitutions were re-measured in 17.1.0 and left
as they are.** Each had been reported from a single corpus line, and the aggregate does not
bear it out. **Tahoma** stays on Arimo: over five Tahoma documents of two corpora, pairing
lines whose extracted width our own render reproduces from Arimo's advance widths (so the
line is purely Arimo) and comparing Word's, the ratio Word/Arimo is 1.0016 / 1.0079 /
0.9830 / 0.9935 / 1.0005 - a median of 1.000, where DejaVu Sans Condensed is +2.2 to +2.9%
off and Carlito -7 to -9%. **Trebuchet MS** likewise: 1.0147 over 21 such lines, against
DejaVu Sans Condensed's +1.0% error - not enough to move. **Times New Roman CYR** measures
1.0007 against Tinos over 41 lines of the document it was reported on, i.e. the current
substitution is exact; the wide line there is the paragraph mark's `w:sz` reaching the run (&sect;2.5).

**Cambria's Greek** did need one, and it is a consequence of the "only for the characters
it can draw" rule above: Caladea, Cambria's metric twin in Latin, has no Greek at all, so
Greek fell through to the document default serif. Over 27 lines of a Greek document whose
geometry is otherwise ours to 0.4pt, Word's Cambria-Bold is **1.0834 x Tinos-Bold**; of the
installed Greek-covering serifs **P052** (URW's Palladio) measures 1.0281 - 5.1% short,
against Tinos's 7.7% - and Noto Serif 1.1574 (+6.8%), C059 1.1706, DejaVu Serif 1.3643.
`FontFallback` therefore prefers P052 for Cambria's Greek, beside the Sylfaen/Georgian
entry; where it is not installed the order falls through to what it was. That document went
from 0.250 to 0.483 of line parity and from 64 pages to Word's 68.

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

<a id="s5nokia"></a>**Held back: a substitute for the one unresolved corporate face.**
`addFirstAvailableSubstitute("Nokia Pure Text", "Source Sans 3", "Source Sans Pro",
"Arimo Regular", "Liberation Sans")` - Segoe UI Light's substitute, and the family is a
humanist sans by its own `w:family="swiss"` and panose serif-style 11 - was measured on the
one corpus document that uses it. It draws the document in the right class instead of base-14
Times and moves its page count towards Word's (63 of Word's 87 to 65), and its body lines are
closer (Word's title line is 266.9pt, base-14 Times 274.4, Source Sans 269.9), but its
headings are further out (115.2 against Times' 120.6 and Source Sans' 104.6) and it cost that
document 0.045 of line parity - the whole of the batch's fall on that corpus. Without a
measurement of Nokia Pure's own advances there is nothing to choose the substitute by, so it
waits for one; the entry and its measurements are recorded in `Mapper`.

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
counterparts keep U+2B99, U+2B9B and U+2B98. Wingdings 0xFC is **U+2713** CHECK MARK for
the same reason, not U+2714 HEAVY CHECK MARK: measured on a document whose bullet is that
character, the drawn widths agree to 0.1pt either way (Word 90.0..213.9, docx4j
90.0..213.8) and only the PDF's text layer differed, so every one of those lines failed to
match.

<a id="s57lvlsym"></a>**A numbering label's own symbol.** `RunFontSelector.fontSelector`
maps a character of a symbol font to its Unicode equivalent and draws it in the substitute
font that has the glyph - the same thing it does for a `w:sym` run - which is why most
bullets stated as a symbol code point are already right (a Wingdings 0xF0A7 comes out
U+25AA in Noto Sans Symbols 2). It is reached through the run's `w:rFonts/@w:hAnsi`,
though, and a numbering label's font comes from the level's `w:rPr`, which does not always
survive to it. Where it does not, the private-use code point reaches FOP unmapped, no
installed face can draw it, the label falls back to the paragraph's own text font and FOP
paints its `NOT_FOUND` glyph, `#`: measured, Word's PDF has U+F0A8 in `SymbolMT` at
x=144.05..297.82 where ours had `#` in Arimo at 144.00..297.54 - the geometry agrees and the
glyph does not. **923 lines of 116 of the three corpora's 449 documents** carried such a `#`,
and Word's own PDFs have none of them.

`XsltFOFunctions.symbolLabelFallback` maps a label which is **still** in the private-use area
after run font selection - which is exactly the case the symbol path did not take - and gives
it the substitute font, checked for the glyph first (`PhysicalFonts.getSymbolFont` and the two
Wingdings ones, which is what `w:sym` uses). Applying `SymbolMapper` to every such
`w:lvlText` *ahead* of run font selection - 17.1.0's first reading of the fix - was measured
over the three corpora and is much worse: it bypasses the mapping that works, turning correct
U+25AA labels into the missing-symbol box, and cost **0.025 of mean line parity on each
corpus over 111 documents with none improved**. The fallback as shipped fixed 109 of real2's
383 excess `#` lines and moved no document's score by 0.005 either way (real1 +21 matched
lines with one document +0.076, real2 +14, real3 exactly unchanged).

<a id="s57symbolrange"></a>**The symbol range needs a fallback chain, not one Windows face.**
`RunFontSelector`'s U+2190-U+2BFF branch asked the run's own font for the glyph and, where
it lacked it, asked exactly one substitute - `Segoe UI Symbol`, which is what Word 2016
uses and which no Linux or macOS box has. Failing that it called no `fontAction` at all, so
the character took whatever font the span already carried, **and the span carried no
`font-family` for the glyph-coverage pass to work on** - `glyphFallback` returns at once
without one. FOP then painted its `NOT_FOUND` glyph, `#`. The second half of the same gap
is that every code point of those blocks - Arrows, Mathematical Operators, Box Drawing,
Geometric Shapes, Miscellaneous Symbols, Dingbats, Braille - is
`Character.UnicodeScript.COMMON`, which `FontFallback.needsCoverage` treats as always
covered, so the coverage pass would have skipped them even with a family to work from.

Both halves are fixed since 17.1.0: the branch names the run's own font whatever happens,
`needsCoverage` is true for a symbol code point, and the coverage pass groups the symbol
blocks under a class of their own (`FontFallback.isSymbol` / `coverageGroupOf`) rather than
under COMMON. The class matters twice: it gets the range its own measured candidate order -
`Segoe UI Symbol`, then Noto Sans Symbols 2, Noto Sans Symbols, Symbola, DejaVu Sans,
FreeSerif, which is the order `PhysicalFonts.getWDingsFont` already picks a symbol-font
bullet by, so an arrow in a text font and the same arrow in a Wingdings bullet are drawn by
the same face - and it stops the space after a symbol being dragged into the symbol font
with it (a *shared* COMMON character follows whatever precedes it, which is right for a
space inside a Georgian phrase and wrong for the space after an arrow). Where nothing
installed covers the character the span is left as it was, with one warning, exactly as for
any other script.

Measured over the 449 renders of the three corpora, the excess `#` in our PDFs against
Word's - Word's own PDFs have none - fell from **93 / 180 / 157, 430 in 39 documents**, to
**93 / 139 / 81, 313 in 26 documents**. What is left is a different cause on each corpus
and not this range: fullwidth CJK punctuation (U+FF0C, U+FF1A, U+3002), the soft hyphen
U+00AD, and private-use code points a symbol font's own `w:sym` did not reach - the first
corpus's 93 are all of those and did not move at all.

`w:caps` and `w:smallCaps` have no XSL-FO equivalent (`text-transform` and `font-variant`
are CSS), so the text itself is upper-cased, in the run's `w:lang`, since Turkish and
Lithuanian case differently; for small caps the originally lower-case stretches go in an
inline at 80% of the size. HTML gets `text-transform` / `font-variant`.

<a id="s57smallcapsline"></a>**Word scales a small-caps run's glyphs, not its line.** FOP
takes a line's ascent from the areas on it, so where that 80% inline is a block's **only**
content the line came out 80% high and the block's own `docx4j:baseline` could not win
against a measurable smaller area. Measured on a letterhead whose CONTACT block is nothing
but a small-caps run, Word puts it and the two cells beside it on one baseline (123.2) where
docx4j split the row into 120.3 / 122.0 / 122.0. The span now carries `docx4j:small-caps`
naming what it was scaled by (`RunFontSelector.HINT_SMALL_CAPS`, promoted by
`WordLayoutFixups.lineBoxAttributes`) and `WordLineLayoutManager` reads its height at the
size the run declares. Scaling the height rather than skipping the area keeps a line correct
where a small-caps run shares it with others (`SmallCapsLineHeightTest`).

---

## 6. Tables

### 6.1 Where the table sits

Normally the table's grid edge sits at the text margin + `w:tblInd`, and the first column's
text one left cell margin further right. **Below compatibility mode 15** - Word 2010's
layout engine and the ones before it - it is the first column's *text* that lands on the
text margin + `w:tblInd`, so the grid edge is one left cell margin further back.

Measured with `table-indent-compat14` / `-compat15`: the first cell's text at 72.0 / 72.0 /
77.3pt for no `w:tblInd`, `w:tblInd` 0 and `w:tblInd` 108 in mode 14, and at 77.8 / 77.8 /
83.1pt in mode 15. **Modes 11 and 12 take the shift too**: measured with
`table-grid-edge-compat12` and `-compat11` (`w:tblInd` 108, at the top level with Word's
default cell margins and with the table's own `w:tblCellMar` 108, and nested in a cell),
Word's first cell text is at 77.3pt in both modes, exactly where mode 14 puts it and 5.8pt
left of where mode 15 would. 17.1.0 briefly restricted the shift to mode 14 alone, on the
strength of the corpus document in the next paragraph, and those two goldens settled it.

**The shift is unconditional in every mode below 15**, whatever the sign of `w:tblInd`
and whether or not the table has one. Measured on `table-grid-edge-signed-compat12`, a
72pt text margin and Word's default cell margins: Word's first cell text is at 66.5pt for
`w:tblInd` -108, at 54.0 for -360, and at 72.0 for no `w:tblInd` at all - fixed layout and
autofit alike - which is margin + `w:tblInd` exactly, so the grid edge is
margin + `w:tblInd` - one cell margin throughout. Modes 14 and 15 of the same probe match
what was already implemented. 17.1.0 briefly capped the shift at `min(shift, max(0,
tblInd))` below mode 14, which put those three at 72.3 / 59.7 / 77.7.

<a id="s61autofit"></a>**A content-autofit table's grid is one cell margin wider than the
text column at each end**, because below mode 15 it is the cell *content* that spans the
column. That is the corpus document the cap was taken from - mode 12, `w:tblW` auto,
every `w:tcW` auto, `w:tcMar` 41 twips, first row a single `w:gridSpan="3"` cell holding a
centred paragraph. On a 70.85..524.45pt text column Word draws the table's borders
68.66..526.78, so the cell content runs exactly 70.85..524.45 and the centred paragraph
lands on 297.65pt, the column's own centre; sizing the grid to the column instead centred
it on 292.25 and cost the document two of Word's fifteen pages. The autofit pass adds the
two cell margins to the width it distributes (`AbstractTableWriter.autofitGridAllowanceTwips`,
zero for HTML), and the centre then comes out right whatever the cell margin is.

**The margin the shift is measured with is the first cell's own**: `w:tcMar/w:left` where
it has one, else `w:tblCellMar/w:left`, else Word's default 108. In the document above
the cells carry `w:tcMar w:left="41"` (2.05pt) while the table declares no `w:tblCellMar`,
and Word's grid sits 2.19pt outside the column at each end, not 5.4.

<a id="s61nested"></a>**A nested table takes no shift either.** Word puts the grid edge of
a table inside a `w:tc` on the containing cell's **content** edge, and adds the nested
table's own cell margin on top of that. Measured on a mode-14 first-page header (page
margin 28.35pt, outer `w:tblInd` 108, cell margin 108 both levels): Word's clip for the
nested table runs from 33.9 = 28.35 + 5.4, and its first cell's text is at 39.1, where
docx4j drew it at 34.0 - one cell margin left, on every cell of every nested table. The
outer table of the same document matched Word exactly, which is what proves the rule is
about nesting and not about the mode; `table-grid-edge-compat12` says the same at mode 12,
where Word's nested cell text is at 83.1 = the containing cell's content edge (77.4) plus
one cell margin. Whether a table is nested is not known where the indent is computed - in
the XSLT pathway the `w:tbl` reaching the table writer was unmarshalled on its own, so it
has no parent - so the shift is stamped on the `fo:table` and
`WordLayoutFixups.nestedTableGridEdge` gives it back to the tables that turn out to be
inside an `fo:table-cell`.

A `w:jc="center"` table wider than the text column is **centred by Word, overhanging both
margins**; its start-indent is the negative half of the overflow.

**A centred table narrower than the column is centred too.** `w:tblPr/w:jc="center"`
centres a table whatever its width; the rule used to fire only where the table was
*wider* than the text column, so a narrower one fell through to `start-indent` 0 and sat
at the left margin. Measured on a corpus document whose centred 5690-twip table sits on a
9638-twip column: Word draws its label "1" at x=174.3 and ours was at 75.7, 99pt out; it
is at 179.8 now.

### 6.2 Cell margins

Word applies default cell margins of 0.08in (108 twips) left and right when neither the
table nor its style sets any - a document need not define a "Normal Table" style, and
docx4j's own default styles part does not. Measured: cell text starts at the border centre
+ half the border width + 5.4pt. Applies to HTML output too.

<a id="s62tcmar"></a>A cell's **own** `w:tcMar` overrides the table's `w:tblCellMar`, and
the content-autofit sizer ([§6.3](#63-autofit-column-widths)) reads it too (17.1.0): where
a table sets `w:tblCellMar` 0 left and right and every cell overrides with `w:tcMar` 30
twips, the columns were sized to the bare text width and the cell writer then emitted 1.5pt
of padding each side - so the very line that sized the column no longer fitted. Measured
against Word: a cell whose one line is 68.9pt wide got a 68.95pt column and a 65.95pt
measure and broke in two, where Word keeps it on one line (70.8..149.5), and the accumulated
extra lines put that document's `medianDy` at -41.0. Where the cell states one side only,
the other comes from the table's pair. **10 documents of the three corpora.**

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

<a id="s63grid"></a>**`w:tcW` is a preferred width, and the grid outranks it.** Word honours the
`w:tblGrid` when every cell has a preferred width, and a row's `w:tcW` need neither
describe every column nor agree with the grid - Word keeps the grid it cached and treats
the cell widths as a hint. docx4j's autofit pass read the condition per *cell* rather than
per *column*, and gave a column whose cells state a `w:tcW` exactly that width, so a table
whose row 1 was stale or partial was laid out on row 1. Measured on a landscape report
whose table is `w:tblW 14580 dxa` with a grid summing to the same 729pt while row 1's
`w:tcW` sum to 404.4pt: Word's page-3 cell clips run `43.9..81.9 | 82.6..128.2 |
128.9..182.0 | 182.4..235.5 | 236.2..327.7 | 328.4..772.3` = **728.4pt, the grid**, on a
769.9pt column (so no over-wide clamp is involved), where docx4j wrote
`width="404.4pt"` and wrapped every cell - "11:30 PM" became "11:30" + "PM", and Word's
249 lines came out as 320. The grid now wins wherever it describes every column of the
widest row and either every column has a preferred width, or the table states an absolute
`w:tblW` which the grid sums to within 1%. A row-1 `w:tcW` sum differing from the grid by
more than a tenth occurs in **45 documents** of the three corpora; the one measured above
went from 0.534 to 0.795 of Word's lines, and two more of that corpus from 0.914 to 0.986
and from 0.758 to 0.864.

The second clause used to require that **at least one cell declare a width**, and that was
wrong (17.1.1): the cells of a table written for its grid are commonly all `w:tcW auto`,
and the precondition sent exactly those tables to the content pass. Measured on a corpus
document whose `w:tblW` is `8691 dxa` - 434.55pt, which is the grid exactly - with every
cell auto: the content pass gave 73.6 / 40.9 / 242.35 / 77.7pt against the grid's 79.2 /
47.65 / 222.4 / 85.3, and its `Booked By:` label does not fit a 40.9pt column where Word's
47.65pt holds it. That document goes **0.829 to 0.991**. The shape - an absolute `w:tblW`
the grid sums to, every cell auto - is 12 documents and 35 tables of the three corpora, and
over all three the change is +128 lines matched, the medians up on all three, one page count
recovered (a table that took four pages of Word's three now takes three) and one lost, and
no document down. A table which states no width of its own and has all-auto cells is still
untouched, so the content-based pass and §6.4's widening still apply to it.

<a id="s63pctcells"></a>**A `w:tcW` in `pct` is a preferred width, exactly as a `dxa` one
is** (17.1.1). Reading only `dxa` left a `pct` table with *no* column preferences at all: it
failed the "every column has a preferred width" test above, fell through to the content pass,
and was laid out on its content rather than on the grid Word cached. `w:tblW pct` with
`w:tcW pct` is **61 documents and 661 tables** of the three corpora. A percentage is of the
table's own width (fiftieths of a per cent), so it needs the table to state one. Measured over
all three corpora: mean line parity 0.8826 to 0.8844, 0.8511 to 0.8523 and 0.8816 to 0.8841,
**+1183 lines matched**, the medians up on all three, `parity >= 0.98` net +1, and **twelve
documents up against one down** - one long document goes 0.914 to 0.981 with its page count
landing exactly on Word's 161, and another 0.804 to 0.929. It is read per cell, as `dxa` is;
requiring the cells to describe every column removes the one document it costs - a table whose
rows are mostly `gridSpan` cells, which declares two of its nine columns and whose other seven
are better content-sized (0.989 to 0.940) - but gives back two of the five it wins on that
corpus, which is a wash, and would treat `pct` more strictly than `dxa` for no reason the
documents support.

**The `table-cell-pct` probe, and why a generated probe cannot answer this.** The probe
(17.1.1) was built to ask which wins when the grid and the cells disagree. Each table is
`w:tblW 5000 pct` = 451.3pt and states its proportions twice; column 1's width by each reading,
against Word:

| case | grid says | cells say | **Word** |
|---|---|---|---|
| P1 grid 66.5% against cells 25% | 300.1pt | 112.8pt | **113.0pt** |
| P2 the two agree (control) | 112.8pt | 112.8pt | **113.0pt** |
| P3 cells state 20% + 40%, summing to 60% | 225.7pt | 90.3pt | **150.7pt** = 33.4% |
| P4 grid 50% against cells 25.92% | 225.7pt | 117.0pt | **117.1pt** |
| P5 the same disagreement stated in `dxa` | 300.0pt | 112.8pt | **113.2pt** |

Read literally that says the cells beat the grid in either unit, and that cells whose
percentages do not add up are scaled to fill the table. Implemented, it takes the probe from
43% to 100% - and costs an unbiased quarter of the 191-document corpus **0.8967 to 0.8771**,
seven documents down against one up.

The reason is that **the probe's `w:tblGrid` carries no authority**. A `w:tblGrid` in a
Word-authored autofit table is Word's own cached layout, so it is what Word draws; a grid this
harness generated is whatever the generator wrote, and Word recomputes from the cells on open.
So the probe measures "what Word does with a grid it did not write", which is not the corpus's
question. The corpus counter-example is direct: a `w:tblW 5000 pct` table whose writable width
(9638tw) its grid sums to - the probe's shape exactly - asks for 13.78% of column 1 where the
grid says 19.41%, and **Word draws the grid's**, the opposite of P1. It is not a compatibility
setting: that document and the probe are both `compatibilityMode 15`. Content does not explain
it either - the column's content fits the smaller width.

So the change above deliberately leaves the grid winning where the two disagree, and any future
probe about a grid must either use `w:tblLayout fixed` (which Word does not recompute) or be
round-tripped through Word before it can be read as evidence.

**Resolved by round-tripping both, 2026-09-09.** `WordGoldenRunner`'s third directory has Word
re-save each document, and a `w:tblGrid` Word writes back *is* Word's layout in twips, so this
no longer has to be inferred from a page image.

On the probe - whose grid Word did not write - Word **recomputes from the cells**, in either
unit, normalising percentages which do not add up: P1's grid of 66.5/33.5 comes back
25.0/75.0 against cells of 25/75, P3's cells of 20% and 40% come back 33.3/66.7, P4 comes back
25.92/74.08 exactly as its cells state, and P5, stated in `dxa`, comes back 25.0/75.0. That is
the page geometry above, confirmed in the file.

On the 191-document corpus, 18 documents and 36 of 727 tables had their grid rewritten, and the
split is by unit: where the cells are **`pct`**, Word's new grid matches the **cells** - the
table this section was written from goes from a cached 15.38% for column 8 to Word's 25.90%,
against the cells' 25.92% - and where they are **`dxa`** it matches the **old grid**, 3 of 4.

The reconciliation is that a grid in a Word-authored document is Word's own cached layout from
a previous session, which Word keeps; a grid this harness generated is not, so Word recomputes.
Hence the rule as it stands - the grid is authoritative, and a `pct` `w:tcW` informs the content
pass rather than overruling it. It is still not universal: one corpus table's `pct` cells
disagree with its grid and Word kept the grid, and what decides that case is open.

**The resaved grid is ground truth for every table**, which is the measurement to build on:
comparing docx4j's computed columns against it over all 727 corpus tables and 145 probe tables
gives the autofit sizer a per-table error in twips instead of a proxy read off a page. The
`table-grid-pct` probe now carries Word's answer for grids deliberately 33% over and under the
column (6000+6000 comes back 4448+4569; 3000+3000 comes back 4671+4346), including the
`w:tblLayout fixed` twins, which Word refits too - and the refit is **not** uniform
(x0.741 against x0.762), which is J28's question answered in Word's own numbers.

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

<a id="s63collapsed"></a>**A collapsed border costs a cell's text measure nothing** - in a
grid-sized cell as much as in a content-sized one. `table-cell-measure` is the
measurement: every table holds one line in a column that line's own advance sized (the
advance rounded up to a whole twip, plus two twips, plus the two 108-twip cell margins),
and its three rows give the end cell margin back nothing, half the border width and the
whole border width, so the rung the wrapping stops at says what was charged. Word wraps
**no** row of the collapsed 0.5, 1.5 and 3pt tables - and its text simply starts half a
border further in and runs half a border past the right cell margin, its three tables
opening at x=77.8 / 78.3 / 79.0 and closing at 454.0 / 454.5 / 455.2, the same 376.2pt
line shifted. FOP's half-of-each-border charge wrapped the first row of the 0.5pt table
and the first two of the others. That settles H12 against the reading `table-fixed` and
`table-cellspacing` had suggested; their lines had no slack at all, so all they said was
that *something* was charged.

With **separate** borders (`w:tblCellSpacing`, §6.6) Word charges what FOP charges: the
same probe's three cell-spacing tables wrap all three rows in Word and here alike, which
is two whole border widths. Nothing is given back there.

<a id="s63guard"></a>**No allowance is held back.** Until 17.1.0 a tenth of a point was,
where the width came from the grid, because FOP's line measure ran that much narrow - its
glyph advances were truncated to 1/1000 em rather than rounded, which on a 30-character
line of 12pt Liberation Serif loses 0.13pt and on a 74-character one 0.27pt. They are
rounded now ([§10](#s10advances)), and with the whole allowance given back `table-fixed`,
`table-cellspacing` and `table-cell-measure` all keep exactly the lines Word keeps: with
the guard still in place the rounded measure broke three of `table-cell-measure`'s lines
Word does not break (100% -> 97%), and without it all three probes are at 100%.

A content-sized column also carries **two twips of slack**
(`AbstractTableWriter.COLUMN_SLACK_TWIPS`), because the cell margin the FO writer emits is
rounded to `1.91mm` = 5.4152pt where Word's is 5.4: without it the line the column exists
to hold broke in two in all three of that probe's content-autofit tables, 0.03pt short.
The FO table writer stamps `docx4j-content-sized` on the `fo:table` for the fixup to read.

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

<a id="s65pct"></a>**A `w:tblW` in `pct` is a width Word gives the table exactly**, and the
`w:tblGrid` is scaled to it - the percentage wins over the grid, where an absolute
`w:tblW` does not. Measured on `table-grid-pct`, whose text column is 9026 twips:

* `w:tblW 5000 pct`, `w:tblLayout="fixed"`, grid 4614+4614 = 9228 (2.2% over): Word's two
  columns are 224.98 and 225.22pt, the grid scaled by 9026/9228. The twin whose grid is
  3000+3000 = 6000 comes out at the same two widths, the grid scaled **up** by 1.504.
  docx4j used the grid as it stood in both, so the first table's cells were 6pt wide of
  Word's and the second's 75pt narrow, which broke three extra lines.
* `w:tblW 6000 pct` - 120% - is drawn 541.2pt wide, from the left margin to x=613.2 on a
  523.2pt column. docx4j clamped it to the column.
* `w:tblW 5000 pct` with `w:tblInd 720` is the full 9026 twips wide **starting at the
  indent**, overhanging the right margin by the indent: the percentage is of the text
  column, not of what the indent leaves of it.
* An autofit table with auto cells keeps Word's content-based distribution *within* that
  width (the 120% table's two columns are 254.0 and 286.2pt, the same 0.888 ratio as
  ours), so only the total is the percentage's.

`AbstractTableWriter.scaleGridToPercentageWidth` scales the grid, and
`fitToAvailableWidth` no longer clamps a **percentage**-width table's autofit columns to
the column. An absolute `w:tblW` buys no such exemption - one corpus table whose
`w:tblW` asks for 117pt more than the column is kept inside it by Word - so only `pct`
overhangs. Paginated output only; in HTML the percentage is the browser's. The probe went
from 86% to 98% of Word's lines; what is left is a prose line Word breaks and FOP keeps,
by 0.1pt of the same truncated advances §10 records.

That exemption is unconditional only for a table which states a width of its own - a
`w:tblW` in `dxa` or `pct`, or `w:tblLayout="fixed"`. For an **autofit** table (`w:tblW`
absent or `auto`, layout not fixed) the grid is only the layout Word cached the last time it
laid the table out, and Word recomputes it against the page it is on now: measured, autofit
grids 1.35 to 2.7 times the text column are drawn by Word *inside* it (one 956.45pt grid on
a 453.6pt column came out 505.3pt wide), while docx4j painted half that document past the
page edge and lost 7 of Word's 15 pages. Such a grid is scaled to the column. **`w:compat/w:growAutofit`** ("Allow Tables to
AutoFit Into Page Margins") is the flag that would switch that off, and reading it was
measured and rejected in 17.1.0: the two corpus documents which state it have Word 365
fitting their grids to the column anyway, and honouring the flag cost them 0.948 -> 0.248
and 0.930 -> 0.842 of Word's lines ([settings §4(e)](word-layout-settings.md)). The
`compat-tables` probe pair carries the shape for a golden. The cut is at
**1.25** (`AbstractTableWriter.GRID_OVERHANG_LIMIT`), above the 19% Word was measured
overhanging and below the narrowest refitted case: fitting autofit grids only 1-2% over
re-broke cells Word does not break, and cost line parity on two documents.

Two corpus measurements of **grid-sized** tables disagree, which the rule above does not
yet account for: a 29%-over grid with `w:tblInd` −1300 is honoured in full and overhangs
both margins (Word's cell clips run 7.0..589.85 on a 595.35pt page), while a 1.7%-over
grid with `w:tblInd` 0 is fitted into the text column (day columns 140.6 / 133.1 / 146.2 /
141.2 / 132.8 summing to 693.8 against a 697.95pt column). The working hypothesis is that
Word clamps an over-wide grid only where the author has not deliberately indented the
table out of the column. The `table-grid-overwide` probe - the same grid 1.7%, 10% and 29%
over the column, each at `w:tblInd` 0 and −1300, autofit and fixed layout, every `w:tcW`
in dxa - is that measurement; nothing has been changed here. Its Word golden has since
arrived, and docx4j matches it on every line of all nine pages, so the rule above covers
the shapes the probe holds and the disagreement is elsewhere.

<a id="s65borders"></a>**`w:tblBorders` is resolved per cell.** Its `top`, `bottom`, `left`
and `right` describe the table's **outer** edge and `insideH`/`insideV` the rules **between**
cells (ECMA-376 17.4.39); the cell's own `w:tcBorders` overrides both. docx4j applied the
outer definition to every cell whenever `insideH` or `insideV` existed, which looks right
only where the two agree. Measured on a table whose outer borders are `nil` and whose
`insideV` is `single sz="18" color="FFFFFF"`: `mutool draw -F trace` on Word's PDF shows a
white 2.25pt rule between the columns (`fill_path x=186.1..188.2 y=251.6..278.0`) and every
one of our cells came out `border-*-style="none"`. 39 documents of the three corpora, 403
tables; the document this was measured on went from 0.17 to 0.93 of Word's lines.

### 6.6 `w:tblCellSpacing`

Word puts a whole gap (2 x the spacing) between the table border and the outer cells, and
each column's cell is `gridCol - 3 x spacing` wide; FO's separate-border model puts half a
gap at the edges instead. Padding on the `fo:table` plus columns narrowed by the spacing
reproduce Word's geometry with the table width unchanged: measured, cell text 3.6pt further
in for 72 twips of spacing.

**The gap is charged against a grid width, not against a content-autofit one.** FOP's
separate border model takes one gap per column where Word takes a whole gap and half of each
outer one, so docx4j gives the extra half back by taking the spacing off each column - which
is right for a grid width, since Word's grid includes the gaps. A content-autofit width is
built from the measured content plus `w:tblCellMar` and never had a gap in it, so taking one
out charged the spacing a second time. Measured on a letterhead with `w:tblW auto`,
`<w:tblCellSpacing w:w="15"/>`, `w:tblCellMar` 15 twips and one 2799-twip grid column: the
autofit width 2736tw (136.8pt) became a 136.05pt column and, less the cell's own 0.75pt of
padding either side, a 134.57pt measure, where the cell's line needs 135.3pt and is one line
in Word (377.4..512.5); 136.8 less the 1.5pt of `border-separation` is exactly Word's 135.3.
`TableWriter.applyColumnCustomAttributes` now takes the gap off only where the columns are
not content-sized (`CellSpacingAutofitTest`).

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

**Word's exact height is the whole row, borders included.** FOP reads `height` on an
`fo:table-row` as the cell's *content* height and advances to the next row by that plus
the border it charges the cell - half of each collapsed border, all of a separate one - so
every exact row came out half a point too tall for a 0.5pt border. `WordLayoutFixups`
takes that allowance off the row's `height` as well as off the clipping container.
Measured on `page-blank`, whose first section is 32 rows of `w:trHeight w:val="400"
w:hRule="exact"` (20pt): Word's row pitch is 20.0 (baselines 617.5 / 637.6 / 657.5 /
677.5) and docx4j's was 20.5 (630.3 / 650.8 / 671.3), 16pt over the page; FOP's area tree
put the rows 20500 millipoints apart for a 20000mp content height, and 20000mp apart once
the height was 19500mp. The same half point was the residual of `table-rowheight`, whose
two exact rows were 30.5 and 10.5pt against Word's 30.1 and 10.0 (probe line parity 89% ->
100%). `w:hRule="atLeast"` rows are not touched: Word's atLeast pitch tracks docx4j's to
0.1pt on the same probe.

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
floating table, whether or not it is taken out of the flow; before 17.1.0 the probe's table
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

<a id="s68tblpy"></a>**`tblpY` is padding above the table inside the float, and it lands.**
Measured on `table-floating` (`tblpY=1440`, 72pt): Word's "float a" cell line is at y=186.8
and docx4j's at **186.6**. What FOP will not do is leave the lines *above* it full width:
it anchors the float at the line it sits at, so the padding narrows those lines too - Word's
first five lines of the anchor paragraph run the full width to x=516 and end at y=169.5,
where ours are cut to x=280 from the paragraph's first line. That is the whole of the
probe's residual, and it is §10's float-anchoring limitation, not the offset.

Left in the flow, where the text follows the table rather than running beside it: a table
filling more than 60% of the column (nothing useful fits beside it; measured on a CV built
out of twelve floating tables, one of them 73% of the column, where Word puts the next table
below it and FOP fitted it into the rest of the band, costing a page), one whose horizontal
position falls outside the text column, and one in a section of more than one column, since
FOP drops a float from a multi-column region silently. A table in a **cell**, a header, a
footer or a footnote is left in the flow too: FOP implements no float under an `fo:table`
and paints nothing at all for one (§10). And a float which a line break inside a run
*follows* is declined, because hoisting it to flow level - which is what that combination
otherwise needs, to avoid FOP's `TraitSetter.setVisibility` NPE - loses a float holding a
table entirely (§10); one corpus document's 219.6pt rubric table is left in the flow for
that reason alone, at a cost of 168.3pt on every line after it.
`docx4j.convert.out.fo.tables.float=false` leaves every one of them in the flow.

The horizontal position the band is measured from is the table's **unshifted** grid edge:
below mode 15 §6.1 has already moved the grid edge back by one left cell margin, which is
about the grid rather than about where Word puts the frame, so a table with no `w:tblpX`
arrived at −108 twips and was declined the float outright.

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
drawn in, the two are drawn on top of each other. Three shapes are positioned: a
table which **opens its section's flow** (the cover page, the letterhead); one which
**opens a page** - everything before it on the page is empty, back to a hard break - and is
narrow enough (60% of the column) for text to fit beside it; and, since 17.1.0, one which
content **precedes** whose band is reserved in the flow ([below](#s68reserve)). Measured on
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

<a id="s68reserve"></a>**A table which content precedes can be positioned with its band
reserved.** The reason it could not was that the flow closing over it drew the two on top
of each other; §9.5's `w:wrap` trick fixes exactly that - an invisible copy of the table
left where it was (FOP honours `visibility="hidden"`: the area keeps its size and paints
nothing) reserves the height in the flow, so what follows is pushed down while the table
itself goes to its anchor. It is right only where the flow has **not already passed** the
table's anchor; where the anchor is above the flow, Word draws the table over ground the
flow has already covered and the reservation is pure error. The fixups cannot know where
the flow has reached, and the measured cut is the anchor's own place on the page:

* a corpus letterhead whose table is anchored at `tblpY=15027` - 751.4pt down an 841.9pt
  page, with 16 paragraphs before it and the letter's body after it - goes from **0.645 to
  0.839** of Word's lines;
* three anchored in the top quarter fall: 203.3pt of a 792pt page 0.933 to 0.853, 66.7pt
  of a 1190.7pt page 0.830 to 0.801, and 94.6pt of an 841.9pt page 0.839 to 0.833.

So the band is reserved only for a table anchored in the **lower half of the page** by a
`tblpY` of its own. A **narrow** table is left in the flow whatever its anchor: Word runs
the text beside it, and reserving the whole band pushes down text Word keeps level -
measured on `table-floating-anchor`, whose page 6 has a narrow table beside the text,
reserving its band cost the probe 0.84 to 0.83 of Word's lines. So is one placed by a
`tblpYSpec`, which gives the frame rather than the table's top edge - the height is not
known before layout, so where the band falls is guesswork, and the one corpus table of
that shape with content before it lost a line.

Two more corpus documents gain lines they were losing under the positioned table
(candidate lines 108 to 151 and 43 to 52, at unchanged parity).
`docx4j.convert.out.fo.tables.reserveBand=false` leaves every such table in the flow, as
17.0.5 did.

A positioned table keeps its columns, cell margins and borders; its own `start-indent` is
reset, because the container (or the float's one-row table) carries the position.
`docx4j.convert.out.fo.tables.position=false` lays every table out in the flow, as 17.0.5
did.

`leftFromText` and `rightFromText` are the gaps the float leaves; `topFromText` and
`bottomFromText` are not used, and nothing wraps beside a *positioned* table (§10).

### 6.9 `w:textDirection`: cells whose text Word turns on its side

`w:tcPr/w:textDirection` `btLr` turns the cell's text 90° anticlockwise
(`reference-orientation="90"`) and `tbRl` turns it clockwise (`-90`). The
`fo:block-container` that carries `reference-orientation` is a **reference area**, and a
reference area turned 90° has to be given *both* of its dimensions: without them FOP
gives the rotated area an inline-progression-dimension of 0 and its viewport a
block-progression-dimension of 0 (area tree: `<block ipd="0" bpd="28800"
is-reference-area="true">` inside `<block ipd="28800" bpd="0" is-viewport-area="true">`),
so the text is set on a line of no measure, takes no width in the cell and is painted past
the page edge. Measured on a corpus certificate whose stub column is `btLr`: Word's first
line at y=53.5 x=99.9..494.8, ours at y=76.1 x=**323.5..672.7** on a 595.3pt page, with the
rows inflated to match.

The two properties are stated in the container's *own* (rotated) frame, and FOP swaps them
onto the viewport - `inline-progression-dimension` becomes the viewport's height and
`block-progression-dimension` its width in the cell. So:

* **`block-progression-dimension` = the cell's content width**: its grid width (its own
  column plus the ones a `w:gridSpan` covers) less the cell margins.
* **`inline-progression-dimension` = how tall the cell is**: the row's `w:trHeight` where
  it states one, and otherwise the cell's *minimum* content width - its longest unbreakable
  unit, the height the rotated text can always be wrapped into - capped at the page's text
  height, which no row can exceed. How tall Word actually makes an auto row depends on how
  many rotated lines the cell's width takes, which is not known before layout; the two
  bounds are that minimum and the whole content on one line, and the minimum measures
  better. Taking the whole content inflates every row whose text Word wraps: where a
  `w:trHeight` is stated it took two documents from 17 pages to 22 and from 39 to 42 (Word
  draws those rows at exactly the stated 148.55pt and 119.5pt), and where none is stated it
  cost a third 0.883 -> 0.878 against 0.884 for the minimum.

Corpus: 15 documents, 160 rotated cells. Five improve - 0.711 -> 0.747, 0.749 -> 0.773,
0.628 -> 0.647, 0.883 -> 0.884, 0.827 -> 0.830 - and two fall a line each, 0.941 -> 0.940
and 0.813 -> 0.807, on rows whose final height Word decides differently from either bound.

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

<a id="s7pgsz"></a>**A continuous section break which changes the page size or the
orientation starts a page.** Word promotes such a break to a page break and gives each part
its own page, and a `fo:page-sequence` carries one page master, so the two parts cannot be
merged. Measured on a 22-page document whose first four `w:sectPr` are `w:type="continuous"`
and whose first three declare `<w:pgSz w:w="23814" w:h="16840" w:orient="landscape"/>` - A3
landscape - against a final A4: Word's page 1 is 1190.6 x 841.9pt with its content at
x=76.6..396.3, where docx4j merged the whole continuous run onto one master, the **last**
part's A4 won, page 1 came out 841.7 x 595.5 and the content ran to x=881.9 - 40pt past our
own page edge, one column overprinting another. Such a break now ends the section instead;
the headers and footers are still this section's, which is what Word keeps, and sections
whose page size agrees are merged as before. `ConversionSectionWrapperFactory.insertPageBreak`
(which already detected the change, and inserted a `w:pageBreakBefore` on the wrong
paragraph - the last of the section rather than the first of the next - which did nothing on
a shared master). That document's page 1 is now 1190.7 x 842.0 and its line parity 0.6494 ->
0.6928; a second document's page count reached Word's. Corpus: 7 documents carry the shape,
2 of them where it changes the output.

**Continuous sections with different column counts.** XSL-FO fixes the column count on the
page master, so a run of continuous sections has to share one. The page-sequence takes the
**largest** count, and each narrower part is wrapped in a container both FO exporters
render as `fo:block span="all"`. FOP balances the columns before such a block, which is
what Word does at a continuous break (a next-page break balances neither side). Sections
whose counts agree are untouched. The common case is a stretch of two-column text inside a
one-column document; taking the last section's count put everything before it into two
columns.

A spanning block is a reference area of its own, so XSL-FO drops the space at its end and
the **last paragraph of the narrower part loses its space-after** - and nothing here has
put it back. Measured on a document whose one-column opening runs into a continuous
two-column section: the 10pt of docDefaults `w:after="200"` on the last block before the
break goes missing, Word's third line at y=290.5 against docx4j's 280.3, and every line
after it carries the -10.2. Two ways of restoring it were measured and both were dropped:
`space-after.conditionality="retain"` on that block keeps the space at *every*
reference-area end, the foot of a page included, where Word drops it - it moved a second
document's later pages by 17.7pt and cost it 0.20 of line parity - and the space-after on
the spanning block itself, which is where it belongs, is discarded by FOP as well
(measured: no change at all). See [§10](#10-known-fop-defects-and-limitations).

<a id="s7onecol"></a>**A section declaring a single `w:col` uses that column's width**,
whether it is narrower *or wider* than the margin box. The narrow half took a 60-page
corpus document from 0.557 to 0.951 (its margin box is 451.45pt and its
`<w:col w:w="8640"/>` 432pt: Word centres on 288 where ours centred on 297.7). The wide
half is measured on a 595.35pt page with 72pt margins - a 451.35pt margin box - whose
`<w:cols w:equalWidth="0"><w:col w:w="9560"/>` declares **478pt**: Word centres a heading
on x=311.15 where ours was 297.5, and ends a right-tabbed line at 535.4 against our 508.0,
a flat **-27.4 = 550 - 522.6**, so Word's page 2 was absorbed into our page 1. Word lets
the section overhang the right margin; docx4j clamps the overhang at the page edge. A
difference under 1% of the margin box is Word's own rounding of a full-width column and is
ignored, and §7's unequal-columns table is built only for several `w:col` children.
**14 documents of the three corpora.**

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
(`Balance.java`). Before 17.1.0 every such section was.

Columns within 5% of each other (4716/4715, 4680/4860 in the corpora) are Word's own
rounding of equal columns and are left to the region body.
`org.docx4j.convert.out.common.wrappers.UnequalColumns` builds the table.

<a id="s73equal"></a>**A column break in columns of equal width**, which the region body
lays out, is the same rule and is taken the same way: the paragraph is divided at the break
(`ConversionSectionWrapperFactory` via `ColumnBreaks`, only for a section whose `w:cols`
declares more than one column), the half which follows it carries the break, and the FO
exporter turns that into `break-before="column"` on its block
(`WordLayoutFixups.columnBreaks`, which also gives the half a line where the break ended
the paragraph). The spacing is the divided paragraph's above: the space-after goes with the
half which ends it and the space-before stays on both. Until 17.1.0 the break was emitted
as an ordinary line break and the column was never taken at all.

Where the section has **one** column there is nowhere to break to, so the break stays the
line break docx4j has always made of it; a break with content before it in its block is one
docx4j did not divide, and is left alone as well.

Two things had to be fixed for FOP to take it. `WordFlowLayoutManager` moves each
paragraph's trailing leading-glue behind the break possibility that follows it (§2.3);
where that possibility is a **forced** break which *ends* the element list - which is how a
break between two blocks reaches the flow - the glue after it makes FOP's
`ElementListUtils.endsWithForcedBreak` false, `AbstractBreaker` does not end the block
sequence, and the break is silently dropped. A forced break discards glue at the start of
the column it opens anyway, so the glue is now removed rather than moved, which is the same
thing on the page and leaves the list ending in the break. Measured on a two-column corpus
document: Word's column 2 opens with "Epsum factorial" at x=315.4, where ours carried on in
column 1 at x=72.0 and spilled Word's one page onto two. (Stock FOP took the break; only
docx4j's line-and-flow managers lost it, and only for a block carrying
`docx4j:line-box` - i.e. every block docx4j writes.)

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
"Vertical alignment" - is `display-align` on `fo:region-body`: `center` for `center`,
`after` for `bottom`, nothing for `top`. It
costs nothing on a full page, so it applies to the whole section as Word applies it.
Measured on a 179-page specification whose title section carries
`<w:vAlign w:val="center"/>`: every line of page 1 was 112.5pt above Word's (Word's first
line at y=275.9, docx4j's at 163.4), and is now within 6pt.

<a id="s75both"></a>**`both` is vertical justification, and FO has no property for it.**
Word keeps the first block at the top of the text area and the last at its bottom, sharing
the slack between the blocks. Measured on `section-valign-bottom`, whose three `both`
sections Word opens at y=83.1 - the same as an unaligned section - and closes at 743.7 and
767.5, the bottom margin: with `display-align="center"` docx4j put the first paragraph
384.8 and the last 465.8, up to 302pt out on every line. The top is the half of it FO can
express, so `both` now takes the region's default alignment; the residual is the spread
between the blocks, and reproducing it needs FOP.

<a id="s75after"></a>**An aligned section counts its last paragraph's `space-after`** as
part of the block it aligns. `space-after.conditionality` defaults to `discard` at the end
of a reference area, so FOP dropped it and put the last line on the bottom margin. The
probe pairs a last paragraph carrying 24pt of space-after with a control carrying none:
Word's two bottom-aligned pages close at y=743.7 and 767.5, 23.8pt apart, where docx4j put
both at 767.4, and its centre-aligned pair is 11.8pt apart, half of the same 24pt. The
other end does not: a bottom-aligned section whose **first** paragraph carries 24pt of
space-before closes where the control does. `WordLayoutFixups.retainSpaceAfterInAlignedFlow`
marks the last block of an aligned flow `retain`; the probe's median dy went from 7.77 to
-0.01.

<a id="s75tbl"></a>**Where the section's last block is a table**, Word's aligned content
also holds the empty paragraph a table must be followed by. docx4j drops a paragraph whose
only content is the `w:sectPr` - Word gives it no line anywhere else, and adding one at the
end of an unaligned flow only pushes the flow's last line off the page - so since 17.1.0 it
is kept for a vertically aligned section whose content ends with a `w:tbl`, and nowhere
else (`ConversionSectionWrapperFactory.closesAlignedTable`). On the probe's three table
sections Word closed 15.69pt above us and 7.67pt on the centred one; with the paragraph's
line back, 2.26 and 0.95. The `both` section does not move, since vertical justification
takes the region's default alignment (above). What is left is that our empty block is
13.4pt tall where Word's line is 15.7.

<a id="s7gutter"></a>**The gutter.** `w:pgMar/@w:gutter` is the binding margin, and Word
adds it to the **left** margin - to the top with `w:settings/w:gutterAtTop`, and to the
right of an even page where `w:mirrorMargins` is set. `PageDimensions.getWritableWidthTwips()`
already subtracts it, so until 17.1.0 the text column was the right width and started in the
wrong place: measured on a document with `w:pgMar w:left="851" w:gutter="567"`, Word puts
every portrait line at **x=70.9** (851 + 567 twips) where docx4j's was at 42.5, -28.35pt on
all 222 pages. Two documents of the three corpora set a gutter.

<a id="s7gutterland"></a>**A landscape section takes no gutter at all.** The same document
is the measurement, since its two `w:sectPr` both carry `w:gutter="567"`: in the landscape
one, whose `w:left` is 680 twips, Word puts the running head at x=49.7 and the table's grid
edge at 28.55 = 34.0 less one 5.4pt cell margin - i.e. on `w:left` itself. Adding the
gutter there put all 222 landscape pages 28.35pt right of Word's (our text ran 62.4..843.2
against Word's 34.1..804.2, past the 841.9pt page edge) *and* narrowed the writable width,
so the document's 100%-wide tables came out 737pt against Word's 765.35pt, the full
`w:tblGrid`. It is not moved to the top either: Word's table on that page begins at
y=52.85, above the 70.9pt a top gutter would give. `PageDimensions.getGutter()` therefore
returns 0 for a landscape section, and `getWritableWidthTwips()` reads it, so the two
cannot disagree.

<a id="s7col"></a>**A single narrow `w:col`.** Where `w:cols` declares exactly one `w:col`
narrower than the margin box, Word uses that width for the text. The unequal-columns table
above is built only for *several* `w:col` children, so a single narrow one fell through:
measured on a document whose margin box is 451.45pt and whose section says
`<w:cols w:space="720" w:equalWidth="0"><w:col w:w="8640"/>` (432pt), Word centres on
`72 + 216 = 288` against docx4j's 297.7 (**+9.7pt on every centred line**) and puts the
right edge at 504 against 523.45 (**+19.45pt on every right-aligned line**), over 58 pages;
the document's line parity went 0.557 to 0.910. A declared width within 1% of the margin
box is Word's own rounding of the full width and is ignored.

**Where the body starts.** Word starts the body at the top margin and moves it down only
where the header itself reaches further, i.e. `max(top margin, header distance + header
height)`. Using the header distance alone pushed the body down by `w:pgMar/@w:header` minus
`w:pgMar/@w:top` wherever the distance was larger and the header empty (13.9pt on every
line of one document). Header and footer extents come from an area-tree pre-pass
(`FOPAreaTreeHelper`), which is how their real heights are known. Measured:
`page-first-even-odd-heights` - a three-line first-page header, a one-line even header, a
five-line odd header containing a picture, one-line odd and three-line even footers, six
pages - matches Word on 7 of 7 pages and 318 of 318 lines.

<a id="s7hfspace"></a>**A header's height includes its last paragraph's space-after.**
XSL-FO drops space at the end of a reference area, so the pre-pass measured one space-after
short and the body then started that much too high (and ended that much too low). Measured
on a document with `w:pgMar w:top="1440" w:header="709"` whose header and footer each hold
two paragraphs 10pt apart: our `region-before` extent was 58.867pt and `region-after`
32.362pt - 11.181 + 10 + 11.181 exactly, the middle 10 being the space between the two
blocks and the trailing one excluded. Word's first body line is at y=113.6 where docx4j's
was 102.3, and Word's footer line at 722.6 where docx4j's was 731.9: **20.6pt more body on
every one of 311 pages**. `WordLayoutFixups` now pins the last block of each
`fo:static-content` with `space-after.conditionality="retain"`, so the pre-pass measures
what Word measures; the space itself is invisible either way, since a static-content is
laid out from the region's top edge. 66 documents of the three corpora have a header or
footer whose last block carries a space-after.

<a id="s7nohdr"></a>**A header or footer the document does not have reserves nothing.**
Where `w:titlePg` or `w:evenAndOddHeaders` asks for a header or footer the document has no
part for, docx4j invents an empty one (`HeaderFooterPolicy.getDummyHeader`) so that the page
master has a region to hang the static-content on. Its one empty paragraph measured a line
box, and that line box was reserved: measured on a document with no header part and no
`w:headerReference` at all, `w:pgMar w:top="432"` (21.6pt) and `w:header="706"` (35.3pt),
Word's body top is `w:top` = 21.6 where ours was `35.3 + 13.799` = 49.1, and every line and
the logo came out **+26.5 to +27.5pt** low (Word's line 203.3 -> ours 229.8; the logo's
`mutool` ty 69.9 -> 97.4). Such a part now reserves nothing, on either side. 44 documents of
the three corpora have `w:header` greater than `w:pgMar/@w:top` and no `headerReference`.

<a id="s7emptyhf"></a>**A header or footer part with nothing in it reserves nothing
either.** The rule above is about the part docx4j invents; a *real* part holding only empty
paragraphs is the same to Word. Measured on a document with `w:pgMar w:top="510"` (25.5pt)
and `w:header="709"` (35.45pt) whose `header1.xml` is a single empty `w:p`: Word's body top
is 25.5 (first baseline 38.7) where ours was `35.45 + 13.428` = 48.88 (baseline 61.6),
**+22.9pt on every page**, and Word's last page-1 row fell onto our page 2. At the foot,
a document with `w:bottom="1418"` (70.9pt) and `w:footer="5811"` (**290.55pt**) whose two
footer parts are each one empty `w:p`: Word's body runs to 756.2 and paints nothing at 551,
where our `margin-bottom="290.55pt"` ended it at 551.4 and made **3 Word pages 5**.
`HeaderFooterPolicy.reservesNothing` decides it: **at most one paragraph**, and that
paragraph holding no non-blank `w:t`, drawing, `w:pict`, table, field or symbol. The
one-paragraph limit is measured too - a single empty `w:p` is what Word writes for a header
it has been given and then cleared, while *several* empty paragraphs are blank lines the
author put there and Word reserves them: on a two-empty-paragraph header with
`w:pgMar w:top="1417"` (70.85pt) and `w:header="708"` (35.4pt), Word's first body baseline
is 110.7, i.e. a body top of about 90.6 = 35.4 + the header's own ~55pt, where reserving
nothing put ours at 70.85 and every line 19.8pt above Word's. 7 documents of the three
corpora have `w:footer` greater than `w:pgMar/@w:bottom` with every footer part empty, and
70 have a header that is only empty paragraphs.

<a id="s7emptyftr"></a>**Settled, and at the foot the rule is different (17.1.0).** The two
documents which disagreed with the paragraph above are the discriminator: an *empty footer
part* is not the same as **no** footer part. With no `footerReference` at all Word reserves
nothing, not even the distance - a document whose 44 `w:sectPr` say `w:bottom="0"` with
`w:footer="720"` runs its body to the foot of the A4 page. With a footer part which happens
to paint nothing, Word still stops the body at `w:footer`: measured on an A4 document whose
three footer parts are each a single empty `w:p`, with `w:pgMar w:bottom="274"` (13.7pt) and
`w:footer="720"` (36pt), Word's body ends at y=792.5 - the distance plus the empty footer's
own 13.43pt line - and puts the next, 12pt, block on the following page, where the bottom
margin alone (a body bottom of 828.25) kept it; our page 1 held content Word puts on two.
The clamp the "reserves nothing" rule really needed was against an **absurd** `w:footer`:
the document it was measured on states `w:footer="5811"` (290.55pt, a third of an 841.95pt
page) and Word ignores it entirely, its last baselines 756.2 and 767.0 against a 70.9pt
bottom margin. So a footer distance past **a quarter of the page** is not honoured, and
otherwise an empty footer part holds the body off at `w:footer`.
`HeaderFooterPolicy.isAbsent` tells the two cases apart. The head of the page keeps the
rule above unchanged - there an empty part reserves neither the distance nor a line box,
which is what its own measurement says. **15 documents of the three corpora have an empty
footer part.**

<a id="s7hfanchor"></a>**An anchored drawing in a header or footer does not make it
taller.** Word positions a floating object out of the flow and sizes the region on its
in-flow paragraphs; we laid the picture out in the flow and charged its height to the
region. Measured on a header whose one paragraph holds only a `wrapSquare` anchor, with
`w:pgMar w:top="1394"` (69.7pt) and `w:header="283"` (14.15pt): Word's body top is 69.7
where our `region-before extent="90.453pt"` put it at 104.6 - **+34.7pt on every line**,
and the tail spilled onto a second page holding nothing but the footer. A header holding
one `wrapTopAndBottom` picture 54.35pt tall came out as `extent="114.054pt"`, exactly
**+54.5pt**, and 3 Word pages became 4. The extent pre-pass therefore takes floating
drawings out of the header and footer paragraphs it measures
(`FOPAreaTreeHelper.dropFloatingDrawingsFromHeadersFooters`, on the pre-pass's own copy -
the real render still paints and positions the picture). Only a paragraph of the header or
footer *itself*, not one in a table it holds: a cell's height is the row's, and doing it
everywhere shortened letterhead tables Word does size around (measured: -0.043 and a page
on one document, -0.022 on another). `docx4j.convert.out.fo.headerExtent.ignoreFloatingObjects=false`
restores 17.0.5's behaviour.

<a id="s7hfspacebefore"></a>**The first paragraph of a header or footer keeps its
space-before.** `space-before.conditionality` defaults to `discard` at the start of a
reference area, the mirror of [§7's space-after rule](#s7hfspace), so FOP dropped it where
Word applies it. Measured on a header whose first block carries `w:before="66"` (3.3pt) and
whose baseline is 8.004pt, with `w:header="426"` (21.3pt): Word's first header baseline is
`21.3 + 3.3 + 8.004` = **32.6** exactly, where ours was 28.3 - **-4.3pt on all 16 pages**;
a second document, whose header style carries `w:before="153"` (7.65pt), has Word at 59.5
against our 50.9. It is pinned only where that first block actually paints something: in
the pre-pass a header whose only content is a floating drawing is measured empty (above),
and retaining the placeholder's space there put a document's spurious second page back.
59 static-contents in 25 documents of the three corpora carry it.

<a id="s7hftblstyle"></a>**A table in a header or footer takes its table style's `w:pPr`
and `w:rPr`,** as one in the body does. ECMA-376 puts the table style's paragraph and run
properties above `docDefaults` and below the paragraph style, which is what
`ParagraphStylesInTableFix` (the `pp.common.tbl-p-style-fix` preprocessing step) builds a
synthetic style for - but it only ever walked the main document part.

Two settings govern that step. **`w:compatSetting overrideTableStyleFontSizeAndJustification`**
(which Word 2010 and later write on every save, 293 documents of the three corpora) decides
whether the default paragraph style's size and `w:jc` override the table style's;
`DocumentSettingsPart.overrideTableStyleFontSizeAndJustification` is read by the step, and
`PStyle11PtInTableOverrideFalseTest` / `PStyle12PtInTableGridOverride*Test` measure it.
**`w:compat/w:useWord2002TableStyleRules`** ("Emulate Word 2002 Table Style Rules") would
switch the whole step off - Word 2002 did not put a table style's `w:pPr` and `w:rPr` above
`docDefaults` at all - and reading it was measured and rejected in 17.1.0. Three mode-11
corpus documents carry a Word-2003 compat block, one without the flag and two with it, and
Word 365's own PDFs of all three apply the table style's properties: skipping this step for
them cost 0.951 -> 0.105 (with eleven of fifty pages), 0.948 -> 0.248 and 0.930 -> 0.842 of
Word's lines ([settings §4(e)](word-layout-settings.md)). The `compat-tables` probe pair
carries the shape for a golden. Measured on a
document whose header table uses `TableGridLight`
(`<w:pPr><w:spacing w:after="0" w:line="240" w:lineRule="auto"/></w:pPr>`) and whose
paragraphs have no `w:pStyle`: Word's four baselines in the row-spanning cell are 104.2 /
116.9 / 129.4 / 142.1 - a pitch of **12.7pt**, the line box alone - where ours were 147.0 /
167.7 / 188.3 / 208.9, a pitch of 20.7 = 12.649 + 8, because `docDefaults`' `w:after="200"`
survived on all 16 header paragraphs. That is 128pt of drift and a
`region-before extent="341.265pt"`, now 215.0. 152 documents of the three corpora use a
table style which carries a `w:pPr/w:spacing`.

<a id="s7extfail"></a>**If the pre-pass fails, the extents fall back to nothing**, not to
the half-page values it starts from. `LayoutMasterSetBuilder` seeds each region with half
the page height and replaces it from the area tree; when the pre-pass threw, the seed
survived and the body became a strip a couple of lines high. Measured: one document renders
**35 pages for Word's 3**, its FO saying `<region-before extent="396.0pt"/>` and
`<region-after extent="396.0pt"/>` on a 792pt page with no header or footer part at all, so
each page held two or three lines at y=366.7. The cause was its
`styles.xml/docDefaults/rPrDefault/rPr` carrying `<w:vanish/>` - every run in the document,
the pre-pass's own filler paragraphs included, is hidden text - which produced an empty
`<fo:flow/>`, invalid FO. Both halves are fixed: the filler runs override `w:vanish`, and an
`fo:flow` or `fo:static-content` with nothing in it gets one empty `fo:block`, as an empty
`fo:table-cell` already did.

<a id="s7negmar"></a>**A negative `w:pgMar/@w:top`** means "the body starts |top| from the
page edge whatever the header does": Word lets the header overlap the text rather than
pushing it down. Measured on a document with `w:top="-312"` (-15.6pt) and `w:header="709"`:
Word's body top is 15.55pt where `max(top, header + header height)` put docx4j's at 49.25 -
**+33.7pt** on the table header, on page 2 and on each of six pictures (Word
82.1/117.1/155.8/195.0/259.9/305.0, docx4j 115.8/150.8/189.8/229.3/294.5/346.6), and 8 Word
pages came out as 14. A second document, `w:top="-993"` (-49.65pt), was **+97.5pt**
throughout and 16 Word pages came out as 21; both are page-exact now. `w:bottom` is the
mirror of it.

<a id="s7pagefield"></a>**`PAGE` in a header or footer must be live.** Word commonly writes
the field's `w:fldChar begin`, `w:instrText` and `w:fldChar separate` in **one run** - and
inside a `w:sdt` when it comes from the "Page Numbers (Bottom of Page)" building block.
`FieldsCombiner` looked at one `w:fldChar` per run, so it saw the BEGIN and never the
SEPARATE, the field was never combined into a `w:fldSimple`, and its runs were emitted as
the *cached result*: the FO held no `fo:page-number` at all. Measured: a 76-page document
printed "Página 73 de 76" on all 76 pages where Word prints 2 ... 76, and a 23-page one
printed "1" on all 23 - one wrong line on every page. Such a run is now split into one run
per item (which renders identically) before the combiner sees it. 52 documents of the three
corpora have a header or footer `PAGE` field and no `fo:page-number`.

A `NUMPAGES` in any section but the last becomes an `fo:page-number-citation-last` naming a
**later** page-sequence, which FOP cannot resolve while it lays that section out, so it
painted nothing: Word's header `"2 di 78"` (`x=479.5..506.9`) against our `"2 di"`
(486.1..499.9). Such a document now takes the 2-pass path, which resolves the count from
the area tree first, as it already did for `SECTIONPAGES`. 17 documents of the three
corpora.

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

**A complex field with no `w:fldChar w:fldCharType="separate"` has no result**
(ECMA-376 17.16.18: the result is what lies between the separate and the end), so Word
paints nothing for it - everything from the begin to the end is field instruction. For an
`IF` field that is how Word writes a conditional block of a footer,
`IF { PAGE } = { NUMPAGES } "…" ""`, with the true branch - which may be a whole table -
sitting in `w:instrText`. We kept the structure, so the operands' own `PAGE` and
`NUMPAGES` fields (which *do* have a separate, so FieldsCombiner turns them into live
fields) were painted, and the true branch's table reserved its height. Measured against
Word 365 on an 8-page document whose footer is exactly that: Word's footer is the page
number alone ("1/8" at y=811.2) and ours had a stray "19" at y=724.4 and a
`region-after extent="112.251pt"` against Word's ~47 - 65pt of body lost on every page,
which is the 6 missing lines and the 9th page (9 pages -> Word's 8).

The whole span therefore goes, paragraph marks apart. Restricted to `IF` deliberately:
measured over the 435 corpus documents, a field with no separate is most often `PAGE`
(24 occurrences in 21 documents), `FORMCHECKBOX`, `MERGEFIELD` or `XE` - all of which we
do render and Word renders too once it updates the field on print. Only `IF` carries its
branches in the instruction, so only `IF` loses content by being kept (10 occurrences in
3 documents). Property `docx4j.convert.out.fields.dropResultlessIf`.

<a id="s7formfield"></a>**A legacy form field paints what its `w:ffData` says, not its field
result.** `FORMDROPDOWN`, `FORMTEXT` and `FORMCHECKBOX` keep their state in the `w:ffData`
of their `w:fldChar w:fldCharType="begin"`, which neither FO pathway nor HTML read at all;
both emit nothing for a `w:fldChar`, so a drop-down came out as the empty `fo:inline` of
its bookmark. Measured against Word 365 on a document whose drop-down offers four
honorifics with no `w:result`, and whose `separate` is immediately followed by its `end`:
Word paints the first of them, so its line runs 297.7..413.6 where ours began at 302.7
and ended at 378.3 - the whole entry, 35.3pt of it, missing. The
selected entry is `w:ddList/w:result` as a zero-based index into the `w:listEntry` list,
0 where there is none (ECMA-376 17.16.20); a `FORMTEXT` with nothing typed into it paints
its `w:textInput/w:default`, and one with a result keeps the result, which is what the
user typed. A `FieldsCombiner` step writes the value out as the field's result run, ahead
of the pass which combines complex fields to `w:fldSimple`, and synthesises a `separate`
where the field has none (17.16.18 puts the result between the separate and the end).
Property `docx4j.convert.out.fields.formFieldResults`; HTML gains it too. Over the three
corpora there are 4 such drop-downs in 2 documents, and no `FORMTEXT` whose default is
unused - 61 of the 288 carry one and every one of those already has a result.

<a id="s7formcheckbox"></a>*A checkbox is deliberately left alone, and this is why.* Word
does not draw one with a glyph. Measured with `mutool draw -F trace` over the goldens of
the 15 documents of the three corpora which hold a `FORMCHECKBOX`, every checkbox in
Word's own PDF is a **stroked square path** - a 0.72pt line, side 7.44 / 7.92 / 8.64 /
9.84 / 10.32 / 11.04 / 11.28pt with the field's font size - and the PDF's text layer has
nothing at all where it sits. Writing a `☐` or `☒` for it, which is what one would
expect from the way Word's own UI draws it, would put a character on the line that Word's
line does not have. What docx4j does lose is the box's **advance**: measured on three
documents by the shift of the text after the box, Word's line continues 9.68, 12.4 and
12.76pt further right than ours (the box side plus about 2.4pt), on 772 fields in 15
documents. Reserving that width without painting anything is what would close it; FO has
no inline box (`fo:inline-container` is not implemented in FOP, §10), so it is not shipped.

*The condition is not evaluated, and does not need to be.* The branch Word chooses on all
but the last page of these documents is the empty one, and neither branch could be
painted conditionally anyway: FOP resolves `fo:page-number` at layout time, long after
the FO is written, so an `IF` whose operand is `PAGE` cannot be decided per page (§10).

<a id="s7pgnumfmt"></a>**`w:pgNumType/@w:fmt` is the section's page number format**, and
becomes `format=` on the `fo:page-sequence` - which it never did where the section's `PAGE`
fields carried a `\*` switch that names no number format. The switch collector recorded the
empty value of a bare `PAGE`, and the `MERGEFORMAT` of `PAGE \* MERGEFORMAT`, and either,
being non-null, masked the section's own format. Measured against Word 365 on a document
declaring `<w:pgNumType w:fmt="upperRoman"/>` whose two footers hold exactly those two
fields: Word's footer prints `I`, `II`, ... on all 25 pages where ours printed `1`, `2`, ...
- essentially that document's whole parity loss. `MERGEFORMAT` and `CHARFORMAT` say what to
do with the field's *character* formatting when it is updated and name no number format, so
neither overrides `w:pgNumType/@w:fmt`; a switch which does name one still wins. The
`lowerRoman` / `upperRoman` / `lowerLetter` / `upperLetter` / Thai / Devanagari / full-width
/ leading-zero mappings were already in `FormattingSwitchHelper`, and the same format is
what the two-pass `NUMPAGES` literal and the glyph-selection sample use.
**9 documents of the three corpora.**

**A `DOCPROPERTY` keeps the result the document cached.** Word does not re-evaluate one when
it opens or prints a document - only an explicit update changes it - so where the property
and the cached result have diverged, the cached text is what a reader of Word's own PDF
sees. Measured on a document whose `docProps/custom.xml` says `invoice_nr=1018` and
`mwst-nr=CHE-258.324.254` while `document.xml` caches `10518` and `CHE-XXX.xxx.xxx.xxx`:
Word prints the cached text on all three of the lines involved, and printing the evaluated
one was that document's whole parity loss. Property
`docx4j.convert.out.fields.docPropertyCachedResult`; a field with no cached result at all is
still evaluated. **8 documents of the three corpora.**

**A `DATE`, `TIME` or `PRINTDATE` field is formatted in the document's own language**
(`w:docDefaults/w:rPrDefault/w:rPr/w:lang`), not in the platform default. Measured on two
Turkish documents whose `DATE` carries `\@ "d MMMM yyyy"`: Word prints `6 Eylül 2026`
(270.1..325.1) where ours printed `7 September 2026` (256.5..338.8). An abbreviated month
name (`MMM`) loses the trailing period CLDR gives it in several languages, since the format
string is where Word's punctuation comes from - a German `dd". "MMM". "yyyy` came out
`07. Sep.. 2026` otherwise. Word's own abbreviations are not always CLDR's (Word's German
September is `Sep`, CLDR's `Sept`), which is a residual.

**`w:pgNumType/@w:start` and the odd/even page masters.** XSL 1.1 §6.4.5 makes
`initial-page-number` a positive integer and FOP clamps anything smaller, so a section
whose `sectPr` says `<w:pgNumType w:start="0"/>` - Word's usual way of writing a cover
page that is "page 0" so the first numbered page is 1 - lays out as folio 1 where Word
calls it page 0, and every `odd-or-even` alternative after it selects the wrong master.
The alternatives are therefore built with ODD and EVEN swapped from such a section on
(and the swap carries into the sections that follow, since their numbering continues).
Measured against Word 365 on a document with `w:evenAndOddHeaders`, an even header of no
`w:jc` and a right-aligned default one: Word's page 2 header is right-aligned at
x=430.3..524.7 and ours was the even one at 70.9..162.9, page 3 the mirror image; it is
now 432.4..524.4. Property `docx4j.convert.out.fo.pgNumType.oddEvenParityFix`.

*The printed number cannot be repaired the same way*: `fo:page-number` is formatted by
FOP from the folio it clamped, and nothing in XSL-FO offsets it, so a `PAGE` field in
such a section still prints one too high (§10). The two-pass literal that carries
`NUMPAGES` is one value for a whole section, and `PAGE` differs on every page of it.

**`w:settings/w:mirrorMargins`.** Word calls `w:pgMar/@w:left` the *inside* margin and
`@w:right` the *outside* one, so on an even (left-hand) page they swap - and so does the
binding edge `@w:gutter` widens. Every page master that is not already chosen by page
parity therefore gains a mirrored twin for the even pages. The twin is a copy of the
finished master, taken *after* the extent pre-pass so that it carries the measured header
and footer extents, and it keeps the same region names, so one `fo:static-content` serves
both. (Built before the pre-pass it would have no measurement of its own: the pre-pass
renders one page per section, so a master only an even page uses is never exercised and
keeps the dummy half-page extent - measured, three corpus documents came out as an export
exception when the twin was made by a JAXB round-trip copy, which the XSL-FO model's
non-root page masters do not survive either.) Measured against Word 365 on a 42-page
document whose three `sectPr` all say `w:left="2268" w:right="1418"`: Word's even pages
start at x=70.8 and ours at 113.4 - 42.6pt out on half the document; they are at 70.9
now. 4 documents of the three corpora. Property
`docx4j.convert.out.fo.mirrorMargins`.

**A space in front of a page number.** FOP treats the end of an `fo:inline` as a place
where a trailing space can be collapsed away, so `<w:t xml:space="preserve">Seite </w:t>`
followed by a `PAGE` field lost its space: Word's "Seite ii" runs 526.1..547.6 = 21.5pt
and ours read "Seiteii" at 527.6..547.4 = 19.8, exactly the 1.81pt an 8pt Carlito space
is. A zero-width space after it - the workaround FldSimpleWriter already uses at the
other end of an `fo:page-number-citation-last` - keeps it. A no-break space keeps it too
and measures the same, but it *is* the text: a footer that already had its space came out
as `"Page\u00a01 / 3"`, which is not what Word wrote (0.82 -> 0.77 on that document
before the zero-width space replaced it).

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
| square, tight, through | an `fo:float` at the nearer edge, padded so the picture sits where Word puts it (text on the other side only); see [where no float is possible, and where none is wanted](#s91nofloat) |
| topAndBottom | a block-container as tall as the picture, at the paragraph's top |
| none (behind or in front of text) | an absolutely positioned block-container that takes no space |
| position relative to page or margins | fixed on the page, no wrapping |
| any wrap, with `@behindDoc="1"` | positioned and taking no space: the wrap is not applied |

<a id="s91behind"></a>**`wp:anchor/@behindDoc="1"` displaces nothing** (17.1.0): Word draws
the picture behind the text, the wrap element is not applied, and nothing is reserved.
Measured on a document whose 155.25pt logo carries `wrapSquare` and `behindDoc="1"`: Word
puts the caption beside it at x=**85.0** where the float put ours at 249.3 - +164.3 =
155.25 plus its 9.0pt `distL`/`distR` - and the float also moved a whole block from above
the table to below it, everything after it +92.6pt. **9 documents of the three corpora.**

Measured: all four probe pictures within 0.3pt of Word's position. Word flows text down
**both** sides of a picture in the middle of a column; XSL-FO's floats are single-sided, so
that case is not reproducible.

FOP details worth knowing if you touch this: a picture's block needs `font-size` 0.1pt and
`line-height` 0 to sit on its container's top; `space-before` inside a float is discarded;
a block-container inside a right float lands at the left edge, so a plain `fo:block` is
used. `docx4j.convert.out.fo.pictures.float=false` lays these pictures out in the flow
instead; see §10 for why you might want that.

<a id="s91nofloat"></a>**Where there is no float to be had** - a table cell, a header or
footer, a footnote, a multi-column region - a wrapped picture takes the text box's
treatment ([§9.2](#92-text-boxes)) rather than reserving its height: narrower than 60% of
its measure it is positioned where Word puts it and takes no space, wider than that it
reserves its height as a top-and-bottom wrap does - except in a multi-column region, where
it is positioned whatever its width ([below](#s91cols)). The measure is the containing cell's
content width in a cell (Word measures a `relativeFrom="column"` offset from the cell
there) and the section's text column otherwise. FOP does not implement floats in a table
at all - it logs "the following feature isn't implemented by Apache FOP, yet: fo:float
(on fo:table)" and paints nothing - so the alternative was the reservation, and where a
cell held two wrapped pictures the two reservations stacked. Measured on a corpus cover
page whose cell holds a 495.95 x 98.55pt banner at (0.25, 0) and a 108 x 19.5pt logo at
(1.5, 10.6): Word draws them **overlapping**, at x=56.7 y=70.85 and x=58.2 y=81.45 from
the page's top left, and makes the row as tall as the lower of the two plus the
paragraph's own line; docx4j stacked 98.55 + 30.1pt and every line of the page was 28.0pt
low, 57 pages against Word's 56. With the rule, the logo lands within 0.05pt of Word and
the page counts agree. 28 such containers sit in table cells across 15 documents of a
103-document corpus, 15 of them narrow enough to be positioned.

An absolutely positioned container is placed relative to its own zero-height wrapper (an
`fo:block-container` is a reference area), so the wrappers which take no space go ahead of
those which reserve height at the head of the paragraph; with the reservation first the
logo above came out at 179.9 rather than 81.5, one reserved height low.

<a id="s91indent"></a>**A paragraph which begins with such a wrapper keeps its first-line
indent.** The wrapper is block-level, so FOP puts the inline content after it into an
anonymous block, which starts at the block's `start-indent` whatever `text-indent` says.
Measured on a corpus document whose "Dear Sir," paragraph is
`<w:ind w:left="60" w:firstLine="360"/>` (3pt + 18pt) and begins with a `w:pict`: our FO
carried both properties and Word drew the text at x=**21.1** where we drew it at **3.0**.
The indent is now reserved by an `fo:leader` of exactly its width at the head of the inline
content - which is what a leading tab and leading whitespace already are ([§4.4](#44-tab-stops),
[§4.5](#45-runs-of-spaces)) - and the property is taken off the block, so the lines after
the first are not indented too. A hanging indent (a negative `text-indent`) is left alone:
there is nothing to reserve, and FOP puts the first line at the start-indent, which is where
Word puts it.

<a id="s91cols"></a>**In a multi-column region a wrapped object is always positioned**,
whatever its width, and so is a text box. FOP paints nothing at all for an `fo:float` in a
multi-column region - neither the picture nor an indent beside it - and the alternative,
reserving the height at the anchor, is charged to the column the *anchor* is in, which is
Word's column only when the object is in that column, and even then Word wraps beside it
rather than below it. Measured: a landscape two-column document (columns 360.675pt) whose
87.75 x 48pt `wrapTight` logo was given to `fo:float` has one picture in its PDF where
`mutool draw -F trace` counts two in Word's, and whose 340.15 x 246.75pt picture anchored
406.0pt from the margin - i.e. in column 2 - reserved 278.4pt at the head of column 1,
putting the title at y=323.0 against Word's 37.0. Positioned, both land where Word draws
them (the second at x=448.5 in column 2) and the title stays at the top. On a portrait
two-column page whose 186.75pt text box sits in a 213pt column, reserving its height cost a
page and took line parity from 0.478 to 0.087. The **column** width, not the section's text
column, is also what a `relativeFrom="column"` offset is measured within, and an object
whose horizontal position is at or past it belongs to a later column, so it reserves
nothing where it is anchored. `WordLayoutFixups.columnCount`/`columnWidthPt` read the
`column-count` and `column-gap` of the page master the object's page-sequence names.

**A picture which leaves no room beside it is not floated**, even in the main flow. Word
wraps beside a picture as long as any measure is left, and it is a **wide** measure that
is left: measured on a corpus document's page 2, a 348pt `wrapSquare` picture on a 453.55pt
column (77%) has Word running seven lines down the 105.65pt beside it. But where the
picture is as wide as the column there is nothing to wrap, and Word puts the text below it,
which is what the flow does anyway - measured on a document whose two full-page pictures
(550.1 x 708.6pt and 497.4 x 648.1pt on a 459.9pt column) Word gives a page each: floated,
FOP anchored both to a line near the foot of one page and drew them on top of each other
past the page edge, and Word's five pages were three. So a picture over **90%** of its
measure reserves its height instead. Of the 70 picture floats of the three corpora, 13 are
that wide, in 8 documents - and they are also the floats FOP's two float defects (§10) bite
on. (The 60% cut above, which decides between positioning and reserving where no float is
possible, is a different question: neither of those reproduces the wrap at all, and 60% is
where the smaller error changes over - §9.2.)

**A picture fills the frame the document declares.** Word draws a picture at its
`wp:extent` (or the VML shape's `width`/`height`) whatever the stored bitmap's own aspect
ratio - a crop (`a:srcRect`) or a deliberate stretch makes the two differ. XSL-FO's
default `scaling` is `uniform`, so with both `content-width` and `content-height` given
FOP scales by the smaller factor and leaves the rest of the frame empty: measured on a
document whose 4:3 photographs are cropped to 1.9:1, Word draws one 492.71 x 259.85pt and
FOP drew it 346.49 x 259.87, so two pictures Word fits on a page needed a page each. The
graphic therefore carries `scaling="non-uniform"`. **Limitation**: the crop itself is not
reproduced - the picture is stretched into the frame rather than cropped to it, which is
the same geometry and the wrong content. 651 of the 1737 pictures of a 103-document corpus
(50 of its documents), 55 of 435 and 22 of 79 in the two others, declare an extent whose
aspect differs from the bitmap's by more than 2%.

**A VML picture** (`w:pict/v:shape` holding a `v:imagedata`) whose shape style says
`position:absolute` is placed the same way, from the same shape properties a VML text box
uses (§9.2). Word writes an absolutely positioned picture exactly as it writes a text box;
docx4j rendered every VML picture inline at the end of its paragraph, which took a line the
picture does not take. Measured: a first-page header with a 66pt picture at
`position:absolute` plus ten right-aligned address lines had Word's first header line at
y=34.3 x=510.5 and docx4j's at y=94.3 x=255.1 - 60pt down and off its alignment - and the
header's measured extent (§7) was 203.4pt against Word's ~111. A picture whose shape states
no position is still laid out in the line, as Word lays it out.

**An embedded object** (`w:object` holding the same `v:shape`/`v:imagedata`) is drawn as its
preview picture, by that same path: Word draws an Equation Editor or MathType equation, an
embedded workbook or Visio drawing, or an object shown as an icon, as the picture stored for
it, never as a placeholder or a label. So the preview's size comes from the VML style, a
`position:absolute` shape is anchored as above, and a metafile preview - which is what Word
writes for an equation - is drawn by the metafile renderer (§9.4). Until 17.1.0 neither FO
exporter matched `w:object` at all, so an object's preview was simply dropped; the same
preview inside a `w:pict` rendered, which is what hid it. Where the object carries no
`v:imagedata`, nothing is emitted.

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

<a id="s92inset"></a>**The inset puts the text inside the shape.** Word's `width`/`height`
are the shape, and its text starts one inset in from them. Down the page that is padding on
the positioned `fo:block-container`, whose content box is the shape less the top and bottom
insets. Across the line it cannot be: FOP measures a block-container's children from its
*content* rectangle and their inherited `start-indent` is 0 there, so a `padding-left` was
thrown away - and `resetTextBox` writing an explicit `start-indent="0pt"` on that same
container made sure of it (XSL-FO 1.1 §5.3.2: an explicit `start-indent` *replaces*
"inherited + padding-start + border-start-width"). Measured against Word 365 on a timetable
laid out in five VML text boxes: Word's text starts at x=**86.4** with the shape clipped at
79.2 - the 7.2pt default inset - where ours started at 79.0, and the same **-7.4pt** on
boxes 4 (Word 506.8 / ours 499.3) and 5 (Word 642.4 / ours 634.9); it is 506.5 and 642.0
now. It is also why a *centred* line in such a box sat 7.2pt left of Word's - the content
rectangle was the right width but began in the wrong place. So across the line the
container's `width` is the shape less the **end** inset (which stays padding, and which FOP
does take off the measure), the **start** inset is `start-indent`, and `resetTextBox` no
longer overwrites either. The measure is unchanged: 297.9 - 7.2 - 7.2 = 283.5. A shape's
`strokeweight` is still uncounted, so a bordered box's text is up to half a stroke left of
Word's. 694 absolutely positioned containers carrying padding, in 40 documents of the three
corpora.

<a id="s92negx"></a>**A negative horizontal offset is honoured**, not clamped to the column
edge: Word draws such a box out into the margin. Measured on a landscape planner whose box
is anchored at -41.0pt, Word's box content rect is 31.0..1141.7 - its border rect starting
48.2pt left of the column - where ours started at 72.0, a constant **+41.0pt** on every line
of the page.

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

From 17.1.0 a WMF, EMF or EMF+ is not handed to FOP as a graphic at all: docx4j replays its
GDI records itself and puts the result in an `fo:instream-foreign-object` as SVG, which FOP
paints (CR-011). What follows is therefore the fallback, reached by a metafile the renderer
cannot parse and by bytes that are no image.

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

<a id="s95"></a>
### 9.5 `w:framePr`, Word's positioned text frames

A paragraph carrying `w:pPr/w:framePr` (ECMA-376 17.3.1.11) is not in the flow: Word puts
it in a box `w:w` wide at `w:x` / `w:y` measured from whatever `w:hAnchor` / `w:vAnchor`
name, and flows the body text past it. docx4j laid such a paragraph out where it fell.
53 documents of the three corpora carry 1,238 frames; it is the first divergence in four
of them and the whole of two.

**Frames anchored to the page or to the margin are positioned**, through the same
machinery an anchored picture uses (§9.1): a zero-height wrapper holding an
`fo:block-container` with `absolute-position="fixed"`, `left` and `top` in page
coordinates - `w:x` where `w:hAnchor="page"`, the text margin plus `w:x` otherwise; `w:y`
where `w:vAnchor="page"`, the top margin plus `w:y` for `w:vAnchor="margin"` - `width` from
`w:w` (the rest of the measure where it is 0 or absent) and `height` from `w:h` where
`w:hRule="exact"`. **Consecutive paragraphs carrying the same `w:framePr` are one frame**,
as Word treats them, and go into one container.

Measured against Word's own PDFs:

* `w:w=2926 w:h=748 w:hRule=exact w:vAnchor=page w:hAnchor=page w:x=8563 w:y=1702` on a
  68.05pt page margin: Word draws the frame's text at x=428.3 y=95.3, i.e. its box at
  428.15 / 85.1 from the page's top left corner - `w:x` and `w:y` exactly - where docx4j
  drew it in the flow at x=68.1 y=81.2.
* A document of 25 such frames went from 0.824 to 0.960 of Word's lines, and from 5 pages
  to Word's 4.

<a id="s95float"></a>**A `w:vAnchor="text"` frame is an `fo:float`.** Word draws it at its
anchor paragraph's own position offset by `w:x`/`w:y`, and flows the text that follows
*beside* it, `w:hSpace`/`w:vSpace` away - which is the float route §9.1 uses for a picture
Word wraps text around and §6.8 for a text-anchored floating table, with the same
construction: FOP gives a float the ipd of its content and ignores the padding of the
block inside it, so the float holds a **one-row table whose columns are the offset from
the column edge, the frame's `w:w`, and the gap to the text**, which reserves exactly the
band Word keeps clear and puts the frame at `w:x` within it. `w:y` is padding above the
frame inside the float and `w:vSpace` padding below it. FOP anchors a side float to a line
and drops one which has none, so the float goes inside the first block after the frame -
the paragraph that now begins where the framed paragraph was, which is what Word measures
`w:y` from.

Measured on a corpus cover whose page margins are all 0 and whose frame is
`w:framePr w:w=5281 w:hAnchor=text w:x=1441 w:y=3177` (264.05pt at 72.05pt into a 595.25pt
column, 158.85pt below the flow position): Word's text is at (72.0, 169.5), where docx4j
had it at (0.0, 10.5). The document went **0.727 to 0.864** of Word's lines.

**Left in the flow**, which is what every text-anchored frame did before 17.1.0:

* a frame with **no `w:w`** at all, which fills the rest of the measure, so nothing runs
  beside it. That is the shape of all 499 frames of the document that is this rule's acid
  test (0.954 of Word's lines in the flow, 0.573 in a container of its own width, 0.954
  still with this rule), and of 4 more frames in three other documents;
* one over **60%** of the column, where nothing useful fits beside it - §6.8's cut,
  measured there;
* `w:wrap="notBeside"` or `"none"`, which say no text may run beside the frame: in the
  flow the frame already reserves its own band (386 of the corpora's 1,238 frames);
* one whose band falls **outside the column** - measured, two corpus documents put a frame
  at `w:x=8545 w:w=2581` in a 9751-twip column and at `w:x=9633 w:w=2086` in a 10392-twip
  one, i.e. hanging into the right margin, which a float cannot do;
* one in a **table cell**, a header, a footer, a footnote or a multi-column region, and one
  inside a borders/shading container: FOP lays out no side float in any of those and paints
  nothing at all for one (§10). Nine frames of one corpus document are in cells;
* one which an `fo:block` inside an `fo:inline` **follows** - a line break inside a run -
  since that combination throws in FOP (§10) and a float holding a table cannot be hoisted
  out of the way;
* one with no following block for the float to anchor to.

Of the corpora's 716 text-anchored frames that may have text beside them, 41 in 14
documents state a `w:w` narrow enough to be floated at all, and 18 floats in one document
survive every test above. Over the three corpora (449 documents) one document changes and
none falls; the 60 probe goldens are unchanged.

**`w:dropCap`** is a frame of the same kind: the framed paragraph *is* the cap - Word puts
the paragraph's first character(s) in a frame of their own and runs the first `w:lines`
lines of the paragraph that follows beside it. Word writes the enlarged size into the run
itself (`w:sz`), so nothing computes a font size here; what is reproduced is the band,
which is `w:lines` lines of the following paragraph's pitch by the cap's own advance width
(measured from the physical font, as §4.4's tab widths are). `w:dropCap="drop"` sets the
cap into the text, so it is a float at the start edge; `"margin"` hangs it in the margin,
which is a positioned container at the column's left edge less the cap's width, taking no
space. **Not measured against a Word golden**: no document of the three corpora carries a
`w:dropCap` at all and no probe has a golden for one, so this is the rule as ECMA-376
17.3.1.11 states it and as Word's own markup implies, not a measurement. It changes no
corpus document.

`w:xAlign`/`w:yAlign` other than `left`/`top` (and `w:yAlign="inline"`, which keeps the
paragraph in the flow) and `w:anchorLock` are not implemented.

**The frame Word applies is the effective one.** A paragraph style may carry a
`w:framePr`, and each of its attributes inherits on its own: a paragraph stating only
`w:framePr w:w="3600"` keeps the anchors and the `w:x`/`w:y` its style gives. Measured on
a corpus letterhead whose `Adresse` style carries
`w:framePr w:w=3629 w:h=2427 w:vAnchor=page w:hAnchor=page w:x=1362 w:y=2042` while its
four address paragraphs carry only `w:framePr w:w=3600 w:wrap=notBeside`: Word draws them
as one frame at (68.1, 102.1) - the style's `w:x`/`w:y` - 180pt (the paragraph's `w:w`)
wide, its lines at y=112.3 / 125.1 / 137.6. Two core defects hid this: `StyleUtil`'s
`apply(CTFramePr, CTFramePr)` took `w:wrap`, the two anchors, the two aligns, `w:hRule`
and `w:dropCap` from the more specific frame *unconditionally*, clearing whatever the
style gave whenever the paragraph's frame omitted them; and `PropertyResolver`'s
`hasDirectPPrFormatting` did not count `w:framePr` at all, so a paragraph whose only
direct formatting was a frame never reached `applyPPr` and lost it.

**`w:wrap` decides whether the flow keeps the frame's band.** `notBeside` and `none` let
no text run beside the frame, so Word's flow steps over it and resumes below; `auto` (the
default), `around`, `tight` and `through` let text run beside it, and the flow keeps
nothing. The band is reproduced by leaving an invisible copy of the frame's own blocks
where they were (FOP honours `visibility="hidden"`: the area keeps its size and paints
nothing), which reserves exactly the height the frame occupies - `w:h` cannot, since
`w:hRule="auto"` is the common case. What Word skips is the **union** of the bands, not
the sum of the heights: the letterhead sets its frames out in pairs at `w:y=3743` and
`w:y=4821` and Word steps over each pair once, so a frame whose top is not below the last
one reserved in the same flow reserves nothing (reserving both put the body text 58pt too
low; reserving neither put it 125pt too high, against Word's first body line at y=310.2).
On that document the reservation is worth 0.548 -> 0.645 of Word's lines, which is where
the frames-in-the-flow layout it replaces already stood.

**On by default** since 17.1.0 (`docx4j.convert.out.fo.frames.position=false` turns it
off). Three corpus documents change and none falls: 0.804 to 0.873, 0.824 to 0.960 and
Word's page count, 0.840 to 0.848. The letterhead kept its 20 of Word's 31 lines either
way while its frames moved to where Word draws them - median dy 58.2 to 22.4 - and its
residual was its page-anchored table, which the floating-table pass then declined to
position because content precedes it; §6.8's reserved band takes that document to
**0.839**. The nesting defect 17.0.5 reported was the
borders/shading container:
`Containerization` builds it from its *first* paragraph's `pPr`, so the frame hint landed
on both the container and the paragraph inside it, the container was positioned and then
the paragraph was positioned again inside it, dragging the container's other, unframed,
paragraphs to the frame's x. The hint is no longer written on such a container.

---

## 10. Known FOP defects and limitations

Worked around here, and worth knowing about:

- **Letter-spacing is left out of the measured width** on FOP's complex-script text path
  (`GlyphMapping.processWordMapping`) while still being rendered, so expanded text
  overflows and condensed text stops short. The line manager restores Word's count
  (§4.6). Upstream report candidate.
- **The letter space is added to the word space twice.** `SpaceVal.makeWordSpacing` does
  `space.plus(letterSpacing.getSpace().mult(2))`, with a TODO saying it is not right, so
  every space in a letter-spaced run is measured one letter space too wide while
  `addMappingAreas` paints it one letter space narrower than it measured it. Measured: a
  2.75pt space in a run carrying `w:spacing w:val="19"` was laid out at 4.65pt where Word
  advances 3.87. The line manager corrects the glyph mapping (§4.6). Upstream report
  candidate.
- **A break before a solidus is prohibited.** FOP follows UAX #14 rule LB13 (no break
  before class SY); Word breaks there, so the space and the slash-led word after it were
  one unbreakable unit (§4.3). Worked around in the line manager.
- **`encoding-mode="single-byte"` misdescribes a CFF font.** It is what suppresses FOP's
  unconditional `liga`/`ccmp`/`locl` (§5.5), but FOP forces such a font to
  `FontType.TRUETYPE`: measured, declaring an `.otf` substitute this way does suppress the
  ligatures and does extract, but the PDF holds `/Subtype /TrueType` with the `OTTO` file
  in a `/FontFile2` stream, which only lenient readers draw. So a CFF-flavoured substitute
  (URW's Nimbus Sans Narrow for Arial Narrow, Source Sans 3 for Segoe UI Light) still gets
  FOP's ligatures, and its text layer carries U+FB01 or an unmapped private-use code
  point. Not worked around; the fix belongs upstream.
- **`initial-page-number` is clamped to 1.** XSL 1.1 §6.4.5 makes it a positive integer,
  so `<w:pgNumType w:start="0"/>` cannot be reproduced: the section's folios all run one
  too high and a `PAGE` field in it prints one too high with them. The `odd-or-even`
  master alternatives are swapped to compensate for the *parity* (§7), which is what
  costs whole-page geometry; the printed number is one character and stays wrong. Nothing
  in XSL-FO offsets `fo:page-number`, and the two-pass literal that carries `NUMPAGES`
  cannot carry `PAGE`, which differs on every page of a section.
- **`display-align` does not count the flow's last block's `space-after`**, which Word
  does (§7). `space-after.conditionality="retain"` on that block is the fix, and is what
  docx4j now writes; before it, zeroing a 10pt `space-after` on the last block of a
  centred flow moved the page not at all (first line y=281.7 before and after, against
  Word's 275.9), which had been read as evidence that Word did not count it either.
<a id="s10advances"></a>
- **Glyph advances are truncated to 1/1000 em, not rounded**, so every line FOP measures
  is up to about 0.1% narrow, and a line Word breaks by a hair is kept.
  `OpenFont.convertTTFUnit2PDFUnit` computes `(n / upem) * 1000 + ((n % upem) * 1000) /
  upem`, integer division throughout. Measured on 12pt Liberation Serif (2048 units per
  em): "incididunt ut labore et dolore" is 139.295pt by the font's own metrics and
  139.164 by FOP's, and a 74-character line is 376.559 against 376.284.

  **Word measures with the font's exact advances**, which is the measurement that settles
  it. Word's own PDF of `line-auto` writes rounded widths in `/Widths` (Liberation
  Serif's `e` is 443.85 units of 2048 and Word writes 444, `i` 277.83 and it writes 278,
  `L` 610.84 and it writes 611 - all of which FOP wrote one lower) and then corrects the
  accumulated position with `TJ` adjustments, so the glyph positions are recoverable:
  over the ragged lines of `line-auto` and `break-ragged`, Word's advance for a whole
  line tracks the exact metric sum to 0.003 - 0.13pt over 70 to 84 characters, where
  truncation is 0.31 - 0.39pt short of it. Rounding is unbiased where truncation loses
  half a unit per glyph.

  **Fixed since 17.1.0**, in both copies. docx4j's own copy of that code rounds, so
  `TextMeasurer` and the table autofit pass measure as Word does; FOP loads its fonts
  with its own copy, so `org.docx4j.fop.fonts.WordGlyphWidths` corrects the width table
  of each font FOP loads, from the same font file read through docx4j's
  (`org.docx4j.fonts.GlyphAdvances`). The correction goes in at the one place both the
  line measure and the `/Widths` the PDF renderer writes read from, so the text layer
  stays consistent with the glyph positions: after it, the `/Widths` docx4j writes for
  Liberation Serif are Word's own. `WordWidthsFontCollection` (a copy of FOP's
  `CustomFontCollection`) registers a `LazyFont` which applies it when the font is first
  loaded, so a font the document never uses is never read;
  `docx4j.convert.out.fo.glyphWidths.round=false` turns it off. A font embedded in the
  docx is corrected too. Upstream report candidate: one expression in `OpenFont`.
<a id="s10ansiwidths"></a>
- **A simple font's width table is built from the wrong encoding vector.**
  `OpenFont.initAnsiWidths` keys the 256-entry table off `Glyphs.WINANSI_ENCODING`, which is
  Adobe's *original PostScript* WinAnsi vector: its code 96 is `quoteleft` and its 0x98
  `asciitilde`, where the `/WinAnsiEncoding` FOP declares in the PDF - and which
  `CodePointMapping` uses to decide which code to emit, and which the viewer reads a code by
  - has `grave` and `tilde`. Because `initAnsiWidths` indexes by unicode, U+2018's advance
  lands at both 0x60 and 0x91 and U+0060's is never stored at all, so the viewer paints
  `grave` at `quoteleft`'s width. Measured over the 449 renders of the three corpora,
  **1403 of the 2514 embedded simple-font width arrays hold quoteleft's advance at code 96**
  (Carlito 42/1000 em out, Arimo 111, Caladea -70, DejaVu 183, Noto Sans 106) and nearly all
  hold asciitilde's at 0x98 (Arimo 251 out, Tinos 208, DejaVu 338, Caladea 399); Word's own
  PDFs write the real glyph's advance in both. Those two are the only live disagreements -
  the rest of the two tables agree, and the eight codes where they do not are codes nothing
  maps to. **Fixed since 17.1.0**: `WordGlyphWidths` writes the advance of the character the
  *declared* encoding gives each code, whether or not FOP's value is a truncation of it, for
  `WinAnsiEncoding`, `StandardEncoding` and `MacRomanEncoding`; a symbol-encoded or custom
  font keeps the truncation-only rule, since there docx4j's glyph lookup and FOP's may not
  agree on the glyph. What it costs on real documents is small: only 4 of the 449 paint a
  grave or a small tilde at all (25 and 23 occurrences), and there the line is 0.46 to 1.10pt
  out, up to 4.3% of a short line - enough to tip a knife-edge break, not enough to move a
  batch mean. What it fixes outright is the `/Widths` docx4j writes. Upstream report
  candidate, with the advance rounding above.

  What it moved: the aggregates rose on all three corpora (mean line parity 0.8781 ->
  0.8801, 0.8421 -> 0.8477 and 0.8738 -> 0.8802; lines matching Word exactly 86.6 ->
  86.8%, 76.3 -> 77.0% and 88.5 -> 90.0%), 37 documents improved and 5 fell, and
  §6.3's `MEASURE_GUARD_PT` went with it.
- **An infeasible `keep-with-next` chain overflows the page rather than breaking.** FOP's
  `BlockStackingLayoutManager.addInBetweenBreak` writes a keep as a penalty of
  `KnuthElement.INFINITE`, and the breaking algorithm has no rule for a run of them longer
  than a page: it puts the whole run on one page and lets it overrun (measured, 1,037 lines
  to y=7693.5 on a 792pt page). Word drops a keep it cannot satisfy. Worked around in
  docx4j's flow manager ([§3](#s39keepchain)). Upstream report candidate.
- **`fo:inline-container` is not implemented**, so there is no way to put a box of a given
  block-progression dimension in a line - which is what Word's legacy form-field checkbox
  is (a stroked square, [§7](#s7formcheckbox)).
- **A word has no intra-word break at all** (§4.3), so a token wider than the measure
  overruns the column instead of breaking where Word breaks it. Worked around by
  splitting such a word into per-character glyph mappings in the line manager.
- **An empty `fo:inline` carrying a `font-size` sizes the line.** FOP builds an empty
  inline area of that size and takes the line's height from it, so a bookmark anchor
  given a size of its own makes the line taller. Only elements that paint text are
  pinned (§2.4).
- **`padding-left` on a positioned `fo:block-container` does not offset its children**
  (`padding-right` is subtracted), and `end-indent` likewise, so a text box's start inset
  has to be `start-indent` and only its end inset can be padding (§9.2).
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
- **`fo:float` is not implemented inside a table.** FOP logs "the following feature isn't
  implemented by Apache FOP, yet: fo:float (on fo:table)" and paints nothing at all - not
  the float's content, not an indent - for a float anywhere under an `fo:table`, so a
  wrapped picture in a cell is positioned or reserved instead ([§9.1](#s91nofloat)).
  Hoisting a float to flow level (above) therefore never takes it out of a cell, a
  header/footer or a footnote: it would be painted somewhere else entirely.
- **A float holding a table renders nothing at flow level**, and a float with no line to
  anchor to is dropped: a float which is a direct child of `fo:flow` and holds an
  `fo:table` produces no area at all, silently (measured, same minimal case with a table in
  the float). So a floating table's float goes inside the paragraph it is anchored to, and
  a floating table which the defect above would move to flow level is left in the flow
  instead - losing the wrap rather than the table ([§6.8](#s69)).
- **A block-container in a multi-column flow** makes FOP throw when it balances the last
  page's columns, which is why merged sections carry their margin differences as indents
  ([§7](#s74)).
- **Space at a `span="all"` boundary is discarded**, on the spanning block as well as on
  the last block inside it, so the space-after Word draws where a one-column stretch runs
  into a continuous multi-column section is lost (measured, 10pt on one document, §7). The
  only lever XSL-FO offers is `space-after.conditionality="retain"`, which keeps the space
  at the foot of every page too, and costs more than it buys.
- **`advanced="false"` on a font declaration is parsed and then discarded** on the PDF path
  (`LazyFont`), so it cannot be used to switch OpenType features off. Reported upstream.
- **No per-run, per-script or per-feature GSUB switch**: the features are a private static
  list in `DefaultScriptProcessor`, and the renderer's complex-scripts option turns off
  Arabic and Indic shaping as well. Hence the `+noliga` declaration (§5.5).
- **`ToUnicode` maps one character per CID**, so a ligature glyph with no cmap entry cannot
  be mapped back to the characters it stands for.
- **`leader-alignment` is ignored** outside the RTF renderer, so a dot leader's dots start
  at the leader's own left edge rather than on the grid Word puts them on. docx4j writes the
  property and applies the phase itself, in the line manager ([§4.4](#s44phase)).

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
- **Page references in tabbed text**: FOP measures an unresolved
  `fo:page-number-citation` as an `MMM` placeholder, and its own redistribution can give
  the difference back only on a justified line. Worked around since 17.1.0 by moving the
  width to the tab when the citation resolves ([§4.4](#s44pageref)); upstream report
  candidate (the placeholder is a fixed private constant, and `resolveIDRef` notifies the
  line but nothing that could re-place the text).
- **Widow control across a `w:br`**: a `w:br` is a nested block, which ends FOP's line
  sequence, so a paragraph without `w:keepLines` can still be split there where Word's
  widow control would not ([§3](#s39)).
- **Space-after against a footnote area** is not yet applied as Word applies it
  ([§3](#s310)).
- **Floating tables** (`w:tblpPr`, [§6.8](#s69)): the horizontal position is always
  applied, and so is `tblpY` - the table lands within 0.2pt of Word's y on the
  `table-floating` probe ([§6.8](#s68tblpy)). What FOP will not do is leave the lines
  *above* the offset full width: it anchors a float to the line it sits at, where Word's
  frame starts `tblpY` below the top of the anchor paragraph, so those lines are narrowed
  where Word leaves them full (`tblpY` is within 15pt of zero for 103 of the 132
  text-anchored tables of the corpora which state one, and the probe's 72pt costs it two
  points of parity). Word runs text down both
  sides of a frame; an `fo:float` is single-sided.
  A table anchored to the **page or the margin box** is positioned only where it opens its
  section or opens a page and is narrow; positioned mid-page it would be drawn over the
  content Word puts below it, and mid-page Word wraps text beside it, which a positioned
  container cannot do. `topFromText`/`bottomFromText` are ignored.
- **Exact-height rows clip** their overflowing content rather than drawing it over the rows
  below ([§6](#s68)).
- **A picture's crop** (`a:srcRect`) is not applied: the picture is stretched into the
  frame the docx declares rather than cropped to it ([§9.1](#91-anchored-pictures)). The
  geometry is Word's, the content is not.
- **Pages carrying only a picture.** The eight documents a long-document corpus reported
  as differing only in page count were each checked for a page with nothing on it, with
  `mutool draw -F trace`: **not one page of either PDF, Word's or docx4j's, is empty** -
  every page the text-based triage called blank carries a picture. What those documents
  have is a picture drawn at the wrong size (fixed, [§9.1](#91-anchored-pictures)) or
  vertical drift accumulated further up, and no page-break rule was found to be wrong
  there. That is about **those eight documents**, and not a statement about Word: a wider
  sweep found Word emitting genuinely empty pages (no text, no image, no path) in two
  documents of a 103-document corpus - six in one of 56 pages and two in one of 22, none
  of them at a section end ([§3.3](#s33sect)), and none of which docx4j emits. What
  produces them is not yet pinned. The `page-blank` probe carries the shapes that could
  not be settled from the
  corpus - a document ending in a page break, a `nextPage` section break after a
  page-filling table, and `oddPage`/`evenPage` sections whose page already has the parity
  asked for - and awaits a Word golden. docx4j today emits a page for the trailing break
  and a filler for the parity break; whether Word does is what the probe measures.

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
| `docx4j:no-tab-hang-ind` | `fo:root` | `true` for `w:compat/w:noTabHangInd` (§4.4) |
| `docx4j:justify-soft-return` | paragraph block | its soft returns end a justified line ([§4.2](#s42shiftreturn)) |

`WordLayoutFixups` also stamps hints of its own, without a namespace prefix (Xalan drops
the declaration when it copies a fragment in the XSLT pathway), and strips every one of
them before the FO reaches FOP; two on an `fo:table` are `docx4j-content-sized` (the
columns came from the content-based autofit pass, §6.3) and `docx4j-grid-shift` (the cell
margin the mode-14 grid edge was moved back by, [§6.1](#s61nested)).
