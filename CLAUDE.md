# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

docx4j is an open source (Apache v2) Java library for creating, editing, and saving OpenXML packages: docx, pptx, and xlsx. It uses JAXB (Jakarta XML Binding 4.0) to create the Java representation. Multi-module Maven project; Java 11 baseline (`<release>11</release>`), JPMS-modularised (modules ship `module-info.java`).

## Build and test commands

```bash
mvn clean install              # full build; works on Linux, macOS and (from 17.0.5) native Windows (see CR-009)
mvn install -DskipTests        # skip tests

# Run tests in the test module (requires upstream modules built: -am)
mvn test -pl docx4j-core-tests -am

# Run a single test class
mvn test -pl docx4j-core-tests -am -Dtest=XmlUtilsDeepCopyTest
```

Notes:
- **Generated sources**: `org.docx4j.wml`, `org.docx4j.vml`, `org.docx4j.dml` etc. are generated at build time by XJC in `docx4j-generated-objects` (from `xsd/ROOT.xsd` into `target/generated-sources/xjc`). They are NOT checked in — you must run `mvn install` at least once before the rest of the reactor (or an IDE) can compile. Only a few hand-written interfaces (e.g. `ContentAccessor`, `SdtElement`) live in that module's `src/main/java`.
- **Adding a schema** (a namespace Office writes that docx4j does not bind yet): follow `docs/developer/adding-a-schema.md` — the xsd and its JAXB annotations, the clean regeneration, module-info, the three JAXB context lists, the prefix table, a Part and its registration, tests with the Office-open check, the hand-off to the TypeScript objects package, one commit.
- Tests are **JUnit 4** (4.13.2, pinned in the parent pom). Surefire in docx4j-core-tests forks one JVM per test class (needed so the configured XPath implementation takes effect) and excludes `**/samples/*.java` and `AbstractNumberingTest.java`.
- Test resources load via classpath (`ResourceUtils.getResource("loadAndSave.docx")`) from `docx4j-core-tests/src/test/resources/`, which also holds the test `docx4j.properties`.
- Version is set via the `${revision}` property in the parent pom (flatten-maven-plugin produces the published poms). An `OSGi` profile switches packaging to `bundle`.

## Branch conventions

Each release lives on its own `VERSION_x_y_z` branch (e.g. `VERSION_17_0_1` is current 17.0.1-SNAPSHOT development). `master` is old; pick the branch matching the version you're working on. Update `CHANGELOG.md` for user-visible changes. Documented change requests (CRs) for non-trivial work live in `docs/developer/change-requests/` (see its README); update the relevant CR as phases land.

## AI attribution in commits

- Stamp your own session's model (`Co-Authored-By: Claude <model>` +
  `Claude-Session: <url>`, per the harness default). Never copy the model
  name from trailers in git history — earlier commits were made by other
  sessions on other models.
- If another model materially contributed in this session (a subagent run
  with a model override), add a second `Co-Authored-By` line for it.
- If another Claude session reviewed or directed this work before push, add
  `Reviewed-by: Claude <model> <noreply@anthropic.com>` (plus that session's
  `Claude-Session` URL on the following line if known).
- The `Co-Authored-By` model names feed the release's "Contributors" block
  in `CHANGELOG.md`, so accuracy here matters.

## Module map

- **docx4j-core** — the main library. Note `org/pptx4j`, `org/xlsx4j`, `org/glox4j` (SmartArt), and `org/opendope` are top-level *packages inside docx4j-core*, not separate modules.
- **docx4j-generated-objects** — XJC-generated JAXB object model (see above). Uses the **docx4j-xjc-copy** XJC plugin to generate fast deep-copy methods, and a parent-pointer plugin (`org.jvnet.jaxb.lang.Child`). **docx4j-copy** holds the `Copyable` interface both depend on.
- **docx4j-JAXB-ReferenceImpl** / **docx4j-JAXB-MOXy** — pick-one runtime selector modules. Consumers add exactly one as a dependency; selection is by classpath (META-INF/services), not a property. `docx4j-JAXB-Internal` on disk is retired (no pom.xml, not in the reactor).
- **docx4j-core-tests** — the test suite (runs against docx4j-JAXB-ReferenceImpl). `docx4j-diffx-tests` and `docx4j-export-fo-tests` exist on disk but are not in the reactor `<modules>` list, as is `docx4j-bundle` (fat jar, commented out).
- **docx4j-export-fo** (+ four `-fonts-*` modules) — docx→PDF via XSL-FO/Apache FOP. Other PDF strategies: **docx4j-documents4j-local/-remote** (drive MS Word via documents4j) and **docx4j-conversion-via-microsoft-graph**.
- **docx4j-diffx** — docx differencing; **docx4j-docx-anon** — document anonymization.
- **docx4j-samples-\*** — runnable examples per capability; **docx4j-samples-resources** holds shared sample files.

## Core architecture (docx4j-core)

