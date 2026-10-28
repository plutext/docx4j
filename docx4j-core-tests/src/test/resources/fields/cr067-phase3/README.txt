CR-067 phase 3: Word 365 evaluation of INDEX/XE, LISTNUM/AUTONUM, FILENAME/TEMPLATE/FILESIZE, SYMBOL, NUMWORDS/NUMCHARS,
ADDRESSBLOCK/GREETINGLINE (three Word sessions in this folder, about 20 minutes in all)

For each docx: open normally (not Open and Repair), note the Word build (File > Account > About Word) in results.txt,
Ctrl+A, F9 (answer and note anything Word asks), Save As <name>-word365.docx beside this README (keep .docx).
Word's saved files are the oracle; there is no need to transcribe the cases. A PDF of each (Save As PDF) is welcome
but optional. Please note any case whose display surprised you, and the N02/N05 paths if Word shows them differently
from what the file holds.

=== phase3-probe-r1.docx (Ctrl+A, F9, Save As phase3-probe-r1-word365.docx) ===

Index entries (XE marks). Each page starts with a PAGE field so the page can be checked. The entries and what each settles:

Page 1 (PAGE_1 should show 1):
  X01: { XE "Apple" } -> Apple, 1 (with pages 2 and 3 below: Apple, 1, 2, 3)
  X02: { XE "Banana:Yellow" } -> a subentry: Banana / Yellow, 1 (Index 2 style)
  X03: { XE "cherry" } -> cherry, 1 sorted with the C entries (case-insensitive order; unmeasured whether shown as typed)
  X04: { XE "Dog" \t "See Cat" } -> Dog. See Cat (the \k separator defaults to '. '; whether the \t text is italic: unmeasured)
  X05: { XE "Egg" \b } -> Egg, 1 with the 1 bold
  X06: { XE "Fig" \i } -> Fig, 1 with the 1 italic
  X07: { XE "10 items" } -> 10 items, 1 (sorted before 2 items if the sort is by character, after it if numeric; unmeasured)
  X08: { XE "2 items" } -> 2 items, 1
  X09: { XE "éclair" } -> éclair, 1 sorted among the E entries (unmeasured)
  X10: { XE "Apple" } -> no second 1 in Apple's page list

Page 2 (PAGE_2 should show 2):
  X11: { XE "Apple" } -> Apple, 1, 2, 3
  X12: { XE "Banana:Green" } -> Banana / Green, 2 before Yellow, 1 (subentries sorted)
  X13: { XE "Grape" \r GrapeRange } -> Grape, 2–3 (the \g range separator defaults to an en dash)
  X14: { XE "apple" } -> a separate entry apple, 2, or merged into Apple (unmeasured: Word's index is case-sensitive, I predict separate)
  X15: { XE "Banana" } -> Banana, 2 on the parent line, then Green, 2 and Yellow, 1 indented
  X16: { XE "Kiwi" \f "b" } -> only in IDX04 (INDEX \f "b"); absent from the others (unmeasured)
  X17: { XE "Melon:Water:Honeydew" } -> Melon / Water / Honeydew, 2 (Index 3 style for the third level)

Page 3 (PAGE_3 should show 3):
  X18: { XE "Apple" } -> Apple, 1, 2, 3 (three marks, three pages, listed not ranged)
  X19: { XE "Nut" \b \i } -> Nut, 3 with the 3 bold italic
  X20: { XE "Orange" \t "See also Apple" } -> Orange. See also Apple
  X21: { XE "Dog" } -> Dog, 3. See Cat, or the cross-reference wins (unmeasured)
  The bookmark GrapeRange runs from X13's paragraph (page 2) to a paragraph on page 3.

The cases (token: field -> predicted). 'unmeasured' marks what the probe exists to settle.

LISTNUM (the default list NumberDefault, then LegalDefault and OutlineDefault)
  1. L01_LISTNUM: { LISTNUM } -> 1)
  2. L02_LISTNUM: { LISTNUM } -> 2)
  3. L03_LEVEL2: { LISTNUM \l 2 } -> a)
  4. L04_LEVEL3: { LISTNUM \l 3 } -> i)
  5. L05_LEVEL1: { LISTNUM \l 1 } -> 3)
  6. L06_START: { LISTNUM \s 10 } -> 10)
  7. L07_AFTER_START: { LISTNUM } -> 11)
  8. L08_TWO_IN_ONE: { LISTNUM } -> 12) then 13) (two fields in the one paragraph; the second is appended by the builder)
  9. L09_LEGAL: { LISTNUM LegalDefault } -> 1.
 10. L10_LEGAL_L2: { LISTNUM LegalDefault \l 2 } -> 1.1.
 11. L11_LEGAL_L2: { LISTNUM LegalDefault \l 2 } -> 1.2.
 12. L12_LEGAL_L1: { LISTNUM LegalDefault } -> 2.
 13. L13_OUTLINE: { LISTNUM OutlineDefault } -> I.
 14. L14_OUTLINE_L2: { LISTNUM OutlineDefault \l 2 } -> A.
 15. L15_OUTLINE_L3: { LISTNUM OutlineDefault \l 3 } -> 1.
 16. L16_NUMBERDEFAULT: { LISTNUM NumberDefault } -> 14) (the same list as the plain LISTNUM; unmeasured)
 17. L17_L2_START: { LISTNUM \l 2 \s 5 } -> e)
 18. L18_L2_NEXT: { LISTNUM \l 2 } -> f)
 19. L19_L1_AFTER: { LISTNUM } -> 15)

