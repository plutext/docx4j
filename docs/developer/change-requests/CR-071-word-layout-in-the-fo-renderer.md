# CR-071: Word layout in the docx4j FO renderer; docx4j drops its copy of FOP's layout managers

Status: ACCEPTED 2026-11-02 (Jason: "CR-071 D1 to D11 as recommended, except for D9 where we decide on (a)"): the move
lands in 2.12-docx4j.1 and docx4j 17.3.6, whose releases wait for it. Phase 0 DONE 2026-11-02 (gate b302); phase 1 is
the fork session's. Proposed the same day (Jason: "what if we just had the single copy in the fork, and accepting that
-Papache-fop, docx4j on stock Apache FOP, would not have Word's line breaking?", then "write the CR please").
Drafted by Claude Opus 5.5 (session docx4j-12). Decisions D1 to D11 (section 6): as recommended, D9 (a). In CR-020's family
(the fork, `../xmlgraphics-fop-plutext`); the fork's side is the fork session's, under a fork CR of its own.
Reviewed the same day by the fork session (xmlgraphics-fop-plutext-d1, reading docx4j's copy against fop-2_11, fop-2_12
and the fork's 2.12-docx4j.1 at 36ee05557): the shape works and the merge cost is low; its four points are folded in
(section 5's phase 2 criterion and new phase 3, D2's placement, D3's form, D7's porting, D11), and it agrees with D9(b)
and D10(a). Its size for phase 1, the shipped copy to a gated drop: 1.5 to 2 weeks. Reviewed again the same day by
docx4j-11 (Fable 5.1) at Jason's request: approved in substance; its corrections are folded in (section 1's note on
FOP-2860, D3's premise, D6's entry point, phase 0's reason, the counts, D7's list, three points in section 7), and its
assessment of D9 is in D9's row. Jason inclines to D9(a), not decided.

## 0. Summary

docx4j's Word layout (`org.docx4j.fop.wordlayout`, 8,206 lines in docx4j-export-fo) is a set of FOP layout managers
docx4j substitutes for FOP's own, chiefly a 4,701-line copy of FOP 2.11's `LineLayoutManager` with Word's greedy line
breaking. The copy predates the fork, and it lives in docx4j so that docx4j on stock Apache FOP (`-Papache-fop`) keeps
Word's layout. The cost is a hand re-derivation at every FOP upgrade, which missed two of FOP 2.12's hunks (CR-020
§8), and reflection into FOP's private members.

This CR moves the Word layout into the fork's own layout managers, behind a switch docx4j turns on, so that there is
one copy of each manager and Apache's changes to them arrive through the fork's ordinary upstream merges. docx4j keeps
the FO side: what it writes, and the settings it passes. On stock Apache FOP docx4j lays out as plain FOP does, as
`docx4j.convert.out.fo.wordLayout=false` does today.

## 1. Background

Word breaks lines greedily: each line takes every word that fits and breaks at the last opportunity before the first
that does not. FOP breaks by Knuth-Plass total fit, optimising over the paragraph; with identical fonts and widths
the two break about a quarter of ragged-right lines differently, and every later line and page moves (package
documentation; Enterprise CR-001 phase 5, docx4j 17.1.0). FOP has no first-fit option and its breaking algorithm is
a private inner class. (FOP-2860's simple line breaking, `FOUserAgent.isSimpleLineBreaking`, is not one: in
`LineLayoutManager` it only stops the retries from raising the adjustment threshold, and breaking stays total fit.)
So docx4j copied the whole `LineLayoutManager`, made its loop greedy, and gives FOP its own
`LayoutManagerMaker` (`FopFactoryBuilder.setLayoutManagerMakerOverride`, a public API). Around it grew Word's line
placement (the leading below a page's last line, the line box), its list items, its page-number citations, and the
rules CR-001 §6.6 records as living by reflection into FOP's private members.

The fork (CR-020) came after, and has been docx4j's default renderer since 17.2.1; `-Papache-fop` is the opt-out. The
newest Word line rules already need it (CR-032 phase 5: text wrapped round page-anchored objects uses the fork's
obstacles, fop/CR-024), so on stock FOP the Word layout is already partial.

What the copy costs:

- **Re-derivation.** README-word-layout.md's recipe (copy the new `LineLayoutManager`, re-apply the greedy members,
  route constructors through `LBP`) is applied by hand. At the move to 2.12 (CR-020 §8, 27bf221d4) two of the six
  hunks 2.12 changed in that class were not carried: FOP-3331 (`db3602d17`, a restart at a paragraph's first element
  dropped its first word where that element was a box; docx4j uses floats by default, `FOConversionContext.useFloats`,
  and its line manager restarts) and FOP-3333 (`f84f61fe5`, `lineArea.completeTraits`, memory). The fork's own
  `LineLayoutManager` has both since 2.11-docx4j.7.
