IF-field probe, round 3 (about 2 minutes): open iffield-probe-r3.docx (no prompt expected; if one, answer No), touch nothing,
Save As Plain Text iffield-probe-r3-word-asis.txt and Save As iffield-probe-r3-word-asis.docx. No F9 needed.

Cases (token: stored -- what it asks):

T01_ALONE: B1  -- a stored-wrong literal IF with no twin: round 2 says it stays B1
T02_STORED: B2  -- stored wrong, a resultless twin follows (T03): the reading says both show A2
T03_TWIN_RESULTLESS: (no result)  -- the resultless twin of T02
T04_TWIN_FIRST_RESULTLESS: (no result)  -- a resultless field before its stored-wrong twin (T05)
T05_STORED_AFTER: B3  -- stored wrong, its resultless twin before it
T06_FORMULA_STORED: 7  -- a formula stored wrong, a resultless twin follows
T07_FORMULA_TWIN: (no result)  -- the resultless twin of T06
T08_DOCPROPERTY_STORED: Nope  -- a DOCPROPERTY stored wrong, a resultless twin follows
T09_DOCPROPERTY_TWIN: (no result)  -- the resultless twin of T08
T10_STRING_STORED: B10  -- a string IF stored wrong, a resultless twin follows
T11_STRING_TWIN: (no result)  -- the resultless twin of T10
T12_WHITESPACE_STORED: B4  -- stored wrong; its twin (T13) differs only in whitespace
T13_WHITESPACE_TWIN: (no result)  -- the resultless twin with two spaces
T14_RESULTLESS_ALONE: (no result)  -- a resultless field with an instruction of its own: computed to show, and written into the file?
T15_DIFFERENT_BRANCHES: OTHER_B  -- the condition of T02 and T03 with other branches: untouched if the key is the whole instruction
