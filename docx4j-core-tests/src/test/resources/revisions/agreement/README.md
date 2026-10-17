# The ports' agreement run (CR-043 phase 3)

docx4j's Accept All and Reject All against docx4j-core-ts's and docx4j-python's, over the Word-made fixtures, compared
structurally by `org.docx4j.model.revisions.DocumentDescription` (docx4j-core-tests' test sources). The results, and
which side Word's own saves take where they differ, are CR-043 section 14.

```
node ts-resolve.mjs ../docx4j-core-ts <ts-out> <fixture.docx>...          # core-ts: main body, headers, footers
../docx4j-python/.venv-fork/bin/python py_resolve.py ../docx4j-python <py-out> <fixture.docx>...   # python: every story
java -cp <core-tests test-classes:docx4j-core:deps> org.docx4j.model.revisions.PortsAgreement <out> <ts-out> <py-out> <fixture.docx>...
diff <out>/<name>.accept.docx4j.txt <out>/<name>.accept.ts.txt
```

Each script loads a fixture, resolves it with its port's own API, and saves `<name>.accept.docx` and
`<name>.reject.docx`; `PortsAgreement` prints, per fixture and verb, how many description lines differ.
