# CR-020: `docx4j-fo-renderer` — an upstream-tracking fork of Apache FOP, with graceful degradation to Apache's own

Status: PROPOSED (2026-09-18). The decision to fork, the naming and the
degradation contract were taken by Jason Harrop on 2026-09-18 (recorded in §2);
nothing coded. Drafted with Claude Fable 5.1 at the close of CR-001 batch 48.
Owner: Jason Harrop.

Scope: a fork of Apache FOP 2.11 maintained at `plutext/xmlgraphics-fop`,
published to Maven Central as `org.docx4j:docx4j-fo-renderer-*`; the switch of
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

- group `org.docx4j`; artifacts **`docx4j-fo-renderer`** (parent),
  **`docx4j-fo-renderer-core`**, and whichever of FOP's `fop-util` /
  `fop-events` the core needs, under the same prefix - no "fop" in an artifact
  id;
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
  the existing `org.docx4j` credentials, signed, with sources and javadoc.
- Every fork-only change carries a JIRA reference where one exists and is
  listed in the fork's README under "Changes from Apache FOP 2.11" with its
  §6.6 item number; a change without a JIRA is sent upstream first unless it
  is docx4j-specific (a hook FOP would not want).

### 3.2 The consumer: `docx4j-export-fo`

- The dependency becomes `org.docx4j:docx4j-fo-renderer-core` (the Apache
  `fop` artifact today at `docx4j-export-fo/pom.xml` line 259, with its
  exclusions carried over). Batik and xmlgraphics-commons stay as they are.
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

0. **Fork set-up and first release.** The `docx4j-2.11` branch, coordinates,
   NOTICE/README/change notices, CI; cherry-pick the existing branches
   (FOP-3328, FOP-3330, empty-glyph-not-composite, cjk-radical-tounicode,
   packed-glyph-bboxes) with their upstream status recorded; release
   `2.11-docx4j.1`. `docx4j-export-fo` switched, `FopCapabilities` in place
   with no hooks yet, the harness switch in. **Gate**: the corpora and probes
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
   no class 2 loss.
3. **The structural items** (Enterprise CR-001.6, PROPOSED): the column model
   and unequal columns, GDI rounding, text on both sides of an anchor and wrap
   around a page-positioned object (§6.6 items 8, 10, 11, 12), hyphenation as
   Word does it (22). Each gets its own design note in this CR when reached;
   these are the changes that would never be accepted upstream as they stand,
   and the reason a fork is worth more than a patch queue.
4. **Upstreaming, ongoing.** Every general fix goes to Apache as a PR with the
   JIRA text drafted for Jason to file (the FOP-3328 pattern); the fork's
   README lists what is still fork-only and why.

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
- **Maintenance** is a release every few weeks while phases 1-3 run, then at
  each Apache release: Metanorma's cadence, and less than today's reflection
  costs in batch time.
- **Open**: whether `fop-util` and `fop-events` are forked or consumed from
  Apache (they are unmodified; consuming them is simpler, forking keeps one
  version line) - decide in phase 0 from the build.

## 6. Suggested sequencing and effort (rough)

Phase 0 two to three days (the CI and the Central release are most of it);
phase 1 about a week across the three hooks with their gates; phase 2 one day
for the classification report, then variable, one batch item per cherry-pick;
phase 3 large and open-ended, planned item by item; phase 4 ongoing. Phase 0
should start after CR-001 batch 48's follow-ups are queued and before batch 49,
so that batch 49 is the first batch gated on both configurations.

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
