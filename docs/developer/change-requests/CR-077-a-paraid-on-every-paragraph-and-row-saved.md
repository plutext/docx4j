# CR-077: a `w14:paraId` on every paragraph and row saved

Status: ACCEPTED 2026-11-06 (Jason: "CR-077 D1–D7 as recommended, then commit and implement"; proposed the same day, "write the CR for the w14:paraId gap"). Raised by core-ts's Word checks 79 and 80 and the
portfolio's DI-026, which records all three implementations. core-ts has fixed its side (CR-002 section 66, `1e02687`), and
python has proposed its own (CR-038). Decisions in §6, all decided; phases in §7.

## 0. Summary

With one paragraph or table row short of a `w14:paraId`, in any story, Word renumbers every id in some files at open, and in others
fills the gap and keeps the rest. Compatibility mode isn't what separates them, and so far the files that keep them hold comments (§1). Every docx4j route that adds a
paragraph or row leaves it without an id, so one `addParagraphOfText` on the first kind of file costs every id in it at Word's next
open, and nobody can tell beforehand which kind it is. Nothing is corrupted. What goes is the ids, which matters to anything that
addresses a paragraph by its id.

core-ts and python give the id when the content is added, through their own add methods. docx4j can't do that alone, because its
editing API is the JAXB tree: a caller's `getContent().add(p)` passes through nothing of ours. So this CR gives the ids at save,
in one pass over a part, where it already uses them. A scratch trial left no paragraph or row without an id on
every route tried, for about 1% to 5% of the time the part's marshal takes (§4).

## 1. What Word does

Word 16.0.20326.20232, 2026-11-06, one run each, through the bridge (core-ts's `test/check79/`, `test/check80/`; word-bridge
`docs/phase-3.md`; DI-026):

| File | Ids Word kept |
|---|---|
| Word's own save | 17 of 17, at open and through two saves |
| A built file with one paragraph's id removed | 0 of 11 |
| A built file with a table whose `w:tr` has no id | 0 of 14 |
| The same file with an id on the `w:tr` | 14 of 14 |
| core-ts's output, new paragraphs and rows with a `w14:paraId` and no `w14:textId` | All, in all four files, and through a save |
| The same with one new row's id removed | 0 of 28 |

So a row's id counts as a paragraph's does, and `w14:textId` isn't needed.

That isn't every file. python's jobs (its CR-038; `bridge/portfolio/q2*`, and `e-*`, `p-*` and `q-*` for the round below) gave a gap to
other Word saves, and varied the compatibility mode alone on the same save, 57 jobs. Ids of the source kept in `document.xml`, as DI-026
has them, with the counts read here from the saves:

| File | Gap | Mode 15 | Mode 12 |
|---|---|---|---|
| `79c-save-1.word.docx` | None | 18 of 18 | 18 of 18 |
| | A body paragraph, or the last row | 0 of 17 | 0 of 17 |
| `76t18`'s Word save | A body paragraph | 23 of 23, the gap filled | 23 of 23, the gap filled |
| `76t26`'s Word save | A footnote's last paragraph | 0 of 20 | 0 of 20 |
| The extraction probe's re-save | Body, row, header, footer, footnote, endnote, a keyed comment | Nothing lost, gaps filled | Not run: a mode 12 save raised the Compatibility Checker |

So the mode isn't the cause, and neither is row against paragraph: the file decides, and what separates them isn't known. A gap
outside `document.xml` can cost the ids in it (`76t26`), though every "nothing moved" there so far comes from a file that keeps its
ids anyway. One of `76t26`'s seven saves wrote no ids at all, and the other six a full new set.

The second batch, on the extraction probe's re-save at mode 15, a file that keeps its ids, read here from the saves:

| In the source | In Word's save |
|---|---|
| Two paragraphs with one id | The first keeps it, and the later gets a new one |
| A Choice's paragraph with no id, its Fallback's holding one | The Choice gets a new id, and the Fallback's is overwritten with it |
| A Fallback's paragraph with no id | It takes its Choice's id |
| A nested table's row with no id | The gap is filled |

So Word reads the Choice and writes its id over the Fallback, and treats a duplicate as `Paginate` does.

python's third round, on `79c`'s save at mode 15, a file that renumbers, saves and `check`s agreeing, as the portfolio reports it:

| In the source | Ids kept in `document.xml` |
|---|---|
| A new footer with no ids, no gap in the body | **0 of 18** |
| Two body paragraphs on one id | The later took a new one, and the other 16 stayed |
| A comment added | 17 of 17, a body gap filled |

So a part other than `document.xml` matters even when it is new, and a duplicate isn't a gap. Inferred, on one comment added and two
files that held them: in a file with no comments, one missing id in any story renumbers every id in the file, and a file with comments
keeps them and fills the gap.

What Word writes, counted here in its own saves:

