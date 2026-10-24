# Run property probes and Word 365's saves (CR-060)

Each `<name>.docx` is a probe; `<name>-word365.docx` is Word 365's save of it. Rounds 1 to 4 were built by the
docx4j-portfolio session on 24 October 2026 (its scratch scripts `onoff/build.py`, `build2.py`, `build3.py`,
`build4.py`), rounds 5 to 7 by docx4j-25 the same day (`build5.py`, `build6.py`, `build7.py`, kept on the share); all were opened
and saved in Word 365 by Jason Harrop the same day (rounds 5 to 7 with the share's `PDFand` macro). Word's PDFs and screenshots, and the build notes, stay on the
share (`Office 2016/onoff-fun/`, its `README.txt`); the rules they established are CR-060 section 1
(`docs/developer/change-requests/CR-060-run-properties-read-as-word-reads-them.md`).

Every probe paragraph after the first is `label: MARKER (end)`, three runs, and only the MARKER run carries the
property tested. Word keeps the paragraphs one for one; it merges runs whose formatting is equal, so in a save the
marker can share a run with its label.

Word's save is the oracle: Word writes back only values it reads one way (an on value bare, an off value `0` or
dropped, a colour as upper-case hex, a repaired element dropped), and docx4j reads those right. So a marker's
reading in the probe must equal its reading in the save. Where the save keeps the probe's markup and docx4j reads
both alike but Word doesn't (a `w:u` with only a `w:color`), the tests state Word's reading outright. So do they
where Word's own save changes what Word drew (read from its PDF): `props-enum-styles` M6 and
`highlight-char-styles` H7, a direct highlight over a character style's, drawn but dropped by the save; and
`colour-elsewhere-styles` U4, `w:u w:color="auto"` over a style's red underline, drawn black but dropped.

| Round | Files | What |
|---|---|---|
| 1 | `onoff-valid`, `onoff-empty`, `onoff-space`, `onoff-TRUE`, `onoff-yes` | `w:b` with every `ST_OnOff` value, bare and absent; `""`, `" "`, `TRUE`, `yes` |
| 2 | `props-b-case`, `props-b-styles`, `props-toggles-1`, `props-toggles-2` | the off words in other cases and with spaces; off values over bold styles, the toggle; ten more on/off properties |
| 2 | `props-u-valid`, `props-u-case`, `props-u-other` | `w:u` bare, colour-only, valid and invalid values |
| 2 | `props-enum-missing`, `props-enum-other` | `highlight`, `vertAlign`, `em`, `color` with the value missing, in another case, or outside the enumeration |
| 3 | `colour-names-highlight`, `colour-names-css`, `colour-case`, `colour-hex` | `w:color` names, cases, hex spellings |
| 4 | `colour-table-grey`, `colour-table-other` | the gray/grey spellings; CSS 4, X11 and system names; the long HTML list |
| 5 | `props-u-styles`, `colour-styles`, `props-enum-styles`, `colour-elsewhere` | invalid, bare and colour-only `w:u`, invalid `w:color`, invalid `highlight`/`vertAlign`/`em` over styles stating them (Word repairs `props-enum-styles`); colour names in an underline colour, a fill and a border |
| 6 | `props-u-onoff-styles`, `colour-auto-styles`, `highlight-char-styles`, `colour-elsewhere-styles` | the on/off words as a `w:u` value over styles; `auto` in other spellings over a style's colour; a character style's highlight; invalid and named fill, border and underline colours over styles |
| 7 | `colour-pattern`, `colour-background-red`, `-navy`, `-f00`, `-hex` | a shading pattern's colour, direct and over a style; the page colour (`w:background`), one per file (CR-061 phase 3) |
