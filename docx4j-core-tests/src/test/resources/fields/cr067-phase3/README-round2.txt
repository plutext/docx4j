CR-067 phase 3, round 2 (about 10 minutes):

A. phase3-probe-r1-word365.docx (round 1): please open it and Save As PDF phase3-probe-r1-word365.pdf. Word writes no
   result for LISTNUM, AUTONUM and SYMBOL into the file (they still say OLD in it), so the PDF is the only record of
   L10 to L19, A01 to A15 and S01 to S14. If Word asks to update fields on open, say no (or yes: F9 gives the same).

B. listnum-probe-r2.docx: open, Ctrl+A, F9, Save As listnum-probe-r2-word365.docx and Save As PDF listnum-probe-r2-word365.pdf.
   Cases (token: field -> predicted):
     1. R01_LISTNUM: { LISTNUM } -> 1) if the default level in a heading-free document is 1; i) if it is always 3 (unmeasured)
     2. R02_LISTNUM: { LISTNUM } -> 2) (or ii))
     3. R03_LEVEL3: { LISTNUM \l 3 } -> i) (a level started under level 2 = 1, the implicit parent)
     4. R04_LEVEL2: { LISTNUM \l 2 } -> b) if R03 started an implicit level-2 item (round 1's reading); a) if not
     5. R05_LEGAL: { LISTNUM LegalDefault } -> the level-1 counter continued in the legal format: 3. (the counters are shared, round 1)
       (H1: a Heading 1 paragraph with no field)
     6. R06_AFTER_H1: { LISTNUM } -> a) if the default level is the preceding heading's level + 1; else the level-1 count continues
       (H2: a Heading 2 paragraph with no field)
     7. R07_AFTER_H2: { LISTNUM } -> i) if the default level is the preceding heading's level + 1
     8. R08_IN_H1 (Heading1): { LISTNUM } -> the field inside a Heading 1 paragraph: 1) at level 1, or a) (unmeasured)
     9. R09_LEVEL1: { LISTNUM \l 1 } -> the level-1 counter + 1
       (H3: a Heading 3 paragraph with no field)
    10. R10_AFTER_H3: { LISTNUM } -> (1) if the default level is the heading's level + 1 = 4
    11. R11_AUTONUM: { AUTONUM } -> 1.
    12. R12_AUTONUM: { AUTONUM } -> 2.
       (H1b: a Heading 1 paragraph with no field)
    13. R13_AUTONUM_AFTER_H1: { AUTONUM } -> 3. if AUTONUM counts on across a heading; 1. if a heading restarts it (unmeasured)
    14. R14_AUTONUM_IN_H1 (Heading1): { AUTONUM } -> a Heading 1 paragraph holding the field: 4., or 1. (unmeasured)
    15. R15_AUTONUMLGL: { AUTONUMLGL } -> in Normal after a Heading 1: 1.1. or 1. (unmeasured)

C. address-probe-r1.docx: open it, attach address-data-r1.docx again (Select Recipients > Use an Existing List), then
   at the end of the document insert Mailings > Address Block (OK with the defaults) and Mailings > Greeting Line (OK with
   the defaults), and Save As address-probe-r1-attached.docx (keep .docx). This records Word's own \f format strings and
   its field mapping (settings.xml), which round 1's merged output cannot show: in it the \n inside a token came out as
   the letter n (A01 to A04), and G03 fell back to its \e text for every record.
