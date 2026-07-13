# Migration plan: bundled `com.topologi.diffx` → `org.pageseeder.diffx:pso-diffx`

Status: Phases 0-5 DONE (July 2026); the upstream Automatic-Module-Name request
is drafted in upstream-issue-automatic-module-name.md, awaiting filing.
Target dependency: `org.pageseeder.diffx:pso-diffx:1.3.4`
(Apache 2.0, Java 11+, actively maintained at https://github.com/pageseeder/diffx).

Phase 5 verdict (July 2026): KEEP the divide-and-conquer splitter and the
bundled org.eclipse.compare LCS.  Benchmark (WholeBodyDiffBenchmark in
docx4j-diffx-tests; 500 generated paragraphs, ~26k tokens/side, seeded):

| scenario            | splitter        | whole-body OptimisticXMLProcessor |
|---------------------|-----------------|-----------------------------------|
| identical           | 13 ms           | 7 ms                              |
| 5% paras edited     | 13 ms, 31 mk    | 6 ms, 57 mk                       |
| 20% paras edited    | 18 ms, 557 mk   | 6 ms, 219 mk                      |
| structural (10 del, 10 ins, 20 moved) | 13 ms, 2129 mk | 11,460 ms, 21,449 mk |
| 50% paras edited    | 21 ms, 2159 mk  | 8 ms, 479 mk                      |

(mk = ins/del markers in the raw output, a crude quality metric; whole-body
MatrixXMLAlgorithm refuses every non-identical scenario at this size -
DataLengthException, >64M comparisons.)

The whole-body optimistic processor is faster and produces cleaner diffs for
pure in-place word edits, but collapses on structural changes (paragraph
deletes/inserts/moves - the common case for document revisions): ~900x slower
and ~10x noisier than the splitter, because the Myers-greedy result breaks
XML well-formedness and triggers an expensive repair path.  The splitter is
uniformly fast (13-21 ms) at this document size.

Possible future tweaks (not pursued): use OptimisticXMLProcessor instead of
MatrixXMLAlgorithm for each changed range inside the splitter (would lift the
5000-token per-range blanket-delete/insert fallback); investigate why the
splitter's per-range pairing yields more markers than whole-body on heavy
in-place edit loads (pre-existing behaviour, not a migration regression).

Outcome notes (July 2026):
- All 40 paragraph-level golden files are byte-identical under the new
  implementation.  Key fidelity tricks: `DiffConfig.legacyDefault()` matches the
  old fork's effective config (namespace-aware, whitespace COMPARE, WORD
  granularity); `org.docx4j.diff.LegacyDiffOutput` replicates the patched
  SafeXMLFormatter/SmartXMLFormatter markup (upstream's legacy-namespace switch
  is package-private and unreachable, so we implement XMLDiffOutput ourselves);
  and the legacy algorithms' operator convention (first sequence = newer,
  insertions before deletions at a replacement) is reproduced by running the
  new algorithm un-swapped and flipping each operator via
  Docx4jDriver.flipped().
- The two body-level goldens were regenerated: content identical, but each
  top-level fragment now carries its own xmlns declarations, because the new
  XMLWriterNSImpl correctly forgets namespace scopes when elements close
  (the old writer's stale mappings meant ins:/del: attributes on fragments
  after the first were bound to the wrong namespace at parse time - a latent
  bug this migration fixes).
- The old 5000-token blanket-delete/insert fallback and openResult's
  hand-built root tag (including its ins->base-URI binding quirk) are
  preserved as-is.

## Background and feasibility

docx4j-diffx bundles a mid-2010 snapshot of Topologi Diff-X (`com.topologi.diffx.*`,
Artistic License 2.0) with docx4j-specific additions and tweaks. The same codebase was
renamed to `org.pageseeder.diffx`, rewritten, and is actively maintained as `pso-diffx`.

Reconnaissance findings that make this migration well-contained:

- Only **one class outside this module** imports `com.topologi.*`: the sample
  `docx4j-samples-docx-diffx/.../CompareDocuments.java` (uses `Docx4jDriver`).
  Everything else funnels through `org.docx4j.diff.Differencer`, which lives in
  this module. The effective external API surface is: `Differencer`,
  `Docx4jDriver.diff(Node, Node, Writer)`, `Main.diff(...)`, `DiffXConfig`.
- pso-diffx 1.x retains a **legacy-namespace mode** (`XMLDiffOutputBase.useLegacyNamespaces`,
  a protected field) emitting the exact `http://www.topologi.com/2005/Diff-X`
  (+`/Insert`, `/Delete`) URIs that the four XSLTs in
  `src/main/resources/org/docx4j/diff/` (`diffx2wml.xslt` etc.) are hard-wired to.
