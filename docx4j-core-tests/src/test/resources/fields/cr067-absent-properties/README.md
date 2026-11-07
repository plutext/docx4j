# A property field with no value

CR-067 section 16. `build_absent_properties.py` builds six files holding the same 36 fields, each `w:dirty` with a stale result of its own; `read_absent_properties.py` reads the bridge's
results as a table. `*-word365.docx` and `*-word365.txt` are Word's saves (16.0.20326.20232, the round `absent-properties`, the prompt to update fields answered Yes, each file twice).
The twins agree but for the clock, in `fp-04`'s and `fp-05`'s CREATEDATE and DOCPROPERTY CreateTime. The word counts differ between files because the fields' own results count.

| File | Parts |
|---|---|
| `fp-00-control.docx` | Core, application and custom, a value for each field |
| `fp-01-no-core.docx`, `fp-02-no-app.docx`, `fp-03-no-custom.docx` | One part gone |
| `fp-04-no-properties.docx` | All three gone |
| `fp-05-properties-missing.docx` | All three there, none of the properties in them |
