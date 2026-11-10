# CR-080 phase 0: how Word draws DrawingML shapes in a PDF

Word 16.0.20326.20232 (Microsoft 365), through word-bridge, 2026-11-10. Four sets, each file staged once (Word was
deterministic over CR-081's 718 measured draws):
- `cr080-phase0`: 13 files, 1,284 probes. 11 ran. `fp60` opened but its save failed (§4), so `fp61` and `fp21` didn't run.
- `cr080-phase0-b`: the bisection, 11 files, 380 probes: `fp60` and `fp61`'s presets in quarters, `fp21`, and the
  suspects last.
- `cr080-phase0-c`: the strict pair for the failed save (WD-003).
- `cr080-phase0-r2`: 3 files, 36 probes: single shapes turned and flipped, groups, canvases.

The probes are `build_shapes.py`'s, listed in `probes.tsv`. Each shape is anchored to the page at (180 pt, 252 pt), with
`wp:wrapNone`, so its frame in the PDF is known exactly. `read_shapes.py`, over `pdfpaths.py` (CR-081's interpreter,
extended to keep each path's points and to resolve patterns), puts every draw in the shape's own frame: points from its
top left, y down, in points. The results are in `phase0-word.json` (each path's segments to 0.01 pt, its colours, line
and pattern), and in `phase0-word.tsv` (a line a probe). 1,319 probes are read.

## 1. What the PDF holds

| What | Drawn as |
|---|---|
| A preset or custom geometry | vector paths (`re`, `m`, `l`, `c`, `h`); arcs as cubic Béziers; fill and stroke as separate draws |
| `a:solidFill` | a fill in DeviceRGB; alpha through an ExtGState (`ca`) |
| `a:gradFill` | a shading pattern (type 2 axial, type 3 radial), its colours sampled into a type 0 function of 256 or 512 steps |
| `a:pattFill` (54 presets) | a tiling pattern: a 16 by 16 pixel image tile, 6 pt square on the page |
| `a:blipFill` stretched | the picture itself; tiled, a tiling pattern of a JPEG tile |
| a line | a stroke, with its width, cap, join and dash set in the content stream |
| a compound line (`dbl`, `thickThin`, `thinThick`, `tri`), or `algn="in"` | filled outlines, not strokes (as CR-081 found for a picture's compound frame) |
| a line with an arrowhead | the line and its head as one filled outline |
| a turned text box's text | glyph outlines, filled: not text, so not searchable |
| a turned, flipped or grouped text box | also an empty picture of the shape's size (200 dpi, every pixel transparent): an artefact |

## 2. The rules, measured

### Geometry

- Every preset and custom geometry draws where its definition puts it in the frame: `fp10` (100 by 100 pt) and `fp11`
  (144 by 72) for the presets at their defaults, `fp12` and `fp13` with their adjustments at half and double. The
  paths are kept for phase 1's test ("every preset's path within 0.1 pt of Word's"). A callout's leader reaches past
  the frame as its adjustments say (`fp10-001`: x = -38.33 pt for `adj4` -38333 at 100 pt).
- **Guides.** The built-in guides `wd32`, `wd12` and `hd10` are what their names say (w/32, w/12, h/10: `fp21`), though
  neither the standard's list nor POI's has `wd12` or `hd10`, and three presets use them undefined.
- **`sqrt` of a negative number** is sqrt(|x|): [MS-OI29500] 2.1.1311 holds for Word (`fp21-004`).
- **A shape with no `a:xfrm`** is drawn at its anchor's extent (`fp78`, `fp80`).

### Lines

- **Width.** As written, but width 0 is a 0.14 pt hairline (`fp30-001`).
- **Defaults.** With no `cap` stated the cap is flat, and with no join stated the join is round.
- **`a:miter lim` isn't passed on.** 800% and 100% both draw with the PDF's default limit of 10 (`fp30-052`, `-053`).
- **Dashes** are multiples of the line width (at width w):

  | prstDash | dot | dash | lgDash | dashDot | lgDashDot | lgDashDotDot | sysDash | sysDot | sysDashDot | sysDashDotDot |
  |---|---|---|---|---|---|---|---|---|---|---|
  | array (× w) | 1 3 | 4 3 | 8 3 | 4 3 1 3 | 8 3 1 3 | 8 3 1 3 1 3 | 3 1 | 1 1 | 3 1 1 1 | 3 1 1 1 1 1 |

  With a round cap, each dash is w shorter and each gap w longer (`dot` 0 4, `dash` 3 4), so the drawn dash, cap
  included, keeps its length. `custDash` is its `d` and `sp` as percentages of w.
- **Arrowheads.** Each size (`w` and `len` of `sm`, `med`, `lg`) is 2, 3 or 5 times the larger of the line width and
  2 pt. At 1 pt, 4, 6 and 10 pt; at 4.5 pt, 9, 13.5 and 22.5 pt (`fp31`). `triangle`, `stealth` and `arrow` end at
  the line's end; `diamond` and `oval` are centred on it.

### Fills

- **Fill modes**, on 4472C4 (`fp20-034`): `lighten` 8FAADC (40% toward white), `lightenLess` 698ED0 (20%), `darken`
  294476 (×0.6), `darkenLess` 375C9E (about ×0.81), `none` not drawn.
- **Gradients** are sampled, not given as stops (§1); phase 2 can write SVG gradients, which FOP turns into
  shadings, and compare colours.
- **Style references** (`fp50`, `fp51`): `fillRef` and `lnRef` resolve through the document's theme, with the
  reference's colour standing for `phClr`. The line widths differ by theme: `lnRef` 1 to 3 is 1, 1.5 and 2 pt under the
  2023 theme, 0.5, 1 and 1.5 pt under 2013. `fillRef` 2, 3 and 1003 are gradients, and 1001 and 1002 solid. An `a:ln`
  in the `spPr` with only a width keeps the style's colour (`fp50-012`). A fill or line in the `spPr` overrides the
  style's.

### Text

- **Text rectangles.** The glyph's box for every preset with its text at the top left and the bottom right is in
  `phase0-word.json` (`fp70` to `fp77`), for phase 1's test of each preset's text rectangle.
- **Word draws no text in 14 presets:** `line`, `lineInv`, `straightConnector1`, `bentConnector2` to `5`,
  `curvedConnector2` to `5`, `chartPlus`, `chartStar`, `chartX`. Its save turns `line` and the connectors into
  connectors (`wps:cNvCnPr`) and drops their text box. The others keep theirs, undrawn.
- **Turned text** (`rot` 30) is drawn turned, as glyph outlines (§1). With `upright="1"` it stays level, as text
  (`fp90-010`).
- **Flipped text** isn't mirrored: `flipH` draws the text where it would be unflipped (`fp90-011`).
- **A shape's outline insets its text**, apparently by half the line's width: the text of a group's child with a 1 pt
  line sits 0.48 pt further in than in a shape with no line (`fp91-015`). One probe; phase 2 confirms it.

### Groups and canvases

- **Groups** (`fp91`) place their children by `a:chOff` and `a:chExt` against the group's `a:off` and `a:ext`, as the
  standard says. A child space twice the group's extent halves the children, and a child offset moves them by minus it.
  Turns and flips compose down to three levels deep, a group's `solidFill` reaches a child's `a:grpFill`, and picture,
  text and connector children are drawn.
- **Canvases** (`fp92`) draw `wpc:bg` as a fill of the canvas's extent and `wpc:whole`'s line as its outline. The
  children are in the canvas's own coordinates, including a group and connectors attached to shapes.

## 3. Word's save

- Word keeps a shape without an `a:xfrm` without one (`fp78`, `fp80`).
- Word's save of `line` and the connector presets is a connector (§2, Text).

## 4. A defect: a shape with a text box and no `a:xfrm` can't be saved (WD-003)

Word opens such a file silently, but its Save As fails with "A file error has occurred", and no file is written.
`fp60` showed it. The bisection (`cr080-phase0-b`) left one suspect, and the strict pair (`cr080-phase0-c`: two
packages whose `word/document.xml` differ only by the `wps:txbx`) confirmed it. The bridge's later version (da85101)
presses OK, and Word's PDF export then succeeds: the text is drawn as it would be with an `a:xfrm`. It is the
portfolio's Word defects register's WD-003 (Jason: "the save failure is worth an entry"), with `fp81-pair-text.docx`
as its reproducer.

## 5. What this changes in the CR (for Jason)

Accepted 2026-11-10 (Jason: "CR-080 decisions as recommended"), and written into the CR's phases 1 and 2 and §8.

- **Phase 2: write an `a:xfrm`.** docx4j must write one in any `wps:spPr` that holds a text box, or Word can't save
  the file (§4). A shape read without one is drawn at its anchor's extent.
- **Phase 2: no text in the 14 presets** of §2. Word draws none there.
- **Phase 2: line rules from §2.**
  - the hairline at 0.14 pt;
  - flat caps and round joins by default;
  - no miter limit from `lim`;
  - the dash table;
  - the arrowhead sizes;
  - compound lines and inside lines as filled outlines.
- **Phase 2: the fill-mode factors and the per-theme line widths of §2**, through CR-034's theme reading.
- **Turned text.** Word turns it into outlines. Recommendation: docx4j keeps it as text, turned. It looks the same
  and stays searchable.
- **D7 (the oracle)** works as proposed. The paths are read as numbers by `pdfpaths.py`, so `mutool trace` isn't
  needed.
- **2.1.1311** holds for Word. **2.1.1315** can't be told apart for a rect, whose text rectangle is its box; the
  presets' text rectangles in `fp70` to `fp77` are what phase 1 tests.

## 6. Not yet done in phase 0

- The load survey's counts of shapes, through the portfolio.
- The licence notes for repackaging POI's geometry (`legals/NOTICE`), which D1's decision needs.
- Reading the numbers against an engine. Phase 1 compares POI's geometry with the paths in `phase0-word.json`.
- What isn't probed: VML shapes (D6, a later CR), wrapping (phase 4), and `behindDoc` stacking.
