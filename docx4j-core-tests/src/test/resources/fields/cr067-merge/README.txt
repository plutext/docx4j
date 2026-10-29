CR-067 phase 2: Word 365 mail merge of the merge conditionals (one merge, about 10 minutes)

Files: merge-probe-r1.docx (the template), merge-data-r1.docx (the data source: a table of five records).

1. Open merge-probe-r1.docx normally. Note the Word build.
2. Mailings > Select Recipients > Use an Existing List... > choose merge-data-r1.docx (in this folder). If Word asks
   which table, take the only one. (The fields may now show their cached OLD text; that is fine.)
3. Mailings > Finish & Merge > Edit Individual Documents... > All > OK. Word will prompt for the ASK and FILLIN fields:
   press OK (Enter) to accept each default, and please COUNT the prompts (M16 and M20 should ask once, M18 and M21 once
   per letter; whether the skipped record also prompts is one of the questions).
4. Save the merged document (the new window, 'Letters1') As merge-probe-r1-merged.docx in this folder; keep the
   .docx format. Also Save As PDF merge-probe-r1-merged.pdf if quick. Close the template without saving (or save it
   as merge-probe-r1-attached.docx if Word insists: either way, not over merge-probe-r1.docx).
5. In results.txt: the Word build, the prompt counts, anything Word asked or complained about (a missing column, say),
   and the number of letters (sections) in the merged document (predicted: 3, since record 2 is skipped).

Data: 1 Ann (no middle) 150 Sydney; 2 Bob J 50 Melbourne; 3 Cat (no middle) 200 Perth; 4 Dan K 300 Hobart; 5 Eve (no middle) 120 Darwin.
Predicted letters: record 1; record 3 whose second half pulls record 4 (NEXTIF on City = Perth); record 5. Record 2 is
skipped (SKIPIF Amount < 100).

Cases (token: field -> predicted, letter by letter):
  1. M01_MERGEREC: { MERGEREC } -> the record's number in the data source: 1; 3; 5 (record 2 is skipped by M12, record 4 is pulled in by M16)
  2. M02_MERGESEQ: { MERGESEQ } -> the letter's number among the merged ones: 1; 2; 3 (a skipped record does not count; unmeasured)
  3. M03_NAME: { MERGEFIELD Name } -> Ann; Cat; Eve
  4. M04_NAME_UPPER: { MERGEFIELD Name \* Upper } -> ANN; CAT; EVE
  5. M05_NAME_BEFORE_AFTER: { MERGEFIELD Name \b "Dear " \f "," } -> Dear Ann,; Dear Cat,; Dear Eve,
  6. M06_MIDDLE: { MERGEFIELD Middle } -> empty for all three (Ann, Cat and Eve have no middle initial); whether Word removes the line (suppress blank lines) is unmeasured: compare the merged output's paragraphs
  7. M07_MIDDLE_BEFORE_AFTER: { MERGEFIELD Middle \b "(" \f ")" } -> empty for all three: \b and \f print only around a non-empty value
  8. M08_AMOUNT_FORMAT: { MERGEFIELD Amount \# "$#,##0.00" } -> $150.00; $200.00; $120.00
  9. M09_DOUBLE: { ={ MERGEFIELD Amount }*2 } -> 300; 400; 240
 10. M10_IF_AMOUNT: { IF { MERGEFIELD Amount } >= 100 "big" "small" } -> big; big; big (small would be Bob's, skipped)
 11. M11_COMPARE_CITY: { COMPARE { MERGEFIELD City } = "S*" } -> 1; 0; 0
 12. M12_SKIPIF: { SKIPIF { MERGEFIELD Amount } < 100 } -> empty; the record whose Amount is below 100 (Bob, 50) produces no letter at all
 13. M13_SET_TOTAL: { SET total { ={ MERGEFIELD Amount }+1 } } -> empty on display (the value 151; 201; 121 stored under the bookmark)
 14. M14_REF_TOTAL: { REF total } -> 151; 201; 121
 15. M15_NOPE: { MERGEFIELD Nope } -> Word's text for a field the data source has no column for (unmeasured; an error text, or «Nope»)
 16. M16_ASK_ONCE: { ASK Greeting "Greeting for every letter?" \d "Hello" \o } -> empty on display; Word asks ONCE (\o) at the start of the merge: press OK for the default Hello
 17. M17_REF_GREETING: { REF Greeting } -> Hello in every letter
 18. M18_ASK_EACH: { ASK Prompt2 "Prompt for this record?" \d "Hi" } -> empty on display; Word asks for each letter (three times, or four if it also asks for the skipped record: please count): press OK each time
 19. M19_REF_PROMPT2: { REF Prompt2 } -> Hi in every letter
 20. M20_FILLIN_ONCE: { FILLIN "Your name?" \d "World" \o } -> World in every letter; asked once (press OK)
 21. M21_FILLIN_EACH: { FILLIN "Per letter?" \d "Each" } -> Each in every letter; asked for each letter (press OK each time; count the prompts)
 22. M22_BEFORE_NEXTIF: { MERGEFIELD Name } -> Ann; Cat; Eve (the record before the NEXTIF)
 23. M23_NEXTIF: { NEXTIF { MERGEFIELD City } = "Perth" } -> empty; in Cat's letter (Perth) the rest of the letter takes the next record (Dan) without starting a new letter
 24. M24_AFTER_NEXTIF: { MERGEFIELD Name } -> Ann; Dan; Eve
 25. M25_MERGEREC_AFTER: { MERGEREC } -> 1; 4; 5
 26. M26_MERGESEQ_AFTER: { MERGESEQ } -> 1; 2 or 3 (whether the pulled record counts is unmeasured); 3 or 4
 27. M27_CITY_AFTER: { MERGEFIELD City } -> Sydney; Hobart; Darwin
