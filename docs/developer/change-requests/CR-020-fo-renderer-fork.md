# CR-020: `docx4j-fo-renderer` — an upstream-tracking fork of Apache FOP, with graceful degradation to Apache's own

Status: ACTIVE (accepted by Jason Harrop 2026-09-18). Phases 0 and 1 landed
the same day; phase 2's classification report landed 2026-09-19 (§8, tables in
§9) and its cherry-picks are queued as batch items; the first release,
2.11-docx4j.1, shipped 2026-09-25; the switch of docx4j-export-fo's default to
it is in progress for 17.3.0 (§8, "The switch"). The decision to fork, the naming and the degradation contract were
taken by Jason Harrop on 2026-09-18 (recorded in §2).
Drafted with Claude Fable 5.1 at the close of CR-001 batch 48; the first
release moved out of phase 0 on Jason's direction the same day.
Owner: Jason Harrop.

Scope: a fork of Apache FOP 2.11 maintained at `plutext/xmlgraphics-fop`,
published to Maven Central as `org.docx4j:docx4j-fo-renderer-*` once it has
earned a release (not before phase 2 is done, §4); the switch of
`docx4j-export-fo` to it with a start-up capability probe that keeps Apache FOP
2.11 working; the hooks that replace today's reflection; the classified
cherry-picks from the Metanorma and Chunlin forks; and, last, the structural
items Enterprise CR-001.6 names. Not in scope: any change to what FO docx4j
emits (that is CR-001's business), or to Batik and xmlgraphics-commons, which
stay Apache's.

## 1. Background

### 1.1 How far docx4j already reaches into FOP

docx4j renders PDF through FOP but no longer treats it as a black box:

- **`docx4j-core` carries a repackaged slice of FOP's font code**
  (`org.docx4j.fonts.fop.*`: `FontFileFinder`, `FontInfoFinder`, `FontTriplet`,
  `FopConfigUtil`, the font cache) so that font discovery and mapping do not
  need FOP on the classpath.
- **`docx4j-export-fo` replaces FOP's layout managers** (`org.docx4j.fop.wordlayout`:
  `WordLayoutManagerMaker`, `WordLineLayoutManager`, `WordListItemLayoutManager`,
  `WordPageNumberCitationLayoutManager`, ...) to lay lines out as Word does, and
  reaches into FOP's area tree and Knuth elements to do it.
- **Three of CR-001's rules live only by reflection into FOP privates**, each
  recorded in Enterprise CR-001 §6.6: the leader's step on Word's 1/300-inch
  grid reads a placed leader's unit from `InlineParent.inlines` (item 27, "a
  leaf layout manager is never told where on the line it sits"); the glyph-run
  leaders descend a single-child `InlineParent` to move each wrapper's IPD
  (batch 48 item 2); and Word's break after a hyphen before digits flips one
  cell of `LineBreakUtils`' private static pair table (item 29, "predates
  Unicode 8.0 and has no API to correct a pair"; batch 48 item 3, worth 5253's
  page and +771 matched lines).
- **Fourteen §6.6 items are open against FOP** (8, 10, 11, 12, 18-26, 29) and
  our four fixes to date sit in a fork already: `plutext/xmlgraphics-fop`
  (local `../xmlgraphics-fop-plutext`), branches `FOP-3328` (the variable-font
  assertion, upstream PR 106), `FOP-empty-glyph-not-composite` (item 23),
  `FOP-cjk-radical-tounicode` (item 26) and `FOP-packed-glyph-bboxes`, with
  FOP-3330 (font retention, PR 107) beside them. Two of those PRs have been
  open upstream for months.

Every one of those is a cost paid in fragility - a FOP point release can break a
reflective read silently - rather than in a fork.

### 1.2 Upstream

Apache FOP is maintained, and moving: its most active committers work for
Smart Communications, whose document-generation product formats through FOP and
whose stated engineering practice is to fix FOP upstream rather than carry a
private fork. That is good for us on two counts - the mainline keeps receiving
font, PDF and security work we want - and it sets the shape of our fork: track
upstream and send general fixes there, do not diverge in architecture.

### 1.3 Precedents

- **Metanorma** (`metanorma/xmlgraphics-fop`) is an upstream-tracking product
  fork: 211 commits over Apache `main` (PDF/UA tagging, attachments, figure
  placement, Japanese glyph handling, `shrink-to-fit`), its own releases
  (v2.11.1-v2.11.5 since June 2026) with periodic merges of upstream. Read on
  2026-09-18: group `org.metanorma`, artifacts `fop-parent`/`fop-core`, name
  "Apache FOP Parent (Metanorma Fork)", published to GitHub Packages, packages
  untouched, README Apache's own - no user guidance and no classpath warning,
  because its only consumer is Metanorma's own tool. Not a precedent for the
  naming (§2.2) or the documentation (§3.4).
- **Chunlin Yao** (`chunlinyao/fop`, branch `yao-2_11`, ported to 2.11 on
  2026-09-08): CJK vertical writing, ruby, `fox:shrink-to-fit` for block
  containers and list blocks, custom letter-spacing. Metanorma pulled its
  `shrink-to-fit` from here.

## 2. Decisions (2026-09-18, Jason Harrop)

### 2.1 Fork, tracking upstream

An upstream-tracking fork of Apache FOP 2.11, Metanorma's shape: a branch off
the 2.11 release tag, `upstream` = `apache/xmlgraphics-fop`, periodic merges
of upstream `main`, general fixes sent upstream as PRs so the fork's delta
stays small. Batik and xmlgraphics-commons are not forked.

### 2.2 Naming: a distinct identity, no Apache mark in the product name

ASF's Downstream Distribution Branding Policy allows a modified distribution
to keep the Apache product name only for accepted upstream fixes, reported
bug fixes and limited backports; novel functionality does not qualify. And
third parties should not incorporate an Apache project mark into their own
product names. So:

- group `org.docx4j`; artifacts **`docx4j-fo-renderer`** (the all-in-one, what
  `fop` is: the artifact a consumer depends on), **`docx4j-fo-renderer-core`**,
  `docx4j-fo-renderer-events`, `docx4j-fo-renderer-util` (both forked, one
  version line; §5's question, decided in phase 0) and
  `docx4j-fo-renderer-parent` - no "fop" in an artifact id;
- description: `docx4j FO renderer, based on Apache FOP 2.11`;
- version `2.11-docx4j.1`, `.2`, ... (the FOP line first, our release after;
  Maven orders the qualifier before `2.11`, which is harmless across group ids);
- this notice, verbatim, in the fork's README, the pom description's
  neighbourhood and `docx4j-export-fo`'s documentation:

  > This is a modified distribution derived from Apache FOP 2.11. It is
  > maintained by Plutext/docx4j and is not an Apache Software Foundation
  > release. Apache FOP is a trademark of the Apache Software Foundation.

- the licence's own requirements: the Apache `NOTICE` carried forward, a
  prominent change notice in every modified file.

### 2.3 Java packages are kept as `org.apache.fop`

Maven Central verifies the group id's namespace, not the packages inside a
jar; the JLS's reversed-domain rule is guidance; ASF's policies govern the
name and the notices. Keeping the packages means cherry-picks from Apache,
Metanorma and Chunlin stay plain git operations, our layout-manager subclasses
and every user's FOP configuration and extension keep working, and a fork fix
can still go upstream as an ordinary patch. If the double-classpath clash of
§3.2 bites real users, a **relocated (shaded) variant** can be published later
from the same source without changing the fork; that costs every user of FOP
internals a rename, so it is the escape hatch, not the default.

### 2.4 Graceful degradation

docx4j is a library, so its users bring their own dependency trees.
`docx4j-export-fo` depends on the fork by default; a user who excludes it and
adds Apache FOP 2.11 must still get a working export, with the rules that
need a fork hook switched off and the rest as they are today. Never worse than
now; the hooks are the only thing lost.

## 3. Design

### 3.1 The fork

- Repository `plutext/xmlgraphics-fop` (exists; the JIRA branches are there).
  New branch **`docx4j-2.11`** from the `fop-2_11` tag; `upstream` remote;
  merge upstream `main` at least at every Apache release and whenever an
  upstream fix we sent lands.
- Build: FOP's own Maven build with the coordinates of §2.2; FOP's test suite
  runs in CI (GitHub Actions) on every push; release to Maven Central through
  the existing `org.docx4j` credentials, signed, with sources and javadoc -
  but not in phase 0. Until the first release the fork is consumed as a
  locally installed `2.11-docx4j.1-SNAPSHOT` (`mvn install` in the fork's
  checkout), which is enough for the harness and for development on this
  machine.
- Every fork-only change carries a JIRA reference where one exists and is
  listed in the fork's README under "Changes from Apache FOP 2.11" with its
  §6.6 item number; a change without a JIRA is sent upstream first unless it
  is docx4j-specific (a hook FOP would not want).

### 3.2 The consumer: `docx4j-export-fo`

- The dependency becomes `org.docx4j:docx4j-fo-renderer-core` (the Apache
  `fop` artifact today at `docx4j-export-fo/pom.xml` line 259, with its
  exclusions carried over). Batik and xmlgraphics-commons stay as they are.
  Until the fork is on Central the swap lives behind a Maven profile
  (`fo-renderer-fork`, off by default): the default build keeps Apache FOP
  2.11 so that anyone building docx4j from source is unaffected, and the
  profile points at the local snapshot. The profile is removed and the fork
  becomes the default dependency at the first release.
- **`FopCapabilities`**, a start-up probe in `org.docx4j.convert.out.fo`, run
  once per JVM: looks for the fork's marker class
  (`org.apache.fop.docx4j.Docx4jFop`, carrying the fork version and a set of
  capability constants, one per hook), records which FOP and which version is
  present, logs one line (`FO renderer: docx4j-fo-renderer 2.11-docx4j.1,
  hooks: pair-table, leader-position, inline-access` or `FO renderer: Apache
  FOP 2.11, hooks: none - N rules off`), and exposes `has(Capability)`.
- **Each hook-dependent rule is gated** on `has(...)`, with today's reflective
  path kept as the fallback where one exists (the pair table, the inline
  descent) and the rule simply not applied where none does. So on Apache FOP
  the exporter behaves as it does at the end of batch 48.
- **Two checks that do not exist today**: two FOP copies on the classpath
  (count one marker resource, e.g. `META-INF/services/...` or a class of the
  fork's, and warn naming both jars: which wins is classpath order); and the
  FOP line found against the one `docx4j-export-fo` was built for (our layout
  managers subclass 2.11 internals; another line gets a warning with the
  version numbers).
- The visitor and XSLT pathways share the probe; nothing else in the FO
  emission changes.

### 3.3 The harness scores both

`docx4j-layout-fidelity` gains a switch (`-Dfidelity.fo=docx4j|apache`, or two
classpaths) and every batch close-out reports the corpus per-class table on
both, so the gap between the fork and Apache FOP is a number rather than a
claim. The b49-batch48 baseline is the last cut on Apache FOP; the first cut on
the fork must reproduce it exactly before any fork-only change lands (§4,
phase 0's gate).

### 3.4 Documentation

`docx4j-export-fo`'s README: one section stating the two supported
configurations (the fork, recommended; Apache FOP 2.11, with the named rules
off), the §2.2 notice, the double-classpath hazard, and where the start-up
line is logged. The CHANGELOG bullet under "PDF via XSL FO". The fork's README
as §3.1.

## 4. Phases

0. **Fork set-up.** The `docx4j-2.11` branch, coordinates, NOTICE/README/
   change notices, CI; cherry-pick the existing branches (FOP-3328, FOP-3330,
   empty-glyph-not-composite, cjk-radical-tounicode, packed-glyph-bboxes) with
   their upstream status recorded; installed locally as
   `2.11-docx4j.1-SNAPSHOT`, no release. `docx4j-export-fo` switched behind
   the `fo-renderer-fork` profile (§3.2), `FopCapabilities` in place with no
   hooks yet, the harness switch in. **Gate**: the corpora and probes
   on the fork reproduce b49-batch48 exactly except where a cherry-picked fix
   is expected to move a document (each such mover named and explained); the
   same run on Apache FOP 2.11 still reproduces b49.
1. **Hooks replacing reflection.** One fork change plus one exporter change
   per hook, the reflective path kept as the fallback: the pair-table override
   (§6.6 item 29), the leaf's position on the line and the placed leader's unit
   (item 27), the `InlineParent`/`TextArea` access the grid pass makes (batch
   48 item 2), then the small "no API" items as they are met (23, 24, 25).
   **Gate** per hook: corpora identical on the fork, and identical to b49 on
   Apache FOP (the fallback still works).
2. **Metanorma and Chunlin, classified then cherry-picked.** A report first:
   every one of Metanorma's 211 commits and Chunlin's five in a table (commit,
   area, what docx4j construct it serves, risk, verdict), written to this CR.
   Candidates already visible: Chunlin's CJK vertical writing and ruby
   (`w:textDirection tbRlV`, `w:ruby`, which we render only as rotated cells
   today) and `shrink-to-fit` (Word's text-box autofit, `wps:bodyPr`
   `normAutofit` with a font scale); Metanorma's PDF/UA tagging (our
   accessibility documentation), attachments, Japanese glyph handling. Each
   cherry-pick is a batch item: probe first where one exists, corpora per class,
   no class 2 loss. (Report: §8 phase 2 and §9. Two of the visible candidates
   did not survive it: ruby is an FO authoring pattern in Chunlin's example,
   not a renderer feature, and shrink-to-fit is better served from Word's
   stored font scale on the docx4j side.)
3. **The structural items** (Enterprise CR-001.6, PROPOSED): the column model
   and unequal columns, GDI rounding, text on both sides of an anchor and wrap
   around a page-positioned object (§6.6 items 8, 10, 11, 12), hyphenation as
   Word does it (22). Each gets its own design note in this CR when reached;
   these are the changes that would never be accepted upstream as they stand,
   and the reason a fork is worth more than a patch queue.
4. **Upstreaming, ongoing.** Every general fix goes to Apache as a PR with the
   JIRA text drafted for Jason to file (the FOP-3328 pattern); the fork's
   README lists what is still fork-only and why.

**The first release** (`2.11-docx4j.1` to Maven Central, the profile of §3.2
dropped, the fork the default dependency) is a milestone rather than a phase,
and it is not part of phase 0: it comes after phase 2 at the earliest, or
after part or all of phase 3 - Jason's call when phase 2 closes, on whether
the hooks and the cherry-picks alone are worth shipping or the first
structural item should be in. Until then the fork exists only as a local
snapshot and nothing published depends on it, so the fork can be reshaped
freely (the `fop-util`/`fop-events` question of §5 included).

## 5. Risks / open questions

- **Two FOPs on one classpath** is the real hazard of same-package forks; §3.2
  detects and warns, and the shaded variant of §2.3 is the way out if warnings
  are not enough.
- **Apache 2.12** will arrive; the fork rebases (merge) and re-releases as
  `2.12-docx4j.1`, and the exporter's line check must move with it. Our layout
  managers subclass internals, so a rebase can be work - it is work we do today
  on every FOP upgrade anyway, now with tests in CI.
- **Users of FOP internals** (their own renderers, event listeners, font
  configuration) are unaffected by the same-package fork and broken by a
  shaded one; that is why shading is the escape hatch.
- **FOP's test suite** must pass in the fork's CI, or a cherry-pick can break
  something CR-001's corpora do not exercise (AFP, PostScript, PCL).
- **The branding line** (§2.2) is the conservative reading of ASF policy; the
  application of trademark rules to a Maven artifact id is not spelled out,
  so the name simply avoids the question.
- **Maintenance** is, after the first release, a release when a batch needs
  one and at each Apache release: Metanorma's cadence, and less than today's
  reflection costs in batch time. Before the first release there is nothing
  to maintain but the local snapshot.
- **Open**: whether `fop-util` and `fop-events` are forked or consumed from
  Apache (they are unmodified; consuming them is simpler, forking keeps one
  version line) - decide in phase 0 from the build.

## 6. Suggested sequencing and effort (rough)

Phase 0 one to two days (the CI and the profile are most of it; no release);
phase 1 about a week across the three hooks with their gates; phase 2 one day
for the classification report, then variable, one batch item per cherry-pick;
the first release after phase 2 or into phase 3 (§4), a day for the Central
mechanics when it comes; phase 3 large and open-ended, planned item by item;
phase 4 ongoing. Phase 0 should start after CR-001 batch 48's follow-ups are
queued and before batch 49, so that batch 49 is the first batch gated on both
configurations.

## 7. References

- Enterprise CR-001 §6.6 (the FOP limitations list; items 8-29), and
  Enterprise CR-001.6 (`../docx4j-portfolio/tasks.yaml`, `enterprise/CR-001.6`),
  which this CR now feeds.
- `plutext/xmlgraphics-fop` (local `../xmlgraphics-fop-plutext`), branches
  `FOP-3328`, `FOP-empty-glyph-not-composite`, `FOP-cjk-radical-tounicode`,
  `FOP-packed-glyph-bboxes`; upstream PRs 106 (FOP-3328) and 107 (FOP-3330).
- `metanorma/xmlgraphics-fop` (compare `apache:main...metanorma:main`);
  `chunlinyao/fop`, branch `yao-2_11`; Apache FOP PR 99 (`shrink-to-fit`).
- ASF: Downstream Distribution Branding Policy; Third-party trademark use
  guidelines; Apache License 2.0 §4.
- `docx4j-layout-fidelity/RULE-CLASSES.md` (the gate the cherry-picks are read
  against).

## 8. Progress

### Phase 0 - DONE 2026-09-18

Fork: `plutext/xmlgraphics-fop` branch `docx4j-2.11` (local only, not pushed;
Jason's call), from the `2_11` tag: the four fix commits cherry-picked with
`-x` (FOP-3328 45fcebf1f, FOP-3330 9448a7460, the empty glyph 0ef2f80a9, the
Kangxi radical d3dbe8ad9), then the coordinates, marker class
`org.apache.fop.docx4j.Docx4jFop`, change notices in every modified file,
NOTICE and README.md with the §2.2 notice and the change table, and Apache's CI
workflow on the branch (ubuntu, JDK 8/11/17/21). Only `fop`, `fop-core`,
`fop-events` and `fop-util` are built; the sandbox, servlet and transcoders
stay in the tree unbuilt. Apache's own dependencies (Batik, xmlgraphics-commons)
had been referenced through `${project.groupId}` and are pinned to
`org.apache.xmlgraphics`. FOP's suite on the branch: 3544 tests, 0 failures;
checkstyle and spotbugs clean (a `RegexpHeader` suppression for the
Plutext-headed files under `org/apache/fop/docx4j`).

docx4j (3fbc6d74a): `FopCapabilities` as §3.2, with the two checks; the
`fo.renderer.*` properties and the `fo-renderer-fork` profile in
`docx4j-export-fo` and in the harness, which copies the fork's jars to
`target/lib-fork` and writes a `renderer` line into every `scoreboard.txt`.

**Gate** (b50-apache and b50-fork against b49-batch48, resaved basis, the
three corpora and the 144 probes): Apache FOP identical to b49 in every row;
the fork identical in every row but one, `14_ru-RU_sdt_num_tbl_7320` (real3,
class 3a) at +2 matched lines (0.9119 to 0.9127), explained: the document
writes U+202F (narrow no-break space) after a list dash, which shares Tinos's
glyph with U+2009; Apache FOP's ToUnicode maps the glyph back to U+2009, the
cherry-picked reverse-mapping fix (§6.6 item 26, written for the Kangxi
radicals) keeps the character written, and Word's PDF carries U+202F, so the
line now pairs. The fix is not CJK-specific: any glyph two code points share.
Same pages, same lines, dy 0.00 between the two renders.

### Phase 1 - DONE 2026-09-18

Fork (9d9e27602): four hooks, each a `Docx4jFop` capability name, public
accessors and one setter, no behaviour change - `pair-table`
(`LineBreakUtils.setLineBreakPairProperty`), `leader-placement`
(`FilledArea.getUnitAreas`, `LeafNodeLayoutManager.getCurrentArea` and
`setAreaInfoIPD`, `LeaderLayoutManager.getFont`), `inline-access`
(`TextLayoutManager` getters, `AlignmentContext`'s constructor and
`getLineHeight` made public, `InlineLayoutManager.getFont`,
`LeafPosition.setLeafPos`, `LineBreakPosition` public with getters,
`ListItemLayoutManager.getBodyList`) and `glyf-empty-glyph` (the fix already
in). Found on the way: `LeafNodeLayoutManager.setCurrentArea` is Apache's own
public method, so docx4j now uses it on both renderers. `LineBreakUtils` is
generated from Unicode data by `src/main/codegen`; the override must be
re-added after a regeneration (noted in the file and the README). Suite 3550
tests, 0 failures; checkstyle and spotbugs clean.

docx4j: `FopHooks` resolves each accessor to a `MethodHandle` through the
public lookup when the renderer advertises the hook, and `LBP`,
`WordListItemLayoutManager` and `WordBreakOpportunities` take that path first,
the field path (as before) otherwise; `FontPaddingResourceResolver.wrap`
returns the plain resolver when `glyf-empty-glyph` is present. A member the
fork advertises but does not have is logged and falls back. The module still
compiles against Apache FOP: no fork-only member is named in source, which is
the point - the accessors are a contract, looked up by name. Tests:
`FopHooksTest` and `FopCapabilitiesTest` on both renderers; the module's 200
unit tests pass on both.

**Gate** (b51-apache and b51-fork, the same corpora and probes): Apache FOP
identical to b49 in every row, and the fork identical to b50-fork in every row
(so still only the one explained mover against b49), with all four hooks
reported active in the scoreboard's renderer line.

### Phase 2 - classification report DONE 2026-09-19; cherry-picks queued

The three forks were fetched into `../xmlgraphics-fop-plutext` as remotes
(`upstream` = apache, `metanorma`, `chunlin`) and measured against each other
(§9.1). The unit of classification is the feature, not the commit: Metanorma's
142 non-merge commits net to sixteen features (§9.2), because about sixty of
them are build, CI, version-bump and "code reverted back to pass tests" churn
around a single upstream merge in May 2026. Every commit is nevertheless
tabled (§9.5) with its feature and verdict, as §4 asks, and the 69 merges are
listed with their range. Chunlin's seven commits are §9.3; the upstream
commits since the 2.11 tag, which the fork will meet at its first merge of
`main`, are §9.4 - they matter here because they supersede three of
Metanorma's fixes and because four of them touch CR-001 ground directly.

**What survived, in the order to take it** (each a batch item: a fork commit
cherry-picked with `-x`, its change notice and README row, the docx4j-side
change where one is named, and the class gate of `RULE-CLASSES.md` on both
renderers):

- **P2-1 Surrogate pairs** (M10d) - DONE, merged to `docx4j-2.11` 2026-09-25
  (fork tip 4074e0330; the gate as run and the merge are in the P2-1 record
  below). Under `font-selection-strategy="character-by-character"`,
  `TextLayoutManager` ended the word wherever per-character font selection
  changed font, so a word could be cut between a high surrogate and its low
  surrogate; `MultiByteFont.mapCharsToGlyphs` then rejected the fragment with
  "ill-formed UTF-16 sequence, contains isolated high surrogate at end of
  sequence" and no PDF was produced. `MultiByteFont`'s own guard is correct;
  the word splitting was the defect. Separately, on a path not reached in that
  failure, `CharUtilities.containsSurrogatePairAt` guarded its end-of-sequence
  case with `(index + 1) > length`, never true at the last index, so callers got
  `StringIndexOutOfBoundsException` where the javadoc promises
  `IllegalArgumentException`; corrected to `>=`. **An earlier version of this
  bullet attributed the render failure to `CharUtilities`, and said emoji and
  Extension-B ideographs in docx4j documents hit exactly this. Both were
  wrong**: the stack above is the measured one, and **docx4j documents do not
  hit this** (measured 2026-09-25) - docx4j never emits
  `font-selection-strategy` and splits runs at every font change itself, so a
  pair never straddles a font change inside one `TextLayoutManager`; a
  purpose-built astral-character document rendered identically on both jar
  sets, with no exception on the baseline. The fork carries it because it is a
  real FOP bug worth sending upstream, and as defence should docx4j's own run
  splitting ever change, not as a fidelity fix. The bidi-level-change guard is
  undemonstrated: no case was found in which the two halves of a pair take
  different levels, so that half is defensive. Coverage:
  `PDFEncodingTestCase.testPDFEncodingWithNonBMPFontCharacterByCharacter` in
  the fork's own suite, which fails on the baseline with the exception above;
  the docx4j-side `surrogate-pairs` probe remains as an astral-character
  regression but cannot fail on the baseline. Upstream: JIRA drafted, not yet
  filed.
- **P2-2 Foreign-XML attribute namespaces** (M12): `XMLObj` resolves a
  prefixed attribute (`xlink:href` on our SVG, MathML) whose declaration sits
  on an ancestor. Gate: an SVG probe declaring `xlink` on `fo:root`. Upstream
  first.
- **P2-3 Unresolved references reported per page** (M11, 04e4e40ad): a
  render-time event naming the page and the id. A harness diagnostic; gate
  identical. Upstream first.
- **P2-4 Arabic letter mark and zero-width space** (M10b, M10c): U+061C
  falls back to U+200B when the font lacks it; U+200B is not turned into a
  word space in accessibility mode. Gate identical unless a corpus text
  carries U+061C (check at cherry-pick). Upstream first.
- **P2-5 Accessibility set**, with the math-in-PDF CR and a PAC run over a
  docx4j document: `fox:actual-text` (M4) and `fox:placement` (M5) on
  graphics, `/ID` on Note and `Scope` on TH (M8a, after diffing against
  upstream FOP-3165 and FOP-3283, which cover the same ground), one GoTo
  action per link (M8c, JIRA FOP-3305), invisible text for an alpha-zero
  colour (M15, the searchable layer over SVG math). Link `/Contents` is NOT
  in this set: upstream FOP-3258 and FOP-3322 take `fox:alt-text` on the
  `fo:basic-link`, so docx4j's part is to emit it (the tooltip or the link
  text) with no renderer change.
- **P2-6 Japanese numerals** (M3), together with docx4j mapping the East
  Asian `w:pgNumType` formats it does not name today. Upstream first.
- **P2-7 Attachments** (M9), rewritten: `AFRelationship`, `/UF`, a
  `FileAttachment` annotation, so an embedded OLE package can travel with the
  PDF (PDF/A-3). Metanorma's version string-matches `/FileAttachment` inside
  `PDFDocument.encode` to switch the encoding; not as is.
- **P2-8 Ligatures** (docx4j's own, found on the way): FOP's default GSUB
  feature list applies `liga`; Word applies standard ligatures only when
  `w14:ligatures` asks. A `gsub-features` hook (§3.2 style) and a probe to
  measure it; Metanorma's language gate (M10e) is not the way.

**Withheld:** vertical writing (M2) until a `tbRlV` CJK probe exists and the
signature, gating and renderer issues in §9.2 are reworked - it is a
simulation on FOP's horizontal line model, and its public-signature changes
would break every consumer compiled against Apache FOP; shrink-to-fit (M1),
because Word stores the scale it computed (`a:normAutofit/@fontScale`,
`@lnSpcReduction`) and docx4j can emit the scaled sizes directly, with no
renderer change and no relayout loop. Skipped outright: §9.2 says why, item
by item; the Metanorma-only structure-tree heuristics and the tag-type change
that mutates static singletons are the ones to be wary of if they are ever
reconsidered.

**Superseded:** Metanorma's FOP-2529 patch (M10a) is our §6.6 item 26 fix
under another name - the JIRA exists, so the item-26 upstream PR goes there
rather than to a new issue; Chunlin's letter-space guard (C3) by docx4j's own
`fixLetterSpaces` and by upstream FOP-2722; the link `/Contents` work (M8b)
by FOP-3258 and FOP-3322.

**Corrections to §4:** ruby is not a renderer feature in Chunlin's fork (his
example stacks `fo:inline-container`s; docx4j can render `w:ruby` that way on
Apache FOP today); Chunlin's five commits are seven, two of them README.

**The upstream backlog** (§9.4) is the other output: 89 commits since the
2.11 tag, of which FOP-3181 (page duplication when the IPD changes - our
sections), FOP-2722 (letter spaces counted in the complex-script path, which
collides with `fixLetterSpaces`), FOP-2880 (soft hyphens, item 22), FOP-3273
(tagging stopping after the first page-sequence - docx4j emits one per
section), the three leader fixes and the five memory fixes behind issue 687
each deserve their own gate. Recommendation: the first release stays on the
2.11 base as §4 says; the merge of `main` is phase 4's first act, its own
gated item, straight after.

### P2-1 gate (2026-09-25) - surrogate pairs, on the fork's branch `P2-1-surrogate-pairs`

Cut and built by the fork's own session (the split of CR-020's work into two
sessions is recorded in the fork's `CLAUDE.md`, 0240513ae): two Metanorma
commits cherry-picked with `-x` plus a regression test; the snapshot
`2.11-docx4j.1-SNAPSHOT` installed by Jason (the session's permission layer
refused the install) at 11:13, verified there by disassembly against the
branch (two calls to `Character.isHighSurrogate` in `TextLayoutManager`, none
before). Gated here, the docx4j side:

| step | result |
|---|---|
| `docx4j-export-fo` tests, `-Pfo-renderer-fork`, classpath verified to hold the fork and no Apache FOP | 202 tests, 0 failures |
| a direct FOP trap (`fo:block font-selection-strategy="character-by-character"`, font-family "DejaVu Serif, DejaVu Sans", an emoji U+1F600 mid-block, last, and three consecutive; rendered by `org.apache.fop.cli.Main` on the phase 1 jars of 2026-09-18 and on the snapshot) | the baseline **throws** `IllegalArgumentException: ill-formed UTF-16 sequence, contains isolated high surrogate at end of sequence` (from `MultiByteFont.mapCharsToGlyphs` - its own guard is right; `TextLayoutManager` had formed the fragment; the fork session corrected its first attribution to `CharUtilities`); the snapshot renders the emoji whole. The bidi-only blocks (Arabic + emoji, in an LTR block and in an `unicode-bidi="embed"` inline) render on both: the font-change branch is the one that split the pair |
| the harness probe `surrogate-pairs` (emoji and an Extension B ideograph at run end, paragraph end, across a font change, inside an RTL run, and consecutive), rendered through docx4j on both jar sets | identical, no exception on the baseline: **docx4j cannot reach the defect** - it never emits `font-selection-strategy` (none in docx4j-export-fo) and splits runs itself at every per-character font change before FOP, and it never produces a lone surrogate. The probe stays as a regression for astral characters; its golden is Jason's to cut. The fork's suite now covers the `TextLayoutManager` half itself (`testPDFEncodingWithNonBMPFontCharacterByCharacter`, over FOP's Aegean600 test font) |
| probes, share corpus vs `goldens-nofields`, vs `b53-batch49-fork` | 148 scored, changed documents 0 (the two not in the baseline are CR-021's mc-textbox probes) |
| real (191), real2 (156), Word's re-saved basis, hyphenate=false, vs `b53-batch49-fork` | changed documents 0 and 0 |
| real3 (102) | 101 identical; `15_es-ES_fields1_num_tbl_2432` (1000 pages) unscored: OutOfMemoryError under `-Xmx7g`, the most the box had free (b53 needed `-Xmx14g` for it). **Closed without the re-score**: P2-1's whole main-source diff is five lines guarded by `Character.isHighSurrogate` (a primitive local, no allocation), so on text with no high surrogate it is byte-identical behaviour and cannot raise peak heap - and the document has **no character outside the BMP**: measured over its 14 XML parts, 4,862,224 characters, zero astral code points and zero numeric references to one. The OOM is the run's heap, a standing FOP memory characteristic of that document (§9.4's issue-687 fixes are the gate for it) |

Reading: no mover anywhere P2-1 was measured, as expected, since the change is
unreachable from docx4j's FO; the fix stays worthwhile for a consumer's own
FO and for the fork's parity with upstream, but the consequence the fork
session put to Jason stands - P2-1 buys docx4j nothing it could observe, and
CR-020's reason for taking it first (emoji and Extension B ideographs in
docx4j documents) does not hold as stated. 449 of 449 documents and 148 probes: a pass, on
P2-1's grounds, with the 2432 row inert by construction rather than scored;
**merged to `docx4j-2.11`
2026-09-25** by the fork session, fast-forward, tip 4074e0330 (six commits
over 0b61748ed, unpushed; the two cherry-picks keep Alexander Dyuzhev's
authorship and their `-x` provenance lines). With Jason: the push, and filing
the JIRA so the upstream twin `FOP-surrogate-pair-word-split` becomes a PR.

For the next items, agreed with the fork session before it starts: P2-2 to
P2-8 are not all inert as P2-1 was. **P2-5** (the accessibility set) changes
PDF structure by design, so "no movers" is the wrong gate - what a pass means
(structure tags present, text layer and line parity unchanged) is to be
written down before the cherry-pick. **P2-8** (the GSUB features hook) does
nothing until docx4j asks for it, so the docx4j side gates the rule that uses
it, with the harness on both renderers, as `RULE-CLASSES.md` requires.

Reachability, measured 2026-09-25 for the fork session's pass over P2-2 to
P2-8 (every `word/*.xml` part of the three corpora on both bases, and the
probes; literal characters and numeric references): **U+061C** (the Arabic
letter mark, P2-4's first half) occurs nowhere; **U+200B** (the zero-width
space, its second half) occurs 4 times in 2 real3 documents
(`12_en-US_sdt_fields1_num_11231`, `12_hr-HR_sdt_fields1_num_tbl_2451`) and
nowhere else; characters outside the BMP occur in no corpus document and
only in the probe `fonts-symbol-and-emoji`. So P2-4's letter-mark half is
inert on this corpus, and (the fork session's correction, the same day) its
zero-width-space half is inert on the default path too: the change is one
conjunct inside `TextLayoutManager.isWordSpace`, which opens with
`userAgent.isAccessibilityEnabled()`, and docx4j never sets accessibility
itself. Containing the character is necessary, not sufficient: a corpus run
cannot gate P2-4 at all (it returns no movers whether the change works or
not). Recommendation to Jason: split it - drop the letter-mark half on the
scan; keep the zero-width-space half as a low-priority item for consumers
who enable accessibility, gated by rendering those two documents with
accessibility on, on both jar sets, and checking the extracted text (copy
and paste output is the point), not pixels. The same scan confirms P2-1's
result across the whole corpus, not just the one document. A shape note for
`inline-access`: the zero-width-space commit widens `GlyphMapping`'s public
eleven-argument constructor to twelve; docx4j-export-fo only reads
`GlyphMapping`, so it compiles either way, but it is an API break for anyone
built against Apache FOP 2.11 - the fork keeps the old signature delegating
with `false`. The fork session's other findings from that pass, as it reported them:
P2-8 (ligatures) is the one live fidelity defect - FOP applies `liga`
unconditionally and docx4j-export-fo has no `w14:ligatures` handling (grep
confirms), so Latin text in a font with a `liga` table gets ligatures Word
would not apply; P2-2 is inert while docx4j's two CR-011 `xmlns:xlink`
workarounds stand, so it wants a capability signal so they can be dropped;
P2-6 and P2-7 are two-sided (docx4j emits neither a `japaneseCounting` page
format nor `pdf:embedded-file`); P2-5's accessibility half is inert unless
the consumer sets it. The reordering is Jason's call.

**P2-8 design review (2026-09-25; the fork's `fop/CR-001`, 59db34636, §7 is
the docx4j half).** The premise "docx4j-export-fo has no `w14:ligatures`
handling" is wrong by module: `RunFontSelector.noLigatures` and
`FopConfigUtil.noLigaTwin` (docx4j-core, since 17.0.5) already send Latin-only
spans of runs asking for neither ligatures nor kerning to a `+noliga` twin
declared `encoding-mode="single-byte"` - the exact design the fork CR's §4
rejects - so on the common path (TrueType font, Latin text, no kerning) Word's
"no ligatures" is honoured today, on Apache FOP. The measurement that
motivated it is in `noLigaTwin`'s javadoc: FOP maps a minted ligature glyph to
U+E000 in ToUnicode, so a third of a French document's lines extracted with
"ti" as U+E000 - which also inverts the fork CR's §8 claim that extraction
cannot see a ligature (it can; a ToUnicode fix upstream is an item in its own
right). Where the twin does not reach, and so where P2-8 has value: CFF
substitutes (no twin; FOP would misdescribe the file), runs asking for kerning
but not ligatures (the `+kern` twin keeps GSUB), mixed-script spans, and the
positive features - Word 365's Normal template sets `standardContextual` in
`docDefaults` (measured on the fixtures), asking for `clig`, which FOP's
default list (`ccmp liga locl`) lacks and the twin cannot add. Mapping
corrections sent: every value without "standard" subtracts `liga`
(`contextual` = `-liga +clig`, not `+clig`); `all` = `+clig +hlig +dlig`;
`cntxtAlts` (`calt`) and `stylisticSets` are the same shape. The docx4j side
is therefore a replacement of the twin by the hook on the fork, the twin kept
as the Apache FOP fallback, the delta on the span's `fo:inline` the selector
already owns (per span, not per block), and the gate adds: TrueType/Latin/
no-kern runs byte-identical between twin and hook. The fork session accepted
the review in full (`fop/CR-001` rewritten, 8d1250cb7: the correction leads,
the case rests on the four gaps, the mapping table verbatim, the twin-aware
partition, per span) and verified the ToUnicode point from FOP's code:
`mapGlyphsToChars` falls through to `createPrivateUseMapping` for a
substituted glyph, `nextPrivateUse` starts at 0xE000, and `CIDSubset.getChars`
is what `PDFToUnicodeCMap` is built from - so a ligature's published meaning
is U+E000 upward. That is now **`fop/CR-002`**: let a multi-character
`CharAssociation` reach the CMap and emit a `bfchar` with a string
destination, the private-use mint kept as the fallback for glyphs with no
character. It affects every FOP user on the default configuration, breaks
search, copy-paste and screen readers, and is cleanly upstream-bound; the
fork session has put to Jason that it outranks P2-8. Enterprise CR-001 §6.6
gets the item (the twin is the docx4j-side workaround). A first scoring of mine on the raw corpus directory showed 11
changed documents; the baseline was cut on the re-saved basis, and the re-run
above is the valid one.

### First release - `2.11-docx4j.1` on Maven Central (2026-09-25)

Released by the fork session: `org.docx4j:docx4j-fo-renderer`, `-core`,
`-events`, `-util` and `-parent`, each jar with sources, javadoc and a detached
signature; the poms fetched back from Central carry literal versions and no
dangling parent. Verified from here the same day: all four artifacts resolve
from Central with `dependency:get`; the core jar's manifest says
`Implementation-Version: 2.11-docx4j.1`, which is what `Docx4jFop.version()`
reads (the `2.11-docx4j.development` string in the class is its fallback for
classes not loaded from a built jar); the marker's capabilities are the four
phase 1 hooks. Contents: the font fixes of phase 1 and before, the four hooks,
and P2-1. Nothing from `fop/CR-001` or `fop/CR-002`. The local snapshot
`2.11-docx4j.1-SNAPSHOT` was left in place, so the harness gate keeps working
until docx4j switches.

**Not yet done in docx4j**: the switch of §3.2 (the profile dropped, the fork
the default dependency of `docx4j-export-fo`) - a patch-release decision for
Jason, since it swaps a transitive dependency for every consumer of the module
(the two-FOPs hazard for a consumer who also depends on Apache FOP directly);
the alternative is to keep the fork profile-only in 17.2.1 and switch at the
next minor. Either way the Getting Started paragraph's "(not yet released)"
goes, and the CHANGELOG gets a "PDF via XSL FO" section.

### The switch - docx4j-export-fo's default, for 17.3.0 (started 2026-09-27)

Registry `docx4j/CR-020.switch`. Step 1, the dependency, done 2026-09-27 on
`VERSION_17_3_0`: `docx4j-export-fo` depends on `org.docx4j:docx4j-fo-renderer`
`2.11-docx4j.1` by default, and the old profile is inverted to `apache-fop`,
which builds against Apache FOP 2.11. The README's recipe for a consumer who
wants Apache FOP excludes all four fork artifacts, because the flattened pom
lists `-core`, `-events` and `-util` as direct dependencies too; excluding
`docx4j-fo-renderer` alone would leave the fork on the classpath beside Apache's.
The fidelity harness keeps Apache in `target/lib` and the fork in `lib-fork`,
and now excludes the fork artifacts from `docx4j-export-fo` as well.

Checked the same day against the installed 17.3.0-SNAPSHOT:
- export-fo's 202 tests pass on both renderers (the probe test's surefire
  classpath shows `docx4j-fo-renderer-core` by default, `fop-core` under
  `-Papache-fop`);
- the flattened pom depends on the four fork artifacts and on no Apache FOP;
- a default consumer gets `FO renderer: docx4j-fo-renderer 2.11-docx4j.1, hooks:
  pair-table, leader-placement, inline-access, glyf-empty-glyph`, and one following
  the README recipe gets `Apache FOP 2.11, hooks: none`, neither with a warning.
  Getting Started renders to 57 pages on each with `-ea`, and the extracted
  text is identical.

Still to come:
- The fork's `fop/CR-001` (the `fox:gsub-features` hook, capability
  `Docx4jFop.GSUB_FEATURES`) and a `2.11-docx4j.2` release, which becomes the
  version 17.3.0 depends on. The fork session waits for Jason's go-ahead on the
  hook, and on whether the fork carries `fop/CR-002` (the ligature ToUnicode fix)
  rather than waiting for Apache.
- Its docx4j half (fork CR-001 §8), in two parts, as the fork session measured
  2026-09-27. The subtractions (`-liga`) improve the text layer (no ligature
  glyph is formed), so they ship with `2.11-docx4j.2`. The additions (`+clig`,
  `+hlig`, `+dlig`) draw ligatures that have no Unicode presentation form,
  whose ToUnicode entry is a private-use code point, so they wait for
  `fop/CR-002`. Non-Latin spans: Jason's decision 2026-09-27 is that the hook reaches
  them, as measured. The measurement is the Word 365 probe `ligatures-arabic` (share
  `corpus/`, golden cut with WordGoldenRunner 2026-09-27; made by
  `~/fidelity-arabic-ligatures/make_probe.py`). Word kept Calibri 6.23's glyph ids in
  its subset, and Calibri's Arabic `liga` forms the Allah ligature U+FDF2. That
  ligature, and the lillah ligatures, are in every row, absent and `none` included; only
  `discretional` and `all` change the row (27 glyphs to 15, the `dlig` lookups). So
  Word applies the standard Arabic ligatures whatever `w14:ligatures` says, and the
  setting only adds. The Latin control in the same probe does follow it (Calibri
  "office fi fl ..." 146.6pt under none, 144.4pt under standard). Consequence: the
  `-liga` subtraction is written for Latin, Greek, Cyrillic, Armenian, Georgian and
  CJK spans, never for Arabic. Other shaping scripts are also excluded, unmeasured.
  The additions, when they come, apply to Arabic too. Side finding for an embedded-font
  CR: Word 365 ignored the embedded, obfuscated Noto Naskh Arabic (it drew those rows
  in Times New Roman and dropped the embedding on re-save), and it drew the
  Traditional Arabic and Arabic Typesetting rows in Calibri, although both fonts are
  installed on the VM (its re-save embedded them).
- The §9 partition, computed on the docx4j side 2026-09-27 (`~/fidelity-gsub-partition/`,
  not redistributable; the tools are in it). The FO is built exactly as the harness builds
  it, with the delta forced on. A document is a mover only if a delta span holds a
  sequence its font's type-4 `liga` ligates. Result: 36 movers, 426 still-nodelta, 141
  still-inert, of 603.

**Gate, 2026-09-27** (the fork's 2.11-docx4j.2-SNAPSHOT at 007ee2592, installed by the fork
session with Jason's clearance; the probe line lists `gsub-features`). One classpath (the
harness's lib-fork, stale jars filtered out), each corpus scored twice: the baseline with
`-Ddocx4j.convert.out.fo.gsubFeatures=false` (the twin as today), then the candidate with
the hook, against the baseline's scoreboard. Probes vs `goldens-nofields`;
real/real2/real3 with `hyphenate=false` as P2-1's gate. Read per document by comparing the
two PDFs' words and positions (`pdftotext -bbox`) against the partition.

| tier | documents | result |
|---|---|---|
| still-nodelta | 426 | 422 identical; 2 differ only in a TIME field that ticked between the runs; 2 unscored (no golden, `noref`, on both sides) |
| still-inert | 141 | 138 identical; 3 unscored (`noref`) |
| mover | 36 | 1 moved, improved: 15_de-DE_tbl_1660, Source Sans 3's "ft" no longer ligated, 0.5250 -> 0.5875; 35 unchanged |
| scoreboards | all four corpora | 0 regressions; changed documents 0, 1 (the improvement), 0, 0 |

**A pass.** The 35 predicted movers that did not move are explained, not failed. FOP never
ligated them on the baseline either:
- The CFF substitutes (Nimbus Sans Narrow, URW Gothic, P052, Nimbus Sans) are embedded as
  single-byte Type 1C, so no GSUB runs at all. The fork CR's first gap does not arise with
  docx4j's font configuration; Source Sans 3 is the exception, which is why it moved.
- Carlito's "ti"/"tt" (a type-4 `liga` in `latn` dflt) is not formed in docx4j's pipeline
  on either renderer. Measured on a minimal document: 22 glyphs for "notification station"
  on Apache FOP 2.11 and on the fork, even with `w14:ligatures` standard.

The hook itself is confirmed on a minimal document in DejaVu Serif, whose ligatures have
code points: the baseline draws ﬀ/ﬁ/ﬂ in the kerned and `contextual` runs; the candidate
draws the letters (15 glyphs to 18), keeps them in the `standard` run, and Apache FOP is
unchanged.

Open, found by the gate, and docx4j-side: **through docx4j, Carlito gets no GSUB at all.** A
minimal document in Calibri (so Carlito) with "office fine ti tt" draws 19 glyphs in every
run, on Apache FOP 2.11 and on the fork. That includes the plain `Carlito Regular` declaration
(not the twin) under `w14:ligatures` standard, and fi/ffi as well as ti/tt, so it is not about
code points. My first reading, that ligatures without a code point were being dropped, was
wrong; the fork session corrected it the same day. At font level (the fork's own loader,
`performSubstitution`), Carlito ligates ti -> U+E000, tt -> U+E001, fi -> U+FB01 and
ffi -> U+FB03, exactly as 17.0.5 measured, so the `+noliga` twin's premise stands and
fop/CR-002 matters more (Carlito is the corpus's commonest font). DejaVu Serif, a system
font, does ligate through docx4j. **Cause, found by the fork session the same day:** Carlito's GSUB has no `DFLT` script entry
(`cyrl`, `grek`, `latn` only); DejaVu's has one. docx4j writes no `script` on its FO, so FOP
works from its default script. `GlyphTable.matchLookups` falls back to `DFLT/dflt` when a
match is empty, so a font without `DFLT` gets no substitution at all, with no warning; a font
with one is rescued. Measured at font level on both Carlito copies (jar and system; they are
different files, both 1.104, and ligate identically): a script of `latn` or `*` ligates, and
`DFLT`, `zyyy` or `auto` does not. For DejaVu, all five ligate. Ruled out on the way: the jar
URL (FOP fixes `useAdvanced` in LazyFont's constructor, whatever the URI), the twin,
letter-spacing, `WordGlyphWidths`, the CJK switch, and the Calibri mapping.

Consequences: docx4j's Calibri documents get no ligatures on any renderer. That matches
Word's default by accident, but a run asking for standard ligatures gets none, and the gate's
10 Carlito movers were inert for this reason, not through a predictor fault. Not changed now: making Carlito shape
(for example by writing `script` on Latin spans) turns its ligatures on wherever the twin does
not reach, and until fop/CR-002 they reach the PDF as U+E000/E001. So the order is CR-002,
then the script, gated as this switch was. **Kerning, the same defect wider** (measured by the fork session the same
day): Carlito has no legacy `kern` table and its GPOS has no `DFLT` either (DejaVu has both).
So under a default script the `+kern` twin (`kerning="true"`) kerns nothing: "AV" is kerned
under `latn` or `*`, null under `DFLT`. docx4j's per-run kerning has been a no-op for Carlito.
The runs it reaches are ones Word kerns: `RunFontSelector.isKerned` is Word's rule (`w:kern`
present, non-zero, no larger than `w:sz`). So FOP has been dropping kerning Word applies,
and the script fix is expected to be a fidelity gain for kerning. It is also a change across
most kerned Calibri text, not a ligature change, so it wants its own CR, partition and gate,
with a large mover set. The FOP behaviour itself (a font without `DFLT` silently gets no substitution under
a default script) is a candidate upstream report; the fork session offers to draft it.

**The language face of it** (fork session, 2026-09-27, `docs/upstream/no-default-script-table.txt`
in the fork): with `script="latn"` and `language="en"`, a font with no `ENG` language system
falls back to `(DFLT, dflt)`, not to `latn`'s default language system. So the result depends on
whatever that font's `DFLT/dflt` carries. The system DejaVu Serif has `liga` there and ligates
under `language="en"`; the system DejaVu Sans's `DFLT/dflt` has `case`, `ccmp`, `dlig`, `kern` and
no `liga` (its `latn` default has `liga`), and does not: two faces of one family differ. docx4j writes
`language` on every FO (all 191 real-corpus FO files; from `w:lang`/docDefaults), and cannot
drop it, because FOP's hyphenation takes its patterns from it (docx4j hyphenates by the
document's language since 17.1.0). So docx4j's script fix needs FOP's fallback order changed
to `(script, lang) -> (script, dflt) -> (DFLT, dflt)`, as OpenType engines do: in the fork, with
a capability so docx4j can tell, and as the upstream report's proposed fix. Then docx4j only
writes `script`. The fork session agrees on the shape. It changes shaping and kerning for every
run that carries a language today, so it wants its own branch and gate, not a ride on
fop/CR-002. Not started; it waits on Jason.
The predictor should also learn a font's embedding mode (CFF single-byte).
- Getting Started's "Two FO renderers" paragraph, rewritten once, when
  `2.11-docx4j.2` is in.
- **Bump DONE 2026-10-02.** `2.11-docx4j.2` is on Maven Central (fork tag `v2.11-docx4j.2`
  at ae4d4bc59; fop/CR-001 to CR-007; eight capabilities). docx4j-export-fo's
  `fo.renderer.version` is `2.11-docx4j.2`, resolved from Central (a stale negative cache of
  the util jar from before the sync had to be cleared); 231 export-fo tests pass against it.
  The fidelity harness's `-Pfo-renderer-fork` snapshot is `2.11-docx4j.3-SNAPSHOT`. The
  Getting Started rewrite above is still to do. The CJK default (`cjkAdvancedFeatures` on
  behind `shared-glyph-tounicode`, fork CR-006 §4) is gated after Jason's yes, per CR-006 §5.

### fop/CR-006 gate - the CJK default on the release (2026-10-02): PASS

Jason's yes given 2026-10-02. On `2.11-docx4j.2` from Maven Central (the harness's
`lib-fork` rebuilt clean against it), two questions, both answered:

**The release reproduces the last snapshot gate.** Baseline `cjk-base` (property unset, the
17.2.0 default: `advanced="false"` on every font whose cmap aliases a radical onto an
ideograph) scored against `cr007-cand` (the 09-30 snapshot): real 0 changed, real2 0, real3 0,
probes 0 (goldens-nofields). Nothing moved between the snapshot and the release.

**The candidate, `cjkAdvancedFeatures=true`, changes what CR-006 §4 said it would and nothing
else.** The flag reaches FOP only through `mustNotUseOpenTypeLayout`, so only the fonts
`GlyphCheck.reverseLookupTakesACjkRadical` names can move: measured here, Source Han Sans CN,
JP and KR (true) and not Droid Sans Fallback (false; it is a TrueType with its own radical
glyphs). The documents whose FO names such a face: three in the whole corpus
(`12____tbl_6108`, `14_bs-Latn-BA_tbl_12851` in real/real2 and `12_en-US_num_tbl_11334` in
real3, all Source Han Sans CN; "URW Gothic" matches a CJK name pattern but is Latin) and two
probes (`fonts-space-cjk`, `fonts-cjk-linebox`). Scored as a mini-corpus and in the probes:
0 changed; the three documents' PDFs are the same size, `mutool trace` gives identical glyph
sequences and positions (1,677 / 7,135 / 8,864 glyphs), `pdftotext` identical, and the
Kangxi count is 0 on both sides (the capability's promise; 988 / 49 / 1,257 ideographs
intact). Why nothing moved: none of the three sets a single Latin letter in Source Han Sans
(0 of 1,270 / 49 / 1,219 characters), and a CJK font's tables do nothing to horizontal text
of its own region, as the 17.2.0 javadoc measured.

**That the flag is in force was proved, not assumed.** A kerning probe (`AVATAR To Ye WAVE
Type LTV.` at 24pt in Source Han Sans CN, once with `w:kern` and once without, plus a
Chinese line ending in AVATAR; `~/.../scratchpad/cjk/kern`): with the tables off every line
is unkerned; with them on, the `w:kern` line's AV, TA and other pairs close up (26 of 27 glyph
positions move, first `V` 14.93 -> 14.52pt), the line without `w:kern` does not move, and the
Chinese line's ideographs do not move while its AVATAR does. The text layer is the same
characters either way. That is exactly §4's predicted mover class, Latin text in a CJK font
on a `+kern` run, which the corpora do not contain.

**Landed:** `FopConfigUtil.cjkAdvancedFeatures()` now answers the property when it is set,
else whether the renderer on the classpath declares `shared-glyph-tounicode` (a one-question
reflection probe of `org.apache.fop.docx4j.Docx4jFop`, since the font configuration is built
in docx4j-core before export-fo's `FopCapabilities` is touched); `keepsCjkLayoutTables()`
exposes the answer; `CjkLayoutDefaultTest` (export-fo) holds that it agrees with
`FopCapabilities` and that the property overrides either way. On Apache FOP nothing changes.
CHANGELOG 17.3.0; Enterprise §6.6 item 26 says the workaround is lifted by default on the
docx4j renderer. The fork's §5 "two commits" are the capability constant there (in
2.11-docx4j.2) and this default here.

### fop/CR-002 gate - the ToUnicode fix (2026-09-27)

The fork session's `fop/CR-002` (branch `CR-002-ligature-tounicode`, off 2.11-docx4j.2, carrying
the gsub-features hook). The pass statements were agreed beforehand: geometry unchanged
everywhere; the per-document private-use count falls; line parity improves or holds.
Candidate against the ligature gate's candidate `gsub-cand` (same hook, no CR-002), all four
corpora, same settings. Geometry is compared glyph by glyph (glyph ids and positions from
`mutool trace`, Unicode ignored), text by counting in `pdftotext` output.

**First run (de8ca1cf9, `cr002-cand`): passed on its face, but it was not right.** Geometry was
598/598, with two score improvements, and six real documents went from 0 private-use
characters to 1..21 (U+F0A7, F0A8, F0B7, F0E0, each replacing a "#"). I read that as a
correction. It was a defect in the commit, found by the fork session. Those "#" are what FOP
**draws** (`Typeface.NOT_FOUND`) when the declared font has no glyph, and the first commit
recorded the missing character as the "#" glyph's meaning, so the CMap published it for every
"#" in the subset, real ones included. The improvements were the text layer matching Word's
golden while the page still showed "#". Fixed in 7289e1726: the stand-in glyph records no
meaning, pinned by `testStandInForMissingCharacterRecordsNothing`. The lesson for reading
these gates: a text-layer change without a geometry change needs checking against what is
drawn, not only against the golden's text.

**Re-check (7289e1726, installed 19:16, `cr002b-cand`): a pass.**

| check | result |
|---|---|
| geometry | 598 of 598 documents identical, glyph for glyph |
| scoreboards | 0 changed documents in every corpus |
| presentation forms in the text layer | 480 -> 18 |
| private-use characters | 129 -> 81, all in two probes: ligatures-arabic 105 -> 63, fonts-hebrew-no-cs 24 -> 18 (the followers of one-character clusters, as designed) |

Latin ligatures show no text-layer change, because through docx4j Carlito gets no GSUB (the
`DFLT` finding above). The fork session merges to 2.11-docx4j.2 and updates §6.6 item 30.

Found on the way, docx4j's: 12_en-US_num_tbl_3236 (and five others) put a symbol-font code point
(U+F0A7 etc.) in a plain Times New Roman run, with no `w:sym`. Word draws a bullet; docx4j sets
Tinos, which has no glyph, so FOP draws "#". The glyph fallback does not handle U+F000-F0FF in a
non-symbol font as Word does. A layout-fidelity item, not CR-020's.

### fop/CR-003 gate - lookup fallback and the kerning flag (2026-09-27): FAIL, first run

The fork's `fop/CR-003` (branch `CR-003-script-fallback`, ca8cf0115, installed 20:18): FOP maps
the FO language to its OpenType tag and falls back `(script, lang) -> (script, dflt) ->
(DFLT, dflt)`; with it, a font declared `kerning="false"` loses `kern` from GPOS (measured
first: the flag had gated only the legacy table, so CR-003 alone would have kerned every plain
Carlito run). Capabilities `lookup-fallback` and `kerning-flag`. It corrects §8 above: FOP
derives `latn` itself, and the untranslated language was the cause, so docx4j does not need to
write `script`. docx4j's pipeline honours `advanced="false"` (via `WordWidthsFontCollection`):
on Apache FOP a CJK document has 0 Kangxi radicals by default and 156 when forced on.

The partition (`~/fidelity-gsub-partition/cr003/`) models before (DFLT/dflt; GPOS kern whatever
the flag) and after, per span. It excludes +noliga, fonts not CID-embedded in the baseline PDF
(by PostScript family), CJK `advanced=false`, and DejaVu's kern (§6.6 item 32: FOP applies none).
It was corrected twice during the reading (bold faces; Common-script spans resolve to `latn`):
447 still, 151 movers.

Candidate `cr003-cand` against `cr002b-cand`:
- text layer unchanged;
- 49 documents' geometry moved;
- scoreboards: 11 improvements (e.g. 15_de-DE_2299 0.7174 -> 0.8913, 14_es-MX_tbl_14140
  0.8788 -> 0.9848) and **5 regressions**. Four are `+kern` runs in Arimo or Tinos. In
  12_en-US_tbl_13872 the FO is identical, yet "repair, if" extracts as "repair,if", because the
  space after a comma or full stop is closed up: an interaction with docx4j's item-15 kernSpaces
  word-spacing, or a double adjustment, still to be found. The fifth is a Carlito Greek document
  (70 -> 71 pages, Word 68), possibly `cpsp`;
- 4 predicted-still documents moved, with small shifts of a space or full stop in single-byte
  Carlito after a bold span.

Not merged; the fork session is investigating. The pass statements stand.

**Reading, the same evening.** Split with docx4j's Word layout on and off, on the released
2.11-docx4j.1 and on CR-003:
- The "repair, if" collapse and the Greek extra page happen only with CR-003 **and** the Word
  layout on. Off, the Greek document is 69 pages either way, and its kerning (1,354 glyphs)
  moves as it should. Both are docx4j's: `WordLineLayoutManager` double-counts something CR-003
  now supplies. Not yet found; `fixLetterSpaces`, `emergencyBreaks` and the leader spacing in
  `LBP` are ruled out.
- The four still-but-moved documents were the **partition's error**. A CFF font (Nimbus Sans
  Narrow, P052, URW Gothic) loads as a MultiByteFont with GSUB and GPOS whatever its PDF
  embedding, so "not CID in the PDF, therefore never shaped" was wrong. Nimbus's `DFLT` carries
  `kern`, so its plain runs were kerned before, which Word never does, and the kerning flag now
  stops it (fork CR-003 §11.1; 6541 improves). With the exclusion made by declaration only
  (+noliga twins, CJK `advanced=false`): 408 still, all 408 identical glyph for glyph; all 49
  documents that moved are predicted movers (kern-lost 68, kern-gained 45, ccmp-led 72, script 5).

CR-003 stands as coded. What remains is docx4j's line-manager fix, then a re-gate of both
together.

**Re-read 2026-09-30: there is no line-manager defect.** Both remaining regressions were
traced glyph by glyph (mutool trace on the released .1 and on the CR-003 snapshot, the
Word layout on and off; scratch renders rebuilt from the same corpus and classpath):
- 12_en-US_tbl_13872 is a FOP painting defect that CR-003 exposes, Enterprise §6.6 item
  33: a word with GPOS adjustments is painted without its letter-space adjustments
  (IFRenderer.renderWord takes the DP alone). The line is bold Arimo with Word character
  spacing on every run; before CR-003 Arimo got no lookup under "en", so the letterAdjust
  path painted; with a kern it does not, the word overdraws its box by 6 x 0.417pt and
  swallows the space. The overdraw is there with the Word layout off too; off, the space
  happened to survive, which is what the "only with the Word layout on" reading saw. Fix
  asked of the fork (its own branch, gated with CR-003).
- 14_en-GB_sdt_num_8371 is kern-lost on a fallback font, and correct by Word's rule. The
  document is Cambria; docx4j maps Cambria to Caladea, which has no Greek, so 13,042 Greek
  spans fall back to P052 (CFF, GPOS kern under DFLT). No run has w:kern, so no +kern twin;
  before CR-003 FOP kerned P052 regardless of kerning="false" (A/nu -0.833pt at 24.5pt, and
  positive pairs too), CR-003 stops it. The words widen or narrow by their pair values, the
  Word rules re-break 144 lines and the document goes 70 -> 71 pages (69 with the Word
  layout off either way, Word 68). Page-independent line-text match against the golden
  falls 1210 -> 1074 of 1640: kerned P052 happened to sit closer to Cambria's Greek advances
  than unkerned P052 does. Not a CR-003 defect and not a docx4j one: the fidelity item is
  the fallback (a Greek-capable Cambria-metric face, or per-script fallback metrics),
  recorded below under "Not done, carried". The gate accepts this document as a mover
  whose movement is right and whose score is a font-substitution artefact.
- Also on .1, the trace shows the letter-spacing of a kerned P052 word painted as one lump
  after its last glyph (+5.138pt = 7 x 0.734 on "Ανοικτό"): item 33 again, on Apache-era
  behaviour, so the defect predates the fork and belongs upstream.

**Re-gate 2026-09-30, CR-003 + CR-005 (fork tip 4a3e36b7c, installed by Jason): PASS.** Runs
`cr003b-cand` against `cr002b-cand`, read with `~/fidelity-gsub-partition/cr003/gatecmp.py`
(word boxes from pdftotext -bbox: identical / paint-only / text-only / moved, crossed with the
partition and with `paintclass.py`, which names the 40 documents holding a letter-spaced span in
a font FOP positions). First a control: today's docx4j build on the CR-003-only jars against the
27th's `cr003-cand`, 557 of 570 word-box identical and 13 DATE fields printing the render day, so
the 27th's baseline stands.
- Still set (408 of 598, 148 probes included): 396 identical, 10 date-field text-only, 2 paint
  (real2 1137, real3 12222): letter-spaced Caladea +kern titles that FOP's legacy kern table sent
  down the DP path; before, the letter-spacing was not painted and every following word on the
  text area was drawn short of its area origin, now each glyph is advance + spacing + kern and the
  words sit where the layout put them (parity and pages identical; both predicted by paintclass).
- Movers (190): 79 moved, 5 paint-only, 1 date-only, 104 did not move (recorded, not failed;
  most are ccmp/cpsp-only predictions on plain text). Scoreboards on the real corpora: 22
  improved (+0.8112 line parity in all), 4 worse (-0.1002): 8371 -0.0785 (the Cambria-to-P052
  fallback, read above), 11559 -0.0127 (Tinos, ccmp under latn/SRB), 3229 -0.0070 and 7235
  -0.0020 (Nimbus Sans Narrow, kern-lost with no w:kern: right by Word's rule, Arial Narrow's
  metrics not Nimbus's). The 27th's four Arimo/Tinos regressions (475, 9832, 13872, 14776) are
  gone; 13872's text layer and geometry are back to the baseline's within its kern. Probes: 146
  identical, fonts-georgia and fonts-segoe-ui kern-lost as predicted, parity unchanged.
- Verdict: the still set holds, every movement is the one predicted, and each score drop is
  explained by a fallback font's metrics rather than a wrong lookup. The fork merges CR-003 (with
  CR-005) into 2.11-docx4j.2; fork CR-004 (DejaVu Sans's shared default language system) gates
  next, on its own partition, in the order Jason sets.
 Expected: 13872 back to its .1 text
layer and geometry within the kern; every letter-spaced span in a positioning font is a
new mover class (paint only, geometry of the boxes unchanged) - the partition's still set
must be re-read for it before the run, since a "still" document whose letter-spaced words
were painted lumped will now move in mutool's glyph positions without moving in layout.

Two docx4j items surfaced, neither for this gate: Cambria's Greek falls to P052 (Caladea
has no Greek; Word's golden is Cambria throughout); and kernSpaces (item 15) reads the
legacy kern table only, so Arimo's GPOS space pairs (A␠ -55, ␠A -55, T␠ Y␠ -18, measured
by the fork session) are applied by nobody.

### The twin gives way: fonts subset under the fork (planned 2026-09-30)

**Why.** docx4j's PDFs are about four times Word's, and fonts are most of it (CR-029 §9.4;
re-measured today: real 181.5 MB against Word's 39.1, real2 200.7 against 50.9, real3 208.6
against 99.7). The `+noliga` twin, declared `encoding-mode="single-byte"` so that FOP applies
no GSUB to a run Word would not ligate, is embedded whole: FOP's PDF writer copies a
single-byte TrueType file as it is (`PDFFactory.makeFontFile`, TrueType branch; only the
Type 0, Type 1 and CFF branches consult the embedding mode), where the CID declaration is
subset to the glyphs used. Jason's question of 2026-09-30, relayed by the fork session, which
re-measured it (one line of Arimo: 8,451 bytes subset, 191,915 whole).

**What changes.** Where the renderer declares `gsub-features` (the fork from 2.11-docx4j.2),
`RunFontSelector.noLigatures` sends no span to the twin: every run that asks for no standard
ligatures gets `fox:gsub-features="-liga"` on the CID declaration, Latin text included. The
twin stays declared (`FopConfigUtil`; a declaration costs nothing until used) and stays the
path on Apache FOP, which has no hook. `docx4j.convert.out.fo.gsubFeatures=false` is the way
back to the twin on the fork. Prerequisites, both now in the merged 2.11-docx4j.2: fop/CR-003
(the kerning flag: the base declaration is `kerning="false"`, so a plain run on the CID path
gets no GPOS kern, which is what the twin's "no GPOS" gave) and fop/CR-005 (letter-spaced
words on the position-adjustments path).

**What the CID path applies that the twin did not.** GSUB `ccmp` and `locl`, GPOS `mark` and
`mkmk` (FOP's GPOS list is kern, mark, mkmk; no `cpsp`, measured on the CR-003 gate). Word
applies all four, so on the affected text the CID path is nearer Word: combining marks are
positioned, localised forms (Romanian comma-below, Turkish, Dutch IJ) are used. Plain ASCII
Latin text, which is most of the twin's traffic, has no such glyph and must not move.
Kerning: none on either path for a plain run; `+kern` runs never took the twin.

**Gate.** Candidate `notwin-cand` against `cr003b-cand` (same docx4j build, the merged fork
jars), read with `gatecmp.py`: documents with no `+noliga` span in the baseline FO are the
still set (word-box identical); those with one are the predicted movers, expected identical
unless their twin text holds a combining mark or a locl-sensitive language, in which case
the movement is read glyph by glyph; scoreboards no worse; text layer identical (the word
text of the box compare). Then the size: whole-PDF bytes per corpus against Word, and one
document's `pdffonts` before and after (every twin gone, every face subset). export-fo's
tests on both renderers; `GsubFeaturesTest` says the twin is never taken with the hook, and
`EmbeddedFontMetricsTest` accepts either declaration (it will see the delta once export-fo
depends on 2.11-docx4j.2).

**Not in this step.** Dropping the twin declarations altogether (a later tidy-up, once no
supported renderer needs them); the `+kern` twin (a CID declaration already, subset); the
CFF faces (never twinned).

**Result (2026-09-30): done, gate passed.** `RunFontSelector.noLigatures` takes no twin where
the hook is on; `docx4j.convert.out.fo.noligaTwin=true` asks it back with the hook still on
for the spans it never reached (`gsubFeatures=false` switches the hook off altogether, which
is a different state: it also drops the delta from the CFF faces and the kerned runs, so a
"twin" baseline rendered that way moved 26 scoreboards for reasons that are not the twin's).
Sizes, whole PDFs: real 181.5 MB to 11.8, real2 200.7 to 19.3, real3 208.6 to 75.0, against
Word's 39.1 / 50.9 / 99.7; every face in the PDF is now a CID subset (13872: the same six
faces, all subset, 79 KB either way, since its runs were all kerned and never took the twin).

The gate, `twin2-base` (hook on, twin forced, same build) against `notwin2-cand`, 598
documents, read glyph by glyph with `glyphcmp.py` (mutool trace, (unicode, x, y) per glyph;
the word-box reader was misled here, because a CID font's PDF descriptor carries the bounding
box's ascent where the single-byte one carries the lower-case ascent, which moves every
pdftotext yMin without moving a baseline):
- 400 documents glyph-identical (the 10 with no twin span, and 390 of the 587 with one).
- 49 moved, all by 1/1000 em on glyphs outside WinAnsi (İ, ı, ł, ś, ć, ą...): the twin's
  widths for those came from FOP's additional 8-bit encodings, which `WordGlyphWidths` never
  corrected, so they were the truncated advance; the CID path has Word's rounding for every
  glyph. Two Turkish documents re-break a line on it (248: 1.000 to 0.962; 4571: 0.9996 to
  0.9981), with Word's own widths; accepted.
- 136 text-layer changes, nearly all improvements: 7,726 spaces that were extracted as
  U+0020 are U+00A0 now (the non-breaking spaces they are); symbol and emoji glyphs extract
  as themselves rather than "#" (fonts-symbol-and-emoji: "🗹"); Vietnamese letters the
  chained encodings mis-mapped (đ as "¶", Đ as "W") extract right; ";" for the Greek
  question mark and "∙" for the bullet operator are the glyphs' own code points. One loss,
  recorded as a fork item: the CID path drops Unicode format characters from the text layer
  (`MultiByteFont.performSubstitution` strips controls): 66 right-to-left marks, 19 U+206A
  and 1 left-to-right mark across four documents (12013: 1.000 to 0.863 on the scoreboard,
  which counts the U+206A Word keeps as words; the ink is the same, Tinos's glyph for it is
  zero-width either way, where Word draws Times New Roman's 0.75 em glyph, an older gap).
- 12 count changes: the format characters above, and a trailing space at a re-broken line.
- Scoreboards: 9 improved (+0.0967), 6 worse (-0.1826), each read above (12013 and 2600
  the format characters, 248 and 4571 the widths, 5041 -0.0004 and 7235 -0.0011 the same).
- Probes: 146 identical, fonts-symbol-and-emoji and spacing-empty-before text-layer only.

**Found on the way, fixed first: the dominant-run tie.** The first candidate moved
16_en-US_fields1_num_tbl_3115 by 4pt from an empty paragraph: two one-space runs, 11pt Arial
and 14pt Cambria, weigh the same in `XsltFOFunctions.applyBlockLineHeight`, and the winner
fell to `HashMap` order of a key holding the family name, which the retired suffix changed.
Word takes the first run (golden: 37.3pt from the previous line's top to the next heading's,
as the 11pt line gives). Now `LinkedHashMap`, so document order breaks a tie, and text
outweighs an empty inline (`DominantRunTieTest`). On its own, against the 27th's twin
render, the tie-break moves 9 documents' lines (8371 4,738 glyphs, 2065 2,511, 7412 161,
the rest under 60) and no scoreboard at all; every one is a one-character paragraph that
had been sized by hash order.

Carried: the twin declarations (still in every FOP config, unused on the fork); the format
characters in the text layer (fork `fop/CR-007`, gated here 2026-10-01: geometry identical,
the U+206A Word keeps restored, but Word writes no bidi marks to its text layer, so the
branch narrows before it merges; Enterprise §6.6 item 34 has the reading); the
Cambria-Greek fallback and the kernSpaces GPOS pairs (above).

### Not done, carried

- Cambria's Greek: docx4j maps Cambria to Caladea, which covers Latin only, so Greek runs
  fall back to P052 with Palatino metrics against Word's Cambria (real3 8371: 13,042 spans).
  Needs a Greek-capable face with Cambria's metrics, or a per-script fallback that keeps the
  document font's advances. Found 2026-09-30 on the fop/CR-003 gate.
- kernSpaces (Enterprise §6.6 item 15) takes a space pair from the legacy kern table only;
  Arimo has GPOS pairs with the space and no kern table, so they are applied by nobody.
  Found 2026-09-30 by the fork session.
- Phase 2 cherry-picks P2-1 to P2-8 (above), one batch item each; then the
  first release, or the upstream merge first - Jason's call.
- The fork branch and its CI are pushed (origin/docx4j-2.11); the
  `metanorma` and `chunlin` remotes exist only in the local checkout.
- §6.6 items 24 (`span="all"`) and 25 (region indents) have no fork change yet;
  25 is a layout change in FOP's page-sequence code, to be met when a batch
  needs it.

## 9. Phase 2 classification (2026-09-19)

### 9.1 Method and numbers

Remotes in `../xmlgraphics-fop-plutext`: `upstream` (apache/xmlgraphics-fop
`main` at 2a8efc165, 2026-07-08), `metanorma` (metanorma/xmlgraphics-fop
`main` at 8662a90c8, tags v2.11.1-v2.11.5), `chunlin` (chunlinyao/fop
`yao-2_11` at a06151041, 2026-09-08). Ranges read with `git rev-list` and
`git diff`; issue titles from the GitHub API; every hunk of both net diffs
read (Metanorma's 8373 lines, Chunlin's 4204), not the commit subjects alone.

| Fork | Base | Commits over base | Net delta | Notes |
|---|---|---|---|---|
| Metanorma `main` | upstream `main` at 32c8c7176 (2026-05-22), i.e. 75 upstream commits past the 2.11 tag merged, 14 not yet | 211 = 142 non-merge + 69 merges | 132 files, +5482 / -262 (2241 lines are the Unicode `Vertical_Orientation` table) | 16 features (§9.2); ~60 commits of build, CI, version-bump and revert churn |
| Chunlin `yao-2_11` | the `2_11` tag exactly | 7 (no merges) | 39 files, +2887 / -105 (README, four images and a sample PDF among them) | 3 features (§9.3); the origin of Metanorma's vertical writing and shrink-to-fit |
| Our `docx4j-2.11` | the `2_11` tag exactly | 7 (4 cherry-picks, set-up, groups, hooks) | - | 89 upstream commits behind `main` (§9.4) |

Verdicts: **TAKE** (a queued batch item, §8 P2-n), **LATER** (a candidate
once docx4j emits the construct, or once a probe exists), **UPSTREAM** (a
general fix to send to Apache; taken here meanwhile only where it moves a
docx4j document), **SUPERSEDED** (by our own fix or by an upstream commit
since 2.11), **SKIP** (Metanorma-specific, checker-specific, churn, or unsafe
as written). "Construct" names the docx4j input the change would serve.

### 9.2 Metanorma by feature

| Id | Feature | Commits (issue) | Size | Construct served | Risk / reading | Verdict |
|---|---|---|---|---|---|---|
| M1 | `fox:shrink-to-fit` on `fo:block-container`: bisection over a font scale (0.2..1, step 0.05, up to five relayouts) that rewrites `CommonFont` and `line-height` on every child `Block`, `InlineLevel` and `FOText` and recreates the child LMs | 9e2999537 (#3; Chunlin f25730a72, 231e5bfa2), style in 3a5b98da1 | 12 files, +327 | `wps:bodyPr/a:normAutofit` (Word's "shrink text on overflow") | `LengthRangeProperty.scale` mutates the property in place, and properties are cached and shared; the loop costs up to five layouts per box; hidden coupling of `fo.properties` to a layout manager class | LATER as a fallback only. Word stores the scale it last computed (`@fontScale`, `@lnSpcReduction`); docx4j emits the scaled font-size and line-height itself, no renderer change - the batch item is docx4j-side |
| M2 | Vertical writing: when the `LayoutContext` writing mode is vertical, words split at upright characters (a 2241-line `Vertical_Orientation` table), GSUB adds `vert`, each upright `WordArea` is rotated -90 degrees in `IFRenderer`; writing mode propagated through `offspringOf`, block stacking, tables, footnotes, static content, inline containers; page numbers and citations rotated per character | 7d3c1094b (#1; Chunlin 6437317f9), 51f5c065b, b26c29dbb (#37), 1958ca013 (#10), 19feafc01, c4b6b8322, 0a2ea2906, faaf0b905; examples 1d9040676, 35c9266e2, 238f30130 | 35 files, ~2700 lines | `w:textDirection tbRlV` (upright CJK in a rotated line) in cells, text boxes and frames; `tbRl` docx4j already renders with `reference-orientation` | HIGH. (a) Changes the signatures of `Substitutable`, `Positionable`, `Font`, `LazyFont`, `MultiByteFont`, `GlyphMapping.doGlyphMapping` and both `GlyphMapping` constructors with no Apache overload kept: any consumer compiled against Apache breaks (docx4j-export-fo reads `GlyphMapping` through the `inline-access` hook and would still compile, `WordWidthsLazyFont` overrides none of these). (b) The `IFRenderer` word-start and `letterSpacesIPD` change applies to every letter-spaced word, vertical or not (CR-001 §6.6 item 16 ground). (c) It is a simulation on the horizontal line model: horizontal advances stand in for vertical ("TODO Vertical text use vertical width"), no `vmtx`, no breaking in the BPD; open Metanorma issue #36 (regions before/after misplaced). (d) Rides on the language gate M10e | LATER: a `tbRlV` CJK probe first; then a rework that keeps the Apache signatures, confines the renderer change to upright words and drops the language gate. Not upstreamable as is |
| M3 | Japanese numerals: `format="&#x4E00;"` on page numbers via a `JapaneseNumeralsFormatter` (ichi, juu, hyaku, sen, man, oku, chou) | f7c5e66da (#7), df18aa260, 18da9635a, test e3d6bd5ad | 2 files, +179 | `w:pgNumType/@fmt="japaneseCounting"` (page fields); list labels are docx4j's own formatter | Low, additive; the formatter is a third-party class (SuuKotoba, MIT) with no licence header | TAKE later (P2-6) with docx4j's page-format mapping; UPSTREAM |
| M4 | `fox:actual-text` on `fo:instream-foreign-object` and `fo:external-graphic`, written as `/ActualText` on the Figure | 61c2bfe6f (#11), test 127fc1db2 | 6 files, +32 | OMML math as SVG (CR math-in-PDF): the linearised expression as the figure's actual text | Low, additive | TAKE (P2-5); UPSTREAM |
| M5 | `fox:placement` on graphics, written as `/Placement` on the Figure | 1ebe06166 (#102), test bb99ae5e3 | 6 files, +54 | `wp:inline` vs `wp:anchor` (Block / Inline placement for PDF/UA-2 checkers) | Low, additive | TAKE (P2-5); UPSTREAM |
| M6 | `fox:title` on `fo:block`, written as `/T` on a Sect | 0aa5d96ac (#67), test in PDFTagsTestCase | 7 files, +188 (156 test) | none: docx4j emits no Sect structure | - | SKIP |
| M7 | `role="Name/Type"`: a standard structure type with a custom tag name, via `StructureType.setTagType` | 5196a6352 (#73), test 7d9f02e06 | 4 files, +31 | none | Unsafe: `StandardStructureTypes` are static singletons, so the last role seen sets the tag type process-wide | SKIP |
| M8a | PDF/UA structure tree: `/ID` on Note, `Scope` on TH and table-tag checking, a Div inside P dropped, `role="SKIP"` on blocks and block-containers (`CommonAccessibility` added to `BlockContainer`), `isPDFA1Safe` no longer forcing Div for table parts under PDF/UA | 8197eb520 (#27), f28e8a4d8 (#28), 2e903f5ac (#32), efd43b7f4 (#33), ab162c363 (#96), ab8e6b9a3, dbe8a41f9, 576aea674 | ~6 files, ~90 | Footnotes (Note) and `w:tblHeader` rows (TH) in accessible output | Note `/ID` from `java.util.Random` (non-reproducible PDFs); the ancestor walk is heuristic and swallows exceptions with `System.out.println("")`; upstream FOP-3165 and FOP-3283 have since changed the same table code | Note `/ID` and TH `Scope`: UPSTREAM after diffing against FOP-3165/3283 (P2-5); the heuristics: SKIP |
| M8b | Link annotations: `/Contents` from the URI (`mailto:` becomes "Email ...") or from the GoTo target's `Alt`; `NoZoom`/`NoRotate` flags dropped under PDF/A | 4f6fb14f6 (#25), eda8ce786, 0cc51a9bb, 1c367304b, 019b5324c, 49a084a9e, a16c8acfd (#72), test 57801b39f | ~5 files, ~90 | Hyperlinks in accessible output | Upstream FOP-3258 and FOP-3322 (2026-05) write `fox:alt-text` on the `fo:basic-link` to `/Contents`, encrypted when needed - a cleaner contract | SUPERSEDED: docx4j emits `fox:alt-text` on links (tooltip or text); flags: SKIP pending a checker report |
| M8c | Navigation: a new `GoToXYAction` per link instead of one per target (FOP-3305; repeated links to one destination each get their own struct elem), named-destination page index, bookmarks without an action serialised, IF encoding setter, `Root/Names/Dests/Limits` removed | a92039606 (#75), 504135708 (#74), f98aab1d4 (#76), a877dbff4 (#30), e74f0a06b (#100), c2871ec43 (part), tests 230d6ae32, df3aaa224, 55c88aa4a, c8ddcbc68 | ~5 files, ~60 | Cross-references and TOC entries to the same bookmark; docx4j's bookmark tree | Per-link actions cost memory on long TOCs; the others are IF-only or checker-specific | Per-link action: UPSTREAM (FOP-3305 is open), TAKE if PAC flags docx4j output (P2-5); the rest SKIP |
| M9 | Attachments: `pdf:embedded-file` gains `afrelationship`, `volatile`, `link-as-file-annotation`; a `/FileAttachment` annotation (paperclip) in place of the JavaScript launch; `/UF`; `/Contents` in UTF-16BE | 7911d245b (#24), d2aeff3b0, 002a4550f (#71), tests 7393e4ff8, adbea6eae, 27b3c1d93 | 8 files, +281 | Embedded OLE and package parts (`w:object`, `word/embeddings/*`) carried in the PDF, PDF/A-3 style | The UTF-16BE switch string-matches `/FileAttachment` inside `PDFDocument.encode`; the description is not escaped; `PDFFileSpec` carries a String flag | TAKE later, rewritten (P2-7); `AFRelationship` UPSTREAM |
| M10a | Shared-glyph reverse mapping: `findCharacterFromGlyphIndex` prefers the original character when a glyph serves several code points (the FOP-2529 patch) | d6ddded83 (#22) | 1 file, +8 | Kangxi radicals, U+202F vs U+2009 - our §6.6 item 26 | Same bug as our phase 0 cherry-pick d3dbe8ad9, fixed differently | SUPERSEDED by ours; the item-26 upstream PR goes on JIRA FOP-2529 |
| M10b | U+061C (Arabic letter mark) falls back to U+200B when the font has no glyph | 4643411e1 (#34) | 1 file, +7 | Arabic documents (Word writes ALM) | Tiny | TAKE (P2-4); UPSTREAM |
| M10c | U+200B is not turned into a word space in accessibility mode (`GlyphMapping.isZeroWidthSpace`) | baec92a9d (#20) | 2 files, +24 | Zero-width spaces pasted from the web, in accessible output | Accessibility mode only | TAKE (P2-4); UPSTREAM |
| M10d | Surrogate pairs: no word split between a high and its low surrogate at a bidi-level change or a per-character font selection; `CharUtilities.isSurrogatePair` bound off by one | 70a1e75d5, ba2ec2ea4 (#39) | 2 files, +5 | Emoji, Extension-B ideographs ("isolated high surrogate" failures) | Tiny, correct | TAKE (P2-1); UPSTREAM |
| M10e | Language gate: `xml:lang="ar"` mapped to `dflt` before lookup matching; GSUB substitution only when the language is `ar` or `dflt` (Metanorma wanted no Latin ligatures) | d5e38ea91 (#19), 618f396f3 (#14), c9e2487f2 (#8), f64cff9c7 (#98) | 2 files, ~6 | none today: docx4j emits no `language`, so FOP's language is already `dflt` | Wrong in general: a BCP-47 tag is not an OpenType language-system tag, and the gate would silence GSUB for any tagged language the day docx4j emits `language` (the deferred idea in `docs/PDF_FOP_Accessibility.md`); FOP's own fallback is `(DFLT, dflt)`, which loses script-specific lookups | SKIP; the ligature question is P2-8, done as a feature-list hook |
| M11 | Messages: default page size INFO commented out; "coverage set class table" WARN commented out; a line-overflow event skipped for `__internal_layout__` ids and a table-overflow message key (its formatter reverted in 241925314); unresolved id references reported per page at render time; every missing glyph logged with its character sequence (the cap of 8 removed); a String-property warning special-cased for `fox:alt-text`; a static `currentPage` in `EventProducingFilter`; font-replacement info (#70) added then restored | 159ea69c4, bafb8efb2 (#12), 7211794f7, 4276335f4 (#15, #16), 241925314, 04e4e40ad (#13), 0da70aa3b, a8cdbcca6 (#21), f9a1eab2b (#23), b0c71631b (#17), ecc6c78eb, 16d92b649, 5f7beb390, 62772d12b (#70) | ~8 files | Harness diagnostics; docx4j's own font reporting (CR-016, CR-017) covers the glyph side | The static is process-wide state in a library; the uncapped glyph log can flood on a document with many missing glyphs | 04e4e40ad: TAKE (P2-3), UPSTREAM; b0c71631b: UPSTREAM as a parser bug (docx4j escapes); the rest SKIP |
| M12 | `XMLObj`: a prefixed attribute on foreign XML whose namespace declaration sits on an ancestor keeps its namespace (was set with a null URI) | d4a2d295b (#18) | 1 file, +5 | `xlink:href` on our SVG (CR-011 metafiles, math), MathML | Tiny | TAKE (P2-2); UPSTREAM |
| M13 | `VersionController`: setting a PDF version lower than or equal to a fixed one no longer throws (only raising it does) | fe2bea219 (#29), tests 9fee3a949, 611177343, c2871ec43 (part) | 1 file, +2 | A user setting a PDF version with a PDF/A mode on | Behavioural | UPSTREAM; SKIP until a docx4j report |
| M14 | `/ProcSet` no longer written to page resources | 7ee55ef2e (#26), test ed6454023, c2871ec43 (part) | 1 file | none (deprecated in PDF 2.0, harmless either way) | - | SKIP |
| M15 | Invisible text: an alpha-zero text colour renders with text mode `3 Tr` (searchable, not painted) | bd8d75bdb, 4b7a41192 (#31) | 1 file, +20 | The text layer over SVG math (CR math-in-PDF) | Low, self-contained | TAKE (P2-5); UPSTREAM |
| M16 | Build and release: `org.metanorma` coordinates, xmlgraphics-commons pin, GitHub Actions and release workflow, checkstyle skips, version bumps, "trigger" commits, "code reverted back to pass tests" (the May 2026 merge fix-ups), checkstyle and spotbugs passes | ~60 commits (listed in §9.5) | pom.xml x9, .github | none | - | SKIP (the style passes 3a5b98da1 and 6d14c68f7 ride with whichever feature is taken) |

### 9.3 Chunlin (`yao-2_11`, seven commits over the 2.11 tag)

| Commit | Date | Subject | Feature | Verdict |
|---|---|---|---|---|
| f25730a72 | 2017-07-12 | Add a quick implementation for shrink text to fit block-container | M1 (origin) | LATER, see M1 |
| 231e5bfa2 | 2017-08-02 | fox:shrink-to-fit support list-block | M1 | LATER, see M1 |
| 4c8e99edb | 2017-08-07 | FOP-2722 don't suppress space when no space left (`suppressibleLetterSpace && letterSpaceCount > 0`, so a word with no letter spaces is not given -1 of them) | letter spacing | SUPERSEDED: docx4j's `fixLetterSpaces` (WordLineLayoutManager) sets Word's count on both FOP paths; upstream FOP-2722 (2a8efc165, 2026-07-08) makes the complex-script path count letter spaces itself - both must be re-verified against the spacing probes at the upstream merge |
| 6437317f9 | 2020-01-07 | vertical writing mode for japanese | M2 (origin); also the `IFRenderer` word-start and `letterSpacesIPD` change that applies to all letter-spaced text | LATER, see M2 |
| 9b2fb2a8c | 2020-01-07 | add readme (Japanese; four images, sample PDF) | docs | SKIP |
| 621e2db8c | 2026-09-08 | Document yao-2_11 port and verification | docs (says FOP-3146/3148/3150 are in the 2.11 base; suite validated on JDK 8) | SKIP |
| a06151041 | 2026-09-08 | Test shrink-to-fit with list blocks | M1 test | with M1 |

Ruby: no renderer code. The `vertical_writing.fo` example builds ruby from
`fo:inline-container`s, which Apache FOP 2.11 renders; `w:ruby` is a docx4j
emission question, not a fork one.

### 9.4 The upstream backlog: `main` since the 2.11 tag (89 commits)

The 2.11 tag sits on a release branch two commits off `main` (64c46d1). Of
the 89 commits on `main` since, Metanorma has merged 75 (to 32c8c7176,
2026-05-22) and lacks 14. Grouped for docx4j; the JIRA number is the key.

| Group | Commits | Bearing on docx4j |
|---|---|---|
| Layout, CR-001 ground | FOP-3181 (page duplication when the IPD changes), FOP-3253 and FOP-3256 (rest page when the last page cannot fit), FOP-2880 (soft hyphen for hyphenation), GI-9484 (leader in a column too small), FOP-3306 x2 (dotted leader not rendered on PDF; rule leader as Artifact), FOP-3325 (spacing in a pattern rule leader), FOP-3327 (square leader style), FOP-3307 (image scaled to the cell IPD), FOP-3252 (table-and-caption), FOP-3279 (force-page-count doubly-odd/even), FOP-2763 (table markers), FOP-3282 and FOP-3316 (static-region-per-page) | Each of the first five can move a corpus document (sections, last pages, item 22, leaders); each needs its own gate when merged |
| Fonts and text | FOP-2722 (letter spaces counted in the complex-script path: collides with docx4j's `fixLetterSpaces`, see C3), FOP-3261 (GID remap on font merge), FOP-3292 (character max index), FOP-3277 (PDFTranscoder font substitution), FOP-3270 (country-specific hyphenation file), FOP-3332 x2 (classes verified when reading a HyphenationTree) | FOP-2722 on the spacing probes; FOP-3332 is a deserialisation hardening we want |
| Memory | FOP-3269 (structure tree collectable), FOP-3272 (accessibility memory), FOP-3280 x2 (MinOptMax, no-kerning fonts), FOP-3333 x2 (traits deduplicated), FOP-3288 (AFP font cache), FOP-3291 and FOP-3293 (image caching) | Issue 687 (OOM) territory; measure with the retention test from FOP-3330 |
| Accessibility | FOP-3165 (table structure under PDF/UA), FOP-3283 (header spanning rows), FOP-3273 (tagging stopped after the first page-sequence - docx4j emits one per section), FOP-3264 (footnote reference type), FOP-3245 (structure tree merging into external documents), FOP-3258 and FOP-3322 (alt text to `/Contents`, encrypted), FOP-3122 (bookmarks copied) | FOP-3273 alone justifies the merge for accessible output; FOP-3165/3283 overlap M8a; FOP-3258/3322 supersede M8b |
| Security and dependencies | FOP-3298 (no DTD from an SVG font), FOP-3302 (servlet secure processing), FOP-3284 x2 (temp file rights), FOP-3299 (signing digest), FOP-3308/bouncycastle 1.81, 1.84, 1.85, FOP-3281 x3 (checkstyle), branch-protection and CI commits | Take as they come |
| Other output formats and API | FOP-3290 (basic links may use URIs), FOP-3304 (custom schemas), FOP-3309 (first metadata only), FOP-3323 (fo:title NPE), FOP-3321, FOP-3311, FOP-3326, FOP-3278 (PostScript), FOP-3268, FOP-3287 (AFP), FOP-2872 (SVG em units), FOP-2758 (custom URI schemes), FOP-3251 (MD5 for names), FOP-3257 (NPE), commons-io in the transcoder, spotbugs and static-class tidying | No bearing; they come with the merge |

Recommendation (§8): the first release stays on the 2.11 base; the merge of
`main` is phase 4's first act, gated like a batch (corpora identical or every
mover explained, the spacing probes for FOP-2722, a sectioned document for
FOP-3181, a PAC run for FOP-3273), straight after the release - or before it
if Jason prefers one gate to two.

### 9.5 Metanorma commit by commit (142 non-merge, oldest first)

Feature ids are §9.2's; the verdict is the feature's unless the row says
otherwise. Tests and examples carry their feature's verdict.

| Commit | Date | Subject | Feature | Verdict |
|---|---|---|---|---|
| 9e2999537 | 2024-11-12 | extension fox:shrink-to-fit from chunlinyao/fop added, #3 | M1 | LATER (docx4j-side first) |
| 7d3c1094b | 2024-11-12 | vertical layout feature from chunlinyao/fop added, #1 | M2 | LATER |
| 1d9040676 | 2024-11-12 | example vertical_writing.fo updated, #2 | M2 example | LATER |
| f7c5e66da | 2024-11-12 | page numbers in Japanese feature added, #7 | M3 | TAKE P2-6 |
| 35c9266e2 | 2024-11-13 | example vertical_writing.fo updated for page numbers, #2 | M2 example | LATER |
| 61c2bfe6f | 2024-11-16 | extension fox:actual-text migrated, #11 | M4 | TAKE P2-5 |
| 159ea69c4 | 2024-11-16 | omit message about default page width height, #12 | M11 | SKIP |
| bafb8efb2 | 2024-11-16 | omit message coverage set class table not yet supported, #12 | M11 | SKIP |
| 7211794f7 | 2024-11-16 | fix warning about overflowing table, #12, #15 | M11 | SKIP |
| 04e4e40ad | 2024-11-16 | restore message Page NN: Unresolved ID reference, #13 | M11 | TAKE P2-3, UPSTREAM |
| b0c71631b | 2024-11-17 | fixing issue with quotes, #17 | M11 | UPSTREAM (parser bug); not taken |
| a8cdbcca6 | 2024-11-17 | determine page and text position for glyph warning, #21 | M11 | SKIP |
| f9a1eab2b | 2024-11-17 | glyph count restriction removed in log, #23 | M11 | SKIP |
| 4276335f4 | 2024-11-17 | localize place of overflowing, #16, #15 | M11 | SKIP (reverted in 241925314) |
| d4a2d295b | 2024-11-17 | jEuclid namespace processing fixed, #18 | M12 | TAKE P2-2, UPSTREAM |
| fe2bea219 | 2024-11-17 | updated for PDF version changing, #29 | M13 | UPSTREAM; not taken |
| 7ee55ef2e | 2024-11-17 | ProcSet array resources generation commented, #26 | M14 | SKIP |
| 618f396f3 | 2024-11-17 | ligatures ignoring, #14 | M10e | SKIP |
| d5e38ea91 | 2024-11-17 | fixing issue with Arabic glyphs, #19 | M10e | SKIP |
| baec92a9d | 2024-11-17 | removing space instead of zero width space, #20 | M10c | TAKE P2-4, UPSTREAM |
| d6ddded83 | 2024-11-17 | patch from FOP-2529 applied, #22 | M10a | SUPERSEDED (item 26; JIRA FOP-2529) |
| a877dbff4 | 2024-11-17 | changed IF writing for surrogate pairs, #30 | M8c | SKIP (IF output only) |
| bd8d75bdb | 2024-11-17 | set transparent mode 3 Tr for invisible text, #31 | M15 | TAKE P2-5, UPSTREAM |
| 4643411e1 | 2024-11-17 | fixing Arabic text marker, #34 | M10b | TAKE P2-4, UPSTREAM |
| 7911d245b | 2024-11-18 | PDF attachment annotation added, #24 | M9 | LATER P2-7 (rewritten) |
| 4f6fb14f6 | 2024-11-18 | Contents key added, #25 | M8b | SUPERSEDED (FOP-3258, FOP-3322) |
| 8197eb520 | 2024-11-18 | ID entry added for Note tag, #27 | M8a | UPSTREAM, P2-5 |
| f28e8a4d8 | 2024-11-18 | updated for Table tags structure, #28 | M8a | UPSTREAM, P2-5 (after FOP-3165/3283) |
| 2e903f5ac | 2024-11-18 | updated for table tags checking, #32 | M8a | SKIP |
| efd43b7f4 | 2024-11-18 | PDF structure tags updated, #33 | M8a | SKIP (heuristics) |
| 51f5c065b | 2024-12-05 | context inherits writing mode from parent | M2 | LATER |
| 238f30130 | 2025-01-16 | vertical_writing.fo sample updated for #37 | M2 example | LATER |
| b26c29dbb | 2025-01-17 | FootnoteBodyLayoutManager updated for writing mode, #37 | M2 | LATER |
| 70a1e75d5 | 2025-01-20 | fixing issue with surrogate pairs, #39 | M10d | TAKE P2-1, UPSTREAM |
| 0a2ea2906 | 2025-01-20 | fixing issue with surrogate pairs, #39 | M2 table | LATER |
| 1958ca013 | 2025-01-31 | rotated japanese page numbers | M2 | LATER |
| 19feafc01 | 2025-06-05 | added a rotate-japanese-text test | M2 test | LATER |
| c4b6b8322 | 2025-06-06 | Character.isWhitespace() instead of expression | M2 test | LATER |
| 35073d814 | 2026-04-18 | updated for test pass, #47 | M16 | SKIP (test churn) |
| bd312b882 | 2026-04-18 | maven.yml updated for skip checkstyle, #47 | M16 | SKIP |
| e3d6bd5ad | 2026-04-19 | added test for page numbers in Japanese, #47 | M3 test | TAKE P2-6 |
| 0eea5c22a | 2026-04-19 | maven.yml updated for skip checkstyle, #47 | M16 | SKIP |
| df18aa260 | 2026-04-19 | JapaneseToNumbers.java updated to pass spotbugs test, #47 | M3 | TAKE P2-6 |
| 05c6d29f8 | 2026-04-19 | updated for test pass, #47 | M16 | SKIP |
| 18da9635a | 2026-04-19 | exclusions.xml updated for class JapaneseNumeralsFormatter. #47 | M3 | TAKE P2-6 |
| fcbd58c07 | 2026-04-19 | updated for test pass, #47 | M16 | SKIP |
| 127fc1db2 | 2026-04-21 | added test for actual-text extension, #47 | M4 test | TAKE P2-5 |
| 6f9ce3897 | 2026-04-21 | updated for test pass, #47 | M16 | SKIP |
| 0da70aa3b | 2026-04-22 | EventProcessing tests updated for unresolvedIDReferenceOnPage, #47 | M11 test | TAKE P2-3 |
| ff4974e1f | 2026-04-23 | updated for test pass, #47 | M16 | SKIP |
| e0d262a64 | 2026-04-23 | pom.xml for fop-events updated, #47 | M16 | SKIP |
| 241925314 | 2026-04-23 | EventFormatter.java restored to original, #47 | M11 | SKIP (revert of 4276335f4) |
| ed6454023 | 2026-04-24 | Test added for omit ProcSet, #47, #26 | M14 test | SKIP |
| 9fee3a949 | 2026-04-24 | Test updated for set PDF version, #47, #29 | M13 test | not taken |
| 611177343 | 2026-04-24 | Test updated for set PDF version, #47, #29 | M13 test | not taken |
| 4b7a41192 | 2026-04-26 | PDF painter updated for math hidden text, #47, #31 | M15 | TAKE P2-5 |
| 2e2d7b1d6 | 2026-04-26 | updated for test pass, #47 | M16 | SKIP |
| eda8ce786 | 2026-05-10 | updated | M8b | SUPERSEDED |
| 7393e4ff8 | 2026-05-10 | Test added for FileAttachment annotation, #47, #24 | M9 test | LATER P2-7 |
| b7cd8b04f | 2026-05-10 | maven.yml updated for skip checkstyle, #47 | M16 | SKIP |
| 0cc51a9bb | 2026-05-10 | PDFLink updated to fix conflict, #47 | M8b | SUPERSEDED |
| 728ab1848 | 2026-05-10 | pom.xml updated, #47 | M16 | SKIP |
| dbe0d8bcb | 2026-05-13 | trigger | M16 | SKIP (empty) |
| 4c6ed58e0 | 2026-05-13 | trigger | M16 | SKIP (empty) |
| 0aa5d96ac | 2026-05-14 | added /T for Sect, #67, #47 | M6 | SKIP |
| 16d92b649 | 2026-05-14 | FontInfo updated for #70 | M11 | SKIP (restored in 62772d12b) |
| 5f7beb390 | 2026-05-14 | FontInfo updated for font replacement info, #70 | M11 | SKIP (restored in 62772d12b) |
| ba2ec2ea4 | 2026-05-14 | TextLayoutManager updated for surrogate pairs issue fix, #39 | M10d | TAKE P2-1, UPSTREAM |
| d2aeff3b0 | 2026-05-14 | PDFDocument updated for /Contents in UTF-16BE, #71 | M9 | LATER P2-7 (not as written) |
| a16c8acfd | 2026-05-14 | PDFLink updated for flags, #72 | M8b | SKIP (flags) |
| 5196a6352 | 2026-05-14 | Added tag type, #73 | M7 | SKIP (static singletons mutated) |
| 504135708 | 2026-05-14 | DocumentNavigationHandler updated to fix wrong named destination, #74 | M8c | SKIP |
| ecc6c78eb | 2026-05-14 | EventProducingFilter updated for #70 | M11 | SKIP (process-wide static) |
| a92039606 | 2026-05-14 | IFRenderer updated for #75 | M8c | UPSTREAM (FOP-3305), P2-5 if PAC flags it |
| f98aab1d4 | 2026-05-14 | IFSerializer updated to fix #76 | M8c | SKIP |
| 002a4550f | 2026-05-17 | PDFDocument updated for /Contents in UTF-16BE, #71 | M9 | LATER P2-7 (not as written) |
| adbea6eae | 2026-05-17 | PDFAttachmentTestCase updated for /Contents in UTF-16BE, #71 | M9 test | LATER P2-7 |
| 57801b39f | 2026-05-18 | Test added for Annotation flags (Ff) for Link, #47, #72 | M8b test | SKIP |
| 7d9f02e06 | 2026-05-18 | Test added for Tag type, #47, #73 | M7 test | SKIP |
| 230d6ae32 | 2026-05-18 | Test added for named destination, #47, #74 | M8c test | SKIP |
| df3aaa224 | 2026-05-19 | Test added for repeated internal-destination links tags, #47, #75 | M8c test | with a92039606 |
| 55c88aa4a | 2026-05-20 | Test added for empty bookmarks in IF, #47, #76 | M8c test | SKIP |
| c8ddcbc68 | 2026-05-20 | Test updated for named destination, #47, #74 | M8c test | SKIP |
| e633bce76 | 2026-05-20 | pom.xml updated, #47 | M16 | SKIP |
| e290103fa | 2026-05-20 | pom.xml updated, #47 | M16 | SKIP |
| ab49a1254 | 2026-05-20 | code reverted back to pass tests, #47 | M16 | SKIP |
| a7eb9d819 | 2026-05-20 | maven.yml updated for skip checkstyle, #47 | M16 | SKIP |
| 41b482807 | 2026-05-20 | code reverted back to pass tests, #47 | M16 | SKIP |
| 502ea8466 | 2026-05-20 | code reverted back to pass tests, #47 | M16 | SKIP |
| 06f17e5ac | 2026-05-20 | code reverted back to pass tests, #47 | M16 | SKIP |
| a4e8acfec | 2026-05-20 | code reverted back to pass tests, #47 | M16 | SKIP |
| 79bd209e9 | 2026-05-20 | code reverted back to pass tests, #47 | M16 | SKIP |
| 27b3c1d93 | 2026-05-20 | PDFAttachmentTestCase updated for /Contents in UTF-16BE, #71 | M9 test | LATER P2-7 |
| d419c0ea8 | 2026-05-21 | code reverted back to pass tests, #47 | M16 | SKIP |
| 576aea674 | 2026-05-21 | PDFTagsTestCase code clean , #47 | M8a test | with M8a |
| bf05b39b6 | 2026-05-21 | maven.yml updated for skip checkstyle, #47 | M16 | SKIP |
| b6da95374 | 2026-05-21 | code reverted back to pass tests, #47 | M16 | SKIP |
| e92d30365 | 2026-05-21 | code reverted back to pass tests, #47 | M16 | SKIP |
| 61c581866 | 2026-05-21 | code reverted back to pass tests, #47 | M16 | SKIP |
| 62772d12b | 2026-05-21 | FontInfo restored to the original, #70 | M11 | SKIP (restore) |
| 0ab4f63d0 | 2026-05-21 | code reverted back to pass tests, #47 | M16 | SKIP |
| 1d7506f35 | 2026-05-21 | code reverted back to pass tests, #47 | M16 | SKIP |
| 16ec75d7c | 2026-05-21 | code reverted back to pass tests, #47 | M16 | SKIP |
| 790dd4419 | 2026-05-22 | code reverted back to pass tests, #47 | M16 | SKIP |
| 14375f4ea | 2026-05-22 | code reverted back to pass tests, #47 | M16 | SKIP |
| eb9d2f071 | 2026-05-22 | maven.yml updated for skip checkstyle, #47 | M16 | SKIP |
| 6e4df4b66 | 2026-05-22 | code reverted back to pass tests, #47 | M16 | SKIP |
| e8e6a7753 | 2026-05-22 | GlyphSubstitutionTable and GlyphMapping updated after merge for test pass, #47 | M2 (merge fix-up) | LATER |
| c2871ec43 | 2026-05-22 | updated for #29, #47 | M12, M13, M14, M8c re-applied after the merge | per feature |
| 0a2967f1f | 2026-05-22 | code reverted back to pass tests, #47 | M16 | SKIP |
| 55e08f401 | 2026-05-22 | code correction, #47 | M8b, M1 correction | per feature |
| c9e2487f2 | 2026-05-22 | GlyphSubstitutionTable updated for PDFVertTestCase pass, #8 | M10e | SKIP |
| c45f74544 | 2026-05-23 | code restored to the latest version, #47 | M16 | SKIP |
| 8038176e0 | 2026-05-24 | code restored to the latest version, #47 | M16 | SKIP |
| 208d3b3bf | 2026-05-24 | set xmlgraphics-common version to 2.11, #4 | M16 | SKIP (we pin 2.11 already) |
| ab8e6b9a3 | 2026-05-25 | code fixing after merge, #45 | M8a (merge fix-up) | with M8a |
| dbe8a41f9 | 2026-05-25 | PDFTagsTestCase fix, #45, #28 | M8a test | with M8a |
| fd20afbcc | 2026-05-25 | PDFObjectStreamTestCase updated, #45 | M16 test | SKIP |
| 1c367304b | 2026-05-26 | PDFLink and tests updated, #45, #25 | M8b | SUPERSEDED |
| 019b5324c | 2026-05-26 | PDFLink and tests updated, #45, #25 | M8b test | SUPERSEDED |
| c6f2b1fd9 | 2026-05-26 | publish-snapshot disabled, #45 | M16 | SKIP |
| 16d008d1c | 2026-05-26 | trigger | M16 | SKIP (empty) |
| f933ec782 | 2026-05-26 | trigger | M16 | SKIP (empty) |
| 3a5b98da1 | 2026-05-27 | updated for checkstyle approvement, #45 | M16 style pass over all features | with whichever feature is taken |
| 6d14c68f7 | 2026-05-27 | updated for spotbugs approvement, #45 | M16 spotbugs pass | with whichever feature is taken |
| 606b28f3b | 2026-05-28 | maven.yml updated, #94 | M16 | SKIP |
| 8cd8b12c3 | 2026-06-03 | pom.xml updated for groupid org.metanorma, #94 | M16 | SKIP |
| e5f3d73dd | 2026-06-03 | pom.xml updated for dist url, #94 | M16 | SKIP |
| 983fbd5f1 | 2026-06-03 | release.yml added, #94 | M16 | SKIP |
| a02265f49 | 2026-06-03 | Potential fix for pull request finding 'CodeQL / Workflow does not contain permissions' | M16 | SKIP |
| 0c651d182 | 2026-06-03 | release.yml fixed, #94 | M16 | SKIP |
| ab162c363 | 2026-06-09 | BlockContainer updated for role SKIP support, #96 | M8a | SKIP (role SKIP) |
| 6dc19738a | 2026-06-09 | bump version, #96 | M16 | SKIP |
| f64cff9c7 | 2026-06-10 | GlyphSubstitutionTable updated for #98 | M10e | SKIP |
| faaf0b905 | 2026-06-10 | PDFVertTestCase updated for #98 | M2 test | LATER |
| e74f0a06b | 2026-07-31 | Root/Names/Dests/Limits removed, #100, metanorma/metanorma-pdfa#70 | M8c | SKIP (checker-specific) |
| 90e4e11b1 | 2026-07-31 | version bump, #100, metanorma/metanorma-pdfa#70 | M16 | SKIP |
| 1ebe06166 | 2026-08-10 | added /Placement for Figure, #102 | M5 | TAKE P2-5, UPSTREAM |
| 0cd3621f0 | 2026-08-10 | version bump, #102 | M16 | SKIP |
| bb99ae5e3 | 2026-08-10 | tests added, #102 | M5 test | TAKE P2-5 |
| 49a084a9e | 2026-08-24 | PDFLink updated for linked text, metanorma/metanorma-pdfa#71 | M8b | SUPERSEDED |
| 1f379f975 | 2026-08-24 | version bump, metanorma/metanorma-pdfa#71 | M16 | SKIP |

The 69 merge commits (2024-11-12 to 2026-08-25) merge upstream `main` or
Metanorma's own feature branches (PRs #48-#93 above); nothing to cherry-pick,
since phase 4 takes upstream directly: 8662a90c8 67b0b4e01 e2505b840 5c2e40b3b
725611713 1394ba995 03cc51213 c19bc9382 f59f90dea f0c9be070 a419ce1be
460de64cb c9b745163 4499d2887 ccb2c408e 06a32c7cb 3dc073f7c beb918498
27970ad0b 4e6973b44 d4e02ab18 96a02fdcf cc02dacb6 834acc15b 6e7812e04
6dfa2724b 7b2377f4a ece7a0bc4 c00f26bf3 3f3b7e214 611c88583 5aacf10ab
235eec388 b75c9b6f0 946aad2f4 a31031bdd cddbdcb6e 91c1690bc 4093873cd
ba6b87aea a74661683 47d01bf57 5e9a1f105 241500e39 10fc50d36 29012637f
fa813698d c29bcfc2a eadd6758e bbd77fddd 2301b674c 48437f8a9 5c43e54fe
37a0c7e4c d45328268 5bd4f95ea 712562678 373c8a6b0 641d451b2 31bb22a8b
c8d107fc8 b764f5ca7 f7a3450d5 916177fc3 9784c43ac 0c65db9b3 4d5fc6703
345fa3c62 a156468d3.
