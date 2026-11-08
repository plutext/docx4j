# CR-079: `mc:AlternateContent` as the root, and a repeated container, read as Word reads them

Status: ACCEPTED 2026-11-07 (Jason: "CR-079: D1–D9 as recommended."). Proposed 2026-11-06 (Jason: "reproduce it first and then write a CR"). Reproduced
on f960df59e; phase 0 done, phase 1 begun, uncommitted (§6). Read against WD 0.101 (portfolio 7150868), which takes phase 0 and `ft-*` into REQ-317 and REQ-322.
Raised by docx4j-generated-objects-ts's Word rounds and docx-tool-identifier's run of the readers (Jason: "tell the various engines about
the disagreement"), written up in `docx4j-portfolio/docs/text_extraction_behaviour_model.md` §7.15.

## 0. Summary

Word opens both shapes silently, and docx4j gets both wrong.

- **`mc:AlternateContent` as the root of `word/document.xml`.** Word reads the branch it takes and saves an ordinary `w:document`. docx4j loads
  the file with no error and leaves an `AlternateContent` in a part typed `Document`, so what comes next fails with a `ClassCastException`: text,
  HTML, `getContent()`, and a save after reading. If a branch raises a validation event the stylesheet runs instead, and docx4j loads the
  Fallback whatever `Requires` says: the wrong words, and no error.
- **A child given twice that Word treats as one container.** JAXB keeps the second whole and raises no event. Of the 13 cases measured docx4j
  matches Word on 8 and loses content on 5, and logs nothing about it: the first `w:body`, the first `w:sdtContent`, the first `w:txbxContent`,
  and what only the first `w:pPr` or `w:rPr` held.

A third shape came from core-ts's round of 2026-11-07: another story's root in the main part. Word has no one answer (§1.1), and docx4j fails on each
root JAXB knows as it does on the `mc` root (§2.3). D9 reads a bare `w:body` and an unknown `w:` root as Word does. The others can't be edited, a save
keeps their bytes, and text says which root it found.

A scratch trial shows both can be read Word's way with what docx4j has: `McSplice.WORD_READS` picks Word's branch at the root, and the unmarshal
listener can see the first child when the second arrives. How often either shape occurs in real documents isn't known. Word doesn't write them.
On a docx with neither shape, the checks cost nothing that can be measured (§3).

## 1. What Word does

Word 16.0.20326.20232, through the bridge, each file twice, the two runs equal apart from generated ids. objects built the files and read the
saves (`rc-*`, `rw-*` in the share's `bridge/portfolio/`); the rows below were read again here from Word's saves.

| `word/document.xml`'s root is `mc:AlternateContent` with | Word |
|---|---|
| A `Choice` it understands (`rw-20`) | The Choice, silently. The save has an ordinary `w:document` |
| A `Choice` it doesn't, and a `Fallback` (`rw-21`) | The Fallback, silently |
| Two `w:document` in the branch taken (`rw-23`, `rw-26`, `rw-27`) | Both, joined in order, silently. The second's section stands, margins included |
| A `Choice` it doesn't understand, and no `Fallback` (`rw-24`) | Silently, an empty document |
| A `Fallback` and no `Choice` (`rw-22`) | Repaired, keeping the Fallback's words. No in-body control, so the repair may come from the missing `Choice` |

| Given twice | Word |
|---|---|
| `w:body` (`rc-10`) | Joined in order. The second's `w:sectPr` stands, and the first's goes with no section break |
| `w:sdtContent`, block level (`rc-11`) | Joined in order |
| `w:txbxContent`, in a VML box (`rc-12`) | Joined, **the second first**. Both runs; unexplained |
| `w:pPr`, `w:rPr` (`rc-20` to `rc-23`) | Merged, the later value winning: `w:jc` then `w:ind` keeps both, `w:b` then `w:i` keeps both |
| `w:jc`, `w:b`, `w:sz` (`rc-30` to `rc-33`) | The last |
| `w:sectPr`, `w:tblPr` (`rc-40`, `rc-41`) | The later value. Both copies held the same properties, so whether they merge wasn't tested |

Every one of these opened silently.

**The rule, since measured** (REQ-322, WD 0.100, extended in 0.101, from `rt-*` and python's `sectpr-merge` round, `sm-*`, 40 files twice). Word applies each copy to one
object in order: what a copy states wins and what it doesn't state stays, for attributes and child elements alike, across joined bodies and one level
in. That covers `w:pPr`, `w:rPr`, `w:sectPr`, `w:tblPr`, and within them `w:ind`, `w:spacing`, `w:rFonts`, `w:pgSz`, `w:pgMar`, `w:cols`, `w:jc`, `w:sz`, `w:b`
and `w:vanish`. An attribute left out stays even where the schema gives a default (`w:orient`, `sm-01`), so the table's "the later value" is too simple;
an on/off element with no `w:val` states on (`sm-81`). The final `w:sectPr` of joined bodies apply the same way, a body with none passed over, and a
`w:sectPr` in a `w:pPr` stays where it is (`rt-15`). Two exceptions: only the first copy's `w:headerReference` and `w:footerReference` stand, a reference on any later copy being ignored, part and all,
whatever its `w:type` and across joined bodies, and a first copy with none leaving none (`ft-*`); and `w:tblW` fits neither reading (`sm-61`). `w:txbxContent` joins in reverse order with two, three and four copies, in a VML box alone. A `w:body` in a `w:body`
reads through; a `w:document` below the root outside a selected branch is refused.

**Phase 0's round** (Jason: "then commence CR-079"; the set `cr079-phase0`, 25 files, each twice, the runs equal, Word 16.0.20326.20232, every save, text and PDF
read; `CR-079-notes/phase0-word.tsv`):

| Shape | Word |
|---|---|
| `mc:AlternateContent` as the root of a header, footer, footnotes, endnotes, comments, styles, numbering or settings part (`c79-10` to `c79-17`) | Silent, the Choice read in all eight |
| The same in a header, the Choice not understood (`c79-18`) | Silent, the Fallback read |
| Two `w:document` in a branch, a `w:background` in either (`c79-20`, `c79-21`) | Both joined, the background standing from whichever states it |
| `mc:Ignorable="z"` on the first `w:document`, `z:x` in the second (`c79-22`) | Refused, so it doesn't reach the second |
| `w:sdtContent` twice at run, row and cell level (`c79-30` to `c79-32`) | Joined in order in one control, at every level; the runs' text in one `w:t` |
| `w:txbxContent` twice in `wps:txbx` (`c79-33`) | The second alone; the first is nowhere. Not VML's reverse join |
| `w:pPr` twice, each with a `w:rPr` (`c79-40`) | One `w:rPr` holding both, so the merge goes on down |
| `w:pPr` twice with `w:numPr`, the second with `w:numId` alone (`c79-41`) | The first's `w:ilvl`, the second's `w:numId` |
| `w:pPr` twice, the first with a `w:pPrChange` (`c79-42`) | Merged, the `w:pPrChange` kept |
| An unknown element in the branch taken, or not (`c79-50`, `c79-51`) | Silent, the Choice read either way |
| A `w:hdr` root with a `w:footnoteReference` and a footnotes part (`c79-60`) | Silent, a blank page. The header's words are in the `separator` footnote, drawn nowhere; the note's are nowhere in the package |
| A bare `w:body` in a branch taken (`c79-70`) | Read |
| An empty branch taken, a `w:document` in the Fallback (`c79-71`) | Silent, an empty document; the Fallback's words are nowhere |

### 1.1 Another root in `word/document.xml`

Measured by core-ts on 2026-11-07, at Jason's "what does Word do if the root element of the main document part is that of some other story", and
sent here at his "tell docx4j", relayed by core-ts (its CR-028 §1.1). Word 16.0.20326.20232, through the bridge, each file twice, the two runs
agreeing, in the same three-part package as `rw-*`, with the main document content type unless stated. Each `result.json` and save was read again
here (`bridge/portfolio/or-*`).

| Root of `word/document.xml` | Word | Word's save |
|---|---|---|
| `w:document` (`or-00`, control) | Silent | Words in the body |
| `w:hdr`, `w:ftr` (`or-10`, `or-11`) | Silent, a blank page | Empty body. The words are the first paragraph of the `separator` footnote (id -1), with no reference. Whether they ever draw isn't measured |
| `w:footnotes`, with and without separators, `w:endnotes` with them (`or-12`, `or-15`, `or-13`) | Refused: "Word experienced an error trying to open the file" | None |
| `w:comments` (`or-14`) | Repair offered; Show Repairs lists nothing | One paragraph holding the comment's range and reference, the comment in `word/comments.xml` |
| `w:styles` (`or-30`) | Silent, empty | Empty body |
| A bare `w:body` (`or-32`) | Silent | Words in the body |
| `w:madeUp` holding a `w:p` (`or-31`) | Silent | Words in the body |
| `mc:AlternateContent`, `w:hdr` in a `w14` Choice, `w:document` in the Fallback (`or-50`) | As `or-10` | As `or-10`. The Fallback's words are nowhere in the save |
| `w:hdr` or `w:document`, with a header's content type (`or-40`, `or-41`) | Repair offered, then refused after Yes | None |

So a branch taken that holds no `w:document` doesn't fall through to the Fallback's. And for a root in the `w:` namespace the content decides, not the
name. core-ts's `madeup` round (`mu-10` to `mu-12`, its saves and `result.json` read again here) refused `w:madeUp` holding one or two `w:document`, as
`rw-12` is refused for `w:foo`, and opened `w:foo` holding a `w:p`, its words in the body, as `or-31` opens `w:madeUp`.

**How far that reading goes** (Jason: "let's do a round for w:madeUp holding a w:p now"; the set `madeup-root`, 20 files, each twice, `mr-01` being
`or-31`'s parts). `CR-079-notes/` has the builder, the reader and `mr-word.tsv`. Word's text and PDF agree with its saves. The two runs are equal, but
for `mr-12`, where Word's default `w:tblLook` differs (`0000`, `04A0`), unexplained. Each row rests on one file.

| Root | Word | Word's save |
|---|---|---|
| `w:madeUp` holding two `w:p` (`mr-10`), a `w:tbl` and a `w:p` (`mr-12`), a block `w:sdt` (`mr-13`), an unknown element holding a `w:p` (`mr-17`) | Silent | All of it in the body, each element as it stood |
| `w:madeUp` holding a `w:p` with `w:jc` and a `w:b` (`mr-18`), or a `w:pStyle` in a package with a styles part (`mr-50`) | Silent | Words in the body, the properties kept |
| `w:madeUp` holding a `w:p` and a landscape `w:sectPr` (`mr-11`) | Silent | Words in the body. The `w:sectPr` isn't read: the page is Word's default |
| `w:madeUp` holding a `w:body` (`mr-15`) | Silent | That body, its landscape section read |
| An empty `w:madeUp` (`mr-16`) | Silent | An empty document |
| `w:madeUp` holding a bare `w:r` (`mr-14`) | Refused | None |
| `w:foo` holding one `w:document` (`mr-03`) | Refused | None |
| `z:madeUp`, and `madeUp` in no namespace, holding a `w:p` (`mr-20`, `mr-21`) | Refused | None |
| `z:madeUp` holding a `w:p`, `z` in the root's `mc:Ignorable` (`mr-22`) | Silent | Empty. The words are nowhere in the package |
| `w:p`, and `w:sdtContent` holding a `w:p` (`mr-32`, `mr-30`) | Silent | Words in the body |
| `w:tc` holding a `w:p` (`mr-31`) | Refused | None |
| `mc:AlternateContent`, `w:madeUp` holding a `w:p` in a `w14` Choice, `w:document` in the Fallback (`mr-40`) | Silent | The Choice's words. The Fallback's are nowhere |

So a `w:` root Word doesn't know behaves as if its tags were absent, and what it holds stands where a body's content stands. That explains each row: a
bare run or a `w:document` can't stand there, a `w:body` can, and a `w:sectPr` means nothing outside a `w:body`. Known names give no such rule: of three
that are no part's root, two read and one is refused, beside `w:hdr`'s blank page. And `or-10` and `or-11`'s text and PDF are empty, so the header's
words in the separator footnote don't draw there.

## 2. What docx4j does (measured 2026-11-06, code of f960df59e)

46 of objects' files, each with a fresh load through `Texts`, `TextUtils`, `getContent()`, `Docx4J.toHTML` and a save, logging at WARN, the text
compared with Word's. `CR-079-notes/` has the probe and its output. Markdown and PDF weren't run here; identifier's run has them failing too.

### 2.1 The root

`AlternateContent` carries `@XmlRootElement`, so the unmarshal returns one and raises no event. `unwrapUsually` stores it, the generic hides it, and
the first cast fails. CR-051's splice doesn't reach it, since `McSplice.inScope` wants a parent.

| File | Word | `Texts` | `TextUtils` | Save, part read |
|---|---|---|---|---|
| `rw-20` | `CHOICE WORDS` | fails | `FALLBACK WORDS` | fails |
| `rw-21` | `FALLBACK WORDS` | fails | `FALLBACK WORDS` | fails |
| `rw-23` | both documents | fails | `FALLBACK WORDS` | fails |
| `rw-24` | nothing | fails | `CHOICE WORDS` | fails |
| `rw-26` | both documents | fails | both documents | fails |
| `rw-27` | both documents | fails | `FALLBACK WORDS` | fails |

- `Texts` fails at `Lister:127`, `getContent()` at `MainDocumentPart:153`, HTML at `AbstractWmlExporter:55`.
- The save fails through `ParaIds.beforeSave`, into `getContent()`. **That isn't CR-077's doing:** 17.3.5 fails on the same cast a step later, in
  `setMceIgnorable`.
- **A save of a part nobody read works:** it copies the bytes.
- **The eager binder** (`binder.eager.MainDocumentPart=true`) and `docx4j.jaxb.mc.onLoad=preserve` fail the same way.
- **A validation event in the file takes another route.** `mc-preprocessor.xslt`'s rule for `mc:AlternateContent` applies at the root, and takes the
  Fallback. A scratch copy of `rw-20` with an unknown element in the Choice's run loads as a `Document` holding `FALLBACK WORDS`, where Word's reading
  of `rw-20` is the Choice. That copy hasn't been through Word.
- **The log.** The load logs nothing at WARN on the direct route, and only the failed save reports. The stylesheet route does warn, that the
  element "is not kept by the preprocessor", with no word of which branch stood.

### 2.2 A repeated child

`Document.body`, `SdtBlock.sdtContent` and the rest are single-valued, so the second overwrites the first. JAXB raises no event, and the stylesheet
never runs. The five losses:

| File | Word | docx4j, read and saved |
|---|---|---|
| `rc-10` | `FIRST BODY WORDS`, `SECOND BODY WORDS` | `SECOND BODY WORDS` |
| `rc-11` | `FIRST CONTROL WORDS`, `SECOND CONTROL WORDS` | `SECOND CONTROL WORDS` |
| `rc-12` | `SECOND BOX WORDS`, `FIRST BOX WORDS` | `SECOND BOX WORDS` |
| `rc-21` | `w:ind` and `w:jc` | `w:ind` |
| `rc-23` | `w:b` and `w:i` | `w:i` |

The other 8 match Word: `rc-20`, `rc-22`, `rc-30`, `rc-32`, `rc-33`, `rc-40`, `rc-41`, and `rc-31`, where docx4j keeps `w:b w:val="false"` and Word
writes nothing, to the same effect. That is what objects measured of its own engine, as both keep the second whole. Nothing about the loss is
logged: the only WARN lines on these files are HTML's, about fonts. The bridge's `.txt` leaves out a text box, on the control too, so `rc-12` is read from Word's save.

### 2.3 Another root

The same probe on 2026-11-07, code of f960df59e. `CR-079-notes/or-output.tsv` has its output. `or-00` reads and saves as it should.

| File | Word | The part holds | `TextUtils` | `Texts`, `getContent()`, HTML | Save, part read |
|---|---|---|---|---|---|
| `or-10`, `or-11` | A blank page | `Hdr`, `Ftr` | The words | Fail, at §2.1's casts | Fails |
| `or-12`, `or-13`, `or-15` | Refused | `CTFootnotes`, `CTEndnotes` | The words | Fail, the same | Fails |
| `or-14` | Repair | `Comments` | The words | Fail, the same | Fails |
| `or-30` | Empty | `Styles` | Nothing | Fail, the same | Fails |
| `or-32` | The words | `Body` | The words | Fail, the same | Fails |
| `or-50` | As `or-10` | `AlternateContent` | `FALLBACK WORDS` | Fail, the same | Fails |
| `or-31` | The words | Nothing: `getJaxbElement()` is null | Fails | Fail with the read's `UnmarshalException` | Copies the bytes |
| `rw-12` | Refused | Nothing, the same | Fails | Fail, the same | Copies the bytes |

So every root JAXB knows fails as §2.1 has it, whichever part of the schema it comes from. A root JAXB doesn't know does better, since CR-065 D1
records the failure, the outputs name the part, and a save keeps the bytes. On `or-40` and `or-41`, `WordprocessingMLPackage.load` fails with a
`ClassCastException` at `WordprocessingMLPackage:174`, as the package loads as a plain one. A package with no main part reads as an empty
`TextResult` with nothing said, by `Extractor`'s code; that wasn't run.

On the `mr-*` files (`CR-079-notes/mr-output.tsv`), every unknown root reads as `or-31` does, whatever Word does with it. `mr-30`, `mr-31` and `mr-32`
fail as a root JAXB knows does, and `mr-40` as the `mc` root does. `w:sdtContent` binds as a `CTSdtContentRow`, which loses the `w:p`: `TextUtils` gives
nothing there.

## 3. A trial (2026-11-06, scratch, the tree untouched)

**The root.** `McSplice.WORD_READS` on the root element of the loaded files, with the `w:document` objects in the branch it returned:

| File | Selected | Documents | Words | Last document's section | As Word |
|---|---|---|---|---|---|
| `rw-20` | Choice | 1 | `CHOICE WORDS` | 11906 x 16838 | Yes |
| `rw-21` | Fallback | 1 | `FALLBACK WORDS` | 11906 x 16838 | Yes |
| `rw-22` | Fallback | 1 | `FALLBACK WORDS` | 11906 x 16838 | Its words; Word repairs |
| `rw-23` | Choice | 2 | both, in order | 11906 x 16838 | Yes |
| `rw-26` | Fallback | 2 | both, in order | 15840 x 12240, top 720 | Yes |
| `rw-27` | Choice | 2 | both, in order | 15840 x 12240, top 720 | Yes |
| `rw-24` | none: it keeps the element whole | | | | Word reads nothing (D2) |

So selection works at the root, `Requires` declared on the root included, and the lax binding has already made the `Document` objects. There is
nothing more to parse.

**The repeated child.** A listener on `rc-10`, `rc-11` and `rc-23` printed what the parent holds. At both `beforeUnmarshal` and `afterUnmarshal` of
the second `Body`, `SdtContentBlock` and `RPr`, the parent still holds the first, and the field takes the second afterwards. So
`afterUnmarshal` can move the first's content into the second, which then stands. That is the JAXB reference implementation; MOXy wasn't run.

**The cost (2026-11-07, scratch, the tree untouched).** Neither shape raises a validation event, so the preprocessor never runs on them, and a
stylesheet could reach them only by running on every read of every part. Seven main parts: the tenth percentile, median and ninetieth percentile
of the 553 in the corpora, and the four largest. The checks were a scratch subclass in front of the stock listener, on the reference
implementation in a warm JVM, the variants taking turns, best of 12 to 300 runs. The control delegates and checks nothing. The last two columns
are the part's real read, as today and with `docx4j.jaxb.preprocess.always`, best of 5 to 60. `CR-079-notes/` has the classes and their output.

| Main part | Unmarshal, stock | All five checks | Control | Read as today | Stylesheet on every read |
|---|---|---|---|---|---|
| 35 KB | 1.33 ms | -0.3% | -0.5% | 1.7 ms | 6.1 ms, 3.6 times |
| 170 KB | 3.25 ms | +0.7% | +0.2% | 3.7 ms | 19.4 ms, 5.3 times |
| 956 KB | 31.07 ms | +0.3% | -0.3% | 31.6 ms | 146.7 ms, 4.6 times |
| 8.3 MB | 308.67 ms | +0.0% | -0.1% | 305.4 ms | 1,670.9 ms, 5.5 times |
| 10.1 MB | 253.71 ms | +0.3% | +0.4% | 255.6 ms | 1,341.7 ms, 5.2 times |
| 10.4 MB | 468.61 ms | +0.2% | +0.3% | 463.1 ms | 3,199.0 ms, 6.9 times |
| 12.5 MB | 162.27 ms | +0.3% | +0.1% | 170.2 ms | 946.5 ms, 5.6 times |

- **The checks** are below what this can see: -0.3% to +0.7%, beside -0.5% to +0.4% for the control. `w:body` and `w:sdtContent` alone give -0.9% to
  +0.6%, so cost is no reason for D5's alternative. They fired on `rc-10` to `rc-12` and `rc-20` to `rc-23`, and on none of the seven parts.
- **The stylesheet's addition** only meets a part that raises an event: 3 of the 553 main parts do. Time in the stylesheet, best of 8, on the five
  largest of the seven: a new `match="/mc:AlternateContent"` template -2.4% to +0.7%, a test in the existing template -6.7% to 0.0%, both noise.
  Each keeps the root of §2.1's scratch copy, where stock gives `w:document`.
- **Not timed:** MOXy, a cold JVM, parts other than the main part, and a file with one of the shapes, which also pays for the join and on a binder
  path for the binder re-created. The 3 of 553 is these corpora; another collection will differ.

## 4. Approach

**The root, in `JaxbXmlPart`.** One step after each unmarshal path, where `jaxbElement` is an `AlternateContent` (D1: every part, with `Document` the main
part's case):

1. `McSplice.WORD_READS` selects, answering `McSplice.NOTHING` where nothing is selected (af26a5f78).
2. No branch gives a new empty `Document` (D2) and `mce.nothing_selected` with no address (WD REQ-801), one `w:document` is taken, and several join by the
   rule below. A branch taken that holds no `w:document` goes as D9 has it, and no other branch stands in, since Word doesn't read the Fallback's there (`or-50`).
3. The Choice's prefixes go on the root's `mc:Ignorable`, as CR-051 has it, and a `ResolvedAlternateContent` records the element whole, so
   `getResolvedAlternateContent()`, the inspector and the text model's branch comparison see what wasn't taken.
4. A binder path re-creates the binder, as CR-051 §11.1 has it after a splice: the tree marshalled and read again through a new binder.

After that nothing downstream sees the element, and every cast holds. `Lister`, `AbstractWmlExporter` and the rest don't change.

**Another root, in the same step (D9).** Where `jaxbElement` is anything else but a `Document`, a `Body`, `P` or `SdtContent` goes into a new `Document`,
which is Word's reading (`or-32`, `mr-30`, `mr-32`). `mr-30` is harder: its `CTSdtContentRow` has already lost the `w:p`, so the words aren't in the
object. It is the stylesheet's, then: JAXB's first pass rejects the `w:p`, so the root reaches the rule below. The rest fail the read with an exception naming the root, so they take CR-065 D1's route, as `w:foo` does today:
`isContentUnreadable()` is true, `getReadableContents` gives the outputs their message, the conveniences refuse, and a save keeps the bytes.
The part records the root's name, and for the roots D9 doesn't refuse, the text model gives no text and `part.unexpected_root` in place of that
refusal, selected by the part's name, with the root's qualified name in `detail` (WD REQ-801). In the
stylesheet, a rule at the root puts an unknown `w:` root in a `w:document` and a `w:body`, where all it holds is on REQ-319's list, and a `w:sdtContent` JAXB's first pass has rejected. Its
own `w:body` is the body and a `w:sectPr` directly in it goes. CR-076's rule needs a known ancestor to read through an unknown element, which a root doesn't give, so a further unknown element is read through in this rule, and CR-076's rules do the rest. Only `MainDocumentPart` sets the rule's parameter, as no other part is measured, and it matches `/`, so it is reached once a document. `Extractor` refuses a package with no main part, and a
nested package with none raises `content.not_read` (WD REQ-321).

**The stylesheet.** No stylesheet of its own, and no pass on a part that doesn't take the route today (§3). In `mc-preprocessor.xslt`, a
`not(parent::*)` test at the head of the existing `mc:AlternateContent` template keeps the root and its branches and processes what they hold, so
the same Java step decides. Today's rule then stands only below the root. A `match="/mc:AlternateContent"` template does the same and timed the
same; the test is used because new match patterns cost +14% to +24% in CR-076, though not here.

**A repeated child, in `Docx4jUnmarshallerListener.afterUnmarshal`,** for the main part alone, as every measurement is of `word/document.xml`. Where the
parent already holds one, WD REQ-322 reads it:

- **`w:body`, a `w:sdtContent` at any level:** the first's content moves to the head of the second. Two `w:document` in a branch use the same join on their
  bodies. A `w:body` in a `w:body` gives way to what it holds.
- **`w:pPr`, `w:rPr`, `w:sectPr`, `w:tblPr`:** one merge, the later copy applied to the earlier, at attribute and child level and one level in.
  `StyleUtil.apply` won't do: it replaces a `w:ind` whole, and leaves out a `w:sectPr` in a `w:pPr`. The merge walks the fields: a child the later copy
  states replaces, or for the elements REQ-322 names, merges with, the earlier's. A `w:headerReference` or `w:footerReference` on a later `w:sectPr` goes, with its part, and a `w:tblW` stays
  as JAXB leaves it, as Word's reading of it isn't known.
- **The final `w:sectPr` of joined bodies** go through the same merge, a body with none passed over.
- **A property repeated in one parent,** `w:ind` twice in a `w:pPr`, the same merge. A `w:jc`, `w:b` or `w:sz` with one attribute needs nothing: JAXB's last is Word's.
- **`w:txbxContent`, in a VML box:** the other way about, the later copy's content first (D6). In `wps:txbx` nothing: JAXB's second is Word's.

**Telling.** One WARN a part with a count, as CR-076 D4 has it. Word says nothing, but today's silent loss of a body is the worst of the five.

## 5. Decisions

All as recommended (Jason, 2026-11-07).

- **D1. Other parts as the root.** A header, footnotes, styles or numbering with this root are the same gap in `JaxbXmlPart`. *Recommended:* measure them in
  phase 0, then the same step for each part where Word reads the branch. Phase 0: Word reads the branch in all eight, so the step goes in `JaxbXmlPart`, each
  part type giving the root it expects, not in `MainDocumentPart`.
- **D2. Nothing selected at the root (`rw-24`).** Word shows an empty document, and WD 0.97 (portfolio 2fd79dc) now says so: REQ-305 selects nothing,
  and REQ-317 reads the part as an empty document. What is left to decide is docx4j's record. *Recommended:* the record holds the element, so the words
  stay visible to inspection.
- **D3. `docx4j.jaxb.mc.onLoad=preserve`.** The mode keeps an element whole for the consumers to choose, but a `MainDocumentPart` has to hold a
  `Document`. *Recommended:* the root resolves in both modes, and the javadoc says so.
- **D4. A save of a part that was read** writes an ordinary `w:document`, as Word's does, and the branch not taken is gone. An unread part stays bytes.
  *Recommended:* as this says. CR-051 does the same in the body.
- **D5. Which repeated children.** *Recommended:* all Word was measured on, as WD REQ-322 now lists them, in phases, the property merges last. CR-076 D10's
  order was to match Word as far as possible. The alternative was `w:body` and `w:sdtContent` alone, until a count says the rest occur.
- **D6. `w:txbxContent`, second first.** Measured in a VML box, unexplained, with two, three and four copies. *Recommended:* follow it for VML, and for
  DrawingML by what phase 0 shows. Phase 0: in `wps:txbx` the second stands alone, which is what JAXB does already, so DrawingML needs nothing. WD REQ-322
  requires nothing here.
- **D7. A WARN.** *Recommended:* one a part with a count, as §4.
- **D8. A count.** *Recommended:* add both shapes to the catalogue query that CR-076 has open, a root that isn't `w:document` and a child given twice
  in these hosts. It can reorder the phases, and nothing waits on it.
- **D9. Another root in the main part (§1.1, §2.3).** Word has no one answer, and a header's words in a separator footnote aren't a reading to copy.
  *Recommended,* as §1.1 and WD 0.100's REQ-318 to REQ-320 have it (portfolio 9034ad0):
  - A root `w:body`, `w:p` or `w:sdtContent` reads as Word reads it, in a new `Document`.
  - An unknown `w:` root reads as if its tags were absent, where it holds a `w:p`, a `w:tbl`, a `w:sdt`, a further unknown element, a `w:body`, or
    nothing. A `w:sectPr` directly in it goes. Each rests on one file, and if a rule is wrong, docx4j reads words from a file Word might refuse, which
    loses nothing.
  - The roots Word refuses take CR-065 D1's route: `w:footnotes`, `w:endnotes`, `w:tc` and §8's list; an unknown `w:` root holding a `w:document` or a
    bare `w:r`; and a root outside the understood namespaces, not `mc:Ignorable`, holding a `w:document` or a `w:p`.
  - Every other root, a branch taken that holds one, and an ignorable root, take that route for `getContent()`, the outputs and a save, so a
    `ClassCastException` becomes a message naming the root, and a save keeps the bytes where it fails today. Text gives nothing and
    `part.unexpected_root`, as Word shows nothing. `TextUtils` refuses, where today it gives the words.

  The alternative for `w:hdr`, `w:ftr` and `w:styles`, which Word opens, is an empty `Document`, as Word shows. A save would then lose the words, so it
  isn't recommended. python decided these readings in its CR-039 (Q9, Q10). core-ts's CR-028 D10 and D11 would refuse every one and leave the two
  readings for a later file.

## 6. Phases

- **Phase 0, what Word hasn't been asked.** Done: §1's round, and `rt-*` and `sm-*`.
- **Phase 1, the root.** The Java step and the template, through all three unmarshal paths, and D9. `rw-*` and the files §1.1 names as fixtures with Word's
  saves, compared as `UnknownElementsTest` compares.
  - **Built, uncommitted** (Jason: "then start CR-079 phase 1"). `JaxbXmlPart.resolveRootElement`, at the head of `resolveAlternateContent` and `afterBinderUnmarshal`, so every path reaches it: the branch Word reads gives
    the part its root, in both modes, the element recorded whole, the binder re-created. `rootFrom` is one object of the part's own class, the class read from its declared type on this path alone. `MainDocumentPart` overrides it: nothing an empty
    document, one or more `w:document` joined, a `w:body` or a `w:p` in a document of its own. The join is phase 2's in the plan and is here, as taking one document meanwhile would have lost words; the `w:sectPr` merge stays phase 3's, the last standing
    with the earlier's only where it has none. `mc-preprocessor.xslt` keeps the element at the root, so the stylesheet route reads the same branch, which closes §2.1's disagreement. Any other root in the main part fails the read with `UnexpectedRootException`, naming it, in place of a
    `ClassCastException`, and a save keeps the bytes. An element in a branch not read leaves no record.
  - **Results.** `McRootTest`: 24 files, each read as Word's save of it reads, every part type of D1, a save of each, `w:document` written (D4), `preserve` (D3), the binder's XPath, `mce.nothing_selected` with no address (REQ-801). `c79-22` is the one departure: Word refuses it and docx4j reads both documents, which loses nothing.
  - **Another root, built, uncommitted** (D9, WD REQ-318 to REQ-321). The stylesheet's rule, as §4. The part records why a read failed only after it has failed, in `getContents`, so a document that reads pays nothing: `MainDocumentPart.unexpectedRootOf` reads the root from the file, or from what stood in the branch, and tells REQ-318 from REQ-320, JAXB knowing the name being what parts a known root from an unknown. `JaxbXmlPart.getUnexpectedRoot()` gives the name; `Extractor` gives no text and `part.unexpected_root`, and a nested reading's names the embedded part. An element nothing binds in a branch goes through the same rule (`mr-40`). `Extractor` refuses a created package with no main part (REQ-321); a nested one can't get there, as its load refuses, which raises `content.not_read`. `getContent()`'s message now gives the deepest cause, so it names the root.
  - **The inspector read such a part as complete.** It walks the XML, whatever the root, so `w:hdr`, `z:madeUp` and even a root Word refuses reported `COMPLETE` with nothing skipped. `Inspect` now asks the part after the walk, not where a global limit stopped it, as reading the part is the cost the limit refused: `part.unexpected_root` or `unreadable`, `PARTIAL`, and an extraction's counts are lower bounds. That reaches any main part that can't be read, not only these roots.
  - **Results.** `MainPartRootTest` over 36 files of Word's rounds. Of the 16 Word reads, each reads as its save; the 14 it refuses are refused, a save keeping the bytes; the 6 it shows nothing of give no text and the warning. `or-14` is through Word's repair, which left no save in that job, and REQ-320 is the rule there. Mutants: 47 on the final code, each caught, the two `XPathAware` sites each caught before it. One more survived and showed a dead edit, `JaxbXmlPart.unmarshal(Element)`, which `MainDocumentPart` never reaches, and it was reverted. The site `XPathAware` takes under MOXy is the one not run: the suites here are the Reference Implementation's.
  - **Fable's review, ten findings.** Fixed:
    - The rule at the root was a second `match="/" priority="1"`, and CR-073's already stood. Xalan takes the last, so CR-073's WARN stopped, and CR-076's count was neither told nor reset. No test saw it, as nothing asserted a log. The rule is in the one template now, and a test holds the stylesheet to one template a pattern, another the two warnings.
    - An XPath as a part's first use threw on a root `w:body` or `w:p`: the binder was refreshed only for an `mc:AlternateContent` root.
    - A global element with no `@XmlRootElement` in a branch at the root, `w:txbxContent` say, threw a RuntimeException out of `getJaxbElement()`, as it was marshalled to get its name. It is named through the introspector now.
    - No `mce.branches_differ` and no inspection finding at a root, against §4's step 3 and REQ-317: records were matched by an address a root doesn't have. The text model now compares a root's branches, naming the part. The inspector restores the element as the first child of `w:document`, its read branch emptied, as the rest of the walk expects that root.
    - A join moved a `w:background` and a `w:sectPr` without their parents.
    - `part.unexpected_root` named the private `mcsdt:sdt` for a `w:sdt` directly in a branch.
    - The stylesheet's list was wider than REQ-319's. I had added `mc:AlternateContent`, unmeasured, and that read the wrong branch, unrecorded; a `w:sdt` of runs read as an empty control, its words going on a save. Both are REQ-320's as written, and the list now is too.
  - **Questions for Word from that review, none acted on.** An unknown root holding `mc:AlternateContent`, or a `w:sdt` of runs. A `w:sdtContent` root that is empty or holds rows: JAXB's first pass takes those as `CTSdtContentRow`, so they give REQ-320, where REQ-319 reads. In a branch at the root: a `w:sdt` directly; `w:txbxContent` and the like; a `w:document` beside another story's root, where the other goes silently. `mc:AlternateContent` as a root in a pptx or xlsx, now read with Word's understood set where it was a ClassCastException.
  - **Left as CR-065 D1's.** In REQ-320's state a Flat OPC save and `getDocumentModel` fail with a NullPointerException, as they do for `rw-12`.
  - **The gate and the cost.** All the suites pass, core-tests 2566 (30 skipped). Gate b389 against b388, and b388 against b387: 0 movers, events equal, probes the same, and it can't show this, as no corpus document has such a root. A document that reads on JAXB's first pass doesn't reach the stylesheet. On one that does, the rule costs nothing to measure: the stylesheet as it stands against the one committed, on the four largest main parts, 11 MB to 13 MB, the variants taking turns, medians of 9, -1.8% to +0.5%, which is noise (`CR-079-notes/CostRoot.java`, `cost-root.tsv`).
  - **The Office check, passed.** The round `cr079-root-saves`, `rs-*`: docx4j's save of the 39 files it reads, every XML part read first, each twice, the control first, with the portfolio's Word-built control ahead of them. Word 16.0.20326.20232, bridge 4dcb436, 8 November: all 80 jobs silent, no dialog. The review's fixes changed no part of any of the 39 saves, compared entry by entry, so the round stands for the code as it is. Against Word's reading of each source (`CR-079-notes/read_root_saves.py`, `rs-word.tsv`): the two runs equal in all 39, the text the same in all 39, the PDF the same to the pixel in 38.
  - **The 39th, `c79-16`, isn't this CR's.** Its line is smaller in the save's PDF, and the numbering part is the same. The source has no styles part and the save has one, docx4j's default: `PropertyResolver` asks for it with `getStyleDefinitionsPart(true)`, the harness read the text before saving, and a numbered paragraph needs the resolver. With an ordinary `w:numbering` root the same read adds the part, and with no read first a save has none. That is against WD REQ-204, in any package with no styles part, and older than the 2019 move to Maven. Fixed apart from this CR (Jason: "do the narrow fix"): the list numbering's `Emulator.resolverOf` asked `getPropertyResolver()`, which adds docx4j's default styles part; it now asks `MainDocumentPart.getPropertyResolverAddingNothing()`, a resolver over those defaults kept out of the package where it has no styles part. Across the test fixtures, 245 read, only `c79-16` and `broken-part-styles-absent` had changed, and none does now (`ReadLeavesThePackageTest`). Exporters and other callers of `getPropertyResolver()` still add the part, which REQ-204 doesn't cover; that is the broad fix, not done.
  - **Word's answers to the review's questions** (Jason: "The small Word round for the review's open questions? yes"; the round `cr079-root-questions`, `rq-*`, 10 files twice, Word 16.0.20326.20232, bridge 4dcb436, 8 November, the runs equal; `CR-079-notes/build_rq.py`, `rq-word.tsv`). docx4j as committed at 4b26e7422 departs from Word in six:
    - `w:madeUp` holding `mc:AlternateContent` (`rq-10`): Word reads the Choice it understands, silently. docx4j gives no text and `part.unexpected_root`.
    - `w:madeUp` holding a `w:sdt` of runs (`rq-11`): Word refuses. docx4j gives no text.
    - An empty `w:sdtContent` root (`rq-20`): Word reads an empty document, silently. docx4j's text is empty too, with the warning.
    - A `w:sdtContent` root holding a `w:tr` or a row-level `w:sdt` (`rq-21`, `rq-22`): Word refuses. docx4j gives no text.
    - A block `w:sdt` in a branch at the root (`rq-30`): Word reads it as the body. docx4j gives no text.
    - `w:txbxContent` in a branch at the root (`rq-31`): Word refuses. docx4j gives no text.
    - `w:document` beside `w:hdr` in a branch, either order (`rq-32`, `rq-33`): Word reads the document, the header's words in the separator footnote as `or-10`'s. docx4j reads the document, as Word does.
  So a run where a body's content stands is refused, as REQ-318 has it for a bare `w:r`, and REQ-319's list wants `mc:AlternateContent` and a block `w:sdt` in a branch.
  - **Following Word on them, built, uncommitted** (Jason: "Follow Word on the six rq-* shapes where doing so is straightforward", "REQ-318 to REQ-320 and REQ-323 as recommended"; WD 0.103, portfolio eceabd6). All six were: the stylesheet's list takes `mc:AlternateContent`, kept with `xsl:copy` so the namespaces its Choice's `Requires` names stay in scope, and the splice then reads the branch Word reads; `refusedRoot` refuses an unknown root holding a control of runs, and a `w:sdtContent` JAXB took as a row control's; an empty one is an empty document; a `w:sdt` in a root branch gets its own name back and reads through the same rule; `w:txbxContent` is refused by name. WD 0.103 makes the last two roots too, as REQ-317 brings a branch's reading to the root: a block `w:sdt` root, which JAXB takes as a row control's and empties, is the stylesheet's, in a body of its own, and a root `w:txbxContent` is refused. A `w:sdt` of runs at the root or in a branch stays REQ-320's, unmeasured. All ten `rq-*` read, are refused or are blank as Word does, `MainPartRootTest` holding each. Word opens docx4j's saves of the five it reads silently, the text and PDF as its reading of each source (the round `rq-ui-saves`, with REQ-323's files).
  - **A correction: the mutants of 4b26e7422.** Its message says 47 mutants, each caught. They didn't run: `MainPartRootTest` had gained a logback appender, which the harness's classpath lacked, so JUnit failed to load the class and every mutant read as caught. The harness now has logback and refuses to run unless the unmutated tests pass. On 4b26e7422's own tree, built for it: 45 of 47 caught. The two survivors were a root refused by its name standing in a branch (`w:tc`), and the inspector counting the branch read twice at a root; tests for both now catch them. On the tree as it stands every mutant of these rules is caught: the 47 where their code is unchanged, six of them re-aimed at code rewritten since, and 17 of the rules above and REQ-323's.
  - **Left for this phase.** A part other than the main one with nothing selected, which fails the read for now, unmeasured.
- **Phase 2, `w:body` and `w:sdtContent`.** The listener, and two `w:document` through the same join.
- **Phase 3, `w:txbxContent`, `w:pPr`, `w:rPr`,** by D5 and D6.

Each build phase ends with all the suites, mutants, the Office check on docx4j's saves, a gate against the baseline and the register, and the cost on the
four largest main parts. Hand-offs to core-ts and python go through the portfolio; each has its own row in §7.15.

## 7. Risks

- **The binder.** `Binder.updateXML` won't do at the root (CR-051 §11.1: the root stays associated with the node it removed), and a joined body
  leaves two `w:body` in the DOM an XPath reads. So both re-create the binder, whose objects are then new ones. Phases 1 and 2 test
  `getJAXBNodesViaXPath` after each.
- **The listener runs for every object.** Five `instanceof` tests on the hot path, with all five children. §3 could see no cost in a scratch form;
  §6 measures the built code.
- **MOXy** is a supported runtime, and the timing in §3 was seen on the reference implementation only. The join moves content from one object to
  another, so it holds if the field is set either side of the callback, but phase 2 runs it.
- **Parents.** Content moved between containers, and a `Document` taken from a branch, need their `getParent()` set again.
- **A second `w:document`'s other content.** A `w:background` stands from whichever states it. An `mc:Ignorable` on one doesn't reach the other (`c79-22`, one file).
- **The merge is thin at its edges.** REQ-322 rests on one difference a file, one level in, and `w:tblW` fits neither reading. The merge is a walk over JAXB's
  fields, so what it does to an element no round tried is a reading of the rule, not a measurement.
- **A consumer of a part's bytes.** A part read and saved no longer matches its source's root. D4.

## 8. Out of scope

- **`rw-25`, a `Choice` nothing understands with no `Fallback`, in a body.** Done, 06628f550 and af26a5f78: nothing is selected, as Word reads it.
- **The roots and faults Word refuses** (`foo`, `z:foo`, `w:foo`, `w:wordDocument`, `pkg:package`, and `wf-10` to `wf-17`, 14 files). docx4j loads
  them, the part reads as null, text fails, and a save copies the bytes. CR-065 D1 has the load question.
- **A header's content type on the main part (`or-40`, `or-41`).** Word refuses, and so does `WordprocessingMLPackage.load`, with a poor message.
- **A part other than the main part holding another story's root.** Unmeasured. D1 asks only about `mc:AlternateContent` there.
- **Word 2003 XML and Flat OPC opened as `.xml`.** Other formats.
- **The stylesheet's rule below the root,** which takes the Fallback in a branch it doesn't keep (`76t44`, in CR-076).
