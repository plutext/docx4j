# CR-075: packages Word repairs on opening: duplicate entries, two main parts, an unreadable relationships part or store

Status: ACCEPTED 2026-11-07 (Jason: "start CR-075 decisions as recommended"): D1 to D10 as recommended in §5. Phases 0
to 5 committed, D10's count to come: 17.3.6 ships without it (Jason, 2026-11-08: "ship today without the count"). Proposed 2026-11-04 (Jason: "Draft the CR for docx4j's load-side items"). The work Jason's repair policy
decisions of the same day leave to docx4j (the portfolio's `docs/repair_policy_across_ports.md`, "decisions as
recommended", P1 to P6, 6085a74). Phases in §6. Jason ruled the two-main-parts
statement on 2026-11-07 ("yes"), as §3 has it, so phase 3 waits on nothing. WD 0.73's item (i) ruled 2026-11-04 (WD 0.76, 7c5ee35):
a relationships part that cannot be read MAY refuse the load, and one that does not refuse reads the relationships
parsed before the error (§3), so phase 4 waits on nothing but D5.

## 0. Summary

Seven package shapes that Word 365 opens only after "Word found unreadable content", or reads by a rule of its
own, are read by docx4j in a way that differs from Word, silently, or refused with the wrong cause:

| # | Shape | Word 365 | docx4j 17.3.6-SNAPSHOT today |
|---|---|---|---|
| 1 | two zip entries named `word/document.xml` | repairs; reads the **first** | reads the **last**, no warning; the save writes the last |
| 2 | two zip entries named `_rels/.rels`, each naming a different main part | repairs; reads the **first** | reads the **last**, no warning; the save drops the other main part |
| 3 | a second entry whose name is OPC-equivalent (`Word/Document.xml` after `word/document.xml`) | repairs: the names are one to Word; reads the probe (the first entry, which is also the exact name); keeps one main part | reads the exact name, no warning; the save drops the other entry |
| 4 | one `_rels/.rels` with two `officeDocument` relationships | repairs; a fresh Word reads the **last relationship's** target, whatever the Overrides', zip entries' or names' order, and a second repaired open in the session the first's (§2); keeps one | reads the **last relationship's** target, no warning; the save keeps both |
| 5 | a part's relationships part not well formed (`word/_rels/document.xml.rels` cut after its third relationship) | repairs ("Comments"); keeps the relationships before the break; the comments' references left as empty comments | reads **no** relationships, no warning (a stack trace on stderr); the save drops styles, numbering, settings, comments and commentsExtended |
| 6 | the package's `_rels/.rels` not well formed (cut after the main part's relationship) | repairs; keeps the main part; the three properties parts lost | refuses the load, the cause a `NullPointerException`, not the part |
| 7 | a custom XML store not well formed | repairs; reads the store as absent and binds its control to the store whose root fits (REQ-509's repair); drops it on save | refuses the whole load ("Failed to getPart") |

The rulings and recommendations in §3 give each a reading: refuse by default where the WD says so (1 to 4), read as
Word's repair gives it where the operation is read-only and the save would lose nothing (5, 7), and refuse a save that
would make a repair permanent with loss unless the caller asks for the repaired form, Word's prompt answered in
advance (the switch).

## 1. The chain in docx4j (measured 2026-11-04, 17.3.6-SNAPSHOT)

- **Duplicate entries (1 to 3).** `ZipPartStore` reads every entry into `partByteArrays`, a `HashMap` keyed by the
  entry's exact name (`ZipPartStore.java:115` and `:152` for a file, `:266` and `:290` for a stream), so a later entry
  of the same name replaces an earlier one, and names differing in case are two keys. Measured on the identifier's
  `extraction-probe-dupmain` (text DECOY_ORPHAN_MAIN, the second entry) and `extraction-probe-dupcase` (the probe's
  text, the exact name), and on the four `two-rels-entries-*` files built for Jason's question of 4 November ("did we
  test the 2 package level rels case?").
- **Two main relationships (4).** `Load3` walks the package's relationships in order and calls
  `setPartShortcut` for each target (`Load3.java:390`, `:405`), so the last `officeDocument` target becomes
  `getMainDocumentPart()`. Both parts are loaded and saved.
- **A part's relationships (5).** `Load3.getRelationshipsPartFromZip` (`Load3.java:255`) attaches an empty
  `RelationshipsPart` (`getRelationshipsPart(true)`) before `RelationshipsPart.unmarshal(InputStream)`
  (`RelationshipsPart.java:995`), which catches every exception and prints it (`:1010`). The part loads with none of
  its relationships; nothing it named is loaded, so an untouched save drops them. Measured on
  `text-extraction/broken-part/broken-part-document-rels.docx` (docx4j-python CR-031 phase B's hand-off).
- **The package's relationships (6).** `Load3.populatePackageRels` (`Load3.java:236`) throws "Error getting document
  from Zipped Part: _rels/.rels", but its cause is the `NullPointerException` of `resetIdAllocator`
  (`RelationshipsPart.java:427`) iterating the null element the swallowed parse left. Measured on
  `broken-part-package-rels.docx`.
- **A store (7).** A custom XML part falls back from JAXB to a DOM parse (`Load3.java:605`), which fails on
  a store that is not well formed, and `getRawPart` throws "Failed to getPart" (`:654`) for the whole load; WD 0.69
  notes the error's part name prints as `/null`. Measured on `broken-part/store-not-read.docx`.

CR-065 D1 (26 October, and its §9, 2 November) is the principle the load already follows for a story or XML part whose
content cannot be read: `load()` returns, the part keeps its source bytes, an edit that would replace it and an output
that needs it refuse naming it, and an untouched save writes the bytes back. Items 5 to 7 are where the load departs
from it; items 1 to 4 are where a package has two readings and docx4j picks one without saying so.

## 2. What Word does (the oracle)

All Jason's runs on the VM (Word for Microsoft 365, build 20326.20232 when last recorded):

- **1:** the identifier's `extraction-probe-dupmain` (3 November): unreadable content, repaired, the first entry read.
- **2, 4:** `~/vbShares/Office 2016/two-rels/` (4 November). Two `_rels/.rels` entries: the first read in both
  orders. Two `officeDocument` relationships: `other.xml` read in both relationship orders; round 2 (one factor at a
  time): zip entries reversed, still `other.xml`; content-type overrides reversed, `document.xml`; names `zeta.xml`
  and `alpha.xml` with alpha last in every order but first by name, `alpha.xml`. Every file reported unreadable
  content; each `.docx` save keeps one main part, renamed `word/document.xml`, and drops the other. With **one**
  relationship and an unreferenced part whose main-document Override is last (the identifier's
  `extraction-probe-decoyct`, 2 November), Word reads the related part with no repair: the Override rule belongs to the
  repair path alone.
- **3:** the identifier's `extraction-probe-dupcase` (4 November): unreadable content, repaired, the probe shown; the
  repaired save holds one main part, `word/document.xml`, whose text equals Word's re-save of the probe, and no trace
  of the decoy. So Word holds names differing only in case to be one, as OPC does, and as six other readers do in
  refusing it (POI, Tika, LibreOffice, LiteParse, office-oxide, stetdoc). The probe was both the first entry and the
  exact name; core-ts's probe separated them (Word check 75a, Jason's run, 4 November; WD 0.88): `word/headerA.xml`
  then `word/headera.xml`, the relationship naming `headera.xml`. Word reports unreadable content, repairs, shows
  headerA's header and saves it as `word/header1.xml`. So Word keeps the first in the central directory, not the
  name the relationship spells: REQ-206 detail (b)'s "first" is Word's own rule, and the identifier's `dupcase` probe
  (decoy second) is read by it ("first entry").
- **Also, not one of the seven:** the same round (75a) found that Word holds any zip entry name in raw UTF-8 unreadable
  (a single one is enough, whichever form the relationship's target takes), and its repair drops the part. That is a
  repair-policy (tier 2) case, not REQ-206's; docx4j's reading of such an entry is not measured here. The case fold
  of non-ASCII names is unmeasured (core-ts's round 2, percent-encoded names, staged).
- **5, 6:** `broken-part-recheck/` (4 November): as in the table; Word's saves are committed with the broken-part
  family (e2af12b74).
- **7:** `store-not-read/` and the broken-part run (3 November): WD 0.69.

**Through the bridge, 7 November (the set `package-wiring`, 17 files, each twice, the two runs agreeing, Word 16.0.20326.20232).**
Shapes 1 to 3 stand, and every output agrees. `dup-central-reversed` reads the central directory's first, not the local headers', which
settles §7's "two orders of first". Identical copies are repaired too (`dup-entry-same-bytes`), and a part's relationships and
`[Content_Types].xml` read their first copy. **Shape 4 was misread on 4 November.** A bridge job opens the file once per output, in one Word process, repairing each time. The set
`open-order` (Jason: "the do small round") ran each format alone and all six orderings on the two files where the last relationship and the last Override differ, each job
twice: 84 outputs. A fresh Word's first open reads the last relationship's target, 36 of 36; the second open in the session the first's, 24 of 24; the third the last's,
24 of 24, whichever format each is, the two runs equal. So the choice alternates with each repaired open in a session, and the Override plays no part. The hand runs of
4 November fit that alternation, in `RUN.txt`'s order, and "the Override last" was a coincidence of the order the files were opened in. A session's
count may run over other repaired files, or only these: the hand runs can't tell, and the bridge's jobs are one file each. A cell left with no block is repaired, and a bare `w:tr` refused, in a source and in a save alike (python's round, `te-*`).

## 3. What has been ruled, and what is still open

- **REQ-206 (WD 0.74; Jason, 4 November, in the core-ts session: "refuse the package, with an optional config option
  to read the first copy and warn"):** a package whose zip holds two entries of one name is refused naming the entry;
  an implementation MAY offer `duplicate_entries: first`, reading the first entry of each name and raising
  `package.duplicate_entry` once per name (selector the entry name, detail the count). Its detail (a), as the WD now
  has it: the check folds case as OPC part-name equivalence does.
- **The repair policy, P1 to P6 (Jason, 4 November: "decisions as recommended"):** P1, every Word check records
  whether a prompt appeared. P2 (R1): a read-only operation reads what Word's repair would give, marked; a write that
  would lose content refuses by default naming the part unless the switch is on (an edit replacing unreadable content,
  an output needing the whole content, a save that would drop what the reading read around); an untouched save of the
  source bytes is lossless and allowed; REQ-206's refusal applies in every operation. P3: under the switch, and in a
  read-only operation, a part reads as Word's repair gives it where measured, else as absent. P4: the switch's name
  becomes a WD option when the first engine builds it (working name `repair: word`). P5: tier 3 read for a caller's
  repair, the lossless round trip kept, a repaired form only under the switch.
- **WD 0.69 (c), a store not well formed:** read as absent, its binding resolved under REQ-509's repair (else the
  surface), `part.not_read` once naming the store where a binding in scope names it; docx4j's load-side items named
  there: keep the store's bytes, pass over it, raise the warning, fix the `/null` part name.
- **Ruled 2026-11-07 (Jason: "yes"; WD 0.100, portfolio 9034ad0), the two-main-parts statement:** a package with more than one `officeDocument`
  relationship is refused, naming the targets. Under the switch, the last relationship's target is read, as a fresh Word reads
  it (§2), and `package.multiple_main_parts` is raised once (selector that part's name, `detail` the number of relationships). This
  replaces the Override-last reading REQ-206's NOTE recommended.
- **Ruled 2026-11-04 (WD 0.76, 7c5ee35), WD 0.73's item (i) for a relationships part:** a relationships part that
  cannot be read MAY refuse the load; an implementation that does not refuse reads the relationships parsed before the
  error, raises `part.not_read` naming the relationships part with the count kept, and reads the parts only a lost
  relationship named as absent. Both readings conform; the prefix is Word's.

## 4. Approach

1. **The switch.** One load option, docx4j's form of P4's `repair: word`: off by default; on, the load reads every
   shape above as Word's repair gives it and a save writes the repaired form. Given as `McLoadMode` is (CR-051): a
   property for the default and a per-load argument (D1).
2. **Load findings.** A load that resolves a duplicate, chooses a main part, reads a relationships part partly or
   passes over a store records a finding on the package (kind, selector, detail), logged once; the text model raises
   it as its warning under the WD's code, the inspector reports it, and the save gate (4) reads it (D2).
3. **Each shape:**
   - 1 to 3 (REQ-206): the part store detects a duplicate under OPC equivalence while it reads the zip and refuses,
     naming the entry and the count; under the switch it keeps the first and records `package.duplicate_entry` (D3).
   - 4: after the package's relationships are read, more than one `officeDocument` relationship refuses, naming the
     targets; under the switch the last relationship's target is the main part and
     `package.multiple_main_parts` is recorded (D4).
   - 5 and 6: `RelationshipsPart` records a failed parse instead of printing it, as `JaxbXmlPart` does since CR-065 D1.
     Then the reading D5 and D6 choose: the relationships before the break (a StAX pass that keeps each complete
     `Relationship`), or a refusal naming the part. Either way the package's refusal names `_rels/.rels` and the parse
     error, and the stack trace on stderr goes.
   - 7: the store becomes an unreadable part under CR-065 D1 (bytes kept, passed over), which the text model already
     reads as absent with REQ-509's repair and `part.not_read` where a binding names it; the load error names the part
     (D7).
4. **The save gate (P2).** A save refuses, naming the finding, where the load recorded a repair the save would make
   permanent with loss (5's lost parts); under the switch it writes the repaired form, as Word's save does (D8).
   Everything else stays as CR-065 D1 has it: an unreadable part reached by a relationship is written back byte for
   byte; an entry no relationship reaches is dropped, as Word's own save drops it (the policy's §5).

## 5. Decisions

| # | Question | Options | Recommendation |
|---|---|---|---|
| D1 | The switch's form. | (a) a `RepairMode` (`NONE`, `WORD`) as `McLoadMode`: a property (`docx4j.openpackaging.load.repair`, `none` or `word`) and load overloads; (b) a `LoadOptions` carrying the `McLoadMode` and the `RepairMode`, one overload per source (`File`, `InputStream`), the `McLoadMode` overloads kept and delegating; (c) the property only. | (b): `OpcPackage.load` already has fourteen overloads, and a second mode would double CR-051's; the property gives the default, as `McLoadMode`'s does. The WD's option name follows P4. |
| D2 | Where a load's findings live. | (a) WARN logs only; (b) a list on `OpcPackage` (kind, selector, detail; `getLoadFindings()`), each logged once, read by the text model (its warnings), the inspector and the save gate; (c) exceptions only. | (b). The text model and the save both need them after the load returns. |
| D3 | REQ-206's duplicate entries (its details ruled 2026-11-04, WD 0.77 b0d333d: OPC part-name equivalence, case-insensitive; "first" in the zip's central directory, Word's own rule (Word check 75a, WD 0.88); every entry covered, `[Content_Types].xml` and the relationships parts included; the save under the option writing only the copy read; `package.duplicate_entry`). | (a) as ruled: refuse naming the entry and the count; under the switch keep the central directory's first and record `package.duplicate_entry` per name. A stream is read for its central directory before choosing (`ZipPartStore`'s stream path reads local headers in order today: buffered, then read as a zip file), so a file and a stream choose alike; (b) refuse only duplicates of names a relationship reaches. | (a), settled by the ruling. Whether `duplicate_entries: first` stays an option of its own is P4's naming question; docx4j's switch covers it, with an alias if the WD keeps the name. |
| D4 | Two `officeDocument` relationships (ruled 2026-11-07, §3). | (a) refuse naming the targets; under the switch the last relationship's target, `package.multiple_main_parts` once; (b) today's last relationship's target with a finding, and no refusal. | (a), as Jason ruled. It first said the target whose main-document Override is last, which §2's bridge rounds disproved. |
| D5 | A part's relationships part not well formed (WD 0.76: either (a) or (b) conforms). | (a) refuse the load naming the part (python CR-031 and core-ts); (b) keep the relationships before the break, record `part.not_read` naming the relationships part with the count kept, the parts only the lost relationships named read as absent with their own warnings where consulted, the save refused unless the switch (D8); (c) today's: none, silently, which does not conform. | (b), Word's reading: a read-only operation gets nearly all of the document, as P2 has it, and the save gate keeps the loss from being written silently. |
| D6 | The package's `_rels/.rels` not well formed. | the same choice as D5 for the package (Word kept the main part's relationship); in either case, from phase 1, the refusal or the finding names `_rels/.rels` and the parse error, not the `NullPointerException`. | As D5; the named cause in phase 1, whatever D5 becomes. |
| D7 | A custom XML store not well formed (WD 0.69 (c), P2, P3). | (a) an unreadable part under CR-065 D1: bytes kept, passed over, read as absent with REQ-509's repair, `part.not_read` where a binding in scope names it, the load error's name fixed; an untouched save writes the bytes; under the switch the save drops it, as Word's does; (b) refuse the load, as today. | (a). |
| D8 | The save gate (P2). | (a) a save refuses, naming the finding, where the load recorded a repair the save would make permanent with loss; under the switch it writes the repaired form; (b) the save warns and writes. | (a), as P2. |
| D9 | Fixtures. | (a) a package-wiring family in docx4j-core-tests (`src/test/resources/package-wiring/`, its Python builder, byte-identical rebuild): the two-rels files of rounds 1 and 2 with Word's saves, docx4j's own duplicate `word/document.xml` and case-variant files (the case variant both after and before the exact name; Word check 75a measured the decoy-first order, Word keeping the first entry, and the fixtures pin it for docx4j), each Word check recording the prompt (P1); the relationships-cut and store files stay in the broken-part family; (b) tests building packages in Java only. | (a). The identifier copies them in (its CR-001 §3c lists two-rels as proposed). |
| D10 | The corpus gate. | (a) before phase 1 lands, the load survey counts duplicate and OPC-equivalent entry names and multiple `officeDocument` relationships in the corpus, as CR-064's D9 gated its refusals; (b) no gate. | (a): REQ-206 turns a silent reading into a refusal, so how many documents it refuses should be known. 17.3.6 ships before it is (Jason, 2026-11-08: "ship today without the count"): none of the 1,311 local corpus documents is refused, each refusal names its cause and `RepairMode.WORD` reads past it, and the count follows from the portfolio's catalogue queries. |

## 6. Phases

| # | Content | Status |
|---|---|---|
| 0 | D9's fixtures (the builder; the two-rels files with Word's saves; docx4j's own duplicate and case-variant files, staged for a Word check recording the prompt); D10's survey count. | Fixtures done: `package-wiring/build_package_wiring.py` rebuilds the nine files of rounds 1 to 3 byte for byte and adds ten (a control; two entries of one name, as different bytes and as the same; `[Content_Types].xml` and a part's relationships twice; a case variant both ways; a central directory the other way from the local headers; a duplicate only the local headers show; a directory entry twice). Word's saves of the first 17 are beside them (§2). D10's count is in the catalogue queries owed once the home copy is in. |
| 1 | D1 and D2 (the switch, the load findings); D3 (REQ-206 in both `ZipPartStore` paths, OPC equivalence); D6's named cause; the part-level relationships failure WARNed and recorded instead of printed (its reading unchanged until phase 4); tests; CHANGELOG. | done, reviewed by Fable (Jason: "you may implement phase 1; we will have fable review it", then "commit phases 0 and 1"). D10's count isn't in: release waits on it, as REQ-206 refuses packages that load today |
| 2 | D7, the store. | done (Jason: "implement CR-075 phase 2 (the store) and phase 4 (the relationships reading and save gate) then commence CR-079", "commit phases 2 and 4 with these fixes"). The store is kept as a `BinaryPart`, so no lookup finds it |
| 3 | D4, two main parts. | done (Jason: "then start phase 3", "Phase 3: commit it"). `PackageRelsUtil.isMainPart`, `Load3.oneMainPart`; the rule runs on every package type, and was measured on Word alone |
| 4 | D5, D6's reading and D8's save gate. | done. A `clone()` for an output and `Docx4J.bind`'s copy back aren't gated, as they're copies, not saves. Gate b382: 0 movers |
| 5 | `repair: all` (WD 0.102 REQ-207 and REQ-208, the External statement; the repair policy's section 4.3, amending P5): docx4j's own repairs, the first of them an `External` `officeDocument` relationship beside an internal one. | built (Jason, through the portfolio: "Building all: instruct docx4j to build it now with External, replacing my earlier direction to it"; then here: "Build repair: all? yes"). `RepairMode.ALL`, `docx4j.openpackaging.load.repair=all`: every repair of `WORD`'s, through a new `followsWord()` at the five places that asked for `WORD`, and docx4j's own, through `makesOwnRepairs()`. `Load3.oneMainPart` reads the internal target and drops every `External` `officeDocument` relationship, so a save writes none, and never fetches the URL; `package.external_main_part` once, selector the part read (after Word's last-relationship repair where there are two), detail the first URL as written. The refusal at the default and at `WORD` stands, and now names `RepairMode.ALL`. An `External` relationship with no internal one is refused in every mode as a package with no main part (REQ-321), where the URL was taken for a part name. The text model's result carries `repaired`, `none`, `word` or `own` (`LoadFinding.repaired`), and each own repair is a load finding the text model warns of and the inspector reports. `RepairMode`'s javadoc lists docx4j's own repairs, as REQ-207 asks. A shape the WD names as refused stays refused at `ALL`, as docx4j has no repair for it. `PackageWiringTest`: four tests; 22 mutants, each caught. Word's check on docx4j's saves (Jason: "run docx4j's repair-all-saves"; the round `repair-all-saves`, docx4j's `RepairMode.ALL` save of python's `x-ext-first-source` and `x-ext-last-source`, each twice, Word 16.0.20326.20232, bridge 4dcb436, 8 November): all silent, no dialog, the text `DOCUMENT` in both runs, so a file Word refuses opens once docx4j has repaired it. |

Each phase's hand-off (the rule, the fixtures, Word's readings) goes to docx4j-python (its planned CR-032 for REQ-206)
and core-ts through the portfolio.

**Fable's review of phases 2 to 4** (Jason: "Fable review of CR-075 phases 2 to 4 first"), ten findings.

- **Fixed.**
  - `Docx4J.bind` copies a package back into itself through a save, so the gate refused it, and the reload left a `RepairMode.WORD` package on the default mode, so the caller's own save was refused too. It now copies with `Save.setCopy`, and keeps the mode as it keeps the findings.
  - A store the reader refuses before parsing, an unknown declared encoding say, still refused the package. It now reads as absent like any other. The parse no longer goes through `XmlPart.setDocument`, which is where the `/null` and the stderr line came from: a store now gives one warning.
  - A store reached by two relationships lost only the first under the switch, and a save then failed on the part. All of them go.
  - The prefix kept on any failure, not only a break, and in any namespace. A relationships part that parses to its end now keeps nothing, so a well-formed `_rels/.rels` in the wrong namespace is refused as it was; a second reading no longer doubles the list.
  - The rich-text binding's Flat OPC save of a package made for one control carried the source's findings, was refused, and left the value stale, with the exception printed and swallowed. Its findings are cleared. This one has no test of its own.
  - `SaveToZipFile.saveFlatOPC` wasn't gated, where `OpcPackage.save` with the Flat OPC flag is. It is now.
  - The inspector didn't report an unreadable store, so it and the text model disagreed. It now lists it as `unreadable`.
  - An external `officeDocument` target is no main part, where under the switch it had died on a URL taken for a part name. docx4j-python's check at 9077b95 found the same.
  - The gate's message and javadoc say it holds even where all the relationships read were all there were, since nothing tells how many followed, and that a caller who has put back what was lost says so with `setRepairMode`.
- **Not a defect.** It asked whether two `officeDocument` relationships to one target should be refused, as they loaded before phase 3. I changed that to one main part, unmeasured, and Word then showed it wrong: the round `one-target` (Jason: "Two officeDocument relationships to one target: measure it"; `ot-*`, each file twice) has Word repairing two and three to one target, a dialog at every open, and saving one relationship. So phase 3's count of relationships stands, as "Word reads such a package only through a repair" says, and the message now says relationships rather than parts.
- **A difference from Word it found.** `broken-part-package-rels`'s property fields were pending on `WD 0.72 (4)`, which is about an unreadable part. They are a different thing: with a property part absent, even on an intact package, Word empties the field under `fields: update` and docx4j leaves its cached result. Labelled `defect (e)`, not fixed here.
- **Left.**
  - `AbstractMerger` clones through a save, so a mail merge of such an input is refused. That stays: the result is a new package with no findings, so this is the one place the loss can be told, and under the switch it goes through (Jason: "Mail merge: keep it refused on purpose").
  - The documents4j and Microsoft Graph exporters save to hand a docx to another engine, so they are refused where docx4j-export-fo isn't (Jason: "documents4j and Graph exporters: leave them refused").
  - `FlatOpcXmlImporter` takes none of these rules, as it reads no zip and no relationships parts of its own.
  - `EmbeddedPackagePart.getBuffer` falls back to the old bytes if a live nested package is refused, logging an error.
- **Measured after the commit, and docx4j differs: an `External` relationship** (python's `x-ext-*`, 8 jobs, each `opened: refused` in its `result.json`, measured only as python built them). Word refuses a package with an `External` `officeDocument` relationship beside an internal one, first or last: a repair offered, then the error. docx4j passes over it and reads the internal part, from the review's fix above, unmeasured at the time. It reads a file Word refuses, which loses nothing. Jason: "External beside an internal relationship: refuse in both modes". `Load3.oneMainPart` now refuses it, under the switch too, naming the `External` target and the internal one, first or last: the switch reads what Word's repair gives, and here the repair gives nothing. Refusing a package that loads today goes in D10's count. Unmeasured: two internal relationships beside it, refused the same way; a pptx or xlsx, refused on Word's reading alone. An `External` relationship with no internal one is left as it was, failing in both modes on the URL taken for a part name, also unmeasured. Gate b387 against b386: 0 movers, events equal, probes the same, so nothing in the local corpora has the shape.
- **Measured after the commit, and docx4j differs** (round `rels-namespace`; Jason: "A part's .rels in a wrong namespace: stage a small round"; docx4j-python's check at 9077b95; `rn-*`, each file twice, the runs equal; `package-wiring/read_rels_namespace.py`). A relationships part well formed in a wrong namespace, on its root, its children, both, or with none, or with only one child wrong, is repaired, a dialog at each open, and Word then reads every `Relationship` by its local name: styles, settings, the header and the note all arrive, in the save, the text and the PDF, each as its control. `_rels/.rels` the same. It isn't Word's defaults: with the part gone (`rn-08`) the header, the style and the note are lost. docx4j as committed keeps none of a part's relationships there, with `part.not_read`, a default save refused and under the switch what they named lost, and refuses `_rels/.rels`. The review's point that a prefix was kept in any namespace, and my fix to it, were unmeasured, and Word goes the other way. Jason: "Wrong-namespace relationships: build the fix as recommended". Such a part is refused, named, as Word reads it only through a repair; under the switch every `Relationship` is read by its local name, recorded as `package.relationships_namespace`, selector the part, detail how many, and a save writes the right namespace with nothing lost, so the gate has nothing to refuse. `part.not_read` stays for a real loss. Refusing a part that loads today goes in D10's count. A break in such a part keeps its prefix by local name too, unmeasured; a root with another local name keeps nothing, unmeasured.

## 7. Risks

- **The shared load path.** Every load passes through the part store and `Load3`; CLAUDE.md's model guidance gives
  changes to the io3 load path to Fable. The duplicate check costs a case-folded set of the entry names per load:
  measured 2026-11-07, warm, on five corpus files of 16 to 201 entries, 0.3 to 3.6 µs, at most 0.14% of a load of 1 to 25 ms.
  D3's stream route, buffered and then read through the central directory, took -9% to +7% of today's store time, under
  0.1 ms, and holds a copy of the zip's compressed bytes while it reads.
- **Documents refused that load today.** REQ-206 refuses packages docx4j has read (with the last entry) since its
  first release; D10's count says how many, before phase 1 lands. The switch is the caller's way back. The local
  corpora, 561 files, hold none: no duplicate or case-variant name, and no second `officeDocument` relationship.
- **Two orders of "first".** A zip's central directory and its local headers can list entries in different orders;
  Word was measured only where they agree. REQ-206 as ruled takes the central directory's, which the stream path must
  then read before it chooses (D3). Phase 1 buffers the stream, within the package limit, and reads its central directory;
  a package with no duplicate is then read through its local headers, as before, so a zip with no usable central
  directory still loads, and the ratio check stays the file route's. A duplicate the central directory doesn't list is
  passed over, as a file passes over it. A directory entry doesn't count. `pw-dup-central-reversed` asks Word.
- **The relationships prefix (phase 4)** changes `RelationshipsPart`'s unmarshal, which every part with relationships
  uses. The StAX pass runs only after a parse has failed, from the bytes the part store still holds, so a part that
  parses pays nothing.

## 8. Out of scope

- The text model's side, with Jason since 3 November: CR-046 §10's fixes (b) (a styles part added to a package
  without one, which makes a second reading differ from the first) and (c) (the read error logged per call), and defect
  (d) in the WD's 0.67 open item (WD 0.68; an unreadable custom properties part leaving every property field on its
  cached result).
- An entry no relationship reaches is dropped by every save, as Word's save drops it (the policy's §5); not changed.
- The inspector's own package checks (CR-050) beyond reporting the load findings.
