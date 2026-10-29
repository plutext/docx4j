IF-field probe, round 6 (about 2 minutes): open iffield-probe-r6.docx (answer No to any prompt), touch nothing,
Save As Plain Text iffield-probe-r6-word-asis.txt and Save As iffield-probe-r6-word-asis.docx. No F9 needed.

Cases (token: stored -- what it asks):

W01_DATE: 2030  -- the DATE: Word refreshes it on open (2026)
W02_STORED: B2  -- stored wrong; an empty twin follows (W03)
W03_TWIN_EMPTY: (separate, no result)  -- a separate with no result, W02's twin
W04_ALONE: B1  -- stored wrong, no twin
W05_NOSEP_ALONE: (no separate)  -- no separate, no twin
W06_STORED3: B3  -- stored wrong; a separate-less twin follows (W07)
W07_NOSEP_TWIN: (no separate)  -- W06's twin
W08_FORMULA_STORED: 7  -- a formula stored wrong; an empty twin follows
W09_FORMULA_EMPTY: (separate, no result)
W10_DOCPROPERTY_STORED: Nope  -- a DOCPROPERTY stored wrong; an empty twin follows
W11_DOCPROPERTY_EMPTY: (separate, no result)
W12_WILDCARD_EMPTY: (separate, no result)  -- the wildcard IF with no result, alone
W13_HIDDEN_BRANCH: VISIBLE_STALE  -- round 1's C12 beside a DATE
W14_COMPARE_EMPTY: (separate, no result)  -- a COMPARE with no result
W15_IF_OVER_DATE: STALE_OUTER  -- round 1's C04
W16_EMPTY_ALONE: (separate, no result)  -- a separate with no result, no twin
W17_STRING_STORED: B10  -- a string IF stored wrong; an empty twin follows
W18_STRING_EMPTY: (separate, no result)

Word's run (Jason, 2026-10-29 08:22; iffield-probe-r6-word-asis.txt/.docx, scored-round6.tsv): with the DATE present, Word's open
rewrote the DATE (2026), the IF over it (VALID), and every IF with literal operands but the last field of the document: the
stored-wrong ones (W02 A2, W04 A1, W06 A3, W17 A10), the empty ones (W03 A2, W16 A8, W12 WILD_MATCH), the separate-less ones
(W05 A9 and W07 A3, given a separate), and the hidden-branch one (W13: separate and result removed, as round 1's C12). It left
the formula (7, empty), the DOCPROPERTY (Nope, empty) and the COMPARE (empty) alone, and W18, the last field, empty; round 1's
last field (C16, the wildcard IF) was the one literal IF it left alone there. Round 7 adds two literal IFs after W18 to test
the last-field exclusion.
