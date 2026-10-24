# CR-062: underline styles and colours, strike with underline, double strike and emphasis marks drawn

Status: ACCEPTED 2026-10-24 (Jason: "go ahead with the recommendations"): D1 to D5 as recommended (§6). Phases 1 and 2
done 2026-10-24 (§8); phase 0's Word run (round 8) awaits Jason. PROPOSED 2026-10-24, drafted by Claude Opus 5.5
(session docx4j-25) at Jason's word ("push then write the small CR"): the small CR CR-060's D6 named. Phases in §5.

## 0. Summary

CR-060 made docx4j read run properties as Word reads them, and named what it found docx4j doesn't draw (its D6):
every underline style is drawn as a single line, and emphasis marks (`w:em`) aren't drawn. Writing this CR found three
more of the same kind:

- **An underline's colour isn't drawn** (`w:u/@w:color`, `@w:themeColor`), though docx4j reads it (CR-034; CR-060).
- **A run both underlined and struck through loses its strike** in HTML and PDF: `Strike` and `Underline` both write
  `text-decoration`, the HTML writers keep the last of a CSS name, and the FO writer's second `setAttribute` replaces
  the first; `Underline` comes second.
- **A double strike (`w:dstrike`) isn't drawn at all**: `PropertyFactory` has it commented out, as `w:em`.

Fix: HTML draws them, through CSS's `text-decoration-line`, `-style`, `-color` and `-thickness` and `text-emphasis`;
PDF draws underline and strike together (XSL-FO's `text-decoration` takes both) and keeps its other limits, which are
FOP's (§3.3). Small.

## 1. Word's drawing

ECMA-376 `ST_Underline` has seventeen styles besides `none`: `single`, `words` (under words, not spaces), `double`,
`thick`, `dotted`, `dottedHeavy`, `dash`, `dashedHeavy`, `dashLong`, `dashLongHeavy`, `dotDash`, `dashDotHeavy`,
`dotDotDash`, `dashDotDotHeavy`, `wave`, `wavyHeavy`, `wavyDouble`; the line takes `w:u/@w:color` (or its theme
colour), else the text's colour. `ST_Em`: `dot` (a dot above, in Latin text), `comma`, `circle`, `underDot`.
`w:strike` is one line through, `w:dstrike` two. Word's PDFs of round 2 (`props-u-valid`: single, double, words,
wave, a red single) and rounds 5 and 6 (dot emphasis drawn above Latin text, over a style too) are measured; the rest
is Word's documented drawing, which phase 0 measures (D5).

## 2. docx4j today (5163c2986)

