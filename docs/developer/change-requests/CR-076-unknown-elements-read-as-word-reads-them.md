# CR-076: an element Word doesn't know read as Word reads it

Status: ACCEPTED 2026-11-06 (Jason: "CR-076 D2–D11 as recommended; commit then start implementing"; proposed the same day, "write the small CR."). Raised by core-ts's Word check 76 (core-ts CR-026 §4.4,
80a4707), where Jason decided core-ts's reading the same day (its D7: "read as Word does"). Round 6's two files, run the
same day, added (D7 here). WD 0.92 §3.1 (REQ-308 to REQ-312, uncommitted at this writing) follows Word's reading, on Jason's
ruling on the discussion paper's §7.14: "we adopt 'as word'". Rounds 7 and 8, the same day, widened and corrected §4. Decisions in §5,
all decided; phases in §6.

## 0. Summary

An unknown `w:` element, `w:madeUp` say, loses docx4j text that Word 365 shows. In a content host Word takes the tags
away and reads what they held, at any depth. docx4j drops the element with all it holds, leaving only a log WARN. All 31
of check 76's files of this shape lose their words here, and a save then writes them without those words (§2.1). A run in
a `w:pPr`'s unknown element comes through in Word too, and is lost here the same way. All told 40 of check 76's files lose words
here that Word shows.

In a `w:t` it is the other way round. Word drops the element with all it holds, and docx4j reads through it, by a rule
CR-073 added and no release has shipped. 17.3.5 loses the text before the element instead (§2.2).

These are small changes to `mc-preprocessor.xslt`. A scratch trial matches Word's saves on check 76's files, but for a field's `w:instrText` and a kept branch's own text (§3, D11),
at a cost of about 3 to 5% of the preprocessor's time on the largest documents. No corpus document here has the shape, so this
is about one reading across the ports and about not losing text, not about a measured population (§2.3).

## 1. What Word does (core-ts's check 76)

Jason's Word runs of 2026-11-05 and 2026-11-06, five rounds, and a sixth to eighth by the portfolio session through the bridge on
2026-11-06 (Word 16.0.20326.20232, each file twice, the two runs of rounds 7 and 8 agreeing). The sources come from core-ts's `test/fixtures/check76/build.mjs`. Word's
saves and PDFs are in its `test/check76/`, with a SHA-1 manifest, round 6's since 1ffb120, and rounds 7 and 8's are there too. The tables are in core-ts's CR-026 §4.3 and §4.4 and CR-021 §11.

