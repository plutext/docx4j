# Text extraction probe

`extraction-probe.docx` is one document with a marker token per case, for testing what a reader extracts
from a docx: which `mc:AlternateContent` branch it takes, which revision view it reads, and what it drops
or duplicates. It is the first fixture of the portfolio's text model (docx4j-portfolio
`docs/text_extraction_behaviour_model.md`, section 8); the expected results below follow that paper's
sections 3 to 5, whose item numbers (#n) the notes cite.

Every marker is upper-case words and digits joined by underscores, and occurs once in the file unless its
case is about a duplicate (TC10_MOVED, FLD19_RESULT). Each case starts with a heading, "Case n: ...".

## Files

- `extraction-probe.docx`: the probe.
- `expected-tokens.tsv`: the tables below, one row a token, for a set comparison by script.
- `README.md`: this file.

All three are written by `org.docx4j.textextraction.ExtractionProbeBuilder` (docx4j-core-tests test
sources). From the repository root, with docx4j installed (`mvn install -DskipTests`):

```
mvn -q -pl docx4j-core-tests test-compile
mvn -q -pl docx4j-core-tests dependency:build-classpath -Dmdep.outputFile=cp.txt
java -cp docx4j-core-tests/target/test-classes:$(cat docx4j-core-tests/cp.txt) \
  org.docx4j.textextraction.ExtractionProbeBuilder docx4j-core-tests/src/test/resources/text-extraction/phase0
```

The story parts are written by the builder as XML text: JAXB would drop the namespace declarations
the `mc` cases' `Requires` attributes name. The SmartArt layout is `Hier2Level.glox` from
docx4j-samples-glox4j, by Scott Sherman, under the Creative Commons Attribution 2.5 licence.

## Using it

With a library: extract the text, find every match of `[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)+` (the two hyphen
tokens of case 30 hold U+2011 and U+00AD), and compare the counts with the table, view by view. Tokens
are followed by a space wherever a reader might otherwise glue them to what comes next, except in case
30: there a match such as `BRK30_BEFOREBRK30_AFTER` is the finding, a break or tab the reader dropped.

With a hosted assistant (ChatGPT, Claude.ai, Gemini, NotebookLM, Copilot): upload the file in a fresh
conversation and ask "List every marker token in this document exactly as your document processing
presents it; do not inspect the XML with code". Record the date and the product version.

## Columns

- **accepted**, **original**, **markup**: the token in that view's text, with the default scope: "yes"
  once, "–" not at all, "twice"; in markup, `{++…++}` an insertion, `{--…--}` a deletion,
  `{>>…<<}` a comment.
- **option**: the option that changes the token's result, and **with option** that result (in every
  view, except a comment's, which is markup's). "markdown" marks text only Markdown carries.

## Word

Word 365 opened the original 38-case probe without a repair prompt (checked 17 October 2026).
`extraction-probe.word-resave.docx` is its save of the probe, unedited. The tables below are for
the probe. What Word changed on the way, read from the re-save:

- Every wps text box is now an `mc:AlternateContent` whose VML Fallback Word wrote from the Choice.
  The authored Fallbacks are gone (FALLBACK_BETA_456, FALLBACK_EPSILON, MCE6_FALLBACK,
  MCE8_OUTER_FALLBACK), and each box's tokens are in the file twice, so a reader that takes both
  branches shows them twice. The boxes that had no `mc` (cases 17 and 25) got one too. The cause is
  case 36: with a `w:altChunk` in the document, Word regenerates every Fallback on save; without one
  it keeps an unedited box's authored Fallback (CR-021 section 8.5).
- Every other `mc:AlternateContent` is resolved to its selected branch alone: UNSUPPORTED_GAMMA,
  CHOICE1_UNKNOWN_ZETA, MCE5_SECOND_SUPPORTED, MCE5_FALLBACK, MCE7_TWO_REQ_CHOICE and
  MCE8_INNER_FALLBACK are gone, and case 7's Fallback box is a plain `w:pict`.
- The HYPERLINK field is a `w:hyperlink`; FLD21_FIELD_URL_MARK is in the relationships.
- The bound control shows BND23_XML_VALUE; BND23_SURFACE_STALE is gone, so `bindings: surface` gives
  the XML value.
- The altChunk is expanded: ALT36_CHUNK_TEXT is an ordinary paragraph, there in every view.
- The header no section uses (HDR26_ORPHAN) is dropped, and the headers are renumbered.
- The SmartArt has a drawing part, so the three SMA35 tokens are in the file twice (data and drawing).
- Each `m:t` is split where letters meet digits (`MTH`, `32_`, `A`): the text is the same, but a
  reader that looks for tokens run by run loses MTH32_A and MTH32_B.

## Rulings the probe prompted

Three cases had no rule in the paper when the probe was built; it has since ruled (17 October 2026):

- Case 33: a picture marked decorative (`adec:decorative`) gives no alt text under any option, even
  with a `descr` (D22).
- Case 28: a resolved comment (`w15:done`) is a comment like any other; JSON adds `resolved: true`.
- Case 21: Markdown gives a hyperlink's target as `[text](target)` for both forms, the HYPERLINK
  field's too, whatever `field_codes` says: the instruction supplies the address, not text.

## Phase 0 extension

Cases 39–49 and 53–54 extend the main probe; cases 50–52 are in `extraction-probe-external.docm`;
cases 55–57 (a table of contents, an interrupted list, an embedded workbook with hidden sheets) are in
`extraction-probe-structure.docx`, a fixture of their own so that the main probe is not rebuilt (19 October 2026);
cases 58–67, the inspector's boundary cases (CR-050 phase 0), are in `extraction-probe-inspect.docx`.
Jason checked the earlier 52-case r2 DOCX without repair, declining field updates (18 October 2026).
The unchanged external DOCM also passed. Jason completed the r3 check for cases 53–54 (18 October 2026).
The Word findings above describe the original 38-case
fixture, retained in the parent directory with its re-save.
`case-statements.tsv` maps the original WD Appendix A plus the approved cases 53–54 (WD 0.5) and 55–57.
`expected-order.tsv` pins presentation and glossary order. `expected-findings.tsv` separates warnings,
metadata and inspector findings from text counts. `expected-inspection.tsv` lists every finding an inspector
makes, by location, and `expected-inspection-status.tsv` each fixture's status (`../INSPECTION.md`).
Inspector requirements are pending CR-050 phase 1.
The default test environment has no HTML reader (jsoup and docx4j-ImportXHTML). The HTML and MHT chunk rows' capability scenario supplies one.
All external targets are deliberately missing local files. Nothing is fetched by the suite.
The VBA project has attribute-only modules and a comment marker; it contains no executable procedures.
See `../support/README.md` for its provenance and rebuild command.

Golden conventions and baseline commands are in `../GOLDENS.md`.

## Cases

### Case 1: a text box: a wps Choice and a VML Fallback with different text

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `CHOICE_ALPHA_123` | yes | yes | yes | `text_boxes: false` | – |  | REQ-301, REQ-302, REQ-518 |
| `FALLBACK_BETA_456` | – | – | – |  |  | the Fallback; Word never shows it | REQ-301, REQ-302, REQ-518 |