- **Reflection.** `LBP` (998 lines) reaches `LineBreakPosition`'s package-private constructor and private fields;
  `FopHooks` looks up the fork's accessors by name, falling back to fields read by reflection on stock FOP.
- **Two places for one rule.** A Word layout change is sometimes docx4j's (the managers) and sometimes the fork's
  (the obstacles, columns, floats), and the split follows history, not the rule.

## 2. What the package is

| Class | Lines | What it is |
|---|---|---|
| `WordLineLayoutManager` | 4,701 | FOP 2.11's `LineLayoutManager` with greedy breaking, tabs and leaders, hyphenation zone, letter spacing, GDI rounding of line metrics |
| `LBP` | 998 | reflective access to `LineBreakPosition`, `AlignmentContext`; the rule-style setter |
| `WordLayoutCustomizer` | 615 | registers the maker (`FopFactoryCustomizer`, ServiceLoader); the switch `docx4j.convert.out.fo.wordLayout` and 35 tuning properties `docx4j.convert.out.fo.wordLayout.*` |
| `WordListItemLayoutManager` | 556 | list items: the last line's leading left discardable |
| `WordBreakOpportunities` | 383 | where a line may break: FOP's UAX #14 (`LineBreakStatus`) with Word's adjustments |
| `WordFlowLayoutManager` | 317 | moves a paragraph's trailing leading behind the following break |
| `WordLayoutElementMapping` | 141 | the `docx4j:` namespace on `fo:block` and `fo:root` (line box, hyphenation zone, ...) |
| `WordLayoutManagerMaker` | 121 | the maker FOP is given |
| `FopHooks` | 110 | the fork's accessors as method handles |
| `WordBlockLayoutManager` | 105 | `BlockLayoutManager` creating the Word line manager (FOP's creation is private) |
| `WordPageNumberCitationLayoutManager` | 93 | an unresolved citation measured as a page number, not "MMM" |
| `LeadingGlue` | 39 | the extra leading as discardable glue |

Outside the package: `TableWriter` (docx4j's FO writer) calls `WordBreakOpportunities.breakBefore` to measure
content-sized columns; `WordLayoutFixups` writes the `docx4j:` attributes by `WordLayoutElementMapping`'s constants;
`WordLayoutCustomizer.extensionNamespace()` decides whether docx4j writes them at all (not when Word layout is off).
Tests: 19 in docx4j-export-fo (`org.docx4j.fop.wordlayout`: `GreedyLineBreakingTest`, `HyphenationTest`,
`WordLeadingTest`, `WordListItemTest`, `TabStopTest`, ...), 7 in docx4j-export-fo-tests that reach it
(`TabAlignmentTest`, `HyphenationFoTest`, `TabLeaderTextLayerTest`, ...).

## 3. Proposal

1. **The fork's own managers carry Word mode** (D2): Word's behaviour goes into the fork's `LineLayoutManager`,
   `BlockLayoutManager`, `ListItemLayoutManager`, `FlowLayoutManager` and `PageNumberCitationLayoutManager`
   themselves, behind a switch, rather than in parallel classes. In the fork a private member can become protected
   or a branch, so `WordBlockLayoutManager`'s re-creation of private logic, `LBP`'s reflection and `FopHooks`'
   lookups are not needed. Off (the fork's default, D5), the fork lays out as Apache FOP and FOP's own layout-engine
   tests pass.
2. **docx4j turns it on and passes its settings** (D3, D4): `WordLayoutCustomizer` keeps reading
   `docx4j.convert.out.fo.wordLayout` and the 35 tuning properties where it reads them today (docx4j.properties,
   system properties), and hands them to the fork through one fork API that `FopCapabilities` detects as a new hook
   (`word-layout`). The property names users set do not change.
3. **The FO contract stays docx4j's to write.** docx4j writes the `docx4j:` attributes when the renderer advertises
   `word-layout` (the fork registers the namespace), and leaves them out otherwise, as `wordLayout=false` does today.
4. **On stock Apache FOP (D1)** docx4j lays out as plain FOP: no greedy breaking and none of the other Word rules the
   package carries (line placement, list items, citations, tabs and leaders, hyphenation zone, GDI rounding).
   `FopCapabilities` warns once that Word layout needs the docx4j FO renderer. README and CHANGELOG say so.
5. **docx4j deletes its copy** once a fork release carrying Word mode is on Central and gated (section 5): the
   managers, `LBP`, `FopHooks`, `LeadingGlue`, the maker. What stays is in section 4.

## 4. What stays in docx4j

- `WordLayoutCustomizer`, reduced to the switch, the settings and the hand-over (D3, D4).
- The `docx4j:` attribute names, as docx4j's own constants for the FO it writes; the fork's element mapping holds
  them on its side, and a docx4j test on the fork checks that the two lists agree.
- Break opportunities for docx4j's FO writer (D6): `TableWriter` asks the fork's when the renderer advertises it, and
  on stock FOP uses FOP's plain UAX #14 (`LineBreakStatus`), which is what plain FOP's line breaking uses.
- `WordLayoutFixups` and everything that writes the FO.

## 5. Phases

- **Phase 0 (docx4j, before the move).** The two hunks the copy lacks (FOP-3331's restart index, FOP-3333's
  `completeTraits` through a capability check, since Apache FOP 2.11 has no such method), gated against b301, the
  2.12 line's baseline. Its purpose under any D9: phase 2's 0-mover criterion is only attributable if the copy
  already has what the fork's managers have, so that the move changes nothing but where the code is. Under D9(b) it
  also gives 17.3.6 the fix; under (a) the copy it fixes is never released.
  **DONE 2026-11-02:** both hunks in `WordLineLayoutManager` (`completeTraits` behind a check made once), with
  `FirstWordBesideFloatTest` (FOP's float_12.xml through the Word layout: without the fix the paragraph reads "float
  left ipsum dolor ...", its first word gone). Gate b302 against b301 (b301's jars, phase 0's export-fo): 1 mover,
  `14_en-GB_num_tbl_4083` +1 in real2 and real-c2, its FO identical, a heading beside its side float no longer drawn
  twice; events equal (Enterprise register cc68079). b302 is phase 2's baseline.
- **Phase 1 (the fork, its own CR).** Word mode in the fork's managers from docx4j's copy at phase 0, behind the
  switch; the settings API; the `word-layout` hook; the element mapping; the unit tests that test the managers
  (D7). A drop in `~/fop-renderers`.
- **Phase 2 (docx4j).** `WordLayoutCustomizer` uses the fork's Word mode when advertised. Gate: the drop with
  docx4j's copy switched off and the fork's Word mode on, against phase 0's baseline: 0 movers and equal events
  expected, which holds only with D11(a). After phase 0 the copy and the fork's line manager differ in three places
  (the fork session's reading): the inline-access accessors `LBP` stands in for (no behaviour); fop/CR-024's
  `handlingObstacle` and edge-only break (equivalent by reading); and fop/CR-023 §4.2's mark (63fdc0aee): the fork's
  `LineBreakingAlgorithm.updateData2` marks a line that overflows by no more than a side float's intrusion
  (`LineBreakPosition.setOverflowsBesideFloat`), and `PageBreakingAlgorithm.overflowsBesideFloatAtNextBox` sets it at
  the float's foot. It has no switch in the fork, and the copy never sets it, though its greedy path goes through its
  own `updateData2`; Word mode built on the fork's would start moving such lines. Then delete the copy, move or skip the tests (D7), README-word-layout.md,
  CHANGELOG, CR-001 §6.6's workaround entries (they become fork code). Released with the fork release.
- **Phase 3 (the fork, then a docx4j gate).** CR-023's overflow-beside-float mark turned on under Word mode (D11), as a
  step of its own with its own gate. Re-measure there what CR-001 §6.6 item 8 calls "float-overflow-below measured
  inert on docx4j's tables": that reading went through docx4j's managers, which never set the mark, so it may not say
  what the rule does under Word layout.

## 6. Decisions

| # | Question | Options | Recommendation |
|---|---|---|---|
| D1 | What docx4j on stock Apache FOP gets | (a) plain FOP layout, all of the package's rules gone, a `FopCapabilities` warning; (b) keep docx4j's copy for stock FOP only (two copies) | (a), as Jason's question has it; note it is the whole package, not only line breaking |
| D2 | The form of Word mode in the fork | (a) inside the fork's own managers, one class each, behind a switch; (b) docx4j's classes moved as they are into the fork | (a): (b) is still a copy beside FOP's `LineLayoutManager`, and Apache's changes would not reach it. Placement, from the fork session's count: of 2.11's 55 members of the class, 38 are unchanged in the copy and 14 modified (+272/−120: the constructor, `Paragraph`, `filterActiveNodes`, `makeLineBreakPosition`, `updateData1`, `updateData2`, `collectInlineKnuthElements`, `findOptimalBreakingPoints`, both `getNextKnuthElements`, `postProcessLineBreaks`, `getChangedKnuthElements`, `getHyphenContext`, `addInlineArea`); the other ~2,900 lines are ~70 new, self-contained members. The branches go into the 14 members of the fork's `LineLayoutManager.java` (1,906 lines to about 2,100, still reviewable against Apache's) and the new members into a fork class beside it. Merge cost: Apache has 13 commits to the class since 2020 (two the fork's own, FOP-3348 and FOP-3349), none since 2.12; of the 6 members 2.11 to 2.12 changed, 3 are among the 14. The other managers are subclasses with 1 to 3 overrides each (commits since 2020: BlockLM 2, ListItemLM 10, FlowLM 3, PageNumberCitationLM 0) |
| D3 | How docx4j turns it on | (a) a fork API on `FopFactoryBuilder` (or the user agent), found as a `FopCapabilities` hook; (b) attributes on `fo:root` | (a), as one setter taking JDK types only (say a `Map<String,String>` on `FopFactoryBuilder`, read in the managers through the `FOUserAgent`), since one source must compile under both renderers (docx4j-export-fo compiles against the fork by default, `fo.renderer.*` in its pom, and against Apache FOP 2.12 under `-Papache-fop`), so the call is by reflection, as `LBP` calls `setRuleStyle` since the 2.12 move; Apache's FOP-2860 simple line breaking (`fobj.getUserAgent().isSimpleLineBreaking()` in `LineLayoutManager`) is the precedent for the plumbing. A typed settings class would mean a reflective call per setter. The per-document values already on `fo:root` (hyphenation zone, tab hang) stay there. Names are the fork session's |
| D4 | The 35 tuning properties | (a) passed through D3's API by their existing names, read where docx4j reads them today; (b) renamed as FOP configuration entries | (a): users' settings keep working |
| D5 | The fork's default | (a) Word mode off; (b) on | (a): the fork's own tests are FOP's, and a user of the renderer without docx4j gets FOP |
| D6 | Break opportunities for `TableWriter` | (a) docx4j keeps its own copy (383 lines); (b) the fork's when advertised, FOP's plain UAX #14 on stock FOP | (b): one copy, and on stock FOP it matches plain FOP's breaking. It needs an entry point of its own: `TableWriter` asks while the FO is written, before any `FopFactory` exists, so not D3's setter but a static method on a fork class, found by reflection as D3's setter is |
| D7 | The tests | (a) the manager-level tests (19) move to the fork, docx4j's FO-path tests (7, export-fo-tests) stay with an assumption on `word-layout`; (b) all stay in docx4j | (a). They port: all 19 are JUnit 4 and FO-based through `FopFactoryBuilder`, as fop-core's are, and only one imports a docx4j class (`FopCapabilities`). The work is mechanical, about a day: fop-core's checkstyle covers tests (tabs on 2,892 test lines, 19 long lines and a star import in the line manager), slf4j to commons-logging (D8), and the 7 tests that set system properties set D3's API instead. The FO-path tests in export-fo-tests that reach the package (`TabAlignmentTest`, `HyphenationFoTest`, `WordLayoutFixupsTest`, `TabLeaderTextLayerTest`, `TabStopHintsTest`, `ListLabelSuffixSpaceTest`, `SmallCapsLineHeightTest`) need an `Assume` on `word-layout` added: export-fo-tests has none on a Word-layout hook today (only on ligatures and the ToUnicode map) |
| D8 | Logging in the moved code | (a) the fork's (commons-logging, as FOP's); (b) slf4j | (a) |
| D9 | Timing | (a) in 2.12-docx4j.1, delaying its Central release and docx4j 17.3.6's push; (b) after: 17.3.6 ships with the copy (and phase 0), the move in 2.12-docx4j.2 and docx4j 17.3.7; (c) (a) with a date (say end of November) after which, if phase 2 has not gated PASS, 2.12-docx4j.1 ships as gated (b298) and the move goes to .2 | **(a), decided 2026-11-02** (Jason; not (c): no fallback date). Recommended (b) at drafting. docx4j-11's assessment of (a): sound, with (c)'s hedge. Its cost: the docx4j commits made since 17.3.5 (16 so far, unpushed) wait on phase 0, the fork's 1.5 to 2 weeks, phase 2's gate and the runbook, about 3 to 4 weeks, putting 17.3.6 in early December, within the release cadence. Its gain: the first public 2.12 renderer carries the settings API and the hook from the start, one Central gate instead of two, and phase 0's copy is never released only to be deleted. Under (a) a docx4j 17.3.6 release entry depends on the fork's CR for phase 1 and on fop/CR-030, and fop/CR-030's release waits on the fork's CR |
| D10 | Phase 0 first | (a) yes; (b) no, the move brings the two hunks | (a) |
| D11 | fop/CR-023 §4.2's overflow-beside-float mark under Word mode | (a) left out in phase 1, so phase 2 is a pure move; turned on in phase 3, gated on its own; (b) on from phase 1 | (a), the fork session's recommendation: (b) would move lines beside side floats in phase 2's gate, where they could not be told from a move error |

## 7. Risks

- **Upstream merges conflict in the fork's `LineLayoutManager`** wherever Apache changes what Word mode changed; the
  fork session resolves them, as it did merging 2.12 (fop/CR-030). Fewer than a re-derivation: the rest of the class
  merges itself.
- **Behaviour drift during the move.** Guarded by phase 2's gate against phase 0 (0 movers expected) and by the moved
  unit tests.
- **Size.** Phase 1, by the fork session's estimate, 1.5 to 2 weeks; most of it unwinding `LBP`'s reflection and
  re-applying the 14 members onto the 2.12 base. The fork side confirms the cost argument: fop/CR-024 §10.2 put the
  width provider below the line manager because anything built in it reaches the gate only once docx4j copies it;
  CR-001 §6.6 items 36 and 37 were each fixed twice (FOP-3348/3349 in the fork, 2ba434600 and 53b2b5921 in docx4j);
  §6.6 item 10's phase B would be built once.
- **Release coupling.** A Word-layout change becomes a fork commit, a gate, a fork release and a docx4j bump, where
  it is a docx4j commit today; already so for the fork's side. The package changed in 62 commits (`git log
  --no-merges`), the last the 2.12 move (27bf221d4, 1 November, `LBP`'s rule-style setter); the line manager's file in
  46, 13 in October, the last on 15 October.
- **Slower fidelity iterations on a line rule.** Each try becomes a fork commit, a `~/fop-renderers` drop and a gate
  from docx4j by the fork-jar recipe (memory: fork-jar gate recipe), where today it is a docx4j commit and a gate.
- **Less of the fork stays upstreamable.** The fork's difference from Apache grows in its most-changed class, so fewer
  of its fixes stay in a form Apache could take. Greedy breaking as an option arguably could (FOP-2860 is the
  precedent for such an option); GDI rounding and the tuning properties could not.
- **Stock-FOP users' PDFs change** (D1): lines and pages move, by about a quarter of ragged-right lines. CHANGELOG
  states it as a known regression and names who they are: applications that already ship Apache FOP, which the fork
  cannot share a classpath with since it keeps FOP's package names. docx4j-export-fo's README promises them that "the
  rest renders as before", which stops being true and is reworded. Also the `FopCapabilities` warning;
  `-Papache-fop`'s tests that assume Word layout skip (D7).
- **Users who set `docx4j.convert.out.fo.wordLayout.*`** keep their settings (D4); a renderer without the hook
  ignores them, with the warning.

## 8. Registry and hand-offs

- Registry (via the portfolio session): `docx4j/CR-071`, proposed; on acceptance it depends on the fork's CR for
  phase 1, and phase 2 on that release.
- The fork session: phase 1 under a fork CR, from this one; the hook and API names are its to choose, with D3's
  shape.
- Enterprise CR-001 §6.6: the entries whose workaround is "by reflection" are re-homed to the fork at phase 2.
