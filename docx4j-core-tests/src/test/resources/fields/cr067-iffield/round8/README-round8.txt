IF-field probe, round 8 (one Save As per file, about 10 minutes): for each iffield-probe-r8-<candidate>.docx: open it (answer No
to any prompt), touch nothing, Save As iffield-probe-r8-<candidate>-word-asis.docx (the docx alone is enough: the results are in it;
a plain-text save beside it is welcome but not needed). Each file holds the candidate field (stored stale), then Y02, Y03, Y04,
three literal IFs stored wrong, then Y05, a literal IF stored wrong that is the last field. The candidate triggers Word's
open-time pass if Y02 to Y04 come out as their true branches (A2, A3, A4); Y05 is expected as stored (B5) either way.

Files:
iffield-probe-r8-date.docx: DATE \@ "yyyy"  -- positive control (round 6)
iffield-probe-r8-none.docx: (no candidate)  -- negative control: no candidate, the three IFs and the last field only
iffield-probe-r8-formula.docx: = 1 + 1  -- negative control: a formula
iffield-probe-r8-docproperty.docx: DOCPROPERTY Title  -- a document property (Title is Probe)
iffield-probe-r8-dirty-docproperty.docx: DIRTY DOCPROPERTY Title  -- the same with w:dirty on its begin (Word asks; answer No)
iffield-probe-r8-edittime.docx: EDITTIME
iffield-probe-r8-printdate.docx: PRINTDATE \@ "yyyy"
iffield-probe-r8-savedate.docx: SAVEDATE \@ "yyyy"
iffield-probe-r8-createdate.docx: CREATEDATE \@ "yyyy"
iffield-probe-r8-filename.docx: FILENAME
iffield-probe-r8-filesize.docx: FILESIZE
iffield-probe-r8-numpages.docx: NUMPAGES
iffield-probe-r8-page.docx: PAGE
iffield-probe-r8-sectionpages.docx: SECTIONPAGES
iffield-probe-r8-section.docx: SECTION
iffield-probe-r8-numwords.docx: NUMWORDS
iffield-probe-r8-numchars.docx: NUMCHARS
iffield-probe-r8-username.docx: USERNAME
iffield-probe-r8-userinitials.docx: USERINITIALS
iffield-probe-r8-author.docx: AUTHOR
iffield-probe-r8-title.docx: TITLE
iffield-probe-r8-info.docx: INFO Title
iffield-probe-r8-revnum.docx: REVNUM
iffield-probe-r8-template.docx: TEMPLATE
iffield-probe-r8-docvariable.docx: DOCVARIABLE x  -- a variable that does not exist
iffield-probe-r8-listnum.docx: LISTNUM
iffield-probe-r8-autonum.docx: AUTONUM
iffield-probe-r8-symbol.docx: SYMBOL 169
iffield-probe-r8-seq.docx: SEQ fig
iffield-probe-r8-ref.docx: REF Edited  -- a REF to a bookmark holding NEW
iffield-probe-r8-compare.docx: COMPARE 1 = 1