AUTONUM, AUTONUMLGL, AUTONUMOUT (the paragraph style is named with each case)
 20. A01_AUTONUM: { AUTONUM } -> 1.
 21. A02_AUTONUM: { AUTONUM } -> 2.
 22. A03_SEPARATOR: { AUTONUM \s : } -> 3:
 23. A04_AUTONUM: { AUTONUM } -> 4.
 24. A05_LGL_H1: { AUTONUMLGL } -> 1. (Heading 1)
 25. A06_LGL_H2: { AUTONUMLGL } -> 1.1. (Heading 2)
 26. A07_LGL_H2: { AUTONUMLGL } -> 1.2. (Heading 2)
 27. A08_LGL_H1: { AUTONUMLGL } -> 2. (Heading 1)
 28. A09_LGL_H2_NOPERIOD: { AUTONUMLGL \e } -> 2.1 (Heading 2; \e drops the trailing period)
 29. A10_LGL_NORMAL: { AUTONUMLGL } -> a Normal paragraph: 2.1.1. or 3. or 1. (unmeasured)
 30. A11_OUT_H1: { AUTONUMOUT } -> I. (Heading 1; III. if the AUTONUMLGL headings count; unmeasured)
 31. A12_OUT_H2: { AUTONUMOUT } -> A. (Heading 2)
 32. A13_OUT_H3: { AUTONUMOUT } -> 1. (Heading 3)
 33. A14_OUT_H2: { AUTONUMOUT } -> B. (Heading 2)
 34. A15_AUTONUM_AFTER: { AUTONUM } -> 5. (continues A04) or 1. (restarted by the headings between); unmeasured

