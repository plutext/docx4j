IF-field probe, round 2 (about 3 minutes): open iffield-probe-r2.docx, touch nothing, Save As Plain Text
iffield-probe-r2-word365-asis.txt and Save As iffield-probe-r2-word365-asis.docx; then Ctrl+A, F9, Save As Plain Text
iffield-probe-r2-word365-f9.txt. Cases (token: the stored result -- what the case asks):

R01_STRING_EQ: STR_MISS  -- a string equality, no wildcard: recomputed on open?
R02_WILDCARD_TRUE_STORED: WILD_MATCH  -- the wildcard IF with the true branch stored: an open-time evaluation without wildcards would flip it to WILD_MISS
R03_NUMERIC_GT: NUM_LE  -- a numeric comparison with another operator
R04_FALSE_COND: WRONG  -- a false condition with the true branch stored
R05_STRING_NE: NE_FALSE  -- a string inequality
R06_CASE: CASE_MATCH  -- case: F9 said a string comparison is case-sensitive (phase 2); the true branch stored
R07_OVER_FORMULA: FORMULA_FALSE  -- an IF over a formula whose stored result is wrong
R08_OVER_PAGE: PAGE_OTHER  -- an IF over PAGE (a layout field)
R09_OVER_RIGHT_DOCPROPERTY: TITLE_MISMATCH  -- an IF over a DOCPROPERTY whose stored result is already right, the outer's wrong
R10_OVER_NUMPAGES: MORE_PAGES  -- an IF over NUMPAGES
R11_DIRTY_WILDCARD: WILD_MISS  -- the wildcard IF with w:dirty on its begin
R12_DIRTY_FORMULA: 3  -- a formula with w:dirty on its begin
R13_DIRTY_DOCPROPERTY: Nope  -- a DOCPROPERTY with w:dirty on its begin
R14_SIMPLE_DIRTY: 3  -- a w:fldSimple formula with w:dirty
R15_COMPARE_EQ: 0  -- COMPARE with the literal operands of C01
R16_IF_NUMBERS_STRINGS: QUOTED_MISS  -- the literal IF with its numbers quoted
R17_SIMPLE_WILDCARD: WILD_MISS  -- the wildcard IF as a w:fldSimple
R18_DOCVARIABLE: VAR_X  -- an IF over a DOCVARIABLE that does not exist (stored x)

Word's run (Jason, 2026-10-29 07:50 to 07:52; iffield-probe-r2-word-asis.txt/.docx, -f9.txt, -word365-dirty-yes.txt; scored-round2.tsv):
Word asked "Do you want to update fields" on open (the w:dirty cases); answered No, it recomputed NONE of the 18 cases: every
as-is text is the stored result. Its save changed only the shape: w:dirty cleared (R11 to R14), the w:fldSimple fields
rewritten as complex fields (R14, R17), a nested field's stored result rewritten as w:instrText (Word's own form). Yes to the
prompt updated every field in the document, not only the dirty ones (an IF over PAGE then said PAGE_OTHER, where F9 says
PAGE_ONE). F9 computed every case. So round 1's open-time rewrite was not a rule about literal IFs: round 1 held fields with
NO result (C08, C09), which Word had to compute to show anything, and every field sharing their exact instruction
(C01, C02, C13, C15: IF 1 = 1 "APPROVED" "REJECTED") came out with the same new result; C04's rewrite follows its DATE,
which Word updates on open. Round 3 tests that reading.