- Three of our four fork patches are obsolete upstream:
  - Secure XML parsing (our swaps to `org.docx4j.XmlUtils` / POI `XMLHelper` in
    `XMLHelper.java`, `DOMRecorder.java`, `DOMWriterImpl.java`, `Extension.java`):
    upstream hardened XXE/entity expansion in 1.3.2/1.3.4, and `Docx4jDriver.diff`
    receives already-parsed DOM `Node`s, so parsing happens on the docx4j side anyway.
  - Our custom `CommentEvent` → upstream `token.impl.XMLComment`.
  - Our patched `Extension` → upstream ships its own `org.pageseeder.diffx.Extension`; drop ours.
  - Remaining to verify: the namespace-handling bugfix from commit `c4b01e663`
    (`SmartXMLFormatter` / `XMLWriterNSImpl`) — confirm upstream's rewritten XML
    writer does not reintroduce it (Phase 3, item 4).

The one real risk: our code uses the **pre-2010 topologi API** (`DOMRecorder` /
`EventSequence` / `SmartXMLFormatter`), and every maintained pso-diffx release
(0.9.0+) has the rewritten pipeline API (`Loader` / `DiffAlgorithm` / `DiffHandler` /
`XMLDiffOutput`). pso-diffx 0.8.1 is a near drop-in namespace rename of what we have,
but it is a 2021 dead-end — targeting it would defeat the purpose. So `Docx4jDriver`
must be rewritten against the new API, and byte-level output compatibility with
`SmartXMLFormatter` must be proven, not assumed.

## API mapping (our fork → pso-diffx 1.3.4)

| Ours (`com.topologi.diffx`) | pso-diffx 1.3.4 (`org.pageseeder.diffx`) |
|---|---|
| `DOMRecorder` | `load.DOMLoader` |
| `sequence.EventSequence` | `xml.Sequence` (`.tokens()`) |
| `sequence.PrefixMapping` | `xml.NamespaceSet` / `xml.Namespace` |
| `sequence.SequenceSlicer` | `sequence.TokenListSlicer` |
| `event.DiffXEvent` (+impls) | `token.XMLToken` (+`token.impl.*`) |
| `algorithm.DiffXFitopsy` / `GuanoAlgorithm` | `core.DefaultXMLProcessor` / `OptimisticXMLProcessor`, or `algorithm.MyersGreedyXMLAlgorithm` |
| `format.DiffXFormatter` | `api.DiffHandler` |
| `format.SmartXMLFormatter` | `format.DefaultXMLDiffOutput` (subclass to set `useLegacyNamespaces = true`) |
| `config.DiffXConfig` (`setIgnoreWhiteSpace`/`setPreserveWhiteSpace`) | `config.DiffConfig` (fluent: `.whitespace(WhiteSpaceProcessing.…)`, `.granularity(TextGranularity.…)`) |
| `Main.diff(Node, Node, Writer, DiffXConfig)` | `Main.diff(Node, Node, Writer, DiffConfig)` — same shape |
| `event.impl.CommentEvent` (our addition) | `token.impl.XMLComment` |
| `Extension` (our patched copy) | upstream `Extension` — drop ours |

Carried over as-is: the `org.eclipse.compare.*` LCS range-differencer copy (EPL;
used by `Docx4jDriver`'s divide-and-conquer and `Differencer`'s word-level pre-pass)
and `org.docx4j.diff.Differencer` + the four XSLTs.

## Phases

### Phase 0 — safety net (do first, standalone)

- `docx4j-diffx-tests` is currently **not in the reactor**; re-add it to the parent
  pom `<modules>`.
- Build a golden-output corpus with the **current** code: for a set of paragraph/body
  pairs (start from `ParagraphDifferencerTest` resources plus a few real docs from
  `sample-docs/`), capture
  (a) the raw pre-XSLT diff XML that `Docx4jDriver.diff` writes, and
  (b) the final tracked-changes WML out of `Differencer.toWML`.
  Check these into the tests module. This is the contract the migration must hold.

### Phase 1 — swap the dependency

