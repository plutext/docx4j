Text extraction probe

Built by docx4j (ExtractionProbeBuilder, in docx4j-core-tests). Each case starts with a heading, Case and its number. Marker tokens are upper-case words joined by underscores; the README beside this file says where each should appear.

## Case 1: a text box: a wps Choice and a VML Fallback with different text

The box: 

CHOICE\_ALPHA\_123

## Case 2: a wps Choice and no Fallback

The box: 

CHOICE\_ONLY\_789

## Case 3: a Choice requiring an unknown namespace, and a Fallback, each a run

The runs: SUPPORTED\_DELTA 

## Case 4: two Choices, the first requiring an unknown namespace, the second wps; a Fallback

The box: 

CHOICE2\_WPS\_ETA

## Case 5: two Choices both understood (w14, then wps), each a run; a Fallback

The runs: MCE5\_FIRST\_SUPPORTED 

## Case 6: the wps namespace bound to another prefix (shape2010)

The box: 

MCE6\_REBOUND\_CHOICE

## Case 7: a Choice requiring two namespaces, one unknown (wps zz9)

The box: 

MCE7\_FALLBACK

## Case 8: nested: a text box in a wps Choice whose paragraph holds another AlternateContent

The box: 

MCE8\_OUTER MCE8\_INNER\_CHOICE 

## Case 9: an insertion and a deletion in one paragraph

TC9\_BASE TC9\_INSERTED 

## Case 10: a move from paragraph A to paragraph B, with its range markers

TC10\_PARA\_A 

TC10\_PARA\_B TC10\_MOVED

## Case 11: a deleted paragraph mark between two paragraphs

TC11\_FIRST TC11\_SECOND

## Case 12: an inserted paragraph mark splitting a paragraph

TC12\_ONE 

TC12\_TWO

## Case 13: a table with a plain row, a deleted row and an inserted row

|TC13\_PLAIN\_ROW|
|---|
|TC13\_INSERTED\_ROW|

After the table.

## Case 14: a deletion inside an insertion (a second author deletes part of the first author’s insertion)

TC14\_INS\_KEPT 

## Case 15: an inserted field and a deleted field

Inserted: TC15\_INSERTED\_FIELD Deleted: 

## Case 16: a formatting-only change

A run made bold, the change tracked: **TC16\_FORMAT\_ONLY**

## Case 17: tracked changes in a text box and in a footnote

The box (wps, no mc): 

TC17\_BOX\_BASE TC17\_BOX\_INS 

TC17\_NOTE\_REF\_HERE [^1]

## Case 18: hidden and near-hidden text

A run with w:vanish: 

A run whose character style is hidden: 

White text: HID18\_WHITE\_TEXT

1pt text: HID18\_TINY

w:webHidden: HID18\_WEBHIDDEN

## Case 19: complex fields: a marker in the code and the result, and one in the code only

QUOTE: FLD19\_RESULT

DOCVARIABLE: FLD19\_RESULT\_TWO

## Case 20: a simple field

fldSimple: FLD20\_SIMPLE\_RESULT

## Case 21: hyperlinks: a w:hyperlink with r:id, and a HYPERLINK field

