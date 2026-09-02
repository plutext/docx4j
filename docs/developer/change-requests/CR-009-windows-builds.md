# CR: Building docx4j on native Windows

Status: DONE (2026-09-02) — both phases shipped; native Windows builds should
now work (first real Windows build report will confirm; the "some tests may
fail on Windows" font caveats still apply)
Scope: the build only (etc/build.xml release tooling and runtime behaviour out of scope)
Phases: 1. line endings — repo-root `.gitattributes` (aed86162d, 17.0.4 cycle);
2. port `modify-generated-sources.sh` → `ModifyGeneratedSources.java` (c47957cf5,
17.0.5 dev; byte-identical on the same XJC input, idempotent, .sh deleted)

## Background

An audit (2026-09-02) of what stops `mvn clean install` on native Windows
found two blockers: CRLF checkouts (Git for Windows default
`core.autocrlf=true`) breaking the patch script and the diffx golden
fixtures that `GoldenOutputTest` compares byte-for-byte, and the patch
script itself being bash.  The analysis and decisions below are kept as
written; the smaller items at the end were audited as non-blockers.

## The (fixed) hard blocker: modify-generated-sources.sh

This module's pom binds exec-maven-plugin at `process-sources` to run
`modify-generated-sources.sh`, which patches the XJC output (parent pointers,
SdtElement signatures, VML attribute order, etc).  Every other module depends
on this one, so on native Windows the build dies at the third module in the
reactor: exec-maven-plugin launches executables via `CreateProcess`, which
cannot run a `.sh` — even when the user's shell is Git Bash.  The script's own
header already says as much:

> TODO: This bash script means docx4j will not build on Windows. Do we need
> to provide an equivalent batch file or PowerShell script for Windows? Or
> assume everyone can use WSL?

Until it is ported, the de facto requirement is WSL (or any Unix userland)
with `bash`, `perl`, `find` and `sed` — and, before `.gitattributes` existed,
an LF checkout (`core.autocrlf` unset/`input`), since a CRLF `\r` in the
shebang or in the heredoc search strings broke the script even under WSL.

## Recommended port: a tiny Java class, source-launched (not Groovy)

Two candidates were considered for a cross-platform replacement:
groovy-maven-plugin (today that means gmavenplus) running an inline/external
Groovy script, or a plain Java class.  **The Java class wins.**  Reasoning:

1. **Zero new dependencies.**  gmavenplus drags the Groovy runtime (several
   MB) into the build, with its own JDK-compatibility cadence — Groovy has
   historically lagged new JDK releases, and docx4j routinely builds on new
   JDKs early.  The Java option needs nothing beyond the JDK the build
   already requires.
2. **No chicken-and-egg, thanks to JEP 330.**  The patcher must run at
   `process-sources`, *before* this module compiles anything — so
   `exec:java` (which needs a compiled class on a classpath) would force the
   patcher into an upstream module and either leak a dependency into the
   published pom or need plugin-dependency contortions.  Instead use
   `exec:exec` with `<executable>java</executable>` and the source file as
   the argument: `java ModifyGeneratedSources.java` (single-file source
   launch, JDK 11+).  No compile step, no module, no classpath.
3. **Same language as the repo.**  Every contributor can read and review it.
   The script's logic is only literal search/replace plus idempotence
   markers — `String.indexOf`/`replace` and `Files.readString`/`writeString`
   with an explicit `StandardCharsets.UTF_8`.  That also retires the
   perl `\Q..\E` escaping, the GNU-vs-BSD `sed` fork for macOS, and the
   dependence on `find` — the byte-exactness the script warns about
   ("Make sure your editor/formatter is not configured to remove trailing
   spaces") is far easier to keep inside one Java file.
4. **What Groovy would have bought** — terser syntax, scripts inline in the
   pom — doesn't pay here: at ~500 lines of search/replace text the patch
   strings belong in a standalone file either way, and an external `.groovy`
   file vs an external `.java` file is a wash, minus the dependency.

Also considered and rejected: maven-antrun `<replace>` tasks (no clean
"fail loudly if the search string is absent" + idempotence-marker semantics,
which are the whole point of `patch_once`), and a custom Maven mojo
(overkill, plus its own bootstrap ordering in the reactor).

Porting notes:

- Keep the two properties of the script that matter: **fail the build** if a
  search string is not found (XJC output drifted), and **idempotence** via
  the `docx4j:patched:<id>` markers so a second `mvn install` is a no-op.
- Text blocks would be the natural way to hold the search/replace strings,
  but they need JDK 15+ *to run the patcher*.  The library targets
  `<release>11</release>`; if building with a bare JDK 11 must stay
  supported, use string concatenation instead (uglier, works everywhere).
  If the build JDK floor is in practice 17+, text blocks are fine.
- `<executable>java</executable>` is resolved per-platform by
  exec-maven-plugin (finds `java.exe` on Windows); pass
  `-Dfile.encoding=UTF-8` or (better) use explicit charsets everywhere.
- Delete `modify-generated-sources.sh` in the same commit the port lands,
  so the two can't drift.

## Decision: keep the bare-JDK-11 build floor (2026-09-02)

The port uses string concatenation, not text blocks: the pom's
`<release>11</release>` is treated as also meaning "buildable with a bare
JDK 11", and JEP 330 source launch runs the patcher on the build JDK, so
text blocks (JDK 15+) would have silently raised the build floor via a
side door.  Two further points sealed it:

- text blocks **strip trailing whitespace** unless escaped (`\s`) — and the
  CTLine search strings depend on trailing spaces in Javadoc lines, so the
  "natural" syntax is actually a trap for exactly these strings;
- with concatenation, every tab and trailing space is an explicit `\t` or
  a visible quoted space, which is more robust against editor/formatter
  cleanup than either the heredocs were or text blocks would be.

Revisit only if the build floor is deliberately raised for other reasons.

## Verification (2026-09-02, shipped in c47957cf5)

- **A naive rebuild-and-diff is confounded: XJC output is nondeterministic
  between runs.**  Building twice (same JDK, same inputs, minutes apart)
  produced different member *ordering* in generated classes neither patcher
  touches (eg `CTArc` attributes/getters shuffled).  Harmless for JAXB
  semantics, but it means generated code is not reproducible build-to-build
  — relevant to any future reproducible-builds effort, and to anyone
  tempted to diff generated trees across builds.
- **The verification that works: a controlled A/B on one frozen XJC
  output.**  Run `mvn generate-sources` once (XJC only — process-sources
  not reached, so unpatched), copy the tree twice, run the bash script on
  one copy and the Java tool on the other, `diff -r`: **byte-identical**.
  The heredocs were transcribed from `cat -A` output (tabs as `^I`, line
  ends as `$`), not by eye — rendered terminal output cannot distinguish
  tabs from spaces, and both the search side (CTLine trailing spaces) and
  the replace side (Highlight/Styles tab/space mix, including a trailing
  tab after one `} else {`) depend on exact bytes.
- **Idempotence**: a second run skips all 20 `patch_once` equivalents via
  the markers and changes nothing (tree still identical).
- **Downstream**: module + docx4j-core rebuild green; deep-copy tests pass.
- **Verified on a real Windows machine (2026-09-03): full `mvn clean
  install`, ALL TESTS PASS.**  The first run got through the whole
  reactor — checkout, XJC, the Java patcher, compile — to one font test
  that NPE'd where DejaVu Sans is absent
  (RunFontSelectorCalibriCheckBoxTest, fixed in 5ed7b6553 with a Segoe UI
  Symbol fallback + Assume guard, the pattern the newer RunFontSelector
  tests already use); with that fix the suite is green.  The long-standing
  "some tests may fail on Windows" caveat is retired (CLAUDE.md updated) —
  the font-dependent tests guard themselves with Assume/fallbacks now.
  Follow-through the Windows runs surfaced: five RunFontSelector tests'
  "Windows-only" guard was dead code (toLowerCase() vs "Windows"), so they
  had silently passed everywhere while testing nothing — fixed e0e9b1345
  (Assume, corrected comparison), after which their first-ever real
  execution exposed a stale expectation in the Japanese test (MS Mincho
  absent on stock Windows 10+; concession branch was gated on Windows 8's
  os.version — fixed 501c8682a, gate on the font's absence itself).
- **macOS verified too (2026-09-03): full build, all tests pass** — as
  predicted (the shell script's BSD-sed fork was the only macOS special
  case, and the Java port removed the need for it).  All three platforms
  are now green.

## Remaining smaller items (none are build blockers)

- **Known test flakiness on Windows**: CLAUDE.md's "some tests may fail on
  Windows" caveat predates this audit and is environment-dependent
  (fonts, mostly).  Enumerating them needs an actual Windows run.
- **Path length**: the longest tracked path is 127 chars, but generated
  sources go deeper (`target/generated-sources/xjc/org/pptx4j/com/microsoft/
  schemas/office/powerpoint/x2010/main/...`).  A deep clone directory can
  brush the legacy 260-char MAX_PATH on setups without `LongPathsEnabled`;
  modern Git + Windows 10+ handles it.  Worth one line in a build README if
  reports ever come in.
- **Checked and clean**: no Windows-illegal filenames, reserved device
  names, case-insensitive filename collisions, or symlinks among tracked
  files; no OS-specific Maven profiles; all other build plugins (XJC,
  docx4j-xjc-copy, shade, flatten, surefire) are pure Java.
