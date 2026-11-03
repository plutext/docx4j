# The broken-part probe family

One document, and the same document with one part broken in one way. What a reader does with a part it cannot
read then shows in the tokens it returns: it refuses the document, reads the part as absent, keeps what it parsed,
or never looks at the part. The brief is docx-tool-identifier's CR-001 §3c (Jason, 3 November 2026: "add the
broken-part family"). Built by `build_broken_part.py` (`python3 -I build_broken_part.py`, from this folder),
tables included; a rebuild is byte-identical (stored zips, fixed dates).

## Files

| Case | File | What is broken |
|---|---|---|
| 100 | `broken-part-intact.docx` | Nothing: the twin each broken file is read against. In order: BP_BEGIN. A run in `w:caps` with no `w:lang` (the settings' `w:themeFontLang` is tr-TR), and one with `w:lang` tr-TR. Two Heading1 paragraphs numbered by their style (STYLE_LIST_1, STYLE_LIST_2), two numbered directly (DIRECT_LIST_1., DIRECT_LIST_2.). A run hidden by a character style; a run in capitals by a character style; a paragraph in small capitals by its style. A run hidden directly. An insertion and a deletion. Three fields marked `w:dirty` (TITLE, DOCPROPERTY Company, DOCPROPERTY Custom1), each with a stale cached result. Two comments, the second a reply (`commentsExtended`). BP_END. |
| 101 | `broken-part-styles.docx` | The styles part not well formed, cut early (its root's start tag and six characters). |
| 102 | `broken-part-styles-absent.docx` | The styles part absent (the part, its relationship and its override). |
| 103 | `broken-part-numbering.docx` | The numbering part cut early. |
| 104 | `broken-part-numbering-absent.docx` | The numbering part absent. |
| 105 | `broken-part-settings.docx` | The settings part cut early. |
| 106 | `broken-part-settings-absent.docx` | The settings part absent. |
| 107 | `broken-part-numbering-unneeded.docx` | The numbering part cut early; the four numbered paragraphs left out. |
| 108 | `broken-part-numbering-order.docx` | The numbering part cut early; the lists interleaved: heading, item, item, heading, item. |
| 109 | `broken-part-numbering-order-absent.docx` | The same, with the numbering part absent. |
| 110 | `broken-part-styles-late.docx` | The styles part cut late: inside the third style, after Hid and Caps. |
| 111 | `broken-part-styles-invalid.docx` | The styles part well formed, with its root in a foreign namespace. |
| 112 | `broken-part-document-entity.docx` | The main part declares an internal entity in a DOCTYPE and uses it (ENTITY_EXPANDED). |
| 113 | `broken-part-document-encoding.docx` | The main part declared UTF-8, its bytes cp1252 (ENC_CAFÉ_X). |
| 114 | `broken-part-document-truncated.docx` | The main part cut after TRUNC_A's paragraph. |
| 115 | `broken-part-core.docx` | `docProps/core.xml` cut early. |
| 116 | `broken-part-app.docx` | `docProps/app.xml` cut early. |
| 117 | `broken-part-custom.docx` | `docProps/custom.xml` cut early. |
| 118 | `broken-part-commentsExtended.docx` | `word/commentsExtended.xml` cut early. |
| 119 | `broken-part-document-rels.docx` | `word/_rels/document.xml.rels` cut after its third relationship, inside `Relationships`. The styles, numbering and settings relationships are kept; the comments and commentsExtended relationships are lost (docx4j-python CR-031 phase B's hand-off). |
| 120 | `broken-part-package-rels.docx` | `_rels/.rels` cut after its first relationship. The main part's relationship is kept; core, app and custom are lost. |
| 121 | `store-not-read.docx` | Two custom XML stores whose roots fit the controls' XPath (`/ns0:root[1]/ns0:name[1]`). Control A names store 1, B names store 2, C names no store. Store 1 is not well formed (cut after `</name>`). |
| 122 | `store-not-read-intact.docx` | The same, with store 1 intact. |
| | `expected-tokens.tsv` | The tokens as Word read each file (below). |
| | `expected-findings.tsv` | docx4j's `part.not_read` (REQ-801), in every view, for the six files whose reading needs a part it cannot read. |
| | `goldens.tsv` | Empty. `ExtractionConformance` reads it. |
| | `*-word365-utf8.txt`, `*-word365.docx` | Word's saves (below). |

Cases are numbered from 100 in the builder's order, since `ExtractionConformance`'s supplemental scenarios key on
cases 36 to 63.

## expected-tokens.tsv

The columns are the compact probe's. Each row gives a token's count in the `accepted`, `original` and `markup`
views under the WD's defaults. `with_option` gives its count in Word's own save, whose scenario is in `option`:
`profile: word-text; list_labels: true; fields: update`. Word's text save writes the labels, and Jason answered Yes
to "update fields?".

The counts are Word's reading after any repair:
- `with_option` counts the token in Word's save; `BrokenPartTest` checks every one.
- The default columns are the WD's default reading of the document as Word read it: hidden text out, labels out,
  fields cached, small capitals as stored, and comments in the markup view only.
- The insertion and the deletion have no option: under the preset, the caller's view still applies.

A file's rows hold the tokens its broken part can change, beside BP_BEGIN and BP_END; the intact file's rows hold
all of them. The note on a file's first row says what Word did on opening it.

`statements` names the token's statement, REQ-801 for a broken file, and, where docx4j reads otherwise today, the
open item or defect the row waits on. A label that is not a REQ id leaves the row pending. (WD 0.72 (1), Word casing a
run with no `w:lang` by its editing language and never by `w:themeFontLang`, was ruled on 4 November 2026 as WD
0.80's `default_language`, and docx4j follows it, so the intact file's ISTANBUL_THEME rows are checked.)

| Label | What it waits on | docx4j today |
|---|---|---|
| `WD 0.72 (2)` | A numbering part absent or not well formed. Word's default decimal list: one nine-level definition, the style's instance plain, the direct paragraphs' instance restarting, so one shared counter after the restart (WD 0.73). | no labels |
| `WD 0.72 (3)` | A styles part cut late. Word keeps the styles before the break (Hid, Caps). | reads the part as absent |
| `WD 0.72 (4)` | A properties part not well formed, under `fields: update`, reads as absent. TITLE and DOCPROPERTY Company come out empty; DOCPROPERTY Custom1 is Word's "Error! Unknown document property name." | keeps the cached results |
| `defect (d)` | docx4j's own: a custom properties part it cannot read leaves every property field on its cached result. | all three fields stale |
| `WD 0.73 (rels)` | A relationships part not well formed. Word keeps the relationships before the break. | a part's relationships read as none, with no warning (no styles, numbering, settings or comments); the package's refuse the load |
| `WD 0.69 (c)` | A custom XML store not well formed reads as absent, and every control is bound under REQ-509's repair. | refuses the load ("Failed to getPart") |

The three main-part files have no rows. Word will not open them, and docx4j refuses to read them (REQ-801), which
`BrokenPartTest` checks.

## docx4j's check

`ExtractionConformance` over this folder (`BrokenPartTest`), with docx4j 17.3.6-SNAPSHOT on 4 November 2026: 978
checks pass and 324 pend, each pending row on one of the labels above. Each labelled row was also checked as if
unlabelled, and every one fails today, so no label hides a row docx4j reads as Word does.

## Word

Jason opened the files in Word for Microsoft 365 on the VM (build 20326.20232 when last recorded, 28 October) on
3 November 2026. On 4 November, in a fresh session, he opened the intact control again, the two relationships files
and the ordering pair. For each file he answered Yes to "update fields?" (every broken-part file has three
`w:dirty` fields; the store files have none), saved as Plain Text in UTF-8 (`<file>-word365-utf8.txt`, 20 saves),
and, where Word repaired, saved a `.docx` (`<file>-word365.docx`, 15 saves). The intact control's `.docx` save is
from the first run.

| File | Word on opening | What it read |
|---|---|---|
| intact | the fields prompt | everything. ISTANBUL_THEME is undotted, and the `.docx` save adds `w:lang` en-AU to `w:docDefaults`. The first run reported unreadable content; the recheck in a fresh session did not, so the report was not the file's. |
| styles, styles-invalid | unreadable content; repaired "Styles" | no styles, as absent |
| styles-absent | the fields prompt | no styles |
| styles-late | repaired "Styles" | Hid and Caps kept; SmallCapsPara and Heading1 lost |
| numbering | repaired "Lists" | "1. first heading / 2. second heading / 1. first item / 2. second item" |
| numbering-absent | the fields prompt | the same |
| numbering-unneeded | repaired "Lists" | nothing lost |
| numbering-order | repaired "Lists" | "1. heading one / 1. item one / 2. item two / 3. heading two / 4. item three": one counter, restarted by the items |
| numbering-order-absent | the fields prompt | the same |
| settings | repaired "Summary Info" | no settings; no token changes |
| settings-absent | the fields prompt | no token changes |
| document-entity, -encoding, -truncated | would not open | |
| core, app, custom | unreadable content; repaired | TITLE empty; Company empty; Custom1 "Error! Unknown document property name." |
| commentsExtended | unreadable content; repaired | the reply link lost ([AB2R1] becomes [AB2]); no token changes |
| document-rels | unreadable content; repaired "Comments" | styles, numbering and settings kept; the comments' relationship lost, their references left as empty comments ([1], [2]) |
| package-rels | unreadable content; repaired | the main part kept; the three properties parts lost, read as for core, app and custom |
| store-not-read | unreadable content; repaired | store 1 dropped; A, B and C all STORE2_VALUE (WD 0.69) |
| store-not-read-intact | nothing | A STORE1_VALUE, B STORE2_VALUE, C STORE1_VALUE (no store named: the first whose root fits) |

The saves are not compared line by line (there is no `word-saves.tsv`). Word's converter differs from the text model
in ways REQ-1002's NOTE lists: comments are marked by initials ([AA1]) and appended after the body, and lines end CR
LF. It also writes each list label followed by a space where the level's suffix is the default tab. The token counts
are compared instead.