w:hyperlink: [FLD21\_LINK\_TEXT](https://example.com/FLD21_URL_MARK)

HYPERLINK field: [FLD21\_FIELD\_LINK\_TEXT](https://example.com/FLD21_FIELD_URL_MARK)

## Case 22: a content control showing its placeholder

The control: PLC22\_PLACEHOLDER

## Case 23: a content control bound to a custom XML part, its surface text stale

The control: BND23\_XML\_VALUE

## Case 24: a rich text control, and a block-level control in a table cell

Inline: SDT24\_CONTENT

|SDT24\_IN\_CELL|
|---|

After the table.

## Case 25: text boxes and legacy WordArt

A floating DrawingML box: 

TBX25\_FLOAT

An inline DrawingML box: 

TBX25\_INLINE

A VML box, no mc: 

TBX25\_VML\_ONLY

|In a cell: |
|---|

TBX25\_IN\_CELL

Legacy WordArt (v:textpath): 

WA25\_TEXTPATH

## Case 26: headers and footers over two sections, and a header no section uses

Section 1 has a default, a first-page and an even-page header, and a default footer (header1 also holds the box of case 25). The next paragraph starts section 2, on a new page, with its own default header.

The last paragraph of section 1.

The first paragraph of section 2.

## Case 27: a footnote, an endnote, and a footnote whose reference is deleted

FN27\_REF\_HERE [^2]

EN27\_REF\_HERE [^e1]

FN27\_DELETED\_REF\_HERE 

## Case 28: comments: one with a reply, and one resolved

CMT28\_ANCHOR and CMT28\_RESOLVED\_ANCHOR

## Case 29: symbols, a numbered list and a Symbol-font bulleted list

SYM29\_MARK  

1. LST29\_ITEM\_A
2. LST29\_ITEM\_B
3. LST29\_ITEM\_C

- LST29\_BULLET\_ONE
- LST29\_BULLET\_TWO

## Case 30: breaks, a tab and hyphens

BRK30\_BEFORE  
BRK30\_AFTER

  
BRK30\_PAGE

  
BRK30\_COLUMN

TAB30\_LEFT	TAB30\_RIGHT

HY30\_NON‑BREAK HY30\_SO­FT

## Case 31: a table: header row, two-paragraph cell, nested table, merged cell, empty cell

|TBL31\_HEADER|||
|---|---|---|
|TBL31\_CELL\_P1<br>TBL31\_CELL\_P2|TBL31\_NESTED||
|TBL31\_MERGED||TBL31\_LAST\_CELL|

After the table.

## Case 32: an equation with a fraction

$$
MTH32\_A+\frac{MTH32\_B}{2}
$$

## Case 33: pictures: alt text and a title, decorative, and an SVG with text in it

Alt text and title: ![IMG33\_ALT\_TEXT](…)

Decorative: ![](…)

SVG, its PNG fallback in a:blip: ![IMG33\_SVG\_ALT](…)

## Case 34: a chart

The chart: 

\[chart: clustered column; value axis: CHT34\_AXIS\_TITLE\]

CHT34\_TITLE

||CHT34\_SERIES\_ONE|CHT34\_SERIES\_TWO|
|---|---:|---:|
|CHT34\_CAT\_A|3|4|
|CHT34\_CAT\_B|5|2|

## Case 35: SmartArt: a root and two children, stored out of reading order

The diagram: 

SMA35\_ROOT

SMA35\_CHILD\_ONE

SMA35\_CHILD\_TWO

## Case 36: an altChunk holding HTML

After the altChunk.

## Case 37: ruby

Ruby: RBY37\_BASE

## Case 38: document properties and a custom XML part

The core properties hold a title and a subject; the custom XML part case 23 binds to holds one more value.

## Case 39: an embedded workbook

## Case 40: an embedded document containing another embedded document

## Case 41: an OLE package containing a plain-text file

## Case 42: an ActiveX label with a preview

## Case 43: a plain-text altChunk

ALT43\_TEXT\_LINE\_ONE

ALT43\_TEXT\_LINE\_TWO

## Case 44: an MHT altChunk

## Case 45: an RTF altChunk

## Case 46: a Flat OPC altChunk

ALT46\_FLATOPC\_CHUNK

## Case 47: a document altChunk containing an HTML altChunk

ALT47\_DOCX\_CHUNK

## Case 48: inspection kinds and characters preserved in extraction

SHD48\_SHADED ZW48\_SP​LIT TAG48\_MARK󠁁󠁂 

OFF48\_BOX

ZER48\_BOX

SPV48\_SEPARATOR

## Case 49: external fields with cached results

DDE49\_RESULT INC49\_RESULT [Local file link](file:///FLD49_FILE_MARK)

## Case 53: an embedded presentation with groups, a table, a hidden slide and speaker notes

## Case 54: a glossary document with two building-block entries

The glossary holds two entries; neither is inserted into the main story.

[^1]: TC17\_NOTE\_BASE 

[^2]: FN27\_NOTE\_TEXT

[^e1]: EN27\_NOTE\_TEXT