Three layers, bottom-up:

1. **OPC packaging layer** (`org.docx4j.openpackaging`): `OpcPackage` (abstract, extends `Base`) is the container — subclasses `WordprocessingMLPackage`, `PresentationMLPackage`, `SpreadsheetMLPackage`. A package holds a `Parts` collection keyed by `PartName`, a `ContentTypeManager`, and a relationships graph (`RelationshipsPart`). XML parts backed by JAXB extend `JaxbXmlPart` → `JaxbXmlPartXPathAware`. Format-specific parts live under `parts/WordprocessingML/`, `parts/PresentationML/`, `parts/SpreadsheetML/`, `parts/DrawingML/`. I/O uses `openpackaging/io3/` (`Load3`, `Save`) with pluggable `PartStore` strategies (`ZipPartStore`, `UnzippedPartStore`); `io/` is legacy.

2. **JAXB layer** (`org.docx4j.jaxb`): `Context` holds the static `JAXBContext jc` for the main content model plus many per-schema contexts (`jcRelationships`, `jcContentTypes`, `jcDocPropsCore`, ...). It detects which JAXB runtime got loaded (RI vs MOXy, via the context's class name) and sets `Context.jaxbImplementation`; namespace-prefix mappers in the selector modules are chosen to match. `XmlUtils` is the central marshal/unmarshal/deep-copy utility (`deepCopyFast` is the default since 17.0.0); `TraversalUtil` is the visitor for walking the content tree; JAXB content classes expose `getContent()`.

3. **Model/facade layer**: `org.docx4j.Docx4J` is the high-level facade (`load`, `save`, `toPDF`, `toHTML`, bind, with `FLAG_*` constants) delegating to exporters in `org.docx4j.convert.out.*`. `org.docx4j.model` holds higher-level abstractions over raw JAXB objects — notably `PropertyResolver` (effective style/formatting resolution), `model/datastorage` (OpenDoPE content-control data binding), `model/listnumbering`, `model/fields`, `model/structure`.

**Configuration**: `org.docx4j.Docx4jProperties` loads a single `docx4j.properties` from the classpath at static init (warns if absent); typed getters plus programmatic `setProperty`. Behavior toggles throughout the codebase (image handling, binder eagerness, fonts, JAXB output formatting) are keyed through it. A reference file is `docx4j-samples-resources/src/main/resources/docx4j.properties`.

## Model guidance

Most tasks here (tests, docs, routine fixes, single-layer features, refactors)
are fine on Opus. Prefer Fable 5 for:

- Cross-layer debugging, especially PDF output / XSL-FO / font-selection issues
- Complex-script and bidi behavior
- OpenDoPE content-control data binding (`org.docx4j.model.datastorage` and
  related binding/bind-flag code)
- JAXB context or classloading problems
- Changes to widely-shared code (`XmlUtils`, `Context`, `Preprocess`, the
  `io3` load/save path)

If you are a smaller model and an investigation keeps not converging, say so
and suggest rerunning on a stronger model rather than guessing.

## FOP defects and limitations

Anything the XSL-FO/PDF work has to route around in Apache FOP is recorded, when found, as a
numbered item in section 6.6 ("FOP limitations for Phase 6") of the Enterprise CR-001,
`../Plutext-Enterprise-Java-11/docs/developer/change-requests/CR-001-word-layout-fidelity.md`,
with the measurement, the docx4j-side workaround (named, so it can be dropped when a fixed FOP
ships) and the upstream status; update that section's closing "Open:" tally. The fix itself goes
into the fork at `../xmlgraphics-fop-plutext` on a branch named for it, with the JIRA text drafted
for Jason to file (JIRA in project FOP, then a GitHub PR titled `FOP-####: ...`, as FOP-3328 and
FOP-3330 were), and the commit subject takes the number once it exists. docx4j-export-fo builds
against released FOP, so a docx4j workaround is still needed until the fix is released.

## Portfolio task registry

This repository's change requests are indexed, with their dependencies on work in the other
docx4j repositories, in `../docx4j-portfolio/tasks.yaml` (ids `<repo>/<CR>[.<phase>]`; this
repository's key is `docx4j`).

- When a CR's status changes (a phase lands; a CR is proposed, deferred or abandoned) or its
  dependencies change, update the matching entry in `tasks.yaml` in the same session (`status`,
  `depends_on`; add an entry for a new CR or phase).
- Then run `python3 ../docx4j-portfolio/scripts/tasks.py check`. It reports `CHANGED` for each CR
  whose Status line was edited; once the registry entry agrees, run `tasks.py accept` (and
  `tasks.py graph` if dependencies changed).
- Before starting a CR or phase, check `python3 ../docx4j-portfolio/scripts/tasks.py blocked`: it
  may be waiting on work in another repository. The TypeScript and Python ports treat this
  repository as their behavioural oracle, so a CR here can unblock (or change) work there.
