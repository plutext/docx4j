IF-field probe (CR-067; Jason's question 2026-10-29: can an IF field give different extracted text, updated or not?)

Files: iffield-probe-r1.docx (the probe) and iffield-probe-r1-updatefields.docx (the same with w:updateFields on, so Word
asks on open). Steps, each a Save As Plain Text (Windows encoding, CR/LF, no line-break insertion) and a Save As docx:
(1) open iffield-probe-r1.docx (no prompt expected), touch nothing: Save As iffield-probe-r1-word365-asis.txt and -asis.docx;
(2) open it again, Ctrl+A, F9: Save As iffield-probe-r1-word365-f9.txt and -f9.docx;
(3) open iffield-probe-r1-updatefields.docx, answer Yes to the update prompt: Save As iffield-probe-r1-word365-updatefields-yes.txt;
    open it again, answer No: Save As iffield-probe-r1-word365-updatefields-no.txt.

Expected per case (token: stored/cached | docx4j fields: update | Word F9). The as-is and updatefields-no texts should equal the
stored column (Word's converter writes the stored result, measured 2026-10-29); updatefields-yes should equal Word F9.

C01_LITERAL_IF: REJECTED | APPROVED | APPROVED  -- a literal condition, the stored result the losing branch
C02_SIMPLE_FIELD: REJECTED | APPROVED | APPROVED  -- the same as a w:fldSimple
C03_DOCPROPERTY_COND: TITLE_MISMATCH | TITLE_MATCH | TITLE_MATCH  -- the condition on the Title property (Probe); the nested field's stored result says Nope
C04_DATE_COND: STALE_OUTER | EXPIRED | VALID  -- the condition on a DATE whose stored result is 2030: docx4j's text model never computes DATE (WD), so it reads 2030 and says EXPIRED; Word rewrites DATE on open (2026) and says VALID
C05_FORMULA: 3 | 2 | 2  -- a formula stored wrong
C06_COMPARE: 0 | 1 | 1  -- a comparison stored wrong
C07_LOCKED: REJECTED | REJECTED | REJECTED  -- w:fldLock: no one may update it
C08_NO_RESULT: (nothing) | APPROVED | APPROVED  -- begin, separate, end: no stored result
C09_NO_SEPARATE: (nothing) | APPROVED | APPROVED  -- begin, end, no separate (REQ-505: field.no_result)
C10_NESTED_BRANCH: LIT | Probe | Probe  -- the true branch is a nested DOCPROPERTY (stored Nope)
C11_REF_EDITED: REF_OLD | REF_NEW | REF_NEW  -- the condition on a REF to the bookmark Edited, whose text was changed to NEW after the last update (the paragraph before)
C12_HIDDEN_TRUE: VISIBLE_STALE | HIDDEN_WHEN_UPDATED | (hidden: HIDDEN_WHEN_UPDATED as hidden text, so Word's plain text omits it)  -- the true branch is hidden text (w:vanish on its instruction run): Word gives the result the branch's formatting
C13_HAND_EDITED: NEITHER_BRANCH_TEXT | APPROVED | APPROVED  -- a stored result that is neither branch (hand-edited)
C14_CONTROL_TRUE: APPROVED | APPROVED | APPROVED  -- control: the stored result is the right one
C15_SIMPLE_EMPTY: (nothing) | APPROVED | APPROVED  -- a w:fldSimple with no result
C16_WILDCARD: WILD_MISS | WILD_MATCH | WILD_MATCH  -- a wildcard comparison (measured in phase 2)