| Where | Holding an id |
|---|---|
| `document.xml`, outside a Fallback, in 557 re-saves of the corpus documents | Every paragraph and row. No duplicate in any of them |
| In an `mc:Fallback`, those same re-saves | 7 of 284 paragraphs and rows, all in one document, each repeating its Choice's id |
| In an `mc:Fallback`, python's `extraction-probe.word-resave.docx` | All 10, in `document.xml` and `header2.xml`, each repeating its Choice's id |
| `word/glossary/document.xml`, those same re-saves | 0 of 207 paragraphs |
| Headers, footers, footnotes, endnotes and comments, 287 Word saves in core-ts's tests and the bridge's jobs | All 1,273 paragraphs |

Not measured: whether comments are really what separates the files; whether a gap in a nested row or a branch renumbers a file of the
kind that renumbers; a row in a header; a text box outside a branch. Three of the corpus's originals hold duplicates and their
re-saves don't, as the second batch explains.

## 2. What docx4j does (measured 2026-11-06, code of 9e3100351)

Each route ran on a fresh load of `79a-ids.word.docx` (Word's save, 17 paragraphs and a row, all with ids), then a save, and the
counts are from the saved `document.xml`:

| Route | Added with no id |
|---|---|
| Load and save, the part read | None: all kept |
| `ObjectFactory.createP`, `addParagraphOfText`, `addStyledParagraphOfText`, `createParagraphOfText`, `addParagraph(xml)`, `addObject`, an `XmlUtils.unmarshalString` fragment | 1 paragraph each |
| `TblFactory.createTable(2, 3, ...)` | 6 paragraphs, 2 rows |
| `createTr` into a table | 1 paragraph, 1 row |
| A `createTc` per row, which is how a column is added: docx4j has no method for it | 1 paragraph |
| `TocGenerator.generateToc` | 3 paragraphs |
| Load repair of a `w:br` in `w:body` with no paragraph after it (`mc-preprocessor.xslt`, CR-073) | 1 paragraph |

Duplicates, with no id missing:

| Route | Duplicate ids |
|---|---|
| `XmlUtils.deepCopy` of a row, added | 3 |
| `XmlUtils.deepCopy` of a paragraph, added | 1 |
| Load repair of a `w:p` in a `w:r`: the outer paragraph's attributes go on both halves | 1 |

By reading, not run: `OpenDoPEHandler` (line 1383) and `MailMerger` (line 193) clone with `deepCopy`, so a repeat or a merge
should give duplicates. `docx4j-markdown` and `docx4j-ImportXHTML` have no `paraId` code, so what they add should have no id.
Headers, footnotes and comments use the same factory.

**`Paginate.assignParaIds`,** public since 17.2.0, gives ids to the paragraphs the FO layout has, in `McMode.DRAW`, with
`w14:textId` set to the same value, and gives a later duplicate a new id. It gave `createTable`'s 6 paragraphs ids and left its 2
rows without. `paginate` and `applyLastRenderedPageBreaks` call it by default, "so that a document paginated once keys stably
from then on". By check 79's table file that doesn't hold for a document with a table: its rows have no ids, so Word renumbers
the paragraphs too. That is inferred, not measured on `Paginate`'s output.

**The reach.** `word/document.xml` of the 561 corpus documents, leaving out a Fallback's content: 382 hold no id, 157 hold one on
everything, 14 are partial, 3 of those with duplicates, and 8 couldn't be read this way. The partial ones run from a few missing
to 46,538 of 46,539, a document with one id. So a part that uses ids isn't always a part that arrived whole (D3).

## 3. What the other two did, and why not here

core-ts's `assignParaIds` runs in each of its add methods, "at creation, not on save", because its save reports and doesn't
rewrite (its CR-017), and because a new paragraph's `w14:` address is wanted at once. python's CR-038 ports that. Both have a
content API, so every addition passes through their code. A file that arrives with a gap isn't repaired in core-ts.

docx4j's add methods are a handful of conveniences beside the tree. `docx4j-samples-docx4j` adds through `getContent().add(...)` as
often as through them (48 lines and 46), and `List.add` tells us nothing. Giving ids in `addParagraphOfText` and `TblFactory` would
cover the routes that are easy to cover and leave as common a one. `TblFactory.createTable` couldn't do it anyway, as it has no document
to ask whether ids are in use.

docx4j has no `w14:` address to serve, so an id isn't wanted before the save. Its nearest need, `Extractor`'s `paraId`, reads what
the tree holds.

## 4. A trial (2026-11-06, scratch, the tree untouched)

A scratch class ran §5's pass on the main part just before a save, counting the saved bytes:

| File | Filled | Left with no id, outside a Fallback | Duplicates left |
|---|---|---|---|
| `79a-ids.word.docx`, plus `createTable`, `addParagraphOfText` and a `deepCopy` of a paragraph | 9, and 1 duplicate given a new id | 0 of 25 paragraphs, 0 of 3 rows | 0 |
| `76t18-hosts-control-word365.docx` with every id but the first taken out: controls, custom XML and every other host | 23 | 0 of 20 and 0 of 4 | 0 |
| `52-ranges-word365.docx`, the same | 564 | 0 of 506 and 0 of 59 | 0 |
| `cr066-r6-T-word365.docx`, the same | 522 | 0 of 419 and 0 of 104 | 0 |
| `79d-textbox.docx`, the same: a `w:pict` text box | 13 | 0 of 14 | 0 |
| A corpus document's re-save with 9 `wps` text boxes, the same | 179 | 0 of 145 and 0 of 35 | 0 |

The last file's 62 paragraphs and rows in a Fallback were not touched. Filling took 1 to 5 ms on all of these.

The cost, best of five, on the four largest main parts in the corpora, with no gap to fill, against that part's marshal: 5, 55, 6
and 5 ms beside 350, 1072, 356 and 400 ms, so +1.4% to +5.2%. A part with no id takes one walk, and one with ids two.

The trial didn't run the suites, and no Word has opened its output. Those are phases 1 and 2.

## 5. Approach

**The pass.** Before `document.xml` or a header, footer, footnotes, endnotes or comments part is written (D2), if it has been read and
it or the main document part holds at least one id, every `w:p` and `w:tr` Word reads that has no id takes one, and so does a later
duplicate (D4). Otherwise nothing changes.

- **Only a part that has been read.** A part nobody read is copied as bytes (`ZipPartStore.saveJaxbXmlPart`), so nothing can have
  been added to it, and this costs nothing there.
- **Only where ids are in use.** A document with none stays so, as in core-ts, and Word gives them at its own save. A row's id
  counts as a paragraph's does. An id anywhere in the part or the main document part counts, in any branch, so a new footer takes ids
  in a document that uses them. A main document part nobody read isn't read for this: nothing can refer to a new part then.
- **The branch Word reads.** `McMode.READ` for what takes an id: the first Choice whose `Requires` are understood, else the
  Fallback. Word's saves mostly leave a Fallback's paragraphs without, and where they don't, repeat the Choice's id (§1). So the pass leaves a Fallback alone either way, and doesn't take that repeat for a duplicate. `McMode.ALL` for the ids in use, so a new
  id clashes with none. A walk into paragraphs is needed, as text boxes are inside them.
- **`w14:paraId` alone,** as check 80 measured (D6). A new id is random, below 0x7FFFFFFF, and unused in the part, as `Paginate`'s is.
- **The tree takes the ids,** so a second save of the same package writes the same ones.

**Where it runs.** `Save.saveRawXmlPart` already handles parts by kind before writing them (`DocPropsCorePart`), and every zip,
unzipped and encrypted save goes through it. `FlatOpcXmlCreator` doesn't, so it calls the same thing. The pass itself is public, as
`org.docx4j.model.structure.ParaIds.assign(part)`, so a caller who wants ids before a save, or who marshals a part directly, can call it. `XmlUtils`'s
marshal methods and `part.marshal` are not a save, and stay as they are.

**The property.** `docx4j.save.paraIds`, by default true (D5).

**`Paginate.assignParaIds`** stays as it is, with its `w14:textId` and its body paragraphs, since `paginate`'s keys need it. Once it has
run, the part uses ids, so a save now gives the rows theirs. That closes §2's case with no change to `Paginate`.

`word/glossary/document.xml` is never one of the parts: Word writes no id there.

## 6. Decisions

As recommended (Jason, 2026-11-06).

| # | Question | Options | Recommendation |
|---|---|---|---|
| D1 | Where the id is given. | (a) At save, as §5; (b) in docx4j's add methods, as core-ts and python; (c) both; (d) only a public method, for callers to call. | (a). It is the only one that sees `getContent().add(...)`, the deep copies and the load repairs, and it needs one seam, not twenty. (d) leaves the gap as it is for everyone who doesn't know of it. |
| D2 | Which parts. | (a) `document.xml` alone; (b) that and headers, footers, notes and comments, where Word's saves hold ids on everything. | (b). A gap in a footnote cost every id in `76t26`'s save, a new footer with none cost every id in `79c`'s, and Word's saves hold ids in all of those parts, so giving them can't be worse. The glossary stays out. |
| D3 | A part that arrives with a gap (14 of 553 corpus documents). | (a) Fill it; (b) leave it, as core-ts. | (a). At save, what arrived can't be told from what was added without a walk at every load. It can only help: Word either fills such a gap itself or renumbers the part. A part read and saved unedited gains ids, up to 46,538 in the extreme case, about 25 bytes each. |
| D4 | A duplicate. | (a) The later one takes a new id, as `Paginate` does; (b) leave it. | (a), which is what Word does: the first keeps its id and the later gets a new one (§1). It costs nothing in the same pass. A Fallback repeating its Choice is Word's own shape, and the pass doesn't reach it. A repeat or a merge clones ids, so this is the route most likely to meet them. |
| D5 | The default. | (a) On, `docx4j.save.paraIds=false` to turn it off; (b) off, on by request. | (a). The people it helps don't know the rule. It writes only an ignorable attribute, only in a part that already has them, and only where one is missing. |
| D6 | `w14:textId` on a new id. | (a) None, as core-ts and check 80; (b) the same value, as `Paginate`; (c) `77777777`, as Word's saves often hold. | (a). It is what Word was seen to accept. |
| D7 | Which release. | (a) 17.3.6; (b) later. | (a). Nothing here waits on anything else in 17.3.6. |

## 7. Phases

- **Phase 0, Word.** DONE: python's round through the bridge, for the ports too (Jason, at the portfolio: "Run the mode 12 against mode
  15 round", then yes to a second batch of 16 shapes). §1 has what it found. It doesn't say what separates the files, and a later
  check could look for that, but nothing here waits on it.
- **Phase 1, the pass** (§5, by D1 to D6). DONE 2026-11-06: `ParaIds`, with `ParaIdsTest`'s 16 tests. 22 mutants, of the pass and the
  hooks, are all caught. Markdown 180, core-tests 2480 (30 skipped), diffx 9 (1 skipped), digsig 138 (1 skipped), export-fo 111,
  export-fo-tests 908 (9 skipped). `SaveToZipFile`, deprecated, takes the hook too.
  - `ParaIds`, the two seams, the property, and its line in `docx4j-samples-resources`' `docx4j.properties`.
  - Tests: §2's routes on Word's save, each leaving no paragraph or row without an id, none repeated, Word's ids in place; a document
    with no ids left with none; a part not read left byte for byte; a Fallback untouched; the glossary untouched; flat OPC; the
    property off; `Paginate` then a save on a document with a table.
  - All the suites. A corpus gate isn't needed, as the PDF path doesn't save, but `docx4j-core-tests`' goldens will show any
    comparison a random id upsets.
  - A CHANGELOG entry.
  - The change is to the shared save path, and CLAUDE.md suggests Fable for that.
- **Phase 2, Word on docx4j's output.** DONE 2026-11-06. Through the bridge, each file twice, the two agreeing, all opening with no dialog, counted here
  from the saves. docx4j's saves of `79c`'s save, which renumbers on a gap, and of the two gap files, ids of the source kept in
  `document.xml`: read and saved 18 of 18; rows added 24 of 24; a table 27 of 27; a column 19 of 19; all of those and paragraphs 38 of
  38; a row and a paragraph copied 22 of 22; the file with a body gap 18 of 18; the file with a footnote gap 20 of 20. The control, the
  all-of-those file with one new row's id taken off, kept 0 of 37. In `footnotes.xml` Word gave the separators new ids, as it does in
  every save, and kept the two notes' ids. A new footer added by docx4j, its paragraph taking an id: 18 of 18, where python's with none kept 0.

## 8. Risks

- **A save that changes the tree.** In `saveRawXmlPart` that has been the `docProps` alone until now. A caller holding the
  `P` sees an id appear, which should be harmless. Two threads saving one package would race, as they would on the docProps.
- **Random ids.** Two runs on the same input write different ids, where today they write the same bytes. A test or a caller comparing
  saved XML across runs, for a document that uses ids and gains a paragraph, sees a difference. Phase 1 shows what the suites make of it.
- **`mc:Ignorable`.** A part with ids has `w14` declared, and docx4j keeps that (CR-049), but no Word has seen this output yet. Phase 2.
- **A digital signature.** The pass adds to a part only where a paragraph or row has no id. If that is an edit, the signature was
  broken anyway. A signed file arriving with a gap and read would change on save, where today it wouldn't (D3). Phase 1 should read
  how `docx4j-digsig` saves.
- **Comments.** `commentsExtended.xml` and its kin refer to a comment's paragraph by id. Under D2 (b) and D4 (a) a duplicate there
  needs care: the first keeps its id, as Word's does, but which one a reference meant can't be told. No duplicate in a comments part has been measured.
- **What is assumed.** Some files follow the rule and some don't, and why isn't known (§1). The pass doesn't need to know: it gives an
  id where Word's own saves have one, and in no file so far has that done harm.

## 9. Out of scope

- The glossary part.
- An id at creation, for a caller who reads `getParaId()` straight after an add. They can call the public method.
- `docx4j-ImportXHTML`, a separate repository: what it adds is given an id by this pass at save, so it needs no change of its own.
- A document with no ids at all. Word gives them, and nothing here relies on them.
