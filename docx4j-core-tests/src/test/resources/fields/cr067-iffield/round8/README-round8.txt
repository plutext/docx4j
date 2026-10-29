IF-field probe, round 8: which fields trigger Word's open-time pass (rounds 6 and 7: DATE and TIME do). One file per
candidate, each holding the candidate field stored stale (Y01), three literal IFs stored wrong (Y02 to Y04) and a literal IF
stored wrong as the last field (Y05). The candidate triggers the pass if Y02 to Y04 come out A2, A3, A4; Y05 is expected B5
either way (the last field is skipped, rounds 6 and 7).

WITH THE GOLDEN RUNNER (the 30 files in corpus\; one Word open and save each, the golden PDF as a bonus). On the VM:

  java -Dfidelity.updateFields=false -Dfidelity.resaveOriginalBytes=true -Dfidelity.connectedExperiences=on -cp "X:\fidelity\harness\classes;X:\fidelity\harness\lib\*" org.docx4j.fidelity.golden.WordGoldenRunner X:\__tmp\cr067\iffield\round8\corpus X:\__tmp\cr067\iffield\round8\goldens X:\__tmp\cr067\iffield\round8\resaved

  - updateFields=false: documents4j's plain open and save, no field update in the script, so Word's own open-time pass is what
    the resaved docx records (the script opens with Documents.Open(file, False, True, False): read-only, no conversion prompt).
  - resaveOriginalBytes=true: Word gets each probe's own bytes, not docx4j's re-save of it (the probes are docx4j-built and
    small, so either would do; the bytes make the measurement about Word alone).
  - The goldens\ PDFs are Word's painted text of each file as opened; the resaved\ docx are what Word wrote, the measurement.
  - Controls: date (expected to trigger) and none (expected not). If the runner's COM open agrees with them, the COM open
    equals the GUI open for this purpose and the other 28 answers stand; the scored table says which way it went.

BY HAND (the one file in by-hand\): iffield-probe-r8-dirty-docproperty.docx carries w:dirty on its DOCPROPERTY, which makes
Word ask "update fields?" on open. Under the runner that prompt would either take the default (Yes, updating everything) or
hang the run, so it stays out of the batch: open it in Word, answer No, touch nothing, Save As
by-hand\iffield-probe-r8-dirty-docproperty-word-asis.docx.

If the runner is not to hand, the by-hand steps work for every file in corpus\ too (open, No to any prompt, Save As
<name>-word-asis.docx into resaved\).

Files (candidate -- note):
iffield-probe-r8-date.docx: DATE \@ "yyyy"  -- positive control (round 6)
iffield-probe-r8-none.docx: (no candidate)  -- negative control: no candidate, the three IFs and the last field only
iffield-probe-r8-formula.docx: = 1 + 1  -- negative control: a formula
iffield-probe-r8-docproperty.docx: DOCPROPERTY Title  -- a document property (Title is Probe)
iffield-probe-r8-dirty-docproperty.docx: DIRTY DOCPROPERTY Title  -- the same with w:dirty on its begin (Word asks; answer No)
iffield-probe-r8-edittime.docx: EDITTIME
iffield-probe-r8-printdate.docx: PRINTDATE \@ "yyyy"
iffield-probe-r8-savedate.docx: SAVEDATE \@ "yyyy"
iffield-probe-r8-createdate.docx: CREATEDATE \@ "yyyy"
iffield-probe-r8-filename.docx: FILENAME
iffield-probe-r8-filesize.docx: FILESIZE
iffield-probe-r8-numpages.docx: NUMPAGES
iffield-probe-r8-page.docx: PAGE
iffield-probe-r8-sectionpages.docx: SECTIONPAGES
iffield-probe-r8-section.docx: SECTION
iffield-probe-r8-numwords.docx: NUMWORDS
iffield-probe-r8-numchars.docx: NUMCHARS
iffield-probe-r8-username.docx: USERNAME
iffield-probe-r8-userinitials.docx: USERINITIALS
iffield-probe-r8-author.docx: AUTHOR
iffield-probe-r8-title.docx: TITLE
iffield-probe-r8-info.docx: INFO Title
iffield-probe-r8-revnum.docx: REVNUM
iffield-probe-r8-template.docx: TEMPLATE
iffield-probe-r8-docvariable.docx: DOCVARIABLE x  -- a variable that does not exist
iffield-probe-r8-listnum.docx: LISTNUM
iffield-probe-r8-autonum.docx: AUTONUM
iffield-probe-r8-symbol.docx: SYMBOL 169
iffield-probe-r8-seq.docx: SEQ fig
iffield-probe-r8-ref.docx: REF Edited  -- a REF to a bookmark holding NEW
iffield-probe-r8-compare.docx: COMPARE 1 = 1

BY-HAND RESULT (Jason, 2026-10-29 09:04 to 09:10; by-hand\, scored-round8-by-hand.tsv): the GUI open triggered the pass for
NONE of the seven (page, numpages, section, sectionpages, printdate, ref, seq: every Y as stored, the candidate's stored
result kept, though Word displays PAGE and the others live), nor for the dirty-flag DOCPROPERTY (No to the prompt: nothing
refreshed). So the GUI open equals the automation resave: the open-time triggers are DATE, TIME and SAVEDATE. The seven
extra triggers of the golden PDFs belong to Word's PDF export (a print, which updates the layout fields, PRINTDATE, REF and
SEQ, and then the literal IFs), not to the open.
