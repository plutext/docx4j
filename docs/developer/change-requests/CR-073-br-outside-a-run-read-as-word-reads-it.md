# CR-073: a w:br outside a w:r read as Word reads it

Status: PROPOSED 2026-11-03 (Jason: "yes, draft CR-073; we need a new probe to answer decisions 1 and 2"). Found by
docx4j's load survey of the Common Crawl corpus (docx4j-load-survey, 2 November 2026; portfolio
`docs/docx_corpus_use.md` §8.14). Phase 0 round 1 DONE 2026-11-03 (a2a971416, Jason's Word run): Word reads a `w:br`
outside a run as a break at that point, all the text kept. Round 2, for D1 and D2, built and on the share; Jason's
run awaited (§3.2). Decisions in §5, phases in §6.

## 0. Summary

A `w:br` directly in a `w:p` or a `w:hyperlink`, or inside a `w:t`, is not where the schema puts it, and 206,351 of
the survey's 3,722,083 documents have one. Word 365 opens all of these shapes without a repair and reads each as a
break at that point in the text, of its own type, keeping all the text (§3.1). docx4j drops the break, so the text
either side runs together; inside a `w:t` it also drops the text before the break, and a `w:t` nested in a `w:t`
loses all its text (§1). This touches every reader of the content: PDF and HTML output, the text model, and code
that walks the tree.

The fix is a few rules in `mc-preprocessor.xslt`, which already runs on all but 11 of these documents and today
copies the `w:br` through to a second unmarshal that drops it (§4).

## 1. The chain (Java, measured 2026-11-03, 17.3.5 and 17.3.6 alike)

1. The part's first unmarshal meets the `w:br` where `CT_P` (or `CT_Hyperlink`, or `CT_Text`'s simple content) has no
   place for it. `JaxbValidationEventHandler` logs it ("unexpected element ... local:"br""), and the read fails.
2. `JaxbXmlPartXPathAware` runs the part through `mc-preprocessor.xslt` ("encountered unexpected content; pre-processing").
   No template matches a `w:br` there, so the identity template copies it unchanged.
3. The second unmarshal continues past unexpected content. A `w:br` in a `w:p` or `w:hyperlink` is skipped. In a
   `w:t` (or a `w:instrText`) the element restarts the text value, so only the text after the last child element is
   kept.

What docx4j reads, on round 1's files (`docx4j-core-tests/src/test/resources/br-placement/`) and round 2's:

| Shape | docx4j reads |
|---|---|
| `<w:p><w:br/></w:p>` | `<w:p/>`: the break gone |
| `w:br` between runs, in a `w:p`, `w:hyperlink`, table cell or footer | the break gone, the runs adjacent |
| `<w:t>a<w:br/>b</w:t>` | "b" only |
| `<w:t><w:t>a</w:t><w:br/><w:t>b</w:t></w:t>` | an empty `w:t` |
| `w:br w:type="page"` in a `w:p` | the page break gone |
| `<w:instrText> QUOTE "a<w:br/>b" </w:instrText>` | `b" `: the field code cut |
| `w:br` directly in `w:body` | gone |

The load survey counts 206,351 documents with the WARN, 206,340 of them preprocessed: the bare `w:br` is 84% of the
survey's 246,821 preprocessed documents. Why 11 documents log the WARN without being preprocessed is not known.

## 2. What producers write

From the survey's location messages and its twenty samples (Jason's copies; never in a repository):

| Place | Documents | Producer, from the samples |
|---|---|---|
| `body/p/br` | 195,642 | a web-to-docx generator calling itself "Microsoft Word 12.0.0" (182,706 documents): `<w:p><w:br/></w:p>` is its blank line, and it also writes `w:br` between runs. It types the core-properties relationship in the officedocument namespace and names hyperlink relationships `link0`, `link1`: the survey's other two largest findings are this generator too |
| `body/p/r/t/br` | 7,398 | PHPWord (21,954 documents with the WARN in all): text with `<w:br/>` in it; and a template filler that nests `w:t` in `w:t` ("Microsoft Office Word 12.0000") |
| `body/tbl/tr/tc/p/br` | 2,272 | |
| `body/tbl/tr/tc/p/r/t/br` | 1,026 | |
| `body/p/hyperlink/br` | 603 | PHPWord writes a `w:br` between a run and a `w:hyperlink` |
| `ftr/p/br` | 332 | |
| `hdr/p/r/instrText/br` | 29 | one letterhead's template: a `w:instrText` outside any field, its lines separated by `w:br` |
| `body/br` | 10 | |

A LibreOffice-labelled sample has a `w:br w:type="page"` directly in its `w:p`, after a bookmark.

## 3. Phase 0: the Word probe

### 3.1 Round 1 (DONE 2026-11-03, a2a971416)

Eight one-case files, one per place (`br-placement/README.md` has the table). Word 365 opened each and saved it with
no repair reported:

- a `w:br` directly in a `w:p` or `w:hyperlink`, in the body, a table cell or a footer, is a break there: Word's save
  puts it at the start of the next run, `<w:r><w:br/><w:t>after</w:t></w:r>`, and its PDF has "before" and "after" on
  two lines;
- `<w:p><w:br/></w:p>` is saved as `<w:p><w:r><w:br/></w:r></w:p>`, and takes two lines: the break's and the mark's;
- `<w:t>a<w:br/>b</w:t>` and the nested `<w:t><w:t>a</w:t><w:br/><w:t>b</w:t></w:t>` are saved as
  `<w:r><w:t>a</w:t></w:r><w:r><w:br/><w:t>b</w:t></w:r>`: all the text kept, the nesting flattened;
- a `w:br w:type="page"` in a `w:p` is saved in a run of its own and starts a page.

