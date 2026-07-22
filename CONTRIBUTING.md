# Contributing to docx4j

Thank you for your interest in contributing to docx4j. Contributions are
accepted under the [Apache License, Version 2.0](legals/LICENSE), the same
license the project is distributed under.

## Certifying your contribution (DCO)

docx4j uses the [Developer Certificate of Origin](https://developercertificate.org/)
(DCO). By adding a `Signed-off-by:` line to each commit, you certify that you
wrote the contribution, or otherwise have the right to submit it under the
Apache License v2, as set out in the DCO text.

Add the sign-off with the `-s` flag when committing:

```bash
git commit -s -m "Fix widget frobnication (issue 123)"
```

which appends a trailer of the form:

```
Signed-off-by: Your Name <your.email@example.com>
```

Use your real name and a working email address. The sign-off must be added by
a human: automated tools and AI agents must not add `Signed-off-by:` lines
(see below).

Pull requests are checked for a valid sign-off on every commit. If you forgot
it, you can amend with `git commit --amend -s` or
`git rebase --signoff HEAD~<n>`.

Corporate contributors whose employers require a signed agreement may
additionally use the corporate contributor agreement in
[`legals/`](legals/docx4j_CorporateContributor.pdf), but the DCO sign-off is
what is required for every pull request.

## AI-assisted contributions

Contributions written with the help of AI tools (Claude, Copilot, and similar)
are welcome, subject to the following conditions:

1. **You are responsible for the contribution.** You must have personally
   reviewed and understood every part of the change, and be able to explain
   and defend it in code review. Bugs, regressions, licensing problems, and
   provenance issues in AI-assisted code remain your responsibility, exactly
   as if you had typed it yourself. Pull requests whose submitter cannot
   answer questions about the code will be closed.

2. **Tool terms must be compatible.** The terms of use of the AI tool must not
   place restrictions on its output that are inconsistent with the Apache
   License v2 or the Open Source Definition.

3. **No third-party code laundering.** Do not submit AI output that you know
   or suspect reproduces identifiable third-party code. If your tool flags
   output as matching existing licensed code, either comply with that license
   (including any NOTICE/attribution obligations) or do not submit it.

4. **Disclose AI assistance** with a commit trailer. Preferred form:

   ```
   Assisted-by: Claude Code (claude-fable-5)
   ```

   `Generated-by:` and `Co-Authored-By:` trailers are also accepted. Purely
   mechanical assistance (autocomplete of a line or two) does not need
   disclosure; generation of substantive logic does.

5. **Only humans sign off.** The `Signed-off-by:` line certifies the DCO and
   must be added by you, not by the tool. Do not configure an AI agent to add
   sign-offs on your behalf.

## Practical guidelines

- **Target branch**: each release lives on its own `VERSION_x_y_z` branch.
  Base your pull request on the current development branch (the most recent
  `VERSION_x_y_z` branch — at the time of writing, `VERSION_17_0_x`). If in
  doubt, ask in the issue first.
- **Discuss first**: for anything beyond a small fix, please open an issue
  describing the problem before investing effort in a pull request.
- **Build**: `mvn clean install` (JDK 11+). The JAXB object model is generated
  at build time, so a full `mvn install` is needed once before IDEs or
  individual modules will compile.
- **Tests**: the suite lives in `docx4j-core-tests` (JUnit 4). Run it with
  `mvn test -pl docx4j-core-tests -am`. Please add or extend a test where
  practical.
- **Changelog**: add a line to `CHANGELOG.md` for user-visible changes.
- **Style**: match the conventions of the surrounding code; use Unix (LF)
  line endings.
