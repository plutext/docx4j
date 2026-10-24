# docprop-case: docx4j #244, DOCPROPERTY names and case

`docprop-case.docx` (built by `build244.py`) has the custom property `Client` = "Acme Ltd" and the title "The Title".
Each marker's field result starts as OLD:

- K1 `DOCPROPERTY Client` (control), K2 `client`, K3 `CLIENT`, K4 `"cLiEnT"`
- K5 `DOCPROPERTY Title` (built-in), K6 `title`
- K7 `DOCPROPERTY Nope` (no such property: Word's error text)

Steps: open in Word 365, select all (Ctrl+A), update fields (F9), then run the `PDFand` macro (it writes
`docprop-case.pdf` and `docprop-case-word365.docx` beside the file). Note anything Word says on opening.
