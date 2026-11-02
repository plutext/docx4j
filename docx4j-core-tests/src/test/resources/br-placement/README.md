# The br-placement probe

Where Word reads a `w:br` that is not inside a `w:r`. docx4j's load survey of the Common Crawl corpus
(docx4j-load-survey, 2 November 2026: 3,722,083 documents) found one in 206,351 documents, all of which needed
the mc preprocessor, and docx4j drops the break, or the text with it. The places it was found, by documents
(portfolio `docs/docx_corpus_use.md` §8.14; the producers there):

| Place | Documents | Commonest producer |
|---|---|---|
| `body/p/br` | 195,642 | a web-to-docx generator calling itself "Microsoft Word 12.0.0" (182,706 documents in all) |
| `body/p/r/t/br` | 7,398 | PHPWord (text with `<w:br/>` in it); a template filler (`w:t` nested in `w:t`) |
| `body/tbl/tr/tc/p/br` | 2,272 | |
| `body/tbl/tr/tc/p/r/t/br` | 1,026 | |
| `body/p/hyperlink/br` | 603 | |
| `ftr/p/br` | 332 | |
| `hdr/p/r/instrText/br` | 29 | (not probed) |
| `body/br` | 10 | (not probed) |

One small file a case, so that a repair in one could not hide the others. Each reads "BRn before" and "BRn after"
either side of the `w:br`. Built by `build_br_placement.py` (run in this directory: `python3 build_br_placement.py .`;
a rebuild is byte-identical; the files Word was given had the same parts, with other zip dates).

## Cases, and what Word and docx4j read

Word 365 (Jason's run, 3 November 2026): every file opened and saved, with no repair reported. `*-word365.docx` are
Word's saves; its PDFs stay on the share. docx4j 17.3.5 and 17.3.6 read every case the same way.

| File | The `w:br` | Word's save | Word's PDF | docx4j |
|---|---|---|---|---|
| `br-1-p-alone` | the whole of a paragraph, `<w:p><w:br/></w:p>` (the generator's blank line) | `<w:p><w:r><w:br/></w:r></w:p>` | the break and the paragraph mark on two lines: 67.0pt between "BR1 line one" and "BR1 line three", three line pitches and two paragraph spacings | `<w:p/>`: the break gone |
| `br-2-p-between-runs` | directly in the `w:p`, between two runs | `<w:r><w:t>BR2 before</w:t></w:r><w:r><w:br/><w:t>BR2 after</w:t></w:r>` | two lines, 17.1pt apart | the break gone, the runs joined |
| `br-3-t-mixed` | in a `w:t`, between its text: `<w:t>BR3 before<w:br/>BR3 after</w:t>` | as case 2 | two lines | **"BR3 after" only**: the text before the `w:br` lost |
| `br-4-t-nested` | between two `w:t` nested in a `w:t` | as case 2 | two lines | **an empty `w:t`**: all the text lost |
| `br-5-hyperlink` | directly in a `w:hyperlink`, between its runs | as case 2, inside the `w:hyperlink` | two lines | the break gone |
| `br-6-cell` | case 2 in a table cell | as case 2 | two lines | the break gone |
| `br-7-footer` | case 2 in a footer | as case 2 | two lines, in the footer | the break gone |
| `br-8-p-page` | `w:type="page"`, directly in the `w:p` after a bookmark | `<w:r><w:br w:type="page"/></w:r>` after the bookmark | "BR8 after" on page 2 | the page break gone |

## Word's rule, as measured

A `w:br` outside a `w:r` - directly in a `w:p` or `w:hyperlink`, or in a `w:t` - is a break at that point in the
text, of its own type (a page break stays one). Word keeps all the text, in document order, and flattens `w:t`
nested in `w:t`. In its save the break is in a run of its own (case 1) or opens the next run (cases 2 to 7).

Not measured: which run properties the break takes where the runs either side differ (the probes' runs have none);
a `w:br` in `w:instrText` and directly in `w:body`.

## Round 2 (CR-073 phase 0, built 2026-11-03; Word's run awaited)

`build_br_placement_2.py` builds `br2-1` to `br2-5`: the run properties Word gives the break (D1: runs of 20pt
bold before and 8pt italic after, a 14pt mark), and a `w:br` in a field code, in a bare `w:instrText` in a header,
and directly in `w:body` (D2). CR-073 §3.2 has the cases.
