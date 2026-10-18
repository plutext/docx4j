# FORMTEXT under `fields: update` (CR-046 phase 2, WD REQ-506)

The Word check of what Word's field update does to a FORMTEXT field, the oracle of
`TextsPhase2Test.formTextAsWordUpdatesIt`. Built by docx4j on 18 October 2026; Jason ran Word
(Microsoft 365) on them the same day. Each has three paragraphs: A, a FORMTEXT whose default text is
FT_DEFAULT, typed as FT_TYPED; B, a FORMTEXT with no default text, typed as FT_TYPED_NO_DEFAULT; C, the
control, a formula `{ = 2+3 }` cached as WRONG.

| File | What it is |
|---|---|
| `cr046-formtext-unprotected.docx` | the document as docx4j built it |
| `cr046-formtext-unprotected-after-f9.docx` | Word's save of it after Ctrl+A, F9: A is FT_DEFAULT, B five en spaces (U+2002), C 5 |
| `cr046-formtext-protected-update-on-open.docx` | protected for filling in forms, with `w:updateFields`: on open Word updated the fields (C became 5) and kept A and B as typed |

Word's screenshots, and the unprotected copy with `w:updateFields` (the control for the protected one,
which Word updated as F9 does), are on the share, `Office 2016\cr046-formtext`.
