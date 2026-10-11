# CR-034: theme colours resolved - one resolver for DrawingML, WordprocessingML and SpreadsheetML colour references

Status: IN PROGRESS. Phase 1 DONE 2026-10-12 (Jason: "start CR-034 phase 1"): `org.docx4j.model.color`
built; against Office's readings every case of the probes is exact or within one unit but the nine
saturation-on-grey cases; over four corpora it returns Word's own value for 36,899 of 36,919 theme
attributes and the theme colour for the 20 stale ones (§11).  Phase 2 next.  Phase 0 DONE 2026-10-12
(Jason: "do Phase 0 of each CR"; his Office run the same day): every probe read (§11).  Word draws the theme reference wherever the written value disagrees;
DrawingML's tint, shade, inv, gamma and channel transforms act in linear RGB, hue, sat and lum in HSL
(saturation not clamped), gray is Rec.709 luma in sRGB, in document order, Word and PowerPoint
identical on all 403 cases; Excel's tint is Windows' integer HLS (HLSMAX 240) and its theme indexes
swap the first four slots; system colours are drawn as the machine's, not lastClr.  Phase 1 next.
Proposed 2026-10-11. Drafted from the charts plan
(`../docx4j-portfolio/docs/charts_plan.md`, phase 1, which made it its own CR) on Jason's
request ("please draft CR-034 (theme-colour resolver) and CR-035 (charts phases 0, 2, 3 and 4).
Do not implement anything yet."). Reviewed the same day by the docx4j-13 session (Claude Fable
5.1, at Jason's request): approved with three amendments, applied (style references resolved in
phase 1, §4.1; D4 named as a departure from the plan, below; the corpus scan committed in phase
0, §6). The decisions of §7 are open. Owner: Jason Harrop. Drafted with Claude Opus 5.5.

Scope: one resolver that turns any colour reference Office writes into the RGB value (and alpha)
Office draws: DrawingML's six colour kinds and their transforms, through the host's colour map
and theme; WordprocessingML's `themeColor`/`themeTint`/`themeShade` and `themeFill` attributes;
SpreadsheetML's `theme`/`tint`/`indexed` colours. Exposed through `PropertyResolver` for
WordprocessingML, and as API for charts (CR-035) and pptx. The FO and HTML outputs take a
WordprocessingML theme colour as Word draws it (§4.5). A shape's style references (`a:fillRef`,
`a:lnRef`, `a:fontRef`) resolve through the theme's format scheme where the entry is a solid
fill (§4.1). Not in scope: painting DrawingML fills and outlines in the FO output, which is
independent of themes (§7 D4); gradients, patterns and effects, which are the chart renderer's
(charts plan phase 7); rendering of cells in xlsx.

**A departure from the plan:** the plan's §6 says phase 1 "lets the PDF output paint text-box and
shape fills and outlines". This CR builds the resolver that painting needs but leaves the painting
to a follow-up CR (D4): the corpus's shapes use direct colours, which need no theme, and painting
is layout-fidelity work with its own gate.

## 0. Why

- **Charts.** A chart's colours are almost all theme references (`a:schemeClr accent1` with
  `lumMod`/`lumOff`), and Office draws them in the *host's* theme: the same chart XML came out
  `4472C4` in Word's PDF of the docx and `156082` in Excel's PDF of the embedded workbook
  (charts plan, finding 7). CR-035's read model reports series colours through this resolver.
- **pptx.** Slides are written in scheme colours throughout, through a colour map that the
  master sets and a layout or slide can override. Nothing in pptx can be drawn right without it.
- **WordprocessingML.** Word writes the resolved value beside every theme reference (§3), so the
  outputs mostly come out right by reading `w:val` - but where the two disagree, docx4j draws
  the value and Word may draw the theme (§2.3, measured in phase 0).
- **The ports.** docx4j-core-ts and docx4j-python treat docx4j as their oracle; the resolver is
  small, pure arithmetic, and the test vectors of §4.6 make it portable as a table.

## 1. What docx4j does today (read from the code, 2026-10-11)

No code in docx4j resolves a theme colour. Nothing reads a theme's `a:clrScheme` to produce a
colour; every output writes the hex value of `w:val`, `w:color` or `w:fill`; there is no HSL
conversion, no DrawingML transform and no SpreadsheetML tint arithmetic in any module.

- **WordprocessingML.** The generated `Color`, `CTShd`, `CTBorder` and `U` carry every theme
  attribute (`STThemeColor` has the 16 values of §2.3). `StyleUtil` is the only main code that
  reads them, to merge and compare, never for a value: `apply(CTShd)` (StyleUtil.java:2515),
  `apply(CTBorder)` (2550), `apply(U)` (2738) and `apply(Color)` (2801) carry them, and
  `isEmpty`/`areEqual` of `CTShd` and `CTBorder` include them. But `isEmpty(Color)` (1484) and
  `areEqual(Color)` (757) look at `w:val` alone, so a `w:color` stating only a theme colour
  counts as empty and is not carried by a merge (§4.2); `isEmpty(U)` (1451) and
  `areEqual(U)` (714) likewise ignore `w:themeColor`.