### 3.2 Round 2 (built 2026-11-03; Jason's run awaited)

`build_br_placement_2.py`, staged in the share's `br-placement-2/`. Round 1's runs had no properties, so they could
not show which run Word gives the break (D1), and round 1 did not try the two places with few documents (D2).

| File | Decision | Asks |
|---|---|---|
| `br2-1-run-properties` | D1 | A 20pt bold run, then the `w:br`, then an 8pt italic run, with a 14pt paragraph mark where stated. Cases: R1 between the runs; R2 two breaks between them, so that the second's line holds only a break and its height shows the break's size; R3 first in the paragraph; R4 last in it; R5 the whole of a paragraph; R6 inside the bold run's `w:t`; R7 between a run and a `w:hyperlink` (PHPWord's shape); R8 between the runs inside a `w:hyperlink`. Word's save shows each break's run properties, and the PDF's line heights show its size. |
| `br2-2-instrtext-field` | D2 | `QUOTE "a<w:br/>b"`: a `w:br` in a field code, the stored result not updated. |
| `br2-3-instrtext-bare-header` | D2 | The letterhead's shape: a `w:instrText` with `w:br` in it and no field, in a header. |
| `br2-4-body-br` | D2 | A `w:br` directly in `w:body`, between two paragraphs. |
| `br2-5-body-br-page` | D2 | The same with `w:type="page"`. |

## 4. Approach

New templates in `mc-preprocessor.xslt`, beside the ones that already repair a `w:r` or `w:p` nested where it
cannot be (`w:r[w:r or w:p]`, `w:p[w:hyperlink/w:p ...]`, 17.1.0 and 17.2.0), each logging once as those do:

1. **A `w:br` that is a child of a run container** (`w:p`, `w:hyperlink`, and the other elements that hold runs
   directly: `w:smartTag`, an inline `w:customXml`, `w:fldSimple`, `w:ins`, `w:del`, `w:moveFrom`, `w:moveTo`, a
   run-level `w:sdtContent`, `w:dir`, `w:bdo`) is wrapped in a `w:r` of its own, its attributes kept (`w:type`,
   `w:clear`). Its run properties are D1's.
2. **A `w:t` with element children** becomes a sequence in its run: each stretch of text a `w:t` with
   `xml:space="preserve"`, each `w:br` a `w:br`, a nested `w:t` flattened to its text. A run may hold several `w:t`
   and `w:br` in order, so the run itself is not split. Which other text elements get the same rule (`w:instrText`,
   `w:delText`, `w:delInstrText`) is D2's.
3. **A `w:br` directly in `w:body`**: D2, by round 2's answer.

The rules cost nothing for a part without such a `w:br`: the preprocessor runs only when the first unmarshal fails,
and a misplaced `w:br` always makes it fail. Every reader downstream of the JAXB tree benefits without a change of
its own: PDF and HTML output and the text model (CR-046). docx4j's save then writes the shape Word writes.

The ports read the XML themselves, and they take docx4j as their oracle for repaired input, so the hand-off is the
probe and the rule (D5).

## 5. Decisions

| # | Question | Options | Recommendation |
|---|---|---|---|
| D1 | The run properties of the `w:r` that holds a `w:br` from a `w:p` or `w:hyperlink`. | (a) none; (b) the next run's (where Word's save puts the break); (c) the previous run's; (d) the paragraph mark's. | Word's answer from round 2. If Word gives it the next run's, (b), with (d) where no run follows. |
| D2 | Which other places get the rules. | `w:instrText` (29 documents), `w:delText` and `w:delInstrText` (no documents seen), a `w:br` directly in `w:body` (10). | Word's reading from round 2 for the first and last. Give `w:delText` and `w:delInstrText` the `w:t` rule, by analogy, so that a deleted line break is not lost. |
| D3 | Logging. | (a) a WARN once per part, as the existing templates; (b) INFO; (c) silent. | (a): the repair changes the tree, and the survey counts them. |
| D4 | What the save writes. | (a) the repaired shape, as Word's save does; (b) an option to keep the original. | (a). |
| D5 | Hand-offs. | The python, core-ts and ts-editor ports, through the portfolio: round 1 and 2's files and Word's saves are the shared probe. | After phase 1. |

Out of scope: other run content misplaced the same way (`w:tab`, `w:cr`, `w:noBreakHyphen` directly in `w:p`). The
survey's summary lists only its commonest messages. The other unexpected content is in the catalogue's
`docx4j_load_messages`: about 40,000 preprocessed documents have none of this `w:br`. A query there by element and
place would say whether any of it needs a rule like this one.

## 6. Phases

- **Phase 0, the probe.** Round 1 done (a2a971416). Round 2 waits for Jason's run.
- **Phase 1, the rules** (§4, by D1 to D4) and tests. A test loads each probe file and compares docx4j's reading
  with Word's save (text, breaks and their types in order, and run properties per D1). Plus the twenty survey
  samples read locally (not committed), and a CHANGELOG entry. The change is to the shared load path, and CLAUDE.md
  suggests Fable for that.
- **Phase 2, the hand-offs** (D5) through the portfolio session.

## 7. Risks

- The preprocessor rewrites the whole part already, so the risk is in the new templates' matches. A `w:br` inside a
  `w:r` is the legal shape and must not match. Tests cover the legal shape, the eight misplaced ones, and a `w:t`
  holding only text.
- A `w:t` child other than `w:t` and `w:br` (a `w:tab` would be the likely one) has no rule yet. The template keeps
  its text, and logs it.
- Text-model goldens: a document with such a `w:br` would gain line breaks. The core-tests text fixtures have none
  (to check in phase 1).
