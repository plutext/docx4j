# mc-root

A part's root other than the one its type has (CR-079; WD REQ-317 to REQ-321), for `McRootTest` and `MainPartRootTest`.

Each `*-word365.docx` is Word 16.0.20326.20232's save of the file of that name, through the bridge. A file with no such save is one Word refused, or for `or-14-comments`
opened through a repair, in a job that left no save. Each was run twice, and CR-079 §1 and §1.1 have what Word did.

| Prefix | Round | Built by |
|---|---|---|
| `rw-` | the root's `mc:AlternateContent`, and a root Word refuses | docx4j-generated-objects-ts |
| `or-` | another story's root in `word/document.xml` | docx4j-core-ts |
| `mu-` | `madeup` | docx4j-core-ts |
| `mr-` | `madeup-root`, `w:madeUp` holding each kind of content | docx4j, `../../../../../docs/developer/change-requests/CR-079-notes/build_mr.py` |
| `c79-` | `cr079-phase0` | docx4j, `CR-079-notes/build_phase0.py` |
| `rq-` | `cr079-root-questions`, the questions Fable's review left | docx4j, `CR-079-notes/build_rq.py` |