- **The property classes** (`org.docx4j.model.properties`), which write both FO and CSS, read no
  theme attribute: `FontColor` (92-108) writes `w:val` (`auto` as black); `PShading`, `RShading`
  and the cell `Shading` say "We just support fill color right now" and write `w:fill` only (a
  pattern's `w:color` is logged as a TODO); the border classes (`RBorder`, `AbstractPBorder`, the
  table `AbstractBorder`) write `w:color`; `Underline` ignores the underline's colour. The one
  colour arithmetic on this side is the table writer's pattern blend
  (`AbstractTableWriter.processAttributes` 1991-2051, `UnitsOfMeasurement.combineColors`), on
  the hex attributes. `PropertyResolver`, `ImmutablePropertyResolver` and the XSLTs have no
  colour logic.
- **`w:clrSchemeMapping`** is read nowhere (`CTSettings.getClrSchemeMapping()` is generated and
  unused).
- **DrawingML.** No main code reads `CTSchemeColor`, `CTSRgbColor`, `CTSystemColor`,
  `CTPresetColor`, `CTHslColor`, `CTScRgbColor` or a transform (each colour class has a
  `List<JAXBElement<?>> egColorTransform`). The FO output of text boxes reads a line's width only
  (`XsltFOFunctions.shapeLinePt`, 463-474; an `a:lnRef` with an index counts as 0.75pt, its
  colour unread) and writes no fill or stroke colour. pptx4j's SVG output hard-codes connectors
  `rgb(99,99,99)` (`SvgExporter` 552), carries no text colour (`TextStyles.getWmlRPr` 361-399:
  bold, italic, underline and size only) and draws no slide background.
- **Colour maps.** The generated `clrMap` of masters, `clrMapOvr` of slides, layouts and notes,
  and `CTChartSpace.clrMapOvr` are read by nothing. `JaxbPmlPart.COLOR_MAPPING` (61) is a
  constant used to create a new master.
- **Themes.** `ThemePart` (`org.docx4j.openpackaging.parts.ThemePart`) has font helpers only;
  the scheme is reached as `getJaxbElement().getThemeElements().getClrScheme()`, whose slots are
  `dml.CTColor`. The shortcuts: `MainDocumentPart.getThemePart()`,
  `MainPresentationPart.getThemePart()` (a slide master's own theme only through its
  relationships), `WorkbookPart.getThemePart()`. `ThemeOverridePart` has no helper and no
  shortcut. `createPackage` adds the default theme of §2.1; `PresentationMLPackage.createPackage`
  shares one theme part between the presentation and its master.
- **SpreadsheetML.** The generated `org.xlsx4j.sml.CTColor` (`auto`, `indexed`, `rgb`, `theme`,
  `tint`) and `CTIndexedColors` are read by no main code, and xlsx4j has no output that draws
  cells.
- **Nearest existing arithmetic:** the trimmed POI `org.docx4j.org.apache.poi.sl.draw.DrawPaint`
  keeps only the sRGB/linear pair (`RGB2SCRGB`, `SCRGB2RGB`), used by the EMF+ gradient blend.
- **Tests:** merge tests only (`StyleUtilMergeRulesTest` 231-255: a `themeFill` survives a direct
  `w:shd`; the underline colour inherits). Fixtures carry theme attributes
  (`NumberingSample1Test`, `styles-simple.xml`, `parts/settings.xml` with a
  `w:clrSchemeMapping`), and no test asserts a resolved colour.

**The ports** have no resolution either: docx4j-core-ts (`src/model/properties/catalogue.mts`)
and docx4j-python (`docx4j_py/model/property_catalogue.py`) mirror `StyleUtil`'s merge, and
their content APIs read `w:val` (python's python-docx facade reports a theme colour's name, not
its value).

**Apache POI** (`../poi`, trunk `ae62bb5116`) is the only neighbour with the arithmetic, and
departs from the spec where Office is unmeasured:
- `XSLFColor` and `DrawPaint.applyColorTransform` (DrawPaint.java:414-439) read one of each
  transform in a fixed order, not document order; apply `shade` and `tint` in linear RGB, then
  the `hue`/`sat`/`lum` mod-off pairs in HSL; and skip `comp`, `inv`, `gray`, `gamma` and the
  channel transforms. Its own TODO says the colours don't match PowerPoint's.
- `ThemesTable.getThemeColor` (140-169) swaps the first four SpreadsheetML indexes (§2.4).
- `ExtendedColor.applyTint` (168-176) applies an xlsx tint to each RGB channel, though its
  javadoc describes the spec's HLS rule.
- Nothing for WordprocessingML's `themeTint`/`themeShade`.

## 2. The rules

### 2.1 Which theme, which colour map