FILENAME, TEMPLATE, FILESIZE
 35. N01_FILENAME: { FILENAME } -> phase3-probe-r1.docx (the name at F9; the Save As name is not applied until the next update)
 36. N02_FILENAME_PATH: { FILENAME \p } -> the full path on the VM (a drive letter or a UNC; please leave as Word wrote it)
 37. N03_FILENAME_UPPER: { FILENAME \* Upper } -> PHASE3-PROBE-R1.DOCX
 38. N04_TEMPLATE: { TEMPLATE } -> Normal.dotm
 39. N05_TEMPLATE_PATH: { TEMPLATE \p } -> C:\Users\<user>\AppData\Roaming\Microsoft\Templates\Normal.dotm
 40. N06_FILESIZE: { FILESIZE } -> the file's size in bytes as opened (unmeasured: before or after the save)
 41. N07_FILESIZE_KB: { FILESIZE \k } -> the size in kilobytes
 42. N08_FILESIZE_MB: { FILESIZE \m } -> 0 (the size in megabytes, rounded)
 43. N09_DOCPROPERTY_TEMPLATE: { DOCPROPERTY Template } -> Normal.dotm (app.xml's Template after the save)
 44. N10_NUMPAGES: { NUMPAGES } -> the page count (a layout field: a cross-check of the XE pages)

SYMBOL
 45. S01_DECIMAL: { SYMBOL 169 } -> © (how Word stores the result: a w:t, or a w:sym; unmeasured)
 46. S02_HEX: { SYMBOL 0xA9 } -> ©
 47. S03_SYMBOL_FONT: { SYMBOL 169 \f "Symbol" } -> ♥ (code 169 in the Symbol font is the heart suit), stored as w:sym w:font="Symbol" w:char="F0A9" (unmeasured)
 48. S04_WINGDINGS: { SYMBOL 65 \f Wingdings } -> the Wingdings glyph at 65 (a hand), w:sym w:font="Wingdings" w:char="F041" (unmeasured)
 49. S05_UNICODE: { SYMBOL 8364 \u } -> €
 50. S06_UNICODE_HEX: { SYMBOL 0x20AC \u } -> €
 51. S07_SIZE: { SYMBOL 169 \s 24 } -> © at 24 points (w:sz 48 on the result run)
 52. S08_FONT_SIZE_H: { SYMBOL 169 \f "Arial" \s 12 \h } -> © in Arial 12 (\h: no line-height change)
 53. S09_OVER_255: { SYMBOL 300 } -> unmeasured: the low byte (44, a comma), the Unicode character 300 (Ĭ), or an error
 54. S10_LETTER: { SYMBOL 65 } -> A
 55. S11_ANSI: { SYMBOL 65 \a } -> A
 56. S12_COURIER: { SYMBOL 0x41 \u \f "Courier New" } -> A in Courier New
 57. S13_NOT_A_NUMBER: { SYMBOL abc } -> an error text (unmeasured), or nothing
 58. S14_NO_ARGUMENT: { SYMBOL } -> an error text (unmeasured), or nothing

INDEX fields (each regenerates the whole index from the XE marks on pages 1 to 3)
 59. IDX01_PLAIN: { INDEX } -> the entries, one paragraph each in the Index 1/2/3 styles, 'entry, pages' with ', ' separators, no headings, one column (unmeasured: the default column count)
 60. IDX02_WORD_DEFAULT: { INDEX \c "2" \z "1033" } -> the same in two columns (Word's own insertion defaults: continuous section breaks around the index)
 61. IDX03_SWITCHES: { INDEX \e ": " \h "A" \l "; " \g " to " \c "1" } -> a letter heading (Index Heading style) before each initial; 'Apple: 1; 2; 3'; 'Grape: 2 to 3'
 62. IDX04_TYPE_B: { INDEX \f "b" } -> Kiwi, 2 only
 63. IDX05_RUN_IN: { INDEX \r } -> subentries run in: 'Banana, 2: Green, 2; Yellow, 1' (unmeasured: the exact separators)
 64. IDX06_LETTER_RANGE: { INDEX \p "a-c" } -> only the entries starting A to C (Apple, apple, Banana..., cherry); whether 10 items and 2 items appear: unmeasured


=== numwords-probe-r1.docx (Ctrl+A, F9, Save As numwords-probe-r1-word365.docx) ===

Two text paragraphs, a footnote (2 words, 13 characters), a header (3 words), then the cases; the case lines
count too (token, '=', result: 3 words each). Please also note the status bar's word count and the Word Count
dialog's numbers (with and without 'Include textboxes, footnotes and endnotes') in results.txt.
Body (the two texts + the 7 case lines with OLD): 36 words, 212 characters without spaces, 239 with spaces, 9 paragraphs.
The number a case shows replaces OLD (3 characters) so NUMCHARS may differ by a few; a word is any run of non-space
characters ('=' counts; 'Well-known', 'e-mail', 'a@b.com', '1,000', "don't" count one each): unmeasured.

  W01_NUMWORDS: { NUMWORDS } -> 36 (footnote and header excluded), 38 if the footnote counts, 41 if the header counts too (unmeasured)
  W02_NUMCHARS: { NUMCHARS } -> 212 (characters without spaces; unmeasured: with spaces would be 239), +13 if the footnote counts
  W03_PROP_WORDS: { DOCPROPERTY Words } -> the same as W01 (app.xml Words after the save)
  W04_PROP_CHARACTERS: { DOCPROPERTY Characters } -> the same as W02
  W05_PROP_CHARS_SPACES: { DOCPROPERTY CharactersWithSpaces } -> 239 (unmeasured)
  W06_PROP_PARAGRAPHS: { DOCPROPERTY Paragraphs } -> 9
  W07_PROP_LINES: { DOCPROPERTY Lines } -> the layout's line count (unmeasured)
After the save, docProps/app.xml's Words, Characters, CharactersWithSpaces, Paragraphs and Lines are read as well.


=== address-probe-r1.docx + address-data-r1.docx (a mail merge, about 5 minutes) ===

1. Open address-probe-r1.docx normally. Mailings > Select Recipients > Use an Existing List... > address-data-r1.docx.
2. Mailings > Match Fields: the columns are named as Word's address fields, so they should map by themselves; if any
   shows '(not matched)', please note which and match it. Then Finish & Merge > Edit Individual Documents... > All.
3. Save the merged document As address-probe-r1-merged.docx in this folder (keep .docx); close the template without
   saving over it (address-probe-r1-attached.docx if Word insists). Note in results.txt the Word build, anything Word
   asked, and the number of letters (predicted 4).

Data: 1 Mr John Smith Jr, Acme Pty Ltd, 1 Main St, Suite 2, Sydney NSW 2000, Australia; 2 Ms Jane Doe, 2 High St, Auckland 1010,
New Zealand (no suffix, company, address 2 or state); 3 Pat Lee (no title), Beta Inc, 3 Low Rd, Perth WA 6000, Australia;
4 Sam (no title or last name), 4 Side Ln, Hobart TAS 7000, Australia.

Cases (token: field -> predicted, letter by letter; '/' marks a line break inside the block):
  1. M01_FIRST_NAME: { MERGEFIELD "First Name" } -> John; Jane; Pat; Sam (the control)
  2. M02_MERGEREC: { MERGEREC } -> 1; 2; 3; 4
  3. A01_ADDRESSBLOCK: { ADDRESSBLOCK \f "<<_TITLE0_ >><<_FIRST0_>><< _LAST0_>><< _SUFFIX0_>>\n<<_COMPANY_\n>><<_STREET1_\n>><<_STREET2_\n>><<_CITY_>><<, _STATE_>><< _POSTAL_>><<\n_COUNTRY_>>" \l 1033 \c 2 \e "Australia" \d } -> Word's own en-US insertion. Lines (line breaks within one paragraph, unmeasured): 1: Mr John Smith Jr / Acme Pty Ltd / 1 Main St / Suite 2 / Sydney, NSW 2000 (no country: it equals \e); 2: Ms Jane Doe / 2 High St / Auckland 1010 / New Zealand (no state, so no ', '; the country differs from \e); 3: Pat Lee / Beta Inc / 3 Low Rd / Perth, WA 6000; 4: Sam / 4 Side Ln / Hobart, TAS 7000. \d (the destination country's conventions) may reorder record 2 (unmeasured)
  4. A02_COUNTRY_ALWAYS: { ADDRESSBLOCK \f "<<_TITLE0_ >><<_FIRST0_>><< _LAST0_>><< _SUFFIX0_>>\n<<_COMPANY_\n>><<_STREET1_\n>><<_STREET2_\n>><<_CITY_>><<, _STATE_>><< _POSTAL_>><<\n_COUNTRY_>>" \l 1033 \c 1 \e "Australia" } -> as A01 with the country on every record (Australia on 1, 3, 4); no \d
  5. A03_COUNTRY_NEVER: { ADDRESSBLOCK \f "<<_TITLE0_ >><<_FIRST0_>><< _LAST0_>><< _SUFFIX0_>>\n<<_COMPANY_\n>><<_STREET1_\n>><<_STREET2_\n>><<_CITY_>><<, _STATE_>><< _POSTAL_>><<\n_COUNTRY_>>" \l 1033 \c 0 \e "Australia" } -> as A01 with no country on any record (New Zealand dropped from 2)
  6. A04_SHORT_FORMAT: { ADDRESSBLOCK \f "<<_FIRST0_>><< _LAST0_>>\n<<_STREET1_\n>><<_CITY_>>" \l 1033 } -> John Smith / 1 Main St / Sydney; Jane Doe / 2 High St / Auckland; Pat Lee / 3 Low Rd / Perth; Sam / 4 Side Ln / Hobart
  7. A05_NO_SWITCHES: { ADDRESSBLOCK } -> Word's default format (unmeasured; the same as A01's, I predict, with its own country rule)
  8. G01_GREETING: { GREETINGLINE \f "<<_BEFORE_ Dear >><<_TITLE0_ >><<_LAST0_>><<_AFTER_ ,>>" \l 1033 \e "Dear Sir or Madam," } -> Dear Mr Smith,; Dear Ms Doe,; record 3 has no title: Dear Pat Lee, or Dear Lee, (unmeasured); record 4 has no last name: Dear Sir or Madam, (the \e text) or Dear Sam, (unmeasured)
  9. G02_FIRST_NAME: { GREETINGLINE \f "<<_BEFORE_ Dear >><<_FIRST0_>><<_AFTER_ ,>>" \l 1033 \e "Dear friend," } -> Dear John,; Dear Jane,; Dear Pat,; Dear Sam,
 10. G03_FULL: { GREETINGLINE \f "<<_BEFORE_ To >><<_TITLE0_ >><<_FIRST0_>><< _LAST0_>><<_AFTER_ :>>" \l 1033 \e "To whom it may concern:" } -> To Mr John Smith:; To Ms Jane Doe:; To Pat Lee:; To Sam: or the \e text (unmeasured)
 11. G04_NO_SWITCHES: { GREETINGLINE } -> Word's default (unmeasured: 'Dear John,'-style or 'Dear Mr Smith,'-style, and the fallback)
