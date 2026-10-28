One file, many readings (CR-067; Jason, 2026-10-29): a one-page purchase approval memo whose ten fields make each reader
class tell a different story. Title property: Purchase approval; custom property Amount: 750. The fields, one per sentence,
with the stored result and what each surface is expected to show (measured in the IF-field probe's eight rounds):

sentence | stored (libraries; text models cached) | docx4j fields: update | Word open (as-is txt/docx) | Word PDF export (as-is) | Word F9
1 Prepared in {DATE} | 2020 | 2020 (the WD never computes DATE) | 2026 | 2026 | 2026
2 Net {IF 1=1 30 90} days | 90 | 30 | 30 (recomputed on open: the DATE triggers the pass) | 30 | 30
3 Amount due ${IF 1=1 "{DOCPROPERTY Amount}" 0} | 500 (a reader flattening nesting prints 1500) | 750 | 500 (a nested-branch IF is not recomputed on open) | 500 | 750
4 Discount {=10 as w:fldSimple}% | 10 (a reader skipping w:fldSimple prints nothing) | 10 | 10 | 10 | 10
5 Supplier code matches {IF "ACME-2026" = "ACME-*" yes no} | no | yes | yes | yes | yes
6 Subtotal check {= 2 + 2} | 5 | 4 | 5 (formulas are not recomputed on open) | 5 | 4
7 Approval status {IF 1=1 approved rejected, locked} | rejected | rejected | rejected | rejected | rejected
8 Printed on page {IF {PAGE} = 1 one "two or later"} | two or later | two or later (PAGE not computed) | two or later | one (the print updates PAGE first) | one
9 Reviewer note {IF 1=1 "<hidden>confidential" none} | none | confidential (visible: a deviation) | (nothing: the open removes the result) | (nothing) | confidential as hidden text
10 Reference {DOCPROPERTY Title} | Purchase approval | Purchase approval | Purchase approval | Purchase approval | Purchase approval

Jason's Word steps: open one-file-many-readings.docx, answer No to any prompt, touch nothing; Save As Plain Text
one-file-many-readings-word365-asis.txt, Save As one-file-many-readings-word365-asis.docx, Save As PDF one-file-many-readings-word365-asis.pdf;
then Ctrl+A, F9, Save As Plain Text one-file-many-readings-word365-f9.txt and Save As PDF one-file-many-readings-word365-f9.pdf.
