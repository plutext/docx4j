# Run property probes and Word 365's saves (CR-060)

Each `<name>.docx` is a probe; `<name>-word365.docx` is Word 365's save of it. Built by the docx4j-portfolio
session on 24 October 2026 (its scratch scripts `onoff/build.py`, `build2.py`, `build3.py`, `build4.py`), opened
and saved in Word 365 by Jason Harrop the same day. Word's PDFs and screenshots, and the build notes, stay on the
share (`Office 2016/onoff-fun/`, its `README.txt`); the rules they established are CR-060 section 1
(`docs/developer/change-requests/CR-060-run-properties-read-as-word-reads-them.md`).

Every probe paragraph after the first is `label: MARKER (end)`, three runs, and only the MARKER run carries the
property tested. Word keeps the paragraphs one for one; it merges runs whose formatting is equal, so in a save the
marker can share a run with its label.

Word's save is the oracle: Word writes back only values it reads one way (an on value bare, an off value `0` or
dropped, a colour as upper-case hex, a repaired element dropped), and docx4j reads those right. So a marker's
reading in the probe must equal its reading in the save. Where the save keeps the probe's markup and docx4j reads
both alike but Word doesn't (a `w:u` with only a `w:color`), the tests state Word's reading outright.

| Round | Files | What |
|---|---|---|
| 1 | `onoff-valid`, `onoff-empty`, `onoff-space`, `onoff-TRUE`, `onoff-yes` | `w:b` with every `ST_OnOff` value, bare and absent; `""`, `" "`, `TRUE`, `yes` |
| 2 | `props-b-case`, `props-b-styles`, `props-toggles-1`, `props-toggles-2` | the off words in other cases and with spaces; off values over bold styles, the toggle; ten more on/off properties |
| 2 | `props-u-valid`, `props-u-case`, `props-u-other` | `w:u` bare, colour-only, valid and invalid values |
| 2 | `props-enum-missing`, `props-enum-other` | `highlight`, `vertAlign`, `em`, `color` with the value missing, in another case, or outside the enumeration |
| 3 | `colour-names-highlight`, `colour-names-css`, `colour-case`, `colour-hex` | `w:color` names, cases, hex spellings |
| 4 | `colour-table-grey`, `colour-table-other` | the gray/grey spellings; CSS 4, X11 and system names; the long HTML list |
