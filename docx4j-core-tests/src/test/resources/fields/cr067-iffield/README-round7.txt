IF-field probe, round 7 (about 3 minutes): for each of iffield-probe-r7-date.docx and iffield-probe-r7-time.docx: open
(answer No to any prompt), touch nothing, Save As Plain Text iffield-probe-r7-date-word-asis.txt / iffield-probe-r7-time-word-asis.txt
and Save As ...-word-asis.docx. No F9 needed. The last field of each document is X05.

date:
X01_TRIGGER: 2030  -- the auto-updating field: DATE \@ "yyyy"
X02_STORED: B2  -- stored wrong
X03_STRING_EMPTY: (separate, no result)  -- round 6's W18, no longer last
X04_STORED4: B4  -- stored wrong, second to last field
X05_LAST: B7  -- stored wrong, the LAST field of the document

time:
X01_TRIGGER: 2030  -- the auto-updating field: TIME \@ "yyyy"
X02_STORED: B2  -- stored wrong
X03_STRING_EMPTY: (separate, no result)  -- round 6's W18, no longer last
X04_STORED4: B4  -- stored wrong, second to last field
X05_LAST: B7  -- stored wrong, the LAST field of the document


Word's run (Jason, 2026-10-29 08:26 to 08:27; the two -word-asis.txt/.docx, scored-round7.tsv): TIME triggers the same pass as
DATE; in both files X02, X03 and X04 were updated (round 6's last case, now third, among them) and X05, the last field of the
document, was left as stored. The last-field skip is confirmed. Save order: in every round the plain text was saved before
the docx (the share's timestamps), and the text already held the rewritten values, so the rewrite happened at open.
