# Hyperlink style probe (FieldHyperlinkStyle)

Where Word draws the Hyperlink character style and where it leaves it off: `hl-fields.docx`, made
by docx4j-layout-fidelity's `HyperlinkStyleProbes` (2026-10-12), 21 labelled cases (`cases.tsv`):
REF and PAGEREF with and without `\h`, a HYPERLINK field, and the runs of a table of contents in
and out of a hyperlink, with the Hyperlink, FollowedHyperlink, another character style, a style
based on Hyperlink, and direct formatting.  The Hyperlink style is 467886, underlined, bold and
14pt, body text black, regular and 11pt, so each case's font size says whether Word drew it.
Every field is locked, so Word drew the results as written.

`word-readings.tsv` is Word 365's PDF of it (`office-colours.ps1`, SaveAs format 17, on the
Windows VM), read with `docx4j-layout-fidelity/tools/office_colours.py`: per label, the colour of
its text, the rules under it (an underline), its font and size.  `FieldHyperlinkStyleTest` checks
docx4j against it case by case.
