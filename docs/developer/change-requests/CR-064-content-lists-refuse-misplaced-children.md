# CR-064: content lists refuse misplaced children at the point of the mistake

Status: ACCEPTED 2026-10-26 (Jason: "do CR-064"): D1 to D9 as recommended; phase 1 DONE 2026-10-26 (§8); phase 2 optional, open.
PROPOSED 2026-10-26, drafted by Claude Fable 5.1 (session docx4j-25) at Jason's word ("open the CR - are
these checks cheap?"), after the portfolio session's manifesto work (docx4j_manifesto.md §2, D7) asked which common
mistakes `ArrayListDocx4j.checkParent` should refuse beyond today's three. The hyperlink case Jason asked for first is
done already (49637c6e4, §1). The cost is measured (§3): cheap. Recommendations in §4; phases in §5.

## 0. Summary

docx4j's content lists are `ArrayListDocx4j`, which sets the parent of what is added and refuses a few things that are
never legal there: a table, a paragraph or bare `Text` in a paragraph (and, since 49637c6e4, in a hyperlink); a run, a
paragraph or a table in a run; `SdtContent` directly in `SdtContent`. Java's compiler cannot catch misplaced content,
because the lists hold `Object`; Word catches it on opening, with "unreadable content" and a repair that drops the
content, far from the line that caused it. This CR takes the check to the other common mistakes, in the same style
("You can't X; do Y", the kind of message a person or an LLM fixes code from), and settles two design points: the
check unwraps `JAXBElement` first, and a check derived from the JAXB annotations does not go in `add()`.

## 1. Today

`checkParent` runs in `add`, `add(int)`, `set` and both `addAll`s (`docx4j-generated-objects`,
`org.docx4j.list.ArrayListDocx4j`). It tests `e` itself, so a wrapped child (`JAXBElement<Text>` from `createRT`, the
normal way Text is added) is never tested: `p.getContent().add(factory.createRT(text))` passes today's Text-in-P rule.
`P.Hyperlink` gained P's three rules on 2026-10-26 (49637c6e4; `ArrayListDocx4jTest.hyperlinkRefusesWhatAParagraphRefuses`).

The check also runs on every load: JAXB RI's `Lister.CollectionLister.addToPack` calls `Collection.add` (confirmed in
jaxb-runtime 4.0.9's bytecode), so each child the unmarshaller binds passes through `checkParent`. Two consequences:
the cost is paid per element of every document opened (§3), and a new refusal is a load-time refusal of any document
another producer wrote that way (§6).

The wml object model has 66 classes with `ArrayListDocx4j` content lists: 36 mixed `List<Object>` fields (the
content models) and 76 typed `List<T>` fields.

## 2. What users get wrong

From the forum's "unreadable content" and "my content disappears" threads over the years (the session's reading, not
a tally; "seen" means threads with that shape exist, e.g. t781 "replace a word with a table", t1147 "disappearing
image"):

| # | Mistake | Seen | Checked today |
|---|---|---|---|
| 1 | A run, or bare Text, straight into Body or a Tc (`body.getContent().add(run)`) | often | no |
| 2 | A Drawing straight into a P instead of inside an R (the image samples wrap it in a run; the run is skipped) | often | no |
| 3 | A Tbl, or Text, in a P | often | yes |
| 4 | A String added to a content list (`r.getContent().add("hello")` compiles; marshals to nothing) | seen | no |
| 5 | A property object added to content instead of set: `sectPr` into Body, `pPr` into P, `rPr` into R, `tblPr`, `trPr`, `tcPr` | seen | no |
| 6 | Hand-built tables: a P straight in a Tr, a Tc straight in a Tbl, a Tbl straight in a Tbl | less | no |
| 7 | A Br or Tab straight in a P | plausible | no |
| 8 | The other run-level containers (fldSimple, ins/del, smartTag, customXml run, sdt run content) given a Tbl, P or Text | rare | no |
| 9 | A P in run-level sdt content, an R in block-level sdt content | rare | no |