| Property | HTML | PDF |
|---|---|---|
| `w:u` any style | `text-decoration: underline` (single) | `text-decoration="underline"` (single) |
| `w:u/@w:color` | not drawn (the text's colour) | not drawn |
| `w:strike` with `w:u` | the underline only (the strike lost) | the underline only |
| `w:dstrike` | not drawn | not drawn |
| `w:em` | not drawn | not drawn |

The resolver has every value (`StyleUtil.apply(U, U)`, `WordColors.of(U)`); CR-060 made the readings right. The HTML
writers turn a toggled-off strike into `text-decoration-line` already, keeping an underline (`XsltHTMLFunctions.
toggleOffCss`, ea6501956).

## 3. Proposed approach

### 3.1 HTML: CSS text decoration

One declaration set per run, made from its `w:u`, `w:strike` and `w:dstrike` together, instead of each writing
`text-decoration` over the others:

- `text-decoration-line`: `underline` and/or `line-through`, or `none`.
- `text-decoration-style`, from the underline (D1): `single` `solid`; `double` `double`; `dotted` `dotted`; `dash`,
  `dashLong` and the dot-dash styles `dashed`; `wave` and `wavyDouble` `wavy`; `words` `solid` (CSS can't leave the
  spaces out; Safari's `text-decoration-skip: spaces` aside). A double strike without an underline: `double`.
- `text-decoration-thickness`: the heavy styles (`thick`, `dottedHeavy`, `dashedHeavy`, `dashLongHeavy`,
  `dashDotHeavy`, `dashDotDotHeavy`, `wavyHeavy`) `0.15em`, else `auto`.
- `text-decoration-color`: the underline's colour as Word draws it (`WordColors.of(U)`, its theme colour where it
  has one), else unset (the text's colour, as Word).

CSS draws every line of an element in one style and colour (D2): with an underline and a strike together, the
underline's style and colour are used for both, and a double strike beside an underline is drawn single.

### 3.2 HTML: emphasis marks

`text-emphasis-style` (D4): `dot` `filled dot`, `comma` `filled sesame`, `circle` `open circle`, `underDot` `filled
dot` with `text-emphasis-position: under right`; `none` `none`. A new property class (`Emphasis`) in
`PropertyFactory`, where `w:em` is commented out now.

### 3.3 PDF

`text-decoration` takes `underline` and `line-through` together (XSL-FO 1.1 §7.17.4 allows a list; FOP draws both,
to be confirmed in phase 0), so a struck and underlined run keeps both; a double strike is drawn single. XSL-FO has no
decoration style, colour or emphasis mark, and FOP no extension for them, so PDF keeps a single underline in the
text's colour and no emphasis marks (D3). That is FOP's limit, not routed around; drawing them would be FOP-fork work
(a `fox:` extension), its own CR if wanted.

### 3.4 What it doesn't change

Reading (CR-060). What a save writes (CR-061). A style class's underline that a run can't undo in HTML (CR-060 §14's
open item, its own work). Words-only underlining in HTML (D1).

## 4. What it affects

HTML output of runs with a styled or coloured underline, a strike beside an underline, a double strike, or an
emphasis mark: drawn as Word draws them, within CSS's limits. PDF output of a run both struck and underlined: both
lines. Nothing else; the ports don't render (core-ts's and python's HTML writers may want the same table later).

## 5. Phases

| Phase | What | Size |
|---|---|---|
| 0 | Round 8 probe (Jason's run): every underline style, black and coloured; strike, double strike, each with an underline; the four emphasis marks on Latin and East Asian text. Fixtures; red tests (the CSS each run gets; PDF's `text-decoration` tokens); FOP's drawing of `underline line-through` checked | 2 h + Jason's run |
| 1 | §3.1 and §3.2: the decoration and emphasis CSS (`Underline`, `Strike`, a `DoubleStrike` and an `Emphasis` property; the writers combine the decoration declarations); tests green | ½ day |
| 2 | §3.3: the FO writer's combined `text-decoration`; CHANGELOG; hand-offs (the ports, the portfolio) | 1 h |

## 6. Decisions (accepted as recommended, Jason, 2026-10-24)

| # | Decision | Recommendation |
|---|---|---|
| D1 | The CSS for each underline style (§3.1) | the table in §3.1: the nearest CSS style, heavy as thicker; `words` drawn solid (CSS can't skip the spaces in every browser) |
| D2 | Underline and strike together, where CSS has one style and colour per element | both lines drawn, in the underline's style and colour (a double strike beside an underline drawn single); nesting a span per line would be exact but changes the HTML's structure |
| D3 | PDF | draw underline and strike together; leave styles, colour and emphasis marks to a FOP-fork CR if wanted |
| D4 | Emphasis marks in CSS (§3.2) | the mapping in §3.2 (CSS's own marks nearest Word's), positioned above, `underDot` below |
| D5 | Measure first | yes: round 8 (phase 0) shows Word's drawing of the styles and marks this CR maps, cheaply |

## 7. Sources

- CR-060 D6 (the gaps named), §1 and §12 to §13 (Word's measured drawing), §14 (the HTML override gap, separate);
  CR-034 (underline colours resolved).
- Code: `model/properties/run/Underline.java`, `Strike.java`, `PropertyFactory.createProperties(OpcPackage, RPr)`
  (`w:dstrike`, `w:em` commented out), `HtmlCssHelper.createCss` (the last of each CSS name kept),
  `XsltHTMLFunctions.toggleOffCss`.
- ECMA-376 Part 1, `ST_Underline`, `ST_Em`; CSS Text Decoration Module Level 3 (`text-decoration-*`,
  `text-emphasis-*`); XSL-FO 1.1 §7.17.4 (`text-decoration`).

## 8. Record (2026-10-24, Claude Opus 5.5, session docx4j-25)

- Phase 0, in part: round 8 built (`build8.py` on the share, beside rounds 1 to 7: `underline-styles`,
  `strike-styles`, `emphasis-marks`), awaiting Jason's run. FOP's drawing of `text-decoration="underline
  line-through"` checked by a minimal FO file: both lines (§3.3 confirmed). `DecorationAndEmphasisTest` (core-tests)
  states the CSS each run gets per D1, D2 and D4 and the FO token list per D3; written with phase 1, not run red
  first.
- Phase 1: `org.docx4j.model.properties.run.TextDecoration` (one property for a run's `w:u`, `w:strike` and
  `w:dstrike`; CSS `text-decoration-line`, `-style`, `-thickness`, `-color`; FO `text-decoration` tokens) and
  `Emphasis` (`text-emphasis-style`, `-position`), in `PropertyFactory.createProperties` for a run's and a paragraph
  mark's properties, in place of `Strike` and `Underline` (which stay, for the CSS-to-docx direction); the commented-out
  `w:dstrike` and `w:em` lines went. A `w:u` stating only a colour writes `text-decoration-color` alone, which colours
  an underline a character style's class draws, as Word does (round 6, U3); found by `RunPropertiesWord365Test`.
  Checked in headless Chrome over round 8: each style, thickness and colour; strike and underline together; the
  marks above, `underDot` below.
- Phase 2: the FO writer's `text-decoration` comes from `TextDecoration` (`underline line-through` together).
- Open for round 8: Chrome breaks an underline at descenders (`text-decoration-skip-ink`, on by default); if Word
  draws through them, `text-decoration-skip-ink: none` matches it.
- Suites: core-tests 2080 run, 0 failures, 30 skipped; docx4j-export-fo-tests 858, 0, 9; docx4j-diffx-tests green.
