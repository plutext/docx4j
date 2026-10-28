CR-067 phase 2: Word 365 evaluation of = formulas, COMPARE, IF, SET/REF and table formulas (one file, about 10 minutes)

1. Open expr-probe-r1.docx normally (not Open and Repair). Note the Word build (File > Account > About Word).
2. Ctrl+A, then F9 to update every field. If Word asks anything, note what and answer to continue.
3. Save As expr-probe-r1-word365.docx (keep the .docx format) beside this README. Word's saved file is the oracle;
   there is no need to transcribe every case. In results.txt please note: the Word build; anything Word asked;
   the VM's Windows regional format (Settings > Time & Language > Region > Regional format, and its decimal symbol
   and list separator if you have them handy); and any case whose display surprised you.
4. Optional: Save As PDF expr-probe-r1-word365.pdf (the formatting of the results, if Word changes any).

Cases (token: field -> predicted after F9). 'unmeasured' marks a prediction the probe exists to settle.

Formulas: operators and the default number format
  1. F01_PRECEDENCE: { =2+3*4 } -> 14
  2. F02_PARENS: { =(2+3)*4 } -> 20
  3. F03_DIV: { =10/4 } -> 2.5
  4. F04_THIRD: { =1/3 } -> 0.33 (Word's default rounds to two decimals; unmeasured)
  5. F05_TWO_THIRDS: { =2/3 } -> 0.67
  6. F06_POWER: { =2^10 } -> 1024
  7. F07_ROOT: { =2^0.5 } -> 1.41
  8. F08_UNARY: { =-5+2 } -> -3
  9. F09_PERCENT: { =50% } -> 0.5
 10. F10_TIMES_NEG: { =2*-3 } -> -6
 11. F11_SPACES: { = 5 + 1 } -> 6
 12. F12_BIG: { =1000000*1000000 } -> 1000000000000 (or an overflow error text; unmeasured)
 13. F13_SW_2DP: { =10 \# "0.00" } -> 10.00
 14. F14_SW_3DP: { =1/3 \# "0.000" } -> 0.333
 15. F15_COMPARE_IN_FORMULA: { =3>2 } -> 1
 16. F16_EQUAL_IN_FORMULA: { =3=2 } -> 0

Formulas: error texts
 17. F17_ZERO_DIVIDE: { =1/0 } -> !Zero Divide
 18. F18_UNEXPECTED_END: { =1+ } -> !Unexpected End of Formula
 19. F19_SYNTAX: { =1 2 } -> !Syntax Error, 2
 20. F20_UNDEFINED: { =nope } -> !Undefined Bookmark, nope
 21. F21_BAD_FUNCTION: { =NOPE(1) } -> !Undefined Bookmark, NOPE (or a syntax error; unmeasured)

Formulas: functions
 22. F22_ABS: { =ABS(-3) } -> 3
 23. F23_INT_POS: { =INT(3.7) } -> 3
 24. F24_INT_NEG: { =INT(-3.7) } -> -4 (rounds down; -3 if it truncates; unmeasured)
 25. F25_ROUND_2: { =ROUND(2.345,2) } -> 2.35
 26. F26_ROUND_HALF: { =ROUND(2.5,0) } -> 3
 27. F27_ROUND_NEG_HALF: { =ROUND(-2.5,0) } -> -3 (half away from zero; -2 if half to even; unmeasured)
 28. F28_ROUND_HUNDREDS: { =ROUND(1234.5,-2) } -> 1200
 29. F29_MOD: { =MOD(7,3) } -> 1
 30. F30_MOD_NEG: { =MOD(-7,3) } -> 2 (floored, as Excel); -1 if truncated; unmeasured
 31. F31_SIGN: { =SIGN(-5) } -> -1
 32. F32_SUM: { =SUM(1,2,3) } -> 6
 33. F33_AVERAGE: { =AVERAGE(1,2,4) } -> 2.33
 34. F34_MIN: { =MIN(4,2,8) } -> 2
 35. F35_MAX: { =MAX(4,2,8) } -> 8
 36. F36_COUNT: { =COUNT(1,2,3) } -> 3
 37. F37_PRODUCT: { =PRODUCT(2,3,4) } -> 24
 38. F38_AND: { =AND(1,0) } -> 0
 39. F39_OR: { =OR(1,0) } -> 1
 40. F40_NOT: { =NOT(0) } -> 1
 41. F41_TRUE: { =TRUE } -> 1
 42. F42_FALSE: { =FALSE } -> 0
 43. F43_IF_FUNCTION: { =IF(3>2,10,20) } -> 10
 44. F44_NESTED_FUNCTIONS: { =ROUND(AVERAGE(1,2),0) } -> 2 (1.5 rounds half away from zero; 2 either way if half to even)

Formulas: number forms (the VM's regional format decides; please note it in results.txt)
 45. F45_DECIMAL_POINT: { =1.5+1 } -> 2.5
 46. F46_DECIMAL_COMMA: { =1,5+1 } -> an error (the comma is the list separator; unmeasured)
 47. F47_THOUSANDS: { =1,000+1 } -> 1001 if a thousands separator is read; else an error (unmeasured)
 48. F48_LEADING_ZEROS: { =007+1 } -> 8

Formulas: bookmarks (bookmark x holds the text 5)
 49. F49_BOOKMARK: { =x*2 } -> 10
 50. F50_BOOKMARK_SPACED: { =3 + x } -> 8
 51. F51_BOOKMARK_IN_FUNCTION: { =SUM(x,1) } -> 6
 52. F52_DEFINED: { =DEFINED(x) } -> 1
 53. F53_DEFINED_NOPE: { =DEFINED(nope) } -> 0
 54. F54_NESTED_REF: { ={ REF x }+1 } -> 6 (the nested field's result is the operand)

COMPARE: numbers, strings, wildcards, case
 55. C01_NUM_GT: { COMPARE 5 > 3 } -> 1
 56. C02_NUM_TWO_DIGITS: { COMPARE 10 > 9 } -> 1 (numeric: a string compare would give 0)
 57. C03_QUOTED_NUMBERS: { COMPARE "10" > "9" } -> 1 if quoted numbers still compare as numbers; 0 if as strings (unmeasured)
 58. C04_STRING_ORDER: { COMPARE abc < abd } -> 1
 59. C05_CASE: { COMPARE "abc" = "ABC" } -> 1 if the string compare ignores case; 0 if not (unmeasured)
 60. C06_WILDCARD_STAR: { COMPARE "abc" = "a*" } -> 1
 61. C07_WILDCARD_Q: { COMPARE "abc" = "a?c" } -> 1
 62. C08_WILDCARD_LEFT: { COMPARE "a?c" = "abc" } -> 0 (wildcards only in the right operand)
 63. C09_MIXED: { COMPARE 5 > "abc" } -> 0 (a string compare: "5" sorts before "abc")
 64. C10_BOOKMARK_NAME: { COMPARE x = 5 } -> 0 (x is the string x, not the bookmark; REF x would be 5)
 65. C11_DECIMAL_EQ: { COMPARE 5 = 5.0 } -> 1
 66. C12_LEADING_ZEROS: { COMPARE 007 = 7 } -> 1
 67. C13_THOUSANDS: { COMPARE 1,000 = 1000 } -> 1 if the thousands separator is read; else 0 (unmeasured)
 68. C14_NEGATIVE: { COMPARE -1 < 0 } -> 1
 69. C15_EMPTY: { COMPARE "" = "" } -> 1
 70. C16_NE: { COMPARE 5 <> 4 } -> 1
 71. C17_GE: { COMPARE 5 >= 5 } -> 1
 72. C18_LE: { COMPARE 5 <= 4 } -> 0

IF: the forms
 73. I01_QUOTED: { IF 5 > 3 "yes" "no" } -> yes
 74. I02_BARE: { IF 5 > 3 yes no } -> yes
 75. I03_NO_FALSE_TEXT: { IF 5 = 4 yes } -> empty
 76. I04_MULTI_WORD: { IF 5 = 5 "multi word true" "multi word false" } -> multi word true
 77. I05_NE: { IF 5 <> 4 yes no } -> yes
 78. I06_GE: { IF 5 >= 5 yes no } -> yes
 79. I07_LE: { IF 5 <= 4 yes no } -> no
 80. I08_NO_SPACES: { IF 5=5 yes no } -> an error text, or yes if Word tokenises the operator (unmeasured)
 81. I09_BAD_OPERATOR: { IF 5 ! 5 yes no } -> Error! Unknown op code for conditional.
 82. I10_EXTRA_TEXT: { IF 5 > 3 yes no extra } -> yes
 83. I11_BARE_EXTRA: { IF 5 = 5 a b c } -> a
 84. I12_EMPTY_STRINGS: { IF "" = "" yes no } -> yes
 85. I13_SPACE_IN_STRING: { IF "a b" = "a b" yes no } -> yes
 86. I14_EMPTY_RIGHT: { IF 5 = "" yes no } -> no
 87. I15_FORMAT_SWITCH: { IF 5 > 3 "yes" "no" \* Upper } -> YES

IF: numbers against strings
 88. I16_NUMERIC: { IF 10 > 9 yes no } -> yes (numeric)
 89. I17_QUOTED_NUMBERS: { IF "10" > "9" yes no } -> yes if quoted numbers compare as numbers; no if as strings (unmeasured)
 90. I18_STRING_ORDER: { IF abc < abd yes no } -> yes
 91. I19_MIXED: { IF 5 > "abc" yes no } -> no (a string compare)
 92. I20_DECIMAL: { IF 5.0 = 5 yes no } -> yes
 93. I21_NEGATIVE: { IF -1 < 0 yes no } -> yes
 94. I22_THOUSANDS: { IF 1,000 > 999 yes no } -> yes if the thousands separator is read; else no (unmeasured)

IF: case and wildcards
 95. I23_CASE: { IF "abc" = "ABC" yes no } -> yes if case is ignored; no if not (unmeasured)
 96. I24_WILDCARD_STAR: { IF "abc" = "a*" yes no } -> yes
 97. I25_WILDCARD_Q: { IF "abc" = "a?c" yes no } -> yes
 98. I26_WILDCARD_NE: { IF "abc" <> "a*" yes no } -> no
 99. I27_WILDCARD_LT: { IF "abc" < "a*" yes no } -> no (no wildcard for <: a plain string compare, and * sorts before b)
100. I28_WILDCARD_LEFT: { IF "a?c" = "abc" yes no } -> no

IF: nested fields
101. I29_NESTED_FORMULA: { IF { =2+3 } = 5 yes no } -> yes
102. I30_NESTED_DATE: { IF { DATE \@ yyyy } > 2000 yes no } -> yes
103. I31_NESTED_IN_RESULT: { IF 5 = 5 { =1+1 } no } -> 2
104. I32_NESTED_REF: { IF { REF x } = 5 yes no } -> yes
105. I33_NESTED_BOTH: { IF { =1+1 } < { =1+2 } yes no } -> yes

SET and REF (bookmarks a SET defines)
106. S01_SET: { SET total 42 } -> empty (SET shows nothing)
107. S02_REF_SET: { REF total } -> 42
108. S03_FORMULA_SET: { =total+1 } -> 43
109. S04_IF_REF_SET: { IF { REF total } > 40 yes no } -> yes
110. S05_DEFINED_SET: { =DEFINED(total) } -> 1
111. S06_SET_STRING: { SET greeting "Hello World" } -> empty
112. S07_REF_STRING: { REF greeting } -> Hello World
113. S08_IF_STRING_WILDCARD: { IF { REF greeting } = "Hello*" yes no } -> yes
114. S09_SET_NESTED: { SET n { =6*7 } } -> empty
115. S10_REF_NESTED: { REF n } -> 42

Table (columns A to D, rows 1 to 5): A1..A3 = 1, 2, 3; B1..B3 = 10, 20, 30; C1 = x (text), C2 empty, C3 = 5.
Formula cells (address: field -> predicted):
  D1: { =SUM(LEFT) } -> T_D1 11 (the text x is not a number)
  D2: { =SUM(LEFT) } -> T_D2 22 (the empty cell is nothing)
  D3: { =SUM(LEFT) } -> T_D3 38
  A4: { =SUM(ABOVE) } -> T_A4 6
  B4: { =AVERAGE(ABOVE) } -> T_B4 20
  C4: { =SUM(ABOVE) } -> T_C4 5 (x and the empty cell skipped)
  D4: { =SUM(A1:B2) } -> T_D4 33
  A5: { =A1+B2 } -> T_A5 21
  B5: { =MAX(A1:A3) } -> T_B5 3
  C5: { =COUNT(C1:C3) } -> T_C5 1 (COUNT counts the numbers only; 2 or 3 if it counts cells; unmeasured)
  D5: { =SUM(ABOVE) } -> T_D5 104 (the sum of the four results above it: 11+22+38+33)

Bookmark x holds the text 5 (a plain bookmark in the second paragraph). The SET cases define the bookmarks total, greeting and n.
The document's language is en-US (createPackage's default); the IF and = number forms follow the VM's regional format.
