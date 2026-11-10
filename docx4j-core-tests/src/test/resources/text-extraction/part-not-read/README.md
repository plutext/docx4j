# part.not_read

The fixture for WD 0.66 REQ-801's `part.not_read` (docx4j CR-046 §10): a part that the reading consulted and
needed for this document, but does not emit, cannot be read. The text is then read as for a package that lacks the
part, and the result carries one `part.not_read` naming it. Built by `build_part_not_read.py`
(`python3 -I build_part_not_read.py`); a rebuild is byte-identical (stored zips, fixed dates).

## Files

| File | Holds |
|---|---|
| `part-not-read-intact.docx` | Ten paragraphs. Two `Heading1` paragraphs, numbered by their style ("Article I", upper Roman). Two numbered directly ("1."). A run hidden by a character style. A run in capitals by a character style and one by direct `w:caps`, neither with a `w:lang`, under the settings' `themeFontLang` tr-TR, which plays no part in their capitals (WD 0.80 REQ-607): "ISTANBUL", cased by `default_language`'s en-US. Before 17.3.6 docx4j took tr-TR from `themeFontLang` ("İSTANBUL"), which Word does not. A paragraph in small capitals by its style. A run hidden directly. An insertion and a deletion. |
| `part-not-read-styles.docx`, `-numbering`, `-settings` | The same, with that part not well formed: cut to two thirds of its characters. |
| `part-not-read-styles-absent.docx`, `-numbering-absent`, `-settings-absent` | The same, with that part absent (part, relationship and override removed). |
| `part-not-read-numbering-unneeded.docx` | Two plain paragraphs, the numbering part not well formed: no paragraph needs it, so nothing is lost and nothing is raised. |
| `expected/<fixture>.<view>.txt` | The plain text in each view, with `list_labels: true` and `small_caps: upper`, the other options at their defaults. |
| `expected-warnings.tsv` | The warnings in each view: kind and selector, `; ` between two. |

The font table is left out: docx4j's text model does not read it, and the WD raises `part.not_read` for one only
where a reading consults it.

## What the expected results say

- A part not well formed reads as the same part absent, in every view, and raises `part.not_read` once, with the
  part's name as its selector.
- **The numbering:** every label is lost, by style and direct.
- **The styles:** the labels of paragraphs numbered by their style are lost, and so are hidden text, capitals
  and small capitals given by a style. The direct labels are kept, as with no styles part. Before 17.3.6
  docx4j lost those too.
- **The settings:** losing them changes no text, since the capitals no longer take `themeFontLang` (WD 0.80). The
  settings are always needed, since the result carries `fields_update_on_open`, so `part.not_read` is raised.
- **A part the document does not need raises nothing** (`numbering-unneeded`).
- **The numbering part absent, a paragraph numbered** (`numbering-absent`): `part.absent` (WD 0.76), once, its
  selector `/word/numbering.xml`, the part the package would have held, as the reading supplies Word's default list
  for it (REQ-510). The styles and settings absent raise nothing: `part.absent` names a part whose content the
  reading supplies, today only the numbering's. Until 17.3.7 docx4j raised nothing here.

Word was not asked: it repairs or refuses a part not well formed, and the rule is about the reading, not
about Word's repair.

`PartNotReadTest` (docx4j-core-tests, `org.docx4j.textextraction`) reads every file in every view against these
expected results. It also checks:
- the numbering emulator outside the text model;
- that an untouched save keeps the unreadable part's bytes;
- that one level down the warning names the embedded package's part.
