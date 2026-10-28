IF-field probe, round 4 (about 2 minutes): open iffield-probe-r4.docx (answer No to any prompt), touch nothing,
Save As Plain Text iffield-probe-r4-word-asis.txt and Save As iffield-probe-r4-word-asis.docx. No F9 needed.

Cases (token: stored -- what it asks):

U01_NOSEP_ALONE: (no separate)  -- a field with no separate, an instruction of its own: computed on open, and written into the file?
U02_STORED: B2  -- stored wrong; a no-separate twin follows (U03)
U03_NOSEP_TWIN: (no separate)  -- the no-separate twin of U02
U04_NOSEP_FIRST: (no separate)  -- a no-separate field before its stored-wrong twin (U05)
U05_STORED_AFTER: B3  -- stored wrong, its no-separate twin before it
U06_OTHER_BRANCHES: OTHER_B  -- U02's condition with other branches
U07_FORMULA_STORED: 7  -- a formula stored wrong; a no-separate twin follows
U08_FORMULA_NOSEP: (no separate)  -- the no-separate twin of U07
U09_DOCPROPERTY_STORED: Nope  -- a DOCPROPERTY stored wrong; a no-separate twin follows
U10_DOCPROPERTY_NOSEP: (no separate)  -- the no-separate twin of U09
U11_EMPTY_STORED: B5  -- stored wrong; a separate-but-empty twin follows (U12): round 3 says both stay
U12_EMPTY_TWIN: (separate, no result)  -- the separate-but-empty twin of U11
U13_WILDCARD_STORED: WILD_MISS  -- the wildcard IF stored wrong; a no-separate twin follows
U14_WILDCARD_NOSEP: (no separate)  -- the no-separate twin of U13
U15_CONTROL: B1  -- a stored-wrong IF with no twin of any kind

Word's run (Jason, 2026-10-29 08:11; iffield-probe-r4-word-asis.txt/.docx, scored-round4.tsv): no prompt; nothing recomputed; a
field with no separate shows nothing and is left as it is in the file (no separate added); every stored result kept; no XML
changed. So neither a resultless nor a separate-less field makes Word compute anything on open. What round 1 had that rounds 2
to 4 never did: a field holding the RIGHT result (C14, APPROVED) in the same instruction family as the rewritten ones. Round 5
tests whether Word shares a stored result across fields with identical instructions, and which one wins.