| Host | Theme | Colour map (`bg1`, `tx1`, `bg2`, `tx2` to a slot) |
|---|---|---|
| docx: body, headers, footers, notes, text boxes | the main document part's theme part | `w:settings/w:clrSchemeMapping` |
| docx: a chart | the document's theme (finding 7), unless the chart has a theme override part | the document's, then the chart's `c:clrMapOvr` |
| pptx: a slide | its master's theme part (each master has its own) | the master's `p:clrMap`, overridden by the layout's then the slide's `p:clrMapOvr` (`a:overrideClrMapping`; `a:masterClrMapping` keeps the master's) |
| pptx: notes, handouts | the notes or handout master's theme | that master's `p:clrMap` |
| pptx: a chart | the slide's theme, unless the chart has a theme override part | the slide's, then the chart's `c:clrMapOvr` |
| xlsx: cells | the workbook's theme part | none: SpreadsheetML indexes the scheme directly (§2.4) |
| xlsx: drawings and charts | the workbook's theme | the default (`bg1`=`lt1`, `tx1`=`dk1`, `bg2`=`lt2`, `tx2`=`dk2`), then the chart's `c:clrMapOvr` |

A package without a theme part gets the theme Office supplies, which docx4j already chooses for
fonts: `docx4j.fonts.defaultTheme`, with the three bundled themes `theme-2023.xml` (the default),
`theme-2013.xml` and `theme-2007.xml` (`Docx4jProperties.DefaultTheme`). The bundled 2007 theme is
the 2013 one with the major Latin face changed, so its colour scheme is 2013's (`accent1`
`4472C4`), not Office 2007's (`4F81BD`): §7 D2.

### 2.2 DrawingML

