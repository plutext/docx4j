IF-field probe, round 5 (about 2 minutes): open iffield-probe-r5.docx (answer No to any prompt), touch nothing,
Save As Plain Text iffield-probe-r5-word-asis.txt and Save As iffield-probe-r5-word-asis.docx. No F9 needed.

Cases (token: stored -- what it asks):

V01_RIGHT_FIRST: A6  -- the right result first; its twin V02 holds the wrong one
V02_WRONG_LAST: B6  -- the wrong result last: if the last wins, both show B6; if the right one spreads, both show A6
V03_WRONG_FIRST: B7  -- the wrong result first
V04_RIGHT_LAST: A7  -- the right result last
V05_THREE_X: X8  -- three twins with three results that are no branch
V06_THREE_Y: Y8
V07_THREE_Z: Z8
V08_FORMULA_WRONG: 9  -- a formula stored wrong; its twin V09 right
V09_FORMULA_RIGHT: 8
V10_DOCPROPERTY_WRONG: Nope  -- a DOCPROPERTY stored wrong; its twin V11 right
V11_DOCPROPERTY_RIGHT: Probe
V12_WILDCARD_WRONG: WILD_MISS  -- the wildcard IF stored wrong; its twin V13 right
V13_WILDCARD_RIGHT: WILD_MATCH
V14_WRONG_WITH_LOCKED_TWIN: B1  -- stored wrong; its twin V15 is locked and right
V15_LOCKED_RIGHT: A1  -- locked, right
V16_WRONG_THEN_EMPTY: B2  -- stored wrong; its twin V17 has an empty result, last
V17_EMPTY_LAST: (separate, no result)  -- an empty result last
V18_HIDDEN_BRANCH_ALONE: VISIBLE_STALE  -- round 1's C12 alone: its true branch hidden text in the code; Word's open removed its separate and result
V19_HIDDEN_RESULT: B3 (hidden)  -- the stored result run hidden, the code plain
V20_WRONG_COMPLEX: B5  -- stored wrong; its twin V21 is a w:fldSimple holding the right result
V21_RIGHT_SIMPLE: A5  -- the simple-field twin, right
V22_SPACED_RIGHT: A6  -- V01's instruction with extra spaces, right: does whitespace separate families?

Word's run (Jason, 2026-10-29 08:16; iffield-probe-r5-word-asis.txt/.docx, scored-round5.tsv): no sharing. Every field kept its
own stored result (the right twin first or last, three different non-branch results, formula, DOCPROPERTY, wildcard, locked,
empty last, simple-field twin, extra spaces); the hidden-branch replica alone kept its separate and result; the hidden result
run stayed hidden (nothing in the text); the only XML change: the w:fldSimple rewritten as a complex field. So round 1's rewrite
is neither a computation nor a sharing of stored results. What round 1 alone contained: a DATE field, which Word refreshes on
open. Round 6 puts a DATE beside the shapes of rounds 3 and 4.
