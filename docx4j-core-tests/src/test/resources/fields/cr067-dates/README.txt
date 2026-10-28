CR-067 D12 date forms probe: how Word reads a date-only, an offset-free and a UTC date (one file, about 5 minutes)

1. Open date-forms-r1.docx normally. Note the Word build.
2. Part 1: Ctrl+A, F9. For D01 to D06 write down the text each field shows.
   Predicted: D01 2024-02-03 00:00:00 (a date only is midnight); D02 2025-06-07 08:09:10 as written (no offset: local);
   D03 the UTC time shifted to your zone (03:04:05Z -> 14:04:05 in Sydney); D04 to D06 the same values in Word's default format.
   Unknown: whether Word drops the seconds here too (it read a file's :06 as :00 in the phase 1 probe).
3. Part 2: hover each inserted text (or Review > Reviewing Pane) and write down the date and time Word shows for R01, R02, R03.
   Predicted: R01 2024-02-03 (midnight or no time); R02 2025-06-07 8:09 AM as written; R03 the Z read as local clock, 2025-01-02 3:04 AM
   (Word means w:date as local wall-clock whatever its Z says, per CR-043), or shifted to your zone if Word converts it.
4. Part 3: click each date control; write down the text shown and the date the picker highlights for B01, B02, B03.
   Predicted (CR-058 D6): B01 2024-02-03 00:00; B02 2025-06-07 08:09 nominal; B03 the Z converted to your zone, 2025-01-02 14:04 in Sydney.
5. Save As date-forms-r1-word365.docx beside this README; put your notes in results.txt here.
Core properties: created 2024-02-03 (date only), modified 2025-06-07T08:09:10 (no offset), lastPrinted 2025-01-02T03:04:05Z.