A colour is one of six kinds (ECMA-376 Part 1 §20.1.2.3): `a:srgbClr` (hex), `a:scrgbClr`
(linear RGB percentages), `a:hslClr`, `a:sysClr` (a system colour, with `lastClr` the value on
the machine that saved it), `a:prstClr` (a named colour) and `a:schemeClr` (a slot of the theme's
colour scheme through the colour map, or `phClr`, the placeholder a style reference supplies:
`a:fillRef`, `a:lnRef`, `a:effectRef`, `a:fontRef` carry the colour that replaces `phClr` in the
theme's format scheme). Any kind takes a list of transforms, applied in document order: `tint`,
`shade`, `comp`, `inv`, `gray`, `alpha`/`alphaOff`/`alphaMod`, `hue`/`hueOff`/`hueMod`,
`sat`/`satOff`/`satMod`, `lum`/`lumOff`/`lumMod`, `red`/`redOff`/`redMod` (and green, blue),
`gamma`, `invGamma`.

The spec gives each transform's meaning but not its colour space or rounding. Implementations
differ: `lumMod`/`lumOff` and `sat*`/`hue*` are HSL operations everywhere; POI applies `tint`
and `shade` in linear RGB, in a fixed order rather than the document's (§1). Office is the oracle:
phase 0 measures every transform from Word's and PowerPoint's PDFs (§5, P3), where a fill is a
`DeviceRGB` colour to read exactly.

**Style references.** A shape with a style (`wps:style` in a docx, `p:style` in a pptx, the
usual way PowerPoint colours a shape) names its fill, line, effect and font by index into the
theme's format scheme (`a:fmtScheme`), each reference carrying the colour that replaces `phClr`
in the entry: `a:fillRef idx` 0 is no fill, 1 to 999 the entries of `a:fillStyleLst`, 1001 and
up those of `a:bgFillStyleLst`; `a:lnRef idx` an entry of `a:lnStyleLst`; `a:fontRef idx`
`major` or `minor`. An entry may be a solid fill, a gradient, a pattern or a picture.

What Office writes most: its colour picker's tints and shades of the ten scheme slots, as
`lumMod`+`lumOff` pairs (`lumMod 20000 lumOff 80000` for "lighter 80%", `lumMod 75000` for
"darker 25%"); and chart styles' `lumMod`/`lumOff`/`shade`/`tint`/`satMod` on `accent1` to
`accent6` and `tx1` (charts plan, finding 2).

### 2.3 WordprocessingML

`w:color`, `w:u`, `w:shd` (pattern colour and fill), the border elements (`w:top` ... `w:bar`,
`w:insideH`/`w:insideV`, `w:tl2br`/`w:tr2bl`) and `w:background` carry `w:themeColor` (or
`w:themeFill`) with an optional `w:themeTint`/`w:themeShade` (or `w:themeFillTint`/
`w:themeFillShade`), a byte in hex: `99` is a tint of 0x99/0xFF = 60%. `text1`, `background1`,
`text2` and `background2` go through `w:clrSchemeMapping`; `dark1` ... `accent6`, `hyperlink` and
`followedHyperlink` name a slot directly.

Word writes the colour it computed beside the reference (`w:val`, `w:color`, `w:fill`). Over the
corpora (§3) that value is the theme slot exactly for a plain reference, and the HSL rule below
to within one unit per channel for a tint or shade:

- tint t: L' = L·t + (1 − t)
- shade t: L' = L·t

The same rule in RGB (each channel toward white or black) misses by more than one unit in 470
tints and 1,445 shades, and Windows' integer HLS (`ColorRGBToHLS`, HLSMAX 240 or 255) misses more
than float HSL does, so neither is Word's arithmetic. Word's rounding is not yet matched: float
HSL rounded is exact in 1,043 of 3,005 tints and 2,764 of 4,698 shades; truncated, 1,610 and
3,214; the rest are one unit off. Phase 1 finds the rounding, with these 7,700 values as the test.

Where the written value and the theme disagree (§3: 20 attributes in 7 documents, every one an
Office 2007 theme value in a document whose theme is newer), which does Word draw? Phase 0 reads
Word's goldens of those seven documents (P1).

### 2.4 SpreadsheetML

`CT_Color` (`x:color`, `x:fgColor`, `x:bgColor`, the font and border colours) is one of `rgb`
(ARGB hex), `theme` (an index into the scheme), `indexed` (the legacy 64-colour palette, or the
workbook's own `x:indexedColors`; 64 and 65 are the system foreground and background) or `auto`,
with an optional `tint` from −1 to 1 applied to the HLS luminance (ECMA-376 Part 1 §18.8.19):
L' = L·(1 + tint) when the tint is negative, L·(1 − tint) + (HLSMAX − HLSMAX·(1 − tint)) when
positive. POI applies the same formula to each RGB channel instead (§1), which gives different
colours; P4 measures which Excel does, and its rounding. The `theme` index does not follow the
scheme's element order: Excel reads 0 as `lt1` and 1 as `dk1`, 2 as `lt2` and 3 as `dk2` (POI's
`ThemesTable` swaps them too).

## 3. Corpus facts (the four real-document corpora, 561 docx, scanned 2026-10-11)

Measured with a scratch script, which phase 0 commits as a harness tool (§6); the corpora are
not redistributable, so only counts are recorded.

- **WordprocessingML theme attributes: 36,892, in 310 of the 561 documents.** By element:
  `w:color` in 266 documents; borders (`w:top` 121, `w:bottom` 133, `w:left`/`w:right` 104,
  `w:insideH` 80, `w:insideV` 82); `w:shd/@w:themeFill` in 120 documents (12,099 attributes);
  `w:u` 2; `w:background` 2. 29,129 are plain references, 3,026 tints and 4,737 shades. Every
  one of the 36,742 in documents with a theme part has a hex value beside it.
- **Plain:** of the 29,039 with a hex value, 29,027 are the theme's slot exactly. The other 12
  are Office 2007 values in documents with a newer theme (`accent1` written `4F81BD`;
  `hyperlink` written `0000FF` against `0563C1`).
- **Tints and shades:** the HSL rule of §2.3 to within one unit per channel in all 3,005 tints
  and all but 8 of the 4,698 shades. The 8 are Office 2007 values again (`365F91`, `243F60`,
  `17365D`: `accent1` and `text2` of the 2007 theme, shaded), so 20 attributes in 7 documents
  disagree with their theme, all for the same reason.
- 150 theme attributes are in documents with no theme part.  (Wrong, corrected 2026-10-12: those
  documents' main part is `word/document2.xml`, or the relationship's target is absolute, and the
  scan looked only for `word/_rels/document.xml.rels`; every document with a theme attribute has a
  theme part.  Corrected, the scan counts 36,919 attributes, 29,156 plain (29,144 exact, the same
  12 stale), the same tints and shades; §11's phase 1 record has the figures.)
- No document maps `w:clrSchemeMapping` other than the default.
- **DrawingML in the document parts** (body, headers, footers, notes; charts not counted): 60
  `a:schemeClr` in 7 documents, 260 `a:srgbClr` in 58, 11 `a:prstClr` in 6; transforms:
  `alpha` 55, `lumMod` 4, `tint`, `satMod`, `shade` one each. So in docx the resolver matters
  for WordprocessingML and charts; drawing shapes mostly use direct colours, and what keeps
  their fills out of the PDF is that the FO output paints none (§7 D4), not the theme.

## 4. Design

### 4.1 Package and classes

A new package `org.docx4j.model.color` in docx4j-core (the codebase's spelling: `FontColor`,
`HighlightColor`), with no dependency beyond the JAXB classes:

- **`ResolvedColor`**: immutable red, green, blue (0-255) and alpha (0-100000, OOXML's unit);
  `toHex()`, `isOpaque()`. Not `java.awt.Color` (§7 D5).
- **`ThemeColors`**: a colour scheme and a colour map, which is all a reference needs. Made by
  `ThemeColors.of(...)` for each host of §2.1: a `WordprocessingMLPackage` (its theme part, or the
  default theme, and `w:clrSchemeMapping`); a slide, layout, master or notes part (the master's
  theme and the `p:clrMap`/`p:clrMapOvr` chain); a `SpreadsheetMLPackage`; and
  `forChart(Chart, ThemeColors host)` for a chart (its theme override part and `c:clrMapOvr`
  over the host's). `withPlaceholder(ResolvedColor)` supplies `phClr`.
- **`DrawingMLColors.resolve(colour, ThemeColors)`**: any of the six kinds with its transforms.
- **`DrawingMLColors.resolveStyle(reference, ThemeColors)`**: a style reference's entry in the
  format scheme, with `phClr` replaced by the reference's colour. A solid fill (or a line with
  one) resolves to its colour; a gradient, pattern or picture entry is reported as "not solid",
  with its kind, and not guessed at: drawing it is the renderer's.
- **`WordColors`**: a WordprocessingML colour from its attributes (value, theme colour, tint,
  shade), by Word's precedence as phase 0 measures it.
- **`SpreadsheetColors`**: a `CT_Color` with the workbook's scheme and `x:indexedColors`.
- HSL and linear-RGB arithmetic package-private, with the rounding phase 0 and 1 settle.

### 4.2 Exposure through `PropertyResolver`

`PropertyResolver` gets the document's `ThemeColors` (made once, lazily) and a method returning
the effective colour of a run, a shading or a border from its resolved properties. The property
classes that write colours (`FontColor`, `RShading`, `PShading`, the border classes, `Underline`)
call it where a theme attribute is present, instead of reading `w:val` alone. The change to
`PropertyResolver` is an accessor; the resolution rules stay where they are.

With it, `StyleUtil`'s `isEmpty(Color)`/`areEqual(Color)` and `isEmpty(U)`/`areEqual(U)` count
the theme attributes, as the `CTShd` and `CTBorder` ones already do (§1), so that a style's
`w:color w:themeColor="accent1"` with no usable `w:val` is inherited instead of dropped. This
changes what the merge carries, so it goes through phase 2's gate.

### 4.3 Themeless packages

As §2.1: the bundled theme `docx4j.fonts.defaultTheme` names. The property keeps its name (it
already chooses the theme part `createPackage` writes, not only fonts), documented as choosing
colours too.

### 4.4 System colours

`a:sysClr` resolves to the Windows default for its name (`windowText` black, `window` white, and
so on), not to `lastClr`.  (Drafted the other way round, `lastClr` first; P3 showed Office draws
the machine's colour and ignores `lastClr`, §11, 2026-10-12.)

### 4.5 What the outputs gain in this CR

- FO and HTML: a WordprocessingML theme colour drawn as Word draws it. If phase 0 finds Word
  draws `w:val` when the two disagree, nothing changes in the output and §4.2 serves the API only.
- No DrawingML painting (§7 D4), no SpreadsheetML output (docx4j renders no cells).
- API for CR-035's read model and, later, the chart renderer and pptx output.

### 4.6 Test vectors for the ports

A resource in docx4j-core-tests, `theme-colors/vectors.tsv`: one line per case (host kind, the
reference as written, the scheme and map it resolves against, the expected hex and alpha), from
the phase 0 probes and from the distinct (base colour, tint or shade, Word's value) triples of
§3, which carry no document content. The Java tests read it; core-ts and python read the same
file, as they do the strict fixtures.

## 5. Phase 0: probes (one run each of Word, PowerPoint and Excel)

Built by docx4j code in the fidelity harness (as CR-030's were), on the share
(`Office 2016/fidelity/theme-colours/`), with Office's PDFs read by a script that lists each
fill's `DeviceRGB` value from `mutool trace`:

- **P1 Precedence (Word):** runs, shading and borders whose `w:val` disagrees with their
  `w:themeColor`, with and without a tint; `w:val="auto"` with a theme colour. And, without a
  new run, Word's goldens of the 7 corpus documents of §3.
- **P2 Colour map (Word):** `w:clrSchemeMapping` with `bg1`↔`t1` swapped: runs in `text1` and
  `background1`, and shapes filled `tx1` and `bg1`. Whether the mapping applies to both.
- **P3 Transforms (Word and PowerPoint):** a grid of rectangles, each `accent1` ... `accent6`,
  `tx1`, `bg2` with one transform at three values (each transform of §2.2), the picker's pairs,
  and orders that differ (`tint` then `lumMod` against `lumMod` then `tint`); a `sysClr` whose
  `lastClr` is not the machine's colour; a shape with only a style reference (`phClr`).
- **P4 SpreadsheetML (Excel):** cells filled with theme 0 to 9 at tints −0.5, −0.25, −0.1, 0,
  0.2, 0.3333, 0.4, 0.6, 0.8; indexed 0 to 65 with and without a custom `x:indexedColors`.
- **P5 pptx map override (PowerPoint):** a slide with `a:overrideClrMapping` (dark background)
  and a layout with one, text and shapes in `tx1`/`bg1`.

## 6. Phases

| Phase | What | Gate | Effort |
|---|---|---|---|
| 0 | The probes of §5; Jason's Office run; readings recorded here. Word's goldens of the 7 corpus documents read. The corpus scan behind §3 committed as a harness tool (`docx4j-layout-fidelity/tools/`; counts and the distinct colour triples only, nothing else from a document), so phase 1's gate has its instrument. | Every probe read; P1's answer decides §4.5; the tool reproduces §3's counts | 1-1.5 days + the run |
| 1 | `org.docx4j.model.color` (§4.1), the arithmetic as phase 0 measured it, and style references; unit tests from the probes; phase 0's tool extended to compare: every WordprocessingML theme attribute in the four corpora resolved and compared with Word's value; the test vectors (§4.6). | All probe readings reproduced (P3's style-reference shape included); the corpus check exact (or each residual explained) | 2.5-3.5 days |
| 2 | §4.2 and §4.5: `PropertyResolver`, the property classes, `StyleUtil`'s `Color` and `U` comparisons. | FO and HTML of the four corpora: the documents that change are exactly those whose values disagree with their theme (none, if Word draws `w:val`); docx4j-core-tests and export-fo-tests green | 1 day |
| 3 | Documentation (the `PropertyResolver` javadoc, the colour package's), CHANGELOG, hand-offs (§9), registry. | | 0.5 day |

## 7. Decisions for Jason

- **D1. Package and names:** `org.docx4j.model.color`, the classes of §4.1. Recommendation: yes.
- **D2. Themeless colours:** the bundled theme `docx4j.fonts.defaultTheme` names (2023 by
  default, as Word 365 does). Its `2007` value gives the 2013 colour scheme (§2.1). Options: (a)
  accept and document it; (b) give `theme-2007.xml` Office 2007's colour scheme, which also
  changes the theme part `createPackage` writes under that setting. Recommendation: (a); the
  setting exists for fonts, and nobody has asked for 2007 colours.
- **D3. Word's precedence:** follow what P1 measures, even though it changes the output for the
  7 corpus documents. Recommendation: yes.
- **D4. DrawingML fills and outlines in the FO output:** not in this CR. The FO output paints no
  DrawingML fill or line today, of any colour kind; in the corpora those shapes use direct
  colours (§3), so painting them is layout-fidelity work with its own gate, which this resolver
  then serves. Recommendation: a follow-up CR, entered in the fidelity register.
- **D5. The colour type:** docx4j's own `ResolvedColor` rather than `java.awt.Color`: it keeps
  OOXML's alpha unit, keeps AWT out of the model, and the ports mirror it. Recommendation: yes.
- **D6. Rounding:** each host's as measured (Word's, PowerPoint's and Excel's may differ), not one
  rounding for all. Recommendation: yes.

## 8. Risks

- **The exact rounding may not be found.** Within one unit is invisible in output, but the ports'
  parity tests need an exact rule; if none reproduces all of §3, the vectors carry a tolerance.
- **Hosts may differ.** Word, PowerPoint and Excel may apply a transform differently; P3 covers
  Word and PowerPoint, P4 Excel's own colours. Charts in Excel are not probed separately.
- **`PropertyResolver` is widely shared.** The change is an accessor plus the property classes'
  colour, gated by the corpus diff of phase 2.

## 9. Hand-offs

- **CR-035** (charts): phase 2's read model reports colours through `DrawingMLColors`.
- **The charts plan:** its §6 sentence on painting fills, aligned with D4 (the reviewing session
  undertook to change it).
- **docx4j-core-ts, docx4j-python:** port the resolver and read the vectors (§4.6).
- **docx4j-mcp:** effective colours in its content description, if it reports colours.
- **Enterprise fidelity register:** D4's follow-up, as a cause (DrawingML fills not painted).

## 10. Effort (rough)

5-6.5 working days, plus one Office run (Word, PowerPoint, Excel) for phase 0. The charts plan
estimated 4-6; style references (the review's first amendment) add half a day.

## 11. Implementation record

### Phase 0 (2026-10-12), done

**The corpus scan** is `docx4j-layout-fidelity/tools/theme_colour_scan.py` (counts only; with
`--triples` the distinct (theme value, op, byte, Word's value), with `--stale` the attributes
that disagree with their theme).  Over the four corpora it reproduces §3 exactly: 29,027 of
29,039 plain references exact; 3,005 tints and 4,690 of 4,698 shades within one unit; float HSL
exact in 1,043 / 2,764 rounded and 1,610 / 3,214 truncated.  117 distinct triples, the core of
§4.6's vectors.

**P1 from the corpus goldens.** All 20 disagreeing attributes are in style definitions
(`styles.xml`), not in runs; 13 of the styles are used by no paragraph in their documents. One
gives an answer: in 11782 the TOC Heading style has `accent1` shaded `BF` with `365F91` written
beside it (Office 2007's `accent1`, shaded), and Word drew its paragraph ("Contents") in
`2E74B5`, which is the document's own `accent1` (`5B9BD5`) shaded `BF`.  So Word draws the theme
reference, and the written value is a cache.  The Hyperlink style's three samples are TOC
entries, which Word drew black, so they say nothing about precedence.  P1's probe settles runs,
shading, borders and the underline.

**The probes** are `org.docx4j.fidelity.corpus.ThemeColourProbes` in the harness: 1,068 labelled
cases in seven files, all in the Office 2023 theme.

| Probe | Cases |
|---|---|
| `tc-p1-precedence.docx` | 17: eight runs, four paragraph shadings (one a 25% pattern), three top borders (each after an unbordered paragraph, since Word joins adjacent paragraphs with equal borders into one box), an underline, a run shading |
| `tc-p2-colour-map.docx` | 13: six runs and seven shapes under a `w:clrSchemeMapping` with the light and dark slots swapped |
| `tc-p3-transforms.docx`, `.pptx` | 403 each: five bases (`accent1`, `accent2`, `accent4`, `tx2`, `bg2`) with each of the 28 transforms at up to three values (330), Office's picker pairs on all ten slots (50), transforms in both orders and the theme's and chart styles' combinations (10), the other colour kinds including two `sysClr` whose `lastClr` is not the machine's colour (9), four shapes coloured only by a style reference |
| `tc-p4-spreadsheet.xlsx` | 156: theme 0 to 9 at nine tints, indexed 0 to 65 |
| `tc-p4-spreadsheet-palette.xlsx` | 66: indexed 0 to 65 against a custom `x:indexedColors` (a hue wheel) |
| `tc-p5-map-override.pptx` | 10: five shapes on a slide overriding the master's colour map, five on a slide keeping it |

A departure from §5: P5's override is on a slide only.  A second layout with its own override
needs a layout part docx4j's `createPackage` does not make; it can follow if the slide case
shows the override applies.

**Reading.** `tools/office_colours.py` matches each label to the fill it sits in (and the
outline around it, its own colour, and thin rules near it) in `mutool trace`'s output; Word's
PDFs give colours to three decimals, enough to recover each 8-bit value.  Checked on docx4j's
own renders of the three Word probes: every label found; text, shading, the borders and the
underline read as docx4j wrote them (docx4j paints no shape fill, §1).

**On the share**, `Office 2016/fidelity/theme-colours/` (`X:\fidelity\theme-colours`): the seven
probes, `cases.tsv`, `office-colours.ps1` (opens each probe in its own application, exports a
PDF to `goldens\`) and `RUN.txt`.

**Office's run** (Jason, 2026-10-12): `office-colours.ps1` on the VM, after two fixes to it:
Word's `ExportAsFixedFormat` stalled, so Word now uses documents4j's route (`Documents.Open` with
four arguments, `SaveAs` format 17), each file copied to a local temp folder first; and the PDFs
are named after the whole file name, since the docx and pptx of P3 share a base name.  All 1,068
labels were found in the seven PDFs.

#### Readings

**P1, precedence (Word).** Word draws the theme reference in every case and ignores the value
written beside it, `FF0000`, `00FF00`, `auto` or Office 2007's `4F81BD` alike: runs (`accent1`
`156082`; tint `99` `45B0E1`; shade `BF` `0F4761`; `text1` `000000`; `hyperlink` `467886`),
paragraph shading (`accent2` `E97132`, tint `66` `F6C5AC`), the 25% pattern (`E7CAE4`: a 25/75
blend of `accent5` and white, in sRGB), top borders (`accent6` `4EA72E`, shade `80` `275317`), the
underline (`accent4` `0F9ED5`), run shading (`accent3` `196B24`).  With 11782's TOC Heading from
the corpus, that settles §2.3's question and D3: the written value is a cache.

**P2, colour map (Word).** The swapped `w:clrSchemeMapping` applies to WordprocessingML theme
colours (`text1` drawn `FFFFFF`, `background1` `000000`, `text2` `E8E8E8`, `background2`
`0E2841`) and to DrawingML scheme colours alike (`tx1` filled `FFFFFF`, `bg1` `000000`, `tx2`
`E8E8E8`, `bg2` `0E2841`); direct slots (`dark1`, `dk1`, `lt1`, `accent1`) are untouched.
Automatic text (no `w:color`) stays black: "auto" does not follow `text1`.

**P3, DrawingML transforms (Word and PowerPoint).** Word's and PowerPoint's readings are identical
on all 403 cases.  A model fits nearly all of them exactly, applying each transform in document
order (each order case differs from its reverse, as the model says):
- `tint` t: linear RGB, c' = 1 − (1 − c)·t; `shade` t: c' = c·t; `inv`: c' = 1 − c; `red`, `green`,
  `blue` and their `Off`/`Mod`: the channel in linear RGB; `gamma`: the colour taken as linear and
  written as sRGB; `invGamma`: the reverse.  (sRGB's transfer function, IEC 61966-2-1.)  Exact in
  every case: 16 tints, 16 shades, 5 inv, 90 channel cases, 10 gamma.  So DrawingML's tint is not
  WordprocessingML's themeTint, which is HSL (§2.3).
- `hue`, `hueOff`, `hueMod`, `lum`, `lumOff`, `lumMod`, `comp` (hue + 180°): HSL of the sRGB
  colour.  Exact in 77 of 85, the rest within one unit; the picker pairs 46 of 50, the rest within
  one.
- `sat`, `satOff`, `satMod`: HSL, with saturation **not clamped** (to 0 or 1) before converting
  back; the channels are clamped after.  33 of 45 exact; the misses are one unit off, or on the
  grey `bg2` (`E8E8E8`), where Office gives a grey's undefined hue about 40° and a negative
  saturation flips it.  Left to phase 1.
- `gray`: Rec.709 luma of the sRGB channels, 0.2126 R + 0.7152 G + 0.0722 B: 5 of 5.
- `alpha`, `alphaOff`, `alphaMod`: as written, 35 of 35.
- Other kinds: `srgbClr` with a tint (linear, as above), `scrgbClr` (linear percentages: 50/25/75%
  is `BC89E1`), `hslClr`, `prstClr` (`coral` `FF7F50`), `hlink`, `folHlink`: exact.  **`sysClr` is
  drawn as the machine's colour, not `lastClr`**: `windowText` with `lastClr` `FF0000` came out
  black, `window` with `00FF00` white.  §4.4 assumed the opposite; the resolver should take the
  Windows default for the named colour and ignore `lastClr`.
- Style references: Word's default shape style gave fill `accent1` (`fillRef 1`), line `accent1`
  shaded 50% (`0C445E`, the same as a shade on its own) and white text (`fontRef` `lt1`);
  `fillRef 1001` took the background fill list (`A02B93`); `fillRef 2`, a gradient in the 2023
  theme, is not a solid fill (as §4.1 has it).

POI got the colour spaces of tint and shade right (linear) but applies transforms in a fixed
order; LibreOffice the same spaces.

**P4, SpreadsheetML (Excel).** Excel swaps the first theme indexes as POI does: theme 0 is `lt1`,
1 `dk1`, 2 `lt2`, 3 `dk2`.  Its tint is HLS, not POI's per-channel RGB (`dk2` at +0.2: Excel
`1D5287`, HLS `1D5488`, POI's rule `3E5367`), and in Windows' integer arithmetic (`ColorRGBToHLS`,
HLSMAX 240), the luminance truncated and a grey's channel rounded: 82 of 90; leaving tint 0 alone
(Excel does) makes it 88, the last two both at tint 0.3333.  So Word's themeTint (float HSL, §2.3)
and Excel's tint are different arithmetic: D6's answer is per host.  Indexed colours: the legacy
64-colour palette, 64 and 65 drawn black and white; a workbook's `x:indexedColors` replaces all 64
entries (each of the 64 custom ones reproduced).

**P5, PowerPoint's colour map override.** The slide's `a:overrideClrMapping` applies to fills
(`tx1` `FFFFFF`, `bg1` `000000`, `tx2` `E8E8E8`, `bg2` `0E2841`) and to the slide's own text colour
(white); the slide keeping the master's map draws the master's.

**What phase 1 builds on:** §4.1 as written, with the spaces above; `sysClr` by name; WordprocessingML
by the theme reference first; Excel's integer HLS.  The readings become test vectors (§4.6) with
the corpus triples.

### Phase 1 (2026-10-12), done

**MS-OI29500 first** (plan §11): its notes on §17.3.2.6 (`w:color`, 2.1.72) give Word's
themeTint and themeShade exactly as §2.3 measured (HSL luminance, L·t + 1 − t and L·t), and add
that **where both are present Word applies the tint**; its notes on the DrawingML transforms say
Office reads a percentage as "50%" or as 50000 and writes only the latter, that `prstClr` needs its
`val`, and that `lumOff` and `satOff` are "not supported by Office and may produce unexpected
color" - which Office 365 contradicts: it applied both, as HSL offsets, in every probe case.

**`org.docx4j.model.color`** (docx4j-core, exported), as §4.1 with the readings of phase 0:
- `ResolvedColor`: red, green, blue and an OOXML alpha; immutable.
- `ThemeColors`: a scheme and a map; `of(WordprocessingMLPackage)` (the main document's theme or
  the default theme, `w:clrSchemeMapping`), `of(SpreadsheetMLPackage)`, `of(Part)` for a slide
  (master's theme, master's `p:clrMap`, layout's then slide's `p:clrMapOvr`), layout, master, notes
  slide, notes or handout master; `forChart(Chart)` (a theme override part, `c:clrMapOvr`);
  `withPlaceholder` for `phClr`; `schemeColor`, `wordThemeColor`, `sheetThemeColor` (Excel's
  index swap).
- `DrawingMLColors`: the six kinds, the transforms in document order in the spaces of §11; the
  fill, line and font style references (`fillRef` 0 / 1-999 / 1001+, `lnRef`, `fontRef`), a
  non-solid entry reported as `StyleFill` of its kind.
- `WordColors`: the theme reference over the written value, tint before shade; **Word's value
  where it is a current cache** (within one unit of the computation), so every attribute Word
  wrote against the document's theme comes back exactly, whatever Word's rounding; the
  computation (float HSL, the channels truncated, which gives Word 365's drawing in the probe's
  three run cases exactly) where the value is stale or absent.
- `SpreadsheetColors`: rgb, theme, indexed (Excel's palette as measured, or the workbook's
  `x:indexedColors`), tint in Windows' integer HLS, the luminance truncated after adding 0.02:
  all 90 tint cells exact (plain truncation missed two at tint 0.3333, which Excel rounds up).
- `ColorTables`: the 140 preset and 30 system colours (POI's `PresetColor`, Apache-2.0) and the
  indexed palette.

**Against Office** (`ThemeColourProbesTest`, every probe case resolved through its own file):
P2, P4 (222 cells) and P5 exact; P1 exact but `P1-10` (a shading's themeFillTint with an
unusable value, one unit); P3 in Word and in PowerPoint exact or within one unit (hue, luminance
and picker cases) but the nine saturation transforms on the grey `bg2`, which stay a known
difference (Office's grey hue).  `ThemeColorsTest`: a new docx has the 2023 theme, a themeless
package the default theme (and `2013` gives `4472C4`, D2), the Excel index swap, a chart in its
host's theme.

**The corpus check** (`org.docx4j.fidelity.corpus.ThemeColourCheck`, the four corpora, 560
documents; one more exceeds docx4j's zip-bomb limit): 36,919 theme attributes.  The computation
alone: plain 29,144 of 29,156 exact (12 stale), shades 3,245 exact and 1,484 within one of 4,737
(8 stale), tints 1,631 exact and 1,395 within one of 3,026.  What `WordColors.resolve` returns:
Word's own value for 36,899; for the 20 stale ones the theme colour, which is what Word draws
(P1, and 11782's TOC Heading).

**Vectors for the ports** (§4.6): `docx4j-core-tests/src/test/resources/theme-colors/`, the seven
probes, `cases.tsv`, Office's `readings.tsv` and the corpus `word-tint-shade.tsv`, with a README.

Decisions taken in building it: D1, D5 and D6 as recommended; D2 (a), the default theme as is.
D3 and D4 are phase 2's.