### Case 2: a wps Choice and no Fallback

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `CHOICE_ONLY_789` | yes | yes | yes | `text_boxes: false` | – |  | REQ-302 |

### Case 3: a Choice requiring an unknown namespace, and a Fallback, each a run

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `UNSUPPORTED_GAMMA` | – | – | – |  |  | Requires zz9, which no reader understands | REQ-302 |
| `SUPPORTED_DELTA` | yes | yes | yes |  |  |  | REQ-302 |

### Case 4: two Choices, the first requiring an unknown namespace, the second wps; a Fallback

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `CHOICE1_UNKNOWN_ZETA` | – | – | – |  |  |  | REQ-302 |
| `CHOICE2_WPS_ETA` | yes | yes | yes | `text_boxes: false` | – | the first Choice whose Requires is understood | REQ-302 |
| `FALLBACK_EPSILON` | – | – | – |  |  |  | REQ-302 |

### Case 5: two Choices both understood (w14, then wps), each a run; a Fallback

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `MCE5_FIRST_SUPPORTED` | yes | yes | yes |  |  | the first understood Choice wins | REQ-302 |
| `MCE5_SECOND_SUPPORTED` | – | – | – |  |  |  | REQ-302 |
| `MCE5_FALLBACK` | – | – | – |  |  |  | REQ-302 |

### Case 6: the wps namespace bound to another prefix (shape2010)

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `MCE6_REBOUND_CHOICE` | yes | yes | yes | `text_boxes: false` | – | the namespace URI decides, not the prefix: a reader matching the prefix wps takes the Fallback | REQ-303 |
| `MCE6_FALLBACK` | – | – | – |  |  |  | REQ-303 |

### Case 7: a Choice requiring two namespaces, one unknown (wps zz9)

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `MCE7_TWO_REQ_CHOICE` | – | – | – |  |  | every namespace a Choice requires must be understood | REQ-302 |
| `MCE7_FALLBACK` | yes | yes | yes | `text_boxes: false` | – |  | REQ-302 |

### Case 8: nested: a text box in a wps Choice whose paragraph holds another AlternateContent

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `MCE8_OUTER` | yes | yes | yes | `text_boxes: false` | – |  | REQ-301 |
| `MCE8_INNER_CHOICE` | yes | yes | yes | `text_boxes: false` | – |  | REQ-301 |
| `MCE8_INNER_FALLBACK` | – | – | – |  |  |  | REQ-301 |
| `MCE8_OUTER_FALLBACK` | – | – | – |  |  | the outer Fallback (a VML box) | REQ-301 |

### Case 9: an insertion and a deletion in one paragraph

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `TC9_BASE` | yes | yes | yes |  |  |  | REQ-402, REQ-403 |
| `TC9_INSERTED` | yes | – | `{++…++}` |  |  |  | REQ-402, REQ-403 |
| `TC9_DELETED` | – | yes | `{--…--}` |  |  |  | REQ-402, REQ-403 |

### Case 10: a move from paragraph A to paragraph B, with its range markers

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `TC10_PARA_A` | yes | yes | yes |  |  |  | REQ-404 |
| `TC10_PARA_B` | yes | yes | yes |  |  |  | REQ-404 |
| `TC10_MOVED` | yes | yes | `{--…--}`, `{++…++}` |  |  | in the file twice (moveFrom, moveTo); accepted: once, after TC10_PARA_B; original: once, after TC10_PARA_A; markup: {--…--} after A and {++…++} after B | REQ-404 |

### Case 11: a deleted paragraph mark between two paragraphs

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `TC11_FIRST` | yes | yes | yes |  |  | accepted: one line, TC11_FIRST TC11_SECOND; original and markup: two lines | REQ-402, REQ-405 |
| `TC11_SECOND` | yes | yes | yes |  |  |  | REQ-402, REQ-405 |

### Case 12: an inserted paragraph mark splitting a paragraph

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `TC12_ONE` | yes | yes | yes |  |  | accepted and markup: two lines; original: one line, TC12_ONE TC12_TWO | REQ-402 |
| `TC12_TWO` | yes | yes | yes |  |  |  | REQ-402 |

### Case 13: a table with a plain row, a deleted row and an inserted row

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `TC13_PLAIN_ROW` | yes | yes | yes |  |  |  | REQ-404 |
| `TC13_DELETED_ROW` | – | yes | `{--…--}` |  |  | the row is gone in accepted | REQ-404 |
| `TC13_INSERTED_ROW` | yes | – | `{++…++}` |  |  | the row is gone in original | REQ-404 |

### Case 14: a deletion inside an insertion (a second author deletes part of the first author’s insertion)

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `TC14_INS_KEPT` | yes | – | `{++…++}` |  |  |  | REQ-404 |
| `TC14_DEL_IN_INS` | – | – | `{--…--}` |  |  | the innermost change wins in markup | REQ-404 |

### Case 15: an inserted field and a deleted field

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `TC15_INSERTED_FIELD` | yes | – | `{++…++}` |  |  | the field's result | REQ-404 |
| `TC15_DELETED_FIELD` | – | yes | `{--…--}` |  |  | the field's result; w:delInstrText is code, never text | REQ-404 |

### Case 16: a formatting-only change

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `TC16_FORMAT_ONLY` | yes | yes | yes |  |  | no marker in markup | REQ-404 |

### Case 17: tracked changes in a text box and in a footnote

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `TC17_BOX_BASE` | yes | yes | yes | `text_boxes: false` | – |  | REQ-404, REQ-518, REQ-503 |
| `TC17_BOX_INS` | yes | – | `{++…++}` | `text_boxes: false` | – |  | REQ-404, REQ-518, REQ-503 |
| `TC17_BOX_DEL` | – | yes | `{--…--}` | `text_boxes: false` | – |  | REQ-404, REQ-518, REQ-503 |
| `TC17_NOTE_REF_HERE` | yes | yes | yes |  |  |  | REQ-404, REQ-518, REQ-503 |
| `TC17_NOTE_BASE` | yes | yes | yes | `notes: false` | – |  | REQ-404, REQ-518, REQ-503 |
| `TC17_NOTE_DEL` | – | yes | `{--…--}` | `notes: false` | – |  | REQ-404, REQ-518, REQ-503 |