| Where the unknown `w:` element stands | Word 365 |
|---|---|
| Twenty content hosts: `w:p`, `w:body`, `w:tc`, `w:r`, `w:tbl`, `w:tr`, `w:sdtContent` (block, run, row, cell), `w:hyperlink`, `w:ins`, `w:hdr`, `w:comment`, `w:fldSimple`, `w:smartTag`, `w:customXml` (block, run, row, cell), `w:footnote`, and from round 7 `w:ftr`, `w:endnote`, `w:del`, `w:moveFrom`, `w:moveTo` | The tags go and what they held stays, in its host (tracked as inserted in a `w:ins`, deleted in a `w:del`, moved in a `w:moveFrom` or `w:moveTo` with the range markers kept, in the link in a `w:hyperlink`). No repair. Nested in another unknown element, the same (`76t7`, and in a `w:tc` and a `w:hyperlink`, `76t35`, `76t36`). Another name, the same (`76t51`). `mc:Ignorable` plays no part where tested (`76t8`) |
| Directly in the `mc:Choice` or `mc:Fallback` Word takes, in a `w:p` | Read through: the words in the paragraph, the other branch's gone (`76t44`, `76t45`) |
| Holding text directly, in a `w:p` | The text goes (`76t41`) |
| Holding a `w:br`, in a `w:p` | A break, at the start of the next run (`76t42`) |
| Holding a `w:p`, last in a `w:p`, with a run before it | A held `w:p`'s mark ends a paragraph: the first's runs join the host, with its properties if it has any and the host's if not, a further one stands alone, and a paragraph with the host's properties follows (`76t43`, `76t56`, `76t58`, `76t68`, `76t69`), at any depth (`76t70`). In a `w:hyperlink` too, whose words stay linked (`76t71`) |
| Holding a `w:p`, with a run after it or none before it | Word refuses the file, in both runs (`76t53` to `76t55`, `76t57`, `76t59`) |
| `w:pPr` | A property in it goes and has no effect (`76t5`). A run in it is read, at the head of the paragraph: `HIDDEN WORDSTARGET TEXT`, one run in Word's save, opened with no message (`76t27`). Several runs, in order (`76t37`). A run and a property together: the run read, the property gone (`76t38`). With no other run, a paragraph of those (`76t39`) |
| A `w:pPr` under a `w:pPrChange` | A run is read at the head of the paragraph, the `w:jc` stands, and the change comes out with `w:author="Unknown"` and no date (`76t40`) |
| A `w:sectPr` in a `w:pPr` | A run is read at the head of the paragraph, and the `w:sectPr` goes, with its section break (`76t50`) |
| `w:sdtPr` at block level, `w:trPr`, `w:tcPr`, `w:tblPr` | With a run in it Word refuses the file, in both runs (`76t46` to `76t49`) |
| `w:rPr` | A property in it goes and has no effect (`76t6`). With a run in it Word refuses the file: "Word experienced an error trying to open the file." (`76t28`) |
| `w:t` | Gone with all it holds (`76u`) |

Round 4 and 5's controls (`76t18`, `76t26`) hold the same hosts with nothing unknown. Each file's save matches its host in
the control's save, so the element changed nothing but itself. That includes two things Word always does: it writes a
`w:fldSimple` as a complex field, and it takes away a `w:customXml`'s tags.

Inside a text element Word's rule is CR-073's rule 2, finished (core-ts CR-021 §11):

| In a `w:t` | Word 365 |
|---|---|
| `w:br` | A break (CR-073) |
| A nested text element | Its text (CR-073) |
| Any other `w:` element: `w:madeUp`, `w:tab` | Gone with all it holds (`76u`, `76z`) |
| A namespace Word doesn't know, ignorable | Gone with all it holds, a `w:br` included (`76n`, `76w`) |
| The same, not ignorable | Could not repair it (`76v`) |
| No namespace | Could not repair it (`76x`, `76y`) |
| A namespace Word knows, not `w:` | Not measured |

Not measured:

- `w:txbxContent`, `w:docPartBody`.
- `w:sdtPr` at run level, the body's own `w:sectPr`, and a property rather than a run in `w:sdtPr`, `w:trPr`, `w:tcPr`, `w:tblPr` or `w:sectPr`.
- Holding what its host doesn't admit, beyond `76t42` and `76t43`: text or a `w:br` directly in another host, a `w:p` with content after it.
- In a `w:pPr`, a `w:hyperlink` or a `w:p`, and depth: an unknown element in an unknown element.

## 2. What docx4j does (measured 2026-11-06, code of 9e3100351)

### 2.1 Content hosts

1. The part's first unmarshal meets `w:madeUp`, `JaxbValidationEventHandler` logs "unexpected element", and the read fails.
2. `JaxbXmlPartXPathAware` runs the part through `mc-preprocessor.xslt`. No template matches, so the identity template
   copies it.
3. The second unmarshal continues past unexpected content, and skips the element with all it holds.

Check 76's sources, the words `HIDDEN WORDS` counted in each part before and after a load, less the file's own description
line:

| Files | docx4j |
|---|---|
| `76t`, `76t1` to `76t4`, `76t7` to `76t17`, `76t19` to `76t25` (23 files) | The words gone in all 23, in the header, comments and footnotes parts as in the main part. A WARN in the log, nothing to the caller. The marshalled part lacks them, so a save does too |
| `76t29` to `76t33` (round 7: `w:ftr`, `w:endnote`, `w:del`, `w:moveFrom`, `w:moveTo`) | The words gone in all five, in the footer and endnotes parts as in the main part |
| `76t35` to `76t40`, `76t43`, `76t50`, `76t51` (round 8) | The words gone in all nine. `76t42` loses its break. `76t41` matches, the text going as in Word |
| `76t44`, `76t45` (a branch) | The first unmarshal passes, so nothing runs. The branch is kept with the `w:madeUp` in it, a save writes it back and Word reads it, but docx4j's own text is `TARGET TEXT, then  and the rest.` |
| `76t46` to `76t49` | Read, the element dropped, where Word refuses (§8) |
| `76t18`, `76t26`, `76t34`, `76t52` (controls) | All the words kept |
| `76t5`, `76t6` | The `w:jc` and `w:b` gone, as Word |
| `76k` (an ignorable element holding a run) | Dropped, as Word |
| `76t27`, a run in a `w:pPr`'s element | `TARGET TEXT`: the words gone, as in the 23 |
| `76t28`, a run in a `w:rPr`'s element | `TARGET TEXT`: read, where Word refuses the file (§8) |

### 2.2 In a text element

`mc-preprocessor.xslt`'s `*` template in mode `split-text`, `@since 17.3.6`, gives every element's text. CR-073 measured that
for a nested `w:t`, which Word knows.

| File | Word | 17.3.5 | This branch |
|---|---|---|---|
| `76n`, ignorable element holding text | `TARGET TEXT` | `TEXT` | `TARGET HIDDEN WORDSTEXT` |
| `76u`, `w:madeUp` holding text | `TARGET TEXT` | `TEXT` | `TARGET HIDDEN WORDSTEXT` |
| `76w`, ignorable element holding a `w:br` | `TARGET TEXT`, no break | `TEXT` | `TARGET `, a break, `TEXT` |
| `76m`, `76z` | As this branch | `TEXT` | As Word |

core-ts (33bae48) and python (23cd141) have changed the rule, so 17.3.6 as it stands would ship a reading both have left.

17.3.5, the last release, loses `TARGET ` on all of these, and on `76v`, `76x` and `76y`, which Word refuses. It keeps only the text
after the last child element, with a log WARN, as CR-073 found for a `w:br` in a `w:t`. docx-tool-identifier measured that on
2026-11-05, and the 17.3.5 jar read here gives the same. So anyone on 17.3.5 loses words Word shows, and CR-073 and rule 2 between
them mend that.

### 2.3 Reach

- None of the fidelity corpora's 561 documents holds a `w:` name outside `xsd/wml/wml.xsd`, in any `word/*.xml` part. So
  those documents use no name that Word knows and the schema doesn't.
- None of core-ts's 720 packages holds an unknown element (its CR-026).
- The load survey's catalogue would say more, by the local name in `docx4j_load_messages`' "unexpected element" WARNs. Neither
  this session nor the portfolio's can reach it. `catalogue.db` is 99 GB in `s3://plutext-docx-corpus/catalogue/`, the home copy
  is planned from the week of 9 November (portfolio `scripts/corpus/STARLINK-PLAN.md`), and a query on AWS would be a cost for
  Jason to approve. So the reach in the survey's 3.7 million documents isn't known.

## 3. A trial (2026-11-06, scratch, the tree untouched)

The property `docx4j.jaxb.JaxbValidationEventHandler` points at a copy of the stylesheet carrying §4's rules, with the
hosts and `wml.xsd`'s names. Rule 2 names check 76's namespace, where §4 asks the understood set. Each part read is
compared with the same part of Word's save, by its paragraphs, rows, cells and words:

| Files | Result |
|---|---|
| The 23 read-through files | 22 match. `76t19` differs only in the `w:instrText` of the complex field Word writes for a `w:fldSimple`; its words match |
| `76t29` to `76t33`, `76t34` (round 7, the twenty hosts) | All six match, the words in their `w:del`, `w:moveFrom` and `w:moveTo` and the range markers kept |
| `76t53` to `76t71` (rounds 9 to 11) | The fourteen Word saved match, `76t71` with its link kept where Word writes a field. Those it refused are read for what they hold |
| `76t35` to `76t43`, `76t50`, `76t51` (round 8) | All eleven match. `76t42` read separately: a run with the break where Word has it at the head of the next run, so the same reading. `76t40`'s change keeps its author and date (D8) |
| `76t44`, `76t45`, `76t52` | `76t52` reads as Word. The other two hold their words in the kept branch, unread (D11). With a second unknown element in the part, so that the stylesheet runs, `76t45` reads as Word and `76t44` doesn't (§7) |
| `76t18`, `76t26` (controls) | `76t18` matches. `76t26` differs as `76t19` does, with nothing unknown in it, so the difference is the field's and not this rule's |
| `76m`, `76n`, `76u`, `76w`, `76z` | All match. The comparison doesn't see breaks, so `76w` was read separately: no break, as Word |
| `76k`, `76t5`, `76t6` | Match |
| `76t27` | `HIDDEN WORDS`, then `TARGET TEXT`, in two runs. Word's save has one, as it joins adjacent runs of the same formatting; the text is the same. `76t5`'s `w:jc` still goes |
| `76t28` | `TARGET TEXT`, unchanged. Word has no save to compare |

§4's walk and host test changed the same day, on docx4j-python's CR-037 §8.2 (77b0202). The first form walked past anything
outside `w:` to the nearest schema name, and took `w:ins` and `w:footnote` by tag. The two forms read the 45 check 76 files
that had a Word save before round 7 alike: 43 match, and `76t19` and `76t26` differ as above. The twenty hosts read those 45 the same. They part only on shapes no Word run has tried,
here in scratch files built on `76-control`:

| A `w:madeUp` | Today | First form | §4's form |
|---|---|---|---|
| Holding a `w:t`, in an `m:r` | Dropped | Words read, a `w:t` in the `m:r` | Dropped |
| Holding an `m:t`, in a `w:ins` in an `m:r` | Dropped | Words read | Dropped |
| Holding a `w:rPr`, in a `w:ins` in an `m:ctrlPr` | Dropped | The `w:rPr` kept | Dropped |
| Holding a run, in a `w:ins` in a `w:rPr`, `w:numPr` or `w:trPr` | Dropped | Dropped, as that `w:ins` takes no run | Dropped |
| Holding a run, in an `ext:opaque` in a `w:p` | Gone | Gone, with the `ext:opaque` | Gone |
| Holding a run, in a `w:ins` in a `w:p`; an `m:r`, in one in an `m:oMath` | Dropped | Read | Read |
| Holding a run, in a branch's `w:p` | Dropped | Read | Read |

Time in the stylesheet, best of eight, on the four largest main parts in the corpora (8 to 12 MB), all the rules in the form
§4 gives: stock 661, 2691, 1128 and 1383 ms, trial 733, 2772, 1163 and 1435. That is +3.0% to +3.7% on the last three and
+10.8% on the first, whose stock time was a low reading: 698 to 713 ms in the four other runs, which would make it +3% to +5%.
A later run set both forms side by side: stock 655, 2509, 1101 and 1310 ms, the first form 733, 2653, 1150 and 1380, §4's form
734, 2679, 1148 and 1380. The revised test costs nothing, as it is asked only of an unknown name. That run puts the whole at
+4.3% to +6.8% on the last three and +12.1% on the first, whose stock time was low again.
The form §4 now gives, side by side with stock in a third run: 656, 2486, 1106 and 1322 ms against 722, 2559, 1121 and 1338, so
+1.2% to +3.0% on the last three and +10.0% on the first, low again. As built, with the `w:p` template of rounds 9 to 11, in a fifth run: 656, 2465, 1097 and 1312 ms
against 727, 2584, 1130 and 1358, so +3.0% to +4.8%, and +10.8%.