- Add `org.pageseeder.diffx:pso-diffx:1.3.4` to `docx4j-diffx/pom.xml`.
- Delete the entire `com/topologi` tree. Keep `org/eclipse/compare` and `org/docx4j/diff`.
- Move `Docx4jDriver` to `org.docx4j.diff.Docx4jDriver` (the `com.topologi` namespace
  goes away; the sample's one import updates trivially).

### Phase 2 — rewrite the glue (~3 classes; the real work)

- **`Docx4jDriver`**: same algorithm (top-level-children split → Eclipse LCS over
  per-child token-list hashes → full diff only on changed ranges; keep the 5000-token
  threshold, the hard-coded OOXML `xmlns` injection, and `openResult`/`closeResult`
  root handling), re-expressed via `DOMLoader` / `Sequence` / `TokenListSlicer` /
  processor / `DiffHandler`. `formatEventSequence` becomes "replay tokens into the
  handler as MATCH operations".
- **`EventSequenceComparator`** (in `org.eclipse.compare`): re-wrap `List<XMLToken>`
  instead of `EventSequence`. Upstream fixed token hashcode performance (their
  issue #1), so hash-based range comparison should get faster — but verify
  equality/hashCode semantics of `XMLToken` vs old `DiffXEvent`, since
  divide-and-conquer correctness rests on it.
- **`Differencer`**: swap `DiffXConfig` → `DiffConfig`, the `Main.diff` call, and the
  `CommentEvent` debug path → `XMLComment`. Its XSLT / `combineAdjacent` / rels
  machinery is untouched.

### Phase 3 — prove output compatibility

Create `org.docx4j.diff.LegacyDiffOutput extends DefaultXMLDiffOutput` setting
`useLegacyNamespaces = true`. Diff its output against the Phase-0 goldens.
Expected friction points, in likely order:

1. **Markup shape** — old `SmartXMLFormatter` vs `DefaultXMLDiffOutput` may differ in
   where `dfx:insert`/`dfx:delete` attributes vs `ins:`/`del:` wrappers land. If so,
   extend the subclass to match, or adjust the XSLTs (prefer the subclass — the XSLTs
   encode a lot of WML knowledge).
2. **Tokenization defaults** — new default granularity is `SPACE_WORD`, whitespace
   `COMPARE`; pick the `DiffConfig` that matches old behavior empirically against goldens.
3. **Attribute-diff representation** changed upstream — check `diffx2wml.xslt` handles it.
4. **Namespace declaration placement** — where our `c4b01e663` bugfix lived; confirm
   upstream's rewritten writer (plus 1.3.4's "expanded formatter capabilities") handles
   the `dfx:`/`del:`/`ins:` declarations on the root correctly.

Fallback if `DefaultXMLDiffOutput` differs too much even in legacy mode: write our own
`XMLDiffOutput` implementation replicating `SmartXMLFormatter` (~300 lines, against a
stable upstream `DiffHandler` API).

### Phase 4 — packaging and hygiene

- **JPMS**: pso-diffx 1.3.4 has no `module-info` and no `Automatic-Module-Name`
  manifest entry (nor does its one dependency,
  `org.pageseeder.xmlwriter:pso-xmlwriter:1.1.1`). They become automatic modules
  (`pso.diffx`, `pso.xmlwriter`) — workable but fragile; file an upstream issue asking
  for `Automatic-Module-Name`. Rewrite our `module-info.java`: drop the 13
  `com.topologi.*` exports, keep `org.docx4j.diff` + the `org.eclipse.compare*` packages.
- **Legals**: this removes Artistic License 2.0 code (the topologi fork — for which no
  `license.txt` is even present in the module today) in favor of an Apache 2.0
  dependency. Update `legals/NOTICE`; the EPL notice for the retained
  `org.eclipse.compare` copy stays.
- Update the commented-out OSGi block in `docx4j-diffx/pom.xml` (references
  `com.topologi.*` packages), the sample `CompareDocuments.java` import, and
  `CHANGELOG.md` (API note: `com.topologi.diffx.*` no longer shipped; `Docx4jDriver`
  moved to `org.docx4j.diff`).

### Phase 5 — optional follow-ups (separate release)

- The divide-and-conquer driver exists to dodge the 2005 algorithms' O(n²) blowup;
  upstream now has Myers greedy/linear and `OptimisticXMLProcessor`. Benchmark
  whole-body diffs without the splitter — if acceptable, the Eclipse LCS copy and much
  of `Docx4jDriver` could eventually be retired, shrinking the module to `Differencer`
  + XSLTs.
- Upstream anything Phase 3 forces us to patch.

## Risks / open questions

- **Output-markup drift is the make-or-break item** — hence goldens before any code
  changes.
- Diff *quality* may change (different LCS algorithms → different but equally valid
  edit scripts). Goldens catch this; where output differs but is semantically fine,
  bless the new golden manually by round-tripping in Word.
- `Differencer.toWML`'s namespace workarounds (`a14`, `w14`, `o` injection) may become
  redundant or misfire against the new writer — retest.
- The change warrants at least a minor-version release (e.g. 17.1.0), given
  `com.topologi.*` disappears from the published jar.

## Sequencing

Phase 0 is standalone and worth doing regardless. Phases 1–3 are one PR (the build is
broken in between). Phase 4 rides along. Phase 5 is separate.
