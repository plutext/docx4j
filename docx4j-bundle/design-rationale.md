# docx4j-bundle: publishing to Maven Central — design rationale

*Written 2026-09-02, deciding the artifact shape for publishing the fat jar to
Maven Central (motivated by `docs/Docx4j_from_Python.md`, where JPype/Python
users currently have to build the bundle themselves).*

## Short answer

Publishing the fat jar to Central is feasible; the blocker is self-imposed, not
a Central limitation. Central's component validation requires each published GAV
to ship a `-sources.jar` and a `-javadoc.jar` (plus the pom, GPG `.asc`
signatures, and checksums) — but it validates their **presence and signatures,
not their contents**. Empty stub jars pass, which is how uber-jar projects
publish to Central. The `docx4j-bundle` module currently just opts out of
producing them.

## Why it fails today

`docx4j-bundle/pom.xml` sets three deliberate skips:

- `maven.javadoc.skip=true` → no `-javadoc.jar`
- `maven.deploy.skip=true` → never deployed
- `nexus-staging` `skipNexusStagingDeployMojo=true` → excluded from staging

and in the parent pom the module is commented out of `<modules>` with the note
"docx4j-bundle needs Sources, Javadoc in order to pass Central Publishing
component validation".

Helpfully, the bundle has **no `src/` of its own** (the main jar is ~2.8 KB,
essentially a manifest). That makes the stub jars easy:

- the inherited `maven-source-plugin` produces a harmless empty `-sources.jar`;
- the javadoc skip exists to dodge the "unnamed module" javadoc error, but that
  error comes from *documenting docx4j's real classes* — the bundle has none, so
  un-skipping yields a clean empty `-javadoc.jar`. (Confirm with one build; some
  javadoc-plugin versions grumble on a source-less project, in which case an
  empty-jar fallback is trivial.)

## The finding that affects both shapes: the deployed pom

The bundle's deployed pom (`.flattened-pom.xml`) currently declares
**`docx4j-core` as a compile dependency** (with a pile of exclusions for what
got shaded in). Because the parent uses `flattenDependencyMode=all`, flatten
bakes that in and overrides shade's normal `dependency-reduced-pom`.

Consequence: as published today, a Maven consumer resolving `docx4j-bundle`
would pull the ~20 MB fat jar **plus** `docx4j-core` unshaded — the core classes
exist in both, so duplicate classes on the classpath. **This must be fixed
regardless of which shape is chosen**: the bundle's deployed pom needs an empty
(or near-empty) `<dependencies>`, i.e. a per-module flatten override. That is the
real work item, and it is shape-independent.

Once the pom is emptied, the shape decision is cleanly about the *artifact
filename* and the *bare-GAV behaviour*.

## The shapes

| | A: `-shaded` classifier (empty main jar) | B: fat jar is the main artifact |
|---|---|---|
| Download URL (the Python case) | `…/docx4j-bundle-<v>-shaded.jar` | `…/docx4j-bundle-<v>.jar` |
| Bare GAV resolves to | the empty ~2.8 KB jar (inert) | the fat jar |
| To get the fat jar as a Maven dep | must add `<classifier>shaded</classifier>` | just the GAV |
| Accidental-misuse risk (added as a normal dep) | low — they get nothing useful | higher — they get 20 MB; with `docx4j-core` also present, duplicate classes |
| Central "empty main artifact" | main jar empty — accepted but unusual; classifier jar carries content and needs its own signature | main jar has content — the normal case |
| sources/javadoc/signature burden | on the (empty) main GAV, plus a signature on the classifier jar | on the main GAV (the fat jar) |
| Matches today's local-build name (`-shaded`, already in the Python doc) | yes | no — a rename (but the doc changes anyway) |

Option C — a distinct artifactId purely for the uber jar — is unnecessary:
`docx4j-bundle` already *is* that distinct name, so B covers it.

## What tips it

