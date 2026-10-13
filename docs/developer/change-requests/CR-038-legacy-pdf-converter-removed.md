# CR-038: The legacy PDF converter removed - docx4j-legacy-service, its hooks in Docx4J and TocGenerator, and every mention

Status: PROPOSED 2026-10-13.  Drafted on Jason's request ("please draft a CR to delete the legacy
PDF conversion service from the reactor, docx4j facade, docx4j.properties and wherever else it may
be mentioned (GettingStarted?, etc/build.xml?, TOC update code/sample?)").  Not implemented.  Owner:
Jason Harrop.  One phase.

Scope: the `docx4j-legacy-service` module, the code in docx4j-core that reaches it, and every
mention of it in the build, the configuration samples, the logging configurations, the legal
notices, the release notes and the Getting Started guide (§2 is the inventory).  Not in scope: the
8.3.x line (`VERSION_8_3_16`), which keeps the module; the other three PDF routes (export-FO,
documents4j, Microsoft Graph), beyond the messages that name the converter; the Microsoft Graph
module, whose `DocxToPdfConverter` and friends are its own (`org.plutext.msgraph`), not this; the
CHANGELOG's history and older CRs' mentions (CR-005 §371), which stay as the record.

## 1. What it is

Plutext's commercial PDF Converter was a web service, offered until 2019; the Getting Started guide
says "That product is no longer available".  docx4j 3.3.0 made it the default route for PDF output
and for a table of contents' page numbers (CHANGELOG, 3.3.0: "changed from XSL FO + Apache FOP to
Plutext's PDF Converter"); 3.3.7 moved it to https; 6.1.0 moved its default endpoint to localhost
("to avoid information leakage"); 8.3.1 moved the client
out of docx4j-core into its own module ("Move legacy PDF converter out of docx4j-core, reducing
core deps"), which docx4j-core reaches by reflection, so that core doesn't depend on it.

- **The module**: `docx4j-legacy-service`, package `org.docx4j.services.client`, seven classes
  (`Converter`, `ConverterHttp`, `Format`, `JsonUtil`, `MyRetryHandler`, `ConversionException`,
  `ConversionRateLimitException`); JPMS module `org.docx4j.services.legacy`.  Its dependencies are
  its own: Apache httpclient 4.5.13 (and httpcore), jackson-core 2.18.6, jackson-databind 2.18.1.
- **Published** to Maven Central with every release from 8.3.1 to 17.3.2: it is in the reactor, so
  each release deploys it (RELEASE_HOWTO's sample log lists it as the reactor's last module).
  The Jackson and httpclient bumps in its history are GitHub's dependency alerts on it.
- **Its callers**: two, both in docx4j-core and both by reflection (§2).  Nothing in the samples,
  the tests, docx4j-mcp or the Enterprise repository calls it (searched 2026-10-13; the Enterprise
  tree has its logger name only in three untracked patch files).

## 2. Where it is mentioned (VERSION_17_3_3, 2026-10-13)

| Where | What | Change |
|---|---|---|
| `pom.xml`, line 78 | `<module>docx4j-legacy-service</module>` | remove |
| `pom.xml`, lines 179-182 | the comment on commons-codec's management names "httpclient (docx4j-legacy-service)" | drop that clause; the management stays (Santuario's xmlsec in docx4j-digsig still asks for an older commons-codec) |
| `docx4j-legacy-service/` | the module | delete (`git rm -r`; the untracked `bin/` and `target/` locally too) |
| `Docx4J.toPDF` | `else if (pdfViaLegacyConverter())`: reads `com.plutext.converter.URL` (default `http://localhost:9016/v1/...`), saves the package, calls `ConverterHttp.convert` by reflection | remove the branch; the final `else` throws "No PDF Converter found" as it does today |
| `Docx4J` | `public static Boolean EXPORT_LEGACY_CONVERTER_DETECTED`; `public static boolean pdfViaLegacyConverter()` (`@since 8.3.1`) | remove (D1) |
| `Docx4J.FOExporterVisitorGetInstance` | its exception's message ends "Doing so will disable Plutext's PDF Converter." | drop that sentence |
| `TocGenerator`, class javadoc (line 79) | "export-fo (or the no-longer-available Converter)" | drop the parenthesis |
| `TocGenerator.getPageNumbersMap` (lines 683-693) | `else if (Docx4J.pdfViaLegacyConverter())` → `getPageNumbersMapViaService()`; the final `else` says "try using docx4j-export-documents4j-local\|remote or docx4j-export-fo" | remove the branch; the message names docx4j-export-fo alone, since documents4j has never supplied page numbers here (only FOP or the service did) |
| `TocGenerator.getPageNumbersMapViaService` (lines 696-760) | saves the package, posts it to the endpoint, reads the bookmarks' page numbers back as JSON through `JsonUtil`, both by reflection | remove (and the imports it alone used) |
| `docx4j-samples-resources/.../docx4j.properties`, lines 318-325 | a commented block on `com.plutext.converter.URL` ("Plutext's now retired commercial converter") | remove; the key is set nowhere |
| `docx4j-samples-resources/.../logback.xml` (149), `log4j2.xml` (135); `docx4j-core-tests/src/test/resources/logback.xml` (148) | a logger for `org.docx4j.services.client` | remove |
| `legals/NOTICE`, lines 66-67 | `MyRetryHandler`'s attribution (a modified Apache httpclient class) | remove |
| `etc/build.xml`, lines 97-105 | a commented-out list of the module's jars ("we don't add them to the zip") | remove |
| `RELEASE_HOWTO.md`, line 168 | the module's line in a sample build log | remove |
| `docs/Docx4j_GettingStarted.docx` (and the md, html, pdf generated from it) | "Note: For a period to 2019, Plutext offered a commercial PDF Converter.  That product is no longer available." | D3 |
| `CHANGELOG.md` | the 3.3.0, 3.3.7, 6.1.0 and 8.3.1 entries | keep; a 17.3.3 entry saying the module is gone, its last release 17.3.2, and what to use instead |
| `CLAUDE.md`, the module map | doesn't mention it | add a line, as for docx4j-docx-anon: "docx4j-legacy-service ends at 17.3.2; the module was deleted for 17.3.3" |
| the TOC samples (`Toc*` in docx4j-samples-docx4j), README, docs/*.md | no mention | none |

The guide's comparison of the PDF routes (`Docx4j_GettingStarted_files/image1.png`) covers
export-FO, Microsoft Graph and documents4j only: no change.  The README's "choice of 3 strategies"
is those three: no change.

## 3. What changes for a user

- **Without docx4j-legacy-service on the classpath** (the usual case, and every case since 2019
  unless someone kept their own converter running): nothing.  `toPDF` tries documents4j (remote,
  then local), then export-FO, then Microsoft Graph, then throws, as today; a TOC's page numbers
  come from export-FO or the same exception.
- **With it on the classpath and a converter instance of their own** (`com.plutext.converter.URL`):
  their build first meets the missing 17.3.3 artifact; PDF output then needs docx4j-export-fo or
  documents4j, and TOC page numbers docx4j-export-fo.  17.3.2's artifact stays on Central.
- **Binary**: a caller compiled against `Docx4J.pdfViaLegacyConverter()` or
  `EXPORT_LEGACY_CONVERTER_DETECTED` gets `NoSuchMethodError` or `NoSuchFieldError` (D1).
- **Dependencies**: httpclient, httpcore and that Jackson leave docx4j's published artifacts (the
  Microsoft Graph samples ask for httpclient themselves), and with them the alerts.

## 4. Decisions for Jason

- **D1, Docx4J's two public members.** (a) Delete them in 17.3.3.  (b) Deprecate them in 17.3.3
  (the method returning false, the field left) and delete them in a later minor release.
  Recommendation: (a).  They only say whether a class of a module no longer published is on the
  classpath; anything that called them did so to choose that route, which is gone.
- **D2, the artifact.** (a) Stop publishing it: 17.3.2 is its last release, said in the CHANGELOG.
  (b) Publish a last 17.3.3 that is empty, or a relocation pom.  Recommendation: (a), as
  docx4j-docx-anon ended at 17.2.0 and was deleted for 17.3.0; a relocation has nowhere to point.
- **D3, the guide's note.** (a) Remove it: after this docx4j has nothing of the converter, and the
  CHANGELOG keeps the history.  (b) Keep it, for readers of the old blog posts that describe the
  converter.  Recommendation: (a).
- **D4, the release.** 17.3.3, the next release (recommended; the change is a deletion with nothing
  to measure), or 17.4.0 if D1 is (b) and the deletion should wait for a minor release.

## 5. The phase

1. Delete the module and its `<module>` line; drop the pom comment's clause.
2. `Docx4J`: the `toPDF` branch, `pdfViaLegacyConverter`, `EXPORT_LEGACY_CONVERTER_DETECTED`, the
   sentence in `FOExporterVisitorGetInstance`'s message; imports left unused.
3. `TocGenerator`: the branch, `getPageNumbersMapViaService`, the javadoc's parenthesis, the
   message naming docx4j-export-fo alone; imports left unused.
4. The configuration and logging files, NOTICE, `etc/build.xml`, RELEASE_HOWTO.
5. The guide (D3) edited with docx4j and its md, html and pdf regenerated by
   `etc/GenGettingStartedDocs.java`; CHANGELOG; CLAUDE.md's module map; the registry.

**Gate**: `mvn clean install` (the reactor builds without the module; nothing depends on it);
core-tests and export-fo-tests in full (the TOC's page numbers through FOP: `TocGenerateTest`,
the two `TocUpdateFacadeTest`s, the Toc* layout tests); a search of the tree for `services.client`, `legacy-service`, `pdfViaLegacyConverter`,
`EXPORT_LEGACY_CONVERTER`, `com.plutext.converter` and `ConverterHttp` finding only the CHANGELOG's
history and older CRs; `mvn dependency:tree` on the published modules without httpclient; the
release's artifact list one shorter (the release verification's pom parity, `RELEASE_HOWTO`).  No
corpus gate and no Office check: where docx4j-export-fo is present its branch comes before the
legacy one, so no document's output can change.

## 6. Risks

- **Someone still runs their own converter.**  The CHANGELOG entry says what replaces it, and 17.3.2
  stays on Central for whoever needs time.
- **The 8.3.x line keeps the module**: a backport touching `Docx4J.toPDF` or `TocGenerator` there
  must not assume it gone.
- **Deleted files coming back**: an open editor (Eclipse's `bin/`, an IDE buffer) can write the
  module's files back; the commit's stat must show the deletes.

## 7. Seen beside it (not in scope unless Jason adds them)

- `toPDF`'s Microsoft Graph branch logs an error ("can't be invoked via this interface") and then
  returns normally, having written nothing: a caller whose only PDF route on the classpath is
  Microsoft Graph gets an empty stream and no exception.  Throwing a `Docx4JException` there would
  be one line.
- The guide's comparison table (image1.png) still calls export-FO "Suitable for simple documents
  (text, tables, supported image types, header/footers)", which predates the Word-layout fidelity
  work (17.0.5 to 17.3), equations, metafiles and charts: a documentation update of its own.

## 8. Effort

Half a day, with the build and the two test suites.