### Case 18: hidden and near-hidden text

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `HID18_VANISH` | – | – | – | `hidden: true` | yes |  | REQ-507, REQ-902 |
| `HID18_STYLE_VANISH` | – | – | – | `hidden: true` | yes | the style's w:vanish (the paper's phase 2 resolves styles) | REQ-507, REQ-902 |
| `HID18_HIDDEN_PARA` | – | – | – | `hidden: true` | yes | runs and mark hidden: no line at all | REQ-507, REQ-902 |
| `HID18_WHITE_TEXT` | yes | yes | yes |  |  | not hidden | REQ-507, REQ-902 |
| `HID18_TINY` | yes | yes | yes |  |  | not hidden | REQ-507, REQ-902 |
| `HID18_WEBHIDDEN` | yes | yes | yes |  |  | web view only, not hidden (#3) | REQ-507, REQ-902 |

### Case 19: complex fields: a marker in the code and the result, and one in the code only

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `FLD19_RESULT` | yes | yes | yes | `field_codes: true` | twice | in the code and the cached result | REQ-505 |
| `FLD19_INSTR_ONLY` | – | – | – | `field_codes: true` | yes | in the code only | REQ-505 |
| `FLD19_RESULT_TWO` | yes | yes | yes |  |  |  | REQ-505 |

### Case 20: a simple field

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `FLD20_SIMPLE_INSTR` | – | – | – | `field_codes: true` | yes | w:fldSimple/@instr | REQ-505 |
| `FLD20_SIMPLE_RESULT` | yes | yes | yes |  |  |  | REQ-505 |

### Case 21: hyperlinks: a w:hyperlink with r:id, and a HYPERLINK field

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `FLD21_LINK_TEXT` | yes | yes | yes |  |  |  | REQ-703 |
| `FLD21_URL_MARK` | – | – | – | `markdown` | yes | the target, in the relationship: Markdown [text](target), not plain text | REQ-703 |
| `FLD21_FIELD_LINK_TEXT` | yes | yes | yes |  |  |  | REQ-703 |
| `FLD21_FIELD_URL_MARK` | – | – | – | `markdown` | yes | the target, in the field code: Markdown carries it whatever field_codes says | REQ-703 |

### Case 22: a content control showing its placeholder

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `PLC22_PLACEHOLDER` | yes | yes | yes | `placeholders: false` | – | Word shows it (#4) | REQ-508 |

### Case 23: a content control bound to a custom XML part, its surface text stale

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `BND23_XML_VALUE` | yes | yes | yes | `bindings: surface` | – | the value in customXml/item1.xml, which Word shows (it refreshes the control on open) (#4a) | REQ-509 |
| `BND23_SURFACE_STALE` | – | – | – | `bindings: surface` | yes | the stale cached surface in w:sdtContent | REQ-509 |

### Case 24: a rich text control, and a block-level control in a table cell

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `SDT24_CONTENT` | yes | yes | yes |  |  |  | REQ-517 |
| `SDT24_IN_CELL` | yes | yes | yes |  |  |  | REQ-517 |

### Case 25: text boxes and legacy WordArt

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `TBX25_FLOAT` | yes | yes | yes | `text_boxes: false` | – |  | REQ-518, REQ-511, REQ-502 |
| `TBX25_INLINE` | yes | yes | yes | `text_boxes: false` | – |  | REQ-518, REQ-511, REQ-502 |
| `TBX25_VML_ONLY` | yes | yes | yes | `text_boxes: false` | – |  | REQ-518, REQ-511, REQ-502 |
| `TBX25_IN_CELL` | yes | yes | yes | `text_boxes: false` | – | after the table (the anchoring paragraph is in a cell) | REQ-518, REQ-511, REQ-502 |
| `TBX25_IN_HEADER` | – | – | – | `include: headers` | yes | a box in section 1's default header | REQ-518, REQ-511, REQ-502 |
| `WA25_TEXTPATH` | yes | yes | yes | `text_boxes: false` | – | v:textpath/@string, as a one-paragraph box (#20b) | REQ-518, REQ-511, REQ-502 |

### Case 26: headers and footers over two sections, and a header no section uses

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `HDR26_DEFAULT` | – | – | – | `include: headers` | yes | section 1 | REQ-502 |
| `HDR26_FIRST` | – | – | – | `include: headers` | yes | section 1's first page (w:titlePg) | REQ-502 |
| `HDR26_EVEN` | – | – | – | `include: headers` | yes | even pages (w:evenAndOddHeaders), section 2's too: it has no even header of its own | REQ-502 |
| `FTR26_DEFAULT` | – | – | – | `include: footers` | yes | section 2 inherits it | REQ-502 |
| `HDR26_SECTION2` | – | – | – | `include: headers` | yes |  | REQ-502 |
| `HDR26_ORPHAN` | – | – | – |  |  | related from the document but no section references it: Word shows none of it | REQ-502 |

### Case 27: a footnote, an endnote, and a footnote whose reference is deleted

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `FN27_REF_HERE` | yes | yes | yes |  |  |  | REQ-503, REQ-402 |
| `FN27_NOTE_TEXT` | yes | yes | yes | `notes: false` | – | after the body, [n] at the reference (#7) | REQ-503, REQ-402 |
| `EN27_REF_HERE` | yes | yes | yes |  |  |  | REQ-503, REQ-402 |
| `EN27_NOTE_TEXT` | yes | yes | yes | `notes: false` | – | after the footnotes | REQ-503, REQ-402 |
| `FN27_DELETED_REF_HERE` | yes | yes | yes |  |  |  | REQ-503, REQ-402 |
| `FN27_DELETED_REF_NOTE` | – | yes | yes | `notes: false` | – | its reference is deleted: gone in accepted; in markup the note is there, its reference {--[n]--} | REQ-503, REQ-402 |

### Case 28: comments: one with a reply, and one resolved

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `CMT28_ANCHOR` | yes | yes | yes |  |  |  | REQ-504 |
| `CMT28_RESOLVED_ANCHOR` | yes | yes | yes |  |  |  | REQ-504 |
| `CMT28_COMMENT_TEXT` | – | – | `{>>…<<}` | `comments: true` | `{>>…<<}` | {>>…<<} at the end of the commented range (#8) | REQ-504 |
| `CMT28_REPLY` | – | – | `{>>…<<}` | `comments: true` | `{>>…<<}` | a reply (commentsExtended paraIdParent), after its parent | REQ-504 |
| `CMT28_RESOLVED_COMMENT` | – | – | `{>>…<<}` | `comments: true` | `{>>…<<}` | resolved (w15:done): a comment like any other; JSON adds resolved: true | REQ-504 |

### Case 29: symbols, a numbered list and a Symbol-font bulleted list

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `SYM29_MARK` | yes | yes | yes |  |  | then w:sym U+F0FC (Wingdings) and U+F061 (Symbol), as written (#14) | REQ-601, REQ-510 |
| `LST29_ITEM_A` | yes | yes | yes |  |  | the labels 1. 2. 3. are not text by default (#9) | REQ-601, REQ-510 |
| `LST29_ITEM_B` | yes | yes | yes |  |  |  | REQ-601, REQ-510 |
| `LST29_ITEM_C` | yes | yes | yes |  |  |  | REQ-601, REQ-510 |
| `LST29_BULLET_ONE` | yes | yes | yes |  |  | the bullet (Symbol U+F0B7) is not text by default | REQ-601, REQ-510 |
| `LST29_BULLET_TWO` | yes | yes | yes |  |  |  | REQ-601, REQ-510 |

### Case 30: breaks, a tab and hyphens

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `BRK30_BEFORE` | yes | yes | yes |  |  | a text-wrapping w:br: \n (#11) | REQ-601, REQ-603 |
| `BRK30_AFTER` | yes | yes | yes |  |  |  | REQ-601, REQ-603 |
| `BRK30_PAGE` | yes | yes | yes |  |  | after a page break: \n (#12) | REQ-601, REQ-603 |
| `BRK30_COLUMN` | yes | yes | yes |  |  | after a column break: \n | REQ-601, REQ-603 |
| `TAB30_LEFT` | yes | yes | yes |  |  | \t between the two (#10) | REQ-601, REQ-603 |
| `TAB30_RIGHT` | yes | yes | yes |  |  |  | REQ-601, REQ-603 |
| `HY30_NON‑BREAK` | yes | yes | yes |  |  | w:noBreakHyphen is U+2011 (#13) | REQ-601, REQ-603 |
| `HY30_SO­FT` | yes | yes | yes |  |  | w:softHyphen is U+00AD (#13) | REQ-601, REQ-603 |

### Case 31: a table: header row, two-paragraph cell, nested table, merged cell, empty cell

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `TBL31_HEADER` | yes | yes | yes |  |  | a row is one line, its cells tab-separated (#18) | REQ-605 |
| `TBL31_CELL_P1` | yes | yes | yes |  |  | a cell's paragraphs joined by \n | REQ-605 |
| `TBL31_CELL_P2` | yes | yes | yes |  |  |  | REQ-605 |
| `TBL31_NESTED` | yes | yes | yes |  |  | flattened into its cell | REQ-605 |
| `TBL31_MERGED` | yes | yes | yes |  |  | w:gridSpan 2 | REQ-605 |
| `TBL31_LAST_CELL` | yes | yes | yes |  |  |  | REQ-605 |

### Case 32: an equation with a fraction

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `MTH32_A` | yes | yes | yes |  |  | the m:t runs in document order (#15); Markdown: LaTeX | REQ-604 |
| `MTH32_B` | yes | yes | yes |  |  |  | REQ-604 |

### Case 33: pictures: alt text and a title, decorative, and an SVG with text in it

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `IMG33_ALT_TEXT` | – | – | – | `alt_text: true` | yes | wp:docPr/@descr: plain text [image: descr] with the option; Markdown ![descr](…) by default (#20) | REQ-514 |
| `IMG33_TITLE` | – | – | – |  |  | wp:docPr/@title is never read (#20) | REQ-514 |
| `IMG33_DECORATIVE_DESCR` | – | – | – |  |  | marked decorative (adec:decorative): no alt text under any option (D22) | REQ-514 |
| `IMG33_SVG_ALT` | – | – | – | `alt_text: true` | yes | as IMG33_ALT_TEXT | REQ-514 |
| `SVG33_INNER_TEXT` | – | – | – |  |  | a <text> element in the SVG part: an image's, never text (#20) | REQ-514 |

### Case 34: a chart

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `CHT34_TITLE` | yes | yes | yes | `charts: false` | – | ChartTable: the title on its own line, then tab-separated rows, after the anchoring paragraph (#20a); the chart's embedded workbook holds these strings too | REQ-511 |
| `CHT34_SERIES_ONE` | yes | yes | yes | `charts: false` | – |  | REQ-511 |
| `CHT34_SERIES_TWO` | yes | yes | yes | `charts: false` | – |  | REQ-511 |
| `CHT34_CAT_A` | yes | yes | yes | `charts: false` | – |  | REQ-511 |
| `CHT34_CAT_B` | yes | yes | yes | `charts: false` | – |  | REQ-511 |
| `CHT34_AXIS_TITLE` | yes | yes | yes | `charts: false` | – | REQ-511; with CR-047, the note [chart: clustered column; value axis: CHT34_AXIS_TITLE] | REQ-511 |

### Case 35: SmartArt: a root and two children, stored out of reading order

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `SMA35_ROOT` | yes | yes | yes | `smartart: false` | – | reading order ROOT, CHILD_ONE, CHILD_TWO (parOf, srcOrd); ptLst order CHILD_TWO, ROOT, CHILD_ONE; cxnLst order ROOT, CHILD_TWO, CHILD_ONE (#20b) | REQ-511 |
| `SMA35_CHILD_ONE` | yes | yes | yes | `smartart: false` | – |  | REQ-511 |
| `SMA35_CHILD_TWO` | yes | yes | yes | `smartart: false` | – |  | REQ-511 |

### Case 36: an altChunk holding HTML

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `ALT36_CHUNK_TEXT` | – | – | – | `html reader present` | yes | without an HTML reader: altchunk.not_expanded; altchunks: false also omits it (CR-053 D7, pending WD 0.26) | REQ-513 |

### Case 37: ruby

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `RBY37_BASE` | yes | yes | yes |  |  |  | REQ-604 |
| `RBY37_RUBY` | – | – | – |  |  | the annotation: base text only (#16) | REQ-604 |

### Case 38: document properties and a custom XML part

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `PRP38_TITLE` | – | – | – | `metadata: true` | yes | docProps/core.xml (#22) | REQ-515, REQ-516 |
| `PRP38_SUBJECT` | – | – | – | `metadata: true` | yes | docProps/core.xml (#22) | REQ-515, REQ-516 |
| `CXP38_DATA` | – | – | – |  |  | customXml/item1.xml (#22) | REQ-515, REQ-516 |

### Case 39: an embedded workbook

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `EMB39_XLSX_CELL` | – | – | – | `embedded: true` | yes | worksheet row at the reference; REQ-512's embedded-workbook case | REQ-512 |

### Case 40: an embedded document containing another embedded document

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `EMB40_DOCX_TEXT` | – | – | – | `embedded: true` | yes | one level only | REQ-512 |
| `EMB40_NESTED_TEXT` | – | – | – |  |  | nested embedded document is not read, including with embedded: true | REQ-512 |

### Case 41: an OLE package containing a plain-text file

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `OLE41_NATIVE_TEXT` | – | – | – | `embedded: true` | yes | Ole10Native stream holding probe.txt | REQ-512 |

### Case 42: an ActiveX label with a preview

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `ACX42_CAPTION` | – | – | – |  |  | never extracted, including embedded: true; inspection: part.activex | REQ-512, REQ-902 |

### Case 43: a plain-text altChunk

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `ALT43_TEXT_LINE_ONE` | yes | yes | yes | `altchunks: false` | – | disabled chunks warn altchunk.not_expanded | REQ-513 |
| `ALT43_TEXT_LINE_TWO` | yes | yes | yes | `altchunks: false` | – | disabled chunks warn altchunk.not_expanded | REQ-513 |

### Case 44: an MHT altChunk

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `ALT44_MHT_CHUNK` | – | – | – | `html reader present` | yes | without an HTML reader: altchunk.not_expanded; altchunks: false also omits it | REQ-513 |

### Case 45: an RTF altChunk

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `ALT45_RTF_CHUNK` | – | – | – |  |  | never expanded; warning altchunk.not_expanded | REQ-513 |

### Case 46: a Flat OPC altChunk

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `ALT46_FLATOPC_CHUNK` | yes | yes | yes | `altchunks: false` | – | disabled chunks warn altchunk.not_expanded | REQ-513 |

### Case 47: a document altChunk containing an HTML altChunk

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `ALT47_DOCX_CHUNK` | yes | yes | yes | `altchunks: false` | – | disabled chunks warn altchunk.not_expanded | REQ-513 |
| `ALT47_NESTED_CHUNK` | – | – | – |  |  | one-level limit; warning altchunk.not_expanded for nested HTML | REQ-513 |

### Case 48: inspection kinds and characters preserved in extraction

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `SHD48_SHADED` | yes | yes | yes |  |  | inspection: text.near_background | REQ-902, REQ-602, REQ-518, REQ-507 |
| `ZW48_SP​LIT` | yes | yes | yes |  |  | U+200B retained; inspection: chars.zero_width | REQ-902, REQ-602, REQ-518, REQ-507 |
| `TAG48_MARK` | yes | yes | yes |  |  | followed by U+E0041 U+E0042, retained; inspection: chars.tag | REQ-902, REQ-602, REQ-518, REQ-507 |
| `OFF48_BOX` | yes | yes | yes | `text_boxes: false` | – | position -5000pt; inspection: shape.off_page | REQ-902, REQ-602, REQ-518, REQ-507 |
| `ZER48_BOX` | yes | yes | yes | `text_boxes: false` | – | width and height zero; inspection: shape.zero_size | REQ-902, REQ-602, REQ-518, REQ-507 |
| `SPV48_SEPARATOR` | yes | yes | yes |  |  | specVanish is a style separator, not hidden text | REQ-902, REQ-602, REQ-518, REQ-507 |

### Case 49: external fields with cached results

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `DDE49_INSTR` | – | – | – | `field_codes: true` | yes | inspection: field.external; never evaluated under fields: update | REQ-506, REQ-902 |
| `DDE49_RESULT` | yes | yes | yes |  |  | cached result, including fields: update | REQ-506, REQ-902 |
| `INC49_INSTR` | – | – | – | `field_codes: true` | yes | inspection: field.external; never fetched under fields: update | REQ-506, REQ-902 |
| `INC49_RESULT` | yes | yes | yes |  |  | cached result, including fields: update | REQ-506, REQ-902 |
| `FLD49_FILE_MARK` | – | – | – | `field_codes: true` | yes | inspection: field.external; Markdown also carries the link target | REQ-506, REQ-902 |

### Case 50: external relationships and updateFields

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `TPL50_MARK` | – | – | – |  |  | external target; inspection: relationship.external; never fetched under any option | REQ-506, REQ-801, REQ-902 |
| `MRG50_MARK` | – | – | – |  |  | external target; inspection: relationship.external; never fetched under any option | REQ-506, REQ-801, REQ-902 |
| `LNK50_MARK` | – | – | – |  |  | external target; inspection: relationship.external; never fetched under any option | REQ-506, REQ-801, REQ-902 |
| `OLE50_LINK_MARK` | – | – | – |  |  | external target; inspection: relationship.external; never fetched under any option | REQ-506, REQ-801, REQ-902 |

### Case 51: a VBA module

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `VBA51_MODULE_MARK` | – | – | – |  |  | inspection: part.macro; never extracted under any option | REQ-902 |

### Case 52: custom and application properties

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `CPR52_CUSTOM` | – | – | – | `metadata: true` | yes | always in block-model metadata | REQ-515 |
| `APP52_COMPANY` | – | – | – | `metadata: true` | yes | always in block-model metadata | REQ-515 |

### Case 53: an embedded presentation with groups, a table, a hidden slide and speaker notes

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `EMB53_SLIDE_ONE` | – | – | – | `embedded: true` | yes | slide order, p:spTree order; groups depth-first; table cells row-major (REQ-512, REQ-605) | REQ-512, REQ-605 |
| `EMB53_GROUP_FIRST` | – | – | – | `embedded: true` | yes | slide order, p:spTree order; groups depth-first; table cells row-major (REQ-512, REQ-605) | REQ-512, REQ-605 |
| `EMB53_GROUP_NESTED` | – | – | – | `embedded: true` | yes | slide order, p:spTree order; groups depth-first; table cells row-major (REQ-512, REQ-605) | REQ-512, REQ-605 |
| `EMB53_AFTER_GROUP` | – | – | – | `embedded: true` | yes | slide order, p:spTree order; groups depth-first; table cells row-major (REQ-512, REQ-605) | REQ-512, REQ-605 |
| `EMB53_TABLE_A` | – | – | – | `embedded: true` | yes | slide order, p:spTree order; groups depth-first; table cells row-major (REQ-512, REQ-605) | REQ-512, REQ-605 |
| `EMB53_TABLE_B` | – | – | – | `embedded: true` | yes | slide order, p:spTree order; groups depth-first; table cells row-major (REQ-512, REQ-605) | REQ-512, REQ-605 |
| `EMB53_TABLE_C` | – | – | – | `embedded: true` | yes | slide order, p:spTree order; groups depth-first; table cells row-major (REQ-512, REQ-605) | REQ-512, REQ-605 |
| `EMB53_TABLE_D` | – | – | – | `embedded: true` | yes | slide order, p:spTree order; groups depth-first; table cells row-major (REQ-512, REQ-605) | REQ-512, REQ-605 |
| `EMB53_SLIDE_TWO` | – | – | – | `embedded: true` | yes | slide order, p:spTree order; groups depth-first; table cells row-major (REQ-512, REQ-605) | REQ-512, REQ-605 |
| `EMB53_HIDDEN_SLIDE` | – | – | – | `embedded: true; hidden: true` | yes | hidden slide show=0; absent by default and with embedded: true; read with embedded: true and hidden: true, as hidden text is | REQ-512, REQ-605 |
| `EMB53_SPEAKER_NOTES` | – | – | – |  |  | speaker notes omitted, including with embedded: true | REQ-512, REQ-605 |

### Case 54: a glossary document with two building-block entries

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `GLS54_ENTRY_ONE` | – | – | – | `include: glossary` | yes | glossary comes last, after endnotes; first docPart (REQ-501, REQ-516) | REQ-501, REQ-516 |
| `GLS54_ENTRY_TWO` | – | – | – | `include: glossary` | yes | second docPart, after the first (REQ-501, REQ-516) | REQ-501, REQ-516 |

### Case 55: a table of contents

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `TOC55_TITLE` | yes | yes | yes |  |  | the contents heading is not the field's (REQ-520), so it is always read | REQ-520, REQ-505 |
| `HEAD55_ONE` | yes | yes | yes | `toc: true` | twice | a heading the table of contents lists: its entry is read only under toc: true | REQ-520, REQ-505 |
| `HEAD55_TWO` | yes | yes | yes | `toc: true` | twice | the same for the second heading | REQ-520, REQ-505 |

### Case 56: a list interrupted by paragraphs

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `LST56_APPLES` | yes | yes | yes |  |  | one line each in plain text; the Markdown golden holds the list markers (REQ-703) | REQ-703, REQ-510 |
| `LST56_MARGIN_NOTE` | yes | yes | yes |  |  | one line each in plain text; the Markdown golden holds the list markers (REQ-703) | REQ-703, REQ-510 |
| `LST56_PEARS` | yes | yes | yes |  |  | one line each in plain text; the Markdown golden holds the list markers (REQ-703) | REQ-703, REQ-510 |
| `LST56_INDENTED_NOTE` | yes | yes | yes |  |  | one line each in plain text; the Markdown golden holds the list markers (REQ-703) | REQ-703, REQ-510 |
| `LST56_PLUMS` | yes | yes | yes |  |  | one line each in plain text; the Markdown golden holds the list markers (REQ-703) | REQ-703, REQ-510 |
| `LST56_QUINCES` | yes | yes | yes |  |  | one line each in plain text; the Markdown golden holds the list markers (REQ-703) | REQ-703, REQ-510 |
| `LST56_QUINCE_ONE` | yes | yes | yes |  |  | one line each in plain text; the Markdown golden holds the list markers (REQ-703) | REQ-703, REQ-510 |
| `LST56_NESTED_NOTE` | yes | yes | yes |  |  | one line each in plain text; the Markdown golden holds the list markers (REQ-703) | REQ-703, REQ-510 |
| `LST56_QUINCE_TWO` | yes | yes | yes |  |  | one line each in plain text; the Markdown golden holds the list markers (REQ-703) | REQ-703, REQ-510 |
| `LST56_OTHER_LIST` | yes | yes | yes |  |  | one line each in plain text; the Markdown golden holds the list markers (REQ-703) | REQ-703, REQ-510 |

### Case 57: an embedded workbook with hidden sheets

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `SHT57_SHOWN` | – | – | – | `embedded: true` | yes | the first sheet, visible | REQ-512, REQ-507 |
| `SHT57_HIDDEN` | – | – | – | `embedded: true; hidden: true` | yes | sheet state hidden: absent under embedded: true alone; read under hidden: true, as hidden text is | REQ-512, REQ-507 |
| `SHT57_VERY_HIDDEN` | – | – | – | `embedded: true; hidden: true` | yes | sheet state veryHidden: as the hidden sheet | REQ-512, REQ-507 |
| `SHT57_LAST` | – | – | – | `embedded: true` | yes | the last sheet, visible after the hidden two | REQ-512, REQ-507 |

### Case 58: formatting: hidden text, tiny text, colours at the threshold, a collapsed heading

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `VAN58_SHOWN` | yes | yes | yes |  |  | w:vanish false over a hidden character style: shown; no inspection finding | REQ-902, REQ-507 |
| `VAN58_INHERITED` | – | – | – | `hidden: true` | yes | hidden through its character style; inspection: text.hidden | REQ-902, REQ-507 |
| `PAR58_VISIBLE` | yes | yes | yes |  |  | the shown part of a partly hidden paragraph | REQ-902, REQ-507 |
| `PAR58_PART_HIDDEN` | – | – | – | `hidden: true` | yes | inspection: text.hidden (its paragraph mark is not hidden) | REQ-902, REQ-507 |
| `PAR58_WHOLE` | – | – | – | `hidden: true` | yes | runs and mark hidden; inspection: paragraph.hidden, one finding | REQ-902, REQ-507 |
| `SZ58_TWO` | yes | yes | yes |  |  | exactly 2pt: no inspection finding | REQ-902, REQ-507 |
| `SZ58_ONE_HALF` | yes | yes | yes |  |  | 1.5pt; inspection: text.tiny | REQ-902, REQ-507 |
| `SZ58_COMPLEX` | yes | yes | yes |  |  | w:cs: drawn at w:szCs, 1.5pt; inspection: text.tiny | REQ-902, REQ-507 |
| `SZ58_NOT_COMPLEX` | yes | yes | yes |  |  | drawn at w:sz, 12pt (w:szCs 1.5pt unused): no inspection finding | REQ-902, REQ-507 |
| `COL58_NEAR` | yes | yes | yes |  |  | D3D3D3 on white, contrast 1.497:1; inspection: text.near_background | REQ-902, REQ-507 |
| `COL58_FAR` | yes | yes | yes |  |  | D2D2D2 on white, contrast 1.512:1: no inspection finding | REQ-902, REQ-507 |
| `HEAD58_COLLAPSED` | yes | yes | yes |  |  | a heading 3 collapsed by default (w15:collapsed) | REQ-902, REQ-507 |
| `BODY58_FIRST` | yes | yes | yes |  |  | inspection: paragraph.collapsed, from here to BODY58_LAST | REQ-902, REQ-507 |
| `HEAD58_NESTED` | yes | yes | yes |  |  | a collapsed heading inside the collapsed body: no finding of its own | REQ-902, REQ-507 |
| `BODY58_LAST` | yes | yes | yes |  |  | the collapsed body ends here, at the next heading 3 | REQ-902, REQ-507 |
| `HEAD58_AFTER` | yes | yes | yes |  |  |  | REQ-902, REQ-507 |

### Case 59: geometry: off the page, partly on it, flat, indeterminate, aligned, hidden, in an unused header

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `GEO59_OFF_PAGE` | yes | yes | yes | `text_boxes: false` | – | page-relative, wholly right of the page; inspection: shape.off_page | REQ-902, REQ-518 |
| `GEO59_PARTLY` | yes | yes | yes | `text_boxes: false` | – | page-relative, partly on the page: no inspection finding | REQ-902, REQ-518 |
| `GEO59_FLAT` | yes | yes | yes | `text_boxes: false` | – | height zero; inspection: shape.zero_size | REQ-902, REQ-518 |
| `GEO59_INDETERMINATE` | yes | yes | yes | `text_boxes: false` | – | 9000000 EMU below its paragraph: on the page or not as the paragraph falls; no finding, undecided | REQ-902, REQ-518 |
| `GEO59_ALIGNED` | yes | yes | yes | `text_boxes: false` | – | wp:align right and top, page-relative: on the page by construction, no finding | REQ-902, REQ-518 |
| `GEO59_HIDDEN` | yes | yes | yes | `text_boxes: false` | – | wp:docPr hidden on an inline box: Word 365 shows it (19 October 2026), so no inspection finding | REQ-902, REQ-518 |
| `HDR59_UNUSED` | – | – | – |  |  | a header no section uses; inspection: story.unused | REQ-902, REQ-518 |
| `HDR59_BOX` | – | – | – |  |  | a page-relative box in the unused header: no section, no page size, undecided | REQ-902, REQ-518 |

### Case 60: characters: supplementary private use, a bidi override split across runs, symbol and icon fonts

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `PUA60_SUPPLEMENTARY` | yes | yes | yes |  |  | followed by U+F0041 (plane 15) in a text font; inspection: chars.private_use | REQ-902, REQ-602 |
| `BIDI60_SPLIT` | yes | yes | yes |  |  | U+202E ends this bold run, U+202C starts the next; inspection: chars.bidi, one finding over both runs | REQ-902, REQ-602 |
| `BIDI60_AFTER` | yes | yes | yes |  |  |  | REQ-902, REQ-602 |
| `SYM60_SYMBOL_FONTS` | yes | yes | yes |  |  | w:sym in Symbol and Wingdings, and U+F04A in a Wingdings run: no inspection finding | REQ-902, REQ-602 |
| `SYM60_TEXT_FONT` | yes | yes | yes |  |  | w:sym U+F041 in Arial, a text font; inspection: chars.private_use | REQ-902, REQ-602 |
| `ICN60_ICON_FONT` | yes | yes | yes |  |  | U+E700 in Segoe MDL2 Assets, an icon font (D17): no inspection finding | REQ-902, REQ-602 |

### Case 61: fields: INCLUDEPICTURE and LINK in both forms, and HYPERLINK to a bookmark, a file and a UNC path

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `FLD61_INCPIC_SIMPLE` | – | – | – | `field_codes: true` | yes | a simple field, keyword in mixed case, argument quoted; inspection: field.code, field.external | REQ-902, REQ-505, REQ-506 |
| `FLD61_SIMPLE_RESULT` | yes | yes | yes |  |  | its cached result | REQ-902, REQ-505, REQ-506 |
| `FLD61_INCPIC_COMPLEX` | – | – | – | `field_codes: true` | yes | a complex field, argument bare; inspection: field.code, field.external | REQ-902, REQ-505, REQ-506 |
| `FLD61_COMPLEX_RESULT` | yes | yes | yes |  |  |  | REQ-902, REQ-505, REQ-506 |
| `FLD61_LINK_BARE` | – | – | – | `field_codes: true` | yes | LINK, simple, keyword lower case, a bare relative path; inspection: field.code, field.external | REQ-902, REQ-505, REQ-506 |
| `FLD61_LINK_SIMPLE_RESULT` | yes | yes | yes |  |  |  | REQ-902, REQ-505, REQ-506 |
| `FLD61_LINK_QUOTED` | – | – | – | `field_codes: true` | yes | LINK, complex, a quoted path; inspection: field.code, field.external | REQ-902, REQ-505, REQ-506 |
| `FLD61_LINK_COMPLEX_RESULT` | yes | yes | yes |  |  |  | REQ-902, REQ-505, REQ-506 |
| `FLD61_TARGET` | yes | yes | yes |  |  | the bookmark the \l link goes to | REQ-902, REQ-505, REQ-506 |
| `FLD61_BOOKMARK_RESULT` | yes | yes | yes |  |  | HYPERLINK \l: inspection: field.code only, neither external nor web | REQ-902, REQ-505, REQ-506 |
| `FLD61_FILE` | – | – | – | `field_codes: true` | yes | HYPERLINK to a file: inspection: field.code, field.external; Markdown also carries the link target | REQ-902, REQ-505, REQ-506 |
| `FLD61_FILE_RESULT` | yes | yes | yes |  |  |  | REQ-902, REQ-505, REQ-506 |
| `FLD61_UNC` | – | – | – | `field_codes: true` | yes | HYPERLINK to a UNC path: inspection: field.code, field.external; Markdown also carries the link target | REQ-902, REQ-505, REQ-506 |
| `FLD61_UNC_RESULT` | yes | yes | yes |  |  |  | REQ-902, REQ-505, REQ-506 |

### Case 62: backgrounds: highlight, shading, auto, a table style, the page colour, a pattern

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `BG62_HIGHLIGHT` | yes | yes | yes |  |  | yellow on its yellow highlight; inspection: text.near_background | REQ-902 |
| `BG62_HIGHLIGHT_OVER_SHADING` | yes | yes | yes |  |  | white on a white highlight over black shading (the highlight is drawn); inspection: text.near_background | REQ-902 |
| `BG62_SHADED_CONTRAST` | yes | yes | yes |  |  | white on black shading: no inspection finding | REQ-902 |
| `BG62_AUTO_COLOUR` | yes | yes | yes |  |  | an auto colour on black shading (Word draws it in contrast): no inspection finding | REQ-902 |
| `BG62_AUTO_FILL` | yes | yes | yes |  |  | C00000 on a run fill of auto over its paragraph's C00000; inspection: text.near_background | REQ-902 |
| `BG62_TABLE_STYLE` | yes | yes | yes |  |  | 1F3864 on its table style's 1F3864 cell shading; inspection: text.near_background | REQ-902 |
| `BG62_PAGE` | yes | yes | yes |  |  | the page colour on the page colour (Word 365 shows the page colour); inspection: text.near_background | REQ-902 |
| `BG62_PAGE_NOT_WHITE` | yes | yes | yes |  |  | C8C8C8: 1.67:1 on white, 1.44:1 on the page colour; inspection: text.near_background | REQ-902 |
| `BG62_PATTERN` | yes | yes | yes |  |  | white on a 50% pattern: a background docx4j cannot determine; no finding, undecided | REQ-902 |

### Case 63: inventory: an unknown part, notes, comments, docVars, people, ink, a watermark, a scripted chunk, an embedded document holding hidden text

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `NOTE63_HERE` | yes | yes | yes |  |  |  | REQ-902, REQ-503, REQ-504, REQ-512, REQ-513 |
| `NOTE63_REFERENCED` | yes | yes | yes | `notes: false` | – | a footnote; inspection: note | REQ-902, REQ-503, REQ-504, REQ-512, REQ-513 |
| `NOTE63_UNREFERENCED` | – | – | – |  |  | a footnote no reference reaches; inspection: story.unused | REQ-902, REQ-503, REQ-504, REQ-512, REQ-513 |
| `CMT63_HERE` | yes | yes | yes |  |  |  | REQ-902, REQ-503, REQ-504, REQ-512, REQ-513 |
| `CMT63_ANCHORED` | – | – | `{>>…<<}` | `comments: true` | `{>>…<<}` | inspection: comment | REQ-902, REQ-503, REQ-504, REQ-512, REQ-513 |
| `CMT63_UNANCHORED` | – | – | – |  |  | a comment with no anchor; inspection: story.unused | REQ-902, REQ-503, REQ-504, REQ-512, REQ-513 |
| `INK63_HERE` | yes | yes | yes |  |  | an ink part (w14:contentPart) after it; inspection: ink | REQ-902, REQ-503, REQ-504, REQ-512, REQ-513 |
| `CHK63_SCRIPTED` | – | – | – | `html reader present` | yes | an HTML chunk holding a script, an event handler and two external references; inspection: altchunk, altchunk.script, altchunk.external_ref; nothing fetched; without an HTML reader: altchunk.not_expanded (CR-053 D7, pending WD 0.26) | REQ-902, REQ-503, REQ-504, REQ-512, REQ-513 |
| `EMB63_HERE` | yes | yes | yes |  |  |  | REQ-902, REQ-503, REQ-504, REQ-512, REQ-513 |
| `EMB63_VISIBLE` | – | – | – | `embedded: true` | yes | the embedded document's shown text | REQ-902, REQ-503, REQ-504, REQ-512, REQ-513 |
| `EMB63_HIDDEN` | – | – | – | `embedded: true; hidden: true` | yes | hidden text inside the embedded document; inspection: text.hidden in its chain | REQ-902, REQ-503, REQ-504, REQ-512, REQ-513 |
| `VAR63_VALUE` | – | – | – |  |  | w:docVars; inspection: doc_vars | REQ-902, REQ-503, REQ-504, REQ-512, REQ-513 |
| `HDR63_USED` | – | – | – | `include: headers` | yes | the header the section uses; inspection: header_footer | REQ-902, REQ-503, REQ-504, REQ-512, REQ-513 |
| `WMK63_WATERMARK` | – | – | – | `include: headers` | yes | its watermark (PowerPlusWaterMarkObject, v:textpath); inspection: watermark | REQ-902, REQ-503, REQ-504, REQ-512, REQ-513 |

### Case 64: links: w:hyperlink to a file, a UNC path, the web, mail, and a bookmark

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `LNK64_FILE_TEXT` | yes | yes | yes |  |  | a w:hyperlink to a file; inspection: relationship.external | REQ-902, REQ-703 |
| `LNK64_UNC_TEXT` | yes | yes | yes |  |  | a w:hyperlink to a UNC path; inspection: relationship.external | REQ-902, REQ-703 |
| `LNK64_WEB_TEXT` | yes | yes | yes |  |  | https; inspection: hyperlink.web | REQ-902, REQ-703 |
| `LNK64_MAIL_TEXT` | yes | yes | yes |  |  | mailto; inspection: hyperlink.web | REQ-902, REQ-703 |
| `LNK64_BOOKMARK_TEXT` | yes | yes | yes |  |  | w:anchor: no inspection finding | REQ-902, REQ-703 |

### Case 65: embedded fonts that draw text as other text or as nothing, a careful swap, symbol and plain faces

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `FNT65_WORD` | yes | yes | yes |  |  | CAT drawn DOG: C an over-wide glyph drawing DOG, A and T blank; inspection: font.glyph_mismatch, font.glyph_blank | REQ-902, REQ-602 |
| `FNT65_TWIN` | yes | yes | yes |  |  | as FNT65_WORD, in a face named like the unaltered one support/inspect-fonts gives to install | REQ-902, REQ-602 |
| `FNT65_BLANK` | yes | yes | yes |  |  | INVISIBLE INK, every letter blank; inspection: font.glyph_blank | REQ-902, REQ-602 |
| `FNT65_ZERO` | yes | yes | yes |  |  | MIXED, X at zero advance with an outline; inspection: font.glyph_mismatch | REQ-902, REQ-602 |
| `FNT65_NAMED` | yes | yes | yes |  |  | MAT drawn AMT, the glyphs' names kept; inspection: font.glyph_mismatch | REQ-902, REQ-602 |
| `FNT65_SHARED` | yes | yes | yes |  |  | FEE drawn FFF, E sharing F's glyph; inspection: font.glyph_mismatch | REQ-902, REQ-602 |
| `FNT65_CAREFUL` | yes | yes | yes |  |  | SIZE drawn ZISE, a careful swap: font.embedded only, a limitation | REQ-902, REQ-602 |
| `FNT65_PLAIN` | yes | yes | yes |  |  | PLAIN, the glyphs as made: font.embedded only | REQ-902, REQ-602 |
| `FNT65_SYMBOL` | yes | yes | yes |  |  | U+F041 U+F061 U+F06B in an embedded symbol-encoded face Word 365 does not draw, not on D17's list; inspection: chars.private_use | REQ-902, REQ-602 |
| `FNT65_CCMP` | yes | yes | yes |  |  | BOB drawn POP through ccmp; inspection: font.glyph_mismatch (Word's use of ccmp is the check's to measure) | REQ-902, REQ-602 |
| `FNT65_LIGA` | yes | yes | yes |  |  | DOT drawn OOT through liga, w14:ligatures standard; inspection: font.glyph_mismatch | REQ-902, REQ-602 |
| `FNT65_NO_LIGA` | yes | yes | yes |  |  | ODD, liga not applied (no w14:ligatures): no inspection finding | REQ-902, REQ-602 |
| `FNT65_PUA_PLAIN` | yes | yes | yes |  |  | U+E123 in a face of neither kind, which does not map it; inspection: chars.private_use | REQ-902, REQ-602 |

### Case 66: a picture named and not described, a decorative one, a workbook with a hidden row and column

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `PIC66_NAME_ONLY` | – | – | – | `alt_text: true` | yes | a picture's name, no descr: its description (AltTextDescriber); inspection: alt_text.name | REQ-902, REQ-514, REQ-512 |
| `PIC66_DECOR_NAME` | – | – | – |  |  | a decorative picture's name; inspection: alt_text.name, unextractable | REQ-902, REQ-514, REQ-512 |
| `PIC66_DECOR_DESCR` | – | – | – |  |  | a decorative picture's descr; inspection: alt_text.description, unextractable | REQ-902, REQ-514, REQ-512 |
| `ROW66_SHOWN` | – | – | – | `embedded: true` | yes | A1 | REQ-902, REQ-514, REQ-512 |
| `COL66_HIDDEN_CELL` | – | – | – | `embedded: true` | yes | B1, in hidden column B: read (Embedded.sheet checks the sheet's state only); inspection: embedded.column_hidden | REQ-902, REQ-514, REQ-512 |
| `ROW66_HIDDEN` | – | – | – | `embedded: true` | yes | A2, in hidden row 2: read; inspection: embedded.row_hidden | REQ-902, REQ-514, REQ-512 |

### Case 67: stored content: a Fallback holding near-background text, the stale surface of a bound control holding tag characters

| Token | accepted | original | markup | option | with option | note | statements |
|---|---|---|---|---|---|---|---|
| `MCE67_HERE` | yes | yes | yes |  |  |  | REQ-902, REQ-302, REQ-509 |
| `MCE67_CHOICE` | yes | yes | yes |  |  | the selected Choice (w14) | REQ-902, REQ-302, REQ-509 |
| `MCE67_FALLBACK_WHITE` | – | – | – |  |  | white text in the unselected Fallback; inspection: mce.branch, and text.near_background in it | REQ-902, REQ-302, REQ-509 |
| `SDT67_HERE` | yes | yes | yes |  |  |  | REQ-902, REQ-302, REQ-509 |
| `SDT67_VALUE` | yes | yes | yes | `bindings: surface` | – | the bound value, read by default | REQ-902, REQ-302, REQ-509 |
| `SDT67_SURFACE` | – | – | – | `bindings: surface` | yes | the stale surface, followed by U+E0041 U+E0042, which Word 365 replaces with the bound value on open; inspection: sdt.stored_surface, and chars.tag in it | REQ-902, REQ-302, REQ-509 |