The audience that motivates this (JPype/Python, and anyone putting one jar on a
classpath) **downloads a single file by URL** — they do not use Maven resolution.
For them, B's URL (`docx4j-bundle-<v>.jar`) is the obvious, guessable one; A
forces them to know the `-shaded` classifier.

The only thing A buys is a foot-gun guard for Maven consumers: a careless
`implementation 'org.docx4j:docx4j-bundle'` pulls an inert empty jar instead of
20 MB. But with the deployed pom emptied (done anyway), B's downside shrinks to
"a user who explicitly adds *both* bundle and core gets duplicates" — and no
shape prevents that; it is user error.

## Decision: B

**Fat jar as the main artifact, with an emptied deployed pom.** It gives the
clean, intuitive download URL Python users want, avoids Central's empty-main-jar
oddity, and the duplicate-classes concern that seems to favour A is actually
resolved by the pom fix both shapes require. The `-shaded` classifier could be
kept as an *additional* attached artifact for continuity with existing local
build scripts, but it is not worth it — update the doc instead.

Pick A only if "the bare coordinate must be inert / Maven-safe by default" is a
hard requirement valued above the clean filename.

### slf4j (resolved during the prototype)

`slf4j-api` is **bundled into the fat jar** (its `org/slf4j/**` classes, including
the built-in NOP fallback provider), and the deployed pom declares **no**
dependencies at all — not even slf4j-api. The earlier "leave slf4j-api as the one
declared dependency" idea was rejected: the whole point of the bundle is a single
self-contained jar for standalone/JPype use, so the API must travel inside it.

There is deliberately **no logging provider** in the jar. docx4j only calls the
slf4j API; the user adds a provider (slf4j-simple, logback, …) to get output —
the Python doc already says so. Trade-off: because nothing is relocated, if a
consumer also puts an external `slf4j-api` on the classpath there will be two
copies of `org.slf4j.*`; for the standalone/single-jar use case that is fine (and
avoided by simply not adding another slf4j-api).

## Remaining considerations (not blockers)

- **License/NOTICE aggregation (done).** The fat jar embeds all transitive
  bytecode (JAXB RI, SLF4J, commons, etc.), all permissive (Apache-2.0/EPL/MIT).
  The shade config now uses `ApacheNoticeResourceTransformer` (aggregates every
  bundled `NOTICE` into one `META-INF/NOTICE`, Apache-2.0 §4d) and
  `ServicesResourceTransformer` (concatenates colliding `META-INF/services/*`
  provider lists — needed so JAXB context discovery and other SPI lookups still
  resolve after shading). The canonical Apache-2.0 `META-INF/LICENSE` is shipped
  as a module resource (`src/main/resources/META-INF/LICENSE`, copied from
  `legals/LICENSE`). `ApacheLicenseResourceTransformer` is intentionally **not**
  used: in this shade version it only *consumes* (drops) LICENSE files without
  emitting one, which would delete our canonical LICENSE too. Individual
  dependencies' extra `LICENSE.txt`/`LICENSE.md`/`NOTICE.md` files are left in
  place as additional attribution. A fully rigorous per-dependency license
  manifest (eg license-maven-plugin's `THIRD-PARTY.txt`) is a possible future
  refinement, beyond simple aggregation.
- **shade vs flatten pom interaction.** Both rewrite the deployed pom; ensure the
  deployed pom is one clean version with no bundled deps re-declared.
- **Doc update.** Once on Central, flip `docs/Docx4j_from_Python.md` from
  "build it yourself / not on Central" to the Central coordinate, and drop the
  local-build instructions.
- **Size.** ~20 MB — within Central limits.
- **Central Portal migration.** The parent still uses `nexus-staging` →
  `ossrh-staging-api.central.sonatype.com`, which already publishes through the
  Portal and enforces the same requirements, so no new publishing infra is
  needed. Migrating to `central-publishing-maven-plugin` is orthogonal.