Not for `checkParent`, because they are about order or completeness, not the child added: a Tc's last block must be a
P (Word's cell last-block rule, measured 2026-10-21), `pPr` first in a P, a Tr needing a Tc. Those belong to a tree
validator (D8).

## 3. Cost (measured 2026-10-26)

Microbenchmark on the installed 17.3.5-SNAPSHOT jars (Java 17, best of 7 rounds of 2M adds, 1024 pre-made children
re-added; a prototype list `ExtList` carrying the whole rule set of §4, with the JAXBElement unwrap, stands for the
proposal):

| List, child, parent | ns per add |
|---|---|
| plain `ArrayList`, R | 7 |
| `ArrayListDocx4j` today, R into P | 8 |
| `ArrayListDocx4j` today, P into Body | 8 |
| `ArrayListDocx4j` today, `JAXBElement<Text>` into R | 10 |
| proposed, R into P | 9 |
| proposed, P into Body | 11 |
| proposed, P into Tc | 11 |
| proposed, `JAXBElement<Text>` into R | 12 |

So today's check costs 1 to 3 ns per add over a plain list, the proposal 2 to 5 ns. On a synthetic document of 30,000
paragraphs of three runs each plus a 300 x 4 table (274 KB docx; 215,101 list elements in the main part, i.e. that
many adds on load), loading takes 390 ms (median of 7), so the checks are about 0.5 ms today and about 1 ms under the
proposal: a quarter of a percent of the load. A reflective check (a `HashMap` lookup per add against a per-class allowed
set) was not measured; it is a different order from a handful of `instanceof` tests, and §4 D8 keeps it out of `add()`
for the reason that matters more, a wrong refusal.

## 4. Decisions

| # | Question | Options | Recommendation |
|---|---|---|---|
| D1 | Unwrap `JAXBElement` before testing? | (a) yes, test the value; (b) no | (a). The wrapped forms are the normal ones for Text, Hyperlink and Tab, so today's rules miss the common call. One line. |
| D2 | Block-level containers (Body, Tc, SdtContentBlock, CTTxbxContent, Hdr, Ftr, CTFtnEdn, Comments.Comment, CTCustomXmlBlock) refuse R, Text, Drawing, Br, Tc, Tr? | (a) yes; (b) Body and Tc only | (a): one set, one message ("add it to a paragraph (P) and add that"). |
| D3 | Run-level containers (P, P.Hyperlink, CTSimpleField, RunIns, RunDel, CTSmartTagRun, CTCustomXmlRun, CTSdtContentRun) refuse Tbl, P, Text, Drawing, Br? | (a) yes, keyed on the set; (b) P and Hyperlink only | (a): the set costs nothing more than P alone. |
| D4 | Tbl refuses Tc, P, R, Tbl; Tr refuses P, R, Tbl? | (a) yes; (b) no | (a). Refuse the known-wrong classes only; a Tbl's content legally holds Tr, SdtRow, CustomXmlRow, ins/del, bookmarks and mc:AlternateContent, so never "allow only". |
| D5 | Property objects refused in content lists (PPr in P, RPr in R, SectPr in Body, TblPr in Tbl, TrPr in Tr, TcPr in Tc) with "use setPPr"? | (a) yes; (b) no | (a). |
| D6 | A String refused? | (a) in the containers of D2 to D4; (b) everywhere; (c) no | (a). Typed `List<String>` fields exist in the model, so never (b). |
| D7 | Exception type | (a) `IllegalArgumentException` (a `RuntimeException`, as today's, so no caller changes); (b) keep bare `RuntimeException` | (a). |
| D8 | A check derived from the JAXB annotations (@XmlElementRefs plus ObjectFactory's @XmlElementDecl scopes, 868 declarations in wml) | (a) in `add()`, cached per class; (b) a separate tree validator, same messages, for tests, the inspector and the MCP server; (c) not at all | (b), later. In `add()` it would have to accept subclasses, lax Object content, mc:AlternateContent and DOM-bound unknowns, and a wrong refusal is a load failure on a legal document, the worst outcome for a library whose job is to open what Word wrote. |
| D9 | Gate for each new rule | (a) the test suite plus loading the fidelity corpus; a rule that trips on real documents is dropped or demoted to a log warning; (b) the suite alone | (a). The check runs on load (§1). |

## 5. Phases

| Phase | Work | Status |
|---|---|---|
| 0 | This CR, with the cost measured (§3) | done 2026-10-26 |
| 1 | D1 to D7 in `ArrayListDocx4j.checkParent`; `ArrayListDocx4jTest` covering each rule and each container set (accepted children too: an R in a hyperlink, a Tr in a Tbl, a `JAXBElement<Text>` in an R, mc:AlternateContent anywhere); CHANGELOG; the D9 gate run and its numbers recorded here | DONE 2026-10-26 (§8) |
| 2 | D8 (b), the tree validator, if wanted after phase 1 | optional |

Estimate for phase 1: a day, most of it the gate.

## 6. Risks

- **Refusing a legal document on load.** Every rule refuses classes that are never legal under that parent in the
  schema, so a legal document cannot trip one. A broken one from another producer can (docx4j today repairs others'
  output, where Word would), hence D9; the fallback is a log warning for that rule.
- **Generated code.** `ArrayListDocx4j` is hand-written in `docx4j-generated-objects`, so the module rebuilds (XJC
  runs again) but nothing is regenerated differently.
- **Performance.** Measured, §3.

## 7. Hand-offs

The TypeScript and Python ports have their own list types; whether they refuse misplaced children at add time is
their question, sent with phase 1's hand-off, with this CR's table of mistakes as the list worth catching.

## 8. Phase 1 done (2026-10-26)

Jason, 2026-10-26: "do CR-064" (D1 to D9 as recommended). `ArrayListDocx4j.checkParent` (hand-written,
docx4j-generated-objects) now unwraps a `JAXBElement` first (D1) and tests five parent sets, each refusing only
classes the schema never allows there, with `IllegalArgumentException` (D7) and the fix in the message:

- **run-level containers** (P, P.Hyperlink, CTSimpleField, RunIns, RunDel, CTSmartTagRun, CTCustomXmlRun,
  CTSdtContentRun; D3): Tbl ("it should be a sibling of the paragraph"), P, Text, Drawing, Br ("add it to a run (R)
  and add that"), PPr ("use setPPr"; D5), String ("put it in a Text, in a run (R)"; D6);
- **R**: R, P, Tbl (as before), RPr ("use setRPr"), String ("a Text (wrapped by the ObjectFactory's createRT)");
- **block-level containers** (Body, Tc, SdtContentBlock, CTTxbxContent, Hdr, Ftr, CTFtnEdn, Comments.Comment,
  CTCustomXmlBlock; D2): R, Text, Drawing, Br ("add it to a paragraph (P) and add that"), Tc, Tr ("a cell goes in a
  row (Tr), in a table (Tbl)"), SectPr ("use Body.setSectPr, or PPr.setSectPr on the paragraph that ends the
  section"), TcPr in a Tc ("use setTcPr"), String;
- **Tbl** (D4): Tc ("add it to a row (Tr)"), P, R ("a cell (Tc) in a row (Tr)"), Tbl ("a nested table goes in a cell"),
  TblPr, String; **Tr**: P, R, Tbl, TrPr, String;
- **SdtContent in SdtContent**, as before.

The message names the container ("You can't add Text directly to a hyperlink; ...", "... to the body", "... to a
table cell (Tc)", "... to a footnote or endnote"), so the reader knows which object was wrong. Nothing else is
refused: mc:AlternateContent, range markup, altChunk, the block- and run-level sdt and customXml wrappers and the
wrapped forms (a `JAXBElement<Tc>` in a Tr, a `JAXBElement<Text>` in an R) are accepted, and the tests say so.

**The D9 gate.** A harness (scratchpad `loadall/LoadAll.java`) loads every docx under the fidelity corpus
(`cloud-corpus/complex-Word-ND/worked-docx`, 454) and the repository (596: the test resources, samples and
fidelity fixtures) and unmarshals every JAXB part, so every child the unmarshaller binds passes through
`checkParent`. Before and after the change: 1050 files, 12575 parts read, 0 part failures, 0 refusals, and the
same single pre-existing load failure (a corpus file over the zip-bomb limit). No rule was dropped or demoted. The full build then found the one trip, in docx4j's own code: the export
preprocessor `Containerization.groupRuns` wrapped a paragraph's bordered runs in a block-level sdt (`SdtBlock` with
`SdtContentBlock`) as a conversion-only holder, which the new block-container rule refuses ("You can't add a run (R)
directly to block-level sdt content"; `VisitorParityTest.testPhase2Containers`). It now uses a run-level sdt
(`SdtRun` with `CTSdtContentRun`), which marshals as the same `w:sdt` the stylesheets and the FO visitor match by tag
(`SdtElement`), so the output is unchanged and the FO test module passes. That is the check doing its job on the
library's own code, the kind of mistake §2 lists. The
reason a legal document cannot trip one is structural as well as measured: none of the containers carries a lax
`@XmlAnyElement`, so the unmarshaller adds to a content list only a child its content model binds there, and a
misplaced element in another producer's file (a `w:r` in `w:body`) is an unexpected element the event handler
skips, never an add. `ArrayListDocx4jTest` (9 tests): each set's refused and accepted children, the message naming
the container, every add path (`add`, `add(int)`, `set`, both `addAll`s) refusing with the list unchanged.

**Cost.** The 30,000-paragraph document of §3 loads in 365 ms best, 381 ms median (9 runs) with the final code,
against 358 to 368 best and 364 to 393 median measured three times on the same file for CR-065 phase 2 the same
morning: within the run-to-run noise, as §3's microbenchmark predicted (about 1 ms of the load).

