# OMML ⇄ MathML corpus — attribution

The corpus for CR-007-math-omml-mathml (see `../README.md`). Layout:

- `mml/NAME.mml` — the MathML **inputs**, one `<math>` Presentation island each.
- `omml/NAME.omml.xml` — Word's **OMML** for that equation (import expected output).
- `word-mml/NAME.word.mml` — Word's **MathML** for that OMML (export oracle).

The `.docx` Word produced in between are throwaway intermediates (git-ignored,
regenerate via `../MathmlToDocx.bas`).

## Source and licence

Extracted from the **W3C MathML Test Suite, Version 2** (Presentation chapter;
files authored by Wolfram Research), © 2002 World Wide Web Consortium (MIT,
ERCIM, Keio). W3C test suites are provided under the W3C Document Licence and the
W3C 3-clause BSD Licence — see <https://www.w3.org/copyright/test-suites-licenses/>.
These inputs are redistributed under that W3C licensing.

The only change from the originals: the MathML namespace
(`xmlns="http://www.w3.org/1998/Math/MathML"`) is declared on the `<math>` root
(the raw suite islands omit it), and the outer indentation is trimmed.

Downstream fixtures generated *from* these (Word's OMML and Word's MathML — see
the README pipeline) are docx4j's own Apache-2.0 fixtures, not covered by this
notice.

## Selection

Representative cases covering the CR §4 element-correspondence table. Dropped:
elementary-math notation (e.g. `menclose notation='longdiv'`), styling/spacing-
only cases (`mstyle`, `mpadded`, `mspace`, `*S*` colour/font/size variants),
deliberate error cases (`merror*`), empty cases (`mfenced1`, `mrow1`), oversized
tables (`mtable2`), and `maction`.

| file | source (Presentation/…) | exercises → OMML |
|---|---|---|
| `mi1.mml` | TokenElements/mi/mi1 | identifier `mi` + inline `mrow` → CTR |
| `mn1.mml` | TokenElements/mn/mn1 | number `mn` (+ ellipsis) → CTR |
| `mo1.mml` | TokenElements/mo/mo1 | operator `mo` → CTR |
| `mtext1.mml` | TokenElements/mtext/mtext1 | text `mtext` → CTR (nor) |
| `ms1.mml` | TokenElements/ms/ms1 | string literal `ms` → CTR |
| `mimathvariant13.mml` | TokenElements/mi/mimathvariant13 | every `mathvariant` → mathPr sty/scr/nor (in a table) |
| `mfrac1.mml` | GeneralLayout/mfrac/mfrac1 | fraction → CTF |
| `mfrac2.mml` | GeneralLayout/mfrac/mfrac2 | differential ⅆ/ⅆx → CTF |
| `mfracAbevelled16.mml` | GeneralLayout/mfrac/mfracAbevelled16 | bevelled fraction → CTF (skewed) |
| `msqrt5.mml` | GeneralLayout/msqrt-mroot/msqrt5 | square root → CTRad (degHide) |
| `mrootB1.mml` | GeneralLayout/msqrt-mroot/mrootB1 | nth root, nested → CTRad |
| `msub1.mml` | ScriptsAndLimits/msub/msub1 | subscript → CTSSub |
| `msup1.mml` | ScriptsAndLimits/msup/msup1 | superscript → CTSSup (has a `malignmark` Word drops) |
| `msubsup1.mml` | ScriptsAndLimits/msubsup/msubsup1 | ∫₀¹ … → CTSSubSup / CTNary |
| `mmultiscripts1.mml` | ScriptsAndLimits/mmultiscripts/mmultiscripts1 | prescripts → CTSPre |
| `mover1.mml` | ScriptsAndLimits/mover/mover1 | accent (x̂) → CTAcc |
| `mover3.mml` | ScriptsAndLimits/mover/mover3 | ∫ under-limit and `munderover` ∫₀^∞ → CTNary limits |
| `munder1.mml` | ScriptsAndLimits/munder/munder1 | under/over lines → CTBar |
| `munder2.mml` | ScriptsAndLimits/munder/munder2 | underbrace (stretchy) → CTGroupChr |
| `mfenced3.mml` | GeneralLayout/mfenced/mfenced3 | ⟨x, y⟩ → CTD |
| `mfencedAdelims6.mml` | GeneralLayout/mfenced/mfencedAdelims6 | custom open/close/separators → CTD |
| `rec-enclose3.mml` | GeneralLayout/menclose/rec-enclose3 | `menclose notation='radical'` → CTRad |
| `mtable1.mml` | TablesAndMatrices/mtable/mtable1 | 3×3 grid → CTM |
| `rec-mtable1.mml` | TablesAndMatrices/mtable/rec-mtable1 | parenthesised matrix → CTD⊃CTM |
| `mphantomB1.mml` | GeneralLayout/mphantom/mphantomB1 | phantom → CTPhant |

Notes: a few carry minor non-representable bits Word normalises away — `msup1`
has a deprecated `malignmark`; `mimathvariant13`, `mover1/3`, `munder1/2` include
`mtext` "vs" comparison labels. Kept because the math structure is what matters;
compare on semantic equivalence, not byte-identity (per the CR's failure policy).