The form matters. Rule 1 alone is -0.9% to +8.9%, nearly all of it under 4%. The same rule 2 written as two new match patterns
in mode `split-text` took the whole to +14% to +24%, in every run, though that mode is rarely entered. Why wasn't traced. Moved
inside the existing template, it costs next to nothing. Round 8's tests went the other way. Added to the catch-all, which every
`w:` element reaches, they took the whole to +10.7% to +14.6%. In templates of their own with plain patterns, `match="w:pPr"`,
`w:p` and `w:sectPr`, nothing but those elements pays, and the catch-all has less to do than before.

The stylesheet runs only on a part whose first unmarshal failed (6.64% of the survey's documents), on a part named in
`docx4j.jaxb.preprocess.always`, and on a part read from a DOM under MOXy.

The trial doesn't compare run properties or tracked-change attributes, and it didn't run the test suites. Those are phase
1's.

## 4. Approach

Both rules go in `mc-preprocessor.xslt`, beside CR-073's. Every route that preprocesses takes that stylesheet for
WordprocessingML: `JaxbXmlPart` and `JaxbXmlPartXPathAware` for a part, `XmlUtils` for a fragment, and the pre-emptive
transform under MOXy.

**Rule 1, reading through.** An element in the `w:` namespace whose name isn't in the schema gives way to its element children,
when the nearest ancestor that isn't itself such an element is one of the twenty hosts. Everything else stays as it is: its
attributes and any text directly in it go, and the second unmarshal goes on dropping what stands in a host outside the list.

- Walking up past unknown `w:` elements alone is what makes "at any depth" work, and it keeps a `w:madeUp` in a `w:pPr` out.
  An element of any other namespace stops the walk. So a `w:madeUp` in an `ext:opaque` isn't read through to the `w:p` beyond
  it (`76k`: Word drops the `ext:opaque` with all it holds), nor is one in an `m:r`, where the words would otherwise come through
  (§3). `mc:` elements don't stop it: `76t44` and `76t45` read through directly in the branch Word takes.
- A `w:br` it gives way to, in a `w:p` or a `w:hyperlink`, goes in a run of its own (`76t42`), since CR-073's rule 1 names its parents
  and wouldn't take it. Word's save has it at the head of the next run, which reads the same.
- A `w:p` held in a `w:p` (D10): a held `w:p`'s mark ends a paragraph. Where the element is the host's last and holds only `w:p`, at any
  depth, the first's runs join the host and its `w:pPr`, if it has one, replaces the host's, a further one stands as its own paragraph,
  and a paragraph with the host's `w:pPr` and nothing else follows, the host's own mark (`76t43`, `76t56`, `76t58`, `76t68` to `76t70`).
  In a `w:hyperlink` last in the `w:p`, the runs stay in the link and the host's mark follows (`76t71`): Word's save has a complex field
  there, whose end stands in that next paragraph, and docx4j keeps the `w:hyperlink`. In the shapes Word refuses, the runs stay where they stand.
- A host is a declaration, not a tag (python's CR-037 §8.2). `wml.xsd` also declares `w:ins` as `CT_TrackChange` in a `w:numPr`,
  `w:rPr` and `w:trPr`, as `CT_RPrChange` in an `m:ctrlPr`, and as `CT_MathRunTrackChange` in an `m:r`, which does hold math
  content. `w:del` has the same but for the `w:numPr`, `w:moveFrom` and `w:moveTo` are `CT_TrackChange` in a `w:rPr`, and
  `w:footnote` and `w:endnote` are `CT_FtnEdnSepRef` in a `w:footnotePr` and `w:endnotePr`. No file measures those, so the test
  reads the parent and leaves them out, which takes an `xmlns:m` in the stylesheet. Any other `w:ins` is the run-level declaration
  that `76t15` measures in a `w:p`, so one in an `m:oMath` counts. The other fourteen names have no such second use.
- XSLT 1.0 allows no variable in a match pattern, so the template matches `w:*` and tests inside it. That makes it the
  catch-all for every `w:` element no named template takes, and its other branch has to do exactly what the identity
  template's `usual-prefix` branch does.
- Whether an element is unknown is a test on its name alone. A `w:` name the schema has, standing where it can't, is not this rule's: CR-073 and the
  nested-paragraph templates deal with some of those, and Word's reading of the rest is not measured.
- "Unknown" means unknown to the schema, not to Word. A name Word knows and the schema doesn't would read through rather than
  drop. §2.3 found no such name in the corpora, and phase 1's catalogue query looks wider. The WD says the standard's names, and
  `wml.xsd` is short of an ECMA-376 transitional schema by nine (python's CR-037 §8.2): `w:contentPart`, `w:header`, `w:headers`,
  `w:label`, `w:longDesc`, `w:objectEmbed`, `w:objectLink`, `w:tabIndex` and `w:title`, none in the list here either. By python's
  reading eight stand only under non-hosts, and `w:contentPart` in a `w:r` holds nothing, so nothing reads differently.

**Rule 1b, in a `w:pPr`** (D7). A `w:r` directly in an unknown element directly in a paragraph's `w:pPr` goes straight after that
`w:pPr`, in order, and anything else in the element goes with it (`76t27`, `76t37`, `76t38`, `76t39`). So does one in the `w:pPr`
under its `w:pPrChange` (`76t40`, D8), and one in its `w:sectPr` (`76t50`), when the `w:sectPr` goes (D9). A `w:madeUp` nested in
one in a `w:pPr` stays out, as nothing measures it, though `76t7` shows depth in a `w:p`. It needs `parent::w:p`: a style's or a
level's `w:pPr` has no paragraph to take a run. Nothing changes for `w:rPr`: Word refuses a run there, and a property goes as it does today.

These go in plain templates of their own, `match="w:pPr"`, `w:p` and `w:sectPr`, not in the catch-all (§3).

**Rule 2, in a text element** (D3). In mode `split-text`, a `w:` element other than `w:br` and the text elements goes with
all it holds, as does one in a namespace outside `McSelection.understoodNamespaces()`. One in no namespace, or in a known
namespace other than `w:`, gives its text as it does today. Word refuses the first and is not measured on the second, so
that is docx4j's and core-ts's reading until a check says otherwise. The test goes inside that mode's existing `*` template,
not in new match patterns (§3). A small `XSLTUtils` hook reaches the set, and only for an element inside a text element, so it
costs nothing elsewhere.

WD 0.93 §3.1 says the same, but for round 8, which it hasn't taken up yet: REQ-308 is rule 1, REQ-309 rule 2, REQ-311 rule 1b, and REQ-310 and REQ-312 what docx4j does today.
Its definition of an unknown element uses the extractor's understood set (REQ-306), which is the set rule 2 asks. REQ-311 says a
run held directly, as rule 1b does. An element of a namespace Word knows, such as `w14:` or `m:`, in a `w:t` is left unmeasured and
unrequired, so rule 2 doesn't conflict with it. One file would measure that shape.

The save writes what was read, the tags gone, which is what Word's own save does with no message. core-ts's D7 refuses a
changed part holding such tags until `checkLossOnSave` is off. docx4j has no such gate, and this CR adds none.

## 5. Decisions

D1 as its row says. D2 to D11 as recommended (Jason, 2026-11-06).

| # | Question | Options | Recommendation |
|---|---|---|---|
| D1 | Which hosts. | (a) The fifteen measured, as core-ts; (b) those and `w:ftr`, `w:endnote`, `w:del`, `w:moveFrom`, `w:moveTo` by analogy; (c) a sixth Word round first, for the ports too. | **DECIDED (c), 2026-11-06.** Jason: "do a sixth Word round first, including w:ftr, w:endnote, w:del, w:moveFrom, w:moveTo". Recommended was (a). core-ts counts `76t27` and `76t28` as round 6, so this "sixth" is its round 7, the same round. Word read through in all five, so §4 has twenty hosts (Jason: "Twenty hosts: yes"). `w:ins` and `w:footnote` count only as the declarations measured (§4), as in python. |
| D2 | Where the schema's names come from: 605 names, 7.5 KB. | (a) A literal in the stylesheet, with a test holding it equal to `xsd/wml/wml.xsd`; (b) a Java hook reflecting the JAXB model, asked per element; (c) a resource generated at build. | (a). It has no per-element call and no build step, and the test stops it drifting. A schema change (`adding-a-schema.md`) then fails the test until the list follows. |
| D3 | CR-073's rule 2: take core-ts's and python's rule (§4). | (a) Yes, here; (b) leave it. | (a). It is unreleased, wrong against Word, and the other half of the same rule. |
| D4 | Logging. | (a) A WARN once a part with a count, as CR-073 D3; (b) as today, JAXB's WARN alone. | (a). A read-through changes the tree, and the existing WARN says the content was unexpected, not what became of it. |
| D5 | Which release. | (a) 17.3.6, with CR-073's rules; (b) later. | (a). D3 has to be before 17.3.6 ships, and rule 1 is the same file and the same tests. |
| D6 | Hand-offs. | python, core-ts and ts-editor, through the portfolio. | After phase 1. core-ts's oracle lists this as a difference until then. python has built the fifteen (its CR-037, 77b0202) and has rule 2 done, and core-ts has decided them, so round 7's five go to both. |
| D7 | A run in a `w:pPr`'s unknown element (`76t27`). | (a) Rule 1b here, direct runs alone, as measured; (b) leave it dropped. | (a). It is the same loss of words Word shows, and a few lines. core-ts's oracle rows agree (2026-11-06), and its CR-026 D8, decided at this width (Jason there: "CR-026 D8: yes", 1ffb120), is the same rule, so (a) keeps the two in step. |
| D8 | `76t40`: Word reads a run under a `w:pPrChange`, and the change loses its author and date. | (a) Read the words and keep the author and date; (b) read them, and write `Unknown` as Word does; (c) leave it dropped. | (a), as core-ts's CR-026 D9 recommends. The words are the reading. `Unknown` would destroy what the file says. python keeps this dropped today, so it needs the change too. |
| D9 | `76t50`: Word reads a run in a paragraph's `w:sectPr`, and the section break goes. | (a) Read the words and drop the `w:sectPr`, as Word; (b) read the words and keep the section; (c) leave it dropped. | (a), as core-ts's D10: it is Word's reading, and pagination follows it. It is one file, with the element first in the `w:sectPr`. |
| D10 | `76t43`: a `w:p` in an unknown element in a `w:p`. | (a) As Word: its runs join the host, an empty paragraph follows; (b) leave it dropped. | (a), as core-ts's D11. Rounds 9 and 11 widened it (Jason: "CR-076 D10 fix to match word as far as possible"): §4. |
| D11 | `76t44`, `76t45`: the element directly in a kept branch, when nothing else sends the part through the stylesheet. | (a) A pass after unmarshal, reading such a DOM element through; (b) leave it: the branch stays whole, a save keeps it and Word reads it, and docx4j's own text misses those words. | (b) for now. A branch's `any` takes the unknown element with no event, so the stylesheet never hears of it, and (a) is new code on the load path for a shape no corpus document holds (§2.3). Rule 1 still reads it through when the stylesheet runs for another reason. |

## 6. Phases

- **Phase 0, the evidence.** DONE: core-ts's check 76 (§1), docx4j's readings (§2), the trial (§3), and D1's further Word round,
  `76t29` to `76t34`, built by core-ts's `build.mjs` so the ports read one set, and run through word-bridge by the portfolio
  session.
  A further round, core-ts's round 8, `76t35` to `76t52` (Jason: "The five extra shape groups from the portfolio: run them as
  another round"), is DONE too, and corrected §4 on `76t40`, `76t44` and `76t45`.
- **Phase 1, the rules** (§4, by D1 to D5 and D7 to D11) and tests.
  - The sources and Word's saves copied in beside `br-placement/`. A test loads each file and compares it with Word's save:
    paragraphs, rows, cells, words, run properties, and that the words stay in their host. Plus the legal shapes, §3's second
    table, a `w:madeUp` in a `w:footnotePr`'s `w:footnote`, and the list test (D2).
  - All the suites and a corpus gate. The gate should show no mover: no corpus document has the shape, and only the catch-all
    template could move one.
  - A CHANGELOG entry.
  - The commit to the portfolio session, so the WD's [PROBE] can point at the files.
  - If the catalogue's home copy is there by then (§2.3), a query by local name, for the reach and for any name Word writes
    that the schema lacks. Phase 1 doesn't wait for it.
  - The change is to the shared load path, and CLAUDE.md suggests Fable for that.
  - DONE 2026-11-06. `mc-preprocessor.xslt`, `XSLTUtils` (the count for D4's WARN, and the understood set
    for rule 2), `UnknownElementsTest`, and `unknown-elements/`: 98 sources and Word's 79 saves, rounds 1 to 11. The test compares 76 files with
    Word's saves, leaving out `76t44` and `76t45` (D11) and `76t71`, which has a test of its own. 62 mutants, each of the twenty hosts,
    each guard by parent and each rule's parts, are caught, but for one that changes only how soon the `w:p` template looks further.
    core-tests 2480 (30 skipped), export-fo 111, export-fo-tests 908 (9 skipped). Gates b370 against b369, and b372 against b371 for the
    `w:p` template: 0 movers, events equal. The catalogue's home copy isn't here yet, so its query is still to do.
- **Phase 2, the hand-offs** (D6) through the portfolio session.

## 7. Risks

- **The catch-all template.** It now carries every `w:` element nothing else takes, so an error there touches each
  preprocessed part. The suites, the gate and the stylesheet's own Strict tests are the check. A Strict document's elements
  are in the `purl` namespace and don't match `w:`, so rule 1 doesn't reach them, as CR-073's rules don't.
- **What the host doesn't admit.** Beyond `76t42` and `76t43`, what comes out where its host doesn't admit it is skipped by the second
  unmarshal, which is today's loss and not a new one. Word's reading of those isn't measured.
- **A Choice's own prefix.** With the stylesheet running, stock and trial alike, `76t44`'s text is the Fallback's, where Word and an
  unprocessed load take the Choice. Its `Requires` prefix is declared on the `mc:Choice`, as Word writes it for a prefix the root
  lacks, and the stylesheet's copy may lose that. Not traced, and not this CR's, but rule 1 sends more parts that way only if they hold an unknown element, as today.
- **The other modes.** The nested-paragraph templates walk in modes of their own (`segment`, `unwrap-paragraphs`). A
  `w:madeUp` inside a paragraph they split isn't tested, and phase 1 should add a case.
- **CR-073's `w:pPr` template.** A paragraph straight after a `w:br` in `w:body` has its `w:pPr` taken by that template and not the
  catch-all, so rule 1b doesn't reach it. No file has both.
- **Text goldens.** A document with such an element gains the words. No fixture here has one (to confirm in phase 1).

## 8. Out of scope

- The hosts §1 lists as not measured, which go on dropping.
- A foreign element that isn't ignorable, or one in no namespace, outside a text element, and a run in a `w:rPr`, a block `w:sdtPr`, a
  `w:trPr`, a `w:tcPr` or a `w:tblPr`: Word refuses those files, and docx4j goes on reading what it can (core-ts's D3 is the same).
- A report to the caller. Text and inspection output gain nothing here, since the words are simply read. core-ts's CR-025 D4
  on warning of a dropped element is a question for the WD, and docx4j follows what it rules.
